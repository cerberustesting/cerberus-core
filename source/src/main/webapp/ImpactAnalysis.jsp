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
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%@ include file="include/global/dependenciesInclusions.html" %>
        <script type="text/javascript" src="dependencies/Tinymce-6.7.0/tinymce.min.js"></script>
        <script type="text/javascript" src="js/pages/ImpactAnalysis.js?v=${appVersion}"></script>
        <script type="text/javascript" src="js/transversalobject/TestCase.js?v=${appVersion}"></script>
        <title id="pageTitle">Impact Analysis</title>
    </head>
    <body x-data x-cloak class="crb_body">
        <jsp:include page="include/global/header2.html"/>
        <jsp:include page="include/global/modalInclusions.jsp"/>
        <main class="crb_main" :class="$store.sidebar.expanded ? 'crb_main_sidebar-expanded' : 'crb_main_sidebar-collapsed'">
            <%@ include file="include/global/messagesArea.html"%>
            <%@ include file="include/utils/modal-confirmation.html"%>
            <%@ include file="include/transversalobject/TestCase.html"%>

            <h1 class="page-title-line" id="title">Impact Analysis</h1>

            <!-- Test Cases Table (createDataTableWithPermissionsNew generates the header block) -->
            <div class="" id="tcList">
                <table id="tcTable" class="table table-hover display" name="tcTable"></table>
            </div>

            <!-- Hidden tabs (kept for potential future use) -->
            <div style="display:none">
                <div id="tabDataLib">
                    <div id="dlList">
                        <table id="dlTable" class="table table-hover display" name="dlTable"></table>
                    </div>
                </div>
                <div id="tabApplications">
                    <div id="appList">
                        <table id="appTable" class="table table-hover display" name="appTable"></table>
                    </div>
                </div>
                <div id="tabServices">
                    <div id="srvList">
                        <table id="srvTable" class="table table-hover display" name="srvTable"></table>
                    </div>
                </div>
            </div>

            <footer class="footer">
                <div class="container-fluid" id="footer"></div>
            </footer>
        </main>
    </body>
</html>
