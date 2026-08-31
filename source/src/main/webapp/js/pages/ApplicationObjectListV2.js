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
 * Application Object list - V2, on js/global/crbTable.js.
 *
 * Action gating is a 1:1 port of ApplicationObjectList.js:207-254, which uses
 * the table-wide `hasPermissions` flag:
 *   - Edit/View : always rendered; icon swaps pencil/eye, same handler either
 *                 way (the shared modal enforces read-only itself)
 *   - Delete    : only when hasPermissions
 * Toolbar: Create and "Generate with AI" are both rendered only when
 * hasPermissions (legacy line 85 wraps both).
 *
 * The Add/Edit modal lives in the shared include/transversal/ApplicationObject.html
 * and is untouched; only the table is reimplemented.
 * ========================================================================== */

$.when($.getScript("js/global/global.js")).then(function () {
    $(document).ready(function () {
        initPage();
    });
});

var CRB_AO_TABLE_ID = "applicationObjectTableV2";

function initPage() {
    var doc = new Doc();
    var application = GetURLParameter("application");

    displayPageLabel();

    // Same modal-close wiring as V1 (ApplicationObjectList.js:36-38).
    window.addEventListener('appobject-modal-close', function () {
        buttonCloseHandler({data: {extra: '#editApplicationObjectModalForm'}});
    });

    createCerberusTable({
        id: CRB_AO_TABLE_ID,
        mount: "#applicationObjectList",
        endpoint: "ReadApplicationObject",
        distinctEndpoint: "ReadApplicationObject",
        // Deep link from the Application modal's "Manage" button:
        // ApplicationObjectList.jsp?application=X opens pre-filtered. Declared
        // here so it is part of the first request.
        initialFilters: (application !== null) ? {"obj.application": [application]} : null,
        // An application object is keyed by (application, object) - no single id.
        rowKey: function (row) { return JSON.stringify([row.application, row.object]); },
        defaultSort: {field: "obj.application", dir: "asc"},
        pageLength: 10,
        searchPlaceholder: "Search objects...",
        emptyMessage: "No application object matches your search",

        toolbar: function (ctx) {
            if (!ctx.hasPermissions) {
                return "";
            }
            return "<button id='createApplicationObjectButton' type='button' " +
                "onclick=\"openModalApplicationObject(undefined, undefined, 'ADD', 'applicationObject')\" " +
                "class='bg-sky-400 hover:bg-sky-500 text-white flex items-center gap-1 px-3 py-1 rounded-lg h-10'>" +
                "<i data-lucide='plus' class='w-4 h-4'></i><span>" +
                doc.getDocLabel("page_applicationObject", "button_create") + "</span></button>" +
                "<button id='generate_ao_with_ai' type='button' " +
                "onclick=\"window.dispatchEvent(new CustomEvent('open-ao', {detail:{}}))\" " +
                "class='crb_table_iconbtn'>" +
                "<i data-lucide='sparkles' class='w-4 h-4'></i><span>Generate with AI</span></button>";
        },

        // `field` = server column name (sorting/filtering), `prop` = key in the row.
        // Note the application column's server name is "obj.application" while the
        // others are bare - that asymmetry is legacy's, kept so sorting keeps working.
        columns: [
            {field: "obj.application", prop: "application", title: "Application",
             width: "140px", filterable: true},
            {field: "object", prop: "object", title: "Object", width: "180px",
             like: true, className: "font-medium", filterable: true},
            {
                field: "value", prop: "value", title: "Value", width: "260px", like: true,
                render: function (row) {
                    // cleanErratum() is a shared helper that strips erratum markup.
                    return cleanErratum(row.value === undefined || row.value === null ? "" : row.value);
                }
            },
            {
                field: "screenshotFilename", prop: "screenshotFilename", title: "Screenshot",
                width: "110px", like: true, sortable: false,
                render: function (row, i) {
                    // The image URL carries no user text (application/object are
                    // URL-encoded) and the click handler is addressed by row index
                    // via the shared helper, so nothing from the server ends up
                    // inside an inline handler.
                    if (!row.screenshotFilename) {
                        return "";
                    }
                    var src = "ReadApplicationObjectImage?application=" +
                        encodeURIComponent(row.application) + "&object=" +
                        encodeURIComponent(row.object) + "&time=" + new Date().getTime();
                    return '<img src="' + crbTableEscape(src) + '" class="crb_ao_thumb" ' +
                        'alt="' + crbTableEscape(row.object) + '" ' +
                        'onclick="crbTableCellCallback(\'' + CRB_AO_TABLE_ID +
                        '\',\'aoV2ShowPicture\',' + Number(i) + ')"/>';
                }
            },
            {field: "xOffset", prop: "xOffset", title: "X Offset", width: "90px", like: true},
            {field: "yOffset", prop: "yOffset", title: "Y Offset", width: "90px", like: true},
            {field: "usrCreated", prop: "usrCreated", title: "Created by",
             width: "120px", visible: false},
            {
                field: "dateCreated", prop: "dateCreated", title: "Created",
                width: "150px", visible: false, like: true,
                render: function (row) { return crbTableEscape(getDate(row.dateCreated)); }
            },
            {field: "usrModif", prop: "usrModif", title: "Modified by",
             width: "120px", visible: false},
            {
                field: "dateModif", prop: "dateModif", title: "Modified",
                width: "150px", visible: false, like: true,
                render: function (row) { return crbTableEscape(getDate(row.dateModif)); }
            }
        ],

        actions: [
            {
                key: "edit", gate: "always",
                icon: function (row, ctx) { return ctx.hasPermissions ? "pencil" : "eye"; },
                title: doc.getDocLabel("page_applicationObject", "button_edit"),
                onClick: function (row) {
                    openModalApplicationObject(row.application, row.object, "EDIT", "applicationObject");
                }
            },
            {
                key: "delete", icon: "trash-2", gate: "permission", danger: true,
                title: doc.getDocLabel("page_applicationObject", "button_delete"),
                onClick: function (row) { deleteEntryClick(row.application, row.object); }
            }
        ]
    });

    refreshPopoverDocumentation("applicationObjectList");
}

function displayPageLabel() {
    var doc = new Doc();
    displayGlobalLabel(doc);
    $("#pageTitle").html(doc.getDocLabel("page_applicationObject", "title"));
    $("#title").html(doc.getDocOnline("page_applicationObject", "title"));
    $("[name='editApplicationObjectField']").html(
        doc.getDocLabel("page_applicationObject", "editapplicationobjectfield"));
    displayFooter(doc);
}

function displayModalLabel() {
    var doc = new Doc();
    $("[name='createApplicationObjectField']").html(doc.getDocLabel("page_applicationObject", "createapplicationobjectfield"));
    $("[name='applicationField']").html(doc.getDocLabel("page_applicationObject", "applicationfield"));
    $("[name='objectField']").html(doc.getDocLabel("page_applicationObject", "objectfield"));
    $("[name='valueField']").html(doc.getDocLabel("page_applicationObject", "valuefield"));
    $("[name='screenshotfilenameField']").html(doc.getDocLabel("page_applicationObject", "screenshotfilenamefield"));
    $("[name='buttonClose']").html(doc.getDocLabel("page_applicationObject", "button_close"));
    $("[name='buttonAdd']").html(doc.getDocLabel("page_applicationObject", "button_add"));
}

/** Opens the screenshot preview. Called by the thumbnail via crbTableCellCallback. */
function aoV2ShowPicture(row) {
    var src = "ReadApplicationObjectImage?application=" + encodeURIComponent(row.application) +
        "&object=" + encodeURIComponent(row.object) + "&time=" + new Date().getTime();
    showPicture("screenshot", src);
}

/* -----------------------------------------------------------------------------
 * Copied from ApplicationObjectList.js. Duplicated rather than reused because
 * loading the V1 file too would register its initPage() as well, initialising
 * the page twice. Only the table-refresh call site differs from the original.
 * -------------------------------------------------------------------------- */

async function deleteEntryClick(application, object) {
    clearResponseMessageMainPage();
    var doc = new Doc();
    var messageComplete = doc.getDocLabel("page_applicationObject", "message_delete");
    messageComplete = messageComplete.replace("%ENTRY%", application + " - " + object);

    const result = await crbConfirmDelete({
        title: doc.getDocLabel("page_applicationObject", "button_delete"),
        html: messageComplete,
        preConfirm: async function () {
            try {
                const resp = await fetch("DeleteApplicationObject", {
                    method: "POST",
                    headers: {"Content-Type": "application/x-www-form-urlencoded"},
                    body: "application=" + encodeURIComponent(application) + "&object=" + encodeURIComponent(object)
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
        // was: fnDraw + fnPageChange('previous') on the DataTable
        var t = crbTableInstance(CRB_AO_TABLE_ID);
        if (t) {
            if (t.rows.length === 1 && t.page > 1) {
                t.goToPage(t.page - 1);
            } else {
                t.reload();
            }
        }
        showMessageMainPage("success", result.value.message, false);
    }
}

function buttonCloseHandler(event) {
    var modalID = event.data.extra;
    $(modalID).find("#application").attr("disabled", false);
    $(modalID)[0].reset();
    $(this).find('div.has-error').removeClass("has-error");
    clearResponseMessage($(modalID));

    window.dispatchEvent(new CustomEvent('ao-preview-reset'));
    imagePasteFromClipboard = undefined;
}

/** Consumed by the global AI bottom bar (aiBottomBar.html) to add a header button. */
function getAIHeaderButtons() {
    return [
        {
            label: "Find XPath",
            onClick: () => {
                window.dispatchEvent(new CustomEvent('open-ao', {detail: {}}));
            }
        }
    ];
}
