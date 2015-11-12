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
    displayPageLabel(doc);
    displayFooter(doc);
    displayInvariantList("Active", "TESTACTIVE");
    displayInvariantList("Automated", "TESTAUTOMATED");

}

function displayPageLabel(doc) {
    $("#pageTitle").html(doc.getDocLabel("test", "Test"));
    $("#title").html(doc.getDocLabel("test", "Test"));
    $("[name='addEntryField']").html(doc.getDocLabel("page_test", "btn_create"));
    $("[name='confirmationField']").html(doc.getDocLabel("page_test", "btn_delete"));
    $("[name='editEntryField']").html(doc.getDocLabel("page_test", "btn_edit"));
    $("[name='testField']").html(doc.getDocOnline("test", "Test"));
    $("[name='activeField']").html(doc.getDocOnline("test", "Active"));
    $("[name='automatedField']").html(doc.getDocOnline("test", "Automated"));
    $("[name='descriptionField']").html(doc.getDocOnline("test", "Description"));
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
    createEntry("CreateTest1", formAdd, "#testTable");
}

function saveUpdateEntryHandler() {
    clearResponseMessage($('#editEntryModal'));
    var formEdit = $('#editEntryModalForm');

    showLoaderInModal('#editEntryModal');
    updateEntry("UpdateTest1", formEdit, "#testTable");
}

function editEntry(test) {
    clearResponseMessageMainPage();
    var jqxhr = $.getJSON("ReadTest", "test=" + encodeURIComponent(test));
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

function deleteEntryHandlerClick() {
    var test = $('#confirmationModal').find('#hiddenField1').prop("value");
    var jqxhr = $.post("DeleteTest1", {test: test}, "json");
    $.when(jqxhr).then(function (data) {
        var messageType = getAlertType(data.messageType);
        if (messageType === "success") {
            //redraw the datatable
            var oTable = $("#testTable").dataTable();
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
    messageComplete = messageComplete.replace("%TABLE%", doc.getDocLabel("test", "Test"));
    messageComplete = messageComplete.replace("%ENTRY%", entry);
    showModalConfirmation(deleteEntryHandlerClick, doc.getDocLabel("page_test", "btn_delete"), messageComplete, entry, "", "", "");
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
    var doc = new Doc();

    var aoColumns = [
        {
            "data": null,
            "bSortable": false,
            "bSearchable": false,
            "title": doc.getDocOnline("page_global", "columnAction"),
            "mRender": function (data, type, obj) {
                var testCaseLink = '<a id="testCaseLink" class="btn btn-primary btn-xs margin-right5"\n\
                                    href="./TestCaseList.jsp?test=' + encodeURIComponent(obj["test"]) + '">\n\
                                    <span class="glyphicon glyphicon-new-window"></span>\n\
                                    </a>';

                if (data["hasPermissions"]) {
                    var editEntry = '<button id="editEntry" onclick="editEntry(\'' + escapeHtml(obj["test"]) + '\');"\n\
                                class="editEntry btn btn-default btn-xs margin-right5" \n\
                                name="editEntry" title="' + doc.getDocLabel("page_test", "btn_edit") + '" type="button">\n\
                                <span class="glyphicon glyphicon-pencil"></span></button>';
                    var deleteEntry = '<button id="deleteEntry" onclick="deleteEntry(\'' + escapeHtml(obj["test"]) + '\');" \n\
                                class="deleteEntry btn btn-default btn-xs margin-right5" \n\
                                name="deleteEntry" title="' + doc.getDocLabel("page_test", "btn_delete") + '" type="button">\n\
                                <span class="glyphicon glyphicon-trash"></span></button>';
                    return '<div class="center btn-group width150">' + editEntry + deleteEntry + testCaseLink + '</div>';
                } else {
                    return '<div class="center btn-group width150">' + testCaseLink + '</div>';
                }
            }
        },
        {
            "data": "test",
            "sName": "test",
            "title": doc.getDocOnline("test", "Test")
        },
        {
            "data": "description",
            "sName": "description",
            "title": doc.getDocOnline("test", "Description")
        },
        {
            "data": "active",
            "sName": "active",
            "title": doc.getDocOnline("test", "Active"),
            "className": "center"
        },
        {
            "data": "automated",
            "sName": "automated",
            "title": doc.getDocOnline("test", "Automated"),
            "className": "center"
        },
        {
            "data": "tDateCrea",
            "sName": "tdatecrea",
            "title": doc.getDocOnline("test", "dateCreation")
        }
    ];
    return aoColumns;
}