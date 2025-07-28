/*
 * Cerberus Copyright (C) 2013 - 2025 cerberustesting
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

///// ACTION JSON OBJECT
Action.prototype.getJsonData = function () {
    var json = {};

    json.toDelete = this.toDelete;
    json.test = this.test;
    json.testcase = this.testcase;
    json.stepId = this.stepId;
    json.actionId = this.actionId;
    json.sort = this.sort;
    json.description = this.description;
    json.action = this.action;
    json.object = this.value1;
    json.property = this.value2;
    json.value3 = this.value3;
    json.options = this.options;
    json.isFatal = this.isFatal;
    json.conditionOperator = this.conditionOperator;
    json.conditionValue1 = this.conditionValue1;
    json.conditionValue2 = this.conditionValue2;
    json.conditionValue3 = this.conditionValue3;
    json.conditionOptions = this.conditionOptions;
    json.screenshotFileName = "";
    json.waitBefore = this.waitBefore;
    json.waitAfter = this.waitAfter;
    json.doScreenshotBefore = this.doScreenshotBefore;
    json.doScreenshotAfter = this.doScreenshotAfter;

    return json;
};

///// GETTERS && SETTERS
Action.prototype.setControls = function (controls, canUpdate) {
    for (var i = 0; i < controls.length; i++) {
        this.setControl(controls[i], undefined, canUpdate);
    }
};

Action.prototype.setControl = function (control, afterControl, canUpdate) {
    if (control instanceof Control) {
        control.draw(afterControl);
        this.controls.push(control);
    } else {
        var controlObj = new Control(control, this, canUpdate);

        controlObj.draw(afterControl);
        this.controls.push(controlObj);
    }
};

Action.prototype.setStepId = function (stepId) {
    this.stepId = stepId;
};

Action.prototype.setActionId = function (actionId) {
    this.actionId = actionId;
};

Action.prototype.getActionId = function () {
    return this.actionId;
};

Action.prototype.setSort = function (sort) {
    this.sort = sort;
    this.refreshSort();
};

Action.prototype.refreshSort = function () {
    this.html.find(".action #labelDiv").text(this.sort);
};

/**
 * Call generateContent Function to generate the HTML Element
 * Set Placeholder
 * Set Select2 for action type
 * Append Element to Action List
 * Refresh Sort
 */
Action.prototype.draw = function (afterAction) {
    var htmlElement = this.html;
    var action = this;

    var row = this.generateContent();

    htmlElement.prepend(row);

    setPlaceholderAction(htmlElement);

    $("[name='actionSelect']").select2({
        minimumResultsForSearch: 20,
        templateSelection: formatActionSelect2Result,
        templateResult: formatActionSelect2Result
    });

    listenEnterKeypressWhenFocusingOnDescription(htmlElement);

    if (afterAction === undefined) {
        this.parentStep.stepActionContainer.append(htmlElement);
    } else {
        afterAction.html.after(htmlElement);
    }
    this.refreshSort();
};

/**
 * Generate HTML Element
 */
Action.prototype.generateContent = function () {
    var action = this;
    var doc = new Doc();
    let uniqfieldid = 'a' + uniqid++;

    var row = $("<div></div>").addClass("step-action row").addClass("action");
    var content = $("<div></div>").addClass("content col-lg-8");
    var firstRow = $("<div style='margin-top:15px;margin-left:0px'></div>").addClass("fieldRow row input-group marginBottom10 col-xs-12 col-lg-12");
    var secondRow = $("<div></div>").addClass("fieldRow row secondRow input-group").css("width", "100%");
    var thirdRow = $("<div></div>").addClass("fieldRow row thirdRow input-group");

    var picture = $("<div></div>").addClass("col-lg-2").css("height", "100%")
            .append($("<div style='margin-top:10px;margin-left:10px;margin-right:10px;max-width: 250px'></div>")
                    .append($("<img>").attr("id", "ApplicationObjectImg1").css("width", "100%").css("cursor", "pointer"))
                    .append($("<img>").attr("id", "ApplicationObjectImg2").css("width", "100%").css("margin-top", "10px").css("cursor", "pointer"))
                    .append($("<img>").attr("id", "ApplicationObjectImg3").css("width", "100%").css("margin-top", "10px").css("cursor", "pointer")));


    //FIRST ROW
    var plusBtn = $("<button></button>").addClass("btn add-btn config-btn").attr("data-toggle", "modal").attr("data-target", "#modalOptions").append($("<span></span>").addClass("glyphicon glyphicon-cog"));
    var addBtn = $("<button></button>").addClass("btn add-btn btnLightGreen").append($("<span></span>").addClass("glyphicon glyphicon-plus"));
    var addABtn = $("<button></button>").addClass("btn add-btn btnLightBlue").append($("<span></span>").addClass("glyphicon glyphicon-plus"));
    var supprBtn = $("<button></button>").addClass("btn add-btn deleteItem-btn").append($("<span></span>").addClass("glyphicon glyphicon-trash"));
    var btnGrp = $("<div></div>").addClass("col-lg-2").css("padding", "0px").append($("<div>").addClass("boutonGroup pull-right").append(addABtn).append(supprBtn).append(addBtn).append(plusBtn));

    addBtn.click(function () {
        addControlAndFocus(action);
        displayControlCombo(action.parentStep.html.data('item'));
    });
    addABtn.click(function () {
        addActionAndFocus(action);
        displayActionCombo(action.parentStep.html.data('item'));
    });

    supprBtn.click(function () {
        setModif(true);
        action.toDelete = (action.toDelete) ? false : true;

        if (action.toDelete) {
            action.html.find(".step-action").addClass("danger");
        } else {
            action.html.find(".step-action").removeClass("danger");
        }
    });

    plusBtn.click(function () {
        displayOverrideOptionsModal(action, action.html);
    });


// DESCRIPTION
    var descContainer = $("<div class='input-group'></div>");
    var descriptionField = $("<input class='description form-control crb-autocomplete-variable' placeholder='" + doc.getDocLabel("page_testcasescript", "describe_action") + "'>").attr("style", "border:0px; text-overflow: ellipsis;");
    var drag = $("<span></span>").addClass("input-group-addon").addClass("drag-step-action").attr("style", "font-weight: 700;border-radius:4px;border:1px solid #ccc").attr("id", "labelDiv").prop("draggable", true);
    drag.on("dragstart", handleDragStart);
    drag.on("dragenter", handleDragEnter);
    drag.on("dragover", handleDragOver);
    drag.on("dragleave", handleDragLeave);
    drag.on("drop", handleDrop);
    drag.on("dragend", handleDragEnd);
    descContainer.append(drag);
    descContainer.append(descriptionField);

    descriptionField.val(this.description);
    descriptionField.css("width", "100%");
    descriptionField.on("change", function () {
        setModif(true);
        action.description = descriptionField.val();
    });
// END OF DESCRIPTION

//ACTION FIELD
    var user = getUser();
    var actionDivContainer = $("<div></div>").addClass("col-lg-8 form-group marginBottom10 actionSelectContainer");
// END OF ACTION FIELD

//VALUE1 FIELD
    var value1Field = $("<input>").attr("data-toggle", "tooltip").attr("data-animation", "false").attr("data-html", "true").attr("data-container", "body").attr("data-placement", "top").attr("data-trigger", "manual")
            .attr("type", "text").addClass("form-control input-sm v1");
    value1Field.val(cleanErratum(this.value1));
    value1Field.on("change", function () {
        action.value1 = convertValueWithErratum(action.value1, value1Field.val());
    });

    var field1Container = $("<div class='input-group'></div>");
    var field1Addon = $("<span></span>").attr("id", "field1Addon").attr('for', uniqfieldid + 'v1').addClass("input-group-addon togglefullscreen").attr("style", "font-weight: 700;");
    field1Addon.append("<img width='15px' height='15px' src='images/action-website.png'>");
    value1Field.attr("aria-describedby", "field1Addon");
    field1Container.append(field1Addon).append(value1Field);
//END OF VALUE1 FIELD

//VALUE2 FIELD
    var value2Field = $("<input>").attr("data-toggle", "tooltip").attr("data-animation", "false").attr("data-html", "true").attr("data-container", "body").attr("data-placement", "top").attr("data-trigger", "manual")
            .attr("type", "text").addClass("form-control input-sm v2");
    value2Field.val(cleanErratum(this.value2));
    value2Field.on("change", function () {
        action.value2 = convertValueWithErratum(action.value2, value2Field.val());
    });

    var field2Container = $("<div class='input-group'></div>");
    var field2Addon = $("<span></span>").attr("id", "field2Addon").attr('for', uniqfieldid + 'v2').addClass("input-group-addon togglefullscreen").attr("style", "font-weight: 700;");
    field2Addon.append("<img width='15px' height='15px' src='images/action-website.png'>");
    value2Field.attr("aria-describedby", "field2Addon");
    field2Container.append(field2Addon).append(value2Field);
//END OF VALUE2 FIELD

//VALUE3 FIELD
    var value3Field = $("<input>").attr("data-toggle", "tooltip").attr("data-animation", "false").attr("data-html", "true").attr("data-container", "body").attr("data-placement", "top").attr("data-trigger", "manual").attr("type", "text").addClass("form-control v3");
    value3Field.val(cleanErratum(this.value3));
    value3Field.on("change", function () {
        action.value3 = convertValueWithErratum(action.value3, value3Field.val());
    });

    var field3Container = $("<div class='input-group'></div>");
    var field3Addon = $("<span></span>").attr("id", "field3Addon").addClass("input-group-addon").attr("style", "font-weight: 700;");
    field3Addon.append("<img width='15px' height='15px' src='images/action-website.png'>");
    value3Field.attr("aria-describedby", "field3Addon");
    field3Container.append(field3Addon).append(value3Field);
//END OF VALUE3 FIELD


//STRUCTURE
    firstRow.append(descContainer);
    secondRow.append(actionDivContainer);
    secondRow.append($("<div></div>").addClass("v1 col-lg-5 form-group marginBottom10").attr('id', uniqfieldid + 'v1').append(field1Container));
    secondRow.append($("<div></div>").addClass("v2 col-lg-2 form-group marginBottom10").attr('id', uniqfieldid + 'v2').append(field2Container));
    secondRow.append($("<div></div>").addClass("v3 col-lg-2 form-group marginBottom10").append(field3Container));


    if ((this.parentStep.isUsingLibraryStep) || (!action.hasPermissionsUpdate)) {
        descriptionField.prop("readonly", true);
        value1Field.prop("readonly", true);
        value2Field.prop("readonly", true);
        value3Field.prop("readonly", true);
        btnGrp.find('.boutonGroup').hide();
    }

    content.append(firstRow);
    content.append(secondRow);
    content.append(thirdRow);


    row.append(content);
    row.append(picture);
    row.append(btnGrp);
    row.data("item", this);

    printLabelForOptions(btnGrp, action.options, action.conditionOptions, "optionLabel");
    printLabelForCondition(btnGrp, action.conditionOperator, action.conditionValue1, action.conditionValue2, action.conditionValue3);
    printLabel(btnGrp, action.isFatal, "actionFatalLabel", "labelOrange", "Stop Execution on Failure")

    return row;
};


/**
 *
 * @param json
 * @param parentStep
 * @param canUpdate
 * @constructor
 */
function Action(json, parentStep, canUpdate) {
    this.html = $("<div></div>").addClass("action-group");
    this.parentStep = parentStep;

    if (json !== null) {
        this.test = json.test;
        this.testcase = json.testcase;
        this.stepId = json.stepId;
        this.actionId = json.actionId;
        this.sort = json.sort;
        this.description = json.description;
        this.action = json.action;
        this.isFatal = json.isFatal;
        this.conditionOperator = json.conditionOperator;
        this.conditionValue1 = json.conditionValue1;
        this.conditionValue2 = json.conditionValue2;
        this.conditionValue3 = json.conditionValue3;
        this.conditionOptions = json.conditionOptions;
        this.screenshotFileName = json.screenshotFileName;
        this.value1 = json.value1;
        this.value2 = json.value2;
        this.value3 = json.value3;
        this.options = json.options;
        this.waitBefore = json.waitBefore;
        this.waitAfter = json.waitAfter;
        this.doScreenshotBefore = json.doScreenshotBefore;
        this.doScreenshotAfter = json.doScreenshotAfter;
        this.controls = [];
        this.setControls(json.controls, canUpdate);
    } else {
        this.test = "";
        this.testcase = "";
        this.stepId = parentStep.stepId;
        this.description = "";
        this.action = "doNothing";
        this.isFatal = true;
        this.conditionOperator = "always";
        this.conditionValue1 = "";
        this.conditionValue2 = "";
        this.conditionValue3 = "";
        this.conditionOptions = [];
        this.screenshotFileName = "";
        this.value1 = "";
        this.value2 = "";
        this.value3 = "";
        this.options = [];
        this.waitBefore = 0;
        this.waitAfter = 0;
        this.doScreenshotBefore = false;
        this.doScreenshotAfter = false;
        this.controls = [];
    }

    this.toDelete = false;
    this.hasPermissionsUpdate = canUpdate;
}

/**
 * Set Placeholder for Specified Action
 * @param action
 */
function setPlaceholderAction(action) {
    var user = getUser();
    var actionElement = $(action).find("[name='actionSelect'] option:selected");
    var placeHolders = actionOptList[actionElement.val()];

    if (typeof placeHolders === 'undefined') {
        placeHolders = actionOptList["unknown"];
    }

    if (typeof placeHolders.field1 !== 'undefined') {
        $(actionElement).parents("div[class*='secondRow']").children("div[class*='v1']")
                .removeClass("col-lg-2 col-lg-3 col-lg-4 col-lg-5 col-lg-6 col-lg-7 col-lg-8 col-lg-9 crb-autocomplete-element crb-autocomplete-property crb-autocomplete-service crb-autocomplete-variable crb-autocomplete-fileuploadflag crb-autocomplete-filesortflag crb-autocomplete-boolean crb-autocomplete-select crb-autocomplete-switch crb-contextual-button")
                .addClass(placeHolders.field1.class);
        $(actionElement).parents("div[class*='secondRow']").children("div[class*='v1']").show();
        $(actionElement).parents("div[class*='secondRow']").children("div[class*='v1']").find('input').attr("placeholder", placeHolders.field1.label[user.language]);
        $(actionElement).parents("div[class*='secondRow']").children("div[class*='v1']").find('#field1Addon').attr("data-toggle", "tooltip").attr("data-original-title", placeHolders.field1.label[user.language]);
        if (typeof placeHolders.field1.picto !== 'undefined') {
            $(actionElement).parents("div[class*='secondRow']").children("div[class*='v1']").find('img').attr("src", placeHolders.field1.picto);
        }
    } else {
        $(actionElement).parents("div[class*='secondRow']").children("div[class*='v1']").hide();
    }
    if (typeof placeHolders.field2 !== 'undefined') {
        $(actionElement).parents("div[class*='secondRow']").children("div[class*='v2']")
                .removeClass("col-lg-2 col-lg-3 col-lg-4 col-lg-5 col-lg-6 col-lg-7 col-lg-8 col-lg-9 crb-autocomplete-element crb-autocomplete-property crb-autocomplete-service crb-autocomplete-variable crb-autocomplete-fileuploadflag crb-autocomplete-filesortflag crb-autocomplete-boolean crb-autocomplete-select crb-autocomplete-switch crb-contextual-button")
                .addClass(placeHolders.field2.class);
        $(actionElement).parents("div[class*='secondRow']").children("div[class*='v2']").show();
        $(actionElement).parents("div[class*='secondRow']").children("div[class*='v2']").find('input').attr("placeholder", placeHolders.field2.label[user.language]);
        $(actionElement).parents("div[class*='secondRow']").children("div[class*='v2']").find('#field2Addon').attr("data-toggle", "tooltip").attr("data-original-title", placeHolders.field2.label[user.language]);
        if (typeof placeHolders.field2.picto !== 'undefined') {
            $(actionElement).parents("div[class*='secondRow']").children("div[class*='v2']").find('img').attr("src", placeHolders.field2.picto);
        }
    } else {
        $(actionElement).parents("div[class*='secondRow']").children("div[class*='v2']").hide();
    }
    if (typeof placeHolders.field3 !== 'undefined') {
        $(actionElement).parents("div[class*='secondRow']").children("div[class*='v3']")
                .removeClass("col-lg-2 col-lg-3 col-lg-4 col-lg-5 col-lg-6 col-lg-7 col-lg-8 col-lg-9 crb-autocomplete-element crb-autocomplete-property crb-autocomplete-service crb-autocomplete-variable crb-autocomplete-fileuploadflag crb-autocomplete-filesortflag crb-autocomplete-boolean crb-autocomplete-select crb-autocomplete-switch crb-contextual-button")
                .addClass(placeHolders.field3.class);
        $(actionElement).parents("div[class*='secondRow']").children("div[class*='v3']").show();
        $(actionElement).parents("div[class*='secondRow']").children("div[class*='v3']").find('input').attr("placeholder", placeHolders.field3.label[user.language]);
        $(actionElement).parents("div[class*='secondRow']").children("div[class*='v3']").find('#field3Addon').attr("data-toggle", "tooltip").attr("data-original-title", placeHolders.field3.label[user.language]);
        if (typeof placeHolders.field3.picto !== 'undefined') {
            $(actionElement).parents("div[class*='secondRow']").children("div[class*='v3']").find('img').attr("src", placeHolders.field3.picto);
        }
    } else {
        $(actionElement).parents("div[class*='secondRow']").children("div[class*='v3']").hide();
    }
    $('[data-toggle="tooltip"]').tooltip();
}

/**
 * Display all Action combo of Current Step
 * @param object
 */
function displayActionCombo(object) {
    $(object.stepActionContainer).find(".action").each(function () {
        var actions = $(getActionCombo());
        var actionItem = $(this).data("item");
        actions.val(actionItem.action);
        actions.off("change").on("change", function () {
            setModif(true);
            actionItem.action = actions.val();
            setPlaceholderAction($(this).parents(".action"));
        });
        $(this).find(".actionSelectContainer").empty();
        $(this).find(".actionSelectContainer").append(actions);
        setPlaceholderAction($(this));

        if ((object.isUsingLibraryStep) || (!object.hasPermissionsUpdate)) {
            actions.prop("disabled", "disabled");
        }

        actions.select2({
            minimumResultsForSearch: 20,
            templateSelection: formatActionSelect2Result,
            templateResult: formatActionSelect2Result
        });
    });

}

/**
 * Create Action JSON Object
 * Attach it to step
 * Sort
 */
function addAction(action) {
    setModif(true);
    var step = $("#steps li.active").data("item");
    var act = new Action(null, step, true);
    step.setAction(act, action);
    setAllSort();
    return act;
}

/**
 * Add action and focus on Description field
 */
function addActionAndFocus(action) {
    $.when(addAction(action)).then(function (action) {
        listenEnterKeypressWhenFocusingOnDescription();
        $($(action.html[0]).find(".description")[0]).focus();
    });
}

function addActionFromBottomButton() {
    addActionAndFocus();
    displayActionCombo($("#steps li.active").data("item"));
}