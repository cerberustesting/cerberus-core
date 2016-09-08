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
    $("#editSoapLibraryButton").click(editEntryModalSaveHandler);
    $("#addSoapLibraryButton").click(addEntryModalSaveHandler);

    //clear the modals fields when closed
    $('#editSoapLibraryModal').on('hidden.bs.modal', editEntryModalCloseHandler);
    $('#addSoapLibraryModal').on('hidden.bs.modal', addEntryModalCloseHandler);

    //configure and create the dataTable
    var configurations = new TableConfigurationsServerSide("soapLibrarysTable", "ReadSoapLibrary", "contentTable", aoColumnsFunc("soapLibrarysTable"), [1, 'asc']);
    createDataTableWithPermissions(configurations, renderOptionsForApplication, "#soapLibraryList");
}

function displayPageLabel() {
    var doc = new Doc();

    $("#title").html(doc.getDocLabel("page_soapLibrary", "allSoapLibrarys"));
    $("[name='addSoapLibraryField']").html(doc.getDocLabel("page_soapLibrary", "addSoapLibrary_field"));
    $("[name='editSoapLibraryField']").html(doc.getDocLabel("page_soapLibrary", "editSoapLibrary_field"));
    $("[name='soapLibraryField']").html(doc.getDocLabel("page_soapLibrary", "soapLibrary_field"));
    $("[name='idnameField']").html(doc.getDocLabel("page_soapLibrary", "idname_field"));
    $("[name='typeField']").html(doc.getDocLabel("page_soapLibrary", "type_field"));
    $("[name='envelopeField']").html(doc.getDocLabel("page_soapLibrary", "envelope_field"));
    $("[name='descriptionField']").html(doc.getDocLabel("page_soapLibrary", "description_field"));
    $("[name='servicePathField']").html(doc.getDocLabel("page_soapLibrary", "servicePath_field"));
    $("[name='methodField']").html(doc.getDocLabel("page_soapLibrary", "method_field"));
    $("[name='parsingAnswerField']").html(doc.getDocLabel("page_soapLibrary", "parsingAnswer_field"));
    $("[name='buttonClose']").html(doc.getDocLabel("page_soapLibrary", "close_btn"));
    $("[name='buttonAdd']").html(doc.getDocLabel("page_soapLibrary", "save_btn"));
    $("#soapLibraryListLabel").html("<span class='glyphicon glyphicon-list'></span> " + doc.getDocLabel("page_soapLibrary", "soapLibrary"));

    displayHeaderLabel(doc);

    displayFooter(doc);
    displayGlobalLabel(doc);
}

function renderOptionsForApplication(data) {
    var doc = new Doc();
    if (data["hasPermissions"]) {
        if ($("#createSoapLibraryButton").length === 0) {
            var contentToAdd = "<div class='marginBottom10'><button id='createSoapLibraryButton' type='button' class='btn btn-default'>\n\
            <span class='glyphicon glyphicon-plus-sign'></span> " + doc.getDocLabel("page_soapLibrary", "button_create") + "</button></div>";
            $("#soapLibrarysTable_wrapper div#soapLibrarysTable_length").before(contentToAdd);
            $('#soapLibraryList #createSoapLibraryButton').click(addEntryClick);
        }
    } else {
        if ($("#blankSpace").length === 0) {
            var contentToAdd = "<div class='marginBottom10' style='height:34px;' id='blankSpace'></div>";
            $("#soapLibrarysTable_wrapper div#soapLibrarysTable_length").before(contentToAdd);
        }
    }
}

function editEntryClick(name) {
    var formEdit = $('#editSoapLibraryModal');

    $.ajax({
        url: "ReadSoapLibrary?name=" + name,
        async: true,
        method: "GET",
        success: function (data) {
            if (data.messageType === "OK") {
                console.log(data);
                formEdit.find("#name").prop("value", data.name);
                formEdit.find("#type").prop("value", data.type);
                formEdit.find("#servicepath").prop("value", data.servicePath);
                formEdit.find("#method").prop("value", data.method);
                formEdit.find("#envelope").prop("value", $('<div/>').html(data.envelope).text());
                formEdit.find("#parsinganswer").prop("value", data.parsingAnswer);
                formEdit.find("#description").prop("value", data.description);
                if (!(data["hasPermissions"])) { // If readonly, we only readonly all fields
                    formEdit.find("#name").prop("readonly", "readonly");
                    formEdit.find("#type").prop("readonly", "readonly");
                    formEdit.find("#servicepath").prop("readonly", "readonly");
                    formEdit.find("#method").prop("readonly", "readonly");
                    formEdit.find("#envelope").prop("readonly", "readonly");
                    formEdit.find("#parsinganswer").prop("readonly", "readonly");
                    formEdit.find("#description").prop("readonly", "readonly");

                    $('#editSoapLibraryButton').attr('class', '');
                    $('#editSoapLibraryButton').attr('hidden', 'hidden');
                }
                formEdit.modal('show');
            } else {
                showUnexpectedError();
            }
        },
        error: showUnexpectedError
    });
}

function addEntryClick() {
    clearResponseMessageMainPage();
    $("#addSoapLibraryModal #idname").empty();

    $('#addSoapLibraryModal').modal('show');
}

function removeEntryClick(name) {
    var doc = new Doc();
    showModalConfirmation(function (ev) {
        var name = $('#confirmationModal #hiddenField1').prop("value");
        $.ajax({
            url: "DeleteSoapLibrary2?name=" + name,
            async: true,
            method: "GET",
            success: function (data) {
                hideLoaderInModal('#removeSoapLibraryModal');
                var oTable = $("#soapLibrarysTable").dataTable();
                oTable.fnDraw(true);
                $('#removeSoapLibraryModal').modal('hide');
                showMessage(data);
            },
            error: showUnexpectedError
        });

        $('#confirmationModal').modal('hide');
    }, doc.getDocLabel("page_soapLibrary", "title_remove"), doc.getDocLabel("page_soapLibrary", "message_remove"), name, undefined, undefined, undefined);
}

function editEntryModalSaveHandler() {
    clearResponseMessage($('#editSoapLibraryModal'));
    var formEdit = $('#editSoapLibraryModal #editSoapLibraryModalForm');

    var sa = formEdit.serializeArray();
    var data = {}
    for (var i in sa) {
        data[sa[i].name] = sa[i].value;
    }
    // Get the header data from the form.
    //var data = convertSerialToJSONObject(formEdit.serialize());
    showLoaderInModal('#editSoapLibraryModal');
    $.ajax({
        url: "UpdateSoapLibrary2",
        async: true,
        method: "POST",
        data: {
            name: data.name,
            type: data.type,
            ServicePath: data.servicepath,
            Method: data.method,
            ParsingAnswer: data.parsinganswer,
            Description: data.description,
            Envelope: data.envelope
        },
        success: function (data) {
            hideLoaderInModal('#editSoapLibraryModal');
            var oTable = $("#soapLibrarysTable").dataTable();
            oTable.fnDraw(true);
            $('#editSoapLibraryModal').modal('hide');
            showMessage(data);
        },
        error: showUnexpectedError
    });

}

function addEntryModalSaveHandler() {
    clearResponseMessage($('#addSoapLibraryModal'));
    var formEdit = $('#addSoapLibraryModal #addSoapLibraryModalForm');

    var sa = formEdit.serializeArray();
    var data = {}
    for (var i in sa) {
        data[sa[i].name] = sa[i].value;
    }
    // Get the header data from the form.
    //var data = convertSerialToJSONObject(formEdit.serialize());
    showLoaderInModal('#addSoapLibraryModal');
    $.ajax({
        url: "CreateSoapLibrary2",
        async: true,
        method: "POST",
        data: {
            name: data.name,
            type: data.type,
            ServicePath: data.servicepath,
            Method: data.method,
            ParsingAnswer: data.parsinganswer,
            Description: data.description,
            Envelope: data.envelope
        },
        success: function (data) {
            hideLoaderInModal('#addSoapLibraryModal');
            var oTable = $("#soapLibrarysTable").dataTable();
            oTable.fnDraw(true);
            $('#addSoapLibraryModal').modal('hide');
            showMessage(data);
        },
        error: showUnexpectedError
    });

}

function editEntryModalCloseHandler() {
    // reset form values
    $('#editSoapLibraryModal #editSoapLibraryModalForm')[0].reset();
    // remove all errors on the form fields
    $(this).find('div.has-error').removeClass("has-error");
    // clear the response messages of the modal
    clearResponseMessage($('#editSoapLibraryModal'));
}
function addEntryModalCloseHandler() {
    // reset form values
    $('#addSoapLibraryModal #addSoapLibraryModalForm')[0].reset();
    // remove all errors on the form fields
    $(this).find('div.has-error').removeClass("has-error");
    // clear the response messages of the modal
    clearResponseMessage($('#addSoapLibraryModal'));
}

function getSys() {
    var sel = document.getElementById("MySystem");
    var selectedIndex = sel.selectedIndex;
    return sel.options[selectedIndex].value;
}

function aoColumnsFunc(tableId) {
    var doc = new Doc();
    var aoColumns = [
        {
            "data": null,
            "bSortable": false,
            "bSearchable": false,
            "title": doc.getDocLabel("page_soapLibrary", "button_col"),
            "mRender": function (data, type, obj) {
                var hasPermissions = $("#" + tableId).attr("hasPermissions");

                var editSoapLibrary = '<button id="editSoapLibrary" onclick="editEntryClick(\'' + obj["name"] + '\');"\n\
                                        class="editApplication btn btn-default btn-xs margin-right5" \n\
                                        name="editSoapLibrary" title="' + doc.getDocLabel("page_soapLibrary", "button_edit") + '" type="button">\n\
                                        <span class="glyphicon glyphicon-pencil"></span></button>';

                var removeSoapLibrary = '<button id="removeSoapLibrary" onclick="removeEntryClick(\'' + obj["name"] + '\');"\n\
                                        class="removeSoapLibrary btn btn-default btn-xs margin-right5" \n\
                                        name="removeSoapLibrary" title="' + doc.getDocLabel("page_soapLibrary", "button_remove") + '" type="button">\n\
                                        <span class="glyphicon glyphicon-remove"></span></button>';
                var viewSoapLibrary = '<button id="editSoapLibrary" onclick="editEntryClick(\'' + obj["name"] + '\');"\n\
                                    class="editApplication btn btn-default btn-xs margin-right5" \n\
                                    name="viewSoapLibrary" title="' + doc.getDocLabel("page_application", "button_edit") + '" type="button">\n\
                                    <span class="glyphicon glyphicon-eye-open"></span></button>';
                if (hasPermissions === "true") { //only draws the options if the user has the correct privileges
                    return '<div class="center btn-group width150">' + editSoapLibrary + removeSoapLibrary + '</div>';
                }
                return '<div class="center btn-group width150">' + viewSoapLibrary + '</div>';

            },
            "width": "50px"
        },
        {"data": "name", "sName": "Name", "title": doc.getDocLabel("page_soapLibrary", "soapLibrary_col")},
        {"data": "type", "sName": "Type", "title": doc.getDocLabel("page_soapLibrary", "type_col")},
        {"data": "envelope", "sName": "Envelope", "title": doc.getDocLabel("page_soapLibrary", "envelope_col")},
        {
            "data": "description",
            "sName": "Description",
            "title": doc.getDocLabel("page_soapLibrary", "description_col")
        },
        {
            "data": "servicePath",
            "sName": "ServicePath",
            "title": doc.getDocLabel("page_soapLibrary", "servicepath_col")
        },
        {"data": "method", "sName": "Method", "title": doc.getDocLabel("page_soapLibrary", "method_col")},
        {
            "data": "parsingAnswer",
            "sName": "ParsingAnswer",
            "title": doc.getDocLabel("page_soapLibrary", "parsinganswer_col")
        }
    ];
    return aoColumns;
}
