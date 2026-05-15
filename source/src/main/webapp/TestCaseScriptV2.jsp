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
        <meta name="active-menu" content="maintain">
        <meta name="active-submenu" content="TestCaseList.jsp">
        <meta name="active-page" content="TestCaseScriptV2.jsp">
        <meta content="text/html; charset=UTF-8" http-equiv="content-type">
        <%@ include file="include/global/dependenciesInclusions.html" %>
        <title id="pageTitle">Test Case Script</title>

        <!-- Ace Editor extensions -->
        <script type="text/javascript" src="dependencies/Ace-1.2.6/ext-language_tools.js"></script>

        <!-- Shared static data (action/control/condition definitions) -->
        <script type="text/javascript" src="js/testcase/testcaseStatic.js?v=${appVersion}"></script>
        <script type="text/javascript" src="js/testcase/condition.js?v=${appVersion}"></script>
        <script type="text/javascript" src="js/testcase/step.js?v=${appVersion}"></script>

        <!-- Dependencies for TestCase Header Modal -->
        <script type="text/javascript" src="dependencies/Tinymce-6.7.0/tinymce.min.js"></script>
        <script type="text/javascript" src="dependencies/Bootstrap-treeview-1.2.0/js/bootstrap-treeview.js"></script>

        <!-- V2 Scripts -->
        <script type="text/javascript" src="js/pages/TestCaseScriptV2.js?v=${appVersion}"></script>
        <script type="text/javascript" src="js/transversalobject/TestCaseSimpleExecution.js?v=${appVersion}"></script>

        <!-- V2 Styles -->
        <link rel="stylesheet" type="text/css" href="css/pages/TestCaseScriptV2.css?v=${appVersion}">
    </head>
    <body x-data x-cloak class="crb_body">
        <jsp:include page="include/global/header2.html"/>
        <jsp:include page="include/global/modalInclusions.jsp"/>
        <main class="crb_main" x-init="$store.sidebar.expanded = false"
              :class="$store.sidebar.expanded ? 'crb_main_sidebar-expanded' : 'crb_main_sidebar-collapsed'">

            <%@ include file="include/global/messagesArea.html"%>
            <%@ include file="include/utils/modal-confirmation.html"%>

            <!-- V2 Modals -->
            <%@ include file="include/pages/testcasescript/manageActionControlOptions.html"%>
            <%@ include file="include/pages/testcasescript/manageProperties.html"%>
            <%@ include file="include/pages/testcasescript/addStep.html"%>

            <!-- Execution modal -->
            <jsp:include page="include/transversal/TestCaseSimpleExecution.html"/>
            <%@ include file="include/utils/modal-generic.html"%>

            <!-- ============================================================ -->
            <!-- REUSABLE TEMPLATES                                           -->
            <!-- ============================================================ -->
            <%@ include file="include/templates/selectDropdown.html"%>

            <!-- ============================================================ -->
            <!-- MAIN V2 CONTENT — Pure Alpine.js                             -->
            <!-- ============================================================ -->
            <div x-data="scriptV2()" x-init="init()" id="scriptV2Root">

                <!-- HEADER BAR -->
                <%@ include file="include/pages/testcasescriptv2/headerBar.html"%>

                <!-- TABS -->
                <div class="w-full flex bg-slate-200 dark:bg-slate-700 p-1 rounded-lg shadow-sm mb-4 h-10">
                    <button @click="setTab('steps')" id="v2TabSteps"
                            :class="tab === 'steps' ? 'crb_tab_selected' : 'crb_tab_not_selected'"
                            class="flex-1 flex items-center justify-center gap-2 px-4 py-3 rounded-md transition-colors duration-200">
                        <i data-lucide="layers" class="w-4 h-4"></i>
                        <span>Steps</span>
                    </button>
                    <button @click="setTab('properties')" id="v2TabProperties"
                            :class="tab === 'properties' ? 'crb_tab_selected' : 'crb_tab_not_selected'"
                            class="flex-1 flex items-center justify-center gap-2 px-4 py-3 rounded-md transition-colors duration-200">
                        <i data-lucide="variable" class="w-4 h-4"></i>
                        <span>Properties</span>
                        <template x-if="unusedPropertiesCount > 0">
                            <span class="inline-flex items-center justify-center min-w-[18px] h-[18px] px-1 rounded-full text-[10px] font-bold bg-amber-100 dark:bg-amber-900/30 text-amber-600 dark:text-amber-400"
                                  x-text="unusedPropertiesCount"></span>
                        </template>
                    </button>
                </div>

                <!-- TAB: STEPS -->
                <div x-show="tab === 'steps'" x-cloak class="grid grid-cols-[320px_1fr] gap-3 items-start">
                    <%@ include file="include/pages/testcasescriptv2/stepList.html"%>
                    <%@ include file="include/pages/testcasescriptv2/actionEditor.html"%>
                </div>

                <!-- TAB: PROPERTIES -->
                <div x-show="tab === 'properties'" x-cloak>
                    <%@ include file="include/pages/testcasescriptv2/propertyPanel.html"%>
                </div>

            </div>

            <footer class="footer">
                <div id="footer" style="display: inline-block"></div>
            </footer>
            <jsp:include page="include/global/aiBottomBar.html"/>
        </main>
    </body>
</html>
