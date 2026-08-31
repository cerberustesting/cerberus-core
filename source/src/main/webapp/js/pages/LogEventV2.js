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

/* =============================================================================
 * Log viewer - V2, on js/global/crbTable.js.
 *
 * Endpoint: ReadLogEvent (contentTable + a columnName -> distinctValues mode).
 * A read-only audit trail: one action, View, which opens the detail modal. No
 * create, no edit, no delete - so the single gate is 'always'.
 *
 * Fixed on the way over:
 *   - the View handler was built as onclick="editEntryClick('${obj.logEventID}')"
 *     with no escaping at all. The id is numeric today, but the pattern is the one
 *     that made a test folder name executable elsewhere in this app; V2 passes the
 *     row to onClick so nothing from the data reaches the markup.
 *   - ?Test=&TestCase= wrapped both values in quotes and fed them to DataTables'
 *     client-side regex search ("'a'|'b'"), which the server-side search does not
 *     understand - the filter silently matched nothing. It is now a plain search on
 *     the two values, which is what the link meant.
 * ========================================================================== */

$.when($.getScript("js/global/global.js")).then(function () {
    $(document).ready(function () {
        initPage();
        $('[data-toggle="popover"]').popover({placement: 'auto', container: 'body'});
    });
});

var CRB_LOG_TABLE_ID = "logEventTableV2";

function initPage() {
    var doc = new Doc();

    displayPageLabel();

    createCerberusTable({
        id: CRB_LOG_TABLE_ID,
        mount: "#logViewer",
        endpoint: "ReadLogEvent",
        distinctEndpoint: "ReadLogEvent",
        rowKey: "logEventID",
        defaultSort: {field: "logEventID", dir: "desc"},
        pageLength: 10,
        lengthMenu: [10, 15, 20, 30, 50, 100, 200, 500],
        searchPlaceholder: "Search the audit trail...",
        emptyMessage: "No log entry matches your search",
        initialSearch: logV2InitialSearch(),

        // `field` = server column name (sName in V1 - note the capitals), `prop` =
        // key in the row.
        columns: [
            {field: "logEventID", prop: "logEventID", title: doc.getDocOnline("logevent", "logeventid"),
             width: "90px", like: true, className: "font-mono text-right tabular-nums"},
            {
                field: "Time", prop: "time", title: doc.getDocOnline("logevent", "time"),
                width: "180px", like: true,
                render: function (row) { return crbTableEscape(logV2Date(row.time)); }
            },
            {
                field: "Status", prop: "status", title: doc.getDocOnline("logevent", "status"),
                width: "120px", filterable: true,
                render: function (row) { return logV2StatusChip(row.status); }
            },
            {field: "Login", prop: "login", title: doc.getDocOnline("logevent", "login"),
             width: "130px", filterable: true},
            {field: "Page", prop: "page", title: doc.getDocOnline("logevent", "page"),
             width: "180px", filterable: true},
            {field: "Action", prop: "action", title: doc.getDocOnline("logevent", "action"),
             width: "130px", filterable: true},
            {field: "Log", prop: "log", title: doc.getDocOnline("logevent", "log"),
             width: "420px", like: true},

            // ---- available from Config, hidden by default ----
            {field: "RemoteIP", prop: "remoteIP", title: doc.getDocOnline("logevent", "remoteip"),
             width: "140px", visible: false},
            {field: "LocalIP", prop: "localIP", title: doc.getDocOnline("logevent", "localip"),
             width: "140px", visible: false}
        ],

        actions: [
            {
                key: "view", icon: "eye", gate: "always",
                title: doc.getDocLabel("page_logviewer", "button_view"),
                onClick: function (row) { logV2ViewEntry(row.logEventID); }
            }
        ]
    });
}

/**
 * ?Test=..&TestCase=.. deep link.
 *
 * V1 built "'<test>'|'<testcase>'" - a DataTables client-side regex - and handed
 * it to a server-side search that treats it as a literal string, so the link
 * filtered to nothing. The two values as a plain search term is what it meant.
 */
function logV2InitialSearch() {
    var test = GetURLParameter("Test");
    var testCase = GetURLParameter("TestCase");
    var parts = [];
    if (test !== null && test !== "" && test !== "null") {
        parts.push(test);
    }
    if (testCase !== null && testCase !== "" && testCase !== "null") {
        parts.push(testCase);
    }
    return parts.join(" ");
}

/** Charter status chip. Same three states and colours as V1. */
function logV2StatusChip(status) {
    status = status || "";
    if (!status) {
        return "";
    }
    var chip = {
        INFO: {cls: "bg-sky-50 text-sky-700 ring-sky-600/20 dark:bg-sky-900/30 dark:text-sky-300", icon: "info"},
        WARN: {cls: "bg-amber-50 text-amber-700 ring-amber-600/20 dark:bg-amber-900/30 dark:text-amber-300", icon: "triangle-alert"},
        ERROR: {cls: "bg-red-50 text-red-700 ring-red-600/20 dark:bg-red-900/30 dark:text-red-300", icon: "circle-x"}
    }[status] || {cls: "bg-slate-50 text-slate-600 ring-slate-500/20 dark:bg-slate-800 dark:text-slate-300", icon: "circle"};

    return '<span class="inline-flex items-center justify-center gap-1.5 rounded-full px-2.5 py-1 ' +
        'text-xs font-semibold ring-1 ring-inset ' + chip.cls + '">' +
        '<i data-lucide="' + chip.icon + '" class="h-3.5 w-3.5"></i>' +
        '<span>' + crbTableEscape(status) + '</span></span>';
}

function logV2Date(value) {
    if (value === undefined || value === null || value === "" || value === 0) {
        return "";
    }
    return getDate(value);
}

/** Loads one log entry into the detail modal. Body identical to LogEvent.js:76-99. */
function logV2ViewEntry(id) {
    clearResponseMessageMainPage();
    var jqxhr = $.getJSON("ReadLogEvent", "logeventid=" + encodeURIComponent(id));
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

/** V1's name, kept as an alias in case a shared include ever calls it. */
var editEntryClick = logV2ViewEntry;

function displayPageLabel() {
    var doc = new Doc();

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
