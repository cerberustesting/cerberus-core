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
    });
});

function initPage() {
    // The page take some parameters.
    var test = GetURLParameter("Test");
    var testCase = GetURLParameter("TestCase");
    var country = GetURLParameter("country");
    var environment = GetURLParameter("environment");

    displayPageLabel();

    var searchArray = [];
    var searchObject = {param: "col", values: "val"};

    if ((test !== null) && (test !== 'ALL')) {
        searchObject = {param: "test", values: test};
        searchArray.push(searchObject);
    }
    if ((testCase !== null) && (testCase !== 'ALL')) {
        searchObject = {param: "testcase", values: testCase};
        searchArray.push(searchObject);
    }
    if ((country !== null) && (country !== 'ALL')) {
        searchObject = {param: "country", values: country};
        searchArray.push(searchObject);
    }
    if ((environment !== null) && (environment !== 'ALL')) {
        searchObject = {param: "environment", values: environment};
        searchArray.push(searchObject);
    }

    loadTable(searchArray);
}

function loadTable(searchArray) {
    //clear the old report content before reloading it
    $("#testCaseExecution").empty();
    $("#testCaseExecution").html('<table id="testCaseExecutionTable" class="table table-hover display" name="testCaseExecutionTable">\n\
                                            </table><div class="marginBottom20"></div>');

    var contentUrl = "ReadTestCaseExecution";

    //configure and create the dataTable
    var lengthMenu = [10, 15, 20, 30, 50, 100, 500, 1000];
    var configurations = new TableConfigurationsServerSide("testCaseExecutionTable", contentUrl, "contentTable", aoColumnsFunc(), [2, 'desc'], lengthMenu, (searchArray.length > 0));
    var table = createDataTableWithPermissionsNew(configurations, undefined, "#testCaseExecution", searchArray, true, undefined, undefined);

    $("#testCaseExecutionTable tbody").on("mouseenter", "tr", function () {
        $(this).addClass("group");
    });

    $("#testCaseExecutionTable").on("draw.dt", function () {
        if (window.lucide) {
            lucide.createIcons();
        }
    });

    if (window.lucide) {
        lucide.createIcons();
    }

    if (searchArray.length > 0) {
        applyFiltersOnMultipleColumns("testCaseExecutionTable", searchArray, false);
    }

}

function displayPageLabel() {
    var doc = new Doc();

    //displayHeaderLabel(doc);
    $("#pageTitle").html(doc.getDocLabel("page_testcaseexecution", "title"));
    $("#title").html(doc.getDocOnline("page_testcaseexecution", "title"));
    $("[name='editLogEventField']").html(doc.getDocOnline("page_testcaseexecution", "button_view"));
    $("[name='logeventidField']").html(doc.getDocOnline("logevent", "logeventid"));
    $("[name='timeField']").html(doc.getDocOnline("logevent", "time"));
    $("[name='pageField']").html(doc.getDocOnline("logevent", "page"));
    $("[name='actionField']").html(doc.getDocOnline("logevent", "action"));
    $("[name='loginField']").html(doc.getDocOnline("logevent", "login"));
    $("[name='logField']").html(doc.getDocOnline("logevent", "log"));
    $("[name='remoteipField']").html(doc.getDocOnline("logevent", "remoteip"));
    $("[name='localipField']").html(doc.getDocOnline("logevent", "localip"));
    displayFooter(doc);
    displayGlobalLabel(doc);
}

function aoColumnsFunc() {
    var doc = new Doc();

    var aoColumns = [
        {
            data: null,
            bSortable: false,
            bSearchable: false,
            title: doc.getDocOnline("page_global", "columnAction"),
            sDefaultContent: "",
            sWidth: "170px",
            render: function (data, type, obj, meta) {
                const row = "row_" + meta.row;

                const baseBtnClass = "inline-flex aspect-square h-8 w-8 items-center justify-center rounded-md transition-all duration-200 " +
                    "text-slate-500 hover:bg-slate-200 dark:hover:bg-slate-800 " +
                    "opacity-20 group-hover:opacity-100 [&_svg]:size-4";

                function actionButtonLink({ id, name, title, link, icon, extraClass = "", disabled = false }) {
                    const disabledClass = disabled ? "opacity-30 cursor-not-allowed pointer-events-none" : "";

                    return `
                <a
                    id="${id}"
                    name="${name}"
                    href="${link}"
                    class="${baseBtnClass} ${extraClass} ${disabledClass}"
                    title="${title}"
                    ${disabled ? 'aria-disabled="true"' : ''}
                >
                    ${icon}
                </a>
            `;
                }

                function actionButton({ id, name, title, onClick, icon, extraClass = "", disabled = false }) {
                    const disabledClass = disabled ? "opacity-30 cursor-not-allowed" : "";

                    return `
                <button
                    id="${id}"
                    name="${name}"
                    type="button"
                    class="${baseBtnClass} ${extraClass} ${disabledClass}"
                    title="${title}"
                    ${disabled ? "disabled" : `onclick="${onClick}"`}
                >
                    ${icon}
                </button>
            `;
                }

                const icons = {
                    viewExecution: `<i data-lucide="eye" class="w-4 h-4"></i>`,
                    editScript: `<i data-lucide="file-pen-line" class="w-4 h-4"></i>`,
                    lastExec: `<i data-lucide="list-filter" class="w-4 h-4"></i>`,
                    tag: `<i data-lucide="tag" class="w-4 h-4"></i>`,
                    run: `<i data-lucide="play" class="w-4 h-4"></i>`
                };

                const test = encodeURIComponent(obj["test"]);
                const testcase = encodeURIComponent(obj["testcase"]);
                const country = encodeURIComponent(obj["country"]);
                const environment = encodeURIComponent(obj["environment"]);
                const executionId = encodeURIComponent(obj["id"]);
                const tag = encodeURIComponent(obj["tag"]);

                let buttons = [];

                // View execution
                buttons.push(actionButton({
                    id: `execution_action_view_${row}`,
                    name: "viewExecution",
                    title: doc.getDocLabel("page_executiondetail", "viewExecution"),
                    onClick: `window.location = './TestCaseExecution.jsp?executionId=${executionId}'`,
                    icon: icons.viewExecution,
                    extraClass: "group-hover:!text-blue-500"
                }));

                // Edit testcase script
                buttons.push(actionButtonLink({
                    id: `execution_action_editscript_${row}`,
                    name: "editScriptTestcase",
                    title: doc.getDocLabel("page_executiondetail", "edittc"),
                    link: `./TestCaseScript.jsp?test=${test}&testcase=${testcase}`,
                    icon: icons.editScript,
                    extraClass: "group-hover:!text-blue-500"
                }));

                // Last executions
                buttons.push(actionButtonLink({
                    id: `execution_action_lastexec_${row}`,
                    name: "lastExecution",
                    title: doc.getDocLabel("page_executiondetail", "lastexecution"),
                    link: `./TestCaseExecutionList.jsp?Test=${test}&TestCase=${testcase}&country=${country}&environment=${environment}`,
                    icon: icons.lastExec,
                    extraClass: "group-hover:!text-purple-500"
                }));

                // Execution tag
                if (!isEmpty(obj["tag"])) {
                    buttons.push(actionButtonLink({
                        id: `execution_action_tag_${obj["id"]}`,
                        name: "tagExecution",
                        title: doc.getDocLabel("page_executiondetail", "see_execution_tag"),
                        link: `./ReportingExecutionByTag.jsp?Tag=${tag}`,
                        icon: icons.tag,
                        extraClass: "group-hover:!text-orange-500"
                    }));
                }

                // Run testcase
                buttons.push(actionButtonLink({
                    id: `execution_action_runtest_${row}`,
                    name: "runTest",
                    title: doc.getDocLabel("page_executiondetail", "runtc"),
                    link: `./RunTests.jsp?test=${test}&testcase=${testcase}&country=${country}&environment=${environment}`,
                    icon: icons.run,
                    extraClass: "group-hover:!text-green-500"
                }));

                return `<div class="flex items-center justify-center gap-1">${buttons.join("")}</div>`;
            }
        },
        {
            data: "controlStatus",
            sName: "exe.controlStatus",
            title: doc.getDocOnline("page_executiondetail", "controlstatus"),
            sWidth: "100px",
            sDefaultContent: "",
            sClass: "center",
            render: function (data, type, obj) {
                if (!obj || obj === "") {
                    return obj;
                }

                const executionLink = `./TestCaseExecution.jsp?executionId=${encodeURIComponent(obj.id)}`;
                const tooltip = generateTooltip(obj);
                const status = obj.controlStatus || "";
                const config = getExecutionStatusConfig(status);

                return `
    <a
        href="${executionLink}"
        target="_blank"
        class="inline-flex no-underline hover:no-underline focus:no-underline"
        data-toggle="tooltip"
        data-html="true"
        title="${tooltip}"
    >
        <span
            class="inline-flex items-center justify-center gap-1.5 rounded-full px-2.5 py-1 text-xs font-semibold ring-1 ring-inset transition-all duration-200 hover:scale-105 hover:shadow-sm ${config.badgeClass}"
        >
            <i data-lucide="${config.icon}" class="h-3.5 w-3.5 ${config.iconClass}"></i>
            <span>${config.label}</span>
        </span>
    </a>
`;
            }
        },
        {
            "data": "id",
            "sName": "exe.id",
            "like": true,
            "title": doc.getDocOnline("page_executiondetail", "id"),
            "sWidth": "50px",
            "sDefaultContent": ""
        },
        {
            "data": "start",
            "sName": "exe.start",
            "like": true,
            "title": doc.getDocOnline("page_executiondetail", "start"),
            "sWidth": "110px",
            "sDefaultContent": "",
            "mRender": function (data, type, obj) {
                return new Date(obj.start).toLocaleString();
            }
        },
        {
            "data": "test",
            "sName": "exe.test",
            "title": doc.getDocOnline("test", "Test"),
            "sWidth": "120px",
            "sDefaultContent": ""
        },
        {
            "data": "testcase",
            "like": true,
            "sName": "exe.testcase",
            "title": doc.getDocOnline("testcase", "TestCase"),
            "sWidth": "70px",
            "sDefaultContent": ""
        },
        {
            "data": "testCaseVersion",
            "visible": false,
            "sName": "exe.TestCaseVersion",
            "title": doc.getDocOnline("testcase", "version"),
            "sWidth": "50px",
            "sDefaultContent": ""
        },
        {
            "data": "status",
            "visible": false,
            "sName": "exe.status",
            "title": doc.getDocOnline("page_executiondetail", "status"),
            "sWidth": "70px",
            "sDefaultContent": ""
        },
        {
            "data": "country",
            "sName": "exe.country",
            "title": doc.getDocOnline("page_executiondetail", "country"),
            "sWidth": "50px",
            "sDefaultContent": ""
        },
        {
            "data": "environment",
            "sName": "exe.environment",
            "title": doc.getDocOnline("page_executiondetail", "environment"),
            "sWidth": "70px",
            "sDefaultContent": ""
        },
        {
            "data": "description",
            "visible": false,
            "like": true,
            "sName": "exe.description",
            "bSearchable": true,
            "title": doc.getDocOnline("testcase", "Description"),
            "sWidth": "150px",
            "sDefaultContent": ""
        },
        {
            "data": "build",
            "visible": false,
            "sName": "exe.build",
            "title": doc.getDocOnline("page_executiondetail", "build"),
            "sWidth": "70px",
            "sDefaultContent": ""
        },
        {
            "data": "revision",
            "visible": false,
            "sName": "exe.revision",
            "title": doc.getDocOnline("page_executiondetail", "revision"),
            "sWidth": "70px",
            "sDefaultContent": ""
        },
        {
            "data": "application",
            "sName": "exe.application",
            "title": doc.getDocOnline("page_executiondetail", "application"),
            "sWidth": "100px",
            "sDefaultContent": ""
        },
        {
            "data": "applicationType",
            "sName": "exe.applicationType",
            "title": doc.getDocOnline("page_executiondetail", "applicationType"),
            "sWidth": "70px",
            "sDefaultContent": ""
        },
        {
            "data": "url",
            "visible": false,
            "sName": "exe.url",
            "title": doc.getDocOnline("page_executiondetail", "url"),
            "sWidth": "70px",
            "sDefaultContent": ""
        },
        {
            "data": "robot",
            "sName": "exe.robot",
            "title": doc.getDocOnline("page_executiondetail", "robot"),
            "sWidth": "70px",
            "sDefaultContent": ""
        },
        {
            "data": "browser",
            "visible": false,
            "sName": "exe.browser",
            "title": doc.getDocOnline("page_executiondetail", "browser"),
            "sWidth": "70px",
            "sDefaultContent": ""
        },
        {
            "data": "version",
            "visible": false,
            "sName": "exe.version",
            "title": doc.getDocOnline("page_executiondetail", "version"),
            "sWidth": "70px",
            "sDefaultContent": ""
        },
        {
            "data": "platform",
            "visible": false,
            "sName": "exe.platform",
            "title": doc.getDocOnline("page_executiondetail", "platform"),
            "sWidth": "70px",
            "sDefaultContent": ""
        },
        {
            "data": "end",
            "visible": false,
            "sName": "exe.end",
            "like": true,
            "title": doc.getDocOnline("page_executiondetail", "end"),
            "sWidth": "70px",
            "sDefaultContent": "",
            "mRender": function (data, type, obj) {
                return new Date(obj.end).toLocaleString();
            }
        },
        {
            "data": "durationMs",
            "visible": false,
            "sName": "exe.durationMs",
            "like": true,
            "title": doc.getDocOnline("testcaseexecution", "durationMs"),
            "sWidth": "70px",
            "sDefaultContent": "",
            "mRender": function (data, type, obj) {
                if (type === 'display') {
                    return getHumanReadableDuration(data / 1000);
                } else {
                    return data;
                }
            }
        },
        {
            "data": "isUseful",
            "visible": false,
            "sName": "exe.isUseful",
            "title": doc.getDocOnline("testcaseexecution", "isUseful"),
            "sWidth": "70px",
            "sDefaultContent": "",
            "mRender": function (data, type, obj) {
                if (data === true) {
                    return data;
                } else {
                    return "";
                }
            }
        },
        {
            "data": "isFlaky",
            "visible": false,
            "sName": "exe.isFlaky",
            "title": doc.getDocOnline("testcaseexecution", "isFlaky"),
            "sWidth": "70px",
            "sDefaultContent": "",
            "mRender": function (data, type, obj) {
                if (data === true) {
                    return data;
                } else {
                    return "";
                }
            }
        },
        {
            "data": "falseNegative",
            "visible": false,
            "sName": "exe.falseNegative",
            "title": doc.getDocOnline("testcaseexecution", "falseNegative"),
            "sWidth": "70px",
            "sDefaultContent": "",
            "mRender": function (data, type, obj) {
                if (data === true) {
                    return data;
                } else {
                    return "";
                }
            }
        },
        {
            "data": "controlMessage",
            "sName": "exe.controlmessage",
            "like": true,
            "title": doc.getDocOnline("page_executiondetail", "controlmessage"),
            "sWidth": "270px",
            "sDefaultContent": ""
        },
        {
            "data": "ip",
            "visible": false,
            "sName": "exe.ip",
            "title": doc.getDocOnline("page_executiondetail", "robothost"),
            "sWidth": "70px",
            "sDefaultContent": ""
        },
        {
            "data": "port",
            "visible": false,
            "sName": "exe.port",
            "title": doc.getDocOnline("page_executiondetail", "robotport"),
            "sWidth": "70px",
            "sDefaultContent": ""
        },
        {
            "data": "tag",
            "sName": "exe.tag",
            "like": true,
            "title": doc.getDocOnline("page_executiondetail", "tag"),
            "sWidth": "170px",
            "sDefaultContent": "",
            "mRender": function (data, type, obj) {
                if (data !== "") {
                    return data;
                } else {
                    $('#tagExec' + (obj["id"])).attr("disabled", "disabled");
                    return data;
                }
            }
        },
        {
            "data": "verbose",
            "visible": false,
            "sName": "exe.verbose",
            "title": doc.getDocOnline("page_executiondetail", "verbose"),
            "sWidth": "70px",
            "sDefaultContent": ""
        },
        {
            "data": "crbVersion",
            "visible": false,
            "sName": "exe.crbVersion",
            "like": true,
            "title": doc.getDocOnline("page_executiondetail", "cerberusversion"),
            "sWidth": "70px",
            "sDefaultContent": ""
        },
        {
            "data": "executor",
            "visible": false,
            "sName": "exe.executor",
            "title": doc.getDocOnline("page_executiondetail", "executor"),
            "sWidth": "70px",
            "sDefaultContent": ""
        },
        {
            "data": "screenSize",
            "visible": false,
            "sName": "exe.screensize",
            "title": doc.getDocOnline("page_executiondetail", "screensize"),
            "sWidth": "70px",
            "sDefaultContent": ""
        },
        {
            "data": "userAgent",
            "visible": false,
            "sName": "exe.userAgent",
            "title": doc.getDocOnline("page_executiondetail", "userAgent"),
            "sWidth": "130px",
            "sDefaultContent": ""
        },
        {
            "data": "queueId",
            "visible": false,
            "sName": "exe.queueId",
            "title": doc.getDocOnline("page_executiondetail", "queueId"),
            "sWidth": "130px",
            "sDefaultContent": ""
        }
    ];
    return aoColumns;
}


/**
 * Duplicated from reportbytag >> TO CLEAN
 */
function getRowClass(status) {
    var rowClass = [];

    rowClass["panel"] = "panel" + status;
    if (status === "OK") {
        rowClass["glyph"] = "glyphicon glyphicon-ok";
    } else if (status === "KO") {
        rowClass["glyph"] = "glyphicon glyphicon-remove";
    } else if (status === "FA") {
        rowClass["glyph"] = "fa fa-bug";
    } else if (status === "CA") {
        rowClass["glyph"] = "fa fa-life-ring";
    } else if (status === "PE") {
        rowClass["glyph"] = "fa fa-hourglass-half";
    } else if (status === "NE") {
        rowClass["glyph"] = "fa fa-clock-o";
    } else if (status === "NA") {
        rowClass["glyph"] = "fa fa-question";
    } else {
        rowClass["glyph"] = "";
    }
    return rowClass;
}

/**
 * DUPLICATED FROM REPORTBYTAG >>> TO REMOVE
 * @param {type} data
 * @returns {String}
 */
function generateTooltip(data) {
    var htmlRes;
    var ctrlmessage = data.controlMessage;
    if (data.controlMessage.length > 200) {
        ctrlmessage = data.controlMessage.substring(0, 200) + '...';
    }

    htmlRes = '<div><span class=\'bold\'>Execution IDs :</span> ' + data.id + '</div>'
    htmlRes += '<div><span class=\'bold\'>Country : </span>' + data.country + '</div>'
    htmlRes += '<div><span class=\'bold\'>Environment : </span>' + data.environment + '</div>'
    if (data.robotDecli !== "") {
        htmlRes += '<div><span class=\'bold\'>Robot : </span>' + data.robotDecli + ' (' + data.browser + ')</div>'
    }
    htmlRes += '<div><span class=\'bold\'>Start : </span>' + getDateMedium(data.start) + '</div>';
    if (getDateShort(data.end) !== "") {
        htmlRes += '<div><span class=\'bold\'>End : </span>' + getDateShort(data.end) + '</div>';
    }
    htmlRes += '<div>' + ctrlmessage + '</div>';

    return htmlRes;
}
