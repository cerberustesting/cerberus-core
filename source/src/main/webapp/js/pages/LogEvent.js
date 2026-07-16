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

    //configure and create the dataTable
    var configurations = new TableConfigurationsServerSide("logViewerTable", "ReadLogEvent", "contentTable", aoColumnsFunc(), [1, 'desc']);

    var table = createDataTableWithPermissionsNew(configurations, undefined, "#logViewer", undefined, true);

    // action buttons reveal on row hover + lucide icons in cells
    $('#logViewerTable').on('draw.dt', function () {
        $(this).find('tbody tr').addClass('group');
        if (window.lucide) {
            lucide.createIcons();
        }
    });

    // if test and testcase parameter are sent, we filter the logs on it.
    if (test !== null && testCase !== null && table && table.search) {
        var searchString = "'" + test + "'|'" + testCase + "'";
        table.search(searchString).draw();
    }
}

function displayPageLabel() {
    var doc = new Doc();

    //displayHeaderLabel(doc);
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

        window.dispatchEvent(new CustomEvent('editlogevent-modal-open'));
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
            "mRender": function (data, type, obj, meta) {
                const baseBtnClass = "inline-flex aspect-square h-8 w-8 items-center justify-center rounded-md transition-all duration-200 " +
                    "text-slate-500 hover:bg-slate-200 dark:hover:bg-slate-800 " +
                    "opacity-20 group-hover:opacity-100 [&_svg]:size-4";

                var viewEntry = `
                <button
                    id="logevent_action_view_row_${meta.row}"
                    type="button"
                    class="${baseBtnClass} group-hover:!text-blue-500"
                    title="${doc.getDocLabel("page_logviewer", "button_view")}"
                    onclick="editEntryClick('${obj["logEventID"]}')">
                    <i data-lucide="eye" class="w-4 h-4"></i>
                </button>
            `;

                return `<div class="flex items-center justify-start gap-1">${viewEntry}</div>`;
            }
        },
        {
            "data": "logEventID",
            "like": true,
            "sName": "logEventID",
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
            "sWidth": "90px",
            "title": doc.getDocOnline("logevent", "status"),
            "mRender": function (data, type, obj) {
                var status = obj["status"] || "";
                var chip = {
                    INFO: { cls: "bg-sky-50 text-sky-700 ring-sky-600/20 dark:bg-sky-900/30 dark:text-sky-300", icon: "info" },
                    WARN: { cls: "bg-amber-50 text-amber-700 ring-amber-600/20 dark:bg-amber-900/30 dark:text-amber-300", icon: "triangle-alert" },
                    ERROR: { cls: "bg-red-50 text-red-700 ring-red-600/20 dark:bg-red-900/30 dark:text-red-300", icon: "circle-x" }
                }[status] || { cls: "bg-slate-50 text-slate-600 ring-slate-500/20 dark:bg-slate-800 dark:text-slate-300", icon: "circle" };

                return `
    <span class="inline-flex items-center justify-center gap-1.5 rounded-full px-2.5 py-1 text-xs font-semibold ring-1 ring-inset ${chip.cls}">
        <i data-lucide="${chip.icon}" class="h-3.5 w-3.5"></i>
        <span>${escapeHtml(status)}</span>
    </span>
`;
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
