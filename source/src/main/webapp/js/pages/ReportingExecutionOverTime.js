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
    });
});
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
    let from = "";
    let to = "";
    let parties = ['internal', 'total'];
    let units = ['request', 'size', 'time'];
    let types = ['img', 'css', 'js', 'total'];

    $.ajax({
        url: "ReadExecutionStat",
        method: "POST",
        data: {
            testCases: tcs,
            from: from,
            to: to,
            parties: parties,
            units: units,
            types: types
        },
        async: false,
        dataType: 'json',
        success: function (data) {
            buildGraphs(data);
        }
    });
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
    var len = data.curves.length;

    let reqdatasets = [];
    let sizedatasets = [];
    let timedatasets = [];

    for (var i = 0; i < len; i++) {

        let c = data.curves[i];
        let d = [];
        lend = c.points.length;
        for (var j = 0; j < lend; j++) {
            let p = {x: c.points[j].x, y: c.points[j].y};
            d.push(p);
        }

        let lab = "";
        if (c.key.party !== "total") {
            lab += c.key.party;
        }
        if (c.key.type !== "total") {
            if (lab !== "") {
                lab += " ";
            }
            lab += c.key.type;
        }
        lab += " " + c.key.testcase.description;
        var dataset = {
            label: lab,
            backgroundColor: get_Color_fromindex(i),
            borderColor: get_Color_fromindex(i),
            pointBorderWidth: 10,
            hitRadius: 15,
            fill: false,
            data: d
        };
        if ((c.key.unit === "size") || (c.key.unit === "sizeMax")) {
            sizedatasets.push(dataset);
        } else if (c.key.unit === "time") {
            timedatasets.push(dataset);
        } else {
            reqdatasets.push(dataset);
        }
    }

    configRequests.data.datasets = reqdatasets;
    configSize.data.datasets = sizedatasets;
    configTime.data.datasets = timedatasets;

    window.myLineReq.update();
    window.myLineSize.update();
    window.myLineTime.update();
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