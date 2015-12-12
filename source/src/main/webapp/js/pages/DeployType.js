/*
 * Cerberus  Copyright (C) 2013  vertigo17
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

$.when($.getScript("js/pages/global/global.js")).then(function () {
    $(document).ready(function () {
        initPage();
    });
});

function initPage() {
    displayPageLabel();
    // handle the click for specific action buttons
    $("#addEntryButton").click(saveNewEntryHandler);
    $("#editEntryButton").click(saveUpdateEntryHandler);

    //clear the modals fields when closed
    $('#addEntryModal').on('hidden.bs.modal', {extra: "#addEntryModal"}, buttonCloseHandler);
    $('#editEntryModal').on('hidden.bs.modal', {extra: "#editEntryModal"}, buttonCloseHandler);

    //configure and create the dataTable
    var configurations = new TableConfigurationsServerSide("deploytypesTable", "ReadDeployType", "contentTable", aoColumnsFunc("deploytypesTable"));

    createDataTableWithPermissions(configurations, renderOptionsForDeployType);
}

function displayPageLabel() {
    var doc = new Doc();

    displayHeaderLabel(doc);
    displayGlobalLabel(doc);
    $("#pageTitle").html(doc.getDocLabel("page_deploytype", "title"));
    $("#title").html(doc.getDocOnline("page_deploytype", "title"));
    $("[name='addEntryField']").html(doc.getDocLabel("page_deploytype", "button_create"));
    $("[name='confirmationField']").html(doc.getDocLabel("page_deploytype", "button_delete"));
    $("[name='editEntryField']").html(doc.getDocLabel("page_deploytype", "button_edit"));
    $("[name='deployTypeField']").html(doc.getDocOnline("deploytype", "deploytype"));
    $("[name='descriptionField']").html(doc.getDocOnline("deploytype", "description"));
    displayFooter(doc);
}

function deleteEntryHandlerClick() {
    var deployType = $('#confirmationModal').find('#hiddenField1').prop("value");
    var jqxhr = $.post("DeleteDeployType", {deploytype: deployType}, "json");
    $.when(jqxhr).then(function (data) {
        var messageType = getAlertType(data.messageType);
        if (messageType === "success") {
            //redraw the datatable
            var oTable = $("#deploytypesTable").dataTable();
            oTable.fnDraw(true);
            var info = oTable.fnGetData().length;

            if (info === 1) {//page has only one row, then returns to the previous page
                oTable.fnPageChange('previous');
            }

        }
        //show message in the main page
        showMessageMainPage(messageType, data.message);
        //close confirmation window
        $('#confirmationModal').modal('hide');
    }).fail(handleErrorAjaxAfterTimeout);
}

function deleteEntry(entry) {
    clearResponseMessageMainPage();
    var doc = new Doc();
    var messageComplete = doc.getDocLabel("page_deploytype", "message_delete");
    messageComplete = messageComplete.replace("%TABLE%", doc.getDocLabel("deploytype", "deploytype"));
    messageComplete = messageComplete.replace("%ENTRY%", entry);
    showModalConfirmation(deleteEntryHandlerClick, doc.getDocLabel("page_deploytype", "button_delete"), messageComplete, entry, "", "", "");
}

function saveEntry(servletName, modalID, form) {
    var jqxhr = $.post(servletName, form.serialize());
    $.when(jqxhr).then(function (data) {
        hideLoaderInModal(modalID);
        if (getAlertType(data.messageType) === 'success') {
            var oTable = $("#deploytypesTable").dataTable();
            oTable.fnDraw(true);
            showMessage(data);
            $(modalID).modal('hide');
        } else {
            showMessage(data, $(modalID));
        }
    }).fail(handleErrorAjaxAfterTimeout);
}

function saveNewEntryHandler() {
    clearResponseMessage($('#addEntryModal'));
    var formAdd = $("#addEntryModal #addEntryModalForm");

    var nameElement = formAdd.find("#deployType");
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
    saveEntry("CreateDeployType", "#addEntryModal", formAdd);

}

function saveUpdateEntryHandler() {
    clearResponseMessage($('#editEntryModal'));
    var formEdit = $('#editEntryModal #editEntryModalForm');

    showLoaderInModal('#editEntryModal');
    saveEntry("UpdateDeployType", "#editEntryModal", formEdit);
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

function CreateDeployTypeClick() {
    clearResponseMessageMainPage();
    $('#addEntryModal').modal('show');
}

function editEntry(id) {
    clearResponseMessageMainPage();
    var jqxhr = $.getJSON("ReadDeployType", "deploytype=" + id);
    $.when(jqxhr).then(function (data) {
        var obj = data["contentTable"];

        var formEdit = $('#editEntryModal');

        formEdit.find("#deployType").prop("value", id);
        formEdit.find("#Description").prop("value", obj["description"]);

        if (!(data["hasPermissions"])) { // If readonly, we only readonly all fields
            formEdit.find("#deployType").prop("readonly", "readonly");
            formEdit.find("#Description").prop("readonly", "readonly");

            $('#editEntryButton').attr('class', '');
            $('#editEntryButton').attr('hidden', 'hidden');
        }

        formEdit.modal('show');
    });
}

function renderOptionsForDeployType(data) {
    var doc = new Doc();
    //check if user has permissions to perform the add and import operations
    if (data["hasPermissions"]) {
        if ($("#createDeployTypeButton").length === 0) {
            var contentToAdd = "<div class='marginBottom10'><button id='createDeployTypeButton' type='button' class='btn btn-default'>\n\
            " + doc.getDocLabel("page_deploytype", "button_create") + "</button></div>";

            $("#deploytypesTable_wrapper div.ColVis").before(contentToAdd);
            $('#deploytype #createDeployTypeButton').click(CreateDeployTypeClick);
        }
    }
}

function aoColumnsFunc(tableId) {
    var doc = new Doc();

    var aoColumns = [
        {"data": null,
            "title": doc.getDocLabel("page_global", "columnAction"),
            "bSortable": false,
            "bSearchable": false,
            "mRender": function (data, type, obj) {
                var hasPermissions = $("#" + tableId).attr("hasPermissions");

                var editEntry = '<button id="editEntry" onclick="editEntry(\'' + obj["deploytype"] + '\');"\n\
                                    class="editEntry btn btn-default btn-xs margin-right5" \n\
                                    name="editEntry" title="' + doc.getDocLabel("page_deploytype", "button_edit") + '" type="button">\n\
                                    <span class="glyphicon glyphicon-pencil"></span></button>';
                var deleteEntry = '<button id="deleteEntry" onclick="deleteEntry(\'' + obj["deploytype"] + '\');" \n\
                                    class="deleteEntry btn btn-default btn-xs margin-right5" \n\
                                    name="deleteEntry" title="' + doc.getDocLabel("page_deploytype", "button_delete") + '" type="button">\n\
                                    <span class="glyphicon glyphicon-trash"></span></button>';
                if (hasPermissions === "true") { //only draws the options if the user has the correct privileges
                    return '<div class="center btn-group width150">' + editEntry + deleteEntry + '</div>';
                }
                return '<div class="center btn-group width150">' + editEntry + '</div>';
            }
        },
        {"data": "deploytype",
            "sName": "deployType",
            "title": doc.getDocOnline("deploytype", "deploytype")},
        {"data": "description",
            "sName": "description",
            "title": doc.getDocOnline("deploytype", "description")}
    ];
    return aoColumns;
}