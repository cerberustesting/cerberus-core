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

        var test = GetURLParameter("test");
        var testcase = GetURLParameter("testcase");
        var environment = GetURLParameter("environment");
        var country = GetURLParameter("country");
        var tag = GetURLParameter("tag");
        var browser = GetURLParameter("browser");

        displayHeaderLabel(doc);
        displayFooter(doc);
        bindToggleCollapse();

        appendCampaignList();
        appendCountryList(country);
        showLoader("#chooseTest");


        $.when(
                loadExecForm(tag),
                loadRobotForm(browser),
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
                loadInvariantMultiSelect("status", "TCSTATUS"),
                loadHardDefinedSingleSelect("length", [{ label: '50', value: 50}, {label: '100', value: 100}, {label: '>100', value: -1}], 0)
                )
            .then(function () {
                typeSelectHandler(test, testcase, environment, country);
        });

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

    });
});

function loadRequestContext() {
    var test = GetURLParameter("test");
    var testcase = GetURLParameter("testcase");
    $('#testCaseList').find('option[value="' + test + '-' + testcase + '"]').prop("selected", true);
}

function typeSelectHandler(test, testcase, environment, country) {
    var value = $("[name='typeSelect']:checked").val();
//    console.log(value);
    if (value === "filters") {

        $("#envSettingsAuto select").prop("disabled", false).val("");

        $("#countryList input.countrycb").each(function () {
            console.log($(this).attr("name"));
            if($(this).attr("name") == country){
                $(this).prop("disabled", false).prop("checked", true);
            }else {
                $(this).prop("disabled", false).prop("checked", false);
            }
        });

        $("#testCaseList").prop("disabled", false);
        $("input[name='envSettings']").prop("disabled", false);
        $("#envSettingsAuto select").empty();
        if(environment != undefined && environment != null){
            $("[name='environment']").append($('<option></option>').text(environment).val(environment));
            $("[name='environment']").val(environment);
        }else {
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
        data: "filter=true&" + $("#filters").serialize() + "&system=" + getUser().defaultSystem + testURL + testCaseURL + lengthURL,
        datatype: "json",
        async: true,
        success: function (data) {

            var testCaseList = $("#testCaseList");

            testCaseList.empty();
            if(data.contentTable.length > 0) {
                for (var i = 0; i < data.contentTable.length; i++) {

                    var text = data.contentTable[i].test + " - " + data.contentTable[i].testCase + " [" + data.contentTable[i].application + "]: " + data.contentTable[i].description;

                    testCaseList.append($("<option></option>")
                        .text(text)
                        .val(data.contentTable[i].test + "-" + data.contentTable[i].testCase)
                        .data("item", data.contentTable[i]));
                }
            }else{
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

            if (country === defCountry)
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

    if ($("#queue li").length === 0) {
        type = getAlertType("KO");
        message = "Execution queue is empty !";
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
        message = "More than 1 execution has been requested. It will be executed in batch mode so please, indicate a tag (to find it back).";
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

            if(selectName == "test"){
                var test = GetURLParameter("test");
                if(test != undefined && test != null && test != "") {
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
    var jqXHR = $.getJSON("ReadRobot");
    $.when(jqXHR).then(function (data) {
        var robotList = $("#robot");

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
    return $.when(
            appendRobotList(),
            loadSelect("BROWSER", "browser"),
            $("[name=Platform]").append($('<option></option>').text("Default").val("")),
            loadSelect("PLATFORM", "Platform"),
            $("[name=screenSize]").append($('<option></option>').text("Default (Client Full Screen)").val("")),
            loadSelect("screensize", "screenSize")
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
