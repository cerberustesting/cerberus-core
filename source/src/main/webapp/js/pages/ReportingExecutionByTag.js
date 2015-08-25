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
/* global handleErrorAjaxAfterTimeout */

$.when($.getScript("js/pages/global/global.js")).then(function () {
    $(document).ready(function () {
        initPage();

        loadTagFilters();
    });
});

function initPage() {
    var doc = getDoc();

    displayHeaderLabel(doc);
    displayFooter(doc);
}

function loadTagFilters() {
    var jqxhr = $.get("ReadTestCaseExecution", "action=1", "json");
    $.when(jqxhr).then(function (data) {
        var messageType = getAlertType(data.messageType);
        if (messageType === "success") {
            var index;
            $('#selectTag').append($('<option></option>').attr("value", "")).attr("placeholder", "Select a Tag");
            for (index = 0; index < data.tags.length; index++) {
//the character " needs a special encoding in order to avoid breaking the string that creates the html element   
                var encodedString = data.tags[index].replace(/\"/g, "%22");
                var option = $('<option></option>').attr("value", encodedString).text(data.tags[index]);
                $('#selectTag').append(option);
            }
        } else {
            showMessageMainPage(messageType, data.message);
        }
    }).fail(handleErrorAjaxAfterTimeout);
}

function getRowClass(status) {
    var rowClass = "status" + status;
    return rowClass;
}

function loadReport() {
    var selectTag = $("#selectTag option:selected").text();

    //clear the old report content before reloading it
    $("#ReportByStatusTable tbody").empty();
    $("#chart").empty();
    $("#functionChart").empty();
    loadReportByStatusTable(selectTag);
    
    var jqxhr = $.get("CampaignExecutionStatusBarGraphByFunction", {CampaignName: "null", Tag: selectTag}, "json");
    $.when(jqxhr).then(function (data) {
        loadReportByFunctionChart(data);
    });
}

function convertData(dataset) {
    var data = [];
    for (var i = 0; i < dataset.labels.length; i++) {
        data.push({name: dataset.labels[i]});
        dataset.axis.forEach(function (column) {
            var columnName = column.label;
            data[i][columnName] = {value: column.data[i], color: column.fillColor};
        });
    }
    return data;
}

function loadReportByFunctionChart(dataset) {
    var data = convertData(dataset);

    var margin = {top: 20, right: 20, bottom: 30, left: 40},
    width = 1200 - margin.left - margin.right,
            height = 500 - margin.top - margin.bottom;

    var x = d3.scale.ordinal()
            .rangeRoundBands([0, width], .1);

    var y = d3.scale.linear()
            .rangeRound([height, 0]);

    var color = d3.scale.ordinal();

    var xAxis = d3.svg.axis()
            .scale(x)
            .orient("bottom");

    var yAxis = d3.svg.axis()
            .scale(y)
            .orient("left");

    var tip = d3.tip()
            .attr('class', 'd3-tip')
            .offset([-10, 0])
            .html(function (d) {
                return "<strong>Function :</strong> <span style='color:red'>" + d.name + "</span>\n\
                        <div><div class='color-box' style='background-color:" + d.OK.color + " ;'></div>OK : " + d.OK.value + "</div>\n\
                        <div><div class='color-box' style='background-color:" + d.KO.color + " ;'></div>KO : " + d.KO.value + "</div>\n\
                        <div><div class='color-box' style='background-color:" + d.CA.color + " ;'></div>CA : " + d.CA.value + "</div>\n\
                        <div><div class='color-box' style='background-color:" + d.FA.color + " ;'></div>FA : " + d.FA.value + "</div>\n\
                        <div><div class='color-box' style='background-color:" + d.NA.color + " ;'></div>NA : " + d.NA.value + "</div>\n\
                        <div><div class='color-box' style='background-color:" + d.NE.color + " ;'></div>NE : " + d.NE.value + "</div>\n\
                        <div><div class='color-box' style='background-color:" + d.PE.color + " ;'></div>PE : " + d.PE.value + "</div>";
            });

    var svg = d3.select("#functionChart").append("svg")
            .attr("width", width + margin.left + margin.right)
            .attr("height", height + margin.top + margin.bottom)
            .append("g")
            .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

    svg.call(tip);

    color.domain(d3.keys(data[0]).filter(function (key) {
        return key !== "name";
    }));

    data.forEach(function (d) {
        var y0 = 0;
        d.test = color.domain().map(function (name) {
            return {name: name, y0: y0, y1: y0 += +d[name].value, color: d[name].color};
        });
        d.total = d.test[d.test.length - 1].y1;
    });

    x.domain(data.map(function (d) {
        return d.name;
    }));
    y.domain([0, d3.max(data, function (d) {
            return d.total;
        })]);

    svg.append("g")
            .attr("class", "x axis")
            .attr("transform", "translate(0," + height + ")")
            .call(xAxis);

    svg.append("g")
            .attr("class", "y axis")
            .call(yAxis)
            .append("text")
            .attr("transform", "rotate(-90)")
            .attr("y", 6)
            .attr("dy", ".71em")
            .style("text-anchor", "end")
            .text("TestCase Number");

    var name = svg.selectAll(".name")
            .data(data)
            .enter().append("g")
            .attr("class", "g")
            .attr("transform", function (d) {
                return "translate(" + x(d.name) + ",0)";
            });

    svg.selectAll(".g")
            .on('mouseover', tip.show)
            .on('mouseout', tip.hide);

    name.selectAll("rect")
            .data(function (d) {
                return d.test;
            })
            .enter().append("rect")
            .attr("width", x.rangeBand())
            .attr("y", function (d) {
                return y(d.y1);
            })
            .attr("height", function (d) {
                return y(d.y0) - y(d.y1);
            })
            .style("fill", function (d) {
                return d.color;
            });
}
;

function loadReportByStatusChart(data) {
    var dataset = data.axis;

    var width = 250;
    var height = 150;
    var radius = Math.min(width, height) / 2;

    var svg = d3.select('#chart')
            .append('svg')
            .attr('width', width)
            .attr('height', height)
            .append('g')
            .attr('transform', 'translate(' + (width / 2) + ',' + (height / 2) + ')');

    var arc = d3.svg.arc()
            .outerRadius(radius);

    var pie = d3.layout.pie()
            .value(function (d) {
                return d.value;
            })
            .sort(null);

    var path = svg.selectAll('path')
            .data(pie(dataset))
            .enter()
            .append('path')
            .attr('d', arc)
            .attr('fill', function (d, i) {
                return d.data.color;
            });
}

function loadReportByStatusTable(selectTag) {
    var jqxhr = $.get("CampaignExecutionGraphByStatus", {CampaignName: "null", Tag: selectTag}, "json");
    $.when(jqxhr).then(function (data) {
        var total = 0;
        // create each line of the table
        for (var index = 0; index < data.labels.length; index++) {
            var rowClass = getRowClass(data.axis[index].label);
            $("#ReportByStatusTable tbody").append(
                    $("<tr></tr>").append(
                    $('<td class=' + rowClass + '></td>').text(data.axis[index].label))
                    .append($("<td></td>").text(data.axis[index].value))
                    );
            // increase the total execution
            total = total + data.axis[index].value;
        }
// add a line for the total
        $("#ReportByStatusTable tbody").append(
                $("<tr></tr>").append(
                $("<th>Total</th>"))
                .append($("<th></th>").text(total))
                );
        loadReportByStatusChart(data);
    }).fail(handleErrorAjaxAfterTimeout);
}