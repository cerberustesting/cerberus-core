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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html class="h-full">
    <head>
        <meta name="active-menu" content="monitor">
        <meta name="active-submenu" content="TestCaseExecution.jsp">
        <meta name="active-page" content="TestCaseExecutionV2.jsp">
        <meta content="text/html; charset=UTF-8" http-equiv="content-type">
        <%@ include file="include/global/dependenciesInclusions.html" %>
        <title id="pageTitle">Execution Detail</title>

        <!-- Dependencies for TestCase Header Modal -->
        <script type="text/javascript" src="dependencies/Tinymce-6.7.0/tinymce.min.js"></script>
        <script type="text/javascript" src="dependencies/Bootstrap-treeview-1.2.0/js/bootstrap-treeview.js"></script>

        <!-- Chart.js for Network tab -->
        <script type="text/javascript" src="dependencies/Chart.js-2.9.3/Chart.min.js"></script>

        <!-- V2 Scripts -->
        <script type="text/javascript" src="js/pages/TestCaseExecutionV2.js?v=${appVersion}"></script>
        <script type="text/javascript" src="js/transversalobject/TestCaseSimpleExecution.js?v=${appVersion}"></script>

        <!-- V2 Styles -->
        <link rel="stylesheet" type="text/css" href="css/pages/TestCaseExecutionV2.css?v=${appVersion}">
    </head>
    <body x-data x-cloak class="crb_body">
        <jsp:include page="include/global/header2.html"/>
        <jsp:include page="include/global/modalInclusions.jsp"/>
        <main class="crb_main" x-init="$store.sidebar.expanded = false"
              :class="$store.sidebar.expanded ? 'crb_main_sidebar-expanded' : 'crb_main_sidebar-collapsed'">

            <%@ include file="include/global/messagesArea.html"%>
            <%@ include file="include/utils/modal-generic.html"%>

            <!-- Execution modal -->
            <jsp:include page="include/transversal/TestCaseSimpleExecution.html"/>

            <!-- Templates -->
            <%@ include file="include/templates/selectDropdown.html"%>

            <!-- ============================================================ -->
            <!-- MAIN V2 CONTENT — Pure Alpine.js                             -->
            <!-- ============================================================ -->
            <div x-data="executionV2()" x-init="init()" id="executionV2Root">

                <!-- HEADER BAR -->
                <%@ include file="include/pages/testcaseexecutionv2/headerBar.html"%>

                <!-- TABS -->
                <div class="w-full flex bg-slate-200 dark:bg-slate-700 p-1 rounded-lg shadow-sm mb-4 h-10">
                    <!-- Tab: Steps -->
                    <button @click="setTab('steps')" id="v2ExeTabSteps"
                            :class="tab === 'steps' ? 'crb_tab_selected' : 'crb_tab_not_selected'"
                            class="flex-1 flex items-center justify-center gap-2 px-4 py-3 rounded-md transition-colors duration-200">
                        <i data-lucide="list-tree" class="w-4 h-4"></i>
                        <span>Steps</span>
                    </button>
                    <!-- Tab: Properties -->
                    <button @click="setTab('properties')" id="v2ExeTabProperties"
                            :class="tab === 'properties' ? 'crb_tab_selected' : 'crb_tab_not_selected'"
                            class="flex-1 flex items-center justify-center gap-2 px-4 py-3 rounded-md transition-colors duration-200">
                        <i data-lucide="variable" class="w-4 h-4"></i>
                        <span>Properties</span>
                    </button>
                    <!-- Tab: Bug -->
                    <button @click="setTab('bug')" id="v2ExeTabBug"
                            :class="tab === 'bug' ? 'crb_tab_selected' : 'crb_tab_not_selected'"
                            class="flex-1 flex items-center justify-center gap-2 px-4 py-3 rounded-md transition-colors duration-200">
                        <i data-lucide="bug" class="w-4 h-4"></i>
                        <span>Bug</span>
                    </button>
                    <!-- Tab: Environment -->
                    <button @click="setTab('env')" id="v2ExeTabEnv"
                            :class="tab === 'env' ? 'crb_tab_selected' : 'crb_tab_not_selected'"
                            class="flex-1 flex items-center justify-center gap-2 px-4 py-3 rounded-md transition-colors duration-200">
                        <i data-lucide="earth" class="w-4 h-4"></i>
                        <span>Environment</span>
                    </button>
                    <!-- Tab: Robot -->
                    <button @click="setTab('robot')" id="v2ExeTabRobot"
                            :class="tab === 'robot' ? 'crb_tab_selected' : 'crb_tab_not_selected'"
                            class="flex-1 flex items-center justify-center gap-2 px-4 py-3 rounded-md transition-colors duration-200">
                        <i data-lucide="bot" class="w-4 h-4"></i>
                        <span>Robot</span>
                    </button>
                    <!-- Tab: Network -->
                    <button @click="setTab('network')" id="v2ExeTabNetwork"
                            :class="tab === 'network' ? 'crb_tab_selected' : 'crb_tab_not_selected'"
                            class="flex-1 flex items-center justify-center gap-2 px-4 py-3 rounded-md transition-colors duration-200">
                        <i data-lucide="network" class="w-4 h-4"></i>
                        <span>Network</span>
                    </button>
                    <!-- Tab: Traceability -->
                    <button @click="setTab('traca')" id="v2ExeTabTraca"
                            :class="tab === 'traca' ? 'crb_tab_selected' : 'crb_tab_not_selected'"
                            class="flex-1 flex items-center justify-center gap-2 px-4 py-3 rounded-md transition-colors duration-200">
                        <i data-lucide="user" class="w-4 h-4"></i>
                        <span>Traceability</span>
                    </button>
                </div>

                <!-- TAB: STEPS -->
                <div x-show="tab === 'steps'" x-cloak class="grid grid-cols-[320px_1fr] gap-3 items-start">
                    <%@ include file="include/pages/testcaseexecutionv2/stepList.html"%>
                    <%@ include file="include/pages/testcaseexecutionv2/stepDetail.html"%>
                </div>

                <!-- TAB: Other tabs -->
                <%@ include file="include/pages/testcaseexecutionv2/tabPanels.html"%>

            </div>

            <footer class="footer">
                <div id="footer" style="display: inline-block"></div>
            </footer>
            <jsp:include page="include/global/aiBottomBar.html"/>
        </main>
    </body>
</html>
