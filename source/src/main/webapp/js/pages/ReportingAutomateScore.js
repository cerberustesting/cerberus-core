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
 * Automate Score - Alpine dashboard.
 *
 * Data comes from api/automatescore/statistics which scores every week on 4 axes
 * (each 0-25): run frequency, campaign duration, stability (flaky + false negative
 * rate) and maintenance effort. The global score (0-100) is the sum of the four.
 * The current week is never fully scored (incomplete): the hero shows the last full week.
 */
function automateScore() {
    return {
        // ── State ──
        loading: false,
        loaded: false,
        error: '',
        raw: null,

        // ── Filters ──
        systems: [],
        selSystems: [],
        campaigns: [],
        selCampaign: '',
        nbWeeks: 8,
        sysDdOpen: false,
        sysSearch: '',
        campDdOpen: false,
        campSearch: '',

        // ── Data ──
        weeks: [],             // [{val: 'yyyy-ww', label: 'Wnn'}] oldest first
        weekStats: {},         // week -> {kpiFrequency, kpiDuration, kpiStability, kpiMaintenance, kpi}
        campaignRows: [],
        testcaseRows: [],
        leaderboard: [],
        kpis: [],              // the 4 evaluated axes of the last full week
        scoreGlobal: null,     // {score, letter, weekLabel, delta}
        trendPoints: [],       // [{label, score, letter}]
        evaluatedIdx: -1,      // index of the evaluated week inside this.weeks

        letterColors: { A: '#00d27a', B: '#84cc16', C: '#f59e0b', D: '#f5803e', E: '#e63757', NA: '#94a3b8' },
        kpiMeta: {
            kpiFrequency: {
                key: 'kpiFrequency', label: 'Run frequency', unit: 'runs / campaign / week',
                target: 'A from 7 runs per campaign per week',
                tip: 'schedule your campaigns to run at least daily',
                help: 'Average number of campaign executions per campaign each week. Running often is the heart of continuous testing: 7+ per week scores A, none scores 0.'
            },
            kpiDuration: {
                key: 'kpiDuration', label: 'Campaign duration', unit: 'average per campaign run',
                target: 'A under 45 minutes per run',
                tip: 'split long campaigns or parallelize on more robots',
                help: 'Average campaign duration. Fast feedback loops score best: under 45 minutes is an A, over 16 hours scores 0.'
            },
            kpiStability: {
                key: 'kpiStability', label: 'Stability', unit: 'flaky + false negative rate',
                target: 'A under 1% unstable executions',
                tip: 'fix or mute the flakiest test cases first',
                help: 'Share of executions that were flaky or declared false negative. Below 1% scores A: trustable results teams can act on.'
            },
            kpiMaintenance: {
                key: 'kpiMaintenance', label: 'Maintenance effort', unit: 'time spent on scripts / week',
                target: 'A under 45 minutes per week',
                tip: 'factorize shared steps into libraries to lower the maintenance cost',
                help: 'Estimated time spent editing test cases during the week. Autonomous, low-maintenance assets score best: under 45 minutes is an A.'
            }
        },

        // ═══════════════════ INIT / LOAD ═══════════════════
        init() {
            var user = getUser() || {};
            this.systems = (user.system || []).slice().sort();
            this.selSystems = (user.defaultSystems || []).slice();
            try {
                var saved = JSON.parse(localStorage.getItem('as.filters') || 'null');
                if (saved && typeof saved === 'object') {
                    if ([4, 8, 12, 26].indexOf(saved.nbWeeks) >= 0) this.nbWeeks = saved.nbWeeks;
                    if (typeof saved.selCampaign === 'string') this.selCampaign = saved.selCampaign;
                }
            } catch (e) { /* defaults */ }
            this._loadCampaigns();
            this.load();
        },
        _loadCampaigns() {
            var self = this;
            $.getJSON('ReadCampaign?iDisplayStart=0&iDisplayLength=300&iSortCol_0=1&sSortDir_0=asc', function (data) {
                self.campaigns = (data.contentTable || []).map(function (c) { return c.campaign; }).filter(Boolean).sort();
            });
        },
        load() {
            var self = this;
            this.loading = true;
            this.error = '';
            localStorage.setItem('as.filters', JSON.stringify({ nbWeeks: this.nbWeeks, selCampaign: this.selCampaign }));

            var params = [];
            this.selSystems.forEach(function (s) { params.push('systems=' + encodeURIComponent(s)); });
            if (this.selCampaign) params.push('campaigns=' + encodeURIComponent(this.selCampaign));
            params.push('to=' + encodeURIComponent(new Date().toISOString()));
            params.push('nbWeeks=' + this.nbWeeks);

            $.getJSON('api/automatescore/statistics?' + params.join('&'), function (data) {
                self.loading = false;
                if (!data || !data.weeks) {
                    self.error = (data && data.message) || 'Could not load the automate score statistics.';
                    self.loaded = false;
                    return;
                }
                self._apply(data);
            }).fail(function (xhr) {
                self.loading = false;
                self.error = 'Could not load the automate score statistics (' + xhr.status + ').';
                self.loaded = false;
            });
        },
        toggleSystem(s) {
            var i = this.selSystems.indexOf(s);
            if (i >= 0) this.selSystems.splice(i, 1);
            else this.selSystems.push(s);
        },
        allSystems(v) { this.selSystems = v ? this.systems.slice() : []; },
        get filteredSystems() {
            var q = this.sysSearch.trim().toLowerCase();
            if (!q) return this.systems;
            return this.systems.filter(function (s) { return s.toLowerCase().indexOf(q) >= 0; });
        },
        get filteredCampaigns() {
            var q = this.campSearch.trim().toLowerCase();
            if (!q) return this.campaigns;
            return this.campaigns.filter(function (c) { return c.toLowerCase().indexOf(q) >= 0; });
        },
        pickCampaign(c) {
            this.selCampaign = c;
            this.campDdOpen = false;
        },

        // ═══════════════════ MAPPING ═══════════════════
        _apply(data) {
            var self = this;
            this.raw = data;
            this.weeks = data.weeks || [];
            this.weekStats = data.weekStats || {};

            // Score history (the current week may be unscored: score stays null)
            this.trendPoints = this.weeks.map(function (w) {
                var st = self.weekStats[w.val] || {};
                var kpi = st.kpi || {};
                return {
                    week: w.val,
                    label: w.label,
                    score: (kpi.score !== undefined && kpi.score !== null) ? kpi.score : null,
                    letter: kpi.scoreL || 'NA'
                };
            });

            // Last fully evaluated week drives the hero + kpi cards. The last entry is the
            // week of 'to' (= now): always in progress and only partially scored, skip it.
            var evaluated = null;
            this.evaluatedIdx = -1;
            for (var i = this.trendPoints.length - 2; i >= 0; i--) {
                if (this.trendPoints[i].score !== null) { evaluated = this.trendPoints[i]; this.evaluatedIdx = i; break; }
            }
            var prevScore = null;
            var prevIdx = -1;
            if (evaluated) {
                for (var j = this.evaluatedIdx - 1; j >= 0; j--) {
                    if (this.trendPoints[j].score !== null) { prevScore = this.trendPoints[j].score; prevIdx = j; break; }
                }
            }
            this.scoreGlobal = evaluated ? {
                score: evaluated.score,
                letter: evaluated.letter,
                weekLabel: evaluated.label,
                delta: prevScore !== null ? evaluated.score - prevScore : null
            } : null;

            // The 4 axes of the evaluated week, with their weekly history for sparklines
            this.kpis = [];
            if (evaluated) {
                var st = this.weekStats[evaluated.week] || {};
                var stPrev = prevIdx >= 0 ? (this.weekStats[this.weeks[prevIdx].val] || {}) : {};
                ['kpiFrequency', 'kpiDuration', 'kpiStability', 'kpiMaintenance'].forEach(function (k) {
                    var kpi = st[k] || {};
                    var kpiPrev = stPrev[k] || {};
                    var meta = self.kpiMeta[k];
                    var score = (kpi.score !== undefined && kpi.score !== null) ? kpi.score : null;
                    var scorePrev = (kpiPrev.score !== undefined && kpiPrev.score !== null) ? kpiPrev.score : null;
                    self.kpis.push({
                        key: k,
                        label: meta.label,
                        unit: meta.unit,
                        help: meta.help,
                        target: meta.target,
                        value: kpi.value,
                        valueF: self._fmtKpiValue(k, kpi.value),
                        score: score,
                        scoreDelta: (score !== null && scorePrev !== null) ? score - scorePrev : null,
                        letter: kpi.scoreL || 'NA',
                        trend: kpi.trend || 'NA',
                        history: self.weeks.map(function (w) {
                            var wst = self.weekStats[w.val] || {};
                            var wk = wst[k] || {};
                            return (wk.score !== undefined && wk.score !== null) ? wk.score : null;
                        })
                    });
                });
            }

            // Campaign table (average behaviour per campaign over the period)
            this.campaignRows = (data.campaigns || []).slice().sort(function (a, b) { return (b.nb || 0) - (a.nb || 0); });

            // Test cases needing attention first (flaky or false negative), then most executed
            this.testcaseRows = (data.testcases || []).slice().sort(function (a, b) {
                var pa = (a.nbFlaky || 0) + (a.nbFN || 0);
                var pb = (b.nbFlaky || 0) + (b.nbFN || 0);
                if (pb !== pa) return pb - pa;
                return (b.nb || 0) - (a.nb || 0);
            });

            // Leaderboard: aggregate the per-user-per-week maintenance activity.
            // Each "user | week" entry holds an ARRAY of work sessions (changes grouped
            // by time proximity, see AutomateScoreService CHANGE_HORIZON).
            var users = {};
            var userWeeks = data['debug-userWeeks'] || {};
            Object.keys(userWeeks).forEach(function (key) {
                var sessions = userWeeks[key];
                if (!Array.isArray(sessions)) sessions = [sessions];
                var weekKey = key.split(' | ')[1] || '';
                var weekUser = '';
                sessions.forEach(function (uw) {
                    var name = uw.user || '';
                    if (!name) return;
                    weekUser = name;
                    if (!users[name]) users[name] = { user: name, saves: 0, timeMs: 0, weeksActive: 0, lastActive: 0, byWeek: {} };
                    users[name].saves += uw.nb || 0;
                    users[name].timeMs += uw.duration || 0;
                    users[name].byWeek[weekKey] = (users[name].byWeek[weekKey] || 0) + (uw.nb || 0);
                    if (uw.dateEndl && uw.dateEndl > users[name].lastActive) users[name].lastActive = uw.dateEndl;
                });
                if (weekUser && users[weekUser]) users[weekUser].weeksActive++;
            });
            var list = Object.values(users);
            list.forEach(function (u) {
                // Impact: 10 pts per saved change + 15 pts per active week (regularity counts)
                u.impact = u.saves * 10 + u.weeksActive * 15;
                u.initials = u.user.slice(0, 2).toUpperCase();
                u.history = self.weeks.map(function (w) { return u.byWeek[w.val] || 0; });
                var n = u.history.length;
                u.weekDelta = n > 1 ? (u.history[n - 1] - u.history[n - 2]) : 0;
            });
            list.sort(function (a, b) { return b.impact - a.impact; });
            var maxSaves = Math.max.apply(null, list.map(function (u) { return u.saves; }).concat([0]));
            var maxTime = Math.max.apply(null, list.map(function (u) { return u.timeMs; }).concat([0]));
            var maxWeeks = Math.max.apply(null, list.map(function (u) { return u.weeksActive; }).concat([0]));
            list.forEach(function (u) {
                u.badges = [];
                if (u.saves === maxSaves && maxSaves > 0) u.badges.push({ type: 'maintainer', label: 'Top maintainer' });
                if (u.timeMs === maxTime && maxTime > 0 && u.badges.length === 0) u.badges.push({ type: 'time', label: 'Deep worker' });
                if (u.weeksActive === maxWeeks && maxWeeks > 1 && u.badges.length === 0) u.badges.push({ type: 'regular', label: 'Most regular' });
            });
            this.leaderboard = list;

            this.loaded = true;
            this.$nextTick(function () { if (window.lucide) lucide.createIcons(); });
        },
        _fmtKpiValue(key, value) {
            if (value === undefined || value === null) return '-';
            if (key === 'kpiFrequency') return String(value);
            if (key === 'kpiStability') return (value / 100).toFixed(1) + '%';
            return this.fmtDuration(value); // durations
        },

        // ═══════════════════ INSIGHTS ═══════════════════
        // A few automatically generated observations: weakest/strongest axis, biggest
        // move vs the previous evaluated week, flaky hotspot and volume variation.
        get insights() {
            var self = this;
            var out = [];
            if (!this.scoreGlobal) {
                out.push({ level: 'info', text: 'No fully evaluated week on this period yet: run campaigns during a full week to get a score.' });
                return out;
            }
            var scored = this.kpis.filter(function (k) { return k.score !== null; });
            if (scored.length) {
                var weakest = scored.reduce(function (a, b) { return b.score < a.score ? b : a; });
                var strongest = scored.reduce(function (a, b) { return b.score > a.score ? b : a; });
                if (weakest.score < 25) {
                    out.push({ level: 'warn', text: 'Weakest axis: ' + weakest.label + ' (' + weakest.score + '/25) - ' + self.kpiMeta[weakest.key].tip + '.' });
                }
                if (strongest.score >= 20 && strongest.key !== weakest.key) {
                    out.push({ level: 'good', text: 'Strongest axis: ' + strongest.label + ' (' + strongest.score + '/25). Keep it up.' });
                }
                var movers = scored.filter(function (k) { return k.scoreDelta !== null && k.scoreDelta !== 0; });
                if (movers.length) {
                    var mover = movers.reduce(function (a, b) { return Math.abs(b.scoreDelta) > Math.abs(a.scoreDelta) ? b : a; });
                    out.push({
                        level: mover.scoreDelta > 0 ? 'good' : 'warn',
                        text: mover.label + (mover.scoreDelta > 0 ? ' gained ' : ' lost ') + Math.abs(mover.scoreDelta) + ' pts vs the previous evaluated week.'
                    });
                }
            }
            var hotspot = this.testcaseRows.length ? this.testcaseRows[0] : null;
            if (hotspot && ((hotspot.nbFlaky || 0) + (hotspot.nbFN || 0)) > 0) {
                out.push({
                    level: 'warn',
                    text: 'Hotspot: ' + hotspot.testcaseId + ' caused ' + ((hotspot.nbFlaky || 0) + (hotspot.nbFN || 0)) + ' unstable result(s) on the period - it is listed below.'
                });
            }
            var vol = this.volumeChart.bars;
            if (this.evaluatedIdx > 0 && vol.length > this.evaluatedIdx) {
                var cur = vol[this.evaluatedIdx].value, prev = vol[this.evaluatedIdx - 1].value;
                if (prev > 0 && cur !== prev) {
                    var pct = Math.round((cur - prev) * 100 / prev);
                    out.push({ level: 'info', text: 'Execution volume ' + (pct > 0 ? '+' : '') + pct + '% on week ' + this.scoreGlobal.weekLabel + ' (' + cur + ' vs ' + prev + ').' });
                }
            }
            return out.slice(0, 4);
        },

        // ═══════════════════ HEATMAP + VOLUME ═══════════════════
        // Axes x weeks letter heatmap (plus a global row): the fastest way to see
        // where and when the score degrades.
        get heatmap() {
            var self = this;
            var axes = ['kpiFrequency', 'kpiDuration', 'kpiStability', 'kpiMaintenance'];
            var rows = axes.map(function (k) {
                return {
                    key: k,
                    label: self.kpiMeta[k].label,
                    cells: self.weeks.map(function (w) {
                        var st = self.weekStats[w.val] || {};
                        var kpi = st[k] || {};
                        return {
                            week: w.label,
                            letter: kpi.scoreL || 'NA',
                            score: (kpi.score !== undefined && kpi.score !== null) ? kpi.score : null,
                            valueF: self._fmtKpiValue(k, kpi.value)
                        };
                    })
                };
            });
            rows.push({
                key: 'global',
                label: 'Global',
                cells: this.weeks.map(function (w, i) {
                    var p = self.trendPoints[i] || {};
                    return { week: w.label, letter: p.letter || 'NA', score: p.score, valueF: p.score !== null ? (p.score + ' / 100') : '-' };
                })
            });
            return rows;
        },
        get volumeChart() {
            var self = this;
            var W = 720, H = 190, padX = 36, padTop = 20, padBottom = 32;
            var innerW = W - padX * 2, innerH = H - padTop - padBottom;
            var bars = this.weeks.map(function (w) {
                var tagSt = (self.raw && self.raw.weekStatsTag && self.raw.weekStatsTag[w.val]) || {};
                var exeSt = (self.raw && self.raw.weekStatsExe && self.raw.weekStatsExe[w.val]) || {};
                var nbExe = tagSt.nbExe || 0;
                var unstable = (exeSt.nbFlaky || 0) + (exeSt.nbFN || 0);
                return { label: w.label, value: nbExe, unstable: unstable, pct: nbExe > 0 ? Math.round(unstable * 1000 / nbExe) / 10 : null };
            });
            var max = Math.max.apply(null, bars.map(function (b) { return b.value; }).concat([1]));
            var n = bars.length;
            var slot = n ? innerW / n : innerW;
            var barW = Math.max(8, Math.min(40, slot * 0.55));
            // Never let week labels overlap: sample them from the END so the most recent
            // week always keeps its label and the spacing stays constant (a "Wnn" label
            // needs ~34px at this font size).
            var labelEvery = Math.max(1, Math.ceil(34 / slot));
            var out = { W: W, H: H, bars: bars, rects: [], flakyPts: [], flakyPath: '', baseline: padTop + innerH, max: max, gridTopY: padTop };
            bars.forEach(function (b, i) {
                var x = padX + i * slot + (slot - barW) / 2;
                var h = max ? (b.value / max) * innerH : 0;
                var showLabel = ((n - 1 - i) % labelEvery === 0);
                out.rects.push({
                    x: x, y: padTop + innerH - h, w: barW, h: h, label: b.label, value: b.value,
                    unstable: b.unstable, cx: x + barW / 2, showLabel: showLabel
                });
            });
            var path = [];
            out.rects.forEach(function (r, i) {
                var b = bars[i];
                if (b.pct === null) return;
                var y = padTop + innerH * (1 - Math.min(b.pct, 100) / 100);
                out.flakyPts.push({ x: r.cx, y: y, pct: b.pct, label: b.label });
                path.push((path.length ? 'L' : 'M') + r.cx.toFixed(1) + ' ' + y.toFixed(1));
            });
            out.flakyPath = path.join(' ');
            return out;
        },

        // ═══════════════════ SVG BUILDERS ═══════════════════
        // Radar: 4 axes (top=Frequency, right=Stability, bottom=Duration, left=Maintenance)
        _radarOrder: ['kpiFrequency', 'kpiStability', 'kpiDuration', 'kpiMaintenance'],
        radarPoint(axisIndex, frac) {
            var cx = 110, cy = 108, r = 82;
            var angles = [-90, 0, 90, 180];
            var a = angles[axisIndex] * Math.PI / 180;
            return (cx + Math.cos(a) * r * frac).toFixed(1) + ',' + (cy + Math.sin(a) * r * frac).toFixed(1);
        },
        radarGrid(frac) {
            var self = this;
            return [0, 1, 2, 3].map(function (i) { return self.radarPoint(i, frac); }).join(' ');
        },
        // one dot per axis, in this._radarOrder order - the polygon is derived from it so
        // the outline and the vertex markers can never drift apart
        get radarDots() {
            var self = this;
            if (!this.kpis.length) return [0, 1, 2, 3].map(function (i) { return { x: 110, y: 108 }; });
            return this._radarOrder.map(function (k, i) {
                var kpi = self.kpis.find(function (x) { return x.key === k; }) || {};
                var frac = (kpi.score !== null && kpi.score !== undefined) ? (kpi.score / 25) : 0;
                var xy = self.radarPoint(i, Math.max(frac, 0.02)).split(',');
                return { x: xy[0], y: xy[1] };
            });
        },
        get radarPolygon() {
            return this.radarDots.map(function (p) { return p.x + ',' + p.y; }).join(' ');
        },
        radarDot(axisIndex) {
            return this.radarDots[axisIndex] || { x: 110, y: 108 };
        },
        radarKpi(axisIndex) {
            var k = this._radarOrder[axisIndex];
            return this.kpis.find(function (x) { return x.key === k; }) || { label: '', letter: 'NA', score: null };
        },

        // Trend + volume charts are built as full SVG strings (rendered with x-html):
        // <template x-for> does not work inside an inline <svg>, same pitfall as the donut.
        get trendChartSvg() {
            var c = this.trendChart;
            var svg = '<svg viewBox="0 0 ' + c.W + ' ' + c.H + '" class="v2as-trend">';
            c.gridY.forEach(function (g) {
                svg += '<line x1="26" x2="' + (c.W - 10) + '" y1="' + g.y + '" y2="' + g.y + '" class="v2as-trend-grid"></line>'
                    + '<text x="20" y="' + (g.y + 3) + '" text-anchor="end" class="v2as-trend-gridlabel">' + g.v + '</text>';
            });
            if (c.area) svg += '<path d="' + c.area + '" class="v2as-trend-area"></path>';
            if (c.path) svg += '<path d="' + c.path + '" class="v2as-trend-line"></path>';
            var n = c.points.length;
            var slot = n > 1 ? (c.points[1].x - c.points[0].x) : 100;
            var labelEvery = Math.max(1, Math.ceil(34 / slot));
            c.points.forEach(function (p, i) {
                if (p.y !== null) {
                    svg += '<circle cx="' + p.x + '" cy="' + p.y + '" r="4.5" class="v2as-trend-dot" fill="' + p.color + '">'
                        + '<title>' + p.label + ': ' + p.score + ' / 100 (' + p.letter + ')</title></circle>';
                }
                if ((n - 1 - i) % labelEvery === 0) {
                    svg += '<text x="' + p.x + '" y="' + (c.H - 6) + '" text-anchor="middle" class="v2as-trend-xlabel">' + p.label + '</text>';
                }
            });
            return svg + '</svg>';
        },
        get volumeChartSvg() {
            var c = this.volumeChart;
            var svg = '<svg viewBox="0 0 ' + c.W + ' ' + c.H + '" class="v2as-volume">';
            // max gridline for scale + baseline
            svg += '<line x1="30" x2="' + (c.W - 12) + '" y1="' + c.gridTopY + '" y2="' + c.gridTopY + '" class="v2as-trend-grid"></line>';
            svg += '<text x="26" y="' + (c.gridTopY + 3) + '" text-anchor="end" class="v2as-trend-gridlabel">' + c.max + '</text>';
            svg += '<line x1="30" x2="' + (c.W - 12) + '" y1="' + c.baseline + '" y2="' + c.baseline + '" class="v2as-volume-baseline"></line>';
            var showVals = c.rects.length < 2 || (c.rects[1].cx - c.rects[0].cx) >= 26;
            c.rects.forEach(function (r) {
                if (r.value > 0) {
                    var h = Math.max(r.h, 2);
                    svg += '<rect x="' + r.x.toFixed(1) + '" y="' + (c.baseline - h).toFixed(1) + '" width="' + r.w.toFixed(1)
                        + '" height="' + h.toFixed(1) + '" rx="4" class="v2as-volume-bar">'
                        + '<title>' + r.label + ': ' + r.value + ' executions' + (r.unstable ? ' - ' + r.unstable + ' unstable' : '') + '</title></rect>';
                    if (showVals) svg += '<text x="' + r.cx.toFixed(1) + '" y="' + (c.baseline - h - 5).toFixed(1) + '" text-anchor="middle" class="v2as-volume-val">' + r.value + '</text>';
                } else {
                    // ghost bar: keeps every week visually anchored to its slot
                    svg += '<rect x="' + r.x.toFixed(1) + '" y="' + (c.baseline - 3) + '" width="' + r.w.toFixed(1)
                        + '" height="3" rx="1.5" class="v2as-volume-ghost"><title>' + r.label + ': no execution</title></rect>';
                }
                // axis tick under each slot, labels hang right below their tick
                svg += '<line x1="' + r.cx.toFixed(1) + '" x2="' + r.cx.toFixed(1) + '" y1="' + c.baseline + '" y2="' + (c.baseline + 4) + '" class="v2as-volume-tick"></line>';
                if (r.showLabel) {
                    svg += '<text x="' + r.cx.toFixed(1) + '" y="' + (c.baseline + 16) + '" text-anchor="middle" class="v2as-volume-xlabel">' + r.label + '</text>';
                }
            });
            if (c.flakyPath) svg += '<path d="' + c.flakyPath + '" class="v2as-volume-flakyline"></path>';
            c.flakyPts.forEach(function (p) {
                svg += '<circle cx="' + p.x.toFixed(1) + '" cy="' + p.y.toFixed(1) + '" r="3.5" class="v2as-volume-flakydot">'
                    + '<title>' + p.label + ': ' + p.pct + '% unstable</title></circle>';
            });
            return svg + '</svg>';
        },

        // Trend line: score history over the selected weeks
        get trendChart() {
            var pts = this.trendPoints;
            var self = this;
            var W = 560, H = 170, padX = 30, padTop = 14, padBottom = 26;
            var innerW = W - padX * 2, innerH = H - padTop - padBottom;
            var n = pts.length;
            var out = { W: W, H: H, points: [], path: '', area: '', gridY: [] };
            [0, 50, 100].forEach(function (v) {
                out.gridY.push({ y: padTop + innerH * (1 - v / 100), v: v });
            });
            if (!n) return out;
            var step = n > 1 ? innerW / (n - 1) : 0;
            var path = [];
            var firstX = null, lastX = null;
            pts.forEach(function (p, i) {
                var x = padX + (n > 1 ? i * step : innerW / 2);
                if (p.score === null) {
                    out.points.push({ x: x, y: null, label: p.label, score: null, letter: p.letter });
                    return;
                }
                var y = padTop + innerH * (1 - p.score / 100);
                out.points.push({ x: x, y: y, label: p.label, score: p.score, letter: p.letter, color: self.letterColor(p.letter) });
                if (firstX === null) firstX = x;
                lastX = x;
                path.push((path.length ? 'L' : 'M') + x.toFixed(1) + ' ' + y.toFixed(1));
            });
            out.path = path.join(' ');
            if (path.length > 1) {
                out.area = out.path + ' L' + lastX.toFixed(1) + ' ' + (padTop + innerH).toFixed(1)
                    + ' L' + firstX.toFixed(1) + ' ' + (padTop + innerH).toFixed(1) + ' Z';
            }
            return out;
        },
        sparkPath(history) {
            var W = 92, H = 26, pad = 2;
            var vals = history || [];
            var n = vals.length;
            if (!n) return '';
            var step = n > 1 ? (W - pad * 2) / (n - 1) : 0;
            var path = [];
            vals.forEach(function (v, i) {
                if (v === null) return;
                var x = pad + i * step;
                var y = pad + (H - pad * 2) * (1 - v / 25);
                path.push((path.length ? 'L' : 'M') + x.toFixed(1) + ' ' + y.toFixed(1));
            });
            return path.join(' ');
        },
        userSparkPath(history) {
            var W = 92, H = 22, pad = 2;
            var vals = history || [];
            var n = vals.length;
            if (!n) return '';
            var max = Math.max.apply(null, vals.concat([1]));
            var step = n > 1 ? (W - pad * 2) / (n - 1) : 0;
            var path = [];
            vals.forEach(function (v, i) {
                var x = pad + i * step;
                var y = pad + (H - pad * 2) * (1 - v / max);
                path.push((path.length ? 'L' : 'M') + x.toFixed(1) + ' ' + y.toFixed(1));
            });
            return path.join(' ');
        },

        // ═══════════════════ MEMBER REPORT ═══════════════════
        memberOpen: false,
        member: null,
        openMember(u) {
            var self = this;
            var changes = (this.raw && this.raw.chgmts ? this.raw.chgmts : []).filter(function (c) { return c.user === u.user; });
            var totalChanges = (this.raw && this.raw.chgmts ? this.raw.chgmts : []).length;

            // What this member works on: most touched test cases, crossed with the
            // period execution stats to flag the ones that are still unstable.
            var map = {};
            changes.forEach(function (c) {
                var k = c.testFolder + ' | ' + c.testcaseId;
                if (!map[k]) map[k] = { testFolder: c.testFolder, testcaseId: c.testcaseId, count: 0, lastTouch: 0 };
                map[k].count++;
                var t = new Date(c.date).getTime();
                if (t > map[k].lastTouch) map[k].lastTouch = t;
            });
            var touched = Object.values(map).sort(function (a, b) { return b.count - a.count; });
            touched.forEach(function (t) {
                var m = self.testcaseRows.find(function (x) { return x.testFolder === t.testFolder && x.testcaseId === t.testcaseId; });
                t.unstable = m ? (m.nbFlaky || 0) + (m.nbFN || 0) : 0;
                t.executed = m ? (m.nb || 0) : 0;
            });

            var sharePct = totalChanges ? Math.round(changes.length * 100 / totalChanges) : 0;
            var regularityPct = this.weeks.length ? Math.round(u.weeksActive * 100 / this.weeks.length) : 0;
            var daysSince = u.lastActive ? Math.floor((Date.now() - u.lastActive) / 86400000) : null;
            var unstableTouched = touched.filter(function (t) { return t.unstable > 0; });
            var neverExecuted = touched.filter(function (t) { return t.executed === 0; });

            // Personal signals: does / does not / should watch
            var signals = [];
            if (regularityPct >= 50) signals.push({ level: 'good', text: 'Regular contributor: active ' + u.weeksActive + ' of the last ' + this.weeks.length + ' weeks (' + regularityPct + '%).' });
            else signals.push({ level: 'warn', text: 'Irregular activity: only ' + u.weeksActive + ' active week(s) out of ' + this.weeks.length + '.' });
            if (daysSince !== null && daysSince > 14) signals.push({ level: 'warn', text: 'No test case change for ' + daysSince + ' days.' });
            if (sharePct >= 60 && totalChanges > 5) signals.push({ level: 'warn', text: 'Carries ' + sharePct + '% of the team changes: knowledge concentration risk.' });
            else if (sharePct > 0) signals.push({ level: 'info', text: 'Contributes ' + sharePct + '% of the team changes on the period.' });
            if (unstableTouched.length > 0) signals.push({ level: 'warn', text: unstableTouched.length + ' test case(s) they maintain still produced flaky or false negative results: ' + unstableTouched.slice(0, 3).map(function (t) { return t.testcaseId; }).join(', ') + '.' });
            else if (touched.length > 0) signals.push({ level: 'good', text: 'None of the test cases they touched produced unstable results on the period.' });
            if (neverExecuted.length > 0) signals.push({ level: 'warn', text: neverExecuted.length + ' maintained test case(s) were never executed on the period (' + neverExecuted.slice(0, 3).map(function (t) { return t.testcaseId; }).join(', ') + '): maintenance without runs is wasted effort.' });
            var avgSession = u.saves > 0 ? Math.round(u.timeMs / u.saves) : 0;

            this.member = {
                user: u.user,
                initials: u.initials,
                badges: u.badges,
                impact: u.impact,
                saves: u.saves,
                timeMs: u.timeMs,
                weeksActive: u.weeksActive,
                lastActive: u.lastActive,
                regularityPct: regularityPct,
                sharePct: sharePct,
                avgSession: avgSession,
                history: u.history,
                touched: touched.slice(0, 8),
                signals: signals
            };
            this.memberOpen = true;
        },
        get memberBarsSvg() {
            if (!this.member) return '';
            var hist = this.member.history || [];
            var W = 620, H = 130, padX = 14, padTop = 16, padBottom = 24;
            var innerW = W - padX * 2, innerH = H - padTop - padBottom;
            var n = hist.length;
            if (!n) return '';
            var max = Math.max.apply(null, hist.concat([1]));
            var slot = innerW / n;
            var barW = Math.max(8, Math.min(44, slot * 0.55));
            var labelEvery = Math.max(1, Math.ceil(34 / slot));
            var showVals = slot >= 26;
            var svg = '<svg viewBox="0 0 ' + W + ' ' + H + '" class="v2as-memberbars">';
            svg += '<line x1="' + padX + '" x2="' + (W - padX) + '" y1="' + (padTop + innerH) + '" y2="' + (padTop + innerH) + '" class="v2as-volume-baseline"></line>';
            var self = this;
            var baseline = padTop + innerH;
            hist.forEach(function (v, i) {
                var x = padX + i * slot + (slot - barW) / 2;
                var h = max ? (v / max) * innerH : 0;
                var cx = x + barW / 2;
                var label = self.weeks[i] ? self.weeks[i].label : '';
                if (v > 0) {
                    svg += '<rect x="' + x.toFixed(1) + '" y="' + (baseline - Math.max(h, 2)).toFixed(1) + '" width="' + barW.toFixed(1)
                        + '" height="' + Math.max(h, 2).toFixed(1) + '" rx="3" class="v2as-volume-bar">'
                        + '<title>' + label + ': ' + v + ' change(s)</title></rect>';
                    if (showVals) svg += '<text x="' + cx.toFixed(1) + '" y="' + (baseline - h - 4).toFixed(1) + '" text-anchor="middle" class="v2as-volume-val">' + v + '</text>';
                } else {
                    svg += '<rect x="' + x.toFixed(1) + '" y="' + (baseline - 3) + '" width="' + barW.toFixed(1)
                        + '" height="3" rx="1.5" class="v2as-volume-ghost"><title>' + label + ': no change</title></rect>';
                }
                svg += '<line x1="' + cx.toFixed(1) + '" x2="' + cx.toFixed(1) + '" y1="' + baseline + '" y2="' + (baseline + 4) + '" class="v2as-volume-tick"></line>';
                if ((n - 1 - i) % labelEvery === 0) {
                    svg += '<text x="' + cx.toFixed(1) + '" y="' + (baseline + 15) + '" text-anchor="middle" class="v2as-volume-xlabel">' + label + '</text>';
                }
            });
            return svg + '</svg>';
        },

        // ═══════════════════ PDF REPORT ═══════════════════
        generatePdf() {
            var w = window.open('', '_blank');
            if (!w) { showMessageMainPage('warning', 'Popup blocked by the browser: allow popups to generate the report.'); return; }
            w.document.open();
            w.document.write(this._printHtml());
            w.document.close();
        },
        _esc(v) {
            return String(v === undefined || v === null ? '' : v)
                .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
        },
        _printHtml() {
            var self = this;
            var sg = this.scoreGlobal;
            var letterColor = sg ? this.letterColor(sg.letter) : '#94a3b8';

            var kpiCells = this.kpis.map(function (k) {
                return '<div class="ptile" style="border-color:' + self.letterColor(k.letter) + '44">'
                    + '<div class="ptile-head">' + self._esc(k.label) + '<span class="plet" style="background:' + self.letterColor(k.letter) + '">' + k.letter + '</span></div>'
                    + '<div class="ptile-v">' + self._esc(k.valueF) + '</div>'
                    + '<div class="ptile-u">' + self._esc(k.unit) + '</div>'
                    + '<div class="ptile-t">' + self._esc(k.target) + '</div>'
                    + '</div>';
            }).join('');

            var heatRows = this.heatmap.map(function (row) {
                var cells = row.cells.map(function (c) {
                    return '<td style="background:' + self.letterColor(c.letter) + (c.letter === 'NA' ? '22' : '30') + '; color:'
                        + (c.letter === 'NA' ? '#94a3b8' : '#0f172a') + '"><b>' + c.letter + '</b><br><span class="mut">' + self._esc(c.valueF) + '</span></td>';
                }).join('');
                return '<tr><td class="hlbl">' + self._esc(row.label) + '</td>' + cells + '</tr>';
            }).join('');
            var heatHead = this.weeks.map(function (w) { return '<th>' + self._esc(w.label) + '</th>'; }).join('');

            var insightsHtml = this.insights.map(function (ins) {
                var color = ins.level === 'good' ? '#059669' : (ins.level === 'warn' ? '#b45309' : '#475569');
                return '<li style="color:' + color + '">' + self._esc(ins.text) + '</li>';
            }).join('');

            var tcRows = this.testcaseRows.slice(0, 12).map(function (t) {
                return '<tr><td>' + self._esc(t.testFolder) + '<br><b>' + self._esc(t.testcaseId) + '</b></td>'
                    + '<td>' + self._esc(t.application) + '</td><td class="num">' + (t.nb || 0) + '</td>'
                    + '<td class="num">' + (t.nbFlaky || 0) + '</td><td class="num">' + (t.nbFN || 0) + '</td>'
                    + '<td class="num">' + self._esc(self.fmtDuration(t.duration)) + '</td></tr>';
            }).join('');

            var teamRows = this.leaderboard.map(function (u) {
                return '<tr><td><b>' + self._esc(u.user) + '</b></td><td class="num">' + u.impact + ' pts</td>'
                    + '<td class="num">' + u.saves + '</td><td class="num">' + self._esc(self.fmtDuration(u.timeMs)) + '</td>'
                    + '<td class="num">' + u.weeksActive + '</td>'
                    + '<td>' + u.badges.map(function (b) { return self._esc(b.label); }).join(', ') + '</td></tr>';
            }).join('');

            return '<!DOCTYPE html><html><head><meta charset="utf-8">'
                + '<title>Automate Score - Cerberus report</title>'
                + '<style>'
                + '@page { size: A4; margin: 12mm; }'
                + '* { box-sizing: border-box; -webkit-print-color-adjust: exact; print-color-adjust: exact; }'
                + 'body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; color: #0f172a; margin: 0; padding: 24px; font-size: 12px; }'
                + '@media print { body { padding: 0; } .printbtn { display: none; } }'
                + '.printbtn { position: fixed; top: 14px; right: 14px; background: #00BCFF; color: #fff; border: none; border-radius: 8px; padding: 9px 16px; font-size: 13px; font-weight: 600; cursor: pointer; box-shadow: 0 6px 18px rgba(0,0,0,0.18); }'
                + '.band { display: flex; justify-content: space-between; align-items: center; background: linear-gradient(135deg, #0b1120, #1d4ed8); color: #fff; border-radius: 14px; padding: 18px 22px; }'
                + '.brand { font-size: 20px; font-weight: 800; letter-spacing: 3px; }'
                + '.rpt { font-size: 11px; opacity: 0.85; margin-top: 2px; }'
                + '.gen { font-size: 10px; opacity: 0.75; text-align: right; }'
                + '.scorerow { display: flex; gap: 14px; margin-top: 14px; align-items: stretch; }'
                + '.scorebox { border: 2px solid ' + letterColor + '44; border-radius: 14px; padding: 14px 22px; text-align: center; min-width: 170px; }'
                + '.scorebox .v { font-size: 38px; font-weight: 800; color: ' + letterColor + '; line-height: 1; }'
                + '.scorebox .l { font-size: 10px; color: #64748b; font-weight: 700; letter-spacing: 1px; margin-top: 4px; }'
                + '.insights { flex: 1; border: 1px solid #e2e8f0; background: #f8fafc; border-radius: 14px; padding: 10px 16px; }'
                + '.insights ul { margin: 4px 0; padding-left: 18px; } .insights li { margin: 3px 0; font-size: 11.5px; }'
                + '.ptiles { display: flex; gap: 10px; margin-top: 14px; }'
                + '.ptile { flex: 1; border: 1.5px solid #e2e8f0; border-radius: 12px; padding: 10px 12px; }'
                + '.ptile-head { font-size: 9.5px; font-weight: 800; text-transform: uppercase; letter-spacing: 0.4px; color: #64748b; display: flex; justify-content: space-between; align-items: center; }'
                + '.plet { display: inline-flex; align-items: center; justify-content: center; width: 18px; height: 18px; border-radius: 5px; color: #fff; font-weight: 800; font-size: 11px; }'
                + '.ptile-v { font-size: 20px; font-weight: 800; margin-top: 5px; }'
                + '.ptile-u { font-size: 9px; color: #64748b; }'
                + '.ptile-t { font-size: 8.5px; color: #94a3b8; margin-top: 5px; font-style: italic; }'
                + 'h2 { font-size: 12px; font-weight: 800; text-transform: uppercase; letter-spacing: 0.6px; color: #334155; margin: 18px 0 6px; border-bottom: 2px solid #e2e8f0; padding-bottom: 4px; }'
                + '.heat { border-collapse: collapse; width: 100%; }'
                + '.heat th { font-size: 9px; color: #64748b; text-transform: uppercase; padding: 4px; }'
                + '.heat td { text-align: center; padding: 6px 4px; font-size: 10px; border: 2px solid #fff; border-radius: 6px; }'
                + '.heat td.hlbl { background: transparent; text-align: left; font-weight: 700; font-size: 10.5px; color: #334155; white-space: nowrap; }'
                + '.heat .mut { font-size: 8px; color: #475569; }'
                + '.data { border-collapse: collapse; width: 100%; }'
                + '.data th { text-align: left; font-size: 9.5px; text-transform: uppercase; color: #64748b; border-bottom: 1.5px solid #cbd5e1; padding: 5px 8px; }'
                + '.data td { border-bottom: 1px solid #eef2f7; padding: 5px 8px; vertical-align: top; }'
                + '.data .num, .data th.num { text-align: right; }'
                + '.mut { color: #64748b; }'
                + '.foot { margin-top: 22px; padding-top: 8px; border-top: 1px solid #e2e8f0; font-size: 9.5px; color: #94a3b8; text-align: center; }'
                + '</style></head><body>'
                + '<button class="printbtn" onclick="window.print()">Print or save as PDF</button>'
                + '<div class="band">'
                +   '<div><div class="brand">CERBERUS</div><div class="rpt">Automate Score report - last ' + this.nbWeeks + ' weeks</div></div>'
                +   '<div class="gen">' + this._esc(this.selSystems.join(', ') || 'All systems') + (this.selCampaign ? ' - campaign ' + this._esc(this.selCampaign) : '')
                +   '<br>Generated on ' + this._esc(new Date().toLocaleString()) + '</div>'
                + '</div>'
                + '<div class="scorerow">'
                +   '<div class="scorebox"><div class="v">' + (sg ? sg.score : '-') + '<span style="font-size:16px; color:#64748b"> /100</span></div>'
                +   '<div class="l">GLOBAL SCORE - WEEK ' + (sg ? this._esc(sg.weekLabel) : '-') + (sg && sg.delta !== null ? ' (' + (sg.delta > 0 ? '+' : '') + sg.delta + ' pts)' : '') + '</div></div>'
                +   '<div class="insights"><b style="font-size:10px; letter-spacing:0.5px; color:#334155">INSIGHTS</b><ul>' + insightsHtml + '</ul></div>'
                + '</div>'
                + '<div class="ptiles">' + kpiCells + '</div>'
                + '<h2>Weekly heatmap</h2>'
                + '<table class="heat"><thead><tr><th></th>' + heatHead + '</tr></thead><tbody>' + heatRows + '</tbody></table>'
                + '<h2>Test cases</h2>'
                + '<table class="data"><thead><tr><th>Test case</th><th>Application</th><th class="num">Exe</th><th class="num">Flaky</th><th class="num">False neg</th><th class="num">Avg duration</th></tr></thead>'
                + '<tbody>' + tcRows + '</tbody></table>'
                + '<h2>Team activity</h2>'
                + '<table class="data"><thead><tr><th>Member</th><th class="num">Impact</th><th class="num">Changes</th><th class="num">Time</th><th class="num">Weeks</th><th>Badges</th></tr></thead>'
                + '<tbody>' + teamRows + '</tbody></table>'
                + '<div class="foot">Cerberus Testing - Automate Score</div>'
                + '<script>setTimeout(function () { window.print(); }, 500);<\/script>'
                + '</body></html>';
        },

        // ═══════════════════ UI HELPERS ═══════════════════
        letterColor(l) { return this.letterColors[l] || this.letterColors.NA; },
        trendMeta(t) {
            switch (t) {
                case 'OKUP': return { arrow: 'up', good: true, label: 'improving' };
                case 'OKDOWN': return { arrow: 'down', good: true, label: 'improving' };
                case 'KOUP': return { arrow: 'up', good: false, label: 'degrading' };
                case 'KODOWN': return { arrow: 'down', good: false, label: 'degrading' };
                case 'ISO': return { arrow: 'flat', good: null, label: 'stable' };
                default: return { arrow: 'flat', good: null, label: 'not evaluated' };
            }
        },
        fmtDuration(ms) {
            if (ms === undefined || ms === null || ms < 0) return '-';
            if (ms === 0) return '0s';
            var s = Math.round(ms / 1000);
            if (s < 60) return s + 's';
            var m = Math.floor(s / 60);
            if (m < 60) return m + 'm ' + (s % 60) + 's';
            var h = Math.floor(m / 60);
            if (h < 48) return h + 'h ' + (m % 60) + 'm';
            return Math.floor(h / 24) + 'd ' + (h % 24) + 'h';
        },
        relTime(ts) {
            if (!ts) return '-';
            var diff = Date.now() - ts;
            if (diff < 0) return '-';
            var min = Math.floor(diff / 60000);
            if (min < 60) return min + ' min ago';
            var h = Math.floor(min / 60);
            if (h < 24) return h + 'h ago';
            var days = Math.floor(h / 24);
            if (days < 31) return days + 'd ago';
            return new Date(ts).toLocaleDateString();
        },
        get attentionCount() {
            return this.testcaseRows.filter(function (t) { return (t.nbFlaky || 0) + (t.nbFN || 0) > 0; }).length;
        },
        openTestcase(t) {
            window.open('./TestCaseScriptV2.jsp?test=' + encodeURIComponent(t.testFolder) + '&testcase=' + encodeURIComponent(t.testcaseId), '_blank');
        }
    };
}
