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
 * My Monitor - the data of the chart widgets (timeline, pie, radar). They are configured in four steps:
 *   w.source   the data: executions, testcases or applications
 *   w.metric   the information (count, okRate, avgDuration...)
 *   w.split    the field the data is split by (the slices, the axes, the curves)
 *   w.filters  the test folders / test cases to keep: [{test, testCase}]. Required by the
 *              executions (a list of test cases); optional for the test cases (test folders).
 *
 * MM.dataWidget(w, sources, extra) builds the component of such a widget. sources gives, per data,
 * its metrics and its splits: {executions: {metrics: [...], splits: [...]}, ...}. extra is what the
 * widget adds (its svg, its summary...), getters included.
 *
 * The records are the executions of the filters over the period, the test cases or the applications.
 */
(function () {
    var MM = window.MyMonitor;

    // Rows of the lists of the application, reduced to the fields a split or a metric can use.
    function rowsOf(source, data) {
        return (data.contentTable || []).map(function (r) {
            return source === 'testcases'
                ? {test: r.test, testcase: r.testcase, application: r.application, status: r.status, type: r.type, priority: r.priority,
                    system: r.system, implementer: r.implementer, t: new Date(r.dateCreated).getTime()}
                : {application: r.application || r.Application, type: r.type, system: r.system, subsystem: r.subsystem, deploytype: r.deploytype};
        });
    }

    MM.dataWidget = function (w, SOURCES, extra) {
        var base = {
            sources: Object.keys(SOURCES),
            records: [],
            statusDays: [],     // executions: the number per day and per status, pending ones included
            loading: false,
            error: '',
            box: {w: 560, h: 180},
            pendTest: '',
            pendCase: '',

            init() {
                // The first versions followed one test case and a dimension.
                if (!Array.isArray(w.filters)) { w.filters = w.test && w.testCase ? [{test: w.test, testCase: w.testCase}] : []; }
                if (!SOURCES[w.source]) { w.source = 'executions'; }
                if (!w.split) { w.split = w.dimension || SOURCES[w.source].splits[0]; }
                this.load();
                this.$watch('periodDays', () => this.load());
                this.$watch('refreshTick', () => this.load());
                this.$watch('cfg', (open) => { if (open) { this.loadTests(); } else { this.load(); } });
                this.$nextTick(() => MM.fitChart(this.$refs.chart, (width, height) => { this.box = {w: width, h: height}; }));
            },

            // ── Settings ──
            get def() { return SOURCES[w.source] || SOURCES.executions; },
            get metrics() { return this.def.metrics; },
            get splits() { return this.def.splits; },
            get metric() { return this.metrics.indexOf(w.metric) >= 0 ? w.metric : this.metrics[0]; },
            get split() { return this.splits.indexOf(w.split) >= 0 ? w.split : this.splits[0]; },
            get needsTestCase() { return w.source === 'executions'; },
            get hasFilters() { return w.source !== 'applications'; },
            sourceLabel(s) { return MM.t('source_' + s); },
            metricLabel(k) { return MM.t(k === 'count' ? 'count_' + w.source : 'metric_' + k); },
            splitLabel(k) { return MM.t('split_' + k); },
            pickSource(s) {
                w.source = s;
                w.metric = SOURCES[s].metrics[0];
                w.split = SOURCES[s].splits[0];
                w.filters = [];
            },
            pickPending(test) {
                this.pendTest = test;
                this.pendCase = '';
                this.loadTestCases(test);
            },
            canAdd() { return !!this.pendTest && (!this.needsTestCase || !!this.pendCase); },
            addFilter() {
                if (!this.canAdd()) { return; }
                var f = {test: this.pendTest, testCase: this.needsTestCase ? this.pendCase : ''};
                var exists = w.filters.some(function (x) { return x.test === f.test && x.testCase === f.testCase; });
                if (!exists) { w.filters.push(f); }
                this.pendTest = '';
                this.pendCase = '';
                this.testcases = [];
            },
            removeFilter(i) { w.filters.splice(i, 1); },
            filterLabel(f) { return f.testCase ? f.test + ' / ' + f.testCase : f.test; },

            // ── Data ──
            load() {
                var self = this, since = Date.now();
                this.error = '';
                if (!this.ready) { this.records = []; this.statusDays = []; return; }
                this.loading = true;
                var query = getUser().defaultSystemsQuery;
                var request;
                if (w.source === 'executions') {
                    request = MM.executionStats(w.filters, this.periodDays).then(function (data) {
                        var n = MM.normalizeExecutions(data);
                        self.statusDays = n.statusDays;
                        return n.exes;
                    });
                } else {
                    var list = w.source === 'testcases' ? 'ReadTestCase' : 'ReadApplication';
                    request = MM.cached(list + '/' + query, function () { return MM.getJSON(list, query); }).then(function (data) {
                        var rows = rowsOf(w.source, data);
                        var tests = w.filters.map(function (f) { return f.test; });
                        return w.source === 'testcases' && tests.length ? rows.filter(function (r) { return tests.indexOf(r.test) >= 0; }) : rows;
                    });
                }
                request.then(function (rows) {
                    self.records = rows;
                }).catch(function (e) {
                    self.error = e.message;
                    self.records = [];
                    self.statusDays = [];
                }).finally(function () { MM.endLoader(since, function () { self.loading = false; }); });
            },
            get ready() { return !this.needsTestCase || w.filters.length > 0; },
            get hasData() { return this.records.length > 0; },

            // Executions per status, pending ones included: [{label, value}].
            statusTotals() {
                var totals = {};
                this.statusDays.forEach(function (d) { Object.keys(d.counts).forEach(function (k) { totals[k] = (totals[k] || 0) + d.counts[k]; }); });
                return MM.IN.statusOrder.filter(function (s) { return totals[s]; }).map(function (s) { return {label: s, value: totals[s]}; });
            }
        };
        return MM.extend(MM.extend(MM.testCasePicker(w), base), extra);
    };
})();
