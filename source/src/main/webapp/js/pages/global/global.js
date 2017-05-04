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
/*
 * Retrieves the plugin script that allows the generation of a loader.
 */
//$.getScript("js/jquery.blockUI.js");

function handleErrorAjaxAfterTimeout(result) {
    var doc = new Doc();

    if (result.readyState === 4 && result.status === 200) {
        $(location).prop("pathname", $(location).prop("pathname"));
        $(location).prop("search", $(location).prop("search"));
    } else {
        var localMessage = new Message("danger", doc.getDocLabel("page_global", "unexpected_error_message"));
        showMessageMainPage(localMessage);
    }

}

/***
 * Returns a label depending on the type of entry
 * @param {type} type - type selected
 * @returns {String} - label associated with the type
 */
function getSubDataLabel(type) {
    var doc = getDoc();
    var docTestdatalibdata = doc.testdatalibdata;
    var labelEntry = "Entry";
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
 * @returns {void}
 */
function displayInvariantList(selectName, idName, forceReload, defaultValue, addValue1, asyn) {
    // Adding the specific value when defined.
    if (addValue1 !== undefined) {
        $("[name='" + selectName + "']").append($('<option></option>').text(addValue1).val(addValue1));
    }

//    console.debug("display Invariant " + idName + " " + forceReload);
    if (forceReload === undefined) {
        forceReload = true;
    }

    var async = true;
    if (asyn != undefined) {
        async = asyn
    }

    var cacheEntryName = idName + "INVARIANT";
    if (forceReload) {
//        console.debug("Purge " + cacheEntryName);
        sessionStorage.removeItem(cacheEntryName);
    }
    var list = JSON.parse(sessionStorage.getItem(cacheEntryName));
    var select = $("<select></select>").addClass("form-control input-sm");

    if (list === null) {
        $.ajax({
            url: "FindInvariantByID",
            data: {idName: idName},
            async: async,
            success: function (data) {
                list = data;
                sessionStorage.setItem(cacheEntryName, JSON.stringify(data));
                for (var index = 0; index < list.length; index++) {
                    var item = list[index].value;
                    var desc = list[index].value + " - " + list[index].description;

                    $("[name='" + selectName + "']").append($('<option></option>').text(desc).val(item));
                }
                if (defaultValue !== undefined) {
                    $("[name='" + selectName + "']").val(defaultValue);
                }
            }
        });
    } else {
        for (var index = 0; index < list.length; index++) {
            var item = list[index].value;
            var desc = list[index].value + " - " + list[index].description;

            $("[name='" + selectName + "']").append($('<option></option>').text(desc).val(item));
        }
        if (defaultValue !== undefined) {
            $("[name='" + selectName + "']").val(defaultValue);
        }
    }
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
 * @returns {void}
 */
function displayAppServiceList(selectName, defaultValue) {
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
 * @returns {void}
 */
function displayApplicationList(selectName, system, defaultValue) {
    var myData = "";
    if ((system !== "") && (system !== undefined) && (system !== null)) {
        myData = "system=" + system;
    }
    if (defaultValue !== undefined && defaultValue !== null) {
        $("[name='" + selectName + "']").append($('<option></option>').text(defaultValue).val(defaultValue));
    }
    $.when($.getJSON("ReadApplication", myData)).then(function (data) {
        for (var option in data.contentTable) {
            $("[name='" + selectName + "']").append($('<option></option>').text(data.contentTable[option].application + " - " + data.contentTable[option].description).val(data.contentTable[option].application));
        }

        if (defaultValue !== undefined && defaultValue !== null) {
            $("[name='" + selectName + "']").val(defaultValue);
        }
    });
}

/**
 * Method that display a combo box in all the selectName tags with the value retrieved from the Project list
 * @param {String} selectName value name of the select tag in the html
 * @param {String} defaultValue to be selected
 * @returns {void}
 */
function displayProjectList(selectName, defaultValue) {
    $.when($.getJSON("ReadProject", "")).then(function (data) {
        $("[name='" + selectName + "']").append($('<option></option>').text("NONE").val(""));
        for (var option in data.contentTable) {
            $("[name='" + selectName + "']").append($('<option></option>').text(data.contentTable[option].idProject + " - " + data.contentTable[option].description).val(data.contentTable[option].idProject));
        }

        if (defaultValue !== undefined) {
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

//    console.debug("display Invariant Build Rev " + system + " " + forceReload);
    if (forceReload === undefined) {
        forceReload = true;
    }

    var cacheEntryName = "BRINVARIANT" + "-" + level + "-" + system;
    if (forceReload) {
//        console.debug("Purge " + cacheEntryName);
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
 * Method that display a combo box in all the selectName tags with the value retrieved from the Environment list
 * @param {String} selectName value name of the select tag in the html
 * @param {String} system name of the system
 * @param {String} defaultValue to be selected
 * @returns {void}
 */
function displayUniqueEnvList(selectName, system, defaultValue) {
    $.when($.getJSON("ReadCountryEnvParam", "unique=true&system=" + system)).then(function (data) {
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
 * @param {boolean} forceReload true if we want to force the reload on cache from the server
 * @param {boolean} notAsync true if we dont want to have Async ajax
 */
function getSelectInvariant(idName, forceReload, notAsync) {
    var cacheEntryName = idName + "INVARIANT";
    if (forceReload) {
//        console.debug("Purge " + cacheEntryName);
        sessionStorage.removeItem(cacheEntryName);
    }
    var async = true;
    if (notAsync) {
        async = false;
    }
    var list = JSON.parse(sessionStorage.getItem(cacheEntryName));
    var select = $("<select></select>").addClass("form-control input-sm");

    if (list === null) {
        $.ajax({
            url: "FindInvariantByID",
            data: {idName: idName},
            async: async,
            success: function (data) {
                list = data;
                sessionStorage.setItem(cacheEntryName, JSON.stringify(data));
                for (var index = 0; index < list.length; index++) {
                    var item = list[index].value;

                    select.append($("<option></option>").text(item).val(item));
                }
            }
        });
    } else {
        for (var index = 0; index < list.length; index++) {
            var item = list[index].value;

            select.append($("<option></option>").text(item).val(item));
        }
    }

    return select;
}

/**
 * This method will return the combo list of TestBattery.
 * It will load the values from the sessionStorage cache of the browser
 * when available, if not available, it will get it from the server and save
 * it on local cache.
 * The forceReload boolean can force the refresh of the list from the server.
 * @param {boolean} forceReload true if we want to force the reload on cache from the server
 * @param {boolean} notAsync true if we dont want to have Async ajax
 */
function getSelectTestBattery(forceReload, notAsync) {
    var cacheEntryName = "TESTBATTERY";
    if (forceReload) {
//        console.debug("Purge " + cacheEntryName);
        sessionStorage.removeItem(cacheEntryName);
    }
    var async = true;
    if (notAsync) {
        async = false;
    }
    var list = JSON.parse(sessionStorage.getItem(cacheEntryName));
    var select = $("<select></select>").addClass("form-control input-sm");

    if (list === null) {
        $.ajax({
            url: "ReadTestBattery",
            async: async,
            success: function (data) {
                list = data.contentTable;
                sessionStorage.setItem(cacheEntryName, JSON.stringify(data.contentTable));
                for (var index = 0; index < list.length; index++) {
                    var item = list[index].testbattery + " - " + list[index].description;
                    select.append($("<option></option>").text(item).val(list[index].testbattery));
                }
            }
        });
    } else {
        for (var index = 0; index < list.length; index++) {
            var item = list[index].testbattery + " - " + list[index].description;

            select.append($("<option></option>").text(item).val(list[index].testbattery));
        }
    }

    return select;
}

/**
 * This method will return the combo list of TestBattery.
 * It will load the values from the sessionStorage cache of the browser
 * when available, if not available, it will get it from the server and save
 * it on local cache.
 * The forceReload boolean can force the refresh of the list from the server.
 * @param {boolean} forceReload true if we want to force the reload on cache from the server
 * @param {boolean} notAsync true if we dont want to have Async ajax
 */
function getSelectLabel(forceReload, notAsync) {
    var cacheEntryName = "TESTBATTERY";
    if (forceReload) {
//        console.debug("Purge " + cacheEntryName);
        sessionStorage.removeItem(cacheEntryName);
    }
    var async = true;
    if (notAsync) {
        async = false;
    }
    var list = JSON.parse(sessionStorage.getItem(cacheEntryName));
    var select = $("<select></select>").addClass("form-control input-sm");

    if (list === null) {
        $.ajax({
            url: "ReadTestBattery",
            async: async,
            success: function (data) {
                list = data.contentTable;
                sessionStorage.setItem(cacheEntryName, JSON.stringify(data.contentTable));
                for (var index = 0; index < list.length; index++) {
                    var item = list[index].testbattery + " - " + list[index].description;
                    select.append($("<option></option>").text(item).val(list[index].testbattery));
                }
            }
        });
    } else {
        for (var index = 0; index < list.length; index++) {
            var item = list[index].testbattery + " - " + list[index].description;

            select.append($("<option></option>").text(item).val(list[index].testbattery));
        }
    }

    return select;
}

function getSelectApplication(system, forceReload) {
    var cacheEntryName = system + "INVARIANT";
    if (forceReload) {
//        console.debug("Purge " + cacheEntryName);
        sessionStorage.removeItem(cacheEntryName);
    }
    var list = JSON.parse(sessionStorage.getItem(cacheEntryName));
    var select = $("<select></select>").addClass("form-control input-sm");

    if (list === null) {
        $.ajax({
            url: "ReadApplication",
            data: {system: system},
            async: true,
            success: function (data) {
                list = data.contentTable;
                sessionStorage.setItem(cacheEntryName, JSON.stringify(list));
                for (var index = 0; index < list.length; index++) {
                    var item = list[index].application;

                    select.append($("<option></option>").text(item).val(item));
                }
            }
        });
    } else {
        for (var index = 0; index < list.length; index++) {
            var item = list[index].application;

            select.append($("<option></option>").text(item).val(item));
        }
    }

    return select;
}

function getSelectDeployType(forceReload) {
    var cacheEntryName = "DEPLOYTYPE";
    if (forceReload) {
//        console.debug("Purge " + cacheEntryName);
        sessionStorage.removeItem(cacheEntryName);
    }
    var list = JSON.parse(sessionStorage.getItem(cacheEntryName));
    var select = $("<select></select>").addClass("form-control input-sm");

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
 * @param {string} param value of the parameter to get ex : "cerberus_homepage_nbbuildhistorydetail"
 * @param {string} sys system that will be used to get the parameter
 * @param {boolean} forceReload true if we want to force the reload on cache from the server
 * 
 */
function getParameter(param, sys, forceReload) {
    var result;
    var cacheEntryName = "PARAMETER_" + param;
    if (forceReload) {
        sessionStorage.removeItem(cacheEntryName);
    }
    var system = sys != undefined ? "&system=" + sys : "";
    var parameter = JSON.parse(sessionStorage.getItem(cacheEntryName));
    if (parameter === null) {
        $.ajax({
            url: "ReadParameter?param=" + param + system,
            data: {},
            async: false,
            success: function (data) {
                sessionStorage.setItem(cacheEntryName, JSON.stringify(data.contentTable))
                result = data.contentTable;
            }
        });
    } else {
        result = parameter;
    }
    return result;
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
        elementAlert.fadeOut();
    }
}

/**
 * Clears the messages added in the main page.
 */
function clearResponseMessageMainPage() {
    $("#mainAlert").removeClass("alert-success");
    $("#mainAlert").removeClass("alert-danger");
    $("#alertDescription").html("");
    $("#mainAlert").fadeOut();
}

/**
 * Method that shows a message
 * @param {type} obj - object containing the message and the message type
 * @param {type} dialog - dialog where the message should be displayed; if null then the message
 * is displayed in the main page.
 */
function showMessage(obj, dialog) {
    var code = getAlertType(obj.messageType);

    if (code !== "success" && dialog !== undefined && dialog !== null) {
        //shows the error message in the current dialog
        var elementAlert = dialog.find("div[id*='DialogMessagesAlert']");
        var elementAlertDescription = dialog.find("span[id*='DialogAlertDescription']");

        elementAlertDescription.html(obj.message);
        elementAlert.addClass("alert-" + code);
        elementAlert.fadeIn();
    } else {
        //shows the message in the main page
        showMessageMainPage(code, obj.message);
    }

    /*if(dialog !== null && obj.messageType==="success"){
     jQuery(dialog).dialog('close');
     }*/
}

/**
 * Method that allows us to append a message in an already existing alert.
 * @param {type} obj  - object containing the message and the message type
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
 * @param {type} type - type of message: success, info, ...
 * @param {type} message - message to show
 */
function showMessageMainPage(type, message) {
    $("#mainAlert").addClass("alert-" + type);
    $("#alertDescription").html(message);
    $("#mainAlert").fadeIn();
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
 * @param {type} handlerClickOk - method triggered when the "ok" is clicked
 * @param {type} title - title to be displayed
 * @param {type} message -  message to be displayed
 * @param {type} hiddenField1 -hidden value that can be added to the confirmation modal. Useful when we want to delete an item, and we can specify it in this field.
 * @param {type} hiddenField2 -hidden value that can be added to the confirmation modal. Useful when we want to delete an item, and we can specify it in this field.
 * @param {type} hiddenField3 -hidden value that can be added to the confirmation modal. Useful when we want to delete an item, and we can specify it in this field.
 * @param {type} hiddenField4 -hidden value that can be added to the confirmation modal. Useful when we want to delete an item, and we can specify it in this field.
 * @returns {undefined}
 */
function showModalConfirmation(handlerClickOk, title, message, hiddenField1, hiddenField2, hiddenField3, hiddenField4) {
    setDataConfirmationModal(title, message, hiddenField1, hiddenField2, hiddenField3, hiddenField4);
    $('#confirmationModal #confirmOk').unbind("click").click(handlerClickOk);
    clearResponseMessageMainPage();
    $('#confirmationModal').modal('show');
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
 * @param {type} defineLenghtMenu - allows the defintion of the select with the number or rows that should be displayed
 * @returns {TableConfigurationsClientSide}
 */
function TableConfigurationsClientSide(divId, data, aoColumnsFunction, defineLenghtMenu) {
    this.divId = divId;
    this.aoColumnsFunction = aoColumnsFunction;
    this.aaData = data;
    if (defineLenghtMenu) {
        this.lengthMenu = [10, 25, 50, 100];
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
 * @returns {TableConfigurationsServerSide}
 */
function TableConfigurationsServerSide(divId, ajaxSource, ajaxProp, aoColumnsFunction, aaSorting, lengthMenu) {
    this.divId = divId;
    this.aoColumnsFunction = aoColumnsFunction;
    this.ajaxSource = ajaxSource;
    this.ajaxProp = ajaxProp;

    this.processing = true;
    this.serverSide = true;
    if (lengthMenu === undefined) {
        this.lengthMenu = [10, 25, 50, 100];
    } else {
        this.lengthMenu = lengthMenu;
    }
    this.lengthChange = true;
    //not mandatory properties, and default values
    this.searchText = "";
    this.searchMenu = "";
    this.tableWidth = "1500px";
    this.displayLength = 10;
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
            showMessageMainPage(type, response.message);
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

    showMessageMainPage(type, message);
}

/***
 * Creates a datatable that is server-side processed.
 * @param {type} tableConfigurations set of configurations that define how data is retrieved and presented
 * @param {Function} callbackFunction callback function to be called after table creation
 * @param {String} objectWaitingLayer object that will report the waiting layer when external calls. Ex : #logViewer
 * @param {Array} filtrableColumns array of parameter name that can trigger filter on columns
 * @param {Boolean} checkPermisson boolean that define if user permission need to be checked
 * @param {type} userCallbackFunction
 * @param {Function} createdRowCallback callback function to be called after each row
 * @return {Object} Return the dataTable object to use the api
 */
function createDataTableWithPermissions(tableConfigurations, callbackFunction, objectWaitingLayer, filtrableColumns, checkPermissions, userCallbackFunction, createdRowCallback) {

    /**
     * Define datatable config with tableConfiguration object received 
     */
    var configs = {};
    var domConf = 'RCB<"clear">lf<"pull-right"p>rti<"marginTop5">';
    if (!tableConfigurations.showColvis) {
        domConf = 'lf<"pull-right"p>rti<"marginTop5">';
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
                    //console.debug(JSON.parse(userPref['DataTables_' + settings.sInstance + '_' + location.pathname]));
                    return JSON.parse(userPref['DataTables_' + settings.sInstance + '_' + location.pathname]);
                }
            }
        };
        configs["colReorder"] = tableConfigurations.colreorder ? {
            fnReorderCallback: function () {
                $("#" + tableConfigurations.divId).DataTable().ajax.reload();
            }
        } : false;
        if (filtrableColumns !== undefined) {
            configs["fnServerParams"] = function (aoData) {
                var filters = generateFiltersOnMultipleColumns(tableConfigurations.divId, filtrableColumns);
                for (var f = 0; f < filters.length; f++) {
                    aoData.push(filters[f]);
                }
                aoData.push({name: "iSortCol_0", value: configs["aaSorting"][0][0]});
                aoData.push({name: "sSortDir_0", value: configs["aaSorting"][0][1]});
            };
        }
        configs["fnServerData"] = function (sSource, aoData, fnCallback, oSettings) {
            var objectWL = $(objectWaitingLayer);
            if (objectWaitingLayer !== undefined) {
                showLoader(objectWL);
            }
            oSettings.jqXHR = $.ajax({
                "dataType": 'json',
                "type": "POST",
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
        configs["data"] = tableConfigurations.aaData;
    }
    var oTable = $("#" + tableConfigurations.divId).DataTable(configs);

    var doc = new Doc();
    var showHideButtonLabel = doc.getDocLabel("page_global", "btn_showHideColumns");
    var showHideButtonTooltip = doc.getDocLabel("page_global", "tooltip_showHideColumns");
    var saveTableConfigurationButtonLabel = doc.getDocLabel("page_global", "btn_savetableconfig");
    var saveTableConfigurationButtonTooltip = doc.getDocDescription("page_global", "tooltip_savetableconfig");
    var restoreFilterButtonLabel = doc.getDocLabel("page_global", "btn_restoreuserpreferences");
    var restoreFilterButtonTooltip = doc.getDocDescription("page_global", "tooltip_restoreuserpreferences");
    if (tableConfigurations.showColvis) {
        //Display button show/hide columns and Save table configuration
        $("#" + tableConfigurations.divId + "_wrapper #saveTableConfigurationButton").remove();
        $("#" + tableConfigurations.divId + "_wrapper #restoreFilterButton").remove();
        $("#" + tableConfigurations.divId + "_wrapper")
                .find("[class='dt-buttons btn-group']").removeClass().addClass("pull-right").find("a").attr('id', 'showHideColumnsButton').removeClass()
                .addClass("btn btn-default").attr("data-toggle", "tooltip").attr("title", showHideButtonTooltip).click(function () {
            //$("#" + tableConfigurations.divId + " thead").empty();
        }).html("<span class='glyphicon glyphicon-cog'></span> " + showHideButtonLabel);
        $("#" + tableConfigurations.divId + "_wrapper #showHideColumnsButton").parent().before(
                $("<button id='saveTableConfigurationButton'></button>").addClass("btn btn-default pull-right").append("<span class='glyphicon glyphicon-floppy-save'></span> " + saveTableConfigurationButtonLabel)
                .attr("data-toggle", "tooltip").attr("title", saveTableConfigurationButtonTooltip).click(function () {
            updateUserPreferences(objectWaitingLayer);
        })
                );
        $("#" + tableConfigurations.divId + "_wrapper #saveTableConfigurationButton").before(
                $("<button id='restoreFilterButton'></button>").addClass("btn btn-default pull-right").append("<span class='glyphicon glyphicon-floppy-open'></span> " + restoreFilterButtonLabel)
                .attr("data-toggle", "tooltip").attr("title", restoreFilterButtonTooltip).click(function () {
            location.reload();
        })
                );
    }
    $("#" + tableConfigurations.divId + "_length select[name='" + tableConfigurations.divId + "_length']").addClass("form-control input-sm");
    $("#" + tableConfigurations.divId + "_length select[name='" + tableConfigurations.divId + "_length']").css("display", "inline");

    $("#" + tableConfigurations.divId + "_filter input[type='search']").addClass("form-control form-control input-sm");

    //Build the Message that appear when filter is fed
    var showFilteredColumnsAlertMessage = "<div id='filterAlertDiv' class='col-sm-12 alert alert-warning' style='padding:0px'><div class='col-sm-11' id='activatedFilters'></div><div class='col-sm-1  filterMessageButtons'><span id='clearFilterButton' data-toggle='tooltip' title='Clear filters' class='pull-right glyphicon glyphicon-remove-sign'  style='cursor:pointer;padding:15px'></span></div>";
    if ($("#" + tableConfigurations.divId + "_paginate").length !== 0) {
        $("#" + tableConfigurations.divId + "_paginate").parent().after($(showFilteredColumnsAlertMessage).hide());
    } else {
        $("#showHideColumnsButton").parent().after($(showFilteredColumnsAlertMessage).hide());
    }
    $("#" + tableConfigurations.divId + "_length").addClass("marginBottom10").addClass("width80").addClass("pull-left");
    $("#" + tableConfigurations.divId + "_filter").addClass("marginBottom10").addClass("width150").addClass("pull-left");

    return oTable;
}
/**
 * Function called after data loaded in tables
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
///////////////////////////////////////////////////////////////////////////////
/////////////////////SEARCH/FILTERS////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
/**
 * Function that allow to reset the filter selected
 * @param {type} oTable datatable object
 * @param {columnNumber} columnNumber. If empty, reset all the column's filter
 * @param {clearGlobalSearch} boolean. true if global search should be cleared.
 * @returns {undefined}
 */
function resetFilters(oTable, columnNumber, clearGlobalSearch) {
    var oSettings = oTable.fnSettings();
    for (iCol = 0; iCol < oSettings.aoPreSearchCols.length; iCol++) {
        /**
         * if columnNumber defined, clear that column search
         */
        if (columnNumber !== undefined) {
            if (parseInt(columnNumber) === iCol) {
                oSettings.aoPreSearchCols[iCol].sSearch = '';
            }
            /**
             * else clear all columns
             */
        } else {
            oSettings.aoPreSearchCols[iCol].sSearch = '';
        }
    }
    /**
     * if clearGlobalSearch, clear search
     */
    if (clearGlobalSearch) {
        oSettings.oPreviousSearch.sSearch = '';
    }
    oTable.fnDraw();
}

/**
 * Function that allow to filter column on specific value
 * @param {type} tableId >Id of the datatable
 * @param {type} column > Name of the column
 * @param {type} value > Value to filter
 * @returns {undefined}
 */
function filterOnColumn(tableId, column, value) {
    var oTable = $('#' + tableId).dataTable();
    resetFilters(oTable);
    var oSettings = oTable.fnSettings();
    for (iCol = 0; iCol < oSettings.aoPreSearchCols.length; iCol++) {
        if (oSettings.aoColumns[iCol].data === column) {
            oTable.api().column(iCol).search(value);
        }
    }
    oTable.fnDraw();
}

/**
 * Function that generate Array that will be send do table to define filter
 * @param {type} tableId > Id of the datatable
 * @param {type} searchColums > Array of columns 
 * @returns {undefined}
 */
function generateFiltersOnMultipleColumns(tableId, searchColumns) {
    var filterConfiguration = Array();
    /**
     * Loop on searchColumns and get Parameter values >> Build an array of object
     */
    var searchArray = new Array;
    for (var searchColumn = 0; searchColumn < searchColumns.length; searchColumn++) {
        var param = GetURLParameters(searchColumns[searchColumn]);
        var searchObject = {
            param: searchColumns[searchColumn],
            values: param};
        searchArray.push(searchObject);
    }
    /**
     * Apply the filter to the table
     */
    var oTable = $('#' + tableId).dataTable();
    //resetFilters(oTable);
    var oSettings = oTable.fnSettings();
    for (iCol = 0; iCol < oSettings.aoPreSearchCols.length; iCol++) {
        for (sCol = 0; sCol < searchArray.length; sCol++) {
            if (oSettings.aoColumns[iCol].data === searchArray[sCol].param
                    && searchArray[sCol].values.length !== 0) {
                var filter = {
                    name: "sSearch_" + iCol,
                    value: searchArray[sCol].values.join(", ")};
                filterConfiguration.push(filter);
            }
        }

    }
    return (filterConfiguration);
}

/**
 * Function that apply filters on given datatable's columns
 *
 * Values can either be contained into the given columns, or retrieved from the current URL.
 *
 * @param tableId the datatable from which filter columns
 * @param searchColumns the array of columns to filter. Column can be either an object {param, values}, or simply the name of the column (param)
 * @param fromURL if values are to be retrived from the current URL
 */
function applyFiltersOnMultipleColumns(tableId, searchColumns, fromURL) {
    // Get or create the search array
    var searchArray = searchColumns;
    if (fromURL) {
        searchArray = [];
        for (var searchColumn = 0; searchColumn < searchColumns.length; searchColumn++) {
            var param = GetURLParameters(searchColumns[searchColumn]);
            var searchObject = {
                param: searchColumns[searchColumn],
                values: param};
            searchArray.push(searchObject);
        }
    }

    // Apply filter on table
    var oTable = $('#' + tableId).dataTable();
    resetFilters(oTable);
    var oSettings = oTable.fnSettings();
    for (iCol = 0; iCol < oSettings.aoPreSearchCols.length; iCol++) {
        for (sCol = 0; sCol < searchArray.length; sCol++) {
            if (oSettings.aoColumns[iCol].data === searchArray[sCol].param) {
                oTable.api().column(iCol).search(searchArray[sCol].values);
            }
        }

    }
    oTable.fnDraw();
}

/**
 * Function that allow to clear filter individually
 * @param {type} tableId >> ID of the datatable
 * @param {type} columnNumber >> Number of the column
 * @param {type} clearGlobalSearch >> boolean. true if global search should be cleared.
 * @returns {undefined}
 */
function clearIndividualFilter(tableId, columnNumber, clearGlobalSearch) {
    var oTable = $('#' + tableId).dataTable();
    resetFilters(oTable, columnNumber, clearGlobalSearch);
}

/**
 * Function that allow display of individual column searching on datatable
 * @param {type} tableId >> ID of the datatable
 * @param {type} contentUrl >> URL of the service to get content of each columns
 * @param {type} oSettings >> datatable settings
 * @returns {undefined}
 */
function displayColumnSearch(tableId, contentUrl, oSettings) {
    //Hide filtered alert message displayed when filtered column
    $("#" + tableId + "_wrapper #filterAlertDiv").hide();
    //Load the table
    var table = $("#" + tableId).dataTable().api();

    var columnVisibleIndex = 0;//Used to Match visible column with columns available
    var doc = new Doc();

    //Start building the Alert Message for filtered column information
    //TODO : Replace with data from doc table
    var filteredInformation = new Array();
    filteredInformation.push("<div class=\"col-sm-2 alert-warning\" style=\"margin-bottom:0px; padding:15px\">Showing information filtering : </div>");
    if (table.search() !== "") {
        filteredInformation.push("<div class=\"col-sm-2 alert alert-warning\" style=\"margin-bottom:0px\">");
        filteredInformation.push("<span id='clearFilterButtonGlobal' onclick='clearIndividualFilter(\"" + tableId + "\", null, true)'  data-toggle='tooltip' title='Clear global filter' class='glyphicon glyphicon-remove-sign pull-right'  style='cursor:pointer;'></span>");
        filteredInformation.push("<div data-toggle='tooltip' data-html='true' title=" + table.search() + " style=\"margin-bottom:0px;white-space: nowrap;overflow: hidden;text-overflow: ellipsis;\">");
        filteredInformation.push("<strong>all columns</strong> containing <br>[" + table.search() + "]</div></div>");
        filteredInformation.push(" ");
    }

    //Get the column name in the right order
    var orderedColumns = [];
    $.each(oSettings.aoColumns, function (i, columns) {
        orderedColumns.push(columns.sName);
    });

    // Delete and Build a new tr in the header to display the editable mark
    //So first delete in case on page reload
    $("#" + tableId + "_wrapper #filterHeader").remove();
    $("#" + tableId + '_wrapper .dataTables_scrollBody').find("#filterHeader").remove();

    //Set the table with relative position position for the editable box.
    $("#" + tableId + "_wrapper").attr("style", "position: relative");
    //Remove the relative position of the header
    $("#" + tableId + '_wrapper [class="dataTables_scrollHead ui-state-default"]').attr("style", "overflow: hidden; border: 0px; width: 100%;");
    //Then create a th tag for each columns
    $("#" + tableId + '_wrapper .dataTables_scrollHeadInner table thead').append('<tr id="filterHeader"></tr>');
    $("#" + tableId + '_wrapper .dataTables_scrollHeadInner table thead tr th').each(function () {
        $("#" + tableId + "_wrapper #filterHeader").append("<th name='filterColumnHeader'></th>");
    });

//Iterate on all columns
    $.each(orderedColumns, function (index, value) {
        var colIndex = table.ajax.params().sColumns.split(',').indexOf(value);

        //Get the value from storage (To display specific string if already filtered)
        var json_obj = JSON.stringify(table.ajax.params());
        var columnSearchValues = JSON.parse(json_obj)["sSearch_" + colIndex].split(',');

        //Get the column names (for title display)
        var title = value;

        //Build the specific tooltip for filtered columns and the tooltip for not filtered columns
        var emptyFilter = doc.getDocLabel("page_global", "tooltip_column_filter_empty");
        var selectedFilter = doc.getDocLabel("page_global", "tooltip_column_filter_filtered");
        var display = '<span class="glyphicon glyphicon-filter pull-right" style="cursor:pointer;opacity:0.2;font-size:125%" data-toggle="tooltip" data-html="true" title="' + emptyFilter + '"></span>';
        var valueFiltered = [];
        if (columnSearchValues !== undefined && columnSearchValues.length > 0 && columnSearchValues[0] !== '') {
            //Build the Alert Message for filtered column information
            var filteredColumnInformation = new Array();
            var filteredTooltip = '<div>';
            $(columnSearchValues).each(function (i) {
                valueFiltered[i] = $('<p>' + columnSearchValues[i] + '</p>').text();
                filteredTooltip += "<br><span>" + $('<p>' + columnSearchValues[i] + '</p>').text() + "</span> ";
                filteredColumnInformation.push(columnSearchValues[i]);
                filteredColumnInformation.push(" | ");
            });
            filteredColumnInformation.pop();
            filteredTooltip += '</div>';
            filteredInformation.push("<div class=\"col-sm-2 alert alert-warning\" style=\"margin-bottom:0px;\">");
            filteredInformation.push("<span id='clearFilterButton" + colIndex + "' onclick='clearIndividualFilter(\"" + tableId + "\", \"" + index + "\", false)' data-toggle='tooltip' title='Clear filter " + title + "' class='pull-right glyphicon glyphicon-remove-sign'  style='cursor:pointer;'></span>");
            filteredInformation.push("<div style=\"margin-bottom:0px;white-space: nowrap;overflow: hidden;text-overflow: ellipsis;\">");
            filteredInformation.push("<strong>" + title + "</strong> IN <br>");
            filteredInformation.push("<div data-toggle=\"tooltip\" data-html=\"true\" title=\"" + filteredTooltip + "\" id=\"alertFilteredValues" + colIndex + "\">[ ");
            filteredInformation.push(filteredColumnInformation);
            filteredInformation.push(" ]</div></div></div>");
            filteredInformation.push(" ");
            display = "<span class='glyphicon glyphicon-filter pull-right' style='cursor:pointer;opacity:0.5;font-size:125%' data-toggle='tooltip' data-html='true' title='" + valueFiltered.length + " " + selectedFilter + " : " + filteredTooltip + "'></span>";
        }

        //init column filter only if column visible
        if (oSettings.aoColumns[index].bVisible) {

            //This is the list of distinct value of the column
            //This will determine value proposed inside the select
            //TODO : to replace by server call to get distinct value in server mode
            var data = [];
            table.column(colIndex).data().unique().sort().each(function (d, j) {
                data.push(d);
            });


            //Get the header cell to display the filter
            var tableCell = $($("#" + tableId + '_wrapper [name="filterColumnHeader"]')[columnVisibleIndex])[0];
            $(tableCell).removeClass().addClass("filterHeader");
            if (table.ajax.params()["bSearchable_" + colIndex]) {
                //Then init the editable object
                var select = $('<span class="label"></span>')
                        .appendTo($(tableCell).attr('data-id', 'filter_' + columnVisibleIndex)
                                .attr('data-order', index))
                        .editable({
                            type: 'checklist',
                            title: title,
                            source: function () {
                                //Check if URL already contains parameters
                                var urlSeparator = contentUrl.indexOf("?") > -1 ? "&" : "?";
                                var url = './' + contentUrl + urlSeparator + 'columnName=' + title;
                                var result;
                                $.ajax({
                                    type: 'GET',
                                    async: false,
                                    url: url,
                                    success: function (responseObject) {
                                        if (responseObject.distinctValues !== undefined) {
                                            result = responseObject.distinctValues;
                                        } else {
                                            //TODO : To remove when all servlet have method to find distinct values
                                            //if undefined, display the distinct value displayed in the table
                                            result = data;
                                        }
                                    },
                                    error: function () {
                                        //TODO : To remove when all servlet have method to find distinct values
                                        //if error, display the distinct value displayed in the table
                                        result = data;
                                    }
                                });
                                return result;
                            }
                            ,
                            onblur: 'cancel',
                            mode: 'popup',
                            placement: 'bottom',
                            emptytext: display,
                            send: 'always',
                            validate: function (value) {
                                if (value === null || value === '' || value.length === 0) {
                                    $("#" + tableId).dataTable().fnFilter('', Math.max($("#" + tableId + " [name='filterColumnHeader']").index($(this).parent()), colIndex));
                                }
                            },
                            display: function (value, sourceData) {
                                var val;
                                $(value).each(function (i) {
                                    val = "<span class='glyphicon glyphicon-filter pull-right'></span>";
                                });
                                $(this).html(val);
                            },
                            success: function (response, newValue) {
                                console.log("index on the table : " + $("#" + tableId + " [name='filterColumnHeader']").index($(this).parent()) + " Initial index : " + colIndex);
                                $("#" + tableId).dataTable().fnFilter(newValue, Math.max($("#" + tableId + " [name='filterColumnHeader']").index($(this).parent()), colIndex));
                            }
                        });
            }
            columnVisibleIndex++;
        }
    }); // end of loop on columns

    //Display the filtered alert message only if search is activated in at least 1 column
    filteredInformation.pop();
    if (filteredInformation.length > 1) {
        var filteredStringToDisplay = "";
        for (var l = 0; l < filteredInformation.length; l++) {
            filteredStringToDisplay += filteredInformation[l];
        }

        $("#" + tableId + "_wrapper #activatedFilters").html(filteredStringToDisplay);
        $("#" + tableId + "_wrapper #clearFilterButton").click(function () {
            resetFilters($("#" + tableId).dataTable(), undefined, true);
        });
        $("#" + tableId + "_wrapper #restoreFilterButton").click(function () {
            location.reload();
        });
        $("#" + tableId + "_wrapper #filterAlertDiv").show();
    }

    //call the displayColumnSearch when table configuration is changed
    $("#" + tableId + "_wrapper #showHideColumnsButton").click(function () {
        $('ul[class="dt-button-collection dropdown-menu"] li').click(function () {
            displayColumnSearch(tableId, contentUrl, oSettings);
        });
    });


//To put into a function
//When clicking on the edit filter links
    $("#" + tableId + "_wrapper .editable").click(function () {

        //Clear custom fields to avoid duplication
        $("#" + tableId + "_wrapper [data-type='custom']").remove();

        //Check the value already selected
        //Check all if no specific value selected
        if ($(this).find("span").size() < 2) {
            $("#" + tableId + '_wrapper .editable-checklist').find("input").prop('checked', true);
        } else {
            $(this).find("span").each(function () {
                $("#" + tableId + '_wrapper .editable-checklist').find("input[value='" + $(this).text() + "']").prop('checked', true);
            });
        }

        //Add an input field to search specific checkbox (search ignore case)
        $.extend($.expr[":"], {
            "containsIN": function (elem, i, match, array) {
                return (elem.textContent || elem.innerText || "").toLowerCase().indexOf((match[3] || "").toLowerCase()) >= 0;
            }
        });
        $(this.parentNode).find("h3").after($('<div></div>').append($('<input>').attr('placeholder', 'Search...')
                .attr('class', 'col-sm-8 form-control input-sm').attr('name', 'searchField')
                .attr('data-type', 'custom').on('keyup', function () {
            $('#' + tableId + '_wrapper .editable-checklist > div').hide();
            $('#' + tableId + '_wrapper .editable-checklist > div:containsIN(' + $(this).val() + ')').show();
        })));
        //Add selectAll/unSelectAll button
        $("#" + tableId + "_wrapper [name='searchField']").after(
                $('<button>').attr('class', 'glyphicon glyphicon-check')
                .attr('type', 'button')
                .attr('title', 'select all').attr('name', 'selectAll')
                .attr('data-type', 'custom').on('click', function () {
            $(this).parent().parent().find("[type='checkbox']:visible").prop('checked', true);
        }));
        $("#" + tableId + "_wrapper [name='searchField']").after(
                $('<button>').attr('class', 'glyphicon glyphicon-unchecked')
                .attr('type', 'button')
                .attr('title', 'unselect all').attr('name', 'unSelectAll')
                .attr('data-type', 'custom').on('click', function () {
            $(this).parent().parent().find("[type='checkbox']:visible").prop('checked', false);
        }));


    });
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
            oTable.fnDraw(true);
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

    var footerString = doc.getDocLabel("page_global", "footer_text");
    var footerBugString = doc.getDocLabel("page_global", "footer_bug");
    var date = new Date();
    var loadTime = window.performance.timing.domContentLoadedEventEnd - window.performance.timing.navigationStart;

    footerString = footerString.replace("%VERSION%", cerberusInformation.projectName + cerberusInformation.projectVersion + "-" + cerberusInformation.databaseCerberusTargetVersion);
    footerString = footerString.replace("%ENV%", cerberusInformation.environment);
    footerString = footerString.replace("%DATE%", date.toISOString());
    footerString = footerString.replace("%TIMING%", loadTime);
    footerBugString = footerBugString.replace("%LINK%", "https://github.com/vertigo17/Cerberus/issues/new?body=Cerberus%20Version%20:%20" + cerberusInformation.projectVersion + "-" + cerberusInformation.databaseCerberusTargetVersion);
    $("#footer").html(footerString + " - " + footerBugString);

    // Background color is light yellow if the environment is not production.
    if ((cerberusInformation.environment !== "prd") && (cerberusInformation.environment !== "PROD")) {
        document.body.style.background = "#FFFFCC";
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
        $(this).on('shown.bs.collapse', function () {
            localStorage.setItem(this.id, true);
            updateUserPreferences();
            $(this).prev().find(".toggle").removeClass("glyphicon-chevron-down").addClass("glyphicon-chevron-right");
        });

        $(this).on('hidden.bs.collapse', function () {
            localStorage.setItem(this.id, false);
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

function drawURL(data) {
    return drawHyperlink(data, data);
}

function drawHyperlink(href, text) {
    if (text.indexOf("://") > -1) {
        return "<a target=\"_blank\" href='" + href + "'>" + text + "</a>";//TODO:FN ver se tem caracters que precisam de ser encapsulados
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

function generateExecutionLink(status, id) {
    var result = "";
    if (status === "NE") {
        result = "./RunTests.jsp?queuedExecution=" + id;
    } else {
        var data = getParameter("cerberus_executiondetail_use")
        if (data.value !== "N") {
            result = "./ExecutionDetail2.jsp?executionId=" + id;
        } else {
            result = "./ExecutionDetail.jsp?id_tc=" + id;
        }
    }
    return result;
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
        rowClass["glyph"] = "fa fa-clock-o";
    } else if (status === "NA") {
        rowClass["glyph"] = "fa fa-question";
    } else {
        rowClass["glyph"] = "";
    }
    return rowClass;
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
 * Method used to restrict usage of some specific caracters.
 * @param {String} val value to test
 * @returns {boolean} true if is null, undefined of len >= 0
 */
var propertyNameRestriction = /[\(\.\,\)\'\"]/g;
var subDataNameRestriction = /[\(\.\,\)\'\"]/g;
var testDataLibNameRestriction = /[\(\.\,\)\'\"]/g;

function restrictCharacters(myfield, e, restrictionType) {
    if (!e)
        var e = window.event;
    if (e.keyCode)
        code = e.keyCode;
    else if (e.which)
        code = e.which;
    var character = String.fromCharCode(code);
    if ((e.keyCode === 39) || (e.keyCode === 40)) {
        return true;
    }
    if (character.match(restrictionType)) {
        console.debug("Key not allowed in that field. keyCode : '" + e.keyCode + "', character : '" + character + "', code : '" + code + "'");
        return false;
    } else {
        return true;
    }
}

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
        return val.split(new RegExp(separator + "(?!.*" + separator + ")"))
    }

    function extractLast(term, separator) {
        return split(term, separator).pop();
    }

    function extractAllButLast(term, separator) {
        var last = split(term, separator).pop();
        var index = term.lastIndexOf(last);
        return term.substring(0, index);
    }
console.log($(identifier));
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
                create: function () {
                    $(this).data('ui-autocomplete')._renderItem = function (ul, item) {
                        var icon = "";
                        if (Tags[this.currentIndexTag].addAfter != "%") {
                            icon = "<span class='ui-corner-all glyphicon glyphicon-chevron-right' tabindex='-1' style='margin-top:3px; float:right;'></span>";
                        }
                        return $("<li class='ui-menu-item'>")
                                .append("<a class='ui-corner-all' tabindex='-1' style='height:100%'><span style='float:left;'>" + item.label + "</span>" + icon + "<span style='clear: both; display: block;'></span></a>")
                                .appendTo(ul);
                    };
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
                                this.currentIndexTag = tag;
                                var arrayToDisplay = $.ui.autocomplete.filter(
                                        Tags[tag].array, extractLast(identifier, Tags[tag].regex));
                                if (Tags[tag].isCreatable && extractLast(identifier, Tags[tag].regex) != "") {
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
                    $(this).focus();
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
    return str == undefined ? str : str.linkify(options == undefined ? DEFAULT_LINKIFY_OPTIONS : options);
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
    var doc = new DOMParser().parseFromString(str, "text/html");
    return Array.from(doc.body.childNodes).some(node => node.nodeType === 1);
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
