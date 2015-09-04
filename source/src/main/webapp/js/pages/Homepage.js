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

$.when($.getScript("js/pages/global/global.js")).then(function() {
    $(document).ready(function() {
        var doc = new Doc();

        displayHeaderLabel(doc);
        displayGlobalLabel(doc);
        //configure and create the dataTable
        var configurations = new TableConfigurationsServerSide("homePageTable", "Homepage?MySystem=" + getSys(), "aaData", aoColumnsFunc());

        createDataTable(configurations);
        //By default, sort the log messages from newest to oldest
        var oTable = $("#homePageTable").dataTable();
        oTable.fnSort([0, 'desc']);
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
        success: function(data) {
            result = data;
        }
    });
    return result;
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
        var obj = '{"data": "' + status[s].value + '","bSortable":false, "sName": "' + status[s].value + '", "title": "' + status[s].value + '"}';
        aoColumns.push(eval('(' + obj + ')'));
    }
    return aoColumns;
}