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
        initApplicationPage();
    });
});

function initApplicationPage() {
    displayHeaderLabel();
    displayPageLabel();
    // handle the click for specific action buttons
    $("#addApplicationButton").click(saveNewApplicationHandler);
    $("#editAplicationButton").click(saveUpdateApplicationHandler);

    //clear the modals fields when closed
    $('#addApplicationModal').on('hidden.bs.modal', addApplicationModalCloseHandler);
    $('#editApplicationModal').on('hidden.bs.modal', editApplicationModalCloseHandler);

    //configure and create the dataTable
    var configurations = new TableConfigurationsServerSide("applicationsTable", "ReadApplication", "contentTable", aoColumnsFunc());

    createDataTableWithPermissions(configurations, renderOptionsForApplication);
    var oTable = $("#applicationsTable").dataTable();
    oTable.fnSort([1, 'asc']);
};

function displayPageLabel() {
    var doc = getDoc();
    
    $("#title").html(displayDocLink(doc.page_application.title));
    $("[name='createApplicationField']").html(doc.page_application.button_create.docLabel);
    $("[name='confirmationField']").html(doc.page_application.button_delete.docLabel);
    $("[name='editApplicationField']").html(doc.page_application.button_edit.docLabel);
    $("[name='buttonAdd']").html(doc.page_global.buttonAdd.docLabel);
    $("[name='buttonClose']").html(doc.page_global.buttonClose.docLabel);
    $("[name='buttonConfirm']").html(doc.page_global.buttonConfirm.docLabel);
    $("[name='buttonDismiss']").html(doc.page_global.buttonDismiss.docLabel);
    $("[name='applicationField']").html(displayDocLink(doc.application.Application));
    $("[name='descriptionField']").html(displayDocLink(doc.application.Description));
    $("[name='sortField']").html(displayDocLink(doc.application.sort));
    $("[name='typeField']").html(displayDocLink(doc.application.type));
    $("[name='systemField']").html(displayDocLink(doc.application.system));
    $("[name='subsystemField']").html(displayDocLink(doc.application.subsystem));
    $("[name='svnurlField']").html(displayDocLink(doc.application.svnurl));
    $("[name='bugtrackerurlField']").html(displayDocLink(doc.application.bugtrackerurl));
    $("[name='bugtrackernewurlField']").html(displayDocLink(doc.application.bugtrackernewurl));
    $("[name='deploytypeField']").html(displayDocLink(doc.application.deploytype));
    $("[name='mavengroupidField']").html(displayDocLink(doc.application.mavengroupid));
    displayFooter(doc);
}

function deleteApplicationHandlerClick() {
    var idApplication = $('#confirmationModal').find('#hiddenField').prop("value");
    var jqxhr = $.post("DeleteApplication", {id: idApplication}, "json");
    $.when(jqxhr).then(function (data) {
        var messageType = getAlertType(data.messageType);
        if (messageType === "success") {
            //redraw the datatable
            var oTable = $("#applicationsTable").dataTable();
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

function deleteProject(idApplication) {
    clearResponseMessageMainPage();
    var doc = getDoc();
    var messageComplete = doc.page_global.deleteMessage.docLabel;
    showModalConfirmation(deleteApplicationHandlerClick, doc.page_application.button_delete.docLabel, messageComplete, idApplication);
}

function saveNewApplicationHandler() {
    clearResponseMessage($('#addApplicationModal'));
    var formAdd = $("#addApplicationModal #addApplicationModalForm");

    var nameElement = formAdd.find("#application");
    var nameElementEmpty = nameElement.prop("value") === '';
    if (nameElementEmpty) {
        var localMessage = new Message("danger", "Please specify the name of the application!");
        nameElement.parents("div.form-group").addClass("has-error");
        showMessage(localMessage, $('#addApplicationModal'));
    } else {
        nameElement.parents("div.form-group").removeClass("has-error");
    }


    // verif if all mendatory fields are not empty
    if (nameElementEmpty)
        return;

    showLoaderInModal('#addApplicationModal');
    var jqxhr = $.post("CreateApplication", formAdd.serialize());
    $.when(jqxhr).then(function (data) {
        hideLoaderInModal('#addApplicationModal');
        console.log(data.messageType);
        if (getAlertType(data.messageType) === 'success') {
            var oTable = $("#applicationsTable").dataTable();
            oTable.fnDraw(true);
            showMessage(data);
            $('#addApplicationModal').modal('hide');
        } else {
            showMessage(data, $('#addApplicationModal'));
        }
    }).fail(handleErrorAjaxAfterTimeout);
}

function saveUpdateApplicationHandler() {
    clearResponseMessage($('#editApplicationModal'));
    var formEdit = $('#editApplicationModal #editApplicationModalForm');
    showLoaderInModal('#editApplicationModal');

    var jqxhr = $.post("UpdateApplication", formEdit.serialize(), "json");
    $.when(jqxhr).then(function (data) {
        // unblock when remote call returns 
        hideLoaderInModal('#editApplicationModal');
        if (getAlertType(data.messageType) === "success") {
            var oTable = $("#applicationsTable").dataTable();
            oTable.fnDraw(true);
            $('#editApplicationModal').modal('hide');
            showMessage(data);

        } else {
            showMessage(data, $('#editApplicationModal'));
        }
    }).fail(handleErrorAjaxAfterTimeout);
}

function addApplicationModalCloseHandler() {
    // reset form values
    $('#addApplicationModal #addApplicationModalForm')[0].reset();
    // remove all errors on the form fields
    $(this).find('div.has-error').removeClass("has-error");
    // clear the response messages of the modal
    clearResponseMessage($('#addApplicationModal'));
}

function editApplicationModalCloseHandler() {
    // reset form values
    $('#editApplicationModal #editApplicationModalForm')[0].reset();
    // remove all errors on the form fields
    $(this).find('div.has-error').removeClass("has-error");
    // clear the response messages of the modal
    clearResponseMessage($('#editApplicationModal'));
}

function CreateApplicationClick() {
    clearResponseMessageMainPage();
    $('#addApplicationModal').modal('show');
}

function editApplication(id) {
    clearResponseMessageMainPage();
    var jqxhr = $.getJSON("ReadApplication", "action=1&application=" + id);
    $.when(jqxhr).then(function (data) {
        var obj = data["contentTable"];

        var formEdit = $('#editApplicationModal');

        formEdit.find("#application").prop("value", id);
        formEdit.find("#description").prop("value", obj["description"]);
        formEdit.find("#sort").prop("value", obj["sort"]);
        formEdit.find("#type").prop("value", obj["type"]);
        formEdit.find("#system").prop("value", obj["system"]);
        formEdit.find("#subsystem").prop("value", obj["subsystem"]);
        formEdit.find("#svnurl").prop("value", obj["svnurl"]);
        formEdit.find("#bugtrackerurl").prop("value", obj["bugTrackerUrl"]);
        formEdit.find("#bugtrackernewurl").prop("value", obj["bugTrackerNewUrl"]);
        formEdit.find("#deploytype").prop("value", obj["deploytype"]);
        formEdit.find("#mavengroupid").prop("value", obj["mavengroupid"]);
        formEdit.modal('show');
    });
}

function renderOptionsForApplication(data) {
    var doc = getDoc();
    //check if user has permissions to perform the add and import operations
    if (data["hasPermissions"]) {
        if ($("#createApplicationButton").length === 0) {
            var contentToAdd = "<div class='marginBottom10'><button id='createApplicationButton' type='button' class='btn btn-default'>\n\
            " + doc.page_application.button_create.docLabel + "</button></div>";

            $("#applicationsTable_wrapper div.ColVis").before(contentToAdd);
            $('#application #createApplicationButton').click(CreateApplicationClick);
        }
    }
}

function aoColumnsFunc() {
    var doc = getDoc();
    var aoColumns = [
        {"data": "button",
            "sName": "Actions",
            "title": doc.page_global.columnAction.docLabel,
            "bSortable": false,
            "bSearchable": false,
            "mRender": function (data, type, obj) {
                var editApplication = '<button id="editApplication" onclick="editApplication(\'' + obj["application"] + '\');"\n\
                                class="editApplication btn btn-default btn-xs margin-right5" \n\
                                name="editApplication" title="\'' + doc.page_application.button_edit.docLabel + '\'" type="button">\n\
                                <span class="glyphicon glyphicon-pencil"></span></button>';
                var deleteApplication = '<button id="deleteApplication" onclick="deleteApplication(\'' + obj["application"] + '\');" \n\
                                class="deleteApplication btn btn-default btn-xs margin-right5" \n\
                                name="deleteApplication" title="\'' + doc.page_application.button_delete.docLabel + '\'" type="button">\n\
                                <span class="glyphicon glyphicon-trash"></span></button>';

                return '<div class="center btn-group width150">' + editApplication + deleteApplication + '</div>';
            }
        },
        {"data": "application",
            "sName": "application",
            "title": displayDocLink(doc.application.Application)},
        {"data": "description",
            "sName": "description",
            "title": displayDocLink(doc.application.Description)},
        {"data": "sort",
            "sName": "sort",
            "title": displayDocLink(doc.application.sort)},
        {"data": "type",
            "sName": "type",
            "title": displayDocLink(doc.application.type)},
        {"data": "system",
            "sName": "system",
            "title": displayDocLink(doc.application.system)},
        {"data": "subsystem",
            "sName": "subsystem",
            "title": displayDocLink(doc.application.subsystem)},
        {"data": "svnurl",
            "sName": "svnurl",
            "title": displayDocLink(doc.application.svnurl)},
        {"data": "bugTrackerUrl",
            "sName": "bugTrackerUrl",
            "title": displayDocLink(doc.application.bugtrackerurl)},
        {"data": "bugTrackerNewUrl",
            "sName": "bugTrackerNewUrl",
            "title": displayDocLink(doc.application.bugtrackernewurl)},
        {"data": "deploytype",
            "sName": "deploytype",
            "title": displayDocLink(doc.application.deploytype)},
        {"data": "mavengroupid",
            "sName": "mavengroupid",
            "title": displayDocLink(doc.application.mavengroupid)}
    ];
    return aoColumns;
}