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
$.when($.getScript("js/pages/global/global.js")).then(function () {
    $(document).ready(function () {
        initPage();
    });
});

function initPage() {
    displayPageLabel();
    // handle the click for specific action buttons
    $("#addLabelButton").click(addEntryModalSaveHandler);
    $("#editLabelButton").click(editEntryModalSaveHandler);

    //clear the modals fields when closed
    $('#addLabelModal').on('hidden.bs.modal', addEntryModalCloseHandler);
    $('#editLabelModal').on('hidden.bs.modal', editEntryModalCloseHandler);

    //configure and create the dataTable
    var configurations = new TableConfigurationsServerSide("labelsTable", "ReadLabel?system=" + getUser().defaultSystem, "contentTable", aoColumnsFunc("labelsTable"), [3, 'asc']);
    createDataTableWithPermissions(configurations, renderOptionsForLabel, "#labelList", undefined, true);
}

function displayPageLabel() {
    var doc = new Doc();

    displayHeaderLabel(doc);
    $("#pageTitle").html(doc.getDocLabel("page_label", "title"));
    $("#title").html(doc.getDocOnline("page_label", "title"));
    $("[name='createLabelField']").html(doc.getDocLabel("page_label", "btn_create"));
    $("[name='confirmationField']").html(doc.getDocLabel("page_label", "btn_delete"));
    $("[name='editLabelField']").html(doc.getDocLabel("page_label", "btn_edit"));
    $("[name='buttonAdd']").html(doc.getDocLabel("page_global", "buttonAdd"));
    $("[name='buttonClose']").html(doc.getDocLabel("page_global", "buttonClose"));
    $("[name='buttonConfirm']").html(doc.getDocLabel("page_global", "buttonConfirm"));
    $("[name='buttonDismiss']").html(doc.getDocLabel("page_global", "buttonDismiss"));
    $("[name='labelField']").html(doc.getDocOnline("label", "label"));
    $("[name='descriptionField']").html(doc.getDocOnline("label", "description"));
    $("[name='colorField']").html(doc.getDocOnline("label", "color"));
    $("[name='parentLabelField']").html(doc.getDocOnline("label", "parentid"));
    $("[name='parentLabel']").attr("readonly", "readonly");

    $("[name='tabsEdit1']").html(doc.getDocOnline("page_label", "tabDef"));
    $("[name='tabsEdit2']").html(doc.getDocOnline("page_label", "tabEnv"));

    displayInvariantList("system", "SYSTEM", false);
    displayFooter(doc);
}

function renderOptionsForLabel(data) {
    var doc = new Doc();
    //check if user has permissions to perform the add and import operations
    if (data["hasPermissions"]) {
        if ($("#createLabelButton").length === 0) {
            var contentToAdd = "<div class='marginBottom10'><button id='createLabelButton' type='button' class='btn btn-default'>\n\
            <span class='glyphicon glyphicon-plus-sign'></span> " + doc.getDocLabel("page_label", "btn_create") + "</button></div>";

            $("#labelsTable_wrapper div#labelsTable_length").before(contentToAdd);
            $('#labelList #createLabelButton').click(addEntryClick);
        }
    }
}

function deleteEntryHandlerClick() {
    var idLabel = $('#confirmationModal').find('#hiddenField1').prop("value");
    var jqxhr = $.post("DeleteLabel", {id: idLabel}, "json");
    $.when(jqxhr).then(function (data) {
        var messageType = getAlertType(data.messageType);
        if (messageType === "success") {
            //redraw the datatable
            var oTable = $("#labelsTable").dataTable();
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

function deleteEntryClick(id) {
    clearResponseMessageMainPage();
    var doc = new Doc();
    var messageComplete = doc.getDocLabel("page_label", "message_delete");
    messageComplete = messageComplete.replace("%ENTRY%", id);
    showModalConfirmation(deleteEntryHandlerClick, doc.getDocLabel("page_label", "button_delete"), messageComplete, id, "", "", "");
}

function addEntryModalSaveHandler() {
    clearResponseMessage($('#addLabelModal'));
    var formAdd = $("#addLabelModal #addLabelModalForm");

    var nameElement = formAdd.find("#addLabelModalForm");
    var nameElementEmpty = nameElement.prop("value") === '';
    if (nameElementEmpty) {
        var localMessage = new Message("danger", "Please specify label!");
        nameElement.parents("div.form-group").addClass("has-error");
        showMessage(localMessage, $('#addLabelModal'));
    } else {
        nameElement.parents("div.form-group").removeClass("has-error");
    }

    // verif if all mendatory fields are not empty
    if (nameElementEmpty)
        return;

    // Get the header data from the form.
    var dataForm = convertSerialToJSONObject(formAdd.serialize());

    showLoaderInModal('#addLabelModal');
    var jqxhr = $.post("CreateLabel", dataForm);
    $.when(jqxhr).then(function (data) {
        hideLoaderInModal('#addLabelModal');
//        console.log(data.messageType);
        if (getAlertType(data.messageType) === 'success') {
            var oTable = $("#labelsTable").dataTable();
            oTable.fnDraw(true);
            showMessage(data);
            $('#addLabelModal').modal('hide');
        } else {
            showMessage(data, $('#addLabelModal'));
        }
    }).fail(handleErrorAjaxAfterTimeout);
}

function addEntryModalCloseHandler() {
    // reset form values
    $('#addLabelModal #addLabelModalForm')[0].reset();
    // remove all errors on the form fields
    $(this).find('div.has-error').removeClass("has-error");
    // clear the response messages of the modal
    clearResponseMessage($('#addLabelModal'));
}

function addEntryClick() {
    clearResponseMessageMainPage();

    // When creating a new label, System takes the default value of the 
    // system already selected in header.
    var formAdd = $('#addLabelModal');
    formAdd.find("#system").prop("value", getUser().defaultSystem);

    $('#addLabelModal').modal('show');

    //ColorPicker
    $("[name='colorDiv']").colorpicker();
    $("[name='colorDiv']").colorpicker('setValue', '#000000');
}

function editEntryModalSaveHandler() {
    clearResponseMessage($('#editLabelModal'));
    var formEdit = $('#editLabelModal #editLabelModalForm');

    // Get the header data from the form.
    var data = convertSerialToJSONObject(formEdit.serialize());

    showLoaderInModal('#editLabelModal');
    $.ajax({
        url: "UpdateLabel",
        async: true,
        method: "POST",
        data: {id: data.id,
            label: data.label,
            color: data.color,
            parentLabel: data.parentLabel,
            system: data.system,
            description: data.description},
        success: function (data) {
            hideLoaderInModal('#editLabelModal');
            if (getAlertType(data.messageType) === "success") {
                var oTable = $("#labelsTable").dataTable();
                oTable.fnDraw(true);
                $('#editLabelModal').modal('hide');
                showMessage(data);
            } else {
                showMessage(data, $('#editLabelModal'));
            }
        },
        error: showUnexpectedError
    });

}

function editEntryModalCloseHandler() {
    // reset form values
    $('#editLabelModal #editLabelModalForm')[0].reset();
    // remove all errors on the form fields
    $(this).find('div.has-error').removeClass("has-error");
    // clear the response messages of the modal
    clearResponseMessage($('#editLabelModal'));
}

function editEntryClick(id, system) {
    clearResponseMessageMainPage();
    var jqxhr = $.getJSON("ReadLabel", "id=" + id);
    $.when(jqxhr).then(function (data) {
        var obj = data["contentTable"];

        var formEdit = $('#editLabelModal');

        formEdit.find("#id").prop("value", id);
        formEdit.find("#label").prop("value", obj["label"]);
        formEdit.find("#color").prop("value", obj["color"]);
        formEdit.find("#parentLabel").prop("value", obj["parentLabel"]);
        formEdit.find("#description").prop("value", obj["description"]);
        formEdit.find("#system").prop("value", obj["system"]);

        if (!(data["hasPermissions"])) { // If readonly, we only readonly all fields
            formEdit.find("#label").prop("readonly", "readonly");
            formEdit.find("#color").prop("readonly", "readonly");
            formEdit.find("#parentLabel").prop("disabled", "disabled");
            formEdit.find("#description").prop("disabled", "disabled");
            formEdit.find("#system").prop("disabled", "disabled");

            $('#editLabelButton').attr('class', '');
            $('#editLabelButton').attr('hidden', 'hidden');
        }

        //ColorPicker
        $("[name='colorDiv']").colorpicker();
        $("[name='colorDiv']").colorpicker('setValue', obj["color"]);

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

                var editLabel = '<button id="editLabel" onclick="editEntryClick(\'' + obj["id"] + '\', \'' + obj["system"] + '\');"\n\
                                    class="editLabel btn btn-default btn-xs margin-right5" \n\
                                    name="editLabel" title="' + doc.getDocLabel("page_label", "btn_edit") + '" type="button">\n\
                                    <span class="glyphicon glyphicon-pencil"></span></button>';
                var viewLabel = '<button id="editLabel" onclick="editEntryClick(\'' + obj["id"] + '\', \'' + obj["system"] + '\');"\n\
                                    class="editLabel btn btn-default btn-xs margin-right5" \n\
                                    name="editLabel" title="' + doc.getDocLabel("page_label", "btn_view") + '" type="button">\n\
                                    <span class="glyphicon glyphicon-eye-open"></span></button>';
                var deleteLabel = '<button id="deleteLabel" onclick="deleteEntryClick(\'' + obj["id"] + '\');" \n\
                                    class="deleteLabel btn btn-default btn-xs margin-right5" \n\
                                    name="deleteLabel" title="' + doc.getDocLabel("page_label", "btn_delete") + '" type="button">\n\
                                    <span class="glyphicon glyphicon-trash"></span></button>';
                if (hasPermissions === "true") { //only draws the options if the user has the correct privileges
                    return '<div class="center btn-group width150">' + editLabel + deleteLabel + '</div>';
                }
                return '<div class="center btn-group width150">' + viewLabel + '</div>';
            }
        },
        {"data": "system",
            "sName": "system",
            "title": doc.getDocOnline("label", "system")},
        {"data": "label",
            "sName": "label",
            "title": doc.getDocOnline("label", "label")},
        {"data": "color",
            "sName": "color",
            "title": doc.getDocOnline("label", "color")},
        {"data": "display",
            "sName": "display",
            "title": doc.getDocOnline("page_label", "display"),
            "bSortable": false,
            "bSearchable": false,
            "render": function (data, type, full, meta) {
                return '<span class="label label-primary" style="background-color:' + data.color + '">' + data.label + '</span> ';
            }
        },
        {"data": "parentLabel",
            "sName": "parentLabel",
            "title": doc.getDocOnline("label", "parentid")},
        {"data": "description",
            "sName": "description",
            "title": doc.getDocOnline("label", "description")}

    ];
    return aoColumns;
}