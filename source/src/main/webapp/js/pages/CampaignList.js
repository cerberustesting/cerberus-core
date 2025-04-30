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

    $('#editTestcampaignModal a[data-toggle="tab"]').on('shown.bs.tab', function (e) {
        var target = $(e.target).attr("href"); // activated tab
        if (target == "#tabsCreate-1") {
        } else if (target == "#tabsCreate-3") {
            $("#parameterTestcampaignsTable").DataTable().draw();
        } else if (target == "#tabsCreate-4") {
            $("#labelTestcampaignsTable").DataTable().draw();
        } else if (target == "#tabsCreate-5") {
            $("#parameterTestcaseTable").DataTable().draw();
        }
    });

    $('#addTestcampaignModal a[data-toggle="tab"]').on('shown.bs.tab', function (e) {
        var target = $(e.target).attr("href"); // activated tab
        if (target == "#tabsCreate-11") {
        } else if (target == "#tabsCreate-13") {
            $("#addModalParameterTestcampaignsTable").DataTable().draw();
        } else if (target == "#tabsCreate-14") {
            $("#addModalLabelTestcampaignsTable").DataTable().draw();
        } else if (target == "#tabsCreate-5") {
            $("#parameterTestcaseTable").DataTable().draw();
        }
    });

    $("#viewTestcampaignModal").on('shown.bs.modal', function (e) {
        $("#viewTestcampaignsTable").DataTable().columns.adjust();
    })

    //configure and create the dataTable
    var configurations = new TableConfigurationsServerSide("testcampaignsTable", "ReadCampaign", "contentTable", aoColumnsFunc(), [1, 'asc']);
    createDataTableWithPermissions(configurations, renderOptionsForCampaign, "#testcampaignList", undefined, true);
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

    displayHeaderLabel(doc);

    displayFooter(doc);
    displayGlobalLabel(doc);
}

function renderOptionsForCampaign(data) {
    var doc = new Doc();
    if (data["hasPermissions"]) {
        if ($("#createTestcampaignButton").length === 0) {
            var contentToAdd = "<div class='marginBottom10'><button id='createTestcampaignButton' type='button' class='btn btn-default'>\n\
            <span class='glyphicon glyphicon-plus-sign'></span> " + doc.getDocLabel("page_testcampaign", "button_create") + "</button></div>";
            $("#testcampaignsTable_wrapper div#testcampaignsTable_length").before(contentToAdd);
            $('#testcampaignList #createTestcampaignButton').click(addEntryClick);
        }
    }
}

function removeEntryClick(key) {
    var doc = new Doc();
    showModalConfirmation(function (ev) {
        var id = $('#confirmationModal #hiddenField1').prop("value");
        $.ajax({
            url: "DeleteCampaign?key=" + encodeURIComponent(key),
            async: true,
            method: "GET",
            success: function (data) {
                hideLoaderInModal('#removeTestampaignModal');
                var oTable = $("#testcampaignsTable").dataTable();
                oTable.fnDraw(false);
                $('#removeTestcampaignModal').modal('hide');
                showMessage(data);
            },
            error: showUnexpectedError
        });
        $('#confirmationModal').modal('hide');
    }, undefined, doc.getDocLabel("page_testcampaign", "title_remove"), doc.getDocLabel("page_testcampaign", "message_remove").replace("%NAME%", key), key, undefined, undefined, undefined);
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
            "mRender": function (data, type, obj) {
                var hasPermissions = $("#" + tableId).attr("hasPermissions");

                var editTestcampaign = '<button id="editTestcampaign" onclick="editEntryClick(\'' + escapeHtml(obj["campaign"]) + '\');"\n\
                                        class="editCampaign btn btn-default btn-xs margin-right5" \n\
                                        name="editTestcampaign" title="' + doc.getDocLabel("page_testcampaign", "button_edit") + '" type="button">\n\
                                        <span class="glyphicon glyphicon-pencil"></span></button>';
                var removeTestcampaign = '<button id="removeTestcampaign" onclick="removeEntryClick(\'' + escapeHtml(obj["campaign"]) + '\');"\n\
                                        class="removeTestcampaign btn btn-default btn-xs margin-right25" \n\
                                        name="removeTestcampaign" title="' + doc.getDocLabel("page_testcampaign", "button_remove") + '" type="button">\n\
                                        <span class="glyphicon glyphicon-trash"></span></button>';
                var viewTestcampaign = '<button id="viewTestcampaign" onclick="viewEntryClick(\'' + escapeHtml(obj["campaign"]) + '\');"\n\
                                        class="viewTestcampaign btn btn-default btn-xs margin-right5" \n\
                                        name="viewTestcampaign" title="' + doc.getDocLabel("page_testcampaign", "button_testcaselist") + '" type="button">\n\
                                        <span class="glyphicon glyphicon-list"></span></button>';
                var viewStatcampaign = '<button id="viewStatcampaign" onclick="viewStatEntryClick(\'' + escapeHtml(obj["campaign"]) + '\');"\n\
                                        class="viewStatcampaign btn btn-default btn-xs margin-right5" \n\
                                        name="viewStatcampaign" title="' + doc.getDocLabel("page_testcampaign", "button_taglist") + '" type="button">\n\
                                        <span class="glyphicon glyphicon-stats"></span></button>';
                var Runcampaign = '<a id="runcampaign" class="btn btn-primary btn-xs margin-right5"\n\
                                    href="./RunTests.jsp?campaign=' + encodeURIComponent(obj["campaign"]) + '" title="' + doc.getDocLabel("page_testcampaign", "button_run") + '" >\n\
                                    <span class="glyphicon glyphicon-play"></span>\n\
                                    </a>';

                return '<div class="center btn-group">' + editTestcampaign + removeTestcampaign + viewTestcampaign + viewStatcampaign + Runcampaign + '</div>';

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
