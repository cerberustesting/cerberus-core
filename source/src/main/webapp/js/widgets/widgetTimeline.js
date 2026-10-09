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
 * Widget "timeline": the data along the time, over the period. Configured like every chart widget
 * (see widgetData.js); the split makes one curve per value ("none": a single curve).
 * Metrics of the executions:
 *   duration     one dot per execution (click opens it); with no split, one curve per country / environment / robot
 *   avgDuration  the average duration per day
 *   status       the executions per day, stacked by status (no split)
 *   count        the number of executions per day
 *   okRate       the percentage of OK per day
 * Metric of the test cases: created, the number created per day.
 */
(function () {
    var MM = window.MyMonitor;
    var IN = MM.IN;

    var SOURCES = {
        executions: {metrics: ['duration', 'avgDuration', 'status', 'count', 'okRate'], splits: ['none', 'country', 'environment', 'robot', 'status', 'application', 'test', 'testcase']},
        testcases: {metrics: ['created'], splits: ['none', 'application', 'status', 'type', 'priority', 'system', 'test', 'implementer']}
    };

    MM.register({
        type: 'timeline', icon: 'chart-line', color: 'cyan', w: 6, h: 4, minW: 3, minH: 3,
        defaults: function () { return {source: 'executions', metric: 'duration', split: 'none', filters: []}; }
    });

    window.widgetTimeline = function (w) {
        return MM.dataWidget(w, SOURCES, {
            get isStatus() { return w.source === 'executions' && this.metric === 'status'; },
            get hasData() { return w.source === 'executions' ? this.statusTotals().length > 0 : this.records.length > 0; },
            get inPeriod() {
                var from = Date.now() - this.periodDays * 86400000;
                return this.records.filter(function (r) { return r.t >= from; });
            },

            get summary() {
                var m = this.metric, exes = this.records, n = exes.length;
                if (w.source === 'testcases') { return {big: MM.fmtNum(this.inPeriod.length), sub: MM.t('createdover', this.periodDays)}; }
                var total = MM.agg.sum(this.statusTotals(), function (s) { return s.value; });
                var ok = MM.agg.sum(this.statusTotals().filter(function (s) { return s.label === 'OK'; }), function (s) { return s.value; });
                if (m === 'duration' || m === 'avgDuration') {
                    return {big: IN.fmtDuration(MM.agg.avgDur(exes)), sub: MM.t('avgduration', n, IN.fmtDuration(n ? exes[n - 1].dur : null))};
                }
                if (m === 'count') { return {big: MM.fmtNum(total), sub: MM.t('executionsover', this.periodDays)}; }
                return {big: (total ? Math.round(ok * 1000 / total) / 10 : 0) + '%', sub: MM.t('okrate', total)};
            },
            get legend() {
                if (this.isStatus) { return this.statusTotals().map(function (s) { return {label: s.label, color: IN.statusColor(s.label)}; }); }
                return this.series.length > 1 ? this.series.map(function (s) { return {label: s.name, color: s.color}; }) : [];
            },

            // The groups of records that make one curve each.
            get groups() {
                var split = this.split, records = w.source === 'testcases' ? this.inPeriod : this.records;
                if (split !== 'none') { return MM.agg.groupBy(records, split).map(function (g) { return {name: g.label, exes: g.exes, key: g.label}; }); }
                if (w.source === 'executions' && this.metric === 'duration') {
                    var map = {}, list = [];
                    records.forEach(function (e) {
                        var name = [e.country, e.environment, e.robot].filter(Boolean).join(' / ') || e.testcase;
                        if (!map[name]) { map[name] = {name: name, exes: [], key: name}; list.push(map[name]); }
                        map[name].exes.push(e);
                    });
                    return list;
                }
                return [{name: this.metricLabel(this.metric), exes: records, key: ''}];
            },
            get series() {
                var m = this.metric, split = this.split, many = this.groups.length > 1;
                return this.groups.map(function (g, i) {
                    var color = split === 'status' ? IN.statusColor(g.key) : many ? IN.seriesPalette[i % IN.seriesPalette.length] : MM.COLORS.blue;
                    var points;
                    if (m === 'duration') {
                        points = g.exes.map(function (e) {
                            return {t: e.t, v: e.dur, dotColor: IN.statusColor(e.status), attr: 'data-exe="' + IN.esc(e.id) + '"',
                                title: g.name + ' - ' + IN.fmtDateTime(e.t) + ' - ' + e.status + ' - ' + IN.fmtDuration(e.dur)};
                        });
                    } else {
                        points = MM.agg.perDay(g.exes).map(function (d) {
                            var v = m === 'okRate' ? MM.agg.okRate(d.exes) : m === 'avgDuration' ? MM.agg.avgDur(d.exes) : d.exes.length;
                            return {t: d.t, v: v, title: g.name + ' - ' + d.date + ' - ' + (m === 'avgDuration' ? IN.fmtDuration(v) : m === 'okRate' ? v + '%' : v)};
                        });
                    }
                    return {name: g.name, color: color, points: points};
                });
            },
            get svg() {
                var W = this.box.w, H = this.box.h;
                if (this.isStatus) {
                    return IN.stackedBars(this.statusDays.map(function (d) {
                        return {label: IN.fmtShortDate(d.date), title: d.date, segments: Object.keys(d.counts).map(function (s) { return {status: s, value: d.counts[s]}; })};
                    }), {W: W, H: H});
                }
                var unit = this.metric === 'duration' || this.metric === 'avgDuration' ? 'duration' : 'number';
                return IN.timeLines(this.series, {W: W, H: H, unit: unit, xDomain: [Date.now() - this.periodDays * 86400000, Date.now()]});
            },
            openExecution(e) {
                var dot = e.target.closest('[data-exe]');
                if (dot) { window.open('./TestCaseExecutionV2.jsp?executionId=' + encodeURIComponent(dot.getAttribute('data-exe')), '_blank'); }
            }
        });
    };
})();
