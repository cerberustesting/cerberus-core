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
$.when($.getScript("js/global/global.js")).then(function () {
    $(document).ready(function () {
        initPage();
    });
});

function initPage() {
    var doc = new Doc();

    displayPageLabel();

    initModalRobot();
    $('#editRobotModal').data("initLabel", true);

    $("[name=screensize]").append($('<option></option>').text(doc.getDocLabel("page_runtest", "default_full_screen")).val(""));

    //clear the modals fields when closed
    $('#editEntryModal').on('hidden.bs.modal', {extra: "#editEntryModal"}, buttonCloseHandler);

    //configure and create the dataTable
    var configurations = new TableConfigurationsServerSide("robotsTable", "ReadRobot", "contentTable", aoColumnsFunc("robotsTable"), [1, 'asc']);

    createDataTableWithPermissions(configurations, renderOptionsForRobot, "#robotList", undefined, true);

    $('[data-toggle="popover"]').popover({
        'placement': 'auto',
        'container': 'body'}
    );
}

function displayPageLabel() {
    var doc = new Doc();

    displayHeaderLabel(doc);
    displayGlobalLabel(doc);
    $("#pageTitle").html(doc.getDocLabel("page_robot", "title"));
    $("#title").html(doc.getDocOnline("page_robot", "title"));

    displayFooter(doc);
}

function renderOptionsForRobot(data) {
    var doc = new Doc();
    //check if user has permissions to perform the add and import operations
    if (data["hasPermissions"]) {
        if ($("#createRobotButton").length === 0) {
            var contentToAdd = "<div class='marginBottom10'><button id='createRobotButton' type='button' class='btn btn-default'>\n\
            <span class='glyphicon glyphicon-plus-sign'></span> " + doc.getDocLabel("page_robot", "button_create") + "</button></div>";

            $("#robotsTable_wrapper #robotsTable_length").before(contentToAdd);
            $("#robotList #createRobotButton").off("click");
            $("#robotList #createRobotButton").click(function () {
                openModalRobot(undefined, "ADD");
            });
        }
    }
}

function deleteEntryHandlerClick() {
    var robotID = $('#confirmationModal').find('#hiddenField1').prop("value");
    var jqxhr = $.post("DeleteRobot", {robotid: robotID}, "json");
    $.when(jqxhr).then(function (data) {
        var messageType = getAlertType(data.messageType);
        if (messageType === "success") {
            //redraw the datatable
            var oTable = $("#robotsTable").dataTable();
            oTable.fnDraw(false);
            var info = oTable.fnGetData().length;

            if (info === 1) {//page has only one row, then returns to the previous page
                oTable.fnPageChange('previous');
            }

        }
        //show message in the main page
        showMessageMainPage(messageType, data.message, false);
        //close confirmation window
        $('#confirmationModal').modal('hide');
    }).fail(handleErrorAjaxAfterTimeout);
}

function deleteEntryClick(entry, name) {
    clearResponseMessageMainPage();
    var doc = new Doc();
    var messageComplete = doc.getDocLabel("page_global", "message_delete");
    messageComplete = messageComplete.replace("%TABLE%", doc.getDocLabel("robot", "robot"));
    messageComplete = messageComplete.replace("%ENTRY%", name);
    showModalConfirmation(deleteEntryHandlerClick, undefined, doc.getDocLabel("page_robot", "button_delete"), messageComplete, entry, "", "", "");
}

function buttonCloseHandler(event) {
    var modalID = event.data.extra;
    // reset form values
    $(modalID + " " + modalID + "Form")[0].reset();
    // remove all errors on the form fields
    $(this).find('div.has-error').removeClass("has-error");
    // clear the response messages of the modal
    clearResponseMessage($(modalID));
}

function aoColumnsFunc(tableId) {
    var doc = new Doc();

    var aoColumns = [
        {"data": null,
            "title": doc.getDocLabel("page_global", "columnAction"),
            "sWidth": "90px",
            "bSortable": false,
            "bSearchable": false,
            "mRender": function (data, type, obj) {
                var hasPermissions = $("#" + tableId).attr("hasPermissions");
                var editEntry = '<button id="editEntry" onclick="openModalRobot(\'' + obj["robot"] + '\',\'EDIT\');"\n\
                                    class="editEntry btn btn-default btn-xs margin-right5" \n\
                                    name="editEntry" title="' + doc.getDocLabel("page_robot", "button_edit") + '" type="button">\n\
                                    <span class="glyphicon glyphicon-pencil"></span></button>';
                var viewEntry = '<button id="editEntry" onclick="openModalRobot(\'' + obj["robot"] + '\',\'EDIT\');"\n\
                                    class="editEntry btn btn-default btn-xs margin-right5" \n\
                                    name="editEntry" title="' + doc.getDocLabel("page_robot", "button_edit") + '" type="button">\n\
                                    <span class="glyphicon glyphicon-eye-open"></span></button>';
                var duplicateEntry = '<button id="editEntry" onclick="openModalRobot(\'' + obj["robot"] + '\',\'DUPLICATE\');"\n\
                                    class="editEntry btn btn-default btn-xs margin-right5" \n\
                                    name="editEntry" title="' + doc.getDocLabel("page_robot", "button_duplicate") + '" type="button">\n\
                                    <span class="glyphicon glyphicon-duplicate"></span></button>';
                var deleteEntry = '<button id="deleteEntry" onclick="deleteEntryClick(\'' + obj["robotID"] + '\',\'' + obj["robot"] + '\');" \n\
                                    class="deleteEntry btn btn-default btn-xs margin-right5" \n\
                                    name="deleteEntry" title="' + doc.getDocLabel("page_robot", "button_delete") + '" type="button">\n\
                                    <span class="glyphicon glyphicon-trash"></span></button>';
                if (hasPermissions === "true") { //only draws the options if the user has the correct privileges
                    return '<div class="center btn-group width150">' + editEntry + duplicateEntry + deleteEntry + '</div>';
                }
                return '<div class="center btn-group width150">' + viewEntry + '</div>';
            }
        },
        {"data": "robot",
            "sName": "robot",
            "sWidth": "50px",
            "title": doc.getDocOnline("robot", "robot")},
        {"data": "type",
            "sName": "type",
            "visible": false,
            "sWidth": "50px",
            "title": doc.getDocOnline("robot", "type")},
        {"data": "platform",
            "sName": "platform",
            "sWidth": "50px",
            "title": doc.getDocOnline("robot", "platform"),
            "mRender": function (data, type, obj) {
                return $("<div></div>")
                        .append($("<img style='height:30px; overflow:hidden; text-overflow:clip; border: 0px; padding:0; margin:0'></img>").text(obj.platform).attr('src', './images/platform-' + obj.platform + '.png'))
                        .append($("<span></span>").text(" " + obj.platform))
                        .html();
            }
        },
        {"data": "browser",
            "sName": "browser",
            "sWidth": "50px",
            "title": doc.getDocOnline("robot", "browser"),
            "mRender": function (data, type, obj) {
                if (obj.browser !== "") {
                    return $("<div></div>")
                            .append($("<img style='height:30px; overflow:hidden; text-overflow:clip; border: 0px; padding:0; margin:0'></img>").text(obj.browser).attr('src', './images/browser-' + obj.browser + '.png'))
                            .append($("<span></span>").text(" " + obj.browser))
                            .html();
                } else {
                    return "";
                }
            }
        },
        {"data": "version",
            "sName": "version",
            "visible": false,
            "sWidth": "50px",
            "title": doc.getDocOnline("robot", "version")},
        {"data": "isActive",
            "sName": "isActive",
            "sWidth": "30px",
            "title": doc.getDocOnline("robot", "active")},
        {"data": "userAgent",
            "sName": "userAgent",
            "visible": false,
            "sWidth": "50px",
            "title": doc.getDocOnline("robot", "useragent")},
        {"data": "screenSize",
            "sName": "screenSize",
            "visible": false,
            "sWidth": "50px",
            "title": doc.getDocOnline("robot", "screensize")},
        {"data": "robotDecli",
            "sName": "robotDecli",
            "visible": false,
            "sWidth": "50px",
            "title": doc.getDocOnline("robot", "robotdecli")},
        {"data": "lbexemethod",
            "sName": "lbexemethod",
            "visible": false,
            "sWidth": "50px",
            "title": doc.getDocOnline("robot", "lbexemethod")},
        {"data": "description",
            "sName": "description",
            "sWidth": "80px",
            "title": doc.getDocOnline("robot", "description")}
    ];
    return aoColumns;
}