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
        <meta name="active-menu" content="maintain">
        <meta name="active-submenu" content="ImpactAnalysis.jsp">
        <meta name="active-page" content="ImpactAnalysis.jsp">
        <meta name="page" content="Impact Analysis">
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%@ include file="include/global/dependenciesInclusions.html" %>
        <script type="text/javascript" src="dependencies/Tinymce-6.7.0/tinymce.min.js"></script>
        <%--
            V2 page: the table is js/global/crbTable.js driven by ImpactAnalysisV2.js.
            ImpactAnalysis.js (V1) is NOT loaded - everything it owned is reimplemented
            there. ImpactAnalysisV1.jsp is the rollback copy.
            tinymce stays: the shared TestCase modal, which the Edit action opens,
            initialises its description editor on it.
        --%>
        <script type="text/javascript" src="js/pages/ImpactAnalysisV2.js?v=${appVersion}"></script>
        <title id="pageTitle">Impact Analysis</title>
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

            <h1 class="page-title-line" id="title">Impact Analysis</h1>

            <%--
                The three hidden tables (dlTable / appTable / srvTable, "kept for
                potential future use") are gone: nothing in the codebase referenced
                those ids, no JS ever initialised them, and they were inside a
                display:none wrapper. They are in ImpactAnalysisV1.jsp if ever needed.
            --%>
            <div id="impactAnalysisList"></div>

            <footer class="footer">
                <div class="container-fluid" id="footer"></div>
            </footer>
            <jsp:include page="include/global/aiBottomBar.html"/>
        </main>
    </body>
</html>
