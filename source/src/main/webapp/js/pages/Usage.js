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
    displayPageLabel();
    loadAIUsageTable();
}

function loadLogViewerTable() {
    if ($.fn.dataTable.isDataTable('#logViewerTable')) {
        $('#logViewerTable').DataTable();
    } else {
        //configure and create the dataTable for Log
        var configurationsLogViewer = new TableConfigurationsServerSide("logViewerTable", "ReadLogEvent", "contentTable", aoColumnsFuncLogViewer(), [1, 'desc']);
        createDataTableWithPermissions(configurationsLogViewer, renderOptionsForLogViewer, "#logViewer");
    }
}

function loadAIUsageTable(filter) {
    let table;

    if ($.fn.dataTable.isDataTable('#aiUsageTable')) {
        table = $('#aiUsageTable').DataTable();
    } else {
        // configure and create the dataTable for AI Usage
        const config = new TableConfigurationsServerSide("aiUsageTable","api/usage/aiCallList","contentTable",aoColumnsFuncAIUsage(),[1, 'desc']);
        table = createDataTableWithPermissions(config, renderOptionsForAIUsage, "#aiUsage");
    }

    // Filter
    if (filter && filter !== "ALL") {
        table.search(filter).draw();
    } else {
        table.search("").draw(); // reset search
    }
}

function renderOptionsForLogViewer() {
    $("#logViewerTable_paginate").parent().addClass("col-md-12").addClass("paddingRight0");
}

function renderOptionsForAIUsage() {

}

function displayPageLabel() {
    var doc = new Doc();
    displayFooter(doc);
    displayGlobalLabel(doc);
}

function editEntryClick(id) {
    clearResponseMessageMainPage();
    var jqxhr = $.getJSON("ReadLogEvent", "logeventid=" + id);
    $.when(jqxhr).then(function (data) {
        var obj = data["contentTableLog"];

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

function aoColumnsFuncLogViewer() {
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

function aoColumnsFuncAIUsage() {
    var doc = new Doc();

    var aoColumns = [
        {
            "data": null,
            "title": doc.getDocLabel("page_global", "columnAction"),
            "bSortable": false,
            "bSearchable": false,
            "sWidth": "50px",
            "mRender": function (data, type, obj) {
                return '<div class="center btn-group width150">' +
                    '<button onclick="viewAIUsage(' + obj["id"] + ');" ' +
                    'class="btn btn-default btn-xs margin-right5" title="View Details">' +
                    '<span class="glyphicon glyphicon-eye-open"></span></button>' +
                    '</div>';
            }
        },
        {
            "data": "id",
            "sName": "id",
            "sWidth": "40px",
            "title": "ID"
        },
        {
            "data": "login",
            "sName": "login",
            "sWidth": "90px",
            "title": "User"
        },
        {
            "data": "sessionID",
            "sName": "sessionID",
            "sWidth": "90px",
            "title": "Session ID"
        },
        {
            "data": "iaModel",
            "sName": "iaModel",
            "sWidth": "90px",
            "title": "Model"
        },
        {
            "data": "iaMaxTokens",
            "sName": "iaMaxTokens",
            "sWidth": "80px",
            "title": "Max Tokens"
        },
        {
            "data": "type",
            "sName": "type",
            "sWidth": "80px",
            "title": "Type"
        },
        {
            "data": "title",
            "like": true,
            "sName": "title",
            "sWidth": "250px",
            "title": "Title",
            "mRender": function(data, type, obj) {
                return data ? data.substring(0, 80) + "..." : "";
            }
        },
        {
            "data": "totalCalls",
            "sName": "totalCalls",
            "sWidth": "80px",
            "title": "Total Calls"
        },
        {
            "data": "totalInputTokens",
            "sName": "totalInputTokens",
            "sWidth": "80px",
            "title": "Total Input Tokens"
        },
        {
            "data": "totalOutputTokens",
            "sName": "totalOutputTokens",
            "sWidth": "80px",
            "title": "Total Output Tokens"
        },
        {
            "data": "totalCost",
            "sName": "totalCost",
            "sWidth": "60px",
            "title": "Cost ($)",
            "mRender": function(data) {
                return data ? data.toFixed(2) : "0.00";
            }
        },
        {
            "data": "usrCreated",
            "sName": "usrCreated",
            "sWidth": "90px",
            "title": "User"
        },
        {
            "data": "dateCreated",
            "sName": "dateCreated",
            "sWidth": "130px",
            "title": "Created",
            "mRender": function (data) {
                return data ? getDate(data) : "";
            }
        }
    ];

    return aoColumns;
}
