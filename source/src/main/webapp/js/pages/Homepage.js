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

var statusOrder = ["OK", "KO", "FA", "NA", "NE", "WE", "PE", "QU", "QE", "PA", "CA"];
var configTcBar = {};
var nbTagLoaded = 0;
var nbTagLoadedTarget = 0;
var futureCampaignRunTime = [];
var futureCampaignRunTimeDurationToTrigger = [];
var idTimeout;

$.when($.getScript("js/global/global.js")).then(function () {
    $(document).ready(function () {
        displayPageLabel();

        bindToggleCollapse();

        initHPGraph_TestCaseAndExecution();

        loadExecutionsHistoBar();
        loadTestcaseHistoGraph();

        loadExeCurrentlyRunning();

        $('body').tooltip({
            selector: '[data-toggle="tooltip"]'
        });
        $('[data-toggle="popover"]').popover({
            'placement': 'auto',
            'container': 'body'
        }
        );

//        $("#tagSettingsModal").on('hidden.bs.modal', modalCloseHandler);

        $("#selectTag").on('change', function () {
            var tagListForm = $("#tagList");
            var selectedTag = $("#selectTag").val();

            if (selectedTag !== "") {
                tagListForm.append('<div class="input-group">\n\
                                    <span class="input-group-addon removeTag"><span class="glyphicon glyphicon-remove"></span></span>\n\
                                    <input type="tag" name="tag" class="form-control" id="tag" value="' + selectedTag + '" readonly>\n\
                                    </div>');
            }
            $("#selectTag").val("");
            $(".removeTag").on('click', function () {
                $(this).parent().remove();
            });
        });

        //configure and create the dataTable
        var jqxhr = $.getJSON("Homepage", "e=1" + getUser().defaultSystemsQuery);

        $.when(jqxhr).then(function (result) {
            var configurations = new TableConfigurationsClientSide("homePageTable", result["aaData"], aoColumnsFunc(), true);
            configurations.tableWidth = "550px";
            configurations.showColvis = false;
            if ($('#homePageTable').hasClass('dataTable') === false) {
                createDataTableWithPermissions(configurations, undefined, "#applicationPanel");
                showTitleWhenTextOverflow();
            } else {
                var oTable = $("#homePageTable").dataTable();
                oTable.fnClearTable();
                if (result["aaData"].length > 0) {
                    oTable.fnAddData(result["aaData"]);
                }
            }

        }).fail(handleErrorAjaxAfterTimeout);

        loadLastTagResultList();

        loadBuildRevTable();

        // Display Changelog;
        $("#documentationFrame").attr("src", "./documentation/D2/changelog_4.21_en.html");
        var windowsHeight = $(window).height() + 'px';
        $('#documentationFrame').css('height', '400px');
        $("#changelogLabel").html("Changelog 4.21");

        //close all sidebar menu
        //closeEveryNavbarMenu();
    });

    updateHeaderStats();

});


function loadQueueStatusWebSocket(sockets) {

    var parser = document.createElement('a');
    parser.href = window.location.href;

    var protocol = "ws:";
    if (parser.protocol === "https:") {
        protocol = "wss:";
    }
    var path = parser.pathname.split("Homepage")[0];
    var new_uri = protocol + parser.host + path + "api/ws/queuestatus";
    console.info("Open Socket to : " + new_uri);
    var socket = new WebSocket(new_uri);

    socket.onopen = function (e) {
    }; //on "écoute" pour savoir si la connexion vers le serveur websocket s'est bien faite
    socket.onmessage = function (e) {
        var data = JSON.parse(e.data);
//        console.info("received data from socket");
//        console.info(data);
        updatePageQueueStatus(data);
//        updatePage(data, steps);
    }; //on récupère les messages provenant du serveur websocket
    socket.onclose = function (e) {
    }; //on est informé lors de la fermeture de la connexion vers le serveur
    socket.onerror = function (e) {
    }; //on traite les cas d'erreur*/

    // Remain in memory
    sockets.push(socket);

}

function displayPageLabel() {
    var doc = new Doc();

    //displayHeaderLabel(doc);
    $("#lastTagExec").html(doc.getDocOnline("homepage", "lastTagExecution"));
//    $("#tagSettingsLabel").html(doc.getDocLabel("homepage", "btn_settings"));
    $("#modalTitle").html(doc.getDocLabel("homepage", "modal_title"));
    $("#testCaseStatusByApp").html(doc.getDocOnline("homepage", "testCaseStatusByApp"));
    $("#title").html(doc.getDocLabel("homepage", "title"));

    $("#reportStatus").html(doc.getDocOnline("page_integrationstatus", "environmentStatus"));
    $("#systemHeader").html(doc.getDocOnline("invariant", "SYSTEM"));
    $("#buildHeader").html(doc.getDocOnline("buildrevisioninvariant", "versionname01"));
    $("#revisionHeader").html(doc.getDocOnline("buildrevisioninvariant", "versionname02"));
    $("#devHeader").html(doc.getDocOnline("page_integrationstatus", "DEV"));
    $("#qaHeader").html(doc.getDocOnline("page_integrationstatus", "QA"));
    $("#uatHeader").html(doc.getDocOnline("page_integrationstatus", "UAT"));
    $("#prodHeader").html(doc.getDocOnline("page_integrationstatus", "PROD"));


    displayFooter(doc);
    displayGlobalLabel(doc);
}

function getSys() {
    const workspaceEl = document.querySelector('[x-data="workspaceSelector()"]');

    // Alpine pas encore prêt → fallback sessionStorage
    if (!workspaceEl || !workspaceEl.__x) {
        let user = JSON.parse(sessionStorage.getItem("user")) || {};
        return user.defaultSystems || [];
    }

    return workspaceEl.__x.$data.selected;
}

function readStatus() {
    var result;
    $.ajax({
        url: "FindInvariantByID",
        data: {idName: "TCSTATUS"},
        async: false,
        dataType: 'json',
        success: function (data) {
            result = data;
        }
    });
    return result;
}

function modalCloseHandler() {
    $("#tagList").empty();
    $("#selectTag").empty();
}

function loadTagFilter() {
    $("#selectTag").select2(getComboConfigTag());
}

function generateTagLink(tagName) {
    var link = '<a href="./ReportingExecutionByTag.jsp?Tag=' + encodeURIComponent(tagName) + '">' + tagName + '</a>';

    return link;
}

function updatePageQueueStatus(data) {

//  UnComment bellow for test and debug purpose. \/ \/ \/
//            let test = {
//                id: 123456,
//                application: "website",
//                test: "Exampe",
//                testcase: "0001A",
//                environment: "PROD",
//                country: "FR"
//            }
//            data.runningExecutionsList.push(test);
//            data.runningExecutionsList.push(test);
//            data.runningExecutionsList.push(test);
//            data.runningExecutionsList.push(test);
//            data.runningExecutionsList.push(test);
//            data.runningExecutionsList.push(test);
//            data.runningExecutionsList.push(test);
//            let queueStats = {
//                globalLimit: 10,
//                running: data.simultaneous_execution_list.length,
//                queueSize: 30
//            }
//            data.queueStats = queueStats;
//  UnComment above for test and debug purpose. /\ /\ /\

    if ((data.queueStats.running > 0) || (data.queueStats.queueSize > 0)) {
        $("#exeRunningPanel").show();

        // Execution Queue progress bar
        let totalQueue = data.queueStats.globalLimit + data.queueStats.queueSize
        let perRunning = data.queueStats.running / totalQueue * 100;
        let perIdle = (data.queueStats.globalLimit - data.queueStats.running) / totalQueue * 100;
        let perQueue = data.queueStats.queueSize / totalQueue * 100;

        $("#progress-barUsed").attr('data-original-title', data.queueStats.running + " running")
        $("#progress-barIdle").attr('data-original-title', (data.queueStats.globalLimit - data.queueStats.running) + " available slot")
        $("#progress-barQueue").attr('data-original-title', (data.queueStats.queueSize) + " still in queue")
        $("#progress-barUsed").attr('aria-valuenow', perRunning);
        $("#progress-barIdle").attr('aria-valuenow', perIdle);
        $("#progress-barQueue").attr('aria-valuenow', perQueue);
        $("#progress-barUsed").attr('style', "width: " + perRunning + "%;")
        $("#progress-barIdle").attr('style', "width: " + perIdle + "%;")
        $("#progress-barQueue").attr('style', "width: " + perQueue + "%;")

        // Execution Counter
        if (data.queueStats.queueSize > 0) {
            $("#exeRunningPanelCnt").text(data.queueStats.running + " / " + data.queueStats.queueSize);
        } else {
            $("#exeRunningPanelCnt").text(data.queueStats.running);
        }

        // Execution List
        $("#exeRunningList").empty();
        let contentCel = "";
        let contentCelNotDisplayed = "";
        // Filter list with only selected systems.
        let newList = [];
        for (var i = 0; i < data.runningExecutionsList.length; i++) {
            let exe = data.runningExecutionsList[i];
            if (getUser().defaultSystems.includes(exe.system)) {
                newList.push(exe);
            }
        }
//        console.info(data.runningExecutionsList);
        for (var i = 0; i < newList.length; i++) {
            let exe = newList[i]; 
            contentCel = "<div class='Exe-tooltip'><strong>Exe : </strong>" + exe.id + "</div>"
            contentCel += "<div class='Exe-tooltip'><strong>Application : </strong>" + exe.application + "</div>"
            contentCel += "<div class='Exe-tooltip'><strong>Testcase : </strong>" + exe.test + " - " + exe.testcase + "</div>"
            contentCel += "<div class='Exe-tooltip'><strong>Environment / Country : </strong>" + exe.environment + " " + exe.country + "</div>"
            contentCel += "<div class='Exe-tooltip'><strong>started </strong>" + getHumanReadableDuration((new Date().getTime() - new Date(exe.start).getTime()) / 1000) + " ago</div>"
            if (i > 3) {
                contentCelNotDisplayed += contentCel + "<div>-------------</div>";
            } else {
                $("#exeRunningList").append($('<a><span class=\'glyphicon glyphicon-expand\'></span></a>')
                        .attr("href", "TestCaseExecution.jsp?executionId=" + exe.id)
                        .attr('style', 'margin-left: 10px; font-size: 10px; background-color: var(--crb-grey-superlight-color); color: var(--crb-black-color)')
                        .attr('data-original-title', contentCel)
                        .attr('data-toggle', 'tooltip')
                        .attr('data-placement', 'bottom')
                        .attr('data-html', 'true')
                        );
            }

        }
//                console.info(contentCelNotDisplayed);
        if (contentCelNotDisplayed !== "") {
            $("#exeRunningList").append($('<a><span class=\'glyphicon glyphicon-option-horizontal\'></span></a>')
                    .attr("href", "TestCaseExecutionList.jsp")
                    .attr('style', 'margin-left: 10px; font-size: 10px; background-color: var(--crb-grey-superlight-color); color: var(--crb-black-color)')
                    .attr('data-original-title', contentCelNotDisplayed)
                    .attr('data-toggle', 'tooltip')
                    .attr('data-placement', 'bottom')
                    .attr('data-html', 'true')
                    );
        }

    } else {
        $("#exeRunningPanel").hide();

    }
}


function loadExeCurrentlyRunning() {

    $.ajax({
        url: "api/executions/running",
        method: "GET",
        async: true,
        dataType: 'json',
        success: function (data) {

            updatePageQueueStatus(data);

            sockets = [];
            loadQueueStatusWebSocket(sockets);

        }
    });
}



function loadExecutionsHistoBar() {
    showLoader($("#panelHistory"));

    const period = localStorage.getItem("execHistoryPeriod") || "1m";
    const { from, to } = getFromToByPeriod(period);

    $.ajax({
        url: "ReadExecutionTagHistory?from=" + from.toISOString() + "&to=" + to.toISOString() + getUser().defaultSystemsQuery,
        method: "GET",
        async: true,
        dataType: 'json',
        success: function (data) {
            buildExeBar(data);
            hideLoader($("#panelHistory"));
        }
    });
}

function buildExeBar(data) {

    let curves = data.curvesNb;

    // Sorting values by nb of requests.
    sortedCurves = curves.sort(function (a, b) {
        let a1 = a.key.nbExe;
        let b1 = b.key.nbExe;
        return b1 - a1;
    });


    var len = sortedCurves.length;

    let timedatasets = [];

    for (var i = 0; i < len; i++) {

        let c = sortedCurves[i];
        let d = [];
        lend = c.points.length;
        for (var j = 0; j < lend; j++) {
            let p = {
                x: c.points[j].x,
                y: c.points[j].y,
                id: c.points[j].exe,
                controlStatus: c.points[j].exeControlStatus
            };
            d.push(p);
        }
        let lab = c.key.key;
        var dataset = {
            label: lab,
            categoryPercentage: 1.0,
            barPercentage: 1.0,
            backgroundColor: getExeStatusRowColor(c.key.key),
            borderColor: getExeStatusRowColor(c.key.key),
            data: c.points,
            borderWidth: 3,
            borderRadius: 3,
            maxBarThickness: 6
        };
        timedatasets.push(dataset);
    }

    configHistoExeBar.data.datasets = timedatasets;
    configHistoExeBar.data.labels = data.curvesDatesNb;

    window.myLineExeHistoBar.update();
}

function getFromToByPeriod(period) {
    const to = new Date();
    const from = new Date();
    switch(period) {
        case "1w": from.setDate(to.getDate() - 7); break;
        case "2w": from.setDate(to.getDate() - 14); break;
        case "1m": from.setMonth(to.getMonth() - 1); break;
        case "2m": from.setMonth(to.getMonth() - 2); break;
        case "3m": from.setMonth(to.getMonth() - 3); break;
        default: from.setMonth(to.getMonth() - 1);
    }
    return { from, to };
}

function loadTestcaseHistoGraph() {
    showLoader($("#panelTcHistory"));

    const period = localStorage.getItem("tcHistoryPeriod") || "1m";
    const { from, to } = getFromToByPeriod(period);

    $.ajax({
        url: "ReadTestCaseStat?from=" + from.toISOString() + "&to=" + to.toISOString() + getUser().defaultSystemsQuery,
        method: "GET",
        async: true,
        dataType: 'json',
        success: function (data) {
            buildTcBar(data);
            hideLoader($("#panelTcHistory"));
        }
    });
}

function buildTcBar(data) {

    let curves = data.curvesNb;

    // Sorting values by nb of requests.
    sortedCurves = curves.sort(function (a, b) {
        let a1 = a.key.nbExe;
        let b1 = b.key.nbExe;
        return b1 - a1;
    });


    var len = sortedCurves.length;

    let timedatasets = [];

    for (var i = 0; i < len; i++) {

        let c = sortedCurves[i];
        let d = [];
        lend = c.points.length;
        for (var j = 0; j < lend; j++) {
            let p = {x: c.points[j].x, y: c.points[j].y};
            d.push(p);
        }
        let lab = c.key.key;

        var dataset = {
            label: lab,
            backgroundColor: "white",
            borderColor: get_Color_fromindex(i),
            pointBackgroundColor: get_Color_fromindex(i),
            borderWidth: 2,
            pointRadius: 2,
            pointHoverRadius: 3,
            hitRadius: 10,
            fill: false,
            data: c.points
        };

        timedatasets.push(dataset);
    }

    configHistoTcBar.data.datasets = timedatasets;
    configHistoTcBar.data.labels = data.curvesDatesNb;

    window.myLineTcHistoBar.update();
}


function initHPGraph_TestCaseAndExecution() {

    var exebaroption = getHPOptionsExeBar("Executions", "nb");
    var tcgraphoption = getHPOptionsTcGraph("Testcases", "nb");

    let exebardatasets = [];
    let tcgraphdatasets = [];

    configHistoExeBar = {
        type: 'bar',
        data: {
            datasets: exebardatasets
        },
        options: exebaroption
    };
    configHistoTcBar = {
        type: 'line',
        data: {
            datasets: tcgraphdatasets
        },
        options: tcgraphoption
    };

    var ctx = document.getElementById('canvasHistExePerStatus').getContext('2d');
    window.myLineExeHistoBar = new Chart(ctx, configHistoExeBar);

    var ctx = document.getElementById('canvasHistTcPerStatus').getContext('2d');
    window.myLineTcHistoBar = new Chart(ctx, configHistoTcBar);

}

function getHPOptionsExeBar(title, unit) {
    let option = {
        legend: {
            labels: {
                usePointStyle: true,
            },
        },
        title: {
            text: title
        },
        scales: {
            xAxes: [{
                    offset: true,
                    type: 'time',
                    stacked: true,
                    time: {
                        tooltipFormat: 'll',
                        unit: 'day',
                        round: 'day',
                        displayFormats: {
                            day: 'MMM D'
                        }
                    },
                    scaleLabel: {
                        display: true,
                        labelString: 'Date'
                    },
                    gridLines: {
                        color: "rgba(0, 0, 0, 0)",
                    }
                }],
            yAxes: [{
                    stacked: true,
                    ticks: {
                        beginAtZero: true
                    }
                }]
        }
    };
    return option;
}

function getHPOptionsTcGraph(title, unit) {
    let option = {
        hover: {
            mode: 'nearest',
            intersect: true
        },
        legend: {
            labels: {
                usePointStyle: true,
            },
        },
        title: {
            text: title
        },
        scales: {
            xAxes: [{
                    offset: false,
                    type: 'time',
                    stacked: false,
                    time: {
                        tooltipFormat: 'll',
                        unit: 'day',
                        round: 'day',
                        displayFormats: {
                            day: 'MMM D'
                        }
                    },
                    scaleLabel: {
                        display: true,
                        labelString: 'Date'
                    },
                    gridLines: {
                        color: "rgba(0, 0, 0, 0)"
                    }
                }],
            yAxes: [{
                    stacked: false,
                    ticks: {
                        beginAtZero: true
                    }
                }]
        }
    };
    return option;
}

function generateTooltip(data, tag, tagObj) {
    var htmlRes;
    var len = statusOrder.length;

    htmlRes = "<div class='tag-tooltip'><strong>Tag : </strong>" + tag;
    htmlRes += "<div>&nbsp;</div>";

    htmlRes += "<div>Started : " + getDate(tagObj.DateStartExe) + " (" + getHumanReadableDuration((new Date().getTime() - new Date(tagObj.DateStartExe).getTime()) / 1000) + " ago)</div>";
    if ((new Date(tagObj.DateEndQueue).getTime() - new Date(tagObj.DateStartExe).getTime()) > 0) {
        htmlRes += "<div>Duration : " + getHumanReadableDuration((new Date(tagObj.DateEndQueue).getTime() - new Date(tagObj.DateStartExe).getTime()) / 1000) + "</div>";
    }
    htmlRes += "<div>&nbsp;</div>";
    for (var index = 0; index < len; index++) {
        var status = statusOrder[index];

        if ((data.hasOwnProperty(status)) && (data[status] > 0)) {
            htmlRes += "<div>\n\
                        <span class='color-box status" + status + "'></span>\n\
                        <strong> " + status + " : </strong>" + data[status] + "</div>";
        }
    }
    htmlRes += '</div>';
    return htmlRes;
}

function generateTagReport(data, tag, rowId, tagObj) {
    var divId = "#tagExecStatusRow" + rowId;
    var reportArea = $(divId).attr("data-tag", tag);
    var buildBar;
    var tooltip = generateTooltip(data, tag, tagObj);
    var len = statusOrder.length;

    let ciRes = '';
    if (!isEmpty(tagObj.ciResult)) {
        ciRes = '<div class="status' + tagObj.ciResult + '" style="display: inline;align-text:right;">';
        ciRes += tagObj.ciResult;
        ciRes += '</div>';
    }

    buildBar = '<div><table style="width: 100%"><tr><td><div>' + generateTagLink(tag) + '</div></td><td style="text-align:right;">' + ciRes + '<div class="hidden-xs" style="display: inline;align-text:right;"> Total executions : ' + data.total + '</div></td></tr></table></div></div>\n\
                                                        <div class="progress" style="height:8px" data-toggle="tooltip" data-html="true" title="' + tooltip + '">';
    for (var index = 0; index < len; index++) {
        var status = statusOrder[index];

        if ((data.hasOwnProperty(status)) && (data[status] > 0)) {
            var percent = (data[status] / data.total) * 100;
            var roundPercent = Math.round(percent * 10) / 10;

            buildBar += '<div class="progress-bar status' + status + '" \n\
                role="progressbar" \n\
                style="width:' + percent + '%;color:transparent">' + roundPercent + '%</div>';
        }
    }
    buildBar += '</div>';
    reportArea.append(buildBar);
}

function loadLastTagResultList() {

    // Empty previous saved scheduled campaign timings and stop timer in case it was created.
    futureCampaignRunTime = [];
    futureCampaignRunTimeDurationToTrigger = [];
    clearTimeout(idTimeout);

    showLoader($("#LastTagExecPanel"));

    var reportArea = $("#tagExecStatus");
    reportArea.empty();
    nbTagLoaded = 0;

    //Get the last tag to display
//    var tagList = JSON.parse(localStorage.getItem("tagList"));
//    var searchTag = localStorage.getItem("tagSearchString");
//    if (searchTag || (tagList && tagList.length > 0)) {
//        $("#tagSettings").addClass("btn-primary");
//    } else {
//        $("#tagSettings").removeClass("btn-primary");
//    }

//    if (tagList === null || tagList.length === 0) {
       var tagList = readLastTagExec("", reportArea);
//    } else {
//        nbTagLoadedTarget = tagList.length;
//        refreshTagList(tagList, reportArea);
//    }

}


function hideLoaderTag() {
    if (nbTagLoaded >= nbTagLoadedTarget) {
        hideLoader($("#LastTagExecPanel"));
    }
}

function refreshTagList(tagList1, reportArea) {

    const tagScheduled = readNextTagScheduled();
    reportArea.empty();

    // ===== Init loader =====
    nbTagLoaded = 0;
    nbTagLoadedTarget = 0;


    const savedConfig = JSON.parse(localStorage.getItem("cerberus_homepage_lasttagexecutionconfig") || "{}");
    const displayNextCampaign = savedConfig.displayNextCampaign !== false; // default true

    // Compter tous les tags pour le loader
    tagList1.campaigns.forEach(c => {
        nbTagLoadedTarget += (tagList1.tagLists[c] || []).length;
    });

    // ===== 1) Next runs =====
    if (tagScheduled && tagScheduled.length > 0 && displayNextCampaign) {
        const topRow = $(`
            <div id="upcomingExecutions" class="mb-4">
                <div class="font-semibold mb-2">Next runs</div>
                <div id="upcomingExecutionsContainer" class="flex gap-2 overflow-x-auto py-1 no-scrollbar border-b border-gray-200"></div>
            </div>
        `);
        reportArea.append(topRow);
        const container = topRow.find('#upcomingExecutionsContainer');

        tagScheduled.forEach(scheduledHTML => {
            const pill = $(`<div class="px-3 py-1 text-blue-600 bg-blue-100/50 dark:text-blue-400 dark:bg-blue-900/50 text-xs font-light rounded-full flex-shrink-0">${scheduledHTML}</div>`);
            container.append(pill);
        });
    }

    // ===== 2) Lignes par campagne =====
    tagList1.campaigns.forEach((campaignName, i) => {
        const tagList = tagList1.tagLists[campaignName] || [];
        const headerText = (campaignName === "noCampaign") ? "---- no campaign defined ----" : campaignName;

        const row = $(`
            <div class="flex items-center gap-4 py-2 border-b border-gray-200">
                <div class="w-48 font-semibold truncate">${headerText}</div>
            </div>
        `);

        const tagContainer = $(`<div class="flex gap-2 overflow-x-auto py-1 no-scrollbar flex-1"></div>`);
        row.append(tagContainer);
        reportArea.append(row);

        tagList.slice().reverse().forEach((obj, index) => {
            const rawTagName = obj.tag;
            const status = obj.ciResult || "NA";
            const encodedTag = encodeURIComponent(rawTagName);
            const tagId = `tagExecStatusRow_${i}_${index}`;
            const cssClass = getStatusColor(status);

            const tooltipContent = `
    <div class="space-y-2">
        <div><strong>Tag :</strong> ${rawTagName}</div>
        <div><strong>Status :</strong> ${status}</div>
        <div><strong>Env :</strong> ${obj.reqEnvironmentList?.replace(/[\[\]"]/g, '') || '-'}</div>
        <div><strong>Country :</strong> ${obj.reqCountryList?.replace(/[\[\]"]/g, '') || '-'}</div>
        <div><strong>Créé le :</strong> ${obj.DateCreated}</div>

        <!-- Score -->
        <div>
            <div class="flex justify-between text-xs mb-1">
                <span>CI Score</span>
                <span>${obj.ciScore || 0} / ${obj.ciScoreMax || 100}</span>
            </div>
            <div class="w-full bg-gray-700 rounded h-2">
                <div class="bg-blue-500 h-2 rounded" style="width: ${100*obj.ciScore/obj.ciScoreMax || 0}%;"></div>
            </div>
        </div>

        <!-- Stats badges -->
        <div class="grid grid-cols-3 gap-1 pt-2 text-center text-xs">
            <div class="px-2 py-1 rounded bg-green-500/80 text-white">OK: ${obj.nbOK}</div>
            <div class="px-2 py-1 rounded bg-red-500/80 text-white">KO: ${obj.nbKO}</div>
            <div class="px-2 py-1 rounded bg-orange-500/80 text-white">FA: ${obj.nbFA}</div>
            <div class="px-2 py-1 rounded bg-blue-500/80 text-white">PE: ${obj.nbPE}</div>
            <div class="px-2 py-1 rounded bg-gray-500/80 text-white">NA: ${obj.nbNA}</div>
            <div class="px-2 py-1 rounded bg-purple-500/80 text-white">WE: ${obj.nbWE}</div>
        </div>
    </div>
`;

            const tagDiv = $(`
    <div id="${tagId}"
         class="tagDetail relative cursor-pointer px-3 py-1 rounded-full text-xs font-light flex-shrink-0 ${cssClass}"
         data-tag="${encodedTag}"
         data-tooltip-content='${tooltipContent}'
    >
        ${rawTagName}
    </div>
`);

            tagContainer.append(tagDiv);

// Tooltip indépendant
            tagDiv.on('mouseenter', function() {
                const el = this;
                if (el._tooltipEl) document.body.removeChild(el._tooltipEl);

                const tooltip = document.createElement('div');
                tooltip.className = 'absolute z-50 rounded-lg bg-gray-800 text-white text-xs p-3 shadow-lg max-w-xs';
                tooltip.innerHTML = el.dataset.tooltipContent;
                document.body.appendChild(tooltip);

                const rect = el.getBoundingClientRect();
                tooltip.style.top = (rect.top + window.scrollY - tooltip.offsetHeight - 8) + 'px';
                tooltip.style.left = (rect.left + rect.width/2 - tooltip.offsetWidth/2 + window.scrollX) + 'px';

                el._tooltipEl = tooltip;
            });

            tagDiv.on('mouseleave', function() {
                const el = this;
                if (el._tooltipEl) {
                    document.body.removeChild(el._tooltipEl);
                    el._tooltipEl = null;
                }
            });


            // Incrémenter nbTagLoaded
            nbTagLoaded++;
            hideLoaderTag();
        });

        // Scroll initial à droite
        Alpine.nextTick(() => {
            tagContainer.scrollLeft(tagContainer[0].scrollWidth);
        });
    });

    updateNextFireTime();
}

function readLastTagExec(searchString, reportArea) {
    var tagListResult = {};
    var tagAgregated = {};
    var campaignList = [];

    // Charger config depuis localStorage
    const savedConfig = JSON.parse(localStorage.getItem("cerberus_homepage_lasttagexecutionconfig") || "{}");

// Valeurs par défaut et validation
    const maxCampaign = (savedConfig.maxCampaign >= 0 && savedConfig.maxCampaign <= 20) ? savedConfig.maxCampaign : 5;
    const maxPerCampaign = (savedConfig.maxPerCampaign >= 0 && savedConfig.maxPerCampaign <= 20) ? savedConfig.maxPerCampaign : 5;

    let resultSetSize = parseInt(savedConfig.resultSetSize);
    if (!(resultSetSize >= 10 && resultSetSize <= 5000)) resultSetSize = 1000;

    const displayNoCampaign = savedConfig.displayNoCampaign !== false; // default true
    const displayNextCampaign = savedConfig.displayNextCampaign !== false; // default true

    const tagFilterList = savedConfig.tagFilterList || "";

    // === Construction de l’URL ===
    var myUrl = "ReadTag?iSortCol_0=0&sSortDir_0=desc&sColumns=id,tag,campaign,description" +
        "&iDisplayLength=" + resultSetSize +
        getUser().defaultSystemsQuery;

    if (!isEmpty(searchString)) {
        myUrl += "&sSearch=" + searchString;
    }

    if (tagFilterList.trim().length > 0) {
        myUrl += "&sSearch_2="+encodeURIComponent(tagFilterList);
    }

    // === Appel AJAX ===
    $.ajax({
        type: "GET",
        url: myUrl,
        async: true,
        dataType: 'json',
        success: function (data) {
            var campaignAdded = 0;

            for (var s = 0; s < data.contentTable.length; s++) {
                let row = data.contentTable[s];
                let campaignName = row.campaign && row.campaign.length > 0 ? row.campaign : "noCampaign";

                // Skip noCampaign si désactivé
                if (campaignName === "noCampaign" && !displayNoCampaign) {
                    continue;
                }

                if (!tagAgregated[campaignName]) {
                    if (campaignAdded < maxCampaign) {
                        tagAgregated[campaignName] = [row];
                        campaignList.push(campaignName);
                        campaignAdded++;
                    }
                } else {
                    if (tagAgregated[campaignName].length < maxPerCampaign) {
                        tagAgregated[campaignName].push(row);
                    }
                }
            }

            tagListResult.campaigns = campaignList;
            tagListResult.tagLists = tagAgregated;

            refreshTagList(tagListResult, reportArea);
        }
    });

    return tagListResult;
}

function readNextTagScheduled() {
    let tagList = [];

    var nbExe = getParameter("cerberus_homepage_nbdisplayedscheduledtag", getUser().defaultSystem, true);
    var paramExe = nbExe.value;

    if (!((paramExe >= 0) && (paramExe <= 20))) {
        paramExe = 5;
    }
    nbTagLoadedTargetScheduled = paramExe;

    var myUrl = "api/campaigns/scheduled";

    $.ajax({
        type: "GET",
        url: myUrl,
        async: false,
        dataType: 'json',
        success: function (data) {
            if (data.schedulerTriggers.length < nbTagLoadedTargetScheduled) {
                nbTagLoadedTargetScheduled = data.schedulerTriggers.length;
            }
            for (var s = 0; s < nbTagLoadedTargetScheduled; s++) {
                let item = data.schedulerTriggers[s];
                tagList.splice(0, 0, "<b>" + item.triggerName + "</b><span class='hidden-xs'> - [" + item.triggerUserCreated + "] - " + new Date(item.triggerNextFiretimeTimestamp).toLocaleString() + "</span> <b id='futurTag" + s + "'>will trigger in " + getHumanReadableDuration(Math.round(item.triggerNextFiretimeDurationToTriggerInMs / 1000)) + "</b>");
                futureCampaignRunTime.push(new Date());
                futureCampaignRunTimeDurationToTrigger.push(item.triggerNextFiretimeDurationToTriggerInMs);
            }
        }
    });
    return tagList;
}

function updateNextFireTime() {
    let nbAlreadyTriggered = 0;
    for (var s = 0; s < futureCampaignRunTime.length; s++) {
        if ((futureCampaignRunTimeDurationToTrigger[s] - (new Date() - new Date(futureCampaignRunTime[s]))) > 0) {
            $("#futurTag" + s).text("will trigger in " + getHumanReadableDuration(Math.round((futureCampaignRunTimeDurationToTrigger[s] - (new Date() - new Date(futureCampaignRunTime[s]))) / 1000)));
        } else {
            $("#futurTag" + s).text("already triggered");
            nbAlreadyTriggered++;
        }
    }
    if ((futureCampaignRunTime.length > 0) && (nbAlreadyTriggered < futureCampaignRunTime.length)) {
        // Refresh the scheduled tag execution every second.
        idTimeout = setTimeout(() => {
            updateNextFireTime();
        }, 1000);
    }
}

function getCountryFilter() {
    return $.ajax({
        url: "FindInvariantByID",
        data: {idName: "COUNTRY"},
        async: false,
        dataType: 'json',
    });
}


function aoColumnsFunc() {
    var doc = new Doc();
    var mDoc = getDoc();
    var status = readStatus();
    var statusLen = status.length;

    var aoColumns = [
        {
            "data": "Application",
            "bSortable": true,
            "sName": "Application",
            "title": doc.getDocOnline("application", "Application"),
            "sWidth": "50px",
            "mRender": function (data, type, oObj) {
                var href = "TestCaseList.jsp?application=" + data;

                return "<a href='" + href + "'>" + data + "</a>";
            }
        },
        {
            "data": "Total",
            "bSortable": true,
            "sWidth": "10px",
            "sClass": "datatable-alignright",
            "sName": "Total",
            "title": "Total"
        }
    ];

    for (var s = 0; s < statusLen; s++) {
        if (status[s].gp1 !== "N") {
            var obj = {
                "data": status[s].value,
                "bSortable": true,
                "sWidth": "10px",
                "sClass": "datatable-alignright",
                "sName": status[s].value,
                "title": status[s].value,
                "mRender": function (data, type, oObj) {
                    if ((data) === 0) {
                        return "";
                    }
                    ;
                    return data;
                }
            };
            aoColumns.push(obj);
        }
    }

    return aoColumns;
}

function toggleConfigPanel() {
    const panel = $("#tagConfigPanel");
    panel.toggleClass("hidden");

    if (!panel.hasClass("hidden")) {
        const config = JSON.parse(localStorage.getItem("cerberus_homepage_lasttagexecutionconfig") || "{}");

        $("#conf_maxCampaign").val(config.maxCampaign || 10); // défaut 10
        $("#conf_maxPerCampaign").val(config.maxPerCampaign || 5); // défaut 5
        $("#conf_resultSetSize").val(config.resultSetSize || 1000);
        $("#conf_displayNoCampaign").prop("checked", config.displayNoCampaign !== false);
        $("#conf_displayNextCampaign").prop("checked", config.displayNextCampaign !== false);
        $("#conf_tagFilterList").val(config.tagFilterList || "");
    }
}


function saveConfigPanel() {
    const localConfig = {
        maxCampaign: $("#conf_maxCampaign").val(),
        maxPerCampaign: $("#conf_maxPerCampaign").val(),
        resultSetSize: $("#conf_resultSetSize").val(),
        displayNoCampaign: $("#conf_displayNoCampaign").is(":checked"),
        displayNextCampaign: $("#conf_displayNextCampaign").is(":checked"),
        tagFilterList: $("#conf_tagFilterList").val()
    };

    localStorage.setItem("cerberus_homepage_lasttagexecutionconfig", JSON.stringify(localConfig));

    toastr.success("Configuration locale mise à jour.");

    $("#tagConfigPanel").addClass("hidden");
    loadLastTagResultList();
}


function loadBuildRevTable() {
    $('#envTableBody tr').remove();
    selectSystem = "VC";
    var jqxhr = $.getJSON("GetEnvironmentsPerBuildRevision", "q=1" + getUser().defaultSystemsQuery);
    $.when(jqxhr).then(function (result) {
        if (result["contentTable"].length > 0) {
            $.each(result["contentTable"], function (idx, obj) {
                appendBuildRevRow(obj);
            });

        } else {
            //$("#ReportByStatusPanel").hide();
        }
    }).fail(handleErrorAjaxAfterTimeout);
}

function counterFormated(system, nb, build, revision, envGP) {
    if (nb === 0) {
        return "";
    } else {
        return "<a href=\"Environment.jsp?" + "&system=" + system + "&build=" + build + "&revision=" + revision + "&envgp=" + envGP + "&active=Y\">" + nb + "</a>"
    }
}

function appendBuildRevRow(dtb) {
    var doc = new Doc();
    var table = $("#envTableBody");

    var toto = counterFormated(dtb.nbEnvDEV);

    var row = $("<tr></tr>");
    var systemCel = $("<td></td>").append(dtb.system);
    var buildCel = $("<td></td>").append(dtb.build);
    var revCel = $("<td></td>").append(dtb.revision);
    var nbdev = $("<td style=\"text-align: right;\"></td>").append(counterFormated(dtb.system, dtb.nbEnvDEV, dtb.build, dtb.revision, "DEV"));
    var nbqa = $("<td style=\"text-align: right;\"></td>").append(counterFormated(dtb.system, dtb.nbEnvQA, dtb.build, dtb.revision, "QA"));
    var nbuat = $("<td style=\"text-align: right;\"></td>").append(counterFormated(dtb.system, dtb.nbEnvUAT, dtb.build, dtb.revision, "UAT"));
    var nbprod = $("<td style=\"text-align: right;\"></td>").append(counterFormated(dtb.system, dtb.nbEnvPROD, dtb.build, dtb.revision, "PROD"));

    row.append(systemCel);
    row.append(buildCel);
    row.append(revCel);
    row.append(nbdev);
    row.append(nbqa);
    row.append(nbuat);
    row.append(nbprod);
    table.append(row);
}

function updateHeaderStats() {

    $("#hp_TestcaseNumber").text("...");
    $("#hp_TestExecutionNumber").text("...");
    $("#hp_ApplicationNumber").text("...");


    var jqxhr = $.getJSON("api/testcases/count", getUser().defaultSystemsQuery);
    $.when(jqxhr).then(function (result) {
        $("#hp_TestcaseNumber").text(result["iTotalRecords"]);
    }).fail(handleErrorAjaxAfterTimeout);

    var jqxhr = $.getJSON("api/executions/count", getUser().defaultSystemsQuery);
    $.when(jqxhr).then(function (result) {
        $("#hp_TestExecutionNumber").text(formatnumberKM(result["iTotalRecords"]));
    }).fail(handleErrorAjaxAfterTimeout);

    var jqxhr = $.getJSON("api/applications/count", getUser().defaultSystemsQuery);
    $.when(jqxhr).then(function (result) {
        $("#hp_ApplicationNumber").text(result["iTotalRecords"]);
    }).fail(handleErrorAjaxAfterTimeout);

    var jqxhr = $.getJSON("api/services/count", getUser().defaultSystemsQuery);
    $.when(jqxhr).then(function (result) {
        $("#hp_ServiceNumber").text(result["iTotalRecords"]);
    }).fail(handleErrorAjaxAfterTimeout);

}


function getStatusColor(status) {
    switch (status) {
        case "OK": return "text-emerald-600 bg-emerald-100/50 dark:text-emerald-400 dark:bg-emerald-900/50";
        case "KO": return "text-red-600 bg-red-100/50 dark:text-red-400 dark:bg-red-900/50";
        case "FA": return "bg-yellow-500";
        case "NA": return "text-gray-600 bg-gray-100/50 dark:text-gray-400 dark:bg-gray-900/50";
        case "NE": return "bg-blue-500";
        case "PE": return "bg-purple-500";
        default: return "text-blue-600 bg-blue-100/50 dark:text-blue-400 dark:bg-blue-900/50";
    }
}

