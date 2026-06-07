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

        // Progress bar
        progress: 0,
        progressColor: '#2c7be5',

        // Properties
        properties: [],
        showSecondary: false,

        // Manual execution
        isManual: false,
        saveState: '',        // '' | 'dirty' | 'saving' | 'saved'

        // Queue
        queueInfo: null,
        queueRefreshTimer: null,

        // WebSocket
        ws: null,
        wsConnected: false,

        // Navigation
        lastExecutions: [],

        // Bugs
        falseNegative: false,

        // Lightbox
        lightboxUrl: null,

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

        // ═══ INIT ═══
        init() {
            console.info('[ExeV2] Initializing...');
            const executionId = GetURLParameter('executionId');
            const queueId = GetURLParameter('executionQueueId');
            const tabURL = GetURLParameter('tabactive');
            if (tabURL) this.tab = tabURL;

            // Load feature flags (getParameterString returns value synchronously, no callback)
            try {
                this.paramActivateWebSocket = getParameterString('cerberus_featureflipping_activatewebsocketpush', '', true) || 'N';
            } catch(e) { this.paramActivateWebSocket = 'N'; console.warn('[ExeV2] Could not load WS param:', e); }
            try {
                var wsPeriod = getParameterString('cerberus_featureflipping_websocketpushperiod', '', true);
                this.paramWebSocketPeriod = parseInt(wsPeriod) || 5000;
            } catch(e) { this.paramWebSocketPeriod = 5000; }

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

        // ═══ DATA LOADING ═══
        _loadExecution(executionId) {
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
                    this.falseNegative = !!(tce.currentFNB && tce.currentFNB !== '' && tce.currentFNB !== '0');

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

                    // WebSocket for live updates if PE
                    if (tce.controlStatus === 'PE') {
                        this.$nextTick(() => {
                            if (this.paramActivateWebSocket === 'Y') {
                                this._connectWebSocket(tce.id);
                            } else {
                                this._startPolling(tce.id);
                            }
                        });
                    }

                    this.mode = 'execution';
                    this._updateFavicon(tce.controlStatus);
                    this._updateProgress();
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
            return p;
        },

        // ═══ TABS ═══
        setTab(name) {
            this.tab = name;
            this.$nextTick(() => { if (window.lucide) lucide.createIcons(); });
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
            const protocol = location.protocol === 'https:' ? 'wss' : 'ws';
            const wsUrl = protocol + '://' + location.host + location.pathname.replace(/\/[^\/]*$/, '') + '/api/ws/execution/' + executionId;
            console.info('[ExeV2] Connecting WebSocket:', wsUrl);

            try {
                this.ws = new WebSocket(wsUrl);
                this.ws.onopen = () => { this.wsConnected = true; console.info('[ExeV2] WebSocket connected'); };
                this.ws.onmessage = (event) => {
                    try {
                        const data = JSON.parse(event.data);
                        this._onWebSocketMessage(data);
                    } catch(e) { console.warn('[ExeV2] WS message parse error:', e); }
                };
                this.ws.onerror = (err) => {
                    console.warn('[ExeV2] WebSocket error, falling back to polling');
                    this.wsConnected = false;
                    setTimeout(() => this._loadExecution(executionId), this.paramWebSocketPeriod);
                };
                this.ws.onclose = () => { this.wsConnected = false; };
            } catch(e) {
                console.warn('[ExeV2] WebSocket not available, using polling');
                this._startPolling(executionId);
            }
        },

        _onWebSocketMessage(data) {
            if (!data.testCaseExecution) return;
            var tce = data.testCaseExecution;
            var prevStatus = this.exe.controlStatus;

            // Merge all top-level fields from tce into exe, then reassign to trigger Alpine reactivity
            // This ensures new properties (remoteLiveUrl, remoteControlLiveUrl, videos, fileList) are detected
            var updatedExe = Object.assign({}, this.exe);
            Object.keys(tce).forEach(function(k) {
                if (k !== 'testCaseStepExecutionList' && k !== 'testCaseExecutionDataList') {
                    updatedExe[k] = tce[k];
                }
            });
            this.exe = updatedExe;

            // Incremental step merge — update returnCode/returnMessage/end in-place
            var newSteps = tce.testCaseStepExecutionList || [];
            newSteps.sort(function(a, b) { return a.sort - b.sort; });
            this._mergeStepUpdates(newSteps);

            // Properties — lightweight replace
            this.properties = (tce.testCaseExecutionDataList || []).map(function(p) { return this._normalizeProperty(p); }.bind(this));

            // Auto-focus on current step during PE
            if (tce.controlStatus === 'PE') {
                var peIdx = this.steps.findIndex(function(s) { return s.returnCode === 'PE'; });
                if (peIdx >= 0 && peIdx !== this.activeStepIndex) this.activeStepIndex = peIdx;
            }

            // Favicon on status change
            if (tce.controlStatus !== prevStatus) {
                this._updateFavicon(tce.controlStatus);
            }

            this.$nextTick(function() {
                if (window.lucide) lucide.createIcons();
                this._updateSidebarTop();
                this._updateProgress();
            }.bind(this));

            // If execution finished, close WS
            if (tce.controlStatus !== 'PE' && this.ws) {
                this.ws.close();
                this.ws = null;
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

        _startPolling(executionId) {
            var self = this;
            console.info('[ExeV2] Starting polling every', this.paramWebSocketPeriod, 'ms');
            if (this._pollingTimer) clearTimeout(this._pollingTimer);
            this._pollingTimer = setTimeout(function() {
                if (!self.exe || self.exe.controlStatus !== 'PE') return;
                $.ajax({
                    url: 'ReadTestCaseExecution',
                    data: { executionId: executionId, executionWithDependency: true },
                    dataType: 'json',
                    success: function(data) {
                        if (data.testCaseExecution) {
                            self._onWebSocketMessage(data);
                        }
                        // Keep polling if still PE
                        if (self.exe && self.exe.controlStatus === 'PE') {
                            self._startPolling(executionId);
                        }
                    },
                    error: function() {
                        // Retry on error
                        if (self.exe && self.exe.controlStatus === 'PE') {
                            self._startPolling(executionId);
                        }
                    }
                });
            }, this.paramWebSocketPeriod);
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
            this._updateProgress();
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
            // Inline navigation
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
            return a.indexOf('verify') === 0;
        },
        _isVerifyControl(ctrl) {
            var c = (ctrl.controlType || ctrl.control || '').toLowerCase();
            return c.indexOf('verify') === 0;
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
        openLightbox(url) {
            this.lightboxUrl = url;
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
                return 'ReadTestCaseExecutionMedia?filename=' + encodeURIComponent(this.exe.videos[0]) + '&filedesc=Video&filetype=MP4';
            }
            // Fallback: find MP4 in execution-level fileList
            if (this.exe && this.exe.fileList) {
                for (var i = 0; i < this.exe.fileList.length; i++) {
                    if (this.exe.fileList[i].fileType === 'MP4') {
                        return 'ReadTestCaseExecutionMedia?filename=' + encodeURIComponent(this.exe.fileList[i].fileName) + '&filedesc=Video&filetype=MP4&auto=true&r=true';
                    }
                }
            }
            return null;
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

        // ═══ PROGRESS BAR ═══
        _updateProgress() {
            // Compute color based on status using raw hex values
            var status = this.exe ? this.exe.controlStatus : '';
            var colorMap = {
                'OK': '#00d27a', 'KO': '#e63757', 'FA': '#f59e0b',
                'PE': '#2c7be5', 'NA': '#eab308', 'NE': '#aaaaaa',
                'CA': '#aaaaaa', 'WE': '#34495E', 'QU': '#BF00BF'
            };
            this.progressColor = colorMap[status] || '#2c7be5';

            // Compute total from testCaseObj definition if available (includes ALL steps/actions/controls
            // from the test case script, not just those already executed). This way if KO happens
            // at step 1 action 1, progress shows the real ratio vs the full test, not 100%.
            var total = 0, done = 0;

            if (this.testCaseObj && this.testCaseObj.steps && this.testCaseObj.steps.length > 0) {
                // Use testCaseObj definition for the total count
                var tcSteps = this.testCaseObj.steps;
                for (var i = 0; i < tcSteps.length; i++) {
                    total++; // step itself
                    var stepActions = tcSteps[i].actions || [];
                    for (var j = 0; j < stepActions.length; j++) {
                        total++; // action
                        var actionControls = stepActions[j].controls || [];
                        total += actionControls.length; // controls
                    }
                }
                // Count done from execution steps
                this.steps.forEach(function(s) {
                    if (s.returnCode && s.returnCode !== 'PE' && s.returnCode !== 'NE' && s.returnCode !== 'WE' && s.returnCode !== 'QU') done++;
                    (s.actions || []).forEach(function(a) {
                        if (a.returnCode && a.returnCode !== 'PE' && a.returnCode !== 'NE' && a.returnCode !== 'WE' && a.returnCode !== 'QU') done++;
                        (a.controls || []).forEach(function(c) {
                            if (c.returnCode && c.returnCode !== 'PE' && c.returnCode !== 'NE' && c.returnCode !== 'WE' && c.returnCode !== 'QU') done++;
                        });
                    });
                });
            } else if (this.steps && this.steps.length > 0) {
                // Fallback: compute from execution steps only
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
            } else {
                // No steps at all — only show 100% if status is OK
                this.progress = (status === 'OK') ? 100 : 0;
                return;
            }

            this.progress = total > 0 ? Math.round((done / total) * 100) : 0;
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
