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
    //configure and create the dataTable
    var configurations = new TableConfigurationsServerSide("executionsTable", "ReadExecutionInQueue", "contentTable", aoColumnsFunc(), [1, 'asc']);
    createDataTableWithPermissions(configurations, renderOptionsForApplication, "#executionList");
}

function displayPageLabel() {
    var doc = new Doc();

    $("#title").html(doc.getDocLabel("page_testcaseexecutionqueue", "allExecution"));

    displayHeaderLabel(doc);

    displayFooter(doc);
    displayGlobalLabel(doc);
}

function renderOptionsForApplication(data) {
    if ($("#blankSpace").length === 0) {
        var contentToAdd = "<div class='marginBottom10' style='height:34px;' id='blankSpace'></div>";
        $("#executionsTable_wrapper div#executionsTable_length").before(contentToAdd);
    }
}

function aoColumnsFunc(tableId) {
    var doc = new Doc();
    var aoColumns = [
        {"data": "id", "sName": "id", "title": doc.getDocLabel("page_testcaseexecutionqueue", "id_col")},
        {"data": "test", "sName": "test", "title": doc.getDocLabel("page_testcaseexecutionqueue", "test_col")},
        {
            "data": "testCase",
            "sName": "testcase",
            "title": doc.getDocLabel("page_testcaseexecutionqueue", "testcase_col")
        },
        {"data": "country", "sName": "country", "title": doc.getDocLabel("page_testcaseexecutionqueue", "country_col")},
        {
            "data": "environment",
            "sName": "environment",
            "title": doc.getDocLabel("page_testcaseexecutionqueue", "environment_col")
        },
        {"data": "browser", "sName": "browser", "title": doc.getDocLabel("page_testcaseexecutionqueue", "browser_col")},
        {"data": "tag", "sName": "tag", "title": doc.getDocLabel("page_testcaseexecutionqueue", "tag_col")},
        {
            "data": "state",
            "sName": "state",
            "title": doc.getDocLabel("page_testcaseexecutionqueue", "state_col")
        }
    ];
    return aoColumns;
}
