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
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>Manual Test Case</title>
    <meta content="text/html; charset=UTF-8" http-equiv="content-type">

    <link rel="shortcut icon" type="image/x-icon" href="images/favicon.ico">
    <link rel="stylesheet" type="text/css" href="css/crb_style.css">

    <style media="screen" type="text/css">
        @import "css/demo_page.css";
        @import "css/demo_table.css";
        @import "css/demo_table_jui.css";
        @import "css/themes/base/jquery-ui.css";
        @import "css/themes/smoothness/jquery-ui-1.7.2.custom.css";
    </style>

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
            /*position: relative;*/
            /*float: left;*/
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
    </style>

    <script type="text/javascript" src="js/jquery-1.10.2.min.js"></script>
    <script type="text/javascript" src="js/ajax-loader.js"></script>
    <script type="text/javascript" src="js/jquery-ui-1.10.2.custom.min.js"></script>
    <script type="text/javascript" src="js/jquery.jeditable.mini.js"></script>
    <script type="text/javascript" src="js/jquery.dataTables.min.js"></script>
    <script type="text/javascript" src="js/jquery.dataTables.editable.js"></script>
</head>
<body>
<%@ include file="include/function.jsp" %>
<%@ include file="include/header.jsp" %>
<div class="fields" id="searchTestCase" style="margin-left: 25px">
    <div>
        <div class="field">
            <label for="test" style="width: 100px">Test</label><br/>
            <select id="test" name="test" style="width: 100px">
                <option value="All">-- ALL --</option>
            </select>
        </div>
        <div class="field">
            <label for="testCase" style="width: 60px">TestCase</label><br/>
            <input id="testCase" name="testCase" type="text" style="width: 60px"/>
        </div>
        <div class="field">
            <label for="project" style="width: 100px">Project</label><br/>
            <select id="project" name="project" style="width: 100px">
                <option value="All">-- ALL --</option>
            </select>
        </div>
        <div class="field">
            <label for="ticket" style="width: 100px">Ticket</label><br/>
            <select id="ticket" name="ticket" style="width: 100px">
                <option value="All">-- ALL --</option>
            </select>
        </div>
        <div class="field">
            <label for="bugID" style="width: 70px">Bug ID</label><br/>
            <select id="bugID" name="bugID" style="width: 70px">
                <option value="All">-- ALL --</option>
            </select>
        </div>
        <div class="field">
            <label for="origine" style="width: 90px">Origin</label><br/>
            <select id="origine" name="origine" style="width: 90px">
                <option value="All">-- ALL --</option>
            </select>
        </div>
        <div class="field">
            <label for="creator" style="width: 90px">Creator</label><br/>
            <select id="creator" name="creator" style="width: 90px">
                <option value="All">-- ALL --</option>
            </select>
        </div>
        <div class="field">
            <label for="system" style="width: 90px">System</label><br/>
            <select id="system" name="system" style="width: 90px">
                <option value="All">-- ALL --</option>
            </select>
        </div>
        <div class="field">
            <label for="application" style="width: 140px">Application</label><br/>
            <select id="application" name="application" style="width: 140px">
                <option value="All">-- ALL --</option>
            </select>
        </div>
        <div class="field">
            <label for="priority" style="width: 60px">Priority</label><br/>
            <select id="priority" name="priority" style="width: 60px">
                <option value="All">-- ALL --</option>
            </select>
        </div>
        <div class="field">
            <label for="status" style="width: 150px">Status</label><br/>
            <select id="status" name="status" style="width: 150px">
                <option value="All">-- ALL --</option>
            </select>
        </div>
    </div>
    <div>
        <div class="field">
            <label for="group" style="width: 150px">Group</label><br/>
            <select id="group" name="group" style="width: 150px">
                <option value="All">-- ALL --</option>
            </select>
        </div>
        <div class="field">
            <label for="activePROD" style="width: 90px">Active PROD</label><br/>
            <select id="activePROD" name="activePROD" style="width: 90px">
                <option value="All">-- ALL --</option>
            </select>
        </div>
        <div class="field">
            <label for="activeUAT" style="width: 90px">Active UAT</label><br/>
            <select id="activeUAT" name="activeUAT" style="width: 90px">
                <option value="All">-- ALL --</option>
            </select>
        </div>
        <div class="field">
            <label for="activeQA" style="width: 90px">Active QA</label><br/>
            <select id="activeQA" name="activeQA" style="width: 90px">
                <option value="All">-- ALL --</option>
            </select>
        </div>
        <div class="field">
            <label for="text" style="width: 500px">Text</label><br/>
            <input id="text" name="text" type="text" style="width: 500px"/>
        </div>
    </div>
    <div>
        <div class="field">
            <label for="tcActive" style="width: 150px">Active</label><br/>
            <select id="tcActive" name="tcActive" style="width: 150px">
                <option value="All">-- ALL --</option>
            </select>
        </div>
        <div class="field">
            <label for="fromBuild" style="width: 90px">From Sprint</label><br/>
            <select id="fromBuild" name="fromBuild" style="width: 90px">
                <option value="All">-- ALL --</option>
            </select>
        </div>
        <div class="field">
            <label for="fromRev" style="width: 90px">From Rev</label><br/>
            <select id="fromRev" name="fromRev" style="width: 90px">
                <option value="All">-- ALL --</option>
            </select>
        </div>
        <div class="field">
            <label for="toBuild" style="width: 90px">To Sprint</label><br/>
            <select id="toBuild" name="toBuild" style="width: 90px">
                <option value="All">-- ALL --</option>
            </select>
        </div>
        <div class="field">
            <label for="toRev" style="width: 90px">To Revision</label><br/>
            <select id="toRev" name="toRev" style="width: 90px">
                <option value="All">-- ALL --</option>
            </select>
        </div>
        <div class="field">
            <label for="targetBuild" style="width: 90px">Target Sprint</label><br/>
            <select id="targetBuild" name="targetBuild" style="width: 90px">
                <option value="All">-- ALL --</option>
            </select>
        </div>
        <div class="field">
            <label for="targetRev" style="width: 90px">Target Revision</label><br/>
            <select id="targetRev" name="targetRev" style="width: 90px">
                <option value="All">-- ALL --</option>
            </select>
        </div>
        <div class="field" style="float: right">
            <button type="button" class="button" onclick="loadTestCases()">Search</button>
        </div>
    </div>
</div>
<div id="manualTestCaseExecution" style="display: none; padding-top: 15px">
    <div class="fields" style="margin-left: 25px">
        <div>
            <h4>Execution Parameters</h4>

            <div class="field">
                <label for="executionEnv" style="width: 90px">Environment</label><br/>
                <select id="executionEnv" name="executionEnv" style="width: 90px">
                    <option value="">----</option>
                </select>
            </div>
            <div class="field">
                <label for="executionCountry" style="width: 90px">Country</label><br/>
                <select id="executionCountry" name="executionCountry" style="width: 90px">
                    <option value="">----</option>
                </select>
            </div>
            <div class="field">
                <label for="executionTag" style="width: 500px">Tag</label><br/>
                <input type="text" id="executionTag" name="executionTag" style="width: 500px"/>
            </div>
        </div>
    </div>
    <div id="divResultMessage" class="field" style="width: 600px; text-align: center">
        <span id="resultMessage" style="color: green; font-size: large; font-weight: 600;"></span>
    </div>

    <div style="width: 90%; padding-left: 25px; font: 90% sans-serif">
        <table id="testCaseTable" class="display">
            <thead>
            <tr>
                <th style="width: 150px">Test</th>
                <th style="width: 40px">Test Case</th>
                <th style="width: 200px">Value Expected</th>
                <th style="width: 450px">How To</th>
                <th style="width: 200px">Control Message</th>
                <th style="width: 50px">Result</th>
            </tr>
            </thead>
            <tbody>
            </tbody>
        </table>
    </div>
</div>
<script type="text/javascript">
    var oTable;
    var loader = new ajaxLoader("#searchTestCase");

    function getAjaxSource() {
        var url = "SearchTestCaseInformation";
        url += "?ScTest=" + $('#test').val();
        url += "&ScTestTest=" + $('#testCase').val();
        url += "&ScProject=" + $('#project').val();
        url += "&ScTicket=" + $('#ticket').val();
        url += "&ScBugID=" + $('#bugID').val();
        url += "&ScOrigin=" + $('#origine').val();
        url += "&ScCreator=" + $('#creator').val();
        url += "&ScSystem=" + $('#system').val();
        url += "&ScApplication=" + $('#application').val();
        url += "&ScPriority=" + $('#priority').val();
        url += "&ScStatus=" + $('#status').val();
        url += "&ScGroup=" + $('#group').val();
        url += "&ScPROD=" + $('#activePROD').val();
        url += "&ScUAT=" + $('#activeUAT').val();
        url += "&ScQA=" + $('#activeQA').val();
        url += "&ScText=" + $('#text').val();
        url += "&ScActive=" + $('#tcActive').val();
        url += "&ScFBuild=" + $('#fromBuild').val();
        url += "&ScFRev=" + $('#fromRev').val();
        url += "&ScTBuild=" + $('#toBuild').val();
        url += "&ScTRev=" + $('#toRev').val();
        url += "&ScTargetBuild=" + $('#targetBuild').val();
        url += "&ScTargetRev=" + $('#targetRev').val();
        return url;
    }

    function saveManualTest(res, row) {
        var data = oTable.fnGetNodes(row);
        if (data.cells[4].textContent == "Click to edit" || data.cells[4].textContent == "") {
            alert("Please edit Control Message before update Result!");
        } else if ($('#executionEnv').val() == "") {
            alert("Please choose Environment before update Result!");
        } else if ($('#executionCountry').val() == "") {
            alert("Please choose Country before update Result!");
        } else {
            var test = data.cells[0].textContent;
            var testCase = data.cells[1].textContent;
            var message = data.cells[4].textContent;
            var env = $('#executionEnv').val();
            var country = $('#executionCountry').val();
            var tag = $('#executionTag').val();

            var d = {test: test, testCase: testCase, env: env, country: country, controlStatus: res, controlMessage: message, tag: tag};

            $.post("SaveManualExecution", d,function () {
                $("#resultMessage").html("Manual Execution of Test Case <i>" + test + " - " + testCase + "</i> created");
                $("#divResultMessage").slideDown("slow");
                data.hidden = true;
                window.setTimeout(showMessage, 7000);
            }).fail(function (error) {
                        alert(error.responseText);
                    });
        }
    }

    function showMessage() {
        $("#divResultMessage").slideUp("slow");
    }

    function loadTestCases() {
        oTable = $('#testCaseTable').dataTable({
            "bJQueryUI": true,
            "bServerSide": false,
            "bDestroy": true,
            "bAutoWidth": false,
            "sAjaxSource": getAjaxSource(),
            "sServerMethod": "POST",
            "aoColumns": [
                {"mDataProp": "test", "sName": "test", sWidth: "150px"},
                {"mDataProp": "testCase", "sName": "ScTestCase", sWidth: "40px"},
                {"mDataProp": "description", "sName": "description", sWidth: "200px"},
                {"mDataProp": "howTo", "sName": "howTo", sWidth: "450px"},
                {"mDataProp": null, "sDefaultContent": '', "bSortable": false, sWidth: "200px"},
                {"mDataProp": null, "sDefaultContent": '', "bSortable": false, sWidth: "50px"}
            ],
            aoColumnDefs: [
                {
                    "aTargets": [5],
                    "fnRender": function (o, v) {
                        return "<p style='text-align: center'><input type='button' style='background-image: url(images/ok.png); width: 20px; height: 20px; border: 0 none; top: 0px' onclick='saveManualTest(\"OK\"," + o.iDataRow + ")'/></p><br/><br/>" +
                                "<p style='text-align: center'><input type='button' style='background-image: url(images/ko.png); width: 20px; height: 20px; border: 0 none; bottom: 0px' onclick='saveManualTest(\"KO\"," + o.iDataRow + ")'/></p>"
                    }
                }
            ]
        }).makeEditable({
                    sUpdateURL: function (value, settings) {
                        return(value);
                    },
                    oEditableSettings: { event: 'click' },
                    "aoColumns": [
                        null,
                        null,
                        null,
                        null,
                        {type: 'textarea', onblur: 'submit'},
                        null
                    ]
                });

        $('#manualTestCaseExecution').show();
    }

    $().ready(function () {
        $.post("GetDataForTestCaseSearch", function (testData) {
            $.each(testData, function (key, value) {
                $.each(value.data, function (k, v) {
                    if (v !== null && v !== "null" && v != "" && v != " ") {
                        $('#' + value.name).append(new Option(v, v));
                    }
                })
            });
            loader.remove();
        });

        var docData = [
            {label: "test", dTable: "test", dField: "Test", dLabel: "Test"},
            {label: "testCase", dTable: "testcase", dField: "TestCase", dLabel: "TestCase"},
            {label: "project", dTable: "testcase", dField: "project", dLabel: "Project"},
            {label: "ticket", dTable: "testcase", dField: "ticket", dLabel: "Ticket"},
            {label: "bugID", dTable: "testcase", dField: "BugID", dLabel: ""},
            {label: "origine", dTable: "testcase", dField: "Origine", dLabel: "Origin"},
            {label: "creator", dTable: "testcase", dField: "creator", dLabel: "Creator"},
            {label: "system", dTable: "application", dField: "system", dLabel: "System"},
            {label: "application", dTable: "testcase", dField: "Application", dLabel: "Application"},
            {label: "priority", dTable: "testcase", dField: "Priority", dLabel: ""},
            {label: "status", dTable: "testcase", dField: "Status", dLabel: ""},
            {label: "group", dTable: "testcase", dField: "Group", dLabel: ""},
            {label: "activePROD", dTable: "testcase", dField: "activePROD", dLabel: ""},
            {label: "activeUAT", dTable: "testcase", dField: "activeUAT", dLabel: ""},
            {label: "activeQA", dTable: "testcase", dField: "activeQA", dLabel: ""},
            {label: "text", dTable: "page_testcasesearch", dField: "text", dLabel: ""},
            {label: "tcActive", dTable: "testcase", dField: "TcActive", dLabel: "Active"},
            {label: "fromBuild", dTable: "testcase", dField: "FromBuild", dLabel: ""},
            {label: "fromRev", dTable: "testcase", dField: "FromRev", dLabel: ""},
            {label: "toBuild", dTable: "testcase", dField: "ToBuild", dLabel: ""},
            {label: "toRev", dTable: "testcase", dField: "ToRev", dLabel: ""},
            {label: "targetBuild", dTable: "testcase", dField: "TargetBuild", dLabel: ""},
            {label: "targetRev", dTable: "testcase", dField: "TargetRev", dLabel: ""}
        ];

        $.each(docData, function (key, value) {
            $.get("DocumentationField", {docTable: value.dTable, docField: value.dField, docLabel: value.dLabel}, function (doc) {
                $("label[for='" + value.label + "']").html(doc);
            });
        });

        $("#searchTestCase").keypress(function (e) {
            if (e.which == 13) {
                loadTestCases();
            }
        });
    });
</script>
</body>
</html>