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

    feedContent();

    displayFooter(doc);
}


function feedContent() {

    var jqxhr = $.getJSON("ReadCerberusDetailInformation");
    $.when(jqxhr).then(function (data) {
        var table = $("#cerberusTableBody");
        table.empty();
        var row = $("<tr></tr>");
        var cel1 = $("<td></td>").append(data.projectName);
        var cel2 = $("<td></td>").append(data.projectVersion);
        var cel3 = $("<td></td>").append(data.databaseCerberusTargetVersion);
        var cel4 = $("<td></td>").append(data.databaseCerberusCurrentVersion);
        var cel5 = $("<td></td>").append(data.environment);
        row.append(cel1);
        row.append(cel2);
        row.append(cel3);
        row.append(cel4);
        row.append(cel5);
        table.append(row);

        var table = $("#jvmTableBody");
        table.empty();
        var row = $("<tr></tr>");
        var cel1 = $("<td></td>").append(data.javaVersion);
        row.append(cel1);
        table.append(row);

        var table = $("#jvmMemTableBody");
        table.empty();
        var row = $("<tr></tr>");
        var cel1 = $("<td></td>").append(data.javaTotalMemory);
        var cel2 = $("<td></td>").append(data.javaUsedMemory);
        var cel3 = $("<td></td>").append(data.javaFreeMemory);
        var cel4 = $("<td></td>").append(data.javaMaxMemory);
        row.append(cel1);
        row.append(cel2);
        row.append(cel3);
        row.append(cel4);
        table.append(row);


        var table = $("#sessionNbTableBody");
        table.empty();
        var row = $("<tr></tr>");
        var cel1 = $("<td></td>").append(data.simultaneous_session);
        row.append(cel1);
        table.append(row);

        var table = $("#sessionTableBody");
        table.empty();
        $.each(data["active_users"], function (idx, obj) {
            var row = $("<tr></tr>");
            var cel1 = $("<td></td>").append(obj);
            row.append(cel1);
            table.append(row);
        });

        var table = $("#exeNbTableBody");
        table.empty();
        var row = $("<tr></tr>");
        var cel1 = $("<td></td>").append(data.simultaneous_execution);
        row.append(cel1);
        table.append(row);

        var table = $("#exeTableBody");
        table.empty();
        $.each(data["simultaneous_execution_list"], function (idx, obj) {
            var row = $("<tr></tr>");
            var cel1 = $("<td></td>").append(FormatedExeId(obj.id));
            row.append(cel1);
            var cel1 = $("<td></td>").append(obj.start);
            row.append(cel1);
            var cel1 = $("<td></td>").append(obj.system);
            row.append(cel1);
            var cel1 = $("<td></td>").append(obj.application);
            row.append(cel1);
            var cel1 = $("<td></td>").append(obj.test);
            row.append(cel1);
            var cel1 = $("<td></td>").append("<a href='TestCaseScript.jsp?test=" + obj.test + "&testcase=" + obj.testcase + "'>" + obj.testcase + "</a>");
            row.append(cel1);
            var cel1 = $("<td></td>").append(obj.environment);
            row.append(cel1);
            var cel1 = $("<td></td>").append(obj.country);
            row.append(cel1);
            var cel1 = $("<td></td>").append(obj.robotIP);
            row.append(cel1);
            var cel1 = $("<td></td>").append(FormatedTag(obj.tag));
            row.append(cel1);
            table.append(row);
        });

//        var table = $("#threadTableBody");
//        table.empty();
//        var row = $("<tr></tr>");
//        var cel1 = $("<td></td>").append(data.queue_in_execution + " / " + data.size_queue);
//        var cel2 = $("<td></td>").append(data.number_of_thread);
//        row.append(cel1);
//        row.append(cel2);
//        table.append(row);

        var table = $("#databaseTableBody");
        table.empty();
        var row = $("<tr></tr>");
        var cel1 = $("<td></td>").append(data.DatabaseProductName);
        var cel2 = $("<td></td>").append(data.DatabaseProductVersion);
        var cel3 = $("<td></td>").append(data.DatabaseMajorVersion);
        var cel4 = $("<td></td>").append(data.DatabaseMinorVersion);
        row.append(cel1);
        row.append(cel2);
        row.append(cel3);
        row.append(cel4);
        table.append(row);

        var table = $("#driverTableBody");
        table.empty();
        var row = $("<tr></tr>");
        var cel1 = $("<td></td>").append(data.DriverName);
        var cel2 = $("<td></td>").append(data.DriverVersion);
        var cel3 = $("<td></td>").append(data.DriverMajorVersion);
        var cel4 = $("<td></td>").append(data.DriverMinorVersion);
        row.append(cel1);
        row.append(cel2);
        row.append(cel3);
        row.append(cel4);
        table.append(row);

        var table = $("#jdbcTableBody");
        table.empty();
        var row = $("<tr></tr>");
        var cel1 = $("<td></td>").append(data.JDBCMinorVersion);
        var cel2 = $("<td></td>").append(data.JDBCMajorVersion);
        row.append(cel1);
        row.append(cel2);
        table.append(row);
    });

}


function FormatedExeId(id) {
    if (id === 0) {
        return id
    } else {
        var data = getParameter("cerberus_executiondetail_use");
        if (data.value == "N") {
            return "<a href='ExecutionDetail.jsp?id_tc=" + id + "'>" + id + "</a>";
        } else {
            return "<a href='ExecutionDetail2.jsp?executionId=" + id + "'>" + id + "</a>";
        }
    }
}

function FormatedTag(tag) {
    if (tag === undefined) {
        return tag
    } else {
        return "<a href='ReportingExecutionByTag.jsp?Tag=" + tag + "'>" + tag + "</a>";
    }
}

function resetThreadPool() {
    $.get('ExecutionThreadReset', function (data) {
        alert('Thread Pool Cleaned');
    });
}
