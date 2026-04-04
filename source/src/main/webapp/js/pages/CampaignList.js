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

        $('[data-toggle="popover"]').popover({
            'placement': 'auto',
            'container': 'body'}
        );
    });
});

function initPage() {
    displayPageLabel();

    displayInvariantList("screenshot", "SCREENSHOT", false, undefined, "");
    displayInvariantList("video", "VIDEO", false, undefined, "");
    displayInvariantList("verbose", "VERBOSE", false, undefined, "");
    displayInvariantList("pageSource", "PAGESOURCE", false, undefined, "");
    displayInvariantList("robotLog", "ROBOTLOG", false, undefined, "");
    displayInvariantList("consoleLog", "CONSOLELOG", false, undefined, "");
    displayInvariantList("retries", "RETRIES", false, undefined, "");
    displayInvariantList("manualExecution", "MANUALEXECUTION", false, undefined, "");
    // Pre load eventconnector invariant.
    getSelectInvariant("EVENTCONNECTOR", false);

    $('#testcampaignsTable').on('draw.dt', function() {
        $(this).find('tbody tr').addClass('group');
        if (window.lucide) lucide.createIcons();
    });

    // Redraw datatables inside modal when switching tabs via Alpine
    $('#editTestcampaignModal').on('shown.bs.modal', function () {
        if (window.lucide) lucide.createIcons();
    });

    $(document).on('click', '#editTestcampaignModal [x-data] button', function() {
        setTimeout(function() {
            if ($("#parameterTestcampaignsTable_wrapper").length > 0) $("#parameterTestcampaignsTable").DataTable().columns.adjust().draw();
            if ($("#labelTestcampaignsTable_wrapper").length > 0) $("#labelTestcampaignsTable").DataTable().columns.adjust().draw();
            if ($("#parameterTestcaseTable_wrapper").length > 0) $("#parameterTestcaseTable").DataTable().columns.adjust().draw();
        }, 100);
    });

    $("#viewTestcampaignModal").on('shown.bs.modal', function (e) {
        $("#viewTestcampaignsTable").DataTable().columns.adjust();
    })

    //configure and create the dataTable
    var configurations = new TableConfigurationsServerSide("testcampaignsTable", "ReadCampaign", "contentTable", aoColumnsFunc("testcampaignsTable"), [1, 'asc']);
    createDataTableWithPermissionsNew(configurations, renderOptionsForCampaign, "#testcampaignList", undefined, true);
}

function displayPageLabel() {
    var doc = new Doc();

    $("#pageTitle").html(doc.getDocLabel("page_testcampaign", "title"));
    $("#title").html(doc.getDocLabel("page_testcampaign", "allTestcampaigns"));
    $("[name='editTestcampaignField']").html(doc.getDocLabel("page_testcampaign", "edittestcampaign_field"));
    $("[name='addTestcampaignField']").html(doc.getDocLabel("page_testcampaign", "addtestcampaign_field"));
    $("[name='campaignField']").html(doc.getDocLabel("page_testcampaign", "campaign_field"));
    $("[name='tagField']").html(doc.getDocOnline("campaign", "tag"));

    $("[name='cIScoreThresholdField']").html(doc.getDocOnline("campaign", "CIScoreThreshold"));
    $("[name='longDescriptionField']").html(doc.getDocOnline("campaign", "longDescription"));

    $("[name='descriptionField']").html(doc.getDocOnline("page_testcampaign", "description_field"));
    $("[name='tabDescription']").html(doc.getDocLabel("page_testcampaign", "description_tab"));
    $("[name='tabLabels']").html(doc.getDocLabel("label", "label"));
    $("[name='tabParameters']").html(doc.getDocLabel("page_testcampaign", "parameter_tab"));
    $("[name='buttonClose']").html(doc.getDocLabel("page_testcampaign", "close_btn"));
    $("[name='buttonAdd']").html(doc.getDocLabel("page_testcampaign", "button_create"));
    $("[name='buttonEdit']").html(doc.getDocLabel("page_testcampaign", "save_btn"));

    $("[name='distriblistField']").html(doc.getDocOnline("testcampaign", "distribList"));
    $("[name='notifystartField']").html(doc.getDocOnline("testcampaign", "notifyStartTagExecution"));
    $("[name='notifyendField']").html(doc.getDocOnline("testcampaign", "notifyEndTagExecution"));

    $("[name='webhookField']").html(doc.getDocOnline("testcampaign", "SlackWebhook"));
    $("[name='channelField']").html(doc.getDocOnline("testcampaign", "SlackChannel"));
    $("[name='notifySlackstartField']").html(doc.getDocOnline("testcampaign", "SlackNotifyStartTagExecution"));
    $("[name='notifySlackendField']").html(doc.getDocOnline("testcampaign", "SlackNotifyEndTagExecution"));

    //displayHeaderLabel(doc);

    displayFooter(doc);
    displayGlobalLabel(doc);
}

function renderOptionsForCampaign(data) {
    var doc = new Doc();
    if (data["hasPermissions"]) {
        if ($("#createTestcampaignButton").length === 0) {
            var contentToAdd = "";
            // Bouton Create
            contentToAdd += `
            <button id='createTestcampaignButton' type='button'
                class='bg-sky-400 hover:bg-sky-500 flex items-center space-x-1 px-3 py-1 rounded-lg h-10 w-auto'>
                <i data-lucide="plus" class="w-4 h-4"></i>
                <span>${doc.getDocLabel("page_testcampaign", "button_create")}</span>
            </button>
            `;

            var $wrapper = $("#testcampaignsTable_buttonWrapper");
            if ($wrapper.length) {
                $wrapper.append(contentToAdd);
                if (window.lucide) lucide.createIcons();
            } else {
                console.warn("Wrapper #testcampaignsTable_buttonWrapper introuvable, insertion avant length");
                $("#testcampaignsTable_wrapper div#testcampaignsTable_length").before("<div id='testcampaignsTable_buttonWrapper' class='flex w-full gap-2'>" + contentToAdd + "</div>");
            }
            $('#testcampaignList #createTestcampaignButton').click(addEntryClick);
        }
    }
}

async function removeEntryClick(key) {
    var doc = new Doc();
    var messageComplete = doc.getDocLabel("page_testcampaign", "message_remove").replace("%NAME%", key);

    const result = await crbConfirmDelete({
        title: doc.getDocLabel("page_testcampaign", "title_remove"),
        html: messageComplete,
        confirmText: doc.getDocLabel("page_global", "btn_delete") || 'Delete',
        cancelText: doc.getDocLabel("page_global", "buttonClose") || 'Cancel',
        preConfirm: async () => {
            try {
                const resp = await fetch("DeleteCampaign?key=" + encodeURIComponent(key), {
                    method: "GET"
                });
                const data = await resp.json();
                if (getAlertType(data.messageType) !== "success") {
                    Swal.showValidationMessage(data.message || "Delete failed");
                    return null;
                }
                return data;
            } catch (e) {
                Swal.showValidationMessage("Unexpected error");
                return null;
            }
        }
    });

    if (result.isConfirmed && result.value) {
        var oTable = $("#testcampaignsTable").dataTable();
        oTable.fnDraw(false);
        showMessage(result.value);
    }
}

function aoColumnsFunc(tableId) {
    var doc = new Doc();
    var aoColumns = [
        {
            "data": null,
            "bSortable": false,
            "bSearchable": false,
            "sWidth": "170px",
            "title": doc.getDocOnline("page_testcampaign", "button_col"),
            "mRender": function (data, type, obj, meta) {
                var hasPermissions = ($("#" + tableId).attr("hasPermissions") === "true");
                var row = "row_" + (meta ? meta.row : 0);

                const baseBtnClass = "inline-flex aspect-square h-8 w-8 items-center justify-center rounded-md transition-all duration-200 " +
                    "text-slate-500 hover:bg-slate-200 dark:hover:bg-slate-800 " +
                    "opacity-20 group-hover:opacity-100 [&_svg]:size-4";

                function actionButton({ id, name, title, onClick, icon, extraClass = "", disabled = false, href = null }) {
                    if (href) {
                        return `<a id="${id}" name="${name}" class="${baseBtnClass} ${extraClass}" title="${title}" href="${href}">
                            ${icon}</a>`;
                    }
                    const disabledClass = disabled ? "opacity-30 cursor-not-allowed" : "";
                    return `<button id="${id}" name="${name}" type="button"
                        class="${baseBtnClass} ${extraClass} ${disabledClass}"
                        title="${title}"
                        ${disabled ? "disabled" : `onclick="${onClick}"`}>
                        ${icon}
                    </button>`;
                }

                const icons = {
                    edit: '<i data-lucide="pencil" class="w-4 h-4"></i>',
                    view: '<i data-lucide="eye" class="w-4 h-4"></i>',
                    delete: '<i data-lucide="trash-2" class="w-4 h-4"></i>',
                    list: '<i data-lucide="list" class="w-4 h-4"></i>',
                    stats: '<i data-lucide="bar-chart-3" class="w-4 h-4"></i>',
                    play: '<i data-lucide="play" class="w-4 h-4"></i>'
                };

                let buttons = [];

                // Edit
                buttons.push(actionButton({
                    id: "editTestcampaign_" + row,
                    name: "editTestcampaign",
                    title: doc.getDocLabel("page_testcampaign", "button_edit"),
                    onClick: "editEntryClick('" + escapeHtml(obj["campaign"]) + "');",
                    icon: hasPermissions ? icons.edit : icons.view
                }));

                // Delete
                if (hasPermissions) {
                    buttons.push(actionButton({
                        id: "removeTestcampaign_" + row,
                        name: "removeTestcampaign",
                        title: doc.getDocLabel("page_testcampaign", "button_remove"),
                        onClick: "removeEntryClick('" + escapeHtml(obj["campaign"]) + "');",
                        icon: icons.delete,
                        extraClass: "group-hover:!text-red-500"
                    }));
                }

                // View TestCases
                buttons.push(actionButton({
                    id: "viewTestcampaign_" + row,
                    name: "viewTestcampaign",
                    title: doc.getDocLabel("page_testcampaign", "button_testcaselist"),
                    onClick: "viewEntryClick('" + escapeHtml(obj["campaign"]) + "');",
                    icon: icons.list
                }));

                // View Stats
                buttons.push(actionButton({
                    id: "viewStatcampaign_" + row,
                    name: "viewStatcampaign",
                    title: doc.getDocLabel("page_testcampaign", "button_taglist"),
                    onClick: "viewStatEntryClick('" + escapeHtml(obj["campaign"]) + "');",
                    icon: icons.stats
                }));

                // Run
                buttons.push(actionButton({
                    id: "runcampaign_" + row,
                    name: "runcampaign",
                    title: doc.getDocLabel("page_testcampaign", "button_run"),
                    href: "./RunTests.jsp?campaign=" + encodeURIComponent(obj["campaign"]),
                    icon: icons.play,
                    extraClass: "group-hover:!text-green-500"
                }));

                return '<div class="flex items-center gap-0.5">' + buttons.join('') + '</div>';
            }
        },
        {
            "data": "campaign",
            "sName": "campaign",
            "sWidth": "80px",
            "title": doc.getDocOnline("page_testcampaign", "testcampaign_col")
        },
        {
            "data": "description",
            "sName": "description",
            "sWidth": "180px",
            "title": doc.getDocOnline("page_testcampaign", "description_col")
        },
        {
            "data": "longDescription",
            "visible": false,
            "sName": "longDescription",
            "sWidth": "180px",
            "title": doc.getDocOnline("campaign", "longDescription")
        },
        {
            "data": "CIScoreThreshold",
            "visible": false,
            "sName": "CIScoreThreshold",
            "sWidth": "180px",
            "title": doc.getDocOnline("campaign", "CIScoreThreshold")
        },
        {
            "data": "group1",
            "visible": false,
            "sName": "group1",
            "sWidth": "80px",
            "title": doc.getDocOnline("campaign", "Group1")
        },
        {
            "data": "group2",
            "visible": false,
            "sName": "group2",
            "sWidth": "80px",
            "title": doc.getDocOnline("campaign", "Group2")
        },
        {
            "data": "group3",
            "visible": false,
            "sName": "group3",
            "sWidth": "80px",
            "title": doc.getDocOnline("campaign", "Group3")
        },
        {
            "data": "Tag",
            "visible": false,
            "sName": "Tag",
            "sWidth": "80px",
            "title": doc.getDocOnline("campaign", "tag")
        },
        {
            "data": "Verbose",
            "visible": false,
            "sName": "Verbose",
            "sWidth": "80px",
            "title": doc.getDocOnline("campaign", "Verbose")
        },
        {
            "data": "Screenshot",
            "visible": false,
            "sName": "Screenshot",
            "sWidth": "80px",
            "title": doc.getDocOnline("campaign", "Screenshot")
        },
        {
            "data": "Video",
            "visible": false,
            "sName": "Video",
            "sWidth": "80px",
            "title": doc.getDocOnline("campaign", "Video")
        },
        {
            "data": "PageSource",
            "visible": false,
            "sName": "PageSource",
            "sWidth": "80px",
            "title": doc.getDocOnline("campaign", "PageSource")
        },
        {
            "data": "RobotLog",
            "visible": false,
            "sName": "RobotLog",
            "sWidth": "80px",
            "title": doc.getDocOnline("campaign", "RobotLog")
        },
        {
            "data": "ConsoleLog",
            "visible": false,
            "sName": "ConsoleLog",
            "sWidth": "80px",
            "title": doc.getDocOnline("campaign", "ConsoleLog")
        },
        {
            "data": "Timeout",
            "visible": false,
            "sName": "Timeout",
            "sWidth": "80px",
            "title": doc.getDocOnline("campaign", "Timeout")
        },
        {
            "data": "Retries",
            "visible": false,
            "sName": "Retries",
            "sWidth": "80px",
            "title": doc.getDocOnline("campaign", "Retries")
        },
        {
            "data": "Priority",
            "visible": false,
            "sName": "Priority",
            "sWidth": "80px",
            "title": doc.getDocOnline("campaign", "Priority")
        },
        {
            "data": "ManualExecution",
            "visible": false,
            "sName": "ManualExecution",
            "sWidth": "80px",
            "title": doc.getDocOnline("campaign", "ManualExecution")
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
