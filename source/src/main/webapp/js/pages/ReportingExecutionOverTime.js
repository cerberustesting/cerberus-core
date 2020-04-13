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
var configRequests = {};
var configSize = {};
var configTime = {};

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

        let from = new Date();
        from.setMonth(from.getMonth() - 1);
        $('#frompicker').data("DateTimePicker").date(moment(from));
        let to = new Date();
        $('#topicker').data("DateTimePicker").date(moment(to));

        var test = GetURLParameter("test");
        var testcase = GetURLParameter("testcase");

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
            $("#testSelect").prop("value", test);

            $("#testSelect").select2({width: "100%"});

            feedPerfTestCase(test, "#testCaseSelect", testcase);

        });


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
    this.numberDisplayed = 8;
}


/***
 * Feed the TestCase select with all the testcase from test defined.
 * @param {String} test - test in order to filter the testcase values.
 * @param {String} selectElement - id of select to refresh.
 * @param {String} defaultTestCase - id of testcase to select.
 * @returns {null}
 */
function feedPerfTestCase(test, selectElement, defaultTestCase) {

    var testCList = $(selectElement);
    testCList.empty();

    var jqxhr = $.getJSON("ReadTestCase", "test=" + test);
    $.when(jqxhr).then(function (data) {

        for (var index = 0; index < data.contentTable.length; index++) {
            testCList.append($('<option></option>').text(data.contentTable[index].testCase + " - " + data.contentTable[index].description).val(data.contentTable[index].testCase));
        }
        if (!isEmpty(defaultTestCase)) {
            testCList.prop("value", defaultTestCase);
        }
        loadPerfGraph();
    });
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
}

function loadPerfGraph() {

    let tcs = [];
    let tc = {
        test: "BenoitWP",
        testCase: "0001A"
    }
    tcs.push(tc);
    tc = {
        test: "BenoitWP",
        testCase: "0002A"
    }
    tcs.push(tc);

    let from = new Date($('#frompicker').data("DateTimePicker").date());

    let to = new Date($('#topicker').data("DateTimePicker").date());

    let parties = [];
    if ($("#parties").val() !== null) {
        if ($("#parties").val().length > 0) {
            parties = $("#parties").val();
        }
    } else {
        parties = ['total', 'internal'];
    }

    let types = [];
    if ($("#types").val() !== null) {
        if ($("#types").val().length > 0) {
            types = $("#types").val();
        }
    } else {
        types = ['total'];
    }

    let units = [];
    if ($("#units").val() !== null) {
        if ($("#units").val().length > 0) {
            units = $("#units").val();
        }
    } else {
        units = ['request', 'size', 'sizemax', 'time'];
    }

    let len = parties.length;
    var partiQ = "";
    for (var i = 0; i < len; i++) {
        partiQ += "&parties=" + parties[i]
    }

    len = types.length;
    var typeQ = "";
    for (var i = 0; i < len; i++) {
        typeQ += "&types=" + types[i]
    }

    len = units.length;
    var unitQ = "";
    for (var i = 0; i < len; i++) {
        unitQ += "&units=" + units[i]
    }

    let test = $("#testSelect").val();
    let testcase = $("#testCaseSelect").val();

    $.ajax({
        url: "ReadExecutionStat?e=1" + partiQ + typeQ + unitQ + "&from=" + from.toISOString() + "&to=" + to.toISOString(),
        method: "GET",
        data: {
            test: test,
            testcase: testcase
        },
        async: false,
        dataType: 'json',
        success: function (data) {
            buildGraphs(data);
            loadCombos(data);
        }
    });
}

function loadCombos(data) {

    var select = $("#parties");
    select.multiselect('destroy');
    var array = data.distinct.parties;
    $("#parties option").remove();
    for (var i = 0; i < array.length; i++) {
        console.info(array[i].name);
        $("#parties").append($('<option></option>').text(array[i].name).val(array[i].name));
    }
    for (var i = 0; i < array.length; i++) {
        if (array[i].isUsed) {
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
        if (array[i].isUsed) {
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
        if (array[i].isUsed) {
            $("#units option[value='" + array[i].name + "']").attr("selected", "selected");
        }
    }
    select.multiselect(new multiSelectConfPerf("units"));



}


function getOptions(title) {
    let option = {
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
                    }
                }]
        }
    };
    return option;
}

function buildGraphs(data) {

    curves = data.curves;

    // Sorting values by nb of requests.
    sortedCurves = curves.sort(function (a, b) {
        let a1 = a.key.testcase.testcase + "-" + a.key.party + "-" + a.key.type;
        let b1 = b.key.testcase.testcase + "-" + b.key.party + "-" + b.key.type;
        return b1.localeCompare(a1);
    });

    var len = sortedCurves.length;

    let reqdatasets = [];
    let sizedatasets = [];
    let timedatasets = [];

    for (var i = 0; i < len; i++) {

        let c = sortedCurves[i];
        let d = [];
        lend = c.points.length;
        for (var j = 0; j < lend; j++) {
            let p = {x: c.points[j].x, y: c.points[j].y};
            d.push(p);
        }
        let lab = getLabel(c.key.party, c.key.type, c.key.testcase.description);
        var dataset = {
            label: lab,
            backgroundColor: get_Color_fromindex(i),
            borderColor: get_Color_fromindex(i),
            pointBorderWidth: 10,
            hitRadius: 15,
            fill: false,
            data: d
        };
        if ((c.key.unit === "size") || (c.key.unit === "sizemax")) {
            sizedatasets.push(dataset);
        } else if (c.key.unit === "time") {
            timedatasets.push(dataset);
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
    configRequests.data.datasets = reqdatasets;
    configSize.data.datasets = sizedatasets;
    configTime.data.datasets = timedatasets;

    window.myLineReq.update();
    window.myLineSize.update();
    window.myLineTime.update();
}

function getLabel(party, type, tcDesc) {
    let lab = tcDesc;
    if (party !== "total") {
        lab += " - " + party;
    }
    if (type !== "total") {
        if (lab !== "") {
            lab += " - ";
        }
        lab += type;
    }
//    lab += " " + tcDesc;
    return lab;
}

function initGraph() {

    var reqoption = getOptions("Requests");
    var sizeoption = getOptions("Size in b");
    var timeoption = getOptions("Time in ms");

    let reqdatasets = [];
    let sizedatasets = [];
    let timedatasets = [];

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

    var ctx = document.getElementById('canvasRequests').getContext('2d');
    window.myLineReq = new Chart(ctx, configRequests);

    var ctx = document.getElementById('canvasSize').getContext('2d');
    window.myLineSize = new Chart(ctx, configSize);

    var ctx = document.getElementById('canvasTime').getContext('2d');
    window.myLineTime = new Chart(ctx, configTime);
}