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
<html>
    <head>
        <meta content="text/html; charset=UTF-8" http-equiv="content-type">
        <%@ include file="include/global/dependenciesInclusions.html" %>
        <script type="text/javascript" src="dependencies/Tinymce-6.7.0/tinymce.min.js"></script>
        <script type="text/javascript" src="dependencies/Bootstrap-treeview-1.2.0/js/bootstrap-treeview.js"></script>
        <script type="text/javascript" src="js/pages/TestCaseList.js"></script>
        <script type="text/javascript" src="js/transversalobject/TestCase.js"></script>
        <script type="text/javascript" src="js/transversalobject/TestCaseSimpleExecution.js"></script>
        <script type="text/javascript" src="js/transversalobject/Application.js"></script>
        <title id="pageTitle">Test Case List</title>
    </head>
    <body>
        <%@ include file="include/global/header.html"%>
        <div class="container-fluid center" id="page-layout">
            <%@ include file="include/global/messagesArea.html"%>
            <%@ include file="include/utils/modal-confirmation.html"%>
            <%@ include file="include/transversalobject/TestCase.html"%>
            <%@ include file="include/transversalobject/TestCaseSimpleExecution.html"%>
            <%@ include file="include/transversalobject/Application.html"%>
            <%@ include file="include/pages/testcaselist/massActionTestCase.html"%>
            <%@ include file="include/pages/testcaselist/importTestCase.html"%>
            <%@ include file="include/pages/testcaselist/importTestCaseFromSIDE.html"%>
            <%@ include file="include/pages/testcaselist/importTestCaseFromTestLink.html"%>

            <h1 class="page-title-line" id="title">Test Case List</h1>

            <div class="panel panel-default">
                <form id="massActionForm" name="massActionForm"  title="" role="form">
                    <div class="panel-body" id="testCaseList">
                        <table id="testCaseTable" class="table table-bordered table-hover display" name="testCaseTable"></table>
                        <div class="marginBottom20"></div>
                    </div>
                </form>
            </div>

            <footer class="footer">
                <div class="container-fluid" id="footer"></div>
            </footer>
        </div>
    </body>
</html>
