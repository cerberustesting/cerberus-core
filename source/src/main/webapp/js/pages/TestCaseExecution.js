/*
 * Cerberus  Copyright (C) 2013  vertigo17
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This file is part of Cerberus.
 *
 * Cerberus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Cerberus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Cerberus.  If not, see <http://www.gnu.org/licenses/>.
 */
$.when($.getScript("js/pages/global/global.js")).then(function () {
    $(document).ready(function () {
        initPage();
    });
});

function initPage() {
    // The page take some parameters.
    var test = GetURLParameter("Test");
    var testCase = GetURLParameter("TestCase");

    displayPageLabel();

//    showLoader('#logViewerTable');
    //configure and create the dataTable
    var configurations = new TableConfigurationsServerSide("testCaseExecutionTable", "ReadTestCaseExecution", "contentTable", aoColumnsFunc(), [1, 'desc']);

    var table = createDataTable(configurations, undefined, undefined, "#testCaseExecution");
//    hideLoader('#logViewerTable');

    var api = table.api();
    // if test and testcase parameter are sent, we filter the logs on it.
    if (test !== null && testCase !== null) {
        var searchString = "'" + test + "'|'" + testCase + "'";
        api.search(searchString).draw();
    }
    
    var allowedColumns = new Array("test","testcase","application");
    applyFiltersOnMultipleColumns("testCaseExecutionTable", allowedColumns);
}

function displayPageLabel() {
    var doc = new Doc();

    displayHeaderLabel(doc);
    $("#pageTitle").html(doc.getDocLabel("page_testcaseexecution", "title"));
    $("#title").html(doc.getDocOnline("page_testcaseexecution", "title"));
    $("[name='editLogEventField']").html(doc.getDocOnline("page_testcaseexecution", "button_view"));
    $("[name='logeventidField']").html(doc.getDocOnline("logevent", "logeventid"));
    $("[name='timeField']").html(doc.getDocOnline("logevent", "time"));
    $("[name='pageField']").html(doc.getDocOnline("logevent", "page"));
    $("[name='actionField']").html(doc.getDocOnline("logevent", "action"));
    $("[name='loginField']").html(doc.getDocOnline("logevent", "login"));
    $("[name='logField']").html(doc.getDocOnline("logevent", "log"));
    $("[name='remoteipField']").html(doc.getDocOnline("logevent", "remoteip"));
    $("[name='localipField']").html(doc.getDocOnline("logevent", "localip"));
    displayFooter(doc);
    displayGlobalLabel(doc);
}

function aoColumnsFunc() {
    var doc = new Doc();

    var aoColumns = [
        {"data": null,
            "title": doc.getDocLabel("page_global", "columnAction"),
            "bSortable": false,
            "bSearchable": false,
            "mRender": function (data, type, obj) {
                var editEntry = '<button id="editEntry" onclick="editEntryClick(\'' + obj["LogEventID"] + '\');"\n\
                                class="editEntry btn btn-default btn-xs margin-right5" \n\
                                name="editEntry" title="' + doc.getDocLabel("page_logviewer", "button_view") + '" type="button">\n\
                                <span class="glyphicon glyphicon-eye-open"></span></button>';

                return '<div class="center btn-group">' + editEntry + '</div>';
            }
        },
        {
            "data": "id",
            "sName": "exe.id",
            "title": doc.getDocOnline("testcaseexecution", "id"),
            "sWidth": "100px",
            "sDefaultContent": ""
        },
        {
            "data": "test",
            "sName": "exe.test",
            "title": doc.getDocOnline("test", "Test"),
            "sWidth": "120px",
            "sDefaultContent": ""
        },
        {
            "data": "testcase",
            "sName": "exe.testcase",
            "title": doc.getDocOnline("testcase", "TestCase"),
            "sWidth": "70px",
            "sDefaultContent": ""
        },
        {
            "data": "build",
            "sName": "exe.build",
            "title": doc.getDocOnline("testcaseexecution", "Build"),
            "sWidth": "70px",
            "sDefaultContent": ""
        },
        {
            "data": "revision",
            "sName": "exe.revision",
            "title": doc.getDocOnline("testcaseexecution", "Revision"),
            "sWidth": "70px",
            "sDefaultContent": ""
        },
        {
            "data": "environment",
            "sName": "exe.environment",
            "title": doc.getDocOnline("testcaseexecution", "Environment"),
            "sWidth": "70px",
            "sDefaultContent": ""
        },
        {
            "data": "country",
            "sName": "exe.country",
            "title": doc.getDocOnline("testcaseexecution", "Country"),
            "sWidth": "70px",
            "sDefaultContent": ""
        },
        {
            "data": "application",
            "sName": "exe.application",
            "title": doc.getDocOnline("application", "Application"),
            "sWidth": "100px",
            "sDefaultContent": ""
        },
        {
            "data": "browser",
            "sName": "exe.browser",
            "title": doc.getDocOnline("testcaseexecution", "Browser"),
            "sWidth": "70px",
            "sDefaultContent": ""
        },
        {
            "data": "version",
            "sName": "exe.version",
            "title": doc.getDocOnline("testcaseexecution", "Version"),
            "sWidth": "70px",
            "sDefaultContent": ""
        },
        {
            "data": "platform",
            "sName": "exe.platform",
            "title": doc.getDocOnline("testcaseexecution", "Platform"),
            "sWidth": "70px",
            "sDefaultContent": ""
        },
        {
            "data": "browserFullVersion",
            "sName": "exe.browserfullversion",
            "title": doc.getDocOnline("testcaseexecution", "BrowserFullVersion"),
            "sWidth": "70px",
            "sDefaultContent": ""
        },
        {
            "data": "start",
            "sName": "exe.start",
            "title": doc.getDocOnline("testcaseexecution", "Start"),
            "sWidth": "70px",
            "sDefaultContent": ""
        },
        {
            "data": "end",
            "sName": "exe.end",
            "title": doc.getDocOnline("testcaseexecution", "End"),
            "sWidth": "70px",
            "sDefaultContent": ""
        },
        {
            "data": "controlStatus",
            "sName": "exe.controlstatus",
            "title": doc.getDocOnline("testcaseexecution", "ControlStatus"),
            "sWidth": "70px",
            "sDefaultContent": ""
        },
        {
            "data": "controlMessage",
            "sName": "exe.controlmessage",
            "title": doc.getDocOnline("testcaseexecution", "ControlMessage"),
            "sWidth": "70px",
            "sDefaultContent": ""
        },
        {
            "data": "ip",
            "sName": "exe.ip",
            "title": doc.getDocOnline("testcaseexecution", "Ip"),
            "sWidth": "70px",
            "sDefaultContent": ""
        },
        {
            "data": "url",
            "sName": "exe.url",
            "title": doc.getDocOnline("testcaseexecution", "Url"),
            "sWidth": "70px",
            "sDefaultContent": ""
        },
        {
            "data": "port",
            "sName": "exe.port",
            "title": doc.getDocOnline("testcaseexecution", "Port"),
            "sWidth": "70px",
            "sDefaultContent": ""
        },
        {
            "data": "tag",
            "sName": "exe.tag",
            "title": doc.getDocOnline("testcaseexecution", "Tag"),
            "sWidth": "70px",
            "sDefaultContent": ""
        },
        {
            "data": "finished",
            "sName": "exe.finished",
            "title": doc.getDocOnline("testcaseexecution", "Finished"),
            "sWidth": "70px",
            "sDefaultContent": ""
        },
        {
            "data": "verbose",
            "sName": "exe.verbose",
            "title": doc.getDocOnline("testcaseexecution", "Verbose"),
            "sWidth": "70px",
            "sDefaultContent": ""
        },
        {
            "data": "status",
            "sName": "exe.status",
            "title": doc.getDocOnline("testcaseexecution", "Status"),
            "sWidth": "70px",
            "sDefaultContent": ""
        },
        {
            "data": "crbVersion",
            "sName": "exe.crbVersion",
            "title": doc.getDocOnline("testcaseexecution", "CrbVersion"),
            "sWidth": "70px",
            "sDefaultContent": ""
        },
        {
            "data": "executor",
            "sName": "exe.executor",
            "title": doc.getDocOnline("testcaseexecution", "Executor"),
            "sWidth": "70px",
            "sDefaultContent": ""
        },
        {
            "data": "screenSize",
            "sName": "exe.screensize",
            "title": doc.getDocOnline("testcaseexecution", "Screensize"),
            "sWidth": "70px",
            "sDefaultContent": ""
        }
    ];
    return aoColumns;
}

