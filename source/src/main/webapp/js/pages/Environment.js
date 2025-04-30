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
var nbRow = 0;

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
    displayPageLabel();

    var urlBuild = GetURLParameter('build', 'ALL'); // Feed Build combo with Build list.
    var urlRevision = GetURLParameter('revision', 'ALL'); // Feed Revision combo with Revision list.
    var urlEnvGp = GetURLParameter('envgp', 'ALL'); // Feed Environment Group combo with Environment list.
    var urlActive = GetURLParameter('active', 'ALL'); // Feed Active combo with Active list.

    var urlSystem = GetURLParameter('system', 'ALL'); // Feed Environment combo with Environment list.
    var urlCountry = GetURLParameter('country', 'ALL'); // Feed Country combo with Country list.
    var urlEnvironment = GetURLParameter('environment', 'ALL'); // Feed Environment combo with Environment list.

    displayInvariantList("system", "SYSTEM", false);
    displayInvariantList("country", "COUNTRY", false);
    displayInvariantList("environment", "ENVIRONMENT", false);
    displayInvariantList("active", "ENVACTIVE", false);
    displayInvariantList("type", "ENVTYPE", false, undefined, undefined, undefined, undefined, "editEnvModal");
    displayInvariantList("maintenanceAct", "MNTACTIVE", false, "N");
    displayInvariantList("chain", "CHAIN", false, "Y");
    displayBatchInvariantList('batch', getUser().defaultSystem);

    displayBuildList('#newBuild', getUser().defaultSystem, "1", "", "", "");
    displayBuildList('#newRevision', getUser().defaultSystem, "2", "", "", "");

    var table = loadEnvTable(urlCountry, urlEnvironment, urlBuild, urlRevision, urlEnvGp, urlActive, urlSystem);

    // Load the select needed in localStorage cache.
    getSelectApplication(getUser().defaultSystem, true);
    getSelectInvariant("PROPERTYDATABASE", true);
    getSelectInvariant('SYSTEM', false);
    getSelectInvariant('COUNTRY', false);
    getSelectInvariant('ENVIRONMENT', false);
    getSelectDeployType(true);

    // handle the click for specific action buttons
    $("#addEnvButton").click(addEntryModalSaveHandler);
    $("#editEnvButton").click(editEntryModalSaveHandler);

    //clear the modals fields when closed
    $('#addEnvModal').on('hidden.bs.modal', addEntryModalCloseHandler);
    $('#editEnvModal').on('hidden.bs.modal', editEntryModalCloseHandler);
    $('#eventEnableModal').on('hidden.bs.modal', eventEnableModalCloseHandler);
    $("#eventEnablePreviewNotificationButton").click(eventEnablePreview);
    $("#eventEnableButton").click(eventEnableModalConfirmHandler);
    $('#eventDisableModal').on('hidden.bs.modal', eventDisableModalCloseHandler);
    $("#eventDisableButton").click(eventDisableModalConfirmHandler);
    $('#eventNewChainModal').on('hidden.bs.modal', eventNewChainModalCloseHandler);
    $("#eventNewChainPreviewNotificationButton").click(eventNewChainPreview);
    $("#eventNewChainButton").click(eventNewChainModalConfirmHandler);

    // Adding rows in edit Modal.
    $("#addDatabase").click(addNewDatabaseRow);
    $("#addDependencies").click(addNewDependenciesRow);
    $("#addDeployType").click(addNewDeployTypeRow);

}

function displayPageLabel() {
    var doc = new Doc();

    displayHeaderLabel(doc);
    $("#pageTitle").html(doc.getDocLabel("page_environment", "title"));
    $("#title").html(doc.getDocOnline("page_environment", "title"));
    $("[name='createEnvField']").html(doc.getDocLabel("page_environment", "button_create"));
    $("[name='confirmationField']").html(doc.getDocLabel("page_environment", "button_delete"));
    $("[name='editEnvField']").html(doc.getDocLabel("page_environment", "button_edit"));
    $("[name='listField']").html(doc.getDocOnline("page_environment", "list"));
    $("[name='buttonAdd']").html(doc.getDocLabel("page_global", "buttonAdd"));
    $("[name='buttonClose']").html(doc.getDocLabel("page_global", "buttonClose"));
    $("[name='buttonConfirm']").html(doc.getDocLabel("page_global", "buttonConfirm"));
    $("[name='buttonDismiss']").html(doc.getDocLabel("page_global", "buttonDismiss"));
    $("[name='filtersField']").html(doc.getDocOnline("page_global", "filters"));
    //$("[name='btnLoad']").html(doc.getDocLabel("page_global", "buttonLoad"));

    $("[name='systemField']").html(doc.getDocOnline("invariant", "SYSTEM"));
    $("[name='countryField']").html(doc.getDocOnline("invariant", "COUNTRY"));
    $("[name='environmentField']").html(doc.getDocOnline("invariant", "ENVIRONMENT"));
    $("[name='buildField']").html(doc.getDocOnline("buildrevisioninvariant", "versionname01"));
    $("[name='revisionField']").html(doc.getDocOnline("buildrevisioninvariant", "versionname02"));
    $("[name='envGpField']").html(doc.getDocOnline("invariant", "ENVGP"));

    $("[name='descriptionField']").html(doc.getDocOnline("countryenvparam", "Description"));
    $("[name='typeField']").html(doc.getDocOnline("countryenvparam", "Type"));
    $("[name='maintenanceActField']").html(doc.getDocOnline("countryenvparam", "maintenanceact"));
    $("[name='maintenanceStrField']").html(doc.getDocOnline("countryenvparam", "maintenancestr"));
    $("[name='maintenanceEndField']").html(doc.getDocOnline("countryenvparam", "maintenanceend"));

    $("[name='activeField']").html(doc.getDocOnline("countryenvparam", "active"));
    $("[name='chainField']").html(doc.getDocOnline("countryenvparam", "chain"));
    $("[name='distribListField']").html(doc.getDocOnline("countryenvparam", "DistribList"));
    $("[name='eMailBodyChainField']").html(doc.getDocOnline("countryenvparam", "EMailBodyChain"));
    $("[name='eMailBodyRevisionField']").html(doc.getDocOnline("countryenvparam", "EMailBodyRevision"));
    $("[name='eMailBodyDisableEnvironmentField']").html(doc.getDocOnline("countryenvparam", "EMailBodyDisableEnvironment"));

    $("[name='changeListField']").html(doc.getDocOnline("page_environment", "listChange"));
    $("[name='eventListField']").html(doc.getDocOnline("page_environment", "listEvent"));

    $("[name='toField']").html(doc.getDocOnline("page_environment", "to"));
    $("[name='ccField']").html(doc.getDocOnline("page_environment", "cc"));
    $("[name='subjectField']").html(doc.getDocOnline("page_environment", "subject"));

    $("[name='eventEnableField']").html(doc.getDocOnline("page_environment", "button_enable"));
    $("[name='currentBuildField']").html(doc.getDocOnline("page_environment", "currentBuild"));
    $("[name='currentRevisionField']").html(doc.getDocOnline("page_environment", "currentRevision"));
    $("[name='newBuildField']").html(doc.getDocOnline("page_environment", "newBuild"));
    $("[name='newRevisionField']").html(doc.getDocOnline("page_environment", "newRevision"));
    $("[name='buttonEnablePreviewNotification']").html(doc.getDocOnline("page_environment", "buttonPreviewNotification"));
    $("[name='buttonEnable']").html(doc.getDocOnline("page_environment", "button_enable1"));
    $("[name='tab1Text']").html(doc.getDocOnline("page_environment", "tabPreview"));
    $("[name='tab2Text']").html(doc.getDocOnline("page_environment", "tabInstallInstruction"));
    $("[name='buildHeader']").html(doc.getDocOnline("buildrevisionparameters", "Build"));
    $("[name='revisionHeader']").html(doc.getDocOnline("buildrevisionparameters", "Revision"));
//    $("[name='applicationHeader']").html(doc.getDocOnline("buildrevisionparameters", "application"));
    $("[name='releaseHeader']").html(doc.getDocOnline("buildrevisionparameters", "Release"));
    $("[name='linkHeader']").html(doc.getDocOnline("buildrevisionparameters", "Link"));
    $("[name='versionHeader']").html(doc.getDocOnline("buildrevisionparameters", "mavenVersion"));

    $("[name='eventDisableField']").html(doc.getDocOnline("page_environment", "button_disable"));
    $("[name='buttonDisable']").html(doc.getDocOnline("page_environment", "button_disable1"));

    $("[name='eventNewChainField']").html(doc.getDocOnline("page_environment", "button_newChain"));
    $("[name='buttonNewChain']").html(doc.getDocOnline("page_environment", "button_newChain1"));
    $("[name='buttonNewChainPreviewNotification']").html(doc.getDocOnline("page_environment", "buttonPreviewNotification"));
    // Tab
    $("[name='tabDefinition']").html(doc.getDocOnline("page_environment", "tabDefinition"));
    $("[name='tabBuild']").html(doc.getDocOnline("page_environment", "tabBuild"));
    $("[name='tabChain']").html(doc.getDocOnline("page_environment", "tabChain"));
    $("[name='tabApplication']").html(doc.getDocOnline("page_environment", "tabApplication"));
    $("[name='tabDatabase']").html(doc.getDocOnline("page_environment", "tabDatabase"));
    $("[name='tabDependencies']").html(doc.getDocOnline("page_environment", "tabDependencies"));
    $("[name='tabDeploy']").html(doc.getDocOnline("page_environment", "tabDeploy"));
    $("[name='tabNotif']").html(doc.getDocOnline("page_environment", "tabNotif"));
    // Application List
//    $("[name='applicationHeader']").html(doc.getDocOnline("application", "Application"));
//    $("[name='ipHeader']").html(doc.getDocOnline("countryenvironmentparameters", "IP") + '<br>' + doc.getDocOnline("countryenvironmentparameters", "URLLOGIN"));
//    $("[name='urlHeader']").html(doc.getDocOnline("countryenvironmentparameters", "URL") + '<br>' + doc.getDocOnline("countryenvironmentparameters", "domain"));
//    $("#var1Header").html(doc.getDocOnline("countryenvironmentparameters", "Var1")
//            + '<br>' + doc.getDocOnline("countryenvironmentparameters", "Var2"));
//    $("#var3Header").html(doc.getDocOnline("countryenvironmentparameters", "Var3")
//            + '<br>' + doc.getDocOnline("countryenvironmentparameters", "Var4"));
//    $("#poolSizeHeader").html(doc.getDocOnline("countryenvironmentparameters", "poolSize"));

    // Databases List
    $("[name='databaseHeader']").html(doc.getDocOnline("countryenvironmentdatabase", "Database"));
    $("[name='connectionPoolNameHeader']").html(doc.getDocOnline("countryenvironmentdatabase", "ConnectionPoolName"));
    // Dependencies List
    $("[name='systemHeader']").html(doc.getDocOnline("countryenvlink", "systemLink"));
    $("[name='countryHeader']").html(doc.getDocOnline("countryenvlink", "CountryLink"));
    $("[name='environmentHeader']").html(doc.getDocOnline("countryenvlink", "EnvironmentLink"));
    // Deploy Type List
    $("[name='deployTypeHeader']").html(doc.getDocOnline("deploytype", "deploytype"));
    $("[name='jenkinsAgentHeader']").html(doc.getDocOnline("countryenvdeploytype", "JenkinsAgent"));

    displayFooter(doc);
}

function loadEnvTable(selectCountry, selectEnvironment, selectBuild, selectRevision, selectEnvGp, selectActive, selectSystem) {

    //clear the old report content before reloading it
    $("#environmentList").empty();
    $("#environmentList").html('<table id="environmentsTable" class="table table-bordered table-hover display" name="environmentsTable">\n\
                                            </table><div class="marginBottom20"></div>');

    //configure and create the dataTable
    var contentUrl = "ReadCountryEnvParam?forceList=Y";

    var configurations = new TableConfigurationsServerSide("environmentsTable", contentUrl, "contentTable", aoColumnsFunc("environmentsTable"), [3, 'asc']);

    var table = createDataTableWithPermissions(configurations, renderOptionsForEnv, "#environmentList", undefined, true);

    var searchArray = [];
    var searchObject = {param: "col", values: "val"};

    if ((selectEnvironment !== null) && (selectEnvironment !== 'ALL')) {
        searchObject = {param: "environment", values: selectEnvironment};
        searchArray.push(searchObject);
    }
    if ((selectCountry !== null) && (selectCountry !== 'ALL')) {
        searchObject = {param: "country", values: selectCountry};
        searchArray.push(searchObject);
    }
    if ((selectBuild !== null) && (selectBuild !== 'ALL')) {
        searchObject = {param: "build", values: selectBuild};
        searchArray.push(searchObject);
    }
    if ((selectRevision !== null) && (selectRevision !== 'ALL')) {
        searchObject = {param: "revision", values: selectRevision};
        searchArray.push(searchObject);
    }
    if ((selectEnvGp !== null) && (selectEnvGp !== 'ALL')) {
        searchObject = {param: "envGp", values: selectEnvGp};
        searchArray.push(searchObject);
    }
    if ((selectActive !== null) && (selectActive !== 'ALL')) {
        searchObject = {param: "active", values: selectActive};
        searchArray.push(searchObject);
    }
    if ((selectSystem !== null) && (selectSystem !== 'ALL')) {
        searchObject = {param: "system", values: selectSystem};
        searchArray.push(searchObject);
    }
    if (searchArray.length > 0) {
        applyFiltersOnMultipleColumns("environmentsTable", searchArray, false);
    }

    return table;
}

function renderOptionsForEnv(data) {
    var doc = new Doc();
    //check if user has permissions to perform the add operations
    if (data["hasPermissions"]) {
        if ($("#createEnvButton").length === 0) {
            var contentToAdd = "<div class='marginBottom10'><button id='createEnvButton' type='button' class='btn btn-default'>\n\
            <span class='glyphicon glyphicon-plus-sign'></span> " + doc.getDocLabel("page_environment", "button_create") + "</button></div>";

            $("#environmentsTable_wrapper div#environmentsTable_length").before(contentToAdd);
            $('#environmentList #createEnvButton').click(addEntryClick);
        }
    }
}

function appendBuildList(selectName, level, defaultValue) {
    var select = $('[name="' + selectName + '"]');

    $.ajax({
        type: "GET",
        url: "ReadBuildRevisionInvariant",
        data: {iSortCol_0: "2", system: getUser().defaultSystem, level: level},
        async: false,
        dataType: 'json',
        success: function (data) {
            select.append($('<option></option>').text("-- ALL --").val("ALL"));

            for (var option in data.contentTable) {
                select.append($('<option></option>').text(data.contentTable[option].versionName).val(data.contentTable[option].versionName));
            }
            if (defaultValue !== undefined && defaultValue !== null) {
                select.val(defaultValue);
            } else {
                select.val("ALL");
            }

        },
        error: showUnexpectedError
    });
}

function deleteEntryHandlerClick() {
    var system = $('#confirmationModal').find('#hiddenField1').prop("value");
    var country = $('#confirmationModal').find('#hiddenField2').prop("value");
    var environment = $('#confirmationModal').find('#hiddenField3').prop("value");
    var jqxhr = $.post("DeleteCountryEnvParam", {system: system, country: country, environment: environment}, "json");
    $.when(jqxhr).then(function (data) {
        var messageType = getAlertType(data.messageType);
        if (messageType === "success") {
            //redraw the datatable
            var oTable = $("#environmentsTable").dataTable();
            oTable.fnDraw(false);
            var info = oTable.fnGetData().length;

            if (info === 1) {//page has only one row, then returns to the previous page
                oTable.fnPageChange('previous');
            }

        }
        //show message in the main page
        showMessageMainPage(messageType, data.message, false);
        //close confirmation window
        $('#confirmationModal').modal('hide');
    }).fail(handleErrorAjaxAfterTimeout);
}

function deleteEntryClick(system, country, environment) {
    clearResponseMessageMainPage();
    var doc = new Doc();
    var messageComplete = doc.getDocLabel("page_environment", "message_delete");
    messageComplete = messageComplete.replace("%SYSTEM%", system);
    messageComplete = messageComplete.replace("%COUNTRY%", country);
    messageComplete = messageComplete.replace("%ENVIRONMENT%", environment);
    showModalConfirmation(deleteEntryHandlerClick, undefined, doc.getDocLabel("page_environment", "button_delete"), messageComplete, system, country, environment, "");
}

function addEntryModalSaveHandler() {
    clearResponseMessage($('#addEnvModal'));
    var formAdd = $("#addEnvModal #addEnvModalForm");

    // Get the header data from the form.
    var dataForm = convertSerialToJSONObject(formAdd.serialize());

    showLoaderInModal('#addEnvModal');
    var jqxhr = $.post("CreateCountryEnvParam", dataForm);
    $.when(jqxhr).then(function (data) {
        hideLoaderInModal('#addEnvModal');
        if (getAlertType(data.messageType) === 'success') {
            var oTable = $("#environmentsTable").dataTable();
            oTable.fnDraw(false);
            showMessage(data);
            $('#addEnvModal').modal('hide');
        } else {
            showMessage(data, $('#addEnvModal'));
        }
    }).fail(handleErrorAjaxAfterTimeout);
}

function addEntryModalCloseHandler() {
    // reset form values
    $('#addEnvModal #addEnvModalForm')[0].reset();
    // remove all errors on the form fields
    $(this).find('div.has-error').removeClass("has-error");
    // clear the response messages of the modal
    clearResponseMessage($('#addEnvModal'));
}

function addEntryClick() {
    clearResponseMessageMainPage();
    // When creating a new item, Define here the default value.
    var formAdd = $('#addEnvModal');

    // User that makes the creation is becoming the owner or the release.
    formAdd.find("#system").prop("value", getUser().defaultSystem);
    // New release goes by default to the build/revision selected in filter combos. (except when ALL)
    var myCountry = $("#selectCountry option:selected").val();
    var myEnvironment = $("#selectEnvironment option:selected").val();
    formAdd.find("#country").val(myCountry);
    formAdd.find("#environment").val(myEnvironment);
    // Force Default values to maintenance times
    formAdd.find("#maintenanceStr").val("01:00:00");
    formAdd.find("#maintenanceEnd").val("01:00:00");

    $('#addEnvModal').modal('show');
}

function editEntryModalSaveHandler() {
    clearResponseMessage($('#editEnvModal'));
    var formEdit = $('#editEnvModal #editEnvModalForm');

    // Getting Data from Application TAB
    var table1 = $("#applicationTableBody tr");
    var table_application = [];
    for (var i = 0; i < table1.length; i++) {
        table_application.push($(table1[i]).data("application"));
    }

    // Getting Data from Database TAB
    var table2 = $("#databaseTableBody tr");
    var table_database = [];
    for (var i = 0; i < table2.length; i++) {
        table_database.push($(table2[i]).data("database"));
    }

    // Getting Data from Dependencies TAB
    var table3 = $("#dependenciesTableBody tr");
    var table_dependencies = [];
    for (var i = 0; i < table3.length; i++) {
        table_dependencies.push($(table3[i]).data("dependencies"));
    }

    // Getting Data from DeployType TAB
    var table4 = $("#deployTypeTableBody tr");
    var table_deployType = [];
    for (var i = 0; i < table4.length; i++) {
        table_deployType.push($(table4[i]).data("deployType"));
    }

    // Get the header data from the form.
    var data = convertSerialToJSONObject(formEdit.serialize());

    showLoaderInModal('#editEnvModal');
    $.ajax({
        url: "UpdateCountryEnvParam",
        async: true,
        method: "POST",
        data: {system: data.system,
            country: data.country,
            environment: data.environment,
            description: data.description,
            type: data.type,
            distribList: data.distribList,
            eMailBodyChain: data.eMailBodyChain,
            eMailBodyDisableEnvironment: data.eMailBodyDisableEnvironment,
            eMailBodyRevision: data.eMailBodyRevision,
            maintenanceAct: data.maintenanceAct,
            maintenanceEnd: data.maintenanceEnd,
            maintenanceStr: data.maintenanceStr,
            chain: data.chain,
            application: JSON.stringify(table_application),
            database: JSON.stringify(table_database),
            dependencies: JSON.stringify(table_dependencies),
            deployType: JSON.stringify(table_deployType)
        },
        success: function (data) {
            hideLoaderInModal('#editEnvModal');
            if (getAlertType(data.messageType) === "success") {
                var oTable = $("#environmentsTable").dataTable();
                oTable.fnDraw(false);
                $('#editEnvModal').modal('hide');
                showMessage(data);
            } else {
                showMessage(data, $('#editEnvModal'));
            }
        },
        error: showUnexpectedError
    });
}

function editEntryModalCloseHandler() {
    // reset form values
    $('#editEnvModal #editEnvModalForm')[0].reset();
    // remove all errors on the form fields
    $(this).find('div.has-error').removeClass("has-error");
    // clear the response messages of the modal
    clearResponseMessage($('#editEnvModal'));
}

function editEntryClick(system, country, environment) {
    clearResponseMessageMainPage();

    showLoaderInModal('#editEnvModalForm');

    var jqxhr = $.getJSON("ReadCountryEnvParam", "system=" + system + "&country=" + country + "&environment=" + environment);
    $.when(jqxhr).then(function (data) {
        hideLoader("#editEnvModalForm");
        var obj = data["contentTable"];

        var formEdit = $('#editEnvModal');

        formEdit.find("#system").prop("value", system);
        formEdit.find("#country").prop("value", country);
        formEdit.find("#environment").prop("value", environment);
        formEdit.find("#buildNew").prop("value", obj["build"]);
        formEdit.find("#revisionNew").prop("value", obj["revision"]);
        formEdit.find("#chain").prop("value", obj["chain"]);
        formEdit.find("#activeNew").prop("checked", obj["active"]);
        formEdit.find("#type").val(obj["type"]);
        formEdit.find("#description").prop("value", obj["description"]);
        formEdit.find("#distribList").prop("value", obj["distribList"]);
        formEdit.find("#eMailBodyChain").prop("value", obj["eMailBodyChain"]);
        formEdit.find("#eMailBodyRevision").prop("value", obj["eMailBodyRevision"]);
        formEdit.find("#eMailBodyDisableEnvironment").prop("value", obj["eMailBodyDisableEnvironment"]);
        formEdit.find("#maintenanceStr").prop("value", obj["maintenanceStr"]);
        formEdit.find("#maintenanceEnd").prop("value", obj["maintenanceEnd"]);
        if (obj["maintenanceAct"]) {
            formEdit.find("#maintenanceAct").val("Y");
        } else {
            formEdit.find("#maintenanceAct").val("N");
        }

        if (!(data["hasPermissions"])) { // If readonly, we only readonly all fields
            formEdit.find("#link").prop("readonly", "readonly");
            formEdit.find("#build").prop("disabled", "disabled");

            formEdit.find("#system").prop("readonly", "readonly");
            formEdit.find("#country").prop("readonly", "readonly");
            formEdit.find("#environment").prop("readonly", "readonly");
            formEdit.find("#buildNew").prop("readonly", "readonly");
            formEdit.find("#revisionNew").prop("readonly", "readonly");
            formEdit.find("#chainNew").prop("readonly", "readonly");
            formEdit.find("#activeNew").prop("disabled", "disabled");
            formEdit.find("#type").prop("disabled", "disabled");
            formEdit.find("#description").prop("readonly", "readonly");
            formEdit.find("#distribList").prop("readonly", "readonly");
            formEdit.find("#eMailBodyChain").prop("readonly", "readonly");
            formEdit.find("#eMailBodyRevision").prop("readonly", "readonly");
            formEdit.find("#eMailBodyDisableEnvironment").prop("readonly", "readonly");
            formEdit.find("#maintenanceStr").prop("readonly", "readonly");
            formEdit.find("#maintenanceEnd").prop("readonly", "readonly");
            formEdit.find("#maintenanceAct").prop("disabled", "disabled");

            $('#editEnvButton').attr('class', '');
            $('#editEnvButton').attr('hidden', 'hidden');
        }
        $("#addApplication").unbind("click").click(function () {
            addNewApplicationRow(system);
        });


        formEdit.modal('show');
    });

    var table = loadChangeTable(system, country, environment);

    var table = loadEventTable(system, country, environment);

    loadDatabaseTable(system, country, environment);

    loadApplicationTable(system, country, environment);

    loadDependenciesTable(system, country, environment);

    loadDeployTypeTable(system, country, environment);
}

function loadChangeTable(selectSystem, selectCountry, selectEnvironment) {
    //clear the old report content before reloading it
    $("#lastChangeList").empty();
    $("#lastChangeList").html('<table id="lastChangeTable" class="table table-hover display" name="lastChangeTable">\n\
                                            </table><div class="marginBottom20"></div>');

    //configure and create the dataTable
    var contentUrl = "ReadCountryEnvParam_log?system=" + selectSystem + "&country=" + selectCountry + "&environment=" + selectEnvironment;

    var configurations = new TableConfigurationsServerSide("lastChangeTable", contentUrl, "contentTable", aoColumnsFuncChange("lastChangeTable"), [0, "desc"]);

    var table = createDataTableWithPermissions(configurations, undefined, "#lastChangeList", undefined, true);
    refreshPopoverDocumentation("lastChangeList");

    return table;
}

function loadEventTable(selectSystem, selectCountry, selectEnvironment) {
    //clear the old report content before reloading it
    $("#lastEventList").empty();
    $("#lastEventList").html('<table id="lastEventTable" class="table table-hover display" name="lastEventTable">\n\
                                            </table><div class="marginBottom20"></div>');

    //configure and create the dataTable
    var contentUrl = "ReadBuildRevisionBatch?system=" + selectSystem + "&country=" + selectCountry + "&environment=" + selectEnvironment;

    var configurations = new TableConfigurationsServerSide("lastEventTable", contentUrl, "contentTable", aoColumnsFuncEvent("lastEventTable"), [0, "desc"]);

    var table = createDataTableWithPermissions(configurations, undefined, "#lastEventList", undefined, true);
    refreshPopoverDocumentation("lastEventList");
    return table;
}

function loadDatabaseTable(selectSystem, selectCountry, selectEnvironment) {
    $('#databaseTableBody tr').remove();
    var jqxhr = $.getJSON("ReadCountryEnvironmentDatabase", "system=" + selectSystem + "&country=" + selectCountry + "&environment=" + selectEnvironment);
    $.when(jqxhr).then(function (result) {
        $.each(result["contentTable"], function (idx, obj) {
            obj.toDelete = false;
            appendDatabaseRow(obj);
        });
        refreshPopoverDocumentation("listPanelDatabase");
    }).fail(handleErrorAjaxAfterTimeout);

}

function appendDatabaseRow(dtb) {
    var doc = new Doc();
    var deleteBtn = $("<button type=\"button\"></button>").addClass("btn btn-default btn-xs").append($("<span></span>").addClass("glyphicon glyphicon-trash"));
    var selectDatabase = getSelectInvariant("PROPERTYDATABASE", false);
    var connectionPoolInput = $("<input  maxlength=\"25\" placeholder=\"-- " + doc.getDocLabel("countryenvironmentdatabase", "ConnectionPoolName") + " --\">").addClass("form-control").val(dtb.connectionPoolName);
    var soapUrlInput = $("<input  maxlength=\"200\" placeholder=\"-- " + doc.getDocLabel("countryenvironmentdatabase", "SoapUrl") + " --\">").addClass("form-control").val(dtb.soapUrl);
    var csvUrlInput = $("<input  maxlength=\"200\" placeholder=\"-- " + doc.getDocLabel("countryenvironmentdatabase", "CsvUrl") + " --\">").addClass("form-control").val(dtb.csvUrl);
    var table = $("#databaseTableBody");

    var row = $("<tr></tr>");
    var deleteBtnRow = $("<td></td>").append(deleteBtn);
    var database = $("<td></td>").append(selectDatabase.val(dtb.database));
    var connectionPoolName = $("<td></td>").append(connectionPoolInput);
    var soapUrl = $("<td></td>").append(soapUrlInput);
    var csvUrl = $("<td></td>").append(csvUrlInput);
    deleteBtn.click(function () {
        dtb.toDelete = (dtb.toDelete) ? false : true;

        if (dtb.toDelete) {
            row.addClass("danger");
        } else {
            row.removeClass("danger");
        }
    });
    selectDatabase.change(function () {
        dtb.database = $(this).val();
    });
    connectionPoolInput.change(function () {
        dtb.connectionPoolName = $(this).val();
    });
    soapUrlInput.change(function () {
        dtb.soapUrl = $(this).val();
    });
    csvUrlInput.change(function () {
        dtb.csvUrl = $(this).val();
    });
    row.append(deleteBtnRow);
    row.append(database);
    row.append(connectionPoolName);
    row.append(soapUrl);
    row.append(csvUrl);
    dtb.database = selectDatabase.prop("value"); // Value that has been requested by dtb parameter may not exist in combo vlaues so we take the real selected value.
    row.data("database", dtb);
    table.append(row);
}

function addNewDatabaseRow() {
    var newDatabase = {
        database: "",
        connectionPoolName: "",
        soapUrl: "",
        csvUrl: "",
        toDelete: false
    };
    appendDatabaseRow(newDatabase);
    refreshPopoverDocumentation("listPanelDatabase");
}

function loadApplicationTable(selectSystem, selectCountry, selectEnvironment) {
    $('#applicationTableBody tr').remove();
    var jqxhr = $.getJSON("ReadCountryEnvironmentParameters", "system=" + selectSystem + "&country=" + selectCountry + "&environment=" + selectEnvironment);
    $.when(jqxhr).then(function (result) {
        $.each(result["contentTable"], function (idx, obj) {
            obj.toDelete = false;
            appendApplicationRow(obj, selectSystem);
        });
        refreshPopoverDocumentation("listPanelApplication");
    }).fail(handleErrorAjaxAfterTimeout);
}

function appendApplicationRow(app, selectSystem) {
    nbRow++;

    var doc = new Doc();
    var deleteBtn = $("<button type=\"button\"></button>").addClass("btn btn-default btn-xs").append($("<span></span>").addClass("glyphicon glyphicon-trash"));
    var selectApplication = getSelectApplication(selectSystem, false);
    var ipInput = $("<input maxlength=\"150\" placeholder=\"-- " + doc.getDocLabel("countryenvironmentparameters", "IP") + " --\">").addClass("form-control").val(app.ip);
    var urlInput = $("<input maxlength=\"150\" placeholder=\"-- " + doc.getDocLabel("countryenvironmentparameters", "URL") + " --\">").addClass("form-control").val(app.url);
    var poolSizeInput = $("<input maxlength=\"150\" placeholder=\"-- " + doc.getDocLabel("countryenvironmentparameters", "poolSize") + " --\">").addClass("form-control").val(app.poolSize);

    var domainInput = $("<input  maxlength=\"150\" placeholder=\"-- " + doc.getDocLabel("countryenvironmentparameters", "domain") + " --\">").addClass("form-control").val(app.domain);
    var urlLoginInput = $("<input  maxlength=\"150\" placeholder=\"-- " + doc.getDocLabel("countryenvironmentparameters", "URLLOGIN") + " --\">").addClass("form-control").val(app.urlLogin);
    var variable1 = $("<input  maxlength=\"150\" placeholder=\"-- " + doc.getDocLabel("countryenvironmentparameters", "Var1") + " --\">").addClass("form-control").val(app.var1);
    var variable2 = $("<input  maxlength=\"150\" placeholder=\"-- " + doc.getDocLabel("countryenvironmentparameters", "Var2") + " --\">").addClass("form-control").val(app.var2);
    var variable3 = $("<input  maxlength=\"150\" placeholder=\"-- " + doc.getDocLabel("countryenvironmentparameters", "Var3") + " --\">").addClass("form-control").val(app.var3);
    var variable4 = $("<input  maxlength=\"150\" placeholder=\"-- " + doc.getDocLabel("countryenvironmentparameters", "Var4") + " --\">").addClass("form-control").val(app.var4);
    var secret1Input = $("<input  maxlength=\"200\" placeholder=\"-- " + doc.getDocLabel("countryenvironmentparameters", "secret1") + " --\">").addClass("form-control").val(app.secret1);
    var secret2Input = $("<input  maxlength=\"200\" placeholder=\"-- " + doc.getDocLabel("countryenvironmentparameters", "secret2") + " --\">").addClass("form-control").val(app.secret2);
    var activeInput = $("<input  type=\"checkbox\">").addClass("form-control input-sm").prop("checked", app.isActive);
    var mobileActivity = $("<input  maxlength=\"254\" placeholder=\"-- " + doc.getDocLabel("countryenvironmentparameters", "mobileActivity") + " --\">").addClass("form-control").val(app.mobileActivity);
    var mobilePackage = $("<input  maxlength=\"254\" placeholder=\"-- " + doc.getDocLabel("countryenvironmentparameters", "mobilePackage") + " --\">").addClass("form-control").val(app.mobilePackage);

    var table = $("#applicationTableBody");

    var row = $("<tr></tr>");

    var td1 = $("<td></td>").append(deleteBtn);

    var td2 = $("<td></td>").append(selectApplication.val(app.application));

    var ipName = $("<div class='form-group col-sm-6'></div>").append("<label for='ip'>" + doc.getDocOnline("countryenvironmentparameters", "IP") + "</label>").append(ipInput);
    var urlName = $("<div class='form-group col-sm-3'></div>").append("<label for='url'>" + doc.getDocOnline("countryenvironmentparameters", "URL") + "</label>").append(urlInput);
    var poolSizeName = $("<div class='form-group col-sm-1'></div>").append("<label for='poolSize'>" + doc.getDocOnline("countryenvironmentparameters", "poolSize") + "</label>").append(poolSizeInput);
    var expandName = $("<div class='form-group col-sm-1'></div>").append("<button class='btn btn-primary' type='button' data-toggle='collapse' data-target='#col" + nbRow + "' aria-expanded='false' aria-controls='col" + nbRow + "'><span class='glyphicon glyphicon-chevron-down'></span></button>");
    var activeName = $("<div class='form-group col-sm-1'></div>").append("<label for='isActive'>" + doc.getDocOnline("countryenvironmentparameters", "isActive") + "</label>").append(activeInput);

    var loginName = $("<div class='form-group col-sm-6'></div>").append("<label for='login'>" + doc.getDocOnline("countryenvironmentparameters", "URLLOGIN") + "</label>").append(urlLoginInput);
    var domainName = $("<div class='form-group col-sm-6'></div>").append("<label for='domain'>" + doc.getDocOnline("countryenvironmentparameters", "domain") + "</label>").append(domainInput);

    var var1Name = $("<div class='form-group col-sm-4'></div>").append("<label for='var1'>" + doc.getDocOnline("countryenvironmentparameters", "Var1") + "</label>").append(variable1);
    var var2Name = $("<div class='form-group col-sm-3'></div>").append("<label for='var2'>" + doc.getDocOnline("countryenvironmentparameters", "Var2") + "</label>").append(variable2);
    var var3Name = $("<div class='form-group col-sm-4'></div>").append("<label for='var3'>" + doc.getDocOnline("countryenvironmentparameters", "Var3") + "</label>").append(variable3);
    var var4Name = $("<div class='form-group col-sm-4'></div>").append("<label for='var4'>" + doc.getDocOnline("countryenvironmentparameters", "Var4") + "</label>").append(variable4);

    var secret1Name = $("<div class='form-group col-sm-4'></div>").append("<label for='secret1'>" + doc.getDocOnline("countryenvironmentparameters", "secret1") + "</label>").append(secret1Input);
    var secret2Name = $("<div class='form-group col-sm-4'></div>").append("<label for='secret2'>" + doc.getDocOnline("countryenvironmentparameters", "secret2") + "</label>").append(secret2Input);
    var mobileActivityName = $("<div class='form-group col-sm-6'></div>").append("<label for='var4'>" + doc.getDocOnline("countryenvironmentparameters", "mobileActivity") + "</label>").append(mobileActivity);
    var mobilePackageName = $("<div class='form-group col-sm-6'></div>").append("<label for='var4'>" + doc.getDocOnline("countryenvironmentparameters", "mobilePackage") + "</label>").append(mobilePackage);

    var drow1 = $("<div class='row'></div>").append(activeName).append(ipName).append(urlName).append(poolSizeName);
    var drow2 = $("<div class='row'></div>").append(var1Name).append(var2Name).append(secret1Name).append(expandName);
    var drow3 = $("<div class='row'></div>").append(loginName).append(domainName);
    var drow4 = $("<div class='row'></div>").append(var3Name).append(var4Name).append(secret2Name);
    var drow5 = $("<div class='row'></div>").append(mobileActivityName).append(mobilePackageName);

    var panelExtra = $("<div class='collapse' id='col" + nbRow + "'></div>").append(drow3).append(drow4).append(drow5);

    var td3 = $("<td></td>").append(drow1).append(drow2).append(panelExtra);

    deleteBtn.click(function () {
        app.toDelete = (app.toDelete) ? false : true;
        if (app.toDelete) {
            row.addClass("danger");
        } else {
            row.removeClass("danger");
        }
    });
    selectApplication.change(function () {
        app.application = $(this).val();
    });
    activeInput.change(function () {
        app.isActive = $(this).prop("checked");
    });
    ipInput.change(function () {
        app.ip = $(this).val();
    });
    domainInput.change(function () {
        app.domain = $(this).val();
    });
    urlInput.change(function () {
        app.url = $(this).val();
    });
    urlLoginInput.change(function () {
        app.urlLogin = $(this).val();
    });
    variable1.change(function () {
        app.var1 = $(this).val();
    });
    variable2.change(function () {
        app.var2 = $(this).val();
    });
    variable3.change(function () {
        app.var3 = $(this).val();
    });
    variable4.change(function () {
        app.var4 = $(this).val();
    });
    secret1Input.change(function () {
        app.secret1 = $(this).val();
    });
    secret2Input.change(function () {
        app.secret2 = $(this).val();
    });
    poolSizeInput.change(function () {
        app.poolSize = $(this).val();
    });
    mobileActivity.change(function () {
        app.mobileActivity = $(this).val();
    });
    mobilePackage.change(function () {
        app.mobilePackage = $(this).val();
    });

    row.append(td1);
    row.append(td2);
    row.append(td3);

    app.application = selectApplication.prop("value"); // Value that has been requested by dtb parameter may not exist in combo vlaues so we take the real selected value.
    row.data("application", app);
    table.append(row);
}

function addNewApplicationRow(selectSystem) {
    var newApplication = {
        application: "",
        isActive: true,
        ip: "",
        domain: "",
        url: "",
        urlLogin: "",
        var1: "",
        var2: "",
        var3: "",
        var4: "",
        secret1: "",
        secret2: "",
        poolSize: 0,
        mobileActivity: "",
        mobilePackage: "",
        toDelete: false
    };
    appendApplicationRow(newApplication, selectSystem);
    refreshPopoverDocumentation("listPanelApplication");
}

function loadDependenciesTable(selectSystem, selectCountry, selectEnvironment) {
    $('#dependenciesTableBody tr').remove();
    var jqxhr = $.getJSON("ReadCountryEnvLink", "system=" + selectSystem + "&country=" + selectCountry + "&environment=" + selectEnvironment);
    $.when(jqxhr).then(function (result) {
        $.each(result["contentTable"], function (idx, obj) {
            obj.toDelete = false;
            appendDependenciesRow(obj);
        });
    }).fail(handleErrorAjaxAfterTimeout);
    refreshPopoverDocumentation("listPanelDependencies");
}

function appendDependenciesRow(env) {
    var doc = new Doc();
    var deleteBtn = $("<button type=\"button\"></button>").addClass("btn btn-default btn-xs").append($("<span></span>").addClass("glyphicon glyphicon-trash"));
    var selectSystemLnk = getSelectInvariant('SYSTEM', false);
    var selectCountryLnk = getSelectInvariant('COUNTRY', false);
    var selectEnvironmentLnk = getSelectInvariant('ENVIRONMENT', false);
    var table = $("#dependenciesTableBody");

    var row = $("<tr></tr>");
    var deleteBtnRow = $("<td></td>").append(deleteBtn);
    var systemLnk = $("<td></td>").append(selectSystemLnk.val(env.systemLink));
    var countryLnk = $("<td></td>").append(selectCountryLnk.val(env.countryLink));
    var environmentLnk = $("<td></td>").append(selectEnvironmentLnk.val(env.environmentLink));
    deleteBtn.click(function () {
        env.toDelete = (env.toDelete) ? false : true;
        if (env.toDelete) {
            row.addClass("danger");
        } else {
            row.removeClass("danger");
        }
    });
    selectSystemLnk.change(function () {
        env.systemLink = $(this).val();
    });
    selectCountryLnk.change(function () {
        env.countryLink = $(this).val();
    });
    selectEnvironmentLnk.change(function () {
        env.environmentLink = $(this).val();
    });
    row.append(deleteBtnRow);
    row.append(systemLnk);
    row.append(countryLnk);
    row.append(environmentLnk);
    env.systemLink = selectSystemLnk.prop("value"); // Value that has been requested by dtb parameter may not exist in combo vlaues so we take the real selected value.
    env.countryLink = selectCountryLnk.prop("value"); // Value that has been requested by dtb parameter may not exist in combo vlaues so we take the real selected value.
    env.environmentLink = selectEnvironmentLnk.prop("value"); // Value that has been requested by dtb parameter may not exist in combo vlaues so we take the real selected value.
    row.data("dependencies", env);
    table.append(row);
}

function addNewDependenciesRow() {
    var selectCountry = $('#editEnvModal #country').prop("value");
    var selectEnvironment = $('#editEnvModal #environment').prop("value");
    ;
    var newDependencies = {
        systemLink: "",
        countryLink: selectCountry,
        environmentLink: selectEnvironment,
        toDelete: false
    };
    appendDependenciesRow(newDependencies);
    refreshPopoverDocumentation("listPanelDependencies");
}

function loadDeployTypeTable(selectSystem, selectCountry, selectEnvironment) {
    $('#deployTypeTableBody tr').remove();
    var jqxhr = $.getJSON("ReadCountryEnvDeployType", "system=" + selectSystem + "&country=" + selectCountry + "&environment=" + selectEnvironment);
    $.when(jqxhr).then(function (result) {
        $.each(result["contentTable"], function (idx, obj) {
            obj.toDelete = false;
            appendDeployTypeRow(obj);
        });
    }).fail(handleErrorAjaxAfterTimeout);
}

function appendDeployTypeRow(depTyp) {
    var doc = new Doc();
    var deleteBtn = $("<button  type=\"button\"></button>").addClass("btn btn-default btn-xs").append($("<span></span>").addClass("glyphicon glyphicon-trash"));
    var selectDeployType = getSelectDeployType();
    var jenkinsAgentInput = $("<input  maxlength=\"50\" placeholder=\"-- " + doc.getDocLabel("countryenvdeploytype", "JenkinsAgent") + " --\">").addClass("form-control").val(depTyp.jenkinsAgent);
    var table = $("#deployTypeTableBody");

    var row = $("<tr></tr>");
    var deleteBtnRow = $("<td></td>").append(deleteBtn);
    var depType = $("<td></td>").append(selectDeployType.val(depTyp.deployType));
    var jenkinsAgent = $("<td></td>").append(jenkinsAgentInput);
    deleteBtn.click(function () {
        depTyp.toDelete = (depTyp.toDelete) ? false : true;
        if (depTyp.toDelete) {
            row.addClass("danger");
        } else {
            row.removeClass("danger");
        }
    });
    selectDeployType.change(function () {
        depTyp.deployType = $(this).val();
    });
    jenkinsAgentInput.change(function () {
        depTyp.jenkinsAgent = $(this).val();
    });
    row.append(deleteBtnRow);
    row.append(depType);
    row.append(jenkinsAgent);
    depTyp.deployType = selectDeployType.prop("value"); // Value that has been requested by depTyp parameter may not exist in combo vlaues so we take the real selected value.
    row.data("deployType", depTyp);
    table.append(row);
}

function addNewDeployTypeRow() {
    var newDeployType = {
        deployType: "",
        jenkinsAgent: "",
        toDelete: false
    };
    appendDeployTypeRow(newDeployType);
}

function eventEnableClick(system, country, environment, build, revision) {
    clearResponseMessageMainPage();
    var formEvent = $('#eventEnableModal');
    formEvent.find("#system").prop("value", system);
    formEvent.find("#country").prop("value", country);
    formEvent.find("#environment").prop("value", environment);
    formEvent.find("#currentBuild").prop("value", build);
    formEvent.find("#currentRevision").prop("value", revision);
    formEvent.find('#newBuild').val(build);
    formEvent.find('#newRevision').val(revision);
    // Select next Revision.
    var sRev = $('#newRevision option:selected').next().val();
    $('#newRevision').prop("value", sRev);
    $("#eventEnableButton").prop("disabled", "disabled");

    // Clean Old field values.
    var formEvent = $('#eventEnableModal');
    // Clean old html data
    formEvent.find("#notifTo").prop("value", '');
    formEvent.find("#notifCc").prop("value", '');
    formEvent.find("#notifSubject").prop("value", '');
    formEvent.find("#notifBody div").remove();
    $('#installInstructionsTableBody tr').remove();

    formEvent.modal('show');
}

function eventEnablePreview() {
    var formEvent = $('#eventEnableModal');
    // Clean old html data
    formEvent.find("#notifTo").prop("value", '');
    formEvent.find("#notifCc").prop("value", '');
    formEvent.find("#notifSubject").prop("value", '');
    formEvent.find("#notifBody div").remove();
    var system = formEvent.find("#system").prop("value");
    var country = formEvent.find("#country").prop("value");
    var environment = formEvent.find("#environment").prop("value");
    var build = formEvent.find("#newBuild").val();
    var revision = formEvent.find("#newRevision").val();
    // Email Preview tab refresh
    showLoaderInModal('#eventEnableModal');
    var jqxhr = $.getJSON("GetNotification", "event=newbuildrevision" + "&system=" + system + "&country=" + country + "&environment=" + environment + "&build=" + build + "&revision=" + revision);
    $.when(jqxhr).then(function (data) {
        hideLoaderInModal('#eventEnableModal');
        formEvent.find("#notifTo").prop("value", data.notificationTo);
        formEvent.find("#notifCc").prop("value", data.notificationCC);
        formEvent.find("#notifSubject").prop("value", data.notificationSubject);
        // We force the table to be smaller than 950px.
        formEvent.find("#notifBody").append("<div><table><tbody><tr><td style=\"max-width: 950px;\">" + data.notificationBody + "</td></tr></tbody></table></div>");
        $("#eventEnableButton").removeAttr("disabled");
    }).fail(handleErrorAjaxAfterTimeout);
    // Installation instructions tab refresh
    refreshlistInstallInstructions();
}

function eventEnableModalCloseHandler() {
    // reset form values
    $('#eventEnableModal #eventEnableModalForm')[0].reset();
    // remove all errors on the form fields
    $(this).find('div.has-error').removeClass("has-error");
    // clear the response messages of the modal
    clearResponseMessage($('#eventEnableModal'));
    // Clean old html data
    $('#notifBody div').remove();
    console.debug("Closed.");
}

function eventEnableModalConfirmHandler() {
    console.debug("Confirmed.");
    var formEvent = $('#eventEnableModal');
    var system = formEvent.find("#system").prop("value");
    var country = formEvent.find("#country").prop("value");
    var environment = formEvent.find("#environment").prop("value");
    var build = formEvent.find("#newBuild").val();
    var revision = formEvent.find("#newRevision").val();

    showLoaderInModal('#eventEnableModal');
    var jqxhr = $.getJSON("NewBuildRev", "system=" + system + "&country=" + country + "&environment=" + environment + "&build=" + build + "&revision=" + revision);
    $.when(jqxhr).then(function (data) {
        console.debug("Email Sent.");
        hideLoaderInModal('#eventEnableModal');
        if (getAlertType(data.messageType) === "success") {
            var oTable = $("#environmentsTable").dataTable();
            oTable.fnDraw(false);
            $('#eventEnableModal').modal('hide');
            showMessage(data);
        } else {
            showMessage(data, $('#eventEnableModal'));
        }
    });
}

function refreshlistInstallInstructions() {
    $('#installInstructionsTableBody tr').remove();

    var formEdit = $('#eventEnableModal');

    var selectCountry = formEdit.find("#country").val();
    var selectEnvironment = formEdit.find("#environment").val();

    var selectBuildFrom = $("#currentBuild").val();
    var selectRevisionFrom = $("#currentRevision").val();
    var selectBuildTo = $("#newBuild").val();
    var selectRevisionTo = $("#newRevision").val();


    var jqxhr = $.getJSON("ReadBuildRevisionParameters", "system=" + getUser().defaultSystem + "&country=" + selectCountry + "&environment=" + selectEnvironment + "&lastbuild=" + selectBuildFrom + "&lastrevision=" + selectRevisionFrom
            + "&build=" + selectBuildTo + "&revision=" + selectRevisionTo + "&getSVNRelease");
    $.when(jqxhr).then(function (result) {
        $.each(result["contentTable"], function (idx, obj) {
            appendNewInstallRow(obj.build, obj.revision, obj.application, obj.appDeployType, obj.release, "", obj.mavenVersion, obj.install);
        });
    }).fail(handleErrorAjaxAfterTimeout);

    var jqxhr = $.getJSON("ReadBuildRevisionParameters", "system=" + getUser().defaultSystem + "&lastbuild=" + selectBuildFrom + "&lastrevision=" + selectRevisionFrom
            + "&build=" + selectBuildTo + "&revision=" + selectRevisionTo + "&getNonSVNRelease");
    $.when(jqxhr).then(function (result) {
        $.each(result["contentTable"], function (idx, obj) {
            appendNewInstallRow(obj.build, obj.revision, obj.application, obj.appDeployType, obj.release, obj.link, "", "");
        });
    }).fail(handleErrorAjaxAfterTimeout);

}

/**
 * Render 1 line on installation instructions modal.
 */
function appendNewInstallRow(build, revision, application, appdeploytype, release, link, version, install) {
    var doc = new Doc();
    if ((version === null) || (version === "undefined") || (version === ""))
        version = "";
    var link_html = "";
    if (link === "") {
        link_html = "";
    } else {
        link_html = '<a target="_blank" href="' + link + '">link <input type="checkbox" name="checklist"></a>';
    }
    var key = 0;
    if (install !== undefined) {
        if (install.length >= 1) {
            for (key in install) {
                link_html += '<a target="_blank" href="' + install[key].link + '">' + install[key].jenkinsAgent + ' <input type="checkbox" name="checklist"></a><br>';
                ;
            }
        }
    }
    //for each install instructions adds a new row
    $('#installInstructionsTableBody').append('<tr> \n\
        <td><div class="nomarginbottom form-group form-group-sm">\n\
            ' + build + '<span></span></div></td>\n\\n\
        <td><div class="nomarginbottom form-group form-group-sm">\n\
            ' + revision + '<span></span></div></td>\n\\n\
        <td><div class="nomarginbottom form-group form-group-sm">\n\
            ' + application + '<span></span> [' + appdeploytype + ']</div></td>\n\\n\
        <td><div class="nomarginbottom form-group form-group-sm">\n\
            ' + release + '<span></span></div></td>\n\\n\
        <td style="text-align:center"><div class="nomarginbottom form-group form-group-sm">' + link_html + '</div></td>\n\\n\
        <td><div class="nomarginbottom form-group form-group-sm">\n\n\
            ' + version + '</div></td>\n\
        </tr>');
}

function eventDisableClick(system, country, environment) {
    clearResponseMessageMainPage();
    showLoaderInModal('#eventDisableModal');
    var jqxhr = $.getJSON("GetNotification", "event=disableenvironment" + "&system=" + system + "&country=" + country + "&environment=" + environment);
    $.when(jqxhr).then(function (data) {
        hideLoaderInModal('#eventDisableModal');
        var formEvent = $('#eventDisableModal');

        formEvent.find("#system").prop("value", system);
        formEvent.find("#country").prop("value", country);
        formEvent.find("#environment").prop("value", environment);

        formEvent.find("#notifTo").prop("value", data.notificationTo);
        formEvent.find("#notifCc").prop("value", data.notificationCC);
        formEvent.find("#notifSubject").prop("value", data.notificationSubject);
        formEvent.find("#notifBody").append("<div>" + data.notificationBody + "</div>");
        formEvent.modal('show');
    });
}

function eventDisableModalCloseHandler() {
    // reset form values
    $('#eventDisableModal #eventDisableModalForm')[0].reset();
    // remove all errors on the form fields
    $(this).find('div.has-error').removeClass("has-error");
    // clear the response messages of the modal
    clearResponseMessage($('#eventDisableModal'));
    // Clean old html data
    $('#notifBody div').remove();
    console.debug("Closed.");
}

function eventDisableModalConfirmHandler() {
    clearResponseMessageMainPage();
    var formEvent = $('#eventDisableModal');
    var system = formEvent.find("#system").prop("value");
    var country = formEvent.find("#country").prop("value");
    var environment = formEvent.find("#environment").prop("value");

    showLoaderInModal('#eventDisableModal');
    var jqxhr = $.getJSON("DisableEnvironment", "system=" + system + "&country=" + country + "&environment=" + environment);
    $.when(jqxhr).then(function (data) {
        hideLoaderInModal('#eventDisableModal');
        if (getAlertType(data.messageType) === "success") {
            var oTable = $("#environmentsTable").dataTable();
            oTable.fnDraw(false);
            $('#eventDisableModal').modal('hide');
            showMessage(data);
        } else {
            showMessage(data, $('#eventDisableModal'));
        }
    });
}

function eventNewChainClick(system, country, environment) {
    clearResponseMessageMainPage();
    var formEvent = $('#eventNewChainModal');
    formEvent.find("#system").prop("value", system);
    formEvent.find("#country").prop("value", country);
    formEvent.find("#environment").prop("value", environment);

    $("#eventNewChainButton").prop("disabled", "disabled");

    formEvent.modal('show');
}

function eventNewChainPreview() {
    var formEvent = $('#eventNewChainModal');
    // Clean old html data
    formEvent.find("#notifTo").prop("value", '');
    formEvent.find("#notifCc").prop("value", '');
    formEvent.find("#notifSubject").prop("value", '');
    formEvent.find("#notifBody div").remove();
    var system = formEvent.find("#system").prop("value");
    var country = formEvent.find("#country").prop("value");
    var environment = formEvent.find("#environment").prop("value");
    var chain = formEvent.find("#batch").val();
    showLoaderInModal('#eventNewChainModal');
    var jqxhr = $.getJSON("GetNotification", "event=newchain" + "&system=" + system + "&country=" + country + "&environment=" + environment + "&chain=" + chain);
    $.when(jqxhr).then(function (data) {
        hideLoaderInModal('#eventNewChainModal');
        formEvent.find("#notifTo").prop("value", data.notificationTo);
        formEvent.find("#notifCc").prop("value", data.notificationCC);
        formEvent.find("#notifSubject").prop("value", data.notificationSubject);
        formEvent.find("#notifBody").append("<div>" + data.notificationBody + "</div>");
        $("#eventNewChainButton").removeAttr("disabled");
    }).fail(handleErrorAjaxAfterTimeout);
}

function eventNewChainModalCloseHandler() {
    // reset form values
    $('#eventNewChainModal #eventNewChainModalForm')[0].reset();
    // remove all errors on the form fields
    $(this).find('div.has-error').removeClass("has-error");
    // clear the response messages of the modal
    clearResponseMessage($('#eventNewChainModal'));
    // Clean old html data
    var formEvent = $('#eventNewChainModal');
    formEvent.find("#notifTo").prop("value", '');
    formEvent.find("#notifCc").prop("value", '');
    formEvent.find("#notifSubject").prop("value", '');
    formEvent.find("#notifBody div").remove();
    console.debug("Closed.");
}

function eventNewChainModalConfirmHandler() {
    console.debug("Confirmed.");
    var formEvent = $('#eventNewChainModal');
    var system = formEvent.find("#system").prop("value");
    var country = formEvent.find("#country").prop("value");
    var environment = formEvent.find("#environment").prop("value");
    var chain = formEvent.find("#batch").val();

    showLoaderInModal('#eventNewChainModal');
    var jqxhr = $.getJSON("NewChain", "system=" + system + "&country=" + country + "&environment=" + environment + "&chain=" + chain);
    $.when(jqxhr).then(function (data) {
        console.debug("Email Sent.");
        hideLoaderInModal('#eventNewChainModal');
        if (getAlertType(data.messageType) === "success") {
            var oTable = $("#environmentsTable").dataTable();
            oTable.fnDraw(false);
            $('#eventNewChainModal').modal('hide');
            showMessage(data);
        } else {
            showMessage(data, $('#eventNewChainModal'));
        }
    });
}

function aoColumnsFunc(tableId) {
    var doc = new Doc();
    var aoColumns = [
        {
            "data": null,
            "title": doc.getDocLabel("page_global", "columnAction"),
            "bSortable": false,
            "sWidth": "160px",
            "bSearchable": false,
            "mRender": function (data, type, obj) {
                var hasPermissions = $("#" + tableId).attr("hasPermissions");

                var editEnv = '<button id="editEnv" onclick="editEntryClick(\'' + obj["system"] + '\',\'' + obj["country"] + '\',\'' + obj["environment"] + '\');"\n\
                                class="editEnv btn btn-default btn-xs margin-right5" \n\
                                name="editEnv" title="' + doc.getDocLabel("page_environment", "button_edit") + '" type="button">\n\
                                <span class="glyphicon glyphicon-pencil"></span></button>';
                var viewEnv = '<button id="editEnv" onclick="editEntryClick(\'' + obj["system"] + '\',\'' + obj["country"] + '\',\'' + obj["environment"] + '\');"\n\
                                class="editEnv btn btn-default btn-xs margin-right5" \n\
                                name="editEnv" title="' + doc.getDocLabel("page_environment", "button_view") + '" type="button">\n\
                                <span class="glyphicon glyphicon-eye-open"></span></button>';
                var deleteEnv = '<button id="deleteEnv" onclick="deleteEntryClick(\'' + obj["system"] + '\',\'' + obj["country"] + '\',\'' + obj["environment"] + '\');" \n\
                                class="deleteEnv btn btn-default btn-xs margin-right25" \n\
                                name="deleteEnv" title="' + doc.getDocLabel("page_environment", "button_delete") + '" type="button">\n\
                                <span class="glyphicon glyphicon-trash"></span></button>';
                var disableEnv = '<button id="disableEnv" onclick="eventDisableClick(\'' + obj["system"] + '\',\'' + obj["country"] + '\',\'' + obj["environment"] + '\');" \n\
                                class="disableEnv btn btn-default btn-xs margin-right5" \n\
                                name="disableEnv" title="' + doc.getDocLabel("page_environment", "button_disable") + '" type="button">\n\
                                <span class="glyphicon glyphicon-remove-circle"></span></button>';
                var enableEnv = '<button id="enableEnv" onclick="eventEnableClick(\'' + obj["system"] + '\',\'' + obj["country"] + '\',\'' + obj["environment"] + '\',\'' + obj["build"] + '\',\'' + obj["revision"] + '\');;" \n\
                                class="enableEnv btn btn-default btn-xs margin-right5" \n\
                                name="enableEnv" title="' + doc.getDocLabel("page_environment", "button_enable") + '" type="button">\n\
                                <span class="glyphicon glyphicon-ok-circle"></span></button>';
                var newChainEnv = '<button id="newChainEnv" onclick="eventNewChainClick(\'' + obj["system"] + '\',\'' + obj["country"] + '\',\'' + obj["environment"] + '\');;" \n\
                                class="newChainEnv btn btn-default btn-xs margin-right5" \n\
                                name="newChainEnv" title="' + doc.getDocLabel("page_environment", "button_newChain") + '" type="button">\n\
                                NC</button>';

                var returnString = '<div class="center btn-group width160">';
                if (hasPermissions === "true") { //only draws the options if the user has the correct privileges
                    returnString += editEnv + deleteEnv;
                } else {
                    returnString += viewEnv;
                }
                if (obj["active"]) {
                    if (obj["chain"] === "Y") {
                        returnString += disableEnv + newChainEnv;
                    } else {
                        returnString += disableEnv;
                    }
                } else {
                    returnString += enableEnv;
                }
                returnString += '</div>';
                return returnString;
            }
        },
        {
            "data": "system",
            "sName": "system",
            "sWidth": "100px",
            "title": doc.getDocOnline("invariant", "SYSTEM")
        },
        {
            "data": "country",
            "sName": "country",
            "sWidth": "70px",
            "title": doc.getDocOnline("invariant", "COUNTRY")
        },
        {
            "data": "environment",
            "sName": "environment",
            "sWidth": "100px",
            "title": doc.getDocOnline("invariant", "ENVIRONMENT")
        },
        {
            "data": "description",
            "like": true,
            "sName": "description",
            "sWidth": "150px",
            "title": doc.getDocOnline("countryenvparam", "Description")
        },
        {
            "data": "envGp",
            "visible": false,
            "sName": "inv.gp1",
            "sWidth": "150px",
            "title": doc.getDocOnline("page_environment", "envgp")
        },
        {
            "data": "active",
            "sName": "active",
            "title": doc.getDocOnline("countryenvparam", "active"),
            "sDefaultContent": "",
            "sWidth": "70px",
            "className": "center",
            "mRender": function (data, type, obj) {
                if ((data === "Y") || (data)) {
                    return '<input type="checkbox" checked readonly disabled />';
                } else {
                    return '<input type="checkbox" readonly disabled />';
                }
            }
        },
        {
            "data": "build",
            "visible": false,
            "sName": "build",
            "sWidth": "80px",
            "title": doc.getDocOnline("buildrevisioninvariant", "versionname01")
        },
        {
            "data": "revision",
            "visible": false,
            "sName": "revision",
            "sWidth": "80px",
            "title": doc.getDocOnline("buildrevisioninvariant", "versionname02")
        },
        {
            "data": "type",
            "visible": false,
            "sName": "type",
            "sWidth": "80px",
            "title": doc.getDocOnline("countryenvparam", "Type")
        },
        {
            "data": "maintenanceAct",
            "visible": false,
            "sName": "maintenanceAct",
            "title": doc.getDocOnline("countryenvparam", "maintenanceact"),
            "sDefaultContent": "",
            "sWidth": "70px",
            "className": "center",
            "mRender": function (data, type, obj) {
                if ((data === "Y") || (data)) {
                    return '<input type="checkbox" checked readonly disabled />';
                } else {
                    return '<input type="checkbox" readonly disabled />';
                }
            }
        },
        {
            "data": "maintenanceStr",
            "visible": false,
            "sName": "maintenanceStr",
            "sWidth": "80px",
            "title": doc.getDocOnline("countryenvparam", "maintenancestr")
        },
        {
            "data": "maintenanceEnd",
            "visible": false,
            "sName": "maintenanceEnd",
            "sWidth": "80px",
            "title": doc.getDocOnline("countryenvparam", "maintenanceend")
        },
        {
            "data": "chain",
            "visible": false,
            "sName": "chain",
            "sWidth": "80px",
            "title": doc.getDocOnline("countryenvparam", "chain")
        }
    ];
    return aoColumns;
}

function aoColumnsFuncChange(tableId) {
    var doc = new Doc();
    var aoColumns = [
        {
            "data": "datecre",
            "sName": "datecre",
            "sWidth": "195px",
            "title": doc.getDocOnline("countryenvparam_log", "datecre"),
            "mRender": function (data, type, oObj) {
                return getDate(oObj["datecre"]);
            }
        },
        {
            "data": "description",
            "sName": "description",
            "sWidth": "360px",
            "title": doc.getDocOnline("countryenvparam_log", "Description")
        },
        {
            "data": "build",
            "sName": "build",
            "sWidth": "120px",
            "title": doc.getDocOnline("buildrevisioninvariant", "versionname01")
        },
        {
            "data": "revision",
            "sName": "revision",
            "sWidth": "120px",
            "title": doc.getDocOnline("buildrevisioninvariant", "versionname02")
        },
        {"data": "creator",
            "sName": "creator",
            "sWidth": "110px",
            "title": doc.getDocOnline("countryenvparam_log", "Creator")
        }
    ];
    return aoColumns;
}

function aoColumnsFuncEvent(tableId) {
    var doc = new Doc();
    var aoColumns = [
        {
            "data": "dateBatch",
            "sName": "dateBatch",
            "sWidth": "195px",
            "title": doc.getDocOnline("buildrevisionbatch", "dateBatch"),
            "mRender": function (data, type, oObj) {
                return getDate(oObj["dateBatch"]);
            }
        },
        {
            "data": "batch",
            "sName": "batch",
            "sWidth": "470px",
            "title": doc.getDocOnline("buildrevisionbatch", "batch")
        },
        {
            "data": "build",
            "sName": "build",
            "sWidth": "120px",
            "title": doc.getDocOnline("buildrevisionbatch", "build")
        },
        {
            "data": "revision",
            "sName": "revision",
            "sWidth": "120px",
            "title": doc.getDocOnline("buildrevisionbatch", "revision")
        }
    ];
    return aoColumns;
}


