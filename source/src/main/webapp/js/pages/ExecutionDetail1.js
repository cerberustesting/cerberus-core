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
        var stepList = [];
        initPage();
        var executionId = GetURLParameter("executionId");
        var socket = new WebSocket("ws://localhost:8080/Cerberus/execution/"+executionId);

        socket.onopen = function(e){
            console.log("connexion ouverte");
        } /*on "écoute" pour savoir si la connexion vers le serveur websocket s'est bien faite */
        socket.onmessage = function(e){
            console.log("message reçu");
            var data = JSON.parse(e.data);
            console.log(data);
            updatePage(data, stepList);
        } /*on récupère les messages provenant du serveur websocket */
        socket.onclose = function(e){
            console.log("connexion fermée");
        } /*on est informé lors de la fermeture de la connexion vers le serveur*/
        socket.onerror = function(e){
            console.log("erreur");
        } /*on traite les cas d'erreur*/
    });
});

function initPage() {
    $("#testCaseConfig #testCaseDetails").hide();
    $("#testCaseConfig #moredetails").click(function(e){
        $("#testCaseConfig #testCaseDetails").toggle();
    });
}

function updatePage(data, stepList){
    var configPanel = $("#testCaseConfig");
    configPanel.find("#test").text(data.test);
    configPanel.find("#testcase").text(data.testcase);
    configPanel.find("#controlstatus").text(data.controlStatus);
    configPanel.find("input#application").val(data.application);
    configPanel.find("input#browser").val(data.browser);
    configPanel.find("input#browserfull").val(data.browserFullVersion);
    configPanel.find("input#country").val(data.country);
    configPanel.find("input#environment").val(data.environment);
    configPanel.find("input#status").val(data.status);
    configPanel.find("input#controlstatus2").val(data.controlStatus);
    configPanel.find("input#controlmessage").val(data.controlMessage);
    configPanel.find("input#ip").val(data.ip);
    configPanel.find("input#port").val(data.port);
    configPanel.find("input#platform").val(data.platform);
    configPanel.find("input#cerberusversion").val(data.crbVersion);
    configPanel.find("input#executor").val(data.executor);
    configPanel.find("input#url").val(data.url);

    createStepList(data.testCaseStepExecutionList,stepList)
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

function createStepList(data, stepList) {
    $("#actionContainer").empty();

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

/** JAVASCRIPT OBJECT **/

function Step(json, stepList) {
    this.stepActionContainer = $("<div></div>").addClass("list-group").css("display", "none");

    this.test = json.test;
    this.testcase = json.testCase;
    this.step = json.step;
    this.description = json.description;
    this.timeElapsed = json.timeElapsed;
    this.useStep = json.useStep;
    this.useStepTest = json.useStepTest;
    this.useStepTestCase = json.useStepTestCase;
    this.useStepStep = json.useStepStep;
    this.inLibrary = json.inLibrary;
    this.returnCode = json.returnCode;
    this.actionList = [];
    this.setActionList(json.testCaseStepActionExecutionList);

    this.stepList = stepList;
    this.toDelete = false;

    this.html = $("<li></li>").addClass("list-group-item row").css("margin-left", "0px");
    this.textArea = $("<div></div>").addClass("col-lg-10")
            .text("[" + this.step + "]  " + this.description + "  (" + this.timeElapsed + ")");

}

Step.prototype.draw = function () {
    var htmlElement = this.html;


    htmlElement.data("item", this);
    htmlElement.click(this.show);
    var object = htmlElement.data("item");
    if (object.returnCode === "OK") {
        htmlElement.append($("<span>").addClass("glyphicon glyphicon-ok pull-left"));
        object.html.addClass("list-group-item-success");
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

    for (var i = 0; i < object.stepList.length; i++) {
        var step = object.stepList[i];

        step.stepActionContainer.hide();
        step.html.removeClass("active");
    }
    $("#stepInfo > span").remove();
    $("#stepContent").removeClass();
    $(this).addClass("active");



    if (object.returnCode === "OK") {
        $("#stepInfo").prepend($("<span>").addClass("glyphicon glyphicon-ok pull-left").attr("style", "font-size:3em"));
        $("#stepContent").addClass("col-lg-9 list-group-item-success");
    } else {
        $("#stepInfo").prepend($("<span>").addClass("glyphicon glyphicon-remove pull-left").attr("style", "font-size:3em"));
        $("#stepContent").addClass("col-lg-9 list-group-item-danger");
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
        this.returnCode = json.returnCode;
        this.returnMessage = json.returnMessage;
        this.object = json.object;
        this.property = json.property;
        this.screenshotFileName = json.screenshotFileName;
        this.controlList = [];
        this.setControlList(json.testCaseStepActionControlExecutionList);
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
    var row = $("<div></div>").addClass("col-sm-11");
    var type = $("<div></div>").addClass("type");


    row.append(this.generateContent());
    row.data("item", this);
    htmlElement.prepend(row);


    if (action.returnCode === "OK") {
        htmlElement.prepend($("<div>").addClass("col-sm-1").append($("<span>").addClass("glyphicon glyphicon-ok").attr("style", "font-size:1.5em")));
        htmlElement.addClass("row list-group-item list-group-item-success");
    } else {
        htmlElement.prepend($("<div>").addClass("col-sm-1").append($("<span>").addClass("glyphicon glyphicon-remove").attr("style", "font-size:1.5em")));
        htmlElement.addClass("row list-group-item list-group-item-danger");
    }

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
    var content = $("<div></div>").addClass("content");
    var firstRow = $("<div></div>").addClass("list-group-item-heading");
    var secondRow = $("<div></div>").addClass("row form-inline");
    var thirdRow = $("<div></div>").addClass("row form-inline");

    var actionList = $("<span>");
    var descField = $("<span>");
    var objectField = $("<span>");
    var propertyField = $("<span>");
    var returnMessageField = $("<h4>").attr("style", "height:30px; font-size:15px;");
    var returnCodeField = $("<span>");

    descField.text(this.sequence + " - " + this.description);
    actionList.text(this.action);
    objectField.text(this.object);
    propertyField.text(this.property);
    
    returnMessageField.text(this.returnMessage);
    
    firstRow.append(returnMessageField);
    secondRow.append($("<span></span>").addClass("col-lg-4").append(actionList));
    secondRow.append($("<span></span>").addClass("col-lg-4").append(objectField));
    secondRow.append($("<span></span>").addClass("col-lg-4").append(propertyField));
    thirdRow.append($("<span></span>").addClass("col-lg-4").append(returnCodeField));
    thirdRow.append($("<span></span>").addClass("col-lg-4").append(descField));

    content.append(firstRow);
    content.append(secondRow);
    content.append(thirdRow);

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
    var content = this.generateContent();

    htmlElement.append(content);
    htmlElement.data("item", this);

    htmlElement.prepend($("<div>").addClass("col-lg-2").append($("<span>").addClass("glyphicon glyphicon-ok").attr("style", "font-size:1.5em")));
    htmlElement.addClass("list-group-item-success");

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
    var content = $("<div></div>").addClass("content col-lg-10").attr("style", "height:80px");
    var firstRow = $("<div></div>").addClass("row");
    var secondRow = $("<div></div>").addClass("row form-inline");
    var thirdRow = $("<div></div>").addClass("row form-inline");
    
    var controlList = $("<text>");
    var descField = $("<text>").addClass("description");
    var objectField = $("<text>");
    var propertyField = $("<text>");
    var fatalField = $("<text>");

    descField.text(this.description);
    controlList.text(this.type);
    objectField.text(this.object);
    propertyField.text(this.property);
    fatalField.text(this.fatal);

    firstRow.append(descField);
    secondRow.append($("<span></span>").addClass("col-lg-3").append(controlList));
    secondRow.append($("<span></span>").addClass("col-lg-4").append(objectField));
    secondRow.append($("<span></span>").addClass("col-lg-4").append(propertyField));
    secondRow.append($("<span></span>").addClass("col-lg-1").append(fatalField));

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

