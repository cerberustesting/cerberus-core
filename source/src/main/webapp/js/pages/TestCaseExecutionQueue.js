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
$.when($.getScript("js/global/global.js")).then(function () {
    $(document).ready(function () {
        initPage();
    });
});

function initPage() {
    var searchS = GetURLParameter("search");

    displayPageLabel();

    // Display queue information
    drawQueueInformation();

    // Display table
    var configurations = new TableConfigurationsServerSide("executionsTable", "ReadExecutionInQueue", "contentTable", aoColumnsFunc("executionsTable"), [1, 'desc']);
    var table = createDataTableWithPermissions(configurations, renderOptionsForApplication, "#executionList", undefined, true);

    if (searchS !== null) {
        table.search(searchS).draw();
    }

    // React on table redraw
    table.on(
            'draw.dt',
            function () {
                // Un-check the select all checkbox
                $('#selectAll')[0].checked = false;
            }
    );

    // React on select all click
    $("#selectAll").click(selectAll);

    // Display mass action
    displayStateList();
    $("#massActionBrpButtonSetState").click(massActionModalSaveHandler_setState);
    $("#massActionBrpButtonRun").click(massActionModalSaveHandler_run);
    $("#massActionBrpButtonDelete").click(massActionModalSaveHandler_delete);
    $('#massActionBrpModal').on('hidden.bs.modal', massActionModalCloseHandler);

    // React on tab changes
    $('#executionList a[data-toggle="tab"]').on('shown.bs.tab', function (e) {
        switch ($(e.target).attr("href")) {
            case "#tabDetails":
                refreshTable();
                break;
            case "#tabSummary":
                refreshQueueInformation();
                break;
        }
    });
}

function displayPageLabel() {
    var doc = new Doc();

    $("#title").html(doc.getDocLabel("page_testcaseexecutionqueue", "allExecution"));

    displayHeaderLabel(doc);

    displayFooter(doc);
    displayGlobalLabel(doc);
}

function displayStateList() {
    $('#massState').append($('<option></option>').text('CANCELLED').val('CANCELLED'));
    $('#massState').append($('<option></option>').text('WAITING').val('WAITING'));
}

function renderOptionsForApplication(data) {
    if ($("#blankSpace").length === 0) {
        var doc = new Doc();
        var contentToAdd = "<div class='marginBottom10' style='height:34px;' id='blankSpace'>";
        contentToAdd += "<button id='createBrpMassButton' type='button' class='btn btn-default'><span class='glyphicon glyphicon-th-list'></span> " + doc.getDocLabel("page_global", "button_massAction") + "</button>";
        contentToAdd += "</div>";

        $("#executionsTable_wrapper div#executionsTable_length").before(contentToAdd);
        $('#executionList #createBrpMassButton').click(massActionClick);
    }
}

function aoColumnsFunc(tableId) {
    var doc = new Doc();
    var aoColumns = [
        {
            "data": null,
            "sName": "selectAll",
            "title": '<input id="selectAll" title="' + doc.getDocLabel("page_global", "tooltip_massAction") + '" type="checkbox"/>',
            "sWidth": "30px",
            "bSortable": false,
            "bSearchable": false,
            "sClass": "overflowVisible", //change the overflow style for a display bug on chrome
            "mRender": function (data, type, obj) {
                var hasPermissions = $("#" + tableId).attr("hasPermissions");

                var selectBrp = '<input id="selectLine" \n\
                                class="selectBrp margin-right5" \n\
                                name="ids" value=' + obj["id"] + ' data-line="select" data-id="' + obj["id"] + '" title="' + doc.getDocLabel("page_global", "tooltip_massActionLine") + '" type="checkbox">\n\
                                </input>';
                if (hasPermissions === "true") { //only draws the options if the user has the correct privileges
                    return '<div class="center btn-group width50">' + selectBrp + '</div>';
                }
                return '<div class="center btn-group width50"></div>';

            }
        },
        {
            "data": "id",
            "sName": "id",
            "title": doc.getDocLabel("page_testcaseexecutionqueue", "id_col"),
            "sWidth": "40px"
        },
        {
            "data": "tag",
            "sName": "tag",
            "title": doc.getDocLabel("page_testcaseexecutionqueue", "tag_col"),
            "sWidth": "150px",
            "mRender": function (data, type, obj) {
                if (isEmpty(obj["tag"])) {
                    return "";
                } else {
                    return '<a href="ReportingExecutionByTag.jsp?Tag=' + obj["tag"] + '">' + obj["tag"] + '</div>';
                }
            }
        },
        {
            "data": "requestDate",
            "sName": "requestDate",
            "title": doc.getDocLabel("page_testcaseexecutionqueue", "requestDate_col"),
            "sWidth": "110px"
        },
        {
            "data": "state",
            "sName": "state",
            "title": doc.getDocLabel("page_testcaseexecutionqueue", "state_col"),
            "sWidth": "70px"
        },
        {
            "data": "comment",
            "sName": "comment",
            "title": doc.getDocLabel("page_testcaseexecutionqueue", "comment_col"),
            "sWidth": "200px",
            "defaultContent": ""
        },
        {
            "data": "exeId",
            "sName": "exeId",
            "title": doc.getDocLabel("page_testcaseexecutionqueue", "exeId"),
            "sWidth": "40px",
            "defaultContent": "",
            "mRender": function (data, type, obj) {
                if (obj["exeId"] <= 0) {
                    return "";
                } else {
                    return '<a href="TestCaseExecution.jsp?executionId=' + obj["exeId"] + '">' + obj["exeId"] + '</div>';
                }
            }
        },
        {
            "data": "UsrCreated",
            "sName": "UsrCreated",
            "sWidth": "70px",
            "defaultContent": "",
            "title": doc.getDocOnline("transversal", "UsrCreated")
        },
        {
            "data": "DateCreated",
            "sName": "DateCreated",
            "sWidth": "110px",
            "defaultContent": "",
            "title": doc.getDocOnline("transversal", "DateCreated")
        },
        {
            "data": "test",
            "sName": "test",
            "title": doc.getDocLabel("page_testcaseexecutionqueue", "test_col"),
            "sWidth": "70px"
        },
        {
            "data": "testCase",
            "sName": "testcase",
            "title": doc.getDocLabel("page_testcaseexecutionqueue", "testcase_col"),
            "sWidth": "70px"
        },
        {
            "data": "country",
            "sName": "country",
            "title": doc.getDocLabel("page_testcaseexecutionqueue", "country_col"),
            "sWidth": "70px"
        },
        {
            "data": "environment",
            "sName": "environment",
            "title": doc.getDocLabel("page_testcaseexecutionqueue", "environment_col"),
            "sWidth": "70px"
        },
        {
            "data": "robot",
            "sName": "robot",
            "title": doc.getDocLabel("page_testcaseexecutionqueue", "robot_col"),
            "sWidth": "70px",
            "defaultContent": "",
            "visible": false
        },
        {
            "data": "robotIP",
            "sName": "robotIP",
            "title": doc.getDocLabel("page_testcaseexecutionqueue", "robotIP_col"),
            "sWidth": "70px",
            "defaultContent": "",
            "visible": false
        },
        {
            "data": "robotPort",
            "sName": "robotPort",
            "title": doc.getDocLabel("page_testcaseexecutionqueue", "robotPort_col"),
            "sWidth": "70px",
            "defaultContent": "",
            "visible": false
        },
        {
            "data": "browser",
            "sName": "browser",
            "title": doc.getDocLabel("page_testcaseexecutionqueue", "browser_col"),
            "sWidth": "70px",
            "defaultContent": "",
            "visible": false
        },
        {
            "data": "browserVersion",
            "sName": "browserVersion",
            "title": doc.getDocLabel("page_testcaseexecutionqueue", "browserVersion_col"),
            "sWidth": "70px",
            "defaultContent": "",
            "visible": false
        },
        {
            "data": "platform",
            "sName": "platform",
            "title": doc.getDocLabel("page_testcaseexecutionqueue", "platform_col"),
            "sWidth": "70px",
            "defaultContent": "",
            "visible": false
        },
        {
            "data": "manualExecution",
            "sName": "manualExecution",
            "title": doc.getDocLabel("page_testcaseexecutionqueue", "manualExecution_col"),
            "sWidth": "70px",
            "defaultContent": "",
            "visible": false
        },
        {
            "data": "manualURL",
            "sName": "manualURL",
            "title": doc.getDocLabel("page_testcaseexecutionqueue", "manualURL_col"),
            "sWidth": "70px",
            "defaultContent": "",
            "visible": false
        },
        {
            "data": "manualHost",
            "sName": "manualHost",
            "title": doc.getDocLabel("page_testcaseexecutionqueue", "manualHost_col"),
            "sWidth": "70px",
            "defaultContent": "",
            "visible": false
        },
        {
            "data": "manualContextRoot",
            "sName": "manualContextRoot",
            "title": doc.getDocLabel("page_testcaseexecutionqueue", "manualContextRoot_col"),
            "sWidth": "70px",
            "defaultContent": "",
            "visible": false
        },
        {
            "data": "manualLoginRelativeURL",
            "sName": "manualLoginRelativeURL",
            "title": doc.getDocLabel("page_testcaseexecutionqueue", "manualLoginRelativeURL_col"),
            "sWidth": "70px",
            "defaultContent": "",
            "visible": false
        },
        {
            "data": "manualEnvData",
            "sName": "manualEnvData",
            "title": doc.getDocLabel("page_testcaseexecutionqueue", "manualEnvData_col"),
            "sWidth": "70px",
            "defaultContent": "",
            "visible": false
        },
        {
            "data": "screenshot",
            "sName": "screenshot",
            "title": doc.getDocLabel("page_testcaseexecutionqueue", "screenshot_col"),
            "sWidth": "70px",
            "defaultContent": "",
            "visible": false
        },
        {
            "data": "pageSource",
            "sName": "pageSource",
            "title": doc.getDocLabel("page_testcaseexecutionqueue", "pageSource_col"),
            "sWidth": "70px",
            "defaultContent": "",
            "visible": false
        },
        {
            "data": "seleniumLog",
            "sName": "seleniumLog",
            "title": doc.getDocLabel("page_testcaseexecutionqueue", "seleniumLog_col"),
            "sWidth": "70px",
            "defaultContent": "",
            "visible": false
        },
        {
            "data": "verbose",
            "sName": "verbose",
            "title": doc.getDocLabel("page_testcaseexecutionqueue", "verbose_col"),
            "sWidth": "70px",
            "defaultContent": "",
            "visible": false
        },
        {
            "data": "retries",
            "sName": "retries",
            "title": doc.getDocLabel("page_testcaseexecutionqueue", "retries_col"),
            "sWidth": "70px",
            "defaultContent": "",
            "visible": false
        },
        {
            "data": "timeout",
            "sName": "timeout",
            "title": doc.getDocLabel("page_testcaseexecutionqueue", "timeout_col"),
            "sWidth": "70px",
            "defaultContent": "",
            "visible": false
        },
        {
            "data": "UsrModif",
            "sName": "UsrModif",
            "sWidth": "70px",
            "defaultContent": "",
            "title": doc.getDocOnline("transversal", "UsrModif")
        },
        {
            "data": "DateModif",
            "sName": "DateModif",
            "sWidth": "110px",
            "defaultContent": "",
            "title": doc.getDocOnline("transversal", "DateModif")
        }
    ];
    return aoColumns;
}

function selectAll() {
    if ($(this).prop("checked")) {
        $("[data-line='select']").prop("checked", true);
    } else {
        $("[data-line='select']").prop("checked", false);
    }
}

function massActionClick() {
    var doc = new Doc();
    clearResponseMessageMainPage();
    // When creating a new item, Define here the default value.
    var formList = $('#massActionForm');
    if (formList.serialize().indexOf("ids") === -1) {
        var localMessage = new Message("danger", doc.getDocLabel("page_global", "message_massActionError"));
        showMessage(localMessage, null);
    } else {
        $('#massActionBrpModal').modal('show');
    }
}

function massActionModalSaveHandler_setState() {
    clearResponseMessage($('#massActionBrpModal'));

    var formNewValues = $('#massActionBrpModal #massActionBrpModalForm').serialize();
    var formList = $('#massActionForm').serialize();

    var jsonFormNewValues = convertSerialToJSONObject(formNewValues);
    var jsonFormList = convertSerialToJSONObject(formList);

    var requestBody = JSON.stringify($.extend(jsonFormNewValues, jsonFormList));

    showLoaderInModal('#massActionBrpModal');

    jsonPost({
        url: 'UpdateExecutionInQueueState',
        data: requestBody,
        success: function (data) {
            refreshTable();
            hideLoaderInModal('#massActionBrpModal');
            $('#massActionBrpModal').modal('hide');

            if (!data || !Array.isArray(data.inError)) {
                showMessage({
                    messageType: 'KO',
                    message: 'Unexpected error. See logs'
                });
            } else if (data.inError.length > 0) {
                showMessage({
                    messageType: 'WARNING',
                    message: 'Some executions have not been updated: ' + data.inError.toString()
                });
            } else {
                showMessage({
                    messageType: 'OK',
                    message: 'Update successfully executed'
                });
            }
        },
        error: handleErrorAjaxAfterTimeout
    });
}

function massActionModalSaveHandler_run() {
    clearResponseMessage($('#massActionBrpModal'));

    var formList = $('#massActionForm').serialize();

    var requestBody = JSON.stringify(convertSerialToJSONObject(formList));

    showLoaderInModal('#massActionBrpModal');

    jsonPost({
        url: 'RunExecutionInQueue',
        data: requestBody,
        success: function (data) {
            refreshTable();
            hideLoaderInModal('#massActionBrpModal');
            $('#massActionBrpModal').modal('hide');

            if (data) {
                showMessage({
                    messageType: 'OK',
                    message: 'In waiting selected executions are running'
                });
            } else {
                showMessage({
                    messageType: 'KO',
                    message: 'Unexpected error. See logs'
                });
            }
        },
        error: handleErrorAjaxAfterTimeout
    });
}

function massActionModalSaveHandler_delete() {
    clearResponseMessage($('#massActionBrpModal'));

    var formList = $('#massActionForm').serialize();

    var requestBody = JSON.stringify(convertSerialToJSONObject(formList));

    showLoaderInModal('#massActionBrpModal');

    jsonPost({
        url: 'DeleteExecutionInQueue',
        data: requestBody,
        success: function (data) {
            refreshTable();
            hideLoaderInModal('#massActionBrpModal');
            $('#massActionBrpModal').modal('hide');

            if (!data || !Array.isArray(data.inError)) {
                showMessage({
                    messageType: 'KO',
                    message: 'Unexpected error. See logs'
                });
            } else if (data.inError.length > 0) {
                showMessage({
                    messageType: 'WARNING',
                    message: 'Some executions have not been deleted: ' + data.inError.toString()
                });
            } else {
                showMessage({
                    messageType: 'OK',
                    message: 'Delete successfully executed'
                });
            }
        },
        error: handleErrorAjaxAfterTimeout
    });
}

function massActionModalCloseHandler() {
    // reset form values
    $('#massActionBrpModal #massActionBrpModalForm')[0].reset();
    // remove all errors on the form fields
    $(this).find('div.has-error').removeClass("has-error");
    // clear the response messages of the modal
    clearResponseMessage($('#massActionBrpModal'));
}

function getTable() {
    return $('#executionsTable').dataTable()
}

function resetTableFilters() {
    resetFilters(getTable());
}

function refreshTable() {
    getTable().fnDraw();
}

function refreshQueueInformation() {
    clearQueueInformation();
    drawQueueInformation();
}

function clearQueueInformation() {
    $('#statusChart').empty();
}

function drawQueueInformation() {

    var jqxhr = $.get("ReadExecutionPools");
    $.when(jqxhr).then(function (data) {
        //var messageType = getAlertType(data.messageType);
        //if (messageType === "success") {
        //redraw the datatable
        for (var inc = 0; inc < data.length; inc++) {
            generatePie("statusChart", inc, data[inc]);
        }
        //}
        //show message in the main page
        //showMessageMainPage(messageType, data.message, false);
        //close confirmation window
        //$('#confirmationModal').modal('hide');
    }).fail(handleErrorAjaxAfterTimeout);
}

function filterAndDisplayTable(poolId) {
    filterTable(poolId);
    displayTable();
}

function filterTable(poolId) {
    $.getJSON({
        url: "ReadExecutionPool",
        data: "system=" + poolId.system + "&application=" + poolId.application + "&country=" + poolId.country + "&environment=" + poolId.environment,
        success: function (data) {
            // Get associated execution ids from pool
            var associcatedIds = [];
            data.EXECUTING.forEach(function (exec) {
                associcatedIds.push(exec.toExecute.id);
            });
            data.QUEUED.forEach(function (exec) {
                associcatedIds.push(exec.toExecute.id);
            });

            if (associcatedIds.length == 0) {
                resetTableFilters();
                showMessage({
                    messageType: 'WARNING',
                    message: 'Execution pool is empty, showing the whole table'
                });
            } else {
                // Apply filter
                applyFiltersOnMultipleColumns(
                        'executionsTable',
                        [
                            {
                                param: 'id',
                                values: associcatedIds
                            }
                        ]
                        );
                refreshTable();
            }
        },
        error: handleErrorAjaxAfterTimeout
    });
}

function displayTable() {
    $('.nav-tabs a[href="#tabDetails"]').tab('show');
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
function generatePie(root, id, data) {
    var width = 130;
    var height = 130;
    var margin = {
        horizontal: 50,
        vertical: 50
    };
    var totalWidth = width + margin.horizontal;
    var totalHeight = height + margin.vertical;

    var container = $('<div/>')
            .attr('id', root + id)
            .attr('role', 'button')
            .addClass('pie')
            .css('width', totalWidth)
            .css('height', totalHeight);
    container.click(function () {
        filterAndDisplayTable(data.id);
    });
    $('#' + root).append(container);

    /**
     * Generate data object which is an array of 2 objects that contains 
     * attributes value and color
     */
    var colors = [{"color": "#3498DB", "value": data.inExecution},
        {"color": "#eee", "value": data.poolSize - data.inExecution}];

    var radius = Math.min(width, height) / 2;

    var svg = d3.select('#' + root + id)
            .append('svg')
            .attr('width', totalHeight)
            .attr('height', totalHeight)
            .append('g')
            .attr('transform', 'translate(' + ((totalWidth) / 2) + ',' + ((totalHeight) / 2) + ')');

    var arc = d3.svg.arc()
            .outerRadius(radius)
            .innerRadius(radius - 10);

    var pie = d3.layout.pie()
            .value(function (d) {
                return d.value;
            })
            .sort(null);

    svg.append("text")
            .attr("dy", "-7.1em")
            .style("text-anchor", "middle")
            .attr("class", "primary-name")
            .text(function (d) {
                return data.id.application;
            });
    svg.append("text")
            .attr("dy", "-7.2em")
            .style("text-anchor", "middle")
            .attr("class", "secondary-name")
            .text(function (d) {
                return '(' + data.id.country + ' - ' + data.id.environment + ')';
            });
    svg.append("text")
            .style("text-anchor", "middle")
            .attr("dy", "+0.2em")
            .attr("class", "count")
            .text(function (d) {
                return data.inExecution + '/' + data.poolSize;
            });
    if (data.remaining > 0) {
        svg.append("text")
                .attr("dy", "+1.9em")
                .style("text-anchor", "middle")
                .attr("class", "remaining")
                .text(function (d) {
                    return '(+ ' + data.remaining + ')';
                });
    }

    var path = svg.selectAll('path')
            .data(pie(colors))
            .enter()
            .append('path')
            .attr('d', arc)
            .attr('fill', function (d, i) {
                return d.data.color;
            });

}
