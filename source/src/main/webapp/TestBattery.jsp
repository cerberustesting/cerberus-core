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
        <title>Test Battery</title>
        <%@ include file="include/dependenciesInclusions_old.html" %>
        <script type="text/javascript" src="dependencies/zz_OldDependencies/js/jquery.form.js"></script>
        
        <style>
            #contents {
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
            
            #testbatteries {
                margin: 10px 0;
            }
        </style>
    </head>
    <body>
        <%@ include file="include/function.jsp"%>
        <%@ include file="include/header.jsp"%>
        <div id="testbatteries">
        List of Test Battery :
        <table  class="display" id="listOfTestBatteries" name="listOfCampaigns">
            <thead>
                <tr>
                    <th>ID</th>
                    <th>Test Battery</th>
                    <th>Description</th>
                </tr>
            </thead>
        </table>
        </div>
        <div id="contents">
            <table  class="display" id="listOfContents" name="listOfParameters">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Test Battery</th>
                        <th>Test</th>
                        <th>TestCase</th>
                        <th>Description</th>
                    </tr>
                </thead>
            </table>
        </div>
        <script>
            var oTable, oTableContent;
            function refreshTestBatteries() {
                $('#contents').hide();
                
                oTable = $('#listOfTestBatteries').dataTable({
                    "aaSorting": [[0, "asc"]],
                    "bServerSide": false,
                    "sAjaxSource": "GetTestBattery?action=findAllTestBattery",
                    "sAjaxDataProp": "TestBatteries",
                    "bJQueryUI": true,
                    "bProcessing": true,
                    "bPaginate": true,
                    "bAutoWidth": false,
                    "sPaginationType": "full_numbers",
                    "bSearchable": true,
                    "aTargets": [0],
                    "iDisplayLength": 10,
                    "aoColumns": [
                        {"sName": "ID", "bVisible": false},
                        {"sName": "Test Battery", "sWidth": "30%"},
                        {"sName": "Description", "sWidth": "70%"}
                    ]
                }).makeEditable({
                    sAddURL: "AddTestBattery",
                    sAddNewRowFormId: "formAddNewTestBattery",
                    sAddNewRowButtonId: "btnAddNewTestBattery",
                    sAddNewRowOkButtonId: "btnAddNewTestBatteryOk",
                    sAddNewRowCancelButtonId: "btnAddNewTestBatteryCancel",
                    sDeleteRowButtonId: "btnDeleteTestBattery",
                    sAddDeleteToolbarSelector: "#listOfTestBatteries_length",
                    sAddHttpMethod: "POST",
                    oAddNewRowButtonOptions: {
                        label: "<b>Create TestBattery</b>",
                        background: "#AAAAAA",
                        icons: {primary: 'ui-icon-plus'}
                    },
                    oAddNewRowFormOptions: {
                        title: 'Add TestBattery Entry',
                        show: "blind",
                        hide: "blind",
                        width: "700px"
                    },
                    sDeleteHttpMethod: "POST",
                    sDeleteURL: "DeleteTestBattery",
                    oDeleteRowButtonOptions: {
                        label: "Remove",
                        icons: {primary:'ui-icon-trash'}
                    },
                    sUpdateURL: "UpdateTestBattery",
                    fnOnEdited: function(){
                        $(".dataTables_processing").css('visibility', 'hidden');
                    },
                    "aoColumns": [
                        null,
                        {
                            indicator   : 'Saving...',
                            tooltip     : 'Double Click to edit...',
                            style       : 'display: inline',
                            onblur      : 'submit'
                        }
                    ]
                });
                
                /* Add a click handler to the rows - this could be used as a callback */
                $('#listOfTestBatteries').find('tbody').click(function(event) {
                        refreshContents($(event.target.parentNode).attr("id"));
                });
            }
            
            function refreshContents(id) {
                $('#contents').hide();

                $('#TestBatteryIdForContent').attr('value',id);
                
                // This example is fairly pointless in reality, but shows how fnDestroy can be used
                if(oTableContent) {
                    var list = $('#listOfContents');
                    list.find('tbody').empty();
                    list.dataTable().fnDestroy();
                }

                $.getJSON("GetTestBattery","action=findAllTestBatteryContent&testBattery="+id,function(data){
                    oTableContent = $('#listOfContents').dataTable({
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
                            {"sName": "Test Battery", "bVisible": false},
                            {"sName": "Test"},
                            {"sName": "TestCase"},
                            {"sName": "Description"}
                        ],
                        "aaData" : data.TestBatteryContents
                    }).makeEditable({
                        sAddURL: "AddTestBatteryContent",
                        sAddNewRowFormId: "formAddNewContent",
                        sAddNewRowButtonId: "btnAddNewContent",
                        sAddNewRowOkButtonId: "btnAddNewContentOk",
                        sAddNewRowCancelButtonId: "btnAddNewContentCancel",
                        sAddDeleteToolbarSelector: "#listOfContents_length",
                        sDeleteRowButtonId: "btnDeleteContent",

                        sAddHttpMethod: "POST",
                        oAddNewRowButtonOptions: {
                            label: "<b>Add Content</b>",
                            background: "#AAAAAA",
                            icons: {primary: 'ui-icon-plus'}
                        },
                        oAddNewRowFormOptions: {
                            title: 'Add Content',
                            show: "blind",
                            hide: "blind",
                            width: "900px",
                            close: function() {
                                refreshContents(id);
                            }
                        },
                        sDeleteHttpMethod: "POST",
                        sDeleteURL: "DeleteTestBatteryContent",
                        oDeleteRowButtonOptions: {
                            label: "Remove",
                            icons: {primary:'ui-icon-trash'}
                        },
                        sUpdateURL: "UpdateTestBatteryContent",
                        fnOnEdited: function(){
                            $(".dataTables_processing").css('visibility', 'hidden');
                        },
                        "aoColumns": [
                            null,
                            null
                        ]
                    });
                    $('#contents').show();
                });
            }

            $(document).ready(function(){
                refreshTestBatteries();

                var system = $('#MySystem').val();
                $('#testcasesearchdiv').load("TestBatteryTestCaseSearch.jsp?system="+system);
            });
        </script>
            <form id="formAddNewTestBattery" class="formForDataTable" action="#" title="Add TestBattery Entry" style="width:600px" method="post">
                <input type="hidden" value="-1" id="ID" name="ID" class="ncdetailstext" rel="0" >
                <label for="TestBattery" style="font-weight:bold">TestBattery</label>
                <input id="TestBattery" name="TestBattery" class="ncdetailstext" rel="1" >
                <br><br>
                <label for="Description" style="font-weight:bold">Description</label>
                <input id="Description" name="Description" class="ncdetailstext" rel="2" >
            </form>
            <form id="formAddNewContent" class="formForDataTable" action="TestBatteryTestCaseResult.jsp" title="Add Content Entry" style="width:900px; height:600px" method="post">
                <input type="hidden" value="-1" id="IDContent" name="ID" class="ncdetailstext" rel="0" >
                <input type="hidden" value="-1" id="TestBatteryIdForContent" name="TestBattery" class="ncdetailstext" rel="1">
                <input type="hidden" id="Test" name="Test" class="ncdetailstext" rel="2" >
                <input type="hidden" id="TestCase" name="TestCase" class="ncdetailstext" rel="3" >
                <input type="hidden" id="Description" name="Descrition" class="ncdetailstext" rel="4" >
                <div id="testcasesearchdiv"></div>
            </form>
     </body>
</html>
