/*
 * Cerberus Copyright (C) 2013 - 2025 cerberustesting
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 * TestCaseScriptV2.js — Alpine.js Store + Component
 */

var _v2_uid = 0;
function v2uid() { return '__v2_' + (++_v2_uid); }
var importInfoIdx = 0;
if (typeof generateImportInfoId === 'undefined') {
    function generateImportInfoId(stepInfo) {
        var hash = 0;
        var strval = stepInfo.description + stepInfo.test + '-' + stepInfo.testCase + '-' + stepInfo.sort;
        if (strval.length === 0) return hash;
        for (var i = 0; i < strval.length; i++) {
            var char = strval.charCodeAt(i);
            hash = ((hash << 5) - hash) + char;
            hash = hash & hash;
        }
        return hash;
    }
}

// Fallback — getHistoryTestcase is defined in legacy TestCaseScript.js
// which is NOT included in V2. We define it here if missing.
if (typeof getHistoryTestcase !== 'function') {
    function getHistoryTestcase(object) {
        return { id: object.test + '-' + object.testcase, test: object.test, testcase: object.testcase, description: object.description };
    }
}

function scriptV2() {
    return {
        // ── State ──
        tab: 'steps',
        steps: [],
        properties: [],
        inheritedProperties: [],
        testInfo: { test: '', testcase: '', testCase: '', description: '', application: '', appType: '', system: '', version: 0, usrCreated: '', usrModif: '', dateCreated: '', dateModif: '', lastRunId: 0, status: '', statusList: [] },
        canUpdate: false,
        hasPermissionsDelete: false,
        activeStepIndex: -1,
        autoSaveEnabled: false,
        saveState: '', // '' | 'dirty' | 'saving' | 'saved'
        undoStack: [],
        redoStack: [],
        clipboardType: null,  // 'step' | 'action' | null — tracks system clipboard content
        propertySearch: '',
        inheritedSearch: '',
        propertyFilter: 'all',
        actionGroups: [],
        controlGroups: [],
        actionFlatItems: [],
        controlFlatItems: [],
        _saveTimer: null,
        _dragIdx: -1,
        _dragControlInfo: null,
        lastRunCountry: '',
        lastRunEnvironment: '',
        lastRunRobot: '',

        // ── Switcher state ──
        testFolders: [],
        testCases: [],
        testFoldersLoaded: false,

        // ── Computed ──
        get activeStep() { return this.steps[this.activeStepIndex] || null; },
        // Build cross-reference maps once
        _propCrossRef() {
            var localNames = {};
            this.properties.forEach(p => { localNames[p.property] = true; });
            var inhNames = {};
            this.inheritedProperties.forEach(p => { inhNames[p.property] = true; });
            return { localNames, inhNames };
        },
        get filteredLocalProperties() {
            var { inhNames } = this._propCrossRef();
            let list = this.properties.map(p => {
                p._isInherited = false;
                p._isUnused = this._isPropertyUnused(p.property);
                p._overridesInherited = !!inhNames[p.property];
                return p;
            });
            // filter
            if (this.propertyFilter === 'unused') list = list.filter(p => p._isUnused);
            if (this.propertyFilter && this.propertyFilter.startsWith('type:')) {
                var t = this.propertyFilter.substring(5);
                list = list.filter(p => p.type === t);
            }
            // search
            if (this.propertySearch) {
                const s = this.propertySearch.toLowerCase();
                list = list.filter(p => (p.property || '').toLowerCase().includes(s) || (p.type || '').toLowerCase().includes(s) || (p.value1 || '').toLowerCase().includes(s));
            }
            return list;
        },
        get filteredInheritedProperties() {
            var { localNames } = this._propCrossRef();
            let list = this.inheritedProperties.map(p => {
                p._isInherited = true; p._isUnused = false;
                p._hasLocalOverride = !!localNames[p.property];
                return p;
            });
            // search (uses own search field)
            const q = (this.inheritedSearch || '').toLowerCase();
            if (q) {
                list = list.filter(p => (p.property || '').toLowerCase().includes(q) || (p.type || '').toLowerCase().includes(q) || (p.value1 || '').toLowerCase().includes(q));
            }
            return list;
        },
        // Keep filteredProperties for backward compat (modal indexing uses it)
        get filteredProperties() {
            return [...this.filteredLocalProperties, ...this.filteredInheritedProperties];
        },
        get allPropertiesCount() { return this.properties.length + this.inheritedProperties.length; },
        get localPropertiesCount() { return this.properties.length; },
        get inheritedPropertiesCount() { return this.inheritedProperties.length; },
        get unusedPropertiesCount() { return this.properties.filter(p => this._isPropertyUnused(p.property)).length; },

        // ── Init ──
        init() {
            this._buildActionGroups();
            this._buildControlGroups();
            // Expose flat lists on window for dropdown loaders (child x-data scopes)
            window._v2ActionItems = this.actionFlatItems;
            window._v2ControlItems = this.controlFlatItems;
            const test = GetURLParameter('test');
            const testcase = GetURLParameter('testcase');
            const tabURL = GetURLParameter('tabactive');
            if (tabURL) this.tab = tabURL;
            if (test && testcase) this._loadTestCase(test, testcase);
            this._loadTestFolders();
            // Keyboard shortcuts
            document.addEventListener('keydown', (e) => {
                if ((e.ctrlKey || e.metaKey) && e.key === 'z') { e.preventDefault(); this.undo(); }
                if ((e.ctrlKey || e.metaKey) && e.key === 'y') { e.preventDefault(); this.redo(); }
                if ((e.ctrlKey || e.metaKey) && e.key === 's') { e.preventDefault(); this.save(); }
            });
            window.addEventListener('beforeunload', (e) => { if (this.saveState === 'dirty') { e.preventDefault(); e.returnValue = ''; } });
            // Browser back/forward navigation
            window.addEventListener('popstate', (e) => {
                if (e.state && e.state.test && e.state.testcase) {
                    this.saveState = '';
                    this.undoStack = [];
                    this.redoStack = [];
                    this.activeStepIndex = -1;
                    this._loadTestCase(e.state.test, e.state.testcase);
                }
            });
            // Reload data when step modal saves
            window.addEventListener('step-modal-saved', (e) => {
                if (e.detail && e.detail.test === this.testInfo.test && e.detail.testcase === this.testInfo.testcase) {
                    this._loadTestCase(this.testInfo.test, this.testInfo.testcase);
                }
            });
            // Reload data when header modal saves (description, status, etc.)
            window.addEventListener('testcase-header-modal-close', () => {
                this._loadTestCase(this.testInfo.test, this.testInfo.testcase);
            });
            // Check clipboard on focus (for cross-tab paste detection)
            var self = this;
            window.addEventListener('focus', function() { self._checkClipboard(); });
            this._checkClipboard();
            this.$nextTick(() => {
                if (window.lucide) lucide.createIcons();
                // Calculate sticky sidebar offset below the header
                this._updateSidebarTop();
            });
        },

        _updateSidebarTop() {
            var header = document.getElementById('v2Header');
            if (header) {
                // The header is sticky top:0 inside crb_main. We need the sidebar to sit below it.
                var headerH = header.offsetHeight + 16; // +16 for mb-4 gap
                document.documentElement.style.setProperty('--v2-sidebar-top', headerH + 'px');
            }
        },

        // ── Data Loading ──
        _loadTestCase(test, testcase) {
            this.testInfo.test = test;
            this.testInfo.testcase = testcase;
            console.info('[V2] Loading TestCase:', test, testcase);
            $.ajax({
                url: 'ReadTestCase',
                data: { test: test, testCase: testcase, withSteps: true, system: getSys() },
                dataType: 'json',
                success: (data) => {
                    console.info('[V2] ReadTestCase response:', data);
                    if (data.messageType === 'KO') { showUnexpectedError(null, 'ERROR', data.message); return; }
                    try { saveHistory(getHistoryTestcase(data.contentTable[0]), 'historyTestcases', 5); } catch(e) { console.warn('[V2] saveHistory failed:', e); }
                    this.canUpdate = data.hasPermissionsUpdate;
                    this.hasPermissionsDelete = data.hasPermissionsDelete;
                    const tc = data.contentTable[0];
                    console.info('[V2] TestCase object keys:', Object.keys(tc));
                    console.info('[V2] Steps raw:', tc.steps);
                    console.info('[V2] Steps count:', (tc.steps || []).length);
                    if (tc.steps && tc.steps.length > 0) {
                        console.info('[V2] First step:', JSON.stringify(tc.steps[0]).substring(0, 500));
                        console.info('[V2] First step actions count:', (tc.steps[0].actions || []).length);
                        if (tc.steps[0].actions && tc.steps[0].actions.length > 0) {
                            console.info('[V2] First action:', JSON.stringify(tc.steps[0].actions[0]));
                        }
                    }
                    this.testInfo.description = tc.description;
                    this.testInfo.application = tc.application;
                    this.testInfo.system = tc.system || '';
                    this.testInfo.version = tc.version;
                    this.testInfo.testCase = tc.testcase || tc.testCase || testcase;
                    this.testInfo.usrCreated = tc.usrCreated || '';
                    this.testInfo.usrModif = tc.usrModif || '';
                    this.testInfo.dateCreated = tc.dateCreated || '';
                    this.testInfo.dateModif = tc.dateModif || '';
                    this.testInfo.lastRunId = tc.lastExecutionId || 0;
                    this.testInfo.status = tc.status || '';
                    // Load status list from invariants
                    $.ajax({ url: 'FindInvariantByID', data: { idName: 'TCSTATUS' }, dataType: 'json',
                        success: (d) => { this.testInfo.statusList = (d || []).map(i => i.value); }
                    });
                    // Load app info
                    $.ajax({ url: 'ReadApplication', data: { application: tc.application }, success: (d) => { this.testInfo.appType = d.contentTable.type; } });
                    // Steps
                    const stepsRaw = tc.steps || [];
                    stepsRaw.sort((a, b) => a.sort - b.sort);
                    this.steps = stepsRaw.map(s => this._normalizeStep(s));
                    console.info('[V2] Normalized steps:', this.steps.length);
                    // Properties
                    this.properties = (tc.properties.testCaseProperties || []).map(p => this._normalizeProp(p));
                    this.inheritedProperties = (tc.properties.inheritedProperties || []).map(p => this._normalizeProp(p));
                    // Test case countries (for property country selection)
                    this.tcCountries = (tc.countries || []).map(c => typeof c === 'string' ? c : (c.country || c.value || ''));
                    // Select first step
                    if (this.steps.length > 0) {
                        const stepIdURL = GetURLParameter('stepId');
                        let idx = 0;
                        if (stepIdURL) { const found = this.steps.findIndex(s => s.stepId == stepIdURL); if (found >= 0) idx = found; }
                        this.selectStep(idx);
                    }
                    console.info('[V2] activeStep after select:', this.activeStep);
                    this.$nextTick(() => { if (window.lucide) lucide.createIcons(); });
                    // Update page title (legacy parity)
                    document.title = 'TestCase - ' + testcase;
                },
                error: (jqXHR, textStatus, errorThrown) => {
                    console.error('[V2] ReadTestCase error:', textStatus, errorThrown);
                    showUnexpectedError(jqXHR, textStatus, errorThrown);
                }
            });
        },

        // ── Switcher: load folder list ──
        _loadTestFolders() {
            $.ajax({
                url: 'ReadTest', dataType: 'json',
                success: (data) => {
                    var list = (data.contentTable || []).map(t => ({ test: t.test, description: t.description || '' }));
                    list.sort((a, b) => a.test.localeCompare(b.test, undefined, { sensitivity: 'base' }));
                    this.testFolders = list;
                    this.testFoldersLoaded = true;
                }
            });
        },
        _loadTestCasesForFolder(test) {
            $.ajax({
                url: 'ReadTestCase', data: { test: test }, dataType: 'json',
                success: (data) => {
                    var list = (data.contentTable || []).map(tc => ({ testcase: tc.testcase, description: tc.description || '', application: tc.application || '' }));
                    list.sort((a, b) => a.testcase.localeCompare(b.testcase, undefined, { sensitivity: 'base' }));
                    this.testCases = list;
                }
            });
        },
        switchToTest(test) {
            if (this.saveState === 'dirty' && !confirm('Unsaved changes will be lost. Switch test folder?')) return;
            // Load TC list for new folder then pick the first one
            $.ajax({
                url: 'ReadTestCase', data: { test: test }, dataType: 'json',
                success: (data) => {
                    var list = (data.contentTable || []).map(tc => ({ testcase: tc.testcase, description: tc.description || '', application: tc.application || '' }));
                    list.sort((a, b) => a.testcase.localeCompare(b.testcase, undefined, { sensitivity: 'base' }));
                    this.testCases = list;
                    if (list.length > 0) {
                        this.switchToTestCase(test, list[0].testcase, true);
                    }
                }
            });
        },
        switchToTestCase(test, testcase, skipDirtyCheck) {
            if (!skipDirtyCheck && this.saveState === 'dirty' && !confirm('Unsaved changes will be lost. Switch test case?')) return;
            this.saveState = '';
            this.undoStack = [];
            this.redoStack = [];
            this.activeStepIndex = -1;
            this._loadTestCase(test, testcase);
            // Update URL without reload
            var url = window.location.pathname + '?test=' + encodeURIComponent(test) + '&testcase=' + encodeURIComponent(testcase);
            history.pushState({ test: test, testcase: testcase }, '', url);
        },

        _normalizeStep(s) {
            s._uid = v2uid();
            s.toDelete = false;
            if (!s.actions) s.actions = [];
            s.actions.sort((a, b) => a.sort - b.sort);
            s.actions = s.actions.map(a => this._normalizeAction(a));
            return s;
        },
        _normalizeAction(a) {
            a._uid = v2uid();
            a.toDelete = false;
            if (!a.controls) a.controls = [];
            if (!a.options) a.options = [];
            if (!a.conditionOptions) a.conditionOptions = [];
            a.controls.sort((x, y) => x.sort - y.sort);
            a.controls = a.controls.map(c => this._normalizeControl(c));
            return a;
        },
        _normalizeControl(c) {
            c._uid = v2uid();
            c.toDelete = false;
            if (!c.options) c.options = [];
            if (!c.conditionOptions) c.conditionOptions = [];
            return c;
        },
        _normalizeProp(p) {
            p._uid = v2uid();
            p.toDelete = false;
            // Normalize countries: backend sends [{value:'FR'}] or strings
            if (p.countries && p.countries.length > 0) {
                p.countries = p.countries.map(c => typeof c === 'object' ? (c.value || c.country || '') : c).filter(c => c);
            } else {
                p.countries = [];
            }
            return p;
        },

        // ── Tabs ──
        setTab(name) {
            this.tab = name;
            InsertURLInHistory('./TestCaseScriptV2.jsp?' + ReplaceURLParameters('tabactive', name));
            this.$nextTick(() => { if (window.lucide) lucide.createIcons(); });
        },

        // ── Step Selection ──
        selectStep(idx) {
            this.activeStepIndex = idx;
            InsertURLInHistory('./TestCaseScriptV2.jsp?' + ReplaceURLParameters('stepId', this.steps[idx]?.stepId || ''));
            this.$nextTick(() => { if (window.lucide) lucide.createIcons(); });
        },

        // ── Mutations (with undo snapshot) ──
        _mutate(fn) {
            this._pushUndo();
            fn();
            this._markDirty();
        },
        _pushUndo() {
            this.undoStack.push(JSON.stringify({ steps: this.steps, properties: this.properties }));
            if (this.undoStack.length > 10) this.undoStack.shift();
            this.redoStack = [];
        },
        undo() {
            if (!this.undoStack.length) return;
            this.redoStack.push(JSON.stringify({ steps: this.steps, properties: this.properties }));
            const prev = JSON.parse(this.undoStack.pop());
            this.steps = prev.steps;
            this.properties = prev.properties;
            if (this.activeStepIndex >= this.steps.length) this.activeStepIndex = this.steps.length - 1;
            this._markDirty();
        },
        redo() {
            if (!this.redoStack.length) return;
            this.undoStack.push(JSON.stringify({ steps: this.steps, properties: this.properties }));
            const next = JSON.parse(this.redoStack.pop());
            this.steps = next.steps;
            this.properties = next.properties;
            this._markDirty();
        },
        _markDirty() {
            this.saveState = 'dirty';
            if (this.autoSaveEnabled) {
                clearTimeout(this._saveTimer);
                this._saveTimer = setTimeout(() => this.save(), 1000);
            }
        },

        // ── Step Operations ──
        updateStepDescription(val) { this._mutate(() => { this.activeStep.description = val; }); },
        addNewStep() {
            const self = this;
            self._pushUndo();
            const s = self._normalizeStep({
                stepId: -1, sort: self.steps.length + 1, description: '', isLibraryStep: false,
                isUsingLibraryStep: false, libraryStepTest: '', libraryStepTestCase: '', libraryStepStepId: -1,
                loop: 'onceIfConditionTrue', conditionOperator: 'always',
                conditionValue1: '', conditionValue2: '', conditionValue3: '',
                conditionOptions: [], isExecutionForced: false, actions: []
            });
            self.steps.push(s);
            self._markDirty();
            self.$nextTick(() => {
                self.activeStepIndex = self.steps.length - 1;
                if (window.lucide) lucide.createIcons();
            });
        },
        duplicateStep(idx) {
            this._mutate(() => {
                const clone = JSON.parse(JSON.stringify(this.steps[idx]));
                clone._uid = v2uid();
                clone.stepId = -1;
                clone.description = clone.description + ' (copy)';
                clone.actions.forEach(a => { a._uid = v2uid(); a.actionId = -1; a.controls.forEach(c => { c._uid = v2uid(); c.controlId = -1; }); });
                this.steps.splice(idx + 1, 0, clone);
                this.selectStep(idx + 1);
            });
        },
        toggleDeleteStep(idx) { this._mutate(() => { this.steps[idx].toDelete = !this.steps[idx].toDelete; }); },
        toggleStepLibrary(idx) { this._mutate(() => { this.steps[idx].isLibraryStep = !this.steps[idx].isLibraryStep; }); },
        unlinkLibraryStep(idx) {
            const self = this;
            const step = self.steps[idx];
            if (!step || !step.isUsingLibraryStep) return;
            const libRef = step.libraryStepTest + ' / ' + step.libraryStepTestCase;
            crbConfirmDelete({
                title: 'Unlink Library Step',
                html: 'Detach this step from <b>' + libRef + '</b>?<br><small>The actions will be copied locally and the link removed.</small>',
                confirmText: 'Unlink',
                confirmColor: '#7c3aed',
                icon: 'question'
            }).then(function(result) {
                if (!result.isConfirmed) return;
                // Fetch the library step actions before unlinking
                $.ajax({
                    url: 'ReadTestCaseStep',
                    data: { test: step.libraryStepTest, testcase: step.libraryStepTestCase, stepId: step.libraryStepStepId },
                    async: false,
                    success: function(data) {
                        self._pushUndo();
                        // Copy actions from library if step has none
                        if (data.step && data.step.actions && step.actions.length === 0) {
                            step.actions = data.step.actions.map(a => self._normalizeAction(a));
                        }
                        step.isUsingLibraryStep = false;
                        step.libraryStepStepId = -1;
                        step.libraryStepTest = '';
                        step.libraryStepTestCase = '';
                        self._markDirty();
                        // Auto-save to persist the unlink
                        self.save();
                    },
                    error: function() {
                        // Even if fetch fails, unlink locally
                        self._pushUndo();
                        step.isUsingLibraryStep = false;
                        step.libraryStepStepId = -1;
                        step.libraryStepTest = '';
                        step.libraryStepTestCase = '';
                        self._markDirty();
                        self.save();
                    }
                });
            });
        },
        openStepOptions(idx) {
            // Step options use the same modal as action/control options
            // The step object has conditionOperator, conditionValue1-3, loop, isExecutionForced, conditionOptions
            const step = this.steps[idx];
            const self = this;
            // We pass the step as an "action-like" object to the existing modal
            displayOverrideOptionsModal(step, $('<div><div class="boutonGroup"></div></div>'));
            // Listen for modal close to mark dirty
            const handler = () => { self._markDirty(); window.removeEventListener('modaloptions-close', handler); };
            window.addEventListener('modaloptions-close', handler);
        },
        searchLibraryStep() {
            const self = this;
            // Reset the modal state
            $('#addStepModal #description').val('');
            $('#importDetail').empty().hide();
            // Open the modal
            window.dispatchEvent(new CustomEvent('addstepmodal-open', { detail: {} }));
            // Load library steps
            if (typeof loadLibraryStep === 'function') {
                loadLibraryStep(undefined, self.testInfo ? self.testInfo.system : '');
            }
            // Bind the confirm button
            $('#addStepConfirm').off('click').on('click', function() {
                self._pushUndo();
                if ($("[name='importInfo']").length === 0) {
                    // Just a new step with description
                    const desc = $('#addStepModal #description').val();
                    if (desc) {
                        const s = self._normalizeStep({
                            stepId: -1, sort: self.steps.length + 1, description: desc,
                            isLibraryStep: false, isUsingLibraryStep: false,
                            libraryStepTest: '', libraryStepTestCase: '', libraryStepStepId: -1,
                            loop: 'onceIfConditionTrue', conditionOperator: 'always',
                            conditionValue1: '', conditionValue2: '', conditionValue3: '',
                            conditionOptions: [], isExecutionForced: false, actions: []
                        });
                        self.steps.push(s);
                        self._markDirty();
                        self.$nextTick(() => { self.activeStepIndex = self.steps.length - 1; });
                    }
                } else {
                    // Import selected library steps
                    $("[name='importInfo']").each(function(idx, importInfo) {
                        const stepInfo = $(importInfo).data('stepInfo');
                        if (!stepInfo) return;
                        const parentDiv = $(importInfo).closest('[id]');
                        const useStepChecked = parentDiv.find("[name='useStep']").prop('checked');
                        $.ajax({
                            url: 'ReadTestCaseStep',
                            data: { test: stepInfo.test, testcase: stepInfo.testCase, stepId: stepInfo.step },
                            async: false,
                            success: function(data) {
                                const s = self._normalizeStep({
                                    stepId: -1, sort: self.steps.length + 1,
                                    description: data.step.description || stepInfo.description,
                                    isLibraryStep: false,
                                    isUsingLibraryStep: !!useStepChecked,
                                    libraryStepTest: useStepChecked ? stepInfo.test : '',
                                    libraryStepTestCase: useStepChecked ? stepInfo.testCase : '',
                                    libraryStepStepId: useStepChecked ? stepInfo.step : -1,
                                    loop: data.step.loop || 'onceIfConditionTrue',
                                    conditionOperator: data.step.conditionOperator || 'always',
                                    conditionValue1: data.step.conditionValue1 || '',
                                    conditionValue2: data.step.conditionValue2 || '',
                                    conditionValue3: data.step.conditionValue3 || '',
                                    conditionOptions: data.step.conditionOptions || [],
                                    isExecutionForced: false,
                                    actions: useStepChecked ? [] : (data.step.actions || [])
                                });
                                self.steps.push(s);
                            }
                        });
                    });
                    self._markDirty();
                    self.$nextTick(() => {
                        self.activeStepIndex = self.steps.length - 1;
                        if (window.lucide) lucide.createIcons();
                    });
                }
            });
        },


        // ── Action Operations ──
        addAction() {
            this._mutate(() => {
                const a = this._normalizeAction({
                    actionId: -1, sort: (this.activeStep.actions.length + 1), description: '',
                    action: 'doNothing', value1: '', value2: '', value3: '',
                    isFatal: true, conditionOperator: 'always',
                    conditionValue1: '', conditionValue2: '', conditionValue3: '',
                    conditionOptions: [], options: [],
                    doScreenshotBefore: false, doScreenshotAfter: false,
                    waitBefore: 0, waitAfter: 0, screenshotFileName: '', controls: []
                });
                this.activeStep.actions.push(a);
            });
            this.$nextTick(() => { if (window.lucide) lucide.createIcons(); });
        },
        insertActionAfter(aIdx) {
            this._mutate(() => {
                const a = this._normalizeAction({
                    actionId: -1, sort: aIdx + 2, description: '',
                    action: 'doNothing', value1: '', value2: '', value3: '',
                    isFatal: true, conditionOperator: 'always',
                    conditionValue1: '', conditionValue2: '', conditionValue3: '',
                    conditionOptions: [], options: [],
                    doScreenshotBefore: false, doScreenshotAfter: false,
                    waitBefore: 0, waitAfter: 0, screenshotFileName: '', controls: []
                });
                this.activeStep.actions.splice(aIdx + 1, 0, a);
            });
            this.$nextTick(() => { if (window.lucide) lucide.createIcons(); });
        },
        updateActionField(aIdx, field, val) { this._mutate(() => { this.activeStep.actions[aIdx][field] = val; }); },
        toggleDeleteAction(aIdx) { this._mutate(() => { this.activeStep.actions[aIdx].toDelete = !this.activeStep.actions[aIdx].toDelete; }); },
        openActionOptions(aIdx) {
            const action = this.activeStep.actions[aIdx];
            const self = this;
            displayOverrideOptionsModal(action, $('<div><div class="boutonGroup"></div></div>'));
            // Mark dirty when modal closes (save was clicked)
            const handler = () => { self._markDirty(); window.removeEventListener('modaloptions-close', handler); };
            window.addEventListener('modaloptions-close', handler);
        },

        // ── Control Operations ──
        addControl(aIdx) {
            this._mutate(() => {
                const c = this._normalizeControl({
                    controlId: -1, sort: (this.activeStep.actions[aIdx].controls.length + 1),
                    description: '', control: 'verifyStringEqual',
                    value1: '', value2: '', value3: '',
                    isFatal: true, conditionOperator: 'always',
                    conditionValue1: '', conditionValue2: '', conditionValue3: '',
                    conditionOptions: [], options: [],
                    doScreenshotBefore: false, doScreenshotAfter: false,
                    waitBefore: 0, waitAfter: 0, screenshotFileName: ''
                });
                this.activeStep.actions[aIdx].controls.push(c);
            });
            this.$nextTick(() => { if (window.lucide) lucide.createIcons(); });
        },
        updateControlField(aIdx, cIdx, field, val) { this._mutate(() => { this.activeStep.actions[aIdx].controls[cIdx][field] = val; }); },
        toggleDeleteControl(aIdx, cIdx) { this._mutate(() => { this.activeStep.actions[aIdx].controls[cIdx].toDelete = !this.activeStep.actions[aIdx].controls[cIdx].toDelete; }); },
        openControlOptions(aIdx, cIdx) {
            const ctrl = this.activeStep.actions[aIdx].controls[cIdx];
            const self = this;
            displayOverrideOptionsModal(ctrl, $('<div><div class="boutonGroup"></div></div>'));
            const handler = () => { self._markDirty(); window.removeEventListener('modaloptions-close', handler); };
            window.addEventListener('modaloptions-close', handler);
        },

        // ── Property Operations ──
        propEditIdx: -1,   // index in filteredProperties for the edit modal (-1 = closed)
        tcCountries: [],    // test case countries available for properties

        /** Sanitize property name — blocks forbidden chars: . space ( ) % */
        _sanitizePropName(name) {
            return (name || '').replace(/[.\s()%]/g, '');
        },

        addProperty() {
            this._mutate(() => {
                const p = this._normalizeProp({
                    property: 'PROP-' + this.properties.length, description: '',
                    type: 'text', database: '', value1: '', value2: '', value3: 'value',
                    countries: this.tcCountries.slice(), nature: 'STATIC', length: '0', rowLimit: 0,
                    cacheExpire: 0, rank: 0, retryNb: 0, retryPeriod: 0
                });
                this.properties.push(p);
                // Open editor for the new property
                this.$nextTick(() => {
                    const idx = this.filteredProperties.findIndex(fp => fp._uid === p._uid);
                    if (idx >= 0) this.propEditIdx = idx;
                });
            });
        },
        duplicateProperty(pIdx) {
            this._mutate(() => {
                const clone = JSON.parse(JSON.stringify(this.filteredProperties[pIdx]));
                clone._uid = v2uid();
                clone.property = clone.property + '-copy';
                clone.toDelete = false;
                this.properties.push(clone);
            });
        },
        toggleDeleteProperty(pIdx) { this._mutate(() => { this.filteredProperties[pIdx].toDelete = !this.filteredProperties[pIdx].toDelete; }); },
        overrideInheritedProperty(pIdx) {
            const src = this.filteredProperties[pIdx];
            this._mutate(() => {
                const clone = JSON.parse(JSON.stringify(src));
                clone._uid = v2uid();
                clone._isInherited = false;
                clone.toDelete = false;
                this.properties.push(clone);
            });
        },
        copyProperty(pIdx) {
            const prop = this.filteredProperties[pIdx];
            const payload = JSON.stringify({ type: 'property', data: prop });
            if (navigator.clipboard) {
                navigator.clipboard.writeText(payload).then(() => {
                    this.clipboardType = 'property';
                    showToast && showToast('Property copied!', 'info');
                });
            }
        },
        pasteProperty() {
            if (navigator.clipboard) {
                var self = this;
                navigator.clipboard.readText().then(function(text) {
                    try {
                        var parsed = JSON.parse(text);
                        if (parsed && parsed.type === 'property' && parsed.data) {
                            self._mutate(function() {
                                var clone = JSON.parse(JSON.stringify(parsed.data));
                                clone._uid = v2uid();
                                clone.toDelete = false;
                                clone._isInherited = false;
                                self.properties.push(clone);
                            });
                        }
                    } catch(e) { console.warn('Paste property: invalid clipboard'); }
                });
            }
        },
        openPropertyEditor(pIdx) {
            var self = this;
            var prop = this.filteredProperties[pIdx];
            if (!prop) return;
            this.propEditIdx = pIdx; // track which property is being edited
            window.dispatchEvent(new CustomEvent('property-modal-open', {
                detail: {
                    property: prop,                         // live reference
                    countries: self.tcCountries,
                    canUpdate: self.canUpdate,
                    onField: function(key, val) {
                        self._mutate(function() { prop[key] = val; });
                    },
                    onCountryToggle: function(country) {
                        self.togglePropertyCountry(self.propEditIdx, country);
                    },
                    onSelectAll: function() {
                        self.selectAllCountries(self.propEditIdx);
                    },
                    onDeselectAll: function() {
                        self.deselectAllCountries(self.propEditIdx);
                    },
                    onClose: function() {
                        self.propEditIdx = -1;
                    },
                    openDataLib: function(val) {
                        self.openModalDataLib(val);
                    }
                }
            }));
        },
        closePropertyEditor() {
            this.propEditIdx = -1;
            window.dispatchEvent(new CustomEvent('property-modal-close'));
        },
        openModalDataLib(datalibName) {
            // Open legacy DataLib modal if function exists
            if (typeof openModalDataLib === 'function') {
                openModalDataLib(datalibName);
            } else if (typeof openDataLibModal === 'function') {
                openDataLibModal(datalibName);
            } else {
                // Fallback: open in new tab
                var url = 'TestDataLib.jsp?testdatalib=' + encodeURIComponent(datalibName || '');
                window.open(url, '_blank');
            }
        },
        /** Open AppService transversal modal in EDIT mode */
        openModalService(serviceName) {
            if (!serviceName) return;
            if (typeof openModalAppService === 'function') {
                openModalAppService(serviceName, 'EDIT');
            } else {
                // Fallback: open in new tab
                window.open('AppServiceList.jsp?service=' + encodeURIComponent(serviceName), '_blank');
            }
        },
        get editingProperty() {
            if (this.propEditIdx < 0 || this.propEditIdx >= this.filteredProperties.length) return null;
            return this.filteredProperties[this.propEditIdx];
        },
        updatePropertyField(pIdx, field, val) {
            this._mutate(() => { this.filteredProperties[pIdx][field] = val; });
        },
        togglePropertyCountry(pIdx, country) {
            this._mutate(() => {
                const prop = this.filteredProperties[pIdx];
                const arr = prop.countries || [];
                const idx = arr.indexOf(country);
                if (idx >= 0) { arr.splice(idx, 1); } else { arr.push(country); }
                prop.countries = arr;
            });
        },
        selectAllCountries(pIdx) {
            this._mutate(() => { this.filteredProperties[pIdx].countries = this.tcCountries.slice(); });
        },
        deselectAllCountries(pIdx) {
            this._mutate(() => { this.filteredProperties[pIdx].countries = []; });
        },
        filterProperties() { /* reactive via x-model */ },
        _isPropertyUnused(propName) {
            if (!propName) return false;
            const needle = '%property.' + propName + '%';
            for (const step of this.steps) {
                for (const a of (step.actions || [])) {
                    if ((a.value1 || '').includes(needle) || (a.value2 || '').includes(needle) || (a.value3 || '').includes(needle)) return false;
                    for (const c of (a.controls || [])) {
                        if ((c.value1 || '').includes(needle) || (c.value2 || '').includes(needle) || (c.value3 || '').includes(needle)) return false;
                    }
                }
            }
            return true;
        },
        /** Returns visible fields config for a given property type, from newPropertyPlaceholder */
        _getPropertyFields(type) {
            if (typeof newPropertyPlaceholder === 'undefined') return { value1: { label: { en: 'Value' } } };
            var fields = newPropertyPlaceholder[type];
            if (!fields) {
                // Type not in schema — provide a default value1 so the textarea always appears
                return { value1: { label: { en: 'Value' } } };
            }
            return fields;
        },
        /** Color accent for property type badge */
        _propTypeColor(type) {
            var map = {
                'text': 'var(--crb-grey-color)', 'getFromSql': 'var(--crb-blue-color)', 'getFromDataLib': 'var(--crb-purple-color)',
                'getFromHtml': 'var(--crb-orange-color)', 'getFromHtmlVisible': 'var(--crb-orange-color)',
                'getFromJson': 'var(--crb-green-color)', 'getRawFromJson': 'var(--crb-green-color)',
                'getFromXml': 'var(--crb-turquoise-color)', 'getDifferencesFromXml': 'var(--crb-turquoise-color)',
                'getFromJS': 'var(--crb-yellow-color)', 'getFromGroovy': 'var(--crb-yellow-color)',
                'getFromCommand': 'var(--crb-red-color)', 'getAttributeFromHtml': 'var(--crb-orange-color)',
                'getFromCookie': 'var(--crb-orange-color)', 'getFromElasticSearch': 'var(--crb-blue-color)',
                'getFromNetworkTraffic': 'var(--crb-blue-color)', 'getFromConsoleLog': 'var(--crb-yellow-color)'
            };
            return map[type] || 'var(--crb-grey-color)';
        },

        // ── Copy/Paste (YAML via System Clipboard — cross test case) ──

        // Lightweight JSON → YAML serializer
        _toYaml(obj, indent) {
            indent = indent || 0;
            var pad = '  '.repeat(indent);
            if (obj === null || obj === undefined) return 'null';
            if (typeof obj === 'boolean') return obj ? 'true' : 'false';
            if (typeof obj === 'number') return String(obj);
            if (typeof obj === 'string') {
                if (obj === '' || /[\n:#{}\[\],&*?|>!%@`]/.test(obj) || obj.trim() !== obj)
                    return JSON.stringify(obj);
                return obj;
            }
            if (Array.isArray(obj)) {
                if (obj.length === 0) return '[]';
                var self = this;
                return obj.map(function(item) {
                    if (typeof item === 'object' && item !== null) {
                        var inner = self._toYaml(item, indent + 1);
                        return pad + '- ' + inner.trimStart();
                    }
                    return pad + '- ' + self._toYaml(item, 0);
                }).join('\n');
            }
            if (typeof obj === 'object') {
                var keys = Object.keys(obj);
                if (keys.length === 0) return '{}';
                var self2 = this;
                return keys.map(function(k) {
                    var v = obj[k];
                    if (typeof v === 'object' && v !== null && !Array.isArray(v)) {
                        return pad + k + ':\n' + self2._toYaml(v, indent + 1);
                    }
                    if (Array.isArray(v) && v.length > 0) {
                        return pad + k + ':\n' + self2._toYaml(v, indent + 1);
                    }
                    return pad + k + ': ' + self2._toYaml(v, 0);
                }).join('\n');
            }
            return String(obj);
        },

        // Clean internal fields before export
        _cleanForExport(obj) {
            var clone = JSON.parse(JSON.stringify(obj));
            function clean(o) {
                if (!o || typeof o !== 'object') return;
                delete o._uid;
                delete o.toDelete;
                if (Array.isArray(o)) { o.forEach(clean); return; }
                Object.values(o).forEach(function(v) {
                    if (typeof v === 'object') clean(v);
                });
            }
            clean(clone);
            return clone;
        },

        // Parse clipboard text back to object
        _parseClipboardText(text) {
            if (!text) return null;
            text = text.trim();
            var type = null;
            if (text.indexOf('# cerberus:step') === 0) { type = 'step'; }
            else if (text.indexOf('# cerberus:action') === 0) { type = 'action'; }
            else return null;
            // Extract embedded JSON from last line
            var lines = text.split('\n');
            var lastLine = lines[lines.length - 1];
            if (lastLine.indexOf('# __json__: ') === 0) {
                try {
                    return { type: type, data: JSON.parse(lastLine.substring('# __json__: '.length)) };
                } catch(e) { return null; }
            }
            return null;
        },

        // Check system clipboard content and update clipboardType
        async _checkClipboard() {
            try {
                var text = await navigator.clipboard.readText();
                var parsed = this._parseClipboardText(text);
                this.clipboardType = parsed ? parsed.type : null;
            } catch(e) {
                // Permission denied or empty — keep current state
            }
        },

        copyStep(idx) {
            var self = this;
            var step = this._cleanForExport(this.steps[idx]);
            var yamlContent = this._toYaml(step);
            var header = '# cerberus:step\n# Copied from: ' + (this.testInfo.test || '') + ' / ' + (this.testInfo.testCase || '') + '\n---\n';
            var jsonBackup = '\n# __json__: ' + JSON.stringify(step);
            var fullText = header + yamlContent + jsonBackup;

            navigator.clipboard.writeText(fullText).then(function() {
                self.clipboardType = 'step';
                showMessageMainPage('success', 'Step copied to clipboard');
            }).catch(function() {
                showMessageMainPage('warning', 'Could not access clipboard');
            });
        },

        copyAction(aIdx) {
            var self = this;
            var action = this._cleanForExport(this.activeStep.actions[aIdx]);
            var yamlContent = this._toYaml(action);
            var header = '# cerberus:action\n# Copied from: ' + (this.testInfo.test || '') + ' / ' + (this.testInfo.testCase || '') + '\n---\n';
            var jsonBackup = '\n# __json__: ' + JSON.stringify(action);
            var fullText = header + yamlContent + jsonBackup;

            navigator.clipboard.writeText(fullText).then(function() {
                self.clipboardType = 'action';
                showMessageMainPage('success', 'Action copied to clipboard');
            }).catch(function() {
                showMessageMainPage('warning', 'Could not access clipboard');
            });
        },

        async pasteAfterStep(idx) {
            var data = null;
            try {
                var text = await navigator.clipboard.readText();
                var parsed = this._parseClipboardText(text);
                if (parsed && parsed.type === 'step') {
                    data = parsed.data;
                } else if (parsed && parsed.type === 'action') {
                    showMessageMainPage('warning', 'Clipboard contains an Action, not a Step. Use the Paste button in the action toolbar.');
                    return;
                }
            } catch(e) {}

            if (!data) {
                showMessageMainPage('warning', 'No step in clipboard. Copy a step first (via the \u22ee menu).');
                return;
            }
            this._mutate(() => {
                var clone = this._normalizeStep(data);
                clone.stepId = -1;
                clone.description = (clone.description || 'Pasted step') + ' (pasted)';
                (clone.actions || []).forEach(a => {
                    a._uid = v2uid();
                    a.actionId = -1;
                    (a.controls || []).forEach(c => { c._uid = v2uid(); c.controlId = -1; });
                });
                this.steps.splice(idx + 1, 0, clone);
                this.selectStep(idx + 1);
            });
            showMessageMainPage('success', 'Step pasted successfully');
        },

        async pasteAction() {
            if (!this.activeStep) return;
            var data = null;
            try {
                var text = await navigator.clipboard.readText();
                var parsed = this._parseClipboardText(text);
                if (parsed && parsed.type === 'action') {
                    data = parsed.data;
                } else if (parsed && parsed.type === 'step') {
                    showMessageMainPage('warning', 'Clipboard contains a Step, not an Action. Use Paste After in the step menu.');
                    return;
                }
            } catch(e) {}

            if (!data) {
                showMessageMainPage('warning', 'No action in clipboard. Copy an action first (via the copy button).');
                return;
            }
            this._mutate(() => {
                var clone = this._normalizeAction(data);
                clone.actionId = -1;
                (clone.controls || []).forEach(c => { c._uid = v2uid(); c.controlId = -1; });
                this.activeStep.actions.push(clone);
            });
            showMessageMainPage('success', 'Action pasted successfully');
        },
        openActionContextMenu(event, aIdx) {
            // For now, action context menu items are directly in the action row buttons
            // This is a placeholder for future right-click menu enhancement
        },

        // ── Drag & Drop Steps ──
        onStepDragStart(e, idx) { this._dragIdx = idx; e.dataTransfer.effectAllowed = 'move'; },
        onStepDragOver(e, idx) { e.preventDefault(); },
        onStepDrop(e, idx) {
            e.preventDefault();
            if (this._dragIdx === idx) return;
            this._mutate(() => {
                const item = this.steps.splice(this._dragIdx, 1)[0];
                this.steps.splice(idx, 0, item);
                this.activeStepIndex = idx;
            });
        },
        onStepDragEnd() { this._dragIdx = -1; },

        // ── Drag & Drop — Modern 2026 ──
        _dnd: { type: null, fromIdx: -1, fromAIdx: -1, fromCIdx: -1, indicatorIdx: -1, indicatorPos: '' },

        // — Actions —
        onActionDragStart(e, idx) {
            this._dnd = { type: 'action', fromIdx: idx, fromAIdx: -1, fromCIdx: -1, indicatorIdx: -1, indicatorPos: '' };
            e.dataTransfer.effectAllowed = 'move';
            // Minimal ghost
            const ghost = document.createElement('div');
            ghost.className = 'v2-drag-ghost';
            ghost.textContent = (idx + 1) + '. ' + (this.getActionLabel(this.activeStep.actions[idx].action) || 'Action');
            document.body.appendChild(ghost);
            e.dataTransfer.setDragImage(ghost, 0, 0);
            setTimeout(() => ghost.remove(), 0);
            // Mark dragged
            const card = e.target.closest('.v2-action-card');
            if (card) card.classList.add('v2-dnd-dragging');
        },
        onActionDragOver(e, idx) {
            e.preventDefault();
            if (this._dnd.type !== 'action') return;
            if (this._dnd.fromIdx === idx) { this._dndClearIndicator(); return; }
            const card = document.getElementById('v2-action-' + idx);
            if (!card) return;
            const rect = card.getBoundingClientRect();
            const midY = rect.top + rect.height / 2;
            const pos = e.clientY < midY ? 'before' : 'after';
            if (this._dnd.indicatorIdx === idx && this._dnd.indicatorPos === pos) return;
            this._dndClearIndicator();
            this._dnd.indicatorIdx = idx;
            this._dnd.indicatorPos = pos;
            card.classList.add(pos === 'before' ? 'v2-dnd-indicator-top' : 'v2-dnd-indicator-bottom');
        },
        onActionDrop(e, idx) {
            e.preventDefault();
            if (this._dnd.type !== 'action' || this._dnd.fromIdx < 0) return;
            const from = this._dnd.fromIdx;
            let to = this._dnd.indicatorPos === 'after' ? idx + 1 : idx;
            if (from < to) to--;
            if (from !== to && to >= 0) {
                this._mutate(() => {
                    const item = this.activeStep.actions.splice(from, 1)[0];
                    this.activeStep.actions.splice(to, 0, item);
                });
            }
            this._dndCleanup();
        },
        onActionDragEnd() {
            this._dndCleanup();
        },

        // — Controls —
        onControlDragStart(e, aIdx, cIdx) {
            this._dnd = { type: 'control', fromIdx: -1, fromAIdx: aIdx, fromCIdx: cIdx, indicatorIdx: -1, indicatorPos: '' };
            e.dataTransfer.effectAllowed = 'move';
            const ghost = document.createElement('div');
            ghost.className = 'v2-drag-ghost v2-drag-ghost--green';
            ghost.textContent = (aIdx+1) + '.' + (cIdx+1) + ' ' + (this.getControlLabel(this.activeStep.actions[aIdx].controls[cIdx].control) || 'Verification');
            document.body.appendChild(ghost);
            e.dataTransfer.setDragImage(ghost, 0, 0);
            setTimeout(() => ghost.remove(), 0);
            const card = e.target.closest('.v2-control-card');
            if (card) card.classList.add('v2-dnd-dragging');
        },
        onControlDragOver(e, aIdx, cIdx) {
            e.preventDefault();
            if (this._dnd.type !== 'control') return;
            if (this._dnd.fromAIdx === aIdx && this._dnd.fromCIdx === cIdx) { this._dndClearIndicator(); return; }
            const cards = document.querySelectorAll('.v2-control-card');
            let targetCard = null;
            cards.forEach(c => {
                const id = c.getAttribute('data-ctrl-id');
                if (id === aIdx + '-' + cIdx) targetCard = c;
            });
            if (!targetCard) return;
            const rect = targetCard.getBoundingClientRect();
            const midY = rect.top + rect.height / 2;
            const pos = e.clientY < midY ? 'before' : 'after';
            if (this._dnd.indicatorIdx === cIdx && this._dnd.indicatorPos === pos && this._dnd.fromAIdx === aIdx) return;
            this._dndClearIndicator();
            this._dnd.indicatorIdx = cIdx;
            this._dnd.indicatorPos = pos;
            targetCard.classList.add(pos === 'before' ? 'v2-dnd-indicator-top' : 'v2-dnd-indicator-bottom');
        },
        onControlDrop(e, targetAIdx, targetCIdx) {
            e.preventDefault();
            if (this._dnd.type !== 'control') return;
            const { fromAIdx, fromCIdx, indicatorPos } = this._dnd;
            let to = indicatorPos === 'after' ? targetCIdx + 1 : targetCIdx;
            if (fromAIdx === targetAIdx && fromCIdx < to) to--;
            if (fromAIdx !== targetAIdx || fromCIdx !== to) {
                this._mutate(() => {
                    const ctrl = this.activeStep.actions[fromAIdx].controls.splice(fromCIdx, 1)[0];
                    this.activeStep.actions[targetAIdx].controls.splice(to, 0, ctrl);
                });
            }
            this._dndCleanup();
        },
        onControlDragEnd() {
            this._dndCleanup();
        },

        // — Helpers —
        _dndClearIndicator() {
            document.querySelectorAll('.v2-dnd-indicator-top, .v2-dnd-indicator-bottom').forEach(el => {
                el.classList.remove('v2-dnd-indicator-top', 'v2-dnd-indicator-bottom');
            });
        },
        _dndCleanup() {
            this._dndClearIndicator();
            document.querySelectorAll('.v2-dnd-dragging').forEach(el => el.classList.remove('v2-dnd-dragging'));
            this._dnd = { type: null, fromIdx: -1, fromAIdx: -1, fromCIdx: -1, indicatorIdx: -1, indicatorPos: '' };
        },



        // ── Save ──
        save() {
            if (this.saveState === 'saving') return;
            // Pre-save validation (legacy parity)
            var activeProps = this.properties.filter(p => !p.toDelete);
            var emptyName = activeProps.some(p => !p.property || p.property.trim() === '');
            var emptyCountry = activeProps.some(p => !p.countries || p.countries.length === 0);
            var doSave = () => {
                this.saveState = 'saving';
                const stepArr = this._buildStepPayload();
                const propArr = this._buildPropPayload();
            const payload = {
                informationInitialTest: this.testInfo.test,
                informationInitialTestCase: this.testInfo.testcase,
                informationTest: this.testInfo.test,
                informationTestCase: this.testInfo.testcase,
                steps: stepArr,
                properties: propArr
            };
            // Draft to localStorage
            try { localStorage.setItem('cerberus_draft_' + this.testInfo.test + '_' + this.testInfo.testcase, JSON.stringify(payload)); } catch(e) {}
            $.ajax({
                url: 'UpdateTestCaseWithDependencies',
                method: 'POST',
                contentType: 'application/json; charset=utf-8',
                dataType: 'json',
                data: JSON.stringify(payload),
                success: () => {
                    this.saveState = 'saved';
                    try { localStorage.removeItem('cerberus_draft_' + this.testInfo.test + '_' + this.testInfo.testcase); } catch(e) {}
                    setTimeout(() => { if (this.saveState === 'saved') this.saveState = ''; }, 2000);
                },
                error: (jqXHR, textStatus, errorThrown) => {
                    this.saveState = 'dirty';
                    showUnexpectedError(jqXHR, textStatus, errorThrown);
                }
            });
            };  // end doSave

            // Validation warnings (same as legacy)
            if (emptyName) {
                if (!confirm('Warning: One or more properties have no name. Continue saving?')) return;
            }
            if (emptyCountry) {
                if (!confirm('Warning: One or more properties have no country selected. Continue saving?')) return;
            }
            doSave();
        },

        _buildStepPayload() {
            const arr = [];
            this.steps.forEach((step, i) => {
                const sJson = {
                    toDelete: step.toDelete, test: step.test || this.testInfo.test, testcase: step.testcase || this.testInfo.testcase,
                    stepId: step.stepId, sort: i + 1, description: step.description,
                    isUsingLibraryStep: step.isUsingLibraryStep, libraryStepTest: step.libraryStepTest || '',
                    libraryStepTestCase: step.libraryStepTestCase || '', libraryStepStepId: step.libraryStepStepId || -1,
                    isLibraryStep: step.isLibraryStep, loop: step.loop || 'onceIfConditionTrue',
                    conditionOperator: step.conditionOperator || 'always',
                    conditionValue1: step.conditionValue1 || '', conditionValue2: step.conditionValue2 || '', conditionValue3: step.conditionValue3 || '',
                    conditionOptions: step.conditionOptions || [], isExecutionForced: step.isExecutionForced || false,
                    actions: []
                };
                (step.actions || []).forEach((action, j) => {
                    const aJson = {
                        toDelete: action.toDelete, test: this.testInfo.test, testcase: this.testInfo.testcase,
                        stepId: step.stepId, actionId: action.actionId, sort: j + 1,
                        description: action.description, action: action.action,
                        object: action.value1 || '', property: action.value2 || '', value3: action.value3 || '',
                        options: action.options || [], isFatal: action.isFatal,
                        conditionOperator: action.conditionOperator || 'always',
                        conditionValue1: action.conditionValue1 || '', conditionValue2: action.conditionValue2 || '', conditionValue3: action.conditionValue3 || '',
                        conditionOptions: action.conditionOptions || [],
                        screenshotFileName: action.screenshotFileName || '',
                        doScreenshotBefore: action.doScreenshotBefore || false, doScreenshotAfter: action.doScreenshotAfter || false,
                        waitBefore: action.waitBefore || 0, waitAfter: action.waitAfter || 0,
                        controls: []
                    };
                    (action.controls || []).forEach((ctrl, k) => {
                        aJson.controls.push({
                            toDelete: ctrl.toDelete, test: this.testInfo.test, testcase: this.testInfo.testcase,
                            stepId: step.stepId, actionId: action.actionId, controlId: ctrl.controlId, sort: k + 1,
                            description: ctrl.description, control: ctrl.control,
                            value1: ctrl.value1 || '', value2: ctrl.value2 || '', value3: ctrl.value3 || '',
                            options: ctrl.options || [], isFatal: ctrl.isFatal,
                            conditionOperator: ctrl.conditionOperator || 'always',
                            conditionValue1: ctrl.conditionValue1 || '', conditionValue2: ctrl.conditionValue2 || '', conditionValue3: ctrl.conditionValue3 || '',
                            conditionOptions: ctrl.conditionOptions || [],
                            screenshotFileName: ctrl.screenshotFileName || '',
                            doScreenshotBefore: ctrl.doScreenshotBefore || false, doScreenshotAfter: ctrl.doScreenshotAfter || false,
                            waitBefore: ctrl.waitBefore || 0, waitAfter: ctrl.waitAfter || 0
                        });
                    });
                    sJson.actions.push(aJson);
                });
                arr.push(sJson);
            });
            return arr;
        },

        _buildPropPayload() {
            return this.properties.map(p => ({
                toDelete: p.toDelete, property: p.property, description: p.description || '',
                type: p.type || 'text', database: p.database || '',
                value1: p.value1 || '', value2: p.value2 || '', value3: p.value3 || 'value',
                countries: (p.countries || []).map(c => typeof c === 'object' ? c : { value: c }),
                nature: p.nature || 'STATIC',
                length: String(p.length || '0'), rowLimit: p.rowLimit || 0,
                cacheExpire: p.cacheExpire || 0, rank: p.rank || 0,
                retryNb: p.retryNb || 0, retryPeriod: p.retryPeriod || 0
            }));
        },

        // ── Header Actions ──
        runTestCase() {
            window.dispatchEvent(new CustomEvent('open-execution', {
                detail: {
                    application: this.testInfo.application,
                    test: this.testInfo.test,
                    testcase: this.testInfo.testCase,
                    description: this.testInfo.description
                }
            }));
        },
        reRunTestCase() {
            // Quick re-run: submit directly via AddToExecutionQueuePrivate
            if (!this.lastRunCountry || !this.lastRunEnvironment) {
                // No previous run, fallback to normal modal
                this.runTestCase();
                return;
            }
            var params = 'e=1'
                + '&test=' + encodeURIComponent(this.testInfo.test)
                + '&testcase=' + encodeURIComponent(this.testInfo.testcase)
                + '&country=' + encodeURIComponent(this.lastRunCountry)
                + '&environment=' + encodeURIComponent(this.lastRunEnvironment);
            if (this.lastRunRobot) params += '&robot=' + encodeURIComponent(this.lastRunRobot);
            params += '&outputformat=json';
            $.post('AddToExecutionQueuePrivate', params, (data) => {
                if (data.nbExe === 1 && data.queueList && data.queueList[0]) {
                    window.location.href = 'TestCaseExecution.jsp?executionQueueId=' + data.queueList[0].queueId;
                } else {
                    handleAddToQueueResponse(data, true);
                }
            }, 'json').fail(handleErrorAjaxAfterTimeout);
        },
        editHeader() {
            openModalTestCase(this.testInfo.test, this.testInfo.testCase, 'EDIT');
        },
        saveAs() {
            openModalTestCase(this.testInfo.test, this.testInfo.testCase, 'DUPLICATE');
        },
        updateStatus(newStatus) {
            var self = this;
            $.post('UpdateTestCase', {
                test: this.testInfo.test,
                testcase: this.testInfo.testCase,
                originalTest: this.testInfo.test,
                originalTestcase: this.testInfo.testCase,
                status: newStatus
            }, function(data) {
                if (getAlertType(data.messageType) === 'success') {
                    self.testInfo.status = newStatus;
                    showMessageMainPage('success', 'Status updated to ' + newStatus);
                } else {
                    showMessageMainPage('danger', data.message || 'Failed to update status');
                }
            }, 'json').fail(handleErrorAjaxAfterTimeout);
        },
        _statusColor(s) {
            if (!s) return '#94a3b8';
            var map = { 'WORKING': '#10b981', 'STANDBY': '#f59e0b', 'IN PROGRESS': '#3b82f6', 'DEPRECATED': '#ef4444', 'CANCELLED': '#6b7280' };
            return map[s.toUpperCase()] || '#94a3b8';
        },
        seeLastExec() { window.location.href = './TestCaseExecutionList.jsp?Test=' + encodeURI(this.testInfo.test) + '&TestCase=' + encodeURI(this.testInfo.testcase); },
        seeLogs() { window.location.href = './LogEvent.jsp?Test=' + encodeURI(this.testInfo.test) + '&TestCase=' + encodeURI(this.testInfo.testcase); },
        deleteTestCase() {
            var doc = new Doc();
            var msg = doc.getDocLabel('page_testcase', 'message_delete').replace('%ENTRY%', this.testInfo.test + ' / ' + this.testInfo.testcase);
            showModalConfirmation(() => {
                $.post('DeleteTestCase', { test: this.testInfo.test, testCase: this.testInfo.testcase }, (data) => {
                    if (getAlertType(data.messageType) === 'success') window.location = './TestCaseList.jsp';
                    $('#confirmationModal').modal('hide');
                }, 'json');
            }, undefined, 'Delete', msg, '', '', '', '');
        },

        // ── Action/Control placeholder & picto helpers ──
        getActionPlaceholder(actionType, fieldKey) {
            if (typeof actionOptList === 'undefined') return '';
            const ph = actionOptList[actionType] || actionOptList['unknown'];
            if (!ph || !ph[fieldKey]) return '';
            const user = getUser();
            return ph[fieldKey].label ? (ph[fieldKey].label[user.language] || '') : '';
        },
        getActionPicto(actionType) {
            if (typeof actionOptGroupList === 'undefined' || typeof actionOptList === 'undefined') return '';
            const item = actionOptList[actionType];
            if (!item || !item.group) return 'images/action-website.png';
            // Find group picto
            const group = actionOptGroupList.find(g => g.name === item.group);
            if (group && group.picto) {
                // Extract src from picto HTML: <img ... src='xxx'/>
                const match = group.picto.match(/src=['"](.*?)['"]/);
                return match ? match[1] : 'images/action-website.png';
            }
            return 'images/action-website.png';
        },
        getActionFieldPicto(actionType, fieldKey) {
            if (typeof actionOptList === 'undefined') return 'images/action-font.png';
            const ph = actionOptList[actionType] || actionOptList['unknown'];
            if (!ph || !ph[fieldKey] || !ph[fieldKey].picto) return 'images/action-font.png';
            return ph[fieldKey].picto;
        },
        getControlPlaceholder(controlType, fieldKey) {
            if (typeof convertToGui === 'undefined') return '';
            const ph = convertToGui[controlType] || convertToGui['unknown'];
            if (!ph || !ph[fieldKey]) return '';
            const user = getUser();
            return ph[fieldKey].label ? (ph[fieldKey].label[user.language] || '') : '';
        },
        getControlFieldPicto(controlType, fieldKey) {
            if (typeof convertToGui === 'undefined') return 'images/action-font.png';
            const ph = convertToGui[controlType] || convertToGui['unknown'];
            if (!ph || !ph[fieldKey] || !ph[fieldKey].picto) return 'images/action-font.png';
            return ph[fieldKey].picto;
        },

        // ── Optional field detection ──
        isFieldOptional(actionType, fieldKey) {
            const label = this.getActionPlaceholder(actionType, fieldKey);
            return label.toLowerCase().startsWith('[opt]');
        },
        isControlFieldOptional(controlType, fieldKey) {
            const label = this.getControlPlaceholder(controlType, fieldKey);
            return label.toLowerCase().startsWith('[opt]');
        },
        hasOptionalActionFields(actionType) {
            return this.isFieldOptional(actionType, 'field1') ||
                   this.isFieldOptional(actionType, 'field2') ||
                   this.isFieldOptional(actionType, 'field3');
        },
        hasOptionalControlFields(controlType) {
            return this.isControlFieldOptional(controlType, 'field1') ||
                   this.isControlFieldOptional(controlType, 'field2') ||
                   this.isControlFieldOptional(controlType, 'field3');
        },

        // ── Autocomplete System ──
        _acResults: [],
        _acVisible: false,
        _acActiveField: null,
        _acSelectedIdx: -1,
        _acActiveAIdx: -1,
        _acActiveCIdx: -1,

        _getFieldAutoType(typeKey, fieldKey, isControl) {
            var def;
            if (isControl) {
                def = (typeof convertToGui !== 'undefined') ? convertToGui[typeKey] : null;
            } else {
                def = (typeof actionOptList !== 'undefined') ? actionOptList[typeKey] : null;
            }
            if (!def || !def[fieldKey] || !def[fieldKey]['class']) return 'variable';
            var cls = def[fieldKey]['class'];
            if (cls.indexOf('crb-autocomplete-element') >= 0) return 'element';
            if (cls.indexOf('crb-autocomplete-service') >= 0) return 'service';
            if (cls.indexOf('crb-autocomplete-property') >= 0) return 'property';
            if (cls.indexOf('crb-autocomplete-switch') >= 0) return 'switch';
            if (cls.indexOf('crb-autocomplete-select') >= 0) return 'select';
            if (cls.indexOf('crb-autocomplete-boolean') >= 0) return 'boolean';
            if (cls.indexOf('crb-autocomplete-fileuploadflag') >= 0) return 'fileuploadflag';
            if (cls.indexOf('crb-autocomplete-filesortflag') >= 0) return 'filesortflag';
            return 'variable';
        },

        _staticSuggestions: {
            element: [
                { value: 'xpath=', label: 'xpath=', desc: 'XPath selector' },
                { value: 'id=', label: 'id=', desc: 'Element ID' },
                { value: 'name=', label: 'name=', desc: 'Element name attribute' },
                { value: 'class=', label: 'class=', desc: 'CSS class name' },
                { value: 'css=', label: 'css=', desc: 'CSS selector' },
                { value: 'data-cerberus=', label: 'data-cerberus=', desc: 'Cerberus data attribute' },
                { value: 'picture=', label: 'picture=', desc: 'Image recognition' },
                { value: 'coord=', label: 'coord=', desc: 'Screen coordinates' },
            ],
            'switch': [
                { value: 'title=', label: 'title=', desc: 'Window title' },
                { value: 'url=', label: 'url=', desc: 'Window URL' },
                { value: 'regexTitle=', label: 'regexTitle=', desc: 'Regex on title' },
                { value: 'regexUrl=', label: 'regexUrl=', desc: 'Regex on URL' },
            ],
            select: [
                { value: 'value=', label: 'value=', desc: 'By value attribute' },
                { value: 'label=', label: 'label=', desc: 'By visible text' },
                { value: 'index=', label: 'index=', desc: 'By index position' },
            ],
            boolean: [
                { value: 'true', label: 'true', desc: 'Boolean true' },
                { value: 'false', label: 'false', desc: 'Boolean false' },
            ],
            fileuploadflag: [
                { value: 'EMPTYFOLDER', label: 'EMPTYFOLDER', desc: 'Empty folder before upload' },
            ],
            filesortflag: [
                { value: 'LASTMODIFIED', label: 'LASTMODIFIED', desc: 'Sort by last modified' },
                { value: 'DESC', label: 'DESC', desc: 'Descending order' },
                { value: 'ASC', label: 'ASC', desc: 'Ascending order' },
                { value: 'IGNORECASEDESC', label: 'IGNORECASEDESC', desc: 'Case-insensitive descending' },
                { value: 'IGNORECASEASC', label: 'IGNORECASEASC', desc: 'Case-insensitive ascending' },
            ],
        },

        _getPropertySuggestions() {
            var seen = {};
            var props = [];
            // Local properties (highest priority)
            var self = this;
            if (this.properties) {
                this.properties.forEach(function(p) {
                    if (p.property && !seen[p.property]) {
                        seen[p.property] = true;
                        props.push({ value: p.property, label: p.property, desc: p.type || 'Local' });
                    }
                });
            }
            // Inherited properties
            if (this.inheritedProperties) {
                this.inheritedProperties.forEach(function(p) {
                    if (p.property && !seen[p.property]) {
                        seen[p.property] = true;
                        props.push({ value: p.property, label: p.property, desc: (p.type || 'Inherited') + ' (inherited)' });
                    }
                });
            }
            return props;
        },

        /** Open property modal by name — used by the pencil button on action fields */
        _openPropertyByName(name) {
            if (!name) return;
            var fp = this.filteredProperties;
            for (var i = 0; i < fp.length; i++) {
                if (fp[i].property === name) {
                    this.openPropertyEditor(i);
                    return;
                }
            }
        },

        /** Pencil button handler — auto-detects service vs property and opens the right modal */
        _openFieldEditModal(actionKey, fieldKey, isControl, value) {
            var autoType = this._getFieldAutoType(actionKey, fieldKey, isControl);
            if (autoType === 'service') {
                this.openModalService(value);
            } else if (autoType === 'property') {
                this._openPropertyByName(value);
            }
        },

        _acFetchTimeout: null,
        _acServiceCache: {},

        acOnFocus(typeKey, fieldKey, isControl, currentVal, aIdx, cIdx) {
            var autoType = this._getFieldAutoType(typeKey, fieldKey, isControl);
            this._acActiveField = { typeKey: typeKey, fieldKey: fieldKey, isControl: isControl, autoType: autoType };
            this._acActiveAIdx = (aIdx !== undefined) ? aIdx : -1;
            this._acActiveCIdx = (cIdx !== undefined) ? cIdx : -1;
            this._acSelectedIdx = -1;
            if (autoType === 'service') {
                this._fetchServiceSuggestions(currentVal || '');
            } else if (autoType === 'property') {
                this._acResults = this._getPropertySuggestions();
                this._filterAcResults(currentVal || '');
                this._acVisible = this._acResults.length > 0;
            } else if (this._staticSuggestions[autoType]) {
                this._acResults = this._staticSuggestions[autoType].slice();
                this._filterAcResults(currentVal || '');
                this._acVisible = this._acResults.length > 0;
            } else {
                this._acVisible = false;
            }
        },

        acOnInput(currentVal) {
            if (!this._acActiveField) return;
            var autoType = this._acActiveField.autoType;
            this._acSelectedIdx = -1;
            if (autoType === 'service') {
                clearTimeout(this._acFetchTimeout);
                var self = this;
                this._acFetchTimeout = setTimeout(function() { self._fetchServiceSuggestions(currentVal); }, 250);
            } else if (autoType === 'property') {
                this._acResults = this._getPropertySuggestions();
                this._filterAcResults(currentVal);
                this._acVisible = this._acResults.length > 0;
            } else if (this._staticSuggestions[autoType]) {
                this._acResults = this._staticSuggestions[autoType].slice();
                this._filterAcResults(currentVal);
                this._acVisible = this._acResults.length > 0;
            }
        },

        _filterAcResults(q) {
            if (!q) return;
            var lower = q.toLowerCase();
            this._acResults = this._acResults.filter(function(r) {
                return r.label.toLowerCase().indexOf(lower) >= 0 || r.value.toLowerCase().indexOf(lower) >= 0;
            });
            // Hide if exact match — the user already picked/typed the value
            if (this._acResults.length > 0 && this._acResults.some(function(r) {
                return r.value.toLowerCase() === lower || r.label.toLowerCase() === lower;
            })) {
                this._acResults = [];
            }
        },

        _fetchServiceSuggestions(query) {
            var self = this;
            var url = 'ReadAppService?service=' + encodeURIComponent(query) + '&limit=15';
            $.getJSON(url, function(data) {
                if (data && data.contentTable) {
                    self._acResults = data.contentTable.map(function(s) {
                        return { value: s.service, label: s.service, desc: s.type || 'Service' };
                    });
                } else {
                    self._acResults = [];
                }
                // Hide if exact match
                var lower = (query || '').toLowerCase();
                if (lower && self._acResults.some(function(r) { return r.value.toLowerCase() === lower; })) {
                    self._acResults = [];
                }
                self._acVisible = self._acResults.length > 0;
            });
        },

        acSelect(item) {
            this._acVisible = false;
            return item.value;
        },

        acOnBlur() {
            var self = this;
            setTimeout(function() { self._acVisible = false; }, 200);
        },

        acOnKeydown(e) {
            if (!this._acVisible || !this._acResults.length) return;
            if (e.key === 'ArrowDown') {
                e.preventDefault();
                this._acSelectedIdx = Math.min(this._acSelectedIdx + 1, this._acResults.length - 1);
            } else if (e.key === 'ArrowUp') {
                e.preventDefault();
                this._acSelectedIdx = Math.max(this._acSelectedIdx - 1, 0);
            } else if (e.key === 'Enter' && this._acSelectedIdx >= 0) {
                e.preventDefault();
                // Will be handled by the template
            } else if (e.key === 'Escape') {
                this._acVisible = false;
            }
        },

        _buildActionGroups() {
            if (typeof actionOptList === 'undefined') { console.warn('[V2] actionOptList not found'); return; }
            const groups = {};
            const user = typeof getUser === 'function' ? getUser() : { language: 'en' };
            // Build group metadata from actionOptGroupList
            const groupMeta = {};
            if (typeof actionOptGroupList !== 'undefined') {
                actionOptGroupList.forEach(g => {
                    const pictoMatch = g.picto ? g.picto.match(/src=['"]([^'"]+)['"]/) : null;
                    groupMeta[g.name] = {
                        label: g.label ? (g.label[user.language] || g.label['en'] || g.name) : g.name,
                        picto: pictoMatch ? pictoMatch[1] : null
                    };
                });
            }
            for (const key in actionOptList) {
                if (key === 'unknown') continue;
                const item = actionOptList[key];
                const gName = item.group || 'Other';
                if (!groups[gName]) {
                    const meta = groupMeta[gName] || {};
                    groups[gName] = {
                        name: gName,
                        label: meta.label || gName,
                        picto: meta.picto || null,
                        options: []
                    };
                }
                const label = item.label ? (item.label[user.language] || item.label['en'] || key) : key;
                groups[gName].options.push({ value: key, label: label });
            }
            this.actionGroups = Object.values(groups);
            console.info('[V2] Action groups built:', this.actionGroups.length, 'groups');
        },
        _buildControlGroups() {
            if (typeof convertToGui === 'undefined') { console.warn('[V2] convertToGui not found'); return; }
            const groups = {};
            const user = typeof getUser === 'function' ? getUser() : { language: 'en' };
            for (const key in convertToGui) {
                if (key === 'unknown') continue;
                const item = convertToGui[key];
                const gName = item.control || 'Other';
                if (!groups[gName]) {
                    groups[gName] = { name: gName, label: gName, options: [] };
                }
                const label = key.replace(/([A-Z])/g, ' $1').replace(/^./, s => s.toUpperCase()).trim();
                groups[gName].options.push({ value: key, label: label });
            }
            this.controlGroups = Object.values(groups);
            console.info('[V2] Control groups built:', this.controlGroups.length, 'groups');
        },
        // Helper: get display label for a selected action value
        getActionLabel(actionValue) {
            for (const g of this.actionGroups) {
                const opt = g.options.find(o => o.value === actionValue);
                if (opt) return opt.label;
            }
            return actionValue || '';
        },
        // Helper: get display label for a selected control value
        getControlLabel(controlValue) {
            for (const g of this.controlGroups) {
                const opt = g.options.find(o => o.value === controlValue);
                if (opt) return opt.label;
            }
            return controlValue || '';
        }


    };
}
