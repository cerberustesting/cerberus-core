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

$.when($.getScript("js/pages/global/global.js")).then(function () {
    $(document).ready(function () {
        var doc = new Doc();
        var stepList = [];

        // Load invariant list into local storage.
        getSelectInvariant("ACTION", false);
        getSelectInvariant("CONTROL", false);
        getSelectInvariant("CTRLFATAL", false);
        getSelectInvariant("PROPERTYTYPE", false);
        getSelectInvariant("PROPERTYDATABASE", false);
        getSelectInvariant("PROPERTYNATURE", false);

        loadLibraryStep();
        bindToggleCollapse();

        var test = GetURLParameter("test");
        var testcase = GetURLParameter("testcase");

        displayHeaderLabel(doc);
        displayGlobalLabel(doc);
        displayFooter(doc);
        displayInvariantList("group", "GROUP", false);
        displayInvariantList("status", "TCSTATUS", false);
        displayInvariantList("priority", "PRIORITY", false);
        $('[name="origin"]').append('<option value="All">All</option>');
        displayInvariantList("origin", "ORIGIN", false);
        displayInvariantList("active", "TCACTIVE", false);
        displayInvariantList("activeQA", "TCACTIVE", false);
        displayInvariantList("activeUAT", "TCACTIVE", false);
        displayInvariantList("activeProd", "TCACTIVE", false);
        displayApplicationList("application", getUser().defaultSystem);
        displayProjectList("project");
        tinymce.init({
            selector: ".wysiwyg"
        });

        $('#editEntryModal').on('hidden.bs.modal', {extra: "#editEntryModal"}, modalFormCleaner);
        $("#editEntryButton").click(saveUpdateEntryHandler);
        $("#editTcInfo").click({test: test, testcase: testcase}, editEntry);

        $("#manageProp").click(function () {
            $("#propertiesModal").modal('show');
        });

        $("#saveStep").click(saveStep);
        $("#cancelEdit").click(cancelEdit);

        var json;
        var testcaseinfo;
        $.ajax({
            url: "ReadTestCase",
            data: {test: test, testCase: testcase, withStep: true},
            dataType: "json",
            success: function (data) {
                testcaseinfo = data.info;
                loadTestCaseInfo(data.info);
                loadProperties(test, testcase, data.info);
                json = data.stepList;
                sortData(json);
                createStepList(json, stepList);
                drawInheritedProperty(data.inheritedProp);
                listenEnterKeypressWhenFocusingOnDescription();
                setPlaceholder();
            },
            error: showUnexpectedError
        });

        $("#addStep").click({stepList: stepList}, addStep);
        $('#addStepModal').on('hidden.bs.modal', function () {
            $("#importInfo").removeData("stepInfo");
            $("#importInfo").empty();
            $("#addStepModal #description").val("");
            $("#useStep").prop("checked", false);
            $("#importDetail").hide();
        });

        $("#addProperty").click(function () {
            var newProperty = {
                property: "",
                country: [],
                type: "text",
                database: "",
                value1: "",
                value2: "",
                length: 0,
                rowLimit: 0,
                nature: "STATIC",
                toDelete: false
            };

            drawProperty(newProperty, testcaseinfo);
        });

        $("#deleteStep").click(function () {
            var step = $("#stepList .active").data("item");

            step.setDelete();
        });

        $("#editBtn").click(editStep);
        $("#addAction").click(function () {
            $.when(addAction()).then(function (action) {
                listenEnterKeypressWhenFocusingOnDescription();
                $($(action.html[0]).find(".description")[0]).focus();
            });
        });
        $("#saveScript").click(saveScript);
        $("#runTestCase").click(function () {
            runTestCase(test, testcase);
        });
    });
});

function addAction() {
    var step = $("#stepList li.active").data("item");
    var action = new Action(null, step);
    step.setAction(action);
    return action;
}

function runTestCase(test, testcase) {
    window.location.href = "./RunTests1.jsp?test=" + test + "&testcase=" + testcase;
}

function saveScript() {
    var stepList = $("#stepList li");
    var stepNumber = 0;
    var stepArr = [];

    for (var i = 0; i < stepList.length; i++) {
        var step = $(stepList[i]).data("item");
        var actionArr = [];
        var seq = 0;

        if (!step.toDelete) {
            var actionList = step.stepActionContainer.children(".action-group").children(".action");

            stepNumber++;
            step.setStep(stepNumber);

            for (var j = 0; j < actionList.length; j++) {
                var action = $(actionList[j]).data("item");
                var controlNumber = 0;
                var controlArr = [];

                if (!action.toDelete) {
                    var controlList = action.html.children(".control");

                    seq++;
                    action.setStep(stepNumber);
                    action.setSequence(seq);

                    for (var k = 0; k < controlList.length; k++) {
                        var control = $(controlList[k]).data("item");

                        if (!control.toDelete) {
                            controlNumber++;
                            control.setStep(stepNumber);
                            control.setSequence(seq);
                            control.setControl(controlNumber);
                            controlArr.push(control.getJsonData());
                        }
                    }
                }
                var actionJson = action.getJsonData();
                actionJson.controlArr = controlArr;
                actionArr.push(actionJson);
            }
            var stepJson = step.getJsonData();
            stepJson.actionArr = actionArr;
            stepArr.push(stepJson);
        }
    }

    var properties = $("#propTable tr");
    var propArr = [];
    for (var i = 0; i < properties.length; i++) {
        propArr.push($(properties[i]).data("property"));
    }

    $.ajax({
        url: "UpdateTestCaseWithDependencies1",
        async: true,
        method: "POST",
        data: {informationInitialTest: GetURLParameter("test"),
            informationInitialTestCase: GetURLParameter("testcase"),
            informationTest: GetURLParameter("test"),
            informationTestCase: GetURLParameter("testcase"),
            stepArray: JSON.stringify(stepArr),
            propArr: JSON.stringify(propArr)},
        success: function () {
            location.reload();
        },
        error: showUnexpectedError
    });
}

function drawProperty(property, testcaseinfo) {
    var selectType = getSelectInvariant("PROPERTYTYPE", false);
    var selectDB = getSelectInvariant("PROPERTYDATABASE", false);
    var selectNature = getSelectInvariant("PROPERTYNATURE", false);
    var deleteBtn = $("<button></button>").addClass("btn btn-default btn-xs").append($("<span></span>").addClass("glyphicon glyphicon-trash"));
    var propertyInput = $("<input>").addClass("form-control input-sm").val(property.property);
    var valueInput = $("<textarea></textarea>").addClass("form-control input-sm").val(property.value1);
    var lengthInput = $("<input>").addClass("form-control input-sm").val(property.length);
    var rowLimitInput = $("<input>").addClass("form-control input-sm").val(property.rowLimit);
    var table = $("#propTable");

    var row = $("<tr></tr>");
    var deleteBtnRow = $("<td></td>").append(deleteBtn);
    var propertyName = $("<td></td>").append(propertyInput);
    var country = $("<td></td>").append(getTestCaseCountry(testcaseinfo.countryList, property.country, false));
    var type = $("<td></td>").append(selectType.val(property.type));
    var db = $("<td></td>").append(selectDB.val(property.database));
    var value = $("<td></td>").append(valueInput);
    var length = $("<td></td>").append(lengthInput);
    var rowLimit = $("<td></td>").append(rowLimitInput);
    var nature = $("<td></td>").append(selectNature.val(property.nature));

    deleteBtn.click(function () {
        property.toDelete = (property.toDelete) ? false : true;

        if (property.toDelete) {
            row.addClass("danger");
        } else {
            row.removeClass("danger");
        }
    });

    propertyInput.change(function () {
        property.property = $(this).val();
    });

    selectType.change(function () {
        property.type = $(this).val();
    });

    selectDB.change(function () {
        property.database = $(this).val();
    });

    valueInput.change(function () {
        property.value1 = $(this).val();
    });

    lengthInput.change(function () {
        property.length = $(this).val();
    });

    rowLimitInput.change(function () {
        property.rowLimit = $(this).val();
    });

    selectNature.change(function () {
        property.nature = $(this).val();
    });

    row.data("property", property);
    row.append(deleteBtnRow);
    row.append(propertyName);
    row.append(country);
    row.append(type);
    row.append(db);
    row.append(value);
    row.append(length);
    row.append(rowLimit);
    row.append(nature);
    table.append(row);
}

function drawInheritedProperty(propList) {
    var selectType = getSelectInvariant("PROPERTYTYPE", false);
    var selectDB = getSelectInvariant("PROPERTYDATABASE", false);
    var selectNature = getSelectInvariant("PROPERTYNATURE", false);
    var table = $("#inheritedPropTable");

    for (var index = 0; index < propList.length; index++) {
        var property = propList[index];

        var row = $("<tr></tr>");
        var deleteBtn = $("<td></td>").append($("<button></button>").addClass("btn btn-default btn-xs").append($("<span></span>").addClass("glyphicon glyphicon-trash")));
        var propertyName = $("<td></td>").append($("<input>").addClass("form-control input-sm").val(property.property).prop("readonly", true));
        var country = $("<td></td>").append(getTestCaseCountry(property.country, property.country, true));
        var type = $("<td></td>").append(selectType.clone().val(property.type).prop("disabled", "disabled"));
        var db = $("<td></td>").append(selectDB.clone().val(property.database).prop("disabled", "disabled"));
        var value = $("<td></td>").append($("<textarea></textarea>").addClass("form-control input-sm").val(property.value1).prop("readonly", true));
        var length = $("<td></td>").append($("<input>").addClass("form-control input-sm").val(property.length).prop("readonly", true));
        var rowLimit = $("<td></td>").append($("<input>").addClass("form-control input-sm").val(property.rowLimit).prop("readonly", true));
        var nature = $("<td></td>").append(selectNature.clone().val(property.nature).prop("disabled", "disabled"));

        row.data("property", property);
        row.append(deleteBtn);
        row.append(propertyName);
        row.append(country);
        row.append(type);
        row.append(db);
        row.append(value);
        row.append(length);
        row.append(rowLimit);
        row.append(nature);
        table.append(row);
    }
}

function loadProperties(test, testcase, testcaseinfo) {
    $.ajax({
        url: "GetPropertiesForTestCase",
        data: {test: test, testcase: testcase},
        async: true,
        success: function (data) {

            for (var index = 0; index < data.length; index++) {
                var property = data[index];

                property.toDelete = false;
                drawProperty(property, testcaseinfo);
            }
        },
        error: showUnexpectedError
    });
}

function getTestCaseCountry(countryList, countryToCheck, isDisabled) {
    var html = [];
    var cpt = 0;
    var div = $("<div></div>").addClass("checkbox");

    $.each(countryList, function (index) {
        var country;

        if (typeof index === "number") {
            country = countryList[index];
        } else if (typeof index === "string") {
            country = index;
        }
        var input = $("<input>").attr("type", "checkbox").attr("name", country);

        if (countryToCheck.indexOf(country) !== -1) {
            input.prop("checked", true);
        }
        if (isDisabled) {
            input.prop("disabled", "disabled");
        } else {
            input.change(function () {
                var country = $(this).prop("name");
                var checked = $(this).prop("checked");
                var index = countryToCheck.indexOf(country);

                if (checked && index === -1) {
                    countryToCheck.push(country);
                } else if (!checked && index !== -1) {
                    countryToCheck.splice(index, 1);
                }

                console.log(countryToCheck);
            });
        }

        div.append($("<label></label>").addClass("checkbox-inline")
                .append(input)
                .append(country));

        cpt++;
        if (cpt % 3 === 0) {
            div = $("<div></div>").addClass("checkbox");
        }
        html.push(div);
    });

    return html;
}

function loadTestCaseInfo(info) {
    $(".testTestCase #test").text(info.test);
    $(".testTestCase #testCase").text(info.testCase);
    $(".testTestCase #description").text(info.shortDescription);
}

function cancelEdit() {
    $("#editStep").hide();
    $("#editStepDescription").val("");
    $("#stepDescription").show();
    $("#stepInfo").show();
}

function editStep() {
    var step = $("#stepList li.active").data("item");

    $("#stepDescription").hide();
    $("#stepInfo").hide();
    $("#editStepDescription").prop("placeholder", "Description").prop("maxlength", "150").val(step.description);
    $("#editStep").show();

    if (step.useStep === "Y") {
        $("#addInLibArea").hide();
    } else {
        $("#addInLibArea").show();
    }
}

function saveStep() {
    var stepHtml = $("#stepList li.active");
    var stepData = stepHtml.data("item");

    stepData.setDescription($("#editStepDescription").val());

    if ($("#addInLib").prop("checked")) {
        stepData.inLibrary = "Y";
    } else {
        stepData.inLibrary = "Y";
    }

    cancelEdit();
}

function addStep(event) {
    var stepList = event.data.stepList;
    $("#addStepModal").modal('show');

    $(".sub-item").click(function () {
        var stepInfo = $(this).data("stepInfo");

        $("#importInfo").text("Imported from " + stepInfo.test + " - " + stepInfo.testCase + " - " + stepInfo.step + ")").data("stepInfo", stepInfo);
        $("#addStepModal #description").val(stepInfo.description);
        $("#useStep").prop("checked", true);

        $("#importDetail").show();
    });

    $("#addStepConfirm").unbind("click").click(function (event) {
        var stepNumber = $("#stepList li").length + 1;
        var step = {"inLibrary": "N",
            "objType": "step",
            "useStepTest": "",
            "useStepTestCase": "",
            "useStep": "N",
            "description": "",
            "useStepStep": -1,
            "step": stepNumber,
            "actionList": []};

        step.description = $("#addStepModal #description").val();
        if ($("#importInfo").data("stepInfo")) {
            var useStep = $("#importInfo").data("stepInfo");
            $.ajax({
                url: "ReadTestCaseStep",
                data: {test: useStep.test, testcase: useStep.testCase, step: useStep.step},
                async: false,
                success: function (data) {
                    step.actionList = data.tcsActionList;

                    for (var index = 0; index < data.tcsActionControlList.length; index++) {
                        var control = data.tcsActionControlList[index];

                        step.actionList[control.sequence - 1].controlList.push(control);
                    }
                    sortStep(step);
                }
            });
            if ($("#useStep").prop("checked")) {
                step.useStep = "Y";
                step.useStepTest = useStep.test;
                step.useStepTestCase = useStep.testCase;
                step.useStepStep = useStep.step;
            }
        }
        var stepObj = new Step(step, stepList);

        stepObj.draw();
        stepList.push(stepObj);
    });
}

function createStepList(data, stepList) {
    for (var i = 0; i < data.length; i++) {
        var step = data[i];
        var stepObj = new Step(step, stepList);

        stepObj.draw();
        stepList.push(stepObj);
    }
    if (stepList.length > 0) {
        $(stepList[0].html[0]).click();
    }
}

/** LIBRARY STEP UTILY FUNCTIONS **/

function loadLibraryStep() {
    $.ajax({
        url: "GetStepInLibrary",
        data: {system: getUser().defaultSystem},
        async: true,
        success: function (data) {
            var test = {};

            for (var index = 0; index < data.testCaseStepList.length; index++) {
                var step = data.testCaseStepList[index];

                if (!test.hasOwnProperty(step.test)) {
                    $("#lib").append($("<a></a>").addClass("list-group-item").attr("data-toggle", "collapse").attr("href", "[data-test='" + step.test + "']")
                            .text(step.test).prepend($("<span></span>").addClass("glyphicon glyphicon-chevron-right")));

                    var listGrp = $("<div></div>").addClass("list-group collapse").attr("data-test", step.test);
                    $("#lib").append(listGrp);

                    test[step.test] = listGrp;
                }

                var listGrp = test[step.test];
                listGrp.append($("<a></a>").addClass("list-group-item sub-item").attr("href", "#").text(step.description).data("stepInfo", step));
            }
            $('.list-group-item').on('click', function () {
                $('.glyphicon', this)
                        .toggleClass('glyphicon-chevron-right')
                        .toggleClass('glyphicon-chevron-down');
            });
        }
    });
}


/** EDIT TEST CASE INFO **/

function editEntry(event) {
    var test = event.data.test;
    var testCase = event.data.testcase;
    clearResponseMessageMainPage();
    var jqxhr = $.getJSON("ReadTestCase", "test=" + encodeURIComponent(test) + "&testCase=" + encodeURIComponent(testCase));
    $.when(jqxhr).then(function (data) {

        var formEdit = $('#editEntryModal');
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

            if (data.bugID !== "" && bugTrackerUrl) {
                bugTrackerUrl = bugTrackerUrl.replace("%BUGID%", data.bugID);
            }

            formEdit.find("#link").prop("href", bugTrackerUrl).text(bugTrackerUrl);

        });

        //test info
        formEdit.find("#test").prop("value", data.test);
        formEdit.find("#testCase").prop("value", data.testCase);

        //test case info
        formEdit.find("#creator").prop("value", data.creator);
        formEdit.find("#lastModifier").prop("value", data.lastModifier);
        formEdit.find("#implementer").prop("value", data.implementer);
        formEdit.find("#tcDateCrea").prop("value", data.tcDateCrea);
        formEdit.find("#ticket").prop("value", data.ticket);
        formEdit.find("#function").prop("value", data.function);
        formEdit.find("#origin").prop("value", data.origin);
        formEdit.find("#refOrigin").prop("value", data.refOrigin);
        formEdit.find("#project").prop("value", data.project);

        // test case parameters
        formEdit.find("#application").prop("value", data.application);
        formEdit.find("#group").prop("value", data.group);
        formEdit.find("#status").prop("value", data.status);
        formEdit.find("#priority").prop("value", data.priority);
        formEdit.find("#actQA").prop("value", data.runQA);
        formEdit.find("#actUAT").prop("value", data.runUAT);
        formEdit.find("#actProd").prop("value", data.runPROD);
        for (var country in data.countryList) {
            $('#countryList input[name="' + data.countryList[country] + '"]').prop("checked", true);
        }
        formEdit.find("#shortDesc").prop("value", data.shortDescription);
        tinyMCE.get('behaviorOrValueExpected1').setContent(data.description);
        tinyMCE.get('howTo1').setContent(data.howTo);

        //activation criteria
        formEdit.find("#active").prop("value", data.active);
        formEdit.find("#bugId").prop("value", data.bugID);
        formEdit.find("#comment").prop("value", data.comment);


        //We desactivate or activate the access to the fields depending on if user has the credentials.
        if (!(data["hasPermissionsUpdate"])) { // If readonly, we only readonly all fields
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
            var myCountryList = $('#countryList');
            myCountryList.find("[class='countrycb']").prop("disabled", "disabled");
            formEdit.find("#shortDesc").prop("readonly", "readonly");
            tinyMCE.get('behaviorOrValueExpected1').getBody().setAttribute('contenteditable', false);
            tinyMCE.get('howTo1').getBody().setAttribute('contenteditable', false);
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
            var myCountryList = $('#countryList');
            myCountryList.find("[class='countrycb']").removeProp("disabled");
            formEdit.find("#shortDesc").removeProp("readonly");
            tinyMCE.get('behaviorOrValueExpected1').getBody().setAttribute('contenteditable', true);
            tinyMCE.get('howTo1').getBody().setAttribute('contenteditable', true);
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
        }

        formEdit.modal('show');
    });
}

function saveUpdateEntryHandler() {
    clearResponseMessage($('#editEntryModal'));

    var formEdit = $('#editEntryModalForm');
    tinyMCE.triggerSave();

    showLoaderInModal('#editEntryModal');
    updateEntry("UpdateTestCase2", formEdit, "#testCaseTable");
}

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

            formEdit.find("#fromSprint").prop("value", editData.fromSprint);
            formEdit.find("#toSprint").prop("value", editData.toSprint);
            formEdit.find("#targetSprint").prop("value", editData.targetSprint);
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

            formEdit.find("#fromRevision").prop("value", editData.fromRevision);
            formEdit.find("#toRevision").prop("value", editData.toRevision);
            formEdit.find("#targetRevision").prop("value", editData.targetRevision);
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

/** DRAG AND DROP HANDLERS **/

var source;

function isBefore(a, b) {
    if (a !== b && a.parentNode === b.parentNode) {
        for (var cur = a; cur; cur = cur.nextSibling) {
            if (cur === b) {
                return true;
            }
        }
    }
    return false;
}

function handleDragStart(event) {
    var dataTransfer = event.originalEvent.dataTransfer;
    var obj = this.parentNode;
    var offsetX = 50;
    var offsetY = 50;
    var img;

    if ($(obj).data("item") instanceof Action) {
        img = obj.parentNode;
    } else if ($(obj).data("item") instanceof Control) {
        img = obj;
    } else {
        img = obj;
        offsetX = 15;
        offsetY = 15;
    }

    source = obj;
    obj.style.opacity = '0.4';
    dataTransfer.effectAllowed = 'move';
    dataTransfer.setData('text/html', img.innerHTML);
    dataTransfer.setDragImage(img, offsetX, offsetY);
}

function handleDragEnter(event) {
    var target = this.parentNode;
    var sourceData = $(source).data("item");
    var targetData = $(target).data("item");

    if (sourceData instanceof Action && targetData instanceof Action) {
        if (isBefore(source.parentNode, target.parentNode)) {
            $(target).parent(".action-group").after(source.parentNode);
        } else {
            $(target).parent(".action-group").before(source.parentNode);
        }
    } else if (sourceData instanceof Control &&
            (targetData instanceof Action || targetData instanceof Control)) {
        if (isBefore(source, target) || targetData instanceof Action) {
            $(target).after(source);
        } else {
            $(target).before(source);
        }
    } else if (sourceData instanceof Step && targetData instanceof Step) {
        if (isBefore(source, target)) {
            $(target).after(source);
        } else {
            $(target).before(source);
        }
    }
}

function handleDragOver(event) {
    var e = event.originalEvent;

    if (e.preventDefault) {
        e.preventDefault(); // Necessary. Allows us to drop.
    }
    e.dataTransfer.dropEffect = 'move';

    return false;
}

function handleDragLeave(event) {

}

function handleDrop(event) {
    var e = event.originalEvent;

    if (e.stopPropagation) {
        e.stopPropagation(); // stops the browser from redirecting.
    }

    return false;
}

function handleDragEnd(event) {
    this.parentNode.style.opacity = '1';
}

/** DATA AGREGATION **/

function sortStep(step) {
    for (var j = 0; j < step.actionList.length; j++) {
        var action = step.actionList[j];

        action.controlList.sort(function (a, b) {
            return a.control - b.control;
        });
    }

    step.actionList.sort(function (a, b) {
        return a.sequence - b.sequence;
    });
}

function sortData(agreg) {
    for (var i = 0; i < agreg.length; i++) {
        var step = agreg[i];

        sortStep(step);
    }

    agreg.sort(function (a, b) {
        return a.step - b.step;
    });
}

/** JAVASCRIPT OBJECT **/

function Step(json, stepList) {
    this.stepActionContainer = $("<div></div>").addClass("step-container").css("display", "none");

    this.test = json.test;
    this.testcase = json.testCase;
    this.step = json.step;
    this.description = json.description;
    this.useStep = json.useStep;
    this.useStepTest = json.useStepTest;
    this.useStepTestCase = json.useStepTestCase;
    this.useStepStep = json.useStepStep;
    this.inLibrary = json.inLibrary;
    this.actionList = [];
    this.setActionList(json.actionList);

    this.stepList = stepList;
    this.toDelete = false;

    this.html = $("<li></li>").addClass("list-group-item row").css("margin-left", "0px");
    this.textArea = $("<div></div>").addClass("col-lg-10").addClass("step-description").text(this.description);

}

Step.prototype.draw = function () {
    var htmlElement = this.html;
    var drag = $("<div></div>").addClass("col-lg-2 drag-step").prop("draggable", true)
            .append($("<span></span>").addClass("fa fa-ellipsis-v"));

    drag.on("dragstart", handleDragStart);
    drag.on("dragenter", handleDragEnter);
    drag.on("dragover", handleDragOver);
    drag.on("dragleave", handleDragLeave);
    drag.on("drop", handleDrop);
    drag.on("dragend", handleDragEnd);

    htmlElement.append(drag);
    htmlElement.append(this.textArea);
    htmlElement.data("item", this);

    htmlElement.click(this.show);

    $("#stepList").append(htmlElement);
    $("#actionContainer").append(this.stepActionContainer);
};

Step.prototype.show = function () {
    var object = $(this).data("item");

    cancelEdit();

    for (var i = 0; i < object.stepList.length; i++) {
        var step = object.stepList[i];

        step.stepActionContainer.hide();
        step.html.removeClass("active");
    }

    $(this).addClass("active");

    if (object.toDelete) {
        $("#deleteStep span").removeClass();
        $("#deleteStep span").addClass("glyphicon glyphicon-remove");
    } else {
        $("#deleteStep span").removeClass();
        $("#deleteStep span").addClass("glyphicon glyphicon-trash");
    }

    if (object.inLibrary === "Y") {
        $("#addInLib").prop("checked", true);
    } else {
        $("#addInLib").prop("checked", false);
    }

    if (object.useStep === "Y") {
        $("#libInfo").text("(Imported from " + object.useStepTest + " - " + object.useStepTestCase + " - " + object.useStepStep + " )");
    } else {
        $("#libInfo").text("");
    }

    object.stepActionContainer.show();
    $("#stepDescription").text(object.description);
    $("#stepInfo").show();
};

Step.prototype.setActionList = function (actionList) {
    for (var i = 0; i < actionList.length; i++) {
        this.setAction(actionList[i]);
    }
};

Step.prototype.setAction = function (action) {
    if (action instanceof Action) {
        action.draw();
        this.actionList.push(action);
    } else {
        var actionObj = new Action(action, this);

        actionObj.draw();
        this.actionList.push(actionObj);
    }
};

Step.prototype.setDescription = function (description) {
    this.description = description;
    this.textArea.text(description);
    $("#stepDescription").text(description);
};

Step.prototype.setDelete = function () {
    this.toDelete = (this.toDelete) ? false : true;

    if (this.toDelete) {
        $("#deleteStep span").removeClass();
        $("#deleteStep span").addClass("glyphicon glyphicon-remove");
    } else {
        $("#deleteStep span").removeClass();
        $("#deleteStep span").addClass("glyphicon glyphicon-trash");
    }

    for (var i = 0; i < this.stepList.length; i++) {
        var step = this.stepList[i];

        if (step.toDelete) {
            step.html.addClass("list-group-item-danger");
        } else {
            step.html.removeClass("list-group-item-danger");
        }
    }
};

Step.prototype.setStep = function (step) {
    this.step = step;
};

Step.prototype.getJsonData = function () {
    var json = {};

    json.toDelete = this.toDelete;
    json.test = this.test;
    json.testcase = this.testcase;
    json.step = this.step;
    json.description = this.description;
    json.useStep = this.useStep;
    json.useStepTest = this.useStepTest;
    json.useStepTestCase = this.useStepTestCase;
    json.useStepStep = this.useStepStep;
    json.inLibrary = this.inLibrary;

    return json;
};

function Action(json, parentStep) {
    this.html = $("<div></div>").addClass("action-group");
    this.parentStep = parentStep;

    if (json !== null) {
        this.test = json.test;
        this.testcase = json.testCase;
        this.step = json.step;
        this.sequence = json.sequence;
        this.description = json.description;
        this.action = json.action;
        this.object = json.object;
        this.property = json.property;
        this.screenshotFileName = json.screenshotFileName;
        this.controlList = [];
        this.setControlList(json.controlList);
    } else {
        this.test = "";
        this.testcase = "";
        this.step = parentStep.step;
        this.sequence = "";
        this.description = "";
        this.action = "Unknown";
        this.object = "";
        this.property = "";
        this.screenshotFileName = "";
        this.controlList = [];
    }

    this.toDelete = false;
}

Action.prototype.draw = function () {
    var htmlElement = this.html;
    var action = this;
    var row = $("<div></div>").addClass("step-action row").addClass("action");
    var type = $("<div></div>").addClass("type");
    var drag = $("<div></div>").addClass("drag-step-action col-lg-1").prop("draggable", true).append(type);
    var addBtn = $("<button></button>").addClass("btn btn-success btn-xs add-btn").append($("<span></span>").addClass("glyphicon glyphicon-plus"));
    var supprBtn = $("<button></button>").addClass("btn btn-danger btn-xs add-btn").append($("<span></span>").addClass("glyphicon glyphicon-trash"));
    var btnGrp = $("<div></div>").addClass("btn-group").append(addBtn).append(supprBtn);

    if (this.parentStep.useStep === "N") {
        drag.append($("<span></span>").addClass("fa fa-ellipsis-v"));
        drag.on("dragstart", handleDragStart);
        drag.on("dragenter", handleDragEnter);
        drag.on("dragover", handleDragOver);
        drag.on("dragleave", handleDragLeave);
        drag.on("drop", handleDrop);
        drag.on("dragend", handleDragEnd);
    }

    addBtn.click(function () {
        var control = new Control(null, action);

        action.setControl(control);
    });

    supprBtn.click(function () {
        action.toDelete = (action.toDelete) ? false : true;

        if (action.toDelete) {
            action.html.addClass("toDelete");
        } else {
            action.html.removeClass("toDelete");
        }
    });

    row.append(drag);
    row.append(this.generateContent());
    row.append(btnGrp);
    row.data("item", this);
    htmlElement.prepend(row);

    this.parentStep.stepActionContainer.append(htmlElement);
};

Action.prototype.setControlList = function (controlList) {
    for (var i = 0; i < controlList.length; i++) {
        this.setControl(controlList[i]);
    }
};

Action.prototype.setControl = function (control) {
    if (control instanceof Control) {
        control.draw();
        this.controlList.push(control);
    } else {
        var controlObj = new Control(control, this);

        controlObj.draw();
        this.controlList.push(controlObj);
    }
};

Action.prototype.setStep = function (step) {
    this.step = step;
};

Action.prototype.setSequence = function (sequence) {
    this.sequence = sequence;
};

Action.prototype.generateContent = function () {
    var obj = this;
    var content = $("<div></div>").addClass("content col-lg-10");
    var firstRow = $("<div></div>").addClass("row");
    var secondRow = $("<div></div>").addClass("row form-inline");

    var actionList = $("<select></select>").addClass("form-control input-sm");
    var descField = $("<input>").addClass("description").addClass("form-control").prop("placeholder", "Describe this action");
    var objectField = $("<input>").addClass("form-control input-sm");
    var propertyField = $("<input>").addClass("form-control input-sm");

    descField.val(this.description);
    descField.on("change", function () {
        obj.description = descField.val();
    });

    actionList = getSelectInvariant("ACTION", false);
    actionList.val(this.action);
    actionList.on("change", function () {
        obj.action = actionList.val();
        setPlaceholder();
    });

    objectField.val(this.object);
    objectField.on("change", function () {
        obj.object = objectField.val();
    });

    propertyField.val(this.property);
    propertyField.on("change", function () {
        obj.property = propertyField.val();
    });

    firstRow.append(descField);
    secondRow.append($("<span></span>").addClass("col-lg-4").append(actionList));
    secondRow.append($("<span></span>").addClass("col-lg-4").append(objectField));
    secondRow.append($("<span></span>").addClass("col-lg-4").append(propertyField));

    if (this.parentStep.useStep === "Y") {
        descField.prop("readonly", true);
        objectField.prop("readonly", true);
        propertyField.prop("readonly", true);
        actionList.prop("disabled", "disabled");
    }

    content.append(firstRow);
    content.append(secondRow);

    return content;
};

Action.prototype.getJsonData = function () {
    var json = {};

    json.toDelete = this.toDelete;
    json.test = this.test;
    json.testcase = this.testcase;
    json.step = this.step;
    json.sequence = this.sequence;
    json.description = this.description;
    json.action = this.action;
    json.object = this.object;
    json.property = this.property;
    json.screenshotFileName = "";

    return json;
};

function Control(json, parentAction) {
    if (json !== null) {
        this.test = json.test;
        this.testcase = json.testCase;
        this.step = json.step;
        this.sequence = json.sequence;
        this.control = json.control;
        this.description = json.description;
        this.type = json.type;
        this.controlValue = json.controlValue;
        this.controlProperty = json.controlProperty;
        this.fatal = json.fatal;
        this.screenshotFileName = "";
    } else {
        this.test = "";
        this.testcase = "";
        this.step = parentAction.step;
        this.sequence = parentAction.sequence;
        this.control = "";
        this.description = "";
        this.type = "Unknown";
        this.controlValue = "";
        this.controlProperty = "";
        this.fatal = "Y";
        this.screenshotFileName = "";
    }

    this.parentStep = parentAction.parentStep;
    this.parentAction = parentAction;

    this.toDelete = false;

    this.html = $("<div></div>").addClass("step-action row").addClass("control");
}

Control.prototype.draw = function () {
    var htmlElement = this.html;
    var control = this;
    var type = $("<div></div>").addClass("type");
    var drag = $("<div></div>").addClass("drag-step-action col-lg-1").prop("draggable", true).append(type);
    var supprBtn = $("<button></button>").addClass("btn btn-danger btn-xs add-btn").append($("<span></span>").addClass("glyphicon glyphicon-trash"));
    var btnGrp = $("<div></div>").addClass("btn-group").append(supprBtn);
    var content = this.generateContent();

    if (this.parentAction.parentStep.useStep === "N") {
        drag.append($("<span></span>").addClass("fa fa-ellipsis-v"));
        drag.on("dragstart", handleDragStart);
        drag.on("dragenter", handleDragEnter);
        drag.on("dragover", handleDragOver);
        drag.on("dragleave", handleDragLeave);
        drag.on("drop", handleDrop);
        drag.on("dragend", handleDragEnd);
    }

    supprBtn.click(function () {
        control.toDelete = (control.toDelete) ? false : true;

        if (control.toDelete) {
            control.html.addClass("toDelete");
        } else {
            control.html.removeClass("toDelete");
        }
    });

    htmlElement.append(drag);
    htmlElement.append(content);
    htmlElement.append(btnGrp);
    htmlElement.data("item", this);

    this.parentAction.html.append(htmlElement);
};

Control.prototype.setStep = function (step) {
    this.step = step;
};

Control.prototype.setSequence = function (sequence) {
    this.sequence = sequence;
};

Control.prototype.setControl = function (control) {
    this.control = control;
};

Control.prototype.generateContent = function () {
    var obj = this;
    var content = $("<div></div>").addClass("content col-lg-10");
    var firstRow = $("<div></div>").addClass("row");
    var secondRow = $("<div></div>").addClass("row form-inline");

    var controlList = $("<select></select>").addClass("form-control input-sm");
    var descField = $("<input>").addClass("description").addClass("form-control").prop("placeholder", "Description");
    var objectField = $("<input>").addClass("form-control input-sm");
    var propertyField = $("<input>").addClass("form-control input-sm");
    var fatalField = $("<select></select>").addClass("form-control input-sm");

    descField.val(this.description);
    descField.on("change", function () {
        obj.description = descField.val();
    });

    controlList = getSelectInvariant("CONTROL", false);
    controlList.val(this.type);
    controlList.on("change", function () {
        obj.type = controlList.val();
    });

    objectField.val(this.object);
    objectField.on("change", function () {
        obj.controlValue = objectField.val();
    });

    propertyField.val(this.property);
    propertyField.on("change", function () {
        obj.controlProperty = propertyField.val();
    });

    fatalField = getSelectInvariant("CTRLFATAL", false);
    fatalField.on("change", function () {
        obj.fatal = fatalField.val();
    });

    firstRow.append(descField);
    secondRow.append($("<span></span>").addClass("col-lg-3").append(controlList));
    secondRow.append($("<span></span>").addClass("col-lg-4").append(objectField));
    secondRow.append($("<span></span>").addClass("col-lg-4").append(propertyField));
    secondRow.append($("<span></span>").addClass("col-lg-1").append(fatalField));

    if (this.parentStep.useStep === "Y") {
        descField.prop("readonly", true);
        objectField.prop("readonly", true);
        propertyField.prop("readonly", true);
        controlList.prop("disabled", "disabled");
        fatalField.prop("disabled", "disabled");
    }

    content.append(firstRow);
    content.append(secondRow);

    return content;
};

Control.prototype.getJsonData = function () {
    var json = {};

    json.toDelete = this.toDelete;
    json.test = this.test;
    json.testcase = this.testcase;
    json.step = this.step;
    json.sequence = this.sequence;
    json.control = this.control;
    json.description = this.description;
    json.type = this.type;
    json.controlValue = this.controlValue;
    json.controlProperty = this.controlProperty;
    json.fatal = this.fatal;
    json.screenshotFileName = this.screenshotFileName;

    return json;
};

/**
 * Call Add Action anf focus to next description when 
 * focusing on description and clicking on enter
 * @returns {undefined}
 */
function listenEnterKeypressWhenFocusingOnDescription() {
    $("input[class='description form-control']").each(function (index, field) {
        $(field).off('keydown');
        $(field).on('keydown', function (e) {
            if (e.which === 13) {
                //if description is not empty, create new action
                if ($(field)[0].value.length !== 0) {
                    $.when(addAction()).then(function (action) {
                        listenEnterKeypressWhenFocusingOnDescription();
                        $($(action.html[0]).find(".description")[0]).focus();
                    });
                } else {
                    //if description is empty, create action or control depending on field
                    if ($(field).closest(".step-action").hasClass("action")) {
                        var newAction = $(field).closest(".action-group");
                        var oldAction = newAction.prev().find(".step-action.row.action").last();
                        newAction.remove();
                        $.when(addControl(oldAction.data("item"))).then(function (action) {
                            listenEnterKeypressWhenFocusingOnDescription();
                            $($(action.html[0]).find(".description")[0]).focus();
                        });
                    } else {
                        var newAction = $(field).closest(".step-action");
                        newAction.remove();
                        $.when(addAction()).then(function (action) {
                            listenEnterKeypressWhenFocusingOnDescription();
                            $($(action.html[0]).find(".description")[0]).focus();
                        });
                    }
                }
            }
        });
    });
}

function addControl(action) {
    var control = new Control(null, action);
    action.setControl(control);
    return control;
}

function setPlaceholder() {
    /**
     * Todo : GetFromDatabase
     */
    var placeHoldersList = {"fr": [
            {"type": "Unknown", "object": null, "property": null},
            {"type": "click", "object": "Chemin vers l'élement à cliquer", "property": null},
            {"type": "clickAndWait", "object": "Action Depreciée", "property": "Action Depreciée"},
            {"type": "calculateProperty", "object": null, "property": "Nom d'une Proprieté"},
            {"type": "doubleClick", "object": "Chemin vers l'élement à double-cliquer", "property": null},
            {"type": "enter", "object": "Action Depreciée", "property": "Action Depreciée"},
            {"type": "focusToIframe", "object": "Identifiant de l'iFrame à cibler", "property": null},
            {"type": "focusDefaultIframe", "object": null, "property": null},
            {"type": "keypress", "object": "[opt] Chemin vers l'élement à cibler", "property": ""},
            {"type": "mouseLeftButtonPress", "object": "Chemin vers l'élement à cibler", "property": null},
            {"type": "mouseLeftButtonRelease", "object": "Chemin vers l'élement", "property": null},
            {"type": "mouseOver", "object": "Chemin vers l'élement", "property": null},
            {"type": "mouseOverAndWait", "object": "Action Depreciée", "property": "Action Depreciée"},
            {"type": "openUrlWithBase", "object": "/URI à appeler", "property": null},
            {"type": "openUrlLogin", "object": null, "property": null},
            {"type": "openUrl", "object": "URL à appeler", "property": null},
            {"type": "select", "object": "Chemin vers l'élement", "property": "Chemin vers l'option"},
            {"type": "selectAndWait", "object": "Action Depreciée", "property": "Action Depreciée"},
            {"type": "type", "object": "Chemin vers l'élement", "property": "Nom de propriété"},
            {"type": "wait", "object": "Valeur(ms) ou élement", "property": null},
            {"type": "switchToWindow", "object": "Identifiant de fenêtre", "property": null},
            {"type": "callSoap", "object": "Nom du Soap (librairie)", "property": "Nom de propriété"},
            {"type": "callSoapWithBase", "object": "Nom du Soap (librairie)", "property": "Nom de propriété"},
            {"type": "manageDialog", "object": "ok ou cancel", "property": null},
            {"type": "getPageSource", "object": null, "property": null},
            {"type": "removeDifference", "object": "Action Depreciée", "property": "Action Depreciée"},
            {"type": "executeSqlUpdate", "object": "Nom de Base de donnée", "property": "Script à executer"},
            {"type": "executeSqlStoredProcedure", "object": "Nom de Base de donnée", "property": "Procedure Stoquée à executer"},
            {"type": "doNothing", "object": null, "property": null},
            {"type": "callSoap_BETA", "object": "Nom du Soap (librairie)", "property": "Nom de propriété"},
            {"type": "callSoapWithBase_BETA", "object": "Nom du Soap (librairie)", "property": "Nom de propriété"}
        ], "en": [
            {"type": "Unknown", "object": null, "property": null},
            {"type": "click", "object": "Element path", "property": null},
            {"type": "clickAndWait", "object": "Deprecated", "property": "Deprecated"},
            {"type": "calculateProperty", "object": null, "property": "Property Name"},
            {"type": "doubleClick", "object": "Element path", "property": null},
            {"type": "enter", "object": "Deprecated", "property": "Deprecated"},
            {"type": "focusToIframe", "object": "Id of the target iFrame", "property": null},
            {"type": "focusDefaultIframe", "object": null, "property": null},
            {"type": "keypress", "object": "[opt] Element path", "property": ""},
            {"type": "mouseLeftButtonPress", "object": "Element path", "property": null},
            {"type": "mouseLeftButtonRelease", "object": "Element path", "property": null},
            {"type": "mouseOver", "object": "Element path", "property": null},
            {"type": "mouseOverAndWait", "object": "Deprecated", "property": "Deprecated"},
            {"type": "openUrlWithBase", "object": "/URI to call", "property": null},
            {"type": "openUrlLogin", "object": null, "property": null},
            {"type": "openUrl", "object": "URL to call", "property": null},
            {"type": "select", "object": "Element path", "property": "Option path"},
            {"type": "selectAndWait", "object": "Deprecated", "property": "Deprecated"},
            {"type": "type", "object": "Element path", "property": "Property Name"},
            {"type": "wait", "object": "Time(ms) or Element", "property": null},
            {"type": "switchToWindow", "object": "Window id", "property": null},
            {"type": "callSoap", "object": "Soap Name (library)", "property": "Property Name"},
            {"type": "callSoapWithBase", "object": "Soap Name (library)", "property": "Property Name"},
            {"type": "manageDialog", "object": "ok or cancel", "property": null},
            {"type": "getPageSource", "object": null, "property": null},
            {"type": "removeDifference", "object": "Deprecated", "property": "Deprecated"},
            {"type": "executeSqlUpdate", "object": "Database Name", "property": "Script"},
            {"type": "executeSqlStoredProcedure", "object": "Database Name", "property": "Stored Procedure"},
            {"type": "doNothing", "object": null, "property": null},
            {"type": "callSoap_BETA", "object": "Soap Name (library)", "property": "Property Name"},
            {"type": "callSoapWithBase_BETA", "object": "Soap Name (library)", "property": "Property Name"}
        ]};

    var user = getUser();
    user.language;
    var placeHolders = placeHoldersList[user.language];

    $('div[class="row form-inline"] option:selected').each(function (i, e) {

        for (var i = 0; i < placeHolders.length; i++) {
            if (placeHolders[i].type === e.value) {
                if (placeHolders[i].object !== null) {
                    $(e).parent().parent().next().show();
                    $(e).parent().parent().next().find('input').prop("placeholder", placeHolders[i].object);
                } else {
                    $(e).parent().parent().next().hide();
                }
                if (placeHolders[i].property !== null) {
                    $(e).parent().parent().next().next().show();
                    $(e).parent().parent().next().next().find('input').prop("placeholder", placeHolders[i].property);
                } else {
                    $(e).parent().parent().next().next().hide();
                }
            }
        }
    });
}
