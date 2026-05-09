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
    //we need to clear the item-groups that were inserted
    $('#testCaseListModal #testCaseListGroup a[id*="cat"]').remove();
    $('#testCaseListModal #testCaseListGroup div[id*="sub_cat"]').remove();
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

        $('#testCaseListModal #totalTestCases').text(doc.getDocLabel("page_testdatalib_m_gettestcases", "nrTests") + " " + result["TestCasesList"].length);

        var htmlContent = "";

        $.each(result["TestCasesList"], function (idx, obj) {

            var item = `<div x-data="{open:false}" class="border-b">
                <b>
                    <a @click="open=!open"
                       class="flex justify-between items-center cursor-pointer px-4 py-3 hover:bg-gray-100">
                        <span>${obj[0]}</span>
                        <span class="ml-6 text-sm text-gray-600">
                            ${doc.getDocLabel("page_testdatalib_m_gettestcases", "nrTestCases")}${obj[2]}
                        </span>
                        <span class="transition-transform"
                              :class="{'rotate-180': open}"><i class="fa fa-chevron-down"></i>
                        </span>
                    </a>
                </b>
                <div x-show="open" x-collapse class="bg-gray-50">
            `;

            htmlContent += item;

            $.each(obj[3], function (idx2, obj2) {
                var hrefTest = 'TestCaseScript.jsp?test=' + obj[0] + '&testcase=' + obj2.TestCaseNumber;
                htmlContent += `
                <div class="px-16 py-3 border-t">
                    <div>
                        <a href="${hrefTest}" target="_blank" class="font-medium hover:underline">
                            ${obj2.TestCaseNumber} - ${obj2.TestCaseDescription}
                        </a>
                    </div>
        
                    <div class="text-sm text-gray-600 mt-1">
                        ${doc.getDocLabel("testcase", "Creator")}: ${obj2.Creator} |
                        ${doc.getDocLabel("testcase", "IsActive")}: ${obj2.Active} |
                        ${doc.getDocLabel("testcase", "Status")}: ${obj2.Status} |
                        ${doc.getDocLabel("invariant", "TESTCASE_TYPE")}: ${obj2.Group} |
                        ${doc.getDocLabel("application", "Application")}: ${obj2.Application}
                    </div>
                </div>
                `;
            });

            htmlContent += `</div></div>`;
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
