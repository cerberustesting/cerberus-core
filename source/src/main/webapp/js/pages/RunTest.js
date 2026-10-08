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
 * Run Tests - queue executions, either:
 *  - from a campaign: picking one fills the environments, countries, robots and execution
 *    parameters from the campaign, everything can still be overridden before the run;
 *  - from a list of test cases: filtered on the server (ReadTestCase), the parameters
 *    start from the user preferences (localStorage) and can be saved back.
 *
 * Both modes post to AddToExecutionQueuePrivate with the same parameters. The page also
 * accepts the deep links used by the rest of the application:
 * ?campaign= | ?test=&testcase=&environment=&country=&tag=&browser=
 */
function runTests() {
    var PREF_KEY = 'runTestsPrefs';
    var CUSTOM_ROBOT = 'CustomConfiguration';
    var EXEC_FIELDS = [
        {key: 'verbose', invariant: 'VERBOSE', param: 'verbose', campaign: 'Verbose', label: 'verbose'},
        {key: 'screenshot', invariant: 'SCREENSHOT', param: 'screenshot', campaign: 'Screenshot', label: 'screenshot'},
        {key: 'video', invariant: 'VIDEO', param: 'video', campaign: 'Video', label: 'video'},
        {key: 'pageSource', invariant: 'PAGESOURCE', param: 'pagesource', campaign: 'PageSource', label: 'pagesource'},
        {key: 'seleniumLog', invariant: 'ROBOTLOG', param: 'seleniumlog', campaign: 'RobotLog', label: 'robotlog'},
        {key: 'consoleLog', invariant: 'CONSOLELOG', param: 'consolelog', campaign: 'ConsoleLog', label: 'consolelog'},
        {key: 'retries', invariant: 'RETRIES', param: 'retries', campaign: 'Retries', label: 'retries'},
        {key: 'manualExecution', invariant: 'MANUALEXECUTION', param: 'manualexecution', campaign: 'ManualExecution', label: 'manualexecution'}
    ];
    var TEXT_FIELDS = [
        {key: 'tag', param: 'tag', campaign: 'Tag'},
        {key: 'timeout', param: 'timeout', campaign: 'Timeout'},
        {key: 'priority', param: 'priority', campaign: 'Priority'}
    ];
    // Filters of the test case list: key = request parameter of ReadTestCase.
    var FILTERS = [
        {key: 'test', label: 'test', url: 'ReadTest', params: {}, text: ['test', 'description'], value: 'test'},
        {key: 'labelid', label: 'label', url: 'ReadLabel', params: {e: 1}, system: true, text: ['label'], value: 'id'},
        {key: 'status', label: 'status', invariant: 'TCSTATUS'},
        {key: 'creator', label: 'creator', url: 'ReadUserPublic', params: {}, text: ['login'], value: 'login'},
        {key: 'implementer', label: 'implementer', url: 'ReadUserPublic', params: {}, text: ['login'], value: 'login'},
        {key: 'type', label: 'type', invariant: 'TESTCASE_TYPE'},
        {key: 'priority', label: 'priority', invariant: 'PRIORITY'},
        {key: 'system', label: 'system', invariant: 'SYSTEM'},
        {key: 'application', label: 'application', url: 'ReadApplication', params: {e: 1}, system: true, text: ['application'], value: 'application'},
        {key: 'campaign', label: 'campaign', url: 'ReadCampaign', params: {}, text: ['campaign'], value: 'campaign'}
    ];

    return {
        mode: 'tests',            // 'tests' | 'campaign'
        loading: true,
        running: false,

        // selection
        campaign: '',
        campaigns: [],
        testcases: [],            // loaded list: ReadTestCase items
        selected: {},             // "test|testcase" -> true
        listFilter: '',
        listLoading: false,
        resultSize: 50,
        filters: {},              // key -> selected values
        filtersLoaded: false,
        filtersOpen: false,
        filterDefs: FILTERS,

        // target
        environments: [],         // [{environment, active}]
        countries: [],
        envMode: 'auto',          // 'auto' | 'manual'
        selEnvironments: [],
        selCountries: [],
        manual: {myhost: '', mycontextroot: '', myloginrelativeurl: '', myenvdata: ''},

        // robot
        robots: [],
        selRobots: [],
        robotInfo: {host: '', port: '', browser: ''},
        custom: {ss_ip: '', ss_p: '', browser: ''},

        // execution
        invariants: {},           // INVARIANT -> [{value, description}]
        execFields: EXEC_FIELDS,
        exec: {tag: '', timeout: '', priority: '', verbose: '', screenshot: '', video: '', pageSource: '',
            seleniumLog: '', consoleLog: '', retries: '', manualExecution: ''},
        execOpen: false,
        prefsSaved: false,

        async init() {
            var self = this;
            document.title = this.t('title');
            FILTERS.forEach(function (f) { self.filters[f.key] = []; });
            FILTERS.forEach(function (f) {
                window.addEventListener('rt-' + f.key + '-change', function (e) { self.filters[f.key] = e.detail || []; });
            });

            var user = getUser();
            var systemQuery = user.defaultSystemsQuery;
            var invariantNames = EXEC_FIELDS.map(function (f) { return f.invariant; }).concat(['BROWSER']);
            await Promise.all([
                $.getJSON('ReadCampaign').then(function (d) {
                    self.campaigns = (d.contentTable || []).map(function (c) { return c.campaign; }).sort();
                }),
                $.getJSON('ReadCountryEnvParam', 'uniqueCountry=true' + systemQuery).then(function (d) {
                    self.countries = (d.contentTable || []).map(function (c) { return c.country; });
                }),
                $.getJSON('ReadCountryEnvParam', 'uniqueEnvironment=true' + systemQuery).then(function (d) {
                    self.environments = d.contentTable || [];
                }),
                $.getJSON('ReadRobot').then(function (d) {
                    self.robots = (d.contentTable || []).map(function (r) { return r.robot; });
                }),
                Promise.all(invariantNames.map(function (n) { return self.loadInvariant(n); }))
            ]);

            this.applyPreferences();
            this.applyUrlParameters();

            // The only choice is the right one.
            if (this.selCountries.length === 0 && this.countries.length === 1) { this.selCountries = this.countries.slice(); }
            if (this.selEnvironments.length === 0 && this.environments.length === 1) { this.selEnvironments = [this.environments[0].environment]; }

            this.loading = false;

            var campaign = GetURLParameters('campaign')[0];
            if (campaign) {
                this.mode = 'campaign';
                this.campaign = campaign;
                await this.loadCampaign();
            } else {
                var test = GetURLParameter('test');
                var testcase = GetURLParameter('testcase');
                await this.loadTestcases(test, testcase);
                if (test && testcase) {
                    this.selected[test + '|' + testcase] = true;
                }
            }
            if (this.selRobots.length === 1) { this.loadRobotInfo(); }
        },

        // ── Labels (labels.js, pageRunTests) - {0}, {1}... are replaced by the extra arguments ──
        t(key) {
            var text = Alpine.store('labels').getLabel('pageRunTests', key);
            for (var i = 1; i < arguments.length; i++) {
                text = text.split('{' + (i - 1) + '}').join(arguments[i]);
            }
            return text;
        },

        // ── Static data ──
        loadInvariant(name) {
            var self = this;
            var cacheName = name + 'INVARIANT';
            var cached = JSON.parse(sessionStorage.getItem(cacheName));
            if (cached) {
                this.invariants[name] = cached;
                return Promise.resolve();
            }
            return Promise.resolve($.ajax({url: 'FindInvariantByID', data: {idName: name}, dataType: 'json'})).then(function (data) {
                sessionStorage.setItem(cacheName, JSON.stringify(data));
                self.invariants[name] = data;
            });
        },
        options(name) {
            return this.invariants[name] || [];
        },
        filterItems(f) {
            var self = this;
            return this._filterItems(f).catch(function (e) {
                console.error('Run Tests: unable to load the "' + f.key + '" filter', e);
                showMessageMainPage('danger', self.t('filterloaderror', self.t(f.label)), false);
                return [];
            });
        },
        _filterItems(f) {
            var self = this;
            if (f.invariant) {
                return this.loadInvariant(f.invariant).then(function () {
                    return self.options(f.invariant).map(function (i) { return {label: String(i.value), value: String(i.value)}; });
                });
            }
            var params = $.extend({}, f.params);
            var query = $.param(params) + (f.system ? getUser().defaultSystemsQuery : '');
            return Promise.resolve($.getJSON(f.url, query)).then(function (d) {
                return (d.contentTable || []).map(function (row) {
                    return {
                        label: f.text.map(function (k) { return row[k]; }).join(' - '),
                        value: String(row[f.value])
                    };
                });
            });
        },
        openFilters() {
            this.filtersOpen = !this.filtersOpen;
            this.filtersLoaded = true;
        },
        get activeFilters() {
            var self = this;
            return FILTERS.reduce(function (n, f) { return n + (self.filters[f.key].length ? 1 : 0); }, 0);
        },

        // ── Mode ──
        setMode(mode) {
            if (this.mode === mode) { return; }
            this.mode = mode;
            clearResponseMessageMainPage();
            if (mode === 'tests') {
                this.testcases = [];
                this.selected = {};
                this.loadTestcases();
            } else {
                this.testcases = [];
                if (this.campaign) { this.loadCampaign(); }
            }
        },

        // ── Test case list ──
        key(tc) { return tc.test + '|' + tc.testcase; },
        get visibleTestcases() {
            var q = this.listFilter.trim().toLowerCase();
            if (!q) { return this.testcases; }
            return this.testcases.filter(function (tc) {
                return (tc.test + ' ' + tc.testcase + ' ' + tc.application + ' ' + (tc.description || '')).toLowerCase().indexOf(q) !== -1;
            });
        },
        get selectedTestcases() {
            var self = this;
            return this.testcases.filter(function (tc) { return self.selected[self.key(tc)]; });
        },
        selectAll(on) {
            var self = this;
            this.visibleTestcases.forEach(function (tc) { self.selected[self.key(tc)] = on; });
        },
        loadTestcases(defTest, defTestcase) {
            var self = this;
            clearResponseMessageMainPage();
            this.listLoading = true;
            var data = $.param({filter: true, length: this.resultSize});
            if (defTest && defTestcase) {
                // Deep link: the server answers with that single test case.
                data += '&' + $.param({test: defTest, testCase: defTestcase});
            } else {
                var params = {};
                FILTERS.forEach(function (f) { if (self.filters[f.key].length) { params[f.key] = self.filters[f.key]; } });
                data += '&' + $.param(params, true);
            }
            data += getUser().defaultSystemsQuery;
            return $.getJSON('ReadTestCase', data).then(function (d) {
                self.testcases = d.contentTable || [];
                var known = {};
                self.testcases.forEach(function (tc) { known[self.key(tc)] = true; });
                Object.keys(self.selected).forEach(function (k) { if (!known[k]) { delete self.selected[k]; } });
                if (self.testcases.length === 0) {
                    showMessageMainPage('warning', self.t('notestcase'), false);
                }
            }).fail(showUnexpectedError).always(function () { self.listLoading = false; });
        },

        // ── Campaign ──
        async loadCampaign() {
            var self = this;
            clearResponseMessageMainPage();
            this.testcases = [];
            if (!this.campaign) { return; }
            this.listLoading = true;
            var name = this.campaign;
            await Promise.all([
                $.getJSON('ReadTestCase', {campaign: name}).then(function (d) {
                    self.testcases = d.contentTable || [];
                }),
                $.getJSON('ReadCampaignParameter', {campaign: name}).then(function (d) {
                    var pick = function (type) {
                        return (d.contentTable || []).filter(function (p) { return p.parameter === type; }).map(function (p) { return p.value; });
                    };
                    self.envMode = 'auto';
                    self.selEnvironments = pick('ENVIRONMENT');
                    self.selCountries = pick('COUNTRY');
                    self.selRobots = pick('ROBOT');
                }),
                $.getJSON('ReadCampaign', {campaign: name}).then(function (d) {
                    var c = d.contentTable;
                    if (!c) { return; }
                    EXEC_FIELDS.concat(TEXT_FIELDS).forEach(function (f) {
                        if (c[f.campaign] !== null && c[f.campaign] !== undefined && c[f.campaign] !== '') {
                            self.exec[f.key] = c[f.campaign];
                        }
                    });
                })
            ]).catch(showUnexpectedError);
            this.listLoading = false;
            this.loadRobotInfo();
        },

        // ── Robot ──
        get isCustomRobot() {
            return this.selRobots.length === 1 && this.selRobots[0] === CUSTOM_ROBOT;
        },
        get namedRobot() {
            return this.selRobots.length === 1 && this.selRobots[0] !== CUSTOM_ROBOT ? this.selRobots[0] : '';
        },
        toggleRobot(robot) {
            var i = this.selRobots.indexOf(robot);
            if (i === -1) {
                // The custom configuration cannot be mixed with the named robots.
                this.selRobots = (robot === CUSTOM_ROBOT) ? [robot] : this.selRobots.filter(function (r) { return r !== CUSTOM_ROBOT; }).concat(robot);
            } else {
                this.selRobots.splice(i, 1);
            }
            this.loadRobotInfo();
        },
        loadRobotInfo() {
            var self = this;
            this.robotInfo = {host: '', port: '', browser: ''};
            if (!this.namedRobot) { return; }
            var robot = this.namedRobot;
            $.getJSON('ReadRobot', {robot: robot}).then(function (data) {
                if (robot !== self.namedRobot) { return; }
                var active = (data.contentTable.executors || []).filter(function (e) { return e.isActive; });
                self.robotInfo = {
                    host: active.length ? active[0].host + (active.length > 1 ? ' ' + self.t('andmore', active.length - 1) : '') : self.t('noexecutor'),
                    port: active.length ? String(active[0].port) : '',
                    browser: data.contentTable.browser
                };
            });
        },
        openRobotModal(mode) {
            var self = this;
            var robot = mode === 'EDIT' ? this.namedRobot : '';
            openModalRobot(robot, mode);
            $('#editRobotModal').on('hidden.bs.modal', function () {
                $('#editRobotModal').unbind('hidden.bs.modal');
                var saved = $('#editRobotModal').data('robot');
                if (saved && $('#editRobotModal').data('Saved')) {
                    if (mode === 'ADD' && self.robots.indexOf(saved.robot) === -1) { self.robots.push(saved.robot); }
                    if (mode === 'ADD') { self.selRobots = [saved.robot]; }
                    self.$nextTick(function () { self.loadRobotInfo(); });
                }
            });
        },

        // ── Target ──
        toggle(list, value) {
            var i = list.indexOf(value);
            if (i === -1) { list.push(value); } else { list.splice(i, 1); }
        },
        envLabel(e) {
            return e.environment + (e.active === false ? ' ' + this.t('disabled') : '');
        },

        // ── Preferences ──
        applyPreferences() {
            var pref = JSON.parse(localStorage.getItem(PREF_KEY));
            if (!pref) { return; }
            $.extend(this.exec, pref.exec);
            $.extend(this.custom, pref.custom);
            this.selRobots = (pref.robots || []).filter(function (r) { return r === CUSTOM_ROBOT || this.robots.indexOf(r) !== -1; }, this);
            this.envMode = pref.envMode || 'auto';
            $.extend(this.manual, pref.manual);
            var envs = this.environments.map(function (e) { return e.environment; });
            this.selEnvironments = (pref.environments || []).filter(function (e) { return envs.indexOf(e) !== -1; });
            this.selCountries = (pref.countries || []).filter(function (c) { return this.countries.indexOf(c) !== -1; }, this);
        },
        applyUrlParameters() {
            var tag = GetURLParameter('tag');
            var browser = GetURLParameter('browser');
            var country = GetURLParameter('country');
            var environment = GetURLParameter('environment');
            if (tag) { this.exec.tag = tag; }
            if (browser) { this.custom.browser = browser; }
            if (country && this.countries.indexOf(country) !== -1) { this.selCountries = [country]; }
            if (environment) { this.selEnvironments = [environment]; }
        },
        savePreferences() {
            var self = this;
            localStorage.setItem(PREF_KEY, JSON.stringify({
                exec: this.exec,
                custom: this.custom,
                robots: this.selRobots,
                envMode: this.envMode,
                manual: this.manual,
                environments: this.selEnvironments,
                countries: this.selCountries
            }));
            this.prefsSaved = true;
            setTimeout(function () { self.prefsSaved = false; }, 2500);
        },
        resetPreferences() {
            localStorage.removeItem(PREF_KEY);
            window.location.reload();
        },

        // ── Run ──
        get testCount() {
            return this.mode === 'campaign' ? this.testcases.length : this.selectedTestcases.length;
        },
        get executionCount() {
            var envs = this.envMode === 'manual' ? 1 : this.selEnvironments.length;
            return this.testCount * envs * this.selCountries.length * Math.max(1, this.isCustomRobot ? 1 : this.selRobots.length);
        },
        buildRequest() {
            var self = this;
            var p = [{name: 'e', value: 1}, {name: 'outputformat', value: 'json'}];
            if (this.mode === 'campaign') { p.push({name: 'campaign', value: this.campaign}); }
            EXEC_FIELDS.concat(TEXT_FIELDS).forEach(function (f) { p.push({name: f.param, value: self.exec[f.key]}); });

            if (this.mode === 'tests') {
                this.selectedTestcases.forEach(function (tc) {
                    p.push({name: 'test', value: tc.test}, {name: 'testcase', value: tc.testcase});
                });
            }
            if (this.envMode === 'manual') {
                p.push({name: 'manualurl', value: 1});
                ['myhost', 'mycontextroot', 'myloginrelativeurl', 'myenvdata'].forEach(function (k) {
                    p.push({name: k, value: self.manual[k]});
                });
            } else {
                this.selEnvironments.forEach(function (e) { p.push({name: 'environment', value: e}); });
            }
            this.selCountries.forEach(function (c) { p.push({name: 'country', value: c}); });
            if (this.isCustomRobot) {
                p.push({name: 'ss_ip', value: this.custom.ss_ip}, {name: 'ss_p', value: this.custom.ss_p}, {name: 'browser', value: this.custom.browser});
            } else {
                this.selRobots.forEach(function (r) { p.push({name: 'robot', value: r}); });
            }
            return $.param(p);
        },
        validate() {
            if (this.mode === 'tests' && this.selectedTestcases.length === 0) { return 'selectonetestcase'; }
            if (this.mode === 'campaign' && !this.campaign) { return 'selectonecampaign'; }
            if (this.envMode === 'auto' && this.selEnvironments.length === 0) { return 'selectoneenv'; }
            if (this.selCountries.length === 0) { return 'selectonecountry'; }
            return null;
        },
        run(redirect) {
            var self = this;
            clearResponseMessageMainPage();
            var error = this.validate();
            if (error) {
                showMessageMainPage('danger', this.t(error), false);
                return;
            }
            this.running = true;
            $.post('AddToExecutionQueuePrivate', this.buildRequest()).then(function (data) {
                data.message = data.message.replace(/\n/g, '<br>');
                if (getAlertType(data.messageType) === 'success') {
                    self.handleQueueResponse(data, redirect);
                } else {
                    showMessageMainPage(getAlertType(data.messageType), data.message, false);
                }
            }).fail(handleErrorAjaxAfterTimeout).always(function () { self.running = false; });
        },
        handleQueueResponse(data, redirect) {
            var self = this;
            [
                ['nbErrorRobotMissing', 'errrobot'],
                ['nbErrorTCNotActive', 'errtcnotactive'],
                ['nbErrorTCNotAllowedOnEnv', 'errtcnotallowed'],
                ['nbErrorEnvNotExistOrNotActive', 'errenv']
            ].forEach(function (e) {
                if (data[e[0]] > 0) { data.message += '<br>' + self.t(e[1], data[e[0]]); }
            });
            var url = null;
            if (data.nbExe === 1) {
                url = 'TestCaseExecution.jsp?executionQueueId=' + data.queueList[0].queueId;
                data.message += '<br><a href="' + url + '" class="v2in-btn v2in-btn--primary" id="goToExecution">' + this.t('openexecution') + '</a>';
            } else if (data.nbExe > 1) {
                url = 'ReportingExecutionByTag.jsp?Tag=' + encodeURIComponent(data.tag);
                data.message += '<br><a href="' + url + '" class="v2in-btn v2in-btn--primary" id="goToTagReport">' + this.t('reportbytag') + '</a>';
            }
            var type = getAlertType(data.messageType);
            if (type === 'success' && data.nbExe === 0) { type = 'warning'; }
            showMessageMainPage(type, data.message, false, 60000);
            if (url && redirect) { window.location.href = url; }
        }
    };
}