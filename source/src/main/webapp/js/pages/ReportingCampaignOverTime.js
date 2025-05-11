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
var configTagDur = {};
var configTagSco = {};
var configTagExe = {};
var configTagBar = {};
var configAvailability1 = {};
var configAvailability2 = {};
// Counters of different countries, env and robotdecli (used to shorten the labels)
var nbCountries = 0;
var nbEnv = 0;
var nbRobot = 0;
var nbCIResult = 0;

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

        moment.locale('en', {
            week: {dow: 1} // Monday is the first day of the week
        });

        $('#frompicker').datetimepicker({
            showTodayButton: true,
            sideBySide: true,
            keepOpen: false
        });
//        $('#frompicker').on("change", function () {
//            $(this).datetimepicker('hide');
//        });

        $('#topicker').datetimepicker({
            showTodayButton: true,
            sideBySide: true,
            keepOpen: false,
            useCurrent: false //Important! See issue #1075
        });
//        $('#topicker').on("change", function () {
//            $(this).datetimepicker('hide');
//        });

//        $("#frompicker").on("dp.change", function (e) {
//            $('#topicker').data("DateTimePicker").minDate(e.date);
//        });
//        $("#topicker").on("dp.change", function (e) {
//            $('#frompicker').data("DateTimePicker").maxDate(e.date);
//        });


        var campaigns = GetURLParameters("campaigns");
        var from = GetURLParameter("from");
        var to = GetURLParameter("to");
        var environments = GetURLParameters("environments");
        var countries = GetURLParameters("countries");
        var robotDeclis = GetURLParameters("robotDeclis");
        var ciResults = GetURLParameters("ciResults");
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
        feedPerfCampaign("#campaignSelect", campaigns, countries, environments, robotDeclis, ciResults, gp1s, gp2s, gp3s);

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
function feedPerfCampaign(selectElement, defaultCampaigns, countries, environments, robotDeclis, ciResults, gp1s, gp2s, gp3s) {
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
        loadPerfGraph(false, countries, environments, robotDeclis, ciResults, gp1s, gp2s, gp3s)
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
    $("#pageTitle").html(doc.getDocLabel("page_campaignreportovertime", "title"));
    $("#title").html(doc.getDocOnline("page_campaignreportovertime", "title"));
    $("#loadbutton").html(doc.getDocLabel("page_global", "buttonLoad"));
    $("#filters").html(doc.getDocOnline("page_global", "filters"));
}

function loadPerfGraph(saveURLtoHistory, countries, environments, robotDeclis, ciResults, gp1s, gp2s, gp3s) {
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
    if (ciResults === null || ciResults === undefined) {
        ciResults = [];
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
    if (countriesQ.length > 1000)
        countriesQ = "";

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

    if ($("#ciResultSelect").val() !== null) {
        ciResults = $("#ciResultSelect").val();
    }
    len = ciResults.length;
    var ciResultsQ = "";
    for (var i = 0; i < len; i++) {
        ciResultsQ += "&ciResults=" + encodeURI(ciResults[i]);
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

    let qS = "from=" + from.toISOString() + "&to=" + to.toISOString() + campaignString + countriesQ + environmentsQ + robotDeclisQ + ciResultsQ + gp1sQ + gp2sQ + gp3sQ;
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
                buildAvailabilityGraphs(data);
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
    nbCIResult = 0;
    for (var i = 0; i < data.ciResults.length; i++) {
        if (data.ciResults[i].isRequested) {
            nbCIResult++;
        }
    }
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

    var select = $("#ciResultSelect");
    select.multiselect('destroy');
    var array = data.distinct.ciResults;
    $("#ciResultSelect option").remove();
    for (var i = 0; i < array.length; i++) {
        let n = array[i].name;
        if (isEmpty(n)) {
            n = "[Empty]";
        }
        $("#ciResultSelect").append($('<option></option>').text(n).val(array[i].name));
    }
    for (var i = 0; i < array.length; i++) {
        if (array[i].isRequested) {
            $("#ciResultSelect option[value='" + array[i].name + "']").attr("selected", "selected");
        }
    }
    select.multiselect(new multiSelectConfPerf("ciResultSelect"));

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
                    newlabel = [];
                    var xLabel = d.datasets[t.datasetIndex].label;
                    let xlab = "";
                    let com1 = "";
                    let desc = "";
                    if (!isEmpty(d.datasets[t.datasetIndex].data[t.index].tag)) {
                        xlab += " - ";
                        xlab += d.datasets[t.datasetIndex].data[t.index].tag;
                    }
                    if (!isEmpty(d.datasets[t.datasetIndex].data[t.index].comment)) {
//                        com1 += " - ";
                        com1 = "   " + d.datasets[t.datasetIndex].data[t.index].comment;
                    }
                    if (!isEmpty(d.datasets[t.datasetIndex].data[t.index].desc)) {
//                        com += " - ";
                        desc = "   " + d.datasets[t.datasetIndex].data[t.index].desc.replace(/<[^>]*>/g, "");
                    }
//                    newlabel.push(xLabel + ': ' + t.yLabel);
//                    newlabel.push(com);
                    if (unit === "size") {
                        newlabel.push(xLabel + ': ' + formatNumber(Math.round(t.yLabel / 1024)) + " kb" + xlab);
                        if (desc !== "")
                            newlabel.push(desc);
                        if (com1 !== "")
                            newlabel.push(com1);
//                        return  + com;
                    } else if (unit === "time") {
                        newlabel.push(xLabel + ': ' + t.yLabel.toString().replace(/(\d)(?=(\d{3})+(?!\d))/g, "$1 ") + " min" + xlab);
                        if (desc !== "")
                            newlabel.push(desc);
                        if (com1 !== "")
                            newlabel.push(com1);
                    } else {
                        newlabel.push(xLabel + ': ' + t.yLabel + xlab);
                        if (desc !== "")
                            newlabel.push(desc);
                        if (com1 !== "")
                            newlabel.push(com1);
                    }
                    return newlabel;
                }
            }
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
        let d2c = [];
        let d3a = [];
        let d3b = [];
        let d3c = [];
        let d3d = [];
        lend = c.points.length;
        for (var j = 0; j < lend; j++) {
            let p = {x: c.points[j].x, y: c.points[j].y, tag: c.points[j].tag, ciResult: c.points[j].ciRes, desc: c.points[j].description, comment: c.points[j].comment, falseNegative: c.points[j].falseNegative};
            d1.push(p);
        }
        for (var j = 0; j < lend; j++) {
            let p = {x: c.points[j].x, y: c.points[j].ciSc, tag: c.points[j].tag, ciResult: c.points[j].ciRes, desc: c.points[j].description, comment: c.points[j].comment, falseNegative: c.points[j].falseNegative};
            d2a.push(p);
        }
        for (var j = 0; j < lend; j++) {
            let p = {x: c.points[j].x, y: c.points[j].ciScT, tag: c.points[j].tag, desc: c.points[j].description, comment: c.points[j].comment, falseNegative: c.points[j].falseNegative};
            d2b.push(p);
        }
        for (var j = 0; j < lend; j++) {
            let p = {x: c.points[j].x, y: c.points[j].ciScM, tag: c.points[j].tag, desc: c.points[j].description, comment: c.points[j].comment, falseNegative: c.points[j].falseNegative};
            d2c.push(p);
        }
        for (var j = 0; j < lend; j++) {
            let p = {x: c.points[j].x, y: c.points[j].nbExeU, tag: c.points[j].tag, ciResult: c.points[j].ciRes, desc: c.points[j].description, comment: c.points[j].comment, falseNegative: c.points[j].falseNegative};
            d3a.push(p);
        }
        for (var j = 0; j < lend; j++) {
            let p = {x: c.points[j].x, y: c.points[j].nbExe, tag: c.points[j].tag, desc: c.points[j].description, comment: c.points[j].comment, falseNegative: c.points[j].falseNegative};
            d3b.push(p);
        }
        for (var j = 0; j < lend; j++) {
            let p = {x: c.points[j].x, y: c.points[j].nbFlaky, tag: c.points[j].tag, desc: c.points[j].description, comment: c.points[j].comment, falseNegative: c.points[j].falseNegative};
            d3c.push(p);
        }
        for (var j = 0; j < lend; j++) {
            let p = {x: c.points[j].x, y: c.points[j].nbMuted, tag: c.points[j].tag, desc: c.points[j].description, comment: c.points[j].comment, falseNegative: c.points[j].falseNegative};
            d3d.push(p);
        }
        let lab = getLabel("c.key.testcase.description", c.key.country, c.key.environment, c.key.robotdecli, undefined, undefined, undefined, c.key.campaign);
        // If the nb of characters of the label is too big, we just put an index instead. That avoid to have the legend taking the full size of the graph.
        if (lab.length > 100)
            lab = i;
        var dataset1 = {
            label: lab,
            backgroundColor: "white",
            borderColor: get_Color_fromindex(i),
            pointBorderWidth: function (d) {
                var index = d.dataIndex;
                var value = d.dataset.data[index];
                return value.falseNegative === true ? 3
                        : 1;
            },
            pointBorderColor: function (d) {
                var index = d.dataIndex;
                var value = d.dataset.data[index];
                return value.falseNegative === true ? '#00d27a'
                        : get_Color_fromindex(i);
            },
            pointBackgroundColor: function (d) {
                var index = d.dataIndex;
                var value = d.dataset.data[index];
                return getExeStatusRowColor(value.ciResult);
            },
            pointRadius: function (context) {
                var index = context.dataIndex;
                var value = context.dataset.data[index];
                return value.comment === '' ? 4 : 8;
            },
            pointHoverRadius: 6,
            hitRadius: 10,
            fill: false,
            data: d1
        };
        var dataset2a = {
            label: lab,
            backgroundColor: "white",
            borderColor: get_Color_fromindex(i),
            pointBorderWidth: function (d) {
                var index = d.dataIndex;
                var value = d.dataset.data[index];
                return value.falseNegative === true ? 3
                        : 1;
            },
            pointBorderColor: function (d) {
                var index = d.dataIndex;
                var value = d.dataset.data[index];
                return value.falseNegative === true ? '#00d27a'
                        : get_Color_fromindex(i);
            },
            pointBackgroundColor: function (d) {
                var index = d.dataIndex;
                var value = d.dataset.data[index];
                return getExeStatusRowColor(value.ciResult);
            },
            pointRadius: function (context) {
                var index = context.dataIndex;
                var value = context.dataset.data[index];

                return value.comment === '' ? 4 : 8;
            },
            pointHoverRadius: 6,
            hitRadius: 10,
            fill: false,
            data: d2a
        };
        var dataset2b = {
            label: lab + " Threshold",
            backgroundColor: "white",
            borderColor: get_Color_fromindex(i),
            borderDash: [10, 10],
            pointBorderWidth: function (d) {
                var index = d.dataIndex;
                var value = d.dataset.data[index];
                return value.falseNegative === true ? 3
                        : 1;
            },
            pointBorderColor: function (d) {
                var index = d.dataIndex;
                var value = d.dataset.data[index];
                return value.falseNegative === true ? '#00d27a'
                        : get_Color_fromindex(i);
            },
            pointBackgroundColor: function (d) {
                var index = d.dataIndex;
                var value = d.dataset.data[index];
                return getExeStatusRowColor(value.ciResult);
            },
            pointRadius: function (context) {
                var index = context.dataIndex;
                var value = context.dataset.data[index];
                return value.comment === '' ? 4 : 8;
            },
            pointHoverRadius: 6,
            hitRadius: 10,
            pointStyle: 'line',
            fill: false,
            data: d2b
        };
        var dataset2c = {
            label: lab + " Max",
            backgroundColor: "white",
            borderColor: get_Color_fromindex(i),
            borderWidth: 5,
//            borderDash:[5, 10],
            pointBorderWidth: function (d) {
                var index = d.dataIndex;
                var value = d.dataset.data[index];
                return value.falseNegative === true ? 3
                        : 1;
            },
            pointBorderColor: function (d) {
                var index = d.dataIndex;
                var value = d.dataset.data[index];
                return value.falseNegative === true ? '#00d27a'
                        : get_Color_fromindex(i);
            },
            pointBackgroundColor: function (d) {
                var index = d.dataIndex;
                var value = d.dataset.data[index];
                return getExeStatusRowColor(value.ciResult);
            },
            pointRadius: function (context) {
                var index = context.dataIndex;
                var value = context.dataset.data[index];
                return value.comment === '' ? 4 : 8;
            },
            pointHoverRadius: 6,
            hitRadius: 10,
            pointStyle: 'line',
            tension: 0,
            fill: false,
            data: d2c,
            hidden: true
        };
        var dataset3a = {
            label: lab + " Useful",
            backgroundColor: "white",
            borderColor: get_Color_fromindex(i),
            pointBorderWidth: function (d) {
                var index = d.dataIndex;
                var value = d.dataset.data[index];
                return value.falseNegative === true ? 3
                        : 1;
            },
            pointBorderColor: function (d) {
                var index = d.dataIndex;
                var value = d.dataset.data[index];
                return value.falseNegative === true ? '#00d27a'
                        : get_Color_fromindex(i);
            },
            pointBackgroundColor: function (d) {
                var index = d.dataIndex;
                var value = d.dataset.data[index];
                return getExeStatusRowColor(value.ciResult);
            },
            pointRadius: function (context) {
                var index = context.dataIndex;
                var value = context.dataset.data[index];
                return value.comment === '' ? 4 : 8;
            },
            pointHoverRadius: 6,
            hitRadius: 10,
            fill: false,
            data: d3a
        };
        var dataset3b = {
            label: lab + " Total",
            backgroundColor: "white",
            borderDash: [10, 10],
            borderColor: get_Color_fromindex(i),
            pointBackgroundColor: function (d) {
                var index = d.dataIndex;
                var value = d.dataset.data[index];
                return getExeStatusRowColor(value.ciResult);
            },
            pointRadius: function (context) {
                var index = context.dataIndex;
                var value = context.dataset.data[index];

                return value.comment === '' ? 4 : 8;
            },
            pointHoverRadius: 6,
            hitRadius: 10,
            pointStyle: 'line',
            fill: false,
            data: d3b
        };
        var dataset3c = {
            label: lab + " Flaky",
            backgroundColor: "white",
//            borderDash:[10, 10],
            borderColor: get_Color_fromindex(i),
            borderWidth: 5,
            pointBackgroundColor: function (d) {
                var index = d.dataIndex;
                var value = d.dataset.data[index];
                return getExeStatusRowColor(value.ciResult);
            },
            pointRadius: function (context) {
                var index = context.dataIndex;
                var value = context.dataset.data[index];

                return value.comment === '' ? 4 : 8;
            },
            pointHoverRadius: 6,
            hitRadius: 10,
            pointStyle: 'line',
            tension: 0,
            fill: 'origin',
            data: d3c,
            hidden: true
        };
        var dataset3d = {
            label: lab + " Muted",
            backgroundColor: "white",
            borderDash: [5, 5],
            borderColor: get_Color_fromindex(i),
            borderWidth: 5,
            pointBackgroundColor: function (d) {
                var index = d.dataIndex;
                var value = d.dataset.data[index];
                return getExeStatusRowColor(value.ciResult);
            },
            pointRadius: function (context) {
                var index = context.dataIndex;
                var value = context.dataset.data[index];

                return value.comment === '' ? 4 : 8;
            },
            pointHoverRadius: 6,
            hitRadius: 10,
            pointStyle: 'line',
            tension: 0,
            fill: false,
            data: d3d,
            hidden: true
        };
        timedatasets.push(dataset1);
        cidatasets.push(dataset2a);
        cidatasets.push(dataset2b);
        cidatasets.push(dataset2c);
        exedatasets.push(dataset3a);
        exedatasets.push(dataset3b);
        exedatasets.push(dataset3c);
        exedatasets.push(dataset3d);
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

function buildAvailabilityGraphs(data) {

    let curves = data.curvesTime;

    var len = curves.length;

    let nbOK = 0;
    let nbKO = 0;

    let durOK = 0;
    let durKO = 0;

    for (var i = 0; i < len; i++) {
        let newCurve = curves[i];
        let lend = newCurve.points.length;
        for (var j = 0; j < lend; j++) {
            let dur = 0;
            if (j === (lend - 1)) {
                dur = 0;
            } else {
                dur = (new Date(newCurve.points[j + 1].x) - new Date(newCurve.points[j].x)) / 1000;
            }
            if ((newCurve.points[j].ciRes === "OK") || (newCurve.points[j].falseNegative)) {
                nbOK++;
                durOK = durOK + dur;
            } else {
                nbKO++;
                durKO = durKO + dur;
            }
        }
    }


    configAvailability1.data.datasets = [];
    configAvailability1.data.datasets.push({
        data: [nbOK, nbKO],
        backgroundColor: [getExeStatusRowColor("OK"), getExeStatusRowColor("KO")],
//        label: 'Nb',
//        labels: ["OK", "Others"]
    });
    configAvailability1.data.labels = ["nb OK", "nb KO"];

    configAvailability2.data.datasets = [];
    configAvailability2.data.datasets.push({
        data: [durOK, durKO],
        backgroundColor: [getExeStatusRowColor("OK"), getExeStatusRowColor("KO")],
//        label: 'Nb',
//        labels: ["OK", "Others"]
    });
    configAvailability2.data.labels = ["OK duration (s)", "KO duration (s)"];
    configAvailability2.data.labels.display = false;
//    display: true,

//    configAvailability1.data.datasets = [nbOK, nbKO];
//    configTagBar.data.labels = data.curvesTag;

    document.getElementById('ChartAvailabilty1Counter').innerHTML = Math.round(nbOK / (nbOK + nbKO) * 100) + " %";
    document.getElementById('ChartAvailabilty1CounterDet').innerHTML = "<b style='color:#e63757'>" + nbKO + "</b> / " + (nbOK + nbKO);
    document.getElementById('ChartAvailabilty2Counter').innerHTML = Math.round(durOK / (durOK + durKO) * 100) + " %";
    document.getElementById('ChartAvailabilty2CounterDet').innerHTML = "<b style='color:#e63757'>" + getHumanReadableDuration(durKO) + "</b> / " + getHumanReadableDuration((durOK + durKO));

    window.myAvailability1.update();
    window.myAvailability2.update();
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
    let availability1datasets = [];
    let availability2datasets = [];

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

    configAvailability1 = {
        type: 'pie',
        data: {
            datasets: availability1datasets
        },
        options: {
            circumference: Math.PI,
            rotation: Math.PI,
            responsive: true,
            legend: {
                display: false
            },
            title: {
                display: true,
                text: "Campaign Availability (Nb)"
            }
        }
    };
    configAvailability2 = {
        type: 'pie',
        data: {
            datasets: availability2datasets
        },
        options: {
            circumference: Math.PI,
            rotation: Math.PI,
            responsive: true,
            legend: {
                display: false
            },
            title: {
                display: true,
                text: "Campaign Availability (Time)"
            }
        }
    };


    // Duration of campaign
    var ctx = document.getElementById('canvasTagDStat').getContext('2d');
    window.myLineTagDur = new Chart(ctx, configTagDur);

    // Score of campaign
    var ctx = document.getElementById('canvasTagSStat').getContext('2d');
    window.myLineTagSco = new Chart(ctx, configTagSco);

    // nb Executions of campaign
    var ctx = document.getElementById('canvasTagEStat').getContext('2d');
    window.myLineTagExe = new Chart(ctx, configTagExe);

    var ctx = document.getElementById('canvasTagBar').getContext('2d');
    window.myLineTagBar = new Chart(ctx, configTagBar);

    var ctx = document.getElementById('canvasAvailability1').getContext('2d');
    window.myAvailability1 = new Chart(ctx, configAvailability1);

    var ctx = document.getElementById('canvasAvailability2').getContext('2d');
    window.myAvailability2 = new Chart(ctx, configAvailability2);

    document.getElementById('canvasTagDStat').onclick = function (evt) {
        var activePoints = window.myLineTagDur.getElementAtEvent(event);
        // make sure click was on an actual point
        if (activePoints.length > 0) {
            let tag = window.myLineTagDur.data.datasets[activePoints[0]._datasetIndex].data[activePoints[0]._index].tag;
            window.open('./ReportingExecutionByTag.jsp?Tag=' + tag);
        }
    };

    document.getElementById('canvasTagSStat').onclick = function (evt) {
        var activePoints = window.myLineTagSco.getElementAtEvent(event);
        // make sure click was on an actual point
        if (activePoints.length > 0) {
            let tag = window.myLineTagSco.data.datasets[activePoints[0]._datasetIndex].data[activePoints[0]._index].tag;
            window.open('./ReportingExecutionByTag.jsp?Tag=' + tag);
        }
    };

    document.getElementById('canvasTagEStat').onclick = function (evt) {
        var activePoints = window.myLineTagExe.getElementAtEvent(event);
        // make sure click was on an actual point
        if (activePoints.length > 0) {
            let tag = window.myLineTagExe.data.datasets[activePoints[0]._datasetIndex].data[activePoints[0]._index].tag;
            window.open('./ReportingExecutionByTag.jsp?Tag=' + tag);
        }
    };

    document.getElementById('canvasTagBar').onclick = function (evt) {
        var activePoints = window.myLineTagBar.getElementAtEvent(event);
        // make sure click was on an actual point
        if (activePoints.length > 0) {
            let tag = window.myLineTagBar.data.labels[activePoints[0]._index];
            window.open('./ReportingExecutionByTag.jsp?Tag=' + tag);
        }
    };

}