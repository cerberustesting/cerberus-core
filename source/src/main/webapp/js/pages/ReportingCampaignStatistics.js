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
 * Campaign Statistics - Alpine dashboard over api/campaignexecutions/statistics.
 *
 * Overview level: one row per campaign over the period (runs, OK rate, duration,
 * reliability). Drill-down level: the same campaign split by environment x country.
 * URLs stay compatible with the legacy page (?campaign=&from=&to=).
 */
function campaignStatistics() {
    var IN = window.InsightsShared;
    return {
        // ── State ──
        loading: false,
        loaded: false,
        error: '',
        rows: [],
        view: 'overview',      // overview | detail
        search: '',
        sortCol: 'campaign',
        sortAsc: true,

        // ── Filters ──
        refSystems: [],
        selSystems: [],
        refApps: [],
        selApps: [],
        appsLoading: false,
        ddOpen: '',
        ddSearch: '',
        from: '',              // yyyy-mm-dd (input type=date)
        to: '',

        // ── Detail level ──
        detail: {
            campaign: '',
            loading: false,
            error: '',
            rows: [],
            envs: [],          // reference lists sent back by the endpoint
            countries: [],
            selEnvs: [],       // empty = all
            selCountries: []
        },

        IN: IN,

        // ═══════════════════ INIT ═══════════════════
        init() {
            var self = this;
            try {
                var user = JSON.parse(sessionStorage.getItem('user') || '{}');
                this.refSystems = (Array.isArray(user.system) ? user.system : []).slice().sort();
            } catch (e) { /* empty */ }
            this.selSystems = this.refSystems.slice();

            // default period: the last 30 days
            var now = new Date();
            this.to = this._dateStr(now);
            this.from = this._dateStr(new Date(now.getTime() - 30 * 86400000));

            // legacy-compatible deep link: ?campaign=&from=&to=(&environments=&countries=)
            var usp = null;
            try { usp = new URLSearchParams(window.location.search); } catch (e) { /* ignore */ }
            if (usp) {
                var f = this._isoToDateStr(usp.get('from'));
                var t = this._isoToDateStr(usp.get('to'));
                if (f) this.from = f;
                if (t) this.to = t;
            }

            this._loadApplications().then(function () {
                var campaign = usp ? usp.get('campaign') : null;
                if (campaign) {
                    self.openDetail(campaign);
                } else {
                    self.load();
                }
            });
        },
        _dateStr(d) {
            var p = function (n) { return (n < 10 ? '0' : '') + n; };
            return d.getFullYear() + '-' + p(d.getMonth() + 1) + '-' + p(d.getDate());
        },
        _isoToDateStr(v) {
            if (!v) return '';
            var d = new Date(decodeURIComponent(v));
            return isNaN(d.getTime()) ? '' : this._dateStr(d);
        },
        _fromIso() {
            var d = new Date(this.from + 'T00:00:00');
            return isNaN(d.getTime()) ? new Date(Date.now() - 30 * 86400000).toISOString() : d.toISOString();
        },
        _toIso() {
            var d = new Date(this.to + 'T23:59:59');
            return isNaN(d.getTime()) ? new Date().toISOString() : d.toISOString();
        },
        _loadApplications() {
            var self = this;
            this.appsLoading = true;
            var q = this.selSystems.map(function (s) { return 'system=' + encodeURIComponent(s); }).join('&');
            return new Promise(function (resolve) {
                if (!self.selSystems.length) { self.refApps = []; self.selApps = []; self.appsLoading = false; resolve(); return; }
                $.getJSON('ReadApplication?' + q, function (data) {
                    self.refApps = ((data && data.contentTable) || []).map(function (a) { return a.application; }).filter(Boolean).sort();
                    // keep only still-valid selections; default = everything
                    var prev = self.selApps.filter(function (a) { return self.refApps.indexOf(a) >= 0; });
                    self.selApps = prev.length ? prev : self.refApps.slice();
                    self.appsLoading = false;
                    resolve();
                }).fail(function () { self.appsLoading = false; resolve(); });
            });
        },

        // ═══════════════════ FILTER DROPDOWNS (same pattern as the monitor) ═══════════════════
        openDd(name) {
            this.ddOpen = this.ddOpen === name ? '' : name;
            this.ddSearch = '';
            if (this.ddOpen) {
                this.$nextTick(function () {
                    var input = document.querySelector('#csFilters .v2in-dd input');
                    if (input && input.offsetParent !== null) input.focus();
                });
            }
        },
        ddItems(name) {
            var q = this.ddSearch.trim().toLowerCase();
            var match = function (s) { return !q || String(s).toLowerCase().indexOf(q) >= 0; };
            return (name === 'system' ? this.refSystems : this.refApps).filter(match);
        },
        selOf(name) { return name === 'system' ? this.selSystems : this.selApps; },
        toggleSel(name, v) {
            var arr = this.selOf(name);
            var i = arr.indexOf(v);
            if (i >= 0) arr.splice(i, 1); else arr.push(v);
            if (name === 'system') this._loadApplications();
        },
        allSel(name, on) {
            var ref = name === 'system' ? this.refSystems : this.refApps;
            var arr = this.selOf(name);
            arr.splice(0, arr.length);
            if (on) ref.forEach(function (v) { arr.push(v); });
            if (name === 'system') this._loadApplications();
        },
        pickerLabel(name) {
            var arr = this.selOf(name);
            var ref = name === 'system' ? this.refSystems : this.refApps;
            var noun = name === 'system' ? 'system' : 'application';
            if (!arr.length) return 'No ' + noun;
            if (arr.length === ref.length) return 'All ' + noun + 's (' + ref.length + ')';
            if (arr.length === 1) return arr[0];
            return arr.slice(0, 2).join(', ') + (arr.length > 2 ? ' +' + (arr.length - 2) : '');
        },

        // ═══════════════════ OVERVIEW LOAD ═══════════════════
        load() {
            var self = this;
            if (!this.selSystems.length || !this.selApps.length) {
                showMessageMainPage('warning', 'Pick at least one system and one application first.');
                return;
            }
            this.view = 'overview';
            this.loading = true;
            this.error = '';
            if (typeof InsertURLInHistory === 'function') InsertURLInHistory('./ReportingCampaignStatistics.jsp');
            $.ajax({
                url: 'api/campaignexecutions/statistics',
                method: 'POST',
                contentType: 'application/json',
                data: JSON.stringify({
                    systems: this.selSystems.map(encodeURIComponent).join(','),
                    applications: this.selApps.map(encodeURIComponent).join(','),
                    group1: '',
                    from: encodeURIComponent(this._fromIso()),
                    to: encodeURIComponent(this._toIso())
                })
            }).done(function (data) {
                self.loading = false;
                self.loaded = true;
                self.rows = (data && data.campaignStatistics) || [];
            }).fail(function (xhr) {
                self.loading = false;
                self.loaded = true;
                self.rows = [];
                if (xhr.status === 404) {
                    // the API answers 404 when nothing matches: that is the empty state, not an error
                    self.error = '';
                    return;
                }
                var msg = 'Could not load the campaign statistics (' + xhr.status + ').';
                try { msg = JSON.parse(xhr.responseText).message || msg; } catch (e) { /* keep default */ }
                self.error = msg;
            });
        },

        // ═══════════════════ OVERVIEW TABLE ═══════════════════
        get filteredRows() {
            var q = this.search.trim().toLowerCase();
            var rows = this.rows;
            if (q) {
                rows = rows.filter(function (r) {
                    return String(r.campaign || '').toLowerCase().indexOf(q) >= 0
                        || String(r.systemList || '').toLowerCase().indexOf(q) >= 0
                        || String(r.applicationList || '').toLowerCase().indexOf(q) >= 0
                        || String(r.campaignGroup1 || '').toLowerCase().indexOf(q) >= 0;
                });
            }
            var col = this.sortCol, asc = this.sortAsc ? 1 : -1;
            return rows.slice().sort(function (a, b) {
                var x = a[col], y = b[col];
                if (x === undefined || x === null) return 1;
                if (y === undefined || y === null) return -1;
                if (typeof x === 'number' && typeof y === 'number') return (x - y) * asc;
                return String(x).localeCompare(String(y)) * asc;
            });
        },
        setSort(col) {
            if (this.sortCol === col) this.sortAsc = !this.sortAsc;
            else { this.sortCol = col; this.sortAsc = col === 'campaign'; }
        },
        get kpis() {
            var n = this.rows.length;
            var runs = 0, okW = 0, relW = 0, durW = 0;
            this.rows.forEach(function (r) {
                var w = r.nbCampaignExecutions || 0;
                runs += w;
                okW += (r.avgOK || 0) * w;
                relW += (r.avgReliability || 0) * w;
                durW += (r.avgDuration || 0) * w;
            });
            return {
                campaigns: n,
                runs: runs,
                okRate: runs ? Math.round(okW / runs * 10) / 10 : null,
                reliability: runs ? Math.round(relW / runs * 10) / 10 : null,
                avgDur: runs ? Math.round(durW / runs) : null
            };
        },

        // ═══════════════════ DETAIL LEVEL ═══════════════════
        openDetail(campaign) {
            this.detail.campaign = campaign;
            this.detail.rows = [];
            this.detail.envs = [];
            this.detail.countries = [];
            this.detail.selEnvs = [];
            this.detail.selCountries = [];
            this.detail.error = '';
            this.view = 'detail';
            this.loadDetail();
        },
        loadDetail() {
            var self = this;
            var d = this.detail;
            d.loading = true;
            d.error = '';
            var params = {
                environments: d.selEnvs.map(encodeURIComponent).join(','),
                countries: d.selCountries.map(encodeURIComponent).join(','),
                from: encodeURIComponent(this._fromIso()),
                to: encodeURIComponent(this._toIso())
            };
            if (typeof InsertURLInHistory === 'function') {
                InsertURLInHistory('./ReportingCampaignStatistics.jsp?campaign=' + encodeURIComponent(d.campaign)
                    + '&from=' + encodeURIComponent(this._fromIso()) + '&to=' + encodeURIComponent(this._toIso()));
            }
            $.ajax({
                url: 'api/campaignexecutions/statistics/' + encodeURIComponent(d.campaign),
                method: 'GET',
                data: params
            }).done(function (data) {
                d.loading = false;
                d.rows = (data && data.campaignStatistics) || [];
                if (data && Array.isArray(data.environments) && data.environments.length) d.envs = data.environments;
                if (data && Array.isArray(data.countries) && data.countries.length) d.countries = data.countries;
            }).fail(function (xhr) {
                d.loading = false;
                d.rows = [];
                if (xhr.status === 404) {
                    // nothing ran for this campaign on the period: empty state, not an error
                    d.error = '';
                    return;
                }
                var msg = 'Could not load the detail of ' + d.campaign + ' (' + xhr.status + ').';
                try { msg = JSON.parse(xhr.responseText).message || msg; } catch (e) { /* keep default */ }
                d.error = msg;
            });
        },
        closeDetail() {
            this.view = 'overview';
            this.detail.campaign = '';
            if (typeof InsertURLInHistory === 'function') InsertURLInHistory('./ReportingCampaignStatistics.jsp');
            if (!this.loaded) this.load();
        },
        toggleDetailFilter(kind, v) {
            var arr = kind === 'env' ? this.detail.selEnvs : this.detail.selCountries;
            var i = arr.indexOf(v);
            if (i >= 0) arr.splice(i, 1); else arr.push(v);
            this.loadDetail();
        },

        // ═══════════════════ CROSS NAVIGATION ═══════════════════
        openTrends(r) {
            window.open('./ReportingCampaignOverTime.jsp?campaigns=' + encodeURIComponent(r.campaign), '_blank');
        },

        // ═══════════════════ HELPERS ═══════════════════
        rateColor(pct) {
            if (pct === undefined || pct === null) return 'var(--crb-grey-color)';
            if (pct >= 90) return 'var(--crb-green-color, #00d27a)';
            if (pct >= 75) return '#84cc16';
            if (pct >= 50) return 'var(--crb-orange-color, #f5803e)';
            return 'var(--crb-red-color, #e63757)';
        },
        pctF(v) { return (v === undefined || v === null) ? '-' : (Math.round(v * 10) / 10) + '%'; },
        durF(sec) { return (sec === undefined || sec === null) ? '-' : this.IN.fmtDuration(Math.round(sec) * 1000); },
        dateF(v) {
            if (!v) return '-';
            var d = new Date(v);
            return isNaN(d.getTime()) ? '-' : d.toLocaleString();
        },
        relTime(v) { return this.IN.relTime(v); }
    };
}
