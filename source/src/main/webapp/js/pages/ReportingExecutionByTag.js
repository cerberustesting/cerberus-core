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

function loadReportByStatusTable() {
    var selectTag = $("#selectTag option:selected").text();

    //clear the table content before reloading it
    $("#ReportByStatusTable tbody").empty();
    $("#chart").empty();

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