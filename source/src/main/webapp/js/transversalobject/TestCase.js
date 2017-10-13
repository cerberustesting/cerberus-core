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
 * Open the modal with testcase information.
 * @param {String} test - id of the test to open the modal
 * @param {String} testcase - id of the testcase to open the modal
 * @param {String} mode - mode to open the modal. Can take the values : ADD, DUPLICATE, EDIT
 * @returns {null}
 */
function openModalTestCase(test, testcase, mode) {

    // We only load the Labels and bind the events once for performance optimisations.
    if ($('#editTestCaseModal').data("initLabel") === undefined) {
        initModalTestCase();
        $('#editTestCaseModal').data("initLabel", true);
    }
    // Init the Saved data to false.
    $('#editTestCaseModal').data("Saved", false);
    $('#editTestCaseModal').data("testcase", undefined);

    if (mode === "EDIT") {
        editTestCaseClick(test, testcase);
    } else if (mode === "DUPLICATE") {
        duplicateTestCaseClick(test, testcase);
    } else {
        addTestCaseClick(test, "ADD");
    }
    $('#editTestCaseModalForm #application').parents("div.form-group").removeClass("has-error");
    clearResponseMessage($('#editTestCaseModal'));
}

function initModalTestCase(doc) {
    var doc = new Doc();

    tinymce.init({
        selector: ".wysiwyg"
    });

    $("[name='testField']").html(doc.getDocOnline("test", "Test"));
    $("[name='testCaseField']").html(doc.getDocOnline("testcase", "TestCase"));
    $("[name='lastModifierField']").html(doc.getDocOnline("testcase", "LastModifier"));
    $("[name='originField']").html(doc.getDocOnline("testcase", "Origine"));
    $("[name='refOriginField']").html(doc.getDocOnline("testcase", "RefOrigine"));
    $("[name='projectField']").html(doc.getDocOnline("project", "idproject"));
    $("[name='ticketField']").html(doc.getDocOnline("testcase", "ticket"));
    $("[name='functionField']").html(doc.getDocOnline("testcase", "Function"));
    $("[name='applicationField']").html(doc.getDocOnline("application", "Application"));
    $("[name='statusField']").html(doc.getDocOnline("testcase", "Status"));
    $("[name='bugIdField']").html(doc.getDocOnline("testcase", "BugID"));
    $("[name='actQAField']").html(doc.getDocOnline("testcase", "activeQA"));
    $("[name='actUATField']").html(doc.getDocOnline("testcase", "activeUAT"));
    $("[name='actUATField']").html(doc.getDocOnline("testcase", "activeUAT"));
    $("[name='actProdField']").html(doc.getDocOnline("testcase", "activePROD"));
    $("[name='shortDescField']").html(doc.getDocOnline("testcase", "Description"));
    $("[name='behaviorOrValueExpectedField']").html(doc.getDocOnline("testcase", "BehaviorOrValueExpected"));
    $("[name='shortDescField']").html(doc.getDocOnline("testcase", "Description"));
    $("[name='howToField']").html(doc.getDocOnline("testcase", "HowTo"));
    $("[name='descriptionField']").html(doc.getDocOnline("test", "Description"));
    $("[name='creatorField']").html(doc.getDocOnline("testcase", "Creator"));
    $("[name='implementerField']").html(doc.getDocOnline("testcase", "Implementer"));
    $("[name='groupField']").html(doc.getDocOnline("invariant", "GROUP"));
    $("[name='priorityField']").html(doc.getDocOnline("invariant", "PRIORITY"));
    $("[name='countryList']").html(doc.getDocOnline("testcase", "countryList"));
    $("[name='bugIdField']").html(doc.getDocOnline("testcase", "BugID"));
    $("[name='tcDateCreaField']").html(doc.getDocOnline("testcase", "TCDateCrea"));
    $("[name='activeField']").html(doc.getDocOnline("testcase", "TcActive"));
    $("[name='fromSprintField']").html(doc.getDocOnline("testcase", "FromBuild"));
    $("[name='fromRevField']").html(doc.getDocOnline("testcase", "FromRev"));
    $("[name='toSprintField']").html(doc.getDocOnline("testcase", "ToBuild"));
    $("[name='toRevField']").html(doc.getDocOnline("testcase", "ToRev"));
    $("[name='targetSprintField']").html(doc.getDocOnline("testcase", "TargetBuild"));
    $("[name='targetRevField']").html(doc.getDocOnline("testcase", "TargetRev"));
    $("[name='conditionOperField']").html(doc.getDocOnline("testcase", "ConditionOper"));
    $("[name='conditionVal1Field']").html(doc.getDocOnline("testcase", "ConditionVal1"));
    $("[name='conditionVal2Field']").html(doc.getDocOnline("testcase", "ConditionVal2"));
    $("[name='commentField']").html(doc.getDocOnline("testcase", "Comment"));
    $("#filters").html(doc.getDocOnline("page_testcaselist", "filters"));
    $("[name='btnLoad']").html(doc.getDocLabel("page_global", "buttonLoad"));
    $("[name='testField']").html(doc.getDocLabel("test", "Test"));
    $("[name='editEntryField']").html(doc.getDocLabel("page_testcaselist", "btn_edit"));
    $("[name='addEntryField']").html(doc.getDocLabel("page_testcaselist", "btn_create"));
    $("[name='linkField']").html(doc.getDocLabel("page_testcaselist", "link"));
    //TABs
    $("[name='testInfoField']").html(doc.getDocLabel("page_testcaselist", "testInfo"));
    $("[name='testCaseInfoField']").html(doc.getDocLabel("page_testcaselist", "testCaseInfo"));
    $("[name='testCaseParameterField']").html(doc.getDocLabel("page_testcaselist", "testCaseParameter"));
    $("[name='activationCriteriaField']").html(doc.getDocLabel("page_testcaselist", "activationCriteria"));
    // Tracability
    $("[name='lbl_datecreated']").html(doc.getDocOnline("transversal", "DateCreated"));
    $("[name='lbl_usrcreated']").html(doc.getDocOnline("transversal", "UsrCreated"));
    $("[name='lbl_datemodif']").html(doc.getDocOnline("transversal", "DateModif"));
    $("[name='lbl_usrmodif']").html(doc.getDocOnline("transversal", "UsrModif"));

    displayInvariantList("group", "GROUP", false);
    displayInvariantList("status", "TCSTATUS", false);
    displayInvariantList("priority", "PRIORITY", false);
    displayInvariantList("conditionOper", "TESTCASECONDITIONOPER", false);
    $('[name="origin"]').append('<option value="All">All</option>');
    displayInvariantList("origin", "ORIGIN", true);
    displayInvariantList("active", "TCACTIVE", false);
    displayInvariantList("activeQA", "TCACTIVE", false);
    displayInvariantList("activeUAT", "TCACTIVE", false);
    displayInvariantList("activeProd", "TCACTIVE", false);
    appendProjectList();

    var availableUserAgent = getInvariantArray("USERAGENT", false);
    $('#editTestCaseModal').find("#userAgent").autocomplete({
        source: availableUserAgent
    });
    var availableScreenSize = getInvariantArray("SCREENSIZE", false);
    $('#editTestCaseModal').find("#screenSize").autocomplete({
        source: availableScreenSize
    });
    var availableFunctions = getInvariantArray("FUNCTION", false);
    $('#editTestCaseModal').find("#function").autocomplete({
        source: availableFunctions
    });


}

/***
 * Open the modal with testcase information.
 * @param {String} test - type selected
 * @param {String} testCase - type selected
 * @returns {null}
 */
function editTestCaseClick(test, testCase) {
    $("#buttonInvert").off("click");
    $("#buttonInvert").click(function () {
        invertCountrySelection();
    });
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

    // In Edit TestCase form, if we change the test, we get the latest testcase from that test.
    $('#editTestCaseModalForm select[name="test"]').off("change");
    $('#editTestCaseModalForm select[name="test"]').change(function () {
        feedTestCaseField(null, "editTestCaseModalForm");
        // Compare with original value in order to display the warning message.
        displayWarningOnChangeTestCaseKey();
    });
    $('#editTestCaseModalForm input[name="testCase"]').off("change");
    $('#editTestCaseModalForm input[name="testCase"]').change(function () {
        // Compare with original value in order to display the warning message.
        displayWarningOnChangeTestCaseKey();
    });
    feedTestCaseModal(test, testCase, "editTestCaseModal", "EDIT");
}

function displayWarningOnChangeTestCaseKey() {
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
    $("#buttonInvert").off("click");
    $("#buttonInvert").click(function () {
        invertCountrySelection();
    });
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
    $('#editTestCaseModalForm select[name="test"]').off("change");
    $('#editTestCaseModalForm select[name="test"]').change(function () {
        feedTestCaseField(null, "editTestCaseModalForm");
    });

    // In Add and duplicate TestCase form, if we change the test, we don't display any warning.
    $('#editTestCaseModalForm select[name="test"]').off("change");
    $('#editTestCaseModalForm select[name="test"]').change(function () {
        feedTestCaseField(null, "editTestCaseModalForm");
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
    $("#buttonInvert").off("click");
    $("#buttonInvert").click(function () {
        invertCountrySelection();
    });
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

    $('#editTestCaseModalForm select[name="test"]').off("change");
    $('#editTestCaseModalForm select[name="test"]').change(function () {
        feedTestCaseField(null, "editTestCaseModalForm");
    });

    // In Add and duplicate TestCase form, if we change the test, we don't display any warning.
    $('#editTestCaseModalForm select[name="test"]').off("change");
    $('#editTestCaseModalForm select[name="test"]').change(function () {
        feedTestCaseField(null, "editTestCaseModalForm");
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
// Predefine the testcase value.
    if ((test === null) || (test === undefined))
        test = $('#' + modalForm + ' select[name="test"]').val();
    $.ajax({
        url: "ReadTestCase",
        method: "GET",
        data: {test: encodeURIComponent(test), getMaxTC: true},
        dataType: "json",
        success: function (data) {
            var testCaseNumber = data.maxTestCase + 1;
            var tcnumber;

            if (testCaseNumber < 10) {
                tcnumber = "000" + testCaseNumber.toString() + "A";
            } else if (testCaseNumber >= 10 && testCaseNumber < 99) {
                tcnumber = "00" + testCaseNumber.toString() + "A";
            } else if (testCaseNumber >= 100 && testCaseNumber < 999) {
                tcnumber = "0" + testCaseNumber.toString() + "A";
            } else if (testCaseNumber >= 1000) {
                tcnumber = testCaseNumber.toString() + "A";
            } else {
                tcnumber = "0001A";
            }
            $('#' + modalForm + ' [name="testCase"]').val(tcnumber);
        },
        error: showUnexpectedError
    });

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
    if (nameElementEmpty) {
        var localMessage = new Message("danger", "Please specify the name of the application!");
        nameElement.parents("div.form-group").addClass("has-error");
        showMessage(localMessage, $('#editTestCaseModal'));
    } else {
        nameElement.parents("div.form-group").removeClass("has-error");
    }

    // verif if all mendatory fields are not empty
    if (nameElementEmpty)
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
    var table1 = $("#testCaseCountryTableBody tr td");
    var table_country = [];
    for (var i = 0; i < table1.length; i++) {
        table_country.push($(table1[i]).data("country"));
    }

    // Getting Data from Label List
    var table2 = $("input[name=labelid]:checked");
    var table_label = [];
    for (var i = 0; i < table2.length; i++) {
        var newLabel1 = {
            labelId: $(table2[i]).val(),
            toDelete: false
        };
        table_label.push(newLabel1);
    }

    // Get the header data from the form.
    var data = convertSerialToJSONObject(formEdit.serialize());

    showLoaderInModal('#editTestCaseModal');
    $.ajax({
        url: myServlet,
        async: true,
        method: "POST",
        data: {test: data.test,
            testCase: data.testCase,
            originalTest: data.originalTest,
            originalTestCase: data.originalTestCase,
            active: data.active,
            activeProd: data.activeProd,
            activeQA: data.activeQA,
            activeUAT: data.activeUAT,
            application: data.application,
            behaviorOrValueExpected: data.behaviorOrValueExpected,
            bugId: data.bugId,
            comment: data.comment,
            fromRev: data.fromRev,
            fromSprint: data.fromSprint,
            function: data.function,
            group: data.group,
            howTo: data.howTo,
            implementer: data.implementer,
            origin: data.origin,
            priority: data.priority,
            project: data.project,
            refOrigin: data.refOrigin,
            shortDesc: data.shortDesc,
            status: data.status,
            targetRev: data.targetRev,
            targetSprint: data.targetSprint,
            conditionOper: data.conditionOper,
            conditionVal1: data.conditionVal1,
            conditionVal2: data.conditionVal2,
            ticket: data.ticket,
            toRev: data.toRev,
            toSprint: data.toSprint,
            userAgent: data.userAgent,
            screenSize: data.screenSize,
            labelList: JSON.stringify(table_label),
            countryList: JSON.stringify(table_country)},
        success: function (dataMessage) {
            hideLoaderInModal('#editTestCaseModal');
            if (getAlertType(dataMessage.messageType) === "success") {
                var oTable = $("#testCaseTable").dataTable();
                oTable.fnDraw(true);
                $('#editTestCaseModal').data("Saved", true);
                $('#editTestCaseModal').data("testcase", data);
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

    appendBuildRevListOnTestCase(getUser().defaultSystem, undefined);

    feedTestCaseData(undefined, modalId, "ADD", true, defaultTest);
    // Labels
    loadLabel(undefined, undefined, "#selectLabel");
    //Application Combo
    appendApplicationList(undefined, undefined);

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

        var testCase = data.contentTable;

        var appInfo = $.getJSON("ReadApplication", "application=" + encodeURIComponent(testCase.application));

        $.when(appInfo).then(function (appData) {
            var currentSys = getUser().defaultSystem;
            var bugTrackerUrl = appData.contentTable.bugTrackerUrl;

            // Loading build and revision various combos.
            appendBuildRevListOnTestCase(appData.contentTable.system, testCase);
            // Title of the label list.
            $("[name='labelField']").html("Labels from system : " + appData.contentTable.system);
            // Loading the label list from aplication of the testcase.
            loadLabel(testCase.labelList, appData.contentTable.system, "#selectLabel");
            // Loading application combo from the system of the current application.
            appendApplicationList(testCase.application, appData.contentTable.system);

            var newbugTrackerUrl = "";
            if (testCase.bugID !== "" && bugTrackerUrl) {
                newbugTrackerUrl = bugTrackerUrl.replace("%BUGID%", testCase.bugID);
            }
            formEdit.find("#link").prop("href", newbugTrackerUrl).text(testCase.bugID);
            formEdit.find("#link").prop("target", "_blank");

            formEdit.find("#bugId").change(function () {
                var newbugid = formEdit.find("#bugId").val();
                var newbugTrackerUrl = "";
                if (newbugid !== "" && bugTrackerUrl) {
                    newbugTrackerUrl = bugTrackerUrl.replace("%BUGID%", newbugid);
                }
                formEdit.find("#link").prop("href", newbugTrackerUrl).text(newbugid);
                formEdit.find("#link").prop("target", "_blank");
            });
        });

        feedTestCaseData(testCase, modalId, mode, data["hasPermissionsUpdate"]);

        formEdit.modal('show');
    });

}

function feedTestCaseData(testCase, modalId, mode, hasPermissionsUpdate, defaultTest) {
    var formEdit = $('#' + modalId);
    var doc = new Doc();

    // Data Feed.
    if (mode === "EDIT") {
        $("[name='editTestCaseField']").html(doc.getDocOnline("page_testcaselist", "btn_edit"));
        appendTestList(testCase.test);
        formEdit.find("#testCase").prop("value", testCase.testCase);
        formEdit.find("#status").prop("value", testCase.status);
        formEdit.find("#usrcreated").prop("value", testCase.usrCreated);
        formEdit.find("#datecreated").prop("value", testCase.dateCreated);
        formEdit.find("#usrmodif").prop("value", testCase.usrModif);
        formEdit.find("#datemodif").prop("value", testCase.dateModif);
        formEdit.find("#actProd").val(testCase.activePROD);
    } else { // DUPLICATE or ADD
        formEdit.find("#usrcreated").prop("value", "");
        formEdit.find("#datecreated").prop("value", "");
        formEdit.find("#usrmodif").prop("value", "");
        formEdit.find("#datemodif").prop("value", "");
        formEdit.find("#actProd").val("N");
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
    if (isEmpty(testCase)) {
        formEdit.find("#originalTest").prop("value", "");
        formEdit.find("#originalTestCase").prop("value", "");
        formEdit.find("#implementer").prop("value", "");
        formEdit.find("#origin").prop("value", "");
        formEdit.find("#refOrigin").prop("value", "");
        formEdit.find("#project").prop("value", "");
        formEdit.find("#ticket").prop("value", "");
        formEdit.find("#function").prop("value", "");
        formEdit.find("#group").val("AUTOMATED");
        formEdit.find("#priority option:nth(0)").attr("selected", "selected");
        formEdit.find("#actQA").val("Y");
        formEdit.find("#actUAT").val("Y");
        formEdit.find("#userAgent").prop("value", "");
        formEdit.find("#screenSize").prop("value", "");
        formEdit.find("#shortDesc").prop("value", "");
        if (tinyMCE.get('behaviorOrValueExpected') != null)
            tinyMCE.get('behaviorOrValueExpected').setContent("");
        if (tinyMCE.get('howTo') != null)
            formEdit.find("#active").prop("value", "Y");
        formEdit.find("#bugId").prop("value", "");
        formEdit.find("#conditionOper").prop("value", "always");
        formEdit.find("#conditionVal1").prop("value", "");
        formEdit.find("#conditionVal2").prop("value", "");
        formEdit.find("#comment").prop("value", "");
    } else {
        formEdit.find("#test").prop("value", testCase.test);
        formEdit.find("#originalTest").prop("value", testCase.test);
        formEdit.find("#originalTestCase").prop("value", testCase.testCase);
        formEdit.find("#newTest").prop("value", testCase.test);
        formEdit.find("#implementer").prop("value", testCase.implementer);
        formEdit.find("#tcDateCrea").prop("value", testCase.dateCreated);
        formEdit.find("#origin").prop("value", testCase.origine);
        formEdit.find("#refOrigin").prop("value", testCase.refOrigine);
        formEdit.find("#project").prop("value", testCase.project);
        formEdit.find("#ticket").prop("value", testCase.ticket);
        formEdit.find("#function").prop("value", testCase.function);
        formEdit.find("#group").prop("value", testCase.group);
        formEdit.find("#priority").prop("value", testCase.priority);
        formEdit.find("#actQA").prop("value", testCase.activeQA);
        formEdit.find("#actUAT").prop("value", testCase.activeUAT);
        formEdit.find("#userAgent").prop("value", testCase.userAgent);
        formEdit.find("#screenSize").prop("value", testCase.screenSize);
        formEdit.find("#shortDesc").prop("value", testCase.description);
        if (tinyMCE.get('behaviorOrValueExpected') != null)
            tinyMCE.get('behaviorOrValueExpected').setContent(testCase.behaviorOrValueExpected);
        if (tinyMCE.get('howTo') != null)
            tinyMCE.get('howTo').setContent(testCase.howTo);
        formEdit.find("#active").prop("value", testCase.tcActive);
        formEdit.find("#bugId").prop("value", testCase.bugID);
        formEdit.find("#conditionOper").prop("value", testCase.conditionOper);
        formEdit.find("#conditionVal1").prop("value", testCase.conditionVal1);
        formEdit.find("#conditionVal2").prop("value", testCase.conditionVal2);
        formEdit.find("#comment").prop("value", testCase.comment);
    }

    // Authorities

    //We define here the rule that enable or nt the fields depending on if user has the credentials to edit.
    var doBloackAllFields = false;
    if (mode === "EDIT") {
        doBloackAllFields = !(hasPermissionsUpdate);
    } else { // DUPLICATE or ADD
        doBloackAllFields = false;
    }

    if (doBloackAllFields) { // If readonly, we only readonly all fields
        //test case info
        formEdit.find("#test").prop("disabled", "disabled");
        formEdit.find("#testCase").prop("readonly", "readonly");
        formEdit.find("#implementer").prop("readonly", "readonly");
        formEdit.find("#origin").prop("disabled", "disabled");
        formEdit.find("#project").prop("disabled", "disabled");
        formEdit.find("#ticket").prop("readonly", "readonly");
        formEdit.find("#function").prop("readonly", "readonly");
        formEdit.find("#application").prop("disabled", "disabled");
        formEdit.find("#status").prop("disabled", "disabled");
        formEdit.find("#group").prop("disabled", "disabled");
        formEdit.find("#priority").prop("disabled", "disabled");
        formEdit.find("#actQA").prop("disabled", "disabled");
        formEdit.find("#actUAT").prop("disabled", "disabled");
        formEdit.find("#actProd").prop("disabled", "disabled");
        formEdit.find("#userAgent").prop("disabled", "disabled");
        formEdit.find("#screenSize").prop("disabled", "disabled");
        formEdit.find("#shortDesc").prop("readonly", "readonly");
        if (tinyMCE.get('behaviorOrValueExpected') !== null)
            tinyMCE.get('behaviorOrValueExpected').getBody().setAttribute('contenteditable', false);
        if (tinyMCE.get('howTo') !== null)
            tinyMCE.get('howTo').getBody().setAttribute('contenteditable', false);
        formEdit.find("#active").prop("disabled", "disabled");
        formEdit.find("#fromSprint").prop("disabled", "disabled");
        formEdit.find("#fromRev").prop("disabled", "disabled");
        formEdit.find("#toSprint").prop("disabled", "disabled");
        formEdit.find("#toRev").prop("disabled", "disabled");
        formEdit.find("#targetSprint").prop("disabled", "disabled");
        formEdit.find("#targetRev").prop("disabled", "disabled");
        formEdit.find("#conditionOper").prop("disabled", "disabled");
        formEdit.find("#conditionVal1").prop("disabled", "disabled");
        formEdit.find("#conditionVal2").prop("disabled", "disabled");
        formEdit.find("#bugId").prop("readonly", "readonly");
        formEdit.find("#comment").prop("readonly", "readonly");
        // feed the country list.
        appendTestCaseCountryList(testCase, true);
        // Save button is hidden.
        $('#editTestCaseButton').attr('class', '');
        $('#editTestCaseButton').attr('hidden', 'hidden');
    } else {
        //test case info
        formEdit.find("#test").removeAttr("disabled");
        formEdit.find("#testCase").removeAttr("readonly");
        formEdit.find("#active").removeProp("disabled");
        formEdit.find("#bugId").removeProp("readonly");
        formEdit.find("#implementer").removeProp("readonly");
        formEdit.find("#origin").removeProp("disabled");
        formEdit.find("#project").removeProp("disabled");
        formEdit.find("#ticket").removeProp("readonly");
        formEdit.find("#function").removeProp("readonly");
        formEdit.find("#application").removeProp("disabled");
        formEdit.find("#status").removeProp("disabled");
        formEdit.find("#group").removeProp("disabled");
        formEdit.find("#priority").removeProp("disabled");
        formEdit.find("#actQA").removeProp("disabled");
        formEdit.find("#actUAT").removeProp("disabled");
        formEdit.find("#actProd").removeProp("disabled");
        formEdit.find("#userAgent").removeProp("disabled");
        formEdit.find("#screenSize").removeProp("disabled");
        formEdit.find("#shortDesc").removeProp("readonly");
        if (tinyMCE.get('behaviorOrValueExpected') !== null)
            tinyMCE.get('behaviorOrValueExpected').getBody().setAttribute('contenteditable', true);
        if (tinyMCE.get('howTo') !== null)
            tinyMCE.get('howTo').getBody().setAttribute('contenteditable', true);
        formEdit.find("#active").removeProp("disabled");
        formEdit.find("#fromSprint").removeProp("disabled");
        formEdit.find("#fromRev").removeProp("disabled");
        formEdit.find("#toSprint").removeProp("disabled");
        formEdit.find("#toRev").removeProp("disabled");
        formEdit.find("#targetSprint").removeProp("disabled");
        formEdit.find("#targetRev").removeProp("disabled");
        formEdit.find("#conditionOper").removeProp("disabled");
        formEdit.find("#conditionVal1").removeProp("disabled");
        formEdit.find("#conditionVal2").removeProp("disabled");
        formEdit.find("#bugId").removeProp("readonly");
        formEdit.find("#comment").removeProp("readonly");
        // feed the country list.
        appendTestCaseCountryList(testCase, false);
    }

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
        var fromBuild = $("[name=fromSprint]");
        var toBuild = $("[name=toSprint]");
        var targetBuild = $("[name=targetSprint]");

        fromBuild.empty();
        toBuild.empty();
        targetBuild.empty();

        fromBuild.append($('<option></option>').text("-----").val(""));
        toBuild.append($('<option></option>').text("-----").val(""));
        targetBuild.append($('<option></option>').text("-----").val(""));

        for (var index = 0; index < data.contentTable.length; index++) {
            fromBuild.append($('<option></option>').text(data.contentTable[index].versionName).val(data.contentTable[index].versionName));
            toBuild.append($('<option></option>').text(data.contentTable[index].versionName).val(data.contentTable[index].versionName));
            targetBuild.append($('<option></option>').text(data.contentTable[index].versionName).val(data.contentTable[index].versionName));
        }

        if (editData !== undefined) {
            var formEdit = $('#editTestCaseModal');

            formEdit.find("#fromSprint").prop("value", editData.fromBuild);
            formEdit.find("#toSprint").prop("value", editData.toBuild);
            formEdit.find("#targetSprint").prop("value", editData.targetBuild);
        }

    });

    var jqxhr = $.getJSON("ReadBuildRevisionInvariant", "system=" + encodeURIComponent(system) + "&level=2");
    $.when(jqxhr).then(function (data) {
        var fromRev = $("[name=fromRev]");
        var toRev = $("[name=toRev]");
        var targetRev = $("[name=targetRev]");

        fromRev.empty();
        toRev.empty();
        targetRev.empty();

        fromRev.append($('<option></option>').text("-----").val(""));
        toRev.append($('<option></option>').text("-----").val(""));
        targetRev.append($('<option></option>').text("-----").val(""));

        for (var index = 0; index < data.contentTable.length; index++) {
            fromRev.append($('<option></option>').text(data.contentTable[index].versionName).val(data.contentTable[index].versionName));
            toRev.append($('<option></option>').text(data.contentTable[index].versionName).val(data.contentTable[index].versionName));
            targetRev.append($('<option></option>').text(data.contentTable[index].versionName).val(data.contentTable[index].versionName));
        }

        if (editData !== undefined) {
            var formEdit = $('#editTestCaseModal');

            formEdit.find("[name=fromRev]").prop("value", editData.fromRev);
            formEdit.find("[name=toRev]").prop("value", editData.toRev);
            formEdit.find("[name=targetRev]").prop("value", editData.targetRev);
        }
    });
}

function appendTestCaseCountryList(testCase, isReadOnly) {
    $("#testCaseCountryTableBody tr").empty();

    var selectCountry = getParameter("cerberus_testcase_defaultselectedcountry", getUser().defaultSystem, false);
    var selectCountryVal = "," + selectCountry.value + ",";

    var jqxhr = $.getJSON("FindInvariantByID", "idName=COUNTRY");
    $.when(jqxhr).then(function (data) {

        for (var index = 0; index < data.length; index++) {
            var country = data[index].value;
            var deleteOpt = true;

            var newCountry1 = {
                country: country,
                toDelete: deleteOpt
            };

            if (testCase === undefined) {
                if ((selectCountryVal === ',ALL,') || (selectCountryVal.indexOf("," + country + ",") !== -1)) {
                    deleteOpt = false;
                } else {
                    deleteOpt = true;
                }
                newCountry1.toDelete = deleteOpt;
            }
            appendTestCaseCountryCell(newCountry1, isReadOnly);
        }

        if (!(testCase === undefined)) {
            // Init the values from the object value.
            for (var myCountry in testCase.countryList) {
                $("#testCaseCountryTableBody [value='" + testCase.countryList[myCountry].country + "']").trigger("click");
            }
        }

    });
}

function appendTestCaseCountryCell(testCaseCountry, isReadOnly) {
    var doc = new Doc();
    var btnid = "btn_" + testCaseCountry.country;
    if (isReadOnly) {
        var checkBox = $("<button id=\"" + btnid + "\" type=\"button\" disabled=\"disabled\"></button>").append(testCaseCountry.country).val(testCaseCountry.country);
    } else {
        var checkBox = $("<button id=\"" + btnid + "\" type=\"button\"></button>").append(testCaseCountry.country).val(testCaseCountry.country);
    }
    var tableRow = $("#testCaseCountryTableBody tr");

    var checkBoxCell = $("<td align=\"center\"></td>").append(checkBox);
    if (testCaseCountry.toDelete) {
        checkBoxCell.addClass("danger");
    } else {
        checkBoxCell.removeClass("danger");
    }

    checkBox.click(function () {
        testCaseCountry.toDelete = (testCaseCountry.toDelete) ? false : true;
        if (testCaseCountry.toDelete) {
            checkBoxCell.addClass("danger");
        } else {
            checkBoxCell.removeClass("danger");
        }
    });

    checkBoxCell.data("country", testCaseCountry);
    tableRow.append(checkBoxCell);
}

function invertCountrySelection() {
    var jqxhr = $.getJSON("FindInvariantByID", "idName=COUNTRY");
    $.when(jqxhr).then(function (data) {

        for (var index = 0; index < data.length; index++) {
            var country = data[index].value;
            document.getElementById('btn_' + country).click();
        }
    });
}

/***
 * Build the list of label and flag them from the testcase values..
 * @param {String} labelList - list of labels from the testcase to flag. Label in that list are displayed first. This is optional.
 * @param {String} mySystem - system that will be used in order to load the label list. if not feed, the default system from user will be used.
 * @param {String} myLabelDiv - Reference of the div where the label will be added. Ex : "#selectLabel".
 * @param {String} labelSize - size of col-xs-?? from 1 to 12. Default to 2 Ex : "4".
 * @returns {null}
 */
function loadLabel(labelList, mySystem, myLabelDiv, labelSize) {

    if (isEmpty(labelSize)) {
        labelSize = "2";
    }
    var labelDiv = myLabelDiv;
    var targetSystem = mySystem;
    if (isEmpty(targetSystem)) {
        targetSystem = getUser().defaultSystem;
    }

    var jqxhr = $.get("ReadLabel?system=" + targetSystem, "", "json");

    $.when(jqxhr).then(function (data) {
        var messageType = getAlertType(data.messageType);
        //DRAW LABEL LIST
        if (messageType === "success") {
            $(labelDiv).empty();
            var index;
            for (index = 0; index < data.contentTable.length; index++) {
                //the character " needs a special encoding in order to avoid breaking the string that creates the html element   
                var labelTag = '<div style="float:left" align="center"><input name="labelid" id="labelId' + data.contentTable[index].id + '" value="' + data.contentTable[index].id + '" type="checkbox">\n\
                <span class="label label-primary" style="cursor:pointer;background-color:' + data.contentTable[index].color + '">' + data.contentTable[index].label + '</span></div> ';
                var option = $('<div style="float:left; height:60px" name="itemLabelDiv" id="itemLabelId' + data.contentTable[index].id + '" class="col-xs-' + labelSize + ' list-group-item list-label"></div>')
                        .attr("value", data.contentTable[index].label).html(labelTag);
                if (data.contentTable[index].system === targetSystem) {
                    $(labelDiv).prepend(option);
                } else {
                    $(labelDiv).append(option);
                }
            }
        } else {
            showMessageMainPage(messageType, data.message, true);
        }
        // Put the selected testcaselabel at the top and check them. 
        if (!(isEmpty(labelList))) {
            var index;
            for (index = 0; index < labelList.length; index++) {
                //For each testcaselabel, put at the top of the list and check them
                var element = $("#itemLabelId" + labelList[index].label.id);
                element.remove();
                $(labelDiv).prepend(element);
                $("#labelId" + labelList[index].label.id).prop("checked", true);
            }
        }
        //ADD CLICK EVENT ON LABEL
        $(labelDiv).find('span').click(function () {
            var status = $(this).parent().find("input").prop('checked');
            $(this).parent().find("input").prop('checked', !status);
        });
    }).fail(handleErrorAjaxAfterTimeout);
}

function appendApplicationList(defautValue, mySystem) {

    $("[name=application]").empty();

    var targetSystem = mySystem;
    if (isEmpty(targetSystem)) {
        targetSystem = getUser().defaultSystem;
    }

    var jqxhr = $.getJSON("ReadApplication");
    $.when(jqxhr).then(function (data) {
        var applicationList = $("[name=application]");

        for (var index = 0; index < data.contentTable.length; index++) {
            if (data.contentTable[index].system === targetSystem) {
                applicationList.prepend($('<option></option>').addClass('bold-option').text(data.contentTable[index].application).val(data.contentTable[index].application));
            } else {
                applicationList.append($('<option></option>').text(data.contentTable[index].application).val(data.contentTable[index].application));
            }
        }
        $("#application").val(defautValue);
    });
}

function appendTestList(defautValue) {

    var user = getUser();
    $("#editTestCaseModal [name=test]").empty();

    var jqxhr = $.getJSON("ReadTest", "");
    $.when(jqxhr).then(function (data) {
        var testList = $("[name=test]");

        for (var index = 0; index < data.contentTable.length; index++) {
            testList.append($('<option></option>').text(data.contentTable[index].test).val(data.contentTable[index].test));
        }
        testList.val(defautValue);

    });
}

function appendProjectList() {
    var jqxhr = $.getJSON("ReadProject");
    $.when(jqxhr).then(function (data) {
        var projectList = $("[name=project]");

        projectList.append($('<option></option>').text("No project defined").val(""));
        for (var index = 0; index < data.contentTable.length; index++) {
            var idProject = data.contentTable[index].idProject;
            var desc = data.contentTable[index].description;

            projectList.append($('<option></option>').text(idProject + " " + desc).val(idProject));
        }
    });
}
