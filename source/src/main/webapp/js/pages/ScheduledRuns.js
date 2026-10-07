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
 * Scheduled Runs - the campaigns planned on a cron (scheduleentry table) and what the
 * scheduler did with them (scheduledexecution table).
 *
 * One call to ReadScheduledRuns gives the schedule entries, their next fire times
 * (computed on the server from the cron, in the server time zone) and the history of the
 * last days. The KPIs, the filters and the per-schedule success rate are computed here
 * from that payload. Creating, pausing and deleting go through the existing
 * CreateScheduleEntry / UpdateScheduleEntry / DeleteScheduleEntry servlets.
 *
 * The success rate is the one of the trigger (campaign accepted by the execution queue
 * or not), not the OK rate of the tests of the campaign - see the campaign trends pages
 * for the latter.
 */
function scheduledRuns() {
    var DAY_MS = 86400000;
    return {
        days: 7,
        filterCampaigns: [],      // empty = every campaign
        filterStatus: '',
        historyLimit: 100,
        loading: false,
        saving: false,
        error: '',
        loadedAt: 0,
        nowTick: Date.now(),
        serverTimeZone: '',
        entries: [],
        upcoming: [],
        history: [],
        campaigns: [],
        _campaignsReady: null,
        formOpen: false,
        form: { name: '', cronDefinition: '', description: '' },
        presets: [
            { label: 'preset15', cron: '0 0/15 * * * ?' },
            { label: 'presethourly', cron: '0 0 * * * ?' },
            { label: 'presetdaily', cron: '0 0 2 * * ?' },
            { label: 'presetweekdays', cron: '0 0 8 ? * MON-FRI' },
            { label: 'presetsunday', cron: '0 0 22 ? * SUN' }
        ],
        _timers: [],

        init() {
            var self = this;
            document.title = this.t('title');
            this.load();
            this._campaignsReady = $.getJSON('ReadCampaign').then(function (data) {
                self.campaigns = ((data && data.contentTable) || []).slice().sort(function (a, b) {
                    return a.campaign.localeCompare(b.campaign);
                });
                return self.campaigns;
            });
            window.addEventListener('sr-campaign-change', function (e) {
                self.filterCampaigns = e.detail || [];
                self.historyLimit = 100;
            });
            this._timers.push(setInterval(function () { self.nowTick = Date.now(); }, 30000));
            this._timers.push(setInterval(function () { self.load(); }, 60000));
        },

        load() {
            var self = this;
            this.loading = true;
            $.getJSON('ReadScheduledRuns', { days: this.days, next: 5 }, function (data) {
                if (data.messageType !== 'OK') {
                    self.error = self.t('loaderror', data.message || '').trim();
                    return;
                }
                self.error = '';
                self.entries = data.entries || [];
                self.upcoming = data.upcoming || [];
                self.history = data.history || [];
                self.serverTimeZone = data.serverTimeZone || '';
                self.loadedAt = Date.now();
                self.nowTick = Date.now();
            }).fail(function () {
                self.error = self.t('loadunreachable');
            }).always(function () {
                self.loading = false;
            });
        },

        setDays(d) {
            this.days = d;
            this.historyLimit = 100;
            this.load();
        },

        toggleForm() {
            this.formOpen = !this.formOpen;
        },

        // ── Labels (labels.js, pageScheduledRuns) - {0}, {1}... are replaced by the extra arguments ──
        t(key) {
            var text = Alpine.store('labels').getLabel('pageScheduledRuns', key);
            for (var i = 1; i < arguments.length; i++) {
                text = text.split('{' + (i - 1) + '}').join(arguments[i]);
            }
            return text;
        },

        // ── Derived data ──
        // Items of the multi select dropdown of the campaign filter
        campaignItems() {
            return this._campaignsReady.then(function (campaigns) {
                return campaigns.map(function (c) { return { name: c.campaign }; });
            });
        },
        _matchesCampaign(name) {
            return this.filterCampaigns.length === 0 || this.filterCampaigns.indexOf(name) >= 0;
        },
        get upcomingList() {
            var self = this;
            return this.upcoming.filter(function (u) { return self._matchesCampaign(u.name); }).slice(0, 15);
        },
        get historyRows() {
            var self = this;
            return this.history.filter(function (h) {
                return self._matchesCampaign(h.name) && (!self.filterStatus || h.status === self.filterStatus);
            });
        },
        get entryRows() {
            var self = this;
            var stats = {};
            this.history.forEach(function (h) {
                var s = stats[h.schedulerId] || (stats[h.schedulerId] = { ok: 0, ko: 0, last: 0 });
                if (h.status === 'TRIGGERED') { s.ok++; } else if (h.status === 'ERROR') { s.ko++; }
                if (h.scheduledDate && h.scheduledDate > s.last) { s.last = h.scheduledDate; }
            });
            return this.entries.filter(function (e) { return self._matchesCampaign(e.name); }).map(function (e) {
                var s = stats[e.id] || { ok: 0, ko: 0, last: 0 };
                return $.extend({}, e, {
                    ok: s.ok,
                    ko: s.ko,
                    rate: (s.ok + s.ko) > 0 ? Math.round(100 * s.ok / (s.ok + s.ko)) : null,
                    lastRun: e.lastExecution || s.last || 0
                });
            });
        },
        get kpis() {
            var entries = this.entries.filter(this._entryFilter());
            var history = this.history.filter(this._historyFilter());
            var ok = 0, ko = 0, lastError = '';
            history.forEach(function (h) {
                if (h.status === 'TRIGGERED') { ok++; } else if (h.status === 'ERROR') { ko++; if (!lastError) { lastError = h.name; } }
            });
            var next = this.upcomingList[0];
            return {
                total: entries.length,
                active: entries.filter(function (e) { return e.active; }).length,
                nextIn: next ? this.until(next.time) : '-',
                nextName: next ? next.name : this.t('kpinextnone'),
                fired: history.length,
                errors: ko,
                lastError: lastError,
                successRate: (ok + ko) > 0 ? Math.round(100 * ok / (ok + ko)) : null
            };
        },
        _entryFilter() {
            var self = this;
            return function (e) { return self._matchesCampaign(e.name); };
        },
        _historyFilter() {
            var self = this;
            return function (h) { return self._matchesCampaign(h.name); };
        },

        // ── Display ──
        fmt(ts) {
            return ts ? window.InsightsShared.fmtDateTime(ts) : '-';
        },
        ago(ts) {
            var s = Math.max(0, Math.floor((this.nowTick - ts) / 1000));
            if (s < 60) { return this.t('justnow'); }
            return this.t('ago', window.InsightsShared.fmtDuration(s * 1000));
        },
        until(ts) {
            var ms = ts - this.nowTick;
            if (ms <= 0) { return this.t('now'); }
            return this.t('inx', window.InsightsShared.fmtDuration(ms));
        },
        statusClass(status) {
            if (status === 'TRIGGERED') { return 'v2in-chip--ok'; }
            if (status === 'ERROR') { return 'v2in-chip--ko'; }
            if (status === 'IGNORED') { return 'v2in-chip--info'; }
            return 'v2in-chip--warn'; // TOLAUNCH: the job was created but never finished
        },

        // ── Actions ──
        _call(servlet, data, done) {
            var self = this;
            this.saving = true;
            $.ajax({ url: servlet, method: 'POST', data: data, dataType: 'json' }).done(function (r) {
                if (r.messageType === 'OK') {
                    done();
                    self.load();
                } else {
                    showMessageMainPage('danger', r.message || self.t('opfailed'), false);
                }
            }).fail(function () {
                showMessageMainPage('danger', self.t('opunreachable'), false);
            }).always(function () {
                self.saving = false;
            });
        },
        create() {
            var self = this;
            if (!this.form.name || !this.form.cronDefinition.trim()) {
                showMessageMainPage('warning', this.t('pickboth'), false);
                return;
            }
            this._call('CreateScheduleEntry', {
                name: this.form.name,
                type: 'CAMPAIGN',
                cronDefinition: this.form.cronDefinition.trim(),
                description: this.form.description,
                active: 'Y'
            }, function () {
                self.form = { name: '', cronDefinition: '', description: '' };
                self.formOpen = false;
            });
        },
        setActive(entry, active) {
            this._call('UpdateScheduleEntry', {
                id: entry.id,
                name: entry.name,
                type: entry.type,
                cronDefinition: entry.cronDefinition,
                active: active ? 'Y' : 'N'
            }, function () {});
        },
        remove(entry) {
            if (!confirm(this.t('confirmdelete', entry.cronDefinition, entry.name))) {
                return;
            }
            this._call('DeleteScheduleEntry', { id: entry.id }, function () {});
        }
    };
}
