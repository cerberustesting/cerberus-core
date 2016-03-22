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
        var system = getUser().defaultSystem;

        oldPreferenceCompatibility();

        displayHeaderLabel(doc);
        displayFooter(doc);
        bindToggleCollapse();
        appendCampaignList();
        appendCountryList();
        typeSelectHandler();
        showLoader("#filtersPanel");
        $.when(
                loadMultiSelect("ReadTest", "system=" + system, "test", ["test", "description"], "test"),
                loadMultiSelect("ReadProject", "sEcho=1", "project", ["idProject"], "idProject"),
                loadMultiSelect("ReadApplication", "system=" + system, "application", ["application"], "application"),
                loadMultiSelect("ReadUserPublic", "", "creator", ["login"], "login"),
                loadMultiSelect("ReadUserPublic", "", "implementer", ["login"], "login"),
                loadMultiSelect("ReadTestBattery", "", "testBattery", ["testbattery"], "testbattery"),
                loadMultiSelect("ReadCampaign", "", "campaign", ["campaign"], "campaign"),
                loadMultiSelect("ReadBuildRevisionInvariant", "level=1&system=" + system, "targetSprint", ["versionName"], "versionName"),
                loadMultiSelect("ReadBuildRevisionInvariant", "level=2&system=" + system, "targetRev", ["versionName"], "versionName"),
                loadInvariantMultiSelect("priority", "PRIORITY"),
                loadInvariantMultiSelect("group", "GROUP"),
                loadInvariantMultiSelect("status", "TCSTATUS")
                ).then(function () {
            hideLoader("#filtersPanel");
            if ($("#typeSelect").val() === "filters") {
                loadTestCaseFromFilter();
            }
        });

        $("[name='typeSelect']").on("change", typeSelectHandler);

        $("#run").click(sendForm);

        $("#loadFiltersBtn").click(loadTestCaseFromFilter);

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

        $("#addQueue").click(checkExecution);

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

        loadExecForm();
        loadRobotForm();

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
        $("#robotConfig").change(function () {
            loadRobotInfo($(this).val());
        });

        $("#testCaseList").on("change", updatePotentialNumber);
        $("#envSettingsAuto select").on("change", updatePotentialNumber);

    });
});

function loadRequestContext() {
    var test = GetURLParameter("test");
    var testcase = GetURLParameter("testcase");
    $('#testCaseList').find('option[value="' + test + '-' + testcase + '"]').prop("selected", true);
}

function typeSelectHandler() {
    var value = $("[name='typeSelect']:checked").val();
    console.log(value);
    if (value === "filters") {

        $("#envSettingsAuto select").prop("disabled", false).val("");

        $("#countryList input.countrycb").each(function () {
            $(this).prop("disabled", false).prop("checked", false);
        });

        $("#testCaseList").prop("disabled", false);
        $("#envSettingsAuto select").empty();
        displayUniqueEnvList("environment", getUser().defaultSystem);
        $("#campaignSelection").hide();
        $("#filters").show();
        $("#resetbutton").show();
        $("#filtersPanelContainer").show();
        loadTestCaseFromFilter();
    } else if (value === "campaign") {
        $("#filtersPanelContainer").hide();
        $("#campaignSelection").show();
        $("#testCaseList").empty();
        $("#envSettingsAuto select").empty();
        displayUniqueEnvList("environment", "");
    }
    updatePotentialNumber();
}

function loadTestCaseFromFilter() {
    showLoader("#chooseTest");
    $.ajax({
        url: "ReadTestCase",
        method: "GET",
        data: "filter=true&" + $("#filters").serialize() + "&system=" + getUser().defaultSystem,
        datatype: "json",
        async: true,
        success: function (data) {
            var testCaseList = $("#testCaseList");

            testCaseList.empty();

            for (var index = 0; index < data.contentTable.length; index++) {
                var text = data.contentTable[index].test + " - " + data.contentTable[index].testCase + " [" + data.contentTable[index].application + "]: " + data.contentTable[index].shortDescription;

                testCaseList.append($("<option></option>")
                        .text(text)
                        .val(data.contentTable[index].test + "-" + data.contentTable[index].testCase)
                        .data("item", data.contentTable[index]));
            }
            hideLoader("#chooseTest");
            loadRequestContext();
        }
    });
}

function appendCountryList() {
    var jqxhr = $.getJSON("FindInvariantByID", "idName=COUNTRY");
    $.when(jqxhr).then(function (data) {
        var countryList = $("[name=countryList]");

        for (var index = 0; index < data.length; index++) {
            var country = data[index].value;

            countryList.append('<label class="checkbox-inline">\n\
                                <input class="countrycb" type="checkbox" name="' + country + '"/>' + country + '\
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
                    var text = data.contentTable[index].test + " - " + data.contentTable[index].testCase + " [" + data.contentTable[index].application + "]: " + data.contentTable[index].shortDescription;

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

    if ($("#queue li").length === 0) {
        type = getAlertType("KO");
        message = "Execution queue is empty !";
        showMessageMainPage(type, message);
        return false;
    } else if ($("#browser").val() === null) {
        type = getAlertType("KO");
        message = "Please, select at least one browser.";
        showMessageMainPage(type, message);
        return false;
    } else if ($("#queue li").length > 1 && $("#tag").val() === "") { // More than 1 excution and no Tag specified.
        type = getAlertType("KO");
        message = "More than 1 execution has been requested. It will be executed in batch mode so please, indicate a tag (to find it back).";
        showMessageMainPage(type, message);
        return false;
    }
    return true;
}

function sendForm() {

    if ($("#queue li").length > 1) { // We have more than 1 execution in the queue.
        if ($("#tag").val() === "") { // We force the Tag is not defined.
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
            setData(executionArray);
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

function setData(executionArray) {
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
    $("#manualRobotATQ").val($("#robotConfig").val());
    $("#ss_ipATQ").val($("#seleniumIP").val());
    $("#ss_pATQ").val($("#seleniumPort").val());
    $("#versionATQ").val($("#browserVersion").val());
    $("#platformATQ").val($("#platform").val());
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

function checkExecution() {
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
        $("#error").text("!!! Please, select at least 1 Test Case !!!");
    } else if (environment.length === 0) {
        $("#error").text("!!! Please, select at least 1 Environment !!!");
    } else if (countries.length === 0) {
        $("#error").text("!!! Please, select at least 1 Country !!!");
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

/** FUNCTIONS TO HANDLE ROBOT/EXECUTION PREFERENCES **/

function loadSelect(idName, selectName) {
    var jqXHR = $.ajax({
        url: "FindInvariantByID",
        method: "GET",
        data: {idName: idName},
        dataType: "json",
        async: true,
        success: function (data) {
            for (var option in data) {
                $("[name='" + selectName + "']").append($('<option></option>').text(data[option].value + " - " + data[option].description).val(data[option].value));
            }
        }
    });

    return jqXHR;
}

function appendRobotList() {
    var jqXHR = $.getJSON("ReadRobot");
    $.when(jqXHR).then(function (data) {
        var robotList = $("#robotConfig");

        robotList.append($('<option></option>').text("-- Custom configuration --").val(""));
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
                $("#screenSize").val("");
            }
        });
    } else {
        var pref = JSON.parse(localStorage.getItem("robotSettings"));
        enableRobotFields();
        if (pref !== null && pref.robotConfig === "") {
            $("#robotConfig").val(pref.robotConfig);
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

function loadExecForm() {
    $.when(
            loadSelect("OUTPUTFORMAT", "OutputFormat"),
            loadSelect("VERBOSE", "Verbose"),
            loadSelect("SCREENSHOT", "Screenshot"),
            loadSelect("SELENIUMLOG", "SeleniumLog"),
            loadSelect("MANUALEXECUTION", "manualExecution"),
            loadSelect("PAGESOURCE", "PageSource"),
            loadSelect("SYNCHRONEOUS", "Synchroneous"),
            loadSelect("RETRIES", "retries")
            ).then(function () {
        applyExecPref();
    });
}

function loadRobotForm() {
    $.when(
            appendRobotList(),
            loadSelect("BROWSER", "browser"),
            $("[name=Platform]").append($('<option></option>').text("Default").val("")),
            loadSelect("PLATFORM", "Platform"),
            $("[name=screenSize]").append($('<option></option>').text("Default (Client Full Screen)").val("")),
            loadSelect("screensize", "screenSize")
            ).then(function () {
        applyRobotPref();
    });
}

function applyExecPref() {
    var pref = JSON.parse(localStorage.getItem("executionSettings"));

    if (pref !== null) {
        $("#tag").val(pref.tag);
        $("#outputFormat").val(pref.outputFormat);
        $("#verbose").val(pref.verbose);
        $("#screenshot").val(pref.screenshot);
        $("#pageSource").val(pref.pageSource);
        $("#seleniumLog").val(pref.seleniumLog);
        $("#synchroneous").val(pref.synchroneous);
        $("#timeout").val(pref.timeout);
        $("#retries").val(pref.retries);
        $("#manualExecution").val(pref.manualExecution);
    }
}

function applyRobotPref() {
    var pref = JSON.parse(localStorage.getItem("robotSettings"));

    if (pref !== null) {
        if (pref.robotConfig === "") {
            enableRobotFields();
            $("#robotConfig").val(pref.robotConfig);
            $("#seleniumIP").val(pref.ss_ip);
            $("#seleniumPort").val(pref.ss_p);
            $("#browser").val(pref.browser);
            $("#version").val(pref.BrowserVersion);
            $("#platform").val(pref.Platform);
            $("#screenSize").val(pref.screenSize);
        } else {
            $("#robotConfig").val(pref.robotConfig);
            loadRobotInfo(pref.robotConfig);
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

/** UTILITY FUNCTIONS TO MIGRATE THE PREFERENCES !!!TO DELETE AFTER THE PAGE IS LIVE FOR A FEW DAYS!!!  **/

function getCookie(cname) {
    var name = cname + "=";
    var ca = document.cookie.split(';');
    for (var i = 0; i < ca.length; i++) {
        var c = ca[i].trim();
        if (c.indexOf(name) === 0)
            var value = c.substring(name.length, c.length);

        document.cookie = cname + "=; expires=Thu, 01 Jan 1970 00:00:00 UTC";
        return value;
    }
    return "";
}

function oldPreferenceCompatibility() {
    if (localStorage.getItem("robotSettings") === null
            && localStorage.getItem("executionSettings") === null) {

        var user = getUser();

        var robotConfig = {
            robotConfig: user.robot,
            seleniumIP: user.robotHost,
            seleniumPort: user.robotPort,
            version: user.robotVersion,
            platform: user.robotPlatform,
            screenSize: getCookie("ExecutionScreenSize")
        };

        var execConfig = {
            tag: getCookie("TagPreference"),
            outputFormt: getCookie("OutputFormatPreference"),
            verbose: getCookie("VerbosePreference"),
            screenshot: getCookie("ScreenshotPreference"),
            pageSource: getCookie("PageSourcePreference"),
            seleniumLog: getCookie("SeleniumLogPreference"),
            synchroneous: getCookie("SynchroneousPreference"),
            timeout: getCookie("TimeoutPreference"),
            retries: getCookie("ExecutionRetries"),
            manualExecution: getCookie("ManualExecutionPreference")
        };

        localStorage.setItem("executionSettings", JSON.stringify(execConfig));
        localStorage.setItem("robotSettings", JSON.stringify(robotConfig));
    }
}