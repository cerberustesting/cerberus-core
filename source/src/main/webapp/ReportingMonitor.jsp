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
        <meta name="active-menu" content="monitor">
        <meta name="active-submenu" content="ReportingMonitor.jsp">
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%@ include file="include/global/dependenciesInclusions.html" %>

        <script type="text/javascript" src="js/pages/insightsShared.js?v=${appVersion}"></script>
        <script type="text/javascript" src="js/pages/ReportingMonitor.js?v=${appVersion}"></script>
        <link rel="stylesheet" type="text/css" href="css/pages/InsightsShared.css?v=${appVersion}"/>

        <title id="pageTitle">Real-Time Monitor</title>
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

            <div x-data="executionMonitor()" x-init="init()" class="v2in-page" id="executionMonitorRoot">

                <!-- Page title above the bar, like every list page -->
                <div class="v2in-pagetitle" x-show="!tvMode">
                    <h1 class="page-title-line">Real-Time Monitor</h1>
                </div>

                <!-- Sticky header: live badge, display settings -->
                <div class="crb_card v2in-card v2in-header" :style="$store.rightPanel.open ? { top: '0px' } : {}" x-show="!tvMode">
                    <div class="flex items-center gap-3 flex-wrap">
                        <span class="v2in-livebadge" :class="wsConnected && subscribed ? 'v2in-livebadge--on' : 'v2in-livebadge--off'">
                            <span class="v2in-pulse" x-show="wsConnected && subscribed"></span>
                            <span x-text="wsConnected && subscribed ? 'LIVE' : 'OFFLINE'"></span>
                        </span>
                        <span class="v2in-dim text-xs" x-show="pushAgo !== null" x-text="'last push ' + pushAgo + 's ago'"></span>

                        <div class="flex-1"></div>

                        <button type="button" class="v2in-btn" :class="showRetry ? 'v2in-btn--on' : ''" @click="showRetry = !showRetry"
                                title="Also show retried executions (non useful ones)">Retries</button>
                        <button type="button" class="v2in-btn" :class="showMuted ? 'v2in-btn--on' : ''" @click="showMuted = !showMuted"
                                title="Also show muted test cases">Muted</button>
                        <button type="button" class="v2in-btn v2in-btn--primary" @click="tvMode = true"
                                title="Fullscreen dashboard for a wall screen - the URL keeps every setting, ESC to leave">
                            <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><rect x="2" y="3" width="20" height="14" rx="2"/><line x1="8" y1="21" x2="16" y2="21"/><line x1="12" y1="17" x2="12" y2="21"/></svg>
                            TV mode
                        </button>
                    </div>

                    <!-- Prominent filters: reference lists, always available -->
                    <div class="v2mo-filters">
                        <template x-for="f in [
                                { name: 'system', label: 'System', all: 'All systems' },
                                { name: 'campaign', label: 'Campaign', all: 'All campaigns' },
                                { name: 'environment', label: 'Environment', all: 'All environments' },
                                { name: 'country', label: 'Country', all: 'All countries' }
                            ]" :key="f.name">
                            <div class="v2in-field relative" @click.outside="if (ddOpen === f.name) ddOpen = ''">
                                <span class="v2in-fieldlabel" x-text="f.label"></span>
                                <button type="button" class="v2in-picker" style="min-width: 190px; max-width: 300px"
                                        :class="[ddOpen === f.name ? 'v2in-picker--active' : '', selOf(f.name).length === 0 ? 'v2in-picker--empty' : '']"
                                        @click="openDd(f.name)"
                                        :title="selOf(f.name).length ? selOf(f.name).join(', ') : f.all">
                                    <span class="v2in-picker-value" x-text="pickerLabel(f.name, f.all)"></span>
                                    <span class="flex-1"></span>
                                    <span class="v2in-picker-count" x-show="selOf(f.name).length" x-text="selOf(f.name).length"></span>
                                    <svg class="w-3.5 h-3.5 v2in-picker-chevron" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path d="M19 9l-7 7-7-7"/></svg>
                                </button>
                                <div x-show="ddOpen === f.name" x-cloak class="v2in-dd" style="top: 100%; min-width: 260px">
                                    <div class="v2in-dd-search">
                                        <input type="text" x-model="ddSearch" :placeholder="'Search ' + f.label.toLowerCase() + '...'" class="v2in-input">
                                        <button type="button" class="v2in-btn v2in-btn--xs" @click="clearSel(f.name)" title="No restriction on this dimension">All</button>
                                    </div>
                                    <div class="v2in-dd-list">
                                        <template x-for="v in ddItems(f.name)" :key="v">
                                            <button type="button" class="v2in-dd-item" @click="toggleSel(f.name, v)">
                                                <span class="v2in-check" :class="selOf(f.name).includes(v) ? 'v2in-check--on' : ''"></span>
                                                <span class="v2in-strong" x-text="v"></span>
                                                <span class="v2in-dim truncate text-xs" x-show="f.name === 'campaign' && campaignDesc(v)" x-text="campaignDesc(v)"></span>
                                            </button>
                                        </template>
                                        <div x-show="ddItems(f.name).length === 0" class="v2in-empty px-3 py-2 text-center">No value matches</div>
                                    </div>
                                </div>
                            </div>
                        </template>

                        <!-- Group by: composable dimensions + auto mode -->
                        <div class="v2in-field" style="flex: 1; min-width: 300px">
                            <span class="v2in-fieldlabel">Group tiles by</span>
                            <div class="v2mo-groupchips">
                                <button type="button" class="v2in-pill" :class="autoCols ? 'v2in-pill--on' : ''"
                                        @click="autoCols = !autoCols" style="font-weight: 800"
                                        title="Pick automatically the dimensions that best split the current board">AUTO</button>
                                <template x-for="d in GROUP_DIMS" :key="d.key">
                                    <button type="button" class="v2in-pill" :class="[isColOn(d.key) ? 'v2in-pill--on' : '', autoCols ? 'v2in-pill--off' : '']"
                                            :style="autoCols ? 'pointer-events: none' : ''"
                                            @click="toggleCol(d.key)" x-text="d.label"
                                            :title="autoCols ? 'Managed by the auto mode' : 'Group the tiles by ' + d.label.toLowerCase()"></button>
                                </template>
                            </div>
                        </div>

                        <!-- Horizon -->
                        <div class="v2in-field">
                            <span class="v2in-fieldlabel">Horizon</span>
                            <div class="flex items-center gap-1.5">
                                <input type="number" min="1" class="v2in-input v2mo-num" x-model.number="horizonMin" title="Only show executions started within this window (minutes)">
                                <div class="v2in-seg">
                                    <button type="button" class="v2in-seg-item" :class="horizonMin === 30 ? 'v2in-seg-item--on' : ''" @click="horizonMin = 30">30m</button>
                                    <button type="button" class="v2in-seg-item" :class="horizonMin === 480 ? 'v2in-seg-item--on' : ''" @click="horizonMin = 480">8h</button>
                                    <button type="button" class="v2in-seg-item" :class="horizonMin === 1440 ? 'v2in-seg-item--on' : ''" @click="horizonMin = 1440">24h</button>
                                    <button type="button" class="v2in-seg-item" :class="horizonMin === 10080 ? 'v2in-seg-item--on' : ''" @click="horizonMin = 10080">7d</button>
                                </div>
                            </div>
                        </div>

                        <!-- Previous executions -->
                        <div class="v2in-field">
                            <span class="v2in-fieldlabel">History per tile</span>
                            <div class="flex items-center gap-1.5">
                                <input type="number" min="0" max="9" class="v2in-input v2mo-num" x-model.number="prevExe" title="Number of previous executions shown on each tile">
                                <div class="v2in-seg">
                                    <button type="button" class="v2in-seg-item" :class="prevExe === 3 ? 'v2in-seg-item--on' : ''" @click="prevExe = 3">3</button>
                                    <button type="button" class="v2in-seg-item" :class="prevExe === 5 ? 'v2in-seg-item--on' : ''" @click="prevExe = 5">5</button>
                                    <button type="button" class="v2in-seg-item" :class="prevExe === 9 ? 'v2in-seg-item--on' : ''" @click="prevExe = 9">9</button>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Active filter chips -->
                    <div class="v2mo-activefilters" x-show="activeFilterChips.length" x-cloak>
                        <span class="v2in-dim text-xs font-semibold">Active filters</span>
                        <template x-for="c in activeFilterChips" :key="c.group + '|' + c.value">
                            <span class="v2in-chip v2in-chip--info">
                                <span class="v2in-dim" x-text="c.group + ':'" style="font-size: 10px"></span>
                                <span x-text="c.value"></span>
                                <button type="button" class="v2in-chip-x" @click="toggleSel(c.group, c.value)" title="Remove this filter">&times;</button>
                            </span>
                        </template>
                        <button type="button" class="v2in-btn v2in-btn--xs" @click="clearAllFilters()">Clear all</button>
                    </div>
                </div>

                <!-- Stage: status bar + board (this is what the TV mode expands) -->
                <div id="monitorStage" :class="tvMode ? 'v2mo-tv' : ''">

                    <!-- TV mode: minimal live strip -->
                    <div class="flex items-center gap-3 mb-3" x-show="tvMode" x-cloak>
                        <span class="v2in-title" style="font-size: 18px">Real-Time Monitor</span>
                        <span class="v2in-livebadge" :class="wsConnected && subscribed ? 'v2in-livebadge--on' : 'v2in-livebadge--off'">
                            <span class="v2in-pulse" x-show="wsConnected && subscribed"></span>
                            <span x-text="wsConnected && subscribed ? 'LIVE' : 'OFFLINE'"></span>
                        </span>
                        <span class="v2in-dim text-xs" x-show="pushAgo !== null" x-text="'last push ' + pushAgo + 's ago'"></span>
                        <div class="flex-1"></div>
                        <button type="button" class="v2in-btn" @click="tvMode = false">Exit TV mode</button>
                    </div>

                    <!-- Status aggregation -->
                    <div class="crb_card v2in-card v2mo-statusbar" x-show="boardData.total > 0" x-cloak>
                        <div class="v2mo-statusbar-track">
                            <template x-for="s in statusSegments" :key="s.status">
                                <div class="v2mo-statusbar-seg" :style="'width:' + s.pct + '%; background:' + s.color"
                                     :title="s.status + ': ' + s.count"></div>
                            </template>
                        </div>
                        <div class="flex items-center gap-2 flex-wrap">
                            <span class="v2in-count" x-text="boardData.total + ' test case(s) on the board'"></span>
                            <template x-for="s in statusSegments" :key="'c' + s.status">
                                <span class="v2in-chip" :style="'background: color-mix(in srgb, ' + s.color + ' 14%, transparent); color:' + s.color">
                                    <span x-text="s.status"></span>&nbsp;<span x-text="s.count"></span>
                                </span>
                            </template>
                            <div class="flex-1"></div>
                            <span class="v2in-dim text-xs" x-text="'grouped by ' + boardData.cols.join(' + ') + (autoCols ? ' (auto)' : '')"></span>
                        </div>
                    </div>

                    <!-- Board -->
                    <div id="monitorBoard" class="v2mo-board">

                        <!-- Onboarding: waiting for the first live push -->
                        <template x-if="!raw">
                            <div class="crb_card v2in-card v2in-hero">
                                <div class="v2in-hero-icon">
                                    <svg class="w-7 h-7" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path d="M22 12h-4l-3 9L9 3l-3 9H2"/></svg>
                                </div>
                                <div class="v2in-hero-title">Your executions, live</div>
                                <div class="v2in-hero-sub">
                                    This board fills itself: every test case executed on this instance appears here in real time,
                                    pushed by the engine over websocket - no reload, no setup. Colored tiles show the latest result,
                                    the small squares their previous runs.
                                </div>
                                <div class="v2in-hero-steps">
                                    <div class="v2in-hero-step"><span class="v2in-hero-stepnum">1</span><span>Run a campaign or a test case, from Cerberus or your CI</span></div>
                                    <div class="v2in-hero-step"><span class="v2in-hero-stepnum">2</span><span>Tiles appear here the moment the engine reports them</span></div>
                                    <div class="v2in-hero-step"><span class="v2in-hero-stepnum">3</span><span>Filter, group, then switch to TV mode for the team screen</span></div>
                                </div>
                                <div class="v2in-hero-cta">
                                    <span class="v2in-livebadge" :class="wsConnected ? 'v2in-livebadge--on' : 'v2in-livebadge--off'">
                                        <span class="v2in-pulse" x-show="wsConnected"></span>
                                        <span x-text="wsConnected ? 'Connected - waiting for the first push' : 'Connecting to the live channel...'"></span>
                                    </span>
                                    <a href="./TestCaseExecutionQueueList.jsp" class="v2in-btn">Open the execution queue</a>
                                </div>
                            </div>
                        </template>

                        <!-- Empty: data exists but everything is filtered out / out of horizon -->
                        <template x-if="raw && boardData.total === 0">
                            <div class="crb_card v2in-card v2in-hero">
                                <div class="v2in-hero-icon">
                                    <svg class="w-7 h-7" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><polygon points="22 3 2 3 10 12.46 10 19 14 21 14 12.46 22 3"/></svg>
                                </div>
                                <div class="v2in-hero-title" x-text="boardData.rawBoxes > 0 ? 'Everything is hidden by your view settings' : ('Nothing ran in the last ' + horizonLabel)"></div>
                                <div class="v2in-hero-sub" x-show="boardData.rawBoxes > 0">
                                    The engine reported <b x-text="boardData.rawBoxes"></b> test case(s), but none matches the current filters and horizon.
                                    Widen the horizon or clear the filters to see them again.
                                </div>
                                <div class="v2in-hero-sub" x-show="boardData.rawBoxes === 0">
                                    The live channel is connected and no execution was reported within the selected horizon.
                                    The board will light up by itself as soon as something runs.
                                </div>
                                <div class="v2in-hero-cta">
                                    <button type="button" class="v2in-btn" x-show="activeFilterChips.length" @click="clearAllFilters()">Clear the filters</button>
                                    <button type="button" class="v2in-btn" x-show="horizonMin < 1440" @click="horizonMin = 1440">Extend horizon to 24h</button>
                                    <button type="button" class="v2in-btn" x-show="horizonMin < 10080" @click="horizonMin = 10080">Extend horizon to 7 days</button>
                                    <button type="button" class="v2in-btn" x-show="!showRetry" @click="showRetry = true">Include retries</button>
                                </div>
                            </div>
                        </template>

                        <template x-for="g in boardData.groups" :key="g.label">
                            <div class="crb_card v2in-card v2mo-group">
                                <div class="v2in-card-head">
                                    <span class="v2in-card-title" x-text="g.label"></span>
                                    <span class="v2in-count" x-text="g.tiles.length"></span>
                                </div>
                                <div class="v2mo-tiles">
                                    <template x-for="t in g.tiles" :key="t.key">
                                        <div class="v2mo-tile" :class="_flash[t.key] ? 'v2mo-tile--flash' : ''"
                                             :style="'--tile-color:' + tileColor(t)"
                                             :title="tileTitle(t)" @click="openExe(t.last)">
                                            <div class="v2mo-tile-top">
                                                <span class="v2mo-tile-status" x-text="t.last.falseNegative ? 'FN' : t.last.controlStatus"></span>
                                                <span class="v2mo-tile-tc" x-text="t.last.testCase"></span>
                                                <span class="flex-1"></span>
                                                <span class="v2mo-tile-ago" x-text="exeAgo(t.last)"></span>
                                            </div>
                                            <div class="v2mo-tile-desc" x-text="t.last.description || t.last.test"></div>
                                            <div class="v2mo-tile-foot">
                                                <span class="v2mo-tile-ctx" x-text="tileCtx(t)"></span>
                                                <span class="flex-1"></span>
                                                <span class="v2mo-tile-prevs">
                                                    <template x-for="(p, pi) in t.prevs" :key="pi">
                                                        <span class="v2mo-prevsq" :style="'background:' + statusColor(p.falseNegative ? 'FN' : p.controlStatus)"
                                                              :title="'Previous: ' + p.controlStatus + (p.falseNegative ? ' (FN)' : '')"
                                                              @click.stop="openExe(p)"></span>
                                                    </template>
                                                </span>
                                            </div>
                                        </div>
                                    </template>
                                </div>
                            </div>
                        </template>
                    </div>

                    <div class="v2mo-tv-hint" x-show="tvMode" x-cloak>ESC to exit - this URL restores this exact dashboard</div>
                </div>

            </div>

            <footer class="footer">
                <div class="container-fluid" id="footer"></div>
            </footer>
        </main>
    </body>
</html>
