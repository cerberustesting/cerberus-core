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
 * Widget "kpi": one figure, like a card of the homepage. The figures come from /api/monitor/kpi.
 * Options: w.source (applications, testcases, campaigns, executions), w.metric (one of the metrics
 * of the source) and w.scope (system: the selected systems, global: all of them).
 * A figure over a period (created, launched, executed) follows the period of the dashboard and
 * is compared with the same number of days just before.
 */
(function () {
    var MM = window.MyMonitor;

    // metrics: the figures of the source, the first one is the default. over: the ones counted over the period.
    var SOURCES = {
        applications: {metrics: ['total', 'created'], icon: 'layout-grid', color: 'blue'},
        testcases: {metrics: ['total', 'working', 'created'], icon: 'file-text', color: 'green'},
        campaigns: {metrics: ['total', 'launched'], icon: 'globe', color: 'purple'},
        executions: {metrics: ['executed'], icon: 'play', color: 'orange'}
    };
    var OVER_PERIOD = ['created', 'launched', 'executed'];

    MM.register({
        type: 'kpi', icon: 'layout-grid', color: 'blue', w: 3, h: 2, minW: 2, minH: 2,
        defaults: function () { return {source: 'applications', metric: 'total', scope: 'system'}; }
    });
    MM.kpiSources = SOURCES;

    window.widgetKpi = function (w) {
        return {
            sources: Object.keys(SOURCES),
            data: null,
            error: '',
            loading: false,

            init() {
                this.load();
                this.$watch('periodDays', () => this.load());
                this.$watch('refreshTick', () => this.load());
                this.$watch('cfg', (open) => { if (!open) { this.load(); } });
            },
            load() {
                var self = this;
                this.error = '';
                var since = Date.now();
                this.loading = true;
                MM.kpi(w.source, this.periodDays).then(function (data) {
                    self.data = data;
                }).catch(function (e) {
                    self.data = null;
                    self.error = e.message;
                }).finally(function () { MM.endLoader(since, function () { self.loading = false; }); });
            },

            // The metric of the settings, or the first one of the source when it is not one of its metrics.
            get metrics() { return (SOURCES[w.source] || SOURCES.applications).metrics; },
            get metric() { return this.metrics.indexOf(w.metric) >= 0 ? w.metric : this.metrics[0]; },
            get overPeriod() { return OVER_PERIOD.indexOf(this.metric) >= 0; },
            get scopeData() { return this.data ? this.data[w.scope === 'global' ? 'global' : 'system'] : null; },
            get raw() {
                var s = this.scopeData;
                return s && s.current && s.current[this.metric] !== undefined ? s.current[this.metric] : null;
            },
            get value() { return this.raw === null ? '-' : MM.fmtNum(this.raw); },
            get diff() {
                var s = this.scopeData;
                if (!this.overPeriod || this.raw === null || !s.previous || s.previous[this.metric] === undefined) { return null; }
                return this.raw - s.previous[this.metric];
            },
            get diffText() { return (this.diff > 0 ? '\u25B2 +' : this.diff < 0 ? '\u25BC ' : '= ') + MM.fmtNum(this.diff); },
            get diffColor() { return this.diff > 0 ? MM.COLORS.green : this.diff < 0 ? MM.COLORS.red : 'var(--crb-grey-color)'; },
            get sub() {
                var label = this.metricLabel(this.metric);
                if (!this.overPeriod) { return label; }
                return label + ' - ' + MM.t('kpi_period', this.periodDays) + (this.diff === null ? '' : ' (' + MM.t('kpi_vsprevious', this.periodDays) + ')');
            },
            metricLabel(k) { return MM.t('kpi_' + k); },
            sourceLabel(s) { return MM.t('kpi_source_' + s); },
            // Picking another source: the card takes its icon and color, unless they were customised.
            pickSource(s) {
                var before = SOURCES[w.source] || {};
                if (w.icon === before.icon) { w.icon = SOURCES[s].icon; }
                if (w.color === before.color) { w.color = SOURCES[s].color; }
                w.source = s;
                w.metric = SOURCES[s].metrics[0];
            }
        };
    };
})();
