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
 * Service Library list - V2, on js/global/crbTable.js.
 *
 * Action gating is a 1:1 port of AppServiceList.js:217-288 (table-wide
 * `hasPermissions`):
 *   - Edit/View  : always rendered; icon swaps pencil/eye, same handler
 *   - Duplicate  : only when hasPermissions
 *   - Delete     : only when hasPermissions
 *   - Test Cases : always rendered, no gate
 * Toolbar: Create is always rendered but `disabled` without permission
 * (legacy line 82), matching that page's convention.
 *
 * Two URL parameters this page must keep honouring:
 *   ?collection=X - pre-filters the list
 *   ?service=X    - opens that service's modal directly (deep link used from
 *                   Data Library and the V1 pages)
 * ========================================================================== */

$.when($.getScript("js/global/global.js")).then(function () {
    $(document).ready(function () {
        initPage();
    });
});

var CRB_SRV_TABLE_ID = "appServiceTableV2";

function initPage() {
    var doc = new Doc();

    displayPageLabel();

    // Application combo used by the service modal (AppServiceList.js:34).
    displayApplicationList("application", "", "", "");

    $('#testCaseListModal').on('hidden.bs.modal', getTestCasesUsingModalCloseHandler);

    var collectionUrl = GetURLParameter("collection");
    var initialFilters = null;
    if ((collectionUrl !== null) && (collectionUrl !== 'ALL')) {
        initialFilters = {"srv.collection": [collectionUrl]};
    }

    createCerberusTable({
        id: CRB_SRV_TABLE_ID,
        mount: "#soapLibraryList",
        endpoint: "ReadAppService",
        distinctEndpoint: "ReadAppService",
        rowKey: "service",
        defaultSort: {field: "srv.collection", dir: "asc"},
        pageLength: 10,
        searchPlaceholder: "Search services...",
        emptyMessage: "No service matches your search",
        // Declared as config rather than applied afterwards: the component does
        // not exist yet when createCerberusTable() returns.
        initialFilters: initialFilters,

        toolbar: function (ctx) {
            // Legacy always renders Create and only disables it (line 82).
            return "<button id='createSoapLibraryButton' type='button'" +
                (ctx.hasPermissions ? " onclick=\"openModalAppService(undefined, 'ADD')\"" : " disabled") +
                " class='bg-sky-400 hover:bg-sky-500 text-white flex items-center gap-1 px-3 py-1 rounded-lg h-10" +
                (ctx.hasPermissions ? "" : " opacity-40 cursor-not-allowed") + "'>" +
                "<i data-lucide='plus' class='w-4 h-4'></i><span>" +
                doc.getDocLabel("page_appservice", "button_create") + "</span></button>";
        },

        // `field` = server column name (sorting/filtering), `prop` = key in the row.
        columns: [
            {field: "srv.collection", prop: "collection", title: doc.getDocLabel("appservice", "collection"),
             width: "150px", filterable: true},
            {field: "srv.Service", prop: "service", title: doc.getDocLabel("appservice", "service"),
             width: "170px", className: "font-medium", filterable: true},
            {
                field: "srv.Type", prop: "type", title: doc.getDocLabel("appservice", "type"),
                width: "90px", filterable: true,
                render: function (row) {
                    if (!row.type) {
                        return "";
                    }
                    return '<img src="./images/logo-' + crbTableEscape(row.type) + '.png" ' +
                        'class="crb_srv_typelogo" alt="' + crbTableEscape(row.type) + '" ' +
                        'title="' + crbTableEscape(row.type) + '"/>';
                }
            },
            {field: "srv.Method", prop: "method", title: doc.getDocLabel("appservice", "method"),
             width: "90px", filterable: true},
            {field: "srv.Application", prop: "application", title: doc.getDocLabel("appservice", "application"),
             width: "140px", filterable: true},
            {field: "ServicePath", prop: "servicePath", title: doc.getDocLabel("appservice", "servicePath"),
             width: "220px", like: true},
            // Legacy declares "srv.Description" twice (AppServiceList.js:357 and
            // again near the end); the duplicate is dropped here - it only ever
            // rendered the same value in two columns.
            {field: "srv.Description", prop: "description", title: doc.getDocLabel("appservice", "description"),
             width: "200px", like: true},
            {field: "BodyType", prop: "bodyType", title: doc.getDocLabel("appservice", "bodyType"),
             width: "110px", visible: false},
            {
                field: "srv.ServiceRequest", prop: "serviceRequest",
                title: doc.getDocLabel("appservice", "srvRequest"),
                width: "300px", visible: false, like: true,
                render: function (row) { return srvV2Code(row.serviceRequest); }
            },
            {field: "srv.operation", prop: "operation", title: doc.getDocLabel("appservice", "operation"),
             width: "140px", visible: false, like: true},
            {field: "srv.authType", prop: "authType", title: doc.getDocLabel("appservice", "authType"),
             width: "120px", visible: false, filterable: true},
            {field: "srv.kafkaTopic", prop: "kafkaTopic", title: doc.getDocLabel("appservice", "kafkaTopic"),
             width: "140px", visible: false},
            {field: "srv.kafkaKey", prop: "kafkaKey", title: doc.getDocLabel("appservice", "kafkaKey"),
             width: "140px", visible: false},
            {field: "srv.kafkaFilterPath", prop: "kafkaFilterPath",
             title: doc.getDocLabel("appservice", "kafkaFilterPath"), width: "150px", visible: false},
            {field: "srv.kafkaFilterValue", prop: "kafkaFilterValue",
             title: doc.getDocLabel("appservice", "kafkaFilterValue"), width: "150px", visible: false},
            {field: "srv.kafkaFilterHeaderPath", prop: "kafkaFilterHeaderPath",
             title: doc.getDocLabel("appservice", "kafkaFilterHeaderPath"), width: "160px", visible: false},
            {field: "srv.kafkaFilterHeaderValue", prop: "kafkaFilterHeaderValue",
             title: doc.getDocLabel("appservice", "kafkaFilterHeaderValue"), width: "160px", visible: false},
            {field: "srv.isAvroEnable", prop: "isAvroEnable",
             title: doc.getDocLabel("appservice", "isAvroEnable"), width: "120px", visible: false},
            {field: "srv.schemaRegistryURL", prop: "schemaRegistryURL",
             title: doc.getDocLabel("appservice", "schemaRegistryURL"), width: "180px", visible: false},
            {field: "srv.isAvroEnableKey", prop: "isAvroEnableKey",
             title: doc.getDocLabel("appservice", "isAvroEnableKey"), width: "140px", visible: false},
            {field: "srv.avroSchemaKey", prop: "avroSchemaKey",
             title: doc.getDocLabel("appservice", "avroSchemaKey"), width: "160px", visible: false},
            {field: "srv.isAvroEnableValue", prop: "isAvroEnableValue",
             title: doc.getDocLabel("appservice", "isAvroEnableValue"), width: "150px", visible: false},
            {field: "srv.avroSchemaValue", prop: "avroSchemaValue",
             title: doc.getDocLabel("appservice", "avroSchemaValue"), width: "160px", visible: false},
            {field: "srv.parentContentService", prop: "parentContentService",
             title: doc.getDocLabel("appservice", "parentContentService"), width: "170px", visible: false},
            {
                field: "srv.dateCreated", prop: "dateCreated",
                title: doc.getDocOnline("transversal", "DateCreated"), width: "150px", visible: false,
                render: function (row) { return crbTableEscape(getDate(row.dateCreated)); }
            },
            {field: "srv.usrCreated", prop: "usrCreated", title: doc.getDocOnline("transversal", "UsrCreated"),
             width: "120px", visible: false},
            {
                field: "srv.dateModif", prop: "dateModif",
                title: doc.getDocOnline("transversal", "DateModif"), width: "150px", visible: false,
                render: function (row) { return crbTableEscape(getDate(row.dateModif)); }
            },
            {field: "srv.usrModif", prop: "usrModif", title: doc.getDocOnline("transversal", "UsrModif"),
             width: "120px", visible: false}
        ],

        actions: [
            {
                key: "edit", gate: "always",
                icon: function (row, ctx) { return ctx.hasPermissions ? "pencil" : "eye"; },
                title: doc.getDocLabel("page_appservice", "button_edit"),
                onClick: function (row) { openModalAppService(row.service, "EDIT"); }
            },
            {
                key: "duplicate", icon: "copy", gate: "permission",
                title: doc.getDocLabel("page_testdatalib", "tooltip_duplicateEntry"),
                onClick: function (row) { openModalAppService(row.service, "DUPLICATE"); }
            },
            {
                key: "delete", icon: "trash-2", gate: "permission", danger: true,
                title: doc.getDocLabel("page_appservice", "button_delete"),
                onClick: function (row) { deleteEntryClick(row.service); }
            },
            {
                key: "testcases", icon: "list", gate: "always",
                title: doc.getDocLabel("page_testdatalib", "tooltip_gettestcases"),
                onClick: function (row) { getTestCasesUsingService(row.service); }
            }
        ]
    });

    // Deep link: ?service=X opens that service's modal straight away.
    // NOTE: clearAppServiceUrlParam() in include/transversal/AppService.html strips
    // the parameter once the modal closes, so a later "Add Service" does not
    // inherit this service's identity - keep that behaviour intact.
    var appServiceUrl = GetURLParameter("service");
    if ((appServiceUrl !== null) && (appServiceUrl !== 'ALL')) {
        openModalAppService(appServiceUrl, "EDIT", undefined);
    }
}

function displayPageLabel() {
    var doc = new Doc();
    $("#title").html(doc.getDocLabel("page_appservice", "title"));
    $("#pageTitle").html(doc.getDocLabel("page_appservice", "title"));
    displayFooter(doc);
    displayGlobalLabel(doc);
}

/** One-line clipped preview for the request body column. */
function srvV2Code(value) {
    if (!value) {
        return "";
    }
    return '<pre class="crb_tdl_code" title="' + crbTableEscape(value) + '">' +
        crbTableEscape(value) + '</pre>';
}

/* -----------------------------------------------------------------------------
 * Copied from AppServiceList.js. Duplicated rather than reused because loading
 * the V1 file too would register its initPage() as well, initialising the page
 * twice. Only the table-refresh call site differs from the original.
 * -------------------------------------------------------------------------- */

function removeEntryClick(service) {
    deleteEntryClick(service);
}

/** Clears the accordion when the "test cases using" modal closes. */
function getTestCasesUsingModalCloseHandler() {
    $('#testCaseListModal #testCaseListGroup').empty();
}

/** Loads and renders the "test cases using this service" accordion. Verbatim from V1. */
function getTestCasesUsingService(service) {
    clearResponseMessageMainPage();
    showLoaderInModal('#testCaseListModal');
    var jqxhr = $.getJSON("ReadAppService", "service=" + service + "&testcase=Y");

    var doc = new Doc();

    $("#testCaseListModalLabel").text("List of test cases affected by the service : " + service);

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

/**
 * getDocLabel() returns the placeholder string "<key> -- Missing doc !!!" when a
 * label is absent from the documentation table, rather than an empty value - so
 * V1's `doc.getDocLabel(...) || "fallback"` never fires and users are shown the
 * raw placeholder in the delete dialog. Detect the marker explicitly.
 */
function srvV2Label(group, key, fallback) {
    var v = new Doc().getDocLabel(group, key);
    if (!v || /Missing doc/i.test(v)) {
        return fallback;
    }
    return v;
}

async function deleteEntryClick(service) {
    clearResponseMessageMainPage();
    var doc = new Doc();
    var messageComplete = srvV2Label("page_appservice", "message_delete",
        "Do you really want to delete the service '%ENTRY%' ?");
    messageComplete = messageComplete.replace("%ENTRY%", service);

    const result = await crbConfirmDelete({
        title: doc.getDocLabel("page_appservice", "button_delete"),
        html: messageComplete,
        confirmText: doc.getDocLabel("page_global", "btn_delete") || 'Delete',
        cancelText: doc.getDocLabel("page_global", "buttonClose") || 'Cancel',
        preConfirm: async () => {
            try {
                const resp = await fetch("DeleteAppService?service=" + encodeURIComponent(service), {
                    method: "GET"
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
        // was: fnDraw on the DataTable
        var t = crbTableInstance(CRB_SRV_TABLE_ID);
        if (t) {
            if (t.rows.length === 1 && t.page > 1) {
                t.goToPage(t.page - 1);
            } else {
                t.reload();
            }
        }
        showMessageMainPage("success", result.value.message || "Service deleted successfully", false);
    }
}
