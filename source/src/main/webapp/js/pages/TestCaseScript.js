/*
 * Cerberus Copyright (C) 2013 - 2017 cerberustesting
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

/* global getSelectInvariant */

var canUpdate = false;
var allDelete = false;
var Tags = [];


$.when($.getScript("js/global/global.js")
        , $.getScript("js/global/autocomplete.js")
        , $.getScript("js/testcase/action.js")
        , $.getScript("js/testcase/property.js")
        , $.getScript("js/testcase/condition.js")
        , $.getScript("js/testcase/control.js")
        ).then(function () {
    $(document).ready(function () {
        initModalDataLib();
        $("#nav-property").on('mouseenter', 'a', function (ev) {
            try {
                $(this).find("button").show();
            } catch (e) {
            }

        }).on('mouseleave', 'a', function (ev) {
            try {
                $(this).find("button").hide();

            } catch (e) {
            }
        });
        $('#propName').trigger("change");

        $('#propName').change(function () {
            openModalAppServiceFromHere();
        });
        $('#createApplicationObjectButton').click(function () {
            openModalApplicationObject(undefined, undefined, "ADD", "testCaseScript");

        });
        $(window).bind('beforeunload', function () {
            if (getModif()) {
                return true; // Display alert Message that a modification has
                // been done
            }
        });

        var doc = new Doc();
        var steps = [];

        // Load invariant list into local storage.
        getSelectInvariant("SRVTYPE", false, false);
        getSelectInvariant("SRVMETHOD", false, false);
        getSelectInvariant("ACTION", false, false);
        getSelectInvariant("CONTROL", false, false);
        getSelectInvariant("CTRLFATAL", false, false);
        getSelectInvariant("PROPERTYTYPE", false, false);
        getSelectInvariant("PROPERTYDATABASE", false, false);
        getSelectInvariant("PROPERTYNATURE", false, false);
        getSelectInvariant("ACTIONFATAL", false, false);
        getSelectInvariant("STEPLOOP", false, false);
        getSelectInvariant("STEPCONDITIONOPERATOR", false, false);
        bindToggleCollapse();
        var test = GetURLParameter("test");
        var testcase = GetURLParameter("testcase");
        var step = GetURLParameter("step");
        var property = GetURLParameter("property");
        var tabactive = GetURLParameter("tabactive");
        var oneclickcreation = GetURLParameter("oneclickcreation");
        displayHeaderLabel(doc);
        displayGlobalLabel(doc);
        displayFooter(doc);
        displayPageLabel(doc);
        $("#addStepModal [name='buttonAdd']").html(doc.getDocLabel("page_global", "btn_add"));


        fillTestAndTestCaseSelect(".testTestCase #test", "#testCaseSelect", test, testcase, false);

        $("#testCaseSelect").bind("change", function (event) {
            window.location.href = "./TestCaseScript.jsp?test=" + encodeURI(test) + "&testcase=" + encodeURI($(this).val());
        });
        $(".testTestCase #test").bind("change", function (event) {
            window.location.href = "./TestCaseScript.jsp?test=" + encodeURI($(this).val());
        });

        if (test !== null && testcase !== null) {
            // Edit TestCase open the TestCase Modal
            $("#editTcInfo").click(function () {
                openModalTestCase(test, testcase, "EDIT");
                $('#editTestCaseModal').on("hidden.bs.modal", function (e) {
                    $('#editTestCaseModal').unbind("hidden.bs.modal");
                    var t = $('#editTestCaseModal').find("#test option:selected");
                    var tc = $('#editTestCaseModal').find("#testCase");
                    if (!((t.val() === test) && (tc.val() === testcase))) {
                        // Key was modified.
                        if ($('#editTestCaseModal').data("Saved")) {
                            // Modal confirm that change was OK.
                            $('#editTestCaseModal').data("Saved", undefined);
                            window.location = "./TestCaseScript.jsp?test=" + encodeURI(t.val()) + "&testcase=" + encodeURI(tc.val());
                        }

                    }
                    $(".testTestCase #test").select2({width: "100%"}).next().css("margin-bottom", "7px");
                });
            });

            $("#TestCaseButton").show();
            $("#tcBody").show();

            var stepsObject;
            var testcaseObject;
            let application;
            let description;
            $.ajax({
                url: "ReadTestCase",
                data: {test: test, testCase: testcase, withSteps: true, system: getSys()},
                dataType: "json",
                success: function (data) {

                    // manage error
                    if (data.messageType !== undefined && data.messageType === "KO") {
                        showUnexpectedError(null, "ERROR", data.message);
                        return;
                    }

                    // Save history entries
                    saveHistory(getHistoryTestcase(data.contentTable[0]), "historyTestcases", 5);
                    refreshHistoryMenu();

                    canUpdate = data.hasPermissionsUpdate;
                    loadLibraryStep(undefined, data.contentTable[0].system);

                    application = data.contentTable[0].application;
                    description = data.contentTable[0].description;
                    testcaseObject = data.contentTable[0];
                    loadTestCaseInfo(testcaseObject);
                    stepsObject = testcaseObject.steps;
                    sortData(stepsObject);
                    testcaseObject.properties.inheritedProperties.sort(function (a, b) {
                        return compareStrings(a.property, b.property);
                    });
                    createSteps(stepsObject, steps, step, data.hasPermissionsUpdate, data.hasPermissionsStepLibrary);
                    drawInheritedProperty(data.contentTable[0].properties.inheritedProperties);

                    var configs = {
                        'system': true,
                        'object': testcaseObject.application,
                        'property': data,
                        'identifier': true
                    };
                    var context = data;
                    initTags(configs, context).then(function (tags) {
                        autocompleteAllFields(configs, context, tags);
                    });
                    loadPropertiesAndDraw(test, testcase, testcaseObject, property, data.hasPermissionsUpdate);

                    // Manage authorities when data is fully loadable.
                    $("#deleteTestCase").attr("disabled", !data.hasPermissionsDelete);
                    $("#addStep").attr("disabled", !data.hasPermissionsUpdate);
                    $("#duplicateStep").attr("disabled", !data.hasPermissionsUpdate);
                    $("#deleteStep").attr("disabled", !data.hasPermissionsUpdate);
                    $("#saveScript").attr("disabled", !data.hasPermissionsUpdate);
                    $("#addActionBottom").attr("disabled", !data.hasPermissionsUpdate);
                    $("#addProperty").attr("disabled", !data.hasPermissionsUpdate);

                    // Button Add Property insert a new Property
                    $("#addProperty").click(function () {

                        if (testcaseObject.countries.length <= 0) {
                            showMessageMainPage("danger", doc.getDocLabel("page_testcasescript", "warning_nocountry"), false);

                        } else {

                            // Store the current saveScript button status and
                            // disable it
                            var saveScriptOldStatus = $("#saveScript").attr("disabled");
                            $("#saveScript").attr("disabled", true);

                            let propIndex = $("#propTable #masterProp").length;
                            var newProperty = {
                                property: "PROP-" + propIndex,
                                description: "",
                                countries: [...testcaseObject.countries],
                                type: "text",
                                database: "",
                                value1: "",
                                value2: "",
                                length: 0,
                                rowLimit: 0,
                                cacheExpire: 0,
                                nature: "STATIC",
                                retryNb: "",
                                retryPeriod: "",
                                rank: 1,
                                toDelete: false
                            };

                            var prop = drawProperty(newProperty, testcaseObject, true, document.getElementsByClassName("property").length);
                            setPlaceholderProperty(prop[0], prop[1]);

                            $(prop[0]).find("#propName").focus();

                            setModif(true);

                            // Restore the saveScript button status
                            $("#saveScript").attr("disabled", typeof saveScriptOldStatus !== typeof undefined && saveScriptOldStatus !== false);
                        }

                    });

                    $('[data-toggle="tooltip"]').tooltip();

                    $('a[data-toggle="tab"]').on('shown.bs.tab', function (e) {
                        initModification();
                    });
                },
                error: showUnexpectedError
            });

            $("#addStep").click({steps: steps}, function (event) {
                // Store the current saveScript button status and disable it
                var saveScriptOldStatus = $("#saveScript").attr("disabled");
                $("#saveScript").attr("disabled", true);

                // Really do add step action
                addStep(event);

                // Restore the saveScript button status
                $("#saveScript").attr("disabled", typeof saveScriptOldStatus !== typeof undefined && saveScriptOldStatus !== false);
            });

//            $("#duplicateStep").click({steps: steps}, function (event) {
//                // Store the current saveScript button status and disable it
//                var saveScriptOldStatus = $("#saveScript").attr("disabled");
//                $("#saveScript").attr("disabled", true);
//
//                // Really do add step action
////                console.info(event);
//                duplicateStep(event);
//
//                // Restore the saveScript button status
//                $("#saveScript").attr("disabled", typeof saveScriptOldStatus !== typeof undefined && saveScriptOldStatus !== false);
//            });

            $('#addStepModal').on('hidden.bs.modal', function () {
                $("#importDetail").find("[name='importInfo']").removeData("stepInfo");
                $("[name='importInfo']").empty();
                $("#addStepModal #description").val("");
                $("#useStep").prop("checked", false);
                $("#importDetail").hide();
                $("#importDetail div.row").remove();
                $(".sub-sub-item.selected").each(function (idx, element) {
                    $(element).removeClass("selected");
                    $(element).find("[name='idx']").remove();
                });
                importInfoIdx = 0;
            });

            $("#deleteStep").click(function () {

                var step = $("#steps .active").data("item");

                if (step.isStepInUseByOtherTestCase) {
                    showStepUsesLibraryInConfirmationModal(step);
                } else {
                    setModif(true);
                    step.setDelete();
                }
            });

            $("#addAction").click(function () {
                addActionAndFocus();
            });

            // CONTEXT SAVE MENU
            $("#saveScript").click(saveScript);
            $("#saveScriptAs").click(function () {
                openModalTestCase(test, testcase, "DUPLICATE");
                $('#editTestCaseModal').on("hidden.bs.modal", function (e) {
                    $('#editTestCaseModal').unbind("hidden.bs.modal");
                    var t = $('#editTestCaseModal').find("#test option:selected");
                    var tc = $('#editTestCaseModal').find("#testCase");
                    if ($('#editTestCaseModal').data("Saved")) {
                        $('#editTestCaseModal').data("Saved", undefined);
                        window.location = "./TestCaseScript.jsp?test=" + t.val() + "&testcase=" + tc.val();
                    }
                    $(".testTestCase #test").select2({width: "100%"}).next().css("margin-bottom", "7px");
                });
            });
            $("#deleteTestCase").click(function () {
                removeTestCaseClick(test, testcase);
            });

            // CONTEXT GOTO & RUN MENU
            $("#seeLogs").parent().attr("href", "./LogEvent.jsp?Test=" + encodeURI(test) + "&TestCase=" + encodeURI(testcase));
            $("#seeTest").parent().attr("href", "./TestCaseList.jsp?test=" + encodeURI(test));
            //$("#runTestCase").parent().attr("href", "./RunTests.jsp?test=" + encodeURI(test) + "&testcase=" + encodeURI(testcase));
            $("#runTestCase").on('click', function () {
                openModalExecutionSimple(application, test, testcase, description);
            });

            $.ajax({
                url: "ReadTestCaseExecution",
                data: {test: test, testCase: testcase, system: getSys()},
                dataType: "json",
                success: function (data) {
                    if (!jQuery.isEmptyObject(data.contentTable)) {
                        $("#seeLastExecUniq").parent().attr("href", "./TestCaseExecution.jsp?executionId=" + encodeURI(data.contentTable.id));
                        $("#seeLastExec").parent().attr("href", "./TestCaseExecutionList.jsp?Test=" + encodeURI(test) + "&TestCase=" + encodeURI(testcase));
                        $("#rerunTestCase").attr("title", "Last Execution was " + data.contentTable.controlStatus + " in " + data.contentTable.env + " in " + data.contentTable.country + " on " + data.contentTable.end);
                        $("#rerunTestCase").parent().attr("href", "./RunTests.jsp?test=" + encodeURI(test) + "&testcase=" + encodeURI(testcase) + "&country=" + encodeURI(data.contentTable.country) + "&environment=" + encodeURI(data.contentTable.env));
                        $("#rerunFromQueue").attr("title", "Last Execution was " + data.contentTable.controlStatus + " in " + data.contentTable.env + " in " + data.contentTable.country + " on " + data.contentTable.end);
                        if (data.contentTable.queueId > 0) {
                            $("#rerunFromQueue").click(function () {
                                openModalTestCaseExecutionQueue(data.contentTable.queueId, "DUPLICATE");
                            });
                        } else {
                            $("#rerunFromQueue").attr("disabled", true);
                        }
                        $("#rerunFromQueueandSee").attr("title", "Last Execution was " + data.contentTable.controlStatus + " in " + data.contentTable.env + " in " + data.contentTable.country + " on " + data.contentTable.end);
                        if (data.contentTable.queueId > 0) {
                            $("#rerunFromQueueandSee").click(function () {
                                triggerTestCaseExecutionQueueandSeeFromTC(data.contentTable.queueId);
                            });
                        } else {
                            $("#rerunFromQueueandSee").attr("disabled", true);
                        }
                    } else {
                        $("#seeLastExecUniq").attr("disabled", true);
                        $("#seeLastExec").attr("disabled", true);
                        $("#rerunTestCase").attr("disabled", true);
                        $("#rerunFromQueue").attr("disabled", true);
                        $("#rerunFromQueueandSee").attr("disabled", true);
                    }
                },
                error: showUnexpectedError
            });
            var height = $("nav.navbar.navbar-inverse.navbar-static-top").outerHeight(true) + $("div.alert.alert-warning").outerHeight(true) + $(".page-title-line").outerHeight(true) - 10;

            $("#divPanelDefault").affix({offset: {top: height}});

            var wrap = $(window);

            wrap.on("scroll", function (e) {
                $(".affix").width($("#page-layout").width() - 3);
            });

            if (tabactive !== null) {
                $("a[name='" + tabactive + "']").click();
            }
        }
        // close all Navbar menu
        closeEveryNavbarMenu();

        $('[data-toggle="popover"]').popover({
            'placement': 'auto',
            'container': 'body'
        }
        );

        // open Run navbar Menu
        openNavbarMenu("navMenuTest");

        if (oneclickcreation === "true") {
            var message = "TestCase has been successfully created.";
            showMessageMainPage("success", message, false, 2000);

            $("#runTestCasePopover").attr('data-toggle', 'popover');
            $("#runTestCasePopover").attr('data-trigger', 'manual');
            $("#runTestCasePopover").attr('title', doc.getDocLabel("page_testcasescript", "runtestcasepopover_title"));
            $("#runTestCasePopover").attr('data-placement', 'bottom');
            $("#runTestCasePopover").attr('data-container', 'body');
            $("#runTestCasePopover").attr("data-content", doc.getDocLabel("page_testcasescript", "runtestcasepopover_content"));

            $("#runTestCasePopover").popover('show');
            $("#runTestCase").attr('style', 'margin-left: 5px; background-color:#5cb85c; color:white');

            $("#runTestCasePopover").fadeTo(5000, 1, function () {
                $("#runTestCasePopover").popover('hide');
                $("#runTestCase").attr('style', 'margin-left: 5px;');
            });


        }



    });
});

function getHistoryTestcase(object) {
    var result = {};
    result.id = object.test + '-' + object.testcase;
    result.test = object.test;
    result.testcase = object.testcase;
    return result;
}

function displayPageLabel(doc) {
    $("h1.page-title-line").html(doc.getDocLabel("page_testcasescript", "testcasescript_title"));
    $("#pageTitle").html(doc.getDocLabel("page_testcasescript", "testcasescript_title"));
    $("#nav-execution #list-wrapper #stepsWrapper h3").html(doc.getDocLabel("page_testcasescript", "steps_title"));
    $("#nav-execution #list-wrapper #tcButton h3").html(doc.getDocLabel("page_global", "columnAction"));
    $("#nav-execution #list-wrapper #deleteButton h3").html(doc.getDocLabel("page_global", "columnAction") + " " + doc.getDocLabel("page_header", "menuTestCase"));

    // CONTEXT MENU
    $("#btnGroupDrop1").html("<span class='glyphicon glyphicon-option-horizontal'></span>");
    $("#seeLastExecUniq").attr('data-toggle', 'tooltip').attr('data-original-title', doc.getDocDescription("page_testcasescript", "see_lastexecuniq")).html("<span class='glyphicon glyphicon-saved'></span> " + doc.getDocLabel('page_testcasescript', 'see_lastexecuniq'));
    $("#seeLastExec").attr('data-toggle', 'tooltip').attr('data-original-title', doc.getDocDescription("page_testcasescript", "see_lastexec")).html("<span class='glyphicon glyphicon-list'></span> " + doc.getDocLabel("page_testcasescript", "see_lastexec"));
    $("#seeTest").attr('data-toggle', 'tooltip').attr('data-original-title', doc.getDocDescription("page_testcasescript", "see_test")).html("<span class='glyphicon glyphicon-list'></span> " + doc.getDocLabel("page_testcasescript", "see_test"));
    $("#seeLogs").attr('data-toggle', 'tooltip').attr('data-original-title', doc.getDocDescription("page_testcasescript", "see_logs")).html("<span class='glyphicon glyphicon-list'></span> " + doc.getDocLabel("page_testcasescript", "see_logs"));
    $("#btnGroupDrop2").html(doc.getDocLabel("page_testcasescript", "run") + " <span class='caret'></span>");
    $("#runTestCase").attr('data-toggle', 'tooltip').attr('data-original-title', doc.getDocDescription("page_testcasescript", "run_testcase")).html("<span class='glyphicon glyphicon-play'></span> " + doc.getDocLabel("page_testcasescript", "run_testcase"));
    $("#rerunFromQueueandSee").attr('data-toggle', 'tooltip').attr('data-original-title', doc.getDocDescription("page_testcasescript", "rerunqueueandsee_testcase")).html("<span class='glyphicon glyphicon-forward'></span> " + doc.getDocLabel("page_testcasescript", "rerunqueueandsee_testcase"));
    $("#editTcInfo").attr('data-toggle', 'tooltip').attr('data-original-title', doc.getDocDescription("page_testcasescript", "edit_testcase")).html("<span class='glyphicon glyphicon-pencil'></span> " + doc.getDocLabel("page_testcasescript", "edit_testcase"));
    $("#saveScript").attr('data-toggle', 'tooltip').attr('data-original-title', doc.getDocDescription("page_testcasescript", "save_script")).html("<span class='glyphicon glyphicon-floppy-disk'></span> " + doc.getDocLabel("page_testcasescript", "save_script"));
    $("#saveScriptAs").attr('data-toggle', 'tooltip').attr('data-original-title', doc.getDocDescription("page_testcasescript", "saveas_script")).html("<span class='glyphicon glyphicon-floppy-disk'></span> " + doc.getDocLabel("page_testcasescript", "saveas_script"));
    $("#deleteTestCase").attr('data-toggle', 'tooltip').attr('data-original-title', doc.getDocDescription("page_testcasescript", "delete")).html("<span class='glyphicon glyphicon-trash'></span> " + doc.getDocLabel("page_testcasescript", "delete"));

    $("#addStep").html(doc.getDocLabel("page_testcasescript", "add_step"));
    $("#addActionBottomBtn button").html(doc.getDocLabel("page_testcasescript", "add_action"));
    $("#stepConditionOperator").prev().html(doc.getDocLabel("page_testcasescript", "step_condition_operation"));
    $("#stepConditionVal1").prev().html(doc.getDocLabel("page_testcasescript", "step_condition_value1"));

    // TestCase
    $("[name='testField']").html(doc.getDocOnline("test", "Test"));
    $("[name='testCaseField']").html(doc.getDocOnline("testcase", "TestCase"));
    $("[name='lastModifierField']").html(doc.getDocOnline("testcase", "LastModifier"));
    $("[name='originField']").html(doc.getDocOnline("testcase", "Origine"));
    $("[name='refOriginField']").html(doc.getDocOnline("testcase", "RefOrigine"));
    $("[name='projectField']").html(doc.getDocOnline("project", "idproject"));
    $("[name='ticketField']").html(doc.getDocOnline("testcase", "ticket"));
    $("[name='functionField']").html(doc.getDocOnline("testcase", "Function"));
    $("[name='applicationField']").html(doc.getDocOnline("application", "Application"));
    $("[name='statusField']").html(doc.getDocOnline("testcase", "Status"));
    $("[name='bugIdField']").html(doc.getDocOnline("testcase", "BugID"));
    $("[name='isActiveQAField']").html(doc.getDocOnline("testcase", "IsActiveQA"));
    $("[name='isActiveUATField']").html(doc.getDocOnline("testcase", "IsActiveUAT"));
    $("[name='isActiveUATField']").html(doc.getDocOnline("testcase", "IsActiveUAT"));
    $("[name='isActivePRODField']").html(doc.getDocOnline("testcase", "IsActivePROD"));
    $("[name='testCaseDescriptionField']").html(doc.getDocOnline("testcase", "Description"));
    $("[name='detailedDescriptionField']").html(doc.getDocOnline("testcase", "detailedDescription"));
    $("[name='testCaseDescriptionField']").html(doc.getDocOnline("testcase", "Description"));
    $("[name='descriptionField']").html(doc.getDocOnline("test", "Description"));
    $("[name='creatorField']").html(doc.getDocOnline("testcase", "Creator"));
    $("[name='implementerField']").html(doc.getDocOnline("testcase", "Implementer"));
    $("[name='groupField']").html(doc.getDocOnline("invariant", "TESTCASE_TYPE"));
    $("[name='priorityField']").html(doc.getDocOnline("invariant", "PRIORITY"));
    $("[name='bugIdField']").html(doc.getDocOnline("testcase", "BugID"));
    $("[name='tcDateCreaField']").html(doc.getDocOnline("testcase", "TCDateCrea"));
    $("[name='isActiveField']").html(doc.getDocOnline("testcase", "IsActive"));
    $("[name='fromMajorField']").html(doc.getDocOnline("testcase", "FromMajor"));
    $("[name='fromMinorField']").html(doc.getDocOnline("testcase", "FromMinor"));
    $("[name='toMajorField']").html(doc.getDocOnline("testcase", "ToMajor"));
    $("[name='toMinorField']").html(doc.getDocOnline("testcase", "ToMinor"));
    $("[name='targetMajorField']").html(doc.getDocOnline("testcase", "TargetMajor"));
    $("[name='targetMinorField']").html(doc.getDocOnline("testcase", "TargetMinor"));
    $("[name='commentField']").html(doc.getDocOnline("testcase", "Comment"));
    $("#filters").html(doc.getDocOnline("page_testcaselist", "filters"));
    $("#testCaseListLabel").html(doc.getDocOnline("page_testcaselist", "testcaselist"));
    $("[name='btnLoad']").html(doc.getDocLabel("page_global", "buttonLoad"));
    $("[name='testField']").html(doc.getDocLabel("test", "Test"));
    $("[name='editEntryField']").html(doc.getDocLabel("page_testcaselist", "btn_edit"));
    $("[name='addEntryField']").html(doc.getDocLabel("page_testcaselist", "btn_create"));
    $("[name='linkField']").html(doc.getDocLabel("page_testcaselist", "link"));
    // PREPARE MASS ACTION
    $("[name='massActionTestCaseField']").html(doc.getDocOnline("page_testcaselist", "massAction"));
    $("[name='testInfoField']").html(doc.getDocLabel("page_testcaselist", "testInfo"));
    $("[name='testCaseInfoField']").html(doc.getDocLabel("page_testcaselist", "testCaseInfo"));
    $("[name='testCaseParameterField']").html(doc.getDocLabel("page_testcaselist", "testCaseParameter"));
    $("[name='activationCriteriaField']").html(doc.getDocLabel("page_testcaselist", "activationCriteria"));
    // Traceability
    $("[name='lbl_datecreated']").html(doc.getDocOnline("transversal", "DateCreated"));
    $("[name='lbl_usrcreated']").html(doc.getDocOnline("transversal", "UsrCreated"));
    $("[name='lbl_datemodif']").html(doc.getDocOnline("transversal", "DateModif"));
    $("[name='lbl_usrmodif']").html(doc.getDocOnline("transversal", "UsrModif"));
}

function triggerTestCaseExecutionQueueandSeeFromTC(queueId) {
    $.ajax({
        url: "CreateTestCaseExecutionQueue",
        async: true,
        method: "POST",
        data: {
            id: queueId,
            actionState: "toQUEUED",
            actionSave: "save"
        },
        success: function (data) {
            if (getAlertType(data.messageType) === "success") {
                showMessageMainPage(getAlertType(data.messageType), data.message, false, 60000);
                var url = "./TestCaseExecution.jsp?executionQueueId=" + encodeURI(data.testCaseExecutionQueueList[0].id);
                window.location.replace(url);
            } else {
                showMessageMainPage(getAlertType(data.messageType), data.message, false, 60000);
            }
        },
        error: showUnexpectedError
    });
}

function addAction(action) {
    setModif(true);
    var step = $("#steps li.active").data("item");
    var act = new Action(null, step, true);
    step.setAction(act, action);
    setAllSort();
    return act;
}

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

function getTestCase(test, testcase, step) {
    window.location.href = "./TestCaseScript.jsp?test=" + encodeURI(test) + "&testcase=" + encodeURI(testcase) + "&step=" + encodeURI(step);
}

function setAllSort() {
    var steps = $("#steps li");
    var stepArr = [];

    // Construct the step/action/control list:
    // Iterate over steps
    for (var i = 0; i < steps.length; i++) {
        var step = $(steps[i]).data("item");
        var actionArr = [];

        if (!step.toDelete) {
            // Set the step's sort
            step.setSort(i + 1);

            // Get step's actions
            var actions = step.stepActionContainer.children(".action-group").children(".action");
            // Iterate over actions
            for (var j = 0; j < actions.length; j++) {
                var action = $(actions[j]).data("item");
                var controlArr = [];

                if (!action.toDelete) {
                    // Set the action's sort
                    action.setSort(j + 1);

                    // Set the action's step
                    action.setStepId(i + 1);

                    // Get action's controls
                    var controls = action.html.children(".control");

                    // Iterate over controls
                    for (var k = 0; k < controls.length; k++) {
                        var control = $(controls[k]).data("item");

                        if (!control.toDelete) {
                            // Set the control's sort
                            control.setParentActionSort(j + 1);
                            control.setSort(k + 1);
                            control.setStepId(i + 1);
                            control.setControlId(k + i);

                            // Then push control into result array
                            controlArr.push(control.getJsonData());
                        }
                    }
                }
                var actionJson = action.getJsonData();
                actionJson.controls = controlArr;
                actionArr.push(actionJson);
            }
            var stepJson = step.getJsonData();
            $("li .step-description").eq(i).text("[" + (i + 1) + "] " + stepJson.description);
            stepJson.actions = actionArr;
            stepArr.push(stepJson);
        }
    }

    return stepArr;
}

function saveScript(property) {

    // Disable the save button to avoid double click.
    $("#saveScript").attr("disabled", true);

    var stepArr = setAllSort();
    var doc = new Doc();

    var properties = $("#propTable #masterProp");
    var propArr = [];
    var propertyWithoutCountry = false;
    var propertyWithoutName = false;

    for (var i = 0; i < properties.length; i++) {
        if (($(properties[i]).data("property").countries.length <= 0) && ($(properties[i]).data("property").toDelete === false)) {
            propertyWithoutCountry = true;
        }
        if (($(properties[i]).data("property").property === "") && ($(properties[i]).data("property").toDelete === false)) {
            propertyWithoutName = true;
        }
        if (!$.isNumeric($(properties[i]).data("property").rank)) {
            $(properties[i]).data("property").rank = 1;
        }
        propArr.push($(properties[i]).data("property"));
    }


    if (property !== undefined) {
        for (i in propArr) {
            if (propArr[i].property === property) {
                propArr[i].toDelete = true;
            }
        }
    }

    var saveProp = function () {
        showLoaderInModal('#propertiesModal');
        $.ajax({
            url: "UpdateTestCaseWithDependencies",
            async: true,
            method: "POST",
            contentType: 'application/json; charset=utf-8',
            dataType: 'json',
            data: JSON.stringify({
                informationInitialTest: GetURLParameter("test"),
                informationInitialTestCase: GetURLParameter("testcase"),
                informationTest: GetURLParameter("test"),
                informationTestCase: GetURLParameter("testcase"),
                steps: stepArr,
                properties: propArr
            }),
            success: function () {

                var stepHtml = $("#steps li.active");
                var stepData = stepHtml.data("item");

                var tabActive = $("#tabsScriptEdit li.active a").attr("name");

                var parser = document.createElement('a');
                parser.href = window.location.href;

                var tutorielId = GetURLParameter("tutorielId", null);
                var startStep = GetURLParameter("startStep", null);

                var tutorialParameters = "";
                if (tutorielId !== null && startStep !== null) {
                    tutorialParameters = "&tutorielId=" + tutorielId + "&startStep=" + startStep;
                }

                var url_sort = "";
                if (!(isEmpty(stepData))) {
                    url_sort = "&step=" + encodeURI(stepData.sort);
                }
                var new_uri = parser.pathname + "?test=" + encodeURI(GetURLParameter("test")) + "&testcase=" + encodeURI(GetURLParameter("testcase")) + url_sort + tutorialParameters + "&tabactive=" + tabActive;
                // If the 1st 2 characters are // we remove 1 of them.
                if ((new_uri[0] === '/') && (new_uri[1] === '/')) {
                    new_uri = new_uri[0] + new_uri.slice(2)
                }
                setModif(false);

                window.location.href = new_uri;
            },
            error: showUnexpectedError
        });
    };


    if (propertyWithoutCountry) {
        showModalConfirmation(function () {
            $('#confirmationModal').modal('hide');
            saveProp();
        }, function () {
            $("#saveScript").attr("disabled", false);
        }, doc.getDocLabel("page_global", "btn_savetableconfig"), doc.getDocLabel("page_testcasescript", "warning_no_country"), "", "", "", "");
    } else if (propertyWithoutName) {
        showModalConfirmation(function () {
            $('#confirmationModal').modal('hide');
            saveProp();
        }, function () {
            $("#saveScript").attr("disabled", false);
        }, doc.getDocLabel("page_global", "btn_savetableconfig"), doc.getDocLabel("page_testcasescript", "warning_one_empty_prop"), "", "", "", "");
    } else {
        saveProp();
    }
}


function prevent(e) {
    e.preventDefault();
}

function drawPropertyList(property, index, isSecondary) {
    var htmlElement = $("<li></li>").addClass("list-group-item list-group-item-calm row").css("margin-left", "0px");

    $(htmlElement).append($("<a ></a>").attr("href", "#propertyLine" + property).text(property));

    var deleteBtn = $("<button style='padding:0px;float:right;display:none' class='btn add-btn deleteItem-btn'></button>").append($("<span></span>").addClass("glyphicon glyphicon-trash"));
    deleteBtn.attr("disabled", !canUpdate);
    $(htmlElement).find("a").append(deleteBtn);

    // add the color for secondary properties
    // TO DO: create a new CSS class
    if (isSecondary === true) {
        htmlElement.css("background-color", "#dfe4e9");
        htmlElement.children("a").append("<span class='secondaryproptext'>secondary</span>");
        htmlElement.find("span.secondaryproptext").css("padding", "0px 10px 0px 10px");
        htmlElement.find("span.secondaryproptext").css("float", "right");
        htmlElement.find("span.secondaryproptext").css("display", "block");
        htmlElement.find("span.secondaryproptext").css("color", "#636e72");
    }

    deleteBtn.click(function (ev) {

        if (allDelete !== true) {
            $("div.list-group-item").each(function () {
                if ($(this).find("#propName").val() === property) {
                    if (!$(this).hasClass("list-group-item-danger")) {
                        $(this).find("button.add-btn.btn-danger").trigger("click");
                    }
                }
            });
        } else {
            $("div.list-group-item").each(function () {
                if ($(this).find("#propName").val() === property) {
                    $(this).find("button.add-btn.btn-danger").trigger("click");
                }
            });
        }
    });

    $("#propList").append(htmlElement);
}

function drawProperty(property, testcaseObject, canUpdate, index) {
    var doc = new Doc();
    var selectType = getSelectInvariant("PROPERTYTYPE", false, false);
    selectType.attr("name", "propertyType");
    var selectDB = getSelectInvariant("PROPERTYDATABASE", false, false);
    var selectNature = getSelectInvariant("PROPERTYNATURE", false, false);
    var deleteBtn = $("<button class='btn add-btn deleteItem-btn'></button>").append($("<span></span>").addClass("glyphicon glyphicon-trash"));
    var moreBtn = $("<button class='btn btn-default add-btn'></button>").append($("<span></span>").addClass("glyphicon glyphicon-chevron-down"));

    var propertyInput = $("<input onkeypress='return tec_keyispressed(event);' id='propName' style='width: 100%; font-size: 16px; font-weight: 600;' name='propName' placeholder='" + doc.getDocLabel("page_testcasescript", "feed_propertyname") + "'>").addClass("form-control input-sm").val(property.property);
    var descriptionInput = $("<textarea rows='1' id='propDescription' placeholder='" + doc.getDocLabel("page_testcasescript", "feed_propertydescription") + "'>").addClass("form-control input-sm").val(property.description);
    var valueInput = $("<pre name='propertyValue' id='propertyValue" + index + "' style='min-height:150px' rows='1' placeholder='" + doc.getDocLabel("page_applicationObject", "Value") + "'></pre>").addClass("form-control input-sm").val(property.value1);
    var value2Input = $("<textarea name='propertyValue2' rows='1' placeholder='" + doc.getDocLabel("page_applicationObject", "Value") + "'></textarea>").addClass("form-control input-sm").val(property.value2);
    var lengthInput = $("<input placeholder='" + doc.getDocLabel("page_testcasescript", "length") + "'>").addClass("form-control input-sm").val(property.length);
    var rowLimitInput = $("<input placeholder='" + doc.getDocLabel("page_testcasescript", "row_limit") + "'>").addClass("form-control input-sm").val(property.rowLimit);
    var cacheExpireInput = $("<input type='number' placeholder=''>").addClass("form-control input-sm").val(property.cacheExpire);
    var retryNbInput = $("<input placeholder='" + doc.getDocLabel("testcasecountryproperties", "RetryNb") + "'>").addClass("form-control input-sm").val(property.retryNb);
    var retryPeriodInput = $("<input placeholder='" + doc.getDocLabel("testcasecountryproperties", "RetryPeriod") + "'>").addClass("form-control input-sm").val(property.retryPeriod);
    var rankInput = $("<input placeholder='" + doc.getDocLabel("testcasecountryproperties", "Rank") + "'>").addClass("form-control input-sm").val(property.rank);
    var table = $("#propTable");

    selectType.attr("disabled", !canUpdate);
    selectDB.attr("disabled", !canUpdate);
    selectNature.attr("disabled", !canUpdate);
    deleteBtn.attr("disabled", !canUpdate);
    propertyInput.prop("readonly", !canUpdate);
    descriptionInput.prop("readonly", !canUpdate);
    valueInput.prop("readonly", !canUpdate);
    value2Input.prop("readonly", !canUpdate);
    lengthInput.prop("readonly", !canUpdate);
    rowLimitInput.prop("readonly", !canUpdate);
    retryNbInput.prop("readonly", !canUpdate);
    retryPeriodInput.prop("readonly", !canUpdate);
    cacheExpireInput.prop("readonly", !canUpdate);
    rankInput.prop("readonly", !canUpdate);

    // if the property is secondary
    var isSecondary = property.rank === 2;
    if (isSecondary) {
        var content = $("<div class='row secondaryProperty list-group-item list-group-item-secondary'></div>");
    } else {
        var content = $("<div class='row property list-group-item'></div>");
    }
    var props = $("<div class='col-sm-11' name='propertyLine' id='propertyLine" + property.property + "'></div>");
    var right = $("<div class='col-sm-1 propertyButtons'></div>");

    var row1 = $("<div class='row' id='masterProp' name='masterProp' style='margin-top:10px;'></div>");
    var row2 = $("<div class='row' name='masterProp'></div>");
    var row3 = $("<div class='row' style='display:none;'></div>");
    var row4 = $("<div class='row' name='masterProp'></div>");
    var row5 = $("<div class='row'></div>");
    var propertyName = $("<div class='col-sm-4 form-group'></div>").append(propertyInput);
    var description = $("<div class='col-sm-8 form-group'></div>").append(descriptionInput);
    var country = $("<div class='col-sm-10'></div>").append(getTestCaseCountry(testcaseObject.countries, property.countries, !canUpdate));
    var type = $("<div class='col-sm-2 form-group'></div>").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "type_field"))).append(selectType.val(property.type));
    var db = $("<div class='col-sm-2 form-group' name='fieldDatabase'></div>").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "db_field"))).append(selectDB.val(property.database));
    var value = $("<div class='col-sm-8 form-group' name='fieldValue1'></div>").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "value1_field"))).append(valueInput);
    var value2 = $("<div class='col-sm-6 form-group' name='fieldValue2'></div>").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "value2_field"))).append(value2Input);
    var length = $("<div class='col-sm-2 form-group' name='fieldLength'></div>").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "length_field"))).append(lengthInput);
    var rowLimit = $("<div class='col-sm-2 form-group' name='fieldRowLimit'></div>").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "rowlimit_field"))).append(rowLimitInput);
    var cacheExpire = $("<div class='col-sm-2 form-group' name='fieldExpire'></div>").append($("<label></label>").text("cacheExpire")).append(cacheExpireInput);

    var nature = $("<div class='col-sm-2 form-group' name='fieldNature'></div>").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "nature_field"))).append(selectNature.val(property.nature));
    var retryNb = $("<div class='col-sm-2 form-group' name='fieldRetryNb'></div>").append($("<label></label>").text(doc.getDocLabel("testcasecountryproperties", "RetryNb"))).append(retryNbInput);
    var retryPeriod = $("<div class='col-sm-1 form-group' name='fieldRetryPeriod'></div>").append($("<label></label>").text(doc.getDocLabel("testcasecountryproperties", "RetryPeriod"))).append(retryPeriodInput);
    var rank = $("<div class='col-sm-1 form-group' name='rank'></div>").append($("<label></label>").text(doc.getDocLabel("testcasecountryproperties", "Rank"))).append(rankInput);

    var selectAllBtn = $("<button></button>").addClass("btn btn-default btn-sm").append($("<span></span>").addClass("glyphicon glyphicon-check")).click(function () {
        country.find("input[type='checkbox']").prop('checked', true).trigger("change");
    });
    selectAllBtn.attr("disabled", !canUpdate);
    var selectNoneBtn = $("<button></button>").addClass("btn btn-default btn-sm").append($("<span></span>").addClass("glyphicon glyphicon-unchecked")).click(function () {
        country.find("input[type='checkbox']").prop('checked', false).trigger("change");
    });
    selectNoneBtn.attr("disabled", !canUpdate);
    var btnRow = $("<div class='col-sm-2'></div>").css("margin-top", "5px").css("margin-bottom", "5px").append(selectAllBtn).append(selectNoneBtn);

    deleteBtn.click(function () {
        // trigger when any deleteBtn is clicked
        var stopAllDelete = false;
        var stopNothing = false;
        var linkToProperty = null;
        var nothing = false;
        property.toDelete = !property.toDelete;

        if (property.toDelete) {
            if (isSecondary) {
                content.removeClass("list-group-item-secondary");
            }
            content.addClass("list-group-item-danger");
        } else {
            content.removeClass("list-group-item-danger");
            if (isSecondary) {
                content.addClass("list-group-item-secondary");
            }
        }

        $(table).find("div.list-group-item").each(function () {
            if ($(this).find("#propName").val() === property.property) {
                if ($(this).hasClass("list-group-item-danger")) {
                    if (stopAllDelete !== true) {
                        allDelete = true;
                    }
                    if (stopNothing !== true) {
                        nothing = false;
                        stopNothing = true;
                    }
                } else {
                    if (stopAllDelete !== true) {
                        allDelete = false;
                        stopAllDelete = true;
                    }
                    if (stopNothing !== true) {
                        nothing = true;
                    }
                }
            }
        });

        $("#propListWrapper li a").each(function () {
            if ($(this).text() === property.property)
                linkToProperty = $(this).parent();
        });

        // go though every link and look for the right one
        if (linkToProperty !== null) {
            if (allDelete === true && nothing === false) {
                // set color to red
                linkToProperty.css("background-color", "#c94350");
            } else if (nothing === true) {
                // set color to white
                linkToProperty.css("background-color", "#fff");
            } else {
                // set color to pink
                linkToProperty.css("background-color", "#f2dede");
            }

        }
    });

    moreBtn.click(function () {
        if ($(this).find("span").hasClass("glyphicon-chevron-down")) {
            $(this).find("span").removeClass("glyphicon-chevron-down");
            $(this).find("span").addClass("glyphicon-chevron-up");
        } else {
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
        setPlaceholderProperty($(this).parents(".property"), property);
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

    cacheExpireInput.change(function () {
        property.cacheExpire = parseInt($(this).val());
    });

    selectNature.change(function () {
        property.nature = $(this).val();
    });

    retryNbInput.change(function () {
        property.retryNb = $(this).val();
    });

    retryPeriodInput.change(function () {
        property.retryPeriod = $(this).val();
    });

    rankInput.change(function () {
        property.rank = $(this).val();
    });

    row1.data("property", property);
    row1.append(propertyName);
    row1.append(description);
    props.append(row1);

    row4.append(btnRow);
    row4.append(country);
    props.append(row4);

    row2.append(type);
    row2.append(db);
    row2.append(value);
    row2.append(value2);
    props.append(row2);


    row3.append(length);
    row3.append(rowLimit);
    row3.append(nature);
    row3.append(retryNb);
    row3.append(retryPeriod);
    row3.append(cacheExpire);
    row3.append(rank);
    props.append(row3);

    right.append(moreBtn).append(deleteBtn);

    content.append(props).append(right);
    table.append(content);
    return [props, property];
}

function drawInheritedProperty(propList) {
    var doc = new Doc();
    var selectType = getSelectInvariant("PROPERTYTYPE", false, false).attr("disabled", true);
    selectType.attr("name", "inheritPropertyType");
    var selectDB = getSelectInvariant("PROPERTYDATABASE", false, false).attr("disabled", true);
    var selectNature = getSelectInvariant("PROPERTYNATURE", false, false).attr("disabled", true);
    var table = $("#inheritedPropPanel");

    for (var index = 0; index < propList.length; index++) {
        var property = propList[index];
        var test = property.fromTest;
        var testcase = property.fromTestcase;

        var moreBtn = $("<button class='btn btn-default add-btn'></button>").append($("<span></span>").addClass("glyphicon glyphicon-chevron-down"));
        var editBtn = $("<a href='./TestCaseScript.jsp?test=" + encodeURI(test) + "&testcase=" + encodeURI(testcase) + "&property=" + encodeURI(property.property) + "' class='btn btn-primary add-btn'></a>").append($("<span></span>").addClass("glyphicon glyphicon-pencil"));

        var propertyInput = $("<input id='propName' name='propName' style='width: 100%; font-size: 16px; font-weight: 600;' placeholder='" + doc.getDocLabel("page_testcasescript", "feed_propertyname") + "' readonly='readonly'>").addClass("form-control input-sm").val(property.property);
        var descriptionInput = $("<textarea rows='1' id='propDescription' placeholder='" + doc.getDocLabel("page_testcasescript", "feed_propertydescription") + "' readonly='readonly'>").addClass("form-control input-sm").val(property.description);
        var valueInput = $("<pre id='inheritPropertyValue" + index + "' style='min-height:150px'  rows='1' placeholder='" + doc.getDocLabel("page_applicationObject", "Value") + "' readonly='readonly'></textarea>").addClass("form-control input-sm").text(property.value1);
        var value2Input = $("<textarea name='inheritPropertyValue2' rows='1' placeholder='" + doc.getDocLabel("page_applicationObject", "Value") + "' readonly='readonly'></textarea>").addClass("form-control input-sm").val(property.value2);
        var lengthInput = $("<input placeholder='" + doc.getDocLabel("page_testcasescript", "length") + "' readonly='readonly'>").addClass("form-control input-sm").val(property.length);
        var rowLimitInput = $("<input placeholder='" + doc.getDocLabel("page_testcasescript", "row_limit") + "' readonly='readonly'>").addClass("form-control input-sm").val(property.rowLimit);
        var cacheExpireInput = $("<input placeholder='0' readonly='readonly'>").addClass("form-control input-sm").val(property.cacheExpire);
        var retryNbInput = $("<input placeholder='" + doc.getDocLabel("testcasecountryproperties", "RetryNb") + "' readonly='readonly'>").addClass("form-control input-sm").val(property.retryNb);
        var rankInput = $("<input type='number' placeholder='" + doc.getDocLabel("testcasecountryproperties", "Rank") + "' readonly='readonly'>").addClass("form-control input-sm").val(property.rank);

        var retryPeriodInput = $("<input placeholder='" + doc.getDocLabel("testcasecountryproperties", "RetryPeriod") + "' readonly='readonly'>").addClass("form-control input-sm").val(property.retryPeriod);

        var content = $("<div class='row property list-group-item disabled'></div>");
        var props = $("<div class='col-sm-11' name='inheritPropertyLine' id='inheritPropertyLine" + property.property + "'></div>");
        var right = $("<div class='col-sm-1 propertyButtons'></div>");

        var row1 = $("<div class='row' id='masterProp' name='masterProp' style='margin-top:10px;'></div>");
        var row2 = $("<div class='row' name='masterProp'></div>");
        var row3 = $("<div class='row' style='display:none;'></div>");
        var row4 = $("<div class='row' name='masterProp'></div>");
        var row5 = $("<div class='row'></div>");
        var propertyName = $("<div class='col-sm-4 form-group'></div>").append(propertyInput);
        var description = $("<div class='col-sm-8 form-group'></div>").append(descriptionInput);
        var country = $("<div class='col-sm-10'></div>").append(getTestCaseCountry(property.countries, property.countries, true));
        var type = $("<div class='col-sm-2 form-group'></div>").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "type_field"))).append(selectType.clone().val(property.type));
        var db = $("<div class='col-sm-2 form-group' name='fieldDatabase'></div>").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "db_field"))).append(selectDB.val(property.database));
        var value = $("<div class='col-sm-8 form-group' name='fieldValue1'></div>").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "value1_field"))).append(valueInput);
        var value2 = $("<div class='col-sm-6 form-group' name='fieldValue2'></div>").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "value2_field"))).append(value2Input);
        var length = $("<div class='col-sm-2 form-group' name='fieldLength'></div>").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "length_field"))).append(lengthInput);
        var rowLimit = $("<div class='col-sm-2 form-group' name='fieldRowLimit'></div>").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "rowlimit_field"))).append(rowLimitInput);
        var nature = $("<div class='col-sm-2 form-group' name='fieldNature'></div>").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "nature_field"))).append(selectNature.val(property.nature));
        var cacheExpire = $("<div class='col-sm-2 form-group' name='fieldExpire'></div>").append($("<label></label>").text("cacheExpire")).append(cacheExpireInput);
        var retryNb = $("<div class='col-sm-2 form-group' name='fieldRetryNb'></div>").append($("<label></label>").text(doc.getDocLabel("testcasecountryproperties", "RetryNb"))).append(retryNbInput);
        var retryPeriod = $("<div class='col-sm-1 form-group' name='fieldRetryPeriod'></div>").append($("<label></label>").text(doc.getDocLabel("testcasecountryproperties", "RetryPeriod"))).append(retryPeriodInput);
        var rank = $("<div class='col-sm-1 form-group' name='Rank'></div>").append($("<label></label>").text(doc.getDocLabel("testcasecountryproperties", "Rank"))).append(rankInput);


        var selectAllBtn = $("<button disabled></button>").addClass("btn btn-default btn-sm").append($("<span></span>").addClass("glyphicon glyphicon-check")).click(function () {
            country.find("input[type='checkbox']").prop('checked', true);
        });
        var selectNoneBtn = $("<button disabled></button>").addClass("btn btn-default btn-sm").append($("<span></span>").addClass("glyphicon glyphicon-unchecked")).click(function () {
            country.find("input[type='checkbox']").prop('checked', false);
        });
        var btnRow = $("<div class='col-sm-2'></div>").css("margin-top", "5px").css("margin-bottom", "5px").append(selectAllBtn).append(selectNoneBtn);

        moreBtn.click(function () {
            if ($(this).find("span").hasClass("glyphicon-chevron-down")) {
                $(this).find("span").removeClass("glyphicon-chevron-down");
                $(this).find("span").addClass("glyphicon-chevron-up");
            } else {
                $(this).find("span").removeClass("glyphicon-chevron-up");
                $(this).find("span").addClass("glyphicon-chevron-down");
            }
            $(this).parent().parent().find(".row:not([name='masterProp'])").toggle();
        });

        row1.data("property", property);
        row1.append(propertyName);
        row1.append(description);
        props.append(row1);

        row4.append(btnRow);
        row4.append(country);
        props.append(row4);

        row2.append(type);
        row2.append(db);
        row2.append(value);
        row2.append(value2);
        props.append(row2);

        row3.append(db);
        row3.append(length);
        row3.append(cacheExpire);
        row3.append(rowLimit);
        row3.append(nature);
        row3.append(retryNb);
        row3.append(retryPeriod);
        row3.append(rank);
        props.append(row3);

        right.append(moreBtn);
        right.append(editBtn);

        content.append(props).append(right);
        table.append(content);

        var htmlElement = $("<li></li>").addClass("list-group-item list-group-item-calm row").css("margin-left", "0px");
        $(htmlElement).append($("<a></a>").attr("href", "#inheritPropertyLine" + property.property).text(property.property));

        $("#inheritPropList").append(htmlElement);
    }

    sortProperties("#inheritedPropPanel");
}

function loadPropertiesAndDraw(test, testcase, testcaseObject, propertyToFocus, canUpdate) {

    let array = [];
    let secondaryPropertiesArray = [];

    let propertyList = [];
    let secondaryPropertyList = [];

    let propertiesFromTestcase = testcaseObject.properties.testCaseProperties.sort((a, b) => {
        return compareStrings(a.property, b.property);
    });

    for (let i = 0; i < propertiesFromTestcase.length; i++) {
        let property = propertiesFromTestcase[i];
        // check if the property is secondary
        let isSecondary = property.rank === 2;

        if (isSecondary) {
            secondaryPropertiesArray.push(propertiesFromTestcase[i].property);
        } else {
            array.push(propertiesFromTestcase[i].property);
        }
        property.toDelete = false;
        let prop = drawProperty(property, testcaseObject, canUpdate, i);
        setPlaceholderProperty(prop[0], prop[1]);

        if (isSecondary) {
            secondaryPropertyList.push(property.property);
        } else {
            propertyList.push(property.property);
        }
    }
    localStorage.setItem("properties", JSON.stringify(propertyList));
    localStorage.setItem("secondaryProperties", JSON.stringify(propertyList));
    sortProperties("#propTable");
    sortSecondaryProperties("#propTable");

    let scope = undefined;
    if (propertyToFocus !== undefined && propertyToFocus !== null) {
        $("#propTable #propName").each(function (i) {
            if ($(this).val() === propertyToFocus) {
                scope = this;
                $("#propertiesModal").on("shown.bs.modal", function (e) {
                    $(scope).focus();
                    $(scope).click();
                });
            }
        });
    }

    let propertyListUnique = Array.from(new Set(propertyList));
    let secondaryPropertyListUnique = Array.from(new Set(secondaryPropertyList));

    for (let index = 0; index < propertyListUnique.length; index++) {
        drawPropertyList(propertyListUnique[index], index, false);
    }

    for (let index = 0; index < secondaryPropertyListUnique.length; index++) {
        drawPropertyList(secondaryPropertyListUnique[index], index, true);
    }

    array.sort(function (a, b) {
        return compareStrings(a, b);
    });
}

function sortProperties(identifier) {
    var container = $(identifier);
    var list = container.children(".property");
    list.sort(function (a, b) {

        var aProp = $(a).find("#masterProp").data("property").property.toLowerCase(),
                bProp = $(b).find("#masterProp").data("property").property.toLowerCase();

        if (aProp > bProp) {
            return 1;
        }
        if (aProp < bProp) {
            return -1;
        }
        return 0;
    });
    container.append(list);
}

// Temporary function: can be merged with sortProperties by adding one parameter
// to call the children() function differently
function sortSecondaryProperties(identifier) {
    var container = $(identifier);
    var list = container.children(".secondaryProperty");
    list.sort(function (a, b) {

        var aProp = $(a).find("#masterProp").data("property").property.toLowerCase(),
                bProp = $(b).find("#masterProp").data("property").property.toLowerCase();

        if (aProp > bProp) {
            return 1;
        }
        if (aProp < bProp) {
            return -1;
        }
        return 0;
    });
    container.append(list);
}

function getTestCaseCountry(countries, countriesToCheck, isDisabled) {
    var html = [];
    var cpt = 0;
    var div = $("<div></div>").addClass("checkbox");

    $.each(countries, function (index) {
        var country;
        if (typeof index === "number") {
            country = countries[index].value;
        } else if (typeof index === "string") {
            country = index;
        }
        var input = $("<input>").attr("type", "checkbox").attr("name", country);
        let countryIndex = countriesToCheck.findIndex(c => c.value === country);
        if (countryIndex !== -1) {
            input.prop("checked", true).trigger("change");
        }
        if (isDisabled) {
            input.prop("disabled", "disabled");
        } else {
            input.change(function () {
                var country = $(this).prop("name");
                var checked = $(this).prop("checked");
                let countryIndexChange = countriesToCheck.findIndex(c => c.value === country);

                if (checked && countryIndexChange === -1) {
                    countriesToCheck.push({value: country});
                } else if (!checked && countryIndexChange !== -1) {
                    countriesToCheck.splice(countryIndexChange, 1);
                }
            });
        }

        div.append($("<label></label>").addClass("checkbox-inline")
                .append(input)
                .append(country));

        cpt++;
        html.push(div);
    });

    return html;
}

function loadTestCaseInfo(info) {
    $(".testTestCase #description").text(info.description);
}

function changeLib() {
    setModif(true);
    var stepHtml = $("#steps li.active");
    var stepData = stepHtml.data("item");
    if (stepData.isLibraryStep) {
        stepData.isLibraryStep = false;
        $(this).removeClass("useStep-btn");
    } else {
        stepData.isLibraryStep = true;
        $(this).addClass("useStep-btn");
    }
}

function generateImportInfoId(stepInfo) {
    var hash = 0;
    let strval = stepInfo.description + stepInfo.test + "-" + stepInfo.testCase + "-" + stepInfo.sort;
    if (strval.length === 0)
        return hash;
    for (i = 0; i < strval.length; i++) {
        char = strval.charCodeAt(i);
        hash = ((hash << 5) - hash) + char;
        hash = hash & hash; // Convert to 32bit integer
    }
    return hash;
}

var importInfoIdx = 0;

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
                '<div id="' + importInfoId + '" class="row">' +
                '   <div class="col-sm-5"><span class="badge">' + importInfoIdx + ' </span>&nbsp;' + stepInfo.description + '</div>' +
                '   <div name="importInfo" class="col-sm-5"></div>' +
                '   <div class="col-sm-2">' +
                '    <label class="checkbox-inline">' +
                '        <input type="checkbox" name="useStep" checked> Use Step' +
                '    </label>' +
                '   </div>' +
                '</div>';

        $("#importDetail").append(importInfo);
        $("#" + importInfoId).find("[name='importInfo']").text("Imported from " + stepInfo.test + " - " + stepInfo.testCase + " - " + stepInfo.sort + ")").data("stepInfo", stepInfo);

        $("#importDetail[name='useStep']").prop("checked", true);

        $("#importDetail").show();
    }
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
        "conditionOptions": "[]",
        "isExecutionForced": false
    };
}

function addStep(event) {
    var steps = event.data.steps;
    $("#addStepModal").modal('show');

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
    console.info(stepObj);
    stepObj.draw();
    steps.push(stepObj);
    stepObj.html.trigger("click");

}


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
            if (steps[i].sort == stepIndex) {
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

/** Modification Status * */

var getModif, setModif, initModification;
(function () {
    var isModif = false;
    getModif = function () {
        return isModif;
    };
    setModif = function (val) {
        isModif = val;
        if (isModif === true && $("#saveScript").hasClass("btn-default")) {
            $("#saveScript").removeClass("btn-default").addClass("btn-primary");
        } else if (isModif === false && $("#saveScript").hasClass("btn-primary")) {
            $("#saveScript").removeClass("btn-primary").addClass("btn-default");
        }

    };
    initModification = function () {
        $(".panel-body input, .panel-body select, .panel-body textarea").change(function () {
            setModif(true);
        });
    };
})();

/** LIBRARY STEP UTILY FUNCTIONS * */

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
                        $("#lib").append($("<a></a>").addClass("list-group-item").attr("data-toggle", "collapse").attr("href", "[data-test='" + step.test + "']")
                                .text(step.test).prepend($("<span></span>").addClass("glyphicon glyphicon-chevron-right")));

                        var listGr = $("<div></div>").addClass("list-group collapse").attr("data-test", step.test);
                        $("#lib").append(listGr);

                        test[step.test] = {content: listGr, testCase: {}};
                    }
                    if ((!test[step.test].testCase.hasOwnProperty(step.testCase))) {
                        var listGrp = test[step.test].content;
                        listGrp.append($("<a></a>").addClass("list-group-item sub-item").attr("data-toggle", "collapse").attr("href", "[data-test='" + step.test + "'][data-testCase='" + step.testCase + "']")
                                .text(step.testCase + " - " + step.tcdesc).prepend($("<span></span>").addClass("glyphicon glyphicon-chevron-right")));

                        var listCaseGr = $("<div></div>").addClass("list-group collapse in").attr("data-test", step.test).attr("data-testCase", step.testCase);
                        listGrp.append(listCaseGr);

                        test[step.test].testCase[step.testCase] = {content: listCaseGr, step: {}};
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

function loadApplicationObject(application) {
    return new Promise(function (resolve, reject) {
        var array = [];
        $.ajax({
            url: "ReadApplicationObject?application=" + application,
            dataType: "json",
            success: function (data) {
                for (var i = 0; i < data.contentTable.length; i++) {
                    array.push(data.contentTable[i]);
                }
                resolve(array);
            }
        });
    });
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
                content += "<a target='_blank' href='./TestCaseScript.jsp?test=" + encodeURI(data.step[i].test) + "&testcase=" + encodeURI(data.step[i].testcase) + "&stepId=" + encodeURI(data.step[i].sort) + "'>" + data.step[i].test + " - " + data.step[i].testcase + " - " + data.step[i].sort + " - " + data.step[i].description + "</a><br/>";
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


/** DRAG AND DROP HANDLERS * */

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
    var obj = this.parentNode.parentNode.parentNode.parentNode;
    var offsetX = 50;
    var offsetY = 50;
    var img;

    $("[draggable='true']").addClass("statusOK");

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
    var target = this.parentNode.parentNode.parentNode.parentNode;
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
    } else if (sourceData instanceof Action && targetData instanceof Step) {
        $(target).click();
    } else if (sourceData instanceof Control && targetData instanceof Step) {
        $(target).click();
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
    this.parentNode.parentNode.parentNode.parentNode.style.opacity = '1';
    setAllSort();
    $("[draggable='true']").removeClass("statusOK");
}

/** DATA AGREGATION * */

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

function sortData(agreg) {
    for (var i = 0; i < agreg.length; i++) {
        var step = agreg[i];

        sortStep(step);
    }

    agreg.sort(function (a, b) {
        return a.sort - b.sort;
    });
}

function compareStrings(a, b) {
    a = a.toLowerCase();
    b = b.toLowerCase();

    return (a < b) ? -1 : (a > b) ? 1 : 0;
}

/** JAVASCRIPT OBJECT * */

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

Step.prototype.draw = function () {
    var htmlElement = this.html;
    var doc = new Doc();

// DESCRIPTION
    var descContainer = $("<div class='input-group'></div>");
    var descriptionField = $("<input id='stepDescription' class='description form-control crb-autocomplete-variable'>").attr("placeholder", doc.getDocLabel("page_testcasescript", "describe_step")).attr("style", "border:0px");
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
        var labelOptions = $("<span class='label label-primary optionLabel' style='background-color:rgba(114,124,245,.25);color:#727cf5'>" + doc.getDocLabel("page_testcasescript", "imported_from") + " <a href='./TestCaseScript.jsp?test=" + encodeURI(this.libraryStepTest) + "&testcase=" + encodeURI(this.libraryStepTestCase) + "&step=" + encodeURI(this.libraryStepSort) + "' >" + this.libraryStepTest + " - " + this.libraryStepTestCase + " - " + this.libraryStepSort + "</a></span>");
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

Step.prototype.show = function () {
    var doc = new Doc();
    var object = $(this).data("item");
    $("#addActionBottomBtn").show();

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
    $("#stepButtons").append('<button class="btn btn-default useStep-btn" title="Is Use Step" data-toggle="tooltip" id="isUseStep"><span class="glyphicon glyphicon-lock"></span></button>');
    $("#stepButtons").append('<button class="btn btn-default library-btn" title="Is Library" data-toggle="tooltip" id="isLib"><span class="glyphicon glyphicon-book"></span></button>');
    $("#stepButtons").append('<button class="btn add-btn config-btn" data-toggle="modal" data-target="#modalStepOptions" id="stepPlus"><span class="glyphicon glyphicon-cog"></span></button>');
    $("#stepButtons").append('<button class="btn add-btn deleteItem-btn" id="deleteStep"><span class="glyphicon glyphicon-trash"></span></button>');


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
        $("#isLib").addClass("useStep-btn");
        if (object.isStepInUseByOtherTestCase) {
            $("#isLib").click(function () {

                showStepUsesLibraryInConfirmationModal(object);

            });
        } else {
            $("#isLib").click(changeLib);
        }
    } else {
        $("#isLib").removeClass("useStep-btn");
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

        var operator = $("<select></select>").addClass("form-control input-sm operator");
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

function displayStepOptionsModal(step, htmlElement) {

    var user = getUser();
    $("#modalStepOptions").find("h5").text("Override Step Option Values");

    $("#stepLoop").val("");
    $("#stepConditionOperator").val("");
    $("#stepConditionVal1").val("");
    $("#stepConditionVal2").val("");
    $("#stepConditionVal3").val("");
    $("#stepForceExe").prop("checked", false);
    $("#timeoutStepConditionVal").val("");
    $("#timeoutStepConditionAct").prop("checked", false);
    $("#highlightStepConditionVal").val("");
    $("#highlightStepConditionAct").prop("checked", false);
    $("#minSimilarityStepConditionVal").val("");
    $("#minSimilarityStepConditionAct").prop("checked", false);
    $("#typeDelayStepConditionVal").val("");
    $("#typeDelayStepConditionAct").prop("checked", false);


//FATAL
    if (step.isExecutionForced) {
        $("#stepForceExe").prop("checked", true);
    }
//END OF FATAL

//LOOP
    $("#stepLoop").replaceWith(getSelectInvariant("STEPLOOP", false, false).css("width", "100%").addClass("form-control input-sm").attr("id", "stepLoop"));
    $("#stepLoop").val(step.loop);
//END OF LOOP

//CONDITION
    $("#stepConditionOperator").empty();
    for (var key in conditionNewUIList) {
        $("#stepConditionOperator").append($("<option></option>").text(conditionNewUIList[key].label[user.language]).val(conditionNewUIList[key].value));
    }
    $("#stepConditionOperator").val(step.conditionOperator);
    $("#stepConditionVal1").val(step.conditionValue1);
    $("#stepConditionVal2").val(step.conditionValue2);
    $("#stepConditionVal3").val(step.conditionValue3);
    setPlaceholderCondition($("#stepConditionOperator"));
//END OF CONDITION

    $("#stepConditionOperator").on("change", function () {
        setModif(true);
        setPlaceholderCondition($(this));
    });
    $("#stepConditionVal1").on("change", function () {
        setModif(true);
    });
    $("#stepConditionVal2").on("change", function () {
        setModif(true);
    });
    $("#stepConditionVal3").on("change", function () {
        setModif(true);
    });
    $("#stepLoop").on("change", function () {
        setModif(true);
    });
    $("#stepForceExe").on("change", function () {
        setModif(true);
    });

    setOptionModal(step.conditionOptions, "StepCondition");

    //EVENT ON SAVE
    $("#optionStepSave").off("click");
    $("#optionStepSave").click(function () {

        step.isExecutionForced = $("#stepForceExe").is(':checked');
        step.conditionOperator = $("#stepConditionOperator").val();
        step.conditionValue1 = $("#stepConditionVal1").val();
        step.conditionValue2 = $("#stepConditionVal2").val();
        step.conditionValue3 = $("#stepConditionVal3").val();
        step.loop = $("#stepLoop").val();


        let newConditionOpts = [];
        newConditionOpts.push({
            "act": $("#timeoutStepConditionAct").prop("checked"),
            "value": $("#timeoutStepConditionVal").val(),
            "option": "timeout"
        });
        newConditionOpts.push({
            "act": $("#minSimilarityStepConditionAct").prop("checked"),
            "value": $("#minSimilarityStepConditionVal").val(),
            "option": "minSimilarity"
        });
        newConditionOpts.push({
            "act": $("#highlightStepConditionAct").prop("checked"),
            "value": $("#highlightStepConditionVal").val(),
            "option": "highlightElement"
        });
        newConditionOpts.push({
            "act": $("#typeDelayStepConditionAct").prop("checked"),
            "value": $("#typeDelayStepConditionVal").val(),
            "option": "typeDelay"
        });

        if (JSON.stringify(step.conditionOptions) !== JSON.stringify(newConditionOpts)) {
            step.conditionOptions = newConditionOpts;
            setModif(true);
        }

        //printLabelForOptions($($(htmlElement)[0]).find(".secondRow"), newOpts,"actionOption");
        //printLabelForOptions($($(htmlElement)[0]).find(".secondRow"), newConditionOpts,"conditionOption");
        //printLabelForCondition(secondRow,action.conditionOperator);
        //printLabelForFatal(action.isFatal, $($(htmlElement)[0]).find(".secondRow"));

    });
}
;

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
        this.controls = [];
    }

    this.toDelete = false;
    this.hasPermissionsUpdate = canUpdate;
}

function getClassNever(object, addClass) {
    return $("<span title='" + object + " will not be executed.'></span>").addClass("fa fa-times " + addClass);
}

function getClassFatal(object, addClass) {
    return $("<span title='" + object + " will stop in case of error.'></span>").addClass("fa fa-exclamation " + addClass);
}

function getClassForce(object, addClass) {
    return $("<span title='" + object + " will be forced to execute.'></span>").addClass("fa fa-arrow-down " + addClass);
}

function getClassWithCondition(object, addClass) {
    return $("<span title='" + object + " has condition defined.'></span>").addClass("fa fa-question " + addClass);
}

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

function displayOverrideOptionsModal(action, htmlElement) {

    var user = getUser();
    $("#modalOptions").find("h5").text("Override Option Values");

    //INIT MODAL
    initOptionModal("");
    initOptionModal("Condition");
    $("#fatalCheckbox").prop("checked", false);
    $("#conditionSelect").val("");
    $("#actionconditionval1").val("");
    $("#actionconditionval2").val("");
    $("#actionconditionval3").val("");

    //FEED FIELDS
    //OPTIONS
    setOptionModal(action.options, "");
    setOptionModal(action.conditionOptions, "Condition");

    //FATAL
    if (action.isFatal) {
        $("#fatalCheckbox").prop("checked", true);
    }

    //CONDITION
    $("#conditionSelect").empty();
    for (var key in conditionNewUIList) {
        $("#conditionSelect").append($("<option></option>").text(conditionNewUIList[key].label[user.language]).val(conditionNewUIList[key].value));
    }

    $("#conditionSelect").val(action.conditionOperator);
    $("#actionconditionval1").val(action.conditionValue1);
    $("#actionconditionval2").val(action.conditionValue2);
    $("#actionconditionval3").val(action.conditionValue3);
    setPlaceholderCondition($("#conditionSelect"));

    $("#conditionSelect").on("change", function () {
        setModif(true);
        setPlaceholderCondition($(this));
    });
    $("#actionconditionval1").on("change", function () {
        setModif(true);
    });
    $("#actionconditionval2").on("change", function () {
        setModif(true);
    });
    $("#actionconditionval3").on("change", function () {
        setModif(true);
    });
    $("#fatalCheckbox").on("change", function () {
        setModif(true);
    });

    //EVENT ON SAVE
    $("#optionsSave").off("click");
    $("#optionsSave").click(function () {

        action.isFatal = $("#fatalCheckbox").is(':checked');

        action.conditionOperator = $("#conditionSelectContainer").find('select').val();
        action.conditionValue1 = $("#actionconditionval1").val();
        action.conditionValue2 = $("#actionconditionval2").val();
        action.conditionValue3 = $("#actionconditionval3").val();

        let newOpts = [];
        newOpts.push({"act": $("#timeoutAct").prop("checked"), "value": $("#timeoutVal").val(), "option": "timeout"});
        newOpts.push({
            "act": $("#minSimilarityAct").prop("checked"),
            "value": $("#minSimilarityVal").val(),
            "option": "minSimilarity"
        });
        newOpts.push({
            "act": $("#highlightAct").prop("checked"),
            "value": $("#highlightVal").val(),
            "option": "highlightElement"
        });
        newOpts.push({
            "act": $("#typeDelayAct").prop("checked"),
            "value": $("#typeDelayVal").val(),
            "option": "typeDelay"
        });

        if (JSON.stringify(action.options) !== JSON.stringify(newOpts)) {
            action.options = newOpts;
            setModif(true);
        }

        let newConditionOpts = [];
        newConditionOpts.push({
            "act": $("#timeoutConditionAct").prop("checked"),
            "value": $("#timeoutConditionVal").val(),
            "option": "timeout"
        });
        newConditionOpts.push({
            "act": $("#minSimilarityConditionAct").prop("checked"),
            "value": $("#minSimilarityConditionVal").val(),
            "option": "minSimilarity"
        });
        newConditionOpts.push({
            "act": $("#highlightConditionAct").prop("checked"),
            "value": $("#highlightConditionVal").val(),
            "option": "highlightElement"
        });
        newConditionOpts.push({
            "act": $("#typeDelayConditionAct").prop("checked"),
            "value": $("#typeDelayConditionVal").val(),
            "option": "typeDelay"
        });

        if (JSON.stringify(action.conditionOptions) !== JSON.stringify(newConditionOpts)) {
            action.conditionOptions = newConditionOpts;
            setModif(true);
        }

        printLabelForOptions($($($(htmlElement)[0]).find(".boutonGroup")[0]).parent(), newOpts, newConditionOpts, "optionLabel");
        printLabelForCondition($($($(htmlElement)[0]).find(".boutonGroup")[0]).parent(), action.conditionOperator, action.conditionValue1, action.conditionValue2, action.conditionValue3);
        printLabel($($($(htmlElement)[0]).find(".boutonGroup")[0]).parent(), action.isFatal, "actionFatalLabel", "labelOrange", "Stop Execution on Failure")
    });
}
;

function formatActionSelect2Result(state) {
    if (typeof $($(state.element)[0]).attr("data-picto") !== 'undefined') {
        return $($($(state.element)[0]).attr("data-picto") + '<span> ' + state.text + '</span>');
    } else {
        return $('<span>' + state.text + '</span>');
    }
}
;

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

Action.prototype.generateContent = function () {
    var action = this;
    var doc = new Doc();
    var row = $("<div></div>").addClass("step-action row").addClass("action");
    var content = $("<div></div>").addClass("content col-lg-8");
    var firstRow = $("<div style='margin-top:15px;margin-left:0px'></div>").addClass("fieldRow row input-group marginBottom10 col-lg-12");
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
    var descriptionField = $("<input class='description form-control crb-autocomplete-variable' placeholder='" + doc.getDocLabel("page_testcasescript", "describe_action") + "'>").attr("style", "border:0px");
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
    var value1Field = $("<input>").attr("data-toggle", "tooltip").attr("data-animation", "false").attr("data-html", "true").attr("data-container", "body").attr("data-placement", "top").attr("data-trigger", "manual").attr("type", "text").addClass("form-control input-sm v1");
    value1Field.val(cleanErratum(this.value1));
    value1Field.on("change", function () {
        action.value1 = convertValueWithErratum(action.value1, value1Field.val());
    });

    var field1Container = $("<div class='input-group'></div>");
    var field1Addon = $("<span></span>").attr("id", "field1Addon").addClass("input-group-addon").attr("style", "font-weight: 700;");
    field1Addon.append("<img width='15px' height='15px' src='images/action-website.png'>");
    value1Field.attr("aria-describedby", "field1Addon");
    field1Container.append(field1Addon).append(value1Field);
//END OF VALUE1 FIELD

//VALUE2 FIELD
    var value2Field = $("<input>").attr("data-toggle", "tooltip").attr("data-animation", "false").attr("data-html", "true").attr("data-container", "body").attr("data-placement", "top").attr("data-trigger", "manual").attr("type", "text").addClass("form-control input-sm v2");
    value2Field.val(cleanErratum(this.value2));
    value2Field.on("change", function () {
        action.value2 = convertValueWithErratum(action.value2, value2Field.val());
    });

    var field2Container = $("<div class='input-group'></div>");
    var field2Addon = $("<span></span>").attr("id", "field2Addon").addClass("input-group-addon").attr("style", "font-weight: 700;");
    field2Addon.append("<img width='15px' height='15px' src='images/action-website.png'>");
    value2Field.attr("aria-describedby", "field2Addon");
    field2Container.append(field2Addon).append(value2Field);
//END OF VALUE2 FIELD

//VALUE3 FIELD
    var value3Field = $("<input>").attr("data-toggle", "tooltip").attr("data-animation", "false").attr("data-html", "true").attr("data-container", "body").attr("data-placement", "top").attr("data-trigger", "manual").attr("type", "text").addClass("form-control input-sm v3");
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
    secondRow.append($("<div></div>").addClass("v1 col-lg-5 form-group marginBottom10").append(field1Container));
    secondRow.append($("<div></div>").addClass("v2 col-lg-2 form-group marginBottom10").append(field2Container));
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


function initOptionModal(context) {
    $("#timeout" + context + "Val").val("");
    $("#timeout" + context + "Act").prop("checked", false);
    $("#highlight" + context + "Val").val("");
    $("#highlight" + context + "Act").prop("checked", false);
    $("#minSimilarity" + context + "Val").val("");
    $("#minSimilarity" + context + "Act").prop("checked", false);
    $("#typeDelay" + context + "Val").val("");
    $("#typeDelay" + context + "Act").prop("checked", false);
}

function setOptionModal(actionOption, context) {
    for (var item in actionOption) {
        switch (actionOption[item].option) {
            case "timeout":
                $("#timeout" + context + "Val").val(actionOption[item].value);
                $("#timeout" + context + "Act").prop("checked", actionOption[item].act);
                break;
            case "highlightElement":
                $("#highlight" + context + "Val").val(actionOption[item].value);
                $("#highlight" + context + "Act").prop("checked", actionOption[item].act);
                break;
            case "minSimilarity":
                $("#minSimilarity" + context + "Val").val(actionOption[item].value);
                $("#minSimilarity" + context + "Act").prop("checked", actionOption[item].act);
                break;
            case "typeDelay":
                $("#typeDelay" + context + "Val").val(actionOption[item].value);
                $("#typeDelay" + context + "Act").prop("checked", actionOption[item].act);
                break;
            default:
                break;
        }
    }
}

function hasOneOptionActive(options) {
    for (var item in options) {
        if (options[item].act)
            return true;
    }
    return false;
}

function printLabelForOptions(element, newOpts, newOptsCondition, className) {
    $(element).find('.' + className).remove();
    let overrideOption = false;
    let title = "";

    for (optionObject in newOpts) {
        if (newOpts[optionObject].act) {
            title = title + "option : " + newOpts[optionObject].option + "=" + newOpts[optionObject].value + " \n\ ";
            overrideOption = true;
        }
    }
    for (optionObject in newOptsCondition) {
        if (newOptsCondition[optionObject].act) {
            title = title + "condition option : " + newOptsCondition[optionObject].option + "=" + newOptsCondition[optionObject].value + " \n\ ";
            overrideOption = true;
        }
    }
    if (overrideOption) {
        var labelOptions = $('<span data-toggle="tooltip" data-original-title="' + title + '" class="label label-primary labelBlue pull-right optionLabel ' + className + '"><span class="glyphicon glyphicon-cog"></span> Override Parameter</span>');
        $(element).append(labelOptions[0]);
    }
}

function printLabelForCondition(element, conditionOperator, conditionValue1, conditionValue2, conditionValue3) {
    $(element).find('.conditionLabel').remove();
    if (conditionOperator === 'never') {
        var labelOptions = $('<span class="label label-primary labelRed optionLabel pull-right conditionLabel"><span class="glyphicon glyphicon-cog"></span> Do not execute</span>');
        $(element).append(labelOptions[0]);
    } else if (conditionOperator !== 'always') {
        var title = "<div><b>Execution Condition : </b></div>";
        title += "<div>'Condition' : '" + conditionOperator + "'</div>";
        title += "<div>'val1' : '" + conditionValue1 + "'</div>";
        title += "<div>'val2' : '" + conditionValue2 + "'</div>";
        title += "<div>'val3' : '" + conditionValue3 + "'</div>";
        var labelOptions = $('<span data-toggle="tooltip" data-html="true"  data-original-title="' + title + '" class="label label-primary labelGreen pull-right optionLabel conditionLabel"><span class="glyphicon glyphicon-cog"></span> Conditional Execution</span>');
        $(element).append(labelOptions[0]);
    }
}

function printLabelForFatal(isFatal, element) {
    $(element).find('.actionFatalLabel').remove();
    if (isFatal) {
        var labelOptions = $('<span class="label label-primary labelOrange optionLabel pull-right actionFatalLabel"><span class="glyphicon glyphicon-cog"></span> Stop test on failure</span>');
        $(element).append(labelOptions[0]);
    }
}


function printLabel(element, displayBoolean, identifierClass, colorClass, text) {
    $(element).find('.' + identifierClass).remove();
    if (displayBoolean) {
        var labelOptions = $('<span class="label label-primary ' + colorClass + ' optionLabel ' + identifierClass + ' pull-right "><span class="glyphicon glyphicon-cog"></span> ' + text + '</span>');
        $(element).append(labelOptions[0]);
    }
}


function getTitleFromOptionsActive(options) {
    let result = "";
    for (var item in options) {
        if (options[item].act)
            result = result + options[item].option + "=" + options[item].value + " / ";
    }
    if (result === "") {
        return "Configure Options";
    }
    return result.substring(0, result.length - 3);
}

function convertValueWithErratum(oldValue, newValue) {
    if (newValue.startsWith('erratum=')) {
        setModif(true);
        let newXpath = newValue.split(',')[0];
        let newSource = newValue.split(newXpath)[1];
        let oldXpath = oldValue.split(',')[0];
        let oldSource = oldValue.split(oldXpath)[1];
        if (newValue.endsWith("[HTML-SOURCE-CONTENT]")) {
            return newXpath + oldSource;
        } else {
            return newValue;
        }
    } else {
        setModif(true);
        return newValue;
    }

}

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

    return json;
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
    }

    this.parentStep = parentAction.parentStep;
    this.parentAction = parentAction;
    this.parentActionSort = parentAction.sort;

    this.toDelete = false;
    this.hasPermissionsUpdate = canUpdate;

    this.html = $("<div></div>").addClass("step-action row").addClass("control");
}

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

Control.prototype.generateContent = function () {
    var control = this;
    var doc = new Doc();
    var row = this.html;
    var content = $("<div></div>").addClass("content col-lg-8");
    var firstRow = $("<div style='margin-top:15px;margin-left:0px'></div>").addClass("fieldRow row input-group marginBottom10 col-lg-12");
    var secondRow = $("<div></div>").addClass("fieldRow row secondRow input-group col-lg-12");
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
    var descriptionField = $("<input class='description form-control crb-autocomplete-variable' style='border:0px' placeholder='" + doc.getDocLabel("page_testcasescript", "describe_control") + "'>");
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
    var controlValue1Field = $("<input>").attr("data-toggle", "tooltip").attr("data-animation", "false").attr("data-html", "true").attr("data-container", "body").attr("data-placement", "top").attr("data-trigger", "manual").addClass("form-control input-sm v1");
    controlValue1Field.val(cleanErratum(this.value1));
    controlValue1Field.css("width", "84%");
    controlValue1Field.on("change", function () {
        setModif(true);
        control.value1 = convertValueWithErratum(control.value1, controlValue1Field.val());
    });
    var controlField1Container = $("<div class='input-group'></div>");
    var controlField1Addon = $("<span></span>").attr("id", "controlField1Addon").addClass("input-group-addon").attr("style", "font-weight: 700;");
    controlField1Addon.append("<img width='15px' height='15px' src='images/action-website.png'>");
    controlValue1Field.attr("aria-describedby", "controlField1Addon");
    controlField1Container.append(controlField1Addon).append(controlValue1Field);
//END OF VALUE1 FIELD

//VALUE2 FIELD
    var controlValue2Field = $("<input>").attr("data-toggle", "tooltip").attr("data-animation", "false").attr("data-html", "true").attr("data-container", "body").attr("data-placement", "top").attr("data-trigger", "manual").addClass("form-control input-sm v2").css("width", "100%");
    controlValue2Field.val(cleanErratum(this.value2));
    controlValue2Field.css("width", "84%");
    controlValue2Field.on("change", function () {
        setModif(true);
        control.value2 = convertValueWithErratum(control.value2, controlValue2Field.val());
    });
    var controlField2Container = $("<div class='input-group'></div>");
    var controlField2Addon = $("<span></span>").attr("id", "controlField2Addon").addClass("input-group-addon").attr("style", "font-weight: 700;");
    controlField2Addon.append("<img width='15px' height='15px' src='images/action-website.png'>");
    controlValue2Field.attr("aria-describedby", "controlField2Addon");
    controlField2Container.append(controlField2Addon).append(controlValue2Field);
//END OF VALUE2 FIELD

//VALUE3 FIELD
    var controlValue3Field = $("<input>").attr("data-toggle", "tooltip").attr("data-animation", "false").attr("data-html", "true").attr("data-container", "body").attr("data-placement", "top").attr("data-trigger", "manual").addClass("form-control input-sm v3").css("width", "100%");
    controlValue3Field.val(this.value3);
    controlValue3Field.css("width", "84%");
    controlValue3Field.on("change", function () {
        setModif(true);
        control.value3 = controlValue3Field.val();
    });
    var controlField3Container = $("<div class='input-group'></div>");
    var controlField3Addon = $("<span></span>").attr("id", "controlField3Addon").addClass("input-group-addon").attr("style", "font-weight: 700;");
    controlField3Addon.append("<img width='15px' height='15px' src='images/action-website.png'>");
    controlValue3Field.attr("aria-describedby", "controlField3Addon");
    controlField3Container.append(controlField3Addon).append(controlValue3Field);
//END OF VALUE3 FIELD

    firstRow.append(descContainer);
    secondRow.append(controlDivContainer);
    secondRow.append($("<div></div>").addClass("v1 col-lg-3 form-group marginBottom10").append(controlField1Container));
    secondRow.append(controlOperatorDivContainer);
    secondRow.append($("<div></div>").addClass("v2 col-lg-3 form-group marginBottom10").append(controlField2Container));
    secondRow.append($("<div></div>").addClass("v3 col-lg-3 form-group marginBottom10").append(controlField3Container));


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

    return json;
};

/**
 * Call Add Action and focus to next description when focusing on description
 * and clicking on enter
 *
 * @returns {undefined}
 */
function listenEnterKeypressWhenFocusingOnDescription(element) {
    $(element).find("input[class='description form-control']").each(function (index, field) {
        $(field).off('keydown');
        $(field).on('keydown', function (e) {
            if (e.which === 13) {
                // if description is not empty, create new action
                if ($(field)[0].value.length !== 0) {
                    addActionAndFocus();
                } else {
                    // if description is empty, create action or control
                    // depending on field
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
 * Find into tag array if object exist
 *
 * @param tagToUse
 * @param label
 *            string to search
 *
 * @return a boolean : true if exist, false if not exist
 */
function objectIntoTagToUseExist(tagToUse, label) {
    for (var i = 0; i < tagToUse.array.length; i++) {
        var data = tagToUse.array[i];

        if (data === undefined) {
            continue;
        }

        if (typeof data === "string") {
            if (data === label)
                return true;
        } else {
            if (data.object === label) {
                return true;
            } else if (data.service === label) {
                return true;
            }
        }
    }
    return false;
}

function loadGuiProperties() {

    let propArr = new Object();

    $("div.list-group-item").each(function () {
        var editor = ace.edit($(this).find("pre").attr("id"));
        let info = new Object();
        info["name"] = $(this).find("#propName").val();
        info["type"] = $(this).find("select").val();
        info["value"] = editor.getValue();
        if (!($(this).find("#propName").val() in propArr)) {
            propArr[$(this).find("#propName").val()] = info;
        }
    });
    return propArr;
}

var autocompleteAllFields, getTags, setTags, handlerToDeleteOnStepChange = [];

(function () {
    // var accessible only in closure
    var TagsToUse = [];
    var tcInfo = [];
    var contextInfo = [];

    getTags = function () {
        return TagsToUse;
    };
    setTags = function (tags) {
        TagsToUse = tags;
    };

    // function accessible everywhere that has access to TagsToUse
    autocompleteAllFields = function (configs, context, Tags) {
        
        if (Tags !== undefined) {
            TagsToUse = Tags;
        }

        if (configs !== undefined) {
            tcInfo = configs.property.contentTable[0];
        }

        if (context !== undefined) {
            contextInfo = context;
        }


        function initAutocompleteElement(e) {
//            console.log("start feed autocomplete on focus (element).");
            initAutocompleteWithTags($(this), configs, contextInfo);
        }

        function initAutocompleteService(e) {
//            console.log("start feed autocomplete on focus (service).");
            initAutocompleteforSpecificFields($(this));
        }

        function initAutocompleteProperty(e) {
//            console.log("start feed autocomplete on focus (property).");
            initAutocompleteforSpecificFields($(this));
            $(this).autocomplete("search", "");
        }


        function   initAutocompleteVariablesOnly(e) {
//            console.log("start feed autocomplete on focus (variable only).");
            configs.indentifier = false;
            initAutocompleteWithTagsNoElement($(this), configs, contextInfo);

        }

        function modifyAutocompleteService(e) {
//            console.log("modify feed autocomplete on input (service).")
            let data = loadGuiProperties();
            try {
                if ($(this).parents(".secondRow").find("[name='actionSelect']").val() === "callService") {
                    let url = "ReadAppService?service=" + encodeURI($(this).val()) + "&limit=15";
                    modifyAutocompleteSource($(this), url);
                } else if ($(this).parents(".secondRow").find("[name='actionSelect']").val() === "calculateProperty") {
                    modifyAutocompleteSource($(this), null, data);
                }
            } catch (e) {
            }

            $(this).trigger("settingsButton");
        }

        function modifyAutocompleteProperty(e) {
//            console.log("modify feed autocomplete on input (property).")
            let data = loadGuiProperties();
            try {
                modifyAutocompleteSource($(this), null, data);
            } catch (e) {
            }
            console.log("trigger settingsButton.");

            $(this).trigger("settingsButton");
        }

        function modifyAutocomplete(e) {
            console.log("modify feed autocomplete on input (generic).")
            console.log("trigger settingsButton.");
            $(this).trigger("settingsButton");
        }



        // Adding Autocomplete on all fields. ##### crb-autocomplete-varaible (include Variables ONLY) #####
        $(document).on('focus', "div.crb-autocomplete-variable input:not([readonly])", initAutocompleteVariablesOnly);
        $(document).on('input', "div.crb-autocomplete-variable input:not([readonly])", modifyAutocomplete);
        $(document).on('focus', "input.crb-autocomplete-variable:not([readonly])", initAutocompleteVariablesOnly);
        $(document).on('input', "input.crb-autocomplete-variable:not([readonly])", modifyAutocomplete);

        // Adding Autocomplete on all fields. ##### crb-autocomplete-element (include Services+Variables) #####
        $(document).on('focus', "div.crb-autocomplete-service input:not([readonly])", initAutocompleteService);
        $(document).on('input', "div.crb-autocomplete-service input:not([readonly])", modifyAutocompleteService);
        $(document).on('focus', "input.crb-autocomplete-service:not([readonly])", initAutocompleteService);
        $(document).on('input', "input.crb-autocomplete-service:not([readonly])", modifyAutocompleteService);

        // Adding Autocomplete on all fields. ##### crb-autocomplete-element (include Properties+Variables) #####
        $(document).on('focus', "div.crb-autocomplete-property input:not([readonly])", initAutocompleteProperty);
        $(document).on('input', "div.crb-autocomplete-property input:not([readonly])", modifyAutocompleteProperty);
        $(document).on('focus', "input.crb-autocomplete-property:not([readonly])", initAutocompleteProperty);
        $(document).on('input', "input.crb-autocomplete-property:not([readonly])", modifyAutocompleteProperty);

        // Adding Autocomplete on all fields. ##### crb-autocomplete-element (include Elements+Variables) #####
        $(document).on('focus', "div.crb-autocomplete-element input:not([readonly])", initAutocompleteElement);
        $(document).on('input', "div.crb-autocomplete-element input:not([readonly])", modifyAutocomplete);
        $(document).on('focus', "input.crb-autocomplete-element:not([readonly])", initAutocompleteElement);
        $(document).on('input', "input.crb-autocomplete-element:not([readonly])", modifyAutocomplete);

        // Adding Contextual buttons when 'settingsButton' event is triggered.
        $(document).on('settingsButton', "div.crb-contextual-button input", function (e) {
            console.log("start generate buttons.");
            var doc = new Doc();
            let currentAction = $(this).parents(".secondRow").find("[name='actionSelect']").val();
            let htmlElement = $(this);
            $(htmlElement).parent().find(".input-group-btn").remove();
            switch (currentAction) {
                case 'callService':
                    if (htmlElement.val()) {
                        $.ajax({
                            url: "ReadAppService?service=" + encodeURI(htmlElement.val()),
                            dataType: "json",
                            success: function (data) {
                                var dataContent = data.contentTable;
                                if ($(htmlElement).parents(".secondRow").find(".v1").val() !== undefined) {
                                    if (dataContent.hasPermissions !== undefined) {
                                        var editEntry = $('<span class="input-group-btn ' + encodeURIComponent(htmlElement.val()) + '"><button id="editEntry" onclick="openModalAppService(\'' + encodeURIComponent(htmlElement.val()) + '\',\'EDIT\'  ,\'TestCase\' );"\n\
        								class="buttonObject btn btn-default input-sm " \n\
        								title="' + doc.getDocLabel("page_applicationObject", "button_edit") + '" type="button">\n\
        						<span class="glyphicon glyphicon-pencil"></span></button></span>');
                                        $(htmlElement).parent().append(editEntry);
                                    } else {
                                        var addEntry = '<span class="input-group-btn ' + encodeURIComponent(htmlElement.val()) + '"><button id="editEntry" onclick="openModalAppService(\'' + encodeURIComponent(htmlElement.val()) + '\',\'ADD\'  ,\'TestCase\' );"\n\
        						class="buttonObject btn btn-default input-sm " \n\
        						title="' + doc.getDocLabel("page_applicationObject", "button_create") + '" type="button">\n\
        						<span class="glyphicon glyphicon-plus"></span></button></span>';
                                        $(htmlElement).parent().append(addEntry);
                                    }
                                }
                            }
                        });
                    }
                    break;
                case 'calculateProperty':
                    let data = loadGuiProperties();
                    var viewEntry = $('<span class="input-group-btn ' + $(htmlElement).val() + '"><button id="editEntry" data-toggle="modal" data-target="#modalProperty" "\n\
        				class="buttonObject btn btn-default input-sm " \n\
        				title="' + doc.getDocLabel("page_applicationObject", "button_edit") + '" type="button">\n\
        				<span class="glyphicon glyphicon-eye-open"></span></button></span>');
                    if (data[$(htmlElement).val()]) {
                        viewEntry.find("button").off("click").on("click", function () {
                            let firstRow = $('<p style="text-align:center" > Type : ' + data[$(htmlElement).val()].type + '</p>');
                            let secondRow = $('<p style="text-align:center"> Value : ' + data[$(htmlElement).val()].value + '</p>');
                            $("#modalProperty").find("h5").text("Property definition");
                            $("#modalProperty").find("#firstRowProperty").find("p").remove();
                            $("#modalProperty").find("#secondRowProperty").find("p").remove();
                            $("#modalProperty").find("#firstRowProperty").append(firstRow);
                            $("#modalProperty").find("#secondRowProperty").append(secondRow);
                        });
                        $(htmlElement).parent().append(viewEntry);
                    }
                    break;
                default:
                    var name = undefined;
                    var nameNotExist = undefined;
                    var objectNotExist = false;
                    var typeNotExist = undefined;
                    var doc = new Doc();
                    var checkObject = [];
                    var betweenPercent = $(htmlElement).val().match(new RegExp(/%[^%]*%/g));
                    if (betweenPercent !== null && betweenPercent.length > 0) {
                        var i = betweenPercent.length - 1;
                        while (i >= 0) {
                            var findname = betweenPercent[i].match(/\.[^\.]*(\.|.$)/g);
                            if (betweenPercent[i].startsWith("%object.") && findname !== null && findname.length > 0) {
                                name = findname[0];
                                name = name.slice(1, name.length - 1);
                                if ($(this).hasClass("v1")) {
                                    $(htmlElement).parents(".step-action").find("#ApplicationObjectImg1")
                                            .attr("src", "ReadApplicationObjectImage?application=" + tcInfo.application + "&object=" + name + "&time=" + new Date().getTime())
                                            .attr("data-toggle", "tooltip").attr("title", name).attr("onclick", "displayPictureOfMinitature1(this)");
                                } else if ($(this).hasClass("v2")) {
                                    $(htmlElement).parents(".step-action").find("#ApplicationObjectImg2")
                                            .attr("src", "ReadApplicationObjectImage?application=" + tcInfo.application + "&object=" + name + "&time=" + new Date().getTime())
                                            .attr("data-toggle", "tooltip").attr("title", name).attr("onclick", "displayPictureOfMinitature1(this)");
                                } else if ($(this).hasClass("v3")) {
                                    $(htmlElement).parents(".step-action").find("#ApplicationObjectImg3")
                                            .attr("src", "ReadApplicationObjectImage?application=" + tcInfo.application + "&object=" + name + "&time=" + new Date().getTime())
                                            .attr("data-toggle", "tooltip").attr("title", name).attr("onclick", "displayPictureOfMinitature1(this)");
                                }
                                if (!objectIntoTagToUseExist(TagsToUse[1], name)) {
                                    var addEntry = $('<span class="input-group-btn many ' + name + '"><button id="editEntry" onclick="openModalApplicationObject(\'' + tcInfo.application + '\', \'' + name + '\',\'ADD\'  ,\'testCaseScript\' );"\n\
	                                		class="buttonObject btn btn-default input-sm " \n\
	                                		title="' + name + '" type="button">\n\
	                                <span class="glyphicon glyphicon-plus"></span></button></span>');
                                    objectNotExist = true;
                                    nameNotExist = name;
                                    typeNotExist = "applicationObject";
                                    $(htmlElement).attr("style", "width:80%").parent().append(addEntry);
                                } else if (objectIntoTagToUseExist(TagsToUse[1], name)) {
                                    var editEntry = '<span class="input-group-btn many ' + name + '"><button id="editEntry" onclick="openModalApplicationObject(\'' + tcInfo.application + '\', \'' + name + '\',\'EDIT\'  ,\'testCaseScript\' );"\n\
	                                class="buttonObject btn btn-default input-sm " \n\
	                                title="' + name + '" type="button">\n\
	                                <span class="glyphicon glyphicon-pencil"></span></button></span>';
                                    $(htmlElement).attr("style", "width:80%").parent().append(editEntry);
                                }
                            } else if (betweenPercent[i].startsWith("%property.") && findname !== null && findname.length > 0) {
                                let data = loadGuiProperties();
                                name = findname[0];
                                name = name.slice(1, name.length - 1);
                                if (objectIntoTagToUseExist(TagsToUse[2], name)) {
                                    var viewEntry = $('<span class="input-group-btn many ' + name + '"><button id="editEntry" data-toggle="modal" data-target="#modalProperty" "\n\
	                                		class="buttonObject btn btn-default input-sm " \n\
	                                		title="' + name + '" type="button">\n\
	                                <span class="glyphicon glyphicon-eye-open"></span></button></span>');
                                    if (data[name]) {
                                        let property = name;
                                        viewEntry.find("button").on("click", function () {
                                            let firstRow = $('<p style="text-align:center" > Type : ' + data[property].type + '</p>');
                                            let secondRow = $('<p style="text-align:center"> Value : ' + data[property].value + '</p>');
                                            $("#modalProperty").find("#firstRowProperty").find("p").remove();
                                            $("#modalProperty").find("#secondRowProperty").find("p").remove();
                                            $("#modalProperty").find("#firstRowProperty").append(firstRow);
                                            $("#modalProperty").find("#secondRowProperty").append(secondRow);
                                            $("#modalProperty").find(".modal-title").html(property);
                                        });
                                        $(htmlElement).parent().append(viewEntry);
                                    }
                                }
                            }
                            i--;
                        }
                    }
            }
        });

        $("div.step-action .content div.fieldRow:nth-child(2) input").trigger("settingsButton");
    };
})();


function removeTestCaseClick(test, testCase) {
    clearResponseMessageMainPage();
    var doc = new Doc();
    var messageComplete = doc.getDocLabel("page_testcase", "message_delete");
    messageComplete = messageComplete.replace("%ENTRY%", test + " / " + testCase);
    showModalConfirmation(deleteTestCaseHandlerClick, undefined, "Delete", messageComplete, test, testCase, "", "");
}

/*
 * Function called when confirmation button pressed @returns {undefined}
 */
function deleteTestCaseHandlerClick() {
    var test = $('#confirmationModal').find('#hiddenField1').prop("value");
    var testCase = $('#confirmationModal').find('#hiddenField2').prop("value");
    var jqxhr = $.post("DeleteTestCase", {test: test, testCase: testCase}, "json");
    $.when(jqxhr).then(function (data) {
        var messageType = getAlertType(data.messageType);
        if (messageType === "success") {
            window.location = "./TestCaseScript.jsp?test=" + encodeURI(test);
        }
        // show message in the main page
        showMessageMainPage(messageType, data.message, false);
        // close confirmation window
        $('#confirmationModal').modal('hide');
    }).fail(handleErrorAjaxAfterTimeout);
}

function setPlaceholderAction(action) {
    var user = getUser();
    var actionElement = $(action).find("[name='actionSelect'] option:selected");
    var placeHolders = actionOptList[actionElement.val()];

    if (typeof placeHolders === 'undefined') {
        placeHolders = actionOptList["unknown"];
    }

    if (typeof placeHolders.field1 !== 'undefined') {
        $(actionElement).parents("div[class*='secondRow']").children("div[class*='v1']").removeClass("col-lg-2 col-lg-3 col-lg-4 col-lg-5 col-lg-6 col-lg-7 col-lg-8 col-lg-9 crb-autocomplete-element crb-autocomplete-property crb-autocomplete-service crb-autocomplete-variable crb-contextual-button").addClass(placeHolders.field1.class);
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
        $(actionElement).parents("div[class*='secondRow']").children("div[class*='v2']").removeClass("col-lg-2 col-lg-3 col-lg-4 col-lg-5 col-lg-6 col-lg-7 col-lg-8 col-lg-9 crb-autocomplete-element crb-autocomplete-property crb-autocomplete-service crb-autocomplete-variable crb-contextual-button").addClass(placeHolders.field2.class);
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
        $(actionElement).parents("div[class*='secondRow']").children("div[class*='v3']").removeClass("col-lg-2 col-lg-3 col-lg-4 col-lg-5 col-lg-6 col-lg-7 col-lg-8 col-lg-9 crb-autocomplete-element crb-autocomplete-property crb-autocomplete-service crb-autocomplete-variable crb-contextual-button").addClass(placeHolders.field3.class);
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

function setPlaceholderCondition(conditionElement) {

    console.log(conditionElement);
    var user = getUser();
    var placeHolders = conditionNewUIList[conditionElement.val()];

    console.log(placeHolders);

    if (typeof placeHolders === 'undefined') {
        placeHolders = conditionNewUIList["always"];
    }

    if (typeof placeHolders.field1 !== 'undefined') {
        // $(actionElement).parents("div[class*='secondRow']").children("div[class*='v1']").removeClass("col-lg-2 col-lg-3 col-lg-4 col-lg-5 col-lg-6 col-lg-7 col-lg-8 col-lg-9").addClass(placeHolders.field1.class);
        $(conditionElement).parents("div[class*='conditions']").find(".v1").find("input").removeClass("crb-autocomplete-element crb-autocomplete-property crb-autocomplete-service crb-autocomplete-variable crb-contextual-button").addClass(placeHolders.field1.class);
        $(conditionElement).parents("div[class*='conditions']").find("label[class='conditionVal1Label']").parent().show();
        $(conditionElement).parents("div[class*='conditions']").find("label[class='conditionVal1Label']").text(placeHolders.field1.label[user.language]);
        if (typeof placeHolders.field1.picto !== 'undefined') {
            //$(actionElement).parents("div[class*='secondRow']").children("div[class*='v1']").find('img').attr("src", placeHolders.field1.picto);
        }
    } else {
        $(conditionElement).parents("div[class*='conditions']").find("label[class='conditionVal1Label']").parent().hide();
    }

    if (typeof placeHolders.field2 !== 'undefined') {
        $(conditionElement).parents("div[class*='conditions']").find(".v2").find("input").removeClass("crb-autocomplete-element crb-autocomplete-property crb-autocomplete-service crb-autocomplete-variable crb-contextual-button").addClass(placeHolders.field2.class);
        $(conditionElement).parents("div[class*='conditions']").find("label[class='conditionVal2Label']").parent().show();
        $(conditionElement).parents("div[class*='conditions']").find("label[class='conditionVal2Label']").text(placeHolders.field2.label[user.language]);

    } else {
        $(conditionElement).parents("div[class*='conditions']").find("label[class='conditionVal2Label']").parent().hide();
    }

    if (typeof placeHolders.field3 !== 'undefined') {
        $(conditionElement).parents("div[class*='conditions']").find(".v3").find("input").removeClass("crb-autocomplete-element crb-autocomplete-property crb-autocomplete-service crb-autocomplete-variable crb-contextual-button").addClass(placeHolders.field3.class);
        $(conditionElement).parents("div[class*='conditions']").find("label[class='conditionVal3Label']").parent().show();
        $(conditionElement).parents("div[class*='conditions']").find("label[class='conditionVal3Label']").text(placeHolders.field3.label[user.language]);

    } else {
        $(conditionElement).parents("div[class*='conditions']").find("label[class='conditionVal3Label']").parent().hide();
    }

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
        control.find("div[class*='v1']").removeClass("col-lg-2 col-lg-3 col-lg-4 col-lg-5 col-lg-6 col-lg-7 col-lg-8 col-lg-9 crb-autocomplete-element crb-autocomplete-property crb-autocomplete-service crb-autocomplete-variable crb-contextual-button").addClass(placeHolders.field1.class);
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
        control.find("div[class*='v2']").removeClass("col-lg-2 col-lg-3 col-lg-4 col-lg-5 col-lg-6 col-lg-7 col-lg-8 col-lg-9 crb-autocomplete-element crb-autocomplete-property crb-autocomplete-service crb-autocomplete-variable crb-contextual-button").addClass(placeHolders.field2.class);
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
        control.find("div[class*='v3']").removeClass("col-lg-2 col-lg-3 col-lg-4 col-lg-5 col-lg-6 col-lg-7 col-lg-8 col-lg-9 crb-autocomplete-element crb-autocomplete-property crb-autocomplete-service crb-autocomplete-variable crb-contextual-button").addClass(placeHolders.field3.class);
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


function setPlaceholderProperty(propertyElement, property) {
    /**
     * Todo : GetFromDatabase Translate for FR
     */

    var user = getUser();
    var placeHolders = propertyUIList[user.language];

    $(propertyElement).find('select[name="propertyType"] option:selected').each(function (i, e) {


        function initChange() {

            if ($("#" + editor.container.id).parent().parent().find("[name='propertyType']").val() === "getFromDataLib") {
                $("#" + editor.container.id).parent().find('.input-group').remove();
                var escaped = encodeURIComponent(editor.getValue());
                if (!isEmpty(escaped)) {
                    $.ajax({
                        url: "ReadTestDataLib",
                        data: {
                            name: escaped,
                            limit: 15,
                            like: "N"
                        },
                        async: true,
                        method: "GET",
                        success: function (data) {
                            if (data.messageType === "OK") {
                                // Feed the data to the screen and manage
                                // authorities.
                                var service = data.contentTable;
                                if (service.length >= 2) {

                                    $("#" + editor.container.id).parent().find('.input-group').remove();
                                    $("#" + editor.container.id).parent().parent().find('.col-btn').remove();

                                    var editEntry = $('<div class="input-group col-sm-5 col-sm-offset-3"><label>Choose one data library</label><select class="datalib  form-control"></select><span class="input-group-btn"  style="vertical-align:bottom"><button class="btn btn-secondary" type="button"><span class="glyphicon glyphicon-pencil"></span></button></span></div>');
                                    $("#" + editor.container.id).parent().append(editEntry);

                                    displayDataLibList(editor.container.id, undefined, data);
                                    $("#" + editor.container.id).parent().find("button").attr('onclick', 'openModalDataLib(\'' + editor.container.id + "\','" + $("#" + editor.container.id).parent().find("select").val() + "\','EDIT'," + "'" + escaped + "')");
                                    $("#" + editor.container.id).parent().find("select").unbind("change").change(function () {
                                        $("#" + editor.container.id).parent().find("button").attr('onclick', 'openModalDataLib(\'' + editor.container.id + "\','" + $("#" + editor.container.id).parent().find("select").val() + "\','EDIT'," + "'" + escaped + "')");
                                    });


                                } else {
                                    $("#" + editor.container.id).parent().find('.input-group').remove();
                                    $("#" + editor.container.id).parent().parent().find('.col-btn').remove();
                                    if (service.length === 1) {
                                        var editEntry = $('<div class="col-btn col-sm-2" style="text-align:center"><label style="width:100%">Edit the DataLib</label><button class="btn btn-secondary" type="button"><span class="glyphicon glyphicon-pencil"></span></button></div>');
                                        var addEntry = $('<div class="col-btn col-sm-2" style="text-align:center"><label style="width:100%">Add the DataLib</label><button class="btn btn-secondary" type="button"><span class="glyphicon glyphicon-plus"></span></button></div>');
                                        $("#" + editor.container.id).parent().removeClass("col-sm-10").addClass("col-sm-8");
                                        $("#" + editor.container.id).parent().parent().append(editEntry);
                                        $("#" + editor.container.id).parent().parent().append(addEntry);
                                        $("#" + editor.container.id).parent().parent().find("button:eq(0)").attr('onclick', 'openModalDataLib(\'' + editor.container.id + "\','" + service[0].testDataLibID + "\','EDIT'," + "'" + escaped + "')");
                                        $("#" + editor.container.id).parent().parent().find("button:eq(1)").attr('onclick', 'openModalDataLib(\'' + editor.container.id + "\','" + escaped + "\','ADD'," + "'" + escaped + "')");
                                    } else {
                                        var addEntry = $('<div class="col-btn col-sm-2" style="text-align:center"><label style="width:100%">Add the DataLib</label><button class="btn btn-secondary ' + escaped + '" type="button"><span class="glyphicon glyphicon-plus"></span></button></div>');
                                        addEntry.find("button").attr("disabled", !canUpdate);
                                        $("#" + editor.container.id).parent().removeClass("col-sm-10").addClass("col-sm-8");
                                        $("#" + editor.container.id).parent().parent().append(addEntry);
                                        $("#" + editor.container.id).parent().parent().find("button").attr('onclick', 'openModalDataLib(\'' + editor.container.id + "\','" + escaped + "\','ADD'," + "'" + escaped + "')");
                                    }
                                }
                            }
                        },
                        error: showUnexpectedError
                    });
                }
            }
        }

        var editor = ace.edit($($(e).parents("div[name='propertyLine']").find("pre[name='propertyValue']"))[0]);

        editor.removeAllListeners('change');

        for (var i = 0; i < placeHolders.length; i++) {
            if (placeHolders[i].type === e.value) {
                if (placeHolders[i].database !== null) {
                    $(e).parents("div[name='propertyLine']").find("div[name='fieldDatabase']").show();
                    $(e).parents("div[name='propertyLine']").find("div[name='fieldDatabase'] label").html(placeHolders[i].database);
                } else {
                    $(e).parents("div[name='propertyLine']").find("div[name='fieldDatabase']").hide();
                }
                if (placeHolders[i].value1 !== null) {
                    $(e).parents("div[name='propertyLine']").find("div[name='fieldValue1']").show();
                    $(e).parents("div[name='propertyLine']").find("div[name='fieldValue1'] label").html(placeHolders[i].value1);
                    $(e).parents("div[name='propertyLine']").find("div[name='fieldValue1']").removeClass();
                    $(e).parents("div[name='propertyLine']").find("div[name='fieldValue1']").addClass(placeHolders[i].value1Class);
                    // Ace module management
                    configureAceEditor(editor, placeHolders[i].value1EditorMode, property);

                    if (placeHolders[i].type === "getFromDataLib") {
                        if ((editor.getValue() !== null)) {
                            initChange();
                        }
                        editor.on('change', initChange);

                    } else {
                        $("#" + editor.container.id).parent().children('.input-group').remove();
                        $("#" + editor.container.id).parent().parent().find('.col-btn').remove();
                    }

                } else {
                    $(e).parents("div[name='propertyLine']").find("div[name='fieldValue1']").hide();
                }
                if (placeHolders[i].value2 !== null) {
                    $(e).parents("div[name='propertyLine']").find("div[name='fieldValue2']").show();
                    $(e).parents("div[name='propertyLine']").find("div[name='fieldValue2'] label").html(placeHolders[i].value2);
                } else {
                    $(e).parents("div[name='propertyLine']").find("div[name='fieldValue2']").hide();
                }
                if (placeHolders[i].length !== null) {
                    $(e).parents("div[name='propertyLine']").find("div[name='fieldLength']").show();
                    $(e).parents("div[name='propertyLine']").find("div[name='fieldLength'] label").html(placeHolders[i].length);
                } else {
                    $(e).parents("div[name='propertyLine']").find("div[name='fieldLength']").hide();
                }
                if (placeHolders[i].rowLimit !== null) {
                    $(e).parents("div[name='propertyLine']").find("div[name='fieldRowLimit']").show();
                    $(e).parents("div[name='propertyLine']").find("div[name='fieldRowLimit'] label").html(placeHolders[i].rowLimit);
                } else {
                    $(e).parents("div[name='propertyLine']").find("div[name='fieldRowLimit']").hide();
                }
                if (placeHolders[i].nature !== null) {
                    $(e).parents("div[name='propertyLine']").find("div[name='fieldNature']").show();
                    $(e).parents("div[name='propertyLine']").find("div[name='fieldNature'] label").html(placeHolders[i].nature);
                } else {
                    $(e).parents("div[name='propertyLine']").find("div[name='fieldNature']").hide();
                }
                if (placeHolders[i].cacheExpire !== null) {
                    $(e).parents("div[name='propertyLine']").find("div[name='fieldExpire']").show();
                    $(e).parents("div[name='propertyLine']").find("div[name='fieldExpire'] label").html(placeHolders[i].cacheExpire);
                } else {
                    $(e).parents("div[name='propertyLine']").find("div[name='fieldExpire']").hide();
                }
                if (placeHolders[i].retry !== null) {
                    $(e).parents("div[name='propertyLine']").find("div[name='fieldRetryNb']").show();
                    $(e).parents("div[name='propertyLine']").find("div[name='fieldRetryNb'] label").html(placeHolders[i].retry);
                } else {
                    $(e).parents("div[name='propertyLine']").find("div[name='fieldRetryNb']").hide();
                }
                if (placeHolders[i].period !== null) {
                    $(e).parents("div[name='propertyLine']").find("div[name='fieldRetryPeriod']").show();
                    $(e).parents("div[name='propertyLine']").find("div[name='fieldRetryPeriod'] label").html(placeHolders[i].period);
                } else {
                    $(e).parents("div[name='propertyLine']").find("div[name='fieldRetryPeriod']").hide();
                }
                if (placeHolders[i].rank !== null) {
                    // condition will always be true
                    $(e).parents("div[name='propertyLine']").find("div[name='rank']").show();
                    $(e).parents("div[name='propertyLine']").find("div[name='rank'] label").html(placeHolders[i].rank);
                } else {
                    $(e).parents("div[name='propertyLine']").find("div[name='fieldRetryPeriod']").hide();
                }


            }
        }
    });

}


function CompleterForAllDataLib() {

    var langTools = ace.require("ace/ext/language_tools");
    langTools.setCompleters([]);// clear the autocompleter list

    var staticWordCompleter = {

        getCompletions: function (editor, session, pos, prefix, callback) {
            var escaped = encodeURIComponent(editor.getValue());
            $.getJSON("ReadTestDataLib?name=" + escaped + "&limit=15&like=Y", function (wordList) {
                callback(null, wordList.contentTable.map(function (ea) {
                    return {name: ea.name, value: ea.name, meta: "DataLib"};
                }));
            });
        }
    };

    langTools.addCompleter(staticWordCompleter);
}


var oldCompleters = null;

/*
 * main function of ace editor
 */
function configureAceEditor(editor, mode, property) {
    // command Name
    var commandNameForAutoCompletePopup = "cerberusPopup";
    var commandNameForIssueDetection = "cerberusIssueDetection";
    // event listenner
    editor.commands.on("afterExec", function (e) {
        var langTools = ace.require('ace/ext/language_tools');


        if (e.command.name === "insertstring" || e.command.name === "paste" || e.command.name === "backspace") {
            // recreate the array at each loop

            if (property.type === "getFromDataLib") {
                CompleterForAllDataLib();
                $("pre").off("input").on("input", function (e) {
                    editor.execCommand("startAutocomplete");
                });
                editor.setOptions({maxLines: 15, enableBasicAutocompletion: true, enableLiveAutocompletion: false});
            } else {
                editor.setOptions({maxLines: 15, enableBasicAutocompletion: true, enableLiveAutocompletion: false});
                var allKeyword = createAllKeywordList(getKeywordList("object"), getKeywordList("property"));
                // editor.completers = [allKeyword]
                if (e.command.name !== "backspace") {
                    addCommandForCustomAutoCompletePopup(editor, allKeyword, commandNameForAutoCompletePopup);
                    editor.commands.exec(commandNameForAutoCompletePopup);// set
                    // autocomplete
                    // popup*/
                } else {
                    addCommandToDetectKeywordIssue(editor, allKeyword, commandNameForIssueDetection);
                    editor.commands.exec(commandNameForIssueDetection);// set
                    // annotation
                }
            }
        }
        createGuterCellListenner(editor);
        property.value1 = editor.session.getValue();

    });

    // editor option
    editor.getSession().setMode(mode);
    editor.off('change');
    editor.setTheme("ace/theme/chrome");
    editor.$blockScrolling = "Infinity";// disable error message

    // set text previously input
    editor.setValue(property.value1);
    // lose focus when loaded
    var count = editor.getSession().getLength();
    editor.gotoLine(count, editor.getSession().getLine(count - 1).length);
}

/*
 * create an array of the current keyword with the keyword that precede them
 */
function createAllKeywordList(objectList, propertyList) {
    var availableObjectProperties = [
        "value",
//        "picturepath",
        "pictureurl"
    ];
    var availableSystemValues = [
        "SYSTEM",
        "APPLI",
        "BROWSER", "ROBOT", "ROBOTDECLI", "SCREENSIZE",
        "APP_DOMAIN", "APP_HOST", "APP_CONTEXTROOT", "EXEURL", "APP_VAR1", "APP_VAR2", "APP_VAR3", "APP_VAR4",
        "ENV", "ENVGP",
        "COUNTRY", "COUNTRYGP1", "COUNTRYGP2", "COUNTRYGP3", "COUNTRYGP4", "COUNTRYGP5", "COUNTRYGP6", "COUNTRYGP7", "COUNTRYGP8", "COUNTRYGP9",
        "TEST",
        "TESTCASE",
        "SSIP", "SSPORT",
        "TAG",
        "EXECUTIONID",
        "EXESTART", "EXEELAPSEDMS",
        "EXESTORAGEURL",
        "STEP.n.n.RETURNCODE", "CURRENTSTEP_INDEX", "CURRENTSTEP_STARTISO", "CURRENTSTEP_ELAPSEDMS", "CURRENTSTEP_SORT",
        "LASTSERVICE_HTTPCODE",
        "TODAY-yyyy", "TODAY-MM", "TODAY-dd", "TODAY-doy", "TODAY-HH", "TODAY-mm", "TODAY-ss",
        "YESTERDAY-yyyy", "YESTERDAY-MM", "YESTERDAY-dd", "YESTERDAY-doy", "YESTERDAY-HH", "YESTERDAY-mm", "YESTERDAY-ss",
        "TOMORROW-yyyy", "TOMORROW-MM", "TOMORROW-dd", "TOMORROW-doy"
    ];
    var availableTags = [
        "property", // 0
        "object", // 1
        "system"    // 2
    ];

    var allKeyword = [];
    allKeyword.push({"motherKeyword": null, "listKeyword": availableTags});
    // property
    allKeyword.push({"motherKeyword": availableTags["0"], "listKeyword": propertyList});
    // object
    allKeyword.push({"motherKeyword": availableTags["1"], "listKeyword": objectList});
    // system
    allKeyword.push({"motherKeyword": availableTags["2"], "listKeyword": availableSystemValues});
    // object tag
    for (var i in objectList) {
        allKeyword.push({"motherKeyword": objectList[i], "listKeyword": availableObjectProperties});
    }
    return allKeyword;
}

/*
 * add an ace command to display autocomplete popup
 */
function addCommandForCustomAutoCompletePopup(editor, allKeyword, commandName) {


    editor.commands.addCommand({
        name: commandName,
        exec: function () {
            var cursorPositionY = editor.getCursorPosition().row;
            var editorValueAtTheLine = editor.session.getLine(cursorPositionY);
            // value on the line the cursor is currently in
            var numberOfPercentCaractereAtLine = (editorValueAtTheLine.match(/\%/g) || []).length;
            // start autocomplete when there is an odd number of %

            if (numberOfPercentCaractereAtLine !== 0 && numberOfPercentCaractereAtLine % 2 === 1) {
                var cursorPositionX = editor.getCursorPosition().column;
                var subStringCursorOn = editorValueAtTheLine.slice(editorValueAtTheLine.lastIndexOf('%', cursorPositionX) + 1, cursorPositionX);
                // Create an array of all the word separated by "." contain
                // between "%" caractere
                var keywordInputList = subStringCursorOn.split(".");


                var potentiallyNeddApoint = true;
                var allKeywordCorrect = true;
                // Check all the keywordInput
                for (var idKeywordToCheck in keywordInputList) {

                    var keywordInputByUserExist;
                    // Just after a "." or a blank line
                    if (keywordInputList[idKeywordToCheck] === "") {
                        keywordInputByUserExist = true;// blank is a valid
                        // keyword
                        keywordInputList.pop();// remove blank caractere
                        potentiallyNeddApoint = false;
                    } else {
                        keywordInputByUserExist = checkIfTheKeywordIsCorrect(allKeyword, keywordInputList, idKeywordToCheck);
                    }
                    // if at least on keyword between the "%" by default
                    // autocompletion is diable
                    if (keywordInputByUserExist === false)
                        allKeywordCorrect = false;

                }

                var currentKeyword = keywordInputList[keywordInputList.length - 1];
                // All the keyword are correct set autocompletion
                if (allKeywordCorrect) {
                    var idNextKeyword = getNextKeywordId(currentKeyword, allKeyword, keywordInputList);
                    // add the special caractere
                    if (potentiallyNeddApoint && currentKeyword !== undefined && idNextKeyword !== -1) {
                        editor.session.insert(editor.getCursorPosition(), ".");
                    }
                    if (currentKeyword !== undefined && idNextKeyword === -1) {
                        editor.session.insert(editor.getCursorPosition(), "%");
                    }
                    // change the autocompletionList

                    if (currentKeyword === undefined) {
                        changeAceCompletionList(allKeyword[0]["listKeyword"], "", editor);
                        editor.execCommand("startAutocomplete");
                    }
                    if (idNextKeyword !== -1 && currentKeyword !== undefined) {
                        changeAceCompletionList(allKeyword[idNextKeyword]["listKeyword"], allKeyword[idNextKeyword]["motherKeyword"], editor);
                        editor.execCommand("startAutocomplete");
                    }
                }
                // The user tryed to add an new object set autocompletion for
                // this specifique part
                if (!allKeywordCorrect && keywordInputList[0] === "object" && keywordInputList.length < 4) {
                    var availableObjectProperties = [
                        "value",
//                        "picturepath",
                        "pictureurl"
                    ];
                    // if the user want to defined a new object
                    if (keywordInputList.length === 2 && potentiallyNeddApoint === false) {
                        changeAceCompletionList(availableObjectProperties, keywordInputList[1], editor);
                        editor.execCommand("startAutocomplete");
                    }
                    // add '%' when an availableObjectProperties was selected
                    if (keywordInputList.length === 3 && availableObjectProperties.indexOf(keywordInputList[2]) !== -1) {
                        editor.session.insert(editor.getCursorPosition(), "%");
                    }
                }
            }
        }
    });

}

/*
 * check if the keywordInputByUser and the keyword designated by the
 * idKeywordToCheck share the same motherKeyword (resolve issue with duplicate)
 */
function checkIfTheKeywordIsCorrect(allKeyword, keywordInputByUser, idKeywordToCheck) {

    for (var y in allKeyword) {
        for (var n in allKeyword[y]["listKeyword"]) {
            if (allKeyword[y]["listKeyword"][n] === keywordInputByUser[idKeywordToCheck]) {
                // check if the keyword matching posses the same mother keyword
                var listMotherKeywordPossible = getPossibleMotherKeyword(allKeyword[y]["listKeyword"][n], allKeyword);
                if (!(idKeywordToCheck >= 1 && listMotherKeywordPossible[0] !== null && getPossibleMotherKeyword(allKeyword[y]["listKeyword"][n], allKeyword).indexOf(keywordInputByUser[idKeywordToCheck - 1]) === -1)) {
                    return true;
                }
            }
        }
    }
    return false;
}

/*
 * Get the list of all the previous keyword possible for this keyword
 */
function getPossibleMotherKeyword(keyword, allKeyword) {
    var idmotherKeyword = [];
    for (var i in allKeyword) {
        for (var y in allKeyword[i]["listKeyword"]) {
            if (allKeyword[i]["listKeyword"][y] === keyword) {
                idmotherKeyword.push(allKeyword[i]["motherKeyword"]);
            }
        }
    }
    if (idmotherKeyword.length === 0)
        return -1;
    else
        return idmotherKeyword;
}

/*
 * Get the id of the next list of keyword by finding which one has keyword as a
 * motherKeyword
 */
function getNextKeywordId(keyword, allKeyword, keywordInputList) {
    // resolve issue with duplicate
    if (keywordInputList[0] !== "object" && keywordInputList.length === 2) {
        return -1;
    }
    // no duplicate
    else {
        var idCurrentKeyword = -1;
        for (i in allKeyword) {
            if (allKeyword[i]["motherKeyword"] === keyword) {
                idCurrentKeyword = i;
            }
        }
        return idCurrentKeyword;
    }
}

/*
 * Replace the autocompletion list of ace editor
 */
function changeAceCompletionList(keywordList, label, editor) {
    var langTools = ace.require("ace/ext/language_tools");
    langTools.setCompleters([]);// clear the autocompleter list
    var completer = {
        getCompletions: function (editor, session, pos, prefix, callback) {
            var completions = [];
            for (var i in keywordList) {
                completions.push({name: "default_name", value: keywordList[i], meta: label});
            }
            callback(null, completions);
        }
    };

    langTools.addCompleter(completer);
}

/*
 * Create a command to find and display (with annotation) the issue in ace
 */
function addCommandToDetectKeywordIssue(editor, allKeyword, commandName) {

    editor.commands.addCommand({
        name: commandName,
        exec: function () {
            var numberOfLine = editor.session.getLength();
            var annotationObjectList = [];
            // var warningKeywordList =[];
            for (var line = 0; line < numberOfLine; line++) {
                var editorValueAtTheLine = editor.session.getLine(line);
                var numberOfPercentCaractereAtLine = (editorValueAtTheLine.match(/\%/g) || []).length;
                if (numberOfPercentCaractereAtLine !== 0 && numberOfPercentCaractereAtLine % 2 === 0) {
                    var editorValueSplit = editorValueAtTheLine.split("%");
                    var cerberusVarAtLine = [];
                    for (var i = 0; i < editorValueSplit.length; i++) {
                        if (i % 2 === 1)
                            cerberusVarAtLine.push(editorValueSplit[i]);
                    }
                    // Check if each cerberus var is correct
                    for (var i in cerberusVarAtLine) {
                        var cerberusVarCurrentlyCheck = cerberusVarAtLine[i];
                        var keywordsListCurrentlyCheck = cerberusVarCurrentlyCheck.split(".");

                        var issueWithKeyword = "none";

                        if (keywordsListCurrentlyCheck.length >= 2) {
                            var startKeyword = keywordsListCurrentlyCheck[0];
                            var secondKeyword = keywordsListCurrentlyCheck[1];

                            if (startKeyword === "property" || startKeyword === "system" && keywordsListCurrentlyCheck.length === 2) {
                                if (getPossibleMotherKeyword(secondKeyword, allKeyword) === -1) {
                                    issueWithKeyword = "warning";
                                } else {
                                    if (getPossibleMotherKeyword(secondKeyword, allKeyword).indexOf(startKeyword) === -1)
                                        issueWithKeyword = "warning";
                                    // keyword exist but not correct
                                }
                            } else if (startKeyword === "object" && keywordsListCurrentlyCheck.length === 3) {

                                if (getPossibleMotherKeyword(secondKeyword, allKeyword) === -1) {
                                    issueWithKeyword = "warning";
                                } else {
                                    if (getPossibleMotherKeyword(secondKeyword, allKeyword).indexOf(startKeyword) === -1)
                                        issueWithKeyword = "warning";
                                    // keyword exist but not correct
                                }
                                var thirdKeyword = keywordsListCurrentlyCheck[2];
                                var availableObjectProperties = [
                                    "value",
//                                    "picturepath",
                                    "pictureurl"
                                ];
                                if (availableObjectProperties.indexOf(thirdKeyword) === -1) {
                                    issueWithKeyword = "error";
                                }
                            } else {
                                issueWithKeyword = "error";
                            }
                        } else {
                            issueWithKeyword = "error";
                        }
                        if (issueWithKeyword === "error") {
                            var messageOfAnnotion = "error invalid keyword";
                            annotationObjectList.push(createAceAnnotationObject(line, messageOfAnnotion, "error", null, null));
                        }
                        if (issueWithKeyword === "warning") {
                            var messageOfAnnotion = "warning the " + keywordsListCurrentlyCheck[0] + " : " + keywordsListCurrentlyCheck[1] + " don't exist";
                            annotationObjectList.push(createAceAnnotationObject(line, messageOfAnnotion, "warning", keywordsListCurrentlyCheck[0], keywordsListCurrentlyCheck[1]));
                        }
                    }
                }
            }
            setAceAnnotation(editor, annotationObjectList);
        }
    });

}

/*
 * object use to highlight line
 */
function createAceAnnotationObject(lineNumber, annotationText, annotationType, keywordTypeVar, keywordValueVar) {

    return {
        row: lineNumber,
        column: 0,
        text: annotationText,
        type: annotationType,
        lineNumber: lineNumber,
        keywordType: keywordTypeVar,
        keywordValue: keywordValueVar
    };
}

// set the list of ace annotion object as annotation
function setAceAnnotation(editor, annotationObjectList) {
    // Set annotation replace all the annotation so if you use it you need to
    // resend every annotation for each change
    editor.getSession().setAnnotations(annotationObjectList);
}

/*
 * Set a listenner for every left part of ace's lines in each line that will
 * resolve issue
 */
function createGuterCellListenner(editor) {

    var currentEditorGutter = editor.container.getElementsByClassName("ace_gutter")[0];
    var cellList = currentEditorGutter.getElementsByClassName("ace_gutter-cell");
    for (var i = 0; i < cellList.length; i++) {

        cellList[i].setAttribute("style", "cursor: pointer");
        cellList[i].onclick = function () {

            var lineClickedId = this.innerHTML - 1;// start at 1
            var annotationObjectList = editor.getSession().getAnnotations();

            for (var y = 0; y < annotationObjectList.length; y++) {
                if (annotationObjectList[y].lineNumber === lineClickedId && annotationObjectList[y].type === "warning") {

                    var keywordType = annotationObjectList[y].keywordType;
                    var keywordValue = annotationObjectList[y].keywordValue;
                    if (keywordType === "property") {
                        addPropertyWithAce(keywordValue);
                    }
                    if (keywordType === "object") {
                        addObjectWithAce(keywordValue);
                    }
                }
            }
            this.className = "ace_gutter-cell";// Remove the warning annotation
        };
    }
}

// Add keywordValue as a new property
function addPropertyWithAce(keywordValue) {

    var test = GetURLParameter("test");
    var testcase = GetURLParameter("testcase");
    var info = GetURLParameter("testcase");
    var property = GetURLParameter("property");

    $.ajax({
        url: "ReadTestCase",
        data: {test: test, testCase: testcase, withSteps: true},
        dataType: "json",
        success: function (data) {

            testCaseObject = data.contentTable[0];
            loadTestCaseInfo(testCaseObject);

            var myCountry = [];
            $.each(testCaseObject.countries, function (index) {
                myCountry.push(index);
            });
            // Store the current saveScript button status and disable it
            var saveScriptOldStatus = $("#saveScript").attr("disabled");
            $("#saveScript").attr("disabled", true);

            var newProperty = {
                property: keywordValue,
                description: "",
                countries: myCountry,
                type: "text",
                database: "",
                value1: "",
                value2: "",
                length: 0,
                rowLimit: 0,
                nature: "STATIC",
                retryNb: "",
                retryPeriod: "",
                toDelete: false,
                rank: 1
            };

            var prop = drawProperty(newProperty, testCaseObject, true, $("div[name='propertyLine']").length);
            setPlaceholderProperty(prop[0], prop[1]);

            // Restore the saveScript button status
            $("#saveScript").attr("disabled", typeof saveScriptOldStatus !== typeof undefined && saveScriptOldStatus !== false);
        }
    });
    getKeywordList("property").push(keywordValue);
}

// Add keywordValue as a new object
function addObjectWithAce(keywordValue) {

    var test = GetURLParameter("test");
    var testcase = GetURLParameter("testcase");
    var info = GetURLParameter("testcase");

    $.ajax({
        url: "ReadTestCase",
        data: {test: test, testCase: testcase, withSteps: true},
        dataType: "json",
        success: function (data) {
            // Store the current saveScript button status and disable it
            var saveScriptOldStatus = $("#saveScript").attr("disabled");
            $("#saveScript").attr("disabled", true);

            var applicationName = data.contentTable[0].application;
            addApplicationObjectModalClick(undefined, keywordValue, applicationName);

            // Restore the saveScript button status
            $("#saveScript").attr("disabled", typeof saveScriptOldStatus !== typeof undefined && saveScriptOldStatus !== false);
        }
    });
}

// Get the CURRENT list of keyword for each type
function getKeywordList(type) {
    if (getTags() !== undefined && getTags().length > 0) {
        var idType = -1;
        switch (type) {
            case "object":
                return getTags()[1].array;
            case "property":
                return getTags()[2].array;
            case "system":
                return getTags()[3].array;
                break;
            default:
                return null;
        }
    } else {
        return null;
    }
}

function tec_keyispressed(e) {
    var toto = "|.| |(|)|%|";
    var charval = "|" + e.key + "|";
    if (toto.indexOf(charval) !== -1) {
        showMessageMainPage("warning", "Character '" + e.key + "' is not allowed on subdata name. This is to avoid creating ambiguous syntax when using variabilization.", false, 4000);
        return false;
    }
    return true;
}

function displayPictureOfMinitature1(element) {
    showPicture("screenshot", $(element).attr('src'));
}