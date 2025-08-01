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

var maxPreviousExe = 3;
var layoutMode = "pileup";
var SEPARATOR = "-";

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
        layoutMode = GetURLParameter("layout", layoutMode);
        //        

        feedLayoutSelectOptions(layoutMode);
//        $("#campaignSelect").empty();
//        $("#campaignSelect").select2({width: "100%"});
//        feedCampaignCombos("#campaignSelect", campaigns, environments);
        $("#systemSelect").empty();
        $("#systemSelect").select2({width: "100%"});
        feedSystemSelectOptions("#systemSelect", systems);
        $("#envSelect").empty();
        $("#envSelect").select2({width: "100%"});
        feedEnvironmentSelectOptions("#envSelect", environments);
        $("#countrySelect").empty();
        $("#countrySelect").select2({width: "100%"});
        feedCountrySelectOptions("#countrySelect", countries);

        openSocketAndBuildTable(systems, environments, countries);


    });
});

function feedLayoutSelectOptions(layoutMode) {
    if (layoutMode === "testcase") {
        $("#layoutMode").find('.btn-1').addClass('btn-primary active');
        $("#layoutMode").find('.btn-2').removeClass('btn-primary');
    } else {
        $("#layoutMode").find('.btn-1').removeClass('btn-primary');
        $("#layoutMode").find('.btn-2').addClass('btn-primary active');
    }
}

function loadBoard(layoutModeNew) {

//    $("#layoutMode").find('.btn').toggleClass('active');
//            if ($(this).find('.btn').size() > 0) {
    if (layoutModeNew === "testcase") {
        layoutMode = "testcase";
        $("#layoutMode").find('.btn-1').addClass('btn-primary active');
        $("#layoutMode").find('.btn-2').removeClass('btn-primary active');
    } else {
        layoutMode = "pileup";
        $("#layoutMode").find('.btn-1').removeClass('btn-primary active');
        $("#layoutMode").find('.btn-2').addClass('btn-primary active');
    }

    let systems = undefined;
    let environments = undefined;
    let countries = undefined;

    let systemQ = "";
    if ($("#systemSelect").val() !== null) {
        for (var i = 0; i < $("#systemSelect").val().length; i++) {
            systemQ = systemQ + "&systems=" + encodeURI($("#systemSelect").val()[i]);
        }
    }

    let environmentsQ = "";
    if ($("#envSelect").val() !== null) {
        for (var i = 0; i < $("#envSelect").val().length; i++) {
            environmentsQ = environmentsQ + "&environments=" + encodeURI($("#envSelect").val()[i]);
        }
    }

    let countriesQ = "";
    if ($("#countrySelect").val() !== null) {
        for (var i = 0; i < $("#countrySelect").val().length; i++) {
            countriesQ = countriesQ + "&countries=" + encodeURI($("#countrySelect").val()[i]);
        }
    }

    let qS = "layout=" + layoutMode + systemQ + environmentsQ + countriesQ;

    InsertURLInHistory("./ReportingMonitor.jsp?" + qS);

    openSocketAndBuildTable(systems, environments, countries);

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


/***
 * Feed the TestCase select with all the testcase from test defined.
 * @param {String} selectElement - id of select to refresh.
 * @param {String} defaultCampaigns - value of default campaign.
 * @param {String} environments - list of selected environments.
 * @param {String} gp1s - list of selected gp1s.
 * @param {String} gp2s - list of selected gp2s.
 * @param {String} gp3s - list of selected gp3s.
 * @returns {null}
 */
function feedCampaignCombos(selectElement, defaultCampaigns, environments, gp1s, gp2s, gp3s) {

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

//    $('#systemSelect').html(options);
//    $("#systemSelect").multiselect('rebuild');
}

function feedEnvironmentSelectOptions(selectElement, environmentsLoad) {
    var envList = $(selectElement);
    envList.empty();

    let envs = getInvariantArray("ENVIRONMENT", false, undefined, false);
//    let user = JSON.parse(sessionStorage.getItem('user'));
//    let envs = user.system;

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

    let countries = getInvariantArray("COuNTRY", false);
    let options = $("#countrySelect").html("");
    $.each(countries, function (index, value) {
        option = `<option value="${value}">${value}</option>`;
        countryList.append(option);
    });
    $('#envSelect').val(countriesLoad);
//    console.info(countriesLoad);

}

function loadEnvironmentCombo(data) {

    var select = $("#envSelect");
    select.multiselect('destroy');
    var array = data.distinct.environments;
    $("#envSelect option").remove();
    for (var i = 0; i < array.length; i++) {
        let n = array[i].name;
        if (isEmpty(n)) {
            n = "[Empty]";
        }
        $("#envSelect").append($('<option></option>').text(n).val(array[i].name));
    }
    for (var i = 0; i < array.length; i++) {
        if (array[i].isRequested) {
            $("#envSelect option[value='" + array[i].name + "']").attr("selected", "selected");
        }
    }
    select.multiselect(new multiSelectConfPerf("envSelect"));
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

/*
 * Loading functions
 */

function openSocketAndBuildTable(systems, environments, countries) {
//    console.info(layoutMode);
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
    var socket = new WebSocket(new_uri);

    socket.onopen = function (e) {
        console.info("ws onopen");
    }; //on "écoute" pour savoir si la connexion vers le serveur websocket s'est bien faite
    socket.onmessage = function (e) {
        var data = JSON.parse(e.data);
//        console.info("received data from socket");
        console.info("ws onmessage");
        console.info(data);
        refreshMonitorTable(data, systems, environments, countries);
//        updatePageQueueStatus(data);
//        updatePage(data, steps);
    }; //on récupère les messages provenant du serveur websocket
    socket.onclose = function (e) {
        console.info("ws onclose");
    }; //on est informé lors de la fermeture de la connexion vers le serveur
    socket.onerror = function (e) {
        console.info("ws onerror");
    }; //on traite les cas d'erreur*/

    // Remain in memory
    sockets.push(socket);
}


function refreshMonitorTable(data, systems, environments, countries) {

    let monTable = $("#tableMonitor");

    let columns = Object.keys(data.environments);
    let rows = Object.keys(data.tests);

    let indexValues = {};
    document.querySelectorAll('.monitor-box').forEach((item, index) => {
//        console.info(item);
//        console.info(index);
//
//        console.info(item.getAttribute("data-tag"));
        exeId = item.getAttribute("data-exeid");
        id = item.getAttribute("id");
        indexValues[id] = exeId;

    });
//    console.info("INDEX");
//    console.info(indexValues);

    monTable.empty();

//    console.info(rows);
//    console.info(columns);
    if (layoutMode === undefined) {
        layoutMode = "piledup";
    } // testcase or piledup

//    console.info(layoutMode);

    if (layoutMode === "testcase") {
        for (var j = 0, maxr = (rows.length + 1); j < maxr; j++) {
            var row = $("<tr></tr>");

            if (j === 0) {

                var cel = $("<td></td>");
                row.append(cel);
                for (var i = 0, maxc = (columns.length); i < maxc; i++) {
//                console.info(i + columns[i]);

                    var cel = $("<td style='text-align: center'></td>");
                    cel.append(columns[i]);
                    row.append(cel);
                }
                monTable.append(row);

            } else {

                var cel = $("<td style='text-align: center;vertical-align: middle'></td>");
                cel.append(rows[j - 1]);
                row.append(cel);


                for (var i = 0, maxc = (columns.length); i < maxc; i++) {
//                console.info(i + columns[i]);

                    var cel = $("<td></td>");
//                console.info(rows[j - 1] + SEPARATOR + columns[i]);
                    cel.append(renderCel(rows[j - 1] + SEPARATOR + columns[i], data.executions[rows[j - 1] + SEPARATOR + columns[i]], indexValues));
                    row.append(cel);
                }
                monTable.append(row);
            }
        }
    } else { //pileup


        // 1st row
        var row = $("<tr></tr>");
//        var cel = $("<td></td>");
//        row.append(cel);
        for (var i = 0, maxc = (columns.length); i < maxc; i++) {
//                console.info(i + columns[i]);
            let cel = $("<td style='text-align: center'></td>");
            cel.append(columns[i]);
            row.append(cel);
        }
        monTable.append(row);


        // 2nd row
        // 
        // 2nd row - 1st column
        row = $("<tr></tr>");
//        var cel = $("<td style='text-align: center;vertical-align: middle'></td>");
//        for (var j = 1, maxr = (rows.length + 1); j < maxr; j++) {
//            cel.append(rows[j - 1]);
//            cel.append("<br>");
//        }
//        row.append(cel);

        // 2nd row - all other columns
        for (var i = 0, maxc = (columns.length); i < maxc; i++) {
//                console.info(i + columns[i]);

            var cel = $("<td></td>");
//                console.info(rows[j - 1] + SEPARATOR + columns[i]);
            for (var j = 1, maxr = (rows.length + 1); j < maxr; j++) {
                cel.append(renderCel(rows[j - 1] + SEPARATOR + columns[i], data.executions[rows[j - 1] + SEPARATOR + columns[i]], indexValues));
                row.append(cel);
            }
            monTable.append(row);
        }
    }

    $(".tooltip").remove();
    showTitleWhenTextOverflow();

}



function renderCel(id, content, indexValues) {
//    console.info(id);

    if (content === undefined) {
        return "";
//        return $("<div id='cel-" + id + "' style='margin-bottom: 2px'></div>");
    }
//    console.info(content);

    let curExe = content[content.length - 1];
//    console.info(curExe);

    let status = curExe.controlStatus;
//    let fa = "fa fa-bug";   fa fa-check

    let fonta = "fa fa-exclamation";
    if (status === "OK") {
        fonta = "fa fa-check";
    } else if (status === "KO") {
        fonta = "fa fa-bug";
    } else if (status === "FA") {
        fonta = "fa fa-exclamation";
    }

    let exedate = new Date(curExe.start);
    let exes = [];
    if (content.length - 1 >= 1) {
//        for (var i = content.length - 2, min = 0; i >= min; i--) {
//            exes.push(content[i]);
//        }
        for (var i = content.length - 2, min = 0; i >= min; i--) {
            exes.push(content[i]);
        }
    }

//    console.info(exes);

    let now = new Date();


    let tooltipcontain = getTooltip(curExe);
//    let tooltipcontain = "";
//    let previousCellExeId = $("#" + id).attr("data-exeid");
//    console.info($("#" + id));
    let previousCellExeId = indexValues[id];
        let classChange = "";
    if ((previousCellExeId !== undefined) && (previousCellExeId != curExe.id)) {
//        console.info("CHANGE on " + id);
        classChange = "new blinking";
    }
//    console.info(curExe.id + " " + previousCellExeId);

    let cel = $('<div style="margin-bottom: 2px" data-toggle="tooltip" data-html="true" title data-original-title="' + tooltipcontain + '" id="' + id + '" data-exeid="' + curExe.id + '" onclick="window.open(\'./TestCaseExecution.jsp?executionId=' + curExe.id + '\');"></div>')
            .addClass(classChange + " monitor-box status-" + status);
    let row1 = $("<div style='margin-right:0px'></div>").addClass("row");

    let r1c1 = $("<div></div>").addClass("col-xs-6 status").append("<span class='" + fonta + "' style='margin-right: 5px;'></span>" + status);
    row1.append(r1c1);

    let r1c2 = $("<div></div>").addClass("col-xs-6 text-right").append(getHumanReadableDuration((now - exedate) / 1000, 1));
    row1.append(r1c2);

    let row2 = $("<div style='margin-right:0px'></div>").addClass("row");
    r2c = $('<div></div>').addClass("col-xs-1 pull-left bold").append(curExe.testCase);
    row2.append(r2c);
    for (var i = 0, max = exes.length; ((i < max) && (i < maxPreviousExe)); i++) {
        let r2c;
        r2c = $('<div data-toggle="tooltip" data-html="true" title data-original-title="' + getTooltip(exes[i]) + '" onclick="window.open(\'./TestCaseExecution.jsp?executionId=' + exes[i].id + '\');stopPropagation(event);"></div>').addClass("monitor-sub-box col-xs-1 pull-right status-" + exes[i].controlStatus);
        row2.prepend(r2c);

    }


    cel.append(row1).append(row2);
    return cel;

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
    htmlRes += '<div style=\'margin-top:5px;\'>' + ctrlmessage + '</div>';

    return htmlRes;
}

function getTextPlurial(nb, textSingle, textPlusial) {
    if (nb > 1) {
        return "" + nb + textPlusial;
    } else if (nb === 1) {
        return "" + nb + textSingle;
    } else {
        return "";
    }
}


