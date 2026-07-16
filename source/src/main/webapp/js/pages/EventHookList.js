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
            'container': 'body'}
        );
    });
});

function initPage() {
    displayPageLabel();

    //configure and create the dataTable
    var configurations = new TableConfigurationsServerSide("eventHooksTable", "ReadEventHook", "contentTable", aoColumnsFunc(), [1, 'asc']);
    createDataTableWithPermissionsNew(configurations, renderOptionsForEventHook, "#eventHookList", undefined, true);
    refreshPopoverDocumentation("eventHookList");

    // action buttons reveal on row hover + lucide icons in cells
    $('#eventHooksTable').on('draw.dt', function () {
        $(this).find('tbody tr').addClass('group');
        if (window.lucide) {
            lucide.createIcons();
        }
    });
}

function displayPageLabel() {
    var doc = new Doc();

    $("#pageTitle").html(doc.getDocLabel("page_eventhook", "title"));
    $("#title").html(doc.getDocOnline("page_eventhook", "title"));

    displayInvariantList("eventReference", "EVENTHOOK", false, "CAMPAIGN_END");
    displayInvariantList("hookConnector", "EVENTCONNECTOR", false, "EMAIL");



   // displayHeaderLabel(doc);

    displayFooter(doc);
    displayGlobalLabel(doc);
}

function renderOptionsForEventHook(data) {

    var doc = new Doc();
    if ($("#createEventHookButton").length === 0) {
        var contentToAdd = `
            <button id='createEventHookButton' type='button'
                class='text-white bg-sky-400 hover:bg-sky-500 flex items-center space-x-1 px-3 py-1 rounded-md mr-2 h-10 w-auto'>
                <i data-lucide="plus" class="w-4 h-4"></i>
                <span>` + doc.getDocLabel("page_global", "btn_add") + `</span>
            </button>`;

        var $wrapper = $("#eventHooksTable_buttonWrapper");
        if ($wrapper.length) {
            $wrapper.prepend(contentToAdd);
        } else {
            $("#eventHooksTable_wrapper div#eventHooksTable_length").before("<div id='eventHooksTable_buttonWrapper'>" + contentToAdd + "</div>");
        }
        if (window.lucide) {
            lucide.createIcons();
        }

        $("#createEventHookButton").off("click").on("click", function () {
            openModalEventHook(0, "ADD");
        });
    }

}

function deleteEventHookHandlerClick() {
    var idLabel = $('#confirmationModal').find('#hiddenField1').prop("value");
    var jqxhr = $.post("DeleteEventHook", {id: idLabel}, "json");
    $.when(jqxhr).then(function (data) {
        var messageType = getAlertType(data.messageType);
        if (messageType === "success") {
            // Redraw the datatable
            var oTable = $("#eventHooksTable").dataTable();
            oTable.fnDraw(false);
        }
        // Show message in the main page
        showMessageMainPage(messageType, data.message, false);
        //close confirmation window
        $('#confirmationModal').modal('hide');
    }).fail(handleErrorAjaxAfterTimeout);
}

function deleteEventHook(id) {
    clearResponseMessageMainPage();
    var doc = new Doc();
    var messageComplete = doc.getDocLabel("page_global", "message_delete");
    messageComplete = messageComplete.replace("%ENTRY%", id);
    messageComplete = messageComplete.replace("%TABLE%", " ID Event Hook ");
    showModalConfirmation(deleteEventHookHandlerClick, undefined, doc.getDocLabel("page_global", "btn_delete"), messageComplete, id, "", "", "");
}


function aoColumnsFunc(tableId) {
    var doc = new Doc();
    var aoColumns = [
        {
            "data": null,
            "bSortable": false,
            "bSearchable": false,
            "sWidth": "80px",
            "title": doc.getDocLabel("page_global", "columnAction"),
            "mRender": function (data, type, obj, meta) {
                var row = "row_" + meta.row;

                const baseBtnClass = "inline-flex aspect-square h-8 w-8 items-center justify-center rounded-md transition-all duration-200 " +
                    "text-slate-500 hover:bg-slate-200 dark:hover:bg-slate-800 " +
                    "opacity-20 group-hover:opacity-100 [&_svg]:size-4";

                function actionButton(id, title, onClick, icon, extraClass) {
                    return '<button id="' + id + '" type="button" class="' + baseBtnClass + ' ' + (extraClass || '') + '" title="' + title + '" onclick="' + onClick + '">' +
                        '<i data-lucide="' + icon + '" class="w-4 h-4"></i></button>';
                }

                var buttons = "";
                buttons += actionButton('eventhook_action_edit_' + row, doc.getDocLabel("page_global", "btn_edit"),
                    "openModalEventHook('" + obj["id"] + "','EDIT');", 'pencil', 'group-hover:!text-blue-500');
                buttons += actionButton('eventhook_action_duplicate_' + row, doc.getDocLabel("page_global", "btn_duplicate"),
                    "openModalEventHook('" + obj["id"] + "','DUPLICATE');", 'copy', 'group-hover:!text-purple-500');
                buttons += actionButton('eventhook_action_delete_' + row, doc.getDocLabel("page_global", "btn_delete"),
                    "deleteEventHook('" + obj["id"] + "');", 'trash-2', 'group-hover:!text-red-500');

                return '<div class="flex items-center justify-start gap-1">' + buttons + '</div>';
            }
        },
        {
            "data": "eventReference",
            "sName": "evh.eventReference",
            "sWidth": "150px",
            "title": doc.getDocLabel("page_eventhook", "eventReference")
        },
        {
            "data": "objectKey1",
            "sName": "evh.objectKey1",
            "sWidth": "100px",
            "title": doc.getDocLabel("page_eventhook", "objectKey1")
        },
        {
            "data": "objectKey2",
            "sName": "evh.objectKey2",
            "sWidth": "100px",
            "title": doc.getDocLabel("page_eventhook", "objectKey2")
        },
        {
            "data": "isActive",
            "sName": "evh.isActive",
            "sWidth": "80px",
            "title": doc.getDocLabel("page_eventhook", "isActive"),
            "mRender": function (data, type, obj) {
                var active = (data === true || data === "true" || data === "Y");
                var chip = active
                    ? "bg-green-50 text-green-700 ring-green-600/20 dark:bg-green-900/30 dark:text-green-300"
                    : "bg-slate-50 text-slate-500 ring-slate-500/20 dark:bg-slate-800 dark:text-slate-400";
                return '<span class="inline-flex items-center gap-1.5 rounded-full px-2.5 py-1 text-xs font-semibold ring-1 ring-inset ' + chip + '">'
                    + '<i data-lucide="' + (active ? 'circle-check' : 'circle-pause') + '" class="h-3.5 w-3.5"></i>'
                    + '<span>' + (active ? 'Active' : 'Inactive') + '</span></span>';
            }
        },
        {
            "data": "hookConnector",
            "sName": "evh.hookConnector",
            "sWidth": "100px",
            "title": doc.getDocLabel("page_eventhook", "hookConnector"),
            "mRender": function (data, type, obj) {
                if (isEmpty(data)) {
                    return "";
                }
                return '<span class="inline-flex items-center rounded px-1.5 py-0.5 text-[11px] font-medium bg-sky-50 text-sky-700 dark:bg-sky-900/30 dark:text-sky-300">' + escapeHtml(data) + '</span>';
            }
        },
        {
            "data": "hookRecipient",
            "sName": "evh.hookRecipient",
            "visible": true,
            "sWidth": "300px",
            "title": doc.getDocLabel("page_eventhook", "hookRecipient")
        },
        {
            "data": "description",
            "like": true,
            "sName": "evh.description",
            "sWidth": "200px",
            "title": doc.getDocLabel("page_eventhook", "description")
        },
        {
            "data": "dateCreated",
            "sName": "evh.dateCreated",
            "visible": false,
            "like": true,
            "title": doc.getDocOnline("transversal", "DateCreated"),
            "mRender": function (data, type, oObj) {
                return getDate(oObj["dateCreated"]);
            },
            "sWidth": "150px",
            "sDefaultContent": ""
        },
        {
            "data": "usrCreated",
            "sName": "evh.usrCreated",
            "visible": false,
            "title": doc.getDocOnline("transversal", "UsrCreated"),
            "sWidth": "100px",
            "sDefaultContent": ""
        },
        {
            "data": "dateModif",
            "visible": false,
            "like": true,
            "sName": "evh.dateModif",
            "title": doc.getDocOnline("transversal", "DateModif"),
            "sWidth": "150px",
            "sDefaultContent": "",
            "mRender": function (data, type, oObj) {
                return getDate(oObj["dateModif"]);
            }
        },
        {
            "data": "usrModif",
            "visible": false,
            "sName": "evh.usrModif",
            "title": doc.getDocOnline("transversal", "UsrModif"),
            "sWidth": "100px",
            "sDefaultContent": ""
        }
    ];
    return aoColumns;
}
