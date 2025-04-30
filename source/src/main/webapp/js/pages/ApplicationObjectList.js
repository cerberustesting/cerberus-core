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

$.when($.getScript("js/global/global.js")).then(function () {

    $(document).ready(function () {
        initPage();
    });

});

function initPage() {

    var doc = new Doc();

    var application = GetURLParameter("application");
    displayPageLabel();

    $('#editApplicationObjectModal').on('hidden.bs.modal', {
        extra: "#editApplicationObjectModalForm"
    }, buttonCloseHandler);

    // configure and create the dataTable

    var configurations = new TableConfigurationsServerSide(
            "applicationObjectsTable", "ReadApplicationObject", "contentTable", aoColumnsFunc("applicationObjectsTable"), [1, 'asc']);
    createDataTableWithPermissions(configurations, renderOptionsForApplicationObject, "#applicationObjectList", undefined, true);
    refreshPopoverDocumentation("applicationObjectList");


    if (application !== null) {
        clearIndividualFilter("applicationObjectsTable", undefined, true);
        filterOnColumn("applicationObjectsTable", "application", application);
    }

}

function displayPageLabel() {
    var doc = new Doc();

    displayHeaderLabel(doc);
    displayGlobalLabel(doc);
    $("#pageTitle").html(doc.getDocLabel("page_applicationObject", "title"));
    $("#title").html(doc.getDocOnline("page_applicationObject", "title"));
    $("[name='editApplicationObjectField']").html(
            doc.getDocLabel("page_applicationObject",
                    "editapplicationobjectfield"));
    displayFooter(doc);
}

function displayModalLabel() {
    var doc = new Doc();

    $("[name='createApplicationObjectField']").html(doc.getDocLabel("page_applicationObject", "createapplicationobjectfield"));
    $("[name='applicationField']").html(doc.getDocLabel("page_applicationObject", "applicationfield"));
    $("[name='objectField']").html(doc.getDocLabel("page_applicationObject", "objectfield"));
    $("[name='valueField']").html(doc.getDocLabel("page_applicationObject", "valuefield"));
    $("[name='screenshotfilenameField']").html(doc.getDocLabel("page_applicationObject", "screenshotfilenamefield"));
    $("[name='buttonClose']").html(doc.getDocLabel("page_applicationObject", "button_close"));
    $("[name='buttonAdd']").html(doc.getDocLabel("page_applicationObject", "button_add"));

}

function renderOptionsForApplicationObject(data) {

    var doc = new Doc();
    // check if user has permissions to perform the add and import operations
    if (data["hasPermissions"]) {
        if ($("#createApplicationObjectButton").length === 0) {
            var contentToAdd = "<div class='marginBottom10'><button id='createApplicationObjectButton' type='button' class='btn btn-default'>\n\
            <span class='glyphicon glyphicon-plus-sign'></span> "
                    + doc
                    .getDocLabel("page_applicationObject",
                            "button_create") + "</button></div>";

            $("#applicationObjectsTable_wrapper div#applicationObjectsTable_length").before(contentToAdd);
            $("#applicationObjectList #createApplicationObjectButton").off("click");
            $('#applicationObjectList #createApplicationObjectButton').click(
                    function () {
                        openModalApplicationObject(undefined, undefined, "ADD",
                                "applicationObject");
                    });
        }
    }
}

function deleteEntryHandlerClick() {
    var application = $('#confirmationModal').find('#hiddenField1').prop(
            "value");
    var object = $('#confirmationModal').find('#hiddenField2').prop("value");
    var jqxhr = $.post("DeleteApplicationObject", {
        application: application,
        object: object
    }, "json");
    $.when(jqxhr).then(function (data) {
        var messageType = getAlertType(data.messageType);
        if (messageType === "success") {
            // redraw the datatable
            var oTable = $("#applicationObjectsTable").dataTable();
            oTable.fnDraw(false);
            var info = oTable.fnGetData().length;

            if (info === 1) {// page has only one row, then returns to the
                // previous page
                oTable.fnPageChange('previous');
            }
        }
        // show message in the main page
        showMessageMainPage(messageType, data.message, false);
        // close confirmation window
        $('#confirmationModal').modal('hide');
    }).fail(handleErrorAjaxAfterTimeout);
}

function deleteEntryClick(application, object) {
    clearResponseMessageMainPage();
    var doc = new Doc();
    var messageComplete = doc.getDocLabel("page_applicationObject",
            "message_delete");
    messageComplete = messageComplete.replace("%ENTRY%", application + " - "
            + object);
    showModalConfirmation(deleteEntryHandlerClick, undefined, doc.getDocLabel(
            "page_applicationObject", "button_delete"), messageComplete,
            application, object, "", "");
}

function buttonCloseHandler(event) {
    var modalID = event.data.extra;
    $(modalID).find("#application").attr("disabled", false);
    // reset form values
    $(modalID)[0].reset();
    // remove all errors on the form fields
    $(this).find('div.has-error').removeClass("has-error");
    // clear the response messages of the modal
    clearResponseMessage($(modalID));

    updateDropzone("Drag and drop Files", '#editApplicationObjectModal');
    // reset imagePasteFromClipboard
    imagePasteFromClipboard = undefined;

}

function aoColumnsFunc(tableId) {
    var doc = new Doc();
    var aoColumns = [
        {
            "data": null,
            "title": doc.getDocLabel("page_global", "columnAction"),
            "bSortable": false,
            "bSearchable": false,
            "sWidth": "60px",
            "mRender": function (data, type, obj) {
                var hasPermissions = $("#" + tableId)
                        .attr("hasPermissions");

                var editEntry = '<button id="editEntry" onclick="openModalApplicationObject(\''
                        + obj["application"]
                        + '\', \''
                        + obj["object"]
                        + '\',\'EDIT\'  ,\'applicationObject\' );"\n\
                                    class="editApplicationObject btn btn-default btn-xs margin-right5" \n\
                                    name="editApplicationObject" title="'
                        + doc.getDocLabel("page_applicationObject",
                                "button_edit")
                        + '" type="button">\n\
                                    <span class="glyphicon glyphicon-pencil"></span></button>';
                var viewEntry = '<button id="editEntry" onclick="openModalApplicationObject(\''
                        + obj["application"]
                        + '\', \''
                        + obj["object"]
                        + '\',\'EDIT\' , \'applicationObject\');"\n\
                                    class="editApplicationObject btn btn-default btn-xs margin-right5" \n\
                                    name="editApplicationObject" title="'
                        + doc.getDocLabel("page_applicationObject",
                                "button_edit")
                        + '" type="button">\n\
                                    <span class="glyphicon glyphicon-eye-open"></span></button>';
                var deleteEntry = '<button id="deleteEntry" onclick="deleteEntryClick(\''
                        + obj["application"]
                        + '\', \''
                        + obj["object"]
                        + '\');" \n\
                                    class="deleteApplicationObject btn btn-default btn-xs margin-right5" \n\
                                    name="deleteApplicationObject" title="'
                        + doc.getDocLabel("page_applicationObject",
                                "button_delete")
                        + '" type="button">\n\
                                    <span class="glyphicon glyphicon-trash"></span></button>';
                if (hasPermissions === "true") { // only draws the
                    // options if the user
                    // has the correct
                    // privileges
                    return '<div class="center btn-group width150">'
                            + editEntry + deleteEntry + '</div>';
                }
                return '<div class="center btn-group width150">'
                        + viewEntry + '</div>';
            }
        },
        {
            "data": "application",
            "sName": "obj.application",
            "sWidth": "60px",
            "title": doc.getDocOnline("page_applicationObject", "Application")
        },
        {
            "data": "object",
            "like": true,
            "sName": "object",
            "sWidth": "80px",
            "title": doc.getDocOnline("page_applicationObject", "Object")
        },
        {
            "data": "value",
            "sName": "value",
            "like": true,
            "sWidth": "60px",
            "title": doc.getDocOnline("page_applicationObject", "Value"),
            "mRender": function (data, type, obj) {
                var currentCase = cleanErratum(obj["value"]);
                return currentCase;
            }
        },
        {
            "data": "screenshotFilename",
            "sName": "screenshotFilename",
            "like": true,
            "sWidth": "60px",
            "title": doc.getDocOnline("page_applicationObject",
                    "ScreenshotFileName"),
            "mRender": function (data, type, obj) {
                var currentCase = "<image "
                        + "onclick ='displayPictureOfMinitature(this)' "
                        + "style ='height: 25px;cursor:  pointer;'"
                        + "src='ReadApplicationObjectImage?application=" + obj["application"] + "&object=" + obj["object"] + "&time=" + new Date().getTime()
                        + "'></image>"
                return currentCase;
            }
        },
        {
            "data": "xOffset",
            "sName": "xOffset",
            "like": true,
            "sWidth": "30px",
            "title": doc.getDocOnline("page_applicationObject", "XOffset")
        },
        {
            "data": "yOffset",
            "sName": "yOffset",
            "like": true,
            "sWidth": "30px",
            "title": doc.getDocOnline("page_applicationObject", "YOffset")
        },
        {
            "data": "usrCreated",
            "visible": false,
            "sName": "usrCreated",
            "sWidth": "60px",
            "title": doc.getDocOnline("page_applicationObject", "UsrCreated")
        },
        {
            "data": "dateCreated",
            "visible": false,
            "like": true,
            "sName": "dateCreated",
            "sWidth": "80px",
            "title": doc.getDocOnline("page_applicationObject", "DateCreated"),
            "mRender": function (data, type, oObj) {
                return getDate(oObj["dateCreated"]);
            }
        },
        {
            "data": "usrModif",
            "visible": false,
            "sName": "usrModif",
            "sWidth": "60px",
            "title": doc
                    .getDocOnline("page_applicationObject", "UsrModif")
        },
        {
            "data": "dateModif",
            "like": true,
            "visible": false,
            "sName": "dateModif",
            "sWidth": "80px",
            "title": doc.getDocOnline("page_applicationObject", "DateModif"),
            "mRender": function (data, type, oObj) {
                return getDate(oObj["dateModif"]);
            }
        }];
    return aoColumns;
}

function displayPictureOfMinitature(element) {
    showPicture("screenshot", $(element).attr('src'));
}