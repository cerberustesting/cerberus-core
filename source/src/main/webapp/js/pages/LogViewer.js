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
        displayPageLabel();

        //configure and create the dataTable
        var configurations = new TableConfigurationsServerSide("logViewerTable", "ReadLogEvent", "contentTable", aoColumnsFunc());

        var table = createDataTable(configurations);
        //By default, sort the log messages from newest to oldest
        table.fnSort([1, 'desc']);
        var api = table.api();
        api.search(buildSearchString()).draw();
    });
});

function buildSearchString() {
    var test = GetURLParameter("Test");
    var testCase = GetURLParameter("TestCase");
    
    if (test !== null && testCase !== null) {
        var searchString = "'" + test + "'|'" + testCase + "'";
        return searchString;
    }
    return '';
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

function editEntry(id) {
    clearResponseMessageMainPage();
    var jqxhr = $.getJSON("ReadLogEvent", "logeventid=" + id);
    $.when(jqxhr).then(function (data) {
        var obj = data["contentTable"];

        var formEdit = $('#editEntryModal');

        formEdit.find("#logeventid").prop("value", id);
        formEdit.find("#time").prop("value", obj["time"]);
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
        {"data": null,
            "title": doc.getDocLabel("page_global", "columnAction"),
            "bSortable": false,
            "bSearchable": false,
            "mRender": function (data, type, obj) {
                var editEntry = '<button id="editEntry" onclick="editEntry(\'' + obj["LogEventID"] + '\');"\n\
                                class="editEntry btn btn-default btn-xs margin-right5" \n\
                                name="editEntry" title="' + doc.getDocLabel("page_logviewer", "button_view") + '" type="button">\n\
                                <span class="glyphicon glyphicon-list-alt"></span></button>';

                return '<div class="center btn-group width150">' + editEntry + '</div>';
            }
        },
        {"data": "time", "sName": "Time", "title": doc.getDocOnline("logevent", "time")},
        {"data": "login", "sName": "Login", "title": doc.getDocOnline("logevent", "login")},
        {"data": "page", "sName": "Page", "title": doc.getDocOnline("logevent", "page")},
        {"data": "action", "sName": "Action", "title": doc.getDocOnline("logevent", "action")},
        {"data": "log", "sName": "Log", "title": doc.getDocOnline("logevent", "log")}
    ];
    return aoColumns;
}
