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

var _v2_uid = 0;
function v2uid() { return '__v2_' + (++_v2_uid); }

/**
 * Show a floating copy-confirmation toast near the mouse cursor.
 * @param {MouseEvent|null} evt   — mouse event for positioning (null → center of viewport)
 * @param {string}          msg   — message to display (e.g. "Step copied!")
 */
function v2CopyToast(evt, msg) {
    var x = evt ? evt.clientX : window.innerWidth / 2;
    var y = evt ? evt.clientY : window.innerHeight / 2;
    var el = document.createElement('div');
    el.className = 'v2-copy-toast';
    el.innerHTML = '<span class="v2-copy-toast__icon"><svg viewBox="0 0 24 24"><path d="M5 13l4 4L19 7"/></svg></span>' + msg;
    el.style.left = (x - 10) + 'px';
    el.style.top = (y - 10) + 'px';
    document.body.appendChild(el);
    el.addEventListener('animationend', function() { el.remove(); });
}

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

        _dragIdx: -1,
        _dragControlInfo: null,
        lastRunCountry: '',
        lastRunEnvironment: '',
        lastRunRobot: '',
        lastRunQueueId: 0,
        lastRunTag: '',

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
                    this.testInfo.lastRunId = 0; // will be set by ReadTestCaseExecution below
                    // Fetch last execution details for re-run button
                    $.ajax({ url: 'ReadTestCaseExecution', data: { test: test, testCase: testcase }, dataType: 'json',
                        success: (exData) => {
                            var ct = exData.contentTable;
                            if (ct && ct.id) {
                                this.testInfo.lastRunId = ct.id;
                                this.lastRunCountry = ct.country || '';
                                this.lastRunEnvironment = ct.env || '';  // API returns 'env' not 'environment'
                                this.lastRunTag = ct.tag || '';
                                this.lastRunQueueId = ct.queueId || 0;
                            }
                        }
                    });
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
                    // Load autocomplete cache (objects, datalib)
                    this._loadAcCache();
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
            self.steps = self.steps.concat([s]);
            self._markDirty();
            self.$nextTick(() => {
                self.activeStepIndex = self.steps.length - 1;
                if (window.lucide) lucide.createIcons();
            });
        },
        duplicateStep(idx) {
            this._mutate(() => {
                const clone = JSON.parse(JSON.stringify(this.steps[idx]));
                // Re-normalize the full clone so Alpine can track all nested objects
                const normalized = this._normalizeStep(clone);
                normalized.stepId = -1;
                normalized.description = (normalized.description || '') + ' (copy)';
                (normalized.actions || []).forEach(a => { a.actionId = -1; (a.controls || []).forEach(c => { c.controlId = -1; }); });
                // Force reactivity: create new array reference
                var before = this.steps.slice(0, idx + 1);
                var after = this.steps.slice(idx + 1);
                this.steps = before.concat([normalized], after);
                this.$nextTick(() => { this.selectStep(idx + 1); });
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
                        // Force reactivity: reassign steps array
                        self.steps = self.steps.concat([s]);
                        self._markDirty();
                        self.$nextTick(() => { self.activeStepIndex = self.steps.length - 1; });
                    }
                } else {
                    // Import selected library steps — collect all imports then apply at once
                    var importInfos = $("[name='importInfo']").toArray();
                    var promises = importInfos.map(function(importInfo) {
                        var stepInfo = $(importInfo).data('stepInfo');
                        if (!stepInfo) return Promise.resolve(null);
                        var parentDiv = $(importInfo).closest('[id]');
                        var useStepChecked = parentDiv.find("[name='useStep']").prop('checked');
                        return new Promise(function(resolve) {
                            $.ajax({
                                url: 'ReadTestCaseStep',
                                data: { test: stepInfo.test, testcase: stepInfo.testCase, stepId: stepInfo.step },
                                dataType: 'json',
                                success: function(data) {
                                    if (!data || !data.step) { resolve(null); return; }
                                    resolve(self._normalizeStep({
                                        stepId: -1, sort: 0,
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
                                        actions: useStepChecked ? [] : JSON.parse(JSON.stringify(data.step.actions || []))
                                    }));
                                },
                                error: function() { resolve(null); }
                            });
                        });
                    });
                    Promise.all(promises).then(function(results) {
                        var newSteps = results.filter(function(s) { return s !== null; });
                        if (newSteps.length === 0) return;
                        // Assign sort values
                        newSteps.forEach(function(s, i) { s.sort = self.steps.length + i + 1; });
                        // Force reactivity: create a new array reference
                        self.steps = self.steps.concat(newSteps);
                        self._markDirty();
                        self.$nextTick(() => {
                            self.activeStepIndex = self.steps.length - 1;
                            if (window.lucide) lucide.createIcons();
                        });
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
                this.activeStep.actions = this.activeStep.actions.concat([a]);
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
                var before = this.activeStep.actions.slice(0, aIdx + 1);
                var after = this.activeStep.actions.slice(aIdx + 1);
                this.activeStep.actions = before.concat([a], after);
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
                this.activeStep.actions[aIdx].controls = this.activeStep.actions[aIdx].controls.concat([c]);
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

        addProperty(name) {
            this._mutate(() => {
                const p = this._normalizeProp({
                    property: name || ('PROP-' + this.properties.length), description: '',
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
        copyProperty(pIdx, $event) {
            var self = this;
            var evt = $event || window.event || null;
            var prop = this.filteredProperties[pIdx];
            var payload = JSON.stringify({ type: 'property', data: prop });
            if (navigator.clipboard) {
                navigator.clipboard.writeText(payload).then(function() {
                    self.clipboardType = 'property';
                    v2CopyToast(evt, 'Property copied!');
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
                                // Generate unique name with Copy-N suffix
                                var baseName = (clone.property || 'PROP').replace(/-Copy-\d+$/, '');
                                var existingNames = self.properties.map(function(p) { return p.property; });
                                var copyNum = 1;
                                var newName = baseName + '-Copy-' + copyNum;
                                while (existingNames.indexOf(newName) >= 0) {
                                    copyNum++;
                                    newName = baseName + '-Copy-' + copyNum;
                                }
                                clone.property = newName;
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
            // If no name, open in ADD mode directly
            if (!datalibName || !datalibName.trim()) {
                if (typeof window.openModalDataLib === 'function') {
                    window.openModalDataLib(null, '', 'ADD', 'TestCaseScript_Steps', null);
                }
                return;
            }
            // Smart EDIT/ADD — check if datalib exists via AJAX (same format as V1 property.js)
            $.ajax({
                url: 'ReadTestDataLib',
                data: { name: datalibName.trim(), limit: 15, like: 'N' },
                dataType: 'json',
                success: function(data) {
                    if (data.messageType === 'OK' && data.contentTable && data.contentTable.length >= 1) {
                        // DataLib exists → EDIT
                        window.openModalDataLib(data.contentTable[0].testDataLibID, datalibName, 'EDIT', 'TestCaseScript_Steps', null);
                    } else {
                        // DataLib doesn't exist → ADD
                        window.openModalDataLib(null, datalibName, 'ADD', 'TestCaseScript_Steps', null);
                    }
                },
                error: function() {
                    // On error, open in ADD mode
                    if (typeof window.openModalDataLib === 'function') {
                        window.openModalDataLib(null, datalibName, 'ADD', 'TestCaseScript_Steps', null);
                    }
                }
            });
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
            else if (text.indexOf('# cerberus:control') === 0) { type = 'control'; }
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

        copyStep(idx, $event) {
            var self = this;
            var evt = $event || window.event || null;
            var step = this._cleanForExport(this.steps[idx]);
            var yamlContent = this._toYaml(step);
            var header = '# cerberus:step\n# Copied from: ' + (this.testInfo.test || '') + ' / ' + (this.testInfo.testCase || '') + '\n---\n';
            var jsonBackup = '\n# __json__: ' + JSON.stringify(step);
            var fullText = header + yamlContent + jsonBackup;

            navigator.clipboard.writeText(fullText).then(function() {
                self.clipboardType = 'step';
                v2CopyToast(evt, 'Step copied!');
            }).catch(function() {
                showMessageMainPage('warning', 'Could not access clipboard');
            });
        },

        copyAction(aIdx, $event) {
            var self = this;
            var evt = $event || window.event || null;
            var action = this._cleanForExport(this.activeStep.actions[aIdx]);
            var yamlContent = this._toYaml(action);
            var header = '# cerberus:action\n# Copied from: ' + (this.testInfo.test || '') + ' / ' + (this.testInfo.testCase || '') + '\n---\n';
            var jsonBackup = '\n# __json__: ' + JSON.stringify(action);
            var fullText = header + yamlContent + jsonBackup;

            navigator.clipboard.writeText(fullText).then(function() {
                self.clipboardType = 'action';
                v2CopyToast(evt, 'Action copied!');
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
                // Deep-clone to break any shared references, then re-normalize
                var freshData = JSON.parse(JSON.stringify(data));
                var clone = this._normalizeStep(freshData);
                clone.stepId = -1;
                clone.description = (clone.description || 'Pasted step') + ' (pasted)';
                (clone.actions || []).forEach(a => {
                    a.actionId = -1;
                    (a.controls || []).forEach(c => { c.controlId = -1; });
                });
                // Force reactivity: create new array reference
                var before = this.steps.slice(0, idx + 1);
                var after = this.steps.slice(idx + 1);
                this.steps = before.concat([clone], after);
                this.$nextTick(() => { this.selectStep(idx + 1); });
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
                var freshData = JSON.parse(JSON.stringify(data));
                var clone = this._normalizeAction(freshData);
                clone.actionId = -1;
                (clone.controls || []).forEach(c => { c.controlId = -1; });
                // Force reactivity: create new array reference
                this.activeStep.actions = this.activeStep.actions.concat([clone]);
            });
            showMessageMainPage('success', 'Action pasted successfully');
        },

        copyControl(aIdx, cIdx, $event) {
            var self = this;
            var evt = $event || window.event || null;
            var ctrl = this._cleanForExport(this.activeStep.actions[aIdx].controls[cIdx]);
            var yamlContent = this._toYaml(ctrl);
            var header = '# cerberus:control\n# Copied from: ' + (this.testInfo.test || '') + ' / ' + (this.testInfo.testCase || '') + '\n---\n';
            var jsonBackup = '\n# __json__: ' + JSON.stringify(ctrl);
            var fullText = header + yamlContent + jsonBackup;

            navigator.clipboard.writeText(fullText).then(function() {
                self.clipboardType = 'control';
                v2CopyToast(evt, 'Control copied!');
            }).catch(function() {
                showMessageMainPage('warning', 'Could not access clipboard');
            });
        },

        async pasteControl(aIdx) {
            if (!this.activeStep) return;
            var data = null;
            try {
                var text = await navigator.clipboard.readText();
                var parsed = this._parseClipboardText(text);
                if (parsed && parsed.type === 'control') {
                    data = parsed.data;
                } else if (parsed && (parsed.type === 'step' || parsed.type === 'action')) {
                    showMessageMainPage('warning', 'Clipboard contains a ' + parsed.type + ', not a Control.');
                    return;
                }
            } catch(e) {}

            if (!data) {
                showMessageMainPage('warning', 'No control in clipboard. Copy a control first.');
                return;
            }
            this._mutate(() => {
                var freshData = JSON.parse(JSON.stringify(data));
                var clone = this._normalizeControl(freshData);
                clone.controlId = -1;
                this.activeStep.actions[aIdx].controls = this.activeStep.actions[aIdx].controls.concat([clone]);
            });
            showMessageMainPage('success', 'Control pasted successfully');
        },

        duplicateControl(aIdx, cIdx) {
            this._mutate(() => {
                var clone = JSON.parse(JSON.stringify(this.activeStep.actions[aIdx].controls[cIdx]));
                clone = this._normalizeControl(clone);
                clone.controlId = -1;
                clone.description = (clone.description || '') + ' (copy)';
                var controls = this.activeStep.actions[aIdx].controls;
                var before = controls.slice(0, cIdx + 1);
                var after = controls.slice(cIdx + 1);
                this.activeStep.actions[aIdx].controls = before.concat([clone], after);
            });
            this.$nextTick(() => { if (window.lucide) lucide.createIcons(); });
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
            // Re-run: duplicate the last queue entry and redirect (legacy pattern)
            if (!this.lastRunQueueId || this.lastRunQueueId <= 0) {
                // No previous queue entry, fallback to normal run
                this.runTestCase();
                return;
            }
            $.ajax({
                url: 'CreateTestCaseExecutionQueue',
                async: true,
                method: 'POST',
                data: {
                    id: this.lastRunQueueId,
                    actionState: 'toQUEUED',
                    actionSave: 'save',
                    tag: this.lastRunTag || ''
                },
                success: function(data) {
                    if (getAlertType(data.messageType) === 'success') {
                        var url = './TestCaseExecutionV2.jsp?executionQueueId=' + encodeURI(data.testCaseExecutionQueueList[0].id);
                        window.location.replace(url);
                    } else {
                        showMessageMainPage(getAlertType(data.messageType), data.message, false, 60000);
                    }
                },
                error: handleErrorAjaxAfterTimeout
            });
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

        // ══════════════════════════════════════════════════
        // ── Autocomplete System V2 — Full %variable% support ──
        // ══════════════════════════════════════════════════

        // --- State ---
        _acResults: [],
        _acVisible: false,
        _acBlurTimer: null,
        _acActiveField: null,
        _acSelectedIdx: -1,
        _acActiveAIdx: -1,
        _acActiveCIdx: -1,
        _acMode: 'field',   // 'field' = normal field suggestions, 'variable' = %xxx% inline
        _acVarPrefix: '',   // e.g. '%property.' — what the user has typed so far
        _acVarLevel: '',    // 'root' | 'property' | 'system' | 'object' | 'objectProp' | 'datalib' | 'datalibProp'
        _acCreateLabel: '', // label for the "Create" button at the bottom

        // --- Cache loaded once at init ---
        _acCache: {
            objects: [],    // [{value:'btnLogin', label:'btnLogin', desc:'ApplicationObject'}]
            datalib: [],    // [{value:'myLib', label:'myLib', desc:'DataLib'}]
            system: [
                'SYSTEM','APPLI','BROWSER','ROBOT','ROBOTDECLI','SCREENSIZE',
                'APP_DOMAIN','APP_HOST','APP_CONTEXTROOT','EXEURL','APP_VAR1','APP_VAR2','APP_VAR3','APP_VAR4','APP_SECRET1','APP_SECRET2',
                'ENV','ENVGP',
                'COUNTRY','COUNTRYGP1','COUNTRYGP2','COUNTRYGP3','COUNTRYGP4','COUNTRYGP5','COUNTRYGP6','COUNTRYGP7','COUNTRYGP8','COUNTRYGP9',
                'TEST','TESTCASE','TESTCASEDESCRIPTION',
                'SSIP','SSPORT','TAG','EXECUTIONID',
                'EXESTART','EXEELAPSEDMS','EXESTORAGEURL',
                'STEP.n.n.RETURNCODE','CURRENTSTEP_INDEX','CURRENTSTEP_STARTISO','CURRENTSTEP_ELAPSEDMS','CURRENTSTEP_SORT',
                'LASTSERVICE_HTTPCODE','LASTSERVICE_CALL','LASTSERVICE_RESPONSE','LASTSERVICE_RESPONSETIME',
                'TODAY-yyyy','TODAY-MM','TODAY-dd','TODAY-D','TODAY-HH','TODAY-mm','TODAY-ss',
                'YESTERDAY-yyyy','YESTERDAY-MM','YESTERDAY-dd','YESTERDAY-D','YESTERDAY-HH','YESTERDAY-mm','YESTERDAY-ss',
                'TOMORROW-yyyy','TOMORROW-MM','TOMORROW-dd','TOMORROW-D'
            ],
            objectProps: ['value', 'pictureurl', 'base64'],
            datalibProps: ['value', 'base64'],
            loaded: false
        },

        _loadAcCache() {
            var self = this;
            if (self._acCache.loaded) return;
            var app = self.testInfo.application;
            if (!app) return;
            self._acCache.loaded = true;
            // Load application objects
            $.ajax({ url: 'ReadApplicationObject', data: { application: app }, dataType: 'json',
                success: function(data) {
                    if (data && data.contentTable) {
                        self._acCache.objects = data.contentTable.map(function(o) {
                            return { value: o.object, label: o.object, desc: 'Object', icon: 'object' };
                        });
                    }
                    console.info('[V2-AC] Loaded', self._acCache.objects.length, 'application objects');
                }
            });
            // Load datalib names
            $.ajax({ url: 'ReadTestDataLib', data: { columnName: 'tdl.Name' }, dataType: 'json',
                success: function(data) {
                    if (data && data.distinctValues) {
                        self._acCache.datalib = data.distinctValues.map(function(name) {
                            return { value: name, label: name, desc: 'DataLib', icon: 'datalib' };
                        });
                    }
                    console.info('[V2-AC] Loaded', self._acCache.datalib.length, 'datalib entries');
                }
            });
        },

        // --- Field auto-type detection (unchanged logic, rewritten for clarity) ---
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

        // --- Static suggestions for field-level autocomplete ---
        _staticSuggestions: {
            element: [
                { value: 'xpath=', label: 'xpath=', desc: 'XPath selector', icon: 'element' },
                { value: 'id=', label: 'id=', desc: 'Element ID', icon: 'element' },
                { value: 'name=', label: 'name=', desc: 'Element name attribute', icon: 'element' },
                { value: 'class=', label: 'class=', desc: 'CSS class name', icon: 'element' },
                { value: 'css=', label: 'css=', desc: 'CSS selector', icon: 'element' },
                { value: 'data-cerberus=', label: 'data-cerberus=', desc: 'Cerberus data attribute', icon: 'element' },
                { value: 'picture=', label: 'picture=', desc: 'Image recognition', icon: 'element' },
                { value: 'coord=', label: 'coord=', desc: 'Screen coordinates', icon: 'element' },
            ],
            'switch': [
                { value: 'title=', label: 'title=', desc: 'Window title', icon: 'element' },
                { value: 'url=', label: 'url=', desc: 'Window URL', icon: 'element' },
                { value: 'regexTitle=', label: 'regexTitle=', desc: 'Regex on title', icon: 'element' },
                { value: 'regexUrl=', label: 'regexUrl=', desc: 'Regex on URL', icon: 'element' },
            ],
            select: [
                { value: 'value=', label: 'value=', desc: 'By value attribute', icon: 'element' },
                { value: 'label=', label: 'label=', desc: 'By visible text', icon: 'element' },
                { value: 'index=', label: 'index=', desc: 'By index position', icon: 'element' },
            ],
            boolean: [
                { value: 'true', label: 'true', desc: 'Boolean true', icon: 'element' },
                { value: 'false', label: 'false', desc: 'Boolean false', icon: 'element' },
            ],
            fileuploadflag: [
                { value: 'EMPTYFOLDER', label: 'EMPTYFOLDER', desc: 'Empty folder before upload', icon: 'element' },
            ],
            filesortflag: [
                { value: 'LASTMODIFIED', label: 'LASTMODIFIED', desc: 'Sort by last modified', icon: 'element' },
                { value: 'DESC', label: 'DESC', desc: 'Descending order', icon: 'element' },
                { value: 'ASC', label: 'ASC', desc: 'Ascending order', icon: 'element' },
                { value: 'IGNORECASEDESC', label: 'IGNORECASEDESC', desc: 'Case-insensitive descending', icon: 'element' },
                { value: 'IGNORECASEASC', label: 'IGNORECASEASC', desc: 'Case-insensitive ascending', icon: 'element' },
            ],
        },

        _getPropertySuggestions() {
            var seen = {};
            var props = [];
            if (this.properties) {
                this.properties.forEach(function(p) {
                    if (p.property && !seen[p.property]) {
                        seen[p.property] = true;
                        props.push({ value: p.property, label: p.property, desc: p.type || 'Local', icon: 'property' });
                    }
                });
            }
            if (this.inheritedProperties) {
                this.inheritedProperties.forEach(function(p) {
                    if (p.property && !seen[p.property]) {
                        seen[p.property] = true;
                        props.push({ value: p.property, label: p.property, desc: (p.type || 'Inherited') + ' (inh.)', icon: 'property' });
                    }
                });
            }
            return props;
        },

        /** Open property modal by name */
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

        /** Pencil button handler */
        _openFieldEditModal(actionKey, fieldKey, isControl, value) {
            var autoType = this._getFieldAutoType(actionKey, fieldKey, isControl);
            if (autoType === 'service') {
                this.openModalService(value);
            } else if (autoType === 'property') {
                this._openPropertyByName(value);
            }
        },

        /**
         * Smart Edit/Create handler — like V1, detects existence then opens the right modal.
         * For service: AJAX call to ReadAppService to check existence, then EDIT or ADD mode.
         * For property: checks local + inherited properties, opens editor or creates + opens editor.
         */
        _openFieldSmartModal(actionKey, fieldKey, isControl, value) {
            if (!value || !value.trim()) return;
            var autoType = this._getFieldAutoType(actionKey, fieldKey, isControl);
            var val = value.trim();
            var self = this;

            if (autoType === 'service') {
                // Like V1: AJAX call to check if service exists
                console.log('[V2] _openFieldSmartModal service:', val);
                $.ajax({
                    url: 'ReadAppService?service=' + encodeURIComponent(val),
                    dataType: 'json',
                    success: function(data) {
                        console.log('[V2] ReadAppService response:', data);
                        var mode = (data.contentTable && data.contentTable.hasPermissions !== undefined) ? 'EDIT' : 'ADD';
                        console.log('[V2] Opening modal in mode:', mode);
                        if (typeof openModalAppService === 'function') {
                            openModalAppService(encodeURIComponent(val), mode, 'TestCase');
                        } else {
                            console.warn('[V2] openModalAppService not found!');
                            window.open('AppServiceList.jsp?service=' + encodeURIComponent(val), '_blank');
                        }
                    },
                    error: function(xhr, status, err) {
                        console.warn('[V2] ReadAppService error:', status, err);
                        // On error, assume ADD
                        if (typeof openModalAppService === 'function') {
                            openModalAppService(encodeURIComponent(val), 'ADD', 'TestCase');
                        }
                    }
                });
            } else if (autoType === 'property') {
                // Check if property exists locally or inherited
                var allProps = (this.properties || []).concat(this.inheritedProperties || []);
                var exists = allProps.some(function(p) { return p.property === val; });
                if (exists) {
                    // Property exists — open its editor
                    this._openPropertyByName(val);
                } else {
                    // Property doesn't exist — create it with name pre-filled, switch to tab, open editor
                    var p = this._normalizeProp({
                        property: val, description: '',
                        type: 'text', database: '', value1: '', value2: '', value3: 'value',
                        countries: this.tcCountries.slice(), nature: 'STATIC', length: '0', rowLimit: 0,
                        cacheExpire: 0, rank: 0, retryNb: 0, retryPeriod: 0
                    });
                    this._mutate(function() { self.properties.push(p); });
                    this.tab = 'properties';
                    this.propertySearch = '';
                    this.propertyFilter = 'all';
                    this.$nextTick(function() {
                        var idx = self.filteredProperties.findIndex(function(fp) { return fp._uid === p._uid; });
                        if (idx >= 0) self.openPropertyEditor(idx);
                    });
                }
            }
        },

        // ── DataLib autocomplete for property value1 when type=getFromDataLib ──
        _dlAutoItems: [],
        _dlSearch: '',
        _dlVisible: false,
        _dlSelectedIdx: -1,
        _dlFetchTimeout: null,

        // Fetch DataLib suggestions from API (debounced) — uses same API as V1 CompleterForAllDataLib
        _dlFetch(query) {
            var self = this;
            clearTimeout(this._dlFetchTimeout);
            this._dlFetchTimeout = setTimeout(function() {
                $.ajax({
                    url: 'ReadTestDataLib',
                    data: { name: query || '', limit: 15, like: 'Y' },
                    dataType: 'json',
                    success: function(data) {
                        if (data.contentTable) {
                            self._dlAutoItems = data.contentTable.map(function(d) { return { label: d.name, value: d.name }; });
                        } else if (data.distinctValues) {
                            self._dlAutoItems = data.distinctValues.map(function(v) { return { label: v, value: v }; });
                        }
                    },
                    error: function() {
                        self._dlAutoItems = [];
                    }
                });
            }, 200);
        },

        // Filtered DataLib suggestions
        get _dlFiltered() {
            var s = (this._dlSearch || '').toLowerCase();
            if (!s) return this._dlAutoItems;
            return this._dlAutoItems.filter(function(it) { return it.label.toLowerCase().indexOf(s) >= 0; });
        },

        // Called when datalib input gets focus
        _dlOnFocus(prop) {
            this._dlSearch = prop.value1 || '';
            this._dlVisible = true;
            this._dlSelectedIdx = -1;
            this._dlFetch(this._dlSearch);
        },

        // Called when datalib input changes
        _dlOnInput(prop, val) {
            this._dlSearch = val;
            prop.value1 = val;
            this._dlFetch(val);
            this._dlVisible = true;
        },

        // Called when a datalib suggestion is selected
        _dlSelect(prop, item) {
            prop.value1 = item.value;
            this._dlSearch = item.value;
            this._dlVisible = false;
        },

        _acFetchTimeout: null,
        _acServiceCache: {},

        // ══════════════════════════════════════════════
        // ── acOnFocus — called when any value input gets focus ──
        // ══════════════════════════════════════════════
        acOnFocus(typeKey, fieldKey, isControl, currentVal, aIdx, cIdx) {
            // Cancel any pending blur-hide so the dropdown stays open
            if (this._acBlurTimer) { clearTimeout(this._acBlurTimer); this._acBlurTimer = null; }
            var autoType = this._getFieldAutoType(typeKey, fieldKey, isControl);
            this._acActiveField = { typeKey: typeKey, fieldKey: fieldKey, isControl: isControl, autoType: autoType };
            this._acActiveAIdx = (aIdx !== undefined) ? aIdx : -1;
            this._acActiveCIdx = (cIdx !== undefined) ? cIdx : -1;
            this._acSelectedIdx = -1;
            this._acCreateLabel = '';

            // Check if we are already inside a %variable
            var varState = this._parseVariableContext(currentVal || '', null);
            if (varState) {
                this._enterVariableMode(varState, currentVal || '');
                return;
            }

            // Normal field-level autocomplete
            this._acMode = 'field';
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
                // 'variable' type — don't show anything until user types %
                this._acVisible = false;
            }
        },

        // ══════════════════════════════════════════════
        // ── acOnInput — called on every keystroke ──
        // ══════════════════════════════════════════════
        acOnInput(currentVal, inputEl) {
            console.log('[V2-AC] acOnInput called:', { currentVal: currentVal, hasInputEl: !!inputEl, activeField: !!this._acActiveField });
            if (!this._acActiveField) { console.warn('[V2-AC] No active field, returning'); return; }
            var autoType = this._acActiveField.autoType;
            console.log('[V2-AC] autoType:', autoType);
            this._acSelectedIdx = -1;
            this._acCreateLabel = '';

            // Always check for %variable% context first
            var cursorPos = inputEl ? inputEl.selectionStart : (currentVal || '').length;
            console.log('[V2-AC] cursorPos:', cursorPos, 'val:', currentVal);
            var varState = this._parseVariableContext(currentVal || '', cursorPos);
            console.log('[V2-AC] varState:', varState);

            if (varState) {
                this._enterVariableMode(varState, currentVal || '');
                console.log('[V2-AC] After enterVariableMode:', { results: this._acResults.length, visible: this._acVisible, mode: this._acMode });
                return;
            }

            // If we were in variable mode but % context ended, exit
            if (this._acMode === 'variable') {
                this._acMode = 'field';
            }

            // Normal field-level autocomplete
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
            } else {
                this._acVisible = false;
            }
        },

        // ══════════════════════════════════════════════
        // ── %variable% Parser ──
        // Analyses the text around cursor to detect %xxx.yyy context
        // ══════════════════════════════════════════════
        _parseVariableContext(text, cursorPos) {
            if (cursorPos === null || cursorPos === undefined) cursorPos = text.length;
            var before = text.substring(0, cursorPos);

            // Count % before cursor — if even, no open variable
            var pctCount = (before.match(/%/g) || []).length;
            console.log('[V2-AC] _parseVariableContext:', { text: text, cursorPos: cursorPos, before: before, pctCount: pctCount });
            if (pctCount === 0 || pctCount % 2 === 0) return null;

            // Extract from last % to cursor
            var lastPct = before.lastIndexOf('%');
            var fragment = before.substring(lastPct + 1); // e.g. "property.myPr" or "system.ENV" or ""
            var parts = fragment.split('.');

            if (parts.length === 1) {
                // User typed % then maybe started a category: %pro...
                return { level: 'root', search: parts[0], startIdx: lastPct };
            } else if (parts.length === 2) {
                var category = parts[0].toLowerCase();
                if (category === 'property' || category === 'system' || category === 'object' || category === 'datalib') {
                    return { level: category, search: parts[1], startIdx: lastPct };
                }
                return null;
            } else if (parts.length === 3) {
                var category = parts[0].toLowerCase();
                if (category === 'object') {
                    return { level: 'objectProp', objectName: parts[1], search: parts[2], startIdx: lastPct };
                } else if (category === 'datalib') {
                    return { level: 'datalibProp', datalibName: parts[1], search: parts[2], startIdx: lastPct };
                }
                return null;
            }
            return null;
        },

        // ══════════════════════════════════════════════
        // ── Enter variable mode with parsed context ──
        // ══════════════════════════════════════════════
        _enterVariableMode(varState, currentVal) {
            this._acMode = 'variable';
            this._acVarLevel = varState.level;
            var search = (varState.search || '').toLowerCase();
            var results = [];
            this._acCreateLabel = '';

            switch (varState.level) {
                case 'root':
                    results = [
                        { value: 'property', label: 'property', desc: 'Test case properties', icon: 'property' },
                        { value: 'system', label: 'system', desc: 'System variables', icon: 'system' },
                        { value: 'object', label: 'object', desc: 'Application objects', icon: 'object' },
                        { value: 'datalib', label: 'datalib', desc: 'Data libraries', icon: 'datalib' },
                    ];
                    if (search) {
                        results = results.filter(function(r) { return r.value.indexOf(search) >= 0; });
                    }
                    break;

                case 'property':
                    results = this._getPropertySuggestions();
                    if (search) {
                        results = results.filter(function(r) { return r.label.toLowerCase().indexOf(search) >= 0; });
                    }
                    // Show create if no exact match
                    if (search && !results.some(function(r) { return r.value.toLowerCase() === search; })) {
                        this._acCreateLabel = search;
                    }
                    break;

                case 'system':
                    results = this._acCache.system.map(function(s) {
                        return { value: s, label: s, desc: 'System', icon: 'system' };
                    });
                    if (search) {
                        results = results.filter(function(r) { return r.label.toLowerCase().indexOf(search) >= 0; });
                    }
                    break;

                case 'object':
                    results = this._acCache.objects.slice();
                    if (search) {
                        results = results.filter(function(r) { return r.label.toLowerCase().indexOf(search) >= 0; });
                    }
                    if (search && !results.some(function(r) { return r.value.toLowerCase() === search; })) {
                        this._acCreateLabel = search;
                    }
                    break;

                case 'objectProp':
                    results = this._acCache.objectProps.map(function(p) {
                        return { value: p, label: p, desc: 'Object attribute', icon: 'object' };
                    });
                    if (search) {
                        results = results.filter(function(r) { return r.label.toLowerCase().indexOf(search) >= 0; });
                    }
                    break;

                case 'datalib':
                    results = this._acCache.datalib.slice();
                    if (search) {
                        results = results.filter(function(r) { return r.label.toLowerCase().indexOf(search) >= 0; });
                    }
                    if (search && !results.some(function(r) { return r.value.toLowerCase() === search; })) {
                        this._acCreateLabel = search;
                    }
                    break;

                case 'datalibProp':
                    results = this._acCache.datalibProps.map(function(p) {
                        return { value: p, label: p, desc: 'DataLib attribute', icon: 'datalib' };
                    });
                    if (search) {
                        results = results.filter(function(r) { return r.label.toLowerCase().indexOf(search) >= 0; });
                    }
                    break;
            }

            this._acResults = results;
            this._acVisible = results.length > 0 || !!this._acCreateLabel;
        },

        // ══════════════════════════════════════════════
        // ── acSelect — handles selection for both modes ──
        // ══════════════════════════════════════════════
        acSelect(item) {
            if (this._acMode === 'field') {
                this._acVisible = false;
                return item.value;
            }
            // Variable mode — handled by acSelectVariable
            return item.value;
        },

        // ══════════════════════════════════════════════
        // ── acSelectVariable — smart insertion at cursor position ──
        // Called from the template when user clicks/enters a variable suggestion
        // Returns the new full input value
        // ══════════════════════════════════════════════
        acSelectVariable(item, inputEl) {
            var text = inputEl.value || '';
            var cursorPos = inputEl.selectionStart || text.length;
            var before = text.substring(0, cursorPos);
            var after = text.substring(cursorPos);
            var lastPct = before.lastIndexOf('%');
            var prefix = text.substring(0, lastPct + 1); // everything up to and including the %
            var level = this._acVarLevel;

            var inserted = '';
            var keepOpen = false;

            switch (level) {
                case 'root':
                    // User selected a category — insert "category." and keep open
                    inserted = item.value + '.';
                    keepOpen = true;
                    break;
                case 'property':
                case 'system':
                    // Terminal — insert "property.xxx%" or "system.xxx%"
                    inserted = level + '.' + item.value + '%';
                    break;
                case 'object':
                case 'datalib':
                    // Intermediate — insert "object.xxx." and keep open for sub-property
                    inserted = level + '.' + item.value + '.';
                    keepOpen = true;
                    break;
                case 'objectProp':
                case 'datalibProp':
                    // Terminal — rebuild full path: "object.objName.prop%"
                    var fragment = before.substring(lastPct + 1);
                    var parts = fragment.split('.');
                    inserted = parts[0] + '.' + parts[1] + '.' + item.value + '%';
                    break;
            }

            var newValue = prefix + inserted + after;
            var newCursorPos = (prefix + inserted).length;

            // Update the input
            inputEl.value = newValue;
            inputEl.setSelectionRange(newCursorPos, newCursorPos);
            inputEl.dispatchEvent(new Event('input', { bubbles: true }));

            if (keepOpen) {
                // Re-parse to show next level
                var self = this;
                setTimeout(function() {
                    var vs = self._parseVariableContext(newValue, newCursorPos);
                    if (vs) {
                        self._enterVariableMode(vs, newValue);
                    }
                }, 50);
            } else {
                this._acVisible = false;
                this._acMode = 'field';
            }

            return newValue;
        },

        // ══════════════════════════════════════════════
        // ── Create actions from dropdown ──
        // ══════════════════════════════════════════════
        acCreateProperty(name) {
            this._acVisible = false;
            var self = this;
            this._mutate(function() {
                var p = self._normalizeProp({
                    property: name, description: '',
                    type: 'text', database: '', value1: '', value2: '', value3: 'value',
                    countries: self.tcCountries.slice(), nature: 'STATIC', length: '0', rowLimit: 0,
                    cacheExpire: 0, rank: 0, retryNb: 0, retryPeriod: 0
                });
                self.properties = self.properties.concat([p]);
            });
            showMessageMainPage('success', 'Property "' + name + '" created');
        },

        acCreateObject(name, inputEl) {
            this._acVisible = false;
            if (typeof openModalApplicationObject === 'function') {
                openModalApplicationObject(this.testInfo.application, name, 'ADD', 'testCaseScript');
            }
        },

        acCreateDataLib(name, inputEl) {
            this._acVisible = false;
            if (typeof openModalDataLib === 'function') {
                openModalDataLib(null, name, 'ADD', 'TestCaseScript_Steps', null);
            }
        },

        acCreateService(name) {
            this._acVisible = false;
            if (typeof openModalAppService === 'function') {
                openModalAppService(encodeURIComponent(name), 'ADD', 'TestCase');
            }
        },

        // ══════════════════════════════════════════════
        // ── Filtering and fetching ──
        // ══════════════════════════════════════════════
        _filterAcResults(q) {
            if (!q) return;
            var lower = q.toLowerCase();
            this._acResults = this._acResults.filter(function(r) {
                return r.label.toLowerCase().indexOf(lower) >= 0 || r.value.toLowerCase().indexOf(lower) >= 0;
            });
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
                        return { value: s.service, label: s.service, desc: s.type || 'Service', icon: 'service' };
                    });
                } else {
                    self._acResults = [];
                }
                var lower = (query || '').toLowerCase();
                // Show create button if no exact match
                self._acCreateLabel = '';
                if (lower && !self._acResults.some(function(r) { return r.value.toLowerCase() === lower; })) {
                    self._acCreateLabel = query;
                }
                if (lower && self._acResults.length === 1 && self._acResults[0].value.toLowerCase() === lower) {
                    self._acResults = [];
                    self._acCreateLabel = '';
                }
                self._acVisible = self._acResults.length > 0 || !!self._acCreateLabel;
            });
        },

        // ══════════════════════════════════════════════
        // ── Blur / Keyboard ──
        // ══════════════════════════════════════════════
        acOnBlur() {
            var self = this;
            if (self._acBlurTimer) clearTimeout(self._acBlurTimer);
            self._acBlurTimer = setTimeout(function() { self._acVisible = false; self._acMode = 'field'; self._acBlurTimer = null; }, 200);
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
