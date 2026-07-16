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
 * Shared helpers for the Insights trend pages (Campaign Trends, Execution Trends,
 * Real-time Monitor). Charts are built as full SVG strings and rendered with
 * x-html: <template x-for> does not work inside an inline <svg>.
 */
window.InsightsShared = (function () {

    var statusOrder = ['OK', 'KO', 'FA', 'NA', 'NE', 'WE', 'PE', 'QU', 'QE', 'PA', 'CA'];
    var statusColors = {
        OK: '#00d27a', KO: '#e63757', FA: '#f5803e', NA: '#94a3b8', NE: '#cbd5e1',
        WE: '#8b5cf6', PE: '#2c7be5', QU: '#60a5fa', QE: '#be123c', PA: '#f59e0b', CA: '#475569',
        FN: '#b45309'
    };
    // stable palette for multi-series lines (combination curves)
    var seriesPalette = ['#2c7be5', '#00d27a', '#8b5cf6', '#f5803e', '#e63757', '#0891b2', '#d946ef', '#84cc16', '#f59e0b', '#64748b'];

    function esc(v) {
        return String(v === undefined || v === null ? '' : v)
            .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
    }

    function statusColor(s) { return statusColors[s] || statusColors.NA; }

    function fmtDuration(ms) {
        if (ms === undefined || ms === null || ms < 0 || isNaN(ms)) return '-';
        if (ms === 0) return '0s';
        var s = Math.round(ms / 1000);
        if (s < 60) return s + 's';
        var m = Math.floor(s / 60);
        if (m < 60) return m + 'm ' + (s % 60) + 's';
        var h = Math.floor(m / 60);
        if (h < 48) return h + 'h ' + (m % 60) + 'm';
        return Math.floor(h / 24) + 'd ' + (h % 24) + 'h';
    }

    function fmtShortDate(v) {
        var d = new Date(v);
        if (isNaN(d.getTime())) return '-';
        var p = function (n) { return (n < 10 ? '0' : '') + n; };
        return p(d.getDate()) + '/' + p(d.getMonth() + 1);
    }

    function fmtDateTime(v) {
        var d = new Date(v);
        if (isNaN(d.getTime())) return '-';
        return d.toLocaleString();
    }

    function relTime(ts) {
        if (!ts) return '-';
        var t = typeof ts === 'number' ? ts : new Date(ts).getTime();
        var diff = Date.now() - t;
        if (isNaN(diff) || diff < 0) return '-';
        var s = Math.floor(diff / 1000);
        if (s < 60) return s + 's ago';
        var min = Math.floor(s / 60);
        if (min < 60) return min + ' min ago';
        var h = Math.floor(min / 60);
        if (h < 24) return h + 'h ago';
        var days = Math.floor(h / 24);
        if (days < 31) return days + 'd ago';
        return new Date(t).toLocaleDateString();
    }

    /**
     * Stacked bars chart -> svg string.
     * items: [{label, title, segments: [{status, value}], meta}] chronological.
     * opts: {W, H, onClickAttr: fn(item, index) -> extra svg attrs string (e.g. data-tag)}
     */
    function stackedBars(items, opts) {
        opts = opts || {};
        var W = opts.W || 720, H = opts.H || 200, padX = 36, padTop = 20, padBottom = 32;
        var innerW = W - padX * 2, innerH = H - padTop - padBottom;
        var n = items.length;
        var svg = '<svg viewBox="0 0 ' + W + ' ' + H + '" class="v2in-chart">';
        var baseline = padTop + innerH;
        var max = 1;
        items.forEach(function (it) {
            var t = 0;
            it.segments.forEach(function (s) { t += s.value; });
            it._total = t;
            if (t > max) max = t;
        });
        svg += '<line x1="30" x2="' + (W - 12) + '" y1="' + padTop + '" y2="' + padTop + '" class="v2in-gridline"></line>';
        svg += '<text x="26" y="' + (padTop + 3) + '" text-anchor="end" class="v2in-gridlabel">' + max + '</text>';
        svg += '<line x1="30" x2="' + (W - 12) + '" y1="' + baseline + '" y2="' + baseline + '" class="v2in-baseline"></line>';
        if (!n) return svg + '</svg>';
        var slot = innerW / n;
        var barW = Math.max(8, Math.min(42, slot * 0.6));
        var labelEvery = Math.max(1, Math.ceil(34 / slot));
        var showVals = slot >= 26;
        items.forEach(function (it, i) {
            var x = padX + i * slot + (slot - barW) / 2;
            var cx = x + barW / 2;
            var y = baseline;
            var extra = opts.onClickAttr ? opts.onClickAttr(it, i) : '';
            if (it._total > 0) {
                it.segments.forEach(function (s) {
                    if (!s.value) return;
                    var h = (s.value / max) * innerH;
                    y -= h;
                    svg += '<rect x="' + x.toFixed(1) + '" y="' + y.toFixed(1) + '" width="' + barW.toFixed(1) + '" height="' + h.toFixed(1)
                        + '" fill="' + statusColor(s.status) + '" rx="1.5" class="v2in-stackseg" ' + extra + '>'
                        + '<title>' + esc(it.title || it.label) + ' - ' + s.status + ': ' + s.value + '</title></rect>';
                });
                if (showVals) svg += '<text x="' + cx.toFixed(1) + '" y="' + (y - 5).toFixed(1) + '" text-anchor="middle" class="v2in-barval">' + it._total + '</text>';
            } else {
                svg += '<rect x="' + x.toFixed(1) + '" y="' + (baseline - 3) + '" width="' + barW.toFixed(1)
                    + '" height="3" rx="1.5" class="v2in-ghost"><title>' + esc(it.title || it.label) + ': nothing</title></rect>';
            }
            svg += '<line x1="' + cx.toFixed(1) + '" x2="' + cx.toFixed(1) + '" y1="' + baseline + '" y2="' + (baseline + 4) + '" class="v2in-tick"></line>';
            if ((n - 1 - i) % labelEvery === 0) {
                svg += '<text x="' + cx.toFixed(1) + '" y="' + (baseline + 16) + '" text-anchor="middle" class="v2in-xlabel">' + esc(it.label) + '</text>';
            }
        });
        return svg + '</svg>';
    }

    /**
     * Multi-series time line chart -> svg string.
     * series: [{name, color, points: [{t(ms), v, title, dotColor, attr}]}]
     * opts: {W, H, unit: 'duration'|'number', xDomain: [min,max] optional}
     */
    function timeLines(series, opts) {
        opts = opts || {};
        var W = opts.W || 720, H = opts.H || 230, padX = 46, padTop = 16, padBottom = 30;
        var innerW = W - padX * 2, innerH = H - padTop - padBottom;
        var svg = '<svg viewBox="0 0 ' + W + ' ' + H + '" class="v2in-chart">';
        var all = [];
        series.forEach(function (s) { s.points.forEach(function (p) { all.push(p); }); });
        if (!all.length) return svg + '</svg>';
        var minT = Infinity, maxT = -Infinity, maxV = 1;
        all.forEach(function (p) {
            if (p.t < minT) minT = p.t;
            if (p.t > maxT) maxT = p.t;
            if (p.v > maxV) maxV = p.v;
        });
        if (opts.xDomain) { minT = opts.xDomain[0]; maxT = opts.xDomain[1]; }
        if (maxT === minT) { maxT = minT + 1; }
        var fmtV = function (v) { return opts.unit === 'duration' ? fmtDuration(v) : String(v); };
        // y grid: 0 / mid / max
        [0, 0.5, 1].forEach(function (f) {
            var y = padTop + innerH * (1 - f);
            svg += '<line x1="' + (padX - 6) + '" x2="' + (W - 12) + '" y1="' + y.toFixed(1) + '" y2="' + y.toFixed(1) + '" class="v2in-gridline"></line>'
                + '<text x="' + (padX - 10) + '" y="' + (y + 3).toFixed(1) + '" text-anchor="end" class="v2in-gridlabel">' + esc(fmtV(Math.round(maxV * f))) + '</text>';
        });
        var xOf = function (t) { return padX + ((t - minT) / (maxT - minT)) * innerW; };
        var yOf = function (v) { return padTop + innerH * (1 - v / maxV); };
        // x labels: 4 evenly spread dates
        [0, 1 / 3, 2 / 3, 1].forEach(function (f) {
            var t = minT + (maxT - minT) * f;
            svg += '<text x="' + xOf(t).toFixed(1) + '" y="' + (H - 8) + '" text-anchor="middle" class="v2in-xlabel">' + fmtShortDate(t) + '</text>';
        });
        series.forEach(function (s) {
            var pts = s.points.slice().sort(function (a, b) { return a.t - b.t; });
            var path = [];
            pts.forEach(function (p) {
                path.push((path.length ? 'L' : 'M') + xOf(p.t).toFixed(1) + ' ' + yOf(p.v).toFixed(1));
            });
            if (path.length > 1) svg += '<path d="' + path.join(' ') + '" fill="none" stroke="' + s.color + '" stroke-width="2" stroke-linejoin="round" stroke-linecap="round" opacity="0.85"></path>';
            pts.forEach(function (p) {
                svg += '<circle cx="' + xOf(p.t).toFixed(1) + '" cy="' + yOf(p.v).toFixed(1) + '" r="4" fill="' + (p.dotColor || s.color)
                    + '" class="v2in-dot" ' + (p.attr || '') + '><title>' + esc(p.title || '') + '</title></circle>';
            });
        });
        return svg + '</svg>';
    }

    return {
        statusOrder: statusOrder,
        statusColors: statusColors,
        statusColor: statusColor,
        seriesPalette: seriesPalette,
        esc: esc,
        fmtDuration: fmtDuration,
        fmtShortDate: fmtShortDate,
        fmtDateTime: fmtDateTime,
        relTime: relTime,
        stackedBars: stackedBars,
        timeLines: timeLines
    };
})();
