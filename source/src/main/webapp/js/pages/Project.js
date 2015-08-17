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
        initProjectPage();
    });
});

function initProjectPage() {
    displayHeaderLabel();
    displayPageLabel();
    // handle the click for specific action buttons
    $("#addProjectButton").click(saveNewProjectHandler);
    $("#editProjectButton").click(saveUpdateProjectHandler);

    //clear the modals fields when closed
    $('#addProjectModal').on('hidden.bs.modal', addProjectModalCloseHandler);
    $('#editProjectModal').on('hidden.bs.modal', editProjectModalCloseHandler);

    //configure and create the dataTable
    var configurations = new TableConfigurationsServerSide("projectsTable", "ReadProject", "contentTable", aoColumnsFunc());

    createDataTableWithPermissions(configurations, renderOptionsForProject);
    var oTable = $("#projectsTable").dataTable();
    oTable.fnSort([1, 'asc']);
};

function displayPageLabel() {
    var doc = getDoc();
    
    $("#pageTitle").html(doc.page_project.title.docLabel);
    $("#title").html(displayDocLink(doc.page_project.title));
    $("[name='createProjectField']").html(doc.page_project.button_create.docLabel);
    $("[name='confirmationField']").html(doc.page_project.button_delete.docLabel);
    $("[name='editProjectField']").html(doc.page_project.button_edit.docLabel);
    $("[name='buttonAdd']").html(doc.page_global.buttonAdd.docLabel);
    $("[name='buttonClose']").html(doc.page_global.buttonClose.docLabel);
    $("[name='buttonConfirm']").html(doc.page_global.buttonConfirm.docLabel);
    $("[name='buttonDismiss']").html(doc.page_global.buttonDismiss.docLabel);
    $("[name='idProjectField']").html(displayDocLink(doc.project.idproject));
    $("[name='activeField']").html(displayDocLink(doc.project.active));
    $("[name='codeField']").html(displayDocLink(doc.project.code));
    $("[name='descriptionField']").html(displayDocLink(doc.project.description));
    displayInvariantList("PROJECTACTIVE", "Active");
    displayFooter(doc);
}

function deleteProjectHandlerClick() {
    var idProject = $('#confirmationModal').find('#hiddenField').prop("value");
    var jqxhr = $.post("DeleteProject", {id: idProject}, "json");
    $.when(jqxhr).then(function (data) {
        var messageType = getAlertType(data.messageType);
        if (messageType === "success") {
            //redraw the datatable
            var oTable = $("#projectsTable").dataTable();
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

function deleteProject(idProject) {
    clearResponseMessageMainPage();
    var doc = getDoc();
    var messageComplete = doc.page_global.deleteMessage.docLabel;
    messageComplete = messageComplete.replace("%TABLE%", doc.project.idproject.docLabel);
    messageComplete = messageComplete.replace("%ENTRY%", idProject);
    showModalConfirmation(deleteProjectHandlerClick, doc.page_project.button_delete.docLabel, messageComplete, idProject);
}

function saveNewProjectHandler() {
    clearResponseMessage($('#addProjectModal'));
    var formAdd = $("#addProjectModal #addProjectModalForm");

    var nameElement = formAdd.find("#idProject");
    var nameElementEmpty = nameElement.prop("value") === '';
    if (nameElementEmpty) {
        var localMessage = new Message("danger", "Please specify the name of the project!");
        nameElement.parents("div.form-group").addClass("has-error");
        showMessage(localMessage, $('#addProjectModal'));
    } else {
        nameElement.parents("div.form-group").removeClass("has-error");
    }

    var codeElement = formAdd.find("#VCCode");
    var codeElementEmpty = codeElement.prop("value") === '';
    if (codeElementEmpty) {
        var localMessage = new Message("danger", "Please specify the code of the project!");
        codeElement.parents("div.form-group").addClass("has-error");
        showMessage(localMessage, $('#addProjectModal'));
    } else {
        codeElement.parents("div.form-group").removeClass("has-error");
    }

    // verif if all mendatory fields are not empty
    if (nameElementEmpty || codeElementEmpty)
        return;

    showLoaderInModal('#addProjectModal');
    var jqxhr = $.post("CreateProject", formAdd.serialize());
    $.when(jqxhr).then(function (data) {
        hideLoaderInModal('#addProjectModal');
        console.log(data.messageType);
        if (getAlertType(data.messageType) === 'success') {
            var oTable = $("#projectsTable").dataTable();
            oTable.fnDraw(true);
            showMessage(data);
            $('#addProjectModal').modal('hide');
        } else {
            showMessage(data, $('#addProjectModal'));
        }
    }).fail(handleErrorAjaxAfterTimeout);
}

function saveUpdateProjectHandler() {
    clearResponseMessage($('#editProjectModal'));
    var formEdit = $('#editProjectModal #editProjectModalForm');
    showLoaderInModal('#editProjectModal');

    var jqxhr = $.post("UpdateProject", formEdit.serialize(), "json");
    $.when(jqxhr).then(function (data) {
        // unblock when remote call returns 
        hideLoaderInModal('#editProjectModal');
        if (getAlertType(data.messageType) === "success") {
            var oTable = $("#projectsTable").dataTable();
            oTable.fnDraw(true);
            $('#editProjectModal').modal('hide');
            showMessage(data);

        } else {
            showMessage(data, $('#editProjectModal'));
        }
    }).fail(handleErrorAjaxAfterTimeout);
}

function addProjectModalCloseHandler() {
    // reset form values
    $('#addProjectModal #addProjectModalForm')[0].reset();
    // remove all errors on the form fields
    $(this).find('div.has-error').removeClass("has-error");
    // clear the response messages of the modal
    clearResponseMessage($('#addProjectModal'));
}

function editProjectModalCloseHandler() {
    // reset form values
    $('#editProjectModal #editProjectModalForm')[0].reset();
    // remove all errors on the form fields
    $(this).find('div.has-error').removeClass("has-error");
    // clear the response messages of the modal
    clearResponseMessage($('#editProjectModal'));
}

function CreateProjectClick() {
    clearResponseMessageMainPage();
    $('#addProjectModal').modal('show');
}

function editProject(id) {
    clearResponseMessageMainPage();
    var jqxhr = $.getJSON("ReadProject", "action=1&idProject=" + id);
    $.when(jqxhr).then(function (data) {
        var obj = data["contentTable"];

        var formEdit = $('#editProjectModal');

        formEdit.find("#idProject").prop("value", id);
        formEdit.find("#VCCode").prop("value", obj["code"]);
        formEdit.find("#Description").prop("value", obj["description"]);
        formEdit.find("#Active").prop("value", obj["active"]);

        formEdit.modal('show');
    });
}

function renderOptionsForProject(data) {
    var doc = getDoc();
    //check if user has permissions to perform the add and import operations
    if (data["hasPermissions"]) {
        if ($("#createProjectButton").length === 0) {
            var contentToAdd = "<div class='marginBottom10'><button id='createProjectButton' type='button' class='btn btn-default'>\n\
            " + doc.page_project.button_create.docLabel + "</button></div>";

            $("#projectsTable_wrapper div.ColVis").before(contentToAdd);
            $('#project #createProjectButton').click(CreateProjectClick);
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
                var editProject = '<button id="editProject" onclick="editProject(\'' + obj["idProject"] + '\');"\n\
                                class="editProject btn btn-default btn-xs margin-right5" \n\
                                name="editProject" title="\'' + doc.page_project.button_edit.docLabel + '\'" type="button">\n\
                                <span class="glyphicon glyphicon-pencil"></span></button>';
                var deleteProject = '<button id="deleteProject" onclick="deleteProject(\'' + obj["idProject"] + '\');" \n\
                                class="deleteProject btn btn-default btn-xs margin-right5" \n\
                                name="deleteProject" title="\'' + doc.page_project.button_delete.docLabel + '\'" type="button">\n\
                                <span class="glyphicon glyphicon-trash"></span></button>';

                return '<div class="center btn-group width150">' + editProject + deleteProject + '</div>';
            }
        },
        {"data": "idProject",
            "sName": "idProject",
            "title": displayDocLink(doc.project.idproject)},
        {"data": "code",
            "sName": "VCCode",
            "title": displayDocLink(doc.project.code)},
        {"data": "description",
            "sName": "description",
            "title": displayDocLink(doc.project.description)},
        {"data": "active",
            "sName": "active",
            "title": displayDocLink(doc.project.active)},
        {"data": "dateCreation",
            "sName": "dateCre",
            "title": displayDocLink(doc.project.dateCreation)}
    ];
    return aoColumns;
}