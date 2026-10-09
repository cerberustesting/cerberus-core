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
 * Widget "pie": how the data splits. Configured like every chart widget (see widgetData.js).
 * The metric of the executions is their number or the time they took.
 */
(function () {
    var MM = window.MyMonitor;
    var IN = MM.IN;

    var SOURCES = {
        executions: {metrics: ['count', 'duration'], splits: ['status', 'country', 'environment', 'robot', 'application', 'test', 'testcase']},
        testcases: {metrics: ['count'], splits: ['application', 'status', 'type', 'priority', 'system', 'test', 'implementer']},
        applications: {metrics: ['count'], splits: ['type', 'system', 'subsystem', 'deploytype']}
    };

    MM.register({
        type: 'pie', icon: 'chart-pie', color: 'purple', w: 4, h: 4, minW: 3, minH: 3,
        defaults: function () { return {source: 'executions', metric: 'count', split: 'status', filters: []}; }
    });

    window.widgetPie = function (w) {
        return MM.dataWidget(w, SOURCES, {
            get byDuration() { return this.metric === 'duration'; },

            get slices() {
                var split = this.split, byDuration = this.byDuration, fmt = byDuration ? IN.fmtDuration : MM.fmtNum;
                var raw;
                if (w.source === 'executions' && split === 'status' && !byDuration) {
                    raw = this.statusTotals();   // the pending executions too
                } else {
                    raw = MM.agg.groupBy(this.records, split).map(function (g) {
                        return {label: g.label, value: byDuration ? MM.agg.sum(g.exes, function (e) { return e.dur; }) : g.exes.length};
                    });
                }
                return raw.map(function (s, i) { return {label: s.label, value: s.value, text: fmt(s.value), color: MM.colorOf(split, s.label, i)}; });
            },
            get hasData() { return this.slices.some(function (s) { return s.value > 0; }); },
            get sum() {
                var total = MM.agg.sum(this.slices, function (s) { return s.value; });
                return this.byDuration ? IN.fmtDuration(total) : MM.fmtNum(total);
            },
            get svg() { return MM.pieSvg(this.slices, this.box.w, this.box.h); }
        });
    };
})();
