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
//    displayPageLabel(doc);
    displayFooter(doc);
    displayInvariantList("GROUP", "group");
    displayInvariantList("TCSTATUS", "status");
    displayInvariantList("PRIORITY", "priority");
    displayInvariantList("TCACTIVE", "active");
    displayInvariantList("TCACTIVE", "activeQA");
    displayInvariantList("TCACTIVE", "activeUAT");
    displayInvariantList("TCACTIVE", "activeProd");
    appendCountryList();
    appendApplicationList();
    appendProjectList();
    appendBuildRevList();
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
        var countryList = $("#countryList");
        var res = '<label class="checkbox-inline"><input type="checkbox" name="BE"/>BE</label>';

        for (var index = 0; index < data.length; index++) {
            var country = data[index].value;

            countryList.append('<label class="checkbox-inline"><input type="checkbox" name="' + country + '"/>' + country + '</label>');
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
    $('#addEntryModal').modal('show');
}

function renderOptionsForTestCaseList(data) {
    var doc = new Doc();
    //check if user has permissions to perform the add and import operations
    if (data["hasPermissions"]) {
        if ($("#createTestCaseButton").length === 0) {
            var contentToAdd = "<div class='marginBottom10'><button id='createTestCaseButton' type='button' class='btn btn-default'>\n\
            " + doc.getDocLabel("page_project", "button_create") + "</button></div>";

            $("#testCaseTable_wrapper div.ColVis").before(contentToAdd);
            $('#testCaseList #createTestCaseButton').click(CreateTestCaseClick);
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
    var testCaseEmpty = nameElement.prop("value") === '';
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

function editEntry(testCase) {
    clearResponseMessageMainPage();
    var test = GetURLParameter('test');
    var jqxhr = $.getJSON("ReadTestCase", "test=" + test + "&testCase=" + testCase);
    $.when(jqxhr).then(function (data) {
        console.log(data);

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
        dataType: "json"
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
        data: {test: test, testCase: testCase, country: country, state: state},
        dataType: "json"
    });
}


function aoColumnsFunc(countries) {
    var aoColumns = [
        {
            "data": null,
            "bSortable": false,
            "bSearchable": false,
            "title": "Actions",
            "sDefaultContent": "",
            "sWidth": "100px",
            "mRender": function (data, type, obj) {
                if (data.hasPermissions) {
                    var editEntry = '<button id="editEntry" onclick="editEntry(\'' + obj["testCase"] + '\');"\n\
                                class="editEntry btn btn-default btn-xs margin-right5" \n\
                                name="editEntry" title="' + "edit test case" + '" type="button">\n\
                                <span class="glyphicon glyphicon-pencil"></span></button>';
                    return '<div class="center btn-group width150">' + editEntry + '</div>';
                }
            }
        },
        {
            "data": "testCase",
            "sName": "testCase",
            "title": "Test Case",
            "sWidth": "70px",
            "sDefaultContent": ""
        },
        {
            "data": "application",
            "sName": "application",
            "title": "Application",
            "sWidth": "100px",
            "sDefaultContent": ""
        },
        {
            "data": "project",
            "sName": "project",
            "title": "Project",
            "sWidth": "100px",
            "sDefaultContent": ""
        },
        {
            "data": "creator",
            "sName": "creator",
            "title": "Creator",
            "sWidth": "100px",
            "sDefaultContent": ""
        },
        {
            "data": "lastModifier",
            "sName": "lastmodifier",
            "title": "Last Modifier",
            "sWidth": "100px",
            "sDefaultContent": ""
        },
        {
            "data": "active",
            "sName": "active",
            "title": "Active",
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
            "title": "Status",
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
            "data": "group",
            "sName": "group",
            "title": "Group",
            "sWidth": "100px",
            "sDefaultContent": ""
        },
        {
            "data": "shortDescription",
            "sName": "description",
            "title": "Description",
            "sWidth": "300px",
            "sDefaultContent": ""
        },
        {
            "data": "tcDateCrea",
            "sName": "tcDateCrea",
            "title": "Created",
            "sWidth": "150px",
            "sDefaultContent": ""
        }
    ];

    for (var index = 0; index < countries.length; index++) {
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