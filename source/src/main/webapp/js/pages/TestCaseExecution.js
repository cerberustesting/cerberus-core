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
                var path = parser.pathname.split("TestCaseExecution")[0];
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
            //disable list-group expansion in case of clicking on link
            $('.linkified').on('click', function (e) {
                e.stopPropagation();
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
        window.location = "TestCaseExecution.jsp?executionId=" + id;
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
    $("#testCaseDetails label[for='userAgent']").text(doc.getDocLabel("page_executiondetail", "userAgent"));
    $("#testCaseDetails label[for='tag']").text(doc.getDocLabel("page_executiondetail", "tag"));
    $("#testCaseDetails label[for='verbose']").text(doc.getDocLabel("page_executiondetail", "verbose"));
    $("#testCaseDetails label[for='build']").text(doc.getDocLabel("page_executiondetail", "build"));
    $("#testCaseDetails label[for='version']").text(doc.getDocLabel("page_executiondetail", "version"));
    $("#steps h3").text(doc.getDocLabel("page_executiondetail", "steps"));
    $("#actions h3").text(doc.getDocLabel("page_global", "columnAction"));
    $("#editTcInfo").html("<span class='glyphicon glyphicon-new-window'></span> " + doc.getDocLabel("page_executiondetail", "edittc"));
    $("#editTcStepInfo").html("<span class='glyphicon glyphicon-new-window'></span> " + doc.getDocLabel("page_executiondetail", "edittcstep"));
    $("#runTestCase").html("<span class='glyphicon glyphicon-forward'></span> " + doc.getDocLabel("page_executiondetail", "runtc"));
    $("#saveTestCaseExecution").html("<span class='glyphicon glyphicon-save'></span> " + doc.getDocLabel("page_executiondetail", "save"));
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
    $("#runTestCase").attr("href", "RunTests.jsp?test=" + data.test + "&testcase=" + data.testcase + "&country=" + data.country + "&environment=" + data.environment + "&browser=" + data.browser + "&tag=" + data.tag);
    $("#ExecutionByTag").attr("href", "ReportingExecutionByTag.jsp?Tag=" + data.tag);
    $("#lastExecution").attr("href", "TestCaseExecution.jsp?test=" + data.test + "&testcase=" + data.testcase);
    $("#lastExecutionwithEnvCountry").attr("href", "TestCaseExecution.jsp?test=" + data.test + "&testcase=" + data.testcase + "&country=" + data.country + "&environment=" + data.environment + "&application=" + data.application);

    updateConfigPanel(data);
    
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
    setUpClickFunctionToSaveTestCaseExecutionButton(data);
    updateExecutionStatus();
}

function updateConfigPanel(data){
    
    var configPanel = $("#testCaseConfig");
    configPanel.find("#idlabel").text(data.id);
    configPanel.find("#test").text(data.test);
    configPanel.find("#testcase").text(data.testcase);
    configPanel.find("#controlstatus").text(data.controlStatus);
    configPanel.find("#environment").text(data.environment);
    configPanel.find("#country").text(data.country);
    configPanel.find("#tcDescription").text(data.description);
    configPanel.find("#controlstatus").removeClass("text-primary");
    
    if (data.controlStatus === "PE") {
        configPanel.find("#controlstatus").addClass("text-primary");
        configPanel.find("#exReturnMessage").addClass("text-primary");
    } else if (data.controlStatus === "OK") {
        configPanel.find("#controlstatus").addClass("text-success");
        configPanel.find("#exReturnMessage").addClass("text-success");
        data.controlMessage ="The test case finished successfully";
    } else if (data.controlStatus === "KO") {
        configPanel.find("#controlstatus").addClass("text-danger");
        configPanel.find("#exReturnMessage").addClass("text-danger");
        data.controlMessage ="The test case failed on validations."
    } else if (data.controlStatus === "NE") {
        configPanel.find("#controlstatus").addClass("text-black");
        configPanel.find("#exReturnMessage").addClass("text-black");
         data.controlMessage ="The test case not executed";
    } else if (data.controlStatus === "FA") {
        configPanel.find("#controlstatus").addClass("text-black");
        configPanel.find("#exReturnMessage").addClass("text-black");
        data.controlMessage ="The test case failed to be executed because of an action.";
    }else {
        configPanel.find("#controlstatus").addClass("text-warning");
        configPanel.find("#exReturnMessage").addClass("text-warning");
    }
    configPanel.find("#exReturnMessage").text(data.controlMessage);
    
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
    configPanel.find("input#userAgent").val(data.userAgent);
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
}


/*
 * show the save button call if an action step or control have a controlStatus NE
 * @returns {undefined}
 */
function showSaveTestCaseExecutionButton(){
    $("#saveTestCaseExecution").css("display", "inherit");
}
/*
 * 
 * set up click function if the button is visible ( visible if alt least one action or step or control have a controlStatus NE )
 * @param {type} data
 * @returns {undefined}
 */
function setUpClickFunctionToSaveTestCaseExecutionButton(data){
    if (  $("#saveTestCaseExecution").is(":visible")  ){
        $("#saveTestCaseExecution").click(function(){
            saveExecution(data);
        });
    }
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

    $("#progress-bar").removeClass(function (index, className) {
        return (className.match(/(^|\s)progress-bar-\S+/g) || []).join(' ');
    });

    if (data.controlStatus != "PE") {
        if (data.controlStatus === "OK") {
            $("#progress-bar").addClass("progress-bar-success");
        } else if (data.controlStatus === "KO") {
            $("#progress-bar").addClass("progress-bar-danger");
        } else if (data.controlStatus === "NE") {
            $("#progress-bar").addClass("progress-bar-black");
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

    var table = $("#propTable");

    for (var ind = 0; ind < propList.length; ind++) {
        var property = propList[ind];
        drawProperty(property, table);
    }
}

function drawProperty(property, table) {
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

    contentfirstRow.append($("<div class='col-sm-2'>").append(propertyName));
    contentfirstRow.append($("<div class='col-sm-10'>").attr("id", "contentField").append(descriptionField).append(returnMessageField));

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
    $(row).find("#contentField").removeClass("col-sm-10").addClass("col-sm-" + (10 - property.fileList.length));
    // Adding all media attached to action execution.
    addFileLink(property.fileList, $(row).find("#contentRow"));

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
    
    var returnMessageField = $("<textarea style='width:100%;' class='form-control input-sm' id='returnmessage'>");
//    returnMessageField.prop("readonly", true);
//    if (this.returnCode === "NE")
//        returnMessageField.prop("readonly", false);
//    this.returnMessageField = returnMessageField;
    
    var indexField = $("<input type='text' class='form-control' id='index'>").prop("readonly", true);
    var natureField = $("<input type='text' class='form-control' id='nature'>").prop("readonly", true);
    var databaseField = $("<input type='text' class='form-control' id='database'>").prop("readonly", true);
    var lengthField = $("<input type='text' class='form-control' id='length'>").prop("readonly", true);
    var rowLimitField = $("<input type='text' class='form-control' id='rowLimit'>").prop("readonly", true);
    var retryNbField = $("<input type='text' class='form-control' id='retryNb'>").prop("readonly", true);
    var retryPeriodField = $("<input type='text' class='form-control' id='retryPeriod'>").prop("readonly", true);

    var typeGroup = $("<div class='form-group'></div>").append($("<label for='action'>" + doc.getDocLabel("page_executiondetail", "type") + "</label>")).append(typeField);
    var descGroup = $("<div class='form-group'></div>").append($("<label for='description'>" + doc.getDocLabel("page_executiondetail", "description") + "</label>")).append(descField);
    var value1Group = $("<div class='form-group'></div>").append($("<label for='value1'>" + doc.getDocLabel("page_executiondetail", "value1") + "</label>")).append(value1Field);
    var value1InitGroup = $("<div class='form-group'></div>").append($("<label for='value1init'>" + doc.getDocLabel("page_executiondetail", "value1init") + "</label>")).append(value1InitField);
    var timeGroup = $("<div class='form-group'></div>").append($("<label for='time'>" + doc.getDocLabel("page_executiondetail", "time") + "</label>")).append(timeField);
    var valueGroup = $("<div class='form-group'></div>").append($("<label for='forceexec'>" + doc.getDocLabel("page_executiondetail", "value") + "</label>")).append(valueField);
    var value2Group = $("<div class='form-group'></div>").append($("<label for='value2'>" + doc.getDocLabel("page_executiondetail", "value2") + "</label>")).append(value2Field);
    var value2InitGroup = $("<div class='form-group'></div>").append($("<label for='value2init'>" + doc.getDocLabel("page_executiondetail", "value2init") + "</label>")).append(value2InitField);
    var returncodeGroup = $("<div class='form-group'></div>").append($("<label for='returncode'>" + doc.getDocLabel("page_executiondetail", "return_code") + "</label>")).append(returnCodeField);
    var returnmessageGroup = $("<div class='form-group'></div>").append($("<label for='returnmessage'>" + doc.getDocLabel("page_executiondetail", "return_message") + "</label>")).append(returnMessageField);
    var indexGroup = $("<div class='form-group'></div>").append($("<label for='sort'>" + doc.getDocLabel("page_executiondetail", "index") + "</label>")).append(indexField);
    var natureGroup = $("<div class='form-group'></div>").append($("<label for='conditionOper'>" + doc.getDocLabel("page_executiondetail", "nature") + "</label>")).append(natureField);
    var lengthGroup = $("<div class='form-group'></div>").append($("<label for='conditionVal1Init'>" + doc.getDocLabel("page_executiondetail", "length") + "</label>")).append(lengthField);
    var databaseGroup = $("<div class='form-group'></div>").append($("<label for='conditionVal1Init'>" + doc.getDocLabel("page_executiondetail", "database") + "</label>")).append(databaseField);
    var rowLimitGroup = $("<div class='form-group'></div>").append($("<label for='conditionVal2Init'>" + doc.getDocLabel("page_executiondetail", "rowLimit") + "</label>")).append(rowLimitField);
    var retryNbGroup = $("<div class='form-group'></div>").append($("<label for='conditionVal1'>" + doc.getDocLabel("page_executiondetail", "retryNb") + "</label>")).append(retryNbField);
    var retryPeriodGroup = $("<div class='form-group'></div>").append($("<label for='conditionVal2'>" + doc.getDocLabel("page_executiondetail", "retryPeriod") + "</label>")).append(retryPeriodField);

    databaseField.val(property.database);
    descField.val(property.description);
    typeField.val(property.type);
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
    row3.append($("<div></div>").addClass("col-sm-2").append(typeGroup));
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
        var stepObj = new Step(step, stepList, i);
        
        $(stepObj).data("id",  {stepId: i, actionId: -1, controlId: -1} );
        
        stepObj.addElements();
        stepObj.updateReturnCode(i);
        stepObj.draw();
        
        stepList.push(stepObj);
        
    }
    if (stepList.length > 0) {
        $("#stepList a:last-child").trigger("click");
    }
    $("#stepList").data("listOfStep", stepList);
}

/** JAVASCRIPT OBJECT **/

function Step(json, stepList, id) {
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
    this.setActionList(json.testCaseStepActionExecutionList, id);

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

Step.prototype.addElements = function () {
    var htmlElement = this.html;
    
    htmlElement.data("item", this);
    htmlElement.click(this.show);
    
    htmlElement.append(this.textArea);
    $("#stepList").append(htmlElement);
    $("#actionContainer").append(this.stepActionContainer);
   
};

//Get the correct return code from added action list
Step.prototype.updateReturnCode = function () {
    var newReturnCode = "OK";
    var everyActionAndControlCheck = true;
    var idCurrentStep =  $(this).data("id");
    
    $(".itemContainer").each(function () {
        var idCurrentActionControl =  $(this).data("id");
        var elementBelongToCurrentStep = (idCurrentStep.stepId == idCurrentActionControl.stepId );
        
        if (elementBelongToCurrentStep){
            
            if ($(this).data("item").returnCode === "NE"){
                everyActionAndControlCheck = false;
            }
            else if ($(this).data("item").returnCode === "KO"){
                newReturnCode = "KO";
            }
            else if ($(this).data("item").returnCode === "FA" && newReturnCode !== "KO"){
                newReturnCode = "FA";
            }
        }
    });
    
    if (!everyActionAndControlCheck && newReturnCode === "OK"  )
        newReturnCode = "NE";
    
    var htmlElement = this.html;
    var object = htmlElement.data("item");
    object.returnCode = newReturnCode;
}

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
        htmlElement.append($("<span>").addClass("glyphicon glyphicon-question-sign pull-left"));
        object.html.addClass("list-group-item-black");
    } else {
        htmlElement.prepend($("<span>").addClass("glyphicon glyphicon-alert pull-left"));
        object.html.addClass("list-group-item-warning");
    }
}



Step.prototype.show = function () {
    var doc = new Doc();
    var object = $(this).data("item");
    var stepDesc = $("<div>").addClass("col-sm-10");
    var stepButton = $("<div id='stepPlus'></a>").addClass("col-sm-1").addClass("paddingLeft0").addClass("paddingTop30").append($("<span class='glyphicon glyphicon-chevron-down'></span>").attr("style", "font-size:1.5em"));

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
    } else if (object.returnCode === "NE") {
        $("#stepInfo").prepend($("<div>").addClass("col-sm-1").append($("<h2>").addClass("glyphicon glyphicon-question-sign pull-left text-black").attr("style", "font-size:3em")));
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
    
    returnMessageWritableForStep( object, $("#stepMessage"));
    
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

Step.prototype.setActionList = function (actionList, idMotherStep) {
    for (var i = 0; i < actionList.length; i++) {
        this.setAction(actionList[i], idMotherStep, i);
    }
};

Step.prototype.setAction = function (action,idMotherStep, idAction) {
    var actionObj;
    if (action instanceof Action) {
        actionObj = action;
    } else {
        actionObj = new Action(action, this);
    }

    this.actionList.push(actionObj);

    actionObj.draw(idMotherStep, idAction);

    actionObj.setControlList(actionObj.controlListJson, idMotherStep, idAction);
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
function returnMessageWritableForStep(object, field){

    field.empty();
    field.data("currentStep", object);
    
    field.prop("readonly", true);
    if (object.returnCode === "NE"){
        field.prop("readonly", false);
        field.change(function() {
            var currentObject = field.data("currentStep");
            currentObject.setReturnMessage( field.val() );
        });
    }
    field.val(object.returnMessage);
}



//Get the json data from the input of the field
Step.prototype.getJsonData = function () {
    var json = {};
    json.conditionOper = this.conditionOper;
    json.conditionVal1 = this.conditionVal1;
    json.conditionVal1Init = this.conditionVal1Init;
    json.conditionVal2 = this.conditionVal2;
    json.conditionVal2Init = this.conditionVal2Init;
    json.description = this.description;
    json.end = this.end;
    json.fullEnd = this.fullEnd;
    json.fullStart = this.fullStart;
    json.id = this.id;
    json.inLibrary = this.inLibrary;
    json.index =  this.index;
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

Action.prototype.draw = function (idMotherStep, id) {
    
    var fullActionElement = $("<div name='fullActionDiv'></div>");
    var htmlElement = this.html;
    var action = this;
    var idCurrentElement = {stepId: idMotherStep, actionId: id, controlId: -1};
    
    var row = $("<div class='itemContainer'></div>").addClass("col-sm-10");
    var type = $("<div></div>").addClass("type");

    var header = this.generateHeader(idCurrentElement);

    row.append(header);
    row.data("item", this);
    //give the action an idid
    row.data("id",idCurrentElement );
    
    var button = $("<div></div>").addClass("marginLeft-15 col-sm-1").append($("<span class='glyphicon glyphicon-chevron-down'></span>").attr("style", "font-size:1.5em"));

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
    } else if (action.returnCode === "NA") {
        htmlElement.prepend($("<div>").addClass("col-sm-1").append($("<span>").addClass("glyphicon glyphicon-alert").attr("style", "font-size:1.5em")));
        htmlElement.addClass("row list-group-item list-group-item-info");
        content.hide();
    } else if (action.returnCode === "NE") {
        htmlElement.prepend($("<div>").addClass("col-sm-1").append($("<span>").addClass("glyphicon glyphicon-question-sign").attr("style", "font-size:1.5em")));
        htmlElement.addClass("row list-group-item list-group-item-black");
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
};

Action.prototype.setControlList = function (controlList, idMotherStep, idMotherAction) {
    for (var i = 0; i < controlList.length; i++) {
        this.setControl(controlList[i],idMotherStep ,idMotherAction, i);
    }
};

Action.prototype.setControl = function (control, idMotherStep, idMotherAction, id) {
    if (control instanceof Control) {
        control.draw(idMotherStep, idMotherAction, id);
        this.controlList.push(control);
    } else {
        var controlObj = new Control(control, this);

        controlObj.draw(idMotherStep, idMotherAction, id);
        this.controlList.push(controlObj);
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
function returnMessageWritable(object, field){
    
    field.empty();
    field.prop("readonly", true);
    if (object.returnCode === "NE"){
        field.prop("readonly", false);
        field.change(function() {
            object.setReturnMessage( field.val() );
        });
    }
}

Action.prototype.generateHeader = function (id) {
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
        elapsedTime.append((convToDate(this.endlong) - convToDate(this.startlong)) + " ms");
    } else {
        elapsedTime.append("...");
    }

    /**
     * If returnCode is NE, display button, else display elapsed time
     */
    if (this.returnCode === "NE") {
        var buttonFA = $($("<button>").addClass("btn btn-warning btn-inverse").attr("type", "button").text("FA"));
        var buttonOK = $($("<button>").addClass("btn btn-success btn-inverse").attr("type", "button").text("OK"));
        buttonOK.click(function (event) {
            event.preventDefault();
            event.stopPropagation();
            triggerActionExecution(this, id, "OK");
            //toggle style of both buttons
//            if (  ($(this).attr("class").indexOf("btn-inverse") !== -1)  !==   ($(buttonFA).attr("class").indexOf("btn-inverse") !== -1) ){
//                $(buttonFA).toggleClass("btn-inverse ");
//            }
//            $(this).toggleClass(" btn-inverse ");
        });
        buttonFA.click(function (event) {
            event.preventDefault();
            event.stopPropagation();
            triggerActionExecution(this, id, "FA");
//            //toggle style of both buttons
//            if (  ($(this).attr("class").indexOf("btn-inverse") !== -1)  !==   ($(buttonOK).attr("class").indexOf("btn-inverse") !== -1) ){
//                $(buttonOK).toggleClass("btn-inverse ");
//            }
//            $(this).toggleClass(" btn-inverse ");
            
        });
        contentField.append($("<div class='col-sm-2'>").addClass("btn-group btn-group-xs").attr("role", "group").append(buttonOK).append(buttonFA));
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
    var newReturnCode ="NE";
    if (status === "OK") {
        currentElement.removeClass(function (index, className) {
            return (className.match(/(^|\s)list-group-item-\S+/g) || []).join(' ');
        }).addClass("row list-group-item list-group-item-success");
        $(currentElement.find("span")[0]).removeClass(function (index, className) {
            return (className.match(/(^|\s)glyphicon-\S+/g) || []).join(' ');
        }).addClass("glyphicon-ok");
        newReturnCode ="OK";
    } else {
        currentElement.removeClass(function (index, className) {
            return (className.match(/(^|\s)list-group-item-\S+/g) || []).join(' ');
        }).addClass("row list-group-item list-group-item-warning");
        $(currentElement.find("span")[0]).removeClass(function (index, className) {
            return (className.match(/(^|\s)glyphicon-\S+/g) || []).join(' ');
        }).addClass("glyphicon-alert");
        newReturnCode ="FA";
    }
    $(currentElement).next("div").find("input[id='returncode']").attr("data-modified", "true");
    $(currentElement).next("div").find("input[id='returnmessage']").val("Action manually executed").change();
    
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
    $(prevElementCurrentStep).next("div").find("input[id='returnmessage']").val("Action manually executed").change();

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
    $(prevElementPreviousStep).next("div").find("input[id='returnmessage']").val("Action manually executed").change();
    //Check Step Status
    updateReturnCode( id, newReturnCode );
    updateStepStatus( id );
    
}

function updateReturnCode(idElementTriggers, newReturnCode){
    //
    $(".itemContainer").each(function () {
        var idCurrentElement =  $(this).data("id");
        //current element
        if ( idCurrentElement === idElementTriggers ){
            $(this).data("item").returnCode = newReturnCode;
            return ;
        }
        //look for the previous elements untouch
        var idName = ["stepId", "actionId", "controlId"];
        if ($(this).data("item").returnCode ==="NE"){
            for(var i =0; i < 3 ; i++){
                if (  idCurrentElement[ idName[i] ] !== idElementTriggers[ idName[i] ]  ){
                    //element before the one clicked
                    if ( ( idCurrentElement[ idName[i] ] === -1) || ( idCurrentElement[ idName[i] ] < idElementTriggers[idName[i] ] ) )  {
                        $(this).data("item").returnCode = "OK";
                    }
                    return ;
                }
            }
        }else
            return;
    });
}

function updateStepStatus(idElementTrigger) {
    
    for (var idStep =idElementTrigger.stepId; idStep >= 0 ; idStep--){// update all the step below the element trigger

        var stepElementTriggerBelongTo = $("#stepList").data("listOfStep")[ idStep ];
        stepElementTriggerBelongTo.updateReturnCode();
        var returnCodeStep = stepElementTriggerBelongTo.html.data("item").returnCode;
        
        var className = "list-group-item-black";
        var glyphiconName = "glyphicon-question-sign";
        if (returnCodeStep ==="OK") {
            className = "list-group-item-success";
            glyphiconName = "glyphicon-ok";
        }
        if (returnCodeStep ==="FA") {
            className = "list-group-item-warning";
            glyphiconName = "glyphicon-alert";
        }
        if (returnCodeStep ==="KO") {
            className = "list-group-item-danger";
            glyphiconName = "glyphicon-remove";
        }
        
        $($("#steps").find("a")[idStep]).removeClass(function (index, className) {
            return (className.match(/(^|\s)list-group-item-\S+/g) || []).join(' ');
        }).addClass(className);
        
        $($("#steps").find("a")[idStep]).find("span").removeClass(function (index, className) {
            return (className.match(/(^|\s)glyphicon-\S+/g) || []).join(' ');
        }).addClass(glyphiconName);
        
        //if is the step focus
        if (idStep === idElementTrigger.stepId){     
            
            $($("#stepInfo h2")[0]).removeClass(function (index, className) {
                return (className.match(/(^|\s)glyphicon-\S+/g) || []).join(' ');
            }).addClass(glyphiconName);
        }
        
    }
    updateExecutionStatus();
}

function updateExecutionStatus() {
    var globalStatus = "OK";
    //update step
    $("#stepList a").each(function (index, element) {
        var stepStatus = $($("#stepList a")[index]).data("item").returnCode;
        if (globalStatus === "OK" && stepStatus === "NE") {
            globalStatus = "NE";
        }
        if (stepStatus === "FA") {
            globalStatus = "FA";
        }
        if (stepStatus === "KO") {
            globalStatus = "KO";
        }
        
    });
    var data = {controlStatus: globalStatus};
    updateLoadBar(data); 
    updateConfigPanel(data);
    updateLoadBar(data); 
}

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
    
    var returnMessageField = $("<textarea style='width:100%;' class='form-control input-sm' id='returnmessage'>");
    returnMessageWritable(this, returnMessageField);
    
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
        timeField.val((convToDate(this.endlong) - convToDate(this.startlong)) + " ms");
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
    
    json.action = this.action;
    json.conditionOper = this.conditionOper;
    json.conditionVal1 = this.conditionVal1;
    json.conditionVal1Init = this.conditionVal1Init;
    json.conditionVal2 = this.conditionVal2;
    json.conditionVal2Init = this.conditionVal2Init;
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
        this.controlSequence = 1;
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

Control.prototype.draw = function (idMotherStep, idMotherAction, idControl) {
    var htmlElement = this.html;
    var row = $("<div class='itemContainer'></div>").addClass("col-sm-10");
    var type = $("<div></div>").addClass("type");
    var currentControlId = {stepId: idMotherStep, actionId: idMotherAction, controlId: idControl};
    
    var header = this.generateHeader(currentControlId);
    row.append(header);
    row.data("item", this);
    row.data("id", currentControlId );
    //set the control Sequence
    this.controlSequence = idControl+1;//start at 1
    
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
    } else if (this.returnCode === "NA") {
        htmlElement.prepend($("<div>").addClass("col-sm-1").append($("<span>").addClass("glyphicon glyphicon-alert").attr("style", "font-size:1.5em")));
        htmlElement.addClass("row list-group-item list-group-item-info");
        content.hide();
    } else if (this.returnCode === "NE") {
        htmlElement.prepend($("<div>").addClass("marginLeft-15 col-sm-1").append($("<span>").addClass("glyphicon glyphicon-question-sign").attr("style", "font-size:1.5em")));
        htmlElement.addClass("row list-group-item list-group-item-black");
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
    var contentField = $("<div></div>").addClass("col-sm-12").attr("id", "contentField");
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

    if (this.returnCode === "NE") {
        var buttonFA = $($("<button>").addClass("btn btn-danger btn-inverse").attr("type", "button").text("KO"));
        var buttonOK = $($("<button>").addClass("btn btn-success btn-inverse").attr("type", "button").text("OK"));
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
        contentField.append($("<div class='col-sm-2'>").addClass("btn-group btn-group-xs").attr("role", "group").append(buttonOK).append(buttonFA));
        showSaveTestCaseExecutionButton();
    } else {
        contentField.append($("<div class='col-sm-2'>").append(elapsedTime));
        
    }

    contentField.append($("<div class='col-sm-10'>").append(descriptionField).append(returnMessageField));

    firstRow.append(contentField);

    content.append(firstRow);

    return content;
};

function triggerControlExecution(element, id, status) {
    var currentElement = $($(element).closest(".control")[0]);
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
    //Check Step Status
    
    updateReturnCode(id, status);
    updateStepStatus(id);
}

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
    
    var returnMessageField = $("<textarea style='width:100%;' class='form-control' id='returnmessage'>");
    returnMessageWritable(this, returnMessageField);
    
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
        timeField.val((convToDate(this.endlong) - convToDate(this.startlong)) + " ms");
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
    
    json.conditionOper = this.conditionOper;
    json.conditionVal1 = this.conditionVal1;
    json.conditionVal1Init = this.conditionVal1Init;
    json.conditionVal2 = this.conditionVal2;
    json.conditionVal2Init = this.conditionVal2Init;
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
    json.type = this.type;
    json.controlProperty = this.value1;
    json.controlValue = this.value2;
    //Value the user is able to modified
    json.returnMessage =this.returnMessage;
    
    return json;
};

// Function in order to add the Media files links into TestCase, step, action and control level.
function addFileLink(fileList, container) {
    $(container).find($("div[name='mediaMiniature']")).remove();
    for (var i = 0; i < fileList.length; i++) {
        if ((fileList[i].fileType === "JPG") || (fileList[i].fileType === "PNG")) {
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
                        .append(fileList[i].fileDesc).prepend("<br>").prepend($("<img>").attr("src", "images/f-" + filetypetxt + ".svg")
                        .css("height", "30px").click(function (f) {
                    showTextArea(fileList[0].fileDesc, "", "ReadTestCaseExecutionMedia?filename=" + fileList[0].fileName + "&filetype=" + fileList[0].fileType + "&filedesc=" + fileList[0].fileDesc);
                    return false;
                }));
            } else if (i === 1) {
                var linkBoxtxt = $("<div name='mediaMiniature'>").addClass("col-sm-1").css("padding", "0px 7px 0px 7px")
                        .append(fileList[i].fileDesc).prepend("<br>").prepend($("<img>").attr("src", "images/f-" + filetypetxt + ".svg")
                        .css("height", "30px").click(function (f) {
                    showTextArea(fileList[1].fileDesc, "", "ReadTestCaseExecutionMedia?filename=" + fileList[1].fileName + "&filetype=" + fileList[1].fileType + "&filedesc=" + fileList[1].fileDesc);
                    return false;
                }));
            } else if (i === 2) {
                var linkBoxtxt = $("<div name='mediaMiniature'>").addClass("col-sm-1").css("padding", "0px 7px 0px 7px")
                        .append(fileList[i].fileDesc).prepend("<br>").prepend($("<img>").attr("src", "images/f-" + filetypetxt + ".svg")
                        .css("height", "30px").click(function (f) {
                    showTextArea(fileList[2].fileDesc, "", "ReadTestCaseExecutionMedia?filename=" + fileList[2].fileName + "&filetype=" + fileList[2].fileType + "&filedesc=" + fileList[2].fileDesc);
                    return false;
                }));
            } else if (i === 3) {
                var linkBoxtxt = $("<div name='mediaMiniature'>").addClass("col-sm-1").css("padding", "0px 7px 0px 7px")
                        .append(fileList[i].fileDesc).prepend("<br>").prepend($("<img>").attr("src", "images/f-" + filetypetxt + ".svg")
                        .css("height", "30px").click(function (f) {
                    showTextArea(fileList[3].fileDesc, "", "ReadTestCaseExecutionMedia?filename=" + fileList[3].fileName + "&filetype=" + fileList[3].fileType + "&filedesc=" + fileList[3].fileDesc);
                    return false;
                }));
            }
            container.append(linkBoxtxt);
        }
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
                stepArray: getScriptInformationOfStep()
            }),
            success: function () {
                
                /*var stepHtml = $("#stepList li.active");
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
 * @param {type} stepListData
 * @returns {Array}
 */

function getScriptInformationOfStep() {
    var stepList = $("#stepList a");
    var stepArr = [];

    // Construct the step/action/control list:
    // Iterate over steps
    for (var i = 0; i < stepList.length; i++) {
        var step = $(stepList[i]).data("item");
        var actionArr = [];
        // Get step's actions
        var actionList = step.stepActionContainer.find("[name='fullActionDiv']");
        
        // Iterate over actions
        for (var j = 0; j < actionList.length; j++) {

            var controlArr = [];
            var action = $(actionList[j]).find("div.itemContainer").data("item");

            // Get action's controls
            var controlList =$(actionList[j]).find("a.control");
            
            // Iterate over controls
            for (var k = 0; k < controlList.length; k++) {
                var control = $(controlList[k]).find("div.itemContainer").data("item");
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
