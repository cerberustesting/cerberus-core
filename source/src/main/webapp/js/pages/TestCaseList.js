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
/* global modalFormCleaner */

var testAutomaticModal = "";

$.when($.getScript("js/global/global.js")).then(function() {
    $(document).ready(function() {
        initPage();
    });
});

function initPage() {
    displayPageLabel();
    var table = loadTable();

    // MASS ACTION
    $("#massActionTestCaseButtonAddLabel").click(massActionModalSaveHandler_addLabel);
    $("#massActionTestCaseButtonRemoveLabel").click(massActionModalSaveHandler_removeLabel);
    $("#massActionTestCaseButtonUpdate").click(massActionModalSaveHandler_update);
    $("#massActionTestCaseButtonExport").click(massActionModalSaveHandler_export);
    $("#massActionTestCaseButtonDelete").click(massActionModalSaveHandler_delete);

    // MASS ACTION
    $('#massActionTestCaseModal').on('hidden.bs.modal', massActionModalCloseHandler);
    $('[data-toggle="tooltip"]').tooltip();

    initModalTestCase();
    $('#editTestCaseModal').data("initLabel", true);
    initMassActionModal();

    $('[data-toggle="popover"]').popover({
        'placement': 'auto',
        'container': 'body'}
    );

    var availableUsers = getUserArray(true);
    $("input#massExecutor").autocomplete({
        source: availableUsers
    });

}

function initMassActionModal() {
    $("#massActionTestCaseModal #massStatus").prop("disabled", true);
    $("#statusCheckbox").prop("checked", false);
    $("#statusCheckbox").change(function() {
        if ($(this).prop("checked")) {
            $("#massActionTestCaseModal #massStatus").prop("disabled", false);
        } else {
            $("#massActionTestCaseModal #massStatus").prop("disabled", true);
        }
    });
    $("#massActionTestCaseModal #massApplication").prop("disabled", true);
    $("#applicationCheckbox").prop("checked", false);
    $("#applicationCheckbox").change(function() {
        if ($(this).prop("checked")) {
            $("#massActionTestCaseModal #massApplication").prop("disabled", false);
        } else {
            $("#massActionTestCaseModal #massApplication").prop("disabled", true);
        }
    });
    $("#massActionTestCaseModal #massPriority").prop("disabled", true);
    $("#priorityCheckbox").prop("checked", false);
    $("#priorityCheckbox").change(function() {
        if ($(this).prop("checked")) {
            $("#massActionTestCaseModal #massPriority").prop("disabled", false);
        } else {
            $("#massActionTestCaseModal #massPriority").prop("disabled", true);
        }
    });
    $("#massActionTestCaseModal #massExecutor").prop("disabled", true);
    $("#executorCheckbox").prop("checked", false);
    $("#executorCheckbox").change(function() {
        if ($(this).prop("checked")) {
            $("#massActionTestCaseModal #massExecutor").prop("disabled", false);
        } else {
            $("#massActionTestCaseModal #massExecutor").prop("disabled", true);
        }
    });
}

function displayPageLabel() {
    var doc = new Doc();

    displayHeaderLabel(doc);
    displayGlobalLabel(doc);
    $("#testCaseListLabel").html(doc.getDocOnline("page_testcaselist", "testcaselist"));
    $("#pageTitle").html(doc.getDocOnline("page_testcaselist", "title"));
    $("#title").html(doc.getDocOnline("page_testcaselist", "title"));
    displayFooter(doc);

}

function loadTable(selectTest, sortColumn) {

    //clear the old report content before reloading it
    $("#testCaseList").empty();
    $("#testCaseList").html('<table id="testCaseTable" class="table table-bordered table-hover display" name="testCaseTable">\n\
                                            </table><div class="marginBottom20"></div>');

    var contentUrl = "ReadTestCase";

    //configure and create the dataTable
    var jqxhr = $.getJSON("FindInvariantByID", "idName=COUNTRY");

    $.when(jqxhr).then(function(data) {
        sortColumn = 2;

        var config = new TableConfigurationsServerSide("testCaseTable", contentUrl, "contentTable", aoColumnsFunc(data, "testCaseTable"), [2, 'asc']);

        var table = createDataTableWithPermissions(config, renderOptionsForTestCaseList, "#testCaseList", undefined, true);


        var app = GetURLParameter('application');
        if (app !== "" && app !== null) {
            filterOnColumn("testCaseTable", "application", app);
        }

        var test = GetURLParameter('test');
        if (test !== "" && test !== null) {
            filterOnColumn("testCaseTable", "test", test);
        }

        var label = GetURLParameter('label');
        if (label !== "" && label !== null) {
            filterOnColumn("testCaseTable", "labels", label);
        }

        // Mass action
        $("#selectAll").click(selectAll);

        return table;

    });
}

function renderOptionsForTestCaseList(data) {
    var doc = new Doc();
    //check if user has permissions to perform the add and import operations
    if (data["hasPermissionsCreate"]) {
        if ($("#createTestCaseButton").length === 0) {
            var contentToAdd = "<div class='marginBottom10'>";
            contentToAdd += "<button id='createTestCaseButton' type='button' class='btn btn-default'><span class='glyphicon glyphicon-plus-sign'></span> " + doc.getDocLabel("page_testcaselist", "btn_create") + "</button>";
            contentToAdd += "<button id='importTestCaseButton' type='button' class='btn btn-default'><span class='glyphicon glyphicon-import'></span> " + doc.getDocLabel("page_testcaselist", "btn_import") + "</button>";
            contentToAdd += "<button id='createBrpMassButton' type='button' class='btn btn-default'><span class='glyphicon glyphicon-th-list'></span> " + doc.getDocLabel("page_global", "button_massAction") + "</button>";
            contentToAdd += "</div>";

            $("#testCaseTable_wrapper #testCaseTable_length").before(contentToAdd);

            $('#testCaseList #createTestCaseButton').click(data, function() {
                // Getting the Test from the 1st row of the testcase table.
                if ($("#testCaseTable td.sorting_1")[0] !== undefined) {
                    var firstRowTest = testAutomaticModal;
//                    addTestCaseClick(firstRowTest);
                    openModalTestCase(firstRowTest, undefined, "ADD");
                } else {
                    var testQueryString = GetURLParameter("test", undefined);
//                    addTestCaseClick();
                    openModalTestCase(testQueryString, undefined, "ADD");
                }
            });
            $('#testCaseList #importTestCaseButton').click(importTestCaseClick);
            $('#testCaseList #createBrpMassButton').click(massActionClick);
        }
    }
}

/********************************************************
 //DELETE TESTCASE
 /********************************************************

 /* Function called on click on delete button
 * This function display a confirmation modal
 * @param {type} test
 * @param {type} testCase
 * @returns {undefined}
 */
function deleteEntryClick(test, testCase) {
    clearResponseMessageMainPage();
    var doc = new Doc();
    var messageComplete = doc.getDocLabel("page_testcase", "message_delete");
    messageComplete = messageComplete.replace("%ENTRY%", test + " / " + testCase);
    showModalConfirmation(deleteEntryHandlerClick, undefined, "Delete", messageComplete, test, testCase, "", "");
}

/*
 * Function called when confirmation button pressed
 * @returns {undefined}
 */
function deleteEntryHandlerClick() {
    var test = $('#confirmationModal').find('#hiddenField1').prop("value");
    var testCase = $('#confirmationModal').find('#hiddenField2').prop("value");
    var jqxhr = $.post("DeleteTestCase", {test: test, testCase: testCase}, "json");
    $.when(jqxhr).then(function(data) {
        var messageType = getAlertType(data.messageType);
        if (messageType === "success") {
            //redraw the datatable
            var oTable = $("#testCaseTable").dataTable();
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

function selectAll() {
    if ($(this).prop("checked"))
        $("[data-line='select']").prop("checked", true);
    else
        $("[data-line='select']").prop("checked", false);
}

function massActionModalSaveHandler_addLabel() {
    clearResponseMessage($('#massActionTestCaseModal'));

    var formNewValues = $('#massActionTestCaseModal #massActionTestCaseModalFormAddLabel');
    var formList = $('#massActionForm');
    var paramSerialized = formNewValues.serialize() + "&" + formList.serialize().replace(/=on/g, '').replace(/test-/g, 'test=').replace(/testcase-/g, '&testcase=');

    var table2 = $('#selectLabelAddS').treeview('getSelected', {levels: 20, silent: true});
    for (var i = 0; i < table2.length; i++) {
        paramSerialized = paramSerialized + "&labelid=" + table2[i].id;
    }
    var table2 = $('#selectLabelAddR').treeview('getSelected', {levels: 20, silent: true});
    for (var i = 0; i < table2.length; i++) {
        paramSerialized = paramSerialized + "&labelid=" + table2[i].id;
    }
    var table2 = $('#selectLabelAddB').treeview('getSelected', {levels: 20, silent: true});
    for (var i = 0; i < table2.length; i++) {
        paramSerialized = paramSerialized + "&labelid=" + table2[i].id;
    }

    showLoaderInModal('#massActionTestCaseModal');

    var jqxhr = $.post("CreateTestCaseLabel", paramSerialized, "json");
    $.when(jqxhr).then(function(data) {
        // unblock when remote call returns
        hideLoaderInModal('#massActionTestCaseModal');
        if ((getAlertType(data.messageType) === "success") || (getAlertType(data.messageType) === "warning")) {
            $('#testCaseTable').DataTable().draw();
            $("#selectAll").prop("checked", false);
            $('#massActionTestCaseModal').modal('hide');
            showMessage(data);
        } else {
            showMessage(data, $('#massActionTestCaseModal'));
        }
    }).fail(handleErrorAjaxAfterTimeout);
}

function massActionModalSaveHandler_removeLabel() {
    clearResponseMessage($('#massActionTestCaseModal'));

    var formNewValues = $('#massActionTestCaseModal #massActionTestCaseModalFormAddLabel');
    var formList = $('#massActionForm');
    var paramSerialized = formNewValues.serialize() + "&" + formList.serialize().replace(/=on/g, '').replace(/test-/g, 'test=').replace(/testcase-/g, '&testcase=');

    var table2 = $('#selectLabelAddS').treeview('getSelected', {levels: 20, silent: true});
    for (var i = 0; i < table2.length; i++) {
        paramSerialized = paramSerialized + "&labelid=" + table2[i].id;
    }
    var table2 = $('#selectLabelAddR').treeview('getSelected', {levels: 20, silent: true});
    for (var i = 0; i < table2.length; i++) {
        paramSerialized = paramSerialized + "&labelid=" + table2[i].id;
    }
    var table2 = $('#selectLabelAddB').treeview('getSelected', {levels: 20, silent: true});
    for (var i = 0; i < table2.length; i++) {
        paramSerialized = paramSerialized + "&labelid=" + table2[i].id;
    }

    showLoaderInModal('#massActionTestCaseModal');

    var jqxhr = $.post("DeleteTestCaseLabel", paramSerialized, "json");
    $.when(jqxhr).then(function(data) {
        // unblock when remote call returns
        hideLoaderInModal('#massActionTestCaseModal');
        if ((getAlertType(data.messageType) === "success") || (getAlertType(data.messageType) === "warning")) {
            $('#testCaseTable').DataTable().draw();
            $("#selectAll").prop("checked", false);
            $('#massActionTestCaseModal').modal('hide');
            showMessage(data);
        } else {
            showMessage(data, $('#massActionTestCaseModal'));
        }
    }).fail(handleErrorAjaxAfterTimeout);
}

function massActionModalSaveHandler_update() {
    clearResponseMessage($('#massActionTestCaseModal'));

    var formNewValues = $('#massActionTestCaseModal #massActionTestCaseModalFormUpdate');
    var formList = $('#massActionForm');
    var paramSerialized = formNewValues.serialize() + "&" + formList.serialize().replace(/=on/g, '').replace(/test-/g, 'test=').replace(/testcase-/g, '&testcase=');

    showLoaderInModal('#massActionTestCaseModal');

    var jqxhr = $.post("UpdateTestCaseMass", paramSerialized, "json");
    $.when(jqxhr).then(function(data) {
        // unblock when remote call returns
        hideLoaderInModal('#massActionTestCaseModal');
        if ((getAlertType(data.messageType) === "success") || (getAlertType(data.messageType) === "warning")) {
            $('#testCaseTable').DataTable().draw();
            $("#selectAll").prop("checked", false);
            $('#massActionTestCaseModal').modal('hide');
            showMessage(data);
        } else {
            showMessage(data, $('#massActionTestCaseModal'));
        }
    }).fail(handleErrorAjaxAfterTimeout);
}

function massActionModalSaveHandler_export() {
    clearResponseMessage($('#massActionTestCaseModal'));

    $("input[data-line=select]:checked").each(function(index, file) {
        var t = $(file).prop("name").replace(/test-/g, 'test=').replace(/testcase-/g, '&testcase=');
        var test = t.split("test=")[1].split("&testcase=")[0];
        var testcase = t.split("test=")[1].split("&testcase=")[1];

        var url = "./ExportTestCase?" + $(file).prop("name").replace(/test-/g, 'test=').replace(/testcase-/g, '&testcase=');
        let iframe = document.createElement('iframe');
        iframe.style.visibility = 'collapse';
        document.body.append(iframe);

        iframe.contentDocument.write(
                "<form action='" + url + "' method='GET'><input name='test' value='" + test + "'/><input name='testcase' value='" + testcase + "'/></form>"
                );
        iframe.contentDocument.forms[0].submit();

        setTimeout(() => iframe.remove(), 2000);
    });
    var data = '{"messageType":"OK","message":"Export OK"}';
    showMessage(JSON.parse(data));
    $('#testCaseTable').DataTable().draw();
    $("#selectAll").prop("checked", false);
    $('#massActionTestCaseModal').modal('hide');
}


function massActionModalSaveHandler_delete() {
    clearResponseMessage($('#massActionTestCaseModal'));

    var doc = new Doc();
    var messageComplete = doc.getDocLabel("page_testcase", "message_delete_all");
    messageComplete += "</br></br>";

    $("input[data-line=select]:checked").each(function(index, file) {
        var t = $(file).prop("name").replace(/test-/g, 'test=').replace(/testcase-/g, '&testcase=');
        var test = t.split("test=")[1].split("&testcase=")[0];
        var testcase = t.split("test=")[1].split("&testcase=")[1];
        messageComplete += (index + 1) + ': ' + test + " - " + testcase;
        messageComplete += "</br>";
    });
    showModalConfirmation(deleteMassTestCase, undefined, "Delete", messageComplete);

}

function deleteMassTestCase() {
    var returnMessage = '{"messageType":"OK","message":"Delete OK"}';

    //Loop on TestCase Selected to delete them
    $("input[data-line=select]:checked").each(function(index, file) {
        var t = $(file).prop("name").replace(/test-/g, 'test=').replace(/testcase-/g, '&testCase=');
        var url = "DeleteTestCase?" + t;

        $.ajax({
            url: url,
            async: true,
            method: "GET",
            success: function(data) {
                data = JSON.parse(data);
                if (getAlertType(data.messageType) !== "success") {
                    returnMessage = data;
                }
            },
            error: function() {
                returnMessage = '{"messageType":"KO","message":"Delete KO"}';
            }
        });

    });
    showMessage(JSON.parse(returnMessage));

    $('#testCaseTable').DataTable().draw();
    $("#selectAll").prop("checked", false);
    $('#confirmationModal').modal('hide');
    $('#massActionTestCaseModal').modal('hide');

}

function massActionModalCloseHandler() {
    // reset form values
    $('#massActionTestCaseModal #massActionTestCaseModalFormUpdate')[0].reset();
    // remove all errors on the form fields
    $(this).find('div.has-error').removeClass("has-error");
    // clear the response messages of the modal
    clearResponseMessage($('#massActionTestCaseModal'));
}

function massActionClick() {
    var doc = new Doc();
    clearResponseMessageMainPage();

    // When creating a new item, Define here the default value.
    var formList = $('#massActionForm');
    if (formList.serialize().indexOf("test-") === -1) {
        var localMessage = new Message("danger", doc.getDocLabel("page_global", "message_massActionError"));
        showMessage(localMessage, null);
    } else {
        // Title of the label list.
        $("[name='labelMassField']").html("Labels from system : " + getUser().defaultSystem);

        // Labels
        loadLabel(undefined, getUser().defaultSystem, "#selectLabelAdd", "4");

        // Load Status.
        $("[name='massStatus']").empty();
        displayInvariantList("massStatus", "TCSTATUS", false);

        // Load Applications.
        $("[name='massApplication']").empty();
        var jqxhr = $.getJSON("ReadApplication");
        $.when(jqxhr).then(function(data) {
            var applicationList = $("[name='massApplication']");

            for (var index = 0; index < data.contentTable.length; index++) {
                if (data.contentTable[index].system === getUser().defaultSystem) {
                    applicationList.prepend($('<option></option>').addClass('bold-option').text(data.contentTable[index].application).val(data.contentTable[index].application));
                } else {
                    applicationList.append($('<option></option>').text(data.contentTable[index].application).val(data.contentTable[index].application));
                }
            }
        });

        // Load Status.
        $("[name='massPriority']").empty();
        displayInvariantList("massPriority", "PRIORITY", false);

        $('#massActionTestCaseModal').modal('show');
    }
}

function importTestCaseClick() {
    $("#importTestCaseButton").off("click");

    var fileInput = document.getElementById('files');
    fileInput.addEventListener('change', function(evnt) {
        fileList = [];
        for (var i = 0; i < fileInput.files.length; i++) {
            fileList.push(fileInput.files[i]);
        }
        renderFileList(fileList);
    });

    $("#importTestCaseButton").click(function() {
        confirmImportTestCaseModalHandler();
    });

    var doc = new Doc();
    var text = doc.getDocLabel("page_testcaselist", "import_testcase_msg");
    $('#importTestCaseModalText').text(text);

    $('#importTestCaseModal').modal('show');
}

function buttonCloseImportHandler(event) {
    var modalID = event.data.extra;
    // reset form values
    $(modalID)[0].reset();
    // remove all errors on the form fields
    $(this).find('div.has-error').removeClass("has-error");
    // clear the response messages of the modal
    clearResponseMessage($(modalID));

}

function confirmImportTestCaseModalHandler() {
    clearResponseMessage($('#importTestCaseModal'));

    var formEdit = $('#importTestCaseModal #importTestCaseModalForm');

    var sa = formEdit.serializeArray();
    var formData = new FormData();

    for (var i in sa) {
        formData.append(sa[i].name, sa[i].value);
    }

    var file = $("#importTestCaseModal input[type=file]");
    for (var i = 0; i < $($(file).get(0)).prop("files").length; i++) {
        formData.append("file", file.prop("files")[i]);
    }

    // Calculate servlet name to call.
    var myServlet = "ImportTestCase";

    // Get the header data from the form.
    showLoaderInModal('#importTestCaseModal');

    $.ajax({
        url: myServlet,
        async: true,
        method: "POST",
        data: formData,
        processData: false,
        contentType: false,
        success: function(data) {
            data = JSON.parse(data);
            if (getAlertType(data.messageType) === "success") {
                var oTable = $("#testCaseTable").dataTable();
                oTable.fnDraw(false);
                $('#importTestCaseModal').modal('hide');
                showMessage(data);
            } else {
                showMessage(data, $('#importTestCaseModal'));
            }
            hideLoaderInModal('#importTestCaseModal');
        },
        error: showUnexpectedError
    });
}

/**
 * Render the list of file to import
 * @param {type} fileList
 * @return nothing
 */
function renderFileList(fileList) {
    var fileListDisplay = document.getElementById('file-list-display');
    fileListDisplay.innerHTML = '';
    fileList.forEach(function(file, index) {
        var fileDisplayEl = document.createElement('p');
        fileDisplayEl.innerHTML = (index + 1) + ': ' + file.name;
        fileListDisplay.appendChild(fileDisplayEl);
    });
}


function setActive(checkbox) {
    var test = checkbox.dataset.test;
    var testCase = checkbox.name;
    var active;

    if (checkbox.checked === true) {
        active = "Y";
    } else {
        active = "N";
    }

    $.ajax({
        url: "UpdateTestCase",
        method: "POST",
        data: {test: test, testCase: testCase, originalTest: test, originalTestCase: testCase, active: active},
        dataType: "json",
        success: function(data) {
            if (active === "Y") {
                $('[id="runTest' + encodeURIComponent(test) + encodeURIComponent(testCase) + '"]').removeAttr("disabled");
            } else {
                $('[id="runTest' + encodeURIComponent(test) + encodeURIComponent(testCase) + '"]').attr("disabled", "disabled");
            }
            clearResponseMessageMainPage();
            var messageType = getAlertType(data.messageType);
            //show message in the main page
            showMessageMainPage(messageType, data.message, false);
        },
        error: showUnexpectedError
    });

}

function setCountry(checkbox) {
    var test = checkbox.dataset.test;
    var testCase = checkbox.dataset.testcase;
    var country = checkbox.name;

    if (checkbox.checked === true) {
        $.ajax({
            url: "CreateTestCaseCountry",
            method: "POST",
            data: "test=" + test + "&testCase=" + testCase + "&country=" + country,
            dataType: "json",
            success: function(data) {
                clearResponseMessageMainPage();
                var messageType = getAlertType(data.messageType);
                //show message in the main page
                showMessageMainPage(messageType, data.message, false);
            },
            error: showUnexpectedError
        });

    } else {
        $.ajax({
            url: "DeleteTestCaseCountry",
            method: "POST",
            data: "test=" + test + "&testCase=" + testCase + "&country=" + country,
            dataType: "json",
            success: function(data) {
                clearResponseMessageMainPage();
                var messageType = getAlertType(data.messageType);
                //show message in the main page
                showMessageMainPage(messageType, data.message, false);
            },
            error: showUnexpectedError
        });
    }

}

function filterOnLabel(element) {
    var newLabel = $(element).get(0).textContent;
    var colIndex = $(element).parent().parent().get(0).cellIndex;
    $("#testCaseTable").dataTable().fnFilter(newLabel, colIndex);
}

function aoColumnsFunc(countries, tableId) {
    var doc = new Doc();

    var countryLen = countries.length;
    var aoColumns = [
        {"data": null,
            "title": '<input id="selectAll" title="' + doc.getDocLabel("page_global", "tooltip_massAction") + '" type="checkbox"></input>',
            "bSortable": false,
            "sWidth": "30px",
            "bSearchable": false,
            "mRender": function(data, type, obj) {
                var selectBrp = '<input id="selectLine" \n\
                                class="selectBrp margin-right5" \n\
                                name="test-' + obj["test"] + 'testcase-' + obj["testCase"] + '" data-line="select" data-id="' + obj["test"] + obj["testCase"] + '" title="' + doc.getDocLabel("page_global", "tooltip_massActionLine") + '" type="checkbox">\n\
                                </input>';
                if (data.hasPermissionsUpdate) { //only draws the options if the user has the correct privileges
                    return '<div class="center btn-group width50">' + selectBrp + '</div>';
                }
                return '<div class="center btn-group width50"></div>';

            }
        },
        {
            "data": null,
            "bSortable": false,
            "bSearchable": false,
            "title": doc.getDocOnline("page_global", "columnAction"),
            "sDefaultContent": "",
            "sWidth": "175px",
            "mRender": function(data, type, obj) {
                var buttons = "";

                var editEntry = '<button id="editEntry" onclick="openModalTestCase(\'' + escapeHtml(obj["test"]) + '\',\'' + escapeHtml(obj["testCase"]) + '\',\'EDIT\');"\n\
                                class="editEntry btn btn-default btn-xs margin-right5" \n\
                                name="editEntry" data-toggle="tooltip"  title="' + doc.getDocLabel("page_testcaselist", "btn_edit") + '" type="button">\n\
                                <span class="glyphicon glyphicon-pencil"></span></button>';
                var viewEntry = '<button id="editEntry" onclick="openModalTestCase(\'' + escapeHtml(obj["test"]) + '\',\'' + escapeHtml(obj["testCase"]) + '\',\'EDIT\');"\n\
                                class="editEntry btn btn-default btn-xs margin-right5" \n\
                                name="editEntry" data-toggle="tooltip"  title="' + doc.getDocLabel("page_testcaselist", "btn_view") + '" type="button">\n\
                                <span class="glyphicon glyphicon-eye-open"></span></button>';
                var deleteEntry = '<button id="deleteEntry" onclick="deleteEntryClick(\'' + escapeHtml(obj["test"]) + '\',\'' + escapeHtml(obj["testCase"]) + '\');"\n\
                                        class="deleteEntry btn btn-default btn-xs margin-right25" \n\
                                        name="deleteEntry" data-toggle="tooltip"  title="' + doc.getDocLabel("page_testcaselist", "btn_delete") + '" type="button">\n\
                                        <span class="glyphicon glyphicon-trash"></span></button>';
                var exportEntry = '<a id="exportEntry" href="./ExportTestCase?test=' + encodeURIComponent(obj["test"]) + '&testcase=' + encodeURIComponent(obj["testCase"]) + '"\n\
                                        class="editEntry btn btn-default btn-xs margin-right5" \n\
                                        name="exportEntry" data-toggle="tooltip"  title="' + doc.getDocLabel("page_testcaselist", "btn_export") + '" type="button">\n\
                                        <span class="glyphicon glyphicon-export"></span></a>';
                var duplicateEntry = '<button id="duplicateEntry" onclick="openModalTestCase(\'' + escapeHtml(obj["test"]) + '\',\'' + escapeHtml(obj["testCase"]) + '\',\'DUPLICATE\');"\n\
                                        class="duplicateEntry btn btn-default btn-xs margin-right5" \n\
                                        name="duplicateEntry" data-toggle="tooltip"  title="' + doc.getDocLabel("page_testcaselist", "btn_duplicate") + '" type="button">\n\
                                        <span class="glyphicon glyphicon-duplicate"></span></button>';
                var editScript = '<a id="testCaseLink" class="btn btn-primary btn-xs marginRight5"\n\
                                    data-toggle="tooltip" title="' + doc.getDocLabel("page_testcaselist", "btn_editScript") + '" href="./TestCaseScript.jsp?test=' + encodeURIComponent(obj["test"]) + '&testcase=' + encodeURIComponent(obj["testCase"]) + '">\n\
                                    <span class="glyphicon glyphicon-new-window"></span>\n\
                                    </a>';
                var runTest = '<a id="runTest" class="btn btn-primary btn-xs marginRight5 marginLeft20"\n\
                                    data-toggle="tooltip" title="' + doc.getDocLabel("page_testcaselist", "btn_runTest") + '" href="./RunTests.jsp?test=' + encodeURIComponent(obj["test"]) + '&testcase=' + encodeURIComponent(obj["testCase"]) + '">\n\
                                    <span class="glyphicon glyphicon-play"></span>\n\
                                    </a>';

                if (data.hasPermissionsUpdate) {
                    buttons += editEntry;
                    buttons += editScript;
                    buttons += duplicateEntry;
                } else {
                    buttons += viewEntry;
                    buttons += editScript;
                    if (data.hasPermissionsCreate) {
                        buttons += duplicateEntry;
                    }
                }
                if (data.hasPermissionsUpdate) {
                    buttons += exportEntry;
                }
                if (data.hasPermissionsDelete) {
                    buttons += deleteEntry;
                }
                buttons += runTest;

                return '<div class="center btn-group width250">' + buttons + '</div>';
            }
        },
        {
            "data": "test",
            "sName": "tec.test",
            "title": doc.getDocOnline("test", "Test"),
            "sWidth": "120px",
            "sDefaultContent": "",
            "mRender": function(data, type, oObj, full) {
                if (full.row == 0) {
                    testAutomaticModal = oObj.test;
                }
                return oObj.test;
            }

        },
        {
            "data": "testCase",
            "sName": "tec.testCase",
            "like": true,
            "title": doc.getDocOnline("testcase", "TestCase"),
            "sWidth": "82px",
            "sDefaultContent": ""
        },
        {
            "data": "description",
            "sName": "tec.description",
            "like": true,
            "title": doc.getDocOnline("testcase", "Description"),
            "sWidth": "300px",
            "sDefaultContent": ""
        },
        {
            "data": "labels",
            "visible": false,
            "sName": "lab.label",
            "title": doc.getDocOnline("label", "label"),
            "bSortable": false,
            "sWidth": "170px",
            "sDefaultContent": "",
            "render": function(data, type, full, meta) {
                var labelValue = '';
                $.each(data, function(i, e) {
                    labelValue += '<div style="float:left"><span class="label label-primary" onclick="filterOnLabel(this)" style="cursor:pointer;background-color:' + e.color + '" data-toggle="tooltip" title="' + e.description + '">' + e.name + '</span></div> ';
                });
                return labelValue;
            }
        },
        {
            "data": "labelsSTICKER",
            "sName": "lab.labelsSTICKER",
            "title": doc.getDocOnline("label", "labelsSTICKER"),
            "bSortable": false,
            "sWidth": "170px",
            "sDefaultContent": "",
            "render": function(data, type, full, meta) {
                var labelValue = '';
                $.each(data, function(i, e) {
                    labelValue += '<div style="float:left"><span class="label label-primary" onclick="filterOnLabel(this)" style="cursor:pointer;background-color:' + e.color + '" data-toggle="tooltip" title="' + e.description + '">' + e.name + '</span></div> ';
                });
                return labelValue;
            }
        },
        {
            "data": "labelsREQUIREMENT",
            "visible": false,
            "sName": "lab.labelsREQUIREMENT",
            "title": doc.getDocOnline("label", "labelsREQUIREMENT"),
            "bSortable": false,
            "sWidth": "170px",
            "sDefaultContent": "",
            "render": function(data, type, full, meta) {
                var labelValue = '';
                $.each(data, function(i, e) {
                    labelValue += '<div style="float:left"><span class="label label-primary" onclick="filterOnLabel(this)" style="cursor:pointer;background-color:' + e.color + '" data-toggle="tooltip" title="' + e.description + '">' + e.name + '</span></div> ';
                });
                return labelValue;
            }
        },
        {
            "data": "labelsBATTERY",
            "visible": false,
            "sName": "lab.labelsBATTERY",
            "title": doc.getDocOnline("label", "labelsBATTERY"),
            "bSortable": false,
            "sWidth": "170px",
            "sDefaultContent": "",
            "render": function(data, type, full, meta) {
                var labelValue = '';
                $.each(data, function(i, e) {
                    labelValue += '<div style="float:left"><span class="label label-primary" onclick="filterOnLabel(this)" style="cursor:pointer;background-color:' + e.color + '" data-toggle="tooltip" title="' + e.description + '">' + e.name + '</span></div> ';
                });
                return labelValue;
            }
        },
        {
            "data": "status",
            "sName": "tec.status",
            "title": doc.getDocOnline("testcase", "Status"),
            "sWidth": "100px",
            "sDefaultContent": ""
        },
        {
            "data": "isActive",
            "visible": false,
            "sName": "tec.isActive",
            "title": doc.getDocOnline("testcase", "IsActive"),
            "sWidth": "100px",
            "sDefaultContent": ""
        },
        {
            "data": "isActivePROD",
            "visible": false,
            "sName": "tec.isActivePROD",
            "title": doc.getDocOnline("testcase", "IsActivePROD"),
            "sWidth": "100px",
            "sDefaultContent": ""
        },
        {
            "data": "isActiveQA",
            "visible": false,
            "sName": "tec.isActiveQA",
            "title": doc.getDocOnline("testcase", "IsActiveQA"),
            "sWidth": "100px",
            "sDefaultContent": ""
        },
        {
            "data": "isActiveUAT",
            "visible": false,
            "sName": "tec.isActiveUAT",
            "title": doc.getDocOnline("testcase", "IsActiveUAT"),
            "sWidth": "100px",
            "sDefaultContent": ""
        },
        {
            "data": "application",
            "sName": "tec.application",
            "title": doc.getDocOnline("application", "Application"),
            "sWidth": "100px",
            "sDefaultContent": ""
        },
        {
            "data": "system",
            "sName": "app.system",
            "title": doc.getDocOnline("invariant", "SYSTEM"),
            "sWidth": "100px",
            "sDefaultContent": ""
        },
        {
            "data": "isActive",
            "visible": false,
            "sName": "tec.isActive",
            "title": doc.getDocOnline("testcase", "isActive"),
            "sDefaultContent": "",
            "sWidth": "70px",
            "className": "center",
            "mRender": function(data, type, obj) {
                if (obj.hasPermissionsUpdate) {
                    if (data === "Y") {
                        return '<input type="checkbox" name="' + obj["testCase"] + '" data-test="' + obj.test + '" onchange="setActive(this);" checked/>';
                    } else if (data === "N") {
                        $('[id="runTest' + encodeURIComponent(obj["test"]) + encodeURIComponent(obj["testCase"]) + '"]').attr("disabled", "disabled");
                        return '<input type="checkbox" name="' + obj["testCase"] + '" data-test="' + obj.test + '" onchange="setActive(this);" />';
                    }
                } else {
                    if (data === "Y") {
                        return '<input type="checkbox" checked disabled />';
                    } else {
                        $('[id="runTest' + encodeURIComponent(obj["test"]) + encodeURIComponent(obj["testCase"]) + '"]').attr("disabled", "disabled");
                        return '<input type="checkbox" disabled />';
                    }
                }
            }
        },
        {
            "data": "priority",
            "visible": false,
            "sName": "tec.priority",
            "title": doc.getDocOnline("invariant", "PRIORITY"),
            "sWidth": "70px",
            "sDefaultContent": ""
        },
        {
            "data": "function",
            "visible": false,
            "like": true,
            "sName": "tec.function",
            "title": doc.getDocOnline("testcase", "Function"),
            "sWidth": "100px",
            "sDefaultContent": ""
        },
        {
            "data": "origine",
            "visible": false,
            "sName": "tec.origine",
            "title": doc.getDocOnline("testcase", "Origine"),
            "sWidth": "70px",
            "sDefaultContent": ""
        },
        {
            "data": "refOrigine",
            "visible": false,
            "sName": "tec.refOrigine",
            "like": true,
            "title": doc.getDocOnline("testcase", "RefOrigine"),
            "sWidth": "80px",
            "sDefaultContent": ""
        },
        {
            "data": "type",
            "visible": false,
            "sName": "tec.type",
            "title": doc.getDocOnline("invariant", "Type"),
            "sWidth": "100px",
            "sDefaultContent": ""
        },
        {
            "data": "version",
            "visible": false,
            "sName": "tec.version",
            "title": doc.getDocOnline("testcase", "version"),
            "sWidth": "50px",
            "sDefaultContent": ""
        },
        {
            "data": "implementer",
            "visible": false,
            "sName": "tec.implementer",
            "title": doc.getDocOnline("testcase", "Implementer"),
            "sWidth": "50px",
            "sDefaultContent": ""
        },
        {
            "data": "executor",
            "visible": false,
            "sName": "tec.executor",
            "title": doc.getDocOnline("testcase", "Executor"),
            "sWidth": "50px",
            "sDefaultContent": ""
        },
        {
            "data": "dateCreated",
            "visible": false,
            "sName": "tec.dateCreated",
            "like": true,
            "title": doc.getDocOnline("transversal", "DateCreated"),
            "sWidth": "150px",
            "sDefaultContent": ""
        },
        {
            "data": "usrCreated",
            "visible": false,
            "sName": "tec.usrCreated",
            "title": doc.getDocOnline("transversal", "UsrCreated"),
            "sWidth": "100px",
            "sDefaultContent": ""
        },
        {
            "data": "dateModif",
            "visible": false,
            "like": true,
            "sName": "tec.dateModif",
            "title": doc.getDocOnline("transversal", "DateModif"),
            "sWidth": "150px",
            "sDefaultContent": "",
            "mRender": function(data, type, oObj) {
                return getDate(oObj["dateModif"]);
            }
        },
        {
            "data": "usrModif",
            "visible": false,
            "sName": "tec.usrModif",
            "title": doc.getDocOnline("transversal", "UsrModif"),
            "sWidth": "100px",
            "sDefaultContent": ""
        }
    ];

    for (var index = 0; index < countryLen; index++) {
        var country = countries[index].value;

        var column = {
            "data": function(row, type, val, meta) {
                var dataTitle = meta.settings.aoColumns[meta.col].sTitle;

                if (row.hasPermissionsUpdate) {
                    if (row.hasOwnProperty("countries") && row["countries"].some(item => item.country === dataTitle)) {
                        return '<input type="checkbox" name="' + dataTitle + '" data-test="' + row.test + '" data-testcase="' + row.testCase + '" onchange="setCountry(this);" checked/>';
                    } else {
                        return '<input type="checkbox" name="' + dataTitle + '" data-test="' + row.test + '" data-testcase="' + row.testCase + '" onchange="setCountry(this);"/>';
                    }
                } else {
                    if (row.hasOwnProperty("countries") && row["countries"].some(item => item.country === dataTitle)) {
                        return '<input type="checkbox" checked disabled/>';
                    } else {
                        return '<input type="checkbox" disabled/>';
                    }
                }
            },
            "bSortable": false,
            "visible": false,
            "bSearchable": false,
            "sClass": "center",
            "title": country,
            "sDefaultContent": "",
            "sWidth": "50px"
        };

        aoColumns.push(column);
    }
    return aoColumns;
}

