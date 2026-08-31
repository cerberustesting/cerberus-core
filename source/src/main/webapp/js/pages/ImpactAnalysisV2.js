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
 * Impact Analysis - V2, on js/global/crbTable.js.
 *
 * What this page is for: you type a string and it lists every place in every
 * test case that mentions it - a header, a step, an action, a control, a
 * property. So the search box is the primary control, and the matched text is
 * highlighted in the columns that can contain it.
 *
 * Endpoint: api/testcases/objects (Spring @PostMapping, TestCasePrivateController).
 * It speaks the same legacy DataTables protocol as every servlet the component
 * already talks to, with two differences worth knowing:
 *   - it is POST-only, so the per-column filter needs distinctMethod:"POST" (a GET
 *     answers 405 and the filter list comes back silently empty);
 *   - `hasPermissions` is PER ROW (computed from that row's test case status),
 *     not one flag for the whole table - hence the row-level gate below.
 *
 * Action gating is a 1:1 port of ImpactAnalysis.js:127-176:
 *   - Open Script : always, a plain link
 *   - Edit/View   : always rendered; icon AND tooltip swap on the row's own
 *                   hasPermissions, exactly as V1 did
 *
 * Fixed on the way over (all verified against the live V1 page):
 *   - the Test pill called filterOnField(), which is defined in TestCaseList.js -
 *     a file this page never loaded. Every click threw ReferenceError and nothing
 *     happened. It now filters the table on that test.
 *   - the Edit handler was built as
 *     onclick="openModalTestCase('"+escapeHtml(obj.test)+"'...)". HTML-escaping
 *     does not protect a JS string inside an attribute: the entity is decoded and
 *     then evaluated, so a test named  x'); alert(1); //  ran. V2 passes the row
 *     itself to onClick, so no row value is ever interpolated into markup.
 *   - the dead switch on obj.object (both branches built the identical URL).
 *   - the "N results" badge, made redundant by the component's own count line.
 * ========================================================================== */

$.when($.getScript("js/global/global.js")).then(function () {
    $(document).ready(function () {
        initPage();
    });
});

var CRB_IA_TABLE_ID = "impactAnalysisTableV2";

function initPage() {
    var doc = new Doc();

    displayPageLabel();

    // The Edit action opens the shared TestCase modal, whose Description field is
    // a tinymce editor; V1 initialised it here and so must this page.
    tinymce.init({
        selector: ".wysiwyg",
        menubar: true,
        statusbar: false,
        toolbar: true,
        resize: true,
        height: 300,
        skin: 'oxide-dark'
    });

    createCerberusTable({
        id: CRB_IA_TABLE_ID,
        mount: "#impactAnalysisList",
        endpoint: "api/testcases/objects",
        distinctEndpoint: "api/testcases/objects",
        distinctMethod: "POST",
        dataProp: "contentTable",
        // A test case contributes many rows (one per step/action/control), so no
        // single field is unique; the coordinates together are.
        rowKey: function (row) {
            return JSON.stringify([row.test, row.testcase, row.object,
                                   row.stepId, row.actionId, row.controlId, row.property]);
        },
        defaultSort: {field: "test", dir: "asc"},
        pageLength: 10,
        // Naming the searched fields is not decoration: the server only matches the
        // free-text search against Value1-3, Condition Value1-3 and Description
        // (TestGenericObjectDAO). Typing "Google" while an Application column full
        // of "Google" is on screen returns nothing, which reads as a broken search
        // unless the scope is stated. Everything else is reachable through the
        // per-column filters.
        searchPlaceholder: "Search in values, conditions and descriptions...",
        emptyMessage: "No match in values, conditions or descriptions. " +
            "For a test, application or status, use the column filters instead.",

        // `field` = server column name (sorting/filtering), `prop` = key in the row.
        // They are identical on this endpoint, but stay explicit for consistency.
        //
        // Default visibility: V1 showed 27 of these at once, which is what made the
        // headers unreadable (they rendered as "O...", "TE...", "APP..."). The 14
        // shown by default are the ones that answer "where is my string used" -
        // location, what the step does, and the values it carries. Everything else
        // is one click away in Config, and the choice is persisted per user.
        columns: [
            {field: "object", title: doc.getDocOnline("page_impactAnalysis", "Object"),
             width: "90px", filterable: true, className: "font-medium"},
            {field: "test", title: doc.getDocOnline("test", "Test"), width: "190px",
             filterable: true,
             render: function (row, i) {
                 if (!row.test) {
                     return "";
                 }
                 // Addressed by row index through the shared helper: never build a
                 // handler out of a row value (see the note at the top of the file).
                 // crbTableCellButton escapes label and title itself - pass raw.
                 return crbTableCellButton(CRB_IA_TABLE_ID, "iaV2FilterOnTest", i,
                     row.test,
                     "inline-flex items-center px-3 py-1 border border-slate-300 dark:border-slate-600 " +
                     "rounded-xl text-sm font-medium text-slate-700 dark:text-slate-200 " +
                     "hover:border-blue-500 hover:text-blue-600 transition cursor-pointer bg-transparent",
                     "Filter on test " + row.test);
             }},
            {field: "testcase", title: doc.getDocOnline("testcase", "TestCase"),
             width: "90px", className: "font-mono"},
            {field: "application", title: doc.getDocOnline("application", "Application"),
             width: "110px", filterable: true},
            {field: "stepId", title: doc.getDocOnline("page_impactAnalysis", "StepId"),
             width: "70px", className: "text-right tabular-nums",
             render: function (row) { return iaV2Index(row.stepId); }},
            {field: "actionId", title: doc.getDocOnline("page_impactAnalysis", "ActionId"),
             width: "70px", className: "text-right tabular-nums",
             render: function (row) { return iaV2Index(row.actionId); }},
            {field: "controlId", title: doc.getDocOnline("page_impactAnalysis", "ControlId"),
             width: "80px", className: "text-right tabular-nums",
             render: function (row) { return iaV2Index(row.controlId); }},
            {field: "actionControl", title: doc.getDocOnline("page_impactAnalysis", "ActionControl"),
             width: "150px", filterable: true},
            {field: "property", title: doc.getDocOnline("page_impactAnalysis", "Property"),
             width: "130px"},
            {field: "value1", title: doc.getDocOnline("page_impactAnalysis", "Value1"),
             width: "220px", like: true,
             prop: "value1"},
            {field: "value2", title: doc.getDocOnline("page_impactAnalysis", "Value2"),
             width: "220px", like: true,
             prop: "value2"},
            {field: "value3", title: doc.getDocOnline("page_impactAnalysis", "Value3"),
             width: "220px", like: true,
             prop: "value3"},
            {field: "description", title: doc.getDocOnline("page_impactAnalysis", "Description"),
             width: "240px", like: true,
             prop: "description"},
            {field: "status", title: doc.getDocOnline("testcase", "Status"),
             width: "120px", filterable: true},

            // ---- available from Config, hidden by default ----
            {field: "active", title: doc.getDocOnline("testcase", "IsActive"),
             width: "80px", visible: false, filterable: true},
            {field: "system", title: doc.getDocOnline("application", "system"),
             width: "110px", visible: false, filterable: true},
            {field: "country", title: doc.getDocOnline("page_impactAnalysis", "Country"),
             width: "80px", visible: false, filterable: true},
            {field: "loop", title: doc.getDocOnline("testcasestep", "Loop"),
             width: "120px", visible: false, filterable: true},
            {field: "conditionOperator", title: doc.getDocOnline("testcase", "ConditionOperator"),
             width: "140px", visible: false, filterable: true},
            {field: "conditionValue1", title: doc.getDocOnline("testcase", "ConditionVal1"),
             width: "200px", visible: false, like: true,
             prop: "conditionValue1"},
            {field: "conditionValue2", title: doc.getDocOnline("testcase", "ConditionVal2"),
             width: "200px", visible: false, like: true,
             prop: "conditionValue2"},
            {field: "conditionValue3", title: doc.getDocOnline("testcase", "ConditionVal3"),
             width: "200px", visible: false, like: true,
             prop: "conditionValue3"},
            {field: "isFatal", title: doc.getDocOnline("page_executiondetail", "fatal"),
             width: "80px", visible: false},
            {field: "doScreenshotBefore",
             title: doc.getDocOnline("testcasestepactioncontrol", "DoScreenshotBefore"),
             width: "110px", visible: false},
            {field: "doScreenshotAfter",
             title: doc.getDocOnline("testcasestepactioncontrol", "DoScreenshotAfter"),
             width: "110px", visible: false},
            {field: "waitBefore", title: doc.getDocOnline("testcasestepactioncontrol", "WaitBefore"),
             width: "90px", visible: false, className: "text-right tabular-nums",
             render: function (row) { return iaV2Index(row.waitBefore); }},
            {field: "waitAfter", title: doc.getDocOnline("testcasestepactioncontrol", "WaitAfter"),
             width: "90px", visible: false, className: "text-right tabular-nums",
             render: function (row) { return iaV2Index(row.waitAfter); }},
            {field: "usrCreated", title: doc.getDocOnline("transversal", "UsrCreated"),
             width: "120px", visible: false},
            {field: "dateCreated", title: doc.getDocOnline("transversal", "DateCreated"),
             width: "150px", visible: false, like: true,
             render: function (row) { return crbTableEscape(getDate(row.dateCreated)); }},
            {field: "usrModif", title: doc.getDocOnline("transversal", "UsrModif"),
             width: "120px", visible: false},
            {field: "dateModif", title: doc.getDocOnline("transversal", "DateModif"),
             width: "150px", visible: false, like: true,
             render: function (row) { return crbTableEscape(getDate(row.dateModif)); }}
        ],

        actions: [
            {
                key: "script", icon: "file-text", gate: "always",
                title: doc.getDocLabel("page_impactAnalysis", "OpenScript"),
                href: function (row) {
                    var url = "TestCaseScript.jsp?test=" + encodeURIComponent(row.test) +
                        "&testcase=" + encodeURIComponent(row.testcase);
                    // -1 is the endpoint's "no step" marker (HEADER and PROPERTY
                    // rows); V1 put it in the URL anyway, which asked the script
                    // page to jump to a step that cannot exist.
                    if (row.stepId !== -1 && row.stepId !== undefined && row.stepId !== null && row.stepId !== "") {
                        url += "&stepId=" + encodeURIComponent(row.stepId);
                    }
                    return url;
                }
            },
            {
                // Rendered for everyone, like V1; only the icon and the tooltip
                // change. The permission is carried by the ROW here, not by the
                // table, because the server derives it from that test case's status.
                key: "edit", gate: "always",
                icon: function (row) { return row.hasPermissions ? "pencil" : "eye"; },
                title: function (row) {
                    return doc.getDocLabel("page_impactAnalysis",
                        row.hasPermissions ? "EditHeader" : "ViewHeader");
                },
                onClick: function (row) {
                    openModalTestCase(row.test, row.testcase, "EDIT");
                }
            }
        ]
    });
}

/** -1 is the endpoint's "not applicable" marker for step/action/control indexes. */
function iaV2Index(value) {
    return (value === -1 || value === undefined || value === null) ? "" : crbTableEscape(value);
}

/*
 * The search highlighting this page introduced now lives in crbTable.js and runs
 * for EVERY table (crbTableHighlight / crbTableHighlightHtml), so the seven
 * render() wrappers and the local iaV2Highlight() that used to sit here are gone:
 * these are plain `prop` columns again and the component marks them.
 *
 * Kept for the record, because it is why the feature exists: V1 did this with
 * textMatch()/formatedTextMatched(), which highlighted the WHOLE cell when it
 * contained the term. Marking the matched substring is what makes a 220px value
 * column readable - the point of this page is to show you where your string is,
 * so it should be pointed at, not merely flagged.
 */

/** Clicking a Test pill filters the list on that test (V1's dead filterOnField). */
function iaV2FilterOnTest(row, table) {
    if (!row.test) {
        return;
    }
    table.activeFilters["test"] = [row.test];
    table.start = 0;
    table.fetch();
}

function displayPageLabel() {
    var doc = new Doc();
    displayGlobalLabel(doc);
    $("#pageTitle").html(doc.getDocLabel("page_impactAnalysis", "title"));
    $("#title").html(doc.getDocOnline("page_impactAnalysis", "title"));
    displayFooter(doc);
}
