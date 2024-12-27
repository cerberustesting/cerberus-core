/*
 * Cerberus Copyright (C) 2013 - 2025 cerberustesting
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

        // 
        var doc = new Doc();

    });
});

function initPage() {
    displayPageLabel();
    // handle the click for specific action buttons
//    $("#editLabelButton").click(editEntryModalSaveHandler);

    //clear the modals fields when closed
//    $('#addLabelModal').on('hidden.bs.modal', addEntryModalCloseHandler);
//    $('#editLabelModal').on('hidden.bs.modal', editEntryModalCloseHandler);

//    $('#editLabelModal #editLabelModalForm #type').on('change', showHideRequirementPanelEdit);
//
//    $('#addLabelModal #addLabelModalForm #type').on('change', showHideRequirementPanelAdd);
//
    tinymce.init({
        selector: ".wysiwyg"
    });

    //configure and create the dataTable
    // + getUser().defaultSystemsQuery
    var configurations = new TableConfigurationsServerSide("tcTable", "api/testcases/objects", "contentTable", aoColumnsFunc_TestCases("TCTable"), [2, 'asc']);
    createDataTableWithPermissions(configurations, renderOptionsForTestCases, "#tcList", undefined, false, refreshTestcaseResultSummary);
}

function refreshTestcaseResultSummary(data) {
    $("#nbTC").text(data.iTotalRecords);
}

function displayPageLabel() {
    var doc = new Doc();

    displayHeaderLabel(doc);
    $("#pageTitle").html(doc.getDocLabel("page_impactAnalysis", "title"));
    $("#title").html(doc.getDocOnline("page_impactAnalysis", "title"));
    $("[name='btnLoad']").html(doc.getDocLabel("page_global", "btn_search"));
    $("[name='tabTC']").html(doc.getDocLabel("page_impactAnalysis", "tabTestCases") + " <span id='nbTC' class='label label-primary'></span>");
    $("[name='tabDL']").html(doc.getDocOnline("page_impactAnalysis", "tabDataLib"));
    $("[name='tabAPP']").html(doc.getDocOnline("page_impactAnalysis", "tabApplications"));
    $("[name='tabSRV']").html(doc.getDocOnline("page_impactAnalysis", "tabServices"));
    $("[name='searchQ']").attr("placeholder", doc.getDocOnline("page_impactAnalysis", "searchPlaceholderServices"));

    displayFooter(doc);
}

function loadAllTables() {
// tcTable_filter
//    $("#tcTable_filter [type='search']").val("TOTO");
    let searchString = $("#searchQ").val();
    $("#tcTable").DataTable().search(searchString).draw();
}

function searchKeyDown() {
    if (event.keyCode == 13) {
        loadAllTables();
    }
}

function emptySearch() {
    $("#searchQ").val("");
    $("#tcTable").DataTable().search("").draw();
}

function renderOptionsForTestCases(data) {
    var doc = new Doc();
    //check if user has permissions to perform the add and import operations
//    if (data["hasPermissions"]) {
//        if ($("#createLabelButton").length === 0) {
//            var contentToAdd = "<div class='marginBottom10'><button id='createLabelButton' type='button' class='btn btn-default'>\n\
//            <span class='glyphicon glyphicon-plus-sign'></span> " + doc.getDocLabel("page_label", "btn_create") + "</button></div>";
//            $("#labelsTable_wrapper div#labelsTable_length").before(contentToAdd);
//            $('#labelList #createLabelButton').click(addEntryClick);
//        }
//    }
}


function textMatch(text) {
    let searchString = $("#searchQ").val().toLowerCase();
    return  (text !== "" && searchString !== "" && text.toLowerCase().includes(searchString))
}

function formatedTextMatched(text) {
    return "<div style='background-color:lightyellow;;padding:0%;'>" + text + "</div>";
}

function aoColumnsFunc_TestCases(tableId) {
    var doc = new Doc();

    var aoColumns = [
        {
            "data": null,
            "title": doc.getDocLabel("page_global", "columnAction"),
            "bSortable": false,
            "bSearchable": false,
            "sWidth": "80px",
            "mRender": function (data, type, obj) {
                let targetUrl = "TestCaseScript.jsp?test=" + encodeURI(obj.test) + "&testcase=" + encodeURI(obj.testcase) + "&stepId=" + obj.stepId;
                switch (obj.object) {
                    case "HEADER":
                    case "PROPERTY":
                        targetUrl = "TestCaseScript.jsp?test=" + encodeURI(obj.test) + "&testcase=" + encodeURI(obj.testcase) + "&stepId=" + obj.stepId;
                        break;
                }
                var editHeader = '<button onclick="openModalTestCase(\'' + escapeHtml(obj.test) + '\',\'' + escapeHtml(obj.testcase) + '\',\'EDIT\');"\n\
                                    class="editLabel btn btn-default btn-xs margin-right5" \n\
                                    name="editHeader" title="' + doc.getDocLabel("page_impactAnalysis", "EditHeader") + '" type="button">\n\
                                    <span class="glyphicon glyphicon-pencil"></span></button>';
                var viewHeader = '<button onclick="openModalTestCase(\'' + escapeHtml(obj.test) + '\',\'' + escapeHtml(obj.testcase) + '\',\'EDIT\');"\n\
                                    class="editLabel btn btn-default btn-xs margin-right5" \n\
                                    name="viewHeader" title="' + doc.getDocLabel("page_impactAnalysis", "ViewHeader") + '" type="button">\n\
                                    <span class="glyphicon glyphicon-eye-open"></span></button>';
                var openScript = '<button onclick="window.open(\'' + targetUrl + '\');"\n\
                                    class="openScript btn btn-primary btn-xs margin-right5" \n\
                                    name="openScript" title="' + doc.getDocLabel("page_impactAnalysis", "OpenScript") + '" type="button">\n\
                                    <span class="glyphicon glyphicon-pencil"></span></button>';
                if (obj.hasPermissions) { //only draws the options if the user has the correct privileges
                    return '<div class="center btn-group width150">' + editHeader + openScript + '</div>';
                }
                return '<div class="center btn-group width150">' + viewHeader + openScript + '</div>';
            }
        },
        {
            "data": "object",
            "like": false,
            "sWidth": "50px",
            "sName": "object",
            "title": doc.getDocOnline("page_impactAnalysis", "Object")
        },
        {
            "data": "test",
            "like": false,
            "sWidth": "80px",
            "sName": "test",
            "title": doc.getDocOnline("test", "Test")
        },
        {
            "data": "testcase",
            "like": false,
            "sWidth": "50px",
            "sName": "testcase",
            "title": doc.getDocOnline("testcase", "TestCase")
        },
        {
            "data": "active",
            "like": false,
            "sWidth": "30px",
            "sName": "active",
            "title": doc.getDocOnline("testcase", "IsActive")
        },
        {
            "data": "status",
            "like": false,
            "sWidth": "50px",
            "sName": "status",
            "title": doc.getDocOnline("testcase", "Status")
        },
        {
            "data": "application",
            "sWidth": "70px",
            "sName": "application",
            "title": doc.getDocOnline("application", "Application")
        },
        {
            "data": "system",
            "sWidth": "60px",
            "sName": "system",
            "title": doc.getDocOnline("application", "system")
        },
        {
            "data": "stepId",
            "sWidth": "20px",
            "sName": "stepId",
            "title": doc.getDocOnline("page_impactAnalysis", "StepId"),
            "mRender": function (data, type, oObj) {
                return oObj.stepId === -1 ? "" : oObj.stepId;
            }
        },
        {
            "data": "actionId",
            "sWidth": "20px",
            "sName": "actionId",
            "title": doc.getDocOnline("page_impactAnalysis", "ActionId"),
            "mRender": function (data, type, oObj) {
                return oObj.actionId === -1 ? "" : oObj.actionId;
            }
        },
        {
            "data": "controlId",
            "sWidth": "20px",
            "sName": "controlId",
            "title": doc.getDocOnline("page_impactAnalysis", "ControlId"),
            "mRender": function (data, type, oObj) {
                return oObj.controlId === -1 ? "" : oObj.controlId;
            }
        },
        {
            "data": "loop",
            "sWidth": "80px",
            "sName": "loop",
            "title": doc.getDocOnline("testcasestep", "Loop")
        },
        {
            "data": "conditionOperator",
            "sWidth": "70px",
            "sName": "conditionOperator",
            "title": doc.getDocOnline("testcase", "ConditionOperator")
        },
        {
            "data": "conditionValue1",
            "sWidth": "150px",
            "sName": "conditionValue1",
            "title": doc.getDocOnline("testcase", "ConditionVal1"),
            "mRender": function (data, type, oObj) {
                if (textMatch(oObj.conditionValue1)) {
                    return formatedTextMatched(oObj.conditionValue1);
                } else {
                    return oObj.conditionValue1;
                }
            }
        },
        {
            "data": "conditionValue2",
            "sWidth": "150px",
            "sName": "conditionValue2",
            "title": doc.getDocOnline("testcase", "ConditionVal2"),
            "mRender": function (data, type, oObj) {
                if (textMatch(oObj.conditionValue2)) {
                    return formatedTextMatched(oObj.conditionValue2);
                } else {
                    return oObj.conditionValue2;
                }
            }
        },
        {
            "data": "conditionValue3",
            "sWidth": "150px",
            "sName": "conditionValue3",
            "title": doc.getDocOnline("testcase", "ConditionVal3"),
            "mRender": function (data, type, oObj) {
                if (textMatch(oObj.conditionValue3)) {
                    return formatedTextMatched(oObj.conditionValue3);
                } else {
                    return oObj.conditionValue3;
                }
            }

        },
        {
            "data": "property",
            "like": false,
            "sWidth": "70px",
            "sName": "property",
            "title": doc.getDocOnline("page_impactAnalysis", "Property")
        },
        {
            "data": "country",
            "like": false,
            "sWidth": "50px",
            "sName": "country",
            "title": doc.getDocOnline("page_impactAnalysis", "Country")
        },
        {
            "data": "actionControl",
            "sWidth": "80px",
            "sName": "actionControl",
            "title": doc.getDocOnline("page_impactAnalysis", "ActionControl")
        },
        {
            "data": "value1",
            "sWidth": "150px",
            "sName": "value1",
            "title": doc.getDocOnline("page_impactAnalysis", "Value1"),
            "mRender": function (data, type, oObj) {
                if (textMatch(oObj.value1)) {
                    return formatedTextMatched(oObj.value1);
                } else {
                    return oObj.value1;
                }
            }

        },
        {
            "data": "value2",
            "sWidth": "150px",
            "sName": "value2",
            "title": doc.getDocOnline("page_impactAnalysis", "Value2"),
            "mRender": function (data, type, oObj) {
                if (textMatch(oObj.value2)) {
                    return formatedTextMatched(oObj.value2);
                } else {
                    return oObj.value2;
                }
            }

        },
        {
            "data": "value3",
            "sWidth": "150px",
            "sName": "value3",
            "title": doc.getDocOnline("page_impactAnalysis", "Value3"),
            "mRender": function (data, type, oObj) {
                if (textMatch(oObj.value3)) {
                    return formatedTextMatched(oObj.value3);
                } else {
                    return oObj.value3;
                }
            }

        },
        {
            "data": "isFatal",
            "sWidth": "20px",
            "sName": "isFatal",
            "title": doc.getDocOnline("page_executiondetail", "fatal")
        },
        {
            "data": "doScreenshotBefore",
            "sWidth": "20px",
            "sName": "doScreenshotBefore",
            "title": doc.getDocOnline("testcasestepactioncontrol", "DoScreenshotBefore")
        },
        {
            "data": "doScreenshotAfter",
            "sWidth": "20px",
            "sName": "doScreenshotAfter",
            "title": doc.getDocOnline("testcasestepactioncontrol", "DoScreenshotAfter")
        },
        {
            "data": "waitBefore",
            "sWidth": "30px",
            "sName": "waitBefore",
            "title": doc.getDocOnline("testcasestepactioncontrol", "WaitBefore"),
            "mRender": function (data, type, oObj) {
                return oObj.waitBefore === -1 ? "" : oObj.waitBefore;
            }

        },
        {
            "data": "waitAfter",
            "sWidth": "30px",
            "sName": "waitAfter",
            "title": doc.getDocOnline("testcasestepactioncontrol", "WaitAfter"),
            "mRender": function (data, type, oObj) {
                return oObj.waitAfter === -1 ? "" : oObj.waitAfter;
            }

        },
        {
            "data": "description",
            "visible": true,
            "like": true,
            "sWidth": "150px",
            "sName": "description",
            "title": doc.getDocOnline("page_impactAnalysis", "Description"),
            "mRender": function (data, type, oObj) {
                if (textMatch(oObj.description)) {
                    return formatedTextMatched(oObj.description);
                } else {
                    return oObj.description;
                }
            }

        },
        {
            "data": "usrCreated",
            "visible": false,
            "sWidth": "30px",
            "sName": "usrCreated",
            "title": doc.getDocOnline("transversal", "UsrCreated")
        },
        {
            "data": "dateCreated",
            "visible": false,
            "like": true,
            "sWidth": "80px",
            "sName": "dateCreated",
            "title": doc.getDocOnline("transversal", "DateCreated"),
            "mRender": function (data, type, oObj) {
                return getDate(oObj["dateCreated"]);
            }
        },
        {
            "data": "usrModif",
            "visible": false,
            "sWidth": "30px",
            "sName": "usrModif",
            "title": doc.getDocOnline("transversal", "UsrModif")
        },
        {
            "data": "dateModif",
            "visible": false,
            "like": true,
            "sWidth": "80px",
            "sName": "dateModif",
            "title": doc.getDocOnline("transversal", "DateModif"),
            "mRender": function (data, type, oObj) {
                return getDate(oObj["dateModif"]);
            }
        }

    ];
    return aoColumns;
}
