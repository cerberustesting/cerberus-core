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
    });
});

function initPage() {
    displayPageLabel();
    //configure and create the dataTable
    var configurations = new TableConfigurationsServerSide("executionsTable", "ReadExecutionInQueue", "contentTable", aoColumnsFunc(), [1, 'asc']);
    createDataTableWithPermissions(configurations, renderOptionsForApplication, "#executionList");
    
    drawQueueInformation();
}

function displayPageLabel() {
    var doc = new Doc();

    $("#title").html(doc.getDocLabel("page_testcaseexecutionqueue", "allExecution"));

    displayHeaderLabel(doc);

    displayFooter(doc);
    displayGlobalLabel(doc);
}

function renderOptionsForApplication(data) {
    if ($("#blankSpace").length === 0) {
        var contentToAdd = "<div class='marginBottom10' style='height:34px;' id='blankSpace'></div>";
        $("#executionsTable_wrapper div#executionsTable_length").before(contentToAdd);
    }
}

function aoColumnsFunc(tableId) {
    var doc = new Doc();
    var aoColumns = [
        {"data": "id", "sName": "id", "title": doc.getDocLabel("page_testcaseexecutionqueue", "id_col")},
        {"data": "test", "sName": "test", "title": doc.getDocLabel("page_testcaseexecutionqueue", "test_col")},
        {
            "data": "testCase",
            "sName": "testcase",
            "title": doc.getDocLabel("page_testcaseexecutionqueue", "testcase_col")
        },
        {"data": "country", "sName": "country", "title": doc.getDocLabel("page_testcaseexecutionqueue", "country_col")},
        {
            "data": "environment",
            "sName": "environment",
            "title": doc.getDocLabel("page_testcaseexecutionqueue", "environment_col")
        },
        {"data": "browser", "sName": "browser", "title": doc.getDocLabel("page_testcaseexecutionqueue", "browser_col")},
        {"data": "tag", "sName": "tag", "title": doc.getDocLabel("page_testcaseexecutionqueue", "tag_col")},
        {
            "data": "state",
            "sName": "state",
            "title": doc.getDocLabel("page_testcaseexecutionqueue", "state_col")
        }
    ];
    return aoColumns;
}

function drawQueueInformation(){
    
    var jqxhr = $.get("ReadExecutionPools");
    $.when(jqxhr).then(function (data) {
        //var messageType = getAlertType(data.messageType);
        //if (messageType === "success") {
            //redraw the datatable
            for (var inc=0; inc<data.length; inc++){
        generatePie("statusChart", data[inc].name, data[inc].poolSize, data[inc].inExecution, data[inc].remaining);
    }
        //}
        //show message in the main page
        //showMessageMainPage(messageType, data.message);
        //close confirmation window
        //$('#confirmationModal').modal('hide');
    }).fail(handleErrorAjaxAfterTimeout);
}

/**
 * Generate Pie generate a pie chart and append it to the defined element.
 * @param {type} elementid : ID of the div where the pie will be included
 * @param {type} name : Name of the queue
 * @param {type} poolSize : Size of the Pool
 * @param {type} inExecution : Number of current execution
 * @param {type} remaining : Number remaining executions in queue
 * @returns {undefined}
 */
function generatePie(elementid, name, poolSize, inExecution, remaining) {

    /**
     * Generate data object which is an array of 2 objects that contains 
     * attributes value and color
     */
    var data = [{"color": "#3498DB", "value": inExecution},
        {"color": "#eee", "value": poolSize - inExecution}];

    var margin = 50;

    var width = 130;
    var height = 130;
    var radius = Math.min(width, height) / 2;

    var svg = d3.select('#' + elementid)
            .append('svg')
            .attr('width', width + margin)
            .attr('height', height + margin)
            .append('g')
            .attr('transform', 'translate(' + ((width + margin) / 2) + ',' + ((height + margin) / 2) + ')')

    var arc = d3.svg.arc()
            .outerRadius(radius)
            .innerRadius(radius - 10);

    var pie = d3.layout.pie()
            .value(function (d) {
                return d.value;
            })
            .sort(null);

    svg.append("text")
            .attr("dy", "-8em")
            .style("text-anchor", "middle")
            .attr("class", "name")
            .text(function (d) {
                return name;
            });
    svg.append("text")
            .style("text-anchor", "middle")
            .attr("dy", "+0.2em")
            .attr("class", "count")
            .text(function (d) {
                return inExecution + '/' + poolSize;
            });
    if (remaining > 0) {
        svg.append("text")
            .attr("dy", "+1.9em")
            .style("text-anchor", "middle")
            .attr("class", "remaining")
            .text(function (d) {
                return '(+ ' + remaining + ')';
            });
    }

    var path = svg.selectAll('path')
            .data(pie(data))
            .enter()
            .append('path')
            .attr('d', arc)
            .attr('fill', function (d, i) {
                return d.data.color;
            });
}