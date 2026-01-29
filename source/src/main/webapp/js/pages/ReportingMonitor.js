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
/* global handleErrorAjaxAfterTimeout */
// ChartJS Config Graphs

// Counters of different countries, env and robotdecli (used to shorten the labels)

var wTestCase = 50;
var wCountry = 70;
var wEnvironment = 50;
var wPrevExe = 25;
var wMinmum = 100;
var wOffsetH = 40;
var wOffsetV = 40;
var wBoxHeigth = 72;

var maxPreviousExe = 2;
var displayHorizonMin = 1440;
var displayRetry = false;
var displayMuted = false;
var fullscreen = false;
var systems = [];
var environments = [];
var countries = [];
var campaigns = [];

// Must be the same as the one used on the back
var SEPARATOR = "-";
var lastReceivedPush = new Date();
var lastReceivedData = {};
var wsOpen = false;
var wsStartOpenning = false;
var socket;

// Variables used for automatic refresh of global last refresh timing and box refresh
var boxTimeout;
var boxTimeoutPeriod = 5000;
var globalTimeout;
var globalTimeoutPeriod = 2000;
var reopenWSTimeout;
var reopenWSTimeoutPeriod = 5000;

// Column selection on monitoring table
var colConfig = {
    "system": false,
    "application": false,
    "test": false,
    "testCase": false,
    "country": true,
    "environment": true,
    "robot": false,
    "campaign": false
};
var autoCol = true;
var col = [];

$.when($.getScript("js/global/global.js")).then(function () {
    $(document).ready(function () {

        initPage();
        bindToggleCollapse();
        $('[data-toggle="popover"]').popover({
            'placement': 'auto',
            'container': 'body'}
        );

//        var campaigns = GetURLParameters("campaigns");

        wTestCase = GetURLParameterInteger("wTestCase", wTestCase);
        wCountry = GetURLParameterInteger("wCountry", wCountry);
        wEnvironment = GetURLParameterInteger("wEnvironment", wEnvironment);
        wPrevExe = GetURLParameterInteger("wPrevExe", wPrevExe);
        wMinmum = GetURLParameterInteger("wMinmum", wMinmum);
        wOffsetH = GetURLParameterInteger("wOffsetH", wOffsetH);
        wOffsetV = GetURLParameterInteger("wOffsetV", wOffsetV);
        wBoxHeigth = GetURLParameterInteger("wBoxHeigth", wBoxHeigth);

        systems = GetURLParameters("systems");
        environments = GetURLParameters("environments");
        countries = GetURLParameters("countries");
        campaigns = GetURLParameters("campaigns");
        col = GetURLParameters("col");
        displayHorizonMin = GetURLParameterInteger("displayHorizonMin", displayHorizonMin);
        maxPreviousExe = GetURLParameterInteger("maxPreviousExe", maxPreviousExe);
        displayRetry = GetURLParameterBoolean("displayRetry", displayRetry);
        displayMuted = GetURLParameterBoolean("displayMuted", displayMuted);
        autoCol = GetURLParameterBoolean("autoCol", autoCol);
        fullscreen = GetURLParameterBoolean("fullscreen", fullscreen);
        if (fullscreen) {
            goFullscreen(false);
        }
        colConfig = feedColConfigFromURL(col);
//        console.info(colConfig);


        feedColConfigSelectOptions();

        $("#campaignSelect").empty();
        $("#campaignSelect").select2({width: "100%"});
        feedCampaignSelectOptions("#campaignSelect", campaigns);

        $("#systemSelect").empty();
        $("#systemSelect").select2({width: "100%"});
        feedSystemSelectOptions("#systemSelect", systems);

        $("#envSelect").empty();
        $("#envSelect").select2({width: "100%"});
        feedEnvironmentSelectOptions("#envSelect", environments);

        $("#countrySelect").empty();
        $("#countrySelect").select2({width: "100%"});
        feedCountrySelectOptions("#countrySelect", countries);

        $("#maxPreviousExe").val(maxPreviousExe);
        $("#displayHorizonMin").val(displayHorizonMin);

        $("#displayRetry").prop("checked", displayRetry);
        $("#displayMuted").prop("checked", displayMuted);

        openSocketAndBuildTable();

        refreshBoxTimings();
        refreshGlobalTimings();
        // Trigger automatic reopen of ws only after 2 sec delay
        wait(2000, function reOpenWSTimings() {
            if ((!wsOpen) & (!wsStartOpenning)) {
                loadBoard();
            }
            // Loop on refresh reopen ws
            reopenWSTimeout = setTimeout(() => {
                reOpenWSTimings();
            }, reopenWSTimeoutPeriod);
        }
        );

    });
});


function feedColConfigFromURL(paramCol) {
    // If at least 1 column is specified from URL, we default all values to false.
    if (paramCol.length > 0) {
        for (var i = 0, max = Object.keys(colConfig).length; i < max; i++) {
//        console.info(Object.keys(colConfig)[i]);
            colConfig[Object.keys(colConfig)[i]] = false;
        }
        for (var i = 0, max = paramCol.length; i < max; i++) {
            colConfig[paramCol[i]] = true;
        }
    }
    return colConfig;
}


function feedCampaignSelectOptions(selectElement, defaultCampaigns) {

    var campaignList = $(selectElement);
    campaignList.empty();

    var jqxhr = $.getJSON("ReadCampaign");
    $.when(jqxhr).then(function (data) {
        for (var index = 0; index < data.contentTable.length; index++) {
            campaignList.append($('<option></option>').text(data.contentTable[index].campaign + " - " + data.contentTable[index].description).val(data.contentTable[index].campaign));
        }
        $('#campaignSelect').val(defaultCampaigns);
        $('#campaignSelect').trigger('change');

    });
}

function feedSystemSelectOptions(selectElement, systemsLoad) {
    var systemList = $(selectElement);
    systemList.empty();

    let user = JSON.parse(sessionStorage.getItem('user'));
    let systems = user.system;
    let options = $("#systemSelect").html("");
    $.each(systems, function (index, value) {
        option = `<option value="${value}">${value}</option>`;
        systemList.append(option);
    });
    $('#systemSelect').val(systemsLoad);
//    console.info(systemsLoad);
}

function feedEnvironmentSelectOptions(selectElement, environmentsLoad) {
    var envList = $(selectElement);
    envList.empty();

    let envs = getInvariantArray("ENVIRONMENT", false, undefined, false);

    let options = $("#envSelect").html("");
    $.each(envs, function (index, value) {
        option = `<option value="${value}">${value}</option>`;
        envList.append(option);
    });
    $('#envSelect').val(environmentsLoad);
//    console.info(environmentsLoad);
}

function feedCountrySelectOptions(selectElement, countriesLoad) {
    var countryList = $(selectElement);
    countryList.empty();

    let countries = getInvariantArray("COuNTRY", false, undefined, false);
    let options = $("#countrySelect").html("");
    $.each(countries, function (index, value) {
        option = `<option value="${value}">${value}</option>`;
        countryList.append(option);
    });
    $('#countrySelect').val(countriesLoad);
//    console.info(countriesLoad);

}

function feedColConfigSelectOptions() {

    for (var i = 0, max = Object.keys(colConfig).length; i < max; i++) {
//        console.info(Object.keys(colConfig)[i]);
        if (colConfig[Object.keys(colConfig)[i]]) {
            $("#layoutMode").find('.btn-' + Object.keys(colConfig)[i]).addClass('btn-primary active');
        } else {
            $("#layoutMode").find('.btn-' + Object.keys(colConfig)[i]).removeClass('btn-primary');
            $("#layoutMode").find('.btn-' + Object.keys(colConfig)[i]).removeClass('active');
        }
    }

    if (autoCol) {
        $("#layoutMode").find('.btn-auto').addClass('btn-primary active');
        for (var i = 0, max = Object.keys(colConfig).length; i < max; i++) {
//            console.info(Object.keys(colConfig)[i]);
            $("#layoutMode").find('.btn-' + Object.keys(colConfig)[i]).attr("disabled", true);
        }
    } else {
        $("#layoutMode").find('.btn-auto').removeClass('btn-primary');
        $("#layoutMode").find('.btn-auto').removeClass('active');
    }

}


function initPage() {
    var doc = new Doc();
    //displayHeaderLabel(doc);
    displayPageLabel(doc);
    displayFooter(doc);
}

function displayPageLabel(doc) {
//    $("#pageTitle").html(doc.getDocLabel("page_campaignreportovertime", "title"));
//    $("#title").html(doc.getDocOnline("page_campaignreportovertime", "title"));
//    $("#loadbutton").html(doc.getDocLabel("page_global", "buttonLoad"));
//    $("#filters").html(doc.getDocOnline("page_global", "filters"));
}


function toggleCol(e, colClicked) {
//    console.info(e);
    if (e.classList.contains("btn-primary")) {
        $("#layoutMode").find('.btn-' + colClicked).removeClass('btn-primary');
        $("#layoutMode").find('.btn-' + colClicked).removeClass('active');
        colConfig[colClicked] = false;
    } else {
        $("#layoutMode").find('.btn-' + colClicked).addClass('btn-primary active');
        colConfig[colClicked] = true;
    }
}

function toggleColAutomode(e) {
//    console.info(e);
//    console.info(colConfig);
    if (e.classList.contains("btn-primary")) {
        $("#layoutMode").find('.btn-auto').removeClass('btn-primary');
        $("#layoutMode").find('.btn-auto').removeClass('active');
        autoCol = false;
        for (var i = 0, max = Object.keys(colConfig).length; i < max; i++) {
            $("#layoutMode").find('.btn-' + Object.keys(colConfig)[i]).removeAttr("disabled");
        }
    } else {
        $("#layoutMode").find('.btn-auto').addClass('btn-primary active');
        autoCol = true;
        for (var i = 0, max = Object.keys(colConfig).length; i < max; i++) {
//            console.info(Object.keys(colConfig)[i]);
            $("#layoutMode").find('.btn-' + Object.keys(colConfig)[i]).attr("disabled", true);
        }
    }
}

function goFullscreen(withRefrash = true) {
    let myButton = document.getElementById("monitoringChart");
    if (myButton.classList.contains("overlay")) {
        myButton.classList.remove('overlay');
        $(document).unbind("keydown");
        fullscreen = false;
    } else {
        myButton.classList.add('overlay');
        fullscreen = true;

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
    // Size of the screen cahnged. We refresh the loading of the table.
    if (withRefrash)
        loadBoard();
}


function loadBoard() {

    systems = [];
    environments = [];
    countries = [];
    campaigns = [];

    let systemQ = "";
    if ($("#systemSelect").val() !== null) {
        for (var i = 0; i < $("#systemSelect").val().length; i++) {
            systemQ = systemQ + "&systems=" + encodeURI($("#systemSelect").val()[i]);
            systems.push($("#systemSelect").val()[i]);
        }
    }

    let environmentsQ = "";
    if ($("#envSelect").val() !== null) {
        for (var i = 0; i < $("#envSelect").val().length; i++) {
            environmentsQ = environmentsQ + "&environments=" + encodeURI($("#envSelect").val()[i]);
            environments.push($("#envSelect").val()[i]);
        }
    }

    let countriesQ = "";
    if ($("#countrySelect").val() !== null) {
        for (var i = 0; i < $("#countrySelect").val().length; i++) {
            countriesQ = countriesQ + "&countries=" + encodeURI($("#countrySelect").val()[i]);
            countries.push($("#countrySelect").val()[i]);
        }
    }

    let campaignsQ = "";
    if ($("#campaignSelect").val() !== null) {
        for (var i = 0; i < $("#campaignSelect").val().length; i++) {
            campaignsQ = campaignsQ + "&campaigns=" + encodeURI($("#campaignSelect").val()[i]);
            campaigns.push($("#campaignSelect").val()[i]);
        }
    }

    maxPreviousExe = $("#maxPreviousExe").val();
    displayHorizonMin = $("#displayHorizonMin").val();

    displayRetry = $("#displayRetry").prop("checked");
    displayMuted = $("#displayMuted").prop("checked");


    ;

    colConfig.system = ($(".btn-system").attr("class").split('active').length > 1);
    colConfig.application = ($(".btn-application").attr("class").split('active').length > 1);
    colConfig.test = ($(".btn-test").attr("class").split('active').length > 1);
    colConfig.testCase = ($(".btn-testCase").attr("class").split('active').length > 1);
    colConfig.country = ($(".btn-country").attr("class").split('active').length > 1);
    colConfig.environment = ($(".btn-environment").attr("class").split('active').length > 1);
    colConfig.robot = ($(".btn-robot").attr("class").split('active').length > 1);
    colConfig.campaign = ($(".btn-campaign").attr("class").split('active').length > 1);
    var qSCol = "";
    for (var i = 0, max = Object.keys(colConfig).length; i < max; i++) {
//        console.info(Object.keys(colConfig)[i]);
        if (colConfig[Object.keys(colConfig)[i]]) {
            qSCol += "&col=" + Object.keys(colConfig)[i];
        }
    }

    let qS = "fullscreen=" + fullscreen + "&maxPreviousExe=" + maxPreviousExe + "&displayHorizonMin=" + displayHorizonMin
            + "&autoCol=" + autoCol + "&displayRetry=" + displayRetry + "&displayMuted=" + displayMuted + qSCol
            + systemQ + environmentsQ + countriesQ + campaignsQ;

    InsertURLInHistory("./ReportingMonitor.jsp?" + qS);
    if (wsOpen) {
        refreshMonitorTable(lastReceivedData);
    } else if (!wsStartOpenning) {
        openSocketAndBuildTable();
    }

}

function openSocketAndBuildTable() {

    if (!wsOpen) {

        sockets = [];
        var parser = document.createElement('a');
        parser.href = window.location.href;

        var protocol = "ws:";
        if (parser.protocol === "https:") {
            protocol = "wss:";
        }
        var path = parser.pathname.split("ReportingMonitor")[0];
        var new_uri = protocol + parser.host + path + "api/ws/executionmonitor";
        console.info("Open Socket to : " + new_uri);
        wsStartOpenning = true;
        socket = new WebSocket(new_uri);

        socket.onopen = function (e) {
            hideLoader("#tableMonitor");
            hideLoader("#progressMonitor");
            console.info("ws onopen");
            wsOpen = true;
            wsStartOpenning = false;
        }; //on "écoute" pour savoir si la connexion vers le serveur websocket s'est bien faite

        socket.onmessage = function (e) {
            var data = JSON.parse(e.data);
            hideLoader("#tableMonitor");
            hideLoader("#progressMonitor");
            console.info("ws onmessage");
            let nbMsSinceLastPushReceived = new Date() - lastReceivedPush;
            console.info("nb of ms since last push received : " + nbMsSinceLastPushReceived);
            lastReceivedPush = new Date();
            lastReceivedData = data;
            console.info(data);
            refreshMonitorTable(data);
        }; //on récupère les messages provenant du serveur websocket

        socket.onclose = function (e) {
            console.info("ws onclose");
            showLoader("#tableMonitor", "Connection closed from server please refresh page.");
            showLoader("#progressMonitor", "Connection closed from server please refresh page.");
            wsOpen = false;
            wsStartOpenning = false;
        }; //on est informé lors de la fermeture de la connexion vers le serveur

        socket.onerror = function (e) {
            console.info("ws onerror");
            showLoader("#tableMonitor", "Connection error on server please refresh page.");
            showLoader("#progressMonitor", "Connection error on server please refresh page.");
            wsOpen = false;
            wsStartOpenning = false;
        }; //on traite les cas d'erreur*/

        // Remain in memory
        sockets.push(socket);

    }

}


function getColumnsFromConfigAndBoxes(data, localColConfig) {
    // Calculate here the list of columns to display
    let boxesArray = Object.keys(data.executionBoxes);
    let columns = {};
    for (var j = 0, maxr = (boxesArray.length); j < maxr; j++) {
        let curExe = data.executions[data.executionBoxes[boxesArray[j]][data.executionBoxes[boxesArray[j]].length - 1]];
//        console.info(curExe);
        let tmpColumn = getColumFromBox(curExe, localColConfig);
        if (columns[tmpColumn.value] === undefined) {
            columns[tmpColumn.value] = {};
            columns[tmpColumn.value].nb = 1;
            columns[tmpColumn.value].obj = tmpColumn;
            columns[tmpColumn.value].label = getColumnLabel(tmpColumn);
        } else {
            columns[tmpColumn.value].nb += 1;
        }
    }
//    console.info("Columns : " + Object.keys(columns).length);
//    console.info(columns);
    return columns;
}

function getBoxesWidth(nbPrevExe, tmpColConfig) {
//    console.info(tmpColConfig);
//    console.info(nbPrevExe);
    let t1 = tmpColConfig.testCase === false ? wTestCase : 0;
    let t2 = tmpColConfig.country === false ? wCountry : 0;
    let t3 = tmpColConfig.environment === false ? wEnvironment : 0;
//    console.info((25 * nbPrevExe) + t1 + t2 + t3);
    return Math.max(wMinmum, (wPrevExe * nbPrevExe) + t1 + t2 + t3);
}

function getNbMaxFromArray(tempColumns) {
    let tmpMaxLines = 0;
    for (var item in Object.values(tempColumns)) {
//        console.info(Object.values(tempColumns)[item]);
        if (Object.values(tempColumns)[item].nb > tmpMaxLines)
            tmpMaxLines = Object.values(tempColumns)[item].nb;
    }
    return tmpMaxLines;
}

function refreshMonitorTable(dataFromWs) {

    let startProcessing = new Date();
    let monTable = $("#tableMonitor");



    // Before we empty the table, we Save here the previous execution id values of each tile into an object in order to identify the ones that changed since last refresh
    var indexPreviousValues = {};
    document.querySelectorAll('.mon-box').forEach((item, index) => {
        exeId = item.getAttribute("data-exeid");
        fn = item.getAttribute("data-fn");
        id = item.getAttribute("id");
        console.info(item);
        indexPreviousValues[id] = exeId + fn;

    });
    console.info("INDEX of previous executions");
    console.info(indexPreviousValues);
    monTable.empty();

//    console.info(dataFromWs);
    if (dataFromWs === null || dataFromWs === undefined || dataFromWs.executionBoxes === undefined || (Object.keys(dataFromWs.executionBoxes).length === 0)) {
        let divMess = $("<h3 style='text-align: center;'></h3>").append("No execution to display!!!");
        monTable.append(divMess);
        $("#statusProgress").empty();
        $("#MonitorHeaderCounter").html("");
        return;
    }

    // Update title with time since last refresh
    document.getElementById("MonitorHeader").setAttribute("data-lastPush", lastReceivedPush.getTime());
    let nbMsSinceLastPushReceived = new Date() - lastReceivedPush;
    $("#MonitorHeader").html("Time since last data received from server : " + getHumanReadableDuration(nbMsSinceLastPushReceived / 1000, 2));


    // Clean here all executions according to filters. data is built from dataFromWs and filters.
    let data = {};
    let agregatedStatus = {};
    let agregatedStatusTotal = 0;
    data.executions = dataFromWs.executions;
    data.environments = dataFromWs.environments;
    data.tests = dataFromWs.tests;
    data.executionBoxes = {};
    let listData = Object.keys(dataFromWs.executionBoxes);
    console.info("Size before filter : " + listData.length);
//    console.info(systems);
//    console.info(environments);
//    console.info(countries);
//    console.info(campaigns);
    for (var j = 0, maxr = (listData.length); j < maxr; j++) {
        let tempArrayExe = [];
        let maxi = dataFromWs.executionBoxes[listData[j]].length;
        for (var i = 0; i < maxi; i++) {
            let exeid = dataFromWs.executionBoxes[listData[j]][i];
            let exeTmp = dataFromWs.executions[exeid];
            let exeOldMin = (new Date().getTime() - exeTmp.start) / 60000;
//            console.info(exeTmp.system);
//            console.info(exeOldMin);
//            console.info((exeTmp.usefull || (!exeTmp.usefull && displayRetry)));
            if (
                    (!exeTmp.muted || (exeTmp.muted && displayMuted))
                    && (exeTmp.usefull || (!exeTmp.usefull && displayRetry))
                    && (exeOldMin < displayHorizonMin)
                    && (systems.length === 0 || containsInArray(exeTmp.system, systems))
                    && (environments.length === 0 || containsInArray(exeTmp.environment, environments))
                    && (countries.length === 0 || containsInArray(exeTmp.country, countries))
                    && (campaigns.length === 0 || containsInArray(exeTmp.campaign, campaigns))
                    ) {
                tempArrayExe.push(exeid);
            }
        }
        if (tempArrayExe.length > 0) {
            data.executionBoxes[listData[j]] = tempArrayExe;

            // Create per status agregation.
            let newstatus = getFinalStatus(dataFromWs.executions[tempArrayExe[tempArrayExe.length - 1]].controlStatus, dataFromWs.executions[tempArrayExe[tempArrayExe.length - 1]].falseNegative);
            if (agregatedStatus[newstatus] === undefined) {
                agregatedStatus[newstatus] = 1;
            } else {
                agregatedStatus[newstatus] = agregatedStatus[newstatus] + 1;
            }
            agregatedStatusTotal++;
        }
    }
//    console.info(data);
    console.info("Size after filter : " + Object.keys(data.executionBoxes).length);
//    console.info(agregatedStatus);


    // Max avalable  width to display all cloumns
    let maxAvailableHPixel = document.getElementById("progressMonitor").offsetWidth - wOffsetH;
    let maxNbColumns = maxAvailableHPixel / 120;

    let maxAvailableVPixel = document.getElementsByTagName("main").offsetHeight - wOffsetV;
    let boxHeigth = wBoxHeigth;

    let maxNblines = maxAvailableVPixel / boxHeigth;

    let minimumBoxHeigth = false;


    // If automode, we guess here the best display combination.
    if (autoCol) {
        console.info("Starting automated column calculation with available size : " + maxAvailableHPixel);
        let bestH_nbMaxCol = 0;
        let bestH_binary = "";
        let bestH_columns = {};
        let bestH_config = {};
        let bestH_maxNbBoxLines = 0;
        let bestH_colWidth = 0;
        let bestH_foundOneConfig = false;

        let bestHV_nbMaxCol = 0;
        let bestHV_binary = "";
        let bestHV_columns = {};
        let bestHV_config = {};
        let bestHV_maxNbBoxLines = 0;
        let bestHV_colWidth = 0;
        let bestHV_foundOneConfig = false;

        let smallestCol_colWidth = 1000;
        let smallestCol_binary = "";
        let smallestCol_columns = {};
        let smallestcol_config = {};
        let smallestCol_maxNbBoxLines = 0;

        for (var i = 0, max = 256; i < max; i++) { // Loop against all binary combination of a 8 bit number (corresponding to the 8 columns)

//            console.info(i.toString(2).padStart(8, '0'));
            // convert number to binary version and then to column config
            let binaryValue = i.toString(2).padStart(8, '0');
            let newColConfig = {
                "system": binaryValue.substr(0, 1) === "1" ? true : false,
                "application": binaryValue.substr(1, 1) === "1" ? true : false,
                "test": binaryValue.substr(2, 1) === "1" ? true : false,
                "testCase": binaryValue.substr(3, 1) === "1" ? true : false,
                "country": binaryValue.substr(4, 1) === "1" ? true : false,
                "environment": binaryValue.substr(5, 1) === "1" ? true : false,
                "robot": binaryValue.substr(6, 1) === "1" ? true : false,
                "campaign": binaryValue.substr(7, 1) === "1" ? true : false
            };
            let boxesWidth = getBoxesWidth(maxPreviousExe, newColConfig);
            maxNbColumns = maxAvailableHPixel / boxesWidth;

//            console.info(binaryValue);
//            console.info(" maxNbCol : " + maxNbColumns + " - Total Available size : " + maxAvailableHPixel + " - Box size : " + getBoxesWidth(maxPreviousExe, newColConfig));
//        console.info(newColConfig);
            tempColumns = getColumnsFromConfigAndBoxes(data, newColConfig);
//            console.info(tempColumns);
            let tmpMaxLines = getNbMaxFromArray(tempColumns);

            if ((Object.keys(tempColumns).length < maxNbColumns)) {
                // there are not too many columns for the screen.

//                console.info(" maxNbLines : " + maxNblines + " - Total Available size : " + maxAvailableVPixel + " - nb lines : " + tmpMaxLines);

                // this if in order to get the conbination that fit in horizontal and vertical constrain.
                if ((tmpMaxLines < maxNblines)) {

                    if ((Object.keys(tempColumns).length > bestHV_nbMaxCol)) {

                        bestHV_nbMaxCol = Object.keys(tempColumns).length;
                        bestHV_columns = tempColumns;
                        bestHV_config = newColConfig;
                        bestHV_binary = binaryValue;
                        bestHV_colWidth = boxesWidth;
                        bestHV_maxNbBoxLines = tmpMaxLines;
                        bestHV_foundOneConfig = true;
                        console.info("Found Better H&V with " + Object.keys(tempColumns).length + " column(s) " + binaryValue);
                        console.info(" maxNbCol   : " + maxNbColumns + " - Total Available size : " + maxAvailableHPixel + " - Box size : " + getBoxesWidth(maxPreviousExe, newColConfig));
                        console.info(" maxNbLines : " + maxNblines + " - Total Available size : " + maxAvailableVPixel + " - nb lines : " + tmpMaxLines);
//                        console.info(tempColumns);

                    }

                }

                // The nb of columns is higher than a previous combination.
                if ((Object.keys(tempColumns).length > bestH_nbMaxCol)) {

//                maxNbBoxLines = tempColumns.nb;
                    bestH_nbMaxCol = Object.keys(tempColumns).length;
                    bestH_columns = tempColumns;
                    bestH_config = newColConfig;
                    bestH_binary = binaryValue;
                    bestH_colWidth = boxesWidth;
                    bestH_maxNbBoxLines = tmpMaxLines;
                    bestH_foundOneConfig = true;
                    console.info("Found Better H with " + Object.keys(tempColumns).length + " column(s) " + binaryValue);
                    console.info("  maxNbCol : " + maxNbColumns + " - Total Available size : " + maxAvailableHPixel + " - Box size : " + getBoxesWidth(maxPreviousExe, newColConfig));
//                    console.info(tempColumns);
                    bestH_maxNbBoxLines = getNbMaxFromArray(tempColumns);

                } else if ((Object.keys(tempColumns).length === bestH_nbMaxCol)) {
//                    console.info("option has the same result does it have less max lines ?");
//                    console.info(Object.values(tempColumns));
                    if (tmpMaxLines < bestH_maxNbBoxLines) {
                        bestH_columns = tempColumns;
                        bestH_config = newColConfig;
                        bestH_binary = binaryValue;
                        bestH_colWidth = boxesWidth;
                        bestH_maxNbBoxLines = tmpMaxLines;
                        console.info(" Yes : Found Even Better combination with lower max nb lines : " + tmpMaxLines + " < " + bestH_maxNbBoxLines + " " + binaryValue);
                        console.info(" maxNbCol : " + maxNbColumns + " - Total Available size : " + maxAvailableHPixel + " - Box size : " + getBoxesWidth(maxPreviousExe, newColConfig));
//                        console.info(tempColumns);
//                    } else {
//                        console.info(" No : Not Better max nb lines : " + tmpMaxLines + " >= " + maxNbBoxLines);
                    }
                }

            } else {

                if (smallestCol_colWidth > (Object.keys(tempColumns).length * boxesWidth)) {

                    smallestCol_colWidth = (Object.keys(tempColumns).length * boxesWidth);
                    smallestCol_columns = tempColumns;
                    smallestcol_config = newColConfig;
                    smallestCol_binary = binaryValue;
                    smallestCol_maxNbBoxLines = tmpMaxLines;
                    console.info("Found smallest combination with " + Object.keys(tempColumns).length + " column(s) " + binaryValue);
                    console.info("  nb col : " + Object.keys(tempColumns).length + " box width : " + boxesWidth + " total size : " + (Object.keys(tempColumns).length * boxesWidth));

                }
//                console.info();
//                console.info(Object.keys(tempColumns).length * boxesWidth);
            }
        }

        // Algo could not find any combination that fit the available space constrain --> We pile into 1 single column (the smallest we could find).
        if (bestHV_foundOneConfig) {
            console.info("H&V combination could be found : " + Object.keys(bestHV_columns).length + " column(s) " + bestHV_binary);
//            console.info("  " + bestHV_binary);
            console.info("  H : nb col : " + Object.keys(bestHV_columns).length + " box width : " + bestHV_colWidth + " total size : " + (Object.keys(bestHV_columns).length * bestHV_colWidth) + " / " + maxAvailableHPixel);
            console.info("  V : nb lines : " + bestHV_maxNbBoxLines + " box heigth : " + boxHeigth + " total size : " + (bestHV_maxNbBoxLines * boxHeigth) + " / " + maxAvailableVPixel);
            columns = bestHV_columns;
            colConfig = bestHV_config;
            if (bestHV_maxNbBoxLines > maxNblines) {
                minimumBoxHeigth = true;
            }
        } else if (bestH_foundOneConfig) {
            console.info("H combination could be found : " + Object.keys(bestH_columns).length + " column(s) " + bestH_binary);
            console.info("  H : nb col : " + Object.keys(bestH_columns).length + " box width : " + bestH_colWidth + " total size : " + (Object.keys(bestH_columns).length * bestH_colWidth) + " / " + maxAvailableHPixel);
            console.info("  V : nb lines : " + bestH_maxNbBoxLines + " box heigth : " + boxHeigth + " total size : " + (bestH_maxNbBoxLines * boxHeigth) + " / " + maxAvailableVPixel);
//            console.info(" " + bestH_binary);
            columns = bestH_columns;
            colConfig = bestH_config;
            if (bestH_maxNbBoxLines > maxNblines) {
                minimumBoxHeigth = true;
            }
        } else {
            console.info("No combination could fit the size screen so we take the smallest we could find : " + Object.keys(smallestCol_columns).length + " column(s) " + smallestCol_binary);
            console.info("  H : nb col : " + Object.keys(smallestCol_columns).length + " box width : " + smallestCol_colWidth + " total size : " + (Object.keys(smallestCol_columns).length * smallestCol_colWidth) + " / " + maxAvailableHPixel);
            console.info("  V : nb lines : " + smallestCol_maxNbBoxLines + " box heigth : " + boxHeigth + " total size : " + (smallestCol_maxNbBoxLines * boxHeigth) + " / " + maxAvailableVPixel);
//            console.info(" " + smallestCol_binary);
            if (smallestCol_maxNbBoxLines > maxNblines) {
                minimumBoxHeigth = true;
            }
            columns = smallestCol_columns;
            colConfig = smallestcol_config;
        }

        feedColConfigSelectOptions();

    } else {
        columns = getColumnsFromConfigAndBoxes(data, colConfig);
        let tmpMaxLines = getNbMaxFromArray(columns);
        let boxesWidth = getBoxesWidth(maxPreviousExe, colConfig);

        if (Object.keys(columns).length > maxNbColumns) {
            console.info("Warning : Too many columns " + Object.keys(columns).length + " columns to display - Available space : " + document.getElementById("progressMonitor").offsetWidth);
            console.info("  H : nb col : " + Object.keys(columns).length + " box width : " + boxesWidth + " total size : " + (Object.keys(columns).length * boxesWidth) + " / " + maxAvailableHPixel);
            console.info("  V : nb lines : " + tmpMaxLines + " box heigth : " + boxHeigth + " total size : " + (tmpMaxLines * boxHeigth) + " / " + maxAvailableVPixel);
        } else {
            console.info("All columns can be displayed !! " + Object.keys(columns).length);
            console.info("  H : nb col : " + Object.keys(columns).length + " box width : " + boxesWidth + " total size : " + (Object.keys(columns).length * boxesWidth) + " / " + maxAvailableHPixel);
            console.info("  V : nb lines : " + tmpMaxLines + " box heigth : " + boxHeigth + " total size : " + (tmpMaxLines * boxHeigth) + " / " + maxAvailableVPixel);
        }
        if (tmpMaxLines > maxNblines) {
            minimumBoxHeigth = true;
        }
    }

    if (minimumBoxHeigth) {
        console.info("Reduced Box activated (box on single line)");
    }

    // Sort here the list of columns
    const sortedColumns = Object.keys(columns).sort().reduce(
            (obj, key) => {
        obj[key] = columns[key];
        return obj;
    },
            {}
    );
//    console.info(sortedColumns);
    columns = sortedColumns;


    // if following all filters, no more executions are to display, we report the no execution message.
    if (Object.keys(data.executionBoxes).length === 0) {
        let divMess = $("<h3 style='text-align: center;'></h3>").append("No execution to display!!!");
        monTable.append(divMess);
        $("#statusProgress").empty();
        $("#MonitorHeaderCounter").html("");
        return;
    }

    $("#MonitorHeaderCounter").html("Total : " + Object.keys(data.executionBoxes).length);

    // 1st row of the table.
    var row = $("<tr></tr>");
//        var cel = $("<td></td>");
//        row.append(cel);
    for (var i = 0, maxc = (Object.keys(columns).length); i < maxc; i++) {
        let col = Object.keys(columns);
//        console.info(i);
//        console.info(col[i]);
        let cel = $("<td style='text-align: center;max-width : 120px'></td>").attr("id", "H" + col[i]);
        cel.append(columns[col[i]].label);
        row.append(cel);
    }
    monTable.append(row);

    // 2nd row where all execution tiles will be added piled up. cel will get the column id so that later we can add all tiles to them.
    var row = $("<tr></tr>");
    for (var i = 0, maxc = (Object.keys(columns).length); i < maxc; i++) {
        let col = Object.keys(columns);
//        console.info(i);
//        console.info(col[i]);
        let cel = $("<td style='text-align: center'></td>").attr("id", col[i]);
        row.append(cel);
    }
    monTable.append(row);

    let boxesArray = Object.keys(data.executionBoxes);

    // Sort boxes by testcase, country and environment.
    sortedBoxesArray = boxesArray.sort(function (a, b) {
//        console.info("sorting : " + a + " " + b);
        let a1 = data.executions[data.executionBoxes[a][data.executionBoxes[a].length - 1]];
        let b1 = data.executions[data.executionBoxes[b][data.executionBoxes[b].length - 1]];
//        console.info("          " + a1 + " " + b1);
        if (a1.testCase.localeCompare(b1.testCase) === 0) {
            if (a1.country.localeCompare(b1.country) === 0) {
                if (a1.environment.localeCompare(b1.environment) === 0) {
                } else {
                    return a1.environment.localeCompare(b1.environment);
                }
            } else {
                return a1.country.localeCompare(b1.country);
            }
        } else {
            return a1.testCase.localeCompare(b1.testCase);
        }
    });


    // Adding all boxes to previously generated td
    for (var j = 0, maxr = (boxesArray.length); j < maxr; j++) {
        curExe = data.executions[data.executionBoxes[boxesArray[j]][data.executionBoxes[boxesArray[j]].length - 1]];
//        console.info(curExe);
        tmpColumn = getColumFromBox(curExe, colConfig);
//        console.info(tmpColumn);
        $("#" + tmpColumn.value).append(
                renderCel(
                        boxesArray[j]
                        , data.executionBoxes[boxesArray[j]]
                        , data.executions, indexPreviousValues
                        , minimumBoxHeigth
                        )
                );
    }

    // Build here global progress bar.
    let statusArray = Object.keys(agregatedStatus);
    let buildBar = '';
    for (var index = 0; index < statusArray.length; index++) {
        var status = statusArray[index];

        var percent = (agregatedStatus[status] / agregatedStatusTotal) * 100;
        var roundPercent = Math.round(percent * 10) / 10;

        buildBar += '<div class="progress-bar status' + status + '" \n\
                role="progressbar" \n\
                style="width:' + percent + '%;color:transparent">' + roundPercent + '%</div>';
    }
    buildBar += '';
    $("#statusProgress").empty();
//    console.info(buildBar);
    $("#statusProgress").append(buildBar);
    
    // Refresh Favicon value in order to notify KO if necessary
    let faviconStat="KO";
    if ((Object.keys(agregatedStatus).length===1) && (Object.keys(agregatedStatus)[0] === "OK")) {
        faviconStat="OK";
    }
    var favicon = new Favico({
        animation: 'slide',
        bgColor: getExeStatusRowColor(faviconStat)
//        textColor: "green"
    });
    favicon.badge("!");
    
    
    // Remove all remaining tooltip that may still be displayed
    $(".tooltip").remove();
    showTitleWhenTextOverflow();

    // Log and report to console the time it spent to refresh the full table. That time should be lower than the ws load periode refresh.
    let endProcessing = new Date();
    let processingDurationMs = endProcessing - startProcessing;
    console.info("time to process : " + processingDurationMs + " ms");

}


function refreshGlobalTimings() {
//    console.info("refresh Global Last Push");
    document.querySelectorAll('.global-counter').forEach((item, index) => {
        let lastPush = item.getAttribute("data-lastpush");
//        console.info(lastPush);
        let sinceLast = new Date().getTime() - (lastPush);
        item.innerHTML = "Time since last data received from server : " + getHumanReadableDuration(sinceLast / 1000, 2);
    });
    // Loop on refresh the Global timing
    globalTimeout = setTimeout(() => {
        refreshGlobalTimings();
    }, globalTimeoutPeriod);
}

function refreshBoxTimings() {
//    console.info("refresh All Execution Boxes");
    document.querySelectorAll('.exe-counter').forEach((item, index) => {
        let lastPush = item.getAttribute("data-exestart");
//        console.info(lastPush);
        let sinceLast = new Date().getTime() - (lastPush);
        item.innerHTML = getHumanReadableDuration(sinceLast / 1000, 1);
    });

    // mon-box
    document.querySelectorAll('.mon-box').forEach((item, index) => {
        let lastPush = item.getAttribute("data-exestart");
//        console.info(lastPush);
        let sinceLast = new Date().getTime() - (lastPush);
        // Stop Blinking after 30 sec
        if ((sinceLast > 30000) && (item.classList.contains("new"))) {
            console.info("remove blinking " + item.id + " sinceLast: "+sinceLast + " classList: " + item.classList);
//            console.info($("#" + item.id));
            $("#" + item.id).removeClass("blinking new");
        }
        // Hide Box after 
        if (sinceLast > (displayHorizonMin * 60000)) {
//            console.info("hide " + sinceLast + " " + item.id);
//            console.info($("#" + item.id));
            $("#" + item.id).hide();
        }
    });

//    let exeOldMin = (now - exedate) / 60000;


    // Loop on refresh all tiles
    boxTimeout = setTimeout(() => {
        refreshBoxTimings();
    }, boxTimeoutPeriod);

}


function containsInArray(value, array) {
    for (var item in array) {
//        console.info(array[item]);
        if (array[item] === value) {
            return true;
        }
    }
    return false;
}

function getColumFromBox(exe, config) {
//    console.info(exe);
//    console.info(config);
    let column = {};
    column.value = "-";
    for (var i = 0, max = (Object.keys(config).length); i < max; i++) {
//        console.info(i + " " + Object.keys(config)[i]);
        let tmpCol = Object.keys(config)[i];
        if (config[tmpCol]) {
//            console.info(tmpCol);
//            console.info(exe[tmpCol]);
            if (exe[tmpCol] !== undefined) {
                column[tmpCol] = exe[tmpCol].replaceAll(" ", "-");
                column.value += exe[tmpCol].replaceAll(" ", "-").replaceAll(".", "-").replaceAll("/", "-").replaceAll("\\", "-") + "-";
            }
        }
    }
    return column;
}

function getColumnLabel(column) {
    let colresult = "";
    for (var i = 0, max = (Object.keys(column).length); i < max; i++) {
        if (Object.keys(column)[i] !== "value") {
            if (isEmpty(Object.values(column)[i])) {
                colresult += "<i>[Empty " + Object.keys(column)[i] + "]</i>" + "<br>";
            } else {
                colresult += "<b>" + Object.values(column)[i] + "</b>" + "<br>";
            }
        }
    }
    return colresult;

}

function renderCel(id, content, contentExe, indexPreviousValues, minimumHeigth) {
//    console.info(id);

    if (content === undefined) {
        return "";
    }
//    console.info(content);

    let curExe = contentExe[content[content.length - 1]];
//    console.info(curExe);

    let status = curExe.controlStatus;

    let fonta = "fa fa-exclamation";
    if (status === "OK") {
        fonta = "fa fa-check";
    } else if (status === "KO") {
        fonta = "fa fa-bug";
    } else if (status === "FA") {
        fonta = "fa fa-exclamation";
    }

    let exedate = new Date(curExe.start);
    let now = new Date();

    let exeOldMin = (now - exedate) / 60000;
//    console.info("Exe Old : " + exeOldMin);
    if (exeOldMin > displayHorizonMin) {
        return "";
    }

    let exes = [];
    if (content.length - 1 >= 1) {
        for (var i = content.length - 2, min = 0; i >= min; i--) {
            exes.push(content[i]);
        }
    }
//    console.info(exes);



    let tooltipcontain = getTooltip(curExe);
//    console.info($("#" + id));
    let previousCellExeId = indexPreviousValues[id];
    let classChange = "";
//    console.info(curExe);
    let nexVal = "" + curExe.id + curExe.falseNegative;
    if ((previousCellExeId !== undefined) && (previousCellExeId != nexVal)) {
        console.info("CHANGE on " + id + " Previous exe cell : " + previousCellExeId + " --> New exe : " + nexVal);
        classChange = "new blinking";
//    } else {
//        console.info("NO CHANGE on " + id + " Previous exe cell : " + previousCellExeId + " --> New exe : " + nexVal);
    }
    let classSize = "";
    if (minimumHeigth) {
        classSize = "mon-box monitor-box-small";
    } else {
        classSize = "mon-box monitor-box";
    }
//    console.info(curExe.id + " " + previousCellExeId);

    let cel = $('<div style="margin-bottom: 2px" data-toggle="tooltip" data-html="true" title data-original-title="' + tooltipcontain + '" id="' + id + '" data-exeid="' + curExe.id + '" data-fn="' + curExe.falseNegative + '" data-exestart=' + exedate.getTime() + ' onclick="window.open(\'./TestCaseExecution.jsp?executionId=' + curExe.id + '\');"></div>')
            .addClass(classChange + " " + classSize + " status" + getFinalStatus(status, curExe.falseNegative));
    if (!minimumHeigth) {
        let row1 = $("<div style='margin-right:0px'></div>").addClass("row");

        let r1c1 = $("<div></div>").addClass("col-xs-6 status").append("<span class='" + fonta + "' style='margin-right: 5px;'></span>" + status);
        row1.append(r1c1);

        let r1c2 = $("<div data-exestart=" + exedate.getTime() + " ></div>").addClass("col-xs-6 text-right exe-counter").append(getHumanReadableDuration((now - exedate) / 1000, 1));
        row1.append(r1c2);

        cel.append(row1)
    }

    let row2 = $("<div style='margin-right:0px'></div>").addClass("row");
    let r2c = $('<div></div>').addClass("col-xs-1 pull-left bold");
    let tmptxt = "";
    if (!colConfig.testCase) {
        tmptxt += curExe.testCase + " / ";
    }
    if (!colConfig.country) {
        tmptxt += curExe.country + " / ";
    }
    if (!colConfig.environment) {
        tmptxt += curExe.environment + " / ";
    }
    tmptxt = tmptxt.substring(0, tmptxt.length - 3);
    r2c.append(tmptxt);
    row2.append(r2c);
    for (var i = 0, max = exes.length; ((i < max) && (i < maxPreviousExe)); i++) {
        let r2c;
        r2c = $('<div data-toggle="tooltip" data-html="true" title data-original-title="' + getTooltip(contentExe[exes[i]]) + '" onclick="window.open(\'./TestCaseExecution.jsp?executionId=' + exes[i] + '\');stopPropagation(event);"></div>')
                .addClass("monitor-sub-box col-xs-1 pull-right status" + getFinalStatus(contentExe[exes[i]].controlStatus, contentExe[exes[i]].falseNegative));
        row2.prepend(r2c);

    }

    cel.append(row2);
    return cel;
}

function getFinalStatus(status, isFalseNegative) {
    // Final status could be OK if exe is false negative.
//    console.info(status + " " + isFalseNegative);
    if (isFalseNegative) {
        return "OK";
    } else {
        return status;
    }
}

function getTooltip(data) {
    var htmlRes;
    var ctrlmessage = data.controlMessage;
    if (ctrlmessage !== undefined && ctrlmessage.length > 200) {
        ctrlmessage = data.controlMessage.substring(0, 200) + '...';
    }
    htmlRes = '<div><span class=\'bold\'>Execution ID :</span> ' + data.id + '</div>';
    htmlRes += '<div style=\'margin-top:5px;\'><span class=\'bold\'>Test : </span>' + data.test + " " + SEPARATOR + " " + data.testCase + '</div>';
    htmlRes += '<div>' + data.description.replaceAll("\"", "\'") + '</div>';
    htmlRes += '<div style=\'margin-top:5px;\'><span class=\'bold\'>Environment : </span>' + data.environment + '</div>';
    htmlRes += '<div><span class=\'bold\'>Country : </span>' + data.country + '</div>';
    if (!isEmpty(data.robot)) {
        htmlRes += '<div><span class=\'bold\'>Robot Decli : </span>' + data.robot + '</div>';
    }
    htmlRes += '<div style=\'margin-top:5px;\'><span class=\'bold\'>Application : </span>' + data.application + " [" + data.system + "]" + '</div>';
    if (!isEmpty(data.campaign))
        htmlRes += '<div><span class=\'bold\'>Campaign : </span>' + data.campaign + '</div>';
    htmlRes += '<div style=\'margin-top:5px;\'><span class=\'bold\'>Start : </span>' + getDate(data.start) + '</div>';
    let dur = data.end - data.start;
    if (getDateShort(data.end) !== "") {
        htmlRes += '<div><span class=\'bold\'>End : </span>' + getDate(data.end) + ' <span class=\'' + getClassDuration(dur) + '\'>(' + getHumanReadableDuration(dur / 1000, 2) + ')</span></div>';
    }
    if (!data.usefull || data.muted) {
        let retryTag = !data.usefull ? "<span class='glyphicon glyphicon-repeat' aria-hidden='true'></span> [RETRY]" : "";
        if (!data.usefull && data.muted)
            retryTag += "<br>";
        let mutedTag = data.muted ? "<span class='glyphicon glyphicon-volume-off' aria-hidden='true'></span> [MUTED]" : "";
        htmlRes += '<div style=\'margin-top:5px;\'>' + retryTag + " " + mutedTag + '</div>';
    }
    htmlRes += '<div style=\'margin-top:5px;\'>' + ctrlmessage.replaceAll("\"", "\'") + '</div>';

    return htmlRes;
}


