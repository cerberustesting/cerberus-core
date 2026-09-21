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
 * Data Library list - V2, on js/global/crbTable.js.
 *
 * Action gating is a 1:1 port of TestDataLibList.js:404-474, which uses the
 * table-wide `hasPermissions` flag:
 *   - Edit/View  : always rendered; icon swaps pencil/eye, same handler either
 *                  way (the shared modal enforces read-only itself)
 *   - Duplicate  : only when hasPermissions
 *   - Delete     : only when hasPermissions
 *   - Test Cases : always rendered, no gate
 * Toolbar: Create is always rendered but `disabled` without permission (legacy
 * line 81), Bulk Rename is omitted entirely without it (line 96).
 *
 * The heavy lifting - the Add/Edit/Duplicate modal, delete confirmation, the
 * "test cases using this" panel and bulk rename - stays in the V1 file and the
 * shared include/transversal/TestDataLib.html, which this page still loads. Only
 * the table itself is reimplemented, so those flows are untouched.
 * ========================================================================== */

$.when($.getScript("js/global/global.js")).then(function () {
    $(document).ready(function () {
        initPage();
    });
});

var CRB_TDL_TABLE_ID = "testDataLibTableV2";

function initPage() {
    var doc = new Doc();

    displayPageLabel();

    // Same page-level wiring as V1 (TestDataLibList.js:33-42). These are what the
    // modals and the "test cases using" panel hook onto; dropping any of them
    // leaves a button that opens nothing.
    window.addEventListener('testdatalib-modal-close', function () {
        buttonCloseHandler({data: {extra: "#editTestLibData"}});
    });
    $('#testCaseListModal').on('hidden.bs.modal', getTestCasesUsingModalCloseHandler);
    displayApplicationList("application", "", "", "");
    initModalDataLib();

    createCerberusTable({
        id: CRB_TDL_TABLE_ID,
        mount: "#testDataLibList",
        endpoint: "ReadTestDataLib",
        distinctEndpoint: "ReadTestDataLib",
        rowKey: "testDataLibID",
        defaultSort: {field: "tdl.Name", dir: "asc"},
        pageLength: 10,
        searchPlaceholder: "Search data libraries...",
        emptyMessage: "No data library matches your search",

        toolbar: function (ctx) {
            var html = "";
            // Legacy always renders Create and only disables it (line 81), rather
            // than hiding it like most other pages - kept identical.
            // Passing the page name matters: the shared modal only refreshes the
            // list inside `if (page === "TestDataLibList")`. V1 called this with
            // no page for ADD (TestDataLibList.js:118), which is why creating an
            // entry never showed up until a manual reload. Fixed here.
            html += "<button id='createLibButton' type='button'" +
                (ctx.hasPermissions
                    ? " onclick=\"openModalDataLib(null, null, 'ADD', 'TestDataLibList', null)\""
                    : " disabled") +
                " class='bg-sky-400 hover:bg-sky-500 text-white flex items-center gap-1 px-3 py-1 rounded-lg h-10" +
                (ctx.hasPermissions ? "" : " opacity-40 cursor-not-allowed") + "'>" +
                "<i data-lucide='plus' class='w-4 h-4'></i><span>" +
                doc.getDocLabel("page_testdatalib", "btn_create") + "</span></button>";

            if (ctx.hasPermissions) {
                html += "<button id='bulkRenameButton' type='button' onclick='openModalDataLibBulk()' " +
                    "class='crb_table_iconbtn'>" +
                    "<i data-lucide='copy' class='w-4 h-4'></i><span>" +
                    doc.getDocLabel("page_testdatalib", "btn_bulkrename") + "</span></button>";
            }
            return html;
        },

        // `field` = server column name (sorting/filtering), `prop` = key in the row.
        columns: [
            {field: "tdl.TestDataLibID", prop: "testDataLibID", title: "ID", width: "70px",
             like: true, className: "font-mono"},
            {field: "tdl.Name", prop: "name", title: "Name", width: "200px",
             className: "font-medium", filterable: true},
            {field: "tdl.System", prop: "system", title: "System", width: "90px", filterable: true},
            {field: "tdl.Environment", prop: "environment", title: "Environment",
             width: "100px", visible: false, filterable: true},
            {field: "tdl.Country", prop: "country", title: "Country",
             width: "90px", visible: false, filterable: true},
            {field: "tdl.Group", prop: "group", title: "Group",
             width: "110px", visible: false, like: true, filterable: true},
            {field: "tdl.Description", prop: "description", title: "Description",
             width: "220px", like: true},
            {
                field: "tdl.Type", prop: "type", title: "Type", width: "110px", filterable: true,
                render: function (row) { return tdlV2TypeCell(row.type); }
            },
            {field: "tdd.value", prop: "subDataValue", title: "Value", width: "160px",
             like: true, sortable: false},
            {field: "tdl.Database", prop: "database", title: "Database",
             width: "110px", visible: false, filterable: true},
            {
                field: "tdl.Script", prop: "script", title: "Script", width: "300px", visible: false,
                render: function (row) { return tdlV2Code(row.script); }
            },
            {field: "tdl.DatabaseUrl", prop: "databaseUrl", title: "Database URL",
             width: "150px", visible: false},
            {field: "tdl.Service", prop: "service", title: "Service", width: "150px", visible: false},
            {field: "tdl.ServicePath", prop: "servicePath", title: "Service Path",
             width: "150px", visible: false, like: true},
            {field: "tdl.method", prop: "method", title: "Method",
             width: "110px", visible: false, like: true, filterable: true},
            {
                field: "tdl.envelope", prop: "envelope", title: "Envelope",
                width: "300px", visible: false, like: true,
                render: function (row) { return tdlV2Code(row.envelope); }
            },
            {field: "tdl.DatabaseCsv", prop: "databaseCsv", title: "Database CSV",
             width: "130px", visible: false},
            {field: "tdl.csvUrl", prop: "csvUrl", title: "CSV URL",
             width: "150px", visible: false, like: true},
            {field: "tdl.separator", prop: "separator", title: "Separator",
             width: "90px", visible: false},
            {
                field: "tdl.Created", prop: "created", title: "Created",
                width: "150px", visible: false, like: true,
                render: function (row) { return crbTableEscape(getDate(row.created)); }
            },
            {field: "tdl.Creator", prop: "creator", title: "Creator", width: "120px", visible: false},
            {
                field: "tdl.LastModified", prop: "lastModified", title: "Modified",
                width: "150px", visible: false, like: true,
                render: function (row) { return crbTableEscape(getDate(row.lastModified)); }
            },
            {field: "tdl.LastModifier", prop: "lastModifier", title: "Modified by",
             width: "120px", visible: false},
            {field: "tdd.column", prop: "subDataColumn", title: "Sub Column",
             width: "110px", visible: false, like: true, sortable: false},
            {field: "tdd.ParsingAnswer", prop: "subDataParsingAnswer", title: "Parsing Answer",
             width: "130px", visible: false, like: true, sortable: false},
            {field: "tdd.ColumnPosition", prop: "subDataColumnPosition", title: "Column Position",
             width: "130px", visible: false, sortable: false}
        ],

        actions: [
            {
                // Always shown; only the icon differs. Legacy keeps the same
                // tooltip key in both states (line 436) - kept, so nothing changes
                // for users relying on it.
                key: "edit", gate: "always",
                icon: function (row, ctx) { return ctx.hasPermissions ? "pencil" : "eye"; },
                title: doc.getDocLabel("page_testdatalib", "tooltip_editentry"),
                onClick: function (row) {
                    openModalDataLib(row.testDataLibID, row.name, "EDIT", "TestDataLibList", null);
                }
            },
            {
                key: "duplicate", icon: "copy", gate: "permission",
                title: doc.getDocLabel("page_testdatalib", "tooltip_duplicateEntry"),
                onClick: function (row) {
                    openModalDataLib(row.testDataLibID, row.name, "DUPLICATE", "TestDataLibList", null);
                }
            },
            {
                key: "delete", icon: "trash-2", gate: "permission", danger: true,
                title: doc.getDocLabel("page_testdatalib", "tooltip_delete"),
                onClick: function (row) {
                    deleteTestDataLibClick(row.testDataLibID, row.name, row.system,
                        row.environment, row.country, row.type);
                }
            },
            {
                key: "testcases", icon: "list", gate: "always",
                title: doc.getDocLabel("page_testdatalib", "tooltip_gettestcases"),
                onClick: function (row) {
                    getTestCasesUsing(row.testDataLibID, row.name, row.country);
                }
            }
        ]
    });

    refreshPopoverDocumentation("testDataLibList");
}

function displayPageLabel() {
    var doc = new Doc();
    displayGlobalLabel(doc);
    $("#pageTitle").html(doc.getDocLabel("page_testdatalib", "page_title"));
    $("#title").html(doc.getDocOnline("page_testdatalib", "title"));
    displayFooter(doc);
}

/** Type cell: icon + label, same four cases as V1 (TestDataLibList.js:529-539). */
function tdlV2TypeCell(type) {
    if (!type) {
        return "";
    }
    var icon;
    if (type === "SQL") {
        icon = '<i data-lucide="table" class="w-4 h-4"></i>';
    } else if (type === "FILE") {
        icon = '<i data-lucide="file" class="w-4 h-4"></i>';
    } else if (type === "INTERNAL") {
        icon = '<img src="images/Logo-cerberus_250.png" width="16" height="16" alt="INTERNAL"/>';
    } else {
        icon = '<i data-lucide="cloud-upload" class="w-4 h-4"></i>';
    }
    return '<span class="inline-flex items-center gap-1.5 text-slate-600 dark:text-slate-300">' +
        icon + '<span class="text-xs font-medium">' + crbTableEscape(type) + '</span></span>';
}

/** One-line clipped preview for script / envelope, as V1 did with a <pre>. */
function tdlV2Code(value) {
    if (!value) {
        return "";
    }
    return '<pre class="crb_tdl_code" title="' + crbTableEscape(value) + '">' +
        crbTableEscape(value) + '</pre>';
}

/* =============================================================================
 * Modal / flow helpers copied from TestDataLibList.js.
 *
 * They are duplicated rather than reused because loading the V1 file alongside
 * this one would register its `initPage()` too, and since both files bind the
 * same document-ready hook the page would initialise twice (two tables, doubled
 * listeners). The V1 file therefore stays untouched and unloaded here.
 *
 * Only the two table-refresh call sites differ from the originals: they drove
 * DataTables through $("#listOfTestDataLib").dataTable(); here they go through
 * the component instance. Everything else is verbatim so these flows behave
 * exactly as before.
 * ========================================================================== */

function openModalDataLibBulk() {
    clearResponseMessageMainPage();

    $('[data-toggle="popover"]').popover({
        'placement': 'auto',
        'z-index': 1060,
        'container': 'body'
    });

    var doc = new Doc();

    if ($('#bulkRenameDataLibModal').data("initLabel") === undefined) {
        $("[name='lbl_currentname']").html(doc.getDocOnline("testdatalib", "currentname"));
        $("[name='lbl_newname']").html(doc.getDocOnline("testdatalib", "newname"));
        $("#bulkRenameValidate").text(doc.getDocLabel("page_global", "btn_bulkrename"));
        $('#bulkRenameDataLibModal').data("initLabel", true);
    }

    $("#bulkRenameValidate").off("click");
    $('#bulkRenameValidate').click(function () {
        confirmDataLibBulkModalHandler();
    });

    window.dispatchEvent(new CustomEvent('bulkrename-modal-open'));
    setTimeout(function () { if (window.lucide) lucide.createIcons(); }, 100);
}

/** Bulk-rename field validation: not blank, no white space. */
function isBlankOrContainsWhiteSpace(str) {
    var regex1 = /^\s*$/;
    var regex2 = /^(.*\s+.*)+$/;
    if (!str || regex1.test(str)) {
        return true;
    } else if (regex2.test(str)) {
        return true;
    }
    return false;
}

function confirmDataLibBulkModalHandler() {
    var bool = false;
    var old_name = $('#dl_currentname').val();
    var new_name = $('#dl_newname').val();
    var formEdit = $('#bulkRenameDataLibModal');
    var old_nameElement = formEdit.find("#dl_currentname");
    var new_nameElement = formEdit.find("#dl_newname");
    var myServlet;

    if (old_nameElement.parents("div.form-group").hasClass("has-error")) {
        old_nameElement.parents("div.form-group").removeClass("has-error");
    }
    if (new_nameElement.parents("div.form-group").hasClass("has-error")) {
        new_nameElement.parents("div.form-group").removeClass("has-error");
    }

    if (!isBlankOrContainsWhiteSpace(old_name)) {
        if (!isBlankOrContainsWhiteSpace(new_name)) {
            myServlet = "BulkRenameDataLib";
            bool = true;
        } else {
            new_nameElement.parents("div.form-group").addClass("has-error");
            showMessage(new Message("danger", new Doc().getDocLabel("page_testdatalib", "wrong_name_message")),
                $('#bulkRenameDataLibModal'));
        }
    } else {
        old_nameElement.parents("div.form-group").addClass("has-error");
        showMessage(new Message("danger", new Doc().getDocLabel("page_testdatalib", "wrong_name_message")),
            $('#bulkRenameDataLibModal'));
    }

    if (bool) {
        $.ajax({
            async: true,
            url: myServlet,
            method: "GET",
            data: 'oldname=' + old_name + '&newname=' + new_name,
            success: function (data) {
                hideLoaderInModal('#bulkRenameDataLibModal');
                if (getAlertType(data.messageType) === "success") {
                    window.dispatchEvent(new CustomEvent('bulkrename-modal-close'));
                    var t = crbTableInstance(CRB_TDL_TABLE_ID);   // was: fnDraw on the DataTable
                    if (t) {
                        t.reload();
                    }
                    showMessage(data);
                } else {
                    showMessage(data, $('#bulkRenameDataLibModal'));
                }
            },
            error: showUnexpectedError
        });
    }
}

async function deleteTestDataLibClick(testDataLibID, name, system, environment, country, type) {
    var doc = new Doc();

    var systemLabel = system === '' ? doc.getDocLabel("page_global", "lbl_all") : system;
    var environmentLabel = environment === '' ? doc.getDocLabel("page_global", "lbl_all") : environment;
    var countryLabel = country === '' ? doc.getDocLabel("page_global", "lbl_all") : country;

    var messageComplete = doc.getDocLabel("page_testdatalib", "message_delete")
        .replace("%ENTRY%", name)
        .replace("%ID%", testDataLibID)
        .replace("%SYSTEM%", systemLabel)
        .replace("%ENVIRONMENT%", environmentLabel)
        .replace("%COUNTRY%", countryLabel);

    const result = await crbConfirmDelete({
        title: doc.getDocLabel("page_testdatalib_delete", "title"),
        html: messageComplete,
        confirmText: doc.getDocLabel("page_global", "btn_delete") || 'Delete',
        cancelText: doc.getDocLabel("page_global", "buttonClose") || 'Cancel',
        preConfirm: async () => {
            try {
                const resp = await fetch("DeleteTestDataLib", {
                    method: "POST",
                    headers: {"Content-Type": "application/x-www-form-urlencoded"},
                    body: "testdatalibid=" + testDataLibID
                });
                const data = await resp.json();
                if (getAlertType(data.messageType) !== "success") {
                    Swal.showValidationMessage(data.message || "Delete failed");
                    return null;
                }
                return data;
            } catch (e) {
                Swal.showValidationMessage("Unexpected error");
                return null;
            }
        }
    });

    if (result.isConfirmed && result.value) {
        // was: fnDraw + fnPageChange('previous') when the last row of a page went
        var t = crbTableInstance(CRB_TDL_TABLE_ID);
        if (t) {
            if (t.rows.length === 1 && t.page > 1) {
                t.goToPage(t.page - 1);
            } else {
                t.reload();
            }
        }
        notifyInPage("success", result.value.message || "Data Library deleted successfully");
    }
}

/** Loads and renders the "test cases using this entry" accordion. Verbatim from V1. */
function getTestCasesUsing(testDataLibID, name, country) {
    clearResponseMessageMainPage();
    showLoaderInModal('#testCaseListModal');
    var jqxhr = $.getJSON("ReadTestDataLib", "testdatalibid=" + testDataLibID + "&name=" + name + "&country=" + country);

    var doc = new Doc();

    $.when(jqxhr).then(function (result) {

        var count = result["TestCasesList"].length;
        $('#testCaseListModal #totalTestCases').text('#tests: ' + count);
        var htmlContent = "";

        $.each(result["TestCasesList"], function (idx, obj) {

            htmlContent += `<div x-data="{open:false}" class="rounded-xl border border-slate-200 dark:border-slate-700 overflow-hidden transition-all duration-200" :class="open ? 'shadow-sm' : ''">
                <button @click="open=!open" type="button"
                    class="w-full flex items-center justify-between px-4 py-3 text-left transition-colors duration-150"
                    :class="open ? 'bg-slate-50 dark:bg-slate-800/50' : 'hover:bg-slate-50 dark:hover:bg-slate-800/30'">
                    <div class="flex items-center gap-3 min-w-0">
                        <div class="h-8 w-8 rounded-lg flex items-center justify-center shrink-0"
                             style="background: color-mix(in srgb, var(--crb-blue-color) 10%, transparent)">
                            <svg class="w-4 h-4" style="color: var(--crb-blue-color)" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path d="M3 7v10a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-6l-2-2H5a2 2 0 00-2 2z"/></svg>
                        </div>
                        <span class="text-sm font-semibold text-slate-800 dark:text-slate-200 truncate">${obj[0]}</span>
                    </div>
                    <div class="flex items-center gap-3 shrink-0">
                        <span class="text-xs font-medium px-2 py-0.5 rounded-full"
                              style="background: color-mix(in srgb, var(--crb-blue-color) 10%, transparent); color: var(--crb-blue-color)">#test cases:${obj[2]}</span>
                        <svg class="w-4 h-4 text-slate-400 transition-transform duration-200" :class="{'rotate-180': open}" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path d="M6 9l6 6 6-6"/></svg>
                    </div>
                </button>
                <div x-show="open" x-collapse>
                    <div class="border-t border-slate-100 dark:border-slate-700">`;

            $.each(obj[3], function (idx2, obj2) {
                var hrefTest = 'TestCaseScript.jsp?test=' + obj[0] + '&testcase=' + obj2.TestCaseNumber;
                var statusColor = obj2.Status === 'WORKING' ? 'bg-emerald-100 text-emerald-700 dark:bg-emerald-900/30 dark:text-emerald-400' :
                                  obj2.Status === 'IN PROGRESS' ? 'bg-amber-100 text-amber-700 dark:bg-amber-900/30 dark:text-amber-400' :
                                  'bg-slate-100 text-slate-600 dark:bg-slate-700 dark:text-slate-400';
                var activeColor = (obj2.Active === 'Y' || obj2.Active === '1') ? 'bg-emerald-500' : 'bg-slate-300 dark:bg-slate-600';

                htmlContent += `
                    <div class="flex items-start gap-3 px-4 py-3 hover:bg-slate-50/80 dark:hover:bg-slate-800/40 transition-colors ${idx2 > 0 ? 'border-t border-slate-100 dark:border-slate-800' : ''}" style="padding-left: 3.5rem;">
                        <div class="w-2 h-2 rounded-full mt-2 shrink-0 ${activeColor}"></div>
                        <div class="flex-1 min-w-0">
                            <div class="flex items-center gap-2 flex-wrap">
                                <a href="${hrefTest}" target="_blank"
                                   class="text-sm font-medium hover:underline truncate" style="color: var(--crb-blue-color)">
                                    ${obj2.TestCaseNumber} — ${obj2.TestCaseDescription}
                                </a>
                                <span class="text-[10px] font-medium px-1.5 py-0.5 rounded ${statusColor}">${obj2.Status}</span>
                            </div>
                            <div class="flex items-center gap-3 mt-1 text-xs text-slate-500 dark:text-slate-400 flex-wrap">
                                <span>${doc.getDocLabel("testcase", "Creator")}: <strong class="text-slate-600 dark:text-slate-300">${obj2.Creator}</strong></span>
                                <span class="text-slate-300 dark:text-slate-600">·</span>
                                <span>${doc.getDocLabel("invariant", "TESTCASE_TYPE")}: <strong class="text-slate-600 dark:text-slate-300">${obj2.Group}</strong></span>
                                <span class="text-slate-300 dark:text-slate-600">·</span>
                                <span>${doc.getDocLabel("application", "Application")}: <strong class="text-slate-600 dark:text-slate-300">${obj2.Application}</strong></span>
                            </div>
                        </div>
                        <a href="${hrefTest}" target="_blank" class="shrink-0 h-7 w-7 rounded-md flex items-center justify-center text-slate-400 hover:text-blue-600 hover:bg-blue-50 dark:hover:bg-blue-900/20 transition mt-0.5" title="Open test case">
                            <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14"/></svg>
                        </a>
                    </div>`;
            });

            htmlContent += `</div></div></div>`;
        });
        if (htmlContent !== '') {
            $('#testCaseListModal #testCaseListGroup').append(htmlContent);
        }
        hideLoaderInModal('#testCaseListModal');
        window.dispatchEvent(new CustomEvent('testcaselist-modal-open'));

    }).fail(handleErrorAjaxAfterTimeout);
}

function buttonCloseHandler(event) {
    var modalID = event.data.extra;
    $(modalID).find("#name").attr("disabled", false);
    $(modalID)[0].reset();
    $(this).find('div.has-error').removeClass("has-error");
    clearResponseMessage($(modalID));
}

/** Clears the accordion when the "test cases using" modal closes. */
function getTestCasesUsingModalCloseHandler() {
    $('#testCaseListModal #testCaseListGroup').empty();
}
