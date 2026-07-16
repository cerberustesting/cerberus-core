<%--

    Cerberus Copyright (C) 2013 - 2025 cerberustesting
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
        <meta name="active-menu" content="insights">
        <meta name="active-submenu" content="ReportingExecutionOverTime.jsp">
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%@ include file="include/global/dependenciesInclusions.html" %>

        <script type="text/javascript" src="js/pages/insightsShared.js?v=${appVersion}"></script>
        <script type="text/javascript" src="js/pages/ReportingExecutionOverTime.js?v=${appVersion}"></script>
        <link rel="stylesheet" type="text/css" href="css/pages/InsightsShared.css?v=${appVersion}"/>

        <title id="pageTitle">Execution Trends</title>
    </head>
    <body x-data x-cloak class="crb_body" :class="$store.rightPanel.open ? 'rp-open' : ''">
        <jsp:include page="include/global/header2.html"/>
        <jsp:include page="include/global/modalInclusions.jsp"/>
        <jsp:include page="include/global/rightPanel.html"/>
        <main class="crb_main_wrp" :class="$store.rightPanel.isResizing ? '' : 'transition-all duration-200'"
              :style="{marginLeft: ($store.sidebar.hidden ? 0 : ($store.sidebar.expanded ? 288 : 80)) + 'px',
                      width: 'calc(100vw - ' + ($store.sidebar.hidden ? 0 : ($store.sidebar.expanded ? 288 : 80))
                          + 'px - '+ ($store.rightPanel.open ? $store.rightPanel.width : 0) + 'px)'}">
            <%@ include file="include/global/messagesArea.html" %>

            <div x-data="executionTrends()" x-init="init()" class="v2in-page" id="executionTrendsRoot">

                <!-- Page title above the bar, like every list page -->
                <div class="v2in-pagetitle">
                    <h1 class="page-title-line">Execution Trends</h1>
                </div>

                <!-- Sticky header: test case picker + period -->
                <div class="crb_card v2in-card v2in-header" :style="$store.rightPanel.open ? { top: '0px' } : {}">
                    <div class="flex items-center gap-3 flex-wrap">

                        <!-- Test case picker: browse folder then tick test cases -->
                        <div class="v2in-field relative" @click.outside="tcDdOpen = false">
                            <span class="v2in-fieldlabel">Test cases to follow</span>
                            <button type="button" class="v2in-picker" style="min-width: 300px; max-width: 460px"
                                    :class="[tcDdOpen ? 'v2in-picker--active' : '', selTestcases.length === 0 ? 'v2in-picker--empty' : '']"
                                    :title="selTestcases.length ? selTestcases.map(tc => tc.test + ' / ' + tc.testCase).join('\n') : 'Browse the test folders and tick the test cases to follow'"
                                    @click="tcDdOpen = !tcDdOpen; if (tcDdOpen) { tcSearch = ''; $nextTick(() => $refs.tcSearchInput && $refs.tcSearchInput.focus()) }">
                                <svg class="w-4 h-4 v2in-picker-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path d="M22 19a2 2 0 01-2 2H4a2 2 0 01-2-2V5a2 2 0 012-2h5l2 3h9a2 2 0 012 2z"/></svg>
                                <span class="v2in-picker-value"
                                      x-text="selTestcases.length === 0 ? 'Choose test cases...' : selTestcases.slice(0, 2).map(tc => tc.testCase).join(', ') + (selTestcases.length > 2 ? ' +' + (selTestcases.length - 2) : '')"></span>
                                <span class="flex-1"></span>
                                <span class="v2in-picker-count" x-show="selTestcases.length" x-text="selTestcases.length"></span>
                                <svg class="w-3.5 h-3.5 v2in-picker-chevron" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path d="M19 9l-7 7-7-7"/></svg>
                            </button>
                            <div x-show="tcDdOpen" x-cloak class="v2in-dd v2in-dd--right" style="min-width: 340px">
                                <div class="v2in-dd-search">
                                    <button type="button" class="v2in-btn v2in-btn--xs" x-show="browsedTest" @click="browsedTest = ''; testcasesOfTest = []; tcSearch = ''" title="Back to the folders">
                                        <svg class="w-3 h-3" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path d="M19 12H5M12 19l-7-7 7-7"/></svg>
                                    </button>
                                    <input type="text" x-ref="tcSearchInput" x-model="tcSearch"
                                           :placeholder="browsedTest ? 'Search test case...' : 'Search test folder...'" class="v2in-input">
                                </div>
                                <div class="v2in-dd-list">
                                    <!-- Folder level -->
                                    <template x-if="!browsedTest">
                                        <div>
                                            <template x-for="t in filteredTests" :key="t">
                                                <button type="button" class="v2in-dd-item" @click="browseTest(t); tcSearch = ''">
                                                    <svg class="w-3.5 h-3.5 shrink-0" style="color: var(--crb-grey-color)" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path d="M22 19a2 2 0 01-2 2H4a2 2 0 01-2-2V5a2 2 0 012-2h5l2 3h9a2 2 0 012 2z"/></svg>
                                                    <span class="truncate" x-text="t"></span>
                                                    <span class="flex-1"></span>
                                                    <svg class="w-3 h-3 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path d="M9 5l7 7-7 7"/></svg>
                                                </button>
                                            </template>
                                            <div x-show="filteredTests.length === 0" class="v2in-empty px-3 py-2 text-center">No test folder matches</div>
                                        </div>
                                    </template>
                                    <!-- Test case level -->
                                    <template x-if="browsedTest">
                                        <div>
                                            <div class="v2in-dim text-xs px-3 py-1 font-semibold truncate" x-text="browsedTest"></div>
                                            <template x-for="tc in filteredTestcases" :key="tc.testCase">
                                                <button type="button" class="v2in-dd-item" @click="toggleTestcase(tc)">
                                                    <span class="v2in-check" :class="isSelected(tc) ? 'v2in-check--on' : ''"></span>
                                                    <span class="v2in-strong" x-text="tc.testCase"></span>
                                                    <span class="v2in-dim truncate text-xs" x-text="tc.description"></span>
                                                </button>
                                            </template>
                                            <div x-show="filteredTestcases.length === 0" class="v2in-empty px-3 py-2 text-center">No test case matches</div>
                                        </div>
                                    </template>
                                </div>
                            </div>
                        </div>

                        <!-- Period -->
                        <div class="v2in-field">
                            <span class="v2in-fieldlabel">Period</span>
                            <div class="v2in-seg" style="min-height: 42px; align-items: center" title="Analysis period">
                                <button type="button" class="v2in-seg-item" :class="periodDays === 7 ? 'v2in-seg-item--on' : ''" @click="periodDays = 7">7d</button>
                                <button type="button" class="v2in-seg-item" :class="periodDays === 30 ? 'v2in-seg-item--on' : ''" @click="periodDays = 30">30d</button>
                                <button type="button" class="v2in-seg-item" :class="periodDays === 90 ? 'v2in-seg-item--on' : ''" @click="periodDays = 90">90d</button>
                                <button type="button" class="v2in-seg-item" :class="periodDays === 180 ? 'v2in-seg-item--on' : ''" @click="periodDays = 180">180d</button>
                            </div>
                        </div>

                        <div class="flex-1"></div>

                        <div class="v2in-field">
                            <span class="v2in-fieldlabel">&nbsp;</span>
                            <button type="button" class="v2in-btn v2in-btn--primary" style="min-height: 42px"
                                    :class="selTestcases.length ? '' : 'v2in-btn--disabled'" @click="load()">
                                <svg class="w-3.5 h-3.5" :class="loading ? 'animate-spin' : ''" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path d="M3 12a9 9 0 019-9 9.75 9.75 0 016.74 2.74L21 8"/><path d="M21 3v5h-5"/><path d="M21 12a9 9 0 01-9 9 9.75 9.75 0 01-6.74-2.74L3 16"/><path d="M3 21v-5h5"/></svg>
                                Load
                            </button>
                        </div>
                    </div>

                    <!-- Selected test cases + secondary filters -->
                    <div class="flex items-center gap-2 flex-wrap mt-3" x-show="selTestcases.length > 0">
                        <template x-for="(tc, i) in selTestcases" :key="tc.test + '|' + tc.testCase">
                            <span class="v2in-chip v2in-chip--info" :title="tc.test + ' / ' + tc.testCase">
                                <span x-text="tc.testCase"></span>
                                <button type="button" class="v2in-chip-x" @click="removeSelected(i)" title="Remove">&times;</button>
                            </span>
                        </template>
                    </div>
                    <div class="flex items-center gap-2 flex-wrap mt-2" x-show="loaded" x-cloak>
                        <template x-for="grp in [
                                { label: 'Env', map: activeEnvs },
                                { label: 'Country', map: activeCountries },
                                { label: 'Robot', map: activeRobots },
                                { label: 'Status', map: activeStatuses }
                            ]" :key="grp.label">
                            <div class="flex items-center gap-1.5 flex-wrap" x-show="Object.keys(grp.map).length > 1">
                                <span class="v2in-dim text-xs font-semibold" x-text="grp.label"></span>
                                <template x-for="k in Object.keys(grp.map).sort()" :key="grp.label + '-' + k">
                                    <button type="button" class="v2in-pill" :class="grp.map[k] ? 'v2in-pill--on' : ''"
                                            @click="toggleIn(grp.map, k)" x-text="k"></button>
                                </template>
                            </div>
                        </template>
                    </div>
                </div>

                <!-- Empty / error / loading -->
                <template x-if="!loaded && !loading && !error">
                    <div class="crb_card v2in-card v2in-hero">
                        <div class="v2in-hero-icon">
                            <svg class="w-7 h-7" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path d="M3 3v18h18"/><path d="M7 14l4-4 4 4 5-6"/></svg>
                        </div>
                        <div class="v2in-hero-title">Follow your test cases over time</div>
                        <div class="v2in-hero-sub">
                            Watch how a handful of test cases behave across days or months: every execution with its duration
                            and status, the trend of runs per day, and the flaky ones that need attention. Ideal to check that a
                            fix really stabilised a test.
                        </div>
                        <div class="v2in-hero-steps">
                            <div class="v2in-hero-step"><span class="v2in-hero-stepnum">1</span><span>Open <b>Test cases to follow</b> and browse a test folder</span></div>
                            <div class="v2in-hero-step"><span class="v2in-hero-stepnum">2</span><span>Tick one or several test cases (mixing folders is fine)</span></div>
                            <div class="v2in-hero-step"><span class="v2in-hero-stepnum">3</span><span>Choose the period and press <b>Load</b></span></div>
                        </div>
                        <div class="v2in-hero-cta">
                            <button type="button" class="v2in-btn v2in-btn--primary"
                                    @click="tcDdOpen = true; tcSearch = ''; window.scrollTo({top: 0, behavior: 'smooth'}); $nextTick(() => $refs.tcSearchInput && $refs.tcSearchInput.focus())">
                                <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path d="M22 19a2 2 0 01-2 2H4a2 2 0 01-2-2V5a2 2 0 012-2h5l2 3h9a2 2 0 012 2z"/></svg>
                                Choose test cases
                            </button>
                        </div>
                    </div>
                </template>
                <template x-if="error">
                    <div class="crb_card v2in-card">
                        <div class="v2in-card-body text-center py-10">
                            <div class="text-sm font-semibold mb-1" x-text="error"></div>
                            <div class="text-xs v2in-dim">Adjust the selection above and reload</div>
                        </div>
                    </div>
                </template>
                <template x-if="loading && !loaded">
                    <div class="crb_card v2in-card">
                        <div class="v2in-card-body text-center py-14">
                            <svg class="w-6 h-6 animate-spin mx-auto mb-2" style="color: var(--crb-blue-color)" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path d="M21 12a9 9 0 11-6.22-8.56"/></svg>
                            <div class="text-xs v2in-dim">Loading execution trends...</div>
                        </div>
                    </div>
                </template>

                <template x-if="loaded && !error">
                    <div class="v2in-page">

                        <!-- KPIs -->
                        <div class="v2in-kpis">
                            <div class="crb_card v2in-card v2in-kpi">
                                <span class="v2in-kpi-label">Executions</span>
                                <span class="v2in-kpi-value" x-text="kpis.total"></span>
                                <span class="v2in-kpi-sub" x-text="'over the last ' + periodDays + ' days'"></span>
                            </div>
                            <div class="crb_card v2in-card v2in-kpi">
                                <span class="v2in-kpi-label">OK rate</span>
                                <span class="v2in-kpi-value" :style="kpis.okRate !== null ? ('color:' + (kpis.okRate >= 90 ? 'var(--crb-green-color)' : (kpis.okRate >= 60 ? 'var(--crb-orange-color)' : 'var(--crb-red-color)'))) : ''"
                                      x-text="kpis.okRate !== null ? kpis.okRate + '%' : '-'"></span>
                                <span class="v2in-kpi-sub">of the charted executions</span>
                            </div>
                            <div class="crb_card v2in-card v2in-kpi">
                                <span class="v2in-kpi-label">Avg duration</span>
                                <span class="v2in-kpi-value" x-text="kpis.avgDur !== null ? fmtDuration(kpis.avgDur) : '-'"></span>
                                <span class="v2in-kpi-sub">per execution</span>
                            </div>
                            <div class="crb_card v2in-card v2in-kpi">
                                <span class="v2in-kpi-label">Max duration</span>
                                <span class="v2in-kpi-value" x-text="kpis.maxDur !== null ? fmtDuration(kpis.maxDur) : '-'"></span>
                                <span class="v2in-kpi-sub">worst execution of the period</span>
                            </div>
                            <div class="crb_card v2in-card v2in-kpi">
                                <span class="v2in-kpi-label">False negative</span>
                                <span class="v2in-kpi-value" :style="kpis.fn > 0 ? 'color: #b45309' : ''" x-text="kpis.fn"></span>
                                <span class="v2in-kpi-sub">declared on the period</span>
                            </div>
                        </div>

                        <!-- Duration over time -->
                        <div class="crb_card v2in-card">
                            <div class="v2in-card-head">
                                <span class="v2in-card-title">Duration over time</span>
                                <span class="flex-1"></span>
                                <span class="v2in-dim text-xs">dots are colored by execution status - click to open</span>
                            </div>
                            <div class="v2in-card-body">
                                <div x-html="durationSvg" @click="chartClick($event)"></div>
                                <div class="v2in-legend mt-2" x-show="durationSeries.length > 1">
                                    <template x-for="s in durationSeries" :key="s.id">
                                        <span class="v2in-legend-item"><span class="v2in-legend-dot" :style="'background:' + s.color"></span><span x-text="s.name"></span></span>
                                    </template>
                                </div>
                                <div class="v2in-empty py-4 text-center" x-show="durationSeries.length === 0">No execution on this period</div>
                            </div>
                        </div>

                        <!-- Executions per day -->
                        <div class="crb_card v2in-card">
                            <div class="v2in-card-head">
                                <span class="v2in-card-title">Executions per day</span>
                                <span class="flex-1"></span>
                                <span class="v2in-legend">
                                    <template x-for="s in legendStatuses" :key="s">
                                        <span class="v2in-legend-item"><span class="v2in-legend-dot" :style="'background:' + statusColor(s)"></span><span x-text="s"></span></span>
                                    </template>
                                </span>
                            </div>
                            <div class="v2in-card-body" x-html="perDaySvg"></div>
                        </div>

                        <!-- Executions list -->
                        <div class="crb_card v2in-card">
                            <div class="v2in-card-head">
                                <span class="v2in-card-title">Executions</span>
                                <span class="v2in-count" x-text="executions.length"></span>
                                <span class="flex-1"></span>
                                <span class="v2in-dim text-xs">click an execution to open it</span>
                            </div>
                            <div class="v2in-table-scroll">
                                <table class="v2in-table">
                                    <thead>
                                        <tr>
                                            <th>Date</th>
                                            <th>Test case</th>
                                            <th>Env / Country</th>
                                            <th class="v2in-num">Status</th>
                                            <th class="v2in-num">Duration</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <template x-for="e in executions" :key="e.exeId">
                                            <tr class="v2in-row-click" @click="openExe(e.exeId)">
                                                <td class="v2in-dim" x-text="fmtDateTime(e.t)"></td>
                                                <td>
                                                    <div class="v2in-dim text-xs" x-text="e.test"></div>
                                                    <div class="v2in-strong" x-text="e.testCase"></div>
                                                </td>
                                                <td x-text="[e.environment, e.country].filter(Boolean).join(' / ')"></td>
                                                <td class="v2in-num">
                                                    <span class="v2in-chip" :style="'background: color-mix(in srgb, ' + statusColor(e.status) + ' 14%, transparent); color:' + statusColor(e.status)"
                                                          x-text="e.status + (e.fn ? ' (FN)' : '')"></span>
                                                </td>
                                                <td class="v2in-num" x-text="fmtDuration(e.durMs)"></td>
                                            </tr>
                                        </template>
                                    </tbody>
                                </table>
                                <template x-if="executions.length === 0">
                                    <div class="v2in-empty py-8 text-center">No execution on this period</div>
                                </template>
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
