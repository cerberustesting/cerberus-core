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

var defaultConfig = {
    // Boolean - Determines whether to draw tooltips on the canvas or not
    showTooltips: true,
    // Boolean used to remove animation during graphic creation
    animation: false,
    //String - Colour of the grid lines
    scaleGridLineColor: "rgba(0,0,0,0.2)",
    //String - A legend template
    legendTemplate: "<ul class=\"<%=name.toLowerCase()%>-legend\"><% for (var i=0; i<segments.length; i++){%><li><span style=\"display:inline-block;width:30px;height:30px;margin:5px;background-color:<%=segments[i].fillColor%>\"><%if(segments[i].label){%><%=segments[i].label%><%}%></span></li><%}%></ul>"
};

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

    if(testcase.ID > 0) {
        var statusExecutionLink = executionLink.clone();
        statusExecutionLink.attr('href', statusExecutionLink.attr('href') + testcase.ID);
        statusExecutionLink.text(testcase.ID);
        statusTestCaseStatusLine.find(".ID").append(statusExecutionLink);
    }

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

function createGraphFromDataToElement(data,element, config) {
    if(!element || !data || !data.type || !data.axis || !data.axis.length > 0) {
        return false;
    }

    if(!config) {
        config = {
            // Boolean - Determines whether to draw tooltips on the canvas or not
            showTooltips: true,

            // Boolean used to remove animation during graphic creation
            animation: false,

            //String - Colour of the grid lines
            scaleGridLineColor: "rgba(0,0,0,0.2)",

            // String - Template string for single tooltips
            tooltipTemplate: "<%if (label){%><%=label%>: <%}%><%= value %>",

            // String - Template string for single tooltips
            multiTooltipTemplate: "<%if (datasetLabel){%><%=datasetLabel%>: <%}%><%= value %>"
        };
    }

    var dataset = false, isOk = false;
    for(var axis=0; axis<data.axis.length; axis++) {
        if(axis == 0) {
            if (data.type == "Donut" || data.type == "Pie" || data.type == "Bar") {
                dataset = [];
            } else if(data.type == "MultiBar" || data.type == "Radar") {
                dataset = {
                    labels: data.labels,
                    datasets: []
                };
            }
        }

        if(data.type == "MultiBar" || data.type == "Radar") {
            dataset.datasets[dataset.datasets.length] = createDatasetMultiBar(data.axis[axis].label, data.axis[axis].data, data.axis[axis].fillColor, 
                data.axis[axis].pointColor, data.axis[axis].pointHighlight);

        } else if(data.type == "BarColor") {
            dataset = createDatasetBar(data.axis[axis].label, data.axis[axis].value, data.axis[axis].color, 
                data.axis[axis].highlight, dataset);

        } else {
            dataset[dataset.length] = createDatasetPie(data.axis[axis].label, data.axis[axis].value, 
                data.axis[axis].color, data.axis[axis].highlight);
        }
        isOk = true;
    }

    if(isOk) {
        var ctx = $(element).get(0).getContext("2d");

        if(data.type == "Pie") {
            return new Chart(ctx).Pie(dataset,config);

        } else if(data.type == "Donut") {
            return new Chart(ctx).Donut(dataset, config);

        } else if(data.type == "Bar") {
            return new Chart(ctx).Bar(dataset, config);

        } else if(data.type == "BarColor") {
            return new Chart(ctx).BarColor(dataset, config);

        } else if(data.type == "Radar") {
            return new Chart(ctx).Radar(dataset, config);

        } else if(data.type == "MultiBar") {
            return new Chart(ctx).StackedBar(dataset,config);

        }
    }
};

function createGraphFromAjaxToElement(ajaxDataGraphURL,element, config) {
    if(!ajaxDataGraphURL || !element) {
        return false;
    }

    jQuery.ajax(ajaxDataGraphURL).done(function(data) {
        createGraphFromDataToElement(data,element, config);
    });
}

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
