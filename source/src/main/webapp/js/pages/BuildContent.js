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

var currentSystem = getUser().defaultSystem;
var urlBuild = "";
var urlRevision = "";
var urlApplication = "";

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

    urlBuild = GetURLParameter('build', 'ALL'); // Feed Build combo with Build list.
    urlRevision = GetURLParameter('revision', 'ALL'); // Feed Revision combo with Revision list.
    urlApplication = GetURLParameter('application', 'ALL');

    loadCombo();

    displayInvariantList('system', "SYSTEM", false, getUser().defaultSystem);
    var select = $('#selectApplication');
    select.empty();
    select.append($('<option></option>').text("-- ALL --").val("ALL"));
    displayApplicationList("application", currentSystem, urlApplication, undefined);

    $("#selectSystem").on('change', function (data) {
        console.info($("#selectSystem").val());
//        var jqxhr = $.getJSON("ReadApplication", "application=" + $("#selectApplication").val());
//        $.when(jqxhr).then(function (result) {
//            console.info(result["contentTable"].system);
        if (currentSystem !== $("#selectSystem").val()) {
            currentSystem = $("#selectSystem").val();

            // Feed Application combo with Application list.
            var select = $('#selectApplication');
            select.empty();
            select.append($('<option></option>').text("-- ALL --").val("ALL"));
            displayApplicationList("application", currentSystem, urlApplication, undefined);

            loadCombo();
        }
//        }).fail(handleErrorAjaxAfterTimeout);
    });

    displayUserList("releaseowner");

    var table = loadBCTable(urlBuild, urlRevision, urlApplication);
    // React on table redraw
    table.on(
            'draw.dt',
            function () {
                // Un-check the select all checkbox
                $('#selectAll')[0].checked = false;
            }
    );

    // handle the click for specific action buttons
    $("#addBrpButton").click(addEntryModalSaveHandler);
    $("#editBrpButton").click(editEntryModalSaveHandler);
    $("#massActionBrpButtonChangeBuildRevision").click(massActionModalSaveHandler_changeBuildRev);
    $("#massActionBrpButtonDelete").click(massActionModalSaveHandler_delete);

    //clear the modals fields when closed
    $('#addBrpModal').on('hidden.bs.modal', addEntryModalCloseHandler);
    $('#editBrpModal').on('hidden.bs.modal', editEntryModalCloseHandler);
    $('#massActionBrpModal').on('hidden.bs.modal', massActionModalCloseHandler);

    $('#listInstallInstructions').on('hidden.bs.modal', listInstallInstructionsModalCloseHandler);
}

function loadCombo() {
    // Filter combo
    $('#selectBuild').empty();
    $('#selectRevision').empty();

    displayBuildList('#selectBuild', currentSystem, "1", urlBuild, "Y", "Y", true);
    displayBuildList('#selectRevision', currentSystem, "2", urlRevision, "Y", "Y", true);

    // Combo in install instruction Modal
    displayBuildList('#selectBuildFrom', currentSystem, "1", urlBuild, "N", "N", false);
    displayBuildList('#selectRevisionFrom', currentSystem, "2", urlRevision, "N", "Y", false);
    displayBuildList('#selectBuildTo', currentSystem, "1", urlBuild, "N", "N", false);
    displayBuildList('#selectRevisionTo', currentSystem, "2", urlRevision, "N", "N", false);

    // Add and edit Modal combo
    displayBuildList('#addBuild', currentSystem, "1", urlBuild, "N", "Y", false);
    displayBuildList('#addRevision', currentSystem, "2", urlRevision, "N", "Y", false);
    displayBuildList('#editBuild', currentSystem, "1", urlBuild, "N", "Y", false);
    displayBuildList('#editRevision', currentSystem, "2", urlRevision, "N", "Y", false);

    // Mass Action Modal combo
    displayBuildList('#massBuild', currentSystem, "1", null, "N", "Y", false);
    displayBuildList('#massRevision', currentSystem, "2", null, "N", "Y", false);

}

function displayPageLabel() {
    var doc = new Doc();

    displayHeaderLabel(doc);
    $("#pageTitle").html(doc.getDocLabel("page_buildcontent", "title"));
    $("#title").html(doc.getDocOnline("page_buildcontent", "title"));
    $("[name='createBrpField']").html(doc.getDocLabel("page_buildcontent", "button_create"));
    $("[name='confirmationField']").html(doc.getDocLabel("page_buildcontent", "button_delete"));
    $("[name='editBrpField']").html(doc.getDocLabel("page_buildcontent", "button_edit"));
    $("[name='buttonAdd']").html(doc.getDocLabel("page_global", "buttonAdd"));
    $("[name='buttonClose']").html(doc.getDocLabel("page_global", "buttonClose"));
    $("[name='buttonConfirm']").html(doc.getDocLabel("page_global", "buttonConfirm"));
    $("[name='buttonDismiss']").html(doc.getDocLabel("page_global", "buttonDismiss"));
    $("[name='filtersField']").html(doc.getDocOnline("page_buildcontent", "filters"));
    $("[name='shortcutsField']").html(doc.getDocOnline("page_buildcontent", "standardfilters"));
    $("[name='listField']").html(doc.getDocOnline("page_buildcontent", "list"));
    $("[name='btnLoad']").html(doc.getDocLabel("page_global", "buttonLoad"));
    $("[name='btnLoadPending']").html(doc.getDocLabel("page_buildcontent", "buttonLoadPending"));
    $("[name='btnLoadLatest']").html(doc.getDocLabel("page_buildcontent", "buttonLoadLatest"));
    $("[name='btnLoadAll']").html(doc.getDocLabel("page_buildcontent", "buttonLoadAll"));
    $("[name='btnLoadAll']").html(doc.getDocLabel("page_buildcontent", "buttonLoadAll"));
    $("[name='btnViewInstall']").html(doc.getDocLabel("page_buildcontent", "buttonInstallInstruction"));

    $("[name='idField']").html(doc.getDocOnline("buildrevisionparameters", "id"));
    $("[name='buildField']").html(doc.getDocOnline("buildrevisionparameters", "Build"));
    $("[name='revisionField']").html(doc.getDocOnline("buildrevisionparameters", "Revision"));
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
    $("[name='repositoryUrlField']").html(doc.getDocOnline("buildrevisionparameters", "repositoryUrl"));
    $("[name='massActionBrpField']").html(doc.getDocOnline("page_buildcontent", "massAction"));

    $("[name='buildHeader']").html(doc.getDocOnline("buildrevisionparameters", "Build"));
    $("[name='revisionHeader']").html(doc.getDocOnline("buildrevisionparameters", "Revision"));
    $("[name='applicationHeader']").html(doc.getDocOnline("buildrevisionparameters", "application"));
    $("[name='releaseHeader']").html(doc.getDocOnline("buildrevisionparameters", "Release"));
    $("[name='linkHeader']").html(doc.getDocOnline("buildrevisionparameters", "Link"));
    $("[name='versionHeader']").html(doc.getDocOnline("buildrevisionparameters", "mavenVersion"));

    $("[name='buildFieldFrom']").html(doc.getDocOnline("page_buildcontent", "buildFrom"));
    $("[name='buildFieldTo']").html(doc.getDocOnline("page_buildcontent", "buildTo"));

    $("[name='listInstallInstructionsModalLabel']").html(doc.getDocOnline("page_buildcontent", "InstallInstructions"));


    displayFooter(doc);
}

function loadBCTable(selectBuild, selectRevision, selectApplication) {

    if (isEmpty(selectBuild)) {
        selectBuild = $("#selectBuild").val();
    }
    if (isEmpty(selectRevision)) {
        selectRevision = $("#selectRevision").val();
    }
    if (isEmpty(selectApplication)) {
        selectApplication = $("#selectApplication").val();
    }

    // We add the Browser history.
    var CallParam = '?';
    if (!isEmptyorALL(selectBuild))
        CallParam += 'build=' + encodeURIComponent(selectBuild);
    if (!isEmptyorALL(selectRevision))
        CallParam += '&revision=' + encodeURIComponent(selectRevision);
    if (!isEmptyorALL(selectApplication))
        CallParam += '&application=' + encodeURIComponent(selectApplication);
    InsertURLInHistory('BuildContent.jsp' + CallParam);

    //clear the old report content before reloading it
    $("#buildContentList").empty();
    $("#buildContentList").html('<table id="buildrevisionparametersTable" class="table table-hover display" name="buildrevisionparametersTable">\n\
                                            </table><div class="marginBottom20"></div>');

    //configure and create the dataTable
    var contentUrl = "ReadBuildRevisionParameters?system=" + currentSystem;
    if (selectRevision !== 'ALL') {
        contentUrl += "&revision=" + selectRevision;
    }
    if (selectBuild !== 'ALL') {
        contentUrl += "&build=" + selectBuild;
    }
    if (selectApplication !== 'ALL') {
        contentUrl += "&application=" + selectApplication;
    }

    var configurations = new TableConfigurationsServerSide("buildrevisionparametersTable", contentUrl, "contentTable", aoColumnsFunc("buildrevisionparametersTable"), [12, 'desc']);

    var table = createDataTableWithPermissions(configurations, renderOptionsForBrp, "#buildContentList", undefined, true);

    // handle the click for specific action on the list.
    $("#selectAll").click(selectAll);

    return table;
}

function renderOptionsForBrp(data) {
    var doc = new Doc();
    //check if user has permissions to perform the add and import operations
    if (data["hasPermissions"]) {

        if ($("#createBrpButton").length === 0) {
            var contentToAdd = "<div class='marginBottom10'>";
            contentToAdd += "<button id='createBrpButton' type='button' class='btn btn-default' ><span class='glyphicon glyphicon-plus-sign'></span> " + doc.getDocLabel("page_buildcontent", "button_create") + "</button>";
            contentToAdd += "<button id='createBrpMassButton' type='button' class='btn btn-default'><span class='glyphicon glyphicon-th-list'></span> " + doc.getDocLabel("page_global", "button_massAction") + "</button>";
            contentToAdd += "</div>";

            $("#buildrevisionparametersTable_wrapper #buildrevisionparametersTable_length").before(contentToAdd);
            $('#buildContentList #createBrpButton').click(addEntryClick);
            $('#buildContentList #createBrpMassButton').click(massActionClick);
        }
    }
}

function setPending() {
    var myBuild = "NONE";
    var myRevision = "NONE";
    var myAplication = "ALL";

    $('#selectBuild').val(myBuild);
    $('#selectRevision').val(myRevision);
    $('#selectApplication').val(myAplication);
    // We refresh the list.
    loadBCTable();
}

function setAll() {
    var myBuild = "ALL";
    var myRevision = "ALL";
    var myAplication = "ALL";

    $('#selectBuild').val(myBuild);
    $('#selectRevision').val(myRevision);
    $('#selectApplication').val(myAplication);
    // We refresh the list.
    loadBCTable();
}

function setLatest() {
    var myBuild = "";
    var myRevision = "";

    // We get the last build revision from ReadBuildRevisionParameters servlet with getlast parameter.
    var param = "getlast=&system=" + currentSystem;
    var jqxhr = $.get("ReadBuildRevisionParameters", param, "json");

    $.when(jqxhr).then(function (data) {
        var messageType = getAlertType(data.messageType);
        if (messageType === "success") {
            myBuild = data.contentTable.build;
            myRevision = data.contentTable.revision;
            $('#selectBuild').val(myBuild);
            $('#selectRevision').val(myRevision);
            // We refresh the list.
            loadBCTable();
        } else {
            showMessageMainPage(messageType, data.message, true);
        }
    }).fail(handleErrorAjaxAfterTimeout);

}

function deleteEntryHandlerClick() {
    var id = $('#confirmationModal').find('#hiddenField1').prop("value");
    var jqxhr = $.post("DeleteBuildRevisionParameters", {id: id}, "json");
    $.when(jqxhr).then(function (data) {
        var messageType = getAlertType(data.messageType);
        if (messageType === "success") {
            //redraw the datatable
            var oTable = $("#buildrevisionparametersTable").dataTable();
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

function deleteEntryClick(id, build, revision, release, application) {
    clearResponseMessageMainPage();
    var doc = new Doc();
    var messageComplete = doc.getDocLabel("page_buildcontent", "message_delete");
    messageComplete = messageComplete.replace("%ENTRY%", id);
    messageComplete = messageComplete.replace("%BUILD%", build);
    messageComplete = messageComplete.replace("%REVISION%", revision);
    messageComplete = messageComplete.replace("%RELEASE%", release);
    messageComplete = messageComplete.replace("%APPLI%", application);
    showModalConfirmation(deleteEntryHandlerClick, undefined, doc.getDocLabel("page_buildcontent", "button_delete"), messageComplete, id, "", "", "");
}

function addEntryModalSaveHandler() {
    var doc = new Doc();
    clearResponseMessage($('#addBrpModal'));
    var formAdd = $("#addBrpModal #addBrpModalForm");

    var nameElement = formAdd.find("#build");
    var nameElementEmpty = nameElement.prop("value") === '';
    if (nameElementEmpty) {
        var localMessage = new Message("danger", doc.getDocLabel("page_buildcontent", "message_ErrorBuild"));
        nameElement.parents("div.form-group").addClass("has-error");
        showMessage(localMessage, $('#addBrpModal'));
    } else {
        nameElement.parents("div.form-group").removeClass("has-error");
    }

    // verif if all mendatory fields are not empty
    if (nameElementEmpty)
        return;

    // Get the header data from the form.
    var dataForm = convertSerialToJSONObject(formAdd.serialize());

    showLoaderInModal('#addBrpModal');
    var jqxhr = $.post("CreateBuildRevisionParameters", dataForm);
    $.when(jqxhr).then(function (data) {
        hideLoaderInModal('#addBrpModal');
        console.log(data.messageType);
        if (getAlertType(data.messageType) === 'success') {
            var oTable = $("#buildrevisionparametersTable").dataTable();
            oTable.fnDraw(false);
            showMessage(data);
            $('#addBrpModal').modal('hide');
        } else {
            showMessage(data, $('#addBrpModal'));
        }
    }).fail(handleErrorAjaxAfterTimeout);
}

function addEntryModalCloseHandler() {
    // reset form values
    $('#addBrpModal #addBrpModalForm')[0].reset();
    // remove all errors on the form fields
    $(this).find('div.has-error').removeClass("has-error");
    // clear the response messages of the modal
    clearResponseMessage($('#addBrpModal'));
}

function addEntryClick() {
    clearResponseMessageMainPage();
    // When creating a new item, Define here the default value.
    var formAdd = $('#addBrpModal');

    // User that makes the creation is becoming the owner or the release.
    formAdd.find("#owner").prop("value", getUser().login);
    // New release goes by default to the build/revision selected in filter combos. (except when ALL)
    var myBuild = $("#selectBuild option:selected").val();
    var myRevision = $("#selectRevision option:selected").val();
    if (myBuild === 'ALL') {
        myBuild = 'NONE';
    }
    if (myRevision === 'ALL') {
        myRevision = 'NONE';
    }
    formAdd.find("#addBuild").val(myBuild);
    formAdd.find("#addRevision").val(myRevision);
    // New release goes by default to the application selected in filter combos. (except when ALL)
    var myAppli = $("#selectApplication option:selected").val();
    if (myAppli !== 'ALL') {
        formAdd.find("#application").val(myAppli);
    }

    $('#addBrpModal').modal('show');
}

function editEntryModalSaveHandler() {
    clearResponseMessage($('#editBrpModal'));
    var formEdit = $('#editBrpModal #editBrpModalForm');
    showLoaderInModal('#editBrpModal');

    var jqxhr = $.post("UpdateBuildRevisionParameters", formEdit.serialize(), "json");
    $.when(jqxhr).then(function (data) {
        // unblock when remote call returns 
        hideLoaderInModal('#editBrpModal');
        if (getAlertType(data.messageType) === "success") {
            var oTable = $("#buildrevisionparametersTable").dataTable();
            oTable.fnDraw(false);
            $('#editBrpModal').modal('hide');
            showMessage(data);

        } else {
            showMessage(data, $('#editBrpModal'));
        }
    }).fail(handleErrorAjaxAfterTimeout);
}

function editEntryModalCloseHandler() {
    // reset form values
    $('#editBrpModal #editBrpModalForm')[0].reset();
    // remove all errors on the form fields
    $(this).find('div.has-error').removeClass("has-error");
    // clear the response messages of the modal
    clearResponseMessage($('#editBrpModal'));
}

function editEntryClick(id) {
    clearResponseMessageMainPage();
    var jqxhr = $.getJSON("ReadBuildRevisionParameters", "id=" + id);
    $.when(jqxhr).then(function (data) {
        var obj = data["contentTable"];

        var formEdit = $('#editBrpModal');

        formEdit.find("#id").prop("value", id);
        formEdit.find("#editBuild").prop("value", obj["build"]);
        formEdit.find("#editRevision").prop("value", obj["revision"]);
        formEdit.find("#datecre").prop("value", obj["datecre"]);
        formEdit.find("#application").prop("value", obj["application"]);
        formEdit.find("#release").prop("value", obj["release"]);
        formEdit.find("#owner").prop("value", obj["releaseOwner"]);
        formEdit.find("#project").prop("value", obj["project"]);
        formEdit.find("#ticketIdFixed").prop("value", obj["ticketIdFixed"]);
        formEdit.find("#bugIdFixed").prop("value", obj["bugIdFixed"]);
        formEdit.find("#link").prop("value", obj["link"]);
        formEdit.find("#subject").prop("value", obj["subject"]);
        formEdit.find("#jenkinsBuildId").prop("value", obj["jenkinsBuildId"]);
        formEdit.find("#mavenGroupId").prop("value", obj["mavenGroupId"]);
        formEdit.find("#mavenArtifactId").prop("value", obj["mavenArtifactId"]);
        formEdit.find("#mavenVersion").prop("value", obj["mavenVersion"]);
        formEdit.find("#repositoryUrl").prop("value", obj["repositoryUrl"]);

        // The link information should not be entered when the release has jenkinsbuildid defined.
        if (obj["jenkinsBuildId"] === "") {
            formEdit.find("#link").removeProp("readonly");
        } else {
            formEdit.find("#link").prop("readonly", "readonly");
        }

        if (!(data["hasPermissions"])) { // If readonly, we only readonly all fields
            formEdit.find("#link").prop("readonly", "readonly");
            formEdit.find("#editBuild").prop("disabled", "disabled");
            formEdit.find("#editRevision").prop("disabled", "disabled");
            formEdit.find("#datecre").prop("readonly", "readonly");
            formEdit.find("#application").prop("disabled", "disabled");
            formEdit.find("#release").prop("readonly", "readonly");
            formEdit.find("#owner").prop("disabled", "disabled");
            formEdit.find("#project").prop("disabled", "disabled");
            formEdit.find("#ticketIdFixed").prop("readonly", "readonly");
            formEdit.find("#bugIdFixed").prop("readonly", "readonly");
            formEdit.find("#link").prop("readonly", "readonly");
            formEdit.find("#subject").prop("readonly", "readonly");
            formEdit.find("#jenkinsBuildId").prop("readonly", "readonly");
            formEdit.find("#mavenGroupId").prop("readonly", "readonly");
            formEdit.find("#mavenArtifactId").prop("readonly", "readonly");
            formEdit.find("#mavenVersion").prop("readonly", "readonly");
            formEdit.find("#repositoryUrl").prop("readonly", "readonly");
            $('#editBrpButton').attr('class', '');
            $('#editBrpButton').attr('hidden', 'hidden');
            console.debug("readonly");
        }

        formEdit.modal('show');
    });
}

/**
 * Handler that cleans the modal for editing subdata when it is closed.
 */
function listInstallInstructionsModalCloseHandler() {
    $('#installInstructionsTableBody tr').remove();
}

/**
 * Handler that cleans the modal for editing subdata when it is closed.
 */
function refreshlistInstallInstructions() {
    $('#installInstructionsTableBody tr').remove();


    var formEdit = $('#listInstallInstructions');

    var selectBuildFrom = $("#selectBuildFrom").val();
    var selectRevisionFrom = $("#selectRevisionFrom").val();
    var selectBuildTo = $("#selectBuildTo").val();
    var selectRevisionTo = $("#selectRevisionTo").val();


    var URL2param = "";
    if (selectRevisionFrom === 'NONE') {
        URL2param = "system=" + currentSystem + "&lastbuild=" + selectBuildFrom
                + "&build=" + selectBuildTo + "&revision=" + selectRevisionTo + "&getSVNRelease";
    } else {
        URL2param = "system=" + currentSystem + "&lastbuild=" + selectBuildFrom + "&lastrevision=" + selectRevisionFrom
                + "&build=" + selectBuildTo + "&revision=" + selectRevisionTo + "&getSVNRelease";
    }
    var jqxhr = $.getJSON("ReadBuildRevisionParameters", URL2param);
    $.when(jqxhr).then(function (result) {
        $.each(result["contentTable"], function (idx, obj) {
            appendNewInstallRow(obj.build, obj.revision, obj.application, obj.release, "", obj.mavenVersion);
        });
    }).fail(handleErrorAjaxAfterTimeout);


    var URL1param = "";
    if (selectRevisionFrom === 'NONE') {
        URL1param = "system=" + currentSystem + "&lastbuild=" + selectBuildFrom
                + "&build=" + selectBuildTo + "&revision=" + selectRevisionTo + "&getNonSVNRelease";
    } else {
        URL1param = "system=" + currentSystem + "&lastbuild=" + selectBuildFrom + "&lastrevision=" + selectRevisionFrom
                + "&build=" + selectBuildTo + "&revision=" + selectRevisionTo + "&getNonSVNRelease";
    }
    var jqxhr = $.getJSON("ReadBuildRevisionParameters", URL1param);
    $.when(jqxhr).then(function (result) {
        $.each(result["contentTable"], function (idx, obj) {
            appendNewInstallRow(obj.build, obj.revision, obj.application, obj.release, obj.link, "");
        });
    }).fail(handleErrorAjaxAfterTimeout);

    formEdit.modal('show');
}

/**
 * Display installation instructions modal if build and revision is defined in main screen.
 */
function displayInstallInstructions() {
    var doc = new Doc();
    clearResponseMessageMainPage();

    var selectBuild = $("#selectBuild").val();
    var selectRevision = $("#selectRevision").val();

    if ((selectBuild === 'ALL') || (selectRevision === 'ALL') || (selectBuild === 'NONE') || (selectRevision === 'NONE')) {
        var localMessage = new Message("danger", doc.getDocLabel("page_buildcontent", "message_instruction"));
        console.warn(localMessage.message);
        showMessage(localMessage, null);

    } else {

// init the select build and rev when coming from the main screen.
        $("#selectBuildFrom").prop("value", selectBuild);
        $("#selectRevisionFrom").prop("value", "NONE");
        $("#selectBuildTo").prop("value", selectBuild);
        $("#selectRevisionTo").prop("value", selectRevision);

        var formEdit = $('#listInstallInstructions');

        var jqxhr = $.getJSON("ReadBuildRevisionParameters", "system=" + currentSystem + "&build=" + selectBuild + "&revision=" + selectRevision + "&getSVNRelease");
        $.when(jqxhr).then(function (result) {
            $.each(result["contentTable"], function (idx, obj) {
                appendNewInstallRow(obj.build, obj.revision, obj.application, obj.release, "", obj.mavenVersion);
            });
        }).fail(handleErrorAjaxAfterTimeout);

        var jqxhr = $.getJSON("ReadBuildRevisionParameters", "system=" + currentSystem + "&build=" + selectBuild + "&revision=" + selectRevision + "&getNonSVNRelease");
        $.when(jqxhr).then(function (result) {
            $.each(result["contentTable"], function (idx, obj) {
                appendNewInstallRow(obj.build, obj.revision, obj.application, obj.release, obj.link, "");
            });
        }).fail(handleErrorAjaxAfterTimeout);

        formEdit.modal('show');
    }
}

/**
 * Render 1 line on installation instructions modal.
 */
function appendNewInstallRow(build, revision, application, release, link, version) {
    var doc = new Doc();
    if ((version === null) || (version === "undefined") || (version === ""))
        version = "";
    var link_html = "";
    if (link === "") {
        link_html = "";
    } else {
        link_html = '<a target="_blank" href="' + link + '">link</a>';
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

function selectAll() {
    if ($(this).prop("checked")) {
        $("[data-line='select']").prop("checked", true);
    } else {
        $("[data-line='select']").prop("checked", false);
    }
}

function massActionModalSaveHandler_changeBuildRev() {
    clearResponseMessage($('#massActionBrpModal'));

    var formNewValues = $('#massActionBrpModal #massActionBrpModalForm');
    var formList = $('#massActionForm');
    var paramSerialized = formNewValues.serialize() + "&" + formList.serialize().replace(/=on/g, '').replace(/id-/g, 'id=');

    showLoaderInModal('#massActionBrpModal');

    var jqxhr = $.post("UpdateBuildRevisionParameters", paramSerialized, "json");
    $.when(jqxhr).then(function (data) {
        // unblock when remote call returns 
        hideLoaderInModal('#massActionBrpModal');
        if ((getAlertType(data.messageType) === "success") || (getAlertType(data.messageType) === "warning")) {
            refreshTable();
            $('#massActionBrpModal').modal('hide');
            showMessage(data);
        } else {
            showMessage(data, $('#massActionBrpModal'));
        }
    }).fail(handleErrorAjaxAfterTimeout);
}

function massActionModalSaveHandler_delete() {
    clearResponseMessage($('#massActionBrpModal'));

    var formList = $('#massActionForm');
    var paramSerialized = formList.serialize().replace(/=on/g, '').replace(/id-/g, 'id=');

    showLoaderInModal('#massActionBrpModal');

    var jqxhr = $.post("DeleteBuildRevisionParameters", paramSerialized, "json");
    $.when(jqxhr).then(function (data) {
        // unblock when remote call returns 
        hideLoaderInModal('#massActionBrpModal');
        if ((getAlertType(data.messageType) === "success") || (getAlertType(data.messageType) === "warning")) {
            refreshTable();
            $('#massActionBrpModal').modal('hide');
            showMessage(data);
        } else {
            showMessage(data, $('#massActionBrpModal'));
        }
    }).fail(handleErrorAjaxAfterTimeout);
}


function massActionModalCloseHandler() {
    // reset form values
    $('#massActionBrpModal #massActionBrpModalForm')[0].reset();
    // remove all errors on the form fields
    $(this).find('div.has-error').removeClass("has-error");
    // clear the response messages of the modal
    clearResponseMessage($('#massActionBrpModal'));
}

function massActionClick() {
    var doc = new Doc();
    clearResponseMessageMainPage();
    // When creating a new item, Define here the default value.
    var formList = $('#massActionForm');
    if (formList.serialize().indexOf("id-") === -1) {
        var localMessage = new Message("danger", doc.getDocLabel("page_global", "message_massActionError"));
        showMessage(localMessage, null);
    } else {
        $('#massActionBrpModal').modal('show');
    }
}

function refreshTable() {
    $('#buildrevisionparametersTable').DataTable().draw();
}

function aoColumnsFunc(tableId) {
    var doc = new Doc();
    var aoColumns = [
        {
            "data": null,
            "title": '<input id="selectAll" title="' + doc.getDocLabel("page_global", "tooltip_massAction") + '" type="checkbox"></input>',
            "bSortable": false,
            "sWidth": "30px",
            "bSearchable": false,
            "mRender": function (data, type, obj) {
                var hasPermissions = $("#" + tableId).attr("hasPermissions");

                var selectBrp = '<input id="selectLine" \n\
                                class="selectBrp margin-right5" \n\
                                name="id-' + obj["id"] + '" data-line="select" data-id="' + obj["id"] + '" title="' + doc.getDocLabel("page_global", "tooltip_massActionLine") + '" type="checkbox">\n\
                                </input>';
                if (hasPermissions === "true") { //only draws the options if the user has the correct privileges
                    return '<div class="center btn-group width50">' + selectBrp + '</div>';
                }
                return '<div class="center btn-group width50"></div>';

            }
        },
        {
            "data": null,
            "title": doc.getDocLabel("page_global", "columnAction"),
            "bSortable": false,
            "sWidth": "80px",
            "bSearchable": false,
            "mRender": function (data, type, obj) {
                var hasPermissions = $("#" + tableId).attr("hasPermissions");

                var editBrp = '<button id="editBrp" onclick="editEntryClick(\'' + obj["id"] + '\');"\n\
                                class="editBrp btn btn-default btn-xs margin-right5" \n\
                                name="editBrp" title="' + doc.getDocLabel("page_buildcontent", "button_edit") + '" type="button">\n\
                                <span class="glyphicon glyphicon-pencil"></span></button>';
                var viewBrp = '<button id="editBrp" onclick="editEntryClick(\'' + obj["id"] + '\');"\n\
                                class="editBrp btn btn-default btn-xs margin-right5" \n\
                                name="editBrp" title="' + doc.getDocLabel("page_buildcontent", "button_edit") + '" type="button">\n\
                                <span class="glyphicon glyphicon-eye-open"></span></button>';
                var deleteBrp = '<button id="deleteBrp" onclick="deleteEntryClick(\'' + obj["id"] + '\',\'' + obj["build"] + '\',\'' + obj["revision"] + '\',\'' + obj["release"] + '\',\'' + obj["application"] + '\');" \n\
                                class="deleteBrp btn btn-default btn-xs margin-right5" \n\
                                name="deleteBrp" title="' + doc.getDocLabel("page_buildcontent", "button_delete") + '" type="button">\n\
                                <span class="glyphicon glyphicon-trash"></span></button>';
                if (hasPermissions === "true") { //only draws the options if the user has the correct privileges
                    return '<div class="center btn-group width150">' + editBrp + deleteBrp + '</div>';
                }
                return '<div class="center btn-group width150">' + viewBrp + '</div>';

            }
        },
        {
            "data": "build",
            "sName": "build",
            "sWidth": "70px",
            "title": doc.getDocOnline("buildrevisionparameters", "Build")
        },
        {
            "data": "revision",
            "sName": "revision",
            "sWidth": "70px",
            "title": doc.getDocOnline("buildrevisionparameters", "Revision")
        },
        {
            "data": "release",
            "sName": "release",
            "sWidth": "100px",
            "title": doc.getDocOnline("buildrevisionparameters", "Release")
        },
        {
            "data": "application",
            "sName": "application",
            "sWidth": "130px",
            "title": doc.getDocOnline("buildrevisionparameters", "application")
        },
        {
            "data": "project",
            "visible": false,
            "sName": "project",
            "sWidth": "80px",
            "title": doc.getDocOnline("buildrevisionparameters", "project")
        },
        {
            "data": "ticketIdFixed",
            "visible": false,
            "like": true,
            "sName": "ticketIdFixed",
            "sWidth": "80px",
            "title": doc.getDocOnline("buildrevisionparameters", "TicketIDFixed")
        },
        {
            "data": "bugIdFixed",
            "visible": false,
            "like": true,
            "sName": "bugIdFixed",
            "sWidth": "80px",
            "title": doc.getDocOnline("buildrevisionparameters", "BugIDFixed")
        },
        {
            "data": "link",
            "visible": false,
            "like": true,
            "sName": "link",
            "sWidth": "250px",
            "title": doc.getDocOnline("buildrevisionparameters", "Link"),
            "mRender": function (data, type, oObj) {
                return drawURL(data);
            }
        },
        {
            "data": "releaseOwner",
            "sName": "releaseOwner",
            "sWidth": "80px",
            "title": doc.getDocOnline("buildrevisionparameters", "ReleaseOwner")
        },
        {
            "data": "subject",
            "sName": "subject",
            "sWidth": "500px",
            "title": doc.getDocOnline("buildrevisionparameters", "subject")
        },
        {
            "data": "datecre",
            "visible": false,
            "sName": "datecre",
            "like": true,
            "sWidth": "150px",
            "title": doc.getDocOnline("buildrevisionparameters", "datecre")
        },
        {
            "data": "jenkinsBuildId",
            "visible": false,
            "like": true,
            "sName": "jenkinsBuildId",
            "sWidth": "80px",
            "title": doc.getDocOnline("buildrevisionparameters", "jenkinsBuildId")
        },
        {
            "data": "mavenGroupId",
            "visible": false,
            "like": true,
            "sName": "mavenGroupId",
            "sWidth": "80px",
            "title": doc.getDocOnline("buildrevisionparameters", "mavenGroupId")
        },
        {
            "data": "mavenArtifactId",
            "visible": false,
            "like": true,
            "sName": "mavenArtifactId",
            "sWidth": "80px",
            "title": doc.getDocOnline("buildrevisionparameters", "mavenArtifactId")
        },
        {
            "data": "mavenVersion",
            "visible": false,
            "like": true,
            "sName": "mavenVersion",
            "sWidth": "80px",
            "title": doc.getDocOnline("buildrevisionparameters", "mavenVersion")
        },
        {
            "data": "repositoryUrl",
            "visible": false,
            "like": true,
            "sName": "repositoryUrl",
            "sWidth": "200px",
            "title": doc.getDocOnline("buildrevisionparameters", "repositoryUrl")
        }
    ];
    return aoColumns;
}
