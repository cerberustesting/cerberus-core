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
 * Real-Time Monitor - Alpine board over the EXECUTION_MONITOR websocket channel.
 *
 * The server pushes the last executions grouped in boxes (one box per test case x
 * country x environment x robot). Everything else is client side: reference filters
 * (system, campaign, environment, country), time horizon, composable grouping
 * dimensions with an auto mode, TV fullscreen overlay and shareable URL. Parameter
 * names in the URL stay compatible with the legacy monitor links.
 *
 * Performance: the board is computed once into boardData (plain data) and only
 * recomputed on push, on a setting change or on the 30s horizon tick - never on the
 * 1s ticker, which only feeds the cheap "Xs ago" text bindings. This matters on TV
 * screens that keep the page open for weeks with hundreds of tiles.
 */
function executionMonitor() {
    var IN = window.InsightsShared;
    var GROUP_DIMS = [
        { key: 'system', label: 'System' },
        { key: 'application', label: 'Application' },
        { key: 'test', label: 'Folder' },
        { key: 'testCase', label: 'Test case' },
        { key: 'country', label: 'Country' },
        { key: 'environment', label: 'Environment' },
        { key: 'robot', label: 'Robot' },
        { key: 'campaign', label: 'Campaign' }
    ];
    return {
        // ── Live state ──
        wsConnected: false,
        subscribed: false,
        lastPush: 0,
        nowTick: Date.now(),      // 1s ticker: only for the "ago" text bindings
        raw: null,
        boardData: { groups: [], statusAgg: {}, total: 0, cols: ['application'], rawBoxes: 0 },
        _tickTimer: null,
        _horizonTimer: null,
        _horizonTick: Date.now(), // 30s ticker: drives the horizon-based recompute
        _lastByBox: {},
        _flash: {},
        _faviconSig: '',

        // ── Reference lists (filters offer every known value, not only the observed ones) ──
        refSystems: [],
        refCampaigns: [],   // [{campaign, description}]
        refEnvs: [],
        refCountries: [],

        // ── Selections (empty array = no restriction) ──
        selSystems: [],
        selCampaigns: [],
        selEnvs: [],
        selCountries: [],
        ddOpen: '',
        ddSearch: '',

        // ── Display settings ──
        horizonMin: 480,
        prevExe: 5,
        showRetry: false,
        showMuted: false,
        tvMode: false,
        groupCols: { system: false, application: true, test: false, testCase: false, country: false, environment: false, robot: false, campaign: false },
        autoCols: true,

        GROUP_DIMS: GROUP_DIMS,
        IN: IN,

        // ═══════════════════ INIT ═══════════════════
        init() {
            var self = this;
            this._loadSettings();
            this._loadReferences();

            // one deep watcher drives persistence, the shareable URL and the board recompute
            this.$watch('boardSettingsSignature', function () {
                self._sanitizeNumbers();
                self._persist();
                self._syncUrl();
                self._recomputeBoard();
            });
            this.$watch('tvMode', function (v) {
                self._syncUrl();
                document.documentElement.classList.toggle('v2mo-tv-on', v);
            });
            // update the favicon badge when the aggregated board status changes
            this.$watch('faviconSignature', function (sig) { self._updateFavicon(sig); });

            // ESC leaves the TV mode
            document.addEventListener('keydown', function (e) {
                if (e.key === 'Escape' && self.tvMode) self.tvMode = false;
            });

            // 1s ticker: cheap text bindings only. 30s ticker: horizon recompute.
            this._tickTimer = setInterval(function () { self.nowTick = Date.now(); }, 1000);
            this._horizonTimer = setInterval(function () {
                self._horizonTick = Date.now();
                self._recomputeBoard();
            }, 30000);
            window.addEventListener('beforeunload', function () {
                clearInterval(self._tickTimer);
                clearInterval(self._horizonTimer);
            });

            // websocket lifecycle
            document.addEventListener(CerberusWs.Event.forChannel(CerberusWs.Channel.EXECUTION_MONITOR), function (e) {
                var message = e.detail || {};
                self._onPush(message.payload || message);
            });
            document.addEventListener(CerberusWs.Event.CONNECTED, function () {
                self.wsConnected = true;
                self.subscribed = false;
                self._subscribe();
            });
            document.addEventListener(CerberusWs.Event.DISCONNECTED, function () {
                self.wsConnected = false;
                self.subscribed = false;
            });
            var ws = Alpine.store('ws');
            if (ws) {
                ws.whenConnected().then(function () {
                    self.wsConnected = true;
                    self._subscribe();
                }).catch(function () { /* badge stays off */ });
            }
        },
        // every setting the board depends on, serialized so the watcher registers deep dependencies
        get boardSettingsSignature() {
            return JSON.stringify([
                this.selSystems, this.selCampaigns, this.selEnvs, this.selCountries,
                this.showRetry, this.showMuted, this.horizonMin, this.prevExe,
                this.autoCols, this.groupCols
            ]);
        },
        _sanitizeNumbers() {
            // number inputs can hold NaN/empty while typing; clamp to safe ranges
            if (!isFinite(this.horizonMin) || this.horizonMin < 1) this.horizonMin = 1;
            if (this.horizonMin > 43200) this.horizonMin = 43200; // 30 days
            if (!isFinite(this.prevExe) || this.prevExe < 0) this.prevExe = 0;
            if (this.prevExe > 10) this.prevExe = 10;
            this.horizonMin = Math.round(this.horizonMin);
            this.prevExe = Math.round(this.prevExe);
        },
        _loadSettings() {
            // saved preferences first, then the URL wins (legacy-compatible parameter names)
            try {
                var saved = JSON.parse(localStorage.getItem('mo.filters') || 'null');
                if (saved && typeof saved === 'object') {
                    if (isFinite(saved.horizonMin)) this.horizonMin = saved.horizonMin;
                    if (isFinite(saved.prevExe)) this.prevExe = saved.prevExe;
                    if (typeof saved.showRetry === 'boolean') this.showRetry = saved.showRetry;
                    if (typeof saved.showMuted === 'boolean') this.showMuted = saved.showMuted;
                    if (typeof saved.autoCols === 'boolean') this.autoCols = saved.autoCols;
                    if (saved.groupCols && typeof saved.groupCols === 'object' && !Array.isArray(saved.groupCols)) {
                        var self = this;
                        Object.keys(this.groupCols).forEach(function (k) {
                            if (typeof saved.groupCols[k] === 'boolean') self.groupCols[k] = saved.groupCols[k];
                        });
                    }
                    if (Array.isArray(saved.selSystems)) this.selSystems = saved.selSystems.filter(function (v) { return typeof v === 'string'; });
                    if (Array.isArray(saved.selCampaigns)) this.selCampaigns = saved.selCampaigns.filter(function (v) { return typeof v === 'string'; });
                    if (Array.isArray(saved.selEnvs)) this.selEnvs = saved.selEnvs.filter(function (v) { return typeof v === 'string'; });
                    if (Array.isArray(saved.selCountries)) this.selCountries = saved.selCountries.filter(function (v) { return typeof v === 'string'; });
                }
            } catch (e) { /* defaults */ }
            try {
                var usp = new URLSearchParams(window.location.search);
                if (usp.getAll('systems').length) this.selSystems = usp.getAll('systems');
                if (usp.getAll('campaigns').length) this.selCampaigns = usp.getAll('campaigns');
                if (usp.getAll('environments').length) this.selEnvs = usp.getAll('environments');
                if (usp.getAll('countries').length) this.selCountries = usp.getAll('countries');
                var hor = parseInt(usp.get('displayHorizonMin'), 10);
                if (!isNaN(hor)) this.horizonMin = hor;
                var prev = parseInt(usp.get('maxPreviousExe'), 10);
                if (!isNaN(prev)) this.prevExe = prev;
                if (usp.get('displayRetry')) this.showRetry = usp.get('displayRetry') === 'true';
                if (usp.get('displayMuted')) this.showMuted = usp.get('displayMuted') === 'true';
                if (usp.get('autoCol')) this.autoCols = usp.get('autoCol') === 'true';
                var cols = usp.getAll('col');
                if (cols.length) {
                    var self2 = this;
                    Object.keys(this.groupCols).forEach(function (k) { self2.groupCols[k] = false; });
                    cols.forEach(function (c) { if (self2.groupCols[c] !== undefined) self2.groupCols[c] = true; });
                }
                if (usp.get('fullscreen') === 'true') this.tvMode = true;
            } catch (e) { /* ignore */ }
            this._sanitizeNumbers();
        },
        _loadReferences() {
            var self = this;
            try {
                var user = JSON.parse(sessionStorage.getItem('user') || '{}');
                this.refSystems = (Array.isArray(user.system) ? user.system : []).slice().sort();
            } catch (e) { /* empty */ }
            $.getJSON('ReadCampaign', function (data) {
                self.refCampaigns = ((data && data.contentTable) || []).map(function (c) {
                    return { campaign: c.campaign, description: c.description || '' };
                }).sort(function (a, b) { return a.campaign.localeCompare(b.campaign); });
            });
            // FindInvariantByID answers a plain array
            $.getJSON('FindInvariantByID', { idName: 'ENVIRONMENT' }, function (data) {
                var list = Array.isArray(data) ? data : (data.contentTable || []);
                self.refEnvs = list.map(function (i) { return i.value; }).filter(Boolean);
            });
            $.getJSON('FindInvariantByID', { idName: 'COUNTRY' }, function (data) {
                var list = Array.isArray(data) ? data : (data.contentTable || []);
                self.refCountries = list.map(function (i) { return i.value; }).filter(Boolean);
            });
        },
        _persist() {
            localStorage.setItem('mo.filters', JSON.stringify({
                horizonMin: this.horizonMin, prevExe: this.prevExe,
                showRetry: this.showRetry, showMuted: this.showMuted,
                autoCols: this.autoCols, groupCols: this.groupCols,
                selSystems: this.selSystems, selCampaigns: this.selCampaigns,
                selEnvs: this.selEnvs, selCountries: this.selCountries
            }));
        },
        _syncUrl() {
            // legacy-compatible shareable URL (also drives the TV dashboards)
            var self = this;
            var qs = [];
            var addAll = function (name, arr) { (arr || []).forEach(function (v) { qs.push(name + '=' + encodeURIComponent(v)); }); };
            addAll('systems', this.selSystems);
            addAll('campaigns', this.selCampaigns);
            addAll('environments', this.selEnvs);
            addAll('countries', this.selCountries);
            if (!this.autoCols) {
                Object.keys(this.groupCols).forEach(function (k) { if (self.groupCols[k]) qs.push('col=' + k); });
            }
            qs.push('displayHorizonMin=' + this.horizonMin);
            qs.push('maxPreviousExe=' + this.prevExe);
            qs.push('displayRetry=' + this.showRetry);
            qs.push('displayMuted=' + this.showMuted);
            qs.push('autoCol=' + this.autoCols);
            if (this.tvMode) qs.push('fullscreen=true');
            if (typeof InsertURLInHistory === 'function') InsertURLInHistory('./ReportingMonitor.jsp?' + qs.join('&'));
        },
        _subscribe() {
            if (this.subscribed) return;
            var user = JSON.parse(sessionStorage.getItem('user') || '{}');
            var ws = Alpine.store('ws');
            if (!ws || !user.login) return;
            var sent = ws.send({
                sender: user.login,
                sessionID: 'executionmonitor-' + user.login,
                subject: CerberusWs.Subject.SUBSCRIBE,
                channels: [CerberusWs.Channel.EXECUTION_MONITOR]
            });
            this.subscribed = !!sent;
        },
        _onPush(data) {
            var self = this;
            this.lastPush = Date.now();
            // flash the boxes whose latest execution changed since the previous push;
            // the highlight clears itself, without any per-second board dependency
            var boxes = (data && data.executionBoxes) || {};
            Object.keys(boxes).forEach(function (k) {
                var ids = boxes[k];
                var last = ids && ids.length ? ids[ids.length - 1] : null;
                if (self._lastByBox[k] !== undefined && self._lastByBox[k] !== last) {
                    var stamp = Date.now();
                    self._flash[k] = stamp;
                    setTimeout(function () {
                        if (self._flash[k] === stamp) delete self._flash[k];
                    }, 4000);
                }
                self._lastByBox[k] = last;
            });
            this.raw = data;
            this._horizonTick = Date.now();
            this._recomputeBoard();
        },

        // ═══════════════════ FILTER DROPDOWNS ═══════════════════
        openDd(name) {
            this.ddOpen = this.ddOpen === name ? '' : name;
            this.ddSearch = '';
            if (this.ddOpen) {
                this.$nextTick(function () {
                    var input = document.querySelector('.v2mo-filters .v2in-dd input');
                    if (input && input.offsetParent !== null) input.focus();
                });
            }
        },
        ddItems(name) {
            var q = this.ddSearch.trim().toLowerCase();
            var match = function (s) { return !q || String(s).toLowerCase().indexOf(q) >= 0; };
            if (name === 'system') return this.refSystems.filter(match);
            if (name === 'environment') return this.refEnvs.filter(match);
            if (name === 'country') return this.refCountries.filter(match);
            if (name === 'campaign') {
                var self = this;
                return this.refCampaigns.filter(function (c) { return match(c.campaign) || match(c.description); })
                    .map(function (c) { return c.campaign; });
            }
            return [];
        },
        selOf(name) {
            if (name === 'system') return this.selSystems;
            if (name === 'campaign') return this.selCampaigns;
            if (name === 'environment') return this.selEnvs;
            return this.selCountries;
        },
        toggleSel(name, v) {
            var arr = this.selOf(name);
            var i = arr.indexOf(v);
            if (i >= 0) arr.splice(i, 1); else arr.push(v);
        },
        clearSel(name) {
            var arr = this.selOf(name);
            arr.splice(0, arr.length);
        },
        pickerLabel(name, allLabel) {
            var arr = this.selOf(name);
            if (!arr.length) return allLabel;
            if (arr.length === 1) return arr[0];
            return arr.slice(0, 2).join(', ') + (arr.length > 2 ? ' +' + (arr.length - 2) : '');
        },
        campaignDesc(campaign) {
            var hit = this.refCampaigns.find(function (c) { return c.campaign === campaign; });
            return hit ? hit.description : '';
        },
        get activeFilterChips() {
            var out = [];
            var push = function (name, arr) { arr.forEach(function (v) { out.push({ group: name, value: v }); }); };
            push('system', this.selSystems);
            push('campaign', this.selCampaigns);
            push('environment', this.selEnvs);
            push('country', this.selCountries);
            return out;
        },
        clearAllFilters() {
            this.selSystems.splice(0); this.selCampaigns.splice(0);
            this.selEnvs.splice(0); this.selCountries.splice(0);
        },

        // ═══════════════════ GROUPING ═══════════════════
        toggleCol(key) {
            if (this.autoCols) return;
            this.groupCols[key] = !this.groupCols[key];
        },
        isColOn(key) { return this.boardData.cols.indexOf(key) >= 0; },

        // ═══════════════════ BOARD (computed once, cached in boardData) ═══════════════════
        _visibleExe(e) {
            if (!e) return false;
            if (e.muted && !this.showMuted) return false;
            if (!e.usefull && !this.showRetry) return false;
            var ageMin = (this._horizonTick - (e.start || 0)) / 60000;
            if (ageMin > this.horizonMin) return false;
            if (this.selSystems.length && this.selSystems.indexOf(e.system) < 0) return false;
            if (this.selEnvs.length && this.selEnvs.indexOf(e.environment) < 0) return false;
            if (this.selCountries.length && this.selCountries.indexOf(e.country) < 0) return false;
            if (this.selCampaigns.length && this.selCampaigns.indexOf(e.campaign) < 0) return false;
            return true;
        },
        _autoPickCols(lasts) {
            // group on the dimensions that really split the visible board (max 3)
            var picked = [];
            var priority = ['application', 'test', 'environment', 'country', 'system', 'campaign', 'robot'];
            priority.forEach(function (dim) {
                if (picked.length >= 3) return;
                var seen = {};
                lasts.forEach(function (e) { seen[e[dim] || '-'] = true; });
                if (Object.keys(seen).length > 1) picked.push(dim);
            });
            return picked.length ? picked : ['application'];
        },
        _recomputeBoard() {
            var self = this;
            var data = this.raw || {};
            var exes = data.executions || {};
            var boxes = data.executionBoxes || {};
            var boxKeys = Object.keys(boxes);

            // visible last execution of each box (single pass, reused by auto mode)
            var visible = [];   // [{boxKey, ids}]
            boxKeys.forEach(function (boxKey) {
                var ids = (boxes[boxKey] || []).filter(function (id) { return self._visibleExe(exes[id]); });
                if (ids.length) visible.push({ boxKey: boxKey, ids: ids });
            });

            var cols;
            if (this.autoCols) {
                cols = this._autoPickCols(visible.map(function (v) { return exes[v.ids[v.ids.length - 1]]; }));
            } else {
                var m = this.groupCols;
                var manual = Object.keys(m).filter(function (k) { return m[k]; });
                cols = manual.length ? manual : ['application'];
            }

            var groups = {};
            var statusAgg = {};
            var total = 0;
            visible.forEach(function (v) {
                var last = exes[v.ids[v.ids.length - 1]];
                var prevs = self.prevExe > 0 ? v.ids.slice(0, -1).slice(-self.prevExe).map(function (id) { return exes[id]; }) : [];
                var groupVal = cols.map(function (dim) { return last[dim] || '-'; }).join(' · ');
                if (!groups[groupVal]) groups[groupVal] = { label: groupVal, tiles: [] };
                groups[groupVal].tiles.push({ key: v.boxKey, last: last, prevs: prevs });
                var st = last.falseNegative ? 'FN' : (last.controlStatus || 'NE');
                statusAgg[st] = (statusAgg[st] || 0) + 1;
                total++;
            });
            var list = Object.values(groups);
            list.forEach(function (g) {
                // same reading order as the legacy board: test case, then country, then environment
                g.tiles.sort(function (a, b) {
                    var x = a.last, y = b.last;
                    return (x.testCase || '').localeCompare(y.testCase || '')
                        || (x.country || '').localeCompare(y.country || '')
                        || (x.environment || '').localeCompare(y.environment || '');
                });
            });
            list.sort(function (a, b) { return a.label.localeCompare(b.label); });
            this.boardData = { groups: list, statusAgg: statusAgg, total: total, cols: cols, rawBoxes: boxKeys.length };
        },
        get statusSegments() {
            var agg = this.boardData.statusAgg;
            var total = this.boardData.total || 1;
            var self = this;
            var order = this.IN.statusOrder.concat(['FN']);
            return order.filter(function (s) { return agg[s]; }).map(function (s) {
                return { status: s, count: agg[s], pct: agg[s] * 100 / total, color: self.IN.statusColor(s) };
            });
        },
        get pushAgo() {
            if (!this.lastPush) return null;
            return Math.max(0, Math.round((this.nowTick - this.lastPush) / 1000));
        },
        exeAgo(e) {
            if (!e || !e.start) return '-';
            var s = Math.max(0, Math.round((this.nowTick - e.start) / 1000));
            if (s < 60) return s + 's';
            var m = Math.floor(s / 60);
            if (m < 60) return m + 'm';
            var h = Math.floor(m / 60);
            if (h < 24) return h + 'h';
            return Math.floor(h / 24) + 'd';
        },
        get horizonLabel() {
            var m = this.horizonMin;
            if (m < 60) return m + ' min';
            if (m < 1440) return Math.round(m / 60) + 'h';
            return Math.round(m / 1440) + ' day(s)';
        },

        // ═══════════════════ FAVICON (KO alert on the browser tab) ═══════════════════
        get faviconSignature() {
            if (!this.raw) return '';
            var agg = this.boardData.statusAgg;
            var keys = Object.keys(agg);
            if (!keys.length) return 'none';
            var allOk = keys.every(function (k) { return k === 'OK' || k === 'FN'; });
            return allOk ? 'ok' : 'ko';
        },
        _updateFavicon(sig) {
            if (sig === this._faviconSig || typeof Favico === 'undefined') return;
            this._faviconSig = sig;
            try {
                var favicon = new Favico({ animation: 'none', bgColor: sig === 'ko' ? '#e63757' : '#00d27a' });
                if (sig === 'ok' || sig === 'ko') favicon.badge('!'); else favicon.reset();
            } catch (e) { /* favicon is cosmetic */ }
        },

        // ═══════════════════ ACTIONS ═══════════════════
        openExe(e) {
            if (e && e.id) window.open('./TestCaseExecutionV2.jsp?executionId=' + e.id, '_blank');
        },

        // ═══════════════════ TILES ═══════════════════
        statusColor(s) { return this.IN.statusColor(s); },
        tileColor(t) {
            return this.IN.statusColor(t.last.falseNegative ? 'FN' : t.last.controlStatus);
        },
        tileCtx(t) {
            // do not repeat the dimensions already carried by the group header
            var e = t.last;
            var cols = this.boardData.cols;
            var parts = [];
            if (cols.indexOf('environment') < 0 && e.environment) parts.push(e.environment);
            if (cols.indexOf('country') < 0 && e.country) parts.push(e.country);
            if (cols.indexOf('robot') < 0 && e.robot) parts.push(e.robot);
            return parts.join(' - ');
        },
        tileTitle(t) {
            var e = t.last;
            var lines = [];
            lines.push('Execution #' + e.id + ' - ' + (e.controlStatus || '') + (e.falseNegative ? ' (false negative)' : ''));
            lines.push(e.test + ' / ' + e.testCase);
            if (e.description) lines.push(e.description);
            lines.push('Env: ' + e.environment + '   Country: ' + e.country + (e.robot ? '   Robot: ' + e.robot : ''));
            lines.push('Application: ' + e.application + (e.system ? ' [' + e.system + ']' : ''));
            if (e.campaign) lines.push('Campaign: ' + e.campaign);
            if (e.start) {
                var l = 'Start: ' + this.IN.fmtDateTime(e.start);
                if (e.end && e.end > e.start) l += '   (' + this.IN.fmtDuration(e.end - e.start) + ')';
                lines.push(l);
            }
            if (!e.usefull || e.muted) lines.push((!e.usefull ? '[RETRY] ' : '') + (e.muted ? '[MUTED]' : ''));
            if (e.controlMessage) lines.push(String(e.controlMessage).slice(0, 200));
            return lines.join('\n');
        }
    };
}
