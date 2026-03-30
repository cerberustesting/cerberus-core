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
        var doc = new Doc();
    });
});

function initPage() {
    displayPageLabel();

    tinymce.init({
        selector: ".wysiwyg",
        menubar: true,
        statusbar: false,
        toolbar: true,
        resize: true,
        height: 300,
        skin: 'oxide-dark'
    });

    // Configure and create the dataTable using the NEW function (same as AppServiceList)
    var configurations = new TableConfigurationsServerSide("tcTable", "api/testcases/objects", "contentTable", aoColumnsFunc_TestCases("tcTable"), [2, 'asc']);
    createDataTableWithPermissionsNew(configurations, renderOptionsForTestCases, "#tcList", undefined, false, refreshTestcaseResultSummary);

    // Add group class for hover effects + init Lucide icons
    $('#tcTable').on('draw.dt', function () {
        $(this).find('tbody tr').addClass('group');
        if (window.lucide) lucide.createIcons();
    });
}

function refreshTestcaseResultSummary(data) {
    // Update the badge counter in the button wrapper
    var $badge = $("#tcCountBadge");
    if ($badge.length) {
        $badge.text(data.iTotalRecords + " results");
    }
}

function displayPageLabel() {
    var doc = new Doc();

    $("#pageTitle").html(doc.getDocLabel("page_impactAnalysis", "title"));
    $("#title").html(doc.getDocOnline("page_impactAnalysis", "title"));

    displayFooter(doc);
}

function renderOptionsForTestCases(data) {
    var doc = new Doc();

    // Add a result count badge in the button wrapper (same pattern as AppServiceList create button)
    if ($("#tcCountBadge").length === 0) {
        var contentToAdd = `
            <div class="flex items-center gap-2">
                <span id="tcCountBadge" class="inline-flex items-center px-3 py-1 rounded-lg text-sm font-medium
                       bg-sky-100 text-sky-700 dark:bg-sky-900/30 dark:text-sky-300">
                    ${data.iTotalRecords || 0} results
                </span>
            </div>
        `;

        var $wrapper = $("#tcTable_buttonWrapper");
        if ($wrapper.length) {
            $wrapper.append(contentToAdd);
        } else {
            $("#tcTable_wrapper #tcTable_length").before("<div id='tcTable_buttonWrapper' class='flex w-full gap-2'>" + contentToAdd + "</div>");
        }
        if (window.lucide) lucide.createIcons();
    }
}

function textMatch(text) {
    // Read from the DataTable's built-in search
    var searchString = $("#tcTable_globalSearch").val();
    if (!searchString) searchString = $("#tcTable").DataTable().search();
    if (!searchString) return false;
    searchString = searchString.toLowerCase();
    return (text !== "" && searchString !== "" && text.toLowerCase().includes(searchString));
}

function formatedTextMatched(text) {
    return '<span class="px-1 rounded bg-amber-100 dark:bg-amber-900/30 text-amber-900 dark:text-amber-200">' + text + '</span>';
}

function aoColumnsFunc_TestCases(tableId) {
    var doc = new Doc();

    var aoColumns = [
        {
            "data": null,
            "title": doc.getDocLabel("page_global", "columnAction"),
            "bSortable": false,
            "bSearchable": false,
            "sWidth": "100px",
            "mRender": function (data, type, obj, meta) {
                var row = "row_" + (meta ? meta.row : 0);

                let targetUrl = "TestCaseScript.jsp?test=" + encodeURI(obj.test) + "&testcase=" + encodeURI(obj.testcase) + "&stepId=" + obj.stepId;
                switch (obj.object) {
                    case "HEADER":
                    case "PROPERTY":
                        targetUrl = "TestCaseScript.jsp?test=" + encodeURI(obj.test) + "&testcase=" + encodeURI(obj.testcase) + "&stepId=" + obj.stepId;
                        break;
                }

                const baseBtnClass = "inline-flex aspect-square h-8 w-8 items-center justify-center rounded-md transition-all duration-200 " +
                    "text-slate-500 hover:bg-slate-200 dark:hover:bg-slate-800 " +
                    "opacity-20 group-hover:opacity-100 [&_svg]:size-4";

                function actionButton({id, name, title, onClick, icon, extraClass = ""}) {
                    return `
                        <button id="${id}" name="${name}" type="button"
                            class="${baseBtnClass} ${extraClass}"
                            title="${title}"
                            onclick="${onClick}">
                            ${icon}
                        </button>`;
                }

                const icons = {
                    edit: '<i data-lucide="pencil" class="w-4 h-4"></i>',
                    view: '<i data-lucide="eye" class="w-4 h-4"></i>',
                    script: '<i data-lucide="code" class="w-4 h-4"></i>'
                };

                let buttons = [];

                // Edit / View header
                buttons.push(actionButton({
                    id: "editTC_" + row,
                    name: obj.hasPermissions ? "editHeader" : "viewHeader",
                    title: doc.getDocLabel("page_impactAnalysis", obj.hasPermissions ? "EditHeader" : "ViewHeader"),
                    onClick: "openModalTestCase('" + escapeHtml(obj.test) + "','" + escapeHtml(obj.testcase) + "','EDIT');",
                    icon: obj.hasPermissions ? icons.edit : icons.view
                }));

                // Open Script
                buttons.push(actionButton({
                    id: "openScript_" + row,
                    name: "openScript",
                    title: doc.getDocLabel("page_impactAnalysis", "OpenScript"),
                    onClick: "window.open('" + targetUrl + "');",
                    icon: icons.script,
                    extraClass: "group-hover:!text-sky-500"
                }));

                return '<div class="flex items-center gap-0.5">' + buttons.join('') + '</div>';
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
