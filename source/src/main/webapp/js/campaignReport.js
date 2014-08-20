/*
 * Cerberus  Copyright (C) 2013  vertigo17
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

var config = {
    //String - Colour of the grid lines
    scaleGridLineColor: "rgba(0,0,0,0.2)",
    //String - A legend template
    legendTemplate: "<ul class=\"<%=name.toLowerCase()%>-legend\"><% for (var i=0; i<segments.length; i++){%><li><span style=\"display:inline-block;width:30px;height:30px;margin:5px;background-color:<%=segments[i].fillColor%>\"><%if(segments[i].label){%><%=segments[i].label%><%}%></span></li><%}%></ul>"

};

var data = {
    labels: ["OK", "KO", "FA", "NA", "PE"],
    datasets: [
        {
            label: "",
            data: [0, 0, 0, 0, 0],
            fillColors: ["#00EE00", "#F7464A", "#FDB45C", "#EEEE00", "#0000DD"],
            strokeColors: ["#00EE00", "#F7464A", "#FDB45C", "#EEEE00", "#0000DD"],
            highlightFills: ["#33DD33", "#FF5A5E", "#FFC870", "#EEEE55", "#5555DD"],
            highlightStrokes: ["#33DD33", "#FF5A5E", "#FFC870", "#EEEE55", "#5555DD"]
        }
    ]
};

var dataDonut = [
    {
        value: 0,
        color: "#00EE00",
        highlight: "#33DD33",
        label: "OK"
    },
    {
        value: 0,
        color: "#F7464A",
        highlight: "#FF5A5E",
        label: "KO"
    },
    {
        value: 0,
        color: "#FDB45C",
        highlight: "#FFC870",
        label: "FA"
    },
    {
        value: 0,
        color: "#EEEE00",
        highlight: "#EEEE55",
        label: "NA"
    },
    {
        value: 0,
        color: "#555555",
        highlight: "#333333",
        label: "PE"
    }
];

var dataPercent = {};

var dataPercentLabels = {};

var dataFunction = {
    labels: [],
    datasets: [
        {
            label: "My Second dataset",
            fillColor: "rgba(151,187,205,0.5)",
            strokeColor: "rgba(151,187,205,0.8)",
            highlightFill: "rgba(151,187,205,0.75)",
            highlightStroke: "rgba(151,187,205,1)",
            data: []
        }
    ]
};

var testCaseStatusLine = $("<tr class='testcase'>" +
        "<td class='ID'></td>" +
        "<td class='Test'></td>" +
        "<td class='TestCase'></td>" +
        "<td class='Control'></td>" +
        "<td class='Status'></td>" +
        "<td class='Application'></td>" +
        "<td class='BugID'></td>" +
        "<td class='Comment'></td>" +
        "<td class='Start'></td>" +
        "<td class='End'></td>" +
        "</tr>");

var executionLink = $("<a target='executionFromReport' href='ExecutionDetail.jsp?id_tc='></a>");
var testcaseLink = $("<a target='testcaseFromReport' href='TestCase.jsp?Load=Load&Test='></a>");

function addTestCaseToStatusTabs(testcase) {
    var statusTable = $("#Status" + testcase.ControlStatus + " tbody");

    var statusTestCaseStatusLine = testCaseStatusLine.clone();

    var statusExecutionLink = executionLink.clone();
    statusExecutionLink.attr('href', statusExecutionLink.attr('href') + testcase.ID);
    statusExecutionLink.text(testcase.ID);
    statusTestCaseStatusLine.find(".ID").append(statusExecutionLink);

    var statusTestcaseLink = testcaseLink.clone();
    statusTestcaseLink.attr('href', statusTestcaseLink.attr('href') + testcase.Test 
            + "&TestCase="+testcase.TestCase);
    statusTestcaseLink.text(testcase.TestCase);    
    statusTestCaseStatusLine.find(".TestCase").append(statusTestcaseLink);

    statusTestCaseStatusLine.find(".Test").text(testcase.Test);
    statusTestCaseStatusLine.find(".Control").text(testcase.ControlStatus);
    statusTestCaseStatusLine.find(".Status").text(testcase.Status);
    statusTestCaseStatusLine.find(".BugID").text(testcase.BugID);
    statusTestCaseStatusLine.find(".Application").text(testcase.Application);
    statusTestCaseStatusLine.find(".Comment").text(testcase.Comment);
    
    var date = new Date();
    date.setTime(testcase.Start);
    statusTestCaseStatusLine.find(".Start").text(date.toLocaleString());
    var date = new Date();
    date.setTime(testcase.End);
    statusTestCaseStatusLine.find(".End").text(date.toLocaleString());

    if (statusTable.find("tr").length % 2) {
        statusTestCaseStatusLine.addClass("odd");
    } else {
        statusTestCaseStatusLine.addClass("even");
    }
    
    statusTable.append(statusTestCaseStatusLine);
};

function addTestCaseToPercentRadar(testcase) {
    
    if(!dataPercent[testcase.Test]) {
        dataPercent[testcase.Test] = {
            OK: 0,
            KO: 0,
            FA: 0,
            NA: 0,
            total: 0
            };
    }
    
    dataPercent[testcase.Test]['total'] = eval(dataPercent[testcase.Test]['total'] + 1);
    dataPercent[testcase.Test][testcase.ControlStatus] = eval(dataPercent[testcase.Test][testcase.ControlStatus] + 1);
};



function computePercentDataRadar(ctx) {
    
    var data = {
        labels: [],
        datasets: [
            {
                label: "OK",
                fillColor: "#00EE00",
                strokeColor: "#00EE00",
                pointColor: "#33DD33",
                pointStrokeColor: "#fff",
                pointHighlightFill: "#33DD33",
                pointHighlightStroke: "#00EE00",
                data: []
            },
            {
                label: "KO",
                fillColor: "#F7464A",
                strokeColor: "#F7464A",
                pointColor: "#FF5A5E",
                pointStrokeColor: "#fff",
                pointHighlightFill: "#FF5A5E",
                pointHighlightStroke: "#F7464A",
                data: []
            },
            {
                label: "FA",
                fillColor: "#FDB45C",
                strokeColor: "#FDB45C",
                pointColor: "#FFC870",
                pointStrokeColor: "#fff",
                pointHighlightFill: "#FFC870",
                pointHighlightStroke: "#FDB45C",
                data: []
            },
            {
                label: "NA",
                fillColor: "#EEEE00",
                strokeColor: "#EEEE00",
                pointColor: "#EEEE55",
                pointStrokeColor: "#fff",
                pointHighlightFill: "#EEEE55",
                pointHighlightStroke: "#EEEE00",
                data: []
            },
            {
                label: "PE",
                fillColor: "#555555",
                strokeColor: "#555555",
                pointColor: "#333333",
                pointStrokeColor: "#fff",
                pointHighlightFill: "#333333",
                pointHighlightStroke: "#555555",
                data: []
            },
        ]
    };

    var config = {
        // String - Template string for single tooltips
        tooltipTemplate: "<%if (label){%><%=label%>: <%}%><%= value %>",

        // String - Template string for single tooltips
        multiTooltipTemplate: "<%if (datasetLabel){%><%=datasetLabel%>: <%}%><%= value %>"
    }

    $.each(dataPercent, function(key, val){
        data.datasets[0].data[data.labels.length] = (val.OK ? val.OK : 0);
        data.datasets[1].data[data.labels.length] = (val.KO ? val.KO : 0);
        data.datasets[2].data[data.labels.length] = (val.FA ? val.FA : 0);
        data.datasets[3].data[data.labels.length] = (val.NA ? val.NA : 0);
        data.datasets[4].data[data.labels.length] = (val.PE ? val.PE : 0);

        data.labels[data.labels.length] = key;

    });

    console.log(data);

    new Chart(ctx).StackedBar(data,config);
   // return data;
}
