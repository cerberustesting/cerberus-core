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

        $("#editEntryButton").click(editEntryModalSaveHandler);
        $("#addEntryButton").click(addEntryModalSaveHandler);

        $('#editEntryModal').on('hidden.bs.modal', {extra: "#editEntryModal"}, modalFormCleaner);
        $('#addEntryModal').on('hidden.bs.modal', {extra: "#addEntryModal"}, modalFormCleaner);

        var config = new TableConfigurationsServerSide("testTable", "ReadTest", "contentTable", aoColumnsFunc(), [1, 'asc']);
        var table = createDataTableWithPermissions(config, renderOptionsForTest, "#testList", undefined, true);

        $('[data-toggle="popover"]').popover({
            'placement': 'auto',
            'container': 'body'}
        );
    });
});

function initPage() {
    var doc = new Doc();

    displayHeaderLabel(doc);
    displayGlobalLabel(doc);
    displayPageLabel(doc);
    displayFooter(doc);
    displayInvariantList("isActive", "TESTACTIVE", false);
    displayInvariantList("Automated", "TESTAUTOMATED", false);

}

function displayPageLabel(doc) {
    $("#pageTitle").html(doc.getDocLabel("test", "Test"));
    $("#title").html(doc.getDocLabel("test", "Test"));
    $("#testListLabel").html(doc.getDocLabel("page_test", "table_testlist"));
    $("[name='addEntryField']").html(doc.getDocLabel("page_test", "btn_create"));
    $("[name='confirmationField']").html(doc.getDocLabel("page_test", "button_delete"));
    $("[name='editEntryField']").html(doc.getDocLabel("page_test", "btn_edit"));
    $("[name='testField']").html(doc.getDocOnline("test", "Test"));
    $("[name='activeField']").html(doc.getDocOnline("test", "isActive"));
    $("[name='automatedField']").html(doc.getDocOnline("test", "Automated"));
    $("[name='descriptionField']").html(doc.getDocOnline("test", "description"));
}

function renderOptionsForTest(data) {
    var doc = new Doc();
    //check if user has permissions to perform the add and import operations

    if (data["hasPermissions"]) {
        if ($("#createTestButton").length === 0) {
            var contentToAdd = "<div class='marginBottom10'><button id='createTestButton' type='button' class='btn btn-default'>\n\
            <span class='glyphicon glyphicon-plus-sign'></span> " + doc.getDocLabel("page_test", "btn_create") + "</button></div>";

            $("#testTable_wrapper div#testTable_length").before(contentToAdd);
            $('#testList #createTestButton').click(addEntryClick);
        }
    }
}

function deleteEntryHandlerClick() {
    var test = $('#confirmationModal').find('#hiddenField1').prop("value");
    var jqxhr = $.post("DeleteTest", {test: test}, "json");
    $.when(jqxhr).then(function (data) {
        var messageType = getAlertType(data.messageType);
        if (messageType === "success") {
            //redraw the datatable
            var oTable = $("#testTable").dataTable();
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

function deleteEntryClick(entry) {
    clearResponseMessageMainPage();
    var doc = new Doc();
    var messageComplete = doc.getDocLabel("page_test", "message_delete");
    messageComplete = messageComplete.replace("%ENTRY%", entry);
    showModalConfirmation(deleteEntryHandlerClick, undefined, doc.getDocLabel("page_test", "button_delete"), messageComplete, entry, "", "", "");
}

function addEntryModalSaveHandler() {
    clearResponseMessage($('#addEntryModal'));
    var formAdd = $("#addEntryModal #addEntryModalForm");

    var nameElement = formAdd.find("#test");
    var nameElementEmpty = nameElement.prop("value") === '';

    // if the Test field contains '&'
    var nameElementInvalid = nameElement.prop("value").search("&");

    if (nameElementEmpty) {
        var localMessage = new Message("danger", "Please specify the name of the test!");
        nameElement.parents("div.form-group").addClass("has-error");
        showMessage(localMessage, $('#addEntryModal'));
    } else if (nameElementInvalid != -1) {
        var localMessage = new Message("danger", "The test folder name cannot contain the symbol : &");
        nameElement.parents("div.form-group").addClass("has-error");
        showMessage(localMessage, $('#addEntryModal'));
    } else {
        nameElement.parents("div.form-group").removeClass("has-error");
    }

    // verif if all mendatory fields are not empty
    if (nameElementEmpty || nameElementInvalid != -1)
        return;

    showLoaderInModal('#addEntryModal');
    createEntry("CreateTest", formAdd, "#testTable");
}

function addEntryClick() {
    clearResponseMessageMainPage();
    $('#addEntryModal').modal('show');
}

function editEntryModalSaveHandler() {
    clearResponseMessage($('#editEntryModal'));
    var formEdit = $('#editEntryModalForm');

    showLoaderInModal('#editEntryModal');
    updateEntry("UpdateTest", formEdit, "#testTable");
}

function editEntryClick(test) {
    clearResponseMessageMainPage();

    // In Edit TestCase form, if we change the test, we get the latest testcase from that test.
    $('#editEntryModalForm input[name="test"]').off("change");
    $('#editEntryModalForm input[name="test"]').change(function () {
        // Compare with original value in order to display the warning message.
        displayWarningOnChangeTestKey();
    });


    var jqxhr = $.getJSON("ReadTest", "test=" + encodeURIComponent(test));
    $.when(jqxhr).then(function (data) {
        var obj = data["contentTable"];

        var formEdit = $('#editEntryModal');

        formEdit.find("#test").prop("value", obj.test);
        formEdit.find("#originalTest").prop("value", obj.test);
        formEdit.find("#isActive").prop("value", obj.isActive);
        formEdit.find("#description").prop("value", obj.description);
        formEdit.find("#automated").prop("value", obj.automated);

        if (!(data["hasPermissions"])) { // If readonly, we only readonly all fields
            formEdit.find("#test").prop("readonly", "readonly");
            formEdit.find("#isActive").prop("disabled", "disabled");
            formEdit.find("#description").prop("readonly", "readonly");
            formEdit.find("#automated").prop("disabled", "disabled");

            $('#editEntryButton').attr('class', '');
            $('#editEntryButton').attr('hidden', 'hidden');
        }

        formEdit.modal('show');
    });
}

function displayWarningOnChangeTestKey() {
    // Compare with original value in order to display the warning message.
    let old1 = $("#originalTest").val();
    let new1 = $('#editEntryModal input[name="test"]').val();
    if (old1 !== new1) {
        var localMessage = new Message("WARNING", "If you rename that test, it will loose the corresponding execution historic of all corresponding test cases.");
        showMessage(localMessage, $('#editEntryModal'));
    } else {
        clearResponseMessage($('#editEntryModal'));
    }
}

function aoColumnsFunc() {
    var doc = new Doc();

    var aoColumns = [
        {
            "data": null,
            "sWidth": "100px",
            "bSortable": false,
            "bSearchable": false,
            "title": doc.getDocOnline("page_global", "columnAction"),
            "mRender": function (data, type, obj) {
                var testCaseLink = '<a id="testCaseLink" class="btn btn-primary btn-xs margin-right5"\n\
                                    href="./TestCaseList.jsp?test=' + encodeURIComponent(obj["test"]) + '" title="' + doc.getDocLabel("page_test", "btn_tclist") + '" >\n\
                                    <span class="glyphicon glyphicon-new-window"></span>\n\
                                    </a>';
                var editEntry = '<button id="editEntry" onclick="editEntryClick(\'' + escapeHtml(obj["test"]) + '\');"\n\
                                class="editEntry btn btn-default btn-xs margin-right5" \n\
                                name="editEntry" title="' + doc.getDocLabel("page_test", "btn_edit") + '" type="button">\n\
                                <span class="glyphicon glyphicon-pencil"></span></button>';
                var viewEntry = '<button id="editEntry" onclick="editEntryClick(\'' + escapeHtml(obj["test"]) + '\');"\n\
                                class="editEntry btn btn-default btn-xs margin-right25" \n\
                                name="editEntry" title="' + doc.getDocLabel("page_test", "btn_edit") + '" type="button">\n\
                                <span class="glyphicon glyphicon-eye-open"></span></button>';
                var deleteEntry = '<button id="deleteEntry" onclick="deleteEntryClick(\'' + escapeHtml(obj["test"]) + '\');" \n\
                                class="deleteEntry btn btn-default btn-xs margin-right25" \n\
                                name="deleteEntry" title="' + doc.getDocLabel("page_test", "button_delete") + '" type="button">\n\
                                <span class="glyphicon glyphicon-trash"></span></button>';

                if (data["hasPermissions"]) {
                    return '<div class="center btn-group width150">' + editEntry + deleteEntry + testCaseLink + '</div>';
                } else {
                    return '<div class="center btn-group width150">' + viewEntry + testCaseLink + '</div>';
                }
            }
        },
        {
            "data": "test",
            "sName": "test",
            "sWidth": "80px",
            "title": doc.getDocOnline("test", "Test")
        },
        {
            "data": "description",
            "sName": "description",
            "like": true,
            "sWidth": "100px",
            "title": doc.getDocOnline("test", "description")
        },
        {
            "data": "isActive",
            "sName": "isActive",
            "sWidth": "30px",
            "title": doc.getDocOnline("test", "isActive"),
            "className": "center",
            "mRender": function (data, type, obj) {
                if (data) {
                    return '<input type="checkbox" checked disabled />';
                } else {
//                        $('[id="runTest' + encodeURIComponent(obj["test"]) + encodeURIComponent(obj["testcase"]) + '"]').attr("disabled", "disabled");
                    return '<input type="checkbox" disabled />';
                }
            }
        },
        {
            "data": "dateCreated",
            "visible": false,
            "sName": "tes.dateCreated",
            "like": true,
            "title": doc.getDocOnline("transversal", "DateCreated"),
            "sWidth": "150px",
            "sDefaultContent": "",
            "mRender": function (data, type, oObj) {
                return getDate(oObj["dateCreated"]);
            }
        },
        {
            "data": "usrCreated",
            "visible": false,
            "sName": "tes.usrCreated",
            "title": doc.getDocOnline("transversal", "UsrCreated"),
            "sWidth": "100px",
            "sDefaultContent": ""
        },
        {
            "data": "dateModif",
            "visible": false,
            "like": true,
            "sName": "tes.dateModif",
            "title": doc.getDocOnline("transversal", "DateModif"),
            "sWidth": "150px",
            "sDefaultContent": "",
            "mRender": function (data, type, oObj) {
                return getDate(oObj["dateModif"]);
            }

        },
        {
            "data": "usrModif",
            "visible": false,
            "sName": "tes.usrModif",
            "title": doc.getDocOnline("transversal", "UsrModif"),
            "sWidth": "100px",
            "sDefaultContent": ""
        }
    ];
    return aoColumns;
}
