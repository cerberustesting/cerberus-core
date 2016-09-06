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
    var configurations = new TableConfigurationsServerSide("sqlLibrarysTable", "ReadSqlLibrary", "contentTable", aoColumnsFunc(), [1, 'asc']);
    createDataTableWithPermissions(configurations, renderOptionsForApplication, "#sqlLibraryList");
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
    $("#sqlLibraryListLabel").html("<span class='glyphicon glyphicon-list'></span> "+doc.getDocLabel("page_sqlLibrary", "sqlLibrary"));

    displayHeaderLabel(doc);

    displayFooter(doc);
    displayGlobalLabel(doc);
}

function renderOptionsForApplication(data) {
    var doc = new Doc();
    if ($("#createSqlLibraryButton").length === 0) {
        var contentToAdd = "<div class='marginBottom10'><button id='createSqlLibraryButton' type='button' class='btn btn-default'>\n\
            <span class='glyphicon glyphicon-plus-sign'></span> " + doc.getDocLabel("page_sqlLibrary", "button_create") + "</button></div>";
        $("#sqlLibrarysTable_wrapper div#sqlLibrarysTable_length").before(contentToAdd);
        $('#sqlLibraryList #createSqlLibraryButton').click(addEntryClick);
    }
}

function editEntryClick(name) {
    var formEdit = $('#editSqlLibraryModal');

    $.ajax({
        url: "ReadSqlLibrary?name="+name,
        async: true,
        method: "GET",
        success: function (data) {
            if(data.messageType === "OK") {
                formEdit.find("#name").prop("value", data.name);
                formEdit.find("#type").prop("value", data.type);
                formEdit.find("#script").prop("value", data.script);
                formEdit.find("#description").prop("value",data.description);
                formEdit.find("#database").find("option[value='"+data.database+"']").attr("selected","selected");
                formEdit.modal('show');
            }else{
                showUnexpectedError();
            }
        },
        error: showUnexpectedError
    });
}

function addEntryClick() {
    clearResponseMessageMainPage();
    $("#addSqlLibraryModal #idname").empty();

    $('#addSqlLibraryModal').modal('show');
}

function removeEntryClick(name) {
    var doc = new Doc();
    showModalConfirmation(function(ev){
        var name = $('#confirmationModal #hiddenField1').prop("value");
        $.ajax({
            url: "DeleteSqlLibrary2?name="+name,
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
    }, doc.getDocLabel("page_sqlLibrary", "title_remove") , doc.getDocLabel("page_sqlLibrary", "message_remove"), name, undefined, undefined, undefined);
}

function editEntryModalSaveHandler() {
    clearResponseMessage($('#editSqlLibraryModal'));
    var formEdit = $('#editSqlLibraryModal #editSqlLibraryModalForm');

    var sa = formEdit.serializeArray();
    var data = {}
    for(var i in sa){
        data[sa[i].name] = sa[i].value;
    }
    // Get the header data from the form.
    //var data = convertSerialToJSONObject(formEdit.serialize());
    showLoaderInModal('#editSqlLibraryModal');
    $.ajax({
        url: "UpdateSqlLibrary2",
        async: true,
        method: "POST",
        data: {name: data.name,
            type: data.type,
            database: data.database,
            script: data.script,
            description: data.description},
        success: function (data) {
            hideLoaderInModal('#editSqlLibraryModal');
            var oTable = $("#sqlLibrarysTable").dataTable();
            oTable.fnDraw(true);
            $('#editSqlLibraryModal').modal('hide');
            showMessage(data);
        },
        error: showUnexpectedError
    });

}

function addEntryModalSaveHandler() {
    clearResponseMessage($('#addSqlLibraryModal'));
    var formEdit = $('#addSqlLibraryModal #addSqlLibraryModalForm');

    var sa = formEdit.serializeArray();
    var data = {}
    for (var i in sa) {
        data[sa[i].name] = sa[i].value;
    }
    // Get the header data from the form.
    //var data = convertSerialToJSONObject(formEdit.serialize());
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
            hideLoaderInModal('#addSqlLibraryModal');
            var oTable = $("#sqlLibrarysTable").dataTable();
            oTable.fnDraw(true);
            $('#addSqlLibraryModal').modal('hide');
            showMessage(data);
        },
        error: showUnexpectedError
    });

}

function editEntryModalCloseHandler() {
    // reset form values
    $('#editSqlLibraryModal #editSqlLibraryModalForm')[0].reset();
    // remove all errors on the form fields
    $(this).find('div.has-error').removeClass("has-error");
    // clear the response messages of the modal
    clearResponseMessage($('#editSqlLibraryModal'));
}
function addEntryModalCloseHandler() {
    // reset form values
    $('#addSqlLibraryModal #addSqlLibraryModalForm')[0].reset();
    // remove all errors on the form fields
    $(this).find('div.has-error').removeClass("has-error");
    // clear the response messages of the modal
    clearResponseMessage($('#addSqlLibraryModal'));
}

function getSys() {
    var sel = document.getElementById("MySystem");
    var selectedIndex = sel.selectedIndex;
    return sel.options[selectedIndex].value;
}

function aoColumnsFunc(tableId) {
    var doc = new Doc();
    var aoColumns = [
        {"data": null,
            "bSortable": false,
            "bSearchable": false,
            "title": doc.getDocLabel("page_sqlLibrary", "button_col"),
            "mRender": function (data, type, obj) {
                var hasPermissions = $("#" + tableId).attr("hasPermissions");

                var editSqlLibrary = '<button id="editSqlLibrary" onclick="editEntryClick(\'' + obj["name"] + '\');"\n\
                                        class="editApplication btn btn-default btn-xs margin-right5" \n\
                                        name="editSqlLibrary" title="' + doc.getDocLabel("page_sqlLibrary", "button_edit") + '" type="button">\n\
                                        <span class="glyphicon glyphicon-pencil"></span></button>';

                var removeSqlLibrary = '<button id="removeSqlLibrary" onclick="removeEntryClick(\'' + obj["name"] + '\');"\n\
                                        class="removeSqlLibrary btn btn-default btn-xs margin-right5" \n\
                                        name="removeSqlLibrary" title="' + doc.getDocLabel("page_sqlLibrary", "button_remove") + '" type="button">\n\
                                        <span class="glyphicon glyphicon-remove"></span></button>';

                return '<div class="center btn-group width150">' + editSqlLibrary + removeSqlLibrary + '</div>';

            },
            "width": "50px"
        },
        {"data": "name", "sName": "Name", "title": doc.getDocLabel("page_sqlLibrary", "sqlLibrary_col")},
        {"data": "type", "sName": "Type", "title": doc.getDocLabel("page_sqlLibrary", "type_col")},
        {"data": "database", "sName": "Database", "title": doc.getDocLabel("page_sqlLibrary", "database_col")},
        {"data": "script", "sName": "Script", "title": doc.getDocLabel("page_sqlLibrary", "script_col")},
        {"data": "description", "sName": "Description", "title": doc.getDocLabel("page_sqlLibrary", "description_col")}
    ];
    return aoColumns;
}
