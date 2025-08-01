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

/***
 * Open the modal.
 * @returns {null}
 */
function openModalTestCaseSimple(defaultTest) {

    // We only load the Labels and bind the events once for performance optimisations.
    if ($('#editTestCaseSimpleCreationModal').data("initLabel") === undefined) {
        initModalTestCaseSimpleCreation();
        $('#editTestCaseSimpleCreationModal').data("initLabel", true);
    }

    // Init the Saved data to false.
    $('#editTestCaseSimpleCreationModal').data("Saved", false);
    $('#editTestCaseSimpleCreationModal').data("testcasesimplecreation", undefined);

    //Add event on Save button.
    $("#addTestCaseSimpleCreationButton").off("click");
    $("#addTestCaseSimpleCreationButton").click(function () {
        if (checkFormSimpleCreationBeforeSubmit()===true) {
            submitSimpleCreationForm();
        }
    });
    $('#addTestCaseSimpleCreationButton').attr('class', 'btn btn-primary');
    $('#addTestCaseSimpleCreationButton').removeProp('hidden');

    $('#editTestCaseSimpleCreationTestFolderId').change(function () {
        SimpleCreationFeedTestCaseField("editTestCaseSimpleCreationModalForm");
    });

    //Feed the modal
    feedNewTestCaseSimpleCreationModal("editTestCaseSimpleCreationModal", defaultTest);

    //Clean response messages
    $('#editTestCaseSimpleCreationApplication').parents("div.form-group").removeClass("has-error");
    clearResponseMessage($('#editTestCaseSimpleCreationModal'));
}

//Init Modal label & combo
function initModalTestCaseSimpleCreation() {
    var doc = new Doc();

    $("#editTestCaseSimpleCreationModalForm [name='testField']").html(doc.getDocLabel("test", "Test"));
    $("#editTestCaseSimpleCreationModalForm [name='testCaseField']").html(doc.getDocLabel("testcase", "TestCase"));
    $("#editTestCaseSimpleCreationModalForm [name='testCaseDescriptionField']").html(doc.getDocLabel("testcase", "Description"));
    $("#editTestCaseSimpleCreationModalForm [name='applicationField']").html(doc.getDocLabel("application", "Application"));
    $("#editTestCaseSimpleCreationModalForm [name='applicationNameField']").html(doc.getDocLabel("application", "Application"));
    $("#editTestCaseSimpleCreationModalForm [name='applicationTypeField']").html(doc.getDocLabel("application", "type"));
    $("#editTestCaseSimpleCreationModalForm [name='applicationHostField']").html(doc.getDocLabel("page_testcasecreate", "applicationGuiHost"));
    $("#editTestCaseSimpleCreationModalForm [name='countryField']").html(doc.getDocLabel("testcase", "countriesLabel"));
    $("#editTestCaseSimpleCreationModalForm [name='environmentField']").html(doc.getDocLabel("invariant", "ENVIRONMENT"));

    $("#editTestCaseSimpleCreationModalForm #ChooseApplicationLabel").html('<span class="card-img-top glyphicon glyphicon-modal-window" style="font-size:15px;"></span>  '+doc.getDocLabel("page_testcasecreate", "chooseOrCreateApplicationLabel"));
    $("#editTestCaseSimpleCreationModalForm #describeTestcaseLabel").html('<span class="card-img-top glyphicon glyphicon-edit" style="font-size:15px;"></span>  '+doc.getDocLabel("page_testcasecreate", "describeTestCaseLabel"));
    $("#editTestCaseSimpleCreationModalForm #defineTestcaseLabel").html('<span class="card-img-top glyphicon glyphicon-folder-open" style="font-size:15px;"></span>   '+doc.getDocLabel("page_testcasecreate", "chooseOrCreateFolderLabel"));

    displayInvariantList("editTestCaseSimpleCreationApplicationType", "APPLITYPE", false);
    displayInvariantList("editTestCaseSimpleCreationCountry", "COUNTRY", false);
    displayInvariantList("editTestCaseSimpleCreationEnvironment", "ENVIRONMENT", false);


    $("#newApplication #type").change(function(){
        switch ($(this).val()) {
            case 'GUI':
                $("[name='applicationHostField']").html(doc.getDocLabel("page_testcasecreate", "applicationGuiHost"));
                break;
            case 'SRV':
                $("[name='applicationHostField']").html(doc.getDocLabel("page_testcasecreate", "applicationSrvHost"));
                break;
            case 'APK':
                $("[name='applicationHostField']").html(doc.getDocLabel("page_testcasecreate", "applicationApkHost"));
                break;
            case 'IPA':
                $("[name='applicationHostField']").html(doc.getDocLabel("page_testcasecreate", "applicationIpaHost"));
                break;
            case 'FAT':
                $("[name='applicationHostField']").html(doc.getDocLabel("page_testcasecreate", "applicationFatHost"));
                break;
            default:
                $("[name='applicationHostField']").html(doc.getDocLabel("page_testcasecreate", "applicationGuiHost"));
        }
    });

    $('[data-toggle="popover"]').popover({
        'placement': 'auto',
        'container': 'body'}
    );
}

function checkFormSimpleCreationBeforeSubmit() {

    clearResponseMessage($('#editTestCaseSimpleCreationModal'));

    var formEdit = $('#editTestCaseSimpleCreationModalForm');

    var nameElement = formEdit.find("#editTestCaseSimpleCreationApplication");
    var nameElementEmpty = nameElement.prop("value") === '';

    var testElement = formEdit.find("#editTestCaseSimpleCreationTestFolderId");
    var testElementInvalid = testElement.prop("value").search("&");
    var testElementEmpty = testElement.prop("value") === '';

    var testIdElement = formEdit.find("#editTestCaseSimpleCreationTestcaseId");
    var testIdElementInvalid = testIdElement.prop("value").search("&");
    var testIdElementEmpty = testIdElement.prop("value") === '';


    var localMessage = new Message("danger", "Unexpected Error!");
    if (nameElementEmpty) {
        localMessage = new Message("danger", "Please specify the name of the application!");
        nameElement.parents("div.form-group").addClass("has-error");
        displayErrorMessage(localMessage, $('#editTestCaseSimpleCreationModal'));
    } else if (testElementInvalid !== -1) {
        localMessage = new Message("danger", "The test folder name cannot contain the symbol : &");
        // only the Test label will be put in red
        testElement.parents("div.form-group").addClass("has-error");
        displayErrorMessage(localMessage, $('#editTestCaseSimpleCreationModal'));
    } else if (testIdElementInvalid !== -1) {
        localMessage = new Message("danger", "The testcase id cannot contain the symbol : &");
        // only the TestId label will be put in red
        testIdElement.parents("div.form-group").addClass("has-error");
        displayErrorMessage(localMessage, $('#editTestCaseSimpleCreationModal'));
    } else if (testElementEmpty) {
        localMessage = new Message("danger", "Please specify the name of the test!");
        testElement.parents("div.form-group").addClass("has-error");
        displayErrorMessage(localMessage, $('#editTestCaseSimpleCreationModal'));
    } else if (testIdElementEmpty) {
        localMessage = new Message("danger", "Please specify the name of the Testcase Id!");
        testIdElement.parents("div.form-group").addClass("has-error");
        displayErrorMessage(localMessage, $('#editTestCaseSimpleCreationModal'));
    } else {
        nameElement.parents("div.form-group").removeClass("has-error");
        testIdElement.parents("div.form-group").removeClass("has-error");
        testElement.parents("div.form-group").removeClass("has-error");
    }

    // verify if all mandatory fields are not empty and valid
    if (nameElementEmpty || testElementInvalid !== -1 || testIdElementInvalid !== -1 || testElementEmpty || testIdElementEmpty) {
        return false;
    }
    return true;
}

function displayErrorMessage(localMessage) {
    var elementAlert = $('#editTestCaseSimpleCreationModal').find("div[id*='DialogMessagesAlert']");
    var elementAlertDescription = $('#editTestCaseSimpleCreationModal').find("span[id*='DialogAlertDescription']");

    elementAlertDescription.html(localMessage.message);
    elementAlert.addClass("alert-" + localMessage.messageType);
    elementAlert.fadeIn();
}

/***
 * Function that support the modal confirmation. Will call servlet to comit the transaction.
 * @param {String} mode - either ADD, EDIT or DUPLICATE in order to define the purpose of the modal.
 * @returns {null}
 */
function submitSimpleCreationForm() {
    showLoaderInModal('#editTestCaseSimpleCreationModal');
    var formEdit = $('#editTestCaseSimpleCreationModalForm');

    // Calculate servlet name to call.
    var myServlet = "api/public/testcases/create";

    var data = {};
    data['application'] = $('#editTestCaseSimpleCreationApplication').val();
    data['type'] = $('#editTestCaseSimpleCreationApplicationType').val();
    data['url'] = $('#editTestCaseSimpleCreationUrl').val();
    data['country'] = $('#editTestCaseSimpleCreationCountry').val();
    data['environment'] = $('#editTestCaseSimpleCreationEnvironment').val();
    data['description'] = $('#editTestCaseSimpleCreationDescription').val();
    data['testFolderId'] = $('#editTestCaseSimpleCreationTestFolderId').val();
    data['testcaseId'] = $('#editTestCaseSimpleCreationTestcaseId').val();
    data['system'] = getUser().defaultSystem;

    // Get the header data from the form.
    showLoaderInModal('#editTestCaseSimpleCreationModal');

    $.ajax({
        url: myServlet,
        async: true,
        method: "POST",
        headers: {"X-API-VERSION": 1},
        contentType: 'application/json',
        dataType: 'json',
        data: JSON.stringify(data),

        success: function (dataMessage) {
            hideLoaderInModal('#editTestCaseSimpleCreationModal');
            if (getAlertType(dataMessage.messageType) === "success") {
                window.location.href = "./TestCaseScript.jsp?test="+encodeURIComponent(dataMessage.test.replace(/\+/g, ' '))+"&testcase="+encodeURIComponent(dataMessage.testcase.replace(/\+/g, ' '))+"&oneclickcreation=true";
            } else {
                showMessage(dataMessage, $('#editTestCaseSimpleCreationModal'));
            }
        },
        error: showUnexpectedError
    });

}


/***
 * Feed the TestCase modal with all the data from the TestCase.
 * @param {String} modalId - Id of the modal to feed.
 * @param {String} defaultTest - default test to selected.
 * @returns {null}
 */
function feedNewTestCaseSimpleCreationModal(modalId, defaultTest) {
    clearResponseMessageMainPage();

    $('#editTestCaseSimpleCreationModalForm').show();

    var formEdit = $('#' + modalId);

    SimpleCreationAppendTestList(defaultTest);
    SimpleCreationFeedTestCaseField("editTestCaseSimpleCreationModalForm");
    SimpleCreationAppendApplicationList(undefined, undefined);

    $('#editTestCaseSimpleCreationModal [name="editTestCaseSimpleCreationApplication"]').change(function() {
        var appList = [];
        $.when($.getJSON("ReadApplication", "")).then(function (data) {
            for (var option in data.contentTable) {
                appList.push(data.contentTable[option].application);
            }
            let applicationSelectValue = $('#editTestCaseSimpleCreationModal [name="editTestCaseSimpleCreationApplication"]').val();
            if (appList.includes(applicationSelectValue) || applicationSelectValue == "") {
                $("#newApplication").attr('style', 'display:none');
            } else {
                $("#newApplication").attr('style', 'display:block');
            }
        });
    });

    formEdit.modal('show');
}


/***
 * Feed the testcase field inside modalForm modal with a new occurence value
 * for the given test. used when create or duplicate a new testcase.
 * @param {String} test - test used to calculate the new testcase value.
 * @param {String} modalForm - modal name where the testcase will be filled.
 * @returns {null}
 */
function SimpleCreationFeedTestCaseField(modalForm) {

    let test = $('#editTestCaseSimpleCreationModalForm select[name="editTestCaseSimpleCreationTestFolderId"]').val();

    $.ajax({
        url: "ReadTestCase",
        method: "GET",
        data: {test: encodeURIComponent(test), getMaxTC: true},
        dataType: "json",
        success: function (data) {
            $('#' + modalForm + ' [name="editTestCaseSimpleCreationTestcaseId"]').val(data.nextAvailableTestcaseId);
        },
        error: showUnexpectedError
    });
}

//Feed Application Combo and select default value if defined
function SimpleCreationAppendApplicationList(defaultValue, mySystem) {

    $('#editTestCaseSimpleCreationModal [name="editTestCaseSimpleCreationApplication"]').empty();
    $('#editTestCaseSimpleCreationModal [name="editTestCaseSimpleCreationApplication"]').select2({...getComboConfigApplication(true), dropdownParent: $('#editTestCaseSimpleCreationModal')});

    // Set Select2 Value.
    let option = $('<option></option>').text(defaultValue).val(defaultValue);
    $('#editTestCaseSimpleCreationModal [name="editTestCaseSimpleCreationApplication"]').append(option).trigger('change'); // append the option and update Select2

}

//Feed Test Combo and select default value if defined
function SimpleCreationAppendTestList(defaultValue) {
    $('#editTestCaseSimpleCreationModal [name="editTestCaseSimpleCreationTestFolderId"]').empty();
    $('#editTestCaseSimpleCreationModal [name="editTestCaseSimpleCreationTestFolderId"]').select2({...getComboConfigTest(), dropdownParent: $('#editTestCaseSimpleCreationModal')});

    // Set Select2 Value.
    let option = $('<option></option>').text(defaultValue).val(defaultValue);
    $('#editTestCaseSimpleCreationModal [name="editTestCaseSimpleCreationTestFolderId"]').append(option).trigger('change'); // append the option and update Select2
}
