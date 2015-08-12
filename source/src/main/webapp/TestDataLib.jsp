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
<% Date DatePageStart = new Date();%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta content="text/html; charset=UTF-8" http-equiv="content-type">
        <title>Test Data Library</title>
        <link rel="shortcut icon" type="image/x-icon" href="images/favicon.ico">

        <link rel="stylesheet" type="text/css" href="css/crb_style.css">
        <link type="text/css" rel="stylesheet" href="css/dataTables.colVis.css">
        <link rel="stylesheet" type="text/css" href="css/jquery.dataTables.css">
        
        <link type="text/css" rel="stylesheet" href="css/bootstrap.css">
        <link rel="stylesheet" href="css/bootstrap-multiselect.css" type="text/css">
        
        
        <link type="text/css" rel="stylesheet" href="css/default.css"> 

        <script type="text/javascript" src="js/jquery-1.9.1.min.js"></script>
        
        <script type="text/javascript" src="js/jquery.dataTables.js"></script>
        <script type="text/javascript" src="js/dataTables.colVis.js"></script>
        
        <script type="text/javascript" src="js/jquery.blockUI.js"></script>
        <script type="text/javascript" src="js/doc.js"></script>
        <script type="text/javascript" src="js/pages/TestDataLibViewModel.js"></script>
        
        <script type="text/javascript" src="js/bootstrap.min.js"></script>         
        <script type="text/javascript" src="js/bootstrap-multiselect.js"></script>
          
    </head>
    <body>
        <%@ include file="include/header.jsp"%>
 
        <div id="page-layout" class="container center">
            <%@ include file="include/messagesArea.html"%>
            <%@ include file="include/utils/modal-confirmation.html"%>
            <%@ include file="include/testdatalib/editTestDataLibData.html"%>
                    <!-- form that adds the new library -->
            <%@ include file="include/testdatalib/addTestDataLib.jsp"%>       
            <%@ include file="include/testdatalib/editTestDataLib.html"%>       
            <%@ include file="include/testdatalib/viewTestDataLibData.html"%>       
            <%@ include file="include/testdatalib/testCaseList.html"%>       
            <%@ include file="include/utils/modal-upload.html"%>       
            
            
            
            <h1 class="page-title-line">Test Data Library</h1>
            <div id="testdatalib" class="well">               
                <table  class="table table-hover display" id="listOfTestDataLib" name="listOfTestDataLib">
                    <thead>
                        <tr>
                            <th id="testdatalibFirstColumnHeader"> Edit | Delete | Subdata  |Test Cases </th> 
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
        </div>     
            
                
                
        

    </body> 

</html>
