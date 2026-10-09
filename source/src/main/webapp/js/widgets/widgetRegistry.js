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
 * My Monitor - registry shared by the widgets.
 *
 * A widget is a module made of two files: widgetXxx.js (the Alpine component and its
 * registration in the catalog below) and widgetXxx.html (its body, included by MyMonitor.jsp).
 * The page gives the card (header, drag, resize, settings: title, icon, color) and the
 * body only draws what is inside: a view and its own settings.
 *
 * What a widget finds in its scope (it is nested in the page and in its card):
 *   w            the widget (saved with the dashboard): w.title, w.icon, w.color, and its own options
 *   periodDays   the period of the dashboard
 *   refreshTick  incremented by the Refresh button
 *   cfg          true while the settings of the card are open
 */
window.MyMonitor = (function () {
    var IN = window.InsightsShared;
    var CACHE_MS = 60000;
    var cache = {};
    var catalog = {};
    var order = [];

    // Same hues as the cards of the homepage and the charts of the reporting pages.
    var COLORS = {blue: '#2c7be5', green: '#00d27a', purple: '#8b5cf6', orange: '#f5803e', pink: '#d946ef', cyan: '#0891b2', amber: '#f59e0b', red: '#e63757'};
    var ICONS = ['star', 'layout-grid', 'file-text', 'globe', 'play', 'sparkles', 'activity', 'chart-column', 'chart-line', 'chart-pie', 'radar', 'timer', 'server', 'calendar', 'bell', 'shield-check'];

    /**
     * def: {type, icon, color, w, h, minW, minH, defaults(): own options of the widget}.
     * Labels widget_<type> and widget_<type>_desc are in labels.js (pageMyMonitor).
     */
    function register(def) {
        catalog[def.type] = def;
        if (order.indexOf(def.type) < 0) { order.push(def.type); }
    }

    function t(key) {
        var text = Alpine.store('labels').getLabel('pageMyMonitor', key);
        for (var i = 1; i < arguments.length; i++) {
            text = text.split('{' + (i - 1) + '}').join(arguments[i]);
        }
        return text;
    }

    // One call per key and per minute: widgets reading the same data share it.
    function cached(key, fetcher) {
        var e = cache[key];
        if (e && Date.now() - e.at < CACHE_MS) { return e.promise; }
        var promise = fetcher();
        cache[key] = {at: Date.now(), promise: promise};
        promise.catch(function () { delete cache[key]; });
        return promise;
    }

    function invalidate() { cache = {}; }

    function getJSON(url, params) {
        return new Promise(function (resolve, reject) {
            $.getJSON(url, params).done(resolve).fail(function (xhr) { reject(new Error(url + ' (' + xhr.status + ')')); });
        });
    }

    /** Figures of a KPI widget over the last days (MonitorPrivateController). */
    function kpi(source, days) {
        return cached('kpi/' + source + '/' + days, function () {
            return fetch('./api/monitor/kpi?source=' + encodeURIComponent(source) + '&days=' + days + '&' + getUser().defaultSystemsQuery).then(function (r) {
                if (!r.ok) { throw new Error(source + ' (' + r.status + ')'); }
                return r.json();
            });
        });
    }

    /** ReadExecutionStat of a list of test cases ([{test, testCase}]) over the last days (same call as Execution Trends). */
    function executionStats(pairs, days) {
        var key = 'exe/' + days + '/' + pairs.map(function (p) { return p.test + '|' + p.testCase; }).join(',');
        return cached(key, function () {
            var params = 'from=' + encodeURIComponent(new Date(Date.now() - days * 86400000).toISOString())
                + '&to=' + encodeURIComponent(new Date().toISOString());
            pairs.forEach(function (p) { params += '&tests=' + encodeURIComponent(p.test) + '&testcases=' + encodeURIComponent(p.testCase); });
            return getJSON('ReadExecutionStat?' + params).then(function (data) {
                if (!data || data.messageType !== 'OK') { throw new Error((data && data.message) || 'ReadExecutionStat'); }
                return data;
            });
        });
    }

    function executionStat(test, testCase, days) { return executionStats([{test: test, testCase: testCase}], days); }

    /** Test case selection of the widgets that follow one: spread it in the component. */
    function testCasePicker(w) {
        return {
            tests: [],
            testcases: [],
            loadTests() {
                var self = this;
                cached('tests', function () { return getJSON('ReadTest'); }).then(function (data) {
                    self.tests = (data.contentTable || []).map(function (x) { return x.test; }).filter(Boolean).sort();
                });
                if (w.test) { this.loadTestCases(w.test); }
            },
            loadTestCases(test) {
                var self = this;
                this.testcases = [];
                if (!test) { return; }
                cached('testcases/' + test, function () { return getJSON('ReadTestCase', 'test=' + encodeURIComponent(test)); }).then(function (data) {
                    self.testcases = (data.contentTable || []).map(function (x) { return x.testcase || x.testCase; }).filter(Boolean).sort();
                });
            },
            pickTest(test) {
                w.test = test;
                w.testCase = '';
                this.loadTestCases(test);
            }
        };
    }

    /**
     * Size the chart to its box: the svg are built with the width and height they are drawn at,
     * so the texts keep their size whatever the size of the widget. Returns the observer.
     */
    function fitChart(el, onSize) {
        if (!el || !window.ResizeObserver) { return null; }
        var pending = false;
        var ro = new ResizeObserver(function () {
            if (pending) { return; }
            pending = true;
            requestAnimationFrame(function () {
                pending = false;
                if (el.clientWidth > 40 && el.clientHeight > 40) { onSize(Math.floor(el.clientWidth), Math.floor(el.clientHeight)); }
            });
        });
        ro.observe(el);
        return ro;
    }

    /** Ends the loader of a widget, once it was seen at least MIN_LOADER ms (a cached answer would flash it). */
    var MIN_LOADER = 500;
    function endLoader(since, done) {
        setTimeout(done, Math.max(0, MIN_LOADER - (Date.now() - since)));
    }

    function fmtNum(n) {
        if (n === null || n === undefined || isNaN(n)) { return '-'; }
        return window.formatnumberKM ? formatnumberKM(n) : String(n);
    }

    return {
        IN: IN, COLORS: COLORS, ICONS: ICONS, t: t,
        catalog: catalog, order: order, register: register,
        cached: cached, invalidate: invalidate, getJSON: getJSON,
        kpi: kpi, executionStat: executionStat, executionStats: executionStats, testCasePicker: testCasePicker,
        fitChart: fitChart, fmtNum: fmtNum, endLoader: endLoader,
        freshId: null   // the widget just added: its card opens its settings
    };
})();
