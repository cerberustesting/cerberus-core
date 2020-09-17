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

//var HOST_PASSWORD_DEFAULT = "********";
//var hostUserBeforeUpdate = null;
var availableHost = [];

/***
 * Open the modal with robot information.
 * @param {String} robot - name of the robot (ex : "MyRobot")
 * @param {String} mode - mode to open the modal. Can take the values : ADD, DUPLICATE, EDIT
 * @returns {null}
 */
function openModalRobot(robot, mode) {

    // We only load the Labels and bind the events once for performance optimisations.
    if ($('#editRobotModal').data("initLabel") === undefined) {
        initModalRobot();
        $('#editRobotModal').data("initLabel", true);
    }

    // Init the Saved data to false.
    $('#editRobotModal').data("Saved", false);
    $('#editRobotModal').data("robot", undefined);

    if (mode === "EDIT") {
        editRobotClick(robot);
    } else if (mode === "ADD") {
        addRobotClick(robot);
    } else {
        // DUPLICATE
        duplicateRobotClick(robot);
    }
}

function initModalRobot() {

    var doc = new Doc();
    $("#editRobotModal [name='buttonClose']").html(doc.getDocLabel("page_global", "buttonClose"));
    $("#editRobotModal [name='buttonAdd']").html(doc.getDocLabel("page_global", "btn_add"));
    $("#editRobotModal [name='buttonDuplicate']").html(doc.getDocLabel("page_global", "btn_duplicate"));
    $("#editRobotModal [name='buttonEdit']").html(doc.getDocLabel("page_global", "buttonEdit"));

    $("#editRobotModal [name='addEntryField']").html(doc.getDocLabel("page_robot", "button_create"));
    $("#editRobotModal [name='confirmationField']").html(doc.getDocLabel("page_robot", "button_delete"));
    $("#editRobotModal [name='editEntryField']").html(doc.getDocLabel("page_robot", "button_edit"));
    $("#editRobotModal [name='robotField']").html(doc.getDocOnline("robot", "robot"));
    $("#editRobotModal [name='platformField']").html(doc.getDocOnline("robot", "platform"));
    $("#editRobotModal [name='browserField']").html(doc.getDocOnline("robot", "browser"));
    $("#editRobotModal [name='versionField']").html(doc.getDocOnline("robot", "version"));
    $("#editRobotModal [name='activeField']").html(doc.getDocOnline("robot", "active"));
    $("#editRobotModal [name='useragentField']").html(doc.getDocOnline("robot", "useragent"));
    $("#editRobotModal [name='screensizeField']").html(doc.getDocOnline("robot", "screensize"));
    $("#editRobotModal [name='descriptionField']").html(doc.getDocOnline("robot", "description"));
    $("#editRobotModal [name='addCapabilityHeader']").html(doc.getDocOnline("robot", "capabilityCapability"));
    $("#editRobotModal [name='addValueHeader']").html(doc.getDocOnline("robot", "capabilityValue"));
    $("#editRobotModal [name='editCapabilityHeader']").html(doc.getDocOnline("robot", "capabilityCapability"));
    $("#editRobotModal [name='editValueHeader']").html(doc.getDocOnline("robot", "capabilityValue"));
    $("#editRobotModal [name='robotdecliField']").html(doc.getDocOnline("robot", "robotdecli"));
    $("#editRobotModal [name='lbexemethodField']").html(doc.getDocOnline("robot", "lbexemethod"));
    $("#editRobotModal [name='typeField']").html(doc.getDocOnline("robot", "type"));

    displayInvariantList("robotActive", "ROBOTACTIVE", false);
    displayInvariantList("robotBrowser", "BROWSER", false, undefined, "");
    displayInvariantList("robotPlatform", "PLATFORM", false, undefined, "");
    displayInvariantList("type", "APPLITYPE", false, undefined, "");
    displayInvariantList("lbexemethod", "ROBOTLBMETHOD", false);

    var availableUserAgent = getInvariantArray("USERAGENT", false);
    $("#editRobotModal [name='useragent']").autocomplete({
        source: availableUserAgent
    });
    var availableScreenSize = getInvariantArray("SCREENSIZE", false);
    $("#editRobotModal [name='screensize']").autocomplete({
        source: availableScreenSize
    });
    // Load invariant into cache.
    getInvariantArray("ROBOTHOST", false);
    getInvariantArray("EXECUTOREXTENSIONHOST", false);

    // Load the select needed in localStorage cache.
    getSelectInvariant("CAPABILITY", true);
    // Adding rows in modals.
    $("#addEditCapability").click(addNewCapabilityRow.bind(null, "editCapabilitiesTableBody"));
    $("#addEditExecutor").click(addNewExecutorRow.bind(null, "editExecutorsTableBody"));


    $("#editRobotButton").off("click");
    $("#editRobotButton").click(function () {
        confirmRobotModalHandler("EDIT");
    });
    $("#addRobotButton").off("click");
    $("#addRobotButton").click(function () {
        confirmRobotModalHandler("ADD");
    });
    $("#duplicateRobotButton").off("click");
    $("#duplicateRobotButton").click(function () {
        confirmRobotModalHandler("DUPLICATE");
    });

}

/***
 * Open the modal with queue information.
 * @param {String} robot - robot selected
 * @returns {null}
 */
function editRobotClick(robot) {

    clearResponseMessage($('#editRobotModal'));

    // When editing the execution queue, we can modify, modify and run or cancel.
    $('#editRobotButton').attr('class', 'btn btn-primary');
    $('#editRobotButton').removeProp('hidden');

    // We cannot duplicate.
    $('#duplicateRobotButton').attr('class', '');
    $('#duplicateRobotButton').attr('hidden', 'hidden');
    $('#addRobotButton').attr('class', '');
    $('#addRobotButton').attr('hidden', 'hidden');

    $('#editRobotModalForm select[name="idname"]').off("change");
    $('#editRobotModalForm select[name="idname"]').change(function () {
    });
    $('#editRobotModalForm input[name="value"]').off("change");
    $('#editRobotModalForm input[name="value"]').change(function () {
        // Compare with original value in order to display the warning message.
    });

    feedRobotModal(robot, "editRobotModal", "EDIT");
}

/***
 * Open the modal with queue information.
 * @param {String} robot - name of the robot to duplicate.
 * @returns {null}
 */
function duplicateRobotClick(robot) {

    clearResponseMessage($('#editExecutionQueueModal'));

    $('#editRobotButton').attr('class', '');
    $('#editRobotButton').attr('hidden', 'hidden');
    $('#duplicateRobotButton').attr('class', 'btn btn-primary');
    $('#duplicateRobotButton').removeProp('hidden');
    $('#addRobotButton').attr('class', '');
    $('#addRobotButton').attr('hidden', 'hidden');

    $('#editRobotModalForm select[name="idname"]').off("change");
    $('#editRobotModalForm input[name="value"]').off("change");

    feedRobotModal(robot, "editRobotModal", "DUPLICATE");
}

/***
 * Open the modal with queue information.
 * @param {String} robot - idname of the invariant to duplicate.
 * @returns {null}
 */
function addRobotClick(robot) {

    clearResponseMessage($('#editExecutionQueueModal'));

    $('#editRobotButton').attr('class', '');
    $('#editRobotButton').attr('hidden', 'hidden');
    $('#addRobotButton').attr('class', 'btn btn-primary');
    $('#addRobotButton').removeProp('hidden');
    $('#duplicateRobotButton').attr('class', '');
    $('#duplicateRobotButton').attr('hidden', 'hidden');

    $('#editRobotModalForm select[name="idname"]').off("change");
    $('#editRobotModalForm input[name="value"]').off("change");

    feedRobotModal(robot, "editRobotModal", "ADD");
}


/***
 * Function that support the modal confirmation. Will call servlet to comit the transaction.
 * @param {String} mode - either ADD, EDIT or DUPLICATE in order to define the purpose of the modal.
 * @returns {null}
 */
function confirmRobotModalHandler(mode) {
    clearResponseMessage($('#editRobotModal'));

    var formEdit = $('#editRobotModal #editRobotModalForm');

    // Calculate servlet name to call.
    var myServlet = "UpdateRobot";
    if ((mode === "ADD") || (mode === "DUPLICATE")) {
        myServlet = "CreateRobot";
    }

    var formEdit = $('#editRobotModal #editRobotModalForm');

    // Getting Data from Capabilities TAB
    var capabilityTable = $("#editCapabilitiesTableBody tr");
    var capabilities = [];
    for (var i = 0; i < capabilityTable.length; i++) {
        var capability = $(capabilityTable[i]).data("capability");
        if (!capability.toDelete) {
            capabilities.push(capability);
        }
    }

    // Getting Data from Executor TAB
    var executorTable = $("#editExecutorsTableBody tr");
    var executors = [];
    for (var i = 0; i < executorTable.length; i++) {
        var executor = $(executorTable[i]).data("executor");
        if (!executor.toDelete) {
            executors.push(executor);
        }
    }

    // Get the header data from the form.
    var data = convertSerialToJSONObject(formEdit.serialize());
    data.capabilities = JSON.stringify(capabilities);
    data.executors = JSON.stringify(executors);

    var tcElement = formEdit.find("#robotName");
    if (isEmpty(data.robot)) {
        tcElement.parents("div.form-group").addClass("has-error");
        var localMessage = new Message("danger", "Please specify an robot name !");
        showMessage(localMessage, $('#editRobotModal'));
        return;
    } else {
        tcElement.parents("div.form-group").removeClass("has-error");
    }


    // we send to the server
//    if (data.hostUsername !== hostUserBeforeUpdate || data.hostPassword !== HOST_PASSWORD_DEFAULT) {
//        data.hostUsernameToSend = data.hostUsername;
//
//        if (data.hostPassword === HOST_PASSWORD_DEFAULT) {
//            $("#hostPassword").parent().addClass("has-error");
//            var localMessage = new Message("danger", "Please specify the new host password !");
//            showMessage(localMessage, $('#editRobotModal'));
//            return;
//        } else {
//            $("#hostPassword").parent().removeClass("has-error");
//            data.hostPasswordToSend = data.hostPassword;
//        }
//    }

    showLoaderInModal('#editRobotModal');


    $.ajax({
        url: myServlet,
        async: true,
        method: "POST",
        data: {robot: data.robot,
            robotid: data.robotid,
            active: data.robotActive,
            host: data.host,
            port: data.port,
            hostUsername: data.hostUsernameToSend,
            hostPassword: data.hostPasswordToSend,
            platform: data.robotPlatform,
            browser: data.robotBrowser,
            version: data.version,
            useragent: data.useragent,
            type: data.type,
            screensize: data.screensize,
            robotDecli: data.robotdecli,
            description: data.description,
            lbexemethod: data.lbexemethod,
            capabilities: data.capabilities,
            executors: data.executors
        },
        success: function (dataMessage) {
//            data = JSON.parse(data);
            hideLoaderInModal('#editRobotModal');
            if (getAlertType(dataMessage.messageType) === "success") {
                var oTable = $("#robotsTable").dataTable();
                oTable.fnDraw(false);
                $('#editRobotModal').data("Saved", true);
                $('#editRobotModal').data("robot", data);
                $('#editRobotModal').modal('hide');
                showMessage(dataMessage);
            } else {
                showMessage(dataMessage, $('#editRobotModal'));
            }
        },
        error: showUnexpectedError
    });

}

/***
 * Feed the Robot modal with all the data.
 * @param {String} robot - name of the robot to load
 * @param {String} modalId - modal id to feed.
 * @param {String} mode - either ADD, EDIT or DUPLICATE in order to define the purpose of the modal.
 * @returns {null}
 */
function feedRobotModal(robot, modalId, mode) {
    clearResponseMessageMainPage();

    var formEdit = $('#' + modalId);

    if (mode === "DUPLICATE" || mode === "EDIT") {
        $.ajax({
            url: "ReadRobot?withCapabilities=true&withExecutors=true",
            async: true,
            method: "POST",
            data: {
                robot: robot
            },
            success: function (data) {
                if (data.messageType === "OK") {

                    // Feed the data to the screen and manage authorities.
                    var robotObj = data.contentTable;
                    var hasPermissions = data.contentTable.hasPermissionsUpdate;

                    feedRobotModalData(robotObj, modalId, mode, hasPermissions);

                    formEdit.modal('show');
                } else {
                    showUnexpectedError();
                }
            },
            error: showUnexpectedError
        });

    } else {
        var robotObj1 = {};
        robotObj1.robot = "";
        robotObj1.active = "Y";
        robotObj1.host = "";
        robotObj1.port = "";
        robotObj1.platform = "";
        robotObj1.browser = "";
        robotObj1.version = "";
        robotObj1.userAgent = "";
        robotObj1.screenSize = "";
        robotObj1.robotDecli = "";
        robotObj1.description = "";
        robotObj1.lbexemethod = "BYRANKING";
        robotObj1.type = ""
        var hasPermissions = true;
        feedRobotModalData(robotObj1, modalId, mode, hasPermissions);

        formEdit.modal('show');

    }

}

/***
 * Feed the TestCase modal with all the data from the TestCase.
 * @param {String} robot - robot object to be loaded.
 * @param {String} modalId - id of the modal form where to feed the data.
 * @param {String} mode - either ADD, EDIT or DUPLICATE in order to define the purpose of the modal.
 * @param {String} hasPermissionsUpdate - boolean if premition is granted.
 * @returns {null}
 */
function feedRobotModalData(robot, modalId, mode, hasPermissionsUpdate) {
    var formEdit = $('#' + modalId);
    var doc = new Doc();
    var isEditable = (((hasPermissionsUpdate) && (mode === "EDIT"))
            || (mode === "DUPLICATE") || (mode === "ADD"));

    // Data Feed.
    if (mode === "EDIT") {
        $("[name='editRobotField']").html(doc.getDocOnline("page_global", "btn_edit"));
    } else if (mode === "ADD") { // DUPLICATE or ADD
        $("[name='editRobotField']").html(doc.getDocOnline("page_global", "btn_add"));
    } else if (mode === "DUPLICATE") { // DUPLICATE or ADD
        $("[name='editRobotField']").html(doc.getDocOnline("page_global", "btn_duplicate"));
    }

    if (isEmpty(robot)) {
        formEdit.find("#robotid").prop("value", "");
        formEdit.find("#robotName").prop("value", "");
        formEdit.find("#active").val("Y");
        formEdit.find("#platform").val("");
        formEdit.find("#browser").val("");
        formEdit.find("#version").prop("value", "");
        formEdit.find("#useragent").prop("value", "");
        formEdit.find("#screensize").prop("value", "");
        formEdit.find("#robotdecli").prop("value", "");
        formEdit.find("#Description").prop("value", "");
        formEdit.find("#type").val("");
        formEdit.find("#lbexemethod").val("ROUNDROBIN");
    } else {
        if (mode === "EDIT") {
            formEdit.find("#robotid").prop("value", robot.robotID);
        } else {
            formEdit.find("#robotid").prop("value", "");
        }
        formEdit.find("#robotName").prop("value", robot.robot);
        formEdit.find("#active").val(robot.active);
        formEdit.find("#platform").val(robot.platform);
        formEdit.find("#browser").val(robot.browser);
        formEdit.find("#version").prop("value", robot.version);
        formEdit.find("#useragent").prop("value", robot.userAgent);
        formEdit.find("#screensize").prop("value", robot.screenSize);
        formEdit.find("#robotdecli").prop("value", robot.robotDecli);
        formEdit.find("#Description").prop("value", robot.description);
        formEdit.find("#type").val(robot.type);
        formEdit.find("#lbexemethod").val(robot.lbexemethod);
        loadCapabilitiesTable("editCapabilitiesTableBody", robot.capabilities);
        loadExecutorsTable("editExecutorsTableBody", robot.executors);
    }

    // Authorities
//    if (mode === "EDIT") {
//    } else {
//    }
    formEdit.find("#robotid").prop("readonly", true);

    //We desactivate or activate the access to the fields depending on if user has the credentials to edit.
    if (isEditable) { // If readonly, we readonly all fields
        formEdit.find("#robotName").prop("readonly", false);
        formEdit.find("#active").removeAttr("disabled");
        formEdit.find("#host").prop("readonly", false);
        formEdit.find("#port").prop("readonly", false);
        formEdit.find("#platform").removeAttr("disabled");
        formEdit.find("#browser").removeAttr("disabled");
        formEdit.find("#lbexemethod").removeAttr("disabled");
        formEdit.find("#version").prop("readonly", false);
        formEdit.find("#useragent").prop("readonly", false);
        formEdit.find("#screensize").prop("readonly", false);
        formEdit.find("#robotdecli").prop("readonly", false);
        formEdit.find("#Description").prop("readonly", false);
        formEdit.find("#hostPassword").prop("readonly", false);
        formEdit.find("#hostUsername").prop("readonly", false);
        formEdit.find("#type").prop("readonly", false);
    } else {
        formEdit.find("#robotName").prop("readonly", "readonly");
        formEdit.find("#active").prop("disabled", "disabled");
        formEdit.find("#host").prop("readonly", "readonly");
        formEdit.find("#port").prop("readonly", "readonly");
        formEdit.find("#platform").prop("disabled", "disabled");
        formEdit.find("#browser").prop("disabled", "disabled");
        formEdit.find("#lbexemethod").prop("disabled", "disabled");
        formEdit.find("#version").prop("readonly", "readonly");
        formEdit.find("#useragent").prop("readonly", "readonly");
        formEdit.find("#screensize").prop("readonly", "readonly");
        formEdit.find("#robotdecli").prop("readonly", "readonly");
        formEdit.find("#Description").prop("readonly", "readonly");
        formEdit.find("#hostPassword").prop("readonly", "readonly");
        formEdit.find("#hostUsername").prop("readonly", "readonly");
        formEdit.find("#type").prop("readonly", "readonly");
    }
}

function loadCapabilitiesTable(tableBody, capabilities) {
    $('#' + tableBody + ' tr').remove();
    $.each(capabilities, function (idx, capability) {
        capability.toDelete = false;
        appendCapabilityRow(tableBody, capability);
    });
}

function loadExecutorsTable(tableBody, executors) {
    $('#' + tableBody + ' tr').remove();
    $.each(executors, function (idx, executor) {
        executor.toDelete = false;
        appendExecutorRow(tableBody, executor);
    });
}

function appendCapabilityRow(tableBody, capability) {
    var doc = new Doc();
    var deleteBtn = $("<button type=\"button\"></button>").addClass("btn btn-default btn-xs").append($("<span></span>").addClass("glyphicon glyphicon-trash"));
    var selectCapability = getSelectInvariant("CAPABILITY", false, true);
    var valueInput = $("<input  maxlength=\"150\" placeholder=\"-- " + doc.getDocLabel("robot", "capabilityValue") + " --\">").addClass("form-control input-sm").val(capability.value);
    var table = $("#" + tableBody);

    var row = $("<tr></tr>");
    var deleteBtnRow = $("<td></td>").append(deleteBtn);
    var cap = $("<td></td>").append(selectCapability.val(capability.capability));
    var value = $("<td></td>").append(valueInput);
    deleteBtn.click(function () {
        capability.toDelete = (capability.toDelete) ? false : true;
        if (capability.toDelete) {
            row.addClass("danger");
        } else {
            row.removeClass("danger");
        }
    });
    selectCapability.change(function () {
        capability.capability = $(this).val();
    });
    valueInput.change(function () {
        capability.value = $(this).val();
    });
    row.append(deleteBtnRow);
    row.append(cap);
    row.append(value);
    capability.capability = selectCapability.prop("value"); // Value that has been requested by dtb parameter may not exist in combo values so we take the real selected value.
    row.data("capability", capability);
    table.append(row);
}

function appendExecutorRow(tableBody, executor) {
    var doc = new Doc();
    var deleteBtn = $("<button type=\"button\"></button>").addClass("btn btn-default btn-xs").append($("<span></span>").addClass("glyphicon glyphicon-trash"));
    var selectActive = getSelectInvariant("ROBOTEXECUTORACTIVE", false);
    var nameInput = $("<input  maxlength=\"150\" placeholder=\"-- " + doc.getDocLabel("robotexecutor", "executor") + " --\">").addClass("form-control input-sm").val(executor.executor);
    var rankInput = $("<input  placeholder=\"-- " + doc.getDocLabel("robotexecutor", "rank") + " --\">").addClass("form-control input-sm").val(executor.rank);
    var hostInput = $("<input  placeholder=\"-- " + doc.getDocLabel("robotexecutor", "host") + " --\">").addClass("form-control input-sm").val(executor.host);
    var portInput = $("<input  placeholder=\"-- " + doc.getDocLabel("robotexecutor", "Port") + " --\">").addClass("form-control input-sm").val(executor.port);
    var hostUserInput = $("<input  placeholder=\"-- " + doc.getDocLabel("robotexecutor", "host_user") + " --\">").addClass("form-control input-sm").val(executor.hostUser);
    var hostPasswordInput = $("<input  placeholder=\"-- " + doc.getDocLabel("robotexecutor", "host_password") + " --\">").addClass("form-control input-sm").val(executor.hostPassword);
    var deviceUdidInput = $("<input  placeholder=\"-- " + doc.getDocLabel("robotexecutor", "deviceUdid") + " --\">").addClass("form-control input-sm").val(executor.deviceUdid);
    var deviceNameInput = $("<input  placeholder=\"-- " + doc.getDocLabel("robotexecutor", "deviceName") + " --\">").addClass("form-control input-sm").val(executor.deviceName);
    var devicePortInput = $("<input  placeholder=\"-- " + doc.getDocLabel("robotexecutor", "devicePort") + " --\">").addClass("form-control input-sm").val(executor.devicePort);
    var deviceLockUnlockInput = $("<input type='checkbox' placeholder=\"-- " + doc.getDocLabel("robotexecutor", "deviceLockUnlock") + " --\">").addClass("form-control input-sm").prop("checked", executor.deviceLockUnlock);
    var executorExtensionHostInput = $("<input  placeholder=\"-- " + doc.getDocLabel("robotexecutor", "executorExtensionHost") + " --\">").addClass("form-control input-sm").val(executor.executorExtensionHost);
    var executorExtensionPortInput = $("<input  placeholder=\"-- " + doc.getDocLabel("robotexecutor", "executorExtensionPort") + " --\">").addClass("form-control input-sm").val(executor.executorExtensionPort);
    var executorProxyHostInput = $("<input  placeholder=\"-- " + doc.getDocLabel("robotexecutor", "executorProxyHost") + " --\">").addClass("form-control input-sm").val(executor.executorProxyHost);
    var executorProxyPortInput = $("<input  placeholder=\"-- " + doc.getDocLabel("robotexecutor", "executorProxyPort") + " --\">").addClass("form-control input-sm").val(executor.executorProxyPort);
    var executorProxyActiveInput = $("<input type='checkbox' placeholder=\"-- " + doc.getDocLabel("robotexecutor", "executorProxyActive") + " --\">").addClass("form-control input-sm").prop("checked", executor.executorProxyActive);
    var table = $("#" + tableBody);



    var row = $("<tr></tr>");

    var td1 = $("<td></td>").append(deleteBtn);

    var name = $("<div class='form-group col-sm-12'></div>").append("<label for='name'>" + doc.getDocOnline("robotexecutor", "executor") + "</label>").append(nameInput);
    var drow01 = $("<div class='row'></div>").append(name);
    var td2 = $("<td></td>").append(drow01);

    var active = $("<div class='form-group col-sm-6'></div>").append("<label for='active'>" + doc.getDocOnline("robotexecutor", "active") + "</label>").append(selectActive.val(executor.active));
    var rank = $("<div class='form-group col-sm-6'></div>").append("<label for='rank'>" + doc.getDocOnline("robotexecutor", "rank") + "</label>").append(rankInput);
    var host = $("<div class='form-group col-sm-6'></div>").append("<label for='host'>" + doc.getDocOnline("robotexecutor", "host") + "</label>").append(hostInput);
    var port = $("<div class='form-group col-sm-6'></div>").append("<label for='port'>" + doc.getDocOnline("robotexecutor", "Port") + "</label>").append(portInput);
    var hostuser = $("<div class='form-group col-sm-6'></div>").append("<label for='hostuser'>" + doc.getDocOnline("robotexecutor", "host_user") + "</label>").append(hostUserInput);
    var hostpass = $("<div class='form-group col-sm-6'></div>").append("<label for='hostpassword'>" + doc.getDocOnline("robotexecutor", "host_password") + "</label>").append(hostPasswordInput);
    var dudid = $("<div class='form-group col-sm-4'></div>").append("<label for='deviceudid'>" + doc.getDocOnline("robotexecutor", "deviceUdid") + "</label>").append(deviceUdidInput);
    var dname = $("<div class='form-group col-sm-4'></div>").append("<label for='devicename'>" + doc.getDocOnline("robotexecutor", "deviceName") + "</label>").append(deviceNameInput);
    var dport = $("<div class='form-group col-sm-2'></div>").append("<label for='deviceport'>" + doc.getDocOnline("robotexecutor", "devicePort") + "</label>").append(devicePortInput);
    var dLockUnlock = $("<div class='form-group col-sm-2'></div>").append("<label for='devicelockunlockinput'>" + doc.getDocOnline("robotexecutor", "deviceLockUnlock") + "</label>").append(deviceLockUnlockInput);
    var epActive = $("<div class='form-group col-sm-2'></div>").append("<label for='executorproxyactive'>" + doc.getDocOnline("robotexecutor", "executorProxyActive") + "</label>").append(executorProxyActiveInput);
    var eehost = $("<div class='form-group col-sm-3'></div>").append("<label for='executorextensionhost'>" + doc.getDocOnline("robotexecutor", "executorExtensionHost") + "</label>").append(executorExtensionHostInput);
    var eeport = $("<div class='form-group col-sm-2'></div>").append("<label for='executorextensionport'>" + doc.getDocOnline("robotexecutor", "executorExtensionPort") + "</label>").append(executorExtensionPortInput);
    var ephost = $("<div class='form-group col-sm-3'></div>").append("<label for='executorproxyhost'>" + doc.getDocOnline("robotexecutor", "executorProxyHost") + "</label>").append(executorProxyHostInput);
    var epport = $("<div class='form-group col-sm-2'></div>").append("<label for='executorproxyport'>" + doc.getDocOnline("robotexecutor", "executorProxyPort") + "</label>").append(executorProxyPortInput);
    var drow1 = $("<div class='row'></div>").append(active).append(rank);
    var drow2 = $("<div class='row'></div>").append(host).append(port);
    var drow3 = $("<div class='row'></div>").append(hostuser).append(hostpass);
    var drow4 = $("<div class='row'></div>").append(dudid).append(dname).append(dport).append(dLockUnlock);
    var drow5 = $("<div class='row'></div>").append(epActive).append(eehost).append(eeport).append(ephost).append(epport);
    var td3 = $("<td></td>").append(drow1).append(drow2).append(drow3).append(drow4).append(drow5);

    deleteBtn.click(function () {
        executor.toDelete = (executor.toDelete) ? false : true;
        if (executor.toDelete) {
            row.addClass("danger");
        } else {
            row.removeClass("danger");
        }
    });
    selectActive.change(function () {
        executor.active = $(this).val();
    });
    nameInput.change(function () {
        executor.executor = $(this).val();
    });
    rankInput.change(function () {
        executor.rank = $(this).val();
    });
    hostInput.change(function () {
        executor.host = $(this).val();
    });
    portInput.change(function () {
        executor.port = $(this).val();
    });
    hostUserInput.change(function () {
        executor.hostUser = $(this).val();
    });
    hostPasswordInput.change(function () {
        executor.hostPassword = $(this).val();
    });
    deviceNameInput.change(function () {
        executor.deviceName = $(this).val();
    });
    deviceUdidInput.change(function () {
        executor.deviceUdid = $(this).val();
    });
    devicePortInput.change(function () {
        executor.devicePort = $(this).val();
    });
    deviceLockUnlockInput.change(function () {
        executor.deviceLockUnlock = $(this).prop("checked");
    });
    executorExtensionHostInput.change(function () {
        executor.executorExtensionHost = $(this).val();
    });
    executorExtensionPortInput.change(function () {
        executor.executorExtensionPort = $(this).val();
    });
    executorProxyHostInput.change(function () {
        executor.executorProxyHost = $(this).val();
    });
    executorProxyPortInput.change(function () {
        executor.executorProxyPort = $(this).val();
    });
    executorProxyActiveInput.change(function () {
        executor.executorProxyActive = $(this).prop("checked");
    });
    hostInput.autocomplete({
        source: getInvariantArray("ROBOTHOST", false)
    });
    executorExtensionHostInput.autocomplete({
        source: getInvariantArray("EXECUTOREXTENSIONHOST", false)
    });

    row.append(td1);
    row.append(td2);
    row.append(td3);
//    executor.active = selectActive.prop("value"); // Value that has been requested by dtb parameter may not exist in combo values so we take the real selected value.
    row.data("executor", executor);
    table.append(row);
}


function addNewCapabilityRow(tableBody) {
    var newCapability = {
        toDelete: false,
        id: 0,
        capability: "",
        value: ""
    };
    appendCapabilityRow(tableBody, newCapability);
}

function addNewExecutorRow(tableBody) {
    var nbExecutorTable = $("#editExecutorsTableBody tr").length;
    nbExecutorTable++;
    var newExecutor = {
        toDelete: false,
        ID: 0,
        executor: "EXE-" + nbExecutorTable,
        active: "Y",
        rank: nbExecutorTable,
        host: "",
        port: "",
        hostUser: "",
        hostPassword: "",
        deviceUdid: "",
        deviceName: "",
        deviceLockUnlock: false,
        description: "",
        executorExtensionHost: "",
        executorExtensionPort: "",
        executorProxyHost: "",
        executorProxyPort: "",
        executorProxyActive: false
    };
    appendExecutorRow(tableBody, newExecutor);
}
