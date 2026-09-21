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
        <meta name="active-menu" content="settings">
        <meta name="active-submenu" content="Environment.jsp">
        <meta name="active-page" content="Environment.jsp">
        <meta name="page" content="Environment List">
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%@ include file="include/global/dependenciesInclusions.html" %>
        <%--
            V2 page: the list is js/global/crbTable.js driven by EnvironmentV2.js.
            Environment.js (V1) is NOT loaded. EnvironmentV1.jsp is the rollback copy.
            The two tables INSIDE the edit modal (last changes, last events) stay
            legacy DataTables - they are modal content, not this page's list.
        --%>
        <script type="text/javascript" src="js/pages/EnvironmentV2.js?v=${appVersion}"></script>
        <title id="pageTitle">Environment</title>
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
            <%@ include file="include/pages/environment/addEnvironment.html"%>
            <%@ include file="include/pages/environment/editEnvironment.html"%>
            <%@ include file="include/pages/environment/eventEnable.html"%>
            <%@ include file="include/pages/environment/eventDisable.html"%>
            <%@ include file="include/pages/environment/eventNewChain.html"%>

            <h1 class="page-title-line" id="title">Environment</h1>

            <div id="environmentList"></div>

            <footer class="footer">
                <div class="container-fluid" id="footer"></div>
            </footer>
            <jsp:include page="include/global/aiBottomBar.html"/>
        </main>
    </body>
</html>
