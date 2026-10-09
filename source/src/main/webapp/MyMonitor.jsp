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
        <meta name="active-submenu" content="MyMonitor.jsp">
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%@ include file="include/global/dependenciesInclusions.html" %>

        <script type="text/javascript" src="js/pages/insightsShared.js?v=${appVersion}"></script>
        <!-- The widgets are modules: a script (component + registration in the catalog) and a body (html) each -->
        <script type="text/javascript" src="js/widgets/widgetRegistry.js?v=${appVersion}"></script>
        <script type="text/javascript" src="js/widgets/widgetKpi.js?v=${appVersion}"></script>
        <script type="text/javascript" src="js/widgets/widgetExec.js?v=${appVersion}"></script>
        <script type="text/javascript" src="js/widgets/widgetData.js?v=${appVersion}"></script>
        <script type="text/javascript" src="js/widgets/widgetTimeline.js?v=${appVersion}"></script>
        <script type="text/javascript" src="js/widgets/widgetPie.js?v=${appVersion}"></script>
        <script type="text/javascript" src="js/widgets/widgetRadar.js?v=${appVersion}"></script>
        <script type="text/javascript" src="js/widgets/widgetText.js?v=${appVersion}"></script>
        <script type="text/javascript" src="js/pages/MyMonitor.js?v=${appVersion}"></script>
        <link rel="stylesheet" type="text/css" href="css/pages/InsightsShared.css?v=${appVersion}"/>
        <link rel="stylesheet" type="text/css" href="css/pages/ReportingMonitorWeb.css?v=${appVersion}"/>
        <link rel="stylesheet" type="text/css" href="css/pages/MyMonitor.css?v=${appVersion}"/>

        <title id="pageTitle">My Monitor</title>
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

            <div x-data="myMonitor()" class="v2in-page" id="myMonitorRoot">

                <div class="v2in-pagetitle" x-show="!tvMode">
                    <h1 class="page-title-line flex items-center gap-3"><span x-text="t('title')"></span>
                        <span class="v2in-chip v2in-chip--warn" :title="t('experimentaltip')" x-text="t('experimental')"></span></h1>
                </div>

                <!-- Sticky header: dashboards (tabs), period and edition -->
                <div class="crb_card v2in-card v2in-header" x-show="!tvMode" :style="$store.rightPanel.open ? { top: '0px' } : {}">
                    <div class="flex flex-col gap-3">

                        <!-- Tabs: one per dashboard, the starred one opens first -->
                        <div class="wm-tabs">
                            <template x-for="d in dashboards" :key="d.id">
                                <div class="wm-tab" :class="d.id === currentId ? 'wm-tab--on' : ''" @click="d.id !== currentId && open(d)">
                                    <button type="button" class="wm-tab-star" :class="d.favorite ? 'wm-tab-star--on' : ''" @click.stop="toggleFavorite(d)"
                                            :title="d.favorite ? t('removefavorite') : t('addfavorite')">
                                        <i data-lucide="star" class="w-3.5 h-3.5"></i>
                                    </button>
                                    <span class="truncate" x-text="d.name"></span>
                                </div>
                            </template>
                            <button type="button" class="wm-tab wm-tab--adhoc" x-show="editMode" x-cloak @click="addDashboard()">
                                <i data-lucide="plus" class="w-3.5 h-3.5"></i><span x-text="t('newdashboardbtn')"></span>
                            </button>
                        </div>

                        <div class="flex items-end gap-3 flex-wrap">
                            <!-- Name of the dashboard (edit mode) -->
                            <div class="v2in-field" x-show="editMode && current" x-cloak>
                                <span class="v2in-fieldlabel" x-text="t('name')"></span>
                                <input type="text" class="v2in-input" style="min-height: 42px; min-width: 220px" x-model="current.name" @change="save()">
                            </div>

                            <!-- Period of the widgets that follow one -->
                            <div class="v2in-field">
                                <span class="v2in-fieldlabel" x-text="t('period')"></span>
                                <div class="v2in-seg" style="min-height: 42px; align-items: center">
                                    <template x-for="p in periods" :key="p.days">
                                        <button type="button" class="v2in-seg-item" :class="periodDays === p.days ? 'v2in-seg-item--on' : ''" @click="setPeriod(p.days)" x-text="p.label"></button>
                                    </template>
                                </div>
                            </div>

                            <div class="flex-1"></div>
                            <span class="v2in-dim text-xs" x-show="loadedAt" x-text="t('refreshed', ago(loadedAt))"></span>

                            <!-- Add a widget (edit mode) -->
                            <div class="relative" x-show="editMode" x-cloak @click.outside="pickerOpen = false">
                                <button type="button" class="v2in-btn" :class="pickerOpen ? 'v2in-btn--on' : ''" @click="pickerOpen = !pickerOpen">
                                    <i data-lucide="plus" class="w-3.5 h-3.5"></i><span x-text="t('addwidget')"></span>
                                </button>
                                <div x-show="pickerOpen" x-cloak class="v2in-dd v2in-dd--right" style="min-width: 320px">
                                    <div class="v2in-dd-list">
                                        <template x-for="c in catalog" :key="c.type">
                                            <button type="button" class="v2in-dd-item" @click="addWidget(c.type)">
                                                <span class="mm-tile mm-tile--sm" :style="'background:' + colors[c.color]"><i :data-lucide="c.icon" class="w-3.5 h-3.5 text-white"></i></span>
                                                <span class="flex flex-col items-start min-w-0">
                                                    <span class="v2in-strong" x-text="t('widget_' + c.type)"></span>
                                                    <span class="v2in-dim text-xs" x-text="t('widget_' + c.type + '_desc')"></span>
                                                </span>
                                            </button>
                                        </template>
                                    </div>
                                </div>
                            </div>
                            <button type="button" class="v2in-btn" x-show="editMode" x-cloak @click="removeDashboard()">
                                <i data-lucide="trash-2" class="w-3.5 h-3.5"></i><span x-text="t('deletedashboard')"></span>
                            </button>
                            <button type="button" class="v2in-btn" x-show="!editMode" @click="refresh()">
                                <i data-lucide="refresh-cw" class="w-3.5 h-3.5"></i><span x-text="t('refresh')"></span>
                            </button>
                            <button type="button" class="v2in-btn" :class="editMode ? 'v2in-btn--primary' : ''" @click="toggleEdit()">
                                <i :data-lucide="editMode ? 'check' : 'layout-dashboard'" class="w-3.5 h-3.5"></i><span x-text="editMode ? t('done') : t('edit')"></span>
                            </button>
                            <button type="button" class="v2in-btn v2in-btn--primary" @click="enterTv()" :title="t('tvtip')">
                                <i data-lucide="tv" class="w-3.5 h-3.5"></i><span x-text="t('tvmode')"></span>
                            </button>
                        </div>
                    </div>
                </div>

                <!-- Stage: what the TV mode expands to the whole screen -->
                <div id="mmStage" :class="tvMode ? 'v2mo-tv' : ''">

                <div class="flex items-center gap-3 mb-3" x-show="tvMode" x-cloak>
                    <span class="v2in-title" style="font-size: 18px" x-text="t('title') + (current ? ' - ' + current.name : '')"></span>
                    <span class="v2in-chip v2in-chip--warn" x-text="t('experimental')"></span>
                    <span class="v2in-dim text-xs" x-text="t('tvrefresh', ago(loadedAt))"></span>
                    <div class="flex-1"></div>
                    <button type="button" class="v2in-btn" @click="exitTv()" x-text="t('exittv')"></button>
                </div>

                <div class="crb_card v2in-card" x-show="widgets.length === 0" x-cloak>
                    <div class="v2in-card-body"><div class="v2in-empty" x-text="t('empty')"></div></div>
                </div>

                <!-- Grid: 12 columns, the widgets are placed by their cell (x, y) and size (w, h) -->
                <div class="mm-grid" x-ref="grid" :class="editMode ? 'mm-grid--edit' : ''" :style="gridStyle()">
                    <template x-for="w in widgets" :key="w.id">
                        <div class="mm-cell" :class="drag && drag.w.id === w.id ? 'mm-cell--drag' : ''" :style="cellStyle(w)">
                            <div class="crb_card mm-card" :class="[editMode ? 'mm-card--edit' : '', w.type === 'text' ? 'mm-card--bare' : '']" x-data="widgetShell(w)">

                                <!-- Header: icon, title; in edit mode it is the handle to move the widget -->
                                <div class="mm-head" :class="editMode ? 'mm-handle' : ''" @pointerdown="!$event.target.closest('button') && startDrag($event, w, 'move')">
                                    <template x-for="i in [w.icon]" :key="i">
                                        <span class="mm-tile" :style="'background:' + hex(w.color)"><i :data-lucide="i" class="w-4 h-4 text-white"></i></span>
                                    </template>
                                    <span class="mm-title truncate" :class="w.type === 'text' ? 'mm-title--text' : ''" x-text="w.type === 'text' ? (w.content || w.title || label) : (w.title || label)"></span>
                                    <span class="flex-1"></span>
                                    <button type="button" class="mm-iconbtn" x-show="editMode" x-cloak @click="cfg = !cfg" :class="cfg ? 'mm-iconbtn--on' : ''" :title="t('configure')">
                                        <i data-lucide="settings-2" class="w-3.5 h-3.5"></i>
                                    </button>
                                    <button type="button" class="mm-iconbtn mm-iconbtn--del" x-show="editMode" x-cloak @click="deleteWidget(w.id)" :title="t('removewidget')">
                                        <i data-lucide="x" class="w-3.5 h-3.5"></i>
                                    </button>
                                </div>

                                <!-- Settings: shown as a popin over the page (the card is too small for them) -->
                                <div class="mm-backdrop" x-show="cfg" x-cloak @click="cfg = false; save()"></div>

                                <div class="mm-body" :class="cfg ? 'mm-body--cfg' : ''" @keydown.escape.window="cfg && (cfg = false, save())">
                                    <div class="mm-popin-title" x-show="cfg" x-cloak>
                                        <span class="mm-tile" :style="'background:' + hex(w.color)"><i :data-lucide="w.icon" class="w-4 h-4 text-white"></i></span>
                                        <span class="mm-title truncate" x-text="w.title || label"></span>
                                    </div>

                                    <!-- Settings common to every widget -->
                                    <div class="mm-field-list" x-show="cfg" x-cloak>
                                        <div class="mm-field">
                                            <span class="v2in-fieldlabel" x-text="t('widgettitle')"></span>
                                            <input type="text" class="v2in-input" x-model="w.title" :placeholder="label">
                                        </div>
                                        <div class="mm-field">
                                            <span class="v2in-fieldlabel" x-text="t('icon')"></span>
                                            <select class="v2in-input" x-model="w.icon">
                                                <template x-for="i in icons" :key="i"><option :value="i" :selected="i === w.icon" x-text="i"></option></template>
                                            </select>
                                        </div>
                                        <div class="mm-field">
                                            <span class="v2in-fieldlabel" x-text="t('color')"></span>
                                            <div class="flex gap-1.5 flex-wrap">
                                                <template x-for="(code, name) in colors" :key="name">
                                                    <button type="button" class="mm-swatch" :class="w.color === name ? 'mm-swatch--on' : ''" :style="'background:' + code" :title="name" @click="w.color = name"></button>
                                                </template>
                                            </div>
                                        </div>
                                    </div>

                                    <!-- The body of the widget: its view and its own settings -->
                                    <template x-if="w.type === 'kpi'">
                                        <div class="mm-widget" x-data="widgetKpi(w)">
                                            <jsp:include page="js/widgets/widgetKpi.html"/>
                                        </div>
                                    </template>
                                    <template x-if="w.type === 'timeline'">
                                        <div class="mm-widget" x-data="widgetTimeline(w)">
                                            <jsp:include page="js/widgets/widgetTimeline.html"/>
                                        </div>
                                    </template>
                                    <template x-if="w.type === 'pie'">
                                        <div class="mm-widget" x-data="widgetPie(w)">
                                            <jsp:include page="js/widgets/widgetPie.html"/>
                                        </div>
                                    </template>
                                    <template x-if="w.type === 'radar'">
                                        <div class="mm-widget" x-data="widgetRadar(w)">
                                            <jsp:include page="js/widgets/widgetRadar.html"/>
                                        </div>
                                    </template>
                                    <template x-if="w.type === 'text'">
                                        <div class="mm-widget">
                                            <jsp:include page="js/widgets/widgetText.html"/>
                                        </div>
                                    </template>

                                    <div class="flex justify-end pt-2" x-show="cfg" x-cloak>
                                        <button type="button" class="v2in-btn v2in-btn--primary v2in-btn--xs" @click="cfg = false; save()" x-text="t('done')"></button>
                                    </div>
                                </div>

                                <!-- Corner to resize the widget -->
                                <span class="mm-resize" x-show="editMode" x-cloak @pointerdown="startDrag($event, w, 'resize')"></span>
                            </div>
                        </div>
                    </template>
                </div>

                <div class="v2mo-tv-hint" x-show="tvMode" x-cloak x-text="t('tvhint')"></div>
                </div>
            </div>
        </main>
    </body>
</html>
