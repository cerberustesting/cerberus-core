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
var paramActivatewebsocketpush = "N";
var paramWebsocketpushperiod = 5000;
var networkStat = {};
var configDo = {};

$.when($.getScript("js/global/global.js")).then(function () {
    $(document).ready(function () {
        var steps = [];
        var doc = new Doc();
        displayHeaderLabel(doc);
        displayFooter(doc);
        displayPageLabel(doc);

        bindToggleCollapse();

        $("#sortSize").click(function () {
            update_thirdParty_Chart(1);
        });
        $("#sortRequest").click(function () {
            update_thirdParty_Chart(2);
        });
        $("#sortTime").click(function () {
            update_thirdParty_Chart(3);
        });


        var availableUsers = getUserArray(true);
        $("#tabDetail input#executor").autocomplete({
            source: availableUsers
        });

        var executionId = GetURLParameter("executionId");
        var executionQueueId = GetURLParameter("executionQueueId");
        paramActivatewebsocketpush = getParameterString("cerberus_featureflipping_activatewebsocketpush", "", true);
        paramWebsocketpushperiod = getParameterString("cerberus_featureflipping_websocketpushperiod", "", true);

        if (isEmpty(executionId)) {
            // executionId parameter is not feed so we probably want to see the queue status.
            $("#TestCaseButton").hide();
            $("#RefreshQueueButton").show();
            $("#refreshQueue").click(function () {
                loadExecutionQueue(executionQueueId, false);
            });
            $("#editQueue").click(function () {
                openModalTestCaseExecutionQueue(executionQueueId, "EDIT");
            });

            loadExecutionQueue(executionQueueId, true);

        } else {
            $("#TestCaseButton").show();
            $("#RefreshQueueButton").hide();
            /* global */ sockets = [];
            initPage(executionId);
            loadExecutionInformation(executionId, steps, sockets);

            $('[data-toggle="popover"]').popover({
                'placement': 'auto',
                'container': 'body'}
            );
        }
    });
});

// Add the testCase to the page title (<head>)
function updatePageTitle(testcase, doc) {
    if (typeof testcase !== 'undefined') {
        if (testcase != null) {
            if (doc === undefined) {
                var doc = new Doc();
            }
            $("#pageTitle").text(doc.getDocLabel("page_executiondetail", "title") + " - " + testcase);
        }
    }
}


function loadExecutionQueue(executionQueueId, bTriggerAgain) {

    $.ajax({
        url: "ReadTestCaseExecutionQueue",
        method: "GET",
        data: "queueid=" + executionQueueId,
        datatype: "json",
        async: true,
        success: function (data) {
            if (data.messageType === "OK") {
                var tceq = data.contentTable;

                var tc = tceq.testCase;
                updatePageTitle(tc);

                var configPanel = $("#testCaseConfig");
                configPanel.find("#idlabel").text("0");
                $("[name='Separator']").text(" - ");
                configPanel.find("#country").text(tceq.country);
                configPanel.find("#environ-ment").text(tceq.environment);
                configPanel.find("#test").text(tceq.test);
                configPanel.find("#testcase").text(tceq.testCase);
                configPanel.find("#exReturnMessage").text(tceq.comment);
                configPanel.find("#controlstatus").text("QU (" + tceq.state + ")");
                // Hide the rest of the screen that will not be feed.
                $("#NavtabsScriptEdit").hide();
                $("#testCaseDetails").hide();
                $(".progress").hide();
                if (tceq.state === "QUEUED") {
                    var curDate = new Date();
                    configPanel.find("#tcDescription").html("Still <span style='color:red;'>" + tceq.nbEntryInQueueToGo + "</span> execution(s) in the Queue before execution start. <br><span class='glyphicon glyphicon-refresh spin text-info'></span> Last refresh : " + curDate);
                    if (bTriggerAgain) {
                        setTimeout(function () {
                            loadExecutionQueue(executionQueueId, true);
                        }, 5000);
                    }

                } else if (tceq.state === "STARTING") {
                    var curDate = new Date();
                    configPanel.find("#tcDescription").html("<span class='glyphicon glyphicon-refresh spin text-info'></span> Last refresh : " + curDate);
                    if (bTriggerAgain) {
                        setTimeout(function () {
                            loadExecutionQueue(executionQueueId, true);
                        }, 5000);
                    }

                } else {
                    configPanel.find("#tcDescription").html("");
                }
                if (tceq.exeId > 0) {
                    var url = "./TestCaseExecution.jsp?executionId=" + tceq.exeId;
                    //console.info("redir : " + url);
                    window.location.replace(url);
                }
            }
        },
        error: showUnexpectedError
    });
}


//global bool that say if the execution is manual
var isTheExecutionManual = false;
function loadExecutionInformation(executionId, steps, sockets) {

    $.ajax({
        url: "ReadTestCaseExecution",
        method: "GET",
        data: "executionId=" + executionId,
        datatype: "json",
        async: true,
        success: function (data) {
            var tce = data.testCaseExecution;

            var tc = tce.testcase;
            updatePageTitle(tc);

            //store in a global var if the manualExecution is set to yes to double check with the control status
            if (tce.manualExecution === "Y")
                isTheExecutionManual = true;
            updatePage(tce, steps);

            if (tce.controlStatus === "PE") {
                if (paramActivatewebsocketpush === "Y") {
                    var parser = document.createElement('a');
                    parser.href = window.location.href;

                    var protocol = "ws:";
                    if (parser.protocol === "https:") {
                        protocol = "wss:";
                    }
                    var path = parser.pathname.split("TestCaseExecution")[0];
                    var new_uri = protocol + parser.host + path + "execution/" + executionId;

                    var socket = new WebSocket(new_uri);

                    socket.onopen = function (e) {
                    } //on "écoute" pour savoir si la connexion vers le serveur websocket s'est bien faite
                    socket.onmessage = function (e) {
                        var data = JSON.parse(e.data);
                        updatePage(data, steps);
                    } //on récupère les messages provenant du serveur websocket
                    socket.onclose = function (e) {
                    } //on est informé lors de la fermeture de la connexion vers le serveur
                    socket.onerror = function (e) {
                        setTimeout(function () {
                            loadExecutionInformation(executionId, steps);
                        }, 5000);
                    } //on traite les cas d'erreur*/

                    // Remain in memory
                    sockets.push(socket);

                } else {

                    setTimeout(function () {
                        loadExecutionInformation(executionId, steps);
                    }, paramWebsocketpushperiod);

                }

            }
            $("#seeProperties").click(function () {
                $("#propertiesModal").modal('show');
            });
            //disable list-group expansion in case of clicking on link
            $('.linkified').on('click', function (e) {
                e.stopPropagation();
            });
        }
    });
}

function initPage(id) {

    var height = $("nav.navbar.navbar-inverse.navbar-static-top").outerHeight(true) + $("div.alert.alert-warning").outerHeight(true) + $(".page-title-line").outerHeight(true) - 10;

    var wrap = $(window);

    wrap.on("scroll", function (e) {
        $(".affix").width($("#page-layout").width() - 3);
    });

    $("#editTcInfo").attr("disabled", true);
    $("#runTestCase").attr("disabled", true);
    $("#rerunTestCase").attr("disabled", true);
    $("#lastExecution").attr("disabled", true);
    $("#lastExecutionoT").attr("disabled", true);

    $("#runOld").click(function () {
        window.location = "TestCaseExecution.jsp?executionId=" + id;
    });

    $("#editTags").click(function () {
        $(this).hide();
        $("#saveTag").show();
        $("#testCaseDetails #tag").attr("readonly", false);
    });

    $("#saveTag").click(function () {
        $("#testCaseDetails #tag").attr("readonly", true);
        $(this).attr("disabled", true);
        $.ajax({
            url: "SetTagToExecution",
            data: {"executionId": id, newTag: $("#testCaseDetails #tag").val()},
            success: function (data) {
                $("#saveTag").attr("disabled", false);
                $("#saveTag").hide();
                $("#editTags").show();
            }
        })
    });

    $("#inheritedPropPanelWrapper").hide();
    $("[name='buttonSave']").hide();
    $("#addProperty").hide();
    $("#duplicateButtons").hide();

    var secondaryPropertiesTable = $("#secondaryPropTable");
    secondaryPropertiesTable.hide();
    $("#showSecondaryProp").click(function () {
        secondaryPropertiesTable.show();
    });
    $("#hideSecondaryProp").click(function () {
        secondaryPropertiesTable.hide();
    });


    var wrap = $(window);
}

function displayPageLabel(doc) {

    //$("#pageTitle").text(doc.getDocLabel("page_executiondetail", "title"));
    $(".alert.alert-warning span").text(doc.getDocLabel("page_global", "beta_message"));
    $(".alert.alert-warning button").text(doc.getDocLabel("page_global", "old_page"));
    $("#more").text(doc.getDocLabel("page_executiondetail", "more_detail"));
    $("#testCaseDetails label[for='application']").text(doc.getDocLabel("page_executiondetail", "application"));
    $("#testCaseDetails label[for='browser']").text(doc.getDocLabel("page_executiondetail", "browser"));
    $("#testCaseDetails label[for='country']").text(doc.getDocLabel("page_executiondetail", "country"));
    $("#testCaseDetails label[for='environment']").text(doc.getDocLabel("page_executiondetail", "environment"));
    $("#testCaseDetails label[for='status']").text(doc.getDocLabel("page_executiondetail", "status"));
    $("#testCaseDetails label[for='controlstatus2']").text(doc.getDocLabel("page_executiondetail", "controlstatus"));
    $("#testCaseDetails label[for='controlmessage']").text(doc.getDocLabel("page_executiondetail", "controlmessage"));
    $("#testCaseDetails label[for='robothost']").text(doc.getDocLabel("page_executiondetail", "robothost"));
    $("#testCaseDetails label[for='robotport']").text(doc.getDocLabel("page_executiondetail", "robotport"));
    $("#testCaseDetails label[for='platform']").text(doc.getDocLabel("page_executiondetail", "platform"));
    $("#testCaseDetails label[for='cerberusversion']").text(doc.getDocLabel("page_executiondetail", "cerberusversion"));
    $("#testCaseDetails label[for='executor']").text(doc.getDocLabel("page_executiondetail", "executor"));
    $("#testCaseDetails label[for='url']").text(doc.getDocLabel("page_executiondetail", "url"));
    $("#testCaseDetails label[for='start']").text(doc.getDocLabel("page_executiondetail", "start"));
    $("#testCaseDetails label[for='end']").text(doc.getDocLabel("page_executiondetail", "end"));
    $("#testCaseDetails label[for='finished']").text(doc.getDocLabel("page_executiondetail", "finished"));
    $("#testCaseDetails label[for='id']").text(doc.getDocLabel("page_executiondetail", "id"));
    $("#testCaseDetails label[for='revision']").text(doc.getDocLabel("page_executiondetail", "revision"));
    $("#testCaseDetails label[for='screenSize']").text(doc.getDocLabel("page_executiondetail", "screensize"));
    $("#testCaseDetails label[for='userAgent']").text(doc.getDocLabel("page_executiondetail", "userAgent"));
    $("#testCaseDetails label[for='tag']").text(doc.getDocLabel("page_executiondetail", "tag"));
    $("#testCaseDetails label[for='exetest']").text(doc.getDocLabel("test", "Test"));
    $("#testCaseDetails label[for='exetestcase']").text(doc.getDocLabel("testcase", "TestCase"));
    $("#testCaseDetails label[for='version']").text(doc.getDocLabel("testcase", "version"));
    $("#testCaseDetails label[for='system']").text(doc.getDocLabel("invariant", "SYSTEM"));
    $("#testCaseDetails label[for='robot']").text(doc.getDocLabel("robot", "robot"));
    $("#testCaseDetails label[for='robotexe']").text(doc.getDocLabel("robotexecutor", "executor"));
    $("#testCaseDetails label[for='robotdecli']").text(doc.getDocLabel("robot", "robotdecli"));
    $("#testCaseDetails label[for='build']").text(doc.getDocLabel("page_executiondetail", "build"));
    $("#testCaseDetails label[for='version']").text(doc.getDocLabel("page_executiondetail", "version"));
    $("#steps h3").text(doc.getDocLabel("page_executiondetail", "steps"));
    $("#actions h3").text(doc.getDocLabel("page_global", "columnAction"));

    $("#btnGroupDrop1").html(doc.getDocLabel("page_executiondetail", "goto") + " <span class='caret'></span>");
    $("#lastExecution").html("<span class='glyphicon glyphicon-list'></span> " + doc.getDocLabel("page_executiondetail", "lastexecution"));
    $("#lastExecutionwithEnvCountry").html("<span class='glyphicon glyphicon-list'></span> " + doc.getDocLabel("page_executiondetail", "lastexecutionwithenvcountry"));
    $("#lastExecutionoT").html("<span class='glyphicon glyphicon-list'></span> " + doc.getDocLabel("page_executiondetail", "lastexecutionoT"));
    $("#lastExecutionoTwithEnvCountry").html("<span class='glyphicon glyphicon-list'></span> " + doc.getDocLabel("page_executiondetail", "lastexecutionoTwithenvcountry"));
    $("#ExecutionByTag").html("<span class='glyphicon glyphicon-tag'></span> " + doc.getDocLabel("page_executiondetail", "see_execution_tag"));
    $("#ExecutionQueue").html("<span class='glyphicon glyphicon-eye-open'></span> " + doc.getDocLabel("page_executiondetail", "see_executionq"));
    $("#ExecutionQueueByTag").html("<span class='glyphicon glyphicon-list'></span> " + doc.getDocLabel("page_executiondetail", "see_executionq_tag"));

    $("#btnGroupDrop2").html(doc.getDocLabel("page_executiondetail", "run") + " <span class='caret'></span>");
    $("#runTestCase").html("<span class='glyphicon glyphicon-play'></span> " + doc.getDocLabel("page_executiondetail", "runtc"));
    $("#rerunTestCase").html("<span class='glyphicon glyphicon-forward'></span> " + doc.getDocLabel("page_executiondetail", "reruntc"));
    $("#rerunFromQueue").html("<span class='glyphicon glyphicon-forward'></span> " + doc.getDocLabel("page_executiondetail", "reruntcqueue"));
    $("#rerunFromQueueandSee").html("<span class='glyphicon glyphicon-forward'></span> " + doc.getDocLabel("page_executiondetail", "reruntcqueueandsee"));
    $("#editTcInfo").html("<span class='glyphicon glyphicon-new-window'></span> " + doc.getDocLabel("page_executiondetail", "edittc"));
    $("#editTcHeader").html("<span class='glyphicon glyphicon-pencil'></span> " + doc.getDocLabel("page_executiondetail", "edittch"));
    $("#editTcStepInfo").html("<span class='glyphicon glyphicon-new-window'></span> " + doc.getDocLabel("page_executiondetail", "edittcstep"));
    $("#saveTestCaseExecution").html("<span class='glyphicon glyphicon-save'></span> " + doc.getDocLabel("page_executiondetail", "save"));

    $("#ns1Label").text(doc.getDocLabel("page_executiondetail", "ns1"));
    $("#ns2Label").text(doc.getDocLabel("page_executiondetail", "ns2"));
    $("#ns3Label").text(doc.getDocLabel("page_executiondetail", "ns3"));


    // Traceability
    $("[name='lbl_datecreated']").html(doc.getDocOnline("transversal", "DateCreated"));
    $("[name='lbl_usrcreated']").html(doc.getDocOnline("transversal", "UsrCreated"));
    $("[name='lbl_datemodif']").html(doc.getDocOnline("transversal", "DateModif"));
    $("[name='lbl_usrmodif']").html(doc.getDocOnline("transversal", "UsrModif"));

}

function updatePage(data, steps) {

    sortData(data.testCaseStepExecutionList);

    if (data.testCaseObj === undefined) {
        //console.info("testcase not exist.");
        $("#editTcInfo").attr("disabled", true);
        $("#editTcInfo").attr("href", "#");
        $("#editTcStepInfo").attr("disabled", true);
        $("#editTcStepInfo").parent().attr("href", "#");
        $("#btnGroupDrop4").unbind("click");
        $("#runTestCase").attr("disabled", true);
        $("#runTestCase").parent().attr("href", "#");
        $("#rerunTestCase").attr("disabled", true);
        $("#rerunTestCase").parent().attr("href", "#");
    } else {
        $("#editTcInfo").attr("disabled", false);
        $("#editTcInfo").attr("href", "TestCaseScript.jsp?test=" + data.test + "&testcase=" + data.testcase);
        $("#editTcStepInfo").attr("disabled", false);
        $("#editTcStepInfo").parent().attr("href", "TestCaseScript.jsp?test=" + data.test + "&testcase=" + data.testcase);
        $("#btnGroupDrop4").click(function () {
            setLinkOnEditTCStepInfoButton();
        });

        $("#runTestCase").attr("disabled", false);
        $("#runTestCase").parent().attr("href", "RunTests.jsp?test=" + data.test + "&testcase=" + data.testcase);
        $("#rerunTestCase").attr("disabled", false);
        $("#rerunTestCase").parent().attr("href", "RunTests.jsp?test=" + data.test + "&testcase=" + data.testcase + "&country=" + data.country + "&environment=" + data.environment + "&tag=" + data.tag);
    }

    $("#lastExecution").attr("disabled", false);
    $("#lastExecution").parent().attr("href", "TestCaseExecutionList.jsp?test=" + data.test + "&testcase=" + data.testcase);
    $("#lastExecutionwithEnvCountry").attr("disabled", false);
    $("#lastExecutionwithEnvCountry").parent().attr("href", "TestCaseExecutionList.jsp?test=" + data.test + "&testcase=" + data.testcase + "&country=" + data.country + "&environment=" + data.environment + "&application=" + data.application);
    $("#lastExecutionoT").attr("disabled", false);
    $("#lastExecutionoT").parent().attr("href", "ReportingExecutionOverTime.jsp?tests=" + data.test + "&testcases=" + data.testcase);
    $("#lastExecutionoTwithEnvCountry").attr("disabled", false);
    $("#lastExecutionoTwithEnvCountry").parent().attr("href", "ReportingExecutionOverTime.jsp?tests=" + data.test + "&testcases=" + data.testcase + "&countrys=" + data.country + "&environments=" + data.environment);
    if (!isEmpty(data.tag)) {
        $("#ExecutionByTag").parent().attr("href", "ReportingExecutionByTag.jsp?Tag=" + data.tag);
        $("#ExecutionQueueByTag").parent().attr("href", "TestCaseExecutionQueueList.jsp?tag=" + data.tag);
    } else {
        $("#ExecutionByTag").attr("disabled", true);
        $("#ExecutionQueueByTag").attr("disabled", true);
    }

    if (isEmpty(data.queueId) || (data.queueId === 0)) {
        $("#ExecutionQueue").attr("disabled", "disabled");
        $("#ExecutionQueue").unbind("click");
        $("#rerunFromQueue").attr("disabled", "disabled");
        $("#rerunFromQueue").unbind("click");
        $("#rerunFromQueueandSee").attr("disabled", "disabled");
        $("#rerunFromQueueandSee").unbind("click");
    } else {
        $("#ExecutionQueue").attr("disabled", false);
        $("#ExecutionQueue").unbind("click");
        $("#ExecutionQueue").click(function () {
            openModalTestCaseExecutionQueue(data.queueId, 'EDIT');
        });
        $("#rerunFromQueue").attr("disabled", false);
        $("#rerunFromQueue").unbind("click");
        $("#rerunFromQueue").click(function () {
            openModalTestCaseExecutionQueue(data.queueId, 'DUPLICATE');
        });
        $("#rerunFromQueueandSee").attr("disabled", false);
        $("#rerunFromQueueandSee").unbind("click");
        $("#rerunFromQueueandSee").click(function () {
            triggerTestCaseExecutionQueueandSee(data.queueId);
        });
    }

    // Adding all media attached to execution.
    var fileContainer = $("#testCaseConfig #tcFileContentField");
    addFileLink(data.fileList, fileContainer, isTheExecutionManual);

    var myURL = $("#bugs").data("appBugURL");
    if (myURL === undefined) {
        // We only refresh the bugURL and call readApplication if the information is not already filed.
        $.ajax({
            url: "ReadApplication",
            data: {application: data.application},
            async: true,
            success: function (dataApp) {
                var link;

                if (data.testCaseObj !== undefined) {

                    // Display already existing bugs.
                    link = getBugIdList(data.testCaseObj.bugs, dataApp.contentTable.bugTrackerUrl);
                    $("#bugs").append(link);

                    // Adding a button to create a new bug.
                    var newBugURL = dataApp.contentTable.bugTrackerNewUrl;
                    if (!isEmpty(newBugURL)) {
                        newBugURL = newBugURL.replace(/%EXEID%/g, data.id);
                        newBugURL = newBugURL.replace(/%EXEDATE%/g, new Date(data.start).toLocaleString());
                        newBugURL = newBugURL.replace(/%TEST%/g, data.test);
                        newBugURL = newBugURL.replace(/%TESTCASE%/g, data.testcase);
                        newBugURL = newBugURL.replace(/%TESTCASEDESC%/g, data.testCaseObj.description);
                        newBugURL = newBugURL.replace(/%COUNTRY%/g, data.country);
                        newBugURL = newBugURL.replace(/%ENV%/g, data.environment);
                        newBugURL = newBugURL.replace(/%BUILD%/g, data.build);
                        newBugURL = newBugURL.replace(/%REV%/g, data.revision);
                        newBugURL = newBugURL.replace(/%BROWSER%/g, data.browser);
                        newBugURL = newBugURL.replace(/%BROWSERFULLVERSION%/g, data.browser + ' ' + data.version + ' ' + data.platform);
                        link = $('<a target="_blank" id="bugs">').attr("href", newBugURL).append($("<button class='btn btn-default btn-block marginTop5'>").text("Open a new bug"));
                    } else {
                        link = $('<a id="bugs">').attr("href", "#").append($("<button class='btn btn-default btn-block'>").text("No 'New Bug' URL Specified.").attr("title", "Please specify 'New Bug' URL on application '" + data.application + "'."));
                    }
                    $("#bugs").append(link);
                    link = $('<a id="bugs">').append($("<button class='btn btn-default btn-block marginTop5' id='editTcHeaderBug'>").text("Assign to Test Case"));
                    $("#bugs").append(link);
                    $("#editTcHeaderBug").unbind("click").click(function () {
                        openModalTestCase(data.test, data.testcase, "EDIT", "tabTCBugReport")
                    });


                }
                $("#bugs").data("appBugURL", "true");

            }
        });
    }
    setConfigPanel(data);

    createStepList(data.testCaseStepExecutionList, steps);
    createProperties(data.testCaseExecutionDataList);
    createVideo(data.videos);
    setUpClickFunctionToSaveTestCaseExecutionButton(data);
    drawDependencies(data.testCaseExecutionQueueDepList, "depTableBody", "editTabDep");

    if (data.httpStat !== undefined) {
        drawNetworkCharts(data.httpStat.stat);
    }
}


function drawNetworkCharts(data) {
    var doc = new Doc();

    $("#editTabNetwork").show();

    var title = [doc.getDocLabel("page_executiondetail", "hits"), 'total : ' + data.total.requests.nb];
    drawChart_HttpStatus(data, title, 'myChart1');

    var title = [doc.getDocLabel("page_executiondetail", "size"), 'total : ' + formatNumber(Math.round(data.total.size.sum / 1024)) + ' Kb'];
    drawChart_SizePerType(data, title, 'myChart2');

    networkStat = data;
    drawChart_PerThirdParty(networkStat, 'myChart3');

    var title = [doc.getDocLabel("page_executiondetail", "thirdPartygantt")];
    drawChart_GanttPerThirdParty(data, title, 'myChart4');

    drawTable_Requests(data, "requestTable", "#NS3Panel")

}


function drawTable_Requests(data, targetTable, targetPanel) {
    var configurations = new TableConfigurationsClientSide(targetTable, data.requests, aoColumnsFunc(), true, [0, 'asc']);
    configurations.lengthMenu = [10, 25, 50, 100, 10000];

    if ($('#' + targetTable).hasClass('dataTable') === false) {
        createDataTableWithPermissions(configurations, undefined, targetPanel);
        showTitleWhenTextOverflow();
    } else {
        var oTable = $("#requestTable").dataTable();
        oTable.fnClearTable();
        if (data.requests.length > 0) {
            oTable.fnAddData(data.requests);
        }
    }
}

function aoColumnsFunc() {
    var doc = new Doc();

    var aoColumns = [
        {"data": "start", "bSortable": true, "sName": "start", "title": doc.getDocOnline("page_executiondetail", "t_start"), "sWidth": "70px"},
        {"data": "provider", "bSortable": true, "sName": "provider", "title": doc.getDocOnline("page_executiondetail", "t_provider"), "sWidth": "100px"},
        {"data": "domain", "bSortable": true, "visible": false, "sName": "domain", "title": doc.getDocOnline("page_executiondetail", "t_domain"), "sWidth": "70px"},
        {"data": "url", "bSortable": true, "sName": "url", "title": doc.getDocOnline("page_executiondetail", "t_url"), "sWidth": "200px"},
        {"data": "contentType", "bSortable": true, "sName": "contentType", "title": doc.getDocOnline("page_executiondetail", "t_contentType"), "sWidth": "70px"},
        {"data": "httpStatus", "bSortable": true, "sName": "httpStatus", "title": doc.getDocOnline("page_executiondetail", "t_httpStatus"), "sWidth": "50px"},
        {"data": "size", "bSortable": true, "sName": "size", "title": doc.getDocOnline("page_executiondetail", "t_size"), "sWidth": "50px"},
        {"data": "time", "bSortable": true, "sName": "time", "title": doc.getDocOnline("page_executiondetail", "t_time"), "sWidth": "50px"}
    ];

    return aoColumns;
}


function drawChart_HttpStatus(data, titletext, target) {

    var dataArray = [];
    var labelArray = [];
    var bgColorArray = [];

    if (data.hasOwnProperty("total")) {

        var newDataArray = [];
        for (var key in data.total.requests) {
            if ((!key.includes("XX") && key.includes("nb") && (key !== "nb")) && (data.total.requests[key] > 0)) {
                var entry = {
                    nb: data.total.requests[key],
                    name: key,
                    color: drawChart_HttpStatus_Color(key)
                };
                newDataArray.push(entry);
            }
        }
        // Sorting values by nb of requests.
        sortedArrayOfObj = newDataArray.sort(function (a, b) {
            return b.nb - a.nb;
        });

        sortedArrayOfObj.forEach(function (d) {
            dataArray.push(d.nb);
            labelArray.push(d.name);
            bgColorArray.push(d.color);
        });

        var config = {
            type: 'pie',
            data: {
                datasets: [{
                        data: dataArray,
                        backgroundColor: bgColorArray,
                        label: 'Hits'
                    }],
                labels: labelArray
            },
            options: {
                responsive: true,
                title: {
                    display: true,
                    text: titletext
                }
            }
        };

        var ctx = document.getElementById(target).getContext('2d');
        let chart = new Chart(ctx, config);

    }

}

function drawChart_HttpStatus_Color(i) {
    if (i !== undefined) {
        if (i.includes("nbE")) {
            return "purple";
        } else if (i.includes("nb2")) {
            return "green";
        } else if (i.includes("nb3")) {
            return "lightgreen";
        } else if (i.includes("nb4")) {
            return "orange";
        } else if (i.includes("nb5")) {
            return "red";
        }
    }
    return "grey";
}


function drawChart_SizePerType(data, titletext, target) {

    if (data.hasOwnProperty("total")) {

        var dataArray = [];
        var labelArray = [];
        var bgColorArray = [];

        var newDataArray = [];
        drawChart_SizePerType_Data(data.total.type.html.sizeSum, "html", newDataArray, "html");
        drawChart_SizePerType_Data(data.total.type.img.sizeSum, "img", newDataArray, "img");
        drawChart_SizePerType_Data(data.total.type.js.sizeSum, "js", newDataArray, "js");
        drawChart_SizePerType_Data(data.total.type.css.sizeSum, "css", newDataArray, "css");
        drawChart_SizePerType_Data(data.total.type.content.sizeSum, "content", newDataArray, "content");
        drawChart_SizePerType_Data(data.total.type.font.sizeSum, "font", newDataArray, "font");
        drawChart_SizePerType_Data(data.total.type.other.sizeSum, "other", newDataArray, "other");
        drawChart_SizePerType_Data(data.total.type.media.sizeSum, "media", newDataArray, "media");

        // Sorting values by nb of requests.
        sortedArrayOfObj = newDataArray.sort(function (a, b) {
            return b.nb - a.nb;
        });

        sortedArrayOfObj.forEach(function (d) {
            dataArray.push(d.nb);
            labelArray.push(d.name);
            bgColorArray.push(d.color);
        });

        var config = {
            type: 'pie',
            data: {
                datasets: [{
                        data: dataArray,
                        backgroundColor: bgColorArray,
                        label: 'Size'
                    }],
                labels: labelArray
            },
            options: {
                responsive: true,
                tooltips: {
                    enabled: true,
                    callbacks: {
                        label: function (tooltipItem, data) {
                            var label = data.labels[tooltipItem.index] + " " + data.datasets[0].label;
                            label += ': ';
                            let tmp = data.datasets[0].data[tooltipItem.index];
                            label += formatNumber(Math.round(tmp / 1024)) + " Kb";
                            return label;
                        }
                    }},
                title: {
                    display: true,
                    text: titletext
                }
            }
        };

        var ctx = document.getElementById(target).getContext('2d');
        let chart = new Chart(ctx, config);

    }

}

function drawChart_SizePerType_Data(nb, key, newDataArray, label) {
    if (nb > 0) {
        var entry = {
            nb: nb,
            name: label,
            color: drawChart_SizePerType_Color(key)
        };
        newDataArray.push(entry);
    }
}

function drawChart_SizePerType_Color(i) {
    if (i !== undefined) {
        if (i.includes("img")) {
            return "purple";
        } else if (i.includes("html")) {
            return "green";
        } else if (i.includes("content")) {
            return "lightgreen";
        } else if (i.includes("js")) {
            return "orange";
        } else if (i.includes("css")) {
            return "blue";
        } else if (i.includes("font")) {
            return "lightblue";
        } else if (i.includes("other")) {
            return "grey";
        }
    }
    return "black";
}


function drawChart_PerThirdParty(data, target) {
    var doc = new Doc();

    var titletext = [doc.getDocLabel("page_executiondetail", "thirdPartychart"), 'total : ' + data.nbThirdParty];

    var labelArray = [];

    configDo = {
        type: 'pie',
        data: {
            datasets: [],
            labels: labelArray
        },
        options: {
            circumference: Math.PI,
            rotation: Math.PI,
            responsive: true,
            tooltips: {
                enabled: true,
                callbacks: {
                    label: function (tooltipItem, data) {
                        var label = data.datasets[0].labels[tooltipItem.index] + " " + data.datasets[tooltipItem.datasetIndex].label;
                        label += ': ';
                        if (tooltipItem.datasetIndex === 0) {
                            let tmp = data.datasets[tooltipItem.datasetIndex].data[tooltipItem.index];
                            label += formatNumber(Math.round(tmp / 1024)) + " Kb";
                        } else {
                            label += formatNumber(data.datasets[tooltipItem.datasetIndex].data[tooltipItem.index]);
                        }
                        return label;
                    }
                }},
            title: {
                display: true,
                text: titletext
            }
        }
    };

    var ctx = document.getElementById(target).getContext('2d');
    window.graph1 = new Chart(ctx, configDo);

    update_thirdParty_Chart(1);
}

function update_thirdParty_Chart(sortCol) {

    var newDataArray = [];
    var dataArray1 = [];
    var dataArray2 = [];
    var dataArray3 = [];
    var labelArray = [];
    var bgColorArray = [];

    $("#sortSize").removeClass("btn-default");
    $("#sortRequest").removeClass("btn-default");
    $("#sortTime").removeClass("btn-default");
    $("#sortSize").removeClass("btn-primary");
    $("#sortRequest").removeClass("btn-primary");
    $("#sortTime").removeClass("btn-primary");

    drawChart_GetThirdPartyDataset(networkStat, newDataArray);

    // Sorting values by nb of requests.
    if (sortCol === 2) {
        sortedArrayOfObj = newDataArray.sort(function (a, b) {
            return b.nb2 - a.nb2;
        });
        $("#sortSize").addClass("btn-default");
        $("#sortRequest").addClass("btn-primary");
        $("#sortTime").addClass("btn-default");
    } else if (sortCol === 3) {
        sortedArrayOfObj = newDataArray.sort(function (a, b) {
            return b.nb3 - a.nb3;
        });
        $("#sortSize").addClass("btn-default");
        $("#sortRequest").addClass("btn-default");
        $("#sortTime").addClass("btn-primary");
    } else {
        sortedArrayOfObj = newDataArray.sort(function (a, b) {
            return b.nb1 - a.nb1;
        });
        $("#sortSize").addClass("btn-primary");
        $("#sortRequest").addClass("btn-default");
        $("#sortTime").addClass("btn-default");
    }
    sortedArrayOfObj.forEach(function (d) {
        dataArray1.push(d.nb1);
        dataArray2.push(d.nb2);
        dataArray3.push(d.nb3);
        labelArray.push(d.name);
        bgColorArray.push(d.color);
    });

    configDo.data.datasets = [];
    configDo.data.datasets.push({
        data: dataArray1,
        backgroundColor: bgColorArray,
        label: 'Size',
        labels: labelArray
    });
    configDo.data.datasets.push({
        data: dataArray2,
        backgroundColor: bgColorArray,
        label: 'Request',
        labels: labelArray
    });
    configDo.data.datasets.push({
        data: dataArray3,
        backgroundColor: bgColorArray,
        label: 'Max Time',
        labels: labelArray
    });

    configDo.data.labels = labelArray;

    window.graph1.update();
}

function drawChart_GetThirdPartyDataset(data, newDataArray) {

    // Internal stat.
    if (data.hasOwnProperty("internal")) {
        drawChart_PerThirdParty_data(data.internal.size.sum, data.internal.requests.nb, data.internal.time.max, "internal", newDataArray, "INTERNAL", "blue")
    }

    // ThirdParty stat.
    if (data.hasOwnProperty("thirdparty")) {
        for (var key in data.thirdparty) {
            drawChart_PerThirdParty_data(data.thirdparty[key].size.sum, data.thirdparty[key].requests.nb, data.thirdparty[key].time.max, key, newDataArray, key, get_Color_fromindex(newDataArray.length))
        }
    }

    // Unknown stat.
    if (data.hasOwnProperty("unknown") && data.unknown.requests.nb > 0) {
        drawChart_PerThirdParty_data(data.unknown.size.sum, data.unknown.requests.nb, data.unknown.time.max, "unknown", newDataArray, "UNKNOWN", "black")
        $("#detailUnknownList").empty();
        let entryUnknown = $('<li class="list-group-item">').text("Unknown Hosts/Domains:");
        $("#detailUnknownList").append(entryUnknown);
        for (var key in data.unknown.hosts) {
            let entryUnknown = $('<li class="list-group-item list-group-item-danger">').text(data.unknown.hosts[key]);
            $("#detailUnknownList").append(entryUnknown);
        }

    }

}

function drawChart_PerThirdParty_data(nb1, nb2, nb3, key, newDataArray, label, color) {
    if (nb1 > 0) {
        var entry = {
            nb1: nb1,
            nb2: nb2,
            nb3: nb3,
            name: label,
            color: color
        };
        newDataArray.push(entry);
    }
}

function drawChart_GanttPerThirdParty_data(start, end, key, newDataArray, label, color) {
    var entry = {
        start: start,
        end: end,
        name: label,
        color: color
    };
    newDataArray.push(entry);
}

function drawChart_GanttPerThirdParty(data, titletext, target) {

    var dataArray1 = [];
    var dataArray2 = [];
    var labelArray = [];
    var bgColorArray = [];

    var newDataArray = [];





    // Internal stat.
    if (data.hasOwnProperty("internal")) {
        drawChart_GanttPerThirdParty_data(data.internal.time.firstStartR, data.internal.time.lastEndR - data.internal.time.firstStartR, "internal", newDataArray, "INTERNAL", "blue");
    }

    // ThirdParty stat.
    if (data.hasOwnProperty("thirdparty")) {
        for (var key in data.thirdparty) {
            drawChart_GanttPerThirdParty_data(data.thirdparty[key].time.firstStartR, data.thirdparty[key].time.lastEndR - data.thirdparty[key].time.firstStartR, key, newDataArray, key, get_Color_fromindex(newDataArray.length));
        }
    }

    // Unknown stat.
    if (data.hasOwnProperty("unknown") && data.unknown.requests.nb > 0) {
        drawChart_GanttPerThirdParty_data(data.unknown.time.firstStartR, data.unknown.time.lastEndR - data.unknown.time.firstStartR, "unknown", newDataArray, "UNKNOWN", "black");
    }


    // Sorting values by nb of requests.
    sortedArrayOfObj = newDataArray.sort(function (a, b) {
        return a.start - b.start;
    });

    sortedArrayOfObj.forEach(function (d) {
        dataArray1.push(d.start);
        dataArray2.push(d.end);
        labelArray.push(d.name);
        bgColorArray.push(d.color);
    });

    var barOptions_stacked = {
        hover: {
            animationDuration: 10
        },
        scales: {
            xAxes: [{
                    label: "Duration",
                    ticks: {
                        beginAtZero: true,
                        fontFamily: "'Open Sans Bold', sans-serif",
                        fontSize: 11
                    },
                    scaleLabel: {
                        display: false
                    },
                    gridLines: {
                    },
                    stacked: true
                }],
            yAxes: [{
                    gridLines: {
                        display: false,
                        color: "#fff",
                        zeroLineColor: "#fff",
                        zeroLineWidth: 0
                    },
                    ticks: {
                        fontFamily: "'Open Sans Bold', sans-serif",
                        fontSize: 11
                    },
                    stacked: true
                }]
        },
        legend: {
            display: true
        },
        title: {
            display: true,
            text: titletext
        }
    };

    var config = {
        type: 'horizontalBar',
        data: {
            labels: labelArray,

            datasets: [{
                    label: "Start",
                    data: dataArray1,
                    backgroundColor: "rgba(63,103,126,0)",
                    hoverBackgroundColor: "rgba(50,90,100,0)"

                },
                {
                    label: "Duration",
                    data: dataArray2,
                    backgroundColor: bgColorArray,
                }]
        },
        options: barOptions_stacked,
    }

    var ctx = document.getElementById(target).getContext('2d');
    let chart = new Chart(ctx, config);

    // this part to make the tooltip only active on your real dataset
    var originalGetElementAtEvent = chart.getElementAtEvent;
    chart.getElementAtEvent = function (e) {
        return originalGetElementAtEvent.apply(this, arguments).filter(function (e) {
            return e._datasetIndex === 1;
        });
    }
}

function formatNumber(num) {
    return num.toString().replace(/(\d)(?=(\d{3})+(?!\d))/g, "$1,")
}

function createVideo(videos) {

    if (videos === undefined || videos.length === 0)
        return;

    $("#tabsScriptEdit").append("<li><a data-toggle=\"tab\" href=\"#tabVideo\" id=\"editTabVideo\" name=\"tabVideo\">Video</a></li>");


    var menuEntry = "";
    var videoEntry = "";

    var videoIndex = 0;

    videos.forEach(function (video) {
        menuEntry += "            <a href=\"javascript:void(0);\" id=\"anchorToVideo" + videoIndex + "\" name=\"anchorToVideo\" index=\"" + videoIndex + "\" class=\"list-group-item row " + (videoIndex == 0 ? "active" : "") + " \" style=\"margin-left: 0px; margin-right: 0px;\">Part " + (videoIndex + 1) + "/" + videos.length + " </a>\n";

        videoEntry +=
                "<source  id=\"video" + videoIndex + "\" index=\"" + videoIndex + "\" name='videoObject' " + (videoIndex == 0 ? "class='active'" : "") + " src=\"ReadTestCaseExecutionMedia?filename=" + video + "&filedesc=Video&filetype=MP4\" type=\"video/mp4\">\n";

        videoIndex++;
    });

    $("#testCaseDetails > div").append(
            "<div class=\"center marginTop25 tab-pane fade\" id=\"tabVideo\">\n" +
            "   <div class=\"row\">" +
            "       <div class=\"col-md-2\">\n" +
            "           <div class=\"list-group step-list side-item\">" +
            menuEntry +
            "           </div>\n" +
            "       </div>" +
            "       <div class=\"col-md-10\">" +
            "           <video id=\"videoTest\" poster=\"images/loading_2.gif\" width=\"500\" height=\"700\" controls style=\"background:black\">" +
            videoEntry +
            "           Your browser does not support the video tag." +
            "           </video>\n" +
            "       </div>" +
            "   </div>" +
            "</div>");


    var myvid = $('#videoTest').get(0);


    $("[name='anchorToVideo']").click(function () {
        $("[name='anchorToVideo']").removeClass("active");
        $(this).addClass("active");

        $("[name='videoObject']").removeClass("active");
        $("#video" + $(this).attr("index")).addClass("active");
        var activesource = $("#videoTest source.active");
        myvid.src = activesource.attr("src");
        myvid.play();
    })

    // automaticaly stream  the next part
    myvid.addEventListener('ended', function (e) {
        // get the active source and the next video source.
        // I set it so if there's no next, it loops to the first one
        var activesource = $("#videoTest source.active");
        var nextsource = $("#videoTest source.active + source");

        if (nextsource.length === 0)
            nextsource = $("#videoTest source:first-child");

        $("[name='anchorToVideo']").removeClass("active");
        $("#anchorToVideo" + nextsource.attr("index")).addClass("active");

        // deactivate current source, and activate next one
        activesource.removeClass("active");
        nextsource.addClass("active");

        // update the video source and play
        myvid.src = nextsource.attr("src");
        myvid.play();
    });

    $("#editTabVideo").click(function () { // automaticaly play video when you arrive on the video page
        myvid.play();
    });


}

function triggerTestCaseExecutionQueueandSee(queueId) {
    $.ajax({
        url: "CreateTestCaseExecutionQueue",
        async: true,
        method: "POST",
        data: {
            id: queueId,
            actionState: "toQUEUED",
            actionSave: "save"
        },
        success: function (data) {
            if (getAlertType(data.messageType) === "success") {
                showMessageMainPage(getAlertType(data.messageType), data.message, false, 60000);
                var url = "./TestCaseExecution.jsp?executionQueueId=" + data.testCaseExecutionQueueList[0].id;
                console.info("redir : " + url);
                window.location.replace(url);
            } else {
                showMessageMainPage(getAlertType(data.messageType), data.message, false, 60000);
            }
        },
        error: showUnexpectedError
    });
}


function setConfigPanel(data) {

    var configPanel = $("#testCaseConfig");
    $("[name='Separator']").text(" - ");
    configPanel.find("#idlabel").text(data.id);
    configPanel.find("#test").text(data.test);
    configPanel.find("#testcase").text(data.testcase);
    configPanel.find("#exReturnMessage").text(data.controlMessage);
    configPanel.find("#controlstatus").text(data.controlStatus);

    if (isTheExecutionManual) {
        var returnMessageField = $("<textarea style='width:100%;' class='form-control' id='returnMessageEx' placeholder='Execution Result Message'>");
        var setToNAButtonField = $("<button class='btn statusNA btn-inverse' type='button'>set to NA</button>");
        setToNAButtonField.click(function () { // automaticaly play video when you arrive on the video page
            setTestCaseReturnCodeToNA();
        });

        returnMessageField.val(data.controlMessage);
        $("#returnMessage").html(returnMessageField).append(setToNAButtonField);

    }

    $("#editTcHeader").unbind("click").click(function () {
        openModalTestCase(data.test, data.testcase, "EDIT")
    })

    configPanel.find("#environment").text(data.environment);
    configPanel.find("#country").text(data.country);
    configPanel.find("#tcDescription").text(data.description);
    configPanel.find("input#application").val(data.application);
    configPanel.find("input#browser").val(data.browser);
    configPanel.find("input#build").val(data.build);
    configPanel.find("input#country").val(data.country);
    configPanel.find("input#environment").val(data.environment);
    configPanel.find("input#environmentData").val(data.environmentData);
    configPanel.find("input#status").val(data.status);

    configPanel.find("input#end").val(getDate(data.end));
    configPanel.find("input#finished").val(data.finished);
    configPanel.find("input#id").val(data.id);
    configPanel.find("input#controlstatus2").val(data.controlStatus);
    configPanel.find("textarea#controlmessage").val(data.controlMessage);
    configPanel.find("input#robot").val(data.robot);
    configPanel.find("input#robotexe").val(data.robotExecutor);
    configPanel.find("input#robothost").val(data.robotHost);
    configPanel.find("input#robotport").val(data.robotPort);
    configPanel.find("input#platform").val(data.platform);
    configPanel.find("input#revision").val(data.revision);
    configPanel.find("input#cerberusversion").val(data.crbVersion);
    if (isTheExecutionManual) {
        $("input#executor").prop("readonly", false);
    }

    configPanel.find("input#executor").val(data.executor);
    configPanel.find("input#screenSize").val(data.screenSize);
    configPanel.find("input#userAgent").val(data.userAgent);
    configPanel.find("input#start").val(new Date(data.start));
    configPanel.find("input#tag").val(data.tag);
    configPanel.find("input#url").val(data.url);
    configPanel.find("input#exetest").val(data.test);
    configPanel.find("input#exetestcase").val(data.testcase);
    configPanel.find("input#version").val(data.version);
    configPanel.find("input#system").val(data.system);
    configPanel.find("input#robotdecli").val(data.robotDecli);
    configPanel.find("input#robotsessionid").val(data.robotSessionId);
    if (data.robotProvider === "BROWSERSTACK") {
        if (data.tagObj !== undefined) {
            let targetUrl = "https://automate.browserstack.com/builds/" + data.tagObj.browserstackBuildHash + "/sessions/" + data.robotSessionId;
            let provImg = $('<img src="./images/browserstack.png" width="20">');
            $("#sessionLinkHeader").empty().append(provImg).show();
            $("#sessionLinkHeader").parent().attr("href", targetUrl).attr("target", "_blank");
            provImg = $('<img src="./images/browserstack.png" width="20">');
            $("#sessionLink").empty().append(provImg).show();
            $("#sessionLink").parent().attr("href", targetUrl).attr("target", "_blank");
        }
    } else if (data.robotProvider === "KOBITON") {
        let targetUrl = "https://portal.kobiton.com/sessions/" + data.robotSessionId;
        let provImg = $('<img src="./images/kobiton.png" width="20">');
        $("#sessionLinkHeader").empty().append(provImg).show();
        $("#sessionLinkHeader").parent().attr("href", targetUrl).attr("target", "_blank");
        provImg = $('<img src="./images/kobiton.png" width="20">');
        $("#sessionLink").empty().append(provImg).show();
        $("#sessionLink").parent().attr("href", targetUrl).attr("target", "_blank");
    } else {
        $("#sessionLink").hide();
        $("#sessionLinkHeader").hide();
    }
    configPanel.find("input#version").val(data.version);

    if (data.conditionOperator === "always" || data.conditionOperator === "") {
        configPanel.find("#condrow1").hide();
        configPanel.find("#condrow2").hide();
    } else {
        configPanel.find("input#conditionOperatorTC").val(data.conditionOperator);
        configPanel.find("input#conditionVal1InitTC").val(data.conditionVal1Init);
        configPanel.find("input#conditionVal2InitTC").val(data.conditionVal2Init);
        configPanel.find("input#conditionVal3InitTC").val(data.conditionVal3Init);
        configPanel.find("input#conditionVal1TC").val(data.conditionVal1);
        configPanel.find("input#conditionVal2TC").val(data.conditionVal2);
        configPanel.find("input#conditionVal3TC").val(data.conditionVal3);
    }

    configPanel.find("input#usrcreated").val(data.usrCreated);
    configPanel.find("input#datecreated").val(data.dateCreated);
    configPanel.find("input#usrmodif").val(data.usrModif);
    configPanel.find("input#datemodif").val(data.dateModif);


    //setTestCaseControlStatue(data.controlStatus);
    setLoadBar(data);
}



function removeColorClass(element) {
    element.removeClass("text-black");
    element.removeClass("text-warning");
    element.removeClass("text-danger");
    element.removeClass("text-success");
    element.removeClass("text-primary");
}

/*
 * show the save button call if an action step or control have a controlStatus NE
 * @returns {undefined}
 */
function showSaveTestCaseExecutionButton() {
    $("#saveTestCaseExecution").attr("disabled", false);
}

/*
 *
 * set up click function if the button is visible ( visible if alt least one action or step or control have a controlStatus NE )
 * @param {type} data
 * @returns {undefined}
 */
function setUpClickFunctionToSaveTestCaseExecutionButton(data) {
    $("#saveTestCaseExecution").click(function () {
        saveExecution(data);
    });
}


function setLinkOnEditTCStepInfoButton() {
    var currentStep = $('#stepInfo');
    $("#editTcStepInfo").parent().attr("href", "TestCaseScript.jsp?test=" + currentStep.attr('test') + "&testcase=" + currentStep.attr('testcase') + "&step=" + currentStep.attr('step'));
}

function setLoadBar(data) {
    var total = 0;
    var ended = 0;
    if (data.testCaseObj !== undefined && data.testCaseObj.testCaseSteps !== undefined) {
        for (var i = 0; i < data.testCaseObj.testCaseSteps.length; i++) {
            var step = data.testCaseObj.testCaseSteps[i];
            var stepExec = data.testCaseStepExecutionList[i];
            if (stepExec !== undefined && stepExec.returnCode !== "PE") {
                ended += 1;
            }
            total += 1;
            for (var j = 0; j < step.actions.length; j++) {
                var action = step.actions[j];
                if (stepExec !== undefined) {
                    var actionExec = stepExec.testCaseStepActionExecutionList[j];
                    if (actionExec !== undefined && actionExec.returnCode !== "PE") {
                        ended += 1;
                    }
                }
                total += 1;
                for (var k = 0; k < action.controls.length; k++) {
                    var control = action.controls[k];
                    if (stepExec !== undefined && actionExec !== undefined) {
                        var controlExec = actionExec.testCaseStepActionControlExecutionList[k];
                        if (controlExec !== undefined && controlExec.returnCode !== "PE") {
                            ended += 1;
                        }
                    }
                    total += 1;
                }
            }
        }
    }

    var progress = ended / total * 100;
    updateDataBarVisual(data.controlStatus, progress);

}
/** DATA AGREGATION **/

function updateDataBarVisual(controlStatus, progress = 100) {

    $("#progress-bar").removeClass(function (index, className) {
        return (className.match(/(^|\s)progress-bar-\S+/g) || []).join(' ');
    });

    if (controlStatus !== "PE") {
        $("#progress-bar").removeClass("progress-bar statusOK statusKO statusNE statusNA statusWE statusFA progress-bar-warning");
        if (controlStatus === "OK") {
            $("#progress-bar").addClass("progress-bar statusOK");
        } else if (controlStatus === "KO") {
            $("#progress-bar").addClass("progress-bar statusKO");
        } else if (controlStatus === "NE") {
            $("#progress-bar").addClass("progress-bar statusNE");
        } else if (controlStatus === "NA") {
            $("#progress-bar").addClass("progress-bar statusNA");
        } else if (controlStatus === "WE" && isTheExecutionManual) {
            $("#progress-bar").addClass("progress-bar statusWE");
        } else {
            $("#progress-bar").addClass("progress-bar statusFA");
        }
        $("#progress-bar").empty().append($("<span style='font-weight:900;'>").append(controlStatus));
        progress = 100;
    }
    $("#progress-bar").css("width", progress + "%").attr("aria-valuenow", progress);
}


function sortStep(step) {

    for (var j = 0; j < step.testCaseStepActionExecutionList.length; j++) {
        var action = step.testCaseStepActionExecutionList[j];

        action.testCaseStepActionControlExecutionList.sort(function (a, b) {
            return a.sort - b.sort;
        });
    }

    step.testCaseStepActionExecutionList.sort(function (a, b) {
        return a.sort - b.sort;
    });
}

function sortData(agreg) {
    for (var i = 0; i < agreg.length; i++) {
        var step = agreg[i];

        sortStep(step);
    }

    agreg.sort(function (a, b) {
        return a.sort - b.sort;
    });
}

function sortProperties(identifier) {
    var container = $(identifier);
    var list = container.children(".property");
    list.sort(function (a, b) {

        var aProp = $(a).find("[name='masterProp']").data("property").property.toLowerCase(),
                bProp = $(b).find("[name='masterProp']").data("property").property.toLowerCase();

        if (aProp > bProp) {
            return 1;
        }
        if (aProp < bProp) {
            return -1;
        }
        return 0;
    });
    container.append(list);
}

function createProperties(propList) {
    $("#propTable").empty();

    var doc = new Doc();

    var table = $("#propTable");
    var secondaryProptable = $("#secondaryPropTable");
    var secondaryPropCount = 0;

    for (var ind = 0; ind < propList.length; ind++) {

        var isThereAnySecondaryProperty = false;
        var property = propList[ind];
        //var isSecondary = property.description.indexOf("[secondary]") >= 0;
        var isSecondary = property.rank == 2;

        if (isSecondary == true) {
            // draw the Property in the second list
            drawProperty(property, secondaryProptable, true);
            isThereAnySecondaryProperty = true;
        } else {
            // draw the Property in the main list
            drawProperty(property, table, false);
        }

        // Avoid displaying the secondary properties section title if there is no secondary properties
        if (isThereAnySecondaryProperty) {
            // display the name of the secondary Properties container
            $('#secondaryPropTableHeader').css("display", "block");
            // TO DO : link it to the docTable
            secondaryPropCount++;
            console.log(secondaryPropCount);
            $('#secondaryPropCount').html(secondaryPropCount);
        }
    }
}

// Noux: I added the isSecondary in case we need the information, but for now I can manage the display by using the table parameter
function drawProperty(property, table, isSecondary) {
    var generateHeaderContent = $("<div></div>").addClass("content");
    var firstRow = $("<div></div>").addClass("row");
    var contentField = $("<div></div>").addClass("col-sm-12");
    var contentfirstRow = $("<div></div>").addClass("row").attr("id", "contentRow");
    var propertyName = $("<h4>").attr("style", "font-size:0.9em;margin:0px;line-height:1;height:0.9em;overflow:hidden;white-space: nowrap;text-overflow: ellipsis;");
    var returnMessageField = $("<h4>").attr("style", "font-size:.9em;margin:0px;line-height:1;height:.95em;overflow:hidden;white-space: nowrap;text-overflow: ellipsis;");
    var descriptionField = $("<h4>").attr("style", "font-size:1.2em;margin:0px;line-height:1;height:1.2em;overflow:hidden;white-space: nowrap;text-overflow: ellipsis;");

    returnMessageField.append(safeLinkify(property.rMessage));
    descriptionField.append(property.value);

    propertyName.append(property.property);

    contentfirstRow.append($("<div class='col-sm-3' title='" + property.property + "'>").append(propertyName));
    contentfirstRow.append($("<div class='col-sm-9'>").attr("id", "contentField").append(descriptionField).append(returnMessageField));

    contentField.append(contentfirstRow);
    firstRow.append(contentField);
    generateHeaderContent.append(firstRow);

    var idname = 'PROPERTY-' + property.property;
    var htmlElement = $("<a href='#' id ='" + idname + "'></a>").addClass("action-group action");
    var row = $("<div></div>").addClass("col-sm-10");
    row.append(generateHeaderContent);
    var button = $("<div></div>").addClass("col-sm-1").append($("<span class='glyphicon glyphicon-chevron-down'></span>").attr("style", "font-size:1.5em"));
    var propContent = getPropertyContent(property);

    if (property.RC === "OK") {
        htmlElement.prepend($("<div>").addClass("col-sm-1").append($("<span>").addClass("glyphicon glyphicon-ok").attr("style", "font-size:1.5em")));
        htmlElement.addClass("row list-group-item list-group-item-success");
        propContent.hide();
    } else if (property.RC === "PE") {
        htmlElement.prepend($("<div>").addClass("col-sm-1").append($("<span>").addClass("glyphicon glyphicon-refresh spin").attr("style", "font-size:1.5em")));
        htmlElement.addClass("row list-group-item list-group-item-info");
        propContent.hide();
    } else if (property.RC === "KO") {
        htmlElement.prepend($("<div>").addClass("col-sm-1").append($("<span>").addClass("glyphicon glyphicon-remove").attr("style", "font-size:1.5em")));
        htmlElement.addClass("row list-group-item list-group-item-danger");
        propContent.hide();
    } else if (property.RC === "NA") {
        htmlElement.prepend($("<div>").addClass("col-sm-1").append($("<span>").addClass("glyphicon glyphicon-alert").attr("style", "font-size:1.5em")));
        htmlElement.addClass("row list-group-item list-group-item-info");
        propContent.hide();
    } else { // FA
        htmlElement.prepend($("<div>").addClass("col-sm-1").append($("<span>").addClass("glyphicon glyphicon-alert").attr("style", "font-size:1.5em")));
        htmlElement.addClass("row list-group-item list-group-item-warning");
        propContent.hide();
    }

    // Starting to reduce the size of the row by the length of elements.
    $(row).find("#contentField").removeClass("col-sm-10").addClass("col-sm-" + (9 - property.fileList.length)).addClass("col-sm-" + (9 - property.fileList.length));
    // Adding all media attached to action execution.
    addFileLink(property.fileList, $(row).find("#contentRow"), isTheExecutionManual);

    htmlElement.prepend(button);
    htmlElement.prepend(row);


    table.append(htmlElement);
    table.append(propContent);

    var container1 = $("#PROPERTY-" + property.property);
    var container2 = $("#content-container-" + property.property);
    container1.click(function () {
        if (container1.find(".glyphicon-chevron-down").length > 0) {
            container1.find(".glyphicon-chevron-down").removeClass("glyphicon-chevron-down").addClass("glyphicon-chevron-up");
        } else {
            container1.find(".glyphicon-chevron-up").removeClass("glyphicon-chevron-up").addClass("glyphicon-chevron-down");
        }
        container2.toggle();
        return false;
    });

}

function getPropertyContent(property) {
    var obj = this;
    var doc = new Doc();

    var row1 = $("<div></div>").addClass("row");
    var row2 = $("<div></div>").addClass("row");
    var row3 = $("<div></div>").addClass("row");
    var row4 = $("<div></div>").addClass("row");
    var row5 = $("<div></div>").addClass("row");
    var row6 = $("<div></div>").addClass("row");
    var row7 = $("<div></div>").addClass("row");
    var container = $("<div id='content-container-" + property.property + "'></div>").addClass("action-group row list-group-item");

    var typeField = $("<input type='text' class='form-control' id='type'>").prop("readonly", true);
    var descField = $("<textarea type='text' rows='1' class='form-control' id='description'>").prop("readonly", true);
    var value1Field = $("<textarea type='text' rows='1' class='form-control' id='value1'>").prop("readonly", true);
    var value1InitField = $("<textarea type='text' rows='1' class='form-control' id='value1init'>").prop("readonly", true);
    var value2Field = $("<textarea type='text' rows='1' class='form-control' id='value2'>").prop("readonly", true);
    var value2InitField = $("<textarea type='text' rows='1' class='form-control' id='value2init'>").prop("readonly", true);
    var timeField = $("<input type='text' class='form-control' id='time'>").prop("readonly", true);
    var valueField = $("<textarea type='text' rows='1' class='form-control' id='value'>").prop("readonly", true);
    var returnCodeField = $("<input type='text' class='form-control' id='returncode'>").prop("readonly", true);
    var rankField = $("<input type='text' class='form-control' id='rank'>").prop("readonly", true);

    var returnMessageField = $("<textarea style='width:100%;' class='form-control input-sm' id='returnmessage'>").prop("readonly", true);

    var indexField = $("<input type='text' class='form-control' id='index'>").prop("readonly", true);
    var natureField = $("<input type='text' class='form-control' id='nature'>").prop("readonly", true);
    var databaseField = $("<input type='text' class='form-control' id='database'>").prop("readonly", true);
    var lengthField = $("<input type='text' class='form-control' id='length'>").prop("readonly", true);
    var rowLimitField = $("<input type='text' class='form-control' id='rowLimit'>").prop("readonly", true);
    var retryNbField = $("<input type='text' class='form-control' id='retryNb'>").prop("readonly", true);
    var retryPeriodField = $("<input type='text' class='form-control' id='retryPeriod'>").prop("readonly", true);

    var typeGroup = $("<div class='form-group'></div>").append($("<label for='type'>" + doc.getDocLabel("testcaseexecutiondata", "type") + "</label>")).append(typeField);

    // add rank
    var rankGroup = $("<div class='form-group'></div>").append($("<label for='rank'>" + doc.getDocLabel("testcasecountryproperties", "Rank") + "</label>")).append(rankField);

    var descGroup = $("<div class='form-group'></div>").append($("<label for='description'>" + doc.getDocLabel("page_executiondetail", "description") + "</label>")).append(descField);
    var value1Group = $("<div class='form-group'></div>").append($("<label for='value1'>" + doc.getDocLabel("page_executiondetail", "value1") + "</label>")).append(value1Field);
    var value1InitGroup = $("<div class='form-group'></div>").append($("<label for='value1init'>" + doc.getDocLabel("page_executiondetail", "value1init") + "</label>")).append(value1InitField);
    var timeGroup = $("<div class='form-group'></div>").append($("<label for='time'>" + doc.getDocLabel("page_executiondetail", "time") + "</label>")).append(timeField);
    var valueGroup = $("<div class='form-group'></div>").append($("<label for='value'>" + doc.getDocLabel("testcaseexecutiondata", "Value") + "</label>")).append(valueField);
    var value2Group = $("<div class='form-group'></div>").append($("<label for='value2'>" + doc.getDocLabel("page_executiondetail", "value2") + "</label>")).append(value2Field);
    var value2InitGroup = $("<div class='form-group'></div>").append($("<label for='value2init'>" + doc.getDocLabel("page_executiondetail", "value2init") + "</label>")).append(value2InitField);
    var returncodeGroup = $("<div class='form-group'></div>").append($("<label for='returncode'>" + doc.getDocLabel("page_executiondetail", "return_code") + "</label>")).append(returnCodeField);
    var returnmessageGroup = $("<div class='form-group'></div>").append($("<label for='returnmessage'>" + doc.getDocLabel("page_executiondetail", "return_message") + "</label>")).append(returnMessageField);
    var indexGroup = $("<div class='form-group'></div>").append($("<label for='sort'>" + doc.getDocLabel("testcaseexecutiondata", "index") + "</label>")).append(indexField);
    var natureGroup = $("<div class='form-group'></div>").append($("<label for='conditionOperator'>" + doc.getDocLabel("testcaseexecutiondata", "nature") + "</label>")).append(natureField);
    var lengthGroup = $("<div class='form-group'></div>").append($("<label for='conditionVal1Init'>" + doc.getDocLabel("testcaseexecutiondata", "length") + "</label>")).append(lengthField);
    var databaseGroup = $("<div class='form-group'></div>").append($("<label for='database'>" + doc.getDocLabel("testcaseexecutiondata", "database") + "</label>")).append(databaseField);
    var rowLimitGroup = $("<div class='form-group'></div>").append($("<label for='conditionVal2Init'>" + doc.getDocLabel("testcaseexecutiondata", "rowlimit") + "</label>")).append(rowLimitField);
    var retryNbGroup = $("<div class='form-group'></div>").append($("<label for='conditionVal1'>" + doc.getDocLabel("testcaseexecutiondata", "retry") + "</label>")).append(retryNbField);
    var retryPeriodGroup = $("<div class='form-group'></div>").append($("<label for='conditionVal2'>" + doc.getDocLabel("testcaseexecutiondata", "retryperiod") + "</label>")).append(retryPeriodField);

    databaseField.val(property.database);
    descField.val(property.description);
    typeField.val(property.type);
    rankField.val(property.rank);
    value1Field.val(property.value1);
    value1InitField.val(property.value1Init);
    value2Field.val(property.value2);
    value2InitField.val(property.value2Init);
    if (property.endLong !== 19700101010000000 && property.endLong !== 0) {
        timeField.val((convToDate(property.endLong) - convToDate(property.startLong)) + " ms");
    } else {
        timeField.val("...");
    }
    valueField.val(property.value);
    returnCodeField.val(property.RC);
    returnMessageField.val(property.rMessage);
    indexField.val(property.index);
    natureField.val(property.nature);
    lengthField.val(property.length);
    rowLimitField.val(property.rowLimit);
    retryNbField.val(property.retryNb);
    retryPeriodField.val(property.retryPeriod);

    row1.append($("<div></div>").addClass("col-sm-2").append(returncodeGroup));
    row1.append($("<div></div>").addClass("col-sm-10").append(descGroup));
    row2.append($("<div></div>").addClass("col-sm-2"));
    row2.append($("<div></div>").addClass("col-sm-5").append(value1InitGroup));
    row2.append($("<div></div>").addClass("col-sm-5").append(value2InitGroup));
    row3.append($("<div></div>").addClass("col-sm-1").append(typeGroup));
    row3.append($("<div></div>").addClass("col-sm-1").append(rankGroup));
    row3.append($("<div></div>").addClass("col-sm-5").append(value1Group));
    row3.append($("<div></div>").addClass("col-sm-5").append(value2Group));
    row4.append($("<div></div>").addClass("col-sm-2").append(indexGroup));
    row4.append($("<div></div>").addClass("col-sm-8").append(valueGroup));
    row4.append($("<div></div>").addClass("col-sm-2").append(timeGroup));
    row5.append($("<div></div>").addClass("col-sm-12").append(returnmessageGroup));
    row6.append($("<div></div>").addClass("col-sm-2").append(databaseGroup));
    row6.append($("<div></div>").addClass("col-sm-2").append(lengthGroup));
    row6.append($("<div></div>").addClass("col-sm-2").append(rowLimitGroup));
    row6.append($("<div></div>").addClass("col-sm-2").append(natureGroup));
    row6.append($("<div></div>").addClass("col-sm-2").append(retryNbGroup));
    row6.append($("<div></div>").addClass("col-sm-2").append(retryPeriodGroup));

    container.append(row1);
    container.append(row2);
    container.append(row3);
    container.append(row4);
    container.append(row5);
    container.append(row6);

    return container;

}

function createPropertiesOld(propList) {
    $("#propTable").empty();

    var doc = new Doc();
    var propertyArray = [];

    var selectType = getSelectInvariant("PROPERTYTYPE", false, true).attr("disabled", true);
    var selectDB = getSelectInvariant("PROPERTYDATABASE", false, true).attr("disabled", true);
    var selectNature = getSelectInvariant("PROPERTYNATURE", false, true).attr("disabled", true);
    var table = $("#propTable");

    for (var ind = 0; ind < propList.length; ind++) {
        var property = propList[ind];
        propertyArray.push(propList[ind].property);

        var test = property.fromTest;
        var testcase = property.fromTestCase;

        var moreBtn = $("<div></div>").append($("<span></span>").addClass("glyphicon glyphicon-chevron-down").attr("style", "font-size:1.5em"));

        var rcDiv = $("<div>").addClass("col-sm-1");
        if (property.RC === "OK") {
            rcDiv.append($("<h4>").html("<span class='glyphicon glyphicon-ok pull-left' style='font-size:1.5em'></span>"))
        } else if (property.RC === "FA") {
            rcDiv.append($("<h4>").html("<span class='glyphicon glyphicon-alert pull-left' style='font-size:1.5em'></span>"))
        } else if (property.RC === "PE") {
            rcDiv.append($("<h4>").html("<span class='glyphicon glyphicon-refresh spin pull-left' style='font-size:1.5em'></span>"))
        } else {
            rcDiv.append($("<h4>").html("<span class='glyphicon glyphicon-remove pull-left' style='font-size:1.5em'></span>"))
        }
        var propertyDiv = $("<div>").addClass("col-sm-2").append($("<h4 style='overflow: hidden; text-overflow: ellipsis; white-space: nowrap'; data-toggle='tooltip'>").text(property.property));
        var typeDiv = $("<div>").addClass("col-sm-2").append($("<h4 style='overflow: hidden; text-overflow: ellipsis; white-space: nowrap'; data-toggle='tooltip'>").text(property.value));
        var messageDiv = $("<div>").addClass("col-sm-7").append($("<h4 style='overflow: hidden; text-overflow: ellipsis; white-space: nowrap'; data-toggle='tooltip'>").text(property.rMessage));

        var propertyInput = $("<textarea style='width:100%;' rows='1' id='propName' placeholder='" + doc.getDocLabel("page_testcasescript", "property_field") + "' readonly>").addClass("form-control input-sm").val(property.property);
        var descriptionInput = $("<textarea style='width:100%;' rows='1' id='propDescription' placeholder='" + doc.getDocLabel("page_testcasescript", "description_field") + "' readonly>").addClass("form-control input-sm").val(property.description);
        var valueInput = $("<textarea style='width:100%;' rows='1' placeholder='" + doc.getDocLabel("page_testcasescript", "Value") + "' readonly></textarea>").addClass("form-control input-sm").val(property.value);
        var value1Input = $("<textarea style='width:100%;' rows='1' placeholder='" + doc.getDocLabel("page_testcasescript", "value_field") + "' readonly></textarea>").addClass("form-control input-sm").val(property.value1);
        var value1InitInput = $("<textarea style='width:100%;' rows='1' placeholder='" + doc.getDocLabel("page_testcasescript", "value1init_field") + "' readonly></textarea>").addClass("form-control input-sm").val(property.value1Init);
        var value2Input = $("<textarea style='width:100%;' rows='1' placeholder='" + doc.getDocLabel("page_testcasescript", "value2_field") + "' readonly></textarea>").addClass("form-control input-sm").val(property.value2);
        var value2InitInput = $("<textarea style='width:100%;' rows='1' placeholder='" + doc.getDocLabel("page_testcasescript", "value2init_field") + "' readonly></textarea>").addClass("form-control input-sm").val(property.value2Init);
        var lengthInput = $("<input placeholder='" + doc.getDocLabel("page_testcasescript", "length_field") + "' readonly>").addClass("form-control input-sm").val(property.length);
        var rowLimitInput = $("<input placeholder='" + doc.getDocLabel("page_testcasescript", "rowlimit_field") + "' readonly>").addClass("form-control input-sm").val(property.rowLimit);
        var rcInput = $("<input placeholder='" + doc.getDocLabel("page_testcasescript", "rc") + "' readonly>").addClass("form-control input-sm").val(property.RC);
        var timeInput = $("<input placeholder='" + doc.getDocLabel("page_testcasescript", "time") + "' readonly>").addClass("form-control input-sm").val(property.endLong - property.startLong);
        var idInput = $("<input placeholder='" + doc.getDocLabel("page_testcasescript", "id") + "' readonly>").addClass("form-control input-sm").val(property.id);
        var indexInput = $("<input placeholder='" + doc.getDocLabel("page_testcasescript", "index") + "' readonly>").addClass("form-control input-sm").val(property.index);
        var rMessageInput = $("<textarea style='width:100%;' placeholder='" + doc.getDocLabel("page_testcasescript", "rMessage") + "' readonly>").addClass("form-control input-sm").val(property.rMessage);
        var retrynbInput = $("<input placeholder='" + doc.getDocLabel("page_testcasescript", "retrynb") + "' readonly>").addClass("form-control input-sm").val(property.retryNb);
        var retryperiodInput = $("<input placeholder='" + doc.getDocLabel("page_testcasescript", "retryperiod") + "' readonly>").addClass("form-control input-sm").val(property.retryperiod);


        var content = $("<div class='row property panel' style='margin-bottom:0px'></div>");
        var headerDiv = $("<div class='panel-heading' style='padding:0px; border-left: 8px solid #f0ad4e;'></div>");
        var header = $("<div class='col-sm-11'></div>");
        var propsbody = $("<div class='panel-body' style='display:none;'>");
        var props = $("<div>");
        var right = $("<div class='col-sm-1 propertyButtons' style='padding:0px;margin-top:10px;'></div>");

        var row1 = $("<div class='row' name='masterProp' style='margin-top:10px;'></div>");
        var row2 = $("<div class='row'></div>");
        var row3 = $("<div class='row'></div>");
        var row4 = $("<div class='row'></div>");
        var row5 = $("<div class='row'></div>");
        var row6 = $("<div class='row'></div>");
        var propertyName = $("<div class='col-sm-2 form-group'></div>").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "property_field"))).append(propertyInput);
        var description = $("<div class='col-sm-8 form-group'></div>").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "description_field"))).append(descriptionInput);
        var type = $("<div class='col-sm-2 form-group'></div>").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "type_field"))).append(selectType.clone().val(property.type));
        var type = $("<div class='col-sm-1 form-group'></div>").append($("<label></label>").text(doc.getDocLabel("testcasecountryproperties", "Rank"))).append(selectType.clone().val(property.type));
        var db = $("<div class='col-sm-2 form-group'></div>").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "db_field"))).append(selectDB.clone().val(property.database));
        var value = $("<div class='col-sm-8 form-group'></div>").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "value_field"))).append(valueInput);
        var value1 = $("<div class='col-sm-6 form-group'></div>").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "value1_field"))).append(value1Input);
        var value1Init = $("<div class='col-sm-6 form-group'></div>").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "value1init_field"))).append(value1InitInput);
        var value2 = $("<div class='col-sm-6 form-group'></div>").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "value2_field"))).append(value2Input);
        var value2Init = $("<div class='col-sm-6 form-group'></div>").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "value2init_field"))).append(value2InitInput);
        var length = $("<div class='col-sm-2 form-group'></div>").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "length_field"))).append(lengthInput);
        var rowLimit = $("<div class='col-sm-2 form-group'></div>").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "rowlimit_field"))).append(rowLimitInput);
        var nature = $("<div class='col-sm-2 form-group'></div>").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "nature_field"))).append(selectNature.clone().val(property.nature));
        var rc = $("<div class='col-sm-2 form-group'></div>").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "rc"))).append(rcInput);
        var time = $("<div class='col-sm-2 form-group'></div>").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "time"))).append(timeInput);
        var id = $("<div class='col-sm-2 form-group'></div>").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "id"))).append(idInput);
        var index = $("<div class='col-sm-2 form-group'></div>").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "index"))).append(indexInput);
        var rMessage = $("<div class='col-sm-12 form-group'></div>").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "rMessage"))).append(rMessageInput);
        var retrynb = $("<div class='col-sm-2 form-group'></div>").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "retrynb"))).append(retrynbInput);
        var retryperiod = $("<div class='col-sm-2 form-group'></div>").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "retryperiod"))).append(retryperiodInput);

        row1.data("property", property);
        row1.append(rMessage);
        props.append(row1);

        row2.append(propertyName);
        row2.append(type);
        row2.append(value);
        props.append(row2);

        row3.append(value1Init);
        row3.append(value2Init);
        props.append(row3);

        row4.append(value1);
        row4.append(value2);
        props.append(row4);

        row6.append(index);
        row6.append(rc);
        row6.append(description);
        props.append(row6);

        row5.append(db);
        row5.append(length);
        row5.append(rowLimit);
        row5.append(nature);
        row5.append(retrynb);
        row5.append(retryperiod);
        props.append(row5);

        header.append(rcDiv).append(propertyDiv).append(typeDiv).append(messageDiv);
        var htmlElement = headerDiv.append(header).append(right).append($("<div>").addClass("clearfix"));

        htmlElement.click(function () {
            if ($(this).find(".glyphicon-chevron-down").length > 0) {
                $(this).find(".glyphicon-chevron-down").removeClass("glyphicon-chevron-down").addClass("glyphicon-chevron-up");
            } else {
                $(this).find(".glyphicon-chevron-up").removeClass("glyphicon-chevron-up").addClass("glyphicon-chevron-down");
            }
            $(this).parent().find(".panel-body").toggle();
        });

        right.append(moreBtn);

        propsbody.append(props);

        content.append(headerDiv).append(propsbody);

        if (property.RC === "OK") {
            content.addClass("panel-success");
        } else if (property.RC === "KO") {
            content.addClass("panel-danger");
        } else if (property.RC === "PE") {
            content.addClass("panel-primary");
        } else {
            content.addClass("panel-warning");
        }

        table.append(content);
    }

    sortProperties("#inheritedPropPanel");
    return propertyArray;
}

function createStepList(data, steps) {
    $("#actionContainer").empty();
    $("#steps").empty();

    for (var i = 0; i < data.length; i++) {
        if (data[i].test === "Pre Testing") {
            var step = data[i];
            var stepObj = new Step(step, steps, i);
            $(stepObj).data("id", {stepId: i, actionId: -1, controlId: -1});
            stepObj.addElements();
            stepObj.draw();
            steps.push(stepObj);
        }
    }

    for (var i = 0; i < data.length; i++) {
        if ((data[i].test !== "Pre Testing") && (data[i].test !== "Post Testing")) {
            var step = data[i];
            var stepObj = new Step(step, steps, i);
            $(stepObj).data("id", {stepId: i, actionId: -1, controlId: -1});
            stepObj.addElements();
            stepObj.draw();
            steps.push(stepObj);
        }
    }

    for (var i = 0; i < data.length; i++) {
        if (data[i].test === "Post Testing") {
            var step = data[i];
            var stepObj = new Step(step, steps, i);
            $(stepObj).data("id", {stepId: i, actionId: -1, controlId: -1});
            stepObj.addElements();
            stepObj.draw();
            steps.push(stepObj);
        }
    }


    if (steps.length > 0) {
        $("#steps a:last-child").trigger("click");
    }
    $("#steps").data("listOfStep", steps);
}

/** JAVASCRIPT OBJECT **/

function Step(json, steps, id) {
    this.stepActionContainer = $("<div></div>").addClass("list-group").css("display", "none");
    this.description = json.description;
    this.end = json.end;
    this.fullEnd = json.fullEnd;
    this.fullStart = json.fullStart;
    this.id = json.id;
    this.returnCode = json.returnCode;
    this.returnMessage = json.returnMessage;
    this.sort = json.sort;
    this.start = json.start;
    this.step = json.step;
    this.index = json.index;
    this.loop = json.loop;
    this.test = json.test;
    this.testcase = json.testcase;
    this.timeElapsed = json.timeElapsed;
    this.conditionOperator = json.conditionOperator;
    this.conditionVal1 = json.conditionVal1;
    this.conditionVal2 = json.conditionVal2;
    this.conditionVal3 = json.conditionVal3;
    this.conditionVal1Init = json.conditionVal1Init;
    this.conditionVal2Init = json.conditionVal2Init;
    this.conditionVal3Init = json.conditionVal3Init;
    this.useStep = json.useStep;
    this.useStepTest = json.useStepTest;
    this.useStepTestCase = json.useStepTestCase;
    this.useStepTestCaseStep = json.useStepTestCaseStep;
    this.useStepStep = json.useStepStep;
    this.inLibrary = json.inLibrary;
    this.actions = [];
    this.setActions(json.testCaseStepActionExecutionList, id);

    this.steps = steps;
    this.toDelete = false;

    this.html = $("<a href='#'></a>").addClass("list-group-item row").css("margin-left", "0px").css("margin-right", "0px");
    $(this.html).data("index", id)
    if (this.test === "Pre Testing") {
        var stepDesc = "[PRE]  " + this.description + "  (" + this.timeElapsed + ")";
    } else if (this.test === "Post Testing") {
        var stepDesc = "[POST]  " + this.description + "  (" + this.timeElapsed + ")";
    } else {
        var stepDesc = "[" + this.sort + "." + +this.index + "]  " + this.description + "  (" + this.timeElapsed + ")";
    }
    this.textArea = $("<div></div>").addClass("col-lg-10").text(stepDesc);

}

Step.prototype.addElements = function () {
    var htmlElement = this.html;

    htmlElement.data("item", this);
    htmlElement.click(this.show);

    htmlElement.append(this.textArea);
    $("#steps").append(htmlElement);
    $("#actionContainer").append(this.stepActionContainer);

};

Step.prototype.draw = function () {

    var htmlElement = this.html;
    var object = htmlElement.data("item");

    if (object.returnCode === "OK") {
        htmlElement.append($("<span>").addClass("glyphicon glyphicon-ok pull-left"));
        object.html.addClass("list-group-item-success");
    } else if (object.returnCode === "PE") {
        htmlElement.append($("<span>").addClass("glyphicon glyphicon-refresh spin pull-left"));
        object.html.addClass("list-group-item-info");
    } else if (object.returnCode === "KO") {
        htmlElement.append($("<span>").addClass("glyphicon glyphicon-remove pull-left"));
        object.html.addClass("list-group-item-danger");
    } else if (object.returnCode === "NA") {
        htmlElement.append($("<span>").addClass("glyphicon glyphicon-alert pull-left"));
        object.html.addClass("list-group-item-info");
    } else if (object.returnCode === "NE") {
        htmlElement.append($("<span>").addClass("pull-left"));
        object.html.addClass("list-group-item-grey");
    } else if (object.returnCode === "FA") {
        htmlElement.append($("<span>").addClass("glyphicon glyphicon-alert pull-left"));
        object.html.addClass("list-group-item-warning");
    } else if (object.returnCode === "WE" && isTheExecutionManual) {
        htmlElement.append($("<span>").addClass("glyphicon glyphicon-question-sign pull-left"));
        object.html.addClass("list-group-item-black");
    } else {
        htmlElement.prepend($("<span>").addClass("glyphicon glyphicon-alert pull-left"));
        object.html.addClass("list-group-item-warning");
    }
}


//update display of the step
Step.prototype.update = function (idStep) {


    var glyphiconColor = "text-black";
    var className = "list-group-item-black";
    var glyphiconName = "glyphicon-question-sign";

    if (this.returnCode === "KO") {
        className = "list-group-item-danger";
        glyphiconName = "glyphicon-remove";
        glyphiconColor = "text-danger";
    } else if (this.returnCode === "FA") {
        className = "list-group-item-warning";
        glyphiconName = "glyphicon-alert";
        glyphiconColor = "text-warning";
    } else if (this.returnCode === "OK") {
        className = "list-group-item-success";
        glyphiconName = "glyphicon-ok";
        glyphiconColor = "text-success";
    }

    $($("#steps").find("a")[idStep]).removeClass(function (index, className) {
        return (className.match(/(^|\s)list-group-item-\S+/g) || []).join(' ');
    }).addClass(className);

    $($("#steps").find("a")[idStep]).find("span").removeClass(function (index, className) {
        return (className.match(/(^|\s)glyphicon-\S+/g) || []).join(' ');
    }).addClass(glyphiconName);

    var glyphIcon = $($("#stepInfo h2")[0]).removeClass(function (index, className) {
        return (className.match(/(^|\s)glyphicon-\S+/g) || []).join(' ');
    });
    //
    removeColorClass(glyphIcon);
    glyphIcon.addClass(glyphiconColor);
    glyphIcon.addClass(glyphiconName);
    //
    $("#stepRC").val(this.returnCode);
}



Step.prototype.show = function () {
    var doc = new Doc();
    var object = $(this).data("item");
    var stepDesc = $("<div>").addClass("col-xs-10");
    var stepButton = $("<div id='stepPlus'></a>").addClass("col-xs-1").addClass("paddingLeft0").addClass("paddingTop30").append($("<span class='glyphicon glyphicon-chevron-down'></span>").attr("style", "font-size:1.5em"));

    for (var i = 0; i < object.steps.length; i++) {
        var step = object.steps[i];
        step.stepActionContainer.hide();
        step.html.removeClass("active");
    }
    $("#stepInfo").empty();
    $("#stepContent").removeClass();
    $(this).addClass("active");
    if (object.returnCode === "OK") {
        $("#stepInfo").prepend($("<div>").addClass("col-xs-1").append($("<h2>").addClass("glyphicon glyphicon-ok pull-left text-success").attr("style", "font-size:3em")));
    } else if (object.returnCode === "PE") {
        $("#stepInfo").prepend($("<div>").addClass("col-xs-1").append($("<h2>").addClass("glyphicon glyphicon-refresh spin pull-left text-info").attr("style", "font-size:3em")));
    } else if (object.returnCode === "KO") {
        $("#stepInfo").prepend($("<div>").addClass("col-xs-1").append($("<h2>").addClass("glyphicon glyphicon-remove pull-left text-danger").attr("style", "font-size:3em")));
    } else if (object.returnCode === "NE") {
        $("#stepInfo").prepend($("<div>").addClass("col-xs-1").append($("<h2>").addClass("pull-left").attr("style", "font-size:3em")));
    } else if (object.returnCode === "WE" && isTheExecutionManual) {
        $("#stepInfo").prepend($("<div>").addClass("col-xs-1").append($("<h2>").addClass("glyphicon glyphicon-question-sign pull-left text-black").attr("style", "font-size:3em")));
    } else {
        $("#stepInfo").prepend($("<div>").addClass("col-xs-1").append($("<h2>").addClass("glyphicon glyphicon-alert pull-left text-warning").attr("style", "font-size:3em")));
    }


    stepDesc.append($("<h2 id='stepHeaderDescription' class='text-center' >").text(object.description));
    stepDesc.append($("<h2 id='stepHeaderMessage' class='text-center' style='font-size:1.2em;'>").text(object.returnMessage));
    $("#stepInfo").attr('test', object.test).attr('testcase', object.testcase).attr('step', object.step);
    $("#stepInfo").append(stepDesc);
    $("#stepInfo").append(stepButton);
    object.stepActionContainer.show();
    $("#stepInfo").show();

    $("#stepRC").val(object.returnCode);
    $("#stepDescription").val(object.description);
    $("#stepSort").val(object.sort);
    $("#stepLoop").val(object.loop);
    $("#stepIndex").val(object.index);
    $("#stepElapsed").val(object.timeElapsed);
    $("#stepConditionOperator").val(object.conditionOperator);
    $("#stepConditionVal1").val(object.conditionVal1);
    $("#stepConditionVal2").val(object.conditionVal2);
    $("#stepConditionVal3").val(object.conditionVal3);
    $("#stepConditionVal1Init").val(object.conditionVal1Init);
    $("#stepConditionVal2Init").val(object.conditionVal2Init);
    $("#stepConditionVal3Init").val(object.conditionVal3Init);

    if (isTheExecutionManual) {
        $("#stepRow2").hide();
        $("#stepRow3").hide();
        $("#stepRow4").hide();
    }

    if (object.conditionOperator === "always") {
        $("#stepRow3").hide();
        $("#stepRow4").hide();
    }

    returnMessageWritableForStep(object, $("#stepMessage"));

    $("#stepInfo").unbind("click").click(function () {
        $("#stepHiddenRow").toggle();
        if ($(this).find("span").hasClass("glyphicon-chevron-down")) {
            $(this).find("span").removeClass("glyphicon-chevron-down").addClass("glyphicon-chevron-up");
        } else {
            $(this).find("span").removeClass("glyphicon-chevron-up").addClass("glyphicon-chevron-down");
        }
    });

    return false;
};

Step.prototype.setActions = function (actions, idMotherStep) {
    for (var i = 0; i < actions.length; i++) {
        this.setAction(actions[i], idMotherStep, i);
    }
};

Step.prototype.setAction = function (action, idMotherStep, idAction) {
    var actionObj;
    if (action instanceof Action) {
        actionObj = action;
    } else {
        actionObj = new Action(action, this);
    }

    this.actions.push(actionObj);

    actionObj.draw(idMotherStep, idAction);

    actionObj.setControls(actionObj.controlsJson, idMotherStep, idAction);
};

Step.prototype.setDescription = function (description) {
    this.description = description;
    this.textArea.text(description);
    $("#stepHeaderDescription").text(description);
};

Step.prototype.setStep = function (step) {
    this.step = step;
};

Step.prototype.setReturnMessage = function (returnMessage) {
    this.returnMessage = returnMessage;
};

/*
 * Set the returnMessage for object with the field object for the step object
 * @param {type} object
 * @param {type} field
 * @returns {undefined}
 */
function returnMessageWritableForStep(object, field) {

    field.empty();
    field.data("currentStep", object);

    field.prop("readonly", true);
    if (isTheExecutionManual) {
        field.prop("readonly", false);
        field.change(function () {
            var currentObject = field.data("currentStep");
            currentObject.setReturnMessage(field.val());
        });
    }
    field.val(object.returnMessage);
}



//Get the json data from the input of the field
Step.prototype.getJsonData = function () {
    var json = {};
    json.conditionOperator = this.conditionOperator;
    json.conditionVal1 = this.conditionVal1;
    json.conditionVal1Init = this.conditionVal1Init;
    json.conditionVal2 = this.conditionVal2;
    json.conditionVal2Init = this.conditionVal2Init;
    json.conditionVal3 = this.conditionVal3;
    json.conditionVal3Init = this.conditionVal3Init;
    json.description = this.description;
    json.end = this.end;
    json.fullEnd = this.fullEnd;
    json.fullStart = this.fullStart;
    json.id = this.id;
    json.inLibrary = this.inLibrary;
    json.index = this.index;
    json.loop = this.loop;
    json.returnCode = this.returnCode;
    json.sort = this.sort;
    json.start = this.start;
    json.step = this.step;
    json.test = this.test;
    json.testcase = this.testcase;
    json.toDelete = this.toDelete;
    json.useStep = this.useStep;
    json.useStepStep = this.useStepStep;
    json.useStepTest = this.useStepTest;
    json.useStepTestCase = this.useStepTestCase;
    json.useStepTestCaseStep = this.useStepTestCaseStep;
    json.screenshotFileName = "";
    //Value the user is able to modified
    json.returnMessage = this.returnMessage;

    return json;
};

function Action(json, parentStep) {
    this.html = $("<a href='#'></a>").addClass("action-group action");
    this.parentStep = parentStep;

    if (json !== null) {
        this.action = json.action;
        this.description = json.description;
        this.end = json.end;
        this.endlong = json.endlong;
        this.forceExeStatus = json.forceExeStatus;
        this.id = json.id;
        this.returnCode = json.returnCode;
        this.returnMessage = json.returnMessage;
        this.sequence = json.sequence;
        this.sort = json.sort;
        this.start = json.start;
        this.startlong = json.startlong;
        this.step = json.step;
        this.index = json.index;
        this.test = json.test;
        this.testcase = json.testcase;
        this.value1 = json.value1;
        this.value2 = json.value2;
        this.value3 = json.value3;
        this.value1init = json.value1init;
        this.value2init = json.value2init;
        this.value3init = json.value3init;
        this.screenshotFileName = json.screenshotFileName;
        this.controlsJson = json.testCaseStepActionControlExecutionList;
        this.controls = [];
        this.fileList = json.fileList;
        this.conditionOperator = json.conditionOperator;
        this.conditionVal1Init = json.conditionVal1Init;
        this.conditionVal2Init = json.conditionVal2Init;
        this.conditionVal3Init = json.conditionVal3Init;
        this.conditionVal1 = json.conditionVal1;
        this.conditionVal2 = json.conditionVal2;
        this.conditionVal3 = json.conditionVal3;
    } else {
        this.action = "Unknown";
        this.description = "";
        this.end = 0;
        this.endlong = 0;
        this.forceExeStatus = "";
        this.id = 0;
        this.returnCode = "";
        this.returnMessage = "";
        this.sequence = 0;
        this.sort = 0;
        this.start = 0;
        this.startlong = 0;
        this.step = parentStep.step;
        this.index = parentStep.index;
        this.test = "";
        this.testcase = "";
        this.value1 = "";
        this.value2 = "";
        this.value3 = "";
        this.value1init = "";
        this.value2init = "";
        this.value3init = "";
        this.screenshotFileName = "";
        this.controlsJson = "";
        this.controls = [];
        this.fileList = [];
        this.conditionOperator = "always";
        this.conditionVal1Init = "";
        this.conditionVal2Init = "";
        this.conditionVal3Init = "";
        this.conditionVal1 = "";
        this.conditionVal2 = "";
        this.conditionVal3 = "";
    }

    this.toDelete = false;
    $(this.html).data("index", this.sort - 1)
}

Action.prototype.draw = function (idMotherStep, id) {

    var fullActionElement = $("<div name='fullActionDiv'></div>");
    var htmlElement = this.html;
    var action = this;
    var idCurrentElement = {stepId: idMotherStep, actionId: id, controlId: -1};

    var row = $("<div class='itemContainer'></div>").addClass("col-xs-10");
    var type = $("<div></div>").addClass("type");

    var header = this.generateHeader(idCurrentElement);

    row.append(header);
    row.data("item", this);
    //give the action an idid
    row.data("id", idCurrentElement);

    var button = $("<div></div>").addClass("marginLeft-15 col-xs-1").append($("<span class='glyphicon glyphicon-chevron-down'></span>").attr("style", "font-size:1.5em"));

    htmlElement.prepend(button);
    htmlElement.prepend(row);

    var content = this.generateContent();

    if (action.returnCode === "OK") {
        htmlElement.prepend($("<div>").addClass("col-xs-1").append($("<span>").addClass("glyphicon glyphicon-ok").attr("style", "font-size:1.5em")));
        htmlElement.addClass("row list-group-item list-group-item-success");
        content.hide();
    } else if (action.returnCode === "PE") {
        htmlElement.prepend($("<div>").addClass("col-xs-1").append($("<span>").addClass("glyphicon glyphicon-refresh spin").attr("style", "font-size:1.5em")));
        htmlElement.addClass("row list-group-item list-group-item-info");
        content.hide();
    } else if (action.returnCode === "KO") {
        htmlElement.prepend($("<div>").addClass("col-xs-1").append($("<span>").addClass("glyphicon glyphicon-remove").attr("style", "font-size:1.5em")));
        htmlElement.addClass("row list-group-item list-group-item-danger");
        content.hide();
    } else if (action.returnCode === "NA") {
        htmlElement.prepend($("<div>").addClass("col-xs-1").append($("<span>").addClass("glyphicon glyphicon-alert").attr("style", "font-size:1.5em")));
        htmlElement.addClass("row list-group-item list-group-item-info");
        content.hide();
    } else if (action.returnCode === "NE") {
        htmlElement.prepend($("<div>").addClass("col-xs-1").append($("<span>").attr("style", "font-size:1.5em")));
        htmlElement.addClass("row list-group-item list-group-item-grey");
        content.hide();
    } else if (action.returnCode === "WE" && isTheExecutionManual) {
        htmlElement.prepend($("<div>").addClass("col-xs-1").append($("<span>").addClass("glyphicon glyphicon-question-sign").attr("style", "font-size:1.5em")));
        htmlElement.addClass("row list-group-item list-group-item-black");
        content.hide();
    } else {
        htmlElement.prepend($("<div>").addClass("col-xs-1").append($("<span>").addClass("glyphicon glyphicon-alert").attr("style", "font-size:1.5em")));
        htmlElement.addClass("row list-group-item list-group-item-warning");
        content.hide();
    }

    // Starting to reduce the size of the row by the length of elements.
    $(header).find("#contentField").removeClass("col-xs-12").addClass("col-xs-" + (12 - this.fileList.length));
    // Adding all media attached to action execution.

    htmlElement.click(function () {
        if ($(this).find(".glyphicon-chevron-down").length > 0) {
            $(this).find(".glyphicon-chevron-down").removeClass("glyphicon-chevron-down").addClass("glyphicon-chevron-up");
        } else {
            $(this).find(".glyphicon-chevron-up").removeClass("glyphicon-chevron-up").addClass("glyphicon-chevron-down");
        }
        content.toggle();
        return false;
    });
    fullActionElement.append(htmlElement);
    fullActionElement.append(content);
    this.parentStep.stepActionContainer.append(fullActionElement);
    //this.parentStep.stepActionContainer.append(content);
    addFileLink(this.fileList, $(header).find(".row"), isTheExecutionManual, idMotherStep);
};

Action.prototype.setControls = function (controls, idMotherStep, idMotherAction) {
    for (var i = 0; i < controls.length; i++) {
        this.setControl(controls[i], idMotherStep, idMotherAction, i);
    }
};

Action.prototype.setControl = function (control, idMotherStep, idMotherAction, id) {
    if (control instanceof Control) {
        control.draw(idMotherStep, idMotherAction, id);
        this.controls.push(control);
    } else {
        var controlObj = new Control(control, this);

        controlObj.draw(idMotherStep, idMotherAction, id);
        this.controls.push(controlObj);
    }
};

Action.prototype.setStep = function (step) {
    this.step = step;
};

Action.prototype.setSequence = function (sequence) {
    this.sequence = sequence;
};

Action.prototype.setReturnMessage = function (returnMessage) {
    this.returnMessage = returnMessage;
};
/*
 * * Set the returnMessage for object with the field object for the action and control object
 * @param {type} object
 * @param {type} field
 * @returns {undefined}
 */
function returnMessageWritable(object, field) {

    field.empty();
    field.prop("readonly", true);
    if (isTheExecutionManual) {
        field.prop("readonly", false);
        field.change(function () {
            object.setReturnMessage(field.val());
        });
    }
}

Action.prototype.generateHeader = function (id) {
    var scope = this;
    var content = $("<div></div>").addClass("content");
    var firstRow = $("<div></div>").addClass("row ");
    var contentField = $("<div></div>").addClass("col-xs-12").attr("id", "contentField");
    var elapsedTime = $("<h4>").attr("style", "font-size:0.9em;margin:0px;line-height:1;height:0.9em;overflow:hidden;white-space: nowrap;text-overflow: ellipsis;");
    var returnMessageField = $("<h4>").attr("style", "font-size:.9em;margin:0px;line-height:1;height:.95em;overflow:hidden;white-space: nowrap;text-overflow: ellipsis;");
    var descriptionField = $("<h4>").attr("style", "font-size:1.2em;margin:0px;line-height:1;height:1.2em;overflow:hidden;white-space: nowrap;text-overflow: ellipsis;");
    returnMessageField.append(safeLinkify(this.returnMessage));
    descriptionField.append(this.description);

    if (this.endlong !== 19700101010000000 && this.endlong !== 0) {
        elapsedTime.append((convToDate(this.endlong) - convToDate(this.startlong)) + " ms");
    } else {
        elapsedTime.append("...");
    }

    /**
     * If returnCode is NE, display button, else display elapsed time
     */
    if (isTheExecutionManual) {

        var buttonFA = $($("<button>").addClass("btn btn statusFA btn-inverse").attr("type", "button").text("FA"));
        var buttonOK = $($("<button>").addClass("btn btn statusOK btn-inverse").attr("type", "button").text("OK"));
        var buttonUpload = $($("<button>").addClass("btn btn-upload btn-info btn-inverse").attr("type", "button").text("UPLOAD"));

        buttonOK.click(function (event) {
            event.preventDefault();
            event.stopPropagation();
            triggerActionExecution(this, id, "OK");
        });
        buttonFA.click(function (event) {
            event.preventDefault();
            event.stopPropagation();
            triggerActionExecution(this, id, "FA");
        });

        buttonUpload.click(function (event) {
            var indexStep = $("#nav-execution").find(".active").data("index");
            var indexAction = $(this).parents("a").data('index')
            var currentActionOrControl = getScriptInformationOfStep()[indexStep]["actionArr"][indexAction]
            var idex = $("#idlabel").text()
            openModalFile(true, currentActionOrControl, "ADD", idex)
            event.preventDefault()
            event.stopPropagation()
        });
        $(buttonUpload).css("float", "right");

        contentField.append($("<div class='col-xs-2'>").addClass("btn-group btn-group-xs").attr("role", "group").append(buttonOK).append(buttonFA));
        contentField.append(buttonUpload);
        //hide save button
        showSaveTestCaseExecutionButton();
    } else {
        contentField.append($("<div class='col-sm-2'>").append(elapsedTime));
    }

    contentField.append($("<div class='col-sm-10'>").append(descriptionField).append(returnMessageField));

    firstRow.append(contentField);

    content.append(firstRow);

    return content;

};

function triggerActionExecution(element, id, status) {
    var currentElement = $($(element).closest(".action")[0]);
    var newReturnCode = "WE";
    if (status === "OK") {
        currentElement.removeClass(function (index, className) {
            return (className.match(/(^|\s)list-group-item\S+/g) || []).join(' ');
        }).addClass("row list-group-item list-group-item-success");
        $(currentElement.find("span")[0]).removeClass(function (index, className) {
            return (className.match(/(^|\s)glyphicon-\S+/g) || []).join(' ');
        }).addClass("glyphicon-ok");
        $(currentElement).next("div").find("input[id='returncode']").val("OK").change();
        newReturnCode = "OK";
    } else if (status === "FA") {
        currentElement.removeClass(function (index, className) {
            return (className.match(/(^|\s)list-group-item-\S+/g) || []).join(' ');
        }).addClass("row list-group-item list-group-item-warning");
        $(currentElement.find("span")[0]).removeClass(function (index, className) {
            return (className.match(/(^|\s)glyphicon-\S+/g) || []).join(' ');
        }).addClass("glyphicon-alert");
        $(currentElement).next("div").find("input[id='returncode']").val("FA").change();
        newReturnCode = "FA";
    }
    $(currentElement).next("div").find("input[id='returncode']").attr("data-modified", "true");
    //$(currentElement).next("div").find("input[id='returnmessage']").val("Action manually executed").change();

    //Modify style of all previous action and control of the current step that have not been modified yet
    var prevElementCurrentStep = $($($(element).closest(".action")[0]).parent().prevAll().find(".list-group-item-black"));
    prevElementCurrentStep.removeClass(function (index, className) {
        return (className.match(/(^|\s)list-group-item-\S+/g) || []).join(' ');
    }).addClass("row list-group-item list-group-item-success");
    //Modify glyphicon of all previous action and control of the current step that have not been modified yet
    $($($($(element).closest(".action")[0]).parent().prevAll().find(".list-group-item")).find(".glyphicon-question-sign")).removeClass(function (index, className) {
        return (className.match(/(^|\s)glyphicon-\S+/g) || []).join(' ');
    }).addClass("glyphicon-ok");
    //Modify Status of all previous action and control of the current step that have not been modified yet
    //$(prevElementCurrentStep).next("div").find("input[id='returncode']:not([data-modified])").val("OK").change();
    //$(prevElementCurrentStep).next("div").find("input[id='returnmessage']").val("Action manually executed").change();

    //Modify style of all previous action and control of the previous steps that have not been modified yet
    var prevElementPreviousStep = $($($(element).closest(".action")[0]).parent().parent().prevAll().find(".list-group-item-black"));
    prevElementPreviousStep.removeClass(function (index, className) {
        return (className.match(/(^|\s)list-group-item-\S+/g) || []).join(' ');
    }).addClass("row list-group-item list-group-item-success");
    //Modify glyphicon of all previous action and control of the previous steps that have not been modified yet
    $($($($(element).closest(".action")[0]).parent().parent().prevAll().find(".list-group-item")).find(".glyphicon-question-sign")).removeClass(function (index, className) {
        return (className.match(/(^|\s)glyphicon-\S+/g) || []).join(' ');
    }).addClass("glyphicon-ok");

    //Modify Status of all previous action and control of the previous step that have not been modified yet
    //$(prevElementPreviousStep).next("div").find("input[id='returncode']:not([data-modified])").val("OK").change();
    //$(prevElementPreviousStep).next("div").find("input[id='returnmessage']").val("Action manually executed").change();
    //update return code
    updateActionControlReturnCode(id, newReturnCode);
}

/*
 * * Update the action focus by idElementTriggers and update the step and the testCase if there was a change in action
 * @param {type} idElementTriggers
 * @param {type} returnCodeElementTrigger
 * @returns {void}
 */
function updateActionControlReturnCode(idElementTriggers, returnCodeElementTrigger) {
    //go though every action or control to update them
    $(".itemContainer").each(function () {

        var idCurrentElement = $(this).data("id");
        var isBeforeTheElementTrigger = false;
        var isTheElementTrigger = (idCurrentElement === idElementTriggers);

        var currentActionControlReturnCode = $(this).data("item").returnCode;
        //if a change in return code is possible on this action or control
        if (returnCodeElementTrigger !== currentActionControlReturnCode) {
            //look if the current action or control is the one trigger
            var isTheElementTrigger = (idCurrentElement === idElementTriggers);
            //look if the current action or control is before the one trigger
            if (!isTheElementTrigger) {
                var idName = ["stepId", "actionId", "controlId"];
                for (var i = 0; i < 3; i++) {
                    if (idCurrentElement[idName[i]] !== idElementTriggers[idName[i]]) {
                        if (idCurrentElement[idName[i]] < idElementTriggers[idName[i]]) {
                            isBeforeTheElementTrigger = true;
                        } else if (idCurrentElement[idName[i]] === -1 && idElementTriggers[idName[i]] !== -1) {
                            isBeforeTheElementTrigger = true;
                        }
                        break;
                    }
                }
            }
            var updateStepNeeded = false;
            //change the returnCode of the one trigger
            if (isTheElementTrigger) {
                $(this).data("item").returnCode = returnCodeElementTrigger;
                updateStepNeeded = true;
            }
            if (isBeforeTheElementTrigger) {
                //change the returnCode if it's untouched
                if (currentActionControlReturnCode === "WE") {
                    //change the return code
                    $(this).data("item").returnCode = "OK";
                    $(this).parent().next("div").find("input[id='returncode']").val("OK").change();
                    updateStepNeeded = true;
                }
            }
            //An action or a control was changed the step need to be updated
            if (updateStepNeeded) {
                //update the element's step triggered
                updateStepExecutionReturnCode(idElementTriggers.stepId, returnCodeElementTrigger, true);
                //then update all the previous step if they are untouched
                for (var idStep = 0; idStep < idElementTriggers.stepId; idStep++) {// update all the step below the element trigger
                    var currentStep = $("#steps").data("listOfStep")[ idStep ];
                    //if previous element are untouch
                    if (currentStep.returnCode === "WE") {
                        updateStepExecutionReturnCode(idStep, "OK", false);
                    }
                }
                updateTestCaseReturnCode();
            }
        }

    });


}

/*
 * * Update the step focus by step id if needed
 * @param {type} stepId
 * @param {type} returnCodeActionControlTrigger
 * @param {type} isStepDisplayed
 * @returns {void}
 */
function updateStepExecutionReturnCode(stepId, returnCodeActionControlTrigger, isStepDisplayed) {

    var newStepReturnCode = null;
    var currentStep = $("#steps").data("listOfStep")[stepId]
    if (returnCodeActionControlTrigger !== currentStep.returnCode) {
        if (returnCodeActionControlTrigger === "KO") {
            newStepReturnCode = "KO";
        } else if (returnCodeActionControlTrigger === "FA" && currentStep.returnCode !== "KO") {
            newStepReturnCode = "FA";
        } else if (returnCodeActionControlTrigger === "OK") {

            var everyActionAndControlOK = true;
            var returnMessageCanBeReset = true;

            $(".itemContainer").each(function () {
                var idCurrentActionControl = $(this).data("id");
                var actionControBelongToCurrentStep = (stepId == idCurrentActionControl.stepId);
                if (actionControBelongToCurrentStep) {

                    if ($(this).data("item").returnCode === "KO") {
                        newStepReturnCode = "KO";
                        everyActionAndControlOK = false;
                        returnMessageCanBeReset = false;
                    } else if (newStepReturnCode !== "KO" && $(this).data("item").returnCode === "FA") {
                        newStepReturnCode = "FA";
                        everyActionAndControlOK = false;
                        returnMessageCanBeReset = false;
                    } else if (newStepReturnCode !== "KO" && newStepReturnCode !== "FA" && $(this).data("item").returnCode === "WE") {
                        everyActionAndControlOK = false;
                    }
                }
            });
            //the last step's action or control was check and no option FA or KO was selected we set the step to OK by default
            if (everyActionAndControlOK) {
                newStepReturnCode = "OK";
                //reset to defaut
            } else if (returnMessageCanBeReset) {
                newStepReturnCode = "WE";
            }
        }
        if (newStepReturnCode !== null) {
            //update step return code
            var stepUpdated = $("#steps").data("listOfStep")[ stepId ];
            stepUpdated.returnCode = newStepReturnCode;

            //update step visual
            var glyphiconColor = "text-black";
            var className = "list-group-item-black";
            var glyphiconName = "glyphicon-question-sign";

            if (newStepReturnCode === "KO") {
                className = "list-group-item-danger";
                glyphiconName = "glyphicon-remove";
                glyphiconColor = "text-danger";
            } else if (newStepReturnCode === "FA") {
                className = "list-group-item-warning";
                glyphiconName = "glyphicon-alert";
                glyphiconColor = "text-warning";
            } else if (newStepReturnCode === "OK") {
                className = "list-group-item-success";
                glyphiconName = "glyphicon-ok";
                glyphiconColor = "text-success";
            }

            $($("#steps").find("a")[stepId]).removeClass(function (index, className) {
                return (className.match(/(^|\s)list-group-item-\S+/g) || []).join(' ');
            }).addClass(className);

            $($("#steps").find("a")[stepId]).find("span").removeClass(function (index, className) {
                return (className.match(/(^|\s)glyphicon-\S+/g) || []).join(' ');
            }).addClass(glyphiconName);

            //if the current step is the one displayed at the center of the screen
            if (isStepDisplayed) {
                var glyphIcon = $($("#stepInfo h2")[0]).removeClass(function (index, className) {
                    return (className.match(/(^|\s)glyphicon-\S+/g) || []).join(' ');
                });

                removeColorClass(glyphIcon);
                glyphIcon.addClass(glyphiconColor);
                glyphIcon.addClass(glyphiconName);
                $("#stepRC").val(newStepReturnCode);
            }
        }
    }
}


function setTestCaseReturnCodeToNA() {
    var testCaseNewReturnCode = "NA";
    var configPanel = $("#testCaseConfig");

    configPanel.find("#controlstatus").text(testCaseNewReturnCode);
//        configPanel.find("#returnMessageEx").text(controlMessage);
    configPanel.find("input#controlstatus2").val(testCaseNewReturnCode);
    updateDataBarVisual(testCaseNewReturnCode);

}
/*
 * * Update the testCase focus if needed
 * @returns {void}
 */
function updateTestCaseReturnCode() {
    var testCaseNewReturnCode = null;
    // go tough every step to see if the testCase need to be update
    for (var idStep = 0; idStep < $("#steps").data("listOfStep").length; idStep++) {
        var currentStep = $("#steps").data("listOfStep")[idStep];
        //a step is not complete no need to go further in the list of step
        if (currentStep.returnCode === "WE") {
            if (testCaseNewReturnCode !== "FA" && testCaseNewReturnCode !== "KO")
                testCaseNewReturnCode = "WE";
            break;//no need to continue
        } else if (currentStep.returnCode === "OK" && testCaseNewReturnCode === null) {
            testCaseNewReturnCode = "OK";
        } else if (currentStep.returnCode === "FA" && testCaseNewReturnCode !== "KO") {
            testCaseNewReturnCode = "FA";
        } else if (currentStep.returnCode === "KO") {
            testCaseNewReturnCode = "KO";
        }
    }

    var configPanel = $("#testCaseConfig");
    if (testCaseNewReturnCode !== null && testCaseNewReturnCode !== configPanel.find("#controlstatus").val()) {

        removeColorClass(configPanel.find("#controlstatus"));
//        removeColorClass(configPanel.find("#exReturnMessage"));
//        var controlMessage = null;

        if (testCaseNewReturnCode === "PE") {
            configPanel.find("#controlstatus").addClass("text-primary");
//            configPanel.find("#exReturnMessage").addClass("text-primary");
//            controlMessage = "";
        } else if (testCaseNewReturnCode === "OK") {
            configPanel.find("#controlstatus").addClass("text-success");
//            configPanel.find("#exReturnMessage").addClass("text-success");
//            controlMessage = "The test case finished successfully."
        } else if (testCaseNewReturnCode === "KO") {
            configPanel.find("#controlstatus").addClass("text-danger");
//            configPanel.find("#exReturnMessage").addClass("text-danger");
//            controlMessage = "The test case failed on validations."
        } else if (testCaseNewReturnCode === "WE") {
            configPanel.find("#controlstatus").addClass("text-black");
//            configPanel.find("#exReturnMessage").addClass("text-black");
//            controlMessage = "The test case has not been executed.";
        } else if (testCaseNewReturnCode === "FA") {
            configPanel.find("#controlstatus").addClass("text-black");
//            configPanel.find("#exReturnMessage").addClass("text-black");
//            controlMessage = "The test case failed to be executed because of an action.";
        }
        configPanel.find("#controlstatus").text(testCaseNewReturnCode);
//        configPanel.find("#returnMessageEx").text(controlMessage);
        configPanel.find("input#controlstatus2").val(testCaseNewReturnCode);
        updateDataBarVisual(testCaseNewReturnCode);
    }
}

Action.prototype.generateContent = function () {
    var obj = this;
    var doc = new Doc();

    var hideOnManual = "";
    var hideOnDoNothing = "";
    var hideCondition = "";
    if (isTheExecutionManual) {
        hideOnManual = " hide";
        if (this.action === "doNothing" || this.action === "Unknown") {
            hideOnDoNothing = " hide";
        }
    } else {
        if (this.conditionOperator === "always") {
            hideCondition = " hide";
        }
    }

    var row1 = $("<div></div>").addClass("row");
    var row2 = $("<div></div>").addClass("row" + hideOnDoNothing);
    var row3 = $("<div></div>").addClass("row" + hideOnDoNothing);
    var row4 = $("<div></div>").addClass("row" + hideOnManual);
    var row5 = $("<div></div>").addClass("row");
    var row6 = $("<div></div>").addClass("row" + hideOnManual + hideCondition);
    var row7 = $("<div></div>").addClass("row" + hideOnManual + hideCondition);
    var container = $("<div id='content-container'></div>").addClass("action-group row list-group-item");
    var actions = $("<input type='text' class='form-control' id='action'>").prop("readonly", true);
    var descField = $("<textarea type='text' rows='1' class='form-control' id='description'>").prop("readonly", true);
    var value1Field = $("<textarea type='text' rows='1' class='form-control' id='value1'>").prop("readonly", true);
    var value1InitField = $("<textarea type='text' rows='1' class='form-control' id='value1init'>").prop("readonly", true);
    var value2Field = $("<textarea type='text' rows='1' class='form-control' id='value2'>").prop("readonly", true);
    var value2InitField = $("<textarea type='text' rows='1' class='form-control' id='value2init'>").prop("readonly", true);
    var value3Field = $("<textarea type='text' rows='1' class='form-control' id='value3'>").prop("readonly", true);
    var value3InitField = $("<textarea type='text' rows='1' class='form-control' id='value3init'>").prop("readonly", true);
    var timeField = $("<input type='text' class='form-control' id='time'>").prop("readonly", true);
    var forceexecField = $("<input type='text' class='form-control' id='forceexec'>").prop("readonly", true);
    var returnCodeField = $("<input type='text' class='form-control' id='returncode'>").prop("readonly", true);

    var returnMessageField = $("<textarea style='width:100%;' class='form-control input-sm' id='returnmessage'>");
    returnMessageWritable(this, returnMessageField);

    var sortField = $("<input type='text' class='form-control' id='sort'>").prop("readonly", true);
    var conditionOperatorField = $("<textarea type='text' rows='1' class='form-control' id='conditionOperator'>").prop("readonly", true);
    var conditionVal1InitField = $("<textarea type='text' rows='1' class='form-control' id='conditionVal1Init'>").prop("readonly", true);
    var conditionVal2InitField = $("<textarea type='text' rows='1' class='form-control' id='conditionVal2Init'>").prop("readonly", true);
    var conditionVal3InitField = $("<textarea type='text' rows='1' class='form-control' id='conditionVal3Init'>").prop("readonly", true);
    var conditionVal1Field = $("<textarea type='text' rows='1' class='form-control' id='conditionVal1'>").prop("readonly", true);
    var conditionVal2Field = $("<textarea type='text' rows='1' class='form-control' id='conditionVal2'>").prop("readonly", true);
    var conditionVal3Field = $("<textarea type='text' rows='1' class='form-control' id='conditionVal3'>").prop("readonly", true);

    var actionGroup = $("<div class='form-group'></div>").append($("<label for='action'>" + doc.getDocLabel("page_executiondetail", "action") + "</label>")).append(actions);
    var descGroup = $("<div class='form-group'></div>").append($("<label for='description'>" + doc.getDocLabel("page_executiondetail", "description") + "</label>")).append(descField);
    var value1Group = $("<div class='form-group'></div>").append($("<label for='value1'>" + doc.getDocLabel("page_executiondetail", "value1") + "</label>")).append(value1Field);
    var value1GroupInit = $("<div class='form-group'></div>").append($("<label for='value1init'>" + doc.getDocLabel("page_executiondetail", "value1init") + "</label>")).append(value1InitField);
    var timeGroup = $("<div class='form-group'></div>").append($("<label for='time'>" + doc.getDocLabel("page_executiondetail", "time") + "</label>")).append(timeField);
    var forceexecGroup = $("<div class='form-group'></div>").append($("<label for='forceexec'>" + doc.getDocLabel("page_executiondetail", "forceexec") + "</label>")).append(forceexecField);
    var value2Group = $("<div class='form-group'></div>").append($("<label for='value2'>" + doc.getDocLabel("page_executiondetail", "value2") + "</label>")).append(value2Field);
    var value2GroupInit = $("<div class='form-group'></div>").append($("<label for='value2init'>" + doc.getDocLabel("page_executiondetail", "value2init") + "</label>")).append(value2InitField);
    var value3Group = $("<div class='form-group'></div>").append($("<label for='value3'>" + doc.getDocLabel("page_executiondetail", "value3") + "</label>")).append(value3Field);
    var value3GroupInit = $("<div class='form-group'></div>").append($("<label for='value3init'>" + doc.getDocLabel("page_executiondetail", "value3init") + "</label>")).append(value3InitField);
    var returncodeGroup = $("<div class='form-group'></div>").append($("<label for='returncode'>" + doc.getDocLabel("page_executiondetail", "return_code") + "</label>")).append(returnCodeField);
    var returnmessageGroup = $("<div class='form-group'></div>").append($("<label for='returnmessage'>" + doc.getDocLabel("page_executiondetail", "return_message") + "</label>")).append(returnMessageField);
    var sortGroup = $("<div class='form-group'></div>").append($("<label for='sort'>" + doc.getDocLabel("page_executiondetail", "sort") + "</label>")).append(sortField);
    var conditionOperatorGroup = $("<div class='form-group'></div>").append($("<label for='conditionOperator'>" + doc.getDocLabel("page_executiondetail", "conditionOperator") + "</label>")).append(conditionOperatorField);
    var conditionVal1InitGroup = $("<div class='form-group'></div>").append($("<label for='conditionVal1Init'>" + doc.getDocLabel("page_executiondetail", "conditionVal1Init") + "</label>")).append(conditionVal1InitField);
    var conditionVal2InitGroup = $("<div class='form-group'></div>").append($("<label for='conditionVal2Init'>" + doc.getDocLabel("page_executiondetail", "conditionVal2Init") + "</label>")).append(conditionVal2InitField);
    var conditionVal3InitGroup = $("<div class='form-group'></div>").append($("<label for='conditionVal3Init'>" + doc.getDocLabel("page_executiondetail", "conditionVal3Init") + "</label>")).append(conditionVal3InitField);
    var conditionVal1Group = $("<div class='form-group'></div>").append($("<label for='conditionVal1'>" + doc.getDocLabel("page_executiondetail", "conditionVal1") + "</label>")).append(conditionVal1Field);
    var conditionVal2Group = $("<div class='form-group'></div>").append($("<label for='conditionVal2'>" + doc.getDocLabel("page_executiondetail", "conditionVal2") + "</label>")).append(conditionVal2Field);
    var conditionVal3Group = $("<div class='form-group'></div>").append($("<label for='conditionVal3'>" + doc.getDocLabel("page_executiondetail", "conditionVal3") + "</label>")).append(conditionVal3Field);

    descField.val(this.description);
    actions.val(this.action);
    value1Field.val(this.value1);
    value1InitField.val(this.value1init);
    value2Field.val(this.value2);
    value2InitField.val(this.value2init);
    value3Field.val(this.value3);
    value3InitField.val(this.value3init);
    if (this.endlong !== 19700101010000000 && this.endlong !== 0) {
        timeField.val((convToDate(this.endlong) - convToDate(this.startlong)) + " ms");
    } else {
        timeField.val("...");
    }
    forceexecField.val(this.forceExeStatus);
    returnCodeField.val(this.returnCode);
    returnMessageField.val(this.returnMessage);
    sortField.val(this.sort);
    conditionOperatorField.val(this.conditionOperator);
    conditionVal1InitField.val(this.conditionVal1Init);
    conditionVal2InitField.val(this.conditionVal2Init);
    conditionVal3InitField.val(this.conditionVal3Init);
    conditionVal1Field.val(this.conditionVal1);
    conditionVal2Field.val(this.conditionVal2);
    conditionVal3Field.val(this.conditionVal3);

    row1.append($("<div></div>").addClass("col-sm-2").append(returncodeGroup));
    row1.append($("<div></div>").addClass("col-sm-10").append(descGroup));
    row2.append($("<div></div>").addClass("col-sm-2"));
    row2.append($("<div></div>").addClass("col-sm-5").append(value1GroupInit));
    if (this.value3 === "") {
        row2.append($("<div></div>").addClass("col-sm-5").append(value2GroupInit));
    } else {
        row2.append($("<div></div>").addClass("col-sm-3").append(value2GroupInit));
        row2.append($("<div></div>").addClass("col-sm-2").append(value3GroupInit));
    }
    row3.append($("<div></div>").addClass("col-sm-2").append(actionGroup));
    row3.append($("<div></div>").addClass("col-sm-5").append(value1Group));
    if (this.value3 === "") {
        row3.append($("<div></div>").addClass("col-sm-5").append(value2Group));
    } else {
        row3.append($("<div></div>").addClass("col-sm-3").append(value2Group));
        row3.append($("<div></div>").addClass("col-sm-2").append(value3Group));
    }
    row4.append($("<div></div>").addClass("col-sm-2").append(sortGroup));
    row4.append($("<div></div>").addClass("col-sm-5").append(forceexecGroup));
    row4.append($("<div></div>").addClass("col-sm-5").append(timeGroup));
    row5.append($("<div></div>").addClass("col-sm-12").append(returnmessageGroup));
    row6.append($("<div></div>").addClass("col-sm-2"));
    row6.append($("<div></div>").addClass("col-sm-4").append(conditionVal1InitGroup));
    row6.append($("<div></div>").addClass("col-sm-4").append(conditionVal2InitGroup));
    row6.append($("<div></div>").addClass("col-sm-2").append(conditionVal3InitGroup));
    row7.append($("<div></div>").addClass("col-sm-2").append(conditionOperatorGroup));
    row7.append($("<div></div>").addClass("col-sm-4").append(conditionVal1Group));
    row7.append($("<div></div>").addClass("col-sm-4").append(conditionVal2Group));
    row7.append($("<div></div>").addClass("col-sm-2").append(conditionVal3Group));

    container.append(row1);
    container.append(row2);
    container.append(row3);
    container.append(row4);
    container.append(row5);
    container.append(row6);
    container.append(row7);

    return container;
};


Action.prototype.getJsonData = function () {
    var json = {};

    json.action = this.action;
    json.conditionOperator = this.conditionOperator;
    json.conditionVal1 = this.conditionVal1;
    json.conditionVal1Init = this.conditionVal1Init;
    json.conditionVal2 = this.conditionVal2;
    json.conditionVal2Init = this.conditionVal2Init;
    json.conditionVal3 = this.conditionVal3;
    json.conditionVal3Init = this.conditionVal3Init;
    json.description = this.description;
    json.end = this.end;
    json.endlong = this.endlong;
    json.forceExeStatus = this.forceExeStatus;
    json.id = this.id;
    json.index = this.index;
    json.returnCode = this.returnCode;
    json.returnMessage = this.returnMessage;
    json.screenshotFileName = this.screenshotFileName;
    json.sequence = this.sequence;
    json.sort = this.sort;
    json.start = this.start;
    json.step = this.step;
    json.test = this.test;
    json.testcase = this.testcase;
    json.toDelete = this.toDelete;
    json.value1 = this.value1;
    json.value1init = this.value1init;
    json.value2 = this.value2;
    json.value2init = this.value2init;
    json.value3 = this.value3;
    json.value3init = this.value3init;
    //Value the user is able to modified
    return json;
};

function Control(json, parentAction) {
    if (json !== null) {
        this.control = json.control;
        this.controlType = json.controlType;
        this.controlSequence = json.controlSequence;
        this.value1 = json.controlProperty;
        this.value2 = json.controlValue;
        this.value3 = json.controlValue3;
        this.value1init = json.controlPropertyInit;
        this.value2init = json.controlValueInit;
        this.value3init = json.controlValue3Init;
        this.description = json.description;
        this.end = json.end;
        this.endlong = json.endlong;
        this.fatal = json.fatal;
        this.id = json.id;
        this.returnCode = json.returnCode;
        this.returnMessage = json.returnMessage;
        this.screenshotFileName = "";
        this.sequence = json.sequence;
        this.sort = json.sort;
        this.start = json.start;
        this.startlong = json.startlong;
        this.step = json.step;
        this.index = json.index;
        this.test = json.test;
        this.testcase = json.testcase;
        this.fileList = json.fileList;
        this.conditionOperator = json.conditionOperator;
        this.conditionVal1Init = json.conditionVal1Init;
        this.conditionVal2Init = json.conditionVal2Init;
        this.conditionVal3Init = json.conditionVal3Init;
        this.conditionVal1 = json.conditionVal1;
        this.conditionVal2 = json.conditionVal2;
        this.conditionVal3 = json.conditionVal3;
    } else {
        this.control = "";
        this.controlType = "Unknown";
        this.controlSequence = 1;
        this.value1 = "";
        this.value2 = "";
        this.value3 = "";
        this.value1init = "";
        this.value2init = "";
        this.value3init = "";
        this.description = "";
        this.end = 0;
        this.endlong = 0;
        this.fatal = "Y";
        this.id = 0;
        this.returnCode = "";
        this.returnMessage = "";
        this.screenshotFileName = "";
        this.sequence = parentAction.sequence;
        this.index = parentAction.index;
        this.sort = 0;
        this.start = 0;
        this.startlong = 0;
        this.step = parentAction.step;
        this.test = "";
        this.testcase = "";
        this.fileList = [];
        this.conditionOperator = "always";
        this.conditionVal1Init = "";
        this.conditionVal2Init = "";
        this.conditionVal3Init = "";
        this.conditionVal1 = "";
        this.conditionVal2 = "";
        this.conditionVal3 = "";
    }

    this.parentStep = parentAction.parentStep;
    this.parentAction = parentAction;

    this.toDelete = false;

    this.html = $("<a href='#'></a>").addClass("action-group control").css("margin-left", "0px");
    $(this.html).data("index", this.sort - 1)
}

Control.prototype.draw = function (idMotherStep, idMotherAction, idControl) {
    var htmlElement = this.html;
    var row = $("<div class='itemContainer'></div>").addClass("col-xs-10");
    var type = $("<div></div>").addClass("type");
    var currentControlId = {stepId: idMotherStep, actionId: idMotherAction, controlId: idControl};

    var header = this.generateHeader(currentControlId);
    row.append(header);
    row.data("item", this);
    row.data("id", currentControlId);

    var button = $("<div></div>").addClass("col-xs-1").append($("<span class='glyphicon glyphicon-chevron-down'></span>").attr("style", "font-size:1.5em"));

    htmlElement.prepend(button);
    htmlElement.prepend(row);

    var content = this.generateContent();
    if (this.returnCode === "OK") {
        htmlElement.prepend($("<div>").addClass("col-xs-1").append($("<span>").addClass("glyphicon glyphicon-ok").attr("style", "font-size:1.5em")));
        htmlElement.addClass("row list-group-item list-group-item-success");
        content.hide();
    } else if (this.returnCode === "PE") {
        htmlElement.prepend($("<div>").addClass("col-xs-1").append($("<span>").addClass("glyphicon glyphicon-refresh spin").attr("style", "font-size:1.5em")));
        htmlElement.addClass("row list-group-item list-group-item-info");
        content.hide();
    } else if (this.returnCode === "KO") {
        htmlElement.prepend($("<div>").addClass("col-xs-1").append($("<span>").addClass("glyphicon glyphicon-remove").attr("style", "font-size:1.5em")));
        htmlElement.addClass("row list-group-item list-group-item-danger");
        content.hide();
    } else if (this.returnCode === "NA") {
        htmlElement.prepend($("<div>").addClass("col-xs-1").append($("<span>").addClass("glyphicon glyphicon-alert").attr("style", "font-size:1.5em")));
        htmlElement.addClass("row list-group-item list-group-item-info");
        content.hide();
    } else if (this.returnCode === "NE") {
        htmlElement.prepend($("<div>").addClass("col-xs-1").append($("<span>").attr("style", "font-size:1.5em")));
        htmlElement.addClass("row list-group-item list-group-item-grey");
        content.hide();
    } else if (this.returnCode === "WE" && isTheExecutionManual) {
        htmlElement.prepend($("<div>").addClass("marginLeft-15 col-xs-1").append($("<span>").addClass("glyphicon glyphicon-question-sign").attr("style", "font-size:1.5em")));
        htmlElement.addClass("row list-group-item list-group-item-black");
        content.hide();
    } else {
        htmlElement.prepend($("<div>").addClass("col-xs-1").append($("<span>").addClass("glyphicon glyphicon-alert").attr("style", "font-size:1.5em")));
        htmlElement.addClass("row list-group-item list-group-item-warning");
        content.hide();
    }
    // Starting to reduce the size of the row by the length of elements.
    $(header).find("#contentField").removeClass("col-xs-12").addClass("col-xs-" + (12 - this.fileList.length * 2)).addClass("col-sm-" + (12 - this.fileList.length * 2));
    // Adding all media attached to control execution.
    addFileLink(this.fileList, $(header).find(".row"), isTheExecutionManual, idMotherStep);

    $(this.parentAction.html).parent().append(htmlElement);
    $(this.parentAction.html).parent().append(content);
    htmlElement.click(function () {
        if ($(this).find(".glyphicon-chevron-down").length > 0) {
            $(this).find(".glyphicon-chevron-down").removeClass("glyphicon-chevron-down").addClass("glyphicon-chevron-up");
        } else {
            $(this).find(".glyphicon-chevron-up").removeClass("glyphicon-chevron-up").addClass("glyphicon-chevron-down");
        }
        content.toggle();
        return false;
    });

};

Control.prototype.setStep = function (step) {
    this.step = step;
};

Control.prototype.setSequence = function (sequence) {
    this.sequence = sequence;
};

Control.prototype.setControl = function (control) {
    this.control = control;
};

Control.prototype.setReturnMessage = function (returnMessage) {
    this.returnMessage = returnMessage;
};

Control.prototype.generateHeader = function (id) {
    var scope = this;
    var content = $("<div></div>").addClass("content");
    var firstRow = $("<div></div>").addClass("row ");
    var contentField = $("<div></div>").addClass("col-xs-12").attr("id", "contentField");
    var elapsedTime = $("<h4>").attr("style", "font-size:0.9em;margin:0px;line-height:1;height:0.9em;overflow:hidden;white-space: nowrap;text-overflow: ellipsis;");
    var returnMessageField = $("<h4>").attr("style", "font-size:.9em;margin:0px;line-height:1;height:.95em;overflow:hidden;white-space: nowrap;text-overflow: ellipsis;");
    var descriptionField = $("<h4>").attr("style", "font-size:1.2em;margin:0px;line-height:1;height:1.2em;overflow:hidden;white-space: nowrap;text-overflow: ellipsis;");

    returnMessageField.append(safeLinkify(this.returnMessage));
    descriptionField.append(this.description);

    if (this.endlong !== 19700101010000000 && this.endlong !== 0) {
        elapsedTime.append((convToDate(this.endlong) - convToDate(this.startlong)) + " ms");
    } else {
        elapsedTime.append("...");
    }

    if (isTheExecutionManual) {
        var buttonFA = $($("<button>").addClass("btn btn-danger btn-inverse").attr("type", "button").text("KO"));
        var buttonOK = $($("<button>").addClass("btn btn-success btn-inverse").attr("type", "button").text("OK"));
        var buttonUpload = $($("<button>").addClass("btn btn-info btn-inverse").attr("type", "button").text("UPLOAD"));
        $(buttonUpload).css("float", "right")
        buttonOK.click(function (event) {
            event.preventDefault();
            event.stopPropagation();
            triggerControlExecution(this, id, "OK");
        });
        buttonFA.click(function (event) {
            event.preventDefault();
            event.stopPropagation();
            triggerControlExecution(this, id, "KO");
        });
        $(buttonUpload).click(function (event) {
            var indexStep = $("#nav-execution").find(".active").data("index");
            var indexAction = $(this).parents("a").parent().find(".action").data('index')
            var indexControl = $(this).parents("a").data('index')
            var currentActionOrControl = getScriptInformationOfStep()[indexStep]["actionArr"][indexAction]["controlArr"][indexControl]
            var idex = $("#idlabel").text()
            openModalFile(false, currentActionOrControl, "ADD", idex)
            event.preventDefault()
            event.stopPropagation()
        })
        contentField.append($("<div class='col-xs-2'>").addClass("btn-group btn-group-xs").attr("role", "group").append(buttonOK).append(buttonFA));
        contentField.append(buttonUpload);
        showSaveTestCaseExecutionButton();
    } else {
        contentField.append($("<div class='col-xs-2'>").append(elapsedTime));

    }

    contentField.append($("<div class='col-xs-10'>").append(descriptionField).append(returnMessageField));

    firstRow.append(contentField);

    content.append(firstRow);

    return content;
};

function triggerControlExecution(element, id, status) {
    var currentElement = $($(element).closest(".control")[0]);
    var newReturnCode = "NE";
    if (status === "OK") {
        currentElement.removeClass(function (index, className) {
            return (className.match(/(^|\s)list-group-item-\S+/g) || []).join(' ');
        }).addClass("row list-group-item list-group-item-success");
        $(currentElement.find("span")[0]).removeClass(function (index, className) {
            return (className.match(/(^|\s)glyphicon-\S+/g) || []).join(' ');
        }).addClass("glyphicon-ok");
        //Modify Status of current action
        $(currentElement).next("div").find("input[id='returncode']").val("OK").change();
        $(currentElement).next("div").find("input[id='returncode']").attr("data-modified", "true");
        $(currentElement).next("div").find("input[id='returnmessage']").val("Action manually executed").change();
        newReturnCode = "OK";
    } else {
        currentElement.removeClass(function (index, className) {
            return (className.match(/(^|\s)list-group-item-\S+/g) || []).join(' ');
        }).addClass("row list-group-item list-group-item-danger");
        $(currentElement.find("span")[0]).removeClass(function (index, className) {
            return (className.match(/(^|\s)glyphicon-\S+/g) || []).join(' ');
        }).addClass("glyphicon-remove");
        //Modify Status of current action
        $(currentElement).next("div").find("input[id='returncode']").val("KO").change();
        $(currentElement).next("div").find("input[id='returncode']").attr("data-modified", "true");
        $(currentElement).next("div").find("input[id='returnmessage']").val("Action manually executed").change();
        newReturnCode = "KO";
    }

    //Modify style of action of the current actiongroup that have not been modified yet
    var prevElementCurrentActionGroup = $($($(element).closest(".control")[0]).prevAll(".list-group-item-black"));
    prevElementCurrentActionGroup.removeClass(function (index, className) {
        return (className.match(/(^|\s)list-group-item-\S+/g) || []).join(' ');
    }).addClass("row list-group-item list-group-item-success");
    //Modify glyphicon of action of the current actiongroup that have not been modified yet
    $($($($(element).closest(".control")[0]).prevAll(".list-group-item")).find(".glyphicon-question-sign")).removeClass(function (index, className) {
        return (className.match(/(^|\s)glyphicon-\S+/g) || []).join(' ');
    }).addClass("glyphicon-ok");
    //Modify Status of action of the current actiongroup that have not been modified yet
    $(prevElementCurrentStep).next("div").find("input[id='returncode']:not([data-modified])").val("OK").change();
    $(prevElementCurrentStep).next("div").find("input[id='returnmessage']").val("Action manually executed").change();

    //Modify style of all previous action and control of the current step that have not been modified yet
    var prevElementCurrentStep = $($($(element).closest(".control")[0]).parent().prevAll().find(".list-group-item-black"));
    prevElementCurrentStep.removeClass(function (index, className) {
        return (className.match(/(^|\s)list-group-item-\S+/g) || []).join(' ');
    }).addClass("row list-group-item list-group-item-success");
    //Modify glyphicon of all previous action and control of the current step that have not been modified yet
    $($($($(element).closest(".control")[0]).parent().prevAll().find(".list-group-item")).find(".glyphicon-question-sign")).removeClass(function (index, className) {
        return (className.match(/(^|\s)glyphicon-\S+/g) || []).join(' ');
    }).addClass("glyphicon-ok");
    //Modify Status of all previous action and control of the current step that have not been modified yet
    $(prevElementCurrentStep).next("div").find("input[id='returncode']:not([data-modified])").val("OK").change();
    $(prevElementCurrentStep).next("div").find("input[id='returnmessage']").val("Action manually executed").change();


    //Modify style of all previous action and control of the previous steps that have not been modified yet
    var prevElementPreviousStep = $($($(element).closest(".control")[0]).parent().parent().prevAll().find(".list-group-item-black"));
    prevElementPreviousStep.removeClass(function (index, className) {
        return (className.match(/(^|\s)list-group-item-\S+/g) || []).join(' ');
    }).addClass("row list-group-item list-group-item-success");
    //Modify glyphicon of all previous action and control of the previous steps that have not been modified yet
    $($($($(element).closest(".control")[0]).parent().parent().prevAll().find(".list-group-item")).find(".glyphicon-question-sign")).removeClass(function (index, className) {
        return (className.match(/(^|\s)glyphicon-\S+/g) || []).join(' ');
    }).addClass("glyphicon-ok");
    //Modify Status of all previous action and control of the previous step that have not been modified yet
    $(prevElementPreviousStep).next("div").find("input[id='returncode']:not([data-modified])").val("OK").change();
    $(prevElementPreviousStep).next("div").find("input[id='returnmessage']").val("Action manually executed").change();
    //update return code
    updateActionControlReturnCode(id, newReturnCode);

}

Control.prototype.generateContent = function () {
    var doc = new Doc();
    var obj = this;

    var hideOnManual = "";
    var hideOnDoNothing = "";
    var hideCondition = "";
    if (isTheExecutionManual) {
        hideOnManual = " hide";
        if (this.controlType === "Unknown") {
            hideOnDoNothing = " hide";
        }
    } else {
        if (this.conditionOperator === "always") {
            hideCondition = " hide";
        }
    }

    var row1 = $("<div></div>").addClass("row");
    var row2 = $("<div></div>").addClass("row" + hideOnDoNothing);
    var row3 = $("<div></div>").addClass("row" + hideOnDoNothing);
    var row4 = $("<div></div>").addClass("row" + hideOnManual);
    var row5 = $("<div></div>").addClass("row");
    var row6 = $("<div></div>").addClass("row" + hideOnManual + hideCondition);
    var row7 = $("<div></div>").addClass("row" + hideOnManual + hideCondition);
    var container = $("<div id='content-container'></div>").addClass("action-group row list-group-item").css("margin-left", "25px");

    var descField = $("<textarea type='text' rows='1' class='form-control' id='description'>").prop("readonly", true);
    var returnCodeField = $("<input type='text' class='form-control' id='returncode'>").prop("readonly", true);
    var controlTypeField = $("<input type='text' class='form-control' id='controltype'>").prop("readonly", true);
    var value1Field = $("<textarea type='text' rows='1' class='form-control' id='value1'>").prop("readonly", true);
    var value1InitField = $("<textarea type='text' rows='1' class='form-control' id='value1init'>").prop("readonly", true);
    var value2Field = $("<textarea type='text' rows='1' class='form-control' id='value2'>").prop("readonly", true);
    var value2InitField = $("<textarea type='text' rows='1' class='form-control' id='value2init'>").prop("readonly", true);
    var value3Field = $("<textarea type='text' rows='1' class='form-control' id='value3'>").prop("readonly", true);
    var value3InitField = $("<textarea type='text' rows='1' class='form-control' id='value3init'>").prop("readonly", true);
    var timeField = $("<input type='text' class='form-control' id='time'>").prop("readonly", true);

    var returnMessageField = $("<textarea style='width:100%;' class='form-control' id='returnmessage'>");
    returnMessageWritable(this, returnMessageField);

    var fatalField = $("<input type='text' class='form-control' id='fatal'>").prop("readonly", true);
    var sortField = $("<input type='text' class='form-control' id='sort'>").prop("readonly", true);
    var conditionOperatorField = $("<textarea type='text' rows='1' class='form-control' id='conditionOperator'>").prop("readonly", true);
    var conditionVal1InitField = $("<textarea type='text' rows='1' class='form-control' id='conditionVal1Init'>").prop("readonly", true);
    var conditionVal2InitField = $("<textarea type='text' rows='1' class='form-control' id='conditionVal2Init'>").prop("readonly", true);
    var conditionVal3InitField = $("<textarea type='text' rows='1' class='form-control' id='conditionVal3Init'>").prop("readonly", true);
    var conditionVal1Field = $("<textarea type='text' rows='1' class='form-control' id='conditionVal1'>").prop("readonly", true);
    var conditionVal2Field = $("<textarea type='text' rows='1' class='form-control' id='conditionVal2'>").prop("readonly", true);
    var conditionVal3Field = $("<textarea type='text' rows='1' class='form-control' id='conditionVal3'>").prop("readonly", true);

    var descGroup = $("<div class='form-group'></div>").append($("<label for='description'>" + doc.getDocLabel("page_executiondetail", "description") + "</label>")).append(descField);
    var returncodeGroup = $("<div class='form-group'></div>").append($("<label for='returncode'>" + doc.getDocLabel("page_executiondetail", "return_code") + "</label>")).append(returnCodeField);
    var returnmessageGroup = $("<div class='form-group'></div>").append($("<label for='returnmessage'>" + doc.getDocLabel("page_executiondetail", "return_message") + "</label>")).append(returnMessageField);
    var controlTypeGroup = $("<div class='form-group'></div>").append($("<label for='controltype'>" + doc.getDocLabel("page_executiondetail", "control_type") + "</label>")).append(controlTypeField);
    var controlValue1Group = $("<div class='form-group'></div>").append($("<label for='controlvalue'>" + doc.getDocLabel("page_executiondetail", "value1") + "</label>")).append(value1Field);
    var controlValue1InitGroup = $("<div class='form-group'></div>").append($("<label for='controlvalueinit'>" + doc.getDocLabel("page_executiondetail", "value1init") + "</label>")).append(value1InitField);
    var timeGroup = $("<div class='form-group'></div>").append($("<label for='time'>" + doc.getDocLabel("page_executiondetail", "time") + "</label>")).append(timeField);
    var controlValue2Group = $("<div class='form-group'></div>").append($("<label for='controlproperty'>" + doc.getDocLabel("page_executiondetail", "value2") + "</label>")).append(value2Field);
    var controlValue2InitGroup = $("<div class='form-group'></div>").append($("<label for='controlpropertyinit'>" + doc.getDocLabel("page_executiondetail", "value2init") + "</label>")).append(value2InitField);
    var controlValue3Group = $("<div class='form-group'></div>").append($("<label for='controlvalue3'>" + doc.getDocLabel("page_executiondetail", "value3") + "</label>")).append(value3Field);
    var controlValue3InitGroup = $("<div class='form-group'></div>").append($("<label for='controlvalue3init'>" + doc.getDocLabel("page_executiondetail", "value3init") + "</label>")).append(value3InitField);
    var fatalGroup = $("<div class='form-group'></div>").append($("<label for='fatal'>" + doc.getDocLabel("page_executiondetail", "fatal") + "</label>")).append(fatalField);
    var sortGroup = $("<div class='form-group'></div>").append($("<label for='sort'>" + doc.getDocLabel("page_executiondetail", "sort") + "</label>")).append(sortField);
    var conditionOperatorGroup = $("<div class='form-group'></div>").append($("<label for='conditionOperator'>" + doc.getDocLabel("page_executiondetail", "conditionOperator") + "</label>")).append(conditionOperatorField);
    var conditionVal1InitGroup = $("<div class='form-group'></div>").append($("<label for='conditionVal1Init'>" + doc.getDocLabel("page_executiondetail", "conditionVal1Init") + "</label>")).append(conditionVal1InitField);
    var conditionVal2InitGroup = $("<div class='form-group'></div>").append($("<label for='conditionVal2Init'>" + doc.getDocLabel("page_executiondetail", "conditionVal2Init") + "</label>")).append(conditionVal2InitField);
    var conditionVal3InitGroup = $("<div class='form-group'></div>").append($("<label for='conditionVal3Init'>" + doc.getDocLabel("page_executiondetail", "conditionVal3Init") + "</label>")).append(conditionVal3InitField);
    var conditionVal1Group = $("<div class='form-group'></div>").append($("<label for='conditionVal1'>" + doc.getDocLabel("page_executiondetail", "conditionVal1") + "</label>")).append(conditionVal1Field);
    var conditionVal2Group = $("<div class='form-group'></div>").append($("<label for='conditionVal2'>" + doc.getDocLabel("page_executiondetail", "conditionVal2") + "</label>")).append(conditionVal2Field);
    var conditionVal3Group = $("<div class='form-group'></div>").append($("<label for='conditionVal3'>" + doc.getDocLabel("page_executiondetail", "conditionVal3") + "</label>")).append(conditionVal3Field);


    descField.val(this.description);
    returnCodeField.val(this.returnCode);
    returnMessageField.val(this.returnMessage);
    controlTypeField.val(this.controlType);
    if (this.endlong !== 19700101010000000 && this.endlong !== 0) {
        timeField.val((convToDate(this.endlong) - convToDate(this.startlong)) + " ms");
    } else {
        timeField.val("...");
    }
    value1Field.val(this.value1);
    value1InitField.val(this.value1init);
    value2Field.val(this.value2);
    value2InitField.val(this.value2init);
    value3Field.val(this.value3);
    value3InitField.val(this.value3init);
    fatalField.val(this.fatal);
    sortField.val(this.sort);
    conditionOperatorField.val(this.conditionOperator);
    conditionVal1InitField.val(this.conditionVal1Init);
    conditionVal2InitField.val(this.conditionVal2Init);
    conditionVal3InitField.val(this.conditionVal3Init);
    conditionVal1Field.val(this.conditionVal1);
    conditionVal2Field.val(this.conditionVal2);
    conditionVal3Field.val(this.conditionVal3);

    row1.append($("<div></div>").addClass("col-sm-2").append(returncodeGroup));
    row1.append($("<div></div>").addClass("col-sm-10").append(descGroup));
    row2.append($("<div></div>").addClass("col-sm-2"));
    row2.append($("<div></div>").addClass("col-sm-4").append(controlValue1InitGroup));
    row2.append($("<div></div>").addClass("col-sm-4").append(controlValue2InitGroup));
    row2.append($("<div></div>").addClass("col-sm-2").append(controlValue3InitGroup));
    row3.append($("<div></div>").addClass("col-sm-2").append(controlTypeGroup));
    row3.append($("<div></div>").addClass("col-sm-4").append(controlValue1Group));
    row3.append($("<div></div>").addClass("col-sm-4").append(controlValue2Group));
    row3.append($("<div></div>").addClass("col-sm-2").append(controlValue3Group));
    row4.append($("<div></div>").addClass("col-sm-2").append(sortGroup));
    row4.append($("<div></div>").addClass("col-sm-5").append(fatalGroup));
    row4.append($("<div></div>").addClass("col-sm-5").append(timeGroup));
    row5.append($("<div></div>").addClass("col-sm-12").append(returnmessageGroup));
    row6.append($("<div></div>").addClass("col-sm-2"));
    row6.append($("<div></div>").addClass("col-sm-4").append(conditionVal1InitGroup));
    row6.append($("<div></div>").addClass("col-sm-4").append(conditionVal2InitGroup));
    row6.append($("<div></div>").addClass("col-sm-2").append(conditionVal3InitGroup));
    row7.append($("<div></div>").addClass("col-sm-2").append(conditionOperatorGroup));
    row7.append($("<div></div>").addClass("col-sm-4").append(conditionVal1Group));
    row7.append($("<div></div>").addClass("col-sm-4").append(conditionVal2Group));
    row7.append($("<div></div>").addClass("col-sm-2").append(conditionVal3Group));

    container.append(row1);
    container.append(row2);
    container.append(row3);
    container.append(row4);
    container.append(row5);
    container.append(row6);
    container.append(row7);

    return container;
};

Control.prototype.getJsonData = function () {
    var json = {};

    json.conditionOperator = this.conditionOperator;
    json.conditionVal1 = this.conditionVal1;
    json.conditionVal1Init = this.conditionVal1Init;
    json.conditionVal2 = this.conditionVal2;
    json.conditionVal2Init = this.conditionVal2Init;
    json.conditionVal3 = this.conditionVal3;
    json.conditionVal3Init = this.conditionVal3Init;
    json.control = this.control;
    json.controlType = this.controlType;
    json.controlSequence = this.controlSequence;
    json.description = this.description;
    json.end = this.end;
    json.endlong = this.endlong;
    json.fatal = this.fatal;
    json.id = this.id;
    json.index = this.index;
    json.returnCode = this.returnCode;
    json.screenshotFileName = this.screenshotFileName;
    json.sequence = this.sequence;
    json.sort = this.sort;
    json.start = this.start;
    json.startlong = this.startlong;
    json.step = this.step;
    json.test = this.test;
    json.testcase = this.testcase;
    json.toDelete = this.toDelete;
    json.value1 = this.value1;
    json.value1init = this.value1init;
    json.value2 = this.value2;
    json.value2init = this.value2init;
    json.value3 = this.value3;
    json.value3init = this.value3init;
    json.type = this.type;
    json.controlProperty = this.value1;
    json.controlValue = this.value2;
    json.controlValue3 = this.value3;
    //Value the user is able to modified
    json.returnMessage = this.returnMessage;

    return json;
};

function changeClickIfManual(isTheExecutionManual, container, idStep, file, event) {
    if (isTheExecutionManual) {
        var idex = $("#idlabel").text()
        if ($(container).parent().parent().parent().hasClass("action")) {
            var indexAction = $(this).parents("a").data('index')
            var currentActionOrControl = getScriptInformationOfStep()[idStep]["actionArr"][indexAction]
            openModalFile(true, currentActionOrControl, "EDIT", idex, file, !isTheExecutionManual)
        } else {
            var indexAction = $(this).parents("a").parent().find(".action").data('index')
            var indexControl = $(this).parents("a").data('index')
            var currentActionOrControl = getScriptInformationOfStep()[idStep]["actionArr"][indexAction]["controlArr"][indexControl]
            openModalFile(false, currentActionOrControl, "EDIT", idex, file, !isTheExecutionManual)
        }
        event.preventDefault()
        event.stopPropagation()
    } else {
        openModalFile(null, null, "EDIT", null, file, !isTheExecutionManual)
    }
}


// Function in order to add the Media files links into TestCase, step, action and control level.
function addFileLink(fileList, container, manual, idStep) {
    var auto = manual == true ? false : true;
    $(container).find($("div[name='mediaMiniature']")).remove();
    for (var i = 0; i < fileList.length; i++) {
        let index = i
        if ((fileList[i].fileType === "JPG") || (fileList[i].fileType === "PNG")) {
            var urlImage = "ReadTestCaseExecutionMedia?filename=" + fileList[i].fileName + "&filetype=" + fileList[i].fileType + "&filedesc=" + fileList[i].fileDesc + "&auto=" + auto;
            var fileDesc = fileList[i].fileDesc;
            var linkBox = $("<div name='mediaMiniature'>").addClass("col-xs-3 col-sm-2").css("padding", "0px 7px 0px 7px")
                    .append(fileList[i].fileDesc).append($("<img>").attr("src", urlImage + "&h=30&w=60").css("max-height", "30px").css("max-width", "60px")
                    .click(function (e) {
                        changeClickIfManual(isTheExecutionManual, container, idStep, fileList[index], e)
                        return false;
                    }));
            container.append(linkBox);



        } else if ((fileList[i].fileType === "HTML") || (fileList[i].fileType === "JSON") || (fileList[i].fileType === "TXT") || (fileList[i].fileType === "XML")) {

            var j = i;
            var urlImagetxt = "ReadTestCaseExecutionMedia?filename=" + fileList[i].fileName + "&filetype=" + fileList[i].fileType + "&filedesc=" + fileList[i].fileDesc + "&auto=" + auto;
            ;
            var fileDesctxt = fileList[i].fileDesc;
            var filetypetxt = fileList[i].fileType.toLowerCase();
            if (i === 0) {
                var linkBoxtxt = $("<div name='mediaMiniature'>").addClass("col-xs-3 col-sm-2").css("padding", "0px 7px 0px 7px")
                        .append(fileList[i].fileDesc).prepend("<br>").prepend($("<img>").attr("src", "images/f-" + filetypetxt + ".svg")
                        .css("height", "30px").click(function (f) {
                    changeClickIfManual(isTheExecutionManual, container, idStep, fileList[index], f)
                    return false;
                }));
            } else if (i === 1) {
                var linkBoxtxt = $("<div name='mediaMiniature'>").addClass("col-xs-3 col-sm-2").css("padding", "0px 7px 0px 7px")
                        .append(fileList[i].fileDesc).prepend("<br>").prepend($("<img>").attr("src", "images/f-" + filetypetxt + ".svg")
                        .css("height", "30px").click(function (f) {
                    changeClickIfManual(isTheExecutionManual, container, idStep, fileList[index], f)
                    return false;
                }));
            } else if (i === 2) {
                var linkBoxtxt = $("<div name='mediaMiniature'>").addClass("col-xs-3 col-sm-2").css("padding", "0px 7px 0px 7px")
                        .append(fileList[i].fileDesc).prepend("<br>").prepend($("<img>").attr("src", "images/f-" + filetypetxt + ".svg")
                        .css("height", "30px").click(function (f) {
                    changeClickIfManual(isTheExecutionManual, container, idStep, fileList[index], f)
                    return false;
                }));
            } else if (i === 3) {
                var linkBoxtxt = $("<div name='mediaMiniature'>").addClass("col-xs-3 col-sm-2").css("padding", "0px 7px 0px 7px")
                        .append(fileList[i].fileDesc).prepend("<br>").prepend($("<img>").attr("src", "images/f-" + filetypetxt + ".svg")
                        .css("height", "30px").click(function (f) {
                    changeClickIfManual(isTheExecutionManual, container, idStep, fileList[index], f)
                    return false;
                }));
            }
            container.append(linkBoxtxt);
        } else if ((fileList[i].fileType === "BIN") || (fileList[i].fileType === "PDF")) {

            var linkBoxtxt = null;

            if (fileList[i].fileType === "BIN") {
                linkBoxtxt = $("<div name='mediaMiniature'>").addClass("col-xs-3 col-sm-2").css("padding", "0px 7px 0px 7px").append(fileList[i].fileDesc).prepend("<br>").prepend($("<img>").attr("src", "images/f-binaire.png").css("height", "30px").click(function (f) {
                    changeClickIfManual(isTheExecutionManual, container, idStep, fileList[index], f)
                    return false;
                }))
            } else if (fileList[i].fileType === "PDF") {
                linkBoxtxt = $("<div name='mediaMiniature'>").addClass("col-xs-3 col-sm-2").css("padding", "0px 7px 0px 7px").append(fileList[i].fileDesc).prepend("<br>").prepend($("<img>").attr("src", "images/f-pdf.svg").css("height", "30px").click(function (f) {
                    changeClickIfManual(isTheExecutionManual, container, idStep, fileList[index], f)
                    return false;
                }))
            }

            container.append(linkBoxtxt);
        }
    }



    if (isTheExecutionManual && fileList.length !== 0) {
        var buttonUpload = $($("<button>").addClass("btn btn-info btn-upload btn-inverse").attr("type", "button").text("UPLOAD"));
        $(buttonUpload).css("float", "right")
        buttonUpload.click(function (event) {
            var idex = $("#idlabel").text()
            if ($(container).parent().parent().parent().hasClass("action")) {
                var indexAction = $(this).parents("a").data('index')
                var currentActionOrControl = getScriptInformationOfStep()[idStep]["actionArr"][indexAction]
                openModalFile(true, currentActionOrControl, "ADD", idex)
            } else {
                var indexAction = $(this).parents("a").parent().find(".action").data('index')
                var indexControl = $(this).parents("a").data('index')
                var currentActionOrControl = getScriptInformationOfStep()[idStep]["actionArr"][indexAction]["controlArr"][indexControl]
                openModalFile(false, currentActionOrControl, "ADD", idex)
            }
            event.preventDefault()
            event.stopPropagation()
        })
        $(container).parent().find("#contentField").append(buttonUpload)
        $(container).parent().find("#contentField").find(".col-sm-10").removeClass("col-sm-10").addClass("col-sm-8")
        $(container).parent().find("#contentField").find(".col-xs-10").removeClass("col-xs-10").addClass("col-xs-8")
    }
}

/*
 * save the parameter enter by the user
 * @param {type} data
 * @returns {undefined}
 */
function saveExecution(data) {
    // Disable the save button to avoid double click.
    $("#saveScript").attr("disabled", true);

    var doc = new Doc();

    var propertyWithoutCountry = false;

    var rMessage = $("#returnMessageEx").val();
    var rCode = $("#controlstatus").html();
    var executor = $("#tabDetail input#executor").val();

    var saveProp = function () {
        showLoaderInModal('#propertiesModal');
        getScriptInformationOfStep()
        $.ajax({
            url: "UpdateTestCaseExecution",
            async: true,
            method: "POST",
            contentType: 'application/json; charset=utf-8',
            //dataType: 'json',
            data: JSON.stringify({
                executionId: GetURLParameter("executionId"),
                controlstatus: rCode,
                executor: executor,
                returnMessage: rMessage,
                stepArray: getScriptInformationOfStep()
            }),
            success: function () {

                /*var stepHtml = $("#steps li.active");
                 var stepData = stepHtml.data("item");
                 var tabActive = $("#tabsScriptEdit li.active a").attr("name");*/

                var parser = document.createElement('a');
                parser.href = window.location.href;

                var new_uri = parser.pathname + "?executionId=" + GetURLParameter("executionId"); //"?test=" + GetURLParameter("test") + "&testcase=" + GetURLParameter("testcase") + "&step=" + 0/*stepData.sort*/ + "&tabactive=" + tabActive;
                //setModif(false);
                window.location.href = new_uri;
            },
            error: showUnexpectedError
        });
    };

    if (propertyWithoutCountry) {
        showModalConfirmation(function () {
            $('#confirmationModal').modal('hide');
            saveProp();
        }, undefined, doc.getDocLabel("page_global", "btn_savetableconfig"), doc.getDocLabel("page_testcasescript", "warning_no_country"), "", "", "", "");
    } else {
        saveProp();
    }

}

/*
 *
 * @param {type} stepsData
 * @returns {Array}
 */

function getScriptInformationOfStep() {
    var steps = $("#steps a");
    var stepArr = [];

    // Construct the step/action/control list:
    // Iterate over steps
    for (var i = 0; i < steps.length; i++) {
        var step = $(steps[i]).data("item");
        var actionArr = [];
        // Get step's actions
        var actions = step.stepActionContainer.find("[name='fullActionDiv']");

        // Iterate over actions
        for (var j = 0; j < actions.length; j++) {

            var controlArr = [];
            var action = $(actions[j]).find("div.itemContainer").data("item");

            // Get action's controls
            var controls = $(actions[j]).find("a.control");

            // Iterate over controls
            for (var k = 0; k < controls.length; k++) {
                var control = $(controls[k]).find("div.itemContainer").data("item");
                controlArr.push(control.getJsonData());
            }
            var actionJson = action.getJsonData();
            actionJson.controlArr = controlArr;
            actionArr.push(actionJson);
        }
        var stepJson = step.getJsonData();
        stepJson.actionArr = actionArr;
        stepArr.push(stepJson);
    }
    return stepArr;
}


