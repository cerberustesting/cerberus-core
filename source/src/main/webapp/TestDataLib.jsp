<%--
  ~ Cerberus  Copyright (C) 2013  vertigo17
  ~ DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
  ~
  ~ This file is part of Cerberus.
  ~
  ~ Cerberus is free software: you can redistribute it and/or modify
  ~ it under the terms of the GNU General Public License as published by
  ~ the Free Software Foundation, either version 3 of the License, or
  ~ (at your option) any later version.
  ~
  ~ Cerberus is distributed in the hope that it will be useful,
  ~ but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~ GNU General Public License for more details.
  ~
  ~ You should have received a copy of the GNU General Public License
  ~ along with Cerberus.  If not, see <http://www.gnu.org/licenses/>.
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta content="text/html; charset=UTF-8" http-equiv="content-type">
        <%@ include file="include/dependenciesInclusions.html" %>
        <title id="pageTitle">Test Data Library</title>        
        <script type="text/javascript" src="js/pages/TestDataLib.js"></script>
    </head>
    <body>
        <%@ include file="include/header.html"%>

        <div id="page-layout" class="container-fluid center">
            <%@ include file="include/messagesArea.html"%>
            <%@ include file="include/utils/modal-confirmation.html"%>            
            <%@ include file="include/testdatalib/manageTestDataLibData.html"%>
            <%@ include file="include/testdatalib/createTestDataLib.html"%>       
            <%@ include file="include/testdatalib/updateTestDataLib.html"%>       
            <%@ include file="include/testdatalib/duplicateTestDataLib.html"%>       
            <%@ include file="include/testdatalib/listTestDataLibData.html"%>       
            <%@ include file="include/testdatalib/listTestCase.html"%>       
            <%@ include file="include/utils/modal-upload.html"%>       
            



            <h1 class="page-title-line" id="title">Test Data Library</h1>
            <div id="testdatalib" class="well">               
                <table  class="table table-hover display" id="listOfTestDataLib" name="listOfTestDataLib">
                    <thead>
                        <tr>
                            <th id="testdatalibFirstColumnHeader"> Actions </th> 
                            <th>Name</th>
                            <th>System</th>
                            <th>Environment</th>
                            <th>Country</th>
                            <th>Group</th>
                            <th>Type</th>
                            <th>Database</th>
                            <th>Script</th>
                            <th>Service Path</th>
                            <th>Method</th>
                            <th>Envelope</th>
                            <th>Description</th>             
                        </tr>                        
                    </thead>
                </table>
                <div class="marginBottom20"></div>
            </div>
            <footer class="footer">
                <div class="container-fluid" id="footer"></div>
            </footer>
        </div>     
    </body> 

</html>
