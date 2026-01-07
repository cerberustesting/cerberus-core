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
/* global modalFormCleaner */

var testAutomaticModal = "";

$.when($.getScript("js/global/global.js")).then(function () {
    $(document).ready(function () {
        initPage();
    });
});

function initPage() {
    displayPageLabel();
    var table = loadTable();


    initModalTestCase();
    $('#editTestCaseModal').data("initLabel", true);

    $('[data-toggle="popover"]').popover({
        'placement': 'auto',
        'container': 'body'}
    );

    var availableUsers = getUserArray(true);
    $("input#massExecutor").autocomplete({
        source: availableUsers,
        messages: {
            noResults: '',
            results: function (amount) {
                return '';
            }
        }
    });

}


function displayPageLabel() {
    var doc = new Doc();

    //displayHeaderLabel(doc);
    displayGlobalLabel(doc);
    $("#testCaseListLabel").html(doc.getDocOnline("page_testcaselist", "testcaselist"));
    $("#pageTitle").html(doc.getDocOnline("page_testcaselist", "title"));
    $("#title").html(doc.getDocOnline("page_testcaselist", "title"));
    displayFooter(doc);

}

function loadTable(selectTest, sortColumn) {

    //clear the old report content before reloading it
    $("#testCaseList").empty();
    $("#testCaseList").html('<table id="testCaseTable" class="table table-hover display" name="testCaseTable">\n\
                                            </table><div class="marginBottom20"></div>');

    var contentUrl = "ReadTestCase";

    //configure and create the dataTable
    var jqxhr = $.getJSON("FindInvariantByID", "idName=COUNTRY");

    $.when(jqxhr).then(function (data) {
        sortColumn = 2;

        var lengthMenu = [10, 15, 20, 30, 50, 100, 500, 1000];
        var config = new TableConfigurationsServerSide("testCaseTable", contentUrl, "contentTable", aoColumnsFunc(data, "testCaseTable"), [2, 'asc'], lengthMenu);

        var table = createDataTableWithPermissionsNew(config, renderOptionsForTestCaseList, "#testCaseList", undefined, true);


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

    // Toujours créer les boutons
    if ($("#createTestCaseButton").length === 0) {
        var disabledCreate = data["hasPermissionsCreate"] ? "" : "disabled";

        var contentToAdd = "";

        // Bouton Create
        contentToAdd += `
            <button id='createTestCaseButton' type='button'
                class='bg-sky-400 hover:bg-sky-500 flex items-center space-x-1 px-3 py-1 rounded-lg h-10 w-auto'
                ${disabledCreate}>
                <span class='glyphicon glyphicon-plus-sign'></span>
                <span>${doc.getDocLabel("page_testcaselist", "btn_create")}</span>
            </button>
        `;

        // Bouton Import
        contentToAdd += `
            <button id='importTestCaseButton' type='button'
                class='flex items-center gap-2 px-3 py-2 rounded-lg border border-gray-300 dark:border-gray-600 hover:border-sky-500 h-10'>
                <span class='glyphicon glyphicon-import'></span>
                <span>${doc.getDocLabel("page_testcaselist", "btn_import")}</span>
            </button>
        `;

        // Bouton Export
        contentToAdd += `
                        <button id='exportTestCaseButton' type='button'
                            class='flex items-center gap-2 px-3 py-2 rounded-lg border border-gray-300 dark:border-gray-600 hover:border-sky-500 h-10' style="display:none;">
                            <span class='glyphicon glyphicon-export'></span>
                            <span>${doc.getDocLabel("page_testcaselist", "btn_export")}</span>
                        </button>`;

        // Bouton Mass Update
                contentToAdd += `
            <button id='massUpdateTestCaseButton' type='button'
                class='flex items-center gap-2 px-3 py-2 rounded-lg border border-gray-300 dark:border-gray-600 hover:border-sky-500 h-10'
                style="display:none;">
                <span class='glyphicon glyphicon-edit'></span>
                <span>Update</span>
            </button>
        `;

        // Bouton Mass Label
                contentToAdd += `
            <button id='massLabelTestCaseButton' type='button'
                class='flex items-center gap-2 px-3 py-2 rounded-lg border border-gray-300 dark:border-gray-600 hover:border-sky-500 h-10'
                style="display:none;">
                <span class='glyphicon glyphicon-tags'></span>
                <span>Label</span>
            </button>
        `;

        // Bouton Mass Delete
                contentToAdd += `
            <button id='massDeleteTestCaseButton' type='button'
                class='flex items-center gap-2 px-3 py-2 rounded-lg border border-gray-300 dark:border-gray-600 hover:border-red-400 h-10'
                style="display:none;">
                <span class='glyphicon glyphicon-trash'></span>
                <span>Delete</span>
            </button>
        `;



        // Cherche ton buttonWrapper
        var $wrapper = $("#testCaseTable_buttonWrapper");

        if ($wrapper.length) {
            // Ajoute le bouton au **début** du wrapper
            $wrapper.append(contentToAdd);
        } else {
            // fallback si le wrapper n’existe pas encore
            console.warn("Wrapper #testCaseTable_buttonWrapper introuvable, insertion avant length");
            $("#testCaseTable_wrapper div#testCaseTable_length").before("<div id='testCaseTable_buttonWrapper' class='flex w-full gap-2'>" + contentToAdd + "</div>");
        }

        // Bind des events seulement si le bouton n'est pas disabled
        if (data["hasPermissionsCreate"]) {
            $("#createTestCaseButton").off("click").on("click", function () {
                var firstRowTest = $("#testCaseTable td.sorting_1")[0] !== undefined ? testAutomaticModal : GetURLParameter("test", undefined);
                //openModalTestCase(firstRowTest, undefined, "ADD");
                window.dispatchEvent(new CustomEvent('testcase-modal-open', { detail: { defaultTest: firstRowTest } }));
            });
        }

        $("#testCaseTable").on("change", "input[type=checkbox]", function() {
            const anyChecked = $("#testCaseTable input[type=checkbox]:checked").length > 0;
            $("#exportTestCaseButton, #createBrpMassButton").toggle(anyChecked);
            $("#exportTestCaseButton, #massUpdateTestCaseButton").toggle(anyChecked);
            $("#exportTestCaseButton, #massLabelTestCaseButton").toggle(anyChecked);
            $("#exportTestCaseButton, #massDeleteTestCaseButton").toggle(anyChecked);
        });
        $("#massUpdateTestCaseButton").off("click").on("click", function () {
            window.dispatchEvent(new CustomEvent('mass-update-open'));
        });

        $("#massLabelTestCaseButton").off("click").on("click", function () {
            window.dispatchEvent(new CustomEvent('mass-label-open'));
        });

        $("#massDeleteTestCaseButton").off("click").on("click", function () {
            deleteMassTestCases();
        });

        $("#importTestCaseButton").off("click").on("click", function () {
            window.dispatchEvent(new CustomEvent('open-import-recording'));
        });


        $("#exportTestCaseButton").off("click").on("click", exportTestCasesMenuClick);
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
    $.when(jqxhr).then(function (data) {
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
    $.when(jqxhr).then(function (data) {
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
    $.when(jqxhr).then(function (data) {
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
    $.when(jqxhr).then(function (data) {
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

function exportTestCasesMenuClick() {
    var doc = new Doc();
    clearResponseMessageMainPage();

    // When creating a new item, Define here the default value.
    var formList = $('#massActionForm');
    if (formList.serialize().indexOf("test-") === -1) {
        notifyInPage("error", doc.getDocLabel("page_global", "message_exportActionError"));
    } else {
        $("input[data-line=select]:checked").each(function () {

            const $checkbox = $(this);
            const params = $(this).prop("name")
                .replace(/test-/g, 'test=')
                .replace(/testcase-/g, '&testcase=');

            const url = "./ExportTestCase?" + params;

            const link = document.createElement("a");
            link.href = url;
            link.style.display = "none";
            link.download = ""; // important

            document.body.appendChild(link);
            link.click();
            link.remove();

            $checkbox.prop("checked", false);
        });
        notifyInPage("success", "Export OK")
        //$('#testCaseTable').DataTable().draw();
        $("#selectAll").prop("checked", false);
    }

}

/**
 * Delete Mass TestCases
 * @returns {Promise<void>}
 */
async function deleteMassTestCases() {

    try {
        const doc = new Doc();

        let messageHtml = doc.getDocLabel("page_testcase", "message_delete_all") + "<br><br>";
        let count = 0;

        $("input[data-line=select]:checked").each(function (index, file) {
            const t = $(file).prop("name")
                .replace(/test-/g, 'test=')
                .replace(/testcase-/g, '&testcase=');

            const test = t.split("test=")[1].split("&testcase=")[0];
            const testcase = t.split("test=")[1].split("&testcase=")[1];

            messageHtml += `${index + 1} : ${test} - ${testcase}<br>`;
            count++;
        });

        if (count === 0) {
            notifyInPage("warning", doc.getDocLabel("page_global", "message_noselection"));
            return;
        }

        const result = await Swal.fire({
            title: 'This action cannot be reverted',
            html: messageHtml,
            icon: 'warning',
            showCancelButton: true,
            confirmButtonText: doc.getDocLabel("page_global", "btn_delete"),
            cancelButtonText: doc.getDocLabel("page_global", "buttonClose"),
            confirmButtonColor: '#dc2626',
            background: 'var(--crb-new-bg)',
            color: 'var(--crb-black-color)',
            showLoaderOnConfirm: true,
            allowOutsideClick: () => !Swal.isLoading(),

            preConfirm: async () => {
                const response = await deleteAllSelectedTestCase();
                if (response.messageType !== 'success') {
                    Swal.showValidationMessage(response.message);
                }
                return response;
            }
        });

        if (result.isConfirmed && result.value?.messageType === 'success') {

            const table = $('#testCaseTable').DataTable();
            table.draw(false);

            if (table.data().length === 0) {
                table.page('previous').draw(false);
            }

            notifyInPage("success","TestCases successfully deleted");
        }

    } catch (e) {
        console.error(e);
        notifyInPage('error', `Unexpected error deleting test cases`);
    }
}

/**
 * Delete All Testcases selected
 * @returns {Promise<{messageType: string, message: string}|{messageType: string, message: string}|{messageType: string, message: string}>}
 */
async function deleteAllSelectedTestCase() {

    const $checked = $("input[data-line=select]:checked");

    if ($checked.length === 0) {
        return {
            messageType: 'error',
            message: 'No test case selected'
        };
    }

    const deletions = [];
    let hasError = false;
    let errorMessage = '';

    $checked.each(function () {

        const t = $(this).prop("name")
            .replace(/test-/g, 'test=')
            .replace(/testcase-/g, '&testCase=');

        const url = "DeleteTestCase?" + t;

        deletions.push(
            fetch(url, { method: "GET" })
                .then(resp => resp.json())
                .then(data => {
                    if (getAlertType(data.messageType) !== "success") {
                        hasError = true;
                        errorMessage = data.message || 'Delete failed';
                    }
                })
                .catch(() => {
                    hasError = true;
                    errorMessage = 'Delete failed';
                })
        );
    });

    await Promise.all(deletions);

    return hasError ? { messageType: 'error', message: errorMessage } : { messageType: 'success', message: 'Delete OK' };
}


function importTestCasesMenuClick() {
    $("#importTestCaseButton").off("click");

    var fileInput = document.getElementById('files');
    fileInput.addEventListener('change', function (evnt) {
        fileList = [];
        for (var i = 0; i < fileInput.files.length; i++) {
            fileList.push(fileInput.files[i]);
        }
        renderFileList(fileList, 'file-list-display');
    });

    $("#importTestCaseButton").click(function () {
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
        success: function (data) {
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
function renderFileList(fileList, elementId) {
    var fileListDisplay = document.getElementById(elementId);
    fileListDisplay.innerHTML = '';
    fileList.forEach(function (file, index) {
        var fileDisplayEl = document.createElement('p');
        fileDisplayEl.innerHTML = (index + 1) + ': ' + file.name;
        fileListDisplay.appendChild(fileDisplayEl);
    });
}

function importTestCasesFromSIDEMenuClick() {
    $("#importTestCaseFromSIDEButton").off("click");

    var fileInput = document.getElementById('filesSIDE');
    fileInput.addEventListener('change', function (evnt) {
        fileList = [];
        for (var i = 0; i < fileInput.files.length; i++) {
            fileList.push(fileInput.files[i]);
        }
        renderFileList(fileList, 'fileside-list-display');
    });

    $("#importTestCaseFromSIDEButton").click(function () {
        confirmImportTestCaseFromSIDEModalHandler();
    });

    var doc = new Doc();
    var text = doc.getDocLabel("page_testcaselist", "import_testcase_msg");
    $('#importTestCaseModalText').text(text);

    $('#importTestCaseFromSIDEModalForm #targetTest').empty();
    $('#importTestCaseFromSIDEModalForm #targetTest').select2(getComboConfigTest());

    $('#importTestCaseFromSIDEModalForm #targetApplication').empty();
    $('#importTestCaseFromSIDEModalForm #targetApplication').select2(getComboConfigApplication(false));

    $('#importTestCaseFromSIDEModal').modal('show');
}

function importTestCasesFromTestLinkMenuClick() {
    $("#importTestCaseFromTestLinkButton").off("click");

    var fileInput = document.getElementById('filesTestLink');
    fileInput.addEventListener('change', function (evnt) {
        fileList = [];
        for (var i = 0; i < fileInput.files.length; i++) {
            fileList.push(fileInput.files[i]);
        }
        renderFileList(fileList, 'filetestlink-list-display');
    });

    $("#importTestCaseFromTestLinkButton").click(function () {
        confirmImportTestCaseFromTestLinkModalHandler();
    });

    var doc = new Doc();
    var text = doc.getDocLabel("page_testcaselist", "import_testcase_msg");
    $('#importTestCaseModalText').text(text);

    $('#importTestCaseFromTestLinkModalForm #targetTest').empty();
    $('#importTestCaseFromTestLinkModalForm #targetTest').select2(getComboConfigTest());

    $('#importTestCaseFromTestLinkModalForm #targetApplication').empty();
    $('#importTestCaseFromTestLinkModalForm #targetApplication').select2(getComboConfigApplication(false));

    $('#importTestCaseFromTestLinkModal').modal('show');
}

function renderFileList(fileList, elementId) {
    var fileListDisplay = document.getElementById(elementId);
    fileListDisplay.innerHTML = '';
    fileList.forEach(function (file, index) {
        var fileDisplayEl = document.createElement('p');
        fileDisplayEl.innerHTML = (index + 1) + ': ' + file.name;
        fileListDisplay.appendChild(fileDisplayEl);
    });
}

function confirmImportTestCaseFromSIDEModalHandler() {
    clearResponseMessage($('#importTestCaseModal'));

    var formEdit = $('#importTestCaseFromSIDEModal #importTestCaseFromSIDEModalForm');

    var sa = formEdit.serializeArray();
    var formData = new FormData();

    for (var i in sa) {
        formData.append(sa[i].name, sa[i].value);
    }

    var file = $("#importTestCaseFromSIDEModal input[type=file]");
    for (var i = 0; i < $($(file).get(0)).prop("files").length; i++) {
        formData.append("file", file.prop("files")[i]);
    }

    // Calculate servlet name to call.
    var myServlet = "ImportTestCaseFromSIDE";

    // Get the header data from the form.
    showLoaderInModal('#importTestCaseFromSIDEModal');

    $.ajax({
        url: myServlet,
        async: true,
        method: "POST",
        data: formData,
        processData: false,
        contentType: false,
        success: function (data) {
            data = JSON.parse(data);
            if (getAlertType(data.messageType) === "success") {
                var oTable = $("#testCaseTable").dataTable();
                oTable.fnDraw(false);
                $('#importTestCaseFromSIDEModal').modal('hide');
                showMessage(data);
            } else {
                showMessage(data, $('#importTestCaseFromSIDEModal'));
            }
            hideLoaderInModal('#importTestCaseFromSIDEModal');
        },
        error: showUnexpectedError
    });
}

function confirmImportTestCaseFromTestLinkModalHandler() {
    clearResponseMessage($('#importTestCaseModal'));

    var formEdit = $('#importTestCaseFromTestLinkModal #importTestCaseFromTestLinkModalForm');

    var sa = formEdit.serializeArray();
    var formData = new FormData();

    for (var i in sa) {
        formData.append(sa[i].name, sa[i].value);
    }

    var file = $("#importTestCaseFromTestLinkModal input[type=file]");
    for (var i = 0; i < $($(file).get(0)).prop("files").length; i++) {
        formData.append("file", file.prop("files")[i]);
    }

    // Calculate servlet name to call.
    var myServlet = "ImportTestCaseFromTestLink";

    // Get the header data from the form.
    showLoaderInModal('#importTestCaseFromTestLinkModal');

    $.ajax({
        url: myServlet,
        async: true,
        method: "POST",
        data: formData,
        processData: false,
        contentType: false,
        success: function (data) {
            data = JSON.parse(data);
            if (getAlertType(data.messageType) === "success") {
                var oTable = $("#testCaseTable").dataTable();
                oTable.fnDraw(false);
                $('#importTestCaseFromTestLinkModal').modal('hide');
                showMessage(data);
            } else {
                showMessage(data, $('#importTestCaseFromTestLinkModal'));
            }
            hideLoaderInModal('#importTestCaseFromTestLinkModal');
        },
        error: showUnexpectedError
    });
}


function setActive(checkbox) {
    var test = checkbox.dataset.test;
    var testcase = checkbox.name;
    var active;

    if (checkbox.checked === true) {
        active = true;
    } else {
        active = false;
    }

    $.ajax({
        url: "UpdateTestCase",
        method: "POST",
        data: {test: test, testcase: testcase, originalTest: test, originalTestcase: testcase, isActive: active},
        dataType: "json",
        success: function (data) {
            if (active) {
                $('[id="runTest' + encodeURIComponent(test) + encodeURIComponent(testcase) + '"]').removeAttr("disabled");
            } else {
                $('[id="runTest' + encodeURIComponent(test) + encodeURIComponent(testcase) + '"]').attr("disabled", "disabled");
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
            success: function (data) {
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
            success: function (data) {
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

function filterOnField(element) {
    var newTest = $(element).get(0).textContent.trim();
    var colIndex = $(element).parent().get(0).cellIndex;
    $("#testCaseTable").dataTable().fnFilter(newTest, colIndex);
}

function aoColumnsFunc(countries, tableId) {
    var doc = new Doc();

    var countryLen = countries.length;
    var aoColumns = [
        {"data": null,
            "title": '<input id="selectAll" title="' + doc.getDocLabel("page_global", "tooltip_massAction") + '" type="checkbox" class="appearance-none w-5 h-5 border border-sky-500 rounded checked:bg-sky-500 checked:sky-blue-500 bg-transparent cursor-pointer"></input>',
            "bSortable": false,
            "sWidth": "30px",
            "bSearchable": false,
            "mRender": function (data, type, obj) {
                var selectBrp = `
  <label class="select-brp inline-flex items-center cursor-pointer">
    <!-- checkbox masqué mais cliquable car dans le label -->
    <input 
      id="selectLine_${obj['test']}${obj['testcase']}"
      type="checkbox"
      name="test-${obj["test"]}testcase-${obj["testcase"]}"
      data-line="select"
      data-id="${obj["test"]}${obj["testcase"]}"
      title="${doc.getDocLabel("page_global", "tooltip_massActionLine")}"
    >
    <!-- case custom -->
    <span class="w-5 h-5 border border-sky-500 rounded flex items-center justify-center transition-colors">
      <svg class="w-3 h-3 text-white" 
           xmlns="http://www.w3.org/2000/svg" 
           viewBox="0 0 20 20" 
           fill="currentColor">
        <path fill-rule="evenodd" 
              d="M16.707 5.293a1 1 0 010 1.414L9 14.414 
                 5.293 10.707a1 1 0 011.414-1.414L9 11.586 
                 l6.293-6.293a1 1 0 011.414 0z" 
              clip-rule="evenodd"/>
      </svg>
    </span>
  </label>
`;
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
            "sWidth": "30px",
            "render": function (data, type, obj, meta) {
                var newTest = escapeHtml(obj["test"]);
                var newTestCase = escapeHtml(obj["testcase"]);
                var row = "row_" + meta.row;

                return `
        <div x-data="{ open: false, pos: {top: 0, left: 0}, timer: null, row: '${row}' }" class="inline-block">
            
            <!-- Bouton "…" -->
            <button @mouseenter="
                        clearTimeout(timer);
                        open = true;
                        const rect = $el.getBoundingClientRect();
                        pos = { 
                            top: rect.top + window.scrollY, 
                            left: rect.right + window.scrollX 
                        };
                    "
                    @mouseleave="timer = setTimeout(() => open = false, 200)"
                    :id="'testcase_action_' + row"
                    class="p-1 rounded hover:bg-gray-200 dark:hover:bg-gray-700">
                <i data-lucide="ellipsis" class="w-4 h-4"></i>
            </button>

            <!-- Tooltip via teleport -->
            <template x-teleport="body">
                <div x-show="open"
                     x-transition
                     @mouseenter="clearTimeout(timer); open=true"
                     @mouseleave="open=false"
                     x-init="$nextTick(() => { if (window.lucide) lucide.createIcons(); })"
                     class="absolute z-50 w-60 bg-slate-50 dark:bg-slate-900 border dark:text-slate-50 text-slate-900 border-gray-200 dark:border-gray-700 rounded-lg shadow-lg"
                     :style="'top:'+(pos.top)+'px; left:'+(pos.left - $el.offsetWidth)+'px;'">

                    <div class="px-3 py-2 flex items-center gap-2 
                         ${data.hasPermissionsUpdate ? 'hover:bg-gray-100 dark:hover:bg-gray-800 cursor-pointer' : 'opacity-50 cursor-not-allowed'}"
                         id="testcase_action_editheader_${row}"
                         onclick="openModalTestCase('${newTest}','${newTestCase}','${data.hasPermissionsUpdate ? 'EDIT' : 'VIEW'}')">
                        <i data-lucide="${data.hasPermissionsUpdate ? 'pencil' : 'eye'}" class="w-4 h-4"></i>
                        ${doc.getDocLabel("page_testcaselist", data.hasPermissionsUpdate ? "btn_edit" : "btn_view")}
                    </div>

                    <div class="px-3 py-2 flex items-center gap-2 hover:bg-gray-100 dark:hover:bg-gray-800 cursor-pointer"
                         id="testcase_action_duplicate_${row}"
                         onclick="openModalTestCase('${newTest}','${newTestCase}','DUPLICATE')">
                        <i data-lucide="copy" class="w-4 h-4"></i>
                        ${doc.getDocLabel("page_testcaselist", "btn_duplicate")}
                    </div>

                    <div class="px-3 py-2 flex items-center gap-2 
                        ${data.hasPermissionsUpdate ? 'hover:bg-gray-100 dark:hover:bg-gray-800 cursor-pointer' : 'opacity-50 cursor-not-allowed'}"
                        id="testcase_action_export_${row}"
                        ${data.hasPermissionsUpdate ? `onclick="window.location.href='./ExportTestCase?test=${encodeURIComponent(obj['test'])}&testcase=${encodeURIComponent(obj['testcase'])}'"` : ''}>
                        <i data-lucide="download" class="w-4 h-4"></i>
                        ${doc.getDocLabel("page_testcaselist", "btn_export")}
                    </div>

                    <div class="px-3 py-2 flex items-center gap-2 
                        ${data.hasPermissionsDelete ? 'hover:bg-gray-100 text-red-600 dark:hover:bg-gray-800 cursor-pointer' : 'opacity-50 cursor-not-allowed'}"
                         id="testcase_action_delete_${row}"
                         ${data.hasPermissionsDelete ? `onclick="deleteEntryClick('${newTest}','${newTestCase}')"` : ''}>
                        <i data-lucide="trash-2" class="w-4 h-4"></i>
                        ${doc.getDocLabel("page_testcaselist", "btn_delete")}
                    </div>

                    <div class="px-3 py-2 flex items-center gap-2 hover:bg-gray-100 dark:hover:bg-gray-800 cursor-pointer"
                         :id="'testcase_action_runtest_' + row"
                         onclick="openModalExecutionSimple('${data.application}','${newTest}','${newTestCase}','${data.description}')">
                        <i data-lucide="play" class="w-4 h-4"></i>
                        ${doc.getDocLabel("page_testcaselist", "btn_runTest")}
                    </div>

                    <div class="px-3 py-2 flex items-center gap-2 hover:bg-gray-100 dark:hover:bg-gray-800 cursor-pointer"
                        :id="'testcase_action_editscript_' + row"
                        onclick="window.location.href='./TestCaseScript.jsp?test=${encodeURIComponent(obj['test'])}&testcase=${encodeURIComponent(obj['testcase'])}'">
                        <i data-lucide="file-text" class="w-4 h-4"></i>
                        ${doc.getDocLabel("page_testcaselist", "btn_editScript")}
                    </div>

                </div>
            </template>
        </div>`;
            }
        }
        ,
        {
            "data": "test",
            "sName": "tec.test",
            "title": doc.getDocOnline("test", "Test"),
            "sWidth": "120px",
            "sDefaultContent": "",
            "mRender": function (data, type, oObj, full) {

                if (!data) {
                    return '';
                }

                // Exemple : mémorisation sur la première ligne
                if (full.row === 0) {
                    testAutomaticModal = oObj.test;
                }

                return `
                    <button
                        type="button"
                        onclick="filterOnField(this)"
                        class="inline-flex items-center px-3 py-1
                            border border-slate-300 dark:border-slate-600 rounded-xl
                            text-sm font-medium text-slate-700 dark:text-slate-200
                            hover:border-blue-500 hover:text-blue-600
                            transition cursor-pointer bg-transparent"
                        title="Open test ${oObj.test}"
                    >
                        ${oObj.test}
                    </button>
                `;
            }
        },
        {
            "data": "testcase",
            "sName": "tec.testcase",
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
            "render": function (data, type, full, meta) {
                var labelValue = '';
                $.each(data, function (i, e) {
                    labelValue += '<div class="mt-2" style="float:left"><span class="px-3 py-1 rounded-xl text-xs font-medium" onclick="filterOnLabel(this)" style="cursor:pointer;background-color:' + e.color + ';color:' + e.fontColor + '" data-toggle="tooltip" title="' + e.description + '">' + e.label + '</span></div> ';
                });
                return labelValue;
            }
        },
        {
            "data": "labels",
            "sName": "lab.labelsSTICKER",
            "title": doc.getDocOnline("label", "labelsSTICKER"),
            "bSortable": false,
            "sWidth": "170px",
            "sDefaultContent": "",
            "render": function (data, type, full, meta) {
                var labelValue = '';
                $.each(data, function (i, e) {
                    if (e.type === "STICKER") {
                        labelValue += '<div class="mt-2" style="float:left"><span class="px-3 py-1 rounded-xl text-xs font-medium" onclick="filterOnLabel(this)" style="cursor:pointer;background-color:' + e.color + ';color:' + e.fontColor + '" data-toggle="tooltip" title="' + e.description + '">' + e.label + '</span></div> ';
                    }
                });
                return labelValue;
            }
        },
        {
            "data": "labels",
            "visible": false,
            "sName": "lab.labelsREQUIREMENT",
            "title": doc.getDocOnline("label", "labelsREQUIREMENT"),
            "bSortable": false,
            "sWidth": "170px",
            "sDefaultContent": "",
            "render": function (data, type, full, meta) {
                var labelValue = '';
                $.each(data, function (i, e) {
                    if (e.type === "REQUIREMENT") {
                        labelValue += '<div class="mt-2" style="float:left"><span class="px-3 py-1 rounded-xl text-xs font-medium" onclick="filterOnLabel(this)" style="cursor:pointer;background-color:' + e.color + ';color:' + e.fontColor + '" data-toggle="tooltip" title="' + e.description + '">' + e.label + '</span></div> ';
                    }
                });
                return labelValue;
            }
        },
        {
            "data": "labels",
            "visible": false,
            "sName": "lab.labelsBATTERY",
            "title": doc.getDocOnline("label", "labelsBATTERY"),
            "bSortable": false,
            "sWidth": "170px",
            "sDefaultContent": "",
            "render": function (data, type, full, meta) {
                var labelValue = '';
                $.each(data, function (i, e) {
                    if (e.type === "BATTERY") {
                        labelValue += '<div class="mt-2" style="float:left"><span class="px-3 py-1 rounded-xl text-xs font-medium" onclick="filterOnLabel(this)" style="cursor:pointer;background-color:' + e.color + ';color:' + e.fontColor + '" data-toggle="tooltip" title="' + e.description + '">' + e.label + '</span></div> ';
                    }
                });
                return labelValue;
            }
        },
        {
            "data": "status",
            "sName": "tec.status",
            "title": doc.getDocOnline("testcase", "Status"),
            "sWidth": "100px",
            "sDefaultContent": "",
            "mRender": function (data, type, oObj, full) {

                if (!data) {
                    return '';
                }

                return `
                    <button
                        type="button"
                        onclick="filterOnField(this)"
                        class="inline-flex items-center px-3 py-1
                            border border-slate-300 dark:border-slate-600 rounded-xl
                            text-sm font-medium text-slate-700 dark:text-slate-200
                            hover:border-blue-500 hover:text-blue-600
                            transition cursor-pointer bg-transparent"
                        title="Open test ${oObj.status}"
                    >
                        ${oObj.status}
                    </button>
                `;
            }
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
            "title": doc.getDocOnline("testcase", "IsActive"),
            "sDefaultContent": "",
            "sWidth": "70px",
            "className": "center",
            "mRender": function (data, type, obj) {
                if (obj.hasPermissionsUpdate) {
                    if (data) {
                        return '<input type="checkbox" name="' + obj["testcase"] + '" data-test="' + obj.test + '" onchange="setActive(this);" checked/>';
                    } else {
                        $('[id="runTest' + encodeURIComponent(obj["test"]) + encodeURIComponent(obj["testcase"]) + '"]').attr("disabled", "disabled");
                        return '<input type="checkbox" name="' + obj["testcase"] + '" data-test="' + obj.test + '" onchange="setActive(this);" />';
                    }
                } else {
                    if (data) {
                        return '<input type="checkbox" checked disabled />';
                    } else {
                        $('[id="runTest' + encodeURIComponent(obj["test"]) + encodeURIComponent(obj["testcase"]) + '"]').attr("disabled", "disabled");
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
            "data": "isMuted",
            "visible": false,
            "sName": "tec.isMuted",
            "title": doc.getDocOnline("testcase", "IsMuted"),
            "sWidth": "20px",
            "mRender": function (data, type, obj) {
                if (obj.isMuted) {
                    return '<span class="glyphicon glyphicon-volume-off" aria-hidden="true"></span>';
                }
                return "";
            },
            "sDefaultContent": ""
        },
        {
            "data": "type",
            "visible": false,
            "sName": "tec.type",
            "title": doc.getDocOnline("invariant", "TESTCASE_TYPE"),
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
            "data": "origine",
            "visible": false,
            "sName": "tec.origine",
            "like": false,
            "title": doc.getDocOnline("testcase", "Origin"),
            "sWidth": "50px",
            "sDefaultContent": ""
        },
        {
            "data": "refOrigine",
            "visible": false,
            "sName": "tec.refOrigine",
            "like": true,
            "title": doc.getDocOnline("testcase", "RefOrigin"),
            "sWidth": "150px",
            "sDefaultContent": ""
        },
        {
            "data": "dateCreated",
            "visible": false,
            "sName": "tec.dateCreated",
            "like": true,
            "title": doc.getDocOnline("transversal", "DateCreated"),
            "sWidth": "150px",
            "sDefaultContent": "",
            "mRender": function (data, type, oObj) {
                return getDate(oObj["dateCreated"]);
            }
        },
        {
            "data": "dateLastExecuted",
            "visible": false,
            "sName": "tec.dateLastExecuted",
            "like": true,
            "title": doc.getDocOnline("testcase", "DateLastExecuted"),
            "sWidth": "150px",
            "sDefaultContent": "",
            "mRender": function (data, type, oObj) {
                return getDate(oObj["dateLastExecuted"]);
            }
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
            "mRender": function (data, type, oObj) {
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
            "data": function (row, type, val, meta) {
                var dataTitle = meta.settings.aoColumns[meta.col].sTitle;

                if (row.hasPermissionsUpdate) {
                    if (row.hasOwnProperty("countries") && row["countries"].some(item => item.value === dataTitle)) {
                        return '<input type="checkbox" name="' + dataTitle + '" data-test="' + row.test + '" data-testcase="' + row.testcase + '" onchange="setCountry(this);" checked/>';
                    } else {
                        return '<input type="checkbox" name="' + dataTitle + '" data-test="' + row.test + '" data-testcase="' + row.testcase + '" onchange="setCountry(this);"/>';
                    }
                } else {
                    if (row.hasOwnProperty("countries") && row["countries"].some(item => item.value === dataTitle)) {
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

