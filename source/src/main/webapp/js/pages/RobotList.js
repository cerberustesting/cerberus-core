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
    var configurations = new TableConfigurationsServerSide("robotsTable", "./api/robots/read", "contentTable", aoColumnsFunc("robotsTable"), [2, 'asc']);

    var table = createDataTableWithPermissionsNew(configurations, renderOptionsForRobot, "#robotList", undefined, true);

    $('[data-toggle="popover"]').popover({
        'placement': 'auto',
        'container': 'body'}
    );

    $('#robotsTable tbody').on('click', 'td.details-control', function () {
        var tr = $(this).closest('tr');
        var row = table.row(tr);

        if (row.child.isShown()) {
            row.child.hide();
            tr.removeClass('shown');
        } else {
            row.child(formatExecutors(row.data())).show();
            tr.addClass('shown');
        }

        if (window.lucide) {
            lucide.createIcons();
        }
    });

    $('#robotsTable').on('draw.dt', function() {
        $(this).find('tbody tr').addClass('group');
    });
}

function displayPageLabel() {
    var doc = new Doc();

    //displayHeaderLabel(doc);
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
            var contentToAdd = "";

            // Bouton Create
            contentToAdd += `
            <button id='createRobotButton' type='button'
                class='bg-sky-400 hover:bg-sky-500 flex items-center space-x-1 px-3 py-1 rounded-lg h-10 w-auto'>
                <span class='glyphicon glyphicon-plus-sign'></span>
                <span>${doc.getDocLabel("page_robot", "button_create")}</span>
            </button>
            `;

            var $wrapper = $("#robotsTable_buttonWrapper");
            if ($wrapper.length) {
                // Ajoute le bouton au **début** du wrapper
                $wrapper.append(contentToAdd);
            } else {
                // fallback si le wrapper n’existe pas encore
                console.warn("Wrapper #robotsTable_buttonWrapper introuvable, insertion avant length");
                $("#robotsTable_wrapper div#testTable_length").before("<div id='robotsTable_buttonWrapper' class='flex w-full gap-2'>" + contentToAdd + "</div>");
            }
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
        {
            "className": "details-control",
            "orderable": false,
            "data": null,
            "defaultContent": `<i data-lucide="chevron-right" class="mt-2 executor-chevron"></i>`,
            "width": "30px",
            "bSortable": false,
            "bSearchable": false
        },
        {
            "data": null,
            "title": doc.getDocLabel("page_global", "columnAction"),
            "sWidth": "100px",
            "bSortable": false,
            "bSearchable": false,
            "mRender": function (data, type, obj) {

                const hasPermissions = $("#" + tableId).attr("hasPermissions") === "true";

                // Style bouton “fantôme” avec hover
                const baseBtnClass = "inline-flex aspect-square h-8 w-8 items-center justify-center rounded-md transition-all duration-200 " +
                    "text-slate-500 hover:bg-slate-200 dark:hover:bg-slate-800 " +
                    "opacity-20 group-hover:opacity-100 [&_svg]:size-4";

                function actionButton({ id, name, title, onClick, icon, extraClass = "" }) {
                    return `
                <button
                    id="${id}" name="${name}"
                    type="button" class="${baseBtnClass} ${extraClass}"
                    title="${title}" onclick="${onClick}">
                    ${icon}
                </button>
            `;
                }

                const icons = {
                    edit: `<i data-lucide="pencil" class="w-4 h-4"></i>`,
                    view: `<i data-lucide="eye" class="w-4 h-4"></i>`,
                    duplicate: `<i data-lucide="copy" class="w-4 h-4"></i>`,
                    delete: `<i data-lucide="trash-2" class="w-4 h-4"></i>`
                };

                let buttons = [];

                if (hasPermissions) {
                    buttons.push(
                        actionButton({
                            id: `editEntry${obj.robotID}`,
                            name: "editEntry",
                            title: doc.getDocLabel("page_robot", "button_edit"),
                            onClick: `openModalRobot('${obj.robot}','EDIT')`,
                            icon: icons.edit
                        }),
                        actionButton({
                            id: `duplicateEntry${obj.robotID}`,
                            name: "duplicateEntry",
                            title: doc.getDocLabel("page_robot", "button_duplicate"),
                            onClick: `openModalRobot('${obj.robot}','DUPLICATE')`,
                            icon: icons.duplicate
                        }),
                        actionButton({
                            id: `deleteEntry${obj.robotID}`,
                            name: "deleteEntry",
                            title: doc.getDocLabel("page_robot", "button_delete"),
                            onClick: `deleteEntryClick('${obj.robotID}','${obj.robot}')`,
                            icon: icons.delete,
                            extraClass: "group-hover:!text-red-500"
                        })
                    );
                } else {
                    buttons.push(
                        actionButton({
                            id: `viewEntry${obj.robotID}`,
                            name: "viewEntry",
                            title: doc.getDocLabel("page_robot", "button_view"),
                            onClick: `openModalRobot('${obj.robot}','EDIT')`,
                            icon: icons.view
                        })
                    );
                }

                return `<div class="flex items-center gap-1 justify-center">${buttons.join("")}</div>`;
            }
        },
        {
            "data": "robot",
            "sWidth": "200px",
            "className": "font-mono",
            "title": "Robot",
            "mRender": function (data, type, obj) {
                return data;
            }
        },
        {"data": "type",
            "sName": "type",
            "visible": false,
            "sWidth": "50px",
            "title": "type"
        },
        {"data": "platform",
            "sName": "platform",
            "sWidth": "70px",
            "title": "platform",
            "className": "text-center",
            "mRender": function (data, type, obj) {
                return `
            <div class="flex flex-col items-center justify-center gap-1">
                <img src="./images/platform-${obj.platform}.png"
                    class="h-5" alt="${obj.platform}"/>
                <span class="text-xs font-medium text-gray-600 dark:text-gray-400">
                    ${obj.platform}
                </span>
            </div>
        `;
            }
        },
        {
            "data": "browser",
            "sName": "browser",
            "sWidth": "70px",
            "title": "browser",
            "className": "text-center",
            "mRender": function (data, type, obj) {
                if (!obj.browser) {
                    return "";
                }

                return `
                    <div class="flex flex-col items-center justify-center gap-1">
                        <img
                            src="./images/browser-${obj.browser}.png"
                            class="h-5"
                            alt="${obj.browser}"
                        />
                        <span class="text-xs font-medium text-gray-600 dark:text-gray-400">
                            ${obj.browser}
                        </span>
                    </div>
                `;
            }
        },
        {
            "data": null,
            "title": "Executors",
            "orderable": false,
            "mRender": function (data, type, obj) {

                var total = obj.executors?.length || 0;
                var running = obj.executors?.filter(e => e.isActive).length || 0;
                var percent = total > 0 ? Math.round((running / total) * 100) : 0;

                var statusClass = running > 0 ? "active" : "idle";

                return `
        <div class="flex items-center gap-3">
            <div class="flex items-center gap-2">
                <div class="status-indicator ${statusClass}"></div>
                <span class="font-mono text-sm">
                    <span class="text-success font-semibold">${running}</span>
                    <span class="text-gray-500"> / ${total}</span>
                </span>
            </div>

            <div class="w-16 h-1.5 bg-gray-300 rounded-full overflow-hidden">
                <div class="h-full bg-gradient-to-r from-teal-500 to-green-500
                            rounded-full transition-all duration-500"
                     style="width: ${percent}%;">
                </div>
            </div>
        </div>`;
            }
        },
        {"data": "version",
            "sName": "version",
            "visible": false,
            "sWidth": "50px",
            "title": doc.getDocOnline("robot", "version")},
        {
            "data": "isActive",
            "sName": "isActive",
            "sWidth": "50px",
            "title": "Active",
            "orderable": true,
            "mRender": function (data, type) {

                if (type !== 'display') {
                    return data;
                }

                if (data === true) {
                    return `
                        <span class="inline-flex items-center rounded-full border px-2.5 py-0.5
                                     text-xs font-semibold transition-colors
                                     border-transparent bg-teal-500 text-teal-900
                                     hover:bg-teal-500/80">Active
                        </span>`;
                }
                return `
                    <span class="inline-flex items-center rounded-full border px-2.5 py-0.5
                                 text-xs font-semibold transition-colors
                                 border-transparent bg-slate-500 text-slate-900
                                 hover:bg-slate-500/80">
                        Inactive
                    </span>`;
            }
        },
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
            "sWidth": "280px",
            "title": "Description"}
    ];
    return aoColumns;
}

function formatExecutors(robot) {

    if (!robot.executors || robot.executors.length === 0) {
        return `<div class="p-4 text-sm text-gray-500">No executor</div>`;
    }

    let html = `
        <div class="p-4 pl-12">
            <h4 class="text-sm font-medium mb-3 text-gray-500">
                Executors (${robot.executors.length})
            </h4>
            <div class="grid gap-2">
    `;

    robot.executors.forEach(function (exe) {

        const isActive = exe.isActive === true;
        const status = isActive ? "running" : "stopped";

        html += `
            <div class="flex items-center justify-between p-3 rounded-lg
                        bg-card/50 border border-border/50">

                <div class="flex items-center gap-4">

                    <!-- Status dot -->
                    <div class="w-2.5 h-2.5 rounded-full ${isActive ? "bg-emerald-500" : "bg-muted"}"></div>

                    <!-- Host -->
                    <span class="font-mono text-sm">${exe.host}:${exe.port}</span>

                    <!-- Status badge -->
                    <span class="inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-semibold capitalize
                        ${isActive ? "bg-teal-500 text-teal-900" : "border"}">
                        ${status}
                    </span>
                </div>

                <!-- VNC button -->
                <button class="vnc-button inline-flex items-center gap-1.5 text-sm
                           ${isActive ? "" : "opacity-50 cursor-not-allowed"}" type="button" style="display:none"
                    ${isActive ? `onclick="openVNC('${exe.host}','${exe.port}')"` : "disabled"}>
                    <svg xmlns="http://www.w3.org/2000/svg"
                         viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"
                         class="lucide lucide-monitor w-3.5 h-3.5">
                        <rect width="20" height="14" x="2" y="3" rx="2"></rect>
                        <line x1="8" x2="16" y1="21" y2="21"></line>
                        <line x1="12" x2="12" y1="17" y2="21"></line>
                    </svg>VNC
                </button>
            </div>
        `;
    });

    html += `
            </div>
        </div>
    `;

    return html;
}

