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
var showBurger = false;
var showSettings = false;

(function () {
    $("#burger").unbind("click").click(function () {
        if (showBurger === false) {
            $("#side-menu li").show();
            showBurger = true;
        } else {
            $("#side-menu li:not(.MainItem)").hide();
            showBurger = false;
        }

    });

    $("#burger-setting").unbind("click").click(function () {
        if (showSettings === false) {
            $(".nav.navbar-top-links.navbar-right").show();
            $(".navbar-header").show();
            showSettings = true;
        } else {
            $(".nav.navbar-top-links.navbar-right").hide();
            $(".navbar-header").hide();
            showSettings = false;
        }
    });
})();

function handleErrorAjaxAfterTimeout(result) {
    const doc = new Doc();

    if (result.readyState === 4 && result.status === 200) {
        $(location).prop("pathname", $(location).prop("pathname"));
        $(location).prop("search", $(location).prop("search"));
    } else {
        const localMessage = new Message("danger", doc.getDocLabel("page_global", "unexpected_error_message"));
        showMessageMainPage(localMessage);
    }
}

/***
 * Returns a label depending on the type of entry
 * @param {type} type - type selected
 * @returns {String} - label associated with the type
 */
function getSubDataLabel(type) {
    const doc = getDoc();
    const docTestdatalibdata = doc.testdatalibdata;
    let labelEntry = "Entry";
    if (type === "INTERNAL") {
        labelEntry = displayDocLink(docTestdatalibdata.value);
    } else if (type === "SQL") {
        labelEntry = displayDocLink(docTestdatalibdata.column);
    } else if (type === "SOAP") {
        labelEntry = displayDocLink(docTestdatalibdata.parsingAnswer);
    }
    return labelEntry;
}

/*********************************************** COMBO DISPLAY ***************************************/

/**
 * Method that display a combo box in all the selectName tags with the value retrieved from the invariant list
 * @param {String} selectName value name of the select tag in the html
 * @param {String} idName value that filters the invariants that will be retrieved (ex : "SYSTEM", "COUNTRY", ...)
 * @param {String} forceReload true in order to force the reload of list from database.
 * @param {String} defaultValue [optional] value to be selected in combo.
 * @param {String} addValue1 [optional] Adds a value on top of the normal List.
 * @param {String} asyn [optional] Do a async ajax request. Default: true
 * @param {String} funcAfterLoad [optional] Function to call after load.
 * @param {String} extra modal id in order to filter name selcted with .selectName parameter
 * @returns {void}
 */
function displayInvariantList(selectName, idName, forceReload, defaultValue, addValue1, asyn, funcAfterLoad, modalID) {
    let selector = "[name='" + selectName + "']";
    if (modalID !== undefined) {
        selector = "#" + modalID + " [name='" + selectName + "']";
    }
    // Adding the specific value when defined.
    if (addValue1 !== undefined) {
        $(selector).append($('<option></option>').text(addValue1).val(addValue1));
    }

    if (forceReload === undefined) {
        forceReload = true;
    }

    var async = true;
    if (asyn !== undefined) {
        async = asyn;
    }

    const cacheEntryName = "INVARIANT_" + idName;
    if (forceReload) {
        sessionStorage.removeItem(cacheEntryName);
    }
    let list = JSON.parse(sessionStorage.getItem(cacheEntryName));
    //var select = $("<select></select>").addClass("form-control input-sm");

    if (list === null) {
        $.ajax({
            url: "FindInvariantByID",
            data: {idName: idName},
            async: async,
            success: function (data) {
                list = data;
                sessionStorage.setItem(cacheEntryName, JSON.stringify(data));
                for (const element of list) {
                    let desc = element.value;
                    if (!isEmpty(element.description))
                        desc += " - " + element.description;

                    $(selector).append($('<option></option>').text(desc).val(element.value));
                }
                if (defaultValue !== undefined) {
                    $(selector).val(defaultValue);
                }
                if (funcAfterLoad !== undefined) {
                    funcAfterLoad();
                }
            }
        });
    } else {
        for (const element of list) {
            const desc = element.value + " - " + element.description;
            $(selector).append($('<option></option>').text(desc).val(element.value));
        }
        if (defaultValue !== undefined) {
            $(selector).val(defaultValue);
        }
        if (funcAfterLoad !== undefined) {
            funcAfterLoad();
        }
    }
}

/**
 * Method that display a combo box in all the selectName tags with the value retrieved from the invariant list
 * @param {String} selectName value name of the select tag in the html
 * @param {String} idName value that filters the invariants that will be retrieved (ex : "SYSTEM", "COUNTRY", ...)
 * @param {String} forceReload true in order to force the reload of list from database.
 * @param {String} defaultValue [optional] value to be selected in combo.
 * @param {String} addValue1 [optional] Adds a value on top of the normal List.
 * @param {String} asyn [optional] Do a async ajax request. Default: true
 * @param {String} funcAfterLoad [optional] Function to call after load.
 * @returns {void}
 */
function displayListFromData(selectName, data, defaultValue) {
    // Adding the specific value when defined.
    let list = data;
    //var select = $("<select></select>").addClass("form-control input-sm");

    for (const element of list) {
//            const desc = element + " - " + element;
        $("[name='" + selectName + "']").append($('<option></option>').text(element).val(element));
    }
    if (defaultValue !== undefined) {
        $("[name='" + selectName + "']").val(defaultValue);
    }
}



/**
 * Method that display a list-group-item with the value retrieved from the Application IP list
 * @param {String} selectName value name of the list-group in the html to append the items
 * @param {String} system value to filter.
 * @param {String} application value to filter.
 * @returns {void}
 */
function displayRobotList(selectName, idName, forceReload, defaultValue) {

    if (forceReload === undefined) {
        forceReload = true;
    }

    const cacheEntryName = "ROBOTLIST";
    if (forceReload) {
        sessionStorage.removeItem(cacheEntryName);
    }
    let list = JSON.parse(sessionStorage.getItem(cacheEntryName));

    if (list === null) {
        $.ajax({
            url: "ReadRobot",
            async: false,
            success: function (data) {
                list = data.contentTable;
                sessionStorage.setItem(cacheEntryName, JSON.stringify(data));
                for (var index = 0; index < list.length; index++) {
                    let browserImg = "<img class='col-xs-2' style='width:60px;height:30px' src='images/browser-" + list[index].browser + ".png'/>";
                    if (list[index].browser == "") {
                        browserImg = "";
                    }
                    let isActive = "";
                    if (list[index].robot === defaultValue) {
                        isActive = " active";
                    }
                    var line = $("<button type='button' data-robot='" + list[index].robot + "' class='list-group-item list-group-item-action" + isActive + "' name='robotItem'>" +
                            "<span class='col-xs-6 grayscale'>" + list[index].robot + "</span>" +
                            "<img class='col-xs-2' style='width:60px;height:30px' src='images/platform-" + list[index].platform + ".png'/>" +
                            browserImg +
                            "<span class='col-xs-2 grayscale'> " + list[index].version + " </span>" +
                            "</button>");
                    line.data("item", list[index]);
                    $("[name='" + selectName + "']").append(line);

                }
                if (defaultValue !== undefined) {
                    $("[name='" + selectName + "']").val(defaultValue);
                }

            }
        });
    } else {
        for (var index = 0; index < list.length; index++) {
            var line = $("<button type='button' class='list-group-item list-group-item-action' data-robot='" + list[index].robot + "' name='robotItem'>" +
                    "<span class='col-lg-6 grayscale'>" + list[index].robot + "</span>" +
                    "<img class='col-lg-2' src='images/platform-" + list[index].platform + ".png'/>" +
                    "<img class='col-lg-2' src='images/browser-" + list[index].browser + ".png'/>" +
                    "<span class='col-lg-2 grayscale'> " + list[index].version + " </span>" +
                    "</button>");
            $("[name='" + selectName + "']").append(line);
        }
    }

    $("[name='robotItem']").each(function () {
        $(this).on("click", function () {
            if ($(this).hasClass("active")) {
                $(this).removeClass("active");
            } else {
                $(this).addClass("active");
            }
        });
    });
}



/**
 * Method that return a list of value retrieved from the invariant list
 * @param {String} idName value that filters the invariants that will be retrieved (ex : "SYSTEM", "COUNTRY", ...)
 * @param {String} forceReload true in order to force the reload of list from database.
 * @param {String} addValue1 [optional] Adds a value on top of the normal List.
 * @param {String} async [optional] Do a async ajax request. Default: true
 * @returns {array}
 */
function getInvariantArray(idName, forceReload, addValue1, async = true) {

    // Adding the specific value when defined.
    if (addValue1 !== undefined) {
        result.add(addValue1);
    }

    if (forceReload === undefined) {
        forceReload = true;
    }

    const cacheEntryName = "INVARIANT_" + idName;
    if (forceReload) {
        sessionStorage.removeItem(cacheEntryName);
    }
    let list = JSON.parse(sessionStorage.getItem(cacheEntryName));

    let result = [];
    if (list === null) {
        $.ajax({
            url: "FindInvariantByID",
            data: {idName: idName},
            async: async,
            success: function (data) {
                list = data;
                sessionStorage.setItem(cacheEntryName, JSON.stringify(data));
                for (const element of list) {
                    result.push(element.value);
                }
            }
        });
    } else {
        for (const element of list) {
            result.push(element.value);
        }
    }
    return result;
}

/**
 * Method that display a combo box in all the selectName tags with the value retrieved from the invariant list
 * and the description of the invariant
 * @param {String} selectName value name of the select tag in the html
 * @param {String} idName value that filters the invariants that will be retrieved
 * @param {String} defaultValue to be selected
 * @returns {void}
 */
function displayInvariantListWithDesc(selectName, idName, defaultValue) {
    $.when($.getJSON("FindInvariantByID", "idName=" + idName)).then(function (data) {
        for (var option in data) {
            $("[name='" + selectName + "']").append($('<option></option>').text(data[option].value + " - " + data[option].description).val(data[option].value));
        }

        if (defaultValue !== undefined) {
            $("[name='" + selectName + "']").val(defaultValue);
        }
    });
}

/**
 * Method that display a combo box in all the selectName tags with the value retrieved from the DeployType list
 * @param {String} selectName value name of the select tag in the html
 * @param {String} defaultValue to be selected
 * @returns {void}
 */
function displayDeployTypeList(selectName, defaultValue) {
    $.when($.getJSON("ReadDeployType", "")).then(function (data) {
        for (var option in data.contentTable) {
            $("select[id='" + selectName + "']").append($('<option></option>').text(data.contentTable[option].deploytype + " - " + data.contentTable[option].description).val(data.contentTable[option].deploytype));
        }

        if (defaultValue !== undefined) {
            $("[name='" + selectName + "']").val(defaultValue);
        }
    });
}

/**
 * Method that display a combo box in all the selectName tags with the value retrieved from the DeployType list
 * @param {String} selectName value name of the select tag in the html
 * @param {String} defaultValue to be selected
 * @param {String} extraValue to be added
 * @returns {void}
 */
function displayAppServiceList(selectName, defaultValue, extraValue) {

    if (extraValue !== undefined) {
        let extraText = extraValue;
        if (extraValue === "") {
            extraText = "-- No Service --";
        }
        $("select[id='" + selectName + "']").append($('<option></option>').text(extraText).val(extraValue));
    }


    $.when($.getJSON("ReadAppService", "")).then(function (data) {
        for (var option in data.contentTable) {
            $("select[id='" + selectName + "']").append($('<option></option>').text(data.contentTable[option].service).val(data.contentTable[option].service));
        }

        if (defaultValue !== undefined) {
            $("[name='" + selectName + "']").val(defaultValue);
        }
    });

}

/**
 * Method that display a combo box in all the selectName tags with the value retrieved from the Application list
 * @param {String} selectName value name of the select tag in the html
 * @param {String} system [optional] value name of the system in order to filter the application list
 * @param {String} defaultValue to be selected
 * @param {String} extraValue add an aditional option if <> undefined
 * @returns {void}
 */
function displayApplicationList(selectName, system, defaultValue, extraValue) {
    var myData = "";
    if (extraValue !== undefined) {
        let extraText = extraValue;
        if (extraValue === "") {
            extraText = "-- No Application --";
        }
        $("[name='" + selectName + "']").append($("<option value='" + extraValue + "'></option>").text(extraText));
    }

    if ((system !== "") && (system !== undefined) && (system !== null)) {
        myData = "system=" + system;
    }

    $.when($.getJSON("ReadApplication", myData)).then(function (data) {
        for (var option in data.contentTable) {
            $("[name='" + selectName + "']").append($("<option></option>").text(data.contentTable[option].application + " [" + data.contentTable[option].type + "] " + data.contentTable[option].description).val(data.contentTable[option].application));
        }

        if (defaultValue !== undefined && defaultValue !== null) {
            $("[name='" + selectName + "']").val(defaultValue);
        }
    });
}

/**
 * Method that display a combo box in all the selectName tags with the value retrieved from the Project list
 * @param {String} selectName value name of the select tag in the html
 * @param {String} system value to filter the relevant list of batch
 * @param {String} defaultValue to be selected [optional]
 * @returns {void}
 */
function displayBatchInvariantList(selectName, system, defaultValue) {
    $.when($.getJSON("ReadBatchInvariant", "system=" + system)).then(function (data) {
        for (var option in data.contentTable) {
            $("[name='" + selectName + "']").append($('<option></option>').text(data.contentTable[option].batch + " - " + data.contentTable[option].description).val(data.contentTable[option].batch));
        }

        if (defaultValue !== undefined) {
            $("[name='" + selectName + "']").val(defaultValue);
        }
    });
}

/**
 * Method that display a combo box in all the selectName tags with the value retrieved from the Build Invariant list
 * @param {String} selectName html ref of the select tag in the html (ex : #selectBuild)
 * @param {String} system value of the system to filter the build.
 * @param {String} level value of the level of the build invariant
 * @param {String} defaultValue to be selected
 * @param {String} withAll "Y" in order to add a ALL entry
 * @param {String} withNone "Y" in order to add a NONE entry
 * @param {String} forceReload true in order to force the reload of build rev list from database
 * @returns {void}
 */
function displayBuildList(selectName, system, level, defaultValue, withAll, withNone, forceReload) {
    var select = $(selectName);

    if (forceReload === undefined) {
        forceReload = true;
    }

    var cacheEntryName = "BRINVARIANT" + "-" + level + "-" + system;
    if (forceReload) {
        sessionStorage.removeItem(cacheEntryName);
    }
    var list = JSON.parse(sessionStorage.getItem(cacheEntryName));

    if (list === null) {
        $.ajax({
            type: "GET",
            url: "ReadBuildRevisionInvariant",
            data: {iSortCol_0: "2", system: system, level: level},
            async: false,
            dataType: 'json',
            success: function (data) {

                if (withAll === "Y") {
                    select.append($('<option></option>').text("-- ALL --").val("ALL"));
                }
                if (withNone === "Y") {
                    select.append($('<option></option>').text("NONE").val("NONE"));
                }

                list = data.contentTable;
                sessionStorage.setItem(cacheEntryName, JSON.stringify(data.contentTable));
                for (var index = 0; index < list.length; index++) {
                    var item = list[index].versionName;
                    select.append($('<option></option>').text(data.contentTable[index].versionName).val(data.contentTable[index].versionName));
                }
                if (defaultValue !== undefined) {
                    select.val(defaultValue);
                }
            },
            error: showUnexpectedError
        });
    } else {

        if (withAll === "Y") {
            select.append($('<option></option>').text("-- ALL --").val("ALL"));
        }
        if (withNone === "Y") {
            select.append($('<option></option>').text("NONE").val("NONE"));
        }

        for (var index = 0; index < list.length; index++) {
            var item = list[index].versionName;
            select.append($('<option></option>').text(item).val(item));
        }
        if (defaultValue !== undefined) {
            select.val(defaultValue);
        }
    }

}

function getActionCombo() {
    var cacheEntryName = "ACTION_COMBO";

    if (sessionStorage.getItem(cacheEntryName) === null) {
        var actionCombo = $("<select></select>").addClass("form-control input-sm");
        actionCombo.css("width", "100%").attr("name", "actionSelect").addClass("templateComboAction");
        var user = getUser();
        for (var i = 0; i < actionOptGroupList.length; i++) {
            actionCombo.append($("<optGroup></optGroup>").attr("label", actionOptGroupList[i].label[user.language]).attr("data-group", actionOptGroupList[i].name).attr("data-picto", actionOptGroupList[i].picto));
        }

        for (var key in actionOptList) {
            if (actionOptList[key].group !== 'none') {
                actionCombo.find("[data-group='" + actionOptList[key].group + "']").append($("<option></option>").text(actionOptList[key].label[user.language]).val(actionOptList[key].value));
            } else {
                actionCombo.prepend($("<option></option>").text(actionOptList[key].label[user.language]).val(actionOptList[key].value));
            }
        }
        sessionStorage.setItem(cacheEntryName, actionCombo.prop('outerHTML'));
    }
    return $.parseHTML(sessionStorage.getItem(cacheEntryName));
}

function getControlCombo() {
    var cacheEntryName = "CONTROL_COMBO";

    if (sessionStorage.getItem(cacheEntryName) === null) {
        var controlCombo = $("<select></select>").addClass("form-control input-sm controlType");
        controlCombo.css("width", "100%").addClass("templateComboControl");
        var user = getUser();
        for (var key in newControlOptList) {
            controlCombo.append($("<option></option>").text(newControlOptList[key].label[user.language]).val(newControlOptList[key].value));
        }
        sessionStorage.setItem(cacheEntryName, controlCombo.prop('outerHTML'));
    }
    return $.parseHTML(sessionStorage.getItem(cacheEntryName));
}


/**
 * Method that display a combo box in all the selectName tags with the value retrieved from the Environment list
 * @param {String} selectName value name of the select tag in the html
 * @param {String} system value of the system to filter the build.
 * @param {String} defaultValue to be selected
 * @returns {void}
 */
function displayEnvList(selectName, system, defaultValue) {
    $.when($.getJSON("ReadCountryEnvParam", "system=" + system + "&active=Y")).then(function (data) {
        for (var option in data.contentTable) {
            $("[name='" + selectName + "']").append($('<option></option>').text(data.contentTable[option].environment).val(data.contentTable[option].environment));
        }

        if (defaultValue !== undefined) {
            $("[name='" + selectName + "']").val(defaultValue);
        }
    });
}

/**
 * Method that display a list-group-item with the value retrieved from the Application IP list
 * @param {String} selectName value name of the list-group in the html to append the items
 * @param {String} system value to filter.
 * @param {String} application value to filter.
 * @returns {void}
 */
function displayApplicationIpList(selectName, system, application, country, environment) {
    $.when($.getJSON("ReadCountryEnvironmentParameters", "system=" + system + "&application=" + application)).then(function (data) {
        for (var option in data.contentTable) {
            let classActive = "";
            if ((country === data.contentTable[option].country) && (environment === data.contentTable[option].environment)) {
                classActive = " active";
            }
            var line = $("<button type='button' data-country='" + data.contentTable[option].country + "' data-environment='" + data.contentTable[option].environment + "' name='applicationIpItem' class='list-group-item list-group-item-action" + classActive + "'>" +
                    "<span class='col-lg-8 grayscale' style='word-wrap: break-word;text-overflow;'>" + data.contentTable[option].ip + "</span>" +
                    "<div class='col-lg-4'><span class='label label-primary' style='background-color:#000000'>" + data.contentTable[option].country + "</span>" +
                    "<span class='label label-primary' style='background-color:#000000'>" + data.contentTable[option].environment + "</span></div>" +
                    "</button>");
            line.data("item", data.contentTable[option]);
            $("[name='" + selectName + "']").append(line);
        }
        $("[name='applicationIpItem']").each(function () {
            $(this).on("click", function () {
                if ($(this).hasClass("active")) {
                    $(this).removeClass("active");
                } else {
                    $(this).addClass("active");
                }
            });
        });

        if ($("[name='applicationIpItem']").size() === 1) {
            $("[name='applicationIpItem']").addClass("active");
        }
    });
}

/**
 * Method that display a combo box in all the selectName tags with the value retrieved from the Environment list
 * @param {String} selectName value name of the select tag in the html
 * @param {String} system name of the system
 * @param {String} defaultValue to be selected
 * @returns {void}
 */
function displayUniqueEnvList(selectName, system, defaultValue) {
    $.when($.getJSON("ReadCountryEnvParam", "uniqueEnvironment=true" + system)).then(function (data) {
        $("[name='" + selectName + "']").empty();
        for (var option in data.contentTable) {
            var text = data.contentTable[option].environment;
            if (data.contentTable[option].active === false)
                text = text + " [Currently Disabled]";
            $("[name='" + selectName + "']").append($('<option></option>').text(text).val(data.contentTable[option].environment));
        }
        if (data.contentTable.length <= 1) { // If only 1 environment, we select it directly.
            $("[name='" + selectName + "']").val(data.contentTable[0].environment);
        } else { // More than 1 value, we may select the default value.
            if (defaultValue !== undefined) {
                $("[name='" + selectName + "']").val(defaultValue);
            }
        }
    });
}

/**
 * Method that display a combo box in all the selectName tags with the value retrieved from the user list
 * @param {String} selectName value name of the select tag in the html
 * @returns {void}
 */
function displayUserList(selectName) {
    var myData = "iSortCol_0=1"; // We sort by login.
    $("[name='" + selectName + "']").append($('<option></option>').text("NONE").val(""));
    $.when($.getJSON("ReadUserPublic", myData)).then(function (data) {
        for (var option in data.contentTable) {
            $("[name='" + selectName + "']").append($('<option></option>').text(data.contentTable[option].login + " - " + data.contentTable[option].name).val(data.contentTable[option].login));
        }
    });
}


/**
 * Method that return a list of value retrieved from the invariant list
 * @param {String} forceReload true in order to force the reload of list from database.
 * @param {String} addValue1 [optional] Adds a value on top of the normal List.
 * @param {String} asyn [optional] Do a async ajax request. Default: true
 * @returns {array}
 */
function getUserArray(forceReload, addValue1, asyn) {
    var result = [];
    // Adding the specific value when defined.
    if (addValue1 !== undefined) {
        result.add(addValue1);
    }

    if (forceReload === undefined) {
        forceReload = true;
    }

    var async = true;
    if (asyn !== undefined) {
        async = asyn;
    }

    var cacheEntryName = "USERLIST";
    if (forceReload) {
        sessionStorage.removeItem(cacheEntryName);
    }
    var list = JSON.parse(sessionStorage.getItem(cacheEntryName));

    if (list === null) {
        $.ajax({
            url: "ReadUserPublic",
            async: async,
            success: function (data) {
                list = data.contentTable;
                sessionStorage.setItem(cacheEntryName, JSON.stringify(data));
                for (var index = 0; index < list.length; index++) {
                    var item = list[index].login;
                    result.push(item);
                }
            },
            error: showUnexpectedError
        });
    } else {
        for (var index = 0; index < list.length; index++) {
            var item = list[index].login;

            result.push(item);
        }
    }

    return result;
}

/**
 * Method that return a list of value retrieved from the invariant list
 * @param {String} forceReload true in order to force the reload of list from database.
 * @param {String} addValue1 [optional] Adds a value on top of the normal List.
 * @param {String} asyn [optional] Do a async ajax request. Default: true
 * @returns {array}
 */
function getCollectionArray(forceReload, asyn) {
    var result = [];

    if (forceReload === undefined) {
        forceReload = true;
    }

    var async = true;
    if (asyn !== undefined) {
        async = asyn;
    }

    var cacheEntryName = "COLLECTIONLIST";
    if (forceReload) {
        sessionStorage.removeItem(cacheEntryName);
    }
    var list = JSON.parse(sessionStorage.getItem(cacheEntryName));

    if (list === null) {
        $.ajax({
            url: "ReadAppService?columnName=srv.collection",
            async: async,
            success: function (data) {
                list = data.distinctValues;
                sessionStorage.setItem(cacheEntryName, JSON.stringify(data.distinctValues));
                for (var index = 0; index < list.length; index++) {
                    var item = list[index];
                    result.push(item);
                }
            },
            error: showUnexpectedError
        });
    } else {
        for (var index = 0; index < list.length; index++) {
            var item = list[index];

            result.push(item);
        }
    }

    return result;
}

/**
 * Auxiliary method that retrieves a list containing the values that belong to the invariant that matches the provided idname.
 * @param {String} idName value that filters the invariants that will be retrieved
 * @param {handleData} handleData method that handles the data retrieved
 */
function getInvariantList(idName, handleData) {
    $.when($.getJSON("GetInvariantList", "idName=" + idName)).then(function (data) {
        handleData(data);
    });
}

/**
 * Auxiliary method that retrieves set of invariants (based on a list of idnames), for each idname the method retrieves a list containing the associated values.
 * @param {list} list containing several idName values that filter the invariants that will be retrieved
 * @param {handleData} handleData method that handles the data retrieved
 */
function getInvariantListN(list, handleData) {
    $.when($.post("GetInvariantList", {
        action: "getNInvariant",
        idName: JSON.stringify(list)
    }, "json")).then(function (data) {
        handleData(data);
    });
}


/**
 * This method will return the combo list of Invariant.
 * It will load the values from the sessionStorage cache of the browser
 * when available, if not available, it will get it from the server and save
 * it on local cache.
 * The forceReload boolean can force the refresh of the list from the server.
 * @param {String} idName of the invariant to load (ex : COUNTRY)
 * @param {boolean} forceReload true if we want to force reloading on cache from the server
 * @param {boolean} async false if we don't want to have Async ajax
 * @param {boolean} addValue Value that can be added at the beginning of the combo.
 */
function getSelectInvariant(idName, forceReload, async = true, addValue) {
    const cacheEntryName = "INVARIANT_" + idName;
    if (forceReload) {
        sessionStorage.removeItem(cacheEntryName);
    }

    let list = JSON.parse(sessionStorage.getItem(cacheEntryName));
    let select = $("<select></select>").addClass("form-control");
    if (addValue !== undefined) {
        select.append($("<option></option>").text(addValue).val(addValue));
    }
    if (list === null) {
        $.ajax({
            url: "FindInvariantByID",
            data: {idName: idName},
            async: async,
            success: function (data) {
                list = data;
                sessionStorage.setItem(cacheEntryName, JSON.stringify(data));
                for (const element of list) {
                    const item = element.value;
                    select.append($("<option></option>").text(item).val(item));
                }
            },
            error: showUnexpectedError
        });
    } else {
        for (const element of list) {
            const item = element.value;
            select.append($("<option></option>").text(item).val(item));
        }
    }
    return select;
}

function cleanCacheInvariant(idName) {
    const cacheEntryName = "INVARIANT_" + idName;
    sessionStorage.removeItem(cacheEntryName);
    if (idName === "SYSTEM") {
        sessionStorage.removeItem("user");
        readUserFromDatabase();
        loadUserSystemCombo();
    }
}

/**
 * This method will return the combo list of Robot.
 * It will load the values from the sessionStorage cache of the browser
 * when available, if not available, it will get it from the server and save
 * it on local cache.
 * The forceReload boolean can force the refresh of the list from the server.
 * @param {boolean} forceReload true if we want to force the reload on cache from the server
 * @param {boolean} notAsync true if we dont want to have Async ajax
 */
function getSelectRobot(forceReload, notAsync) {
    var cacheEntryName = "ROBOTLIST";
    if (forceReload) {
        sessionStorage.removeItem(cacheEntryName);
    }
    var async = true;
    if (notAsync) {
        async = false;
    }
    var list = JSON.parse(sessionStorage.getItem(cacheEntryName));
    var select = $("<select></select>").addClass("form-control");

    if (list === null) {
        $.ajax({
            url: "ReadRobot",
            async: async,
            success: function (data) {
                list = data.contentTable;
                sessionStorage.setItem(cacheEntryName, JSON.stringify(data));
                for (var index = 0; index < list.length; index++) {
                    var item = list[index].robot;
                    var text = list[index].robot + " [" + list[index].host + "]";
                    select.append($("<option></option>").text(text).val(item));
                }
            },
            error: showUnexpectedError
        });
    } else {
        for (var index = 0; index < list.length; index++) {
            var item = list[index].value;
            var text = list[index].robot + " [" + list[index].host + "]";

            select.append($("<option></option>").text(text).val(item));
        }
    }

    return select;
}


/**
 * This method will return the combo list of Label.
 * It will load the values from the sessionStorage cache of the browser
 * when available, if not available, it will get it from the server and save
 * it on local cache.
 * The forceReload boolean can force the refresh of the list from the server.
 * @param {String} system filter label from system
 * @param {boolean} forceReload true if we want to force the reload on cache from the server
 * @param {boolean} notAsync true if we dont want to have Async ajax
 */
function getSelectLabel(system, forceReload, notAsync) {
    var cacheEntryName = "LABEL_" + system;
    if (forceReload) {
        sessionStorage.removeItem(cacheEntryName);
    }
    var async = true;
    if (notAsync) {
        async = false;
    }
    var list = JSON.parse(sessionStorage.getItem(cacheEntryName));
    var select = $("<select></select>").addClass("form-control");

    if (list === null) {
        $.ajax({
            url: "ReadLabel",
            async: async,
            data: {system: system},
            success: function (data) {
                list = data.contentTable;
                sessionStorage.setItem(cacheEntryName, JSON.stringify(data.contentTable));
                for (var index = 0; index < list.length; index++) {
                    var item = list[index].label + " - " + list[index].color;
                    select.append($("<option></option>").text(item).val(list[index].id));
                }
            },
            error: showUnexpectedError
        });
    } else {
        for (var index = 0; index < list.length; index++) {
            var item = list[index].label + " - " + list[index].color;

            select.append($("<option></option>").text(item).val(list[index].id));
        }
    }

    return select;
}

function getSelectApplication(system, forceReload) {
    var cacheEntryName = "APPLICATIONS_" + system;
    if (forceReload) {
        sessionStorage.removeItem(cacheEntryName);
    }
    var list = JSON.parse(sessionStorage.getItem(cacheEntryName));
    var select = $("<select></select>").addClass("form-control");

    if (list === null) {
        $.ajax({
            url: "ReadApplication",
            data: {system: system},
            async: false,
            success: function (data) {
                list = data.contentTable;
                sessionStorage.setItem(cacheEntryName, JSON.stringify(list));
                for (var index = 0; index < list.length; index++) {
                    var item = list[index].application;

                    select.append($("<option></option>").text(item + "[" + list[index].type + "]").val(item));
                }
            },
            error: showUnexpectedError
        });
    } else {
        for (var index = 0; index < list.length; index++) {
            var item = list[index].application;

            select.append($("<option></option>").text(item).val(item));
        }
    }

    return select;
}

function getSelectApplicationWithoutSystem() {

    var list = [];

    var select = $("<select></select>").addClass("form-control");

    $.ajax({
        url: "ReadApplication",
        async: false,
        success: function (data) {
            list = data.contentTable;
            for (var index = 0; index < list.length; index++) {
                var item = list[index].application;
                select.append($("<option></option>").text(item + " [" + list[index].type + "]").val(item));
            }
        },
        error: showUnexpectedError
    });

    return select;
}

function getSelectFolder() {

    var list = [];

    var select = $("<select></select>").addClass("form-control");

    $.ajax({
        url: "ReadTest?iSortCol_0=0&sSortDir_0=asc&sColumns=test",
        async: false,
        success: function (data) {
            list = data.contentTable;
            for (var index = 0; index < list.length; index++) {
                var item = list[index].test;
                select.append($("<option></option>").text(item).val(item));
            }
        },
        error: showUnexpectedError
    });

    return select;
}

function getSelectDeployType(forceReload) {
    var cacheEntryName = "DEPLOYTYPE";
    if (forceReload) {
        sessionStorage.removeItem(cacheEntryName);
    }
    var list = JSON.parse(sessionStorage.getItem(cacheEntryName));
    var select = $("<select></select>").addClass("form-control");

    if (list === null) {
        $.ajax({
            url: "ReadDeployType",
            data: {},
            async: true,
            success: function (data) {
                list = data.contentTable;
                sessionStorage.setItem(cacheEntryName, JSON.stringify(list));
                for (var index = 0; index < list.length; index++) {
                    var item = list[index].deploytype;

                    select.append($("<option></option>").text(item).val(item));
                }
            }
        });
    } else {
        for (var index = 0; index < list.length; index++) {
            var item = list[index].deploytype;

            select.append($("<option></option>").text(item).val(item));
        }
    }

    return select;
}


/**
 * Get and cache a parameter value.
 * The forceReload boolean can force the refresh of the list from the server.
 * @param {string} param value of the parameter to get ex : "cerberus_homepage_nbdisplayedtag"
 * @param {string} sys system that will be used to get the parameter
 * @param {boolean} forceReload true if we want to force the reload on cache from the server
 *
 */
function getParameter(param, sys, forceReload) {
    var result;
    if (isEmpty(sys)) {
        var cacheEntryName = "PARAMETER_" + param;
        var systemQuery = "";
    } else {
        var cacheEntryName = "PARAMETER_" + param + "_" + sys;
        var systemQuery = "&system=" + sys;
    }
    if (forceReload) {
        sessionStorage.removeItem(cacheEntryName);
    }
    var parameter = JSON.parse(sessionStorage.getItem(cacheEntryName));
    if (parameter === null) {
        $.ajax({
            url: "ReadParameter?param=" + param + systemQuery,
            data: {},
            async: false,
            success: function (data) {
                sessionStorage.setItem(cacheEntryName, JSON.stringify(data.contentTable));
                result = data.contentTable;
            }
        });
    } else {
        result = parameter;
    }
    return result;
}

/**
 * Get and cache a parameter value.
 * The forceReload boolean can force the refresh of the list from the server.
 * @param {string} param value of the parameter to get ex : "cerberus_homepage_nbdisplayedtag"
 * @param {string} sys system that will be used to get the parameter
 * @param {boolean} forceReload true if we want to force the reload on cache from the server
 *
 */
function getParameterString(param, sys, forceReload) {
    return getParameter(param, sys, forceReload).value;
}

/**
 * Get and cache a parameter value.
 * The forceReload boolean can force the refresh of the list from the server.
 * @param {string} param value of the parameter to get ex : "cerberus_homepage_nbdisplayedtag"
 * @param {string} sys system that will be used to get the parameter
 * @param {boolean} forceReload true if we want to force the reload on cache from the server
 *
 */
function getParameterBoolean(param, sys, forceReload) {
    let val = getParameter(param, sys, forceReload).value;
    if (val === 'Y' || val === 'y' || val === 'true' || val === 'yes')
        return true;
    return false;
}

/***********************************************Messages/ALERT***************************************/

/**
 * Auxiliary method that gets the code returned by the service and translates it into the corresponding type of message
 * @param {type} code  string to be translated
 * @returns {String} the string used by the message to determine the type of message
 */
function getAlertType(code) {
    if (code === "OK") {
        return "success";
    } else if (code === "KO") {
        return "danger";
    } else if (code === "WARNING") {
        return "warning";
    }
    return "danger";
}
function getAlertHttpType(code) {
    if ((code >= 200) && (code < 300)) {
        return "success";
    } else if ((code < 200)) {
        return "warning";
    } else if (code >= 300) {
        return "danger";
    }
    return "danger";
}

/**
 * Creates a message that should be presented to the user after the execution of an operation-
 * @param {type} messageType type of message (success, danger); it influes the color of the alert message
 * @param {type} message description of the message
 * @returns {Message} creates a object of a message
 */
function Message(messageType, message) {
    this.messageType = messageType;
    this.message = message;
}

/**
 * Clears the messages added in a dialog.
 * @param {type} dialog dialog where the messages are displayed
 */
function clearResponseMessage(dialog) {
    var elementAlert = dialog.find("div[id*='DialogMessagesAlert']");
    if (Boolean(elementAlert)) {
        elementAlert.slideUp(0);
    }
}

/**
 * Clears the messages added in the main page.
 */
function clearResponseMessageMainPage() {
    $("#mainAlert").removeClass("alert-success");
    $("#mainAlert").removeClass("alert-danger");
    $("#alertDescription").html("");
    $("#mainAlert").slideUp(0);
}

/**
 * Method that shows a message
 * @param {type} obj - object containing the message and the message type
 * @param {type} dialog - dialog where the message should be displayed; if null then the message
 * @param {boolean} silentMode - if true, message is not displayed if OK (default is false).
 * @param {integer} waitinMs - delay that the modal will stay visible in ms (default is automaticly calculated).
 * is displayed in the main page.
 */
function showMessage(obj, dialog, silentMode, waitinMs) {
    var code = getAlertType(obj.messageType);

    if (dialog !== undefined && dialog !== null) {

        if (isEmpty(waitinMs)) {
            // Automatically fadeout after n second.
            waitinMs = 10000; // Default wait to 10 seconds.
            if (code === "success") {
                waitinMs = 2000;
            } else if (code === "error") {
                waitinMs = 5000;
            }
        }

        //shows the error message in the current dialog
        var elementAlert = dialog.find("div[id*='DialogMessagesAlert']");
        var elementAlertDescription = dialog.find("span[id*='DialogAlertDescription']");

        elementAlertDescription.html(obj.message);
        elementAlert.removeClass("alert-success");
        elementAlert.removeClass("alert-danger");
        elementAlert.removeClass("alert-warning");
        elementAlert.addClass("alert-" + code);

        // We slowly hide it after waitinMs ms delay.
        elementAlert.fadeTo(500, 1, function () {
            setTimeout(function () {
                elementAlert.slideUp(500);
            }, waitinMs);
        });


//        elementAlert.fadeIn();
    } else {
        //shows the message in the main page
        showMessageMainPage(code, obj.message, silentMode, waitinMs);
    }

    /*if(dialog !== null && obj.messageType==="success"){
     jQuery(dialog).dialog('close');
     }*/
}

/**
 * Method that allows us to append a message in an already existing alert.
 * @param {object} obj  - object containing the message and the message type
 * @param {type} dialog - dialog where the message should be displayed; if null then the message
 * is displayed in the main page.
 */
function appendMessage(obj, dialog) {
    if (dialog !== null) {
        var elementAlertDescription = dialog.find("span[id*='DialogAlertDescription']");
        elementAlertDescription.append("<br/>" + obj.message);
    } else {
        $("#alertDescription").append(obj.message);
    }
}

/***
 * Shows a message in the main page. The area is defined in the header.jsp
 * @param {String} type - type of message: success, info, error, warning, ...
 * @param {String} message - message to show
 * @param {boolean} silentMode - if true, message is not displayed if OK (default is false).
 * @param {integer} waitinMs - delay that the modal will stay visible in ms (default is automaticly calculated).
 */
function showMessageMainPage(type, message, silentMode, waitinMs) {
    if (isEmpty(silentMode)) {
        silentMode = false;
    }

    if (isEmpty(waitinMs)) {
        // Automatically fadeout after n second.
        waitinMs = 10000; // Default wait to 10 seconds.
        if (type === "success") {
            waitinMs = 2000;
        } else if (type === "error") {
            waitinMs = 5000;
        }
    }
    // Only display if not success in silent mode.
    if (!((type === "success") && (silentMode))) {
        // We stop the previous delayed slide up (if any) and hide the alert.
        $("#mainAlert").stop();
        $("#mainAlert").slideUp(10);

        // We feed the new content and disply the alert.
        $("#mainAlert").removeClass("alert-success");
        $("#mainAlert").removeClass("alert-warning");
        $("#mainAlert").removeClass("alert-danger");
        $("#mainAlert").removeClass("alert-error");
        $("#mainAlert").removeClass("alert-info");
        $("#mainAlert").addClass("alert-" + type);
        $("#alertDescription").html(message);
        $("#mainAlert").slideDown(10);

        // We slowly hide it after waitinMs ms delay.
        $("#mainAlert").fadeTo(500, 1, function () {
            setTimeout(function () {
                $("#mainAlert").slideUp(500);
            }, waitinMs);
        });


    }
}

/*****************************************************************************/

$(function () {


    /**
     * Closes the alert message that is visible in the main page
     *
     /*****************************************************************************/
    $("#buttonMainAlert").click(function () {
        var elementToClose = $(this).closest("." + $(this).attr("data-hide"));
        $(elementToClose).siblings("strong span[class='alert-description']").text("");
        $("#mainAlert").removeClass("alert-success");
        $("#mainAlert").removeClass("alert-danger");
        $(elementToClose).fadeOut();
    });

    /**
     * Closes the alert page that is visible in the dialogs
     */
    $("[data-hide]").on("click", function () {
        var elementToClose = $(this).closest("." + $(this).attr("data-hide"));
        $(elementToClose).siblings("strong span[class='alert-description']").text("");
        $(elementToClose).parents("#mainAlert").removeClass("alert-success");
        $(elementToClose).parents("#mainAlert").removeClass("alert-danger");
        //$(this).closest("." + $(this).attr("data-hide")).hide();
        $(elementToClose).hide();
    });

    /**
     * Clears all the information from the modal that allows the upload of files
     */
    //resets the modal that allows the upload of files
    $('#modalUpload').on('hidden.bs.modal', function () {
        resetModalUpload();
    });
    //resets the confirmation modal data
    $('#confirmationModal').on('hidden.bs.modal', function () {
        resetConfirmationModal();
    });
});

/********************************LOADER*******************************************/

/**
 * Method that shows a loader inside a html element
 * @param {type} element
 */
function showLoader(element) {
    // Check if element is already blocked
    var uiElement = $(element);
    if (uiElement.data('blockUI.isBlocked')) {
        return;
    }
    var doc = new Doc();
    var processing = doc.getDocLabel("page_global", "processing");
    uiElement.block({message: processing});
}

/**
 * Method that hides a loader that was specified in a modal dialog
 * @param {type} element
 */
function hideLoader(element) {
    $(element).unblock();
}

/**
 * Method that shows a loader inside the content of a modal dialog
 * @param {type} element dialog
 */
function showLoaderInModal(element) {
    var doc = new Doc();
    var processing = doc.getDocLabel("page_global", "processing");
    $(element).find(".modal-content").block({message: processing});
}

/**
 * Method that hides a loader that was specified in a modal dialog
 * @param {type} element dialog
 */
function hideLoaderInModal(element) {
    $(element).find(".modal-content").unblock();
}

/**
 * Method that reset form values from a modal
 * @param {type} event
 * @returns {void}
 */
function modalFormCleaner(event) {
    var modalID = event.data.extra;
    // reset form values
    $(modalID + " " + modalID + "Form")[0].reset();
    // remove all errors on the form fields
    $(this).find('div.has-error').removeClass("has-error");
    // clear the response messages of the modal
    clearResponseMessage($(modalID));
}

/***********************************MODAL CONFIRMATION*************************************************/

/**
 *
 * @param {type} handlerClickOk - method triggered when the "Yes" is clicked
 * @param {type} handlerClickNo - method triggered when the "No" is clicked
 * @param {type} title - title to be displayed
 * @param {type} message -  message to be displayed
 * @param {type} hiddenField1 -hidden value that can be added to the confirmation modal. Useful when we want to delete an item, and we can specify it in this field.
 * @param {type} hiddenField2 -hidden value that can be added to the confirmation modal. Useful when we want to delete an item, and we can specify it in this field.
 * @param {type} hiddenField3 -hidden value that can be added to the confirmation modal. Useful when we want to delete an item, and we can specify it in this field.
 * @param {type} hiddenField4 -hidden value that can be added to the confirmation modal. Useful when we want to delete an item, and we can specify it in this field.
 * @returns {undefined}
 */
function showModalConfirmation(handlerClickOk, handlerClickNo, title, message, hiddenField1, hiddenField2, hiddenField3, hiddenField4) {
    setDataConfirmationModal(title, message, hiddenField1, hiddenField2, hiddenField3, hiddenField4);
    $('#confirmationModal #confirmOk').unbind("click").click(handlerClickOk);
    $('#confirmationModal #confirmNo').unbind("click").click(handlerClickNo);
    clearResponseMessageMainPage();
    $('#confirmationModal').modal('show');
}

function modalConfirmationIsVisible() {
    return $('#confirmationModal').is(":visible");
}

function hideModalConfirmationIsVisible() {
    return $('#confirmationModal').modal('hide');
}

/**
 * Method that cleans the confirmation modal after being closed.
 */
function resetConfirmationModal() {
    setDataConfirmationModal("", "", "", "", "", "");
    $('#confirmationModal #confirmOk').unbind('click');
}

/**
 * Method that allows the specification of a confirmation modal.
 * @param {type} title -  title to be displayed
 * @param {type} message -  message to be displayed
 * @param {type} hiddenField1 - hidden value that can be added to the confirmation modal. Useful when we want to delete an item, and we can specify it in this fiedl.
 * @param {type} hiddenField2 - hidden value that can be added to the confirmation modal. Useful when we want to delete an item, and we can specify it in this fiedl.
 * @param {type} hiddenField3 - hidden value that can be added to the confirmation modal. Useful when we want to delete an item, and we can specify it in this fiedl.
 * @param {type} hiddenField4 - hidden value that can be added to the confirmation modal. Useful when we want to delete an item, and we can specify it in this fiedl.
 */
function setDataConfirmationModal(title, message, hiddenField1, hiddenField2, hiddenField3, hiddenField4) {
    $('#confirmationModalLabel').html(title);
    $('#modalBody').html(message);
    if (hiddenField1 !== null) {
        $('#confirmationModal #hiddenField1').prop("value", hiddenField1);
    }
    if (hiddenField2 !== null) {
        $('#confirmationModal #hiddenField2').prop("value", hiddenField2);
    }
    if (hiddenField3 !== null) {
        $('#confirmationModal #hiddenField3').prop("value", hiddenField3);
    }
    if (hiddenField4 !== null) {
        $('#confirmationModal #hiddenField4').prop("value", hiddenField4);
    }
}

/**************************MODAL UPLOAD *********************************************/

/**
 * Auxiliary function that shows a modal dialog that allows the upload of files
 * @param {type} handlerClickOk / function that will be executed when the user clicks in the upload button
 * @param {type} fileExtension / extension of files that are allowed
 * @param {type} translations - the user can specify a new translations for the upload dialog.
 */
function showModalUpload(handlerClickOk, fileExtension, translations) {
    clearResponseMessageMainPage();
    //if translations are defined, then the title and buttons will be modified
    if (Boolean(translations)) {
        //update translations if a specific page secifies it
        $.each(translations, function (index) {
            $("#" + index).text(translations[index]);
        });
    } else {
        //use the default translations (for the specific language)
        var doc = new Doc();
        $("#modalUploadLabel").text(doc.getDocLabel("modal_upload", "title"));
        $("#choseFileLabel").text(doc.getDocLabel("modal_upload", "btn_choose"));
        $("#cancelButton").text(doc.getDocLabel("modal_upload", "btn_cancel"));
        $("#uploadOk").text(doc.getDocLabel("modal_upload", "btn_upload"));
    }

    $('#modalUpload').modal('show');
    $('#modalUpload').find('#uploadOk').click(handlerClickOk);
    $('#modalUpload').find("#fileInput").change(function () {
        validatesFileExtension(this.value, fileExtension);
    });
}

/**
 * Auxiliary function that validates if a fileName has a valid extension
 * @param {type} fileName name to be validated
 * @param {type} fileExtension extension against with the name is validated
 */
function validatesFileExtension(fileName, fileExtension) {
    var ext = fileName.match(/^([^\\]*)\.(\w+)$/);

    if (ext !== null && ext[ext.length - 1].toUpperCase() === fileExtension.toUpperCase()) {
        clearResponseMessage($('#modalUpload'));
        $("#upload-file-info").html(fileName);
        $('#uploadOk').removeProp("disabled");
    } else {
        resetModalUpload();
        var doc = new Doc();
        var localMessage = new Message("danger", doc.getDocLabel("page_global", "invalid_extension_message") + fileExtension + "!");
        showMessage(localMessage, $('#modalUpload'));
    }
}

/**
 * Method that resets the data entered in the modal used for uploading files.
 */
function resetModalUpload() {
    $('#modalUpload #fileInput').prop("value", "");
    $('#modalUpload #uploadOk').prop("disabled", "disabled");
    $('#modalUpload #uploadOk').unbind('click');

    $('#modalUpload #upload-file-info').text("");
    //gets the form and translates it in order to be uploadedshowModalUpload
    $('#modalUpload #formUpload')[0].reset();
    clearResponseMessage($('#modalUpload'));
}

/********************************TABLES*******************************************/

/**
 * Allows the definition of a new ajax source for datatables
 * @param {type} oSettings settings
 * @param {type} sNewSource new source
 */
$.fn.dataTableExt.oApi.fnNewAjax = function (oSettings, sNewSource) {
    if (typeof sNewSource !== 'undefined' && sNewSource !== null) {
        oSettings.sAjaxSource = sNewSource;
    }
    this.fnDraw();
};

/**
 * Auxiliary object that stores configurations that should be applied in a table that is client-side
 * @param {type} divId - table unique identifier
 * @param {type} data - data that is presented in the table
 * @param {type} aoColumnsFunction - function to render the columns
 * @param {booblean} activatePagination - true in order to activate pagination. False display all records.
 * @param {type} aaSorting - Table to define the sorting column and order. Ex : [3, 'asc']
 * @returns {TableConfigurationsClientSide}
 */
function TableConfigurationsClientSide(divId, data, aoColumnsFunction, activatePagination, aaSorting) {
    this.divId = divId;
    this.aoColumnsFunction = aoColumnsFunction;
    this.aaData = data;
    this.aaSorting = aaSorting;
    this.bDisplayRefreshButton = false;

    if (activatePagination) {
        this.lengthMenu = [10, 15, 20, 30, 50, 100];
        this.lengthChange = true;
        this.bPaginate = true;
        this.displayLength = 10;
        this.sPaginationType = "full_numbers";
    } else {
        this.bPaginate = false;
        this.lengthChange = false;
        this.displayLength = "All";
    }
    this.processing = false;
    this.serverSide = false;
    //not mandatory properties, and default values
    this.searchText = "";
    this.searchMenu = "";
    this.tableWidth = "1500px";
    this.bJQueryUI = true; //Enable jQuery UI ThemeRoller support (required as ThemeRoller requires some slightly different and additional mark-up from what DataTables has traditionally used

    //Enable or disable automatic column width calculation. This can be disabled as an optimisation (it takes some time to calculate the widths) if the tables widths are passed in using aoColumns.
    this.autoWidth = false;
    //Enable or disable state saving. When enabled a cookie will be used to save table display information such as pagination information, display length, filtering and sorting. As such when the end user reloads the page the display will match what they had previously set up
    this.stateSave = true;
    this.showColvis = true;
    this.scrollY = false;
    this.scrollX = true;
    this.scrollCollapse = false;
    this.lang = getDataTableLanguage();
    this.stateDuration = 0;
    this.colreorder = true;
    this.searchDelay = 500;
}

/**
 * Auxiliary object that stores configurations that should be applied in a table that is server-side
 * @param {type} divId - table unique identifier
 * @param {type} ajaxSource - ajax url
 * @param {type} ajaxProp -  json property
 * @param {type} aoColumnsFunction - function to render the columns
 * @param {type} aaSorting - Table to define the sorting column and order. Ex : [3, 'asc']
 * @param {type} lengthMenu - Length of the table default to [10, 25, 50, 100]
 * @returns {TableConfigurationsServerSide}
 */
function TableConfigurationsServerSide(divId, ajaxSource, ajaxProp, aoColumnsFunction, aaSorting, lengthMenu) {
    this.divId = divId;
    this.aoColumnsFunction = aoColumnsFunction;
    this.ajaxSource = ajaxSource;
    this.ajaxProp = ajaxProp;

    this.bDisplayRefreshButton = true;
    this.processing = true;
    this.serverSide = true;
    if (lengthMenu === undefined) {
        this.lengthMenu = [10, 15, 20, 30, 50, 100];
    } else {
        this.lengthMenu = lengthMenu;
    }
    this.lengthChange = true;
    //not mandatory properties, and default values
    this.searchText = "";
    this.searchMenu = "";
    this.tableWidth = "1500px";
    this.displayLength = 15;
    this.bJQueryUI = true; //Enable jQuery UI ThemeRoller support (required as ThemeRoller requires some slightly different and additional mark-up from what DataTables has traditionally used
    this.bPaginate = true;
    this.sPaginationType = "full_numbers";
    //Enable or disable automatic column width calculation. This can be disabled as an optimisation (it takes some time to calculate the widths) if the tables widths are passed in using aoColumns.
    this.autoWidth = false;
    //Enable or disable state saving. When enabled a cookie will be used to save table display information such as pagination information, display length, filtering and sorting. As such when the end user reloads the page the display will match what they had previously set up
    this.stateSave = true;
    this.showColvis = true;
    this.scrollY = false;
    this.scrollX = true;
    this.scrollCollapse = false;
    this.lang = getDataTableLanguage();
    this.orderClasses = true;
    this.bDeferRender = false;
    this.aaSorting = aaSorting;
    this.stateDuration = 0;
    this.colreorder = true;
    this.searchDelay = 500;
}

function returnMessageHandler(response) {
    if (response.hasOwnProperty("messageType") && response.hasOwnProperty("message")) {
        if (response.messageType !== "OK") {
            var type = getAlertType(response.messageType);

            clearResponseMessageMainPage();
            showMessageMainPage(type, response.message, false);
        }
    } else {
        showUnexpectedError();
    }
}

function showUnexpectedError(jqXHR, textStatus, errorThrown) {

    clearResponseMessageMainPage();
    var type = getAlertType(textStatus);
    var message = "";
    if (textStatus !== undefined && errorThrown !== undefined) {
        message = textStatus.toUpperCase() + " - " + errorThrown;
    } else {
        message = "ERROR - An unexpected error occured, the servlet may not be available. Please check if your session is still active";
    }
    showMessageMainPage(type, message, false);
    return message;
}

/***
 * Creates a datatable that is server-side processed.
 * @param {type} tableConfigurations set of configurations that define how data is retrieved and presented
 * @param {Function} callbackFunction callback function to be called after table creation (only on server side)
 * @param {String} objectWaitingLayer object that will report the waiting layer when external calls. Ex : #logViewer
 * @param {Array} filtrableColumns array of parameter name that can trigger filter on columns
 * @param {Boolean} checkPermissions boolean that define if user permission need to be checked
 * @param {type} userCallbackFunction
 * @param {Function} createdRowCallback callback function to be called after each row
 * @return {Object} Return the dataTable object to use the api
 */
function createDataTableWithPermissions(tableConfigurations, callbackFunction, objectWaitingLayer, filtrableColumns, checkPermissions, userCallbackFunction, createdRowCallback, async = true) {
    /**
     * Define datatable config with tableConfiguration object received
     */
    var configs = {};
    var domConf = 'ZRCB<"clear">lf<"pull-right"p>rti<"marginTop5">'; // Z allow to activate table resize
    if (!tableConfigurations.showColvis) {
        domConf = 'Zlf<"pull-right"p>rti<"marginTop5">';
    }
    configs["dom"] = domConf;
    configs["stateDuration"] = tableConfigurations.stateDuration;
    configs["serverSide"] = tableConfigurations.serverSide;
    configs["processing"] = tableConfigurations.processing;
    configs["bJQueryUI"] = tableConfigurations.bJQueryUI;
    configs["bPaginate"] = tableConfigurations.bPaginate;
    configs["autoWidth"] = tableConfigurations.autoWidth;
    configs["sPaginationType"] = tableConfigurations.sPaginationType;
    configs["columns.searchable"] = false;
    configs["columnDefs.targets"] = [0];
    configs["pageLength"] = tableConfigurations.displayLength;
    configs["scrollX"] = tableConfigurations.tableWidth;
    configs["scrollY"] = tableConfigurations.scrollY;
    configs["scrollCollapse"] = tableConfigurations.scrollCollapse;
    configs["stateSave"] = tableConfigurations.stateSave;
    configs["language"] = tableConfigurations.lang.table;
    configs["columns"] = tableConfigurations.aoColumnsFunction;
    configs["colVis"] = tableConfigurations.lang.colVis;
    configs["scrollX"] = tableConfigurations.scrollX;
    configs["lengthChange"] = tableConfigurations.lengthChange;
    configs["lengthMenu"] = tableConfigurations.lengthMenu;
    configs["orderClasses"] = tableConfigurations.orderClasses;
    configs["bDeferRender"] = tableConfigurations.bDeferRender;
    configs["columnReorder"] = tableConfigurations.colreorder;
    configs["searchDelay"] = tableConfigurations.searchDelay;
    configs["buttons"] = ['colvis'];
    if (tableConfigurations.aaSorting !== undefined) {
        configs["aaSorting"] = [tableConfigurations.aaSorting];
    }
    if (createdRowCallback !== undefined) {
        configs["createdRow"] = createdRowCallback;
    }
    if (tableConfigurations.serverSide) {

        configs["sAjaxSource"] = tableConfigurations.ajaxSource;
        configs["sAjaxDataProp"] = tableConfigurations.ajaxProp;

        configs["fnStateSaveCallback"] = function (settings, data) {
            try {
                localStorage.setItem(
                        'DataTables_' + settings.sInstance + '_' + location.pathname,
                        JSON.stringify(data)
                        );
            } catch (e) {
                console.error("access denied, " + e)
            }
        };
        configs["fnStateLoadCallback"] = function (settings) {
            //Get UserPreferences from user object
            var user = null;
            $.when(getUser()).then(function (data) {
                user = data;
            });
            while (user === null) {
                //Wait for user information make sure to don't loose it
            }

            if ("" !== user.userPreferences && undefined !== user.userPreferences && null !== user.userPreferences) {
                var userPref = JSON.parse(user.userPreferences);
                if (undefined !== userPref['DataTables_' + settings.sInstance + '_' + location.pathname]) {
                    return JSON.parse(userPref['DataTables_' + settings.sInstance + '_' + location.pathname]);
                }
            }
        };
        configs["colReorder"] = tableConfigurations.colreorder ? {
            fnReorderCallback: function () {
                $("#" + tableConfigurations.divId).DataTable().ajax.reload();
            }
        } : false;

        configs["fnServerData"] = function (sSource, aoData, fnCallback, oSettings) {

            var like = "";

            $.each(oSettings.aoColumns, function (index, value) {
                if (oSettings.aoColumns[index].like) {
                    like += oSettings.aoColumns[index].sName + ",";
                }
            });

            like = like.substring(0, like.length - 1);

            aoData.push({name: "sLike", value: like});
            if (sSource !== "ReadTest") { // RG, don't filter on system if it is a Test Folder
                for (var s in getUser().defaultSystems) {
                    aoData.push({name: "system", value: getUser().defaultSystems[s]});
                }
            }

            var objectWL = $(objectWaitingLayer);
            if (objectWaitingLayer !== undefined) {
                showLoader(objectWL);
            }

            oSettings.jqXHR = $.ajax({
                "dataType": 'json',
                "type": "POST",
                "async": async === undefined ? true : async,
                "url": sSource,
                "data": aoData,
                "success": function (json) {
                    if (objectWaitingLayer !== undefined) {
                        hideLoader(objectWL);
                    }
                    if (checkPermissions !== undefined && Boolean(checkPermissions)) {
                        var tabCheckPermissions = $("#" + tableConfigurations.divId);
                        var hasPermissions = false; //by default does not have permissions
                        if (Boolean(json["hasPermissions"])) { //if the response information about permissions then we will update it
                            hasPermissions = json["hasPermissions"];
                        }
                        //sets the permissions in the table
                        tabCheckPermissions.attr("hasPermissions", hasPermissions);
                    }
                    returnMessageHandler(json);
                    fnCallback(json);
                    if (Boolean(userCallbackFunction)) {
                        userCallbackFunction(json);
                    }
                },
                "error": showUnexpectedError
            });
            $.when(oSettings.jqXHR).then(function (data) {
                //updates the table with basis on the permissions that the current user has
                afterDatatableFeeds(tableConfigurations.divId, tableConfigurations.ajaxSource, oSettings);

                if (callbackFunction !== undefined)
                    callbackFunction(data);
            });
        };

    } else {

        configs["fnStateSaveCallback"] = function (oSettings, data) {
            try {
                localStorage.setItem(
                        'DataTables_' + oSettings.sInstance + '_' + location.pathname,
                        JSON.stringify(data)
                        );
            } catch (e) {
                console.error("access denied, " + e);
            }
            afterDatatableFeedsForServerSide(tableConfigurations.aaData, tableConfigurations.divId, oSettings);
        };

        configs["data"] = tableConfigurations.aaData;

        configs["fnStateLoadCallback"] = function (settings) {
            //Get UserPreferences from user object

            var user = null;
            $.when(getUser()).then(function (data) {
                user = data;
            });
            while (user === null) {
                //Wait for user information make sure to don't loose it
            }

            if ("" !== user.userPreferences && undefined !== user.userPreferences && null !== user.userPreferences) {
                var userPref = JSON.parse(user.userPreferences);
                var currentTable = userPref['DataTables_' + settings.sInstance + '_' + location.pathname];
                if (undefined !== currentTable) {
                    for (var i = 0; i < JSON.parse(currentTable)["columns"].length; i++) {
                        var currentSearch = JSON.parse(currentTable)["columns"][i]["search"]["search"];
                        var search = currentSearch.substr(1, currentSearch.length - 2);
                        search = search.split("|");
                        columnSearchValuesForClientSide.push(search);
                    }
                    return JSON.parse(currentTable);
                }
            }
        };
    }

    var oTable = $("#" + tableConfigurations.divId).DataTable(configs);

    var doc = new Doc();
    var showHideButtonLabel = doc.getDocLabel("page_global", "btn_showHideColumns");
    var showHideButtonTooltip = doc.getDocLabel("page_global", "tooltip_showHideColumns");
    var saveTableConfigurationButtonLabel = doc.getDocLabel("page_global", "btn_savetableconfig");
    var saveTableConfigurationButtonTooltip = doc.getDocDescription("page_global", "tooltip_savetableconfig");
    var restoreFilterButtonLabel = doc.getDocLabel("page_global", "btn_restoreuserpreferences");
    var restoreFilterButtonTooltip = doc.getDocDescription("page_global", "tooltip_restoreuserpreferences");
    var resetTableConfigurationButtonLabel = doc.getDocLabel("page_global", "btn_resettableconfig");
    var resetTableConfigurationButtonTooltip = doc.getDocDescription("page_global", "tooltip_resettableconfig");
    if (tableConfigurations.showColvis) {
        //Display button show/hide columns and Save table configuration
        $("#" + tableConfigurations.divId + "_wrapper #saveTableConfigurationButton").remove();
        $("#" + tableConfigurations.divId + "_wrapper #restoreFilterButton").remove();
        $("#" + tableConfigurations.divId + "_wrapper #resetFilterButton").remove();

        $("#" + tableConfigurations.divId + "_wrapper")
                .find("[class='dt-buttons btn-group']").removeClass().addClass("pull-right").find("a").attr('id', 'showHideColumnsButton').removeClass()
                .addClass("btn btn-default pull-right").attr("data-toggle", "tooltip").attr("title", showHideButtonTooltip).click(function () {
            //$("#" + tableConfigurations.divId + " thead").empty();
        }).html("<span class='glyphicon glyphicon-cog'></span> " + showHideButtonLabel);

        $("#" + tableConfigurations.divId + "_wrapper #showHideColumnsButton").parent().before(
                $("<button type='button' id='saveTableConfigurationButton'></button>").addClass("btn btn-default pull-right").append("<span class='glyphicon glyphicon-floppy-save'></span> " + saveTableConfigurationButtonLabel)
                .attr("data-toggle", "tooltip").attr("title", saveTableConfigurationButtonTooltip).click(function () {
            updateUserPreferences(objectWaitingLayer);
        })
                );

        $("#" + tableConfigurations.divId + "_wrapper #saveTableConfigurationButton").before(
                $("<button type='button' id='restoreFilterButton'></button>").addClass("btn btn-default pull-right").append("<span class='glyphicon glyphicon-floppy-open'></span> " + restoreFilterButtonLabel)
                .attr("data-toggle", "tooltip").attr("title", restoreFilterButtonTooltip).click(function () {
            location.reload();
        })
                );

        $("#" + tableConfigurations.divId + "_wrapper #restoreFilterButton").before(
                $("<button type='button' id='resetFilterButton'></button>").addClass("btn btn-default pull-right").append("<span class='glyphicon glyphicon-remove'></span> " + resetTableConfigurationButtonLabel)
                .attr("data-toggle", "tooltip").attr("title", resetTableConfigurationButtonTooltip).click(function () {
            localStorage.removeItem('DataTables_' + tableConfigurations.divId + '_' + location.pathname);
            updateUserPreferences(objectWaitingLayer);
            location.reload();
        })
                );


    }
    $("#" + tableConfigurations.divId + "_length select[name='" + tableConfigurations.divId + "_length']").addClass("form-control input-sm");
    $("#" + tableConfigurations.divId + "_length select[name='" + tableConfigurations.divId + "_length']").css("display", "inline");

    $("#" + tableConfigurations.divId + "_filter input[type='search']").addClass("form-control form-control input-sm");

    $("#" + tableConfigurations.divId + "_length").addClass("marginBottom10").addClass("width80").addClass("pull-left");
    $("#" + tableConfigurations.divId + "_filter").addClass("marginBottom10").addClass("width150").addClass("pull-left");
    $("#" + tableConfigurations.divId + "_filter").find('label').addClass("input-group");
    if (tableConfigurations.bDisplayRefreshButton) {
        $("#" + tableConfigurations.divId + "_filter").find('label').append("<span class='input-group-btn'><button id='dataTableRefresh' class='buttonObject btn btn-default input-sm' title='Refresh' type='button'><span class='glyphicon glyphicon-refresh'></span></button></span>");
    }

    $("#dataTableRefresh").click(function () {
        $("#" + tableConfigurations.divId).dataTable().fnDraw(false);
    });

    return oTable;
}

/**
 * Function called after data loaded in tables from server side
 * @returns {undefined}
 */
function afterDatatableFeedsForServerSide(tableConfigurations, tableConfigurations, oSettings) {
    /**
     * Display individual search on each columns
     */
    displayColumnSearchForClientSideTable(tableConfigurations, tableConfigurations, oSettings);


    /**
     * Add tooltip on fields where data need to be wrapped.
     */
    showTitleWhenTextOverflow();
}


/**
 * Function called after data loaded in tables from server side
 * @returns {undefined}
 */
function afterDatatableFeeds(divId, ajaxSource, oSettings) {
    /**
     * Display individual search on each columns
     */
    displayColumnSearch(divId, ajaxSource, oSettings);

    /**
     * Add tooltip on fields where data need to be wrapped.
     */
    showTitleWhenTextOverflow();

    /**
     * If specific function defined in page, call
     */

    if (typeof afterTableLoad === "function") {
        afterTableLoad();
    }
}

/**
 * This function add a tooltip if data in table field has to be wrapped
 * @returns {undefined}
 */
function showTitleWhenTextOverflow() {
    /**
     * for TH and TD, append title into div
     */
    $('td, th, h4').each(function () {
        var $ele = $(this);
        //Check if text to display is bigger than the width
        if (this.offsetWidth < this.scrollWidth && $ele.get(0).innerText.trim().length > 0) {
            $ele.attr('title', '<div>' + $ele.text() + '</div>');
            $ele.attr('data-html', true);
            $ele.attr('data-toggle', 'tooltip');
        }
    });
    /**
     * for PRE, create PRE and CODE tag into tooltip
     */
//    $('pre').each(function () {
//        var $ele = $(this);
//        if (this.offsetWidth < this.scrollWidth) {
//            $ele.attr('title', '<div><pre style="min-height:150px; width:800px">' + $ele.html() + '</pre></div>');
//            $ele.attr('data-html', true);
//            $ele.attr('data-toggle', 'tooltip');
//        }
//    });

    $('[data-toggle="tooltip"]').tooltip({
        container: 'body'
    });

//    $('[data-toggle="tooltip"]').on('inserted.bs.tooltip', function () {
//        if ($($("#" + $($(this).get(0)).attr("aria-describedby")).get(0)).find("pre").get(0) !== undefined) {
//            //Highlight envelop on modal loading
//            var editor = ace.edit($($("#" + $($(this).get(0)).attr("aria-describedby")).get(0)).find("pre").get(0));
//            editor.setTheme("ace/theme/chrome");
//            editor.getSession().setMode("ace/mode/xml");
//            editor.setOptions({
//                maxLines: 15
//            });
//        }
//    });

}

function resetColReorder(tableId) {
    $('#' + tableId).DataTable().colReorder.reset();
}

/**
 * Create the entry and display the message retrieved by the ajax call
 * @param {type} servletName
 * @param {type} form
 * @param {type} tableID
 * @returns {void}
 */
function createEntry(servletName, form, tableID) {
    // Get the header data from the form.
    var dataForm = convertSerialToJSONObject(form.serialize());

    var jqxhr = $.post(servletName, dataForm);
    $.when(jqxhr).then(function (data) {
        hideLoaderInModal("#addEntryModal");
        if (getAlertType(data.messageType) === 'success') {
            var oTable = $(tableID).dataTable();
            oTable.fnDraw(false);
            showMessage(data);
            $("#addEntryModal").modal('hide');
        } else {
            showMessage(data, $("#addEntryModal"));
        }
    }).fail(handleErrorAjaxAfterTimeout);
}

/**
 * Update the entry and display the message retrieved by the ajax call (does not change pagination)
 * @param {type} servletName
 * @param {type} form
 * @param {type} tableID
 * @returns {void}
 */
function updateEntry(servletName, form, tableID) {
    // Get the header data from the form.
    var dataForm = convertSerialToJSONObject(form.serialize());

    var jqxhr = $.post(servletName, dataForm);
    $.when(jqxhr).then(function (data) {
        hideLoaderInModal("#editEntryModal");
        if (getAlertType(data.messageType) === 'success') {
            var oTable = $(tableID).dataTable();
            oTable.fnDraw(false);
            showMessage(data);
            $("#editEntryModal").modal('hide');
        } else {
            showMessage(data, $("#editEntryModal"));
        }
    }).fail(handleErrorAjaxAfterTimeout);
}

/**
 * Duplicate the entry and display the message retrieved by the ajax call (does not change pagination)
 * @param {type} servletName
 * @param {type} form
 * @param {type} tableID
 * @returns {void}
 */
function duplicateEntry(servletName, form, tableID) {
    // Get the header data from the form.
    var dataForm = convertSerialToJSONObject(form.serialize());

    var jqxhr = $.post(servletName, dataForm);
    $.when(jqxhr).then(function (data) {
        hideLoaderInModal("#duplicateEntryModal");
        if (getAlertType(data.messageType) === 'success') {
            var oTable = $(tableID).dataTable();
            oTable.fnDraw(false);
            showMessage(data);
            $("#duplicateEntryModal").modal('hide');
        } else {
            showMessage(data, $("#duplicateEntryModal"));
        }
    }).fail(handleErrorAjaxAfterTimeout);
}

/**
 * This function is used to stop the propagtion of the click on the "?" anchor present on dataTables header so when we click on it, the sorting of the column doesn't change.
 * It should be called directly in the <a> tag with the onclick attribute
 * @param {type} event
 * @returns {undefined}
 */
function stopPropagation(event) {
    if (event.stopPropagation !== undefined) {
        event.stopPropagation();
    } else {
        event.cancelBubble = true;
    }
}

/**
 * Plugin used to delay the search filter; the purpose is to minimise the number of requests made to the server. Source: //cdn.datatables.net/plug-ins/1.10.7/api/fnSetFilteringDelay.js
 * @param {type} oSettings table settings
 * @param {type} iDelay time to delay
 * @returns {jQuery.fn.dataTableExt.oApi}
 */
jQuery.fn.dataTableExt.oApi.fnSetFilteringDelay = function (oSettings, iDelay) {
    var _that = this;

    if (iDelay === undefined) {
        iDelay = 250;
    }

    this.each(function (i) {
        $.fn.dataTableExt.iApiIndex = i;
        var
                $this = this,
                oTimerId = null,
                sPreviousSearch = null,
                anControl = $('input', _that.fnSettings().aanFeatures.f);

        anControl.unbind('keyup search input').bind('keyup search input', function () {
            var $$this = $this;

            if (sPreviousSearch === null || sPreviousSearch !== anControl.val()) {
                window.clearTimeout(oTimerId);
                sPreviousSearch = anControl.val();
                oTimerId = window.setTimeout(function () {
                    $.fn.dataTableExt.iApiIndex = i;
                    _that.fnFilter(anControl.val());
                }, iDelay);
            }
        });

        return this;
    });
    return this;
};

/**
 * Auxiliary function that sets the autocomplete option in one html element.
 * @param {type} selector
 * @param {type} source
 * @returns {undefined}
 */
function setAutoCompleteServerSide(selector, source) {
    var configurations = {};
    //sets the source of data
    configurations["source"] = source;
    //does not display the summary text
    configurations["messages"] = {
        noResults: '',
        results: function () {
        }
    };
    //specifies a delay to avoid excessive requests to the server
    configurations["delay"] = 500;
    //sets the autocomplete in the element
    $(selector).autocomplete(configurations);

}

/**
 * display global label
 * @param {JavaScript Object} doc
 * @returns {void}
 */
function displayGlobalLabel(doc) {
    $("[name='buttonAdd']").html(doc.getDocLabel("page_global", "buttonAdd"));
    $("[name='buttonClose']").html(doc.getDocLabel("page_global", "buttonClose"));
    $("[name='buttonConfirm']").html(doc.getDocLabel("page_global", "buttonConfirm"));
    $("[name='buttonDismiss']").html(doc.getDocLabel("page_global", "buttonDismiss"));
}

/**
 * generate and display the footer
 * @param {JavaScript Object} doc
 * @returns {void}
 */
function displayFooter(doc) {
    var cerberusInformation = getCerberusInformation();
    if (cerberusInformation !== null) {
        var footerString = doc.getDocLabel("page_global", "footer_text");
        var footerBugString = doc.getDocLabel("page_global", "footer_bug");
        var date = new Date();
        var loadTime = window.performance.timing.domContentLoadedEventEnd - window.performance.timing.navigationStart;

        footerString = footerString.replace("%VERSION%", cerberusInformation.projectName + cerberusInformation.projectVersion + "-" + cerberusInformation.databaseCerberusTargetVersion);
        footerString = footerString.replace("%ENV%", cerberusInformation.environment);
        footerString = footerString.replace("%BUILD%", cerberusInformation.projectBuild);
        footerString = footerString.replace("%DATE%", date.toISOString());
        footerString = footerString.replace("%TIMING%", loadTime);
        footerString = footerString.replace("%SERVERDATE%", cerberusInformation.serverDate);
        footerBugString = footerBugString.replace("%LINK%", "https://github.com/vertigo17/Cerberus/issues/new?body=Cerberus%20Version%20:%20" + cerberusInformation.projectVersion + "-" + cerberusInformation.databaseCerberusTargetVersion);
        $("#footer").html(footerString + " - " + footerBugString);

        // Tune the page layout to the environment where Cerberus is running.
        envTuning(cerberusInformation.environment);
    }

}

/**
 * Change the page layout in order to show that we are in production or not.
 * @param {String} myenv
 * @returns {void}
 */
function envTuning(myenv) {
    // Background color is light yellow if the environment is not production.

    isProduction = true;
    isProduction = ((myenv === "prd") || (myenv === "prod") || (myenv === "PROD") || (myenv === "demo"));
    isDev = ((window.location.hostname.includes('localhost'))
            || (window.location.hostname.includes('gravity.cerberus-testing.com'))
            || (window.location.hostname.includes('qa.cerberus-testing.com'))
            || (window.location.hostname.includes('gravity.cerberus-testing.fr'))
            || (window.location.hostname.includes('qa.cerberus-testing.fr'))
            );

    if (!isProduction) {
        document.body.style.background = "#FFFFCC";
    }

    if ((isProduction) && (!isDev)) {
//        document.getElementById("menuDocumentationD3").style.display = "none";
//        document.getElementById("menuDocumentationD3").style.display = "none";
//        document.getElementById("menuSwagger").style.display = "none";

        // Hide Russia language entry.
        document.getElementById("MyLang")[2].style.display = "none";
    }
}

/**
 * Get the parameter passed in the url Example : url?param=value
 * @param {String} sParam parameter you want to get value from
 * @param {String} defaultValue Default value in case the parameter is not defined in the URL.
 * @returns {GetURLParameter.sParameterName} the value or defaultValue does not exist in URL or null if not found in URL and no default value specified.
 */
function GetURLParameter(sParam, defaultValue) {
    var sPageURL = window.location.search.substring(1);
    var sURLVariables = sPageURL.split('&');

    for (var i = 0; i < sURLVariables.length; i++) {
        var sParameterName = sURLVariables[i].split('=');
        if (sParameterName[0] === sParam) {
            return decodeURIComponent(sParameterName[1]);
        }
    }
    if (defaultValue === undefined) {
        return null;
    } else {
        return defaultValue;
    }
}

/**
 * Get the parameter passed in the url Example : url?param=value
 * @param {String} sParam parameter you want to get value from
 * @param {String} defaultValue Default value in case the parameter is not defined in the URL.
 * @returns {GetURLParameter.sParameterName} the value or defaultValue does not exist in URL or null if not found in URL and no default value specified.
 */
function GetURLAnchorValue(sParam, defaultValue) {
    var sPageURL = window.location.hash.substring(1);
    var sURLVariables = sPageURL.split('|');

    for (var i = 0; i < sURLVariables.length; i++) {
        var sParameterName = sURLVariables[i].split('=');
        if (sParameterName[0] === sParam) {
            return decodeURIComponent(sParameterName[1]);
        }
    }
    if (defaultValue === undefined) {
        return null;
    } else {
        return defaultValue;
    }
}

/**
 * Get the parameter passed in the url Example : url?param=value
 * @param {String} sParam parameter you want to get value from
 * @returns {GetURLParameter.sParameterName} the value or defaultValue does not exist in URL or null if not found in URL and no default value specified.
 */
function GetURLParameters(sParam) {
    var sPageURL = window.location.search.substring(1);
    var sURLVariables = sPageURL.split('&');
    var result = new Array;

    for (var i = 0; i < sURLVariables.length; i++) {
        var sParameterName = sURLVariables[i].split('=');
        if (sParameterName[0] === sParam) {
            result.push(decodeURIComponent(sParameterName[1]));
        }
    }
    return result;
}

/**
 * Replace the parameter passed in the url Example : url?param=value&toto=tutu is replaced to  url?param=newValue&toto=tutu
 * @param {String} sParam parameter you want to replace value from
 * @param {String} new Value of the sParam
 * @returns {GetURLParameter.sParameterName} the value or defaultValue does not exist in URL or null if not found in URL and no default value specified.
 */
function ReplaceURLParameters(sParam, sValue) {
    let sPageURL = window.location.search.substring(1);
    let sURLVariables = sPageURL.split('&');
    let result = "";
    let replaced = false;

    for (var i = 0; i < sURLVariables.length; i++) {
        var sParameterName = sURLVariables[i].split('=');
        if (sParameterName[0] === sParam) {
            result += sParameterName[0] + "=" + sValue;
            replaced = true;
//            result.push(decodeURIComponent(sParameterName[1]));
        } else {
            result += sParameterName[0] + "=" + sParameterName[1];
        }
        result += "&";
    }
    if (!replaced) {
        result += sParam + "=" + sValue + "&";
    }
    return result.substring(0, result.length - 1);
}

/**
 * Add an browser history entry only if different from the current one.
 * @param {string} sUrl Url to insert in the history.
 * @returns {void}
 */
function InsertURLInHistory(sUrl) {
    if (sUrl.substr(sUrl.length - 1) === "?") { // If the url ends by ?, we remove it.
        sUrl = sUrl.substr(0, sUrl.length - 1);
    }
    var currentURL = window.location.href.replace(window.location.origin, "");
    var currentURLtoTest = currentURL + "TOTO";
    var sUrltoTest = sUrl + "TOTO";
    if (currentURLtoTest.indexOf(sUrltoTest) === -1) {
        window.history.pushState({}, '', sUrl);
    }
    return null;
}

function convertSerialToJSONObject(serial) {
    var data = serial.split("&");

    var obj = {};
    for (var param in data) {
        var key = data[param].split("=")[0];
        var value = data[param].split("=")[1];

        if (obj.hasOwnProperty(key)) {
            var tmp = obj[key];

            if (typeof tmp === "object") {
                obj[key].push(value);
            } else {
                obj[key] = [tmp, value];
            }
        } else {
            obj[key] = value;
        }
    }
    return obj;
}

/**
 * Bind the toggle action to the panel body
 * @returns {void}
 */
function bindToggleCollapse() {
    $(".collapse").each(function () {
        if (this.id !== "sidenavbar-subnavlist") {//disable interaction with the navbar
            $(this).on('shown.bs.collapse', function () {
                localStorage.setItem("PanelCollapse_" + this.id, true);
                updateUserPreferences();
                $(this).prev().find(".toggle").removeClass("glyphicon-chevron-down").addClass("glyphicon-chevron-right");
            });

            $(this).on('hidden.bs.collapse', function () {
                localStorage.setItem("PanelCollapse_" + this.id, false);
                updateUserPreferences();
                $(this).prev().find(".toggle").removeClass("glyphicon-chevron-right").addClass("glyphicon-chevron-down");
            });

            if (localStorage.getItem("PanelCollapse_" + this.id) === "false") {
                $(this).removeClass('in');
                $(this).prev().find(".toggle").removeClass("glyphicon-chevron-right").addClass("glyphicon-chevron-down");
            } else {
                $(this).addClass('in');
                $(this).prev().find(".toggle").removeClass("glyphicon-chevron-down").addClass("glyphicon-chevron-right");
            }
        }
    });
}

/**
 * Bind the toggle fullscreen to the text input and ace fields
 * bid is done on all elements with class togglefullscreen
 * the for attribute point to the div that will move to fullscreen
 * @returns {void}
 */
function bindToggleFullscreen() {
    $(".togglefullscreen").unbind("click").click(function () {
        let idTomove = this.getAttribute('for');
        let myButton = document.getElementById(idTomove);
        if (myButton.classList.contains("overlay")) {
            myButton.classList.remove('overlay');
            $(document).unbind("keydown");
        } else {
            myButton.classList.add('overlay');

            $(document).bind("keydown", function (e) {
                e = e || window.event;
                var charCode = e.which || e.keyCode;
                // ESC key will remove fullscreen mode
                if (charCode == 27) {
                    myButton.classList.remove('overlay');
                    $(document).unbind("keydown");
                }
            });

        }
    });
}

function drawURL(data) {
    return drawHyperlink(data, data);
}

function drawHyperlink(href, text) {
    if (text.indexOf("://") > -1) {
        let host = text.split("://")[1].split("/")[0];
        return "<a target=\"_blank\" href='" + href + "'>" + host + "</a>";//TODO:FN ver se tem caracters que precisam de ser encapsulados
    }
    return text;
}

function drawHyperlinkExternal(href, text) {
    if (text !== '') {
        return "<a target = '_blank' href='" + href + "'>" + text + "</a>";//TODO:FN ver se tem caracters que precisam de ser encapsulados
    }
    return '';
}

function loadSelectElement(data, element, includeEmpty, includeEmptyText) {
    $(element).empty();
    if (includeEmpty !== null && includeEmpty) {
        $(element).append("<option value=''>" + includeEmptyText + "</option>");
    }
    $.each(data, function (idx, obj) {
        $(element).append("<option value='" + obj + "'>" + obj + "</option>");
    });

}

function escapeHtml(unsafe) {
    return unsafe
            .replace(/"/g, "&quot;")
            .replace(/\\/g, '\\\\')
            .replace(/'/g, "\\'");
}

function getShortenString(bigString) {
    if (bigString.length > 300) {
        return bigString.substring(0, 300) + "... (TOO LONG TO DISPLAY !! Please check Action or Property detail)";
    }
    return bigString;
}

function getRowClass(status) {
    var rowClass = [];

    rowClass["panel"] = "panel" + status;
    if (status === "OK") {
        rowClass["glyph"] = "glyphicon glyphicon-ok";
    } else if (status === "KO") {
        rowClass["glyph"] = "glyphicon glyphicon-remove";
    } else if (status === "FA") {
        rowClass["glyph"] = "fa fa-bug";
    } else if (status === "CA") {
        rowClass["glyph"] = "fa fa-life-ring";
    } else if (status === "PE") {
        rowClass["glyph"] = "fa fa-hourglass-half";
    } else if (status === "NE") {
        rowClass["glyph"] = "glyphicon glyphicon-remove";
    } else if (status === "WE") {
        rowClass["glyph"] = "fa fa-hand-lizard-o";
    } else if (status === "NA") {
        rowClass["glyph"] = "fa fa-question";
    } else if (status === "QU") {
        rowClass["glyph"] = "fa fa-hourglass-half";
    } else if (status === "PA") {
        rowClass["glyph"] = "glyphicon glyphicon-pause blink";
    } else if (status === "QE") {
        rowClass["glyph"] = "glyphicon glyphicon-tasks";
    } else {
        rowClass["glyph"] = "";
    }
    return rowClass;
}

function getExeStatusRowColor(status) {
    if (status === "OK") {
        return '#00d27a';
    } else if (status === "KO") {
        return '#e63757';
    } else if (status === "FA") {
        return '#f5803e';
    } else if (status === "CA") {
        return '#c6a20d';
    } else if (status === "PE") {
        return 'rgb(44,123,229)';
    } else if (status === "NE") {
        return '#FFFFFF';
    } else if (status === "WE") {
        return '#34495E';
    } else if (status === "NA") {
        return '#F1C40F';
    } else if (status === "QU") {
        return '#BF00BF';
    } else if (status === "PA") {
        return '#D8BFD8';
    } else if (status === "QE") {
        return '#5C025C';
    } else {
        return 'lightgrey';
    }
}

/**
 * Method that return true if val is null, undefined or empty
 * @param {String} val value to test
 * @returns {boolean} true if is null, undefined of len >= 0
 */
function isEmpty(val) {
    return (val === undefined || val === null || val.length <= 0) ? true : false;
}

/**
 * Method that return true if val is null, undefined, empty or = ALL
 * @param {String} val value to test
 * @returns {boolean} true if is null, undefined of len >= 0
 */
function isEmptyorALL(val) {
    return (val === undefined || val === null || val.length <= 0 || val === 'ALL') ? true : false;
}

/**
 * Method that return a date object from a timestamp input in format : YYYYMMDDHHMMSSsss ex : 20170326183605616
 * @param {integer} timestamp value in format YYYYMMDDHHMMSSsss
 * @returns {date} date object
 */
function convToDate(timestamp) {
    var str = "" + timestamp;
    strDateObj = new Date(str.substring(0, 4), str.substring(4, 6), str.substring(6, 8), str.substring(8, 10), str.substring(10, 12), str.substring(12, 14), str.substring(14, 17));
    return strDateObj;
}

/**
 * Method that return a String that contain the date. If date is 1970, the string return will be empty.
 * @param {string} date
 * @returns {string} date in string format
 */
function getDate(date) {
    var d1 = new Date('1980-01-01');
    var endExe = new Date(date);
    if (endExe > d1) {
        return endExe.toLocaleString();
    } else {
        return "";
    }
}

/**
 * Method that return a String that contain the date. If date is 1970, the string return will be empty.
 * @param {string} date
 * @returns {string} date in string format
 */
function getDateTime(date) {
    var d1 = new Date('1980-01-01');
    var endExe = new Date(date);
    if (endExe > d1) {
        return endExe.toLocaleTimeString();
    } else {
        return "";
    }
}

/**
 * Method that return a String that contain the date. If date is 1970, the string return will be empty.
 * @param {string} date
 * @returns {string} date in string format
 */
function getDateShort(date) {
    var endExe = new Date(date);
    var d1 = new Date('1980-01-01');
    if (endExe > d1) {
        return ('0' + endExe.getHours()).substr(-2) + ":" + ('0' + endExe.getMinutes()).substr(-2) + ":" + ('0' + endExe.getSeconds()).substr(-2);
    } else {
        return "";
    }
}

function getDateMedium(date) {
    var endExe = new Date(date);
    var d1 = new Date('1980-01-01');
    if (endExe > d1) {
        return ("0" + endExe.getDate()).slice(-2) + "-" + ("0" + (endExe.getMonth() + 1)).slice(-2) + "-" + endExe.getFullYear() + " " + ('0' + endExe.getHours()).substr(-2) + ":" + ('0' + endExe.getMinutes()).substr(-2) + ":" + ('0' + endExe.getSeconds()).substr(-2);
    } else {
        return "";
    }
}

function getHumanReadableDuration(durInSec, nbUnits = 2) {
    let dur = durInSec;
    let unit = "s";
    let cnt1 = 0;
    let cnt2 = 0;
    if (dur > 60) {
        dur = dur / 60;
        unit = "min";
    } else {
        return Math.round(dur) + " " + unit;
    }
    if (dur >= 60) {
        dur = dur / 60;
        unit = "h";
    } else {
        cnt1 = Math.floor(dur);
        cnt2 = durInSec - (cnt1 * 60);
        if ((cnt2 > 0) && (nbUnits > 1)) {
            return cnt1 + " " + unit + " " + Math.round(cnt2) + " s";
        } else {
            return cnt1 + " " + unit;
        }
    }
    if (dur > 24) {
        dur = dur / 24;
        unit = "d";
    } else {
        cnt1 = Math.floor(dur);
        cnt2 = durInSec - (cnt1 * 60 * 60);
        if ((cnt2 > 0) && (nbUnits > 1)) {
            return cnt1 + " " + unit + " " + getHumanReadableDuration(cnt2, (nbUnits - 1));
        } else {
            return cnt1 + " " + unit;
        }
    }
    cnt1 = Math.floor(dur);
    cnt2 = durInSec - (cnt1 * 60 * 60 * 24);
    if ((cnt2 > 0) && (nbUnits > 1)) {
        return cnt1 + " " + unit + " " + getHumanReadableDuration(cnt2, (nbUnits - 1));
    } else {
        return cnt1 + " " + unit;
}
}

function getClassDuration(duration) {
    if (duration <= 60000) {
        return '';
    }
    if (duration > 300000) {
        return 'statusKO';
    }
    return 'statusFA';
}

function mimicISOString(date) {
    let d = new Date(date),
            month = '' + (d.getMonth() + 1),
            day = '' + d.getDate(),
            year = d.getFullYear();
    if (month.length < 2)
        month = '0' + month;
    if (day.length < 2)
        day = '0' + day;
    return [year, month, day].join('-') + 'T00:00:00.000Z';
}



var unitlist = ["", " k", " M", " G"];
function formatnumberKM(number) {
    let sign = Math.sign(number);
    let unit = 0;

    while (Math.abs(number) >= 1000)
    {
        unit = unit + 1;
        number = Math.floor(Math.abs(number) / 100) / 10;
    }
    return sign * Math.abs(number) + unitlist[unit];
}

function getFromStorage(sSessionEntry, defaultValue) {
    if (sessionStorage.getItem(sSessionEntry) !== null) {
        return sessionStorage.getItem(sSessionEntry);
    } else {
        return defaultValue;
    }
}

function setTimeRange(id) {
    let fromD = new Date();
    fromD.setHours(00);
    fromD.setMinutes(00);
    fromD.setSeconds(00);

    let toD = new Date();
    toD.setHours(24);
    toD.setMinutes(00);
    toD.setSeconds(00);

    let now = new Date();

//    fromD ;
    if (id === 1) { // Previous Month
        fromD.setMonth(toD.getMonth() - 1);
    } else if (id === 2) { // Previous 3 Months
        fromD.setMonth(fromD.getMonth() - 3);
    } else if (id === 3) { // Previous 6 Months
        fromD.setMonth(fromD.getMonth() - 6);
    } else if (id === 4) { // Previous Year
        fromD.setMonth(fromD.getMonth() - 12);
    } else if (id === 5) { // Previous Week
        fromD.setDate(toD.getDate() - 7);
    } else if (id === 6) { // Current Day
        fromD.setDate(toD.getDate() - 1);
    } else if (id === 7) { // This Month
        fromD.setDate(1);
    } else if (id === 8) { // Last Calendar Month       
        fromD.setMonth(fromD.getMonth() - 1);
        fromD.setDate(1);
        toD.setDate(1);
    } else if (id === 9) { // Previous Calendar Month
        fromD.setMonth(fromD.getMonth() - 2);
        fromD.setDate(1);
        toD.setMonth(toD.getMonth() - 1);
        toD.setDate(1);
    } else if (id === 10) { // Previous Hour
        fromD.setHours(now.getHours() - 1, now.getMinutes());
    } else if (id === 11) { // Previous 6 Hours
        fromD.setHours(now.getHours() - 6, now.getMinutes());
    }

    $('#frompicker').data("DateTimePicker").date(moment(fromD));
    $('#topicker').data("DateTimePicker").date(moment(toD));

    console.info("From : " + fromD.toLocaleString() + " - To : " + toD.toLocaleString());
}



/**
 * Method used to restrict usage of some specific caracters.
 * @param {String} val value to test
 * @returns {boolean} true if is null, undefined of len >= 0
 */
var propertyNameRestriction = /[\(\.\,\)\'\"]/g;
var subDataNameRestriction = /[\(\.\,\)\'\"]/g;
var testDataLibNameRestriction = /[\(\.\,\)\'\"]/g;

/**
 * Function that allow to autoComplete Input with different data and regex that decide which data to show
 * @param {type} identifier jquery identifier to find the input to affect the autocomplete
 * @param {type} Tags array of Tags order by priority (first regex find will display its list only):
 *             {
 *                  array : array of String to analyse and display,
 *                  regex : regex to detect if we have to show the list or not,
 *                  addBefore : String to add before the value when selected,
 *                  addAfter : String to add after the value when selected
 *             }
 *
 */
function autocompleteVariable(identifier, Tags) {

    function split(val, separator) {
        return val.split(new RegExp(separator + "(?!.*" + separator + ")"));
    }

    function extractLast(term, separator) {
        return split(term, separator).pop();
    }

    function extractAllButLast(term, separator) {
        var last = split(term, separator).pop();
        var index = term.lastIndexOf(last);
        return term.substring(0, index);
    }

    $(identifier)
            // don't navigate away from the field on tab when selecting an item
            .on("keydown", function (event) {
                if (event.keyCode === $.ui.keyCode.TAB &&
                        $(this).autocomplete("instance").menu.active) {
                    event.preventDefault();
                }
                // We hide the message generated by autocomplete because we don't want it
                $("span[role='status']").hide();
            })
            .autocomplete({
                minLength: 1,
                messages: {
                    noResults: '',
                    results: function () {
                    }
                },
                open: function () {
                    //If autocomplete is in modal, needs to be upper the modal
                    if ($(this).closest($(".modal")).length > 0) {
                        $(this).autocomplete('widget').css('z-index', 1050);
                    }
                    return false;
                },
                source: function (request, response) {
                    //Get the part of the string we want (between the last % before our cursor and the cursor)
                    var selectionStart = this.element[0].selectionStart;
                    var stringToAnalyse = this.term.substring(0, selectionStart);
                    var identifier = stringToAnalyse.substring(stringToAnalyse.lastIndexOf("%"));
                    //If there is a pair number of % it means there is no open variable that needs to be autocompleted
                    if ((this.term.match(/%/g) || []).length % 2 > 0) {
                        //Start Iterating on Tags
                        var tag = 0;
                        var found = false;
                        while (tag < Tags.length && !found) {
                            //If We find the separator, then we filter with the already written part
                            if ((identifier.match(new RegExp(Tags[tag].regex)) || []).length > 0) {
                                var arrayLabels = [];

                                if (Tags[tag].regex === "%object\\.") {
                                    Tags[tag].array.forEach(function (data) {
                                        arrayLabels.push(data.object);
                                    });

                                } else {
                                    arrayLabels = Tags[tag].array;
                                }
                                this.currentIndexTag = tag;
                                var arrayToDisplay = $.ui.autocomplete.filter(
                                        arrayLabels, extractLast(identifier, Tags[tag].regex));
                                if (Tags[tag].isCreatable && extractLast(identifier, Tags[tag].regex) !== "") {
                                    arrayToDisplay.push(extractLast(identifier, Tags[tag].regex));
                                }
                                response(arrayToDisplay);
                                found = true;
                            }
                            tag++;
                        }
                    }
                },
                focus: function () {
                    $('a[data-toggle="tooltip"]').each(function (idx, data) {
                        var direction = "top";
                        if (idx < 4)
                            direction = "bottom";

                        $(data).tooltip({
                            animated: 'fade',
                            placement: direction,
                            html: true
                        });

                        var parent = $(data).parent().parent();
                        if (parent.hasClass("ui-autocomplete")) {
                            parent.css("min-height", "120px"); // add height to do place to display tooltip. else overflow:auto hide tooltip
                        }
                    });
                    // prevent value inserted on focus
                    return false;
                },
                select: function (event, ui) {
                    //Get the part of the string we want (between the last % before our cursor and the cursor)
                    var stringToAnalyse = this.value.substring(0, this.selectionStart);
                    var identifier = stringToAnalyse.substring(stringToAnalyse.lastIndexOf("%"));
                    //Start iterating on Tags
                    var found = false;
                    var tag = 0;
                    while (tag < Tags.length && !found) {
                        //If we find our separator, we compute the output
                        if ((identifier.match(new RegExp(Tags[tag].regex)) || []).length > 0) {
                            // remove the current input
                            var beforeRegex = extractAllButLast(this.value.substring(0, this.selectionStart), Tags[tag].regex);
                            var afterCursor = this.value.substring(this.selectionStart, this.value.length);
                            // add the selected item and eventually the content to add
                            var value = Tags[tag].addBefore + ui.item.value + Tags[tag].addAfter;
                            //If it is the end of the variable, we automaticly add a % at the end of the line

                            this.value = beforeRegex + value + afterCursor;
                            this.setSelectionRange((beforeRegex + value).length, (beforeRegex + value).length);

                            found = true;
                        }
                        tag++;
                    }
                    // We trigger input to potentially display an image if there is one
                    $(this).trigger("input").trigger("change");
                    return false;
                },
                close: function (event, ui) {
                    val = $(this).val();
                    $(this).autocomplete("search", val); //keep autocomplete open by
                    //searching the same input again
                    return false;
                }
            });
}

/**
 * Function that allows us to retrieve all url parameters. Also, it groups values by parameter name. E.g., ?country=BE&country=CH
 * @param {type} param
 */
$.extend({
    getUrlVars: function () {
        var vars = [], hash;
        var hashes = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&');
        for (var i = 0; i < hashes.length; i++) {

            hash = hashes[i].split('=');
            if (hash.length > 1) {
                var values = [];

                if (vars.indexOf(hash[0]) > -1) { //hash already exists
                    values = vars[hash[0]];
                } else {
                    vars.push(hash[0]);
                }

                values.push(hash[1]);
                vars[hash[0]] = values;
            }
        }


        return vars;
    },
    getUrlVar: function (name) {
        return $.getUrlVars()[name];
    }
});

/**
 * Auxiliary function that opens the modal that allows user to view a picture.
 * @param {type} title
 * @param {type} pictureUrl
 * @returns {undefined}
 */
function showPicture(title, pictureUrl) {
    var doc = new Doc();
    $('#showGenericModalTitle').text(title);
    $('#closeShowGenericButton').text(doc.getDocLabel("page_global", "buttonClose"));

    $('#modalContent').empty();
    //set the translations
    $('#modalContent').append($('<img>').addClass("selectedPicture").attr("src", pictureUrl + "&h=400&w=800"));
    $('#modalContent').css("overflow", "auto");
    if ($("#btnFullPicture").length > 0) {
        $("#btnFullPicture").remove();
    }
    $('#modal-footer').prepend($('<button>').attr("id", "btnFullPicture").text("Full Picture").addClass("btn btn-default").click(function () {
        window.open(pictureUrl + "&r=true", "_blank");
    }));
    $('#showGenericModal').modal('show');
}

/**
 * Auxiliary function that opens the modal that allows user to view a textarea.
 * @param {type} title
 * @param {type} text
 * @param {type} fileUrl
 * @returns {undefined}
 */
function showTextArea(title, text, fileUrl) {
    var doc = new Doc();
    $('#showGenericModalTitle').text(title);
    $('#closeShowGenericButton').text(doc.getDocLabel("page_global", "buttonClose"));

    $('#modalContent').empty();
    //set the translations

    var jqxhr = $.get(fileUrl, "&autoContentType=N");
    $.when(jqxhr).then(function (data) {
        $('#modalContent').append($("<div>").addClass("form-group").append($("<pre id='previewContent'></pre>").addClass("form-control").attr("style", "min-height:15px").text(data)));
        //Highlight content on modal loading
        var editor = ace.edit($("#previewContent")[0]);
        editor.setTheme("ace/theme/chrome");
        var textMode = defineAceMode(editor.getSession().getDocument().getValue());
        editor.getSession().setMode(textMode);
        editor.setOptions({
            maxLines: Infinity
        });
        editor.setReadOnly(true);

        //Autoindentation
        var jsbOpts = {
            indent_size: 2
        };
        var session = editor.getSession();

        if (textMode.endsWith("json")) {
            session.setValue(js_beautify(session.getValue(), jsbOpts));
        } else if (textMode.endsWith("xml")) {
            session.setValue(html_beautify(session.getValue(), jsbOpts));
        }
    });

    $('#modal-footer #btnFullPicture').remove();
    $('#modal-footer').prepend($('<button>').attr("id", "btnFullPicture").text("Full File").addClass("btn btn-default").click(function () {
        window.open(fileUrl, "_blank");
    }));

    $('#showGenericModal').modal('show');
}

/**
 * Default options to apply when using linkify actions
 *
 * @see #safeLinkify(str, options)
 */
var DEFAULT_LINKIFY_OPTIONS = {
    validate: {
        email: function (value) {
            return false;
        },
        url: function (value) {
            return /^(http|ftp)s?:\/\//.test(value);
        }
    }
};

/**
 * Find any potential links from the given string and replace them by real HTML link
 *
 * @param str the given string to format
 * @param options (optional) options to use during find and replace process. If not given, then use #DEFAULT_LINKIFY_OPTIONS
 * @returns {*} a new string with any potential links replaced by the HTML link value
 * @see http://soapbox.github.io/linkifyjs/
 */
function safeLinkify(str, options) {
    return str === undefined ? str : str.linkify(options === undefined ? DEFAULT_LINKIFY_OPTIONS : options);
}

/**
 * Determine if a text is a Json
 * @param {type} str The String to check
 * @returns {Boolean} True if str is a valid JSON
 */
function isJson(str) {
    try {
        JSON.parse(str);
    } catch (e) {
        return false;
    }
    return true;
}

/**
 * Determine if a text is a HTML or XML
 * @param {type} str The string to check
 * @returns {Boolean} True if str is a HTML or a XML
 */
function isHTMLorXML(str) {
    const doc = new DOMParser().parseFromString(str, "text/html");
    return Array.from(doc.body.childNodes).some((node) => node.nodeType === 1);
}

/**
 * Return the Ace mode to use regarding the type of the text
 * @param {type} text The text that will help to determine the Ace mode
 * @returns {String} The Ace mode
 */
function defineAceMode(text) {
    if (isJson(text)) {
        return "ace/mode/json";
    } else if (isHTMLorXML(text)) {
        return "ace/mode/xml";
    }
}

/**
 * Do a JSON encoded HTTP POST call
 *
 * @param conf the same configuration as the Jquery's post method
 * @returns {undefined} void
 */
function jsonPost(conf) {
    conf.contentType = 'application/json;charset=UTF-8';
    $.post(conf);
}

function getSys() {
    const sel = document.getElementById("MySystem");
    return sel.options[sel.selectedIndex].value;
}

/********************************SELECT2 COMBO*******************************************/

/**
 * Do a JSON encoded HTTP POST call
 *
 * @param {json} tag object that will be formated
 * @returns {undefined} void
 */
function comboConfigTag_format(tag) {
    let markup = "<div class='select2-result-tag clearfix'>" +
            "<div class='select2-result-tag__title'>" + tag.tag + "</div>";

    markup += "<div class='select2-result-tag__statistics'>";
    if (tag.DateCreated) {
        markup += "<div class='select2-result-tag__detail'><i class='fa fa-calendar'></i> " + tag.DateCreated + "</div>";
    }
    if (tag.nbExeUsefull > 0) {
        markup += "<div class='select2-result-tag__detail'> " + tag.nbExeUsefull + " Exe(s)</div>";
        markup += "<div class='select2-result-tag__detail " + tag.ciResult + "'> " + tag.ciResult + "</div>";
    }
    if (tag.campaign) {
        markup += "<div class='select2-result-tag__detail'><i class='fa fa-list'></i> " + tag.campaign + "</div>";
    }
    markup += "</div>";
    markup += "</div>";

    return markup;
}

function comboConfigTag_formatSelection(tag) {
    if (!isEmpty(tag.campaign)) {
        return `${tag.id}[${tag.campaign}]`;
    }
    return tag.id;
}


function getComboConfigTag() {

    return {
        ajax: {
            url: "ReadTag?iSortCol_0=0&sSortDir_0=desc&sColumns=id,tag,campaign,description&iDisplayLength=30" + getUser().defaultSystemsQuery,
            dataType: 'json',
            delay: 250,
            data: function (params) {
                return {
                    sSearch: params.term, // search term
                    iDisplayStartPage: params.page
                };
            },
            processResults: function (data, params) {
                params.page = params.page || 1;
                return {
                    results: $.map(data.contentTable, function (obj) {
                        return {
                            id: obj.tag,
                            text: obj.tag,
                            tag: obj.tag,
                            description: obj.description,
                            campaign: obj.campaign,
                            DateCreated: obj.DateCreated,
                            nbExeUsefull: obj.nbExeUsefull,
                            ciResult: obj.ciResult
                        };
                    }),
                    pagination: {
                        more: (params.page * 30) < data.iTotalRecords
                    }
                };
            },
            cache: true
        },
        escapeMarkup: function (markup) {
            return markup;
        }, // let our custom formatter work
        minimumInputLength: 0,
        templateResult: comboConfigTag_format, // omitted for brevity, see the source of this page
        templateSelection: comboConfigTag_formatSelection // omitted for brevity, see the source of this page
    };
}


function getComboConfigService() {
    return {
        ajax: {
            url: "ReadAppService?iSortCol_0=0&sSortDir_0=asc&sColumns=service,type,method,description&iDisplayLength=30&sSearch_0=",
            dataType: 'json',
            delay: 250,
            data: function (params) {
                params.page = params.page || 1;
                return {
                    sSearch: params.term, // search term
                    iDisplayStart: (params.page * 30) - 30
                };
            },
            processResults: function (data, params) {
                params.page = params.page || 1;
                return {
                    results: $.map(data.contentTable, function (obj) {
                        return {id: obj.service, text: obj.service};
                    }),
                    pagination: {
                        more: (params.page * 30) < data.iTotalRecords
                    }
                };
            },
            cache: true,
            allowClear: true
        },
//                tags: true,
        width: "100%",
        minimumInputLength: 0
    };
}

function getComboConfigTest() {
    return {
        ajax: {
            url: "ReadTest?iSortCol_0=0&sSortDir_0=asc&sColumns=test&iDisplayLength=30&sSearch_0=",
            dataType: 'json',
            delay: 250,
            data: function (params) {
                params.page = params.page || 1;
                return {
                    sSearch: params.term, // search term
                    iDisplayStart: (params.page * 30) - 30
                };
            },
            processResults: function (data, params) {
                params.page = params.page || 1;
                return {
                    results: $.map(data.contentTable, function (obj) {
                        return {id: obj.test, text: obj.test};
                    }),
                    pagination: {
                        more: (params.page * 30) < data.iTotalRecords
                    }
                };
            },
            cache: true,
            allowClear: true
        },
        // Allow entry that does not exist in the list..
        tags: true,
        width: "100%",
        minimumInputLength: 0
    };
}

function getComboConfigApplication(editable) {
    return {
        ajax: {
            url: "ReadApplication",
            dataType: 'json',
            delay: 250,
            data: function (params) {
                params.page = params.page || 1;
                return {
                    sSearch: params.term, // search term
                    iDisplayStart: (params.page * 30) - 30
                };
            },
            processResults: function (data, params) {
                params.page = params.page || 1;
                return {
                    results: $.map(data.contentTable, function (obj) {
                        return {id: obj.application, text: obj.application, type: obj.type};
                    }),
                    pagination: {
                        more: (params.page * 30) < data.iTotalRecords
                    }
                };
            },
            cache: true,
            allowClear: true
        },
        tags: editable,
        width: "100%",
        minimumInputLength: 0,
        templateResult: comboConfigApplication_format // omitted for brevity, see the source of this page
    };
}

function comboConfigApplication_format(application) {
    var doc = new Doc();
    var color = "labelGreen";
    var appType = "NEW APPLICATION ? : CLICK HERE TO CREATE IT";

    if (!isEmpty(application.type)) {
        color = "labelBlue";
        appType = doc.getDocLabel("comboApplicationType", application.type);
        return $('<span name="appNameLabel">' + application.id + ' <img id="AppLogo"  class="" style="height:20px; overflow:hidden; text-overflow:clip; border: 0px; padding:0; margin:0; margin-left: 10px" src="./images/logoapp-' + application.type + '.png"></img></span>');
    } else {
        return $('<span name="appNameLabel">' + application.id + ' <span name="appTypeLabel" class="label ' + color + '" style="margin-left:10px;margin-bottom:0px;height:30px;border-radius:30px;padding:8px">' + appType + '</span></span>');
    }
}



function getBugIdList(data, appUrl) {
    let link = "";
    $.each(data, function (_, obj) {
        link = link + getBugIdRow(obj.id, obj.desc, obj.url, obj.act, appUrl);
    });
    return link;
}


function getBugIdRow(id, desc, url, act, appUrl) {
    let bugUrl = "";
    let link = "";

    if (!isEmpty(url)) {
        bugUrl = url;
    } else {
        if (!isEmpty(appUrl)) {
            bugUrl = appUrl.replace(/%BUGID%/g, id);
        }
    }
    if (act) {
        if (!isEmpty(bugUrl)) {
            link = link + '<a target="_blank" href="' + bugUrl + '">' + id;
            if (desc !== "") {
                link = link + " - " + desc;
            }
            link = link + "</a><br>";

        } else {
            link = link + '' + id;
            if (desc !== "") {
                link = link + " - " + desc;
            }
            link = link + "<br>";

        }
    }
    return link;
}

function get_Color_fromindex(index) {
    const colors = ['#3366FF', '#3498DB', 'blueviolet', 'midnightblue', 'salmon', 'olive', 'teal', 'grey', '#cad3f1', 'yellow', 'magenta', 'lightgreen', 'lightgrey', 'coral', 'violet', '#B91D0D', 'olive'];
    return colors[index % colors.length];
}


function saveHistory(tce, historyEntry, maxEntries) {
    let entryList = localStorage.getItem(historyEntry);
    entryList = JSON.parse(entryList);
    if (entryList === null) {
        entryList = [];
        entryList.push(tce);
        localStorage.setItem(historyEntry, JSON.stringify(entryList));
    } else {
        // Remove the entries that has the same id in order to avoid duplicates.
        for (const item in entryList) {
            if (tce.id === entryList[item].id) {
                entryList.splice(item, 1);
            }
        }
        entryList.push(tce);
        if (entryList.length > maxEntries) {
            entryList.shift();
        }

        localStorage.setItem(historyEntry, JSON.stringify(entryList));
    }

}


function generateUUID() { // Public Domain/MIT
    let d = new Date().getTime();//Timestamp
    let d2 = ((typeof performance !== 'undefined') && performance.now && (performance.now() * 1000)) || 0;//Time in microseconds since page-load or 0 if unsupported
    return 'xxxxxxxx-xxxx-xxxx-yxxx-xxxxxxxxxxxx-xxxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
        let r = Math.random() * 16;//random number between 0 and 16
        if (d > 0) {//Use timestamp until depleted
            r = (d + r) % 16 | 0;
            d = Math.floor(d / 16);
        } else {//Use microseconds since page-load if supported
            r = (d2 + r) % 16 | 0;
            d2 = Math.floor(d2 / 16);
        }
        return (c === 'x' ? r : (r & 0x3 | 0x8)).toString(16);
    });
}


function cleanErratum(oldValue) {
    if (oldValue.startsWith('erratum=') && oldValue.includes(",")) {
        return oldValue.split(',')[0] + ",[HTML-SOURCE-CONTENT]";
    }
    return oldValue;
}

function refreshPopoverDocumentation(containerid) {
    //As the executors list is dynamically generated after the global popover initialization, need to init popover again on executors list only.
    let popDefinition = {};
    popDefinition.placement = "auto";
    popDefinition.container = "#" + containerid;
    $('#' + containerid + ' [data-toggle="popover"]').popover(popDefinition);
}

