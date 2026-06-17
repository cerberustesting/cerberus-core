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

var _v2exe_uid = 0;
function v2exeuid() { return '__v2exe_' + (++_v2exe_uid); }

function executionV2() {
    return {
        // ═══ STATE ═══
        mode: 'loading',     // 'loading' | 'execution' | 'queue'
        tab: 'steps',

        // Execution data
        exe: {},              // Données brutes de l'exécution
        testCaseObj: null,    // Objet TestCase associé
        applicationObj: null, // Objet Application

        // Steps
        steps: [],
        activeStepIndex: -1,

        // Progress bar — computed via getters (see COMPUTED section)

        // Properties
        properties: [],
        showSecondary: false,

        // Manual execution
        isManual: false,
        saveState: '',        // '' | 'dirty' | 'saving' | 'saved'

        // Queue
        queueInfo: null,
        queueRefreshTimer: null,

        // WebSocket / Live refresh
        ws: null,
        wsConnected: false,
        _pollingTimer: null,
        _pollErrorCount: 0,
        _wsHeartbeatTimer: null,  // Safety: if no WS push in 5s, do one API poll
        liveStatus: 'idle',   // 'idle' | 'ws' | 'polling' | 'error' | 'done'
        _spinDone: false,     // true for 5s after PE→done transition
        _spinDoneTimer: null,

        // Navigation
        lastExecutions: [],

        // Bugs
        falseNegative: false,

        // Lightbox
        lightboxUrl: null,
        lightboxLabel: '',

        // Vision modal (Live / Video)
        visionModal: { open: false, mode: 'live', url: '', fullscreen: false },

        // Feature flags
        paramActivateWebSocket: 'N',
        paramWebSocketPeriod: 5000,

        // ═══ COMPUTED ═══
        get activeStep() { return this.steps[this.activeStepIndex] || null; },
        get preSteps() { return this.steps.filter(s => s._isPreTesting); },
        get postSteps() { return this.steps.filter(s => s._isPostTesting); },
        get mainSteps() { return this.steps.filter(s => !s._isPreTesting && !s._isPostTesting); },
        get primaryProperties() { return this.properties.filter(p => p.rank !== 2); },
        get secondaryProperties() { return this.properties.filter(p => p.rank === 2); },

        get statusColor() {
            return this._getStatusColor(this.exe.controlStatus);
        },

        get progressColor() {
            var status = this.exe ? this.exe.controlStatus : '';
            var colorMap = {
                'OK': '#00d27a', 'KO': '#e63757', 'FA': '#f59e0b',
                'PE': '#2c7be5', 'NA': '#eab308', 'NE': '#aaaaaa',
                'CA': '#aaaaaa', 'WE': '#34495E', 'QU': '#BF00BF'
            };
            return colorMap[status] || '#aaaaaa';
        },

        get progress() {
            var status = this.exe ? this.exe.controlStatus : '';
            // OK always means 100% completion
            if (status === 'OK') return 100;
            // No steps loaded yet
            if (!this.steps || this.steps.length === 0) {
                return (status && status !== 'PE' && status !== 'NE' && status !== 'QU') ? 100 : 0;
            }
            // Count items that have been actually processed
            var total = 0, done = 0;
            this.steps.forEach(function(s) {
                total++;
                if (s.returnCode && s.returnCode !== 'PE' && s.returnCode !== 'NE' && s.returnCode !== 'WE' && s.returnCode !== 'QU') done++;
                (s.actions || []).forEach(function(a) {
                    total++;
                    if (a.returnCode && a.returnCode !== 'PE' && a.returnCode !== 'NE' && a.returnCode !== 'WE' && a.returnCode !== 'QU') done++;
                    (a.controls || []).forEach(function(c) {
                        total++;
                        if (c.returnCode && c.returnCode !== 'PE' && c.returnCode !== 'NE' && c.returnCode !== 'WE' && c.returnCode !== 'QU') done++;
                    });
                });
            });
            return total > 0 ? Math.round((done / total) * 100) : 0;
        },

        // Progress for the visual bar: 100% when execution finished (any status except PE/NE/QU)
        get progressBar() {
            var status = this.exe ? this.exe.controlStatus : '';
            if (!status || status === 'PE' || status === 'NE' || status === 'QU') return this.progress;
            return 100;
        },

        // ═══ INIT ═══
        init() {
            console.info('[ExeV2] Initializing...');
            const executionId = GetURLParameter('executionId');
            const queueId = GetURLParameter('executionQueueId');
            const tabURL = GetURLParameter('tabactive');
            if (tabURL) this.tab = tabURL;

            // Load feature flags from sessionStorage cache (non-blocking)
            // getParameterString uses sync AJAX which can freeze the page — read cache directly instead
            try {
                var cachedWsPush = JSON.parse(sessionStorage.getItem('PARAMETER_cerberus_featureflipping_activatewebsocketpush'));
                var cachedWsPeriod = JSON.parse(sessionStorage.getItem('PARAMETER_cerberus_featureflipping_websocketpushperiod'));
                this.paramActivateWebSocket = (cachedWsPush && cachedWsPush.value) || 'Y';
                this.paramWebSocketPeriod = parseInt((cachedWsPeriod && cachedWsPeriod.value) || '5000') || 5000;
            } catch(e) {
                this.paramActivateWebSocket = 'Y';
                this.paramWebSocketPeriod = 5000;
            }
            console.info('[ExeV2] Feature flags: WS=' + this.paramActivateWebSocket + ', period=' + this.paramWebSocketPeriod);

            // Keyboard shortcuts
            document.addEventListener('keydown', (e) => {
                if ((e.ctrlKey || e.metaKey) && e.key === 's') {
                    e.preventDefault();
                    if (this.isManual && this.saveState === 'dirty') this.save();
                }
            });
            window.addEventListener('beforeunload', (e) => {
                if (this.saveState === 'dirty') { e.preventDefault(); e.returnValue = ''; }
            });

            // Branch: execution or queue mode
            if (executionId) {
                this.mode = 'execution';
                this._loadExecution(executionId);
            } else if (queueId) {
                this.mode = 'queue';
                this._loadQueue(queueId);
            } else {
                this.mode = 'execution';
                // No ID provided, show error
                showMessageMainPage('danger', 'No execution ID provided.', false);
            }

            this.$nextTick(() => {
                if (window.lucide) lucide.createIcons();
                this._updateSidebarTop();
            });
        },

        _updateSidebarTop() {
            var header = document.getElementById('v2ExeHeader');
            if (header) {
                var headerH = header.offsetHeight + 16;
                document.documentElement.style.setProperty('--v2-exe-sidebar-top', headerH + 'px');
            }
        },

        // ═══ LIVE CLEANUP ═══
        _stopLive() {
            if (this._pollingTimer) { clearTimeout(this._pollingTimer); this._pollingTimer = null; }
            if (this._wsHeartbeatTimer) { clearTimeout(this._wsHeartbeatTimer); this._wsHeartbeatTimer = null; }
            if (this.ws) { try { this.ws.close(); } catch(e) {} this.ws = null; }
            this.wsConnected = false;
            this._pollErrorCount = 0;
            if (this.liveStatus !== 'idle') this.liveStatus = 'done';
        },

        // ═══ DATA LOADING ═══
        _loadExecution(executionId) {
            this._stopLive();  // Kill any existing WS/polling before loading new execution
            console.info('[ExeV2] Loading execution:', executionId);
            $.ajax({
                url: 'ReadTestCaseExecution',
                data: { executionId: executionId, executionWithDependency: true },
                dataType: 'json',
                success: (data) => {
                    if (data.messageType === 'KO') {
                        showUnexpectedError(null, 'ERROR', data.message);
                        return;
                    }
                    const tce = data.testCaseExecution;
                    if (!tce) { showUnexpectedError(null, 'ERROR', 'Execution not found'); return; }

                    this.exe = tce;
                    this.testCaseObj = tce.testCaseObj || null;
                    this.isManual = (tce.manualExecution === 'Y');
                    this.falseNegative = !!(tce.currentFNB && tce.currentFNB !== '' && tce.currentFNB !== '0') || !!(tce.falseNegative);

                    // History
                    try {
                        saveHistory({
                            id: tce.id,
                            test: tce.test,
                            testcase: tce.testcase || tce.testCase,
                            controlStatus: tce.controlStatus,
                            env: tce.environment,
                            country: tce.country
                        }, 'historyExecutions', 5);
                    } catch(e) { console.warn('[ExeV2] saveHistory failed:', e); }

                    // Steps
                    const stepsRaw = tce.testCaseStepExecutionList || [];
                    stepsRaw.sort((a, b) => a.sort - b.sort);
                    this.steps = stepsRaw.map(s => this._normalizeStep(s));

                    // Properties
                    this.properties = (tce.testCaseExecutionDataList || []).map(p => this._normalizeProperty(p));

                    // Load application info
                    if (tce.application) {
                        $.ajax({
                            url: 'ReadApplication',
                            data: { application: tce.application },
                            dataType: 'json',
                            success: (d) => { this.applicationObj = d.contentTable || null; }
                        });
                    }

                    // Load last executions for navigation
                    this._loadLastExecutions(tce.test, tce.testcase || tce.testCase);

                    // Select first step (or focused step from URL)
                    if (this.steps.length > 0) {
                        const stepFocus = GetURLParameter('stepFocus');
                        let idx = 0;
                        if (stepFocus) {
                            const parts = stepFocus.split('-');
                            if (parts.length >= 2) {
                                idx = this.steps.findIndex(s => s.step == parts[0] && s.index == parts[1]);
                                if (idx < 0) idx = 0;
                            }
                        } else {
                            // Focus on last completed step or step in progress
                            const peIdx = this.steps.findIndex(s => s.returnCode === 'PE');
                            if (peIdx >= 0) idx = peIdx;
                            else {
                                // Last step with a returnCode
                                for (let i = this.steps.length - 1; i >= 0; i--) {
                                    if (this.steps[i].returnCode && this.steps[i].returnCode !== 'NE') { idx = i; break; }
                                }
                            }
                        }
                        this.selectStep(idx);
                    }

                    // Update page title
                    document.title = 'Execution #' + tce.id + ' - ' + (tce.testcase || tce.testCase);

                    // Live updates if PE — WS first, polling as fallback
                    if (tce.controlStatus === 'PE') {
                        this.$nextTick(() => {
                            if (this.paramActivateWebSocket === 'Y') {
                                // Try WS — it will start polling as fallback if WS fails/closes
                                this._connectWebSocket(tce.id);
                            } else {
                                // WS disabled — go straight to polling
                                this._startPolling(tce.id);
                            }
                        });
                    }

                    this.mode = 'execution';
                    this._updateFavicon(tce.controlStatus);
                    this.$nextTick(() => {
                        if (window.lucide) lucide.createIcons();
                        this._updateSidebarTop();
                    });
                },
                error: (jqXHR, textStatus, errorThrown) => {
                    console.error('[ExeV2] ReadTestCaseExecution error:', textStatus, errorThrown);
                    showUnexpectedError(jqXHR, textStatus, errorThrown);
                }
            });
        },

        _loadQueue(queueId) {
            console.info('[ExeV2] Loading queue:', queueId);
            $.ajax({
                url: 'ReadTestCaseExecutionQueue',
                data: { queueid: queueId },
                dataType: 'json',
                success: (data) => {
                    const ct = data.contentTable;
                    if (!ct) return;
                    this.queueInfo = ct;

                    // If execution started, redirect
                    if (ct.exeId && ct.exeId > 0) {
                        var url = 'TestCaseExecutionV2.jsp?executionId=' + ct.exeId;
                        window.location.href = url;
                        return;
                    }

                    // Auto-refresh if still queued
                    if (ct.state === 'QUEUED' || ct.state === 'STARTING' || ct.state === 'WAITING') {
                        this.queueRefreshTimer = setTimeout(() => this._loadQueue(queueId), 5000);
                    }

                    this.$nextTick(() => { if (window.lucide) lucide.createIcons(); });
                },
                error: showUnexpectedError
            });
        },

        _loadLastExecutions(test, testcase) {
            $.ajax({
                url: 'api/executions/getLastByCriteria',
                data: { test: test, testCase: testcase, numberOfExecution: 10 },
                dataType: 'json',
                success: (data) => {
                    this.lastExecutions = (data || []).map(e => ({
                        id: e.id,
                        controlStatus: e.controlStatus,
                        env: e.environment,
                        country: e.country,
                        end: e.end,
                        tag: e.tag
                    }));
                }
            });
        },

        // ═══ NORMALIZERS ═══
        _normalizeStep(s) {
            s._uid = v2exeuid();
            s._isPreTesting = (s.sort < 0 || (s.conditionOperator && s.conditionOperator.indexOf('Pre') >= 0) || s.sort == 0);
            s._isPostTesting = (s.sort > 9000 || (s.conditionOperator && s.conditionOperator.indexOf('Post') >= 0));
            s._nbActionsKO = 0;
            s._nbControlsKO = 0;

            if (!s.testCaseStepActionExecutionList) s.testCaseStepActionExecutionList = [];
            s.testCaseStepActionExecutionList.sort((a, b) => a.sort - b.sort);
            s.actions = s.testCaseStepActionExecutionList.map(a => this._normalizeAction(a));

            // Count KO
            s.actions.forEach(a => {
                if (a.returnCode === 'KO' || a.returnCode === 'FA') s._nbActionsKO++;
                (a.controls || []).forEach(c => {
                    if (c.returnCode === 'KO' || c.returnCode === 'FA') s._nbControlsKO++;
                });
            });

            return s;
        },

        _normalizeAction(a) {
            a._uid = v2exeuid();
            a._expanded = false;

            if (!a.testCaseStepActionControlExecutionList) a.testCaseStepActionControlExecutionList = [];
            a.testCaseStepActionControlExecutionList.sort((x, y) => x.sort - y.sort);
            a.controls = a.testCaseStepActionControlExecutionList.map(c => this._normalizeControl(c));

            if (!a.fileList) a.fileList = [];
            return a;
        },

        _normalizeControl(c) {
            c._uid = v2exeuid();
            c._expanded = false;
            if (!c.fileList) c.fileList = [];
            return c;
        },

        _normalizeProperty(p) {
            p._uid = v2exeuid();
            p._expanded = false;
            // Remap API field names (Java sends RC/rMessage, V2 uses returnCode/returnMessage)
            if (p.RC !== undefined && p.returnCode === undefined) p.returnCode = p.RC;
            if (p.rMessage !== undefined && p.returnMessage === undefined) p.returnMessage = p.rMessage;
            if (!p.fileList) p.fileList = [];
            return p;
        },

        // ═══ TABS ═══
        setTab(name) {
            this.tab = name;
            this.$nextTick(() => {
                if (window.lucide) lucide.createIcons();
                if (name === 'network' && !this.networkStat) {
                    this._initNetwork();
                }
            });
        },

        // ═══ STEP NAVIGATION ═══
        selectStep(idx) {
            this.activeStepIndex = idx;
            this.$nextTick(() => { if (window.lucide) lucide.createIcons(); });
        },

        toggleActionDetail(action) {
            action._expanded = !action._expanded;
            this.$nextTick(() => { if (window.lucide) lucide.createIcons(); });
        },

        toggleControlDetail(control) {
            control._expanded = !control._expanded;
            this.$nextTick(() => { if (window.lucide) lucide.createIcons(); });
        },

        // ═══ WEBSOCKET ═══
        _connectWebSocket(executionId) {
            // Close any existing WS first to prevent duplicates
            if (this.ws) {
                try { this.ws.onclose = null; this.ws.onerror = null; this.ws.close(); } catch(e) {}
                this.ws = null;
                this.wsConnected = false;
            }

            const protocol = location.protocol === 'https:' ? 'wss' : 'ws';
            var parser = document.createElement('a');
            parser.href = window.location.href;
            var path = parser.pathname.split('TestCaseExecution')[0];
            const wsUrl = protocol + '://' + parser.host + path + 'api/ws/execution/' + executionId;
            console.info('[ExeV2] Connecting WebSocket:', wsUrl);

            var self = this;
            var fallbackDone = false;

            function fallbackToPolling() {
                if (fallbackDone) return;
                fallbackDone = true;
                self.wsConnected = false;
                // Only start polling fallback if execution is still running
                if (self.exe && self.exe.controlStatus === 'PE') {
                    console.info('[ExeV2] WS closed — starting polling fallback');
                    self.liveStatus = 'polling';
                    self._startPolling(executionId);
                }
            }

            try {
                this.ws = new WebSocket(wsUrl);
                this.ws.onopen = () => {
                    this.wsConnected = true;
                    this.liveStatus = 'ws';
                    this._pollErrorCount = 0;
                    // WS is primary — stop polling to avoid double server load
                    if (this._pollingTimer) {
                        clearTimeout(this._pollingTimer);
                        this._pollingTimer = null;
                    }
                    // Start heartbeat: if no WS push within period, do one API poll
                    this._startWsHeartbeat(executionId);
                    console.info('[ExeV2] WebSocket connected — polling stopped, heartbeat started');
                };
                this.ws.onmessage = (event) => {
                    try {
                        const data = JSON.parse(event.data);
                        this._onWebSocketMessage(data);
                    } catch(e) { console.warn('[ExeV2] WS message parse error:', e); }
                };
                this.ws.onerror = () => { fallbackToPolling(); };
                this.ws.onclose = () => { fallbackToPolling(); };
            } catch(e) {
                console.warn('[ExeV2] WebSocket not available — starting polling fallback');
                this.liveStatus = 'polling';
                this._startPolling(executionId);
            }
        },

        _onWebSocketMessage(data) {
            // WS sends execution flat (type='testCaseExecution'), API wraps it in { testCaseExecution: {...} }
            var tce = data.testCaseExecution || (data.type === 'testCaseExecution' ? data : null);
            if (!tce) return;
            var prevStatus = this.exe.controlStatus;

            // Reset heartbeat timer — we just got fresh data
            if (this.wsConnected && tce.controlStatus === 'PE') {
                this._startWsHeartbeat(tce.id);
            }

            // Update top-level exe fields without full replace (keeps Alpine reactivity smooth)
            Object.keys(tce).forEach(function(k) {
                if (k !== 'testCaseStepExecutionList' && k !== 'testCaseExecutionDataList') {
                    this.exe[k] = tce[k];
                }
            }.bind(this));
            // Force Alpine to detect exe changes (computed getters like progress depend on it)
            this.exe = Object.assign({}, this.exe);

            // Incremental step merge — update returnCode/returnMessage/end in-place
            var newSteps = tce.testCaseStepExecutionList || [];
            newSteps.sort(function(a, b) { return a.sort - b.sort; });
            this._mergeStepUpdates(newSteps);
            // Force Alpine reactivity — shallow copy triggers getter recalculation
            this.steps = this.steps.slice();

            // Properties — preserve expanded state across updates
            var expandedProps = {};
            (this.properties || []).forEach(function(p) { if (p._expanded) expandedProps[p.property] = true; });
            this.properties = (tce.testCaseExecutionDataList || []).map(function(p) { return this._normalizeProperty(p); }.bind(this));
            this.properties.forEach(function(p) { if (expandedProps[p.property]) p._expanded = true; });

            // Auto-focus: select the best step when execution is PE
            if (tce.controlStatus === 'PE' && this.steps.length > 0) {
                var peIdx = this.steps.findIndex(function(s) { return s.returnCode === 'PE'; });
                if (peIdx >= 0 && peIdx !== this.activeStepIndex) {
                    // A step is actively running — follow it
                    this.activeStepIndex = peIdx;
                } else if (this.activeStepIndex < 0 || this.activeStepIndex >= this.steps.length) {
                    // No step selected (first load or steps just appeared) — select PE or last
                    this.activeStepIndex = peIdx >= 0 ? peIdx : this.steps.length - 1;
                }
            }

            // Favicon on status change
            if (tce.controlStatus !== prevStatus) {
                this._updateFavicon(tce.controlStatus);
            }

            this.$nextTick(function() {
                if (window.lucide) lucide.createIcons();
                this._updateSidebarTop();
            }.bind(this));

            // If execution finished, stop all live refresh
            if (tce.controlStatus !== 'PE') {
                this._stopLive();
                this.liveStatus = 'done';
                // Spin-down animation for 5 seconds
                if (prevStatus === 'PE') {
                    this._spinDone = true;
                    if (this._spinDoneTimer) clearTimeout(this._spinDoneTimer);
                    this._spinDoneTimer = setTimeout(() => { this._spinDone = false; }, 5000);
                }
                // Final full reload to get all data
                this._loadExecution(tce.id);
            }
        },

        // Incremental merge: update existing step/action/control objects in-place
        // This avoids full DOM re-render and makes transitions smooth
        _mergeStepUpdates(newSteps) {
            var self = this;
            if (this.steps.length === 0) {
                // First load
                this.steps = newSteps.map(function(s) { return self._normalizeStep(s); });
                return;
            }
            // Build lookup by step+index
            var existingMap = {};
            this.steps.forEach(function(s) { existingMap[s.step + '-' + s.index] = s; });

            var needsFullRebuild = false;
            newSteps.forEach(function(ns) {
                var key = ns.step + '-' + ns.index;
                var existing = existingMap[key];
                if (!existing) { needsFullRebuild = true; return; }

                // Merge step-level fields
                existing.returnCode = ns.returnCode;
                existing.returnMessage = ns.returnMessage;
                existing.start = ns.start;
                existing.end = ns.end;
                existing.conditionOperator = ns.conditionOperator;

                // Merge actions
                var newActions = ns.testCaseStepActionExecutionList || [];
                newActions.sort(function(a, b) { return a.sort - b.sort; });

                if (existing.actions.length !== newActions.length) {
                    // Structure changed, rebuild this step
                    var rebuilt = self._normalizeStep(ns);
                    Object.keys(rebuilt).forEach(function(k) { existing[k] = rebuilt[k]; });
                    return;
                }

                newActions.forEach(function(na, ai) {
                    var ea = existing.actions[ai];
                    if (!ea) return;
                    // Update action fields in-place
                    ea.returnCode = na.returnCode;
                    ea.returnMessage = na.returnMessage;
                    ea.start = na.start;
                    ea.end = na.end;
                    ea.value1 = na.value1;
                    ea.value2 = na.value2;
                    if (na.fileList) ea.fileList = na.fileList;

                    // Merge controls
                    var newControls = na.testCaseStepActionControlExecutionList || [];
                    newControls.sort(function(x, y) { return x.sort - y.sort; });

                    if ((ea.controls || []).length !== newControls.length) {
                        ea.controls = newControls.map(function(c) { return self._normalizeControl(c); });
                    } else {
                        newControls.forEach(function(nc, ci) {
                            var ec = ea.controls[ci];
                            if (!ec) return;
                            ec.returnCode = nc.returnCode;
                            ec.returnMessage = nc.returnMessage;
                            ec.start = nc.start;
                            ec.end = nc.end;
                            ec.value1 = nc.value1;
                            ec.value2 = nc.value2;
                            if (nc.fileList) ec.fileList = nc.fileList;
                        });
                    }
                });

                // Recount KO
                existing._nbActionsKO = 0;
                existing._nbControlsKO = 0;
                existing.actions.forEach(function(a) {
                    if (a.returnCode === 'KO' || a.returnCode === 'FA') existing._nbActionsKO++;
                    (a.controls || []).forEach(function(c) {
                        if (c.returnCode === 'KO' || c.returnCode === 'FA') existing._nbControlsKO++;
                    });
                });
            });

            if (needsFullRebuild) {
                this.steps = newSteps.map(function(s) { return self._normalizeStep(s); });
            }
        },

        // Heartbeat: if no WS push within period, do one API poll to guarantee freshness
        _startWsHeartbeat(executionId) {
            if (this._wsHeartbeatTimer) clearTimeout(this._wsHeartbeatTimer);
            var self = this;
            this._wsHeartbeatTimer = setTimeout(function() {
                if (!self.wsConnected || !self.exe || self.exe.controlStatus !== 'PE') return;
                console.debug('[ExeV2] Heartbeat: no WS push in ' + self.paramWebSocketPeriod + 'ms — doing API poll');
                $.ajax({
                    url: 'ReadTestCaseExecution',
                    data: { executionId: executionId, executionWithDependency: true },
                    dataType: 'json',
                    timeout: 10000,
                    success: function(data) {
                        if (data.testCaseExecution) {
                            self._onWebSocketMessage(data);
                        }
                    },
                    error: function() {
                        console.warn('[ExeV2] Heartbeat poll failed — will retry on next cycle');
                        // Reset heartbeat to try again
                        if (self.wsConnected && self.exe && self.exe.controlStatus === 'PE') {
                            self._startWsHeartbeat(executionId);
                        }
                    }
                });
            }, this.paramWebSocketPeriod);
        },

        _startPolling(executionId) {
            var self = this;
            // Don't poll if WS is connected — WS is primary
            if (this.wsConnected) {
                console.debug('[ExeV2] WS active — skipping polling');
                return;
            }
            if (this._pollingTimer) clearTimeout(this._pollingTimer);

            // Backoff: base × 2^errors, capped at 30s
            var delay = Math.min(this.paramWebSocketPeriod * Math.pow(2, this._pollErrorCount), 30000);
            if (this._pollErrorCount === 0) {
                console.info('[ExeV2] Polling every', delay, 'ms');
            }
            this.liveStatus = this._pollErrorCount >= 3 ? 'error' : 'polling';

            this._pollingTimer = setTimeout(function() {
                if (!self.exe || self.exe.controlStatus !== 'PE') {
                    self.liveStatus = 'done';
                    return;
                }
                $.ajax({
                    url: 'ReadTestCaseExecution',
                    data: { executionId: executionId, executionWithDependency: true },
                    dataType: 'json',
                    timeout: 15000,  // 15s timeout to avoid hanging requests
                    success: function(data) {
                        self._pollErrorCount = 0;  // Reset on success
                        if (self.wsConnected) self.liveStatus = 'ws'; else self.liveStatus = 'polling';
                        if (data.testCaseExecution) {
                            self._onWebSocketMessage(data);
                        }
                        // Keep polling if still PE
                        if (self.exe && self.exe.controlStatus === 'PE') {
                            self._startPolling(executionId);
                        } else {
                            self.liveStatus = 'done';
                        }
                    },
                    error: function() {
                        self._pollErrorCount++;
                        console.warn('[ExeV2] Poll error #' + self._pollErrorCount + ', next retry in ' + Math.min(self.paramWebSocketPeriod * Math.pow(2, self._pollErrorCount), 30000) + 'ms');
                        // Retry with backoff if still PE
                        if (self.exe && self.exe.controlStatus === 'PE') {
                            self._startPolling(executionId);
                        }
                    }
                });
            }, delay);
        },

        // ═══ MANUAL EXECUTION ═══
        setActionStatus(stepIdx, actionIdx, status) {
            const step = this.steps[stepIdx];
            if (!step) return;
            const action = step.actions[actionIdx];
            if (!action) return;

            // Auto-validate previous actions as OK
            if (status === 'OK' || status === 'FA' || status === 'KO') {
                this._autoValidatePrevious(stepIdx, actionIdx);
            }

            action.returnCode = status;
            this._markDirty();
            this.$nextTick(() => {
                this._recalculateGlobalStatus();
                if (window.lucide) lucide.createIcons();
            });
        },

        setControlStatus(stepIdx, actionIdx, controlIdx, status) {
            const control = this.steps[stepIdx]?.actions[actionIdx]?.controls[controlIdx];
            if (!control) return;
            control.returnCode = status;
            this._markDirty();
            this.$nextTick(() => this._recalculateGlobalStatus());
        },

        _autoValidatePrevious(stepIdx, actionIdx) {
            // Auto-validate all previous steps' actions and this step's previous actions as OK
            for (let si = 0; si <= stepIdx; si++) {
                const step = this.steps[si];
                const maxA = (si < stepIdx) ? step.actions.length : actionIdx;
                for (let ai = 0; ai < maxA; ai++) {
                    if (!step.actions[ai].returnCode || step.actions[ai].returnCode === 'WE' || step.actions[ai].returnCode === 'NE' || step.actions[ai].returnCode === 'PE') {
                        step.actions[ai].returnCode = 'OK';
                        // Also validate controls
                        (step.actions[ai].controls || []).forEach(c => {
                            if (!c.returnCode || c.returnCode === 'WE' || c.returnCode === 'NE' || c.returnCode === 'PE') {
                                c.returnCode = 'OK';
                            }
                        });
                    }
                }
                // Update step return code
                if (si < stepIdx) step.returnCode = 'OK';
            }
        },

        _recalculateGlobalStatus() {
            let globalStatus = 'OK';
            this.steps.forEach(s => {
                let stepStatus = 'OK';
                s.actions.forEach(a => {
                    if (a.returnCode === 'KO') stepStatus = 'KO';
                    else if (a.returnCode === 'FA' && stepStatus !== 'KO') stepStatus = 'FA';
                    else if ((a.returnCode === 'WE' || a.returnCode === 'NE' || a.returnCode === 'PE') && stepStatus === 'OK') stepStatus = 'WE';
                    // Controls
                    (a.controls || []).forEach(c => {
                        if (c.returnCode === 'KO' && c.fatal === 'Y') stepStatus = 'KO';
                        else if (c.returnCode === 'KO') { if (stepStatus !== 'KO') stepStatus = 'FA'; }
                    });
                });
                s.returnCode = stepStatus;
                if (stepStatus === 'KO') globalStatus = 'KO';
                else if (stepStatus === 'FA' && globalStatus !== 'KO') globalStatus = 'FA';
                else if (stepStatus === 'WE' && globalStatus === 'OK') globalStatus = 'WE';
            });
            this.exe.controlStatus = globalStatus;
        },

        _markDirty() {
            this.saveState = 'dirty';
        },

        // ═══ SAVE ═══
        save() {
            if (this.saveState !== 'dirty') return;
            this.saveState = 'saving';

            const payload = this._buildPayload();

            $.ajax({
                url: 'UpdateTestCaseExecution',
                type: 'POST',
                contentType: 'application/json',
                data: JSON.stringify(payload),
                success: (data) => {
                    this.saveState = 'saved';
                    setTimeout(() => { this.saveState = ''; }, 2000);
                    showMessageMainPage('success', 'Execution saved successfully', true);
                },
                error: (jqXHR, textStatus, errorThrown) => {
                    this.saveState = 'dirty';
                    showUnexpectedError(jqXHR, textStatus, errorThrown);
                }
            });
        },

        _buildPayload() {
            return {
                executionId: this.exe.id,
                controlstatus: this.exe.controlStatus,
                returnMessage: this.exe.controlMessage || '',
                executor: this.exe.executor || '',
                stepArray: this.steps.map(s => ({
                    test: s.test,
                    testcase: s.testcase,
                    step: s.step,
                    index: s.index,
                    sort: s.sort,
                    loop: s.loop,
                    conditionOperator: s.conditionOperator,
                    conditionVal1: s.conditionValue1 || s.conditionVal1 || '',
                    conditionVal2: s.conditionValue2 || s.conditionVal2 || '',
                    conditionVal3: s.conditionValue3 || s.conditionVal3 || '',
                    start: s.start,
                    end: s.end,
                    returnCode: s.returnCode,
                    returnMessage: s.returnMessage || '',
                    description: s.description,
                    actionArr: s.actions.map(a => ({
                        test: a.test,
                        testcase: a.testcase,
                        step: a.step,
                        index: a.index,
                        sequence: a.sequence,
                        sort: a.sort,
                        action: a.action,
                        value1: a.value1 || '',
                        value2: a.value2 || '',
                        value3: a.value3 || '',
                        forceExeStatus: a.forceExeStatus || '',
                        conditionOperator: a.conditionOperator || '',
                        conditionVal1: a.conditionValue1 || a.conditionVal1 || '',
                        conditionVal2: a.conditionValue2 || a.conditionVal2 || '',
                        conditionVal3: a.conditionValue3 || a.conditionVal3 || '',
                        start: a.start,
                        end: a.end,
                        returnCode: a.returnCode,
                        returnMessage: a.returnMessage || '',
                        description: a.description || '',
                        controlArr: a.controls.map(c => ({
                            test: c.test,
                            testcase: c.testcase,
                            step: c.step,
                            index: c.index,
                            sequence: c.sequence,
                            controlSequence: c.controlSequence || c.control,
                            sort: c.sort,
                            controlType: c.controlType || c.control,
                            value1: c.value1 || '',
                            value2: c.value2 || '',
                            value3: c.value3 || '',
                            fatal: c.fatal || 'Y',
                            conditionOperator: c.conditionOperator || '',
                            conditionVal1: c.conditionValue1 || c.conditionVal1 || '',
                            conditionVal2: c.conditionValue2 || c.conditionVal2 || '',
                            conditionVal3: c.conditionValue3 || c.conditionVal3 || '',
                            start: c.start,
                            end: c.end,
                            returnCode: c.returnCode,
                            returnMessage: c.returnMessage || '',
                            description: c.description || ''
                        }))
                    }))
                }))
            };
        },

        // ═══ HEADER ACTIONS ═══
        switchToExecution(executionId) {
            // Inline navigation — cleanup live refresh before switching
            this._stopLive();
            this.mode = 'loading';
            this.steps = [];
            this.activeStepIndex = -1;
            this._loadExecution(executionId);
            var url = window.location.pathname + '?executionId=' + executionId;
            history.pushState({ executionId: executionId }, '', url);
        },

        reRun() {
            window.dispatchEvent(new CustomEvent('open-execution', {
                detail: {
                    test: this.exe.test,
                    testCase: this.exe.testcase || this.exe.testCase,
                    application: this.exe.application || (this.testCaseObj && this.testCaseObj.application) || '',
                    description: this.exe.description || '',
                    tag: this.exe.tag || ''
                }
            }));
        },

        editTag(newTag) {
            $.ajax({
                url: 'SetTagToExecution',
                data: { executionId: this.exe.id, newTag: newTag },
                success: () => {
                    this.exe.tag = newTag;
                    showMessageMainPage('success', 'Tag updated', true);
                },
                error: showUnexpectedError
            });
        },

        toggleFalseNegative() {
            const action = this.falseNegative ? 'undeclareFalseNegative' : 'declareFalseNegative';
            $.ajax({
                url: 'api/executions/' + this.exe.id + '/' + action,
                type: 'POST',
                success: () => {
                    this.falseNegative = !this.falseNegative;
                },
                error: showUnexpectedError
            });
        },

        createBug() {
            $.ajax({
                url: 'api/executions/' + this.exe.id + '/createBug',
                type: 'POST',
                dataType: 'json',
                success: (data) => {
                    showMessageMainPage('success', 'Bug created successfully', true);
                    // Reload to get updated bug info
                    this._loadExecution(this.exe.id);
                },
                error: showUnexpectedError
            });
        },

        _getBugNewUrl() {
            if (!this.applicationObj || !this.applicationObj.bugTrackerNewUrl) return '#';
            var url = this.applicationObj.bugTrackerNewUrl;
            var exe = this.exe;
            url = url.replace(/%EXEID%/g, exe.id || '');
            url = url.replace(/%EXEDATE%/g, exe.start || '');
            url = url.replace(/%TEST%/g, exe.test || '');
            url = url.replace(/%TESTCASE%/g, exe.testcase || exe.testCase || '');
            url = url.replace(/%TESTCASEDESC%/g, exe.description || '');
            url = url.replace(/%COUNTRY%/g, exe.country || '');
            url = url.replace(/%ENV%/g, exe.environment || '');
            url = url.replace(/%BUILD%/g, exe.build || '');
            url = url.replace(/%REV%/g, exe.revision || '');
            url = url.replace(/%BROWSER%/g, exe.browser || '');
            url = url.replace(/%BROWSERFULLVERSION%/g, (exe.browser || '') + ' ' + (exe.version || '') + ' ' + (exe.platform || ''));
            return encodeURI(url);
        },

        _getProviderSessionUrl() {
            var provider = (this.exe.robotProvider || '').toUpperCase();
            var sessionId = this.exe.robotProviderSessionId || '';
            // If there's a pre-built URL, use it
            if (this.exe.robotProviderSessionIdUrl) return this.exe.robotProviderSessionIdUrl;
            if (provider === 'BROWSERSTACK') {
                var hash = (this.exe.robotSessionId || '').split('/')[0] || '';
                return 'https://automate.browserstack.com/builds/' + hash + '/sessions/' + sessionId;
            } else if (provider === 'LAMBDATEST') {
                return 'https://automation.lambdatest.com/logs/?testID=' + sessionId + '&build=' + encodeURIComponent(this.exe.build || '');
            } else if (provider === 'KOBITON') {
                return 'https://portal.kobiton.com/sessions/' + sessionId;
            }
            return '#';
        },

        // ═══ NETWORK TAB ═══
        networkStat: null,
        networkCharts: {},
        networkIndexFilter: [],
        networkSortCol: 'size',  // 'size' | 'request' | 'time'

        // HAR viewer state
        harData: null,
        harLoading: false,
        harLoaded: false,
        harFilter: '',
        harStatusFilter: 'all',
        harSortBy: 'time',
        harSelected: null,
        harDetailTab: 'request',   // 'request' | 'response'

        _initNetwork() {
            if (!this.exe || !this.exe.httpStat) return;
            this.networkStat = this.exe.httpStat.stat;
            // Default: all indices selected
            if (this.networkStat && this.networkStat.index) {
                this.networkIndexFilter = this.networkStat.index.map(function(idx) { return idx.index; });
            }
            // Load HAR if enriched file exists
            var harFile = (this.exe.fileList || []).find(function(f) {
                return f.fileName && f.fileName.endsWith('enriched_har.json');
            });
            if (harFile && !this.harLoaded) {
                this._loadHar(harFile);
            }
        },

        _loadHar(file) {
            if (this.harLoaded || this.harLoading) return;
            this.harLoading = true;
            var self = this;
            var url = 'ReadTestCaseExecutionMedia'
                + '?filename=' + encodeURIComponent(file.fileName)
                + '&filetype=' + (file.fileType || 'JSON')
                + '&filedesc=' + encodeURIComponent(file.fileDesc || '')
                + '&auto=true&autoContentType=N';
            fetch(url)
                .then(function(r) { return r.text(); })
                .then(function(txt) {
                    try {
                        var har = JSON.parse(txt);
                        har.log.entries.forEach(function(e, i) { e._id = i; });
                        self.harData = har;
                        self.harLoaded = true;
                    } catch(e) { console.warn('[ExeV2] HAR parse error:', e); }
                })
                .finally(function() { self.harLoading = false; });
        },

        _isNetworkIndexSelected(index) {
            return this.networkIndexFilter.indexOf(index) !== -1;
        },

        // Request table filters
        netReqUrlFilter: '',
        netReqStatusFilter: 'all',
        netReqSelected: null,

        get networkRequests() {
            if (!this.networkStat || !this.networkStat.requests) return [];
            var self = this;
            return this.networkStat.requests.filter(function(r) {
                return self._isNetworkIndexSelected(r.index);
            });
        },

        get networkRequestsFiltered() {
            var self = this;
            return this.networkRequests.filter(function(r) {
                // URL filter
                if (self.netReqUrlFilter && (r.url || '').toLowerCase().indexOf(self.netReqUrlFilter.toLowerCase()) < 0) return false;
                // Status filter
                if (self.netReqStatusFilter !== 'all') {
                    var s = '' + (r.httpStatus || '');
                    if (self.netReqStatusFilter === '2xx' && !s.startsWith('2')) return false;
                    if (self.netReqStatusFilter === '3xx' && !s.startsWith('3')) return false;
                    if (self.netReqStatusFilter === '4xx' && !s.startsWith('4')) return false;
                    if (self.netReqStatusFilter === '5xx' && !s.startsWith('5')) return false;
                }
                return true;
            });
        },

        get networkHttpStatusData() {
            var counts = {};
            var total = 0;
            this.networkRequests.forEach(function(r) {
                total++;
                var key = '' + r.httpStatus;
                counts[key] = (counts[key] || 0) + 1;
            });
            var entries = [];
            for (var k in counts) {
                entries.push({ label: k, value: counts[k], color: _httpStatusColor(k) });
            }
            entries.sort(function(a, b) { return b.value - a.value; });
            return { entries: entries, total: total };
        },

        get networkSizeByTypeData() {
            var sizes = {};
            var total = 0;
            this.networkRequests.forEach(function(r) {
                total += r.size || 0;
                var key = '' + (r.contentType || 'other');
                sizes[key] = (sizes[key] || 0) + (r.size || 0);
            });
            var entries = [];
            for (var k in sizes) {
                entries.push({ label: k, value: sizes[k], color: _contentTypeColor(k) });
            }
            entries.sort(function(a, b) { return b.value - a.value; });
            return { entries: entries, total: total };
        },

        get networkThirdPartyData() {
            var providers = {};
            var nbTP = 0;
            this.networkRequests.forEach(function(r) {
                var p = '' + r.provider;
                if (!providers[p]) {
                    providers[p] = { size: 0, nb: 0, time: 0 };
                    if (p !== 'unknown' && p !== 'internal') nbTP++;
                }
                providers[p].size += r.size || 0;
                providers[p].nb++;
                if (r.time > providers[p].time) providers[p].time = r.time;
            });
            var entries = [];
            var ci = 0;
            for (var k in providers) {
                var color = k === 'internal' ? '#3b82f6' : k === 'unknown' ? '#64748b' : get_Color_fromindex(ci++);
                entries.push({
                    label: k === 'internal' ? 'INTERNAL' : k === 'unknown' ? 'UNKNOWN' : k,
                    size: providers[k].size,
                    nb: providers[k].nb,
                    time: providers[k].time,
                    color: color
                });
            }
            var sortKey = this.networkSortCol;
            entries.sort(function(a, b) {
                if (sortKey === 'request') return b.nb - a.nb;
                if (sortKey === 'time') return b.time - a.time;
                return b.size - a.size;
            });
            return { entries: entries, nbTP: nbTP };
        },

        get networkUnknownDomains() {
            var domains = [];
            this.networkRequests.forEach(function(r) {
                if ('' + r.provider === 'unknown' && domains.indexOf(r.domain) === -1) {
                    domains.push(r.domain);
                }
            });
            return domains;
        },

        get harFilteredEntries() {
            if (!this.harData) return [];
            var entries = this.harData.log.entries.slice();
            var f = this.harFilter.toLowerCase();
            if (f) {
                entries = entries.filter(function(e) {
                    return e.request.url.toLowerCase().indexOf(f) !== -1;
                });
            }
            var sf = this.harStatusFilter;
            if (sf !== 'all') {
                entries = entries.filter(function(e) {
                    var s = e.response.status;
                    if (sf === '2xx') return s < 300;
                    if (sf === '4xx') return s >= 400 && s < 500;
                    return s >= 500;
                });
            }
            var sb = this.harSortBy;
            entries.sort(function(a, b) {
                if (sb === 'status') return b.response.status - a.response.status;
                if (sb === 'size') return (b.response.content.size || 0) - (a.response.content.size || 0);
                return (b.time || 0) - (a.time || 0);
            });
            return entries;
        },

        _harStatusClass(status) {
            if (status >= 500) return 'bg-red-100 dark:bg-red-900/30 text-red-700 dark:text-red-300';
            if (status >= 400) return 'bg-amber-100 dark:bg-amber-900/30 text-amber-700 dark:text-amber-300';
            return 'bg-green-100 dark:bg-green-900/30 text-green-700 dark:text-green-300';
        },

        _highlightJson(obj) {
            if (!obj) return '';
            return JSON.stringify(obj, null, 2)
                .replace(/(&)/g, '&amp;')
                .replace(/(<)/g, '&lt;')
                .replace(/(\".*?\")(?=\s*:)/g, '<span style="color:#93c5fd">$1</span>')
                .replace(/:\s*(\".*?\")/g, ': <span style="color:#86efac">$1</span>')
                .replace(/:\s*(\d+|\btrue\b|\bfalse\b|\bnull\b)/g, ': <span style="color:#fbbf24">$1</span>');
        },

        _formatKb(bytes) {
            if (!bytes) return '0';
            return Math.round(bytes / 1024).toLocaleString() + ' KB';
        },

        // ═══ STEP MODAL ═══
        openStepModal(step) {
            if (!step) return;
            window.dispatchEvent(new CustomEvent('step-modal-open', {
                detail: {
                    test: step.test || this.exe.test,
                    testcase: step.testcase || this.exe.testcase || this.exe.testCase,
                    stepId: step.step || step.stepId
                }
            }));
        },

        openPropertyModal(prop) {
            if (!prop) return;
            var self = this;
            // Clone the property for the modal (live editing)
            var liveProp = JSON.parse(JSON.stringify(prop));
            // Normalize countries to flat array if needed
            if (liveProp.countries && liveProp.countries.length > 0 && typeof liveProp.countries[0] === 'object') {
                liveProp.countries = liveProp.countries.map(function(c) { return c.value || c; });
            }
            window.dispatchEvent(new CustomEvent('property-modal-open', {
                detail: {
                    property: liveProp,
                    countries: self._getCountries(),
                    canUpdate: true,
                    onField: function(key, val) { liveProp[key] = val; },
                    onCountryToggle: function(country) {
                        var arr = liveProp.countries || [];
                        var idx = arr.indexOf(country);
                        if (idx >= 0) arr.splice(idx, 1); else arr.push(country);
                        liveProp.countries = arr;
                    },
                    onSelectAll: function() {
                        liveProp.countries = (self._getCountries() || []).slice();
                    },
                    onDeselectAll: function() {
                        liveProp.countries = [];
                    },
                    onClose: function() {
                        // The property modal has a "Done" button, not Save
                        // So we do nothing special on close
                    }
                }
            }));
        },

        // ═══ MEDIA ═══
        getMediaUrl(file, w, h) {
            return 'ReadTestCaseExecutionMedia?filename=' + encodeURIComponent(file.fileName)
                + '&filetype=' + encodeURIComponent(file.fileType || '')
                + '&filedesc=' + encodeURIComponent(file.fileDesc || '')
                + '&id=' + this.exe.id
                + '&w=' + (w || 150)
                + '&h=' + (h || 100);
        },

        getMediaFullUrl(file) {
            return 'ReadTestCaseExecutionMedia?filename=' + encodeURIComponent(file.fileName)
                + '&filetype=' + encodeURIComponent(file.fileType || '')
                + '&filedesc=' + encodeURIComponent(file.fileDesc || '')
                + '&id=' + this.exe.id
                + '&r=true';
        },

        isImageFile(file) {
            const ext = (file.fileType || file.fileName || '').toLowerCase();
            return ext.endsWith('jpg') || ext.endsWith('jpeg') || ext.endsWith('png') || ext.endsWith('gif') || ext === 'JPG' || ext === 'PNG' || ext === 'GIF';
        },

        getFileIcon(file) {
            const ext = (file.fileType || '').toUpperCase();
            if (ext === 'HTML') return 'code';
            if (ext === 'JSON') return 'braces';
            if (ext === 'XML') return 'file-code';
            if (ext === 'TXT') return 'file-text';
            if (ext === 'PDF') return 'file';
            if (ext === 'MP4' || ext === 'WEBM') return 'video';
            if (ext === 'HAR') return 'network';
            return 'file';
        },

        // ═══ SMART PREVIEW HELPERS ═══
        _isVerifyAction(action) {
            var a = (action.action || '').toLowerCase();
            if (a.indexOf('verify') !== 0) return false;
            // Only show Expected/Got if at least one value is present
            return !!(action.value1 || action.value1Init || action.value2 || action.value2Init);
        },
        _isVerifyControl(ctrl) {
            var c = (ctrl.controlType || ctrl.control || '').toLowerCase();
            if (c.indexOf('verify') !== 0) return false;
            // Only show Expected/Got if at least one value is present
            return !!(ctrl.value1 || ctrl.value1Init || ctrl.value2 || ctrl.value2Init);
        },
        _isServiceAction(action) {
            var a = (action.action || '').toLowerCase();
            return a === 'callservice' || a === 'callsoapservice' || a === 'callservicewithbase';
        },
        _isScreenshotAction(action) {
            var a = (action.action || '').toLowerCase();
            return a === 'takescreenshot' || a === 'getpagesource';
        },
        _isImageFile(file) {
            var ext = (file.fileType || file.fileName || '').toUpperCase();
            return ext === 'JPG' || ext === 'JPEG' || ext === 'PNG' || ext === 'GIF';
        },
        _getScreenshots(fileList) {
            if (!fileList) return [];
            var self = this;
            return fileList.filter(function(f) { return self._isImageFile(f); });
        },
        _getDataFiles(fileList) {
            if (!fileList) return [];
            var self = this;
            return fileList.filter(function(f) { return !self._isImageFile(f) && f.fileType !== 'MP4' && f.fileType !== 'BIN'; });
        },
        _getFileByDesc(fileList, desc) {
            if (!fileList) return null;
            return fileList.find(function(f) { return f.fileDesc === desc; }) || null;
        },
        _loadMediaContent(file, callback) {
            if (!file) return;
            var url = this.getMediaFullUrl(file);
            $.ajax({
                url: url,
                dataType: 'text',
                success: function(data) {
                    // Try to pretty-print JSON
                    try {
                        var parsed = JSON.parse(data);
                        callback(JSON.stringify(parsed, null, 2));
                    } catch(e) {
                        callback(data);
                    }
                },
                error: function() {
                    callback('[Error loading content]');
                }
            });
        },
        openLightbox(url, label) {
            this.lightboxUrl = url;
            this.lightboxLabel = label || '';
        },
        openFileModal(file) {
            if (!file) return;
            // Use the transversal File.html modal (openModalFile is defined in File.html included via modalInclusions.jsp)
            if (typeof openModalFile === 'function') {
                openModalFile(null, null, 'VIEW', null, {
                    fileName: file.fileName,
                    fileType: file.fileType || '',
                    fileDesc: file.fileDesc || '',
                    level: file.level || '',
                    id: this.exe.id
                }, true);
            } else {
                // Fallback: open in new tab
                window.open(this.getMediaFullUrl(file), '_blank');
            }
        },
        reRunSame() {
            var self = this;
            var queueId = self.exe.queueID || self.exe.queueId;
            if (!queueId || queueId <= 0) {
                // Fallback to Run modal if no queueId
                self.reRun();
                return;
            }
            $.ajax({
                url: 'CreateTestCaseExecutionQueue',
                method: 'POST',
                data: {
                    id: queueId,
                    actionState: 'toQUEUED',
                    actionSave: 'save',
                    tag: self.exe.tag || ''
                },
                success: function(data) {
                    if (getAlertType(data.messageType) === 'success') {
                        var newQueueId = data.testCaseExecutionQueueList[0].id;
                        var url = './TestCaseExecutionV2.jsp?executionQueueId=' + encodeURIComponent(newQueueId);
                        window.location.replace(url);
                    } else {
                        showMessageMainPage(getAlertType(data.messageType), data.message || 'Could not queue execution', false, 60000);
                    }
                },
                error: function(jqXHR, textStatus, errorThrown) {
                    showUnexpectedError(jqXHR, textStatus, errorThrown);
                }
            });
        },

        // ═══ VISION MODAL (Live / Video) ═══
        openVisionModal(mode, url) {
            if (mode === 'live' || mode === 'control') {
                // Live/control URLs are on external servers (Guacamole/noVNC) that block iframe embedding
                // Same approach as legacy: open in a new tab
                window.open(url, '_blank');
                return;
            }
            // Video mode: open in modal with embedded player
            this.visionModal.mode = mode;
            this.visionModal.url = url;
            this.visionModal.fullscreen = false;
            this.visionModal.open = true;
            document.body.style.overflow = 'hidden';
            this.$nextTick(function() { if (window.lucide) lucide.createIcons(); }.bind(this));
        },

        closeVisionModal() {
            this.visionModal.open = false;
            document.body.style.overflow = '';
        },

        _getVideoUrl() {
            // Check exe.videos array first (legacy API)
            if (this.exe && this.exe.videos && this.exe.videos.length > 0) {
                return 'ReadTestCaseExecutionMedia?filename=' + encodeURIComponent(this.exe.videos[0]) + '&filedesc=Video&filetype=MP4&id=' + this.exe.id + '&r=true';
            }
            // Fallback: find MP4 in execution-level fileList
            if (this.exe && this.exe.fileList) {
                for (var i = 0; i < this.exe.fileList.length; i++) {
                    if (this.exe.fileList[i].fileType === 'MP4') {
                        return 'ReadTestCaseExecutionMedia?filename=' + encodeURIComponent(this.exe.fileList[i].fileName) + '&filedesc=Video&filetype=MP4&id=' + this.exe.id + '&r=true';
                    }
                }
            }
            // Deep scan: check step/action/control fileList for MP4
            if (this.steps) {
                for (var si = 0; si < this.steps.length; si++) {
                    var step = this.steps[si];
                    var actions = step.actions || [];
                    for (var ai = 0; ai < actions.length; ai++) {
                        var a = actions[ai];
                        if (a.fileList) {
                            for (var fi = 0; fi < a.fileList.length; fi++) {
                                if (a.fileList[fi].fileType === 'MP4') {
                                    return this.getMediaFullUrl(a.fileList[fi]);
                                }
                            }
                        }
                        var controls = a.controls || [];
                        for (var ci = 0; ci < controls.length; ci++) {
                            if (controls[ci].fileList) {
                                for (var cfi = 0; cfi < controls[ci].fileList.length; cfi++) {
                                    if (controls[ci].fileList[cfi].fileType === 'MP4') {
                                        return this.getMediaFullUrl(controls[ci].fileList[cfi]);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return null;
        },

        _hasVideo() {
            return !!this._getVideoUrl();
        },

        // ═══ HELPERS ═══
        _getCountries() {
            // Try to get countries from test case object
            if (this.testCaseObj && this.testCaseObj.countries) {
                return this.testCaseObj.countries.map(function(c) { return typeof c === 'object' ? (c.value || c.country) : c; });
            }
            // Fallback: get from execution
            if (this.exe && this.exe.country) {
                return [this.exe.country];
            }
            return [];
        },

        _updateFavicon(status) {
            try {
                // rgbToHex helper (inline since not loaded on V2 page)
                var _rgbHex = function(rgb) {
                    var parts = rgb.match(/\d+/g);
                    if (!parts) return '#999999';
                    return '#' + parts.map(function(x) { return parseInt(x).toString(16).padStart(2, '0'); }).join('');
                };
                if (!this._favicon) {
                    this._favicon = new Favico({
                        animation: 'slide',
                        bgColor: _rgbHex(getExeStatusRowColor(status))
                    });
                } else {
                    this._favicon.badge('');
                    this._favicon = new Favico({
                        animation: 'slide',
                        bgColor: _rgbHex(getExeStatusRowColor(status))
                    });
                }
                this._favicon.badge(status);
            } catch(e) { console.warn('[ExeV2] Favicon error:', e); }
        },

        _getStatusColor(status) {
            const map = {
                'OK': 'green', 'KO': 'red', 'FA': 'amber', 'PE': 'blue',
                'NE': 'slate', 'NA': 'yellow', 'WE': 'gray', 'CA': 'gray',
                'QU': 'slate', 'QE': 'slate', 'PA': 'purple'
            };
            return map[status] || 'slate';
        },

        _getStatusIcon(status) {
            const map = {
                'OK': 'check-circle', 'KO': 'x-circle', 'FA': 'alert-triangle',
                'PE': 'loader', 'NE': 'minus-circle', 'NA': 'alert-circle',
                'WE': 'help-circle', 'CA': 'ban', 'QU': 'clock', 'QE': 'alert-octagon'
            };
            return map[status] || 'help-circle';
        },

        _getStatusLabel(status) {
            const map = {
                'OK': 'OK', 'KO': 'KO', 'FA': 'FA', 'PE': 'Pending',
                'NE': 'Not Executed', 'NA': 'N/A', 'WE': 'Waiting',
                'CA': 'Cancelled', 'QU': 'Queued', 'QE': 'Queue Error'
            };
            return map[status] || status;
        },

        // Duration color coding — same thresholds as legacy (>5s orange, >30s red)
        _durationColorClass(start, end) {
            if (!start) return 'text-slate-400';
            if (!end || end <= 0) end = Date.now();
            var ms = end - start;
            if (ms < 0 || ms > 31536000000) return 'text-slate-400';
            if (ms > 30000) return 'text-red-500 font-bold';
            if (ms > 5000) return 'text-orange-500 font-bold';
            return 'text-slate-400';
        },

        formatDuration(startLong, endLong) {
            if (!startLong) return '-';
            // If no end time yet, use current time for live display
            if (!endLong || endLong <= 0) endLong = Date.now();
            var ms = endLong - startLong;
            // Guard against negative or absurd values (bad date)
            if (ms < 0 || ms > 31536000000) return '-';
            if (ms < 1000) return ms + 'ms';
            if (ms < 60000) return (ms / 1000).toFixed(1) + 's';
            return Math.floor(ms / 60000) + 'm ' + Math.floor((ms % 60000) / 1000) + 's';
        },

        formatDate(timestamp) {
            if (!timestamp) return '-';
            try {
                const d = new Date(timestamp);
                return d.toLocaleString();
            } catch(e) { return timestamp; }
        }
    };
}

// ═══ Network Chart Color Helpers ═══
function _httpStatusColor(status) {
    if (status && ('' + status).includes('nbE')) return '#a855f7';
    if (status && ('' + status).startsWith('2')) return '#22c55e';
    if (status && ('' + status).startsWith('3')) return '#86efac';
    if (status && ('' + status).startsWith('4')) return '#f97316';
    if (status && ('' + status).startsWith('5')) return '#ef4444';
    return '#94a3b8';
}

function _contentTypeColor(type) {
    if (!type) return '#1e293b';
    if (type.includes('img')) return '#a855f7';
    if (type.includes('html')) return '#22c55e';
    if (type.includes('content')) return '#86efac';
    if (type.includes('js')) return '#f97316';
    if (type.includes('css')) return '#3b82f6';
    if (type.includes('font')) return '#93c5fd';
    if (type.includes('media')) return '#ec4899';
    if (type.includes('other')) return '#94a3b8';
    return '#1e293b';
}
