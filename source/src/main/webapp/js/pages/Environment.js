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

    displayBuildList('#newBuild', getUser().defaultSystem, "1", "", "", "");
    displayBuildList('#newRevision', getUser().defaultSystem, "2", "", "", "");

    var table = loadEnvTable(urlCountry, urlEnvironment, urlBuild, urlRevision);
    table.fnSort([3, 'asc']);

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

function renderOptionsForEnv(data) {
    var doc = new Doc();
    //check if user has permissions to perform the add operations
    if (data["hasPermissions"]) {
        if ($("#createEnvButton").length === 0) {
            var contentToAdd = "<div class='marginBottom10'><button id='createEnvButton' type='button' class='btn btn-default'>\n\
            " + doc.getDocLabel("page_environment", "button_create") + "</button></div>";

            $("#environmentsTable_wrapper div.ColVis").before(contentToAdd);
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

            if (defaultValue !== undefined) {
                select.val(defaultValue);
            }

        },
        error: showUnexpectedError
    });
}

function deleteEntryHandlerClick() {
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

function deleteEntryClick(system, country, environment) {
    clearResponseMessageMainPage();
    var doc = new Doc();
    var messageComplete = doc.getDocLabel("page_environment", "message_delete");
    messageComplete = messageComplete.replace("%SYSTEM%", system);
    messageComplete = messageComplete.replace("%COUNTRY%", country);
    messageComplete = messageComplete.replace("%ENVIRONMENT%", environment);
    showModalConfirmation(deleteEntryHandlerClick, doc.getDocLabel("page_environment", "button_delete"), messageComplete, system, country, environment, "");
}

function addEntryModalSaveHandler() {
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
        }

        formEdit.modal('show');
    });

    var table = loadChangeTable(system, country, environment);
    table.fnSort([0, 'desc']);

    var table = loadEventTable(system, country, environment);
    table.fnSort([0, 'desc']);
}

function loadChangeTable(selectSystem, selectCountry, selectEnvironment) {
    //clear the old report content before reloading it
    $("#lastChangeList").empty();
    $("#lastChangeList").html('<table id="lastChangeTable" class="table table-hover display" name="lastChangeTable">\n\
                                            </table><div class="marginBottom20"></div>');

    //configure and create the dataTable
    var contentUrl = "ReadCountryEnvParam_log?system=" + selectSystem + "&country=" + selectCountry + "&environment=" + selectEnvironment;

    var configurations = new TableConfigurationsServerSide("lastChangeTable", contentUrl, "contentTable", aoColumnsFuncChange("lastChangeTable"));

    var table = createDataTableWithPermissions(configurations, null);
    return table;
}

function loadEventTable(selectSystem, selectCountry, selectEnvironment) {
    //clear the old report content before reloading it
    $("#lastEventList").empty();
    $("#lastEventList").html('<table id="lastEventTable" class="table table-hover display" name="lastEventTable">\n\
                                            </table><div class="marginBottom20"></div>');

    //configure and create the dataTable
    var contentUrl = "ReadBuildRevisionBatch?system=" + selectSystem + "&country=" + selectCountry + "&environment=" + selectEnvironment;

    var configurations = new TableConfigurationsServerSide("lastEventTable", contentUrl, "contentTable", aoColumnsFuncEvent("lastEventTable"));

    var table = createDataTableWithPermissions(configurations, null);
    return table;
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
    var jqxhr = $.getJSON("GetNotification", "event=newbuildrevision" + "&system=" + system + "&country=" + country + "&environment=" + environment + "&build=" + build + "&revision=" + revision);
    $.when(jqxhr).then(function (data) {
        formEvent.find("#notifTo").prop("value", data.notificationTo);
        formEvent.find("#notifCc").prop("value", data.notificationCC);
        formEvent.find("#notifSubject").prop("value", data.notificationSubject);
        // We force the table to be smaller than 950px.
        formEvent.find("#notifBody").append("<div><table><tbody><tr><td style=\"max-width: 950px;\">" + data.notificationBody + "</td></tr></tbody></table></div>");
        $("#eventEnableButton").removeProp("disabled");
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

    var jqxhr = $.getJSON("NewBuildRev1", "system=" + system + "&country=" + country + "&environment=" + environment + "&build=" + build + "&revision=" + revision);
    $.when(jqxhr).then(function (data) {
        console.debug("Email Sent.");
        hideLoaderInModal('#eventEnableModal');
        if (getAlertType(data.messageType) === "success") {
            var oTable = $("#environmentsTable").dataTable();
            oTable.fnDraw(true);
            $('#eventEnableModal').modal('hide');
            showMessage(data);
        } else {
            showMessage(data, $('#eventEnableModal'));
        }
    });
}

/**
 * Handler that cleans the modal for editing subdata when it is closed.
 */
function refreshlistInstallInstructions() {
    console.debug("Refresh install nstructions.");

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
            appendNewInstallRow(obj.build, obj.revision, obj.application, obj.release, "", obj.mavenVersion, obj.install);
        });
    }).fail(handleErrorAjaxAfterTimeout);

    var jqxhr = $.getJSON("ReadBuildRevisionParameters", "system=" + getUser().defaultSystem + "&lastbuild=" + selectBuildFrom + "&lastrevision=" + selectRevisionFrom
            + "&build=" + selectBuildTo + "&revision=" + selectRevisionTo + "&getNonSVNRelease");
    $.when(jqxhr).then(function (result) {
        $.each(result["contentTable"], function (idx, obj) {
            appendNewInstallRow(obj.build, obj.revision, obj.application, obj.release, obj.link, "", "");
        });
    }).fail(handleErrorAjaxAfterTimeout);

}

/**
 * Render 1 line on installation instructions modal.
 */
function appendNewInstallRow(build, revision, application, release, link, version, install) {
    var doc = new Doc();
    if ((version === null) || (version === "undefined") || (version === ""))
        version = "";
    var link_html = "";
    if (link === "") {
        link_html = "";
    } else {
        link_html = '<a target="_blank" href="' + link + '">link <input type="checkbox" name="checklist"></a>';
    }
    var key=0;
    if (install.length >= 1) {
        for (key in install) {
            link_html += '<a target="_blank" href="' + install[key].link + '">'+install[key].jenkinsAgent+' <input type="checkbox" name="checklist"></a>';;
        }
    }
    //for each install instructions adds a new row
    $('#installInstructionsTableBody').append('<tr> \n\
        <td><div class="nomarginbottom form-group form-group-sm">\n\
            <input readonly name="build" type="text" class="releaseClass form-control input-xs" value="' + build + '"/><span></span></div></td>\n\\n\
        <td><div class="nomarginbottom form-group form-group-sm">\n\
            <input readonly name="build" type="text" class="releaseClass form-control input-xs" value="' + revision + '"/><span></span></div></td>\n\\n\
        <td><div class="nomarginbottom form-group form-group-sm">\n\
            <input readonly name="application" type="text" class="releaseClass form-control input-xs" value="' + application + '"/><span></span></div></td>\n\\n\
        <td><div class="nomarginbottom form-group form-group-sm">\n\
            <input readonly name="release" type="text" class="releaseClass form-control input-xs" value="' + release + '"/><span></span></div></td>\n\\n\
        <td style="text-align:center"><div class="nomarginbottom form-group form-group-sm">' + link_html + '</div></td>\n\\n\
        <td><div class="nomarginbottom form-group form-group-sm">\n\n\
            <input readonly name="version" type="text" class="releaseClass form-control input-xs" value="' + version + '" /></div></td>\n\
        </tr>');
}

function eventDisableClick(system, country, environment) {
    clearResponseMessageMainPage();
    var jqxhr = $.getJSON("GetNotification", "event=disableenvironment" + "&system=" + system + "&country=" + country + "&environment=" + environment);
    $.when(jqxhr).then(function (data) {
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

    var jqxhr = $.getJSON("DisableEnvironment1", "system=" + system + "&country=" + country + "&environment=" + environment);
    $.when(jqxhr).then(function (data) {
        hideLoaderInModal('#eventDisableModal');
        if (getAlertType(data.messageType) === "success") {
            var oTable = $("#environmentsTable").dataTable();
            oTable.fnDraw(true);
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
    var jqxhr = $.getJSON("GetNotification", "event=newchain" + "&system=" + system + "&country=" + country + "&environment=" + environment + "&chain=" + chain);
    $.when(jqxhr).then(function (data) {
        formEvent.find("#notifTo").prop("value", data.notificationTo);
        formEvent.find("#notifCc").prop("value", data.notificationCC);
        formEvent.find("#notifSubject").prop("value", data.notificationSubject);
        formEvent.find("#notifBody").append("<div>" + data.notificationBody + "</div>");
        $("#eventNewChainButton").removeProp("disabled");
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

    var jqxhr = $.getJSON("NewChain1", "system=" + system + "&country=" + country + "&environment=" + environment + "&chain=" + chain);
    $.when(jqxhr).then(function (data) {
        console.debug("Email Sent.");
        hideLoaderInModal('#eventNewChainModal');
        if (getAlertType(data.messageType) === "success") {
            var oTable = $("#environmentsTable").dataTable();
            oTable.fnDraw(true);
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
        {"data": null,
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
                                name="editEnv" title="' + doc.getDocLabel("page_environment", "button_edit") + '" type="button">\n\
                                <span class="glyphicon glyphicon-eye-open"></span></button>';
                var deleteEnv = '<button id="deleteEnv" onclick="deleteEntryClick(\'' + obj["system"] + '\',\'' + obj["country"] + '\',\'' + obj["environment"] + '\');" \n\
                                class="deleteEnv btn btn-default btn-xs margin-right25" \n\
                                name="deleteEnv" title="' + doc.getDocLabel("page_environment", "button_delete") + '" type="button">\n\
                                <span class="glyphicon glyphicon-trash"></span></button>';
                var disableEnv = '<button id="disableEnv" onclick="eventDisableClick(\'' + obj["system"] + '\',\'' + obj["country"] + '\',\'' + obj["environment"] + '\');" \n\
                                class="disableEnv btn btn-default btn-xs margin-right5" \n\
                                name="disableEnv" title="' + doc.getDocLabel("page_environment", "button_delete") + '" type="button">\n\
                                <span class="glyphicon glyphicon-remove-circle"></span></button>';
                var enableEnv = '<button id="enableEnv" onclick="eventEnableClick(\'' + obj["system"] + '\',\'' + obj["country"] + '\',\'' + obj["environment"] + '\',\'' + obj["build"] + '\',\'' + obj["revision"] + '\');;" \n\
                                class="enableEnv btn btn-default btn-xs margin-right5" \n\
                                name="enableEnv" title="' + doc.getDocLabel("page_environment", "button_delete") + '" type="button">\n\
                                <span class="glyphicon glyphicon-ok-circle"></span></button>';
                var newChainEnv = '<button id="newChainEnv" onclick="eventNewChainClick(\'' + obj["system"] + '\',\'' + obj["country"] + '\',\'' + obj["environment"] + '\');;" \n\
                                class="newChainEnv btn btn-default btn-xs margin-right5" \n\
                                name="newChainEnv" title="' + doc.getDocLabel("page_environment", "button_delete") + '" type="button">\n\
                                NC</button>';

                var returnString = '<div class="center btn-group width160">';
                if (hasPermissions === "true") { //only draws the options if the user has the correct privileges
                    returnString += editEnv + deleteEnv;
                } else {
                    returnString += viewEnv;
                }
                if (obj["active"]) {
                    returnString += disableEnv + newChainEnv;
                } else {
                    returnString += enableEnv;
                }
                returnString += '</div>';
                return returnString;
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

function aoColumnsFuncChange(tableId) {
    var doc = new Doc();
    var aoColumns = [
        {"data": "datecre",
            "sName": "datecre",
            "sWidth": "145px",
            "title": doc.getDocOnline("countryenvparam_log", "datecre")},
        {"data": "description",
            "sName": "description",
            "sWidth": "140px",
            "title": doc.getDocOnline("countryenvparam_log", "description")},
        {"data": "build",
            "sName": "build",
            "sWidth": "70px",
            "title": doc.getDocOnline("countryenvparam_log", "build")},
        {"data": "revision",
            "sName": "revision",
            "sWidth": "70px",
            "title": doc.getDocOnline("countryenvparam_log", "revision")},
        {"data": "creator",
            "sName": "creator",
            "sWidth": "80px",
            "title": doc.getDocOnline("countryenvparam_log", "creator")}
    ];
    return aoColumns;
}

function aoColumnsFuncEvent(tableId) {
    var doc = new Doc();
    var aoColumns = [
        {"data": "dateBatch",
            "sName": "dateBatch",
            "sWidth": "145px",
            "title": doc.getDocOnline("buildrevisionbatch", "dateBatch")},
        {"data": "batch",
            "sName": "batch",
            "sWidth": "220px",
            "title": doc.getDocOnline("buildrevisionbatch", "batch")},
        {"data": "build",
            "sName": "build",
            "sWidth": "70px",
            "title": doc.getDocOnline("buildrevisionbatch", "build")},
        {"data": "revision",
            "sName": "revision",
            "sWidth": "70px",
            "title": doc.getDocOnline("buildrevisionbatch", "revision")}
    ];
    return aoColumns;
}
