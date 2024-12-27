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
$.when($.getScript("js/global/global.js")).then(function () {
    $(document).ready(function () {
        var doc = new Doc();

        displayHeaderLabel(doc);
        displayFooter(doc);
        bindToggleCollapseCustom();
        displayPageLabel(doc);

        appendCampaignList();
        $("#campaignSelect").select2();

        var country = GetURLParameter("country");
        appendCountryList(country);

        var system = getUser().defaultSystem;
        var test = decodeURIComponent(GetURLParameter("test"));
        var testcase = decodeURIComponent(GetURLParameter("testcase"));
        var environment = GetURLParameter("environment");
        var country = GetURLParameter("country");
        var tag = decodeURIComponent(GetURLParameter("tag"));
        var browser = GetURLParameter("browser");
        //check if Extended Test Case Filters is collapse and load the data if it is not
        var filterPanelDataLoaded = false;
        var filterPanel = document.getElementById("filtersPanel");

        if (filterPanel.className === "panel-body collapse defaultNotExpanded in") {
            loadTestCaseFilterData(system);
            filterPanelDataLoaded = true;
            updateUserPreferences();
        }
        //add a listenner to load the data when needed
        $("#FilterPanelHeader").click(function () {
            if (!filterPanelDataLoaded) {
                loadTestCaseFilterData(system);
                filterPanelDataLoaded = true;
                updateUserPreferences();
            }
        });

        $("#robotSettings #robot").change(function () {
            loadRobotInfo($(this).val());
        });

        $("#SelectionManual").on("click", function () {
            selectionManual();
        });
        $("#SelectionCampaign").on("click", function () {
            selectionCampaign();
        });

        var myCampaign = GetURLParameters("campaign");
        if (!isEmpty(myCampaign)) {
            var $option = $('<option></option>').text(myCampaign).val(myCampaign);
            $("#campaignSelect").append($option).trigger('change'); // append the option and update Select2
            $.when(
                    selectionCampaign(),
                    loadExecForm(tag),
                    loadRobotForm(browser),
                    loadHardDefinedSingleSelect("length", [{label: '50', value: 50}, {label: '100', value: 100}, {label: '>100', value: -1}], 0)
                    ).then(function () {
                loadCampaign();
            });

        } else {
            //load the data that need to be display in any case
            loadTestCaseEssentialData(test, testcase, environment, country, tag, browser, true);
        }

        // Run Test Case button click
        $("#runTestCase").on("click", function () {
            runTestCase(false);
        });
        // Run Test Case button click
        $("#runTestCaseAndSee").on("click", function () {
            runTestCase(true);
        });

        $("#loadFiltersBtn").click(function () {
            loadTestCaseFromFilter(null, null);
        });

        $("#loadCampaignBtn").click(function () {
            loadCampaign();
        });

        $("#testcaseSelectAll").click(function () {
            $("#testCaseList option").prop("selected", true);
        });

        $("#testcaseSelectNone").click(function () {
            $("#testCaseList option").prop("selected", false);
        });

        $('[name="envSettings"]').on("change", function () {
            if (this.value === "auto") {
                $("#envSettingsMan").hide();
                $("#envSettingsAuto").show();
            } else if (this.value === "manual") {
                $("#envSettingsAuto").hide();
                $("#envSettingsMan").show();
            }
        });

        $("#saveRobotPreferences").click(saveRobotPreferences);

        $("#saveExecutionParams").click(saveExecutionPreferences);

        $("#robotCreate").click(function () {
            openModalRobot_FromRunTest("", "ADD");
        });


        $("#countrySelectAll").on("click", function () {
            $("#countryList input").prop('checked', true);
        });
        $("#countrySelectNone").on("click", function () {
            $("#countryList input").prop('checked', false);
        });

        //open Run navbar Menu
        openNavbarMenu("navMenuRun");

        $('[data-toggle="popover"]').popover({
            'placement': 'auto',
            'container': 'body'}
        );
    });
});


function loadCampaign() {
    var campaign = $("#campaignSelect").val();
    loadCampaignContent(campaign);
    loadCampaignParameter(campaign);
}

function loadRequestContext() {
    var test = GetURLParameter("test");
    var testcase = GetURLParameter("testcase");
    $('#testCaseList').find('option[value="' + test + '-' + testcase + '"]').prop("selected", true);
}

function displayPageLabel() {
    var doc = new Doc();
    $("#pageTitle").html(doc.getDocLabel("page_runtest", "title"));
    $("#title").html(doc.getDocOnline("page_runtest", "title"));
    $("h1.page-title-line").text(doc.getDocLabel("page_runtest", "title"));
    $("#selectionPanel div.panel-heading").text(doc.getDocLabel("page_runtest", "selection_type"));
    $("#selectionPanel input[value='filters']").next().text(doc.getDocLabel("page_runtest", "select_list_test"));
    $("#selectionPanel input[value='campaign']").next().text(doc.getDocLabel("page_runtest", "select_campaign"));
    $("#loadCampaignBtn").text(doc.getDocLabel("page_runtest", "load"));
    $("#ChooseTestHeader").text(doc.getDocLabel("page_runtest", "ChooseTest"));
    $("#FilterHeader").text(doc.getDocLabel("page_runtest", "filters"));
    $("#lbl_test").text(doc.getDocLabel("page_runtest", "test"));
//    $("#lbl_project").text(doc.getDocLabel("page_runtest", "project"));
    $("#lbl_application").text(doc.getDocLabel("page_runtest", "application"));
    $("#lbl_creator").text(doc.getDocLabel("page_runtest", "creator"));
    $("#lbl_implementer").text(doc.getDocLabel("page_runtest", "implementer"));
    $("#lbl_type").text(doc.getDocLabel("invariant", "TESTCASE_TYPE"));
    $("#lbl_campaign").text(doc.getDocLabel("page_runtest", "campaign"));
    $("#lbl_priority").text(doc.getDocLabel("page_runtest", "priority"));
    $("#lbl_status").text(doc.getDocLabel("page_runtest", "status"));
    $("#lbl_targetMinor").text(doc.getDocLabel("page_runtest", "TargetMinor"));
    $("#lbl_targetMajor").text(doc.getDocLabel("page_runtest", "TargetMajor"));
    $("#lbl_size").text(doc.getDocLabel("page_runtest", "size"));
    $("input[name='envSettings'][value='auto']").next().text(doc.getDocLabel("page_runtest", "automatic"));
    $("input[name='envSettings'][value='manual']").next().text(doc.getDocLabel("page_runtest", "manual"));
    $("label[for='myhost']").text(doc.getDocLabel("page_runtest", "myhost"));
    $("label[for='mycontextroot']").text(doc.getDocLabel("page_runtest", "mycontextroot"));
    $("label[for='myloginrelativeurl']").text(doc.getDocLabel("page_runtest", "myloginrelativeurl"));
    $("label[for='myenvdata']").text(doc.getDocLabel("page_runtest", "myenvdata"));
    $("#testcaseListLabel").text(doc.getDocLabel("page_runtest", "testcaseList"));
    $("#countryListLabel").text(doc.getDocLabel("page_runtest", "countryList"));
    $("#envListLabel").text(doc.getDocLabel("page_runtest", "envList"));
    $("#rbtLabel").text(doc.getDocLabel("page_runtest", "robot_settings"));
    $("#RobotPanel label[for='robot']").text(doc.getDocLabel("page_runtest", "select_robot"));
    $("#RobotPanel label[for='seleniumIP']").text(doc.getDocLabel("page_runtest", "selenium_ip"));
    $("#RobotPanel label[for='seleniumPort']").text(doc.getDocLabel("page_runtest", "selenium_port"));
    $("#RobotPanel label[for='browser']").text(doc.getDocLabel("page_runtest", "browser"));
    $("#saveRobotPreferences").text(doc.getDocLabel("page_runtest", "saverobotpref"));
    $("#exeLabel").text(doc.getDocLabel("page_runtest", "execution_settings"));
    $("#executionPanel label[for='tag']").html(doc.getDocOnline("page_runtest", "tag") + "&nbsp;<span class='toggle glyphicon glyphicon-tag pull-right'></span>");
    $("#executionPanel label[for='verbose']").text(doc.getDocOnline("page_runtest", "verbose"));
    $("#executionPanel label[for='screenshot']").html(doc.getDocOnline("page_runtest", "screenshot") + "&nbsp;<span class='toggle glyphicon glyphicon-picture pull-right'></span>");
    $("#executionPanel label[for='video']").html(doc.getDocOnline("page_runtest", "video") + "&nbsp;<span class='toggle glyphicon glyphicon-film pull-right'></span>");
    $("#executionPanel label[for='pageSource']").html(doc.getDocOnline("page_runtest", "pagesource") + "&nbsp;<span class='toggle glyphicon glyphicon-list pull-right'></span>");
    $("#executionPanel label[for='seleniumLog']").html(doc.getDocOnline("page_runtest", "seleniumlog") + "&nbsp;<span class='toggle glyphicon glyphicon-list pull-right'></span>");
    $("#executionPanel label[for='consoleLog']").html(doc.getDocOnline("page_runtest", "consolelog") + "&nbsp;<span class='toggle glyphicon glyphicon-console pull-right'></span>");
    $("#executionPanel label[for='timeout']").html(doc.getDocOnline("page_runtest", "timeout") + "&nbsp;<span class='toggle glyphicon glyphicon-time pull-right'></span>");
    $("#executionPanel label[for='retries']").html(doc.getDocOnline("page_runtest", "retries") + "&nbsp;<span class='toggle glyphicon glyphicon-repeat pull-right'></span>");
    $("#executionPanel label[for='manualExecution']").text(doc.getDocOnline("page_runtest", "manual_execution"));
    $("#executionPanel label[for='priority']").text(doc.getDocOnline("page_runtest", "priority"));
    $("#saveExecutionParams").text(doc.getDocLabel("page_runtest", "save_execution_params"));
}

function selectionCampaign() {
    var alreadyLoaded = $('#selectionPanel').data("LoadedMode");

    if (alreadyLoaded !== "campaign") {

        $("#SelectionManual").removeClass("btn-success");
        $("#SelectionManual").addClass("btn-default");
        $("#SelectionCampaign").removeClass("btn-default");
        $("#SelectionCampaign").addClass("btn-success");

        $("#testcaseSelectAll").prop("disabled", true);
        $("#testcaseSelectNone").prop("disabled", true);

        $('#runTestCase').text("Run Campaign");
        $('#runTestCaseAndSee').text("Run Campaign (and See Result)");

        $("#filtersPanelContainer").hide();
        $("#campaignSelection").show();
        $("#testCaseList").empty();
        $("#envSettingsAuto select").empty();
        displayUniqueEnvList("environment", "");

        $('#selectionPanel').data("LoadedMode", "campaign");

        loadCampaign();

    }


}

function selectionManual(test, testcase, environment, country) {
    var alreadyLoaded = $('#selectionPanel').data("LoadedMode");
    if (alreadyLoaded !== "manual") {

        $("#SelectionManual").removeClass("btn-default");
        $("#SelectionManual").addClass("btn-success");
        $("#SelectionCampaign").removeClass("btn-success");
        $("#SelectionCampaign").addClass("btn-default");

        $("#envSettingsAuto select").prop("disabled", false).val("");
        var mysize = $("#countryList input.countrycb").length;
        $("#countryList input.countrycb").each(function () {
            if (($(this).attr("name") === country) || (mysize <= 1)) { // We select the country if it is the one from the URL or if there is only 1 country.
                $(this).prop("disabled", false).prop("checked", true);
            } else {
                $(this).prop("disabled", false).prop("checked", false);
            }
        });

        $("#testCaseList").prop("disabled", false);
        $("input[name='envSettings']").prop("disabled", false);
        $("#envSettingsAuto select").empty();
        if (environment !== undefined && environment !== null) {
            $("[name='environment']").append($('<option></option>').text(environment).val(environment));
            $("[name='environment']").val(environment);
        } else {
            displayUniqueEnvList("environment", getUser().defaultSystemsQuery, environment);
        }

        $("#testcaseSelectAll").prop("disabled", false);
        $("#testcaseSelectNone").prop("disabled", false);

        $("#campaignSelection").hide();
        $("#filters").show();
        $("#filtersPanelContainer").show();

        $('#runTestCase').text("Run TestCase");
        $('#runTestCaseAndSee').text("Run TestCase (and See Result)");

        loadTestCaseFromFilter(test, testcase);

        $('#selectionPanel').data("LoadedMode", "manual");

    }

}

function loadTestCaseFromFilter(defTest, defTestcase) {

    showLoader("#chooseTest");
    var testURL = "";
    var testCaseURL = "";
    
    if ((defTest !== "null") && (defTest !== null) && (defTest !== undefined)) { // If test is defined, we limit the testcase list on that test.
        testURL = "&test=" + encodeURIComponent(defTest);
    }
    if ((defTestcase !== "null") && (defTestcase !== null) && (defTestcase !== undefined)) { // If test is defined, we limit the testcase list on that test.
        testCaseURL = "&testCase=" + encodeURIComponent(defTestcase);
    }
    // Get the requested result size value
    var lengthURL = '&length=' + $("#lengthFilter").find(':selected').val();
    var serialize = "";
    if ($("#filters").serialize() != "") {
        serialize = "&" + $("#filters").serialize();
    }
    $.ajax({
        url: "ReadTestCase",
        method: "GET",
        data: "filter=true" + getUser().defaultSystemsQuery + serialize + testURL + testCaseURL + lengthURL,
        datatype: "json",
        async: true,
        success: function (data) {

            var testCaseList = $("#testCaseList");
            testCaseList.empty();

            if (data.contentTable === undefined) {
                showMessageMainPage("danger", "Test Case : " + defTest + " - " + defTestcase + " does not exist !", true);
            } else {
                if (data.contentTable.length > 1) {
                    for (var i = 0; i < data.contentTable.length; i++) {

                        var text = data.contentTable[i].test + " - " + data.contentTable[i].testcase + " [" + data.contentTable[i].application + "]: " + data.contentTable[i].description;

                        testCaseList.append($("<option></option>")
                                .text(text)
                                .val(data.contentTable[i].test + "-" + data.contentTable[i].testcase)
                                .data("item", data.contentTable[i]));
                    }
                } else {
                    if (data.contentTable.length <= 0) {
                        showMessageMainPage("danger", "No test case available to run ! Please select another system or create some.", true);

                    } else {
                        var text = data.contentTable[0].test + " - " + data.contentTable[0].testcase + " [" + data.contentTable[0].application + "]: " + data.contentTable[0].description;

                        testCaseList.append($("<option></option>")
                                .text(text)
                                .val(data.contentTable[0].test + "-" + data.contentTable[0].testcase)
                                .data("item", data.contentTable[0]));

                    }

                }
            }
            hideLoader("#chooseTest");
            if ((defTest !== null) && (defTest !== undefined)) { // if test is defined we select the value in the select list.
                $('#testCaseList').find('option[value="' + defTest + '-' + defTestcase + '"]').prop("selected", true);
            }
        }
    });
}

function appendCountryList(defCountry) {

    var jqxhr = $.getJSON("ReadCountryEnvParam", "uniqueCountry=true"+getUser().defaultSystemsQuery);
    $.when(jqxhr).then(function (data) {
        var countryList = $("[name=countryList]");

        for (var index = 0; index < data.contentTable.length; index++) {
            var country = data.contentTable[index].country;
            if ((country === defCountry) || (data.contentTable.length <= 1)) // We select the the country if it is the one from the URL or if there is only 1 country.
            {
                myChecked = 'checked="" ';

            } else {
                myChecked = "";
            }
            countryList.append('<label class="checkbox-inline">\n\
                                <input class="countrycb" type="checkbox" ' + myChecked + ' name="' + country + '"/>' + country + '\
                                </label>');
        }
    });
}

// UTILITY FUNCTIONS FOR CAMPAIGN LAUNCHING

function loadCampaignContent(campaign) {
    clearResponseMessageMainPage();
    if (campaign !== "") {
        showLoader("#chooseTest");
        $.ajax({
            url: "ReadTestCase",
            method: "GET",
            data: {campaign: campaign},
            datatype: "json",
            async: true,
            success: function (data) {

                var testCaseList = $("#testCaseList");

                testCaseList.empty().prop("disabled", "disabled");

                for (var index = 0; index < data.contentTable.length; index++) {
                    var text = data.contentTable[index].test + " - " + data.contentTable[index].testcase + " [" + data.contentTable[index].application + "]: " + data.contentTable[index].description;

                    testCaseList.append($("<option></option>")
                            .text(text)
                            .val(data.contentTable[index].testcase)
                            .prop("selected", true)
                            .data("item", data.contentTable[index]));
                }

                showMessage(data, $('#page-layout'), true);
                hideLoader("#chooseTest");
            },
            error: showUnexpectedError
        });
    }
}

function loadCampaignParameter(campaign) {
    if (!isEmpty(campaign)) {

        $.ajax({
            url: "ReadCampaignParameter",
            method: "GET",
            data: {campaign: campaign},
            datatype: "json",
            async: true,
            success: function (data) {
                var robot = [];
                var env = [];
                var countries = [];

                for (var index = 0; index < data.contentTable.length; index++) {
                    var type = data.contentTable[index].parameter;
                    var value = data.contentTable[index].value;
                    if (type === "ROBOT") {
                        robot.push(value);
                    } else if (type === "ENVIRONMENT") {
                        env.push(value);
                    } else if (type === "COUNTRY") {
                        countries.push(value);
                    }
                }

                // Environments
                $("#envSettingsAuto select").val(env);
                $("input[name='envSettings'][value='auto']").click();

                // Country
                $("#countryList input.countrycb").each(function () {
                    var country = $(this).prop("name");

                    if (countries.indexOf(country) !== -1) {
                        $(this).prop("checked", true);
                    } else {
                        $(this).prop("checked", false);
                    }
                });

                // Robot
                $("#robot option").each(function () {
                    var selected = false;
                    for (var index = 0; index < robot.length; index++) {

                        if (robot[index] === $(this).val()) {
                            $("#robot").find('option[value="' + $(this).val() + '"]').prop("selected", true);
                            selected = true;
                        }
                        if (!selected) {
                            $("#robot").find('option[value="' + $(this).val() + '"]').prop("selected", false);

                        }
                    }


                });
                loadRobotInfo(robot);
            }
        });

        $.ajax({
            url: "ReadCampaign?campaign=" + campaign,
            method: "GET",
            data: {campaign: campaign},
            datatype: "json",
            async: true,
            success: function (data) {
                if (data.contentTable !== null) {
                    if (data.contentTable.Screenshot !== null && data.contentTable.Screenshot !== "") {
                        $('#screenshot option[value="' + data.contentTable.Screenshot + '"]').prop('selected', true);
                    }

                    if (data.contentTable.Video !== null && data.contentTable.Video !== "") {
                        $('#video option[value="' + data.contentTable.Video + '"]').prop('selected', true);
                    }

                    if (data.contentTable.Verbose !== null && data.contentTable.Verbose !== "") {
                        $('#verbose option[value="' + data.contentTable.Verbose + '"]').prop('selected', true);
                    }

                    if (data.contentTable.Tag !== null && data.contentTable.Tag !== "") {
                        $("#tag").val(data.contentTable.Tag);
                    }

                    if (data.contentTable.Priority !== null && data.contentTable.Priority !== "") {
                        $("#priority").val(data.contentTable.Priority);
                    }

                    if (data.contentTable.PageSource !== null && data.contentTable.PageSource !== "") {
                        $('#pageSource option[value="' + data.contentTable.PageSource + '"]').prop('selected', true);
                    }

                    if (data.contentTable.RobotLog !== null && data.contentTable.RobotLog !== "") {
                        $('#seleniumLog option[value="' + data.contentTable.RobotLog + '"]').prop('selected', true);
                    }

                    if (data.contentTable.ConsoleLog !== null && data.contentTable.ConsoleLog !== "") {
                        $('#consoleLog option[value="' + data.contentTable.ConsoleLog + '"]').prop('selected', true);
                    }

                    if (data.contentTable.Timeout !== null && data.contentTable.Timeout !== "") {
                        $("#timeout").val(data.contentTable.Timeout);
                    }

                    if (data.contentTable.Retries !== null && data.contentTable.Retries !== "") {
                        $('#retries option[value="' + data.contentTable.Retries + '"]').prop('selected', true);
                    }

                    if (data.contentTable.ManualExecution !== null && data.contentTable.ManualExecution !== "") {
                        $('#manualExecution option[value="' + data.contentTable.ManualExecution + '"]').prop('selected', true);
                    }
                }
            }

        });
    }
}

/** FORM SENDING UTILITY FUNCTIONS (VALID FOR SERVLET ADDTOEXECUTIONQUEUE) **/

function runTestCase(doRedirect) {

    var doc = new Doc();

    var fromCampaign = false;
    if ($('#selectionPanel').data("LoadedMode") === "campaign") {
        var fromCampaign = true;
    }

    clearResponseMessageMainPage();

    var paramSerialized = "e=1";
    if (fromCampaign) {
        paramSerialized += "&campaign=" + $("#campaignSelect").val();
    }
    paramSerialized += "&tag=" + encodeURIComponent($("#executionSettingsForm #tag").val());
    paramSerialized += "&screenshot=" + $("#executionSettingsForm #screenshot").val();
    paramSerialized += "&video=" + $("#executionSettingsForm #video").val();
    paramSerialized += "&verbose=" + $("#executionSettingsForm #verbose").val();
    paramSerialized += "&timeout=" + $("#executionSettingsForm #timeout").val();
    paramSerialized += "&pagesource=" + $("#executionSettingsForm #pageSource").val();
    paramSerialized += "&seleniumlog=" + $("#executionSettingsForm #seleniumLog").val();
    paramSerialized += "&consolelog=" + $("#executionSettingsForm #consoleLog").val();
    paramSerialized += "&manualexecution=" + $("#executionSettingsForm #manualExecution").val();
    paramSerialized += "&retries=" + $("#executionSettingsForm #retries").val();
    paramSerialized += "&priority=" + $("#executionSettingsForm #priority").val();
    paramSerialized += "&outputformat=json";

    var teststring = "";
    if (!fromCampaign) {
        var select = $("#testCaseList option:selected");
        select.each(function () {
            var item = $(this).data("item");
            if (isEmpty(item.testcase)) {
                teststring += "&test=" + item.test + "&testcase=" + item.testcase;
            } else {
                teststring += "&test=" + item.test + "&testcase=" + item.testcase;
            }
        });
    }


    var environmentstring = "";
    var settings = $('input[name="envSettings"]:checked').val();
    if (settings === "auto") {
        var envListAuto = $("#envSettingsAuto select").val();
        if (envListAuto !== null) {
            for (var index = 0; index < envListAuto.length; index++) {
                environmentstring += "&environment=" + envListAuto[index];
            }
        }
    } else if (settings === "manual") {
        environmentstring += "&manualurl=1";
        environmentstring += "&myhost=" + $("#envSettingsMan #myhost").val();
        environmentstring += "&mycontextroot=" + $("#envSettingsMan #mycontextroot").val();
        environmentstring += "&myloginrelativeurl=" + $("#envSettingsMan #myloginrelativeurl").val();
        environmentstring += "&myenvdata=" + $("#envSettingsMan #myenvdata").val();
    }

    var countriesstring = "";
    $("#countryList .countrycb").each(function () {
        if ($(this).prop("checked")) {
            countriesstring += "&country=" + $(this).prop("name");
        }
    });

    var robotsstring = "";
    var robotSettings = $("#robotSettingsForm #robot").serialize();
    if (robotSettings !== "robot=CustomConfiguration") {
        robotsstring += "&" + robotSettings;
    } else {
        paramSerialized += "&ss_ip=" + $("#robotSettingsForm #seleniumIP").val();
        paramSerialized += "&ss_p=" + $("#robotSettingsForm #seleniumPort").val();
        paramSerialized += "&browser=" + $("#robotSettingsForm #browser").val();
    }

    if (!fromCampaign) {
        if (teststring === "") {
            showMessageMainPage("danger", doc.getDocLabel("page_runtest", "select_one_testcase"), false);
        } else if (environmentstring === "") {
            showMessageMainPage("danger", doc.getDocLabel("page_runtest", "select_one_env"), false);
        } else if (countriesstring === "") {
            showMessageMainPage("danger", doc.getDocLabel("page_runtest", "select_one_country"), false);
        }
    }

    showLoader('#page-layout');

    var jqxhr = $.post("AddToExecutionQueuePrivate", paramSerialized + teststring + environmentstring + countriesstring + robotsstring);
    $.when(jqxhr).then(function (data) {
        // unblock when remote call returns
        hideLoader('#page-layout');
        data.message = data.message.replace(/\n/g, '<br>');
        if (getAlertType(data.messageType) === "success") {
            handleAddToQueueResponse(data, doRedirect);
        } else {
            showMessageMainPage(getAlertType(data.messageType), data.message, false);
        }
    }).fail(handleErrorAjaxAfterTimeout);

}

function handleAddToQueueResponse(data, doRedirect) {
    if (data.nbErrorRobotMissing > 0) {
        data.message = data.message + "<br>" + data.nbErrorRobotMissing + " Executions not added due to <b>Empty Robot</b>.";
    }
    if (data.nbErrorTCNotActive > 0) {
        data.message = data.message + "<br>" + data.nbErrorTCNotActive + " Executions not added due to <b>Test Case not active</b>.";
    }
    if (data.nbErrorTCNotAllowedOnEnv > 0) {
        data.message = data.message + "<br>" + data.nbErrorTCNotAllowedOnEnv + " Executions not added due to <b>Test Case not beeing allowed to run on the corresponding group of environment</b>.";
    }
    if (data.nbErrorEnvNotExistOrNotActive > 0) {
        data.message = data.message + "<br>" + data.nbErrorEnvNotExistOrNotActive + " Executions not added due to <b>Environment/Country not active or don't exist</b>.";
    }
    if (data.nbExe === 1) {
        data.message = data.message + "<br><a href='TestCaseExecution.jsp?executionQueueId=" + data.queueList[0].queueId + "'><button class='btn btn-primary' id='goToExecution'>Open Execution</button></a>";
    }
    if (data.nbExe > 1) {
        data.message = data.message + "<br><a href='ReportingExecutionByTag.jsp?Tag=" + encodeURIComponent(data.tag) + "'><button class='btn btn-primary' id='goToTagReport'>Report by Tag</button></a>"
    }
    var rc = getAlertType(data.messageType);
    if ((rc === "success") && (data.nbExe === 0)) {
        rc = "warning";
    }
    showMessageMainPage(rc, data.message, false, 60000);
    if ((data.nbExe === 1) && doRedirect) {
        window.location.href = "TestCaseExecution.jsp?executionQueueId=" + data.queueList[0].queueId;
    }
    if ((data.nbExe > 1) && doRedirect) {
        window.location.href = "ReportingExecutionByTag.jsp?Tag=" + encodeURIComponent(data.tag);
    }
}

/** UTILITY FUNCTIONS FOR TESTCASE FILTERS **/

function appendCampaignList() {
    var jqxhr = $.getJSON("ReadCampaign");
    $.when(jqxhr).then(function (data) {
        var campaignList = $("#campaignSelect");

        campaignList.append($('<option></option>').text(""));
        for (var index = 0; index < data.contentTable.length; index++) {
            campaignList.append($('<option></option>').text(data.contentTable[index].campaign)
                    .val(data.contentTable[index].campaign));
        }

    });
}

function multiSelectConf(name) {
    this.maxHeight = 450;
    this.checkboxName = name;
    this.buttonWidth = "100%";
    this.enableFiltering = true;
    this.enableCaseInsensitiveFiltering = true;
}

function loadMultiSelect(url, urlParams, selectName, textItem, valueItem) {
    var jqXHR = $.ajax({
        url: url,
        method: "GET",
        data: urlParams,
        dataType: "json",
        async: true,
        success: function (data) {
            var select = $("#" + selectName + "Filter");

            for (var index = 0; index < data.contentTable.length; index++) {
                var text = textItem.map(function (item) {
                    return data.contentTable[index][item];
                }).join(" - ");

                select.append($("<option></option>").text(text)
                        .val(data.contentTable[index][valueItem])
                        .data("item", data.contentTable[index]));
            }

            select.multiselect(new multiSelectConf(selectName));

            if (selectName === "test") {
                var test = GetURLParameter("test");
                if (test !== undefined && test !== null && test !== "") {
                    $("#filters").find("select[id='testFilter'] option[value='" + test + "']").attr("selected", "selected");
                    select.multiselect("rebuild");
                }
            }
        },
        "error": showUnexpectedError
    });
    return jqXHR;
}

function loadInvariantMultiSelect(selectName, idName) {
    var jqXHR = $.ajax({
        url: "FindInvariantByID",
        method: "GET",
        data: {idName: idName},
        dataType: "json",
        async: true,
        success: function (data) {
            var select = $("#" + selectName + "Filter");

            for (var option in data) {
                select.append($("<option></option>").text(data[option].value)
                        .val(data[option].value)
                        .data("item", data[option]));
            }

            select.multiselect(new multiSelectConf(selectName));
        },
        error: showUnexpectedError
    });
    return jqXHR;
}

function loadHardDefinedSingleSelect(selectName, values, initialSelectionIndex) {
    // Construct select 
    var select = $("#" + selectName + "Filter");
    for (var index in values) {
        // Define the option to append 
        var option = $("<option></option>")
                .text(values[index].label)
                .val(values[index].value)
                .data("item", values[index]);

        // Check if this option has to be initially selected 
        if (initialSelectionIndex !== undefined && index == initialSelectionIndex) {
            option.prop("selected", true);
        }

        // Append this option to the associated select 
        select.append(option);
    }
}

/** FUNCTIONS TO HANDLE ROBOT/EXECUTION PREFERENCES **/

function loadSelect(idName, selectName, forceReload, defaultValue) {

    if (forceReload === undefined) {
        forceReload = false;
    }

    var cacheEntryName = idName + "INVARIANT";
    if (forceReload) {
        sessionStorage.removeItem(cacheEntryName);
    }
    var list = JSON.parse(sessionStorage.getItem(cacheEntryName));
    var select = $("<select></select>").addClass("form-control input-sm");
    if (defaultValue !== undefined) {
        $("[name='" + selectName + "']").append($('<option></option>').text(defaultValue).val(defaultValue));
    }
    if (list === null) {
        return $.ajax({
            url: "FindInvariantByID",
            data: {idName: idName},
            async: true,
            success: function (data) {
                list = data;
                sessionStorage.setItem(cacheEntryName, JSON.stringify(data));
                for (var index = 0; index < list.length; index++) {
                    var item = list[index].value;
                    var desc = list[index].description;

//                    $("[name='" + selectName + "']").append($('<option></option>').text(item).val(item));
                    $("[name='" + selectName + "']").append($('<option></option>').text(item + " - " + desc).val(item));
                }
            }
        });
    } else {
        for (var index = 0; index < list.length; index++) {
            var item = list[index].value;
            var desc = list[index].description;

            $("[name='" + selectName + "']").append($('<option></option>').text(item + " - " + desc).val(item));
        }
    }

}

function openModalRobot_FromRunTest(robot, mode) {
    openModalRobot(robot, mode);
    $('#editRobotModal').on("hidden.bs.modal", function (e) {
        $('#editRobotModal').unbind("hidden.bs.modal");
        var robotobj = $('#editRobotModal').data("robot");
        if ((!(robotobj === undefined)) && ($('#editRobotModal').data("Saved"))) {
            if (mode === "EDIT") {
                // when modal is closed, we check that robot object exist and has been saved in order to update the current screen.
                loadRobotInfo($('#editRobotModal').data("robot").robot);
            }
            if (mode === "ADD") {
                var robotList = $("#robotSettings #robot");
                var newRobot = $('#editRobotModal').data("robot").robot;
                robotList.append($('<option></option>').text(newRobot).val(newRobot));
                robotList.val(newRobot);
                // when modal is closed, we check that robot object exist and has been saved in order to update the current screen.
                loadRobotInfo(newRobot);
            }
        }
    });
}


function appendRobotList() {
    var doc = new Doc();
    var jqXHR = $.getJSON("ReadRobot");
    $.when(jqXHR).then(function (data) {
        var robotList = $("#robotSettings #robot");

        robotList.append($('<option></option>').text(doc.getDocLabel("page_runtest", "custom_config")).val("CustomConfiguration"));
        for (var index = 0; index < data.contentTable.length; index++) {
            robotList.append($('<option></option>').text(data.contentTable[index].robot).val(data.contentTable[index].robot));
        }
    });

    return jqXHR;
}

function loadRobotInfo(robot) {

    if (!(robot instanceof Array)) {
        robot = [robot]
    }

    if (robot[0] !== "" && robot[0] !== "CustomConfiguration") {
        // We can edit Robot.
        $("#robotEdit").removeClass("disabled");
        $('#robotEdit').unbind("click");
        $("#robotEdit").click(function (e) {
            openModalRobot_FromRunTest(robot[0], "EDIT");
        });

        $.ajax({
            url: "ReadRobot",
            method: "GET",
            data: {robot: robot[0]},
            dataType: "json",
            async: true,
            success: function (data) {
                disableRobotFields();
                // Get Robot IP and Port from Executor.
                var ExeHost = "No executor found...";
                var ExePort = "";
                var nbExe = 0;
                for (var i = 0; i < data.contentTable.executors.length; i++) {
                    var curExecutor = data.contentTable.executors[i];
                    if (curExecutor.isActive) {
                        nbExe++;
                        if (nbExe < 2) {
                            ExeHost = curExecutor.host;
                            ExePort = curExecutor.port;
                        }
                    }
                }
                if (nbExe > 1) {
                    ExeHost += " (and " + (nbExe - 1) + " more...)";
                    ExePort += " (and " + (nbExe - 1) + " more...)";
                }
                $("#robotSettings #seleniumIP").val(ExeHost);
                $("#robotSettings #seleniumPort").val(ExePort);
                $("#robotSettings #browser").val(data.contentTable.browser);
            }
        });
    } else {
        var pref = JSON.parse(localStorage.getItem("robotSettings"));
        enableRobotFields();
        // No need to edit Robot.
        $("#robotEdit").addClass("disabled");
        $('#robotEdit').unbind("click");
        $("#saveRobotPreferences").removeClass("disabled");

        if (pref !== undefined && (pref.robot === "" || pref.robot === "CustomConfiguration")) {
            $("#robotSettings #robot").val(pref.robot);
            $("#robotSettings #seleniumIP").val(pref.ss_ip);
            $("#robotSettings #seleniumPort").val(pref.ss_p);
            $("#robotSettings #browser").val(pref.browser);
        } else {
            $("#robotSettings #seleniumIP").val("");
            $("#robotSettings #seleniumPort").val("");
            $("#robotSettings #browser").val("");
        }
    }

}

function saveRobotPreferences() {
    var pref = convertSerialToJSONObject($("#robotSettingsForm").serialize());
    localStorage.setItem("robotSettings", JSON.stringify(pref));
}

function saveExecutionPreferences() {
    var pref = convertSerialToJSONObject($("#executionSettingsForm").serialize());
    localStorage.setItem("executionSettings", JSON.stringify(pref));
}

function loadExecForm(tag) {
    return $.when(
            loadSelect("VERBOSE", "Verbose", false, ""),
            loadSelect("SCREENSHOT", "Screenshot", false, ""),
            loadSelect("VIDEO", "Video", false, ""),
            loadSelect("ROBOTLOG", "SeleniumLog", false, ""),
            loadSelect("CONSOLELOG", "ConsoleLog", false, ""),
            loadSelect("MANUALEXECUTION", "manualExecution", false, ""),
            loadSelect("PAGESOURCE", "PageSource", false, ""),
            loadSelect("RETRIES", "retries", false, "")
            ).then(function () {
        applyExecPref(tag);
    });
}

function loadRobotForm(browser) {
    var doc = new Doc();
    return $.when(
            appendRobotList(),
            loadSelect("BROWSER", "browser")
//            $("#robotSettingsForm [name=platform]").append($('<option></option>').text(doc.getDocLabel("page_runtest", "default")).val("")),
//            loadSelect("PLATFORM", "platform"),
//            $("#robotSettingsForm [name=screenSize]").append($('<option></option>').text(doc.getDocLabel("page_runtest", "default_full_screen")).val("")),
//            loadSelect("screensize", "screenSize")
//            $("#robotSettingsForm [name='screenSize']").autocomplete({source: getInvariantArray("SCREENSIZE", false)})
            ).then(function () {
        applyRobotPref(browser);
    });
}

function applyExecPref(tag) {
    var pref = JSON.parse(localStorage.getItem("executionSettings"));

    if (pref !== null) {
        if ((tag !== null) && (tag !== "null")) { // if tag defined from URL we take that value.
            $("#tag").val(tag);
        } else {
            $("#tag").val(pref.Tag);
        }
        $("#verbose").val(pref.Verbose);
        $("#screenshot").val(pref.Screenshot);
        $("#video").val(pref.Video);
        $("#pageSource").val(pref.PageSource);
        $("#seleniumLog").val(pref.SeleniumLog);
        $("#consoleLog").val(pref.ConsoleLog);
        $("#timeout").val(pref.timeout);
        $("#retries").val(pref.retries);
        $("#priority").val(pref.priority);
        $("#manualExecution").val(pref.manualExecution);
    }
}

function applyRobotPref(browser) {
    var pref = JSON.parse(localStorage.getItem("robotSettings"));

    if (pref !== null) {
        if ((pref.robot === "") || (pref.robot === undefined)) {
            enableRobotFields();
            // No need to edit Robot.
            $("#robotEdit").addClass("disabled");
            $('#robotEdit').unbind("click");

            $("#robotSettings #robot").val(pref.robot);
            $("#robotSettingsForm #seleniumIP").val(pref.ss_ip);
            $("#robotSettingsForm #seleniumPort").val(pref.ss_p);
            if (browser !== null) { // if browser defined from URL we take that value.
                $("#robotSettingsForm #browser").val(browser);
            } else {
                $("#robotSettingsForm #browser").val(pref.browser);
            }
            $("#robotSettingsForm #version").val(pref.BrowserVersion);
            $("#robotSettingsForm #platform").val(pref.platform);
            $("#robotSettingsForm #screenSize").val(pref.screenSize);
        } else {
            $("#robotSettings #robot").val(pref.robot);
            loadRobotInfo(pref.robot);
        }
    }
}

function disableRobotFields() {
    $("#robotSettings #seleniumIP").prop("readonly", true);
    $("#robotSettings #seleniumPort").prop("readonly", true);
    $("#robotSettings #browser").prop("disabled", "disabled");
    $("#robotSettings #version").prop("readonly", true);
    $("#robotSettings #platform").prop("disabled", "disabled");
    $("#robotSettings #screenSize").prop("disabled", "disabled");
}

function enableRobotFields() {
    $("#robotSettings #seleniumIP").prop("readonly", false);
    $("#robotSettings #seleniumPort").prop("readonly", false);
    $("#robotSettings #browser").prop("disabled", false);
    $("#robotSettings #version").prop("readonly", false);
    $("#robotSettings #platform").prop("disabled", false);
    $("#robotSettings #screenSize").prop("disabled", false);
}

//Load the data to put in Extended Test Case Filters panel
function loadTestCaseFilterData(system) {
    showLoader("#filtersPanelContainer");
    $.when(
            loadMultiSelect("ReadTest", "", "test", ["test", "description"], "test"),
//            loadMultiSelect("ReadProject", "sEcho=1", "project", ["idProject"], "idProject"),
            loadMultiSelect("ReadApplication", "e=1" + getUser().defaultSystemsQuery, "application", ["application"], "application"),
            loadMultiSelect("ReadUserPublic", "", "creator", ["login"], "login"),
            loadMultiSelect("ReadUserPublic", "", "implementer", ["login"], "login"),
            loadMultiSelect("ReadCampaign", "", "campaign", ["campaign"], "campaign"),
            loadMultiSelect("ReadBuildRevisionInvariant", "level=1" + getUser().defaultSystemsQuery, "targetMajor", ["versionName"], "versionName"),
            loadMultiSelect("ReadBuildRevisionInvariant", "level=2" + getUser().defaultSystemsQuery, "targetMinor", ["versionName"], "versionName"),
            loadMultiSelect("ReadLabel", "e=1" + getUser().defaultSystemsQuery, "labelid", ["label"], "id"),
            loadInvariantMultiSelect("system", "SYSTEM"),
            loadInvariantMultiSelect("priority", "PRIORITY"),
            loadInvariantMultiSelect("type", "TESTCASE_TYPE"),
            loadInvariantMultiSelect("status", "TCSTATUS")
            ).then(function () {
        hideLoader("#filtersPanelContainer");
    });
}


function loadTestCaseEssentialData(test, testcase, environment, country, tag, browser, doLoadManualSelection) {
    showLoader("#chooseTest");
    $.when(
            loadExecForm(tag),
            loadRobotForm(browser),
            loadHardDefinedSingleSelect("length", [{label: '50', value: 50}, {label: '100', value: 100}, {label: '>100', value: -1}], 0)
            )
            .then(function () {
                if (doLoadManualSelection) {
                    selectionManual(test, testcase, environment, country);
                }
            });

}
//Remove the call to updateUserPreferences when no new data are loaded by the filter
function bindToggleCollapseCustom() {
    $(".collapse").each(function () {
        if (this.id !== "sidenavbar-subnavlist") {//disable interaction with the navbar
            $(this).on('shown.bs.collapse', function () {
                localStorage.setItem(this.id, true);
                if ($(this)[0].id != "filtersPanel")
                    updateUserPreferences();
                $(this).prev().find(".toggle").removeClass("glyphicon-chevron-down").addClass("glyphicon-chevron-right");
            });

            $(this).on('hidden.bs.collapse', function () {
                localStorage.setItem(this.id, false);
                if ($(this)[0].id != "filtersPanel")
                    updateUserPreferences();
                $(this).prev().find(".toggle").removeClass("glyphicon-chevron-right").addClass("glyphicon-chevron-down");
            });

            // pannel that has class defaultNotExpanded are not expanded by defaault. All others are.
            if (localStorage.getItem(this.id) === null || localStorage.getItem(this.id) === undefined) {
                if ($(this).hasClass("defaultNotExpanded")) {
                    localStorage.setItem(this.id, false);
                }
            }

            if (localStorage.getItem(this.id) === "false") {
                $(this).removeClass('in');
                $(this).prev().find(".toggle").removeClass("glyphicon-chevron-right").addClass("glyphicon-chevron-down");
            } else {
                $(this).addClass('in');
                $(this).prev().find(".toggle").removeClass("glyphicon-chevron-down").addClass("glyphicon-chevron-right");
            }
        }
    });
}
