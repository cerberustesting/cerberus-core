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
$.when($.getScript("js/global/global.js")).then(function () {
    $(document).ready(function () {
        initPage();
        $('[data-toggle="popover"]').popover({
            'placement': 'auto',
            'container': 'body'
        });
    });
});

function initPage() {
    displayPageLabel();

    // Load Application Combo (used on service modal).
    displayApplicationList("application", "", "", "");

    // configure and create the dataTable
    var configurations = new TableConfigurationsServerSide("soapLibrarysTable", "ReadAppService", "contentTable", aoColumnsFunc("soapLibrarysTable"), [1, 'asc']);
    createDataTableWithPermissionsNew(configurations, renderOptionsForAppService, "#soapLibraryList", undefined, true);

    $('#soapLibrarysTable').on('draw.dt', function() {
        $(this).find('tbody tr').addClass('group');
        if (window.lucide) lucide.createIcons();
    });

    $('#testCaseListModal').on('hidden.bs.modal', getTestCasesUsingModalCloseHandler);
    
    
    // Colleection can be filtered from URL
    var collectionUrl = GetURLParameter("collection");
    var searchArray = [];
    var searchObject = {param: "col", values: "val"};
    if ((collectionUrl !== null) && (collectionUrl !== 'ALL')) {
        searchObject = {param: "collection", values: collectionUrl};
        searchArray.push(searchObject);
    }
    if (searchArray.length > 0) {
        applyFiltersOnMultipleColumns("soapLibrarysTable", searchArray, false);
    }
   
    // Directly open the service when URL include service parameter on query string.
    var appServiceUrl = GetURLParameter("service");
    if ((appServiceUrl !== null) && (appServiceUrl !== 'ALL')) {
        openModalAppService(appServiceUrl, "EDIT", undefined) 
    }
}

function displayPageLabel() {
    var doc = new Doc();

    $("#title").html(doc.getDocLabel("page_appservice", "title"));
    $("#pageTitle").html(doc.getDocLabel("page_appservice", "title"));
    //displayHeaderLabel(doc);
    displayFooter(doc);
    displayGlobalLabel(doc);

}

function renderOptionsForAppService(data) {
    var doc = new Doc();

    if ($("#createSoapLibraryButton").length === 0) {
        var disabledCreate = data["hasPermissions"] ? "" : "disabled";

        var contentToAdd = `
            <button id='createSoapLibraryButton' type='button'
                class='bg-sky-400 hover:bg-sky-500 flex items-center space-x-1 px-3 py-1 rounded-lg h-10 w-auto'
                ${disabledCreate}>
                <i data-lucide="plus" class="w-4 h-4"></i>
                <span>${doc.getDocLabel("page_appservice", "button_create")}</span>
            </button>
        `;

        var $wrapper = $("#soapLibrarysTable_buttonWrapper");
        if ($wrapper.length) {
            $wrapper.append(contentToAdd);
            if (window.lucide) lucide.createIcons();
        } else {
            $("#soapLibrarysTable_wrapper #soapLibrarysTable_length").before("<div id='soapLibrarysTable_buttonWrapper' class='flex w-full gap-2'>" + contentToAdd + "</div>");
            if (window.lucide) lucide.createIcons();
        }

        if (data["hasPermissions"]) {
            $("#createSoapLibraryButton").off("click").on("click", function () {
                openModalAppService(undefined, "ADD");
            });
        }
    }
}

function removeEntryClick(service) {
    deleteEntryClick(service);
}

/**
 * Handler that cleans the test case list modal when it is closed
 */
function getTestCasesUsingModalCloseHandler() {
    //we need to clear the accordion items that were inserted
    $('#testCaseListModal #testCaseListGroup').empty();
}

/**
 * Function that loads all test cases that are associated with the selected entry
 * @param {type} service service name
 */
function getTestCasesUsingService(service) {
    clearResponseMessageMainPage();
    showLoaderInModal('#testCaseListModal');
    var jqxhr = $.getJSON("ReadAppService", "service=" + service + "&testcase=Y");

    var doc = new Doc();

    $("#testCaseListModalLabel").text("List of test cases affected by the service : " + service)

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

function aoColumnsFunc(tableId) {
    var doc = new Doc();
    var aoColumns = [
        {
            "data": null,
            "bSortable": false,
            "bSearchable": false,
            "sWidth": "150px",
            "title": doc.getDocLabel("page_global", "columnAction"),
            "mRender": function (data, type, obj, meta) {
                var hasPermissions = ($("#" + tableId).attr("hasPermissions") === "true");
                var row = "row_" + (meta ? meta.row : 0);

                const baseBtnClass = "inline-flex aspect-square h-8 w-8 items-center justify-center rounded-md transition-all duration-200 " +
                    "text-slate-500 hover:bg-slate-200 dark:hover:bg-slate-800 " +
                    "opacity-20 group-hover:opacity-100 [&_svg]:size-4";

                function actionButton({ id, name, title, onClick, icon, extraClass = "", disabled = false }) {
                    const disabledClass = disabled ? "opacity-30 cursor-not-allowed" : "";
                    return `
                        <button id="${id}" name="${name}" type="button"
                            class="${baseBtnClass} ${extraClass} ${disabledClass}"
                            title="${title}"
                            ${disabled ? "disabled" : `onclick="${onClick}"`}>
                            ${icon}
                        </button>`;
                }

                const icons = {
                    edit: `<i data-lucide="pencil" class="w-4 h-4"></i>`,
                    view: `<i data-lucide="eye" class="w-4 h-4"></i>`,
                    duplicate: `<i data-lucide="copy" class="w-4 h-4"></i>`,
                    delete: `<i data-lucide="trash-2" class="w-4 h-4"></i>`,
                    list: `<i data-lucide="list" class="w-4 h-4"></i>`
                };

                let buttons = [];

                // Edit / View
                buttons.push(actionButton({
                    id: `editService_${row}`,
                    name: "editAppService",
                    title: doc.getDocLabel("page_appservice", "button_edit"),
                    onClick: `openModalAppService('${obj["service"]}', 'EDIT')`,
                    icon: hasPermissions ? icons.edit : icons.view
                }));

                // Duplicate
                if (hasPermissions) {
                    buttons.push(actionButton({
                        id: `duplicateService_${row}`,
                        name: "duplicateAppService",
                        title: doc.getDocLabel("page_testdatalib", "tooltip_duplicateEntry"),
                        onClick: `openModalAppService('${obj["service"]}', 'DUPLICATE')`,
                        icon: icons.duplicate
                    }));
                }

                // Delete
                if (hasPermissions) {
                    buttons.push(actionButton({
                        id: `deleteService_${row}`,
                        name: "deleteAppService",
                        title: doc.getDocLabel("page_appservice", "button_delete"),
                        onClick: `deleteEntryClick('${obj["service"]}')`,
                        icon: icons.delete,
                        extraClass: "group-hover:!text-red-500"
                    }));
                }

                // View Test Cases
                buttons.push(actionButton({
                    id: `viewTestCases_${row}`,
                    name: "getTestCasesUsing",
                    title: doc.getDocLabel("page_testdatalib", "tooltip_gettestcases"),
                    onClick: `getTestCasesUsingService('${obj.service}')`,
                    icon: icons.list
                }));

                return '<div class="flex items-center gap-0.5">' + buttons.join('') + '</div>';
            }
        },
        {
            "sName": "srv.collection",
            "data": "collection",
            "sWidth": "150px",
            "title": doc.getDocLabel("appservice", "collection")
        }, {
            "sName": "srv.Service",
            "data": "service",
            "sWidth": "150px",
            "title": doc.getDocLabel("appservice", "service")
        },
        {
            "sName": "srv.Type",
            "data": "type",
            "sWidth": "50px",
            "title": doc.getDocLabel("appservice", "type"),
            "mRender": function (data, type, obj) {
                return $("<div></div>")
                        .append($("<img style='height:20px; overflow:hidden; text-overflow:clip; border: 0px; padding:0; margin:0'></img>").text(obj.type).attr('src', './images/logo-' + obj.type + '.png'))
                        .html();
            }
        }, {
            "sName": "srv.Method",
            "visible": true,
            "data": "method",
            "sWidth": "50px",
            "title": doc.getDocLabel("appservice", "method")
        }, {
            "sName": "srv.Application",
            "data": "application",
            "sWidth": "150px",
            "title": doc.getDocLabel("appservice", "application")
        }, {
            "sName": "ServicePath",
            "like": true,
            "data": "servicePath",
            "sWidth": "150px",
            "title": doc.getDocLabel("appservice", "servicePath")
        }, {
            "sName": "BodyType",
            "visible": false,
            "data": "bodyType",
            "sWidth": "70px",
            "title": doc.getDocLabel("appservice", "bodyType")
        }, {
            "sName": "srv.ServiceRequest",
            "visible": false,
            "like": true,
            "data": "serviceRequest",
            "title": doc.getDocLabel("appservice", "srvRequest"),
            "sWidth": "350px",
            "mRender": function (data, type, obj) {
                return $("<div></div>")
                        .append(
                                $(
                                        "<pre name='envelopeField' style='height:20px; overflow:hidden; text-overflow:clip; border: 0px; padding:0; margin:0'></pre>")
                                .text(obj['serviceRequest']))
                        .html();
            }
        }, {
            "sName": "srv.operation",
            "visible": false,
            "like": true,
            "data": "operation",
            "sWidth": "150px",
            "title": doc.getDocLabel("appservice", "operation")
        }, {
            "sName": "srv.Description",
            "like": true,
            "data": "description",
            "sWidth": "200px",
            "title": doc.getDocLabel("appservice", "description")
        }, {
            "sName": "srv.authType",
            "visible": false,
            "data": "authType",
            "sWidth": "100px",
            "title": doc.getDocLabel("appservice", "authType")
        }, {
            "sName": "srv.kafkaTopic",
            "visible": false,
            "data": "kafkaTopic",
            "sWidth": "150px",
            "title": doc.getDocLabel("appservice", "kafkaTopic")
        }, {
            "sName": "srv.kafkaKey",
            "visible": false,
            "data": "kafkaKey",
            "sWidth": "150px",
            "title": doc.getDocLabel("appservice", "kafkaKey")
        }, {
            "sName": "srv.kafkaFilterPath",
            "visible": false,
            "data": "kafkaFilterPath",
            "sWidth": "150px",
            "title": doc.getDocLabel("appservice", "kafkaFilterPath")
        }, {
            "sName": "srv.kafkaFilterValue",
            "visible": false,
            "data": "kafkaFilterValue",
            "sWidth": "150px",
            "title": doc.getDocLabel("appservice", "kafkaFilterValue")
        }, {
            "sName": "srv.kafkaFilterHeaderPath",
            "visible": false,
            "data": "kafkaFilterHeaderPath",
            "sWidth": "150px",
            "title": doc.getDocLabel("appservice", "kafkaFilterHeaderPath")
        }, {
            "sName": "srv.kafkaFilterHeaderValue",
            "visible": false,
            "data": "kafkaFilterHeaderValue",
            "sWidth": "150px",
            "title": doc.getDocLabel("appservice", "kafkaFilterHeaderValue")
        }, {
            "sName": "srv.isAvroEnable",
            "visible": false,
            "data": "isAvroEnable",
            "sWidth": "50px",
            "title": doc.getDocLabel("appservice", "isAvroEnable")
        }, {
            "sName": "srv.schemaRegistryURL",
            "visible": false,
            "data": "schemaRegistryURL",
            "sWidth": "150px",
            "title": doc.getDocLabel("appservice", "schemaRegistryURL")
        }, {
            "sName": "srv.isAvroEnableKey",
            "visible": false,
            "data": "isAvroEnableKey",
            "sWidth": "50px",
            "title": doc.getDocLabel("appservice", "isAvroEnableKey")
        }, {
            "sName": "srv.avroSchemaKey",
            "visible": false,
            "data": "avroSchemaKey",
            "sWidth": "150px",
            "title": doc.getDocLabel("appservice", "avroSchemaKey")
        }, {
            "sName": "srv.isAvroEnableValue",
            "visible": false,
            "data": "isAvroEnableValue",
            "sWidth": "50px",
            "title": doc.getDocLabel("appservice", "isAvroEnableValue")
        }, {
            "sName": "srv.avroSchemaValue",
            "visible": false,
            "data": "avroSchemaValue",
            "sWidth": "150px",
            "title": doc.getDocLabel("appservice", "avroSchemaValue")
        }, {
            "sName": "srv.parentContentService",
            "visible": false,
            "data": "parentContentService",
            "sWidth": "150px",
            "title": doc.getDocLabel("appservice", "parentContentService")
        }, {
            "sName": "srv.Description",
            "like": true,
            "data": "description",
            "sWidth": "200px",
            "title": doc.getDocLabel("appservice", "description")
        }, {
            "sName": "srv.dateCreated",
            "visible": false,
            "like": true,
            "data": "dateCreated",
            "sWidth": "150px",
            "title": doc.getDocOnline("transversal", "DateCreated"),
            "mRender": function (data, type, oObj) {
                return getDate(oObj["DateCreated"]);
            }
        }, {
            "sName": "srv.usrCreated",
            "visible": false,
            "data": "usrCreated",
            "sWidth": "70px",
            "title": doc.getDocOnline("transversal", "UsrCreated")
        }, {
            "sName": "srv.dateModif",
            "visible": false,
            "like": true,
            "data": "dateModif",
            "sWidth": "150px",
            "title": doc.getDocOnline("transversal", "DateModif"),
            "mRender": function (data, type, oObj) {
                return getDate(oObj["DateModif"]);
            }
        }, {
            "sName": "srv.usrModif",
            "visible": false,
            "data": "usrModif",
            "sWidth": "70px",
            "title": doc.getDocOnline("transversal", "UsrModif")
        }];
    return aoColumns;
}

async function deleteEntryClick(service) {
    clearResponseMessageMainPage();
    var doc = new Doc();
    var messageComplete = doc.getDocLabel("page_appservice", "message_delete") || "Are you sure you want to delete this service?";
    messageComplete = messageComplete.replace("%ENTRY%", service);

    const result = await crbConfirmDelete({
        title: doc.getDocLabel("page_appservice", "title_remove") || "Delete Service",
        html: messageComplete,
        confirmText: doc.getDocLabel("page_global", "btn_delete") || "Delete",
        cancelText: doc.getDocLabel("page_global", "buttonClose") || "Cancel",
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
        var oTable = $("#soapLibrarysTable").dataTable();
        oTable.fnDraw(false);
        if (oTable.fnGetData().length === 1) {
            oTable.fnPageChange('previous');
        }
        notifyInPage("success", result.value.message || "Service deleted successfully");
    }
}
