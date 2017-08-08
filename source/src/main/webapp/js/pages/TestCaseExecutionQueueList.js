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

    displayAndRefresh_followup();


    // Display table
    var configurations = new TableConfigurationsServerSide("executionsTable", "ReadTestCaseExecutionQueue", "contentTable", aoColumnsFunc("executionsTable"), [1, 'desc'], [10, 25, 50, 100, 200, 500, 1000]);
    var table = createDataTableWithPermissions(configurations, renderOptionsForExeQueue, "#executionList", undefined, true);

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
    $("#massActionExeQButtonSubmit").click(massActionModalSaveHandler_submit);
    $("#massActionExeQButtonCopy").click(massActionModalSaveHandler_copy);
    $("#massActionExeQButtonCancel").click(massActionModalSaveHandler_cancel);
    $("#massActionExeQButtonCancelForce").click(massActionModalSaveHandler_cancelForce);
    $("#massActionExeQButtonPrio").click(massActionModalSaveHandler_changePrio);
    $('#massActionExeQModal').on('hidden.bs.modal', massActionModalCloseHandler);

    // React on tab changes
    $('#executionList a[data-toggle="tab"]').on('shown.bs.tab', function (e) {
        switch ($(e.target).attr("href")) {
            case "#tabDetails":
                refreshTable();
                break;
            case "#tabFollowUp":
                displayAndRefresh_followup();
                break;
            case "#tabJobStatus":
                displayAndRefresh_jobStatus();
                break;
        }
    });
}

function displayAndRefresh_followup() {
    // Display table
    var jqxhr = $.getJSON("ReadTestCaseExecutionQueue?flag=queueStatus");
    $.when(jqxhr).then(function (data) {
        var obj = data["contentTable"];

        /* TESTCASE */

        var array = [];

        $.each(obj, function (e) {

            array.push(
                    [obj[e].contrainId, obj[e].system, obj[e].environment, obj[e].country, obj[e].application, obj[e].robot, obj[e].nbRunning, obj[e].nbPoolSize, obj[e].nbInQueue]
                    );
        });

        if ($("#followUpTableList #followUpTable_wrapper").length > 0) {
            $("#followUpTableList #followUpTable").DataTable().clear();
            $("#followUpTableList #followUpTable").DataTable().rows.add(array).draw();
        } else {
            var configurations1 = new TableConfigurationsClientSide("followUpTable", array, aoColumnsFunc_followUp(), true);
            createDataTableWithPermissions(configurations1, undefined, "#followUpTableList", undefined, true);
        }
    });


}

function displayAndRefresh_jobStatus() {
    // Display table
    var jqxhr = $.getJSON("ExecuteNextInQueue");
    $.when(jqxhr).then(function (data) {
        var obj = data;

        $("#jobRunning").val(data["jobRunning"]);
        $("#jobStart").val(data["jobStart"]);
        $("#jobActive").val(data["jobActive"]);
    });
}

function forceExecution() {
    // Display table
    var jqxhr = $.getJSON("ExecuteNextInQueue?forceExecution=Y");
    $.when(jqxhr).then(function (data) {
        var obj = data;

        $("#jobRunning").val(data["jobRunning"]);
        $("#jobStart").val(data["jobStart"]);
        $("#jobActive").val(data["jobActive"]);
    });
}


function renderOptionsForExeQueue(data) {
    if ($("#blankSpace").length === 0) {
        var doc = new Doc();
        var contentToAdd = "<div class='marginBottom10' style='height:34px;' id='blankSpace'>";
        contentToAdd += "<button id='createBrpMassButton' type='button' class='btn btn-default'><span class='glyphicon glyphicon-th-list'></span> " + doc.getDocLabel("page_global", "button_massAction") + "</button>";
        contentToAdd += "<button id='refreshExecutionButton' type='button' class='btn btn-default'><span class='glyphicon glyphicon-refresh'></span> " + doc.getDocLabel("page_global", "refresh") + "</button>";
        contentToAdd += "<button id='selectPendingButton' type='button' class='btn btn-default'>" + doc.getDocLabel("page_testcaseexecutionqueue", "button_filterPending") + "</button>";
        contentToAdd += "<button id='selectRunningButton' type='button' class='btn btn-default'>" + doc.getDocLabel("page_testcaseexecutionqueue", "button_filterExecuting") + "</button>";
        contentToAdd += "</div>";

        $("#executionsTable_wrapper div#executionsTable_length").before(contentToAdd);
        $('#executionList #createBrpMassButton').click(massActionClick);
        $('#executionList #refreshExecutionButton').click(refreshTable);
        $('#executionList #selectPendingButton').click(filterPending);
        $('#executionList #selectRunningButton').click(filterERunning);
    }
}

function filterPending() {
    filterOnColumn("executionsTable", "state", "QUEUED");
}

function filterERunning() {
    filterOnColumn("executionsTable", "state", "EXECUTING,STARTING,WAITING");
}

function displayPageLabel() {
    var doc = new Doc();

    $("#title").html(doc.getDocLabel("page_testcaseexecutionqueue", "allExecution"));

    displayHeaderLabel(doc);

    displayFooter(doc);
    displayGlobalLabel(doc);
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
    if (formList.serialize().indexOf("id") === -1) {
        var localMessage = new Message("danger", doc.getDocLabel("page_global", "message_massActionError"));
        showMessage(localMessage, null);
    } else {
        $('#massActionExeQModal').modal('show');
    }
}

function massActionModalSaveHandler_submit() {
    clearResponseMessage($('#massActionExeQModal'));

    var formNewValues = $('#massActionExeQModal #massActionExeQModalForm');
    var formList = $('#massActionForm');
    var paramSerialized = formList.serialize();

    showLoaderInModal('#massActionExeQModal');

    var jqxhr = $.post("UpdateTestCaseExecutionQueue", paramSerialized + "&actionState=toQUEUED", "json");
    $.when(jqxhr).then(function (data) {
        // unblock when remote call returns 
        hideLoaderInModal('#massActionExeQModal');
        if ((getAlertType(data.messageType) === "success") || (getAlertType(data.messageType) === "warning")) {
            $('#executionsTable').DataTable().draw();
            $("#selectAll").prop("checked", false);
            $('#massActionExeQModal').modal('hide');
            showMessage(data);
        } else {
            showMessage(data, $('#massActionExeQModal'));
        }
    }).fail(handleErrorAjaxAfterTimeout);
}

function massActionModalSaveHandler_copy() {
    clearResponseMessage($('#massActionExeQModal'));

    var formNewValues = $('#massActionExeQModal #massActionExeQModalForm');
    var formList = $('#massActionForm');
    var paramSerialized = formList.serialize();

    showLoaderInModal('#massActionExeQModal');

    var jqxhr = $.post("CreateTestCaseExecutionQueue", paramSerialized + "&actionState=toQUEUED&actionSave=save", "json");
    $.when(jqxhr).then(function (data) {
        // unblock when remote call returns 
        hideLoaderInModal('#massActionExeQModal');
        if ((getAlertType(data.messageType) === "success") || (getAlertType(data.messageType) === "warning")) {
            $('#executionsTable').DataTable().draw();
            $("#selectAll").prop("checked", false);
            $('#massActionExeQModal').modal('hide');
            showMessage(data);
        } else {
            showMessage(data, $('#massActionExeQModal'));
        }
    }).fail(handleErrorAjaxAfterTimeout);
}

function massActionModalSaveHandler_cancel() {
    clearResponseMessage($('#massActionExeQModal'));

    var formNewValues = $('#massActionExeQModal #massActionExeQModalForm');
    var formList = $('#massActionForm');
    var paramSerialized = formList.serialize();

    showLoaderInModal('#massActionExeQModal');

    var jqxhr = $.post("UpdateTestCaseExecutionQueue", paramSerialized + "&actionState=toCANCELLED", "json");
    $.when(jqxhr).then(function (data) {
        // unblock when remote call returns 
        hideLoaderInModal('#massActionExeQModal');
        if ((getAlertType(data.messageType) === "success") || (getAlertType(data.messageType) === "warning")) {
            $('#executionsTable').DataTable().draw();
            $("#selectAll").prop("checked", false);
            $('#massActionExeQModal').modal('hide');
            showMessage(data);
        } else {
            showMessage(data, $('#massActionExeQModal'));
        }
    }).fail(handleErrorAjaxAfterTimeout);
}

function massActionModalSaveHandler_cancelForce() {
    clearResponseMessage($('#massActionExeQModal'));

    var formNewValues = $('#massActionExeQModal #massActionExeQModalForm');
    var formList = $('#massActionForm');
    var paramSerialized = formList.serialize();

    showLoaderInModal('#massActionExeQModal');

    var jqxhr = $.post("UpdateTestCaseExecutionQueue", paramSerialized + "&actionState=toCANCELLEDForce", "json");
    $.when(jqxhr).then(function (data) {
        // unblock when remote call returns 
        hideLoaderInModal('#massActionExeQModal');
        if ((getAlertType(data.messageType) === "success") || (getAlertType(data.messageType) === "warning")) {
            $('#executionsTable').DataTable().draw();
            $("#selectAll").prop("checked", false);
            $('#massActionExeQModal').modal('hide');
            showMessage(data);
        } else {
            showMessage(data, $('#massActionExeQModal'));
        }
    }).fail(handleErrorAjaxAfterTimeout);
}

function massActionModalSaveHandler_changePrio() {
    clearResponseMessage($('#massActionExeQModal'));

    var formNewValues = $('#massActionExeQModal #massActionExeQModalForm');
    var formList = $('#massActionForm');
    var paramSerialized = formList.serialize();
    var newPrio = $('#massActionExeQModalForm #priority').val();

    showLoaderInModal('#massActionExeQModal');

    var jqxhr = $.post("UpdateTestCaseExecutionQueue", paramSerialized + "&actionSave=priority&priority=" + newPrio, "json");
    $.when(jqxhr).then(function (data) {
        // unblock when remote call returns 
        hideLoaderInModal('#massActionExeQModal');
        if ((getAlertType(data.messageType) === "success") || (getAlertType(data.messageType) === "warning")) {
            $('#executionsTable').DataTable().draw();
            $("#selectAll").prop("checked", false);
            $('#massActionExeQModal').modal('hide');
            showMessage(data);
        } else {
            showMessage(data, $('#massActionExeQModal'));
        }
    }).fail(handleErrorAjaxAfterTimeout);
}

function massActionModalCloseHandler() {
    // reset form values
    $('#massActionExeQModal #massActionExeQModalForm')[0].reset();
    // remove all errors on the form fields
    $(this).find('div.has-error').removeClass("has-error");
    // clear the response messages of the modal
    clearResponseMessage($('#massActionExeQModal'));
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

function filterAndDisplayTable(poolId) {
    filterTable(poolId);
    displayTable();
}

function displayTable() {
    $('.nav-tabs a[href="#tabDetails"]').tab('show');
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
                                name="id" value=' + obj["id"] + ' data-line="select" data-id="' + obj["id"] + '" title="' + doc.getDocLabel("page_global", "tooltip_massActionLine") + '" type="checkbox">\n\
                                </input>';
                if (hasPermissions === "true") { //only draws the options if the user has the correct privileges
                    return '<div class="center btn-group width50">' + selectBrp + '</div>';
                }
                return '<div class="center btn-group width50"></div>';

            }
        },
        {
            "sName": "id",
            "data": "id",
            "bSortable": false,
            "bSearchable": false,
            "sWidth": "50px",
            "title": doc.getDocLabel("testdatalib", "actions"),
            "mRender": function (data, type, oObj) {
//                var hasPermissions = $("#" + tableId).attr("hasPermissions");
//                var hasPermissions = true;
                var hasPermissions = $("#" + tableId).attr("hasPermissions");
                var editElement = '<button id="editExeQ' + data + '"  onclick="openModalTestCaseExecutionQueue(' + data + ',\'EDIT\');" \n\
                                class="btn btn-default btn-xs margin-right5" \n\
                            name="editExecutionQueue" title="' + doc.getDocLabel("page_testdatalib", "tooltip_editentry") + '" type="button">\n\
                            <span class="glyphicon glyphicon-pencil"></span></button>';
                var viewElement = '<button id="viewExeQ' + data + '"  onclick="openModalTestCaseExecutionQueue(' + data + ',\'EDIT\');" \n\
                                class="btn btn-default btn-xs margin-right5" \n\
                            name="viewExecutionQueue" title="' + doc.getDocLabel("page_testdatalib", "tooltip_editentry") + '" type="button">\n\
                            <span class="glyphicon glyphicon-eye-open"></span></button>';
                var duplicateElement = '<button id="dupExeQ' + data + '"  onclick="openModalTestCaseExecutionQueue(' + data + ',\'DUPLICATE\');" \n\
                                class="btn btn-default btn-xs margin-right5" \n\
                            name="duplicateExecutionQueue" title="' + doc.getDocLabel("page_testdatalib", "tooltip_editentry") + '" type="button">\n\
                            <span class="glyphicon glyphicon-duplicate"></span></button>';

                var buttons = "";
                if ((hasPermissions) && ((oObj.state === "WAITING") || (oObj.state === "ERROR") || (oObj.state === "CANCELLED") || (oObj.state === "QUEUED"))) {
                    buttons += editElement;
                } else {
                    buttons += viewElement;
                }
                buttons += duplicateElement;
                return '<div class="center btn-group width100">' + buttons + '</div>';
            }
        },
        {
            "data": "id",
            "sName": "id",
            "title": doc.getDocLabel("page_testcaseexecutionqueue", "id_col"),
            "sWidth": "40px"
        },
        {
            "data": "priority",
            "sName": "priority",
            "title": doc.getDocLabel("testcaseexecutionqueue", "priority"),
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
            "data": "debugFlag",
            "sName": "debugFlag",
            "title": doc.getDocLabel("testcaseexecutionqueue", "debugFlag"),
            "sWidth": "70px",
            "defaultContent": "",
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

function aoColumnsFunc_followUp() {
    var doc = new Doc();
    var aoColumns = [
        {"data": "0", "sName": "constrainsId", "title": doc.getDocLabel("page_testcaseexecutionqueue", "constrain")},
        {"data": "1", "sName": "system", "title": doc.getDocLabel("invariant", "SYSTEM")},
        {"data": "2", "sName": "environment", "title": doc.getDocLabel("invariant", "ENVIRONMENT")},
        {"data": "3", "sName": "country", "title": doc.getDocLabel("invariant", "COUNTRY")},
        {"data": "4", "sName": "application", "title": doc.getDocLabel("application", "Application")},
        {"data": "5", "sName": "robot", "title": doc.getDocLabel("robot", "robot")},
        {"data": "6", "sName": "nbRunning", "title": doc.getDocLabel("page_testcaseexecutionqueue", "nbRunning")},
        {"data": "7", "sName": "nbPoolSize", "title": doc.getDocLabel("page_testcaseexecutionqueue", "nbPoolSize")},
        {"data": "8", "sName": "nbInQueue", "title": doc.getDocLabel("page_testcaseexecutionqueue", "nbInQueue")},
        {
            "data": null, "sName": "saturation", "title": doc.getDocLabel("page_testcaseexecutionqueue", "saturation"),
            "mRender": function (data, type, obj) {
                var saturation_level;
                var satcolor;
                if (obj[7] > 0) {
                    saturation_level = (obj[6] / obj[7]) * 100;
                } else {
                    saturation_level = 0;
                }
                if (saturation_level > 90) {
                    satcolor = "#D9534F";
                } else {
                    satcolor = "#5CB85C";
                }
                return "<div class='progress-bar' role='progressbar' style='width:" + saturation_level + "%; background-color: " + satcolor + ";'>" + saturation_level + "%</div>";

            }
        },
        {
            "data": null, "sName": "oversaturation", "title": doc.getDocLabel("page_testcaseexecutionqueue", "oversaturation"),
            "mRender": function (data, type, obj) {
                if ((obj[7] > 0) && ((obj[6] >= obj[7]))) {
                    return obj[8];
                } else {
                    return 0;
                }
            }
        }
    ];
    return aoColumns;
}
