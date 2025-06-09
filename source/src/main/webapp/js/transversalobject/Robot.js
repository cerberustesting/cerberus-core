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

//var HOST_PASSWORD_DEFAULT = "********";
//var hostUserBeforeUpdate = null;
var availableHost = [];
var nbRow = 0;
var originalRobotName = null;

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
    $("#editRobotModal [name='isAcceptInsecureCertsField']").html(doc.getDocOnline("robot", "IsAcceptInsecureCerts"));
    $("#editRobotModal [name='extraParamField']").html(doc.getDocOnline("robot", "ExtraParam"));
    $("#editRobotModal [name='acceptNotificationsField']").html(doc.getDocOnline("robot", "AcceptNotifications"));
    $("#editRobotModal [name='profileFolderField']").html(doc.getDocOnline("robot", "ProfileFolder"));
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
    displayInvariantList("type", "APPLITYPE", false, undefined, "", undefined, undefined, "editRobotModal");
    displayInvariantList("lbexemethod", "ROBOTLBMETHOD", false);

    var availableUserAgent = getInvariantArray("USERAGENT", false);
    $("#editRobotModal [name='useragent']").autocomplete({
        source: availableUserAgent,
        minLength: 0,
        messages: {
            noResults: '',
            results: function (amount) {
                return '';
            }
        }
    }).on("focus", function () {
        $(this).autocomplete("search", "");
    });
    var availableScreenSize = getInvariantArray("SCREENSIZE", false);
    $("#editRobotModal [name='screensize']").autocomplete({
        source: availableScreenSize,
        minLength: 0,
        messages: {
            noResults: '',
            results: function (amount) {
                return '';
            }
        }
    }).on("focus", function () {
        $(this).autocomplete("search", "");
    });
    // Load invariant into cache.
    getInvariantArray("ROBOTHOST", false);
    getInvariantArray("ROBOTPROXYHOST", false);

    // Load the select needed in localStorage cache.
    getSelectInvariant("CAPABILITY", true);
    getSelectInvariant("ROBOTEXECUTORACTIVE", true);

    getSelectInvariant('PROXYTYPE', false);

    // Adding rows in modals.
    $("#addEditCapability").off("click").click(addNewCapabilityRow.bind(null, "editCapabilitiesTableBody"));
    $("#addEditExecutor").off("click").click(addNewExecutorRow.bind(null, "editExecutorsTableBody"));


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

    // when type is changed we enable / disable type field.
    $("#platform").off("change");
    $("#platform").change(function () {
        if ($(this).val() !== "") {
            $('#platformLogo').attr('src', './images/platform-' + $(this).val() + '.png');
        } else {
            $('#platformLogo').attr('src', '');
        }
    });
    $("#browser").off("change");
    $("#browser").change(function () {
        if ($(this).val() !== "") {
            $('#browserLogo').attr('src', './images/browser-' + $(this).val() + '.png');
        } else {
            $('#browserLogo').attr('src', '');
        }
    });

    $("#editRobotModal").on("hidden.bs.modal", function() {
       originalRobotName = null;
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

    data.acceptNotifications = formEdit.find("#acceptNotifications").find("label[class*='active'] > input").attr('data-accnotif');
    data.isAcceptInsecureCerts = formEdit.find("#isAcceptInsecureCerts").prop("checked");
    data.robotIsActive = formEdit.find("#robotIsActive").prop("checked");
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
        data: {
            originalRobotName: originalRobotName,
            robot: data.robot,
            robotid: data.robotid,
            isActive: data.robotIsActive,
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
            profileFolder: data.profileFolder,
            robotDecli: data.robotdecli,
            description: data.description,
            isAcceptInsecureCerts: data.isAcceptInsecureCerts,
            extraParam: data.extraParam,
            acceptNotifications: data.acceptNotifications,
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
    originalRobotName = robot;

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
        robotObj1.isActive = true;
        robotObj1.host = "";
        robotObj1.port = "";
        robotObj1.platform = "";
        robotObj1.browser = "";
        robotObj1.version = "";
        robotObj1.userAgent = "";
        robotObj1.screenSize = "";
        robotObj1.profileFolder = "";
        robotObj1.robotDecli = "";
        robotObj1.description = "";
        robotObj1.lbexemethod = "BYRANKING";
        robotObj1.type = "";
        robotObj1.extraParam = "";
        robotObj1.acceptNotifications = 0;
        robotObj1.isAcceptInsecureCerts = true;
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
        formEdit.find("#robotIsActive").prop("checked", true);
        formEdit.find("#platform").val("");
        formEdit.find("#browser").val("");
        formEdit.find("#version").prop("value", "");
        formEdit.find("#useragent").prop("value", "");
        formEdit.find("#screensize").prop("value", "");
        formEdit.find("#profileFolder").prop("value", "");
        formEdit.find("#robotdecli").prop("value", "");
        formEdit.find("#Description").prop("value", "");
        formEdit.find("#type").val("");
        formEdit.find("#lbexemethod").val("ROUNDROBIN");
        formEdit.find("#extraParam").val("");
        formEdit.find("#acceptNotifications").val("");
        formEdit.find("#isAcceptInsecureCerts").prop("checked", true);
    } else {
        if (mode === "EDIT") {
            formEdit.find("#robotid").prop("value", robot.robotID);
        } else {
            formEdit.find("#robotid").prop("value", "");
        }
        formEdit.find("#robotName").prop("value", robot.robot);
        formEdit.find("#robotIsActive").prop("checked", robot.isActive);
        formEdit.find("#platform").val(robot.platform);
        if (robot.platform !== "") {
            $('#platformLogo').attr('src', './images/platform-' + robot.platform + '.png');
        } else {
            $('#platformLogo').attr('src', '');
        }
        formEdit.find("#browser").val(robot.browser);
        if (robot.browser !== "") {
            $('#browserLogo').attr('src', './images/browser-' + robot.browser + '.png');
        } else {
            $('#browserLogo').attr('src', '');
        }
        formEdit.find("#version").prop("value", robot.version);
        formEdit.find("#useragent").prop("value", robot.userAgent);
        formEdit.find("#screensize").prop("value", robot.screenSize);
        formEdit.find("#profileFolder").prop("value", robot.profileFolder);
        formEdit.find("#robotdecli").prop("value", robot.robotDecli);
        formEdit.find("#Description").prop("value", robot.description);
        formEdit.find("#type").val(robot.type);
        formEdit.find("#lbexemethod").val(robot.lbexemethod);
        formEdit.find("#extraParam").val(robot.extraParam);
        formEdit.find("#acceptNotifications").find("input[data-accnotif='" + robot.acceptNotifications + "']").off("click").click();
        formEdit.find("#isAcceptInsecureCerts").prop("checked", robot.isAcceptInsecureCerts);
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
        formEdit.find("#robotIsActive").removeAttr("disabled");
        formEdit.find("#host").prop("readonly", false);
        formEdit.find("#port").prop("readonly", false);
        formEdit.find("#platform").removeAttr("disabled");
        formEdit.find("#browser").removeAttr("disabled");
        formEdit.find("#lbexemethod").removeAttr("disabled");
        formEdit.find("#version").prop("readonly", false);
        formEdit.find("#useragent").prop("readonly", false);
        formEdit.find("#screensize").prop("readonly", false);
        formEdit.find("#profileFolder").prop("readonly", false);
        formEdit.find("#robotdecli").prop("readonly", false);
        formEdit.find("#Description").prop("readonly", false);
        formEdit.find("#hostPassword").prop("readonly", false);
        formEdit.find("#hostUsername").prop("readonly", false);
        formEdit.find("#type").prop("readonly", false);
        formEdit.find("#extraParam").prop("readonly", false);
        formEdit.find("#acceptNotifications").prop("readonly", false);
        formEdit.find("#isAcceptInsecureCerts").prop("readonly", false);
    } else {
        formEdit.find("#robotName").prop("readonly", "readonly");
        formEdit.find("#robotIsActive").prop("disabled", "disabled");
        formEdit.find("#host").prop("readonly", "readonly");
        formEdit.find("#port").prop("readonly", "readonly");
        formEdit.find("#platform").prop("disabled", "disabled");
        formEdit.find("#browser").prop("disabled", "disabled");
        formEdit.find("#lbexemethod").prop("disabled", "disabled");
        formEdit.find("#version").prop("readonly", "readonly");
        formEdit.find("#useragent").prop("readonly", "readonly");
        formEdit.find("#screensize").prop("readonly", "readonly");
        formEdit.find("#profileFolder").prop("readonly", "readonly");
        formEdit.find("#robotdecli").prop("readonly", "readonly");
        formEdit.find("#Description").prop("readonly", "readonly");
        formEdit.find("#hostPassword").prop("readonly", "readonly");
        formEdit.find("#hostUsername").prop("readonly", "readonly");
        formEdit.find("#type").prop("readonly", "readonly");
        formEdit.find("#extraParam").prop("readonly", "readonly");
        formEdit.find("#acceptNotifications").prop("readonly", "readonly");
        formEdit.find("#isAcceptInsecureCerts").prop("readonly", "readonly");
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

    //As the executors list is dynamically generated after the global popover initialization, need to init popover again on executors list only.
    $('#' + tableBody + ' [data-toggle="popover"]').popover({
        'placement': 'auto',
        'container': '#listPanelEditExecutors'}
    );
}

function appendCapabilityRow(tableBody, capability) {
    var doc = new Doc();
    var deleteBtn = $("<button type=\"button\"></button>").addClass("btn btn-default btn-xs").append($("<span></span>").addClass("glyphicon glyphicon-trash"));
    var inputCapability = $("<input  maxlength=\"45\" placeholder=\"-- " + doc.getDocLabel("robot", "capabilityCapability") + " --\">").addClass("form-control").val(capability.capability);
//    var inputCapability = getSelectInvariant("CAPABILITY", false, false);
    var valueInput = $("<input  maxlength=\"255\" placeholder=\"-- " + doc.getDocLabel("robot", "capabilityValue") + " --\">").addClass("form-control").val(capability.value);
    var table = $("#" + tableBody);


    inputCapability.autocomplete({
        source: getInvariantArray("CAPABILITY", false),
        minLength: 0,
        select: function (event, ui) {
            capability.capability = ui.item.value;
        },
        messages: {
            noResults: '',
            results: function (amount) {
                return '';
            }
        }
    }).on("focus", function () {
        $(this).autocomplete("search", "");
    });


    var row = $("<tr></tr>");
    var deleteBtnRow = $("<td></td>").append(deleteBtn);
    var cap = $("<td></td>").append(inputCapability);
    var value = $("<td></td>").append(valueInput);
    deleteBtn.click(function () {
        capability.toDelete = (capability.toDelete) ? false : true;
        if (capability.toDelete) {
            row.addClass("danger");
        } else {
            row.removeClass("danger");
        }
    });
    inputCapability.change(function () {
        capability.capability = $(this).val();
    });
    valueInput.change(function () {
        capability.value = $(this).val();
    });
    row.append(deleteBtnRow);
    row.append(cap);
    row.append(value);
//    capability.capability = inputCapability.prop("value"); // Value that has been requested by dtb parameter may not exist in combo values so we take the real selected value.
    row.data("capability", capability);
    table.append(row);
}

function appendExecutorRow(tableBody, executor) {
    nbRow++;

    var doc = new Doc();
    var deleteBtn = $("<button type=\"button\"></button>").addClass("btn btn-default btn-xs").append($("<span></span>").addClass("glyphicon glyphicon-trash"));
    var selectActive = $("<input  type=\"checkbox\">").addClass("form-control input-sm").prop("checked", executor.isActive);
    var nameInput = $("<input  maxlength=\"150\" placeholder=\"-- " + doc.getDocLabel("robotexecutor", "executor") + " --\">").addClass("form-control input-sm").val(executor.executor);
    var rankInput = $("<input  placeholder=\"-- " + doc.getDocLabel("robotexecutor", "rank") + " --\">").addClass("form-control input-sm").val(executor.rank);
    var hostInput = $("<input  placeholder=\"-- " + doc.getDocLabel("robotexecutor", "host") + " --\">").addClass("form-control input-sm").val(executor.host);
    var portInput = $("<input  placeholder=\"-- " + doc.getDocLabel("robotexecutor", "Port") + " --\">").addClass("form-control input-sm").val(executor.port);
    var hostUserInput = $("<input  placeholder=\"-- " + doc.getDocLabel("robotexecutor", "hostUser") + " --\">").addClass("form-control input-sm").val(executor.hostUser);
    var hostPasswordInput = $("<input  placeholder=\"-- " + doc.getDocLabel("robotexecutor", "hostPassword") + " --\">").addClass("form-control input-sm").val(executor.hostPassword);
    var deviceUdidInput = $("<input  placeholder=\"-- " + doc.getDocLabel("robotexecutor", "deviceUdid") + " --\">").addClass("form-control input-sm").val(executor.deviceUdid);
    var deviceNameInput = $("<input  placeholder=\"-- " + doc.getDocLabel("robotexecutor", "deviceName") + " --\">").addClass("form-control input-sm").val(executor.deviceName);
    var devicePortInput = $("<input  placeholder=\"-- " + doc.getDocLabel("robotexecutor", "devicePort") + " --\">").addClass("form-control input-sm").val(executor.devicePort);
    var deviceLockUnlockInput = $("<input type='checkbox' placeholder=\"-- " + doc.getDocLabel("robotexecutor", "deviceLockUnlock") + " --\">").addClass("form-control input-sm").prop("checked", executor.isDeviceLockUnlock);
    var executorProxyServiceHostInput = $("<input  placeholder=\"-- " + doc.getDocLabel("robotexecutor", "executorProxyServiceHost") + " --\">").addClass("form-control input-sm").val(executor.executorProxyServiceHost);
    var executorProxyServicePortInput = $("<input  placeholder=\"-- " + doc.getDocLabel("robotexecutor", "executorProxyServicePort") + " --\">").addClass("form-control input-sm").val(executor.executorProxyServicePort);
    var executorBrowserProxyHostInput = $("<input  placeholder=\"-- " + doc.getDocLabel("robotexecutor", "executorBrowserProxyHost") + " --\">").addClass("form-control input-sm").val(executor.executorBrowserProxyHost);
    var executorBrowserProxyPortInput = $("<input  placeholder=\"-- " + doc.getDocLabel("robotexecutor", "executorBrowserProxyPort") + " --\">").addClass("form-control input-sm").val(executor.executorBrowserProxyPort);
    var executorExtensionPortInput = $("<input  placeholder=\"-- " + doc.getDocLabel("robotexecutor", "executorExtensionPort") + " --\">").addClass("form-control input-sm").val(executor.executorExtensionPort);

    var selectProxyTypeLnk = getSelectInvariant('PROXYTYPE', false);
    var table = $("#" + tableBody);

    if (executor.executorProxyType === 'NONE' || executor.executorProxyType === 'MANUAL') {
        executorProxyServiceHostInput.prop("readonly", true);
        executorProxyServicePortInput.prop("readonly", true);
    } else {
        executorProxyServiceHostInput.prop("readonly", false);
        executorProxyServicePortInput.prop("readonly", false);
    }
    if (executor.executorProxyType === 'NONE') {
        executorBrowserProxyHostInput.prop("readonly", true);
        executorBrowserProxyPortInput.prop("readonly", true);
    } else {
        executorBrowserProxyHostInput.prop("readonly", false);
        executorBrowserProxyPortInput.prop("readonly", false);
    }



    selectProxyTypeLnk.change(function () {
        executor.executorProxyType = $(this).val();
        if (executor.executorProxyType === 'NONE' || executor.executorProxyType === 'MANUAL') {
            executorProxyServiceHostInput.prop("readonly", true);
            executorProxyServicePortInput.prop("readonly", true);
        } else {
            executorProxyServiceHostInput.prop("readonly", false);
            executorProxyServicePortInput.prop("readonly", false);
        }
        if (executor.executorProxyType === 'NONE') {
            executorBrowserProxyHostInput.prop("readonly", true);
            executorBrowserProxyPortInput.prop("readonly", true);
        } else {
            executorBrowserProxyHostInput.prop("readonly", false);
            executorBrowserProxyPortInput.prop("readonly", false);
        }

    });

    var row = $("<tr></tr>");

    var td1 = $("<td></td>").append(deleteBtn);

    var name = $("<div class='form-group col-sm-12'></div>").append("<label for='name'>" + doc.getDocOnline("robotexecutor", "executor") + "</label>").append(nameInput);
    var drow01 = $("<div class='row'></div>").append(name);
    var td2 = $("<td></td>").append(drow01);

    var active = $("<div class='form-group col-sm-6'></div>").append("<label for='active'>" + doc.getDocOnline("robotexecutor", "active") + "</label>").append(selectActive);
    var rank = $("<div class='form-group col-sm-4'></div>").append("<label for='rank'>" + doc.getDocOnline("robotexecutor", "rank") + "</label>").append(rankInput);
    var expandName = $("<div class='form-group col-sm-2'></div>").append("<button class='btn btn-primary' type='button' data-toggle='collapse' data-target='#col" + nbRow + "' aria-expanded='false' aria-controls='col" + nbRow + "'><span class='glyphicon glyphicon-chevron-down'></span></button>");
    var host = $("<div class='form-group col-sm-4'></div>").append("<label for='host'>" + doc.getDocOnline("robotexecutor", "host") + "</label>").append(hostInput);
    var port = $("<div class='form-group col-sm-2'></div>").append("<label for='port'>" + doc.getDocOnline("robotexecutor", "Port") + "</label>").append(portInput);
    var hostUser = $("<div class='form-group col-sm-3'></div>").append("<label for='hostUser'>" + doc.getDocOnline("robotexecutor", "hostUser") + "</label>").append(hostUserInput);
    var hostPassword = $("<div class='form-group col-sm-3'></div>").append("<label for='hostPassword'>" + doc.getDocOnline("robotexecutor", "hostPassword") + "</label>").append(hostPasswordInput);
    var dudid = $("<div class='form-group col-sm-4'></div>").append("<label for='deviceudid'>" + doc.getDocOnline("robotexecutor", "deviceUdid") + "</label>").append(deviceUdidInput);
    var dname = $("<div class='form-group col-sm-4'></div>").append("<label for='devicename'>" + doc.getDocOnline("robotexecutor", "deviceName") + "</label>").append(deviceNameInput);
    var dport = $("<div class='form-group col-sm-2'></div>").append("<label for='deviceport'>" + doc.getDocOnline("robotexecutor", "devicePort") + "</label>").append(devicePortInput);
    var dLockUnlock = $("<div class='form-group col-sm-2'></div>").append("<label for='devicelockunlockinput'>" + doc.getDocOnline("robotexecutor", "deviceLockUnlock") + "</label>").append(deviceLockUnlockInput);
    var epType = $("<div class='form-group col-sm-2'></div>").append("<label for='executorproxytype'>" + doc.getDocOnline("robotexecutor", "executorProxyType") + "</label>").append(selectProxyTypeLnk.val(executor.executorProxyType));
    var eehost = $("<div class='form-group col-sm-3'></div>").append("<label for='executorProxyServiceHost'>" + doc.getDocOnline("robotexecutor", "executorProxyServiceHost") + "</label>").append(executorProxyServiceHostInput);
    var eeport = $("<div class='form-group col-sm-2'></div>").append("<label for='executorProxyServicePort'>" + doc.getDocOnline("robotexecutor", "executorProxyServicePort") + "</label>").append(executorProxyServicePortInput);
    var ephost = $("<div class='form-group col-sm-3'></div>").append("<label for='executorBrowserProxyHost'>" + doc.getDocOnline("robotexecutor", "executorBrowserProxyHost") + "</label>").append(executorBrowserProxyHostInput);
    var epport = $("<div class='form-group col-sm-2'></div>").append("<label for='executorBrowserProxyPort'>" + doc.getDocOnline("robotexecutor", "executorBrowserProxyPort") + "</label>").append(executorBrowserProxyPortInput);
    var executorExtensionPortGroup = $("<div class='form-group col-sm-2'></div>").append("<label for='executorExtensionPort'>" + doc.getDocOnline("robotexecutor", "executorExtensionPort") + "</label>").append(executorExtensionPortInput);
    var drow1 = $("<div class='row'></div>").append(active).append(rank).append(expandName);
    var drow2 = $("<div class='row'></div>").append(host).append(port).append(hostUser).append(hostPassword);
//    var drow3 = $("<div class='row'></div>").append(hostUser).append(hostPassword);
    var drow4 = $("<div class='row alert alert-warning'></div>").append(dudid).append(dname).append(dport).append(dLockUnlock);
    var drow5 = $("<div class='row alert alert-warning'></div>").append(epType).append(eehost).append(eeport).append(ephost).append(epport);
    var drow6 = $("<div class='row alert alert-warning'></div>").append(executorExtensionPortGroup);
    var panelExtra = $("<div class='collapse' id='col" + nbRow + "'></div>").append(drow4).append(drow5).append(drow6);
    var td3 = $("<td></td>").append(drow1).append(drow2).append(panelExtra);



    deleteBtn.click(function () {
        executor.toDelete = (executor.toDelete) ? false : true;
        if (executor.toDelete) {
            row.addClass("danger");
        } else {
            row.removeClass("danger");
        }
    });
    selectActive.change(function () {
        executor.isActive = $(this).prop("checked");
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
        executor.isDeviceLockUnlock = $(this).prop("checked");
    });
    executorProxyServiceHostInput.change(function () {
        executor.executorProxyServiceHost = $(this).val();
    });
    executorProxyServicePortInput.change(function () {
        executor.executorProxyServicePort = $(this).val();
    });
    executorBrowserProxyHostInput.change(function () {
        executor.executorBrowserProxyHost = $(this).val();
    });
    executorBrowserProxyPortInput.change(function () {
        executor.executorBrowserProxyPort = $(this).val();
    });
    executorExtensionPortInput.change(function () {
        executor.executorExtensionPort = $(this).val();
    });

    hostInput.autocomplete({
        source: getInvariantArray("ROBOTHOST", false),
        minLength: 0,
        messages: {
            noResults: '',
            results: function (amount) {
                return '';
            }
        }
    }).on("focus", function () {
        $(this).autocomplete("search", "");
    });

    executorProxyServiceHostInput.autocomplete({
        source: getInvariantArray("ROBOTPROXYHOST", false),
        minLength: 0,
        messages: {
            noResults: '',
            results: function (amount) {
                return '';
            }
        }
    }).on("focus", function () {
        $(this).autocomplete("search", "");
    });

    executorBrowserProxyHostInput.autocomplete({
        source: getInvariantArray("ROBOTPROXYHOST", false),
        minLength: 0,
        messages: {
            noResults: '',
            results: function (amount) {
                return '';
            }
        }
    }).on("focus", function () {
        $(this).autocomplete("search", "");
    });

    row.append(td1);
    row.append(td2);
    row.append(td3);
//    executor.active = selectActive.prop("value"); // Value that has been requested by dtb parameter may not exist in combo values so we take the real selected value.
    executor.executorProxyType = selectProxyTypeLnk.prop("value"); // Value that has been requested by dtb parameter may not exist in combo vlaues so we take the real selected value.
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
        isActive: true,
        rank: nbExecutorTable,
        host: "",
        port: "",
        hostUser: "",
        hostPassword: "",
        deviceUdid: "",
        deviceName: "",
        isDeviceLockUnlock: false,
        description: "",
        executorProxyServiceHost: "",
        executorProxyServicePort: 8093,
        executorBrowserProxyHost: "",
        executorBrowserProxyPort: 0,
        executorExtensionPort: 0,
        executorProxyType: "NONE"
    };
    appendExecutorRow(tableBody, newExecutor);
    refreshPopoverDocumentation("editExecutorsTable");

}
