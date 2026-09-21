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
        <meta name="active-page" content="TestCaseExecutionDebug.jsp">
        <meta content="text/html; charset=UTF-8" http-equiv="content-type">
        <%@ include file="include/global/dependenciesInclusions.html" %>
        <title id="pageTitle">Test Case Debug</title>

        <!-- V2 Scripts -->
        <script type="text/javascript" src="js/testcase/testcaseStatic.js?v=${appVersion}"></script>
        <script type="text/javascript" src="js/testcase/condition.js?v=${appVersion}"></script>
        <script type="text/javascript" src="js/pages/TestCaseExecutionDebug.js?v=${appVersion}"></script>
    </head>
    <body x-data x-cloak class="crb_body" :class="$store.rightPanel.open ? 'rp-open' : ''">
        <jsp:include page="include/global/header2.html"/>

        <!-- Step Options modals (Options gear on TestCaseStep.html's action/control rows + step header).
             Included before modalInclusions.jsp so its z-index-on-open observer (which only scans
             .crb_modal nodes present in the DOM at that point) picks these up too; otherwise opening
             one from within TestCaseStep.html's compact step editor renders it behind that modal. -->
        <%@ include file="include/pages/testcasescript/manageActionControlOptions.html"%>
        <%@ include file="include/pages/testcasescript/manageStepOptions.html"%>

        <jsp:include page="include/global/modalInclusions.jsp"/>
        <jsp:include page="include/global/rightPanel.html"/>
        <main class="crb_main_wrp" x-init="$store.sidebar.expanded = false"
              :class="$store.rightPanel.isResizing ? '' : 'transition-all duration-200'"
              :style="{marginLeft: ($store.sidebar.hidden ? 0 : ($store.sidebar.expanded ? 288 : 80)) + 'px',
                      width: 'calc(100vw - ' + ($store.sidebar.hidden ? 0 : ($store.sidebar.expanded ? 288 : 80))
                          + 'px - '+ ($store.rightPanel.open ? $store.rightPanel.width : 0) + 'px)'}">

            <%@ include file="include/global/messagesArea.html"%>

            <!-- ============================================================ -->
            <!-- MAIN DEBUG CONTENT — Pure Alpine.js                          -->
            <!-- ============================================================ -->
            <div class="flex items-start justify-between gap-4 mb-4">
                <h1 class="page-title-line mb-0" id="title">Test Case Execution Debug</h1>
            </div>
            <div x-data="debugExecutionV2()" x-init="init()" id="debugExecutionV2Root">

                <!-- HEADER / CONTROL BAR -->
                <%@ include file="include/pages/testcaseexecutiondebug/controlBar.html"%>

                <!-- MAIN LAYOUT : live view + page source (main column) + instructions/elements (side) -->
                <div class="grid grid-cols-[1fr_480px] gap-3 items-start">
                    <div class="flex flex-col gap-3">
                        <%@ include file="include/pages/testcaseexecutiondebug/liveViewPanel.html"%>
                        <%@ include file="include/pages/testcaseexecutiondebug/pageSourcePanel.html"%>
                    </div>
                    <div class="flex flex-col gap-3">
                        <%@ include file="include/pages/testcaseexecutiondebug/stepList.html"%>
                        <%@ include file="include/pages/testcaseexecutiondebug/elementsPanel.html"%>
                    </div>
                </div>

            </div>

            <footer class="footer">
                <div id="footer" style="display: inline-block"></div>
            </footer>
            <jsp:include page="include/global/aiBottomBar.html"/>
        </main>
    </body>
</html>
