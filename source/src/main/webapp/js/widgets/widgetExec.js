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
 * My Monitor - what the chart widgets (timeline, pie, radar) share: the executions of some test
 * cases over the period (ReadExecutionStat, like Execution Trends), the aggregates drawn from them
 * and the svg of the pie and the radar.
 *
 * An execution: {id, t, dur, status, country, environment, robot}. The ones still pending
 * have no duration and are not in the list: the counts per status come from statusDays.
 */
(function () {
    var MM = window.MyMonitor;
    var IN = MM.IN;

    // Object.assign would evaluate the getters once: this keeps them.
    MM.extend = function (target, source) {
        return Object.defineProperties(target, Object.getOwnPropertyDescriptors(source));
    };

    MM.normalizeExecutions = normalize;

    function normalize(data) {
        var exes = [];
        (data.datasetExeTime || []).forEach(function (curve) {
            var key = curve.key || {};
            var tc = key.testcase || {};
            (curve.points || []).forEach(function (p) {
                var t = new Date(p.x).getTime();
                if (isNaN(t)) { return; }
                exes.push({id: p.exe, t: t, dur: p.y || 0, status: p.exeControlStatus || '',
                    country: key.country || '', environment: key.environment || '', robot: key.robotdecli || '',
                    test: tc.test || '', testcase: tc.testcase || tc.testCase || '', application: key.application || tc.application || ''});
            });
        });
        exes.sort(function (a, b) { return a.t - b.t; });

        // One entry per day: {date, counts: {status: n}}. The dates come unsorted.
        var dates = (data.datasetExeStatusNbDates || []).slice();
        var days = {};
        dates.forEach(function (d) { days[d] = {}; });
        (data.datasetExeStatusNb || []).forEach(function (curve) {
            var st = String(curve.key && curve.key.key ? curve.key.key : curve.key);
            (curve.points || []).forEach(function (v, i) {
                if (v && dates[i] !== undefined) { days[dates[i]][st] = (days[dates[i]][st] || 0) + v; }
            });
        });
        var statusDays = Object.keys(days).sort().map(function (d) { return {date: d, counts: days[d]}; });
        return {exes: exes, statusDays: statusDays};
    }

    function sum(list, fn) { return list.reduce(function (s, x) { return s + fn(x); }, 0); }

    /** Executions grouped by day (UTC, like the server): [{t, date, exes}] chronological. */
    function perDay(exes) {
        var map = {};
        exes.forEach(function (e) {
            var d = new Date(e.t).toISOString().slice(0, 10);
            (map[d] = map[d] || {t: Date.parse(d), date: d, exes: []}).exes.push(e);
        });
        return Object.keys(map).sort().map(function (d) { return map[d]; });
    }

    /** Executions grouped by a dimension (country, environment, robot): [{label, exes}]. */
    function groupBy(exes, dim) {
        var map = {}, order = [];
        exes.forEach(function (e) {
            var k = e[dim] || '-';
            if (!map[k]) { map[k] = {label: k, exes: []}; order.push(k); }
            map[k].exes.push(e);
        });
        return order.sort().map(function (k) { return map[k]; });
    }

    function okRate(exes) {
        return exes.length ? Math.round(sum(exes, function (e) { return e.status === 'OK' ? 1 : 0; }) * 1000 / exes.length) / 10 : 0;
    }
    function avgDur(exes) { return exes.length ? Math.round(sum(exes, function (e) { return e.dur; }) / exes.length) : 0; }

    MM.agg = {perDay: perDay, groupBy: groupBy, okRate: okRate, avgDur: avgDur, sum: sum};

    /** Color of a slice / an axis: the status color, or the palette for the other dimensions. */
    MM.colorOf = function (dim, label, i) {
        return dim === 'status' ? IN.statusColor(label) : IN.seriesPalette[i % IN.seriesPalette.length];
    };

    // ── Charts ──
    function polar(cx, cy, r, a) { return [cx + r * Math.sin(a), cy - r * Math.cos(a)]; }

    /** Donut -> svg. slices: [{label, value, color, text}]. */
    MM.pieSvg = function (slices, W, H) {
        var total = sum(slices, function (s) { return s.value; });
        var svg = '<svg viewBox="0 0 ' + W + ' ' + H + '" class="v2in-chart">';
        var cx = W / 2, cy = H / 2, R = Math.max(20, Math.min(W, H) / 2 - 8), r = R * 0.58;
        if (!total) { return svg + '</svg>'; }
        var a0 = 0;
        slices.forEach(function (s) {
            if (!s.value) { return; }
            var span = s.value / total * Math.PI * 2;
            var title = '<title>' + IN.esc(s.label + ': ' + s.text + ' (' + Math.round(s.value * 1000 / total) / 10 + '%)') + '</title>';
            if (span >= Math.PI * 2 - 1e-6) {
                svg += '<circle cx="' + cx + '" cy="' + cy + '" r="' + ((R + r) / 2) + '" fill="none" stroke="' + s.color + '" stroke-width="' + (R - r) + '">' + title + '</circle>';
            } else {
                var a1 = a0 + span, big = span > Math.PI ? 1 : 0;
                var p0 = polar(cx, cy, R, a0), p1 = polar(cx, cy, R, a1), q1 = polar(cx, cy, r, a1), q0 = polar(cx, cy, r, a0);
                svg += '<path d="M' + p0[0].toFixed(1) + ' ' + p0[1].toFixed(1) + ' A' + R + ' ' + R + ' 0 ' + big + ' 1 ' + p1[0].toFixed(1) + ' ' + p1[1].toFixed(1)
                    + ' L' + q1[0].toFixed(1) + ' ' + q1[1].toFixed(1) + ' A' + r + ' ' + r + ' 0 ' + big + ' 0 ' + q0[0].toFixed(1) + ' ' + q0[1].toFixed(1)
                    + ' Z" fill="' + s.color + '" stroke="#fff" stroke-width="1.5">' + title + '</path>';
            }
            a0 += span;
        });
        return svg + '</svg>';
    };

    /** Radar -> svg. axes: [{label, value, text}]; the scale goes from 0 to max (given, or the biggest value). */
    MM.radarSvg = function (axes, W, H, color, max) {
        var svg = '<svg viewBox="0 0 ' + W + ' ' + H + '" class="v2in-chart">';
        var n = axes.length;
        if (n < 3) { return svg + '</svg>'; }
        var cx = W / 2, cy = H / 2, R = Math.max(20, Math.min(W / 2 - 60, H / 2 - 22));
        max = max || Math.max.apply(null, axes.map(function (a) { return a.value; })) || 1;
        [0.25, 0.5, 0.75, 1].forEach(function (f) {
            svg += '<polygon points="' + axes.map(function (a, i) { return polar(cx, cy, R * f, i * 2 * Math.PI / n).map(function (v) { return v.toFixed(1); }).join(','); }).join(' ')
                + '" fill="none" class="v2in-gridline"></polygon>';
        });
        axes.forEach(function (a, i) {
            var ang = i * 2 * Math.PI / n, e = polar(cx, cy, R, ang), l = polar(cx, cy, R + 10, ang);
            var anchor = Math.abs(l[0] - cx) < 4 ? 'middle' : (l[0] > cx ? 'start' : 'end');
            svg += '<line x1="' + cx + '" y1="' + cy + '" x2="' + e[0].toFixed(1) + '" y2="' + e[1].toFixed(1) + '" class="v2in-gridline"></line>'
                + '<text x="' + l[0].toFixed(1) + '" y="' + (l[1] + 3).toFixed(1) + '" text-anchor="' + anchor + '" class="v2in-xlabel">' + IN.esc(a.label) + '</text>';
        });
        var pts = axes.map(function (a, i) { return polar(cx, cy, R * Math.min(1, a.value / max), i * 2 * Math.PI / n); });
        svg += '<polygon points="' + pts.map(function (p) { return p[0].toFixed(1) + ',' + p[1].toFixed(1); }).join(' ') + '" fill="' + color + '" fill-opacity="0.22" stroke="' + color + '" stroke-width="2"></polygon>';
        pts.forEach(function (p, i) {
            svg += '<circle cx="' + p[0].toFixed(1) + '" cy="' + p[1].toFixed(1) + '" r="4" fill="' + color + '" class="v2in-dot"><title>' + IN.esc(axes[i].label + ': ' + axes[i].text) + '</title></circle>';
        });
        return svg + '</svg>';
    };
})();
