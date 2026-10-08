<%--

    Cerberus Copyright (C) 2013 - 2026 cerberustesting
    DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.

    This file is part of Cerberus.

    Cerberus is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Cerberus is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Cerberus.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html class="h-full">
    <head>
        <meta name="active-menu" content="execute">
        <meta name="active-submenu" content="RunTests.jsp">
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%@ include file="include/global/dependenciesInclusions.html" %>

        <link rel="stylesheet" type="text/css" href="css/pages/InsightsShared.css?v=${appVersion}"/>
        <link rel="stylesheet" type="text/css" href="css/pages/RunTests.css?v=${appVersion}"/>
        <script type="text/javascript" src="js/pages/RunTest.js?v=${appVersion}"></script>

        <title id="pageTitle">Run Tests</title>
    </head>
    <body x-data x-cloak class="crb_body" :class="$store.rightPanel.open ? 'rp-open' : ''">
        <jsp:include page="include/global/header2.html"/>
        <jsp:include page="include/global/modalInclusions.jsp"/>
        <jsp:include page="include/global/rightPanel.html"/>
        <jsp:include page="include/templates/selectMultipleDropdown.html"/>
        <main class="crb_main_wrp" :class="$store.rightPanel.isResizing ? '' : 'transition-all duration-200'"
              :style="{marginLeft: ($store.sidebar.hidden ? 0 : ($store.sidebar.expanded ? 288 : 80)) + 'px',
                      width: 'calc(100vw - ' + ($store.sidebar.hidden ? 0 : ($store.sidebar.expanded ? 288 : 80))
                          + 'px - '+ ($store.rightPanel.open ? $store.rightPanel.width : 0) + 'px)'}">
            <%@ include file="include/global/messagesArea.html" %>

            <div x-data="runTests()" class="v2in-page" id="runTestsRoot">

                <div class="v2in-pagetitle">
                    <h1 class="page-title-line" x-text="t('title')"></h1>
                </div>

                <!-- Sticky bar: what to run, and the run buttons -->
                <div class="crb_card v2in-card v2in-header" :style="$store.rightPanel.open ? { top: '0px' } : {}">
                    <div class="flex items-center gap-3 flex-wrap">
                        <div class="v2in-seg">
                            <button type="button" id="SelectionManual" class="v2in-seg-item" :class="mode === 'tests' ? 'v2in-seg-item--on' : ''"
                                    @click="setMode('tests')" x-text="t('modetests')"></button>
                            <button type="button" id="SelectionCampaign" class="v2in-seg-item" :class="mode === 'campaign' ? 'v2in-seg-item--on' : ''"
                                    @click="setMode('campaign')" x-text="t('modecampaign')"></button>
                        </div>
                        <div class="v2in-field" x-show="mode === 'campaign'" x-cloak>
                            <select class="v2in-input" id="campaignSelect" style="min-width: 260px" x-model="campaign" @change="loadCampaign()">
                                <option value="" x-text="t('pickcampaign')"></option>
                                <template x-for="c in campaigns" :key="c">
                                    <option :value="c" x-text="c"></option>
                                </template>
                            </select>
                        </div>
                        <span class="v2in-dim text-xs" x-show="mode === 'campaign' && campaign" x-text="t('campaignhint')"></span>
                        <div class="flex-1"></div>
                        <span class="v2in-dim text-xs" x-text="t('summary', testCount, executionCount)"></span>
                        <button type="button" class="v2in-btn" id="runTestCase" @click="run(false)" :disabled="running || loading"
                                x-text="mode === 'campaign' ? t('runcampaign') : t('run')"></button>
                        <button type="button" class="v2in-btn v2in-btn--primary" id="runTestCaseAndSee" @click="run(true)" :disabled="running || loading"
                                x-text="mode === 'campaign' ? t('runcampaignsee') : t('runsee')"></button>
                    </div>
                </div>

                <!-- Test cases -->
                <div class="crb_card v2in-card" id="TestPanel">
                    <div class="v2in-card-head">
                        <span class="v2in-card-title" x-text="mode === 'campaign' ? t('campaigntests') : t('testcases')"></span>
                        <span class="v2in-count" x-text="mode === 'campaign' ? testcases.length : (selectedTestcases.length + ' / ' + testcases.length)"></span>
                        <div class="flex-1"></div>
                        <template x-if="mode === 'tests'">
                            <button type="button" class="v2in-btn v2in-btn--xs" :class="filtersOpen || activeFilters ? 'v2in-btn--on' : ''" @click="openFilters()">
                                <span x-text="t('filters') + (activeFilters ? ' (' + activeFilters + ')' : '')"></span>
                            </button>
                        </template>
                    </div>

                    <!-- Filters, created the first time they are opened (each one loads its own values) -->
                    <template x-if="filtersLoaded">
                        <div class="v2in-card-body rt-filters" x-show="mode === 'tests' && filtersOpen" id="filtersPanel">
                            <div class="rt-filter-grid">
                                <template x-for="f in filterDefs" :key="f.key">
                                    <div class="v2in-field">
                                        <span class="v2in-fieldlabel" x-text="t(f.label)"></span>
                                        <div x-data="multiSelectDropdown({ id: 'rt-' + f.key, labelField: 'label', valueField: 'value', returnType: 'value',
                                                                          placeholder: t('any'), loader: () => filterItems(f) })"></div>
                                    </div>
                                </template>
                            </div>
                            <div class="flex items-center gap-3 mt-3 flex-wrap">
                                <button type="button" class="v2in-btn v2in-btn--primary" id="loadFiltersBtn" @click="loadTestcases()" :disabled="listLoading" x-text="t('search')"></button>
                                <div class="v2in-field">
                                    <select class="v2in-input" x-model.number="resultSize" :title="t('resultsize')">
                                        <option :value="50">50</option>
                                        <option :value="100">100</option>
                                        <option :value="-1">&gt;100</option>
                                    </select>
                                </div>
                            </div>
                        </div>
                    </template>

                    <div class="v2in-card-body">
                        <div class="flex items-center gap-2 flex-wrap mb-2" x-show="mode === 'tests'">
                            <input type="text" class="v2in-input" style="min-width: 260px" x-model="listFilter" :placeholder="t('quickfilter')" spellcheck="false">
                            <button type="button" class="v2in-btn v2in-btn--xs" id="testcaseSelectAll" @click="selectAll(true)" x-text="t('selectall')"></button>
                            <button type="button" class="v2in-btn v2in-btn--xs" id="testcaseSelectNone" @click="selectAll(false)" x-text="t('selectnone')"></button>
                            <span class="v2in-dim text-xs" x-show="listLoading" x-text="t('loading')"></span>
                        </div>
                        <div class="v2in-empty" x-show="!listLoading && visibleTestcases.length === 0" x-text="mode === 'campaign' && !campaign ? t('pickcampaignfirst') : t('notestcase')"></div>
                        <div class="v2in-table-scroll rt-testlist" x-show="visibleTestcases.length > 0">
                            <table class="v2in-table">
                                <thead>
                                    <tr>
                                        <th class="rt-check" x-show="mode === 'tests'"></th>
                                        <th x-text="t('test')"></th><th x-text="t('testcase')"></th><th x-text="t('application')"></th><th x-text="t('description')"></th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <template x-for="tc in visibleTestcases" :key="key(tc)">
                                        <tr :class="mode === 'tests' ? 'v2in-row-click' : ''" @click="mode === 'tests' && (selected[key(tc)] = !selected[key(tc)])">
                                            <td class="rt-check" x-show="mode === 'tests'"><span class="v2in-check" :class="selected[key(tc)] ? 'v2in-check--on' : ''"></span></td>
                                            <td class="v2in-strong" x-text="tc.test"></td>
                                            <td x-text="tc.testcase"></td>
                                            <td x-text="tc.application"></td>
                                            <td class="v2in-dim" x-text="tc.description"></td>
                                        </tr>
                                    </template>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>

                <!-- Created once the static lists are loaded, so that the selects find their options -->
                <template x-if="!loading">
                <div class="v2in-page">
                <div class="rt-grid">
                    <!-- Environments and countries -->
                    <div class="crb_card v2in-card" id="envSettingsBlock">
                        <div class="v2in-card-head">
                            <span class="v2in-card-title" x-text="t('target')"></span>
                            <div class="flex-1"></div>
                            <div class="v2in-seg">
                                <button type="button" class="v2in-seg-item" :class="envMode === 'auto' ? 'v2in-seg-item--on' : ''" @click="envMode = 'auto'" x-text="t('automatic')"></button>
                                <button type="button" class="v2in-seg-item" :class="envMode === 'manual' ? 'v2in-seg-item--on' : ''" @click="envMode = 'manual'" x-text="t('manual')"></button>
                            </div>
                        </div>
                        <div class="v2in-card-body rt-stack">
                            <div class="v2in-field" x-show="envMode === 'auto'">
                                <span class="v2in-fieldlabel" x-text="t('environments')"></span>
                                <div class="flex items-center gap-2 flex-wrap">
                                    <template x-for="e in environments" :key="e.environment">
                                        <button type="button" class="v2in-pill" :class="selEnvironments.includes(e.environment) ? 'v2in-pill--on' : ''"
                                                @click="toggle(selEnvironments, e.environment)" x-text="envLabel(e)"></button>
                                    </template>
                                </div>
                            </div>
                            <div class="rt-form" x-show="envMode === 'manual'" x-cloak>
                                <label class="v2in-field"><span class="v2in-fieldlabel" x-text="t('myhost')"></span>
                                    <input type="text" class="v2in-input" id="myhost" x-model="manual.myhost"></label>
                                <label class="v2in-field"><span class="v2in-fieldlabel" x-text="t('mycontextroot')"></span>
                                    <input type="text" class="v2in-input" id="mycontextroot" x-model="manual.mycontextroot"></label>
                                <label class="v2in-field"><span class="v2in-fieldlabel" x-text="t('myloginrelativeurl')"></span>
                                    <input type="text" class="v2in-input" id="myloginrelativeurl" x-model="manual.myloginrelativeurl"></label>
                                <label class="v2in-field"><span class="v2in-fieldlabel" x-text="t('myenvdata')"></span>
                                    <select class="v2in-input" id="myenvdata" x-model="manual.myenvdata">
                                        <option value=""></option>
                                        <template x-for="e in environments" :key="e.environment">
                                            <option :value="e.environment" x-text="e.environment"></option>
                                        </template>
                                    </select></label>
                            </div>
                            <div class="v2in-field" id="countrySettingsBlock">
                                <span class="v2in-fieldlabel" x-text="t('countries')"></span>
                                <div class="flex items-center gap-2 flex-wrap">
                                    <template x-for="c in countries" :key="c">
                                        <button type="button" class="v2in-pill" :class="selCountries.includes(c) ? 'v2in-pill--on' : ''"
                                                @click="toggle(selCountries, c)" x-text="c"></button>
                                    </template>
                                    <button type="button" class="v2in-btn v2in-btn--xs" id="countrySelectAll" @click="selCountries = countries.slice()" x-text="t('selectall')"></button>
                                    <button type="button" class="v2in-btn v2in-btn--xs" id="countrySelectNone" @click="selCountries = []" x-text="t('selectnone')"></button>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Robot -->
                    <div class="crb_card v2in-card" id="RobotPanel">
                        <div class="v2in-card-head">
                            <span class="v2in-card-title" x-text="t('robot')"></span>
                            <div class="flex-1"></div>
                            <button type="button" class="v2in-btn v2in-btn--xs" id="robotEdit" x-show="namedRobot" @click="openRobotModal('EDIT')" x-text="t('editrobot')"></button>
                            <button type="button" class="v2in-btn v2in-btn--xs" id="robotCreate" @click="openRobotModal('ADD')" x-text="t('newrobot')"></button>
                        </div>
                        <div class="v2in-card-body rt-stack" id="robotSettings">
                            <div class="flex items-center gap-2 flex-wrap">
                                <button type="button" class="v2in-pill" :class="isCustomRobot ? 'v2in-pill--on' : ''" @click="toggleRobot('CustomConfiguration')" x-text="t('customconfig')"></button>
                                <template x-for="r in robots" :key="r">
                                    <button type="button" class="v2in-pill" :class="selRobots.includes(r) ? 'v2in-pill--on' : ''" @click="toggleRobot(r)" x-text="r"></button>
                                </template>
                            </div>
                            <div class="rt-form" x-show="isCustomRobot" x-cloak>
                                <label class="v2in-field"><span class="v2in-fieldlabel" x-text="t('seleniumip')"></span>
                                    <input type="text" class="v2in-input" id="seleniumIP" x-model="custom.ss_ip"></label>
                                <label class="v2in-field"><span class="v2in-fieldlabel" x-text="t('seleniumport')"></span>
                                    <input type="text" class="v2in-input" id="seleniumPort" x-model="custom.ss_p"></label>
                                <label class="v2in-field"><span class="v2in-fieldlabel" x-text="t('browser')"></span>
                                    <select class="v2in-input" id="browser" x-model="custom.browser">
                                        <option value=""></option>
                                        <template x-for="b in options('BROWSER')" :key="b.value">
                                            <option :value="b.value" x-text="b.value + ' - ' + b.description"></option>
                                        </template>
                                    </select></label>
                            </div>
                            <div class="rt-form" x-show="namedRobot" x-cloak>
                                <div class="v2in-field"><span class="v2in-fieldlabel" x-text="t('seleniumip')"></span><span x-text="robotInfo.host || '-'"></span></div>
                                <div class="v2in-field"><span class="v2in-fieldlabel" x-text="t('seleniumport')"></span><span x-text="robotInfo.port || '-'"></span></div>
                                <div class="v2in-field"><span class="v2in-fieldlabel" x-text="t('browser')"></span><span x-text="robotInfo.browser || '-'"></span></div>
                            </div>
                            <span class="v2in-dim text-xs" x-show="selRobots.length > 1" x-text="t('multirobot', selRobots.length)"></span>
                        </div>
                    </div>

                    <!-- Execution parameters -->
                <div class="crb_card v2in-card" id="executionPanel">
                    <div class="v2in-card-head">
                        <span class="v2in-card-title" x-text="t('execution')"></span>
                        <div class="flex-1"></div>
                        <span class="v2in-chip v2in-chip--ok" x-show="prefsSaved" x-cloak x-text="t('prefssaved')"></span>
                        <button type="button" class="v2in-btn v2in-btn--xs" id="saveExecutionParams" @click="savePreferences()" x-text="t('saveprefs')"></button>
                        <button type="button" class="v2in-btn v2in-btn--xs" @click="resetPreferences()" x-text="t('resetprefs')"></button>
                    </div>
                    <div class="v2in-card-body" id="executionSettings">
                        <div class="rt-form rt-form--wide">
                            <label class="v2in-field"><span class="v2in-fieldlabel" x-text="t('tag')"></span>
                                <input type="text" class="v2in-input" id="tag" maxlength="255" x-model="exec.tag"></label>
                            <template x-for="f in execFields" :key="f.key">
                                <label class="v2in-field"><span class="v2in-fieldlabel" x-text="t(f.label)"></span>
                                    <select class="v2in-input" :id="f.key" x-model="exec[f.key]">
                                        <option value=""></option>
                                        <template x-for="o in options(f.invariant)" :key="o.value">
                                            <option :value="o.value" x-text="o.value + ' - ' + o.description"></option>
                                        </template>
                                    </select></label>
                            </template>
                            <label class="v2in-field"><span class="v2in-fieldlabel" x-text="t('timeout')"></span>
                                <input type="text" class="v2in-input" id="timeout" x-model="exec.timeout"></label>
                            <label class="v2in-field"><span class="v2in-fieldlabel" x-text="t('priority')"></span>
                                <input type="text" class="v2in-input" id="priority" x-model="exec.priority"></label>
                        </div>
                    </div>
                </div>
                </div>

                </div>
                </template>

            </div>

            <footer class="footer">
                <div class="container-fluid" id="footer"></div>
            </footer>
        </main>
    </body>
</html>
