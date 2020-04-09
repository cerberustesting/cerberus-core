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
    });
});

function initPage() {

    displayPageLabel();

    bindToggleCollapse();

}

function displayPageLabel() {
    var doc = new Doc();

    displayHeaderLabel(doc);

    feedContent();

    displayFooter(doc);
}

function feedContent() {

    showLoader('#panelActivity');
    showLoader('#panelInformation');
    showLoader('#paneljvmInformation');
    showLoader('#paneldtbInformation');
    showLoader('#panelschInformation');

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

        var table = $("#cerberusAuthTableBody");
        table.empty();
        var row = $("<tr></tr>");
        var cel1 = $("<td></td>").append(data.authentification);
        var cel2 = $("<td></td>").append(data.isKeycloak);
        var cel3 = $("<td></td>").append(data.keycloakRealm);
        var cel4 = $("<td></td>").append(data.keycloakClient);
        var cel5 = $("<td></td>").append(data.keycloakUrl);
        row.append(cel1);
        row.append(cel2);
        row.append(cel3);
        row.append(cel4);
        row.append(cel5);
        table.append(row);

        var table = $("#cerberusSaaSTableBody");
        table.empty();
        var row = $("<tr></tr>");
        var cel1 = $("<td></td>").append(data.saaS);
        var cel2 = $("<td></td>").append(data.isSaaS.toString());
        var cel3 = $("<td></td>").append(data.saasInstance);
        var cel4 = $("<td></td>").append(data.saasParallelrun);
        row.append(cel1);
        row.append(cel2);
        row.append(cel3);
        row.append(cel4);
        table.append(row);

        var table = $("#jvmTableBody");
        table.empty();
        var row = $("<tr></tr>");
        var cel1 = $("<td></td>").append(data.javaVersion);
        row.append(cel1);
        table.append(row);

        var table = $("#appjvmTableBody");
        table.empty();
        var row = $("<tr></tr>");
        var cel1 = $("<td></td>").append(data.applicationServerInfo);
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

        var table = $("#exeNbTableBody");
        table.empty();
        var row = $("<tr></tr>");
        var cel1 = $("<td></td>").append(data.simultaneous_execution);
        var cel2 = $("<td></td>").append(data.executionThreadPoolInstanceActive.toString());
        row.append(cel1);
        row.append(cel2);
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

        var table = $("#databaseTableBody");
        table.empty();
        var row = $("<tr></tr>");
        var cel1 = $("<td></td>").append(data.databaseProductName);
        var cel2 = $("<td></td>").append(data.databaseProductVersion);
        var cel3 = $("<td></td>").append(data.databaseMajorVersion);
        var cel4 = $("<td></td>").append(data.databaseMinorVersion);
        row.append(cel1);
        row.append(cel2);
        row.append(cel3);
        row.append(cel4);
        table.append(row);

        var table = $("#driverTableBody");
        table.empty();
        var row = $("<tr></tr>");
        var cel1 = $("<td></td>").append(data.driverName);
        var cel2 = $("<td></td>").append(data.driverVersion);
        var cel3 = $("<td></td>").append(data.driverMajorVersion);
        var cel4 = $("<td></td>").append(data.driverMinorVersion);
        row.append(cel1);
        row.append(cel2);
        row.append(cel3);
        row.append(cel4);
        table.append(row);

        var table = $("#jdbcTableBody");
        table.empty();
        var row = $("<tr></tr>");
        var cel1 = $("<td></td>").append(data.jDBCMinorVersion);
        var cel2 = $("<td></td>").append(data.jDBCMajorVersion);
        row.append(cel1);
        row.append(cel2);
        table.append(row);

        var table = $("#schedulerTableBody");
        table.empty();
        var row = $("<tr></tr>");
        var cel1 = $("<td></td>").append(data.scheduler.schedulerInstanceVersion);
        var cel2 = $("<td></td>").append(data.scheduler.schedulerReloadIsRunning.toString());
        row.append(cel1);
        row.append(cel2);
        table.append(row);

        var table = $("#schedulerDateTableBody");
        table.empty();
        var row = $("<tr></tr>");
        var cel1 = $("<td></td>").append(data.scheduler.serverDate);
        var cel2 = $("<td></td>").append(data.scheduler.serverTimeZone);
        row.append(cel1);
        row.append(cel2);
        table.append(row);

        var table = $("#schDetTableBody");
        table.empty();
        $.each(data.scheduler["schedulerTriggers"], function (idx, obj) {
            var row = $("<tr></tr>");
            var cel1 = $("<td rowspan='2'></td>").append(obj.triggerType);
            row.append(cel1);
            var cel1 = $("<td rowspan='2'></td>").append(obj.triggerName);
            row.append(cel1);
            var cel1 = $("<td></td>").append(obj.triggerNextFiretime);
            console.info(obj.triggerNextFiretime);
            row.append(cel1);
            var cel1 = $("<td></td>").append(obj.triggerUserCreated);
            row.append(cel1);
            table.append(row);
            var row = $("<tr></tr>");
            var cel1 = $("<td colspan='2'></td>").append(obj.triggerCronDefinition);
            row.append(cel1);
            table.append(row);
        });

        hideLoader('#panelActivity');
        hideLoader('#panelInformation');
        hideLoader('#paneljvmInformation');
        hideLoader('#paneldtbInformation');
        hideLoader('#panelschInformation');

    });

}

function FormatedExeId(id) {
    if (id === 0) {
        return id
    } else {
        return "<a href='TestCaseExecution.jsp?executionId=" + id + "'>" + id + "</a>";
    }
}

function FormatedTag(tag) {
    if (tag === undefined) {
        return tag
    } else {
        return "<a href='ReportingExecutionByTag.jsp?Tag=" + tag + "'>" + tag + "</a>";
    }
}
