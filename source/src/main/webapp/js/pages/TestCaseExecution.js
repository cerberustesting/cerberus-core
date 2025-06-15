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
var paramActivatewebsocketpush = "N";
var paramWebsocketpushperiod = 5000;
var networkStat = {};
var configDo = {};
var configNb = {};
var configSize = {};
var configGantt = {};
var sortCol = 1;
var stepFocus = -1;
var falseNegative = false;

$.when($.getScript("js/global/global.js")).then(function () {
    $(document).ready(function () {
        var steps = [];
        var doc = new Doc();
        displayHeaderLabel(doc);
        displayFooter(doc);
        displayPageLabel(doc);

        bindToggleCollapse();

        $("#sortSize").click(function () {
            sortCol = 1;
            update_thirdParty_Chart();
        });
        $("#sortRequest").click(function () {
            sortCol = 2;
            update_thirdParty_Chart();
        });
        $("#sortTime").click(function () {
            sortCol = 3;
            update_thirdParty_Chart();
        });


        var availableUsers = getUserArray(true);
        $("#tabDetail input#executor").autocomplete({
            source: availableUsers,
            messages: {
                noResults: '',
                results: function (amount) {
                    return '';
                }
            }
        });

        var executionId = GetURLParameter("executionId");
        var executionQueueId = GetURLParameter("executionQueueId");
        stepFocus = GetURLAnchorValue("stepId", 99999);
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

            $("#falseNegative").click(function () {
                toggleFalseNegative(executionId);
            });

            /* global */
            sockets = [];
            initPage(executionId);
            loadExecutionInformation(executionId, steps, sockets);

            $('[data-toggle="popover"]').popover({
                'placement': 'auto',
                'container': 'body'
            }
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

                var tc = tceq.testcase;
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

            // Save history of execution into sessionStorage.
            saveHistory(getHistoryExecution(tce), "historyExecutions", 5);
            refreshHistoryMenu();

            if (tce.controlStatus === "PE") {
                if (paramActivatewebsocketpush === "Y") {
                    var parser = document.createElement('a');
                    parser.href = window.location.href;

                    var protocol = "ws:";
                    if (parser.protocol === "https:") {
                        protocol = "wss:";
                    }
                    var path = parser.pathname.split("TestCaseExecution")[0];
                    var new_uri = protocol + parser.host + path + "api/ws/execution/" + executionId;

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

function getHistoryExecution(tce) {
    var result = {};
    result.id = tce.id;
    result.test = tce.test;
    result.testcase = tce.testcase;
    result.system = tce.system;
    result.environment = tce.environment;
    result.country = tce.country;
    result.robot = tce.robot;
    result.description = tce.description;
    result.controlStatus = tce.controlStatus;
    return result;
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
        });
    });

    $("#editRobot").click(function () {
        openModalRobot($("#tabRobot #robot").val(), "EDIT");
    });

    $("#tabEnv #editApplication").click(function () {
        openModalApplication($("#tabEnv #application").val(), "EDIT");
    });

    $("#tabEnv #editCountry").click(function () {
        openModalInvariant("COUNTRY", $("#tabEnv #country").val(), "EDIT");
    });

    $("#tabEnv #editEnvironment").click(function () {
        openModalInvariant("ENVIRONMENT", $("#tabEnv #environment").val(), "EDIT");
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
    $("#lastExecutionoT").html("<span class='glyphicon glyphicon-stats'></span> " + doc.getDocLabel("page_executiondetail", "lastexecutionoT"));
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
    $("#editTcHeader").html("<span class='glyphicon glyphicon-edit'></span> " + doc.getDocLabel("page_executiondetail", "edittch"));
    $("#editTcStepInfo").html("<span class='glyphicon glyphicon-pencil'></span> " + doc.getDocLabel("page_executiondetail", "edittcstep"));
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
        if ((data.testCaseObj.origine === "Jira-Cloud" || data.testCaseObj.origine === "Jira-DC") && (data.testCaseObj.refOrigine !== "")) {
            $("#externalRef").show();
            $("#externalRef").text(data.testCaseObj.refOrigine);
            if (data.testCaseObj.refOrigineUrl !== "") {
                $("#externalRef").attr("onclick", "window.open('" + data.testCaseObj.refOrigineUrl + "')");
                $("#externalRef").attr("style", "cursor:pointer");
            } else {
                $("#externalRef").removeAttr("onclick");
                $("#externalRef").attr("style", "cursor:auto')");
            }
        } else {
            $("#externalRef").hide();
        }
        $("#editTcInfo").attr("disabled", false);
        $("#editTcInfo").attr("href", "TestCaseScript.jsp?test=" + data.test + "&testcase=" + data.testcase);
        $("#editTcStepInfo").attr("disabled", false);
        $("#editTcStepInfo").parent().attr("href", "TestCaseScript.jsp?test=" + encodeURI(data.test) + "&testcase=" + encodeURI(data.testcase));
        $("#btnGroupDrop4").click(function () {
            setLinkOnEditTCStepInfoButton();
        });

        $("#runTestCase").attr("disabled", false);
        $("#runTestCase").on('click', function () {
            openModalExecutionSimple(data.application, data.test, data.testcase, data.description, data.country, data.environment, data.robot);
        });
        //$("#runTestCase").parent().attr("href", "RunTests.jsp?test=" + data.test + "&testcase=" + data.testcase);
        $("#rerunTestCase").attr("disabled", false);
        $("#rerunTestCase").parent().attr("href", "RunTests.jsp?test=" + data.test + "&testcase=" + data.testcase + "&country=" + data.country + "&environment=" + data.environment + "&tag=" + data.tag);
    }

    $("#lastExecution").attr("disabled", false);
    $("#lastExecution").parent().attr("href", "TestCaseExecutionList.jsp?Test=" + data.test + "&TestCase=" + data.testcase);
    $("#lastExecutionwithEnvCountry").attr("disabled", false);
    $("#lastExecutionwithEnvCountry").parent().attr("href", "TestCaseExecutionList.jsp?Test=" + data.test + "&TestCase=" + data.testcase + "&country=" + data.country + "&environment=" + data.environment + "&application=" + data.application);
    $("#lastExecutionoT").attr("disabled", false);
    $("#lastExecutionoT").parent().attr("href", "ReportingExecutionOverTime.jsp?tests=" + data.test + "&testcases=" + data.testcase);
    $("#lastExecutionoTwithEnvCountry").attr("disabled", false);
    $("#lastExecutionoTwithEnvCountry").parent().attr("href", "ReportingExecutionOverTime.jsp?tests=" + data.test + "&testcases=" + data.testcase + "&countrys=" + data.country + "&environments=" + data.environment);
    falseNegative = data.falseNegative;
    if (data.controlStatus === "OK") {
        $("#falseNegative").hide();
        $("#false-negative-bar").hide();
    } else {
        $("#falseNegative").show();
        if (data.falseNegative) {
            $("#false-negative-bar").show();
            $("#falseNegative .glyphicon").removeClass("glyphicon-ok").addClass("glyphicon-remove");
        } else {
            $("#false-negative-bar").hide();
            $("#falseNegative .glyphicon").removeClass("glyphicon-remove").addClass("glyphicon-ok");
        }
    }
    if (!isEmpty(data.tag)) {
        $("#ExecutionByTag").parent().attr("href", "ReportingExecutionByTag.jsp?Tag=" + encodeURI(data.tag));
        $("#openTag").parent().attr("href", "ReportingExecutionByTag.jsp?Tag=" + encodeURI(data.tag));
        $("#ExecutionQueueByTag").parent().attr("href", "TestCaseExecutionQueueList.jsp?tag=" + encodeURI(data.tag));
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
            triggerTestCaseExecutionQueueandSee(data.queueId, data.tag);
        });
    }

    // Adding all media attached to execution.
    var fileContainer = $("#testCaseConfig #tcFileContentField");
    var fileExeContainer = $("#testCaseConfig #tcDetailFileContentField");
    addFileLink(data.fileList, fileContainer, fileExeContainer, isTheExecutionManual);

    var myURL = $("#bugButtons").data("appBugURL");
    if (myURL === undefined) {
        // We only refresh the bugURL and call readApplication if the information is not already filed.
        $.ajax({
            url: "ReadApplication",
            data: {application: data.application},
            async: true,
            success: function (dataApp) {
                var link;

                if (data.testCaseObj !== undefined) {

                    var configPanel = $("#testCaseConfig");
                    configPanel.find("#AppLogo").attr("src", "./images/logoapp-" + dataApp.contentTable.type + ".png");

                    // Display already existing bugs.
                    link = getBugIdList(data.testCaseObj.bugs, dataApp.contentTable.bugTrackerUrl);
                    $("#bugs").append(link);

                    // Adding a button to create a new bug redirecting to Bugtracked URL.
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
                        link = $('<a target="_blank">').attr("href", newBugURL).append($("<button class='btn btn-default btn-block marginTop5'>").text(" Open a new bug From Application Bug Tracker").prepend($(" <span class='glyphicon glyphicon-new-window'></span>")));
                    } else {
                        link = $('<a>').attr("href", "#").append($("<button class='btn btn-default btn-block'>").text("No 'New Bug' URL Specified.").attr("title", "Please specify 'New Bug' URL on application '" + data.application + "'."));
                    }
                    $("#bugButtons").append(link);


                    // Open Bug with direct call to BugTracker using connector
                    if (dataApp.contentTable.bugTrackerConnector !== "NONE") {
                        link = $("<button class='btn btn-default btn-block marginTop5'>").attr("id", "addBugFromExternal").text(" Open a new bug using " + dataApp.contentTable.bugTrackerConnector + " connector").prepend($(" <span class='glyphicon glyphicon-cloud'></span>"));
                        $("#bugButtons").append(link);
                        $("#addBugFromExternal").click(function () {
                            $('#addBugFromExternal').attr('disabled', 'disabled');

                            $.ajax({
                                url: "api/executions/" + data.id + "/createBug",
                                async: true,
                                method: "POST",
                                success: function (bugCreated) {
                                    try {
                                        let bugCreatedJ = JSON.parse(bugCreated);
//                                        console.info(bugCreatedJ);
                                        if (bugCreatedJ.bug) {
//                                            console.info(bugCreatedJ.bug);
                                            $("#bugs").append(getBugIdRow(bugCreatedJ.bug.id, bugCreatedJ.bug.desc, bugCreatedJ.bug.url, bugCreatedJ.bug.act, dataApp.contentTable.bugTrackerUrlF));
                                        }
                                        showMessageMainPage(getAlertHttpType(bugCreatedJ.statusCode), bugCreatedJ.message, false, 30000);
                                    } catch (e) {

                                    }
                                    $("#addBugFromExternal").removeAttr('disabled');


                                }
                            });
                        });
                    }

                    // Open Modal TestCase Header on Bug tab
                    link = $('<a>').append($("<button class='btn btn-default btn-block marginTop5' id='editTcHeaderBug'>").text("Manually Assign a bug to Test Case"));
                    $("#bugButtons").append(link);
                    $("#editTcHeaderBug").unbind("click").click(function () {
                        openModalTestCase(data.test, data.testcase, "EDIT", "tabTCBugReport")
                    });


                }
                $("#bugButtons").data("appBugURL", "true");

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
        networkStat = data.httpStat.stat;

        // Feed index select box.
        loadIndexSelect();

        drawNetworkCharts();
    }

    $('[data-toggle="tooltip"]').tooltip();
}


function drawNetworkCharts() {
    var doc = new Doc();

    $("#editTabNetwork").show();

    drawChart_HttpStatus('myChart1');

    drawChart_SizePerType('myChart2');

    drawChart_PerThirdParty(networkStat, 'myChart3');

    var title = [doc.getDocLabel("page_executiondetail", "thirdPartygantt")];
    drawChart_GanttPerThirdParty(networkStat, title, 'myChart4');

    drawTable_Requests(networkStat, "requestTable", "#NS3Panel");

}


function updateAllGraphs() {
    update_HttpStatus();
    update_SizePerType();
    drawTable_Requests(networkStat, "requestTable", "#NS3Panel");
    update_thirdParty_Chart();
    update_GanttPerThirdParty();

}

function isIndexSelected(index, selectedIndex) {
    for (var key in selectedIndex) {
        // WARNING : type are not the same so == should be used in stead of ===
        if (selectedIndex[key].id == index) {
            return true;
        }
    }
    return false;
}

function loadIndexSelect() {
//    console.info("loadingSelect...")
    $("#selectIndex").select2({
        width: '100%' // need to override the default to secure to take the full size available.
    });
    $('#selectIndex').empty().trigger("change");

    if (networkStat.index !== undefined) {
        for (var key in networkStat.index) {
            var $option = $('<option selected></option>').text(networkStat.index[key].name).val(networkStat.index[key].index);
            $("#selectIndex").append($option); // append the option.
        }
        // Refresh the select2
        $("#selectIndex").trigger('change');
    }

}

function drawTable_Requests(data, targetTable, targetPanel) {
    var configurations = new TableConfigurationsClientSide(targetTable, data.requests, aoColumnsFunc(), true, [0, 'asc']);
    configurations.lengthMenu = [10, 15, 20, 30, 50, 100, 10000];

    if ($('#' + targetTable).hasClass('dataTable') === false) {
        createDataTableWithPermissions(configurations, undefined, targetPanel);
        showTitleWhenTextOverflow();
    } else {
        let selectedIndex = $('#selectIndex').select2('data');
        var newData = [];
        for (var key in networkStat.requests) {
            if (isIndexSelected(networkStat.requests[key].index, selectedIndex)) {
                newData.push(networkStat.requests[key]);
            }
        }

        var oTable = $("#requestTable").dataTable();
        oTable.fnClearTable();
        oTable.fnAddData(newData);
    }
}

function aoColumnsFunc() {
    var doc = new Doc();

    var aoColumns = [
        {
            "data": "start",
            "bSortable": true,
            "sName": "start",
            "title": doc.getDocOnline("page_executiondetail", "t_start"),
            "sWidth": "70px"
        },
        {
            "data": "provider",
            "bSortable": true,
            "sName": "provider",
            "title": doc.getDocOnline("page_executiondetail", "t_provider"),
            "sWidth": "100px"
        },
        {
            "data": "domain",
            "bSortable": true,
            "visible": false,
            "sName": "domain",
            "title": doc.getDocOnline("page_executiondetail", "t_domain"),
            "sWidth": "70px"
        },
        {
            "data": "url",
            "bSortable": true,
            "sName": "url",
            "title": doc.getDocOnline("page_executiondetail", "t_url"),
            "sWidth": "200px"
        },
        {
            "data": "contentType",
            "bSortable": true,
            "sName": "contentType",
            "title": doc.getDocOnline("page_executiondetail", "t_contentType"),
            "sWidth": "70px"
        },
        {
            "data": "httpStatus",
            "bSortable": true,
            "sName": "httpStatus",
            "title": doc.getDocOnline("page_executiondetail", "t_httpStatus"),
            "sWidth": "50px"
        },
        {
            "data": "size",
            "bSortable": true,
            "sName": "size",
            "title": doc.getDocOnline("page_executiondetail", "t_size"),
            "sWidth": "50px"
        },
        {
            "data": "time",
            "bSortable": true,
            "sName": "time",
            "title": doc.getDocOnline("page_executiondetail", "t_time"),
            "sWidth": "50px"
        },
        {
            "data": "index",
            "bSortable": true,
            "visible": false,
            "sName": "index",
            "title": doc.getDocOnline("page_executiondetail", "t_index"),
            "sWidth": "30px"
        },
        {
            "data": "indexName",
            "bSortable": true,
            "sName": "indexName",
            "title": doc.getDocOnline("page_executiondetail", "t_indexName"),
            "sWidth": "50px"
        }
    ];

    return aoColumns;
}


function drawChart_HttpStatus(target) {

    configNb = {
        type: 'pie',
        data: {
            datasets: [{
                    data: [],
                    backgroundColor: [],
                    label: 'Hits'
                }],
            labels: []
        },
        options: {
            responsive: true,
            title: {
                display: true,
                text: []
            }
        }
    };

    var ctx = document.getElementById(target).getContext('2d');
    window.graphNb = new Chart(ctx, configNb);

    update_HttpStatus();
}

function update_HttpStatus() {
    var doc = new Doc();

    let selectedIndex = $('#selectIndex').select2('data');
    let resultNb = {};
    let resultNbTotal = 0;
    for (var key in networkStat.requests) {
        if (isIndexSelected(networkStat.requests[key].index, selectedIndex)) {
            resultNbTotal++;
            let entryNb = "" + networkStat.requests[key].httpStatus;
            let entrySize = "" + networkStat.requests[key].contentType;
            if (resultNb.hasOwnProperty(entryNb)) {
                resultNb[entryNb]++;
            } else {
                resultNb[entryNb] = 1;
            }
        }
    }

    let data = resultNb;

    var dataArray = [];
    var labelArray = [];
    var bgColorArray = [];

    var newDataArray = [];
    for (var key in data) {
        var entry = {
            nb: data[key],
            name: key,
            color: drawChart_HttpStatus_Color(key)
        };
        newDataArray.push(entry);
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

    configNb.data.datasets[0].data = dataArray;
    configNb.data.datasets[0].backgroundColor = bgColorArray;
    configNb.data.labels = labelArray;
    configNb.options.title.text = [doc.getDocLabel("page_executiondetail", "hits"), 'total : ' + resultNbTotal];

    window.graphNb.update();
}


function drawChart_HttpStatus_Color(i) {
    if (i !== undefined) {
        if (i.includes("nbE")) {
            return "purple";
        } else if (i.startsWith("2")) {
            return "green";
        } else if (i.startsWith("3")) {
            return "lightgreen";
        } else if (i.startsWith("4")) {
            return "orange";
        } else if (i.startsWith("5")) {
            return "red";
        }
    }
    return "grey";
}


function drawChart_SizePerType(target) {

    configSize = {
        type: 'pie',
        data: {
            datasets: [{
                    data: [],
                    backgroundColor: [],
                    label: 'Size'
                }],
            labels: []
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
                }
            },
            title: {
                display: true,
                text: []
            }
        }
    };

    var ctx = document.getElementById(target).getContext('2d');
    window.graphSize = new Chart(ctx, configSize);

    update_SizePerType();

}

function update_SizePerType() {
    var doc = new Doc();

    // Filtering the data for only selected index.
    let selectedIndex = $('#selectIndex').select2('data');
    let resultSize = {};
    let resultSizeTotal = 0;
    for (var key in networkStat.requests) {
        if (isIndexSelected(networkStat.requests[key].index, selectedIndex)) {
            resultSizeTotal = resultSizeTotal + networkStat.requests[key].size;
            let entrySize = "" + networkStat.requests[key].contentType;
            if (resultSize.hasOwnProperty(entrySize)) {
                resultSize[entrySize] = resultSize[entrySize] + networkStat.requests[key].size;
            } else {
                resultSize[entrySize] = networkStat.requests[key].size;
            }
        }
    }

    let data = resultSize;

    var newDataArray = [];
    drawChart_SizePerType_Data(data.html, "html", newDataArray, "html");
    drawChart_SizePerType_Data(data.img, "img", newDataArray, "img");
    drawChart_SizePerType_Data(data.js, "js", newDataArray, "js");
    drawChart_SizePerType_Data(data.css, "css", newDataArray, "css");
    drawChart_SizePerType_Data(data.content, "content", newDataArray, "content");
    drawChart_SizePerType_Data(data.font, "font", newDataArray, "font");
    drawChart_SizePerType_Data(data.other, "other", newDataArray, "other");
    drawChart_SizePerType_Data(data.media, "media", newDataArray, "media");

    // Sorting values by nb of requests.
    sortedArrayOfObj = newDataArray.sort(function (a, b) {
        return b.nb - a.nb;
    });

    var dataArray = [];
    var labelArray = [];
    var bgColorArray = [];


    sortedArrayOfObj.forEach(function (d) {
        dataArray.push(d.nb);
        labelArray.push(d.name);
        bgColorArray.push(d.color);
    });

    configSize.data.datasets[0].data = dataArray;
    configSize.data.datasets[0].backgroundColor = bgColorArray;
    configSize.data.labels = labelArray;
    configSize.options.title.text = [doc.getDocLabel("page_executiondetail", "size"), 'total : ' + formatNumber(Math.round(resultSizeTotal / 1024)) + ' Kb'];
//    var title = [doc.getDocLabel("page_executiondetail", "size"), 'total : ' + formatNumber(Math.round(resultSizeTotal / 1024)) + ' Kb'];

    window.graphSize.update();
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
                }
            },
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

function toggleFalseNegative(executionId) {
    if (falseNegative) {

        $.ajax({
            url: "api/executions/" + executionId + "/undeclareFalseNegative",
            method: "POST",
            data: {falseNegative: false},
            success: function (data) {
                falseNegative = false;
                $("#false-negative-bar").hide();
                $("#falseNegative .glyphicon").removeClass("glyphicon-remove").addClass("glyphicon-ok");
            }
        });

    } else {
        $.ajax({
            url: "api/executions/" + executionId + "/declareFalseNegative",
            method: "POST",
            data: {falseNegative: true},
            success: function (data) {
                falseNegative = true;
                $("#false-negative-bar").show();
                $("#falseNegative .glyphicon").removeClass("glyphicon-ok").addClass("glyphicon-remove");
            }
        });
    }
}

function update_thirdParty_Chart() {
    var doc = new Doc();

    // Filtering the data for only selected index.
    let selectedIndex = $('#selectIndex').select2('data');
    let result3rdParty = {};
    let unknownDomain = [];
    let nbThirdParty = 0;
    for (var key in networkStat.requests) {
        if (isIndexSelected(networkStat.requests[key].index, selectedIndex)) {
            let entryProvider = "" + networkStat.requests[key].provider;
            // Add (unduplicated) all domains that are in unknown provider
            if (entryProvider === "unknown") {
                var exist = false;
                for (var i = 0; i < unknownDomain.length; i++) {
                    if (unknownDomain[i] === networkStat.requests[key].domain) {
                        // it happened.
                        exist = true;
                        break;
                    }
                }
                if (!exist)
                    unknownDomain.push(networkStat.requests[key].domain);
            }
            if (result3rdParty.hasOwnProperty(entryProvider)) {
                result3rdParty[entryProvider].size = result3rdParty[entryProvider].size + networkStat.requests[key].size;
                if (networkStat.requests[key].time > result3rdParty[entryProvider].time) {
                    result3rdParty[entryProvider].time = networkStat.requests[key].time;
                }
                result3rdParty[entryProvider].nb++;
            } else {
                if ((entryProvider !== "unknown") && (entryProvider !== "internal")) {
                    nbThirdParty++;
                }
                result3rdParty[entryProvider] = {
                    size: networkStat.requests[key].size,
                    nb: 1,
                    time: networkStat.requests[key].time
                };
            }
        }
    }

    // Display unknown hosts in warning mode.
    $("#detailUnknownList").empty();
    let entryUnknown = $('<li class="list-group-item">').text("Unknown Hosts/Domains:");
    $("#detailUnknownList").append(entryUnknown);
    for (var key in unknownDomain) {
        let entryUnknown = $('<li class="list-group-item list-group-item-danger">').text(unknownDomain[key]);
        $("#detailUnknownList").append(entryUnknown);
    }

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

    drawChart_GetThirdPartyDataset(result3rdParty, newDataArray);

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

    configDo.options.title.text = [doc.getDocLabel("page_executiondetail", "thirdPartychart"), 'total : ' + nbThirdParty];

    window.graph1.update();
}

function drawChart_GetThirdPartyDataset(data, newDataArray) {
    for (var key in data) {
        // Internal stat.
        if (key === "internal") {
            drawChart_PerThirdParty_data(data.internal.size, data.internal.nb, data.internal.time, "internal", newDataArray, "INTERNAL", "blue")
        } else if (key === "unknown") {
            drawChart_PerThirdParty_data(data.unknown.size, data.unknown.nb, data.unknown.time, "unknown", newDataArray, "UNKNOWN", "black")
        } else {
            drawChart_PerThirdParty_data(data[key].size, data[key].nb, data[key].time, key, newDataArray, key, get_Color_fromindex(newDataArray.length))
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
                    gridLines: {},
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

    configGantt = {
        type: 'horizontalBar',
        data: {
            labels: [],

            datasets: [{
                    label: "Start",
                    data: [],
                    backgroundColor: "rgba(63,103,126,0)",
                    hoverBackgroundColor: "rgba(50,90,100,0)"

                },
                {
                    label: "Duration",
                    data: [],
                    backgroundColor: [],
                }]
        },
        options: barOptions_stacked,
    }

    var ctx = document.getElementById(target).getContext('2d');
    window.graphGantt = new Chart(ctx, configGantt);

    // this part to make the tooltip only active on your real dataset
    var originalGetElementAtEvent = window.graphGantt.getElementAtEvent;
    window.graphGantt.getElementAtEvent = function (e) {
        return originalGetElementAtEvent.apply(this, arguments).filter(function (e) {
            return e._datasetIndex === 1;
        });
    }
    update_GanttPerThirdParty();
}

function update_GanttPerThirdParty() {

    // Filtering the data for only selected index
    let selectedIndex = $('#selectIndex').select2('data');
    let result3rdParty = {};
    for (var key in networkStat.requests) {
        if (isIndexSelected(networkStat.requests[key].index, selectedIndex)) {

            let entryProvider = "" + networkStat.requests[key].provider;
            let start = networkStat.requests[key].start;
            let end = networkStat.requests[key].start + networkStat.requests[key].time;

            if (result3rdParty.hasOwnProperty(entryProvider)) {
                if (end > result3rdParty[entryProvider].end) {
                    result3rdParty[entryProvider].end = end;
                }
                if (start < result3rdParty[entryProvider].time) {
                    result3rdParty[entryProvider].start = start;
                }
            } else {
                result3rdParty[entryProvider] = {start: start, end: end};
            }
        }
    }

    var dataArray1 = [];
    var dataArray2 = [];
    var labelArray = [];
    var bgColorArray = [];

    var newDataArray = [];

    for (var key in result3rdParty) {
        // Internal stat.
        if (key === "internal") {
            drawChart_GanttPerThirdParty_data(result3rdParty[key].start, result3rdParty[key].end - result3rdParty[key].start, "internal", newDataArray, "INTERNAL", "blue");
        } else if (key === "unknown") {
            drawChart_GanttPerThirdParty_data(result3rdParty[key].start, result3rdParty[key].end - result3rdParty[key].start, "unknown", newDataArray, "UNKNOWN", "black");
        } else {
            drawChart_GanttPerThirdParty_data(result3rdParty[key].start, result3rdParty[key].end - result3rdParty[key].start, key, newDataArray, key, get_Color_fromindex(newDataArray.length));
        }
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

    configGantt.data.labels = labelArray;
    configGantt.data.datasets[0].data = dataArray1;
    configGantt.data.datasets[1].data = dataArray2;
    configGantt.data.datasets[1].backgroundColor = bgColorArray;

    window.graphGantt.update();
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

function triggerTestCaseExecutionQueueandSee(queueId, tag) {
    $.ajax({
        url: "CreateTestCaseExecutionQueue",
        async: true,
        method: "POST",
        data: {
            id: queueId,
            actionState: "toQUEUED",
            actionSave: "save",
            tag: tag
        },
        success: function (data) {
            if (getAlertType(data.messageType) === "success") {
                showMessageMainPage(getAlertType(data.messageType), data.message, false, 60000);
                var url = "./TestCaseExecution.jsp?executionQueueId=" + data.testCaseExecutionQueueList[0].id;
//                console.info("redir : " + url);
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
    configPanel.find("#AppName").text("[" + data.application + "]");

    var favicon = new Favico({
        animation: 'slide',
        bgColor: getExeStatusRowColor(data.controlStatus)
//        textColor: "green"
    });
    favicon.badge(data.controlStatus);
//    if (data.controlStatus === "OK") {
//        // Change FaviIcon to notify the testcase is finished
//        var fi = document.getElementById("favicon");
//        fi.setAttribute("href", "logo-REST.png");
//    }
    if (data.controlStatus !== "PE") {
        configPanel.find("#duration").text("(" + getHumanReadableDuration(data.durationMs / 1000, 2) + ")");
        $("#duration").addClass(getClassDuration(data.durationMs));
    }
    configPanel.find("#duration").attr("data-original-title", new Date(data.start).toLocaleString());

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
        openModalTestCase(data.test, data.testcase, "EDIT");
    });

    configPanel.find("#environment").text(data.environment);
    configPanel.find("#country").text(data.country);
    configPanel.find("#tcDescription").text(data.description);
    configPanel.find("input#application").val(data.application);
    configPanel.find("input#build").val(data.build);
    configPanel.find("input#revision").val(data.revision);
    configPanel.find("input#country").val(data.country);
    configPanel.find("input#environment").val(data.environment);
    configPanel.find("input#environmentData").val(data.environmentData);
    configPanel.find("input#status").val(data.status);

    configPanel.find("input#end").val(new Date(data.end).toLocaleString());
    configPanel.find("input#finished").val(data.finished);
    configPanel.find("input#id").val(data.id);
    configPanel.find("input#controlstatus2").val(data.controlStatus);
    configPanel.find("textarea#controlmessage").val(data.controlMessage);
    configPanel.find("input#robot").val(data.robot);
    if (isEmpty(data.robot)) {
        configPanel.find("#editRobot").attr("disabled", true);
    } else {
        configPanel.find("#editRobot").attr("disabled", false);
    }

    configPanel.find("input#robotexe").val(data.robotExecutor);
    configPanel.find("input#robothost").val(data.robotHost);
    configPanel.find("input#robotport").val(data.robotPort);
    configPanel.find("input#platform").val(data.platform);
    if (data.platform !== "") {
        $("#exOS").text(data.platform);
        $("#exOSLogo").attr("src", "./images/platform-" + data.platform.toUpperCase() + ".png");
    }
    configPanel.find("input#browser").val(data.browser);
    if (data.browser !== "") {
        $("#exBrowser").text(data.browser + " " + data.version);
        $("#exBrowserLogo").attr("src", "./images/browser-" + data.browser + ".png");
    }
    configPanel.find("input#version").val(data.version);
    configPanel.find("input#cerberusversion").val(data.crbVersion);
    if (isTheExecutionManual) {
        $("input#executor").prop("readonly", false);
    }

    configPanel.find("input#executor").val(data.executor);
    configPanel.find("input#screenSize").val(data.screenSize);
    configPanel.find("input#userAgent").val(data.userAgent);
    configPanel.find("input#start").val(new Date(data.start).toLocaleString());
    configPanel.find("input#tag").val(data.tag);
    configPanel.find("input#url").val(data.url);
    configPanel.find("input#exetest").val(data.test);
    configPanel.find("input#exetestcase").val(data.testcase);
    configPanel.find("input#testcaseversion").val(data.testCaseVersion);
    configPanel.find("input#system").val(data.system);
    configPanel.find("input#robotdecli").val(data.robotDecli);
    configPanel.find("input#robotsessionid").val(data.robotProviderSessionId);
    if (data.robotProvider === "BROWSERSTACK") {
        if (data.tagObj !== undefined) {
            let targetUrl = "https://automate.browserstack.com/builds/" + data.tagObj.browserstackBuildHash + "/sessions/" + data.robotProviderSessionId;
            if (data.tagObj.browserstackAppBuildHash.length > 8) {
                targetUrl = "https://app-automate.browserstack.com/builds/" + data.tagObj.browserstackAppBuildHash + "/sessions/" + data.robotProviderSessionId;
            }
            let provImg = $('<img src="./images/browserstack.png" width="20">');
            $("#sessionLinkHeader").empty().append(provImg).show();
            $("#sessionLinkHeader").parent().attr("href", targetUrl).attr("target", "_blank");
            provImg = $('<img src="./images/browserstack.png" width="20">');
            $("#sessionLink").empty().append(provImg).show();
            $("#sessionLink").parent().attr("href", targetUrl).attr("target", "_blank");
        }
    } else if (data.robotProvider === "KOBITON") {
        let targetUrl = "https://portal.kobiton.com/sessions/" + data.robotProviderSessionId;
        let provImg = $('<img src="./images/kobiton.png" width="20">');
        $("#sessionLinkHeader").empty().append(provImg).show();
        $("#sessionLinkHeader").parent().attr("href", targetUrl).attr("target", "_blank");
        provImg = $('<img src="./images/kobiton.png" width="20">');
        $("#sessionLink").empty().append(provImg).show();
        $("#sessionLink").parent().attr("href", targetUrl).attr("target", "_blank");
    } else if (data.robotProvider === "LAMBDATEST") {
        //WOFHL-CU17N-2NBNL-4RBTO&build=842773
        let targetUrl = "https://automation.lambdatest.com/logs/?testID=" + data.robotProviderSessionId + "&build=" + data.tagObj.lambdaTestBuild;
        let provImg = $('<img src="./images/lambdatest.png" height="20">');
        $("#sessionLinkHeader").empty().append(provImg).show();
        $("#sessionLinkHeader").parent().attr("href", targetUrl).attr("target", "_blank");
        provImg = $('<img src="./images/lambdatest.png" height="20">');
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
    configPanel.find("input#datecreated").val(getDate(data.dateCreated));
    configPanel.find("input#usrmodif").val(data.usrModif);
    configPanel.find("input#datemodif").val(getDate(data.dateModif));


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
    $("#saveTestCaseExecution").attr("style", "display:block");
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

    // progress calculation in %. We compare nb of step, controls, action vs testcase definition.
    var total = 0;
    var ended = 0;
    if (data.testCaseObj !== undefined && data.testCaseObj.steps !== undefined) {
        for (var i = 0; i < data.testCaseObj.steps.length; i++) {
            var step = data.testCaseObj.steps[i];
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
    var propertyName = $("<h4>").attr("style", "font-size:0.9em;margin:0px;line-height:1;height:0.9em;overflow:hidden;word-wrap: break-word;text-overflow: ellipsis;");
    var returnMessageField = $("<h4>").attr("style", "font-size:.9em;margin:0px;line-height:1;height:.95em;overflow:hidden;word-wrap: break-word;text-overflow: ellipsis;");
    var descriptionField = $("<h4>").attr("style", "font-size:1.2em;margin:0px;line-height:1;height:1.2em;overflow:hidden;word-wrap: break-word;text-overflow: ellipsis;");

    returnMessageField.append(safeLinkify(property.rMessage));
    descriptionField.append(getShortenString(property.value));

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
        htmlElement.addClass("itemStatusOK")
        htmlElement.addClass("row list-group-item");
        propContent.hide();
    } else if (property.RC === "PE") {
        htmlElement.prepend($("<div>").addClass("col-sm-1").append($("<span>").addClass("glyphicon glyphicon-refresh spin").attr("style", "font-size:1.5em")));
        htmlElement.addClass("row list-group-item");
        propContent.hide();
    } else if (property.RC === "KO") {
        htmlElement.prepend($("<div>").addClass("col-sm-1").append($("<span>").addClass("glyphicon glyphicon-remove").attr("style", "font-size:1.5em")));
        htmlElement.addClass("row list-group-item");
        htmlElement.addClass("itemStatusKO")
        propContent.hide();
    } else if (property.RC === "NA") {
        htmlElement.prepend($("<div>").addClass("col-sm-1").append($("<span>").addClass("glyphicon glyphicon-alert").attr("style", "font-size:1.5em")));
        htmlElement.addClass("row list-group-item");
        propContent.hide();
    } else { // FA
        htmlElement.prepend($("<div>").addClass("col-sm-1").append($("<span>").addClass("glyphicon glyphicon-alert").attr("style", "font-size:1.5em")));
        htmlElement.addClass("itemStatusFA")
        htmlElement.addClass("row list-group-item");
        propContent.hide();
    }

    // Starting to reduce the size of the row by the length of elements.
    $(row).find("#contentField").removeClass("col-sm-10").addClass("col-sm-" + (9 - property.fileList.length)).addClass("col-sm-" + (9 - property.fileList.length));
    // Adding all media attached to action execution.
    addFileLink(property.fileList, $(row).find("#contentRow"), $(row).find("#contentRow"), isTheExecutionManual);

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
    var value3Field = $("<textarea type='text' rows='1' class='form-control' id='value3'>").prop("readonly", true);
    var value3InitField = $("<textarea type='text' rows='1' class='form-control' id='value3init'>").prop("readonly", true);
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
    var value3Group = $("<div class='form-group'></div>").append($("<label for='value3'>" + doc.getDocLabel("page_executiondetail", "value3") + "</label>")).append(value3Field);
    var value3InitGroup = $("<div class='form-group'></div>").append($("<label for='value3init'>" + doc.getDocLabel("page_executiondetail", "value3init") + "</label>")).append(value3InitField);
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
    value3Field.val(property.value3);
    value3InitField.val(property.value3Init);
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
    row2.append($("<div></div>").addClass("col-sm-5").append(value3InitGroup));
    row3.append($("<div></div>").addClass("col-sm-1").append(typeGroup));
    row3.append($("<div></div>").addClass("col-sm-1").append(rankGroup));
    row3.append($("<div></div>").addClass("col-sm-5").append(value1Group));
    row3.append($("<div></div>").addClass("col-sm-5").append(value2Group));
    row3.append($("<div></div>").addClass("col-sm-5").append(value3Group));
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

    var selectType = getSelectInvariant("PROPERTYTYPE", false, false).attr("disabled", true);
    var selectDB = getSelectInvariant("PROPERTYDATABASE", false, false).attr("disabled", true);
    var selectNature = getSelectInvariant("PROPERTYNATURE", false, false).attr("disabled", true);
    var table = $("#propTable");

    for (var ind = 0; ind < propList.length; ind++) {
        var property = propList[ind];
        propertyArray.push(propList[ind].property);

        var test = property.fromTest;
        var testcase = property.fromTestcase;

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
        var propertyDiv = $("<div>").addClass("col-sm-2").append($("<h4 style='overflow: hidden; text-overflow: ellipsis; word-wrap: break-word'; data-toggle='tooltip'>").text(property.property));
        var typeDiv = $("<div>").addClass("col-sm-2").append($("<h4 style='overflow: hidden; text-overflow: ellipsis; word-wrap: break-word'; data-toggle='tooltip'>").text(property.value));
        var messageDiv = $("<div>").addClass("col-sm-7").append($("<h4 style='overflow: hidden; text-overflow: ellipsis; word-wrap: break-word'; data-toggle='tooltip'>").text(property.rMessage));

        var propertyInput = $("<textarea style='width:100%;' rows='1' id='propName' placeholder='" + doc.getDocLabel("page_testcasescript", "property_field") + "' readonly>").addClass("form-control input-sm").val(property.property);
        var descriptionInput = $("<textarea style='width:100%;' rows='1' id='propDescription' placeholder='" + doc.getDocLabel("page_testcasescript", "description_field") + "' readonly>").addClass("form-control input-sm").val(property.description);
        var valueInput = $("<textarea style='width:100%;' rows='1' placeholder='" + doc.getDocLabel("page_testcasescript", "Value") + "' readonly></textarea>").addClass("form-control input-sm").val(property.value);
        var value1Input = $("<textarea style='width:100%;' rows='1' placeholder='" + doc.getDocLabel("page_testcasescript", "value_field") + "' readonly></textarea>").addClass("form-control input-sm").val(property.value1);
        var value1InitInput = $("<textarea style='width:100%;' rows='1' placeholder='" + doc.getDocLabel("page_testcasescript", "value1init_field") + "' readonly></textarea>").addClass("form-control input-sm").val(property.value1Init);
        var value2Input = $("<textarea style='width:100%;' rows='1' placeholder='" + doc.getDocLabel("page_testcasescript", "value2_field") + "' readonly></textarea>").addClass("form-control input-sm").val(property.value2);
        var value2InitInput = $("<textarea style='width:100%;' rows='1' placeholder='" + doc.getDocLabel("page_testcasescript", "value2init_field") + "' readonly></textarea>").addClass("form-control input-sm").val(property.value2Init);
        var value3Input = $("<textarea style='width:100%;' rows='1' placeholder='" + doc.getDocLabel("page_testcasescript", "value3_field") + "' readonly></textarea>").addClass("form-control input-sm").val(property.value3);
        var value3InitInput = $("<textarea style='width:100%;' rows='1' placeholder='" + doc.getDocLabel("page_testcasescript", "value3init_field") + "' readonly></textarea>").addClass("form-control input-sm").val(property.value3Init);
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
        var value3 = $("<div class='col-sm-6 form-group'></div>").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "value3_field"))).append(value3Input);
        var value3Init = $("<div class='col-sm-6 form-group'></div>").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "value3init_field"))).append(value3InitInput);
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
        row3.append(value3Init);
        props.append(row3);

        row4.append(value1);
        row4.append(value2);
        row4.append(value3);
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

    data.sort((a, b) => (a.start > b.start) ? 1 : -1);

    const PRE_TESTING = "Pre Testing";
    const POST_TESTING = "Post Testing";

    const preTests = data.filter(step => step.test === PRE_TESTING);
    const regularSteps = data.filter(step => (step.test !== PRE_TESTING && step.test !== POST_TESTING));
    const postTests = data.filter(step => step.test === POST_TESTING);

    const orderedStepDataList = [...preTests, ...regularSteps, ...postTests];

    for (const [i, stepData] of orderedStepDataList.entries()) {
        let step = new Step(stepData, steps, i);
        $(step).data("id", {stepId: i, actionId: -1, controlId: -1});
        step.addElements();
        step.draw();
        steps.push(step);
    }

    if ((stepFocus !== undefined) && (stepFocus !== 99999)) {
        var find = false;
        for (var i = 0; i < steps.length; i++) {
            let curStepIndex = steps[i].step + "-" + steps[i].index;
            // Use == in stead of ===
            if (curStepIndex == stepFocus) {
                find = true;
                $(steps[i].html[0]).click();
            }
        }
        if ((!find) && (steps.length > 0)) {
            $(steps[0].html[0]).click();
        }
    } else if (steps.length > 0) {
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
    this.isUsingLibraryStep = json.isUsingLibraryStep;
    this.libraryStepTest = json.libraryStepTest;
    this.libraryStepTestcase = json.libraryStepTestcase;
    this.useStepTestCaseStep = json.useStepTestCaseStep;
    this.libraryStepStepId = json.libraryStepStepId;
    this.isLibraryStep = json.isLibraryStep;
    this.actions = [];
    this.setActions(json.testCaseStepActionExecutionList, id);

    this.steps = steps;
    this.toDelete = false;

    this.html = $("<a href='#'></a>").addClass("list-group-item list-group-item-calm row stepItem").css("margin-left", "0px").css("margin-right", "0px");
    $(this.html).data("index", id);
    let timeElapsedFormat = "...";
    if (this.timeElapsed !== undefined && this.timeElapsed > 0) {
        timeElapsedFormat = this.timeElapsed;
    }
    if (this.test === "Pre Testing") {
        var stepDesc = "[PRE]  " + this.description + "  (" + timeElapsedFormat + ")";
    } else if (this.test === "Post Testing") {
        var stepDesc = "[POST]  " + this.description + "  (" + timeElapsedFormat + ")";
    } else {
        var stepDesc = "[" + this.sort + "." + +this.index + "]  " + this.description + "  (" + timeElapsedFormat + ")";
    }
    this.textArea = $("<div></div>").addClass("col-lg-10").text(stepDesc);

    var stepLabelContainer = $("<div class='col-sm-12 stepLabelContainer' style='padding-left: 0px;margin-top:10px'></div>");

    var conditionTooltip = "<b>Condition : </b>" + this.conditionOperator + "</br>";
    if (conditionVal1 !== undefined)
        conditionTooltip += "<b>Val1 : </b>" + this.conditionVal1.replaceAll("'", '').replaceAll('"', '') + "</br>";
    if (conditionVal2 !== undefined)
        conditionTooltip += "<b>Val2 : </b>" + this.conditionVal2.replaceAll("'", '').replaceAll('"', '') + "</br>";
    if (conditionVal3 !== undefined)
        conditionTooltip += "<b>Val3 : </b>" + this.conditionVal3.replaceAll("'", '').replaceAll('"', '') + "</br>";

    if (this.loop !== "onceIfConditionTrue" && this.loop !== "onceIfConditionFalse") {
        var labelOptions = $('<span class="label label-primary optionLabel labelLightGreen">Loop</span>');
        stepLabelContainer.append(labelOptions[0]);
    } else if ((this.conditionOperator !== "never")
            && (this.conditionOperator !== "always")) {
        if (this.returnCode !== "NE") {

            var labelOptions = $('<span class="label label-primary optionLabel labelLight">Condition verified</span>')
                    .attr("data-toggle", "tooltip").attr("data-html", "true").attr("data-original-title", conditionTooltip);
            stepLabelContainer.append(labelOptions[0]);
        } else {
            var labelOptions = $('<span class="label label-primary optionLabel labelLight">Not executed due to condition</span>')
                    .attr("data-toggle", "tooltip").attr("data-html", "true").attr("data-original-title", conditionTooltip);
            stepLabelContainer.append(labelOptions[0]);

        }

    }
    if ((this.loop === "onceIfConditionTrue" && this.conditionOperator === "never")
            || (this.loop === "onceIfConditionFalse" && this.conditionOperator === "always")) {
        var labelOptions = $('<span class="label label-primary optionLabel labelLight">Not executed due to condition</span>')
                .attr("data-toggle", "tooltip").attr("data-html", "true").attr("data-original-title", conditionTooltip);
        stepLabelContainer.append(labelOptions[0]);
    }


    this.textArea.append(stepLabelContainer);

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
        htmlElement.prepend($('<span class="label label-primary labelGreen optionLabel pull-left"><span class="glyphicon glyphicon-ok"></span></span>'));
        htmlElement.addClass("stepStatusOK");
    } else if (object.returnCode === "FA") {
        htmlElement.prepend($('<span class="label label-primary labelOrange optionLabel pull-left"><span class="glyphicon glyphicon-alert"></span></span>'));
        htmlElement.addClass("stepStatusFA");
    } else if (object.returnCode === "PE") {
        htmlElement.prepend($('<span class="label label-primary labelBlue optionLabel pull-left"><span class="glyphicon glyphicon-refresh spin"></span></span>'));
    } else if (object.returnCode === "KO") {
        htmlElement.prepend($('<span class="label label-primary labelRed optionLabel pull-left"><span class="glyphicon glyphicon-remove"></span></span>'));
        htmlElement.addClass("stepStatusKO");
    } else if (object.returnCode === "NA") {
        htmlElement.prepend($('<span class="label label-primary labelYellow optionLabel pull-left"><span class="glyphicon glyphicon-alert"></span></span>'));
    } else if (object.returnCode === "NE") {
        htmlElement.prepend($('<span class="label label-primary labelLight optionLabel pull-left"><span class="glyphicon glyphicon-alert"></span></span>'));
    } else if (object.returnCode === "WE" && isTheExecutionManual) {
        htmlElement.prepend($('<span class="label label-primary labelDark optionLabel pull-left"><span class="glyphicon glyphicon-question-sign"></span></span>'));
    } else {
        htmlElement.prepend($('<span class="label label-primary labelLight optionLabel pull-left"><span class="glyphicon glyphicon-alert"></span></span>'));
    }
}


//update display of the step
Step.prototype.update = function (idStep) {

    $("#stepRC").val(this.returnCode);
};


Step.prototype.show = function (a) {
    var doc = new Doc();
    var object = $(this).data("item");
    var stepDesc = $("<div>").addClass("col-xs-10");
    var stepButton = $("<div id='stepPlus'></a>").addClass("col-xs-1").addClass("paddingLeft0").addClass("paddingTop30").append($("<span class='glyphicon glyphicon-chevron-down'></span>").attr("style", "font-size:1.5em"));

    if (a.cancelable) {
        // the show action comes from a click so we save the step/index where focus was done and save it inside stepFocus for next refresh and inside the URL in case of F5.
        const url = new URL(window.location);
        url.hash = '#stepId=' + object.step + "-" + object.index;
        stepFocus = object.step + "-" + object.index;
        window.history.pushState({}, '', url);
        $("#editTcStepInfo").parent().attr("href", "./TestCaseScript.jsp?test=" + encodeURI(object.test) + "&testcase=" + encodeURI(object.testcase) + '&stepId=' + object.step);
    } else if (stepFocus != 99999) {
        $("#editTcStepInfo").parent().attr("href", "./TestCaseScript.jsp?test=" + encodeURI(object.test) + "&testcase=" + encodeURI(object.testcase) + '&stepId=' + object.step);
    }

    for (var i = 0; i < object.steps.length; i++) {
        var step = object.steps[i];
        step.stepActionContainer.hide();
        step.html.removeClass("active");
    }
    $("#stepConditionMessageContent").empty();
    $("#stepConditionMessageContent").css('display', 'none');
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
        $("#stepConditionMessageContent").text(object.returnMessage);
        $("#stepConditionMessageContent").css('display', 'block');
    } else if (object.returnCode === "WE" && isTheExecutionManual) {
        $("#stepInfo").prepend($("<div>").addClass("col-xs-1").append($("<h2>").addClass("glyphicon glyphicon-question-sign pull-left text-black").attr("style", "font-size:3em")));
    } else {
        $("#stepInfo").prepend($("<div>").addClass("col-xs-1").append($("<h2>").addClass("glyphicon glyphicon-alert pull-left text-warning").attr("style", "font-size:3em")));
    }


    stepDesc.append($("<h2 id='stepHeaderDescription' class='text-center' >").text("[" + object.sort + "." + object.index + "]  " + object.description));
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
    } else {
        $("#stepRow3").show();
        $("#stepRow4").show();
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
    json.conditionOptions = this.conditionOptions;
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
    json.isLibraryStep = this.isLibraryStep;
    json.index = this.index;
    json.loop = this.loop;
    json.returnCode = this.returnCode;
    json.sort = this.sort;
    json.start = this.start;
    json.step = this.step;
    json.test = this.test;
    json.testcase = this.testcase;
    json.toDelete = this.toDelete;
    json.isUsingLibraryStep = this.isUsingLibraryStep;
    json.libraryStepStepId = this.libraryStepStepId;
    json.libraryStepTest = this.libraryStepTest;
    json.libraryStepTestcase = this.libraryStepTestcase;
    json.useStepTestCaseStep = this.useStepTestCaseStep;
    json.screenshotFileName = "";
    //Value the user is able to modified
    json.returnMessage = this.returnMessage;

    return json;
};

function Action(json, parentStep) {
    this.html = $("<a href='#'></a>").addClass("action-group action row list-group-item");
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

    var action = this;
    var fullActionElement = $("<div name='fullActionDiv' class='initialStatus'></div>");
    //fullActionElement.data("item", action);
    var htmlElement = this.html;
    var idCurrentElement = {stepId: idMotherStep, actionId: id, controlId: -1};

//DESCRIPTION
    var description = $("<div class='description'></div>").addClass("col-sm-8");
    var returnMessageField = $("<span>").addClass("col-sm-12").attr("style", "overflow:hidden;word-wrap: break-word;text-overflow: ellipsis;font-size: 10px;margin-top: 5px;font-weight: 500;");
    var descriptionField = $("<span>").addClass("col-sm-12").attr("style", "overflow:hidden;word-wrap: break-word;text-overflow: ellipsis;font-size: 13px;");
    returnMessageField.append(safeLinkify(this.returnMessage));
    descriptionField.append(this.description);
    description.append(descriptionField);
    description.append(returnMessageField);
//END OF DESCRIPTION

//MEDIA
    var media = $("<div class='media'></div>").addClass("col-sm-3");
//END OF MEDIA

// STATUS & BUTTON
    var status = $("<div class='status'></div>").addClass("col-sm-1");
    var elapsedTime = $("<span>").attr("style", "font-size:0.9em;margin:0px;line-height:1;height:0.9em;overflow:hidden;word-wrap: break-word;text-overflow: ellipsis;");
    /**
     * If returnCode is NE, display button, else display status & elapsed time
     */
    if (isTheExecutionManual) {

        var buttonUpload = $($("<button>").addClass("btn btnLightTurquoise marginRight5").attr("type", "button").html('<span class="glyphicon glyphicon-upload"></span>'));

        var buttonGroup = $('<div class="btn-group" role="group">');
        var buttonOK = $('<button name="buttonOK" class="btn btnLightGreen"><span class="glyphicon glyphicon-ok"></span></button>');
        var buttonFA = $('<button class="btn btnLightOrange"><span class="glyphicon glyphicon-alert"></span></button>');
        var inputStatus = $('<input style="display:none" name="returncode"/>');
        var inputMessage = $('<input style="display:none" name="returnmessage"/>');
        buttonGroup.append(buttonOK).append(inputStatus).append(inputMessage).append(buttonFA).css("float", "right");

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
            var idex = $("#idlabel").text();
            openModalFile(true, action, "ADD", idex);
            event.preventDefault();
            event.stopPropagation();
        });
        $(buttonUpload).css("float", "right");

        media.append(buttonGroup).append(buttonUpload);
        showSaveTestCaseExecutionButton();
    } else {
        elapsedTime.append("<br><br>");
        elapsedTime.append(generateCleanElapsed(this.endlong, this.startlong));
    }

    if (action.returnCode === "OK") {
        status.append($('<span class="label label-primary labelGreen optionLabel pull-left"><span class="glyphicon glyphicon-ok"></span></span>'));
        htmlElement.addClass("itemStatusOK");
    } else if (action.returnCode === "FA") {
        status.append($('<span class="label label-primary labelOrange optionLabel pull-left"><span class="glyphicon glyphicon-alert"></span></span>'));
        htmlElement.addClass("itemStatusFA");
    } else if (action.returnCode === "PE") {
        status.append($('<span class="label label-primary labelBlue optionLabel pull-left"><span class="glyphicon glyphicon-refresh spin"></span></span>'));
    } else if (action.returnCode === "KO") {
        status.append($('<span class="label label-primary labelRed optionLabel pull-left"><span class="glyphicon glyphicon-remove"></span></span>'));
        htmlElement.addClass("itemStatusKO");
    } else if (action.returnCode === "NA") {
        status.append($('<span class="label label-primary labelYellow optionLabel pull-left"><span class="glyphicon glyphicon-alert"></span></span>'));
    } else if (action.returnCode === "NE") {
        status.append($('<span class="label label-primary labelLight optionLabel pull-left"><span class="glyphicon glyphicon-alert"></span></span>'));
    } else if (action.returnCode === "WE" && isTheExecutionManual) {
        status.append($('<span class="label label-primary labelDark optionLabel pull-left"><span class="glyphicon glyphicon-question-sign"></span></span>'));
    } else {
        status.append($('<span class="label label-primary labelLight optionLabel pull-left"><span class="glyphicon glyphicon-alert"></span></span>'));
    }

    status.append(elapsedTime);

    var content = this.generateContent();
    content.hide();

// END OF STATUS & BUTTON

    htmlElement.append(status);
    htmlElement.append(description);
    htmlElement.append(media);

    htmlElement.data("item", this);
    //give the action an idid
    htmlElement.data("id", idCurrentElement);

    htmlElement.click(function () {
        content.toggle();
        return false;
    });

    // Starting to reduce the size of the row by the length of elements.
    //$(header).find("#contentField").removeClass("col-xs-12").addClass("col-xs-" + (12 - this.fileList.length));
    // Adding all media attached to action execution.

    fullActionElement.append(htmlElement);
    fullActionElement.append(content);
    this.parentStep.stepActionContainer.append(fullActionElement);
    addFileLink(this.fileList, media, media, isTheExecutionManual, idMotherStep);
};


function generateCleanElapsed(endlong, startlong) {
    if (endlong !== 19700101010000000 && endlong !== 0) {
        let e1 = convToDate(endlong) - convToDate(startlong);
        if (e1 > 999) {
            return ((convToDate(endlong) - convToDate(startlong)) / 1000).toFixed(2) + ' s';
        } else {
            return e1 + " ms";
        }
    } else {
        return "...";
    }
}


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

function triggerActionExecution(element, id, status) {
    var newReturnCode = "WE";
    //update first all element of actionDiv in case of control change
    /*$(element).parents("[name='fullActionDiv']").find(".action-group").find(".status").find("span.glyphicon").removeClass().addClass("glyphicon glyphicon-ok pull-left");
     $(element).parents("[name='fullActionDiv']").find(".action-group").find(".status").find("span.label").removeClass().addClass("label label-primary labelGreen optionLabel pull-left");
     $(element).parents("[name='fullActionDiv']").find(".action-group").find("input[name='returncode']").val("OK").change();
     $(element).parents("[name='fullActionDiv']").find(".action-group").find("input[name='returncode']").attr("data-modified", "true");
     $(element).parents("[name='fullActionDiv']").find(".action").addClass("itemStatusOK");
     $(element).parents("[name='fullActionDiv']").find(".control").addClass("itemStatusOK");
     $(element).parents("[name='fullActionDiv']").removeClass("initialStatus");
     $(element).parents("[name='fullActionDiv']").find(".action-group").data("item").returnCode = "OK";*/

    // update element checked
    if (status === "OK") {
        $(element).parents(".action-group").removeClass("itemStatusFA itemStatusKO");
        $(element).parents(".action-group").addClass("itemStatusOK");
        $(element).parents(".action-group").find(".status").find("span.glyphicon").removeClass().addClass("glyphicon glyphicon-ok pull-left");
        $(element).parents(".action-group").find(".status").find("span.label").removeClass().addClass("label label-primary labelGreen optionLabel pull-left");
        $(element).parents(".action-group").find("input[name='returncode']").val("OK").change();
        newReturnCode = "OK";
    } else if (status === "FA") {
        $(element).parents(".action-group").removeClass("itemStatusOK itemStatusKO");
        $(element).parents(".action-group").addClass("itemStatusFA");
        $(element).parents(".action-group").find(".status").find("span.glyphicon").removeClass().addClass("glyphicon glyphicon-alert pull-left");
        $(element).parents(".action-group").find(".status").find("span.label").removeClass().addClass("label label-primary labelOrange optionLabel pull-left");
        $(element).parents(".action-group").find("input[name='returncode']").val("FA").change();
        newReturnCode = "FA";
    } else if (status === "KO") {
        $(element).parents(".action-group").removeClass("itemStatusOK itemStatusFA");
        $(element).parents(".action-group").addClass("itemStatusKO");
        $(element).parents(".action-group").find(".status").find("span.glyphicon").removeClass().addClass("glyphicon glyphicon-remove pull-left");
        $(element).parents(".action-group").find(".status").find("span.label").removeClass().addClass("label label-primary labelRed optionLabel pull-left");
        $(element).parents(".action-group").find("input[name='returncode']").val("KO").change();
        newReturnCode = "KO";
    }
    $(element).parents(".action-group").data("item").returnCode = newReturnCode;


    //Modify all previous action and control of the current step that have not been modified yet
    var prevElementCurrentStep = $(element).parents("[name='fullActionDiv']").prevAll('.initialStatus');
    if (!prevElementCurrentStep.find(".action").hasClass("itemStatusOK") && !prevElementCurrentStep.find(".action").hasClass("itemStatusFA") && !prevElementCurrentStep.find(".action").hasClass("itemStatusKO") && !prevElementCurrentStep.find(".control").hasClass("itemStatusOK") && !prevElementCurrentStep.find(".control").hasClass("itemStatusFA") && !prevElementCurrentStep.find(".control").hasClass("itemStatusKO")) {
        prevElementCurrentStep.find(".action").addClass("itemStatusOK");
        prevElementCurrentStep.find(".control").addClass("itemStatusOK");
        prevElementCurrentStep.find(".status").find("span.glyphicon").removeClass().addClass("glyphicon glyphicon-ok pull-left");
        prevElementCurrentStep.find(".status").find("span.label").removeClass().addClass("label label-primary labelGreen optionLabel pull-left");
        prevElementCurrentStep.find("input[name='returncode']").attr("data-modified", "true").val("OK").change();
        prevElementCurrentStep.find("input[id='returnmessage']").val("Action manually executed").change();
        prevElementCurrentStep.find(".action-group").each(function (i, obj) {
            if (typeof $(obj).data("item") !== "undefined") {
                $(obj).data("item").returnCode = "OK";
            }
        });
        prevElementCurrentStep.removeClass('initialStatus');
    }

    //Modify all previous action and control of the previous steps that have not been modified yet
    var prevElementPreviousStep = $(element).parents("[name='fullActionDiv']").parent().prevAll().find('.initialStatus');
    if (!prevElementPreviousStep.find(".action").hasClass("itemStatusOK") && !prevElementPreviousStep.find(".action").hasClass("itemStatusFA") && !prevElementPreviousStep.find(".action").hasClass("itemStatusKO")) {
        prevElementPreviousStep.find(".action").addClass("itemStatusOK");
        prevElementPreviousStep.find(".control").addClass("itemStatusOK");
        prevElementPreviousStep.find(".status").find("span.glyphicon").removeClass().addClass("glyphicon glyphicon-ok pull-left");
        prevElementPreviousStep.find(".status").find("span.label").removeClass().addClass("label label-primary labelGreen optionLabel pull-left");
        prevElementPreviousStep.find("input[name='returncode']").attr("data-modified", "true").val("OK").change();
        prevElementPreviousStep.find("input[id='returnmessage']").val("Action manually executed").change();
        prevElementPreviousStep.find(".action-group").each(function (i, obj) {
            if (typeof $(obj).data("item") !== "undefined") {
                $(obj).data("item").returnCode = "OK";
            }
        });
        prevElementPreviousStep.removeClass('initialStatus');
    }

    // Modify Steps
    var testCaseNewReturnCode = "WE";
    let koStatus = 0;
    let faStatus = 0;
    $("#actionContainer").children().each(function (i) {
        var returnCodes = $(this).find("[name='returncode']").map(function () {
            return $(this).val();
        }).get();
        //Step is KO when we have only KO and no FA, otherwise, if we have KO and FA, status of step is FA
        if (returnCodes.includes("KO") && !returnCodes.includes("FA")) {
            $($(".stepItem")[i]).find("span.glyphicon").removeClass().addClass("glyphicon glyphicon-remove pull-left");
            $($(".stepItem")[i]).find("span.label").removeClass().addClass("label label-primary labelRed optionLabel pull-left");
            $($(".stepItem")[i]).removeClass("stepStatusOK stepStatusFA");
            $($(".stepItem")[i]).addClass("stepStatusKO");
            testCaseNewReturnCode = "KO";
            koStatus++;
            if (typeof $($(".stepItem")[i]).data("item") !== 'undefined') {
                $($(".stepItem")[i]).data("item").returnCode = testCaseNewReturnCode;
            }
            //htmlElement.prepend($('<span class="label label-primary labelBlue optionLabel pull-left"><span class="glyphicon glyphicon-refresh spin"></span></span>'));

        } else if (returnCodes.includes("FA")) {
            $($(".stepItem")[i]).find("span.glyphicon").removeClass().addClass("glyphicon glyphicon-alert pull-left");
            $($(".stepItem")[i]).find("span.label").removeClass().addClass("label label-primary labelOrange optionLabel pull-left");
            $($(".stepItem")[i]).removeClass("stepStatusOK stepStatusKO");
            $($(".stepItem")[i]).addClass("stepStatusFA");
            testCaseNewReturnCode = "FA";
            faStatus++;
            if (typeof $($(".stepItem")[i]).data("item") !== 'undefined') {
                $($(".stepItem")[i]).data("item").returnCode = testCaseNewReturnCode;
            }
            //htmlElement.prepend($('<span class="label label-primary labelBlue optionLabel pull-left"><span class="glyphicon glyphicon-refresh spin"></span></span>'));

        } else {
            if (!returnCodes.includes("") || returnCodes.includes("OK")) {
                $($(".stepItem")[i]).find("span.glyphicon").removeClass().addClass("glyphicon glyphicon-ok pull-left");
                $($(".stepItem")[i]).find("span.label").removeClass().addClass("label label-primary labelGreen optionLabel pull-left");
                $($(".stepItem")[i]).removeClass("stepStatusKO stepStatusFA");
                $($(".stepItem")[i]).addClass("stepStatusOK");
                testCaseNewReturnCode = "OK";
                if (typeof $($(".stepItem")[i]).data("item") !== 'undefined') {
                    $($(".stepItem")[i]).data("item").returnCode = testCaseNewReturnCode;
                }
            }
        }

    });

    //Global result of testcase
    if (koStatus > 0) {
        testCaseNewReturnCode = "KO";
        if (faStatus > 0) {
            testCaseNewReturnCode = "FA";
        }
    } else if (faStatus > 0) {
        testCaseNewReturnCode = "FA";
    } else {
        testCaseNewReturnCode = "OK";
    }

    // Modify Execution
    var configPanel = $("#testCaseConfig");
    configPanel.find("#controlstatus").text(testCaseNewReturnCode);
    configPanel.find("input#controlstatus2").val(testCaseNewReturnCode);

    updateDataBarVisual(testCaseNewReturnCode);
}

function setTestCaseReturnCodeToNA() {
    var testCaseNewReturnCode = "NA";
    var configPanel = $("#testCaseConfig");

    configPanel.find("#controlstatus").text(testCaseNewReturnCode);
    configPanel.find("input#controlstatus2").val(testCaseNewReturnCode);

    updateDataBarVisual(testCaseNewReturnCode);
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

    this.html = $("<a href='#'></a>").addClass("action-group control row list-group-item").css("margin-left", "0px");
    $(this.html).data("index", this.sort - 1)
}

Control.prototype.draw = function (idMotherStep, idMotherAction, idControl) {
    var control = this;
    var htmlElement = this.html;
    var row = $("<div class='itemContainer'></div>").addClass("col-xs-10");
    var type = $("<div></div>").addClass("type");
    var currentControlId = {stepId: idMotherStep, actionId: idMotherAction, controlId: idControl};

//DESCRIPTION
    var description = $("<div class='description'></div>").addClass("col-sm-8");
    var returnMessageField = $("<span>").addClass("col-sm-12").attr("style", "overflow:hidden;word-wrap: break-word;text-overflow: ellipsis;font-size: 10px;margin-top: 5px;font-weight: 500;");
    var descriptionField = $("<span>").addClass("col-sm-12").attr("style", "overflow:hidden;word-wrap: break-word;text-overflow: ellipsis;font-size: 13px;");
    returnMessageField.append(safeLinkify(this.returnMessage));
    descriptionField.append(this.description);
    description.append(descriptionField);
    description.append(returnMessageField);
//END OF DESCRIPTION

//MEDIA
    var media = $("<div class='media'></div>").addClass("col-sm-3");
//END OF MEDIA

// STATUS & BUTTON
    var status = $("<div class='status'></div>").addClass("col-sm-1");
    var elapsedTime = $("<span>").attr("style", "font-size:0.9em;margin:0px;line-height:1;height:0.9em;overflow:hidden;word-wrap: break-word;text-overflow: ellipsis;");

    /**
     * If returnCode is NE, display button, else display status & elapsed time
     */
    if (isTheExecutionManual) {

        var buttonUpload = $($("<button>").addClass("btn btnLightTurquoise marginRight5").attr("type", "button").html('<span class="glyphicon glyphicon-upload"></span>'));
        var buttonGroup = $('<div class="btn-group" role="group">');
        var buttonOK = $('<button name="buttonOK" class="btn btnLightGreen"><span class="glyphicon glyphicon-ok"></span></button>');
        var buttonKO = $('<button class="btn btnLightRed"><span class="glyphicon glyphicon-alert"></span></button>');
        var inputStatus = $('<input style="display:none" name="returncode"/>');
        var inputMessage = $('<input style="display:none" name="returnmessage"/>');
        buttonGroup.append(buttonOK).append(inputStatus).append(inputMessage).append(buttonKO).css("float", "right");

        buttonOK.click(function (event) {
            event.preventDefault();
            event.stopPropagation();
            triggerActionExecution(this, id, "OK");
        });
        buttonKO.click(function (event) {
            event.preventDefault();
            event.stopPropagation();
            triggerActionExecution(this, id, "KO");
        });
        $(buttonUpload).click(function (event) {
            var idex = $("#idlabel").text()
            openModalFile(false, control, "ADD", idex)
            event.preventDefault()
            event.stopPropagation()
        });
        $(buttonUpload).css("float", "right");

        media.append(buttonGroup).append(buttonUpload);
        showSaveTestCaseExecutionButton();
    } else {
        elapsedTime.append("<br><br>");
        elapsedTime.append(generateCleanElapsed(this.endlong, this.startlong));
    }


    if (control.returnCode === "OK") {
        status.append($('<span class="label label-primary labelGreen optionLabel pull-left"><span class="glyphicon glyphicon-ok"></span></span>'));
        htmlElement.addClass("itemStatusOK");
    } else if (control.returnCode === "FA") {
        status.append($('<span class="label label-primary labelOrange optionLabel pull-left"><span class="glyphicon glyphicon-alert"></span></span>'));
        htmlElement.addClass("itemStatusFA");
    } else if (control.returnCode === "PE") {
        status.append($('<span class="label label-primary labelBlue optionLabel pull-left"><span class="glyphicon glyphicon-refresh spin"></span></span>'));
    } else if (control.returnCode === "KO") {
        status.append($('<span class="label label-primary labelRed optionLabel pull-left"><span class="glyphicon glyphicon-remove"></span></span>'));
        htmlElement.addClass("itemStatusKO");
    } else if (control.returnCode === "NA") {
        status.append($('<span class="label label-primary labelYellow optionLabel pull-left"><span class="glyphicon glyphicon-alert"></span></span>'));
    } else if (control.returnCode === "NE") {
        status.append($('<span class="label label-primary labelLight optionLabel pull-left"><span class="glyphicon glyphicon-alert"></span></span>'));
    } else if (control.returnCode === "WE" && isTheExecutionManual) {
        status.append($('<span class="label label-primary labelDark optionLabel pull-left"><span class="glyphicon glyphicon-question-sign"></span></span>'));
    } else {
        status.append($('<span class="label label-primary labelLight optionLabel pull-left"><span class="glyphicon glyphicon-alert"></span></span>'));
    }

    status.append(elapsedTime);

    var content = this.generateContent();
    content.hide();


// END OF STATUS & BUTTON

    htmlElement.append(status);
    htmlElement.append(description);
    htmlElement.append(media);

    htmlElement.data("item", this);
    //give the action an id
    htmlElement.data("id", currentControlId);

    htmlElement.click(function () {
        content.toggle();
        return false;
    });

    $(this.parentAction.html).parent().append(htmlElement);
    $(this.parentAction.html).parent().append(content);

    // Starting to reduce the size of the row by the length of elements.
    //$(header).find("#contentField").removeClass("col-xs-12").addClass("col-xs-" + (12 - this.fileList.length * 2)).addClass("col-sm-" + (12 - this.fileList.length * 2));
    // Adding all media attached to control execution.
    addFileLink(this.fileList, media, media, isTheExecutionManual, idMotherStep);
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
    var container = $("<div id='content-container'></div>").addClass("action-group row list-group-item").css("margin-left", "0px");

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
        // if ($(container).parent().parent().parent().hasClass("action")) {
        var indexAction = $(container).parents("a").data('item').index
        var currentActionOrControl = getScriptInformationOfStep()[idStep]["actionArr"][indexAction]
        openModalFile(true, currentActionOrControl, "EDIT", idex, file, !isTheExecutionManual)
        // } else {
        //    var indexAction = $(container).parents("a").data('item')
        //      var indexControl = $(this).parents("a").data('index')
        //    var currentActionOrControl = getScriptInformationOfStep()[idStep]["actionArr"][indexAction]["controlArr"][indexControl]
        //    openModalFile(false, currentActionOrControl, "EDIT", idex, file, !isTheExecutionManual)
        // }
        event.preventDefault()
        event.stopPropagation()
    } else {
        openModalFile(null, null, "VIEW", null, file, !isTheExecutionManual)
    }
}


// Function in order to add the Media files links into TestCase, step, action and control level.
function addFileLink(fileList, container, containerExe, manual, idStep) {
    var auto = manual == true ? false : true;
    $(container).find($("div[name='mediaMiniature']")).remove();
    $(containerExe).find($("div[name='mediaMiniature']")).remove();
    for (var i = 0; i < fileList.length; i++) {
        let index = i
        if ((fileList[i].fileType === "JPG") || (fileList[i].fileType === "PNG")) {
            var urlImage = "ReadTestCaseExecutionMedia?filename=" + fileList[i].fileName + "&filetype=" + fileList[i].fileType + "&filedesc=" + fileList[i].fileDesc + "&auto=" + auto;
            var fileDesc = fileList[i].fileDesc;
            var linkBox = $("<div name='mediaMiniature'>").addClass("col-sm-12").css("margin-bottom", "5px")
                    .append($("<img>").attr("src", urlImage + "&h=30&w=60").css("max-height", "30px").css("max-width", "60px")
                            .attr("data-toggle", "tooltip").attr("data-original-title", fileList[i].fileDesc)
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
            var linkBoxtxt = $("<div name='mediaMiniature'>").addClass("col-sm-12").css("margin-bottom", "5px")
                    .prepend("<br>").prepend($("<img>").attr("src", "images/f-" + filetypetxt + ".svg")
                    .attr("data-toggle", "tooltip").attr("data-original-title", fileList[i].fileDesc)
                    .css("height", "30px").click(function (e) {
                changeClickIfManual(isTheExecutionManual, container, idStep, fileList[index], e)
                return false;
            }));
            if (fileList[i].fileDesc === "Execution Log") {
                containerExe.append(linkBoxtxt);
            } else {
                container.append(linkBoxtxt);
            }
        } else if ((fileList[i].fileType === "BIN") || (fileList[i].fileType === "PDF")) {

            var linkBoxtxt = null;

            if (fileList[i].fileType === "BIN") {
                linkBoxtxt = $("<div name='mediaMiniature'>").addClass("col-sm-12").css("margin-bottom", "5px")

                        .prepend("<br>").prepend($("<img>").attr("src", "images/f-binaire.png").css("height", "30px")
                        .attr("data-toggle", "tooltip").attr("data-original-title", fileList[i].fileDesc).click(function (f) {
                    changeClickIfManual(isTheExecutionManual, container, idStep, fileList[index], f)
                    return false;
                }))
            } else if (fileList[i].fileType === "PDF") {
                linkBoxtxt = $("<div name='mediaMiniature'>").addClass("col-sm-12").css("margin-bottom", "5px")
                        .prepend("<br>").prepend($("<img>").attr("src", "images/f-pdf.svg").css("height", "30px")
                        .attr("data-toggle", "tooltip").attr("data-original-title", fileList[i].fileDesc).click(function (f) {
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
                var indexAction = $(this).parents("a").data('item').action
                var currentActionOrControl = getScriptInformationOfStep()[idStep]["actionArr"][indexAction]
                openModalFile(true, currentActionOrControl, "ADD", idex)
            } else {
                var indexAction = $(this).parents("a").data('item').action
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
            var action = $(actions[j]).find("a.action").data("item");

            // Get action's controls
            var controls = $(actions[j]).find("a.control");

            // Iterate over controls
            for (var k = 0; k < controls.length; k++) {
                var control = $(controls[k]).data("item");
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


