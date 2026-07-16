/*
 * Cerberus Copyright (C) 2013 - 2025 cerberustesting
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


var tabClicked = false;

$.when($.getScript("js/global/global.js")).then(function () {
    $(document).ready(function () {
        initPage();

        $('[data-toggle="popover"]').popover({
            'placement': 'auto',
            'container': 'body'}
        );

    });
});

function initPage() {
    var searchS = GetURLParameter("search");
    var myTag = GetURLParameter("tag");

    displayPageLabel();
    initGraph();

    $('#frompicker').datetimepicker();
    $('#topicker').datetimepicker({
        useCurrent: false //Important! See issue #1075
    });

    $("#frompicker").on("dp.change", function (e) {
        $('#topicker').data("DateTimePicker").minDate(e.date);
    });
    $("#topicker").on("dp.change", function (e) {
        $('#frompicker').data("DateTimePicker").maxDate(e.date);
    });

    var from = GetURLParameter("from");
    var to = GetURLParameter("to");

    let fromD;
    let toD;
    if (from === null) {
        fromD = new Date();
        fromD.setHours(fromD.getHours() - 1);
    } else {
        fromD = new Date(from);
    }
    if (to === null) {
        toD = new Date();
        toD.setHours(23);
        toD.setMinutes(59);
    } else {
        toD = new Date(to);
    }
    $('#frompicker').data("DateTimePicker").date(moment(fromD));
    $('#topicker').data("DateTimePicker").date(moment(toD));


    // Display table
    var configurations = new TableConfigurationsServerSide("executionsTable", "ReadTestCaseExecutionQueue", "contentTable", aoColumnsFunc("executionsTable"), [2, 'desc'], [10, 15, 20, 30, 50, 100, 200, 500, 1000]);
    var table = createDataTableWithPermissionsNew(configurations, renderOptionsForExeQueue, "#executionList", undefined, true);

    if (searchS !== null) {
        table.search(searchS).draw();
    }

    if (myTag !== null) {
        filterTag(myTag);
    }

    // React on table redraw
    table.on(
            'draw.dt',
            function () {
                // Un-check the select all checkbox
                if ($('#selectAll').length) {
                    $('#selectAll')[0].checked = false;
                }
                // action buttons reveal on row hover + lucide icons in cells
                $('#executionsTable tbody tr').addClass('group');
                if (window.lucide) {
                    lucide.createIcons();
                }
            }
    );
    $('#followUpTable').on('draw.dt', function () {
        $(this).find('tbody tr').addClass('group');
        if (window.lucide) {
            lucide.createIcons();
        }
    });

    // React on select all click
    $("#selectAll").click(selectAll);

    // Display mass action
    $("#massActionExeQButtonSubmit").click(massActionModalSaveHandler_submit);
    $("#massActionExeQButtonCopy").click(massActionModalSaveHandler_copy);
    $("#massActionExeQButtonCancel").click(massActionModalSaveHandler_cancel);
    $("#massActionExeQButtonCancelForce").click(massActionModalSaveHandler_cancelForce);
    $("#massActionExeQButtonErrorForce").click(massActionModalSaveHandler_errorForce);
    $("#massActionExeQButtonPrio").click(massActionModalSaveHandler_changePrio);
    $('#massActionExeQModal').on('hidden.bs.modal', massActionModalCloseHandler);
    window.addEventListener('massaction-modal-close', massActionModalCloseHandler);

    // Tabs are driven by Alpine buttons in the JSP; keep the last visited one
    var tab = sessionStorage.getItem("TestCaseExecutionQueueList-TAB");
    if (isEmpty(tab) || tab.indexOf('#') === 0) {
        tab = "details";
    }
    switchQueueTab(tab);
}

// Called by the Alpine tab buttons of the page
function switchQueueTab(name) {
    sessionStorage.setItem("TestCaseExecutionQueueList-TAB", name);
    switch (name) {
        case "details":
            if (tabClicked) {
                refreshTable();
            }
            break;
        case "followup":
            displayAndRefresh_followup();
            tabClicked = true;
            break;
        case "jobstatus":
            displayAndRefresh_jobStatus();
            tabClicked = true;
            break;
        case "history":
            loadStatGraph();
            tabClicked = true;
            break;
    }
}

function displayAndRefresh_followup() {
    showLoader('#followUpTableList');

    // Display table
    var jqxhr = $.getJSON("ReadTestCaseExecutionQueue?flag=queueStatus");
    $.when(jqxhr).then(function (data) {
        var obj = data["contentTable"];

        /* TESTCASE */

        var array = [];

        $.each(obj, function (e) {

            array.push(
                    [obj[e].contrainId, obj[e].system, obj[e].environment, obj[e].country, obj[e].application
                                , obj[e].robot, obj[e].nbRunning, obj[e].nbPoolSize, obj[e].nbInQueue, obj[e].hasPermissionsUpdate, obj[e].invariantExist]
                    );
        });

        if ($("#followUpTableList #followUpTable_wrapper").length > 0) {
            $("#followUpTableList #followUpTable").DataTable().clear();
            $("#followUpTableList #followUpTable").DataTable().rows.add(array).draw();
        } else {
            var configurations1 = new TableConfigurationsClientSide("followUpTable", array, aoColumnsFunc_followUp(), true, [1, 'asc']);
            createDataTableWithPermissionsNew(configurations1, undefined, "#followUpTableList", undefined, true);
        }

        hideLoader('#followUpTableList');
    });


}

function displayAndRefresh_jobStatus() {
    showLoader('#QueueJobStatus');
    showLoader('#QueueJobActive');

    var jqxhr = $.getJSON("GetExecutionsInQueue");
    $.when(jqxhr).then(function (data) {
        var obj = data;

        $("#jobRunning").val(data["jobRunning"]);
        $("#jobStart").val(data["jobStart"]);
        $("#jobActive").val(data["jobActive"].toString());
        $("#instanceJobActive").val(data["executionThreadPoolInstanceActive"].toString());
        if (data["jobActive"]) {
            $("#jobActiveStatus").html('<i data-lucide="refresh-cw" class="w-7 h-7 animate-spin" style="color: var(--crb-green-color, #00d27a); animation-duration: 3s"></i>');
            $("#modifyParambutton").html('<i data-lucide="pause" class="w-4 h-4"></i><span>Stop Queue Job</span>');
        } else {
            $("#jobActiveStatus").html('<i data-lucide="pause" class="w-7 h-7" style="color: var(--crb-orange-color, #f5803e)"></i>');
            $("#modifyParambutton").html('<i data-lucide="play" class="w-4 h-4"></i><span>Start Queue Job</span>');
        }
        if (window.lucide) {
            lucide.createIcons();
        }

        if (data["jobActiveHasPermissionsUpdate"]) {
            $("#modifyParambutton").attr("disabled", false);
        } else {
            $("#modifyParambutton").attr("disabled", true);
        }

        hideLoader('#QueueJobStatus');
        hideLoader('#QueueJobActive');

    });
}

function forceExecution() {

    var jqxhr = $.getJSON("GetExecutionsInQueue?forceExecution=Y");
    $.when(jqxhr).then(function (data) {
        var obj = data;

        displayAndRefresh_jobStatus();

    });
}

function renderOptionsForExeQueue(data) {
    if ($("#createBrpMassButton").length === 0) {
        var doc = new Doc();
        var secondaryBtnClass = "flex items-center gap-1.5 px-3 py-1 rounded-md mr-2 h-10 w-auto border border-gray-300 dark:border-gray-600 hover:bg-gray-100 dark:hover:bg-gray-700 transition";
        var contentToAdd = `
            <button id='createBrpMassButton' type='button'
                class='text-white bg-sky-400 hover:bg-sky-500 flex items-center space-x-1 px-3 py-1 rounded-md mr-2 h-10 w-auto'>
                <i data-lucide="list-checks" class="w-4 h-4"></i>
                <span>` + doc.getDocLabel("page_global", "button_massAction") + `</span>
            </button>
            <button id='selectDepButton' type='button' class='` + secondaryBtnClass + `'>
                <i data-lucide="git-branch" class="w-4 h-4"></i>
                <span>` + doc.getDocLabel("page_testcaseexecutionqueue", "button_filterPendingWithDep") + `</span>
            </button>
            <button id='selectPendingButton' type='button' class='` + secondaryBtnClass + `'>
                <i data-lucide="clock" class="w-4 h-4"></i>
                <span>` + doc.getDocLabel("page_testcaseexecutionqueue", "button_filterPending") + `</span>
            </button>
            <button id='selectRunningButton' type='button' class='` + secondaryBtnClass + `'>
                <i data-lucide="play" class="w-4 h-4"></i>
                <span>` + doc.getDocLabel("page_testcaseexecutionqueue", "button_filterExecuting") + `</span>
            </button>
        `;

        var $wrapper = $("#executionsTable_buttonWrapper");
        if ($wrapper.length) {
            $wrapper.prepend(contentToAdd);
        } else {
            $("#executionsTable_wrapper div#executionsTable_length").before("<div id='executionsTable_buttonWrapper'>" + contentToAdd + "</div>");
        }
        if (window.lucide) {
            lucide.createIcons();
        }

        $('#executionList #createBrpMassButton').click(massActionClick);
        $('#executionList #selectDepButton').click(filterQueuedWithDep);
        $('#executionList #selectPendingButton').click(filterQueued);
        $('#executionList #selectRunningButton').click(filterERunning);
    }
}

function filterQueued() {
    filterOnColumn("executionsTable", "state", "QUEUED");
}

function filterQueuedWithDep() {
    filterOnColumn("executionsTable", "state", "QUWITHDEP,QUEUED");
}

function filterERunning() {
    filterOnColumn("executionsTable", "state", "EXECUTING,STARTING,WAITING");
}

function filterTag(myTag) {
    filterOnColumn("executionsTable", "tag", myTag);
}

function displayPageLabel() {
    var doc = new Doc();

    $("#title").html(doc.getDocLabel("page_testcaseexecutionqueue", "allExecution"));
    $("#pageTitle").html(doc.getDocLabel("page_testcaseexecutionqueue", "allExecution"));


    //displayHeaderLabel(doc);

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
        window.dispatchEvent(new CustomEvent('massaction-modal-open'));
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
            window.dispatchEvent(new CustomEvent('massaction-modal-close'));
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
            window.dispatchEvent(new CustomEvent('massaction-modal-close'));
            if (data.addedEntries === 1) {
                data.message = data.message + "<a href='TestCaseExecution.jsp?executionQueueId=" + data.testCaseExecutionQueueList[0].id + "'><button class='btn btn-primary' id='goToExecution'>Open Execution</button></a>";
            }
            showMessageMainPage(getAlertType(data.messageType), data.message, false, 60000);
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
            window.dispatchEvent(new CustomEvent('massaction-modal-close'));
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
            window.dispatchEvent(new CustomEvent('massaction-modal-close'));
            showMessage(data);
        } else {
            showMessage(data, $('#massActionExeQModal'));
        }
    }).fail(handleErrorAjaxAfterTimeout);
}

function massActionModalSaveHandler_errorForce() {
    clearResponseMessage($('#massActionExeQModal'));

    var formNewValues = $('#massActionExeQModal #massActionExeQModalForm');
    var formList = $('#massActionForm');
    var paramSerialized = formList.serialize();

    showLoaderInModal('#massActionExeQModal');

    var jqxhr = $.post("UpdateTestCaseExecutionQueue", paramSerialized + "&actionState=toERRORForce", "json");
    $.when(jqxhr).then(function (data) {
        // unblock when remote call returns 
        hideLoaderInModal('#massActionExeQModal');
        if ((getAlertType(data.messageType) === "success") || (getAlertType(data.messageType) === "warning")) {
            $('#executionsTable').DataTable().draw();
            $("#selectAll").prop("checked", false);
            window.dispatchEvent(new CustomEvent('massaction-modal-close'));
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
            window.dispatchEvent(new CustomEvent('massaction-modal-close'));
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
    console.info("refresh");
    getTable().fnDraw();
}

//function filterAndDisplayTable(poolId) {
//    filterTable(poolId);
//    displayTable();
//}
//
//function displayTable() {
//    $('.nav-tabs a[href="#tabDetails"]').tab('show');
//}

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
                                class="selectBrp h-4 w-4 accent-sky-500 cursor-pointer" \n\
                                name="id" value=' + obj["id"] + ' data-line="select" data-id="' + obj["id"] + '" title="' + doc.getDocLabel("page_global", "tooltip_massActionLine") + '" type="checkbox">\n\
                                </input>';
                if (hasPermissions === "true") { //only draws the options if the user has the correct privileges
                    return '<div class="flex items-center justify-start">' + selectBrp + '</div>';
                }
                return '';

            }
        },
        {
            "sName": "id",
            "data": "id",
            "bSortable": false,
            "bSearchable": false,
            "sWidth": "50px",
            "title": doc.getDocLabel("page_global", "columnAction"),
            "mRender": function (data, type, oObj) {
                var hasPermissions = $("#" + tableId).attr("hasPermissions");

                const baseBtnClass = "inline-flex aspect-square h-8 w-8 items-center justify-center rounded-md transition-all duration-200 " +
                    "text-slate-500 hover:bg-slate-200 dark:hover:bg-slate-800 " +
                    "opacity-20 group-hover:opacity-100 [&_svg]:size-4";

                function actionButton(id, title, onClick, icon, extraClass) {
                    return '<button id="' + id + '" type="button" class="' + baseBtnClass + ' ' + (extraClass || '') + '" title="' + title + '" onclick="' + onClick + '">' +
                        '<i data-lucide="' + icon + '" class="w-4 h-4"></i></button>';
                }

                var canEdit = (hasPermissions) && ((oObj.state === "WAITING") || (oObj.state === "ERROR") || (oObj.state === "CANCELLED") || (oObj.state === "QUEUED"));
                var buttons = "";
                buttons += actionButton(
                    (canEdit ? 'editExeQ' : 'viewExeQ') + data,
                    doc.getDocLabel("page_testcaseexecutionqueue", canEdit ? "tooltip_editentry" : "tooltip_viewentry"),
                    "openModalTestCaseExecutionQueue(" + data + ",'EDIT');",
                    canEdit ? 'pencil' : 'eye',
                    'group-hover:!text-blue-500');
                buttons += actionButton(
                    'dupExeQ' + data,
                    doc.getDocLabel("page_testcaseexecutionqueue", "tooltip_dupentry"),
                    "openModalTestCaseExecutionQueue(" + data + ",'DUPLICATE');",
                    'copy',
                    'group-hover:!text-purple-500');
                return '<div class="flex items-center justify-start gap-1">' + buttons + '</div>';
            }
        },
        {
            "data": "id",
            "like": true,
            "sName": "id",
            "title": doc.getDocLabel("page_testcaseexecutionqueue", "id_col"),
            "sWidth": "40px",
            "mRender": function (data, type, oObj) {
                if (oObj["exeId"] <= 0) {
                    return '<a href="TestCaseExecutionV2.jsp?executionQueueId=' + oObj["id"] + '">' + oObj["id"] + '</a>';
                } else {
                    return '<a href="TestCaseExecutionV2.jsp?executionId=' + oObj["exeId"] + '">' + oObj["id"] + '</a>';
                }
            }
        },
        {
            "data": "test",
            "sName": "test",
            "title": doc.getDocLabel("page_testcaseexecutionqueue", "test_col"),
            "sWidth": "70px"
        },
        {
            "data": "testCase",
            "like": true,
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
            "defaultContent": ""
        },
        {
            "data": "tag",
            "like": true,
            "sName": "tag",
            "title": doc.getDocLabel("page_testcaseexecutionqueue", "tag_col"),
            "sWidth": "150px",
            "mRender": function (data, type, obj) {
                if (isEmpty(obj["tag"])) {
                    return "";
                } else {
                    return '<a href="ReportingExecutionByTagV2.jsp?Tag=' + encodeURIComponent(obj["tag"]) + '">' + obj["tag"] + '</a>';
                }
            }
        },
        {
            "data": "requestDate",
            "like": true,
            "sName": "requestDate",
            "title": doc.getDocLabel("page_testcaseexecutionqueue", "requestDate_col"),
            "sWidth": "110px",
            "mRender": function (data, type, oObj) {
                return getDate(oObj["requestDate"]);
            }
        },
        {
            "data": "state",
            "sName": "state",
            "title": doc.getDocLabel("page_testcaseexecutionqueue", "state_col"),
            "sWidth": "110px",
            "mRender": function (data, type, obj) {
                var state = obj["state"] || "";
                var chip = {
                    QUEUED: "bg-sky-50 text-sky-700 ring-sky-600/20 dark:bg-sky-900/30 dark:text-sky-300",
                    QUEUED_PAUSED: "bg-amber-50 text-amber-700 ring-amber-600/20 dark:bg-amber-900/30 dark:text-amber-300",
                    QUWITHDEP: "bg-sky-50 text-sky-600 ring-sky-600/20 dark:bg-sky-900/20 dark:text-sky-400",
                    QUWITHDEP_PAUSED: "bg-amber-50 text-amber-700 ring-amber-600/20 dark:bg-amber-900/30 dark:text-amber-300",
                    WAITING: "bg-violet-50 text-violet-700 ring-violet-600/20 dark:bg-violet-900/30 dark:text-violet-300",
                    STARTING: "bg-blue-50 text-blue-700 ring-blue-600/20 dark:bg-blue-900/30 dark:text-blue-300",
                    EXECUTING: "bg-blue-50 text-blue-700 ring-blue-600/20 dark:bg-blue-900/30 dark:text-blue-300",
                    DONE: "bg-green-50 text-green-700 ring-green-600/20 dark:bg-green-900/30 dark:text-green-300",
                    CANCELLED: "bg-slate-50 text-slate-600 ring-slate-500/20 dark:bg-slate-800 dark:text-slate-300",
                    ERROR: "bg-red-50 text-red-700 ring-red-600/20 dark:bg-red-900/30 dark:text-red-300"
                }[state] || "bg-slate-50 text-slate-600 ring-slate-500/20 dark:bg-slate-800 dark:text-slate-300";
                return '<span class="inline-flex items-center rounded-full px-2.5 py-1 text-xs font-semibold ring-1 ring-inset ' + chip + '">' + escapeHtml(state) + '</span>';
            }
        },
        {
            "data": "comment",
            "like": true,
            "sName": "comment",
            "title": doc.getDocLabel("page_testcaseexecutionqueue", "comment_col"),
            "sWidth": "200px",
            "defaultContent": ""
        },
        {
            "data": "priority",
            "visible": false,
            "sName": "priority",
            "title": doc.getDocLabel("testcaseexecutionqueue", "priority"),
            "sWidth": "40px"
        },
        {
            "data": "exeId",
            "like": true,
            "sName": "exeId",
            "title": doc.getDocLabel("page_testcaseexecutionqueue", "exeId"),
            "sWidth": "40px",
            "defaultContent": "",
            "mRender": function (data, type, obj) {
                if (obj["exeId"] <= 0) {
                    return "";
                } else {
                    return '<a href="TestCaseExecution.jsp?executionId=' + obj["exeId"] + '">' + obj["exeId"] + '</a>';
                }
            }
        },
        {
            "data": "UsrCreated",
            "visible": false,
            "sName": "UsrCreated",
            "sWidth": "70px",
            "defaultContent": "",
            "title": doc.getDocOnline("transversal", "UsrCreated")
        },
        {
            "data": "DateCreated",
            "visible": false,
            "like": true,
            "sName": "DateCreated",
            "sWidth": "110px",
            "defaultContent": "",
            "title": doc.getDocOnline("transversal", "DateCreated"),
            "mRender": function (data, type, oObj) {
                return getDate(oObj["DateCreated"]);
            }
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
            "visible": false,
            "sName": "debugFlag",
            "title": doc.getDocLabel("testcaseexecutionqueue", "debugFlag"),
            "sWidth": "70px",
            "defaultContent": ""
        },
        {
            "data": "UsrModif",
            "visible": false,
            "sName": "UsrModif",
            "sWidth": "70px",
            "defaultContent": "",
            "title": doc.getDocOnline("transversal", "UsrModif")
        },
        {
            "data": "DateModif",
            "visible": false,
            "sName": "DateModif",
            "sWidth": "110px",
            "defaultContent": "",
            "title": doc.getDocOnline("transversal", "DateModif"),
            "mRender": function (data, type, oObj) {
                return getDate(oObj["DateModif"]);
            }
        }
    ];
    return aoColumns;
}

function aoColumnsFunc_followUp() {
    var doc = new Doc();
    var aoColumns = [
        {
            "data": null,
            "sWidth": "50px",
            "sSearchable": false,
            "sName": "action",
            "title": doc.getDocLabel("page_global", "columnAction"),
            "mRender": function (data, type, oObj) {
                const baseBtnClass = "inline-flex aspect-square h-8 w-8 items-center justify-center rounded-md transition-all duration-200 " +
                    "text-slate-500 hover:bg-slate-200 dark:hover:bg-slate-800 " +
                    "opacity-20 group-hover:opacity-100 [&_svg]:size-4 group-hover:!text-blue-500";
                function fuButton(title, onClick, icon) {
                    return '<button type="button" class="' + baseBtnClass + '" title="' + title + '" onclick="' + onClick + '">' +
                        '<i data-lucide="' + icon + '" class="w-4 h-4"></i></button>';
                }
                var editGlobalParam = fuButton(doc.getDocLabel("page_parameter", "editparameter_field"), 'openModalParameter(\'cerberus_queueexecution_global_threadpoolsize\',\'' + getSys() + '\');', 'pencil');
                var editRobotParam = fuButton(doc.getDocLabel("page_parameter", "editparameter_field"), 'openModalParameter(\'cerberus_queueexecution_defaultrobothost_threadpoolsize\',\'' + getSys() + '\');', 'pencil');
                var editRobotInvariant = fuButton(doc.getDocLabel("page_invariant", "button_edit"), 'openModalInvariant(\'ROBOTHOST\',\'' + data[5] + '\',\'EDIT\',\'tabInvAdvanced\');', 'pencil');
                var addRobotInvariant = fuButton(doc.getDocLabel("page_invariant", "button_create"), 'openModalInvariant(\'ROBOTHOST\',\'' + data[5] + '\',\'ADD\',\'tabInvAdvanced\');', 'plus');
                var editRobotExtParam = fuButton(doc.getDocLabel("page_parameter", "editparameter_field"), 'openModalParameter(\'cerberus_queueexecution_defaultexecutorexthost_threadpoolsize\',\'' + getSys() + '\');', 'pencil');
                var editRobotExtInvariant = fuButton(doc.getDocLabel("page_invariant", "button_edit"), 'openModalInvariant(\'ROBOTPROXYHOST\',\'' + data[5] + '\',\'EDIT\',\'tabInvAdvanced\');', 'pencil');
                var addRobotExtInvariant = fuButton(doc.getDocLabel("page_invariant", "button_create"), 'openModalInvariant(\'ROBOTPROXYHOST\',\'' + data[5] + '\',\'ADD\',\'tabInvAdvanced\');', 'plus');
                var editApplication = fuButton(doc.getDocLabel("page_invariant", "button_edit"), 'openModalApplication(\'' + data[4] + '\', \'EDIT\', \'ApplicationList\');', 'pencil');

                var buttons = "";
                if ((data[0] === "constrain1_global") && (data[9])) {
                    // Constrain is global and hasPermitionUpdate is true.
                    buttons += editGlobalParam;
                }
                if (((data[0] === "constrain2_applienvironment") || (data[0] === "constrain3_application")) && (data[9]))
                {
                    console.info(data);
                    // Constrain is global and hasPermitionUpdate is true.
                    buttons += editApplication;
                }
                if ((data[0] === "constrain4_robot") && (data[9])) {
                    // Constrain is global and hasPermitionUpdate is true.
                    if (data[10]) {
                        // Invariant exist. We can edit it.
                        buttons += editRobotInvariant;
                    } else if (!isEmpty(data[5]) && data[5] !== "null") {
                        //Invariant does not exist and is not null or empty. We can either create it or change default parameter.
                        buttons += editRobotParam;
                        buttons += addRobotInvariant;
                    }
                }
                if ((data[0] === "constrain5_proxyservice") && (data[9])) {
                    // Constrain is global and hasPermitionUpdate is true.
                    if (data[10]) {
                        // Invariant exist. We can edit it.
                        buttons += editRobotExtInvariant;
                    } else if (!isEmpty(data[5]) && data[5] !== "null") {
                        //Invariant does not exist and is not null or empty. We can either create it or change default parameter.
                        buttons += editRobotExtParam;
                        buttons += addRobotExtInvariant;
                    }
                }
                return '<div class="flex items-center justify-start gap-1">' + buttons + '</div>';
            }
        }
        ,
        {"data": "0", "sName": "constrainsId", "sWidth": "100px", "title": doc.getDocLabel("page_testcaseexecutionqueue", "constrain")},
        {"data": "1", "sName": "system", "sWidth": "50px", "title": doc.getDocLabel("invariant", "SYSTEM")},
        {"data": "2", "sName": "environment", "sWidth": "50px", "title": doc.getDocLabel("invariant", "ENVIRONMENT")},
        {"data": "3", "sName": "country", "sWidth": "50px", "title": doc.getDocLabel("invariant", "COUNTRY")},
        {"data": "4", "sName": "application", "sWidth": "50px", "title": doc.getDocLabel("application", "Application")},
        {"data": "5", "sName": "robot", "sWidth": "50px", "title": doc.getDocLabel("page_testcaseexecutionqueue", "robothost")},
        {"data": "6", "sName": "nbRunning", "sWidth": "50px", "title": doc.getDocLabel("page_testcaseexecutionqueue", "nbRunning")},
        {"data": "7", "sName": "nbPoolSize", "sWidth": "50px", "title": doc.getDocLabel("page_testcaseexecutionqueue", "nbPoolSize")},
        {"data": "8", "sName": "nbInQueue", "sWidth": "50px", "title": doc.getDocLabel("page_testcaseexecutionqueue", "nbInQueue")},
        {
            "data": null, "sName": "saturation", "sWidth": "50px", "title": doc.getDocLabel("page_testcaseexecutionqueue", "saturation"),
            "mRender": function (data, type, obj) {
                var saturation_level;
                var satcolor;
                if (obj[7] > 0) {
                    saturation_level = (obj[6] / obj[7]) * 100;
                    saturation_level = Math.round(saturation_level * 10) / 10

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
            "data": null, "sName": "oversaturation", "sWidth": "50px", "title": doc.getDocLabel("page_testcaseexecutionqueue", "oversaturation"),
            "mRender": function (data, type, obj) {
                if ((obj[7] > 0) && ((obj[6] >= obj[7]))) {
                    return obj[8];
                } else {
                    return "";
                }
            }
        }
    ];
    return aoColumns;
}

function getOptions(title, unit) {
    let option = {
        responsive: true,
        maintainAspectRatio: false,
        hover: {
            mode: 'nearest',
            intersect: true
        },
        tooltips: {
            callbacks: {
                label: function (t, d) {
                    var xLabel = d.datasets[t.datasetIndex].label;
                    return xLabel + ': ' + t.yLabel;
                }
            },
        },
        title: {
            text: title
        },
        scales: {
            xAxes: [{
                    type: 'time',
                    time: {
                        tooltipFormat: 'll HH:mm:ss'
                    },
                    scaleLabel: {
                        display: true,
                        labelString: 'Date'
                    }
                }],
            yAxes: [{
                    scaleLabel: {
                        display: true,
                        labelString: title
                    },
                    ticks: {
                        callback: function (value, index, values) {
                            return value;
                        }}

                }]
        }
    };
    return option;
}

function initGraph() {

    var queueStatoption = getOptions("", "nb");

    let queueStatdatasets = [];

    configQueueStat = {
        type: 'line',
        data: {
            datasets: queueStatdatasets
        },
        options: queueStatoption
    };

    var ctx = document.getElementById('canvasQueueStat').getContext('2d');
    window.myLineQueueStat = new Chart(ctx, configQueueStat);
}


function loadStatGraph() {
    showLoader($("#qsFilterPanel"));

    let from = new Date($('#frompicker').data("DateTimePicker").date());
    let to = new Date($('#topicker').data("DateTimePicker").date());

    let qS = "from=" + from.toISOString() + "&to=" + to.toISOString();
//    let qS = "from=2020-08-07T01:01:01.0Z&to=2020-08-07T16:14:01.0Z";

    $.ajax({
        url: "ReadQueueStat?" + qS,
        method: "GET",
        async: true,
        dataType: 'json',
        success: function (data) {
            var messageType = getAlertType(data.messageType);

            if (data.messageType === "OK") {
                buildGraphs(data);
            } else {
                showMessageMainPage(messageType, data.message, false);
            }
            hideLoader($("#qsFilterPanel"));
        },
        error: showUnexpectedError
    });
}


function buildGraphs(data) {

    let curves = data.datasetQueueStat;

    // Sorting values by nb of requests.
    sortedCurves = curves.sort(function (a, b) {
//        let a1 = a.key.testcase.test + "-" + a.key.testcase.testcase + "-" + a.key.unit + "-" + a.key.country + "-" + a.key.environment + "-" + a.key.robotdecli;
//        let b1 = b.key.testcase.test + "-" + b.key.testcase.testcase + "-" + b.key.unit + "-" + b.key.country + "-" + b.key.environment + "-" + a.key.robotdecli;
//        return b1.localeCompare(a1);
        return true;
    });

    var len = sortedCurves.length;

    let timedatasets = [];

    for (var i = 0; i < len; i++) {

        let c = sortedCurves[i];
        let d = [];
        lend = c.points.length;
        for (var j = 0; j < lend; j++) {
            let p = {x: c.points[j].x, y: c.points[j].y};
            d.push(p);
        }
        let lab = c.key.key;
        let doFill = false;
        if (c.key.key === "CurrentlyRunning") {
            doFill = true;
        }
        var dataset = {
            label: lab,
            backgroundColor: "white",
            borderColor: getColorQueueStat(c.key.key),
            pointBackgroundColor: getColorQueueStat(c.key.key),
            pointRadius: 1,
            pointHoverRadius: 6,
            hitRadius: 10,
            fill: doFill,
            lineTension: 0,
            data: d
        };
        timedatasets.push(dataset);
    }

    if (timedatasets.length > 0) {
        $("#panelQueueStat").show();
    } else {
        $("#panelQueueStat").hide();
    }
    configQueueStat.data.datasets = timedatasets;

    window.myLineQueueStat.update();
}

function getColorQueueStat(name) {
    switch (name) {
        case "CurrentlyRunning":
            return "green";
        case "GlobalConstrain":
            return "red";
        case "QueueSize":
            return "darkblue";
    }
    return "red";

}
