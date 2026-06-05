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
    displayParametersTable();
}

function displayParametersTable(parameterList) {
    var ajaxUrl = "ReadParameter?system1=" + encodeURIComponent(getSys());

    if ($.fn.dataTable.isDataTable('#parametersTable')) {
        $('#parametersTable').DataTable().ajax.url(ajaxUrl).load();
    } else {
        var configurations = new TableConfigurationsServerSide(
            "parametersTable",
            ajaxUrl,
            "contentTable",
            aoColumnsFunc(),
            [1, 'asc']
        );

        createDataTableWithPermissionsNew(
            configurations,
            renderOptionsForApplication,
            "#parameterList",
            undefined,
            true
        );
    }
}

function displayAllParametersTable() {
    $(document)
        .off('cerberus:filterAlertCreated.aiParameters');

    displayParametersTable();
    const table = $("#parametersTable").dataTable();
    table.fnFilter("", 1);
}

function displayFilteredParametersTable(view) {

    const FILTERED_PARAMETER_VIEWS = {
        ai: [
            "cerberus_ai_mcp_apikey",
            "cerberus_ai_mcp_host",
            "cerberus_ai_use_mcp",
            "cerberus_log_mcpcalls",
            "cerberus_mcp_enable",
            "cerberus_anthropic_apikey",
            "cerberus_anthropic_defaultmodel",
            "cerberus_anthropic_maxtoken",
            "cerberus_anthropic_price_input_per_million",
            "cerberus_anthropic_price_output_per_million"
        ],

        smtp: [
            "cerberus_smtp_from",
            "cerberus_smtp_host",
            "cerberus_smtp_isSetTls",
            "cerberus_smtp_password",
            "cerberus_smtp_port",
            "cerberus_smtp_username"
        ]
    };
    const parameterList = FILTERED_PARAMETER_VIEWS[view];

    if (!parameterList) {
        console.warn("Unknown filtered parameters view:", view);
        displayParametersTable();
        return;
    }

    $(document)
        .off('cerberus:filterAlertCreated.aiParameters')
        .on('cerberus:filterAlertCreated.aiParameters', function (event, data) {
            if (data.tableId === 'parametersTable') {
                data.filterAlertDiv.hide();
            }
        });

    displayParametersTable();
    const table = $("#parametersTable").dataTable();

    table.fnFilter(parameterList.join(","),1);
}


function displayPageLabel() {
    var doc = new Doc();

    $("#pageTitle").html(doc.getDocLabel("page_parameter", "allParameters"));
    $("#title").html(doc.getDocOnline("page_parameter", "allParameters"));
    
    //displayHeaderLabel(doc);

    displayFooter(doc);
    displayGlobalLabel(doc);
}

function renderOptionsForApplication(data) {
    if ($("#blankSpace").length === 0) {
        var contentToAdd = "<div class='marginBottom10' id='blankSpace'></div>";
        $("#parametersTable_wrapper div#parametersTable_length").before(contentToAdd);
    }
}


function aoColumnsFunc(tableId) {
    var doc = new Doc();
    var aoColumns = [
        {
            "data": null,
            "bSortable": false,
            "bSearchable": false,
            "sWidth": "50px",
            "title": doc.getDocLabel("page_parameter", "button_col"),
            "mRender": function (data, type, obj) {
                var myClass = "glyphicon";
                if (data.hasPermissionsUpdate) {
                    myClass += " glyphicon-pencil";
                } else {
                    myClass += " glyphicon-eye-open";
                }
                var editParameter = '<button id="editParameter" onclick="openModalParameter(\'' + obj["param"] + '\', \'' + getSys() + '\');"\n\
                                        class="btn btn-default btn-xs margin-right5" \n\
                                        name="editParameter" title="' + doc.getDocLabel("page_parameter", "button_edit") + '" type="button">\n\
                                        <span class="' + myClass + '"></span></button>';

                return '<div class="center btn-group width150">' + editParameter + '</div>';

            }
        },
        {
            "data": "param",
            "sName": "par.param",
            "sWidth": "100px",
            "title": doc.getDocLabel("page_parameter", "parameter_col")
        },
        {
            "data": "value",
            "sName": "par.value",
            "sWidth": "80px",
            "title": doc.getDocLabel("page_parameter", "cerberus_col")
        },
        {
            "data": "system1value",
            "sName": "par1.value",
            "sWidth": "80px",
            "title": doc.getDocLabel("page_parameter", "system_col") + " (" + getSys() + ")"
        },
        {
            "data": "description",
            "like": true,
            "sName": "par.description",
            "sWidth": "200px",
            "title": doc.getDocLabel("page_parameter", "description_col")
        }
    ];
    return aoColumns;
}
