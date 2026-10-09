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
        <meta name="active-submenu" content="ReportingMonitorApi.jsp">
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%@ include file="include/global/dependenciesInclusions.html" %>

        <script type="text/javascript" src="js/pages/insightsShared.js?v=${appVersion}"></script>
        <script type="text/javascript" src="js/pages/ReportingMonitorApi.js?v=${appVersion}"></script>
        <link rel="stylesheet" type="text/css" href="css/pages/InsightsShared.css?v=${appVersion}"/>
        <link rel="stylesheet" type="text/css" href="css/pages/ReportingMonitorWeb.css?v=${appVersion}"/>

        <title id="pageTitle">API Monitor</title>
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

            <div x-data="apiMonitor()" class="v2in-page" id="apiMonitorRoot">

                <div class="v2in-pagetitle">
                    <h1 class="page-title-line" x-text="t('title')"></h1>
                </div>

                <!-- Sticky header: favorites (tabs), service, period and filters -->
                <div class="crb_card v2in-card v2in-header" :style="$store.rightPanel.open ? { top: '0px' } : {}">
                    <div class="flex flex-col gap-3">

                        <!-- Tabs: one per favorite service, loaded when clicked -->
                        <div class="wm-tabs" x-show="favorites.length || adHoc">
                            <template x-for="f in favorites" :key="f">
                                <div class="wm-tab" :class="f === current ? 'wm-tab--on' : ''" @click="f !== current && open(f)" :title="f">
                                    <i data-lucide="star" class="w-3.5 h-3.5"></i>
                                    <span class="truncate" x-text="f"></span>
                                    <button type="button" class="wm-tab-x" @click.stop="toggleFavorite(f)" :title="t('removefavorite')">&times;</button>
                                </div>
                            </template>
                            <div class="wm-tab wm-tab--on wm-tab--adhoc" x-show="adHoc" x-cloak>
                                <i data-lucide="search" class="w-3.5 h-3.5"></i>
                                <span class="truncate" x-text="current"></span>
                            </div>
                        </div>

                        <div class="flex items-end gap-3 flex-wrap">
                            <!-- Service picker -->
                            <div class="v2in-field relative" @click.outside="svcDdOpen = false">
                                <span class="v2in-fieldlabel" x-text="t('service')"></span>
                                <div class="flex items-center gap-2">
                                    <button type="button" class="v2in-picker" style="min-width: 300px; max-width: 460px"
                                            :class="[svcDdOpen ? 'v2in-picker--active' : '', !current ? 'v2in-picker--empty' : '']"
                                            @click="svcDdOpen = !svcDdOpen; if (svcDdOpen) { svcSearch = ''; $nextTick(() => $refs.svcSearchInput && $refs.svcSearchInput.focus()) }">
                                        <i data-lucide="plug" class="w-4 h-4 v2in-picker-icon"></i>
                                        <span class="v2in-picker-value" x-text="current || t('chooseservice')"></span>
                                        <span class="flex-1"></span>
                                        <svg class="w-3.5 h-3.5 v2in-picker-chevron" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path d="M19 9l-7 7-7-7"/></svg>
                                    </button>
                                    <button type="button" class="wm-star" :class="current && isFavorite ? 'wm-star--on' : ''" x-show="current" x-cloak
                                            @click="toggleFavorite()" :title="current && isFavorite ? t('removefavorite') : t('addfavorite')">
                                        <i data-lucide="star" class="w-4 h-4"></i>
                                    </button>
                                </div>
                                <div x-show="svcDdOpen" x-cloak class="v2in-dd v2in-dd--right" style="min-width: 340px">
                                    <div class="v2in-dd-search">
                                        <input type="text" x-ref="svcSearchInput" x-model="svcSearch" :placeholder="t('searchservice')" class="v2in-input">
                                    </div>
                                    <div class="v2in-dd-list">
                                        <template x-for="s in filteredServices" :key="s.service">
                                            <button type="button" class="v2in-dd-item" @click="pick(s.service)">
                                                <span class="v2in-strong truncate" x-text="s.service"></span>
                                                <span class="flex-1"></span>
                                                <span class="v2in-dim text-xs shrink-0" x-text="t('calls', s.calls)"></span>
                                            </button>
                                        </template>
                                        <div x-show="filteredServices.length === 0" class="v2in-empty px-3 py-2 text-center" x-text="t('noservice')"></div>
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

                        <div class="grid grid-cols-1 gap-6 lg:grid-cols-3 wm-top">

                            <!-- Response time -->
                            <div class="crb_card lg:col-span-2">
                                <div class="flex flex-wrap items-start justify-between gap-3">
                                    <div class="wm-head">
                                        <div class="wm-tile"><i data-lucide="timer" class="w-5 h-5 text-blue-500"></i></div>
                                        <div>
                                            <div class="wm-title" x-text="t('responsetime')"></div>
                                            <div class="wm-sub" x-text="current + ' - ' + t('callscount', rows.length) + (groupSize > 1 ? ' - ' + t('grouped', groupSize) : '')"></div>
                                        </div>
                                    </div>
                                    <div class="v2in-legend">
                                        <span class="v2in-legend-item"><span class="v2in-legend-dot" style="background:#2c7be5"></span><span x-text="t('average')"></span></span>
                                        <span class="v2in-legend-item" x-show="groupSize > 1"><span class="v2in-legend-dot" style="background:#8b5cf6"></span><span x-text="t('p95')"></span></span>
                                        <span class="v2in-legend-item"><span class="v2in-legend-dot" style="background:#e63757"></span><span x-text="t('errorlegend')"></span></span>
                                    </div>
                                </div>

                                <div class="mt-4 flex flex-col gap-4 md:flex-row md:items-end">
                                    <div class="shrink-0">
                                        <div class="wm-big" x-text="fmtMs(kpis.avg)"></div>
                                        <div class="mt-1 text-xs font-semibold" x-show="delta('avg')" :style="'color:' + (delta('avg') && delta('avg').good ? '#00d27a' : '#e63757')" x-text="delta('avg') ? delta('avg').text : ''"></div>
                                        <div class="mt-1 text-xs v2in-dim" x-show="!delta('avg')" x-text="t('novsprevious')"></div>
                                    </div>
                                    <div class="flex-1 min-w-0" x-ref="timeChart" @click="onChartClick($event)" x-html="timeChart"></div>
                                </div>

                                <div class="mt-4">
                                    <div class="v2in-fieldlabel mb-2" x-text="t('statusperexecution')"></div>
                                    <div class="wm-strip">
                                        <template x-for="r in strip" :key="r.id">
                                            <button type="button" class="wm-strip-item" :class="selected && selected.id === r.id ? 'wm-strip-item--on' : ''"
                                                    :style="'background:' + statusColor(r.status)" :title="fmtDate(r.start) + ' - ' + r.httpCode + ' - ' + r.durationMs + ' ms - ' + r.status"
                                                    @click="select(r)"></button>
                                        </template>
                                    </div>
                                </div>
                            </div>

                            <!-- KPIs -->
                            <div class="flex flex-col gap-6">
                                <div class="grid grid-cols-2 gap-3">
                                    <div class="crb_card wm-kpi"><div class="wm-kpi-label" x-text="t('successrate')"></div><div class="wm-kpi-value" style="color:#00d27a" x-text="kpis.rate === null ? '-' : kpis.rate.toFixed(1) + '%'"></div></div>
                                    <div class="crb_card wm-kpi"><div class="wm-kpi-label" x-text="t('errors')"></div><div class="wm-kpi-value" style="color:#e63757" x-text="kpis.errors"></div></div>
                                    <div class="crb_card wm-kpi"><div class="wm-kpi-label" x-text="t('p50')"></div><div class="wm-kpi-value" style="color:#2c7be5" x-text="fmtMs(kpis.p50)"></div></div>
                                    <div class="crb_card wm-kpi">
                                        <div class="wm-kpi-label" x-text="t('p95')"></div>
                                        <div class="wm-kpi-value" style="color:#8b5cf6" x-text="fmtMs(kpis.p95)"></div>
                                        <div class="text-xs font-semibold" x-show="delta('p95')" :style="'color:' + (delta('p95') && delta('p95').good ? '#00d27a' : '#e63757')" x-text="delta('p95') ? delta('p95').text : ''"></div>
                                    </div>
                                </div>
                                <div class="crb_card wm-kpi"><div class="wm-kpi-label" x-text="t('callsperiod')"></div><div class="wm-kpi-value" x-text="kpis.n"></div></div>

                                <!-- HTTP codes -->
                                <div class="crb_card">
                                    <div class="wm-head">
                                        <div class="wm-tile"><i data-lucide="globe" class="w-5 h-5 text-green-500"></i></div>
                                        <div>
                                            <div class="wm-title" x-text="t('httpcodes')"></div>
                                            <div class="wm-sub" x-text="t('httpcodessub')"></div>
                                        </div>
                                    </div>
                                    <div class="wm-bar mt-4">
                                        <template x-for="c in httpCodes" :key="c.key">
                                            <div :style="'width:' + c.pct + '%;background:' + c.color"></div>
                                        </template>
                                    </div>
                                    <div class="mt-3 flex flex-col gap-2 text-sm">
                                        <template x-for="c in httpCodes" :key="c.key">
                                            <div class="flex items-center justify-between">
                                                <span class="flex items-center gap-2"><span class="v2in-legend-dot" :style="'background:' + c.color"></span><span x-text="c.label"></span></span>
                                                <span class="v2in-dim" x-text="c.n + ' - ' + Math.round(c.pct) + '%'"></span>
                                            </div>
                                        </template>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div class="grid grid-cols-1 gap-6 lg:grid-cols-3 wm-top">

                            <!-- Distribution -->
                            <div class="crb_card">
                                <div class="wm-head">
                                    <div class="wm-tile"><i data-lucide="bar-chart-3" class="w-5 h-5 text-orange-500"></i></div>
                                    <div>
                                        <div class="wm-title" x-text="t('distribution')"></div>
                                        <div class="wm-sub" x-text="t('distributionsub')"></div>
                                    </div>
                                </div>
                                <div class="mt-4 flex flex-col gap-3 text-sm">
                                    <template x-for="d in distribution" :key="d.label">
                                        <div>
                                            <div class="flex justify-between gap-2"><span x-text="d.label"></span><span class="v2in-dim" x-text="d.n + ' - ' + d.share + '%'"></span></div>
                                            <div class="wm-meter"><div :style="'width:' + d.pct + '%;background:#2c7be5'"></div></div>
                                        </div>
                                    </template>
                                </div>
                            </div>

                            <!-- Consumers -->
                            <div class="crb_card">
                                <div class="wm-head">
                                    <div class="wm-tile"><i data-lucide="share-2" class="w-5 h-5 text-purple-500"></i></div>
                                    <div>
                                        <div class="wm-title" x-text="t('consumers')"></div>
                                        <div class="wm-sub" x-text="t('consumerssub')"></div>
                                    </div>
                                </div>
                                <div class="mt-4 flex flex-col gap-3 text-sm">
                                    <template x-for="c in consumers" :key="c.name">
                                        <div>
                                            <div class="flex justify-between gap-2"><span class="truncate" :title="c.name" x-text="c.name"></span>
                                                <span class="shrink-0" :class="c.errors ? '' : 'v2in-dim'" :style="c.errors ? 'color:#e63757' : ''" x-text="c.errors + ' / ' + c.n"></span></div>
                                            <div class="wm-meter"><div :style="'width:' + Math.max(c.rate, c.errors ? 3 : 0) + '%;background:#e63757'"></div></div>
                                        </div>
                                    </template>
                                    <div class="v2in-empty" x-show="consumers.length === 0" x-text="t('noconsumer')"></div>
                                </div>
                            </div>

                            <!-- Selected call -->
                            <div class="crb_card" x-show="selected" x-effect="loadFiles(selected)">
                                <div class="wm-head">
                                    <div class="wm-tile"><i data-lucide="send" class="w-5 h-5 text-blue-500"></i></div>
                                    <div class="min-w-0">
                                        <div class="wm-title" x-text="selected ? t('call', selected.id) : ''"></div>
                                        <div class="wm-sub truncate" x-text="selected ? fmtDate(selected.start) + ' - ' + selected.environment + ' - ' + selected.country : ''"></div>
                                    </div>
                                    <div class="flex-1"></div>
                                    <span class="v2in-chip" :style="selected ? 'color:' + statusColor(selected.status) : ''" x-text="selected ? '\u25CF ' + selected.status : ''"></span>
                                </div>
                                <template x-if="selected">
                                    <div class="mt-4 grid grid-cols-2 gap-3">
                                        <div class="wm-cell"><div class="wm-kpi-label" x-text="t('duration')"></div><div class="wm-cell-value" x-text="fmtMs(selected.durationMs)"></div></div>
                                        <div class="wm-cell"><div class="wm-kpi-label" x-text="t('http')"></div><div class="wm-cell-value" :style="'color:' + httpColor(selected.httpCode)" x-text="selected.httpCode || '-'"></div></div>
                                        <div class="wm-cell"><div class="wm-kpi-label" x-text="t('size')"></div><div class="wm-cell-value" x-text="fmtSize(selected.responseSize)"></div></div>
                                        <div class="wm-cell"><div class="wm-kpi-label" x-text="t('method')"></div><div class="wm-cell-value" x-text="selected.method || selected.type"></div></div>
                                    </div>
                                </template>
                                <div class="mt-3 text-xs v2in-dim truncate" x-text="selected ? (selected.test || '-') + ' / ' + (selected.testcase || '-') : ''"></div>
                                <div class="mt-3 flex items-center gap-2 flex-wrap">
                                    <template x-for="f in filesOf(selected)" :key="f.fileDesc">
                                        <a class="v2in-btn" :href="fileUrl(selected, f)" target="_blank" rel="noopener">
                                            <i data-lucide="file-json" class="w-3.5 h-3.5"></i><span x-text="fileLabel(f)"></span>
                                        </a>
                                    </template>
                                    <button type="button" class="v2in-btn" @click="openExecution(selected)">
                                        <i data-lucide="external-link" class="w-3.5 h-3.5"></i><span x-text="t('open')"></span>
                                    </button>
                                </div>
                                <div class="mt-2 text-xs v2in-dim" x-show="selected && !selected.fileLevel" x-text="t('nofiles')"></div>
                            </div>
                        </div>

                        <!-- Latest calls -->
                        <div class="crb_card v2in-card">
                            <div class="v2in-card-head">
                                <span class="v2in-card-title" x-text="t('latest')"></span>
                                <span class="v2in-count" x-text="latest.length"></span>
                            </div>
                            <div class="v2in-card-body">
                                <div class="v2in-table-scroll">
                                    <table class="v2in-table">
                                        <thead><tr>
                                            <th x-text="t('when')"></th><th x-text="t('testcase')"></th><th x-text="t('environment')"></th><th x-text="t('country')"></th>
                                            <th class="v2in-num" x-text="t('http')"></th><th class="v2in-num" x-text="t('duration')"></th><th class="v2in-num" x-text="t('size')"></th><th x-text="t('status')"></th>
                                        </tr></thead>
                                        <tbody>
                                            <template x-for="r in latest" :key="r.id">
                                                <tr class="v2in-row-click" :class="selected && selected.id === r.id ? 'wm-row--on' : ''" @click="select(r)" @dblclick="openExecution(r)">
                                                    <td style="white-space: nowrap" x-text="fmtDate(r.start)"></td>
                                                    <td><span class="v2in-strong" x-text="r.testcase || '-'"></span> <span class="v2in-dim" x-text="r.test"></span></td>
                                                    <td x-text="r.environment"></td><td x-text="r.country"></td>
                                                    <td class="v2in-num" :style="'color:' + httpColor(r.httpCode)" x-text="r.httpCode || '-'"></td>
                                                    <td class="v2in-num" x-text="fmtMs(r.durationMs)"></td><td class="v2in-num v2in-dim" x-text="fmtSize(r.responseSize)"></td>
                                                    <td><span class="v2in-chip" :style="'color:' + statusColor(r.status)" x-text="'\u25CF ' + r.status"></span></td>
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
