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
 * My Monitor - a dashboard made of widgets placed on a grid.
 *
 * The widgets are modules (js/widgets: widgetXxx.js + widgetXxx.html) listed in the catalog of
 * MyMonitor (widgetRegistry.js). This page owns what they share: the dashboards, the grid
 * (placement, drag and resize in edit mode), the period and the refresh.
 *
 * Dashboards are saved in the browser (localStorage). Like the favorites of the Web Monitor, they
 * are tabs; the starred one is the one opened first.
 */
function myMonitor() {
    var MM = window.MyMonitor;
    var STORAGE_KEY = 'myMonitor';
    var LEGACY_KEY = 'widgets';     // the first version of the page kept its widgets alone under this key
    var COLS = 12;
    var ROW_H = 76;
    var TV_REFRESH_MS = 60000;

    function uid() { return 'w' + Date.now().toString(36) + Math.random().toString(36).slice(2, 6); }

    function overlap(a, b) {
        return a.x < b.x + b.w && b.x < a.x + a.w && a.y < b.y + b.h && b.y < a.y + a.h;
    }

    // First free place of a w x h widget, scanning the grid from the top left.
    function freeSlot(widgets, w, h) {
        for (var y = 0; ; y++) {
            for (var x = 0; x + w <= COLS; x++) {
                var probe = {x: x, y: y, w: w, h: h};
                if (!widgets.some(function (o) { return overlap(probe, o); })) { return {x: x, y: y}; }
            }
        }
    }

    function makeWidget(type, widgets, title) {
        var def = MM.catalog[type];
        var slot = freeSlot(widgets, def.w, def.h);
        return Object.assign({id: uid(), type: type, title: title || '', icon: def.icon, color: def.color, x: slot.x, y: slot.y, w: def.w, h: def.h}, def.defaults());
    }

    function starter() {
        var widgets = [];
        ['applications', 'testcases', 'campaigns', 'executions'].forEach(function (source) {
            var w = makeWidget('kpi', widgets);
            var src = MM.kpiSources[source];
            Object.assign(w, {source: source, metric: src.metrics[0], icon: src.icon, color: src.color});
            widgets.push(w);
        });
        return widgets;
    }

    // The widgets of the first version: only the counters and the notes have a successor.
    function fromLegacy(legacy) {
        var sources = {Application: 'applications', Testcase: 'testcases', Execution: 'executions'};
        var widgets = [];
        legacy.forEach(function (old) {
            if (old.type === 'count' && sources[old.option]) {
                var w = makeWidget('kpi', widgets);
                var src = MM.kpiSources[sources[old.option]];
                Object.assign(w, {source: sources[old.option], metric: src.metrics[0], title: old.title || '', icon: src.icon, color: src.color});
                widgets.push(w);
            } else if (old.type === 'text') {
                widgets.push(makeWidget('text', widgets, old.title));
            }
        });
        return widgets;
    }

    return {
        dashboards: [],
        currentId: null,
        editMode: false,
        periodDays: 7,
        periods: [{label: '7d', days: 7}, {label: '30d', days: 30}, {label: '90d', days: 90}],
        refreshTick: 0,
        loadedAt: 0,
        pickerOpen: false,
        tvMode: false,
        drag: null,
        colors: MM.COLORS,
        icons: MM.ICONS,

        init() {
            document.title = this.t('title');
            this.watchIcons();
            this.restore();
            this.open(this.dashboards.find(function (d) { return d.favorite; }) || this.dashboards[0]);
            this.loadedAt = Date.now();
            this.initTv();
        },
        // ── TV mode: the dashboard alone on the whole screen, refreshed every minute ──
        initTv() {
            document.addEventListener('keydown', (e) => { if (e.key === 'Escape' && this.tvMode) { this.exitTv(); } });
            this.$watch('tvMode', (on) => {
                document.documentElement.classList.toggle('v2mo-tv-on', on);
                clearInterval(this._tvTimer);
                if (on) { this._tvTimer = setInterval(() => this.refresh(), TV_REFRESH_MS); }
                if (typeof InsertURLInHistory === 'function') { InsertURLInHistory('./MyMonitor.jsp' + (on ? '?tv=true' : '')); }
            });
            if (new URLSearchParams(location.search).get('tv') === 'true') { this.tvMode = true; }
        },
        enterTv() {
            this.editMode = false;
            this.pickerOpen = false;
            this.save();
            this.tvMode = true;
        },
        exitTv() { this.tvMode = false; },
        // The icons (lucide) are drawn once: the ones rendered later by x-if / x-for need another pass.
        watchIcons() {
            var scheduled = false;
            new MutationObserver(function () {
                if (scheduled || !window.lucide) { return; }
                scheduled = true;
                requestAnimationFrame(function () { scheduled = false; lucide.createIcons(); });
            }).observe(this.$el, {childList: true, subtree: true});
        },
        t: MM.t,
        ago(ts) { return MM.IN.relTime(ts); },

        // ── Dashboards ──
        restore() {
            var saved = null;
            try { saved = JSON.parse(localStorage.getItem(STORAGE_KEY)); } catch (e) { saved = null; }
            if (saved && Array.isArray(saved.dashboards) && saved.dashboards.length) {
                this.dashboards = saved.dashboards.filter(function (d) { return Array.isArray(d.widgets); });
                // The first charts became the timeline, which draws the status or the duration.
                var renamed = {executionStatus: 'status', executionDuration: 'duration'};
                this.dashboards.forEach(function (d) {
                    d.widgets.forEach(function (w) {
                        if (renamed[w.type]) { w.metric = renamed[w.type]; w.type = 'timeline'; }
                    });
                });
            }
            if (!this.dashboards.length) {
                var legacy = null;
                try { legacy = JSON.parse(localStorage.getItem(LEGACY_KEY)); } catch (e) { legacy = null; }
                var widgets = Array.isArray(legacy) ? fromLegacy(legacy) : [];
                this.dashboards = [{id: uid(), name: this.t('mydashboard'), favorite: true, period: 7, widgets: widgets.length ? widgets : starter()}];
                this.save();
            }
        },
        save() {
            localStorage.setItem(STORAGE_KEY, JSON.stringify({dashboards: this.dashboards}));
        },
        get current() {
            return this.dashboards.find((d) => d.id === this.currentId) || null;
        },
        get widgets() {
            return this.current ? this.current.widgets : [];
        },
        open(d) {
            if (!d) { return; }
            this.currentId = d.id;
            this.periodDays = d.period || 7;
            this.pickerOpen = false;
        },
        addDashboard() {
            var d = {id: uid(), name: this.t('newdashboard', this.dashboards.length + 1), favorite: false, period: 7, widgets: []};
            this.dashboards.push(d);
            this.open(d);
            this.editMode = true;
            this.save();
        },
        removeDashboard() {
            if (!this.current || !confirm(this.t('confirmdelete', this.current.name))) { return; }
            var id = this.current.id;
            this.dashboards = this.dashboards.filter(function (d) { return d.id !== id; });
            if (!this.dashboards.length) {
                this.dashboards = [{id: uid(), name: this.t('mydashboard'), favorite: true, period: 7, widgets: []}];
            }
            this.open(this.dashboards[0]);
            this.save();
        },
        toggleFavorite(d) {
            var on = !d.favorite;
            this.dashboards.forEach(function (o) { o.favorite = false; });
            d.favorite = on;
            this.save();
        },

        // ── Period and refresh ──
        setPeriod(days) {
            this.periodDays = days;
            if (this.current) { this.current.period = days; }
            this.save();
        },
        refresh() {
            MM.invalidate();
            this.refreshTick++;
            this.loadedAt = Date.now();
        },

        // ── Widgets ──
        get catalog() {
            return MM.order.map(function (type) { return MM.catalog[type]; });
        },
        addWidget(type) {
            if (!this.current) { return; }
            var w = makeWidget(type, this.current.widgets);
            MM.freshId = w.id;   // its card opens the settings
            this.current.widgets.push(w);
            this.pickerOpen = false;
            this.save();
        },
        deleteWidget(id) {
            this.current.widgets = this.current.widgets.filter(function (w) { return w.id !== id; });
            this.save();
        },
        toggleEdit() {
            this.editMode = !this.editMode;
            this.pickerOpen = false;
            if (!this.editMode) { this.save(); }
        },

        // ── Grid ──
        gridStyle() {
            var rows = 0;
            this.widgets.forEach(function (w) { rows = Math.max(rows, w.y + w.h); });
            if (this.editMode) { rows += 2; }
            return {'--mm-row': ROW_H + 'px', height: Math.max(rows, 2) * ROW_H + 'px'};
        },
        cellStyle(w) {
            return {
                left: (w.x * 100 / COLS) + '%', width: (w.w * 100 / COLS) + '%',
                top: (w.y * ROW_H) + 'px', height: (w.h * ROW_H) + 'px'
            };
        },
        // mode: 'move' (header of the card) or 'resize' (corner)
        startDrag(e, w, mode) {
            if (!this.editMode || e.button > 0) { return; }
            e.preventDefault();
            var grid = this.$refs.grid;
            this.drag = {w: w, mode: mode, sx: e.clientX, sy: e.clientY, ox: w.x, oy: w.y, ow: w.w, oh: w.h, colW: grid.clientWidth / COLS};
            this._move = (ev) => this.onDrag(ev);
            this._up = () => this.endDrag();
            window.addEventListener('pointermove', this._move);
            window.addEventListener('pointerup', this._up);
        },
        onDrag(e) {
            var d = this.drag;
            if (!d) { return; }
            var def = MM.catalog[d.w.type];
            var dx = Math.round((e.clientX - d.sx) / d.colW);
            var dy = Math.round((e.clientY - d.sy) / ROW_H);
            if (d.mode === 'move') {
                d.w.x = Math.min(Math.max(0, d.ox + dx), COLS - d.w.w);
                d.w.y = Math.max(0, d.oy + dy);
            } else {
                d.w.w = Math.min(Math.max(def.minW || 2, d.ow + dx), COLS - d.w.x);
                d.w.h = Math.max(def.minH || 1, d.oh + dy);
            }
        },
        endDrag() {
            window.removeEventListener('pointermove', this._move);
            window.removeEventListener('pointerup', this._up);
            if (this.drag) { this.resolve(this.drag.w); this.save(); }
            this.drag = null;
        },
        // The widget that was dropped keeps its place: the ones it overlaps are pushed down.
        resolve(moved) {
            var widgets = this.widgets;
            var queue = [moved];
            for (var guard = 0; queue.length && guard < 1000; guard++) {
                var a = queue.shift();
                widgets.forEach(function (b) {
                    if (b !== a && overlap(a, b)) {
                        b.y = a.y + a.h;
                        queue.push(b);
                    }
                });
            }
        }
    };
}

/**
 * The card around a widget: header (icon, title), settings common to every widget (title, icon,
 * color) and the Done button. The body of the widget is nested in it and reads cfg.
 */
function widgetShell(w) {
    var MM = window.MyMonitor;
    return {
        cfg: MM.freshId === w.id,
        init() { if (MM.freshId === w.id) { MM.freshId = null; } },
        get label() { return MM.t('widget_' + w.type); },
        hex(color) { return MM.COLORS[color] || MM.COLORS.blue; }
    };
}
