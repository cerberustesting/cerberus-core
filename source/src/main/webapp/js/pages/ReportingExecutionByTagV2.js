/*
 * Cerberus Copyright (C) 2013 - 2025 cerberustesting
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This file is part of Cerberus.
 *
 * Cerberus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Cerberus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Cerberus.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Campaign Report V2 — Alpine component.
 *
 * Data flow:
 *  - One full load from ReadTestCaseExecutionByTag (always fullList=ALL, every status/country on):
 *    filtering is done client-side, instantly, with no server round-trip.
 *  - Live updates over the central websocket: campaign.delta.{campaign} patches individual
 *    execution cells, campaign.update.{campaign} refreshes the CI verdict, campaign.done.{campaign}
 *    triggers a final reload. Every payload is filtered by tag (channels are keyed by campaign
 *    name — several tags of the same campaign may run concurrently).
 *  - Fallback: soft polling while executions are pending and the websocket is not available
 *    (tag without campaign, ws down...).
 */
function campaignReportV2() {
    return {
        // ── Core state ──
        tag: '',
        loading: false,
        loaded: false,
        error: '',
        tagObj: null,          // tagObject from the servlet
        columns: [],           // execution combinations: "ENV COUNTRY ROBOT"
        rows: [],              // one row per test/testcase, with execTab{column: cell}
        durationMax: 0,
        bugs: [],              // bugContent.bugSummary-like list (bug, test, testCase, status)
        manualList: [],
        statsList: [],         // statsChart.contentTable rows (per env/country/robot/app split)
        statsTotal: null,
        folderStats: [],       // testFolderChart per-folder aggregation
        labelStickers: [],
        labelRequirements: [],
        canUpdate: true,

        // ── Client-side filters ──
        activeStatuses: {},    // status -> bool
        activeCountries: {},   // country -> bool
        activeEnvs: {},        // environment -> bool
        activeRobots: {},      // robotDecli -> bool
        activeApps: {},        // application -> bool
        search: '',
        rowsMode: 'all',       // 'all' | 'hideOk' (keep flaky visible) | 'hideOkFlaky'
        onlyFlaky: false,      // keep only executions that needed retries
        onlyBugged: false,     // keep only test cases with an active bug
        onlyManual: false,     // keep only manual executions
        filtersOpen: false,    // advanced filters modal
        showCols: { prio: true, app: true, lastRun: true },

        // ── PDF / report export ──
        pdfOpen: false,
        pdfTemplate: 'executive', // 'executive' | 'detailed' | 'trend' | 'legacy'
        emailRecipient: '',
        emailSending: false,

        // ── Grid sort ──
        sortBy: 'test',        // 'test' | 'app' | 'priority' | 'lastRun'
        sortDir: 1,

        // ── Selection (mass actions) ──
        selected: {},          // queueId -> true

        // ── Live ──
        liveMode: 'idle',      // 'ws' | 'polling' | 'idle'
        liveFeed: [],          // most recent first: {ts, kind, status, test, testCase, env, country, text}
        _wsSubscribed: false,
        _wsChannels: [],
        _wsHandlers: [],       // [{event, handler}]
        _pollTimer: null,
        _reloadDebounce: null,

        // ── UI state ──
        sections: { bugs: true, manual: true, breakdown: true },
        breakdownTab: 'environment', // environment | country | robot | application | combination | folder | label
        labelTab: 'stickers',
        editDesc: false,
        editComment: false,
        draftDesc: '',
        draftComment: '',

        // ── Tag selector ──
        tagDdOpen: false,
        tagSearch: '',
        tagOptions: [],
        tagCampaigns: [],      // campaign names for the picker filter
        tagFilterCampaign: '', // only list executions of this campaign
        tagFilterCi: '',       // '' | 'OK' | 'KO' | 'RUN' (no verdict yet)
        _tagSearchTimer: null,

        statusOrder: ['OK', 'KO', 'FA', 'NA', 'NE', 'WE', 'PE', 'QU', 'QE', 'PA', 'CA'],
        statusColors: {
            OK: '#00d27a', KO: '#e63757', FA: '#f5803e', NA: '#94a3b8', NE: '#cbd5e1',
            WE: '#8b5cf6', PE: '#2c7be5', QU: '#60a5fa', QE: '#be123c', PA: '#f59e0b', CA: '#475569'
        },
        statusLabels: {
            OK: 'OK', KO: 'KO', FA: 'Failed', NA: 'Not Applicable', NE: 'Not Executed',
            WE: 'Waiting Exe', PE: 'Executing', QU: 'Queued', QE: 'Queue Error', PA: 'Paused', CA: 'Cancelled'
        },

        // ═══════════════════ INIT ═══════════════════
        init() {
            this.statusOrder.forEach(s => { this.activeStatuses[s] = true; });
            try {
                var savedSections = JSON.parse(localStorage.getItem('rtv2.sections') || 'null');
                if (savedSections) this.sections = Object.assign(this.sections, savedSections);
            } catch (e) { /* keep defaults */ }

            try {
                var savedTab = localStorage.getItem('rtv2.breakdownTab');
                if (savedTab) this.breakdownTab = savedTab;
                var savedCols = JSON.parse(localStorage.getItem('rtv2.showCols') || 'null');
                if (savedCols) this.showCols = Object.assign(this.showCols, savedCols);
            } catch (e) { /* keep defaults */ }

            this.tag = GetURLParameter('Tag') || '';
            this._loadTagOptions('');
            this._loadCampaignOptions();
            if (this.tag) this.loadReport();

            window.addEventListener('popstate', () => {
                var urlTag = GetURLParameter('Tag') || '';
                if (urlTag && urlTag !== this.tag) { this._teardownLive(); this._resetForTag(urlTag); this.loadReport(); }
            });

            this._bindWsLifecycle();

            this.$watch('sections', (v) => localStorage.setItem('rtv2.sections', JSON.stringify(v)));
            this.$watch('breakdownTab', (v) => localStorage.setItem('rtv2.breakdownTab', v));
            this.$watch('showCols', (v) => localStorage.setItem('rtv2.showCols', JSON.stringify(v)));
            this.$watch('pdfOpen', (v) => { if (v) this._prefillEmailFromHooks(); });

            window.addEventListener('beforeunload', () => this._teardownLive());
        },

        // ═══════════════════ TAG SELECTOR ═══════════════════
        _loadTagOptions(term) {
            var self = this;
            var url = 'ReadTag?iSortCol_0=0&sSortDir_0=desc&sColumns=id,tag,campaign,description&iDisplayLength=30'
                + (getUser().defaultSystemsQuery || '') + '&sSearch=' + encodeURIComponent(term || '')
                + (this.tagFilterCampaign ? '&sSearch_2=' + encodeURIComponent(this.tagFilterCampaign) : '');
            $.getJSON(url, function (data) {
                self.tagOptions = (data.contentTable || []).map(function (t) {
                    var c = self._tagCounters(t);
                    return {
                        tag: t.tag,
                        campaign: t.campaign || '',
                        description: t.description || '',
                        ciResult: t.ciResult || '',
                        DateCreated: t.DateCreated,
                        total: c.total,
                        okPct: c.okPct,
                        notOk: (t.nbKO || 0) + (t.nbFA || 0),
                        flaky: t.nbFlaky || 0
                    };
                });
            });
        },
        _loadCampaignOptions() {
            var self = this;
            $.getJSON('ReadCampaign?iDisplayStart=0&iDisplayLength=200&iSortCol_0=1&sSortDir_0=asc', function (data) {
                self.tagCampaigns = (data.contentTable || [])
                    .map(function (c) { return c.campaign; })
                    .filter(Boolean)
                    .sort();
            });
        },
        get filteredTagOptions() {
            var f = this.tagFilterCi;
            if (!f) return this.tagOptions;
            return this.tagOptions.filter(function (t) {
                if (f === 'RUN') return !t.ciResult;
                return t.ciResult === f;
            });
        },
        onTagSearchInput() {
            var self = this;
            clearTimeout(this._tagSearchTimer);
            this._tagSearchTimer = setTimeout(function () { self._loadTagOptions(self.tagSearch); }, 300);
        },
        pickTag(tag) {
            this.tagDdOpen = false;
            if (!tag || tag === this.tag) return;
            this._teardownLive();
            this._resetForTag(tag);
            InsertURLInHistory('ReportingExecutionByTagV2.jsp?Tag=' + encodeURIComponent(tag));
            this.loadReport();
        },
        _resetForTag(tag) {
            this.tag = tag;
            this.liveFeed = [];
            this.selected = {};
            var main = document.querySelector('main.crb_main');
            if (main) main.scrollTop = 0;
        },

        // ═══════════════════ LOAD ═══════════════════
        _reportParams() {
            var p = 'Tag=' + encodeURIComponent(this.tag) + '&fullList=ALL';
            this.statusOrder.forEach(function (s) { p += '&' + s + '=on'; });
            // every country on: the servlet checks each COUNTRY invariant against request params
            Object.keys(this.activeCountries).forEach(function (c) { p += '&' + encodeURIComponent(c) + '=on'; });
            if (Object.keys(this.activeCountries).length === 0) p += '&__allCountries=1';
            p += '&env=on&country=on&robotDecli=on&app=on';
            return p;
        },
        loadReport(silent) {
            var self = this;
            if (!this.tag) return;
            if (!silent) { this.loading = true; this.error = ''; }

            // First load: we do not know the country list yet — ask the servlet with every
            // country by loading the invariant list first (only once).
            var ensureCountries = Object.keys(this.activeCountries).length > 0
                ? Promise.resolve()
                : new Promise(function (resolve) {
                    $.getJSON('FindInvariantByID', { idName: 'COUNTRY' }, function (data) {
                        var list = Array.isArray(data) ? data : (data.contentTable || []);
                        list.forEach(function (c) { self.activeCountries[c.value] = true; });
                        resolve();
                    }).fail(function () { resolve(); });
                });

            ensureCountries.then(function () {
                $.getJSON('ReadTestCaseExecutionByTag?' + self._reportParams(), function (data) {
                    self.loading = false;
                    if (!data || !data.tagObject) {
                        self.loaded = false;
                        self.error = "Tag '" + self.tag + "' does not exist.";
                        return;
                    }
                    self._applyReport(data);
                }).fail(function () {
                    self.loading = false;
                    self.error = 'Could not load the report.';
                });
            });
        },
        _applyReport(data) {
            var self = this;
            // Snapshot the current cell statuses: on silent reloads (polling) the diff feeds
            // the live activity list, so live events show even without a websocket channel.
            var prevCells = null;
            if (this.loaded && this.rows.length) {
                prevCells = {};
                this.rows.forEach(function (r) {
                    Object.keys(r.execTab || {}).forEach(function (k) {
                        var c = r.execTab[k];
                        if (c) prevCells[r.test + '|' + r.testCase + '|' + k] = c.ControlStatus;
                    });
                });
            }
            this.error = '';
            this.tagObj = data.tagObject;
            this.durationMax = (data.table && data.table.durationMax) || 0;
            // Server sends columns as {environment, country, robotDecli}; execTab is keyed by
            // "env country robotDecli" (see ReadTestCaseExecutionByTag execKey) — build both.
            this.columns = ((data.table && data.table.tableColumns) || []).map(function (c) {
                return {
                    key: c.environment + ' ' + c.country + ' ' + c.robotDecli,
                    environment: c.environment,
                    country: c.country,
                    robotDecli: c.robotDecli
                };
            });
            this.rows = (data.table && data.table.tableContent) || [];
            this.bugs = this._extractBugs(data.table && data.table.bugContent);
            var man = data.manualExecutionList || {};
            this.manualList = (man.perExecutor || []).map(function (e) {
                return {
                    executor: e.executor,
                    total: (e.executionList || []).length,
                    stillToDo: (e.executionWEList || []).length
                };
            });
            this._applyStats(data.statsChart);
            this._applyFolders(data.testFolderChart);
            this._applyLabels(data.labelStat);
            this.draftDesc = this.tagObj.description || '';
            this.draftComment = this.tagObj.comment || '';
            this.loaded = true;

            // Feed the live activity list with what changed since the last load
            // (cells already patched by a ws delta keep the same status: no duplicate entry)
            if (prevCells) {
                this.rows.forEach(function (r) {
                    Object.keys(r.execTab || {}).forEach(function (k) {
                        var c = r.execTab[k];
                        if (!c) return;
                        var old = prevCells[r.test + '|' + r.testCase + '|' + k];
                        if (old !== undefined && old !== c.ControlStatus) {
                            self._pushFeed({
                                kind: 'exe', status: c.ControlStatus,
                                test: r.test, testCase: r.testCase,
                                env: c.Environment, country: c.Country,
                                executionId: (c.ID && c.ID !== '0') ? c.ID : undefined
                            });
                        }
                    });
                });
            }

            // dimensions actually present (for the filter chips)
            var seenCountry = {}, seenEnv = {}, seenRobot = {}, seenApp = {};
            this.rows.forEach(function (r) {
                if (r.application) seenApp[r.application] = true;
                Object.values(r.execTab || {}).forEach(function (c) {
                    if (!c) return;
                    if (c.Country) seenCountry[c.Country] = true;
                    if (c.Environment) seenEnv[c.Environment] = true;
                    if (c.RobotDecli) seenRobot[c.RobotDecli] = true;
                });
            });
            Object.keys(seenCountry).forEach(function (c) { if (self.activeCountries[c] === undefined) self.activeCountries[c] = true; });
            Object.keys(seenEnv).forEach(function (e) { if (self.activeEnvs[e] === undefined) self.activeEnvs[e] = true; });
            Object.keys(seenRobot).forEach(function (r) { if (self.activeRobots[r] === undefined) self.activeRobots[r] = true; });
            Object.keys(seenApp).forEach(function (a) { if (self.activeApps[a] === undefined) self.activeApps[a] = true; });

            try { saveHistory({ id: this.tagObj.id, tag: this.tag }, 'historyCampaigns', 5); } catch (e) { /* non blocking */ }

            document.title = this.tag + ' - Campaign Report';
            this._setupLive();
            this.$nextTick(function () { if (window.lucide) lucide.createIcons(); });
        },
        _extractBugs(bugContent) {
            if (!bugContent) return [];
            var list = bugContent.bugSummary || bugContent.contentTable || bugContent || [];
            if (!Array.isArray(list)) return [];
            return list;
        },
        _applyStats(statsChart) {
            // contentTable = { split: [{environment, country, robotDecli, application, total, OK, KO...}], total: {...} }
            this.statsList = [];
            this.statsTotal = null;
            if (!statsChart || !statsChart.contentTable) return;
            this.statsTotal = statsChart.contentTable.total || null;
            this.statsList = statsChart.contentTable.split || [];
        },
        _applyFolders(chart) {
            // axis = [{ name, OK: {value, color}, KO: {value, color}, ... }]
            this.folderStats = [];
            if (!chart || !chart.axis) return;
            var self = this;
            var list = Array.isArray(chart.axis) ? chart.axis : Object.values(chart.axis);
            list.forEach(function (e) {
                if (!e || !e.name) return;
                var entry = { folder: e.name, total: 0 };
                self.statusOrder.forEach(function (s) {
                    var v = e[s] && e[s].value !== undefined ? e[s].value : 0;
                    entry[s] = v;
                    entry.total += v;
                });
                if (entry.total > 0) self.folderStats.push(entry);
            });
        },
        _applyLabels(labelStat) {
            this.labelStickers = (labelStat && labelStat.labelTreeSTICKER) || [];
            this.labelRequirements = (labelStat && labelStat.labelTreeREQUIREMENT) || [];
        },
        splitKey(e) {
            var parts = [];
            if (e.environment && e.environment !== 'Total') parts.push(e.environment);
            if (e.country) parts.push(e.country);
            if (e.robotDecli) parts.push(e.robotDecli);
            if (e.application) parts.push(e.application);
            return parts.join(' / ') || 'Total';
        },
        splitSegments(e) {
            var self = this;
            var segs = [];
            if (!e || !e.total) return segs;
            this.statusOrder.forEach(function (s) {
                var v = e[s] || 0;
                if (v > 0) segs.push({ status: s, count: v, pct: (v / e.total) * 100, color: self.statusColors[s] });
            });
            return segs;
        },

        // ═══════════════════ COMPUTED ═══════════════════
        get statusCounts() {
            var counts = {};
            this.statusOrder.forEach(function (s) { counts[s] = 0; });
            var total = 0;
            this.rows.forEach(function (r) {
                Object.values(r.execTab || {}).forEach(function (c) {
                    if (c && c.ControlStatus !== undefined && counts[c.ControlStatus] !== undefined) {
                        counts[c.ControlStatus]++;
                        total++;
                    }
                });
            });
            counts._total = total;
            return counts;
        },
        get progressSegments() {
            var c = this.statusCounts;
            var segs = [];
            var self = this;
            if (!c._total) return segs;
            this.statusOrder.forEach(function (s) {
                if (c[s] > 0) segs.push({ status: s, count: c[s], pct: (c[s] / c._total) * 100, color: self.statusColors[s] });
            });
            return segs;
        },
        get pendingCount() {
            var c = this.statusCounts;
            return c.QU + c.QE + c.PA + c.PE + c.WE + c.NE;
        },
        get isRunning() {
            var c = this.statusCounts;
            return (c.QU + c.PA + c.PE + c.WE) > 0;
        },
        get advancementPct() {
            var c = this.statusCounts;
            if (!c._total) return 0;
            var done = c.OK + c.KO + c.FA + c.NA + c.CA + c.QE;
            return Math.round((done / c._total) * 100);
        },
        _rowMatchesSearch(r) {
            if (!this.search) return true;
            var q = this.search.toLowerCase();
            return (r.test || '').toLowerCase().indexOf(q) >= 0
                || (r.testCase || '').toLowerCase().indexOf(q) >= 0
                || (r.shortDesc || '').toLowerCase().indexOf(q) >= 0
                || (r.application || '').toLowerCase().indexOf(q) >= 0;
        },
        cellVisible(cell) {
            if (!cell) return false;
            if (this.activeStatuses[cell.ControlStatus] === false) return false;
            if (this.activeCountries[cell.Country] === false) return false;
            if (this.activeEnvs[cell.Environment] === false) return false;
            if (cell.RobotDecli && this.activeRobots[cell.RobotDecli] === false) return false;
            if (this.onlyFlaky && !cell.isFlaky) return false;
            if (this.onlyManual && (cell.ManualExecution || 'N') !== 'Y') return false;
            return true;
        },
        // rowsMode 'hideOk' keeps flaky/bugged rows visible; 'hideOkFlaky' hides them too
        _rowHiddenByMode(r) {
            if (this.rowsMode === 'all') return false;
            var cells = Object.values(r.execTab || {});
            if (!cells.length) return false;
            var allOk = cells.every(function (c) { return c && (c.ControlStatus === 'OK' || c.ControlStatus === 'QU'); });
            if (!allOk) return false;
            if (this.rowBugs(r).length > 0) return false;
            if (this.rowsMode === 'hideOk') {
                var flaky = cells.some(function (c) { return c && c.isFlaky; });
                return !flaky;
            }
            return true; // hideOkFlaky
        },
        get visibleRows() {
            var self = this;
            var list = this.rows.filter(function (r) {
                if (!self._rowMatchesSearch(r)) return false;
                if (self.activeApps[r.application] === false) return false;
                if (self.onlyBugged && self.rowBugs(r).length === 0) return false;
                if (self._rowHiddenByMode(r)) return false;
                var cells = Object.values(r.execTab || {});
                return cells.some(function (c) { return self.cellVisible(c); });
            });
            var dir = this.sortDir;
            var key = this.sortBy;
            list.sort(function (a, b) {
                var va, vb;
                if (key === 'app') { va = (a.application || '').toLowerCase(); vb = (b.application || '').toLowerCase(); }
                else if (key === 'priority') { va = a.priority || 99; vb = b.priority || 99; }
                else if (key === 'lastRun') { va = a.lastExeEnd || 0; vb = b.lastExeEnd || 0; }
                else { va = ((a.test || '') + ' ' + (a.testCase || '')).toLowerCase(); vb = ((b.test || '') + ' ' + (b.testCase || '')).toLowerCase(); }
                if (va < vb) return -dir;
                if (va > vb) return dir;
                return 0;
            });
            return list;
        },
        setSort(key) {
            if (this.sortBy === key) this.sortDir = -this.sortDir;
            else { this.sortBy = key; this.sortDir = 1; }
        },
        get visibleColumns() {
            // hide a combination column when every one of its cells is filtered out
            var self = this;
            return this.columns.filter(function (col) {
                return self.visibleRows.some(function (r) { return self.cellVisible((r.execTab || {})[col.key]); });
            });
        },
        get selectedCount() { return Object.keys(this.selected).length; },
        // Donut segments over every execution cell (updates live as ws deltas patch the cells).
        // r=80 -> circumference 502.655; each segment is a stroked arc offset by what precedes it.
        get donut() {
            var C = 502.655;
            var c = this.statusCounts;
            var self = this;
            var segs = [];
            var acc = 0;
            if (!c._total) return segs;
            this.statusOrder.forEach(function (s) {
                if (!c[s]) return;
                var frac = c[s] / c._total;
                segs.push({
                    status: s, count: c[s], pct: Math.round(frac * 1000) / 10,
                    color: self.statusColors[s],
                    dash: frac * C, offset: -acc
                });
                acc += frac * C;
            });
            return segs;
        },
        get okPct() {
            var c = this.statusCounts;
            return c._total ? Math.round((c.OK / c._total) * 100) : 0;
        },
        // Arc of one status inside the donut ring (r=80 -> C=502.655). Used by the static
        // SVG circles: <template x-for> cannot live inside an <svg> so each status has its own node.
        donutArc(s) {
            var C = 502.655;
            var c = this.statusCounts;
            if (!c._total || !c[s]) return { dash: 0, offset: 0 };
            var acc = 0;
            for (var i = 0; i < this.statusOrder.length; i++) {
                var st = this.statusOrder[i];
                if (st === s) break;
                acc += (c[st] / c._total) * C;
            }
            return { dash: (c[s] / c._total) * C, offset: -acc };
        },
        // Official counters frozen on the tag when the campaign ended. The donut and the grid
        // live-update when executions are submitted again (OK can become KO), but this
        // snapshot keeps the original campaign verdict visible - same logic as the legacy page.
        get officialCounts() {
            var t = this.tagObj || {};
            var counts = {};
            var total = 0;
            this.statusOrder.forEach(function (s) {
                counts[s] = t['nb' + s] || 0;
                total += counts[s];
            });
            counts._total = total;
            counts.okPct = total ? Math.round((counts.OK / total) * 100) : 0;
            return counts;
        },
        get liveDiffersFromOfficial() {
            var o = this.officialCounts;
            if (!o._total) return false; // campaign still running: no frozen counters yet
            var c = this.statusCounts;
            if (o._total !== c._total) return true;
            var self = this;
            return this.statusOrder.some(function (s) { return (o[s] || 0) !== (c[s] || 0); });
        },
        get officialSummary() {
            var o = this.officialCounts;
            var self = this;
            var parts = [];
            this.statusOrder.forEach(function (s) { if (o[s] > 0) parts.push(o[s] + ' ' + s); });
            return parts.join(' - ');
        },

        // KPI tiles next to the donut: done/not ok/pending counters + rates
        get statusKpis() {
            var c = this.statusCounts;
            var notOk = c.KO + c.FA + c.NA + c.CA;
            var pending = c.QU + c.QE + c.PA + c.PE + c.WE + c.NE;
            return {
                total: c._total,
                ok: c.OK, okPct: c._total ? Math.round((c.OK / c._total) * 100) : 0,
                notOk: notOk, notOkPct: c._total ? Math.round((notOk / c._total) * 100) : 0,
                pending: pending, pendingPct: c._total ? Math.round((pending / c._total) * 100) : 0
            };
        },

        // ═══════════════════ BREAKDOWN (one table per dimension) ═══════════════════
        _aggBreakdown(dimFn) {
            var self = this;
            var map = {};
            this.rows.forEach(function (r) {
                Object.values(r.execTab || {}).forEach(function (c) {
                    if (!c) return;
                    var key = dimFn(r, c) || '-';
                    if (!map[key]) {
                        map[key] = { label: key, total: 0 };
                        self.statusOrder.forEach(function (s) { map[key][s] = 0; });
                    }
                    if (map[key][c.ControlStatus] !== undefined) map[key][c.ControlStatus]++;
                    map[key].total++;
                });
            });
            return Object.values(map).sort(function (a, b) { return a.label < b.label ? -1 : (a.label > b.label ? 1 : 0); });
        },
        get breakdownRows() { return this._breakdownRowsForTab(this.breakdownTab); },
        _breakdownRowsForTab(t) {
            var self = this;
            if (t === 'environment') return this._aggBreakdown(function (r, c) { return c.Environment; });
            if (t === 'country') return this._aggBreakdown(function (r, c) { return c.Country; });
            if (t === 'robot') return this._aggBreakdown(function (r, c) { return c.RobotDecli; });
            if (t === 'application') return this._aggBreakdown(function (r, c) { return r.application; });
            if (t === 'combination') {
                return this.statsList.map(function (e) {
                    var o = { label: self.splitKey(e), total: e.total || 0 };
                    self.statusOrder.forEach(function (s) { o[s] = e[s] || 0; });
                    return o;
                });
            }
            if (t === 'folder') {
                return this.folderStats.map(function (f) {
                    var o = { label: f.folder, total: f.total || 0 };
                    self.statusOrder.forEach(function (s) { o[s] = f[s] || 0; });
                    return o;
                });
            }
            return [];
        },
        // only show status columns that carry at least one execution in the current tab
        get breakdownStatusCols() {
            var rows = this.breakdownRows;
            return this.statusOrder.filter(function (s) {
                return rows.some(function (r) { return (r[s] || 0) > 0; });
            });
        },
        breakdownDimLabel(t) {
            return { environment: 'Environment', country: 'Country', robot: 'Robot', application: 'Application', combination: 'Combination', folder: 'Test folder' }[t] || t;
        },

        // Scope of the campaign as recorded on the tag (environments/countries/robots/applications)
        get tagScope() {
            var t = this.tagObj || {};
            return {
                environments: this._parseList(t.environmentList),
                countries: this._parseList(t.countryList),
                robots: this._parseList(t.robotDecliList),
                applications: this._parseList(t.applicationList),
                systems: this._parseList(t.systemList)
            };
        },
        _parseList(raw) {
            if (!raw) return [];
            if (Array.isArray(raw)) return raw;
            try { var v = JSON.parse(raw); return Array.isArray(v) ? v : []; } catch (e) { return []; }
        },
        get ciScorePct() {
            var t = this.tagObj || {};
            var max = t.ciScoreThreshold || t.ciScoreMax || 100;
            if (!max) return 0;
            return Math.min(100, Math.round(((t.ciScore || 0) / max) * 100));
        },
        get bugSummary() {
            var opened = 0, toClean = 0, toCreate = 0;
            this.bugs.forEach(function (b) {
                var s = (b.status || '').toUpperCase();
                if (s.indexOf('CLEAN') >= 0) toClean++;
                else if (s.indexOf('CREATE') >= 0 || b.bug === '') toCreate++;
                else opened++;
            });
            return { opened: opened, toClean: toClean, toCreate: toCreate, total: this.bugs.length };
        },

        // ═══════════════════ FILTER ACTIONS ═══════════════════
        toggleStatus(s) { this.activeStatuses[s] = !this.activeStatuses[s]; },
        onlyStatus(s) {
            var self = this;
            var isOnly = this.activeStatuses[s] && this.statusOrder.every(function (o) { return o === s ? true : !self.activeStatuses[o]; });
            this.statusOrder.forEach(function (o) { self.activeStatuses[o] = isOnly ? true : (o === s); });
        },
        allStatuses(v) { var self = this; this.statusOrder.forEach(function (s) { self.activeStatuses[s] = v; }); },
        // isolate every failed-ish status at once (Not OK tile); click again to reset
        onlyNotOk() {
            var self = this;
            var notOk = ['KO', 'FA', 'NA', 'CA'];
            var isIsolated = this.statusOrder.every(function (s) {
                return notOk.indexOf(s) >= 0 ? self.activeStatuses[s] : !self.activeStatuses[s];
            });
            this.statusOrder.forEach(function (s) {
                self.activeStatuses[s] = isIsolated ? true : notOk.indexOf(s) >= 0;
            });
        },
        toggleCountry(c) { this.activeCountries[c] = !this.activeCountries[c]; },
        allCountries(v) { var self = this; Object.keys(this.activeCountries).forEach(function (c) { self.activeCountries[c] = v; }); },
        toggleIn(map, k) { map[k] = !map[k]; },
        allIn(map, v) { Object.keys(map).forEach(function (k) { map[k] = v; }); },
        get activeFilterCount() {
            var self = this;
            var offIn = function (map) { return Object.keys(map).some(function (k) { return map[k] === false; }); };
            var n = 0;
            if (this.search !== '') n++;
            if (this.rowsMode !== 'all') n++;
            if (this.statusOrder.some(function (s) { return self.activeStatuses[s] === false; })) n++;
            if (offIn(this.activeCountries)) n++;
            if (offIn(this.activeEnvs)) n++;
            if (offIn(this.activeRobots)) n++;
            if (offIn(this.activeApps)) n++;
            if (this.onlyFlaky) n++;
            if (this.onlyBugged) n++;
            if (this.onlyManual) n++;
            return n;
        },
        get hasActiveFilter() { return this.activeFilterCount > 0; },
        resetFilters() {
            this.search = '';
            this.rowsMode = 'all';
            this.onlyFlaky = false;
            this.onlyBugged = false;
            this.onlyManual = false;
            this.allStatuses(true);
            this.allIn(this.activeCountries, true);
            this.allIn(this.activeEnvs, true);
            this.allIn(this.activeRobots, true);
            this.allIn(this.activeApps, true);
        },

        // ═══════════════════ GRID HELPERS ═══════════════════
        cellFor(row, col) { return (row.execTab || {})[col.key] || null; },
        cellTitle(cell) {
            if (!cell) return '';
            var t = cell.ControlStatus + ' — ' + (cell.ControlMessage || '');
            t += '\nEnv: ' + cell.Environment + '  Country: ' + cell.Country + '  Robot: ' + (cell.RobotDecli || '-');
            if (cell.Executor) t += '\nExecutor: ' + cell.Executor;
            if (cell.Start) t += '\nStart: ' + new Date(cell.Start).toLocaleString();
            if (cell.DurationMs) t += '\nDuration: ' + this.fmtDuration(cell.DurationMs);
            if (cell.NbExecutions && String(cell.NbExecutions) !== '1') t += '\nRetries: ' + cell.NbExecutions;
            if (cell.QueueState) t += '\nQueue: ' + cell.QueueState;
            if (cell.isFalseNegative) t += '\nFALSE NEGATIVE';
            if (cell.isFlaky) t += '\nFLAKY';
            return t;
        },
        openCell(cell) {
            if (!cell) return;
            var st = cell.ControlStatus;
            if ((st === 'QU' || st === 'QE' || st === 'PA') && cell.QueueID && cell.QueueID !== '0') {
                if (typeof openModalTestCaseExecutionQueue === 'function') {
                    openModalTestCaseExecutionQueue(cell.QueueID, 'EDIT');
                }
                return;
            }
            if (cell.ID && cell.ID !== '0') window.open('./TestCaseExecutionV2.jsp?executionId=' + cell.ID, '_blank');
        },
        openPrevious(cell) {
            if (cell && cell.previousExeId) window.open('./TestCaseExecutionV2.jsp?executionId=' + cell.previousExeId, '_blank');
        },
        cellSelectable(cell) { return !!(cell && cell.QueueID && cell.QueueID !== '0'); },
        isSelected(cell) { return this.cellSelectable(cell) && !!this.selected[cell.QueueID]; },
        toggleSelect(cell) {
            if (!this.cellSelectable(cell)) return;
            if (this.selected[cell.QueueID]) delete this.selected[cell.QueueID];
            else this.selected[cell.QueueID] = true;
        },

        // ═══════════════════ MASS ACTIONS ═══════════════════
        selectByStatus(status, manual) {
            var self = this;
            this.rows.forEach(function (r) {
                Object.values(r.execTab || {}).forEach(function (c) {
                    if (!self.cellSelectable(c)) return;
                    var stateOk = (status === 'QEERROR')
                        ? (c.ControlStatus === 'QE' && (c.QueueState || '').indexOf('ERROR') >= 0)
                        : c.ControlStatus === status;
                    var manualOk = manual === undefined || (c.ManualExecution || 'N') === manual;
                    if (stateOk && manualOk) self.selected[c.QueueID] = true;
                });
            });
        },
        clearSelection() { this.selected = {}; },
        submitAgain(withDep) {
            var self = this;
            var ids = Object.keys(this.selected);
            if (!ids.length) return;
            var body = ids.map(function (id) { return 'id=' + encodeURIComponent(id); }).join('&');
            body += '&actionState=' + (withDep ? 'toQUEUEDwithDep' : 'toQUEUED');
            body += '&tag=' + encodeURIComponent(this.tag) + '&actionSave=save';
            this.loading = true;
            $.post('CreateTestCaseExecutionQueue', body, null, 'json')
                .done(function (data) {
                    var type = getAlertType(data.messageType);
                    showMessageMainPage(type === 'success' ? 'success' : type, data.message, false, 8000);
                    self.clearSelection();
                    self.loadReport(true);
                })
                .fail(function () { showMessageMainPage('danger', 'Submit again failed.'); })
                .always(function () { self.loading = false; });
        },

        // ═══════════════════ TAG ACTIONS ═══════════════════
        _tagApi(action, okMsg) {
            var self = this;
            $.ajax({ url: 'api/campaignexecutions/' + encodeURIComponent(this.tag) + '/' + action, type: 'POST' })
                .done(function () { showMessageMainPage('success', okMsg); self.loadReport(true); })
                .fail(function (xhr) { showMessageMainPage('danger', 'Action failed: ' + (xhr.responseText || xhr.status)); });
        },
        pauseTag() { this._tagApi('pause', 'Pause requested on every queued execution.'); },
        resumeTag() { this._tagApi('resume', 'Resume requested on every paused execution.'); },
        cancelTag() { this._tagApi('cancel', 'Cancellation requested on every non started execution.'); },
        toggleFalseNegative() {
            var action = this.tagObj && this.tagObj.falseNegative ? 'undeclareFalseNegative' : 'declareFalseNegative';
            this._tagApi(action, action === 'declareFalseNegative' ? 'Campaign declared as false negative.' : 'False negative flag removed.');
        },
        saveDesc() {
            var self = this;
            $.post('UpdateTag', { tag: this.tag, description: this.draftDesc })
                .done(function () { self.tagObj.description = self.draftDesc; self.editDesc = false; showMessageMainPage('success', 'Description saved.'); })
                .fail(function () { showMessageMainPage('danger', 'Could not save the description.'); });
        },
        saveComment() {
            var self = this;
            $.post('UpdateTag', { tag: this.tag, comment: this.draftComment })
                .done(function () { self.tagObj.comment = self.draftComment; self.editComment = false; showMessageMainPage('success', 'Comment saved.'); })
                .fail(function () { showMessageMainPage('danger', 'Could not save the comment.'); });
        },
        get pdfHref() {
            return (this.tagObj && this.tagObj.ciResult) ? ('./api/public/campaignexecutions/pdf/' + encodeURIComponent(this.tag)) : null;
        },
        editCampaign() {
            if (this.tagObj && this.tagObj.campaign && typeof campaign_editEntryClick === 'function') {
                campaign_editEntryClick(this.tagObj.campaign);
            }
        },

        // ═══════════════════ PDF / PRINT REPORTS ═══════════════════
        generatePdf() {
            var self = this;
            if (this.pdfTemplate === 'legacy') {
                if (this.pdfHref) window.open(this.pdfHref, '_blank');
            } else if (this.pdfTemplate === 'trend') {
                this._loadTrendTags().then(function (tags) {
                    if (tags.length < 2) {
                        showMessageMainPage('warning', 'Not enough execution history on this campaign to build a trend report.');
                        return;
                    }
                    self._openPrintDoc(self._printTrendHtml(tags));
                });
            } else {
                this._openPrintDoc(this._printHtml(this.pdfTemplate));
            }
            this.pdfOpen = false;
        },
        _openPrintDoc(html) {
            var w = window.open('', '_blank');
            if (!w) { showMessageMainPage('warning', 'Popup blocked by the browser: allow popups to generate the report.'); return; }
            w.document.open();
            w.document.write(html);
            w.document.close();
        },
        // Last executions of the same campaign (official tag counters), oldest first
        _loadTrendTags() {
            var self = this;
            return new Promise(function (resolve) {
                var campaign = self.tagObj && self.tagObj.campaign;
                if (!campaign) { resolve([]); return; }
                var url = 'ReadTag?iDisplayStart=0&iDisplayLength=12&iSortCol_0=0&sSortDir_0=desc'
                    + '&sColumns=id,tag,campaign,description&sSearch_2=' + encodeURIComponent(campaign);
                $.getJSON(url, function (data) {
                    var list = (data.contentTable || []).filter(function (t) {
                        // keep runs of this campaign that actually executed something
                        return t.campaign === campaign && (self._tagCounters(t).total > 0 || t.tag === self.tag);
                    });
                    list.sort(function (a, b) { return (a.id || 0) - (b.id || 0); });
                    resolve(list);
                }).fail(function () { resolve([]); });
            });
        },
        // Prefill the recipients with the ones configured on the campaign CAMPAIGN_END email
        // event hooks (the exact recipients Cerberus notifies when the campaign finishes).
        _prefillEmailFromHooks() {
            var self = this;
            var campaign = this.tagObj && this.tagObj.campaign;
            if (!campaign || this.emailRecipient) return;
            // the servlet sorts on a bogus default column without explicit sColumns/iSortCol_0
            $.getJSON('ReadEventHook?iDisplayStart=0&iDisplayLength=300&iSortCol_0=0&sSortDir_0=asc&sColumns=ID,EventReference,ObjectKey1,HookConnector,HookRecipient', function (data) {
                var recipients = [];
                (data.contentTable || []).forEach(function (h) {
                    if ((h.hookConnector || '') !== 'EMAIL') return;
                    if (String(h.eventReference || '').indexOf('CAMPAIGN_END') !== 0) return;
                    if (h.isActive === false) return;
                    var key = h.objectKey1 || '';
                    if (key && key !== campaign) return; // hook scoped to another campaign
                    String(h.hookRecipient || '').split(/[;,]/).forEach(function (r) {
                        r = r.trim();
                        if (r && recipients.indexOf(r) === -1) recipients.push(r);
                    });
                });
                if (recipients.length && !self.emailRecipient) self.emailRecipient = recipients.join('; ');
            });
        },
        sendReportEmail() {
            var self = this;
            var to = (this.emailRecipient || '').trim();
            if (!to) { showMessageMainPage('warning', 'Please type at least one recipient email address.'); return; }
            this.emailSending = true;
            $.ajax({
                url: 'api/campaignexecutions/' + encodeURIComponent(this.tag) + '/notify?recipient=' + encodeURIComponent(to),
                type: 'POST'
            }).done(function (data) {
                // the private API wraps errors in a HTTP 200 body: {status: 'BAD_REQUEST'|..., message}
                if (data && data.status && data.status !== 'OK') {
                    showMessageMainPage('danger', data.message || 'Could not send the email.');
                    return;
                }
                showMessageMainPage('success', (data && data.message) || ('Campaign report sent to ' + to + '.'));
                self.pdfOpen = false;
            }).fail(function (xhr) {
                var msg = 'Could not send the email.';
                try { msg = JSON.parse(xhr.responseText).message || msg; } catch (e) { /* keep default */ }
                showMessageMainPage('danger', msg);
            }).always(function () { self.emailSending = false; });
        },
        // Run again: open the transversal execution modal in campaign mode, feeding it with
        // exactly what this run executed (test cases, env/country combos, robots preselected).
        openRunAgain() {
            if (!this.tagObj || !this.tagObj.campaign) return;
            var seenTc = {};
            var testcases = [];
            this.rows.forEach(function (r) {
                var k = r.test + '|' + r.testCase;
                if (!seenTc[k]) {
                    seenTc[k] = true;
                    testcases.push({ test: r.test, testCase: r.testCase });
                }
            });
            var seenEnv = {};
            var envCountries = [];
            var seenRobot = {};
            var robots = [];
            this.columns.forEach(function (c) {
                var k = c.environment + '|' + c.country;
                if (!seenEnv[k]) {
                    seenEnv[k] = true;
                    envCountries.push({ environment: c.environment, country: c.country });
                }
                if (c.robotDecli && !seenRobot[c.robotDecli]) {
                    seenRobot[c.robotDecli] = true;
                    robots.push(c.robotDecli);
                }
            });
            window.dispatchEvent(new CustomEvent('open-execution', {
                detail: {
                    campaign: this.tagObj.campaign,
                    testcases: testcases,
                    envCountries: envCountries,
                    robots: robots
                }
            }));
        },
        _esc(v) {
            return String(v === undefined || v === null ? '' : v)
                .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
        },
        _printDonutSvg(size) {
            // same arcs as the live donut, rendered as a plain SVG string
            var C = 502.655;
            var c = this.statusCounts;
            var self = this;
            var circles = '';
            var acc = 0;
            if (c._total) {
                this.statusOrder.forEach(function (s) {
                    if (!c[s]) return;
                    var dash = (c[s] / c._total) * C;
                    circles += '<circle cx="100" cy="100" r="80" fill="none" stroke="' + self.statusColors[s]
                        + '" stroke-width="26" stroke-dasharray="' + dash.toFixed(2) + ' ' + C.toFixed(2)
                        + '" stroke-dashoffset="' + (-acc).toFixed(2) + '"></circle>';
                    acc += dash;
                });
            }
            return '<svg width="' + size + '" height="' + size + '" viewBox="0 0 200 200">'
                + '<circle cx="100" cy="100" r="80" fill="none" stroke="#eef2f7" stroke-width="26"></circle>'
                + '<g transform="rotate(-90 100 100)">' + circles + '</g>'
                + '<text x="100" y="97" text-anchor="middle" font-size="34" font-weight="800" fill="#0f172a">' + this.okPct + '%</text>'
                + '<text x="100" y="118" text-anchor="middle" font-size="10" font-weight="700" fill="#64748b" letter-spacing="1">OK RATE</text>'
                + '</svg>';
        },
        _printLegend() {
            var self = this;
            var c = this.statusCounts;
            var html = '<table class="legend">';
            this.statusOrder.forEach(function (s) {
                if (!c[s]) return;
                html += '<tr>'
                    + '<td><span class="dot" style="background:' + self.statusColors[s] + '"></span> <b>' + s + '</b> <span class="mut">' + self._esc(self.statusLabels[s]) + '</span></td>'
                    + '<td class="num"><b>' + c[s] + '</b></td>'
                    + '<td class="num mut">' + (Math.round((c[s] / c._total) * 1000) / 10) + '%</td>'
                    + '</tr>';
            });
            return html + '</table>';
        },
        _printBreakdownTable(title, rows) {
            if (!rows.length) return '';
            var self = this;
            var cols = this.statusOrder.filter(function (s) { return rows.some(function (r) { return (r[s] || 0) > 0; }); });
            var html = '<h2>' + this._esc(title) + '</h2><table class="data"><thead><tr><th>' + this._esc(title) + '</th>';
            cols.forEach(function (s) { html += '<th class="num" style="color:' + self.statusColors[s] + '">' + s + '</th>'; });
            html += '<th class="num">Total</th><th class="num">% OK</th></tr></thead><tbody>';
            rows.forEach(function (r) {
                html += '<tr><td>' + self._esc(r.label) + '</td>';
                cols.forEach(function (s) { html += '<td class="num">' + (r[s] || '-') + '</td>'; });
                html += '<td class="num"><b>' + r.total + '</b></td><td class="num">' + self.pct(r.OK || 0, r.total) + '%</td></tr>';
            });
            return html + '</tbody></table>';
        },
        _printExecutionsTable() {
            var self = this;
            var html = '<h2>Executions</h2><table class="data"><thead><tr><th>Test case</th><th>Application</th>';
            this.columns.forEach(function (col) {
                html += '<th class="num">' + self._esc(col.environment + ' ' + col.country) + '<br><span class="mut">' + self._esc(col.robotDecli || '') + '</span></th>';
            });
            html += '</tr></thead><tbody>';
            this.rows.forEach(function (r) {
                html += '<tr><td><span class="mut">' + self._esc(r.test) + '</span><br><b>' + self._esc(r.testCase) + '</b>'
                    + (self.rowBugs(r).length ? ' <span class="chip chip-red">' + self.rowBugs(r).length + ' bug</span>' : '')
                    + '<br><span class="mut small">' + self._esc(r.shortDesc) + '</span></td>'
                    + '<td>' + self._esc(r.application) + '</td>';
                self.columns.forEach(function (col) {
                    var cell = (r.execTab || {})[col.key];
                    if (!cell) { html += '<td class="num mut">-</td>'; return; }
                    html += '<td class="num"><span class="chip" style="background:' + self.statusColor(cell.ControlStatus) + '1f; color:' + self.statusColor(cell.ControlStatus) + '">'
                        + cell.ControlStatus + '</span>'
                        + (cell.isFlaky ? '<br><span class="mut small">flaky</span>' : '')
                        + (cell.NbExecutions && String(cell.NbExecutions) !== '1' ? '<br><span class="mut small">x' + cell.NbExecutions + '</span>' : '')
                        + '</td>';
                });
                html += '</tr>';
            });
            return html + '</tbody></table>';
        },
        _printBugsTable() {
            if (!this.bugs.length) return '';
            var self = this;
            var html = '<h2>Bugs</h2><table class="data"><thead><tr><th>Bug</th><th>Test case</th><th>Status</th></tr></thead><tbody>';
            this.bugs.forEach(function (b) {
                html += '<tr><td><b>' + self._esc(b.bug || '-') + '</b></td><td>' + self._esc(b.testCase) + '</td><td>' + self._esc(b.status || 'OPEN') + '</td></tr>';
            });
            return html + '</tbody></table>';
        },
        _printHtml(mode) {
            var t = this.tagObj || {};
            var k = this.statusKpis;
            var scope = this.tagScope;
            var ci = t.ciResult || '';
            var ciColor = ci === 'OK' ? '#00d27a' : (ci === 'KO' ? '#e63757' : '#94a3b8');
            var detailed = mode === 'detailed';

            var kv = function (label, value) {
                return '<tr><td class="k">' + label + '</td><td class="v">' + value + '</td></tr>';
            };

            var body = ''
                + '<div class="band">'
                +   '<div>'
                +     '<div class="brand">CERBERUS</div>'
                +     '<div class="rpt">Campaign execution report' + (detailed ? ' - detailed' : '') + '</div>'
                +   '</div>'
                +   '<div class="band-right">'
                +     '<div class="tag">' + this._esc(this.tag) + '</div>'
                +     '<div class="gen">Generated on ' + this._esc(new Date().toLocaleString()) + (t.UsrCreated ? ' - launched by ' + this._esc(t.UsrCreated) : '') + '</div>'
                +   '</div>'
                + '</div>'

                + '<div class="verdictrow">'
                +   '<div class="verdict" style="border-color:' + ciColor + '33">'
                +     '<div class="verdict-label">CI RESULT</div>'
                +     '<div class="verdict-value" style="color:' + ciColor + '">' + (ci || 'N/A') + '</div>'
                +     '<div class="verdict-score">' + (t.ciScore || 0) + ' / ' + (t.ciScoreThreshold || 100) + ' penalty points</div>'
                +   '</div>'
                +   '<div class="tiles">'
                +     '<div class="tile"><div class="tile-v">' + k.total + '</div><div class="tile-l">Total</div></div>'
                +     '<div class="tile tile-ok"><div class="tile-v">' + k.ok + '</div><div class="tile-l">OK - ' + k.okPct + '%</div></div>'
                +     '<div class="tile tile-ko"><div class="tile-v">' + k.notOk + '</div><div class="tile-l">Not OK - ' + k.notOkPct + '%</div></div>'
                +     (k.pending ? '<div class="tile tile-pe"><div class="tile-v">' + k.pending + '</div><div class="tile-l">Pending</div></div>' : '')
                +     ((t.nbFlaky || 0) > 0 ? '<div class="tile tile-fl"><div class="tile-v">' + t.nbFlaky + '</div><div class="tile-l">Flaky</div></div>' : '')
                +   '</div>'
                + '</div>'

                + '<div class="cols">'
                +   '<div class="donutbox">' + this._printDonutSvg(210) + this._printLegend() + '</div>'
                +   '<div class="infobox">'
                +     '<h2>Tag information</h2>'
                +     '<table class="kv">'
                +       (t.campaign ? kv('Campaign', this._esc(t.campaign)) : '')
                +       kv('Submitted', this._esc(this.fmtDate(t.DateCreated)))
                +       kv('Started', this._esc(this.fmtDate(t.DateStartExe)))
                +       kv('Ended', this._esc(this.fmtDate(t.DateEndQueue)))
                +       kv('Duration', this._esc(this.tagDurationMin))
                +       kv('Executions', k.total + (t.nbExe !== t.nbExeUsefull ? ' (' + (t.nbExeUsefull || 0) + ' useful)' : ''))
                +       (scope.environments.length ? kv('Environments', this._esc(scope.environments.join(', '))) : '')
                +       (scope.countries.length ? kv('Countries', this._esc(scope.countries.join(', '))) : '')
                +       (scope.robots.length ? kv('Robots', this._esc(scope.robots.join(', '))) : '')
                +       (scope.applications.length ? kv('Applications', this._esc(scope.applications.join(', '))) : '')
                +       (t.description ? kv('Description', this._esc(String(t.description).replace(/<[^>]*>/g, ' '))) : '')
                +       (t.comment ? kv('Comment', this._esc(t.comment)) : '')
                +     '</table>'
                +   '</div>'
                + '</div>';

            body += this._printBreakdownTable('Environment', this._aggBreakdown(function (r, c) { return c.Environment; }));
            body += this._printBreakdownTable('Country', this._aggBreakdown(function (r, c) { return c.Country; }));

            if (detailed) {
                body += this._printBreakdownTable('Application', this._aggBreakdown(function (r, c) { return r.application; }));
                body += this._printBreakdownTable('Test folder', this._breakdownRowsForTab('folder'));
                body += this._printExecutionsTable();
                body += this._printBugsTable();
            }

            body += '<div class="foot">Cerberus Testing - campaign report for tag ' + this._esc(this.tag) + '</div>';

            return '<!DOCTYPE html><html><head><meta charset="utf-8">'
                + '<title>' + this._esc(this.tag) + ' - Cerberus report</title>'
                + '<style>' + this._printCss() + '</style>'
                + '</head><body>'
                + '<button class="printbtn" onclick="window.print()">Print or save as PDF</button>'
                + body
                + '<script>setTimeout(function () { window.print(); }, 500);<\/script>'
                + '</body></html>';
        },
        // ── Trend report: official tag counters of the last runs of the same campaign ──
        _tagCounters(t) {
            var total = 0;
            var self = this;
            var counts = {};
            this.statusOrder.forEach(function (s) {
                var v = t['nb' + s] || 0;
                counts[s] = v;
                total += v;
            });
            return { counts: counts, total: total, okPct: total ? Math.round((t.nbOK / total) * 100) : 0 };
        },
        _fmtShortDate(v) {
            var d = new Date(v);
            if (isNaN(d.getTime())) return '-';
            var p = function (n) { return (n < 10 ? '0' : '') + n; };
            return p(d.getDate()) + '/' + p(d.getMonth() + 1) + ' ' + p(d.getHours()) + ':' + p(d.getMinutes());
        },
        _tagDurationOf(t) {
            var d = (new Date(t.DateEndQueue)) - (new Date(t.DateStartExe));
            return (isNaN(d) || d < 0) ? null : d;
        },
        _printTrendChart(tags) {
            var self = this;
            var W = 700, H = 190, chartH = 150, gap = 10;
            var barW = Math.min(64, Math.floor((W - gap * (tags.length + 1)) / tags.length));
            var svg = '<svg width="100%" viewBox="0 0 ' + W + ' ' + H + '" style="max-width:' + W + 'px">';
            var okPoints = [];
            tags.forEach(function (t, i) {
                var c = self._tagCounters(t);
                var x = gap + i * (barW + gap);
                var y = chartH;
                var isCurrent = t.tag === self.tag;
                self.statusOrder.forEach(function (s) {
                    if (!c.counts[s] || !c.total) return;
                    var h = (c.counts[s] / c.total) * (chartH - 18);
                    y -= h;
                    svg += '<rect x="' + x + '" y="' + y.toFixed(1) + '" width="' + barW + '" height="' + h.toFixed(1)
                        + '" fill="' + self.statusColors[s] + '" rx="1"><title>' + self._esc(t.tag) + ' - ' + s + ': ' + c.counts[s] + '</title></rect>';
                });
                svg += '<text x="' + (x + barW / 2) + '" y="' + (y - 5) + '" text-anchor="middle" font-size="9.5" font-weight="700" fill="#334155">' + c.okPct + '%</text>';
                svg += '<text x="' + (x + barW / 2) + '" y="' + (chartH + 12) + '" text-anchor="middle" font-size="8" fill="' + (isCurrent ? '#1d4ed8' : '#94a3b8') + '" font-weight="' + (isCurrent ? '800' : '500') + '">' + self._esc(self._fmtShortDate(t.DateCreated)) + '</text>';
                if (isCurrent) {
                    svg += '<rect x="' + (x - 3) + '" y="8" width="' + (barW + 6) + '" height="' + (chartH + 8) + '" fill="none" stroke="#1d4ed8" stroke-width="1.2" stroke-dasharray="4 3" rx="6"></rect>';
                }
                okPoints.push({ x: x + barW / 2, y: 18 + (1 - c.okPct / 100) * (chartH - 36) });
            });
            // ok-rate line on top of the bars
            if (okPoints.length > 1) {
                var pts = okPoints.map(function (p) { return p.x.toFixed(1) + ',' + p.y.toFixed(1); }).join(' ');
                svg += '<polyline points="' + pts + '" fill="none" stroke="#0f172a" stroke-width="1.6" stroke-opacity="0.55" stroke-dasharray="1 3"></polyline>';
                okPoints.forEach(function (p) {
                    svg += '<circle cx="' + p.x.toFixed(1) + '" cy="' + p.y.toFixed(1) + '" r="2.4" fill="#0f172a" fill-opacity="0.65"></circle>';
                });
            }
            return svg + '</svg>';
        },
        _printTrendHtml(tags) {
            var self = this;
            var current = tags.find(function (t) { return t.tag === self.tag; }) || tags[tags.length - 1];
            var idx = tags.indexOf(current);
            var previous = idx > 0 ? tags[idx - 1] : null;
            var cur = this._tagCounters(current);
            var campaign = (this.tagObj && this.tagObj.campaign) || '';

            var deltaHtml = '';
            if (previous) {
                var prev = this._tagCounters(previous);
                var dOk = cur.okPct - prev.okPct;
                var dKo = (current.nbKO + current.nbFA) - (previous.nbKO + previous.nbFA);
                var dExe = cur.total - prev.total;
                var durCur = this._tagDurationOf(current), durPrev = this._tagDurationOf(previous);
                var sign = function (n, unit, invert) {
                    var good = invert ? n <= 0 : n >= 0;
                    var color = n === 0 ? '#64748b' : (good ? '#059669' : '#e63757');
                    return '<span style="color:' + color + '; font-weight:800">' + (n > 0 ? '+' : '') + n + (unit || '') + '</span>';
                };
                deltaHtml = '<div class="deltas"><b>vs previous run (' + this._esc(previous.tag) + '):</b> '
                    + 'OK rate ' + sign(dOk, ' pts') + ' &nbsp; KO+FA ' + sign(dKo, '', true) + ' &nbsp; executions ' + sign(dExe)
                    + (durCur !== null && durPrev !== null ? ' &nbsp; duration ' + sign(Math.round((durCur - durPrev) / 1000), 's', true) : '')
                    + '</div>';
            }

            var legend = '';
            this.statusOrder.forEach(function (s) {
                if (tags.every(function (t) { return !(t['nb' + s] > 0); })) return;
                legend += '<span class="lgd"><span class="dot" style="background:' + self.statusColors[s] + '"></span>' + s + '</span>';
            });

            var rowsHtml = '';
            tags.slice().reverse().forEach(function (t) {
                var c = self._tagCounters(t);
                var isCurrent = t.tag === self.tag;
                var dur = self._tagDurationOf(t);
                rowsHtml += '<tr' + (isCurrent ? ' class="cur"' : '') + '>'
                    + '<td>' + (isCurrent ? '<b>' + self._esc(t.tag) + '</b> <span class="chip chip-blue">THIS RUN</span>' : self._esc(t.tag)) + '</td>'
                    + '<td>' + self._esc(self._fmtShortDate(t.DateCreated)) + '</td>'
                    + '<td class="num">' + c.total + '</td>'
                    + '<td class="num" style="color:#059669"><b>' + (t.nbOK || 0) + '</b></td>'
                    + '<td class="num" style="color:#e63757">' + ((t.nbKO || 0) + (t.nbFA || 0)) + '</td>'
                    + '<td class="num">' + (t.nbFlaky || 0) + '</td>'
                    + '<td class="num"><b>' + c.okPct + '%</b></td>'
                    + '<td class="num">' + (t.ciResult ? '<span class="chip" style="background:' + (t.ciResult === 'OK' ? '#e9faf2; color:#059669' : '#fdecef; color:#e63757') + '">' + self._esc(t.ciResult) + '</span>' : '-') + '</td>'
                    + '<td class="num">' + (dur !== null ? self.fmtDuration(dur) : '-') + '</td>'
                    + '</tr>';
            });

            var body = ''
                + '<div class="band">'
                +   '<div>'
                +     '<div class="brand">CERBERUS</div>'
                +     '<div class="rpt">Campaign trend report - last ' + tags.length + ' runs</div>'
                +   '</div>'
                +   '<div class="band-right">'
                +     '<div class="tag">' + this._esc(campaign) + '</div>'
                +     '<div class="gen">Current run: ' + this._esc(this.tag) + ' - generated on ' + this._esc(new Date().toLocaleString()) + '</div>'
                +   '</div>'
                + '</div>'
                + deltaHtml
                + '<h2>Result history</h2>'
                + '<div class="chartbox">' + this._printTrendChart(tags) + '<div class="lgds">' + legend + '<span class="lgd"><span class="dot" style="background:#0f172a; opacity:0.6"></span>OK rate</span></div></div>'
                + '<h2>Run by run</h2>'
                + '<table class="data"><thead><tr><th>Tag</th><th>Date</th><th class="num">Exe</th><th class="num">OK</th><th class="num">KO+FA</th><th class="num">Flaky</th><th class="num">% OK</th><th class="num">CI</th><th class="num">Duration</th></tr></thead>'
                + '<tbody>' + rowsHtml + '</tbody></table>'
                + '<div class="foot">Cerberus Testing - trend report for campaign ' + this._esc(campaign) + '</div>';

            return '<!DOCTYPE html><html><head><meta charset="utf-8">'
                + '<title>' + this._esc(campaign) + ' - Cerberus trend report</title>'
                + '<style>' + this._printCss()
                + '.deltas { margin-top: 14px; padding: 10px 14px; border: 1px solid #e2e8f0; border-radius: 10px; background: #f8fafc; font-size: 12px; }'
                + '.chartbox { border: 1px solid #eef2f7; border-radius: 10px; padding: 12px; }'
                + '.lgds { display: flex; gap: 12px; flex-wrap: wrap; margin-top: 6px; }'
                + '.lgd { font-size: 9.5px; font-weight: 700; color: #475569; }'
                + '.chip-blue { background: #dbeafe; color: #1d4ed8; font-size: 8.5px; }'
                + 'tr.cur td { background: #eff6ff; }'
                + '</style></head><body>'
                + '<button class="printbtn" onclick="window.print()">Print or save as PDF</button>'
                + body
                + '<script>setTimeout(function () { window.print(); }, 500);<\/script>'
                + '</body></html>';
        },
        _printCss() {
            return ''
                + '@page { size: A4; margin: 12mm; }'
                + '* { box-sizing: border-box; -webkit-print-color-adjust: exact; print-color-adjust: exact; }'
                + 'body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; color: #0f172a; margin: 0; padding: 24px; font-size: 12px; }'
                + '@media print { body { padding: 0; } .printbtn { display: none; } }'
                + '.printbtn { position: fixed; top: 14px; right: 14px; background: #00BCFF; color: #fff; border: none; border-radius: 8px; padding: 9px 16px; font-size: 13px; font-weight: 600; cursor: pointer; box-shadow: 0 6px 18px rgba(0,0,0,0.18); }'
                + '.band { display: flex; justify-content: space-between; align-items: center; background: linear-gradient(135deg, #0b1120, #1d4ed8); color: #fff; border-radius: 14px; padding: 18px 22px; }'
                + '.brand { font-size: 20px; font-weight: 800; letter-spacing: 3px; }'
                + '.rpt { font-size: 11px; opacity: 0.85; margin-top: 2px; }'
                + '.band-right { text-align: right; }'
                + '.tag { font-size: 15px; font-weight: 700; }'
                + '.gen { font-size: 10px; opacity: 0.75; margin-top: 2px; }'
                + '.verdictrow { display: flex; gap: 12px; margin-top: 14px; align-items: stretch; }'
                + '.verdict { border: 2px solid; border-radius: 14px; padding: 12px 20px; text-align: center; min-width: 150px; }'
                + '.verdict-label { font-size: 9px; font-weight: 800; letter-spacing: 1px; color: #64748b; }'
                + '.verdict-value { font-size: 30px; font-weight: 800; line-height: 1.1; }'
                + '.verdict-score { font-size: 9.5px; color: #64748b; }'
                + '.tiles { display: flex; gap: 10px; flex: 1; }'
                + '.tile { flex: 1; border: 1px solid #e2e8f0; background: #f8fafc; border-radius: 12px; display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 8px; }'
                + '.tile-v { font-size: 22px; font-weight: 800; }'
                + '.tile-l { font-size: 9px; font-weight: 700; text-transform: uppercase; letter-spacing: 0.5px; color: #64748b; margin-top: 2px; }'
                + '.tile-ok { background: #e9faf2; border-color: #b8ecd4; } .tile-ok .tile-v { color: #059669; }'
                + '.tile-ko { background: #fdecef; border-color: #f6c1cb; } .tile-ko .tile-v { color: #e63757; }'
                + '.tile-pe { background: #fff4e6; border-color: #fbd9a5; } .tile-pe .tile-v { color: #f59e0b; }'
                + '.tile-fl { background: #fef3c7; border-color: #fde68a; } .tile-fl .tile-v { color: #b45309; }'
                + '.cols { display: flex; gap: 18px; margin-top: 16px; }'
                + '.donutbox { display: flex; align-items: center; gap: 14px; }'
                + '.infobox { flex: 1; }'
                + 'h2 { font-size: 12px; font-weight: 800; text-transform: uppercase; letter-spacing: 0.6px; color: #334155; margin: 18px 0 6px; border-bottom: 2px solid #e2e8f0; padding-bottom: 4px; }'
                + '.cols h2 { margin-top: 0; }'
                + '.kv { border-collapse: collapse; width: 100%; }'
                + '.kv td { padding: 3px 0; vertical-align: top; }'
                + '.kv .k { color: #64748b; font-weight: 600; width: 110px; font-size: 10.5px; }'
                + '.kv .v { font-weight: 600; }'
                + '.legend { border-collapse: collapse; }'
                + '.legend td { padding: 2.5px 8px 2.5px 0; font-size: 11px; }'
                + '.legend .num { text-align: right; }'
                + '.dot { display: inline-block; width: 9px; height: 9px; border-radius: 2px; margin-right: 2px; vertical-align: baseline; }'
                + '.mut { color: #64748b; }'
                + '.small { font-size: 9.5px; }'
                + '.data { border-collapse: collapse; width: 100%; page-break-inside: auto; }'
                + '.data th { text-align: left; font-size: 9.5px; text-transform: uppercase; letter-spacing: 0.4px; color: #64748b; border-bottom: 1.5px solid #cbd5e1; padding: 5px 8px; }'
                + '.data td { border-bottom: 1px solid #eef2f7; padding: 5px 8px; vertical-align: top; }'
                + '.data tr { page-break-inside: avoid; }'
                + '.data .num, .data th.num { text-align: center; }'
                + '.chip { display: inline-block; font-size: 10px; font-weight: 800; padding: 2px 8px; border-radius: 999px; }'
                + '.chip-red { background: #fdecef; color: #e63757; font-size: 9px; }'
                + '.foot { margin-top: 22px; padding-top: 8px; border-top: 1px solid #e2e8f0; font-size: 9.5px; color: #94a3b8; text-align: center; }';
        },

        // ═══════════════════ LIVE (WS + polling) ═══════════════════
        _bindWsLifecycle() {
            var self = this;
            document.addEventListener(CerberusWs.Event.CONNECTED, function () {
                if (self.loaded) self._setupLive();
            });
            document.addEventListener(CerberusWs.Event.DISCONNECTED, function () {
                self._wsSubscribed = false;
                self._refreshLiveMode();
            });
        },
        _setupLive() {
            this._teardownLive(true);
            var campaign = this.tagObj && this.tagObj.campaign;
            if (campaign) this._subscribeCampaign(campaign);
            this._refreshLiveMode();
        },
        _subscribeCampaign(campaign) {
            var self = this;
            var ws = Alpine.store('ws');
            if (!ws) { this._refreshLiveMode(); return; }

            var channels = [
                CerberusWs.Channel.CHANNEL_CAMPAIGN_DELTA_ID(campaign),
                CerberusWs.Channel.CHANNEL_CAMPAIGN_UPDATE_ID(campaign),
                CerberusWs.Channel.CHANNEL_CAMPAIGN_DONE_ID(campaign)
            ];

            var mk = function (fn) { return function (ev) { fn.call(self, ev.detail && ev.detail.payload, ev.detail); }; };
            var pairs = [
                [CerberusWs.Event.forChannel(channels[0]), mk(this._onDelta)],
                [CerberusWs.Event.forChannel(channels[1]), mk(this._onCampaignUpdate)],
                [CerberusWs.Event.forChannel(channels[2]), mk(this._onCampaignDone)]
            ];
            pairs.forEach(function (p) { document.addEventListener(p[0], p[1]); });
            this._wsHandlers = pairs.map(function (p) { return { event: p[0], handler: p[1] }; });
            this._wsChannels = channels;

            var user = JSON.parse(sessionStorage.getItem('user') || '{}');
            ws.whenConnected().then(function () {
                var ok = ws.send({
                    sender: user.login || 'anonymous',
                    subject: CerberusWs.Subject.SUBSCRIBE,
                    channels: channels,
                    sessionID: 'campaignreport-' + self.tag
                });
                self._wsSubscribed = !!ok;
                self._refreshLiveMode();
            }).catch(function () {
                self._wsSubscribed = false;
                self._refreshLiveMode();
            });
        },
        _teardownLive(keepPolling) {
            var self = this;
            (this._wsHandlers || []).forEach(function (p) { document.removeEventListener(p.event, p.handler); });
            this._wsHandlers = [];
            if (this._wsSubscribed && this._wsChannels.length) {
                var ws = Alpine.store('ws');
                if (ws) ws.send({ subject: CerberusWs.Subject.UNSUBSCRIBE, channels: this._wsChannels, sessionID: 'campaignreport-' + this.tag });
            }
            this._wsSubscribed = false;
            this._wsChannels = [];
            if (!keepPolling && this._pollTimer) { clearInterval(this._pollTimer); this._pollTimer = null; }
        },
        _refreshLiveMode() {
            var running = this.isRunning;
            if (this._wsSubscribed && running) this.liveMode = 'ws';
            else if (running) this.liveMode = 'polling';
            else this.liveMode = 'idle';

            // polling only when something is pending and ws does not cover us
            var needPolling = running && !this._wsSubscribed;
            if (needPolling && !this._pollTimer) {
                var self = this;
                this._pollTimer = setInterval(function () { self.loadReport(true); }, 10000);
            } else if (!needPolling && this._pollTimer) {
                clearInterval(this._pollTimer);
                this._pollTimer = null;
            }
        },
        _pushFeed(entry) {
            entry.ts = Date.now();
            this.liveFeed.unshift(entry);
            if (this.liveFeed.length > 30) this.liveFeed.length = 30;
        },
        _onDelta(light) {
            if (!light || light.tag !== this.tag) return;
            this._pushFeed({
                kind: 'exe',
                status: light.controlStatus || '',
                test: light.test || '',
                testCase: light.testcase || '',
                env: light.environment || '',
                country: light.country || '',
                progress: light.progressPercent,
                executionId: light.testcaseExecutionId
            });
            var row = this.rows.find(function (r) { return r.test === light.test && r.testCase === light.testcase; });
            if (!row) { this._scheduleSoftReload(); return; }

            var cells = Object.values(row.execTab || {});
            var cell = cells.find(function (c) {
                return (light.queueId && String(c.QueueID) === String(light.queueId))
                    || (light.testcaseExecutionId && String(c.ID) === String(light.testcaseExecutionId));
            });
            if (!cell) {
                // fall back on env+country when the combination is unambiguous
                var matches = cells.filter(function (c) { return c.Environment === light.environment && c.Country === light.country; });
                if (matches.length === 1) cell = matches[0];
            }
            if (!cell) { this._scheduleSoftReload(); return; }

            if (light.testcaseExecutionId) cell.ID = String(light.testcaseExecutionId);
            cell.ControlStatus = light.controlStatus || cell.ControlStatus;
            if (light.controlMessage !== undefined) cell.ControlMessage = light.controlMessage;
            if (light.progressPercent !== undefined) cell.progressPercent = light.progressPercent;
            // Reassign so Alpine recomputes the getters built from this.rows
            this.rows = this.rows.slice();
            this._refreshLiveMode();
        },
        _onCampaignUpdate(dto) {
            if (!dto || dto.campaignExecutionId !== this.tag || !this.tagObj) return;
            if (dto.ciResult !== undefined && dto.ciResult !== this.tagObj.ciResult) {
                this.tagObj.ciResult = dto.ciResult;
                this._pushFeed({ kind: 'ci', status: dto.ciResult, text: 'CI result updated: ' + dto.ciResult });
            }
        },
        _onCampaignDone(dto) {
            if (!dto || dto.campaignExecutionId !== this.tag) return;
            var ci = dto.ciResult || '';
            this._pushFeed({ kind: 'done', status: ci, text: 'Campaign finished - CI result: ' + (ci || 'n/a') });
            showMessageMainPage(ci === 'OK' ? 'success' : 'warning', 'Campaign finished — CI result: ' + (ci || 'n/a'), false, 10000);
            this.loadReport(true);
        },
        _scheduleSoftReload() {
            var self = this;
            clearTimeout(this._reloadDebounce);
            this._reloadDebounce = setTimeout(function () { self.loadReport(true); }, 3000);
        },

        // ═══════════════════ FORMAT HELPERS ═══════════════════
        fmtDate(ts) {
            if (!ts) return '-';
            var d = new Date(ts);
            if (isNaN(d.getTime()) || d.getFullYear() < 1990) return '-';
            return d.toLocaleString();
        },
        fmtDuration(ms) {
            if (!ms || ms < 0) return '-';
            var s = Math.round(ms / 1000);
            if (s < 60) return s + 's';
            var m = Math.floor(s / 60);
            if (m < 60) return m + 'm ' + (s % 60) + 's';
            return Math.floor(m / 60) + 'h ' + (m % 60) + 'm';
        },
        fmtTime(ts) {
            if (!ts) return '';
            var d = new Date(ts);
            var p = function (n) { return (n < 10 ? '0' : '') + n; };
            return p(d.getHours()) + ':' + p(d.getMinutes()) + ':' + p(d.getSeconds());
        },
        relTime(ts) {
            if (!ts) return '-';
            var diff = Date.now() - ts;
            if (diff < 0) return '-';
            var min = Math.floor(diff / 60000);
            if (min < 1) return 'just now';
            if (min < 60) return min + ' min ago';
            var h = Math.floor(min / 60);
            if (h < 24) return h + 'h ago';
            var days = Math.floor(h / 24);
            if (days < 31) return days + 'd ago';
            return new Date(ts).toLocaleDateString();
        },
        get tagDurationMin() {
            if (!this.tagObj) return '-';
            var start = this.tagObj.DateStartExe, end = this.tagObj.DateEndQueue;
            if (!start || !end) return '-';
            var d = (new Date(end)) - (new Date(start));
            if (isNaN(d) || d < 0) return '-';
            return Math.round(d / 60000) + ' min';
        },
        pct(n, total) { return total ? Math.round((n / total) * 1000) / 10 : 0; },
        renderLabelNode(node, depth) {
            // TreeNode from the server: { text: <html with label colors + counters>, nodes: [children] }
            if (!node) return '';
            var html = '<div class="v2rt-labeltree-node" style="padding-left:' + (depth * 18) + 'px">' + (node.text || '') + '</div>';
            var self = this;
            (node.nodes || []).forEach(function (n) { html += self.renderLabelNode(n, depth + 1); });
            return html;
        },
        statusColor(s) { return this.statusColors[s] || '#94a3b8'; },
        rowBugs(r) { return Array.isArray(r.bugs) ? r.bugs.filter(function (b) { return b && b.id; }) : []; }
    };
}
