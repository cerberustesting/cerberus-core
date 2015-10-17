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

        $("#editEntryButton").click(saveUpdateEntryHandler);

        var urlTag = GetURLParameter('test');
        loadTestFilters(urlTag);

        $('#editEntryModal').on('hidden.bs.modal', {extra: "#editEntryModal"}, modalFormCleaner);
        $('#addEntryModal').on('hidden.bs.modal', {extra: "#addEntryModal"}, modalFormCleaner);

        $("#addEntryButton").click(saveNewEntryHandler);
    });
});

function initPage() {
    var doc = new Doc();

    displayHeaderLabel(doc);
    displayGlobalLabel(doc);
    displayPageLabel(doc);
    displayFooter(doc);
    displayInvariantList("group", "GROUP");
    displayInvariantList("status", "TCSTATUS");
    displayInvariantList("priority", "PRIORITY");
    $('[name="origin"]').append('<option value="All">All</option>');
    displayInvariantList("origin", "ORIGIN");
    displayInvariantList("active", "TCACTIVE");
    displayInvariantList("activeQA", "TCACTIVE");
    displayInvariantList("activeUAT", "TCACTIVE");
    displayInvariantList("activeProd", "TCACTIVE");
    appendCountryList();
    appendApplicationList();
    appendProjectList();
    appendBuildRevList();
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
    
}

function appendBuildRevList() {
    var user = getUser();

    var jqxhr = $.getJSON("GetBuildRevisionInvariant", "System=" + user.defaultSystem + "&level=1");
    $.when(jqxhr).then(function (data) {
        var fromBuild = $("[name=fromSprint]");
        var toBuild = $("[name=toSprint]");
        var targetBuild = $("[name=targetSprint]");

        fromBuild.append($('<option></option>').text("-----").val(""));
        toBuild.append($('<option></option>').text("-----").val(""));
        targetBuild.append($('<option></option>').text("-----").val(""));

        for (var index = 0; index < data.aaData.length; index++) {
            fromBuild.append($('<option></option>').text(data.aaData[index].versionName).val(data.aaData[index].versionName));
            toBuild.append($('<option></option>').text(data.aaData[index].versionName).val(data.aaData[index].versionName));
            targetBuild.append($('<option></option>').text(data.aaData[index].versionName).val(data.aaData[index].versionName));
        }
    });
    var jqxhr = $.getJSON("GetBuildRevisionInvariant", "System=" + user.defaultSystem + "&level=2");
    $.when(jqxhr).then(function (data) {
        var fromRev = $("[name=fromRev]");
        var toRev = $("[name=toRev]");
        var targetRev = $("[name=targetRev]");

        fromRev.append($('<option></option>').text("-----").val(""));
        toRev.append($('<option></option>').text("-----").val(""));
        targetRev.append($('<option></option>').text("-----").val(""));

        for (var index = 0; index < data.aaData.length; index++) {
            fromRev.append($('<option></option>').text(data.aaData[index].versionName).val(data.aaData[index].versionName));
            toRev.append($('<option></option>').text(data.aaData[index].versionName).val(data.aaData[index].versionName));
            targetRev.append($('<option></option>').text(data.aaData[index].versionName).val(data.aaData[index].versionName));
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
                                <input class="countrycb-hidden" type="hidden" name="' + country + '" value="off"/></label>');
        }
    });
}

function appendApplicationList() {
    var user = getUser();

    var jqxhr = $.getJSON("ReadApplication", "system=" + user.defaultSystem);
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

function loadTestFilters(urlTag) {
    var jqxhr = $.get("ReadTest", {sEcho: "1"}, "json");
    $.when(jqxhr).then(function (data) {
        var messageType = getAlertType(data.messageType);
        if (messageType === "success") {
            var index;
            $('#selectTest').append($('<option></option>').attr("value", "")).attr("placeholder", "Select a Test");
            for (index = 0; index < data.contentTable.length; index++) {
                //the character " needs a special encoding in order to avoid breaking the string that creates the html element   
                var encodedString = data.contentTable[index].test.replace(/\"/g, "%22");
                var text = data.contentTable[index].test + ' - ' + data.contentTable[index].description;
                var option = $('<option></option>').attr("value", encodedString).text(text);
                $('#selectTest').append(option);
                $('#testAdd').append($('<option></option>').text(text).val(encodedString));
            }

            //if the tag is passed as a url parameter, then it loads the report from this tag
            if (urlTag !== null) {
                $('#selectTest option[value="' + urlTag + '"]').attr("selected", "selected");
                loadTable();
            }
        } else {
            showMessageMainPage(messageType, data.message);
        }
    }).fail(handleErrorAjaxAfterTimeout);
}

function loadTable() {
    var selectTest = $("#selectTest option:selected").attr("value");

    window.history.pushState('test', '', 'TestCaseList.jsp?test=' + selectTest);

    if ($("#testCaseTable_wrapper").hasClass("initialized")) {
        $("#testCaseList").empty();
        $("#testCaseList").html('<table id="testCaseTable" class="table table-hover display" name="testCaseTable">\n\
                                            </table><div class="marginBottom20"></div>');
    }

    if (selectTest !== "") {
        var jqxhr = $.getJSON("FindInvariantByID", "idName=COUNTRY");

        $.when(jqxhr).then(function (data) {
            var config = new TableConfigurationsServerSide("testCaseTable", "ReadTestCase?test=" + selectTest, "contentTable", aoColumnsFunc(data));

            var table = createDataTableWithPermissions(config, renderOptionsForTestCaseList);
            table.fnSort([1, 'asc']);

            $('#testCaseTable_wrapper').not('.initialized').addClass('initialized');
        });
    }
}

function CreateTestCaseClick() {
    clearResponseMessageMainPage();
    var test = GetURLParameter('test');

    $.ajax({
        url: "ReadTestCase",
        method: "GET",
        data: {test: test, getMaxTC: true},
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

            $('#addEntryModalForm #testCase').val(tcnumber);
        },
        error: function (e) {
            showUnexpectedError();
        }
    });

    if (test !== "") {
        $('#testAdd option[value="' + test + '"]').attr("selected", "selected");
    }

    $('#addEntryModalForm #actProd option[value="N"]').attr("selected", "selected");

    $('#addEntryModal').modal('show');
}

function renderOptionsForTestCaseList(data) {
    var doc = new Doc();
    //check if user has permissions to perform the add and import operations
    if (data["hasPermissions"]) {
        if ($("#createTestCaseButton").length === 0) {
            var contentToAdd = "<div class='marginBottom10'><button id='createTestCaseButton' type='button' class='btn btn-default'>\n\
            " + "Create Test Case" + "</button></div>";

            $("#testCaseTable_wrapper div.ColVis").before(contentToAdd);
            $('#testCaseList #createTestCaseButton').click(data, CreateTestCaseClick);
        }
    }
}

function saveNewEntryHandler() {
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

    showLoaderInModal('#addEntryModal');
    createEntry("CreateTestCase2", formAdd, "#testCaseTable");
}

function saveUpdateEntryHandler() {
    clearResponseMessage($('#editEntryModal'));

    var formEdit = $('#editEntryModalForm');

    showLoaderInModal('#editEntryModal');
    updateEntry("UpdateTestCase2", formEdit, "#testCaseTable");
}

function deleteEntryHandlerClick() {
    var test = GetURLParameter('test');
    var testCase = $('#confirmationModal').find('#hiddenField').prop("value");
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

function deleteEntry(entry) {
    clearResponseMessageMainPage();
    var doc = new Doc();
    var messageComplete = doc.getDocLabel("page_global", "deleteMessage");
    messageComplete = messageComplete.replace("%TABLE%", "TestCase");
    messageComplete = messageComplete.replace("%ENTRY%", entry);
    showModalConfirmation(deleteEntryHandlerClick, "Delete", messageComplete, entry);
}

function editEntry(testCase) {
    clearResponseMessageMainPage();
    var test = GetURLParameter('test');
    var jqxhr = $.getJSON("ReadTestCase", "test=" + test + "&testCase=" + testCase);
    $.when(jqxhr).then(function (data) {

        var formEdit = $('#editEntryModal');

        //test info
        formEdit.find("#test").prop("value", data.test);
        formEdit.find("#testCase").prop("value", data.testCase);
        formEdit.find("#description").prop("value", data.description);
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
        formEdit.find("#shortDesc").prop("value", data.shortDesc);
        formEdit.find("#behaviorOrValueExpected").prop("value", data.behaviorOrValueExpected);
        formEdit.find("#howTo").prop("value", data.howTo);
        //activation criteria
        formEdit.find("#active").prop("value", data.active);
        formEdit.find("#bugId").prop("value", data.bugID);
        formEdit.find("#link").prop("value", data.link);
        formEdit.find("#comment").prop("value", data.comment);
        formEdit.find("#fromSprint").prop("value", data.fromSprint);
        formEdit.find("#toSprint").prop("value", data.toSprint);
        formEdit.find("#targetSprint").prop("value", data.targetSprint);
        formEdit.find("#fromRevision").prop("value", data.fromSprint);
        formEdit.find("#toRevision").prop("value", data.toRevision);
        formEdit.find("#targetRevision").prop("value", data.targetRevision);

        formEdit.modal('show');
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
        url: "UpdateTestCase2",
        method: "POST",
        data: {test: test, testCase: testCase, active: active},
        dataType: "json",
        success: function (data) {
            clearResponseMessageMainPage();
            var messageType = getAlertType(data.messageType);
            //show message in the main page
            showMessageMainPage(messageType, data.message);
        },
        error: function (e) {
            showUnexpectedError();
        }
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
        error: function (e) {
            showUnexpectedError();
        }
    });
}


function aoColumnsFunc(countries) {
    var doc = new Doc();

    var countryLen = countries.length;
    var aoColumns = [
        {
            "data": null,
            "bSortable": false,
            "bSearchable": false,
            "title": doc.getDocOnline("page_global", "columnAction"),
            "sDefaultContent": "",
            "sWidth": "100px",
            "mRender": function (data, type, obj) {
                if (data.hasPermissions) {
                    var editEntry = '<button id="editEntry" onclick="editEntry(\'' + obj["testCase"] + '\');"\n\
                                class="editEntry btn btn-default btn-xs margin-right5" \n\
                                name="editEntry" title="' + "edit test case" + '" type="button">\n\
                                <span class="glyphicon glyphicon-pencil"></span></button>';

                    var deleteEntry = '<button id="deleteEntry" onclick="deleteEntry(\'' + obj["testCase"] + '\');"\n\
                                        class="deleteEntry btn btn-default btn-xs margin-right5" \n\
                                        name="deleteEntry" title="' + "delete test case" + '" type="button">\n\
                                        <span class="glyphicon glyphicon-trash"></span></button>';

                    return '<div class="center btn-group width150">' + editEntry + deleteEntry + '</div>';
                }
            }
        },
        {
            "data": "testCase",
            "sName": "testCase",
            "title": doc.getDocOnline("testcase", "TestCase"),
            "sWidth": "70px",
            "sDefaultContent": ""
        },
        {
            "data": "application",
            "sName": "application",
            "title": doc.getDocOnline("application", "Application"),
            "sWidth": "100px",
            "sDefaultContent": ""
        },
        {
            "data": "project",
            "sName": "project",
            "title": doc.getDocOnline("project", "idproject"),
            "sWidth": "100px",
            "sDefaultContent": ""
        },
        {
            "data": "creator",
            "sName": "creator",
            "title": doc.getDocOnline("testcase", "Creator"),
            "sWidth": "100px",
            "sDefaultContent": ""
        },
        {
            "data": "lastModifier",
            "sName": "lastmodifier",
            "title": doc.getDocOnline("testcase", "LastModifier"),
            "sWidth": "100px",
            "sDefaultContent": ""
        },
        {
            "data": "active",
            "sName": "active",
            "title": doc.getDocOnline("testcase", "TcActive"),
            "sDefaultContent": "",
            "sWidth": "70px",
            "className": "center",
            "mRender": function (data, type, obj) {
                if (data === "Y") {
                    return '<input type="checkbox" name="' + obj["testCase"] + '" data-test="' + obj.test + '" onchange="setActive(this);" checked/>';
                } else if (data === "N") {
                    return '<input type="checkbox" name="' + obj["testCase"] + '" data-test="' + obj.test + '" onchange="setActive(this);" />';
                }
            }
        },
        {
            "data": "status",
            "sName": "status",
            "title": doc.getDocOnline("testcase", "Status"),
            "sWidth": "100px",
            "sDefaultContent": ""
        },
        {
            "data": "priority",
            "sName": "priority",
            "title": "Priority",
            "sWidth": "70px",
            "sDefaultContent": ""
        },
        {
            "data": "origin",
            "sName": "origine",
            "title": doc.getDocOnline("testcase", "Origine"),
            "sWidth": "70px",
            "sDefaultContent": ""
        },
        {
            "data": "refOrigin",
            "sName": "refOrigine",
            "title": doc.getDocOnline("testcase", "RefOrigine"),
            "sWidth": "80px",
            "sDefaultContent": ""
        },
        {
            "data": "group",
            "sName": "group",
            "title": "Group",
            "sWidth": "100px",
            "sDefaultContent": ""
        },
        {
            "data": "shortDescription",
            "sName": "description",
            "title": doc.getDocOnline("testcase", "Description"),
            "sWidth": "300px",
            "sDefaultContent": ""
        },
        {
            "data": "tcDateCrea",
            "sName": "tcDateCrea",
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

                if (row.hasOwnProperty("countryList") && row["countryList"].hasOwnProperty(dataTitle)) {
                    return '<input type="checkbox" name="' + dataTitle + '" data-test="' + row.test + '" data-testcase="' + row.testCase + '" onchange="setCountry(this);" checked/>';
                } else {
                    return '<input type="checkbox" name="' + dataTitle + '" data-test="' + row.test + '" data-testcase="' + row.testCase + '" onchange="setCountry(this);"/>';
                }
            },
            "bSortable": false,
            "bSearchable": false,
            "sClass": "center",
            "title": country,
            "sDefaultContent": ""
        };

        aoColumns.push(column);
    }

    return aoColumns;
}