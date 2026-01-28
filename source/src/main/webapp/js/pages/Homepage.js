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

    $(document).on('mouseenter', '.execution-dot', function (e) {

        const content = $(this).data('tooltip');
        if (!content) return;

        const $tooltip = $(`
        <div class="execution-tooltip fixed z-[9999]
                    bg-gray-800 text-white border text-xs
                    p-3 rounded-lg shadow-lg w-64">
            ${content}
        </div>
    `);

        $('body').append($tooltip);

        const rect = this.getBoundingClientRect();

        $tooltip.css({
            top: rect.top - $tooltip.outerHeight() - 8,
            left: rect.left + rect.width / 2 - $tooltip.outerWidth() / 2
        });

        $(this).data('activeTooltip', $tooltip);
    });

    $(document).on('mouseleave', '.execution-dot', function () {
        const $tooltip = $(this).data('activeTooltip');
        if ($tooltip) {
            $tooltip.remove();
            $(this).removeData('activeTooltip');
        }
    });

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

    //showLoader($("#LastTagExecPanel"));

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

/**
 * Empty TagList and draw new TagList
 * @param tagList1
 * @param reportArea
 */
function refreshTagList(tagList1, reportArea) {
    reportArea.empty();
    const nextRuns = readNextTagScheduled();
    renderCampaignGrid(reportArea, tagList1, nextRuns);
    updateNextFireTime();
    if (window.lucide) lucide.createIcons();
}

/**
 * Create grid and iterate on tag List to create cards
 * @param container
 * @param tagList1
 * @param nextRuns
 */
function renderCampaignGrid(container, tagList1, nextRuns) {
    const grid = $(`<div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3  gap-6"></div>`);
    tagList1.campaigns.forEach(name => {
        const tags = tagList1.tagLists[name] || [];
        const nextRun = nextRuns.find(n =>
            tags.some(t => t.campaign === n.tag)
        );
        grid.append(renderCampaignCard(name,tags,nextRun ? `${nextRun.nextRunLabel}` : null));
    });
    container.append(grid);
}

/**
 *
 * @param campaignName
 * @param tagExecutions
 * @param nextRun
 * @returns {*|jQuery|HTMLElement}
 */
function renderCampaignCard(campaignName, tagExecutions, nextRun) {
    const stats = computeCampaignStats(tagExecutions);
    return $(`<div class="crb_card_tag">
                <div class="flex justify-between items-start mb-8 gap-4">
                    <div class="flex flex-col gap-1 min-w-0 min-h-[1.5rem]">
                        <!-- Ligne 1 : campagne -->
                        <div class="flex items-center gap-2 min-w-0">
                            <i data-lucide="tag"
                               class="w-4 h-4 text-sky-500 flex-shrink-0"></i>
                
                            <span class="text-sm font-semibold truncate">
                                ${campaignName === "noCampaign" ? "--- No campaign defined ---" : campaignName}
                            </span>
                        </div>
                        <!-- Ligne 2 : next run -->
                        ${nextRun ? `<div class="pl-6 text-xs text-gray-500 truncate"> ${renderNextRunBadge(nextRun)}</div>` : ""}
                    </div>
                
                    <!-- Droite : badge succès -->
                    <div class="flex-shrink-0">
                        ${renderSuccessBadge(stats.successRate)}
                    </div>
                </div>
                <!-- Content row -->
                <div class="flex flex-col md:flex-row md:items-center gap-4 overflow-x-auto no-scrollbar">
                    <!-- Trend graph -->
                    <div class="w-full md:w-24 flex-shrink-0">
                        <div class="relative w-full h-6">
                            ${renderTrendGraph(stats.responseTime, stats.status)}
                        </div>
                    </div>
                    <!-- Progress -->
                    <div class="flex-1 mt-2 md:mt-0">
                        <!-- Progress bar -->
                        <div class="h-2 bg-gray-200 dark:bg-gray-700 rounded">
                            <div class="h-2 rounded bg-green-500" style="width:${stats.successRate}%"></div>
                        </div>
                    
                        <!-- Texte en dessous -->
                        <div class="flex justify-between text-xs mt-1 text-gray-500">
                            <span>${stats.executions} exec.</span>
                            <div class="flex items-center gap-2">
                                <div class="flex items-center gap-1 text-green-500">
                                    <i data-lucide="circle-check" class="w-3 h-3"></i>
                                    <span>${stats.ok}</span>
                                </div>
                                <div class="flex items-center gap-1 text-red-500">
                                    <i data-lucide="circle-x" class="w-3 h-3"></i>
                                    <span>${stats.ko}</span>
                                </div>
                            </div>
                        </div>
                    </div>
                    <!-- Status dots -->
                    <div class="flex flex-wrap gap-1 justify-start md:justify-end md:flex-nowrap md:flex-shrink-0">
                        ${renderExecutionDots(stats.lastResults, tagExecutions)}
                    </div>
                </div>
            </div>`);
}

/**
 * Render next run Label
 * @param label
 * @returns {string}
 */
function renderNextRunBadge(label) {
    return `<span class="px-2 py-0.5 rounded-full text-xs bg-blue-100 text-blue-700
                     dark:bg-blue-900/40 dark:text-blue-400 whitespace-nowrap">▶ ${label}</span>`;
}

function computeCampaignStats(tags) {
    const executions = tags.length;

    const ok = tags.filter(t => t.ciResult === "OK").length;
    const ko = executions - ok;

    return {
        executions,
        successRate: executions ? Math.round(ok * 100 / executions) : 0,
        lastResults: tags.slice(-5).map(t => t.ciResult),
        history: tags.map(t => t.ciScore || 0),
        responseTime: tags.map(t =>
            getResponseTime(t.DateStartExe, t.DateEndQueue)
        ),
        status: tags.map(t => t.ciResult),
        ok,
        ko
    };
}

function getResponseTime(startStr, endStr) {
    if (!startStr || !endStr) return null;

    const start = new Date(startStr.replace(" ", "T"));
    const end = new Date(endStr.replace(" ", "T"));

    return Math.round((end - start) / 1000); // en secondes
}

/**
 * Render Each Execution cards with associated Tooltip
 * @param results
 * @param obj
 * @returns {*}
 */
function renderExecutionDots(results, obj = []) {

    return results
        .slice(0, 5)
        .map((status, i) => ({
            status,
            exec: obj[i] || {}
        }))
        .reverse()
        .map(({ status, exec }) => {

            const tooltipContent = `
                <div class="space-y-2 text-xs">
                    <div><strong>Tag :</strong> ${exec.tag || '-'}</div>
                    <div><strong>Status :</strong> ${status}</div>
                    <div><strong>Env :</strong> ${exec.reqEnvironmentList?.replace(/[\[\]"]/g, '') || '-'}</div>
                    <div><strong>Country :</strong> ${exec.reqCountryList?.replace(/[\[\]"]/g, '') || '-'}</div>
                    <div><strong>Créé le :</strong> ${exec.DateCreated || '-'}</div>
                    <div>
                        <div class="flex justify-between mb-1">
                            <span>CI Score</span>
                            <span>${exec.ciScore || 0} / ${exec.ciScoreMax || 100}</span>
                        </div>
                        <div class="w-full bg-gray-600 rounded h-1.5">
                            <div class="bg-blue-500 h-1.5 rounded"
                                 style="width:${exec.ciScoreMax ? (100 * exec.ciScore / exec.ciScoreMax) : 0}%;">
                            </div>
                        </div>
                    </div>
                    <div class="grid grid-cols-3 gap-1 pt-2 text-center">
                        <div class="px-2 py-1 rounded bg-green-500/80 text-white">OK: ${exec.nbOK || 0}</div>
                        <div class="px-2 py-1 rounded bg-red-500/80 text-white">KO: ${exec.nbKO || 0}</div>
                        <div class="px-2 py-1 rounded bg-orange-500/80 text-white">FA: ${exec.nbFA || 0}</div>
                        <div class="px-2 py-1 rounded bg-blue-500/80 text-white">PE: ${exec.nbPE || 0}</div>
                        <div class="px-2 py-1 rounded bg-gray-500/80 text-white">NA: ${exec.nbNA || 0}</div>
                        <div class="px-2 py-1 rounded bg-purple-500/80 text-white">WE: ${exec.nbWE || 0}</div>
                    </div>
                </div>
            `;

            const encodedTag = encodeURIComponent(exec.tag || "");
            return `
                <span class="execution-dot w-7 h-7 md:w-8 md:h-8 flex items-center justify-center rounded-xl cursor-pointer
                    ${exec.nbPE > 0
                ? "bg-blue-100 text-blue-600 dark:bg-blue-900/40 dark:text-blue-400"
                : status === "OK"
                    ? "bg-green-100 text-green-600 dark:bg-green-900/40 dark:text-green-400"
                    : "bg-red-100 text-red-600 dark:bg-red-900/40 dark:text-red-400"}"
                    data-tooltip="${tooltipContent.replace(/"/g, '&quot;')}"
                    onclick="window.location.href='./ReportingExecutionByTag.jsp?Tag=${encodedTag}'">
                    ${exec.nbPE > 0 ? "⧗" : status === "OK" ? "✓" : "✕"}
                </span>
            `;
        })
        .join("");
}

/**
 * Render SuccessBadge
 * @param rate
 * @returns {string}
 */
function renderSuccessBadge(rate) {

    if (rate >= 90) {
        return `
            <span class="px-2 py-0.5 rounded-full text-xs
                         bg-green-100 text-green-700
                         dark:bg-green-900/40 dark:text-green-400">
                ↑ ${rate}%
            </span>
        `;
    }

    if (rate >= 75) {
        return `
            <span class="px-2 py-0.5 rounded-full text-xs
                         bg-yellow-100 text-yellow-700
                         dark:bg-yellow-900/40 dark:text-yellow-400">
                ↑ ${rate}%
            </span>
        `;
    }

    if (rate >= 60) {
        return `
            <span class="px-2 py-0.5 rounded-full text-xs
                         bg-orange-100 text-orange-700
                         dark:bg-orange-900/40 dark:text-orange-400">
                ↗ ${rate}%
            </span>
        `;
    }

    return `
        <span class="px-2 py-0.5 rounded-full text-xs
                     bg-red-100 text-red-700
                     dark:bg-red-900/40 dark:text-red-400">
            ↘ ${rate}%
        </span>
    `;
}

function renderTrendGraph(responseTime = [], status = []) {
    if (!responseTime.length) {
        return `<div class="h-6 flex items-center text-xs text-gray-400">No data</div>`;
    }

    const HEIGHT = 24;
    const padding = 3;

    const max = Math.max(...responseTime);
    const min = Math.min(...responseTime);
    const range = max - min || 1;

    const getX = i => (i / (responseTime.length - 1 || 1)) * 100;
    const getY = v => padding + (1 - (v - min) / range) * (HEIGHT - padding * 2);

    // Création des points
    const points = responseTime.map((v, i) => ({
        x: getX(i),
        y: getY(v),
        value: v,
        status: status[i]
    }));

    // Inverser l'ordre pour affichage de droite à gauche
    const pointsReversed = points.slice().reverse().map((p, i) => ({
        ...p,
        x: getX(i) // recalcule x pour l'affichage inversé
    }));

    const linePoints = pointsReversed.map(p => `${p.x},${p.y}`).join(" ");
    const fillPoints = `0,${HEIGHT} ${linePoints} 100,${HEIGHT}`;

    return `
        <div class="relative w-full h-6">
            <svg viewBox="0 0 100 ${HEIGHT}" class="w-full h-6" preserveAspectRatio="xMidYMid meet">
       
                <!-- Fill -->
                <polygon points="${fillPoints}" fill="#cbd5f5" opacity="0.35"/>
        
                <!-- Line -->
                <polyline points="${linePoints}" fill="none" stroke="#94a3b8" stroke-width="2"
                          stroke-linecap="round" stroke-linejoin="round"/>
        
                <!-- Points -->
                ${pointsReversed.map(p => `
                    <circle cx="${p.x}" cy="${p.y}" r="2.8"
                            fill="${p.status === "OK" ? "#22c55e" : "#ef4444"}"
                            stroke="white" stroke-width="0.8"/>
                `).join("")}
        
                <!-- Hover zones -->
                ${pointsReversed.map(p => `
                    <rect x="${p.x - 4}" y="0" width="8" height="${HEIGHT}" fill="transparent"
                          data-value="${p.value}" data-status="${p.status}"
                          onmousemove="
                                const tooltip = this.closest('.relative').querySelector('#tooltip');
                                tooltip.style.display = 'block';
                                tooltip.innerHTML = this.dataset.value + ' sec. (' + this.dataset.status + ')';
                                const rect = this.getBoundingClientRect();
                                const parentRect = this.closest('.relative').getBoundingClientRect();
                                tooltip.style.left = (rect.x - parentRect.x + rect.width/2) + 'px';
                                tooltip.style.top = (rect.y - parentRect.y - 28) + 'px';
                            "
                          onmouseleave="
                                const tooltip = this.closest('.relative').querySelector('#tooltip');
                                tooltip.style.display = 'none';
                            "
                    ></rect>
                `).join("")}
        
            </svg>
        
            <!-- Tooltip HTML flottant -->
            <div id="tooltip"
                 class="absolute bg-gray-800 text-white text-xs px-2 py-1 rounded pointer-events-none whitespace-nowrap z-50" style="display:none">
            </div>
        </div>
        `;
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

    const result = [];

    let nbExe = getParameter(
        "cerberus_homepage_nbdisplayedscheduledtag",
        getUser().defaultSystem,
        true
    );

    let limit = nbExe?.value;
    if (!(limit >= 0 && limit <= 50)) {
        limit = 5;
    }

    nbTagLoadedTargetScheduled = limit;

    $.ajax({
        type: "GET",
        url: "api/campaigns/scheduled",
        async: false,
        dataType: "json",
        success: function (data) {

            const triggers = data.schedulerTriggers || [];
            const max = Math.min(limit, triggers.length);

            for (let i = 0; i < max; i++) {
                const t = triggers[i];

                result.push({
                    tag: t.triggerName,
                    nextRunLabel:
                        "will trigger in " +
                        getHumanReadableDuration(
                            Math.round(t.triggerNextFiretimeDurationToTriggerInMs / 1000)
                        ),
                    nextFireDate: new Date(t.triggerNextFiretimeTimestamp),
                    durationMs: t.triggerNextFiretimeDurationToTriggerInMs,
                    user: t.triggerUserCreated
                });

                futureCampaignRunTime.push(new Date());
                futureCampaignRunTimeDurationToTrigger.push(
                    t.triggerNextFiretimeDurationToTriggerInMs
                );
            }
        }
    });

    return result;
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

    notifyInPage("success","Configuration locale mise à jour.");

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

/**
 * map API result with expected data for the card
 */
function mapCampaigns(api) {
    const global = api.global || {};
    const globalPrev = api.globalPreviousMonth || {};

    const system = api.system || {};
    const systemPrev = api.systemPreviousMonth || {};

    return {
        workspaces: {
            value: system.totalCampaigns || 0,
            tab: "Selected",
            label: "Campaigns (Workspaces)",
            currentValue: system.totalCampaigns || 0,
            previousValue: systemPrev.totalCampaigns || 0,
            diff: (system.totalCampaigns || 0) - (systemPrev.totalCampaigns || 0),
            diffPositive: true
        },
        launched: {
            value: system.totalCampaignsLaunched || 0,
            tab: "Execution",
            label: "Campaigns launched",
            currentValue: system.totalCampaignsLaunched || 0,
            previousValue: systemPrev.totalCampaignsLaunched || 0,
            diff: (system.totalCampaignsLaunched || 0) - (systemPrev.totalCampaignsLaunched || 0),
            diffPositive: true
        },
        total: {
            value: global.totalCampaigns || 0,
            tab: "Total",
            label: "Campagnes (Total)",
            currentValue: global.totalCampaigns || 0,
            previousValue: globalPrev.totalCampaigns || 0,
            diff: (global.totalCampaigns || 0) - (globalPrev.totalCampaigns || 0),
            diffPositive: true
        }
    };
}

/**
 * map API result with expected data for the card
 */
function mapTestCases(api) {
    const global = api.global || {};
    const globalPrev = api.globalPreviousMonth || {};

    const system = api.system || {};
    const systemPrev = api.systemPreviousMonth || {};

    return {
        selected: {
            value: system.totalCount || 0,
            tab: "Selected",
            label: "Testcases (Workspaces)",
            currentValue: system.totalCount || 0,
            previousValue: (system.totalCount || 0) - (systemPrev.totalCount || 0),
            diff: (system.totalCount || 0) - (systemPrev.totalCount || 0),
            diffPositive: true
        },
        working: {
            value: system.workingCount || 0,
            tab: "Working",
            label: "Testcases (Working)"
        },
        total: {
            value: global.totalCount || 0,
            tab: "Total",
            label: "Testcases (Total)",
            currentValue: global.totalCount || 0,
            previousValue: (global.totalCount || 0) - (globalPrev.totalCount || 0),
            diff: (global.totalCount || 0) - (globalPrev.totalCount || 0),
            diffPositive: true
        }
    };
}


function mapExecutions(api) {
    const global = api.globalLastMonth || {};
    const globalPrev = api.globalPreviousMonth || {};

    const system = api.systemLastMonth || {};
    const systemPrev = api.systemPreviousMonth || {};

    return {
        selected: {
            value: system.totalExecutions || 0,
            tab: "Selected",
            label: "Executions last month (Workspaces)",
            currentValue: system.totalExecutions || 0,
            previousValue: (system.totalExecutions || 0) - (systemPrev.totalExecutions || 0),
            diff: (system.totalExecutions || 0) - (systemPrev.totalExecutions || 0),
            diffPositive: true
        },

        total: {
            value: global.totalExecutions || 0,
            tab: "Total",
            label: "Executions last month (Total)",
            currentValue: global.totalExecutions || 0,
            previousValue: (global.totalExecutions || 0) - (globalPrev.totalExecutions || 0),
            diff: (global.totalExecutions || 0) - (globalPrev.totalExecutions || 0),
            diffPositive: true
        }
    };
}

function mapAIUsage(api) {
    const current = api.currentPeriod || {};
    const previous = api.previousPeriod || {};
    const currentUser = api.userCurrentPeriod || {};
    const previousUser = api.previousPreviousPeriod || {};

    return {
        totalRequests: {
            value: current.totalSessions || 0,
            tab: "Requests",
            label: "requêtes IA (total)",
            currentValue: current.totalSessions || 0,
            previousValue: (current.totalSessions || 0) - (previous.totalSessions || 0),
            diff: (current.totalSessions || 0) - (previous.totalSessions || 0),
            diffPositive: true // hausse = bon
        },
        totalTokens: {
            value: formatTokens((current.totalInputTokens || 0) + (current.totalOutputTokens || 0)),
            tab: "Tokens",
            label: "tokens consommés",
            currentValue: (current.totalInputTokens || 0) + (current.totalOutputTokens || 0),
            previousValue: ((current.totalInputTokens || 0) + (current.totalOutputTokens || 0)) -
                ((previous.totalInputTokens || 0) + (previous.totalOutputTokens || 0)),
            diff: ((current.totalInputTokens || 0) + (current.totalOutputTokens || 0)) -
                ((previous.totalInputTokens || 0) + (previous.totalOutputTokens || 0)),
            diffPositive: true
        },
        totalCost: {
            value: formatEuro(current.totalCost || 0),
            tab: "Cost",
            label: "coût total (global)",
            currentValue: current.totalCost || 0,
            previousValue: (current.totalCost || 0) - (previous.totalCost || 0),
            diff: (current.totalCost || 0) - (previous.totalCost || 0),
            diffPositive: false
        },
        userCost: {
            value: formatEuro(currentUser.totalCost || 0),
            tab: "User",
            label: "coût utilisateur",
            currentValue: currentUser.totalCost || 0,
            previousValue: (currentUser.totalCost || 0) - (previousUser.totalCost || 0),
            diff: (currentUser.totalCost || 0) - (previousUser.totalCost || 0),
            diffPositive: false
        }
    };
}

function mapApplication(api) {
    const store = Alpine.store('labels');
    console.log(store);
    const global = api.global || {};
    const globalPrev = api.globalPreviousMonth || {};

    const system = api.system || {};
    const systemPrev = api.systemPreviousMonth || {};

    return {
        workspaces: {
            value: system.totalApplications || 0,
            tab: store.getLabel('homepage','applicationtabselected'),
            label: store.getLabel('homepage','applicationtabselectedlabel'),
            currentValue: system.totalApplications || 0,
            previousValue: (system.totalApplications || 0) - (systemPrev.totalApplications || 0),
            diff: (system.totalApplications || 0) - (systemPrev.totalApplications || 0),
            diffPositive: true
        },
        byTypeSystem: {
            tab: "Per Type",
            label: "Applications par Type",
            value: (() => {
                const map = api.system?.totalApplicationsByType;
                if (!map || typeof map !== "object" || Object.keys(map).length === 0) {
                    return "Aucun type";
                }
                return Object.entries(map)
                    .filter(([type, count]) => type && count != null)
                    .map(([type, count]) => type+':'+count)
                    .join(", ");
            })()
        },
        total: {
            value: global.totalApplications || 0,
            tab: "Total",
            label: "Applications (Total)",
            currentValue: global.totalApplications || 0,
            previousValue: (global.totalApplications || 0) - (globalPrev.totalApplications || 0),
            diff: (global.totalApplications || 0) - (globalPrev.totalApplications || 0),
            diffPositive: true
        },


    };
}

