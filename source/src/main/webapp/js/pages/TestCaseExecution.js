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
    loadTable();
    
}

function loadTable(){
    //clear the old report content before reloading it
    $("#testCaseExecution").empty();
    $("#testCaseExecution").html('<table id="testCaseExecutionTable" class="table table-bordered table-hover display" name="testCaseExecutionTable">\n\
                                            </table><div class="marginBottom20"></div>');

    var contentUrl = "ReadTestCaseExecution";

    //configure and create the dataTable
    var lengthMenu = [10, 25, 50, 100, 500, 1000];
    var configurations = new TableConfigurationsServerSide("testCaseExecutionTable", contentUrl, "contentTable", aoColumnsFunc(), [3, 'desc'], lengthMenu);
    configurations.aaSorting = [3, 'desc'];
    
    var filtrableColumns = new Array("test","testcase","application","country","environment");
    
    var table = createDataTableWithPermissions(configurations, undefined, "#testCaseExecution", filtrableColumns);
    
   
}

function afterTableLoad(){
    $("#testCaseExecutionTable_paginate").hide();
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
        {
            "data": null,
            "bSortable": false,
            "bSearchable": false,
            "title": doc.getDocOnline("page_global", "columnAction"),
            "sDefaultContent": "",
            "sWidth": "100px",
            "mRender": function (data, type, obj) {
                var buttons = "";

                var editScript = '<a id="testCaseBetaLink" class="btn btn-default btn-xs marginRight5"\n\
                                    data-toggle="tooltip" title="' + doc.getDocLabel("page_executiondetail", "edittc") + '" href="./TestCaseScript.jsp?test=' + encodeURIComponent(obj["test"]) + '&testcase=' + encodeURIComponent(obj["testcase"]) + '">\n\
                                    <span class="glyphicon glyphicon-new-window"></span>\n\
                                    </a>';
                var runTest = '<a id="runTest" class="btn btn-default btn-xs marginRight5"\n\
                                    data-toggle="tooltip" title="' + doc.getDocLabel("page_executiondetail", "runtc") + '" href="./RunTests1.jsp?test=' + encodeURIComponent(obj["test"]) + '&testcase=' + encodeURIComponent(obj["testcase"]) + '">\n\
                                    <span class="glyphicon glyphicon-play"></span>\n\
                                    </a>';
                var lastExec = '<a id="lastExec" class="btn btn-default btn-xs marginRight5"\n\
                                    data-toggle="tooltip" title="' + doc.getDocLabel("page_executiondetail", "lastexecution") + '" href="./RunTests1.jsp?test=' + encodeURIComponent(obj["test"]) + '&testcase=' + encodeURIComponent(obj["testcase"]) + '">\n\
                                    <span class="glyphicon glyphicon-backward"></span>\n\
                                    </a>';
                var tag = '<a id="tagExec'+(obj["id"])+'" class="btn btn-default btn-xs marginRight5"\n\
                                    data-toggle="tooltip" title="' + doc.getDocLabel("page_executiondetail", "see_execution_tag") + '" href="./ReportingExecutionByTag.jsp?Tag=' + obj["tag"] + '">\n\
                                    <span class="glyphicon glyphicon-tag"></span>\n\
                                    </a>';

                
                buttons += editScript;
                buttons += lastExec;
                buttons += tag;
                buttons += runTest;



                return '<div class="center btn-group width250">' + buttons + '</div>';
            }
        },
        {
            "data": "controlStatus",
            "sName": "exe.controlStatus",
            "title": doc.getDocOnline("page_executiondetail", "controlstatus"),
            "sWidth": "100px",
            "sDefaultContent": "",
            "sClass": "center",
            "mRender": function (data, type, obj) {
                if (obj !== "") {
                    var executionLink = "./ExecutionDetail2.jsp?executionId=" + obj.id;
                    var glyphClass = getRowClass(obj.controlStatus);
                    var tooltip = generateTooltip(obj);
                    var cell = '<a href="' + executionLink + '"><div class="progress-bar status' + obj.controlStatus + '" \n\
                                role="progressbar" aria-valuenow="100" aria-valuemin="0" aria-valuemax="100" style="width: 100%;cursor: pointer; height: 20px;" \n\
                                data-toggle="tooltip" data-html="true" title="' + tooltip + '"\n\
                                <span class="' + glyphClass.glyph + ' marginRight5" style="margin-top:0;"></span>\n\
                                 <span>' + obj.controlStatus + '<span></div></a>';
                    return cell;
                } else {
                    return obj;
                }
            }
        },
        {
            "data": "id",
            "sName": "exe.id",
            "bSearchable": false,
            "title": doc.getDocOnline("page_executiondetail", "id"),
            "sWidth": "120px",
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
            "data": "country",
            "sName": "exe.country",
            "title": doc.getDocOnline("page_executiondetail", "country"),
            "sWidth": "70px",
            "sDefaultContent": ""
        },
        {
            "data": "environment",
            "sName": "exe.environment",
            "title": doc.getDocOnline("page_executiondetail", "environment"),
            "sWidth": "70px",
            "sDefaultContent": ""
        },
        {
            "data": "build",
            "sName": "exe.build",
            "title": doc.getDocOnline("page_executiondetail", "build"),
            "sWidth": "70px",
            "sDefaultContent": ""
        },
        {
            "data": "revision",
            "sName": "exe.revision",
            "title": doc.getDocOnline("page_executiondetail", "revision"),
            "sWidth": "70px",
            "sDefaultContent": ""
        },
        {
            "data": "application",
            "sName": "exe.application",
            "title": doc.getDocOnline("page_executiondetail", "application"),
            "sWidth": "100px",
            "sDefaultContent": ""
        },
        {
            "data": "browser",
            "sName": "exe.browser",
            "title": doc.getDocOnline("page_executiondetail", "browser"),
            "sWidth": "70px",
            "sDefaultContent": ""
        },
        {
            "data": "version",
            "sName": "exe.version",
            "title": doc.getDocOnline("page_executiondetail", "version"),
            "sWidth": "70px",
            "sDefaultContent": ""
        },
        {
            "data": "platform",
            "sName": "exe.platform",
            "title": doc.getDocOnline("page_executiondetail", "platform"),
            "sWidth": "70px",
            "sDefaultContent": ""
        },
        {
            "data": "browserFullVersion",
            "sName": "exe.browserfullversion",
            "title": doc.getDocOnline("page_executiondetail", "browserfull"),
            "sWidth": "70px",
            "sDefaultContent": ""
        },
        {
            "data": "start",
            "sName": "exe.start",
            "bSearchable": false,
            "title": doc.getDocOnline("page_executiondetail", "start"),
            "sWidth": "70px",
            "sDefaultContent": ""
        },
        {
            "data": "end",
            "sName": "exe.end",
            "bSearchable": false,
            "title": doc.getDocOnline("page_executiondetail", "end"),
            "sWidth": "70px",
            "sDefaultContent": ""
        },
        {
            "data": "controlMessage",
            "sName": "exe.controlmessage",
            "bSearchable": false,
            "title": doc.getDocOnline("page_executiondetail", "controlmessage"),
            "sWidth": "70px",
            "sDefaultContent": ""
        },
        {
            "data": "ip",
            "sName": "exe.ip",
            "title": doc.getDocOnline("page_executiondetail", "ip"),
            "sWidth": "70px",
            "sDefaultContent": ""
        },
        {
            "data": "url",
            "sName": "exe.url",
            "title": doc.getDocOnline("page_executiondetail", "url"),
            "sWidth": "70px",
            "sDefaultContent": ""
        },
        {
            "data": "port",
            "sName": "exe.port",
            "title": doc.getDocOnline("page_executiondetail", "port"),
            "sWidth": "70px",
            "sDefaultContent": ""
        },
        {
            "data": "tag",
            "sName": "exe.tag",
            "title": doc.getDocOnline("page_executiondetail", "tag"),
            "sWidth": "70px",
            "sDefaultContent": "",
            "mRender": function (data, type, obj) {
                if (data !== "") {
                        return data;
                    } else {
                        $('#tagExec' + (obj["id"])).attr("disabled", "disabled");
                        return data;
                    }
             }
        },
        {
            "data": "verbose",
            "sName": "exe.verbose",
            "title": doc.getDocOnline("page_executiondetail", "verbose"),
            "sWidth": "70px",
            "sDefaultContent": ""
        },
        {
            "data": "status",
            "sName": "exe.status",
            "title": doc.getDocOnline("page_executiondetail", "status"),
            "sWidth": "70px",
            "sDefaultContent": ""
        },
        {
            "data": "crbVersion",
            "sName": "exe.crbVersion",
            "bSearchable": false,
            "title": doc.getDocOnline("page_executiondetail", "cerberusversion"),
            "sWidth": "70px",
            "sDefaultContent": ""
        },
        {
            "data": "executor",
            "sName": "exe.executor",
            "title": doc.getDocOnline("page_executiondetail", "executor"),
            "sWidth": "70px",
            "sDefaultContent": ""
        },
        {
            "data": "screenSize",
            "sName": "exe.screensize",
            "bSearchable": false,
            "title": doc.getDocOnline("page_executiondetail", "screensize"),
            "sWidth": "70px",
            "sDefaultContent": ""
        }
    ];
    return aoColumns;
}


/**
 * Duplicated from reportbytag >> TO CLEAN
 */
function getRowClass(status) {
    var rowClass = [];

    rowClass["panel"] = "panel" + status;
    if (status === "OK") {
        rowClass["glyph"] = "glyphicon glyphicon-ok";
    } else if (status === "KO") {
        rowClass["glyph"] = "glyphicon glyphicon-remove";
    } else if (status === "FA") {
        rowClass["glyph"] = "fa fa-bug";
    } else if (status === "CA") {
        rowClass["glyph"] = "fa fa-life-ring";
    } else if (status === "PE") {
        rowClass["glyph"] = "fa fa-hourglass-half";
    } else if (status === "NE") {
        rowClass["glyph"] = "fa fa-clock-o";
    } else if (status === "NA") {
        rowClass["glyph"] = "fa fa-question";
    } else {
        rowClass["glyph"] = "";
    }
    return rowClass;
}

/**
 * DUPLICATED FROM REPORTBYTAG >>> TO REMOVE
 * @param {type} data
 * @returns {String}
 */
function generateTooltip(data) {
    var htmlRes;

    htmlRes = '<div><span class=\'bold\'>Execution ID :</span> ' + data.id + '</div>' +
            '<div><span class=\'bold\'>Country : </span>' + data.country + '</div>' +
            '<div><span class=\'bold\'>Environment : </span>' + data.environment + '</div>' +
            '<div><span class=\'bold\'>Browser : </span>' + data.browser + '</div>' +
            '<div><span class=\'bold\'>Start : </span>' + new Date(data.start) + '</div>' +
            '<div><span class=\'bold\'>End : </span>' + new Date(data.end) + '</div>' +
            '<div>' + data.controlMessage + '</div>';

    return htmlRes;
}
