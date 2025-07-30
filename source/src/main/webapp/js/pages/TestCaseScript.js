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

/* global getSelectInvariant */

var canUpdate = false;
var allDelete = false;
var Tags = [];
var exeId = 0;
var lastExecutedQueueId = 0;
var lastExecutedTag = "";
var uniqid = 1000;

$.when($.getScript("js/global/global.js")
        , $.getScript("js/global/autocomplete.js")
        , $.getScript("js/testcase/action.js")
        , $.getScript("js/testcase/step.js")
        , $.getScript("js/testcase/testcaseStatic.js")
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
        // Load Application Combo (used on service modal).
        displayApplicationList("application", "", "", "");
        bindToggleCollapse();
        var test = GetURLParameter("test");
        var testcase = GetURLParameter("testcase");
        var property = GetURLParameter("property");
        var oneclickcreation = GetURLParameter("oneclickcreation");

        var tabactive = GetURLParameter("tabactive");
        var step = GetURLParameter("stepId");

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

                    $.ajax({
                        url: "ReadApplication",
                        data: {application: application},
                        async: true,
                        success: function (dataApp) {

                            var configPanel = $("#testCaseTitle");
                            configPanel.find("#AppLogo").attr("src", "./images/logoapp-" + dataApp.contentTable.type + ".png");
                            configPanel.find("#AppName").text("[" + application + "]");

                        }
                    });

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
                        'identifier': "element",
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
                                value3: "value",
                                length: "0",
                                rowLimit: 0,
                                cacheExpire: 0,
                                nature: "STATIC",
                                retryNb: "",
                                retryPeriod: "",
                                rank: 0,
                                toDelete: false
                            };

                            var prop = drawProperty(newProperty, testcaseObject, true, document.getElementsByClassName("property").length);

                            setPlaceholderProperty($(prop[0]), prop[1]);

                            $(prop[0]).find("#propName").focus();

                            setModif(true);

                            // Restore the saveScript button status
                            $("#saveScript").attr("disabled", typeof saveScriptOldStatus !== typeof undefined && saveScriptOldStatus !== false);
                        }

                    });

                    $('[data-toggle="tooltip"]').tooltip();

                    $('a[data-toggle="tab"]').on('shown.bs.tab', function (e) {
                        var target = $(e.target).attr("href")
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
            $("#saveScript").click(function () {
                saveScript();
            });
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
                        exeId = data.contentTable.id;
                        $("#seeLastExecUniq").parent().attr("href", "./TestCaseExecution.jsp?executionId=" + encodeURI(data.contentTable.id) + window.location.hash);
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
                        $("#rerunFromQueueandSee").attr("data-original-title", "Last Execution was <span class='status" + data.contentTable.controlStatus + "'>" + data.contentTable.controlStatus + "</span> in <b>" + data.contentTable.env + "</b> in <b>" + data.contentTable.country + "</b><br> on " + new Date(data.contentTable.end).toLocaleString());
                        $("#runTestCase").unbind('click');
                        $("#runTestCase").on('click', function () {
                            openModalExecutionSimple(application, test, testcase, description, data.contentTable.country, data.contentTable.env, data.contentTable.robot);
                        });

                        if (data.contentTable.queueId > 0) {
                            $("#rerunFromQueueandSee").off("click");
                            $("#rerunFromQueueandSee").click(function () {
                                triggerTestCaseExecutionQueueandSeeFromTC(data.contentTable.queueId, data.contentTable.tag);
                            });
                            lastExecutedQueueId = data.contentTable.queueId;
                            lastExecutedTag = data.contentTable.tag;
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

            $("#divPanelDefault").affix({offset: 120});
//            $("#nav-execution").affix({offset: 120});

            var wrap = $(window);

            wrap.on("scroll", function (e) {
                $(".affix").width($("#page-layout").width() - 3);
            });

            if (tabactive !== null) {
                $("a[name='" + tabactive + "']").click();
            }

            $("a[name='tabProperties']").on("shown.bs.tab", function (e) {
                e.target; // newly activated tab
                e.relatedTarget; // previous active tab
                InsertURLInHistory("./TestCaseScript.jsp?" + ReplaceURLParameters("tabactive", "tabProperties"));

            })
            $("a[name='tabSteps']").on("shown.bs.tab", function (e) {
                e.target; // newly activated tab
                e.relatedTarget; // previous active tab
                InsertURLInHistory("./TestCaseScript.jsp?" + ReplaceURLParameters("tabactive", "tabSteps"));
            })
            $("a[name='tabInheritedProperties']").on("shown.bs.tab", function (e) {
                e.target; // newly activated tab
                e.relatedTarget; // previous active tab
                InsertURLInHistory("./TestCaseScript.jsp?" + ReplaceURLParameters("tabactive", "tabInheritedProperties"));
            })

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
    result.description = object.description;
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
    $("#seeLastExecUniq").attr('data-toggle', 'tooltip').attr('data-original-title', doc.getDocDescription("page_testcasescript", "see_lastexecuniq")).html("<span class='glyphicon glyphicon-cog'></span> " + doc.getDocLabel('page_testcasescript', 'see_lastexecuniq'));
    $("#seeLastExec").attr('data-toggle', 'tooltip').attr('data-original-title', doc.getDocDescription("page_testcasescript", "see_lastexec")).html("<span class='glyphicon glyphicon-list'></span> " + doc.getDocLabel("page_testcasescript", "see_lastexec"));
    $("#seeTest").attr('data-toggle', 'tooltip').attr('data-original-title', doc.getDocDescription("page_testcasescript", "see_test")).html("<span class='glyphicon glyphicon-list'></span> " + doc.getDocLabel("page_testcasescript", "see_test"));
    $("#seeLogs").attr('data-toggle', 'tooltip').attr('data-original-title', doc.getDocDescription("page_testcasescript", "see_logs")).html("<span class='glyphicon glyphicon-list'></span> " + doc.getDocLabel("page_testcasescript", "see_logs"));
    $("#btnGroupDrop2").html(doc.getDocLabel("page_testcasescript", "run") + " <span class='caret'></span>");
    $("#runTestCase").attr('data-toggle', 'tooltip').attr('data-original-title', doc.getDocDescription("page_testcasescript", "run_testcase")).html("<span class='glyphicon glyphicon-play'></span> " + doc.getDocLabel("page_testcasescript", "run_testcase"));
    $("#rerunFromQueueandSee").attr('data-toggle', 'tooltip').attr('data-original-title', doc.getDocDescription("page_testcasescript", "rerunqueueandsee_testcase")).html("<span class='glyphicon glyphicon-forward'></span> " + doc.getDocLabel("page_testcasescript", "rerunqueueandsee_testcase"));
    $("#editTcInfo").attr('data-toggle', 'tooltip').attr('data-original-title', doc.getDocDescription("page_testcasescript", "edit_testcase")).html("<span class='glyphicon glyphicon-edit'></span> " + doc.getDocLabel("page_testcasescript", "edit_testcase"));
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

function triggerTestCaseExecutionQueueandSeeFromTC(queueId, tag) {
    $.ajax({
        url: "CreateTestCaseExecutionQueue",
        async: true,
        method: "POST",
        data: {
            id: queueId,
            actionState: "toQUEUED",
            actionSave: "save",
            tag: tag
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

function saveScript(queueid = 0, tag = "") {
    // If queueid != 0, rerun will be triggered

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
            $(properties[i]).data("property").rank = 0;
        }
        propArr.push($(properties[i]).data("property"));
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
                var url_step = "";
                if (!(isEmpty(stepData))) {
                    url_step = "&stepId=" + encodeURI(stepData.stepId);
                }
                var new_uri = parser.pathname + "?test=" + encodeURI(GetURLParameter("test")) + "&testcase=" + encodeURI(GetURLParameter("testcase")) + tutorialParameters + url_step + "&tabactive=" + tabActive;
                // If the 1st 2 characters are // we remove 1 of them.
                if ((new_uri[0] === '/') && (new_uri[1] === '/')) {
                    new_uri = new_uri[0] + new_uri.slice(2);
                }
                setModif(false);

                if (queueid !== 0) {
                    // ReRun the execution
                    triggerTestCaseExecutionQueueandSeeFromTC(queueid, tag);

                } else {
                    // Force reload of the page once save has been done
                    window.location.href = new_uri;

                }


            },
            error: function (jqXHR, textStatus, errorThrown) {
                showUnexpectedError(jqXHR, textStatus, errorThrown);
                $("#saveScript").attr("disabled", false);
            }
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

function appendActionsForConditionCombobox(combo, operator) {

    combo.empty();
    var steps = $("#steps li").data("item").steps;
    for (s in steps) {
        if (operator.startsWith("ifStepStatus")) {
            if (steps[s].sort != undefined) {
                combo.append($("<option></option>")
                        .text("Step " + steps[s].sort + " - " + steps[s].description)
                        .attr("stepId", steps[s].stepId)
                        .val(steps[s].stepId));
            }
        } else {
            var actions = $("#steps li").data("item").steps[s].actions;
            for (a in actions) {
                if (operator.startsWith("ifActionStatus")) {
                    if (actions[a].sort != undefined) {
                        combo.append($("<option></option>")
                                .text("Step " + steps[s].sort + " - Action " + actions[a].sort + " - " + actions[a].description)
                                .attr("actionId", actions[a].actionId)
                                .attr("stepId", steps[s].stepId)
                                .val(steps[s].stepId + "-" + actions[a].actionId));
                    }
                } else {
                    var controls = $("#steps li").data("item").steps[s].actions[a].controls;
                    for (c in controls) {
                        if (controls[c].sort != undefined) {
                            combo.append($("<option></option>")
                                    .text("Step " + steps[s].sort + " - Action " + actions[a].sort + " - Control " + controls[c].sort + " - " + controls[c].description)
                                    .attr("actionId", actions[a].actionId)
                                    .attr("stepId", steps[s].stepId)
                                    .attr("controlId", controls[c].controlId)
                                    .val(steps[s].stepId + "-" + actions[a].actionId + "-" + controls[c].controlId));
                        }
                    }
                }
            }
        }
    }
}

/** Modification Status * */

var getModif, setModif, initModification;
(function () {
    var isModif = false;
    var doc = new Doc();

    getModif = function () {
        return isModif;
    };
    setModif = function (val) {
//        console.info("isModif set to " + val)
        isModif = val;
        if (isModif) {
            if ($("#saveScript").hasClass("btn-default")) {
                $("#saveScript").removeClass("btn-default").addClass("btn-primary");
            }
            if ($("#rerunFromQueueandSee").hasClass("btn-default")) {
                $("#rerunFromQueueandSee").removeClass("btn-default").addClass("btn-primary");
                $("#rerunFromQueueandSee").html("<span class='glyphicon glyphicon-forward'></span> " + doc.getDocLabel("page_testcasescript", "savererunqueueandsee_testcase"));
                $("#rerunFromQueueandSee").off("click");
                $("#rerunFromQueueandSee").click(function () {
                    saveScript(lastExecutedQueueId, lastExecutedTag);
                });


            }
        } else {
            if ($("#saveScript").hasClass("btn-primary")) {
                $("#saveScript").removeClass("btn-primary").addClass("btn-default");
            }
            if ($("#rerunFromQueueandSee").hasClass("btn-primary")) {
                $("#rerunFromQueueandSee").removeClass("btn-primary").addClass("btn-default");
                $("#rerunFromQueueandSee").html("<span class='glyphicon glyphicon-forward'></span> " + doc.getDocLabel("page_testcasescript", "rerunqueueandsee_testcase"));
            }

        }

    };
    initModification = function () {
        $("#propertiesModal input, #propertiesModal select, #propertiesModal textarea, #propertiesModal pre").change(function () {
            console.info("chenge detected initModification");
            setModif(true);
        });
    };
})();



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



function formatActionSelect2Result(state) {
    if (typeof $($(state.element)[0]).attr("data-picto") !== 'undefined') {
        return $($($(state.element)[0]).attr("data-picto") + '<span> ' + state.text + '</span>');
    } else {
        return $('<span>' + state.text + '</span>');
    }
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
        if (conditionValue1 !== undefined)
            title += "<div>'val1' : '" + conditionValue1.replaceAll("'", '').replaceAll('"', '') + "'</div>";
        if (conditionValue2 !== undefined)
            title += "<div>'val2' : '" + conditionValue2.replaceAll("'", '').replaceAll('"', '') + "'</div>";
        if (conditionValue3 !== undefined)
            title += "<div>'val3' : '" + conditionValue3.replaceAll("'", '').replaceAll('"', '') + "'</div>";
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
            configs.identifier = "element";
            initAutocompleteWithTags($(this), configs, contextInfo);
        }

        function initAutocompleteElementSwitch(e) {
//            console.log("start feed autocomplete on focus (element).");
            configs.identifier = "switch";
            initAutocompleteWithTags($(this), configs, contextInfo);
        }

        function initAutocompleteElementSelect(e) {
//            console.log("start feed autocomplete on focus (element).");
            configs.identifier = "select";
            initAutocompleteWithTags($(this), configs, contextInfo);
        }

        function initAutocompleteElementBoolean(e) {
//            console.log("start feed autocomplete on focus (element).");
            configs.identifier = "boolean";
            initAutocompleteWithTags($(this), configs, contextInfo);
        }

        function initAutocompleteElementFileUploadFlag(e) {
//            console.log("start feed autocomplete on focus (element).");
            configs.identifier = "fileuploadflag";
            initAutocompleteWithTags($(this), configs, contextInfo);
        }

        function initAutocompleteElementFileSortFlag(e) {
//            console.log("start feed autocomplete on focus (element).");
            configs.identifier = "filesortflag";
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


        function initAutocompleteVariablesOnly(e) {
//            console.log("start feed autocomplete on focus (variable only).");
            configs.identifier = "none";
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
//            console.log("trigger settingsButton.");

            $(this).trigger("settingsButton");
        }

        function modifyAutocomplete(e) {
//            console.log("modify feed autocomplete on input (generic).")
//            console.log("trigger settingsButton.");
            $(this).trigger("settingsButton");
        }


        // Adding Autocomplete on all fields. ##### crb-autocomplete-variable (include Variables ONLY) #####
        $(document).on('focus', "div.crb-autocomplete-variable input:not([readonly])", initAutocompleteVariablesOnly);
        $(document).on('input', "div.crb-autocomplete-variable input:not([readonly])", modifyAutocomplete);
        $(document).on('focus', "input.crb-autocomplete-variable:not([readonly])", initAutocompleteVariablesOnly);
        $(document).on('input', "input.crb-autocomplete-variable:not([readonly])", modifyAutocomplete);

        // Adding Autocomplete on all fields. ##### crb-autocomplete-service (include Services+Variables) #####
        $(document).on('focus', "div.crb-autocomplete-service input:not([readonly])", initAutocompleteService);
        $(document).on('input', "div.crb-autocomplete-service input:not([readonly])", modifyAutocompleteService);
        $(document).on('focus', "input.crb-autocomplete-service:not([readonly])", initAutocompleteService);
        $(document).on('input', "input.crb-autocomplete-service:not([readonly])", modifyAutocompleteService);

        // Adding Autocomplete on all fields. ##### crb-autocomplete-property (include Properties+Variables) #####
        $(document).on('focus', "div.crb-autocomplete-property input:not([readonly])", initAutocompleteProperty);
        $(document).on('input', "div.crb-autocomplete-property input:not([readonly])", modifyAutocompleteProperty);
        $(document).on('focus', "input.crb-autocomplete-property:not([readonly])", initAutocompleteProperty);
        $(document).on('input', "input.crb-autocomplete-property:not([readonly])", modifyAutocompleteProperty);

        // Adding Autocomplete on all fields. ##### crb-autocomplete-element (include Elements+Variables) #####
        $(document).on('focus', "div.crb-autocomplete-element input:not([readonly])", initAutocompleteElement);
        $(document).on('input', "div.crb-autocomplete-element input:not([readonly])", modifyAutocomplete);
        $(document).on('focus', "input.crb-autocomplete-element:not([readonly])", initAutocompleteElement);
        $(document).on('input', "input.crb-autocomplete-element:not([readonly])", modifyAutocomplete);

        // Adding Autocomplete on all fields. ##### crb-autocomplete-switch (include Switch Elements+Variables) #####
        $(document).on('focus', "div.crb-autocomplete-switch input:not([readonly])", initAutocompleteElementSwitch);
        $(document).on('input', "div.crb-autocomplete-switch input:not([readonly])", modifyAutocomplete);
        $(document).on('focus', "input.crb-autocomplete-switch:not([readonly])", initAutocompleteElementSwitch);
        $(document).on('input', "input.crb-autocomplete-switch:not([readonly])", modifyAutocomplete);

        // Adding Autocomplete on all fields. ##### crb-autocomplete-select (include Select Option Elements+Variables) #####
        $(document).on('focus', "div.crb-autocomplete-select input:not([readonly])", initAutocompleteElementSelect);
        $(document).on('input', "div.crb-autocomplete-select input:not([readonly])", modifyAutocomplete);
        $(document).on('focus', "input.crb-autocomplete-select:not([readonly])", initAutocompleteElementSelect);
        $(document).on('input', "input.crb-autocomplete-select:not([readonly])", modifyAutocomplete);

        // Adding Autocomplete on all fields. ##### crb-autocomplete-boolean (include Boolean+Variables) #####
        $(document).on('focus', "div.crb-autocomplete-boolean input:not([readonly])", initAutocompleteElementBoolean);
        $(document).on('input', "div.crb-autocomplete-boolean input:not([readonly])", modifyAutocomplete);
        $(document).on('focus', "input.crb-autocomplete-boolean:not([readonly])", initAutocompleteElementBoolean);
        $(document).on('input', "input.crb-autocomplete-boolean:not([readonly])", modifyAutocomplete);

        // Adding Autocomplete on all fields. ##### crb-autocomplete-fileuploadflag (include File Flags+Variables) #####
        $(document).on('focus', "div.crb-autocomplete-fileuploadflag input:not([readonly])", initAutocompleteElementFileUploadFlag);
        $(document).on('input', "div.crb-autocomplete-fileuploadflag input:not([readonly])", modifyAutocomplete);
        $(document).on('focus', "input.crb-autocomplete-fileuploadflag:not([readonly])", initAutocompleteElementFileUploadFlag);
        $(document).on('input', "input.crb-autocomplete-fileuploadflag:not([readonly])", modifyAutocomplete);

        // Adding Autocomplete on all fields. ##### crb-autocomplete-filesortflag (include File Flags+Variables) #####
        $(document).on('focus', "div.crb-autocomplete-filesortflag input:not([readonly])", initAutocompleteElementFileSortFlag);
        $(document).on('input', "div.crb-autocomplete-filesortflag input:not([readonly])", modifyAutocomplete);
        $(document).on('focus', "input.crb-autocomplete-filesortflag:not([readonly])", initAutocompleteElementFileSortFlag);
        $(document).on('input', "input.crb-autocomplete-filesortflag:not([readonly])", modifyAutocomplete);

        // Adding Contextual buttons when 'settingsButton' event is triggered.
        $(document).on('settingsButton', "div.crb-contextual-button input", function (e) {
//            console.log("start generate buttons.");
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
                                    var addEntry = $('<span class="input-group-btn many ' + name + '"><button id="editEntry" onclick="openModalApplicationObject(\'' + tcInfo.application + '\', \'' + name + '\',\'ADD\' , \'testCaseScript\');"\n\
	                                		class="buttonObject btn btn-default input-sm " \n\
	                                		title="' + name + '" type="button">\n\
	                                <span class="glyphicon glyphicon-plus"></span></button></span>');
                                    objectNotExist = true;
                                    nameNotExist = name;
                                    typeNotExist = "applicationObject";
                                    $(htmlElement).attr("style", "width:80%").parent().append(addEntry);
                                } else if (objectIntoTagToUseExist(TagsToUse[1], name)) {
                                    var editEntry = '<span class="input-group-btn many ' + name + '"><button id="editEntry" onclick="openModalApplicationObject(\'' + tcInfo.application + '\', \'' + name + '\',\'EDIT\' , \'testCaseScript\');"\n\
	                                class="buttonObject btn btn-default input-sm " \n\
	                                title="' + name + '" type="button">\n\
	                                <span class="glyphicon glyphicon-pencil"></span></button></span>';
                                    $(htmlElement).attr("style", "width:80%").parent().append(editEntry);
                                }
                            } else if (betweenPercent[i].startsWith("%datalib.") && findname !== null && findname.length > 0) {
                                name = findname[0];
                                name = name.slice(1, name.length - 1);
                                if (!objectIntoTagToUseExist(TagsToUse[3], name)) {
                                    var addEntry = $('<span class="input-group-btn many ' + name + '"><button id="editEntry" onclick="openModalDataLib(null, \'' + name + '\', \'ADD\'  ,\'TestCaseScript_Steps\', null);"\n\
	                                		class="buttonObject btn btn-default input-sm " \n\
	                                		title="' + name + '" type="button">\n\
	                                <span class="glyphicon glyphicon-plus"></span></button></span>');
                                    objectNotExist = true;
                                    nameNotExist = name;
                                    typeNotExist = "applicationObject";
                                    $(htmlElement).attr("style", "width:80%").parent().append(addEntry);
                                } else if (objectIntoTagToUseExist(TagsToUse[3], name)) {
                                    var editEntry = '<span class="input-group-btn many ' + name + '"><button id="editEntry" onclick="openModalDataLib(null, \'' + name + '\', \'EDIT\'  ,\'TestCaseScript_Steps\', null);"\n\
	                                class="buttonObject btn btn-default input-sm " \n\
	                                title="' + name + '" type="button">\n\
	                                <span class="glyphicon glyphicon-pencil"></span></button></span>';
                                    $(htmlElement).attr("style", "width:80%").parent().append(editEntry);
                                }
                            } else if (betweenPercent[i].startsWith("%property.") && findname !== null && findname.length > 0) {
                                let data = loadGuiProperties();
                                name = findname[0];
                                name = name.slice(1, name.length - 1);
                                if (objectIntoTagToUseExist(TagsToUse[4], name)) {
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
        "APP_DOMAIN", "APP_HOST", "APP_CONTEXTROOT", "EXEURL", "APP_VAR1", "APP_VAR2", "APP_VAR3", "APP_VAR4", "APP_SECRET1", "APP_SECRET2",
        "ENV", "ENVGP",
        "COUNTRY", "COUNTRYGP1", "COUNTRYGP2", "COUNTRYGP3", "COUNTRYGP4", "COUNTRYGP5", "COUNTRYGP6", "COUNTRYGP7", "COUNTRYGP8", "COUNTRYGP9",
        "TEST",
        "TESTCASE", "TESTCASEDESCRIPTION",
        "SSIP", "SSPORT",
        "TAG",
        "EXECUTIONID",
        "EXESTART", "EXEELAPSEDMS",
        "EXESTORAGEURL",
        "STEP.n.n.RETURNCODE", "CURRENTSTEP_INDEX", "CURRENTSTEP_STARTISO", "CURRENTSTEP_ELAPSEDMS", "CURRENTSTEP_SORT",
        "LASTSERVICE_HTTPCODE", "LASTSERVICE_CALL", "LASTSERVICE_RESPONSE", "LASTSERVICE_RESPONSETIME",
        "TODAY-yyyy", "TODAY-MM", "TODAY-dd", "TODAY-D", "TODAY-HH", "TODAY-mm", "TODAY-ss",
        "YESTERDAY-yyyy", "YESTERDAY-MM", "YESTERDAY-dd", "YESTERDAY-D", "YESTERDAY-HH", "YESTERDAY-mm", "YESTERDAY-ss",
        "TOMORROW-yyyy", "TOMORROW-MM", "TOMORROW-dd", "TOMORROW-D"
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