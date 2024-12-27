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
    // handle the click for specific action buttons
    $("#addEntryButton").click(addEntryModalSaveHandler);
    $("#editEntryButton").click(editEntryModalSaveHandler);

    //clear the modals fields when closed
    $('#addEntryModal').on('hidden.bs.modal', {extra: "#addEntryModal"}, buttonCloseHandler);
    $('#editEntryModal').on('hidden.bs.modal', {extra: "#editEntryModal"}, buttonCloseHandler);

    //configure and create the dataTable
    var configurations = new TableConfigurationsServerSide("batchinvariantsTable", "ReadBatchInvariant", "contentTable", aoColumnsFunc("batchinvariantsTable"), [1, 'asc']);

    createDataTableWithPermissions(configurations, renderOptionsForBatchInvariant, "#batchinvariant", undefined, true);
}

function displayPageLabel() {
    var doc = new Doc();

    displayHeaderLabel(doc);
    displayGlobalLabel(doc);
    $("#pageTitle").html(doc.getDocLabel("page_batchinvariant", "title"));
    $("#title").html(doc.getDocOnline("page_batchinvariant", "title"));
    $("[name='addEntryField']").html(doc.getDocLabel("page_batchinvariant", "button_create"));
    $("[name='confirmationField']").html(doc.getDocLabel("page_batchinvariant", "button_delete"));
    $("[name='editEntryField']").html(doc.getDocLabel("page_batchinvariant", "button_edit"));
    $("[name='batchField']").html(doc.getDocOnline("batchinvariant", "Batch"));
    $("[name='systemField']").html(doc.getDocOnline("batchinvariant", "system"));
    $("[name='descriptionField']").html(doc.getDocOnline("batchinvariant", "Description"));
    displayInvariantList("system", "SYSTEM", false, getUser().defaultSystem);
    displayFooter(doc);
}

function renderOptionsForBatchInvariant(data) {
    var doc = new Doc();
    //check if user has permissions to perform the add and import operations
    if (data["hasPermissions"]) {
        if ($("#createBatchInvariantButton").length === 0) {
            var contentToAdd = "<div class='marginBottom10'><button id='createBatchInvariantButton' type='button' class='btn btn-default'>\n\
            <span class='glyphicon glyphicon-plus-sign'></span> " + doc.getDocLabel("page_batchinvariant", "button_create") + "</button></div>";

            $("#batchinvariantsTable_wrapper div#batchinvariantsTable_length").before(contentToAdd);
            $('#batchinvariantList #createBatchInvariantButton').click(addEntryClick);
        }
    }
}

function deleteEntryHandlerClick() {
    var batch = $('#confirmationModal').find('#hiddenField1').prop("value");
    var jqxhr = $.post("DeleteBatchInvariant", {batch: batch}, "json");
    $.when(jqxhr).then(function (data) {
        var messageType = getAlertType(data.messageType);
        if (messageType === "success") {
            //redraw the datatable
            var oTable = $("#batchinvariantsTable").dataTable();
            oTable.fnDraw(false);
            var info = oTable.fnGetData().length;

            if (info === 1) {//page has only one row, then returns to the previous page
                oTable.fnPageChange('previous');
            }

        }
        //show message in the main page
        showMessageMainPage(messageType, data.message, false);
        //close confirmation window
        $('#confirmationModal').modal('hide');
    }).fail(handleErrorAjaxAfterTimeout);
}

function deleteEntryClick(batch) {
    clearResponseMessageMainPage();
    var doc = new Doc();
    var messageComplete = doc.getDocLabel("page_batchinvariant", "message_delete");
    messageComplete = messageComplete.replace("%TABLE%", doc.getDocLabel("batchinvariant", "batch"));
    messageComplete = messageComplete.replace("%ENTRY%", batch);
    showModalConfirmation(deleteEntryHandlerClick, undefined, doc.getDocLabel("page_batchinvariant", "button_delete"), messageComplete, batch, "", "", "");
}

function addEntryModalSaveHandler() {
    clearResponseMessage($('#addEntryModal'));
    var formAdd = $("#addEntryModal #addEntryModalForm");

    var nameElement = formAdd.find("#batchInvariant");
    var nameElementEmpty = nameElement.prop("value") === '';
    if (nameElementEmpty) {
        var localMessage = new Message("danger", "Please specify the name of the Type of Deployment!");
        nameElement.parents("div.form-group").addClass("has-error");
        showMessage(localMessage, $('#addEntryModal'));
    } else {
        nameElement.parents("div.form-group").removeClass("has-error");
    }

    // verif if all mendatory fields are not empty
    if (nameElementEmpty)
        return;

    showLoaderInModal('#addEntryModal');
    saveEntry("CreateBatchInvariant", "#addEntryModal", formAdd);

}

function addEntryClick() {
    clearResponseMessageMainPage();
    $('#addEntryModal').modal('show');
}

function editEntryModalSaveHandler() {
    clearResponseMessage($('#editEntryModal'));
    var formEdit = $('#editEntryModal #editEntryModalForm');

    showLoaderInModal('#editEntryModal');
    saveEntry("UpdateBatchInvariant", "#editEntryModal", formEdit);
}

function editEntryClick(batch) {
    clearResponseMessageMainPage();
    var jqxhr = $.getJSON("ReadBatchInvariant", "batch=" + batch);
    $.when(jqxhr).then(function (data) {
        var obj = data["contentTable"];

        var formEdit = $('#editEntryModal');

        formEdit.find("#batch").prop("value", batch);
        formEdit.find("#system").prop("value", obj["system"]);
        formEdit.find("#Description").prop("value", obj["description"]);

        if (!(data["hasPermissions"])) { // If readonly, we only readonly all fields
            formEdit.find("#batch").prop("readonly", "readonly");
            formEdit.find("#system").prop("readonly", "readonly");
            formEdit.find("#Description").prop("readonly", "readonly");

            $('#editEntryButton').attr('class', '');
            $('#editEntryButton').attr('hidden', 'hidden');
        }

        formEdit.modal('show');
    });
}

function saveEntry(servletName, modalID, form) {
    // Get the header data from the form.
    var dataForm = convertSerialToJSONObject(form.serialize());

    var jqxhr = $.post(servletName, dataForm);
    $.when(jqxhr).then(function (data) {
        hideLoaderInModal(modalID);
        if (getAlertType(data.messageType) === 'success') {
            var oTable = $("#batchinvariantsTable").dataTable();
            oTable.fnDraw(false);
            showMessage(data);
            $(modalID).modal('hide');
        } else {
            showMessage(data, $(modalID));
        }
    }).fail(handleErrorAjaxAfterTimeout);
}

function buttonCloseHandler(event) {
    var modalID = event.data.extra;
    // reset form values
    $(modalID + " " + modalID + "Form")[0].reset();
    // remove all errors on the form fields
    $(this).find('div.has-error').removeClass("has-error");
    // clear the response messages of the modal
    clearResponseMessage($(modalID));
}

function aoColumnsFunc(tableId) {
    var doc = new Doc();

    var aoColumns = [
        {
            "data": null,
            "title": doc.getDocLabel("page_global", "columnAction"),
            "bSortable": false,
            "bSearchable": false,
            "sWidth": "50px",
            "mRender": function (data, type, obj) {
                var hasPermissions = $("#" + tableId).attr("hasPermissions");

                var editEntry = '<button id="editEntry" onclick="editEntryClick(\'' + obj["batch"] + '\');"\n\
                                    class="editEntry btn btn-default btn-xs margin-right5" \n\
                                    name="editEntry" title="' + doc.getDocLabel("page_batchinvariant", "button_edit") + '" type="button">\n\
                                    <span class="glyphicon glyphicon-pencil"></span></button>';
                var viewEntry = '<button id="editEntry" onclick="editEntryClick(,\'' + obj["batch"] + '\');"\n\
                                    class="editEntry btn btn-default btn-xs margin-right5" \n\
                                    name="editEntry" title="' + doc.getDocLabel("page_batchinvariant", "button_edit") + '" type="button">\n\
                                    <span class="glyphicon glyphicon-eye-open"></span></button>';
                var deleteEntry = '<button id="deleteEntry" onclick="deleteEntryClick(\'' + obj["batch"] + '\');" \n\
                                    class="deleteEntry btn btn-default btn-xs margin-right5" \n\
                                    name="deleteEntry" title="' + doc.getDocLabel("page_batchinvariant", "button_delete") + '" type="button">\n\
                                    <span class="glyphicon glyphicon-trash"></span></button>';
                if (hasPermissions === "true") { //only draws the options if the user has the correct privileges
                    return '<div class="center btn-group width150">' + editEntry + deleteEntry + '</div>';
                }
                return '<div class="center btn-group width150">' + viewEntry + '</div>';
            }
        },
        {
            "data": "batch",
            "like": true,
            "sName": "batch",
            "sWidth": "50px",
            "title": doc.getDocOnline("batchinvariant", "Batch")},
        {
            "data": "system",
            "sName": "system",
            "sWidth": "50px",
            "title": doc.getDocOnline("batchinvariant", "system")},
        {
            "data": "description",
            "like": true,
            "sName": "description",
            "sWidth": "100px",
            "title": doc.getDocOnline("batchinvariant", "Description")}
    ];
    return aoColumns;
}