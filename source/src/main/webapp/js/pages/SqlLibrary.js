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
    $("#editSqlLibraryButton").click(editEntryModalSaveHandler);
    $("#addSqlLibraryButton").click(addEntryModalSaveHandler);

    //clear the modals fields when closed
    $('#editSqlLibraryModal').on('hidden.bs.modal', editEntryModalCloseHandler);
    $('#addSqlLibraryModal').on('hidden.bs.modal', addEntryModalCloseHandler);

    // Invariant Combo loading.
    displayInvariantList("database", "PROPERTYDATABASE", false, "", "");

    //configure and create the dataTable
    var configurations = new TableConfigurationsServerSide("sqlLibrarysTable", "ReadSqlLibrary", "contentTable", aoColumnsFunc("sqlLibrarysTable"), [1, 'asc']);
    createDataTableWithPermissions(configurations, renderOptionsForSqlLibrary, "#sqlLibraryList", undefined, true);
}

/**
 * After table feeds, 
 * @returns {undefined}
 */
function afterTableLoad() {
    $.each($("pre[name='scriptField']"), function (i, e) {
        //Highlight envelop on modal loading
        var editor = ace.edit($(e).get(0));
        editor.setTheme("ace/theme/chrome");
        editor.getSession().setMode("ace/mode/sql");
        editor.setOptions({
            maxLines: 1,
            showLineNumbers: false,
            showGutter: false,
            highlightActiveLine: false,
            highlightGutterLine: false,
            readOnly: true
        });
        editor.renderer.$cursorLayer.element.style.opacity = 0;
    });
}

function displayPageLabel() {
    var doc = new Doc();

    $("#title").html(doc.getDocLabel("page_sqlLibrary", "allSqlLibrarys"));
    $("#pageTitle").html(doc.getDocLabel("page_sqlLibrary", "allSqlLibrarys"));
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
            var contentToAdd = "<div class='marginBottom10' id='blankSpace'></div>";
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

                //Destroy the previous Ace object.
                ace.edit($("#editSqlLibraryModalForm #script")[0]).destroy();

                formEdit.find("#name").prop("value", data.name);
                formEdit.find("#type").prop("value", $('<div/>').html(data.type).text());
                formEdit.find("#script").text(data.script);
                formEdit.find("#description").prop("value", $('<div/>').html(data.description).text());
                formEdit.find("#database").val(data.database);

                //Highlight envelop on modal loading
                var editor = ace.edit($("#editSqlLibraryModalForm #script")[0]);
                editor.setTheme("ace/theme/chrome");
                editor.getSession().setMode("ace/mode/sql");
                editor.setOptions({
                    maxLines: Infinity
                });

                if (!(data["hasPermissions"])) { // If readonly, we only readonly all fields
                    formEdit.find("#name").prop("readonly", "readonly");
                    formEdit.find("#type").prop("readonly", "readonly");
                    formEdit.find("#script").prop("readonly", "readonly");
                    formEdit.find("#description").prop("readonly", "readonly");
                    formEdit.find("#database").prop("disabled", "disabled");

                    $('#editSqlLibraryButton').attr('class', '');
                    $('#editSqlLibraryButton').attr('hidden', 'hidden');
                }

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
    //Add script, not in the form
    var editor = ace.edit($("#editSqlLibraryModalForm #script")[0]);
    data.script = encodeURIComponent(editor.getSession().getDocument().getValue());

    showLoaderInModal('#editSqlLibraryModal');
    $.ajax({
        url: "UpdateSqlLibrary",
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
                oTable.fnDraw(false);
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

    //Highlight envelop on modal loading
    var editor = ace.edit($("#addSqlLibraryModalForm #script")[0]);
    editor.setTheme("ace/theme/chrome");
    editor.getSession().setMode("ace/mode/sql");
    editor.setOptions({
        maxLines: Infinity
    });


    $('#addSqlLibraryModal').modal('show');
}

function addEntryModalSaveHandler() {
    clearResponseMessage($('#addSqlLibraryModal'));
    var formEdit = $('#addSqlLibraryModal #addSqlLibraryModalForm');

    // Get the header data from the form.
    var data = convertSerialToJSONObject(formEdit.serialize());
    //Add script, not in the form
    var editor = ace.edit($("#addSqlLibraryModalForm #script")[0]);
    data.script = encodeURIComponent(editor.getSession().getDocument().getValue());

    showLoaderInModal('#addSqlLibraryModal');
    $.ajax({
        url: "CreateSqlLibrary",
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
                oTable.fnDraw(false);
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

function removeEntryClickHandler() {
    var name = $('#confirmationModal #hiddenField1').prop("value");
    var jqxhr = $.post("DeleteSqlLibrary", {name: name}, "json");
    $.when(jqxhr).then(function (data) {
        var messageType = getAlertType(data.messageType);
        if (messageType === "success") {
            //redraw the datatable
            var oTable = $("#sqlLibrarysTable").dataTable();
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

function removeEntryClick(name) {
    clearResponseMessageMainPage();
    var doc = new Doc();
    var messageComplete = doc.getDocLabel("page_sqlLibrary", "message_remove");
    messageComplete = messageComplete.replace("%ENTRY%", name);
    showModalConfirmation(removeEntryClickHandler, undefined, doc.getDocLabel("page_sqlLibrary", "title_remove"), messageComplete, name, "", "", "");
}

function aoColumnsFunc(tableId) {
    var doc = new Doc();
    var aoColumns = [
        {
            "data": null,
            "bSortable": false,
            "bSearchable": false,
            "sWidth": "50px",
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

            }
        },
        {
            "data": "name",
            "sName": "Name",
            "sWidth": "50px",
            "title": doc.getDocLabel("page_sqlLibrary", "sqlLibrary_col")
        },
        {
            "data": "type",
            "sName": "Type",
            "sWidth": "50px",
            "title": doc.getDocLabel("page_sqlLibrary", "type_col")},
        {
            "data": "database",
            "sName": "Database",
            "sWidth": "50px",
            "title": doc.getDocLabel("page_sqlLibrary", "database_col")},
        {
            "data": "script",
            "like":true,
            "sName": "Script",
            "sWidth": "150px",
            "title": doc.getDocLabel("page_sqlLibrary", "script_col"),
            "mRender": function (data, type, obj) {
                return $("<div></div>").append($("<pre name='scriptField' style='height:20px; overflow:hidden; text-overflow:clip; border: 0px; padding:0; margin:0'></pre>").text(obj['script'])).html();
            }
        },
        {
            "data": "description",
            "like":true,
            "sName": "Description",
            "sWidth": "100px",
            "title": doc.getDocLabel("page_sqlLibrary", "description_col")
        }
    ];
    return aoColumns;
}
