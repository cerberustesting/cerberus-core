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

var imagePasteFromClipboard = undefined;//stock the picture if the user chose to upload it from his clipboard

function openModalApplicationObject(applicationObject, value, mode, page) {
    if ($('#editApplicationObjectModal').data("initLabel") === undefined) {
        if (page === "applicationObject") {
            initModalApplicationObject("applicationObject", undefined);
        } else {
            initModalApplicationObject("testCaseScript", applicationObject);
        }
        $('#editApplicationObjectModal').data("initLabel", true);
    }
    if (mode === "EDIT") {
        editApplicationObjectClick(applicationObject, value);
    } else if (mode === "ADD") {
        addApplicationObjectClick(applicationObject, value);
    }
}

function initModalApplicationObject(page, application) {

    var doc = new Doc();
    $("[name='buttonClose']").html(
            doc.getDocLabel("page_global", "buttonClose"));
    $("[name='buttonAdd']").html(doc.getDocLabel("page_global", "btn_add"));
    $("[name='buttonEdit']").html(doc.getDocLabel("page_global", "buttonEdit"));
    $("[name='addEntryField']").html(
            doc.getDocLabel("page_applicationObject", "button_create"));
    $("[name='confirmationField']").html(
            doc.getDocLabel("page_applicationObject", "button_delete"));
    $("[name='editEntryField']").html(
            doc.getDocLabel("page_applicationObject", "button_edit"));
    $("[name='objectField']").html(doc.getDocOnline("page_applicationObject", "Object"));
    $("[name='applicationField']").html(doc.getDocOnline("page_applicationObject", "Application"));
    $("[name='screenshotfilenameField']").html(doc.getDocOnline("page_applicationObject", "ScreenshotFileName"));
    $("[name='xOffsetField']").html(doc.getDocOnline("page_applicationObject", "XOffset"));
    $("[name='yOffsetField']").html(doc.getDocOnline("page_applicationObject", "YOffset"));

    // Application dropdown is now a crbDropdown component, no select2 needed
    // $('[name="application"]').select2(getComboConfigApplication(false));


    $("#editApplicationObjectButton").off("click");
    $("#editApplicationObjectButton").click(function () {
        confirmApplicationObjectModalHandler(page, "EDIT");
    });
    $("#addApplicationObjectButton").off("click");
    $("#addApplicationObjectButton").click(function () {
        confirmApplicationObjectModalHandler(page, "ADD");
    });

    // Upload drag/drop/paste now handled by Alpine crbUploadZone component
    
    refreshPopoverDocumentation("editApplicationObjectModal");
    
}

function editApplicationObjectClick(applicationObject, value) {

    clearResponseMessage($('#editApplicationObjectModal'));

    $('#editApplicationObjectButton').attr('class', 'px-3 py-2 rounded-md bg-blue-400 text-white hover:bg-blue-700 transition').removeAttr('hidden');
    $('#addApplicationObjectButton').attr('class', 'hidden').attr('hidden', 'hidden');


    $('#editApplicationObjectModalForm select[name="idname"]').off("change");
    $('#editApplicationObjectModalForm input[name="value"]').off("change");

    feedApplicationObjectModal(applicationObject, value, "editApplicationObjectModal", "EDIT");
    // Upload listeners now handled by Alpine crbUploadZone component
}

function addApplicationObjectClick(applicationObject, value) {

    clearResponseMessage($('#editApplicationObjectModal'));

    $('#addApplicationObjectButton').attr('class', 'px-3 py-2 rounded-md bg-blue-400 text-white hover:bg-blue-700 transition').removeAttr('hidden');
    $('#editApplicationObjectButton').attr('class', 'hidden').attr('hidden', 'hidden');

    $('#editApplicationObjectModalForm select[name="idname"]').off("change");
    $('#editApplicationObjectModalForm input[name="value"]').off("change");


    feedApplicationObjectModal(applicationObject, value, "editApplicationObjectModal", "ADD");
    // Upload listeners now handled by Alpine crbUploadZone component
}

function confirmApplicationObjectModalHandler(page, mode) {
    clearResponseMessage($('#editApplicationObjectModal'));

    var formEdit = $('#editApplicationObjectModal #editApplicationObjectModalForm');
    formEdit.find("#application").attr("disabled", false);

    var sa = formEdit.serializeArray();
    var formData = new FormData();

    for (var i in sa) {
        formData.append(sa[i].name, sa[i].value);
    }

    try {
        if (imagePasteFromClipboard !== undefined) {//imagePasteFromClipboard is undefined, the picture to upload should be taken inside the input
            formData.append("file", imagePasteFromClipboard);
        } else {
            var file = $("#editApplicationObjectModal input[type=file]");
            formData.append("file", file.prop("files")[0]);
        }
        ;
    } catch (e) {
    }

    // Calculate servlet name to call.
    var myServlet = "UpdateApplicationObject";
    if ((mode === "ADD")) {
        myServlet = "CreateApplicationObject";
    }

    // Get the header data from the form.

    showLoaderInModal('#editApplicationObjectModal');

    $.ajax({
        url: myServlet,
        async: true,
        method: "POST",
        data: formData,
        processData: false,
        contentType: false,
        success: function (data) {
            // data = JSON.parse(data);
            if (getAlertType(data.messageType) === "success") {
                if (page == "applicationObject") {

                    var oTable = $("#applicationObjectsTable").dataTable();
                    oTable.fnDraw(false);

                } else if (page == "testCaseScript") {

                    //TestCaseScript.js must be loaded so getTags exist
                    var Tags = getTags();
                    for (var i = 0; i < Tags.length; i++) {
                        if (Tags[i].regex == "%object\\.") {
                            Tags[i].array.push(formData.get("object"));
                        }
                    }
                    $("div.step-action .content div.fieldRow div:nth-child(n+2) input").trigger("input");

                }
                $('#editApplicationObjectModal').data("Saved", true);
                window.dispatchEvent(new CustomEvent('appobject-modal-close'));
                showMessage(data);
            } else {
                showMessage(data, $('#editApplicationObjectModal'));
            }
            hideLoaderInModal('#editApplicationObjectModal');
        },
        error: showUnexpectedError
    });
}


function feedApplicationObjectModal(application, object, modalId, mode) {
    clearResponseMessageMainPage();
    var formEdit = $('#' + modalId);

    if (mode === "EDIT") {
        $.ajax({
            url: "ReadApplicationObject",
            async: true,
            method: "POST",
            data: {
                application: application,
                object: object
            },
            success: function (data) {
                if (data.messageType === "OK") {
                    // Feed the data to the screen and manage
                    // authorities.
                    var applicationObj = data.contentTable;
                    var hasPermissions = data.hasPermissions;

                    feedApplicationObjectModalData(applicationObj, modalId, mode,
                            hasPermissions);
                    window.dispatchEvent(new CustomEvent('appobject-modal-open', { detail: {} }));
                } else {
                    showUnexpectedError();
                }
            },
            error: showUnexpectedError
        });
    } else {
        var applicationObj1 = {};
        applicationObj1.application = application;
        formEdit.find("#application").val(applicationObj1.application);
        applicationObj1.object = object;
        applicationObj1.value = "";
        applicationObj1.screenshotfilename = "Drag and drop Files";
        var hasPermissions = true;
        feedApplicationObjectModalData(applicationObj1, modalId, mode, hasPermissions);
        window.dispatchEvent(new CustomEvent('appobject-modal-open', { detail: {} }));
    }
}


function feedApplicationObjectModalData(applicationObject, modalId, mode, hasPermissionsUpdate) {
    var formEdit = $('#' + modalId);
    var doc = new Doc();
    var isEditable = (((hasPermissionsUpdate) && (mode === "EDIT")) || (mode === "ADD"));

    // Data Feed.
    if (mode === "EDIT") {
        $("[name='editApplicationObjectField']").html(doc.getDocOnline("page_global", "btn_edit"));
    } else if (mode === "ADD") { // DUPLICATE or ADD
        $("[name='editApplicationObjectField']").html(doc.getDocOnline("page_global", "btn_add"));
    }

    if (applicationObject === undefined) {
        formEdit.find("#application").val("");
        window.dispatchEvent(new CustomEvent('aoApplication-preselect', { detail: '' }));
        formEdit.find("#object").prop("value", "");
        formEdit.find("#value").prop("value", "");
        formEdit.find("#inputFile").val("Drag and drop Files");

    } else {
        formEdit.find("#originalApplication").prop("value", applicationObject.application);

        if (applicationObject.application === undefined) {
            formEdit.find('#application').val("");
            window.dispatchEvent(new CustomEvent('aoApplication-preselect', { detail: '' }));
        } else {
            formEdit.find("#application").val(applicationObject.application);
            window.dispatchEvent(new CustomEvent('aoApplication-preselect', { detail: applicationObject.application }));
        }

        // Handle screenshot preview: only show preview in EDIT mode with a real image
        if (mode === "EDIT" && applicationObject.screenshotfilename !== "" && applicationObject.application && applicationObject.object) {
            var pictureUrl = "ReadApplicationObjectImage?application=" + applicationObject.application + "&object=" + applicationObject.object + "&time=" + new Date().getTime();
            window.dispatchEvent(new CustomEvent('ao-preview-update', { detail: { name: applicationObject.screenshotFilename || '', url: pictureUrl + "&h=400&w=800" } }));
        } else {
            // ADD mode or no screenshot — show the dropzone
            window.dispatchEvent(new CustomEvent('ao-preview-reset'));
        }

        formEdit.find("#originalObject").prop("value", applicationObject.object);
        formEdit.find("#object").prop("value", applicationObject.object);
        formEdit.find("#value").prop("value", cleanErratum(applicationObject.value));
        formEdit.find("#xOffset").prop("value", applicationObject.xOffset);
        formEdit.find("#yOffset").prop("value", applicationObject.yOffset);

    }

    if (isEditable) { // If readonly, we readonly all fields
        formEdit.find("#value").prop("readonly", false);
        formEdit.find("#inputFile").attr("disabled", false);
    } else {
        formEdit.find("#value").prop("readonly", true);
        formEdit.find("#inputFile").attr("disabled", true);
        $('#editApplicationObjectButton').attr('class', '');
        $('#editApplicationObjectButton').attr('hidden', 'hidden');
    }
}

// All upload functions (drag/drop, paste, file input, preview) are now
// handled by the Alpine.js crbUploadZone component defined in ApplicationObject.html.
// The component communicates via:
//   - 'ao-preview-update' event (with {name, url}) to show preview
//   - 'ao-preview-reset' event to reset to dropzone
//   - window.imagePasteFromClipboard for AJAX submit compatibility
