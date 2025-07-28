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

Control.prototype.getJsonData = function () {
    var json = {};

    json.toDelete = this.toDelete;
    json.test = this.test;
    json.testcase = this.testcase;
    json.stepId = this.stepId;
    json.actionId = this.actionId;
    json.control = this.control;
    json.sort = this.sort;
    json.description = this.description;
    json.controlId = this.controlId;
    json.value1 = this.value1;
    json.value2 = this.value2;
    json.value3 = this.value3;
    json.options = this.options;
    json.isFatal = this.isFatal;
    json.conditionOperator = this.conditionOperator;
    json.conditionValue1 = this.conditionValue1;
    json.conditionValue2 = this.conditionValue2;
    json.conditionValue3 = this.conditionValue3;
    json.conditionOptions = this.conditionOptions;

    json.screenshotFileName = this.screenshotFileName;

    json.waitBefore = this.waitBefore;
    json.waitAfter = this.waitAfter;
    json.doScreenshotBefore = this.doScreenshotBefore;
    json.doScreenshotAfter = this.doScreenshotAfter;

    return json;
};

Control.prototype.generateContent = function () {
    var control = this;
    var doc = new Doc();
    let uniqfieldid = 'c' + uniqid++;
    
    var row = this.html;
    var content = $("<div></div>").addClass("content col-lg-8");
    var firstRow = $("<div style='margin-top:15px;margin-left:0px'></div>").addClass("fieldRow row input-group marginBottom10 col-xs-12 col-lg-12");
    var secondRow = $("<div></div>").addClass("fieldRow row secondRow input-group col-xs-12 col-lg-12");
    var thirdRow = $("<div></div>").addClass("fieldRow row thirdRow input-group");

    var picture = $("<div></div>").addClass("col-lg-2").css("height", "100%")
            .append($("<div style='margin-top:10px;margin-left:10px;margin-right:10px;max-width: 250px'></div>")
                    .append($("<img>").attr("id", "ApplicationObjectImg1").css("width", "100%").css("cursor", "pointer"))
                    .append($("<img>").attr("id", "ApplicationObjectImg2").css("width", "100%").css("margin-top", "10px").css("cursor", "pointer"))
                    .append($("<img>").attr("id", "ApplicationObjectImg3").css("width", "100%").css("margin-top", "10px").css("cursor", "pointer")));


    var plusBtn = $("<button></button>").addClass("btn add-btn config-btn").attr("data-toggle", "modal").attr("data-target", "#modalOptions").append($("<span></span>").addClass("glyphicon glyphicon-cog"));
    var addBtn = $("<button></button>").addClass("btn add-btn btnLightGreen").append($("<span></span>").addClass("glyphicon glyphicon-plus"));
    var addABtn = $("<button></button>").addClass("btn add-btn btnLightBlue").append($("<span></span>").addClass("glyphicon glyphicon-plus"));
    var supprBtn = $("<button></button>").addClass("btn add-btn deleteItem-btn").append($("<span></span>").addClass("glyphicon glyphicon-trash"));
    var btnGrp = $("<div></div>").addClass("col-lg-2").css("padding", "0px").append($("<div>").addClass("marginRight10 boutonGroup pull-right").append(addABtn).append(supprBtn).append(addBtn).append(plusBtn));

    addABtn.click(function () {
        addActionAndFocus(control.parentAction);
        displayActionCombo(control.parentStep.html.data('item'));
    });

    addBtn.click(function () {
        addControlAndFocus(control.parentAction, control);
        displayControlCombo(control.parentStep.html.data('item'));
    });
    supprBtn.click(function () {
        setModif(true);
        control.toDelete = (control.toDelete) ? false : true;

        if (control.toDelete) {
            control.html.addClass("danger");
        } else {
            control.html.removeClass("danger");
        }
    });

    plusBtn.click(function () {
        displayOverrideOptionsModal(control, control.html);
    });


//DESCRIPTION
    var descContainer = $("<div class='input-group'></div>");
    var descriptionField = $("<input class='description form-control crb-autocomplete-variable' placeholder='" + doc.getDocLabel("page_testcasescript", "describe_control") + "'>").attr("style", "border:0px; text-overflow: ellipsis;");
    var drag = $("<span></span>").addClass("input-group-addon").addClass("drag-step-action").attr("style", "font-weight: 700;").attr("id", "labelDiv").prop("draggable", true);
    var ctrlNumber = $($("<span class='input-group-addon' style='font-weight: 700;border-top-right-radius: 4px;border-bottom-right-radius: 4px;' id='labelControlDiv'></span>"));

    drag.on("dragstart", handleDragStart);
    drag.on("dragenter", handleDragEnter);
    drag.on("dragover", handleDragOver);
    drag.on("dragleave", handleDragLeave);
    drag.on("drop", handleDrop);
    drag.on("dragend", handleDragEnd);
    descContainer.append(drag);
    descContainer.append(ctrlNumber);
    descContainer.append(descriptionField);

    descriptionField.val(this.description);
    descriptionField.css("width", "100%");
    descriptionField.on("change", function () {
        setModif(true);
        control.description = descriptionField.val();
    });
//END OF DESCRIPTION

//CONTROL FIELD
    var user = getUser();
    var controlDivContainer = $("<div></div>").addClass("col-lg-8 form-group marginBottom10 controlSelectContainer");
    var controlOperatorDivContainer = $("<div></div>").addClass("col-lg-4 form-group marginBottom10 controlOperatorContainer");
// END OF CONTROL FIELD

//VALUE1 FIELD
    var controlValue1Field = $("<input>").attr("data-toggle", "tooltip").attr("data-animation", "false").attr("data-html", "true").attr("data-container", "body").attr("data-placement", "top").attr("data-trigger", "manual").addClass("form-control v1");
    controlValue1Field.val(cleanErratum(this.value1));
    controlValue1Field.css("width", "84%");
    controlValue1Field.on("change", function () {
        setModif(true);
        control.value1 = convertValueWithErratum(control.value1, controlValue1Field.val());
    });
    var controlField1Container = $("<div class='input-group'></div>");
    var controlField1Addon = $("<span></span>").attr("id", "controlField1Addon").attr('for', uniqfieldid + 'v1').addClass("input-group-addon togglefullscreen").attr("style", "font-weight: 700;");
    controlField1Addon.append("<img width='15px' height='15px' src='images/action-website.png'>");
    controlValue1Field.attr("aria-describedby", "controlField1Addon");
    controlField1Container.append(controlField1Addon).append(controlValue1Field);
//END OF VALUE1 FIELD

//VALUE2 FIELD
    var controlValue2Field = $("<input>").attr("data-toggle", "tooltip").attr("data-animation", "false").attr("data-html", "true").attr("data-container", "body").attr("data-placement", "top").attr("data-trigger", "manual").addClass("form-control v2").css("width", "100%");
    controlValue2Field.val(cleanErratum(this.value2));
    controlValue2Field.css("width", "84%");
    controlValue2Field.on("change", function () {
        setModif(true);
        control.value2 = convertValueWithErratum(control.value2, controlValue2Field.val());
    });
    var controlField2Container = $("<div class='input-group'></div>");
    var controlField2Addon = $("<span></span>").attr("id", "controlField2Addon").attr('for', uniqfieldid + 'v2').addClass("input-group-addon togglefullscreen").attr("style", "font-weight: 700;");
    controlField2Addon.append("<img width='15px' height='15px' src='images/action-website.png'>");
    controlValue2Field.attr("aria-describedby", "controlField2Addon");
    controlField2Container.append(controlField2Addon).append(controlValue2Field);
//END OF VALUE2 FIELD

//VALUE3 FIELD
    var controlValue3Field = $("<input>").attr("data-toggle", "tooltip").attr("data-animation", "false").attr("data-html", "true").attr("data-container", "body").attr("data-placement", "top").attr("data-trigger", "manual").addClass("form-control v3").css("width", "100%");
    controlValue3Field.val(this.value3);
    controlValue3Field.css("width", "84%");
    controlValue3Field.on("change", function () {
        setModif(true);
        control.value3 = controlValue3Field.val();
    });
    var controlField3Container = $("<div class='input-group'></div>");
    var controlField3Addon = $("<span></span>").attr("id", "controlField3Addon").attr('for', uniqfieldid + 'v3').addClass("input-group-addon togglefullscreen").attr("style", "font-weight: 700;");
    controlField3Addon.append("<img width='15px' height='15px' src='images/action-website.png'>");
    controlValue3Field.attr("aria-describedby", "controlField3Addon");
    controlField3Container.append(controlField3Addon).append(controlValue3Field);
//END OF VALUE3 FIELD

    firstRow.append(descContainer);
    secondRow.append(controlDivContainer);
    secondRow.append($("<div></div>").addClass("v1 col-lg-3 form-group marginBottom10").attr('id', uniqfieldid + 'v1').append(controlField1Container));
    secondRow.append(controlOperatorDivContainer);
    secondRow.append($("<div></div>").addClass("v2 col-lg-3 form-group marginBottom10").attr('id', uniqfieldid + 'v2').append(controlField2Container));
    secondRow.append($("<div></div>").addClass("v3 col-lg-3 form-group marginBottom10").attr('id', uniqfieldid + 'v3').append(controlField3Container));


    if ((this.parentStep.isUsingLibraryStep) || (!control.hasPermissionsUpdate)) {
        descriptionField.prop("readonly", true);
        controlValue1Field.prop("readonly", true);
        controlValue2Field.prop("readonly", true);
        controlValue3Field.prop("readonly", true);
        //controls.prop("disabled", "disabled");
        btnGrp.find('.boutonGroup').hide();
    }

    content.append(firstRow);
    content.append(secondRow);
    content.append(thirdRow);

    row.append(content);
    row.append(picture);
    row.append(btnGrp);
    row.data("item", this);

    printLabelForOptions(btnGrp, control.options, control.conditionOptions, "controlOptionLabel");
    printLabelForCondition(btnGrp, control.conditionOperator);
    printLabel(btnGrp, control.isFatal, "controlFatalLabel", "labelOrange", "Stop Execution on Failure")


    return row;
};

Control.prototype.draw = function (afterControl) {
    var htmlElement = this.html;
    var control = this;

    var row = this.generateContent();

    htmlElement.append(row);

    //setPlaceholderControl(htmlElement);
    //setPlaceholderCondition(htmlElement);
    listenEnterKeypressWhenFocusingOnDescription(htmlElement);

    if (afterControl === undefined) {
        this.parentAction.html.append(htmlElement);
    } else {
        afterControl.html.after(htmlElement);
    }
    this.refreshSort();
};

Control.prototype.setStepId = function (stepId) {
    this.stepId = stepId;
};

Control.prototype.setActionId = function (actionId) {
    this.actionId = actionId;
};

Control.prototype.getControl = function () {
    return this.control;
};

Control.prototype.setControlId = function (controlId) {
    this.controlId = controlId;
};

Control.prototype.setControl = function (control) {
    this.control = control;
};

Control.prototype.setParentActionSort = function (parentActionSort) {
    this.parentActionSort = parentActionSort;
};

Control.prototype.setSort = function (sort) {
    this.sort = sort;
    this.refreshSort();
};

Control.prototype.refreshSort = function () {
    this.html.find("#labelDiv").text(this.parentActionSort);
    this.html.find("#labelControlDiv").text(this.sort);
};

function Control(json, parentAction, canUpdate) {
    if (json !== null) {
        this.test = json.test;
        this.testcase = json.testcase;
        this.stepId = json.stepId;
        this.actionId = json.actionId;
        this.control = json.control;
        this.sort = json.sort;
        this.description = json.description;
        this.controlId = json.controlId;
        this.value1 = json.value1;
        this.value2 = json.value2;
        this.value3 = json.value3;
        this.options = json.options;
        this.isFatal = json.isFatal;
        this.conditionOperator = json.conditionOperator;
        this.conditionValue1 = json.conditionValue1;
        this.conditionValue2 = json.conditionValue2;
        this.conditionValue3 = json.conditionValue3;
        this.conditionOptions = json.conditionOptions;
        this.screenshotFileName = "";
        this.waitBefore = json.waitBefore;
        this.waitAfter = json.waitAfter;
        this.doScreenshotBefore = json.doScreenshotBefore;
        this.doScreenshotAfter = json.doScreenshotAfter;
    } else {
        this.test = "";
        this.testcase = "";
        this.stepId = parentAction.stepId;
        this.actionId = parentAction.actionId;
        this.control = "Unknown";
        this.description = "";
        this.objType = "Unknown";
        this.value1 = "";
        this.value2 = "";
        this.value3 = "";
        this.options = [];
        this.isFatal = false;
        this.conditionOperator = "always";
        this.conditionValue1 = "";
        this.conditionValue2 = "";
        this.conditionValue3 = "";
        this.conditionOptions = [];
        this.screenshotFileName = "";
        this.waitBefore = 0;
        this.waitAfter = 0;
        this.doScreenshotBefore = false;
        this.doScreenshotAfter = false;
    }

    this.parentStep = parentAction.parentStep;
    this.parentAction = parentAction;
    this.parentActionSort = parentAction.sort;

    this.toDelete = false;
    this.hasPermissionsUpdate = canUpdate;

    this.html = $("<div></div>").addClass("step-action row").addClass("control");
}

function setPlaceholderControl(control) {

    var user = getUser();
    var controlSelect = control.find(".controlType");
    var operatorSelect = control.find(".operator");
    var placeHolders = convertToGui[newControlOptList[controlSelect.val()][operatorSelect.val()]];

    if (typeof placeHolders === 'undefined') {
        placeHolders = convertToGui["unknown"];
    }

    if (placeHolders.operator === "unknown") {
        control.find(".operator").hide();
    } else {
        control.find(".operator").show();
    }

    if (typeof placeHolders.field1 !== 'undefined') {
        control.find("div[class*='v1']")
                .removeClass("col-lg-2 col-lg-3 col-lg-4 col-lg-5 col-lg-6 col-lg-7 col-lg-8 col-lg-9 crb-autocomplete-element crb-autocomplete-property crb-autocomplete-service crb-autocomplete-variable crb-autocomplete-fileuploadflag crb-autocomplete-filesortflag crb-autocomplete-boolean crb-autocomplete-select crb-autocomplete-switch crb-contextual-button")
                .addClass(placeHolders.field1.class);
        control.find("div[class*='v1']").show();
        control.find("div[class*='v1']").find('input').attr("placeholder", placeHolders.field1.label[user.language]);
        control.find("div[class*='v1']").find('#controlField1Addon').attr("data-toggle", "tooltip").attr("data-original-title", placeHolders.field1.label[user.language]);

        if (typeof placeHolders.field1.picto !== 'undefined') {
            control.find("div[class*='v1']").find('img').attr("src", placeHolders.field1.picto);
        }
    } else {
        control.find("div[class*='v1']").hide();
    }
    if (typeof placeHolders.field2 !== 'undefined') {
        control.find("div[class*='v2']")
                .removeClass("col-lg-2 col-lg-3 col-lg-4 col-lg-5 col-lg-6 col-lg-7 col-lg-8 col-lg-9 crb-autocomplete-element crb-autocomplete-property crb-autocomplete-service crb-autocomplete-variable crb-autocomplete-fileuploadflag crb-autocomplete-filesortflag crb-autocomplete-boolean crb-autocomplete-select crb-autocomplete-switch crb-contextual-button")
                .addClass(placeHolders.field2.class);
        control.find("div[class*='v2']").show();
        control.find("div[class*='v2']").find('input').attr("placeholder", placeHolders.field2.label[user.language]);
        control.find("div[class*='v2']").find('#controlField2Addon').attr("data-toggle", "tooltip").attr("data-original-title", placeHolders.field2.label[user.language]);
        if (typeof placeHolders.field2.picto !== 'undefined') {
            control.find("div[class*='v2']").find('img').attr("src", placeHolders.field2.picto);
        }
    } else {
        control.find("div[class*='v2']").hide();
    }
    if (typeof placeHolders.field3 !== 'undefined') {
        control.find("div[class*='v3']")
                .removeClass("col-lg-2 col-lg-3 col-lg-4 col-lg-5 col-lg-6 col-lg-7 col-lg-8 col-lg-9 crb-autocomplete-element crb-autocomplete-property crb-autocomplete-service crb-autocomplete-variable crb-autocomplete-fileuploadflag crb-autocomplete-filesortflag crb-autocomplete-boolean crb-autocomplete-select crb-autocomplete-switch crb-contextual-button")
                .addClass(placeHolders.field3.class);
        control.find("div[class*='v3']").show();
        control.find("div[class*='v3']").find('input').attr("placeholder", placeHolders.field3.label[user.language]);
        control.find("div[class*='v3']").find('#controlField3Addon').attr("data-toggle", "tooltip").attr("data-original-title", placeHolders.field3.label[user.language]);
        if (typeof placeHolders.field3.picto !== 'undefined') {
            control.find("div[class*='v3']").find('img').attr("src", placeHolders.field3.picto);
        }
    } else {
        control.find("div[class*='v3']").hide();
    }
    $('[data-toggle="tooltip"]').tooltip();
}

function addControl(action, control) {
    setModif(true);
    var act;
    if (action instanceof Action) {
        act = action;
    } else {
        act = action.data("item");
    }

    var ctrl = new Control(null, act, true);
    act.setControl(ctrl, control, true);
    setAllSort();
    return ctrl;
}

function addControlAndFocus(oldAction, control) {
    $.when(addControl(oldAction, control)).then(function (action) {
        $($(action.html[0]).find(".description")[0]).focus();
    });
}

/**
 * Display all Control Combo of Current Step
 * @param object
 */
function displayControlCombo(object) {
    var user = getUser();
    $(object.stepActionContainer).find(".control").each(function () {

        var controls = $(getControlCombo());
        var controlItem = $(this).data("item");

        $(this).find(".controlSelectContainer").empty();
        $(this).find(".controlSelectContainer").append(controls);

        var operator = $("<select></select>").addClass("form-control operator");
        if (typeof convertToGui[controlItem.control] !== 'undefined') {
            controls.val(convertToGui[controlItem.control].control);

            for (var key in operatorOptList) {
                var ctrlType = Array.from(operatorOptList[key].control_type);
                if (ctrlType.includes(convertToGui[controlItem.control].control)) {
                    operator.append($("<option></option>").text(operatorOptList[key].label[user.language]).val(operatorOptList[key].value));
                }
            }
            operator.val(convertToGui[controlItem.control].operator);
        }
        $(this).find(".controlOperatorContainer").empty();
        $(this).find(".controlOperatorContainer").append(operator);

        setPlaceholderControl($(this));

        controls.on("change", function () {
            setModif(true);
            $(this).parents(".control").find(".operator").empty();
            for (var key in operatorOptList) {
                var ctrlType = Array.from(operatorOptList[key].control_type);
                if (ctrlType.includes($(this).find(":selected").val())) {
                    $(this).parents(".control").find(".operator").append($("<option></option>").text(operatorOptList[key].label[user.language]).val(operatorOptList[key].value));
                }
            }
            controlItem.control = newControlOptList[$(this).find(":selected").val()][$(this).parents(".control").find(".operator").val()];
            setPlaceholderControl($(this).parents(".control"));
        });

        operator.on("change", function () {
            setModif(true);
            var controlSelect = $(this).parents(".control").find(".controlType");
            var operatorSelect = $(this).find(":selected");
            controlItem.control = newControlOptList[controlSelect.val()][operatorSelect.val()];
            setPlaceholderControl($(this).parents(".control"));
        });

        if ((object.isUsingLibraryStep) || (!object.hasPermissionsUpdate)) {
            controls.prop("disabled", "disabled");
            operator.prop("disabled", "disabled");
        }

        controls.select2({
            minimumResultsForSearch: 20,
            templateSelection: formatActionSelect2Result,
            templateResult: formatActionSelect2Result
        });
    });
}