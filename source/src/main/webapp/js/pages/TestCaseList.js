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

        var urlTag = GetURLParameter('test');
        loadTestFilters(urlTag);

//        $("#editEntryButton").click(saveUpdateEntryHandler);
//        $("#addEntryButton").click(saveNewEntryHandler);
//
//        $('#editEntryModal').on('hidden.bs.modal', {extra: "#editEntryModal"}, modalFormCleaner);
//        $('#addEntryModal').on('hidden.bs.modal', {extra: "#addEntryModal"}, modalFormCleaner);

    });
});

function initPage() {
    var doc = new Doc();

    displayHeaderLabel(doc);
//    displayGlobalLabel(doc);
//    displayPageLabel(doc);
    displayFooter(doc);
}

function loadTestFilters(urlTag) {
    var jqxhr = $.get("ReadTest", {sEcho: "1"}, "json");
    $.when(jqxhr).then(function (data) {
        var messageType = getAlertType(data.messageType);
        if (messageType === "success") {
            var index;
            $('#selectTest').append($('<option></option>').attr("value", "")).attr("placeholder", "Select a Test");
            for (index = 0; index < data.contentTable.length; index++) {
                //the character " needs a special encoding in order to avoid breaking the string that creates the html element   
                var encodedString = data.contentTable[index].test.replace(/\"/g, "%22");
                var text = data.contentTable[index].test + ' - ' + data.contentTable[index].description;
                var option = $('<option></option>').attr("value", encodedString).text(text);
                $('#selectTest').append(option);
            }

            //if the tag is passed as a url parameter, then it loads the report from this tag
            if (urlTag !== null) {
                $('#selectTest option[value="' + urlTag + '"]').attr("selected", "selected");
                loadTable();
            }
        } else {
            showMessageMainPage(messageType, data.message);
        }
    }).fail(handleErrorAjaxAfterTimeout);
}

function loadTable() {
    var selectTest = $("#selectTest option:selected").attr("value");

    window.history.pushState('test', '', 'TestCaseList.jsp?test=' + selectTest);

    if ($("#testCaseTable_wrapper").hasClass("initialized")) {
        $("#testCaseList").empty();
        $("#testCaseList").html('<table id="testCaseTable" class="table table-hover display" name="testCaseTable">\n\
                                            </table><div class="marginBottom20"></div>');
    }

    if (selectTest !== "") {
        var jqxhr = $.getJSON("FindInvariantByID", "idName=COUNTRY");

        $.when(jqxhr).then(function (data) {
            var config = new TableConfigurationsServerSide("testCaseTable", "ReadTestCase?test=" + selectTest, "contentTable", aoColumnsFunc(data));

            var table = createDataTable(config);
            table.fnSort([1, 'asc']);

            $('#testCaseTable_wrapper').not('.initialized').addClass('initialized');
        });
    }
}

function editEntry(testCase) {
//    clearResponseMessageMainPage();
//    var jqxhr = $.getJSON("ReadTest", "test=" + test);
//    $.when(jqxhr).then(function (data) {
//        var obj = data["contentTable"];

    var formEdit = $('#editEntryModal');

//        formEdit.find("#test").prop("value", obj.test);
//        formEdit.find("#active").prop("value", obj.active);
//        formEdit.find("#description").prop("value", obj.description);
//        formEdit.find("#automated").prop("value", obj.automated);

    formEdit.modal('show');
//    });
}

function aoColumnsFunc(countries) {
    var aoColumns = [
        {
            "data": null,
            "bSortable": false,
            "bSearchable": false,
            "title": "Actions",
            "sDefaultContent": "",
            "mRender": function (data, type, obj) {
                if (data.hasPermissions) {
                    var editEntry = '<button id="editEntry" onclick="editEntry(\'' + obj["testCase"] + '\');"\n\
                                class="editEntry btn btn-default btn-xs margin-right5" \n\
                                name="editEntry" title="' + "edit test case" + '" type="button">\n\
                                <span class="glyphicon glyphicon-pencil"></span></button>';
                    return '<div class="center btn-group width150">' + editEntry + '</div>';
                }
            }
        },
        {
            "data": "testCase",
            "sName": "testCase",
            "title": "Test Case",
            "sDefaultContent": ""
        },
        {
            "data": "application",
            "sName": "application",
            "title": "Application",
            "sDefaultContent": ""
        },
        {
            "data": "project",
            "sName": "project",
            "title": "Project",
            "sDefaultContent": ""
        },
        {
            "data": "creator",
            "sName": "creator",
            "title": "Creator",
            "sDefaultContent": ""
        },
        {
            "data": "lastModifier",
            "sName": "lastmodifier",
            "title": "Last Modifier",
            "sDefaultContent": ""
        },
        {
            "data": "active",
            "sName": "active",
            "title": "Active",
            "sDefaultContent": "",
            "mRender": function (data, type, obj) {
                if (data === "Y") {
                    return data + '<button class="btn btn-default btn-xs btnActive">Activate</button>';
                } else if (data === "N") {
                    return data + '<button class="btn btn-default btn-xs btnActive">Disable</button>';
                }
            }
        },
        {
            "data": "status",
            "sName": "status",
            "title": "Status",
            "sDefaultContent": ""
        },
        {
            "data": "priority",
            "sName": "priority",
            "title": "Priority",
            "sDefaultContent": ""
        },
        {
            "data": "group",
            "sName": "group",
            "title": "Group",
            "sDefaultContent": ""
        },
        {
            "data": "shortDescription",
            "sName": "description",
            "title": "Description",
            "sDefaultContent": ""
        },
        {
            "data": "tcDateCrea",
            "sName": "tcDateCrea",
            "title": "Created",
            "sDefaultContent": ""
        }
    ];

    for (var index = 0; index < countries.length; index++) {
        var country = countries[index].value;
        
        var column = {
            "bSortable": false,
            "bSearchable": false,
            "title": country,
            "sDefaultContent": "",
            "mRender": function (data, type, obj) {
                if (obj.hasOwnProperty("countryList") && obj.countryList.hasOwnProperty()) {
                    return "YES";
                } else {
                    return "NO";
                }
            }
        };
        
        aoColumns.push(column);
    }

    return aoColumns;
}