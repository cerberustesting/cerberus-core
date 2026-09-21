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
 * Application list - V2, on js/global/crbTable.js.
 *
 * Endpoint: ReadApplication (contentTable + table-level hasPermissions).
 * Its distinct-values mode is guarded by `request.getParameter("system") != null`
 * (ReadApplication.java:119), so the systems have to be part of distinctEndpoint
 * or every column filter comes back empty without saying why.
 *
 * Action gating is a 1:1 port of ApplicationList.js:166-227 (table-wide flag):
 *   - Edit/View  : always rendered; only the icon swaps on hasPermissions
 *   - Duplicate  : only when hasPermissions
 *   - Delete     : only when hasPermissions
 *
 * Fixed on the way over:
 *   - the delete handler was built as onclick="deleteEntryClick('"+escapeHtml(app)
 *     +"')". HTML-escaping does not protect a JS string inside an attribute, so an
 *     application named  x'); alert(1); //  ran. V2 passes the row to onClick.
 *   - a console.info(obj.type) left in the type renderer, firing once per row.
 *   - the type logo was an inline-styled <img> repeated per row; it now uses the
 *     shared .crb_srv_typelogo class, and a missing logo file no longer leaves a
 *     broken-image icon next to the label.
 * ========================================================================== */

$.when($.getScript("js/global/global.js")).then(function () {
    $(document).ready(function () {
        initPage();
        $('[data-toggle="popover"]').popover({placement: 'auto', container: 'body'});
    });
});

var CRB_APP_TABLE_ID = "applicationTableV2";

function initPage() {
    var doc = new Doc();

    displayPageLabel();

    // Cached by the application modal's Environment tab. Kept identical to V1.
    getSelectInvariant("ENVIRONMENT", true);
    getSelectInvariant("COUNTRY", true);

    createCerberusTable({
        id: CRB_APP_TABLE_ID,
        mount: "#applicationList",
        endpoint: "ReadApplication",
        // See the header note: distinct values are only served when a system is present.
        distinctEndpoint: "ReadApplication?q=1" + getUser().defaultSystemsQuery,
        rowKey: "application",
        defaultSort: {field: "application", dir: "asc"},
        pageLength: 10,
        searchPlaceholder: "Search applications...",
        emptyMessage: "No application matches your search",

        toolbar: function (ctx) {
            if (!ctx.hasPermissions) {
                return "";
            }
            return "<button id='createApplicationButton' type='button' " +
                "onclick=\"openModalApplication(undefined, 'ADD', 'ApplicationList')\" " +
                "class='bg-sky-400 hover:bg-sky-500 text-white flex items-center gap-1 px-3 py-1 rounded-lg h-10'>" +
                "<i data-lucide='plus' class='w-4 h-4'></i><span>" +
                doc.getDocLabel("page_application", "button_create") + "</span></button>";
        },

        // `field` = server column name (sName in V1), `prop` = key in the row.
        // Visibility is V1's: Application, Description, Type, System and Pool Size.
        columns: [
            {field: "application", title: doc.getDocOnline("application", "Application"),
             width: "180px", className: "font-medium", filterable: true},
            {field: "description", title: doc.getDocOnline("application", "Description"),
             width: "320px", like: true},
            {
                field: "type", title: doc.getDocOnline("application", "type"),
                width: "140px", filterable: true,
                render: function (row) { return appV2Type(row.type); }
            },
            {field: "system", title: doc.getDocOnline("application", "system"),
             width: "130px", filterable: true},
            {field: "poolSize", title: doc.getDocOnline("application", "poolSize"),
             width: "100px", className: "text-right tabular-nums"},

            // ---- available from Config, hidden by default ----
            {field: "sort", title: doc.getDocOnline("application", "sort"),
             width: "90px", visible: false, className: "text-right tabular-nums"},
            {field: "subsystem", title: doc.getDocOnline("application", "subsystem"),
             width: "140px", visible: false, filterable: true},
            {field: "bugTrackerConnector", title: doc.getDocOnline("application", "bugTrackerConnector"),
             width: "160px", visible: false, filterable: true},
            {
                field: "bugTrackerNewUrl", title: doc.getDocOnline("application", "bugtrackernewurl"),
                width: "240px", visible: false, like: true,
                render: function (row) { return drawURL(row.bugTrackerNewUrl); }
            },
            {
                field: "bugTrackerUrl", title: doc.getDocOnline("application", "bugtrackerurl"),
                width: "240px", visible: false, like: true,
                render: function (row) { return drawURL(row.bugTrackerUrl); }
            },
            {
                field: "repoUrl", title: doc.getDocOnline("application", "repourl"),
                width: "240px", visible: false, like: true,
                render: function (row) { return drawURL(row.repoUrl); }
            },
            {field: "deploytype", title: doc.getDocOnline("application", "deploytype"),
             width: "140px", visible: false, filterable: true},
            {field: "mavengroupid", title: doc.getDocOnline("application", "mavengroupid"),
             width: "180px", visible: false}
        ],

        actions: [
            {
                key: "edit", gate: "always",
                icon: function (row, ctx) { return ctx.hasPermissions ? "pencil" : "eye"; },
                title: function (row, ctx) {
                    return ctx.hasPermissions
                        ? doc.getDocLabel("page_application", "button_edit")
                        : doc.getDocLabel("page_application", "button_view");
                },
                onClick: function (row) { appV2OpenModal(row.application, "EDIT"); }
            },
            {
                key: "duplicate", icon: "copy", gate: "permission",
                title: doc.getDocLabel("page_application", "button_duplicate") || "Duplicate",
                onClick: function (row) { appV2OpenModal(row.application, "DUPLICATE"); }
            },
            {
                key: "delete", icon: "trash-2", gate: "permission", danger: true,
                title: doc.getDocLabel("page_application", "button_delete") || "Delete",
                onClick: function (row) { appV2DeleteEntryClick(row.application); }
            }
        ]
    });
}

/**
 * Opens the shared application modal.
 *
 * The name is URL-encoded on the way in, exactly as V1 did, because
 * feedApplicationModal() concatenates it straight into a query string
 * ("application=" + application, Application.html:466) without encoding it
 * itself. Most other callers in the app pass the raw name and so break on any
 * name needing escaping; encoding here is both faithful to V1 and correct.
 * Do not "simplify" this to the raw value without fixing the modal first.
 */
function appV2OpenModal(application, mode) {
    openModalApplication(encodeURIComponent(application), mode, "ApplicationList");
}

/**
 * Application type cell: the small platform logo plus its label.
 *
 * V1 built an <img> with six inline styles per row and no fallback, so an
 * unknown type showed the browser's broken-image glyph. The class is shared with
 * the Service Library list, and onerror hides the image instead.
 */
function appV2Type(type) {
    if (!type) {
        return "";
    }
    var safe = crbTableEscape(type);
    return '<span class="inline-flex items-center gap-2">' +
        '<img class="crb_srv_typelogo" alt="" src="./images/logoapp-' + encodeURIComponent(type) + '.png" ' +
        'onerror="this.style.display=\'none\'">' +
        '<span>' + safe + '</span></span>';
}

/**
 * Delete an application. Same dialog and same servlet as
 * ApplicationList.js:126-163; only the post-delete refresh changed, because the
 * DataTable it used to redraw does not exist on this page.
 */
async function appV2DeleteEntryClick(idApplication) {
    clearResponseMessageMainPage();
    var doc = new Doc();
    var messageComplete = doc.getDocLabel("page_application", "message_delete")
        .replace("%ENTRY%", idApplication);

    const result = await crbConfirmDelete({
        title: doc.getDocLabel("page_application", "button_delete") || 'Delete Application',
        html: messageComplete,
        confirmText: doc.getDocLabel("page_global", "btn_delete") || 'Delete',
        cancelText: doc.getDocLabel("page_global", "buttonClose") || 'Cancel',
        preConfirm: async () => {
            try {
                const resp = await $.post("DeleteApplication", {application: idApplication}, "json");
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
        var table = crbTableInstance(CRB_APP_TABLE_ID);
        if (table) {
            table.reload();
        }
        showMessageMainPage(getAlertType(result.value.messageType), result.value.message, false);
    }
}

/** V1's name, kept as an alias in case a shared modal ever calls it. */
var deleteEntryClick = appV2DeleteEntryClick;

function displayPageLabel() {
    var doc = new Doc();

    $("#pageTitle").html(doc.getDocLabel("page_application", "title"));
    $("#title").html(doc.getDocOnline("page_application", "title"));
    $("#applicationListLabel").html(doc.getDocLabel("page_application", "table_application"));
    $("[name='createApplicationField']").html(doc.getDocLabel("page_application", "button_create"));
    $("[name='confirmationField']").html(doc.getDocLabel("page_application", "button_delete"));
    $("[name='editApplicationField']").html(doc.getDocLabel("page_application", "button_edit"));
    $("[name='buttonAdd']").html(doc.getDocLabel("page_global", "buttonAdd"));
    $("[name='buttonEdit']").html(doc.getDocLabel("page_global", "buttonEdit"));
    $("[name='buttonClose']").html(doc.getDocLabel("page_global", "buttonClose"));
    $("[name='buttonConfirm']").html(doc.getDocLabel("page_global", "buttonConfirm"));
    $("[name='buttonDismiss']").html(doc.getDocLabel("page_global", "buttonDismiss"));
    $("#editAppDefinition").html(doc.getDocLabel("page_global", "tab_definition"));
    $("#editAppAdvanced").html(doc.getDocLabel("page_global", "tab_advanced"));
    $("[name='applicationField']").html(doc.getDocOnline("application", "Application"));
    $("[name='descriptionField']").html(doc.getDocOnline("application", "Description"));
    $("[name='typeField']").html(doc.getDocOnline("application", "type"));
    $("[name='systemField']").html(doc.getDocOnline("application", "system"));
    $("[name='poolSizeField']").html(doc.getDocOnline("application", "poolSize"));
    $("[name='subsystemField']").html(doc.getDocOnline("application", "subsystem"));
    $("[name='repourlField']").html(doc.getDocOnline("application", "repourl"));
    $("[name='bugtrackerurlField']").html(doc.getDocOnline("application", "bugtrackerurl"));
    $("[name='bugtrackernewurlField']").html(doc.getDocOnline("application", "bugtrackernewurl"));
    $("[name='deploytypeField']").html(doc.getDocOnline("application", "deploytype"));
    $("[name='mavengroupidField']").html(doc.getDocOnline("application", "mavengroupid"));

    $("[name='tabsEdit1']").html(doc.getDocOnline("page_application", "tabDef"));
    $("[name='tabsEdit2']").html(doc.getDocOnline("page_application", "tabEnv"));

    $("#environmentHeader").html(doc.getDocOnline("invariant", "ENVIRONMENT"));
    $("#countryHeader").html(doc.getDocOnline("invariant", "COUNTRY"));
    $("#ipHeader").html(doc.getDocOnline("countryenvironmentparameters", "IP")
            + '<br>' + doc.getDocOnline("countryenvironmentparameters", "URLLOGIN"));
    $("#urlHeader").html(doc.getDocOnline("countryenvironmentparameters", "URL")
            + '<br>' + doc.getDocOnline("countryenvironmentparameters", "domain"));
    $("#var1Header").html(doc.getDocOnline("countryenvironmentparameters", "Var1")
            + '<br>' + doc.getDocOnline("countryenvironmentparameters", "Var2"));
    $("#var3Header").html(doc.getDocOnline("countryenvironmentparameters", "Var3")
            + '<br>' + doc.getDocOnline("countryenvironmentparameters", "Var4"));
    $("#poolSizeHeader").html(doc.getDocOnline("countryenvironmentparameters", "poolSize"));
    $("#mobileData").html(doc.getDocOnline("countryenvironmentparameters", "mobileActivity")
            + '<br>' + doc.getDocOnline("countryenvironmentparameters", "mobilePackage"));

    displayFooter(doc);
    displayGlobalLabel(doc);
}
