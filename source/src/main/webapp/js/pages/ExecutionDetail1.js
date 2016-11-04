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
        var executionId = GetURLParameter("executionId");
        initPage(executionId);

        var socket = new WebSocket("ws://localhost:8080/Cerberus/execution/"+executionId);

        socket.onopen = function(e){
            console.log("connexion ouverte");
        } /*on "écoute" pour savoir si la connexion vers le serveur websocket s'est bien faite */
        socket.onmessage = function(e){
            console.log("message reçu");
            var data = JSON.parse(e.data);
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

function initPage(id) {
    var doc = new Doc();
    $("#testCaseConfig #testCaseDetails").hide();
    $("#testCaseConfig #moredetails").click(function(e){
        $("#testCaseConfig #testCaseDetails").toggle();
        $('#list-wrapper').data('bs.affix').options.offset.top = $("#handler").outerHeight(true) - 100 ;
    });

    $('#list-wrapper').affix({offset: {top: $("#handler").outerHeight(true) - 100} });

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
    $("#stepList").empty();

    for (var i = 0; i < data.length; i++) {
        var step = data[i];
        var stepObj = new Step(step, stepList);

        stepObj.draw();
        stepList.push(stepObj);
    }
    if (stepList.length > 0) {
        $("#stepList a:first-child").trigger("click");
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

    this.html = $("<a href='#'></a>").addClass("list-group-item row").css("margin-left", "0px").css("margin-right", "0px");
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
    } else {
        $("#stepInfo").prepend($("<div>").addClass("col-sm-1").append($("<h2>").addClass("glyphicon glyphicon-remove pull-left text-danger").attr("style", "font-size:3em")));
        $("#stepContent").addClass("col-lg-9");
    }

    stepDesc.append($("<h2 id='stepDescription'>").text(object.description));
    if (object.useStep === "Y") {
        stepDesc.append($("<div id='libInfo'>").text("(Imported from " + object.useStepTest + " - " + object.useStepTestCase + " - " + object.useStepStep + " )"));
    } else {
        stepDesc.append($("<div id='libInfo'>").text(""));
    }
    $("#stepInfo").append(stepDesc);
    object.stepActionContainer.show();
    $("#stepInfo").show();
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
    } else {
        htmlElement.prepend($("<div>").addClass("col-sm-1").append($("<span>").addClass("glyphicon glyphicon-remove").attr("style", "font-size:1.5em")));
        htmlElement.addClass("row list-group-item list-group-item-danger");
        content.show();
    }

    this.parentStep.stepActionContainer.append(htmlElement);
    this.parentStep.stepActionContainer.append(content);
    htmlElement.click(function(){
       content.toggle();
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
    var content = $("<div></div>").addClass("content");
    var firstRow = $("<div></div>").addClass("list-group-item-heading");
    var returnMessageField = $("<h4>").attr("style", "font-size:1.5em;margin:0px;line-height:1.3;height:1.5em;overflow:hidden;white-space: nowrap;text-overflow: ellipsis;");


    returnMessageField.text(this.returnMessage);

    firstRow.append(returnMessageField);

    content.append(firstRow);

    return content;
};

Action.prototype.generateContent = function () {
    var obj = this;

    var secondRow = $("<div></div>").addClass("row");
    var thirdRow = $("<div></div>").addClass("row");
    var fourthRow = $("<div></div>").addClass("row");
    var container = $("<div id='content-container'></div>").addClass("action-group row list-group-item");

    var actionList = $("<input type='text' class='form-control' id='action'>").prop("readonly",true);
    var descField = $("<input type='text' class='form-control' id='description'>").prop("readonly",true);
    var objectField = $("<input type='text' class='form-control' id='object'>").prop("readonly",true);
    var propertyField = $("<input type='text' class='form-control' id='property'>").prop("readonly",true);
    var returnCodeField = $("<input type='text' class='form-control' id='returncode'>").prop("readonly",true);
    var returnMessageField = $("<textarea class='form-control' id='returnmessage'>").prop("readonly",true);

    var actionGroup = $("<div class='form-group'></div>").append($("<label for='action'>Action</label>")).append(actionList);
    var descGroup = $("<div class='form-group'></div>").append($("<label for='description'>Description</label>")).append(descField);
    var objectGroup = $("<div class='form-group'></div>").append($("<label for='object'>Object</label>")).append(objectField);
    var propertyGroup = $("<div class='form-group'></div>").append($("<label for='property'>Property</label>")).append(propertyField);
    var returncodeGroup = $("<div class='form-group'></div>").append($("<label for='returncode'>Return Code</label>")).append(returnCodeField);
    var returnmessageGroup = $("<div class='form-group'></div>").append($("<label for='returnmessage'>Return Message</label>")).append(returnMessageField);



    descField.val(this.sequence + " - " + this.description);
    actionList.val(this.action);
    objectField.val(this.object);
    propertyField.val(this.property);
    returnCodeField.val(this.returnCode);
    returnMessageField.val(this.returnMessage);

    secondRow.append($("<div></div>").addClass("col-sm-4").append(actionGroup));
    secondRow.append($("<div></div>").addClass("col-sm-4").append(descGroup));
    secondRow.append($("<div></div>").addClass("col-sm-4").append(objectGroup));
    thirdRow.append($("<div></div>").addClass("col-sm-4").append(returncodeGroup));
    thirdRow.append($("<div></div>").addClass("col-sm-4").append(propertyGroup));
    thirdRow.append($("<div></div>").addClass("col-sm-4"));
    fourthRow.append($("<div></div>").addClass("col-sm-12").append(returnmessageGroup));

    container.append(secondRow);
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
        this.controlType = json.controlType;
        this.controlValue = json.controlValue;
        this.controlProperty = json.controlProperty;
        this.fatal = json.fatal;
        this.returnCode = json.returnCode;
        this.returnMessage = json.returnMessage;
        this.screenshotFileName = "";
    } else {
        this.test = "";
        this.testcase = "";
        this.step = parentAction.step;
        this.sequence = parentAction.sequence;
        this.control = "";
        this.description = "";
        this.controlType = "Unknown";
        this.controlValue = "";
        this.controlProperty = "";
        this.fatal = "Y";
        this.screenshotFileName = "";
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
    } else {
        htmlElement.prepend($("<div>").addClass("col-sm-1").append($("<span>").addClass("glyphicon glyphicon-remove").attr("style", "font-size:1.5em")));
        htmlElement.addClass("row list-group-item list-group-item-danger");
        content.show();
    }

    this.parentStep.stepActionContainer.append(htmlElement);
    this.parentStep.stepActionContainer.append(content);
    htmlElement.click(function(){
        content.toggle();
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
    var content = $("<div></div>").addClass("content");
    var firstRow = $("<div></div>").addClass("list-group-item-heading");
    var returnMessageField = $("<h4>").attr("style", "font-size:1.5em;margin:0px;line-height:1.3;height:1.5em;overflow:hidden;white-space: nowrap;text-overflow: ellipsis;");


    returnMessageField.text(this.returnMessage);

    firstRow.append(returnMessageField);

    content.append(firstRow);

    return content;
};

Control.prototype.generateContent = function () {
    var obj = this;

    var secondRow = $("<div></div>").addClass("row");
    var thirdRow = $("<div></div>").addClass("row");
    var fourthRow = $("<div></div>").addClass("row");
    var container = $("<div id='content-container'></div>").addClass("action-group row list-group-item").css("margin-left","25px");

    var descField = $("<input type='text' class='form-control' id='description'>").prop("readonly",true);
    var returnCodeField = $("<input type='text' class='form-control' id='returncode'>").prop("readonly",true);
    var controlTypeField = $("<input type='text' class='form-control' id='controltype'>").prop("readonly",true);
    var controlValueField = $("<input type='text' class='form-control' id='controlvalue'>").prop("readonly",true);
    var controlPropertyField = $("<input type='text' class='form-control' id='controlproperty'>").prop("readonly",true);
    var returnMessageField = $("<textarea class='form-control' id='returnmessage'>").prop("readonly",true);

    var descGroup = $("<div class='form-group'></div>").append($("<label for='description'>Description</label>")).append(descField);
    var returncodeGroup = $("<div class='form-group'></div>").append($("<label for='returncode'>Return Code</label>")).append(returnCodeField);
    var returnmessageGroup = $("<div class='form-group'></div>").append($("<label for='returnmessage'>Return Message</label>")).append(returnMessageField);
    var controlTypeGroup = $("<div class='form-group'></div>").append($("<label for='controltype'>Control Type</label>")).append(controlTypeField);
    var controlValueGroup = $("<div class='form-group'></div>").append($("<label for='controlvalue'>Control Value</label>")).append(controlValueField);
    var controlPropertyGroup = $("<div class='form-group'></div>").append($("<label for='controlproperty'>Control Property</label>")).append(controlPropertyField);



    descField.val(this.sequence + " - " + this.description);
    returnCodeField.val(this.returnCode);
    returnMessageField.val(this.returnMessage);
    controlTypeField.val(this.controlType);
    controlValueField.val(this.controlValue);
    controlPropertyField.val(this.controlProperty);

    secondRow.append($("<div></div>").addClass("col-sm-4").append(returncodeGroup));
    secondRow.append($("<div></div>").addClass("col-sm-4").append(descGroup));
    thirdRow.append($("<div></div>").addClass("col-sm-4").append(controlTypeGroup));
    thirdRow.append($("<div></div>").addClass("col-sm-4").append(controlValueGroup));
    thirdRow.append($("<div></div>").addClass("col-sm-4").append(controlPropertyGroup));
    fourthRow.append($("<div></div>").addClass("col-sm-12").append(returnmessageGroup));

    container.append(secondRow);
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
    json.controlValue = this.controlValue;
    json.controlProperty = this.controlProperty;
    json.fatal = this.fatal;
    json.screenshotFileName = this.screenshotFileName;

    return json;
};

