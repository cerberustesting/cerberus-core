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
        <meta name="active-submenu" content="ReportingCampaignOverTime.jsp">
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%@ include file="include/global/dependenciesInclusions.html" %>

        <script type="text/javascript" src="js/pages/insightsShared.js?v=${appVersion}"></script>
        <script type="text/javascript" src="js/pages/ReportingCampaignOverTime.js?v=${appVersion}"></script>
        <link rel="stylesheet" type="text/css" href="css/pages/InsightsShared.css?v=${appVersion}"/>

        <title id="pageTitle">Campaign Trends</title>
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

            <div x-data="campaignTrends()" x-init="init()" class="v2in-page" id="campaignTrendsRoot">

                <!-- Page title above the bar, like every list page -->
                <div class="v2in-pagetitle">
                    <h1 class="page-title-line">Campaign Trends</h1>
                </div>

                <!-- Sticky header: filters -->
                <div class="crb_card v2in-card v2in-header" :style="$store.rightPanel.open ? { top: '0px' } : {}">
                    <div class="flex items-center gap-3 flex-wrap">

                        <!-- Campaigns multi picker -->
                        <div class="v2in-field relative" @click.outside="campDdOpen = false">
                            <span class="v2in-fieldlabel">Campaigns to compare</span>
                            <button type="button" class="v2in-picker" style="min-width: 280px; max-width: 440px"
                                    :class="[campDdOpen ? 'v2in-picker--active' : '', selCampaigns.length === 0 ? '' : '']"
                                    :title="selCampaigns.length ? selCampaigns.join('\n') : 'Every campaign of the instance - open to focus on some of them'"
                                    @click="campDdOpen = !campDdOpen; if (campDdOpen) { campSearch = ''; $nextTick(() => $refs.campSearchInput && $refs.campSearchInput.focus()) }">
                                <svg class="w-4 h-4 v2in-picker-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path d="M20.59 13.41l-7.17 7.17a2 2 0 01-2.83 0L2 12V2h10l8.59 8.59a2 2 0 010 2.82z"/><line x1="7" y1="7" x2="7.01" y2="7"/></svg>
                                <span class="v2in-picker-value"
                                      x-text="selCampaigns.length === 0 ? ('All campaigns' + (campaigns.length ? ' (' + campaigns.length + ')' : '')) : selCampaigns.slice(0, 2).join(', ') + (selCampaigns.length > 2 ? ' +' + (selCampaigns.length - 2) : '')"></span>
                                <span class="flex-1"></span>
                                <span class="v2in-picker-count" x-show="selCampaigns.length" x-text="selCampaigns.length"></span>
                                <svg class="w-3.5 h-3.5 v2in-picker-chevron" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path d="M19 9l-7 7-7-7"/></svg>
                            </button>
                            <div x-show="campDdOpen" x-cloak class="v2in-dd v2in-dd--right">
                                <div class="v2in-dd-search">
                                    <input type="text" x-ref="campSearchInput" x-model="campSearch" placeholder="Search campaign..." class="v2in-input">
                                    <button type="button" class="v2in-btn v2in-btn--xs" @click="selCampaigns = []">Clear</button>
                                </div>
                                <div class="v2in-dd-list">
                                    <template x-for="c in filteredCampaigns" :key="c">
                                        <button type="button" class="v2in-dd-item" @click="toggleCampaign(c)">
                                            <span class="v2in-check" :class="selCampaigns.includes(c) ? 'v2in-check--on' : ''"></span>
                                            <span x-text="c"></span>
                                        </button>
                                    </template>
                                    <div x-show="filteredCampaigns.length === 0" class="v2in-empty px-3 py-2 text-center">No campaign matches</div>
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
                            <button type="button" class="v2in-btn v2in-btn--primary" style="min-height: 42px" @click="load()">
                                <svg class="w-3.5 h-3.5" :class="loading ? 'animate-spin' : ''" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path d="M3 12a9 9 0 019-9 9.75 9.75 0 016.74 2.74L21 8"/><path d="M21 3v5h-5"/><path d="M21 12a9 9 0 01-9 9 9.75 9.75 0 01-6.74-2.74L3 16"/><path d="M3 21v-5h5"/></svg>
                                Load
                            </button>
                        </div>
                    </div>

                    <!-- Secondary filters (from the loaded data, applied server-side) -->
                    <div class="flex items-center gap-2 flex-wrap mt-3" x-show="loaded" x-cloak>
                        <template x-for="grp in [
                                { label: 'Env', map: activeEnvs },
                                { label: 'Country', map: activeCountries },
                                { label: 'Robot', map: activeRobots },
                                { label: 'CI', map: activeCi }
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

                <!-- Error / loading -->
                <template x-if="error">
                    <div class="crb_card v2in-card">
                        <div class="v2in-card-body text-center py-10">
                            <div class="text-sm font-semibold mb-1" x-text="error"></div>
                            <div class="text-xs v2in-dim">Adjust the filters above and reload</div>
                        </div>
                    </div>
                </template>
                <template x-if="loading && !loaded">
                    <div class="crb_card v2in-card">
                        <div class="v2in-card-body text-center py-14">
                            <svg class="w-6 h-6 animate-spin mx-auto mb-2" style="color: var(--crb-blue-color)" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path d="M21 12a9 9 0 11-6.22-8.56"/></svg>
                            <div class="text-xs v2in-dim">Loading campaign trends...</div>
                        </div>
                    </div>
                </template>

                <!-- No run on the period: explain and guide instead of showing zeros -->
                <template x-if="loaded && !error && runs.length === 0">
                    <div class="crb_card v2in-card v2in-hero">
                        <div class="v2in-hero-icon">
                            <svg class="w-7 h-7" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path d="M20.59 13.41l-7.17 7.17a2 2 0 01-2.83 0L2 12V2h10l8.59 8.59a2 2 0 010 2.82z"/><line x1="7" y1="7" x2="7.01" y2="7"/></svg>
                        </div>
                        <div class="v2in-hero-title">No campaign run on this period</div>
                        <div class="v2in-hero-sub">
                            This page tracks how your campaigns behave run after run: pass rate, duration, CI verdicts and flaky
                            executions. <span x-show="selCampaigns.length">The selected campaign(s) did not run in the last <b x-text="periodDays"></b> days.</span>
                            <span x-show="!selCampaigns.length">Nothing ran in the last <b x-text="periodDays"></b> days.</span>
                        </div>
                        <div class="v2in-hero-cta">
                            <button type="button" class="v2in-btn" x-show="periodDays < 180" @click="periodDays = 180; load()">Look back 180 days</button>
                            <button type="button" class="v2in-btn" x-show="selCampaigns.length" @click="selCampaigns = []; load()">Include every campaign</button>
                            <button type="button" class="v2in-btn v2in-btn--primary"
                                    @click="campDdOpen = true; campSearch = ''; window.scrollTo({top: 0, behavior: 'smooth'}); $nextTick(() => $refs.campSearchInput && $refs.campSearchInput.focus())">
                                Pick other campaigns
                            </button>
                        </div>
                    </div>
                </template>

                <template x-if="loaded && !error && runs.length > 0">
                    <div class="v2in-page">

                        <!-- KPIs -->
                        <div class="v2in-kpis">
                            <div class="crb_card v2in-card v2in-kpi">
                                <span class="v2in-kpi-label">Campaign runs</span>
                                <span class="v2in-kpi-value" x-text="kpis.runs"></span>
                                <span class="v2in-kpi-sub" x-text="'over the last ' + periodDays + ' days'"></span>
                            </div>
                            <div class="crb_card v2in-card v2in-kpi">
                                <span class="v2in-kpi-label">CI pass rate</span>
                                <span class="v2in-kpi-value" :style="kpis.ciRate !== null ? ('color:' + (kpis.ciRate >= 80 ? 'var(--crb-green-color)' : (kpis.ciRate >= 50 ? 'var(--crb-orange-color)' : 'var(--crb-red-color)'))) : ''"
                                      x-text="kpis.ciRate !== null ? kpis.ciRate + '%' : '-'"></span>
                                <span class="v2in-kpi-sub" x-text="kpis.ciKnown + ' run(s) with a CI verdict'"></span>
                            </div>
                            <div class="crb_card v2in-card v2in-kpi">
                                <span class="v2in-kpi-label">Avg duration</span>
                                <span class="v2in-kpi-value" x-text="kpis.avgDur !== null ? fmtDuration(kpis.avgDur) : '-'"></span>
                                <span class="v2in-kpi-sub">per campaign run</span>
                            </div>
                            <div class="crb_card v2in-card v2in-kpi">
                                <span class="v2in-kpi-label">Executions</span>
                                <span class="v2in-kpi-value" x-text="kpis.totalExe"></span>
                                <span class="v2in-kpi-sub">useful test case executions</span>
                            </div>
                            <div class="crb_card v2in-card v2in-kpi">
                                <span class="v2in-kpi-label">Flaky</span>
                                <span class="v2in-kpi-value" :style="kpis.flaky > 0 ? 'color: #b45309' : ''" x-text="kpis.flaky"></span>
                                <span class="v2in-kpi-sub">executions that needed retries</span>
                            </div>
                        </div>

                        <!-- Runs history: stacked status bars -->
                        <div class="crb_card v2in-card">
                            <div class="v2in-card-head">
                                <span class="v2in-card-title">Runs history</span>
                                <span class="v2in-count" x-text="runs.length + ' runs'"></span>
                                <span class="flex-1"></span>
                                <span class="v2in-legend">
                                    <template x-for="s in legendStatuses" :key="s">
                                        <span class="v2in-legend-item"><span class="v2in-legend-dot" :style="'background:' + statusColor(s)"></span><span x-text="s"></span></span>
                                    </template>
                                </span>
                            </div>
                            <div class="v2in-card-body" x-html="runsBarsSvg" @click="chartClick($event)" title="Click a bar to open the campaign report"></div>
                        </div>

                        <!-- Duration / CI score over time -->
                        <div class="crb_card v2in-card">
                            <div class="v2in-card-head">
                                <span class="v2in-card-title" x-text="chartTab === 'duration' ? 'Duration over time' : 'CI score over time'"></span>
                                <span class="flex-1"></span>
                                <div class="v2in-seg">
                                    <button type="button" class="v2in-seg-item" :class="chartTab === 'duration' ? 'v2in-seg-item--on' : ''" @click="chartTab = 'duration'">Duration</button>
                                    <button type="button" class="v2in-seg-item" :class="chartTab === 'ci' ? 'v2in-seg-item--on' : ''" @click="chartTab = 'ci'">CI score</button>
                                </div>
                            </div>
                            <div class="v2in-card-body">
                                <template x-if="chartTab === 'duration'">
                                    <div>
                                        <div x-html="durationSvg" @click="chartClick($event)"></div>
                                        <div class="v2in-legend mt-2" x-show="durationSeries.length > 1">
                                            <template x-for="s in durationSeries" :key="s.id">
                                                <span class="v2in-legend-item"><span class="v2in-legend-dot" :style="'background:' + s.color"></span><span x-text="s.name"></span></span>
                                            </template>
                                        </div>
                                        <div class="v2in-empty py-4 text-center" x-show="durationSeries.length === 0">No run with a duration above one minute on this period</div>
                                    </div>
                                </template>
                                <template x-if="chartTab === 'ci'">
                                    <div x-html="ciSvg" @click="chartClick($event)" title="Click a dot to open the campaign report"></div>
                                </template>
                            </div>
                        </div>

                        <!-- Runs table -->
                        <div class="crb_card v2in-card">
                            <div class="v2in-card-head">
                                <span class="v2in-card-title">Run by run</span>
                                <span class="v2in-count" x-text="tableRuns.length"></span>
                                <span class="flex-1"></span>
                                <span class="v2in-dim text-xs">click a run to open its campaign report</span>
                            </div>
                            <div class="v2in-table-scroll">
                                <table class="v2in-table">
                                    <thead>
                                        <tr>
                                            <th>Run</th>
                                            <th>Campaign</th>
                                            <th class="v2in-num">Date</th>
                                            <th class="v2in-num">Executions</th>
                                            <th class="v2in-num">Flaky</th>
                                            <th class="v2in-num">CI</th>
                                            <th class="v2in-num">Duration</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <template x-for="r in tableRuns" :key="r.tag">
                                            <tr class="v2in-row-click" @click="openRun(r.tag)">
                                                <td class="v2in-strong" x-text="r.tag"></td>
                                                <td x-text="r.campaign || '-'"></td>
                                                <td class="v2in-num v2in-dim" x-text="r.t ? fmtDateTime(r.t) : '-'"></td>
                                                <td class="v2in-num" x-text="r.nbExeU"></td>
                                                <td class="v2in-num">
                                                    <span :class="r.nbFlaky > 0 ? 'v2in-chip v2in-chip--warn' : 'v2in-dim'" x-text="r.nbFlaky"></span>
                                                </td>
                                                <td class="v2in-num">
                                                    <span x-show="r.ciRes" class="v2in-chip" :class="r.ciRes === 'OK' ? 'v2in-chip--ok' : 'v2in-chip--ko'"
                                                          x-text="r.ciRes + (r.ciSc !== undefined && r.ciSc !== null ? ' ' + r.ciSc + '/' + (r.ciScT || 100) : '')"></span>
                                                    <span x-show="!r.ciRes" class="v2in-dim">-</span>
                                                </td>
                                                <td class="v2in-num" x-text="r.durMs !== null ? fmtDuration(r.durMs) : '-'"></td>
                                            </tr>
                                        </template>
                                    </tbody>
                                </table>
                                <template x-if="tableRuns.length === 0">
                                    <div class="v2in-empty py-8 text-center">No campaign run on this period</div>
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
