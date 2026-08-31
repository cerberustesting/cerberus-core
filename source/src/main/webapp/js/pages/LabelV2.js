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
 * Label & Tag list - V2, on js/global/crbTable.js.
 *
 * This page differs from the other migrations: the table is only ONE of four
 * tabs. The other three (Requirement / Sticker / Battery trees) are
 * bootstrap-treeview instances driven by generateLabelTree(), and they are NOT
 * migrated - they keep working exactly as before, including the icon/button
 * reskin already applied to .treeview in components.css.
 *
 * Action gating is a 1:1 port of Label.js:509-567 (table-wide `hasPermissions`):
 *   - Edit/View  : always rendered; icon AND tooltip swap pencil/eye
 *   - Delete     : only when hasPermissions
 *   - Test Cases : always rendered, a plain link, no gate
 *
 * Everything the trees and modals need (generateLabelTree, addEntryClick,
 * editEntryClick, the save handlers...) still lives in js/pages/Label.js, which
 * this page does NOT load - so those functions are re-declared here. See the
 * note above the copied block below.
 * ========================================================================== */

/*
 * IMPORTANT - this page loads js/pages/Label.js TOO (see Label.jsp).
 *
 * Unlike the other migrations, almost everything on this page that is not the
 * table still lives in Label.js: the three trees (generateLabelTree), the
 * add/edit modals and their save handlers, the parent-label combo, the
 * requirement panel toggles... Copying ~500 lines of that here would guarantee
 * the two drift apart, so the V1 file is loaded as-is and only initPage() and
 * the table are overridden below (this file is loaded second, so its initPage
 * wins).
 *
 * The catch: BOTH files register their own document-ready hook, and both now
 * resolve to this initPage - so it would run twice (two tables, doubled
 * listeners, two identical requests). The guard below makes it idempotent,
 * which is also why it must not be removed if the load order ever changes.
 */
$.when($.getScript("js/global/global.js")).then(function () {
    $(document).ready(function () {
        initPage();
    });
});

var CRB_LABEL_TABLE_ID = "labelTableV2";
var crbLabelV2Initialised = false;

function initPage() {
    if (crbLabelV2Initialised) {
        return;
    }
    crbLabelV2Initialised = true;

    var doc = new Doc();

    displayPageLabel();

    $("#addLabelButton").click(addEntryModalSaveHandler);
    $("#editLabelButton").click(editEntryModalSaveHandler);

    tinymce.init({
        selector: ".wysiwyg",
        menubar: true,
        statusbar: false,
        toolbar: true,
        resize: true,
        height: 400,
        skin: 'oxide-dark'
    });

    createCerberusTable({
        id: CRB_LABEL_TABLE_ID,
        mount: "#labelList",
        endpoint: "ReadLabel?q=1" + getUser().defaultSystemsQuery,
        distinctEndpoint: "ReadLabel?q=1" + getUser().defaultSystemsQuery,
        rowKey: "id",
        defaultSort: {field: "system", dir: "asc"},
        pageLength: 10,
        searchPlaceholder: "Search labels...",
        emptyMessage: "No label matches your search",

        toolbar: function (ctx) {
            if (!ctx.hasPermissions) {
                return "";
            }
            return "<button id='createLabelButton' type='button' " +
                "onclick=\"addEntryClick()\" " +
                "class='bg-sky-400 hover:bg-sky-500 text-white flex items-center gap-1 px-3 py-1 rounded-lg h-10'>" +
                "<i data-lucide='plus' class='w-4 h-4'></i><span>" +
                doc.getDocLabel("page_label", "btn_create") + "</span></button>";
        },

        // `field` = server column name (sorting/filtering), `prop` = key in the row.
        columns: [
            {field: "lab.id", prop: "id", title: doc.getDocOnline("label", "id"),
             width: "70px", like: true, className: "font-mono"},
            {field: "system", prop: "system", title: doc.getDocOnline("label", "system"),
             width: "110px", filterable: true},
            {field: "label", prop: "label", title: doc.getDocOnline("label", "label"),
             width: "180px", className: "font-medium", filterable: true},
            {field: "description", prop: "description", title: doc.getDocOnline("label", "description"),
             width: "240px", like: true},
            {field: "type", prop: "type", title: doc.getDocOnline("label", "type"),
             width: "120px", filterable: true},
            {
                // Coloured preview of the label as it appears elsewhere in the app.
                // Not sortable/searchable server-side, same as V1.
                title: doc.getDocOnline("page_label", "display"), sortable: false, width: "160px",
                render: function (row) {
                    if (!row.label) {
                        return "";
                    }
                    // Ink derived from the colour rather than read from row.fontColor:
                    // ReadLabel serialises the entity, whose fontColor field is never
                    // populated (the value only exists in the nested `display` object),
                    // so reading it here returned null and a #000000 label came out as
                    // black text on black. See crbFontColorFor in js/global/global.js.
                    return '<span class="crb_tc_label" style="' + crbChipStyle(row.color) +
                        '">' + crbTableEscape(row.label) + '</span>';
                }
            },
            {
                // Parent label chip; clicking it filters the list on that parent.
                // Addressed by row index through the shared helper so the label
                // name never lands inside an inline handler.
                title: doc.getDocOnline("label", "parentid"), sortable: false,
                width: "160px", visible: false,
                render: function (row, i) {
                    if (!row.labelParentObject) {
                        return "";
                    }
                    return '<button type="button" class="crb_tc_label" style="' +
                        crbChipStyle(row.labelParentObject.color) + 'border:0;cursor:pointer" ' +
                        'title="' + crbTableEscape(row.labelParentObject.description || '') + '" ' +
                        'onclick="crbTableCellCallback(\'' + CRB_LABEL_TABLE_ID +
                        '\',\'labelV2FilterOnParent\',' + Number(i) + ')">' +
                        crbTableEscape(row.labelParentObject.label) + '</button>';
                }
            },
            {field: "longDescription", prop: "longDescription", title: doc.getDocOnline("label", "longdesc"),
             width: "240px", visible: false, like: true},
            {field: "color", prop: "color", title: doc.getDocOnline("label", "color"),
             width: "100px", visible: false},
            {field: "requirementType", prop: "requirementType", title: doc.getDocOnline("label", "reqtype"),
             width: "140px", visible: false, filterable: true},
            {field: "requirementStatus", prop: "requirementStatus", title: doc.getDocOnline("label", "reqstatus"),
             width: "140px", visible: false, filterable: true},
            {field: "requirementCriticity", prop: "requirementCriticity",
             title: doc.getDocOnline("label", "reqcriticity"), width: "140px", visible: false, filterable: true},
            {field: "usrCreated", prop: "usrCreated", title: doc.getDocOnline("transversal", "UsrCreated"),
             width: "120px", visible: false},
            {
                field: "dateCreated", prop: "dateCreated", title: doc.getDocOnline("transversal", "DateCreated"),
                width: "150px", visible: false,
                render: function (row) { return crbTableEscape(getDate(row.dateCreated)); }
            },
            {field: "usrModif", prop: "usrModif", title: doc.getDocOnline("transversal", "UsrModif"),
             width: "120px", visible: false},
            {
                field: "dateModif", prop: "dateModif", title: doc.getDocOnline("transversal", "DateModif"),
                width: "150px", visible: false,
                render: function (row) { return crbTableEscape(getDate(row.dateModif)); }
            }
        ],

        actions: [
            {
                key: "edit", gate: "always",
                icon: function (row, ctx) { return ctx.hasPermissions ? "pencil" : "eye"; },
                title: function (row, ctx) {
                    return ctx.hasPermissions
                        ? doc.getDocLabel("page_label", "btn_edit")
                        : doc.getDocLabel("page_label", "btn_view");
                },
                onClick: function (row) { editEntryClick(row.id, row.system); }
            },
            {
                key: "delete", icon: "trash-2", gate: "permission", danger: true,
                title: doc.getDocLabel("page_label", "btn_delete"),
                onClick: function (row) { deleteEntryClick(row.id, row.label); }
            },
            {
                key: "testcases", icon: "list", gate: "always",
                title: doc.getDocLabel("page_label", "btn_tclist"),
                href: function (row) { return "./TestCaseList.jsp?label=" + encodeURIComponent(row.label); }
            }
        ]
    });

    // Application.html is pulled in on EVERY page by modalInclusions.jsp and
    // declares its own editEntryModalCloseHandler. Being parsed in the body, after
    // the head scripts, its declaration wins the global name - so closing the Edit
    // Label modal reset the *Application* form instead, and Label's own error
    // styling and response message survived until the next reload. Rebinding here,
    // at document-ready, is after every include has been parsed. Safe on this page:
    // the Application modal is never opened from Label.
    // (Same collision in the V1 page; fixing it there would change a file kept
    // byte-for-byte as the rollback copy, so it stays scoped to V2.)
    window.editEntryModalCloseHandler = labelV2EditModalCloseHandler;

    // The three trees are built by generateLabelTree() below, which Label.js's
    // own document-ready block calls right after this function - see the note
    // above that function about how it takes over the name.
}

/* =============================================================================
 * The three browse trees - Requirement / Sticker / Battery.
 *
 * This REDECLARES Label.js#generateLabelTree. Both files are loaded, both
 * declare the name at top level, and this file is loaded second, so this body
 * wins - which is what makes Label.js's own document-ready call (Label.js:37)
 * build V2 trees without that file changing. The same deliberate override as
 * initPage() at the top of this file; the checker in check-v2-migration.sh
 * flags shadowed globals, and these two are the page's known, intended pair.
 *
 * Also now dead, and harmless: Label.js:38-86 binds click handlers to
 * #expandAllTreeR / #collapseAllTreeR / #refreshButtonTreeR / #createLabelButtonTreeR
 * and their S and B twins. Those twelve buttons were three copies of the same
 * hand-rolled toolbar in Label.jsp; the tree component draws one now, so the
 * ids no longer exist and jQuery binds to an empty set - a no-op, not an error.
 * ========================================================================== */
var CRB_LABEL_TREES = [
    {id: "labelTreeR", mount: "#mainTreeR", type: "REQUIREMENT", typeLabel: "requirement", key: "requirements"},
    {id: "labelTreeS", mount: "#mainTreeS", type: "STICKER", typeLabel: "sticker", key: "stickers"},
    {id: "labelTreeB", mount: "#mainTreeB", type: "BATTERY", typeLabel: "battery", key: "batteries"}
];

function generateLabelTree() {
    CRB_LABEL_TREES.forEach(function (tree) {
        // Idempotent: generateLabelTree() is also the refresh path, called after
        // every create / edit / delete (Label.js:279, 313, 37x). Rebuilding the
        // markup would throw away the user's expand state and detach the Alpine
        // component; only the data is replaced, below.
        if (!window.crbLabelTreeRegistry[tree.id]) {
            createCerberusLabelTree({
                id: tree.id,
                mount: tree.mount,
                mode: "browse",
                type: tree.type,
                typeLabel: tree.typeLabel,
                searchPlaceholder: "Search " + tree.typeLabel + " labels...",
                onRefresh: generateLabelTree,
                onCreate: function (type) {
                    addEntryClick(type);
                    // V1's Create buttons called this alongside addEntryClick to
                    // show/hide the requirement-only fields for the preselected
                    // type; dropping it would leave the modal showing the wrong
                    // half of the form.
                    showHideRequirementPanelAdd();
                },
                onEdit: function (label) {
                    editEntryClick(label.id, label.system);
                },
                onDelete: function (label) {
                    deleteEntryClick(label.id, label.label);
                }
            });
        }
    });

    return $.ajax("ReadLabel?q=1" + getUser().defaultSystemsQuery + "&withHierarchy=true")
        .then(function (data) {
            var hierarchy = data.labelHierarchy || {};
            // hasPermissions travels with the same response as the nodes, so the
            // create/edit/delete gate is set from the server on every refresh
            // rather than assumed at build time. V1 asked for the flag on this
            // request and never read it: the tree rendered Edit and Delete for
            // everyone, because hasButtons=Y put the buttons in the server-built
            // HTML with no gate of any kind. Only the table honoured it.
            CRB_LABEL_TREES.forEach(function (tree) {
                crbLabelTreeSetNodes(tree.id, hierarchy[tree.key] || [],
                    {hasPermissions: !!data.hasPermissions});
            });
        }).fail(handleErrorAjaxAfterTimeout);
}

/**
 * Label's own edit-modal close handler, rescued from the name collision above.
 * Body identical to Label.js#editEntryModalCloseHandler.
 */
function labelV2EditModalCloseHandler() {
    var form = $('#editLabelModal #editLabelModalForm')[0];
    if (form) {
        form.reset();
    }
    $('#editLabelModal').find('div.has-error').removeClass("has-error");
    clearResponseMessage($('#editLabelModal'));
}

/** Clicking a parent-label chip filters the list on it. */
function labelV2FilterOnParent(row, table) {
    if (!row.labelParentObject) {
        return;
    }
    table.activeFilters["label"] = [row.labelParentObject.label];
    table.start = 0;
    table.fetch();
}

function displayPageLabel() {
    var doc = new Doc();
    displayGlobalLabel(doc);
    $("#pageTitle").html(doc.getDocLabel("page_label", "title"));
    $("#title").html(doc.getDocOnline("page_label", "title"));
    displayFooter(doc);
}

