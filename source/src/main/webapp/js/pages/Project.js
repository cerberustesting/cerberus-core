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
    var configurations = new TableConfigurationsServerSide("projectsTable", "ReadProject", "contentTable", aoColumnsFunc());

    createDataTableWithPermissions(configurations, renderOptionsForProject);
    var oTable = $("#projectsTable").dataTable();
    oTable.fnSort([1, 'asc']);
}
;

function displayPageLabel() {
    var doc = new Doc();

    displayHeaderLabel(doc);
    displayGlobalLabel(doc);
    $("#pageTitle").html(doc.getDocLabel("page_project", "title"));
    $("#title").html(doc.getDocOnline("page_project", "title"));
    $("[name='addEntryField']").html(doc.getDocLabel("page_project", "button_create"));
    $("[name='confirmationField']").html(doc.getDocLabel("page_project", "button_delete"));
    $("[name='editEntryField']").html(doc.getDocLabel("page_project", "button_edit"));
    $("[name='idProjectField']").html(doc.getDocOnline("project", "idproject"));
    $("[name='activeField']").html(doc.getDocOnline("project", "active"));
    $("[name='codeField']").html(doc.getDocOnline("project", "code"));
    $("[name='descriptionField']").html(doc.getDocOnline("project", "description"));
    displayInvariantList("PROJECTACTIVE", "Active");
    displayFooter(doc);
}

function deleteEntryHandlerClick() {
    var idProject = $('#confirmationModal').find('#hiddenField').prop("value");
    var jqxhr = $.post("DeleteProject", {idproject: idProject}, "json");
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

function deleteEntry(entry) {
    clearResponseMessageMainPage();
    var doc = new Doc();
    var messageComplete = doc.getDocLabel("page_global", "deleteMessage");
    messageComplete = messageComplete.replace("%TABLE%", doc.getDocLabel("project", "idproject"));
    messageComplete = messageComplete.replace("%ENTRY%", entry);
    showModalConfirmation(deleteEntryHandlerClick, doc.getDocLabel("page_project", "button_delete"), messageComplete, entry);
}

function saveEntry(servletName, modalID, form) {
    var jqxhr = $.post(servletName, form.serialize());
    $.when(jqxhr).then(function (data) {
        hideLoaderInModal(modalID);
        if (getAlertType(data.messageType) === 'success') {
            var oTable = $("#projectsTable").dataTable();
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

    var nameElement = formAdd.find("#idProject");
    var nameElementEmpty = nameElement.prop("value") === '';
    if (nameElementEmpty) {
        var localMessage = new Message("danger", "Please specify the name of the project!");
        nameElement.parents("div.form-group").addClass("has-error");
        showMessage(localMessage, $('#addEntryModal'));
    } else {
        nameElement.parents("div.form-group").removeClass("has-error");
    }

    var codeElement = formAdd.find("#VCCode");
    var codeElementEmpty = codeElement.prop("value") === '';
    if (codeElementEmpty) {
        var localMessage = new Message("danger", "Please specify the code of the project!");
        codeElement.parents("div.form-group").addClass("has-error");
        showMessage(localMessage, $('#addEntryModal'));
    } else {
        codeElement.parents("div.form-group").removeClass("has-error");
    }

    // verif if all mendatory fields are not empty
    if (nameElementEmpty || codeElementEmpty)
        return;

    showLoaderInModal('#addEntryModal');
    saveEntry("CreateProject", "#addEntryModal", formAdd);

}

function saveUpdateEntryHandler() {
    clearResponseMessage($('#editEntryModal'));
    var formEdit = $('#editEntryModal #editEntryModalForm');

    showLoaderInModal('#editEntryModal');
    saveEntry("UpdateProject", "#editEntryModal", formEdit);
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

function CreateProjectClick() {
    clearResponseMessageMainPage();
    $('#addEntryModal').modal('show');
}

function editEntry(id) {
    clearResponseMessageMainPage();
    var jqxhr = $.getJSON("ReadProject", "idProject=" + id);
    $.when(jqxhr).then(function (data) {
        var obj = data["contentTable"];

        var formEdit = $('#editEntryModal');

        formEdit.find("#idProject").prop("value", id);
        formEdit.find("#VCCode").prop("value", obj["code"]);
        formEdit.find("#Description").prop("value", obj["description"]);
        formEdit.find("#Active").prop("value", obj["active"]);

        formEdit.modal('show');
    });
}

function renderOptionsForProject(data) {
    var doc = new Doc();
    //check if user has permissions to perform the add and import operations
    if (data["hasPermissions"]) {
        if ($("#createProjectButton").length === 0) {
            var contentToAdd = "<div class='marginBottom10'><button id='createProjectButton' type='button' class='btn btn-default'>\n\
            " + doc.getDocLabel("page_project", "button_create") + "</button></div>";

            $("#projectsTable_wrapper div.ColVis").before(contentToAdd);
            $('#project #createProjectButton').click(CreateProjectClick);
        }
    }
}

function aoColumnsFunc() {
    var doc = new Doc();
    var aoColumns = [
        {"data": null,
            "title": doc.getDocLabel("page_global", "columnAction"),
            "bSortable": false,
            "bSearchable": false,
            "mRender": function (data, type, obj) {
                var editEntry = '<button id="editEntry" onclick="editEntry(\'' + obj["idProject"] + '\');"\n\
                                class="editEntry btn btn-default btn-xs margin-right5" \n\
                                name="editEntry" title="' + doc.getDocLabel("page_project", "button_edit") + '" type="button">\n\
                                <span class="glyphicon glyphicon-pencil"></span></button>';
                var deleteEntry = '<button id="deleteEntry" onclick="deleteEntry(\'' + obj["idProject"] + '\');" \n\
                                class="deleteEntry btn btn-default btn-xs margin-right5" \n\
                                name="deleteEntry" title="' + doc.getDocLabel("page_project", "button_delete") + '" type="button">\n\
                                <span class="glyphicon glyphicon-trash"></span></button>';

                return '<div class="center btn-group width150">' + editEntry + deleteEntry + '</div>';
            }
        },
        {"data": "idProject",
            "sName": "idProject",
            "title": doc.getDocOnline("project", "idproject")},
        {"data": "code",
            "sName": "VCCode",
            "title": doc.getDocOnline("project", "code")},
        {"data": "description",
            "sName": "description",
            "title": doc.getDocOnline("project", "description")},
        {"data": "active",
            "sName": "active",
            "title": doc.getDocOnline("project", "active")},
        {"data": "dateCreation",
            "sName": "dateCre",
            "title": doc.getDocOnline("project", "dateCreation")}
    ];
    return aoColumns;
}