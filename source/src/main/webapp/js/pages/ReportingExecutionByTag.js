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

        bindToggleCollapse();

        $("#splitFilter input").click(function () {
            //save the filter preferences in the session storage
            var serial = $("#splitFilter input").serialize();
            var obj = convertSerialToJSONObject(serial);
            sessionStorage.setItem("splitFilter", JSON.stringify(obj));
        });
        
        splitFilterPreferences();

        var urlTag = GetURLParameter('Tag');
        loadTagFilters(urlTag);
        $('body').tooltip({
            selector: '[data-toggle="tooltip"]'
        });
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
    loadCountryFilter();
}

function loadCountryFilter() {
    $.ajax({url: "FindInvariantByID",
        data: {idName: "COUNTRY"},
        async: false,
        dataType: 'json',
        success: function (data) {
            var countryFilter = $("#countryFilter");
            var len = data.length;

            for (var i = 0; i < len; i++) {
                var filter = JSON.parse(sessionStorage.getItem("countryFilter"));
                var cb;

                //Load the filters depenbding on the preferences retrieved from session storage
                if (filter !== null && !filter.hasOwnProperty(data[i].value)) {
                    cb = '<label class="checkbox-inline">\n\
                        <input type="checkbox" name="' + data[i].value + '"/>\n\
                        ' + data[i].value + '</label>';
                } else {
                    cb = '<label class="checkbox-inline">\n\
                        <input type="checkbox" name="' + data[i].value + '" checked/>\n\
                        ' + data[i].value + '</label>';
                }
                countryFilter.append(cb);
            }
            $("#countryFilter input").on("click", function () {
                //save the filter preferences in the session storage
                var serial = $("#countryFilter input").serialize();
                var obj = convertSerialToJSONObject(serial);
                sessionStorage.setItem("countryFilter", JSON.stringify(obj));
            });
        }
    });
}

function splitFilterPreferences() {
    var filter = JSON.parse(sessionStorage.getItem("splitFilter"));
    
    if (filter !== null) {
        $("#splitFilter input").each(function () {
            if (filter.hasOwnProperty($(this).prop("name"))) {
                $(this).prop("checked", true);
            } else {
                $(this).prop("checked", false);
            }
        });
    }
}

function displayPageLabel(doc) {
    $("#pageTitle").html(doc.getDocLabel("page_reportbytag", "title"));
    $("#title").html(doc.getDocOnline("page_reportbytag", "title"));
    $("#loadbutton").html(doc.getDocLabel("page_reportbytag", "button_load"));
    $("#reloadbutton").html(doc.getDocLabel("page_reportbytag", "button_reload"));
    $("#filters").html(doc.getDocOnline("page_reportbytag", "filters"));
    $("#reportStatus").html(doc.getDocOnline("page_reportbytag", "report_status"));
    $("#reportFunction").html(doc.getDocOnline("page_reportbytag", "report_function"));
    $("#envCountryBrowser").html(doc.getDocOnline("page_reportbytag", "report_envcountrybrowser"));
    $("#List").html(doc.getDocOnline("page_reportbytag", "report_list"));
    $("#statusLabel").html(doc.getDocLabel("testcase", "Status") + " :");
}

function loadTagFilters(urlTag) {
    var jqxhr = $.get("ReadTag", "", "json");
    $.when(jqxhr).then(function (data) {
        var messageType = getAlertType(data.messageType);
        if (messageType === "success") {
            var index;
            var len = data.contentTable.length;

            $('#selectTag').append($('<option></option>').attr("value", "")).attr("placeholder", "Select a Tag");
            for (index = 0; index < len; index++) {
                //the character " needs a special encoding in order to avoid breaking the string that creates the html element   
                var encodedString = data.contentTable[index].replace(/\"/g, "%22");
                var option = $('<option></option>').attr("value", encodedString).text(data.contentTable[index]);
                $('#selectTag').append(option);
            }

            //if the tag is passed as a url parameter, then it loads the report from this tag
            if (urlTag !== null) {
                $('#selectTag option[value="' + urlTag + '"]').attr("selected", "selected");
                loadReport();
            }
        } else {
            showMessageMainPage(messageType, data.message);
        }
    }).fail(handleErrorAjaxAfterTimeout);
}

function loadReport() {
    var selectTag = $("#selectTag option:selected").text();

    window.history.pushState('Tag', '', 'ReportingExecutionByTag.jsp?Tag=' + encodeURIComponent(selectTag));

    //clear the old report content before reloading it
    $("#ReportByStatusTable").empty();
    $("#statusChart").empty();
    $("#functionChart").empty();
    $("#progressEnvCountryBrowser").empty();
    if ($("#listTable_wrapper").hasClass("initialized")) {
        $("#tableArea").empty();
        $("#tableArea").html('<table id="listTable" class="table table-hover display" name="listTable">\n\
                                            </table><div class="marginBottom20"></div>');
    }
    if (selectTag !== "") {
        //handle the test case execution list display
        loadEnvCountryBrowserReport();
        loadReportList();
        //Retrieve data for charts and draw them
        var jqxhr = $.get("GetReportData", {CampaignName: "null", Tag: selectTag}, "json");
        $.when(jqxhr).then(function (data) {
            loadReportByStatusTable(data);
            loadReportByFunctionChart(data);
        });
    }
}

function generateBarTooltip(data, statusOrder) {
    var htmlRes = "";
    var len = statusOrder.length;

    for (var index = 0; index < len; index++) {
        var status = statusOrder[index];

        if (data.total.hasOwnProperty(status)) {
            htmlRes += "<div>\n\
                        <span class='color-box status" + status + "'></span>\n\
                        <strong> " + status + " : </strong>" + data.total[status] + "</div>";
        }
    }
    htmlRes += '</div>';
    return htmlRes;
}

function buildBar(tag, obj) {
    $.ajax({
        type: "GET",
        url: "GetReportData",
        data: {barData: true, Tag: tag, env: obj.env, country: obj.country, browser: obj.browser, app: obj.application},
        async: true,
        dataType: 'json',
        success: function (data) {
            $("#totalExec").html(data.totalReport);
            var buildBar;
            var statusOrder = ["OK", "KO", "FA", "NA", "NE", "PE", "CA"];
            var len = statusOrder.length;
            var key = obj.env + " " + obj.country + " " + obj.browser + " " + obj.application;
            var tooltip = generateBarTooltip(data.contentTable, statusOrder);

            buildBar = '<div>' + key + '<div class="pull-right" style="display: inline;">Total executions : ' + data.contentTable.totalReport + '</div>\n\
                                                        </div><div class="progress" data-toggle="tooltip" data-html="true" title="' + tooltip + '">';

            for (var i = 0; i < len; i++) {
                var status = statusOrder[i];
                if (data.contentTable.total[status] !== 0) {
                    var percent = (data.contentTable.total[status] / data.contentTable.totalReport) * 100;
                    var roundPercent = Math.round(percent * 10) / 10;

                    buildBar += '<div class="progress-bar status' + status + '" \n\
                                    role="progressbar" \n\
                                    aria-valuenow="60" \n\
                                    aria-value="0" \n\
                                    aria-valuemax="100" \n\
                                    style="width:' + percent + '%;">' + roundPercent + '%</div>';
                }
                $('#EnvCountryBrowser' + status + ' .value').html(data.contentTable.total[status]);
            }
            buildBar += '</div>';
            $("#progressEnvCountryBrowser").append(buildBar);
        },
        error: function () {
            showUnexpectedError();
        }
    });
}

function loadEnvCountryBrowserReport() {
    var tag = GetURLParameter('Tag');
    $("#progressEnvCountryBrowser").empty();
    var params = convertSerialToJSONObject($("#splitFilter input").serialize());

    //turns checkbox status too boolean
    if (params.env === "on") {
        params.env = true;
    }
    if (params.country === "on") {
        params.country = true;
    }
    if (params.browser === "on") {
        params.browser = true;
    }
    if (params.app === "on") {
        params.app = true;
    }

    $.ajax({
        type: "GET",
        url: "GetReportData",
        data: {split: true, Tag: tag, env: params.env, country: params.country, browser: params.browser, app: params.app},
        async: true,
        dataType: 'json',
        success: function (json) {
            var len = json.contentTable.length;
            for (var index = 0; index < len; index++) {
                //draw a progress bar for each combo retrieved
                buildBar(tag, json.contentTable[index]);
            }
        },
        error: function () {
            showUnexpectedError();
        }
    });
}

function loadReportList() {
    var selectTag = $("#selectTag option:selected").text();
    var statusFilter = $("#statusFilter input");
    var countryFilter = $("#countryFilter input");

    if ($("#listTable_wrapper").hasClass("initialized")) {
        $("#tableArea").empty();
        $("#tableArea").html('<table id="listTable" class="table table-hover display" name="listTable">\n\
                                            </table><div class="marginBottom20"></div>');
    }

    if (selectTag !== "") {
        //configure and create the dataTable
        var jqxhr = $.getJSON("ReadTestCaseExecution", "Tag=" + encodeURIComponent(selectTag) + "&" + statusFilter.serialize() + "&" + countryFilter.serialize());
        $.when(jqxhr).then(function (data) {
            var request = "ReadTestCaseExecution?Tag=" + encodeURIComponent(selectTag) + "&" + statusFilter.serialize() + "&" + countryFilter.serialize();

            var config = new TableConfigurationsServerSide("listTable", request, "testList", aoColumnsFunc(data.Columns));
            customConfig(config);

            var table = createDataTable(config, createShortDescRow);

            $('#listTable_wrapper').not('.initialized').addClass('initialized');

        });
    }
}

/*
 * Status panels
 */

function appendPanelStatus(status, total) {
    var rowClass = getRowClass(status);
    $("#ReportByStatusTable").append(
            $("<div class='panel " + rowClass.panel + "'></div>").append(
            $('<div class="panel-heading"></div>').append(
            $('<div class="row"></div>').append(
            $('<div class="col-xs-6 status"></div>').text(status).prepend(
            $('<span class="' + rowClass.glyph + '" style="margin-right: 5px;"></span>'))).append(
            $('<div class="col-xs-6 text-right"></div>').append(
            $('<div class="total"></div>').text(total[status].value)))).append(
            $('<div class="row"></div>').append(
            $('<div class="percentage pull-right"></div>').text('Percentage : ' + Math.round(((total[status].value / total.test) * 100) * 100) / 100 + '%')))));
}

function loadReportByStatusTable(data) {
    var total = {};
    var len = data.axis.length;

    //calculate totaltest nb
    total["test"] = 0;
    for (var index = 0; index < len; index++) {
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

    // create a panel for each control status
    for (var label in total) {
        if (label !== "test") {
            appendPanelStatus(label, total);
        }
    }
// add a panel for the total
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

    var dataset = [];
    for (var label in total) {
        if (label !== "test") {
            dataset.push(total[label]);
        }
    }
    loadReportByStatusChart(dataset);
}

/*
 * Charts functions
 */

function loadReportByStatusChart(data) {

    var margin = {top: 20, right: 25, bottom: 20, left: 50};

    var offsetW = document.getElementById('statusChart').offsetWidth;
    if (offsetW === 0) {
        offsetW = 300;
    }
    var offsetH = document.getElementById('ReportByStatusTable').offsetHeight;
    if (offsetH === 0) {
        offsetH = 300;
    }

    var width = offsetW - margin.left - margin.right;
    var height = offsetH - margin.top - margin.bottom;
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

function convertData(dataset) {
    var data = [];

    for (var i in dataset)
        data.push(dataset[i]);
    return data;
}

function loadReportByFunctionChart(dataset) {
    var data = convertData(dataset.axis);

    var margin = {top: 20, right: 20, bottom: 200, left: 150},
    width = 1200 - margin.left - margin.right,
            height = 600 - margin.top - margin.bottom;

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
                var len = d.chartData.length;

                for (var index = 0; index < len; index++) {
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
            .call(wrap, 200)
            .style({"text-anchor": "end"})
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

/*
 * Helper functions
 */

function createShortDescRow(row, data, index) {
    var tableAPI = $("#listTable").DataTable();
    var createdRow = tableAPI.row(row);
    var rowClass = "";

    if (index % 2 === 0) {
        rowClass = "odd printBorder";
    } else {
        rowClass = "even printBorder";
    }

    createdRow.child(data.shortDesc);
    $(row).children('.center').attr('rowspan', '2');
    $(createdRow.child()).attr('class', rowClass);
    $(createdRow.child()).children('td').attr('colspan', '3').attr('class', 'shortDesc');
    createdRow.child.show();
}

function generateTooltip(data) {
    var htmlRes;

    htmlRes = '<div><span class=\'bold\'>Test ID :</span> ' + data.ID + '</div>' +
            '<div><span class=\'bold\'>Country : </span>' + data.Country + '</div>' +
            '<div><span class=\'bold\'>Environment : </span>' + data.Environment + '</div>' +
            '<div><span class=\'bold\'>Browser : </span>' + data.Browser + '</div>' +
            '<div><span class=\'bold\'>Start : </span>' + data.Start + '</div>' +
            '<div><span class=\'bold\'>End : </span>' + data.End + '</div>' +
            '<div>' + data.ControlMessage + '</div>';

    return htmlRes;
}

function aoColumnsFunc(Columns) {
    var doc = new Doc();
    var colLen = Columns.length;
    var nbColumn = colLen + 3;
    var testCaseInfoWidth = (1 / 3) * 30;
    var testExecWidth = (1 / nbColumn) * 70;


    var aoColumns = [
        {
            "data": "test",
            "sName": "test",
            "sWidth": testCaseInfoWidth + "%",
            "title": doc.getDocOnline("test", "Test"),
            "sClass": "bold"
        },
        {
            "data": "testCase",
            "sName": "testCase",
            "sWidth": testCaseInfoWidth + "%",
            "title": doc.getDocOnline("testcase", "TestCase"),
            "mRender": function (data, type, obj, meta) {
                var result = "<a href='./TestCase.jsp?Test=" + encodeURIComponent(obj.test) + "&TestCase=" + encodeURIComponent(obj.testCase) + "&Load=Load'>" + obj.testCase + "</a>";
                return result;
            }
        },
        {
            "data": "application",
            "sName": "application",
            "sWidth": testCaseInfoWidth + "%",
            "title": doc.getDocOnline("application", "Application")
        }
    ];
    for (var i = 0; i < colLen; i++) {
        var title = Columns[i].environment + " " + Columns[i].country + " " + Columns[i].browser;

        var col = {
            "title": title,
            "bSortable": false,
            "bSearchable": false,
            "sWidth": testExecWidth + "%",
            "data": function (row, type, val, meta) {
                var dataTitle = meta.settings.aoColumns[meta.col].sTitle;
                if (row.hasOwnProperty("execTab") && row["execTab"].hasOwnProperty(dataTitle)) {
                    return row["execTab"][dataTitle];
                } else {
                    return "";
                }
            },
            "sClass": "center",
            "mRender": function (data) {
                if (data !== "") {
                    var executionLink = generateExecutionLink(data.ControlStatus, data.ID);
                    var glyphClass = getRowClass(data.ControlStatus);
                    var tooltip = generateTooltip(data);
                    var cell = '<div class="progress-bar status' + data.ControlStatus + '" \n\
                                role="progressbar" aria-valuenow="100" aria-valuemin="0" aria-valuemax="100" style="width: 100%;cursor: pointer; height: 40px;" \n\
                                data-toggle="tooltip" data-html="true" title="' + tooltip + '"\n\
                                onclick="window.open(\'' + executionLink + '\')">\n\
                                <span class="' + glyphClass.glyph + ' marginRight5"></span>\n\
                                 <span>' + data.ControlStatus + '<span></div>';
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

function customConfig(config) {
    var doc = new Doc();
    var customColvisConfig = {"buttonText": doc.getDocLabel("dataTable", "colVis"),
        "exclude": [0, 1, 2],
        "stateChange": function (iColumn, bVisible) {
            $('.shortDesc').each(function () {
                $(this).attr('colspan', '3');
            });
        }
    };

    config.paginate = false;
    config.lang.colVis = customColvisConfig;
    config.orderClasses = false;
    config.bDeferRender = true;
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
    } else if (status === "NA") {
        rowClass["glyph"] = "fa fa-question";
    } else {
        rowClass["glyph"] = "";
    }
    return rowClass;
}

function generateExecutionLink(status, id) {
    var result = "";
    if (status === "NE") {
        result = "./RunTests.jsp?queuedExecution=" + id;
    } else {
        result = "./ExecutionDetail.jsp?id_tc=" + id;
    }
    return result;
}

function wrap(text, width) {
    text.each(function () {
        var text = d3.select(this),
                words = text.text().split(/\s+/).reverse(),
                word,
                line = [],
                lineNumber = 0,
                lineHeight = 1.1, // ems
                y = text.attr("y"),
                dy = parseFloat(text.attr("dy")),
                tspan = text.text(null).append("tspan").attr("x", 0).attr("y", y).attr("dy", dy + "em");
        while (word = words.pop()) {
            line.push(word);
            tspan.text(line.join(" "));
            if (tspan.node().getComputedTextLength() > width) {
                line.pop();
                tspan.text(line.join(" "));
                line = [word];
                tspan = text.append("tspan").attr("x", 0).attr("y", y).attr("dy", ++lineNumber * lineHeight + dy + "em").text(word);
            }
        }
    });
}
