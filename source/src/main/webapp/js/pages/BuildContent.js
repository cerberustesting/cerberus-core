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
        initBuildContentPage();
    });
});

function initBuildContentPage() {
    displayPageLabel();
    // handle the click for specific action buttons
    $("#addApplicationButton").click(saveNewBrpHandler);
    $("#editApplicationButton").click(saveUpdateBrpHandler);

    //clear the modals fields when closed
    $('#addApplicationModal').on('hidden.bs.modal', addBrpModalCloseHandler);
    $('#editApplicationModal').on('hidden.bs.modal', editBrpModalCloseHandler);

    //configure and create the dataTable
    var configurations = new TableConfigurationsServerSide("buildrevisionparametersTable", "ReadBuildRevisionParameters?system=" + getUser().defaultSystem, "contentTable", aoColumnsFunc());

    var table = createDataTableWithPermissions(configurations, renderOptionsForBrp);

    table.fnSort([1, 'asc']);
}
;

function displayPageLabel() {
    var doc = new Doc();

    displayHeaderLabel(doc);
    $("#pageTitle").html(doc.getDocLabel("page_application", "title"));
    $("#title").html(doc.getDocOnline("page_application", "title"));
    $("[name='createApplicationField']").html(doc.getDocLabel("page_application", "button_create"));
    $("[name='confirmationField']").html(doc.getDocLabel("page_application", "button_delete"));
    $("[name='editApplicationField']").html(doc.getDocLabel("page_application", "button_edit"));
    $("[name='buttonAdd']").html(doc.getDocLabel("page_global", "buttonAdd"));
    $("[name='buttonClose']").html(doc.getDocLabel("page_global", "buttonClose"));
    $("[name='buttonConfirm']").html(doc.getDocLabel("page_global", "buttonConfirm"));
    $("[name='buttonDismiss']").html(doc.getDocLabel("page_global", "buttonDismiss"));
    $("[name='applicationField']").html(doc.getDocOnline("application", "Application"));
    $("[name='descriptionField']").html(doc.getDocOnline("application", "Description"));
    $("[name='sortField']").html(doc.getDocOnline("application", "sort"));
    $("[name='typeField']").html(doc.getDocOnline("application", "type"));
    $("[name='systemField']").html(doc.getDocOnline("application", "system"));
    $("[name='subsystemField']").html(doc.getDocOnline("application", "subsystem"));
    $("[name='svnurlField']").html(doc.getDocOnline("application", "svnurl"));
    $("[name='bugtrackerurlField']").html(doc.getDocOnline("application", "bugtrackerurl"));
    $("[name='bugtrackernewurlField']").html(doc.getDocOnline("application", "bugtrackernewurl"));
    $("[name='deploytypeField']").html(doc.getDocOnline("application", "deploytype"));
    $("[name='mavengroupidField']").html(doc.getDocOnline("application", "mavengroupid"));
    displayFooter(doc);
}

function deleteBrpHandlerClick() {
    var idApplication = $('#confirmationModal').find('#hiddenField').prop("value");
    var jqxhr = $.post("DeleteApplication", {application: idApplication}, "json");
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

function deleteBrp(id) {
    clearResponseMessageMainPage();
    var doc = new Doc();
    var messageComplete = doc.getDocLabel("page_global", "deleteMessage");
    messageComplete = messageComplete.replace("%TABLE%", doc.getDocLabel("application", "Application"));
    messageComplete = messageComplete.replace("%ENTRY%", id);
    showModalConfirmation(deleteBrpHandlerClick, doc.getDocLabel("page_application", "button_delete"), messageComplete, id);
}

function saveNewBrpHandler() {
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

function saveUpdateBrpHandler() {
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

function addBrpModalCloseHandler() {
    // reset form values
    $('#addApplicationModal #addApplicationModalForm')[0].reset();
    // remove all errors on the form fields
    $(this).find('div.has-error').removeClass("has-error");
    // clear the response messages of the modal
    clearResponseMessage($('#addApplicationModal'));
}

function editBrpModalCloseHandler() {
    // reset form values
    $('#editApplicationModal #editApplicationModalForm')[0].reset();
    // remove all errors on the form fields
    $(this).find('div.has-error').removeClass("has-error");
    // clear the response messages of the modal
    clearResponseMessage($('#editApplicationModal'));
}

function CreateBrpClick() {
    clearResponseMessageMainPage();
    // When creating a new application, System takes the default value of the 
    // system already selected in header.
    var formAdd = $('#addApplicationModal');
    formAdd.find("#system").prop("value", getUser().defaultSystem);
    $('#addApplicationModal').modal('show');
}

function editBrp(id) {
    clearResponseMessageMainPage();
    var jqxhr = $.getJSON("ReadBuildRevisionParameters", "id=" + id);
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

function renderOptionsForBrp(data) {
    var doc = new Doc();
    //check if user has permissions to perform the add and import operations
    if (data["hasPermissions"]) {
        if ($("#createApplicationButton").length === 0) {
            var contentToAdd = "<div class='marginBottom10'><button id='createApplicationButton' type='button' class='btn btn-default'>\n\
            " + doc.getDocLabel("page_application", "button_create") + "</button></div>";

            $("#applicationsTable_wrapper div.ColVis").before(contentToAdd);
            $('#application #createApplicationButton').click(CreateBrpClick);
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
                var editBrp = '<button id="editBrp" onclick="editBrp(\'' + obj["id"] + '\');"\n\
                                class="editBrp btn btn-default btn-xs margin-right5" \n\
                                name="editBrp" title="\'' + doc.getDocLabel("page_application", "button_edit") + '\'" type="button">\n\
                                <span class="glyphicon glyphicon-pencil"></span></button>';
                var deleteBrp = '<button id="deleteBrp" onclick="deleteBrp(\'' + obj["id"] + '\');" \n\
                                class="deleteBrp btn btn-default btn-xs margin-right5" \n\
                                name="deleteBrp" title="\'' + doc.getDocLabel("page_application", "button_delete") + '\'" type="button">\n\
                                <span class="glyphicon glyphicon-trash"></span></button>';

                return '<div class="center btn-group width150">' + editBrp + deleteBrp + '</div>';
            }
        },
        {"data": "build",
            "sName": "build",
            "title": doc.getDocOnline("buildrevisionparameters", "Build")},
        {"data": "revision",
            "sName": "revision",
            "title": doc.getDocOnline("buildrevisionparameters", "Revision")},
        {"data": "release",
            "sName": "release",
            "title": doc.getDocOnline("buildrevisionparameters", "Release")},
        {"data": "application",
            "sName": "application",
            "title": doc.getDocOnline("buildrevisionparameters", "application")},
        {"data": "project",
            "sName": "project",
            "title": doc.getDocOnline("buildrevisionparameters", "project")},
        {"data": "ticketIdFixed",
            "sName": "ticketIdFixed",
            "title": doc.getDocOnline("buildrevisionparameters", "TicketIDFixed")},
        {"data": "bugIdFixed",
            "sName": "bugIdFixed",
            "title": doc.getDocOnline("buildrevisionparameters", "BugIDFixed")},
        {"data": "link",
            "sName": "link",
            "title": doc.getDocOnline("buildrevisionparameters", "Link")},
        {"data": "releaseOwner",
            "sName": "releaseOwner",
            "title": doc.getDocOnline("buildrevisionparameters", "ReleaseOwner")},
        {"data": "subject",
            "sName": "subject",
            "title": doc.getDocOnline("buildrevisionparameters", "subject")},
        {"data": "datecre",
            "sName": "datecre",
            "title": doc.getDocOnline("buildrevisionparameters", "datecre")},
        {"data": "jenkinsBuildId",
            "sName": "jenkinsBuildId",
            "title": doc.getDocOnline("buildrevisionparameters", "jenkinsbuildid")},
        {"data": "mavenGroupId",
            "sName": "mavenGroupId",
            "title": doc.getDocOnline("buildrevisionparameters", "mavengroupid")},
        {"data": "mavenArtifactId",
            "sName": "mavenArtifactId",
            "title": doc.getDocOnline("buildrevisionparameters", "mavenartifactid")},
        {"data": "mavenVersion",
            "sName": "mavenVersion",
            "title": doc.getDocOnline("buildrevisionparameters", "mavenversion")}
    ];
    return aoColumns;
}