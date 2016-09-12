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

/* global modalFormCleaner */

$.when($.getScript("js/pages/global/global.js")).then(function () {
    $(document).ready(function () {
        initPage();
    });
});

function initPage() {
    var doc = new Doc();

    displayHeaderLabel(doc);
    displayGlobalLabel(doc);
    displayPageLabel(doc);
    displayFooter(doc);
    displayInvariantList("group", "GROUP", false);
    displayInvariantList("status", "TCSTATUS", false);
    displayInvariantList("priority", "PRIORITY", false);
    $('[name="origin"]').append('<option value="All">All</option>');
    displayInvariantList("origin", "ORIGIN", true);
    displayInvariantList("active", "TCACTIVE", false);
    displayInvariantList("activeQA", "TCACTIVE", false);
    displayInvariantList("activeUAT", "TCACTIVE", false);
    displayInvariantList("activeProd", "TCACTIVE", false);
    appendCountryList();
    appendApplicationList();
    appendProjectList();
    appendBuildRevList(getUser().defaultSystem);

    var selectTest = GetURLParameter('test');
    loadTestFilters(selectTest);
    loadTestComboAddTestCase(selectTest);

    tinymce.init({
        selector: "textarea"
    });

    if (isEmptyorALL(selectTest)) {
        loadTable(selectTest, 1);
    } else {
        loadTable(selectTest, 2);
    }

    // handle the click for specific action buttons
    $("#editEntryButton").click(editEntryModalSaveHandler);
    $("#addEntryButton").click(addEntryModalSaveHandler);
    $("#duplicateEntryButton").click(duplicateEntryModalSaveHandler);
    //PREPARE MASS ACTION
    //$("#massActionBrpButton").click(massActionModalSaveHandler);

    $('#editEntryModal').on('hidden.bs.modal', {extra: "#editEntryModal"}, modalFormCleaner);
    $('#addEntryModal').on('hidden.bs.modal', {extra: "#addEntryModal"}, modalFormCleaner);
    $('#duplicateEntryModal').on('hidden.bs.modal', {extra: "#duplicateEntryModal"}, modalFormCleaner);
    $('#manageLabelModal').on('hidden.bs.modal', {extra: "#manageLabelModal"}, modalFormCleaner);
    //PREPARE MASS ACTION
    //$('#massActionBrpModal').on('hidden.bs.modal', massActionModalCloseHandler);
    $('[data-toggle="tooltip"]').tooltip();

    $("#saveManageLabelButton").click(saveTestCaseLabel);
}

function displayPageLabel(doc) {
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
    $("[name='commentField']").html(doc.getDocOnline("testcase", "Comment"));
    $("#filters").html(doc.getDocOnline("page_testcaselist", "filters"));
    $("#testCaseListLabel").html(doc.getDocOnline("page_testcaselist", "testcaselist"));
    $("[name='btnLoad']").html(doc.getDocLabel("page_global", "buttonLoad"));
    $("[name='testField']").html(doc.getDocLabel("test", "Test"));
    $("[name='editEntryField']").html(doc.getDocLabel("page_testcaselist", "btn_edit"));
    $("[name='addEntryField']").html(doc.getDocLabel("page_testcaselist", "btn_create"));
    $("[name='linkField']").html(doc.getDocLabel("page_testcaselist", "link"));
    //PREPARE MASS ACTION
    //$("[name='massActionBrpField']").html(doc.getDocOnline("page_testcaselist", "massAction"));

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

function loadTable(selectTest, sortColumn) {

    if (isEmpty(selectTest)) {
        selectTest = $("#selectTest").val();
    }

    // We add the Browser history.
    var CallParam = '?';
    if (!isEmptyorALL(selectTest))
        CallParam += 'test=' + encodeURIComponent(selectTest);
    InsertURLInHistory('TestCaseList.jsp' + CallParam);

    //clear the old report content before reloading it
    $("#testCaseList").empty();
    $("#testCaseList").html('<table id="testCaseTable" class="table table-bordered table-hover display" name="testCaseTable">\n\
                                            </table><div class="marginBottom20"></div>');

    var contentUrl = "ReadTestCase?system=" + getUser().defaultSystem;
    if (!isEmptyorALL(selectTest)) {
        contentUrl += "&test=" + encodeURIComponent(selectTest);
    }

    //configure and create the dataTable
    var jqxhr = $.getJSON("FindInvariantByID", "idName=COUNTRY");

    $.when(jqxhr).then(function (data) {

        if (sortColumn === undefined)
            sortColumn = 2;

        var config = new TableConfigurationsServerSide("testCaseTable", contentUrl, "contentTable", aoColumnsFunc(data, "testCaseTable"), [sortColumn, 'asc']);

        var table = createDataTableWithPermissions(config, renderOptionsForTestCaseList, "#testCaseList");

        //PREPARE MASS ACTION
        //$("#selectAll").click(selectAll);

    });
}

function renderOptionsForTestCaseList(data) {
    var doc = new Doc();
    //check if user has permissions to perform the add and import operations
    if (data["hasPermissionsCreate"]) {
        if ($("#createTestCaseButton").length === 0) {
            var contentToAdd = "<div class='marginBottom10'>";
            contentToAdd += "<button id='createTestCaseButton' type='button' class='btn btn-default'>\n\
           <span class='glyphicon glyphicon-plus-sign'></span> " + doc.getDocLabel("page_testcaselist", "btn_create") + "</button></div>";
//PREPARE MASS ACTION
//var contentToAddAfter = "<button id='createBrpMassButton' type='button' class='pull-right btn btn-default'><span class='glyphicon glyphicon-th-list'></span> " + doc.getDocLabel("page_global", "button_massAction") + "</button>";

            $("#testCaseTable_wrapper #testCaseTable_length").before(contentToAdd);
            //PREPARE MASS ACTION
            //$("#showHideColumnsButton").parent().after(contentToAddAfter);

            $('#testCaseList #createTestCaseButton').click(data, addEntryClick);
            //PREPARE MASS ACTION
            //$('#testCaseList #createBrpMassButton').click(massActionClick);
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
    showModalConfirmation(deleteEntryHandlerClick, "Delete", messageComplete, test, testCase, "", "");
}

/*
 * Function called when confirmation button pressed 
 * @returns {undefined}
 */
function deleteEntryHandlerClick() {
    var test = $('#confirmationModal').find('#hiddenField1').prop("value");
    var testCase = $('#confirmationModal').find('#hiddenField2').prop("value");
    var jqxhr = $.post("DeleteTestCase2", {test: test, testCase: testCase}, "json");
    $.when(jqxhr).then(function (data) {
        var messageType = getAlertType(data.messageType);
        if (messageType === "success") {
            //redraw the datatable
            var oTable = $("#testCaseTable").dataTable();
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

/********************************************************
 //CREATE TESTCASE 
 /********************************************************/

/*
 * Function called on click on create button
 * The creation Modal is displayed with test selected, and some default values from user preferences
 * @returns {undefined}
 */
function addEntryClick() {
    clearResponseMessageMainPage();
    var pref = JSON.parse(localStorage.getItem("createTC"));
    var form = $("#addEntryModalForm");

    // Test by default comes from the URL (and the combo filter).
    var test = GetURLParameter('test');
    if (test !== "") {
        $('#testAdd option[value="' + test + '"]').attr("selected", "selected");
    }
    // TestCase is taken from the last value in database +1. This is an auto sequence. 
    feedTestCase(null, "addEntryModalForm");

    // In Add TestCase form, if we change the test, we get the latest testcase from that test.
    $('#addEntryModalForm select[name="test"]').change(function () {
        feedTestCase(null, "addEntryModalForm");
    });

    // By default we desactivate the execution of the testcase in production environment.
    $('#addEntryModalForm #actProd option[value="N"]').attr("selected", "selected");

    // The rest of the field come from the LocalStorage.
    if (pref !== null) {
        form.find("#origin").val(pref.origin);
        form.find("#refOrigin").val(pref.refOrigin);
        form.find(".countrycb").each(function () {
            if (pref[$(this).prop("name")] !== "off") {
                $(this).prop("checked", true);
            } else {
                $(this).prop("checked", false);
            }
        });
        form.find("#project").val(pref.project);
        form.find("#ticket").val(pref.ticket);
        form.find("#function").val(pref.function);
        form.find("#application").val(pref.application);
        form.find("#status").val(pref.status);
        form.find("#group").val(pref.group);
        form.find("#priority").val(pref.priority);
        form.find("#bugId").val(pref.bugId);
        form.find("#activeQA").val(pref.activeQA);
        form.find("#activeUAT").val(pref.activeUAT);
        form.find("#activeProd").val(pref.activeProd);
    }

    $('#addEntryModal').modal('show');
}


/* 
 * By clicking on save button, 
 * @returns {undefined}
 */
function addEntryModalSaveHandler() {
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

    var testCase = formAdd.find("#testCase");
    var testCaseEmpty = testCase.prop("value") === '';
    if (testCaseEmpty) {
        var localMessage = new Message("danger", "Please specify the name of the testCase!");
        testCase.parents("div.form-group").addClass("has-error");
        showMessage(localMessage, $('#addEntryModal'));
    } else {
        testCase.parents("div.form-group").removeClass("has-error");
    }

    // verif if all mendatory fields are not empty
    if (nameElementEmpty || testCaseEmpty)
        return;

    tinyMCE.triggerSave();
    localStorage.setItem("createTC", JSON.stringify(convertSerialToJSONObject(formAdd.serialize())));
    showLoaderInModal('#addEntryModal');
    createEntry("CreateTestCase2", formAdd, "#testCaseTable");
}

/********************************************************
 //GENERATE TESTCASE NUMBER (CREATE AND DUPLICATE)
 /********************************************************/
function feedTestCase(test, modalForm) {
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

/********************************************************
 //DUPLICATE TESTCASE 
 /********************************************************/

/**
 * Feed Duplicate Entry Formulary
 * @param {type} test
 * @param {type} testCase
 * @returns {undefined}
 */
function duplicateEntryClick(test, testCase) {
    feedTestCaseModal(test, testCase, "duplicateEntryModal");
    feedTestCase(test, "duplicateEntryModalForm");

    // In Duplicate TestCase form, if we change the test, we get the latest testcase from that test.
    $('#duplicateEntryModalForm select[name="test"]').change(function () {
        feedTestCase(null, "duplicateEntryModalForm");
    });
}
/**
 * On click on duplicate button event, submit formulary
 * @returns {undefined}
 */
function duplicateEntryModalSaveHandler() {
    clearResponseMessage($('#duplicateEntryModal'));

    var formEdit = $('#duplicateEntryModalForm');

    showLoaderInModal('#duplicateEntryModal');
    duplicateEntry("DuplicateTestCase", formEdit, "#testCaseTable");
}


/********************************************************
 //EDIT TESTCASE 
 /********************************************************/
function editEntryModalSaveHandler() {
    clearResponseMessage($('#editEntryModal'));

    var formEdit = $('#editEntryModalForm');
    tinyMCE.triggerSave();

    showLoaderInModal('#editEntryModal');
    updateEntry("UpdateTestCase2", formEdit, "#testCaseTable");
}

function editEntryClick(test, testCase) {
    feedTestCaseModal(test, testCase, "editEntryModal");
}

/********************************************************
 //EDIT AND DUPLICATE >> FEED TESTCASE MODAL
 /********************************************************/
function feedTestCaseModal(test, testCase, modalId) {
    clearResponseMessageMainPage();
    var jqxhr = $.getJSON("ReadTestCase", "test=" + encodeURIComponent(test) + "&testCase=" + encodeURIComponent(testCase));
    $.when(jqxhr).then(function (data) {

        var formEdit = $('#' + modalId);
        var testInfo = $.getJSON("ReadTest", "test=" + encodeURIComponent(test));
        var appInfo = $.getJSON("ReadApplication", "application=" + encodeURIComponent(data.application));

        $.when(testInfo).then(function (data) {
            formEdit.find("#testDesc").prop("value", data.contentTable.description);
        });

        $.when(appInfo).then(function (appData) {
            var currentSys = getUser().defaultSystem;
            var bugTrackerUrl = appData.contentTable.bugTrackerUrl;

            appendBuildRevList(appData.contentTable.system, data);

            if (appData.contentTable.system !== currentSys) {
                $("[name=application]").empty();
                formEdit.find("#application").append($('<option></option>').text(data.application).val(data.application));
                appendApplicationList(currentSys);
            }
            formEdit.find("#application").prop("value", data.application);

            var newbugTrackerUrl = "";
            if (data.bugID !== "" && bugTrackerUrl) {
                newbugTrackerUrl = bugTrackerUrl.replace("%BUGID%", data.bugID);
            }
            formEdit.find("#link").prop("href", newbugTrackerUrl).text(data.bugID);
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

        //test info
        formEdit.find("#originalTest").prop("value", data.test);
        formEdit.find("#originalTestCase").prop("value", data.testCase);
        formEdit.find("#newTest").prop("value", data.test);
        formEdit.find("#test").prop("value", data.test);
        formEdit.find("#testCase").prop("value", data.testCase);

        //test case info
        formEdit.find("#creator").prop("value", data.usrCreated);
        formEdit.find("#lastModifier").prop("value", data.usrModif);
        formEdit.find("#implementer").prop("value", data.implementer);
        formEdit.find("#tcDateCrea").prop("value", data.dateCreated);
        formEdit.find("#origin").prop("value", data.origine);
        formEdit.find("#refOrigin").prop("value", data.refOrigine);
        formEdit.find("#project").prop("value", data.project);
        formEdit.find("#ticket").prop("value", data.ticket);
        formEdit.find("#function").prop("value", data.function);

        // test case parameters
        formEdit.find("#application").prop("value", data.application);
        formEdit.find("#status").prop("value", data.status);
        formEdit.find("#group").prop("value", data.group);
        formEdit.find("#priority").prop("value", data.priority);
        formEdit.find("#actQA").prop("value", data.activeQA);
        formEdit.find("#actUAT").prop("value", data.activeUAT);
        formEdit.find("#actProd").prop("value", data.activePROD);
        formEdit.find("#userAgent").prop("value", data.userAgent);
        for (var country in data.countryList) {
            $('#countryList input[name="' + data.countryList[country] + '"]').prop("checked", true);
        }
        formEdit.find("#shortDesc").prop("value", data.description);
        tinyMCE.get('behaviorOrValueExpected').setContent(data.behaviorOrValueExpected);
        tinyMCE.get('howTo').setContent(data.howTo);

        //activation criteria
        formEdit.find("#active").prop("value", data.tcActive);
        formEdit.find("#bugId").prop("value", data.bugID);
        formEdit.find("#comment").prop("value", data.comment);

        formEdit.find("#usrcreated").prop("value", data.usrCreated);
        formEdit.find("#datecreated").prop("value", data.dateCreated);
        formEdit.find("#usrmodif").prop("value", data.usrModif);
        formEdit.find("#datemodif").prop("value", data.dateModif);


        //We desactivate or activate the access to the fields depending on if user has the credentials to edit.
        if (!(data["hasPermissionsUpdate"]) && modalId === "editEntryModal") { // If readonly, we only readonly all fields
            //test case info
            formEdit.find("#implementer").prop("readonly", "readonly");
            formEdit.find("#origin").prop("disabled", "disabled");
            formEdit.find("#project").prop("disabled", "disabled");
            formEdit.find("#ticket").prop("readonly", "readonly");
            formEdit.find("#function").prop("readonly", "readonly");
            // test case parameters
            formEdit.find("#application").prop("disabled", "disabled");
            formEdit.find("#status").prop("disabled", "disabled");
            formEdit.find("#group").prop("disabled", "disabled");
            formEdit.find("#priority").prop("disabled", "disabled");
            formEdit.find("#actQA").prop("disabled", "disabled");
            formEdit.find("#actUAT").prop("disabled", "disabled");
            formEdit.find("#actProd").prop("disabled", "disabled");
            formEdit.find("#userAgent").prop("disabled", "disabled");
            var myCountryList = $('#countryList');
            myCountryList.find("[class='countrycb']").prop("disabled", "disabled");
            formEdit.find("#shortDesc").prop("readonly", "readonly");
            tinyMCE.get('behaviorOrValueExpected').getBody().setAttribute('contenteditable', false);
            tinyMCE.get('howTo').getBody().setAttribute('contenteditable', false);
            //activation criteria
            formEdit.find("#active").prop("disabled", "disabled");
            formEdit.find("#fromSprint").prop("disabled", "disabled");
            formEdit.find("#fromRev").prop("disabled", "disabled");
            formEdit.find("#toSprint").prop("disabled", "disabled");
            formEdit.find("#toRev").prop("disabled", "disabled");
            formEdit.find("#targetSprint").prop("disabled", "disabled");
            formEdit.find("#targetRev").prop("disabled", "disabled");
            formEdit.find("#bugId").prop("readonly", "readonly");
            formEdit.find("#comment").prop("readonly", "readonly");
            // Save button is hidden.
            $('#editEntryButton').attr('class', '');
            $('#editEntryButton').attr('hidden', 'hidden');
        } else {
            formEdit.find("#active").removeProp("disabled");
            formEdit.find("#bugId").removeProp("readonly");

            //test case info
            formEdit.find("#implementer").removeProp("readonly");
            formEdit.find("#origin").removeProp("disabled");
            formEdit.find("#project").removeProp("disabled");
            formEdit.find("#ticket").removeProp("readonly");
            formEdit.find("#function").removeProp("readonly");
            // test case parameters
            formEdit.find("#application").removeProp("disabled");
            formEdit.find("#status").removeProp("disabled");
            formEdit.find("#group").removeProp("disabled");
            formEdit.find("#priority").removeProp("disabled");
            formEdit.find("#actQA").removeProp("disabled");
            formEdit.find("#actUAT").removeProp("disabled");
            formEdit.find("#actProd").removeProp("disabled");
            formEdit.find("#userAgent").removeProp("disabled");
            var myCountryList = $('#countryList');
            myCountryList.find("[class='countrycb']").removeProp("disabled");
            formEdit.find("#shortDesc").removeProp("readonly");
            tinyMCE.get('behaviorOrValueExpected').getBody().setAttribute('contenteditable', true);
            tinyMCE.get('howTo').getBody().setAttribute('contenteditable', true);
            //activation criteria
            formEdit.find("#active").removeProp("disabled");
            formEdit.find("#fromSprint").removeProp("disabled");
            formEdit.find("#fromRev").removeProp("disabled");
            formEdit.find("#toSprint").removeProp("disabled");
            formEdit.find("#toRev").removeProp("disabled");
            formEdit.find("#targetSprint").removeProp("disabled");
            formEdit.find("#targetRev").removeProp("disabled");
            formEdit.find("#bugId").removeProp("readonly");
            formEdit.find("#comment").removeProp("readonly");
            // Save button is displayed.
            $('#editEntryButton').attr('class', 'btn btn-primary');
            $('#editEntryButton').removeProp('hidden');
            //Duplicate button is displayed if hasPermissionsCreate
            if (data["hasPermissionsCreate"]) {
                $('#duplicateEntryButton').attr('class', 'btn btn-primary');
                $('#duplicateEntryButton').removeProp('hidden');
            }
        }



        formEdit.modal('show');
    });
}

/********************************************************
 //TRANSVERSAL >> FEED COMBO FIELDS
 /********************************************************/
function appendBuildRevList(system, editData) {

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
            var formEdit = $('#editEntryModal');

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
            var formEdit = $('#editEntryModal');

            formEdit.find("[name=fromRev]").prop("value", editData.fromRev);
            formEdit.find("[name=toRev]").prop("value", editData.toRev);
            formEdit.find("[name=targetRev]").prop("value", editData.targetRev);
        }
    });
}

function appendCountryList() {
    var jqxhr = $.getJSON("FindInvariantByID", "idName=COUNTRY");
    $.when(jqxhr).then(function (data) {
        var countryList = $("[name=countryList]");

        for (var index = 0; index < data.length; index++) {
            var country = data[index].value;

            countryList.append('<label class="checkbox-inline"><input class="countrycb" type="checkbox" name="' + country + '"/>' + country + '\
                                <input id="countryCheckB" class="countrycb-hidden" type="hidden" name="' + country + '" value="off"/></label>');
        }
    });
}

function appendApplicationList() {
    var user = getUser();

    var jqxhr = $.getJSON("ReadApplication", "system=" + encodeURIComponent(user.defaultSystem));
    $.when(jqxhr).then(function (data) {
        var applicationList = $("[name=application]");

        for (var index = 0; index < data.contentTable.length; index++) {
            applicationList.append($('<option></option>').text(data.contentTable[index].application).val(data.contentTable[index].application));
        }
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

function loadTestFilters(selectTest) {
    var jqxhr = $.get("ReadTest", "system=" + getUser().defaultSystem);
    $.when(jqxhr).then(function (data) {
        var messageType = getAlertType(data.messageType);
        var option = $('<option></option>').attr("value", "ALL").text("-- ALL --");
        $('#selectTest').append(option);
        if (messageType === "success") {
            var index;
            for (index = 0; index < data.contentTable.length; index++) {
                //the character " needs a special encoding in order to avoid breaking the string that creates the html element   
                var encodedString = data.contentTable[index].test.replace(/\"/g, "%22");
                var text = data.contentTable[index].test + ' - ' + data.contentTable[index].description;
                var option = $('<option></option>').attr("value", encodedString).text(text);
                $('#selectTest').append(option);
            }
            $('#selectTest').select2();

            //if the test is passed as a url parameter, then we load the testcase list from that test. If not we load the list with testcases from all tests.
            if (!isEmptyorALL(selectTest)) {
//                $('#selectTest').val(selectTest);
                $('#selectTest').val(selectTest).trigger("change");

                var selectTestNew = $("#selectTest option:selected").attr("value");
                if (selectTestNew !== selectTest) { // If the url test value does not exist in the combobox --> we display a warning message.
                    showMessageMainPage("warning", "The test \"" + selectTest + "\" contains no testcase on application that belong to " + getUser().defaultSystem + " system.");
                    option = $('<option></option>').attr("value", selectTest).text(selectTest);
                    $('#selectTest').append(option);
//                    $('#selectTest').val(selectTest);
                    $('#selectTest').val(selectTest).trigger("change");
                }
            }
        } else {
            showMessageMainPage(messageType, data.message);
        }
    }).fail(handleErrorAjaxAfterTimeout);
}

function loadTestComboAddTestCase(selectTest) {
    var jqxhr = $.get("ReadTest");
    $.when(jqxhr).then(function (data) {
        var messageType = getAlertType(data.messageType);
        if (messageType === "success") {
            var index;
            for (index = 0; index < data.contentTable.length; index++) {
                //the character " needs a special encoding in order to avoid breaking the string that creates the html element   
                var encodedString = data.contentTable[index].test.replace(/\"/g, "%22");
                var text = data.contentTable[index].test + ' - ' + data.contentTable[index].description;
                var option = $('<option></option>').attr("value", encodedString).text(text);
                $('select[name="test"]').append($('<option></option>').text(text).val(encodedString));
            }
            if (selectTest !== undefined) {
                $('select[name="test"]').val(selectTest);
            }

        } else {
            showMessageMainPage(messageType, data.message);
        }
    }).fail(handleErrorAjaxAfterTimeout);
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
        url: "UpdateTestCase2",
        method: "POST",
        data: {test: test, testCase: testCase, active: active},
        dataType: "json",
        success: function (data) {
            if (active === "Y") {
                $("#runTest" + test + testCase).removeAttr("disabled");
            } else {
                $('#runTest' + test + testCase).attr("disabled", "disabled");
            }
            clearResponseMessageMainPage();
            var messageType = getAlertType(data.messageType);
            //show message in the main page
            showMessageMainPage(messageType, data.message);
        },
        error: showUnexpectedError
    });

}

function setCountry(checkbox) {
    var test = checkbox.dataset.test;
    var testCase = checkbox.dataset.testcase;
    var country = checkbox.name;
    var state;

    if (checkbox.checked === true) {
        state = "on";
    } else {
        state = "off";
    }

    $.ajax({
        url: "UpdateTestCase2",
        method: "POST",
        data: "test=" + test + "&testCase=" + testCase + "&" + country + "=" + state,
        dataType: "json",
        success: function (data) {
            clearResponseMessageMainPage();
            var messageType = getAlertType(data.messageType);
            //show message in the main page
            showMessageMainPage(messageType, data.message);
        },
        error: showUnexpectedError
    });
}
/** IMPLEMENT MASS ACTION ON TESTCASELIST PAGE
 
 function selectAll() {
 if ($(this).prop("checked"))
 $("[data-line='select']").prop("checked", true);
 else
 $("[data-line='select']").removeProp("checked");
 }
 
 function massActionModalSaveHandler() {
 clearResponseMessage($('#massActionBrpModal'));
 
 var formNewValues = $('#massActionBrpModal #massActionBrpModalForm');
 var formList = $('#massActionForm');
 var paramSerialized = formNewValues.serialize() + "&" + formList.serialize().replace(/=on/g, '').replace(/id-/g, 'id=');
 
 showLoaderInModal('#massActionBrpModal');
 
 var jqxhr = $.post("UpdateBuildRevisionParameters", paramSerialized, "json");
 $.when(jqxhr).then(function (data) {
 // unblock when remote call returns 
 hideLoaderInModal('#massActionBrpModal');
 if ((getAlertType(data.messageType) === "success") || (getAlertType(data.messageType) === "warning")) {
 var oTable = $("#buildrevisionparametersTable").dataTable();
 oTable.fnDraw(true);
 $('#massActionBrpModal').modal('hide');
 showMessage(data);
 } else {
 showMessage(data, $('#massActionBrpModal'));
 }
 }).fail(handleErrorAjaxAfterTimeout);
 }
 
 function massActionModalCloseHandler() {
 // reset form values
 $('#massActionBrpModal #massActionBrpModalForm')[0].reset();
 // remove all errors on the form fields
 $(this).find('div.has-error').removeClass("has-error");
 // clear the response messages of the modal
 clearResponseMessage($('#massActionBrpModal'));
 }
 
 function massActionClick() {
 var doc = new Doc();
 console.debug("Mass Action");
 clearResponseMessageMainPage();
 // When creating a new item, Define here the default value.
 var formList = $('#massActionForm');
 if (formList.serialize().indexOf("id-") === -1) {
 var localMessage = new Message("danger", doc.getDocLabel("page_buildcontent", "message_massActionError1"));
 showMessage(localMessage, null);
 } else {
 $('#massActionBrpModal').modal('show');
 }
 }
 * 
 *
 */

function aoColumnsFunc(countries, tableId) {
    var doc = new Doc();

    var countryLen = countries.length;
    var aoColumns = [
//PREPARE MASS ACTION
//        {"data": null,
//            "title": '<input id="selectAll" title="' + doc.getDocLabel("page_global", "tooltip_massAction") + '" type="checkbox"></input>',
//            "bSortable": false,
//            "sWidth": "30px",
//            "bSearchable": false,
//            "mRender": function (data, type, obj) {
//                console.log(obj);
//                var hasPermissions = $("#" + tableId).attr("hasPermissions");
//
//                var selectBrp = '<input id="selectLine" \n\
//                                class="selectBrp margin-right5" \n\
//                                name="id-' + obj["test"] + obj["testCase"] + '" data-line="select" data-id="' + obj["test"] + obj["testCase"] + '" title="' + doc.getDocLabel("page_global", "tooltip_massActionLine") + '" type="checkbox">\n\
//                                </input>';
//                if (hasPermissions === "true") { //only draws the options if the user has the correct privileges
//                    return '<div class="center btn-group width50">' + selectBrp + '</div>';
//                }
//                return '<div class="center btn-group width50"></div>';
//
//            }
//        },
        {
            "data": null,
            "bSortable": false,
            "bSearchable": false,
            "title": doc.getDocOnline("page_global", "columnAction"),
            "sDefaultContent": "",
            "sWidth": "190px",
            "mRender": function (data, type, obj) {
                var buttons = "";

                var testCaseLink = '<button id="testCaseLink" class="btn btn-primary btn-inverse btn-xs margin-right5"\n\
                                    data-toggle="tooltip" title="' + doc.getDocLabel("page_testcaselist", "btn_editScript") + '" onclick=window.location="./TestCase.jsp?Test=' + encodeURIComponent(obj["test"]) + "&TestCase=" + encodeURIComponent(obj["testCase"]) + '&Load=Load">\n\
                                    <span class="glyphicon glyphicon-new-window"></span>\n\
                                    </button>';
                var editEntry = '<button id="editEntry" onclick="editEntryClick(\'' + escapeHtml(obj["test"]) + '\',\'' + escapeHtml(obj["testCase"]) + '\');"\n\
                                class="editEntry btn btn-default btn-xs margin-right5" \n\
                                name="editEntry" data-toggle="tooltip"  title="' + doc.getDocLabel("page_testcaselist", "btn_edit") + '" type="button">\n\
                                <span class="glyphicon glyphicon-pencil"></span></button>';
                var viewEntry = '<button id="editEntry" onclick="editEntryClick(\'' + escapeHtml(obj["test"]) + '\',\'' + escapeHtml(obj["testCase"]) + '\');"\n\
                                class="editEntry btn btn-default btn-xs margin-right5" \n\
                                name="editEntry" data-toggle="tooltip"  title="' + doc.getDocLabel("page_testcaselist", "btn_view") + '" type="button">\n\
                                <span class="glyphicon glyphicon-eye-open"></span></button>';
                var deleteEntry = '<button id="deleteEntry" onclick="deleteEntryClick(\'' + escapeHtml(obj["test"]) + '\',\'' + escapeHtml(obj["testCase"]) + '\');"\n\
                                        class="deleteEntry btn btn-default btn-xs margin-right5" \n\
                                        name="deleteEntry" data-toggle="tooltip"  title="' + doc.getDocLabel("page_testcaselist", "btn_delete") + '" type="button">\n\
                                        <span class="glyphicon glyphicon-trash"></span></button>';
                var duplicateEntry = '<button id="duplicateEntry" onclick="duplicateEntryClick(\'' + escapeHtml(obj["test"]) + '\',\'' + escapeHtml(obj["testCase"]) + '\');"\n\
                                        class="duplicateEntry btn btn-default btn-xs margin-right5" \n\
                                        name="duplicateEntry" data-toggle="tooltip"  title="' + doc.getDocLabel("page_testcaselist", "btn_duplicate") + '" type="button">\n\
                                        <span class="glyphicon glyphicon-duplicate"></span></button>';
                var testCaseBetaLink = '<button id="testCaseBetaLink" class="btn btn-warning btn-xs margin-right5"\n\
                                    data-toggle="tooltip" title="' + doc.getDocLabel("page_testcaselist", "btn_editScript") + " (beta page)" + '" onclick=window.location="./TestCaseScript.jsp?test=' + encodeURIComponent(obj["test"]) + "&testcase=" + encodeURIComponent(obj["testCase"]) + '">\n\
                                    <span class="glyphicon glyphicon-new-window"></span>\n\
                                    </button>';
                var editLabel = '<button id="editLabel" onclick="editLabelClick(\'' + escapeHtml(obj["test"]) + '\',\'' + escapeHtml(obj["testCase"]) + '\');"\n\
                                class="editLabel btn btn-default btn-xs margin-right5" \n\
                                name="editLabel" data-toggle="tooltip"  title="' + doc.getDocLabel("page_testcaselist", "btn_editLabel") + '" type="button">\n\
                                <span class="glyphicon glyphicon-tag"></span></button>';
                var runTest = '<button id="runTest' + encodeURIComponent(obj["test"]) + encodeURIComponent(obj["testCase"]) + '" class="btn btn-default btn-xs margin-right5"\n\
                                    data-toggle="tooltip" title="' + doc.getDocLabel("page_testcaselist", "btn_runTest") + '" onclick=window.location="./RunTests1.jsp?test=' + encodeURIComponent(obj["test"]) + "&testcase=" + encodeURIComponent(obj["testCase"]) + '">\n\
                                    <span class="glyphicon glyphicon-play"></span>\n\
                                    </button>';

                if (data.hasPermissionsUpdate) {
                    buttons += editEntry;
                    buttons += duplicateEntry;
                } else {
                    buttons += viewEntry;
                }
                if (data.hasPermissionsDelete) {
                    buttons += deleteEntry;
                }
                buttons += editLabel;
                buttons += runTest;
                buttons += testCaseLink;
                buttons += testCaseBetaLink;



                return '<div class="center btn-group width250">' + buttons + '</div>';
            }
        },
        {
            "data": "test",
            "sName": "tec.test",
            "title": doc.getDocOnline("test", "Test"),
            "sWidth": "120px",
            "sDefaultContent": ""
        },
        {
            "data": "testCase",
            "sName": "tec.testCase",
            "title": doc.getDocOnline("testcase", "TestCase"),
            "sWidth": "70px",
            "sDefaultContent": ""
        },
        {
            "data": "labels",
            "sName": "lab.label",
            "title": doc.getDocOnline("label", "label"),
            "sWidth": "170px",
            "sDefaultContent": "",
            "render": function (data, type, full, meta) {
                var labelValue = '';
                $.each(data, function (i, e) {
                    labelValue += '<div style="float:left"><span class="label label-primary" onclick="filterOnLabel(this)" style="cursor:pointer;background-color:' + e.color + '">' + e.name + '</span></div> ';
                });
                return labelValue;
            }
        },
        {
            "data": "application",
            "sName": "tec.application",
            "title": doc.getDocOnline("application", "Application"),
            "sWidth": "100px",
            "sDefaultContent": ""
        },
        {
            "data": "project",
            "sName": "tec.project",
            "title": doc.getDocOnline("project", "idproject"),
            "sWidth": "100px",
            "sDefaultContent": ""
        },
        {
            "data": "usrCreated",
            "sName": "tec.usrCreated",
            "title": doc.getDocOnline("testcase", "Creator"),
            "sWidth": "100px",
            "sDefaultContent": ""
        },
        {
            "data": "usrModif",
            "sName": "tec.usrModif",
            "title": doc.getDocOnline("testcase", "LastModifier"),
            "sWidth": "100px",
            "sDefaultContent": ""
        },
        {
            "data": "tcActive",
            "sName": "tec.tcactive",
            "title": doc.getDocOnline("testcase", "TcActive"),
            "sDefaultContent": "",
            "sWidth": "70px",
            "className": "center",
            "mRender": function (data, type, obj) {
                if (obj.hasPermissionsUpdate) {
                    if (data === "Y") {
                        return '<input type="checkbox" name="' + obj["testCase"] + '" data-test="' + obj.test + '" onchange="setActive(this);" checked/>';
                    } else if (data === "N") {
                        $('#runTest' + encodeURIComponent(obj["test"]) + encodeURIComponent(obj["testCase"])).attr("disabled", "disabled");
                        return '<input type="checkbox" name="' + obj["testCase"] + '" data-test="' + obj.test + '" onchange="setActive(this);" />';
                    }
                } else {
                    if (data === "Y") {
                        return '<input type="checkbox" checked disabled />';
                    } else {
                        $('#runTest' + encodeURIComponent(obj["test"]) + encodeURIComponent(obj["testCase"])).attr("disabled", "disabled");
                        return '<input type="checkbox" disabled />';
                    }
                }
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
            "data": "priority",
            "sName": "tec.priority",
            "title": doc.getDocOnline("invariant", "PRIORITY"),
            "sWidth": "70px",
            "sDefaultContent": ""
        },
        {
            "data": "origine",
            "sName": "tec.origine",
            "title": doc.getDocOnline("testcase", "Origine"),
            "sWidth": "70px",
            "sDefaultContent": ""
        },
        {
            "data": "refOrigine",
            "sName": "tec.refOrigine",
            "title": doc.getDocOnline("testcase", "RefOrigine"),
            "sWidth": "80px",
            "sDefaultContent": ""
        },
        {
            "data": "group",
            "sName": "tec.group",
            "title": doc.getDocOnline("invariant", "GROUP"),
            "sWidth": "100px",
            "sDefaultContent": ""
        },
        {
            "data": "description",
            "sName": "tec.description",
            "title": doc.getDocOnline("testcase", "Description"),
            "sWidth": "300px",
            "sDefaultContent": ""
        },
        {
            "data": "dateCreated",
            "sName": "tec.dateCreated",
            "title": doc.getDocOnline("testcase", "TCDateCrea"),
            "sWidth": "150px",
            "sDefaultContent": ""
        }
    ];

    for (var index = 0; index < countryLen; index++) {
        var country = countries[index].value;

        var column = {
            "data": function (row, type, val, meta) {
                var dataTitle = meta.settings.aoColumns[meta.col].sTitle;

                if (row.hasPermissionsUpdate) {
                    if (row.hasOwnProperty("countryList") && row["countryList"].hasOwnProperty(dataTitle)) {
                        return '<input type="checkbox" name="' + dataTitle + '" data-test="' + row.test + '" data-testcase="' + row.testCase + '" onchange="setCountry(this);" checked/>';
                    } else {
                        return '<input type="checkbox" name="' + dataTitle + '" data-test="' + row.test + '" data-testcase="' + row.testCase + '" onchange="setCountry(this);"/>';
                    }
                } else {
                    if (row.hasOwnProperty("countryList") && row["countryList"].hasOwnProperty(dataTitle)) {
                        return '<input type="checkbox" checked disabled/>';
                    } else {
                        return '<input type="checkbox" disabled/>';
                    }
                }
            },
            "bSortable": false,
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

/******************************************************************************
 * LABEL MANAGEMENT
 * Load label list
 */
function loadLabelFilter(test, testcase) {

    $("#manageLabelModalForm #test").val(test);
    $("#manageLabelModalForm #testCase").val(testcase);
    var jqxhr = $.get("ReadLabel?system=" + getUser().defaultSystem, "", "json");

    $.when(jqxhr).then(function (data) {
        var messageType = getAlertType(data.messageType);

        if (messageType === "success") {
            $('#selectLabel').empty();
            var index;
            for (index = 0; index < data.contentTable.length; index++) {
                //the character " needs a special encoding in order to avoid breaking the string that creates the html element   
                var labelTag = '<div style="float:left"><input id="labelId' + data.contentTable[index].id + '" data-labelid="' + data.contentTable[index].id + '" type="checkbox">\n\
                <span class="label label-primary" style="background-color:' + data.contentTable[index].color + '">' + data.contentTable[index].label + '</span></div> ';
                var option = $('<li id="itemLabelId' + data.contentTable[index].id + '" class="list-group-item list-label"></li>')
                        .attr("value", data.contentTable[index].label).html(labelTag);
                $('#selectLabel').append(option);
            }
        } else {
            showMessageMainPage(messageType, data.message);
        }
        loadTestCaseLabel(test, testcase);
    }).fail(handleErrorAjaxAfterTimeout);
}

/**
 * 
 * 
 */
function loadTestCaseLabel(test, testcase) {
    var jqxhr = $.get("ReadTestCaseLabel?test=" + test + "&testcase=" + testcase, "", "json");
//Get the label of the selected testcase
    $.when(jqxhr).then(function (data) {
        var messageType = getAlertType(data.messageType);

        if (messageType === "success") {
            var index;
            var labelTag = '';
            for (index = 0; index < data.contentTable.length; index++) {
                //For each testcaselabel, put at the top of the list and check them
                var element = $("#itemLabelId" + data.contentTable[index].label.id);
                element.remove();
                $("#selectLabel").prepend(element);
                $("#labelId" + data.contentTable[index].label.id).prop("checked", true);
            }
        } else {
            showMessageMainPage(messageType, data.message);
        }
    }).fail(handleErrorAjaxAfterTimeout);
}

function filterOnLabel(element) {
    var newLabel = $(element).get(0).textContent;
    var colIndex = $(element).parent().parent().get(0).cellIndex;
    $("#testCaseTable").dataTable().fnFilter(newLabel, colIndex);
}

function editLabelClick(test, testcase) {
    clearResponseMessageMainPage();
    loadLabelFilter(test, testcase);
    $("#manageLabelModal").modal('show');
}

function saveTestCaseLabel() {
    var test = $("#manageLabelModalForm #test").val();
    var testcase = $("#manageLabelModalForm #testCase").val();
    var labelListForm = $("#manageLabelModalForm input:checked");
    var labelList = '';

    $.each(labelListForm, function (i, e) {
        labelList += "&labelid=" + $(e).attr("data-labelid");
    });

    var jqxhr = $.get("SaveTestCaseLabel?test=" + test + "&testcase=" + testcase + labelList, "", "json");

    $.when(jqxhr).then(function (data) {
        var messageType = getAlertType(data.messageType);

        if (messageType === "success") {
            $("#manageLabelModal").modal('hide');
            var oTable = $("#testCaseTable").DataTable();
            oTable.draw();
            showMessageMainPage(messageType, data.message);
        } else {
            showMessageMainPage(messageType, data.message);
        }
    }).fail(handleErrorAjaxAfterTimeout);
}


