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
 * Execution history - V2, on js/global/crbTable.js.
 *
 * Endpoint: ReadTestCaseExecution (contentTable, table-level hasPermissions, and a
 * columnName -> distinctValues mode so the per-column filters work).
 *
 * A read-only page: no create, no delete, and every action is a navigation. So
 * every gate is 'always', except the execution-tag one, which V1 rendered only
 * when the execution actually carries a tag - that becomes a row-level gate.
 *
 * Deep links: TestCaseExecutionList.jsp?Test=..&TestCase=..&country=..&environment=..
 * still pre-filter the list (the "last executions" action links here that way).
 * V1 did it by calling applyFiltersOnMultipleColumns after building the table;
 * V2 seeds cfg.initialFilters instead, so the very first request already carries
 * the filter and the page never flashes the unfiltered list.
 *
 * Fixed on the way over:
 *   - the Tag column's renderer disabled "#tagExec<id>", an element that does not
 *     exist on this page (the tag button is #execution_action_tag_<id>). A render
 *     function that mutates the DOM of another cell is also a rendering-order trap;
 *     the tag button is now gated on the row's own data instead.
 *   - generateTooltip() read data.controlMessage.length unguarded, which throws on
 *     an execution with no control message and leaves the whole cell blank.
 *   - "Invalid Date" in the Start/End columns for executions with no end date.
 * ========================================================================== */

$.when($.getScript("js/global/global.js")).then(function () {
    $(document).ready(function () {
        initPage();
        $('[data-toggle="popover"]').popover({placement: 'auto', container: 'body'});
    });
});

var CRB_EXE_TABLE_ID = "executionListTableV2";

function initPage() {
    var doc = new Doc();

    displayPageLabel();

    createCerberusTable({
        id: CRB_EXE_TABLE_ID,
        mount: "#testCaseExecution",
        endpoint: "ReadTestCaseExecution",
        distinctEndpoint: "ReadTestCaseExecution",
        rowKey: "id",
        defaultSort: {field: "exe.id", dir: "desc"},
        pageLength: 10,
        lengthMenu: [10, 15, 20, 30, 50, 100, 500, 1000],
        searchPlaceholder: "Search executions...",
        emptyMessage: "No execution matches your search",
        initialFilters: exeV2FiltersFromUrl(),

        // `field` = server column name (sName in V1, prefixed exe.*), `prop` = key
        // in the returned row. They differ on every column here, which is exactly
        // the conflation that made the first migrated page render empty cells.
        columns: [
            {
                field: "exe.controlStatus", prop: "controlStatus",
                title: doc.getDocOnline("page_executiondetail", "controlstatus"),
                width: "130px", filterable: true,
                render: function (row) { return exeV2StatusChip(row); }
            },
            {field: "exe.id", prop: "id", title: doc.getDocOnline("page_executiondetail", "id"),
             width: "90px", like: true, className: "font-mono text-right tabular-nums"},
            {
                field: "exe.start", prop: "start", title: doc.getDocOnline("page_executiondetail", "start"),
                width: "170px", like: true,
                render: function (row) { return crbTableEscape(exeV2DateTime(row.start)); }
            },
            {field: "exe.test", prop: "test", title: doc.getDocOnline("test", "Test"),
             width: "190px", filterable: true},
            {field: "exe.testcase", prop: "testcase", title: doc.getDocOnline("testcase", "TestCase"),
             width: "100px", like: true, className: "font-mono"},
            {field: "exe.country", prop: "country", title: doc.getDocOnline("page_executiondetail", "country"),
             width: "90px", filterable: true},
            {field: "exe.environment", prop: "environment",
             title: doc.getDocOnline("page_executiondetail", "environment"),
             width: "120px", filterable: true},
            {field: "exe.application", prop: "application",
             title: doc.getDocOnline("page_executiondetail", "application"),
             width: "140px", filterable: true},
            {field: "exe.applicationType", prop: "applicationType",
             title: doc.getDocOnline("page_executiondetail", "applicationType"),
             width: "110px", filterable: true},
            {field: "exe.robot", prop: "robot", title: doc.getDocOnline("page_executiondetail", "robot"),
             width: "120px", filterable: true},
            {field: "exe.controlmessage", prop: "controlMessage",
             title: doc.getDocOnline("page_executiondetail", "controlmessage"),
             width: "340px", like: true},
            {field: "exe.tag", prop: "tag", title: doc.getDocOnline("page_executiondetail", "tag"),
             width: "220px", like: true},

            // ---- available from Config, hidden by default ----
            {field: "exe.TestCaseVersion", prop: "testCaseVersion",
             title: doc.getDocOnline("testcase", "version"), width: "90px", visible: false},
            {field: "exe.status", prop: "status", title: doc.getDocOnline("page_executiondetail", "status"),
             width: "110px", visible: false, filterable: true},
            {field: "exe.description", prop: "description", title: doc.getDocOnline("testcase", "Description"),
             width: "260px", visible: false, like: true},
            {field: "exe.build", prop: "build", title: doc.getDocOnline("page_executiondetail", "build"),
             width: "100px", visible: false, filterable: true},
            {field: "exe.revision", prop: "revision", title: doc.getDocOnline("page_executiondetail", "revision"),
             width: "100px", visible: false, filterable: true},
            {field: "exe.url", prop: "url", title: doc.getDocOnline("page_executiondetail", "url"),
             width: "240px", visible: false},
            {field: "exe.browser", prop: "browser", title: doc.getDocOnline("page_executiondetail", "browser"),
             width: "110px", visible: false, filterable: true},
            {field: "exe.version", prop: "version", title: doc.getDocOnline("page_executiondetail", "version"),
             width: "100px", visible: false},
            {field: "exe.platform", prop: "platform", title: doc.getDocOnline("page_executiondetail", "platform"),
             width: "110px", visible: false, filterable: true},
            {
                field: "exe.end", prop: "end", title: doc.getDocOnline("page_executiondetail", "end"),
                width: "170px", visible: false, like: true,
                render: function (row) { return crbTableEscape(exeV2DateTime(row.end)); }
            },
            {
                field: "exe.durationMs", prop: "durationMs",
                title: doc.getDocOnline("testcaseexecution", "durationMs"),
                width: "110px", visible: false, like: true, className: "text-right tabular-nums",
                render: function (row) {
                    return (row.durationMs === undefined || row.durationMs === null || row.durationMs === "")
                        ? "" : crbTableEscape(getHumanReadableDuration(row.durationMs / 1000));
                }
            },
            {
                field: "exe.isUseful", prop: "isUseful", title: doc.getDocOnline("testcaseexecution", "isUseful"),
                width: "100px", visible: false,
                render: function (row) { return exeV2Bool(row.isUseful); }
            },
            {
                field: "exe.isFlaky", prop: "isFlaky", title: doc.getDocOnline("testcaseexecution", "isFlaky"),
                width: "100px", visible: false,
                render: function (row) { return exeV2Bool(row.isFlaky); }
            },
            {
                field: "exe.falseNegative", prop: "falseNegative",
                title: doc.getDocOnline("testcaseexecution", "falseNegative"),
                width: "130px", visible: false,
                render: function (row) { return exeV2Bool(row.falseNegative); }
            },
            {field: "exe.ip", prop: "ip", title: doc.getDocOnline("page_executiondetail", "robothost"),
             width: "130px", visible: false},
            {field: "exe.port", prop: "port", title: doc.getDocOnline("page_executiondetail", "robotport"),
             width: "100px", visible: false},
            {field: "exe.verbose", prop: "verbose", title: doc.getDocOnline("page_executiondetail", "verbose"),
             width: "100px", visible: false},
            {field: "exe.crbVersion", prop: "crbVersion",
             title: doc.getDocOnline("page_executiondetail", "cerberusversion"),
             width: "130px", visible: false, like: true},
            {field: "exe.executor", prop: "executor", title: doc.getDocOnline("page_executiondetail", "executor"),
             width: "130px", visible: false, filterable: true},
            {field: "exe.screensize", prop: "screenSize",
             title: doc.getDocOnline("page_executiondetail", "screensize"), width: "120px", visible: false},
            {field: "exe.userAgent", prop: "userAgent",
             title: doc.getDocOnline("page_executiondetail", "userAgent"), width: "220px", visible: false},
            {field: "exe.queueId", prop: "queueId", title: doc.getDocOnline("page_executiondetail", "queueId"),
             width: "120px", visible: false, className: "text-right tabular-nums"}
        ],

        actions: [
            {
                key: "view", icon: "eye", gate: "always",
                title: doc.getDocLabel("page_executiondetail", "viewExecution"),
                href: function (row) {
                    return "./TestCaseExecution.jsp?executionId=" + encodeURIComponent(row.id);
                }
            },
            {
                key: "editscript", icon: "file-pen-line", gate: "always",
                title: doc.getDocLabel("page_executiondetail", "edittc"),
                href: function (row) {
                    return "./TestCaseScript.jsp?test=" + encodeURIComponent(row.test) +
                        "&testcase=" + encodeURIComponent(row.testcase);
                }
            },
            {
                key: "lastexec", icon: "list-filter", gate: "always",
                title: doc.getDocLabel("page_executiondetail", "lastexecution"),
                href: function (row) {
                    return "./TestCaseExecutionList.jsp?Test=" + encodeURIComponent(row.test) +
                        "&TestCase=" + encodeURIComponent(row.testcase) +
                        "&country=" + encodeURIComponent(row.country) +
                        "&environment=" + encodeURIComponent(row.environment);
                }
            },
            {
                // V1 rendered this button only for executions that carry a tag.
                // Same rule, expressed as the gate it always was.
                key: "tag", icon: "tag",
                gate: function (row) { return !isEmpty(row.tag); },
                title: doc.getDocLabel("page_executiondetail", "see_execution_tag"),
                href: function (row) {
                    return "./ReportingExecutionByTag.jsp?Tag=" + encodeURIComponent(row.tag);
                }
            },
            {
                key: "run", icon: "play", gate: "always",
                title: doc.getDocLabel("page_executiondetail", "runtc"),
                href: function (row) {
                    return "./RunTests.jsp?test=" + encodeURIComponent(row.test) +
                        "&testcase=" + encodeURIComponent(row.testcase) +
                        "&country=" + encodeURIComponent(row.country) +
                        "&environment=" + encodeURIComponent(row.environment);
                }
            }
        ]
    });
}

/**
 * Deep-link filters, from the same four URL parameters V1 read.
 *
 * Keys are the SERVER column names, because that is what the component turns into
 * sSearch_<n> - not the data keys. 'ALL' is V1's "no filter" sentinel.
 */
function exeV2FiltersFromUrl() {
    var mapping = [
        {param: "Test", field: "exe.test"},
        {param: "TestCase", field: "exe.testcase"},
        {param: "country", field: "exe.country"},
        {param: "environment", field: "exe.environment"}
    ];
    var filters = {};
    mapping.forEach(function (m) {
        var value = GetURLParameter(m.param);
        if (value !== null && value !== "" && value !== "ALL" && value !== "null") {
            filters[m.field] = [value];
        }
    });
    return filters;
}

/** Charter status chip, linking to the execution detail. */
function exeV2StatusChip(row) {
    var status = row.controlStatus || "";
    if (!status) {
        return "";
    }
    var config = getExecutionStatusConfig(status);
    return '<a href="./TestCaseExecution.jsp?executionId=' + encodeURIComponent(row.id) + '" ' +
        'target="_blank" class="inline-flex no-underline hover:no-underline focus:no-underline" ' +
        'data-toggle="tooltip" data-html="true" title="' + crbTableEscape(exeV2Tooltip(row)) + '">' +
        '<span class="inline-flex items-center justify-center gap-1.5 rounded-full px-2.5 py-1 text-xs ' +
        'font-semibold ring-1 ring-inset transition-all duration-200 hover:scale-105 hover:shadow-sm ' +
        crbTableEscape(config.badgeClass) + '">' +
        '<i data-lucide="' + crbTableEscape(config.icon) + '" class="h-3.5 w-3.5 ' +
        crbTableEscape(config.iconClass) + '"></i><span>' + crbTableEscape(config.label) +
        '</span></span></a>';
}

/**
 * Tooltip body for the status chip.
 *
 * Guards controlMessage: V1 read .length on it unconditionally, so a single
 * execution without a control message threw inside the renderer and blanked the
 * cell. Values are escaped because the tooltip is injected as HTML.
 */
function exeV2Tooltip(row) {
    var message = row.controlMessage || "";
    if (message.length > 200) {
        message = message.substring(0, 200) + "...";
    }
    var line = function (label, value) {
        return "<div><span class='bold'>" + label + " : </span>" + crbTableEscape(value) + "</div>";
    };
    var html = line("Execution ID", row.id) + line("Country", row.country) + line("Environment", row.environment);
    if (row.robotDecli) {
        html += line("Robot", row.robotDecli + " (" + (row.browser || "") + ")");
    }
    html += line("Start", getDateMedium(row.start));
    if (getDateShort(row.end) !== "") {
        html += line("End", getDateShort(row.end));
    }
    html += "<div>" + crbTableEscape(message) + "</div>";
    return html;
}

/** Date cell. An unset date must show nothing, not "Invalid Date". */
function exeV2DateTime(value) {
    if (value === undefined || value === null || value === "" || value === 0) {
        return "";
    }
    var d = new Date(value);
    return isNaN(d.getTime()) ? "" : d.toLocaleString();
}

/** V1 showed `true` and left false/undefined blank; same here. */
function exeV2Bool(value) {
    return value === true ? "true" : "";
}

function displayPageLabel() {
    var doc = new Doc();

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
