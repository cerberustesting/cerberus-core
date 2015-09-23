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

        $("#editEntryButton").click(saveUpdateEntryHandler);
        $("#addEntryButton").click(saveNewEntryHandler);

        $('#editEntryModal').on('hidden.bs.modal', {extra: "#editEntryModal"}, modalFormCleaner);
        $('#addEntryModal').on('hidden.bs.modal', {extra: "#addEntryModal"}, modalFormCleaner);

        var config = new TableConfigurationsServerSide("testTable", "ReadTest", "contentTable", aoColumnsFunc());

        var table = createDataTableWithPermissions(config, renderOptionsForTest);
        table.fnSort([1, 'asc']);

    });
});

function initPage() {
    var doc = new Doc();

    displayHeaderLabel(doc);
    displayGlobalLabel(doc);
//    displayPageLabel(doc);
    displayFooter(doc);
    displayInvariantList("TESTACTIVE", "Active");
    displayInvariantList("TESTAUTOMATED", "Automated");

}

function saveNewEntryHandler() {
    clearResponseMessage($('#addEntryModal'));
    var formAdd = $("#addEntryModal #addEntryModalForm");

    var nameElement = formAdd.find("#test");
    var nameElementEmpty = nameElement.prop("value") === '';
    if (nameElementEmpty) {
        var localMessage = new Message("danger", "Please specify the name of the test!");
        nameElement.parents("div.form-group").addClass("has-error");
        showMessage(localMessage, $('#addEntryModal'));
    } else {
        nameElement.parents("div.form-group").removeClass("has-error");
    }

    // verif if all mendatory fields are not empty
    if (nameElementEmpty)
        return;

    showLoaderInModal('#addEntryModal');
    saveEntry("CreateProject", "#addEntryModal", formAdd);
}

function saveEntry(servletName, modalID, form) {
    var jqxhr = $.post(servletName, form.serialize());
    $.when(jqxhr).then(function (data) {
        hideLoaderInModal(modalID);
        if (getAlertType(data.messageType) === 'success') {
            var oTable = $("#testTable").dataTable();
            oTable.fnDraw(true);
            showMessage(data);
            $(modalID).modal('hide');
        } else {
            showMessage(data, $(modalID));
        }
    }).fail(handleErrorAjaxAfterTimeout);
}

function saveUpdateEntryHandler() {
    clearResponseMessage($('#editEntryModal'));
    var formEdit = $('#editEntryModalForm');

    console.log(formEdit.serialize());
    showLoaderInModal('#editEntryModal');
    saveEntry("UpdateTest1", "#editEntryModal", formEdit);
}

function editEntry(test) {
    clearResponseMessageMainPage();
    var jqxhr = $.getJSON("ReadTest", "test=" + test);
    $.when(jqxhr).then(function (data) {
        var obj = data["contentTable"];

        var formEdit = $('#editEntryModal');

        formEdit.find("#test").prop("value", obj.test);
        formEdit.find("#active").prop("value", obj.active);
        formEdit.find("#description").prop("value", obj.description);
        formEdit.find("#automated").prop("value", obj.automated);

        formEdit.modal('show');
    });
}

function CreateTestClick() {
    clearResponseMessageMainPage();
    $('#addEntryModal').modal('show');
}

function renderOptionsForTest(data) {
    var doc = new Doc();
    //check if user has permissions to perform the add and import operations

    if (data["hasPermissions"]) {
        if ($("#createTestButton").length === 0) {
            var contentToAdd = "<div class='marginBottom10'><button id='createTestButton' type='button' class='btn btn-default'>\n\
            " + doc.getDocLabel("page_test", "btn_create") + "</button></div>";

            $("#testTable_wrapper div.ColVis").before(contentToAdd);
            $('#testList #createTestButton').click(CreateTestClick);
        }
    }
}

function aoColumnsFunc() {
    var aoColumns = [
        {
            "data": null,
            "bSortable": false,
            "bSearchable": false,
            "title": "Actions",
            "mRender": function (data, type, obj) {
                var editEntry = '<button id="editEntry" onclick="editEntry(\'' + obj["test"] + '\');"\n\
                                class="editEntry btn btn-default btn-xs margin-right5" \n\
                                name="editEntry" title="' + "edit entry" + '" type="button">\n\
                                <span class="glyphicon glyphicon-pencil"></span></button>';
                var deleteEntry = '<button id="deleteEntry" onclick="deleteEntry(\'' + obj + '\');" \n\
                                class="deleteEntry btn btn-default btn-xs margin-right5" \n\
                                name="deleteEntry" title="' + "delete entry" + '" type="button">\n\
                                <span class="glyphicon glyphicon-trash"></span></button>';
                return '<div class="center btn-group width150">' + editEntry + deleteEntry + '</div>';
            }
        },
        {
            "data": "test",
            "sName": "test",
            "title": "Test"
        },
        {
            "data": "description",
            "sName": "description",
            "title": "Description"
        },
        {
            "data": "active",
            "sName": "active",
            "title": "Active"
        },
        {
            "data": "automated",
            "sName": "automated",
            "title": "Automated"
        },
        {
            "data": "tDateCrea",
            "sName": "tdatecrea",
            "title": "Created"
        }
    ];
    return aoColumns;
}