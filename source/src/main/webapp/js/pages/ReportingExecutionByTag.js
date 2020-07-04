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
/* global handleErrorAjaxAfterTimeout */

var statusOrder = ["OK", "KO", "FA", "NA", "NE", "WE", "PE", "QU", "QE", "CA"];
$.when($.getScript("js/global/global.js")).then(function() {
    $(document).ready(function() {

        initPage();

        bindToggleCollapse();

        var urlTag = GetURLParameter('Tag');

        $("#splitFilter input").click(function() {
            //save the filter preferences in the session storage
            var serial = $("#splitFilter input").serialize();
            var obj = convertSerialToJSONObject(serial);
            sessionStorage.setItem("splitFilter", JSON.stringify(obj));
            //split when check or uncheck filter
            if (urlTag !== null && urlTag !== "" && urlTag !== undefined) {
                filterCountryBrowserReport(urlTag);
            }
        });

        $(document).on("mouseover", "td.center", function(e) {
            var id = $(e.currentTarget).attr("aria-describedby")
            $("#" + id).css("display", "none")
        })

        splitFilterPreferences();

        $("#splitLabelFilter input").click(function() {
            //save the filter preferences in the session storage
            var serial = $("#splitLabelFilter input").serialize();
            var obj = convertSerialToJSONObject(serial);
            sessionStorage.setItem("splitLabelFilter", JSON.stringify(obj));
            //split when check or uncheck filter
            if (urlTag !== null && urlTag !== "" && urlTag !== undefined) {
                filterLabelReport(urlTag);
            }
        });

        $("#reportByEnvCountryBrowser .nav li").on("click", function(event) {
            stopPropagation(event);
            $(this).parent().find(".active").removeClass("active");
            $(this).addClass("active");
            if ($(this).prop("id") === "tab") {
                $("#progressEnvCountryBrowser").hide();
                $("#summaryTableDiv").show();
            } else if ($(this).prop("id") === "graph") {
                $("#progressEnvCountryBrowser").show();
                $("#summaryTableDiv").hide();
            }
        });

        $("#reportByLabel .nav li").on("click", function(event) {
            stopPropagation(event);
            $(this).parent().find(".active").removeClass("active");
            $(this).addClass("active");
            if ($(this).prop("id") === "requirements") {
                $("#mainTreeExeS").hide();
                $("#mainTreeExeR").show();
            } else if ($(this).prop("id") === "stickers") {
                $("#mainTreeExeS").show();
                $("#mainTreeExeR").hide();
            }
        });

        loadTagFilters(urlTag);
        if (urlTag !== null) {
            loadAllReports(urlTag);
        }
        $('body').tooltip({
            selector: '[data-toggle="tooltip"]'
        });

        //open Run navbar Menu
        openNavbarMenu("navMenuExecutionReporting");

        $('[data-toggle="popover"]').popover({
            'placement': 'auto',
            'container': 'body'}
        );


    });
});

/*
 * Loading functions
 */

function initPage() {
    var doc = new Doc();

    displayHeaderLabel(doc);
    displayPageLabel(doc);
    displayFooter(doc);
    loadCountryFilter();
    $("#exportList").change(controlExportRadioButtons);
    loadSummaryTableOptions();
}

function loadSummaryTableOptions() {
    if (document.queryCommandSupported('Copy')) {
        $("#copyButton").html("Copy to Clipboard");
    } else {
        $("#copyButton").html("Select table");
    }

}

function loadCountryFilter() {
    $.ajax({url: "FindInvariantByID",
        data: {idName: "COUNTRY"},
        async: false,
        dataType: 'json',
        success: function(data) {
            var countryFilter = $("#countryFilter");
            var len = data.length;

            for (var i = 0; i < len; i++) {
                var filter = JSON.parse(sessionStorage.getItem("countryFilter"));
                var cb;

                //Load the filters depenbding on the preferences retrieved from session storage
                if (filter !== null && !filter.hasOwnProperty(data[i].value)) {
                    cb = '<label class="checkbox-inline">\n\
                        <input type="checkbox" name="' + data[i].value + '"/>\n\
                        ' + data[i].value + '</label>';
                } else {
                    cb = '<label class="checkbox-inline">\n\
                        <input type="checkbox" name="' + data[i].value + '" checked/>\n\
                        ' + data[i].value + '</label>';
                }
                countryFilter.append(cb);
            }
            $("#countryFilter input").on("click", function() {
                //save the filter preferences in the session storage
                var serial = $("#countryFilter input").serialize();
                var obj = convertSerialToJSONObject(serial);
                sessionStorage.setItem("countryFilter", JSON.stringify(obj));
            });
        },
        error: showUnexpectedError
    });
    $("#countrySelectAll").on("click", function() {
        $("#countryFilter input").prop('checked', true);
    });
    $("#countryUnselectAll").on("click", function() {
        $("#countryFilter input").prop('checked', false);
    });
    $("#statusSelectAll").on("click", function() {
        $("#statusFilter input").prop('checked', true);
    });
    $("#statusUnselectAll").on("click", function() {
        $("#statusFilter input").prop('checked', false);
    });
}

function splitFilterPreferences() {
    var filter = JSON.parse(sessionStorage.getItem("splitFilter"));

    if (filter !== null) {
        $("#splitFilter input").each(function() {
            if (filter.hasOwnProperty($(this).prop("name"))) {
                $(this).prop("checked", true);
            } else {
                $(this).prop("checked", false);
            }
        });
    }
}

function displaySummaryTableLabel(doc) {
    $("#summaryTableTitle").html(doc.getDocOnline("page_reportbytag", "summary_table"));
    //summary table header
    $("#summaryTableHeaderApplication").html(doc.getDocOnline("application", "Application"));
    $("#summaryTableHeaderCountry").html(doc.getDocOnline("invariant", "COUNTRY"));
    $("#summaryTableHeaderEnvironment").html(doc.getDocOnline("invariant", "ENVIRONMENT"));

    $("#selectTableButtonText").html(doc.getDocOnline("page_reportbytag", "btn_select_table"));
}

function displayExportDataLabel(doc) {
    //$("#exportDataLabel").html(doc.getDocOnline("page_global", "export_data")); //export panel //TODO:FN remove comments after development
    //$("#exportDataButton").html(doc.getDocOnline("page_global", "btn_export")); //button export //TODO:FN remove comments after development
}

function displayPageLabel(doc) {
    $("#pageTitle").html(doc.getDocLabel("page_reportbytag", "title"));
    $("#title").html(doc.getDocOnline("page_reportbytag", "title"));
    $("#loadbutton").html(doc.getDocLabel("page_reportbytag", "button_load"));
    $("#reloadbutton").html(doc.getDocLabel("page_reportbytag", "button_reload"));
    $("#filters").html(doc.getDocOnline("page_reportbytag", "filters"));
    $("#reportStatus").html(doc.getDocOnline("page_reportbytag", "report_status"));
    $("#reportTestFolder").html(doc.getDocOnline("page_reportbytag", "report_testfolder"));
    displaySummaryTableLabel(doc);
    displayExportDataLabel(doc);
    $("#envCountryBrowser").html(doc.getDocOnline("page_reportbytag", "report_envcountrybrowser"));
    $("#List").html(doc.getDocOnline("page_reportbytag", "report_list"));
    $("#statusLabel").html(doc.getDocLabel("testcase", "Status") + " :");
}

function loadTagFilters(urlTag) {

    $("#selectTag").select2(getComboConfigTag());

    if (urlTag !== null) {
        var $option = $('<option></option>').text(urlTag).val(urlTag);
        $("#selectTag").append($option).trigger('change'); // append the option and update Select2
    }
}

function loadAllReports(urlTag) {

    if (urlTag === undefined) {
        urlTag = $('#selectTag').val();
        InsertURLInHistory('ReportingExecutionByTag.jsp?Tag=' + encodeURIComponent(urlTag) + '');
    }

    if (urlTag !== "") {
        loadReportingData(urlTag);
    }
}

function loadReportingData(selectTag) {
    //var selectTag = $("#selectTag option:selected").text();
    showLoader($("#TagDetail"));
    showLoader($("#ReportByStatus"));
    showLoader($("#testFolderChart"));
    showLoader($("#BugReportByStatus"));
    showLoader($("#ManualReportByExecutor"));
    showLoader($("#reportEnvCountryBrowser"));
    showLoader($("#reportLabel"));
    showLoader($("#listReport"));
    var statusFilter = $("#statusFilter input");
    var countryFilter = $("#countryFilter input");
    var params = $("#splitFilter input");
    var paramsLabel = $("#splitLabelFilter input");
    $("#startExe").val("");
    $("#endExe").val("");
    $("#endLastExe").val("");
    $("#durExe").val("");
    $("#TagUsrCreated").val("");
    $("#Tagcampaign").val("");

    var fullL = "";
    var fullListSelected = "";
    if (document.getElementById("fullList") !== null) {
        var fullL = "fullList=" + document.getElementById("fullList").checked;
        if (document.getElementById("fullList").checked === true) {
            fullListSelected = "checked";
        } else {
            fullListSelected = "";
        }
    }
    var param = "?Tag=" + selectTag + "&" + statusFilter.serialize() + "&" + countryFilter.serialize() + "&" + params.serialize() + "&" + paramsLabel.serialize() + fullL;

    //Retrieve data for charts and draw them
    var jqxhr = $.get("ReadTestCaseExecutionByTag" + param, null, "json");
    $.when(jqxhr).then(function(data) {

        if (data.hasOwnProperty('tagObject')) {

            // Tag Detail feed.
            $("#startExe").val(data.tagObject.DateCreated);
            $("#endExe").val(data.tagObject.DateEndQueue);
            $("#endLastExe").val(data.testFolderChart.globalEnd);
            $("#TagUsrCreated").val(data.tagObject.UsrCreated);
            $("#Tagcampaign").val(data.tagObject.campaign);
            if (isEmpty(data.tagObject.campaign)) {
                $("#TagcampaignCel1").addClass("hidden");
                $("#TagcampaignCel2").addClass("hidden");
            } else {
                $("#TagcampaignCel1").removeClass("hidden");
                $("#TagcampaignCel2").removeClass("hidden");
                $("#buttonRunCampaign").attr("href", "./RunTests.jsp?campaign=" + data.tagObject.campaign);
                $("#buttonSeeStatsCampaign").attr("href", "./ReportingCampaignOverTime.jsp?campaigns=" + data.tagObject.campaign);
            }

            $("#durExe").val(data.tagDuration);
            if (data.tagDuration >= 0) {
                $("#panelDuration").removeClass("hidden");
                $("#durExe").removeClass("hidden");
            } else {
                $("#panelDuration").addClass("hidden");
                $("#durExe").addClass("hidden");
            }
            hideLoader($("#TagDetail"));

            // Report By Status
            $("#ReportByStatusTable").empty();
            $("#statusChart").empty();
            loadReportByStatusTable(data.testFolderChart, selectTag);

            // Report By Function
            $("#ReportTestFolderChart").empty();
            loadReportTestFolderChart(data.testFolderChart, selectTag);

            // Bug Report
            $("#BugReportTable").empty();
            loadBugReportByStatusTable(data.table.bugContent, selectTag);

            // Manual Report
            $("#ManualReportSum").empty();
            loadManualReportByExecutorTable(data.manualExecutionList, selectTag);

            // Report By Application Environment Country Browser
            loadEnvCountryBrowserReport(data.statsChart);

            // Report By Label
            $("#progressLabel").empty();
            loadLabelReport(data.labelStat);

            // Detailed Test Case List Report
            loadReportList(data.table, selectTag, fullListSelected);

        } else {

            hideLoader($("#TagDetail"));
            hideLoader($("#ReportByStatus"));
            hideLoader($("#testFolderChart"));
            hideLoader($("#BugReportByStatus"));
            hideLoader($("#reportEnvCountryBrowser"));
            hideLoader($("#reportLabel"));
            hideLoader($("#listReport"));
            showMessageMainPage("danger", "Tag '" + selectTag + "' does not exist.", false);

        }

        $('[data-toggle="popover"]').popover({
            'placement': 'auto',
            'container': 'body'}
        ).on('shown.bs.popover', function() {
            // Manually offer possibility to popover elemt to know when it's loading
            let idPopup = $(this).attr("aria-describedby")
            let elmt = $("#" + idPopup).find("[onload]")
            if (elmt.length > 0)
                eval(elmt.attr("onload")) // TODO eval la method
        });
    });

}

function  filterCountryBrowserReport(selectTag, splitFilterSettings) {
    //var selectTag = $("#selectTag option:selected").text();
    var statusFilter = $("#statusFilter input");
    var countryFilter = $("#countryFilter input");
    var params = $("#splitFilter input");
    var requestToServlet = "ReadTestCaseExecutionByTag?Tag=" + selectTag + "&" + statusFilter.serialize() + "&" + countryFilter.serialize() + "&" + params.serialize() + "&" + "outputReport=statsChart";
    var jqxhr = $.get(requestToServlet, null, "json");

    $.when(jqxhr).then(function(data) {
        loadEnvCountryBrowserReport(data.statsChart);
    });

}

function  filterLabelReport(selectTag) {
    //var selectTag = $("#selectTag option:selected").text();
    var statusFilter = $("#statusFilter input");
    var countryFilter = $("#countryFilter input");
    var params = $("#splitLabelFilter input");
    var requestToServlet = "ReadTestCaseExecutionByTag?Tag=" + selectTag + "&" + statusFilter.serialize() + "&" + countryFilter.serialize() + "&" + params.serialize() + "&" + "outputReport=labelStat";
    var jqxhr = $.get(requestToServlet, null, "json");

    $.when(jqxhr).then(function(data) {
        $("#progressLabel").empty();
        if (!$.isEmptyObject(data.labelStat)) {
            loadLabelReport(data.labelStat);
        }
    });

}

function generateBarTooltip(data) {
    var htmlRes = "";
    var len = statusOrder.length;

    for (var index = 0; index < len; index++) {
        var status = statusOrder[index];

        if (data.hasOwnProperty(status)) {
            if (data[status] > 0) {
                htmlRes += "<div>\n\
                        <span class='color-box status" + status + "'></span>\n\
                        <strong> " + status + " : </strong>" + data[status] + "</div>";
            }
        }
    }
    htmlRes += '</div>';
    return htmlRes;
}

function buildBar(obj) {
    var buildBar;

    var len = statusOrder.length;
    //Build the title to show at the top of the bar by checking the value of the checkbox
    var params = $("#splitFilter input");
    var key = "";
    if (params[0].checked)
        key += obj.environment + " ";
    if (params[1].checked)
        key += obj.country + " ";
    if (params[2].checked)
        key += obj.robotDecli + " ";
    if (params[3].checked)
        key += obj.application;
    if (key === "")//if no spliter if selected
        key = "Total";

    var tooltip = generateBarTooltip(obj);
    buildBar = '<div class="row"><div class="col-sm-6">' + key + '</div><div class="col-sm-6 pull-right" style="display: inline;">Total executions : ' + obj.total + '</div>';
    buildBar += '</div><div class="progress" data-toggle="tooltip" data-html="true" title="' + tooltip + '">';

    for (var i = 0; i < len; i++) {
        var status = statusOrder[i];

        if (obj[status] !== 0) {
            var percent = (obj[status] / obj.total) * 100;
            var roundPercent = Math.round(percent * 10) / 10;

            buildBar += '<div class="progress-bar status' + status + '" \n\
                                    role="progressbar" \n\
                                    style="width:' + percent + '%;">' + roundPercent + '%</div>';
        }
    }
    buildBar += '</div>';
    $("#progressEnvCountryBrowser").append(buildBar);
}

function buildLabelBar(obj) {
    var buildBar;

    var len = statusOrder.length;
    //Build the title to show at the top of the bar by checking the value of the checkbox
    var params = $("#splitLabelFilter input");
    var key = '<div class="pull-left"><span class="label label-primary" style="background-color:' + obj.label.map.color + '" data-toggle="tooltip" title="' + obj.label.map.description + '">' + obj.label.map.name + '</span></div>';

    var tooltip = generateBarTooltip(obj);
    buildBar = '<div>' + key + '<div class="pull-right" style="display: inline;margin-bottom:5px">Total executions : ' + obj.total + '</div>\n\
                                                        </div><div class="progress" style="width:100%;" data-toggle="tooltip" data-html="true" title="' + tooltip + '">';

    buildBar += '<div>'
    for (var i = 0; i < len; i++) {
        var status = statusOrder[i];

        if (obj[status] !== 0) {
            var percent = (obj[status] / obj.total) * 100;
            var roundPercent = Math.round(percent * 10) / 10;

            buildBar += '<div class="progress-bar status' + status + '" \n\
                                    role="progressbar" \n\
                                    style="width:' + percent + '%;">' + roundPercent + '%</div>';
        }
    }
    buildBar += '</div>';
    $("#progressLabel").append(buildBar);
}

function loadEnvCountryBrowserReport(data) {
    //adds a loader to a table
    showLoader($("#reportEnvCountryBrowser"));
    $("#progressEnvCountryBrowser").empty();

    var len = data.contentTable.split.length;
    if (len > 0) {

        $("#reportByEnvCountryBrowser").show();
        createSummaryTable(data.contentTable);
        for (var index = 0; index < len; index++) {
            //draw a progress bar for each combo retrieved
            buildBar(data.contentTable.split[index]);
        }
    } else {
        $("#reportByEnvCountryBrowser").hide();
    }
    hideLoader($("#reportEnvCountryBrowser"));

}

function loadLabelReport(data) {
    //adds a loader to a table
    showLoader($("#reportLabel"));
    $("#progressLabel").empty();

    if (data !== undefined) {
        $("#reportByLabel").show();
        $('#mainTreeExeS').treeview({data: data.labelTreeSTICKER, enableLinks: false, showTags: true, levels: 1});
        $('#mainTreeExeR').treeview({data: data.labelTreeREQUIREMENT, enableLinks: false, showTags: true, levels: 1});
    } else {
        $("#reportByLabel").hide();
    }

    hideLoader($("#reportLabel"));

}

function loadReportList(data2, selectTag, fullListSelected) {
    if (data2.tableColumns) {
        showLoader($("#listReport"));

        $("#ListPanel").show();

        if (selectTag !== "") {
            if ($("#listTable_wrapper").hasClass("initialized")) {
                $("#tableArea").empty();
                $("#tableArea").html('<form id="massActionForm" name="massActionForm"  title="" role="form"><table id="listTable" class="table display" name="listTable">\n\
                                            </table></form><div class="marginBottom20"></div>');
            }

            var config = new TableConfigurationsClientSide("listTable", data2.tableContent, aoColumnsFunc(data2.tableColumns), true, [0, 'asc']);
            customConfig(config);

            var table = createDataTableWithPermissions(config, undefined, "#tableArea", undefined, undefined, undefined, createShortDescRow);
            $('#listTable_wrapper').not('.initialized').addClass('initialized');
            hideLoader($("#listReport"));
            renderOptionsForExeList(selectTag, fullListSelected);
        }

    } else {
        $("#ListPanel").hide();
    }
}


/*
 * Bugs panels
 */

function loadBugReportByStatusTable(data, selectTag) {
    var len = data.bugSummary.length;
    var doc = new Doc();

    $("#bugTableReportBody tr").remove();

    if (len > 0) {
        $("#BugReportByStatusPanel").show();
        //calculate totaltest nb
        for (var index = 0; index < len; index++) {
            // increase the total execution
//            var bugLink = '<a target="_blank" href="' + data.BugTrackerStat[index].bugIdURL + '">' + data.BugTrackerStat[index].bugId + "</a>";
//            var editEntry = '<button id="editEntry" onclick="openModalTestCase_FromRepTag(this,\'' + escapeHtml(data.BugTrackerStat[index].testFirst) + '\',\'' + escapeHtml(data.BugTrackerStat[index].testCaseFirst) + '\',\'EDIT\');"\n\
//                                class="editEntry btn btn-default btn-xs margin-right5" \n\
//                                name="editEntry" data-toggle="tooltip"  title="' + doc.getDocLabel("page_testcaselist", "btn_edit") + '" type="button">\n\
//                                <span class="glyphicon glyphicon-pencil"></span></button>' + data.BugTrackerStat[index].testCaseFirst;
//            var exeLink = '<a target="_blank" href="TestCaseExecution.jsp?executionId=' + data.BugTrackerStat[index].exeIdLast + '">' + data.BugTrackerStat[index].exeIdLast + "</a> (" + data.BugTrackerStat[index].exeIdLastStatus + ")";

            var tr = $('<tr>');
            tr.append($('<td>').text(data.bugSummary[index].bug).css("text-align", "center"));
            tr.append($('<td>').text(data.bugSummary[index].test).css("text-align", "center"));
            tr.append($('<td>').text(data.bugSummary[index].testCase).css("text-align", "center"));
            tr.append($('<td>').text(data.bugSummary[index].status).css("text-align", "center"));
            $("#bugTableReportBody").append(tr);

        }

// add a panel for the total

        $("#BugReportTable").append(data.nbBugs + " bugs<br>" + data.nbTOCLEAN + " TestCases / Bugs to Clean<br>" + data.nbPENDING + " TestCases / Bugs Still Running<br>" + data.nbTOREPORT + " TestCases / Bugs To report<br>");


//        if (data.totalBugToReport > 0) {
//            if (data.totalBugToReportReported !== data.totalBugToReport) {
//                $("#BugReportTable").append(
//                        $("<div class='panel panel-primary'></div>").append(
//                        $('<div class="panel-heading"></div>').append(
//                        $('<div class="row"></div>').append(
//                        $('<div class="col-xs-8 status"></div>').text("Bugs to Report").prepend(
//                        $('<span class="" style="margin-right: 5px;"></span>'))).append(
//                        $('<div class="col-xs-4 text-right"></div>').append(
//                        $('<div class="total"></div>').text(data.totalBugToReport))
//                        ))));
//            }
//            if (data.totalBugToReportReported !== 0) {
//                $("#BugReportTable").append(
//                        $("<div class='panel panel-primary'></div>").append(
//                        $('<div class="panel-heading"></div>').append(
//                        $('<div class="row"></div>').append(
//                        $('<div class="col-xs-8 status"></div>').text("Bugs Reported").prepend(
//                        $('<span class="" style="margin-right: 5px;"></span>'))).append(
//                        $('<div class="col-xs-4 text-right"></div>').append(
//                        $('<div class="total"></div>').text(data.totalBugToReportReported)).append(
//                        $('<div class="row"></div>').append(
//                        $('<div class="percentage pull-right"></div>').text(Math.round(((data.totalBugToReportReported / data.totalBugToReport) * 100) * 100) / 100 + '%'))
//                        )
//                        ))));
//            }
//        }
//
//        if (data.totalBugToClean !== 0) {
//            $("#BugReportTable").append(
//                    $("<div class='panel panelTOCLEAN'></div>").append(
//                    $('<div class="panel-heading"></div>').append(
//                    $('<div class="row"></div>').append(
//                    $('<div class="col-xs-8 status"></div>').text("Bugs to Clean").prepend(
//                    $('<span class="" style="margin-right: 5px;"></span>'))).append(
//                    $('<div class="col-xs-4 text-right"></div>').append(
//                    $('<div class="total"></div>').text(data.totalBugToClean))
//                    ))));
//        }
    } else {
        $("#BugReportByStatusPanel").hide();
    }

    hideLoader($("#BugReportByStatus"));

}

/*
 * Manual Executions panel
 */

function loadManualReportByExecutorTable(data, selectTag) {
    var len = data.perExecutor.length;
    var doc = new Doc();

    $("#manualTableBody tr").remove();

    if (len > 0) {
        $("#ManualReportByExecutorPanel").show();
        //calculate totaltest nb
        for (var index = 0; index < len; index++) {
            var tr = $('<tr>');
            if (getUser().login === data.perExecutor[index].executor) {
                tr.append($('<td>').html(data.perExecutor[index].executor).css("text-align", "center").css("background-color", "yellow"));
            } else {
                tr.append($('<td>').html(data.perExecutor[index].executor).css("text-align", "center"));
            }
            tr.append($('<td>').text(data.perExecutor[index].executionList.length).css("text-align", "center"));
            var per = (data.perExecutor[index].executionList.length - data.perExecutor[index].executionWEList.length) / data.perExecutor[index].executionList.length;
            tr.append($('<td>').html(Math.round(((per) * 100) * 100) / 100 + '%').css("text-align", "center"));
            tr.append($('<td>').text(data.perExecutor[index].executionWEList.length).css("text-align", "center"));
            $("#manualTableBody").append(tr);

        }
    } else {
        $("#ManualReportByExecutorPanel").hide();
    }


// add a panel for the total

    var per = (data.totalExecution - data.totalWEExecution) / data.totalExecution;
    var done = (data.totalExecution - data.totalWEExecution);
    var buildBar = '<div class="progress"><div class="progress-bar statusBLACK"  role="progressbar" style="width:' + Math.round(((per) * 100) * 100) / 100 + '%">' + Math.round(((per) * 100) * 100) / 100 + '%</div></div>';
    buildBar += '<div style="text-align: center;"> ' + Math.round(((per) * 100) * 100) / 100 + '% (' + done + ' / ' + data.totalExecution + ')</div>';
    $("#ManualReportSum").html(buildBar);

    hideLoader($("#ManualReportByExecutor"));

}

/*
 * Status panels
 */

function appendPanelStatus(status, total, selectTag) {
    var rowClass = getRowClass(status);
    if ((rowClass.panel === "panelQU") || (rowClass.panel === "panelQE")) {
        // When we display the QU or QE status, we add a link to all executions in the queue on the queue page.
        $("#ReportByStatusTable").append(
                $("<a href='./TestCaseExecutionQueueList.jsp?tag=" + selectTag + "'></a>").append(
                $("<div class='panel " + rowClass.panel + "'></div>").append(
                $('<div class="panel-heading"></div>').append(
                $('<div class="row"></div>').append(
                $('<div class="col-xs-6 status"></div>').text(status).prepend(
                $('<span class="' + rowClass.glyph + '" style="margin-right: 5px;"></span>'))).append(
                $('<div class="col-xs-6 text-right"></div>').append(
                $('<div class="total"></div>').text(total[status].value)))).append(
                $('<div class="row"></div>').append(
                $('<div class="percentage pull-right"></div>').text('Percentage : ' + Math.round(((total[status].value / total.test) * 100) * 100) / 100 + '%'))
                ))));
    } else {
        $("#ReportByStatusTable").append(
                $("<div class='panel " + rowClass.panel + "'></div>").append(
                $('<div class="panel-heading"></div>').append(
                $('<div class="row"></div>').append(
                $('<div class="col-xs-6 status"></div>').text(status).prepend(
                $('<span class="' + rowClass.glyph + '" style="margin-right: 5px;"></span>'))).append(
                $('<div class="col-xs-6 text-right"></div>').append(
                $('<div class="total"></div>').text(total[status].value)))).append(
                $('<div class="row"></div>').append(
                $('<div class="percentage pull-right"></div>').text('Percentage : ' + Math.round(((total[status].value / total.test) * 100) * 100) / 100 + '%'))
                )));
    }
}

function loadReportByStatusTable(data, selectTag) {
    var total = {};
    var len = data.axis.length;

    //calculate totaltest nb
    total["test"] = 0;
    for (var index = 0; index < len; index++) {
        // increase the total execution
        for (var key in data.axis[index]) {
            if (key !== "name") {
                if (total.hasOwnProperty(key)) {
                    total[key].value += data.axis[index][key].value;
                } else {
                    total[key] = {"value": data.axis[index][key].value,
                        "color": data.axis[index][key].color};
                }
                total.test += data.axis[index][key].value;
            }
        }
    }

    // create a panel for each control status

    var len = statusOrder.length;
    //Build the title to show at the top of the bar by checking the value of the checkbox

    for (var i = 0; i < len; i++) {
        var status = statusOrder[i];
        if (total.hasOwnProperty(status)) {
            appendPanelStatus(status, total, selectTag);
        }
    }


//    for (var label in total) {
//        if (label !== "test") {
//            appendPanelStatus(label, total, selectTag);
//        }
//    }
    // add a panel for the total
    $("#ReportByStatusTable").append(
            $("<div class='panel panel-primary'></div>").append(
            $('<div class="panel-heading"></div>').append(
            $('<div class="row"></div>').append(
            $('<div class="col-xs-6 status"></div>').text("Total").prepend(
            $('<span class="" style="margin-right: 5px;"></span>'))).append(
            $('<div class="col-xs-6 text-right"></div>').append(
            $('<div class="total"></div>').text(total.test))
            ))));
    //format data to be used by the chart

    var dataset = [];
    for (var label in total) {
        if (label !== "test") {
            dataset.push(total[label]);
        }
    }
    loadReportByStatusChart(dataset);
}

/*
 * Charts functions
 */

function loadReportByStatusChart(data) {

    var margin = {top: 20, right: 25, bottom: 20, left: 50};

    var offsetW = document.getElementById('statusChart').offsetWidth;
    if (offsetW === 0) {
        offsetW = 300;
    }
    var offsetH = document.getElementById('ReportByStatusTable').offsetHeight;
    if (offsetH === 0) {
        offsetH = 300;
    }

    var width = offsetW - margin.left - margin.right;
    var height = offsetH - margin.top - margin.bottom;
    var radius = Math.min(width, height) / 2;

    var svg = d3.select('#statusChart')
            .append('svg')
            .attr('width', width + margin.left + margin.right)
            .attr('height', height + margin.top + margin.bottom)
            .append('g')
            .attr('transform', 'translate(' + (width / 2) + ',' + (height / 2) + ')')

    var arc = d3.svg.arc()
            .outerRadius(radius);

    var pie = d3.layout.pie()
            .value(function(d) {
                return d.value;
            })
            .sort(null);

    var path = svg.selectAll('path')
            .data(pie(data))
            .enter()
            .append('path')
            .attr('d', arc)
            .attr('fill', function(d, i) {
                return d.data.color;
            });
    hideLoader($("#ReportByStatus"));
}

function convertData(dataset) {
    var data = [];

    for (var i in dataset)
        data.push(dataset[i]);
    return data;
}

function loadReportTestFolderChart(dataset) {
    var data = convertData(dataset.axis);

    if (dataset.axis.length > 0) {
        $("#ReportByTestFolderPanel").show();

        var margin = {top: 20, right: 20, bottom: 200, left: 150},
                width = 1200 - margin.left - margin.right,
                height = 600 - margin.top - margin.bottom;

        var x = d3.scale.ordinal()
                .rangeRoundBands([0, width], .1);

        var y = d3.scale.linear()
                .rangeRound([height, 0]);

        var xAxis = d3.svg.axis()
                .scale(x)
                .orient("bottom");

        var yAxis = d3.svg.axis()
                .scale(y)
                .orient("left");

        var tip = d3.tip()
                .attr('class', 'd3-tip')
                .offset([-10, 0])
                .html(function(d) {
                    var res = "<strong>Function :</strong> <span style='color:red'>" + d.name + "</span>";
                    var len = d.chartData.length;

                    for (var index = 0; index < len; index++) {
                        res = res + "<div><div class='color-box' style='background-color:" + d.chartData[index].color + " ;'>\n\
                    </div>" + d.chartData[index].name + " : " + d[d.chartData[index].name].value + "</div>";
                    }
                    return res;
                });

        var svg = d3.select("#ReportTestFolderChart").append("svg")
                .attr("width", width + margin.left + margin.right)
                .attr("height", height + margin.top + margin.bottom)
                .append("g")
                .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

        svg.call(tip);


        data.forEach(function(d) {
            var y0 = 0;
            d.chartData = [];
            for (var status in d) {
                if (status !== "name" && status !== "chartData") {
                    d.chartData.push({name: status, y0: y0, y1: y0 += +d[status].value, color: d[status].color});
                }
            }
            d.totalTests = d.chartData[d.chartData.length - 1].y1;
        });

        x.domain(data.map(function(d) {
            return d.name;
        }));
        y.domain([0, d3.max(data, function(d) {
                return d.totalTests;
            })]);

        svg.append("g")
                .attr("class", "x axis")
                .attr("transform", "translate(0," + height + ")")
                .call(xAxis)
                .selectAll("text")
                .call(wrap, 200)
                .style({"text-anchor": "end"})
                .attr("dx", "-.8em")
                .attr("dy", "-.55em")
                .attr("transform", "rotate(-75)");

        svg.append("g")
                .attr("class", "y axis")
                .call(yAxis)
                .append("text")
                .attr("transform", "rotate(-90)")
                .attr("y", 6)
                .attr("dy", ".71em")
                .style("text-anchor", "end")
                .text("TestCase Number");

        var name = svg.selectAll(".name")
                .data(data)
                .enter().append("g")
                .attr("class", "g")
                .attr("transform", function(d) {
                    return "translate(" + x(d.name) + ",0)";
                });

        svg.selectAll(".g")
                .on('mouseover', tip.show)
                .on('mouseout', tip.hide);

        name.selectAll("rect")
                .data(function(d) {
                    return d.chartData;
                })
                .enter().append("rect")
                .attr("width", x.rangeBand())
                .attr("y", function(d) {
                    return y(d.y1);
                })
                .attr("height", function(d) {
                    return y(d.y0) - y(d.y1);
                })
                .style("fill", function(d) {
                    return d.color;
                });
    } else {
        $("#ReportByTestFolderPanel").hide();
    }
    hideLoader($("#testFolderChart"));
}

/*** EXPORT OPTIONS***/

function exportReport() {
    //open file chooser and then export
    var selectTag = $("#selectTag option:selected").text();
    var statusFilter = $("#statusFilter input");
    var countryFilter = $("#countryFilter input");
    var exportDataFilter = $("#exportData input");

    var jqxhr = $.getJSON("ReadTestCaseExecution", "Tag=" + selectTag + "&" + statusFilter.serialize() +
            "&" + countryFilter.serialize() + "&" + exportDataFilter.serialize());
    $.when(jqxhr).then(function(data) {
        alert(data);
    });
}

function controlExportRadioButtons() {
    //control radiobuttons
    var isChecked = $(this).prop("checked");
    if (isChecked) {
        $("input[name='exportOption']").prop("disabled", false);
    } else {
        $("input[name='exportOption']").prop("disabled", true);
    }
}

/*** SUMMARY TABLE options ****/

/**
 * Create a row for the summaryTable
 * @param {JSONObject} row containing the data of the row
 * @param {boolean} isTotalRow true is the row to display is total line.
 * @returns {jQuery} the jquery object row
 */
function createRow(row, isTotalRow) {

    var $tr = $('<tr>');
    var params = $("#splitFilter input");

    if (!isTotalRow) {
        if (params[0].checked) {
            if (row.environment === "Total")//TODO: make this change in the back-end
                row.environment = "";//The part where Total is written is handle in a more generic way
            $tr.append($('<td>').text(row.environment).css("text-align", "center"));
        }
        if (params[1].checked)
            $tr.append($('<td>').text(row.country).css("text-align", "center"));
        if (params[2].checked)
            $tr.append($('<td>').text(row.robotDecli).css("text-align", "center"));
        if (params[3].checked)
            $tr.append($('<td>').text(row.application).css("text-align", "center"));
    } else {
        var blankSpaceToAdd = 0;
        for (var i in params) {
            if (params[i].checked)
                blankSpaceToAdd++;
        }
        if (blankSpaceToAdd !== 0) {
            $tr.append($('<td>').text("Total").css("text-align", "center"));
            blankSpaceToAdd--;//remove a blank space for the total
            for (var i = 0; i < blankSpaceToAdd; i++) {
                $tr.append($('<td>').text("").css("text-align", "center"));
            }
        }
    }
    $tr.append(
            $('<td>').text(row.OK).css("text-align", "right"),
            $('<td>').text(row.KO).css("text-align", "right"),
            $('<td>').text(row.FA).css("text-align", "right"),
            $('<td>').text(row.NA).css("text-align", "right"),
            $('<td>').text(row.NE).css("text-align", "right"),
            $('<td>').text(row.WE).css("text-align", "right"),
            $('<td>').text(row.PE).css("text-align", "right"),
            $('<td>').text(row.QU).css("text-align", "right"),
            $('<td>').text(row.QE).css("text-align", "right"),
            $('<td>').text(row.CA).css("text-align", "right"),
            $('<td>').text(row.notOKTotal).css("text-align", "right"),
            $('<td>').text(row.total).css("text-align", "right"),
            $('<td>').text(row.percOK + "%").css("text-align", "right"),
            $('<td>').text(row.percKO + "%").css("text-align", "right"),
            $('<td>').text(row.percFA + "%").css("text-align", "right"),
            $('<td>').text(row.percNA + "%").css("text-align", "right"),
            $('<td>').text(row.percNE + "%").css("text-align", "right"),
            $('<td>').text(row.percWE + "%").css("text-align", "right"),
            $('<td>').text(row.percPE + "%").css("text-align", "right"),
            $('<td>').text(row.percQU + "%").css("text-align", "right"),
            $('<td>').text(row.percQE + "%").css("text-align", "right"),
            $('<td>').text(row.percCA + "%").css("text-align", "right"),
            $('<td>').text(row.percNotOKTotal + "%").css("text-align", "right")
            );
    return $tr;
}

/**
 * Create a row for the summaryTableHeader
 * @returns {jQuery} the jquery object row
 */
function createHeaderRow() {
    var $tr = $('<tr>');

    var params = $("#splitFilter input");
    if (params[0].checked)
        $tr.append($('<td>').text("Environment").css("text-align", "center"));
    if (params[1].checked)
        $tr.append($('<td>').text("Country").css("text-align", "center"));
    if (params[2].checked)
        $tr.append($('<td>').text("Browser").css("text-align", "center"));
    if (params[3].checked)
        $tr.append($('<td>').text("Application").css("text-align", "center"));

    $tr.append(
            $('<td>').text("OK").css("text-align", "center"),
            $('<td>').text("KO").css("text-align", "center"),
            $('<td>').text("FA").css("text-align", "center"),
            $('<td>').text("NA").css("text-align", "center"),
            $('<td>').text("NE").css("text-align", "center"),
            $('<td>').text("WE").css("text-align", "center"),
            $('<td>').text("PE").css("text-align", "center"),
            $('<td>').text("QU").css("text-align", "center"),
            $('<td>').text("QE").css("text-align", "center"),
            $('<td>').text("CA").css("text-align", "center"),
            $('<td>').text("NOT OK").css("text-align", "center"),
            $('<td>').text("TOTAL").css("text-align", "center"),
            $('<td>').text("% OK").css("text-align", "center"),
            $('<td>').text("% KO").css("text-align", "center"),
            $('<td>').text("% FA").css("text-align", "center"),
            $('<td>').text("% NA").css("text-align", "center"),
            $('<td>').text("% NE").css("text-align", "center"),
            $('<td>').text("% WE").css("text-align", "center"),
            $('<td>').text("% PE").css("text-align", "center"),
            $('<td>').text("% QU").css("text-align", "center"),
            $('<td>').text("% QE").css("text-align", "center"),
            $('<td>').text("% CA").css("text-align", "center"),
            $('<td>').text("% NOT OK").css("text-align", "center")
            );
    return $tr;
}
/**
 * Creates a summary table from data retrieved from server.
 * @param {type} data
 * @returns {undefined}
 */
function createSummaryTable(data) {
    //cleans the data that was already added
    $("#summaryTableHeader tr").remove();
    $("#summaryTableBody tr").remove();
    $("#summaryTableHeader").append(createHeaderRow(data.total));
    //TODO:FN verifies if table is empty?
    $.when($.each(data.split, function(idx, obj) {
        //creates a new row
        //numbers are aligned to right
        var $tr = createRow(obj, false);
        if (obj.percOK === 100) {
            $($tr).addClass("summary100");
        }
        $("#summaryTableBody").append($tr);
    })).then(function() {
        var $total = createRow(data.total, true);
        $total.addClass("summaryTotal");
        $("#summaryTableBody").append($total);
        //alternate colors
        $("#summaryTableBody tr:odd").css("background-color", "rgba(225,231,243,0.2)");
        //if the row is the summary total, then it will have the background color blue
        $("#summaryTableBody tr.summaryTotal").css("background-color", "rgba(66,139,202,0.2)").css("font-weight", "900");
        //if the row has 100% ok, then it will have the background color green
        $("#summaryTableBody tr.summary100").css("background-color", "rgba(92,184,0,0.2)");

    });
}
function selectTableToCopy() {

    var el = document.getElementById('summaryTable');

    var body = document.body, range, sel;
    if (document.createRange && window.getSelection) {
        range = document.createRange();
        sel = window.getSelection();
        sel.removeAllRanges();
        try {
            range.selectNodeContents(el);
            sel.addRange(range);
        } catch (e) {
            range.selectNode(el);
            sel.addRange(range);
        }

    } else if (body.createTextRange) {
        range = body.createTextRange();
        range.moveToElementText(el);
        range.select();
    }
}

function createShortDescRow(row, data, index) {
    var tableAPI = $("#listTable").DataTable();

    var createdRow = tableAPI.row(row);

    createdRow.child([data.shortDesc, "labels"]);
    $(row).children('.center').attr('rowspan', '3');
    $(row).children('.priority').attr('rowspan', '3');
    $(row).children('.bugid').attr('rowspan', '3');
    $(createdRow.child()).children('td').attr('colspan', '3').attr('class', 'shortDesc').attr('data-toggle', 'tooltip').attr('data-original-title', data.shortDesc);
    var labelValue = '';
    $.each(data.labels, function(i, e) {
        labelValue += '<div style="float:left"><span class="label label-primary" style="background-color:' + e.color + '">' + e.name + '</span></div> ';
    });
    $($(createdRow.child())[1]).children('td').html(labelValue);
    createdRow.child.show();
}

function generateTooltip(data) {
    var htmlRes;
    var ctrlmessage = data.ControlMessage;
    if (ctrlmessage !== undefined && ctrlmessage.length > 200) {
        ctrlmessage = data.ControlMessage.substring(0, 200) + '...';
    }
    if (!isEmpty(data.NbExecutions) && (data.NbExecutions >= 2)) {
        htmlRes = '<div><span class=\'bold\'>Execution ID :</span> ' + data.ID + ' - (' + data.NbExecutions + ' Exe(s))</div>';
    } else {
        htmlRes = '<div><span class=\'bold\'>Execution ID :</span> ' + data.ID + '</div>';
    }
    htmlRes += '<div><span class=\'bold\'>Environment : </span>' + data.Environment + '</div>';
    htmlRes += '<div><span class=\'bold\'>Country : </span>' + data.Country + '</div>';
    if ((data.ManualExecution === "Y")) {
        htmlRes += '<div><span class=\'bold\'>Manual Execution';
        if ((data.Executor !== "")) {
            htmlRes += ' by ' + data.Executor;
        } else {
            htmlRes += '.</span></div>';

        }
    }
    if ((data.RobotDecli !== undefined) && (data.RobotDecli !== '')) {
        htmlRes += '<div><span class=\'bold\'>Robot Decli : </span>' + data.RobotDecli + '</div>';
    }
    htmlRes += '<div><span class=\'bold\'>Start : </span>' + getDateShort(data.Start) + '</div>';
    if (getDateShort(data.End) !== "") {
        htmlRes += '<div><span class=\'bold\'>End : </span>' + getDateShort(data.End) + '</div>';
    }
    htmlRes += '<div>' + ctrlmessage + '</div>';

    return htmlRes;
}

function openModalTestCase_FromRepTag(element, test, testcase, mode) {
    openModalTestCase(test, testcase, mode, "tabTCBugReport");
    $('#editTestCaseModal').on("hidden.bs.modal", function(e) {
        $('#editTestCaseModal').unbind("hidden.bs.modal");

        var testcaseobj = $('#editTestCaseModal').data("testcase");
        if ((!(testcaseobj === undefined)) && ($('#editTestCaseModal').data("Saved"))) {
            // when modal is closed, we check that testcase object exist and has been saved in order to update the comment and bugid on reportbytag screen.
            var newComment = $('#editTestCaseModal').data("testcase").comment;
            $(element).parent().parent().find('td.comment').text(decodeURI(newComment).replace(/\+/g, ' ').replace(/%2B/g, '+'));

            var newBugId = $('#editTestCaseModal').data("bug");
            var link = "";
            var appurl = $('#editTestCaseModal').data("appURL");
            link = getBugIdList(newBugId, appurl);

            $(element).parent().parent().find('td.bugid').html(link);
        }
    });
}

function selectAllQueue(checkboxid, manualExecution, status) {
    if ($('#' + checkboxid).prop("checked")) {
        if (!isEmpty(manualExecution)) {
            $("[data-line='select" + manualExecution + "-" + status + "']").prop("checked", true);
        } else {
            $("[data-line='selectA-" + status + "']").prop("checked", true);
            $("[data-line='selectN-" + status + "']").prop("checked", true);
            $("[data-line='selectY-" + status + "']").prop("checked", true);
            $("[data-line='select-" + status + "']").prop("checked", true);
        }
    } else {
        if (!isEmpty(manualExecution)) {
            $("[data-line='select" + manualExecution + "-" + status + "']").prop("checked", false);
        } else {
            $("[data-line='selectA-" + status + "']").prop("checked", false);
            $("[data-line='selectN-" + status + "']").prop("checked", false);
            $("[data-line='selectY-" + status + "']").prop("checked", false);
            $("[data-line='select-" + status + "']").prop("checked", false);
        }
    }
    refreshNbChecked();
}

function refreshNbChecked() {
    // Count total nb of result in order to display it and activate or not the button.
    var nbchecked = $("[data-select='id']:checked").size();
    if (nbchecked > 0) {
        $('#submitExe').prop("disabled", false);
        $('#submitExe').html("<span class='glyphicon glyphicon-play'></span> Submit Again (" + nbchecked + ")");
        $('#submitExewithDep').prop("disabled", false);
        $('#submitExewithDep').html("<span class='glyphicon glyphicon-play'></span> Submit Again with Dep (" + nbchecked + ")");
    } else {
        $('#submitExe').prop("disabled", true);
        $('#submitExe').html("<span class='glyphicon glyphicon-play'></span> Submit Again");
        $('#submitExewithDep').prop("disabled", true);
        $('#submitExewithDep').html("<span class='glyphicon glyphicon-play'></span> Submit Again with Dep");
    }
}

function renderOptionsForExeList(selectTag, fullListSelected) {
    if ($("#blankSpace").length === 0) {
        var doc = new Doc();
        var contentToAdd = "<div class='marginBottom10'>";
        contentToAdd += "<label class='marginRight10'>Select all/none :</label>";
        contentToAdd += "<label class='checkbox-inline'><input id='selectAllQueueQEERROR' type='checkbox'></input>QE (ERROR)</label>";
        contentToAdd += "<label class='checkbox-inline'><input id='selectAllQueueFA' type='checkbox'></input>FA</label>";
        contentToAdd += "<label class='checkbox-inline'><input id='selectAllQueueFAManual' type='checkbox'></input>FA (Manual)</label>";
        contentToAdd += "<label class='checkbox-inline'><input id='selectAllQueueKO' type='checkbox'></input>KO</label>";
        contentToAdd += "<label class='checkbox-inline'><input id='selectAllQueueKOManual' type='checkbox'></input>KO (Manual)</label>";
        contentToAdd += "<label class='checkbox-inline marginRight10'><input id='selectAllQueueNA' type='checkbox'></input>NA</label>";
        contentToAdd += "<div class='btn-group marginRight20'>";
        contentToAdd += "<button id='submitExe' type='button' disabled='disabled' title='Submit again the selected executions.' class='btn btn-default'><span class='glyphicon glyphicon-play'></span> Submit Again</button>";
        contentToAdd += "<button id='btnGroupDrop4' type='button' class='btn btn-default dropdown-toggle' data-toggle='dropdown' aria-haspopup='true' aria-expanded='false'><span class='caret'></span><span class='sr-only'>Toggle Dropdown</span></button>";
        contentToAdd += "<div class='dropdown-menu'><button id='submitExewithDep' type='button' disabled='disabled' title='Submit again the selected executions with all dependencies.' class='btn btn-default'><span class='glyphicon glyphicon-play'></span> Submit Again with Dep</button></div>";
        contentToAdd += "</div>";
        contentToAdd += "<a href='TestCaseExecutionQueueList.jsp?tag=" + selectTag + "'><button id='openqueue' type='button' class='btn btn-default marginLeft20'><span class='glyphicon glyphicon-list'></span> Open Queue</button></a>";
        contentToAdd += "<label class='checkbox-inline'><input id='fullList' type='checkbox' " + fullListSelected + "></input>Full List</label>";
        contentToAdd += "<button id='refresh' type='button' title='Refresh.' class='btn btn-default marginLeft20' onclick='loadAllReports()'><span class='glyphicon glyphicon-refresh'></span> Refresh</button>";
        contentToAdd += "</div>";

        $("#listTable_length").before(contentToAdd);

        $('#selectAllQueueQEERROR').click(function() {
            selectAllQueue("selectAllQueueQEERROR", "", "QEERROR");
        });
        $('#selectAllQueueFA').click(function() {
            selectAllQueue("selectAllQueueFA", "N", "FA");
        });
        $('#selectAllQueueFAManual').click(function() {
            selectAllQueue("selectAllQueueFAManual", "Y", "FA");
        });
        $('#selectAllQueueKO').click(function() {
            selectAllQueue("selectAllQueueKO", "N", "KO");
        });
        $('#selectAllQueueKOManual').click(function() {
            selectAllQueue("selectAllQueueKOManual", "Y", "KO");
        });
        $('#selectAllQueueNA').click(function() {
            selectAllQueue("selectAllQueueNA", "", "NA");
        });
        $('#submitExe').click(massAction_copyQueueWithoutDep);
        $('#submitExewithDep').click(massAction_copyQueueWithDep);
    }
}

function massAction_copyQueue(option) {

    if (option === undefined) {
        option = "toQUEUED";
    }
    clearResponseMessageMainPage();

    var doc = new Doc();
    var formList = $('#massActionForm');
    var paramSerialized = formList.serialize();

    if (paramSerialized.indexOf("id") === -1) {
        var localMessage = new Message("danger", doc.getDocLabel("page_global", "message_massActionError"));
        showMessage(localMessage, null);
    } else {

        var jqxhr = $.post("CreateTestCaseExecutionQueue", paramSerialized + "&actionState=" + option + "&actionSave=save", "json");
        $.when(jqxhr).then(function(data) {
            // unblock when remote call returns
            if ((getAlertType(data.messageType) === "success") || (getAlertType(data.messageType) === "warning")) {
                if (data.addedEntries === 1) {
                    data.message = data.message + "<a href='TestCaseExecution.jsp?executionQueueId=" + data.testCaseExecutionQueueList[0].id + "'><button class='btn btn-primary' id='goToExecution'>Open Execution</button></a>";
                }
                let formA = $('#massActionForm :input#selectLine');
                for (var i = 0; i < formA.length; i++) {
                    if (formA[i].checked) {
                        $('[data-id="' + formA[i].value + '"]').prop("checked", false);
                    }
                }
                $('[data-id="201811"]').prop("checked", false);
                refreshNbChecked();
                showMessageMainPage(getAlertType(data.messageType), data.message, false, 60000);
            } else {
                showMessage(data);
            }
        }).fail(handleErrorAjaxAfterTimeout);
    }

}

function massAction_copyQueueWithDep() {
    massAction_copyQueue("toQUEUEDwithDep");
}
function massAction_copyQueueWithoutDep() {
    massAction_copyQueue("toQUEUED");
}

var cptDep = 0;
function aoColumnsFunc(Columns) {
    var doc = new Doc();
    var colNb = Columns.length;
    var nbColumn = colNb + 5;
    var testCaseInfoWidth = (1 / 6) * 30;
    var testExecWidth = (1 / nbColumn) * 70;
    var tag = $('#selectTag').val();

    var aoColumns = [
        {
            "data": "test",
            "sName": "tec.test",
            "sWidth": "80px",
            "title": doc.getDocOnline("test", "Test"),
            "sClass": "bold",
            "fnCreatedCell": function(row, data, dataIndex) {
                // Set the data-status attribute, and add a class
                $(row).attr('data-original-title', data)
                $(row).attr('data-toggle', "tooltip")
            }
        },
        {
            "data": "testCase",
            "sName": "tec.testCase",
            "sWidth": "60px",
            "title": doc.getDocOnline("testcase", "TestCase"),
            "mRender": function(data, type, obj, meta) {
                var result = "<a href='./TestCaseScript.jsp?test=" + encodeURIComponent(obj.test) + "&testcase=" + encodeURIComponent(obj.testCase) + "'>" + obj.testCase + "</a>";
                var editEntry = '<button id="editEntry" onclick="openModalTestCase_FromRepTag(this,\'' + escapeHtml(obj["test"]) + '\',\'' + escapeHtml(obj["testCase"]) + '\',\'EDIT\');"\n\
                                class="editEntry btn btn-default btn-xs margin-right5" \n\
                                name="editEntry" data-toggle="tooltip"  title="' + doc.getDocLabel("page_testcaselist", "btn_edit") + '" type="button">\n\
                                <span class="glyphicon glyphicon-pencil"></span></button>';
                if (obj.testExist) {
                    return editEntry + result;
                } else {
                    return obj.testCase;
                }
            }
        },
        {
            "data": "application",
            "sName": "app.application",
            "sWidth": "60px",
            "title": doc.getDocOnline("application", "Application")
        }
    ];
    for (var i = 0; i < colNb; i++) {
        var title = Columns[i].environment + " " + Columns[i].country + " " + Columns[i].robotDecli;

        var col = {
            "title": title,
            "bSortable": true,
            "bSearchable": true,
            "class": "mainCell",
            "sWidth": "40px",
            "data": function(row, type, val, meta) {
                var dataTitle = meta.settings.aoColumns[meta.col].sTitle;
                if (row.hasOwnProperty("execTab") && row["execTab"].hasOwnProperty(dataTitle)) {
                    return row["execTab"][dataTitle];
                } else {
                    return "";
                }
            },
            "sClass": "center",
            "mRender": function(data, type, row, meta) {
                if (data !== "") {
                    // Getting selected Tag;
                    var glyphClass = getRowClass(data.ControlStatus);
                    var tooltip = generateTooltip(data);
                    let idProgressBar = (data.Test + "_" + data.TestCase + "_" + data.Country + "_" + data.Environment + "_" + data.RobotDecli).replace(/\./g, '_').replace(/ /g, '_').replace(/\:/g, '_');
                    var cell = "";
                    cell += '<div class="input-group mainCell" id="' + idProgressBar + '">';
                    cell += '<span style="border:0px;border-radius:0px;box-shadow: inset 0 -1px 0 rgba(0,0,0,.15);" class="input-group-addon status' + data.ControlStatus + '">';
                    var state = data.ControlStatus;
                    if (!isEmpty(data.QueueState)) {
                        state += data.QueueState;
                    }
                    if ((data.QueueID !== undefined) && (data.QueueID !== "0")) {
                        cell += '<input id="selectLine" name="id" value=' + data.QueueID + ' onclick="refreshNbChecked()" data-select="id" data-line="select' + data.ManualExecution + '-' + state + '" data-id="' + data.QueueID + '" title="Select for Action" type="checkbox"></input>';
                    }
                    cell += '</span>';
                    let statWidth = "100";
                    if (data.previousExeControlStatus !== undefined) {
                        cell += '<div style="width: 20%;cursor: pointer; height: 40px;" class="progress-bar status' + data.previousExeControlStatus + '"';
                        cell += ' onclick="window.open(\'./TestCaseExecution.jsp?executionId=' + data.previousExeId + '\')">';
                        cell += data.previousExeControlStatus;
                        cell += '</div>';
                        statWidth = "80";
                    }
                    if ((data.ControlStatus === "QU") || (data.ControlStatus === "QE")) {
                        cell += '<div class="progress-bar progress-bar-queue status' + data.ControlStatus + '" id1="' + idProgressBar + '" ';
                    } else {
                        cell += '<div class="progress-bar status' + data.ControlStatus + '" id1="' + idProgressBar + '" ';
                    }
                    cell += 'role="progressbar" aria-valuenow="100" aria-valuemin="0" aria-valuemax="100" style="width: ' + statWidth + '%;cursor: pointer; height: 40px;"';
                    cell += 'data-toggle="tooltip" data-html="true" title="' + tooltip + '"';
                    if ((data.ControlStatus === "QU") || (data.ControlStatus === "QE")) {
                        cell += ' onclick="openModalTestCaseExecutionQueue(' + data.QueueID + ', \'EDIT\');">\n\' ';
                    } else {
                        cell += ' onclick="window.open(\'./TestCaseExecution.jsp?executionId=' + data.ID + '\')">';
                    }
                    cell += '<span class="' + glyphClass.glyph + ' marginRight5"></span>';
                    if (getUser().login === data.Executor) {
                        cell += '<span style="color:yellow" name="tcResult">' + data.ControlStatus + '</span>';
                    } else {
                        cell += '<span name="tcResult">' + data.ControlStatus + '</span>';
                    }
                    if (data.QueueState !== undefined) {
                        cell += '<br><span style="font-size: xx-small">' + data.QueueState + " " + '</span>';
                    }
                    if (data.TestCaseDep.length > 0) {
                        let button = ""
                        let txt = ""
                        let dependencyArray = ""
                        for (let dep of data.TestCaseDep) {
                            dependencyArray += "{test:'" + dep.test + "',testcase:'" + dep.testcase + "',Country:'" + data.Country + "',Environment:'" + data.Environment + "',robotdecli:'" + data.RobotDecli + "'},"
                        }
                        var dependency = "renderDependency('dep" + cptDep + "',[" + dependencyArray + "]);"
                    }
                    cell += '</div>';
                    if (data.TestCaseDep.length > 0) {
                        cell += '<span style="padding:0px; border:0px;border-radius:0px;box-shadow: inset 0 -1px 0 rgba(0,0,0,.15);" class="input-group-addon ">';
                        cell += '<a id="dep' + cptDep + '" role="button" class="btn btn-info hideFeatureTCDependencies" onclick="stopPropagation(event);' + dependency + '" data-html="true" data-toggle="popover" data-placement="right">' +
                                '<span class="glyphicon glyphicon-tasks" aria-hidden="true"></span> </a>'
                        cell += '</span>';
                        cptDep++;
                    }
                    cell += '</div>';
                    return cell;
                } else {
                    return data;
                }
            }
        };
        aoColumns.push(col);
    }
    var col =
            {
                "data": "priority",
                "sName": "tec.priority",
                "sClass": "priority",
                "sWidth": "20px",
                "title": doc.getDocOnline("invariant", "PRIORITY")
            };
    aoColumns.push(col);
    var col =
            {
                "data": "comment",
                "sName": "tec.comment",
                "sClass": "comment",
                "sWidth": "60px",
                "title": doc.getDocOnline("testcase", "Comment")
            };
    aoColumns.push(col);
    var col =
            {
                "data": "bugs",
                "bSearchable": false,
                "mRender": function(data, type, obj) {
                    return getBugIdList(data, obj.AppBugURL);
                },
                "sName": "tec.bugs",
                "sClass": "bugid",
                "sWidth": "40px",
                "title": doc.getDocOnline("testcase", "BugID")
            };
    aoColumns.push(col);

    var col =
            {
                "data": "NbRetry",
                "sName": "NbRetry",
                "sClass": "NbRetry",
                "sWidth": "40px",
                "title": "Total nb of Retries"
            };
    aoColumns.push(col);

    return aoColumns;
}

function renderDependency(id, dependencyArray) {
    let text = ""
    // Remove all background of mainCell
    $(".mainCell").parent().removeClass("info");
    dependencyArray.forEach(dep => {
        let idProgressBar = (dep.test + "_" + dep.testcase + "_" + dep.Country + "_" + dep.Environment + "_" + dep.robotdecli).replace(/ /g, '_').replace(/\./g, '_').replace(/\:/g, '_');
        let tcDepResult = $("#" + idProgressBar).find("[name='tcResult']").text();
        text += "<a style='cursor: pointer;' onclick='$(\"#" + idProgressBar + "\").click()' style='font-size: xx-small'><div style='width: 20%' class='progress-bar status" + tcDepResult + "'>" + tcDepResult + "</div></a><a href='#" + idProgressBar + "'>" + dep.test + " - " + dep.testcase + "</a><br>";
        // Add background of mainCell that are dependent.
        $("#" + idProgressBar).parent().addClass("info");
    });
    $("#" + id).attr('title', "Dependency")
            .addClass("info")
            .popover('fixTitle')
            .attr("data-content", text)
            .attr("data-placement", "right")
            .popover('show');
}

function customConfig(config) {
    var doc = new Doc();
    var customColvisConfig = {"buttonText": doc.getDocLabel("dataTable", "colVis"),
        "exclude": [0, 1, 2],
        "stateChange": function(iColumn, bVisible) {
            $('.shortDesc').each(function() {
                $(this).attr('colspan', '3');
            });
            $('.label').each(function() {
                $(this).attr('colspan', '3');
            });
        }
    };

    config.bPaginate = true;
    config.lengthMenu = [10, 25, 50, 100, 500, 1000, 1500, 2000];
    config.lang.colVis = customColvisConfig;
    config.orderClasses = false;
    config.bDeferRender = true;
    config.displayLength = 500;

}


function wrap(text, width) {
    text.each(function() {
        var text = d3.select(this),
                words = text.text().split(/\s+/).reverse(),
                word,
                line = [],
                lineNumber = 0,
                lineHeight = 1.1, // ems
                y = text.attr("y"),
                dy = parseFloat(text.attr("dy")),
                tspan = text.text(null).append("tspan").attr("x", 0).attr("y", y).attr("dy", dy + "em");
        while (word = words.pop()) {
            line.push(word);
            tspan.text(line.join(" "));
            if (tspan.node().getComputedTextLength() > width) {
                line.pop();
                tspan.text(line.join(" "));
                line = [word];
                tspan = text.append("tspan").attr("x", 0).attr("y", y).attr("dy", ++lineNumber * lineHeight + dy + "em").text(word);
            }
        }
    });
}
