/*
 * Cerberus Copyright (C) 2013 - 2026 cerberustesting
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
 * Web Monitor - the network behavior (HAR statistics) of ONE test case over a period.
 *
 * One call to ReadWebMonitor gives the executions of the test case over twice the chosen
 * period: the second half is only used for the "vs previous period" deltas. Browser,
 * environment and country filters, KPIs, charts and the execution detail are computed
 * here from that payload, so changing a filter never goes back to the server.
 *
 * Favorites (localStorage) are tabs: a tab loads its test case when it is clicked, so
 * going back to a followed test case does not need a new search.
 */
function webMonitor() {
    var IN = window.InsightsShared;
    var HOUR_MS = 3600000;
    var FAV_KEY = 'webMonitorFavorites';
    var COLORS = {ok: '#00d27a', ko: '#e63757', blue: '#2c7be5', purple: '#8b5cf6', amber: '#f59e0b', grey: '#94a3b8', cyan: '#0891b2'};
    var TYPES = [
        {key: 'js', color: COLORS.blue},
        {key: 'img', color: COLORS.ok},
        {key: 'css', color: COLORS.purple},
        {key: 'html', color: COLORS.amber},
        {key: 'media', color: COLORS.ko},
        {key: 'other', color: COLORS.grey}
    ];

    function avg(rows, key) {
        if (!rows.length) { return null; }
        return rows.reduce(function (s, r) { return s + r[key]; }, 0) / rows.length;
    }

    return {
        // ── Test case selection ──
        tests: [],
        browsedTest: '',
        testcasesOfTest: [],
        tcSearch: '',
        tcDdOpen: false,
        favorites: [],
        current: null,            // {test, testCase}

        // ── Filters ──
        periodHours: 24 * 7,
        periods: [{label: '24h', hours: 24}, {label: '7d', hours: 24 * 7}, {label: '30d', hours: 24 * 30}],
        fRobots: [],
        fEnvs: [],
        fCountries: [],

        // ── Data ──
        raw: [],
        asOf: Date.now(),
        selectedId: null,
        loading: false,
        loaded: false,
        error: '',
        loadedAt: 0,
        chartW: 800,

        init() {
            var self = this;
            document.title = this.t('title');
            this.watchIcons();
            try { this.favorites = JSON.parse(localStorage.getItem(FAV_KEY)) || []; } catch (e) { this.favorites = []; }
            $.getJSON('ReadTest', function (data) {
                self.tests = (data.contentTable || []).map(function (t) { return t.test; }).filter(Boolean).sort();
            });
            var test = GetURLParameter('test');
            var testcase = GetURLParameter('testcase');
            if (test && testcase) {
                this.open({test: test, testCase: testcase});
            } else if (this.favorites.length) {
                this.open(this.favorites[0]);
            }
            this.$nextTick(function () { self.measure(); });
            window.addEventListener('resize', function () { self.measure(); });
        },
        // The icons (lucide) are drawn once: the ones rendered later by x-if / x-for need another pass.
        watchIcons() {
            var scheduled = false;
            new MutationObserver(function () {
                if (scheduled || !window.lucide) { return; }
                scheduled = true;
                requestAnimationFrame(function () { scheduled = false; lucide.createIcons(); });
            }).observe(this.$el, {childList: true, subtree: true});
        },
        measure() {
            var el = this.$refs.timeChart;
            if (el && el.clientWidth) { this.chartW = el.clientWidth; }
        },

        // ── Labels (labels.js, pageReportingMonitorWeb) - {0}, {1}... are replaced by the extra arguments ──
        t(key) {
            var text = Alpine.store('labels').getLabel('pageReportingMonitorWeb', key);
            for (var i = 1; i < arguments.length; i++) {
                text = text.split('{' + (i - 1) + '}').join(arguments[i]);
            }
            return text;
        },

        // ── Test case picker (same behavior as the Execution Trends page) ──
        browseTest(test) {
            var self = this;
            this.browsedTest = test;
            this.testcasesOfTest = [];
            this.tcSearch = '';
            if (!test) { return; }
            $.getJSON('ReadTestCase', 'test=' + encodeURIComponent(test), function (data) {
                self.testcasesOfTest = (data.contentTable || []).map(function (tc) {
                    return {test: test, testCase: tc.testcase || tc.testCase, description: tc.description || ''};
                });
            });
        },
        get filteredTests() {
            var q = this.tcSearch.trim().toLowerCase();
            return q ? this.tests.filter(function (t) { return t.toLowerCase().indexOf(q) >= 0; }) : this.tests;
        },
        get filteredTestcases() {
            var q = this.tcSearch.trim().toLowerCase();
            if (!q) { return this.testcasesOfTest; }
            return this.testcasesOfTest.filter(function (tc) {
                return tc.testCase.toLowerCase().indexOf(q) >= 0 || tc.description.toLowerCase().indexOf(q) >= 0;
            });
        },
        pick(tc) {
            this.tcDdOpen = false;
            this.open({test: tc.test, testCase: tc.testCase});
        },

        // ── Favorites / tabs ──
        sameCase(a, b) { return !!a && !!b && a.test === b.test && a.testCase === b.testCase; },
        get isFavorite() {
            var cur = this.current;
            return this.favorites.some(function (f) { return f.test === cur.test && f.testCase === cur.testCase; });
        },
        get adHoc() {
            return this.current && !this.isFavorite;
        },
        toggleFavorite(tc) {
            tc = tc || this.current;
            var self = this;
            var i = this.favorites.findIndex(function (f) { return self.sameCase(f, tc); });
            if (i === -1) { this.favorites.push({test: tc.test, testCase: tc.testCase}); } else { this.favorites.splice(i, 1); }
            localStorage.setItem(FAV_KEY, JSON.stringify(this.favorites));
        },
        // A tab is only loaded when it is opened.
        open(tc) {
            this.current = {test: tc.test, testCase: tc.testCase};
            this.fRobots = []; this.fEnvs = []; this.fCountries = [];
            this.load();
        },

        // ── Data ──
        load() {
            var self = this;
            if (!this.current) { return; }
            var asOf = Date.now();
            var requested = this.current;
            this.loading = true;
            this.error = '';
            $.getJSON('ReadWebMonitor', {
                test: requested.test,
                testcase: requested.testCase,
                from: asOf - 2 * 24 * 30 * HOUR_MS,
                to: asOf
            }, function (data) {
                if (!self.sameCase(requested, self.current)) { return; } // another tab was opened meanwhile
                if (data.messageType === 'KO' || data.messageType === 'FA') {
                    self.error = self.t('loaderror', data.message || '');
                    self.raw = [];
                    return;
                }
                self.raw = (data.contentTable || []).sort(function (a, b) { return a.start - b.start; });
                self.asOf = asOf;
                self.loadedAt = asOf;
                self.loaded = true;
                self.selectedId = null;
            }).fail(function () {
                self.error = self.t('loaderror', 'HTTP');
            }).always(function () {
                self.loading = false;
                self.$nextTick(function () { self.measure(); });
            });
        },
        setPeriod(hours) {
            this.periodHours = hours;
            this.selectedId = null;
        },

        // ── Facets (browser, environment, country): values present in the loaded data ──
        facetValues(key) {
            var seen = {};
            this.raw.forEach(function (r) { if (r[key]) { seen[r[key]] = true; } });
            return Object.keys(seen).sort();
        },
        get robots() { return this.facetValues('robotDecli'); },
        get envs() { return this.facetValues('environment'); },
        get countries() { return this.facetValues('country'); },
        toggleFacet(list, value) {
            var i = list.indexOf(value);
            if (i === -1) { list.push(value); } else { list.splice(i, 1); }
            this.selectedId = null;
        },
        get filtered() {
            var self = this;
            return this.raw.filter(function (r) {
                return (!self.fRobots.length || self.fRobots.indexOf(r.robotDecli) !== -1)
                        && (!self.fEnvs.length || self.fEnvs.indexOf(r.environment) !== -1)
                        && (!self.fCountries.length || self.fCountries.indexOf(r.country) !== -1);
            });
        },
        get rows() {   // current period, oldest first
            var from = this.asOf - this.periodHours * HOUR_MS;
            return this.filtered.filter(function (r) { return r.start >= from; });
        },
        get previousRows() {
            var to = this.asOf - this.periodHours * HOUR_MS;
            var from = to - this.periodHours * HOUR_MS;
            return this.filtered.filter(function (r) { return r.start >= from && r.start < to; });
        },
        get selected() {
            var rows = this.rows;
            var id = this.selectedId;
            return rows.find(function (r) { return r.id === id; }) || rows[rows.length - 1] || null;
        },
        select(r) { this.selectedId = r.id; },
        openExecution(r) {
            if (r) { window.open('./TestCaseExecutionV2.jsp?executionId=' + encodeURIComponent(r.id), '_blank'); }
        },

        // ── KPIs ──
        get kpis() {
            var rows = this.rows;
            var ok = rows.filter(function (r) { return r.controlStatus === 'OK'; }).length;
            return {
                n: rows.length,
                time: avg(rows, 'totalTime'),
                size: avg(rows, 'totalSize'),
                hits: avg(rows, 'totalHits'),
                parties: avg(rows, 'nbThirdParty'),
                rate: rows.length ? 100 * ok / rows.length : null,
                failures: rows.length - ok
            };
        },
        // Delta of the average of `key` against the previous period. A lower value is an improvement.
        delta(key) {
            var cur = avg(this.rows, key);
            var prev = avg(this.previousRows, key);
            if (cur === null || prev === null || prev === 0) { return null; }
            var pct = 100 * (cur - prev) / prev;
            return {pct: pct, text: (pct <= 0 ? '▼ ' : '▲ ') + Math.abs(pct).toFixed(1) + '% ' + this.t('vsprevious'), good: pct <= 0};
        },

        // ── Formatting ──
        fmtMs(v) {
            if (v === null || v === undefined || isNaN(v)) { return '-'; }
            return v >= 10000 ? (v / 1000).toFixed(1) + ' s' : Math.round(v) + ' ms';
        },
        fmtSize(bytes) {
            if (bytes === null || bytes === undefined || isNaN(bytes)) { return '-'; }
            var kb = bytes / 1024;
            return kb >= 1024 ? (kb / 1024).toFixed(1) + ' MB' : Math.round(kb) + ' KB';
        },
        fmtNum(v) { return v === null || v === undefined ? '-' : String(Math.round(v)); },
        fmtDate(ts) {
            var d = new Date(ts);
            var p = function (n) { return (n < 10 ? '0' : '') + n; };
            return p(d.getDate()) + '/' + p(d.getMonth() + 1) + ' ' + p(d.getHours()) + ':' + p(d.getMinutes());
        },
        ago(ts) { return IN.relTime(ts); },
        statusColor(s) { return IN.statusColor(s); },
        statusLabel(r) { return r.controlStatus; },

        // ── Charts (SVG strings, drawn at the real pixel width so that dots stay round) ──
        get timeChart() {
            var rows = this.rows;
            var W = this.chartW, H = 220, padL = 44, padR = 12, padT = 12, padB = 24;
            if (!rows.length) { return ''; }
            var max = 1;
            rows.forEach(function (r) { max = Math.max(max, r.totalTime, r.internalTime); });
            max *= 1.1;
            var innerW = W - padL - padR, innerH = H - padT - padB;
            var xOf = function (i) { return padL + (rows.length > 1 ? i / (rows.length - 1) : 0.5) * innerW; };
            var yOf = function (v) { return padT + innerH * (1 - v / max); };
            var svg = '<svg width="' + W + '" height="' + H + '" viewBox="0 0 ' + W + ' ' + H + '" class="v2in-chart">'
                    + '<defs><linearGradient id="wm-fill" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="' + COLORS.blue + '" stop-opacity="0.28"></stop><stop offset="100%" stop-color="' + COLORS.blue + '" stop-opacity="0"></stop></linearGradient></defs>';
            [0, 0.5, 1].forEach(function (f) {
                var y = padT + innerH * (1 - f);
                svg += '<line x1="' + padL + '" x2="' + (W - padR) + '" y1="' + y.toFixed(1) + '" y2="' + y.toFixed(1) + '" class="v2in-gridline"></line>'
                        + '<text x="' + (padL - 6) + '" y="' + (y + 3).toFixed(1) + '" text-anchor="end" class="v2in-gridlabel">' + IN.esc(Math.round(max * f / 1.1)) + '</text>';
            });
            var line = function (key) {
                return rows.map(function (r, i) { return (i ? 'L' : 'M') + xOf(i).toFixed(1) + ' ' + yOf(r[key]).toFixed(1); }).join(' ');
            };
            if (rows.length > 1) {
                svg += '<path d="' + line('totalTime') + ' L' + xOf(rows.length - 1).toFixed(1) + ' ' + (padT + innerH) + ' L' + xOf(0).toFixed(1) + ' ' + (padT + innerH) + ' Z" fill="url(#wm-fill)"></path>'
                        + '<path d="' + line('internalTime') + '" fill="none" stroke="' + COLORS.purple + '" stroke-width="1.5" stroke-opacity="0.8"></path>'
                        + '<path d="' + line('totalTime') + '" fill="none" stroke="' + COLORS.blue + '" stroke-width="2.2" stroke-linejoin="round"></path>';
            }
            var sel = this.selected;
            rows.forEach(function (r, i) {
                var ko = r.controlStatus !== 'OK';
                var isSel = sel && r.id === sel.id;
                if (isSel) {
                    svg += '<line x1="' + xOf(i).toFixed(1) + '" x2="' + xOf(i).toFixed(1) + '" y1="' + padT + '" y2="' + (padT + innerH) + '" stroke="' + COLORS.blue + '" stroke-opacity="0.4"></line>';
                }
                svg += '<circle cx="' + xOf(i).toFixed(1) + '" cy="' + yOf(r.totalTime).toFixed(1) + '" r="' + (isSel ? 5 : (ko ? 4 : 2.5)) + '" fill="' + IN.statusColor(r.controlStatus)
                        + '" class="v2in-dot" data-id="' + r.id + '"><title>#' + r.id + ' - ' + IN.esc(this.fmtDate(r.start)) + ' - ' + r.totalTime + ' ms - ' + IN.esc(r.controlStatus) + '</title></circle>';
            }, this);
            svg += '<text x="' + padL + '" y="' + (H - 6) + '" class="v2in-xlabel">' + IN.esc(this.fmtDate(rows[0].start)) + '</text>'
                    + '<text x="' + (W - padR) + '" y="' + (H - 6) + '" text-anchor="end" class="v2in-xlabel">' + IN.esc(this.fmtDate(rows[rows.length - 1].start)) + '</text>';
            return svg + '</svg>';
        },
        get trafficChart() {
            var rows = this.rows;
            var W = 300, H = 110;
            if (!rows.length) { return ''; }
            var maxS = 1, maxH = 1;
            rows.forEach(function (r) { maxS = Math.max(maxS, r.totalSize); maxH = Math.max(maxH, r.totalHits); });
            var xOf = function (i) { return rows.length > 1 ? i / (rows.length - 1) * W : W / 2; };
            var path = function (key, max) {
                return rows.map(function (r, i) { return (i ? 'L' : 'M') + xOf(i).toFixed(1) + ' ' + (H - 8 - (H - 16) * r[key] / max).toFixed(1); }).join(' ');
            };
            var svg = '<svg viewBox="0 0 ' + W + ' ' + H + '" preserveAspectRatio="none" class="v2in-chart" style="height:110px">';
            if (rows.length > 1) {
                svg += '<path d="' + path('totalHits', maxH) + '" fill="none" stroke="' + COLORS.blue + '" stroke-width="1.4" stroke-opacity="0.6" vector-effect="non-scaling-stroke"></path>'
                        + '<path d="' + path('totalSize', maxS) + '" fill="none" stroke="' + COLORS.ok + '" stroke-width="2.2" vector-effect="non-scaling-stroke"></path>';
            }
            return svg + '</svg>';
        },
        onChartClick(e) {
            var id = e.target && e.target.getAttribute && e.target.getAttribute('data-id');
            if (id) { this.selectedId = Number(id); }
        },
        get strip() {   // status strip: the last 60 executions
            return this.rows.slice(-60);
        },

        // ── Weight by content type / third party hosts of the selected execution ──
        get typeWeights() {
            var r = this.selected;
            if (!r) { return {total: 0, items: []}; }
            var known = 0;
            var items = TYPES.filter(function (t) { return t.key !== 'other'; }).map(function (t) {
                var size = r.sizeByType[t.key] || 0;
                known += size;
                return {key: t.key, color: t.color, size: size};
            });
            items.push({key: 'other', color: COLORS.grey, size: Math.max(0, r.totalSize - known)});
            var total = items.reduce(function (s, i) { return s + i.size; }, 0) || 1;
            items.forEach(function (i) { i.pct = 100 * i.size / total; });
            return {total: r.totalSize, items: items.filter(function (i) { return i.size > 0; })};
        },
        get partyList() {
            var r = this.selected;
            if (!r) { return []; }
            var list = (r.thirdParties || []).slice().sort(function (a, b) { return b.size - a.size; }).slice(0, 8);
            var max = list.length ? Math.max(1, list[0].size) : 1;
            return list.map(function (p) { return {name: p.name, requests: p.requests, size: p.size, pct: 100 * p.size / max}; });
        },
        get latest() {
            return this.rows.slice(-20).reverse();
        }
    };
}