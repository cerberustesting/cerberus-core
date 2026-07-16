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
 * Campaign Trends - Alpine dashboard over ReadTagStat.
 *
 * Every campaign run (tag) of the selected period is charted: stacked status bars
 * per run, duration / CI score over time per execution combination, and a run table
 * that links back to the Campaign Report of each tag.
 */
function campaignTrends() {
    var IN = window.InsightsShared;
    return {
        // ── State ──
        loading: false,
        loaded: false,
        error: '',
        raw: null,

        // ── Filters ──
        campaigns: [],
        selCampaigns: [],
        campDdOpen: false,
        campSearch: '',
        periodDays: 30,
        activeCountries: {},
        activeEnvs: {},
        activeRobots: {},
        activeCi: {},

        // ── Data ──
        runs: [],            // one entry per tag: {tag, campaign, t, durMs, ciRes, ciSc, ciScT, nbExe, nbExeU, nbFlaky, nbMuted, fn, segments[]}
        durationSeries: [],  // one line per combination
        chartTab: 'duration', // 'duration' | 'ci'

        IN: IN,

        // ═══════════════════ INIT / LOAD ═══════════════════
        init() {
            try {
                var saved = JSON.parse(localStorage.getItem('ct.filters') || 'null');
                if (saved && typeof saved === 'object') {
                    if ([7, 30, 90, 180].indexOf(saved.periodDays) >= 0) this.periodDays = saved.periodDays;
                    if (Array.isArray(saved.selCampaigns)) {
                        this.selCampaigns = saved.selCampaigns.filter(function (c) { return typeof c === 'string'; });
                    }
                }
            } catch (e) { /* defaults */ }
            var urlCampaigns = [];
            try {
                new URLSearchParams(window.location.search).getAll('campaigns').forEach(function (c) { if (c) urlCampaigns.push(c); });
            } catch (e) { /* ignore */ }
            if (urlCampaigns.length) this.selCampaigns = urlCampaigns;
            var self = this;
            // ReadTagStat requires explicit campaigns: wait for the list before the first load
            this._loadCampaigns().then(function () { self.load(); });
        },
        _loadCampaigns() {
            var self = this;
            return new Promise(function (resolve) {
                $.getJSON('ReadCampaign?iDisplayStart=0&iDisplayLength=300&iSortCol_0=1&sSortDir_0=asc', function (data) {
                    self.campaigns = (data.contentTable || []).map(function (c) { return c.campaign; }).filter(Boolean).sort();
                    resolve();
                }).fail(function () { resolve(); });
            });
        },
        get filteredCampaigns() {
            var q = this.campSearch.trim().toLowerCase();
            if (!q) return this.campaigns;
            return this.campaigns.filter(function (c) { return c.toLowerCase().indexOf(q) >= 0; });
        },
        toggleCampaign(c) {
            var i = this.selCampaigns.indexOf(c);
            if (i >= 0) this.selCampaigns.splice(i, 1);
            else this.selCampaigns.push(c);
        },
        load() {
            var self = this;
            this.loading = true;
            this.error = '';
            localStorage.setItem('ct.filters', JSON.stringify({ periodDays: this.periodDays, selCampaigns: this.selCampaigns }));

            var to = new Date();
            var from = new Date(Date.now() - this.periodDays * 86400000);
            var params = ['from=' + encodeURIComponent(from.toISOString()), 'to=' + encodeURIComponent(to.toISOString())];
            // the servlet only returns tags of the requested campaigns: none selected = all known
            var requested = this.selCampaigns.length ? this.selCampaigns : this.campaigns;
            requested.forEach(function (c) { params.push('campaigns=' + encodeURIComponent(c)); });
            var pushOff = function (map, name) {
                Object.keys(map).forEach(function (k) { if (map[k]) params.push(name + '=' + encodeURIComponent(k)); });
            };
            // server-side filters (only sent when at least one value is unchecked)
            var offIn = function (map) { return Object.keys(map).some(function (k) { return !map[k]; }); };
            if (offIn(this.activeCountries)) pushOff(this.activeCountries, 'countries');
            if (offIn(this.activeEnvs)) pushOff(this.activeEnvs, 'environments');
            if (offIn(this.activeRobots)) pushOff(this.activeRobots, 'robotDeclis');
            if (offIn(this.activeCi)) pushOff(this.activeCi, 'ciResults');

            InsertURLInHistory('./ReportingCampaignOverTime.jsp?' + this.selCampaigns.map(function (c) { return 'campaigns=' + encodeURIComponent(c); }).join('&'));

            $.getJSON('ReadTagStat?' + params.join('&'), function (data) {
                self.loading = false;
                if (!data || data.messageType !== 'OK') {
                    self.error = (data && data.message) || 'Could not load the campaign statistics.';
                    self.loaded = false;
                    return;
                }
                self._apply(data);
            }).fail(function (xhr) {
                self.loading = false;
                self.error = 'Could not load the campaign statistics (' + xhr.status + ').';
                self.loaded = false;
            });
        },
        toggleIn(map, k) { map[k] = !map[k]; this.load(); },
        allIn(map) { Object.keys(map).forEach(function (k) { map[k] = true; }); this.load(); },

        // ═══════════════════ MAPPING ═══════════════════
        _apply(data) {
            var self = this;
            this.raw = data;

            // distinct values feed the secondary filters; entries are {hasData, name, isRequested}
            var syncMap = function (map, list) {
                (list || []).forEach(function (e) {
                    var k = typeof e === 'string' ? e : e.name;
                    if (!k) return; // empty names (e.g. executions without robot) stay unfiltered
                    if (map[k] === undefined) map[k] = true;
                });
                return map;
            };
            var dist = data.distinct || {};
            syncMap(this.activeCountries, dist.countries);
            syncMap(this.activeEnvs, dist.environments);
            syncMap(this.activeRobots, dist.robotDeclis);
            syncMap(this.activeCi, dist.ciResults);

            // Status counts per tag (curvesTagStatus points are aligned with curvesTag)
            var tagLabels = data.curvesTag || [];
            var statusByTag = {};
            tagLabels.forEach(function (t) { statusByTag[t] = []; });
            (data.curvesTagStatus || []).forEach(function (curve) {
                var st = curve.key && curve.key.key ? curve.key.key : curve.key;
                (curve.points || []).forEach(function (v, i) {
                    var tag = tagLabels[i];
                    if (tag === undefined || !v) return;
                    statusByTag[tag].push({ status: String(st), value: v });
                });
            });

            // One run per tag from the duration curves (they carry all the tag metadata)
            var runMap = {};
            this.durationSeries = [];
            (data.curvesTime || []).forEach(function (curve, ci) {
                var key = curve.key || {};
                var serieName = [key.campaign, key.country, key.environment, key.robotdecli].filter(Boolean).join(' / ');
                // key.key is the server-side composite, unique per curve; ci as fallback
                var serie = { id: key.key || ('serie-' + ci), name: serieName || ('serie ' + (ci + 1)), color: IN.seriesPalette[ci % IN.seriesPalette.length], points: [] };
                (curve.points || []).forEach(function (p) {
                    var t = new Date(p.x).getTime();
                    if (isNaN(t)) return;
                    var durMs = (p.y || 0) * 60000; // server unit: minutes
                    serie.points.push({
                        t: t, v: durMs,
                        title: p.tag + ' - ' + IN.fmtDuration(durMs) + (p.ciRes ? ' - CI ' + p.ciRes : ''),
                        dotColor: p.ciRes === 'KO' ? IN.statusColors.KO : (p.ciRes === 'OK' ? IN.statusColors.OK : undefined),
                        attr: 'data-tag="' + IN.esc(p.tag) + '"',
                        _p: p
                    });
                    if (!runMap[p.tag]) {
                        runMap[p.tag] = {
                            tag: p.tag,
                            campaign: key.campaign || '',
                            t: t,
                            durMs: durMs,
                            ciRes: p.ciRes || '',
                            ciSc: p.ciSc, ciScT: p.ciScT,
                            nbExe: p.nbExe || 0, nbExeU: p.nbExeU || 0,
                            nbFlaky: p.nbFlaky || 0, nbMuted: p.nbMuted || 0,
                            fn: !!p.falseNegative,
                            segments: statusByTag[p.tag] || []
                        };
                    } else if (durMs > runMap[p.tag].durMs) {
                        runMap[p.tag].durMs = durMs;
                    }
                });
                if (serie.points.length) self.durationSeries.push(serie);
            });
            // tags that only exist in the status curves (no duration point)
            tagLabels.forEach(function (tag) {
                if (!runMap[tag]) {
                    runMap[tag] = { tag: tag, campaign: '', t: self._tagDate(tag), durMs: null, ciRes: '', nbExe: 0, nbExeU: 0, nbFlaky: 0, nbMuted: 0, fn: false, segments: statusByTag[tag] || [] };
                }
            });

            var runs = Object.values(runMap);
            // chronological; undated runs (no parsable date at all) go first, in server order
            runs.sort(function (a, b) {
                if (a.t && b.t) return a.t - b.t;
                if (a.t) return 1;
                if (b.t) return -1;
                return tagLabels.indexOf(a.tag) - tagLabels.indexOf(b.tag);
            });
            this.runs = runs;
            this.loaded = true;
        },
        // default Cerberus tags embed their date (xxx.YYYYMMDD-HHMMSS): recover it when the
        // servlet did not send a duration point for the run
        _tagDate(tag) {
            var m = /(\d{4})(\d{2})(\d{2})-(\d{2})(\d{2})(\d{2})/.exec(tag || '');
            if (!m) return 0;
            var t = new Date(+m[1], +m[2] - 1, +m[3], +m[4], +m[5], +m[6]).getTime();
            return isNaN(t) ? 0 : t;
        },

        // ═══════════════════ COMPUTED ═══════════════════
        get kpis() {
            var runs = this.runs;
            var totalExe = 0, flaky = 0, okCi = 0, ciKnown = 0, durSum = 0, durN = 0;
            runs.forEach(function (r) {
                totalExe += r.nbExeU || 0;
                flaky += r.nbFlaky || 0;
                if (r.ciRes) { ciKnown++; if (r.ciRes === 'OK') okCi++; }
                if (r.durMs !== null && r.durMs > 0) { durSum += r.durMs; durN++; }
            });
            return {
                runs: runs.length,
                ciRate: ciKnown ? Math.round(okCi * 100 / ciKnown) : null,
                ciKnown: ciKnown,
                avgDur: durN ? Math.round(durSum / durN) : null,
                totalExe: totalExe,
                flaky: flaky
            };
        },
        get runsBarsSvg() {
            var IN = this.IN;
            var items = this.runs.map(function (r) {
                return {
                    label: r.t ? IN.fmtShortDate(r.t) : '?',
                    title: r.tag,
                    segments: r.segments,
                    tag: r.tag
                };
            });
            return IN.stackedBars(items, {
                W: 900, H: 210,
                onClickAttr: function (it) { return 'data-tag="' + IN.esc(it.tag) + '"'; }
            });
        },
        get durationSvg() {
            return this.IN.timeLines(this.durationSeries, { W: 900, H: 230, unit: 'duration' });
        },
        get ciSvg() {
            var IN = this.IN;
            var pts = [];
            this.runs.forEach(function (r) {
                if (!r.t || r.ciSc === undefined || r.ciSc === null) return;
                pts.push({
                    t: r.t, v: r.ciSc,
                    title: r.tag + ' - CI score ' + r.ciSc + ' / ' + (r.ciScT || 100) + ' (' + (r.ciRes || '-') + ')',
                    dotColor: r.ciRes === 'OK' ? IN.statusColors.OK : (r.ciRes === 'KO' ? IN.statusColors.KO : IN.statusColors.NA),
                    attr: 'data-tag="' + IN.esc(r.tag) + '"'
                });
            });
            return IN.timeLines([{ name: 'CI score', color: '#64748b', points: pts }], { W: 900, H: 230, unit: 'number' });
        },
        get tableRuns() {
            return this.runs.slice().reverse();
        },
        get legendStatuses() {
            var present = {};
            this.runs.forEach(function (r) { r.segments.forEach(function (s) { present[s.status] = true; }); });
            return this.IN.statusOrder.filter(function (s) { return present[s]; });
        },

        // ═══════════════════ ACTIONS ═══════════════════
        openRun(tag) {
            if (tag) window.location.href = './ReportingExecutionByTagV2.jsp?Tag=' + encodeURIComponent(tag);
        },
        chartClick(ev) {
            var el = ev.target.closest('[data-tag]');
            if (el) this.openRun(el.getAttribute('data-tag'));
        },

        // ═══════════════════ HELPERS (template shortcuts) ═══════════════════
        fmtDuration(ms) { return this.IN.fmtDuration(ms); },
        fmtDateTime(v) { return this.IN.fmtDateTime(v); },
        relTime(v) { return this.IN.relTime(v); },
        statusColor(s) { return this.IN.statusColor(s); }
    };
}
