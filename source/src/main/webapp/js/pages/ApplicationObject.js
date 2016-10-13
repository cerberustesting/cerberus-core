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
    $("#addApplicationObjectButton").click(addEntryModalSaveHandler);
    $("#editApplicationObjectButton").click(editEntryModalSaveHandler);

    //clear the modals fields when closed
    $('#addApplicationObjectModal').on('hidden.bs.modal', addEntryModalCloseHandler);
    $('#editApplicationObjectModal').on('hidden.bs.modal', editEntryModalCloseHandler);

    // Load the select needed in localStorage cache.
    getSelectInvariant("ENVIRONMENT", true);
    getSelectInvariant("COUNTRY", true);


    //configure and create the dataTable
    var configurations = new TableConfigurationsServerSide("applicationObjectsTable", "ReadApplicationObject?system=" + getUser().defaultSystem, "contentTable", aoColumnsFunc("applicationObjectsTable"), [3, 'asc']);
    createDataTableWithPermissions(configurations, renderOptionsForApplicationObject, "#applicationObjectList");
}

function displayPageLabel() {
    var doc = new Doc();

    displayHeaderLabel(doc);
    $("#pageTitle").html(doc.getDocLabel("page_applicationObject", "title"));
    $("#title").html(doc.getDocOnline("page_applicationObject", "title"));
    $("[name='createApplicationObjectField']").html(doc.getDocLabel("page_applicationObject", "button_create"));
    $("[name='confirmationField']").html(doc.getDocLabel("page_applicationObject", "button_delete"));
    $("[name='editApplicationObjectField']").html(doc.getDocLabel("page_applicationObject", "button_edit"));
    $("[name='buttonAdd']").html(doc.getDocLabel("page_global", "buttonAdd"));
    $("[name='buttonClose']").html(doc.getDocLabel("page_global", "buttonClose"));
    $("[name='buttonConfirm']").html(doc.getDocLabel("page_global", "buttonConfirm"));
    $("[name='buttonDismiss']").html(doc.getDocLabel("page_global", "buttonDismiss"));
    $("[name='applicationObjectField']").html(doc.getDocOnline("applicationObject", "ApplicationObject"));
    $("[name='descriptionField']").html(doc.getDocOnline("applicationObject", "Description"));
    $("[name='sortField']").html(doc.getDocOnline("applicationObject", "sort"));
    $("[name='typeField']").html(doc.getDocOnline("applicationObject", "type"));
    $("[name='systemField']").html(doc.getDocOnline("applicationObject", "system"));
    $("[name='subsystemField']").html(doc.getDocOnline("applicationObject", "subsystem"));
    $("[name='svnurlField']").html(doc.getDocOnline("applicationObject", "svnurl"));
    $("[name='bugtrackerurlField']").html(doc.getDocOnline("applicationObject", "bugtrackerurl"));
    $("[name='bugtrackernewurlField']").html(doc.getDocOnline("applicationObject", "bugtrackernewurl"));
    $("[name='deploytypeField']").html(doc.getDocOnline("applicationObject", "deploytype"));
    $("[name='mavengroupidField']").html(doc.getDocOnline("applicationObject", "mavengroupid"));

    $("[name='tabsEdit1']").html(doc.getDocOnline("page_applicationObject", "tabDef"));
    $("[name='tabsEdit2']").html(doc.getDocOnline("page_applicationObject", "tabEnv"));

    $("#environmentHeader").html(doc.getDocOnline("invariant", "ENVIRONMENT"));
    $("#countryHeader").html(doc.getDocOnline("invariant", "COUNTRY"));
    $("#ipHeader").html(doc.getDocOnline("countryenvironmentparameters", "IP") 
            + '<br>' + doc.getDocOnline("countryenvironmentparameters", "URLLOGIN"));
    $("#urlHeader").html(doc.getDocOnline("countryenvironmentparameters", "URL") 
            + '<br>' + doc.getDocOnline("countryenvironmentparameters", "domain"));
    $("#var1Header").html(doc.getDocOnline("countryenvironmentparameters", "Var1") 
            + '<br>' + doc.getDocOnline("countryenvironmentparameters", "Var2"));
    $("#var3Header").html(doc.getDocOnline("countryenvironmentparameters", "Var3") 
            + '<br>' + doc.getDocOnline("countryenvironmentparameters", "Var4"));

    displayInvariantList("system", "SYSTEM", false);
    displayInvariantList("type", "APPLITYPE", false);
    displayDeployTypeList("deploytype");
    displayFooter(doc);
}

function renderOptionsForApplicationObject(data) {
    var doc = new Doc();
    //check if user has permissions to perform the add and import operations
    if (data["hasPermissions"]) {
        if ($("#createApplicationObjectButton").length === 0) {
            var contentToAdd = "<div class='marginBottom10'><button id='createApplicationObjectButton' type='button' class='btn btn-default'>\n\
            <span class='glyphicon glyphicon-plus-sign'></span> " + doc.getDocLabel("page_applicationObject", "button_create") + "</button></div>";

            $("#applicationObjectsTable_wrapper div#applicationObjectsTable_length").before(contentToAdd);
            $('#applicationObjectList #createApplicationObjectButton').click(addEntryClick);
        }
    }
}

function deleteEntryHandlerClick() {
    var idApplicationObject = $('#confirmationModal').find('#hiddenField1').prop("value");
    var jqxhr = $.post("DeleteApplicationObject", {applicationObject: idApplicationObject}, "json");
    $.when(jqxhr).then(function (data) {
        var messageType = getAlertType(data.messageType);
        if (messageType === "success") {
            //redraw the datatable
            var oTable = $("#applicationObjectsTable").dataTable();
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

function deleteEntryClick(idApplicationObject) {
    clearResponseMessageMainPage();
    var doc = new Doc();
    var messageComplete = doc.getDocLabel("page_applicationObject", "message_delete");
    messageComplete = messageComplete.replace("%ENTRY%", idApplicationObject);
    showModalConfirmation(deleteEntryHandlerClick, doc.getDocLabel("page_applicationObject", "button_delete"), messageComplete, idApplicationObject, "", "", "");
}

function addEntryModalSaveHandler() {
    clearResponseMessage($('#addApplicationObjectModal'));
    var formAdd = $("#addApplicationObjectModal #addApplicationObjectModalForm");

    // Get the header data from the form.
    var dataForm = convertSerialToJSONObject(formAdd.serialize());
    
    showLoaderInModal('#addApplicationObjectModal');
    var jqxhr = $.post("CreateApplicationObject", dataForm);
    $.when(jqxhr).then(function (data) {
        hideLoaderInModal('#addApplicationObjectModal');
//        console.log(data.messageType);
        if (getAlertType(data.messageType) === 'success') {
            var oTable = $("#applicationObjectsTable").dataTable();
            oTable.fnDraw(true);
            showMessage(data);
            $('#addApplicationObjectModal').modal('hide');
        } else {
            showMessage(data, $('#addApplicationObjectModal'));
        }
    }).fail(handleErrorAjaxAfterTimeout);
}

function addEntryModalCloseHandler() {
    // reset form values
    $('#addApplicationObjectModal #addApplicationObjectModalForm')[0].reset();
    // remove all errors on the form fields
    $(this).find('div.has-error').removeClass("has-error");
    // clear the response messages of the modal
    clearResponseMessage($('#addApplicationObjectModal'));
}

function addEntryClick() {
    clearResponseMessageMainPage();

    // When creating a new applicationObject, System takes the default value of the 
    // system already selected in header.
    var formAdd = $('#addApplicationObjectModal');
    // Default to NONE to Application.
    formAdd.find("#type").val("NONE");

    $('#addApplicationObjectModal').modal('show');
}

function editEntryModalSaveHandler() {
    clearResponseMessage($('#editApplicationObjectModal'));
    var formEdit = $('#editApplicationObjectModal #editApplicationObjectModalForm');

    // Get the header data from the form.
    var data = convertSerialToJSONObject(formEdit.serialize());

    showLoaderInModal('#editApplicationObjectModal');
    $.ajax({
        url: "UpdateApplicationObject",
        async: true,
        method: "POST",
        data: {application: data.application,
            object: data.object,
            value: data.value,
            screenhotfilename: data.screenhotfilename},
        success: function (data) {
            hideLoaderInModal('#editApplicationObjectModal');
            if (getAlertType(data.messageType) === "success") {
                var oTable = $("#applicationObjectsTable").dataTable();
                oTable.fnDraw(true);
                $('#editApplicationObjectModal').modal('hide');
                showMessage(data);
            } else {
                showMessage(data, $('#editApplicationObjectModal'));
            }
        },
        error: showUnexpectedError
    });

}

function editEntryModalCloseHandler() {
    // reset form values
    $('#editApplicationObjectModal #editApplicationObjectModalForm')[0].reset();
    // remove all errors on the form fields
    $(this).find('div.has-error').removeClass("has-error");
    // clear the response messages of the modal
    clearResponseMessage($('#editApplicationObjectModal'));
}

function editEntryClick(application, object) {
    clearResponseMessageMainPage();
    var jqxhr = $.getJSON("ReadApplicationObject", "application=" + application, "object=" + object);
    $.when(jqxhr).then(function (data) {
        var obj = data["contentTable"];

        var formEdit = $('#editApplicationObjectModal');

        formEdit.find("#applicationObject").prop("value", id);
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

        if (!(data["hasPermissions"])) { // If readonly, we only readonly all fields
            formEdit.find("#description").prop("readonly", "readonly");
            formEdit.find("#sort").prop("readonly", "readonly");
            formEdit.find("#type").prop("disabled", "disabled");
            formEdit.find("#system").prop("disabled", "disabled");
            formEdit.find("#subsystem").prop("readonly", "readonly");
            formEdit.find("#svnurl").prop("readonly", "readonly");
            formEdit.find("#bugtrackerurl").prop("readonly", "readonly");
            formEdit.find("#bugtrackernewurl").prop("readonly", "readonly");
            formEdit.find("#deploytype").prop("disabled", "disabled");
            formEdit.find("#mavengroupid").prop("readonly", "readonly");

            $('#editApplicationObjectButton').attr('class', '');
            $('#editApplicationObjectButton').attr('hidden', 'hidden');
        }

        formEdit.modal('show');
    });
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

                var editApplicationObject = '<button id="editApplicationObject" onclick="editEntryClick(\'' + obj["Application"] + '\', \'' + obj["Object"] + '\');"\n\
                                    class="editApplicationObject btn btn-default btn-xs margin-right5" \n\
                                    name="editApplicationObject" title="' + doc.getDocLabel("page_applicationObject", "button_edit") + '" type="button">\n\
                                    <span class="glyphicon glyphicon-pencil"></span></button>';
                var viewApplicationObject = '<button id="editApplicationObject" onclick="editEntryClick(\'' + obj["Application"] + '\', \'' + obj["Object"] + '\');"\n\
                                    class="editApplicationObject btn btn-default btn-xs margin-right5" \n\
                                    name="editApplicationObject" title="' + doc.getDocLabel("page_applicationObject", "button_edit") + '" type="button">\n\
                                    <span class="glyphicon glyphicon-eye-open"></span></button>';
                var deleteApplicationObject = '<button id="deleteApplicationObject" onclick="deleteEntryClick(\'' + obj["Application"] + '\', \'' + obj["Object"] + '\');" \n\
                                    class="deleteApplicationObject btn btn-default btn-xs margin-right5" \n\
                                    name="deleteApplicationObject" title="' + doc.getDocLabel("page_applicationObject", "button_delete") + '" type="button">\n\
                                    <span class="glyphicon glyphicon-trash"></span></button>';
                if (hasPermissions === "true") { //only draws the options if the user has the correct privileges
                    return '<div class="center btn-group width150">' + editApplicationObject + deleteApplicationObject + '</div>';
                }
                return '<div class="center btn-group width150">' + viewApplicationObject + '</div>';
            }
        },
        {"data": "application",
            "sName": "application",
            "title": doc.getDocOnline("applicationObject", "Application")},
        {"data": "object",
            "sName": "object",
            "title": doc.getDocOnline("applicationObject", "Object")},
        {"data": "value",
            "sName": "value",
            "title": doc.getDocOnline("applicationObject", "Value")},
        {"data": "screenshotfilename",
            "sName": "screenshotfilename",
            "title": doc.getDocOnline("applicationObject", "ScreenshotFileName")},
        {"data": "usrcreated",
            "sName": "usrcreated",
            "title": doc.getDocOnline("applicationObject", "UsrCreated")},
        {"data": "datecreated",
            "sName": "datecreated",
            "title": doc.getDocOnline("applicationObject", "DateCreated")},
        {"data": "usrmodif",
            "sName": "usrmodif",
            "title": doc.getDocOnline("applicationObject", "UsrModif")
        },
        {"data": "datemodif",
            "sName": "datemodif",
            "title": doc.getDocOnline("applicationObject", "DateModif")
        }
    ];
    return aoColumns;
}