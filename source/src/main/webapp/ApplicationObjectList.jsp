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
        <meta name="active-submenu" content="ApplicationObjectList.jsp">
        <meta name="active-page" content="ApplicationObjectList.jsp">
        <meta name="page" content="Application Object">
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%@ include file="include/global/dependenciesInclusions.html" %>
        <script type="text/javascript" src="js/pages/ApplicationObjectList.js"></script>
        <script type="text/javascript" src="js/transversalobject/ApplicationObject.js"></script>
        <title id="pageTitle">Application Object</title>
    </head>
    <body x-data x-cloak class="crb_body">
        <jsp:include page="include/global/header2.html"/>
        <jsp:include page="include/global/modalInclusions.jsp"/>
        <main class="crb_main" :class="$store.sidebar.expanded ? 'crb_main_sidebar-expanded' : 'crb_main_sidebar-collapsed'">
            <%@ include file="include/global/messagesArea.html"%>
            <%@ include file="include/utils/modal-confirmation.html"%>
            <%@ include file="include/utils/modal-generic.html"%>
            <%@ include file="include/transversalobject/ApplicationObject.html"%>


            <h1 class="page-title-line" id="title" x-text="$store.labels.getLabel('applicationObject','title')"></h1>
            <p class="page-subtitle-line" x-text="$store.labels.getLabel('applicationObject','subtitle')"></p>

            <div>
                <div id="applicationObjectList">
                    <table id="applicationObjectsTable" class="table table-hover display" name="applicationObjectsTable"></table>
                    <div class="marginBottom20"></div>
                </div>
            </div>
            <footer class="footer">
                <div class="container-fluid" id="footer"></div>
            </footer>
            <jsp:include page="include/global/aiBottomBar.html"/>
        </div>
        </main>
    </body>
</html>
