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
 * Execution Trends - Alpine dashboard over ReadExecutionStat.
 *
 * Pick one or several test cases and follow them over time: duration of every
 * execution (dots colored by status, click opens the execution), executions per
 * day stacked by status, and the plain list of the executions of the period.
 */
function executionTrends() {
    var IN = window.InsightsShared;
    return {
        // ── State ──
        loading: false,
        loaded: false,
        error: '',
        raw: null,

        // ── Test case picker ──
        tests: [],             // test folders
        testcasesOfTest: [],   // test cases of the browsed folder
        browsedTest: '',
        selTestcases: [],      // [{test, testCase}]
        tcDdOpen: false,
        tcSearch: '',

        // ── Filters ──
        periodDays: 30,
        activeCountries: {},
        activeEnvs: {},
        activeRobots: {},
        activeStatuses: {},

        // ── Data ──
        durationSeries: [],
        executions: [],        // flat list: {t, exeId, status, durMs, serie}
        statusPerDay: [],      // stacked bars items

        IN: IN,

        // ═══════════════════ INIT ═══════════════════
        init() {
            try {
                var saved = JSON.parse(localStorage.getItem('et.filters') || 'null');
                if (saved && typeof saved === 'object') {
                    if ([7, 30, 90, 180].indexOf(saved.periodDays) >= 0) this.periodDays = saved.periodDays;
                    if (Array.isArray(saved.selTestcases)) {
                        this.selTestcases = saved.selTestcases.filter(function (tc) {
                            return tc && typeof tc.test === 'string' && typeof tc.testCase === 'string';
                        });
                    }
                }
            } catch (e) { /* defaults */ }
            // deep link: ?tests=X&testcases=Y (repeated, paired)
            try {
                var usp = new URLSearchParams(window.location.search);
                var ts = usp.getAll('tests'), tcs = usp.getAll('testcases');
                if (ts.length && ts.length === tcs.length) {
                    this.selTestcases = ts.map(function (t, i) { return { test: t, testCase: tcs[i] }; });
                }
            } catch (e) { /* ignore */ }
            this._loadTests();
            this._buildTable();
            if (this.selTestcases.length) this.load();
        },

        /**
         * The executions list is the shared V2 table (js/global/crbTable.js) in
         * CLIENT mode: its rows are derived in the browser from the duration curves,
         * so there is no endpoint to page against. EMBEDDED, because the card and its
         * titled head already exist on the page - the component adds no frame.
         * Built once; load() pushes each new dataset with crbTableSetRows().
         */
        _buildTable() {
            var self = this;
            createCerberusTable({
                id: 'etExecutionsTable',
                mount: '#etExecutionsTableMount',
                clientRows: [],
                embedded: true,
                onRefresh: function () { self.load(); },
                pageLength: 15,
                lengthMenu: [15, 25, 50, 100, 200],
                searchPlaceholder: 'Search executions...',
                emptyMessage: 'No execution on this period',
                rowKey: 'exeId',
                defaultSort: {field: 't', dir: 'desc'},
                persistColumns: true,
                columns: [
                    {
                        field: 't', title: 'Date', width: '180px',
                        render: function (row) { return crbTableEscape(self.fmtDateTime(row.t)); }
                    },
                    {
                        field: 'testCase', title: 'Test case', width: '240px',
                        render: function (row) {
                            return '<div class="v2in-dim text-xs">' + crbTableEscape(row.test) + '</div>' +
                                '<div class="v2in-strong">' + crbTableEscape(row.testCase) + '</div>';
                        }
                    },
                    {field: 'test', title: 'Test folder', width: '200px', visible: false, filterable: true},
                    {field: 'environment', title: 'Environment', width: '130px', filterable: true},
                    {field: 'country', title: 'Country', width: '110px', filterable: true},
                    {
                        field: 'status', title: 'Status', width: '130px', filterable: true,
                        render: function (row) {
                            var c = self.statusColor(row.status);
                            return '<span class="v2in-chip" style="background: color-mix(in srgb, ' +
                                crbTableEscape(c) + ' 14%, transparent); color:' + crbTableEscape(c) + '">' +
                                crbTableEscape(row.status + (row.fn ? ' (FN)' : '')) + '</span>';
                        }
                    },
                    {
                        field: 'durMs', title: 'Duration', width: '130px',
                        className: 'text-right tabular-nums',
                        render: function (row) { return crbTableEscape(self.fmtDuration(row.durMs)); }
                    },
                    {field: 'exeId', title: 'Execution id', width: '130px', visible: false,
                     className: 'font-mono text-right tabular-nums'},
                    {field: 'serie', title: 'Serie', width: '260px', visible: false}
                ],
                actions: [
                    {
                        key: 'open', icon: 'external-link', gate: 'always',
                        title: 'Open the execution',
                        onClick: function (row) { self.openExe(row.exeId); }
                    }
                ]
            });
        },
        _loadTests() {
            var self = this;
            $.getJSON('ReadTest', function (data) {
                self.tests = (data.contentTable || []).map(function (t) { return t.test; }).filter(Boolean).sort();
            });
        },
        browseTest(test) {
            var self = this;
            this.browsedTest = test;
            this.testcasesOfTest = [];
            if (!test) return;
            $.getJSON('ReadTestCase', 'test=' + encodeURIComponent(test), function (data) {
                self.testcasesOfTest = (data.contentTable || []).map(function (tc) {
                    return { test: test, testCase: tc.testcase || tc.testCase, description: tc.description || '' };
                });
            });
        },
        get filteredTests() {
            var q = this.tcSearch.trim().toLowerCase();
            if (!q) return this.tests;
            return this.tests.filter(function (t) { return t.toLowerCase().indexOf(q) >= 0; });
        },
        get filteredTestcases() {
            var q = this.tcSearch.trim().toLowerCase();
            if (!q) return this.testcasesOfTest;
            return this.testcasesOfTest.filter(function (tc) {
                return tc.testCase.toLowerCase().indexOf(q) >= 0 || tc.description.toLowerCase().indexOf(q) >= 0;
            });
        },
        isSelected(tc) {
            return this.selTestcases.some(function (s) { return s.test === tc.test && s.testCase === tc.testCase; });
        },
        toggleTestcase(tc) {
            var i = this.selTestcases.findIndex(function (s) { return s.test === tc.test && s.testCase === tc.testCase; });
            if (i >= 0) this.selTestcases.splice(i, 1);
            else this.selTestcases.push({ test: tc.test, testCase: tc.testCase });
        },
        removeSelected(i) { this.selTestcases.splice(i, 1); },

        // ═══════════════════ LOAD ═══════════════════
        load() {
            var self = this;
            if (!this.selTestcases.length) {
                showMessageMainPage('warning', 'Pick at least one test case first.');
                return;
            }
            this.loading = true;
            this.error = '';
            localStorage.setItem('et.filters', JSON.stringify({ periodDays: this.periodDays, selTestcases: this.selTestcases }));

            var to = new Date();
            var from = new Date(Date.now() - this.periodDays * 86400000);
            var params = ['from=' + encodeURIComponent(from.toISOString()), 'to=' + encodeURIComponent(to.toISOString())];
            var urlParams = [];
            this.selTestcases.forEach(function (tc) {
                params.push('tests=' + encodeURIComponent(tc.test));
                params.push('testcases=' + encodeURIComponent(tc.testCase));
                urlParams.push('tests=' + encodeURIComponent(tc.test));
                urlParams.push('testcases=' + encodeURIComponent(tc.testCase));
            });
            var pushOff = function (map, name) {
                Object.keys(map).forEach(function (k) { if (map[k]) params.push(name + '=' + encodeURIComponent(k)); });
            };
            var offIn = function (map) { return Object.keys(map).some(function (k) { return !map[k]; }); };
            if (offIn(this.activeCountries)) pushOff(this.activeCountries, 'countries');
            if (offIn(this.activeEnvs)) pushOff(this.activeEnvs, 'environments');
            if (offIn(this.activeRobots)) pushOff(this.activeRobots, 'robotDeclis');
            if (offIn(this.activeStatuses)) pushOff(this.activeStatuses, 'controlStatuss');

            InsertURLInHistory('./ReportingExecutionOverTime.jsp?' + urlParams.join('&'));

            $.getJSON('ReadExecutionStat?' + params.join('&'), function (data) {
                self.loading = false;
                if (!data || data.messageType !== 'OK') {
                    self.error = (data && data.message) || 'Could not load the execution statistics.';
                    self.loaded = false;
                    return;
                }
                self._apply(data);
            }).fail(function (xhr) {
                self.loading = false;
                self.error = 'Could not load the execution statistics (' + xhr.status + ').';
                self.loaded = false;
            });
        },
        toggleIn(map, k) { map[k] = !map[k]; this.load(); },

        // ═══════════════════ MAPPING ═══════════════════
        _apply(data) {
            var self = this;
            this.raw = data;

            // distinct entries are {hasData, name, isRequested}
            var syncMap = function (map, list) {
                (list || []).forEach(function (e) {
                    var k = typeof e === 'string' ? e : e.name;
                    if (!k) return;
                    if (map[k] === undefined) map[k] = true;
                });
            };
            var dist = data.distinct || {};
            syncMap(this.activeCountries, dist.countries);
            syncMap(this.activeEnvs, dist.environments);
            syncMap(this.activeRobots, dist.robotDeclis);
            syncMap(this.activeStatuses, dist.controlStatuss);

            // Duration curves: one serie per testcase x combination
            this.durationSeries = [];
            this.executions = [];
            (data.datasetExeTime || []).forEach(function (curve, ci) {
                var key = curve.key || {};
                var tcId = (key.testcase && (key.testcase.testcase || key.testcase.testCase)) || '';
                var serieName = [tcId, key.country, key.environment, key.robotdecli].filter(Boolean).join(' / ');
                // key.key is the server-side composite (test|testcase|country|env|robot|), unique per curve
                var serie = { id: key.key || ('serie-' + ci), name: serieName || ('serie ' + (ci + 1)), color: IN.seriesPalette[ci % IN.seriesPalette.length], points: [] };
                (curve.points || []).forEach(function (p) {
                    var t = new Date(p.x).getTime();
                    if (isNaN(t)) return;
                    var st = p.exeControlStatus || '';
                    serie.points.push({
                        t: t, v: p.y || 0,
                        title: serieName + ' - ' + IN.fmtDateTime(t) + ' - ' + st + ' - ' + IN.fmtDuration(p.y || 0),
                        dotColor: IN.statusColor(st),
                        attr: 'data-exe="' + p.exe + '"'
                    });
                    self.executions.push({
                        t: t, exeId: p.exe, status: st, fn: !!p.falseNegative,
                        durMs: p.y || 0, serie: serieName,
                        test: key.testcase ? key.testcase.test : '', testCase: tcId,
                        country: key.country || '', environment: key.environment || ''
                    });
                });
                if (serie.points.length) self.durationSeries.push(serie);
            });
            this.executions.sort(function (a, b) { return b.t - a.t; });
            // The array stays (the chart tooltips and the count in the card head read
            // it); the V2 table gets the same rows.
            crbTableSetRows('etExecutionsTable', this.executions);

            // Executions per day, stacked by status (dates come unsorted from the server)
            var dates = (data.datasetExeStatusNbDates || []).slice();
            var perDay = {};
            dates.forEach(function (d) { perDay[d] = []; });
            (data.datasetExeStatusNb || []).forEach(function (curve) {
                var st = curve.key && curve.key.key ? curve.key.key : curve.key;
                (curve.points || []).forEach(function (v, i) {
                    if (!v || dates[i] === undefined) return;
                    perDay[dates[i]].push({ status: String(st), value: v });
                });
            });
            this.statusPerDay = Object.keys(perDay).sort().map(function (d) {
                return { label: IN.fmtShortDate(d), title: d, segments: perDay[d] };
            });

            this.loaded = true;
        },

        // ═══════════════════ COMPUTED ═══════════════════
        get kpis() {
            var n = this.executions.length;
            var ok = 0, flakyish = 0, durSum = 0, durMax = 0;
            this.executions.forEach(function (e) {
                if (e.status === 'OK') ok++;
                if (e.fn) flakyish++;
                durSum += e.durMs;
                if (e.durMs > durMax) durMax = e.durMs;
            });
            return {
                total: n,
                okRate: n ? Math.round(ok * 100 / n) : null,
                avgDur: n ? Math.round(durSum / n) : null,
                maxDur: n ? durMax : null,
                fn: flakyish
            };
        },
        get durationSvg() {
            return this.IN.timeLines(this.durationSeries, { W: 900, H: 240, unit: 'duration' });
        },
        get perDaySvg() {
            return this.IN.stackedBars(this.statusPerDay, { W: 900, H: 200 });
        },
        get legendStatuses() {
            var present = {};
            this.executions.forEach(function (e) { present[e.status] = true; });
            return this.IN.statusOrder.filter(function (s) { return present[s]; });
        },

        // ═══════════════════ ACTIONS ═══════════════════
        openExe(id) {
            if (id) window.open('./TestCaseExecutionV2.jsp?executionId=' + encodeURIComponent(id), '_blank');
        },
        chartClick(ev) {
            var el = ev.target.closest('[data-exe]');
            if (el) this.openExe(el.getAttribute('data-exe'));
        },

        // ═══════════════════ HELPERS ═══════════════════
        fmtDuration(ms) { return this.IN.fmtDuration(ms); },
        fmtDateTime(v) { return this.IN.fmtDateTime(v); },
        statusColor(s) { return this.IN.statusColor(s); }
    };
}
