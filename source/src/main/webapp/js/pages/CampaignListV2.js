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

/* =============================================================================
 * Campaign list - V2, on js/global/crbTable.js.
 *
 * Endpoint: ReadCampaign (contentTable + table-level hasPermissions + a
 * columnName -> distinctValues mode, so per-column filters work over GET).
 *
 * Action gating is a 1:1 port of CampaignList.js:170-262 (table-wide flag):
 *   - Edit/View     : always rendered; icon AND tooltip swap on hasPermissions
 *   - Delete        : only when hasPermissions
 *   - Test cases    : always, opens the campaign content modal
 *   - Statistics    : always, opens the campaign tag/stats modal
 *   - Run           : always, a plain link to RunTests.jsp
 * and the Create button in the toolbar is permission-gated, as before.
 *
 * Everything the modals need still lives in include/transversal/Campaign.html and
 * include/pages/testcampaign/*.html, which this page includes exactly as V1 did -
 * including the eight displayInvariantList() calls and the EVENTCONNECTOR preload
 * below, without which the edit modal's dropdowns come up empty.
 *
 * Fixed on the way over:
 *   - the four handlers were built as onclick="fn('" + escapeHtml(campaign) + "')".
 *     HTML-escaping does not protect a JS string inside an attribute - the entity
 *     is decoded and THEN evaluated - so a campaign named  x'); alert(1); //  ran.
 *     V2 passes the row to onClick, so no row value reaches the markup.
 *   - deleting refreshed the table through fnDraw on a DataTable that no longer
 *     exists here; it now goes through the component.
 * ========================================================================== */

$.when($.getScript("js/global/global.js")).then(function () {
    $(document).ready(function () {
        initPage();
        $('[data-toggle="popover"]').popover({placement: 'auto', container: 'body'});
    });
});

var CRB_CAMP_TABLE_ID = "campaignTableV2";

function initPage() {
    var doc = new Doc();

    displayPageLabel();

    // Dropdowns of the campaign edit modal. Kept identical to V1: these fill the
    // Screenshot/Video/Verbose/... selects, and the modal reads them at open time.
    displayInvariantList("screenshot", "SCREENSHOT", false, undefined, "");
    displayInvariantList("video", "VIDEO", false, undefined, "");
    displayInvariantList("verbose", "VERBOSE", false, undefined, "");
    displayInvariantList("pageSource", "PAGESOURCE", false, undefined, "");
    displayInvariantList("robotLog", "ROBOTLOG", false, undefined, "");
    displayInvariantList("consoleLog", "CONSOLELOG", false, undefined, "");
    displayInvariantList("retries", "RETRIES", false, undefined, "");
    displayInvariantList("manualExecution", "MANUALEXECUTION", false, undefined, "");
    getSelectInvariant("EVENTCONNECTOR", false);

    // NOT ported: V1 bound a click handler on every button of the edit modal to
    // re-run columns.adjust() on #parameterTestcampaignsTable,
    // #labelTestcampaignsTable and #parameterTestcaseTable, for DataTables laid out
    // while their tab was hidden. Those three ids are plain <table> elements in
    // Campaign.html today, filled row by row through #labelTableBody /
    // #parameterTableBody / #criteriaTableBody - nothing in the codebase ever turns
    // them into DataTables, so the guard `#<id>_wrapper.length > 0` is never true
    // and the handler fired on all 38 buttons of the modal to do nothing. Verified
    // live before removing.

    createCerberusTable({
        id: CRB_CAMP_TABLE_ID,
        mount: "#testcampaignList",
        endpoint: "ReadCampaign",
        distinctEndpoint: "ReadCampaign",
        rowKey: "campaign",
        defaultSort: {field: "campaign", dir: "asc"},
        pageLength: 10,
        searchPlaceholder: "Search campaigns...",
        emptyMessage: "No campaign matches your search",
        // ReadCampaign takes no system parameter; sending one is harmless but
        // misleading, so it is left off.
        sendDefaultSystems: false,

        toolbar: function (ctx) {
            if (!ctx.hasPermissions) {
                return "";
            }
            return "<button id='createTestcampaignButton' type='button' " +
                "onclick=\"campaign_addEntryClick()\" " +
                "class='bg-sky-400 hover:bg-sky-500 text-white flex items-center gap-1 px-3 py-1 rounded-lg h-10'>" +
                "<i data-lucide='plus' class='w-4 h-4'></i><span>" +
                doc.getDocLabel("page_testcampaign", "button_create") + "</span></button>";
        },

        // `field` = server column name (sName in V1), `prop` = key in the row.
        // They match on this servlet, but stay explicit for consistency.
        // Visibility is V1's: only Campaign, Description and Last Executed are
        // shown; everything else is one click away in Config.
        columns: [
            {field: "campaign", title: doc.getDocOnline("page_testcampaign", "testcampaign_col"),
             width: "200px", className: "font-medium", filterable: true},
            {field: "description", title: doc.getDocOnline("page_testcampaign", "description_col"),
             width: "320px", like: true},
            {
                field: "DateLastExecuted", title: doc.getDocOnline("campaign", "DateLastExecuted"),
                width: "170px", like: true,
                render: function (row) { return crbTableEscape(campV2Date(row.DateLastExecuted)); }
            },
            {field: "longDescription", title: doc.getDocOnline("campaign", "longDescription"),
             width: "260px", visible: false, like: true},
            {field: "CIScoreThreshold", title: doc.getDocOnline("campaign", "CIScoreThreshold"),
             width: "140px", visible: false},
            {field: "group1", title: doc.getDocOnline("campaign", "Group1"),
             width: "120px", visible: false, filterable: true},
            {field: "group2", title: doc.getDocOnline("campaign", "Group2"),
             width: "120px", visible: false, filterable: true},
            {field: "group3", title: doc.getDocOnline("campaign", "Group3"),
             width: "120px", visible: false, filterable: true},
            {field: "Tag", title: doc.getDocOnline("campaign", "tag"),
             width: "140px", visible: false},
            {field: "Verbose", title: doc.getDocOnline("campaign", "Verbose"),
             width: "110px", visible: false},
            {field: "Screenshot", title: doc.getDocOnline("campaign", "Screenshot"),
             width: "110px", visible: false},
            {field: "Video", title: doc.getDocOnline("campaign", "Video"),
             width: "110px", visible: false},
            {field: "PageSource", title: doc.getDocOnline("campaign", "PageSource"),
             width: "120px", visible: false},
            {field: "RobotLog", title: doc.getDocOnline("campaign", "RobotLog"),
             width: "110px", visible: false},
            {field: "ConsoleLog", title: doc.getDocOnline("campaign", "ConsoleLog"),
             width: "120px", visible: false},
            {field: "Timeout", title: doc.getDocOnline("campaign", "Timeout"),
             width: "100px", visible: false},
            {field: "Retries", title: doc.getDocOnline("campaign", "Retries"),
             width: "100px", visible: false},
            {field: "Priority", title: doc.getDocOnline("campaign", "Priority"),
             width: "100px", visible: false},
            {field: "ManualExecution", title: doc.getDocOnline("campaign", "ManualExecution"),
             width: "150px", visible: false},
            {field: "UsrCreated", title: doc.getDocOnline("transversal", "UsrCreated"),
             width: "120px", visible: false},
            {
                field: "DateCreated", title: doc.getDocOnline("transversal", "DateCreated"),
                width: "170px", visible: false, like: true,
                render: function (row) { return crbTableEscape(campV2Date(row.DateCreated)); }
            },
            {field: "UsrModif", title: doc.getDocOnline("transversal", "UsrModif"),
             width: "120px", visible: false},
            {
                field: "DateModif", title: doc.getDocOnline("transversal", "DateModif"),
                width: "170px", visible: false, like: true,
                render: function (row) { return crbTableEscape(campV2Date(row.DateModif)); }
            }
        ],

        actions: [
            {
                key: "edit", gate: "always",
                icon: function (row, ctx) { return ctx.hasPermissions ? "pencil" : "eye"; },
                title: function (row, ctx) {
                    return ctx.hasPermissions
                        ? doc.getDocLabel("page_testcampaign", "button_edit")
                        : doc.getDocLabel("page_testcampaign", "button_view");
                },
                onClick: function (row) { campaign_editEntryClick(row.campaign); }
            },
            {
                key: "delete", icon: "trash-2", gate: "permission", danger: true,
                title: doc.getDocLabel("page_testcampaign", "button_remove"),
                onClick: function (row) { campV2RemoveEntryClick(row.campaign); }
            },
            {
                key: "testcases", icon: "list", gate: "always",
                title: doc.getDocLabel("page_testcampaign", "button_testcaselist"),
                onClick: function (row) { campaign_viewEntryClick(row.campaign); }
            },
            {
                key: "stats", icon: "bar-chart-3", gate: "always",
                title: doc.getDocLabel("page_testcampaign", "button_taglist"),
                onClick: function (row) { campaign_viewStatEntryClick(row.campaign); }
            },
            {
                key: "run", icon: "play", gate: "always",
                title: doc.getDocLabel("page_testcampaign", "button_run"),
                href: function (row) {
                    return "./RunTests.jsp?campaign=" + encodeURIComponent(row.campaign);
                }
            }
        ]
    });
}

/**
 * Campaigns that were never executed come back with an empty DateLastExecuted,
 * and UsrModif/DateModif are empty on a campaign that was never edited. getDate()
 * turns those into an epoch-looking date; show nothing instead.
 */
function campV2Date(value) {
    if (value === undefined || value === null || value === "" || value === 0) {
        return "";
    }
    return getDate(value);
}

/**
 * Delete a campaign. Same confirmation dialog and same servlet as
 * CampaignList.js:141-166; only the post-delete refresh changed, because the
 * DataTable it used to redraw does not exist on this page.
 */
async function campV2RemoveEntryClick(key) {
    var doc = new Doc();
    var messageComplete = doc.getDocLabel("page_testcampaign", "message_remove").replace("%NAME%", key);

    const result = await crbConfirmDelete({
        title: doc.getDocLabel("page_testcampaign", "title_remove"),
        html: messageComplete,
        confirmText: doc.getDocLabel("page_global", "btn_delete") || 'Delete',
        cancelText: doc.getDocLabel("page_global", "buttonClose") || 'Cancel',
        preConfirm: async () => {
            try {
                const resp = await fetch("DeleteCampaign?key=" + encodeURIComponent(key), {method: "GET"});
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
        var table = crbTableInstance(CRB_CAMP_TABLE_ID);
        if (table) {
            table.reload();
        }
        showMessage(result.value);
    }
}

/**
 * V1's own removeEntryClick lived in CampaignList.js, which this page no longer
 * loads. Nothing in the shared includes calls it by that name today, but keeping
 * the alias means a modal that starts to would not fail silently.
 */
var removeEntryClick = campV2RemoveEntryClick;

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

    displayFooter(doc);
    displayGlobalLabel(doc);
}
