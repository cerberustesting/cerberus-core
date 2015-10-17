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
        initBuildContentPage();
    });
});

function initBuildContentPage() {
    displayPageLabel();
    // handle the click for specific action buttons
    $("#addBrpButton").click(saveNewBrpHandler);
    $("#editBrpButton").click(saveUpdateBrpHandler);

    //clear the modals fields when closed
    $('#addBrpModal').on('hidden.bs.modal', addBrpModalCloseHandler);
    $('#editBrpModal').on('hidden.bs.modal', editBrpModalCloseHandler);

    //if the build or revision is passed as a url parameter, then it loads the table
    var urlBuild = GetURLParameter('build');
    var urlRevision = GetURLParameter('revision');
    if (urlBuild !== null) {
        $('#selectBuild option[value="' + urlBuild + '"]').attr("selected", "selected");
    }
    if (urlRevision !== null) {
        $('#selectRevision option[value="' + urlRevision + '"]').attr("selected", "selected");
    }
//    console.log('B : ' + urlBuild + ' R : ' + urlRevision);
    loadBCTable();
}

function setPending() {
    var myBuild = "NONE";
    var myRevision = "NONE";
    $('#selectBuild option[value="' + myBuild + '"]').attr("selected", true);
    $('#selectRevision option[value="' + myRevision + '"]').attr("selected", true);
//    loadBCTable();
}

function setLatest() {
    var myBuild = "";
    var myRevision = "";
    // TODO : Get the latest build and revision.
    var param = "getlast=&system=" + getUser().defaultSystem;
    var jqxhr = $.get("ReadBuildRevisionParameters", param, "json");

    $.when(jqxhr).then(function (data) {
        var messageType = getAlertType(data.messageType);
        if (messageType === "success") {
            myBuild = data.contentTable.build;
            myRevision = data.contentTable.revision;
            console.log('Latest B : ' + myBuild + ' R : ' + myRevision);
            $('#selectBuild option[value="' + myBuild + '"]').attr("selected", true);
            $('#selectRevision option[value="' + myRevision + '"]').attr("selected", true);
        } else {
            showMessageMainPage(messageType, data.message);
        }
    }).fail(handleErrorAjaxAfterTimeout);

//    loadBCTable();
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


    displayBuildList("build", getUser().defaultSystem, "1");
    displayBuildList("revision", getUser().defaultSystem, "2");
    displayApplicationList("application", getUser().defaultSystem);
    displayProjectList("project");
    displayUserList("releaseowner");
    displayFooter(doc);
}

function loadBCTable() {
    var selectBuild = $("#selectBuild option:selected").text();
    var selectRevision = $("#selectRevision option:selected").text();

    console.log('B : ' + selectBuild + ' R : ' + selectRevision);

    window.history.pushState('Tag', '', 'BuildContent.jsp?build=' + encodeURIComponent(selectBuild) + '&revision=' + encodeURIComponent(selectRevision));

    //clear the old report content before reloading it
//    if ($("#listTable_wrapper").hasClass("initialized")) {
    $("#buildContentList").empty();
    $("#buildContentList").html('<table id="buildrevisionparametersTable" class="table table-hover display" name="buildrevisionparametersTable">\n\
                                            </table><div class="marginBottom20"></div>');
//    }
    if (selectBuild !== "") {
        //configure and create the dataTable
        var param = "?system=" + getUser().defaultSystem + "&revision=" + selectRevision + "&build=" + selectBuild;
        var configurations = new TableConfigurationsServerSide("buildrevisionparametersTable", "ReadBuildRevisionParameters" + param, "contentTable", aoColumnsFunc());

        var table = createDataTableWithPermissions(configurations, renderOptionsForBrp);

    }
}

function deleteBrpHandlerClick() {
    var id = $('#confirmationModal').find('#hiddenField').prop("value");
    var jqxhr = $.post("DeleteBuildRevisionParameters", {id: id}, "json");
    $.when(jqxhr).then(function (data) {
        var messageType = getAlertType(data.messageType);
        if (messageType === "success") {
            //redraw the datatable
            var oTable = $("#buildrevisionparametersTable").dataTable();
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

function deleteBrp(id, build, revision, release, application) {
    clearResponseMessageMainPage();
    var doc = new Doc();
    var messageComplete = doc.getDocLabel("page_buildcontent", "deleteMessage");
    messageComplete = "Do you want to delete release entry %ENTRY% ?<br> NB : It correspond to the release %RELEASE% of application %APPLI% of Build %BUILD% Revision %REVISION%."
    messageComplete = doc.getDocLabel("page_buildcontent", "message_delete");
    messageComplete = messageComplete.replace("%ENTRY%", id);
    messageComplete = messageComplete.replace("%BUILD%", build);
    messageComplete = messageComplete.replace("%REVISION%", revision);
    messageComplete = messageComplete.replace("%RELEASE%", release);
    messageComplete = messageComplete.replace("%APPLI%", application);
    showModalConfirmation(deleteBrpHandlerClick, doc.getDocLabel("page_buildcontent", "button_delete"), messageComplete, id);
}

function saveNewBrpHandler() {
    clearResponseMessage($('#addBrpModal'));
    var formAdd = $("#addBrpModal #addBrpModalForm");

    var nameElement = formAdd.find("#build");
    var nameElementEmpty = nameElement.prop("value") === '';
    if (nameElementEmpty) {
        var localMessage = new Message("danger", "Please specify the name of the build!");
        nameElement.parents("div.form-group").addClass("has-error");
        showMessage(localMessage, $('#addBrpModal'));
    } else {
        nameElement.parents("div.form-group").removeClass("has-error");
    }

    // verif if all mendatory fields are not empty
    if (nameElementEmpty)
        return;

    showLoaderInModal('#addBrpModal');
    var jqxhr = $.post("CreateBuildRevisionParameters", formAdd.serialize());
    $.when(jqxhr).then(function (data) {
        hideLoaderInModal('#addBrpModal');
        console.log(data.messageType);
        if (getAlertType(data.messageType) === 'success') {
            var oTable = $("#buildrevisionparametersTable").dataTable();
            oTable.fnDraw(true);
            showMessage(data);
            $('#addBrpModal').modal('hide');
        } else {
            showMessage(data, $('#addBrpModal'));
        }
    }).fail(handleErrorAjaxAfterTimeout);
}

function saveUpdateBrpHandler() {
    clearResponseMessage($('#editBrpModal'));
    var formEdit = $('#editBrpModal #editBrpModalForm');
    showLoaderInModal('#editBrpModal');

    var jqxhr = $.post("UpdateBuildRevisionParameters", formEdit.serialize(), "json");
    $.when(jqxhr).then(function (data) {
        // unblock when remote call returns 
        hideLoaderInModal('#editBrpModal');
        if (getAlertType(data.messageType) === "success") {
            var oTable = $("#buildrevisionparametersTable").dataTable();
            oTable.fnDraw(true);
            $('#editBrpModal').modal('hide');
            showMessage(data);

        } else {
            showMessage(data, $('#editBrpModal'));
        }
    }).fail(handleErrorAjaxAfterTimeout);
}

function addBrpModalCloseHandler() {
    // reset form values
    $('#addBrpModal #addBrpModalForm')[0].reset();
    // remove all errors on the form fields
    $(this).find('div.has-error').removeClass("has-error");
    // clear the response messages of the modal
    clearResponseMessage($('#addBrpModal'));
}

function editBrpModalCloseHandler() {
    // reset form values
    $('#editBrpModal #editBrpModalForm')[0].reset();
    // remove all errors on the form fields
    $(this).find('div.has-error').removeClass("has-error");
    // clear the response messages of the modal
    clearResponseMessage($('#editBrpModal'));
}

function CreateBrpClick() {
    clearResponseMessageMainPage();
    // When creating a new item, Define here the default value.
    var formAdd = $('#addBrpModal');
    formAdd.find("#owner").prop("value", getUser().login);
    $('#addBrpModal').modal('show');
}

function editBrp(id) {
    clearResponseMessageMainPage();
    var jqxhr = $.getJSON("ReadBuildRevisionParameters", "id=" + id);
    $.when(jqxhr).then(function (data) {
        var obj = data["contentTable"];

        var formEdit = $('#editBrpModal');

        formEdit.find("#id").prop("value", id);
        formEdit.find("#build").prop("value", obj["build"]);
        formEdit.find("#revision").prop("value", obj["revision"]);
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

        formEdit.modal('show');
    });
}

function renderOptionsForBrp(data) {
    var doc = new Doc();
    //check if user has permissions to perform the add and import operations
    if (data["hasPermissions"]) {
        if ($("#createBrpButton").length === 0) {
            var contentToAdd = "<div class='marginBottom10'><button id='createBrpButton' type='button' class='btn btn-default'>\n\
            " + doc.getDocLabel("page_buildcontent", "button_create") + "</button></div>";

            $("#buildrevisionparametersTable_wrapper div.ColVis").before(contentToAdd);
            $('#buildContentList #createBrpButton').click(CreateBrpClick);
        }
    }
}

function aoColumnsFunc() {
    var doc = new Doc();
    var aoColumns = [
        {"data": null,
            "title": doc.getDocLabel("page_global", "columnAction"),
            "bSortable": false,
            "bSearchable": false,
            "mRender": function (data, type, obj) {
                var editBrp = '<button id="editBrp" onclick="editBrp(\'' + obj["id"] + '\');"\n\
                                class="editBrp btn btn-default btn-xs margin-right5" \n\
                                name="editBrp" title="\'' + doc.getDocLabel("page_buildcontent", "button_edit") + '\'" type="button">\n\
                                <span class="glyphicon glyphicon-pencil"></span></button>';
                var deleteBrp = '<button id="deleteBrp" onclick="deleteBrp(\'' + obj["id"] + '\',\'' + obj["build"] + '\',\'' + obj["revision"] + '\',\'' + obj["release"] + '\',\'' + obj["application"] + '\');" \n\
                                class="deleteBrp btn btn-default btn-xs margin-right5" \n\
                                name="deleteBrp" title="\'' + doc.getDocLabel("page_buildcontent", "button_delete") + '\'" type="button">\n\
                                <span class="glyphicon glyphicon-trash"></span></button>';

                return '<div class="center btn-group width150">' + editBrp + deleteBrp + '</div>';
            }
        },
        {"data": "build",
            "sName": "build",
            "title": doc.getDocOnline("buildrevisionparameters", "Build")},
        {"data": "revision",
            "sName": "revision",
            "title": doc.getDocOnline("buildrevisionparameters", "Revision")},
        {"data": "release",
            "sName": "release",
            "title": doc.getDocOnline("buildrevisionparameters", "Release")},
        {"data": "application",
            "sName": "application",
            "title": doc.getDocOnline("buildrevisionparameters", "application")},
        {"data": "project",
            "sName": "project",
            "title": doc.getDocOnline("buildrevisionparameters", "project")},
        {"data": "ticketIdFixed",
            "sName": "ticketIdFixed",
            "title": doc.getDocOnline("buildrevisionparameters", "TicketIDFixed")},
        {"data": "bugIdFixed",
            "sName": "bugIdFixed",
            "title": doc.getDocOnline("buildrevisionparameters", "BugIDFixed")},
        {"data": "link",
            "sName": "link",
            "title": doc.getDocOnline("buildrevisionparameters", "Link")},
        {"data": "releaseOwner",
            "sName": "releaseOwner",
            "title": doc.getDocOnline("buildrevisionparameters", "ReleaseOwner")},
        {"data": "subject",
            "sName": "subject",
            "title": doc.getDocOnline("buildrevisionparameters", "subject")},
        {"data": "datecre",
            "sName": "datecre",
            "title": doc.getDocOnline("buildrevisionparameters", "datecre")},
        {"data": "jenkinsBuildId",
            "sName": "jenkinsBuildId",
            "title": doc.getDocOnline("buildrevisionparameters", "jenkinsBuildId")},
        {"data": "mavenGroupId",
            "sName": "mavenGroupId",
            "title": doc.getDocOnline("buildrevisionparameters", "mavenGroupId")},
        {"data": "mavenArtifactId",
            "sName": "mavenArtifactId",
            "title": doc.getDocOnline("buildrevisionparameters", "mavenArtifactId")},
        {"data": "mavenVersion",
            "sName": "mavenVersion",
            "title": doc.getDocOnline("buildrevisionparameters", "mavenVersion")}
    ];
    return aoColumns;
}