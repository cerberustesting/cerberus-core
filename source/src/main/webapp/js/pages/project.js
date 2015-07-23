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

$.when($.getScript("js/pages/global.js")).then(function () {
    $(document).ready(function () {
        $('#projectsTable').dataTable({

        });

        var configurations = new TableConfigurationsServerSide("projectsTable", "GetProject", "aaData", aoColumnsFunc());

//        createDataTableWithPermissions(configurations);
    });
});

function aoColumnsFunc() {
    var aoColumns = [];
    $("#projectsTable th").each(function (i) {
        switch (i) {
            case 0:
                break;
//
//            case 1 :
//                aoColumns.push({className: "width250", "sName": "Name"});
//                break;
//            case 2 :
//                aoColumns.push({className: "width80", "sName": "System"});
//                break;
//            case 3 :
//                aoColumns.push({className: "width100", "sName": "Environment"});
//                break;
//            case 4 :
//                aoColumns.push({className: "width80", "sName": "Country"});
//                break;
//            case 5 :
//                aoColumns.push({className: "width100", "sName": "Group"});
//                break;
//            case 6 :
//                aoColumns.push({className: "width80", "sName": "Type"});
//                break;
//            case 7 :
//                aoColumns.push({className: "width100", "sName": "Database"});
//                break;
//            case 8 :
//                aoColumns.push({className: "width500", "sName": "Script"});
//                break;
//            case 9 :
//                aoColumns.push({className: "width250", "sName": "ServicePath"});
//                break;
//            case 10 :
//                aoColumns.push({className: "width250", "sName": "Method"});
//                break;
//            case 11 :
//                aoColumns.push({className: "width500", "sName": "Envelope"});
//                break;
//            case 12:
//                aoColumns.push({className: "width150", "sName": "Description"});
//                break;

            default :
                aoColumns.push({"sWidth": "100px"});
                break;
        }
    });
    return aoColumns;

}