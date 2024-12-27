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
    var configurations = new TableConfigurationsServerSide("buildrevdefinitionsTable", "ReadBuildRevisionInvariant", "contentTable", aoColumnsFunc("buildrevdefinitionsTable"), [2, 'asc']);

    var table = createDataTableWithPermissions(configurations, renderOptionsForBuildRevDefinition, "#buildrevdefinition", undefined, true);

}

function displayPageLabel() {
    var doc = new Doc();

    displayHeaderLabel(doc);
    displayGlobalLabel(doc);
    $("#pageTitle").html(doc.getDocLabel("page_buildrevdefinition", "title"));
    $("#title").html(doc.getDocOnline("page_buildrevdefinition", "title"));
    $("[name='addEntryField']").html(doc.getDocLabel("page_buildrevdefinition", "button_create"));
    $("[name='confirmationField']").html(doc.getDocLabel("page_buildrevdefinition", "button_delete"));
    $("[name='editEntryField']").html(doc.getDocLabel("page_buildrevdefinition", "button_edit"));
    $("[name='systemField']").html(doc.getDocOnline("buildrevisioninvariant", "system"));
    $("[name='levelField']").html(doc.getDocOnline("buildrevisioninvariant", "level"));
    $("[name='seqField']").html(doc.getDocOnline("buildrevisioninvariant", "seq"));
    $("[name='versionnameField']").html(doc.getDocOnline("buildrevisioninvariant", "versionName"));
    displayInvariantList("system", "SYSTEM", false);
    $("[name='level']").append($('<option></option>').text("1").val("1"));
    $("[name='level']").append($('<option></option>').text("2").val("2"));
    displayFooter(doc);
}

function renderOptionsForBuildRevDefinition(data) {
    var doc = new Doc();
    //check if user has permissions to perform the add and import operations
    if (data["hasPermissions"]) {
        if ($("#createBuildRevDefinitionButton").length === 0) {
            var contentToAdd = "<div class='marginBottom10'><button id='createBuildRevDefinitionButton' type='button' class='btn btn-default'>\n\
            <span class='glyphicon glyphicon-plus-sign'></span> " + doc.getDocLabel("page_buildrevdefinition", "button_create") + "</button></div>";

            $("#buildrevdefinitionsTable_wrapper #buildrevdefinitionsTable_length").before(contentToAdd);
            $('#buildrevdefinition #createBuildRevDefinitionButton').click(addEntryClick);
        }
    }
}

function deleteEntryHandlerClick() {
    var system = $('#confirmationModal').find('#hiddenField1').prop("value");
    var level = $('#confirmationModal').find('#hiddenField2').prop("value");
    var seq = $('#confirmationModal').find('#hiddenField3').prop("value");
    var jqxhr = $.post("DeleteBuildRevisionInvariant", {system: encodeURIComponent(system), level: encodeURIComponent(level), seq: encodeURIComponent(seq)}, "json");
    $.when(jqxhr).then(function (data) {
        var messageType = getAlertType(data.messageType);
        if (messageType === "success") {
            //redraw the datatable
            var oTable = $("#buildrevdefinitionsTable").dataTable();
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

function deleteEntryClick(system, level, seq, versionname) {
    clearResponseMessageMainPage();
    var doc = new Doc();
    var messageComplete = doc.getDocLabel("page_global", "message_delete");
    var entry = versionname + " (level : " + level + " sequence : " + seq + ")";
    messageComplete = messageComplete.replace("%TABLE%", "Build Revision Definition");
    messageComplete = messageComplete.replace("%ENTRY%", entry);
    showModalConfirmation(deleteEntryHandlerClick, undefined, doc.getDocLabel("page_buildrevdefinition", "button_delete"), messageComplete, system, level, seq, "");
}

function addEntryModalSaveHandler() {
    clearResponseMessage($('#addEntryModal'));
    var formAdd = $("#addEntryModal #addEntryModalForm");

    var nameElement = formAdd.find("#seq");
    var nameElementEmpty = nameElement.prop("value") === '';
    if (nameElementEmpty) {
        var localMessage = new Message("danger", "Please specify a sequence!");
        nameElement.parents("div.form-group").addClass("has-error");
        showMessage(localMessage, $('#addEntryModal'));
    } else {
        nameElement.parents("div.form-group").removeClass("has-error");
    }

    var codeElement = formAdd.find("#versionname");
    var codeElementEmpty = codeElement.prop("value") === '';
    if (codeElementEmpty) {
        var localMessage = new Message("danger", "Please specify a version name!");
        codeElement.parents("div.form-group").addClass("has-error");
        showMessage(localMessage, $('#addEntryModal'));
    } else {
        codeElement.parents("div.form-group").removeClass("has-error");
    }

    // verif if all mendatory fields are not empty
    if (nameElementEmpty || codeElementEmpty)
        return;

    showLoaderInModal('#addEntryModal');
    createEntry("CreateBuildRevisionInvariant", formAdd, "#buildrevdefinitionsTable");

}

function addEntryClick() {
    clearResponseMessageMainPage();
    // When creating a new Entry, System takes the default value of the 
    // system already selected in header.
    var formAdd = $('#addEntryModal');
    formAdd.find("#system").prop("value", getUser().defaultSystem);
    $('#addEntryModal').modal('show');
}

function editEntryModalSaveHandler() {
    clearResponseMessage($('#editEntryModal'));
    var formEdit = $('#editEntryModal #editEntryModalForm');

    showLoaderInModal('#editEntryModal');
    updateEntry("UpdateBuildRevisionInvariant", formEdit, "#buildrevdefinitionsTable");
}

function editEntryClick(system, level, seq) {
    clearResponseMessageMainPage();
    var param = "system=" + encodeURIComponent(system);
    param = param + "&level=" + encodeURIComponent(level)
    param = param + "&seq=" + encodeURIComponent(seq)
    var jqxhr = $.getJSON("ReadBuildRevisionInvariant", param);
    $.when(jqxhr).then(function (data) {
        var obj = data["contentTable"];

        var formEdit = $('#editEntryModal');

        formEdit.find("#system").prop("value", obj["system"]);
        formEdit.find("#level").prop("value", obj["level"]);
        formEdit.find("#seq").prop("value", obj["seq"]);
        formEdit.find("#versionname").prop("value", obj["versionName"]);

        if (!(data["hasPermissions"])) { // If readonly, we only readonly all fields
            formEdit.find("#system").prop("disabled", "disabled");
            formEdit.find("#level").prop("disabled", "disabled");
            formEdit.find("#seq").prop("readonly", "readonly");
            formEdit.find("#versionname").prop("readonly", "readonly");

            $('#editEntryButton').attr('class', '');
            $('#editEntryButton').attr('hidden', 'hidden');
        }

        formEdit.modal('show');
    });
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
        {"data": null,
            "title": doc.getDocLabel("page_global", "columnAction"),
            "bSortable": false,
            "bSearchable": false,
            "sWidth": "50px",
            "mRender": function (data, type, obj) {
                var hasPermissions = $("#" + tableId).attr("hasPermissions");

                var editEntry = '<button id="editEntry" onclick="editEntryClick(\'' + escapeHtml(obj["system"]) + '\',\'' + obj["level"] + '\',\'' + obj["seq"] + '\');"\n\
                                    class="editEntry btn btn-default btn-xs margin-right5" \n\
                                    name="editEntry" title="' + doc.getDocLabel("page_buildrevdefinition", "button_edit") + '" type="button">\n\
                                    <span class="glyphicon glyphicon-pencil"></span></button>';
                var viewEntry = '<button id="editEntry" onclick="editEntryClick(\'' + escapeHtml(obj["system"]) + '\',\'' + obj["level"] + '\',\'' + obj["seq"] + '\');"\n\
                                    class="editEntry btn btn-default btn-xs margin-right5" \n\
                                    name="editEntry" title="' + doc.getDocLabel("page_buildrevdefinition", "button_edit") + '" type="button">\n\
                                    <span class="glyphicon glyphicon-eye-open"></span></button>';
                var deleteEntry = '<button id="deleteEntry" onclick="deleteEntryClick(\'' + escapeHtml(obj["system"]) + '\',\'' + obj["level"] + '\',\'' + obj["seq"] + '\',\'' + obj["versionName"] + '\');" \n\
                                    class="deleteEntry btn btn-default btn-xs margin-right5" \n\
                                    name="deleteEntry" title="' + doc.getDocLabel("page_buildrevdefinition", "button_delete") + '" type="button">\n\
                                    <span class="glyphicon glyphicon-trash"></span></button>';
                if (hasPermissions === "true") { //only draws the options if the user has the correct privileges
                    return '<div class="center btn-group width150">' + editEntry + deleteEntry + '</div>';
                }
                return '<div class="center btn-group width150">' + viewEntry + '</div>';
            }
        },
        {
            "data": "system",
            "sName": "system",
            "sWidth": "50px",
            "title": doc.getDocOnline("buildrevisioninvariant", "system")},
        {
            "data": "level",
            "sName": "level",
            "sWidth": "30px",
            "title": doc.getDocOnline("buildrevisioninvariant", "level")},
        {
            "data": "seq",
            "sName": "seq",
            "sWidth": "40px",
            "title": doc.getDocOnline("buildrevisioninvariant", "seq")},
        {
            "data": "versionName",
            "sName": "versionName",
            "sWidth": "50px",
            "title": doc.getDocOnline("buildrevisioninvariant", "versionName")}
    ];
    return aoColumns;
}
