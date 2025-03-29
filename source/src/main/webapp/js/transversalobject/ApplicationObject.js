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

    //displayApplicationList('application', undefined, application, undefined);
    $('[name="application"]').select2(getComboConfigApplication(false));


    $("#editApplicationObjectButton").off("click");
    $("#editApplicationObjectButton").click(function () {
        confirmApplicationObjectModalHandler(page, "EDIT");
    });
    $("#addApplicationObjectButton").off("click");
    $("#addApplicationObjectButton").click(function () {
        confirmApplicationObjectModalHandler(page, "ADD");
    });

    setUpDragAndDrop('#editApplicationObjectModal');
    
    refreshPopoverDocumentation("editApplicationObjectModal");
    
}

function editApplicationObjectClick(applicationObject, value) {

    clearResponseMessage($('#editApplicationObjectModal'));

    $('#editApplicationObjectButton').attr('class', 'btn btn-primary');
    $('#editApplicationObjectButton').removeProp('hidden');

    $('#addApplicationObjectButton').attr('class', '');
    $('#addApplicationObjectButton').attr('hidden', 'hidden');

    $('#editApplicationObjectModalForm select[name="idname"]').off("change");
    $('#editApplicationObjectModalForm input[name="value"]').off("change");

    feedApplicationObjectModal(applicationObject, value, "editApplicationObjectModal", "EDIT");
    listennerForInputTypeFile('#editApplicationObjectModal')
    pasteListennerForClipboardPicture('#editApplicationObjectModal');
}

function addApplicationObjectClick(applicationObject, value) {

    clearResponseMessage($('#editApplicationObjectModal'));

    $('#editApplicationObjectButton').attr('class', '');
    $('#editApplicationObjectButton').attr('hidden', 'hidden');

    $('#addApplicationObjectButton').attr('class', 'btn btn-primary');
    $('#addApplicationObjectButton').removeProp('hidden');

    $('#editApplicationObjectModalForm select[name="idname"]').off("change");
    $('#editApplicationObjectModalForm input[name="value"]').off("change");


    feedApplicationObjectModal(applicationObject, value, "editApplicationObjectModal", "ADD");
    listennerForInputTypeFile('#editApplicationObjectModal');
    pasteListennerForClipboardPicture('#editApplicationObjectModal');
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
                $('#editApplicationObjectModal').modal('hide');
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
                    formEdit.modal('show');
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
        formEdit.modal('show');
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
        formEdit.find('#application').val("").trigger('change');
        formEdit.find('#application').trigger('change');
        formEdit.find("#object").prop("value", "");
        formEdit.find("#value").prop("value", "");
        formEdit.find("#inputFile").val("Drag and drop Files");

    } else {
        formEdit.find("#originalApplication").prop("value", applicationObject.application);

        if (applicationObject.application === undefined) {
            formEdit.find('#application').val("").trigger('change');
        } else {
            //formEdit.find("#application").val(applicationObject.application);
            var newOption = new Option(applicationObject.application, applicationObject.application, true, true);
            formEdit.find('#application').append(newOption).trigger('change');
        }

        if (applicationObject.screenshotfilename == "") {
            updateDropzone("Drag and drop Files", "#" + modalId);
        } else {
            updateDropzone(applicationObject.screenshotFilename, "#" + modalId);
        }

        formEdit.find("#originalObject").prop("value", applicationObject.object);
        formEdit.find("#object").prop("value", applicationObject.object);
        formEdit.find("#value").prop("value", cleanErratum(applicationObject.value));
        formEdit.find("#xOffset").prop("value", applicationObject.xOffset);
        formEdit.find("#yOffset").prop("value", applicationObject.yOffset);

        pictureUrl = "ReadApplicationObjectImage?application=" + applicationObject.application + "&object=" + applicationObject.object + "&time=" + new Date().getTime()
        formEdit.find("#selectedPicture").attr("src", pictureUrl + "&h=400&w=800");

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

/**
 * add a listenner for a paste event to catch clipboard if it's a picture
 * @returns {void}
 */
function pasteListennerForClipboardPicture(idModal) {
    var _self = this;
    //handlers
    document.addEventListener('paste', function (e) {
        _self.paste_auto(e);
    }, false);
    //on paste
    this.paste_auto = function (e) {
        //handle paste event if the user do not select an input;
        if (e.clipboardData && !$(e.target).is("input")) {
            var items = e.clipboardData.items;
            handlePictureSend(items, idModal);
            e.preventDefault();
        }
    };

}


/**
 * set up the event listenner to make a drag and drop dropzone
 * @returns {void}
 */
function setUpDragAndDrop(idModal) {
    var dropzone = $(idModal).find("#dropzone")[0];
    dropzone.addEventListener("dragenter", dragenter, false);
    dropzone.addEventListener("dragover", dragover, false);
    dropzone.addEventListener("drop", function (event) {
        drop(event, idModal);
    });
}

/**
 * prevent the browser to open the file drag into an other tab
 * @returns {void}
 */
function dragenter(e) {
    e.stopPropagation();
    e.preventDefault();
}

/**
 * prevent the browser to open the file drag into an other tab
 * @returns {void}
 */
function dragover(e) {
    e.stopPropagation();
    e.preventDefault();
}

/**
 * prevent the browser to open the file drag into an other tab and handle the file when the user put his file
 * @returns {void}
 */
function drop(e, idModal) {
    e.stopPropagation();
    e.preventDefault();

    var dt = e.dataTransfer;
    var items = dt.items;
    handlePictureSend(items, idModal);
}

/**
 * get the picture from items and update the label with the name of the 
 * return a boolean if whether or not it succeed to handle the file 
 * @param {DataTransferItemList} items 
 * @returns {boolean}
 */
function handlePictureSend(items, idModal) {
    if (!items)
        return false;
    //access data directly
    for (var i = 0; i < items.length; i++) {
        ///check if the input is an image
        if (items[i].type.indexOf("image") !== -1) {
            //image from clipboard found
            var blob = items[i].getAsFile();
            imagePasteFromClipboard = blob;

            // Crossbrowser support for URL
            var URLObj = window.URL || window.webkitURL;

            // Creates a DOMString containing a URL representing the object given in the parameter
            // namely the original Blob
            $(idModal).find("#selectedPicture").attr("src", URLObj.createObjectURL(imagePasteFromClipboard));

            var source = URLObj.createObjectURL(blob);
            var nameToDisplay = blob.name;
            updateDropzone(nameToDisplay, idModal, blob.lastModifiedDate);
            return true;
        } else {
            var message = new Message("danger", "The file input is not a picture");
            showMessage(message, $(idModal));
        }
    }
}



/* functions used by both modal */

/**
 * add a listenner for an input type file
 * @returns {void}
 */

function listennerForInputTypeFile(idModal) {

    var inputs = $(idModal).find("#inputFile");
    inputs[0].addEventListener('change', function (e) {
        //check if the input is an image
        if (inputs[0].files[0].type.indexOf("image") !== -1) {
            var fileName = '';
            if (this.files && this.files.length > 1)
                fileName = (this.getAttribute('data-multiple-caption') || '').replace('{count}', this.files.length);
            else
                fileName = e.target.value.split('\\').pop();
            if (fileName) {
                updateDropzone(fileName, idModal);
            }
        } else {//not an image 
            var message = new Message("danger", "The file input is not a picture");
            showMessage(message, $(idModal));
        }
    });

}


/**
 * change the text inside the label specified and add the attribute uploadSources
 * @param {string} id of the input the label link to
 * @param {string} message that will put inside the label
 * @param {boolean} is the picture upload should be taken from the clipboard
 * @returns {void}
 */
function updateDropzone(messageToDisplay, idModal, modifDate) {

    var dropzoneText = $(idModal).find("#dropzoneText");
    var glyphIconUpload = "<span class='glyphicon glyphicon-download-alt'></span>";
    dropzoneText.html(messageToDisplay + " " + glyphIconUpload + " <br><i>" + getDateMedium(modifDate) + "</i> ");
    if (imagePasteFromClipboard !== undefined) {
        //reset value inside the input
        var inputs = $(idModal).find("#inputFile")[0];
        inputs.value = "";
    } else {
        //reset value for the var that stock the picture inside the clipboard
        imagePasteFromClipboard = undefined;
    }
}


