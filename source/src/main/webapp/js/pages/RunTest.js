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
$.when($.getScript("js/pages/global/global.js")).then(function () {
    $(document).ready(function () {
        var doc = new Doc();

        displayHeaderLabel(doc);
        displayFooter(doc);
        bindToggleCollapseCustom();
        displayPageLabel(doc);

        appendCampaignList();
//        $("#campaignSelect").select2();
        
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
            loadTestCaseFilterData(system, tag, browser);
            filterPanelDataLoaded = true;
            updateUserPreferences();
        }
        //add a listenner to load the data when needed
        $("#FilterPanelHeader").click(function () {
            if (!filterPanelDataLoaded) {
                loadTestCaseFilterData(system, tag, browser);
                filterPanelDataLoaded = true
                updateUserPreferences();
            }
        });
        //load the data that need to be display in any case
        loadTestCaseEssentialData(test, testcase, environment, country);

        var system = getUser().defaultSystem;

        $("[name='typeSelect']").on("change", typeSelectHandler);

        $("#run").click(sendForm);

        $("#loadFiltersBtn").click(function () {
            loadTestCaseFromFilter(null, null);
        });

        $("#loadCampaignBtn").click(function () {
            var campaign = $("#campaignSelect").val();
            loadCampaignContent(campaign);
            loadCampaignParameter(campaign);
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

        $("#selectAll").click(function () {
            $("#testCaseList option").prop("selected", "selected");
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
        $("#robot").change(function () {
            loadRobotInfo($(this).val());
        });

        $("#testCaseList").on("change", updatePotentialNumber);
        $("#envSettingsAuto select").on("change", updatePotentialNumber);

        $("#countrySelectAll").on("click", function () {
            $("#countryList input").prop('checked', true);
            updatePotentialNumber();
        });
        $("#countryUnselectAll").on("click", function () {
            $("#countryList input").prop('checked', false);
            updatePotentialNumber();
        });

    });
});

function loadRequestContext() {
    var test = GetURLParameter("test");
    var testcase = GetURLParameter("testcase");
    $('#testCaseList').find('option[value="' + test + '-' + testcase + '"]').prop("selected", true);
}

function displayPageLabel() {
    var doc = new Doc();
    $("h1.page-title-line").text(doc.getDocLabel("page_runtest", "title"));
    $("#environmentPanel div.panel-heading").text(doc.getDocLabel("page_runtest", "selection_type"));
    $("#environmentPanel input[value='filters']").next().text(doc.getDocLabel("page_runtest", "select_list_test"));
    $("#environmentPanel input[value='campaign']").next().text(doc.getDocLabel("page_runtest", "select_campaign"));
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
    $("#lbl_testBattery").text(doc.getDocLabel("page_runtest", "testbattery"));
    $("#lbl_priority").text(doc.getDocLabel("page_runtest", "priority"));
    $("#lbl_status").text(doc.getDocLabel("page_runtest", "status"));
    $("#lbl_targetRev").text(doc.getDocLabel("page_runtest", "targetrev"));
    $("#lbl_targetSprint").text(doc.getDocLabel("page_runtest", "targetsprint"));
    $("#lbl_size").text(doc.getDocLabel("page_runtest", "size"));
    $("#selectAll").text(doc.getDocLabel("page_runtest", "select_all"));
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
    $("#executionPanel label[for='outputFormat']").text(doc.getDocLabel("page_runtest", "outputformat"));
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

function typeSelectHandler(test, testcase, environment, country) {
    var value = $("[name='typeSelect']:checked").val();
//    console.log(value);
    if (value === "filters") {

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
        $("#campaignSelection").hide();
        $("#filters").show();
        $("#resetbutton").show();
        $("#filtersPanelContainer").show();

        loadTestCaseFromFilter(test, testcase);

    } else if (value === "campaign") {
        $("#filtersPanelContainer").hide();
        $("#campaignSelection").show();
        $("#testCaseList").empty();
        $("#envSettingsAuto select").empty();
        displayUniqueEnvList("environment", "");
    }
    updatePotentialNumber();
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
                updatePotentialNumber();
                hideLoader("#chooseTest");
            }
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
        showMessageMainPage(type, message);
        return false;
// Browser is not mandatory when application is not GUI.        
//    } else if ($("#browser").val() === null) {
//        type = getAlertType("KO");
//        message = "Please, select at least one browser.";
//        showMessageMainPage(type, message);
//        return false;
    } else if ($("#queue li").length > 1 && $("#tag").val() === "") { // More than 1 excution and no Tag specified.
        type = getAlertType("KO");
        message = doc.getDocLabel("page_runtest", "more_than_one_execution_requested"); //More than 1 execution has been requested. It will be executed in batch mode so please, indicate a tag (to find it back)
        showMessageMainPage(type, message);
        return false;
    }
    return true;
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
        var browsers = $("#browser").val() ? $("#browser").val() : [];
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
                data.ManualHost = $("#myhost").val();
                data.ManualContextRoot = $("#mycontextroot").val();
                data.ManualLoginRelativeURL = $("#myloginrelativeurl").val();
                data.ManualEnvData = $("#myenvdata").val();
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
    var browser = $("#browser").val() ? $("#browser").val() : [""];
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
    $("#outputformatATQ").val($("#outputFormat").val());
    $("#screenshotATQ").val($("#screenshot").val());
    $("#verboseATQ").val($("#verbose").val());
    $("#timeoutATQ").val($("#timeout").val());
    $("#synchroneousATQ").val($("#synchroneous").val());
    $("#pageSourceATQ").val($("#pageSource").val());
    $("#seleniumLogATQ").val($("#seleniumLog").val());
    $("#manualExecutionATQ").val($("#manualExecution").val());
    $("#retriesATQ").val($("#retries").val());
    $("#screenSizeATQ").val($("#screenSize").val());
    $("#manualRobotATQ").val($("#robot").val());
    $("#ss_ipATQ").val($("#seleniumIP").val());
    $("#ss_pATQ").val($("#seleniumPort").val());
    $("#versionATQ").val($("#version").val());
    $("#platformATQ").val($("#platform").val());
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
    var browser = $("#browser").val() ? $("#browser").val() : [];
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

    $("#error").empty();
    if (testcase.length === 0) {
        $("#error").text(doc.getDocLabel("page_runtest", "select_one_testcase")); //!!! Please, select at least 1 Test Case !!!
    } else if (environment.length === 0) {
        $("#error").text(doc.getDocLabel("page_runtest", "select_one_env")); //"!!! Please, select at least 1 Environment !!!");
    } else if (countries.length === 0) {
        $("#error").text(doc.getDocLabel("page_runtest", "select_one_country")); //"!!! Please, select at least 1 Country !!!");
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
            campaignList.append($('<option></option>').text(data.contentTable[index].campaign + " - " + data.contentTable[index].description)
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

function loadSelect(idName, selectName, forceReload) {

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

//            $("[name='" + selectName + "']").append($('<option></option>').text(item).val(item));
            $("[name='" + selectName + "']").append($('<option></option>').text(item + " - " + desc).val(item));
        }
    }

//    var jqXHR = $.ajax({
//        url: "FindInvariantByID",
//        method: "GET",
//        data: {idName: idName},
//        dataType: "json",
//        async: true,
//        success: function (data) {
//            for (var option in data) {
//                $("[name='" + selectName + "']").append($('<option></option>').text(data[option].value + " - " + data[option].description).val(data[option].value));
//            }
//        }
//    });
//
//    return jqXHR;
}

function appendRobotList() {
    var doc = new Doc();
    var jqXHR = $.getJSON("ReadRobot");
    $.when(jqXHR).then(function (data) {
        var robotList = $("#robot");

        robotList.append($('<option></option>').text(doc.getDocLabel("page_runtest", "custom_config")).val(""));
        for (var index = 0; index < data.contentTable.length; index++) {
            robotList.append($('<option></option>').text(data.contentTable[index].robot).val(data.contentTable[index].robot));
        }
    });

    return jqXHR;
}

function loadRobotInfo(robot) {

    if (robot !== "") {
        $.ajax({
            url: "ReadRobot",
            method: "GET",
            data: {robot: robot},
            dataType: "json",
            async: true,
            success: function (data) {
                disableRobotFields();
                $("#seleniumIP").val(data.contentTable.host);
                $("#seleniumPort").val(data.contentTable.port);
                $("#browser").val(data.contentTable.browser);
                $("#version").val(data.contentTable.version);
                $("#platform").val(data.contentTable.platform);
                $("#screenSize").val(data.contentTable.screenSize);
            }
        });
    } else {
        var pref = JSON.parse(localStorage.getItem("robotSettings"));
        enableRobotFields();
        if (pref !== null && pref.robot === "") {
            $("#robot").val(pref.robot);
            $("#seleniumIP").val(pref.ss_ip);
            $("#seleniumPort").val(pref.ss_p);
            $("#browser").val(pref.browser);
            $("#version").val(pref.BrowserVersion);
            $("#platform").val(pref.Platform);
            $("#screenSize").val(pref.screenSize);
        } else {
            $("#seleniumIP").val("");
            $("#seleniumPort").val("");
            $("#browser").val([]);
            $("#version").val("");
            $("#platform").val("");
            $("#screenSize").val("");
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
//    displayInvariantList("group", "GROUP", false);
            loadSelect("OUTPUTFORMAT", "outputformat"),
            loadSelect("VERBOSE", "Verbose"),
            loadSelect("SCREENSHOT", "Screenshot"),
            loadSelect("SELENIUMLOG", "SeleniumLog"),
            loadSelect("MANUALEXECUTION", "manualExecution"),
            loadSelect("PAGESOURCE", "PageSource"),
            loadSelect("SYNCHRONEOUS", "Synchroneous"),
            loadSelect("RETRIES", "retries")
            ).then(function () {
        applyExecPref(tag);
    });
}

function loadRobotForm(browser) {
    var doc = new Doc();
    return $.when(
            appendRobotList(),
            loadSelect("BROWSER", "browser"),
            $("[name=Platform]").append($('<option></option>').text(doc.getDocLabel("page_runtest", "default")).val("")),
            loadSelect("PLATFORM", "Platform"),
            $("[name=screenSize]").append($('<option></option>').text(doc.getDocLabel("page_runtest", "default_full_screen")).val("")),
//            loadSelect("screensize", "screenSize")
            $("[name='screenSize']").autocomplete({source: getInvariantArray("SCREENSIZE", false)})
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
        $("#outputFormat").val(pref.outputformat);
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
        if (pref.robot === "") {
            enableRobotFields();
            $("#robot").val(pref.robot);
            $("#seleniumIP").val(pref.ss_ip);
            $("#seleniumPort").val(pref.ss_p);
//            console.debug(browser);
            if (browser !== null) { // if browser defined from URL we take that value.
                $("#browser").val(browser);
            } else {
                $("#browser").val(pref.browser);
            }
            $("#version").val(pref.BrowserVersion);
            $("#platform").val(pref.Platform);
            $("#screenSize").val(pref.screenSize);
        } else {
            $("#robot").val(pref.robot);
            loadRobotInfo(pref.robot);
        }
    }
}

function disableRobotFields() {
    $("#seleniumIP").prop("readonly", true);
    $("#seleniumPort").prop("readonly", true);
    $("#browser").prop("disabled", "disabled");
    $("#version").prop("readonly", true);
    $("#platform").prop("disabled", "disabled");
    $("#screenSize").prop("disabled", "disabled");
}

function enableRobotFields() {
    $("#seleniumIP").prop("readonly", false);
    $("#seleniumPort").prop("readonly", false);
    $("#browser").prop("disabled", false);
    $("#version").prop("readonly", false);
    $("#platform").prop("disabled", false);
    $("#screenSize").prop("disabled", false);
}

//Load the data to put in Extended Test Case Filters panel
function loadTestCaseFilterData(system, tag, browser) {
    showLoader("#filtersPanelContainer");
    $.when(
            loadExecForm(tag),
            loadRobotForm(browser),
            loadMultiSelect("ReadTest", "", "test", ["test", "description"], "test"),
            loadMultiSelect("ReadProject", "sEcho=1", "project", ["idProject"], "idProject"),
            loadMultiSelect("ReadApplication", "", "application", ["application"], "application"),
            loadMultiSelect("ReadUserPublic", "", "creator", ["login"], "login"),
            loadMultiSelect("ReadUserPublic", "", "implementer", ["login"], "login"),
            loadMultiSelect("ReadTestBattery", "", "testBattery", ["testbattery"], "testbattery"),
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


function loadTestCaseEssentialData(test, testcase, environment, country) {
    showLoader("#chooseTest");
    $.when(
            loadHardDefinedSingleSelect("length", [{label: '50', value: 50}, {label: '100', value: 100}, {label: '>100', value: -1}], 0)
            )
            .then(function () {
                typeSelectHandler(test, testcase, environment, country);
            });

}
//Remove the call to updateUserPreferences when no new data are loaded by the filter
function bindToggleCollapseCustom() {
    $(".collapse").each(function () {
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
    });
}