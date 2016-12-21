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

        $(window).bind('beforeunload', function(){
            if(getModif()){
                return true; //Display alert Message that a modification has been done
            }
        });

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
        var property = GetURLParameter("property");

        $("#runOld").parent().attr("href","./TestCase.jsp?Test=" + test + "&TestCase=" + testcase + "&Load=Load");

        displayHeaderLabel(doc);
        displayGlobalLabel(doc);
        displayFooter(doc);

        displayPageLabel(doc);

        $("#addStepModal [name='buttonAdd']").html(doc.getDocLabel("page_global", "btn_add"));
        
        displayInvariantList("conditionOper", "TESTCASECONDITIONOPER", false);
        displayInvariantList("group", "GROUP", false, true);
        displayInvariantList("status", "TCSTATUS", false, true);
        displayInvariantList("priority", "PRIORITY", false, true);
        $('[name="origin"]').append('<option value="All">' + doc.getDocLabel("page_global", "lbl_all") + '</option>');
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

        $.ajax({
            url: "ReadTest",
            async: true,
            success: function (data){
                data.contentTable.sort(function (a, b) {
                    var aa = a.test.toLowerCase();
                    var bb = b.test.toLowerCase();
                    if (aa > bb) {
                        return 1;
                    } else if (aa < bb) {
                        return -1;
                    }
                    return 0;
                });
                $(".testTestCase #test").prepend("<option value=''>" + doc.getDocLabel("page_testcasescript", "select_test") + "</option>");
                for (var i = 0; i < data.contentTable.length; i++) {
                    $(".testTestCase #test").append("<option value='" + data.contentTable[i].test + "'>" + data.contentTable[i].test + " - " + data.contentTable[i].description + "</option>")
                }

                if(test != null) {
                    $(".testTestCase #test option[value='" + test + "']").prop('selected', true);
                }
                $(".testTestCase #test").bind("change", function (event) {
                    window.location.href = "./TestCaseScript.jsp?test=" + $(this).val();
                });
                $(".testTestCase #test").select2({width:"100%"}).next().css("margin-bottom","7px");
            }
        });

        if(test != null) {
            $.ajax({
                url: "ReadTestCase?test=" + test,
                async: true,
                success: function (data) {
                    data.contentTable.sort(function (a, b) {
                        var aa = a.testCase.toLowerCase();
                        var bb = b.testCase.toLowerCase();
                        if (aa > bb) {
                            return 1;
                        } else if (aa < bb) {
                            return -1;
                        }
                        return 0;
                    });
                    $("#testCaseSelect").prepend("<option value=''>" + doc.getDocLabel("page_testcasescript", "select_testcase") + "</option>");
                    for (var i = 0; i < data.contentTable.length; i++) {
                        $("#testCaseSelect").append("<option value='" + data.contentTable[i].testCase + "'>" + data.contentTable[i].testCase + " - " + data.contentTable[i].description + "</option>")
                    }
                    if(testcase != null) {
                        $("#testCaseSelect option[value='" + testcase + "']").prop('selected', true);
                    }
                    $("#testCaseSelect").bind("change", function (event) {
                        window.location.href = "./TestCaseScript.jsp?test=" + test + "&testcase=" + $(this).val();
                    });
                    $("#testCaseSelect").select2({width: '100%'});
                }
            });
        }
        if(test != null && testcase != null){
            // Edit TestCase open the TestCase Modal
            $("#editTcInfo").click(function () {
                editTestCaseClick(test, testcase);
            });

            $("#TestCaseButton").show();
            $("#tcBody").show();

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
                    var inheritedProperties = drawInheritedProperty(data.inheritedProp);

                    listenEnterKeypressWhenFocusingOnDescription();
                    setPlaceholderAction();
                    setPlaceholderControl();

                    var propertiesPromise = loadProperties(test, testcase, data.info, property);
                    var objectsPromise = loadApplicationObject(data);

                    Promise.all([propertiesPromise,objectsPromise]).then(function(data2){
                        var properties = data2[0];
                        var availableObjects = data2[1];
                        var availableProperties = properties.concat(inheritedProperties.filter(function (item) {
                            return properties.indexOf(item) < 0;
                        }));
                        var availableObjectProperties = [
                            "value",
                            "picturepath",
                            "pictureurl"
                        ];
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

                    });

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
                        autocompleteAllFields();
                    });

                    $('[data-toggle="tooltip"]').tooltip();

                    initModification();

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

                if(step.isStepInUseByOtherTestCase){
                    showStepUsesLibraryInConfirmationModal(step);
                }else {
                    setModif(true);
                    step.setDelete();
                }
            });

            $("#addAction").click(function () {
                addActionAndFocus()
            });
            $("#saveScript").click(saveScript);

            $("#runTestCase").parent().attr("href","./RunTests1.jsp?test=" + test + "&testcase=" + testcase);
            $("#seeLastExec").parent().attr("href","./ExecutionDetailList.jsp?test=" + test + "&testcase=" + testcase);
            $("#seeLogs").parent().attr("href","./LogViewer.jsp?Test=" + test + "&TestCase=" + testcase);

            $.ajax({
                url: "ReadTestCaseExecution",
                data: {test: test, testCase: testcase},
                dataType: "json",
                success: function (data) {
                    if(!jQuery.isEmptyObject(data.contentTable)) {
                        $("#rerunTestCase").parent().attr("href","./RunTests1.jsp?test=" + test + "&testcase=" + testcase + "&country=" + data.contentTable.country + "&environment=" + data.contentTable.env);
                        $("#rerunTestCase").attr("title", "Last Execution was " + data.contentTable.controlStatus + " in " + data.contentTable.env + " in " + data.contentTable.country + " on " + data.contentTable.end)
                    }else{
                        $("#rerunTestCase").attr("disabled",true);
                        $("#seeLastExec").attr("disabled",true);
                    }
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
        }
    });
});

function displayPageLabel(doc){
    $("h1.page-title-line").html(doc.getDocLabel("page_testcasescript", "testcasescript_title"));
    $("#nav-execution #list-wrapper #stepListWrapper h3").html(doc.getDocLabel("page_testcasescript", "steps_title"));
    $("#nav-execution #list-wrapper #tcButton h3").html(doc.getDocLabel("page_global", "columnAction"));
    $("#saveScript").html("<span class='glyphicon glyphicon-save'></span> " + doc.getDocLabel("page_testcasescript", "save_script"));
    $("#editTcInfo").html(doc.getDocLabel("page_testcasescript", "edit_testcase"));
    $("#runTestCase").html("<span class='glyphicon glyphicon-play'></span> " + doc.getDocLabel("page_testcasescript", "run_testcase"));
    $("#rerunTestCase").html("<span class='glyphicon glyphicon-forward'></span> " + doc.getDocLabel("page_testcasescript", "rerun_testcase"));
    $("#seeLastExec").html("<span class='glyphicon glyphicon-fast-backward'></span> " + doc.getDocLabel("page_testcasescript", "see_lastexec"));
    $("#seeLogs").html("<span class='glyphicon glyphicon-book'></span> " + doc.getDocLabel("page_testcasescript", "see_logs"));
    $("#runOld").html("<span class='glyphicon glyphicon-bookmark'></span> " + doc.getDocLabel("page_testcasescript", "run_old"));
    $("#addStep").html(doc.getDocLabel("page_testcasescript", "add_step"));
    $("#manageProp").html(doc.getDocLabel("page_testcasescript", "manage_prop"));
    $("#addActionBottomBtn button").html(doc.getDocLabel("page_testcasescript", "add_action"));
    $("#stepConditionOper").prev().html(doc.getDocLabel("page_testcasescript", "step_condition_operation"));
    $("#stepConditionVal1").prev().html(doc.getDocLabel("page_testcasescript", "step_condition_value1"));
}

function addAction() {
    setModif(true);
    var step = $("#stepList li.active").data("item");
    var action = new Action(null, step);
    step.setAction(action);
    return action;
}

function addActionAndFocus() {
    $.when(addAction()).then(function (action) {
        listenEnterKeypressWhenFocusingOnDescription();
        $($(action.html[0]).find(".description")[0]).focus();
        autocompleteAllFields();
        setPlaceholderAction();
    });
}

function getTestCase(test, testcase, step) {
    window.location.href = "./TestCaseScript.jsp?test=" + test + "&testcase=" + testcase + "&step=" + step;
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

            var stepHtml = $("#stepList li.active");
            var stepData = stepHtml.data("item");

            var parser = document.createElement('a');
            parser.href = window.location.href;

            var new_uri = parser.pathname + "?test=" + GetURLParameter("test") + "&testcase=" + GetURLParameter("testcase") + "&step=" + stepData.sort;

            setModif(false);

            window.location.href = new_uri;
        },
        error: showUnexpectedError
    });
}

function drawProperty(property, testcaseinfo) {
    var doc = new Doc();
    var selectType = getSelectInvariant("PROPERTYTYPE", false, true);
    var selectDB = getSelectInvariant("PROPERTYDATABASE", false, true);
    var selectNature = getSelectInvariant("PROPERTYNATURE", false, true);
    var deleteBtn = $("<button class='col-lg-6 btn btn-danger btn-sm'></button>").append($("<span></span>").addClass("glyphicon glyphicon-trash"));
    var moreBtn = $("<button class='col-lg-6 btn btn-default btn-sm'></button>").append($("<span></span>").addClass("glyphicon glyphicon-chevron-down"));

    var propertyInput = $("<input onkeypress='return restrictCharacters(this, event, propertyNameRestriction);' id='propName' placeholder='" + doc.getDocLabel("page_testcasescript", "feed_propertyname") + "'>").addClass("form-control input-sm").val(property.property);
    var descriptionInput = $("<textarea rows='1' id='propDescription' placeholder='" + doc.getDocLabel("page_testcasescript", "feed_propertydescription") + "'>").addClass("form-control input-sm").val(property.description);
    var valueInput = $("<textarea rows='1' placeholder='" + doc.getDocLabel("page_applicationObject", "Value") + "'></textarea>").addClass("form-control input-sm").val(property.value1);
    var value2Input = $("<textarea rows='1' placeholder='" + doc.getDocLabel("page_applicationObject", "Value") + "'></textarea>").addClass("form-control input-sm").val(property.value2);
    var lengthInput = $("<input placeholder='" + doc.getDocLabel("page_testcasescript", "length") + "'>").addClass("form-control input-sm").val(property.length);
    var rowLimitInput = $("<input placeholder='" + doc.getDocLabel("page_testcasescript", "row_limit") + "'>").addClass("form-control input-sm").val(property.rowLimit);
    var table = $("#propTable");

    var content = $("<div class='row property list-group-item'></div>");
    var props = $("<div class='col-sm-11'></div>");
    var right = $("<div class='col-sm-1 propertyButtons'></div>");

    var row1 = $("<div class='row' name='masterProp' style='margin-top:10px;'></div>");
    var row2 = $("<div class='row' style='display:none;'></div>");
    var row3 = $("<div class='row' style='display:none;'></div>");
    var row4 = $("<div class='row'></div>");
    var row5 = $("<div class='row'></div>");
    var propertyName = $("<div class='col-sm-2 form-group has-feedback'></div>").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "property_field"))).append(propertyInput);
    var description = $("<div class='col-sm-4 form-group has-feedback'></div>").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "description_field"))).append(descriptionInput);
    var country = $("<div class='col-sm-10 form-group has-feedback'></div>").append(getTestCaseCountry(testcaseinfo.countryList, property.country));
    var type = $("<div class='col-sm-2 form-group has-feedback'></div>").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "type_field"))).append(selectType.val(property.type));
    var db = $("<div class='col-sm-2 form-group has-feedback'></div>").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "db_field"))).append(selectDB.val(property.database));
    var value = $("<div class='col-sm-4 form-group has-feedback'></div>").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "value1_field"))).append(valueInput);
    var value2 = $("<div class='col-sm-4 form-group has-feedback'></div>").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "value2_field"))).append(value2Input);
    var length = $("<div class='col-sm-2 form-group has-feedback'></div>").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "length_field"))).append(lengthInput);
    var rowLimit = $("<div class='col-sm-2 form-group has-feedback'></div>").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "rowlimit_field"))).append(rowLimitInput);
    var nature = $("<div class='col-sm-2 form-group has-feedback'></div>").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "nature_field"))).append(selectNature.val(property.nature));


    var selectAllBtn = $("<button></button>").addClass("btn btn-default btn-sm").append($("<span></span>").addClass("glyphicon glyphicon-check")).click(function(){country.find("input[type='checkbox']").prop('checked', true).trigger("change");});
    var selectNoneBtn = $("<button></button>").addClass("btn btn-default btn-sm").append($("<span></span>").addClass("glyphicon glyphicon-unchecked")).click(function(){country.find("input[type='checkbox']").prop('checked', false).trigger("change");});
    var btnRow = $("<div class='col-sm-2 form-group has-feedback'></div>").append(selectAllBtn).append(selectNoneBtn);

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

    value2Input.change(function () {
        property.value2 = $(this).val();
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
    row1.append(value2);
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
    var doc = new Doc();
    var propertyArray = [];

    var selectType = getSelectInvariant("PROPERTYTYPE", false, true).attr("disabled",true);
    var selectDB = getSelectInvariant("PROPERTYDATABASE", false, true).attr("disabled",true);
    var selectNature = getSelectInvariant("PROPERTYNATURE", false, true).attr("disabled",true);
    var table = $("#inheritedPropPanel");

    for (var index = 0; index < propList.length; index++) {
        var property = propList[index];
        propertyArray.push(propList[index].property);

        var test = property.fromTest;
        var testcase = property.fromTestCase;

        var moreBtn = $("<button class='col-sm-6 btn btn-default btn-sm'></button>").append($("<span></span>").addClass("glyphicon glyphicon-chevron-down"));
        var editBtn = $("<a href='./TestCaseScript.jsp?test=" + test + "&testcase=" + testcase + "&property=" + property.property + "' class='col-sm-6 btn btn-primary btn-sm'></a>").append($("<span></span>").addClass("glyphicon glyphicon-pencil"));

        var propertyInput = $("<input id='propName' placeholder='" + doc.getDocLabel("page_testcasescript", "feed_propertyname") + "' disabled>").addClass("form-control input-sm").val(property.property);
        var descriptionInput = $("<textarea rows='1' id='propDescription' placeholder='" + doc.getDocLabel("page_testcasescript", "feed_propertydescription") + "' disabled>").addClass("form-control input-sm").val(property.description);
        var valueInput = $("<textarea rows='1' placeholder='" + doc.getDocLabel("page_applicationObject", "Value") + "' disabled></textarea>").addClass("form-control input-sm").val(property.value1);
        var value2Input = $("<textarea rows='1' placeholder='" + doc.getDocLabel("page_applicationObject", "Value") + "' disabled></textarea>").addClass("form-control input-sm").val(property.value2);
        var lengthInput = $("<input placeholder='" + doc.getDocLabel("page_testcasescript", "length") + "' disabled>").addClass("form-control input-sm").val(property.length);
        var rowLimitInput = $("<input placeholder='" + doc.getDocLabel("page_testcasescript", "row_limit") + "' disabled>").addClass("form-control input-sm").val(property.rowLimit);

        var content = $("<div class='row property list-group-item disabled'></div>");
        var props = $("<div class='col-sm-11'></div>");
        var right = $("<div class='col-sm-1 propertyButtons'></div>");

        var row1 = $("<div class='row' name='masterProp' style='margin-top:10px;'></div>");
        var row2 = $("<div class='row' style='display:none;'></div>");
        var row3 = $("<div class='row' style='display:none;'></div>");
        var row4 = $("<div class='row'></div>");
        var row5 = $("<div class='row'></div>");
        var propertyName = $("<div class='col-sm-2 form-group has-feedback'></div>").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "property_field"))).append(propertyInput);
        var description = $("<div class='col-sm-4 form-group has-feedback'></div>").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "description_field"))).append(descriptionInput);
        var country = $("<div class='col-sm-10 form-group has-feedback'></div>").append(getTestCaseCountry(property.country, property.country, true));
        var type = $("<div class='col-sm-2 form-group has-feedback'></div>").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "type_field"))).append(selectType.clone().val(property.type));
        var db = $("<div class='col-sm-2 form-group has-feedback'></div>").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "db_field"))).append(selectDB.clone().val(property.database));
        var value = $("<div class='col-sm-4 form-group has-feedback'></div>").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "value1_field"))).append(valueInput);
        var value2 = $("<div class='col-sm-4 form-group has-feedback'></div>").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "value2_field"))).append(value2Input);
        var length = $("<div class='col-sm-2 form-group has-feedback'></div>").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "length_field"))).append(lengthInput);
        var rowLimit = $("<div class='col-sm-2 form-group has-feedback'></div>").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "rowlimit_field"))).append(rowLimitInput);
        var nature = $("<div class='col-sm-2 form-group has-feedback'></div>").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "nature_field"))).append(selectNature.clone().val(property.nature));


        var selectAllBtn = $("<button disabled></button>").addClass("btn btn-default btn-sm").append($("<span></span>").addClass("glyphicon glyphicon-check")).click(function(){country.find("input[type='checkbox']").prop('checked', true);});
        var selectNoneBtn = $("<button disabled></button>").addClass("btn btn-default btn-sm").append($("<span></span>").addClass("glyphicon glyphicon-unchecked")).click(function(){country.find("input[type='checkbox']").prop('checked', false);});
        var btnRow = $("<div class='col-sm-2 form-group has-feedback'></div>").append(selectAllBtn).append(selectNoneBtn);

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
        row1.append(value2);
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
        right.append(editBtn);

        content.append(props).append(right);
        table.append(content);
    }

    sortProperties("#inheritedPropPanel");
    return propertyArray;
}

function loadProperties(test, testcase, testcaseinfo, propertyToFocus) {

    return new Promise(function(resolve, reject) {
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
                var scope = undefined;
                if (propertyToFocus != undefined && propertyToFocus != null) {
                    $("#propertiesModal #propTable #propName").each(function (i) {
                        if ($(this).val() == propertyToFocus) {
                            scope = this;
                            $("#propertiesModal").on("shown.bs.modal", function (e) {
                                $(scope).focus();
                                $(scope).click();
                            })
                            $("#propertiesModal").modal("show");
                        }
                    });
                }

                resolve(array);

            },
            error: showUnexpectedError
        });
    });
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
    $(".testTestCase #description").text(info.shortDescription);
}

function changeLib() {
    setModif(true);
    var stepHtml = $("#stepList li.active");
    var stepData = stepHtml.data("item");
    if(stepData.inLibrary == "Y"){
        stepData.inLibrary = "N";
        $(this).removeClass("btn-dark");
    }else{
        stepData.inLibrary = "Y";
        $(this).addClass("btn-dark");
    }
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
        setModif(true);
        var step = {"inLibrary": "N",
            "objType": "step",
            "useStepTest": "",
            "useStepTestCase": "",
            "useStep": "N",
            "description": "",
            "useStepStep": -1,
            "actionList": [],
            "conditionOper": "always",
            "conditionVal1": "",
            "conditionVal2": ""
        };

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
        stepObj.html.trigger("click");
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
    }else{
        $("#stepHeader").hide();
        $("#addActionBottomBtn").hide();
        $("#addAction").attr("disabled",true);
    }
}

/** Modification Status **/

var getModif, setModif, initModification;
(function(){
    var isModif = false;
    getModif = function(){
        return isModif;
    };
    setModif = function(val){
        isModif = val;
        if(isModif == true && $("#saveScript").hasClass("btn-default")){
            $("#saveScript").removeClass("btn-default").addClass("btn-primary");
        }else if(isModif == false && $("#saveScript").hasClass("btn-primary")){
            $("#saveScript").removeClass("btn-primary").addClass("btn-default");
        }

    };
    initModification = function(){
        $(".panel-body input, .panel-body select").change(function(){
            setModif(true);
        })
    };
})();

/** LIBRARY STEP UTILY FUNCTIONS **/

function loadLibraryStep(search) {
    $("#lib").empty();
    showLoaderInModal("#addStepModal");
    $.ajax({
        url: "GetStepInLibrary",
        data: {system: getUser().defaultSystem},
        async: true,
        success: function (data) {
            var test = {};

            for (var index = 0; index < data.testCaseStepList.length; index++) {
                var step = data.testCaseStepList[index];

                if(search == undefined || search == "" || step.description.indexOf(search) > -1 || step.testCase.indexOf(search) > -1 || step.test.indexOf(search) > -1) {
                    if (!test.hasOwnProperty(step.test)) {
                        $("#lib").append($("<a></a>").addClass("list-group-item").attr("data-toggle", "collapse").attr("href", "[data-test='" + step.test + "']")
                                .text(step.test).prepend($("<span></span>").addClass("glyphicon glyphicon-chevron-right")));

                        var listGr = $("<div></div>").addClass("list-group collapse").attr("data-test", step.test);
                        $("#lib").append(listGr);

                        test[step.test] = {content : listGr, testCase: {}};
                    }
                    if((!test[step.test].testCase.hasOwnProperty(step.testCase))){
                        var listGrp = test[step.test].content;
                        listGrp.append($("<a></a>").addClass("list-group-item sub-item").attr("data-toggle", "collapse").attr("href", "[data-test='" + step.test + "'][data-testCase='" + step.testCase + "']")
                            .text(step.testCase).prepend($("<span></span>").addClass("glyphicon glyphicon-chevron-right")));

                        var listCaseGr = $("<div></div>").addClass("list-group collapse").attr("data-test", step.test).attr("data-testCase", step.testCase);
                        listGrp.append(listCaseGr);

                        test[step.test].testCase[step.testCase] = {content : listCaseGr, step: {}};
                    }
                    var listCaseGrp = test[step.test].testCase[step.testCase].content;
                    var listStepGrp = $("<a></a>").addClass("list-group-item sub-sub-item").attr("href", "#").text(step.description).data("stepInfo", step);
                    listCaseGrp.append(listStepGrp);
                    test[step.test].testCase[step.testCase].step[step.description] = listStepGrp;
                }
            }

            if(search != undefined && search != ""){
                $('#lib').find("div").toggleClass('in');
            }

            $('.list-group-item').unbind("click").on('click', function () {
                $('.glyphicon', this)
                        .toggleClass('glyphicon-chevron-right')
                        .toggleClass('glyphicon-chevron-down');
            });

            $("#addStepModal #search").unbind("input").on("input",function(e){
                var search = $(this).val();
                // Clear any previously set timer before setting a fresh one
                window.clearTimeout($(this).data("timeout"));
                $(this).data("timeout", setTimeout(function () {
                    loadLibraryStep(search);
                }, 500));
            });

            hideLoaderInModal("#addStepModal");
        }
    });
}

function loadApplicationObject(dataInit){
    return new Promise(function(resolve, reject){
        var array = [];
        $.ajax({
            url: "ReadApplicationObject?application="+dataInit.info.application,
            dataType: "json",
            success: function(data) {
                for(var i = 0; i<data.contentTable.length; i++) {
                    array.push(data.contentTable[i].object);
                }
                resolve(array);
            }
        });
    });
}

function showStepUsesLibraryInConfirmationModal(object){
    var doc = new Doc();
    $("#confirmationModal [name='buttonConfirm']").text("OK");
    $("#confirmationModal [name='buttonDismiss']").hide();
    $("#confirmationModal").on("hidden.bs.modal",function(){
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
            step: object.step,
            getUses: true
        },
        success: function(data) {
            var content = "";
            for(var i = 0; i<data.step.length; i++){
                content += "<a target='_blank' href='./TestCaseScript.jsp?test="+data.step[i].test+"&testcase="+data.step[i].testCase+"&step="+data.step[i].sort+"'>"+data.step[i].test+" - "+data.step[i].testCase+" - "+data.step[i].sort+" - "+data.step[i].description+"</a><br/>"
            }
            $("#confirmationModal #otherStepThatUseIt").empty().append(content);
        }
    });
    showModalConfirmation(function(){
        $('#confirmationModal').modal('hide');
    }, doc.getDocLabel("page_global", "warning"),
        doc.getDocLabel("page_testcasescript", "cant_detach_library") +
        "<br/>" +
        "<div id='otherStepThatUseIt' style='width:100%;'>" +
        "<div style='width:30px; margin-left: auto; margin-right: auto;'>" +
        "<span class='glyphicon glyphicon-refresh spin'></span>" +
        "</div>" +
        "</div>", "", "", "", "");
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
    setModif(true);
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
    this.conditionOper = json.conditionOper;
    this.conditionVal1 = json.conditionVal1;
    this.conditionVal2 = json.conditionVal2;
    this.inLibrary = json.inLibrary;
    this.isStepInUseByOtherTestCase = json.isStepInUseByOtherTestCase;
    this.actionList = [];
    this.setActionList(json.actionList);

    this.stepList = stepList;
    this.toDelete = false;

    this.html = $("<li></li>").addClass("list-group-item list-group-item-calm row").css("margin-left", "0px");
    this.textArea = $("<div></div>").addClass("col-lg-10").addClass("step-description").text(this.description);

}

Step.prototype.draw = function () {
    var scope = this;
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

    $("#stepPlus").unbind("click").click(function(){
        $("#stepHiddenRow").toggle();
        if($(this).find("span").hasClass("glyphicon-chevron-down")){
            $(this).find("span").removeClass("glyphicon-chevron-down").addClass("glyphicon-chevron-up");
        }else{
            $(this).find("span").removeClass("glyphicon-chevron-up").addClass("glyphicon-chevron-down");
        }
    });

    $("#stepList").append(htmlElement);
    $("#actionContainer").append(this.stepActionContainer);
};

Step.prototype.show = function () {
    var scope = this;
    var doc = new Doc();
    var object = $(this).data("item");

    $("#stepHeader").show();
    $("#addActionBottomBtn").show();

    for (var i = 0; i < object.stepList.length; i++) {
        var step = object.stepList[i];

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

    $("#isLib").unbind("click");
    if (object.inLibrary === "Y") {
        $("#isLib").addClass("btn-dark");
        if(object.isStepInUseByOtherTestCase){
            $("#isLib").click(function(){

                showStepUsesLibraryInConfirmationModal(object);

            });
        }else{
            $("#isLib").click(changeLib);
        }
    }else{
        $("#isLib").removeClass("btn-dark");
        $("#isLib").click(changeLib);
    }

    if (object.useStep === "Y") {
        $("#isLib").hide();
        $("#UseStepRow").html("(" + doc.getDocLabel("page_testcasescript", "imported_from") + " <a href='./TestCaseScript.jsp?test=" + object.useStepTest + "&testcase=" + object.useStepTestCase + "&step=" + object.useStepStepSort + "' >" + object.useStepTest + " - " + object.useStepTestCase + " - " + object.useStepStepSort + "</a>)").show();
        $("#UseStepRowButton").html("|").show();
        $("#addAction").prop("disabled",true);
        $("#addActionBottomBtn").hide();
        $("#isUseStep").show();
    } else {
        $("#isLib").show();
        $("#UseStepRow").html("").hide();
        $("#UseStepRowButton").html("").hide();
        $("#addAction").prop("disabled",false);
        $("#addActionBottomBtn").show();
        $("#isUseStep").hide();
    }

    if(object.toDelete){
        $("#contentWrapper").addClass("list-group-item-danger");
    }else{
        $("#contentWrapper").removeClass("list-group-item-danger");
    }

    var actionconditionVal1 = $("#stepConditionVal1");
    actionconditionVal1.css("width","100%");
    actionconditionVal1.on("change", function () {
        object.conditionVal1 = actionconditionVal1.val();
    });
    actionconditionVal1.val(object.conditionVal1);

    var actionconditionVal2 = $("#stepConditionVal2");
    actionconditionVal2.css("width","100%");
    actionconditionVal2.on("change", function () {
        object.conditionVal2 = actionconditionVal2.val();
    });
    actionconditionVal2.val(object.conditionVal2);

    var actionconditiononper = $("#stepConditionOper");
    actionconditiononper.replaceWith(getSelectInvariant("ACTIONCONDITIONOPER", false, true).css("width","100%").addClass("form-control input-sm").attr("id","stepConditionOper"));
    actionconditiononper = $("#stepConditionOper");
    actionconditiononper.on("change", function () {
        object.conditionOper = actionconditiononper.val();
        if(object.conditionOper != "ifPropertyExist"){
            actionconditionVal1.parent().hide();
            actionconditionVal2.parent().hide();
        }else{
            actionconditionVal1.parent().show();
            actionconditionVal2.parent().show();
        }
    });
    actionconditiononper.val(object.conditionOper).trigger("change");

    object.stepActionContainer.show();
    $("#stepDescription").unbind("change").change(function(){
        setModif(true);
        object.description = $(this).val();
    });

    $("#isUseStep").unbind("click").click(function(){
        setModif(true);
        if(object.useStep === "Y"){
            showModalConfirmation(function(){
                object.useStep = "N";
                object.useStepStep = -1;
                object.useStepTest = ";";
                object.useStepTestCase = "";
                saveScript();
            }, doc.getDocLabel("page_testcasescript", "unlink_useStep"), doc.getDocLabel("page_testcasescript", "unlink_useStep_warning"), "", "", "", "");
        }
    });

    $("#stepDescription").val(object.description);
    $("#stepInfo").show();
    $("#addActionContainer").show();
    $("#stepHeader").show()

    object.stepActionContainer.find("div.fieldRow div:nth-child(n+2) input").trigger("input");

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
    $("#stepDescription").val(description);
};

Step.prototype.setDelete = function () {
    this.toDelete = (this.toDelete) ? false : true;

    if($("#contentWrapper").hasClass("list-group-item-danger")){
        $("#contentWrapper").removeClass("list-group-item-danger");
    }else{
        $("#contentWrapper").removeClass("well").addClass("list-group-item-danger well")
    }

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
            step.html.removeClass("list-group-item-calm");
        } else {
            step.html.addClass("list-group-item-calm");
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
    json.conditionOper = this.conditionOper;
    json.conditionVal1 = this.conditionVal1;
    json.conditionVal2 = this.conditionVal2;

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
        this.conditionVal1 = json.conditionVal1;
        this.conditionVal2 = json.conditionVal2;
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
        this.conditionVal1 = "";
        this.conditionVal2 = "";
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
        addControlAndFocus(row);
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
    var doc = new Doc();
    var content = $("<div></div>").addClass("content col-lg-9");
    var firstRow = $("<div style='margin-top:15px;'></div>").addClass("fieldRow row form-group has-feedback");
    var secondRow = $("<div></div>").addClass("fieldRow row");
    var thirdRow = $("<div></div>").addClass("fieldRow row").hide();

    var actionList = $("<select></select>").addClass("form-control input-sm");
    var descField = $("<input>").addClass("description").addClass("form-control").prop("placeholder", doc.getDocLabel("page_testcasescript", "describe_action"));
    var objectField = $("<input>").attr("data-toggle","tooltip").attr("data-animation","false").attr("data-html","true").attr("data-container","body").attr("data-placement","top").attr("data-trigger","manual").attr("type","text").addClass("form-control input-sm");
    var propertyField = $("<input>").attr("data-toggle","tooltip").attr("data-animation","false").attr("data-html","true").attr("data-container","body").attr("data-placement","top").attr("data-trigger","manual").attr("type","text").addClass("form-control input-sm");

    var actionconditionval1 = $("<input>").attr("type","text").addClass("form-control input-sm");
    var actionconditionval2 = $("<input>").attr("type","text").addClass("form-control input-sm");
    var actionconditionoper = $("<select></select>").addClass("form-control input-sm");
    var forceExeStatusList = $("<select></select>").addClass("form-control input-sm");

    descField.val(this.description);
    descField.css("width","100%");
    descField.on("change", function () {
        obj.description = descField.val();
    });

    actionconditionval1.css("width","100%");
    actionconditionval1.on("change", function () {
        obj.conditionVal1 = actionconditionval1.val();
    });
    actionconditionval1.val(this.conditionVal1);

    actionconditionval2.css("width","100%");
    actionconditionval2.on("change", function () {
        obj.conditionVal2 = actionconditionval2.val();
    });
    actionconditionval2.val(this.conditionVal2);

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

    actionconditionoper = getSelectInvariant("ACTIONCONDITIONOPER", false, true).css("width","100%");
    actionconditionoper.on("change", function () {
        obj.conditionOper = actionconditionoper.val();
        if(obj.conditionOper != "ifPropertyExist"){
            actionconditionval1.parent().hide();
            actionconditionval2.parent().hide();
        }else{
            actionconditionval1.parent().show();
            actionconditionval2.parent().show();
        }
//        setPlaceholderAction();
    });
    actionconditionoper.val(this.conditionOper).trigger("change");

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
    secondRow.append($("<div></div>").addClass("col-lg-3 form-group has-feedback").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "action_field"))).append(actionList));
    secondRow.append($("<div></div>").addClass("col-lg-5 form-group has-feedback").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "value1_field"))).append(objectField));
    secondRow.append($("<div></div>").addClass("col-lg-4 form-group has-feedback").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "value2_field"))).append(propertyField));
    thirdRow.append($("<div></div>").addClass("col-lg-3 form-group has-feedback").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "condition_operation_field"))).append(actionconditionoper));
    thirdRow.append($("<div></div>").addClass("col-lg-4 form-group has-feedback").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "condition_parameter_field"))).append(actionconditionval1));
    thirdRow.append($("<div></div>").addClass("col-lg-4 form-group has-feedback").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "condition_parameter_field"))).append(actionconditionval2));
    thirdRow.append($("<div></div>").addClass("col-lg-3 form-group has-feedback").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "force_execution_field"))).append(forceExeStatusList));

    actionconditionoper.trigger("change");

    if (this.parentStep.useStep === "Y") {
        descField.prop("readonly", true);
        objectField.prop("readonly", true);
        propertyField.prop("readonly", true);
        actionList.prop("disabled", "disabled");
        forceExeStatusList.prop("disabled", "disabled");
        actionconditionoper.prop("disabled", "disabled");
        actionconditionval1.prop("readonly", true);
        actionconditionval2.prop("readonly", true);
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
    json.conditionVal1= this.conditionVal1;
    json.conditionVal2= this.conditionVal2;
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
        this.conditionOper = json.conditionOper;
        this.conditionVal1 = json.conditionVal1;
        this.conditionVal2 = json.conditionVal2;
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
        this.conditionOper = "always";
        this.conditionVal1 = "";
        this.conditionVal2 = "";
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
        setModif(true);
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

    if (this.parentStep.useStep === "Y") {
        supprBtn.attr("disabled",true);
    }

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
    var doc = new Doc();
    var content = $("<div></div>").addClass("content col-lg-9");
    var firstRow = $("<div style='margin-top:15px;'></div>").addClass("fieldRow row form-group has-feedback");
    var secondRow = $("<div></div>").addClass("fieldRow row");
    var thirdRow = $("<div></div>").addClass("fieldRow row").hide();

    var controlList = $("<select></select>").addClass("form-control input-sm").css("width", "100%");
    var descField = $("<input>").addClass("description").addClass("form-control").prop("placeholder", doc.getDocLabel("page_testcasescript", "describe_control"));
    var controlValueField = $("<input>").attr("data-toggle","tooltip").attr("data-animation","false").attr("data-html","true").attr("data-container","body").attr("data-placement","top").attr("data-trigger","manual").addClass("form-control input-sm").css("width", "100%");
    var controlPropertyField = $("<input>").attr("data-toggle","tooltip").attr("data-animation","false").attr("data-html","true").attr("data-container","body").attr("data-placement","top").attr("data-trigger","manual").addClass("form-control input-sm").css("width", "100%");

    var actionconditionval1 = $("<input>").attr("type","text").addClass("form-control input-sm");
    var actionconditionval2 = $("<input>").attr("type","text").addClass("form-control input-sm");
    var actionconditionoper = $("<select></select>").addClass("form-control input-sm");
    var fatalList = $("<select></select>").addClass("form-control input-sm");

    descField.val(this.description);
    descField.css("width","100%");
    descField.on("change", function () {
        obj.description = descField.val();
    });

    actionconditionval1.css("width","100%");
    actionconditionval1.on("change", function () {
        obj.conditionVal1 = actionconditionval1.val();
    });
    actionconditionval1.val(this.conditionVal1);

    actionconditionval2.css("width","100%");
    actionconditionval2.on("change", function () {
        obj.conditionVal2 = actionconditionval2.val();
    });
    actionconditionval2.val(this.conditionVal2);

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
    secondRow.append($("<div></div>").addClass("col-lg-4 form-group has-feedback").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "control_field"))).append(controlList));
    secondRow.append($("<div></div>").addClass("col-lg-4 form-group has-feedback").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "value1_field"))).append(controlValueField));
    secondRow.append($("<div></div>").addClass("col-lg-4 form-group has-feedback").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "value2_field"))).append(controlPropertyField));

    thirdRow.append($("<div></div>").addClass("col-lg-4 form-group has-feedback").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "condition_parameter_field"))).append(actionconditionval1));
    thirdRow.append($("<div></div>").addClass("col-lg-4 form-group has-feedback").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "condition_parameter_field"))).append(actionconditionval2));
    thirdRow.append($("<div></div>").addClass("col-lg-3 form-group has-feedback").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "fatal_field"))).append(fatalList));


    actionconditionoper = getSelectInvariant("ACTIONCONDITIONOPER", false, true).css("width","100%");
    actionconditionoper.on("change", function () {
        obj.conditionOper = actionconditionoper.val();
        if(obj.conditionOper != "ifPropertyExist"){
            actionconditionval1.parent().hide();
            actionconditionval2.parent().hide();
        }else{
            actionconditionval1.parent().show();
            actionconditionval2.parent().show();
        }
    });
    actionconditionoper.val(this.conditionOper).trigger("change");

    thirdRow.prepend($("<div></div>").addClass("col-lg-3 form-group has-feedback").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "condition_operation_field"))).append(actionconditionoper));


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
    json.conditionOper = this.conditionOper;
    json.conditionVal1 = this.conditionVal1;
    json.conditionVal2 = this.conditionVal2;
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
                    addActionAndFocus();
                } else {
                    //if description is empty, create action or control depending on field
                    if ($(field).closest(".step-action").hasClass("action")) {
                        var newAction = $(field).closest(".action-group");
                        var oldAction = newAction.prev().find(".step-action.row.action").last();
                        newAction.remove();
                        addControlAndFocus(oldAction);
                    } else {
                        var newAction = $(field).closest(".step-action");
                        newAction.remove();
                        addActionAndFocus();
                    }
                }
            }
        });
    });
}

function addControl(action) {
    setModif(true);
    var control = new Control(null, action);
    action.setControl(control);
    return control;
}

function addControlAndFocus(oldAction) {
    $.when(addControl(oldAction.data("item"))).then(function (action) {
        listenEnterKeypressWhenFocusingOnDescription();
        $($(action.html[0]).find(".description")[0]).focus();
        setPlaceholderControl();
        autocompleteAllFields();
    });
}

var autocompleteAllFields, getTags, setTags;
(function() {
    //var accessible only in closure
    var TagsToUse = [];
    var tcInfo;
    var test;
    var testcase;
    getTags = function(){
        return TagsToUse;
    };
    setTags = function(tags){
        TagsToUse = tags;
    };
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

        autocompleteVariable("#propTable .property .row:nth-child(1) textarea, div.step-action .content div.fieldRow div:nth-child(n+2) input", TagsToUse);

        $("div.step-action .content div.fieldRow div:nth-child(n+2) input").each(function(i,e){
            $(e).unbind("input").on("input",function(ev){
                var name = undefined;
                var nameNotExist = undefined;
                var objectNotExist = false;
                var typeNotExist = undefined;
                var doc = new Doc();
                var checkObject = [];
                var betweenPercent = $(e).val().match(new RegExp(/%[^%]*%/g));
                if(betweenPercent != null && betweenPercent.length > 0){
                    var i = betweenPercent.length - 1;
                    while(i>=0){
                        var findname = betweenPercent[i].match(/\.[^\.]*(\.|.$)/g);
                        if(betweenPercent[i].startsWith("%object.") && findname != null && findname.length > 0) {
                            name = findname[0];
                            name = name.slice( 1, name.length - 1 );

                            $(e).parent().parent().parent().parent().find("#ApplicationObjectImg").attr("src","ReadApplicationObjectImage?application=" + tcInfo.application + "&object=" + name + "&time=" + new Date().getTime());
                            
                            if(TagsToUse[1].array.indexOf(name) < 0){
                                console.log(TagsToUse[1].array);
                                console.log(name);
                                objectNotExist = true;
                                nameNotExist = name;
                                typeNotExist = "applicationobject";
                            }
                        } else if(betweenPercent[i].startsWith("%property.") && findname != null && findname.length > 0) {
                            name = findname[0];
                            name = name.slice( 1, name.length - 1);

                            if(TagsToUse[2].array.indexOf(name) < 0){
                                objectNotExist = true;
                                nameNotExist = name;
                                typeNotExist = "property";
                            }
                        }
                        i--;
                    }
                }
                if(objectNotExist){
                    if(typeNotExist == "applicationobject") {
                        var newTitle = "<a style='color: #fff;' href='#' onclick='addApplicationObjectModalClick(undefined, \"" + nameNotExist + "\",\"" + tcInfo.application + "\")'><span class='glyphicon glyphicon-exclamation-sign' aria-hidden='true'></span>" + doc.getDocLabel("page_global", "warning") + ": " + nameNotExist + " " + doc.getDocLabel("page_testcasescript", "not_application_object") + "</a>";
                        if (newTitle != $(e).attr('data-original-title')) {
                            $(e).attr('data-original-title', newTitle).tooltip('fixTitle').tooltip('show');
                        }else {
                            $(e).tooltip('show');
                        }
                    }else if(typeNotExist == "property"){
                        //TODO better way to add property
                        var newTitle = "<a style='color: #fff;' href='#' onclick=\"$('#manageProp').click();$('#addProperty').click();$('#propTable input#propName:last-child').val('" + nameNotExist + "').trigger('change');\"><span class='glyphicon glyphicon-exclamation-sign' aria-hidden='true'></span> " + doc.getDocLabel("page_global", "warning") + " : " + nameNotExist + " " + doc.getDocLabel("page_testcasescript", "not_property") + "</a>";
                        if (newTitle != $(e).attr('data-original-title')) {
                            $(e).attr('data-original-title', newTitle).tooltip('fixTitle').tooltip('show');
                        }else{
                            $(e).tooltip('show');
                        }
                    }
                }else{
                    $(e).tooltip('destroy');
                }
            });
        }).trigger("input");
    };
})();

editPropertiesModalClick = function(test, testcase, info, propertyToAdd, propertyToFocus) {
    $("#propTable").empty();
    loadProperties(test, testcase, info, propertyToFocus).then(function(){
        autocompleteAllFields();
    });
    if (propertyToAdd != undefined && propertyToAdd != null) {
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
};

function editPropertiesModalSaveHandler(){
    clearResponseMessage($('#propertiesModal'));
    var doc = new Doc();

    var properties = $("#propTable [name='masterProp']");
    var propArr = [];
    var propertyWithoutCountry = false;
    for (var i = 0; i < properties.length; i++) {
        if($(properties[i]).data("property").country.length <= 0){
            propertyWithoutCountry = true;
        }
        propArr.push($(properties[i]).data("property"));
    }
    var saveProp = function() {
        showLoaderInModal('#propertiesModal');
        $.ajax({
            url: "UpdateTestCaseProperties1",
            async: true,
            method: "POST",
            data: {
                informationInitialTest: GetURLParameter("test"),
                informationInitialTestCase: GetURLParameter("testcase"),
                informationTest: GetURLParameter("test"),
                informationTestCase: GetURLParameter("testcase"),
                propArr: JSON.stringify(propArr)
            },
            success: function (data) {
                var Tags = getTags();

                var array = [];

                for(var i = 0; i<propArr.length; i++){
                    array.push(propArr[i].property);
                }

                for(var i = 0; i < Tags.length; i++){
                    if(Tags[i].regex == "%property\\."){
                        Tags[i].array = array;
                    }
                }

                hideLoaderInModal('#propertiesModal');
                if (getAlertType(data.messageType) === 'success') {
                    $("div.step-action .content div.fieldRow div:nth-child(n+2) input").trigger("input");
                    showMessage(data);
                    $('#propertiesModal').modal('hide');
                } else {
                    showMessage(data, $('#propertiesModal'));
                }
            },
            error: showUnexpectedError
        });
    };

    if(propertyWithoutCountry){
        showModalConfirmation(function(){
            $('#confirmationModal').modal('hide');
            saveProp();
        }, doc.getDocLabel("page_global", "btn_savetableconfig"), doc.getDocLabel("page_testcasescript", "warning_no_country"), "", "", "", "");
    }else{
        saveProp();
    }
}

function setPlaceholderAction() {
    /**
     * Todo : GetFromDatabase
     */
    var placeHoldersList = {"fr": [
        {"type": "Unknown", "object": null, "property": null},
        {"type": "keypress", "object": "[opt] Chemin vers l'élement à cibler", "property": ""},
        {"type": "hideKeyboard", "object": null, "property": null},
        {"type": "swipe", "object": "Action (UP DOWN LEFT RIGHT CUSTOM...)", "property": "Direction x;y;z;y"},
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
        {"type": "calculateProperty", "object": "Nom d'une Proprieté", "property": "[opt] Nom d'une autre propriété"},
        {"type": "doNothing", "object": null, "property": null},
        {"type": "getPageSource", "object": null, "property": null}
    ], "en": [
        {"type": "Unknown", "object": null, "property": null},
        {"type": "keypress", "object": "[opt] Element path", "property": ""},
        {"type": "hideKeyboard", "object": null, "property": null},
        {"type": "swipe", "object": "Action (UP DOWN LEFT RIGHT CUSTOM...)", "property": "Direction x;y;z;y"},
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
        {"type": "calculateProperty", "object": "Property Name", "property": "[opt] Name of an other property"},
        {"type": "doNothing", "object": null, "property": null},
        {"type": "getPageSource", "object": null, "property": null}
    ]};

    var user = getUser();
    var placeHolders = placeHoldersList[user.language];

    $('select#actionSelect option:selected').each(function (i, e) {
        for (var i = 0; i < placeHolders.length; i++) {
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
            {"type": "verifyStringEqual", "controlValue": "String2", "controlProp": "String1", "fatal": ""},
            {"type": "verifyStringDifferent", "controlValue": "String2", "controlProp": "String1", "fatal": ""},
            {"type": "verifyStringGreater", "controlValue": "String2 ex : AAA", "controlProp": "String1 ex: ZZZ", "fatal": ""},
            {"type": "verifyStringMinor", "controlValue": "String2 ex : ZZZ", "controlProp": "String1 ex: AAA", "fatal": ""},
            {"type": "verifyStringContains", "controlValue": "String2 ex : toto", "controlProp": "String1 ex : ot", "fatal": ""},
            {"type": "verifyIntegerEquals", "controlValue": "Integer2", "controlProp": "Integer1", "fatal": ""},
            {"type": "verifyIntegerDifferent", "controlValue": "Integer2", "controlProp": "Integer1", "fatal": ""},
            {"type": "verifyIntegerGreater", "controlValue": "Integer2 ex : 10", "controlProp": "Integer1 ex : 20", "fatal": ""},
            {"type": "verifyIntegerMinor", "controlValue": "Integer2 ex : 20", "controlProp": "Integer1 ex : 10", "fatal": ""},
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
            {"type": "getPageSource", "controlValue": null, "controlProp": null, "fatal": null}
        ], "en": [
            {"type": "Unknown", "controlValue": null, "controlProp": null, "fatal": null},
            {"type": "verifyStringEqual", "controlValue": "String2", "controlProp": "String1", "fatal": ""},
            {"type": "verifyStringDifferent", "controlValue": "String2", "controlProp": "String1", "fatal": ""},
            {"type": "verifyStringGreater", "controlValue": "String2 ex : AAA", "controlProp": "String1 ex: ZZZ", "fatal": ""},
            {"type": "verifyStringMinor", "controlValue": "String2 ex : ZZZ", "controlProp": "String1 ex: AAA", "fatal": ""},
            {"type": "verifyStringContains", "controlValue": "String2 ex : toto", "controlProp": "String1 ex : ot", "fatal": ""},
            {"type": "verifyIntegerEquals", "controlValue": "Integer2", "controlProp": "Integer1", "fatal": ""},
            {"type": "verifyIntegerDifferent", "controlValue": "Integer2", "controlProp": "Integer1", "fatal": ""},
            {"type": "verifyIntegerGreater", "controlValue": "Integer2 ex : 10", "controlProp": "Integer1 ex : 20", "fatal": ""},
            {"type": "verifyIntegerMinor", "controlValue": "Integer2 ex : 20", "controlProp": "Integer1 ex : 10", "fatal": ""},
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
            {"type": "getPageSource", "controlValue": null, "controlProp": null, "fatal": null}
        ]};

    var user = getUser();
    var placeHolders = placeHoldersList[user.language];

    $('select#controlSelect option:selected').each(function (i, e) {

        for (var i = 0; i < placeHolders.length; i++) {
            if (placeHolders[i].type === e.value) {
                if (placeHolders[i].controlProp !== null) {
                    $(e).parent().parent().next().show();
                    $(e).parent().parent().next().find('label').text(placeHolders[i].controlProp);
                } else {
                    $(e).parent().parent().next().hide();
                }
                if (placeHolders[i].controlValue !== null) {
                    $(e).parent().parent().next().next().show();
                    $(e).parent().parent().next().next().find('label').text(placeHolders[i].controlValue);
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
    var placeHolders = placeHoldersList[user.language];


    $('div[class="rowProperty form-inline"] option:selected').each(function (i, e) {
        for (var i = 0; i < placeHolders.length; i++) {
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
