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
        let campaign = GetURLParameter("campaign");
        let fromDate = GetURLParameter("from");
        let toDate = GetURLParameter("to");

        if (campaign !== null && campaign !== "" && campaign !== undefined) {
            initDetailedPage();
            if ((fromDate !== null && fromDate !== "" && fromDate !== undefined) && (toDate !== null && toDate !== "" && toDate !== undefined)) {
                $('#fromPicker').datetimepicker().data("DateTimePicker").date(new Date(fromDate));
                $('#toPicker').datetimepicker().data("DateTimePicker").date(new Date(toDate));
                getStatisticsByEnvCountry();
            }
        } else {
            initGlobalPage();
        }

        //Hide the columns header search bar (searchable activated on specific columns to allow the global search on datatable, but not useful to show the header filter search bar on each column)
        hideColumnSearchBar();

        $('[data-toggle="popover"]').popover({
            'placement': 'auto',
            'container': 'body'}
        );

        $("#systemSelect").change(function () {
            if ($(this).val() == null) {
                $('#applicationSelect').multiselect("deselectAll", false);
                $('#applicationSelect').multiselect('updateButtonText');
                $('#applicationSelect').next('div').find('button').prop('disabled', true);
                $('#loadButton').prop('disabled', true);
            } else {
                setApplicationSelectOptions($(this).val());
                $('#applicationSelect').next('div').find('button').prop('disabled', false);
            }
        });

        $("#applicationSelect").change(function () {
            if ($(this).val() == null) {
                $('#loadButton').prop('disabled', true);
            } else {
                $('#loadButton').prop('disabled', false);
            }
        })
    });
});

function createMultiSelect(select) {
    select.multiselect({
        maxHeight: 450,
        checkboxName: name,
        buttonWidth: "100%",
        enableFiltering: true,
        enableCaseInsensitiveFiltering: true,
        includeSelectAllOption: true,
        includeSelectAllIfMoreThan: 1
    });
}

function createDateTimePicker(select) {
    select.datetimepicker();
    select.data("DateTimePicker").date(new Date());
}

function prepareFilterList(filter) {
    if (filter !== null) {
        for (let i = 0; i < filter.length; i++) {
            filter[i] = encodeURIComponent(filter[i]);
        }
    } else {
        filter = "";
    }
    return filter;
}

function updateDatatable(datatable, data) {
    datatable.DataTable().clear();
    datatable.DataTable().rows.add(data.campaignStatistics);
    datatable.DataTable().columns.adjust().draw();
}

function clearDatatable(datatable) {
    datatable.DataTable().clear();
    datatable.DataTable().columns.adjust().draw();
}

function setLoadingStatus(datatable) {
    $("#loading").show();
    datatable.css("filter", "blur(5px)");
    datatable.css("pointer-events", "none");
    datatable.css("user-select", "none");
}

function removeLoadingStatus(datatable) {
    $("#loading").hide();
    datatable.css("filter", "");
    datatable.css("pointer-events", "");
    datatable.css("user-select", "");
}

function setSelectOptions(selectId, options, param) {
    let select = $(selectId);
    if (select.val() === null) {
        let selectOptions = select.html("");
        $.each(options, function(index, value) {
            selectOptions += `<option value="${value}">${value}</option>`;
        });
        select.html(selectOptions);
        select.multiselect('rebuild');
        if (param == "selectAll") {
            select.multiselect('selectAll', false);
        }
        select.multiselect('updateButtonText');
    }
}

function setSystemSelectOptions() {
    let user = JSON.parse(sessionStorage.getItem('user'));
    let systems = user.system;
    let options = $("#systemSelect").html("");
    $.each(systems, function(index, value) {
        options += `<option value="${value}">${value}</option>`;
    })
    $('#systemSelect').html(options);
    $("#systemSelect").multiselect('rebuild');
}

function setApplicationSelectOptions(systems) {
    let systemsQ = "";
    $("#applicationSelect").html("");
    $('#applicationSelect').multiselect('refresh');
    $.each(systems, function(index, value) {
        systemsQ +=  "&system=" + encodeURI(systems[index]);
    });
    $.ajax
        ({
            url: "ReadApplication?"+systemsQ,
            async: true,
            dataType: "json",
            method: 'GET',
            data: {
                system: encodeURI(systems),
            },
            success: function(data) {
                let result = data.contentTable;
                let options = $("#applicationSelect").html();
                $.each(result, function(index, value) {
                    options += `<option value="${result[index].application}">${result[index].application}</option>`;
                })
                $('#applicationSelect').html(options);
                $("#applicationSelect").multiselect('rebuild');
            }
        });
}

function initGlobalPage() {
    createMultiSelect($('#systemSelect'));
    createMultiSelect($('#applicationSelect'));
    createMultiSelect($('#group1Select'));
    createDateTimePicker($('#fromPicker'));
    createDateTimePicker($('#toPicker'));
    let config = new TableConfigurationsClientSide("tagStatisticTable", "", aoColumnsFunc(), true, [1, 'asc']);
    createDataTableWithPermissions(config, undefined, "#tagStatisticList", undefined, undefined, undefined, undefined);
    displayPageLabel();
    setSystemSelectOptions();
    $('#applicationSelect').next('div').find('button').prop('disabled', true);
    $('#group1Select').next('div').find('button').prop('disabled', true);
    $('#loadButton').prop('disabled', true);

    $('#loadButton').click(function()
        {
            getStatistics();
        }
    );
}

function initDetailedPage() {
    displayPageLabel();
    $('#systemAppGroup1Filters').hide();
    createMultiSelect($('#environmentSelect'));
    createMultiSelect($('#countrySelect'));
    createDateTimePicker($('#fromPicker'));
    createDateTimePicker($('#toPicker'));
    $('#campaign').text('"' + GetURLParameter("campaign") + '"');
    $('#envCountryFilters').show();
    $('#tagStatisticList').hide();
    $('#tagStatisticDetailList').show();
    $('#loadButton').closest('.input-group-btn').hide()
    $('#loadDetailButton').closest('.input-group-btn').show()
    let config = new TableConfigurationsClientSide("tagStatisticDetailTable", "", aoColumnsDetailFunc(), true, [1, 'asc']);
    createDataTableWithPermissions(config, undefined, "#tagStatisticDetailList", undefined, undefined, undefined, undefined);

    $('#loadDetailButton').click(function()
        {
            getStatisticsByEnvCountry();
        }
    );
}

function getStatistics() {
    let systems = prepareFilterList($('#systemSelect').val());
    let applications = prepareFilterList($('#applicationSelect').val());
    let group1 = prepareFilterList($('#group1Select').val());

    $.ajax
    ({
        url: "api/campaignexecutions/statistics",
        async: true,
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        data: JSON.stringify({
            systems: encodeURI(systems),
            applications: encodeURI(applications),
            group1: encodeURI(group1),
            from: encodeURIComponent(new Date($('#fromPicker').data("DateTimePicker").date()).toISOString()),
            to: encodeURIComponent(new Date($('#toPicker').data("DateTimePicker").date()).toISOString())
        }),
        beforeSend: function() {
            setLoadingStatus($("#tagStatisticList"));
        },
        error: function(jqXHR, textStatus, errorThrown) {
            removeLoadingStatus($("#tagStatisticList"));
            let response = JSON.parse(jqXHR.responseText);
            clearDatatable($("#tagStatisticTable"));
            showMessageMainPage("danger", response.message, false);
        },
        success: function(data) {
            updateDatatable($("#tagStatisticTable"), data);
            removeLoadingStatus($("#tagStatisticList"));
            $('#group1Select').prop('disabled', false);
            setSelectOptions("#group1Select", data.group1List);
        }
    });
}

function getStatisticsByEnvCountry() {
    let campaign = GetURLParameter("campaign");
    let environments = prepareFilterList($('#environmentSelect').val());
    let countries = prepareFilterList($('#countrySelect').val());
    let from = new Date($('#fromPicker').data("DateTimePicker").date()).toISOString();
    let to = new Date($('#toPicker').data("DateTimePicker").date()).toISOString();

    $.ajax
    ({
        url: "api/campaignexecutions/statistics/" + GetURLParameter("campaign"),
        async: true,
        method: 'GET',
        data: {
            environments: encodeURI(environments),
            countries: encodeURI(countries),
            from: encodeURIComponent(from),
            to: encodeURIComponent(to)
        },
        beforeSend: function() {
            setLoadingStatus($("#tagStatisticDetailList"));
        },
        error: function(jqXHR, textStatus, errorThrown) {
            removeLoadingStatus($("#tagStatisticDetailList"));
            let response = JSON.parse(jqXHR.responseText);
            clearDatatable($("#tagStatisticDetailTable"));
            showMessageMainPage("danger", response.message, false);
        },
        success: function(data) {
            updateDatatable($("#tagStatisticDetailTable"), data);
            removeLoadingStatus($("#tagStatisticDetailList"));
            setSelectOptions("#environmentSelect", data.environments, "selectAll");
            setSelectOptions("#countrySelect", data.countries, "selectAll");
            InsertURLInHistory('ReportingCampaignStatistics.jsp?campaign=' + encodeURIComponent(campaign) + '&from=' + encodeURIComponent(from) + '&to=' + encodeURIComponent(to) + '&environments=' + encodeURI(environments) + '&countries=' + encodeURI(countries));
        }
    });
}

function displayPageLabel() {
    var doc = new Doc();
    $("#pageTitle").html(doc.getDocLabel("page_campaignstatistics", "title"));
    $("#title").html(doc.getDocLabel("page_campaignstatistics", "title") + ` <span id="campaign" style="text-transform: none;"></span>`);
    $("#labelEnvironmentSelect").html(doc.getDocLabel("page_campaignstatistics", "labelEnvironmentSelect"));
    $("#labelCountrySelect").html(doc.getDocLabel("page_campaignstatistics", "labelCountrySelect"));
    $("#labelSystemSelect").html(doc.getDocLabel("page_campaignstatistics", "labelSystemSelect"));
    $("#labelApplicationSelect").html(doc.getDocLabel("page_campaignstatistics", "labelApplicationSelect"));
    $("#labelGroup1Select").html(doc.getDocLabel("page_campaignstatistics", "labelGroup1Select"));
    $("#labelFromPicker").html(doc.getDocLabel("page_campaignstatistics", "labelFromPicker"));
    $("#labelToPicker").html(doc.getDocLabel("page_campaignstatistics", "labelToPicker"));
    $("#loadButton").html(doc.getDocLabel("page_campaignstatistics", "buttonLoad"));
    $("#loadDetailButton").html(doc.getDocLabel("page_campaignstatistics", "buttonLoad"));
    displayHeaderLabel(doc);
    displayFooter(doc);
    displayGlobalLabel(doc);
}

function aoColumnsFunc(tableId) {
    var doc = new Doc();
    const aoColumns = [
        {
            "data": null,
            "title": doc.getDocLabel("page_global", "columnAction"),
            "orderable": false,
            "searchable": false,
            "width": "50px",
            "render": function (data, type, obj) {
                const viewDetailByCountryEnv = `<a id="viewDetailByCountryEnv"
                                        href="ReportingCampaignStatistics.jsp?campaign=${obj.campaign}&from=${$('#fromPicker').data("DateTimePicker").date()}&to=${$('#toPicker').data("DateTimePicker").date()}"
                                        target="_blank"
                                        class="viewDetailByCountryEnv btn btn-default btn-xs margin-right5"
                                        title="${doc.getDocLabel("page_campaignstatistics", "buttonDetailByCountryEnv")}"
                                        type="button">
                                        <span class="glyphicon glyphicon-zoom-in"></span></a>`;
                const viewStatCampaign = `<button id="viewStatcampaign" onclick="viewStatEntryClick('${obj.campaign}');"
                                                    class="viewStatcampaign btn btn-default btn-xs margin-right5"
                                                    name="viewStatcampaign" title="${doc.getDocLabel("page_testcampaign", "button_taglist")}" type="button">
                                                    <span class="glyphicon glyphicon-stats"></span></button>`;

                return `<div class="center btn-group">${viewStatCampaign}${viewDetailByCountryEnv}</div>`;

            }
        },
        {
            "data": "campaign",
            "name": "campaign",
            "searchable": true,
            "width": "80px",
            "title": doc.getDocLabel("page_campaignstatistics", "campaign_col")
        },
        {
            "data": "systemList",
            "name": "systems",
            "searchable": true,
            "width": "120px",
            "className": "center",
            "title": doc.getDocOnline("page_campaignstatistics", "systems_col")
        },
        {
            "data": "applicationList",
            "name": "applications",
            "searchable": true,
            "width": "120px",
            "title": doc.getDocOnline("page_campaignstatistics", "applications_col")
        },
        {
            "data": "campaignGroup1",
            "name": "campaignGroup1",
            "searchable": true,
            "width": "60px",
            "title": doc.getDocOnline("page_campaignstatistics", "group1_col")
        },
        {
            "data": "minDateStart",
            "name": "minDateStart",
            "searchable": false,
            "type": "datetime",
            "width": "125px",
            "title": doc.getDocOnline("page_campaignstatistics", "minDateStart_col"),
        },
        {
            "data": "maxDateEnd",
            "name": "maxDateEnd",
            "searchable": false,
            "type": "datetime",
            "width": "125px",
            "title": doc.getDocOnline("page_campaignstatistics", "maxDateEnd_col"),
        },
        {
            "data": "avgOK",
            "name": "avgOK",
            "searchable": false,
            "width": "130px",
            "title": doc.getDocOnline("page_campaignstatistics", "avgOK_col"),
            "render": function (data, type, obj) {
                let roundedPercentage = Math.round(obj.avgOK * 10) / 10;
                let color = getGreenToRed(obj.avgOK);
                return `<div class="progress-bar" role="progressbar" style="width: ${roundedPercentage}%; background-color: ${color}; color: black;"> ${roundedPercentage} %</div>`;
            }
        },
        {
            "data": "avgDuration",
            "name": "avgDuration",
            "searchable": false,
            "width": "110px",
            "title": doc.getDocOnline("page_campaignstatistics", "avgDuration_col"),
            "render": function (data, type, obj) {
                let roundedAvgDuration = Math.round(obj.avgDuration);
                let hours = Math.floor(roundedAvgDuration / 3600);
                let minutes = Math.floor((roundedAvgDuration % 3600) / 60);
                let seconds = roundedAvgDuration % 60;
                return `${hours.toString().padStart(2, "0")}:${minutes.toString().padStart(2, "0")}:${seconds.toString().padStart(2, "0")}`;
            }
        },
        {
            "data": "avgReliability",
            "name": "avgReliability",
            "searchable": false,
            "width": "130px",
            "title": doc.getDocOnline("page_campaignstatistics", "avgReliability_col"),
            "render": function (data, type, obj) {
                let roundedPercentage = Math.round(obj.avgReliability * 10) / 10;
                let color = getGreenToRed(roundedPercentage);
                return `<div class="progress-bar" role="progressbar" style="width: ${roundedPercentage}%; background-color: ${color}; color: black;"> ${roundedPercentage} %</div>`;
            }
        },
        {
            "data": "nbCampaignExecutions",
            "name": "nbCampaignExecutions",
            "searchable": false,
            "width": "130px",
            "title": doc.getDocOnline("page_campaignstatistics", "nbCampaignExecutions_col")
        },
    ];
    return aoColumns;
}

function aoColumnsDetailFunc(tableId) {
    var doc = new Doc();
    const aoColumns = [
        {
            "data": "environment",
            "name": "environment",
            "searchable": true,
            "width": "90px",
            "className": "center",
            "title": doc.getDocLabel("page_campaignstatistics", "environment_col")
        },
        {
          "data": "country",
          "name": "country",
          "searchable": true,
          "width": "90px",
          "className": "center",
          "title": doc.getDocLabel("page_campaignstatistics", "country_col")
        },
        {
            "data": "systemList",
            "name": "systems",
            "searchable": true,
            "width": "120px",
            "className": "center",
            "title": doc.getDocOnline("page_campaignstatistics", "systems_col")
        },
        {
            "data": "applicationList",
            "name": "applications",
            "searchable": true,
            "width": "120px",
            "title": doc.getDocOnline("page_campaignstatistics", "applications_col")
        },
        {
            "data": "minDateStart",
            "name": "minDateStart",
            "searchable": false,
            "type": "datetime",
            "width": "125px",
            "title": doc.getDocOnline("page_campaignstatistics", "minDateStart_col"),
        },
        {
            "data": "maxDateEnd",
            "name": "maxDateEnd",
            "type": "datetime",
            "searchable": false,
            "width": "125px",
            "title": doc.getDocOnline("page_campaignstatistics", "maxDateEnd_col"),
        },
        {
            "data": "avgOK",
            "name": "avgOK",
            "searchable": false,
            "width": "130px",
            "title": doc.getDocOnline("page_campaignstatistics", "avgOK_col"),
            "render": function (data, type, obj) {
                let roundedPercentage = Math.round(obj.avgOK * 10) / 10;
                let color = getGreenToRed(obj.avgOK);
                return `<div class="progress-bar" role="progressbar" style="width: ${roundedPercentage}%; background-color: ${color}; color: black;"> ${roundedPercentage} %</div>`;
            }
        },
        {
            "data": "avgDuration",
            "name": "avgDuration",
            "searchable": false,
            "width": "110px",
            "title": doc.getDocOnline("page_campaignstatistics", "avgDuration_col"),
            "render": function (data, type, obj) {
                let roundedAvgDuration = Math.round(obj.avgDuration);
                let hours = Math.floor(roundedAvgDuration / 3600);
                let minutes = Math.floor((roundedAvgDuration % 3600) / 60);
                let seconds = roundedAvgDuration % 60;
                return `${hours.toString().padStart(2, "0")}:${minutes.toString().padStart(2, "0")}:${seconds.toString().padStart(2, "0")}`;
            }
        },
        {
            "data": "avgReliability",
            "name": "avgReliability",
            "searchable": false,
            "width": "130px",
            "title": doc.getDocOnline("page_campaignstatistics", "avgReliability_col"),
            "render": function (data, type, obj) {
                let roundedPercentage = Math.round(obj.avgReliability * 10) / 10;
                let color = getGreenToRed(roundedPercentage);
                return `<div class="progress-bar" role="progressbar" style="width: ${roundedPercentage}%; background-color: ${color}; color: black;"> ${roundedPercentage} %</div>`;
            }
        },
        {
            "data": "nbExeUseful",
            "name": "nbExeUseful",
            "searchable": false,
            "width": "120px",
            "title": doc.getDocOnline("page_campaignstatistics", "nbExeUseful_col")
        },
        {
            "data": "nbExe",
            "name": "nbExe",
            "searchable": false,
            "width": "120px",
            "title": doc.getDocOnline("page_campaignstatistics", "nbExe_col")
        },
    ];
    return aoColumns;
}

function getGreenToRed(percent) {
    r = percent < 50 ? 255 : Math.floor(255 - (percent * 2 - 100) * 255 / 100);
    g = percent > 50 ? 255 : Math.floor((percent * 2) * 255 / 100);
    return 'rgb(' + r + ',' + g + ',0)';
}

function hideColumnSearchBar() {
    $(".filterHeader span").hide();
    $('#tagStatisticTable').on('draw.dt', function () {
        $(".filterHeader span").hide();
    });
    $('#tagStatisticDetailTable').on('draw.dt', function () {
        $(".filterHeader span").hide();
    });
}
