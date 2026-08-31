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
 * Test Case list - V2, on js/global/crbTable.js. TestCaseList.jsp (V1) is left
 * untouched and keeps working; this is a parallel page for comparison.
 *
 * This page is the stress test for the component: per-ROW permissions (not a
 * single table-wide flag), five action buttons with three different behaviours,
 * multi-row selection driving four mass actions, and a wide column set.
 *
 * PERMISSION BEHAVIOUR IS A DELIBERATE 1:1 PORT of TestCaseList.js:967-1078,
 * including the parts that look wrong:
 *   - Edit Script  : always clickable, icon only swaps pencil/eye  (line 1019)
 *   - Edit Header  : disabled when !hasPermissionsUpdate           (line 1036)
 *   - Duplicate    : always clickable, no permission check         (line 1040)
 *   - Delete       : always clickable, no permission check         (line 1049)
 *   - Run          : always clickable                              (line 1059)
 * The legacy code computes `hasPermissionsDelete` at line 971 and then never
 * uses it, so Delete is offered to everyone. That is reproduced here rather
 * than silently "fixed", because changing who can delete a test case is a
 * product decision, not a refactor. It is written as an explicit
 * `gate: "always"` with this comment attached, so the choice is visible instead
 * of being an omission - flip it to `gate: (row) => row.hasPermissionsDelete`
 * the day that decision is made.
 * ========================================================================== */

$.when($.getScript("js/global/global.js")).then(function () {
    $(document).ready(function () {
        initPage();
    });
});

var CRB_TC_TABLE_ID = "testCaseTableV2";

function initPage() {
    var doc = new Doc();

    displayPageLabel();

    // Same two lines as V1 (TestCaseList.js:35-36). openModalTestCase() would
    // lazily self-init on first use anyway, but doing it up front keeps the
    // first click as fast as before - TinyMCE and the label tree are heavy.
    // NOTE: this page must also load tinymce, bootstrap-treeview and
    // TestCaseSimpleExecution (see the <script> block in TestCaseList.jsp);
    // without them these modals throw and every action button silently fails.
    initModalTestCase();
    $('#editTestCaseModal').data("initLabel", true);

    createCerberusTable({
        id: CRB_TC_TABLE_ID,
        mount: "#testCaseList",
        endpoint: "ReadTestCase",
        distinctEndpoint: "ReadTestCase",
        // A test case has no single id column - it is keyed by (test, testcase).
        // JSON-encoding the pair avoids any separator character colliding with a
        // folder or case name that happens to contain it.
        rowKey: function (row) { return JSON.stringify([row.test, row.testcase]); },
        defaultSort: {field: "tec.test", dir: "asc"},
        pageLength: 10,
        lengthMenu: [10, 15, 20, 30, 50, 100, 500, 1000],
        searchPlaceholder: "Search test cases...",
        emptyMessage: "No test case matches your search",

        // Legacy renders a row checkbox only when that row's own
        // hasPermissionsUpdate is true (TestCaseList.js:954), which is what keeps
        // the mass actions scoped to rows the user may change.
        selection: {gate: function (row) { return Boolean(row.hasPermissionsUpdate); }},

        toolbar: function (ctx) {
            var html = "";
            var selecting = ctx.selectedCount > 0;

            // ReadTestCase sends `hasPermissionsCreate` (not the `hasPermissions`
            // most other endpoints use), so read it off the raw response - this is
            // the flag legacy gates its Create button on (TestCaseList.js:122).
            if (ctx.response.hasPermissionsCreate) {
                html += "<button id='createTestCaseButton' type='button' " +
                    "onclick=\"window.dispatchEvent(new CustomEvent('testcase-modal-open'))\" " +
                    "class='bg-sky-400 hover:bg-sky-500 text-white flex items-center gap-1 px-3 py-1 rounded-lg h-10'>" +
                    "<i data-lucide='plus' class='w-4 h-4'></i><span>" +
                    doc.getDocLabel("page_testcaselist", "btn_create") + "</span></button>";

                // Import and Test Folder are navigation away from the current list,
                // which is not what you want mid-selection - hidden while rows are
                // selected so the bar only offers what applies to that selection.
                if (!selecting) {
                    html += "<button id='importTestCaseButton' type='button' " +
                        "onclick=\"window.dispatchEvent(new CustomEvent('open-import-recording'))\" " +
                        "class='crb_table_iconbtn'>" +
                        "<i data-lucide='upload' class='w-4 h-4'></i><span>Import</span></button>";
                }
            }
            if (!selecting) {
                html += "<a href='./Test.jsp' class='crb_table_iconbtn'>" +
                    "<i data-lucide='folder' class='w-4 h-4'></i><span>Test Folder</span></a>";
            }

            // Mass-action buttons appear only once something is selected, mirroring
            // legacy's toggleDisplayMassButtons() (TestCaseList.js:303-310). The
            // "N selected" counter itself lives in the component's meta row, next
            // to the entry count.
            if (selecting) {
                html += massBtn("tcMassExport", "download", "Export", "tcV2MassExport()");
                html += massBtn("tcMassUpdate", "pencil", "Update", "tcV2MassUpdate()");
                html += massBtn("tcMassLabel", "tag", "Label", "tcV2MassLabel()");
                html += massBtn("tcMassDelete", "trash-2", "Delete", "tcV2MassDelete()", true);
            }
            return html;
        },

        // `field` = server column name (sorting/filtering), `prop` = key in the
        // returned row. On this endpoint they differ for every column.
        columns: [
            {
                field: "tec.test", prop: "test", title: "Test Folder", width: "180px", filterable: true,
                // Addresses the row by index via the shared helper. Interpolating
                // row.test into the handler instead would be exploitable: HTML
                // escaping does not survive an onclick attribute, so a folder named
                //   x'); anything(); //
                // would execute. See crbTableCellButton() in crbTable.js.
                render: function (row, i) {
                    return crbTableCellButton(CRB_TC_TABLE_ID, "tcV2FilterOnFolder", i,
                        row.test, "crb_tc_pill", "Filter on this folder");
                }
            },
            {field: "tec.testcase", prop: "testcase", title: "Testcase", width: "110px",
             className: "font-mono", like: true, filterable: true},
            {field: "tec.description", prop: "description", title: "Description",
             width: "300px", like: true},
            {
                // Only STICKER-type labels, matching legacy's labelsSTICKER column.
                // `field` names the server-side column so the value filter works
                // (verified: ReadTestCase returns distinct sticker names for it and
                // narrows correctly on sSearch_N); `prop` is unused because the cell
                // is rendered from the row's `labels` array instead of one value.
                // Not sortable - the server cannot order by it, same as legacy.
                field: "lab.labelsSTICKER", title: "Stickers", sortable: false,
                filterable: true, width: "180px",
                render: function (row) { return tcV2Labels(row, "STICKER"); }
            },
            {
                field: "tec.status", prop: "status", title: "Status", width: "130px", filterable: true,
                render: function (row) { return tcV2StatusChip(row.status); }
            },
            {field: "tec.application", prop: "application", title: "Application",
             width: "130px", filterable: true},
            {field: "app.system", prop: "system", title: "System",
             width: "110px", visible: false, filterable: true},
            {
                field: "tec.isActive", prop: "isActive", title: "Active", width: "100px", filterable: true,
                render: function (row) {
                    return row.isActive
                        ? '<span class="crb_tc_chip crb_tc_chip--on">Active</span>'
                        : '<span class="crb_tc_chip crb_tc_chip--off">Inactive</span>';
                }
            },
            {field: "tec.priority", prop: "priority", title: "Priority", width: "90px",
             visible: false, filterable: true},
            {field: "tec.type", prop: "type", title: "Type", width: "110px",
             visible: false, filterable: true},
            {field: "tec.usrCreated", prop: "usrCreated", title: "Created by", width: "120px", visible: false},
            {
                field: "tec.dateCreated", prop: "dateCreated", title: "Created", width: "150px",
                visible: false, like: true,
                render: function (row) { return crbTableEscape(getDate(row.dateCreated)); }
            },
            {field: "tec.usrModif", prop: "usrModif", title: "Modified by", width: "120px", visible: false},
            {
                field: "tec.dateModif", prop: "dateModif", title: "Modified", width: "150px",
                visible: false, like: true,
                render: function (row) { return crbTableEscape(getDate(row.dateModif)); }
            }
        ],

        actions: [
            {
                // A real link: keeps middle-click / open-in-new-tab working.
                key: "editscript", gate: "always",
                href: function (row) {
                    return "TestCaseScript.jsp?test=" + encodeURIComponent(row.test) +
                        "&testcase=" + encodeURIComponent(row.testcase);
                },
                icon: function (row) { return row.hasPermissionsUpdate ? "pencil" : "eye"; },
                title: function (row) {
                    return row.hasPermissionsUpdate
                        ? doc.getDocLabel("page_testcaselist", "btn_editScript")
                        : doc.getDocLabel("page_testcaselist", "btn_view");
                }
            },
            {
                key: "editheader", icon: "file-text", gate: "always",
                disabled: function (row) { return !row.hasPermissionsUpdate; },
                title: function (row) {
                    return row.hasPermissionsUpdate
                        ? doc.getDocLabel("page_testcaselist", "btn_edit")
                        : doc.getDocLabel("page_testcaselist", "btn_view");
                },
                onClick: function (row) {
                    openModalTestCase(row.test, row.testcase, row.hasPermissionsUpdate ? "EDIT" : "VIEW");
                }
            },
            {
                // Legacy: no permission check at all on duplicate. Ported as-is.
                key: "duplicate", icon: "copy", gate: "always",
                title: doc.getDocLabel("page_testcaselist", "btn_duplicate"),
                onClick: function (row) { openModalTestCase(row.test, row.testcase, "DUPLICATE"); }
            },
            {
                // Legacy computes hasPermissionsDelete then never reads it, so every
                // user gets a working Delete button. Kept identical on purpose.
                key: "delete", icon: "trash-2", gate: "always", danger: true,
                title: doc.getDocLabel("page_testcaselist", "btn_delete"),
                onClick: function (row) { tcV2Delete(row.test, row.testcase); }
            },
            {
                key: "run", icon: "play", gate: "always",
                title: doc.getDocLabel("page_testcaselist", "btn_runTest"),
                onClick: function (row) {
                    window.dispatchEvent(new CustomEvent("open-execution", {
                        detail: {
                            application: row.application,
                            test: row.test,
                            testcase: row.testcase,
                            description: row.description
                        }
                    }));
                }
            }
        ]
    });
}

function displayPageLabel() {
    var doc = new Doc();
    displayGlobalLabel(doc);
    $("#pageTitle").html(doc.getDocLabel("page_testcaselist", "title"));
    $("#title").html(doc.getDocOnline("page_testcaselist", "title"));
    displayFooter(doc);
}

function massBtn(id, icon, label, onClick, danger) {
    return "<button id='" + id + "' type='button' onclick=\"" + onClick + "\" " +
        "class='crb_table_iconbtn" + (danger ? " crb_table_iconbtn--danger" : "") + "'>" +
        "<i data-lucide='" + icon + "' class='w-4 h-4'></i><span>" + label + "</span></button>";
}

/** Coloured label pills, filtered to one label type. */
function tcV2Labels(row, type) {
    var labels = (row.labels || []).filter(function (l) { return l.type === type; });
    if (!labels.length) {
        return "";
    }
    return '<div class="flex flex-wrap gap-1">' + labels.map(function (l) {
        // Ink computed from the colour, not taken from l.fontColor: several
        // endpoints leave that field null and the old `|| "#000"` fallback painted
        // black text on black labels. See crbFontColorFor in js/global/global.js.
        return '<span class="crb_tc_label" style="' + crbChipStyle(l.color) + '">' +
            crbTableEscape(l.label) + '</span>';
    }).join("") + '</div>';
}

/** Charter status chip. */
function tcV2StatusChip(status) {
    if (!status) {
        return "";
    }
    var tone = "crb_tc_chip--neutral";
    if (status === "WORKING") { tone = "crb_tc_chip--on"; }
    else if (status === "STANDBY") { tone = "crb_tc_chip--warn"; }
    else if (status === "IN PROGRESS") { tone = "crb_tc_chip--info"; }
    return '<span class="crb_tc_chip ' + tone + '">' + crbTableEscape(status) + '</span>';
}

/**
 * Clicking a Test-folder pill filters the table on it (legacy filterOnField
 * equivalent). Receives the row object from crbTableCellCallback, so the folder
 * name never travels through an HTML attribute.
 */
function tcV2FilterOnFolder(row, table) {
    table.activeFilters["tec.test"] = [row.test];
    table.start = 0;
    table.fetch();
}

async function tcV2Delete(test, testcase) {
    clearResponseMessageMainPage();
    var doc = new Doc();
    var msg = doc.getDocLabel("page_global", "message_delete")
        .replace("%TABLE%", "Test Case").replace("%ENTRY%", test + " / " + testcase);

    const result = await crbConfirmDelete({
        title: doc.getDocLabel("page_testcaselist", "btn_delete"),
        html: msg,
        confirmText: doc.getDocLabel("page_global", "btn_delete") || "Delete",
        cancelText: doc.getDocLabel("page_global", "buttonClose") || "Cancel",
        preConfirm: async () => {
            const resp = await fetch("DeleteTestCase?test=" + encodeURIComponent(test) +
                "&testCase=" + encodeURIComponent(testcase));
            const data = await resp.json();
            if (getAlertType(data.messageType) !== "success") {
                Swal.showValidationMessage(data.message || "Delete failed");
                return null;
            }
            return data;
        }
    });

    if (result.isConfirmed && result.value) {
        var t = crbTableInstance(CRB_TC_TABLE_ID);
        if (t) {
            if (t.rows.length === 1 && t.page > 1) { t.goToPage(t.page - 1); } else { t.reload(); }
        }
        showMessageMainPage("success", result.value.message || "Test case deleted", false);
    }
}

/* -----------------------------------------------------------------------------
 * Mass actions. Legacy builds its payload by serialising a wrapping <form> of
 * checkboxes and regex-rewriting the field names
 * (TestCaseList.js:317 - `.replace(/test-/g,'test=')`); here the component hands
 * back the selected row objects directly, so the same servlets are called with
 * the same parameters without that string surgery.
 * -------------------------------------------------------------------------- */
function tcV2Selected() {
    var t = crbTableInstance(CRB_TC_TABLE_ID);
    return t ? t.selectedRows : [];
}

function tcV2MassExport() {
    // One download per selected test case, exactly as legacy does
    // (TestCaseList.js:419-436) - there is no combined-export endpoint.
    tcV2Selected().forEach(function (row) {
        var a = document.createElement("a");
        a.href = "./ExportTestCase?test=" + encodeURIComponent(row.test) +
            "&testcase=" + encodeURIComponent(row.testcase);
        a.download = "";
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
    });
}

function tcV2MassUpdate() {
    window.dispatchEvent(new CustomEvent("mass-update-open", {detail: {rows: tcV2Selected()}}));
}

function tcV2MassLabel() {
    window.dispatchEvent(new CustomEvent("mass-label-open", {detail: {rows: tcV2Selected()}}));
}

/**
 * Serialises the current selection the way the mass-action servlets expect:
 * repeated test=<folder>&testcase=<id> pairs.
 *
 * V1 got this string by serialising a <form> wrapping every row checkbox and
 * then regex-rewriting the field names (TestCaseList.js:317). The V2 table has
 * no such form - selection lives in the component - so the pairs are built
 * directly, which also removes that page's dependency on checkbox name encoding.
 */
function tcV2SelectionParams() {
    return tcV2Selected().map(function (row) {
        return "test=" + encodeURIComponent(row.test) + "&testcase=" + encodeURIComponent(row.testcase);
    }).join("&");
}

/**
 * Label ids ticked in the three pickers of the mass-label modal.
 *
 * loadLabelSelectedIds() lives in the shared include that BUILDS those pickers
 * (include/transversal/TestCase.html), so the id-to-container mapping is stated
 * once instead of being re-derived at each of the four read sites that used to
 * copy the same three treeview('getSelected') calls.
 */
function tcV2SelectedLabelIds() {
    return loadLabelSelectedIds("#selectLabelAdd").map(function (id) {
        return "labelid=" + encodeURIComponent(id);
    }).join("&");
}

/**
 * Add/remove labels on every selected test case.
 *
 * Ported from massActionModalSaveHandler_addLabel / _removeLabel in
 * TestCaseList.js:312-384 - the modal markup calls these two names directly
 * (TestCaseListMassActionLabel.html:123,128), so they must exist globally on
 * this page too; the V1 file that defined them is deliberately not loaded here.
 */
function tcV2MassLabelSubmit(servlet) {
    // Every #massActionTestCaseModal reference the V1 version carried is dead:
    // that Bootstrap modal was replaced by the Alpine one in
    // TestCaseListMassActionLabel.html, whose root id is #massActionLabelModal and
    // which has no <form> at all. So the loader never appeared, and on a REJECTED
    // save showMessage() rendered into a jQuery set of zero elements - the user got
    // no error whatsoever. The message now goes to the main page, which is where it
    // has to go anyway: the modal's add()/remove() call close() immediately after
    // this function, so it is already hidden by the time the response lands.
    var paramSerialized = tcV2SelectionParams();
    var labelIds = tcV2SelectedLabelIds();
    if (labelIds) {
        paramSerialized += "&" + labelIds;
    }

    $.when($.post(servlet, paramSerialized, "json")).then(function (data) {
        var type = getAlertType(data.messageType);
        if (type === "success" || type === "warning") {
            var t = crbTableInstance(CRB_TC_TABLE_ID);
            if (t) {
                t.clearSelection();
                t.reload();
            }
        }
        showMessage(data);
    }).fail(handleErrorAjaxAfterTimeout);
}

function massActionModalSaveHandler_addLabel() {
    tcV2MassLabelSubmit("CreateTestCaseLabel");
}

function massActionModalSaveHandler_removeLabel() {
    tcV2MassLabelSubmit("DeleteTestCaseLabel");
}

async function tcV2MassDelete() {
    var rows = tcV2Selected();
    if (!rows.length) {
        return;
    }
    var doc = new Doc();
    const result = await crbConfirmDelete({
        title: "Delete " + rows.length + " test case(s)",
        html: "This action cannot be reverted.",
        confirmText: doc.getDocLabel("page_global", "btn_delete") || "Delete",
        cancelText: doc.getDocLabel("page_global", "buttonClose") || "Cancel"
    });
    if (!result.isConfirmed) {
        return;
    }

    var results = await Promise.all(rows.map(function (row) {
        return fetch("DeleteTestCase?test=" + encodeURIComponent(row.test) +
            "&testCase=" + encodeURIComponent(row.testcase)).then(function (r) { return r.json(); });
    }));
    var failed = results.filter(function (d) { return getAlertType(d.messageType) !== "success"; });

    var t = crbTableInstance(CRB_TC_TABLE_ID);
    if (t) {
        t.clearSelection();
        t.reload();
    }
    if (failed.length) {
        showMessageMainPage("warning", failed.length + " of " + rows.length + " could not be deleted", false);
    } else {
        showMessageMainPage("success", rows.length + " test case(s) deleted", false);
    }
}
