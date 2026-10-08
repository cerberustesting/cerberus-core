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
                        <div class="rt-seg">
                            <button type="button" id="SelectionManual" class="rt-seg-item" :class="mode === 'tests' ? 'rt-seg-item--on' : ''" @click="setMode('tests')">
                                <i data-lucide="list-checks" class="w-4 h-4"></i><span x-text="t('modetests')"></span>
                            </button>
                            <button type="button" id="SelectionCampaign" class="rt-seg-item" :class="mode === 'campaign' ? 'rt-seg-item--on' : ''" @click="setMode('campaign')">
                                <i data-lucide="tag" class="w-4 h-4"></i><span x-text="t('modecampaign')"></span>
                            </button>
                        </div>
                        <div x-show="mode === 'campaign'" x-cloak>
                            <select class="rt-input" id="campaignSelect" style="min-width: 260px" x-model="campaign" @change="loadCampaign()">
                                <option value="" x-text="t('pickcampaign')"></option>
                                <template x-for="c in campaigns" :key="c">
                                    <option :value="c" x-text="c"></option>
                                </template>
                            </select>
                        </div>
                        <span class="text-xs text-slate-500 dark:text-slate-400" x-show="mode === 'campaign' && campaign" x-text="t('campaignhint')"></span>
                        <div class="flex-1"></div>
                        <span class="rt-summary" x-text="t('summary', testCount, executionCount)"></span>
                        <button type="button" class="rt-btn" id="runTestCase" @click="run(false)" :disabled="running || loading">
                            <i data-lucide="play" class="w-3.5 h-3.5"></i>
                            <span x-text="mode === 'campaign' ? t('runcampaign') : t('run')"></span>
                        </button>
                        <button type="button" class="rt-btn rt-btn--primary" id="runTestCaseAndSee" @click="run(true)" :disabled="running || loading">
                            <i data-lucide="external-link" class="w-3.5 h-3.5"></i>
                            <span x-text="mode === 'campaign' ? t('runcampaignsee') : t('runsee')"></span>
                        </button>
                    </div>
                </div>

                <!-- Test cases -->
                <div class="crb_card v2in-card" id="TestPanel">
                    <div class="v2in-card-body rt-stack">
                        <div class="rt-head">
                            <div class="rt-tile">
                                <span x-show="mode === 'tests'"><i data-lucide="list-checks" class="text-blue-500 w-5 h-5"></i></span>
                                <span x-show="mode === 'campaign'" x-cloak><i data-lucide="tag" class="text-blue-500 w-5 h-5"></i></span>
                            </div>
                            <h4 class="rt-title" x-text="mode === 'campaign' ? t('campaigntests') : t('testcases')"></h4>
                            <span class="rt-badge rt-badge--blue" x-text="mode === 'campaign' ? testcases.length : (selectedTestcases.length + ' / ' + testcases.length)"></span>
                            <div class="flex-1"></div>
                            <template x-if="mode === 'tests'">
                                <button type="button" class="rt-btn rt-btn--sm" :class="filtersOpen || activeFilters ? 'rt-btn--on' : ''" @click="openFilters()">
                                    <i data-lucide="filter" class="w-3.5 h-3.5"></i>
                                    <span x-text="t('filters') + (activeFilters ? ' (' + activeFilters + ')' : '')"></span>
                                </button>
                            </template>
                        </div>

                        <!-- Filters, created the first time they are opened (each one loads its own values) -->
                        <template x-if="filtersLoaded">
                            <div class="rt-filters" x-show="mode === 'tests' && filtersOpen" id="filtersPanel">
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
                                    <button type="button" class="rt-btn rt-btn--primary" id="loadFiltersBtn" @click="loadTestcases()" :disabled="listLoading">
                                        <i data-lucide="search" class="w-3.5 h-3.5"></i><span x-text="t('search')"></span>
                                    </button>
                                    <select class="rt-input" x-model.number="resultSize" :title="t('resultsize')">
                                        <option :value="50">50</option>
                                        <option :value="100">100</option>
                                        <option :value="-1">&gt;100</option>
                                    </select>
                                </div>
                            </div>
                        </template>

                        <div class="flex items-center gap-2 flex-wrap" x-show="mode === 'tests'">
                            <input type="text" class="rt-input" style="min-width: 280px" x-model="listFilter" :placeholder="t('quickfilter')" spellcheck="false">
                            <button type="button" class="rt-btn rt-btn--sm" id="testcaseSelectAll" @click="selectAll(true)">
                                <i data-lucide="check-check" class="w-3.5 h-3.5"></i><span x-text="t('selectall')"></span>
                            </button>
                            <button type="button" class="rt-btn rt-btn--sm" id="testcaseSelectNone" @click="selectAll(false)">
                                <i data-lucide="square" class="w-3.5 h-3.5"></i><span x-text="t('selectnone')"></span>
                            </button>
                            <span class="text-xs text-slate-500 dark:text-slate-400" x-show="listLoading" x-text="t('loading')"></span>
                        </div>
                        <div class="v2in-empty" x-show="!listLoading && visibleTestcases.length === 0" x-text="mode === 'campaign' && !campaign ? t('pickcampaignfirst') : t('notestcase')"></div>
                        <div class="rt-testlist" x-show="visibleTestcases.length > 0">
                            <template x-for="tc in visibleTestcases" :key="key(tc)">
                                <div class="rt-item rt-item--row" :class="[selected[key(tc)] ? 'rt-item--on' : '', mode === 'tests' ? '' : 'rt-item--static']"
                                     @click="mode === 'tests' && (selected[key(tc)] = !selected[key(tc)])">
                                    <span class="v2in-check" x-show="mode === 'tests'" :class="selected[key(tc)] ? 'v2in-check--on' : ''"></span>
                                    <div class="min-w-0 flex-1">
                                        <div class="rt-item-title truncate"><span x-text="tc.test"></span> / <span x-text="tc.testcase"></span></div>
                                        <div class="rt-item-sub truncate" x-text="tc.description"></div>
                                    </div>
                                    <span class="rt-badge rt-badge--blue" x-text="tc.application"></span>
                                </div>
                            </template>
                        </div>
                    </div>
                </div>

                <!-- Created once the static lists are loaded, so that the selects find their options -->
                <template x-if="!loading">
                <div class="rt-grid">

                    <!-- Environments and countries -->
                    <div class="crb_card v2in-card" id="envSettingsBlock">
                        <div class="v2in-card-body rt-stack">
                            <div class="rt-head">
                                <div class="rt-tile"><i data-lucide="earth" class="text-green-500 w-5 h-5"></i></div>
                                <h4 class="rt-title" x-text="t('target')"></h4>
                                <div class="flex-1"></div>
                                <div class="rt-seg rt-seg--sm">
                                    <button type="button" class="rt-seg-item" :class="envMode === 'auto' ? 'rt-seg-item--on' : ''" @click="envMode = 'auto'" x-text="t('automatic')"></button>
                                    <button type="button" class="rt-seg-item" :class="envMode === 'manual' ? 'rt-seg-item--on' : ''" @click="envMode = 'manual'" x-text="t('manual')"></button>
                                </div>
                            </div>

                            <template x-if="envMode === 'auto'">
                                <div class="rt-stack">
                                    <input type="text" class="rt-input" x-model="envFilter" :placeholder="t('filterenvs')" spellcheck="false">
                                    <div class="rt-list">
                                        <template x-for="e in filteredEnvironments" :key="e.environment">
                                            <button type="button" class="rt-item" :class="selEnvironments.includes(e.environment) ? 'rt-item--on' : ''" @click="toggle(selEnvironments, e.environment)">
                                                <div class="min-w-0 flex-1 text-left">
                                                    <div class="rt-item-title truncate" x-text="e.environment"></div>
                                                    <div class="rt-item-sub" x-show="e.active === false" x-text="t('disabled')"></div>
                                                </div>
                                                <span class="rt-badge rt-badge--green" x-text="e.environment"></span>
                                            </button>
                                        </template>
                                    </div>
                                </div>
                            </template>

                            <div class="rt-form" x-show="envMode === 'manual'" x-cloak>
                                <label class="v2in-field"><span class="v2in-fieldlabel" x-text="t('myhost')"></span>
                                    <input type="text" class="rt-input" id="myhost" x-model="manual.myhost"></label>
                                <label class="v2in-field"><span class="v2in-fieldlabel" x-text="t('mycontextroot')"></span>
                                    <input type="text" class="rt-input" id="mycontextroot" x-model="manual.mycontextroot"></label>
                                <label class="v2in-field"><span class="v2in-fieldlabel" x-text="t('myloginrelativeurl')"></span>
                                    <input type="text" class="rt-input" id="myloginrelativeurl" x-model="manual.myloginrelativeurl"></label>
                                <label class="v2in-field"><span class="v2in-fieldlabel" x-text="t('myenvdata')"></span>
                                    <select class="rt-input" id="myenvdata" x-model="manual.myenvdata">
                                        <option value=""></option>
                                        <template x-for="e in environments" :key="e.environment">
                                            <option :value="e.environment" x-text="e.environment"></option>
                                        </template>
                                    </select></label>
                            </div>

                            <div class="rt-stack" id="countrySettingsBlock">
                                <div class="rt-subhead">
                                    <i data-lucide="flag" class="w-4 h-4"></i>
                                    <span x-text="t('countries')"></span>
                                    <div class="flex-1"></div>
                                    <button type="button" class="rt-link" id="countrySelectAll" @click="selCountries = countries.slice()" x-text="t('selectall')"></button>
                                    <button type="button" class="rt-link" id="countrySelectNone" @click="selCountries = []" x-text="t('selectnone')"></button>
                                </div>
                                <div class="flex items-center gap-2 flex-wrap">
                                    <template x-for="c in countries" :key="c">
                                        <button type="button" class="rt-chip" :class="selCountries.includes(c) ? 'rt-chip--on' : ''" @click="toggle(selCountries, c)" x-text="c"></button>
                                    </template>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Robot -->
                    <div class="crb_card v2in-card" id="RobotPanel">
                        <div class="v2in-card-body rt-stack" id="robotSettings">
                            <div class="rt-head">
                                <div class="rt-tile"><i data-lucide="bot" class="text-blue-500 w-5 h-5"></i></div>
                                <h4 class="rt-title" x-text="t('robot')"></h4>
                                <div class="flex-1"></div>
                                <button type="button" class="rt-iconbtn" id="robotEdit" x-show="namedRobot" @click="openRobotModal('EDIT')" :title="t('editrobot')">
                                    <i data-lucide="pencil" class="w-4 h-4"></i>
                                </button>
                                <button type="button" class="rt-iconbtn" id="robotCreate" @click="openRobotModal('ADD')" :title="t('newrobot')">
                                    <i data-lucide="plus" class="w-4 h-4"></i>
                                </button>
                            </div>
                            <input type="text" class="rt-input" x-model="robotFilter" :placeholder="t('filterrobots')" spellcheck="false">
                            <div class="rt-list">
                                <button type="button" class="rt-item" :class="isCustomRobot ? 'rt-item--on' : ''" @click="toggleRobot('CustomConfiguration')">
                                    <div class="rt-robot-icons"><i data-lucide="settings-2" class="w-5 h-5 text-slate-500"></i></div>
                                    <div class="min-w-0 flex-1 text-left">
                                        <div class="rt-item-title" x-text="t('customconfig')"></div>
                                    </div>
                                </button>
                                <template x-for="r in filteredRobots" :key="r.robot">
                                    <button type="button" class="rt-item" :class="selRobots.includes(r.robot) ? 'rt-item--on' : ''" @click="toggleRobot(r.robot)">
                                        <div class="rt-robot-icons">
                                            <img :src="'images/platform-' + r.platform + '.png'" :title="r.platform" onerror="this.style.display='none'">
                                            <img :src="'images/browser-' + r.browser + '.png'" :title="r.browser" onerror="this.style.display='none'">
                                        </div>
                                        <div class="min-w-0 flex-1 text-left">
                                            <div class="rt-item-title truncate" x-text="r.robot"></div>
                                            <div class="rt-item-sub flex items-center gap-2">
                                                <span class="rt-badge" :class="r.active ? 'rt-badge--green' : 'rt-badge--red'" x-text="r.active ? 'ACTIVE' : 'INACTIVE'"></span>
                                                <span x-text="t('executors', r.activeExecutorsCount)"></span>
                                            </div>
                                        </div>
                                    </button>
                                </template>
                            </div>
                            <div class="rt-form" x-show="isCustomRobot" x-cloak>
                                <label class="v2in-field"><span class="v2in-fieldlabel" x-text="t('seleniumip')"></span>
                                    <input type="text" class="rt-input" id="seleniumIP" x-model="custom.ss_ip"></label>
                                <label class="v2in-field"><span class="v2in-fieldlabel" x-text="t('seleniumport')"></span>
                                    <input type="text" class="rt-input" id="seleniumPort" x-model="custom.ss_p"></label>
                                <label class="v2in-field"><span class="v2in-fieldlabel" x-text="t('browser')"></span>
                                    <select class="rt-input" id="browser" x-model="custom.browser">
                                        <option value=""></option>
                                        <template x-for="b in options('BROWSER')" :key="b.value">
                                            <option :value="b.value" x-text="b.value + ' - ' + b.description"></option>
                                        </template>
                                    </select></label>
                            </div>
                            <div class="rt-form" x-show="namedRobot" x-cloak>
                                <div class="v2in-field"><span class="v2in-fieldlabel" x-text="t('seleniumip')"></span><span class="text-sm" x-text="robotInfo.host || '-'"></span></div>
                                <div class="v2in-field"><span class="v2in-fieldlabel" x-text="t('seleniumport')"></span><span class="text-sm" x-text="robotInfo.port || '-'"></span></div>
                                <div class="v2in-field"><span class="v2in-fieldlabel" x-text="t('browser')"></span><span class="text-sm" x-text="robotInfo.browser || '-'"></span></div>
                            </div>
                            <span class="text-xs text-slate-500 dark:text-slate-400" x-show="selRobots.length > 1" x-text="t('multirobot', selRobots.length)"></span>
                        </div>
                    </div>

                    <!-- Execution parameters -->
                    <div class="crb_card v2in-card" id="executionPanel">
                        <div class="v2in-card-body rt-stack" id="executionSettings">
                            <div class="rt-head">
                                <div class="rt-tile"><i data-lucide="sliders" class="text-orange-500 w-5 h-5"></i></div>
                                <h4 class="rt-title" x-text="t('execution')"></h4>
                                <div class="flex-1"></div>
                                <span class="rt-badge rt-badge--green" x-show="prefsSaved" x-cloak x-text="t('prefssaved')"></span>
                                <button type="button" class="rt-iconbtn rt-iconbtn--ok" id="saveExecutionParams" @click="savePreferences()" :title="t('saveprefs')">
                                    <i data-lucide="save" class="w-4 h-4"></i>
                                </button>
                                <button type="button" class="rt-iconbtn" @click="resetPreferences()" :title="t('resetprefs')">
                                    <i data-lucide="rotate-ccw" class="w-4 h-4"></i>
                                </button>
                            </div>

                            <div class="rt-row">
                                <div class="rt-row-icon"><i data-lucide="tag" class="w-4 h-4"></i></div>
                                <input type="text" class="rt-input flex-1" id="tag" maxlength="255" x-model="exec.tag" :placeholder="t('tag')" :title="t('tag')">
                            </div>
                            <template x-for="f in execFields" :key="f.key">
                                <div class="rt-row">
                                    <div class="rt-row-icon"><i :data-lucide="f.icon" class="w-4 h-4"></i></div>
                                    <select class="rt-input flex-1" :id="f.key" x-model="exec[f.key]" :title="t(f.label)">
                                        <option value="" x-text="t(f.label)"></option>
                                        <template x-for="o in options(f.invariant)" :key="o.value">
                                            <option :value="o.value" x-text="o.value + ' - ' + o.description"></option>
                                        </template>
                                    </select>
                                </div>
                            </template>
                            <div class="rt-row">
                                <div class="rt-row-icon"><i data-lucide="clock" class="w-4 h-4"></i></div>
                                <input type="number" class="rt-input flex-1" id="timeout" x-model="exec.timeout" :placeholder="t('timeouthint')" :title="t('timeout')">
                            </div>
                            <div class="rt-row">
                                <div class="rt-row-icon"><i data-lucide="arrow-up" class="w-4 h-4"></i></div>
                                <input type="number" class="rt-input flex-1" id="priority" x-model="exec.priority" :placeholder="t('priorityhint')" :title="t('priority')">
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
