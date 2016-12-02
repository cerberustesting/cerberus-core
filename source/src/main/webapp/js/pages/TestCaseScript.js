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
        var stepList = [];

        initPageModal("testCaseScript");

        // Load invariant list into local storage.
        getSelectInvariant("ACTION", false, true);
        getSelectInvariant("CONTROL", false, true);
        getSelectInvariant("CTRLFATAL", false, true);
        getSelectInvariant("PROPERTYTYPE", false, true);
        getSelectInvariant("PROPERTYDATABASE", false, true);
        getSelectInvariant("PROPERTYNATURE", false, true);
        getSelectInvariant("ACTIONFORCEEXESTATUS", false, true);

        loadLibraryStep();
        bindToggleCollapse();

        var test = GetURLParameter("test");
        var testcase = GetURLParameter("testcase");
        var step = GetURLParameter("step");

        displayHeaderLabel(doc);
        displayGlobalLabel(doc);
        displayFooter(doc);
        
        displayInvariantList("group", "GROUP", false, true);
        displayInvariantList("status", "TCSTATUS", false, true);
        displayInvariantList("priority", "PRIORITY", false, true);
        $('[name="origin"]').append('<option value="All">All</option>');
        displayInvariantList("origin", "ORIGIN", false, true);
        displayInvariantList("active", "TCACTIVE", false, true);
        displayInvariantList("activeQA", "TCACTIVE", false, true);
        displayInvariantList("activeUAT", "TCACTIVE", false, true);
        displayInvariantList("activeProd", "TCACTIVE", false, true);
        displayApplicationList("application", getUser().defaultSystem);
        displayProjectList("project");
        tinymce.init({
            selector: ".wysiwyg"
        });

        // Edit TestCase open the TestCase Modal
        $("#editTcInfo").click(function () {
            editTestCaseClick(test, testcase);
        });

        $("#saveStep").click(saveStep);
        $("#isLib").click(changeLib);
        $("#cancelEdit").click(cancelEdit);

        var json;
        var testcaseinfo;
        var Tags;
        $.ajax({
            url: "ReadTestCase",
            data: {test: test, testCase: testcase, withStep: true},
            dataType: "json",
            success: function (data) {

                testcaseinfo = data.info;
                loadTestCaseInfo(data.info);
                json = data.stepList;
                sortData(json);
                createStepList(json, stepList, step);
                drawInheritedProperty(data.inheritedProp);
                listenEnterKeypressWhenFocusingOnDescription();
                setPlaceholderAction();
                setPlaceholderControl();

                var availableProperties = loadProperties(test, testcase, data.info);
                var availableObjects = loadApplicationObject(data);
                var availableObjectProperties = [
                    "value",
                    "picturepath",
                    "pictureurl"
                ]
                var availableTags = [
                    "property",
                    "object"
                ];

                Tags = [
                    {
                        array : availableObjectProperties,
                        regex : "%object\\.[^\\.]*\\.",
                        addBefore : "",
                        addAfter : "%",
                        isCreatable : false
                    },
                    {
                        array : availableObjects,
                        regex : "%object\\.",
                        addBefore : "",
                        addAfter : ".",
                        isCreatable : true
                    },
                    {
                        array : availableProperties,
                        regex : "%property\\.",
                        addBefore : "",
                        addAfter : "%",
                        isCreatable : true
                    },
                    {
                        array : availableTags,
                        regex : "%",
                        addBefore : "",
                        addAfter : ".",
                        isCreatable : false
                    }
                ];

                autocompleteAllFields(Tags, data.info, test, testcase);

                // Building full list of country from testcase.
                var myCountry = [];
                $.each(testcaseinfo.countryList, function (index) {
                    myCountry.push(index);
                });

                // Button Add Property insert a new Property
                $("#addProperty").click(function () {
                    var newProperty = {
                        property: "",
                        description: "",
                        country: myCountry,
                        type: "text",
                        database: "",
                        value1: "",
                        value2: "",
                        length: 0,
                        rowLimit: 0,
                        nature: "STATIC",
                        toDelete: false
                    };

                    drawProperty(newProperty, testcaseinfo);

                });

                $('[data-toggle="tooltip"]').tooltip();

            },
            error: showUnexpectedError
        });


        $("#manageProp").click(function(){
            editPropertiesModalClick(test,testcase,testcaseinfo);
        });

        $("#propertiesModal [name='buttonSave']").click(editPropertiesModalSaveHandler);

        $("#addStep").click({stepList: stepList}, addStep);
        $('#addStepModal').on('hidden.bs.modal', function () {
            $("#importInfo").removeData("stepInfo");
            $("#importInfo").empty();
            $("#addStepModal #description").val("");
            $("#useStep").prop("checked", false);
            $("#importDetail").hide();
        });

        $("#deleteStep").click(function () {
            var step = $("#stepList .active").data("item");

            step.setDelete();
        });

        $("#editBtn").click(editStep);
        $("#addAction").click(function () {
            $.when(addAction()).then(function (action) {
                listenEnterKeypressWhenFocusingOnDescription();
                $($(action.html[0]).find(".description")[0]).focus();
                autocompleteAllFields();
                setPlaceholderAction();
            });
        });
        $("#saveScript").click(saveScript);
        $("#runTestCase").click(function () {
            runTestCase(test, testcase);
        });
        $("#seeLastExec").click(function () {
            seeLastExec(test, testcase);
        });
        $("#seeLogs").click(function () {
            seeLogs(test, testcase);
        });
        $.ajax({
            url: "ReadTestCaseExecution",
            data: {test: test, testCase: testcase},
            dataType: "json",
            success: function (data) {
                $("#rerunTestCase").click(function () {
                    rerunTestCase(test, testcase, data.contentTable.country, data.contentTable.env);
                });
                $("#rerunTestCase").attr("title","Last Execution was " + data.contentTable.controlStatus + " in " + data.contentTable.env + " in " + data.contentTable.country + " on " + data.contentTable.end)
            },
            error: showUnexpectedError
        });
        var height = $("nav.navbar.navbar-inverse.navbar-static-top").outerHeight(true) + $("div.alert.alert-warning").outerHeight(true) + $(".page-title-line").outerHeight(true) - 10;

        $("#testCaseTitle").affix({offset: {top: height} });
        $("#list-wrapper").affix({offset: {top: height} });

        var wrap = $(window);

        wrap.on("scroll", function(e) {
            if($("#testCaseTitle").width() != $("#testCaseTitle").parent().width()-30) {
                $("#testCaseTitle").width($("#testCaseTitle").parent().width() - 30);
                $("#list-wrapper").width($("#nav-execution").width());
            }
        });

        wrap.resize(function(e){
            if($("#testCaseTitle").width() != $("#testCaseTitle").parent().width()-30) {
                $("#testCaseTitle").width($("#testCaseTitle").parent().width() - 30);
                $("#list-wrapper").width($("#nav-execution").width());
            }
        })
    });
});

function addAction() {
    var step = $("#stepList li.active").data("item");
    var action = new Action(null, step);
    step.setAction(action);
    return action;
}

function runTestCase(test, testcase) {
    window.location.href = "./RunTests1.jsp?test=" + test + "&testcase=" + testcase;
}
function getTestCase(test, testcase, step) {
    window.location.href = "./TestCaseScript.jsp?test=" + test + "&testcase=" + testcase + "&step=" + step;
}
function seeLogs(test, testcase) {
    window.location.href = "./LogViewer.jsp?Test=" + test + "&TestCase=" + testcase;
}
function seeLastExec(test, testcase) {
    window.location.href = "./ExecutionDetailList.jsp?test=" + test + "&testcase=" + testcase;
}
function rerunTestCase(test, testcase, country, environment) {
    window.location.href = "./RunTests1.jsp?test=" + test + "&testcase=" + testcase + "&country=" + country + "&environment=" + environment;
}

function saveScript() {
    var stepList = $("#stepList li");
    var stepArr = [];

    // Construct the step/action/control list:
    // Iterate over steps
    for (var i = 0; i < stepList.length; i++) {
        var step = $(stepList[i]).data("item");
        var actionArr = [];

        if (!step.toDelete) {
            // Set the step's sort
            step.setSort(i + 1);

            // Get step's actions
            var actionList = step.stepActionContainer.children(".action-group").children(".action");

            // Iterate over actions
            for (var j = 0; j < actionList.length; j++) {
                var action = $(actionList[j]).data("item");
                var controlArr = [];

                if (!action.toDelete) {
                    // Set the action's sort
                    action.setSort(j + 1);

                    // Get action's controls
                    var controlList = action.html.children(".control");

                    // Iterate over controls
                    for (var k = 0; k < controlList.length; k++) {
                        var control = $(controlList[k]).data("item");

                        if (!control.toDelete) {
                            // Set the control's sort
                            control.setSort(k + 1);

                            // Then push control into result array
                            controlArr.push(control.getJsonData());
                        }
                    }
                }
                var actionJson = action.getJsonData();
                actionJson.controlArr = controlArr;
                actionArr.push(actionJson);
            }
            var stepJson = step.getJsonData();
            stepJson.actionArr = actionArr;
            stepArr.push(stepJson);
        }
    }

    var properties = $("[name='masterProp']");
    var propArr = [];
    for (var i = 0; i < properties.length; i++) {
        propArr.push($(properties[i]).data("property"));
    }

    $.ajax({
        url: "UpdateTestCaseWithDependencies1",
        async: true,
        method: "POST",
        data: {informationInitialTest: GetURLParameter("test"),
            informationInitialTestCase: GetURLParameter("testcase"),
            informationTest: GetURLParameter("test"),
            informationTestCase: GetURLParameter("testcase"),
            stepArray: JSON.stringify(stepArr),
            propArr: JSON.stringify(propArr)},
        success: function () {
            location.reload();
        },
        error: showUnexpectedError
    });
}

function drawProperty(property, testcaseinfo) {
    var selectType = getSelectInvariant("PROPERTYTYPE", false, true);
    var selectDB = getSelectInvariant("PROPERTYDATABASE", false, true);
    var selectNature = getSelectInvariant("PROPERTYNATURE", false, true);
    var deleteBtn = $("<button class='col-lg-6 btn btn-danger btn-sm'></button>").append($("<span></span>").addClass("glyphicon glyphicon-trash"));
    var moreBtn = $("<button class='col-lg-6 btn btn-default btn-sm'></button>").append($("<span></span>").addClass("glyphicon glyphicon-chevron-down"));

    var propertyInput = $("<input onkeypress='return restrictCharacters(this, event, propertyNameRestriction);' id='propName' placeholder='Feed Property name'>").addClass("form-control input-sm").val(property.property);
    var descriptionInput = $("<textarea rows='1' id='propDescription' placeholder='Feed Property description'>").addClass("form-control input-sm").val(property.description);
    var valueInput = $("<textarea rows='1' placeholder='Value'></textarea>").addClass("form-control input-sm").val(property.value1);
    var lengthInput = $("<input placeholder='Length'>").addClass("form-control input-sm").val(property.length);
    var rowLimitInput = $("<input placeholder='Row Limit'>").addClass("form-control input-sm").val(property.rowLimit);
    var table = $("#propTable");

    var content = $("<div class='row property list-group-item'></div>");
    var props = $("<div class='col-sm-11'></div>");
    var right = $("<div class='col-sm-1 propertyButtons'></div>");

    var row1 = $("<div class='row' name='masterProp' style='margin-top:10px;'></div>");
    var row2 = $("<div class='row' style='display:none;'></div>");
    var row3 = $("<div class='row' style='display:none;'></div>");
    var row4 = $("<div class='row'></div>");
    var row5 = $("<div class='row'></div>");
    var propertyName = $("<div class='col-sm-2 form-group'></div>").append($("<label></label>").text("Property: ")).append(propertyInput);
    var description = $("<div class='col-sm-4 form-group'></div>").append($("<label></label>").text("Description: ")).append(descriptionInput);
    var country = $("<div class='col-sm-10 form-group'></div>").append(getTestCaseCountry(testcaseinfo.countryList, property.country));
    var type = $("<div class='col-sm-2 form-group'></div>").append($("<label></label>").text("Type: ")).append(selectType.val(property.type));
    var db = $("<div class='col-sm-2 form-group'></div>").append($("<label></label>").text("DB: ")).append(selectDB.val(property.database));
    var value = $("<div class='col-sm-8 form-group'></div>").append($("<label></label>").text("Value: ")).append(valueInput);
    var length = $("<div class='col-sm-2 form-group'></div>").append($("<label></label>").text("Length: ")).append(lengthInput);
    var rowLimit = $("<div class='col-sm-2 form-group'></div>").append($("<label></label>").text("Row Limit: ")).append(rowLimitInput);
    var nature = $("<div class='col-sm-2 form-group'></div>").append($("<label></label>").text("Nature: ")).append(selectNature.val(property.nature));


    var selectAllBtn = $("<button></button>").addClass("btn btn-default btn-sm").append($("<span></span>").addClass("glyphicon glyphicon-check")).click(function(){country.find("input[type='checkbox']").prop('checked', true).trigger("change");});
    var selectNoneBtn = $("<button></button>").addClass("btn btn-default btn-sm").append($("<span></span>").addClass("glyphicon glyphicon-unchecked")).click(function(){country.find("input[type='checkbox']").prop('checked', false).trigger("change");});
    var btnRow = $("<div class='col-sm-2 form-group'></div>").append(selectAllBtn).append(selectNoneBtn);

    deleteBtn.click(function () {
        property.toDelete = (property.toDelete) ? false : true;

        if (property.toDelete) {
            content.addClass("list-group-item-danger");
        } else {
            content.removeClass("list-group-item-danger");
        }
    });

    moreBtn.click(function(){
        if($(this).find("span").hasClass("glyphicon-chevron-down")){
            $(this).find("span").removeClass("glyphicon-chevron-down");
            $(this).find("span").addClass("glyphicon-chevron-up");
        }else{
            $(this).find("span").removeClass("glyphicon-chevron-up");
            $(this).find("span").addClass("glyphicon-chevron-down");
        }
        $(this).parent().parent().find(".row:not([name='masterProp'])").toggle();
    });

    propertyInput.change(function () {
        property.property = $(this).val();
    });

    descriptionInput.change(function () {
        property.description = $(this).val();
    });

    selectType.change(function () {
        property.type = $(this).val();
    });

    selectDB.change(function () {
        property.database = $(this).val();
    });

    valueInput.change(function () {
        property.value1 = $(this).val();
    });

    lengthInput.change(function () {
        property.length = $(this).val();
    });

    rowLimitInput.change(function () {
        property.rowLimit = $(this).val();
    });

    selectNature.change(function () {
        property.nature = $(this).val();
    });

    row1.data("property", property);
    row1.append(propertyName);
    row1.append(type);
    row1.append(value);
    props.append(row1);

    row2.append(db);
    row2.append(length);
    row2.append(rowLimit);
    row2.append(nature);
    row2.append(description);
    props.append(row2);

    row3.append(btnRow);
    row3.append(country);
    props.append(row3);

    right.append(moreBtn).append(deleteBtn);

    content.append(props).append(right);
    table.append(content);
}

function drawInheritedProperty(propList) {
    var selectType = getSelectInvariant("PROPERTYTYPE", false, true).attr("disabled",true);
    var selectDB = getSelectInvariant("PROPERTYDATABASE", false, true).attr("disabled",true);
    var selectNature = getSelectInvariant("PROPERTYNATURE", false, true).attr("disabled",true);
    var table = $("#inheritedPropPanel");

    for (var index = 0; index < propList.length; index++) {
        var property = propList[index];

        var moreBtn = $("<button class='col-sm-12 btn btn-default btn-sm' style='margin-top:32px;'></button>").append($("<span></span>").addClass("glyphicon glyphicon-chevron-down"));

        var propertyInput = $("<input id='propName' placeholder='Feed Property name' disabled>").addClass("form-control input-sm").val(property.property);
        var descriptionInput = $("<textarea rows='1' id='propDescription' placeholder='Feed Property description' disabled>").addClass("form-control input-sm").val(property.description);
        var valueInput = $("<textarea rows='1' placeholder='Value' disabled></textarea>").addClass("form-control input-sm").val(property.value1);
        var lengthInput = $("<input placeholder='Length' disabled>").addClass("form-control input-sm").val(property.length);
        var rowLimitInput = $("<input placeholder='Row Limit' disabled>").addClass("form-control input-sm").val(property.rowLimit);

        var content = $("<div class='row property list-group-item disabled'></div>");
        var props = $("<div class='col-sm-11'></div>");
        var right = $("<div class='col-sm-1'></div>");

        var row1 = $("<div class='row' name='masterProp' style='margin-top:10px;'></div>");
        var row2 = $("<div class='row' style='display:none;'></div>");
        var row3 = $("<div class='row' style='display:none;'></div>");
        var row4 = $("<div class='row'></div>");
        var row5 = $("<div class='row'></div>");
        var propertyName = $("<div class='col-sm-2 form-group'></div>").append($("<label></label>").text("Property: ")).append(propertyInput);
        var description = $("<div class='col-sm-4 form-group'></div>").append($("<label></label>").text("Description: ")).append(descriptionInput);
        var country = $("<div class='col-sm-10 form-group'></div>").append(getTestCaseCountry(property.country, property.country, true));
        var type = $("<div class='col-sm-2 form-group'></div>").append($("<label></label>").text("Type: ")).append(selectType.clone().val(property.type));
        var db = $("<div class='col-sm-2 form-group'></div>").append($("<label></label>").text("DB: ")).append(selectDB.clone().val(property.database));
        var value = $("<div class='col-sm-8 form-group'></div>").append($("<label></label>").text("Value: ")).append(valueInput);
        var length = $("<div class='col-sm-2 form-group'></div>").append($("<label></label>").text("Length: ")).append(lengthInput);
        var rowLimit = $("<div class='col-sm-2 form-group'></div>").append($("<label></label>").text("Row Limit: ")).append(rowLimitInput);
        var nature = $("<div class='col-sm-2 form-group'></div>").append($("<label></label>").text("Nature: ")).append(selectNature.clone().val(property.nature));


        var selectAllBtn = $("<button disabled></button>").addClass("btn btn-default btn-sm").append($("<span></span>").addClass("glyphicon glyphicon-check")).click(function(){country.find("input[type='checkbox']").prop('checked', true);});
        var selectNoneBtn = $("<button disabled></button>").addClass("btn btn-default btn-sm").append($("<span></span>").addClass("glyphicon glyphicon-unchecked")).click(function(){country.find("input[type='checkbox']").prop('checked', false);});
        var btnRow = $("<div class='col-sm-2 form-group'></div>").append(selectAllBtn).append(selectNoneBtn);

        moreBtn.click(function(){
            if($(this).find("span").hasClass("glyphicon-chevron-down")){
                $(this).find("span").removeClass("glyphicon-chevron-down");
                $(this).find("span").addClass("glyphicon-chevron-up");
            }else{
                $(this).find("span").removeClass("glyphicon-chevron-up");
                $(this).find("span").addClass("glyphicon-chevron-down");
            }
            $(this).parent().parent().find(".row:not([name='masterProp'])").toggle();
        });

        row1.data("property", property);
        row1.append(propertyName);
        row1.append(type);
        row1.append(value);
        props.append(row1);

        row2.append(db);
        row2.append(length);
        row2.append(rowLimit);
        row2.append(nature);
        row2.append(description);
        props.append(row2);

        row3.append(btnRow);
        row3.append(country);
        props.append(row3);

        right.append(moreBtn);

        content.append(props).append(right);
        table.append(content);
    }
}

function loadProperties(test, testcase, testcaseinfo) {
    var array = [];
    $.ajax({
        url: "GetPropertiesForTestCase",
        data: {test: test, testcase: testcase},
        async: true,
        success: function (data) {

            for (var index = 0; index < data.length; index++) {
                var property = data[index];
                array.push(data[index].property);
                property.toDelete = false;
                drawProperty(property, testcaseinfo);
            }

            sortProperties("#propTable");
        },
        error: showUnexpectedError
    });
    return array;
}

function sortProperties(identifier){
    var container = $(identifier);
    var list = container.children(".property");
    list.sort(function(a,b){

        var aProp = $(a).find("[name='masterProp']").data("property").property.toLowerCase(),
            bProp = $(b).find("[name='masterProp']").data("property").property.toLowerCase();

        if(aProp > bProp) {
            return 1;
        }
        if(aProp < bProp) {
            return -1;
        }
        return 0;
    });
    container.append(list);
}

function getTestCaseCountry(countryList, countryToCheck, isDisabled) {
    var html = [];
    var cpt = 0;
    var div = $("<div></div>").addClass("checkbox");

    $.each(countryList, function (index) {
        var country;

        if (typeof index === "number") {
            country = countryList[index];
        } else if (typeof index === "string") {
            country = index;
        }
        var input = $("<input>").attr("type", "checkbox").attr("name", country);

        if ((countryToCheck.indexOf(country) !== -1)) {
            input.prop("checked", true).trigger("change");
        }
        if (isDisabled) {
            input.prop("disabled", "disabled");
        } else {
            input.change(function () {
                var country = $(this).prop("name");
                var checked = $(this).prop("checked");
                var index = countryToCheck.indexOf(country);

                if (checked && index === -1) {
                    countryToCheck.push(country);
                } else if (!checked && index !== -1) {
                    countryToCheck.splice(index, 1);
                }
            });
        }

        div.append($("<label></label>").addClass("checkbox-inline")
                .append(input)
                .append(country));

        cpt++;
//        if (cpt % 10 === 0) {
//            div = $("<div></div>").addClass("checkbox");
//        }
        html.push(div);
    });

    return html;
}

function loadTestCaseInfo(info) {
    $(".testTestCase #test").text(info.test);
    $.ajax({
        url: "ReadTestCase?test=" + info.test,
        async: true,
        success: function (data) {
            data.contentTable.sort(function (a, b){
                var aa = a.testCase.toLowerCase();
                var bb = b.testCase.toLowerCase();
                if(aa > bb) {
                    return 1;
                } else if (aa < bb) {
                    return -1;
                }
                return 0;
            });
            for(var i = 0; i<data.contentTable.length; i++){
                $("#testCaseSelect").append("<option value='" + data.contentTable[i].testCase + "'>" + data.contentTable[i].testCase + " - " + data.contentTable[i].description + "</option>")
            }
            $("#testCaseSelect option[value='" + info.testCase + "']").prop('selected', true);
            $("#testCaseSelect").bind("change",function(event){
                window.location.href = "./TestCaseScript.jsp?test=" + info.test + "&testcase=" + $(this).val();
            });
            $("#testCaseSelect").select2({ width: '100%' });
        }
    });
    $(".testTestCase #description").text(info.shortDescription);
}

function cancelEdit() {
    $("#editStep").hide();
    $("#editStepDescription").val("");
    $("#stepDescription").show();
    $("#stepInfo").show();
}

function changeLib() {
    var stepHtml = $("#stepList li.active");
    var stepData = stepHtml.data("item");
    if(stepData.inLibrary == "Y"){
        stepData.inLibrary = "N";
        $(this).removeClass("btn-warning");
        $(this).addClass("btn-default");
    }else{
        stepData.inLibrary = "Y";
        $(this).removeClass("btn-default");
        $(this).addClass("btn-warning");
    }
}

function editStep() {
    var step = $("#stepList li.active").data("item");

    $("#stepDescription").hide();
    $("#stepInfo").hide();
    $("#editStepDescription").prop("placeholder", "Description").prop("maxlength", "150").val(step.description);
    $("#editStep").show();

}

function saveStep() {
    var stepHtml = $("#stepList li.active");
    var stepData = stepHtml.data("item");

    stepData.setDescription($("#editStepDescription").val());

    cancelEdit();
}

function addStep(event) {
    var stepList = event.data.stepList;
    $("#addStepModal").modal('show');

    // Setting the focus on the Description of the step.
    $('#addStepModal').on('shown.bs.modal', function () {
        $('#description').focus();
    })

    $(".sub-sub-item").click(function () {
        var stepInfo = $(this).data("stepInfo");

        $("#importInfo").text("Imported from " + stepInfo.test + " - " + stepInfo.testCase + " - " + stepInfo.sort + ")").data("stepInfo", stepInfo);
        $("#addStepModal #description").val(stepInfo.description);
        $("#useStep").prop("checked", true);

        $("#importDetail").show();
    });

    $("#addStepConfirm").unbind("click").click(function (event) {
        var step = {"inLibrary": "N",
            "objType": "step",
            "useStepTest": "",
            "useStepTestCase": "",
            "useStep": "N",
            "description": "",
            "useStepStep": -1,
            "actionList": []};

        step.description = $("#addStepModal #description").val();
        if ($("#importInfo").data("stepInfo")) {
            var useStep = $("#importInfo").data("stepInfo");
            $.ajax({
                url: "ReadTestCaseStep",
                data: {test: useStep.test, testcase: useStep.testCase, step: useStep.step},
                async: false,
                success: function (data) {
                    step.actionList = data.tcsActionList;

                    for (var index = 0; index < data.tcsActionControlList.length; index++) {
                        var control = data.tcsActionControlList[index];

                        step.actionList[control.sequence - 1].controlList.push(control);
                    }
                    sortStep(step);
                }
            });
            if ($("#useStep").prop("checked")) {
                step.useStep = "Y";
                step.useStepTest = useStep.test;
                step.useStepTestCase = useStep.testCase;
                step.useStepStep = useStep.step;
                step.useStepStepSort = useStep.sort;
            }
        }
        var stepObj = new Step(step, stepList);

        stepObj.draw();
        stepList.push(stepObj);
    });
}

function createStepList(data, stepList, stepIndex) {
    for (var i = 0; i < data.length; i++) {
        var step = data[i];
        var stepObj = new Step(step, stepList);

        stepObj.draw();
        stepList.push(stepObj);
    }

    if(stepIndex != undefined){
        var find = false;
        for(var i = 0; i < stepList.length; i++) {
            if(stepList[i].sort == stepIndex) {
                find = true;
                $(stepList[i].html[0]).click();
            }
        }
        if(!find){
            $(stepList[0].html[0]).click();
        }
    }else if (stepList.length > 0) {
        $(stepList[0].html[0]).click();
    }
}

/** LIBRARY STEP UTILY FUNCTIONS **/

function loadLibraryStep() {
    $.ajax({
        url: "GetStepInLibrary",
        data: {system: getUser().defaultSystem},
        async: true,
        success: function (data) {
            var test = {};

            for (var index = 0; index < data.testCaseStepList.length; index++) {
                var step = data.testCaseStepList[index];

                if (!test.hasOwnProperty(step.test)) {
                    $("#lib").append($("<a></a>").addClass("list-group-item").attr("data-toggle", "collapse").attr("href", "[data-test='" + step.test + "']")
                            .text(step.test).prepend($("<span></span>").addClass("glyphicon glyphicon-chevron-right")));

                    var listGrp = $("<div></div>").addClass("list-group collapse").attr("data-test", step.test);
                    $("#lib").append(listGrp);

                    test[step.test] = {content : listGrp, testCase: {}};
                }
                if((!test[step.test].testCase.hasOwnProperty(step.testCase))){
                    var listGrp = test[step.test].content;
                    listGrp.append($("<a></a>").addClass("list-group-item sub-item").attr("data-toggle", "collapse").attr("href", "[data-testCase='" + step.testCase + "']")
                        .text(step.testCase).prepend($("<span></span>").addClass("glyphicon glyphicon-chevron-right")));

                    var listCaseGrp = $("<div></div>").addClass("list-group collapse").attr("data-testCase", step.testCase);
                    listGrp.append(listCaseGrp);

                    test[step.test].testCase[step.testCase] = listCaseGrp;
                }
                var listCaseGrp = test[step.test].testCase[step.testCase];
                listCaseGrp.append($("<a></a>").addClass("list-group-item sub-sub-item").attr("href", "#").text(step.description).data("stepInfo", step));
            }
            $('.list-group-item').on('click', function () {
                $('.glyphicon', this)
                        .toggleClass('glyphicon-chevron-right')
                        .toggleClass('glyphicon-chevron-down');
            });
        }
    });
}

function loadApplicationObject(dataInit){
    var array = [];
    $.ajax({
        url: "ReadApplicationObject?application="+dataInit.info.application,
        dataType: "json",
        success: function(data) {
            for(var i = 0; i<data.contentTable.length; i++) {
                array.push(data.contentTable[i].object);
            }
        }
    });
    return array;
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
    var offsetX = 50;
    var offsetY = 50;
    var img;

    if ($(obj).data("item") instanceof Action) {
        img = obj.parentNode;
    } else if ($(obj).data("item") instanceof Control) {
        img = obj;
    } else {
        img = obj;
        offsetX = 15;
        offsetY = 15;
    }

    source = obj;
    obj.style.opacity = '0.4';
    dataTransfer.effectAllowed = 'move';
    dataTransfer.setData('text/html', img.innerHTML);
    dataTransfer.setDragImage(img, offsetX, offsetY);
}

function handleDragEnter(event) {
    var target = this.parentNode;
    var sourceData = $(source).data("item");
    var targetData = $(target).data("item");

    if (sourceData instanceof Action && targetData instanceof Action) {
        if (isBefore(source.parentNode, target.parentNode)) {
            $(target).parent(".action-group").after(source.parentNode);
        } else {
            $(target).parent(".action-group").before(source.parentNode);
        }
    } else if (sourceData instanceof Control &&
            (targetData instanceof Action || targetData instanceof Control)) {
        if (isBefore(source, target) || targetData instanceof Action) {
            $(target).after(source);
        } else {
            $(target).before(source);
        }
    } else if (sourceData instanceof Step && targetData instanceof Step) {
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

function sortStep(step) {
    for (var j = 0; j < step.actionList.length; j++) {
        var action = step.actionList[j];

        action.controlList.sort(function (a, b) {
            return a.sort - b.sort;
        });
    }

    step.actionList.sort(function (a, b) {
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

/** JAVASCRIPT OBJECT **/

function Step(json, stepList) {
    this.stepActionContainer = $("<div></div>").addClass("step-container").css("display", "none");

    this.test = json.test;
    this.testcase = json.testCase;
    this.step = json.step;
    this.sort = json.sort;
    this.description = json.description;
    this.useStep = json.useStep;
    this.useStepTest = json.useStepTest;
    this.useStepTestCase = json.useStepTestCase;
    this.useStepStep = json.useStepStep;
    this.useStepStepSort = json.useStepStepSort;
    this.inLibrary = json.inLibrary;
    this.actionList = [];
    this.setActionList(json.actionList);

    this.stepList = stepList;
    this.toDelete = false;

    this.html = $("<li></li>").addClass("list-group-item list-group-item-calm row").css("margin-left", "0px");
    this.textArea = $("<div></div>").addClass("col-lg-10").addClass("step-description").text(this.description);

}

Step.prototype.draw = function () {
    var htmlElement = this.html;
    var drag = $("<div></div>").addClass("col-lg-2 drag-step").prop("draggable", true)
            .append($("<span></span>").addClass("fa fa-ellipsis-v"));

    drag.on("dragstart", handleDragStart);
    drag.on("dragenter", handleDragEnter);
    drag.on("dragover", handleDragOver);
    drag.on("dragleave", handleDragLeave);
    drag.on("drop", handleDrop);
    drag.on("dragend", handleDragEnd);

    htmlElement.append(drag);
    htmlElement.append(this.textArea);
    htmlElement.data("item", this);

    htmlElement.click(this.show);

    $("#stepList").append(htmlElement);
    $("#actionContainer").append(this.stepActionContainer);
};

Step.prototype.show = function () {
    var object = $(this).data("item");

    cancelEdit();

    for (var i = 0; i < object.stepList.length; i++) {
        var step = object.stepList[i];

        step.stepActionContainer.hide();
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

    if (object.inLibrary === "Y") {
        $("#isLib").removeClass("btn-default").addClass("btn-warning");
    }else{
        $("#isLib").removeClass("btn-warning").addClass("btn-default");
    }

    if (object.useStep === "Y") {
        $("#isLib").attr("disabled",true);
        $("#libInfo").html("(Imported from <a href='./TestCaseScript.jsp?test=" + object.useStepTest + "&testcase=" + object.useStepTestCase + "&step=" + object.useStepStepSort + "' >" + object.useStepTest + " - " + object.useStepTestCase + " - " + object.useStepStepSort + "</a>)");
        $("#addAction").prop("disabled",true);
    } else {
        $("#isLib").attr("disabled",false);
        $("#libInfo").text("");
        $("#addAction").prop("disabled",false);
    }

    object.stepActionContainer.show();
    $("#stepDescription").text(object.description);
    $("#stepInfo").show();
    $("#addActionContainer").show();
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

Step.prototype.setDelete = function () {
    this.toDelete = (this.toDelete) ? false : true;

    if (this.toDelete) {
        $("#deleteStep span").removeClass();
        $("#deleteStep span").addClass("glyphicon glyphicon-remove");
    } else {
        $("#deleteStep span").removeClass();
        $("#deleteStep span").addClass("glyphicon glyphicon-trash");
    }

    for (var i = 0; i < this.stepList.length; i++) {
        var step = this.stepList[i];

        if (step.toDelete) {
            step.html.addClass("list-group-item-danger");
        } else {
            step.html.removeClass("list-group-item-danger");
        }
    }
};

Step.prototype.setStep = function (step) {
    this.step = step;
};

Step.prototype.getStep = function () {
    return this.step;
};

Step.prototype.setSort = function (sort) {
    this.sort = sort;
};

Step.prototype.getJsonData = function () {
    var json = {};

    json.toDelete = this.toDelete;
    json.test = this.test;
    json.testcase = this.testcase;
    json.step = this.step;
    json.sort = this.sort;
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
        this.sort = json.sort;
        this.description = json.description;
        this.action = json.action;
        this.object = json.object;
        this.property = json.property;
        this.forceExeStatus = json.forceExeStatus;
        this.conditionOper = json.conditionOper;
        this.conditionVal = json.conditionVal1;
        this.screenshotFileName = json.screenshotFileName;
        this.value1 = json.value1;
        this.value2 = json.value2;
        this.controlList = [];
        this.setControlList(json.controlList);
    } else {
        this.test = "";
        this.testcase = "";
        this.step = parentStep.step;
        this.description = "";
        this.action = "Unknown";
        this.object = "";
        this.property = "";
        this.forceExeStatus = "";
        this.conditionOper = "always";
        this.conditionVal = "";
        this.screenshotFileName = "";
        this.value1 = "";
        this.value2 = "";
        this.controlList = [];
    }

    this.toDelete = false;
}

Action.prototype.draw = function () {
    var htmlElement = this.html;
    var action = this;
    var row = $("<div></div>").addClass("step-action row").addClass("action");
    var drag = $("<div></div>").addClass("drag-step-action col-lg-1").prop("draggable", true);
    var plusBtn = $("<button></button>").addClass("btn btn-default add-btn").append($("<span></span>").addClass("glyphicon glyphicon-chevron-down"));
    var addBtn = $("<button></button>").addClass("btn btn-success add-btn").append($("<span></span>").addClass("glyphicon glyphicon-plus"));
    var supprBtn = $("<button></button>").addClass("btn btn-danger add-btn").append($("<span></span>").addClass("glyphicon glyphicon-trash"));
    var btnGrp = $("<div></div>").addClass("col-lg-1").css("padding","0px").append($("<div>").addClass("boutonGroup").append(supprBtn).append(addBtn).append(plusBtn));
    var imgGrp = $("<div></div>").addClass("col-lg-1").css("height","100%").append($("<div style='margin-top:40px;'></div>").append($("<img>").attr("id","ApplicationObjectImg").css("width","100%")));

    if (this.parentStep.useStep === "N") {
        drag.append($("<span></span>").addClass("fa fa-ellipsis-v"));
        drag.on("dragstart", handleDragStart);
        drag.on("dragenter", handleDragEnter);
        drag.on("dragover", handleDragOver);
        drag.on("dragleave", handleDragLeave);
        drag.on("drop", handleDrop);
        drag.on("dragend", handleDragEnd);
    }else{
        addBtn.prop("disabled",true);
        supprBtn.prop("disabled",true);
    }

    plusBtn.click(function(){
        var container = $(this).parent().parent().parent();
        container.find(".fieldRow:eq(2)").toggle();
        if($(this).find("span").hasClass("glyphicon-chevron-down")){
            $(this).find("span").removeClass("glyphicon-chevron-down").addClass("glyphicon-chevron-up");
        }else{
            $(this).find("span").removeClass("glyphicon-chevron-up").addClass("glyphicon-chevron-down");
        }
    });

    addBtn.click(function () {
        var control = new Control(null, action);

        action.setControl(control);

        setPlaceholderControl();
        autocompleteAllFields();
    });

    supprBtn.click(function () {
        action.toDelete = (action.toDelete) ? false : true;

        if (action.toDelete) {
            action.html.find(".step-action").addClass("danger");
        } else {
            action.html.find(".step-action").removeClass("danger");
        }
    });

    row.append(drag);
    row.append(this.generateContent());
    row.append(imgGrp);
    row.append(btnGrp);
    row.data("item", this);
    htmlElement.prepend(row);

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

Action.prototype.getSequence = function () {
    return this.sequence;
};

Action.prototype.setSort = function (sort) {
    this.sort = sort;
};

Action.prototype.generateContent = function () {
    var obj = this;
    var content = $("<div></div>").addClass("content col-lg-9");
    var firstRow = $("<div style='margin-top:15px;'></div>").addClass("fieldRow row form-group");
    var secondRow = $("<div></div>").addClass("fieldRow row");
    var thirdRow = $("<div></div>").addClass("fieldRow row").hide();

    var actionList = $("<select></select>").addClass("form-control input-sm");
    var descField = $("<input>").addClass("description").addClass("form-control").prop("placeholder", "Describe this action");
    var objectField = $("<input>").attr("data-toggle","tooltip").attr("data-html","true").attr("data-container","body").attr("data-placement","top").attr("data-trigger","manual").attr("type","text").addClass("form-control input-sm");
    var propertyField = $("<input>").attr("data-toggle","tooltip").attr("data-html","true").attr("data-container","body").attr("data-placement","top").attr("data-trigger","manual").attr("type","text").addClass("form-control input-sm");
    var actionconditionparam = $("<input>").attr("type","text").addClass("form-control input-sm");

    var actionconditiononper = $("<select></select>").addClass("form-control input-sm");
    var forceExeStatusList = $("<select></select>").addClass("form-control input-sm");

    descField.val(this.description);
    descField.css("width","100%");
    descField.on("change", function () {
        obj.description = descField.val();
    });

    actionconditionparam.css("width","100%");
    actionconditionparam.on("change", function () {
        obj.conditionVal = actionconditionparam.val();
    });
    actionconditionparam.val(this.conditionVal);

    actionList = getSelectInvariant("ACTION", false, true).css("width","100%").attr("id","actionSelect");
    actionList.val(this.action);
    actionList.on("change", function () {
        obj.action = actionList.val();
        setPlaceholderAction();
    });

    forceExeStatusList = getSelectInvariant("ACTIONFORCEEXESTATUS", false, true).css("width","100%");
    forceExeStatusList.val(this.forceExeStatus);
    forceExeStatusList.on("change", function () {
        obj.forceExeStatus = forceExeStatusList.val();
//        setPlaceholderAction();
    });

    actionconditiononper = getSelectInvariant("ACTIONCONDITIONOPER", false, true).css("width","100%");
    actionconditiononper.on("change", function () {
        obj.conditionOper = actionconditiononper.val();
        if(obj.conditionOper != "ifPropertyExist"){
            actionconditionparam.parent().hide();
        }else{
            actionconditionparam.parent().show();
        }
//        setPlaceholderAction();
    });
    actionconditiononper.val(this.conditionOper).trigger("change");

    objectField.val(this.value1);
    objectField.css("width","100%");
    objectField.on("change", function () {
        obj.value1 = objectField.val();
    });

    propertyField.val(this.value2);
    propertyField.css("width","100%");
    propertyField.on("change", function () {
        obj.value2 = propertyField.val();
    });

    firstRow.append(descField);
    secondRow.append($("<div></div>").addClass("col-lg-3 form-group").append($("<label></label>").text("Action:")).append(actionList));
    secondRow.append($("<div></div>").addClass("col-lg-5 form-group").append($("<label></label>").text("Value 1:")).append(objectField));
    secondRow.append($("<div></div>").addClass("col-lg-4 form-group").append($("<label></label>").text("Value 2:")).append(propertyField));
    thirdRow.append($("<div></div>").addClass("col-lg-3 form-group").append($("<label></label>").text("Condition Operation:")).append(actionconditiononper));
    thirdRow.append($("<div></div>").addClass("col-lg-4 form-group").append($("<label></label>").text("Condition Parameter:")).append(actionconditionparam));
    thirdRow.append($("<div></div>").addClass("col-lg-3 form-group").append($("<label></label>").text("Force Execution:")).append(forceExeStatusList));

    actionconditiononper.trigger("change");

    if (this.parentStep.useStep === "Y") {
        descField.prop("readonly", true);
        objectField.prop("readonly", true);
        propertyField.prop("readonly", true);
        actionList.prop("disabled", "disabled");
        forceExeStatusList.prop("disabled", "disabled");
        actionconditiononper.prop("disabled", "disabled");
        actionconditionparam.prop("readonly", true);
    }

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
    json.sort = this.sort;
    json.description = this.description;
    json.action = this.action;
    json.object = this.value1;
    json.property = this.value2;
    json.forceExeStatus = this.forceExeStatus;
    json.conditionOper = this.conditionOper;
    json.conditionVal= this.conditionVal;
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
        this.sort = json.sort;
        this.description = json.description;
        this.objType = json.objType;
        this.controlSequence = json.controlSequence;
        this.value1 = json.value1;
        this.value2 = json.value2;
        this.fatal = json.fatal;
        this.screenshotFileName = "";
    } else {
        this.test = "";
        this.testcase = "";
        this.step = parentAction.step;
        this.sequence = parentAction.sequence;
        this.control = "Unknown";
        this.description = "";
        this.objType = "Unknown";
        this.value1 = "";
        this.value2 = "";
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
    var drag = $("<div></div>").addClass("drag-step-action col-lg-1").prop("draggable", true);
    var plusBtn = $("<button></button>").addClass("btn btn-default add-btn").append($("<span></span>").addClass("glyphicon glyphicon-chevron-down"));
    var supprBtn = $("<button></button>").addClass("btn btn-danger add-btn").append($("<span></span>").addClass("glyphicon glyphicon-trash"));
    var btnGrp = $("<div></div>").addClass("col-lg-1").append($("<div>").addClass("boutonGroup").append(supprBtn).append(plusBtn));
    var imgGrp = $("<div></div>").addClass("col-lg-1").css("height","100%").append($("<span style='display: inline-block; height: 100%; vertical-align: middle;'></span>")).append($("<img>").attr("id","ApplicationObjectImg").css("width","100%"));

    var content = this.generateContent();

    if (this.parentAction.parentStep.useStep === "N") {
        drag.append($("<span></span>").addClass("fa fa-ellipsis-v"));
        drag.on("dragstart", handleDragStart);
        drag.on("dragenter", handleDragEnter);
        drag.on("dragover", handleDragOver);
        drag.on("dragleave", handleDragLeave);
        drag.on("drop", handleDrop);
        drag.on("dragend", handleDragEnd);
    }

    supprBtn.click(function () {
        control.toDelete = (control.toDelete) ? false : true;

        if (control.toDelete) {
            control.html.addClass("danger");
        } else {
            control.html.removeClass("danger");
        }
    });

    plusBtn.click(function(){
        var container = $(this).parent().parent().parent();
        container.find(".fieldRow:eq(2)").toggle();
        if($(this).find("span").hasClass("glyphicon-chevron-down")){
            $(this).find("span").removeClass("glyphicon-chevron-down").addClass("glyphicon-chevron-up");
        }else{
            $(this).find("span").removeClass("glyphicon-chevron-up").addClass("glyphicon-chevron-down");
        }
    });

    htmlElement.append(drag);
    htmlElement.append(content);
    htmlElement.append(imgGrp);
    htmlElement.append(btnGrp);
    htmlElement.data("item", this);

    this.parentAction.html.append(htmlElement);
};

Control.prototype.setStep = function (step) {
    this.step = step;
};

Control.prototype.setSequence = function (sequence) {
    this.sequence = sequence;
};

Control.prototype.getControl = function () {
    return this.control;
}

Control.prototype.setControl = function (control) {
    this.control = control;
};

Control.prototype.setSort = function (sort) {
    this.sort = sort;
};

Control.prototype.generateContent = function () {
    var obj = this;
    var content = $("<div></div>").addClass("content col-lg-9");
    var firstRow = $("<div style='margin-top:15px;'></div>").addClass("fieldRow row form-group");
    var secondRow = $("<div></div>").addClass("fieldRow row");
    var thirdRow = $("<div></div>").addClass("fieldRow row").hide();

    var controlList = $("<select></select>").addClass("form-control input-sm").css("width", "100%");
    var descField = $("<input>").addClass("description").addClass("form-control").prop("placeholder", "Description");
    var controlValueField = $("<input>").attr("data-toggle","tooltip").attr("data-html","true").attr("data-container","body").attr("data-placement","top").attr("data-trigger","manual").addClass("form-control input-sm").css("width", "100%");
    var controlPropertyField = $("<input>").attr("data-toggle","tooltip").attr("data-html","true").attr("data-container","body").attr("data-placement","top").attr("data-trigger","manual").addClass("form-control input-sm").css("width", "100%");
    var fatalList = $("<select></select>").addClass("form-control input-sm");

    descField.val(this.description);
    descField.css("width","100%");
    descField.on("change", function () {
        obj.description = descField.val();
    });

    controlList = getSelectInvariant("CONTROL", false, true).attr("id","controlSelect");
    controlList.val(this.control);
    controlList.css("width","100%");
    controlList.on("change", function () {
        obj.control = controlList.val();
        setPlaceholderControl();
    });

    controlValueField.val(this.value1);
    controlValueField.css("width","100%")
    controlValueField.on("change", function () {
        obj.value1 = controlValueField.val();
    });

    controlPropertyField.val(this.value2);
    controlPropertyField.css("width","100%");
    controlPropertyField.on("change", function () {
        obj.value2 = controlPropertyField.val();
    });

    fatalList = getSelectInvariant("CTRLFATAL", false, true);
    fatalList.val(this.fatal);
    fatalList.css("width","100%");
    fatalList.on("change", function () {
        obj.fatal = fatalList.val();
    });

    firstRow.append(descField);
    secondRow.append($("<div></div>").addClass("col-lg-4 form-group").append($("<label></label>").text("Control:")).append(controlList));
    secondRow.append($("<div></div>").addClass("col-lg-4 form-group").append($("<label></label>").text("Value 1:")).append(controlValueField));
    secondRow.append($("<div></div>").addClass("col-lg-4 form-group").append($("<label></label>").text("Value 2:")).append(controlPropertyField));
    thirdRow.append($("<div></div>").addClass("col-lg-2 form-group").append($("<label></label>").text("Fatal:")).append(fatalList));

    if (this.parentStep.useStep === "Y") {
        descField.prop("readonly", true);
        controlValueField.prop("readonly", true);
        controlPropertyField.prop("readonly", true);
        controlList.prop("disabled", "disabled");
        fatalList.prop("disabled", "disabled");
    }

    content.append(firstRow);
    content.append(secondRow);
    content.append(thirdRow);

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
    json.sort = this.sort;
    json.description = this.description;
    json.objType = this.objType;
    json.controlSequence = this.controlSequence;
    json.value1 = this.value1;
    json.value2 = this.value2;
    json.fatal = this.fatal;
    json.screenshotFileName = this.screenshotFileName;

    return json;
};

/**
 * Call Add Action anf focus to next description when 
 * focusing on description and clicking on enter
 * @returns {undefined}
 */
function listenEnterKeypressWhenFocusingOnDescription() {
    $("input[class='description form-control']").each(function (index, field) {
        $(field).off('keydown');
        $(field).on('keydown', function (e) {
            if (e.which === 13) {
                //if description is not empty, create new action
                if ($(field)[0].value.length !== 0) {
                    $.when(addAction()).then(function (action) {
                        listenEnterKeypressWhenFocusingOnDescription();
                        $($(action.html[0]).find(".description")[0]).focus();
                        setPlaceholderAction();
                        autocompleteAllFields();
                    });
                } else {
                    //if description is empty, create action or control depending on field
                    if ($(field).closest(".step-action").hasClass("action")) {
                        var newAction = $(field).closest(".action-group");
                        var oldAction = newAction.prev().find(".step-action.row.action").last();
                        newAction.remove();
                        $.when(addControl(oldAction.data("item"))).then(function (action) {
                            listenEnterKeypressWhenFocusingOnDescription();
                            $($(action.html[0]).find(".description")[0]).focus();
                            setPlaceholderControl();
                            autocompleteAllFields();
                        });
                    } else {
                        var newAction = $(field).closest(".step-action");
                        newAction.remove();
                        $.when(addAction()).then(function (action) {
                            listenEnterKeypressWhenFocusingOnDescription();
                            $($(action.html[0]).find(".description")[0]).focus();
                            setPlaceholderAction();
                            autocompleteAllFields();
                        });
                    }
                }
            }
        });
    });
}

function addControl(action) {
    var control = new Control(null, action);
    action.setControl(control);
    return control;
}

var autocompleteAllFields;
(function() {
    //var accessible only in closure
    var TagsToUse = [];
    var tcInfo;
    var test;
    var testcase;
    //function accessible everywhere that has access to TagsToUse
    autocompleteAllFields = function(Tags, info, thistest, thistestcase) {
        if(Tags != undefined){
            TagsToUse = Tags;
        }
        if(info != undefined){
            tcInfo = info;
        }
        if(thistest != undefined){
            test = thistest;
        }
        if(thistestcase != undefined){
            testcase = thistestcase;
        }

        autocompleteVariable("div.step-action .content div.fieldRow div:nth-child(n+2) input", TagsToUse);

        $("div.step-action .content div.fieldRow div:nth-child(n+2) input").each(function(i,e){
            $(e).on("input change",function(ev){
                var name = undefined;
                var nameNotExist = undefined;
                var objectNotExist = false;
                var typeNotExist = undefined;
                var checkObject = [];
                var betweenPercent = $(e).val().match(new RegExp(/%[^%]*%/g));
                if(betweenPercent != null && betweenPercent.length > 0){
                    var i = betweenPercent.length - 1;
                    while(i>=0){
                        var findname = betweenPercent[i].match(/\.[^\.]*(\.|.$)/g);
                        if(betweenPercent[i].startsWith("%object.") && findname != null && findname.length > 0) {
                            name = findname[0];
                            name = name.slice( 1, name.length - 1 );

                            $(e).parent().parent().parent().parent().find("#ApplicationObjectImg").attr("src","ReadApplicationObjectImage?application=" + tcInfo.application + "&object=" + name);

                            checkObject.push($.ajax({
                                url: "ReadApplicationObject",
                                data: {application: tcInfo.application, object: name},
                                dataType: "json",
                                success: function (data) {
                                    if(data.contentTable == undefined){
                                        objectNotExist = true;
                                        nameNotExist = name;
                                        typeNotExist = "applicationobject";
                                    }
                                },
                                error: showUnexpectedError
                            }));
                        } else if(betweenPercent[i].startsWith("%property.") && findname != null && findname.length > 0) {
                            name = findname[0];
                            name = name.slice( 1, name.length - 1);

                            checkObject.push($.ajax({
                                url: "GetPropertiesForTestCase",
                                data: {test: test, testcase: testcase, property: name},
                                dataType: "json",
                                success: function (data) {
                                    if(data == undefined || data.length <= 0){
                                        objectNotExist = true;
                                        nameNotExist = name;
                                        typeNotExist = "property";
                                    }
                                },
                                error: showUnexpectedError
                            }));
                        }
                        i--;
                    }
                }
                Promise.all(checkObject).then(function(data){
                    if(objectNotExist){
                        if(typeNotExist == "applicationobject") {
                            var newTitle = "<a style='color: #fff;' href='#' onclick='addApplicationObjectModalClick(undefined, \"" + nameNotExist + "\",\"" + tcInfo.application + "\")'><span class='glyphicon glyphicon-exclamation-sign' aria-hidden='true'></span> Warning : " + nameNotExist + " is not an Object of this Application</a>";
                            if (newTitle != $(e).attr('data-original-title')) {
                                $(e).attr('data-original-title', newTitle).tooltip('fixTitle').tooltip('show');
                            }
                        }else if(typeNotExist == "property"){
                            //TODO better way to add property
                            var newTitle = "<a style='color: #fff;' href='#' onclick=\"$('#manageProp').click();$('#addProperty').click();$('tbody#propTable tr input#propName:last-child').val('" + nameNotExist + "').trigger('change');\"><span class='glyphicon glyphicon-exclamation-sign' aria-hidden='true'></span> Warning : " + nameNotExist + " is not a Property</a>";
                            if (newTitle != $(e).attr('data-original-title')) {
                                $(e).attr('data-original-title', newTitle).tooltip('fixTitle').tooltip('show');
                            }
                        }
                    }else{
                        $(e).tooltip('destroy');
                    }
                });
            });
        }).trigger("change");
    };
})();

editPropertiesModalClick = function(test, testcase, info, propertyToAdd) {
    if (info == undefined) {
        info = previousInfo;
    }
    $("#propTable").empty();
    loadProperties(test, testcase, info);
    if (propertyToAdd != undefined) {
        // Building full list of country from testcase.
        var myCountry = [];
        $.each(info.countryList, function (index) {
            myCountry.push(index);
        });

        var newProperty = {
            property: propertyToAdd,
            description: "",
            country: myCountry,
            type: "text",
            database: "",
            value1: "",
            value2: "",
            length: 0,
            rowLimit: 0,
            nature: "STATIC",
            toDelete: false
        };

        drawProperty(newProperty, info);
    }
    $("#propertiesModal").modal('show');
}

function editPropertiesModalSaveHandler(){
    clearResponseMessage($('#propertiesModal'));
    showLoaderInModal('#propertiesModal');

    var properties = $("#propTable [name='masterProp']");
    console.log(properties);
    var propArr = [];
    for (var i = 0; i < properties.length; i++) {
        propArr.push($(properties[i]).data("property"));
    }

    $.ajax({
        url: "UpdateTestCaseProperties1",
        async: true,
        method: "POST",
        data: {informationInitialTest: GetURLParameter("test"),
            informationInitialTestCase: GetURLParameter("testcase"),
            informationTest: GetURLParameter("test"),
            informationTestCase: GetURLParameter("testcase"),
            propArr: JSON.stringify(propArr)},
        success: function (data) {
            hideLoaderInModal('#propertiesModal');
            if (getAlertType(data.messageType) === 'success') {
                $("div.step-action .content div.fieldRow div:nth-child(n+2) input").trigger("change");
                showMessage(data);
                $('#propertiesModal').modal('hide');
            } else {
                showMessage(data, $('#propertiesModal'));
            }
        },
        error: showUnexpectedError
    });
}

function setPlaceholderAction() {
    /**
     * Todo : GetFromDatabase
     */
    var placeHoldersList = {"fr": [
        {"type": "Unknown", "object": null, "property": null},
        {"type": "keypress", "object": "[opt] Chemin vers l'élement à cibler", "property": ""},
        {"type": "hideKeyboard", "object": null, "property": null},
        {"type": "swipe", "object": null, "property": null},
        {"type": "click", "object": "Chemin vers l'élement à cliquer", "property": null},
        {"type": "mouseLeftButtonPress", "object": "Chemin vers l'élement à cibler", "property": null},
        {"type": "mouseLeftButtonRelease", "object": "Chemin vers l'élement", "property": null},
        {"type": "doubleClick", "object": "Chemin vers l'élement à double-cliquer", "property": null},
        {"type": "rightClick", "object": "Chemin vers l'élement à clicker avec le bouton droit", "property": null},
        {"type": "focusToIframe", "object": "Identifiant de l'iFrame à cibler", "property": null},
        {"type": "focusDefaultIframe", "object": null, "property": null},
        {"type": "switchToWindow", "object": "Identifiant de fenêtre", "property": null},
        {"type": "manageDialog", "object": "ok ou cancel", "property": null},
        {"type": "mouseOver", "object": "Chemin vers l'élement", "property": null},
        {"type": "mouseOverAndWait", "object": "Action Depreciée", "property": "Action Depreciée"},
        {"type": "openUrlWithBase", "object": "/URI à appeler", "property": null},
        {"type": "openUrlLogin", "object": null, "property": null},
        {"type": "openUrl", "object": "URL à appeler", "property": null},
        {"type": "select", "object": "Chemin vers l'élement", "property": "Chemin vers l'option"},
        {"type": "type", "object": "Chemin vers l'élement", "property": "Nom de propriété"},
        {"type": "wait", "object": "Valeur(ms) ou élement", "property": null},
        {"type": "callSoap", "object": "Nom du Soap (librairie)", "property": "Nom de propriété"},
        {"type": "callSoapWithBase", "object": "Nom du Soap (librairie)", "property": "Nom de propriété"},
        {"type": "removeDifference", "object": "Action Depreciée", "property": "Action Depreciée"},
        {"type": "executeSqlUpdate", "object": "Nom de Base de donnée", "property": "Script à executer"},
        {"type": "executeSqlStoredProcedure", "object": "Nom de Base de donnée", "property": "Procedure Stoquée à executer"},
        {"type": "calculateProperty", "object": null, "property": "Nom d'une Proprieté"},
        {"type": "doNothing", "object": null, "property": null},
        {"type": "skipAction", "object": null, "property": null},
        {"type": "getPageSource", "object": null, "property": null}
    ], "en": [
        {"type": "Unknown", "object": null, "property": null},
        {"type": "keypress", "object": "[opt] Element path", "property": ""},
        {"type": "hideKeyboard", "object": null, "property": null},
        {"type": "swipe", "object": null, "property": null},
        {"type": "click", "object": "Element path", "property": null},
        {"type": "mouseLeftButtonPress", "object": "Element path", "property": null},
        {"type": "mouseLeftButtonRelease", "object": "Element path", "property": null},
        {"type": "doubleClick", "object": "Element path", "property": null},
        {"type": "rightClick", "object": "Element path", "property": null},
        {"type": "focusToIframe", "object": "Id of the target iFrame", "property": null},
        {"type": "focusDefaultIframe", "object": null, "property": null},
        {"type": "switchToWindow", "object": "Window id", "property": null},
        {"type": "manageDialog", "object": "ok or cancel", "property": null},
        {"type": "mouseOver", "object": "Element path", "property": null},
        {"type": "mouseOverAndWait", "object": "Deprecated", "property": "Deprecated"},
        {"type": "openUrlWithBase", "object": "/URI to call", "property": null},
        {"type": "openUrlLogin", "object": null, "property": null},
        {"type": "openUrl", "object": "URL to call", "property": null},
        {"type": "select", "object": "Element path", "property": "Option path"},
        {"type": "type", "object": "Element path", "property": "Property Name"},
        {"type": "wait", "object": "Time(ms) or Element", "property": null},
        {"type": "callSoap", "object": "Soap Name (library)", "property": "Property Name"},
        {"type": "callSoapWithBase", "object": "Soap Name (library)", "property": "Property Name"},
        {"type": "removeDifference", "object": "Deprecated", "property": "Deprecated"},
        {"type": "executeSqlUpdate", "object": "Database Name", "property": "Script"},
        {"type": "executeSqlStoredProcedure", "object": "Database Name", "property": "Stored Procedure"},
        {"type": "calculateProperty", "object": null, "property": "Property Name"},
        {"type": "doNothing", "object": null, "property": null},
        {"type": "skipAction", "object": null, "property": null},
        {"type": "getPageSource", "object": null, "property": null}
    ]};

    var user = getUser();
    var placeHolders = placeHoldersList[user.language];

//    console.debug("-- Action");

    $('select#actionSelect option:selected').each(function (i, e) {
        for (var i = 0; i < placeHolders.length; i++) {
            console.log(e.value + " / " + placeHolders[i].type);
            if (placeHolders[i].type === e.value) {
                if (placeHolders[i].object !== null) {
                    $(e).parent().parent().next().show();
                    $(e).parent().parent().next().find('label').text(placeHolders[i].object);
                } else {
                    $(e).parent().parent().next().hide();
                }
                if (placeHolders[i].property !== null) {
                    $(e).parent().parent().next().next().show();
                    $(e).parent().parent().next().next().find('label').text(placeHolders[i].property);
                } else {
                    $(e).parent().parent().next().next().hide();
                }
            }
        }
    });
}

function setPlaceholderControl() {
    /**
     * Todo : GetFromDatabase
     */
    var placeHoldersList = {"fr": [
            {"type": "Unknown", "controlValue": null, "controlProp": null, "fatal": null},
            {"type": "verifyStringEqual", "controlValue": "String1", "controlProp": "String2", "fatal": ""},
            {"type": "verifyStringDifferent", "controlValue": "String1", "controlProp": "String2", "fatal": ""},
            {"type": "verifyStringGreater", "controlValue": "String1 ex : AAA", "controlProp": "String2 ex: ZZZ", "fatal": ""},
            {"type": "verifyStringMinor", "controlValue": "String1 ex : ZZZ", "controlProp": "String2 ex: AAA", "fatal": ""},
            {"type": "verifyStringContains", "controlValue": "String1 ex : toto", "controlProp": "String2 ex : ot", "fatal": ""},
            {"type": "verifyIntegerEquals", "controlValue": "Integer1", "controlProp": "Integer2", "fatal": ""},
            {"type": "verifyIntegerDifferent", "controlValue": "Integer1", "controlProp": "Integer2", "fatal": ""},
            {"type": "verifyIntegerGreater", "controlValue": "Integer1 ex : 10", "controlProp": "Integer2 ex : 20", "fatal": ""},
            {"type": "verifyIntegerMinor", "controlValue": "Integer1 ex : 20", "controlProp": "Integer2 ex : 10", "fatal": ""},
            {"type": "verifyElementPresent", "controlValue": null, "controlProp": "Element ex : data-cerberus=fieldToto", "fatal": ""},
            {"type": "verifyElementNotPresent", "controlValue": null, "controlProp": "Element ex : data-cerberus=fieldToto", "fatal": ""},
            {"type": "verifyElementVisible", "controlValue": null, "controlProp": "Element ex : data-cerberus=fieldToto", "fatal": ""},
            {"type": "verifyElementNotVisible", "controlValue": null, "controlProp": "Element ex : data-cerberus=fieldToto", "fatal": ""},
            {"type": "verifyElementEquals", "controlValue": "Expected element", "controlProp": "XPath of the element", "fatal": ""},
            {"type": "verifyElementDifferent", "controlValue": "Not Expected element", "controlProp": "XPath of the element", "fatal": ""},
            {"type": "verifyElementInElement", "controlValue": "Sub Element", "controlProp": "Master Element", "fatal": ""},
            {"type": "verifyElementClickable", "controlValue": null, "controlProp": "Element ex : data-cerberus=fieldToto", "fatal": ""},
            {"type": "verifyElementNotClickable", "controlValue": null, "controlProp": "Element ex : data-cerberus=fieldToto", "fatal": ""},
            {"type": "verifyTextInElement", "controlValue": "Text", "controlProp": "Element", "fatal": ""},
            {"type": "verifyTextNotInElement", "controlValue": "Text", "controlProp": "Element", "fatal": ""},
            {"type": "verifyRegexInElement", "controlValue": "Regex", "controlProp": "Element", "fatal": ""},
            {"type": "verifyTextInPage", "controlValue": null, "controlProp": "Regex", "fatal": ""},
            {"type": "verifyTextNotInPage", "controlValue": null, "controlProp": "Regex", "fatal": ""},
            {"type": "verifyTitle", "controlValue": null, "controlProp": "Title", "fatal": ""},
            {"type": "verifyUrl", "controlValue": null, "controlProp": "URL", "fatal": ""},
            {"type": "verifyTextInDialog", "controlValue": null, "controlProp": "Text", "fatal": ""},
            {"type": "verifyXmlTreeStructure", "controlValue": "Tree", "controlProp": "XPath", "fatal": ""},
            {"type": "takeScreenshot", "controlValue": null, "controlProp": null, "fatal": null},
            {"type": "getPageSource", "controlValue": null, "controlProp": null, "fatal": null},
            {"type": "skipControl", "controlValue": null, "controlProp": null, "fatal": null}
        ], "en": [
            {"type": "Unknown", "controlValue": null, "controlProp": null, "fatal": null},
            {"type": "verifyStringEqual", "controlValue": "String1", "controlProp": "String2", "fatal": ""},
            {"type": "verifyStringDifferent", "controlValue": "String1", "controlProp": "String2", "fatal": ""},
            {"type": "verifyStringGreater", "controlValue": "String1 ex : AAA", "controlProp": "String2 ex: ZZZ", "fatal": ""},
            {"type": "verifyStringMinor", "controlValue": "String1 ex : ZZZ", "controlProp": "String2 ex: AAA", "fatal": ""},
            {"type": "verifyStringContains", "controlValue": "String1 ex : toto", "controlProp": "String2 ex : ot", "fatal": ""},
            {"type": "verifyIntegerEquals", "controlValue": "Integer1", "controlProp": "Integer2", "fatal": ""},
            {"type": "verifyIntegerDifferent", "controlValue": "Integer1", "controlProp": "Integer2", "fatal": ""},
            {"type": "verifyIntegerGreater", "controlValue": "Integer1 ex : 10", "controlProp": "Integer2 ex : 20", "fatal": ""},
            {"type": "verifyIntegerMinor", "controlValue": "Integer1 ex : 20", "controlProp": "Integer2 ex : 10", "fatal": ""},
            {"type": "verifyElementPresent", "controlValue": null, "controlProp": "Element ex : data-cerberus=fieldToto", "fatal": ""},
            {"type": "verifyElementNotPresent", "controlValue": null, "controlProp": "Element ex : data-cerberus=fieldToto", "fatal": ""},
            {"type": "verifyElementVisible", "controlValue": null, "controlProp": "Element ex : data-cerberus=fieldToto", "fatal": ""},
            {"type": "verifyElementNotVisible", "controlValue": null, "controlProp": "Element ex : data-cerberus=fieldToto", "fatal": ""},
            {"type": "verifyElementEquals", "controlValue": "Expected element", "controlProp": "XPath of the element", "fatal": ""},
            {"type": "verifyElementDifferent", "controlValue": "Not Expected element", "controlProp": "XPath of the element", "fatal": ""},
            {"type": "verifyElementInElement", "controlValue": "Sub Element", "controlProp": "Master Element", "fatal": ""},
            {"type": "verifyElementClickable", "controlValue": null, "controlProp": "Element ex : data-cerberus=fieldToto", "fatal": ""},
            {"type": "verifyElementNotClickable", "controlValue": null, "controlProp": "Element ex : data-cerberus=fieldToto", "fatal": ""},
            {"type": "verifyTextInElement", "controlValue": "Text", "controlProp": "Element", "fatal": ""},
            {"type": "verifyTextNotInElement", "controlValue": "Text", "controlProp": "Element", "fatal": ""},
            {"type": "verifyRegexInElement", "controlValue": "Regex", "controlProp": "Element", "fatal": ""},
            {"type": "verifyTextInPage", "controlValue": null, "controlProp": "Regex", "fatal": ""},
            {"type": "verifyTextNotInPage", "controlValue": null, "controlProp": "Regex", "fatal": ""},
            {"type": "verifyTitle", "controlValue": null, "controlProp": "Title", "fatal": ""},
            {"type": "verifyUrl", "controlValue": null, "controlProp": "URL", "fatal": ""},
            {"type": "verifyTextInDialog", "controlValue": null, "controlProp": "Text", "fatal": ""},
            {"type": "verifyXmlTreeStructure", "controlValue": "Tree", "controlProp": "XPath", "fatal": ""},
            {"type": "takeScreenshot", "controlValue": null, "controlProp": null, "fatal": null},
            {"type": "getPageSource", "controlValue": null, "controlProp": null, "fatal": null},
            {"type": "skipControl", "controlValue": null, "controlProp": null, "fatal": null}
        ]};

    var user = getUser();
    var placeHolders = placeHoldersList[user.language];

//    console.debug("-- Control");

    $('select#controlSelect option:selected').each(function (i, e) {

        for (var i = 0; i < placeHolders.length; i++) {
            if (placeHolders[i].type === e.value) {
                if (placeHolders[i].controlValue !== null) {
                    $(e).parent().parent().next().show();
                    $(e).parent().parent().next().find('label').text(placeHolders[i].controlValue);
                } else {
                    $(e).parent().parent().next().hide();
                }
                if (placeHolders[i].controlProp !== null) {
                    $(e).parent().parent().next().next().show();
                    $(e).parent().parent().next().next().find('label').text(placeHolders[i].controlProp);
                } else {
                    $(e).parent().parent().next().next().hide();
                }
                if (placeHolders[i].fatal !== null) {
                    $(e).parent().parent().next().next().next().show();
                } else {
                    $(e).parent().parent().next().next().next().hide();
                }
            }
        }
    });
}

function setPlaceholderProperty() {
    /**
     * Todo : GetFromDatabase
     */
    var placeHoldersList = {"fr": [
            {"type": "text", "database": null, "length": null, "rowLimit": null, "nature": null},
            {"type": "executeSql", "database": null, "length": null, "rowLimit": null, "nature": null}
        ], "en": [
            {"type": "text", "database": null, "length": null, "rowLimit": null, "nature": null},
            {"type": "executeSql", "database": null, "length": null, "rowLimit": null, "nature": null}
        ]};

    var user = getUser();
    user.language;
    var placeHolders = placeHoldersList[user.language];

    console.debug("-- Property");

    $('div[class="rowProperty form-inline"] option:selected').each(function (i, e) {
        console.debug(e.value);
        for (var i = 0; i < placeHolders.length; i++) {
            console.debug(placeHolders[i].type + " - " + e.value);
            if (placeHolders[i].type === e.value) {
                if (placeHolders[i].controlValue !== null) {
                    $(e).parent().parent().next().show();
                    $(e).parent().parent().next().find('input').prop("placeholder", placeHolders[i].controlValue);
                } else {
                    $(e).parent().parent().next().hide();
                }
                if (placeHolders[i].controlProp !== null) {
                    $(e).parent().parent().next().next().show();
                    $(e).parent().parent().next().next().find('input').prop("placeholder", placeHolders[i].controlProp);
                } else {
                    $(e).parent().parent().next().next().hide();
                }
                if (placeHolders[i].fatal !== null) {
                    $(e).parent().parent().next().next().next().show();
                } else {
                    $(e).parent().parent().next().next().next().hide();
                }
            }
        }
    });

}
