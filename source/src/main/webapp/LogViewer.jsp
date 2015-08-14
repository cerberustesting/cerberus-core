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
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%@ include file="include/dependenciesInclusions.html" %>
        <script type="text/javascript" src="js/pages/LogViewer.js"></script>
        <title>Log Viewer</title>

<!--        <script type="text/javascript">      
            $(document).ready(function(){
                $('#logsTable').dataTable({
                    "aLengthMenu": [
                        [20, 50, 100, 200],
                        [20, 50, 100, 200]
                    ], 
                    "aaSorting": [[ 0, "desc" ]],
                    "iDisplayLength" : 50,
                    "bServerSide": true,
                    "sAjaxSource": "GetLogEvent",
                    "bJQueryUI": true,
                    "bProcessing": false,
                    "sPaginationType": "full_numbers",
                    "bSearchable": false, "aTargets": [ 0 ],
                    "aoColumns": [
                        {"mDataProp": "time", "sName": "Time", "sWidth": "15%"},
                        {"mDataProp": "login", "sName": "Login", "sWidth": "10%"},
                        {"mDataProp": "page", "sName": "Page", "sWidth": "15%"},
                        {"mDataProp": "action", "sName": "Action", "sWidth": "10%"},
                        {"mDataProp": "log", "sName": "Log", "sWidth": "50%"}
                    ]
                });
                
                <%
                if(request.getParameter("Test") != null && request.getParameter("TestCase") != null) {
                    String search = request.getParameter("Test") + "'|'"+request.getParameter("TestCase");
                    %>$("#logsTable_filter input[type='search']").delay(1000).val("<%=search%>").trigger( "change" );<%
                }
                %>
                        
                
            });
        </script>-->
    </head>
     <body>
        <%@ include file="include/header.html" %>
        <div class="container-fluid center" id="page-layout">

            <h1 class="page-title-line" id="title">Log Viewer</h1>
            <div id="logViewer" class="well">
                <table id="logViewerTable" class="table table-hover display" name="logViewerTable"></table>
                <div class="marginBottom20"></div>
            </div>
            <footer class="footer">
                <div class="container-fluid" id="footer"></div>
            </footer>
        </div>
    </body>
</html>
