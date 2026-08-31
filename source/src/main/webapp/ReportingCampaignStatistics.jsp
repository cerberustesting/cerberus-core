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
        <meta name="active-submenu" content="ReportingCampaignStatistics.jsp">
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%@ include file="include/global/dependenciesInclusions.html" %>

        <script type="text/javascript" src="js/pages/insightsShared.js?v=${appVersion}"></script>
        <%-- The detail tables on this page are the shared V2 table in client mode. --%>
        <script type="text/javascript" src="js/pages/ReportingCampaignStatistics.js?v=${appVersion}"></script>
        <link rel="stylesheet" type="text/css" href="css/pages/InsightsShared.css?v=${appVersion}"/>

        <title id="pageTitle">Campaign Statistics</title>
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

            <div x-data="campaignStatistics()" x-init="init()" class="v2in-page" id="campaignStatisticsRoot">

                <!-- Page title above the bar, like every list page -->
                <div class="v2in-pagetitle">
                    <h1 class="page-title-line">Campaign Statistics</h1>
                </div>

                <!-- Sticky header: prominent filters -->
                <div class="crb_card v2in-card v2in-header" :style="$store.rightPanel.open ? { top: '0px' } : {}">

                    <div class="v2mo-filters" id="csFilters">
                        <!-- Systems -->
                        <div class="v2in-field relative" @click.outside="if (ddOpen === 'system') ddOpen = ''">
                            <span class="v2in-fieldlabel">Systems</span>
                            <button type="button" class="v2in-picker" style="min-width: 200px; max-width: 300px"
                                    :class="ddOpen === 'system' ? 'v2in-picker--active' : ''"
                                    :title="selSystems.length ? selSystems.join(', ') : 'Pick at least one system'"
                                    @click="openDd('system')">
                                <span class="v2in-picker-value" x-text="pickerLabel('system')"></span>
                                <span class="flex-1"></span>
                                <span class="v2in-picker-count" x-show="selSystems.length && selSystems.length < refSystems.length" x-text="selSystems.length"></span>
                                <svg class="w-3.5 h-3.5 v2in-picker-chevron" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path d="M19 9l-7 7-7-7"/></svg>
                            </button>
                            <div x-show="ddOpen === 'system'" x-cloak class="v2in-dd" style="top: 100%; min-width: 250px">
                                <div class="v2in-dd-search">
                                    <input type="text" x-model="ddSearch" placeholder="Search system..." class="v2in-input">
                                    <button type="button" class="v2in-btn v2in-btn--xs" @click="allSel('system', true)">All</button>
                                    <button type="button" class="v2in-btn v2in-btn--xs" @click="allSel('system', false)">None</button>
                                </div>
                                <div class="v2in-dd-list">
                                    <template x-for="v in ddItems('system')" :key="v">
                                        <button type="button" class="v2in-dd-item" @click="toggleSel('system', v)">
                                            <span class="v2in-check" :class="selSystems.includes(v) ? 'v2in-check--on' : ''"></span>
                                            <span class="v2in-strong" x-text="v"></span>
                                        </button>
                                    </template>
                                    <div x-show="ddItems('system').length === 0" class="v2in-empty px-3 py-2 text-center">No system matches</div>
                                </div>
                            </div>
                        </div>

                        <!-- Applications -->
                        <div class="v2in-field relative" @click.outside="if (ddOpen === 'application') ddOpen = ''">
                            <span class="v2in-fieldlabel">Applications</span>
                            <button type="button" class="v2in-picker" style="min-width: 220px; max-width: 320px"
                                    :class="ddOpen === 'application' ? 'v2in-picker--active' : ''"
                                    :title="selApps.length ? selApps.join(', ') : 'Pick at least one application'"
                                    @click="openDd('application')">
                                <svg x-show="appsLoading" class="w-3.5 h-3.5 animate-spin" style="color: var(--crb-blue-color)" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path d="M21 12a9 9 0 11-6.22-8.56"/></svg>
                                <span class="v2in-picker-value" x-text="pickerLabel('application')"></span>
                                <span class="flex-1"></span>
                                <span class="v2in-picker-count" x-show="selApps.length && selApps.length < refApps.length" x-text="selApps.length"></span>
                                <svg class="w-3.5 h-3.5 v2in-picker-chevron" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path d="M19 9l-7 7-7-7"/></svg>
                            </button>
                            <div x-show="ddOpen === 'application'" x-cloak class="v2in-dd" style="top: 100%; min-width: 260px">
                                <div class="v2in-dd-search">
                                    <input type="text" x-model="ddSearch" placeholder="Search application..." class="v2in-input">
                                    <button type="button" class="v2in-btn v2in-btn--xs" @click="allSel('application', true)">All</button>
                                    <button type="button" class="v2in-btn v2in-btn--xs" @click="allSel('application', false)">None</button>
                                </div>
                                <div class="v2in-dd-list">
                                    <template x-for="v in ddItems('application')" :key="v">
                                        <button type="button" class="v2in-dd-item" @click="toggleSel('application', v)">
                                            <span class="v2in-check" :class="selApps.includes(v) ? 'v2in-check--on' : ''"></span>
                                            <span class="v2in-strong" x-text="v"></span>
                                        </button>
                                    </template>
                                    <div x-show="ddItems('application').length === 0" class="v2in-empty px-3 py-2 text-center">No application matches</div>
                                </div>
                            </div>
                        </div>

                        <!-- Period -->
                        <div class="v2in-field">
                            <span class="v2in-fieldlabel">From</span>
                            <input type="date" class="v2in-input" style="min-height: 42px" x-model="from">
                        </div>
                        <div class="v2in-field">
                            <span class="v2in-fieldlabel">To</span>
                            <input type="date" class="v2in-input" style="min-height: 42px" x-model="to">
                        </div>

                        <div class="v2in-field">
                            <span class="v2in-fieldlabel">&nbsp;</span>
                            <button type="button" class="v2in-btn v2in-btn--primary" style="min-height: 42px"
                                    :class="(selSystems.length && selApps.length) ? '' : 'v2in-btn--disabled'"
                                    @click="view === 'detail' ? loadDetail() : load()">
                                <svg class="w-3.5 h-3.5" :class="(loading || detail.loading) ? 'animate-spin' : ''" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path d="M3 12a9 9 0 019-9 9.75 9.75 0 016.74 2.74L21 8"/><path d="M21 3v5h-5"/><path d="M21 12a9 9 0 01-9 9 9.75 9.75 0 01-6.74-2.74L3 16"/><path d="M3 21v-5h5"/></svg>
                                Load
                            </button>
                        </div>

                        <div class="v2in-field" x-show="loaded && view === 'overview'">
                            <span class="v2in-fieldlabel">&nbsp;</span>
                            <span class="v2in-count" style="align-self: flex-start" x-text="rows.length + ' campaign(s)'"></span>
                        </div>
                    </div>
                </div>

                <!-- ═══════════ OVERVIEW ═══════════ -->
                <%--
                    x-show, not x-if, on the two views and on the detail body: a
                    <template x-if> destroys and recreates its content on every flip,
                    which would take the mounted V2 tables with it - switching to the
                    detail and back would leave the registry pointing at dead nodes.
                --%>
                <div x-show="view === 'overview'" x-cloak>
                    <div class="v2in-page">

                        <template x-if="loading && !rows.length">
                            <div class="crb_card v2in-card">
                                <div class="v2in-card-body text-center py-14">
                                    <svg class="w-6 h-6 animate-spin mx-auto mb-2" style="color: var(--crb-blue-color)" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path d="M21 12a9 9 0 11-6.22-8.56"/></svg>
                                    <div class="text-xs v2in-dim">Computing campaign statistics...</div>
                                </div>
                            </div>
                        </template>

                        <template x-if="error">
                            <div class="crb_card v2in-card">
                                <div class="v2in-card-body text-center py-10">
                                    <div class="text-sm font-semibold mb-1" x-text="error"></div>
                                    <div class="text-xs v2in-dim">Adjust the filters above and reload</div>
                                </div>
                            </div>
                        </template>

                        <!-- No data: explain and guide -->
                        <template x-if="loaded && !error && !loading && rows.length === 0">
                            <div class="crb_card v2in-card v2in-hero">
                                <div class="v2in-hero-icon">
                                    <svg class="w-7 h-7" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path d="M3 3v18h18"/><rect x="7" y="10" width="3" height="8" rx="1"/><rect x="13" y="6" width="3" height="12" rx="1"/></svg>
                                </div>
                                <div class="v2in-hero-title">No campaign ran on this period</div>
                                <div class="v2in-hero-sub">
                                    This page compares your campaigns over a period: number of runs, pass rate, average
                                    duration and reliability, with a drill-down by environment and country.
                                    No execution matches the current systems, applications and period.
                                </div>
                                <div class="v2in-hero-cta">
                                    <button type="button" class="v2in-btn" @click="from = _dateStr(new Date(Date.now() - 90 * 86400000)); load()">Look back 90 days</button>
                                    <button type="button" class="v2in-btn" @click="allSel('system', true); allSel('application', true); $nextTick(() => load())">Include everything</button>
                                </div>
                            </div>
                        </template>

                        <%-- x-show, not x-if: the mounted V2 table has to survive
                             every load, including one that returns no row. --%>
                        <div class="v2in-page" x-show="loaded && !error && rows.length > 0" x-cloak>
                                <!-- KPIs -->
                                <div class="v2in-kpis">
                                    <div class="crb_card v2in-card v2in-kpi">
                                        <span class="v2in-kpi-label">Campaigns</span>
                                        <span class="v2in-kpi-value" x-text="kpis.campaigns"></span>
                                        <span class="v2in-kpi-sub">with at least one run</span>
                                    </div>
                                    <div class="crb_card v2in-card v2in-kpi">
                                        <span class="v2in-kpi-label">Campaign runs</span>
                                        <span class="v2in-kpi-value" x-text="kpis.runs"></span>
                                        <span class="v2in-kpi-sub">over the period</span>
                                    </div>
                                    <div class="crb_card v2in-card v2in-kpi">
                                        <span class="v2in-kpi-label">OK rate</span>
                                        <span class="v2in-kpi-value" :style="'color:' + rateColor(kpis.okRate)" x-text="kpis.okRate !== null ? kpis.okRate + '%' : '-'"></span>
                                        <span class="v2in-kpi-sub">weighted by runs</span>
                                    </div>
                                    <div class="crb_card v2in-card v2in-kpi">
                                        <span class="v2in-kpi-label">Reliability</span>
                                        <span class="v2in-kpi-value" :style="'color:' + rateColor(kpis.reliability)" x-text="kpis.reliability !== null ? kpis.reliability + '%' : '-'"></span>
                                        <span class="v2in-kpi-sub">executions without technical failure</span>
                                    </div>
                                    <div class="crb_card v2in-card v2in-kpi">
                                        <span class="v2in-kpi-label">Avg duration</span>
                                        <span class="v2in-kpi-value" x-text="kpis.avgDur !== null ? durF(kpis.avgDur) : '-'"></span>
                                        <span class="v2in-kpi-sub">per campaign run</span>
                                    </div>
                                </div>

                                <!-- Campaign table -->
                                <div class="crb_card v2in-card">
                                    <div class="v2in-card-head">
                                        <span class="v2in-card-title">Campaigns</span>
                                        <span class="v2in-count" x-text="rows.length"></span>
                                        <span class="flex-1"></span>
                                        <span class="v2in-dim text-xs">click a campaign for its environment / country detail</span>
                                    </div>
                                    <%-- Shared V2 table, CLIENT mode (rows built in the
                                         browser) and EMBEDDED (the card and head above
                                         are the page's). --%>
                                    <div id="csCampaignTableMount"></div>
                                </div>
                        </div>
                    </div>
                </div>

                <!-- ═══════════ DETAIL: one campaign by environment x country ═══════════ -->
                <div x-show="view === 'detail'" x-cloak>
                    <div class="crb_card v2in-card">
                        <div class="v2in-card-head">
                            <button type="button" class="v2in-btn v2in-btn--xs" @click="closeDetail()" title="Back to every campaign">
                                <svg class="w-3 h-3" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path d="M19 12H5M12 19l-7-7 7-7"/></svg>
                                Back
                            </button>
                            <span class="v2in-card-title">Detail by environment &amp; country</span>
                            <span class="v2in-chip v2in-chip--info" x-text="detail.campaign"></span>
                            <span class="v2in-count" x-show="!detail.loading" x-text="detail.rows.length + ' combination(s)'"></span>
                            <span class="flex-1"></span>
                            <button type="button" class="v2in-btn v2in-btn--xs" @click="openTrends({campaign: detail.campaign})" title="Open the run-by-run trend of this campaign">Trends</button>
                        </div>

                        <!-- Env / country pills -->
                        <div class="v2in-card-body" style="padding-bottom: 0" x-show="detail.envs.length > 1 || detail.countries.length > 1">
                            <div class="flex items-center gap-2 flex-wrap">
                                <template x-if="detail.envs.length > 1">
                                    <div class="flex items-center gap-1.5 flex-wrap">
                                        <span class="v2in-dim text-xs font-semibold">Environment</span>
                                        <template x-for="e in detail.envs" :key="'e' + e">
                                            <button type="button" class="v2in-pill"
                                                    :class="(detail.selEnvs.length === 0 || detail.selEnvs.includes(e)) ? 'v2in-pill--on' : ''"
                                                    @click="toggleDetailFilter('env', e)" x-text="e"></button>
                                        </template>
                                    </div>
                                </template>
                                <template x-if="detail.countries.length > 1">
                                    <div class="flex items-center gap-1.5 flex-wrap">
                                        <span class="v2in-dim text-xs font-semibold">Country</span>
                                        <template x-for="c in detail.countries" :key="'c' + c">
                                            <button type="button" class="v2in-pill"
                                                    :class="(detail.selCountries.length === 0 || detail.selCountries.includes(c)) ? 'v2in-pill--on' : ''"
                                                    @click="toggleDetailFilter('country', c)" x-text="c"></button>
                                        </template>
                                    </div>
                                </template>
                            </div>
                        </div>

                        <template x-if="detail.loading">
                            <div class="v2in-card-body text-center py-12">
                                <svg class="w-6 h-6 animate-spin mx-auto mb-2" style="color: var(--crb-blue-color)" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path d="M21 12a9 9 0 11-6.22-8.56"/></svg>
                                <div class="text-xs v2in-dim" x-text="'Loading the detail of ' + detail.campaign + '...'"></div>
                            </div>
                        </template>
                        <template x-if="!detail.loading && detail.error">
                            <div class="v2in-card-body text-center py-10">
                                <div class="text-sm font-semibold mb-1" x-text="detail.error"></div>
                                <div class="text-xs v2in-dim">Widen the period or go back to the campaign list</div>
                            </div>
                        </template>
                        <%-- x-show, not x-if: the mounted V2 table must survive the
                             loading / error / ready flips. --%>
                        <div x-show="!detail.loading && !detail.error" x-cloak>
                            <%-- Shared V2 table, CLIENT mode + EMBEDDED. --%>
                            <div id="csDetailTableMount"></div>
                        </div>
                    </div>
                </div>

            </div>

            <footer class="footer">
                <div class="container-fluid" id="footer"></div>
            </footer>
        </main>
    </body>
</html>
