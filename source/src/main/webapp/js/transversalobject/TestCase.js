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
var curMode = "";
var bugTrackerUrl = "";
var checkEmptyDescription = false;
var isInTutorial = false;

/***
 * Open the modal with testcase information.
 * @param {String} test - id of the test to open the modal
 * @param {String} testcase - id of the testcase to open the modal
 * @param {String} mode - mode to open the modal. Can take the values : ADD, DUPLICATE, EDIT
 * @param {String} tab - name of the tab to activate
 * @returns {null}
 */
function openModalTestCase(test, testcase, mode, tab) {
    curMode = mode;

    checkEmptyDescription = getParameterBoolean("cerberus_testcasepage_controlemptybugdescription", getUser().defaultSystem, true);

    // We only load the Labels and bind the events once for performance optimisations.
    if ($('#editTestCaseModal').data("initLabel") === undefined) {
        initModalTestCase();
        $('#editTestCaseModal').data("initLabel", true);
    }
    // Init the Saved data to false.
    $('#editTestCaseModal').data("Saved", false);
    $('#editTestCaseModal').data("testcase", undefined);

    if (!isEmpty(tab)) {
        $('.nav-tabs a[href="#' + tab + '"]').tab('show');
    }

    if (mode === "EDIT") {
        editTestCaseClick(test, testcase);
    } else if (mode === "DUPLICATE") {
        duplicateTestCaseClick(test, testcase);
    } else {
        addTestCaseClick(test, "ADD");
    }
    if (GetURLParameter("tutorielId") !== null) {
        isInTutorial = true;
    }
    $('#editTestCaseModalForm #application').parents("div.form-group").removeClass("has-error");
    clearResponseMessage($('#editTestCaseModal'));

}

function initModalTestCase() {
    var doc = new Doc();

    tinymce.init({
        selector: ".wysiwyg"
    });

    var availableUsers = getUserArray(true);
    $("#editTestCaseModal input#executor").autocomplete({
        source: availableUsers,
        messages: {
            noResults: '',
            results: function (amount) {
                return '';
            }
        }
    });
    $("#editTestCaseModal input#implementer").autocomplete({
        source: availableUsers,
        messages: {
            noResults: '',
            results: function (amount) {
                return '';
            }
        }
    });

    $("[name='testField']").html(doc.getDocOnline("test", "Test"));
    $("[name='testCaseField']").html(doc.getDocOnline("testcase", "TestCase"));
    $("[name='lastModifierField']").html(doc.getDocOnline("testcase", "LastModifier"));
    $("[name='applicationField']").html(doc.getDocOnline("application", "Application"));
    $("[name='statusField']").html(doc.getDocOnline("testcase", "Status"));
    $("[name='isActiveQAField']").html(doc.getDocOnline("testcase", "IsActiveQA"));
    $("[name='isActiveUATField']").html(doc.getDocOnline("testcase", "IsActiveUAT"));
    $("[name='isActiveUATField']").html(doc.getDocOnline("testcase", "IsActiveUAT"));
    $("[name='isActivePRODField']").html(doc.getDocOnline("testcase", "IsActivePROD"));
    $("[name='testCaseDescriptionField']").html(doc.getDocOnline("testcase", "Description"));
    $("[name='detailedDescriptionField']").html(doc.getDocOnline("testcase", "detailedDescription"));
    $("[name='testCaseDescriptionField']").html(doc.getDocOnline("testcase", "Description"));
    $("[name='descriptionField']").html(doc.getDocOnline("test", "Description"));
    $("[name='creatorField']").html(doc.getDocOnline("testcase", "Creator"));
    $("[name='implementerField']").html(doc.getDocOnline("testcase", "Implementer"));
    $("[name='executorField']").html(doc.getDocOnline("testcase", "Executor"));
    $("[name='typeField']").html(doc.getDocOnline("invariant", "TESTCASE_TYPE"));
    $("[name='priorityField']").html(doc.getDocOnline("invariant", "PRIORITY"));
    $("[name='countriesLabel']").html(doc.getDocOnline("testcase", "countriesLabel"));
    $("[name='tcDateCreaField']").html(doc.getDocOnline("testcase", "TCDateCrea"));
    $("[name='isActiveField']").html(doc.getDocOnline("testcase", "IsActive"));
    $("[name='fromMajorField']").html(doc.getDocOnline("testcase", "FromMajor"));
    $("[name='fromMinorField']").html(doc.getDocOnline("testcase", "FromMinor"));
    $("[name='toMajorField']").html(doc.getDocOnline("testcase", "ToMajor"));
    $("[name='toMinorField']").html(doc.getDocOnline("testcase", "ToMinor"));
    $("[name='targetMajorField']").html(doc.getDocOnline("testcase", "TargetMajor"));
    $("[name='targetMinorField']").html(doc.getDocOnline("testcase", "TargetMinor"));
    $("[name='conditionOperatorField']").html(doc.getDocOnline("testcase", "conditionOperator"));
    $("[name='conditionVal1Field']").html(doc.getDocOnline("testcase", "ConditionVal1"));
    $("[name='conditionVal2Field']").html(doc.getDocOnline("testcase", "ConditionVal2"));
    $("[name='conditionVal3Field']").html(doc.getDocOnline("testcase", "ConditionVal3"));
    $("[name='commentField']").html(doc.getDocOnline("testcase", "Comment"));
    $("[name='versionActivation']").html(doc.getDocOnline("testcase", "versionActivation"));
    $("[name='activationConditions']").html(doc.getDocOnline("testcase", "activationConditions"));
    $("[name='robotConstraints']").html(doc.getDocOnline("testcase", "robotConstraints"));
    $("#filters").html(doc.getDocOnline("page_testcaselist", "filters"));
    $("[name='btnLoad']").html(doc.getDocLabel("page_global", "buttonLoad"));
    $("[name='testField']").html(doc.getDocLabel("test", "Test"));
    $("[name='editEntryField']").html(doc.getDocLabel("page_testcaselist", "btn_edit"));
    $("[name='addEntryField']").html(doc.getDocLabel("page_testcaselist", "btn_create"));
    $("[name='linkField']").html(doc.getDocLabel("page_testcaselist", "link"));
    $("[name='originField']").html(doc.getDocLabel("testcase", "Origin"));
    $("[name='refOriginField']").html(doc.getDocLabel("testcase", "RefOrigin"));
    //TABs
    $("[name='testInfoField']").html(doc.getDocLabel("page_testcaselist", "testInfo"));
    $("[name='testCaseInfoField']").html(doc.getDocLabel("page_testcaselist", "testCaseInfo"));
    $("[name='testCaseParameterField']").html(doc.getDocLabel("page_testcaselist", "testCaseParameter"));
    $("[name='activationCriteriaField']").html(doc.getDocLabel("page_testcaselist", "activationCriteria"));
    // Traceability
    $("[name='lbl_datecreated']").html(doc.getDocOnline("transversal", "DateCreated"));
    $("[name='lbl_usrcreated']").html(doc.getDocOnline("transversal", "UsrCreated"));
    $("[name='lbl_datemodif']").html(doc.getDocOnline("transversal", "DateModif"));
    $("[name='lbl_usrmodif']").html(doc.getDocOnline("transversal", "UsrModif"));
    $("[name='versionField']").html(doc.getDocOnline("testcase", "version"));

    displayInvariantList("type", "TESTCASE_TYPE", false, undefined, undefined, undefined, undefined, "editTestCaseModal");
    displayInvariantList("status", "TCSTATUS", false, undefined, undefined, undefined, undefined, "editTestCaseModal");
    displayInvariantList("priority", "PRIORITY", false, undefined, undefined, undefined, undefined, "editTestCaseModal");
    displayInvariantList("conditionOperator", "TESTCASECONDITIONOPERATOR", false);

    $('[data-toggle="popover"]').popover({
        'placement': 'auto',
        'container': 'body'}
    );

    var availableOrigin = getInvariantArray("EXTERNALPROVIDER", false);
    $('#editTestCaseModal').find("#origin").autocomplete({
        source: availableOrigin,
        minLength: 0,
        messages: {
            noResults: '',
            results: function (amount) {
                return '';
            }
        }
    }).on("focus", function () {
        $(this).autocomplete("search", "");
    });

    var availableUserAgent = getInvariantArray("USERAGENT", false);
    $('#editTestCaseModal').find("#userAgent").autocomplete({
        source: availableUserAgent,
        minLength: 0,
        messages: {
            noResults: '',
            results: function (amount) {
                return '';
            }
        }
    }).on("focus", function () {
        $(this).autocomplete("search", "");
    });

    var availableScreenSize = getInvariantArray("SCREENSIZE", false);
    $('#editTestCaseModal').find("#screenSize").autocomplete({
        source: availableScreenSize,
        minLength: 0,
        messages: {
            noResults: '',
            results: function (amount) {
                return '';
            }
        }
    }).on("focus", function () {
        $(this).autocomplete("search", "");
    });

    $("#select_all").change(function () {  //"select all" change
        $("#countries input").prop('checked', $(this).prop("checked")); //change all ".checkbox" checked status
    });

    $("#addTestCaseDependencyButton").off("click").click(function () {
        var test = $("#selectTest").val();
        var testCase = $("#selectTestCase").val();
        var testCaseTxt = $("#selectTestCase option:selected").text();

        var indexTest = $("#selectTest").prop('selectedIndex')
        var indexTestCase = $("#selectTestCase").prop('selectedIndex')

        if ($('#' + getHtmlIdForTestCase(test, testCase)).length > 0) {
            showMessage(new Message("KO", 'Test case is already added'), $('#editTestCaseModal'));
        } else if (indexTest === 0 || indexTestCase === 0) {
            showMessage(new Message("KO", 'Select a test case'), $('#editTestCaseModal'));
        } else {
            addHtmlForDependencyLine(0, test, testCase, testCaseTxt, true, "", 0, "TCEXEEND");
        }
    })

}

function addHtmlForDependencyLine(id, test, testCase, testCaseTxt, activate, description = "", delay = 0, type) {
    let checked = "";
    if (activate)
        checked = "checked";
    $("#depenencyTable").append(
            '<tr role="row" class="odd" id="' + getHtmlIdForTestCase(test, testCase) + '"  test="' + test + '" testcase="' + testCase + '" testcaseid="' + id + '">' +
            '<td class="sorting_1" style="width: 50px;">' +
            '<div class="center btn-group">' +
            '<button id="removeTestparameter" onclick="removeTestCaseDependency(\'' + test + '\',\'' + testCase + '\');" class="removeTestparameter btn btn-default btn-xs margin-right5" name="removeTestparameter" title="Remove Test Case Dependency" type="button">' +
            '<span class="glyphicon glyphicon-trash"></span>' +
            '</button>' +
            '</div>' +
            '</td>' +
            '<td style="font-size: 14px">' + test + ' - ' + testCaseTxt + depTypeSelect(type) +
            '</td>' +
            '<td style="width: 50px;">  <input class="form-control input-xs"  type="checkbox"  name="activate" ' + checked + '/></td>' +
            '<td style="width: 60px;">  <input class="form-control" name="depDelay" value="' + delay + '"/></td>' +
            '<td>  <input class="form-control" name="depDescription" value="' + description + '"/></td>' +
            '</tr>'
            );
}

function depTypeSelect(value) {
    let descOK = "Execution ends OK";
    let desc = "Execution ends in any status";
    let selectedOK = "selected='selected'";
    let selected = "selected='selected'";
    if (value === "TCEXEENDOK") {
        selected = "";
    } else {
        selectedOK = "";
    }
    return "<select type='text' class='form-control marginTop5' name='type'>" +
            "<option value='TCEXEEND' " + selected + ">" + desc + "</option>" +
            "<option value='TCEXEENDOK' " + selectedOK + ">" + descOK + "</option>" +
            "</select>";

}

function getHtmlIdForTestCase(test, testCase) {
    return (test + '-' + testCase).replace(/ /g, '_').replace(/\./g, '_').replace(/\:/g, '_').replace(/\)/g, '_').replace(/\(/g, '_').replace(/\//g, '_');
}

function removeTestCaseDependency(test, testCase) {
    $('#' + getHtmlIdForTestCase(test, testCase)).remove();
}


/***
 * Open the modal with testcase information.
 * @param {String} test - type selected
 * @param {String} testCase - type selected
 * @returns {null}
 */
function editTestCaseClick(test, testCase) {

    $("#editTestCaseButton").off("click");
    $("#editTestCaseButton").click(function () {
        confirmTestCaseModalHandler("EDIT");
    });

    $('#editTestCaseButton').attr('class', 'btn btn-primary');
    $('#editTestCaseButton').removeProp('hidden');
    $('#duplicateTestCaseButton').attr('class', '');
    $('#duplicateTestCaseButton').attr('hidden', 'hidden');
    $('#addTestCaseButton').attr('class', '');
    $('#addTestCaseButton').attr('hidden', 'hidden');


    $("#originalTest").prop("value", test);
    $("#originalTestCase").prop("value", testCase);
    $("#editTestCaseModalForm #testCase").prop("value", testCase);

    // In Edit TestCase form, if we change the test, we get the latest testcase from that test.
    $('#editTestCaseModalForm select[name="test"]').off("change");
    $('#editTestCaseModalForm select[name="test"]').change(function () {
        feedTestCaseField(test, "editTestCaseModalForm");
        // Compare with original value in order to display the warning message.
        displayWarningOnChangeTestCaseKey(test, testCase);
    });
    $('#editTestCaseModalForm input[name="testCase"]').off("change");
    $('#editTestCaseModalForm input[name="testCase"]').change(function () {
        // Compare with original value in order to display the warning message.
        displayWarningOnChangeTestCaseKey(test, testCase);
    });
    feedTestCaseModal(test, testCase, "editTestCaseModal", "EDIT");
}

function displayWarningOnChangeTestCaseKey(test, testCase) {
    // Compare with original value in order to display the warning message.
    let old1 = $("#originalTest").val();
    let old2 = $("#originalTestCase").val();
    let new1 = $('#editTestCaseModalForm select[name="test"]').val();
    let new2 = $('#editTestCaseModalForm input[name="testCase"]').val();

    if ((old1 !== new1) || (old2 !== new2)) {
        var localMessage = new Message("WARNING", "If you rename that test case, it will loose the corresponding execution historic.");
        showMessage(localMessage, $('#editTestCaseModal'));
    } else {
        clearResponseMessage($('#editTestCaseModal'));
    }
}

/***
 * Open the modal with testcase information.
 * @param {String} test - type selected
 * @param {String} testCase - type selected
 * @returns {null}
 */
function duplicateTestCaseClick(test, testCase) {

    $("#duplicateTestCaseButton").off("click");
    $("#duplicateTestCaseButton").click(function () {
        confirmTestCaseModalHandler("DUPLICATE");
    });

    $('#editTestCaseButton').attr('class', '');
    $('#editTestCaseButton').attr('hidden', 'hidden');
    $('#duplicateTestCaseButton').attr('class', 'btn btn-primary');
    $('#duplicateTestCaseButton').removeProp('hidden');
    $('#addTestCaseButton').attr('class', '');
    $('#addTestCaseButton').attr('hidden', 'hidden');

    // In Duplicate TestCase form, if we change the test, we get the latest testcase from that test.
//    $('#editTestCaseModalForm select[name="test"]').off("change");
//    $('#editTestCaseModalForm select[name="test"]').change(function () {
//        feedTestCaseField(null, "editTestCaseModalForm");
//    });

    // In Add and duplicate TestCase form, if we change the test, we don't display any warning.
    $('#editTestCaseModalForm select[name="test"]').off("change");
    $('#editTestCaseModalForm select[name="test"]').change(function () {
        feedTestCaseField(test, "editTestCaseModalForm");
    });
    $('#editTestCaseModalForm input[name="testCase"]').off("change");

    feedTestCaseModal(test, testCase, "editTestCaseModal", "DUPLICATE");
}

/***
 * Open the modal in order to create a new testcase.
 * @param {String} defaultTest - optionaly define the test context to pick for creating the new testcase.
 * @returns {null}
 */
function addTestCaseClick(defaultTest) {

    $("#addTestCaseButton").off("click");
    $("#addTestCaseButton").click(function () {
        confirmTestCaseModalHandler("ADD");
    });

    $('#editTestCaseButton').attr('class', '');
    $('#editTestCaseButton').attr('hidden', 'hidden');
    $('#duplicateTestCaseButton').attr('class', '');
    $('#duplicateTestCaseButton').attr('hidden', 'hidden');
    $('#addTestCaseButton').attr('class', 'btn btn-primary');
    $('#addTestCaseButton').removeProp('hidden');

//    $('#editTestCaseModalForm select[name="test"]').off("change");
//    $('#editTestCaseModalForm select[name="test"]').change(function () {
//        feedTestCaseField(null, "editTestCaseModalForm");
//    });

    // In Add and duplicate TestCase form, if we change the test, we don't display any warning.
    $('#editTestCaseModalForm select[name="test"]').off("change");
    $('#editTestCaseModalForm select[name="test"]').change(function () {
        feedTestCaseField(defaultTest, "editTestCaseModalForm");
    });
    $('#editTestCaseModalForm input[name="testCase"]').off("change");

    feedNewTestCaseModal("editTestCaseModal", defaultTest);
}

/***
 * Feed the testcase field inside modalForm modal with a new occurence value
 * for the given test. used when create or duplicate a new testcase.
 * @param {String} test - test used to calculate the new testcase value.
 * @param {String} modalForm - modal name where the testcase will be filled.
 * @returns {null}
 */
function feedTestCaseField(test, modalForm) {
//    console.info("feed Test Case. " + test + " mode : " + curMode);
    var trigNewTestCase = true;
// Predefine the testcase value.
    if (curMode !== "EDIT") {
        trigNewTestCase = true;
        let new1 = $('#editTestCaseModalForm select[name="test"]').val();
        test = new1;

    } else {
        trigNewTestCase = false;
        let old1 = $("#originalTest").val();
        let new1 = $('#editTestCaseModalForm select[name="test"]').val();
        if (test !== new1) {
            test = new1;
            trigNewTestCase = true;
        } else {

            trigNewTestCase = false;
        }

    }

    if (trigNewTestCase) {
        $.ajax({
            url: "ReadTestCase",
            method: "GET",
            data: {test: encodeURIComponent(test), getMaxTC: true},
            dataType: "json",
            success: function (data) {
                $('#' + modalForm + ' [name="testCase"]').val(data.nextAvailableTestcaseId);
            },
            error: showUnexpectedError
        });
    }

}

/***
 * Function that support the modal confirmation. Will call servlet to comit the transaction.
 * @param {String} mode - either ADD, EDIT or DUPLICATE in order to define the purpose of the modal.
 * @returns {null}
 */
function confirmTestCaseModalHandler(mode) {
    clearResponseMessage($('#editTestCaseModal'));

    var formEdit = $('#editTestCaseModalForm');

    var nameElement = formEdit.find("#application");
    var nameElementEmpty = nameElement.prop("value") === '';

    var testElement = formEdit.find("#test");
    var testElementInvalid = testElement.prop("value").search("&");
    var testElementEmpty = testElement.prop("value") === '';

    var testIdElement = formEdit.find("#testCase");
    var testIdElementInvalid = testIdElement.prop("value").search("&");
    var testIdElementEmpty = testIdElement.prop("value") === '';

    var bugIdElement = formEdit.find("#editTCBugReport");
    var bugIdEmptyDesc = false;
    var bugIdDuplicated = false;
    var bugIdDuplicatedList = [];
    var table1 = $("#bugTableBody tr");
    var listBugId = "/";
    for (var i = 0; i < table1.length; i++) {
        var bug = $(table1[i]).data("bug");
        if (isEmpty(bug.desc)) {
            bugIdEmptyDesc = true;
        }
        if (listBugId.indexOf("/" + bug.id + "/") >= 0) {
            bugIdDuplicated = true;
            bugIdDuplicatedList.push(bug.id);
        }
        listBugId += bug.id + "/";
    }

    var localMessage = new Message("danger", "Unexpected Error!");
    if (nameElementEmpty) {
        localMessage = new Message("danger", "Please specify the name of the application!");
        nameElement.parents("div.form-group").addClass("has-error");
        showMessage(localMessage, $('#editTestCaseModal'));
    } else if (testElementInvalid !== -1) {
        localMessage = new Message("danger", "The test folder name cannot contain the symbol : &");
        // only the Test label will be put in red
        testElement.parents("div.form-group").addClass("has-error");
        showMessage(localMessage, $('#editTestCaseModal'));
    } else if (testIdElementInvalid !== -1) {
        localMessage = new Message("danger", "The testcase id cannot contain the symbol : &");
        // only the TestId label will be put in red
        testIdElement.parents("div.form-group").addClass("has-error");
        showMessage(localMessage, $('#editTestCaseModal'));
    } else if (testElementEmpty) {
        localMessage = new Message("danger", "Please specify the name of the test folder!");
        testElement.parents("div.form-group").addClass("has-error");
        showMessage(localMessage, $('#editTestCaseModal'));
    } else if (testIdElementEmpty) {
        localMessage = new Message("danger", "Please specify the name of the Testcase Id!");
        testIdElement.parents("div.form-group").addClass("has-error");
        showMessage(localMessage, $('#editTestCaseModal'));
    } else if (bugIdDuplicated) {
        localMessage = new Message("danger", "Duplicate BugID on Bug Report Tab! " + bugIdDuplicatedList.length + " duplicate entry(ies) : " + bugIdDuplicatedList.toString());
        bugIdElement.parents("div.form-group").addClass("has-error");
        showMessage(localMessage, $('#editTestCaseModal'));
    } else if (bugIdEmptyDesc && checkEmptyDescription) {
        localMessage = new Message("danger", "At least one BugID Description is empty on Bug Report Tab!");
        bugIdElement.parents("div.form-group").addClass("has-error");
        showMessage(localMessage, $('#editTestCaseModal'));
    } else {
        nameElement.parents("div.form-group").removeClass("has-error");
        testIdElement.parents("div.form-group").removeClass("has-error");
        testElement.parents("div.form-group").removeClass("has-error");
        bugIdElement.parents("div.form-group").removeClass("has-error");
    }

    // verify if all mandatory fields are not empty and valid
    if (nameElementEmpty || testElementInvalid !== -1 || testIdElementInvalid !== -1 || testElementEmpty || testIdElementEmpty || bugIdDuplicated || (bugIdEmptyDesc && checkEmptyDescription))
        return;

    tinyMCE.triggerSave();

    showLoaderInModal('#editTestCaseModal');

    // Enable the test combo before submit the form.
    if (mode === 'EDIT') {
        formEdit.find("#test").removeAttr("disabled");
    }
    // Calculate servlet name to call.
    var myServlet = "UpdateTestCase";
    if ((mode === "ADD") || (mode === "DUPLICATE")) {
        myServlet = "CreateTestCase";
    }

    // Getting Data from Country List
    var countries = $("#countries input");
    var table_country = [];
    for (var i = 0; i < countries.length; i++) {
        if (countries[i].checked === true) {
            var countryValue = {
                value: $(countries[i]).attr("name"),
                toDelete: false
            }
        } else {
            countryValue = {
                value: $(countries[i]).attr("name"),
                toDelete: true
            }
        }
        table_country.push(countryValue)
    }

    // Getting Data from Label List
    var table_label = [];
    var table2 = $('#selectLabelS').treeview('getSelected', {levels: 20, silent: true});
    for (var i = 0; i < table2.length; i++) {
        var newLabel1 = {
            labelId: table2[i].id,
            toDelete: false
        };
        table_label.push(newLabel1);
    }
    var table2 = $('#selectLabelR').treeview('getSelected', {levels: 20, silent: true});
    for (var i = 0; i < table2.length; i++) {
        var newLabel1 = {
            labelId: table2[i].id,
            toDelete: false
        };
        table_label.push(newLabel1);
    }
    var table2 = $('#selectLabelB').treeview('getSelected', {levels: 20, silent: true});
    for (var i = 0; i < table2.length; i++) {
        var newLabel1 = {
            labelId: table2[i].id,
            toDelete: false
        };
        table_label.push(newLabel1);
    }
    let dataIsMuted = ($("#editTestCaseModalForm").find("#isMuted .glyphicon").hasClass("glyphicon-volume-off"));


    // Getting Dependency data
    let testcaseDependencies = []
    $("#depenencyTable").find("tr")
            .each((t, v) =>
                testcaseDependencies.push(
                        {
                            id: $(v).attr("testcaseid"),
                            test: $(v).attr("test"),
                            testcase: $(v).attr("testcase"),
                            description: $(v).find("[name='depDescription']").val(),
                            depDelay: $(v).find("[name='depDelay']").val(),
                            type: $(v).find("[name='type']").val(),
                            isActive: $(v).find("[name='activate']").is(":checked")
                        }
                )
            )


    // Get the header data from the form.
    var data = convertSerialToJSONObject(formEdit.serialize());
//    console.info(data);
    data.isActive = (data.isActive === "1") ? true : false;
    data.isActivePROD = (data.isActivePROD === "1") ? true : false;
    data.isActiveQA = (data.isActiveQA === "1") ? true : false;
    data.isActiveUAT = (data.isActiveUAT === "1") ? true : false;
//    console.info(data);

    showLoaderInModal('#editTestCaseModal');

    // Getting Data from Bug table body.
    var table1 = $("#bugTableBody tr");
    var table_bug = [];

    for (var i = 0; i < table1.length; i++) {
        var bug = $(table1[i]).data("bug")
        if (bug.toDelete !== true) {
            var DataObj1 = {};
            DataObj1.id = bug.id;
            DataObj1.desc = bug.desc;
            DataObj1.act = bug.act;
            DataObj1.url = bug.url;
            DataObj1.dateCreated = bug.dateCreated;
            DataObj1.dateClosed = bug.dateClosed;
            table_bug.push(DataObj1);
        }
    }

    $.ajax({
        url: myServlet,
        async: true,
        method: "POST",
        data: {
            test: data.test,
            testcase: data.testCase,
            originalTest: data.originalTest,
            originalTestcase: data.originalTestCase,
            isActive: data.isActive,
            isActivePROD: data.isActivePROD,
            isActiveQA: data.isActiveQA,
            isActiveUAT: data.isActiveUAT,
            application: data.application,
            detailedDescription: data.detailedDescription,
            bugs: JSON.stringify(table_bug),
            comment: data.comment,
            fromMinor: data.fromMinor,
            fromMajor: data.fromMajor,
            type: data.type,
            implementer: data.implementer,
            executor: data.executor,
            origin: data.origin,
            priority: data.priority,
            project: data.project,
            refOrigin: data.refOrigin,
            description: data.description,
            status: data.status,
            targetMinor: data.targetMinor,
            targetMajor: data.targetMajor,
            conditionOperator: data.conditionOperator,
            conditionValue1: data.conditionValue1,
            conditionValue2: data.conditionValue2,
            conditionValue3: data.conditionValue3,
            ticket: data.ticket,
            toMinor: data.toMinor,
            toMajor: data.toMajor,
            userAgent: data.userAgent,
            screenSize: data.screenSize,
            isMuted: dataIsMuted,
            labels: JSON.stringify(table_label),
            countries: JSON.stringify(table_country),
            dependencies: JSON.stringify(testcaseDependencies)},
        success: function (dataMessage) {
            hideLoaderInModal('#editTestCaseModal');
            if (getAlertType(dataMessage.messageType) === "success") {
                if (isInTutorial) {
                    $('#confirmationModal').modal('hide');
                    window.location.href = "TestCaseScript.jsp?test=" + encodeURI(data.test.replace(/\+/g, ' ')) + "&testcase=" + encodeURI(data.testCase.replace(/\+/g, ' ')) + "&tutorielId=" + GetURLParameter("tutorielId") + "&startStep=10";
                } else {
                    if (((mode === "ADD") || (mode === "DUPLICATE"))) {
                        var doc = new Doc();
                        // If we created a testcase, We propose the user to go and edit testcase directly.
                        showModalConfirmation(function () {
                            $('#confirmationModal').modal('hide');
                            // Due to already encoded format of data.test, we need to decode it first and then encode it again.
                            window.location.href = "TestCaseScript.jsp?test=" + encodeURIComponent(decodeURIComponent(data.test).replace(/\+/g, ' ')) + "&testcase=" + encodeURIComponent(decodeURIComponent(data.testCase).replace(/\+/g, ' '));
                        }, function () {
                        }, doc.getDocLabel("page_global", "btn_savetableconfig"), doc.getDocLabel("page_testcaselist", "ask_edit_testcase"), "", "", "", "");
                    }
                }
                var oTable = $("#testCaseTable").dataTable();
                oTable.fnDraw(false);
                $('#editTestCaseModal').data("Saved", true);
                $('#editTestCaseModal').data("testcase", data);
                $('#editTestCaseModal').data("bug", table_bug);
                $('#editTestCaseModal').data("appURL", bugTrackerUrl);
                $('#editTestCaseModal').modal('hide');
                showMessage(dataMessage);
            } else {
                showMessage(dataMessage, $('#editTestCaseModal'));
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
function feedNewTestCaseModal(modalId, defaultTest) {
    clearResponseMessageMainPage();

    var formEdit = $('#' + modalId);

    $("#addBug").off("click");
    $("#addBug").click(function () {
        addNewBugRow("bugTableBody", undefined);
    });

    appendBuildRevListOnTestCase(getUser().defaultSystem, undefined);

    feedTestCaseData(undefined, modalId, "ADD", true, defaultTest);
    // Labels
    loadLabel(undefined, undefined, "#selectLabel");
    //Application Combo
    appendApplicationList(undefined, undefined, modalId);

    formEdit.modal('show');
}

/***
 * Feed the TestCase modal with all the data from the TestCase.
 * @param {String} test - type selected
 * @param {String} testCase - type selected
 * @param {String} modalId - type selected
 * @param {String} mode - either ADD, EDIT or DUPLICATE in order to define the purpose of the modal.
 * @returns {null}
 */
function feedTestCaseModal(test, testCase, modalId, mode) {
    clearResponseMessageMainPage();

    var formEdit = $('#' + modalId);


    var jqxhr = $.getJSON("ReadTestCase", "test=" + encodeURIComponent(test) + "&testCase=" + encodeURIComponent(testCase));
    $.when(jqxhr).then(function (data) {

        var testCase = data.contentTable[0];
        var testperm = data["hasPermissionsUpdate"];

        var appInfo = $.getJSON("ReadApplication", "application=" + encodeURIComponent(testCase.application));

        $.when(appInfo).then(function (appData) {
            var currentSys = getUser().defaultSystem;
            var t = appData.contentTable;
            let bugTrackerUrl = t.bugTrackerUrl;

            feedTestCaseData(testCase, modalId, mode, testperm, undefined, bugTrackerUrl);

            $("#addBug").off("click");
            $("#addBug").click(function () {
                addNewBugRow("bugTableBody", bugTrackerUrl);
            });

            // Loading build and revision various combos.
            appendBuildRevListOnTestCase(t.system, testCase);
            // Title of the label list.
            $("#labelField").html("Labels from system : " + t.system);
            // Loading the label list from aplication of the testcase.
            loadLabel(testCase.labels, t.system, "#selectLabel", undefined, testCase.test, testCase.testcase);
            // Loading application combo from the system of the current application.
            appendApplicationList(testCase.application, t.system, modalId);

        });

        formEdit.modal('show');
    });

    fillTestAndTestCaseSelect("#selectTest", "#selectTestCase", undefined, undefined, true)
    $("#selectTest").change(function () {
        fillTestCaseSelect("#selectTestCase", $("#selectTest").val(), undefined, true);
    });


}



function fillTestCaseSelect(selectorTestCaseSelect, test, testcase, allTestCases) {
    var doc = new Doc();
    var system = getSys();
    var url1 = "";
    if (allTestCases) {
        url1 = getUser().systemQuery;
    } else {
        url1 = getUser().defaultSystemsQuery;
    }
    if (test !== null && test !== undefined) {
        $.ajax({
            url: "ReadTestCase?test=" + encodeURIComponent(test) + url1,
            async: true,
            success: function (data) {
                data.contentTable.sort(function (a, b) {
                    var aa = a.testcase.toLowerCase();
                    var bb = b.testcase.toLowerCase();
                    if (aa > bb) {
                        return 1;
                    } else if (aa < bb) {
                        return -1;
                    }
                    return 0;
                });
                $(selectorTestCaseSelect).find('option').remove()

                $(selectorTestCaseSelect).prepend("<option value=''>" + doc.getDocLabel("page_testcasescript", "select_testcase") + "</option>");
                for (var i = 0; i < data.contentTable.length; i++) {
                    $(selectorTestCaseSelect).append("<option value='" + data.contentTable[i].testcase + "'>" + data.contentTable[i].testcase + " - " + data.contentTable[i].description + " [" + data.contentTable[i].application + "]</option>")
                }
                if (testcase !== null) {
                    $(selectorTestCaseSelect + " option[value='" + testcase + "']").prop('selected', true);
                    window.document.title = "TestCase - " + testcase;
                }

                $(selectorTestCaseSelect).select2({width: '100%'});
            }
        });
    }

}

/**
 * Fill Test and Testcase select,
 * @param test   auto select this test
 * @param testcase  auto select this testcase
 */
function fillTestAndTestCaseSelect(selectorTestSelect, selectorTestCaseSelect, test, testcase, allTestCases) {
    var doc = new Doc();
    var system = getSys();
    $.ajax({
        url: "ReadTest",
        async: true,
        success: function (data) {
            data.contentTable.sort(function (a, b) {
                var aa = a.test.toLowerCase();
                var bb = b.test.toLowerCase();
                if (aa > bb) {
                    return 1;
                } else if (aa < bb) {
                    return -1;
                }
                return 0;
            });

            $(selectorTestSelect).find("option").remove();
            $(selectorTestSelect).prepend("<option value=''>" + doc.getDocLabel("page_testcasescript", "select_test") + "</option>");
            for (var i = 0; i < data.contentTable.length; i++) {
                $(selectorTestSelect).append("<option value='" + data.contentTable[i].test + "'>" + data.contentTable[i].test + " - " + data.contentTable[i].description + "</option>");
            }

            if (test !== null) {
                $(selectorTestSelect + " option[value='" + test + "']").prop('selected', true);
            }

            $(selectorTestSelect).select2({width: "100%"}).next().css("margin-bottom", "7px");
        }
    });
    fillTestCaseSelect(selectorTestCaseSelect, test, testcase, allTestCases)
//    if (selectorTestCaseSelect !== undefined) {
//        fillTestCaseSelect(selectorTestCaseSelect, test, testcase, allTestCases);
//    }
}


function feedTestCaseData(testCase, modalId, mode, hasPermissionsUpdate, defaultTest, bugTrackerUrl) {

    var formEdit = $('#' + modalId);
    var doc = new Doc();

//    $('#editTestCaseModal [name="test"]').select2(getComboConfigTest());

    var observer = new MutationObserver(function (mutations, me) {
        var detailedDescription = tinyMCE.get('detailedDescription');
        if (detailedDescription !== null) {
            if (isEmpty(testCase)) {
                tinyMCE.get('detailedDescription').setContent("");
            } else {
                tinyMCE.get('detailedDescription').setContent(testCase.detailedDescription);
            }

            me.disconnect();
        }
        return;
    });

    // start observing
    observer.observe(document, {
        childList: true,
        subtree: true
    });

    // Data Feed.
    if (mode === "EDIT") {
        $("[name='editTestCaseField']").html(doc.getDocOnline("page_testcaselist", "btn_edit"));
        appendTestList(testCase.test);
        formEdit.find("#testCase").prop("value", testCase.testcase);
        formEdit.find("#status").prop("value", testCase.status);
        formEdit.find("#usrcreated").prop("value", testCase.usrCreated);
        formEdit.find("#datecreated").prop("value", getDate(testCase.dateCreated));
        formEdit.find("#usrmodif").prop("value", testCase.usrModif);
        formEdit.find("#datemodif").prop("value", getDate(testCase.dateModif));
    } else { // DUPLICATE or ADD
        formEdit.find("#usrcreated").prop("value", "");
        formEdit.find("#datecreated").prop("value", "");
        formEdit.find("#usrmodif").prop("value", "");
        formEdit.find("#datemodif").prop("value", "");
        formEdit.find("#isActivePROD").prop("checked", false);
        formEdit.find("#status option:nth(0)").attr("selected", "selected"); // We select the 1st entry of the status combobox.
        if (mode === "ADD") {
            $("[name='editTestCaseField']").html(doc.getDocOnline("page_testcaselist", "btn_create"));
            appendTestList(defaultTest);
            feedTestCaseField(defaultTest, "editTestCaseModalForm");  // Calculate corresponding testcase value.
        } else { // DUPLICATE
            $("[name='editTestCaseField']").html(doc.getDocOnline("page_testcaselist", "btn_duplicate"));
            appendTestList(testCase.test);
            feedTestCaseField(testCase.test, "editTestCaseModalForm");  // Calculate corresponding testcase value.
        }
    }
    formEdit.find("#isMuted .glyphicon").removeClass("glyphicon-volume-up glyphicon-volume-off");
    if (isEmpty(testCase)) {
        formEdit.find("#originalTest").prop("value", "");
        formEdit.find("#originalTestCase").prop("value", "");
        formEdit.find("#implementer").prop("value", "");
        formEdit.find("#executor").prop("value", "");
        formEdit.find("#type").val("AUTOMATED");
        formEdit.find("#priority option:nth(0)").attr("selected", "selected");
        formEdit.find("#isActiveQA").prop("checked", true);
        formEdit.find("#isActiveUAT").prop("checked", true);
        formEdit.find("#userAgent").prop("value", "");
        formEdit.find("#screenSize").prop("value", "");
        formEdit.find("#description").prop("value", "");
        formEdit.find("#isActive").prop("checked", true);
        $('#bugTableBody tr').remove();
        formEdit.find("#conditionOperator").prop("value", "always");
        formEdit.find("#conditionVal1").prop("value", "");
        formEdit.find("#conditionVal2").prop("value", "");
        formEdit.find("#conditionVal3").prop("value", "");
        formEdit.find("#comment").prop("value", "");
        formEdit.find("#origin").prop("value", "");
        formEdit.find("#refOrigin").prop("value", "");
        formEdit.find("#isMuted .glyphicon").addClass("glyphicon-volume-up");
    } else {
        formEdit.find("#test").prop("value", testCase.test);
        formEdit.find("#originalTest").prop("value", testCase.test);
        formEdit.find("#originalTestCase").prop("value", testCase.testcase);
        formEdit.find("#newTest").prop("value", testCase.test);
        formEdit.find("#implementer").prop("value", testCase.implementer);
        formEdit.find("#executor").prop("value", testCase.executor);
        formEdit.find("#tcDateCrea").prop("value", testCase.dateCreated);
        formEdit.find("#type").prop("value", testCase.type);
        formEdit.find("#priority").prop("value", testCase.priority);
        formEdit.find("#isActiveQA").prop("checked", testCase.isActiveQA);
        formEdit.find("#isActiveUAT").prop("checked", testCase.isActiveUAT);
        formEdit.find("#isActivePROD").prop("checked", testCase.isActivePROD);
        formEdit.find("#userAgent").prop("value", testCase.userAgent);
        formEdit.find("#screenSize").prop("value", testCase.screenSize);
        formEdit.find("#description").prop("value", testCase.description);
        formEdit.find("#isActive").prop("checked", testCase.isActive);
        formEdit.find("#origin").prop("value", testCase.origine);
        formEdit.find("#refOrigin").prop("value", testCase.refOrigine);
        if (testCase.isMuted) {
            formEdit.find("#isMuted .glyphicon").addClass("glyphicon-volume-off");
        } else {
            formEdit.find("#isMuted .glyphicon").addClass("glyphicon-volume-up");
        }

        $('#bugTableBody tr').remove();
        // Sorting Bug list.
        testCase.bugs.sort(function (a, b) {
            if (a.act === b.act) {
                if (a.id === b.id) {
                    if (b.dateCreated < a.dateCreated)
                        return 1;
                    else
                        return -1;
                } else {
                    if (a.id > b.id)
                        return 1;
                    else
                        return -1;
                }
            }
            if (a.act === false)
                return 1;
            else
                return -1;
        });
        $.each(testCase.bugs, function (idx, obj) {
            obj.toDelete = false;
            if (isEmpty(obj.act)) {
                obj.act = true;
            }
            if (isEmpty(obj.dateCreated)) {
                obj.dateCreated = new Date('1970-01-01');
            }
            if (isEmpty(obj.dateClosed)) {
                obj.dateClosed = new Date('1970-01-01');
            }

            appendbugRow(obj, "bugTableBody", bugTrackerUrl);
        });

        formEdit.find("#conditionOperator").prop("value", testCase.conditionOperator);
        formEdit.find("#conditionVal1").prop("value", testCase.conditionValue1);
        formEdit.find("#conditionVal2").prop("value", testCase.conditionValue2);
        formEdit.find("#conditionVal3").prop("value", testCase.conditionValue3);
        formEdit.find("#comment").prop("value", testCase.comment);
        formEdit.find("#version").prop("value", testCase.version);
        appendTestCaseDepList(testCase);
    }

    // Authorities

    //We define here the rule that enable or nt the fields depending on if user has the credentials to edit.
    var doBlockAllFields = false;
    if (mode === "EDIT") {
        doBlockAllFields = !(hasPermissionsUpdate);
    } else { // DUPLICATE or ADD
        doBlockAllFields = false;
    }

    if (doBlockAllFields) { // If readonly, we only readonly all fields
        //test case info
        formEdit.find("#test").prop("disabled", "disabled");
        formEdit.find("#testCase").prop("readonly", "readonly");
        formEdit.find("#implementer").prop("readonly", "readonly");
        formEdit.find("#application").prop("disabled", "disabled");
        formEdit.find("#status").prop("disabled", "disabled");
        formEdit.find("#type").prop("disabled", "disabled");
        formEdit.find("#priority").prop("disabled", "disabled");
        formEdit.find("#isActiveQA").prop("readonly", "readonly");
        formEdit.find("#isActiveUAT").prop("readonly", "readonly");
        formEdit.find("#isActivePROD").prop("readonly", "readonly");
        formEdit.find("#userAgent").prop("disabled", "disabled");
        formEdit.find("#screenSize").prop("disabled", "disabled");
        formEdit.find("#description").prop("readonly", "readonly");
        if (tinyMCE.get('detailedDescription').getBody() !== null)
            tinyMCE.get('detailedDescription').getBody().setAttribute('contenteditable', false);
        formEdit.find("#isActive").prop("readonly", "readonly");
        formEdit.find("#fromMajor").prop("disabled", "disabled");
        formEdit.find("#fromMinor").prop("disabled", "disabled");
        formEdit.find("#toMajor").prop("disabled", "disabled");
        formEdit.find("#toMinor").prop("disabled", "disabled");
        formEdit.find("#targetMajor").prop("disabled", "disabled");
        formEdit.find("#targetMinor").prop("disabled", "disabled");
        formEdit.find("#conditionOperator").prop("disabled", "disabled");
        formEdit.find("#conditionVal1").prop("disabled", "disabled");
        formEdit.find("#conditionVal2").prop("disabled", "disabled");
        formEdit.find("#conditionVal3").prop("disabled", "disabled");
        formEdit.find("#comment").prop("readonly", "readonly");
        formEdit.find("#addBug").prop("disabled", "disabled");
        formEdit.find("#addTestCaseDependencyButton").prop("disabled", "disabled");
        formEdit.find("#executor").prop("readonly", "readonly");
        formEdit.find("#origin").prop("readonly", "readonly");
        formEdit.find("#refOrigin").prop("readonly", "readonly");
//        formEdit.find("#bugId").prop("readonly", "readonly");
        // feed the country list.
        appendTestCaseCountries(testCase, true);
        // Save button is hidden.
        $('#editTestCaseButton').attr('class', '');
        $('#editTestCaseButton').attr('hidden', 'hidden');
    } else {
        //test case info
        formEdit.find("#test").removeAttr("disabled");
        formEdit.find("#testCase").removeAttr("readonly");
        formEdit.find("#isActive").removeProp("readonly");
//        formEdit.find("#bugId").removeProp("readonly");
        formEdit.find("#implementer").removeProp("readonly");
        formEdit.find("#application").removeProp("disabled");
        formEdit.find("#status").removeProp("disabled");
        formEdit.find("#type").removeProp("disabled");
        formEdit.find("#priority").removeProp("disabled");
        formEdit.find("#isActiveQA").removeProp("readonly");
        formEdit.find("#isActiveUAT").removeProp("readonly");
        formEdit.find("#isActivePROD").removeProp("readonly");
        formEdit.find("#userAgent").removeProp("disabled");
        formEdit.find("#screenSize").removeProp("disabled");
        formEdit.find("#description").removeProp("readonly");
        if (tinyMCE.get('detailedDescription').getBody() !== null)
            tinyMCE.get('detailedDescription').getBody().setAttribute('contenteditable', true);
        formEdit.find("#isActive").removeProp("readonly");
        formEdit.find("#fromMajor").removeProp("disabled");
        formEdit.find("#fromMinor").removeProp("disabled");
        formEdit.find("#toMajor").removeProp("disabled");
        formEdit.find("#toMinor").removeProp("disabled");
        formEdit.find("#targetMajor").removeProp("disabled");
        formEdit.find("#targetMinor").removeProp("disabled");
        formEdit.find("#conditionOperator").removeProp("disabled");
        formEdit.find("#conditionVal1").removeProp("disabled");
        formEdit.find("#conditionVal2").removeProp("disabled");
        formEdit.find("#conditionVal3").removeProp("disabled");
        formEdit.find("#comment").removeProp("readonly");
        formEdit.find("#addBug").removeProp("disabled");
        formEdit.find("#addTestCaseDependencyButton").removeProp("disabled");
        formEdit.find("#executor").removeProp("disabled");
        formEdit.find("#origin").removeProp("readonly");
        formEdit.find("#refOrigin").removeProp("readonly");
        // feed the country list.
        appendTestCaseCountries(testCase, false);
    }

}

function appendbugRow(obj, tablebody, bugTrackerUrl) {
    var table = $("#" + tablebody);

    var newbugTrackerUrl = "";
    if (!isEmpty(obj.url)) {
        newbugTrackerUrl = obj.url;
    } else {
        if (!isEmpty(bugTrackerUrl)) {
            if (!isEmpty(obj.id)) {
                newbugTrackerUrl = bugTrackerUrl.replace(/%BUGID%/g, obj.id);
            }
        }
    }

    var row = $("<tr></tr>");
    var deleteBtn = $("<button type=\"button\"></button>").addClass("btn btn-default btn-xs").append($("<span></span>").addClass("glyphicon glyphicon-trash"));
    var actInput = $("<input type='checkbox'>").addClass("form-control input-sm").prop("checked", obj.act);
    var bugidInput = $("<input  maxlength=\"15\">").addClass("form-control").val(obj.id);
    var bugdescInput = $("<input  maxlength=\"50\">").addClass("form-control").val(obj.desc);
    var dateCreatedInput = $("<input readonly=\"true\">").addClass("form-control input-sm").val(getDate(obj.dateCreated));
    var dateClosedInput = $("<input readonly=\"true\">").addClass("form-control input-sm").val(getDate(obj.dateClosed));
    if (newbugTrackerUrl !== "") {
        var buglinkText = $("<a></a>").text(obj.id);
        buglinkText.prop("href", newbugTrackerUrl).prop("target", "_blank");
    } else {
        var buglinkText = $("<div></div>").text(obj.id);
    }
    var deleteData = $("<td></td>").append(deleteBtn);
    var actData = $("<td></td>").append(actInput);
    var bugidData = $("<td></td>").append(bugidInput);
    var bugdescData = $("<td></td>").append(bugdescInput);
    var buglinkData = $("<td></td>").append(buglinkText);
    var dateCreatedData = $("<td></td>").append(dateCreatedInput);
    var dateClosedData = $("<td></td>").append(dateClosedInput);

    deleteBtn.click(function () {
        obj.toDelete = (obj.toDelete) ? false : true;

        if (obj.toDelete) {
            row.addClass("danger");
        } else {
            row.removeClass("danger");
        }
    });

    actInput.click(function () {
        obj.act = (obj.act) ? false : true;

        if ((obj.act) === false) {
            obj.dateClosed = new Date();
            dateClosedInput.val(obj.dateClosed.toLocaleString());
        }
    });

    bugidInput.change(function () {
        obj.id = $(this).val();
        var newbugTrackerUrl = "";
        if (obj.id !== "" && bugTrackerUrl) {
            newbugTrackerUrl = bugTrackerUrl.replace("%BUGID%", obj.id);
        }
        buglinkText.prop("href", newbugTrackerUrl).text(obj.id);
    });
    bugdescInput.change(function () {
        obj.desc = $(this).val();
    });

    row.append(deleteData).append(actData).append(bugidData).append(bugdescData).append(buglinkData).append(dateClosedData).append(dateCreatedData);
    row.data("bug", obj);
    table.append(row);
}

function addNewBugRow(dataTableBody, bugTrackerUrl) {
    var nbRows = $("#" + dataTableBody + " tr").size() + 1;
    var newBugData = {
        id: "BUGID" + nbRows,
        desc: "",
        url: "",
        act: true,
        dateCreated: new Date(),
        dateClosed: new Date('1970-01-01'),
        toDelete: false
    };
    appendbugRow(newBugData, dataTableBody, bugTrackerUrl);
}


/***
 * Feed Build and Revision combo on the testcase modal.
 * @param {String} system - system of the testcase.
 * @param {String} editData - testcase data that will be used to feed the values of all combos.
 * @returns {null}
 */
function appendBuildRevListOnTestCase(system, editData) {

    var jqxhr = $.getJSON("ReadBuildRevisionInvariant", "system=" + encodeURIComponent(system) + "&level=1");
    $.when(jqxhr).then(function (data) {
        var fromMajor = $("[name=fromMajor]");
        var toMajor = $("[name=toMajor]");
        var targetMajor = $("[name=targetMajor]");

        fromMajor.empty();
        toMajor.empty();
        targetMajor.empty();

        fromMajor.append($('<option></option>').text("-----").val(""));
        toMajor.append($('<option></option>').text("-----").val(""));
        targetMajor.append($('<option></option>').text("-----").val(""));

        for (var index = 0; index < data.contentTable.length; index++) {
            fromMajor.append($('<option></option>').text(data.contentTable[index].versionName).val(data.contentTable[index].versionName));
            toMajor.append($('<option></option>').text(data.contentTable[index].versionName).val(data.contentTable[index].versionName));
            targetMajor.append($('<option></option>').text(data.contentTable[index].versionName).val(data.contentTable[index].versionName));
        }

        if (editData !== undefined) {
            var formEdit = $('#editTestCaseModal');

            formEdit.find("#fromMajor").prop("value", editData.fromMajor);
            formEdit.find("#toMajor").prop("value", editData.toMajor);
            formEdit.find("#targetMajor").prop("value", editData.targetMajor);
        }

    });

    var jqxhr = $.getJSON("ReadBuildRevisionInvariant", "system=" + encodeURIComponent(system) + "&level=2");
    $.when(jqxhr).then(function (data) {
        var fromMinor = $("[name=fromMinor]");
        var toMinor = $("[name=toMinor]");
        var targetMinor = $("[name=targetMinor]");

        fromMinor.empty();
        toMinor.empty();
        targetMinor.empty();

        fromMinor.append($('<option></option>').text("-----").val(""));
        toMinor.append($('<option></option>').text("-----").val(""));
        targetMinor.append($('<option></option>').text("-----").val(""));

        for (var index = 0; index < data.contentTable.length; index++) {
            fromMinor.append($('<option></option>').text(data.contentTable[index].versionName).val(data.contentTable[index].versionName));
            toMinor.append($('<option></option>').text(data.contentTable[index].versionName).val(data.contentTable[index].versionName));
            targetMinor.append($('<option></option>').text(data.contentTable[index].versionName).val(data.contentTable[index].versionName));
        }

        if (editData !== undefined) {
            var formEdit = $('#editTestCaseModal');

            formEdit.find("[name=fromMinor]").prop("value", editData.fromMinor);
            formEdit.find("[name=toMinor]").prop("value", editData.toMinor);
            formEdit.find("[name=targetMinor]").prop("value", editData.targetMinor);
        }
    });
}

function appendTestCaseDepList(testCase) {
    $("#depenencyTable").find("tr").remove();// clean the table

    testCase.dependencies.forEach((dep) => {
        addHtmlForDependencyLine(dep.id, dep.dependencyTest, dep.dependencyTestcase, dep.dependencyTestcase + " - " + dep.testcaseDescription, dep.isActive, dep.description, dep.dependencyTCDelay, dep.type);
    });
}


function appendTestCaseCountries(testCase, isReadOnly) {
    $("#countries label").remove();
    var countries = $("[name=countries]");

    var jqxhr = $.getJSON("FindInvariantByID", "idName=COUNTRY");
    $.when(jqxhr).then(function (data) {
        for (var index = 0; index < data.length; index++) {
            var country = data[index].value;

            countries.append('<label class="checkbox-inline">\n\
                                <input class="countrycb" type="checkbox" ' + ' name="' + country + '"/>' + country + '\
                                </label>');
        }
        $("[class='countrycb']").off("click").click(function () {
            //uncheck "select all", if one of the listed checkbox item is unchecked
            if (false == $(this).prop("checked")) { //if this item is unchecked
                $("#select_all").prop('checked', false); //change "select all" checked status to false
            }
            //check "select all" if all checkbox items are checked
            if ($("[class='countrycb']:checked").length == $("[class='countrycb']").length) {
                $("#select_all").prop('checked', true);
            }
        });

        if (!(testCase === undefined)) {
            // Init the values from the object value.
            for (var myCountry in testCase.countries) {
                $("#countries [name='" + testCase.countries[myCountry].value + "']").prop("checked", "checked");
            }
        }
        if (testCase === undefined) {
            $("#countries input").attr('checked', true);
            $("#select_all").attr('checked', true);
        }

        if (isReadOnly) {
            $("#countries input").attr('disabled', true);
            $("#select_all").attr('disabled', true);
        }
    });
}

/***
 * Build the list of label and flag them from the testcase values..
 * @param {String} labels - list of labels from the testcase to flag. Label in that list are displayed first. This is optional.
 * @param {String} mySystem - system that will be used in order to load the label list. if not feed, the default system from user will be used.
 * @param {String} myLabelDiv - Reference of the div where the label will be added. Ex : "#selectLabel".
 * @param {String} labelSize - size of col-xs-?? from 1 to 12. Default to 2 Ex : "4".
 * @param {String} test - Test Folder to Select.
 * @param {String} testCase - Test ID to Select.
 * @returns {null}
 */
function loadLabel(labels, mySystem, myLabelDiv, labelSize, test, testCase) {

    if (isEmpty(labelSize)) {
        labelSize = "2";
    }
    var labelDiv = myLabelDiv;
    var targetSystem = mySystem;
    if (isEmpty(targetSystem)) {
        targetSystem = getUser().defaultSystem;
    }

    var jqxhr = $.get("ReadLabel?system=" + targetSystem + "&withHierarchy=true&isSelectable=Y&testSelect=" + encodeURI(test) + "&testCaseSelect=" + encodeURI(testCase), "", "json");

    $.when(jqxhr).then(function (data) {
        var messageType = getAlertType(data.messageType);

        //DRAW LABEL LIST
        if (messageType === "success") {

            //DRAW LABEL TREE

            $(labelDiv + 'S').treeview({data: data.labelHierarchy.stickers, enableLinks: false, showTags: true, multiSelect: true});
            $(labelDiv + 'B').treeview({data: data.labelHierarchy.batteries, enableLinks: false, showTags: true, multiSelect: true});
            $(labelDiv + 'R').treeview({data: data.labelHierarchy.requirements, enableLinks: false, showTags: true, multiSelect: true});

            $(labelDiv + 'S').treeview('expandAll', {levels: 20, silent: true});
            $(labelDiv + 'B').treeview('expandAll', {levels: 20, silent: true});
            $(labelDiv + 'R').treeview('expandAll', {levels: 20, silent: true});

        } else {
            showMessageMainPage(messageType, data.message, true);
        }

    }).fail(handleErrorAjaxAfterTimeout);
}

function appendApplicationList(defautValue, mySystem, modalId) {

    $("[name=application]").empty();

    var targetSystem = mySystem;
    if (isEmpty(targetSystem)) {
        targetSystem = getUser().defaultSystem;
    }

    var jqxhr = $.getJSON("ReadApplication", "q=1" + getUser().systemQuery);
    $.when(jqxhr).then(function (data) {
        var applicationList = $("[name=application]");

        for (var index = 0; index < data.contentTable.length; index++) {
            if (data.contentTable[index].system === targetSystem) {
                applicationList.prepend($('<option></option>').addClass('bold-option').text(data.contentTable[index].application + " [" + data.contentTable[index].type + "]").val(data.contentTable[index].application));
            } else {
                applicationList.append($('<option></option>').text(data.contentTable[index].application + " [" + data.contentTable[index].type + "]").val(data.contentTable[index].application));
            }
        }
        $("#editTestCaseModalForm #application").val(defautValue);

        $('#' + modalId + " #editApplication").off("click");
        $('#' + modalId + " #editApplication").click(function () {
            openModalApplication($("#editTestCaseModalForm #application").val(), "EDIT");
        });


    });
}

function appendTestList(defautValue) {
    $('#editTestCaseModal [name="test"]').empty();
    $('#editTestCaseModal [name="test"]').select2({...getComboConfigTest(), dropdownParent: $('#editTestCaseModal')});
//    fillTestAndTestCaseSelect("#test", undefined, undefined, undefined, true)


    // Set Select2 Value.
    var myoption = $('<option></option>').text(defautValue).val(defautValue);
    $("#editTestCaseModal [name=test]").append(myoption).trigger('change'); // append the option and update Select2

}

function toggleIsMuted() {
    if ($("#editTestCaseModalForm").find("#isMuted .glyphicon").hasClass("glyphicon-volume-off")) {
        $("#editTestCaseModalForm").find("#isMuted .glyphicon").removeClass("glyphicon-volume-off").addClass("glyphicon-volume-up");
    } else {
        $("#editTestCaseModalForm").find("#isMuted .glyphicon").removeClass("glyphicon-volume-up").addClass("glyphicon-volume-off");
    }
}
