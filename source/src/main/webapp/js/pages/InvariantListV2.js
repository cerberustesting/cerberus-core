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
 * Invariants - V2, on js/global/crbTable.js.
 *
 * TWO tables, one per tab, both on ReadInvariant (?access=PUBLIC / ?access=PRIVATE)
 * and both migrated. They share their column set; only the actions differ:
 *   - Public  : Edit, Duplicate, Delete, plus the Create button
 *   - Private : View only (these are the instance's internal invariants)
 * V1 already made that distinction, by writing two column functions that happened
 * to differ; here it is the gate contract saying it out loud.
 *
 * Both tables are built up front, as V1 did - the private one is inside a hidden
 * tab panel, which costs one extra request on load and keeps the tab switch
 * instant. crbTable measures nothing on mount, so being hidden is harmless (unlike
 * a DataTable, which lays its columns out at zero width when hidden).
 *
 * Fixed on the way over:
 *   - every handler was built as onclick="openModalInvariant('${obj.idName}',
 *     '${escapeHtml(obj.value)}', 'EDIT')". HTML-escaping does not protect a JS
 *     string inside an attribute, and the Delete handler did not even escape:
 *     onclick="removeEntryClick('${obj.idName}','${obj.value}')". An invariant
 *     value containing a quote broke the row; one crafted to close the string ran.
 *     V2 passes the row to onClick.
 *   - after a delete, V1 redrew #invariantsTable even when the row came from the
 *     private table.
 * ========================================================================== */

$.when($.getScript("js/global/global.js")).then(function () {
    $(document).ready(function () {
        initPage();
        $('[data-toggle="popover"]').popover({placement: 'auto', container: 'body'});
    });
});

var CRB_INV_PUBLIC_TABLE_ID = "invariantPublicTableV2";
var CRB_INV_PRIVATE_TABLE_ID = "invariantPrivateTableV2";

function initPage() {
    displayPageLabel();
    displayPublicTable();
    displayPrivateTable();
}

/**
 * Columns shared by both tabs. `field` = server column name (sName in V1),
 * `prop` = key in the row; they differ on idName and veryShortDesc.
 */
function invV2Columns() {
    var doc = new Doc();
    return [
        {field: "idname", prop: "idName", title: doc.getDocLabel("page_invariant", "idname"),
         width: "220px", className: "font-medium", filterable: true},
        {field: "value", prop: "value", title: doc.getDocLabel("page_invariant", "value"),
         width: "200px", like: true, className: "font-mono"},
        {field: "description", prop: "description", title: doc.getDocLabel("page_invariant", "description"),
         width: "360px", like: true},
        {field: "gp1", prop: "gp1", title: doc.getDocLabel("page_invariant", "gp1"), width: "120px"},
        {field: "gp2", prop: "gp2", title: doc.getDocLabel("page_invariant", "gp2"), width: "120px"},

        // ---- available from Config, hidden by default ----
        {field: "sort", prop: "sort", title: doc.getDocLabel("page_invariant", "sort"),
         width: "90px", visible: false, className: "text-right tabular-nums"},
        {field: "VeryShortDesc", prop: "veryShortDesc",
         title: doc.getDocLabel("page_invariant", "veryShortDesc"), width: "140px", visible: false},
        {field: "gp3", prop: "gp3", title: doc.getDocLabel("page_invariant", "gp3"), width: "120px", visible: false},
        {field: "gp4", prop: "gp4", title: doc.getDocLabel("page_invariant", "gp4"), width: "120px", visible: false},
        {field: "gp5", prop: "gp5", title: doc.getDocLabel("page_invariant", "gp5"), width: "120px", visible: false},
        {field: "gp6", prop: "gp6", title: doc.getDocLabel("page_invariant", "gp6"), width: "120px", visible: false},
        {field: "gp7", prop: "gp7", title: doc.getDocLabel("page_invariant", "gp7"), width: "120px", visible: false},
        {field: "gp8", prop: "gp8", title: doc.getDocLabel("page_invariant", "gp8"), width: "120px", visible: false},
        {field: "gp9", prop: "gp9", title: doc.getDocLabel("page_invariant", "gp9"), width: "120px", visible: false}
    ];
}

/** idname + value is the primary key of an invariant. */
function invV2RowKey(row) {
    return JSON.stringify([row.idName, row.value]);
}

function displayPublicTable() {
    if (crbTableInstance(CRB_INV_PUBLIC_TABLE_ID)) {
        return;
    }
    var doc = new Doc();
    createCerberusTable({
        id: CRB_INV_PUBLIC_TABLE_ID,
        mount: "#invariantList",
        endpoint: "ReadInvariant?access=PUBLIC",
        distinctEndpoint: "ReadInvariant?access=PUBLIC",
        rowKey: invV2RowKey,
        defaultSort: {field: "idname", dir: "asc"},
        pageLength: 15,
        searchPlaceholder: "Search invariants...",
        emptyMessage: "No invariant matches your search",
        columns: invV2Columns(),

        toolbar: function () {
            return "<button id='createInvariantButton' type='button' " +
                "onclick=\"openModalInvariant(undefined, undefined, 'ADD')\" " +
                "class='bg-sky-400 hover:bg-sky-500 text-white flex items-center gap-1 px-3 py-1 rounded-lg h-10'>" +
                "<i data-lucide='plus' class='w-4 h-4'></i><span>" +
                doc.getDocLabel("page_invariant", "button_create") + "</span></button>";
        },

        actions: [
            {
                key: "edit", icon: "pencil", gate: "always",
                title: doc.getDocLabel("page_invariant", "button_edit"),
                onClick: function (row) { openModalInvariant(row.idName, row.value, 'EDIT'); }
            },
            {
                key: "duplicate", icon: "copy", gate: "always",
                title: doc.getDocLabel("page_invariant", "button_duplicate"),
                onClick: function (row) { openModalInvariant(row.idName, row.value, 'DUPLICATE'); }
            },
            {
                key: "remove", icon: "trash-2", gate: "always", danger: true,
                title: doc.getDocLabel("page_invariant", "button_remove"),
                onClick: function (row) { deleteInvariant(row.idName, row.value); }
            }
        ]
    });
}

function displayPrivateTable() {
    if (crbTableInstance(CRB_INV_PRIVATE_TABLE_ID)) {
        return;
    }
    var doc = new Doc();
    createCerberusTable({
        id: CRB_INV_PRIVATE_TABLE_ID,
        mount: "#invariantPrivateList",
        endpoint: "ReadInvariant?access=PRIVATE",
        distinctEndpoint: "ReadInvariant?access=PRIVATE",
        rowKey: invV2RowKey,
        defaultSort: {field: "idname", dir: "asc"},
        pageLength: 15,
        searchPlaceholder: "Search private invariants...",
        emptyMessage: "No private invariant matches your search",
        columns: invV2Columns(),
        actions: [
            {
                // Read-only, exactly as V1: the private tab opened the same modal
                // but showed an eye. No create, duplicate or delete here.
                key: "view", icon: "eye", gate: "always",
                title: doc.getDocLabel("page_invariant", "button_edit"),
                onClick: function (row) { openModalInvariant(row.idName, row.value, 'EDIT'); }
            }
        ]
    });
}

/** Reloads whichever invariant list is on screen. */
function invV2ReloadAll() {
    [CRB_INV_PUBLIC_TABLE_ID, CRB_INV_PRIVATE_TABLE_ID].forEach(function (id) {
        var t = crbTableInstance(id);
        if (t) {
            t.reload();
        }
    });
}

function displayPageLabel() {
    var doc = new Doc();

    $("#title").html(doc.getDocLabel("page_invariant", "allInvariants"));
    $("[name='editInvariantField']").html(doc.getDocLabel("page_invariant", "editinvariant_field"));
    $("[name='addInvariantField']").html(doc.getDocLabel("page_invariant", "addinvariant_field"));
    $("[name='systemField']").html(doc.getDocLabel("page_invariant", "system_field") + " (" + getSys() + ")");
    displayFooter(doc);
    displayGlobalLabel(doc);
}

/* --- Carried over from InvariantList.js (V1) -------------------------------
 * The delete confirmation is unchanged apart from its refresh, which used to
 * redraw #invariantsTable even for a row coming from the private tab.
 * ------------------------------------------------------------------------- */
async function deleteInvariant(param, value) {
    try {
        const result = await Swal.fire({
            title: 'Are you sure?',
            text: `Do you really want to delete invariant "${param}" with value "${value}"?`,
            icon: 'warning',
            showCancelButton: true,
            confirmButtonText: 'Yes, delete it!',
            cancelButtonText: 'Cancel',
            background: 'var(--crb-new-bg)',
            color: 'var(--crb-black-color)',
            showLoaderOnConfirm: true,
            allowOutsideClick: () => !Swal.isLoading(),
            preConfirm: async () => {
                try {
                    const payload = new URLSearchParams({ idName: param, value: value });
                    const response = await fetch('DeleteInvariant', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                        body: payload.toString()
                    });
                    const data = await response.json();
                    if (data.messageType !== 'OK') {
                        Swal.showValidationMessage(data.message || 'Deletion failed');
                    }
                    return data;
                } catch (err) {
                    Swal.showValidationMessage(`Request failed: ${err}`);
                }
            }
        });

        if (result.isConfirmed && result.value) {
            const data = result.value;
            const messageType = getAlertType(data.messageType);

            if (messageType === 'success') {
                // V2: was a fnDraw on #invariantsTable, which does not exist here and
                // was the wrong table anyway when the row came from the private tab.
                invV2ReloadAll();
                cleanCacheInvariant(param);
            }

            notifyInPage("success", 'Invariant deleted');
        }
    } catch (e) {
        console.error(e);
        notifyInline('Unexpected error deleting invariant', 'error', '#DialogMessagesArea', true);
    }
}


function removeEntryClick(param, value) {
    deleteInvariant(param, value);
}

