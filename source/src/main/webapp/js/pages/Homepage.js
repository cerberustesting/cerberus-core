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
        displayPageLabel();

        bindToggleCollapse();

        $('body').tooltip({
            selector: '[data-toggle="tooltip"]'
        });
        $('[data-toggle="popover"]').popover({
            'placement': 'auto',
            'container': 'body'}
        );

        $("#tagSettingsModal").on('hidden.bs.modal', modalCloseHandler);

        $("#selectTag").on('change', function () {
            var tagListForm = $("#tagList");
            var selectedTag = $("#selectTag").val();

            if (selectedTag !== "") {
                tagListForm.append('<div class="input-group">\n\
                                    <span class="input-group-addon removeTag"><span class="glyphicon glyphicon-remove"></span></span>\n\
                                    <input type="tag" name="tag" class="form-control" id="tag" value="' + selectedTag + '" readonly>\n\
                                    </div>');
            }
            $("#selectTag").val("");
            $(".removeTag").on('click', function () {
                $(this).parent().remove();
            });
        });

        $("#saveTagList").on('click', function () {
            var tagListForm = $("#tagListForm input");
            var tagList = [];


            $.each(tagListForm.serializeArray(), function () {
                tagList.push(this.value);
            });

            localStorage.setItem("tagList", JSON.stringify(tagList));

            $("#tagSettingsModal").modal('hide');
            $('#tagExecStatus').empty();
            loadTagExec();
        });

        $("#tagSettings").on('click', function (event) {
            stopPropagation(event);
            var tagListForm = $("#tagList");
            var tagList = JSON.parse(localStorage.getItem("tagList"));

            if (tagList !== null) {
                for (var index = 0; index < tagList.length; index++) {
                    tagListForm.append('<div class="input-group">\n\
                                        <span class="input-group-addon removeTag"><span class="glyphicon glyphicon-remove"></span></span>\n\
                                        <input type="tag" name="tag" class="form-control" id="tag" value="' + tagList[index] + '" readonly>\n\
                                        </div>');
                }
            }

            loadTagFilter();

            $(".removeTag").on('click', function () {
                $(this).parent().remove();
            });

            $("#tagSettingsModal").modal('show');
        });

        //configure and create the dataTable
        //var configurations = new TableConfigurationsServerSide("homePageTable", "Homepage?system=" + getSys(), "aaData", aoColumnsFunc());
        //var configurations = new TableConfigurationsServerSide("homePageTable", "Homepage?system=" + getSys(), "aaData", aoColumnsFunc());
        var jqxhr = $.getJSON("Homepage", "system=" + getSys());

        $.when(jqxhr).then(function (result) {
            var configurations = new TableConfigurationsClientSide("homePageTable", result["aaData"], aoColumnsFunc(), true);
            configurations.tableWidth = "550px";
            configurations.showColvis = false;
            if ($('#homePageTable').hasClass('dataTable') === false) {
                createDataTableWithPermissions(configurations, undefined, "#applicationPanel");
                showTitleWhenTextOverflow();
            } else {
                var oTable = $("#homePageTable").dataTable();
                oTable.fnClearTable();
                if (result["aaData"].length > 0) {
                    oTable.fnAddData(result["aaData"]);
                }
            }


        }).fail(handleErrorAjaxAfterTimeout);

        loadTagExec();

        loadBuildRevTable();

        //close all sidebar menu
        closeEveryNavbarMenu();
    });

});

function displayPageLabel() {
    var doc = new Doc();

    displayHeaderLabel(doc);
    $("#lastTagExec").html(doc.getDocOnline("homepage", "lastTagExecution"));
    $("#tagSettingsLabel").html(doc.getDocLabel("homepage", "btn_settings"));
    $("#modalTitle").html(doc.getDocLabel("homepage", "modal_title"));
    $("#testCaseStatusByApp").html(doc.getDocOnline("homepage", "testCaseStatusByApp"));
    $("#title").html(doc.getDocLabel("homepage", "title"));

    $("#reportStatus").html(doc.getDocOnline("page_integrationstatus", "environmentStatus"));
    $("#buildHeader").html(doc.getDocOnline("buildrevisioninvariant", "versionname01"));
    $("#revisionHeader").html(doc.getDocOnline("buildrevisioninvariant", "versionname02"));
    $("#devHeader").html(doc.getDocOnline("page_integrationstatus", "DEV"));
    $("#qaHeader").html(doc.getDocOnline("page_integrationstatus", "QA"));
    $("#uatHeader").html(doc.getDocOnline("page_integrationstatus", "UAT"));
    $("#prodHeader").html(doc.getDocOnline("page_integrationstatus", "PROD"));


    displayFooter(doc);
    displayGlobalLabel(doc);
}

function getSys() {
    var sel = document.getElementById("MySystem");
    var selectedIndex = sel.selectedIndex;
    return sel.options[selectedIndex].value;
}

function readStatus() {
    var result;
    $.ajax({url: "FindInvariantByID",
        data: {idName: "TCSTATUS"},
        async: false,
        dataType: 'json',
        success: function (data) {
            result = data;
        }
    });
    return result;
}

function modalCloseHandler() {
    $("#tagList").empty();
    $("#selectTag").empty();
}

function loadTagFilter() {
    $("#selectTag").select2(getComboConfigTag());
}

function generateTagLink(tagName) {
    var link = '<a href="./ReportingExecutionByTag.jsp?Tag=' + encodeURIComponent(tagName) + '">' + tagName + '</a>';

    return link;
}

function generateTooltip(data, statusOrder, tag) {
    var htmlRes;
    var len = statusOrder.length;

    htmlRes = "<div class='tag-tooltip'><strong>Tag : </strong>" + tag;
    for (var index = 0; index < len; index++) {
        var status = statusOrder[index];

        if (data.hasOwnProperty(status)) {
            htmlRes += "<div>\n\
                        <span class='color-box status" + status + "'></span>\n\
                        <strong> " + status + " : </strong>" + data[status] + "</div>";
        }
    }
    htmlRes += '</div>';
    return htmlRes;
}

function generateTagReport(data, tag) {
    var reportArea = $("#tagExecStatus");
    var statusOrder = ["OK", "KO", "FA", "NA", "NE", "PE", "QU", "CA"];
    var buildBar;
    var tooltip = generateTooltip(data, statusOrder, tag);
    var len = statusOrder.length;

    buildBar = '<div>' + generateTagLink(tag) + '<div class="pull-right" style="display: inline;">Total executions : ' + data.total + '</div>\n\
                                                        </div><div class="progress" data-toggle="tooltip" data-html="true" title="' + tooltip + '">';
    for (var index = 0; index < len; index++) {
        var status = statusOrder[index];

        if (data.hasOwnProperty(status)) {
            var percent = (data[status] / data.total) * 100;
            var roundPercent = Math.round(percent * 10) / 10;

            buildBar += '<div class="progress-bar status' + status + '" \n\
                role="progressbar" \n\
                style="width:' + percent + '%">' + roundPercent + '%</div>';
        }
    }
    buildBar += '</div>';
    reportArea.append(buildBar);
}

function loadTagExec() {
//Get the last tag to display
    var tagList = JSON.parse(localStorage.getItem("tagList"));

    if (tagList === null || tagList.length === 0) {
        tagList = readLastTagExec();
    }

    for (var index = 0; index < tagList.length; index++) {
        let : tagName = tagList[index];
        //TODO find a way to remove the use for resendTag
        var requestToServlet = "ReadTestCaseExecutionByTag?Tag=" + tagName + "&" + "outputReport=totalStatsCharts" + "&" + "outputReport=resendTag";
        var jqxhr = $.get(requestToServlet, null, "json");

        $.when(jqxhr).then(function (data) {
            generateTagReport(data.statsChart.contentTable.total, data.tag);
        });
    }

}

function readLastTagExec() {
    var tagList = [];

    var nbExe = getParameter("cerberus_homepage_nbdisplayedtag", getUser().defaultSystem, false);

    $.ajax({
        type: "GET",
        url: "ReadTag1?iSortCol_0=0&sSortDir_0=desc&sColumns=id,tag,campaign,description&iDisplayLength=10",
//        data: {tagNumber: nbExe.value},
        async: false,
        dataType: 'json',
        success: function (data) {
            for (var s = 0; s < data.contentTable.length; s++) {
                tagList.push(data.contentTable[s].tag);
            }
//            tagList = data.contentTable;
        }
    });
    return tagList;
}

function getCountryFilter() {
    return $.ajax({url: "FindInvariantByID",
        data: {idName: "COUNTRY"},
        async: false,
        dataType: 'json',
    });
}


function aoColumnsFunc() {
    var doc = getDoc();
    var status = readStatus();
    var statusLen = status.length;
    var aoColumns = [
        {"data": "Application", "bSortable": true, "sName": "Application", "title": displayDocLink(doc.application.Application),
            "mRender": function (data, type, oObj) {
                var href = "TestCaseList.jsp?application=" + data;

                return "<a href='" + href + "'>" + data + "</a>";
            }
        },
        {"data": "Total", "bSortable": true, "sName": "Total", "title": "Total"}
    ];

    for (var s = 0; s < statusLen; s++) {
        var obj = {
            "data": status[s].value,
            "bSortable": true,
            "sName": status[s].value,
            "title": status[s].value
        };
        aoColumns.push(obj);
    }

    return aoColumns;
}

function loadBuildRevTable() {
    $('#envTableBody tr').remove();
    selectSystem = "VC";
    var jqxhr = $.getJSON("GetEnvironmentsPerBuildRevision", "system=" + getUser().defaultSystem);
    $.when(jqxhr).then(function (result) {
        $.each(result["contentTable"], function (idx, obj) {
            appendBuildRevRow(obj);
        });
    }).fail(handleErrorAjaxAfterTimeout);
}

function counterFormated(nb, build, revision, envGP) {
    if (nb === 0) {
        return "";
    } else {
        return "<a href=\"Environment.jsp?&build=" + build + "&revision=" + revision + "&envgp=" + envGP + "&active=Y\">" + nb + "</a>"
    }
}

function appendBuildRevRow(dtb) {
    var doc = new Doc();
    var table = $("#envTableBody");

    var toto = counterFormated(dtb.nbEnvDEV);

    var row = $("<tr></tr>");
    var buildRow = $("<td></td>").append(dtb.build);
    var revRow = $("<td></td>").append(dtb.revision);
    var nbdev = $("<td style=\"text-align: right;\"></td>").append(counterFormated(dtb.nbEnvDEV, dtb.build, dtb.revision, "DEV"));
    var nbqa = $("<td style=\"text-align: right;\"></td>").append(counterFormated(dtb.nbEnvQA, dtb.build, dtb.revision, "QA"));
    var nbuat = $("<td style=\"text-align: right;\"></td>").append(counterFormated(dtb.nbEnvUAT, dtb.build, dtb.revision, "UAT"));
    var nbprod = $("<td style=\"text-align: right;\"></td>").append(counterFormated(dtb.nbEnvPROD, dtb.build, dtb.revision, "PROD"));

    row.append(buildRow);
    row.append(revRow);
    row.append(nbdev);
    row.append(nbqa);
    row.append(nbuat);
    row.append(nbprod);
    table.append(row);
}


