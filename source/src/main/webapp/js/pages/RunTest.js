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
        var test = GetURLParameter("test");
        var testcase = GetURLParameter("testcase");
        var environment = GetURLParameter("environment");
        var country = GetURLParameter("country");
        var tag = GetURLParameter("tag");
        var browser = GetURLParameter("browser");
        //check if Extended Test Case Filters is collapse and load the data if it is not
        var filterPanelDataLoaded = false;
        var filterPanel = document.getElementById("filtersPanel");
        if (filterPanel.className === "panel-body collapse in") {
            loadTestCaseFilterData(system);
            filterPanelDataLoaded = true;
            updateUserPreferences();
        }
        //add a listenner to load the data when needed
        $("#FilterPanelHeader").click(function () {
            if (!filterPanelDataLoaded) {
                loadTestCaseFilterData(system);
                filterPanelDataLoaded = true
                updateUserPreferences();
            }
        });

        $("#robotSettings #robot").change(function () {
            loadRobotInfo($(this).val());
        });

        //load the data that need to be display in any case
        loadTestCaseEssentialData(test, testcase, environment, country, tag, browser);

        var system = getUser().defaultSystem;

        $("#SelectionManual").on("click", function () {
            selectionManual();
        });
        $("#SelectionCampaign").on("click", function () {
            selectionCampaign();
        });

        $("#run").click(sendForm);
        $("#runList").click(sendForm);

        // Run Campaign button click
        $("#runCampaign").click(function () {
            runCampaign();
        });
        $("#runCampaignUp").click(function () {
            runCampaign();
        });

        // Run Test Case button click
        $("#runTestCase").click(function () {
            runTestCase();
        });
        $("#runTestCaseUp").click(function () {
            runTestCase();
        });

        $("#loadFiltersBtn").click(function () {
            loadTestCaseFromFilter(null, null);
        });

        $("#loadCampaignBtn").click(function () {
//            var campaign = $("#campaignSelect").val();
//            loadCampaignContent(campaign);
//            loadCampaignParameter(campaign);
            loadCampaign();
        });

        $("#resetbutton").click(function () {
            $(".multiselectelement").each(function () {
                $(this).multiselect('deselectAll', false);
                $(this).multiselect('updateButtonText');
            });
        });

        $("#addQueue").click(function () {
            checkExecution(false);
        });
        $("#addQueueAndRun").click(function () {
            checkExecution(true);
        });
        $("#addQueueAndRunBis").click(function () {
            checkExecution(true);
        });

        $("#testcaseSelectAll").click(function () {
            $("#testCaseList option").prop("selected", true);
            updatePotentialNumber();
        });

        $("#testcaseSelectNone").click(function () {
            $("#testCaseList option").prop("selected", false);
            updatePotentialNumber();
        });

        $("#resetQueue").click(function (event) {
            stopPropagation(event);
            $("#queue").empty();
            $("#notValidList").empty();
            updateValidNumber();
            updateNotValidNumber();
            $("#notValid").hide();
        });

        $("#notValidNumber").click(function () {
            $("#notValidTC").modal("show");
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


        $("#testCaseList").on("change", updatePotentialNumber);
        $("#envSettingsAuto select").on("change", updatePotentialNumber);

        $("#countrySelectAll").on("click", function () {
            $("#countryList input").prop('checked', true);
            updatePotentialNumber();
        });
        $("#countrySelectNone").on("click", function () {
            $("#countryList input").prop('checked', false);
            updatePotentialNumber();
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
    $("h1.page-title-line").text(doc.getDocLabel("page_runtest", "title"));
    $("#selectionPanel div.panel-heading").text(doc.getDocLabel("page_runtest", "selection_type"));
    $("#selectionPanel input[value='filters']").next().text(doc.getDocLabel("page_runtest", "select_list_test"));
    $("#selectionPanel input[value='campaign']").next().text(doc.getDocLabel("page_runtest", "select_campaign"));
    $("#loadCampaignBtn").text(doc.getDocLabel("page_runtest", "load"));
    $("#ChooseTestHeader").text(doc.getDocLabel("page_runtest", "ChooseTest"));
    $("#FilterHeader").text(doc.getDocLabel("page_runtest", "filters"));
    $("#lbl_test").text(doc.getDocLabel("page_runtest", "test"));
    $("#lbl_project").text(doc.getDocLabel("page_runtest", "project"));
    $("#lbl_application").text(doc.getDocLabel("page_runtest", "application"));
    $("#lbl_creator").text(doc.getDocLabel("page_runtest", "creator"));
    $("#lbl_implementer").text(doc.getDocLabel("page_runtest", "implementer"));
    $("#lbl_group").text(doc.getDocLabel("page_runtest", "group"));
    $("#lbl_campaign").text(doc.getDocLabel("page_runtest", "campaign"));
    $("#lbl_priority").text(doc.getDocLabel("page_runtest", "priority"));
    $("#lbl_status").text(doc.getDocLabel("page_runtest", "status"));
    $("#lbl_targetRev").text(doc.getDocLabel("page_runtest", "targetrev"));
    $("#lbl_targetSprint").text(doc.getDocLabel("page_runtest", "targetsprint"));
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
    $("#potential span:nth-child(2)").text(doc.getDocLabel("page_runtest", "potential"));
    $("#addQueue").text(doc.getDocLabel("page_runtest", "addtoqueue"));
    $("#addQueueAndRun").text(doc.getDocLabel("page_runtest", "addtoqueueandrun"));
    $("#addQueueAndRunBis").text(doc.getDocLabel("page_runtest", "addtoqueueandrun"));
    $("#RobotPanel .panel-heading").text(doc.getDocLabel("page_runtest", "robot_settings"));
    $("#RobotPanel label[for='robot']").text(doc.getDocLabel("page_runtest", "select_robot"));
    $("#RobotPanel label[for='seleniumIP']").text(doc.getDocLabel("page_runtest", "selenium_ip"));
    $("#RobotPanel label[for='seleniumPort']").text(doc.getDocLabel("page_runtest", "selenium_port"));
    $("#RobotPanel label[for='browser']").text(doc.getDocLabel("page_runtest", "browser"));
    $("#RobotPanel label[for='version']").text(doc.getDocLabel("page_runtest", "version"));
    $("#RobotPanel label[for='platform']").text(doc.getDocLabel("page_runtest", "platform"));
    $("#RobotPanel label[for='screenSize']").text(doc.getDocLabel("page_runtest", "screensize"));
    $("#saveRobotPreferences").text(doc.getDocLabel("page_runtest", "saverobotpref"));
    $("#executionPanel .panel-heading").text(doc.getDocLabel("page_runtest", "execution_settings"));
    $("#executionPanel label[for='tag']").text(doc.getDocLabel("page_runtest", "tag"));
    $("#executionPanel label[for='verbose']").text(doc.getDocLabel("page_runtest", "verbose"));
    $("#executionPanel label[for='screenshot']").text(doc.getDocLabel("page_runtest", "screenshot"));
    $("#executionPanel label[for='pageSource']").text(doc.getDocLabel("page_runtest", "pagesource"));
    $("#executionPanel label[for='seleniumLog']").text(doc.getDocLabel("page_runtest", "seleniumlog"));
    $("#executionPanel label[for='synchroneous']").text(doc.getDocLabel("page_runtest", "synchroneous"));
    $("#executionPanel label[for='timeout']").text(doc.getDocLabel("page_runtest", "timeout"));
    $("#executionPanel label[for='retries']").text(doc.getDocLabel("page_runtest", "retries"));
    $("#executionPanel label[for='manualExecution']").text(doc.getDocLabel("page_runtest", "manual_execution"));
    $("#saveExecutionParams").text(doc.getDocLabel("page_runtest", "save_execution_params"));
    $("#notValid span:nth-child(2)").text(doc.getDocLabel("page_runtest", "notValid"));
    $("#valid span:nth-child(2)").text(doc.getDocLabel("page_runtest", "valid"));
    $("#resetQueue").text(doc.getDocLabel("page_runtest", "reset_queue"));
    $("#resetQueue").next().text(doc.getDocLabel("page_runtest", "queue"));
    $("#run").text(doc.getDocLabel("page_runtest", "run"));
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
        $("#countrySelectAll").prop("disabled", true);
        $("#countrySelectNone").prop("disabled", true);

        $("#exeList").hide();
        $("#potencialBlock").hide();
        $("#run").hide();
        $("#addQueueAndRunBis").hide();
        $("#runCampaign").show();
        $("#runCampaignUp").show();

        // NEW
        $("#runTestCase").hide();
        $("#runTestCaseUp").hide();

        $("#filtersPanelContainer").hide();
        $("#campaignSelection").show();
        $("#testCaseList").empty();
        $("#envSettingsAuto select").empty();
        displayUniqueEnvList("environment", "");
//    updatePotentialNumber();
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
            if (($(this).attr("name") == country) || (mysize <= 1)) { // We select the the country if it is the one from the URL or if there is only 1 country.
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
            displayUniqueEnvList("environment", getUser().defaultSystem, environment);
        }

        $("#testcaseSelectAll").prop("disabled", false);
        $("#testcaseSelectNone").prop("disabled", false);
        $("#countrySelectAll").prop("disabled", false);
        $("#countrySelectNone").prop("disabled", false);

        $("#campaignSelection").hide();
        $("#filters").show();
        $("#resetbutton").show();
        $("#exeList").show();
        $("#potencialBlock").show();
        $("#run").show();
        $("#addQueueAndRunBis").show();
        $("#runCampaign").hide();
        $("#runCampaignUp").hide();
        $("#filtersPanelContainer").show();

        // NEW
        $("#runTestCase").show();
        $("#runTestCaseUp").show();
        $("#resetbutton").hide();
        $("#exeList").hide();
        $("#potencialBlock").hide();
        $("#run").hide();
        $("#addQueueAndRunBis").hide();

        loadTestCaseFromFilter(test, testcase);

        updatePotentialNumber();
        $('#selectionPanel').data("LoadedMode", "manual");

    }

}

function loadTestCaseFromFilter(defTest, defTestcase) {

//    console.debug("loadTestCaseFromFilter Called" + defTest + defTestcase);
    showLoader("#chooseTest");
    var testURL = "";
    var testCaseURL = "";

    if ((defTest !== null) && (defTest !== undefined)) { // If test is defined, we limit the testcase list on that test.
        testURL = "&test=" + defTest;
    }
    if ((defTestcase !== null) && (defTestcase !== undefined)) { // If test is defined, we limit the testcase list on that test.
        testCaseURL = "&testCase=" + defTestcase;
    }
    // Get the requested result size value
    var lengthURL = '&length=' + $("#lengthFilter").find(':selected').val();
    $.ajax({
        url: "ReadTestCase",
        method: "GET",
        data: "filter=true&" + $("#filters").serialize() + testURL + testCaseURL + lengthURL,
        datatype: "json",
        async: true,
        success: function (data) {

            var testCaseList = $("#testCaseList");

            testCaseList.empty();

            if (data.contentTable === undefined) {
                showMessageMainPage("danger", "Test Case : " + defTest + " - " + defTestcase + " does not exist !", true);
            } else {
                if (data.contentTable.length > 0) {
                    for (var i = 0; i < data.contentTable.length; i++) {

                        var text = data.contentTable[i].test + " - " + data.contentTable[i].testCase + " [" + data.contentTable[i].application + "]: " + data.contentTable[i].description;

                        testCaseList.append($("<option></option>")
                                .text(text)
                                .val(data.contentTable[i].test + "-" + data.contentTable[i].testCase)
                                .data("item", data.contentTable[i]));
                    }
                } else {
                    var text = data.contentTable.test + " - " + data.contentTable.testCase + " [" + data.contentTable.application + "]: " + data.contentTable.description;

                    testCaseList.append($("<option></option>")
                            .text(text)
                            .val(data.contentTable.test + "-" + data.contentTable.testCase)
                            .data("item", data.contentTable));
                }
            }
            hideLoader("#chooseTest");
            if ((defTest !== null) && (defTest !== undefined)) { // if test is defined we select the value in the select list.
                $('#testCaseList').find('option[value="' + defTest + '-' + defTestcase + '"]').prop("selected", true);
                updatePotentialNumber();
            }
        }
    });
}

function appendCountryList(defCountry) {
    var jqxhr = $.getJSON("FindInvariantByID", "idName=COUNTRY");
    $.when(jqxhr).then(function (data) {
        var countryList = $("[name=countryList]");

        for (var index = 0; index < data.length; index++) {
            var country = data[index].value;
            if ((country === defCountry) || (data.length <= 1)) // We select the the country if it is the one from the URL or if there is only 1 country.
            {
                myChecked = 'checked="" ';

            } else {
                myChecked = "";
            }
            countryList.append('<label class="checkbox-inline">\n\
                                <input class="countrycb" type="checkbox" ' + myChecked + ' name="' + country + '"/>' + country + '\
                                </label>');
        }
        $("#countryList input.countrycb").each(function () {
            $(this).on("change", updatePotentialNumber);
        });
    });
}

/** UTILITY FUNCTIONS FOR CAMPAIGN LAUNCHING **/

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
                    var text = data.contentTable[index].test + " - " + data.contentTable[index].testCase + " [" + data.contentTable[index].application + "]: " + data.contentTable[index].description;

                    testCaseList.append($("<option></option>")
                            .text(text)
                            .val(data.contentTable[index].testCase)
                            .prop("selected", true)
                            .data("item", data.contentTable[index]));
                }

                showMessage(data, $('#page-layout'), true);
                updatePotentialNumber();
                hideLoader("#chooseTest");
            },
            error: showUnexpectedError
        });
    }
}

function loadCampaignParameter(campaign) {
    $.ajax({
        url: "ReadCampaignParameter",
        method: "GET",
        data: {campaign: campaign},
        datatype: "json",
        async: true,
        success: function (data) {
            var browser = [];
            var env = [];
            var countries = [];

            for (var index = 0; index < data.contentTable.length; index++) {
                var type = data.contentTable[index].parameter;
                var value = data.contentTable[index].value;

                if (type === "BROWSER") {
                    browser.push(value);
                } else if (type === "ENVIRONMENT") {
                    env.push(value);
                } else if (type === "COUNTRY") {
                    countries.push(value);
                }
            }

            $("#envSettingsAuto select").prop("disabled", "disabled").val(env);

            $("input[name='envSettings'][value='auto']").click();
            $("input[name='envSettings']").prop("disabled", true);

            $("#countryList input.countrycb").each(function () {
                var country = $(this).prop("name");

                $(this).prop("disabled", "disabled");
                if (countries.indexOf(country) !== -1) {
                    $(this).prop("checked", true);
                } else {
                    $(this).prop("checked", false);
                }
            });
        }
    });
}

/** FORM SENDING UTILITY FUNCTIONS (VALID FOR SERVLET ADDTOEXECUTIONQUEUE) **/

function checkForms() {
    var type;
    var message;
    var doc = new Doc();

    if ($("#queue li").length === 0) {
        type = getAlertType("KO");
        message = doc.getDocLabel("page_runtest", "empty_queue"); //Execution queue is empty !
        showMessageMainPage(type, message, false);
        return false;
    } else if ($("#queue li").length > 1 && $("#tag").val() === "") { // More than 1 excution and no Tag specified.
        type = getAlertType("KO");
        message = doc.getDocLabel("page_runtest", "more_than_one_execution_requested"); //More than 1 execution has been requested. It will be executed in batch mode so please, indicate a tag (to find it back)
        showMessageMainPage(type, message, false);
        return false;
    }
    return true;
}

function runCampaign() {

    clearResponseMessageMainPage();

    var browserSettings = $("#robotSettingsForm #browser").serialize();
    var paramSerialized = "campaign=" + $("#campaignSelect").val();
    paramSerialized += "&robot=" + $("#robotSettingsForm #robot").val();
    paramSerialized += "&ss_ip=" + $("#robotSettingsForm #seleniumIP").val();
    paramSerialized += "&ss_p=" + $("#robotSettingsForm #seleniumPort").val();
    paramSerialized += "&tag=" + $("#executionSettingsForm #tag").val();
    paramSerialized += "&screenshot=" + $("#executionSettingsForm #screenshot").val();
    paramSerialized += "&verbose=" + $("#executionSettingsForm #verbose").val();
    paramSerialized += "&timeout=" + $("#executionSettingsForm #timeout").val();
    paramSerialized += "&pagesource=" + $("#executionSettingsForm #pageSource").val();
    paramSerialized += "&seleniumlog=" + $("#executionSettingsForm #seleniumLog").val();
    paramSerialized += "&manualexecution=" + $("#executionSettingsForm #manualExecution").val();
    paramSerialized += "&retries=" + $("#executionSettingsForm #retries").val();
    paramSerialized += "&priority=1000";
    paramSerialized += "&outputformat=json";
    if (!isEmpty(browserSettings)) {
        paramSerialized += "&" + browserSettings;
    }

    showLoader('#page-layout');

    var jqxhr = $.post("AddToExecutionQueueV002", paramSerialized, "json");
    $.when(jqxhr).then(function (data) {
        // unblock when remote call returns 
        hideLoader('#page-layout');
        data.message = data.message.replace(/\n/g, '<br>');
        if (getAlertType(data.messageType) === "success") {
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
                data.message = data.message + "<br><a href='TestCaseExecution.jsp?executionQueueId=" + data.queueList[0].queueId + "'><button class='btn btn-primary' id='goToExecution'>Get to Execution</button></a>";
            }
            if (data.nbExe > 1) {
                data.message = data.message + "<br><a href='ReportingExecutionByTag.jsp?Tag=" + data.tag + "'><button class='btn btn-primary' id='goToTagReport'>Report by Tag</button></a>"
            }
            showMessageMainPage(getAlertType(data.messageType), data.message, false, 60000);
        } else {
            showMessageMainPage(getAlertType(data.messageType), data.message, false);
        }
    }).fail(handleErrorAjaxAfterTimeout);

}

function runTestCase() {

    var doc = new Doc();

    clearResponseMessageMainPage();

    var browserSettings = $("#robotSettingsForm #browser").serialize();
    var paramSerialized = "robot=" + $("#robotSettingsForm #robot").val();
    paramSerialized += "&ss_ip=" + $("#robotSettingsForm #seleniumIP").val();
    paramSerialized += "&ss_p=" + $("#robotSettingsForm #seleniumPort").val();
    paramSerialized += "&tag=" + $("#executionSettingsForm #tag").val();
    paramSerialized += "&screenshot=" + $("#executionSettingsForm #screenshot").val();
    paramSerialized += "&verbose=" + $("#executionSettingsForm #verbose").val();
    paramSerialized += "&timeout=" + $("#executionSettingsForm #timeout").val();
    paramSerialized += "&pagesource=" + $("#executionSettingsForm #pageSource").val();
    paramSerialized += "&seleniumlog=" + $("#executionSettingsForm #seleniumLog").val();
    paramSerialized += "&manualexecution=" + $("#executionSettingsForm #manualExecution").val();
    paramSerialized += "&retries=" + $("#executionSettingsForm #retries").val();
    paramSerialized += "&priority=1000";
    paramSerialized += "&outputformat=json";
    if (!isEmpty(browserSettings)) {
        paramSerialized += "&" + browserSettings;
    }

    var teststring = "";
    var select = $("#testCaseList option:selected");
    select.each(function () {
        var item = $(this).data("item");
        teststring += "&test=" + item.test + "&testcase=" + item.testCase;
    });


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
//            countries.push($(this).prop("name"));
            countriesstring += "&country=" + $(this).prop("name");
        }
    });


    if (teststring === "") {
        showMessageMainPage("danger", doc.getDocLabel("page_runtest", "select_one_testcase"), false);
    } else if (environmentstring === "") {
        showMessageMainPage("danger", doc.getDocLabel("page_runtest", "select_one_env"), false);
    } else if (countriesstring === "") {
        showMessageMainPage("danger", doc.getDocLabel("page_runtest", "select_one_country"), false);
    } else {
        console.info(teststring);
        console.info(countriesstring);
        console.info(environmentstring);
    }

    showLoader('#page-layout');

    console.info(paramSerialized + teststring + countriesstring + environmentstring);

    var jqxhr = $.post("AddToExecutionQueueV002", paramSerialized + teststring + countriesstring + environmentstring);
    $.when(jqxhr).then(function (data) {
        // unblock when remote call returns 
        hideLoader('#page-layout');
        data.message = data.message.replace(/\n/g, '<br>');
        if (getAlertType(data.messageType) === "success") {
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
                data.message = data.message + "<br><a href='TestCaseExecution.jsp?executionQueueId=" + data.queueList[0].queueId + "'><button class='btn btn-primary' id='goToExecution'>Get to Execution</button></a>";
            }
            if (data.nbExe > 1) {
                data.message = data.message + "<br><a href='ReportingExecutionByTag.jsp?Tag=" + data.tag + "'><button class='btn btn-primary' id='goToTagReport'>Report by Tag</button></a>"
            }
            showMessageMainPage(getAlertType(data.messageType), data.message, false, 60000);
        } else {
            showMessageMainPage(getAlertType(data.messageType), data.message, false);
        }
    }).fail(handleErrorAjaxAfterTimeout);

}


function sendForm() {

    if ($("#queue li").length > 1) { // We have more than 1 execution in the queue.
        if ($("#tag").val() === "") { // We force the Tag if it is not defined yet.
            var utc = new Date();
            var tag = getUser().login + "-" + utc.toJSON().slice(0, 13) + utc.toJSON().slice(14, 16) + utc.toJSON().slice(17, 19)
            $("#tag").prop("value", tag);
        }
    }

    if (checkForms()) {
        var data = {};
        var executionList = $("#queue li");
        var executionArray = [];
        var browsers = $("#robotSettingsForm #browser").val() ? $("#robotSettingsForm #browser").val() : [];
        var robotSettings = convertSerialToJSONObject($("#robotSettingsForm").serialize());
        var execSettings = convertSerialToJSONObject($("#executionSettingsForm").serialize());

        executionList.each(function () {
            var data = $(this).data("item");
            executionArray.push(data);
        });

        if (executionArray.length === 1) {

            //Call RunTestCase
            setSingleExecutionDataForm(executionArray);
            $("#RunTestCase").submit();

        } else if (executionArray.length > 1) {

            for (var key in robotSettings) {
                data[key] = robotSettings[key];
            }
            for (var key in execSettings) {
                data[key] = execSettings[key];
            }

            if ($('input[name="envSettings"]:checked').val() === "manual") {
                data.ManualHost = $("#robotSettingsForm #myhost").val();
                data.ManualContextRoot = $("#robotSettingsForm #mycontextroot").val();
                data.ManualLoginRelativeURL = $("#robotSettingsForm #myloginrelativeurl").val();
                data.ManualEnvData = $("#robotSettingsForm #myenvdata").val();
            }

            data.browsers = JSON.stringify(browsers);
            data.toAddList = JSON.stringify(executionArray);
            data.push = true;

            $.ajax({
                url: "GetExecutionQueue",
                method: "POST",
                data: data,
                async: true,
                success: function (data) {
                    if (data.redirect) {
                        window.location.assign(data.redirect);
                    }
                },
                error: showUnexpectedError
            });
        }
    }
}

function setSingleExecutionDataForm(executionArray) {
    var browser = $("#robotSettingsForm #browser").val() ? $("#robotSettingsForm #browser").val() : [""];
    var settings = $('input[name="envSettings"]:checked').val();

    if (settings === "auto") {
        $("#manualURLATQ").val("false");
        $("#envATQ").val(executionArray[0].env);
    } else {
        $("#manualURLATQ").val("true");
    }

    $("#testATQ").val(executionArray[0].test);
    $("#testcaseATQ").val(executionArray[0].testcase);
    $("#countryATQ").val(executionArray[0].country);
    $("#browserATQ").val(browser[0]);
    $("#myhostATQ").val($("#myhost").val());
    $("#mycontextrootATQ").val($("#mycontextroot").val());
    $("#myloginrelativeurlATQ").val($("#myloginrelativeurl").val());
    $("#myenvdataATQ").val($("#myenvdata").val());
    $("#screenshotATQ").val($("#screenshot").val());
    $("#verboseATQ").val($("#verbose").val());
    $("#timeoutATQ").val($("#timeout").val());
    $("#synchroneousATQ").val($("#synchroneous").val());
    $("#pageSourceATQ").val($("#pageSource").val());
    $("#seleniumLogATQ").val($("#seleniumLog").val());
    $("#manualExecutionATQ").val($("#manualExecution").val());
    $("#retriesATQ").val($("#retries").val());
    $("#screenSizeATQ").val($("#robotSettingsForm #screenSize").val());
    $("#manualRobotATQ").val($("#robotSettings #robot").val());
    $("#ss_ipATQ").val($("#robotSettingsForm #seleniumIP").val());
    $("#ss_pATQ").val($("#robotSettingsForm #seleniumPort").val());
    $("#versionATQ").val($("#robotSettingsForm #version").val());
    $("#platformATQ").val($("#robotSettingsForm #platform").val());
    $("#tagATQ").val($("#tag").val());
}

/** UTILITY FUNCTIONS FOR QUEUE **/

function deleteRow(row) {
    row.parent('li').remove();
}

function getCountries() {
    var countries = [];

    $("#countryList .countrycb").each(function () {
        if ($(this).prop("checked")) {
            countries.push($(this).prop("name"));
        }
    });

    return countries;
}

function getEnvironment() {
    var envList = [];
    var settings = $('input[name="envSettings"]:checked').val();

    if (settings === "auto") {
        var envListAuto = $("#envSettingsAuto select").val();

        if (envListAuto !== null) {
            for (var index = 0; index < envListAuto.length; index++) {
                envList.push(envListAuto[index]);
            }
        }
    } else if (settings === "manual") {
        envList.push("MANUAL");
    }

    return envList;
}

function notValidHandler(list) {
    if (list.length !== 0) {
        var queue = $("#notValidList");

        for (var index = 0; index < list.length; index++) {
            var execution = list[index];

            queue.append($('<li></li>').addClass("list-group-item").append($("<div></div>").text(execution.test + " - " + execution.testcase + " - " +
                    execution.env + " - " + execution.country)).append($("<div></div>").text(execution.message).addClass("error-msg")).data("item", execution));

        }

        updateNotValidNumber();
        $("#notValid").show();
    }
}

function updateNotValidNumber() {
    $("#notValidNumber").text($("#notValidList li").length);
}

function updateValidNumber() {
    $("#validNumber").text($("#queue li").length);
}

function updatePotentialNumber() {
    var testCaseSelected = $("#testCaseList option:selected").length;
    var envSelected = $("#envSettingsAuto select option:selected").length;
    var countrySelected = getCountries().length;
    var result = testCaseSelected * envSelected * countrySelected;

    $("#potentialNumber").text(result);
}

function addToQueue(executionList) {
    var notValidList = [];
    var queue = $("#queue");
    var removeBtn = $("<span></span>").addClass("glyphicon glyphicon-remove delete").click(function () {
        deleteRow($(this));
        updateValidNumber();
    });

    for (var index = 0; index < executionList.length; index++) {
        var execution = executionList[index];

        if (execution.isValid) {
            queue.append($('<li></li>').addClass("list-group-item").text(execution.test + " - " + execution.testcase + " - " +
                    execution.env + " - " + execution.country)
                    .prepend(removeBtn.clone(true)).data("item", execution));
        } else {
            notValidList.push(execution);
        }
    }
    updateValidNumber();

    notValidHandler(notValidList);
}

function checkExecution(triggerRun) {
    var doc = new Doc();
    var select = $("#testCaseList option:selected");
    var environment = getEnvironment();
    var countries = getCountries();
    var browser = $("#robotSettingsForm #browser").val() ? $("#robotSettingsForm #browser").val() : [];
    var testcase = [];

    select.each(function () {
        var item = $(this).data("item");

        testcase.push({"test": item.test,
            "testcase": item.testCase,
            "application": item.application,
            "runQA": item.runQA,
            "runUAT": item.runUAT,
            "runPROD": item.runPROD});
    });

    if (testcase.length === 0) {
        showMessageMainPage("danger", doc.getDocLabel("page_runtest", "select_one_testcase"), false);
    } else if (environment.length === 0) {
        showMessageMainPage("danger", doc.getDocLabel("page_runtest", "select_one_env"), false);
    } else if (countries.length === 0) {
        showMessageMainPage("danger", doc.getDocLabel("page_runtest", "select_one_country"), false);
    } else {
        showLoader("#queuePanel");
        $.ajax({
            url: "GetExecutionQueue",
            method: "POST",
            data: {"check": true,
                "push": false,
                "testcase": JSON.stringify(testcase),
                "environment": JSON.stringify(environment),
                "countries": JSON.stringify(countries),
                "browsers": JSON.stringify(browser)},
            success: function (data) {
                hideLoader("#queuePanel");
                addToQueue(data.contentTable);
                if (triggerRun) {
                    sendForm();
                }
            },
            error: showUnexpectedError
        });
    }
}

/** UTILITY FUNCTIONS FOR FILTERS **/

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
    this.maxHeight = 150;
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

            if (selectName == "test") {
                var test = GetURLParameter("test");
                if (test != undefined && test != null && test != "") {
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

//    console.debug("display Invariant " + idName + " " + forceReload);
    if (forceReload === undefined) {
        forceReload = false;
    }

    var cacheEntryName = idName + "INVARIANT";
    if (forceReload) {
//        console.debug("Purge " + cacheEntryName);
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

        robotList.append($('<option></option>').text(doc.getDocLabel("page_runtest", "custom_config")).val(""));
        for (var index = 0; index < data.contentTable.length; index++) {
            robotList.append($('<option></option>').text(data.contentTable[index].robot).val(data.contentTable[index].robot));
        }
    });

    return jqXHR;
}

function loadRobotInfo(robot) {

    if (robot !== "") {
        // We can edit Robot.
        $("#robotEdit").removeClass("disabled");
        $('#robotEdit').unbind("click");
        $("#robotEdit").click(function (e) {
            openModalRobot_FromRunTest(robot, "EDIT");
        });

        $.ajax({
            url: "ReadRobot",
            method: "GET",
            data: {robot: robot},
            dataType: "json",
            async: true,
            success: function (data) {
                disableRobotFields();
                $("#robotSettings #seleniumIP").val(data.contentTable.host);
                $("#robotSettings #seleniumPort").val(data.contentTable.port);
                $("#robotSettings #browser").val(data.contentTable.browser);
                $("#robotSettings #version").val(data.contentTable.version);
                $("#robotSettings #platform").val(data.contentTable.platform);
                $("#robotSettings #screenSize").val(data.contentTable.screenSize);
            }
        });
    } else {
        var pref = JSON.parse(localStorage.getItem("robotSettings"));
        enableRobotFields();
        // No need to edit Robot.
        $("#robotEdit").addClass("disabled");
        $('#robotEdit').unbind("click");

        if (pref !== null && pref.robot === "") {
            $("#robotSettings #robot").val(pref.robot);
            $("#robotSettings #seleniumIP").val(pref.ss_ip);
            $("#robotSettings #seleniumPort").val(pref.ss_p);
            $("#robotSettings #browser").val(pref.browser);
            $("#robotSettings #version").val(pref.BrowserVersion);
            $("#robotSettings #platform").val(pref.platform);
            $("#robotSettings #screenSize").val(pref.screenSize);
        } else {
            $("#robotSettings #seleniumIP").val("");
            $("#robotSettings #seleniumPort").val("");
            $("#robotSettings #browser").val([]);
            $("#robotSettings #version").val("");
            $("#robotSettings #platform").val("");
            $("#robotSettings #screenSize").val("");
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
            loadSelect("SELENIUMLOG", "SeleniumLog", false, ""),
            loadSelect("MANUALEXECUTION", "manualExecution", false, ""),
            loadSelect("PAGESOURCE", "PageSource", false, ""),
            loadSelect("SYNCHRONEOUS", "Synchroneous", false, ""),
            loadSelect("RETRIES", "retries", false, "")
            ).then(function () {
        applyExecPref(tag);
    });
}

function loadRobotForm(browser) {
    var doc = new Doc();
    return $.when(
            appendRobotList(),
            loadSelect("BROWSER", "browser"),
            $("#robotSettingsForm [name=platform]").append($('<option></option>').text(doc.getDocLabel("page_runtest", "default")).val("")),
//            loadSelect("PLATFORM", "platform"),
            $("#robotSettingsForm [name=screenSize]").append($('<option></option>').text(doc.getDocLabel("page_runtest", "default_full_screen")).val("")),
//            loadSelect("screensize", "screenSize")
            $("#robotSettingsForm [name='screenSize']").autocomplete({source: getInvariantArray("SCREENSIZE", false)})
            ).then(function () {
        applyRobotPref(browser);
    });
}

function applyExecPref(tag) {
    var pref = JSON.parse(localStorage.getItem("executionSettings"));

    if (pref !== null) {
        if (tag !== null) { // if tag defined from URL we take that value.
            $("#tag").val(tag);
        } else {
            $("#tag").val(pref.Tag);
        }
        $("#verbose").val(pref.Verbose);
        $("#screenshot").val(pref.Screenshot);
        $("#pageSource").val(pref.PageSource);
        $("#seleniumLog").val(pref.SeleniumLog);
        $("#synchroneous").val(pref.Synchroneous);
        $("#timeout").val(pref.timeout);
        $("#retries").val(pref.retries);
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
//            console.debug(browser);
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
            loadMultiSelect("ReadProject", "sEcho=1", "project", ["idProject"], "idProject"),
            loadMultiSelect("ReadApplication", "", "application", ["application"], "application"),
            loadMultiSelect("ReadUserPublic", "", "creator", ["login"], "login"),
            loadMultiSelect("ReadUserPublic", "", "implementer", ["login"], "login"),
            loadMultiSelect("ReadCampaign", "", "campaign", ["campaign"], "campaign"),
            loadMultiSelect("ReadBuildRevisionInvariant", "level=1&system=" + system, "targetSprint", ["versionName"], "versionName"),
            loadMultiSelect("ReadBuildRevisionInvariant", "level=2&system=" + system, "targetRev", ["versionName"], "versionName"),
            loadMultiSelect("ReadLabel", "system=" + system, "labelid", ["label"], "id"),
            loadInvariantMultiSelect("system", "SYSTEM"),
            loadInvariantMultiSelect("priority", "PRIORITY"),
            loadInvariantMultiSelect("group", "GROUP"),
            loadInvariantMultiSelect("status", "TCSTATUS")
            ).then(function () {
        hideLoader("#filtersPanelContainer");
    });
}


function loadTestCaseEssentialData(test, testcase, environment, country, tag, browser) {
    showLoader("#chooseTest");
    $.when(
            loadExecForm(tag),
            loadRobotForm(browser),
            loadHardDefinedSingleSelect("length", [{label: '50', value: 50}, {label: '100', value: 100}, {label: '>100', value: -1}], 0)
            )
            .then(function () {

                selectionManual(test, testcase, environment, country);
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
