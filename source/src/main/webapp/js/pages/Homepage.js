/*
 * Cerberus  Copyright (C) 2013  vertigo17
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

$.when($.getScript("js/pages/global/global.js")).then(function () {
    $(document).ready(function () {
        displayPageLabel();

        bindToggleCollapse("#tagExecStatus");
        bindToggleCollapse("#applicationPanel");

        $('body').tooltip({
            selector: '[data-toggle="tooltip"]'
        });

        $("#tagSettingsModal").on('hidden.bs.modal', modalCloseHandler);

        $("#addTag").on('click', function () {
            var tagListForm = $("#tagList");
            var selectedTag = $("#selectTag option:selected").text();

            if (selectedTag !== "") {
                tagListForm.append('<div class="input-group">\n\
                                    <span class="input-group-addon"><span class="glyphicon glyphicon-remove"></span></span>\n\
                                    <input type="tag" name="tag" class="form-control" id="tag" value="' + selectedTag + '" readonly>\n\
                                    </div>');
            }
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

        $("#tagSettings").on('click', function () {
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
        var configurations = new TableConfigurationsServerSide("homePageTable", "Homepage?MySystem=" + getSys(), "aaData", aoColumnsFunc());
        var table = createDataTable(configurations);
        //By default, sort the log messages from newest to oldest

        table.fnSort([0, 'desc']);
        loadTagExec();
    });
});

function displayPageLabel() {
    var doc = new Doc();

    displayHeaderLabel(doc);
    $("#lastTagExec").html(doc.getDocOnline("homepage", "lastTagExecution"));
    $("#tagSettingsLabel").html(doc.getDocLabel("homepage", "btn_settings"));
    $("#modalTitle").html(doc.getDocLabel("homepage", "modal_title"));
    $("#addTag").html(doc.getDocLabel("homepage", "btn_addTag"));
    $("#testCaseStatusByApp").html(doc.getDocOnline("homepage", "testCaseStatusByApp"));
    $("#title").html(doc.getDocLabel("homepage", "title"));
    displayFooter(doc);
    displayGlobalLabel(doc);
}

function getSys()
{
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

function readLastTagExec() {
    var tagList = [];

    $.ajax({
        type: "GET",
        url: "ReadTag",
        data: {tagNumber: "5"},
        async: false,
        dataType: 'json',
        success: function (data) {
            tagList = data.contentTable;
        }
    });
    return tagList;
}

function modalCloseHandler() {
    $("#tagList").empty();
    $("#selectTag").empty();
}

function loadTagFilter() {
    var jqxhr = $.get("ReadTag", "", "json");

    $.when(jqxhr).then(function (data) {
        var messageType = getAlertType(data.messageType);

        if (messageType === "success") {
            var index;
            $('#selectTag').append($('<option></option>').attr("value", "")).attr("placeholder", "Select a Tag");
            for (index = 0; index < data.contentTable.length; index++) {
                //the character " needs a special encoding in order to avoid breaking the string that creates the html element   
                var encodedString = data.contentTable[index].replace(/\"/g, "%22");
                var option = $('<option></option>').attr("value", encodedString).text(data.contentTable[index]);
                $('#selectTag').append(option);
            }
        } else {
            showMessageMainPage(messageType, data.message);
        }
    }).fail(handleErrorAjaxAfterTimeout);
}

function generateTagLink(tagName) {
    var link = '<a href="./ReportingExecutionByTag.jsp?Tag=' + encodeURIComponent(tagName) + '">' + tagName + '</a>';

    return link;
}

function generateTooltip(data, statusOrder) {
    var htmlRes;

    htmlRes = "<div class='tag-tooltip'><strong>Tag : </strong>" + data.tag;
    for (var index = 0; index < statusOrder.length; index++) {
        var status = statusOrder[index];

        if (data.total.hasOwnProperty(status)) {
            data.total[status].percent = (data.total[status].value / data.total.totalTest) * 100;
            data.total[status].roundPercent = Math.round(((data.total[status].value / data.total.totalTest) * 100) * 10) / 10;


            htmlRes += "<div>\n\
                        <span class='color-box' style='background-color: " + data.total[status].color + ";'></span>\n\
                        <strong> " + status + " : </strong>" + data.total[status].roundPercent + "%</div>";
        }
    }
    htmlRes += '</div>';
    return htmlRes;
}

function generateProgressBar(statusObj) {
    var bar = '<div class="progress-bar" \n\
                role="progressbar" \n\
                aria-valuenow="60" \n\
                aria-value="0" \n\
                aria-valuemax="100" \n\
                style="width:' + statusObj.percent + '%;background-color:' + statusObj.color + '">' + statusObj.roundPercent + '%</div>';

    return bar;
}

function getTotalExec(execData) {
    var total = 0;

    for (var key in execData) {
        total += execData[key].value;
    }

    execData.totalTest = total;
}

function generateTagReport(data) {
    var reportArea = $("#tagExecStatus");
    var statusOrder = ["OK", "KO", "FA", "NA", "NE", "PE", "CA"];

    getTotalExec(data.total);
    var buildBar;
    var tooltip = generateTooltip(data, statusOrder);

    buildBar = '<div>' + generateTagLink(data.tag) + '<div class="pull-right" style="display: inline;">Total executions : ' + data.total.totalTest + '</div>\n\
                                                        </div><div class="progress" data-toggle="tooltip" data-html="true" title="' + tooltip + '">';
    for (var index = 0; index < statusOrder.length; index++) {
        var status = statusOrder[index];

        if (data.total.hasOwnProperty(status)) {
            buildBar += generateProgressBar(data.total[status]);
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
        var tagName = tagList[index];
        $.ajax({
            type: "GET",
            url: "GetReportData",
            data: {CampaignName: "null", Tag: tagName},
            async: true,
            dataType: 'json',
            success: function (data) {
                var tagData = {};
                var total = {};
                for (var index = 0; index < data.axis.length; index++) {
                    for (var key in data.axis[index]) {
                        if (key !== "name") {
                            if (total.hasOwnProperty(key)) {
                                total[key].value += data.axis[index][key].value;
                            } else {
                                total[key] = {"value": data.axis[index][key].value,
                                    "color": data.axis[index][key].color};
                            }
                        }
                    }
                }

                tagData.tag = data.tag;
                tagData.total = total;
                generateTagReport(tagData);
            }
        });
    }
}

function aoColumnsFunc() {
    var doc = getDoc();
    var status = readStatus();
    var t = "";
    var aoColumns = [
        {"data": "Application", "bSortable": false, "sName": "Application", "title": displayDocLink(doc.application.Application)},
        {"data": "Total", "bSortable": false, "sName": "Total", "title": "Total"}
    ];
    for (var s = 0; s < status.length; s++) {
        var obj = {
            "data": status[s].value,
            "bSortable": false,
            "sName": status[s].value,
            "title": status[s].value
        };
        aoColumns.push(obj);
    }
    return aoColumns;
}
