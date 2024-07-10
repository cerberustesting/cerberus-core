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
        initPage();

        $('[data-toggle="popover"]').popover({
            'placement': 'auto',
            'container': 'body'}
        );

        $('#application-select option').prop('selected', true);

        $('#system-select').multiselect({
            maxHeight: 450,
            checkboxName: name,
            buttonWidth: "100%",
            enableFiltering: true,
            enableCaseInsensitiveFiltering: true,
            includeSelectAllOption: true,
            includeSelectAllIfMoreThan: 1
        });

        $('#application-select').multiselect({
            maxHeight: 450,
            checkboxName: name,
            buttonWidth: "100%",
            enableFiltering: true,
            enableCaseInsensitiveFiltering: true,
            includeSelectAllOption: true,
            includeSelectAllIfMoreThan: 1
        });

        $('#group1-select').multiselect({
            maxHeight: 450,
            checkboxName: name,
            buttonWidth: "100%",
            enableFiltering: true,
            enableCaseInsensitiveFiltering: true,
            includeSelectAllOption: true,
            includeSelectAllIfMoreThan: 1
        });

        $('#system-select').multiselect('selectAll', false);
        $('#system-select').multiselect('updateButtonText');

        $('#loadbutton').click(function()
        {
            let systems = $('#system-select').val();
            if (systems === null) {
                $('#system-select').multiselect('selectAll', false);
                $('#system-select').multiselect('updateButtonText');
                systems = $('#system-select').val();
            }
            for (let i = 0; i < systems.length; i++) {
                systems[i] = encodeURIComponent(systems[i]);
            }

            let applications = $('#application-select').val();
            if (applications === null) {
                $('#application-select').multiselect('selectAll', false);
                $('#application-select').multiselect('updateButtonText');
                applications = $('#application-select').val();
            }
            for (let i = 0; i < applications.length; i++) {
                applications[i] = encodeURIComponent(applications[i]);
            }

            let group1 = $('#group1-select').val();
            if (group1 !== null) {
                for (let i = 0; i < group1.length; i++) {
                    group1[i] = encodeURIComponent(group1[i]);
                }
            } else {
                group1 = "";
            }


            $.ajax
            ({
                url: "api/campaignexecutions/statistics",
                async: true,
                method: 'GET',
                data: {
                    systemsFilter: encodeURI(systems),
                    applicationsFilter: encodeURI(applications),
                    group1Filter: encodeURI(group1),
                    from: encodeURIComponent(new Date($('#frompicker').data("DateTimePicker").date()).toISOString()),
                    to: encodeURIComponent(new Date($('#topicker').data("DateTimePicker").date()).toISOString())
                },
                beforeSend: function() {
                    $("#tagStatisticList").css("filter", "blur(5px)");
                },
                success: function(data) {
                    $("#tagStatisticTable").DataTable().clear();
                    $("#tagStatisticTable").DataTable().rows.add(data.contentTable);
                    $("#tagStatisticTable").DataTable().columns.adjust().draw();
                    $("#tagStatisticList").css("filter", "");
                    setGroup1SelectOptions(data.allCampaignGroup1);
                }
            });
        });
    });
});

function setSystemSelectOptions() {
    let user = JSON.parse(sessionStorage.getItem('user'));
    let systems = user.system;
    $.each(systems, function(index, value) {
        let option = $('<option></option>').attr('value', value).text(value);
        $("#system-select").append(option);
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
                    $('#application-select').multiselect('selectAll', false);
                    $('#application-select').multiselect('updateButtonText');
                })
            }
        });
}

function setGroup1SelectOptions(allCampaignGroup1) {
    $.each(allCampaignGroup1, function(index, value) {
        if ($("#group1-select option[value='" + allCampaignGroup1[index] + "']").length === 0) {
            let option = $('<option></option>').attr('value', allCampaignGroup1[index]).text(allCampaignGroup1[index]);
            $("#group1-select").append(option);
            $("#group1-select").multiselect('rebuild');
            $('#group1-select').multiselect('updateButtonText');
        }
    });
}



function initPage() {
    $('#frompicker').datetimepicker();
    $('#frompicker').data("DateTimePicker").date(new Date());
    $('#topicker').datetimepicker();
    $('#topicker').data("DateTimePicker").date(new Date());
    let config = new TableConfigurationsClientSide("tagStatisticTable", "", aoColumnsFunc(), true, [0, 'asc']);
    createDataTableWithPermissions(config, undefined, "#tagStatisticList", undefined, undefined, undefined, undefined);
    displayPageLabel();
    setSystemSelectOptions();
    setApplicationSelectOptions();
    setGroup1SelectOptions();
}

function displayPageLabel() {
    var doc = new Doc();
    $("#pageTitle").html(doc.getDocLabel("page_campaignstatistics", "title"));
    $("#title").html(doc.getDocLabel("page_campaignstatistics", "title"));
    $("#labelSystemSelect").html(doc.getDocLabel("page_campaignstatistics", "labelSystemSelect"));
    $("#labelApplicationSelect").html(doc.getDocLabel("page_campaignstatistics", "labelApplicationSelect"));
    $("#labelGroup1Select").html(doc.getDocLabel("page_campaignstatistics", "labelGroup1Select"));
    $("#labelFromPicker").html(doc.getDocLabel("page_campaignstatistics", "labelFromPicker"));
    $("#labelToPicker").html(doc.getDocLabel("page_campaignstatistics", "labelToPicker"));
    $("#loadbutton").html(doc.getDocLabel("page_campaignstatistics", "buttonLoad"));
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
                const viewDetailByCountryEnv = '<button id="viewDetailByCountryEnv" onclick="\n\
                                        class="viewDetailByCountryEnv btn btn-default btn-xs margin-right5" \n\
                                        name="viewDetailByCountryEnv" title="' + doc.getDocLabel("page_campaignstatistics", "buttonDetailByCountryEnv") + '" type="button">\n\
                                        <span class="glyphicon glyphicon-stats"></span></button>';

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
            "width": "150px",
            "className": "center",
            "title": doc.getDocLabel("page_campaignstatistics", "systems_col")
        },
        {
            "data": "applicationList",
            "name": "applications",
            "searchable": false,
            "width": "150px",
            "title": doc.getDocLabel("page_campaignstatistics", "applications_col")
        },
        {
            "data": "campaignGroup1",
            "name": "Campaign Group 1",
            "searchable": false,
            "width": "80px",
            "title": doc.getDocLabel("page_campaignstatistics", "group1_col")
        },
        {
            "data": "minDateStart",
            "name": "minDateStart",
            "searchable": false,
            "width": "130px",
            "title": doc.getDocLabel("page_campaignstatistics", "minDateStart_col"),
            "render": function (data, type, obj) {
                return new Date(obj.minDateStart).toLocaleString();
            }
        },
        {
            "data": "maxDateEnd",
            "name": "maxDateEnd",
            "searchable": false,
            "width": "130px",
            "title": doc.getDocLabel("page_campaignstatistics", "maxDateEnd_col"),
            "render": function (data, type, obj) {
                return new Date(obj.maxDateEnd).toLocaleString();
            }
        },
        {
            "data": "avgOK",
            "name": "avgOK",
            "searchable": false,
            "width": "150px",
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
            "width": "150px",
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
            "width": "150px",
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
            "width": "150px",
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
