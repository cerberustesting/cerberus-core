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
<%-- 
    Document   : Test2
    Created on : 23 sept. 2015, 16:07:19
    Author     : cerberus
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html class="h-full">
    <head>
        <meta name="active-menu" content="maintain">
        <meta name="active-submenu" content="TestCaseList.jsp">
        <meta content="text/html; charset=UTF-8" http-equiv="content-type">
        <%@ include file="include/global/dependenciesInclusions.html" %>
        <script type="text/javascript" src="dependencies/Tinymce-6.7.0/tinymce.min.js"></script>
        <script type="text/javascript" src="dependencies/Bootstrap-treeview-1.2.0/js/bootstrap-treeview.js"></script>
        <script type="text/javascript" src="js/pages/TestCaseList.js"></script>
        <script type="text/javascript" src="js/transversalobject/TestCase.js"></script>
        <script type="text/javascript" src="js/transversalobject/TestCaseSimpleExecution.js"></script>
        <script type="text/javascript" src="js/transversalobject/Application.js"></script>
        <title id="pageTitle">Test Case</title>
    </head>
    <body x-data x-cloak class="crb_body">
        <jsp:include page="include/global/header2.html"/>
        <main class="crb_main" :class="$store.sidebar.expanded ? 'crb_main_sidebar-expanded' : 'crb_main_sidebar-collapsed'">
            <div>
                <%@ include file="include/global/messagesArea.html"%>
                <%@ include file="include/utils/modal-confirmation.html"%>
                <%@ include file="include/transversalobject/TestCase.html"%>
                <jsp:include page="include/transversalobject/TestCaseSimpleCreation.html"/>
                <%@ include file="include/transversalobject/TestCaseSimpleExecution.html"%>
                <%@ include file="include/transversalobject/Application.html"%>
                <%@ include file="include/pages/testcaselist/importTestCaseFromTestLink.html"%>
                <jsp:include page="include/transversalobject/TestCaseListMassActionUpdate.html"/>
                <jsp:include page="include/transversalobject/TestCaseListMassActionLabel.html"/>
                <jsp:include page="include/transversalobject/TestCaseSimpleCreationImport.html"/>
                <jsp:include page="include/templates/selectDropdown.html"/>

                <h1 class="page-title-line" id="title">Test Case</h1>

                <div class="">
                    <form id="massActionForm" name="massActionForm"  title="" role="form">
                        <div id="testCaseList">
                            <table id="testCaseTable" class="table table-hover display" name="testCaseTable"></table>
                            <div class="marginBottom20"></div>
                        </div>
                    </form>
                </div>

                <footer class="footer">
                    <div class="container-fluid" id="footer"></div>
                </footer>
            </div>
        </main>
    </body>
</html>
