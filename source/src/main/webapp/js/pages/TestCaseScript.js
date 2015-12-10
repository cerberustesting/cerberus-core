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

        var test = GetURLParameter("test");
        var testcase = GetURLParameter("testcase");
        displayHeaderLabel(doc);
        displayGlobalLabel(doc);
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
        displayApplicationList("application", getUser().defaultSystem);
        displayProjectList("project");
        tinymce.init({
            selector: "textarea"
        });

        $('#editEntryModal').on('hidden.bs.modal', {extra: "#editEntryModal"}, modalFormCleaner);
        $("#editEntryButton").click(saveUpdateEntryHandler);
        $("#editTcInfo").click({test: test, testcase: testcase}, editEntry);
        $("#addStep").click(addStep);

        var json;
        $.ajax({
            url: "ReadTestCase",
            data: {test: test, testCase: testcase, withStep: true},
            dataType: "json",
            success: function (data) {
                loadTestCaseInfo(data.info);
                data = agregateData(data);
                createStepList(data);
            },
            error: showUnexpectedError
        });
    });
});

function loadTestCaseInfo(info) {
    $(".testTestCase #test").text(info.test);
    $(".testTestCase #testCase").text(info.testCase);
    $(".testTestCase #description").text(info.shortDescription);
}

function cancelEdit() {
    $("#editStep").hide();
    $("#editStepDescription").val("");
    $("#stepDescription").show();
    $("#editBtnArea").show();
}

function editStep(event) {
    var step = event.data.step;

    $("#stepDescription").hide();
    $("#editBtnArea").hide();
    $("#editStepDescription").prop("placeholder", "Description").val(step.description);
    $("#editStep").show();

    $("#cancelEdit").click(cancelEdit);
}

function deleteStep(event) {
    var stepNumber = event.data.stepNumber;
    
    $("#stepList li:nth-child("+ stepNumber +")").remove();
}

function addStep(event) {
    var drag = $("<span></span>").addClass("glyphicon glyphicon-move drag-step").css("margin-right", "5px").prop("draggable", true);
    var htmlElement = $("<li></li>").addClass("list-group-item");
    var stepNumber = $("#stepList li").length + 1;

    htmlElement.prepend(drag);
    $("#stepDescription").hide();
    $("#editBtnArea").hide();
    $("#editStepDescription").prop("placeholder", "Description").val("");
    $("#editStep").show();

    $("#cancelEdit").click({stepNumber: stepNumber}, deleteStep);

    $("#stepList").append(htmlElement);
}

function getControlListHtml(controlList) {
    var html = [];

    for (var i = 0; i < controlList.length; i++) {
        var control = controlList[i];

        html.push(generateRow(control, "control"));
    }
    return html;
}

function createStepList(data) {
    for (var i = 0; i < data.length; i++) {
        var step = data[i];
        var drag = $("<span></span>").addClass("glyphicon glyphicon-move drag-step").css("margin-right", "5px").prop("draggable", true);
        var htmlElement = $("<li></li>").addClass("list-group-item");

        htmlElement.text(step.description);
        htmlElement.prepend(drag);

        drag.on("dragstart", handleDragStart);
        drag.on("dragenter", handleDragEnter);
        drag.on("dragover", handleDragOver);
        drag.on("dragleave", handleDragLeave);
        drag.on("drop", handleDrop);
        drag.on("dragend", handleDragEnd);

        $("#stepList").append(htmlElement.data("item", step));
        loadStepInfo(step);
        htmlElement.click(function () {
            var step = $(this).data("item");

            $("#stepList li").each(function () {
                $(this).removeClass("active");
            });
            $(this).addClass("active");

            $(".step-container").each(function () {
                $(this).hide();
            });

            $("#editBtnArea").show();
            $("#editBtn").click({step: step}, editStep);
            $("#stepDescription").text(step.description);
            $(".step-container[data-step='" + step.step + "']").show();
            cancelEdit();
        });
    }
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

/** HELPER FUNCTIONS TO GENERATE ACTION AND CONTROL ROWS **/

function generateRow(stepAction, rowClass) {
    var row = $("<div></div>").addClass("step-action row").addClass(rowClass).data("item", stepAction);
    var type = $("<div></div>").addClass("type");
    var drag = $("<div></div>").addClass("drag-step-action col-lg-1").prop("draggable", true).append(type)
            .append($("<span></span>").addClass("glyphicon glyphicon-move"));
    var content = generateContent(stepAction);

    drag.on("dragstart", handleDragStart);
    drag.on("dragenter", handleDragEnter);
    drag.on("dragover", handleDragOver);
    drag.on("dragleave", handleDragLeave);
    drag.on("drop", handleDrop);
    drag.on("dragend", handleDragEnd);

    row.append(drag);
    row.append(content);
    return row;
}

function generateContent(stepAction) {
    var content = $("<div></div>").addClass("content col-lg-11");
    var firstRow = $("<div></div>").addClass("row");
    var secondRow = $("<div></div>").addClass("row form-inline");

    firstRow.append($("<input>").addClass("description").addClass("form-control").val(stepAction.description).prop("placeholder", "Description"));
    if (stepAction.objType === "action") {
        secondRow.append($("<span></span>").text(stepAction.action).addClass("col-lg-4"));
        secondRow.append($("<span></span>").addClass("col-lg-4").append($("<input>").val(stepAction.object).addClass("form-control input-sm")));
        secondRow.append($("<span></span>").addClass("col-lg-4").append($("<input>").val(stepAction.property).addClass("form-control input-sm")));
    } else {
        secondRow.append($("<span></span>").text(stepAction.type).addClass("col-lg-3"));
        secondRow.append($("<span></span>").addClass("col-lg-4").append($("<label></label>").text("Value : ")).append($("<input>").val(stepAction.controlValue).addClass("form-control input-sm")));
        secondRow.append($("<span></span>").addClass("col-lg-4").append($("<input>").val(stepAction.controlProperty).addClass("form-control input-sm")));
        secondRow.append($("<span></span>").text(stepAction.fatal).addClass("col-lg-1"));
    }
    content.append(firstRow);
    content.append(secondRow);

    return content;
}

function loadStepInfo(step) {
    var actionList = step.actionList;
    var container = $("#actionContainer");
    var stepContainer = $("<div></div>").addClass("step-container").attr("data-step", step.step).css("display", "none");

    for (var i = 0; i < actionList.length; i++) {
        var actionGroup = $("<div></div>").addClass("action-group");
        var action = actionList[i];
        var actionRow = generateRow(action, "action");

        actionGroup.append(actionRow);
        actionGroup.append(getControlListHtml(action.controlList));
        stepContainer.append(actionGroup);
    }
    container.append(stepContainer);
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
    var img;

    if ($(obj).data("item").objType === "action") {
        img = obj.parentNode;
    } else {
        img = obj;
    }

    source = obj;
    obj.style.opacity = '0.4';
    dataTransfer.effectAllowed = 'move';
    dataTransfer.setData('text/html', img.innerHTML);
    dataTransfer.setDragImage(img, 50, 50);
}

function handleDragEnter(event) {
    var target = this.parentNode;
    var sourceData = $(source).data("item");
    var targetData = $(target).data("item");

    if (sourceData.objType === "action" && targetData.objType === "action") {
        if (isBefore(source.parentNode, target.parentNode)) {
            $(target).parent(".action-group").after(source.parentNode);
        } else {
            $(target).parent(".action-group").before(source.parentNode);
        }
    } else if (sourceData.objType === "control") {
        if (isBefore(source, target) || targetData.objType === "action") {
            $(target).after(source);
        } else {
            $(target).before(source);
        }
    } else {
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

function getIndexOf(type, data, stepNumber) {
    for (var i = 0; i < data.length; i++) {
        if (data[i][type] === stepNumber) {
            return i;
        }
    }
    return -1;
}

function sortData(agreg) {
    for (var i = 0; i < agreg.length; i++) {
        var step = agreg[i];
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
    agreg.sort(function (a, b) {
        return a.step - b.step;
    });
}

function agregateData(data) {
    var agreg = [];

    for (var i = 0; i < data.stepList.length; i++) {
        data.stepList[i].actionList = [];
        agreg.push(data.stepList[i]);
    }

    for (var i = 0; i < data.actionList.length; i++) {
        var object = data.actionList[i];
        var index = getIndexOf("step", agreg, object.step);
        var step = agreg[index];

        object.controlList = [];
        step.actionList.push(object);
    }

    for (var i = 0; i < data.controlList.length; i++) {
        var object = data.controlList[i];
        var indexStep = getIndexOf("step", agreg, object.step);
        var step = agreg[indexStep];
        var indexAction = getIndexOf("sequence", step.actionList, object.sequence);
        var action = step.actionList[indexAction];

        action.controlList.push(object);
    }
    sortData(agreg);
    return agreg;
}