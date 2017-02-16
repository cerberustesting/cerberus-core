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
    $("#editSqlLibraryButton").click(editEntryModalSaveHandler);
    $("#addSqlLibraryButton").click(addEntryModalSaveHandler);

    //clear the modals fields when closed
    $('#editSqlLibraryModal').on('hidden.bs.modal', editEntryModalCloseHandler);
    $('#addSqlLibraryModal').on('hidden.bs.modal', addEntryModalCloseHandler);

    //configure and create the dataTable
    var configurations = new TableConfigurationsServerSide("sqlLibrarysTable", "ReadSqlLibrary", "contentTable", aoColumnsFunc("sqlLibrarysTable"), [1, 'asc']);
    createDataTableWithPermissions(configurations, renderOptionsForSqlLibrary, "#sqlLibraryList", undefined, true);
}

/**
 * After table feeds, 
 * @returns {undefined}
 */
function afterTableLoad() {
    $.each($("code[name='scriptField']"), function (i, e) {
        Prism.highlightElement($(e).get(0));
    });
}

function displayPageLabel() {
    var doc = new Doc();

    $("#title").html(doc.getDocLabel("page_sqlLibrary", "allSqlLibrarys"));
    $("[name='addSqlLibraryField']").html(doc.getDocLabel("page_sqlLibrary", "addSqlLibrary_field"));
    $("[name='editSqlLibraryField']").html(doc.getDocLabel("page_sqlLibrary", "editSqlLibrary_field"));
    $("[name='sqlLibraryField']").html(doc.getDocLabel("page_sqlLibrary", "sqlLibrary_field"));
    $("[name='idnameField']").html(doc.getDocLabel("page_sqlLibrary", "idname_field"));
    $("[name='typeField']").html(doc.getDocLabel("page_sqlLibrary", "type_field"));
    $("[name='databaseField']").html(doc.getDocLabel("page_sqlLibrary", "database_field"));
    $("[name='scriptField']").html(doc.getDocLabel("page_sqlLibrary", "script_field"));
    $("[name='descriptionField']").html(doc.getDocLabel("page_sqlLibrary", "description_field"));
    $("[name='buttonClose']").html(doc.getDocLabel("page_sqlLibrary", "close_btn"));
    $("[name='buttonAdd']").html(doc.getDocLabel("page_sqlLibrary", "save_btn"));
    $("#sqlLibraryListLabel").html("<span class='glyphicon glyphicon-list'></span> " + doc.getDocLabel("page_sqlLibrary", "sqlLibrary"));

    displayHeaderLabel(doc);

    displayFooter(doc);
    displayGlobalLabel(doc);
}

function renderOptionsForSqlLibrary(data) {
    var doc = new Doc();
    if (data["hasPermissions"]) {
        if ($("#createSqlLibraryButton").length === 0) {
            var contentToAdd = "<div class='marginBottom10'><button id='createSqlLibraryButton' type='button' class='btn btn-default'>\n\
            <span class='glyphicon glyphicon-plus-sign'></span> " + doc.getDocLabel("page_sqlLibrary", "button_create") + "</button></div>";
            $("#sqlLibrarysTable_wrapper div#sqlLibrarysTable_length").before(contentToAdd);
            $('#sqlLibraryList #createSqlLibraryButton').click(addEntryClick);
        }
    } else {
        if ($("#blankSpace").length === 0) {
            var contentToAdd = "<div class='marginBottom10' style='height:34px;' id='blankSpace'></div>";
            $("#sqlLibrarysTable_wrapper div#sqlLibrarysTable_length").before(contentToAdd);
        }
    }
}

function editEntryClick(name) {
    var formEdit = $('#editSqlLibraryModal');

    $.ajax({
        url: "ReadSqlLibrary?name=" + name,
        async: true,
        method: "GET",
        success: function (data) {
            if (data.messageType === "OK") {
                formEdit.find("#name").prop("value", data.name);
                formEdit.find("#type").prop("value", $('<div/>').html(data.type).text());
                formEdit.find("#script").text(data.script);
                formEdit.find("#description").prop("value", $('<div/>').html(data.description).text());
                formEdit.find("#database").find("option[value='" + data.database + "']").attr("selected", "selected");
                if (!(data["hasPermissions"])) { // If readonly, we only readonly all fields
                    formEdit.find("#name").prop("readonly", "readonly");
                    formEdit.find("#type").prop("readonly", "readonly");
                    formEdit.find("#script").prop("readonly", "readonly");
                    formEdit.find("#description").prop("readonly", "readonly");
                    formEdit.find("#database").prop("disabled", "disabled");

                    $('#editSqlLibraryButton').attr('class', '');
                    $('#editSqlLibraryButton').attr('hidden', 'hidden');
                }

                //Highlight envelop on modal loading
                Prism.highlightElement($("#script")[0]);

                /**
                 * On edition, get the caret position, refresh the envelope to have 
                 * syntax coloration in real time, then set the caret position.
                 */
                $('#editSqlLibraryModal #script').on("keyup", function (e) {
                    //Get the position of the carret
                    var pos = $(this).caret('pos');

                    //On Firefox only, when pressing enter, it create a <br> tag.
                    //So, if the <br> tag is present, replace it with <span>&#13;</span>
                    if ($("#editSqlLibraryModal #script br").length !== 0) {
                        $("#editSqlLibraryModal #script br").replaceWith("<span>&#13;</span>");
                        pos++;
                    }
                    //Apply syntax coloration
                    Prism.highlightElement($("#editSqlLibraryModal #script")[0]);
                    //Set the caret position to the initia one.
                    $(this).caret('pos', pos);
                });

                //On click on <pre> tag, focus on <code> tag to make the modification into this element,
                //Add class on container to highlight field
                $('#editSqlLibraryModal #scriptContainer').on("click", function (e) {
                    $('#editSqlLibraryModal #scriptContainer').addClass('highlightedContainer');
                    $('#editSqlLibraryModal #script').focus();
                });

                //Remove class to stop highlight envelop field
                $('#editSqlLibraryModal #script').on('blur', function () {
                    $('#editSqlLibraryModal #scriptContainer').removeClass('highlightedContainer');
                });

                formEdit.modal('show');
            } else {
                showUnexpectedError();
            }
        },
        error: showUnexpectedError
    });
}

function editEntryModalSaveHandler() {
    clearResponseMessage($('#editSqlLibraryModal'));
    var formEdit = $('#editSqlLibraryModal #editSqlLibraryModalForm');

    // Get the header data from the form.
    var data = convertSerialToJSONObject(formEdit.serialize());
    //Add envelope and script, not in the form
    data.script = encodeURI($("#editSqlLibraryModalForm #script").text());

    showLoaderInModal('#editSqlLibraryModal');
    $.ajax({
        url: "UpdateSqlLibrary2",
        async: true,
        method: "POST",
        data: {
            name: data.name,
            type: data.type,
            database: data.database,
            script: data.script,
            description: data.description
        },
        success: function (data) {
            data = JSON.parse(data);
            hideLoaderInModal('#editSqlLibraryModal');
            if (getAlertType(data.messageType) === 'success') {
                var oTable = $("#sqlLibrarysTable").dataTable();
                oTable.fnDraw(true);
                $('#editSqlLibraryModal').modal('hide');
                showMessage(data);
            } else {
                showMessage(data, $('#editSqlLibraryModal'));
            }
        },
        error: showUnexpectedError
    });

}

function editEntryModalCloseHandler() {
    // reset form values
    $('#editSqlLibraryModal #editSqlLibraryModalForm')[0].reset();
    $('#editSqlLibraryModal #script').empty();
    // remove all errors on the form fields
    $(this).find('div.has-error').removeClass("has-error");
    // clear the response messages of the modal
    clearResponseMessage($('#editSqlLibraryModal'));
}

function addEntryClick() {
    clearResponseMessageMainPage();
    /**
     * Clear previous form
     */
    $("#addSqlLibraryModal #idname").empty();
    $('#addSqlLibraryModal #envelope').empty();

    /**
     * On edition, get the caret position, refresh the envelope to have 
     * syntax coloration in real time, then set the caret position.
     */

    $('#addSqlLibraryModal #script').on("keyup", function (e) {
        //Get the position of the carret
        var pos = $(this).caret('pos');

        //On Firefox only, when pressing enter, it create a <br> tag.
        //So, if the <br> tag is present, replace it with <span>&#13;</span>
        if ($("#addSqlLibraryModal #script br").length !== 0) {
            $("#addSqlLibraryModal #script br").replaceWith("<span>&#13;</span>");
            pos++;
        }
        //Apply syntax coloration
        Prism.highlightElement($("#addSqlLibraryModal #script")[0]);
        //Set the caret position to the initia one.
        $(this).caret('pos', pos);
    });

    //On click on <pre> tag, focus on <code> tag to make the modification into this element,
    //Add class on container to highlight field
    $('#addSqlLibraryModal #scriptContainer').on("click", function (e) {
        $('#addSqlLibraryModal #scriptContainer').addClass('highlightedContainer');
        $('#addSqlLibraryModal #script').focus();
    });

    //Remove class to stop highlight envelop field
    $('#addSqlLibraryModal #script').on('blur', function () {
        $('#addSqlLibraryModal #scriptContainer').removeClass('highlightedContainer');
    });


    $('#addSqlLibraryModal').modal('show');
}

function addEntryModalSaveHandler() {
    clearResponseMessage($('#addSqlLibraryModal'));
    var formEdit = $('#addSqlLibraryModal #addSqlLibraryModalForm');

    // Get the header data from the form.
    var data = convertSerialToJSONObject(formEdit.serialize());
    //Add envelope and script, not in the form
    data.script = encodeURI($("#addSqlLibraryModalForm #script").text());

    showLoaderInModal('#addSqlLibraryModal');
    $.ajax({
        url: "CreateSqlLibrary2",
        async: true,
        method: "POST",
        data: {
            name: data.name,
            type: data.type,
            script: data.script,
            database: data.database,
            description: data.description
        },
        success: function (data) {
            data = JSON.parse(data);
            hideLoaderInModal('#addSqlLibraryModal');
            if (getAlertType(data.messageType) === 'success') {
                var oTable = $("#sqlLibrarysTable").dataTable();
                oTable.fnDraw(true);
                $('#addSqlLibraryModal').modal('hide');
                showMessage(data);
            } else {
                showMessage(data, $('#addSqlLibraryModal'));
            }
        },
        error: showUnexpectedError
    });

}

function addEntryModalCloseHandler() {
    // reset form values
    $('#addSqlLibraryModal #addSqlLibraryModalForm')[0].reset();
    // remove all errors on the form fields
    $(this).find('div.has-error').removeClass("has-error");
    // clear the response messages of the modal
    clearResponseMessage($('#addSqlLibraryModal'));
}

function removeEntryClick(name) {
    var doc = new Doc();
    showModalConfirmation(function (ev) {
        var name = $('#confirmationModal #hiddenField1').prop("value");
        $.ajax({
            url: "DeleteSqlLibrary2?name=" + name,
            async: true,
            method: "GET",
            success: function (data) {
                hideLoaderInModal('#removeSqlLibraryModal');
                var oTable = $("#sqlLibrarysTable").dataTable();
                oTable.fnDraw(true);
                $('#removeSqlLibraryModal').modal('hide');
                showMessage(data);
            },
            error: showUnexpectedError
        });

        $('#confirmationModal').modal('hide');
    }, doc.getDocLabel("page_sqlLibrary", "title_remove"), doc.getDocLabel("page_sqlLibrary", "message_remove"), name, undefined, undefined, undefined);
}

function aoColumnsFunc(tableId) {
    var doc = new Doc();
    var aoColumns = [
        {
            "data": null,
            "bSortable": false,
            "bSearchable": false,
            "title": doc.getDocLabel("page_sqlLibrary", "button_col"),
            "mRender": function (data, type, obj) {
                var hasPermissions = $("#" + tableId).attr("hasPermissions");

                var editSqlLibrary = '<button id="editSqlLibrary" onclick="editEntryClick(\'' + obj["name"] + '\');"\n\
                                        class="editSqlLibrary btn btn-default btn-xs margin-right5" \n\
                                        name="editSqlLibrary" title="' + doc.getDocLabel("page_sqlLibrary", "button_edit") + '" type="button">\n\
                                        <span class="glyphicon glyphicon-pencil"></span></button>';

                var removeSqlLibrary = '<button id="removeSqlLibrary" onclick="removeEntryClick(\'' + obj["name"] + '\');"\n\
                                        class="removeSqlLibrary btn btn-default btn-xs margin-right5" \n\
                                        name="removeSqlLibrary" title="' + doc.getDocLabel("page_sqlLibrary", "button_remove") + '" type="button">\n\
                                        <span class="glyphicon glyphicon-trash"></span></button>';
                var viewSqlLibrary = '<button id="editSqlLibrary" onclick="editEntryClick(\'' + obj["name"] + '\');"\n\
                                    class="editSqlLibrary btn btn-default btn-xs margin-right5" \n\
                                    name="viewSqlLibrary" title="' + doc.getDocLabel("page_application", "button_edit") + '" type="button">\n\
                                    <span class="glyphicon glyphicon-eye-open"></span></button>';
                if (hasPermissions === "true") { //only draws the options if the user has the correct privileges
                    return '<div class="center btn-group width150">' + editSqlLibrary + removeSqlLibrary + '</div>';
                }
                return '<div class="center btn-group width150">' + viewSqlLibrary + '</div>';

            },
            "width": "50px"
        },
        {"data": "name", "sName": "Name", "title": doc.getDocLabel("page_sqlLibrary", "sqlLibrary_col")},
        {"data": "type", "sName": "Type", "title": doc.getDocLabel("page_sqlLibrary", "type_col")},
        {"data": "database", "sName": "Database", "title": doc.getDocLabel("page_sqlLibrary", "database_col")},
        {"data": "script", "sName": "Script", "sWidth": "450px", "title": doc.getDocLabel("page_sqlLibrary", "script_col"),
            "mRender": function (data, type, obj) {
                return $("<div></div>").append($("<pre style='height:20px; overflow:hidden; text-overflow:clip; border: 0px; padding:0; margin:0'></pre>").append($("<code name='scriptField' class='language-sql'></code>").text(obj['script']))).html();
            }},
        {"data": "description", "sName": "Description", "title": doc.getDocLabel("page_sqlLibrary", "description_col")}
    ];
    return aoColumns;
}
