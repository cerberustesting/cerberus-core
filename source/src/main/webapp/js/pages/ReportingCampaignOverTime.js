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
/* global handleErrorAjaxAfterTimeout */
// ChartJS Config Graphs
var configTagDur = {};
var configTagSco = {};
var configTagExe = {};
var configTagBar = {};
// Counters of different countries, env and robotdecli (used to shorten the labels)
var nbCountries = 0;
var nbEnv = 0;
var nbRobot = 0;

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

        moment.locale("fr");

        $('#frompicker').datetimepicker();
        $('#topicker').datetimepicker({
            useCurrent: false //Important! See issue #1075
        });

        $("#frompicker").on("dp.change", function (e) {
            $('#topicker').data("DateTimePicker").minDate(e.date);
        });
        $("#topicker").on("dp.change", function (e) {
            $('#frompicker').data("DateTimePicker").maxDate(e.date);
        });


        var campaigns = GetURLParameters("campaigns");
        var from = GetURLParameter("from");
        var to = GetURLParameter("to");
        var environments = GetURLParameters("environments");
        var countries = GetURLParameters("countries");
        var robotDeclis = GetURLParameters("robotDeclis");
        var gp1s = GetURLParameters("group1s");
        var gp2s = GetURLParameters("group2s");
        var gp3s = GetURLParameters("group3s");

        let fromD;
        let toD;
        if (from === null) {
            fromD = new Date();
            fromD.setMonth(fromD.getMonth() - 1);
        } else {
            fromD = new Date(from);
        }
        if (to === null) {
            toD = new Date();
        } else {
            toD = new Date(to);
        }
        $('#frompicker').data("DateTimePicker").date(moment(fromD));
        $('#topicker').data("DateTimePicker").date(moment(toD));

        $("#campaignSelect").empty();
        $("#campaignSelect").select2({width: "100%"});
        feedPerfCampaign("#campaignSelect", campaigns, countries, environments, robotDeclis, gp1s, gp2s, gp3s);

    });
});

function multiSelectConfPerf(name) {
    this.maxHeight = 450;
    this.checkboxName = name;
    this.buttonWidth = "100%";
    this.enableFiltering = true;
    this.enableCaseInsensitiveFiltering = true;
    this.includeSelectAllOption = true;
    this.includeSelectAllIfMoreThan = 4;
    this.numberDisplayed = 10;
}


/***
 * Feed the TestCase select with all the testcase from test defined.
 * @param {String} selectElement - id of select to refresh.
 * @param {String} defaultCampaigns - id of testcase to select.
 * @returns {null}
 */
function feedPerfCampaign(selectElement, defaultCampaigns, countries, environments, robotDeclis, gp1s, gp2s, gp3s) {
    showLoader($("#otFilterPanel"));

    var campaignList = $(selectElement);
    campaignList.empty();

    var jqxhr = $.getJSON("ReadCampaign");
    $.when(jqxhr).then(function (data) {
        for (var index = 0; index < data.contentTable.length; index++) {
            campaignList.append($('<option></option>').text(data.contentTable[index].campaign + " - " + data.contentTable[index].description).val(data.contentTable[index].campaign));
        }
        $('#campaignSelect').val(defaultCampaigns);
        $('#campaignSelect').trigger('change');

        feedCampaignGp("#gp1Select", data.distinct.group1);
        feedCampaignGp("#gp2Select", data.distinct.group2);
        feedCampaignGp("#gp3Select", data.distinct.group3);
        loadPerfGraph(false, countries, environments, robotDeclis, gp1s, gp2s, gp3s)
        hideLoader($("#otFilterPanel"));

    });
}

function feedCampaignGp(selectId, data) {
    var select = $(selectId);
    select.multiselect('destroy');
    var array = data;
    $(selectId + " option").remove();
    for (var i = 0; i < array.length; i++) {
        let n = array[i];
        if (isEmpty(n)) {
            n = "[Empty]";
        }
        $(selectId).append($('<option></option>').text(n).val(array[i]));
    }
//    for (var i = 0; i < array.length; i++) {
//        if (array[i].isRequested) {
//            $(selectId + " option[value='" + array[i] + "']").attr("selected", "selected");
//        }
//    }
    select.multiselect(new multiSelectConfPerf(selectId));

}

/*
 * Loading functions
 */

function initPage() {
    var doc = new Doc();
    displayHeaderLabel(doc);
    displayPageLabel(doc);
    displayFooter(doc);
    initGraph();
}

function displayPageLabel(doc) {
    $("#pageTitle").html(doc.getDocLabel("page_reportovertime", "title"));
    $("#title").html(doc.getDocOnline("page_reportovertime", "title"));
    $("#loadbutton").html(doc.getDocLabel("page_global", "buttonLoad"));
    $("#filters").html(doc.getDocOnline("page_global", "filters"));
    $("#lblPerfRequests").html(doc.getDocLabel("page_reportovertime", "lblPerfRequests"));
    $("#lblPerfSize").html(doc.getDocLabel("page_reportovertime", "lblPerfSize"));
    $("#lblPerfTime").html(doc.getDocLabel("page_reportovertime", "lblPerfTime"));
    $("#lblTestStat").html(doc.getDocLabel("page_reportovertime", "lblTestStat"));
    $("#lblTestStatBar").html(doc.getDocLabel("page_reportovertime", "lblTestStatBar"));
}

function loadPerfGraph(saveURLtoHistory, countries, environments, robotDeclis, gp1s, gp2s, gp3s) {
    showLoader($("#otFilterPanel"));

    if (countries === null || countries === undefined) {
        countries = [];
    }
    if (environments === null || environments === undefined) {
        environments = [];
    }
    if (robotDeclis === null || robotDeclis === undefined) {
        robotDeclis = [];
    }

    let from = new Date($('#frompicker').data("DateTimePicker").date());

    let to = new Date($('#topicker').data("DateTimePicker").date());

    if ($("#countrySelect").val() !== null) {
        countries = $("#countrySelect").val();
    }
    let len = countries.length;
    var countriesQ = "";
    for (var i = 0; i < len; i++) {
        countriesQ += "&countries=" + encodeURI(countries[i]);
    }

    if ($("#envSelect").val() !== null) {
        environments = $("#envSelect").val();
    }
    len = environments.length;
    var environmentsQ = "";
    for (var i = 0; i < len; i++) {
        environmentsQ += "&environments=" + encodeURI(environments[i]);
    }

    if ($("#robotSelect").val() !== null) {
        robotDeclis = $("#robotSelect").val();
    }
    len = robotDeclis.length;
    var robotDeclisQ = "";
    for (var i = 0; i < len; i++) {
        robotDeclisQ += "&robotDeclis=" + encodeURI(robotDeclis[i]);
    }

    var campaignString = "";
    if ($("#campaignSelect").val() !== null) {
        for (var i = 0; i < $("#campaignSelect").val().length; i++) {
            var campaignString = campaignString + "&campaigns=" + encodeURI($("#campaignSelect").val()[i]);
        }
    }

    if ($("#gp1Select").val() !== null) {
        gp1s = $("#gp1Select").val();
    }
    var gp1sQ = "";
    if (gp1s !== undefined) {
        len = gp1s.length;
        for (var i = 0; i < len; i++) {
            gp1sQ += "&group1s=" + encodeURI(gp1s[i]);
        }
    }

    if ($("#gp2Select").val() !== null) {
        gp2s = $("#gp2Select").val();
    }
    var gp2sQ = "";
    if (gp2s !== undefined) {
        len = gp2s.length;
        for (var i = 0; i < len; i++) {
            gp2sQ += "&group2s=" + encodeURI(gp2s[i]);
        }
    }

    if ($("#gp3Select").val() !== null) {
        gp3s = $("#gp3Select").val();
    }
    var gp3sQ = "";
    if (gp3s !== undefined) {
        len = gp3s.length;
        for (var i = 0; i < len; i++) {
            gp3sQ += "&group3s=" + encodeURI(gp3s[i]);
        }
    }

    let qS = "from=" + from.toISOString() + "&to=" + to.toISOString() + campaignString + countriesQ + environmentsQ + robotDeclisQ + gp1sQ + gp2sQ + gp3sQ;
    if (saveURLtoHistory) {
        InsertURLInHistory("./ReportingCampaignOverTime.jsp?" + qS);
    }

    $.ajax({
        url: "ReadTagStat?" + qS,
        method: "GET",
        async: true,
        dataType: 'json',
        success: function (data) {
            if (data.messageType === "OK") {
                updateNbDistinct(data.distinct);
                loadCombos(data);
                buildTagGraphs(data);
                buildTagBarGraphs(data);
            }
            hideLoader($("#otFilterPanel"));
        }
    });
}

function updateNbDistinct(data) {

    nbCountries = 0;
    for (var i = 0; i < data.countries.length; i++) {
        if (data.countries[i].isRequested) {
            nbCountries++;
        }
    }
    nbEnv = 0;
    for (var i = 0; i < data.environments.length; i++) {
        if (data.environments[i].isRequested) {
            nbEnv++;
        }
    }
    nbRobot = 0;
    for (var i = 0; i < data.robotDeclis.length; i++) {
        if (data.robotDeclis[i].isRequested) {
            nbRobot++;
        }
    }
}

function setTimeRange(id) {
    let fromD;
    let toD = new Date();
    toD.setHours(23);
    toD.setMinutes(59);
    fromD = new Date();
    fromD.setHours(23);
    fromD.setMinutes(59);
    if (id === 1) { // 1 month
        fromD.setMonth(fromD.getMonth() - 1);
    } else if (id === 2) { // 3 months
        fromD.setMonth(fromD.getMonth() - 3);
    } else if (id === 3) { // 6 months
        fromD.setMonth(fromD.getMonth() - 6);
    } else if (id === 4) { //
        fromD.setMonth(fromD.getMonth() - 12);
    } else if (id === 5) {
        fromD.setHours(fromD.getHours() - 168);
    } else if (id === 6) {
        fromD.setHours(fromD.getHours() - 24);
    }
    $('#frompicker').data("DateTimePicker").date(moment(fromD));
    $('#topicker').data("DateTimePicker").date(moment(toD));
}

function loadCombos(data) {

    var select = $("#countrySelect");
    select.multiselect('destroy');
    var array = data.distinct.countries;
    $("#countrySelect option").remove();
    for (var i = 0; i < array.length; i++) {
        let n = array[i].name;
        if (isEmpty(n)) {
            n = "[Empty]";
        }
        $("#countrySelect").append($('<option></option>').text(n).val(array[i].name));
    }
    for (var i = 0; i < array.length; i++) {
        if (array[i].isRequested) {
            $("#countrySelect option[value='" + array[i].name + "']").attr("selected", "selected");
        }
    }
    select.multiselect(new multiSelectConfPerf("countrySelect"));

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

    var select = $("#robotSelect");
    select.multiselect('destroy');
    var array = data.distinct.robotDeclis;
    $("#robotSelect option").remove();
    for (var i = 0; i < array.length; i++) {
        let n = array[i].name;
        if (isEmpty(n)) {
            n = "[Empty]";
        }
        $("#robotSelect").append($('<option></option>').text(n).val(array[i].name));
    }
    for (var i = 0; i < array.length; i++) {
        if (array[i].isRequested) {
            $("#robotSelect option[value='" + array[i].name + "']").attr("selected", "selected");
        }
    }
    select.multiselect(new multiSelectConfPerf("robotSelect"));

}

function getOptions(title, unit, axisType) {
    let option = {
        responsive: true,
        maintainAspectRatio: false,
        hover: {
            mode: 'nearest',
            intersect: true
        },
        tooltips: {
            callbacks: {
                label: function (t, d) {
                    var xLabel = d.datasets[t.datasetIndex].label;
                    if (unit === "size") {
                        return xLabel + ': ' + formatNumber(Math.round(t.yLabel / 1024)) + " kb";
                    } else if (unit === "time") {
                        return xLabel + ': ' + t.yLabel.toString().replace(/(\d)(?=(\d{3})+(?!\d))/g, "$1 ") + " min";
                    } else {
                        return xLabel + ': ' + t.yLabel;
                    }
                }
            },
        },
        title: {
            text: title
        },
        scales: {
            xAxes: [{
                    type: 'time',
                    time: {
                        tooltipFormat: 'll HH:mm'
                    },
                    scaleLabel: {
                        display: true,
                        labelString: 'Date'
                    }
                }],
            yAxes: [{
                    scaleLabel: {
                        display: true,
                        labelString: title
                    },
                    ticks: {
                        callback: function (value, index, values) {
                            if (unit === "size") {
                                return formatNumber(Math.round(value / 1024));
                            } else if (unit === "time") {
                                return value.toString().replace(/(\d)(?=(\d{3})+(?!\d))/g, "$1 ");
                            } else {
                                return value;
                            }
                        }},
                    type: axisType

                }]
        }
    };
    return option;
}

function getOptionsBar(title, unit) {
    let option = {
        responsive: true,
        maintainAspectRatio: false,
        title: {
            text: title
        },
        scales: {
            xAxes: [{
                    offset: true,
//                    type: 'time',
                    stacked: true,
//                    time: {
//                        tooltipFormat: 'll',
//                        unit: 'day',
//                        round: 'day',
//                        displayFormats: {
//                            day: 'MMM D'
//                        }},
                    scaleLabel: {
                        display: true,
                        labelString: 'Tag'
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

function formatNumber(num) {
    return num.toString().replace(/(\d)(?=(\d{3})+(?!\d))/g, "$1,")
}

function buildTagGraphs(data) {

    let curves = data.curvesTime;

    // Sorting values by nb of requests.
    sortedCurves = curves.sort(function (a, b) {
        let a1 = "-" + a.key;
        let b1 = "-" + b.key;
        return b1.localeCompare(a1);
    });

    var len = sortedCurves.length;

    let timedatasets = [];
    let cidatasets = [];
    let exedatasets = [];

    for (var i = 0; i < len; i++) {

        let c = sortedCurves[i];
        let d1 = [];
        let d2a = [];
        let d2b = [];
        let d3a = [];
        let d3b = [];
        lend = c.points.length;
        for (var j = 0; j < lend; j++) {
            let p = {x: c.points[j].x, y: c.points[j].y, tag: c.points[j].tag, ciResult: c.points[j].ciRes};
            d1.push(p);
        }
        for (var j = 0; j < lend; j++) {
            let p = {x: c.points[j].x, y: c.points[j].ciSc, tag: c.points[j].tag, ciResult: c.points[j].ciRes};
            d2a.push(p);
        }
        for (var j = 0; j < lend; j++) {
            let p = {x: c.points[j].x, y: c.points[j].ciScT, tag: c.points[j].tag};
            d2b.push(p);
        }
        for (var j = 0; j < lend; j++) {
            let p = {x: c.points[j].x, y: c.points[j].nbExeU, tag: c.points[j].tag, ciResult: c.points[j].ciRes};
            d3a.push(p);
        }
        for (var j = 0; j < lend; j++) {
            let p = {x: c.points[j].x, y: c.points[j].nbExe, tag: c.points[j].tag};
            d3b.push(p);
        }
        let lab = getLabel("c.key.testcase.description", c.key.country, c.key.environment, c.key.robotdecli, undefined, undefined, undefined, c.key.campaign);
        var dataset1 = {
            label: lab,
            backgroundColor: "white",
            borderColor: get_Color_fromindex(i),
            pointBackgroundColor: function (d) {
                var index = d.dataIndex;
                var value = d.dataset.data[index];
                return getExeStatusRowColor(value.ciResult);
            },
            pointRadius: 4,
            pointHoverRadius: 6,
            hitRadius: 10,
            fill: false,
            data: d1
        };
        var dataset2a = {
            label: lab,
            backgroundColor: "white",
            borderColor: get_Color_fromindex(i),
            pointBackgroundColor: function (d) {
                var index = d.dataIndex;
                var value = d.dataset.data[index];
                return getExeStatusRowColor(value.ciResult);
            },
            pointRadius: 4,
            pointHoverRadius: 6,
            hitRadius: 10,
            fill: false,
            data: d2a
        };
        var dataset2b = {
            label: lab + " Threshold",
            backgroundColor: "white",
            borderColor: get_Color_fromindex(i),
            pointBackgroundColor: function (d) {
                var index = d.dataIndex;
                var value = d.dataset.data[index];
                return getExeStatusRowColor(value.ciResult);
            },
            pointRadius: 4,
            pointHoverRadius: 6,
            hitRadius: 10,
            pointStyle: 'line',
            fill: false,
            data: d2b
        };
        var dataset3a = {
            label: lab + " Useful",
            backgroundColor: "white",
            borderColor: get_Color_fromindex(i),
            pointBackgroundColor: function (d) {
                var index = d.dataIndex;
                var value = d.dataset.data[index];
                return getExeStatusRowColor(value.ciResult);
            },
            pointRadius: 4,
            pointHoverRadius: 6,
            hitRadius: 10,
            fill: false,
            data: d3a
        };
        var dataset3b = {
            label: lab + " Total",
            backgroundColor: "white",
            borderColor: get_Color_fromindex(i),
            pointBackgroundColor: function (d) {
                var index = d.dataIndex;
                var value = d.dataset.data[index];
                return getExeStatusRowColor(value.ciResult);
            },
            pointRadius: 4,
            pointHoverRadius: 6,
            hitRadius: 10,
            pointStyle: 'line',
            fill: false,
            data: d3b
        };
        timedatasets.push(dataset1);
        cidatasets.push(dataset2a);
        cidatasets.push(dataset2b);
        exedatasets.push(dataset3a);
        exedatasets.push(dataset3b);
    }

    if (timedatasets.length > 0) {
        $("#panelTagDStat").show();
        $("#panelTagSStat").show();
        $("#panelTagEStat").show();
    } else {
        $("#panelTagDStat").hide();
        $("#panelTagSStat").hide();
        $("#panelTagEStat").hide();
    }
    configTagDur.data.datasets = timedatasets;
    configTagSco.data.datasets = cidatasets;
    configTagExe.data.datasets = exedatasets;

    window.myLineTagDur.update();
    window.myLineTagSco.update();
    window.myLineTagExe.update();
}

function buildTagBarGraphs(data) {

    let curves = data.curvesTagStatus;

    // Sorting values by nb of requests.
    sortedCurves = curves.sort(function (a, b) {
        let a1 = a.key.totalExe;
        let b1 = b.key.totalExe;
        // Put RETRY On Top no matter what.
        if (b.key.key === "RETRY") {
            if (a.key.key === "RETRY") {
                return b1 - a1;
            } else {
                return -10;
            }
        } else {
            if (a.key.key === "RETRY") {
                return 10;
            } else {
                return b1 - a1;
            }
        }
        return b1 - a1;
    });


    var len = sortedCurves.length;

    let timedatasets = [];

    for (var i = 0; i < len; i++) {

        let c = sortedCurves[i];
        let d = [];
        lend = c.points.length;
        for (var j = 0; j < lend; j++) {
            let p = {x: c.points[j].x, y: c.points[j].y, id: c.points[j].exe, controlStatus: c.points[j].exeControlStatus};
            d.push(p);
        }
        let lab = c.key.key;
        var dataset = {
            label: lab,
            categoryPercentage: 1.0,
            barPercentage: 1.0,
            backgroundColor: getExeStatusRowColor(c.key.key),
            borderColor: getExeStatusRowColor(c.key.key),
            data: c.points
        };
        timedatasets.push(dataset);
    }

    if (timedatasets.length > 0) {
        $("#panelTagStatBar").show();
    } else {
        $("#panelTagStatBar").hide();
    }
    configTagBar.data.datasets = timedatasets;
    configTagBar.data.labels = data.curvesTag;

//    console.info(configTagBar);
    window.myLineTagBar.update();
}


function getLabel(tcDesc, country, env, robot, unit, party, type, testcaseid) {
    let lab = tcDesc;
    if (lab.length > 20) {
        lab = testcaseid;
    }
    if ((party !== undefined) && (party !== "total")) {
        lab += " - " + party;
    }
    if ((type !== undefined) && (type !== "total")) {
        if (lab !== "") {
            lab += " - ";
        }
        lab += type;
    }

    if (nbCountries > 1) {
        lab += " - " + country;
    }
    if (nbEnv > 1) {
        lab += " - " + env;
    }
    if (nbRobot > 1) {
        lab += " - " + robot;
    }
    if ((unit !== undefined) && (unit === "totalsize") || (unit === "sizemax") || (unit === "totaltime") || (unit === "timemax")) {
        if (lab !== "") {
            lab += " [";
        }
        lab += unit + "]";
    }

    return lab;
}

function initGraph() {

    var tagduroption = getOptions("Campaign Duration (min)", "time", "linear");
    var tagscooption = getOptions("Campaign CI Score", "score", "logarithmic");
    var tagexeoption = getOptions("Campaign Executions", "nb", "linear");
    var tagbaroption = getOptionsBar("Tag Status", "nb");

    let tagdurdatasets = [];
    let tagscodatasets = [];
    let tagexedatasets = [];
    let tagbardatasets = [];

    configTagDur = {
        type: 'line',
        data: {
            datasets: tagdurdatasets
        },
        options: tagduroption
    };
    configTagSco = {
        type: 'line',
        data: {
            datasets: tagscodatasets
        },
        options: tagscooption
    };
    configTagExe = {
        type: 'line',
        data: {
            datasets: tagexedatasets
        },
        options: tagexeoption
    };
    configTagBar = {
        type: 'bar',
        data: {
            datasets: tagbardatasets
        },
        options: tagbaroption
    };

    var ctx = document.getElementById('canvasTagDStat').getContext('2d');
    window.myLineTagDur = new Chart(ctx, configTagDur);

    var ctx = document.getElementById('canvasTagSStat').getContext('2d');
    window.myLineTagSco = new Chart(ctx, configTagSco);

    var ctx = document.getElementById('canvasTagEStat').getContext('2d');
    window.myLineTagExe = new Chart(ctx, configTagExe);

    var ctx = document.getElementById('canvasTagBar').getContext('2d');
    window.myLineTagBar = new Chart(ctx, configTagBar);

    document.getElementById('canvasTagDStat').onclick = function (evt) {
        var activePoints = window.myLineTagDur.getElementAtEvent(event);
        // make sure click was on an actual point
        if (activePoints.length > 0) {
            let tag = window.myLineTagDur.data.datasets[activePoints[0]._datasetIndex].data[activePoints[0]._index].tag;
            window.open('./ReportingExecutionByTag.jsp?Tag=' + tag, '_blank');
        }
    };

    document.getElementById('canvasTagSStat').onclick = function (evt) {
        var activePoints = window.myLineTagSco.getElementAtEvent(event);
        // make sure click was on an actual point
        if (activePoints.length > 0) {
            let tag = window.myLineTagSco.data.datasets[activePoints[0]._datasetIndex].data[activePoints[0]._index].tag;
            window.open('./ReportingExecutionByTag.jsp?Tag=' + tag, '_blank');
        }
    };

    document.getElementById('canvasTagEStat').onclick = function (evt) {
        var activePoints = window.myLineTagExe.getElementAtEvent(event);
        // make sure click was on an actual point
        if (activePoints.length > 0) {
            let tag = window.myLineTagExe.data.datasets[activePoints[0]._datasetIndex].data[activePoints[0]._index].tag;
            window.open('./ReportingExecutionByTag.jsp?Tag=' + tag, '_blank');
        }
    };

    document.getElementById('canvasTagBar').onclick = function (evt) {
        var activePoints = window.myLineTagBar.getElementAtEvent(event);
        // make sure click was on an actual point
        if (activePoints.length > 0) {
            let tag = window.myLineTagBar.data.labels[activePoints[0]._index];
            window.open('./ReportingExecutionByTag.jsp?Tag=' + tag, '_blank');
        }
    };

}