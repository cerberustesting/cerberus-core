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
<%@page import="org.cerberus.crud.entity.TestCaseExecution"%>
<%@page import="org.cerberus.crud.service.ITestCaseExecutionService"%>
<% Date start = new Date();
   %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
    <head>
        <title>Execution in Queue</title>
        <meta content="text/html; charset=UTF-8" http-equiv="content-type">

        <link rel="stylesheet" type="text/css" href="dependencies/zz_OldDependencies/css/jquery.dataTables.tableTools.css">
        <%@ include file="include/dependenciesInclusions_old.html" %>

        <script type="text/javascript" src="dependencies/zz_OldDependencies/js/ajax-loader.js"></script>
        <script type="text/javascript" src="dependencies/zz_OldDependencies/js/jquery.dataTables.tableTools.js"></script>
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

            #testCaseTable_wrapper{
                background-image: -moz-linear-gradient(bottom, #ebebeb, #CCCCCC); 
                background-image: -webkit-linear-gradient(bottom, #ebebeb, #CCCCCC); 
                font-weight:bold;
                font-family: Trebuchet MS;
                color:#555555;
                text-align: center;
            }

            table.dataTable tbody tr.selected {
                background-color: #b0bed9;
            }
        </style>
    </head>
    <body>
        <%@ include file="include/function.jsp" %>
        <%@ include file="include/header.jsp" %>
        <div id="manualTestCaseExecution" style="display: none;">
            <p class="dttTitle">Execution In Queue</p>
            <div style="width: 100%; font: 90% sans-serif">
                <table id="testCaseTable" class="display">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Test</th>
                            <th>Test Case</th>
                            <th>Environment</th>
                            <th>Country</th>
                            <th>Browser</th>
                            <th>Tag</th>
                            <th>Processed</th>
                            <th>Run</th>
                        </tr>
                    </thead>
                    <tbody>
                    </tbody>
                </table>
            </div>
        </div>
        <script type="text/javascript">
            var oTable;
            function showMessage() {
                $("#divResultMessage").slideUp("slow");
            }

            function loadTestCases() {
                oTable = $('#testCaseTable').dataTable({
                    "bJQueryUI": true,
                    "bServerSide": false,
                    "Searching": true,
                    "bDestroy": true,
                    "bAutoWidth": false,
                    "sAjaxSource": "FindExecutionInQueue",
                    "sServerMethod": "POST",
                    dom: 'T<"clear">lfrtip',
                    tableTools: {
                        "sRowSelect": "multi",
                        "aButtons": [
                            {"sExtends": "select",
                                "sButtonText": "Select All",
                                "fnClick": function (nButton, oConfig, oFlash) {
                                    var oTT = TableTools.fnGetInstance('testCaseTable');
                                    oTT.fnSelectAll(true); //True = Select only filtered rows (true). Optional - default false.
                                    checkSelected();
                                }},
                            {"sExtends": "select",
                                "sButtonText": "Select None",
                                "fnClick": function (nButton, oConfig, oFlash) {
                                    var oTT = TableTools.fnGetInstance('testCaseTable');
                                    oTT.fnSelectNone(true); //True = Select only filtered rows (true). Optional - default false.
                                    checkSelected();
                                }},
                            {
                                "sExtends": "ajax",
                                "sButtonText": "Delete",
                                "sAjaxUrl": "DeleteExecutionInQueue",
                                "bHeader": false,
                                "bSelectedOnly": true,
                                "mColumns": [0],
                                "sFieldSeperator": "|",
                                "fnClick": function (nButton, oConfig) {
                                    var sData = this.fnGetTableData(oConfig);
                                    if (!$(nButton).hasClass('DTTT_disabled') && confirm('Do you really want to delete?')) {
                                        $.ajax({
                                            "url": "DeleteExecutionInQueue",
                                            "data": [
                                                {
                                                    "name": "tableData",
                                                    "value": sData
                                                }
                                            ],
                                            "success": function () {
                                                loadTestCases()
                                            },
                                            "dataType": "json",
                                            "type": "POST",
                                            "cache": false,
                                            "error": function () {
                                                alert("Error detected when sending table data to server");
                                            }
                                        });
                                    }
                                    else {
                                        alert('Not deleted');
                                    }
                                },
                                "oSelectorOpts": {filter: 'applied', order: 'current'}
                            },
                            {
                                "sExtends": "collection",
                                "sButtonText": "Change Processed",
                                "aButtons": [{
                                        "sExtends": "ajax",
                                        "sButtonText": "To Processed",
                                        "sAjaxUrl": "UpdateExecutionInQueue?changeTo=1",
                                        "bSelectedOnly": true,
                                        "sRowSelect": "multi",
                                        "bHeader": false,
                                        "mColumns": [0],
                                        "bOpenRows": true,
                                        "sFieldSeperator": "|",
                                        "fnClick": function (nButton, oConfig) {
                                            var sData = this.fnGetTableData(oConfig);
                                            if (!$(nButton).hasClass('DTTT_disabled')) {
                                                $.ajax({
                                                    "url": "UpdateExecutionInQueue?changeTo=1",
                                                    "data": [
                                                        {
                                                            "name": "tableData",
                                                            "value": sData
                                                        }
                                                    ],
                                                    "success": function () {
                                                        loadTestCases()
                                                    },
                                                    "dataType": "json",
                                                    "type": "POST",
                                                    "cache": false,
                                                    "error": function () {
                                                        alert("Error detected when sending table data to server");
                                                    }
                                                });
                                            }
                                        },
                                        "oSelectorOpts": {filter: 'applied', order: 'current'}
                                    }, {
                                        "sExtends": "ajax",
                                        "sButtonText": "To Not processed",
                                        "sAjaxUrl": "UpdateExecutionInQueue?changeTo=0",
                                        "bSelectedOnly": true,
                                        "sRowSelect": "multi",
                                        "success": function () {
                                            loadTestCases()
                                        },
                                        "bHeader": false,
                                        "mColumns": [0],
                                        "bOpenRows": true,
                                        "sFieldSeperator": "|", "fnClick": function (nButton, oConfig) {
                                            var sData = this.fnGetTableData(oConfig);
                                            if (!$(nButton).hasClass('DTTT_disabled')) {
                                                $.ajax({
                                                    "url": "UpdateExecutionInQueue?changeTo=0",
                                                    "data": [
                                                        {
                                                            "name": "tableData",
                                                            "value": sData
                                                        }
                                                    ],
                                                    "success": function () {
                                                        loadTestCases()
                                                    },
                                                    "dataType": "json",
                                                    "type": "POST",
                                                    "cache": false,
                                                    "error": function () {
                                                        alert("Error detected when sending table data to server");
                                                    }
                                                });
                                            }
                                        },
                                        "oSelectorOpts": {filter: 'applied', order: 'current'}
                                    }]
                            }]},
                    "aoColumns": [
                        {"sName": "id", "bSortable": false, sWidth: "5%"},
                        {"sName": "test", "bSortable": false, sWidth: "20%"},
                        {"sName": "testcase", "bSortable": false, sWidth: "10%"},
                        {"sName": "environment", "bSortable": false, sWidth: "10%"},
                        {"sName": "country", "bSortable": false, sWidth: "10%"},
                        {"sName": "browser", "bSortable": false, sWidth: "10%"},
                        {"sName": "tag", "bSortable": false, sWidth: "10%"},
                        {"sName": "processed", "bSortable": false, sWidth: "10%"},
                        {"sDefaultContent": '', "bSortable": false, sWidth: "10%"}
                    ],
                    aoColumnDefs: [
                        {
                            "aTargets": [8],
                            "mRender": function (data, type, full) {
                                return "<p style='text-align: center'><input type='button' style='background-image: url(images/play.png);background-size: 100%; width: 20px; height: 20px; border: 0 none; top: 0px' onclick=window.location.href='RunTests.jsp?queuedExecution=" + full[0] + "'></p>"
                            }
                        }
                    ]
                });
                $('#manualTestCaseExecution').show();
                $('#testCaseTable tbody').on('click', 'tr', function () {
                    $(this).toggleClass('selected');
                    checkSelected();
                });
                checkSelected();
            }

            function checkSelected() {
                if ($("#testCaseTable tbody").find('tr.DTTT_selected').length > 0) {
                    $('#ToolTables_testCaseTable_0').addClass('DTTT_disabled');
                    $('#ToolTables_testCaseTable_2').removeClass('DTTT_disabled');
                    $('#ToolTables_testCaseTable_3').removeClass('DTTT_disabled');
                    $('#ToolTables_testCaseTable_4').removeClass('DTTT_disabled');
                    $('#ToolTables_testCaseTable_5').removeClass('DTTT_disabled');
                } else {
                    $('#ToolTables_testCaseTable_0').removeClass('DTTT_disabled');
                    $('#ToolTables_testCaseTable_2').addClass('DTTT_disabled');
                    $('#ToolTables_testCaseTable_3').addClass('DTTT_disabled');
                    $('#ToolTables_testCaseTable_4').addClass('DTTT_disabled');
                    $('#ToolTables_testCaseTable_5').addClass('DTTT_disabled');
                }
            }

        </script>
        <script>
            $(document).ready(function () {
                loadTestCases();
            });
        </script>
        <br/>
        <div id="popin"></div>
        <div style="float: left">
            <%=display_footer(start)%>
        </div>
    </body>
</html>