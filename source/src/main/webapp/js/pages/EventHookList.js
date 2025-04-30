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
    createDataTableWithPermissions(configurations, renderOptionsForEventHook, "#eventHookList", undefined, true);
    refreshPopoverDocumentation("eventHookList");
}

function displayPageLabel() {
    var doc = new Doc();

    $("#pageTitle").html(doc.getDocLabel("page_eventhook", "title"));
    $("#title").html(doc.getDocOnline("page_eventhook", "title"));

    displayInvariantList("eventReference", "EVENTHOOK", false, "CAMPAIGN_END");
    displayInvariantList("hookConnector", "EVENTCONNECTOR", false, "EMAIL");



    displayHeaderLabel(doc);

    displayFooter(doc);
    displayGlobalLabel(doc);
}

function renderOptionsForEventHook(data) {

    var doc = new Doc();
    if ($("#createEventHookButton").length === 0) {
        var contentToAdd = "<div class='marginBottom10'><button id='createEventHookButton' type='button' class='btn btn-default'>\n\
            <span class='glyphicon glyphicon-plus-sign'></span> "
                + doc.getDocLabel("page_global", "btn_add") + "</button></div>";

        $("#eventHooksTable_wrapper div#eventHooksTable_length").before(contentToAdd);
        $("#eventHookList #createEventHookButton").off("click");
        $('#eventHookList #createEventHookButton').click(
                function () {
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
            "mRender": function (data, type, obj) {
                var editEH = '<button id="editEventHook" onclick="openModalEventHook(\'' + obj["id"] + '\',\'EDIT\');"\n\
                                        class="btn btn-default btn-xs margin-right5" \n\
                                        name="editEventHook" title="' + doc.getDocLabel("page_global", "columnAction") + '" type="button">\n\
                                        <span class="glyphicon glyphicon-pencil"></span></button>';
                var duplicateEH = '<button id="editEventHook" onclick="openModalEventHook(\'' + obj["id"] + '\',\'DUPLICATE\');"\n\
                                        class="btn btn-default btn-xs margin-right5" \n\
                                        name="editEventHook" title="' + doc.getDocLabel("page_global", "columnAction") + '" type="button">\n\
                                        <span class="glyphicon glyphicon-duplicate"></span></button>';
                var deleteEH = '<button id="deleteEventHook" onclick="deleteEventHook(\'' + obj["id"] + '\');"\n\
                                        class="btn btn-default btn-xs margin-right5" \n\
                                        name="deleteEventHook" title="' + doc.getDocLabel("page_global", "columnAction") + '" type="button">\n\
                                        <span class="glyphicon glyphicon-trash"></span></button>';

                return '<div class="center btn-group width150">' + editEH + duplicateEH + deleteEH + '</div>';

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
            "sWidth": "50px",
            "title": doc.getDocLabel("page_eventhook", "isActive")
        },
        {
            "data": "hookConnector",
            "sName": "evh.hookConnector",
            "sWidth": "80px",
            "title": doc.getDocLabel("page_eventhook", "hookConnector")
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
