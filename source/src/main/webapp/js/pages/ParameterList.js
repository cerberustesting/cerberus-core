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
    });
});

function initPage() {
    displayPageLabel();

    //configure and create the dataTable
    var configurations = new TableConfigurationsServerSide("parametersTable", "ReadParameter?system1=" + getSys(), "contentTable", aoColumnsFunc(), [1, 'asc']);
    createDataTableWithPermissions(configurations, renderOptionsForApplication, "#parameterList", undefined, true);
}

function displayPageLabel() {
    var doc = new Doc();

    displayHeaderLabel(doc);

    displayFooter(doc);
    displayGlobalLabel(doc);
}

function renderOptionsForApplication(data) {
    if ($("#blankSpace").length === 0) {
        var contentToAdd = "<div class='marginBottom10' style='height:34px;' id='blankSpace'></div>";
        $("#parametersTable_wrapper div#parametersTable_length").before(contentToAdd);
    }
}


function aoColumnsFunc(tableId) {
    var doc = new Doc();
    var aoColumns = [
        {"data": null,
            "bSortable": false,
            "bSearchable": false,
            "title": doc.getDocLabel("page_parameter", "button_col"),
            "mRender": function (data, type, obj) {
                var hasPermissions = $("#" + tableId).attr("hasPermissions");

                var editParameter = '<button id="editParameter" onclick="openModalParameter(\'' + obj["param"] + '\', \'' + getSys() + '\');"\n\
                                        class="btn btn-default btn-xs margin-right5" \n\
                                        name="editParameter" title="' + doc.getDocLabel("page_parameter", "button_edit") + '" type="button">\n\
                                        <span class="glyphicon glyphicon-pencil"></span></button>';

                return '<div class="center btn-group width150">' + editParameter + '</div>';

            },
            "width": "50px"
        },
        {"data": "param", "sName": "par.param", "title": doc.getDocLabel("page_parameter", "parameter_col")},
        {"data": "value", "sName": "par.value", "title": doc.getDocLabel("page_parameter", "cerberus_col")},
        {"data": "system1value", "sName": "par1.value", "title": doc.getDocLabel("page_parameter", "system_col") + " (" + getSys() + ")"},
        {"data": "description", "sName": "par.description", "title": doc.getDocLabel("page_parameter", "description_col")}
    ];
    return aoColumns;
}
