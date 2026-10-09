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
        <meta name="active-submenu" content="ScheduledRuns.jsp">
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%@ include file="include/global/dependenciesInclusions.html" %>

        <script type="text/javascript" src="js/pages/insightsShared.js?v=${appVersion}"></script>
        <script type="text/javascript" src="js/pages/ScheduledRuns.js?v=${appVersion}"></script>
        <link rel="stylesheet" type="text/css" href="css/pages/InsightsShared.css?v=${appVersion}"/>

        <title id="pageTitle">Scheduled Runs</title>
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

            <div x-data="scheduledRuns()" class="v2in-page" id="scheduledRunsRoot">

                <div class="v2in-pagetitle">
                    <h1 class="page-title-line" x-text="t('title')"></h1>
                </div>

                <!-- Sticky header: period, campaign filter, refresh, new schedule -->
                <div class="crb_card v2in-card v2in-header" :style="$store.rightPanel.open ? { top: '0px' } : {}">
                    <div class="flex items-center gap-3 flex-wrap">
                        <div class="v2in-field">
                            <span class="v2in-fieldlabel" x-text="t('history')"></span>
                            <div class="v2in-seg">
                                <template x-for="d in [1, 7, 30]" :key="d">
                                    <button type="button" class="v2in-seg-item" :class="days === d ? 'v2in-seg-item--on' : ''"
                                            @click="setDays(d)" x-text="d + 'd'"></button>
                                </template>
                            </div>
                        </div>
                        <div class="v2in-field">
                            <span class="v2in-fieldlabel" x-text="t('campaign')"></span>
                            <div style="width: 240px"
                                 x-data="multiSelectDropdown({ id: 'sr-campaign', labelField: 'name', valueField: 'name', returnType: 'value',
                                                               placeholder: t('allcampaigns'), loader: () => campaignItems() })"></div>
                        </div>
                        <span class="v2in-dim text-xs" x-show="loadedAt" x-text="t('refreshed', ago(loadedAt)) + (serverTimeZone ? ' - ' + t('cronzone', serverTimeZone) : '')"></span>
                        <div class="flex-1"></div>
                        <button type="button" class="v2in-btn" @click="load()" :disabled="loading" x-text="t('refresh')"></button>
                        <button type="button" class="v2in-btn v2in-btn--primary" @click="toggleForm()">
                            <span x-text="formOpen ? t('close') : t('newschedule')"></span>
                        </button>
                    </div>
                </div>

                <!-- New schedule -->
                <div class="crb_card v2in-card" x-show="formOpen" x-cloak>
                    <div class="v2in-card-head"><span class="v2in-card-title" x-text="t('newschedule')"></span></div>
                    <div class="v2in-card-body">
                        <div class="flex items-end gap-3 flex-wrap">
                            <div class="v2in-field">
                                <span class="v2in-fieldlabel" x-text="t('campaign')"></span>
                                <select class="v2in-input" x-model="form.name" style="min-width: 220px">
                                    <option value="" x-text="t('pickcampaign')"></option>
                                    <template x-for="c in campaigns" :key="c.campaign">
                                        <option :value="c.campaign" x-text="c.campaign"></option>
                                    </template>
                                </select>
                            </div>
                            <div class="v2in-field" style="flex: 1; min-width: 260px">
                                <span class="v2in-fieldlabel" x-text="t('cronlabel')"></span>
                                <input type="text" class="v2in-input" x-model="form.cronDefinition" placeholder="0 0 2 * * ?" spellcheck="false">
                            </div>
                            <div class="v2in-field" style="flex: 1; min-width: 220px">
                                <span class="v2in-fieldlabel" x-text="t('description')"></span>
                                <input type="text" class="v2in-input" x-model="form.description" maxlength="200">
                            </div>
                            <button type="button" class="v2in-btn v2in-btn--primary" @click="create()" :disabled="saving" x-text="t('create')"></button>
                        </div>
                        <div class="flex items-center gap-2 flex-wrap mt-3">
                            <span class="v2in-dim text-xs" x-text="t('presets')"></span>
                            <template x-for="p in presets" :key="p.cron">
                                <button type="button" class="v2in-pill" :class="form.cronDefinition === p.cron ? 'v2in-pill--on' : ''"
                                        @click="form.cronDefinition = p.cron" x-text="t(p.label)" :title="p.cron"></button>
                            </template>
                        </div>
                    </div>
                </div>

                <!-- Error from the last load -->
                <div class="crb_card v2in-card" x-show="error" x-cloak>
                    <span class="v2in-chip v2in-chip--ko" x-text="error"></span>
                </div>

                <!-- KPIs (same cards as the homepage) -->
                <div class="grid grid-cols-1 md:grid-cols-3 lg:grid-cols-5 gap-6" x-init="$nextTick(() => window.lucide && lucide.createIcons())">
                    <div class="crb_card">
                        <div class="flex items-center space-x-3 mb-2">
                            <div class="w-8 h-8 rounded-xl flex items-center justify-center bg-blue-500">
                                <i data-lucide="calendar-clock" class="w-4 h-4 text-white"></i>
                            </div>
                            <span class="text-sm font-semibold" x-text="t('kpiactive')"></span>
                        </div>
                        <div class="text-2xl font-bold" x-text="kpis.active"></div>
                        <div class="text-gray-500 text-sm mt-1 truncate" x-text="t('kpiactivesub', kpis.total, kpis.total - kpis.active)"></div>
                    </div>
                    <div class="crb_card">
                        <div class="flex items-center space-x-3 mb-2">
                            <div class="w-8 h-8 rounded-xl flex items-center justify-center bg-purple-500">
                                <i data-lucide="timer" class="w-4 h-4 text-white"></i>
                            </div>
                            <span class="text-sm font-semibold" x-text="t('kpinext')"></span>
                        </div>
                        <div class="text-2xl font-bold" x-text="kpis.nextIn"></div>
                        <div class="text-gray-500 text-sm mt-1 truncate" x-text="kpis.nextName"></div>
                    </div>
                    <div class="crb_card">
                        <div class="flex items-center space-x-3 mb-2">
                            <div class="w-8 h-8 rounded-xl flex items-center justify-center bg-orange-500">
                                <i data-lucide="play" class="w-4 h-4 text-white"></i>
                            </div>
                            <span class="text-sm font-semibold" x-text="t('kpifired')"></span>
                        </div>
                        <div class="text-2xl font-bold" x-text="kpis.fired"></div>
                        <div class="text-gray-500 text-sm mt-1 truncate" x-text="t('kpifiredsub', days)"></div>
                    </div>
                    <div class="crb_card">
                        <div class="flex items-center space-x-3 mb-2">
                            <div class="w-8 h-8 rounded-xl flex items-center justify-center bg-green-500">
                                <i data-lucide="circle-check" class="w-4 h-4 text-white"></i>
                            </div>
                            <span class="text-sm font-semibold" x-text="t('kpisuccess')"></span>
                        </div>
                        <div class="text-2xl font-bold" :style="kpis.successRate !== null ? ('color:' + (kpis.successRate >= 95 ? 'var(--crb-green-color)' : (kpis.successRate >= 80 ? 'var(--crb-orange-color)' : 'var(--crb-red-color)'))) : ''" x-text="kpis.successRate !== null ? kpis.successRate + '%' : '-'"></div>
                        <div class="text-gray-500 text-sm mt-1 truncate" x-text="t('kpisuccesssub')"></div>
                    </div>
                    <div class="crb_card">
                        <div class="flex items-center space-x-3 mb-2">
                            <div class="w-8 h-8 rounded-xl flex items-center justify-center bg-red-500">
                                <i data-lucide="circle-alert" class="w-4 h-4 text-white"></i>
                            </div>
                            <span class="text-sm font-semibold" x-text="t('kpierrors')"></span>
                        </div>
                        <div class="text-2xl font-bold" :style="kpis.errors > 0 ? 'color: var(--crb-red-color)' : ''" x-text="kpis.errors"></div>
                        <div class="text-gray-500 text-sm mt-1 truncate" x-text="kpis.lastError ? t('kpierrorslast', kpis.lastError) : t('kpierrorsnone')"></div>
                    </div>
                </div>

                <div class="grid gap-4" style="grid-template-columns: repeat(auto-fit, minmax(420px, 1fr))">

                    <!-- Upcoming -->
                    <div class="crb_card v2in-card">
                        <div class="v2in-card-head"><span class="v2in-card-title" x-text="t('upcoming')"></span></div>
                        <div class="v2in-card-body">
                            <div class="v2in-empty" x-show="upcomingList.length === 0" x-text="t('upcomingempty')"></div>
                            <div class="v2in-table-scroll" x-show="upcomingList.length > 0">
                                <table class="v2in-table">
                                    <thead><tr><th x-text="t('when')"></th><th x-text="t('in')"></th><th x-text="t('campaign')"></th></tr></thead>
                                    <tbody>
                                        <template x-for="u in upcomingList" :key="u.schedulerId + '-' + u.time">
                                            <tr>
                                                <td x-text="fmt(u.time)"></td>
                                                <td class="v2in-dim" x-text="until(u.time)"></td>
                                                <td class="v2in-strong" x-text="u.name"></td>
                                            </tr>
                                        </template>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>

                    <!-- Schedules -->
                    <div class="crb_card v2in-card">
                        <div class="v2in-card-head"><span class="v2in-card-title" x-text="t('schedules')"></span></div>
                        <div class="v2in-card-body">
                            <div class="v2in-empty" x-show="entryRows.length === 0" x-text="t('schedulesempty')"></div>
                            <div class="v2in-table-scroll" x-show="entryRows.length > 0">
                                <table class="v2in-table">
                                    <thead><tr><th x-text="t('campaign')"></th><th x-text="t('cron')"></th><th x-text="t('state')"></th><th x-text="t('lastrun')"></th><th x-text="t('success')"></th><th></th></tr></thead>
                                    <tbody>
                                        <template x-for="e in entryRows" :key="e.id">
                                            <tr>
                                                <td>
                                                    <span class="v2in-strong" x-text="e.name"></span>
                                                    <div class="v2in-dim text-xs" x-show="e.description" x-text="e.description"></div>
                                                </td>
                                                <td>
                                                    <code x-text="e.cronDefinition"></code>
                                                    <div class="text-xs" style="color: var(--crb-red-color)" x-show="e.cronError" x-text="e.cronError"></div>
                                                </td>
                                                <td>
                                                    <span class="v2in-chip" :class="e.active ? 'v2in-chip--ok' : 'v2in-chip--warn'" x-text="e.active ? t('active') : t('paused')"></span>
                                                </td>
                                                <td class="v2in-dim" x-text="e.lastRun ? ago(e.lastRun) : '-'" :title="e.lastRun ? fmt(e.lastRun) : ''"></td>
                                                <td>
                                                    <span x-show="e.rate !== null" x-text="e.rate + '% (' + e.ok + '/' + (e.ok + e.ko) + ')'"
                                                          :style="'color:' + (e.rate >= 95 ? 'var(--crb-green-color)' : (e.rate >= 80 ? 'var(--crb-orange-color)' : 'var(--crb-red-color)'))"></span>
                                                    <span class="v2in-dim" x-show="e.rate === null">-</span>
                                                </td>
                                                <td style="white-space: nowrap">
                                                    <button type="button" class="v2in-btn v2in-btn--xs" @click="setActive(e, !e.active)"
                                                            x-text="e.active ? t('pause') : t('resume')"></button>
                                                    <button type="button" class="v2in-btn v2in-btn--xs" @click="remove(e)" x-text="t('delete')"></button>
                                                </td>
                                            </tr>
                                        </template>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- History -->
                <div class="crb_card v2in-card">
                    <div class="v2in-card-head">
                        <span class="v2in-card-title" x-text="t('historytitle', historyRows.length)"></span>
                        <div class="flex-1"></div>
                        <template x-for="s in ['TRIGGERED', 'ERROR', 'IGNORED']" :key="s">
                            <button type="button" class="v2in-pill" :class="filterStatus === s ? 'v2in-pill--on' : ''"
                                    @click="filterStatus = (filterStatus === s ? '' : s)" x-text="s"></button>
                        </template>
                    </div>
                    <div class="v2in-card-body">
                        <div class="v2in-empty" x-show="historyRows.length === 0" x-text="t('historyempty')"></div>
                        <div class="v2in-table-scroll" x-show="historyRows.length > 0">
                            <table class="v2in-table">
                                <thead><tr><th x-text="t('scheduled')"></th><th x-text="t('campaign')"></th><th x-text="t('status')"></th><th x-text="t('detail')"></th></tr></thead>
                                <tbody>
                                    <template x-for="h in historyRows.slice(0, historyLimit)" :key="h.id">
                                        <tr>
                                            <td x-text="fmt(h.scheduledDate)" style="white-space: nowrap"></td>
                                            <td class="v2in-strong" x-text="h.name"></td>
                                            <td><span class="v2in-chip" :class="statusClass(h.status)" x-text="h.status"></span></td>
                                            <td class="v2in-dim text-xs" x-text="h.comment"></td>
                                        </tr>
                                    </template>
                                </tbody>
                            </table>
                        </div>
                        <div class="mt-3" x-show="historyRows.length > historyLimit">
                            <button type="button" class="v2in-btn" @click="historyLimit += 100"
                                    x-text="t('showmore', historyRows.length - historyLimit)"></button>
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
