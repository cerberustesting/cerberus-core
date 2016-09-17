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
    feedTestCaseModal(test, testCase, "editTestCaseModal");
}

/***
 * Save the Modal information when clicking on save button.
 * @returns {null}
 */
function editTestCaseModalSaveHandler() {
    clearResponseMessage($('#editTestCaseModal'));

    var formEdit = $('#editTestCaseModalForm');
    tinyMCE.triggerSave();

    showLoaderInModal('#editTestCaseModal');

    // Get the header data from the form.
    var dataForm = convertSerialToJSONObject(formEdit.serialize());

    var jqxhr = $.post("UpdateTestCase2", formEdit.serialize());
    $.when(jqxhr).then(function (data) {
        hideLoaderInModal("#editTestCaseModal");
        if (getAlertType(data.messageType) === 'success') {
            var oTable = $("#testCaseTable").dataTable();
            oTable.fnDraw(false);
            showMessage(data);
            $("#editTestCaseModal").modal('hide');
        } else {
            showMessage(data, $("#editTestCaseModal"));
        }
    }).fail(handleErrorAjaxAfterTimeout);
}

/***
 * Feed the TestCase modal with all the data from the TestCase.
 * @param {String} test - type selected
 * @param {String} testCase - type selected
 * @param {String} modalId - type selected
 * @returns {null}
 */
function feedTestCaseModal(test, testCase, modalId) {
    clearResponseMessageMainPage();
    var jqxhr = $.getJSON("ReadTestCase", "test=" + encodeURIComponent(test) + "&testCase=" + encodeURIComponent(testCase));
    $.when(jqxhr).then(function (data) {

        var testCase = data.contentTable;

        var formEdit = $('#' + modalId);
        var testInfo = $.getJSON("ReadTest", "test=" + encodeURIComponent(test));
        var appInfo = $.getJSON("ReadApplication", "application=" + encodeURIComponent(testCase.application));

        $.when(testInfo).then(function (data) {
            formEdit.find("#testDesc").prop("value", data.contentTable.description);
        });

        $.when(appInfo).then(function (appData) {
            var currentSys = getUser().defaultSystem;
            var bugTrackerUrl = appData.contentTable.bugTrackerUrl;

            appendBuildRevListOnTestCase(appData.contentTable.system, testCase);

            if (appData.contentTable.system !== currentSys) {
                $("[name=application]").empty();
                formEdit.find("#application").append($('<option></option>').text(testCase.application).val(testCase.application));
                appendApplicationList(currentSys);
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

        //test info
        formEdit.find("#originalTest").prop("value", testCase.test);
        formEdit.find("#originalTestCase").prop("value", testCase.testCase);
        formEdit.find("#newTest").prop("value", testCase.test);
        formEdit.find("#test").prop("value", testCase.test);
        formEdit.find("#testCase").prop("value", testCase.testCase);
        formEdit.find("#creator").prop("value", testCase.usrCreated);
        formEdit.find("#lastModifier").prop("value", testCase.usrModif);
        formEdit.find("#implementer").prop("value", testCase.implementer);
        formEdit.find("#tcDateCrea").prop("value", testCase.dateCreated);
        formEdit.find("#origin").prop("value", testCase.origine);
        formEdit.find("#refOrigin").prop("value", testCase.refOrigine);
        formEdit.find("#project").prop("value", testCase.project);
        formEdit.find("#ticket").prop("value", testCase.ticket);
        formEdit.find("#function").prop("value", testCase.function);
        formEdit.find("#application").prop("value", testCase.application);
        formEdit.find("#status").prop("value", testCase.status);
        formEdit.find("#group").prop("value", testCase.group);
        formEdit.find("#priority").prop("value", testCase.priority);
        formEdit.find("#actQA").prop("value", testCase.activeQA);
        formEdit.find("#actUAT").prop("value", testCase.activeUAT);
        formEdit.find("#actProd").prop("value", testCase.activePROD);
        formEdit.find("#userAgent").prop("value", testCase.userAgent);
        formEdit.find("#shortDesc").prop("value", testCase.description);
        tinyMCE.get('behaviorOrValueExpected').setContent(testCase.behaviorOrValueExpected);
        tinyMCE.get('howTo').setContent(testCase.howTo);
        formEdit.find("#active").prop("value", testCase.tcActive);
        formEdit.find("#bugId").prop("value", testCase.bugID);
        formEdit.find("#comment").prop("value", testCase.comment);
        formEdit.find("#usrcreated").prop("value", testCase.usrCreated);
        formEdit.find("#datecreated").prop("value", testCase.dateCreated);
        formEdit.find("#usrmodif").prop("value", testCase.usrModif);
        formEdit.find("#datemodif").prop("value", testCase.dateModif);
        for (var country in testCase.countryList) {
            $('#countryList input[name="' + testCase.countryList[country].country + '"]').prop("checked", true);
        }
        $("#testCaseCountryTableBody tr").empty();
        appendTestCaseCountryList(testCase);

        //Label
        loadLabel(testCase.labelList);

        //We desactivate or activate the access to the fields depending on if user has the credentials to edit.
        if (!(data["hasPermissionsUpdate"]) && modalId === "editTestCaseModal") { // If readonly, we only readonly all fields
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
            formEdit.find("#bugId").prop("readonly", "readonly");
            formEdit.find("#comment").prop("readonly", "readonly");
            var myCountryList = $('#countryList');
            myCountryList.find("[class='countrycb']").prop("disabled", "disabled");
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
            formEdit.find("#bugId").removeProp("readonly");
            formEdit.find("#comment").removeProp("readonly");
            var myCountryList = $('#countryList');
            myCountryList.find("[class='countrycb']").removeProp("disabled");
            // Save button is displayed.
            $('#editTestCaseButton').attr('class', 'btn btn-primary');
            $('#editTestCaseButton').removeProp('hidden');
            //Duplicate button is displayed if hasPermissionsCreate
            if (data["hasPermissionsCreate"]) {
                $('#duplicateTestCaseButton').attr('class', 'btn btn-primary');
                $('#duplicateTestCaseButton').removeProp('hidden');
            }
        }

        formEdit.modal('show');
    });
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

function appendTestCaseCountryList(testCase) {
    var countryList = $("[name=countryList]");
    countryList.empty();

    var jqxhr = $.getJSON("FindInvariantByID", "idName=COUNTRY");
    $.when(jqxhr).then(function (data) {

        for (var index = 0; index < data.length; index++) {
            var country = data[index].value;

//            countryList.append('<label class="checkbox-inline"><input class="countrycb" type="checkbox" name="' + country + '"/>' + country + '\
//                                <input id="countryCheckB" class="countrycb-hidden" type="hidden" name="' + country + '" value="off"/></label>');

            var newCountry1 = {
                country: country,
                toDelete: true
            };
            appendTestCaseCountryCell(newCountry1);

        }
        for (var myCountry in testCase.countryList) {
            $("#testCaseCountryTableBody [value='" + testCase.countryList[myCountry].country + "']").trigger("click");
        }

    });
}


function appendTestCaseCountryCell(testCaseCountry) {
    var doc = new Doc();
    var checkBox = $("<button type=\"button\" disabled></button>").append(testCaseCountry.country).val(testCaseCountry.country);
    var tableRow = $("#testCaseCountryTableBody tr");

    var row = $("<tr></tr>");
    var checkBoxCell = $("<td style=\"align : center;\"></td>").append(checkBox);
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
    row.append(checkBoxCell);
//    country.environment = selectEnvironment.prop("value"); // Value that has been requested by dtb parameter may not exist in combo vlaues so we take the real selected value.
//    country.country = selectCountry.prop("value"); // Value that has been requested by dtb parameter may not exist in combo vlaues so we take the real selected value.
    checkBoxCell.data("country", testCaseCountry);
    tableRow.append(checkBoxCell);
}

/******************************************************************************
 * LABEL MANAGEMENT
 * Load label list
 */
function loadLabel(labelList) {

    var jqxhr = $.get("ReadLabel?system=" + getUser().defaultSystem, "", "json");

    $.when(jqxhr).then(function (data) {
        var messageType = getAlertType(data.messageType);
        //DRAW LABEL LIST
        if (messageType === "success") {
            $('#selectLabel').empty();
            var index;
            for (index = 0; index < data.contentTable.length; index++) {
                //the character " needs a special encoding in order to avoid breaking the string that creates the html element   
                var labelTag = '<div style="float:left"><input name="labelid" id="labelId' + data.contentTable[index].id + '" value="' + data.contentTable[index].id + '" type="checkbox">\n\
                <span class="label label-primary" style="background-color:' + data.contentTable[index].color + '">' + data.contentTable[index].label + '</span></div> ';
                var option = $('<li id="itemLabelId' + data.contentTable[index].id + '" class="list-group-item list-label"></li>')
                        .attr("value", data.contentTable[index].label).html(labelTag);
                $('#selectLabel').append(option);
            }
        } else {
            showMessageMainPage(messageType, data.message);
        }
        //PUT THE TESTCASELABEL AT THE TOP
        var index;
        for (index = 0; index < labelList.length; index++) {
            //For each testcaselabel, put at the top of the list and check them
            var element = $("#itemLabelId" + labelList[index].label.id);
            element.remove();
            $("#selectLabel").prepend(element);
            $("#labelId" + labelList[index].label.id).prop("checked", true);
        }
    }).fail(handleErrorAjaxAfterTimeout);
}
