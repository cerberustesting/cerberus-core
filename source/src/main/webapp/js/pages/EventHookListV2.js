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
 * Event hooks - V2, on js/global/crbTable.js.
 *
 * Endpoint: ReadEventHook (contentTable + table-level hasPermissions).
 *
 * Action gating, ported from EventHookList.js:120-150 - except that V1 rendered
 * all three actions unconditionally and never read the permission flag its own
 * endpoint sends, so a read-only user was offered Edit, Duplicate and Delete on
 * every hook. Same deviation, and same reason, as UserManager: the servlets do
 * check, so pressing them produced an error rather than a change, but the buttons
 * had no business being there.
 *   - Edit / Duplicate / Delete : permission
 *
 * Fixed on the way over:
 *   - the handlers were built as onclick="openModalEventHook('" + obj.id + "',...)"
 *     with no escaping at all. The id is numeric today, but it is the pattern that
 *     made a test folder name executable elsewhere in this app.
 *   - the delete confirmation went through the legacy showModalConfirmation with a
 *     hidden field carrying the id; it now uses crbConfirmDelete like every other
 *     migrated page, so the id never leaves the closure.
 * ========================================================================== */

$.when($.getScript("js/global/global.js")).then(function () {
    $(document).ready(function () {
        initPage();
        $('[data-toggle="popover"]').popover({placement: 'auto', container: 'body'});
    });
});

var CRB_EVH_TABLE_ID = "eventHookTableV2";

function initPage() {
    var doc = new Doc();

    displayPageLabel();

    createCerberusTable({
        id: CRB_EVH_TABLE_ID,
        mount: "#eventHookList",
        endpoint: "ReadEventHook",
        distinctEndpoint: "ReadEventHook",
        rowKey: "id",
        defaultSort: {field: "evh.eventReference", dir: "asc"},
        pageLength: 10,
        searchPlaceholder: "Search event hooks...",
        emptyMessage: "No event hook matches your search",
        sendDefaultSystems: false,

        toolbar: function (ctx) {
            if (!ctx.hasPermissions) {
                return "";
            }
            return "<button id='createEventHookButton' type='button' " +
                "onclick=\"openModalEventHook(0, 'ADD')\" " +
                "class='bg-sky-400 hover:bg-sky-500 text-white flex items-center gap-1 px-3 py-1 rounded-lg h-10'>" +
                "<i data-lucide='plus' class='w-4 h-4'></i><span>" +
                doc.getDocLabel("page_global", "btn_add") + "</span></button>";
        },

        // `field` = server column name (sName in V1, prefixed evh.*), `prop` = key
        // in the row - they differ on every column here.
        columns: [
            {field: "evh.eventReference", prop: "eventReference",
             title: doc.getDocLabel("page_eventhook", "eventReference"),
             width: "200px", className: "font-medium", filterable: true},
            {field: "evh.objectKey1", prop: "objectKey1",
             title: doc.getDocLabel("page_eventhook", "objectKey1"), width: "160px"},
            {field: "evh.objectKey2", prop: "objectKey2",
             title: doc.getDocLabel("page_eventhook", "objectKey2"), width: "160px"},
            {
                field: "evh.isActive", prop: "isActive",
                title: doc.getDocLabel("page_eventhook", "isActive"),
                width: "120px", filterable: true,
                render: function (row) { return evhV2ActiveChip(row.isActive); }
            },
            {
                field: "evh.hookConnector", prop: "hookConnector",
                title: doc.getDocLabel("page_eventhook", "hookConnector"),
                width: "140px", filterable: true,
                render: function (row) {
                    if (isEmpty(row.hookConnector)) {
                        return "";
                    }
                    return '<span class="inline-flex items-center rounded px-1.5 py-0.5 text-[11px] ' +
                        'font-medium bg-sky-50 text-sky-700 dark:bg-sky-900/30 dark:text-sky-300">' +
                        crbTableEscape(row.hookConnector) + '</span>';
                }
            },
            {field: "evh.hookRecipient", prop: "hookRecipient",
             title: doc.getDocLabel("page_eventhook", "hookRecipient"), width: "320px"},
            {field: "evh.description", prop: "description",
             title: doc.getDocLabel("page_eventhook", "description"), width: "260px", like: true},

            // ---- available from Config, hidden by default ----
            {field: "evh.usrCreated", prop: "usrCreated",
             title: doc.getDocOnline("transversal", "UsrCreated"), width: "130px", visible: false},
            {
                field: "evh.dateCreated", prop: "dateCreated",
                title: doc.getDocOnline("transversal", "DateCreated"),
                width: "170px", visible: false, like: true,
                render: function (row) { return crbTableEscape(evhV2Date(row.dateCreated)); }
            },
            {field: "evh.usrModif", prop: "usrModif",
             title: doc.getDocOnline("transversal", "UsrModif"), width: "130px", visible: false},
            {
                field: "evh.dateModif", prop: "dateModif",
                title: doc.getDocOnline("transversal", "DateModif"),
                width: "170px", visible: false, like: true,
                render: function (row) { return crbTableEscape(evhV2Date(row.dateModif)); }
            }
        ],

        actions: [
            {
                key: "edit", icon: "pencil", gate: "permission",
                title: doc.getDocLabel("page_global", "btn_edit"),
                onClick: function (row) { openModalEventHook(row.id, "EDIT"); }
            },
            {
                key: "duplicate", icon: "copy", gate: "permission",
                title: doc.getDocLabel("page_global", "btn_duplicate"),
                onClick: function (row) { openModalEventHook(row.id, "DUPLICATE"); }
            },
            {
                key: "delete", icon: "trash-2", gate: "permission", danger: true,
                title: doc.getDocLabel("page_global", "btn_delete"),
                onClick: function (row) { evhV2DeleteEntryClick(row.id); }
            }
        ]
    });
}

/** Active / inactive chip. The server sends a boolean, older rows a "Y" string. */
function evhV2ActiveChip(value) {
    var active = (value === true || value === "true" || value === "Y");
    var chip = active
        ? "bg-green-50 text-green-700 ring-green-600/20 dark:bg-green-900/30 dark:text-green-300"
        : "bg-slate-50 text-slate-500 ring-slate-500/20 dark:bg-slate-800 dark:text-slate-400";
    return '<span class="inline-flex items-center gap-1.5 rounded-full px-2.5 py-1 text-xs ' +
        'font-semibold ring-1 ring-inset ' + chip + '">' +
        '<i data-lucide="' + (active ? 'circle-check' : 'circle-pause') + '" class="h-3.5 w-3.5"></i>' +
        '<span>' + (active ? 'Active' : 'Inactive') + '</span></span>';
}

function evhV2Date(value) {
    if (value === undefined || value === null || value === "" || value === 0) {
        return "";
    }
    return getDate(value);
}

/**
 * Delete an event hook. Same servlet as EventHookList.js:93-115; the legacy
 * confirmation modal (which stashed the id in a hidden input) is replaced by the
 * shared crbConfirmDelete used by every other migrated page.
 */
async function evhV2DeleteEntryClick(id) {
    clearResponseMessageMainPage();
    var doc = new Doc();
    var messageComplete = doc.getDocLabel("page_global", "message_delete")
        .replace("%ENTRY%", id)
        .replace("%TABLE%", " ID Event Hook ");

    const result = await crbConfirmDelete({
        title: doc.getDocLabel("page_global", "btn_delete") || 'Delete',
        html: messageComplete,
        confirmText: doc.getDocLabel("page_global", "btn_delete") || 'Delete',
        cancelText: doc.getDocLabel("page_global", "buttonClose") || 'Cancel',
        preConfirm: async () => {
            try {
                const resp = await $.post("DeleteEventHook", {id: id}, "json");
                if (getAlertType(resp.messageType) !== "success") {
                    Swal.showValidationMessage(resp.message || "Delete failed");
                    return null;
                }
                return resp;
            } catch (e) {
                Swal.showValidationMessage("Unexpected error");
                return null;
            }
        }
    });

    if (result.isConfirmed && result.value) {
        var table = crbTableInstance(CRB_EVH_TABLE_ID);
        if (table) {
            table.reload();
        }
        showMessageMainPage(getAlertType(result.value.messageType), result.value.message, false);
    }
}

/** V1's name, kept as an alias in case a shared modal ever calls it. */
var deleteEventHook = evhV2DeleteEntryClick;

function displayPageLabel() {
    var doc = new Doc();

    $("#pageTitle").html(doc.getDocLabel("page_eventhook", "title"));
    $("#title").html(doc.getDocOnline("page_eventhook", "title"));

    // Invariant combos the event hook modal reads at open time.
    displayInvariantList("eventReference", "EVENTHOOK", false, "CAMPAIGN_END");
    displayInvariantList("hookConnector", "EVENTCONNECTOR", false, "EMAIL");

    displayFooter(doc);
    displayGlobalLabel(doc);
}
