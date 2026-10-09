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
 * API Monitor - the calls of ONE registered service made by the executions (table
 * testcaseexecutionservicecall, filled by ServiceService.callService).
 *
 * One call to ReadApiMonitor gives the calls of the service over twice the chosen period:
 * the second half only feeds the "vs previous period" deltas. Environment and country
 * filters, KPIs, percentiles, distribution, HTTP codes, consumers and the charts are
 * computed here, so changing a filter never goes back to the server.
 *
 * Favorites (localStorage) are tabs: a tab loads its service when it is clicked.
 */
function apiMonitor() {
    var IN = window.InsightsShared;
    var HOUR_MS = 3600000;
    var FAV_KEY = 'apiMonitorFavorites';
    var MAX_POINTS = 120;       // the time chart groups the calls beyond that
    var COLORS = {ok: '#00d27a', ko: '#e63757', blue: '#2c7be5', purple: '#8b5cf6', amber: '#f59e0b', grey: '#94a3b8', orange: '#f5803e'};
    var RANGES = [
        {label: '< 100 ms', max: 100}, {label: '< 200 ms', max: 200}, {label: '< 500 ms', max: 500},
        {label: '< 1 s', max: 1000}, {label: '< 2 s', max: 2000}, {label: '< 5 s', max: 5000}, {label: '≥ 5 s', max: Infinity}
    ];
    var HTTP_CLASSES = [
        {key: '2', label: '2xx', color: COLORS.ok}, {key: '3', label: '3xx', color: COLORS.blue},
        {key: '4', label: '4xx', color: COLORS.amber}, {key: '5', label: '5xx', color: COLORS.ko}, {key: '0', label: null, color: COLORS.grey}
    ];

    function avg(rows, key) {
        if (!rows.length) { return null; }
        return rows.reduce(function (s, r) { return s + r[key]; }, 0) / rows.length;
    }
    function percentile(values, q) {
        if (!values.length) { return null; }
        var sorted = values.slice().sort(function (a, b) { return a - b; });
        return sorted[Math.min(sorted.length - 1, Math.max(0, Math.ceil(q * sorted.length) - 1))];
    }
    function isOk(r) { return r.status === 'OK'; }

    return {
        // ── Service selection ──
        services: [],              // [{service, calls}] called over the last 30 days
        svcSearch: '',
        svcDdOpen: false,
        favorites: [],
        current: '',

        // ── Filters ──
        periodHours: 24 * 7,
        periods: [{label: '24h', hours: 24}, {label: '7d', hours: 24 * 7}, {label: '30d', hours: 24 * 30}],
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
        files: {},                 // call id -> recorded files [{fileDesc, fileName, fileType}], loaded when the call is inspected

        init() {
            var self = this;
            document.title = this.t('title');
            this.watchIcons();
            try { this.favorites = JSON.parse(localStorage.getItem(FAV_KEY)) || []; } catch (e) { this.favorites = []; }
            $.getJSON('ReadApiMonitor', {list: true, from: Date.now() - 30 * 24 * HOUR_MS, to: Date.now()}, function (data) {
                self.services = data.contentTable || [];
            });
            var service = GetURLParameter('service');
            if (service) {
                this.open(service);
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

        // ── Labels (labels.js, pageReportingMonitorApi) - {0}, {1}... are replaced by the extra arguments ──
        t(key) {
            var text = Alpine.store('labels').getLabel('pageReportingMonitorApi', key);
            for (var i = 1; i < arguments.length; i++) {
                text = text.split('{' + (i - 1) + '}').join(arguments[i]);
            }
            return text;
        },

        // ── Service picker ──
        get filteredServices() {
            var q = this.svcSearch.trim().toLowerCase();
            return q ? this.services.filter(function (s) { return s.service.toLowerCase().indexOf(q) >= 0; }) : this.services;
        },
        pick(service) {
            this.svcDdOpen = false;
            this.open(service);
        },

        // ── Favorites / tabs ──
        get isFavorite() { return this.favorites.indexOf(this.current) !== -1; },
        get adHoc() { return !!this.current && !this.isFavorite; },
        toggleFavorite(service) {
            service = service || this.current;
            var i = this.favorites.indexOf(service);
            if (i === -1) { this.favorites.push(service); } else { this.favorites.splice(i, 1); }
            localStorage.setItem(FAV_KEY, JSON.stringify(this.favorites));
        },
        // A tab is only loaded when it is opened.
        open(service) {
            this.current = service;
            this.fEnvs = []; this.fCountries = [];
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
            $.getJSON('ReadApiMonitor', {
                service: requested,
                from: asOf - 2 * 24 * 30 * HOUR_MS,
                to: asOf
            }, function (data) {
                if (requested !== self.current) { return; } // another tab was opened meanwhile
                if (data.messageType === 'KO' || data.messageType === 'FA') {
                    self.error = self.t('loaderror', data.message || '');
                    self.raw = [];
                    return;
                }
                self.raw = data.contentTable || [];
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

        // ── Facets (environment, country): values present in the loaded data ──
        facetValues(key) {
            var seen = {};
            this.raw.forEach(function (r) { if (r[key]) { seen[r[key]] = true; } });
            return Object.keys(seen).sort();
        },
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
                return (!self.fEnvs.length || self.fEnvs.indexOf(r.environment) !== -1)
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
            if (r) { window.open('./TestCaseExecutionV2.jsp?executionId=' + encodeURIComponent(r.exeId), '_blank'); }
        },

        // ── KPIs ──
        get kpis() {
            var rows = this.rows;
            var durations = rows.map(function (r) { return r.durationMs; });
            var ok = rows.filter(isOk).length;
            return {
                n: rows.length,
                avg: avg(rows, 'durationMs'),
                p50: percentile(durations, 0.5),
                p95: percentile(durations, 0.95),
                rate: rows.length ? 100 * ok / rows.length : null,
                errors: rows.length - ok
            };
        },
        // Delta of the average / p95 against the previous period. A lower value is an improvement.
        delta(kind) {
            var value = function (rows) {
                if (!rows.length) { return null; }
                return kind === 'p95' ? percentile(rows.map(function (r) { return r.durationMs; }), 0.95) : avg(rows, 'durationMs');
            };
            var cur = value(this.rows), prev = value(this.previousRows);
            if (cur === null || prev === null || prev === 0) { return null; }
            var pct = 100 * (cur - prev) / prev;
            return {text: (pct <= 0 ? '▼ ' : '▲ ') + Math.abs(pct).toFixed(1) + '% ' + this.t('vsprevious'), good: pct <= 0};
        },

        // ── Recorded request / response of the inspected call (files on disk, see RecorderService.recordServiceCall) ──
        loadFiles(call) {
            var self = this;
            if (!call || !call.fileLevel || this.files[call.id] !== undefined) { return; }
            this.files[call.id] = [];
            $.getJSON('ReadApiMonitor', {files: true, exeid: call.exeId, level: call.fileLevel}, function (data) {
                self.files[call.id] = data.contentTable || [];
            });
        },
        filesOf(call) {
            return call ? (this.files[call.id] || []) : [];
        },
        fileLabel(file) {
            var key = {'Request': 'filerequest', 'Response': 'fileresponse', 'Service Call': 'filecall'}[file.fileDesc];
            return key ? this.t(key) : file.fileDesc;
        },
        fileUrl(call, file) {
            return 'ReadTestCaseExecutionMedia?filename=' + encodeURIComponent(file.fileName)
                    + '&filetype=' + encodeURIComponent(file.fileType || '')
                    + '&filedesc=' + encodeURIComponent(file.fileDesc || '')
                    + '&id=' + call.exeId + '&r=true';
        },

        // ── Formatting ──
        fmtMs(v) {
            if (v === null || v === undefined || isNaN(v)) { return '-'; }
            return v >= 10000 ? (v / 1000).toFixed(1) + ' s' : Math.round(v) + ' ms';
        },
        fmtSize(bytes) {
            if (bytes === null || bytes === undefined || isNaN(bytes)) { return '-'; }
            if (bytes < 1024) { return bytes + ' B'; }
            var kb = bytes / 1024;
            return kb >= 1024 ? (kb / 1024).toFixed(1) + ' MB' : Math.round(kb) + ' KB';
        },
        fmtDate(ts) {
            var d = new Date(ts);
            var p = function (n) { return (n < 10 ? '0' : '') + n; };
            return p(d.getDate()) + '/' + p(d.getMonth() + 1) + ' ' + p(d.getHours()) + ':' + p(d.getMinutes());
        },
        ago(ts) { return IN.relTime(ts); },
        statusColor(s) { return s === 'OK' ? COLORS.ok : (IN.statusColor(s) || COLORS.ko); },
        httpColor(code) {
            return code >= 500 ? COLORS.ko : code >= 400 ? COLORS.amber : code >= 300 ? COLORS.blue : code >= 200 ? COLORS.ok : COLORS.grey;
        },

        // ── Time chart: one point per call, or per group of calls beyond MAX_POINTS ──
        get groupSize() { return Math.max(1, Math.ceil(this.rows.length / MAX_POINTS)); },
        get points() {
            var rows = this.rows, size = this.groupSize, out = [];
            for (var i = 0; i < rows.length; i += size) {
                var group = rows.slice(i, i + size);
                var durations = group.map(function (r) { return r.durationMs; });
                var failed = group.filter(function (r) { return !isOk(r); });
                // The call to inspect for a point: the slowest failed call, else the slowest call.
                var pool = failed.length ? failed : group;
                var rep = pool.reduce(function (a, b) { return b.durationMs > a.durationMs ? b : a; });
                out.push({
                    avg: avg(group, 'durationMs'),
                    p95: percentile(durations, 0.95),
                    errors: failed.length,
                    n: group.length,
                    rep: rep,
                    start: group[0].start
                });
            }
            return out;
        },
        get timeChart() {
            var pts = this.points;
            var W = this.chartW, H = 220, padL = 44, padR = 12, padT = 12, padB = 24;
            if (!pts.length) { return ''; }
            var grouped = this.groupSize > 1;
            var max = 1;
            pts.forEach(function (p) { max = Math.max(max, p.avg, grouped ? p.p95 : 0); });
            max *= 1.1;
            var innerW = W - padL - padR, innerH = H - padT - padB;
            var xOf = function (i) { return padL + (pts.length > 1 ? i / (pts.length - 1) : 0.5) * innerW; };
            var yOf = function (v) { return padT + innerH * (1 - v / max); };
            var svg = '<svg width="' + W + '" height="' + H + '" viewBox="0 0 ' + W + ' ' + H + '" class="v2in-chart">'
                    + '<defs><linearGradient id="am-fill" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="' + COLORS.blue + '" stop-opacity="0.28"></stop><stop offset="100%" stop-color="' + COLORS.blue + '" stop-opacity="0"></stop></linearGradient></defs>';
            [0, 0.5, 1].forEach(function (f) {
                var y = padT + innerH * (1 - f);
                svg += '<line x1="' + padL + '" x2="' + (W - padR) + '" y1="' + y.toFixed(1) + '" y2="' + y.toFixed(1) + '" class="v2in-gridline"></line>'
                        + '<text x="' + (padL - 6) + '" y="' + (y + 3).toFixed(1) + '" text-anchor="end" class="v2in-gridlabel">' + IN.esc(Math.round(max * f / 1.1)) + '</text>';
            });
            var line = function (key) {
                return pts.map(function (p, i) { return (i ? 'L' : 'M') + xOf(i).toFixed(1) + ' ' + yOf(p[key]).toFixed(1); }).join(' ');
            };
            if (pts.length > 1) {
                svg += '<path d="' + line('avg') + ' L' + xOf(pts.length - 1).toFixed(1) + ' ' + (padT + innerH) + ' L' + xOf(0).toFixed(1) + ' ' + (padT + innerH) + ' Z" fill="url(#am-fill)"></path>';
                if (grouped) { svg += '<path d="' + line('p95') + '" fill="none" stroke="' + COLORS.purple + '" stroke-width="1.5" stroke-opacity="0.8"></path>'; }
                svg += '<path d="' + line('avg') + '" fill="none" stroke="' + COLORS.blue + '" stroke-width="2.2" stroke-linejoin="round"></path>';
            }
            var sel = this.selected;
            pts.forEach(function (p, i) {
                var ko = p.errors > 0;
                var isSel = sel && this.pointHasCall(p, sel);
                if (isSel) {
                    svg += '<line x1="' + xOf(i).toFixed(1) + '" x2="' + xOf(i).toFixed(1) + '" y1="' + padT + '" y2="' + (padT + innerH) + '" stroke="' + COLORS.blue + '" stroke-opacity="0.4"></line>';
                }
                var title = this.fmtDate(p.start) + ' - ' + (grouped ? this.t('callscount', p.n) + ' - ' + this.t('average') + ' ' : '') + Math.round(p.avg) + ' ms'
                        + (grouped ? ' - ' + this.t('p95') + ' ' + Math.round(p.p95) + ' ms' : '') + (ko ? ' - ' + this.t('errors') + ': ' + p.errors : '');
                svg += '<circle cx="' + xOf(i).toFixed(1) + '" cy="' + yOf(p.avg).toFixed(1) + '" r="' + (isSel ? 5 : (ko ? 4 : 2.5)) + '" fill="' + (ko ? COLORS.ko : COLORS.ok)
                        + '" class="v2in-dot" data-id="' + p.rep.id + '"><title>' + IN.esc(title) + '</title></circle>';
            }, this);
            svg += '<text x="' + padL + '" y="' + (H - 6) + '" class="v2in-xlabel">' + IN.esc(this.fmtDate(pts[0].start)) + '</text>'
                    + '<text x="' + (W - padR) + '" y="' + (H - 6) + '" text-anchor="end" class="v2in-xlabel">' + IN.esc(this.fmtDate(this.rows[this.rows.length - 1].start)) + '</text>';
            return svg + '</svg>';
        },
        // Whether the selected call belongs to the group of calls drawn as that point.
        pointHasCall(p, call) {
            if (p.rep.id === call.id) { return true; }
            var size = this.groupSize;
            if (size === 1) { return false; }
            var rows = this.rows;
            var idx = rows.indexOf(call);
            var first = rows.indexOf(rows.find(function (r) { return r.start === p.start; }));
            return idx >= first && idx < first + size;
        },
        onChartClick(e) {
            var id = e.target && e.target.getAttribute && e.target.getAttribute('data-id');
            if (id) { this.selectedId = Number(id); }
        },
        get strip() {   // status strip: the last 60 calls
            return this.rows.slice(-60);
        },

        // ── Distribution, HTTP codes, consumers ──
        get distribution() {
            var rows = this.rows;
            var counts = RANGES.map(function () { return 0; });
            rows.forEach(function (r) {
                for (var i = 0; i < RANGES.length; i++) {
                    if (r.durationMs < RANGES[i].max) { counts[i]++; break; }
                }
            });
            var max = Math.max.apply(null, counts.concat([1]));
            return RANGES.map(function (range, i) {
                return {label: range.label, n: counts[i], pct: 100 * counts[i] / max, share: rows.length ? Math.round(100 * counts[i] / rows.length) : 0};
            });
        },
        get httpCodes() {
            var rows = this.rows, self = this;
            var counts = {};
            rows.forEach(function (r) {
                var k = r.httpCode > 0 ? String(Math.floor(r.httpCode / 100)) : '0';
                counts[k] = (counts[k] || 0) + 1;
            });
            return HTTP_CLASSES.filter(function (c) { return counts[c.key]; }).map(function (c) {
                return {key: c.key, label: c.label || self.t('nocode'), color: c.color, n: counts[c.key], pct: 100 * counts[c.key] / rows.length};
            });
        },
        get consumers() {
            var by = {};
            this.rows.forEach(function (r) {
                var k = (r.test || '-') + ' / ' + (r.testcase || '-');
                var c = by[k] || (by[k] = {name: k, n: 0, errors: 0, total: 0});
                c.n++; c.total += r.durationMs;
                if (!isOk(r)) { c.errors++; }
            });
            return Object.keys(by).map(function (k) {
                var c = by[k];
                return {name: c.name, n: c.n, errors: c.errors, rate: Math.round(100 * c.errors / c.n), avg: c.total / c.n};
            }).sort(function (a, b) { return b.errors - a.errors || b.n - a.n; }).slice(0, 8);
        },
        get latest() {
            return this.rows.slice(-20).reverse();
        }
    };
}