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

var maxPreviousExe = 2;
var layoutMode = "piledup";
var displayHorizonMin = 120;
var displayRetry = false;
var displayMuted = false;

// Must be the same as the one used on the back
var SEPARATOR = "-";
var lastReceivedPush = new Date();
var lastReceivedData = {};
var wsOpen = false;

// Variables used for automatic refresh of global last refresh timing and box refresh
var boxTimeout;
var boxTimeoutPeriod = 5000;
var globalTimeout;
var globalTimeoutPeriod = 2000;
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

var socket;

$.when($.getScript("js/global/global.js")).then(function () {
    $(document).ready(function () {

        initPage();
        bindToggleCollapse();
        //open Run navbar Menu
        openNavbarMenu("navMenuExecutionReporting");
        $('[data-toggle="popover"]').popover({
            'placement': 'auto',
            'container': 'body'}
        );

//        var campaigns = GetURLParameters("campaigns");
        var systems = GetURLParameters("systems");
        var environments = GetURLParameters("environments");
        var countries = GetURLParameters("countries");
        var campaigns = GetURLParameters("campaigns");
        var col = GetURLParameters("col");
        layoutMode = GetURLParameter("layout", layoutMode);
        displayHorizonMin = GetURLParameter("displayHorizonMin", displayHorizonMin);
        maxPreviousExe = GetURLParameter("maxPreviousExe", maxPreviousExe);
        displayRetry = GetURLParameterBoolean("displayRetry", displayRetry);
        displayMuted = GetURLParameterBoolean("displayMuted", displayMuted);
        if (GetURLParameterBoolean("fullscreen", false)) {
            goFullscreen();
        }
        colConfig = feedColConfigFromURL(col);
        console.info(colConfig);


        feedColConfigSelectOptions();

//        $("#campaignSelect").empty();
//        $("#campaignSelect").select2({width: "100%"});
//        feedCampaignSelectOptions("#campaignSelect", campaigns, environments);

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

        openSocketAndBuildTable(systems, environments, countries);

        refreshBoxTimings();
        refreshGlobalTimings();

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


function feedCampaignSelectOptions(selectElement, defaultCampaigns, environments, gp1s, gp2s, gp3s) {

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
}


function initPage() {
    var doc = new Doc();
    displayHeaderLabel(doc);
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

function goFullscreen() {
    let myButton = document.getElementById("monitoringChart");
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
}


function loadBoard() {

    let systems = [];
    let environments = [];
    let countries = [];

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
        console.info(Object.keys(colConfig)[i]);
        if (colConfig[Object.keys(colConfig)[i]]) {
            qSCol += "&col=" + Object.keys(colConfig)[i];
        }
    }


    let qS = "fullscreen=false&layout=" + layoutMode + "&maxPreviousExe=" + maxPreviousExe + "&displayHorizonMin=" + displayHorizonMin
            + "&displayRetry=" + displayRetry + "&displayMuted=" + displayMuted + qSCol
            + systemQ + environmentsQ + countriesQ;

    InsertURLInHistory("./ReportingMonitor.jsp?" + qS);
    if (wsOpen) {
        refreshMonitorTable(lastReceivedData, systems, environments, countries);
    } else {
        openSocketAndBuildTable(systems, environments, countries);
    }

}

function openSocketAndBuildTable(systems, environments, countries) {

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
    socket = new WebSocket(new_uri);

    socket.onopen = function (e) {
        hideLoader("#monitoringChart");
        console.info("ws onopen");
        wsOpen = true;
    }; //on "écoute" pour savoir si la connexion vers le serveur websocket s'est bien faite

    socket.onmessage = function (e) {
        var data = JSON.parse(e.data);
        hideLoader("#monitoringChart");
        console.info("ws onmessage");
        let nbMsSinceLastPushReceived = new Date() - lastReceivedPush;
        console.info("nb of ms since last push received : " + nbMsSinceLastPushReceived);
        lastReceivedPush = new Date();
        lastReceivedData = data;
        console.info(data);
        refreshMonitorTable(data, systems, environments, countries);
    }; //on récupère les messages provenant du serveur websocket

    socket.onclose = function (e) {
        console.info("ws onclose");
        showLoader("#monitoringChart", "Connection closed from server please refresh page.");
        wsOpen = false;
    }; //on est informé lors de la fermeture de la connexion vers le serveur

    socket.onerror = function (e) {
        console.info("ws onerror");
        showLoader("#monitoringChart", "Connection error on server please refresh page.");
        wsOpen = false;
    }; //on traite les cas d'erreur*/

    // Remain in memory
    sockets.push(socket);
}

function refreshMonitorTable(dataFromWs, systems, environments, countries) {

    let startProcessing = new Date();
    let monTable = $("#tableMonitor");



    // Before we empty the table, we Save here the previous execution id values of each tile into an object in order to identify the ones that changed since last refresh
    var indexPreviousValues = {};
    document.querySelectorAll('.monitor-box').forEach((item, index) => {
        exeId = item.getAttribute("data-exeid");
        id = item.getAttribute("id");
        indexPreviousValues[id] = exeId;

    });
//    console.info("INDEX of previous executions");
//    console.info(indexPreviousValues);
    monTable.empty();

//    console.info(dataFromWs);
    if (dataFromWs === null || dataFromWs === undefined || dataFromWs.executionBoxes === undefined || (Object.keys(dataFromWs.executionBoxes).length === 0)) {
        let divMess = $("<h3 style='text-align: center;'></h3>").append("No execution to display!!!");

        monTable.append(divMess);
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
    for (var j = 0, maxr = (listData.length); j < maxr; j++) {
        let tempArrayExe = [];
        let maxi = dataFromWs.executionBoxes[listData[j]].length;
        for (var i = 0; i < maxi; i++) {
            let exeid = dataFromWs.executionBoxes[listData[j]][i];
            let exeTmp = dataFromWs.executions[exeid];
            let exeOldMin = (new Date().getTime() - exeTmp.start) / 60000;
            if (
                    (!exeTmp.muted || (exeTmp.muted && displayMuted))
                    && (exeTmp.usefull || (!exeTmp.usefull && displayRetry))
                    && (exeOldMin < displayHorizonMin)
                    && (systems.length === 0 || containsInArray(exeTmp.system, systems))
                    && (environments.length === 0 || containsInArray(exeTmp.environment, environments))
                    && (countries.length === 0 || containsInArray(exeTmp.country, countries))
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
//    console.info(agregatedStatus);


    // Calculate here the list of columnes to display
    let boxesArray = Object.keys(data.executionBoxes);
    let columns = {};
    for (var j = 0, maxr = (boxesArray.length); j < maxr; j++) {
        let curExe = data.executions[data.executionBoxes[boxesArray[j]][data.executionBoxes[boxesArray[j]].length - 1]];
//        console.info(curExe);
        let tmpColumn = getColumFromBox(curExe, colConfig);
        if (columns[tmpColumn.value] === undefined) {
            columns[tmpColumn.value] = {};
            columns[tmpColumn.value].nb = 1;
            columns[tmpColumn.value].obj = tmpColumn;
        } else {
            columns[tmpColumn.value].nb += 1;
        }
    }
//    console.info(columns);



    // if following all filters, no more executions are to display, we report the no execution message.
    if (Object.keys(data.executionBoxes).length === 0) {
        let divMess = $("<h3 style='text-align: center;'></h3>").append("No execution to display!!!");

        monTable.append(divMess);
        return;
    }

    // 1st row of the table.
    var row = $("<tr></tr>");
//        var cel = $("<td></td>");
//        row.append(cel);
    for (var i = 0, maxc = (Object.keys(columns).length); i < maxc; i++) {
        let col = Object.keys(columns);
//        console.info(i);
//        console.info(col[i]);
        let cel = $("<td style='text-align: center'></td>").attr("id", "H" + col[i]);
        cel.append(columns[col[i]].obj.value);
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


    // Adding all boxes to previously generated td
    for (var j = 0, maxr = (boxesArray.length); j < maxr; j++) {
        curExe = data.executions[data.executionBoxes[boxesArray[j]][data.executionBoxes[boxesArray[j]].length - 1]];
//        console.info(curExe);
        tmpColumn = getColumFromBox(curExe, colConfig);
//        console.info(tmpColumn);
        $("#" + tmpColumn.value).append(renderCel(boxesArray[j]
                , data.executionBoxes[boxesArray[j]]
                , data.executions, indexPreviousValues));
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

    // monitor-box
    document.querySelectorAll('.monitor-box').forEach((item, index) => {
        let lastPush = item.getAttribute("data-exestart");
//        console.info(lastPush);
        let sinceLast = new Date().getTime() - (lastPush);
        // Stop Blinking after 30 sec
        if ((sinceLast > 30000) && (item.classList.contains("new"))) {
            console.info("remove blinking " + sinceLast + " " + item.classList);
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
    for (var i = 0, max = (Object.keys(config).length - 1); i < max; i++) {
//        console.info(i + " " + Object.keys(config)[i]);
        let tmpCol = Object.keys(config)[i];
        if (config[tmpCol]) {
//            console.info(tmpCol);
//            console.info(exe[tmpCol]);
            if (exe[tmpCol] !== undefined) {
                column[tmpCol] = exe[tmpCol].replace(" ", "-");
                column.value += exe[tmpCol].replace(" ", "-") + "-";
            }
        }
    }
    return column;
}

function renderCel(id, content, contentExe, indexPreviousValues) {
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
    if ((previousCellExeId !== undefined) && (previousCellExeId != curExe.id)) {
        console.info("CHANGE on " + id + " Previous exe cell : " + previousCellExeId + " --> New exe : " + curExe.id);
        classChange = "new blinking";
//    } else {
//        console.info("NO CHANGE on " + id + " Previous exe cell : " + previousCellExeId + " --> New exe : " + curExe.id);
    }
//    console.info(curExe.id + " " + previousCellExeId);

    let cel = $('<div style="margin-bottom: 2px" data-toggle="tooltip" data-html="true" title data-original-title="' + tooltipcontain + '" id="' + id + '" data-exeid="' + curExe.id + '"  data-exestart=' + exedate.getTime() + ' onclick="window.open(\'./TestCaseExecution.jsp?executionId=' + curExe.id + '\');"></div>')
            .addClass(classChange + " monitor-box status-" + getFinalStatus(status, curExe.falseNegative));
    let row1 = $("<div style='margin-right:0px'></div>").addClass("row");

    let r1c1 = $("<div></div>").addClass("col-xs-6 status").append("<span class='" + fonta + "' style='margin-right: 5px;'></span>" + status);
    row1.append(r1c1);

    let r1c2 = $("<div data-exestart=" + exedate.getTime() + " ></div>").addClass("col-xs-6 text-right exe-counter").append(getHumanReadableDuration((now - exedate) / 1000, 1));
    row1.append(r1c2);

    let row2 = $("<div style='margin-right:0px'></div>").addClass("row");
    r2c = $('<div></div>').addClass("col-xs-1 pull-left bold").append(curExe.testCase);
    row2.append(r2c);
    for (var i = 0, max = exes.length; ((i < max) && (i < maxPreviousExe)); i++) {
        let r2c;
        r2c = $('<div data-toggle="tooltip" data-html="true" title data-original-title="' + getTooltip(contentExe[exes[i]]) + '" onclick="window.open(\'./TestCaseExecution.jsp?executionId=' + exes[i] + '\');stopPropagation(event);"></div>')
                .addClass("monitor-sub-box col-xs-1 pull-right status-" + getFinalStatus(contentExe[exes[i]].controlStatus, contentExe[exes[i]].falseNegative));
        row2.prepend(r2c);

    }

    cel.append(row1).append(row2);
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
    htmlRes += '<div style=\'margin-top:5px;\'><span class=\'bold\'>Test : </span>' + data.test + SEPARATOR + data.testCase + '</div>';
    htmlRes += '<div>' + data.description + '</div>';
    htmlRes += '<div style=\'margin-top:5px;\'><span class=\'bold\'>Environment : </span>' + data.environment + '</div>';
    htmlRes += '<div><span class=\'bold\'>Country : </span>' + data.country + '</div>';
    htmlRes += '<div><span class=\'bold\'>Application : </span>' + data.application + '</div>';
    if ((data.robot !== undefined) && (data.robot !== '')) {
        htmlRes += '<div><span class=\'bold\'>Robot Decli : </span>' + data.robot + '</div>';
    }
    htmlRes += '<div style=\'margin-top:5px;\'><span class=\'bold\'>Start : </span>' + getDate(data.start) + '</div>';
    let dur = data.end - data.start;
    if (getDateShort(data.end) !== "") {
        htmlRes += '<div><span class=\'bold\'>End : </span>' + getDate(data.end) + ' <span class=\'' + getClassDuration(dur) + '\'>(' + getHumanReadableDuration(dur / 1000, 2) + ')</span></div>';
    }
    if (!data.usefull || data.muted) {
        let retryTag = !data.usefull ? "[RETRY]" : "";
        let mutedTag = data.muted ? "[MUTED]" : "";
        htmlRes += '<div style=\'margin-top:5px;\'>' + retryTag + " " + mutedTag + '</div>';
    }
    htmlRes += '<div style=\'margin-top:5px;\'>' + ctrlmessage + '</div>';

    return htmlRes;
}


