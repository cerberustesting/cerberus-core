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
<html>
    <head>
        <meta content="text/html; charset=UTF-8" http-equiv="content-type">
        <%@ include file="include/global/dependenciesInclusions.html" %>
        <title id="pageTitle">Test Data Library</title>        
        <script type="text/javascript" src="js/pages/TestDataLibList.js"></script>
        <script type="text/javascript" src="js/transversalobject/TestDataLib.js"></script>
        <script type="text/javascript" src="js/transversalobject/AppService.js"></script>
        <script type="text/javascript" src="js/global/autocomplete.js"></script>
    </head>
    <body>
        <%@ include file="include/global/header.html"%>

        <div id="page-layout" class="container-fluid center">     
            <%@ include file="include/global/messagesArea.html"%>
            <%@ include file="include/utils/modal-confirmation.html"%>
            <%@ include file="include/utils/modal-upload.html"%>
            <%@ include file="include/transversalobject/TestDataLib.html"%>              
            <%@ include file="include/pages/testdatalib/listTestCase.html"%> 
            <%@ include file="include/pages/testdatalib/bulkRename.html"%> 
            <%@ include file="include/transversalobject/AppService.html"%>      
            <h1 class="page-title-line" id="title">Test Data Library</h1>
            <div class="panel panel-default">
                <div class="panel-body" id="testDataLibList">
                    <table id="listOfTestDataLib" class="table table-bordered table-hover display" name="listOfTestDataLib"></table>
                    <div class="marginBottom20"></div>
                </div>
            </div>
            <footer class="footer">
                <div class="container-fluid" id="footer"></div>
            </footer>
        </div>     
    </body> 

</html>
