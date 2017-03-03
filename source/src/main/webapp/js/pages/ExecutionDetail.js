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
        var stepList = [];
        var executionId = GetURLParameter("executionId");
        /* global */ sockets = [];
        initPage(executionId);
        loadExecutionInformation(executionId, stepList, sockets);
    });
});

function loadExecutionInformation(executionId, stepList, sockets) {

    $.ajax({
        url: "ReadTestCaseExecution",
        method: "GET",
        data: "executionId=" + executionId,
        datatype: "json",
        async: true,
        success: function (data) {
            var tce = data.testCaseExecution;
            updatePage(tce, stepList);
            if (tce.controlStatus == "PE") {
                var parser = document.createElement('a');
                parser.href = window.location.href;

                var protocol = "ws:";
                if (parser.protocol == "https:") {
                    protocol = "wss:";
                }
                var path = parser.pathname.split("ExecutionDetail2")[0];
                var new_uri = protocol + parser.host + path + "execution/" + executionId;

                var socket = new WebSocket(new_uri);

                socket.onopen = function (e) {
                } //on "écoute" pour savoir si la connexion vers le serveur websocket s'est bien faite
                socket.onmessage = function (e) {
                    var data = JSON.parse(e.data);
                    updatePage(data, stepList);
                } //on récupère les messages provenant du serveur websocket
                socket.onclose = function (e) {
                } //on est informé lors de la fermeture de la connexion vers le serveur
                socket.onerror = function (e) {
                    setTimeout(function () {
                        loadExecutionInformation(executionId, stepList);
                    }, 5000);
                } //on traite les cas d'erreur*/

                // Remain in memory
                sockets.push(socket);
            }
            $("#seeProperties").click(function () {
                $("#propertiesModal").modal('show');
            });
        }
    });
}

function initPage(id) {

    var doc = new Doc();
    var height = $("nav.navbar.navbar-inverse.navbar-static-top").outerHeight(true) + $("div.alert.alert-warning").outerHeight(true) + $(".page-title-line").outerHeight(true) - 10;
    $('#divPanelDefault').affix({offset: {top: height}});

    var wrap = $(window);

    wrap.on("scroll", function (e) {
        $(".affix").width($("#page-layout").width() - 3);
    });

    $("#editTcInfo").prop("disabled", true);
    $("#runTestCase").prop("disabled", true);
    $("#lastExecution").prop("disabled", true);

    $("#runOld").click(function () {
        window.location = "ExecutionDetail.jsp?id_tc=" + id;
    });

    $("#editTag").click(function () {
        $(this).hide();
        $("#saveTag").show();
        $("#tag").attr("readonly", false);
    });

    $("#saveTag").click(function () {
        $("#tag").attr("readonly", true);
        $(this).attr("disabled", true);
        $.ajax({
            url: "SetTagToExecution",
            data: {"executionId": id, newTag: $("#tag").val()},
            success: function (data) {
                $("#saveTag").attr("disabled", false);
                $("#saveTag").hide();
                $("#editTag").show();
            }
        })
    });

    displayHeaderLabel(doc);
    displayFooter(doc);
    displayPageLabel(doc);

    $("#inheritedPropPanelWrapper").hide();
    $("[name='buttonSave']").hide();
    $("#addProperty").hide();
    $("#duplicateButtons").hide();

    var wrap = $(window);

//    wrap.on("scroll", function (e) {
//        if ($("#list-wrapper").width() != $("#nav-execution").parent().width() - 30) {
//            $("#list-wrapper").width($("#nav-execution").width());
//        }
//    });
//
//    wrap.resize(function (e) {
//        if ($("#list-wrapper").width() != $("#nav-execution").parent().width() - 30) {
//            $("#list-wrapper").width($("#nav-execution").width());
//        }
//    })
}

function displayPageLabel(doc) {
    $("#pageTitle").text(doc.getDocLabel("page_executiondetail", "title"));
    $(".alert.alert-warning span").text(doc.getDocLabel("page_global", "beta_message"));
    $(".alert.alert-warning button").text(doc.getDocLabel("page_global", "old_page"));
    $("#ExecutionByTag").html("<span class='glyphicon glyphicon-tag'></span> " + doc.getDocLabel("page_executiondetail", "see_execution_tag"));
    $("#more").text(doc.getDocLabel("page_executiondetail", "more_detail"));
    $("#testCaseDetails label[for='application']").text(doc.getDocLabel("page_executiondetail", "application"));
    $("#testCaseDetails label[for='browser']").text(doc.getDocLabel("page_executiondetail", "browser"));
    $("#testCaseDetails label[for='browserfull']").text(doc.getDocLabel("page_executiondetail", "browserfull"));
    $("#testCaseDetails label[for='country']").text(doc.getDocLabel("page_executiondetail", "country"));
    $("#testCaseDetails label[for='environment']").text(doc.getDocLabel("page_executiondetail", "environment"));
    $("#testCaseDetails label[for='status']").text(doc.getDocLabel("page_executiondetail", "status"));
    $("#testCaseDetails label[for='controlstatus2']").text(doc.getDocLabel("page_executiondetail", "controlstatus"));
    $("#testCaseDetails label[for='controlmessage']").text(doc.getDocLabel("page_executiondetail", "controlmessage"));
    $("#testCaseDetails label[for='ip']").text(doc.getDocLabel("page_executiondetail", "ip"));
    $("#testCaseDetails label[for='port']").text(doc.getDocLabel("page_executiondetail", "port"));
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
    $("#testCaseDetails label[for='tag']").text(doc.getDocLabel("page_executiondetail", "tag"));
    $("#testCaseDetails label[for='verbose']").text(doc.getDocLabel("page_executiondetail", "verbose"));
    $("#testCaseDetails label[for='build']").text(doc.getDocLabel("page_executiondetail", "build"));
    $("#testCaseDetails label[for='version']").text(doc.getDocLabel("page_executiondetail", "version"));
    $("#steps h3").text(doc.getDocLabel("page_executiondetail", "steps"));
    $("#actions h3").text(doc.getDocLabel("page_global", "columnAction"));
    $("#editTcInfo").html("<span class='glyphicon glyphicon-new-window'></span> " + doc.getDocLabel("page_executiondetail", "edittc"));
    $("#editTcStepInfo").html("<span class='glyphicon glyphicon-new-window'></span> " + doc.getDocLabel("page_executiondetail", "edittcstep"));
    $("#runTestCase").html("<span class='glyphicon glyphicon-forward'></span> " + doc.getDocLabel("page_executiondetail", "runtc"));
    $("#lastExecution").html("<span class='glyphicon glyphicon-backward'></span> " + doc.getDocLabel("page_executiondetail", "lastexecution"));
    $("#lastExecutionwithEnvCountry").html("<span class='glyphicon glyphicon-backward'></span> " + doc.getDocLabel("page_executiondetail", "lastexecutionwithenvcountry"));
}

function updatePage(data, stepList) {

    sortData(data.testCaseStepExecutionList);

    $("#editTcInfo").prop("disabled", false);
    $("#runTestCase").prop("disabled", false);
    $("#lastExecution").prop("disabled", false);

    $("#editTcToggleButton").click(function () {
        setLinkOnEditTCStepInfoButton();
    });

    $("#editTcStepInfo").attr("href", "TestCaseScript.jsp?test=" + data.test + "&testcase=" + data.testcase);
    $("#editTcInfo").attr("href", "TestCaseScript.jsp?test=" + data.test + "&testcase=" + data.testcase);
    $("#runTestCase").attr("href", "RunTests1.jsp?test=" + data.test + "&testcase=" + data.testcase + "&country=" + data.country + "&environment=" + data.environment + "&browser=" + data.browser + "&tag=" + data.tag);
    $("#ExecutionByTag").attr("href", "ReportingExecutionByTag.jsp?Tag=" + data.tag);
    $("#lastExecution").attr("href", "TestCaseExecution.jsp?test=" + data.test + "&testcase=" + data.testcase);
    $("#lastExecutionwithEnvCountry").attr("href", "TestCaseExecution.jsp?test=" + data.test + "&testcase=" + data.testcase + "&country=" + data.country + "&environment=" + data.environment + "&application=" + data.application);

    var configPanel = $("#testCaseConfig");
    configPanel.find("#idlabel").text(data.id);
    configPanel.find("#test").text(data.test);
    configPanel.find("#testcase").text(data.testcase);
    configPanel.find("#controlstatus").text(data.controlStatus);
    configPanel.find("#environment").text(data.environment);
    configPanel.find("#controlstatus").removeClass("text-primary");
    if (data.controlStatus === "PE") {
        configPanel.find("#controlstatus").addClass("text-primary");
    } else if (data.controlStatus === "OK") {
        configPanel.find("#controlstatus").addClass("text-success");
        //$("#testCaseConfig").removeClass("panel-default").addClass("panel-success");
    } else if (data.controlStatus === "KO") {
        configPanel.find("#controlstatus").addClass("text-danger");
        //$("#testCaseConfig").removeClass("panel-default").addClass("panel-danger");
    } else {
        configPanel.find("#controlstatus").addClass("text-warning");
        //$("#testCaseConfig").removeClass("panel-default").addClass("panel-warning");
    }
    configPanel.find("input#application").val(data.application);
    configPanel.find("input#browser").val(data.browser);
    configPanel.find("input#browserfull").val(data.browserFullVersion);
    configPanel.find("input#build").val(data.build);
    configPanel.find("input#country").val(data.country);
    configPanel.find("input#environment").val(data.environment);
    configPanel.find("input#environmentData").val(data.environmentData);
    configPanel.find("input#status").val(data.status);
    configPanel.find("input#controlstatus2").val(data.controlStatus);
    configPanel.find("input#controlmessage").val(data.controlMessage);
    configPanel.find("input#end").val(new Date(data.end));
    configPanel.find("input#finished").val(data.finished);
    configPanel.find("input#id").val(data.id);
    configPanel.find("input#ip").val(data.ip);
    configPanel.find("input#port").val(data.port);
    configPanel.find("input#platform").val(data.platform);
    configPanel.find("input#revision").val(data.revision);
    configPanel.find("input#cerberusversion").val(data.crbVersion);
    configPanel.find("input#executor").val(data.executor);
    configPanel.find("input#screenSize").val(data.screenSize);
    configPanel.find("input#start").val(new Date(data.start));
    configPanel.find("input#tag").val(data.tag);
    configPanel.find("input#url").val(data.url);
    configPanel.find("input#verbose").val(data.verbose);
    configPanel.find("input#version").val(data.version);

    configPanel.find("input#conditionOperTC").val(data.conditionOper);
    configPanel.find("input#conditionVal1InitTC").val(data.conditionVal1Init);
    configPanel.find("input#conditionVal2InitTC").val(data.conditionVal2Init);
    configPanel.find("input#conditionVal1TC").val(data.conditionVal1);
    configPanel.find("input#conditionVal2TC").val(data.conditionVal2);

    // Adding all media attached to execution.
    var fileContainer = $("#testCaseConfig #tcFileContentField");
    addFileLink(data.fileList, fileContainer);

    var myURL = $("#bugID").data("appBugURL");
    if (myURL === undefined) {
        // We only refresh the bugURL and call readApplication if the information is not already filed.
        $.ajax({
            url: "ReadApplication",
            data: {application: data.application},
            async: true,
            success: function (dataApp) {
                var link;
                var newBugURL = dataApp.contentTable.bugTrackerNewUrl;
                if ((data.testCaseObj.bugId == undefined || data.testCaseObj.bugId == "") && newBugURL != undefined) {
                    newBugURL = newBugURL.replace("%EXEID%", data.id);
                    newBugURL = newBugURL.replace("%EXEDATE%", new Date(data.start).toLocaleString());
                    newBugURL = newBugURL.replace("%TEST%", data.test);
                    newBugURL = newBugURL.replace("%TESTCASE%", data.testcase);
                    newBugURL = newBugURL.replace("%TESTCASEDESC%", data.testCaseObj.description);
                    newBugURL = newBugURL.replace("%COUNTRY%", data.country);
                    newBugURL = newBugURL.replace("%ENV%", data.environment);
                    newBugURL = newBugURL.replace("%BUILD%", data.build);
                    newBugURL = newBugURL.replace("%REV%", data.revision);
                    newBugURL = newBugURL.replace("%BROWSER%", data.browser);
                    newBugURL = newBugURL.replace("%BROWSERFULLVERSION%", data.browserFullVersion);
                    link = $('<a target="_blank" id="bugID">').attr("href", newBugURL).append($("<button class='btn btn-default btn-block'>").text("Open a new bug"));
                } else {
                    newBugURL = dataApp.contentTable.bugTrackerUrl;
                    if (newBugURL != undefined && newBugURL != "") {
                        newBugURL = newBugURL.replace("%BUGID%", data.testCaseObj.bugId);
                        link = $('<a target="_blank" id="bugID">').attr("href", newBugURL).append($("<button class='btn btn-default btn-block'>").text(data.testCaseObj.bugId));
                    } else {
                        link = $("<span>").text(data.testCaseObj.bugId);
                    }
                }
                $("#bugID").append(link);
                $("#bugID").data("appBugURL", "true");
            }
        });
    }

    createStepList(data.testCaseStepExecutionList, stepList);
    createProperties(data.testCaseExecutionDataList);
    updateLoadBar(data);
}

function setLinkOnEditTCStepInfoButton() {
    var currentStep = $('#stepInfo');
    $("#editTcStepInfo").attr("href", "TestCaseScript.jsp?test=" + currentStep.attr('test') + "&testcase=" + currentStep.attr('testcase') + "&step=" + currentStep.attr('step'));
}

function updateLoadBar(data) {
    var total = 0;
    var ended = 0;
    if (data.testCaseObj != undefined && data.testCaseObj.testCaseStepList != undefined) {
        for (var i = 0; i < data.testCaseObj.testCaseStepList.length; i++) {
            var step = data.testCaseObj.testCaseStepList[i];
            var stepExec = data.testCaseStepExecutionList[i];
            if (stepExec != undefined && stepExec.returnCode != "PE") {
                ended += 1;
            }
            total += 1;
            for (var j = 0; j < step.testCaseStepActionList.length; j++) {
                var action = step.testCaseStepActionList[j];
                if (stepExec != undefined) {
                    var actionExec = stepExec.testCaseStepActionExecutionList[j];
                    if (actionExec != undefined && actionExec.returnCode != "PE") {
                        ended += 1;
                    }
                }
                total += 1;
                for (var k = 0; k < action.testCaseStepActionControlList.length; k++) {
                    var control = action.testCaseStepActionControlList[k];
                    if (stepExec != undefined && actionExec != undefined) {
                        var controlExec = actionExec.testCaseStepActionControlExecutionList[k];
                        if (controlExec != undefined && controlExec.returnCode != "PE") {
                            ended += 1;
                        }
                    }
                    total += 1;
                }
            }
        }
    }
    var progress = ended / total * 100;
    if (data.controlStatus != "PE") {
        if (data.controlStatus === "OK") {
            $("#progress-bar").addClass("progress-bar-success");
        } else if (data.controlStatus === "KO") {
            $("#progress-bar").addClass("progress-bar-danger");
        } else {
            $("#progress-bar").addClass("progress-bar-warning");
        }
        $("#progress-bar").empty().append($("<span style='font-weight:900;'>").append(data.controlStatus));
        progress = 100;
    }
    $("#progress-bar").css("width", progress + "%").attr("aria-valuenow", progress);
}
/** DATA AGREGATION **/

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
        if (property.RC == "OK") {
            rcDiv.append($("<h4>").html("<span class='glyphicon glyphicon-ok pull-left' style='font-size:1.5em'></span>"))
        } else if (property.RC == "FA") {
            rcDiv.append($("<h4>").html("<span class='glyphicon glyphicon-alert pull-left' style='font-size:1.5em'></span>"))
        } else if (property.RC == "PE") {
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

        if (property.RC == "OK") {
            content.addClass("panel-success");
        } else if (property.RC == "KO") {
            content.addClass("panel-danger");
        } else if (property.RC == "PE") {
            content.addClass("panel-primary");
        } else {
            content.addClass("panel-warning");
        }

        table.append(content);
    }

    sortProperties("#inheritedPropPanel");
    return propertyArray;
}

function createStepList(data, stepList) {
    $("#actionContainer").empty();
    $("#stepList").empty();

    for (var i = 0; i < data.length; i++) {
        var step = data[i];
        var stepObj = new Step(step, stepList);

        stepObj.draw();
        stepList.push(stepObj);
    }
    if (stepList.length > 0) {
        $("#stepList a:last-child").trigger("click");
    }
}

/** JAVASCRIPT OBJECT **/

function Step(json, stepList) {
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
    this.conditionOper = json.conditionOper;
    this.conditionVal1 = json.conditionVal1;
    this.conditionVal2 = json.conditionVal2;
    this.conditionVal1Init = json.conditionVal1Init;
    this.conditionVal2Init = json.conditionVal2Init;
    this.useStep = json.useStep;
    this.useStepTest = json.useStepTest;
    this.useStepTestCase = json.useStepTestCase;
    this.useStepTestCaseStep = json.useStepTestCaseStep;
    this.useStepStep = json.useStepStep;
    this.inLibrary = json.inLibrary;
    this.actionList = [];
    this.setActionList(json.testCaseStepActionExecutionList);

    this.stepList = stepList;
    this.toDelete = false;

    this.html = $("<a href='#'></a>").addClass("list-group-item row").css("margin-left", "0px").css("margin-right", "0px");
    if (this.test === "Pre Testing") {
        var stepDesc = "[PRE]  " + this.description + "  (" + this.timeElapsed + ")";
    } else {
        var stepDesc = "[" + this.sort + "." + +this.index + "]  " + this.description + "  (" + this.timeElapsed + ")";
    }
    this.textArea = $("<div></div>").addClass("col-lg-10").text(stepDesc);

}

Step.prototype.draw = function () {
    var htmlElement = this.html;


    htmlElement.data("item", this);
    htmlElement.click(this.show);
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
    } else {
        htmlElement.prepend($("<span>").addClass("glyphicon glyphicon-alert pull-left"));
        object.html.addClass("list-group-item-warning");
    }
    htmlElement.append(this.textArea);
    $("#stepList").append(htmlElement);
    $("#actionContainer").append(this.stepActionContainer);
};

Step.prototype.show = function () {
    var doc = new Doc();
    var object = $(this).data("item");
    var stepDesc = $("<div>").addClass("col-sm-10");
    var stepButton = $("<div id='stepPlus'></a>").addClass("col-sm-1").addClass("paddingLeft0").addClass("paddingTop30").append($("<span class='glyphicon glyphicon-chevron-down'></span>").attr("style", "font-size:1.5em"));
//    var stepButton1 = $("<div id='stepPlus'></div>").addClass("col-sm-1").append($("<span class='glyphicon glyphicon-chevron-down'></span>").attr("style", "font-size:1.5em"));
//    stepButton.append(stepButton1);

    for (var i = 0; i < object.stepList.length; i++) {
        var step = object.stepList[i];

        step.stepActionContainer.hide();
        step.html.removeClass("active");
    }
    $("#stepInfo").empty();
    $("#stepContent").removeClass();
    $(this).addClass("active");

    if (object.returnCode === "OK") {
        $("#stepInfo").prepend($("<div>").addClass("col-sm-1").append($("<h2>").addClass("glyphicon glyphicon-ok pull-left text-success").attr("style", "font-size:3em")));
        // $("#stepContent").addClass("col-lg-9");
    } else if (object.returnCode === "PE") {
        $("#stepInfo").prepend($("<div>").addClass("col-sm-1").append($("<h2>").addClass("glyphicon glyphicon-refresh spin pull-left text-info").attr("style", "font-size:3em")));
        // $("#stepContent").addClass("col-lg-9");
    } else if (object.returnCode === "KO") {
        $("#stepInfo").prepend($("<div>").addClass("col-sm-1").append($("<h2>").addClass("glyphicon glyphicon-remove pull-left text-danger").attr("style", "font-size:3em")));
        // $("#stepContent").addClass("col-lg-9");
    } else {
        $("#stepInfo").prepend($("<div>").addClass("col-sm-1").append($("<h2>").addClass("glyphicon glyphicon-alert pull-left text-warning").attr("style", "font-size:3em")));
        // $("#stepContent").addClass("col-lg-9");
    }


    stepDesc.append($("<h2 id='stepHeaderDescription' >").text(object.description));
    stepDesc.append($("<h2 id='stepHeaderMessage' style='font-size:1.2em;'>").text(object.returnMessage));
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
    $("#stepConditionOper").val(object.conditionOper);
    $("#stepConditionVal1").val(object.conditionVal1);
    $("#stepConditionVal2").val(object.conditionVal2);
    $("#stepConditionVal1Init").val(object.conditionVal1Init);
    $("#stepConditionVal2Init").val(object.conditionVal2Init);
    $("#stepMessage").val(object.returnMessage);


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

Step.prototype.setActionList = function (actionList) {
    for (var i = 0; i < actionList.length; i++) {
        this.setAction(actionList[i]);
    }
};

Step.prototype.setAction = function (action) {
    var actionObj;
    if (action instanceof Action) {
        actionObj = action;
    } else {
        actionObj = new Action(action, this);
    }

    this.actionList.push(actionObj);

    actionObj.draw();

    actionObj.setControlList(actionObj.controlListJson);
};

Step.prototype.setDescription = function (description) {
    this.description = description;
    this.textArea.text(description);
    $("#stepHeaderDescription").text(description);
};

Step.prototype.setStep = function (step) {
    this.step = step;
};

Step.prototype.getJsonData = function () {
    var json = {};

    json.toDelete = this.toDelete;
    json.test = this.test;
    json.testcase = this.testcase;
    json.step = this.step;
    json.index = this.index;
    json.description = this.description;
    json.useStep = this.useStep;
    json.useStepTest = this.useStepTest;
    json.useStepTestCase = this.useStepTestCase;
    json.useStepStep = this.useStepStep;
    json.inLibrary = this.inLibrary;

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
        this.value1init = json.value1init;
        this.value2init = json.value2init;
        this.screenshotFileName = json.screenshotFileName;
        this.controlListJson = json.testCaseStepActionControlExecutionList;
        this.controlList = [];
        this.fileList = json.fileList;
        this.conditionOper = json.conditionOper;
        this.conditionVal1Init = json.conditionVal1Init;
        this.conditionVal2Init = json.conditionVal2Init;
        this.conditionVal1 = json.conditionVal1;
        this.conditionVal2 = json.conditionVal2;
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
        this.value1init = "";
        this.value2init = "";
        this.screenshotFileName = "";
        this.controlListJson = "";
        this.controlList = [];
        this.fileList = [];
        this.conditionOper = "always";
        this.conditionVal1Init = "";
        this.conditionVal2Init = "";
        this.conditionVal1 = "";
        this.conditionVal2 = "";
    }

    this.toDelete = false;
}

Action.prototype.draw = function () {
    var htmlElement = this.html;
    var action = this;
    var row = $("<div></div>").addClass("col-sm-10");
    var type = $("<div></div>").addClass("type");

    var header = this.generateHeader();

    row.append(header);
    row.data("item", this);

    var button = $("<div></div>").addClass("col-sm-1").append($("<span class='glyphicon glyphicon-chevron-down'></span>").attr("style", "font-size:1.5em"));

    htmlElement.prepend(button);
    htmlElement.prepend(row);

    var content = this.generateContent();

    if (action.returnCode === "OK") {
        htmlElement.prepend($("<div>").addClass("col-sm-1").append($("<span>").addClass("glyphicon glyphicon-ok").attr("style", "font-size:1.5em")));
        htmlElement.addClass("row list-group-item list-group-item-success");
        content.hide();
    } else if (action.returnCode === "PE") {
        htmlElement.prepend($("<div>").addClass("col-sm-1").append($("<span>").addClass("glyphicon glyphicon-refresh spin").attr("style", "font-size:1.5em")));
        htmlElement.addClass("row list-group-item list-group-item-info");
        content.hide();
    } else if (action.returnCode === "KO") {
        htmlElement.prepend($("<div>").addClass("col-sm-1").append($("<span>").addClass("glyphicon glyphicon-remove").attr("style", "font-size:1.5em")));
        htmlElement.addClass("row list-group-item list-group-item-danger");
        content.hide();
    } else {
        htmlElement.prepend($("<div>").addClass("col-sm-1").append($("<span>").addClass("glyphicon glyphicon-alert").attr("style", "font-size:1.5em")));
        htmlElement.addClass("row list-group-item list-group-item-warning");
        content.hide();
    }

    // Starting to reduce the size of the row by the length of elements.
    $(header).find("#contentField").removeClass("col-sm-12").addClass("col-sm-" + (12 - this.fileList.length));
    // Adding all media attached to action execution.
    addFileLink(this.fileList, $(header).find(".row"));

    this.parentStep.stepActionContainer.append(htmlElement);
    this.parentStep.stepActionContainer.append(content);
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

Action.prototype.setControlList = function (controlList) {
    for (var i = 0; i < controlList.length; i++) {
        this.setControl(controlList[i]);
    }
};

Action.prototype.setControl = function (control) {
    if (control instanceof Control) {
        control.draw();
        this.controlList.push(control);
    } else {
        var controlObj = new Control(control, this);

        controlObj.draw();
        this.controlList.push(controlObj);
    }
};

Action.prototype.setStep = function (step) {
    this.step = step;
};

Action.prototype.setSequence = function (sequence) {
    this.sequence = sequence;
};

Action.prototype.generateHeader = function () {
    var scope = this;
    var content = $("<div></div>").addClass("content");
    var firstRow = $("<div></div>").addClass("row ");
    var contentField = $("<div></div>").addClass("col-sm-12").attr("id", "contentField");
    var elapsedTime = $("<h4>").attr("style", "font-size:0.9em;margin:0px;line-height:1;height:0.9em;overflow:hidden;white-space: nowrap;text-overflow: ellipsis;");
    var returnMessageField = $("<h4>").attr("style", "font-size:.9em;margin:0px;line-height:1;height:.95em;overflow:hidden;white-space: nowrap;text-overflow: ellipsis;");
    var descriptionField = $("<h4>").attr("style", "font-size:1.2em;margin:0px;line-height:1;height:1.2em;overflow:hidden;white-space: nowrap;text-overflow: ellipsis;");

    returnMessageField.append(safeLinkify(this.returnMessage));
    descriptionField.append(this.description);

    if (this.endlong !== 19700101010000000 && this.endlong !== 0) {
        elapsedTime.append((this.endlong - this.startlong) + " ms");
    } else {
        elapsedTime.append("...");
    }


    contentField.append($("<div class='col-sm-2'>").append(elapsedTime));
    contentField.append($("<div class='col-sm-10'>").append(descriptionField).append(returnMessageField));

    firstRow.append(contentField);

    content.append(firstRow);

    return content;

};

Action.prototype.generateContent = function () {
    var obj = this;
    var doc = new Doc();

    var row1 = $("<div></div>").addClass("row");
    var row2 = $("<div></div>").addClass("row");
    var row3 = $("<div></div>").addClass("row");
    var row4 = $("<div></div>").addClass("row");
    var row5 = $("<div></div>").addClass("row");
    var row6 = $("<div></div>").addClass("row");
    var row7 = $("<div></div>").addClass("row");
    var container = $("<div id='content-container'></div>").addClass("action-group row list-group-item");

    var actionList = $("<input type='text' class='form-control' id='action'>").prop("readonly", true);
    var descField = $("<textarea type='text' rows='1' class='form-control' id='description'>").prop("readonly", true);
    var value1Field = $("<textarea type='text' rows='1' class='form-control' id='value1'>").prop("readonly", true);
    var value1InitField = $("<textarea type='text' rows='1' class='form-control' id='value1init'>").prop("readonly", true);
    var value2Field = $("<textarea type='text' rows='1' class='form-control' id='value2'>").prop("readonly", true);
    var value2InitField = $("<textarea type='text' rows='1' class='form-control' id='value2init'>").prop("readonly", true);
    var timeField = $("<input type='text' class='form-control' id='time'>").prop("readonly", true);
    var forceexecField = $("<input type='text' class='form-control' id='forceexec'>").prop("readonly", true);
    var returnCodeField = $("<input type='text' class='form-control' id='returncode'>").prop("readonly", true);
    var returnMessageField = $("<textarea style='width:100%;' class='form-control' id='returnmessage'>").prop("readonly", true);
    var sortField = $("<input type='text' class='form-control' id='sort'>").prop("readonly", true);
    var conditionOperField = $("<textarea type='text' rows='1' class='form-control' id='conditionOper'>").prop("readonly", true);
    var conditionVal1InitField = $("<textarea type='text' rows='1' class='form-control' id='conditionVal1Init'>").prop("readonly", true);
    var conditionVal2InitField = $("<textarea type='text' rows='1' class='form-control' id='conditionVal2Init'>").prop("readonly", true);
    var conditionVal1Field = $("<textarea type='text' rows='1' class='form-control' id='conditionVal1'>").prop("readonly", true);
    var conditionVal2Field = $("<textarea type='text' rows='1' class='form-control' id='conditionVal2'>").prop("readonly", true);

    var actionGroup = $("<div class='form-group'></div>").append($("<label for='action'>" + doc.getDocLabel("page_executiondetail", "action") + "</label>")).append(actionList);
    var descGroup = $("<div class='form-group'></div>").append($("<label for='description'>" + doc.getDocLabel("page_executiondetail", "description") + "</label>")).append(descField);
    var objectGroup = $("<div class='form-group'></div>").append($("<label for='value1'>" + doc.getDocLabel("page_executiondetail", "value1") + "</label>")).append(value1Field);
    var objectGroupInit = $("<div class='form-group'></div>").append($("<label for='value1init'>" + doc.getDocLabel("page_executiondetail", "value1init") + "</label>")).append(value1InitField);
    var timeGroup = $("<div class='form-group'></div>").append($("<label for='time'>" + doc.getDocLabel("page_executiondetail", "time") + "</label>")).append(timeField);
    var forceexecGroup = $("<div class='form-group'></div>").append($("<label for='forceexec'>" + doc.getDocLabel("page_executiondetail", "forceexec") + "</label>")).append(forceexecField);
    var propertyGroup = $("<div class='form-group'></div>").append($("<label for='value2'>" + doc.getDocLabel("page_executiondetail", "value2") + "</label>")).append(value2Field);
    var propertyGroupInit = $("<div class='form-group'></div>").append($("<label for='value2init'>" + doc.getDocLabel("page_executiondetail", "value2init") + "</label>")).append(value2InitField);
    var returncodeGroup = $("<div class='form-group'></div>").append($("<label for='returncode'>" + doc.getDocLabel("page_executiondetail", "return_code") + "</label>")).append(returnCodeField);
    var returnmessageGroup = $("<div class='form-group'></div>").append($("<label for='returnmessage'>" + doc.getDocLabel("page_executiondetail", "return_message") + "</label>")).append(returnMessageField);
    var sortGroup = $("<div class='form-group'></div>").append($("<label for='sort'>" + doc.getDocLabel("page_executiondetail", "sort") + "</label>")).append(sortField);
    var conditionOperGroup = $("<div class='form-group'></div>").append($("<label for='conditionOper'>" + doc.getDocLabel("page_executiondetail", "conditionOper") + "</label>")).append(conditionOperField);
    var conditionVal1InitGroup = $("<div class='form-group'></div>").append($("<label for='conditionVal1Init'>" + doc.getDocLabel("page_executiondetail", "conditionVal1Init") + "</label>")).append(conditionVal1InitField);
    var conditionVal2InitGroup = $("<div class='form-group'></div>").append($("<label for='conditionVal2Init'>" + doc.getDocLabel("page_executiondetail", "conditionVal2Init") + "</label>")).append(conditionVal2InitField);
    var conditionVal1Group = $("<div class='form-group'></div>").append($("<label for='conditionVal1'>" + doc.getDocLabel("page_executiondetail", "conditionVal1") + "</label>")).append(conditionVal1Field);
    var conditionVal2Group = $("<div class='form-group'></div>").append($("<label for='conditionVal2'>" + doc.getDocLabel("page_executiondetail", "conditionVal2") + "</label>")).append(conditionVal2Field);


    descField.val(this.description);
    actionList.val(this.action);
    value1Field.val(this.value1);
    value1InitField.val(this.value1init);
    value2Field.val(this.value2);
    value2InitField.val(this.value2init);
    if (this.endlong !== 19700101010000000 && this.endlong !== 0) {
        timeField.val((this.endlong - this.startlong) + " ms");
    } else {
        timeField.val("...");
    }
    forceexecField.val(this.forceExeStatus);
    returnCodeField.val(this.returnCode);
    returnMessageField.val(this.returnMessage);
    sortField.val(this.sort);
    conditionOperField.val(this.conditionOper);
    conditionVal1InitField.val(this.conditionVal1Init);
    conditionVal2InitField.val(this.conditionVal2Init);
    conditionVal1Field.val(this.conditionVal1);
    conditionVal2Field.val(this.conditionVal2);

    row1.append($("<div></div>").addClass("col-sm-2").append(returncodeGroup));
    row1.append($("<div></div>").addClass("col-sm-10").append(descGroup));
    row2.append($("<div></div>").addClass("col-sm-2"));
    row2.append($("<div></div>").addClass("col-sm-5").append(objectGroupInit));
    row2.append($("<div></div>").addClass("col-sm-5").append(propertyGroupInit));
    row3.append($("<div></div>").addClass("col-sm-2").append(actionGroup));
    row3.append($("<div></div>").addClass("col-sm-5").append(objectGroup));
    row3.append($("<div></div>").addClass("col-sm-5").append(propertyGroup));
    row4.append($("<div></div>").addClass("col-sm-2").append(sortGroup));
    row4.append($("<div></div>").addClass("col-sm-5").append(forceexecGroup));
    row4.append($("<div></div>").addClass("col-sm-5").append(timeGroup));
    row5.append($("<div></div>").addClass("col-sm-12").append(returnmessageGroup));
    row6.append($("<div></div>").addClass("col-sm-2"));
    row6.append($("<div></div>").addClass("col-sm-5").append(conditionVal1InitGroup));
    row6.append($("<div></div>").addClass("col-sm-5").append(conditionVal2InitGroup));
    row7.append($("<div></div>").addClass("col-sm-2").append(conditionOperGroup));
    row7.append($("<div></div>").addClass("col-sm-5").append(conditionVal1Group));
    row7.append($("<div></div>").addClass("col-sm-5").append(conditionVal2Group));

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

    json.toDelete = this.toDelete;
    json.test = this.test;
    json.testcase = this.testcase;
    json.step = this.step;
    json.index = this.index;
    json.sequence = this.sequence;
    json.description = this.description;
    json.action = this.action;
    json.value1 = this.value1;
    json.value2 = this.value2;
    json.screenshotFileName = "";

    return json;
};

function Control(json, parentAction) {
    if (json !== null) {
        this.control = json.control;
        this.controlType = json.controlType;
        this.value1 = json.controlProperty;
        this.value2 = json.controlValue;
        this.value1init = json.controlPropertyInit;
        this.value2init = json.controlValueInit;
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
        this.conditionOper = json.conditionOper;
        this.conditionVal1Init = json.conditionVal1Init;
        this.conditionVal2Init = json.conditionVal2Init;
        this.conditionVal1 = json.conditionVal1;
        this.conditionVal2 = json.conditionVal2;
    } else {
        this.control = "";
        this.controlType = "Unknown";
        this.value1 = "";
        this.value2 = "";
        this.value1init = "";
        this.value2init = "";
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
        this.conditionOper = "always";
        this.conditionVal1Init = "";
        this.conditionVal2Init = "";
        this.conditionVal1 = "";
        this.conditionVal2 = "";
    }

    this.parentStep = parentAction.parentStep;
    this.parentAction = parentAction;

    this.toDelete = false;

    this.html = $("<a href='#'></a>").addClass("action-group control").css("margin-left", "0px");
}

Control.prototype.draw = function () {
    var htmlElement = this.html;
    var row = $("<div></div>").addClass("col-sm-10");
    var type = $("<div></div>").addClass("type");

    var header = this.generateHeader();

    row.append(header);
    row.data("item", this);

    var button = $("<div></div>").addClass("col-sm-1").append($("<span class='glyphicon glyphicon-chevron-down'></span>").attr("style", "font-size:1.5em"));

    htmlElement.prepend(button);
    htmlElement.prepend(row);

    var content = this.generateContent();

    if (this.returnCode === "OK") {
        htmlElement.prepend($("<div>").addClass("col-sm-1").append($("<span>").addClass("glyphicon glyphicon-ok").attr("style", "font-size:1.5em")));
        htmlElement.addClass("row list-group-item list-group-item-success");
        content.hide();
    } else if (this.returnCode === "PE") {
        htmlElement.prepend($("<div>").addClass("col-sm-1").append($("<span>").addClass("glyphicon glyphicon-refresh spin").attr("style", "font-size:1.5em")));
        htmlElement.addClass("row list-group-item list-group-item-info");
        content.hide();
    } else if (this.returnCode === "KO") {
        htmlElement.prepend($("<div>").addClass("col-sm-1").append($("<span>").addClass("glyphicon glyphicon-remove").attr("style", "font-size:1.5em")));
        htmlElement.addClass("row list-group-item list-group-item-danger");
        content.hide();
    } else {
        htmlElement.prepend($("<div>").addClass("col-sm-1").append($("<span>").addClass("glyphicon glyphicon-alert").attr("style", "font-size:1.5em")));
        htmlElement.addClass("row list-group-item list-group-item-warning");
        content.hide();
    }


    // Starting to reduce the size of the row by the length of elements.
    $(header).find("#contentField").removeClass("col-sm-12").addClass("col-sm-" + (12 - this.fileList.length));
    // Adding all media attached to control execution.
    addFileLink(this.fileList, $(header).find(".row"));

    this.parentStep.stepActionContainer.append(htmlElement);
    this.parentStep.stepActionContainer.append(content);
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

Control.prototype.generateHeader = function () {
    var scope = this;
    var content = $("<div></div>").addClass("content");
    var firstRow = $("<div></div>").addClass("row ");
    var contentField = $("<div></div>").addClass("col-sm-12").attr("id", "contentField");
    var elapsedTime = $("<h4>").attr("style", "font-size:0.9em;margin:0px;line-height:1;height:0.9em;overflow:hidden;white-space: nowrap;text-overflow: ellipsis;");
    var returnMessageField = $("<h4>").attr("style", "font-size:.9em;margin:0px;line-height:1;height:.95em;overflow:hidden;white-space: nowrap;text-overflow: ellipsis;");
    var descriptionField = $("<h4>").attr("style", "font-size:1.2em;margin:0px;line-height:1;height:1.2em;overflow:hidden;white-space: nowrap;text-overflow: ellipsis;");

    returnMessageField.append(safeLinkify(this.returnMessage));
    descriptionField.append(this.description);

    if (this.endlong !== 19700101010000000 && this.endlong !== 0) {
        elapsedTime.append((this.endlong - this.startlong) + " ms");
    } else {
        elapsedTime.append("...");
    }

    contentField.append($("<div class='col-sm-2'>").append(elapsedTime));
    contentField.append($("<div class='col-sm-10'>").append(descriptionField).append(returnMessageField));

    firstRow.append(contentField);

    content.append(firstRow);

    return content;
};

Control.prototype.generateContent = function () {
    var doc = new Doc();
    var obj = this;

    var row1 = $("<div></div>").addClass("row");
    var row2 = $("<div></div>").addClass("row");
    var row3 = $("<div></div>").addClass("row");
    var row4 = $("<div></div>").addClass("row");
    var row5 = $("<div></div>").addClass("row");
    var row6 = $("<div></div>").addClass("row");
    var row7 = $("<div></div>").addClass("row");
    var container = $("<div id='content-container'></div>").addClass("action-group row list-group-item").css("margin-left", "25px");

    var descField = $("<textarea type='text' rows='1' class='form-control' id='description'>").prop("readonly", true);
    var returnCodeField = $("<input type='text' class='form-control' id='returncode'>").prop("readonly", true);
    var controlTypeField = $("<input type='text' class='form-control' id='controltype'>").prop("readonly", true);
    var value1Field = $("<textarea type='text' rows='1' class='form-control' id='value1'>").prop("readonly", true);
    var value1InitField = $("<textarea type='text' rows='1' class='form-control' id='value1init'>").prop("readonly", true);
    var value2Field = $("<textarea type='text' rows='1' class='form-control' id='value2'>").prop("readonly", true);
    var value2InitField = $("<textarea type='text' rows='1' class='form-control' id='value2init'>").prop("readonly", true);
    var timeField = $("<input type='text' class='form-control' id='time'>").prop("readonly", true);
    var returnMessageField = $("<textarea style='width:100%;' class='form-control' id='returnmessage'>").prop("readonly", true);
    var fatalField = $("<input type='text' class='form-control' id='fatal'>").prop("readonly", true);
    var sortField = $("<input type='text' class='form-control' id='sort'>").prop("readonly", true);
    var conditionOperField = $("<textarea type='text' rows='1' class='form-control' id='conditionOper'>").prop("readonly", true);
    var conditionVal1InitField = $("<textarea type='text' rows='1' class='form-control' id='conditionVal1Init'>").prop("readonly", true);
    var conditionVal2InitField = $("<textarea type='text' rows='1' class='form-control' id='conditionVal2Init'>").prop("readonly", true);
    var conditionVal1Field = $("<textarea type='text' rows='1' class='form-control' id='conditionVal1'>").prop("readonly", true);
    var conditionVal2Field = $("<textarea type='text' rows='1' class='form-control' id='conditionVal2'>").prop("readonly", true);

    var descGroup = $("<div class='form-group'></div>").append($("<label for='description'>" + doc.getDocLabel("page_executiondetail", "description") + "</label>")).append(descField);
    var returncodeGroup = $("<div class='form-group'></div>").append($("<label for='returncode'>" + doc.getDocLabel("page_executiondetail", "return_code") + "</label>")).append(returnCodeField);
    var returnmessageGroup = $("<div class='form-group'></div>").append($("<label for='returnmessage'>" + doc.getDocLabel("page_executiondetail", "return_message") + "</label>")).append(returnMessageField);
    var controlTypeGroup = $("<div class='form-group'></div>").append($("<label for='controltype'>" + doc.getDocLabel("page_executiondetail", "control_type") + "</label>")).append(controlTypeField);
    var controlValue1Group = $("<div class='form-group'></div>").append($("<label for='controlvalue'>" + doc.getDocLabel("page_executiondetail", "value1") + "</label>")).append(value1Field);
    var controlValue1InitGroup = $("<div class='form-group'></div>").append($("<label for='controlvalueinit'>" + doc.getDocLabel("page_executiondetail", "value1init") + "</label>")).append(value1InitField);
    var timeGroup = $("<div class='form-group'></div>").append($("<label for='time'>" + doc.getDocLabel("page_executiondetail", "time") + "</label>")).append(timeField);
    var controlValue2Group = $("<div class='form-group'></div>").append($("<label for='controlproperty'>" + doc.getDocLabel("page_executiondetail", "value2") + "</label>")).append(value2Field);
    var controlValue2InitGroup = $("<div class='form-group'></div>").append($("<label for='controlpropertyinit'>" + doc.getDocLabel("page_executiondetail", "value2init") + "</label>")).append(value2InitField);
    var fatalGroup = $("<div class='form-group'></div>").append($("<label for='fatal'>" + doc.getDocLabel("page_executiondetail", "fatal") + "</label>")).append(fatalField);
    var sortGroup = $("<div class='form-group'></div>").append($("<label for='sort'>" + doc.getDocLabel("page_executiondetail", "sort") + "</label>")).append(sortField);
    var conditionOperGroup = $("<div class='form-group'></div>").append($("<label for='conditionOper'>" + doc.getDocLabel("page_executiondetail", "conditionOper") + "</label>")).append(conditionOperField);
    var conditionVal1InitGroup = $("<div class='form-group'></div>").append($("<label for='conditionVal1Init'>" + doc.getDocLabel("page_executiondetail", "conditionVal1Init") + "</label>")).append(conditionVal1InitField);
    var conditionVal2InitGroup = $("<div class='form-group'></div>").append($("<label for='conditionVal2Init'>" + doc.getDocLabel("page_executiondetail", "conditionVal2Init") + "</label>")).append(conditionVal2InitField);
    var conditionVal1Group = $("<div class='form-group'></div>").append($("<label for='conditionVal1'>" + doc.getDocLabel("page_executiondetail", "conditionVal1") + "</label>")).append(conditionVal1Field);
    var conditionVal2Group = $("<div class='form-group'></div>").append($("<label for='conditionVal2'>" + doc.getDocLabel("page_executiondetail", "conditionVal2") + "</label>")).append(conditionVal2Field);



    descField.val(this.description);
    returnCodeField.val(this.returnCode);
    returnMessageField.val(this.returnMessage);
    controlTypeField.val(this.controlType);
    if (this.endlong !== 19700101010000000 && this.endlong !== 0) {
        timeField.val((this.endlong - this.startlong) + " ms");
    } else {
        timeField.val("...");
    }
    value1Field.val(this.value1);
    value1InitField.val(this.value1init);
    value2Field.val(this.value2);
    value2InitField.val(this.value2init);
    fatalField.val(this.fatal);
    sortField.val(this.sort);
    conditionOperField.val(this.conditionOper);
    conditionVal1InitField.val(this.conditionVal1Init);
    conditionVal2InitField.val(this.conditionVal2Init);
    conditionVal1Field.val(this.conditionVal1);
    conditionVal2Field.val(this.conditionVal2);

    row1.append($("<div></div>").addClass("col-sm-2").append(returncodeGroup));
    row1.append($("<div></div>").addClass("col-sm-10").append(descGroup));
    row2.append($("<div></div>").addClass("col-sm-2"));
    row2.append($("<div></div>").addClass("col-sm-5").append(controlValue1InitGroup));
    row2.append($("<div></div>").addClass("col-sm-5").append(controlValue2InitGroup));
    row3.append($("<div></div>").addClass("col-sm-2").append(controlTypeGroup));
    row3.append($("<div></div>").addClass("col-sm-5").append(controlValue1Group));
    row3.append($("<div></div>").addClass("col-sm-5").append(controlValue2Group));
    row4.append($("<div></div>").addClass("col-sm-2").append(sortGroup));
    row4.append($("<div></div>").addClass("col-sm-5").append(fatalGroup));
    row4.append($("<div></div>").addClass("col-sm-5").append(timeGroup));
    row5.append($("<div></div>").addClass("col-sm-12").append(returnmessageGroup));
    row6.append($("<div></div>").addClass("col-sm-2"));
    row6.append($("<div></div>").addClass("col-sm-5").append(conditionVal1InitGroup));
    row6.append($("<div></div>").addClass("col-sm-5").append(conditionVal2InitGroup));
    row7.append($("<div></div>").addClass("col-sm-2").append(conditionOperGroup));
    row7.append($("<div></div>").addClass("col-sm-5").append(conditionVal1Group));
    row7.append($("<div></div>").addClass("col-sm-5").append(conditionVal2Group));

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

    json.toDelete = this.toDelete;
    json.test = this.test;
    json.testcase = this.testcase;
    json.step = this.step;
    json.sequence = this.sequence;
    json.control = this.control;
    json.description = this.description;
    json.type = this.type;
    json.controlProperty = this.value1;
    json.controlValue = this.value2;
    json.fatal = this.fatal;
    json.screenshotFileName = this.screenshotFileName;

    return json;
};

// Function in order to add the Media files links into TestCase, step, action and control level.
function addFileLink(fileList, container) {
    $(container).find($("div[name='mediaMiniature']")).remove();
    for (var i = 0; i < fileList.length; i++) {
        if (fileList[i].fileType === "JPG") {
            var urlImage = "ReadTestCaseExecutionMedia?filename=" + fileList[i].fileName + "&filetype=" + fileList[i].fileType + "&filedesc=" + fileList[i].fileDesc;
            var fileDesc = fileList[i].fileDesc;
            var linkBox = $("<div name='mediaMiniature'>").addClass("col-sm-1").css("padding", "0px 7px 0px 7px")
                    .append(fileList[i].fileDesc).append($("<img>").attr("src", urlImage + "&h=30").css("height", "30px")
                    .click(function (e) {
                        showPicture(fileDesc, urlImage);
                        return false;
                    }));
            container.append(linkBox);
        } else if ((fileList[i].fileType === "HTML") || (fileList[i].fileType === "JSON") || (fileList[i].fileType === "TXT") || (fileList[i].fileType === "XML")) {
            var j = i;
            var urlImagetxt = "ReadTestCaseExecutionMedia?filename=" + fileList[i].fileName + "&filetype=" + fileList[i].fileType + "&filedesc=" + fileList[i].fileDesc;
            var fileDesctxt = fileList[i].fileDesc;
            var filetypetxt = fileList[i].fileType.toLowerCase();
            if (i === 0) {
                var linkBoxtxt = $("<div name='mediaMiniature'>").addClass("col-sm-1").css("padding", "0px 7px 0px 7px")
                        .append(fileList[i].fileDesc).append($("<img>").attr("src", "images/f-" + filetypetxt + ".svg")
                        .css("height", "30px").click(function (f) {
                    showTextArea(fileList[0].fileDesc, "", "ReadTestCaseExecutionMedia?filename=" + fileList[0].fileName + "&filetype=" + fileList[0].fileType + "&filedesc=" + fileList[0].fileDesc);
                    return false;
                }));
            } else if (i === 1) {
                var linkBoxtxt = $("<div name='mediaMiniature'>").addClass("col-sm-1").css("padding", "0px 7px 0px 7px")
                        .append(fileList[i].fileDesc).append($("<img>").attr("src", "images/f-" + filetypetxt + ".svg")
                        .css("height", "30px").click(function (f) {
                    showTextArea(fileList[1].fileDesc, "", "ReadTestCaseExecutionMedia?filename=" + fileList[1].fileName + "&filetype=" + fileList[1].fileType + "&filedesc=" + fileList[1].fileDesc);
                    return false;
                }));
            } else if (i === 2) {
                var linkBoxtxt = $("<div name='mediaMiniature'>").addClass("col-sm-1").css("padding", "0px 7px 0px 7px")
                        .append(fileList[i].fileDesc).append($("<img>").attr("src", "images/f-" + filetypetxt + ".svg")
                        .css("height", "30px").click(function (f) {
                    showTextArea(fileList[2].fileDesc, "", "ReadTestCaseExecutionMedia?filename=" + fileList[2].fileName + "&filetype=" + fileList[2].fileType + "&filedesc=" + fileList[2].fileDesc);
                    return false;
                }));
            }
            container.append(linkBoxtxt);
        }
    }
}

