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
        <meta name="active-menu" content="execute">
        <meta name="active-submenu" content="TestCaseExecutionQueueList.jsp">
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%@ include file="include/global/dependenciesInclusions.html" %>
        <script type="text/javascript" src="dependencies/Moment-2.30.1/moment-with-locales.min.js"></script>
        <script type="text/javascript" src="dependencies/Chart.js-2.9.3/Chart.min.js"></script>
        <script type="text/javascript" src="dependencies/Bootstrap-datetimepicker-4.17.47/bootstrap-datetimepicker.min.js"></script>
        <%--
            V2 page: the "Executions in queue" table is js/global/crbTable.js driven
            by TestCaseExecutionQueueListV2.js. TestCaseExecutionQueueList.js (V1) is
            NOT loaded. TestCaseExecutionQueueListV1.jsp is the rollback copy.
            The Pools Follow Up table stays a legacy CLIENT-side DataTable: it is fed
            from an in-memory array, not from a paginated endpoint, which is not what
            crbTable does. Untouched by this migration.
        --%>
        <script type="text/javascript" src="js/pages/TestCaseExecutionQueueListV2.js?v=${appVersion}"></script>
        <title id="pageTitle">Executions Queue</title>
        <link rel="stylesheet" type="text/css" href="css/pages/TestCaseExecutionQueue.css?v=${appVersion}"/>
    </head>
    <body x-data x-cloak class="crb_body">
        <jsp:include page="include/global/header2.html"/>
        <jsp:include page="include/global/modalInclusions.jsp"/>
        <jsp:include page="include/global/rightPanel.html"/>
        <main class="crb_main_wrp" :class="$store.rightPanel.isResizing ? '' : 'transition-all duration-200'"
              :style="{marginLeft: ($store.sidebar.hidden ? 0 : ($store.sidebar.expanded ? 288 : 80)) + 'px',
                      width: 'calc(100vw - ' + ($store.sidebar.hidden ? 0 : ($store.sidebar.expanded ? 288 : 80))
                          + 'px - '+ ($store.rightPanel.open ? $store.rightPanel.width : 0) + 'px)'}">
            <%@ include file="include/global/messagesArea.html"%>
            <%@ include file="include/utils/modal-confirmation.html"%>
            <%@ include file="include/pages/testcaseexecutionqueue/massActionExecutionPending.html"%>

            <%@ include file="include/transversal/Invariant.html"%>

            <h1 class="page-title-line" id="title">Executions Queue</h1>

            <div x-data="{ tab: (function () { var t = sessionStorage.getItem('TestCaseExecutionQueueList-TAB'); return (!t || t[0] === '#') ? 'details' : t; })() }" class="w-full">

                <%-- Shared tab bar (.crb_tabs, components.css), same component as
                     Label.jsp / TestCaseScriptV2 / TestCaseExecutionV2. --%>
                <div class="crb_tabs">
                    <button @click="tab = 'details'; switchQueueTab('details');"
                            :class="tab === 'details' ? 'crb_tab--active' : ''"
                            class="crb_tab">
                        <i data-lucide="list-checks" class="w-4 h-4"></i>Executions in queue
                    </button>
                    <button @click="tab = 'followup'; switchQueueTab('followup');"
                            :class="tab === 'followup' ? 'crb_tab--active' : ''"
                            class="crb_tab">
                        <i data-lucide="layers" class="w-4 h-4"></i>Pools Follow Up
                    </button>
                    <button @click="tab = 'history'; switchQueueTab('history');"
                            :class="tab === 'history' ? 'crb_tab--active' : ''"
                            class="crb_tab">
                        <i data-lucide="chart-column" class="w-4 h-4"></i>Queue History
                    </button>
                    <button @click="tab = 'jobstatus'; switchQueueTab('jobstatus');"
                            :class="tab === 'jobstatus' ? 'crb_tab--active' : ''"
                            class="crb_tab">
                        <i data-lucide="settings-2" class="w-4 h-4"></i>Queue Job Status
                    </button>
                </div>

                <!-- ═══════════ Executions in queue ═══════════ -->
                <div x-show="tab === 'details'" x-cloak id="tabDetails">
                    <%-- The <form> V1 wrapped around the table only existed so that
                         massActionForm.serialize() could collect the ticked row ids.
                         The component owns the selection now (exeQV2SelectionParams
                         builds the same id=..&id=.. payload), so the form is gone. --%>
                    <div id="executionList"></div>
                </div>

                <!-- ═══════════ Pools Follow Up ═══════════ -->
                <div x-show="tab === 'followup'" x-cloak id="tabFollowUp">
                    <div class="mb-3">
                        <button type="button" id="refreshFollowUpbutton" onclick="displayAndRefresh_followup()"
                                class="flex items-center gap-1.5 px-3 py-1 rounded-md h-10 w-auto border border-gray-300 dark:border-gray-600 hover:bg-gray-100 dark:hover:bg-gray-700 transition">
                            <i data-lucide="refresh-cw" class="w-4 h-4"></i>
                            <span>Refresh</span>
                        </button>
                    </div>
                    <div id="followUpTableList">
                        <table id="followUpTable" class="table table-hover display" name="followUpTable"></table>
                        <div class="marginBottom20"></div>
                    </div>
                </div>

                <!-- ═══════════ Queue History ═══════════ -->
                <div x-show="tab === 'history'" x-cloak id="tabQueueHistory">
                    <div class="crb_card p-4 mb-4" id="FiltersPanel">
                        <div class="flex items-end gap-4 flex-wrap" id="qsFilterPanel">
                            <div>
                                <label for="frompicker" class="block mb-1 text-xs font-bold uppercase tracking-wide text-slate-500 dark:text-slate-400">From</label>
                                <div class='input-group date' id='frompicker' style="max-width: 230px">
                                    <input type='text' class="form-control h-10 border border-gray-300 dark:border-gray-600 rounded-l-md px-3 bg-white dark:bg-slate-800"/>
                                    <span class="input-group-addon border border-l-0 border-gray-300 dark:border-gray-600 rounded-r-md px-3 cursor-pointer bg-slate-50 dark:bg-slate-700">
                                        <i data-lucide="calendar" class="w-4 h-4"></i>
                                    </span>
                                </div>
                            </div>
                            <div>
                                <label for="topicker" class="block mb-1 text-xs font-bold uppercase tracking-wide text-slate-500 dark:text-slate-400">To</label>
                                <div class='input-group date' id='topicker' style="max-width: 230px">
                                    <input type='text' class="form-control h-10 border border-gray-300 dark:border-gray-600 rounded-l-md px-3 bg-white dark:bg-slate-800"/>
                                    <span class="input-group-addon border border-l-0 border-gray-300 dark:border-gray-600 rounded-r-md px-3 cursor-pointer bg-slate-50 dark:bg-slate-700">
                                        <i data-lucide="calendar" class="w-4 h-4"></i>
                                    </span>
                                </div>
                            </div>

                            <div class="relative" x-data="{ open: false }" @click.outside="open = false">
                                <button type="button" @click="open = !open"
                                        class="flex items-center gap-1.5 px-3 py-1 rounded-md h-10 w-auto border border-gray-300 dark:border-gray-600 hover:bg-gray-100 dark:hover:bg-gray-700 transition">
                                    <i data-lucide="clock" class="w-4 h-4"></i>
                                    <span>Preset Range</span>
                                    <i data-lucide="chevron-down" class="w-3.5 h-3.5"></i>
                                </button>
                                <div x-show="open" x-cloak
                                     class="absolute left-0 top-full mt-1 w-52 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 shadow-lg p-1 z-50">
                                    <button type="button" class="flex w-full px-3 py-2 rounded-lg text-xs text-slate-700 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-700 transition-colors" @click="setTimeRange(10); open = false">Previous Hour</button>
                                    <button type="button" class="flex w-full px-3 py-2 rounded-lg text-xs text-slate-700 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-700 transition-colors" @click="setTimeRange(11); open = false">Previous 6 Hours</button>
                                    <button type="button" class="flex w-full px-3 py-2 rounded-lg text-xs text-slate-700 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-700 transition-colors" @click="setTimeRange(6); open = false">Current Day</button>
                                    <button type="button" class="flex w-full px-3 py-2 rounded-lg text-xs text-slate-700 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-700 transition-colors" @click="setTimeRange(5); open = false">Previous Week</button>
                                    <button type="button" class="flex w-full px-3 py-2 rounded-lg text-xs text-slate-700 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-700 transition-colors" @click="setTimeRange(1); open = false">Previous Month</button>
                                    <button type="button" class="flex w-full px-3 py-2 rounded-lg text-xs text-slate-700 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-700 transition-colors" @click="setTimeRange(2); open = false">Previous 3 Months</button>
                                    <button type="button" class="flex w-full px-3 py-2 rounded-lg text-xs text-slate-700 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-700 transition-colors" @click="setTimeRange(3); open = false">Previous 6 Months</button>
                                    <button type="button" class="flex w-full px-3 py-2 rounded-lg text-xs text-slate-700 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-700 transition-colors" @click="setTimeRange(4); open = false">Previous Year</button>
                                </div>
                            </div>

                            <button type="button" id="loadbutton" onclick="loadStatGraph();"
                                    class="text-white bg-sky-400 hover:bg-sky-500 flex items-center space-x-1 px-3 py-1 rounded-md h-10 w-auto">
                                <i data-lucide="refresh-cw" class="w-4 h-4"></i>
                                <span>Load</span>
                            </button>
                        </div>
                    </div>

                    <div id="ReportQueueStatPanel">
                        <div id="panelQueueStat" class="crb_card p-4" style="display: none">
                            <div class="flex items-center gap-2 mb-3">
                                <i data-lucide="chart-column" class="w-4 h-4 text-slate-500"></i>
                                <span class="text-xs font-bold uppercase tracking-wide text-slate-700 dark:text-slate-200" id="lblQueueStat">Queue Execution Status</span>
                            </div>
                            <div id="perfChart1">
                                <div id="ChartQueueStat" style="height: 400px">
                                    <canvas id="canvasQueueStat"></canvas>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- ═══════════ Queue Job Status ═══════════ -->
                <div x-show="tab === 'jobstatus'" x-cloak id="tabJobStatus">
                    <div class="mb-3">
                        <button type="button" id="refreshJobStatusbutton" onclick="displayAndRefresh_jobStatus()"
                                class="flex items-center gap-1.5 px-3 py-1 rounded-md h-10 w-auto border border-gray-300 dark:border-gray-600 hover:bg-gray-100 dark:hover:bg-gray-700 transition">
                            <i data-lucide="refresh-cw" class="w-4 h-4"></i>
                            <span>Refresh</span>
                        </button>
                    </div>

                    <div class="crb_card p-4 mb-4" id="QueueJobActive">
                        <div class="flex items-center gap-4 flex-wrap">
                            <span id="jobActiveStatus" class="inline-flex items-center justify-center h-12 w-12 rounded-xl bg-slate-100 dark:bg-slate-800"></span>
                            <button type="button" id="modifyParambutton" onclick="enableDisableJob();"
                                    class="flex items-center gap-1.5 px-3 py-1 rounded-md h-10 w-auto border border-gray-300 dark:border-gray-600 hover:bg-gray-100 dark:hover:bg-gray-700 transition disabled:opacity-40 disabled:cursor-not-allowed">
                                <i data-lucide="play" class="w-4 h-4"></i><span>Activate / Desactivate Queue Job</span>
                            </button>
                            <input type="text" class="hidden" name="jobActive" id="jobActive" readonly>
                            <div class="flex-1 min-w-[220px]">
                                <label for="instanceJobActive" name="jobActiveField" class="block mb-1 text-xs font-bold uppercase tracking-wide text-slate-500 dark:text-slate-400">Instance Job Activated</label>
                                <input type="text" name="instanceJobActive" id="instanceJobActive" readonly
                                       class="w-full h-10 border rounded-md px-3 text-sm bg-slate-50 dark:bg-slate-800 border-gray-300 dark:border-gray-600 text-slate-700 dark:text-slate-300">
                            </div>
                        </div>
                    </div>

                    <div class="crb_card p-4" id="QueueJobStatus">
                        <div class="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
                            <div>
                                <label for="jobRunning" name="jobRunningField" class="block mb-1 text-xs font-bold uppercase tracking-wide text-slate-500 dark:text-slate-400">Is Queue Job currently running ?</label>
                                <input type="text" name="jobRunning" id="jobRunning" readonly
                                       class="w-full h-10 border rounded-md px-3 text-sm bg-slate-50 dark:bg-slate-800 border-gray-300 dark:border-gray-600 text-slate-700 dark:text-slate-300">
                            </div>
                            <div>
                                <label for="jobStart" name="jobStartField" class="block mb-1 text-xs font-bold uppercase tracking-wide text-slate-500 dark:text-slate-400">Last Queue Job start</label>
                                <input type="text" name="jobStart" id="jobStart" readonly
                                       class="w-full h-10 border rounded-md px-3 text-sm bg-slate-50 dark:bg-slate-800 border-gray-300 dark:border-gray-600 text-slate-700 dark:text-slate-300">
                            </div>
                        </div>
                        <button type="button" id="refreshForceExebutton" onclick="forceExecution()"
                                class="text-white bg-sky-400 hover:bg-sky-500 flex items-center space-x-1 px-3 py-1 rounded-md h-10 w-auto">
                            <i data-lucide="play" class="w-4 h-4"></i>
                            <span>Force Execution</span>
                        </button>
                    </div>
                </div>

            </div>

            <footer class="footer">
                <div class="container-fluid" id="footer"></div>
            </footer>
        </main>
    </body>
</html>
