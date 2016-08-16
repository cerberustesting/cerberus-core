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

        var jqxhr = $.getJSON("GetParameterSystem", "system=" + getSys());

        $.when(jqxhr).then(function (result) {
            var configurations = new TableConfigurationsClientSide("parameterTable", result["aaData"], aoColumnsFunc(), true);
            //configurations.tableWidth = "550px";
            configurations.showColvis = false;
            configurations.bPaginate = true;
            if ($('#homePageTable').hasClass('dataTable') === false) {
                createDataTable(configurations);
            } else {
                var oTable = $("#parameterTable").dataTable();
                oTable.fnClearTable();
                if (result["aaData"].length > 0) {
                    oTable.fnAddData(result["aaData"]);
                }
            }


        }).fail(handleErrorAjaxAfterTimeout);
    });
});

function displayPageLabel() {
    var doc = new Doc();

    displayHeaderLabel(doc);
    displayFooter(doc);
    displayGlobalLabel(doc);
}

function getSys() {
    var sel = document.getElementById("MySystem");
    var selectedIndex = sel.selectedIndex;
    return sel.options[selectedIndex].value;
}

function aoColumnsFunc() {
    var aoColumns = [
        {"data": "0", "bSortable": true, "sName": "Parameter", "title": "Parameter", "width": "20%"},
        {"data": "1", "bSortable": true, "sName": "ValueCerberus", "title": "Cerberus Value", "width": "20%"},
        {"data": "2", "bSortable": true, "sName": "ValueSystem", "title": "System "+getSys()+" Value", "width": "20%"},
        {"data": "3", "bSortable": true, "sName": "Description", "title": "Description", "width": "40%"}
    ];

    return aoColumns;
}
