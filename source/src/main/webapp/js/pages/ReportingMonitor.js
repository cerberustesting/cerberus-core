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

var maxPreviousExe = 5;


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

        var campaigns = GetURLParameters("campaigns");
        var systems = GetURLParameters("systems");
        var environments = GetURLParameters("environments");
        //        

        $("#campaignSelect").empty();
        $("#campaignSelect").select2({width: "100%"});
        feedCampaignCombos("#campaignSelect", campaigns, environments);
        $("#systemSelect").empty();
        $("#systemSelect").select2({width: "100%"});
        feedSystemSelectOptions("#systemSelect");
        $("#envSelect").empty();
        $("#envSelect").select2({width: "100%"});
//        feedSystemSelectOptions("#envSelect");

        loadMonitoringBoard(systems, campaigns, environments);


    });
});


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

function feedSystemSelectOptions(selectElement) {
    var systemList = $(selectElement);
    systemList.empty();

    let user = JSON.parse(sessionStorage.getItem('user'));
    let systems = user.system;
    let options = $("#systemSelect").html("");
    $.each(systems, function (index, value) {
        option = `<option value="${value}">${value}</option>`;
        systemList.append(option);
    });
//    $('#systemSelect').html(options);
//    $("#systemSelect").multiselect('rebuild');
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



function loadMonitoringBoard(systems, campaigns, environments) {
//    console.info(systems);
//    console.info(campaigns);
//    console.info(environments);

// Call WS and refresh table if necessary.

    buildTable();

// Feed contain of the table.

}


function buildTable() {



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



    let data = {
        "executions": {
            "Examples|4027A|FR|QA|": [
                {
                    "country": "FR",
                    "controlStatus": "OK",
                    "test": "Examples",
                    "controlMessage": "The test case finished successfully",
                    "start": 1753638622307,
                    "description": "ex1",
                    "robot": "",
                    "environment": "QA",
                    "system": "CERBERUS",
                    "application": "Dummy-SRV-jira",
                    "environmentData": "QA",
                    "end": 1753638635557,
                    "tag": "tutuè20250723-192029 with space",
                    "id": 755888,
                    "testCase": "4027A"
                },
                {
                    "country": "FR",
                    "controlStatus": "OK",
                    "test": "Examples",
                    "controlMessage": "The test case finished successfully",
                    "start": 1753638691412,
                    "description": "ex1",
                    "robot": "",
                    "environment": "QA",
                    "system": "CERBERUS",
                    "application": "Dummy-SRV-jira",
                    "environmentData": "QA",
                    "end": 1753638697771,
                    "tag": "tutuè20250723-192029 with space",
                    "id": 755889,
                    "testCase": "4027A"
                },
                {
                    "country": "FR",
                    "controlStatus": "KO",
                    "test": "Examples",
                    "controlMessage": "The test case finished, but failed on validations.",
                    "start": 1753638754811,
                    "description": "ex1",
                    "robot": "",
                    "environment": "QA",
                    "system": "CERBERUS",
                    "application": "Dummy-SRV-jira",
                    "environmentData": "QA",
                    "end": 1753638758452,
                    "tag": "tutuè20250727-195231 with space",
                    "id": 755896,
                    "testCase": "4027A"
                },
                {
                    "country": "FR",
                    "controlStatus": "OK",
                    "test": "Examples",
                    "controlMessage": "The test case finished successfully",
                    "start": 1753638765697,
                    "description": "ex1",
                    "robot": "",
                    "environment": "QA",
                    "system": "CERBERUS",
                    "application": "Dummy-SRV-jira",
                    "environmentData": "QA",
                    "end": 1753638774642,
                    "tag": "tutuè20250727-195231 with space",
                    "id": 755901,
                    "testCase": "4027A"
                }
            ],
            "Examples|4030A|BE|QA|": [
                {
                    "country": "BE",
                    "controlStatus": "KO",
                    "test": "Examples",
                    "controlMessage": "The test case finished, but failed on validations.",
                    "start": 1753638756823,
                    "description": "ex1",
                    "robot": "",
                    "environment": "QA",
                    "system": "CERBERUS",
                    "application": "Dummy-SRV-gitlab",
                    "environmentData": "QA",
                    "end": 1753638764611,
                    "tag": "tutuè20250727-195231 with space",
                    "id": 755899,
                    "testCase": "4030A"
                }
            ],
            "Examples|4027A|BE|QA|": [
                {
                    "country": "BE",
                    "controlStatus": "FA",
                    "test": "Examples",
                    "controlMessage": "The test case failed to be executed because of an action.",
                    "start": 1753638752281,
                    "description": "ex1",
                    "robot": "",
                    "environment": "QA",
                    "system": "CERBERUS",
                    "application": "Dummy-SRV-jira",
                    "environmentData": "QA",
                    "end": 1753638754544,
                    "tag": "tutuè20250727-195231 with space",
                    "id": 755891,
                    "testCase": "4027A"
                },
                {
                    "country": "BE",
                    "controlStatus": "FA",
                    "test": "Examples",
                    "controlMessage": "The test case failed to be executed because of an action.",
                    "start": 1753638758548,
                    "description": "ex1",
                    "robot": "",
                    "environment": "QA",
                    "system": "CERBERUS",
                    "application": "Dummy-SRV-jira",
                    "environmentData": "QA",
                    "end": 1753638765599,
                    "tag": "tutuè20250727-195231 with space",
                    "id": 755900,
                    "testCase": "4027A"
                },
                {
                    "country": "BE",
                    "controlStatus": "FA",
                    "test": "Examples",
                    "controlMessage": "The test case failed to be executed because of an action.",
                    "start": 1753638774751,
                    "description": "ex1",
                    "robot": "",
                    "environment": "QA",
                    "system": "CERBERUS",
                    "application": "Dummy-SRV-jira",
                    "environmentData": "QA",
                    "end": 1753638780784,
                    "tag": "tutuè20250727-195231 with space",
                    "id": 755903,
                    "testCase": "4027A"
                },
                {
                    "country": "BE",
                    "controlStatus": "FA",
                    "test": "Examples",
                    "controlMessage": "The test case failed to be executed because of an action.",
                    "start": 1753638780903,
                    "description": "ex1",
                    "robot": "",
                    "environment": "QA",
                    "system": "CERBERUS",
                    "application": "Dummy-SRV-jira",
                    "environmentData": "QA",
                    "end": 1753638783595,
                    "tag": "tutuè20250727-195231 with space",
                    "id": 755904,
                    "testCase": "4027A"
                }
            ],
            "Examples|4034A|FR|QA|LocalRobot": [
                {
                    "country": "FR",
                    "controlStatus": "OK",
                    "test": "Examples",
                    "controlMessage": "The test case finished successfully",
                    "start": 1753638754221,
                    "description": "ex1",
                    "robot": "LocalRobot",
                    "environment": "QA",
                    "system": "CERBERUS",
                    "application": "Dummy-GUI",
                    "environmentData": "QA",
                    "end": 1753638763277,
                    "tag": "tutuè20250727-195231 with space",
                    "id": 755894,
                    "testCase": "4034A"
                }
            ],
            "Examples|4030A|FR|QA|": [
                {
                    "country": "FR",
                    "controlStatus": "OK",
                    "test": "Examples",
                    "controlMessage": "The test case finished successfully",
                    "start": 1753638874307,
                    "description": "ex1",
                    "robot": "",
                    "environment": "QA",
                    "system": "CERBERUS",
                    "application": "Dummy-SRV-gitlab",
                    "environmentData": "QA",
                    "end": 1753638874896,
                    "tag": "tutuè20250727-195231 with space",
                    "id": 755906,
                    "testCase": "4030A"
                }
            ],
            "Examples|4026 A|BE|QA|": [
                {
                    "country": "BE",
                    "controlStatus": "KO",
                    "test": "Examples",
                    "controlMessage": "The test case failed to be executed because of an action.",
                    "start": 1753638752269,
                    "description": "ex1",
                    "robot": "",
                    "environment": "QA",
                    "system": "CERBERUS",
                    "application": "Dummy-SRV",
                    "environmentData": "QA",
                    "end": 1753638753284,
                    "tag": "tutuè20250727-195231 with space",
                    "id": 755890,
                    "testCase": "4026 A"
                },
                {
                    "country": "BE",
                    "controlStatus": "FA",
                    "test": "Examples",
                    "controlMessage": "The test case failed to be executed because of an action.",
                    "start": 1753638754231,
                    "description": "ex1",
                    "robot": "",
                    "environment": "QA",
                    "system": "CERBERUS",
                    "application": "Dummy-SRV",
                    "environmentData": "QA",
                    "end": 1753638754533,
                    "tag": "tutuè20250727-195231 with space",
                    "id": 755895,
                    "testCase": "4026 A"
                },
                {
                    "country": "BE",
                    "controlStatus": "FA",
                    "test": "Examples",
                    "controlMessage": "The test case failed to be executed because of an action.",
                    "start": 1753638754231,
                    "description": "ex1",
                    "robot": "",
                    "environment": "QA",
                    "system": "CERBERUS",
                    "application": "Dummy-SRV",
                    "environmentData": "QA",
                    "end": 1753638754533,
                    "tag": "tutuè20250727-195231 with space",
                    "id": 755895,
                    "testCase": "4026 A"
                },
                {
                    "country": "BE",
                    "controlStatus": "FA",
                    "test": "Examples",
                    "controlMessage": "The test case failed to be executed because of an action.",
                    "start": 1753638754231,
                    "description": "ex1",
                    "robot": "",
                    "environment": "QA",
                    "system": "CERBERUS",
                    "application": "Dummy-SRV",
                    "environmentData": "QA",
                    "end": 1753638754533,
                    "tag": "tutuè20250727-195231 with space",
                    "id": 755895,
                    "testCase": "4026 A"
                },
                {
                    "country": "BE",
                    "controlStatus": "FA",
                    "test": "Examples",
                    "controlMessage": "The test case failed to be executed because of an action.",
                    "start": 1753638754231,
                    "description": "ex1",
                    "robot": "",
                    "environment": "QA",
                    "system": "CERBERUS",
                    "application": "Dummy-SRV",
                    "environmentData": "QA",
                    "end": 1753638754533,
                    "tag": "tutuè20250727-195231 with space",
                    "id": 755895,
                    "testCase": "4026 A"
                },
                {
                    "country": "BE",
                    "controlStatus": "FA",
                    "test": "Examples",
                    "controlMessage": "The test case failed to be executed because of an action.",
                    "start": 1753638754231,
                    "description": "ex1",
                    "robot": "",
                    "environment": "QA",
                    "system": "CERBERUS",
                    "application": "Dummy-SRV",
                    "environmentData": "QA",
                    "end": 1753638754533,
                    "tag": "tutuè20250727-195231 with space",
                    "id": 755895,
                    "testCase": "4026 A"
                },
                {
                    "country": "BE",
                    "controlStatus": "OK",
                    "test": "Examples",
                    "controlMessage": "The test case failed to be executed because of an action.",
                    "start": 1753638755825,
                    "description": "ex1",
                    "robot": "",
                    "environment": "QA",
                    "system": "CERBERUS",
                    "application": "Dummy-SRV",
                    "environmentData": "QA",
                    "end": 1753638756722,
                    "tag": "tutuè20250727-195231 with space",
                    "id": 755898,
                    "testCase": "4026 A"
                }
            ],
            "Examples|4028A|BE|QA|": [
                {
                    "country": "BE",
                    "controlStatus": "KO",
                    "test": "Examples",
                    "controlMessage": "The test case finished successfully",
                    "start": 1753638797924,
                    "description": "ex1",
                    "robot": "",
                    "environment": "QA",
                    "system": "CERBERUS",
                    "application": "Dummy-SRV-azuredevops",
                    "environmentData": "QA",
                    "end": 1753638820793,
                    "tag": "tutuè20250727-195231 with space",
                    "id": 755905,
                    "testCase": "4028A"
                }
            ],
            "Examples|4029A|FR|QA|": [
                {
                    "country": "FR",
                    "controlStatus": "OK",
                    "test": "Examples",
                    "controlMessage": "The test case finished successfully",
                    "start": 1753638754207,
                    "description": "ex1",
                    "robot": "",
                    "environment": "QA",
                    "system": "CERBERUS",
                    "application": "Dummy-SRV-github",
                    "environmentData": "QA",
                    "end": 1753638756356,
                    "tag": "tutuè20250727-195231 with space",
                    "id": 755893,
                    "testCase": "4029A"
                }
            ],
            "Examples|4026 A|FR|QA|": [
                {
                    "country": "FR",
                    "controlStatus": "OK",
                    "test": "Examples",
                    "controlMessage": "The test case finished successfully",
                    "start": 1753638753405,
                    "description": "ex1",
                    "robot": "",
                    "environment": "QA",
                    "system": "CERBERUS",
                    "application": "Dummy-SRV",
                    "environmentData": "QA",
                    "end": 1753638754116,
                    "tag": "tutuè20250727-195231 with space",
                    "id": 755892,
                    "testCase": "4026 A"
                }
            ],
            "Examples|4028A|FR|QA|": [
                {
                    "country": "FR",
                    "controlStatus": "OK",
                    "test": "Examples",
                    "controlMessage": "The test case finished successfully",
                    "start": 1753638774740,
                    "description": "ex1",
                    "robot": "",
                    "environment": "QA",
                    "system": "CERBERUS",
                    "application": "Dummy-SRV-azuredevops",
                    "environmentData": "QA",
                    "end": 1753638797846,
                    "tag": "tutuè20250727-195231 with space",
                    "id": 755902,
                    "testCase": "4028A"
                }
            ]
        },
        "tests": {
            "Examples|4029A": 1,
            "Examples|4034A": 1,
            "Examples|4026 A": 5,
            "Examples|4028A": 2,
            "Examples|4030A": 2,
            "Examples|4027A": 8
        },
        "environments": {
            "FR|QA|": 8,
            "BE|QA|": 10,
            "FR|QA|LocalRobot": 1
        }
    };

    let monTable = $("#tableMonitor");

    let columns = Object.keys(data.environments);
//    columns.push("COL1");
//    columns.push("COL2");
//    columns.push("COL3");
//    columns.push("COL4");
//    columns.push("COL5");

    let rows = Object.keys(data.tests);
//    rows.push("ROW1");
//    rows.push("ROW2");
//    rows.push("ROW3");
//    rows.push("ROW4");
//    rows.push("ROW5");
//    rows.push("ROW6");
//    rows.push("ROW7");
//    rows.push("ROW8");
//    rows.push("ROW9");
//    rows.push("ROW10");

    monTable.empty();

//    console.info(rows);
//    console.info(columns);

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
//                console.info(rows[j - 1] + "|" + columns[i]);
                cel.append(renderCel(i + "-" + j, data.executions[rows[j - 1] + "|" + columns[i]]));
                row.append(cel);
            }
            monTable.append(row);
        }


    }
    showTitleWhenTextOverflow();

}


function renderCel(id, content) {
//    console.info(id);

    if (content === undefined) {
        return $("<div id='cel-" + id + "'></div>");
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
        for (var i = content.length - 2, min = 0; i >= min; i--) {
            exes.push(content[i]);
        }
    }

//    console.info(exes);

    let now = new Date();


    let tooltipcontain = getTooltip(curExe);
//    let tooltipcontain = "";


    let cel = $('<div data-toggle="tooltip" data-html="true" title data-original-title="' + tooltipcontain + '" id="cel-' + id + '" onclick="window.open(\'./TestCaseExecution.jsp?executionId=' + curExe.id + '\');"></div>')
            .addClass("monitor-box status-" + status);
    let row1 = $("<div style='margin-right:0px'></div>").addClass("row");

    let r1c1 = $("<div></div>").addClass("col-xs-6 status").append("<span class='" + fonta + "' style='margin-right: 5px;'></span>" + status);
    row1.append(r1c1);

    let r1c2 = $("<div></div>").addClass("col-xs-6 text-right").append(getHumanReadableDuration((now - exedate) / 1000, 1));
    row1.append(r1c2);

    let row2 = $("<div style='margin-right:0px'></div>").addClass("row pull-right");
    for (var i = 0, max = exes.length; ((i < max) && (i < maxPreviousExe)); i++) {
        let r2c;
        r2c = $('<div data-toggle="tooltip" data-html="true" title data-original-title="' + getTooltip(exes[i]) + '" onclick="window.open(\'./TestCaseExecution.jsp?executionId=' + exes[i].id + '\');stopPropagation(event);"></div>').addClass("monitor-sub-box col-xs-1 status-" + exes[i].controlStatus);
        row2.append(r2c);

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
    htmlRes += '<div><span class=\'bold\'>Environment : </span>' + data.environment + '</div>';
    htmlRes += '<div><span class=\'bold\'>Country : </span>' + data.country + '</div>';
    if ((data.robot !== undefined) && (data.robot !== '')) {
        htmlRes += '<div><span class=\'bold\'>Robot Decli : </span>' + data.robot + '</div>';
    }
    htmlRes += '<div><span class=\'bold\'>Start : </span>' + getDate(data.start) + '</div>';
    let dur = data.end - data.start;
    if (getDateShort(data.end) !== "") {
        htmlRes += '<div><span class=\'bold\'>End : </span>' + getDate(data.end) + ' <span class=\'' + getClassDuration(dur) + '\'>(' + getHumanReadableDuration(dur / 1000, 2) + ')</span></div>';
    }
    htmlRes += '<div>' + ctrlmessage + '</div>';

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


