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

function debugExecutionV2() {
    return {
        // ═══ STATE ═══
        executionId: null,
        executionUUID: null,

        exe: {},
        steps: [],
        // Last known-good static tree (test/step/action/control definitions). Some WS pushes
        // during a run carry a testCaseExecution whose testCaseObj.steps is empty/not set
        // (e.g. a push emitted before the engine has attached it) — falling back to this cache
        // instead of an empty list keeps the Instructions panel visible instead of it flashing
        // empty every time that happens.
        _staticSteps: [],

        // Debug-specific pending status. Kept up to date primarily by WebSocket pushes
        // (WebSocketService.notifyDebugPending, on the execution.delta.<id> channel), with
        // GET /status polling only as an initial baseline and a slow reconciliation fallback.
        status: { state: 'RUNNING', controlStatus: '', pendingStepId: null, pendingActionId: null, pendingActionDescription: '', pendingControlId: null, pendingControlDescription: '', pendingFailed: false },

        // The step/action/control currently being worked through — unlike status.pendingStepId
        // (which is null except while actually PAUSED waiting for "Next"), this stays put across
        // the whole pause-then-run cycle of a single action/control : it's only updated when a
        // *new* pause is reported, never cleared by the "resumed, now running" delta. Combined
        // with isRunning, this is what lets the dot turn blue only once "Next" is actually
        // clicked (see isStepActive/isActionActive/isControlActive) while still keeping the step
        // expanded for the item's whole lifetime, not just the brief paused sliver.
        activeStepId: null,
        activeActionId: null,
        activeControlId: null,

        loading: true,
        busy: false,          // true only while the next()/stop() POST itself is in flight
        finished: false,      // execution ended (OK/KO/FA/... or session expired)
        sessionExpired: false,
        errorMessage: '',

        // "Run to here" fast-forward : repeated next() calls until a target action/control has
        // executed, stopping immediately on the first KO/FA.
        autoRunning: false,
        autoRunTarget: null,   // { stepId, actionId, controlId } — controlId null when target is an action

        wsConnected: false,
        _wsSessionID: null,
        _wsUpdateEventName: null,
        _wsDeltaEventName: null,
        _onWsUpdateHandler: null,
        _onWsDeltaHandler: null,

        // Live view iframe : browsers don't fire an <iframe> "error" event when the embedded
        // page refuses via X-Frame-Options/CSP, so a load-within-timeout heuristic is used
        // instead (see _armLiveViewFallbackTimer).
        liveViewLoaded: false,
        liveViewFailed: false,
        _liveViewFallbackTimer: null,

        _reconciliationTimer: null,
        _redirectTimer: null,

        // Page source panel : shows the HTML source captured after the most recently executed
        // action/control (debug sessions force pageSource=2, so one is captured every time).
        pageSourceFile: null,
        pageSourceContent: '',
        pageSourceLoading: false,
        _pageSourceEditor: null,

        // "Get elements" : the interactive/notable elements found in the current page source,
        // each with a short description and an XPath locator (deterministic Jsoup parsing).
        elements: [],
        elementsLoading: false,
        elementsError: '',

        // ApplicationObjects of the current application, loaded once and matched to each
        // detected element by exact xpath == value equality (see aosForXpath).
        applicationObjects: [],
        _applicationObjectsLoadedFor: null,

        // Manual expand/collapse overrides, keyed by stepId — survives tree rebuilds (which
        // happen on every WS update) since it lives outside the (rebuilt) steps array.
        _stepExpandOverrides: {},

        // ═══ COMPUTED ═══
        get preSteps() { return this.steps.filter(s => s._isPreTesting); },
        get postSteps() { return this.steps.filter(s => s._isPostTesting); },
        get mainSteps() { return this.steps.filter(s => !s._isPreTesting && !s._isPostTesting); },

        get isWaitingForNext() {
            return !this.finished && this.status.state === 'WAITING_FOR_NEXT';
        },

        // True only while the engine is actually executing the active item (between the "Next"
        // click and the pause for whatever comes after it) — as opposed to isWaitingForNext,
        // which is true while it's sitting paused offering that same item for a "Next" click.
        get isRunning() {
            return !this.finished && this.status.state === 'RUNNING';
        },

        isActionPending(step, action) {
            return this.isWaitingForNext
                && this.status.pendingControlId == null
                && this.status.pendingStepId != null
                && this.status.pendingActionId != null
                && step.stepId === this.status.pendingStepId
                && action.actionId === this.status.pendingActionId;
        },

        isControlPending(step, action, control) {
            return this.isWaitingForNext
                && this.status.pendingControlId != null
                && step.stepId === this.status.pendingStepId
                && action.actionId === this.status.pendingActionId
                && control.controlId === this.status.pendingControlId;
        },

        // "Active" (blue dot) = the engine is actually EXECUTING this exact item right now — the
        // sliver between the "Next" click and the pause for whatever comes after it. While it's
        // merely the upcoming item, paused and offering "Next" (isWaitingForNext), it stays grey
        // like anything else not yet run ; only isActionPending/isControlPending highlight that
        // paused-and-offered state (the pulsing row glow), not this dot. Also excludes anything
        // already resolved (OK/KO/FA), so a completed item can't render as both its final color
        // and blue during the brief window before the next pause arrives.
        isStepActive(step) {
            return this.isRunning && step.stepId === this.activeStepId && !['OK', 'KO', 'FA'].includes(step.returnCode);
        },

        isActionActive(step, action) {
            return this.isRunning
                && this.activeControlId == null
                && step.stepId === this.activeStepId
                && action.actionId === this.activeActionId
                && !['OK', 'KO', 'FA'].includes(action.returnCode);
        },

        isControlActive(step, action, control) {
            return this.isRunning
                && this.activeControlId != null
                && step.stepId === this.activeStepId
                && action.actionId === this.activeActionId
                && control.controlId === this.activeControlId
                && !['OK', 'KO', 'FA'].includes(control.returnCode);
        },

        isAutoRunTarget(step, actionOrControl, isControl) {
            if (!this.autoRunTarget) return false;
            const t = this.autoRunTarget;
            if (isControl) {
                return t.controlId != null && t.stepId === step.stepId && t.controlId === actionOrControl.controlId;
            }
            return t.controlId == null && t.stepId === step.stepId && t.actionId === actionOrControl.actionId;
        },

        // ═══ INIT ═══
        init() {
            this.executionId = GetURLParameter('executionId');
            this.executionUUID = GetURLParameter('executionUUID');

            if (!this.executionId || !this.executionUUID) {
                this.errorMessage = 'Missing executionId/executionUUID parameter.';
                this.loading = false;
                return;
            }

            this._loadExecution(() => {
                this.loading = false;
                this._loadStatus();
                this._subscribeWs();
                this._startReconciliationPoll();
            });

            // The step-edit modal (include/transversal/TestCaseStep.html, already globally
            // available via modalInclusions.jsp) dispatches this once a step is saved — reload
            // the execution so the static tree (exe.testCaseObj.steps) reflects the edit, since
            // it can affect an action/control not yet reached by this session.
            window.addEventListener('step-modal-saved', (e) => {
                if (!e.detail) return;
                const test = this.exe.test;
                const testcase = this.exe.testcase || this.exe.testCase;
                const savedOwnStep = e.detail.test === test && e.detail.testcase === testcase;
                // Editing a library-linked step opens the modal on the library master's own
                // (test, testcase) — see editStep — so also reload when what was just saved IS
                // the master behind one of this tree's library-linked steps.
                const savedLibraryMaster = this.steps.some(s => s.isUsingLibraryStep
                    && s.libraryStepTest === e.detail.test
                    && s.libraryStepTestCase === e.detail.testcase);
                if (savedOwnStep || savedLibraryMaster) {
                    this._loadExecution();
                }
            });

            // The ApplicationObject modal (include/transversal/ApplicationObject.html) has no
            // dedicated "saved" event, only this generic close one — cheap enough to just
            // reload the whole list on every close rather than track save success separately.
            window.addEventListener('appobject-modal-close', () => this._loadApplicationObjects());

            this.$nextTick(() => { if (window.lucide) lucide.createIcons(); });
        },

        editStep(step) {
            // A library-linked step is just a read-only view of its master here : opening the
            // modal on this step's own (test, testcase, stepId) lets you "save" it, but the
            // backend never persists actions for isUsingLibraryStep rows (only the master's own
            // row gets its actions written) — so the edit is silently lost. Target the master's
            // location instead, same as the normal test case editor's library-step button does.
            const detail = step.isUsingLibraryStep
                ? { test: step.libraryStepTest, testcase: step.libraryStepTestCase, stepId: step.libraryStepStepId }
                : { test: this.exe.test, testcase: this.exe.testcase || this.exe.testCase, stepId: step.stepId };

            window.dispatchEvent(new CustomEvent('step-modal-open', { detail }));
        },

        _apiBase() {
            // REST controllers are served by the Spring DispatcherServlet mounted at /api/* —
            // the controller's own @RequestMapping("/public/debugexecutions/") is only the
            // remainder of the path after that mount point.
            return getCerberusBasePath() + 'api/public/debugexecutions/';
        },

        _apiHeaders(extra) {
            return Object.assign({ 'X-API-VERSION': '1' }, extra || {});
        },

        // ═══ WEBSOCKET ═══
        // Two channels, both already generic/whitelisted server-side (used by TestCaseExecutionV2.js
        // for EXECUTION_UPDATE ; EXECUTION_DELTA existed but was unused until WebSocketService.
        // notifyDebugPending started pushing debug pause/resume events on it) :
        //  - execution.update.<id> : full execution + step/action/control tree, pushed after
        //    every action/control (same mechanism the normal execution report page relies on).
        //  - execution.delta.<id>  : small debug-only payload (state, pending action/control),
        //    pushed exactly when a debug pause starts/ends.
        _subscribeWs() {
            const wsStore = Alpine.store('ws');
            if (!wsStore) {
                console.warn('[DebugV2] Alpine ws store not available, relying on polling only');
                return;
            }

            const updateChannel = CerberusWs.Channel.EXECUTION_UPDATE_ID(this.executionId);
            const deltaChannel = CerberusWs.Channel.EXECUTION_DELTA_ID(this.executionId);

            this._wsUpdateEventName = CerberusWs.Event.forChannel(updateChannel);
            this._wsDeltaEventName = CerberusWs.Event.forChannel(deltaChannel);

            this._onWsUpdateHandler = (event) => this._onWsUpdateMessage(event.detail);
            this._onWsDeltaHandler = (event) => this._onWsDeltaMessage(event.detail);

            document.addEventListener(this._wsUpdateEventName, this._onWsUpdateHandler);
            document.addEventListener(this._wsDeltaEventName, this._onWsDeltaHandler);

            const user = JSON.parse(sessionStorage.getItem('user') || '{}');
            const sender = user.login || user.user || 'anonymous';
            this._wsSessionID = 'debugexecution-' + this.executionId + '-' + sender;

            wsStore.whenConnected()
                .then(() => {
                    const ok = wsStore.send({
                        sender: sender,
                        subject: CerberusWs.Subject.SUBSCRIBE,
                        channels: [updateChannel, deltaChannel],
                        sessionID: this._wsSessionID
                    });
                    this.wsConnected = !!ok;
                    if (ok) console.info('[DebugV2] Subscribed to', updateChannel, deltaChannel);
                })
                .catch((e) => {
                    console.warn('[DebugV2] WS subscribe failed — relying on the reconciliation poll:', e);
                    this.wsConnected = false;
                });
        },

        _onWsUpdateMessage(message) {
            if (!message) return;
            const payload = message.payload || {};
            const tce = payload.testCaseExecution || payload;
            const pushedId = tce.id || tce.executionId;
            if (!pushedId || String(pushedId) !== String(this.executionId)) return;

            this.wsConnected = true;
            this._applyExecution(tce);
        },

        _onWsDeltaMessage(message) {
            if (!message) return;
            const payload = message.payload || {};
            const pushedId = payload.executionId;
            if (!pushedId || String(pushedId) !== String(this.executionId)) return;

            this.wsConnected = true;
            this._applyStatus(payload);
        },

        // ═══ DATA LOADING ═══
        _loadExecution(onDone) {
            $.ajax({
                url: 'ReadTestCaseExecution',
                data: { executionId: this.executionId, executionWithDependency: true },
                dataType: 'json',
                success: (data) => {
                    if (data.messageType === 'KO') {
                        showUnexpectedError(null, 'ERROR', data.message);
                        if (onDone) onDone();
                        return;
                    }
                    const tce = data.testCaseExecution;
                    if (!tce) {
                        showUnexpectedError(null, 'ERROR', 'Execution not found');
                        if (onDone) onDone();
                        return;
                    }
                    this._applyExecution(tce);
                    if (onDone) onDone();
                },
                error: (jqXHR, textStatus, errorThrown) => {
                    showUnexpectedError(jqXHR, textStatus, errorThrown);
                    if (onDone) onDone();
                }
            });
        },

        _applyExecution(tce) {
            // remoteLiveUrl/remoteControlLiveUrl are never persisted to DB (in-memory only on the
            // running execution object), so ReadTestCaseExecution never returns them — carry over
            // whatever a WS delta or /status call already taught us, or this would wipe them out.
            const previousLiveUrl = this.exe.remoteLiveUrl;
            const previousControlLiveUrl = this.exe.remoteControlLiveUrl;

            this.exe = tce;
            this.executionUUID = tce.executionUUID || this.executionUUID;
            this.exe.remoteLiveUrl = tce.remoteLiveUrl || previousLiveUrl;
            this.exe.remoteControlLiveUrl = tce.remoteControlLiveUrl || previousControlLiveUrl;

            if (this.exe.remoteLiveUrl && !previousLiveUrl) {
                this._armLiveViewFallbackTimer();
            }

            this._rebuildTree(tce);
            this._refreshPageSource();

            if (this.exe.application && this._applicationObjectsLoadedFor !== this.exe.application) {
                this._loadApplicationObjects();
            }

            document.title = 'Debug #' + tce.id + ' - ' + (tce.testcase || tce.testCase);

            if (tce.controlStatus && tce.controlStatus !== 'PE') {
                this.finished = true;
                this._goToReport();
            }

            this.$nextTick(() => { if (window.lucide) lucide.createIcons(); });
        },

        // ═══ TREE : static test case definition (exe.testCaseObj.steps, always the full list)
        // overlaid with execution-row results (exe.testCaseStepExecutionList, only present for
        // steps/actions/controls already reached) ═══
        _rebuildTree(tce) {
            const pushedStaticSteps = (tce.testCaseObj && tce.testCaseObj.steps) || [];
            if (pushedStaticSteps.length > 0) {
                this._staticSteps = pushedStaticSteps;
            }
            const staticSteps = this._staticSteps;
            const execIndex = this._indexExecutionRows(tce);

            const merged = staticSteps
                .slice()
                .sort((a, b) => a.sort - b.sort)
                .map(s => this._mergeStep(s, execIndex));

            this._autoExpand(merged);
            this.steps = merged;
        },

        _indexExecutionRows(tce) {
            const stepIndex = {};
            const actionIndex = {};
            const controlIndex = {};

            (tce.testCaseStepExecutionList || []).forEach(se => {
                stepIndex[se.step] = se;
                (se.testCaseStepActionExecutionList || []).forEach(ae => {
                    actionIndex[se.step + '|' + ae.sequence] = ae;
                    (ae.testCaseStepActionControlExecutionList || []).forEach(ce => {
                        controlIndex[se.step + '|' + ae.sequence + '|' + ce.control] = ce;
                    });
                });
            });

            return { stepIndex, actionIndex, controlIndex };
        },

        _mergeStep(staticStep, execIndex) {
            const stepExec = execIndex.stepIndex[staticStep.stepId];

            const step = {
                // Stable, content-derived key (not a fresh id per rebuild) so Alpine's x-for
                // recognizes "same step as before" across the frequent WS-driven rebuilds and
                // patches it in place, instead of destroying/recreating the whole subtree (and
                // its expand state, hover state, etc.) on every single push.
                _uid: 'step-' + staticStep.stepId,
                stepId: staticStep.stepId,
                sort: staticStep.sort,
                description: staticStep.description,
                conditionOperator: staticStep.conditionOperator,
                // Library-step pointer : when isUsingLibraryStep is true, this step's actions
                // are just a read-only view of the master living at libraryStepTest/TestCase/
                // StepId — editing must target THAT location (see editStep), or the backend
                // silently drops the change instead of persisting it anywhere.
                isUsingLibraryStep: staticStep.isUsingLibraryStep,
                libraryStepTest: staticStep.libraryStepTest,
                libraryStepTestCase: staticStep.libraryStepTestCase,
                libraryStepStepId: staticStep.libraryStepStepId,
                returnCode: stepExec ? stepExec.returnCode : null,
                start: stepExec ? stepExec.start : null,
                end: stepExec ? stepExec.end : null,
                _hasExecuted: !!stepExec
            };
            step._isPreTesting = (step.sort < 0 || (step.conditionOperator && step.conditionOperator.indexOf('Pre') >= 0) || step.sort == 0);
            step._isPostTesting = (step.sort > 9000 || (step.conditionOperator && step.conditionOperator.indexOf('Post') >= 0));

            step.actions = (staticStep.actions || [])
                .slice()
                .sort((a, b) => a.sort - b.sort)
                .map(a => this._mergeAction(a, staticStep.stepId, execIndex));

            step._nbActionsKO = step.actions.filter(a => a.returnCode === 'KO' || a.returnCode === 'FA').length;
            step._nbControlsKO = step.actions.reduce((n, a) => n + a.controls.filter(c => c.returnCode === 'KO' || c.returnCode === 'FA').length, 0);

            return step;
        },

        _mergeAction(staticAction, stepId, execIndex) {
            const actionExec = execIndex.actionIndex[stepId + '|' + staticAction.actionId];

            const action = {
                _uid: 'action-' + stepId + '-' + staticAction.actionId,
                stepId: stepId,
                actionId: staticAction.actionId,
                sort: staticAction.sort,
                action: staticAction.action,
                description: staticAction.description,
                returnCode: actionExec ? actionExec.returnCode : null,
                returnMessage: actionExec ? actionExec.returnMessage : null,
                fileList: actionExec ? (actionExec.fileList || []) : [],
                _hasExecuted: !!actionExec
            };

            action.controls = (staticAction.controls || [])
                .slice()
                .sort((a, b) => a.sort - b.sort)
                .map(c => this._mergeControl(c, stepId, staticAction.actionId, execIndex));

            return action;
        },

        _mergeControl(staticControl, stepId, actionId, execIndex) {
            const controlExec = execIndex.controlIndex[stepId + '|' + actionId + '|' + staticControl.controlId];

            return {
                _uid: 'control-' + stepId + '-' + actionId + '-' + staticControl.controlId,
                stepId: stepId,
                actionId: actionId,
                controlId: staticControl.controlId,
                controlType: staticControl.control,
                sort: staticControl.sort,
                description: staticControl.description,
                returnCode: controlExec ? controlExec.returnCode : null,
                fileList: controlExec ? (controlExec.fileList || []) : [],
                _hasExecuted: !!controlExec
            };
        },

        // ═══ EXPAND / COLLAPSE ═══
        // Long test cases stay readable by default : only the step currently being debugged (or
        // any step already showing a failure) auto-expands ; everything else is collapsed unless
        // the user explicitly toggled it. The active step always wins, even over a previous
        // manual collapse — leaving the step you're actively stepping through hidden would defeat
        // the point.
        _autoExpand(steps) {
            steps.forEach(s => {
                const isActive = s.stepId === this.activeStepId;
                if (isActive) {
                    s._expanded = true;
                } else if (Object.prototype.hasOwnProperty.call(this._stepExpandOverrides, s.stepId)) {
                    s._expanded = this._stepExpandOverrides[s.stepId];
                } else {
                    // Only the active step auto-expands ; everything else stays collapsed by
                    // default (unless it has a failure) to keep a long test case readable. Their
                    // "run to here" buttons aren't gated on the step being active though — click
                    // the chevron to expand any step and its buttons work exactly the same.
                    s._expanded = s._nbActionsKO > 0 || s._nbControlsKO > 0;
                }
            });
        },

        toggleStep(step) {
            step._expanded = !step._expanded;
            this._stepExpandOverrides[step.stepId] = step._expanded;
        },

        _loadStatus(onDone) {
            fetch(this._apiBase() + encodeURIComponent(this.executionUUID) + '/status', {
                headers: this._apiHeaders()
            })
                .then(r => r.json())
                .then(json => this._applyStatus(json.data || {}, onDone))
                .catch(e => {
                    console.error('[DebugV2] status poll failed:', e);
                    if (onDone) onDone();
                });
        },

        _applyStatus(data, onDone) {
            if (!data.executionId || data.state === 'FINISHED') {
                this.finished = true;
                this.status = { state: 'FINISHED', controlStatus: data.controlStatus || this.exe.controlStatus, pendingStepId: null, pendingActionId: null, pendingActionDescription: '', pendingControlId: null, pendingControlDescription: '', pendingFailed: false };
                this.activeStepId = null;
                this.activeActionId = null;
                this.activeControlId = null;
                this._goToReport();
            } else {
                this.status = Object.assign({
                    pendingStepId: null, pendingActionId: null, pendingActionDescription: '',
                    pendingControlId: null, pendingControlDescription: '', pendingFailed: false
                }, data);

                // Only a genuine new pause moves the "active" pointer — the "resumed, now
                // running" delta (pendingStepId=null) must NOT clear it, or the step being
                // actively executed would drop out of "active" for the whole run duration.
                if (data.pendingStepId != null) {
                    this.activeStepId = data.pendingStepId;
                    this.activeActionId = data.pendingActionId != null ? data.pendingActionId : null;
                    this.activeControlId = data.pendingControlId != null ? data.pendingControlId : null;
                }
            }

            // remoteLiveUrl/remoteControlLiveUrl live only on the in-memory execution object
            // (never persisted to DB), so ReadTestCaseExecution can never see them while
            // running — WS deltas / this status endpoint are the only sources for them.
            if (data.remoteLiveUrl && !this.exe.remoteLiveUrl) {
                this.exe.remoteLiveUrl = data.remoteLiveUrl;
                this._armLiveViewFallbackTimer();
            }
            if (data.remoteControlLiveUrl && !this.exe.remoteControlLiveUrl) {
                this.exe.remoteControlLiveUrl = data.remoteControlLiveUrl;
            }

            // The pending pointer moved : re-run the auto-expand rule so the newly active step
            // opens (without touching steps the user explicitly toggled).
            this._autoExpand(this.steps);

            if (onDone) onDone();
        },

        _findMergedItem(stepId, actionId, controlId) {
            const step = this.steps.find(s => s.stepId === stepId);
            if (!step) return null;
            const action = (step.actions || []).find(a => a.actionId === actionId);
            if (!action) return null;
            if (controlId == null) return action;
            return (action.controls || []).find(c => c.controlId === controlId) || null;
        },

        // ═══ PAGE SOURCE ═══
        // Debug sessions force pageSource=2 (capture after every action/control), so the most
        // recently executed action/control always has an HTML source file — walk the tree in
        // execution order (pre steps, then main, then post) and remember the last one reached.
        _lastExecutedItem() {
            const orderedSteps = [].concat(this.preSteps, this.mainSteps, this.postSteps);
            let last = null;
            orderedSteps.forEach(step => {
                (step.actions || []).forEach(action => {
                    if (action._hasExecuted) last = action;
                    (action.controls || []).forEach(control => {
                        if (control._hasExecuted) last = control;
                    });
                });
            });
            return last;
        },

        _refreshPageSource() {
            const item = this._lastExecutedItem();
            // Some control types (e.g. "Get Page Source") record their own HTML capture AND
            // get an automatic one from the debug session's forced pageSource=2 — when both are
            // present, the automatic one is appended last and is the one to show ; picking the
            // first (as this used to) can show an unrelated, earlier capture instead.
            const htmlFiles = item ? (item.fileList || []).filter(f => f.fileType === 'HTML') : [];
            const file = htmlFiles.length > 0 ? htmlFiles[htmlFiles.length - 1] : null;

            if (!file) {
                this.pageSourceFile = null;
                this.pageSourceContent = '';
                this.elements = [];
                this.elementsError = '';
                return;
            }

            if (this.pageSourceFile && this.pageSourceFile.fileName === file.fileName) {
                return; // already loaded, nothing changed
            }

            this.pageSourceFile = file;
            this.pageSourceLoading = true;
            // A new source snapshot makes any previously extracted elements list stale.
            this.elements = [];
            this.elementsError = '';

            $.get('ReadTestCaseExecutionMedia', {
                filename: file.fileName,
                filetype: file.fileType,
                filedesc: file.fileDesc,
                auto: true,
                autoContentType: 'N'
            })
                .done((data) => {
                    this.pageSourceContent = typeof data === 'string' ? data : JSON.stringify(data);
                    this.pageSourceLoading = false;
                    this.$nextTick(() => this._renderPageSourceEditor());
                    // Deterministic (Jsoup, no AI call) and near-instant, so it's run
                    // automatically on every new capture rather than waiting for a manual click.
                    this.extractElements();
                })
                .fail((jqXHR, textStatus, errorThrown) => {
                    console.error('[DebugV2] Unable to load page source:', textStatus, errorThrown);
                    this.pageSourceContent = '';
                    this.pageSourceLoading = false;
                });
        },

        _renderPageSourceEditor() {
            const el = document.getElementById('v2DbgPageSourceContent');
            if (!el || !window.ace) return;

            if (!this._pageSourceEditor) {
                this._pageSourceEditor = ace.edit(el);
                this._pageSourceEditor.setTheme('ace/theme/chrome');
                this._pageSourceEditor.setOptions({ maxLines: 30, readOnly: true });
            }

            const session = this._pageSourceEditor.getSession();
            let mode = '';
            if (typeof defineAceMode === 'function') {
                mode = defineAceMode(this.pageSourceContent) || '';
                if (mode) session.setMode(mode);
            }
            session.setValue(this.pageSourceContent || '');

            // Debug page-source captures come back as a single unindented line per tag (no
            // pretty-printing) — technically valid, correctly syntax-highlighted HTML, but
            // unreadable in practice. Re-indent with the same js-beautify convention already
            // used by include/transversal/File.html / global.js's showTextArea.
            const jsbOpts = { indent_size: 2 };
            if (mode.endsWith('json') && typeof js_beautify === 'function') {
                session.setValue(js_beautify(session.getValue(), jsbOpts));
            } else if (mode.endsWith('xml') && typeof html_beautify === 'function') {
                session.setValue(html_beautify(session.getValue(), jsbOpts));
            }
        },

        // Extracts the interactive/notable elements of the currently displayed page source, each
        // with a short description and an XPath locator (see pageSourcePanel.html). Runs
        // automatically after every new capture (see _refreshPageSource) ; this button is a
        // manual re-run, e.g. after a transient network failure.
        async extractElements() {
            if (!this.pageSourceContent || this.elementsLoading) return;

            this.elementsLoading = true;
            this.elementsError = '';

            try {
                const r = await fetch(this._apiBase() + 'elements', {
                    method: 'POST',
                    headers: this._apiHeaders({ 'Content-Type': 'application/json' }),
                    body: JSON.stringify({ pageSource: this.pageSourceContent })
                });

                const rawText = await r.text();
                let json = null;
                try { json = JSON.parse(rawText); } catch (parseErr) { /* not JSON */ }

                if (!r.ok || !json || !json.data) {
                    console.error('[DebugV2] extractElements server response (' + r.status + '):', rawText);
                    throw new Error((json && (json.message || json.debugMessage)) || ('HTTP ' + r.status));
                }

                this.elements = (json.data.elements || []).map(el => Object.assign({ _copied: false, _highlighting: false, _aoExpanded: false }, el));
                this.$nextTick(() => { if (window.lucide) lucide.createIcons(); });
            } catch (e) {
                console.error('[DebugV2] extractElements failed:', e);
                this.elementsError = 'Unable to retrieve the elements list from the AI.';
                this.elements = [];
            } finally {
                this.elementsLoading = false;
            }
        },

        // Copies "xpath=<value>" (the Cerberus locator prefix convention) to the clipboard and
        // briefly flashes a checkmark on the row's copy button.
        copyXpath(el) {
            navigator.clipboard.writeText('xpath=' + (el.xpath || ''))
                .then(() => {
                    el._copied = true;
                    setTimeout(() => { el._copied = false; }, 1500);
                })
                .catch(e => {
                    console.error('[DebugV2] copyXpath failed:', e);
                    this.errorMessage = 'Unable to copy to clipboard.';
                });
        },

        // ═══ APPLICATION OBJECTS ═══
        // ApplicationObjects matching a detected element (same application, exact xpath ==
        // value equality) : viewed/edited/deleted via the existing global AO modal
        // (include/transversal/ApplicationObject.html), created via a lightweight direct POST
        // (the modal's own create/edit flow needs a screenshot upload that doesn't apply here).

        aosForXpath(xpath) {
            return this.applicationObjects.filter(ao => ao.value === xpath);
        },

        toggleAoExpand(el) {
            el._aoExpanded = !el._aoExpanded;
            if (el._aoExpanded) {
                this.$nextTick(() => { if (window.lucide) lucide.createIcons(); });
            }
        },

        _loadApplicationObjects() {
            const application = this.exe.application;
            if (!application) return;
            this._applicationObjectsLoadedFor = application;

            $.get('ReadApplicationObject', { application: application })
                .done((data) => {
                    if (this._applicationObjectsLoadedFor === application) {
                        this.applicationObjects = (data && data.contentTable) || [];
                    }
                })
                .fail((jqXHR, textStatus, errorThrown) => {
                    console.error('[DebugV2] Unable to load application objects:', textStatus, errorThrown);
                });
        },

        // PAGENAME_ELEMENTNAME-style suggestion (see generate_applicationobject.prompt), minus
        // the page name since this page has no notion of one — just a reasonable starting point
        // for the object-name prompt, not a guaranteed-unique final value.
        _suggestObjectName(description) {
            const slug = (description || 'ELEMENT')
                .toUpperCase()
                .replace(/[^A-Z0-9]+/g, '_')
                .replace(/^_+|_+$/g, '');
            return (slug || 'ELEMENT').slice(0, 60);
        },

        addApplicationObject(el) {
            const application = this.exe.application;
            if (!application) return;

            const objectName = window.prompt('Object name for this ApplicationObject :', this._suggestObjectName(el.description));
            if (!objectName) return;

            const formData = new FormData();
            formData.append('application', application);
            formData.append('object', objectName);
            formData.append('value', el.xpath || '');

            $.ajax({
                url: 'CreateApplicationObject',
                method: 'POST',
                data: formData,
                processData: false,
                contentType: false
            })
                .done((data) => {
                    if (getAlertType(data.messageType) !== 'success') {
                        this.errorMessage = data.message || 'Unable to create the ApplicationObject.';
                        return;
                    }
                    this._loadApplicationObjects();
                })
                .fail((jqXHR, textStatus, errorThrown) => {
                    console.error('[DebugV2] addApplicationObject failed:', textStatus, errorThrown);
                    this.errorMessage = 'Unable to create the ApplicationObject.';
                });
        },

        editApplicationObject(ao) {
            // Global function from include/transversal/ApplicationObject.html (already loaded
            // via modalInclusions.jsp) — 'testCaseScript' page mode pre-fills application+object
            // without requiring the object-list page's own data grid.
            openModalApplicationObject(ao.application, ao.object, 'EDIT', 'testCaseScript');
        },

        deleteApplicationObject(ao) {
            if (!confirm('Delete ApplicationObject "' + ao.object + '" ?')) return;

            $.post('DeleteApplicationObject', { application: ao.application, object: ao.object })
                .done((data) => {
                    if (getAlertType(data.messageType) !== 'success') {
                        this.errorMessage = data.message || 'Unable to delete the ApplicationObject.';
                        return;
                    }
                    this._loadApplicationObjects();
                })
                .fail((jqXHR, textStatus, errorThrown) => {
                    console.error('[DebugV2] deleteApplicationObject failed:', textStatus, errorThrown);
                    this.errorMessage = 'Unable to delete the ApplicationObject.';
                });
        },

        // ═══ HIGHLIGHT ═══
        // Ad hoc command against the live paused session — outlines the element in yellow/red
        // for ~2s server-side, bypassing the step/action engine entirely (see
        // DebugExecutionService.highlightElement).
        async highlightElement(el) {
            if (el._highlighting) return;
            el._highlighting = true;
            this.errorMessage = '';

            try {
                const r = await fetch(this._apiBase() + encodeURIComponent(this.executionUUID) + '/highlight', {
                    method: 'POST',
                    headers: this._apiHeaders({ 'Content-Type': 'application/json' }),
                    body: JSON.stringify({ xpath: el.xpath })
                });

                const rawText = await r.text();
                let json = null;
                try { json = JSON.parse(rawText); } catch (parseErr) { /* not JSON */ }

                if (!r.ok) {
                    console.error('[DebugV2] highlightElement server response (' + r.status + '):', rawText);
                    throw new Error((json && (json.message || json.debugMessage)) || ('HTTP ' + r.status));
                }
            } catch (e) {
                console.error('[DebugV2] highlightElement failed:', e);
                this.errorMessage = 'Unable to highlight this element on the live page.';
            } finally {
                el._highlighting = false;
            }
        },

        // ═══ LIVE VIEW ═══
        onLiveViewLoad() {
            this.liveViewLoaded = true;
            this.liveViewFailed = false;
            if (this._liveViewFallbackTimer) {
                clearTimeout(this._liveViewFallbackTimer);
                this._liveViewFallbackTimer = null;
            }
        },

        _armLiveViewFallbackTimer() {
            if (this._liveViewFallbackTimer) clearTimeout(this._liveViewFallbackTimer);
            this.liveViewLoaded = false;
            this.liveViewFailed = false;
            this._liveViewFallbackTimer = setTimeout(() => {
                if (!this.liveViewLoaded) this.liveViewFailed = true;
            }, 4000);
        },

        // ═══ ACTIONS ═══
        async _sendNext() {
            try {
                const r = await fetch(this._apiBase() + encodeURIComponent(this.executionUUID) + '/next', {
                    method: 'POST',
                    headers: this._apiHeaders()
                });
                if (!r.ok) throw new Error('HTTP ' + r.status);
                return true;
            } catch (e) {
                console.error('[DebugV2] next() failed:', e);
                this.errorMessage = 'Unable to advance to the next action.';
                return false;
            }
        },

        async next() {
            if (this.busy || this.finished || !this.isWaitingForNext || this.autoRunning) return;
            this.busy = true;
            this.errorMessage = '';
            await this._sendNext();
            this.busy = false;
        },

        // Only meaningful when status.pendingFailed is true : re-runs the action/control that
        // just failed (the engine reloads its definition fresh from DB first), instead of
        // accepting the failure and moving on.
        async retry() {
            if (this.busy || this.finished || !this.isWaitingForNext || !this.status.pendingFailed || this.autoRunning) return;
            this.busy = true;
            this.errorMessage = '';
            try {
                const r = await fetch(this._apiBase() + encodeURIComponent(this.executionUUID) + '/retry', {
                    method: 'POST',
                    headers: this._apiHeaders()
                });
                if (!r.ok) throw new Error('HTTP ' + r.status);
            } catch (e) {
                console.error('[DebugV2] retry() failed:', e);
                this.errorMessage = 'Unable to retry the action.';
            }
            this.busy = false;
        },

        stop() {
            this.autoRunning = false; // cancel any fast-forward in progress
            if (this.busy) return;
            if (!confirm('Stop this debug session?')) return;

            this.busy = true;
            this.errorMessage = '';

            fetch(this._apiBase() + encodeURIComponent(this.executionUUID) + '/stop', {
                method: 'POST',
                headers: this._apiHeaders()
            })
                .then(() => {
                    this.finished = true;
                    this._goToReport();
                    this._loadExecution(() => { this.busy = false; });
                })
                .catch(e => {
                    console.error('[DebugV2] stop() failed:', e);
                    this.errorMessage = 'Unable to stop the debug session.';
                    this.busy = false;
                });
        },

        // "Run to here" : click on the fast-forward icon next to any action/control to keep
        // clicking next() automatically until that specific action/control has executed,
        // stopping immediately if anything KO/FA along the way.
        async runTo(stepId, actionId, controlId) {
            if (this.autoRunning || this.finished || this.busy || !this.isWaitingForNext) return;

            this.autoRunning = true;
            this.autoRunTarget = { stepId, actionId, controlId };
            this.errorMessage = '';

            try {
                while (this.autoRunning && !this.finished && this.isWaitingForNext) {
                    const prevStepId = this.status.pendingStepId;
                    const prevActionId = this.status.pendingActionId;
                    const prevControlId = this.status.pendingControlId;
                    const wasAtTarget = prevStepId === stepId && prevActionId === actionId && prevControlId === controlId;

                    const ok = await this._sendNext();
                    if (!ok) break;

                    await this._waitForNextPause(prevStepId, prevActionId, prevControlId);

                    if (!this.autoRunning) break; // cancelled mid-flight

                    const completed = this._findMergedItem(prevStepId, prevActionId, prevControlId);
                    if (completed && (completed.returnCode === 'KO' || completed.returnCode === 'FA')) {
                        this.errorMessage = 'Exécution automatique arrêtée : "' + (completed.description || completed.action || completed.controlType || '') + '" a échoué (' + completed.returnCode + ').';
                        break;
                    }

                    if (wasAtTarget) break;
                }
            } finally {
                this.autoRunning = false;
                this.autoRunTarget = null;
            }
        },

        cancelAutoRun() {
            this.autoRunning = false;
        },

        // Once the session is finished (naturally, stopped, or expired), this page has nothing
        // left to do — leave the "finished" banner up briefly, then hand off to the execution
        // report page.
        _goToReport() {
            if (this._redirectTimer) return;
            this._redirectTimer = setTimeout(() => {
                window.location.href = 'TestCaseExecutionV2.jsp?executionId=' + encodeURIComponent(this.executionId);
            }, 1500);
        },

        // Watches the LOCAL reactive status (kept live by the WS delta handler above) until it
        // settles on a new pending item — no extra network polling needed, the WS push already
        // updates this.status asynchronously in the background.
        _waitForNextPause(prevStepId, prevActionId, prevControlId) {
            const maxAttempts = 200; // ~30s safety cap at 150ms
            let attempts = 0;

            return new Promise((resolve) => {
                const check = () => {
                    attempts++;
                    if (this.finished) { resolve(); return; }
                    const changed = this.status.pendingStepId !== prevStepId
                        || this.status.pendingActionId !== prevActionId
                        || this.status.pendingControlId !== prevControlId;
                    if (this.isWaitingForNext && changed) { resolve(); return; }
                    if (attempts >= maxAttempts) { resolve(); return; }
                    setTimeout(check, 150);
                };
                check();
            });
        },

        // Belt-and-suspenders : catches a missed WS message (reconnect gap, backgrounded tab)
        // and the backend's idle-timeout auto-stop (which nothing would otherwise notify us of).
        _startReconciliationPoll() {
            if (this._reconciliationTimer) clearInterval(this._reconciliationTimer);
            this._reconciliationTimer = setInterval(() => {
                if (this.finished) {
                    clearInterval(this._reconciliationTimer);
                    this._reconciliationTimer = null;
                    return;
                }
                if (this.busy || this.autoRunning) return;

                const wasWaiting = this.isWaitingForNext;
                this._loadStatus(() => {
                    if (this.finished && wasWaiting) {
                        // We were paused (nothing the user did could have ended it) and now the
                        // session is gone : the idle-timeout sweep must have force-stopped it.
                        this.sessionExpired = true;
                    }
                });
            }, 5000);
        }
    };
}
