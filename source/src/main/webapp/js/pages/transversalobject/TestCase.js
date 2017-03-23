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

function displayTestCaseLabel(doc) {
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
    $("#testCaseListLabel").html(doc.getDocOnline("page_testcaselist", "testcaselist"));
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

    feedTestCaseModal(test, testCase, "editTestCaseModal", "EDIT");
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
    $('#editTestCaseModalForm select[name="test"]').off("change");
    $('#editTestCaseModalForm select[name="test"]').change(function () {
        feedTestCaseField(null, "editTestCaseModalForm");
    });

    feedTestCaseModal(test, testCase, "editTestCaseModal", "DUPLICATE");
}

/***
 * Open the modal in order to create a new testcase.
 * @returns {null}
 */
function addTestCaseClick() {
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

    feedNewTestCaseModal("editTestCaseModal");
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
    tinyMCE.triggerSave();

    showLoaderInModal('#editTestCaseModal');

    // Enable the test combo before submit the form.
    if (mode === 'EDIT') {
        formEdit.find("#test").removeAttr("disabled");
    }
    // Calculate servlet name to call.
    var myServlet = "UpdateTestCase2";
    if ((mode === "ADD") || (mode === "DUPLICATE")) {
        myServlet = "CreateTestCase2";
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
            labelList: JSON.stringify(table_label),
            countryList: JSON.stringify(table_country)},
        success: function (data) {
            hideLoaderInModal('#editTestCaseModal');
            if (getAlertType(data.messageType) === "success") {
                var oTable = $("#testCaseTable").dataTable();
                oTable.fnDraw(true);
                $('#editTestCaseModal').data("Saved",true);
                $('#editTestCaseModal').modal('hide');
                showMessage(data);
            } else {
                showMessage(data, $('#editTestCaseModal'));
            }
        },
        error: showUnexpectedError
    });
    if (mode === 'EDIT') { // Disable back the test combo before submit the form.
        formEdit.find("#test").prop("disabled", "disabled");
    }

}


/***
 * Feed the TestCase modal with all the data from the TestCase.
 * @param {String} modalId - type selected
 * @returns {null}
 */
function feedNewTestCaseModal(modalId) {
    clearResponseMessageMainPage();

    var formEdit = $('#' + modalId);

    appendBuildRevListOnTestCase(getUser().defaultSystem, undefined);

    feedTestCaseData(undefined, modalId, "ADD", true);
    // Labels
    loadLabel(undefined, undefined);
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
            // Loading the labl list from aplication of the testcase.
            loadLabel(testCase.labelList, appData.contentTable.system);
            // Loading application combo from the system of the current application.
            appendApplicationList(testCase.application, appData.contentTable.system);

            if (appData.contentTable.system !== currentSys) {
                $("[name=application]").empty();
                formEdit.find("#application").append($('<option></option>').text(testCase.application).val(testCase.application));
            }
            formEdit.find("#application").prop("value", testCase.application);

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


function feedTestCaseData(testCase, modalId, mode, hasPermissionsUpdate) {
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
        formEdit.find("#actProd").prop("value", testCase.activePROD);
    } else { // DUPLICATE or ADD
        formEdit.find("#usrcreated").prop("value", "");
        formEdit.find("#datecreated").prop("value", "");
        formEdit.find("#usrmodif").prop("value", "");
        formEdit.find("#datemodif").prop("value", "");
        formEdit.find("#actProd").prop("value", "N");
        if (mode === "ADD") {
            $("[name='editTestCaseField']").html(doc.getDocOnline("page_testcaselist", "btn_create"));
            appendTestList(undefined);
            feedTestCaseField(undefined, "editTestCaseModalForm");  // Calculate corresponding testcase value.
        } else { // DUPLICATE
            $("[name='editTestCaseField']").html(doc.getDocOnline("page_testcaselist", "btn_duplicate"));
            appendTestList(testCase.test);
            feedTestCaseField(testCase.test, "editTestCaseModalForm");  // Calculate corresponding testcase value.
        }
    }
    if (isEmpty(testCase)) {
        formEdit.find("#status").prop("value", "STANDBY");
        formEdit.find("#originalTest").prop("value", "");
        formEdit.find("#originalTestCase").prop("value", "");
        formEdit.find("#implementer").prop("value", "");
        formEdit.find("#origin").prop("value", "");
        formEdit.find("#refOrigin").prop("value", "");
        formEdit.find("#project").prop("value", "");
        formEdit.find("#ticket").prop("value", "");
        formEdit.find("#function").prop("value", "");
        formEdit.find("#group").prop("value", "AUTOMATED");
        formEdit.find("#priority").prop("value", "");
        formEdit.find("#actQA").prop("value", "Y");
        formEdit.find("#actUAT").prop("value", "Y");
        formEdit.find("#userAgent").prop("value", "");
        formEdit.find("#shortDesc").prop("value", "");
        tinyMCE.get('behaviorOrValueExpected').setContent("");
        tinyMCE.get('howTo').setContent("");
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
        formEdit.find("#shortDesc").prop("value", testCase.description);
        tinyMCE.get('behaviorOrValueExpected').setContent(testCase.behaviorOrValueExpected);
        tinyMCE.get('howTo').setContent(testCase.howTo);
        formEdit.find("#active").prop("value", testCase.tcActive);
        formEdit.find("#bugId").prop("value", testCase.bugID);
        formEdit.find("#conditionOper").prop("value", testCase.conditionOper);
        formEdit.find("#conditionVal1").prop("value", testCase.conditionVal1);
        formEdit.find("#conditionVal2").prop("value", testCase.conditionVal2);
        formEdit.find("#comment").prop("value", testCase.comment);
    }

    // Authorities
    if (mode === "EDIT") {
        formEdit.find("#test").prop("disabled", "disabled");
        formEdit.find("#testCase").prop("readonly", "readonly");
    } else {
        formEdit.find("#test").removeAttr("disabled");
        formEdit.find("#testCase").removeAttr("readonly");
    }
    //We desactivate or activate the access to the fields depending on if user has the credentials to edit.
    if (!(hasPermissionsUpdate)) { // If readonly, we only readonly all fields
        //test case info
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
        formEdit.find("#shortDesc").prop("readonly", "readonly");
        tinyMCE.get('behaviorOrValueExpected').getBody().setAttribute('contenteditable', false);
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
        formEdit.find("#shortDesc").removeProp("readonly");
        tinyMCE.get('behaviorOrValueExpected').getBody().setAttribute('contenteditable', true);
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

    var jqxhr = $.getJSON("FindInvariantByID", "idName=COUNTRY");
    $.when(jqxhr).then(function (data) {

        for (var index = 0; index < data.length; index++) {
            var country = data[index].value;

            var newCountry1 = {
                country: country,
                toDelete: true
            };
            if (testCase === undefined) {
                newCountry1.toDelete = false;
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
    if (isReadOnly) {
        var checkBox = $("<button type=\"button\" disabled=\"disabled\"></button>").append(testCaseCountry.country).val(testCaseCountry.country);
    } else {
        var checkBox = $("<button type=\"button\"></button>").append(testCaseCountry.country).val(testCaseCountry.country);
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

/***
 * Build the list of label and flag them from the testcase values..
 * @param {String} labelList - list of labels from the testcase to flag.
 * @param {String} mySystem - system that will be used in order to load the label list. if not feed, the default system from user will be used.
 * @returns {null}
 */
function loadLabel(labelList, mySystem) {

    var targetSystem = mySystem;
    if (isEmpty(targetSystem)) {
        targetSystem = getUser().defaultSystem;
    }
    
    var jqxhr = $.get("ReadLabel?system=" + targetSystem, "", "json");

    $.when(jqxhr).then(function (data) {
        var messageType = getAlertType(data.messageType);
        //DRAW LABEL LIST
        if (messageType === "success") {
            $('#selectLabel').empty();
            var index;
            for (index = 0; index < data.contentTable.length; index++) {
                //the character " needs a special encoding in order to avoid breaking the string that creates the html element   
                var labelTag = '<div style="float:left" align="center"><input name="labelid" id="labelId' + data.contentTable[index].id + '" value="' + data.contentTable[index].id + '" type="checkbox">\n\
                <span class="label label-primary" style="cursor:pointer;background-color:' + data.contentTable[index].color + '">' + data.contentTable[index].label + '</span></div> ';
                var option = $('<div style="float:left" name="itemLabelDiv" id="itemLabelId' + data.contentTable[index].id + '" class="col-xs-2 list-group-item list-label"></div>')
                        .attr("value", data.contentTable[index].label).html(labelTag);
                $('#selectLabel').append(option);
            }
        } else {
            showMessageMainPage(messageType, data.message);
        }
        // Put the selected testcaselabel at the top and check them. 
        if (!(isEmpty(labelList))) {
            var index;
            for (index = 0; index < labelList.length; index++) {
                //For each testcaselabel, put at the top of the list and check them
                var element = $("#itemLabelId" + labelList[index].label.id);
                element.remove();
                $("#selectLabel").prepend(element);
                $("#labelId" + labelList[index].label.id).prop("checked", true);
            }
        }
        //ADD CLICK EVENT ON LABEL
        $('#selectLabel').find('span').click(function () {
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

    var jqxhr = $.getJSON("ReadApplication", "system=" + targetSystem);
    $.when(jqxhr).then(function (data) {
        var applicationList = $("[name=application]");

        for (var index = 0; index < data.contentTable.length; index++) {
            applicationList.append($('<option></option>').text(data.contentTable[index].application).val(data.contentTable[index].application));
        }
        $("#application").prop("value", defautValue);
    });
}

function appendTestList(defautValue) {
    var user = getUser();
    $("[name=test]").empty();

    var jqxhr = $.getJSON("ReadTest", "");
    $.when(jqxhr).then(function (data) {
        var testList = $("[name=test]");

        for (var index = 0; index < data.contentTable.length; index++) {
            testList.append($('<option></option>').text(data.contentTable[index].test).val(data.contentTable[index].test));
        }
        $("#test").prop("value", defautValue);

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
