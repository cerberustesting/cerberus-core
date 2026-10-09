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
 * Widget "radar": one axis per value of a split, to compare them on one figure (3 axes at least).
 * Configured like every chart widget (see widgetData.js).
 */
(function () {
    var MM = window.MyMonitor;
    var IN = MM.IN;

    var SOURCES = {
        executions: {metrics: ['count', 'okRate', 'avgDuration'], splits: ['country', 'environment', 'robot', 'status', 'application', 'test', 'testcase']},
        testcases: {metrics: ['count'], splits: ['application', 'status', 'type', 'priority', 'system', 'test', 'implementer']},
        applications: {metrics: ['count'], splits: ['type', 'system', 'subsystem', 'deploytype']}
    };

    MM.register({
        type: 'radar', icon: 'radar', color: 'orange', w: 4, h: 4, minW: 3, minH: 3,
        defaults: function () { return {source: 'executions', metric: 'okRate', split: 'country', filters: []}; }
    });

    window.widgetRadar = function (w) {
        return MM.dataWidget(w, SOURCES, {
            get axes() {
                var m = this.metric;
                return MM.agg.groupBy(this.records, this.split).map(function (g) {
                    var v = m === 'okRate' ? MM.agg.okRate(g.exes) : m === 'avgDuration' ? MM.agg.avgDur(g.exes) : g.exes.length;
                    return {label: g.label, value: v, text: m === 'avgDuration' ? IN.fmtDuration(v) : m === 'okRate' ? v + '%' : String(v)};
                });
            },
            get enough() { return this.axes.length >= 3; },
            get svg() { return MM.radarSvg(this.axes, this.box.w, this.box.h, MM.COLORS.orange, this.metric === 'okRate' ? 100 : 0); }
        });
    };
})();
