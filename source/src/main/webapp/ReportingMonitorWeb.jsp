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
        <meta name="active-menu" content="monitor">
        <meta name="active-submenu" content="ReportingMonitorWeb.jsp">
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%@ include file="include/global/dependenciesInclusions.html" %>

        <script type="text/javascript" src="js/pages/insightsShared.js?v=${appVersion}"></script>
        <script type="text/javascript" src="js/pages/ReportingMonitorWeb.js?v=${appVersion}"></script>
        <link rel="stylesheet" type="text/css" href="css/pages/InsightsShared.css?v=${appVersion}"/>
        <link rel="stylesheet" type="text/css" href="css/pages/ReportingMonitorWeb.css?v=${appVersion}"/>

        <title id="pageTitle">Web Monitor</title>
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

            <div x-data="webMonitor()" class="v2in-page" id="webMonitorRoot">

                <div class="v2in-pagetitle">
                    <h1 class="page-title-line" x-text="t('title')"></h1>
                </div>

                <!-- Sticky header: favorites (tabs), test case, period and filters -->
                <div class="crb_card v2in-card v2in-header" :style="$store.rightPanel.open ? { top: '0px' } : {}">
                    <div class="flex flex-col gap-3">

                        <!-- Tabs: one per favorite test case, loaded when clicked -->
                        <div class="wm-tabs" x-show="favorites.length || adHoc">
                            <template x-for="f in favorites" :key="f.test + '/' + f.testCase">
                                <div class="wm-tab" :class="sameCase(f, current) ? 'wm-tab--on' : ''" @click="!sameCase(f, current) && open(f)" :title="f.test + ' / ' + f.testCase">
                                    <i data-lucide="star" class="w-3.5 h-3.5"></i>
                                    <span class="truncate" x-text="f.testCase"></span>
                                    <span class="wm-tab-sub truncate" x-text="f.test"></span>
                                    <button type="button" class="wm-tab-x" @click.stop="toggleFavorite(f)" :title="t('removefavorite')">&times;</button>
                                </div>
                            </template>
                            <div class="wm-tab wm-tab--on wm-tab--adhoc" x-show="adHoc" x-cloak>
                                <i data-lucide="search" class="w-3.5 h-3.5"></i>
                                <span class="truncate" x-text="current ? current.testCase : ''"></span>
                                <span class="wm-tab-sub truncate" x-text="current ? current.test : ''"></span>
                            </div>
                        </div>

                        <div class="flex items-end gap-3 flex-wrap">
                            <!-- Test case picker: browse a folder, then pick a test case -->
                            <div class="v2in-field relative" @click.outside="tcDdOpen = false">
                                <span class="v2in-fieldlabel" x-text="t('testcase')"></span>
                                <div class="flex items-center gap-2">
                                    <button type="button" class="v2in-picker" style="min-width: 300px; max-width: 460px"
                                            :class="[tcDdOpen ? 'v2in-picker--active' : '', !current ? 'v2in-picker--empty' : '']"
                                            @click="tcDdOpen = !tcDdOpen; if (tcDdOpen) { tcSearch = ''; $nextTick(() => $refs.tcSearchInput && $refs.tcSearchInput.focus()) }">
                                        <svg class="w-4 h-4 v2in-picker-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path d="M22 19a2 2 0 01-2 2H4a2 2 0 01-2-2V5a2 2 0 012-2h5l2 3h9a2 2 0 012 2z"/></svg>
                                        <span class="v2in-picker-value" x-text="current ? current.test + ' / ' + current.testCase : t('choosetestcase')"></span>
                                        <span class="flex-1"></span>
                                        <svg class="w-3.5 h-3.5 v2in-picker-chevron" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path d="M19 9l-7 7-7-7"/></svg>
                                    </button>
                                    <button type="button" class="wm-star" :class="current && isFavorite ? 'wm-star--on' : ''" x-show="current" x-cloak
                                            @click="toggleFavorite()" :title="current && isFavorite ? t('removefavorite') : t('addfavorite')">
                                        <i data-lucide="star" class="w-4 h-4"></i>
                                    </button>
                                </div>
                                <div x-show="tcDdOpen" x-cloak class="v2in-dd v2in-dd--right" style="min-width: 340px">
                                    <div class="v2in-dd-search">
                                        <button type="button" class="v2in-btn v2in-btn--xs" x-show="browsedTest" @click="browseTest('')" :title="t('back')">
                                            <svg class="w-3 h-3" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path d="M19 12H5M12 19l-7-7 7-7"/></svg>
                                        </button>
                                        <input type="text" x-ref="tcSearchInput" x-model="tcSearch" :placeholder="browsedTest ? t('searchtestcase') : t('searchfolder')" class="v2in-input">
                                    </div>
                                    <div class="v2in-dd-list">
                                        <template x-if="!browsedTest">
                                            <div>
                                                <template x-for="tst in filteredTests" :key="tst">
                                                    <button type="button" class="v2in-dd-item" @click="browseTest(tst)">
                                                        <svg class="w-3.5 h-3.5 shrink-0" style="color: var(--crb-grey-color)" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path d="M22 19a2 2 0 01-2 2H4a2 2 0 01-2-2V5a2 2 0 012-2h5l2 3h9a2 2 0 012 2z"/></svg>
                                                        <span class="truncate" x-text="tst"></span>
                                                        <span class="flex-1"></span>
                                                        <svg class="w-3 h-3 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path d="M9 5l7 7-7 7"/></svg>
                                                    </button>
                                                </template>
                                                <div x-show="filteredTests.length === 0" class="v2in-empty px-3 py-2 text-center" x-text="t('nofolder')"></div>
                                            </div>
                                        </template>
                                        <template x-if="browsedTest">
                                            <div>
                                                <div class="v2in-dim text-xs px-3 py-1 font-semibold truncate" x-text="browsedTest"></div>
                                                <template x-for="tc in filteredTestcases" :key="tc.testCase">
                                                    <button type="button" class="v2in-dd-item" @click="pick(tc)">
                                                        <span class="v2in-strong" x-text="tc.testCase"></span>
                                                        <span class="v2in-dim truncate text-xs" x-text="tc.description"></span>
                                                    </button>
                                                </template>
                                                <div x-show="filteredTestcases.length === 0" class="v2in-empty px-3 py-2 text-center" x-text="t('notestcase')"></div>
                                            </div>
                                        </template>
                                    </div>
                                </div>
                            </div>

                            <!-- Period -->
                            <div class="v2in-field">
                                <span class="v2in-fieldlabel" x-text="t('period')"></span>
                                <div class="v2in-seg" style="min-height: 42px; align-items: center">
                                    <template x-for="p in periods" :key="p.hours">
                                        <button type="button" class="v2in-seg-item" :class="periodHours === p.hours ? 'v2in-seg-item--on' : ''" @click="setPeriod(p.hours)" x-text="p.label"></button>
                                    </template>
                                </div>
                            </div>

                            <!-- Facets: only worth showing when there is a choice -->
                            <div class="v2in-field" x-show="robots.length > 1" x-cloak>
                                <span class="v2in-fieldlabel" x-text="t('browser')"></span>
                                <div class="flex gap-1.5 flex-wrap">
                                    <template x-for="v in robots" :key="v">
                                        <button type="button" class="v2in-pill" :class="fRobots.includes(v) ? 'v2in-pill--on' : ''" @click="toggleFacet(fRobots, v)" x-text="v"></button>
                                    </template>
                                </div>
                            </div>
                            <div class="v2in-field" x-show="envs.length > 1" x-cloak>
                                <span class="v2in-fieldlabel" x-text="t('environment')"></span>
                                <div class="flex gap-1.5 flex-wrap">
                                    <template x-for="v in envs" :key="v">
                                        <button type="button" class="v2in-pill" :class="fEnvs.includes(v) ? 'v2in-pill--on' : ''" @click="toggleFacet(fEnvs, v)" x-text="v"></button>
                                    </template>
                                </div>
                            </div>
                            <div class="v2in-field" x-show="countries.length > 1" x-cloak>
                                <span class="v2in-fieldlabel" x-text="t('country')"></span>
                                <div class="flex gap-1.5 flex-wrap">
                                    <template x-for="v in countries" :key="v">
                                        <button type="button" class="v2in-pill" :class="fCountries.includes(v) ? 'v2in-pill--on' : ''" @click="toggleFacet(fCountries, v)" x-text="v"></button>
                                    </template>
                                </div>
                            </div>

                            <div class="flex-1"></div>
                            <span class="v2in-dim text-xs" x-show="loadedAt" x-text="t('refreshed', ago(loadedAt))"></span>
                            <button type="button" class="v2in-btn" @click="load()" :disabled="loading || !current">
                                <i data-lucide="refresh-cw" class="w-3.5 h-3.5"></i><span x-text="t('refresh')"></span>
                            </button>
                        </div>
                    </div>
                </div>

                <!-- Error -->
                <div class="crb_card v2in-card" x-show="error" x-cloak>
                    <div class="v2in-card-body"><span class="v2in-chip v2in-chip--ko" x-text="error"></span></div>
                </div>

                <!-- Nothing chosen / no data -->
                <div class="crb_card v2in-card" x-show="!current" x-cloak>
                    <div class="v2in-card-body"><div class="v2in-empty" x-text="t('empty')"></div></div>
                </div>
                <div class="crb_card v2in-card" x-show="current && loaded && !loading && !error && rows.length === 0" x-cloak>
                    <div class="v2in-card-body"><div class="v2in-empty" x-text="t('nodata')"></div></div>
                </div>
                <div class="v2in-dim text-xs" x-show="loading" x-text="t('loading')"></div>

                <template x-if="current && rows.length > 0">
                    <div class="v2in-page">

                        <div class="grid grid-cols-1 gap-6 lg:grid-cols-3">

                            <!-- Network time over the executions -->
                            <div class="crb_card lg:col-span-2">
                                <div class="flex flex-wrap items-start justify-between gap-3">
                                    <div class="wm-head">
                                        <div class="wm-tile"><i data-lucide="timer" class="w-5 h-5 text-blue-500"></i></div>
                                        <div>
                                            <div class="wm-title" x-text="t('responsetime')"></div>
                                            <div class="wm-sub" x-text="current.test + ' / ' + current.testCase + ' - ' + t('executionscount', rows.length)"></div>
                                        </div>
                                    </div>
                                    <div class="v2in-legend">
                                        <span class="v2in-legend-item"><span class="v2in-legend-dot" style="background:#2c7be5"></span><span x-text="t('totaltime')"></span></span>
                                        <span class="v2in-legend-item"><span class="v2in-legend-dot" style="background:#8b5cf6"></span><span x-text="t('internaltime')"></span></span>
                                        <span class="v2in-legend-item"><span class="v2in-legend-dot" style="background:#e63757"></span>KO</span>
                                    </div>
                                </div>

                                <div class="mt-4 flex flex-col gap-4 md:flex-row md:items-end">
                                    <div class="shrink-0">
                                        <div class="wm-big" x-text="fmtMs(kpis.time)"></div>
                                        <div class="mt-1 text-xs font-semibold" x-show="delta('totalTime')" :style="'color:' + (delta('totalTime') && delta('totalTime').good ? '#00d27a' : '#e63757')" x-text="delta('totalTime') ? delta('totalTime').text : ''"></div>
                                        <div class="mt-1 text-xs v2in-dim" x-show="!delta('totalTime')" x-text="t('novsprevious')"></div>
                                    </div>
                                    <div class="flex-1 min-w-0" x-ref="timeChart" @click="onChartClick($event)" x-html="timeChart"></div>
                                </div>

                                <div class="mt-4">
                                    <div class="v2in-fieldlabel mb-2" x-text="t('statusperexecution')"></div>
                                    <div class="wm-strip">
                                        <template x-for="r in strip" :key="r.id">
                                            <button type="button" class="wm-strip-item" :class="selected && selected.id === r.id ? 'wm-strip-item--on' : ''"
                                                    :style="'background:' + statusColor(r.controlStatus)" :title="'#' + r.id + ' - ' + fmtDate(r.start) + ' - ' + r.controlStatus"
                                                    @click="select(r)"></button>
                                        </template>
                                    </div>
                                </div>
                            </div>

                            <!-- Traffic and KPIs -->
                            <div class="flex flex-col gap-6">
                                <div class="crb_card">
                                    <div class="wm-head">
                                        <div class="wm-tile"><i data-lucide="network" class="w-5 h-5 text-green-500"></i></div>
                                        <div class="wm-title" x-text="t('traffic')"></div>
                                        <div class="flex-1"></div>
                                        <span class="v2in-chip">KB &middot; req</span>
                                    </div>
                                    <div class="mt-3 wm-chartbox" x-html="trafficChart"></div>
                                    <div class="mt-3 flex items-center justify-between text-xs">
                                        <span class="v2in-dim" x-text="t('avgtransfer')"></span>
                                        <span><strong x-text="fmtSize(kpis.size)"></strong>
                                            <span class="font-semibold" x-show="delta('totalSize')" :style="'color:' + (delta('totalSize') && delta('totalSize').good ? '#00d27a' : '#e63757')" x-text="delta('totalSize') ? delta('totalSize').text : ''"></span></span>
                                    </div>
                                </div>
                                <div class="grid grid-cols-2 gap-3">
                                    <div class="crb_card wm-kpi"><div class="wm-kpi-label" x-text="t('successrate')"></div><div class="wm-kpi-value" style="color:#00d27a" x-text="kpis.rate === null ? '-' : kpis.rate.toFixed(1) + '%'"></div></div>
                                    <div class="crb_card wm-kpi"><div class="wm-kpi-label" x-text="t('failures')"></div><div class="wm-kpi-value" style="color:#e63757" x-text="kpis.failures"></div></div>
                                    <div class="crb_card wm-kpi"><div class="wm-kpi-label" x-text="t('requests')"></div><div class="wm-kpi-value" style="color:#2c7be5" x-text="fmtNum(kpis.hits)"></div></div>
                                    <div class="crb_card wm-kpi"><div class="wm-kpi-label" x-text="t('thirdparties')"></div><div class="wm-kpi-value" style="color:#8b5cf6" x-text="fmtNum(kpis.parties)"></div></div>
                                </div>
                            </div>
                        </div>

                        <div class="grid grid-cols-1 gap-6 lg:grid-cols-3">

                            <!-- Weight by content type (selected execution) -->
                            <div class="crb_card">
                                <div class="wm-head">
                                    <div class="wm-tile"><i data-lucide="package" class="w-5 h-5 text-orange-500"></i></div>
                                    <div>
                                        <div class="wm-title" x-text="t('weightbytype')"></div>
                                        <div class="wm-sub" x-text="t('totalof', fmtSize(typeWeights.total))"></div>
                                    </div>
                                </div>
                                <div class="wm-bar mt-4">
                                    <template x-for="i in typeWeights.items" :key="i.key">
                                        <div :style="'width:' + i.pct + '%;background:' + i.color"></div>
                                    </template>
                                </div>
                                <div class="mt-4 flex flex-col gap-2 text-sm">
                                    <template x-for="i in typeWeights.items" :key="i.key">
                                        <div class="flex items-center justify-between">
                                            <span class="flex items-center gap-2"><span class="v2in-legend-dot" :style="'background:' + i.color"></span><span x-text="t('type' + i.key)"></span></span>
                                            <span class="v2in-dim" x-text="fmtSize(i.size) + ' - ' + Math.round(i.pct) + '%'"></span>
                                        </div>
                                    </template>
                                </div>
                            </div>

                            <!-- Third party hosts (selected execution) -->
                            <div class="crb_card">
                                <div class="wm-head">
                                    <div class="wm-tile"><i data-lucide="share-2" class="w-5 h-5 text-purple-500"></i></div>
                                    <div>
                                        <div class="wm-title" x-text="t('thirdpartyhosts')"></div>
                                        <div class="wm-sub" x-text="t('thirdpartysub')"></div>
                                    </div>
                                </div>
                                <div class="mt-4 flex flex-col gap-3 text-sm">
                                    <template x-for="p in partyList" :key="p.name">
                                        <div>
                                            <div class="flex justify-between gap-2"><span class="truncate" :title="p.name" x-text="p.name"></span><span class="v2in-dim shrink-0" x-text="p.requests + ' req - ' + fmtSize(p.size)"></span></div>
                                            <div class="wm-meter"><div :style="'width:' + p.pct + '%'"></div></div>
                                        </div>
                                    </template>
                                    <div class="v2in-empty" x-show="partyList.length === 0" x-text="t('nothirdparty')"></div>
                                </div>
                            </div>

                            <!-- Selected execution -->
                            <div class="crb_card" x-show="selected">
                                <div class="wm-head">
                                    <div class="wm-tile"><i data-lucide="square-play" class="w-5 h-5 text-blue-500"></i></div>
                                    <div class="min-w-0">
                                        <div class="wm-title" x-text="selected ? t('execution', selected.id) : ''"></div>
                                        <div class="wm-sub truncate" x-text="selected ? fmtDate(selected.start) + ' - ' + selected.robotDecli + ' - ' + selected.environment + ' - ' + selected.country : ''"></div>
                                    </div>
                                    <div class="flex-1"></div>
                                    <span class="v2in-chip" :style="selected ? 'color:' + statusColor(selected.controlStatus) : ''" x-text="selected ? '● ' + selected.controlStatus : ''"></span>
                                </div>
                                <template x-if="selected">
                                    <div class="mt-4 grid grid-cols-2 gap-3">
                                        <div class="wm-cell"><div class="wm-kpi-label" x-text="t('totaltime')"></div><div class="wm-cell-value" x-text="fmtMs(selected.totalTime)"></div></div>
                                        <div class="wm-cell"><div class="wm-kpi-label" x-text="t('internaltimelabel')"></div><div class="wm-cell-value" x-text="fmtMs(selected.internalTime)"></div></div>
                                        <div class="wm-cell"><div class="wm-kpi-label" x-text="t('transfer')"></div><div class="wm-cell-value" x-text="fmtSize(selected.totalSize)"></div></div>
                                        <div class="wm-cell"><div class="wm-kpi-label" x-text="t('requests')"></div><div class="wm-cell-value" x-text="selected.totalHits"></div></div>
                                    </div>
                                </template>
                                <button type="button" class="v2in-btn mt-4" @click="openExecution(selected)">
                                    <i data-lucide="external-link" class="w-3.5 h-3.5"></i><span x-text="t('open')"></span>
                                </button>
                            </div>
                        </div>

                        <!-- Latest executions -->
                        <div class="crb_card v2in-card">
                            <div class="v2in-card-head">
                                <span class="v2in-card-title" x-text="t('latest')"></span>
                                <span class="v2in-count" x-text="latest.length"></span>
                            </div>
                            <div class="v2in-card-body">
                                <div class="v2in-table-scroll">
                                    <table class="v2in-table">
                                        <thead><tr>
                                            <th x-text="t('execcol')"></th><th x-text="t('environment')"></th><th x-text="t('country')"></th><th x-text="t('robot')"></th>
                                            <th class="v2in-num" x-text="t('totaltime')"></th><th class="v2in-num" x-text="t('internaltime')"></th>
                                            <th class="v2in-num" x-text="t('requests')"></th><th class="v2in-num" x-text="t('size')"></th><th x-text="t('status')"></th>
                                        </tr></thead>
                                        <tbody>
                                            <template x-for="r in latest" :key="r.id">
                                                <tr class="v2in-row-click" :class="selected && selected.id === r.id ? 'wm-row--on' : ''" @click="select(r)">
                                                    <td style="white-space: nowrap"><span class="v2in-strong" x-text="'#' + r.id"></span> <span class="v2in-dim" x-text="fmtDate(r.start)"></span></td>
                                                    <td x-text="r.environment"></td><td x-text="r.country"></td><td x-text="r.robotDecli"></td>
                                                    <td class="v2in-num" x-text="fmtMs(r.totalTime)"></td><td class="v2in-num v2in-dim" x-text="fmtMs(r.internalTime)"></td>
                                                    <td class="v2in-num" x-text="r.totalHits"></td><td class="v2in-num" x-text="fmtSize(r.totalSize)"></td>
                                                    <td><span class="v2in-chip" :style="'color:' + statusColor(r.controlStatus)" x-text="'● ' + r.controlStatus"></span></td>
                                                </tr>
                                            </template>
                                        </tbody>
                                    </table>
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
