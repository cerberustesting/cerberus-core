/*
 * Cerberus Copyright (C) 2013 - 2025 cerberustesting
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
$.when($.getScript("js/global/global.js")).then(function () {
    $(document).ready(function () {
        initPage();
        $('[data-toggle="popover"]').popover({
            'placement': 'auto',
            'container': 'body'}
        );
    });
});

function initPage() {
    // The page take some parameters.
    var test = GetURLParameter("Test");
    var testCase = GetURLParameter("TestCase");

    displayPageLabel();

//    showLoader('#logViewerTable');
    //configure and create the dataTable
    var configurations = new TableConfigurationsServerSide("logViewerTable", "ReadLogEvent", "contentTable", aoColumnsFunc(), [1, 'desc']);

    var table = createDataTableWithPermissions(configurations, renderOptionsForLogViewer, "#logViewer");
//    hideLoader('#logViewerTable');

    //var api = table.api();
    // if test and testcase parameter are sent, we filter the logs on it.
    if (test !== null && testCase !== null) {
        var searchString = "'" + test + "'|'" + testCase + "'";
        table.search(searchString).draw();
    }
}

function renderOptionsForLogViewer() {
    $("#logViewerTable_paginate").parent().addClass("col-md-12").addClass("paddingRight0");
}

function displayPageLabel() {
    var doc = new Doc();

    displayHeaderLabel(doc);
    $("#pageTitle").html(doc.getDocLabel("page_logviewer", "title"));
    $("#title").html(doc.getDocOnline("page_logviewer", "title"));
    $("[name='editLogEventField']").html(doc.getDocOnline("page_logviewer", "button_view"));
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

function editEntryClick(id) {
    clearResponseMessageMainPage();
    var jqxhr = $.getJSON("ReadLogEvent", "logeventid=" + id);
    $.when(jqxhr).then(function (data) {
        var obj = data["contentTable"];

        var formEdit = $('#editEntryModal');

        formEdit.find("#logeventid").prop("value", id);
        formEdit.find("#time").prop("value", getDate(obj["time"]));
        formEdit.find("#remoteip").prop("value", obj["remoteIP"]);
        formEdit.find("#localip").prop("value", obj["localIP"]);
        formEdit.find("#page").prop("value", obj["page"]);
        formEdit.find("#action").prop("value", obj["action"]);
        formEdit.find("#login").prop("value", obj["login"]);
        formEdit.find("#log").prop("value", obj["log"]);

        formEdit.modal('show');
    });
}

function aoColumnsFunc() {
    var doc = new Doc();

    var aoColumns = [
        {
            "data": null,
            "title": doc.getDocLabel("page_global", "columnAction"),
            "bSortable": false,
            "bSearchable": false,
            "sWidth": "50px",
            "mRender": function (data, type, obj) {
                var editEntry = '<button id="editEntry" onclick="editEntryClick(\'' + obj["LogEventID"] + '\');"\n\
                                class="editEntry btn btn-default btn-xs margin-right5" \n\
                                name="editEntry" title="' + doc.getDocLabel("page_logviewer", "button_view") + '" type="button">\n\
                                <span class="glyphicon glyphicon-eye-open"></span></button>';

                return '<div class="center btn-group width150">' + editEntry + '</div>';
            }
        },
        {
            "data": "LogEventID",
            "like": true,
            "sName": "LogEventID",
            "sWidth": "40px",
            "title": doc.getDocOnline("logevent", "logeventid")
        },
        {
            "data": "time",
            "like": true,
            "sName": "Time",
            "sWidth": "90px",
            "title": doc.getDocOnline("logevent", "time"),
            "mRender": function (data, type, oObj) {
                return getDate(oObj["time"]);
            }
        },
        {
            "data": "status",
            "sName": "Status",
            "sWidth": "30px",
            "title": doc.getDocOnline("logevent", "status"),
            "mRender": function (data, type, obj) {
                let statusEntry = '<span class="alert-info">' + obj["status"] + '</span>';
                if (obj["status"] === "WARN") {
                    statusEntry = '<span class="alert-warning">' + obj["status"] + '</span>';
                } else if (obj["status"] === "INFO") {
                    statusEntry = '<span class="alert-info">' + obj["status"] + '</span>';
                } else if (obj["status"] === "ERROR") {
                    statusEntry = '<span class="alert-danger">' + obj["status"] + '</span>';
                }
                return statusEntry;
            }
        },
        {
            "data": "login",
            "sName": "Login",
            "sWidth": "50px",
            "title": doc.getDocOnline("logevent", "login")
        },
        {
            "data": "page",
            "sName": "Page",
            "sWidth": "100px",
            "title": doc.getDocOnline("logevent", "page")
        },
        {
            "data": "action",
            "sName": "Action",
            "sWidth": "50px",
            "title": doc.getDocOnline("logevent", "action")
        },
        {
            "data": "log",
            "like": true,
            "sName": "Log",
            "sWidth": "250px",
            "title": doc.getDocOnline("logevent", "log")
        }
    ];
    return aoColumns;
}
