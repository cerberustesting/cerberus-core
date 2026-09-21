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
 * Executions queue - V2, on js/global/crbTable.js.
 *
 * SCOPE. Only the "Executions in queue" table is migrated. The other three tabs
 * are carried over unchanged:
 *   - Pools Follow Up  : a legacy CLIENT-side DataTable built from an in-memory
 *                        array (TableConfigurationsClientSide). crbTable is
 *                        server-side only, so migrating it would mean writing a
 *                        second engine - out of scope, and it works today.
 *   - Queue History    : a Chart.js graph, no table involved.
 *   - Queue Job Status : a read-only form plus two buttons.
 *
 * Endpoint: ReadTestCaseExecutionQueue (contentTable + table-level hasPermissions
 * + a columnName -> distinctValues mode for the per-column filters).
 *
 * Action gating is a 1:1 port of TestCaseExecutionQueueList.js:531-568:
 *   - Edit/View : always rendered, but the icon AND tooltip say "view" unless the
 *                 user has the permission AND the row is in a state that can still
 *                 be changed (WAITING / ERROR / CANCELLED / QUEUED). That composite
 *                 condition is exactly what the V2 gate contract calls a row gate.
 *   - Duplicate : always
 * and the selection checkbox is permission-gated, as before.
 *
 * Mass actions keep the same six servlets and the same payload (id=..&id=..); only
 * where the ids come from changed - the component's selection instead of
 * serializing a <form> wrapped around the table.
 *
 * Fixed on the way over:
 *   - the tab panels used x-transition.opacity. That transition drives itself with
 *     requestAnimationFrame, which a browser freezes on a backgrounded tab, leaving
 *     every panel visible at once until the next click. Plain x-show now.
 *   - refreshTable() logged "refresh" to the console on every tab switch.
 * ========================================================================== */

$.when($.getScript("js/global/global.js")).then(function () {
    $(document).ready(function () {
        initPage();
        $('[data-toggle="popover"]').popover({placement: 'auto', container: 'body'});
    });
});

var CRB_EXQ_TABLE_ID = "executionQueueTableV2";
var tabClicked = false;

function initPage() {
    var doc = new Doc();

    displayPageLabel();
    initGraph();

    exeQInitDatePickers();

    createCerberusTable({
        id: CRB_EXQ_TABLE_ID,
        mount: "#executionList",
        endpoint: "ReadTestCaseExecutionQueue",
        distinctEndpoint: "ReadTestCaseExecutionQueue",
        rowKey: "id",
        defaultSort: {field: "id", dir: "desc"},
        pageLength: 10,
        lengthMenu: [10, 15, 20, 30, 50, 100, 200, 500, 1000],
        searchPlaceholder: "Search the queue...",
        emptyMessage: "No execution in the queue matches your search",
        // ?search= and ?tag= deep links, applied to the FIRST request. V1 applied
        // them after the table had already loaded once.
        initialSearch: exeQInitialSearch(),
        initialFilters: exeQInitialFilters(),

        // Only rows the user may act on can be ticked - a selection that cannot be
        // submitted is worse than no checkbox.
        selection: {gate: function (row, ctx) { return Boolean(ctx.hasPermissions); }},

        toolbar: function (ctx) {
            var html = "";
            var secondary = "flex items-center gap-1.5 px-3 py-1 rounded-md h-10 border " +
                "border-gray-300 dark:border-gray-600 hover:bg-gray-100 dark:hover:bg-gray-700 transition";

            if (ctx.hasPermissions) {
                html += "<button id='createBrpMassButton' type='button' onclick='exeQMassActionClick()' " +
                    "class='bg-sky-400 hover:bg-sky-500 text-white flex items-center gap-1 px-3 py-1 rounded-lg h-10'>" +
                    "<i data-lucide='list-checks' class='w-4 h-4'></i><span>" +
                    doc.getDocLabel("page_global", "button_massAction") + "</span></button>";
            }
            // Quick filters. Hidden while rows are selected, like the other V2 pages:
            // changing the filter under a selection is how you submit the wrong rows.
            if (!ctx.selectedCount) {
                html += "<button id='selectDepButton' type='button' onclick=\"exeQQuickFilter('QUWITHDEP,QUEUED')\" class='" + secondary + "'>" +
                    "<i data-lucide='git-branch' class='w-4 h-4'></i><span>" +
                    doc.getDocLabel("page_testcaseexecutionqueue", "button_filterPendingWithDep") + "</span></button>";
                html += "<button id='selectPendingButton' type='button' onclick=\"exeQQuickFilter('QUEUED')\" class='" + secondary + "'>" +
                    "<i data-lucide='clock' class='w-4 h-4'></i><span>" +
                    doc.getDocLabel("page_testcaseexecutionqueue", "button_filterPending") + "</span></button>";
                html += "<button id='selectRunningButton' type='button' onclick=\"exeQQuickFilter('EXECUTING,STARTING,WAITING')\" class='" + secondary + "'>" +
                    "<i data-lucide='play' class='w-4 h-4'></i><span>" +
                    doc.getDocLabel("page_testcaseexecutionqueue", "button_filterExecuting") + "</span></button>";
            }
            return html;
        },

        // `field` = server column name (sName in V1), `prop` = key in the row.
        // They differ on testCase, and on the four transversal columns.
        columns: [
            {
                field: "id", prop: "id", title: doc.getDocLabel("page_testcaseexecutionqueue", "id_col"),
                width: "90px", like: true, className: "font-mono text-right tabular-nums",
                render: function (row) { return exeQIdLink(row); }
            },
            {field: "test", prop: "test", title: doc.getDocLabel("page_testcaseexecutionqueue", "test_col"),
             width: "180px", filterable: true},
            {field: "testcase", prop: "testCase",
             title: doc.getDocLabel("page_testcaseexecutionqueue", "testcase_col"),
             width: "100px", like: true, className: "font-mono"},
            {field: "country", prop: "country", title: doc.getDocLabel("page_testcaseexecutionqueue", "country_col"),
             width: "90px", filterable: true},
            {field: "environment", prop: "environment",
             title: doc.getDocLabel("page_testcaseexecutionqueue", "environment_col"),
             width: "120px", filterable: true},
            {field: "robot", prop: "robot", title: doc.getDocLabel("page_testcaseexecutionqueue", "robot_col"),
             width: "120px", filterable: true},
            {
                field: "tag", prop: "tag", title: doc.getDocLabel("page_testcaseexecutionqueue", "tag_col"),
                width: "220px", like: true,
                render: function (row) { return exeQTagLink(row); }
            },
            {
                field: "requestDate", prop: "requestDate",
                title: doc.getDocLabel("page_testcaseexecutionqueue", "requestDate_col"),
                width: "170px", like: true,
                render: function (row) { return crbTableEscape(exeQDate(row.requestDate)); }
            },
            {
                field: "state", prop: "state", title: doc.getDocLabel("page_testcaseexecutionqueue", "state_col"),
                width: "140px", filterable: true,
                render: function (row) { return exeQStateChip(row.state); }
            },
            {field: "comment", prop: "comment", title: doc.getDocLabel("page_testcaseexecutionqueue", "comment_col"),
             width: "280px", like: true},
            {
                field: "exeId", prop: "exeId", title: doc.getDocLabel("page_testcaseexecutionqueue", "exeId"),
                width: "110px", like: true, className: "text-right tabular-nums",
                render: function (row) { return exeQExeIdLink(row); }
            },

            // ---- available from Config, hidden by default ----
            {field: "priority", prop: "priority", title: doc.getDocLabel("testcaseexecutionqueue", "priority"),
             width: "90px", visible: false, className: "text-right tabular-nums"},
            {field: "robotIP", prop: "robotIP", title: doc.getDocLabel("page_testcaseexecutionqueue", "robotIP_col"),
             width: "130px", visible: false},
            {field: "robotPort", prop: "robotPort", title: doc.getDocLabel("page_testcaseexecutionqueue", "robotPort_col"),
             width: "100px", visible: false},
            {field: "browser", prop: "browser", title: doc.getDocLabel("page_testcaseexecutionqueue", "browser_col"),
             width: "110px", visible: false, filterable: true},
            {field: "browserVersion", prop: "browserVersion",
             title: doc.getDocLabel("page_testcaseexecutionqueue", "browserVersion_col"),
             width: "130px", visible: false},
            {field: "platform", prop: "platform", title: doc.getDocLabel("page_testcaseexecutionqueue", "platform_col"),
             width: "110px", visible: false, filterable: true},
            {field: "manualExecution", prop: "manualExecution",
             title: doc.getDocLabel("page_testcaseexecutionqueue", "manualExecution_col"),
             width: "140px", visible: false},
            {field: "manualURL", prop: "manualURL", title: doc.getDocLabel("page_testcaseexecutionqueue", "manualURL_col"),
             width: "140px", visible: false},
            {field: "manualHost", prop: "manualHost",
             title: doc.getDocLabel("page_testcaseexecutionqueue", "manualHost_col"), width: "140px", visible: false},
            {field: "manualContextRoot", prop: "manualContextRoot",
             title: doc.getDocLabel("page_testcaseexecutionqueue", "manualContextRoot_col"),
             width: "160px", visible: false},
            {field: "manualLoginRelativeURL", prop: "manualLoginRelativeURL",
             title: doc.getDocLabel("page_testcaseexecutionqueue", "manualLoginRelativeURL_col"),
             width: "180px", visible: false},
            {field: "manualEnvData", prop: "manualEnvData",
             title: doc.getDocLabel("page_testcaseexecutionqueue", "manualEnvData_col"),
             width: "150px", visible: false},
            {field: "screenshot", prop: "screenshot",
             title: doc.getDocLabel("page_testcaseexecutionqueue", "screenshot_col"), width: "120px", visible: false},
            {field: "pageSource", prop: "pageSource",
             title: doc.getDocLabel("page_testcaseexecutionqueue", "pageSource_col"), width: "120px", visible: false},
            {field: "seleniumLog", prop: "seleniumLog",
             title: doc.getDocLabel("page_testcaseexecutionqueue", "seleniumLog_col"), width: "120px", visible: false},
            {field: "verbose", prop: "verbose", title: doc.getDocLabel("page_testcaseexecutionqueue", "verbose_col"),
             width: "100px", visible: false},
            {field: "retries", prop: "retries", title: doc.getDocLabel("page_testcaseexecutionqueue", "retries_col"),
             width: "100px", visible: false},
            {field: "timeout", prop: "timeout", title: doc.getDocLabel("page_testcaseexecutionqueue", "timeout_col"),
             width: "100px", visible: false},
            {field: "debugFlag", prop: "debugFlag", title: doc.getDocLabel("testcaseexecutionqueue", "debugFlag"),
             width: "110px", visible: false},
            {field: "UsrCreated", prop: "UsrCreated", title: doc.getDocOnline("transversal", "UsrCreated"),
             width: "120px", visible: false},
            {
                field: "DateCreated", prop: "DateCreated", title: doc.getDocOnline("transversal", "DateCreated"),
                width: "170px", visible: false, like: true,
                render: function (row) { return crbTableEscape(exeQDate(row.DateCreated)); }
            },
            {field: "UsrModif", prop: "UsrModif", title: doc.getDocOnline("transversal", "UsrModif"),
             width: "120px", visible: false},
            {
                field: "DateModif", prop: "DateModif", title: doc.getDocOnline("transversal", "DateModif"),
                width: "170px", visible: false,
                render: function (row) { return crbTableEscape(exeQDate(row.DateModif)); }
            }
        ],

        actions: [
            {
                // V1's condition, unchanged: editable only with the permission AND
                // in a state that can still change. Otherwise the same button opens
                // the same modal read-only, which is why the gate is 'always' and
                // only the icon/tooltip move.
                key: "edit", gate: "always",
                icon: function (row, ctx) { return exeQCanEdit(row, ctx) ? "pencil" : "eye"; },
                title: function (row, ctx) {
                    return doc.getDocLabel("page_testcaseexecutionqueue",
                        exeQCanEdit(row, ctx) ? "tooltip_editentry" : "tooltip_viewentry");
                },
                onClick: function (row) { openModalTestCaseExecutionQueue(row.id, 'EDIT'); }
            },
            {
                key: "duplicate", icon: "copy", gate: "always",
                title: doc.getDocLabel("page_testcaseexecutionqueue", "tooltip_dupentry"),
                onClick: function (row) { openModalTestCaseExecutionQueue(row.id, 'DUPLICATE'); }
            }
        ]
    });

    // Mass-action modal buttons - same six handlers, same servlets as V1.
    $("#massActionExeQButtonSubmit").click(function () { exeQMassAction("UpdateTestCaseExecutionQueue", "&actionState=toQUEUED"); });
    $("#massActionExeQButtonCopy").click(exeQMassActionCopy);
    $("#massActionExeQButtonCancel").click(function () { exeQMassAction("UpdateTestCaseExecutionQueue", "&actionState=toCANCELLED"); });
    $("#massActionExeQButtonCancelForce").click(function () { exeQMassAction("UpdateTestCaseExecutionQueue", "&actionState=toCANCELLEDForce"); });
    $("#massActionExeQButtonErrorForce").click(function () { exeQMassAction("UpdateTestCaseExecutionQueue", "&actionState=toERRORForce"); });
    $("#massActionExeQButtonPrio").click(exeQMassActionChangePrio);
    $('#massActionExeQModal').on('hidden.bs.modal', massActionModalCloseHandler);
    window.addEventListener('massaction-modal-close', massActionModalCloseHandler);

    // Restore the last visited tab (the Alpine x-data in the JSP reads the same key).
    var tab = sessionStorage.getItem("TestCaseExecutionQueueList-TAB");
    if (isEmpty(tab) || tab.indexOf('#') === 0) {
        tab = "details";
    }
    switchQueueTab(tab);
}

/* -----------------------------------------------------------------------------
 * Table helpers
 * -------------------------------------------------------------------------- */

/** Editable = has the permission AND the row is in a state that can still change. */
function exeQCanEdit(row, ctx) {
    return Boolean(ctx.hasPermissions) &&
        ["WAITING", "ERROR", "CANCELLED", "QUEUED"].indexOf(row.state) !== -1;
}

/** The queue id links to the execution when there is one, to the queue entry otherwise. */
function exeQIdLink(row) {
    var target = (row.exeId > 0)
        ? "TestCaseExecutionV2.jsp?executionId=" + encodeURIComponent(row.exeId)
        : "TestCaseExecutionV2.jsp?executionQueueId=" + encodeURIComponent(row.id);
    return '<a href="' + target + '">' + crbTableEscape(row.id) + '</a>';
}

function exeQExeIdLink(row) {
    if (!(row.exeId > 0)) {
        return "";
    }
    return '<a href="TestCaseExecution.jsp?executionId=' + encodeURIComponent(row.exeId) + '">' +
        crbTableEscape(row.exeId) + '</a>';
}

function exeQTagLink(row) {
    if (isEmpty(row.tag)) {
        return "";
    }
    return '<a href="ReportingExecutionByTagV2.jsp?Tag=' + encodeURIComponent(row.tag) + '">' +
        crbTableEscape(row.tag) + '</a>';
}

/** Queue state chip. Same colour per state as V1. */
function exeQStateChip(state) {
    state = state || "";
    if (!state) {
        return "";
    }
    var chip = {
        QUEUED: "bg-sky-50 text-sky-700 ring-sky-600/20 dark:bg-sky-900/30 dark:text-sky-300",
        QUEUED_PAUSED: "bg-amber-50 text-amber-700 ring-amber-600/20 dark:bg-amber-900/30 dark:text-amber-300",
        QUWITHDEP: "bg-sky-50 text-sky-600 ring-sky-600/20 dark:bg-sky-900/20 dark:text-sky-400",
        QUWITHDEP_PAUSED: "bg-amber-50 text-amber-700 ring-amber-600/20 dark:bg-amber-900/30 dark:text-amber-300",
        WAITING: "bg-violet-50 text-violet-700 ring-violet-600/20 dark:bg-violet-900/30 dark:text-violet-300",
        STARTING: "bg-blue-50 text-blue-700 ring-blue-600/20 dark:bg-blue-900/30 dark:text-blue-300",
        EXECUTING: "bg-blue-50 text-blue-700 ring-blue-600/20 dark:bg-blue-900/30 dark:text-blue-300",
        DONE: "bg-green-50 text-green-700 ring-green-600/20 dark:bg-green-900/30 dark:text-green-300",
        CANCELLED: "bg-slate-50 text-slate-600 ring-slate-500/20 dark:bg-slate-800 dark:text-slate-300",
        ERROR: "bg-red-50 text-red-700 ring-red-600/20 dark:bg-red-900/30 dark:text-red-300"
    }[state] || "bg-slate-50 text-slate-600 ring-slate-500/20 dark:bg-slate-800 dark:text-slate-300";
    return '<span class="inline-flex items-center rounded-full px-2.5 py-1 text-xs font-semibold ' +
        'ring-1 ring-inset ' + chip + '">' + crbTableEscape(state) + '</span>';
}

/** Unset dates must render blank, not an epoch date. */
function exeQDate(value) {
    if (value === undefined || value === null || value === "" || value === 0) {
        return "";
    }
    return getDate(value);
}

/** ?search=... deep link. */
function exeQInitialSearch() {
    var s = GetURLParameter("search");
    return (s === null || s === "null") ? "" : s;
}

/** ?tag=... deep link, as a column filter (V1 called filterOnColumn after load). */
function exeQInitialFilters() {
    var tag = GetURLParameter("tag");
    return (tag === null || tag === "" || tag === "null") ? {} : {tag: [tag]};
}

/** The three toolbar shortcuts, expressed as a state filter. */
function exeQQuickFilter(states) {
    var table = crbTableInstance(CRB_EXQ_TABLE_ID);
    if (!table) {
        return;
    }
    table.activeFilters["state"] = states.split(",");
    table.start = 0;
    table.fetch();
}

/* -----------------------------------------------------------------------------
 * Mass actions
 * -------------------------------------------------------------------------- */

/**
 * Ids currently ticked.
 *
 * Deliberately selectedKeys and not selectedRows: the latter is a getter over the
 * rows of the CURRENT page, so a selection made across two pages would silently
 * submit only half of it. rowKey is "id" here, so the keys are the ids.
 */
function exeQSelectedIds() {
    var table = crbTableInstance(CRB_EXQ_TABLE_ID);
    return table ? table.selectedKeys : [];
}

/**
 * The payload V1 produced by serializing the <form> that wrapped the table:
 * one id= parameter per ticked row. Same shape, so the servlets are untouched.
 */
function exeQSelectionParams() {
    return exeQSelectedIds().map(function (id) {
        return "id=" + encodeURIComponent(id);
    }).join("&");
}

function exeQMassActionClick() {
    var doc = new Doc();
    clearResponseMessageMainPage();
    if (!exeQSelectedIds().length) {
        showMessage(new Message("danger", doc.getDocLabel("page_global", "message_massActionError")), null);
        return;
    }
    window.dispatchEvent(new CustomEvent('massaction-modal-open'));
}

/** Shared tail of the five state-changing mass actions. */
function exeQMassAction(servlet, extraParams) {
    clearResponseMessage($('#massActionExeQModal'));
    var params = exeQSelectionParams() + extraParams;

    showLoaderInModal('#massActionExeQModal');
    $.when($.post(servlet, params, "json")).then(function (data) {
        hideLoaderInModal('#massActionExeQModal');
        var type = getAlertType(data.messageType);
        if (type === "success" || type === "warning") {
            exeQAfterMassAction();
            showMessage(data);
        } else {
            showMessage(data, $('#massActionExeQModal'));
        }
    }).fail(handleErrorAjaxAfterTimeout);
}

/** Copy needs its own success branch: it links to the execution it created. */
function exeQMassActionCopy() {
    clearResponseMessage($('#massActionExeQModal'));
    var params = exeQSelectionParams() + "&actionState=toQUEUED&actionSave=save";

    showLoaderInModal('#massActionExeQModal');
    $.when($.post("CreateTestCaseExecutionQueue", params, "json")).then(function (data) {
        hideLoaderInModal('#massActionExeQModal');
        var type = getAlertType(data.messageType);
        if (type === "success" || type === "warning") {
            exeQAfterMassAction();
            if (data.addedEntries === 1 && data.testCaseExecutionQueueList && data.testCaseExecutionQueueList[0]) {
                data.message = data.message +
                    "<a href='TestCaseExecution.jsp?executionQueueId=" + data.testCaseExecutionQueueList[0].id +
                    "'><button class='btn btn-primary' id='goToExecution'>Open Execution</button></a>";
            }
            showMessageMainPage(type, data.message, false, 60000);
        } else {
            showMessage(data, $('#massActionExeQModal'));
        }
    }).fail(handleErrorAjaxAfterTimeout);
}

/** Priority change. Same two parameters V1 sent (TestCaseExecutionQueueList.js:460). */
function exeQMassActionChangePrio() {
    clearResponseMessage($('#massActionExeQModal'));
    var newPrio = $('#massActionExeQModalForm #priority').val();
    var params = exeQSelectionParams() + "&actionSave=priority&priority=" + encodeURIComponent(newPrio);

    showLoaderInModal('#massActionExeQModal');
    $.when($.post("UpdateTestCaseExecutionQueue", params, "json")).then(function (data) {
        hideLoaderInModal('#massActionExeQModal');
        var type = getAlertType(data.messageType);
        if (type === "success" || type === "warning") {
            exeQAfterMassAction();
            showMessage(data);
        } else {
            showMessage(data, $('#massActionExeQModal'));
        }
    }).fail(handleErrorAjaxAfterTimeout);
}

/** Reload, drop the selection, close the modal. */
function exeQAfterMassAction() {
    var table = crbTableInstance(CRB_EXQ_TABLE_ID);
    if (table) {
        table.clearSelection();
        table.reload();
    }
    window.dispatchEvent(new CustomEvent('massaction-modal-close'));
}

function massActionModalCloseHandler() {
    var form = $('#massActionExeQModal #massActionExeQModalForm')[0];
    if (form) {
        form.reset();
    }
    $('#massActionExeQModal').find('div.has-error').removeClass("has-error");
    clearResponseMessage($('#massActionExeQModal'));
}

/* -----------------------------------------------------------------------------
 * Tabs, page labels, and the three non-migrated tabs
 * -------------------------------------------------------------------------- */

// Called by the Alpine tab buttons of the page.
function switchQueueTab(name) {
    sessionStorage.setItem("TestCaseExecutionQueueList-TAB", name);
    switch (name) {
        case "details":
            if (tabClicked) {
                refreshTable();
            }
            break;
        case "followup":
            displayAndRefresh_followup();
            tabClicked = true;
            break;
        case "jobstatus":
            displayAndRefresh_jobStatus();
            tabClicked = true;
            break;
        case "history":
            loadStatGraph();
            tabClicked = true;
            break;
    }
}

function refreshTable() {
    var table = crbTableInstance(CRB_EXQ_TABLE_ID);
    if (table) {
        table.reload();
    }
}

function resetTableFilters() {
    var table = crbTableInstance(CRB_EXQ_TABLE_ID);
    if (table) {
        table.activeFilters = {};
        table.search = "";
        table.start = 0;
        table.fetch();
    }
}

function displayPageLabel() {
    var doc = new Doc();
    $("#title").html(doc.getDocLabel("page_testcaseexecutionqueue", "allExecution"));
    $("#pageTitle").html(doc.getDocLabel("page_testcaseexecutionqueue", "allExecution"));
    displayFooter(doc);
    displayGlobalLabel(doc);
}

/** Date pickers of the Queue History tab. Unchanged from V1. */
function exeQInitDatePickers() {
    $('#frompicker').datetimepicker();
    $('#topicker').datetimepicker({useCurrent: false});

    $("#frompicker").on("dp.change", function (e) {
        $('#topicker').data("DateTimePicker").minDate(e.date);
    });
    $("#topicker").on("dp.change", function (e) {
        $('#frompicker').data("DateTimePicker").maxDate(e.date);
    });

    var from = GetURLParameter("from");
    var to = GetURLParameter("to");
    var fromD;
    var toD;
    if (from === null) {
        fromD = new Date();
        fromD.setHours(fromD.getHours() - 1);
    } else {
        fromD = new Date(from);
    }
    if (to === null) {
        toD = new Date();
        toD.setHours(23);
        toD.setMinutes(59);
    } else {
        toD = new Date(to);
    }
    $('#frompicker').data("DateTimePicker").date(moment(fromD));
    $('#topicker').data("DateTimePicker").date(moment(toD));
}

/* --- Carried over unchanged from TestCaseExecutionQueueList.js (V1) ---------
 * The Pools Follow Up table, the Queue Job Status panel and the Queue History
 * graph are not part of this migration; the code below is the V1 code verbatim.
 * ------------------------------------------------------------------------- */
function displayAndRefresh_followup() {
    showLoader('#followUpTableList');

    // Display table
    var jqxhr = $.getJSON("ReadTestCaseExecutionQueue?flag=queueStatus");
    $.when(jqxhr).then(function (data) {
        var obj = data["contentTable"];

        /* TESTCASE */

        var array = [];

        $.each(obj, function (e) {

            array.push(
                    [obj[e].contrainId, obj[e].system, obj[e].environment, obj[e].country, obj[e].application
                                , obj[e].robot, obj[e].nbRunning, obj[e].nbPoolSize, obj[e].nbInQueue, obj[e].hasPermissionsUpdate, obj[e].invariantExist]
                    );
        });

        if ($("#followUpTableList #followUpTable_wrapper").length > 0) {
            $("#followUpTableList #followUpTable").DataTable().clear();
            $("#followUpTableList #followUpTable").DataTable().rows.add(array).draw();
        } else {
            var configurations1 = new TableConfigurationsClientSide("followUpTable", array, aoColumnsFunc_followUp(), true, [1, 'asc']);
            createDataTableWithPermissionsNew(configurations1, undefined, "#followUpTableList", undefined, true);
        }

        hideLoader('#followUpTableList');
    });


}

function displayAndRefresh_jobStatus() {
    showLoader('#QueueJobStatus');
    showLoader('#QueueJobActive');

    var jqxhr = $.getJSON("GetExecutionsInQueue");
    $.when(jqxhr).then(function (data) {
        var obj = data;

        $("#jobRunning").val(data["jobRunning"]);
        $("#jobStart").val(data["jobStart"]);
        $("#jobActive").val(data["jobActive"].toString());
        $("#instanceJobActive").val(data["executionThreadPoolInstanceActive"].toString());
        if (data["jobActive"]) {
            $("#jobActiveStatus").html('<i data-lucide="refresh-cw" class="w-7 h-7 animate-spin" style="color: var(--crb-green-color, #00d27a); animation-duration: 3s"></i>');
            $("#modifyParambutton").html('<i data-lucide="pause" class="w-4 h-4"></i><span>Stop Queue Job</span>');
        } else {
            $("#jobActiveStatus").html('<i data-lucide="pause" class="w-7 h-7" style="color: var(--crb-orange-color, #f5803e)"></i>');
            $("#modifyParambutton").html('<i data-lucide="play" class="w-4 h-4"></i><span>Start Queue Job</span>');
        }
        if (window.lucide) {
            lucide.createIcons();
        }

        if (data["jobActiveHasPermissionsUpdate"]) {
            $("#modifyParambutton").attr("disabled", false);
        } else {
            $("#modifyParambutton").attr("disabled", true);
        }

        hideLoader('#QueueJobStatus');
        hideLoader('#QueueJobActive');

    });
}

function forceExecution() {

    var jqxhr = $.getJSON("GetExecutionsInQueue?forceExecution=Y");
    $.when(jqxhr).then(function (data) {
        var obj = data;

        displayAndRefresh_jobStatus();

    });
}

function aoColumnsFunc_followUp() {
    var doc = new Doc();
    var aoColumns = [
        {
            "data": null,
            "sWidth": "50px",
            "sSearchable": false,
            "sName": "action",
            "title": doc.getDocLabel("page_global", "columnAction"),
            "mRender": function (data, type, oObj) {
                const baseBtnClass = "inline-flex aspect-square h-8 w-8 items-center justify-center rounded-md transition-all duration-200 " +
                    "text-slate-500 hover:bg-slate-200 dark:hover:bg-slate-800 " +
                    "opacity-20 group-hover:opacity-100 [&_svg]:size-4 group-hover:!text-blue-500";
                function fuButton(title, onClick, icon) {
                    return '<button type="button" class="' + baseBtnClass + '" title="' + title + '" onclick="' + onClick + '">' +
                        '<i data-lucide="' + icon + '" class="w-4 h-4"></i></button>';
                }
                var editGlobalParam = fuButton(doc.getDocLabel("page_parameter", "editparameter_field"), 'openModalParameter(\'cerberus_queueexecution_global_threadpoolsize\',\'' + getSys() + '\');', 'pencil');
                var editRobotParam = fuButton(doc.getDocLabel("page_parameter", "editparameter_field"), 'openModalParameter(\'cerberus_queueexecution_defaultrobothost_threadpoolsize\',\'' + getSys() + '\');', 'pencil');
                var editRobotInvariant = fuButton(doc.getDocLabel("page_invariant", "button_edit"), 'openModalInvariant(\'ROBOTHOST\',\'' + data[5] + '\',\'EDIT\',\'tabInvAdvanced\');', 'pencil');
                var addRobotInvariant = fuButton(doc.getDocLabel("page_invariant", "button_create"), 'openModalInvariant(\'ROBOTHOST\',\'' + data[5] + '\',\'ADD\',\'tabInvAdvanced\');', 'plus');
                var editRobotExtParam = fuButton(doc.getDocLabel("page_parameter", "editparameter_field"), 'openModalParameter(\'cerberus_queueexecution_defaultexecutorexthost_threadpoolsize\',\'' + getSys() + '\');', 'pencil');
                var editRobotExtInvariant = fuButton(doc.getDocLabel("page_invariant", "button_edit"), 'openModalInvariant(\'ROBOTPROXYHOST\',\'' + data[5] + '\',\'EDIT\',\'tabInvAdvanced\');', 'pencil');
                var addRobotExtInvariant = fuButton(doc.getDocLabel("page_invariant", "button_create"), 'openModalInvariant(\'ROBOTPROXYHOST\',\'' + data[5] + '\',\'ADD\',\'tabInvAdvanced\');', 'plus');
                var editApplication = fuButton(doc.getDocLabel("page_invariant", "button_edit"), 'openModalApplication(\'' + data[4] + '\', \'EDIT\', \'ApplicationList\');', 'pencil');

                var buttons = "";
                if ((data[0] === "constrain1_global") && (data[9])) {
                    // Constrain is global and hasPermitionUpdate is true.
                    buttons += editGlobalParam;
                }
                if (((data[0] === "constrain2_applienvironment") || (data[0] === "constrain3_application")) && (data[9]))
                {
                    console.info(data);
                    // Constrain is global and hasPermitionUpdate is true.
                    buttons += editApplication;
                }
                if ((data[0] === "constrain4_robot") && (data[9])) {
                    // Constrain is global and hasPermitionUpdate is true.
                    if (data[10]) {
                        // Invariant exist. We can edit it.
                        buttons += editRobotInvariant;
                    } else if (!isEmpty(data[5]) && data[5] !== "null") {
                        //Invariant does not exist and is not null or empty. We can either create it or change default parameter.
                        buttons += editRobotParam;
                        buttons += addRobotInvariant;
                    }
                }
                if ((data[0] === "constrain5_proxyservice") && (data[9])) {
                    // Constrain is global and hasPermitionUpdate is true.
                    if (data[10]) {
                        // Invariant exist. We can edit it.
                        buttons += editRobotExtInvariant;
                    } else if (!isEmpty(data[5]) && data[5] !== "null") {
                        //Invariant does not exist and is not null or empty. We can either create it or change default parameter.
                        buttons += editRobotExtParam;
                        buttons += addRobotExtInvariant;
                    }
                }
                return '<div class="flex items-center justify-start gap-1">' + buttons + '</div>';
            }
        }
        ,
        {"data": "0", "sName": "constrainsId", "sWidth": "100px", "title": doc.getDocLabel("page_testcaseexecutionqueue", "constrain")},
        {"data": "1", "sName": "system", "sWidth": "50px", "title": doc.getDocLabel("invariant", "SYSTEM")},
        {"data": "2", "sName": "environment", "sWidth": "50px", "title": doc.getDocLabel("invariant", "ENVIRONMENT")},
        {"data": "3", "sName": "country", "sWidth": "50px", "title": doc.getDocLabel("invariant", "COUNTRY")},
        {"data": "4", "sName": "application", "sWidth": "50px", "title": doc.getDocLabel("application", "Application")},
        {"data": "5", "sName": "robot", "sWidth": "50px", "title": doc.getDocLabel("page_testcaseexecutionqueue", "robothost")},
        {"data": "6", "sName": "nbRunning", "sWidth": "50px", "title": doc.getDocLabel("page_testcaseexecutionqueue", "nbRunning")},
        {"data": "7", "sName": "nbPoolSize", "sWidth": "50px", "title": doc.getDocLabel("page_testcaseexecutionqueue", "nbPoolSize")},
        {"data": "8", "sName": "nbInQueue", "sWidth": "50px", "title": doc.getDocLabel("page_testcaseexecutionqueue", "nbInQueue")},
        {
            "data": null, "sName": "saturation", "sWidth": "50px", "title": doc.getDocLabel("page_testcaseexecutionqueue", "saturation"),
            "mRender": function (data, type, obj) {
                var saturation_level;
                var satcolor;
                if (obj[7] > 0) {
                    saturation_level = (obj[6] / obj[7]) * 100;
                    saturation_level = Math.round(saturation_level * 10) / 10

                } else {
                    saturation_level = 0;
                }
                if (saturation_level > 90) {
                    satcolor = "#D9534F";
                } else {
                    satcolor = "#5CB85C";
                }
                return "<div class='progress-bar' role='progressbar' style='width:" + saturation_level + "%; background-color: " + satcolor + ";'>" + saturation_level + "%</div>";

            }
        },
        {
            "data": null, "sName": "oversaturation", "sWidth": "50px", "title": doc.getDocLabel("page_testcaseexecutionqueue", "oversaturation"),
            "mRender": function (data, type, obj) {
                if ((obj[7] > 0) && ((obj[6] >= obj[7]))) {
                    return obj[8];
                } else {
                    return "";
                }
            }
        }
    ];
    return aoColumns;
}

function getOptions(title, unit) {
    let option = {
        responsive: true,
        maintainAspectRatio: false,
        hover: {
            mode: 'nearest',
            intersect: true
        },
        tooltips: {
            callbacks: {
                label: function (t, d) {
                    var xLabel = d.datasets[t.datasetIndex].label;
                    return xLabel + ': ' + t.yLabel;
                }
            },
        },
        title: {
            text: title
        },
        scales: {
            xAxes: [{
                    type: 'time',
                    time: {
                        tooltipFormat: 'll HH:mm:ss'
                    },
                    scaleLabel: {
                        display: true,
                        labelString: 'Date'
                    }
                }],
            yAxes: [{
                    scaleLabel: {
                        display: true,
                        labelString: title
                    },
                    ticks: {
                        callback: function (value, index, values) {
                            return value;
                        }}

                }]
        }
    };
    return option;
}

function initGraph() {

    var queueStatoption = getOptions("", "nb");

    let queueStatdatasets = [];

    configQueueStat = {
        type: 'line',
        data: {
            datasets: queueStatdatasets
        },
        options: queueStatoption
    };

    var ctx = document.getElementById('canvasQueueStat').getContext('2d');
    window.myLineQueueStat = new Chart(ctx, configQueueStat);
}


function loadStatGraph() {
    showLoader($("#qsFilterPanel"));

    let from = new Date($('#frompicker').data("DateTimePicker").date());
    let to = new Date($('#topicker').data("DateTimePicker").date());

    let qS = "from=" + from.toISOString() + "&to=" + to.toISOString();
//    let qS = "from=2020-08-07T01:01:01.0Z&to=2020-08-07T16:14:01.0Z";

    $.ajax({
        url: "ReadQueueStat?" + qS,
        method: "GET",
        async: true,
        dataType: 'json',
        success: function (data) {
            var messageType = getAlertType(data.messageType);

            if (data.messageType === "OK") {
                buildGraphs(data);
            } else {
                showMessageMainPage(messageType, data.message, false);
            }
            hideLoader($("#qsFilterPanel"));
        },
        error: showUnexpectedError
    });
}


function buildGraphs(data) {

    let curves = data.datasetQueueStat;

    // Sorting values by nb of requests.
    sortedCurves = curves.sort(function (a, b) {
//        let a1 = a.key.testcase.test + "-" + a.key.testcase.testcase + "-" + a.key.unit + "-" + a.key.country + "-" + a.key.environment + "-" + a.key.robotdecli;
//        let b1 = b.key.testcase.test + "-" + b.key.testcase.testcase + "-" + b.key.unit + "-" + b.key.country + "-" + b.key.environment + "-" + a.key.robotdecli;
//        return b1.localeCompare(a1);
        return true;
    });

    var len = sortedCurves.length;

    let timedatasets = [];

    for (var i = 0; i < len; i++) {

        let c = sortedCurves[i];
        let d = [];
        lend = c.points.length;
        for (var j = 0; j < lend; j++) {
            let p = {x: c.points[j].x, y: c.points[j].y};
            d.push(p);
        }
        let lab = c.key.key;
        let doFill = false;
        if (c.key.key === "CurrentlyRunning") {
            doFill = true;
        }
        var dataset = {
            label: lab,
            backgroundColor: "white",
            borderColor: getColorQueueStat(c.key.key),
            pointBackgroundColor: getColorQueueStat(c.key.key),
            pointRadius: 1,
            pointHoverRadius: 6,
            hitRadius: 10,
            fill: doFill,
            lineTension: 0,
            data: d
        };
        timedatasets.push(dataset);
    }

    if (timedatasets.length > 0) {
        $("#panelQueueStat").show();
    } else {
        $("#panelQueueStat").hide();
    }
    configQueueStat.data.datasets = timedatasets;

    window.myLineQueueStat.update();
}

function getColorQueueStat(name) {
    switch (name) {
        case "CurrentlyRunning":
            return "green";
        case "GlobalConstrain":
            return "red";
        case "QueueSize":
            return "darkblue";
    }
    return "red";

}
