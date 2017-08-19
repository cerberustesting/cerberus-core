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

var imagePasteFromClipboard = undefined;//stock the picture if the user chose to upload it from his clipboard

/*
 * init an add object modal
 * @param {type} current page name
 * @returns {undefined}
 */
function initPageModalToAddObject(page){
    // handle the click for specific action buttons
    $("#addApplicationObjectButton").click(function(){
        addApplicationObjectModalSaveHandler(page, '#addApplicationObjectModal');
    });
    $('#addApplicationObjectModal').on('hidden.bs.modal', function () {
        addApplicationObjectModalCloseHandler();
    });
    setUpDragAndDrop('#addApplicationObjectModal');
    
    hidePasteMessageIfNotOnFirefox()
}

/*
 * init an edit object modal
 * @returns {undefined}
 */
function initPageModalToEditObject(){
    // handle the click for specific action buttons
    $("#editApplicationObjectButton").click(function(){
        editApplicationObjectModalSaveHandler();
    });
    //clear the modals fields when closed
    $('#editApplicationObjectModal').on('hidden.bs.modal', function () {
        editApplicationObjectModalCloseHandler();;
    });
    setUpDragAndDrop('#editApplicationObjectModal');
    
    hidePasteMessageIfNotOnFirefox();
}

/*
 * hide message if not on firefox
 * @returns {undefined}
 */
function hidePasteMessageIfNotOnFirefox(){
    var isOnFirefox = typeof InstallTrigger !== 'undefined';
    if ( !isOnFirefox ){
        for (var i =0; i < $('[id*="DropzoneClipboardPasteMessage"]').length; i++ ){
            $('[id*="DropzoneClipboardPasteMessage"]')[i].style.display = 'none';
        }
    } 
}

/* functions for add object modal */

/*
 * save the data in the modal's form
 * @param {type} page
 * @returns {undefined}
 */
function addApplicationObjectModalSaveHandler(page) {
    
    clearResponseMessage($('#addApplicationObjectModal'));
    var formAdd = $("#addApplicationObjectModal #addApplicationObjectModalForm");
    var file = $("#addApplicationObjectModal input[type=file]");
    // Get the header data from the form
    var sa = formAdd.serializeArray();
    var formData = new FormData();
    
    for (var i in sa) {
        formData.append(sa[i].name, sa[i].value);
    }
    
    if( imagePasteFromClipboard !== undefined ){//imagePasteFromClipboard is undefined, the picture to upload should be taken inside the input
        formData.append("file",imagePasteFromClipboard);
    }else{
        var file = $("#addApplicationObjectModal input[type=file]");
        formData.append("file",file.prop("files")[0]);
    }
    showLoaderInModal('#addApplicationObjectModal');
    
    var jqxhr = $.ajax({
        type: "POST",
        url: "CreateApplicationObject",
        data: formData,
        processData: false,
        contentType: false
    });
    $.when(jqxhr).then(function (data) {
        hideLoaderInModal('#addApplicationObjectModal');
        
        if (getAlertType(data.messageType) === 'success') {
            if(page == "applicationObject") {
                var oTable = $("#applicationObjectsTable").dataTable();
                oTable.fnDraw(true);
            }else if(page == "testCaseScript"){
                //TestCaseScript.js must be loaded so getTags exist
                var Tags = getTags();
                for(var i = 0; i < Tags.length; i++){
                    if(Tags[i].regex == "%object\\."){
                        Tags[i].array.push(formData.get("object"));
                    }
                }
                $("div.step-action .content div.fieldRow div:nth-child(n+2) input").trigger("input");
            }
            showMessage(data);
            $('#addApplicationObjectModal').modal('hide');

        } else {
            showMessage(data, $('#addApplicationObjectModal'));
        }
    }).fail(handleErrorAjaxAfterTimeout);
}

/*
 * call when the modal is close
 * @returns {undefined}
 */
function addApplicationObjectModalCloseHandler() {
    // reset form values
    $('#addApplicationObjectModal #addApplicationObjectModalForm')[0].reset();
    // remove all errors on the form fields
    $(this).find('div.has-error').removeClass("has-error");
    // clear the response messages of the modal
    clearResponseMessage($('#addApplicationObjectModal'));
    //Reset label button text
    updateDropzone("Drag and drop Files ",'#addApplicationObjectModal');
    //reset imagePasteFromClipboard
    imagePasteFromClipboard = undefined;
}

/*
 * create modal
 * @param {type} event
 * @param {type} object
 * @param {type} application
 * @returns {undefined}
 */
function addApplicationObjectModalClick(event, object, application) {
    clearResponseMessageMainPage();

    $('#addApplicationObjectModal #application').empty();
    displayApplicationList("application","",application);

    if(object != undefined){
        $("[name='object']").val(object);
    }
    // When creating a new applicationObject, System takes the default value of the 
    // system already selected in header.
    var formAdd = $('#addApplicationObjectModal');
    // Default to NONE to Application.
    formAdd.find("#type").val("NONE");

    $('#addApplicationObjectModal').modal('show');
    
    listennerForInputTypeFile('#addApplicationObjectModal');
    pasteListennerForClipboardPicture('#addApplicationObjectModal');
}

/* functions edit object modal */

/*
 * save the data in the modal's form
 * @returns {undefined}
 */
function editApplicationObjectModalSaveHandler() {
    clearResponseMessage($('#editApplicationObjectModal'));
    $('#editApplicationObjectModal #editApplicationObjectModalForm select#application').attr("disabled",false);
    var formEdit = $('#editApplicationObjectModal #editApplicationObjectModalForm');
    var file = $("#editApplicationObjectModal input[type=file]");
    
    // Get the header data from the form.
    var sa = formEdit.serializeArray();
    var formData = new FormData();
    var data = {}
    for (var i in sa) {
        formData.append(sa[i].name, sa[i].value);
    }
    
    if( imagePasteFromClipboard !== undefined ){//imagePasteFromClipboard is undefined, the picture to upload should be taken inside the input
        formData.append("file",imagePasteFromClipboard);
    }else{
        var file = $("#editApplicationObjectModal input[type=file]");
        formData.append("file",file.prop("files")[0]);
    }
    showLoaderInModal('#editApplicationObjectModal');  
    $.ajax({
        type: "POST",
        url: "UpdateApplicationObject",
        data: formData,
        async: true,
        processData: false,
        contentType: false,
        success: function (data) {
            hideLoaderInModal('#editApplicationObjectModal');
            if (getAlertType(data.messageType) === "success") {
                var oTable = $("#applicationObjectsTable").dataTable();
                oTable.fnDraw(true);
                $('#editApplicationObjectModal').modal('hide');
                showMessage(data);
            } else {
                console.log(data)
                showMessage(data, $('#editApplicationObjectModal'));
            }
        },
        error: showUnexpectedError
    });

}

/*
 * call when the modal is close
 * @returns {undefined}
 */
function editApplicationObjectModalCloseHandler() {
    // reset form values
    $('#editApplicationObjectModal #editApplicationObjectModalForm')[0].reset();
    // remove all errors on the form fields
    $(this).find('div.has-error').removeClass("has-error");
    // clear the response messages of the modal
    clearResponseMessage($('#editApplicationObjectModal'));
    //Reset label button text
    updateDropzone("Drag and drop Files ",'#editApplicationObjectModal');
    //reset imagePasteFromClipboard
    imagePasteFromClipboard = undefined;
}


/*
 * show modal
 * @param {type} application
 * @param {type} object
 * @returns {undefined}
 */
function editApplicationObjectClick(application, object) {
    clearResponseMessageMainPage();
    $('#editApplicationObjectModal #application').empty();
    displayApplicationList("application","",application);
    var jqxhr = $.getJSON("ReadApplicationObject", "application=" + application + "&object=" + object);
    $.when(jqxhr).then(function (data) {

        var obj = data["contentTable"];
        var formEdit = $('#editApplicationObjectModal');

        formEdit.find("#application option[value='" + obj["application"] + "']").prop("selected", true);
        formEdit.find("#object").prop("value", obj["object"]);
        formEdit.find("#value").prop("value", obj["value"]);
        //formEdit.find("#screenshotfilename").prop("value", obj["screenshotfilename"]);

        formEdit.find("#object").prop("readonly", "readonly");
        formEdit.find("#application").prop("disabled", "disabled");

        if (!(data["hasPermissions"])) { // If readonly, we only readonly all fields

            formEdit.find("#screenshotfilename").prop("readonly", "readonly");
            formEdit.find("#application").prop("readonly", "readonly");
            
            $('#editApplicationObjectButton').attr('class', '');
            $('#editApplicationObjectButton').attr('hidden', 'hidden');
        }
        formEdit.modal('show');
        listennerForInputTypeFile('#editApplicationObjectModal');
        pasteListennerForClipboardPicture('#editApplicationObjectModal');
    });

}

/* functions used by both modal */

/**
 * add a listenner for an input type file
 * @returns {void}
 */
function listennerForInputTypeFile(idModal){
    
    if (idModal === "#editApplicationObjectModal")
        var inputs = $(idModal).find("#inputFile_editObject");
    else
        var inputs = $(idModal).find("#inputFile");

    inputs[0].addEventListener( 'change', function( e ){
        //check if the input is an image
        if(  inputs[0].files[0].type.indexOf("image") !== -1 ){
            var fileName = '';
            if( this.files && this.files.length > 1 )
                fileName = ( this.getAttribute( 'data-multiple-caption' ) || '' ).replace( '{count}', this.files.length );
            else
                fileName = e.target.value.split( '\\' ).pop();

            if( fileName ){
                updateDropzone(fileName, idModal);
            }
        }else{//not an image 
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
function updateDropzone(messageToDisplay, idModal){
    
    var dropzoneText = $(idModal).find("#dropzoneText");
    var glyphIconUpload = "<span class='glyphicon glyphicon-download-alt'></span>";
    dropzoneText.html(messageToDisplay +" "+ glyphIconUpload);
    if( imagePasteFromClipboard !== undefined ){
        //reset value inside the input
        if (idModal === "#editApplicationObjectModal")
            var inputs = $(idModal).find("#inputFile_editObject")[0];
        else
            var inputs = $(idModal).find("#inputFile")[0];
        inputs.value = "";
    }
    else{
        //reset value for the var that stock the picture inside the clipboard
        imagePasteFromClipboard = undefined;
    }
}


/**
 * add a listenner for a paste event to catch clipboard if it's a picture
 * @returns {void}
 */
function pasteListennerForClipboardPicture( idModal) {
    var _self = this;
    //handlers
    document.addEventListener('paste', function (e) { _self.paste_auto(e); }, false);
    
    //on paste
    this.paste_auto = function (e) {
        //handle paste event if the user do not select an input
        if (e.clipboardData && !$(e.target).is( "input" )) {
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
function setUpDragAndDrop(idModal){
    var dropzone = $(idModal).find("#dropzone")[0];
    dropzone.addEventListener("dragenter", dragenter, false);
    dropzone.addEventListener("dragover", dragover, false);
    dropzone.addEventListener("drop", function(event) { drop(event, idModal); } );
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
  handlePictureSend(items,idModal);
}

/**
 * get the picture from items and update the label with the name of the 
 * return a boolean if whether or not it succeed to handle the file 
 * @param {DataTransferItemList} items 
 * @returns {boolean}
 */
function handlePictureSend(items,idModal){
    if (!items) return false;
    //access data directly
    for (var i = 0; i < items.length; i++) {
        ///check if the input is an image
        if (items[i].type.indexOf("image") !== -1) {
            //image from clipboard found
            var blob = items[i].getAsFile();
            imagePasteFromClipboard =blob;
            var URLObj = window.URL || window.webkitURL;
            var source = URLObj.createObjectURL(blob);
            var nameToDisplay =blob.name;
            updateDropzone(nameToDisplay, idModal);
            return true;
        }else{
            var message = new Message("danger", "The file input is not a picture");
            showMessage(message, $(idModal));
        }
    }
}
