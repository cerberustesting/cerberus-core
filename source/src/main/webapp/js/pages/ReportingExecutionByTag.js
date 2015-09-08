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
    var doc = new Doc();

    displayHeaderLabel(doc);
    displayFooter(doc);
}

function aoColumnsFunc(Columns) {
    var aoColumns = [
        {"data": "test",
            "sName": "test",
            "title": "test"},
        {"data": "testCase",
            "sName": "testCase",
            "title": "testCase"
        },
        {"data": "application",
            "sName": "application",
            "title": "application"
        },
        {"data": "status",
            "sName": "status",
            "title": "status"
        },
        {"data": "shortDesc",
            "sName": "description",
            "title": "description"
        },
        {"data": "bugId",
            "sName": "bugId",
            "title": "bugId"
        },
        {"data": "function",
            "sName": "function",
            "title": "function"
        }
    ];
    for (var i = 0; i < Columns.length; i++) {
        var title = Columns[i].environment + " " + Columns[i].country + " " + Columns[i].browser;

        var col = {"title": title,
//            "bSortable": false,
//            "bSearchable": false,
            "data": function (row, type, val, meta) {
                var dataTitle = meta.settings.aoColumns[meta.col].sTitle;
                if (row.hasOwnProperty("execTab") && row["execTab"].hasOwnProperty(dataTitle)) {
                    return row["execTab"][dataTitle].ControlStatus;
                } else {
                    return "";
                }
            },
            "sClass": "center",
            "sName": "testCase",
            "mRender": function (data, type, obj) {
                if (data !== "") {
                    var glyphClass = getRowClass(data);
                    var cell = '<div class="progress-bar status' + data + '" role="progressbar" aria-valuenow="100" aria-valuemin="0" aria-valuemax="100" style="width: 100%;">\n\
                            <span class="' + glyphClass.glyph + ' marginRight5"></span>' + data + '</div>';
                    return cell;
                } else {
                    return data;
                }
            }
        };
        aoColumns.push(col);
    }
    return aoColumns;
}

function loadTagFilters() {
    var jqxhr = $.get("ReadTestCaseExecution", "", "json");
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
    var rowClass = [];

    rowClass["panel"] = "panel" + status;
    if (status === "OK") {
        rowClass["glyph"] = "glyphicon glyphicon-ok";
    } else if (status === "KO") {
        rowClass["glyph"] = "glyphicon glyphicon-remove";
    } else if (status === "FA") {
        rowClass["glyph"] = "fa fa-bug";
    } else if (status === "CA") {
        rowClass["glyph"] = "fa fa-life-ring";
    } else if (status === "PE") {
        rowClass["glyph"] = "fa fa-hourglass-half";
    } else if (status === "NE") {
        rowClass["glyph"] = "fa fa-clock-o";
    } else {
        rowClass["glyph"] = "";
    }
    return rowClass;
}

function loadReport() {
    var selectTag = $("#selectTag option:selected").text();
    var statusFilter = $("#statusFilter input");
    //clear the old report content before reloading it
    $("#ReportByStatusTable").empty();
    $("#statusChart").empty();
    $("#functionChart").empty();
    if ($("#listTable_wrapper").hasClass("initialized")) {
        $("#ListPanel .panel-body").empty();
        $("#ListPanel .panel-body").html('<table id="listTable" class="table table-hover display" name="listTable">\n\
                                            </table><div class="marginBottom20"></div>');
    }
    if (selectTag !== "") {
        //configure and create the dataTable
//        var jqxhr = $.getJSON("ReadTestCaseExecution", "Tag=" + selectTag + "&" + statusFilter.serialize());
//        $.when(jqxhr).then(function (data) {
//            var configurations = new TableConfigurationsServerSide("listTable", "ReadTestCaseExecution?Tag=" + selectTag + "&" + statusFilter.serialize(), "testList", aoColumnsFunc(data.Columns));
//
//            createDataTable(configurations);
//            $('#listTable_wrapper').not('.initialized').addClass('initialized');
//        });


        var jqxhr = $.get("GetReportData", {CampaignName: "null", Tag: selectTag}, "json");
        $.when(jqxhr).then(function (data) {
            loadReportByStatusTable(data);
            loadReportByFunctionChart(data);
        });
    }
}

function convertData(dataset) {
    var data = [];

    for (var i in dataset)
        data.push(dataset[i]);
    return data;
}

function loadReportByFunctionChart(dataset) {
    var data = convertData(dataset.axis);

    var margin = {top: 20, right: 20, bottom: 100, left: 150},
    width = 1200 - margin.left - margin.right,
            height = 500 - margin.top - margin.bottom;

    var x = d3.scale.ordinal()
            .rangeRoundBands([0, width], .1);

    var y = d3.scale.linear()
            .rangeRound([height, 0]);

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
                var res = "<strong>Function :</strong> <span style='color:red'>" + d.name + "</span>";
                for (var index = 0; index < d.chartData.length; index++) {
                    res = res + "<div><div class='color-box' style='background-color:" + d.chartData[index].color + " ;'>\n\
                    </div>" + d.chartData[index].name + " : " + d[d.chartData[index].name].value + "</div>";
                }
                return res;
            });

    var svg = d3.select("#functionChart").append("svg")
            .attr("width", width + margin.left + margin.right)
            .attr("height", height + margin.top + margin.bottom)
            .append("g")
            .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

    svg.call(tip);


    data.forEach(function (d) {
        var y0 = 0;
        d.chartData = [];
        for (var status in d) {
            if (status !== "name" && status !== "chartData") {
                d.chartData.push({name: status, y0: y0, y1: y0 += +d[status].value, color: d[status].color});
            }
        }
        d.totalTests = d.chartData[d.chartData.length - 1].y1;
    });

    x.domain(data.map(function (d) {
        return d.name;
    }));
    y.domain([0, d3.max(data, function (d) {
            return d.totalTests;
        })]);

    svg.append("g")
            .attr("class", "x axis")
            .attr("transform", "translate(0," + height + ")")
            .call(xAxis)
            .selectAll("text")
            .style("text-anchor", "end")
            .attr("dx", "-.8em")
            .attr("dy", "-.55em")
            .attr("transform", "rotate(-75)");

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
                return d.chartData;
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

    var margin = {top: 20, right: 25, bottom: 20, left: 50};
    var width = document.getElementById('statusChart').offsetWidth - margin.left - margin.right;
    var height = document.getElementById('ReportByStatusTable').offsetHeight - margin.top - margin.bottom;
    var radius = Math.min(width, height) / 2;

    var svg = d3.select('#statusChart')
            .append('svg')
            .attr('width', width + margin.left + margin.right)
            .attr('height', height + margin.top + margin.bottom)
            .append('g')
            .attr('transform', 'translate(' + (width / 2) + ',' + (height / 2) + ')')

    var arc = d3.svg.arc()
            .outerRadius(radius);

    var pie = d3.layout.pie()
            .value(function (d) {
                return d.value;
            })
            .sort(null);

    var path = svg.selectAll('path')
            .data(pie(data))
            .enter()
            .append('path')
            .attr('d', arc)
            .attr('fill', function (d, i) {
                return d.data.color;
            });
}

function appendPanelStatus(axis, status, total) {
    var rowClass = getRowClass(status);
    $("#ReportByStatusTable").append(
            $("<div class='panel " + rowClass.panel + "'></div>").append(
            $('<div class="panel-heading"></div>').append(
            $('<div class="row"></div>').append(
            $('<div class="col-xs-6 status"></div>').text(status).prepend(
            $('<span class="' + rowClass.glyph + '" style="margin-right: 5px;"></span>'))).append(
            $('<div class="col-xs-6 text-right"></div>').append(
            $('<div class="total"></div>').text(total[status].value)).append(
            $('<div></div>').text('Percentage : ' + Math.round(((total[status].value / total.test) * 100) * 100) / 100 + '%'))))));
}

function createStatusFilter(total) {
    var filterContainer = $("#filterContainer");

    filterContainer.append($('<label>Status :</label>\n\
                            <div class="form-group" id="statusFilter"></div>'));
    for (var label in total) {
        if (label !== "test") {
            $("#statusFilter").append('<label class="checkbox-inline">\n\
                                        <input type="checkbox" name=' + label + ' checked/>'
                                        + label + '</label>');
        }
    }
}

function loadReportByStatusTable(data) {
    var total = {};
    //calculate totaltest nb
    total["test"] = 0;
    for (var index = 0; index < data.axis.length; index++) {
        // increase the total execution
        for (var key in data.axis[index]) {
            if (key !== "name") {
                if (total.hasOwnProperty(key)) {
                    total[key].value += data.axis[index][key].value;
                } else {
                    total[key] = {"value": data.axis[index][key].value,
                        "color": data.axis[index][key].color};
                }
                total.test += data.axis[index][key].value;
            }
        }
    }

    // create each line of the table
    for (var label in total) {
        if (label !== "test") {
            appendPanelStatus(data.axis[index], label, total);
        }
    }
// add a line for the total
    $("#ReportByStatusTable").append(
            $("<div class='panel panel-primary'></div>").append(
            $('<div class="panel-heading"></div>').append(
            $('<div class="row"></div>').append(
            $('<div class="col-xs-6 status"></div>').text("Total").prepend(
            $('<span class="" style="margin-right: 5px;"></span>'))).append(
            $('<div class="col-xs-6 text-right"></div>').append(
            $('<div class="total"></div>').text(total.test))
            ))));
    //format data to be used by the chart
    createStatusFilter(total);
    var dataset = [];
    for (var label in total) {
        if (label !== "test") {
            dataset.push(total[label]);
        }
    }
    loadReportByStatusChart(dataset);
}