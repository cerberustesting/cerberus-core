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
    $('#editTestCaseSimpleCreationModal').data("testcase", undefined);

    //Add event on Save button.
    $("#addTestCaseSimpleCreationButton").off("click");
    $("#addTestCaseSimpleCreationButton").click(function () {
        if (checkFormSimpleCreationBeforeSubmit()===true) {
            submitSimpleCreationForm();
        }
    });
    $('#addTestCaseSimpleCreationButton').attr('class', 'btn btn-primary');
    $('#addTestCaseSimpleCreationButton').removeProp('hidden');

    $('#editTestCaseSimpleCreationModalForm select[name="testFolderId"]').change(function () {
        SimpleCreationFeedTestCaseField("editTestCaseSimpleCreationModalForm");
    });

    //Feed the modal
    feedNewTestCaseSimpleCreationModal("editTestCaseSimpleCreationModal", defaultTest);

    //Clean response messages
    $('#editTestCaseSimpleCreationModalForm #application').parents("div.form-group").removeClass("has-error");
    clearResponseMessage($('#editTestCaseSimpleCreationModal'));
}

//Init Modal label & combo
function initModalTestCaseSimpleCreation() {
    var doc = new Doc();

    $("[name='testField']").html(doc.getDocLabel("test", "Test"));
    $("[name='testCaseField']").html(doc.getDocLabel("testcase", "TestCase"));
    $("[name='testCaseDescriptionField']").html(doc.getDocLabel("testcase", "Description"));
    $("[name='applicationField']").html(doc.getDocLabel("application", "Application"));
    $("[name='applicationNameField']").html(doc.getDocLabel("application", "Application"));
    $("[name='applicationTypeField']").html(doc.getDocLabel("application", "type"));
    $("[name='applicationHostField']").html(doc.getDocLabel("page_testcasecreate", "applicationGuiHost"));
    $("[name='countryField']").html(doc.getDocLabel("testcase", "countriesLabel"));
    $("[name='environmentField']").html(doc.getDocLabel("invariant", "ENVIRONMENT"));

    $("#ChooseApplicationLabel").html(doc.getDocLabel("page_testcasecreate", "chooseOrCreateApplicationLabel"));
    $("#describeTestcaseLabel").html(doc.getDocLabel("page_testcasecreate", "describeTestCaseLabel"));
    $("#defineTestcaseLabel").html(doc.getDocLabel("page_testcasecreate", "chooseOrCreateFolderLabel"));

    displayInvariantList("type", "APPLITYPE", false);
    displayInvariantList("country", "COUNTRY", false);
    displayInvariantList("environment", "ENVIRONMENT", false);


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

    var nameElement = formEdit.find("#application");
    var nameElementEmpty = nameElement.prop("value") === '';

    var testElement = formEdit.find("#test");
    var testElementInvalid = testElement.prop("value").search("&");
    var testElementEmpty = testElement.prop("value") === '';

    var testIdElement = formEdit.find("#testCase");
    var testIdElementInvalid = testIdElement.prop("value").search("&");
    var testIdElementEmpty = testIdElement.prop("value") === '';


    var localMessage = new Message("danger", "Unexpected Error!");
    if (nameElementEmpty) {
        localMessage = new Message("danger", "Please specify the name of the application!");
        nameElement.parents("div.form-group").addClass("has-error");
        showMessage(localMessage, $('#editTestCaseSimpleCreationModal'));
    } else if (testElementInvalid !== -1) {
        localMessage = new Message("danger", "The test name cannot contains the symbol : &");
        // only the Test label will be put in red
        testElement.parents("div.form-group").addClass("has-error");
        showMessage(localMessage, $('#editTestCaseSimpleCreationModal'));
    } else if (testIdElementInvalid !== -1) {
        localMessage = new Message("danger", "The testcase id name cannot contains the symbol : &");
        // only the TestId label will be put in red
        testIdElement.parents("div.form-group").addClass("has-error");
        showMessage(localMessage, $('#editTestCaseSimpleCreationModal'));
    } else if (testElementEmpty) {
        localMessage = new Message("danger", "Please specify the name of the test!");
        testElement.parents("div.form-group").addClass("has-error");
        showMessage(localMessage, $('#editTestCaseSimpleCreationModal'));
    } else if (testIdElementEmpty) {
        localMessage = new Message("danger", "Please specify the name of the Testcase Id!");
        testIdElement.parents("div.form-group").addClass("has-error");
        showMessage(localMessage, $('#editTestCaseSimpleCreationModal'));
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
        $.each(formEdit.serializeArray(), function(i, field) {
            data[field.name] = field.value;
        });
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
                window.location.href = "./TestCaseScript.jsp?test="+dataMessage.test+"&testcase="+dataMessage.testcase+"&oneclickcreation=true";
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

    $('#editTestCaseSimpleCreationModal [name="application"]').change(function() {
        if ($('#editTestCaseSimpleCreationModal [name="application"] option[data-select2-tag=true]')[0]!==undefined) {
            $("#newApplication").attr('style', 'display:block');
        } else {
            $("#newApplication").attr('style', 'display:none');
        }
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

    let test = $('#editTestCaseSimpleCreationModalForm select[name="testFolderId"]').val();

    $.ajax({
        url: "ReadTestCase",
        method: "GET",
        data: {test: encodeURIComponent(test), getMaxTC: true},
        dataType: "json",
        success: function (data) {
            $('#' + modalForm + ' [name="testcaseId"]').val(data.nextAvailableTestcaseId);
        },
        error: showUnexpectedError
    });
}

//Feed Application Combo and select default value if defined
function SimpleCreationAppendApplicationList(defaultValue, mySystem) {

    $('#editTestCaseSimpleCreationModal [name="application"]').empty();
    $('#editTestCaseSimpleCreationModal [name="application"]').select2(getComboConfigApplication());

    // Set Select2 Value.
    let option = $('<option></option>').text(defaultValue).val(defaultValue);
    $('#editTestCaseSimpleCreationModal [name="application"]').append(option).trigger('change'); // append the option and update Select2

}

//Feed Test Combo and select default value if defined
function SimpleCreationAppendTestList(defaultValue) {
    $('#editTestCaseSimpleCreationModal [name="testFolderId"]').empty();
    $('#editTestCaseSimpleCreationModal [name="testFolderId"]').select2(getComboConfigTest());

    // Set Select2 Value.
    let option = $('<option></option>').text(defaultValue).val(defaultValue);
    $('#editTestCaseSimpleCreationModal [name="testFolderId"]').append(option).trigger('change'); // append the option and update Select2
}
