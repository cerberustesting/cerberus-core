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
        let campaign = GetURLParameter("campaign");
        let fromDate = GetURLParameter("from");
        let toDate = GetURLParameter("to");

        if (campaign !== null && campaign !== "" && campaign !== undefined) {
            initDetailedPage();
            if ((fromDate !== null && fromDate !== "" && fromDate !== undefined) && (toDate !== null && toDate !== "" && toDate !== undefined)) {
                $('#frompicker').datetimepicker().data("DateTimePicker").date(new Date(fromDate));
                $('#topicker').datetimepicker().data("DateTimePicker").date(new Date(toDate));
                getStatisticsByEnvCountry();
            }
        } else {
            initGlobalPage();
        }

        $('[data-toggle="popover"]').popover({
            'placement': 'auto',
            'container': 'body'}
        );
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
    let select = $('#' + selectId);
    if (select.val() === null) {
        select.empty();
        select.multiselect('rebuild');
        $.each(options, function(index, value) {
            if ($("#" + selectId + " option[value='" + options[index] + "']").length === 0) {
                let option = $('<option></option>').attr('value', options[index]).text(options[index]);
                select.append(option);
            }
        });
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
    $.each(systems, function(index, value) {
        let option = $('<option></option>').attr('value', value).text(value);
        $("#system-select").append(option);
        $("#system-select").multiselect('rebuild');
    })
}

function setApplicationSelectOptions() {
    let systemsQ = "";
    let systems = JSON.parse(sessionStorage.getItem('user')).system;
    $.each(systems, function(index, value) {
        systemsQ +=  "&system=" + encodeURI(systems[index]);
    })
    $.ajax
        ({
            url: "ReadApplication?"+systemsQ,
            async: true,
            dataType: "json",
            method: 'GET',
            data: {
                system: encodeURI(JSON.parse(sessionStorage.getItem('user')).system),
            },
            success: function(data) {
                let result = data.contentTable;
                $.each(result, function(index, value) {
                    let option = $('<option></option>').attr('value', result[index].application).text(result[index].application);
                    $("#application-select").append(option);
                    $("#application-select").multiselect('rebuild');
                })
            }
        });
}

function createDateTimePicker(select) {
    select.datetimepicker();
    select.data("DateTimePicker").date(new Date());
}

function initDetailedPage() {
    displayPageLabel();
    $('#systemAppGroup1Filters').hide();
    createMultiSelect($('#environment-select'));
    createMultiSelect($('#country-select'));
    createDateTimePicker($('#frompicker'));
    createDateTimePicker($('#topicker'));
    $('#campaign').text('"' + GetURLParameter("campaign") + '"');
    $('#envCountryFilters').show();
    $('#tagStatisticList').hide();
    $('#tagStatisticDetailList').show();
    $('#loadbutton').closest('.input-group-btn').hide()
    $('#loadDetailButton').closest('.input-group-btn').show()
    let config = new TableConfigurationsClientSide("tagStatisticDetailTable", "", aoColumnsDetailFunc(), true, [0, 'asc']);
    createDataTableWithPermissions(config, undefined, "#tagStatisticDetailList", undefined, undefined, undefined, undefined);

    $('#loadDetailButton').click(function()
        {
            getStatisticsByEnvCountry();
        }
    );
}

function getStatisticsByEnvCountry() {
    let campaign = GetURLParameter("campaign");
    let environments = prepareFilterList($('#environment-select').val());
    let countries = prepareFilterList($('#country-select').val());
    let from = new Date($('#frompicker').data("DateTimePicker").date()).toISOString();
    let to = new Date($('#topicker').data("DateTimePicker").date()).toISOString();

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
            showMessageMainPage("danger", response.message, false);
        },
        success: function(data) {
            updateDatatable($("#tagStatisticDetailTable"), data);
            removeLoadingStatus($("#tagStatisticDetailList"));
            setSelectOptions("environment-select", data.environments, "selectAll");
            setSelectOptions("country-select", data.countries, "selectAll");
            InsertURLInHistory('ReportingCampaignStatistics.jsp?campaign=' + encodeURIComponent(campaign) + '&from=' + encodeURIComponent(from) + '&to=' + encodeURIComponent(to) + '&environments=' + encodeURI(environments) + '&countries=' + encodeURI(countries));
        }
    });
}

function initGlobalPage() {
    createMultiSelect($('#system-select'));
    createMultiSelect($('#application-select'));
    createMultiSelect($('#group1-select'));
    createDateTimePicker($('#frompicker'));
    createDateTimePicker($('#topicker'));
    let config = new TableConfigurationsClientSide("tagStatisticTable", "", aoColumnsFunc(), true, [0, 'asc']);
    createDataTableWithPermissions(config, undefined, "#tagStatisticList", undefined, undefined, undefined, undefined);
    displayPageLabel();
    setSystemSelectOptions();
    setApplicationSelectOptions();

    $('#loadbutton').click(function()
        {
            getStatistics();
        }
    );
}
function getStatistics() {
        let systems = prepareFilterList($('#system-select').val());
        let applications = prepareFilterList($('#application-select').val());
        let group1 = prepareFilterList($('#group1-select').val());

        $.ajax
        ({
            url: "api/campaignexecutions/statistics",
            async: true,
            method: 'GET',
            data: {
                systems: encodeURI(systems),
                applications: encodeURI(applications),
                group1: encodeURI(group1),
                from: encodeURIComponent(new Date($('#frompicker').data("DateTimePicker").date()).toISOString()),
                to: encodeURIComponent(new Date($('#topicker').data("DateTimePicker").date()).toISOString())
            },
            beforeSend: function() {
                setLoadingStatus($("#tagStatisticList"));
            },
            error: function(jqXHR, textStatus, errorThrown) {
                removeLoadingStatus($("#tagStatisticList"));
                let response = JSON.parse(jqXHR.responseText);
                showMessageMainPage("danger", response.message, false);
            },
            success: function(data) {
                updateDatatable($("#tagStatisticTable"), data);
                removeLoadingStatus($("#tagStatisticList"));
                setSelectOptions("group1-select", data.globalGroup1List);
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
    $("#loadbutton").html(doc.getDocLabel("page_campaignstatistics", "buttonLoad"));
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
            "orderable": false,
            "searchable": false,
            "width": "30px",
            "render": function (data, type, obj) {
                const viewDetailByCountryEnv = `<a id="viewDetailByCountryEnv"
                                        href="ReportingCampaignStatistics.jsp?campaign=${obj.campaign}&from=${$('#frompicker').data("DateTimePicker").date()}&to=${$('#topicker').data("DateTimePicker").date()}"
                                        target="_blank"
                                        class="viewDetailByCountryEnv btn btn-default btn-xs margin-right5"
                                        title="${doc.getDocLabel("page_campaignstatistics", "buttonDetailByCountryEnv")}"
                                        type="button">
                                        <span class="glyphicon glyphicon-stats"></span></a>`;

                return '<div class="center btn-group">' + viewDetailByCountryEnv + '</div>';

            }
        },
        {
            "data": "campaign",
            "name": "campaign",
            "searchable": false,
            "width": "80px",
            "title": doc.getDocLabel("page_campaignstatistics", "campaign_col")
        },
        {
            "data": "systemList",
            "name": "systems",
            "searchable": false,
            "width": "120px",
            "className": "center",
            "title": doc.getDocLabel("page_campaignstatistics", "systems_col")
        },
        {
            "data": "applicationList",
            "name": "applications",
            "searchable": false,
            "width": "120px",
            "title": doc.getDocLabel("page_campaignstatistics", "applications_col")
        },
        {
            "data": "campaignGroup1",
            "name": "Campaign Group 1",
            "searchable": false,
            "width": "60px",
            "title": doc.getDocLabel("page_campaignstatistics", "group1_col")
        },
        {
            "data": "minDateStart",
            "name": "minDateStart",
            "searchable": false,
            "width": "120px",
            "title": doc.getDocLabel("page_campaignstatistics", "minDateStart_col"),
            "render": function (data, type, obj) {
                return new Date(obj.minDateStart).toLocaleString();
            }
        },
        {
            "data": "maxDateEnd",
            "name": "maxDateEnd",
            "searchable": false,
            "width": "120px",
            "title": doc.getDocLabel("page_campaignstatistics", "maxDateEnd_col"),
            "render": function (data, type, obj) {
                return new Date(obj.maxDateEnd).toLocaleString();
            }
        },
        {
            "data": "avgOK",
            "name": "avgOK",
            "searchable": false,
            "width": "130px",
            "title": doc.getDocLabel("page_campaignstatistics", "avgOK_col"),
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
            "title": doc.getDocLabel("page_campaignstatistics", "avgDuration_col"),
            "render": function (data, type, obj) {
                let roundedAvgDuration = Math.round(obj.avgDuration);
                if (roundedAvgDuration <= 59) {
                    return `${roundedAvgDuration} s`;
                } else {
                    let minutes = Math.floor(roundedAvgDuration / 60);
                    let remainingSeconds = roundedAvgDuration % 60;
                    return `${minutes} min ${remainingSeconds} sec`;
                }
            }
        },
        {
            "data": "avgReliability",
            "name": "avgReliability",
            "searchable": false,
            "width": "130px",
            "title": doc.getDocLabel("page_campaignstatistics", "avgReliability_col"),
            "render": function (data, type, obj) {
                let roundedPercentage = Math.round(obj.avgReliability * 10) / 10;
                let color = getGreenToRed(roundedPercentage);
                return `<div class="progress-bar" role="progressbar" style="width: ${roundedPercentage}%; background-color: ${color}; color: black;"> ${roundedPercentage} %</div>`;
            }
        },
        {
            "data": "avgNbExeUsefull",
            "name": "avgNbExeUsefull",
            "searchable": false,
            "width": "130px",
            "title": doc.getDocLabel("page_campaignstatistics", "avgNbExeUsefull_col")
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
            "searchable": false,
            "width": "90px",
            "className": "center",
            "title": doc.getDocLabel("page_campaignstatistics", "environment_col")
        },
        {
          "data": "country",
          "name": "country",
          "searchable": false,
          "width": "90px",
          "className": "center",
          "title": doc.getDocLabel("page_campaignstatistics", "country_col")
        },
        {
            "data": "systemList",
            "name": "systems",
            "searchable": false,
            "width": "120px",
            "className": "center",
            "title": doc.getDocLabel("page_campaignstatistics", "systems_col")
        },
        {
            "data": "applicationList",
            "name": "applications",
            "searchable": false,
            "width": "120px",
            "title": doc.getDocLabel("page_campaignstatistics", "applications_col")
        },
        {
            "data": "minDateStart",
            "name": "minDateStart",
            "searchable": false,
            "width": "120px",
            "title": doc.getDocLabel("page_campaignstatistics", "minDateStart_col"),
            "render": function (data, type, obj) {
                return new Date(obj.minDateStart).toLocaleString();
            }
        },
        {
            "data": "maxDateEnd",
            "name": "maxDateEnd",
            "searchable": false,
            "width": "120px",
            "title": doc.getDocLabel("page_campaignstatistics", "maxDateEnd_col"),
            "render": function (data, type, obj) {
                return new Date(obj.maxDateEnd).toLocaleString();
            }
        },
        {
            "data": "avgOK",
            "name": "avgOK",
            "searchable": false,
            "width": "130px",
            "title": doc.getDocLabel("page_campaignstatistics", "avgOK_col"),
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
            "title": doc.getDocLabel("page_campaignstatistics", "avgDuration_col"),
            "render": function (data, type, obj) {
                let roundedAvgDuration = Math.round(obj.avgDuration);
                if (roundedAvgDuration <= 59) {
                    return `${roundedAvgDuration} s`;
                } else {
                    let minutes = Math.floor(roundedAvgDuration / 60);
                    let remainingSeconds = roundedAvgDuration % 60;
                    return `${minutes} min ${remainingSeconds} sec`;
                }
            }
        },
        {
            "data": "avgReliability",
            "name": "avgReliability",
            "searchable": false,
            "width": "130px",
            "title": doc.getDocLabel("page_campaignstatistics", "avgReliability_col"),
            "render": function (data, type, obj) {
                let roundedPercentage = Math.round(obj.avgReliability * 10) / 10;
                let color = getGreenToRed(roundedPercentage);
                return `<div class="progress-bar" role="progressbar" style="width: ${roundedPercentage}%; background-color: ${color}; color: black;"> ${roundedPercentage} %</div>`;
            }
        },
        {
            "data": "avgNbExeUsefull",
            "name": "avgNbExeUsefull",
            "searchable": false,
            "width": "130px",
            "title": doc.getDocLabel("page_campaignstatistics", "avgNbExeUsefull_col")
        },
    ];
    return aoColumns;
}

function getGreenToRed(percent) {
    r = percent < 50 ? 255 : Math.floor(255 - (percent * 2 - 100) * 255 / 100);
    g = percent > 50 ? 255 : Math.floor((percent * 2) * 255 / 100);
    return 'rgb(' + r + ',' + g + ',0)';
}
