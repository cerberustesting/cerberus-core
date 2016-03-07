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

$.when($.getScript("js/pages/global/global.js")).then(function() {
    $(document).ready(function() {
        var doc = new Doc();
        var stepList = [];

        bindToggleCollapse();

        var executionId = GetURLParameter("executionId");

        displayHeaderLabel(doc);
        displayGlobalLabel(doc);
        displayFooter(doc);
        
        var steps;
        var testCaseExecution;
        $.ajax({
            url: "ReadTestCaseExecution",
            data: {executionId: executionId},
            dataType: "json",
            success: function(data) {
                testCaseExecution = data.testCaseExecution;
                loadTitleInfo(data.testCaseExecution);
                steps = testCaseExecution.testCaseStepExecutionList;
                //sortData(steps);
                createStepList(steps, stepList);
                
            },
            error: showUnexpectedError
        });

        $("#runTestCase").click(function() {
            runTestCase(test, testcase);
        });
    });
});

function runTestCase(test, testcase) {
    window.location.href = "./RunTest2.jsp?test=" + test + "&testcase=" + testcase;
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


function loadTitleInfo(info) {
    $(".testTestCase #executionId").text(info.id);
    $(".testTestCase #test").text(info.test);
    $(".testTestCase #testCase").text(info.testcase);
}



/** DATA AGREGATION **/

function sortStep(step) {
    for (var j = 0; j < step.actionList.length; j++) {
        var action = step.actionList[j];

        action.controlList.sort(function(a, b) {
            return a.control - b.control;
        });
    }

    step.actionList.sort(function(a, b) {
        return a.sequence - b.sequence;
    });
}

function sortData(agreg) {
    for (var i = 0; i < agreg.length; i++) {
        var step = agreg[i];

        sortStep(step);
    }

    agreg.sort(function(a, b) {
        return a.step - b.step;
    });
}

/** JAVASCRIPT OBJECT **/

function Step(json, stepList) {
    this.stepActionContainer = $("<div></div>").addClass("step-container").css("display", "none");

    this.test = json[0].test;
    this.testcase = json.testCase;
    this.step = json[0].step;
    this.description = json.description;
    this.timeElapsed = json[0].timeElapsed;
    this.useStep = json.useStep;
    this.useStepTest = json.useStepTest;
    this.useStepTestCase = json.useStepTestCase;
    this.useStepStep = json.useStepStep;
    this.inLibrary = json.inLibrary;
    this.actionList = [];
    this.setActionList(json[0].testCaseStepActionExecutionList);

    this.stepList = stepList;
    this.toDelete = false;

    this.html = $("<li></li>").addClass("list-group-item row").css("margin-left", "0px");
    this.textArea = $("<div></div>").addClass("col-lg-10").addClass("step-description").text("[Step "+this.step+"]  "+this.timeElapsed);

}

Step.prototype.draw = function() {
    var htmlElement = this.html;
    
    
    htmlElement.data("item", this);
    htmlElement.click(this.show);
    var object = htmlElement.data("item");
if (object.useStep === "Y") {
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

Step.prototype.show = function() {
    var object = $(this).data("item");

    for (var i = 0; i < object.stepList.length; i++) {
        var step = object.stepList[i];

        step.stepActionContainer.hide();
        step.html.removeClass("active");
    }
    $("#stepInfo > span").remove();
    $("#stepContent").removeClass();
    $(this).addClass("active");

    

if (object.useStep === "Y") {
    $("#stepInfo").prepend($("<span>").addClass("glyphicon glyphicon-ok pull-left").attr("style", "font-size:3em"));
        $("#stepContent").addClass("col-lg-8 list-group-item-success");
    } else {
        $("#stepInfo").prepend($("<span>").addClass("glyphicon glyphicon-remove pull-left").attr("style", "font-size:3em"));
        $("#stepContent").addClass("col-lg-8 list-group-item-danger");
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

Step.prototype.setActionList = function(actionList) {
    for (var i = 0; i < actionList.length; i++) {
        this.setAction(actionList[i]);
    }
};

Step.prototype.setAction = function(action) {
    if (action instanceof Action) {
        action.draw();
        this.actionList.push(action);
    } else {
        var actionObj = new Action(action, this);

        actionObj.draw();
        this.actionList.push(actionObj);
    }
};

Step.prototype.setDescription = function(description) {
    this.description = description;
    this.textArea.text(description);
    $("#stepDescription").text(description);
};

Step.prototype.setStep = function(step) {
    this.step = step;
};

Step.prototype.getJsonData = function() {
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
        this.sequence = json[0].sequence;
        this.description = json.description;
        this.action = json.action;
        this.object = json.object;
        this.property = json.property;
        this.screenshotFileName = json.screenshotFileName;
        this.controlList = [];
        this.setControlList(json[0].testCaseStepActionControlExecutionList);
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

Action.prototype.draw = function() {
    var htmlElement = this.html;
    var action = this;
    var row = $("<div></div>").addClass("col-lg-11");
    var type = $("<div></div>").addClass("type");
    
    
    row.append(this.generateContent());
    row.data("item", this);
    htmlElement.prepend(row);
    htmlElement.prepend($("<div>").addClass("col-lg-1").append($("<span>").addClass("glyphicon glyphicon-ok").attr("style", "font-size:1.5em")));
    htmlElement.addClass("list-group-item-success");

    this.parentStep.stepActionContainer.append(htmlElement);
};

Action.prototype.setControlList = function(controlList) {
    for (var i = 0; i < controlList.length; i++) {
        this.setControl(controlList[i]);
    }
};

Action.prototype.setControl = function(control) {
    if (control instanceof Control) {
        control.draw();
        this.controlList.push(control);
    } else {
        var controlObj = new Control(control, this);

        controlObj.draw();
        this.controlList.push(controlObj);
    }
};

Action.prototype.setStep = function(step) {
    this.step = step;
};

Action.prototype.setSequence = function(sequence) {
    this.sequence = sequence;
};

Action.prototype.generateContent = function() {
    var obj = this;
    var content = $("<div></div>").addClass("content col-lg-10").attr("style", "height:80px");
    var firstRow = $("<div></div>").addClass("row");
    var secondRow = $("<div></div>").addClass("row form-inline");

    var actionList = $("<text>");
    var descField = $("<text>").addClass("description");
    var objectField = $("<text>");
    var propertyField = $("<text>");

    descField.text(this.sequence);
    actionList.text(this.action);
    objectField.text(this.object);
    propertyField.text(this.property);
    
    firstRow.append(descField);
    secondRow.append($("<span></span>").addClass("col-lg-4").append(actionList));
    secondRow.append($("<span></span>").addClass("col-lg-4").append(objectField));
    secondRow.append($("<span></span>").addClass("col-lg-4").append(propertyField));

    
    content.append(firstRow);
    content.append(secondRow);

    return content;
};

Action.prototype.getJsonData = function() {
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

Control.prototype.draw = function() {
    var htmlElement = this.html;
    var control = this;
    var type = $("<div></div>").addClass("type");
    var content = this.generateContent();

    htmlElement.append(content);
    htmlElement.data("item", this);
    
    htmlElement.prepend($("<div>").addClass("col-lg-1").append($("<span>").addClass("glyphicon glyphicon-ok").attr("style", "font-size:1.5em")));
    htmlElement.addClass("list-group-item-success");

    this.parentAction.html.append(htmlElement);
};

Control.prototype.setStep = function(step) {
    this.step = step;
};

Control.prototype.setSequence = function(sequence) {
    this.sequence = sequence;
};

Control.prototype.setControl = function(control) {
    this.control = control;
};

Control.prototype.generateContent = function() {
    var obj = this;
    var content = $("<div></div>");
    var firstRow = $("<div></div>");
    var secondRow = $("<div></div>");

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

Control.prototype.getJsonData = function() {
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

