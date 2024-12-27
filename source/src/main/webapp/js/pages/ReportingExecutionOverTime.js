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
var configRequests = {};
var configSize = {};
var configTime = {};
var configParty = {};
var configTcTime = {};
var configTcBar = {};
var configAvailability1 = {};
var configAvailability2 = {};
// Counters of different countries, env and robotdecli (used to shorten the labels)
var nbCountries = 0;
var nbEnv = 0;
var nbRobot = 0;
var nbcontrolStatus = 0;

$.when($.getScript("js/global/global.js")).then(function () {
    $(document).ready(function () {

        initPage();
        bindToggleCollapse();
        var urlTest = GetURLParameter('Test');
        var urlTestCase = GetURLParameter('TestCase');
        //open Run navbar Menu
        openNavbarMenu("navMenuExecutionReporting");
        $('[data-toggle="popover"]').popover({
            'placement': 'auto',
            'container': 'body'}
        );

        $('#frompicker').datetimepicker();
        $('#topicker').datetimepicker({
            useCurrent: false //Important! See issue #1075
        });

//        $("#frompicker").on("dp.change", function (e) {
//            $('#topicker').data("DateTimePicker").minDate(e.date);
//        });
//        $("#topicker").on("dp.change", function (e) {
//            $('#frompicker').data("DateTimePicker").maxDate(e.date);
//        });


        var tests = GetURLParameters("tests");
        var testcases = GetURLParameters("testcases");
        var from = GetURLParameter("from");
        var to = GetURLParameter("to");
        var parties = GetURLParameters("parties");
        var types = GetURLParameters("types");
        var units = GetURLParameters("units");
        var environments = GetURLParameters("environments");
        var countries = GetURLParameters("countries");
        var robotDeclis = GetURLParameters("robotDeclis");
        var controlStatuss = GetURLParameters("controlStatuss");

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



        $("#testSelect").empty();
        $("#testCaseSelect").empty();

        $("#testSelect").bind("change", function (event) {
            feedPerfTestCase($(this).val(), "#testCaseSelect");
        });

        $("#testCaseSelect").select2({width: "100%"});


        var jqxhr = $.getJSON("ReadTest", "");
        $.when(jqxhr).then(function (data) {
            var testList = $("#testSelect");

            for (var index = 0; index < data.contentTable.length; index++) {
                testList.append($('<option></option>').text(data.contentTable[index].test).val(data.contentTable[index].test));
            }
            $("#testSelect").prop("value", tests[0]);

            $("#testSelect").select2({width: "100%"});

            feedPerfTestCase(tests[0], "#testCaseSelect", testcases, parties, types, units, countries, environments, robotDeclis, controlStatuss);

        }).fail(handleErrorAjaxAfterTimeout);


        var select = $("#parties");
        select.multiselect(new multiSelectConfPerf("parties"));

        var select = $("#types");
        select.multiselect(new multiSelectConfPerf("types"));

        var select = $("#units");
        select.multiselect(new multiSelectConfPerf("units"));


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
 * @param {String} test - test in order to filter the testcase values.
 * @param {String} selectElement - id of select to refresh.
 * @param {String} defaultTestCases - id of testcase to select.
 * @param {String} types 
 * @param {String} units 
 * @param {String} countries 
 * @param {String} environments 
 * @param {String} robotDeclis 
 * @param {String} parties 
 * @returns {null}
 */
function feedPerfTestCase(test, selectElement, defaultTestCases, parties, types, units, countries, environments, robotDeclis, controlStatuss) {
    showLoader($("#otFilterPanel"));

    var testCList = $(selectElement);
    testCList.empty();

    var jqxhr = $.getJSON("ReadTestCase", "test=" + test);
    $.when(jqxhr).then(function (data) {

        for (var index = 0; index < data.contentTable.length; index++) {
            testCList.append($('<option></option>').text(data.contentTable[index].testcase + " - " + data.contentTable[index].description).val(data.contentTable[index].testcase));
        }
        $('#testCaseSelect').val(defaultTestCases);
        $('#testCaseSelect').trigger('change');
        loadPerfGraph(false, parties, types, units, countries, environments, robotDeclis, controlStatuss);
    }).fail(handleErrorAjaxAfterTimeout);
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

function loadPerfGraph(saveURLtoHistory, parties, types, units, countries, environments, robotDeclis, controlStatuss) {
    showLoader($("#otFilterPanel"));

    if (parties === null || parties === undefined) {
        parties = [];
    }
    if (types === null || types === undefined) {
        types = [];
    }
    if (units === null || units === undefined) {
        units = [];
    }
    if (countries === null || countries === undefined) {
        countries = [];
    }
    if (environments === null || environments === undefined) {
        environments = [];
    }
    if (robotDeclis === null || robotDeclis === undefined) {
        robotDeclis = [];
    }
    if (controlStatuss === null || controlStatuss === undefined) {
        controlStatuss = [];
    }

    let from = new Date($('#frompicker').data("DateTimePicker").date());

    let to = new Date($('#topicker').data("DateTimePicker").date());

    if ($("#parties").val() !== null) {
        parties = $("#parties").val();
    }

    if ($("#types").val() !== null) {
        types = $("#types").val();
    }

    if ($("#units").val() !== null) {
        units = $("#units").val();
    }

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

    if ($("#controlStatusSelect").val() !== null) {
        controlStatuss = $("#controlStatusSelect").val();
    }
    len = controlStatuss.length;
    var controlStatussQ = "";
    for (var i = 0; i < len; i++) {
        controlStatussQ += "&controlStatuss=" + encodeURI(controlStatuss[i]);
    }

    len = parties.length;
    var partiQ = "";
    for (var i = 0; i < len; i++) {
        partiQ += "&parties=" + encodeURI(parties[i]);
    }

    len = types.length;
    var typeQ = "";
    for (var i = 0; i < len; i++) {
        typeQ += "&types=" + encodeURI(types[i]);
    }

    len = units.length;
    var unitQ = "";
    for (var i = 0; i < len; i++) {
        unitQ += "&units=" + encodeURI(units[i]);
    }

    let test = $("#testSelect").val();
    let testcase = $("#testCaseSelect").val();
    var tcString = "";
    if ($("#testCaseSelect").val() !== null) {
        for (var i = 0; i < $("#testCaseSelect").val().length; i++) {
            var tcString = tcString + "&tests=" + encodeURI(test) + "&testcases=" + encodeURI($("#testCaseSelect").val()[i]);
        }
    }

    let qS = "from=" + from.toISOString() + "&to=" + to.toISOString() + countriesQ + environmentsQ + robotDeclisQ + controlStatussQ + partiQ + typeQ + unitQ + tcString;
    if (saveURLtoHistory) {
        InsertURLInHistory("./ReportingExecutionOverTime.jsp?" + qS);
    }

    $.ajax({
        url: "ReadExecutionStat?" + qS,
        method: "GET",
        async: true,
        dataType: 'json',
        success: function (data) {
            var messageType = getAlertType(data.messageType);

            if (data.messageType === "OK") {
                updateNbDistinct(data.distinct);
                buildGraphs(data);
                buildExeGraphs(data);
                buildExeBarGraphs(data);
                buildAvailabilityGraphs(data);
                loadCombos(data);
            } else {
                showMessageMainPage(messageType, data.message, false);
            }
            hideLoader($("#otFilterPanel"));
        },
        error: showUnexpectedError
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
    nbcontrolStatus = 0;
    for (var i = 0; i < data.controlStatuss.length; i++) {
        if (data.controlStatuss[i].isRequested) {
            nbcontrolStatus++;
        }
    }
}

function loadCombos(data) {

    if (data.hasPerfdata) {
        $("#perfFilters").show();
    } else {
        $("#perfFilters").hide();
    }
    var select = $("#parties");
    select.multiselect('destroy');
    var array = data.distinct.parties;
    $("#parties option").remove();
    for (var i = 0; i < array.length; i++) {
        $("#parties").append($('<option></option>').text(array[i].name).val(array[i].name));
    }
    for (var i = 0; i < array.length; i++) {
        if (array[i].isRequested) {
            $("#parties option[value='" + array[i].name + "']").attr("selected", "selected");
        }
    }
    select.multiselect(new multiSelectConfPerf("parties"));


    var select = $("#types");
    select.multiselect('destroy');
    var array = data.distinct.types;
    $("#types option").remove();
    for (var i = 0; i < array.length; i++) {
        $("#types").append($('<option></option>').text(array[i].name).val(array[i].name));
    }
    for (var i = 0; i < array.length; i++) {
        if (array[i].isRequested) {
            $("#types option[value='" + array[i].name + "']").attr("selected", "selected");
        }
    }
    select.multiselect(new multiSelectConfPerf("types"));


    var select = $("#units");
    select.multiselect('destroy');
    var array = data.distinct.units;
    $("#units option").remove();
    for (var i = 0; i < array.length; i++) {
        $("#units").append($('<option></option>').text(array[i].name).val(array[i].name));
    }
    for (var i = 0; i < array.length; i++) {
        if (array[i].isRequested) {
            $("#units option[value='" + array[i].name + "']").attr("selected", "selected");
        }
    }
    select.multiselect(new multiSelectConfPerf("units"));

    var select = $("#countrySelect");
    select.multiselect('destroy');
    var array = data.distinct.countries;
    $("#countrySelect option").remove();
    for (var i = 0; i < array.length; i++) {
        $("#countrySelect").append($('<option></option>').text(array[i].name).val(array[i].name));
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
        $("#envSelect").append($('<option></option>').text(array[i].name).val(array[i].name));
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

    var select = $("#controlStatusSelect");
    select.multiselect('destroy');
    var array = data.distinct.controlStatuss;
    $("#controlStatusSelect option").remove();
    for (var i = 0; i < array.length; i++) {
        let n = array[i].name;
        if (isEmpty(n)) {
            n = "[Empty]";
        }
        $("#controlStatusSelect").append($('<option></option>').text(n).val(array[i].name));
    }
    for (var i = 0; i < array.length; i++) {
        if (array[i].isRequested) {
            $("#controlStatusSelect option[value='" + array[i].name + "']").attr("selected", "selected");
        }
    }
    select.multiselect(new multiSelectConfPerf("controlStatusSelect"));

}

function getOptions(title, unit) {
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
                        return xLabel + ': ' + t.yLabel.toString().replace(/(\d)(?=(\d{3})+(?!\d))/g, "$1 ") + " ms";
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
                        }}

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
                    type: 'time',
                    stacked: true,
                    time: {
                        tooltipFormat: 'll',
                        unit: 'day',
                        round: 'day',
                        displayFormats: {
                            day: 'MMM D'
                        }},
                    scaleLabel: {
                        display: true,
                        labelString: 'Date'
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

function buildGraphs(data) {

    let curves = data.datasetPerf;

    // Sorting values by nb of requests.
    sortedCurves = curves.sort(function (a, b) {
        let a1 = a.key.testcase.test + "-" + a.key.testcase.testcase + "-" + a.key.unit + "-" + a.key.party + "-" + a.key.type;
        let b1 = b.key.testcase.test + "-" + b.key.testcase.testcase + "-" + b.key.unit + "-" + b.key.party + "-" + b.key.type;
        return b1.localeCompare(a1);
    });

    var len = sortedCurves.length;

    let reqdatasets = [];
    let sizedatasets = [];
    let timedatasets = [];
    let partydatasets = [];

    for (var i = 0; i < len; i++) {

        let c = sortedCurves[i];
        let d = [];
        lend = c.points.length;
        for (var j = 0; j < lend; j++) {
            let p = {x: c.points[j].x, y: c.points[j].y, id: c.points[j].exe, controlStatus: c.points[j].exeControlStatus, falseNegative: c.points[j].falseNegative};
            d.push(p);
        }
        let lab = getLabel(c.key.testcase.description, c.key.country, c.key.environment, c.key.robotdecli, c.key.unit, c.key.party, c.key.type, c.key.testcase.testcase);
        var dataset = {
            label: lab,
            backgroundColor: get_Color_fromindex(i),
            pointBorderWidth: function (d) {
                var index = d.dataIndex;
                var value = d.dataset.data[index];
//                console.info(value);
                return value.falseNegative === true ? 3
                        : 1;
            },
            pointBorderColor: function (d) {
                var index = d.dataIndex;
                var value = d.dataset.data[index];
//                console.info(value);
                return value.falseNegative === true ? '#00d27a'
                        : get_Color_fromindex(i);
            },
            pointBackgroundColor: function (d) {
                var index = d.dataIndex;
                var value = d.dataset.data[index];
                return getExeStatusRowColor(value.controlStatus);
            },
            borderColor: get_Color_fromindex(i),
            pointRadius: 3,
            pointHoverRadius: 6,
            hitRadius: 10,
            fill: false,
            data: d
        };
        if ((c.key.unit === "totalsize") || (c.key.unit === "sizemax")) {
            sizedatasets.push(dataset);
        } else if ((c.key.unit === "totaltime") || (c.key.unit === "timemax")) {
            timedatasets.push(dataset);
        } else if (c.key.unit === "nbthirdparty") {
            partydatasets.push(dataset);
        } else {
            reqdatasets.push(dataset);
        }
    }

    if (reqdatasets.length > 0) {
        $("#panelPerfRequests").show();
    } else {
        $("#panelPerfRequests").hide();
    }
    if (sizedatasets.length > 0) {
        $("#panelPerfSize").show();
    } else {
        $("#panelPerfSize").hide();
    }
    if (timedatasets.length > 0) {
        $("#panelPerfTime").show();
    } else {
        $("#panelPerfTime").hide();
    }
    if (partydatasets.length > 0) {
        $("#panelPerfParty").show();
    } else {
        $("#panelPerfParty").hide();
    }
    configRequests.data.datasets = reqdatasets;
    configSize.data.datasets = sizedatasets;
    configTime.data.datasets = timedatasets;
    configParty.data.datasets = partydatasets;

    window.myLineReq.update();
    window.myLineSize.update();
    window.myLineTime.update();
    window.myLineParty.update();
}

function buildExeGraphs(data) {

    let curves = data.datasetExeTime;

    // Sorting values by nb of requests.
    sortedCurves = curves.sort(function (a, b) {
        let a1 = a.key.testcase.test + "-" + a.key.testcase.testcase + "-" + a.key.unit + "-" + a.key.country + "-" + a.key.environment + "-" + a.key.robotdecli;
        let b1 = b.key.testcase.test + "-" + b.key.testcase.testcase + "-" + b.key.unit + "-" + b.key.country + "-" + b.key.environment + "-" + a.key.robotdecli;
        return b1.localeCompare(a1);
    });

    var len = sortedCurves.length;

    let timedatasets = [];

    for (var i = 0; i < len; i++) {

        let c = sortedCurves[i];
        let d = [];
        lend = c.points.length;
        for (var j = 0; j < lend; j++) {
            let p = {x: c.points[j].x, y: c.points[j].y, id: c.points[j].exe, controlStatus: c.points[j].exeControlStatus, falseNegative: c.points[j].falseNegative};
            d.push(p);
        }
        let lab = getLabel(c.key.testcase.description, c.key.country, c.key.environment, c.key.robotdecli, undefined, undefined, undefined, c.key.testcase.testcase);
        var dataset = {
            label: lab,
            backgroundColor: "white",
            pointBorderWidth: function (d) {
                var index = d.dataIndex;
                var value = d.dataset.data[index];
//                console.info(value);
                return value.falseNegative === true ? 3
                        : 1;
            },
            pointBorderColor: function (d) {
                var index = d.dataIndex;
                var value = d.dataset.data[index];
//                console.info(value);
                return value.falseNegative === true ? '#00d27a'
                        : get_Color_fromindex(i);
            },
            borderColor: get_Color_fromindex(i),
            pointBackgroundColor: function (d) {
                var index = d.dataIndex;
                var value = d.dataset.data[index];
                return getExeStatusRowColor(value.controlStatus);
            },
            pointRadius: 4,
            pointHoverRadius: 6,
            hitRadius: 10,
            fill: false,
            data: d
        };
        timedatasets.push(dataset);
    }

    if (timedatasets.length > 0) {
        $("#panelTestStat").show();
    } else {
        $("#panelTestStat").hide();
    }
    configTcTime.data.datasets = timedatasets;

    window.myLineTcTime.update();
}

function buildExeBarGraphs(data) {

    let curves = data.datasetExeStatusNb;

    // Sorting values by nb of requests.
    sortedCurves = curves.sort(function (a, b) {
        let a1 = a.key.key;
        let b1 = b.key.key;
        return b1.localeCompare(a1);
    });


    var len = sortedCurves.length;

    let timedatasets = [];

    for (var i = 0; i < len; i++) {

        let c = sortedCurves[i];
        let d = [];
        lend = c.points.length;
        for (var j = 0; j < lend; j++) {
            let p = {x: c.points[j].x, y: c.points[j].y, id: c.points[j].exe, controlStatus: c.points[j].exeControlStatus, falseNegative: c.points[j].falseNegative};
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
        $("#panelTestStatBar").show();
    } else {
        $("#panelTestStatBar").hide();
    }
    configTcBar.data.datasets = timedatasets;
    configTcBar.data.labels = data.datasetExeStatusNbDates;

//    console.info(configTcBar);
    window.myLineTcBar.update();
}

function buildAvailabilityGraphs(data) {
    let curves = data.datasetExeTime;

    var len = curves.length;

    let nbOK = 0;
    let nbKO = 0;

    let durOK = 0;
    let durKO = 0;

    for (var i = 0; i < len; i++) {
        let newCurve = curves[i];
//        console.info(newCurve);
        let lend = newCurve.points.length;
        for (var j = 0; j < lend; j++) {
            let dur = 0;
//            console.info(j + " / " + lend)
            if (j === (lend - 1)) {
                dur = 0;
            } else {
//                console.info(newCurve.points[j].x)
//                console.info(newCurve.points[j + 1].x)
                dur = (new Date(newCurve.points[j + 1].x) - new Date(newCurve.points[j].x)) / 1000;
//                console.info((new Date(newCurve.points[j + 1].x) - new Date(newCurve.points[j].x)) / 1000)
            }

            if ((newCurve.points[j].exeControlStatus === "OK") || (newCurve.points[j].falseNegative)) {
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
        backgroundColor: [getExeStatusRowColor("OK"), getExeStatusRowColor("OTHERS")],
    });
    configAvailability1.data.labels = ["nb OK", "nb Others"];

    configAvailability2.data.datasets = [];
    configAvailability2.data.datasets.push({
        data: [durOK, durKO],
        backgroundColor: [getExeStatusRowColor("OK"), getExeStatusRowColor("OTHERS")],
    });
    configAvailability2.data.labels = ["OK duration (s)", "Others duration (s)"];
    configAvailability2.data.labels.display = false;

    document.getElementById('ChartAvailabilty1Counter').innerHTML = Math.round(nbOK / (nbOK + nbKO) * 100) + " %";
    document.getElementById('ChartAvailabilty1CounterDet').innerHTML = "<b style='color:lightgrey'>" + nbKO + "</b> / " + (nbOK + nbKO);

    document.getElementById('ChartAvailabilty2Counter').innerHTML = Math.round(durOK / (durOK + durKO) * 100) + " %";
    document.getElementById('ChartAvailabilty2CounterDet').innerHTML = "<b style='color:lightgrey'>" + getHumanReadableDuration(durKO) + "</b> / " + getHumanReadableDuration((durOK + durKO));


//    console.info(configTagBar);
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

    var reqoption = getOptions("Requests", "request");
    var sizeoption = getOptions("Size in kb", "size");
    var timeoption = getOptions("Time in ms", "time");
    var partyoption = getOptions("nb Third Party", "nbthirdparty");
    var tctimeoption = getOptions("Test Case Duration", "time");
    var tcbaroption = getOptionsBar("Test Case Duration", "nb");

    let reqdatasets = [];
    let sizedatasets = [];
    let timedatasets = [];
    let partydatasets = [];
    let tctimedatasets = [];
    let tcbardatasets = [];
    let availability1datasets = [];
    let availability2datasets = [];

    configRequests = {
        type: 'line',
        data: {
            datasets: reqdatasets
        },
        options: reqoption
    };
    configSize = {
        type: 'line',
        data: {
            datasets: sizedatasets
        },
        options: sizeoption
    };
    configTime = {
        type: 'line',
        data: {
            datasets: timedatasets
        },
        options: timeoption
    };
    configParty = {
        type: 'line',
        data: {
            datasets: partydatasets
        },
        options: partyoption
    };
    configTcTime = {
        type: 'line',
        data: {
            datasets: tctimedatasets
        },
        options: tctimeoption
    };
    configTcBar = {
        type: 'bar',
        data: {
            datasets: tcbardatasets
        },
        options: tcbaroption
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
                text: "Execution Availability (Nb)"
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
                text: "Execution Availability (Time)"
            }
        }
    };

    var ctx = document.getElementById('canvasRequests').getContext('2d');
    window.myLineReq = new Chart(ctx, configRequests);

    var ctx = document.getElementById('canvasSize').getContext('2d');
    window.myLineSize = new Chart(ctx, configSize);

    var ctx = document.getElementById('canvasTime').getContext('2d');
    window.myLineTime = new Chart(ctx, configTime);

    var ctx = document.getElementById('canvasParty').getContext('2d');
    window.myLineParty = new Chart(ctx, configParty);

    var ctx = document.getElementById('canvasTestStat').getContext('2d');
    window.myLineTcTime = new Chart(ctx, configTcTime);

    var ctx = document.getElementById('canvasTestStatBar').getContext('2d');
    window.myLineTcBar = new Chart(ctx, configTcBar);

    var ctx = document.getElementById('canvasAvailability1').getContext('2d');
    window.myAvailability1 = new Chart(ctx, configAvailability1);

    var ctx = document.getElementById('canvasAvailability2').getContext('2d');
    window.myAvailability2 = new Chart(ctx, configAvailability2);


    document.getElementById('canvasRequests').onclick = function (evt) {
        var activePoints = window.myLineReq.getElementAtEvent(event);
        // make sure click was on an actual point
        if (activePoints.length > 0) {
            let exe = window.myLineReq.data.datasets[activePoints[0]._datasetIndex].data[activePoints[0]._index].id;
            window.open('./TestCaseExecution.jsp?executionId=' + exe, '_blank');
        }
    };

    document.getElementById('canvasSize').onclick = function (evt) {
        var activePoints = window.myLineSize.getElementAtEvent(event);
        // make sure click was on an actual point
        if (activePoints.length > 0) {
            let exe = window.myLineSize.data.datasets[activePoints[0]._datasetIndex].data[activePoints[0]._index].id;
            window.open('./TestCaseExecution.jsp?executionId=' + exe, '_blank');
        }
    };

    document.getElementById('canvasTime').onclick = function (evt) {
        var activePoints = window.myLineTime.getElementAtEvent(event);
        // make sure click was on an actual point
        if (activePoints.length > 0) {
            let exe = window.myLineTime.data.datasets[activePoints[0]._datasetIndex].data[activePoints[0]._index].id;
            window.open('./TestCaseExecution.jsp?executionId=' + exe, '_blank');
        }
    };

    document.getElementById('canvasParty').onclick = function (evt) {
        var activePoints = window.myLineParty.getElementAtEvent(event);
        // make sure click was on an actual point
        if (activePoints.length > 0) {
            let exe = window.myLineParty.data.datasets[activePoints[0]._datasetIndex].data[activePoints[0]._index].id;
            window.open('./TestCaseExecution.jsp?executionId=' + exe, '_blank');
        }
    };

    document.getElementById('canvasTestStat').onclick = function (evt) {
        var activePoints = window.myLineTcTime.getElementAtEvent(event);
        // make sure click was on an actual point
        if (activePoints.length > 0) {
            let exe = window.myLineTcTime.data.datasets[activePoints[0]._datasetIndex].data[activePoints[0]._index].id;
            window.open('./TestCaseExecution.jsp?executionId=' + exe, '_blank');
        }
    };

}