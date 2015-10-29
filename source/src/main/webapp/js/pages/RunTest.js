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

        displayHeaderLabel(doc);
        displayFooter(doc);
        bindToggleCollapse();
        appendCountryList();
        loadMultiSelect("ReadTest", "sEcho=1", "#testFilter", "test", "test");
        loadMultiSelect("ReadProject", "sEcho=1", "#projectFilter", "idProject", "idProject");


        $("#loadbutton").click(loadTestCaseFromFilter);
        $("#addQueue").click(addToQueue);

        loadExecForm();
        loadRobotForm();
        displayEnvList("environment", getUser().defaultSystem);

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
        $("#robotConfig").change(loadRobotInfo);
    });
});

function deleteRow() {
    $(this).parent('li').remove();
}

function addToQueue() {
    var select = $("#testCaseList option:selected");

    select.each(function () {
        var queue = $("#queue");
        var removeBtn = $("<span></span>").addClass("glyphicon glyphicon-remove delete").click(deleteRow);
        var item = $(this).data("item");

        queue.append($('<li></li>').addClass("list-group-item").text(item.test + " - " + item.testCase).prepend(removeBtn).data("item", item));

        $(this).remove();
    });
}

function loadTestCaseFromFilter() {
    console.log($("#filters").serialize());

    $.ajax({
        url: "ReadTestCase",
        method: "GET",
        data: "filter=true&" + $("#filters").serialize(),
        datatype: "json",
        async: true,
        success: function (data) {
            var testCaseList = $("#testCaseList");

            testCaseList.empty();

            for (var index = 0; index < data.contentTable.length; index++) {
                var text = data.contentTable[index].test + " - " + data.contentTable[index].testCase + " [" + data.contentTable[index].application + "]: " + data.contentTable[index].shortDescription;

                testCaseList.append($("<option></option>").text(text).val(data.contentTable[index].testCase).data("item", data.contentTable[index]));
            }
        }
    });
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
    var pref = JSON.parse(localStorage.getItem("executionSettings"));

    if (pref !== null) {
        $("#tag").val(pref.tag);
        $("#timeout").val(pref.timeout);
    }

    loadSelect("OUTPUTFORMAT", "outputFormat", pref);
    loadSelect("VERBOSE", "verbose", pref);
    loadSelect("SCREENSHOT", "screenshot", pref);
    loadSelect("SELENIUMLOG", "seleniumLog", pref);
    loadSelect("MANUALEXECUTION", "manualExecution", pref);
    loadSelect("PAGESOURCE", "pageSource", pref);
    loadSelect("SYNCHRONEOUS", "synchroneous", pref);
    loadSelect("RETRIES", "retries", pref);
}

function loadRobotForm() {
    var pref = JSON.parse(localStorage.getItem("robotSettings"));

    if (pref !== null) {
        $("#seleniumIP").val(pref.seleniumIP);
        $("#seleniumPort").val(pref.seleniumPort);
        $("#version").val(pref.version);
    }

    appendRobotList(pref);
    loadSelect("BROWSER", "browser", pref);
    $("[name=platform]").append($('<option></option>').text("Optional").val(""));
    loadSelect("PLATFORM", "platform", pref);
    $("[name=screenSize]").append($('<option></option>').text("Default (Client Full Screen)").val(""));
    loadSelect("screensize", "screenSize", pref);

}

function loadRobotInfo() {
    var value = "";
    var pref = JSON.parse(localStorage.getItem("robotSettings"));

    if (this.value !== undefined) {
        value = $(this).val();
    } else if (pref !== null) {
        value = pref.robotConfig;
    }

    if (value !== "") {
        $.ajax({
            url: "ReadRobot",
            method: "GET",
            data: {robotid: value},
            dataType: "json",
            async: true,
            success: function (data) {
                $("#seleniumIP").val(data.contentTable.host).attr("readonly", true);
                $("#seleniumPort").val(data.contentTable.port).attr("readonly", true);
                $("#browser").val(data.contentTable.browser).attr("readonly", true);
                $("#version").val(data.contentTable.version).attr("readonly", true);
                $("#platform").val(data.contentTable.platform).attr("readonly", true);
                $("#screenSize").val("").attr("readonly", true);
            }
        });
    } else {
        if (pref !== null) {
            for (var key in pref) {
                if (key !== "robotConfig") {
                    $("#" + key).attr("readonly", false).val(pref[key]);
                }
            }
        } else {
            $("#seleniumIP").attr("readonly", false).val("");
            $("#seleniumPort").attr("readonly", false).val("");
            $("#browser").attr("readonly", false).val("");
            $("#version").attr("readonly", false).val("");
            $("#platform").attr("readonly", false).val("");
            $("#screenSize").attr("readonly", false).val("");
        }
    }
}

function loadSelect(idName, selectName, pref) {
    $.ajax({
        url: "FindInvariantByID",
        method: "GET",
        data: {idName: idName},
        dataType: "json",
        async: true,
        success: function (data) {
            for (var option in data) {
                $("[name='" + selectName + "']").append($('<option></option>').text(data[option].value + " - " + data[option].description).val(data[option].value));
            }
            if (pref !== null) {
                $("[name='" + selectName + "']").val(pref[selectName]);
            }
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

function loadMultiSelect(url, urlParams, selectID, textItem, valueItem) {
    $.ajax({
        url: url,
        method: "GET",
        data: urlParams,
        dataType: "json",
        async: true,
        success: function (data) {
            var select = $(selectID);

            for (var index = 0; index < data.contentTable.length; index++) {
                var text = data.contentTable[text];

                select.append($("<option></option>").text(data.contentTable[index][textItem])
                        .val(data.contentTable[index][valueItem])
                        .data("item", data.contentTable[index]));
            }

            select.multiselect(new multiSelectConf(valueItem));
        },
        error: function (e) {
            showUnexpectedError();
        }
    });
}

function loadTestList() {
    $.ajax({
        url: "ReadTest",
        method: "GET",
        data: {sEcho: 1},
        dataType: "json",
        async: true,
        success: function (data) {
            var testList = $("#testFilter");

            for (var index = 0; index < data.contentTable.length; index++) {
                testList.append($("<option></option>").text(data.contentTable[index].test + " - " + data.contentTable[index].description).val(data.contentTable[index].test));
            }

            testList.multiselect({
                maxHeight: 150,
                checkboxName: 'test',
                buttonWidth: '100%',
                enableFiltering: true,
                enableCaseInsensitiveFiltering: true
            });
        }
    });
}

function appendCountryList() {
    var jqxhr = $.getJSON("FindInvariantByID", "idName=COUNTRY");
    $.when(jqxhr).then(function (data) {
        var countryList = $("[name=countryList]");

        for (var index = 0; index < data.length; index++) {
            var country = data[index].value;

            countryList.append('<label class="checkbox-inline"><input class="countrycb" type="checkbox" name="' + country + '"/>' + country + '\
                                <input class="countrycb-hidden" type="hidden" name="' + country + '" value="off"/></label>');
        }
    });
}

function appendRobotList(pref) {
    var jqxhr = $.getJSON("ReadRobot");
    $.when(jqxhr).then(function (data) {
        var robotList = $("#robotConfig");

        robotList.append($('<option></option>').text("Custom configuration").val(""));
        for (var index = 0; index < data.contentTable.length; index++) {
            robotList.append($('<option></option>').text(data.contentTable[index].robot).val(data.contentTable[index].robotID));
        }

        if (pref !== null) {
            robotList.val(pref["robotConfig"]);
            loadRobotInfo();
        }
    });
}