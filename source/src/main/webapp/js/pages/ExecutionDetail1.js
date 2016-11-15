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
    getParameter("cerberus_executiondetail_use").then(function(data){
        if(data.value == "N"){
            window.location = "ExecutionDetail.jsp?id_tc="+GetURLParameter("executionId");
        }
    });
    $(document).ready(function () {
        var stepList = [];
        var executionId = GetURLParameter("executionId");
        initPage(executionId);

        var loc = window.location, new_uri;
        if (loc.protocol === "https:") {
            new_uri = "wss:";
        } else {
            new_uri = "ws:";
        }
        new_uri += "//" + loc.host;
        new_uri += "/" + "Cerberus/execution/" + executionId;

        var socket = new WebSocket(new_uri);

        socket.onopen = function(e){
        } /*on "écoute" pour savoir si la connexion vers le serveur websocket s'est bien faite */
        socket.onmessage = function(e){
            var data = JSON.parse(e.data);
            updatePage(data, stepList);
        } /*on récupère les messages provenant du serveur websocket */
        socket.onclose = function(e){
        } /*on est informé lors de la fermeture de la connexion vers le serveur*/
        socket.onerror = function(e){
        } /*on traite les cas d'erreur*/
    });
});

function initPage(id) {

    var doc = new Doc();
    $("#testCaseConfig #testCaseDetails").hide();
    $(".panel-heading").click(function(e){
        $("#testCaseConfig #testCaseDetails").toggle();
        $('#list-wrapper').data('bs.affix').options.offset.top = $("nav.navbar.navbar-inverse.navbar-static-top").outerHeight(true) + $("div.alert.alert-warning").outerHeight(true) + $("div.progres").outerHeight(true) + $("#testCaseConfig").outerHeight(true);
        return false;
    });

    $('#list-wrapper').affix({offset: {top: $("nav.navbar.navbar-inverse.navbar-static-top").outerHeight(true) + $("div.alert.alert-warning").outerHeight(true) + $("div.progres").outerHeight(true) + $("#testCaseConfig").outerHeight(true)} });

    $("#editTcInfo").prop("disabled",true);
    $("#runTestCase").prop("disabled",true);
    $("#lastExecution").prop("disabled",true);

    $("#runOld").click(function () {
        window.location = "ExecutionDetail.jsp?id_tc="+id;
    });

    displayHeaderLabel(doc);
    displayFooter(doc);
}

function updatePage(data, stepList){

    sortData(data.testCaseStepExecutionList);

    $("#editTcInfo").prop("disabled",false);
    $("#runTestCase").prop("disabled",false);
    $("#lastExecution").prop("disabled",false);

    $("#editTcInfo").click(function () {
        window.location = "TestCaseScript.jsp?test=" + data.test + "&testcase=" + data.testcase;
    });
    $("#runTestCase").click(function () {
        window.location = "RunTests1.jsp?test=" + data.test + "&testcase=" + data.testcase + "&country=" + data.country + "&environment=" + data.environment + "&browser=" + data.browser + "&tag=" + data.tag;
    });
    $("#lastExecution").click(function () {
        window.location = "ExecutionDetailList.jsp?test=" + data.test + "&testcase=" + data.testcase;
    });

    var configPanel = $("#testCaseConfig");

    configPanel.find("#ExecutionByTag").click(function(){
        window.open("ReportingExecutionByTag.jsp?Tag=" + data.tag,'_blank');
        return false;
    });

    configPanel.find("#idlabel").text(data.id);
    configPanel.find("#test").text(data.test);
    configPanel.find("#testcase").text(data.testcase);
    configPanel.find("#controlstatus").text(data.controlStatus);
    configPanel.find("input#application").val(data.application);
    configPanel.find("input#browser").val(data.browser);
    configPanel.find("input#browserfull").val(data.browserFullVersion);
    configPanel.find("input#build").val(data.build);
    configPanel.find("input#country").val(data.country);
    configPanel.find("input#environment").val(data.environment);
    configPanel.find("input#status").val(data.status);
    configPanel.find("input#controlstatus2").val(data.controlStatus);
    configPanel.find("input#controlmessage").val(data.controlMessage);
    configPanel.find("input#end").val(data.end);
    configPanel.find("input#finished").val(data.finished);
    configPanel.find("input#id").val(data.id);
    configPanel.find("input#ip").val(data.ip);
    configPanel.find("input#port").val(data.port);
    configPanel.find("input#platform").val(data.platform);
    configPanel.find("input#revision").val(data.revision);
    configPanel.find("input#cerberusversion").val(data.crbVersion);
    configPanel.find("input#executor").val(data.executor);
    configPanel.find("input#screenSize").val(data.screenSize);
    configPanel.find("input#start").val(data.start);
    configPanel.find("input#tag").val(data.tag);
    configPanel.find("input#url").val(data.url);
    configPanel.find("input#verbose").val(data.verbose);
    configPanel.find("input#version").val(data.version);

    createStepList(data.testCaseStepExecutionList,stepList);
    updateLoadBar(data);
}

function updateLoadBar(data){
    var total = 0;
    var ended = 0;
    for (var i = 0; i < data.testCaseStepExecutionList.length; i++) {
        var step = data.testCaseStepExecutionList[i];
        if (step.returnCode != "PE") {
            ended += 1;
        }
        total += 1;
        for (var j = 0; j < step.testCaseStepActionExecutionList.length; j++) {
            var action = step.testCaseStepActionExecutionList[j];
            if (action.returnCode != "PE") {
                ended += 1;
            }
            total += 1;
            for (var k = 0; k < action.testCaseStepActionControlExecutionList.length; k++) {
                var control = action.testCaseStepActionControlExecutionList[k];
                if (control.returnCode != "PE") {
                    ended += 1;
                }
                total += 1;
            }
        }
    }

    var progress = ended / total * 100;
    $("#progress-bar").css("width",progress + "%").attr("aria-valuenow",progress);
    if(progress == 100 && data.controlStatus != "PE"){
        $("#progress-bar").addClass("progress-bar-success");
    }
}

/** DATA AGREGATION **/

function sortStep(step) {

    for (var j = 0; j < step.testCaseStepActionExecutionList.length; j++) {
        var action = step.testCaseStepActionExecutionList[j];

        action.testCaseStepActionControlExecutionList.sort(function (a, b) {
            return a.sort - b.sort;
        });
    }

    step.testCaseStepActionExecutionList.sort(function (a, b) {
        return a.sort - b.sort;
    });
}

function sortData(agreg) {
    for (var i = 0; i < agreg.length; i++) {
        var step = agreg[i];

        sortStep(step);
    }

    agreg.sort(function (a, b) {
        return a.sort - b.sort;
    });
}

function createStepList(data, stepList) {
    $("#actionContainer").empty();
    $("#stepList").empty();

    for (var i = 0; i < data.length; i++) {
        var step = data[i];
        var stepObj = new Step(step, stepList);

        stepObj.draw();
        stepList.push(stepObj);
    }
    if (stepList.length > 0) {
        $("#stepList a:last-child").trigger("click");
    }
}

/** JAVASCRIPT OBJECT **/

function Step(json, stepList) {
    this.stepActionContainer = $("<div></div>").addClass("list-group").css("display", "none");

    this.description = json.description;
    this.end = json.end;
    this.fullEnd = json.fullEnd;
    this.fullStart = json.fullStart;
    this.id = json.id;
    this.returnCode = json.returnCode;
    this.sort = json.sort;
    this.start = json.start;
    this.step = json.step;
    this.test = json.test;
    this.testcase = json.testCase;
    this.timeElapsed = json.timeElapsed;
    this.useStep = json.useStep;
    this.useStepTest = json.useStepTest;
    this.useStepTestCase = json.useStepTestCase;
    this.useStepTestCaseStep = json.useStepTestCaseStep;
    this.useStepStep = json.useStepStep;
    this.inLibrary = json.inLibrary;
    this.actionList = [];
    this.setActionList(json.testCaseStepActionExecutionList);

    this.stepList = stepList;
    this.toDelete = false;

    this.html = $("<a href='#'></a>").addClass("list-group-item row").css("margin-left", "0px").css("margin-right", "0px");
    this.textArea = $("<div></div>").addClass("col-lg-10")
            .text("[" + this.sort + "]  " + this.description + "  (" + this.timeElapsed + ")");

}

Step.prototype.draw = function () {
    var htmlElement = this.html;


    htmlElement.data("item", this);
    htmlElement.click(this.show);
    var object = htmlElement.data("item");
    if (object.returnCode === "OK") {
        htmlElement.append($("<span>").addClass("glyphicon glyphicon-ok pull-left"));
        object.html.addClass("list-group-item-success");
    } else if (object.returnCode === "PE") {
        htmlElement.append($("<span>").addClass("glyphicon glyphicon-refresh spin pull-left"));
        object.html.addClass("list-group-item-info");
    } else if (object.returnCode === "FA") {
        htmlElement.append($("<span>").addClass("glyphicon glyphicon-alert pull-left"));
        object.html.addClass("list-group-item-warning");
    } else {
        htmlElement.prepend($("<span>").addClass("glyphicon glyphicon-remove pull-left"));
        object.html.addClass("list-group-item-danger");
    }
    htmlElement.append(this.textArea);
    $("#stepList").append(htmlElement);
    $("#actionContainer").append(this.stepActionContainer);
};

Step.prototype.show = function () {
    var object = $(this).data("item");
    var stepDesc = $("<div>").addClass("col-sm-11");

    for (var i = 0; i < object.stepList.length; i++) {
        var step = object.stepList[i];

        step.stepActionContainer.hide();
        step.html.removeClass("active");
    }
    $("#stepInfo").empty();
    $("#stepContent").removeClass();
    $(this).addClass("active");



    if (object.returnCode === "OK") {
        $("#stepInfo").prepend($("<div>").addClass("col-sm-1").append($("<h2>").addClass("glyphicon glyphicon-ok pull-left text-success").attr("style", "font-size:3em")));
        $("#stepContent").addClass("col-lg-9");
    } else if (object.returnCode === "PE") {
        $("#stepInfo").prepend($("<div>").addClass("col-sm-1").append($("<h2>").addClass("glyphicon glyphicon-refresh spin pull-left text-info").attr("style", "font-size:3em")));
        $("#stepContent").addClass("col-lg-9");
    } else if (object.returnCode === "FA") {
        $("#stepInfo").prepend($("<div>").addClass("col-sm-1").append($("<h2>").addClass("glyphicon glyphicon-alert pull-left text-warning").attr("style", "font-size:3em")));
        $("#stepContent").addClass("col-lg-9");
    } else {
        $("#stepInfo").prepend($("<div>").addClass("col-sm-1").append($("<h2>").addClass("glyphicon glyphicon-remove pull-left text-danger").attr("style", "font-size:3em")));
        $("#stepContent").addClass("col-lg-9");
    }

    stepDesc.append($("<h2 id='stepDescription' style='float:left;'>").text(object.description));
    if (object.useStep === "Y") {
        stepDesc.append($("<div id='libInfo' style='float:right; margin-top: 20px;'>").text("(Imported from " + object.useStepTest + " - " + object.useStepTestCase + " - " + object.useStepStep + " )"));
    } else {
        stepDesc.append($("<div id='libInfo' style='float:right; margin-top: 20px;'>").text(""));
    }
    $("#stepInfo").append(stepDesc);
    object.stepActionContainer.show();
    $("#stepInfo").show();
    return false;
};

Step.prototype.setActionList = function (actionList) {
    for (var i = 0; i < actionList.length; i++) {
        this.setAction(actionList[i]);
    }
};

Step.prototype.setAction = function (action) {
    if (action instanceof Action) {
        this.actionList.push(action);
    } else {
        var actionObj = new Action(action, this);
        this.actionList.push(actionObj);
    }
};

Step.prototype.setDescription = function (description) {
    this.description = description;
    this.textArea.text(description);
    $("#stepDescription").text(description);
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
    this.html = $("<a href='#'></a>").addClass("action-group");
    this.parentStep = parentStep;

    if (json !== null) {
        this.action = json.action;
        this.description = json.description;
        this.end = json.end;
        this.endlong = json.endlong;
        this.forceExeStatus = json.forceExeStatus;
        this.id = json.id;
        this.returnCode = json.returnCode;
        this.returnMessage = json.returnMessage;
        this.sequence = json.sequence;
        this.sort = json.sort;
        this.start = json.start;
        this.startlong = json.startlong;
        this.step = json.step;
        this.test = json.test;
        this.testcase = json.testcase;
        this.value1 = json.value1;
        this.value2 = json.value2;
        this.screenshotFileName = json.screenshotFileName;
        this.controlList = [];
    } else {
        this.action = "Unknown";
        this.description = "";
        this.end = 0;
        this.endlong = 0;
        this.forceExeStatus = "";
        this.id = 0;
        this.returnCode = "";
        this.returnMessage = "";
        this.sequence = 0;
        this.sort = 0;
        this.start = 0;
        this.startlong = 0;
        this.step = parentStep.step;
        this.test = "";
        this.testcase = "";
        this.value1 = "";
        this.value2 = "";
        this.screenshotFileName = "";
        this.controlList = [];
    }

    this.draw();

    if(json !== null){
        this.setControlList(json.testCaseStepActionControlExecutionList);
    }

    this.toDelete = false;
}

Action.prototype.draw = function () {
    var htmlElement = this.html;
    var action = this;
    var row = $("<div></div>").addClass("col-sm-10");
    var type = $("<div></div>").addClass("type");

    row.append(this.generateHeader());
    row.data("item", this);

    var button = $("<div></div>").addClass("col-sm-1").append($("<span class='glyphicon glyphicon-chevron-down'></span>").attr("style", "font-size:1.5em"));

    htmlElement.prepend(button);
    htmlElement.prepend(row);

    var content = this.generateContent();

    if (action.returnCode === "OK") {
        htmlElement.prepend($("<div>").addClass("col-sm-1").append($("<span>").addClass("glyphicon glyphicon-ok").attr("style", "font-size:1.5em")));
        htmlElement.addClass("row list-group-item list-group-item-success");
        content.hide();
    } else if (action.returnCode === "PE") {
        htmlElement.prepend($("<div>").addClass("col-sm-1").append($("<span>").addClass("glyphicon glyphicon-refresh spin").attr("style", "font-size:1.5em")));
        htmlElement.addClass("row list-group-item list-group-item-info");
        content.hide();
    } else if (action.returnCode === "FA") {
        htmlElement.prepend($("<div>").addClass("col-sm-1").append($("<span>").addClass("glyphicon glyphicon-alert").attr("style", "font-size:1.5em")));
        htmlElement.addClass("row list-group-item list-group-item-warning");
        content.hide();
    } else {
        htmlElement.prepend($("<div>").addClass("col-sm-1").append($("<span>").addClass("glyphicon glyphicon-remove").attr("style", "font-size:1.5em")));
        htmlElement.addClass("row list-group-item list-group-item-danger");
        content.show();
    }

    this.parentStep.stepActionContainer.append(htmlElement);
    this.parentStep.stepActionContainer.append(content);
    htmlElement.click(function(){
       content.toggle();
        return false;
    });
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

Action.prototype.generateHeader = function () {
    var scope = this;
    var content = $("<div></div>").addClass("content");
    var firstRow = $("<div></div>").addClass("row list-group-item-heading");
    var returnMessageField = $("<h4>").addClass("col-sm-10").attr("style", "font-size:1.5em;margin:0px;line-height:1.3;height:1.5em;overflow:hidden;white-space: nowrap;text-overflow: ellipsis;");
    var image = $("<div>").addClass("col-sm-2").append($("<img>").css("float","right").attr("src","ReadTestCaseExecutionImage?id=" + this.id + "&test=" + this.test + "&testcase=" + this.testcase + "&type=action&step=" + this.step + "&sequence=" + this.sequence ).css("height","30px"));

    returnMessageField.text(this.returnMessage);

    image.click(function(){
        showPicture("ReadTestCaseExecutionImage?id=" + scope.id + "&test=" + scope.test + "&testcase=" + scope.testcase + "&type=action&step=" + scope.step + "&sequence=" + scope.sequence + "&h=400&w=800", scope.step, scope.sequence);
        return false;
    });

    firstRow.append(returnMessageField);
    firstRow.append(image);

    content.append(firstRow);

    return content;
};

Action.prototype.generateContent = function () {
    var obj = this;

    var secondRow = $("<div></div>").addClass("row");
    var thirdRow = $("<div></div>").addClass("row");
    var fourthRow = $("<div></div>").addClass("row");
    var fifthRow = $("<div></div>").addClass("row");
    var container = $("<div id='content-container'></div>").addClass("action-group row list-group-item");

    var actionList = $("<input type='text' class='form-control' id='action'>").prop("readonly",true);
    var descField = $("<input type='text' class='form-control' id='description'>").prop("readonly",true);
    var value1Field = $("<input type='text' class='form-control' id='value1'>").prop("readonly",true);
    var value2Field = $("<input type='text' class='form-control' id='value2'>").prop("readonly",true);
    var timeField = $("<input type='text' class='form-control' id='time'>").prop("readonly",true);
    var returnCodeField = $("<input type='text' class='form-control' id='returncode'>").prop("readonly",true);
    var returnMessageField = $("<textarea class='form-control' id='returnmessage'>").prop("readonly",true);
    var sortField = $("<input type='text' class='form-control' id='sort'>").prop("readonly",true);

    var actionGroup = $("<div class='form-group'></div>").append($("<label for='action'>Action</label>")).append(actionList);
    var descGroup = $("<div class='form-group'></div>").append($("<label for='description'>Description</label>")).append(descField);
    var objectGroup = $("<div class='form-group'></div>").append($("<label for='value1'>Value 1</label>")).append(value1Field);
    var timeGroup = $("<div class='form-group'></div>").append($("<label for='time'>Time</label>")).append(timeField);
    var propertyGroup = $("<div class='form-group'></div>").append($("<label for='value2'>Value 2</label>")).append(value2Field);
    var returncodeGroup = $("<div class='form-group'></div>").append($("<label for='returncode'>Return Code</label>")).append(returnCodeField);
    var returnmessageGroup = $("<div class='form-group'></div>").append($("<label for='returnmessage'>Return Message</label>")).append(returnMessageField);
    var sortGroup = $("<div class='form-group'></div>").append($("<label for='sort'>Sort</label>")).append(sortField);



    descField.val(this.sequence + " - " + this.description);
    actionList.val(this.action);
    value1Field.val(this.value1);
    value2Field.val(this.value2);
    timeField.val((this.endlong - this.startlong) + " ms");
    returnCodeField.val(this.returnCode);
    returnMessageField.val(this.returnMessage);
    sortField.val(this.sort);

    secondRow.append($("<div></div>").addClass("col-sm-4").append(returncodeGroup));
    secondRow.append($("<div></div>").addClass("col-sm-4").append(descGroup));
    secondRow.append($("<div></div>").addClass("col-sm-4").append(timeGroup));
    fifthRow.append($("<div></div>").addClass("col-sm-4").append(sortGroup));
    thirdRow.append($("<div></div>").addClass("col-sm-4").append(actionGroup));
    thirdRow.append($("<div></div>").addClass("col-sm-4").append(objectGroup));
    thirdRow.append($("<div></div>").addClass("col-sm-4").append(propertyGroup));
    fourthRow.append($("<div></div>").addClass("col-sm-12").append(returnmessageGroup));

    container.append(secondRow);
    container.append(fifthRow);
    container.append(thirdRow);
    container.append(fourthRow);

    return container;
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
    json.value1 = this.value1;
    json.value2 = this.value2;
    json.screenshotFileName = "";

    return json;
};

function Control(json, parentAction) {
    if (json !== null) {
        this.control = json.control;
        this.controlType = json.controlType;
        this.value1 = json.controlProperty;
        this.value2 = json.controlValue;
        this.description = json.description;
        this.end = json.end;
        this.endlong = json.endlong;
        this.fatal = json.fatal;
        this.id = json.id;
        this.returnCode = json.returnCode;
        this.returnMessage = json.returnMessage;
        this.screenshotFileName = "";
        this.sequence = json.sequence;
        this.sort = json.sort;
        this.start = json.start;
        this.startlong = json.startlong;
        this.step = json.step;
        this.test = json.test;
        this.testcase = json.testcase;
    } else {
        this.control = "";
        this.controlType = "Unknown";
        this.value1 = "";
        this.value2 = "";
        this.description = "";
        this.end = 0;
        this.endlong = 0;
        this.fatal = "Y";
        this.id = 0;
        this.returnCode = "";
        this.returnMessage = "";
        this.screenshotFileName = "";
        this.sequence = parentAction.sequence;
        this.sort = 0;
        this.start = 0;
        this.startlong = 0;
        this.step = parentAction.step;
        this.test = "";
        this.testcase = "";
    }

    this.parentStep = parentAction.parentStep;
    this.parentAction = parentAction;

    this.toDelete = false;

    this.html = $("<a href='#'></a>").addClass("action-group").css("margin-left","25px");
}

Control.prototype.draw = function () {
    var htmlElement = this.html;
    var row = $("<div></div>").addClass("col-sm-10");
    var type = $("<div></div>").addClass("type");

    row.append(this.generateHeader());
    row.data("item", this);

    var button = $("<div></div>").addClass("col-sm-1").append($("<span class='glyphicon glyphicon-chevron-down'></span>").attr("style", "font-size:1.5em"));

    htmlElement.prepend(button);
    htmlElement.prepend(row);

    var content = this.generateContent();

    if (this.returnCode === "OK") {
        htmlElement.prepend($("<div>").addClass("col-sm-1").append($("<span>").addClass("glyphicon glyphicon-ok").attr("style", "font-size:1.5em")));
        htmlElement.addClass("row list-group-item list-group-item-success");
        content.hide();
    } else if (this.returnCode === "PE") {
        htmlElement.prepend($("<div>").addClass("col-sm-1").append($("<span>").addClass("glyphicon glyphicon-refresh spin").attr("style", "font-size:1.5em")));
        htmlElement.addClass("row list-group-item list-group-item-info");
        content.hide();
    } else if (this.returnCode === "FA") {
        htmlElement.prepend($("<div>").addClass("col-sm-1").append($("<span>").addClass("glyphicon glyphicon-alert").attr("style", "font-size:1.5em")));
        htmlElement.addClass("row list-group-item list-group-item-warning");
        content.hide();
    } else {
        htmlElement.prepend($("<div>").addClass("col-sm-1").append($("<span>").addClass("glyphicon glyphicon-remove").attr("style", "font-size:1.5em")));
        htmlElement.addClass("row list-group-item list-group-item-danger");
        content.show();
    }

    this.parentStep.stepActionContainer.append(htmlElement);
    this.parentStep.stepActionContainer.append(content);
    htmlElement.click(function(){
        content.toggle();
        return false;
    });

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

Control.prototype.generateHeader = function () {
    var scope = this;
    var content = $("<div></div>").addClass("content");
    var firstRow = $("<div></div>").addClass("row list-group-item-heading");
    var returnMessageField = $("<h4>").addClass("col-sm-10").attr("style", "font-size:1.5em;margin:0px;line-height:1.3;height:1.5em;overflow:hidden;white-space: nowrap;text-overflow: ellipsis;");
    var image = $("<div>").addClass("col-sm-2").append($("<img>").css("float","right").attr("src","ReadTestCaseExecutionImage?id=" + this.id + "&test=" + this.test + "&testcase=" + this.testcase + "&type=control&step=" + this.step + "&sequence=" + this.sequence + "&sequenceControl=" + this.control).css("height","30px"));

    image.click(function(){
        showPicture("ReadTestCaseExecutionImage?id=" + scope.id + "&test=" + scope.test + "&testcase=" + scope.testcase + "&type=action&step=" + scope.step + "&sequence=" + scope.sequence + "&sequenceControl=" + scope.control + "&h=400&w=800", scope.step, scope.sequence, scope.control);
        return false;
    });

    returnMessageField.text(this.returnMessage);

    firstRow.append(returnMessageField);
    firstRow.append(image);

    content.append(firstRow);

    return content;
};

Control.prototype.generateContent = function () {
    var obj = this;

    var secondRow = $("<div></div>").addClass("row");
    var thirdRow = $("<div></div>").addClass("row");
    var fourthRow = $("<div></div>").addClass("row");
    var fifthRow = $("<div></div>").addClass("row");
    var container = $("<div id='content-container'></div>").addClass("action-group row list-group-item").css("margin-left","25px");

    var descField = $("<input type='text' class='form-control' id='description'>").prop("readonly",true);
    var returnCodeField = $("<input type='text' class='form-control' id='returncode'>").prop("readonly",true);
    var controlTypeField = $("<input type='text' class='form-control' id='controltype'>").prop("readonly",true);
    var value1Field = $("<input type='text' class='form-control' id='value1'>").prop("readonly",true);
    var value2Field = $("<input type='text' class='form-control' id='value2'>").prop("readonly",true);
    var timeField = $("<input type='text' class='form-control' id='time'>").prop("readonly",true);
    var returnMessageField = $("<textarea class='form-control' id='returnmessage'>").prop("readonly",true);
    var fatalField = $("<input type='text' class='form-control' id='fatal'>").prop("readonly",true);
    var sortField = $("<input type='text' class='form-control' id='sort'>").prop("readonly",true);

    var descGroup = $("<div class='form-group'></div>").append($("<label for='description'>Description</label>")).append(descField);
    var returncodeGroup = $("<div class='form-group'></div>").append($("<label for='returncode'>Return Code</label>")).append(returnCodeField);
    var returnmessageGroup = $("<div class='form-group'></div>").append($("<label for='returnmessage'>Return Message</label>")).append(returnMessageField);
    var controlTypeGroup = $("<div class='form-group'></div>").append($("<label for='controltype'>Control Type</label>")).append(controlTypeField);
    var controlValueGroup = $("<div class='form-group'></div>").append($("<label for='controlvalue'>Value 1</label>")).append(value1Field);
    var timeGroup = $("<div class='form-group'></div>").append($("<label for='time'>Time</label>")).append(timeField);
    var controlPropertyGroup = $("<div class='form-group'></div>").append($("<label for='controlproperty'>Value 2</label>")).append(value2Field);
    var fatalGroup = $("<div class='form-group'></div>").append($("<label for='fatal'>Fatal</label>")).append(fatalField);
    var sortGroup = $("<div class='form-group'></div>").append($("<label for='sort'>Sort</label>")).append(sortField);



    descField.val(this.sequence + " - " + this.description);
    returnCodeField.val(this.returnCode);
    returnMessageField.val(this.returnMessage);
    controlTypeField.val(this.controlType);
    timeField.val((this.endlong - this.startlong) + " ms");
    value1Field.val(this.value1);
    value2Field.val(this.value2);
    fatalField.val(this.fatal);
    sortField.val(this.sort);

    secondRow.append($("<div></div>").addClass("col-sm-4").append(returncodeGroup));
    secondRow.append($("<div></div>").addClass("col-sm-4").append(descGroup));
    secondRow.append($("<div></div>").addClass("col-sm-4").append(timeGroup));
    fifthRow.append($("<div></div>").addClass("col-sm-4").append(sortGroup));
    fifthRow.append($("<div></div>").addClass("col-sm-4").append(fatalGroup));
    thirdRow.append($("<div></div>").addClass("col-sm-4").append(controlTypeGroup));
    thirdRow.append($("<div></div>").addClass("col-sm-4").append(controlValueGroup));
    thirdRow.append($("<div></div>").addClass("col-sm-4").append(controlPropertyGroup));
    fourthRow.append($("<div></div>").addClass("col-sm-12").append(returnmessageGroup));

    container.append(secondRow);
    container.append(fifthRow);
    container.append(thirdRow);
    container.append(fourthRow);

    return container;
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
    json.controlProperty = this.value1;
    json.controlValue = this.value2;
    json.fatal = this.fatal;
    json.screenshotFileName = this.screenshotFileName;

    return json;
};

