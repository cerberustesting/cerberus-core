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

// displayStepOptionsModal has been moved to manageStepOptions.html for Alpine.js integration

Step.prototype.setActions = function (actions, canUpdate) {
    //var start =  Date.now();
    for (var i = 0; i < actions.length; i++) {
        this.setAction(actions[i], undefined, canUpdate);
    }
    //var end = Date.now();
    //var elapsed = end-start;
    //console.log("Elapsed : " + elapsed);
};

Step.prototype.setAction = function (action, afterAction, canUpdate) {
    if (action instanceof Action) {
        action.draw(afterAction);
        this.actions.push(action);
    } else {
        var actionObj = new Action(action, this, canUpdate);

        actionObj.draw(afterAction);
        this.actions.push(actionObj);
    }
};

Step.prototype.setDescription = function (description) {
    this.description = description;
    this.textArea.text(description);
    $("#stepDescription").val(description);
};

Step.prototype.setDelete = function () {
    this.toDelete = (this.toDelete) ? false : true;

    if ($("#contentWrapper").hasClass("list-group-item-danger")) {
        $("#contentWrapper").removeClass("list-group-item-danger");
    } else {
        $("#contentWrapper").removeClass("well").addClass("list-group-item-danger well");
    }

    if (this.toDelete) {
        $("#deleteStep span").removeClass();
        $("#deleteStep span").addClass("glyphicon glyphicon-remove");
    } else {
        $("#deleteStep span").removeClass();
        $("#deleteStep span").addClass("glyphicon glyphicon-trash");
    }

    for (var i = 0; i < this.steps.length; i++) {
        var step = this.steps[i];

        if (step.toDelete) {
            step.html.addClass("list-group-item-danger");
            step.html.removeClass("list-group-item-calm");
        } else {
            step.html.addClass("list-group-item-calm");
            step.html.removeClass("list-group-item-danger");
        }
    }
};

Step.prototype.setStepId = function (stepId) {
    this.stepId = stepId;
};

Step.prototype.getStepId = function () {
    return this.stepId;
};

Step.prototype.setSort = function (sort) {
    this.sort = sort;
    this.refreshSort();
};

Step.prototype.refreshSort = function () {
    this.html.find("#labelDiv").empty().text(this.sort);
};

Step.prototype.getJsonData = function () {
    var json = {};

    json.toDelete = this.toDelete;
    json.test = this.test;
    json.testcase = this.testcase;
    json.stepId = this.stepId;
    json.sort = this.sort;
    json.description = this.description;
    json.isUsingLibraryStep = this.isUsingLibraryStep;
    json.libraryStepTest = this.libraryStepTest;
    json.libraryStepTestCase = this.libraryStepTestCase;
    json.libraryStepStepId = this.libraryStepStepId;
    json.isLibraryStep = this.isLibraryStep;
    json.loop = this.loop;
    json.conditionOperator = this.conditionOperator;
    json.conditionValue1 = this.conditionValue1;
    json.conditionValue2 = this.conditionValue2;
    json.conditionValue3 = this.conditionValue3;
    json.conditionOptions = this.conditionOptions;
    json.isExecutionForced = this.isExecutionForced;

    return json;
};

Step.prototype.show = function () {
    var doc = new Doc();
    var object = $(this).data("item");
    $("#addActionBottomBtn").show();

    const url = new URL(window.location);
//    url.hash = '#stepId=' + object.stepId;
//    console.info(ReplaceURLParameters("stepId", object.stepId));
    InsertURLInHistory("./TestCaseScript.jsp?" + ReplaceURLParameters("stepId", object.stepId));
//    window.history.pushState({}, '', url);
    $("#seeLastExecUniq").parent().attr("href", "./TestCaseExecution.jsp?executionId=" + encodeURI(exeId) + "#stepId=" + object.stepId + "-1");

    for (var i = 0; i < object.steps.length; i++) {
        var step = object.steps[i];

        step.stepActionContainer.hide();
        step.stepActionContainer.find("[data-toggle='tooltip']").tooltip("hide");
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

    $(".stepButtonContainer").empty();
    var thisButtonContainer = object.html.find('.stepButtonContainer');
    thisButtonContainer.append('<div class="fieldRow row" id="UseStepRowButton" style="display: none; color: transparent;"></div>');
    thisButtonContainer.append('<div style="margin-right: auto; margin-left: auto;" id="stepButtons">');
    $("#stepButtons").append('<button class="btn btn-default btnPurple" title="Is Use Step" data-toggle="tooltip" id="isUseStep"><span class="glyphicon glyphicon-lock"></span></button>');
    $("#stepButtons").append('<button class="btn btn-default btnLightPurple" title="Is Library" data-toggle="tooltip" id="isLib"><span class="glyphicon glyphicon-book"></span></button>');
    $("#stepButtons").append('<button class="btn add-btn btnLightOrange" id="stepPlus"><span class="glyphicon glyphicon-cog"></span></button>');
    $("#stepButtons").append('<button class="btn add-btn btnLightRed" id="deleteStep"><span class="glyphicon glyphicon-trash"></span></button>');


    $("#stepPlus").click(function () {
        displayStepOptionsModal(object, object.html);
    });

    $("#deleteStep").click(function () {
        if (object.isStepInUseByOtherTestCase) {
            showStepUsesLibraryInConfirmationModal(object);
        } else {
            setModif(true);
            object.setDelete();
        }
    });

    $("#isLib").unbind("click");
    if (object.isLibraryStep) {
        $("#isLib").addClass("btnPurple");
        if (object.isStepInUseByOtherTestCase) {
            $("#isLib").click(function () {

                showStepUsesLibraryInConfirmationModal(object);

            });
        } else {
            $("#isLib").click(changeLib);
        }
    } else {
        $("#isLib").removeClass("btnPurple");
        $("#isLib").click(changeLib);
    }

    if (object.isUsingLibraryStep) {
        $("#isLib").hide();
        $("#UseStepRowButton").html("|").show();
        $("#addAction").prop("disabled", true);
        $("#addActionBottomBtn").hide();
        $("#isUseStep").show();
        $("#stepConditionOption").prop("disabled", true);
    } else {
        $("#isLib").show();
        $("#UseStepRowButton").html("").hide();
        $("#addAction").prop("disabled", false);
        $("#addActionBottomBtn").show();
        $("#isUseStep").hide();
        $("#stepConditionOption").prop("disabled", false);
    }

    if (object.toDelete) {
        $("#contentWrapper").addClass("list-group-item-danger");
    } else {
        $("#contentWrapper").removeClass("list-group-item-danger");
    }

    object.stepActionContainer.show();

    displayActionCombo(object);

    displayControlCombo(object);

    $(object.stepActionContainer).find('input:not(".description")').trigger('settingsButton');

    $(this).find("#stepDescription").unbind("change").change(function () {
        setModif(true);
        object.description = $(this).val();
    });

    $("#isUseStep").unbind("click").click(function () {
        setModif(true);
        if (object.isUsingLibraryStep) {
            showModalConfirmation(function () {
                object.isUsingLibraryStep = false;
                object.libraryStepStepId = -1;
                object.libraryStepTest = "";
                object.libraryStepTestCase = "";
                saveScript();
            }, undefined, doc.getDocLabel("page_testcasescript", "unlink_useStep"), doc.getDocLabel("page_testcasescript", "unlink_useStep_warning"), "", "", "", "");
        }
    });

    //if (object.isExecutionForced) {
    //    $("#stepForceExe").val("true");
    // } else {
    //    $("#stepForceExe").val("false");
    //}
    //$("#stepId").text(object.sort);
    //$("#stepInfo").show();
    //$("#addActionContainer").show();
    //$("#stepHeader").show();
    //setPlaceholderCondition($("#stepConditionOperator").parent().parent(".row"));

    // Disable fields if Permission not allowing.
    // Description and unlink the step with UseStep of the Step can be modified
    // if hasPermitionUpdate is true.
    var activateDisable = !object.hasPermissionsUpdate;
    $("#stepDescription").attr("disabled", activateDisable);
    $("#isUseStep").attr("disabled", activateDisable);
    // Flag the Step as a library if hasPermissionsUpdate and
    // hasPermissionsStepLibrary is true.
    var activateIsLib = !(object.hasPermissionsUpdate && object.hasPermissionsStepLibrary)
    $("#isLib").attr("disabled", activateIsLib);
    // Detail of the Step can be modified if hasPermitionUpdate is true and Step
    // is not a useStep.
    var activateDisableWithUseStep = !(object.hasPermissionsUpdate && !(object.isUsingLibraryStep));
    $("#stepLoop").attr("disabled", activateDisableWithUseStep);
    $("#stepConditionOperator").attr("disabled", activateDisableWithUseStep);
    $("#stepConditionVal1").attr("disabled", activateDisableWithUseStep);
    $("#stepConditionVal2").attr("disabled", activateDisableWithUseStep);
    $("#stepConditionVal3").attr("disabled", activateDisableWithUseStep);
};

Step.prototype.draw = function () {
    var htmlElement = this.html;
    var doc = new Doc();

// DESCRIPTION
    var descContainer = $("<div class='input-group'></div>");
    var descriptionField = $("<input id='stepDescription' class='description form-control crb-autocomplete-variable'>").attr("placeholder", doc.getDocLabel("page_testcasescript", "describe_step")).attr("style", "border:0px; text-overflow: ellipsis;");
    var drag = $("<span></span>").addClass("input-group-addon").addClass("drag-step").attr("style", "font-weight: 700;border-radius:4px;border:1px solid #ccc").prop("draggable", true).text(this.steps.length + 1);
    drag.on("dragstart", handleDragStart);
    drag.on("dragenter", handleDragEnter);
    drag.on("dragover", handleDragOver);
    drag.on("dragleave", handleDragLeave);
    drag.on("drop", handleDrop);
    drag.on("dragend", handleDragEnd);
    descContainer.append(drag).append(descriptionField);

    descriptionField.val(this.description);
    descriptionField.css("width", "100%");
    descriptionField.on("change", function () {
        setModif(true);
        this.description = descriptionField.val();
    });
// END OF DESCRIPTION

// LABEL CONTAINERS
    var stepLabelContainer = $("<div class='col-sm-12 stepLabelContainer' style='padding-left: 0px;margin-top:10px'></div>");
    if (this.isExecutionForced) {
        var labelOptions = $('<span class="label label-primary optionLabel labelLightOrange">Force Execution</span>');
        stepLabelContainer.append(labelOptions[0]);
    }

    if (this.loop !== "onceIfConditionTrue" && this.loop !== "onceIfConditionFalse") {
        var labelOptions = $('<span class="label label-primary optionLabel labelLightGreen">Loop</span>');
        stepLabelContainer.append(labelOptions[0]);
    } else if ((this.conditionOperator !== "never")
        && (this.conditionOperator !== "always")) {


    }
    if ((this.loop === "onceIfConditionTrue" && this.conditionOperator === "never")
        || (this.loop === "onceIfConditionFalse" && this.conditionOperator === "always")) {
        var labelOptions = $('<span class="label label-primary optionLabel labelLightRed">Do not execute</span>');
        stepLabelContainer.append(labelOptions[0]);
    }

    if (this.isLibraryStep) {
        var labelOptions = $('<span class="label label-primary optionLabel labelLightPurple">is Library</span>');
        stepLabelContainer.append(labelOptions[0]);
    }

// END OF LABEL CONTAINERS

// BUTTON CONTAINERS
    var stepButtonContainer = $("<div class='col-sm-12'></div>").append("<div class='stepButtonContainer pull-right' style='padding-left:0px'></div>");
// END OF BUTTON CONTAINERS
    var useStepContainer = $("<div class='col-sm-12 fieldRow row' class='useStepContainer' id='UseStepRow' style='display: none;'></div>");
    if (this.isUsingLibraryStep) {
        //useStepContainer.html("(" + doc.getDocLabel("page_testcasescript", "imported_from") + " <a href='./TestCaseScript.jsp?test=" + encodeURI(this.libraryStepTest) + "&testcase=" + encodeURI(this.libraryStepTestCase) + "&step=" + encodeURI(this.libraryStepSort) + "' >" + this.libraryStepTest + " - " + this.libraryStepTestCase + " - " + this.libraryStepSort + "</a>)").show();
        var labelOptions = $("<span class='label label-primary optionLabel' style='background-color:rgba(114,124,245,.25);color:#727cf5'>" + doc.getDocLabel("page_testcasescript", "imported_from") + " <a href='./TestCaseScript.jsp?test=" + encodeURI(this.libraryStepTest) + "&testcase=" + encodeURI(this.libraryStepTestCase) + "&stepId=" + this.libraryStepStepId + "' >" + this.libraryStepTest + " - " + this.libraryStepTestCase + " - " + this.libraryStepSort + "</a></span>");
        stepLabelContainer.append(labelOptions[0]);

    }

    htmlElement.append($("<div class='col-sm-12' style='padding-left: 0px;'></div>").append($("<div></div>").append(descContainer).append(stepLabelContainer).append(useStepContainer)));
    //htmlElement.append(stepLabelContainer);
    //htmlElement.append(useStepContainer);
    htmlElement.append(stepButtonContainer);
    htmlElement.data("item", this);
    htmlElement.click(this.show);

    $("#steps").append(htmlElement);
    $("#actionContainer").append(this.stepActionContainer);

    $("[name='actionSelect']").select2({
        minimumResultsForSearch: 20,
        templateSelection: formatActionSelect2Result,
        templateResult: formatActionSelect2Result
    });

    this.refreshSort();
};

function createSteps(data, steps, stepIndex, canUpdate, hasPermissionsStepLibrary) {
    // If the testcase has no steps, we create an empty one.
    if (data.length === 0) {
        var step = initStep();
        var stepObj = new Step(step, steps, canUpdate, hasPermissionsStepLibrary);

        stepObj.draw();
        steps.push(stepObj);
//        setModif(true);
    }
    for (var i = 0; i < data.length; i++) {
        var step = data[i];
        var stepObj = new Step(step, steps, canUpdate, hasPermissionsStepLibrary);

        stepObj.draw();
        steps.push(stepObj);
    }

    if (stepIndex !== undefined) {
        var find = false;
        for (var i = 0; i < steps.length; i++) {
            // Use == in stead of ===
            if (steps[i].stepId == stepIndex) {
                find = true;
                $(steps[i].html[0]).click();
            }
        }
        if ((!find) && (steps.length > 0)) {
            $(steps[0].html[0]).click();
        }
    } else if (steps.length > 0) {
        $(steps[0].html[0]).click();
    } else {
        $("#stepHeader").hide();
        $("#addActionBottomBtn").hide();
        $("#addAction").attr("disabled", true);
    }
}

function Step(json, steps, canUpdate, hasPermissionsStepLibrary) {
    this.stepActionContainer = $("<div></div>").addClass("step-container").css("display", "none");
    this.sort = json.sort;
    this.stepId = json.stepId;
    this.description = json.description;
    this.isExecutionForced = json.isExecutionForced;
    this.loop = json.loop;
    this.conditionOperator = json.conditionOperator;
    this.conditionValue1 = json.conditionValue1;
    this.conditionValue2 = json.conditionValue2;
    this.conditionValue3 = json.conditionValue3;
    this.conditionOptions = json.conditionOptions;
    this.isUsingLibraryStep = json.isUsingLibraryStep;
    this.isLibraryStep = json.isLibraryStep;
    this.libraryStepTest = json.libraryStepTest;
    this.libraryStepTestCase = json.libraryStepTestCase;
    this.libraryStepStepId = json.libraryStepStepId;
    this.libraryStepSort = json.libraryStepSort;
    this.test = json.test;
    this.testcase = json.testcase;
    this.isStepInUseByOtherTestCase = json.isStepInUseByOtherTestCase;
    this.actions = [];
    if (canUpdate) {
        // If we can update the testcase we check whether we can still modify
        // following the TestStepLibrary group.
        if (!hasPermissionsStepLibrary && json.isLibraryStep) {
            canUpdate = false;
        }
    }
    this.setActions(json.actions, canUpdate);

    this.steps = steps;
    this.toDelete = false;
    this.hasPermissionsUpdate = canUpdate;
    this.hasPermissionsStepLibrary = hasPermissionsStepLibrary;

    this.html = $("<li style='padding-right:5px'></li>").addClass("list-group-item list-group-item-calm row stepItem").css("margin-left", "0px");
    this.stepNumberDisplay = $("<span></span>").addClass("input-group-addon").addClass("drag-step-step").attr("style", "font-weight: 400;").prop("draggable", true).text(steps.length + 1);
    this.stepDescriptionDisplay = $("<input class='description form-control crb-autocomplete-variable'>").attr("style", "border:0px").val(this.description);
    this.textArea = $("<div class='input-group'></div>").addClass("step-description").append(this.stepNumberDisplay).append(this.stepDescriptionDisplay);

}

function showStepUsesLibraryInConfirmationModal(object) {
    var doc = new Doc();
    $("#confirmationModal [name='buttonConfirm']").text("OK");
    $("#confirmationModal [name='buttonDismiss']").hide();
    $("#confirmationModal").on("hidden.bs.modal", function () {
        $("#confirmationModal [name='buttonConfirm']").text(doc.getDocLabel("page_global", "buttonConfirm"));
        $("#confirmationModal [name='buttonDismiss']").show();
        $("#confirmationModal").unbind("hidden.bs.modal");
    });

    $.ajax({
        url: "ReadTestCaseStep",
        dataType: "json",
        data: {
            test: object.test,
            testcase: object.testcase,
            stepId: object.stepId,
            getUses: true
        },
        success: function (data) {
            var content = "";
            for (var i = 0; i < data.step.length; i++) {
                content += "<a target='_blank' href='./TestCaseScript.jsp?test=" + encodeURI(data.step[i].test) + "&testcase=" + encodeURI(data.step[i].testcase) + "&stepId=" + encodeURI(data.step[i].stepId) + "'>" + data.step[i].test + " - " + data.step[i].testcase + " - " + data.step[i].testcaseObj.description+ " [Step : " + data.step[i].sort + " " + data.step[i].description + "]</a><br/>";
            }
            $("#confirmationModal #otherStepThatUseIt").empty().append(content);
        }
    });
    showModalConfirmation(function () {
        $('#confirmationModal').modal('hide');
    }, undefined, doc.getDocLabel("page_global", "warning"),
        doc.getDocLabel("page_testcasescript", "cant_detach_library") +
        "<br/>" +
        "<div id='otherStepThatUseIt' style='width:100%;'>" +
        "<div style='width:30px; margin-left: auto; margin-right: auto;'>" +
        "<span class='glyphicon glyphicon-refresh spin'></span>" +
        "</div>" +
        "</div>", "", "", "", "");
}

function loadLibraryStep(search, system) {
    var search_lower = "";
    if (search !== undefined) {
        search_lower = search.toLowerCase();
    }
    $("#lib").empty();
    showLoaderInModal("#addStepModal");
    $.ajax({
        url: "GetStepInLibrary",
        data: {system: system},
        async: true,
        success: function (data) {
            var test = {};

            for (var index = 0; index < data.testCaseSteps.length; index++) {
                var step = data.testCaseSteps[index];

                if (search === undefined || search === "" || step.description.toLowerCase().indexOf(search_lower) > -1 || step.testCase.toLowerCase().indexOf(search_lower) > -1 || step.test.toLowerCase().indexOf(search_lower) > -1) {
                    if (!test.hasOwnProperty(step.test)) {

                        $("#lib").append(`
                            <details class="my-1">
                                <summary class="cursor-pointer flex justify-between items-center py-2">
                                    <span>${step.test}</span>
                                    <span class="transition-transform"> ▶ </span>
                                </summary>
                                <div class="pl-4 list-group" data-test="${step.test}">
                                </div>
                            </details>
                        `);

                        var listGr = $(`[data-test='${step.test}']`);

                        test[step.test] = {content: listGr,testCase: {}};
                    }



                    if (!test[step.test].testCase.hasOwnProperty(step.testCase)) {

                        var listGr = test[step.test].content;

                        listGr.append(`
                            <details class="border-l ml-4 my-1">
                                <summary class="cursor-pointer flex justify-between items-center py-1">
                                    <span>${step.testCase} - ${step.tcdesc}</span>
                                    <span class="transition-transform open:rotate-90"> ▶ </span>
                                </summary>
                        
                                <div class="pl-4 list-group"
                                     data-test="${step.test}"
                                     data-testCase="${step.testCase}">
                                </div>
                            </details>
                        `);

                        var listCaseGr = $(`[data-test='${step.test}'][data-testCase='${step.testCase}']`);

                        test[step.test].testCase[step.testCase] = {content: listCaseGr,step: {}};
                    }
                    var listCaseGrp = test[step.test].testCase[step.testCase].content;
                    var listStepGrp = $("<a></a>").addClass("list-group-item sub-sub-item").attr("href", "#").text(step.description).data("stepInfo", step);
                    listStepGrp.attr("onclick", "javascript:showImportStepDetail($(this))");
                    listCaseGrp.append(listStepGrp);
                    test[step.test].testCase[step.testCase].step[step.description] = listStepGrp;
                }
            }

            if (search !== undefined && search !== "") {
                $('#lib').find("div").toggleClass('in');
            }

            $('#addStepModal > .list-group-item').unbind("click").on('click', function () {
                $('.glyphicon', this)
                    .toggleClass('glyphicon-chevron-right')
                    .toggleClass('glyphicon-chevron-down');
            });

            $("#addStepModal #search").unbind("input").on("input", function (e) {
                var search = $(this).val();
                // Clear any previously set timer before setting a fresh one
                window.clearTimeout($(this).data("timeout"));
                $(this).data("timeout", setTimeout(function () {
                    loadLibraryStep(search, system);
                }, 500));
            });

            hideLoaderInModal("#addStepModal");
        }
    });
}

function addStep(event) {
    var steps = event.data.steps;
    window.dispatchEvent(new CustomEvent('addstepmodal-open', { detail: {} }));

    // Setting the focus on the Description of the step.
    $('#addStepModal').on('shown.bs.modal', function () {
        $('#addStepModal #description').focus();
    });

    $("#addStepConfirm").unbind("click").click(function (event) {
        setModif(true);

        if ($("[name='importInfo']").length === 0) { // added a new step
            var step = initStep();
            step.description = $("#addStepModal #description").val();
            var stepObj = new Step(step, steps, true);
            stepObj.draw();
            steps.push(stepObj);
            stepObj.html.trigger("click");
        } else {
            // added a library step
            $("[name='importInfo']").each(function (idx, importInfo) {
                var step = initStep();
                if ($(importInfo).data("stepInfo")) {
                    var useStep = $(importInfo).data("stepInfo");

                    step.description = useStep.description;

                    $.ajax({
                        url: "ReadTestCaseStep",
                        data: {test: useStep.test, testcase: useStep.testCase, stepId: useStep.step},
                        async: false,
                        success: function (data) {
                            step.actions = data.step.actions;
                            step.conditionOperator = data.step.conditionOperator;
                            step.conditionValue1 = data.step.conditionValue1;
                            step.conditionValue2 = data.step.conditionValue2;
                            step.conditionValue3 = data.step.conditionValue3;
                            step.conditionOptions = data.step.conditionOptions;
                            step.loop = data.step.loop;
                            sortStep(step);
                        }
                    });
                    if ($("#" + generateImportInfoId(useStep)).find("[name='useStep']").prop("checked")) {
                        step.isUsingLibraryStep = true;
                        step.libraryStepTest = useStep.test;
                        step.libraryStepTestCase = useStep.testCase;
                        step.libraryStepStepId = useStep.step;
                        step.libraryStepSort = useStep.sort;
                    }
                }
                var stepObj = new Step(step, steps, true);

                stepObj.draw();
                steps.push(stepObj);
                stepObj.html.trigger("click");
            });
        }
    });
}

function duplicateStep(event) {
    var steps = event.data.steps;

    var step = initStep();
    var step = steps[0];
    step.description = "New Step";
    var stepObj = new Step(step, steps, true);
//    console.info(stepObj);
    stepObj.draw();
    steps.push(stepObj);
    stepObj.html.trigger("click");

}

function initStep() {
    return {
        "isLibraryStep": false,
        "objType": "step",
        "libraryStepTest": "",
        "libraryStepTestCase": "",
        "isUsingLibraryStep": false,
        "description": "",
        "libraryStepStepId": -1,
        "actions": [],
        "loop": "onceIfConditionTrue",
        "conditionOperator": "always",
        "conditionValue1": "",
        "conditionValue2": "",
        "conditionValue3": "",
        "conditionOptions": [],
        "isExecutionForced": false
    };
}

function showImportStepDetail(element) {

    var stepInfo = $(element).data("stepInfo");

    if ($(element).hasClass("selected")) {
        $(element).removeClass("selected");
        $(element).find("[name='idx']").remove();
        $("#" + generateImportInfoId(stepInfo)).remove();
    } else {
        importInfoIdx++;
        $(element).addClass("selected");
        $(element).append('<span class="badge" name="idx">' + importInfoIdx + ' </span>');
        var importInfoId = generateImportInfoId(stepInfo);

        var importInfo =
            '<div id="' + importInfoId + '" class="grid gap-1 p-3 mb-2 bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-lg text-sm" style="grid-template-columns: 1fr auto; align-items: center;">' +
            '   <div class="flex items-center gap-2 min-w-0">' +
            '       <span class="inline-flex items-center justify-center h-5 min-w-[1.25rem] px-1.5 rounded-full text-[11px] font-semibold text-white bg-blue-500 shrink-0">' + importInfoIdx + '</span>' +
            '       <span class="truncate font-medium text-slate-800 dark:text-slate-200">' + stepInfo.description + '</span>' +
            '   </div>' +
            '   <div class="flex items-center gap-1.5 shrink-0">' +
            '       <input type="checkbox" name="useStep" checked class="h-4 w-4 rounded border-slate-300 dark:border-slate-600 text-blue-600 focus:ring-blue-500 cursor-pointer">' +
            '       <span class="text-xs text-slate-500 dark:text-slate-400 whitespace-nowrap">Use Step</span>' +
            '   </div>' +
            '   <div name="importInfo" class="text-[11px] text-slate-400 dark:text-slate-500 truncate" style="grid-column: 1 / -1;"></div>' +
            '</div>';

        $("#importDetail").append(importInfo);
        $("#" + importInfoId).find("[name='importInfo']").text("Imported from " + stepInfo.test + " - " + stepInfo.testCase + " - " + stepInfo.sort + ")").data("stepInfo", stepInfo);

        $("#importDetail[name='useStep']").prop("checked", true);

        $("#importDetail").show();
    }
}

function sortStep(step) {
    for (var j = 0; j < step.actions.length; j++) {
        var action = step.actions[j];

        action.controls.sort(function (a, b) {
            return a.sort - b.sort;
        });
    }

    step.actions.sort(function (a, b) {
        return a.sort - b.sort;
    });
}