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

var data;
data = createDatasetBar("OK",0,"#00EE00","#33DD33",false);
data = createDatasetBar("KO",0,"#F7464A","#FF5A5E",data);
data = createDatasetBar("FA",0,"#FDB45C","#FFC870",data);
data = createDatasetBar("NA",0,"#EEEE00","#EEEE55",data);
data = createDatasetBar("PE",0,"#0000DD","#5555DD",data);



var dataDonut = [
    createDatasetPie("OK",0,"#00EE00","#33DD33"),
    createDatasetPie("KO",0,"#F7464A","#FF5A5E"),
    createDatasetPie("FA",0,"#FDB45C","#FFC870"),
    createDatasetPie("NA",0,"#EEEE00","#EEEE55"),
    createDatasetPie("PE",0,"#555555","#333333")
];

var dataPercent = {};

var dataPercentLabels = {};

var dataFunction = createDatasetBar("My Second dataset",0,"rgba(151,187,205,0.5)","rgba(151,187,205,1)",false);

var testCaseStatusLine = $("<tr class='testcase'>" +
        "<td class='ID'></td>" +
        "<td class='Function'></td>" +
        "<td class='Test'></td>" +
        "<td class='TestCase'></td>" +
        "<td class='ShortDescription wrapAll'></td>" +
        "<td class='Control'></td>" +
        "<td class='Status'></td>" +
        "<td class='Application'></td>" +
        "<td class='BugID'></td>" +
        "<td class='Comment'></td>" +
        "<td class='Start'></td>" +
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

    var testCaseFunction = testcase.Function || "(function not defined)";
    statusTestCaseStatusLine.find(".Function").text(testCaseFunction);

    statusTestCaseStatusLine.find(".Control").text(testcase.ControlStatus);
    statusTestCaseStatusLine.find(".Status").text(testcase.Status);
    statusTestCaseStatusLine.find(".BugID").append(testcase.BugID);
    statusTestCaseStatusLine.find(".Application").text(testcase.Application);
    statusTestCaseStatusLine.find(".Comment").text(testcase.Comment);
    
    var date = new Date();
    date.setTime(testcase.Start);
    statusTestCaseStatusLine.find(".Start").text(date.toLocaleString());

    statusTestCaseStatusLine.find(".ShortDescription").append(testcase.ShortDescription);

    if (statusTable.find("tr").length % 2) {
        statusTestCaseStatusLine.addClass("odd");
    } else {
        statusTestCaseStatusLine.addClass("even");
    }
    
    statusTable.append(statusTestCaseStatusLine);
};

function createGraphFromAjaxToElement(ajaxDataGraphURL,element, config) {
    if(!ajaxDataGraphURL || !element) {
        return false;
    }

    if(!config) {
        config = {
            // String - Template string for single tooltips
            tooltipTemplate: "<%if (label){%><%=label%>: <%}%><%= value %>",

            // String - Template string for single tooltips
            multiTooltipTemplate: "<%if (datasetLabel){%><%=datasetLabel%>: <%}%><%= value %>"
        };
    }

    jQuery.ajax(ajaxDataGraphURL).done(function(data) {
        if(!data || !data.type || !data.axis || !data.axis.length <= 0) {
            return false;
        }

        var dataset = false;
        for(var axis=0; axis<data.axis.length; axis++) {
            if(axis == 0 && (data.type == "Pie" || data.type == "Bar" || data.type == "MultiBar") ) {
                dataset = [];
            }

            if(data.type == "Pie" || data.type == "Bar") {
                dataset[dataset.length] = createDatasetPie(data.axis[0].label, data.axis[0].value, 
                    data.axis[0].color, data.axis[0].highlight);

            } else if(data.type == "BarColors") {
                dataset = createDatasetBar(data.axis[0].label, data.axis[0].value, data.axis[0].color, 
                    data.axis[0].highlight, dataset);

            } else if(data.type == "MultiBar") {
                createDatasetMultiBar(data.axis[0].label, data.axis[0].data, data.axis[0].fillColor, 
                    data.axis[0].pointColor, data.axis[0].pointHighlight);
            }
        }
        
        if(!dataset) {
            var ctx = $(element).get(0).getContext("2d");

            if(data.type == "Pie") {
                new Chart(ctx).Pie(dataset,config);

            } else if(data.type == "Bar") {
                new Chart(ctx).Bar(dataset, config);

            } else if(data.type == "BarColors") {
                new Chart(ctx).BarColors(dataset, config);

            } else if(data.type == "MultiBar") {
                new Chart(ctx).StackedBar(dataset,config);

            }
        }
    });
}

function addTestCaseToPercentRadar(testcase) {
    
    var testCaseFunction = testcase.Function || "(function not defined)";
    
    if(!dataPercent[testCaseFunction]) {
        dataPercent[testCaseFunction] = {
            OK: 0,
            KO: 0,
            FA: 0,
            NA: 0,
            total: 0
        };
    }
    
    dataPercent[testCaseFunction]['total'] = eval(dataPercent[testCaseFunction]['total'] + 1);
    dataPercent[testCaseFunction][testcase.ControlStatus] = eval(dataPercent[testCaseFunction][testcase.ControlStatus] + 1);
};

function createDatasetBar(label, value, color, highlight, dataset) {
    
    if(!dataset) {
        dataset = {
            labels: [],
            datasets: [
                {
                    label: "",
                    data: [],
                    fillColors: [],
                    strokeColors: [],
                    highlightFills: [],
                    highlightStrokes: []
                }
            ]
        };
    }
    
    var index = dataset.labels.length;
    dataset.labels[index] = label;
    
    dataset.datasets[0].data[index] = value;
    dataset.datasets[0].fillColors[index] = color;
    dataset.datasets[0].strokeColors[index] = color;
    dataset.datasets[0].highlightFills[index] = highlight;
    dataset.datasets[0].highlightStrokes[index] = highlight;
    
    
    return dataset;
};

function createDatasetPie(label, value, color, highlight) {
    var dataset =     {
        value: value,
        color: color,
        highlight: highlight,
        label: label
    };
    return dataset;
};

function createDatasetMultiBar(label, data, fillColor, pointColor, pointHighlight) {
    var dataset = {
                label: label,
                fillColor: fillColor,
                strokeColor: fillColor,
                pointColor: pointColor,
                pointStrokeColor: pointColor,
                pointHighlightFill: pointHighlight,
                pointHighlightStroke: pointHighlight,
                data: data
            };
    return dataset;
};

function computePercentDataRadar(ctx) {
    var config = {
        // String - Template string for single tooltips
        tooltipTemplate: "<%if (label){%><%=label%>: <%}%><%= value %>",

        // String - Template string for single tooltips
        multiTooltipTemplate: "<%if (datasetLabel){%><%=datasetLabel%>: <%}%><%= value %>"
    }

    var dataBar = {
        OK: [],
        KO: [],
        FA: [],
        NA: [],
        PE: [],
        labels: []
    };

    $.each(dataPercent, function(key, val){
        dataBar.OK[dataBar.labels.length] = (val.OK ? val.OK : 0);
        dataBar.KO[dataBar.labels.length] = (val.KO ? val.KO : 0);
        dataBar.FA[dataBar.labels.length] = (val.FA ? val.FA : 0);
        dataBar.NA[dataBar.labels.length] = (val.NA ? val.NA : 0);
        dataBar.PE[dataBar.labels.length] = (val.PE ? val.PE : 0);

        dataBar.labels[dataBar.labels.length] = key;

    });
    
    var data = {
        labels: dataBar.labels,
        datasets: [ 
            createDatasetMultiBar("OK",dataBar.OK,"#00EE00","#33DD33","#33FF55"),
            createDatasetMultiBar("KO",dataBar.KO,"#F7464A","#FF5A5E","#FF7A7E"),
            createDatasetMultiBar("FA",dataBar.FA,"#FDB45C","#FFC870","#FFE890"),
            createDatasetMultiBar("NA",dataBar.NA,"#EEEE00","#EEEE55","#EEFE65"),
            createDatasetMultiBar("PE",dataBar.PE,"#555555","#333333","#33F3F3")
        ]
    };

    //console.log(data);

    new Chart(ctx).StackedBar(data,config);
   // return data;
}
