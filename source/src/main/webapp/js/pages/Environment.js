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
        initPage();
    });
});

function initPage() {
    displayPageLabel();

    var urlBuild = GetURLParameter('build'); // Feed Build combo with Build list.
    var urlRevision = GetURLParameter('revision'); // Feed Revision combo with Revision list.
    var urlCountry = GetURLParameter('country'); // Feed Country combo with Country list.
    var urlEnvironment = GetURLParameter('environment'); // Feed Environment combo with Environment list.

    appendBuildList("build", "1", urlBuild);
    appendBuildList("revision", "2", urlRevision);

    var select = $('#selectCountry');
    select.append($('<option></option>').text("-- ALL --").val("ALL"));
    displayInvariantList("country", "COUNTRY", urlCountry);

    var select = $('#selectEnvironment');
    select.append($('<option></option>').text("-- ALL --").val("ALL"));
    displayInvariantList("environment", "ENVIRONMENT", urlEnvironment);

    displayInvariantList("system", "SYSTEM");
    displayInvariantList("type", "ENVTYPE");
    displayInvariantList("maintenanceAct", "MNTACTIVE", "N");

    var table = loadEnvTable(urlCountry, urlEnvironment, urlBuild, urlRevision);
    table.fnSort([3, 'asc']);

    // handle the click for specific action buttons
    $("#addEnvButton").click(saveNewEnvHandler);
    $("#editEnvButton").click(saveUpdateEnvHandler);

    //clear the modals fields when closed
    $('#addEnvModal').on('hidden.bs.modal', addEnvModalCloseHandler);
    $('#editEnvModal').on('hidden.bs.modal', editEnvModalCloseHandler);
}

function displayPageLabel() {
    var doc = new Doc();

    displayHeaderLabel(doc);
    $("#pageTitle").html(doc.getDocLabel("page_environment", "title"));
    $("#title").html(doc.getDocOnline("page_environment", "title"));
    $("[name='createEnvField']").html(doc.getDocLabel("page_environment", "button_create"));
    $("[name='confirmationField']").html(doc.getDocLabel("page_environment", "button_delete"));
    $("[name='editEnvField']").html(doc.getDocLabel("page_environment", "button_edit"));
    $("[name='buttonAdd']").html(doc.getDocLabel("page_global", "buttonAdd"));
    $("[name='buttonClose']").html(doc.getDocLabel("page_global", "buttonClose"));
    $("[name='buttonConfirm']").html(doc.getDocLabel("page_global", "buttonConfirm"));
    $("[name='buttonDismiss']").html(doc.getDocLabel("page_global", "buttonDismiss"));
    $("[name='filtersField']").html(doc.getDocOnline("page_global", "filters"));
    $("[name='btnLoad']").html(doc.getDocLabel("page_global", "buttonLoad"));

    $("[name='countryField']").html(doc.getDocOnline("invariant", "COUNTRY"));
    $("[name='environmentField']").html(doc.getDocOnline("invariant", "ENVIRONMENT"));
    $("[name='buildField']").html(doc.getDocOnline("buildrevisioninvariant", "versionname01"));
    $("[name='revisionField']").html(doc.getDocOnline("buildrevisioninvariant", "versionname02"));

    $("[name='datecreField']").html(doc.getDocOnline("buildrevisionparameters", "datecre"));
    $("[name='applicationField']").html(doc.getDocOnline("buildrevisionparameters", "application"));
    $("[name='releaseField']").html(doc.getDocOnline("buildrevisionparameters", "Release"));
    $("[name='ownerField']").html(doc.getDocOnline("buildrevisionparameters", "ReleaseOwner"));
    $("[name='projectField']").html(doc.getDocOnline("buildrevisionparameters", "project"));
    $("[name='ticketIdFixedField']").html(doc.getDocOnline("buildrevisionparameters", "TicketIDFixed"));
    $("[name='bugIdFixedField']").html(doc.getDocOnline("buildrevisionparameters", "BugIDFixed"));
    $("[name='linkField']").html(doc.getDocOnline("buildrevisionparameters", "Link"));
    $("[name='subjectField']").html(doc.getDocOnline("buildrevisionparameters", "subject"));
    $("[name='jenkinsBuildIdField']").html(doc.getDocOnline("buildrevisionparameters", "jenkinsBuildId"));
    $("[name='mavenGroupIdField']").html(doc.getDocOnline("buildrevisionparameters", "mavenGroupId"));
    $("[name='mavenArtifactIdField']").html(doc.getDocOnline("buildrevisionparameters", "mavenArtifactId"));
    $("[name='mavenVersionField']").html(doc.getDocOnline("buildrevisionparameters", "mavenVersion"));
    displayFooter(doc);
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

            if (defaultValue !== undefined) {
                select.val(defaultValue);
            }

        },
        error: showUnexpectedError
    });
}

function loadEnvTable(selectCountry, selectEnvironment, selectBuild, selectRevision) {
    
    if (isEmpty(selectCountry)) {
        selectCountry = $("#selectCountry").val();
    }
    if (isEmpty(selectEnvironment)) {
        selectEnvironment = $("#selectEnvironment").val();
    }
    if (isEmpty(selectBuild)) {
        selectBuild = $("#selectBuild").val();
    }
    if (isEmpty(selectRevision)) {
        selectRevision = $("#selectRevision").val();
    }
    
    // We add the Browser history.
    var CallParam = '?';
    if (!isEmptyorALL(selectCountry))
        CallParam += 'country=' + encodeURIComponent(selectCountry);
    if (!isEmptyorALL(selectEnvironment))
        CallParam += '&environment=' + encodeURIComponent(selectEnvironment);
    if (!isEmptyorALL(selectBuild))
        CallParam += '&build=' + encodeURIComponent(selectBuild);
    if (!isEmptyorALL(selectRevision))
        CallParam += '&revision=' + encodeURIComponent(selectRevision)
    InsertURLInHistory('Environment1.jsp' + CallParam);

    //clear the old report content before reloading it
    $("#environmentList").empty();
    $("#environmentList").html('<table id="environmentsTable" class="table table-hover display" name="environmentsTable">\n\
                                            </table><div class="marginBottom20"></div>');

    //configure and create the dataTable
    var contentUrl = "ReadCountryEnvParam?forceList=Y&system=" + getUser().defaultSystem;
    if (selectEnvironment !== 'ALL') {
        contentUrl = contentUrl + "&environment=" + selectEnvironment;
    }
    if (selectCountry !== 'ALL') {
        contentUrl = contentUrl + "&country=" + selectCountry;
    }
    if (selectBuild !== 'ALL') {
        contentUrl = contentUrl + "&build=" + selectBuild;
    }
    if (selectRevision !== 'ALL') {
        contentUrl = contentUrl + "&revision=" + selectRevision;
    }

    var configurations = new TableConfigurationsServerSide("environmentsTable", contentUrl, "contentTable", aoColumnsFunc("environmentsTable"));

    var table = createDataTableWithPermissions(configurations, renderOptionsForEnv);
    return table;
}

function deleteEnvHandlerClick() {
    var system = $('#confirmationModal').find('#hiddenField1').prop("value");
    var country = $('#confirmationModal').find('#hiddenField2').prop("value");
    var environment = $('#confirmationModal').find('#hiddenField3').prop("value");
    var jqxhr = $.post("DeleteCountryEnvParam1", {system: system, country: country, environment: environment}, "json");
    $.when(jqxhr).then(function (data) {
        var messageType = getAlertType(data.messageType);
        if (messageType === "success") {
            //redraw the datatable
            var oTable = $("#environmentsTable").dataTable();
            oTable.fnDraw(true);
            var info = oTable.fnGetData().length;

            if (info === 1) {//page has only one row, then returns to the previous page
                oTable.fnPageChange('previous');
            }

        }
        //show message in the main page
        showMessageMainPage(messageType, data.message);
        //close confirmation window
        $('#confirmationModal').modal('hide');
    }).fail(handleErrorAjaxAfterTimeout);
}

function deleteEnv(system, country, environment) {
    clearResponseMessageMainPage();
    var doc = new Doc();
    var messageComplete = doc.getDocLabel("page_environment", "message_delete");
    messageComplete = messageComplete.replace("%SYSTEM%", system);
    messageComplete = messageComplete.replace("%COUNTRY%", country);
    messageComplete = messageComplete.replace("%ENVIRONMENT%", environment);
    showModalConfirmation(deleteEnvHandlerClick, doc.getDocLabel("page_environment", "button_delete"), messageComplete, system, country, environment, "");
}

function saveNewEnvHandler() {
    clearResponseMessage($('#addEnvModal'));
    var formAdd = $("#addEnvModal #addEnvModalForm");

    var nameElement = formAdd.find("#build");
    var nameElementEmpty = nameElement.prop("value") === '';
    if (nameElementEmpty) {
        var localMessage = new Message("danger", "Please specify the name of the build!");
        nameElement.parents("div.form-group").addClass("has-error");
        showMessage(localMessage, $('#addEnvModal'));
    } else {
        nameElement.parents("div.form-group").removeClass("has-error");
    }

    // verif if all mendatory fields are not empty
    if (nameElementEmpty)
        return;

    showLoaderInModal('#addEnvModal');
    var jqxhr = $.post("CreateCountryEnvParam1", formAdd.serialize());
    $.when(jqxhr).then(function (data) {
        hideLoaderInModal('#addEnvModal');
        console.log(data.messageType);
        if (getAlertType(data.messageType) === 'success') {
            var oTable = $("#environmentsTable").dataTable();
            oTable.fnDraw(true);
            showMessage(data);
            $('#addEnvModal').modal('hide');
        } else {
            showMessage(data, $('#addEnvModal'));
        }
    }).fail(handleErrorAjaxAfterTimeout);
}

function saveUpdateEnvHandler() {
    clearResponseMessage($('#editEnvModal'));
    var formEdit = $('#editEnvModal #editEnvModalForm');
    showLoaderInModal('#editEnvModal');

    var jqxhr = $.post("UpdateCountryEnvParam1", formEdit.serialize(), "json");
    $.when(jqxhr).then(function (data) {
        // unblock when remote call returns 
        hideLoaderInModal('#editEnvModal');
        if (getAlertType(data.messageType) === "success") {
            var oTable = $("#environmentsTable").dataTable();
            oTable.fnDraw(true);
            $('#editEnvModal').modal('hide');
            showMessage(data);

        } else {
            showMessage(data, $('#editEnvModal'));
        }
    }).fail(handleErrorAjaxAfterTimeout);
}

function addEnvModalCloseHandler() {
    // reset form values
    $('#addEnvModal #addEnvModalForm')[0].reset();
    // remove all errors on the form fields
    $(this).find('div.has-error').removeClass("has-error");
    // clear the response messages of the modal
    clearResponseMessage($('#addEnvModal'));
}

function editEnvModalCloseHandler() {
    // reset form values
    $('#editEnvModal #editEnvModalForm')[0].reset();
    // remove all errors on the form fields
    $(this).find('div.has-error').removeClass("has-error");
    // clear the response messages of the modal
    clearResponseMessage($('#editEnvModal'));
}

function CreateEnvClick() {
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

    $('#addEnvModal').modal('show');
}

function editEnv(system, country, environment) {
    clearResponseMessageMainPage();
    var jqxhr = $.getJSON("ReadCountryEnvParam", "system=" + system + "&country=" + country + "&environment=" + environment);
    $.when(jqxhr).then(function (data) {
        var obj = data["contentTable"];

        var formEdit = $('#editEnvModal');

        formEdit.find("#system").prop("value", system);
        formEdit.find("#country").prop("value", country);
        formEdit.find("#environment").prop("value", environment);
        formEdit.find("#buildNew").prop("value", obj["build"]);
        formEdit.find("#revisionNew").prop("value", obj["revision"]);
        formEdit.find("#chainNew").prop("value", obj["chain"]);
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
            console.debug("readonly");
        }

        formEdit.modal('show');
    });
}

function renderOptionsForEnv(data) {
    var doc = new Doc();
    //check if user has permissions to perform the add operations
    if (data["hasPermissions"]) {
        if ($("#createEnvButton").length === 0) {
            var contentToAdd = "<div class='marginBottom10'><button id='createEnvButton' type='button' class='btn btn-default'>\n\
            " + doc.getDocLabel("page_environment", "button_create") + "</button></div>";

            $("#environmentsTable_wrapper div.ColVis").before(contentToAdd);
            $('#environmentList #createEnvButton').click(CreateEnvClick);
        }
    }
}

function aoColumnsFunc(tableId) {
    var doc = new Doc();
    var aoColumns = [
        {"data": null,
            "title": doc.getDocLabel("page_global", "columnAction"),
            "bSortable": false,
            "sWidth": "80px",
            "bSearchable": false,
            "mRender": function (data, type, obj) {
                var hasPermissions = $("#" + tableId).attr("hasPermissions");

                var editEnv = '<button id="editEnv" onclick="editEnv(\'' + obj["system"] + '\',\'' + obj["country"] + '\',\'' + obj["environment"] + '\');"\n\
                                class="editEnv btn btn-default btn-xs margin-right5" \n\
                                name="editEnv" title="\'' + doc.getDocLabel("page_environment", "button_edit") + '\'" type="button">\n\
                                <span class="glyphicon glyphicon-pencil"></span></button>';
                var viewEnv = '<button id="editEnv" onclick="editEnv(\'' + obj["system"] + '\',\'' + obj["country"] + '\',\'' + obj["environment"] + '\');"\n\
                                class="editEnv btn btn-default btn-xs margin-right5" \n\
                                name="editEnv" title="\'' + doc.getDocLabel("page_environment", "button_edit") + '\'" type="button">\n\
                                <span class="glyphicon glyphicon-eye-open"></span></button>';
                var deleteEnv = '<button id="deleteEnv" onclick="deleteEnv(\'' + obj["system"] + '\',\'' + obj["country"] + '\',\'' + obj["environment"] + '\');" \n\
                                class="deleteEnv btn btn-default btn-xs margin-right5" \n\
                                name="deleteEnv" title="\'' + doc.getDocLabel("page_environment", "button_delete") + '\'" type="button">\n\
                                <span class="glyphicon glyphicon-trash"></span></button>';
                if (hasPermissions === "true") { //only draws the options if the user has the correct privileges
                    return '<div class="center btn-group width150">' + editEnv + deleteEnv + '</div>';
                }
                return '<div class="center btn-group width150">' + viewEnv + '</div>';
            }
        },
        {"data": "system",
            "sName": "system",
            "sWidth": "100px",
            "title": doc.getDocOnline("invariant", "SYSTEM")},
        {"data": "country",
            "sName": "country",
            "sWidth": "70px",
            "title": doc.getDocOnline("invariant", "COUNTRY")},
        {"data": "environment",
            "sName": "environment",
            "sWidth": "100px",
            "title": doc.getDocOnline("invariant", "ENVIRONMENT")},
        {"data": "description",
            "sName": "description",
            "sWidth": "150px",
            "title": doc.getDocOnline("countryenvparam", "Description")},
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
        {"data": "build",
            "sName": "build",
            "sWidth": "80px",
            "title": doc.getDocOnline("buildrevisioninvariant", "versionname01")},
        {"data": "revision",
            "sName": "revision",
            "sWidth": "80px",
            "title": doc.getDocOnline("buildrevisioninvariant", "versionname02")},
        {"data": "chain",
            "sName": "chain",
            "sWidth": "80px",
            "title": doc.getDocOnline("countryenvparam", "chain")},
        {"data": "type",
            "sName": "type",
            "sWidth": "80px",
            "title": doc.getDocOnline("countryenvparam", "Type")},
        {
            "data": "maintenanceAct",
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
        {"data": "maintenanceStr",
            "sName": "maintenanceStr",
            "sWidth": "80px",
            "title": doc.getDocOnline("countryenvparam", "maintenancestr")},
        {"data": "maintenanceEnd",
            "sName": "maintenanceEnd",
            "sWidth": "80px",
            "title": doc.getDocOnline("countryenvparam", "maintenanceend")}
    ];
    return aoColumns;
}
