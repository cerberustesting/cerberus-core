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
<%@page import="org.json.JSONObject"%>
<%@page import="org.json.JSONArray"%>
<%@page import="org.cerberus.entity.TestCaseExecution"%>
<%@page import="org.cerberus.service.ITestCaseExecutionService"%>
<% Date start = new Date(); %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
    <head>
        <title>Run Manual Test Case</title>
        <meta content="text/html; charset=UTF-8" http-equiv="content-type">
        <link rel="stylesheet" type="text/css" href="css/crb_style.css">
        <link rel="stylesheet" type="text/css" href="css/jquery.dataTables.css">
        <link rel="stylesheet" type="text/css" href="css/jquery-ui.css">
        <link rel="stylesheet" type="text/css" href="css/dataTables_jui.css">
        <link rel="shortcut icon" type="image/x-icon" href="images/favicon.ico">
        <script type="text/javascript" src="js/jquery-1.9.1.min.js"></script>
        <script type="text/javascript" src="js/jquery-ui-1.10.2.js"></script>
        <script type="text/javascript" src="js/jquery.jeditable.mini.js"></script>
        <script type="text/javascript" src="js/jquery.dataTables.min.js"></script>
        <script type="text/javascript" src="js/jquery.dataTables.editable.js"></script>
        <script type="text/javascript" src="js/jquery.validate.min.js"></script>
        <script type="text/javascript" src="js/ajax-loader.js"></script>
        <style type="text/css">
            .fields {
                background-color: #E1E7F3;
                border: 2px solid #8999C4;
                display: inline-block;
                border-radius: 15px;
                padding: 5px;
                margin-bottom: 3px;
                margin-top: 3px;
            }

            .field {
                display: inline-block;
                padding-bottom: 5px;
                padding-left: 5px;
            }

            .field label {
                font-weight: bold;
                display: inline-block;
                background-color: #CAD3F1;
            }

            h4 {
                color: blue;
                margin-top: 2px;
                margin-bottom: 2px;
                font-weight: bold;
            }

            #searchTestCase {
                position: relative;
                float: left;
            }

            .ajax_loader {
                background: url("images/spinner_squares_circle.gif") no-repeat center center transparent;
                width: 100%;
                height: 100%;
            }

            .center {
                text-align: center;
            }
        </style>
    </head>
    <body>
        <%@ include file="include/function.jsp" %>
        <%@ include file="include/header.jsp" %>
        <%
            IInvariantService invariantService = appContext.getBean(IInvariantService.class);
            List<Invariant> invariants = invariantService.findListOfInvariantById("BROWSER");
            
                    %>
        <div id="searchTestCase" style="width:100%">
            <div class="fields" style="margin-left: 15px; width:97%">
                <div>
                    <h4>Search</h4>
                    <div class="field">
                        <label for="tag" style="width: 100px">Tag</label><br/>
                        <select id="tag" name="tag" style="width: 100px">
                            <option value="All">-- ALL --</option>
                        </select>
                    </div>
                </div>
                <div><input type="button" value="Search" onclick="loadTestCases()"></div>
            </div>
        </div>
        <div id="manualTestCaseExecution" style="display: none; padding-top: 25px; padding-left: 15px;">
            <div style="margin-left: 15px; width: 97%; font: 90% sans-serif">
                <table id="testCaseTable" class="display">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Test</th>
                            <th>Test Case</th>
                            <th>Environment</th>
                            <th>Country</th>
                            <th>Browser</th>
                            <th>Run</th>
                        </tr>
                    </thead>
                    <tbody>
                    </tbody>
                </table>
            </div>
        </div>
        <script>function displayOrHideElement(element) {
                var disp = document.getElementById(element).style.display;
                if (disp === "none") {
                    document.getElementById(element).style.display = "inline";
                } else {
                    document.getElementById(element).style.display = "none";
                }


            }</script>
        <script type="text/javascript">
            var oTable;
            
            
            function showMessage() {
                $("#divResultMessage").slideUp("slow");
            }

            function loadTestCases() {
                oTable = $('#testCaseTable').dataTable({
                        "bJQueryUI": true,
                        "bServerSide": false,
                        "bDestroy": true,
                        "bAutoWidth": false,
                        "sAjaxSource": "FindExecutionInQueue",
                        "sServerMethod": "POST",
                        "aoColumns": [
                            {"sName": "id", "bSortable": false, sWidth: "5%"},
                            {"sName": "test", "bSortable": false, sWidth: "20%"},
                            {"sName": "testcase", "bSortable": false, sWidth: "10%"},
                            {"sName": "environment", "bSortable": false, sWidth: "10%"},
                            {"sName": "country", "bSortable": false, sWidth: "10%"},
                            {"sName": "browser", "bSortable": false, sWidth: "10%"},
                            {"sDefaultContent": '', "bSortable": false, sWidth: "10%"}
                        ],
                        aoColumnDefs: [
                            {
                                "aTargets": [6],
                                "mRender": function(data, type, full) {
                                    return "<p style='text-align: center'><input type='button' style='background-image: url(images/play.png);background-size: 100%; width: 20px; height: 20px; border: 0 none; top: 0px' onclick='openRunManualPopin(\""+full[0]+"\",\""+full[1]+"\",\""+full[3]+"\",\""+full[2]+"\")'/></p>"
                                }
                            }
                        ]
                    });

                    $('#manualTestCaseExecution').show();
                }
            
        </script>
        <br/>
        <div id="popin"></div>
        <div style="float: left">
            <%=display_footer(start)%>
        </div>
    </body>
</html>