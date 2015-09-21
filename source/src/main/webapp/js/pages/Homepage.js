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
        var doc = new Doc();

        displayHeaderLabel(doc);
        displayGlobalLabel(doc);

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

            localStorage.setItem("tagList", JSON.stringify(tagListForm.serializeArray()));
            $("#tagSettingsModal").modal('hide');
            $('#tagExecStatus').empty();
            loadLastTagExec();
        });

        $("#tagSettings").on('click', function () {
            var tagListForm = $("#tagList");
            var tagList = JSON.parse(localStorage.getItem("tagList"));

            if (tagList !== null) {
                for (var index = 0; index < tagList.length; index++) {
                    tagListForm.append('<div class="input-group">\n\
                                        <span class="input-group-addon removeTag"><span class="glyphicon glyphicon-remove"></span></span>\n\
                                        <input type="tag" name="tag" class="form-control" id="tag" value="' + tagList[index].value + '" readonly>\n\
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
        loadLastTagExec();
    });
});


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

function modalCloseHandler() {
    $("#tagList").empty();
    $("#selectTag").empty();
}

function loadTagFilter() {
    var jqxhr = $.get("ReadTestCaseExecution", "", "json");

    $.when(jqxhr).then(function (data) {
        var messageType = getAlertType(data.messageType);

        if (messageType === "success") {
            var index;
            $('#selectTag').append($('<option></option>').attr("value", "")).attr("placeholder", "Select a Tag");
            for (index = 0; index < data.tags.length; index++) {
                //the character " needs a special encoding in order to avoid breaking the string that creates the html element   
                var encodedString = data.tags[index].replace(/\"/g, "%22");
                var option = $('<option></option>').attr("value", encodedString).text(data.tags[index]);
                $('#selectTag').append(option);
            }
        } else {
            showMessageMainPage(messageType, data.message);
        }
    }).fail(handleErrorAjaxAfterTimeout);
}

function generateTagLink(tagName) {
    var link = '<a href="./ReportingExecutionByTag.jsp?tag=' + tagName + '">' + tagName + '</a>';

    return link;
}

function generateTooltip(data) {
    var htmlRes;

    htmlRes = "<div class='tag-tooltip'><strong>Tag : </strong>" + data.tag;
    for (var status in data.total) {
        if (status !== "totalTest") {
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

    data.forEach(function (d) {
        getTotalExec(d.total);
        var buildBar;
        var tooltip = generateTooltip(d);

        buildBar = '<div>' + generateTagLink(d.tag) + '<div class="pull-right" style="display: inline;">Total executions : ' + d.total.totalTest + '</div>\n\
                                                        </div><div class="progress" data-toggle="tooltip" data-html="true" title="' + tooltip + '">';
        for (var index = 0; index < statusOrder.length; index++) {
            var status = statusOrder[index];

            if (d.total.hasOwnProperty(status)) {
                buildBar += generateProgressBar(d.total[status]);
            }
        }
        buildBar += '</div>';
        reportArea.append(buildBar);
    });
}

function loadLastTagExec() {
//Get the last tag which have been executed
    var tagList = JSON.parse(localStorage.getItem("tagList"));

    var tagExec = [];
    for (var index = 0; index < tagList.length; index++) {
        var tagName = tagList[index].value;
        $.ajax({
            type: "GET",
            url: "GetReportData",
            data: {CampaignName: "null", Tag: tagName},
            async: false,
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

                tagData.tag = tagName;
                tagData.total = total;
                tagExec.push(tagData);
            }
        });
    }
    generateTagReport(tagExec);
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