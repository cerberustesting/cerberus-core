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
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html class="h-full">
    <head>
        <meta name="active-menu" content="admin">
        <meta name="active-submenu" content="CerberusInformation.jsp">
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%@ include file="include/global/dependenciesInclusions.html" %>
        <%--
            V2 page. The two lists are js/global/crbTable.js in client mode and the
            rest is rendered by Alpine from the single ReadCerberusDetailInformation
            payload - see the header of CerberusInformationV2.js for what V1 was
            dropping and what was rebuilt.
            js/pages/CerberusInformation.js (V1) is NOT loaded.
            CerberusInformationV1.jsp is the rollback copy.
        --%>
        <script type="text/javascript" src="js/pages/CerberusInformationV2.js?v=${appVersion}"></script>
        <title id="pageTitle">Cerberus Monitoring</title>
    </head>
    <body x-data x-cloak class="crb_body">
        <jsp:include page="include/global/header2.html"/>
        <jsp:include page="include/global/modalInclusions.jsp"/>
        <jsp:include page="include/global/rightPanel.html"/>
        <%--
            Same shell as every migrated page (crb_main_wrp + the sidebar/right-panel
            width binding). V1 was still on crb_main + crb_main_sidebar-expanded and
            never included rightPanel.html, so opening the right panel from here slid
            it over the content instead of narrowing it.
        --%>
        <main class="crb_main_wrp" :class="$store.rightPanel.isResizing ? '' : 'transition-all duration-200'"
              :style="{marginLeft: ($store.sidebar.hidden ? 0 : ($store.sidebar.expanded ? 288 : 80)) + 'px',
                      width: 'calc(100vw - ' + ($store.sidebar.hidden ? 0 : ($store.sidebar.expanded ? 288 : 80))
                          + 'px - '+ ($store.rightPanel.open ? $store.rightPanel.width : 0) + 'px)'}">
            <%@ include file="include/global/messagesArea.html"%>
            <%@ include file="include/utils/modal-confirmation.html"%>

            <div x-data="cerberusInfo()">

                <%--
                    ============ Page bar ============
                    The title keeps its own line, like every other page: header2.html
                    parks a `fixed top-4 z-[70]` control cluster in the top right
                    corner of EVERY page, and anything right-aligned on the title
                    line slides underneath it. The controls go in a crb_table_bar
                    strip instead - the same toolbar shell the tables use, so the
                    page reads as one toolbar vocabulary rather than loose buttons.
                --%>
                <h1 class="page-title-line" id="title">Cerberus Monitoring</h1>

                <div class="crb_table_bar">
                    <div class="crb_table_bar_row" style="justify-content: space-between">
                        <div class="crb_table_bar_left">
                            <span class="crb_table_meta_label">
                                <span x-show="lastUpdated" x-cloak>Updated <span x-text="lastUpdated"></span></span>
                                <span x-show="!lastUpdated">Reading instance...</span>
                            </span>

                            <%--
                                Auto-refresh, on by default. The whole page is live
                                state and V1 offered a single manual button, so a
                                reading could sit on screen for an hour looking
                                current. Same toggle markup as the Muted switch in
                                the Edit Test Case modal.
                            --%>
                            <label class="flex items-center gap-2 cursor-pointer select-none ml-2">
                                <button type="button" role="switch" :aria-checked="auto ? 'true' : 'false'"
                                        aria-label="Auto-refresh every 30 seconds"
                                        @click="auto = !auto"
                                        class="relative inline-flex h-6 w-11 shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 ease-in-out focus:outline-none"
                                        :class="auto ? 'bg-sky-500' : 'bg-slate-300 dark:bg-slate-600'">
                                    <span class="pointer-events-none inline-block h-5 w-5 transform rounded-full bg-white shadow ring-0 transition duration-200 ease-in-out"
                                          :class="auto ? 'translate-x-5' : 'translate-x-0'"></span>
                                </button>
                                <span class="crb_table_meta_label">Auto-refresh every 30s</span>
                            </label>
                        </div>

                        <div class="flex items-center gap-2">
                            <button type="button" class="crb_table_iconbtn crb_table_iconbtn--labelled"
                                    @click="copyDiagnostics()" title="Copy every version and setting as text">
                                <i data-lucide="clipboard-copy" class="w-4 h-4"></i><span>Copy diagnostics</span>
                            </button>

                            <button type="button" class="crb_table_iconbtn" @click="load()"
                                    title="Refresh now" aria-label="Refresh now">
                                <span :class="loading ? 'crb_table_spin' : ''" class="inline-flex">
                                    <i data-lucide="refresh-cw" class="w-4 h-4"></i>
                                </span>
                            </button>
                        </div>
                    </div>
                </div>

                <%-- ============ Failure notice ============ --%>
                <div x-show="error" x-cloak
                     class="flex items-center gap-2 mb-4 rounded-xl px-4 py-3 text-sm bg-rose-50 text-rose-700 ring-1 ring-inset ring-rose-200 dark:bg-rose-500/10 dark:text-rose-300 dark:ring-rose-500/30">
                    <i data-lucide="alert-circle" class="w-4 h-4"></i>
                    <span x-text="error"></span>
                </div>

                <%--
                    ============ Schema drift ============
                    The one comparison on this page that must not be missed. V1 put
                    the current and the target schema version in two adjacent cells
                    and left the reader to notice they differed.
                --%>
                <div x-show="schemaDrift" x-cloak
                     class="flex items-start gap-3 mb-4 rounded-xl px-4 py-3 bg-rose-50 ring-1 ring-inset ring-rose-200 dark:bg-rose-500/10 dark:ring-rose-500/30">
                    <i data-lucide="database-zap" class="w-5 h-5 shrink-0 text-rose-600 dark:text-rose-400"></i>
                    <div>
                        <p class="text-sm font-semibold !mb-0.5 text-rose-800 dark:text-rose-200">Database schema is out of date</p>
                        <p class="text-xs !mb-0 text-rose-700 dark:text-rose-300">
                            This instance runs schema <span class="font-bold" x-text="val(schemaCurrent)"></span>
                            but this build expects <span class="font-bold" x-text="val(schemaTarget)"></span>.
                            Features added between the two versions may fail without an obvious error.
                        </p>
                    </div>
                </div>

                <%-- ============ Live state ============ --%>
                <div class="crb_stats">

                    <div class="crb_stat" :class="engineActive ? 'crb_stat--ok' : 'crb_stat--danger'">
                        <div class="crb_stat_head">
                            <span class="crb_stat_icon"><i data-lucide="cpu" class="w-4 h-4"></i></span>
                            <span class="crb_stat_label">Execution engine</span>
                        </div>
                        <div class="crb_stat_body">
                            <span class="crb_tc_chip" :class="engineActive ? 'crb_tc_chip--on' : 'crb_tc_chip--warn'">
                                <span x-text="engineActive ? 'Accepting executions' : 'Not accepting'"></span>
                            </span>
                        </div>
                        <span class="crb_stat_foot">
                            Thread pool on this instance
                        </span>
                    </div>

                    <div class="crb_stat crb_stat--info">
                        <div class="crb_stat_head">
                            <span class="crb_stat_icon"><i data-lucide="play" class="w-4 h-4"></i></span>
                            <span class="crb_stat_label">Running now</span>
                        </div>
                        <div class="crb_stat_body">
                            <span class="crb_stat_value" x-text="runningCount"></span>
                            <span class="crb_stat_unit">execution<span x-show="runningCount !== 1">s</span></span>
                        </div>
                        <span class="crb_stat_foot">
                            <span x-text="num(queue.running)"></span> reported by the queue
                        </span>
                    </div>

                    <div class="crb_stat" :class="queueSize > 0 ? 'crb_stat--warn' : ''">
                        <div class="crb_stat_head">
                            <span class="crb_stat_icon"><i data-lucide="layers" class="w-4 h-4"></i></span>
                            <span class="crb_stat_label">Queue</span>
                        </div>
                        <div class="crb_stat_body">
                            <span class="crb_stat_value" x-text="queueSize"></span>
                            <span class="crb_stat_unit">waiting</span>
                        </div>
                        <span class="crb_stat_foot"
                              x-text="globalLimit > 0 ? ('Global limit ' + globalLimit) : 'No global limit'"></span>
                    </div>

                    <div class="crb_stat" :class="'crb_stat--' + heapTone">
                        <div class="crb_stat_head">
                            <span class="crb_stat_icon"><i data-lucide="memory-stick" class="w-4 h-4"></i></span>
                            <span class="crb_stat_label">Heap</span>
                        </div>
                        <div class="crb_stat_body">
                            <span class="crb_stat_value" x-text="heapPercent + '%'"></span>
                            <span class="crb_stat_unit"
                                  x-text="heapUsed + ' / ' + heapMax + ' MB'"></span>
                        </div>
                        <span class="crb_meter">
                            <span class="crb_meter_track">
                                <span class="crb_meter_fill"
                                      :style="'width:' + heapPercent + '%;background:' + heapColor"></span>
                            </span>
                        </span>
                    </div>

                    <div class="crb_stat">
                        <div class="crb_stat_head">
                            <span class="crb_stat_icon"><i data-lucide="users" class="w-4 h-4"></i></span>
                            <span class="crb_stat_label">Sessions</span>
                        </div>
                        <div class="crb_stat_body">
                            <span class="crb_stat_value" x-text="num(sessions.simultaneous_session)"></span>
                            <span class="crb_stat_unit">open</span>
                        </div>
                        <span class="crb_stat_foot"
                              x-text="activeUsers.length ? (activeUsers.length + ' user' + (activeUsers.length === 1 ? '' : 's') + ' active') : 'No user activity recorded'"></span>
                    </div>
                </div>

                <%-- ============ Running executions ============ --%>
                <div class="crb_card !p-0 overflow-hidden !mb-4">
                    <div class="crb_card_head">
                        <i data-lucide="activity" class="w-4 h-4 text-slate-400"></i>
                        <span class="crb_card_head_title">Running executions</span>
                        <span class="crb_card_head_count" x-text="runningCount"></span>
                        <span class="crb_card_head_hint">Live on this instance</span>
                    </div>
                    <div class="p-4">
                        <div id="runningExecutionsTable"></div>
                    </div>
                </div>

                <%-- ============ Scheduler ============ --%>
                <div class="crb_card !p-0 overflow-hidden !mb-4">
                    <div class="crb_card_head">
                        <i data-lucide="alarm-clock" class="w-4 h-4 text-slate-400"></i>
                        <span class="crb_card_head_title">Scheduler</span>
                        <span class="crb_card_head_count" x-text="triggers.length"></span>
                        <span class="crb_card_head_hint">Quartz triggers on this instance</span>
                    </div>

                    <div class="px-4 pt-3">
                        <div class="crb_spec">
                            <div class="crb_spec_row">
                                <span class="crb_spec_key">Instance version</span>
                                <span class="crb_spec_val" x-text="val(scheduler.schedulerInstanceVersion)"></span>
                            </div>
                            <div class="crb_spec_row">
                                <span class="crb_spec_key">Reload running</span>
                                <span class="crb_spec_val">
                                    <span class="crb_tc_chip"
                                          :class="scheduler.schedulerReloadIsRunning ? 'crb_tc_chip--warn' : 'crb_tc_chip--off'"
                                          x-text="bool(scheduler.schedulerReloadIsRunning)"></span>
                                </span>
                            </div>
                            <div class="crb_spec_row">
                                <span class="crb_spec_key">Server time</span>
                                <span class="crb_spec_val crb_spec_val--mono"
                                      x-text="date(scheduler.serverDate) + ' · ' + val(scheduler.serverTimeZone)"></span>
                            </div>
                        </div>
                    </div>

                    <div class="p-4">
                        <div id="schedulerTriggersTable"></div>
                    </div>
                </div>

                <%-- ============ Static facts ============ --%>
                <div class="crb_infogrid">

                    <%-- ---- Instance ---- --%>
                    <div class="crb_card !p-0 overflow-hidden">
                        <div class="crb_card_head">
                            <i data-lucide="box" class="w-4 h-4 text-slate-400"></i>
                            <span class="crb_card_head_title">Instance</span>
                        </div>
                        <div class="px-4 py-2">
                            <div class="crb_spec">
                                <div class="crb_spec_row">
                                    <span class="crb_spec_key">Project</span>
                                    <span class="crb_spec_val" x-text="val(cerberus.projectName)"></span>
                                </div>
                                <div class="crb_spec_row">
                                    <span class="crb_spec_key">Version</span>
                                    <span class="crb_spec_val crb_spec_val--mono" x-text="val(cerberus.projectVersion)"></span>
                                </div>
                                <div class="crb_spec_row">
                                    <span class="crb_spec_key">Build</span>
                                    <span class="crb_spec_val crb_spec_val--mono" x-text="val(cerberus.projectBuild)"></span>
                                </div>
                                <div class="crb_spec_row">
                                    <span class="crb_spec_key">Environment</span>
                                    <span class="crb_spec_val">
                                        <span class="crb_tc_chip crb_tc_chip--info" x-text="val(cerberus.environment)"></span>
                                    </span>
                                </div>
                                <div class="crb_spec_row">
                                    <span class="crb_spec_key">Database schema</span>
                                    <span class="crb_spec_val crb_spec_val--mono"
                                          :class="schemaDrift ? '!text-rose-600 dark:!text-rose-400 font-bold' : ''"
                                          x-text="val(schemaCurrent) + ' / ' + val(schemaTarget)"></span>
                                </div>
                            </div>
                        </div>
                    </div>

                    <%-- ---- Runtime ---- --%>
                    <div class="crb_card !p-0 overflow-hidden">
                        <div class="crb_card_head">
                            <i data-lucide="server" class="w-4 h-4 text-slate-400"></i>
                            <span class="crb_card_head_title">Runtime</span>
                        </div>
                        <div class="px-4 py-2">
                            <div class="crb_spec">
                                <div class="crb_spec_row">
                                    <span class="crb_spec_key">Java version</span>
                                    <span class="crb_spec_val crb_spec_val--mono" x-text="val(java.javaVersion)"></span>
                                </div>
                                <div class="crb_spec_row">
                                    <span class="crb_spec_key">Application server</span>
                                    <span class="crb_spec_val" x-text="val(java.applicationServerInfo)"></span>
                                </div>
                                <%--
                                    The four heap figures V1 showed as a 4-column table.
                                    Total is the heap the JVM currently holds, Max what it
                                    may grow to - the pair that the % above is computed
                                    from is used/max, which is why both are spelled out.
                                --%>
                                <div class="crb_spec_row">
                                    <span class="crb_spec_key">Heap used</span>
                                    <span class="crb_spec_val crb_spec_val--mono" x-text="num(java.javaUsedMemory) + ' MB'"></span>
                                </div>
                                <div class="crb_spec_row">
                                    <span class="crb_spec_key">Heap free</span>
                                    <span class="crb_spec_val crb_spec_val--mono" x-text="num(java.javaFreeMemory) + ' MB'"></span>
                                </div>
                                <div class="crb_spec_row">
                                    <span class="crb_spec_key">Heap allocated</span>
                                    <span class="crb_spec_val crb_spec_val--mono" x-text="num(java.javaTotalMemory) + ' MB'"></span>
                                </div>
                                <div class="crb_spec_row">
                                    <span class="crb_spec_key">Heap maximum</span>
                                    <span class="crb_spec_val crb_spec_val--mono" x-text="num(java.javaMaxMemory) + ' MB'"></span>
                                </div>
                            </div>
                        </div>
                    </div>

                    <%-- ---- Database ---- --%>
                    <div class="crb_card !p-0 overflow-hidden">
                        <div class="crb_card_head">
                            <i data-lucide="database" class="w-4 h-4 text-slate-400"></i>
                            <span class="crb_card_head_title">Database</span>
                        </div>
                        <div class="px-4 py-2">
                            <div class="crb_spec">
                                <div class="crb_spec_row">
                                    <span class="crb_spec_key">Product</span>
                                    <span class="crb_spec_val"
                                          x-text="val(db.databaseProductName) + ' ' + val(db.databaseProductVersion)"></span>
                                </div>
                                <div class="crb_spec_row">
                                    <span class="crb_spec_key">Major / minor</span>
                                    <span class="crb_spec_val crb_spec_val--mono"
                                          x-text="val(db.databaseMajorVersion) + '.' + val(db.databaseMinorVersion)"></span>
                                </div>
                                <div class="crb_spec_row">
                                    <span class="crb_spec_key">Driver</span>
                                    <span class="crb_spec_val" x-text="val(db.driverName)"></span>
                                </div>
                                <div class="crb_spec_row">
                                    <span class="crb_spec_key">Driver version</span>
                                    <span class="crb_spec_val crb_spec_val--mono" x-text="val(db.driverVersion)"></span>
                                </div>
                                <div class="crb_spec_row">
                                    <span class="crb_spec_key">Driver major / minor</span>
                                    <span class="crb_spec_val crb_spec_val--mono"
                                          x-text="val(db.driverMajorVersion) + '.' + val(db.driverMinorVersion)"></span>
                                </div>
                                <div class="crb_spec_row">
                                    <span class="crb_spec_key">JDBC</span>
                                    <span class="crb_spec_val crb_spec_val--mono"
                                          x-text="val(db.jDBCMajorVersion) + '.' + val(db.jDBCMinorVersion)"></span>
                                </div>
                            </div>
                        </div>
                    </div>

                    <%-- ---- Authentication & hosting ---- --%>
                    <div class="crb_card !p-0 overflow-hidden">
                        <div class="crb_card_head">
                            <i data-lucide="shield-check" class="w-4 h-4 text-slate-400"></i>
                            <span class="crb_card_head_title">Authentication &amp; hosting</span>
                        </div>
                        <div class="px-4 py-2">
                            <div class="crb_spec">
                                <div class="crb_spec_row">
                                    <span class="crb_spec_key">Method</span>
                                    <span class="crb_spec_val" x-text="val(auth.authentification)"></span>
                                </div>
                                <%--
                                    The three Keycloak fields only exist when Keycloak is
                                    the method. V1 always drew the columns, so a non-Keycloak
                                    instance showed three empty cells under three headers.
                                --%>
                                <template x-if="isKeycloak">
                                    <div class="crb_spec">
                                        <div class="crb_spec_row">
                                            <span class="crb_spec_key">Keycloak realm</span>
                                            <span class="crb_spec_val" x-text="val(auth.keycloakRealm)"></span>
                                        </div>
                                        <div class="crb_spec_row">
                                            <span class="crb_spec_key">Keycloak client</span>
                                            <span class="crb_spec_val" x-text="val(auth.keycloakClient)"></span>
                                        </div>
                                        <div class="crb_spec_row">
                                            <span class="crb_spec_key">Keycloak URL</span>
                                            <span class="crb_spec_val crb_spec_val--mono" x-text="val(auth.keycloakUrl)"></span>
                                        </div>
                                    </div>
                                </template>
                                <div class="crb_spec_row" x-show="!isKeycloak">
                                    <span class="crb_spec_key">Keycloak</span>
                                    <span class="crb_spec_val crb_spec_val--muted">Not in use</span>
                                </div>
                                <div class="crb_spec_row">
                                    <span class="crb_spec_key">SaaS</span>
                                    <span class="crb_spec_val">
                                        <span class="crb_tc_chip" :class="isSaaS ? 'crb_tc_chip--info' : 'crb_tc_chip--off'"
                                              x-text="bool(saas.isSaaS)"></span>
                                    </span>
                                </div>
                                <div class="crb_spec_row" x-show="isSet(saas.saasInstance)">
                                    <span class="crb_spec_key">SaaS instance</span>
                                    <span class="crb_spec_val" x-text="val(saas.saasInstance)"></span>
                                </div>
                                <div class="crb_spec_row">
                                    <span class="crb_spec_key">Parallel runs allowed</span>
                                    <span class="crb_spec_val crb_spec_val--mono" x-text="num(saas.saasParallelrun)"></span>
                                </div>
                            </div>
                        </div>
                    </div>

                    <%--
                        ---- Credit limits ----
                        `creditLimit` is in the payload and V1 never rendered it at all.
                    --%>
                    <div class="crb_card !p-0 overflow-hidden">
                        <div class="crb_card_head">
                            <i data-lucide="gauge" class="w-4 h-4 text-slate-400"></i>
                            <span class="crb_card_head_title">Credit limits</span>
                        </div>
                        <div class="px-4 py-2">
                            <div class="crb_spec">
                                <div class="crb_spec_row">
                                    <span class="crb_spec_key">Executions per day</span>
                                    <span class="crb_spec_val crb_spec_val--mono"
                                          x-text="Number(credit.numberOfExecution) ? num(credit.numberOfExecution) : 'Unlimited'"></span>
                                </div>
                                <div class="crb_spec_row">
                                    <span class="crb_spec_key">Execution seconds per day</span>
                                    <span class="crb_spec_val crb_spec_val--mono"
                                          x-text="Number(credit.durationOfExecutionInSecond) ? num(credit.durationOfExecutionInSecond) : 'Unlimited'"></span>
                                </div>
                            </div>
                        </div>
                    </div>

                    <%--
                        ---- Sessions ----
                        `sessions` is in the payload and V1 never rendered it either.
                        The tile above answers "how many"; this answers "who".
                    --%>
                    <div class="crb_card !p-0 overflow-hidden">
                        <div class="crb_card_head">
                            <i data-lucide="users" class="w-4 h-4 text-slate-400"></i>
                            <span class="crb_card_head_title">Sessions</span>
                        </div>
                        <div class="px-4 py-2">
                            <div class="crb_spec">
                                <div class="crb_spec_row">
                                    <span class="crb_spec_key">Open sessions</span>
                                    <span class="crb_spec_val crb_spec_val--mono" x-text="num(sessions.simultaneous_session)"></span>
                                </div>
                                <div class="crb_spec_row">
                                    <span class="crb_spec_key">Active users</span>
                                    <span class="crb_spec_val crb_spec_val--muted" x-show="!activeUsers.length">None right now</span>
                                    <span class="crb_spec_val" x-show="activeUsers.length" x-text="activeUsers.join(', ')"></span>
                                </div>
                            </div>
                        </div>
                    </div>

                    <%--
                        ---- Parameter cache ----
                        Also absent from V1. Useful precisely when a parameter change has
                        not taken effect yet: the entry is still cached and this says for
                        how much longer.
                    --%>
                    <div class="crb_card !p-0 overflow-hidden">
                        <div class="crb_card_head">
                            <i data-lucide="layers-2" class="w-4 h-4 text-slate-400"></i>
                            <span class="crb_card_head_title">Parameter cache</span>
                            <span class="crb_card_head_count" x-text="cacheEntries.length"></span>
                            <span class="crb_card_head_hint"
                                  x-text="'TTL ' + age(cache.cacheParameterDurationInS)"></span>
                        </div>
                        <div class="px-4 py-2 max-h-72 overflow-auto">
                            <div class="crb_spec">
                                <template x-for="entry in cacheEntries" :key="entry.key">
                                    <div class="crb_spec_row">
                                        <span class="crb_spec_key crb_spec_val--mono"
                                              x-text="entry.key.replace(/#$/, '')"></span>
                                        <span class="crb_spec_val crb_spec_val--mono crb_spec_val--muted"
                                              x-text="age(entry.durationFromCreatedInS)"></span>
                                    </div>
                                </template>
                                <div class="crb_spec_row" x-show="!cacheEntries.length">
                                    <span class="crb_spec_val crb_spec_val--muted" style="text-align:left">
                                        Nothing cached right now
                                    </span>
                                </div>
                            </div>
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
