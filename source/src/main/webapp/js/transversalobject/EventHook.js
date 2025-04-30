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

/***
 * Open the modal with testcase information.
 * @param {String} hookid - id key to open the modal.
 * @returns {null}
 */
function openModalEventHook(hookid, mode) {

    // We only load the Labels and bind the events once for performance optimisations.
    if ($('#editEventHookModal').data("initLabel") === undefined) {
        initModalEventHook();
        $('#editEventHookModal').data("initLabel", true);
    }
    editEventHookClick(hookid, mode);
}

/***
 * Initialise modal.
 * @returns {null}
 */
function initModalEventHook() {

    var doc = new Doc();
    $("#title").html(doc.getDocLabel("page_eventhook", "title"));

    $("[name='tabEH1']").html(doc.getDocLabel("page_eventhook", "title"));
    $("[name='tabsEH25']").html(doc.getDocLabel("page_global", "traca"));

    $("[name='eventReferenceField']").html(doc.getDocOnline("page_eventhook", "eventReference"));
    $("[name='isActiveField']").html(doc.getDocOnline("page_eventhook", "isActive"));
    $("[name='objectKey1Field']").html(doc.getDocOnline("page_eventhook", "objectKey1"));
    $("[name='objectKey2Field']").html(doc.getDocOnline("page_eventhook", "objectKey2"));
    $("[name='hookConnectorField']").html(doc.getDocOnline("page_eventhook", "hookConnector"));
    $("[name='hookRecipientField']").html(doc.getDocOnline("page_eventhook", "hookRecipient"));
    $("[name='hookChannelField']").html(doc.getDocOnline("page_eventhook", "hookChannel"));
    $("[name='descriptionField']").html(doc.getDocOnline("page_eventhook", "description"));

    $("[name='lbl_created']").html(doc.getDocOnline("transversal", "DateCreated"));
    $("[name='lbl_creator']").html(doc.getDocOnline("transversal", "UsrCreated"));
    $("[name='lbl_lastModified']").html(doc.getDocOnline("transversal", "DateModif"));
    $("[name='lbl_lastModifier']").html(doc.getDocOnline("transversal", "UsrModif"));

    $("[name='buttonClose']").html(doc.getDocLabel("page_global", "buttonClose"));
    $("[name='buttonEdit']").html(doc.getDocLabel("page_global", "buttonEdit"));
    $("[name='buttonAdd']").html(doc.getDocLabel("page_global", "buttonAdd"));
    $("[name='buttonDuplicate']").html(doc.getDocLabel("page_global", "buttonDuplicate"));

    $("#editEventHookButton").off("click");
    $("#editEventHookButton").click(function () {
        confirmEventHookModalHandler("EDIT");
    });
    $("#addEventHookButton").off("click");
    $("#addEventHookButton").click(function () {
        confirmEventHookModalHandler("ADD");
    });
    $("#duplicateEventHookButton").off("click");
    $("#duplicateEventHookButton").click(function () {
        confirmEventHookModalHandler("DUPLICATE");
    });



    //clear the modals fields when closed
    $('#editEventHookModal').on('hidden.bs.modal', editEventHookModalCloseHandler);
}

function editEventHookModalCloseHandler() {
    // remove all errors on the form fields
    $(this).find('div.has-error').removeClass("has-error");
    // clear the response messages of the modal
    clearResponseMessage($('#editEventHookModal'));
}

/***
 * Open the modal with testcase information.
 * @param {String} eventid - type selected
 * @returns {null}
 */
function editEventHookClick(eventid, mode) {

    clearResponseMessage($('#editEventHookModal'));

    // When editing the execution queue, we can modify, modify and run or cancel.

    $('#editEventHookButton').hide();
    $('#addEventHookButton').hide();
    $('#duplicateEventHookButton').hide();

    var doc = new Doc();
    if (mode === "EDIT") {
        $('#editEventHookButton').show();
        $("[name='editEventHookField']").html(doc.getDocLabel("page_global", "buttonEdit") + " " + doc.getDocLabel("page_eventhook", "title"));
    } else if (mode === "ADD") {
        $('#addEventHookButton').show();
        $("[name='editEventHookField']").html(doc.getDocLabel("page_global", "buttonAdd") + " " + doc.getDocLabel("page_eventhook", "title"));
    } else if (mode === "DUPLICATE") {
        $('#duplicateEventHookButton').show();
        $("[name='editEventHookField']").html(doc.getDocLabel("page_global", "buttonDuplicate") + " " + doc.getDocLabel("page_eventhook", "title"));
    }

    feedEventHookModal(eventid, "editEventHookModal", mode);
}

/***
 * Function that support the modal confirmation. Will call servlet to comit the transaction.
 * @param {String} mode - either ADD, EDIT or DUPLICATE in order to define the purpose of the modal.
 * @returns {null}
 */
function confirmEventHookModalHandler(mode) {
    clearResponseMessage($('#editEventHookModal'));

    var eventHookID = $('#id').val();

    var formEdit = $('#editEventHookModal #editEventHookModalForm');

    showLoaderInModal('#editEventHookModal');

    // Calculate servlet name to call.
    var myServlet = "UpdateEventHook";
    if ((mode === "ADD") || (mode === "DUPLICATE")) {
        myServlet = "CreateEventHook";
    }

    // Get the header data from the form.
    var data = convertSerialToJSONObject(formEdit.serialize());

//    data.servicePath = encodeURIComponent(data.servicePath);

//    var formData = new FormData();
//    var file = $("#editSoapLibraryModal input[type=file]");
//
//    for (var i in data) {
//        formData.append(data[i].name, encodeURIComponent(data[i].value));
//    }

//    formData.append("contentList", JSON.stringify(table_content));
//    formData.append("headerList", JSON.stringify(table_header));
//    formData.append("srvRequest", encodeURIComponent(editor.getSession().getDocument().getValue()));

//    if (file.prop("files").length != 0) {
//        formData.append("file", file.prop("files")[0]);
//    }
//
//    if (isEmpty(formData.get("isFollowRedir"))) {
//        formData.append("isFollowRedir", 0);
//    }

//    var temp = data.service;

    data.isActive = (data.isActive === "on");
    $.ajax({
        url: myServlet,
        async: true,
        method: "POST",
        data: {
            description: data.description,
            eventReference: data.eventReference,
            hookChannel: data.hookChannel,
            hookConnector: data.hookConnector,
            hookRecipient: data.hookRecipient,
            id: data.id,
            isActive: data.isActive,
            objectKey1: data.objectKey1,
            objectKey2: data.objectKey2
        },
        success: function (data) {

            if (getAlertType(data.messageType) === "success") {
                var oTable = $("#eventHooksTable").dataTable();
                oTable.fnDraw(false);
                $('#editEventHookModal').data("Saved", true);
                $('#editEventHookModal').modal('hide');
                showMessage(data);
            } else {
                showMessage(data, $('#editEventHookModal'));
            }

            hideLoaderInModal('#editEventHookModal');
        },
        error: function (data) {
            data.message = showUnexpectedError();
            showMessage(data, $('#editEventHookModal'));
            hideLoaderInModal('#editEventHookModal');
        }
    });

}

/***
 * Feed the TestCase modal with all the data from the TestCase.
 * @param {String} eventid - id of the execution queue to load
 * @param {String} modalId - modal id to feed.
 * @returns {null}
 */
function feedEventHookModal(eventid, modalId, mode) {
    clearResponseMessageMainPage();
    var formEdit = $('#' + modalId);

    if (mode === "DUPLICATE" || mode === "EDIT") {
        $.ajax({
            url: "ReadEventHook",
            async: true,
            method: "POST",
            data: {
                id: eventid
            },
            success: function (data) {
                if (data.messageType === "OK") {

                    // Feed the data to the screen and manage authorities.
                    var eventHook = data.contentTable;
                    feedEventHookModalData(eventHook, modalId, mode, eventHook.hasPermissions);

                    formEdit.modal('show');
                } else {
                    showUnexpectedError();
                }
                refreshPopoverDocumentation("editEventHookModal");
            },
            error: showUnexpectedError
        });

    } else {
        var eventHookObj = {};
        var hasPermissions = true;
        eventHookObj.description = "";
        eventHookObj.eventReference = "CAMPAIGN_END";
        eventHookObj.hasPermissionsUpdate = true;
        eventHookObj.hookChannel = "";
        eventHookObj.hookConnector = "EMAIL";
        eventHookObj.hookRecipient = "";
        eventHookObj.id = 0;
        eventHookObj.isActive = true;
        eventHookObj.objectKey1 = "";
        eventHookObj.objectKey2 = "";

        feedEventHookModalData(eventHookObj, modalId, mode, hasPermissions);
        formEdit.modal('show');

    }

}

/***
 * Feed the TestCase modal with all the data from the TestCase.
 * @param {String} eventhookid - service object to be loaded.
 * @param {String} modalId - id of the modal form where to feed the data.
 * @param {String} mode - either ADD, EDIT or DUPLICATE in order to define the purpose of the modal.
 * @param {String} hasPermissionsUpdate - boolean if premition is granted.
 * @returns {null}
 */
function feedEventHookModalData(eventhookid, modalId, mode, hasPermissionsUpdate) {
    var formEdit = $('#' + modalId);
    var doc = new Doc();

    // Data Feed.
    if (mode === "EDIT") {
        formEdit.find("#id").prop("value", eventhookid.id);
        formEdit.find("#usrcreated").prop("value", eventhookid.usrCreated);
        formEdit.find("#datecreated").prop("value", getDate(eventhookid.dateCreated));
        formEdit.find("#usrmodif").prop("value", eventhookid.usrModif);
        formEdit.find("#datemodif").prop("value", getDate(eventhookid.dateModif));
    } else { // DUPLICATE or ADD
        formEdit.find("#usrcreated").prop("value", "");
        formEdit.find("#datecreated").prop("value", "");
        formEdit.find("#usrmodif").prop("value", "");
        formEdit.find("#datemodif").prop("value", "");
        if (mode === "ADD") {
            formEdit.find("#id").prop("value", eventhookid.id);
        } else { // DUPLICATE
            formEdit.find("#id").prop("value", eventhookid.id);
        }
    }

    formEdit.find("#isActive").prop("checked", eventhookid.isActive);

    formEdit.find("#eventReference").val(eventhookid.eventReference);
    formEdit.find("#hookConnector").val(eventhookid.hookConnector);

    formEdit.find("#hookChannel").val(eventhookid.hookChannel);
    formEdit.find("#hookRecipient").prop("value", eventhookid.hookRecipient);
    formEdit.find("#objectKey1").prop("value", eventhookid.objectKey1);
    formEdit.find("#objectKey2").prop("value", eventhookid.objectKey2);
    formEdit.find("#description").prop("value", eventhookid.description);



    // Authorities
    //We desactivate or activate the access to the fields depending on if user has the credentials to edit.
    if (!(hasPermissionsUpdate)) { // If readonly, we readonly all fields
//        formEdit.find("#addHeader").prop("disabled", "disabled");
//        formEdit.find("#servicePath").prop("readonly", true);
    } else {
//        formEdit.find("#addHeader").removeProp("disabled");
//        formEdit.find("#servicePath").prop("readonly", false);
    }


}

