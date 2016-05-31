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
        initPage();
    });
});

function initPage() {
    displayPageLabel();
}

function displayPageLabel() {
    var doc = new Doc();

    displayHeaderLabel(doc);
    $("#pageTitle").html(doc.getDocLabel("page_application", "title"));
    $("#title").html(doc.getDocOnline("page_application", "title"));

    $("[name='createApplicationField']").html(doc.getDocLabel("page_application", "button_create"));
    $("#environmentHeader").html(doc.getDocOnline("invariant", "ENVIRONMENT"));

    feedContent();

    displayFooter(doc);
}


function feedContent() {
    var table = $("#cerberusTableBody");
    var row = $("<tr></tr>");
    var cel1 = $("<td></td>").append("cel1");
    var cel2 = $("<td></td>").append("cel2");
    var cel3 = $("<td></td>").append("cel3");
    row.append(cel1);
    row.append(cel2);
    row.append(cel3);
    table.append(row);
    
    var table = $("#jvmTableBody");
    var row = $("<tr></tr>");
    var cel1 = $("<td></td>").append("cel1");
    row.append(cel1);
    table.append(row);
    
    var table = $("#sessionNbTableBody");
    var row = $("<tr></tr>");
    var cel1 = $("<td></td>").append("9999");
    row.append(cel1);
    table.append(row);
    
    var table = $("#sessionTableBody");
    var row = $("<tr></tr>");
    var cel1 = $("<td></td>").append("user1");
    row.append(cel1);
    table.append(row);
    var row = $("<tr></tr>");
    var cel1 = $("<td></td>").append("user1");
    row.append(cel1);
    table.append(row);
    
    var table = $("#exeNbTableBody");
    var row = $("<tr></tr>");
    var cel1 = $("<td></td>").append("8888");
    row.append(cel1);
    table.append(row);
    
    var table = $("#exeTableBody");
    var row = $("<tr></tr>");
    var cel1 = $("<td></td>").append("test1");
    row.append(cel1);
    var cel1 = $("<td></td>").append("testcase1");
    row.append(cel1);
    var cel1 = $("<td></td>").append("application1");
    row.append(cel1);
    table.append(row);
    var row = $("<tr></tr>");
    var cel1 = $("<td></td>").append("test2");
    row.append(cel1);
    var cel1 = $("<td></td>").append("testcase2");
    row.append(cel1);
    var cel1 = $("<td></td>").append("application2");
    row.append(cel1);
    table.append(row);
    
    var table = $("#threadTableBody");
    var row = $("<tr></tr>");
    var cel1 = $("<td></td>").append("cel1");
    var cel2 = $("<td></td>").append("cel2");
    row.append(cel1);
    row.append(cel2);
    table.append(row);
    
    var table = $("#databaseTableBody");
    var row = $("<tr></tr>");
    var cel1 = $("<td></td>").append("cel1");
    var cel2 = $("<td></td>").append("cel2");
    var cel3 = $("<td></td>").append("cel3");
    var cel4 = $("<td></td>").append("cel4");
    row.append(cel1);
    row.append(cel2);
    row.append(cel3);
    row.append(cel4);
    table.append(row);
    
    var table = $("#driverTableBody");
    var row = $("<tr></tr>");
    var cel1 = $("<td></td>").append("cel1");
    var cel2 = $("<td></td>").append("cel2");
    var cel3 = $("<td></td>").append("cel3");
    var cel4 = $("<td></td>").append("cel4");
    row.append(cel1);
    row.append(cel2);
    row.append(cel3);
    row.append(cel4);
    table.append(row);
    
    var table = $("#jdbcTableBody");
    var row = $("<tr></tr>");
    var cel1 = $("<td></td>").append("cel1");
    var cel2 = $("<td></td>").append("cel2");
    row.append(cel1);
    row.append(cel2);
    table.append(row);
    
}