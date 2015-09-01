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
        //configure and create the dataTable
        var configurations = new TableConfigurationsServerSide("logViewerTable", "ReadLogEvent", "aaData", aoColumnsFunc());

        createDataTable(configurations);
        //By default, sort the log messages from newest to oldest
        var oTable = $("#logViewerTable").dataTable();
        oTable.fnSort([0, 'desc']);
    });
});

function displayPageLabel() {
    var doc = new Doc();

    displayHeaderLabel(doc);
    $("#pageTitle").html(doc.getDocLabel("page_logviewer", "title"));
    $("#title").html(doc.getDocOnline("page_logviewer", "title"));
    displayFooter(doc);
}

function aoColumnsFunc() {
    var doc = new Doc();

    var aoColumns = [
        {"data": "time", "sName": "Time", "title": doc.getDocOnline("logevent", "time")},
        {"data": "login", "sName": "Login", "title": doc.getDocOnline("logevent", "login")},
        {"data": "page", "sName": "Page", "title": doc.getDocOnline("logevent", "page")},
        {"data": "action", "sName": "Action", "title": doc.getDocOnline("logevent", "action")},
        {"data": "log", "sName": "Log", "title": doc.getDocOnline("logevent", "log")}
    ];
    return aoColumns;
}