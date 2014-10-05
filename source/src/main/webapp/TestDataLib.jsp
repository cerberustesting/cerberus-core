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
        <link rel="stylesheet" type="text/css" href="css/jquery.dataTables.css">
        <link rel="stylesheet" type="text/css" href="css/jquery-ui.css">
        <link rel="stylesheet" type="text/css" href="css/dataTables_jui.css">
        <link type="text/css" rel="stylesheet" href="css/jquery.multiselect.css">

        <script type="text/javascript" src="js/jquery-1.9.1.min.js"></script>
        <script type="text/javascript" src="js/jquery-ui-1.10.2.js"></script>
        <script type="text/javascript" src="js/jquery.dataTables.js"></script>
        <script type="text/javascript" src="js/jquery.jeditable.mini.js"></script>
        <script type="text/javascript" src="js/jquery.dataTables.editable.js"></script>
        <script type="text/javascript" src="js/jquery.validate.min.js"></script>
        <script type="text/javascript" src="js/jquery.multiselect.js" charset="utf-8"></script>
        <script type="text/javascript" src="js/jquery.form.js"></script>

        <style>
            #datas {
                margin: 3px;
                vertical-align: top;
                display: inline-block;
                *zoom: 1;
                *display: inline;
                width: 100%;
            }

            .formForDataTable {
                display: none;
            }

            #testdatalib {
                margin: 10px 0;
            }
        </style>
    </head>
    <body>
        <%@ include file="include/function.jsp"%>
        <%@ include file="include/header.jsp"%>
        <div id="testdatalib">
            List of Test Data Library :
            <table  class="display" id="listOfTestDataLib" name="listOfCampaigns">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Test Data Lib</th>
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
        </div>
        <div id="datas">
            <table  class="display" id="listOfDatas" name="listOfParameters">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Sub Data</th>
                        <th>Value</th>
                        <th>Column</th>
                        <th>Parsing Answer</th>
                        <th>Description</th>
                    </tr>
                </thead>
            </table>
        </div>
        <script>
            var oTable, oTableContent;
            function refreshTestDataLib() {
                $('#datas').hide();

                oTable = $('#listOfTestDataLib').dataTable({
                    "aaSorting": [[0, "asc"]],
                    "bServerSide": false,
                    "sAjaxSource": "GetTestDataLib",
                    "sAjaxDataProp": "TestDataLib",
                    "bJQueryUI": true,
                    "bProcessing": true,
                    "bPaginate": true,
                    "bAutoWidth": false,
                    "sPaginationType": "full_numbers",
                    "bSearchable": true,
                    "aTargets": [0],
                    "iDisplayLength": 25,
                    "aoColumns": [
                        {"sName": "ID", "bVisible": false},
                        {"sName": "Data Lib", "sWidth": "10%"},
                        {"sName": "System", "sWidth": "10%"},
                        {"sName": "Environment", "sWidth": "10%"},
                        {"sName": "Country", "sWidth": "10%"},
                        {"sName": "Group", "sWidth": "10%"},
                        {"sName": "Type", "sWidth": "10%"},
                        {"sName": "Database", "sWidth": "10%"},
                        {"sName": "Script", "sWidth": "10%"},
                        {"sName": "Service Path", "sWidth": "5%"},
                        {"sName": "Method", "sWidth": "5%"},
                        {"sName": "Envelope", "sWidth": "5%"},
                        {"sName": "Description", "sWidth": "5%"}
                    ]
                }).makeEditable({
                    sAddURL: "AddTestDataLib",
                    sAddNewRowFormId: "formAddNewTestDataLib",
                    sAddNewRowButtonId: "btnAddNewTestDataLib",
                    sAddNewRowOkButtonId: "btnAddNewTestDataLibOk",
                    sAddNewRowCancelButtonId: "btnAddNewTestDataLibCancel",
                    sDeleteRowButtonId: "btnDeleteTestDataLib",
                    sAddDeleteToolbarSelector: "#listOfTestDataLib_length",
                    sAddHttpMethod: "POST",
                    oAddNewRowButtonOptions: {
                        label: "<b>Create TestDataLib</b>",
                        background: "#AAAAAA",
                        icons: {primary: 'ui-icon-plus'}
                    },
                    oAddNewRowFormOptions: {
                        title: 'Add TestDataLib Entry',
                        show: "blind",
                        hide: "blind",
                        width: "700px"
                    },
                    sDeleteHttpMethod: "POST",
                    sDeleteURL: "DeleteTestDataLib",
                    oDeleteRowButtonOptions: {
                        label: "Remove",
                        icons: {primary: 'ui-icon-trash'}
                    },
                    sUpdateURL: "UpdateTestDataLib",
                    fnOnEdited: function() {
                        $(".dataTables_processing").css('visibility', 'hidden');
                    },
                    "aoColumns": [
                        null,
                        {
                            indicator: 'Saving...',
                            tooltip: 'Double Click to edit...',
                            style: 'display: inline',
                            onblur: 'submit'
                        },
                        {
                            indicator: 'Saving...',
                            tooltip: 'Double Click to edit...',
                            style: 'display: inline',
                            onblur: 'submit'
                        },
                        {
                            indicator: 'Saving...',
                            tooltip: 'Double Click to edit...',
                            style: 'display: inline',
                            onblur: 'submit'
                        },
                        {
                            indicator: 'Saving...',
                            tooltip: 'Double Click to edit...',
                            style: 'display: inline',
                            onblur: 'submit'
                        },
                        {
                            indicator: 'Saving...',
                            tooltip: 'Double Click to edit...',
                            style: 'display: inline',
                            onblur: 'submit'
                        },
                        {
                            indicator: 'Saving...',
                            tooltip: 'Double Click to edit...',
                            style: 'display: inline',
                            onblur: 'submit'
                        },
                        {
                            indicator: 'Saving...',
                            tooltip: 'Double Click to edit...',
                            style: 'display: inline',
                            onblur: 'submit'
                        },
                        {
                            indicator: 'Saving...',
                            tooltip: 'Double Click to edit...',
                            style: 'display: inline',
                            onblur: 'submit'
                        },
                        {
                            indicator: 'Saving...',
                            tooltip: 'Double Click to edit...',
                            style: 'display: inline',
                            onblur: 'submit'
                        },
                        {
                            indicator: 'Saving...',
                            tooltip: 'Double Click to edit...',
                            style: 'display: inline',
                            onblur: 'submit'
                        },
                        {
                            indicator: 'Saving...',
                            tooltip: 'Double Click to edit...',
                            style: 'display: inline',
                            onblur: 'submit'
                        },
                        {
                            indicator: 'Saving...',
                            tooltip: 'Double Click to edit...',
                            style: 'display: inline',
                            onblur: 'submit'
                        }
                    ]
                });

                /* Add a click handler to the rows - this could be used as a callback */
                $('#listOfTestDataLib').find('tbody').click(function(event) {
                    refreshDatas($(event.target.parentNode).attr("id"));
                });
            }

            function refreshDatas(id) {
                $('#datas').hide();

                $('#TestDataLibIdForContent').attr('value', id);

                // This example is fairly pointless in reality, but shows how fnDestroy can be used
                if (oTableContent) {
                    var list = $('#listOfDatas');
                    list.find('tbody').empty();
                    list.dataTable().fnDestroy();
                }

                $.getJSON("GetTestDataLib", "action=findAllTestDataLibContent&testDataLib=" + id, function(data) {
                    oTableContent = $('#listOfDatas').dataTable({
                        "aaSorting": [[0, "asc"]],
                        "bServerSide": false,
                        "bJQueryUI": true,
                        "bProcessing": true,
                        "bDestroy": true,
                        "bPaginate": true,
                        "bAutoWidth": false,
                        "sPaginationType": "full_numbers",
                        "bSearchable": true,
                        "aTargets": [0],
                        "iDisplayLength": 25,
                        "aoColumns": [
                            {"sName": "ID", "bVisible": false},
                            {"sName": "Sub Data"},
                            {"sName": "Value"},
                            {"sName": "Column"},
                            {"sName": "ParsingAnswer"},
                            {"sName": "Description"}
                        ],
                        "aaData": data.TestDataLibDatas
                    }).makeEditable({
                        sAddURL: "AddTestDataLibData",
                        sAddNewRowFormId: "formAddNewData",
                        sAddNewRowButtonId: "btnAddNewData",
                        sAddNewRowOkButtonId: "btnAddNewDataOk",
                        sAddNewRowCancelButtonId: "btnAddNewDataCancel",
                        sAddDeleteToolbarSelector: "#listOfData_length",
                        sDeleteRowButtonId: "btnDeleteData",
                        sAddHttpMethod: "POST",
                        oAddNewRowButtonOptions: {
                            label: "<b>Add Data</b>",
                            background: "#AAAAAA",
                            icons: {primary: 'ui-icon-plus'}
                        },
                        oAddNewRowFormOptions: {
                            title: 'Add Data',
                            show: "blind",
                            hide: "blind",
                            width: "900px",
                            close: function() {
                                refreshDatas(id);
                            }
                        },
                        sDeleteHttpMethod: "POST",
                        sDeleteURL: "DeleteTestDataLibData",
                        oDeleteRowButtonOptions: {
                            label: "Remove",
                            icons: {primary: 'ui-icon-trash'}
                        },
                        sUpdateURL: "UpdateTestDataLibData",
                        fnOnEdited: function() {
                            $(".dataTables_processing").css('visibility', 'hidden');
                        },
                        "aoColumns": [
                            null,
                            null,
                            {
                                indicator: 'Saving...',
                                tooltip: 'Double Click to edit...',
                                style: 'display: inline',
                                onblur: 'submit'
                            },
                            {
                                indicator: 'Saving...',
                                tooltip: 'Double Click to edit...',
                                style: 'display: inline',
                                onblur: 'submit'
                            },
                            {
                                indicator: 'Saving...',
                                tooltip: 'Double Click to edit...',
                                style: 'display: inline',
                                onblur: 'submit'
                            },
                            {
                                indicator: 'Saving...',
                                tooltip: 'Double Click to edit...',
                                style: 'display: inline',
                                onblur: 'submit'
                            }
                        ]
                    });
                    $('#datas').show();
                });
            }

            $(document).ready(function() {
                refreshTestDataLib();

                var system = $('#MySystem').val();
                $('#testcasesearchdiv').load("TestBatteryTestCaseSearch.jsp?system=" + system);
            });
        </script>
        <form id="formAddNewTestDataLib" class="formForDataTable" action="#" title="Add TestDataLib Entry" style="width:600px" method="post">
            <input type="hidden" value="-1" id="ID" name="ID" class="ncdetailstext" rel="0" >
            <label for="TestDataLib" style="font-weight:bold">TestDataLib</label>
            <input id="TestDataLib" name="TestDataLib" class="ncdetailstext" rel="1" >
            <br><br>
            <label for="System" style="font-weight:bold">System</label>
            <input id="System" name="System" class="ncdetailstext" rel="2" >
            <br><br>
            <label for="Environment" style="font-weight:bold">Environment</label>
            <input id="Environment" name="Environment" class="ncdetailstext" rel="3" >
            <br><br>
            <label for="Country" style="font-weight:bold">Country</label>
            <input id="Country" name="Country" class="ncdetailstext" rel="4" >
            <br><br>
            <label for="Group" style="font-weight:bold">Group</label>
            <input id="Group" name="Group" class="ncdetailstext" rel="5" >
            <br><br>
            <label for="Type" style="font-weight:bold">Type</label>
            <input id="Type" name="Type" class="ncdetailstext" rel="6" >
            <br><br>
            <label for="Database" style="font-weight:bold">Database</label>
            <input id="Database" name="Description" class="ncdetailstext" rel="7" >
            <br><br>
            <label for="Script" style="font-weight:bold">Script</label>
            <input id="Script" name="Script" class="ncdetailstext" rel="8" >
            <br><br>
            <label for="Service Path" style="font-weight:bold">Service Path</label>
            <input id="Service Path" name="Service Path" class="ncdetailstext" rel="9" >
            <br><br>
            <label for="Method" style="font-weight:bold">Method</label>
            <input id="Method" name="Method" class="ncdetailstext" rel="10" >
            <br><br>
            <label for="Envelope" style="font-weight:bold">Envelope</label>
            <input id="Envelope" name="Envelope" class="ncdetailstext" rel="11" >
            <br><br>
            <label for="Description" style="font-weight:bold">Description</label>
            <input id="Description" name="Description" class="ncdetailstext" rel="12" >
        </form>
        <form id="formAddNewData" class="formForDataTable" action="TestBatteryTestCaseResult.jsp" title="Add Content Entry" style="width:900px; height:600px" method="post">
            <input type="hidden" value="-1" id="IDContent" name="ID" class="ncdetailstext" rel="0" >
            <input type="hidden" value="-1" id="TestDataLibIdForData" name="TestDataLib" class="ncdetailstext" rel="1">
            <input type="hidden" id="Test" name="Test" class="ncdetailstext" rel="2" >
            <input type="hidden" id="TestCase" name="TestCase" class="ncdetailstext" rel="3" >
            <input type="hidden" id="Description" name="Descrition" class="ncdetailstext" rel="4" >
            <div id="testcasesearchdiv"></div>
        </form>
    </body>
</html>
