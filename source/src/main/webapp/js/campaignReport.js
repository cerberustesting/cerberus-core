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


var testCaseStatusLine = $("<tr class='testcase'>" +
        "<td class='ID'></td>" +
        "<td class='Test'></td>" +
        "<td class='TestCase'></td>" +
        "<td class='Control'></td>" +
        "<td class='Status'></td>" +
        "<td class='TestBattery'></td>" +
        "<td class='BugID'></td>" +
        "<td class='Comment'></td>" +
        "</tr>");

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

function addTestCaseToStatusTabs(testcase) {
    var statusTable = $("#Status" + testcase.ControlStatus + " tbody");
    statusTestCaseStatusLine = testCaseStatusLine.clone();
    statusTestCaseStatusLine.find(".ID").text(testcase.ID);
    statusTestCaseStatusLine.find(".Test").text(testcase.Test);
    statusTestCaseStatusLine.find(".TestCase").text(testcase.TestCase);
    statusTestCaseStatusLine.find(".Control").text(testcase.ControlStatus);
    statusTestCaseStatusLine.find(".Status").text(testcase.Status);
    statusTestCaseStatusLine.find(".ID").text(testcase.ID);
    statusTestCaseStatusLine.find(".BugID").text(testcase.BugID);
    statusTestCaseStatusLine.find(".Comment").text(testcase.Comment);

    console.log(statusTable.find("tr"));

    if (statusTable.find("tr").length % 2) {
        statusTestCaseStatusLine.addClass("odd");
    } else {
        statusTestCaseStatusLine.addClass("even");
    }

    statusTable.append(statusTestCaseStatusLine);
}

