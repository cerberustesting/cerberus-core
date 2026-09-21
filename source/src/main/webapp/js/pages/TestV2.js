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
 * Test folders - V2, on js/global/crbTable.js.
 *
 * Endpoint: ReadTest (contentTable + table-level hasPermissions). Reached from the
 * "Test Folder" button of the test case list.
 *
 * Action gating is a 1:1 port of Test.js:127-181:
 *   - Edit/View  : always rendered; only the icon swaps on hasPermissions
 *   - Delete     : only when hasPermissions
 *   - Test cases : always, a plain link to the filtered list
 *
 * Fixed on the way over:
 *   - the handlers were built as onclick="editEntryClick('"+escapeHtml(test)+"')".
 *     HTML-escaping does not protect a JS string inside an attribute, so a folder
 *     named  x'); alert(1); //  ran - the very defect proven on this app's test
 *     folder names. V2 passes the row to onClick.
 *   - the per-row `setTimeout(lucide.createIcons, 50)` fired once per rendered row
 *     on every draw; the component refreshes the icons once per render.
 *   - the Active column rendered a disabled <input type=checkbox>, which reads as a
 *     control that refuses to move and is skipped by keyboard navigation.
 *   - `hasPermissions` was read off `data` (the row) rather than the response, so
 *     the Delete button depended on a field the rows do not carry: it never showed.
 * ========================================================================== */

$.when($.getScript("js/global/global.js")).then(function () {
    $(document).ready(function () {
        initPage();
        $('[data-toggle="popover"]').popover({placement: 'auto', container: 'body'});
    });
});

var CRB_TEST_TABLE_ID = "testFolderTableV2";

function initPage() {
    var doc = new Doc();

    displayGlobalLabel(doc);
    displayPageLabel(doc);
    displayFooter(doc);
    displayInvariantList("testFolderIsActive", "TESTACTIVE", false);

    createCerberusTable({
        id: CRB_TEST_TABLE_ID,
        mount: "#testList",
        endpoint: "ReadTest",
        distinctEndpoint: "ReadTest",
        rowKey: "test",
        defaultSort: {field: "test", dir: "asc"},
        pageLength: 15,
        searchPlaceholder: "Search test folders...",
        emptyMessage: "No test folder matches your search",
        sendDefaultSystems: false,

        toolbar: function (ctx) {
            if (!ctx.hasPermissions) {
                return "";
            }
            return "<button id='createTestButton' type='button' onclick='addEntryClick()' " +
                "class='bg-sky-400 hover:bg-sky-500 text-white flex items-center gap-1 px-3 py-1 rounded-lg h-10'>" +
                "<i data-lucide='plus' class='w-4 h-4'></i><span>" +
                doc.getDocLabel("page_test", "btn_create") + "</span></button>";
        },

        columns: [
            {field: "test", title: doc.getDocOnline("test", "Test"),
             width: "280px", className: "font-medium", filterable: true},
            {field: "description", title: doc.getDocOnline("test", "description"),
             width: "460px", like: true},
            {
                field: "isActive", title: doc.getDocOnline("test", "isActive"),
                width: "120px", filterable: true,
                render: function (row) { return testV2ActiveChip(row.isActive); }
            },

            // ---- available from Config, hidden by default ----
            {field: "tes.usrCreated", prop: "usrCreated",
             title: doc.getDocOnline("transversal", "UsrCreated"), width: "130px", visible: false},
            {
                field: "tes.dateCreated", prop: "dateCreated",
                title: doc.getDocOnline("transversal", "DateCreated"),
                width: "170px", visible: false, like: true,
                render: function (row) { return crbTableEscape(testV2Date(row.dateCreated)); }
            },
            {field: "tes.usrModif", prop: "usrModif",
             title: doc.getDocOnline("transversal", "UsrModif"), width: "130px", visible: false},
            {
                field: "tes.dateModif", prop: "dateModif",
                title: doc.getDocOnline("transversal", "DateModif"),
                width: "170px", visible: false, like: true,
                render: function (row) { return crbTableEscape(testV2Date(row.dateModif)); }
            }
        ],

        actions: [
            {
                key: "edit", gate: "always",
                icon: function (row, ctx) { return ctx.hasPermissions ? "pencil" : "eye"; },
                title: function (row, ctx) {
                    return ctx.hasPermissions
                        ? doc.getDocLabel("page_test", "btn_edit")
                        : doc.getDocLabel("page_test", "btn_edit");
                },
                onClick: function (row) { editEntryClick(row.test); }
            },
            {
                key: "delete", icon: "trash-2", gate: "permission", danger: true,
                title: doc.getDocLabel("page_test", "button_delete"),
                onClick: function (row) { testV2DeleteEntryClick(row.test); }
            },
            {
                key: "testcases", icon: "external-link", gate: "always",
                title: doc.getDocLabel("page_test", "btn_tclist"),
                href: function (row) {
                    return "./TestCaseList.jsp?test=" + encodeURIComponent(row.test);
                }
            }
        ]
    });
}

/** Yes/no chip, replacing V1's disabled checkbox. */
function testV2ActiveChip(value) {
    var on = (value === true || value === "Y" || value === "true");
    var cls = on
        ? "bg-emerald-50 text-emerald-700 dark:bg-emerald-500/10 dark:text-emerald-400"
        : "bg-slate-100 text-slate-600 dark:bg-slate-500/10 dark:text-slate-400";
    return '<span class="inline-flex items-center rounded-full px-2.5 py-1 text-xs font-semibold ' +
        'ring-1 ring-inset ' + cls + '">' + (on ? "Yes" : "No") + '</span>';
}

function testV2Date(value) {
    if (value === undefined || value === null || value === "" || value === 0) {
        return "";
    }
    return getDate(value);
}

/** Delete a test folder. Same dialog and servlet as Test.js:85-105. */
function testV2DeleteEntryClick(entry) {
    clearResponseMessageMainPage();
    var doc = new Doc();
    var messageComplete = doc.getDocLabel("page_test", "message_delete").replace("%ENTRY%", entry);

    crbConfirmDelete({
        title: doc.getDocLabel("page_test", "button_delete"),
        html: messageComplete,
        preConfirm: function () {
            return $.post("DeleteTest", {test: entry}, "json").then(function (data) {
                var messageType = getAlertType(data.messageType);
                if (messageType === "success") {
                    var table = crbTableInstance(CRB_TEST_TABLE_ID);
                    if (table) {
                        table.reload();
                    }
                }
                showMessageMainPage(messageType, data.message, false);
                return data;
            }).fail(handleErrorAjaxAfterTimeout);
        }
    });
}

/** V1's name, kept as an alias in case a shared modal ever calls it. */
var deleteEntryClick = testV2DeleteEntryClick;

function addEntryClick() {
    clearResponseMessageMainPage();
    window.dispatchEvent(new CustomEvent('test-folder-modal-open', {detail: {mode: 'ADD'}}));
}

function editEntryClick(test) {
    clearResponseMessageMainPage();
    window.dispatchEvent(new CustomEvent('test-folder-modal-open', {detail: {mode: 'EDIT', test: test}}));
}

function displayPageLabel(doc) {
    $("#pageTitle").html(doc.getDocLabel("test", "Test"));
    $("#title").html(doc.getDocLabel("test", "Test"));
    $("[name='addEntryField']").html(doc.getDocLabel("page_test", "btn_create"));
    $("[name='confirmationField']").html(doc.getDocLabel("page_test", "button_delete"));
    $("[name='editEntryField']").html(doc.getDocLabel("page_test", "btn_edit"));
    $("[name='testField']").html(doc.getDocOnline("test", "Test"));
    $("[name='activeField']").html(doc.getDocOnline("test", "isActive"));
    $("[name='descriptionField']").html(doc.getDocOnline("test", "description"));
}
