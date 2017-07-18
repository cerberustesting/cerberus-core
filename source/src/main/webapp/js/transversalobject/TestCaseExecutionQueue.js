/*
 * Cerberus Copyright (C) 2013 - 2017 cerberustesting
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
function displayExecutionQueueLabel(doc) {

    $("[name='soapLibraryField']").html(doc.getDocLabel("appservice", "service"));
    $("[name='typeField']").html(doc.getDocLabel("appservice", "type"));
    $("[name='descriptionField']").html(doc.getDocLabel("appservice", "description"));
    $("[name='servicePathField']").html(doc.getDocLabel("appservice", "servicePath"));
    $("[name='methodField']").html(doc.getDocLabel("appservice", "method"));
    $("[name='buttonClose']").html(doc.getDocLabel("page_appservice", "close_btn"));
    $("[name='buttonAdd']").html(doc.getDocLabel("page_appservice", "save_btn"));
    $("#soapLibraryListLabel").html("<span class='glyphicon glyphicon-list'></span> " + doc.getDocLabel("appservice", "service"));
    // Tracability
    $("[name='lbl_created']").html(doc.getDocOnline("transversal", "DateCreated"));
    $("[name='lbl_creator']").html(doc.getDocOnline("transversal", "UsrCreated"));
    $("[name='lbl_lastModified']").html(doc.getDocOnline("transversal", "DateModif"));
    $("[name='lbl_lastModifier']").html(doc.getDocOnline("transversal", "UsrModif"));
}

/***
 * Open the modal with testcase information.
 * @param {String} queueID - type selected
 * @returns {null}
 */
function submitExecutionQueueClick(queueID) {
    console.info(queueID);
    var doc = new Doc();
    $("[name='editExecutionQueueField']").html(doc.getDocLabel("page_appservice", "editSoapLibrary_field"));

    $("#submitExecutionQueueButton").off("click");
    $("#submitExecutionQueueButton").click(function () {
        confirmExecutionQueueModalHandler("EDIT");
    });

    // Prepare all Events handler of the modal.
    prepareExecutionQueueModal();

//    $('#submitExecutionQueueButton').attr('class', 'btn btn-primary');
//    $('#submitExecutionQueueButton').removeProp('hidden');
    
    $('#submitExecutionQueueButton').attr('class', '');
    $('#submitExecutionQueueButton').attr('hidden', 'hidden');
    
    $('#copyExecutionQueueButton').attr('class', '');
    $('#copyExecutionQueueButton').attr('hidden', 'hidden');
    $('#cancelExecutionQueueButton').attr('class', '');
    $('#cancelExecutionQueueButton').attr('hidden', 'hidden');

    feedExecutionQueueModal(queueID, "editExecutionQueueModal", "EDIT");
}

/***
 * Open the modal with testcase information.
 * @param {String} service - type selected
 * @returns {null}
 */
function duplicateExecutionQueueClick(service) {
    $("#duplicateSoapLibraryButton").off("click");
    $("#duplicateSoapLibraryButton").click(function () {
        confirmExecutionQueueModalHandler("DUPLICATE");
    });

    // Prepare all Events handler of the modal.
    prepareExecutionQueueModal();

    $('#editSoapLibraryButton').attr('class', '');
    $('#editSoapLibraryButton').attr('hidden', 'hidden');
    $('#duplicateSoapLibraryButton').attr('class', 'btn btn-primary');
    $('#duplicateSoapLibraryButton').removeProp('hidden');
    $('#addSoapLibraryButton').attr('class', '');
    $('#addSoapLibraryButton').attr('hidden', 'hidden');

    feedExecutionQueueModal(service, "editSoapLibraryModal", "DUPLICATE");
}

/***
 * Open the modal in order to create a new testcase.
 * @returns {null}
 */
function addExecutionQueueClick() {
    $("#addSoapLibraryButton").off("click");
    $("#addSoapLibraryButton").click(function () {
        confirmExecutionQueueModalHandler("ADD");
    });

    // Prepare all Events handler of the modal.
    prepareExecutionQueueModal();

    $('#editSoapLibraryButton').attr('class', '');
    $('#editSoapLibraryButton').attr('hidden', 'hidden');
    $('#duplicateSoapLibraryButton').attr('class', '');
    $('#duplicateSoapLibraryButton').attr('hidden', 'hidden');
    $('#addSoapLibraryButton').attr('class', 'btn btn-primary');
    $('#addSoapLibraryButton').removeProp('hidden');

    feedNewExecutionQueueModal("editSoapLibraryModal");
}

/***
 * Function that initialise the modal with event handlers.
 * @returns {null}
 */
function prepareExecutionQueueModal() {

    // No events on Modal.

}


/***
 * Function that support the modal confirmation. Will call servlet to comit the transaction.
 * @param {String} mode - either ADD, EDIT or DUPLICATE in order to define the purpose of the modal.
 * @returns {null}
 */
function confirmExecutionQueueModalHandler(mode) {
    clearResponseMessage($('#editSoapLibraryModal'));

    var formEdit = $('#editSoapLibraryModal #editSoapLibraryModalForm');

    showLoaderInModal('#editSoapLibraryModal');

    // Enable the test combo before submit the form.
    if (mode === 'EDIT') {
        formEdit.find("#service").removeAttr("disabled");
    }
    // Calculate servlet name to call.
    var myServlet = "UpdateExecutionQueue";
    if ((mode === "ADD") || (mode === "DUPLICATE")) {
        myServlet = "CreateExecutionQueue";
    }

    // Get the header data from the form.
    var data = convertSerialToJSONObject(formEdit.serialize());

    //Add envelope, not in the form
    var editor = ace.edit($("#editSoapLibraryModal #srvRequest")[0]);
    data.srvRequest = encodeURIComponent(editor.getSession().getDocument().getValue());

    // Getting Data from Content TAB
    var table1 = $("#contentTableBody tr");
    var table_content = [];
    for (var i = 0; i < table1.length; i++) {
        table_content.push($(table1[i]).data("content"));
    }
    // Getting Data from Header TAB
    var table2 = $("#headerTableBody tr");
    var table_header = [];
    for (var i = 0; i < table2.length; i++) {
        table_header.push($(table2[i]).data("header"));
    }


    showLoaderInModal('#editTestCaseModal');
    $.ajax({
        url: myServlet,
        async: true,
        method: "POST",
        data: {
            service: data.service,
            application: data.application,
            type: data.type,
            method: data.method,
            servicePath: data.servicePath,
            operation: data.operation,
            attachementurl: data.attachementurl,
            description: data.description,
            group: data.group,
            serviceRequest: data.srvRequest,
            contentList: JSON.stringify(table_content),
            headerList: JSON.stringify(table_header)
        },
        success: function (data) {
            data = JSON.parse(data);
            hideLoaderInModal('#editSoapLibraryModal');
            if (getAlertType(data.messageType) === "success") {
                var oTable = $("#soapLibrarysTable").dataTable();
                oTable.fnDraw(true);
                $('#editSoapLibraryModal').data("Saved", true);
                $('#editSoapLibraryModal').modal('hide');
                showMessage(data);
            } else {
                showMessage(data, $('#editSoapLibraryModal'));
            }
        },
        error: showUnexpectedError
    });
    if (mode === 'EDIT') { // Disable back the test combo before submit the form.
        formEdit.find("#service").prop("disabled", "disabled");
    }

}


/***
 * Feed the TestCase modal with all the data from the TestCase.
 * @param {String} modalId - type selected
 * @returns {null}
 */
function feedNewExecutionQueueModal(modalId) {
    clearResponseMessageMainPage();

    var formEdit = $('#' + modalId);

    // Feed the data to the screen and manage authorities.
    feedExecutionQueueModalData(undefined, modalId, "ADD", true);

    formEdit.modal('show');
}

/***
 * Feed the TestCase modal with all the data from the TestCase.
 * @param {String} queueid - id of the execution queue to load
 * @param {String} modalId - modal id to feed.
 * @param {String} mode - either ADD, EDIT or DUPLICATE in order to define the purpose of the modal.
 * @returns {null}
 */
function feedExecutionQueueModal(queueid, modalId, mode) {
    clearResponseMessageMainPage();

    var formEdit = $('#' + modalId);

    $.ajax({
        url: "ReadTestCaseExecutionQueue?queueid=" + queueid,
        async: true,
        method: "GET",
        success: function (data) {
            if (data.messageType === "OK") {

                // Feed the data to the screen and manage authorities.
                var exeQ = data.contentTable;
                feedExecutionQueueModalData(exeQ, modalId, mode, true);

                formEdit.modal('show');
            } else {
                showUnexpectedError();
            }
        },
        error: showUnexpectedError
    });

}


/***
 * Feed the TestCase modal with all the data from the TestCase.
 * @param {String} exeQ - service object to be loaded.
 * @param {String} modalId - id of the modal form where to feed the data.
 * @param {String} mode - either ADD, EDIT or DUPLICATE in order to define the purpose of the modal.
 * @param {String} hasPermissionsUpdate - boolean if premition is granted.
 * @returns {null}
 */
function feedExecutionQueueModalData(exeQ, modalId, mode, hasPermissionsUpdate) {
    var formEdit = $('#' + modalId);
    var doc = new Doc();

    //Destroy the previous Ace object.
    ace.edit($("#editSoapLibraryModal #srvRequest")[0]).destroy();

    // Data Feed.
    if (mode === "EDIT") {
        $("[name='editSoapLibraryField']").html(doc.getDocOnline("page_appservice", "button_edit"));
        formEdit.find("#ID").prop("value", exeQ.id);
        formEdit.find("#usrcreated").prop("value", exeQ.UsrCreated);
        formEdit.find("#datecreated").prop("value", exeQ.DateCreated);
        formEdit.find("#usrmodif").prop("value", exeQ.UsrModif);
        formEdit.find("#datemodif").prop("value", exeQ.DateModif);
    } else { // DUPLICATE or ADD
        formEdit.find("#usrcreated").prop("value", "");
        formEdit.find("#datecreated").prop("value", "");
        formEdit.find("#usrmodif").prop("value", "");
        formEdit.find("#datemodif").prop("value", "");
        if (mode === "ADD") {
            $("[name='editSoapLibraryField']").html(doc.getDocOnline("page_appservice", "button_create"));
            formEdit.find("#ID").prop("value", "");
        } else { // DUPLICATE
            $("[name='editSoapLibraryField']").html(doc.getDocOnline("page_appservice", "button_duplicate"));
            formEdit.find("#ID").prop("value", exeQ.id);
        }
    }
    if (isEmpty(exeQ)) {
        formEdit.find("#originalid").prop("value", "");
        formEdit.find("#tag").prop("value", "");
        formEdit.find("#requestDate").prop("value", "");
        formEdit.find("#state").prop("value", "GET");
        formEdit.find("#comment").prop("value", "");
        formEdit.find("#exeId").prop("value", "");
        formEdit.find("#test").prop("value", "");
        formEdit.find("#testCase").text("");
        formEdit.find("#country").prop("value", "");
        formEdit.find("#environment").prop("value", "");
    } else {
        formEdit.find("#tag").val(exeQ.tag);
        formEdit.find("#requestDate").val(exeQ.requestDate);
        formEdit.find("#state").val(exeQ.state);
        formEdit.find("#comment").prop("value", exeQ.comment);
        formEdit.find("#exeId").prop("value", exeQ.exeId);
        formEdit.find("#test").prop("value", exeQ.test);
        formEdit.find("#testCase").prop("value", exeQ.testCase);
        formEdit.find("#country").prop("value", exeQ.country);
        formEdit.find("#environment").prop("value", exeQ.environment);

    }
    //Highlight envelop on modal loading
    var editor = ace.edit($("#editSoapLibraryModal #srvRequest")[0]);
    editor.setTheme("ace/theme/chrome");
    editor.getSession().setMode(defineAceMode(editor.getSession().getDocument().getValue()));
    editor.setOptions({
        maxLines: Infinity
    });

    //On ADD, try to autodetect Ace mode until it is defined
    if (mode === "ADD") {
        $($("#editSoapLibraryModal #srvRequest").get(0)).keyup(function () {
            if (editor.getSession().getMode().$id === "ace/mode/text") {
                editor.getSession().setMode(defineAceMode(editor.getSession().getDocument().getValue()));
            }
        });

    }

    // Authorities
    if (mode === "EDIT") {
        formEdit.find("#service").prop("readonly", "readonly");
    } else {
        formEdit.find("#service").removeAttr("readonly");
        formEdit.find("#service").removeProp("readonly");
    }
    //We desactivate or activate the access to the fields depending on if user has the credentials to edit.
    if (!(hasPermissionsUpdate)) { // If readonly, we readonly all fields
        formEdit.find("#application").prop("readonly", "readonly");
        formEdit.find("#type").prop("disabled", "disabled");
        formEdit.find("#method").prop("disabled", "disabled");
        formEdit.find("#servicePath").prop("readonly", true);
        formEdit.find("#attachementurl").prop("readonly", true);
        formEdit.find("#srvRequest").prop("readonly", "readonly");
        formEdit.find("#description").prop("readonly", "readonly");
        // We hide Save button.
        $('#editSoapLibraryButton').attr('class', '');
        $('#editSoapLibraryButton').attr('hidden', 'hidden');
    } else {
        formEdit.find("#application").removeProp("readonly");
        formEdit.find("#type").removeProp("disabled");
        formEdit.find("#method").removeProp("disabled");
        formEdit.find("#servicePath").prop("readonly", false);
        formEdit.find("#attachementurl").prop("readonly", false);
        formEdit.find("#srvRequest").removeProp("readonly");
        formEdit.find("#description").removeProp("disabled");
    }


}


