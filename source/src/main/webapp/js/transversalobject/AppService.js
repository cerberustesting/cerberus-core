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

function openModalAppService(service,mode){
	if ($('#editSoapLibraryModal').data("initLabel") === undefined){
		initModalAppService()		
		$('#editSoapLibraryModal').data("initLabel", true);
		
	}
	
	if (mode === "EDIT"){
		editAppServiceClick(service);
	}else if (mode == "ADD"){
		addAppServiceClick(service);
	}else{
		duplicateAppServiceClick(service);
	}
}

function initModalAppService(){
	console.info("init");
	var doc = new Doc();
	
    displayInvariantList("type", "SRVTYPE", false, "SOAP");
    displayInvariantList("method", "SRVMETHOD", false, "GET");
    displayApplicationList("application", "", "");
	
	$("[name='buttonEdit']").html(doc.getDocLabel("page_appservice", "button_edit"));
	$("[name='addEntryField']").html(
			doc.getDocLabel("page_appserivce", "button_create"));
	$("[name='confirmationField']").html(
			doc.getDocLabel("page_appservice", "button_delete"));
	$("[name='editEntryField']").html(
			doc.getDocLabel("page_appservice", "button_edit"));
	$("[name='applicationField']").html(doc.getDocOnline("page_applicationObject", "Application"));
    $("[name='soapLibraryField']").html(doc.getDocLabel("appservice", "service"));
    $("[name='typeField']").html(doc.getDocLabel("appservice", "type"));
    $("[name='descriptionField']").html(doc.getDocLabel("appservice", "description"));
    $("[name='servicePathField']").html(doc.getDocLabel("appservice", "servicePath"));
    $("[name='methodField']").html(doc.getDocLabel("appservice", "method"));
    $("[name='buttonClose']").html(doc.getDocLabel("page_appservice", "close_btn"));
    $("[name='buttonAdd']").html(doc.getDocLabel("page_appservice", "save_btn"));
    $("#soapLibraryListLabel").html("<span class='glyphicon glyphicon-list'></span> " + doc.getDocLabel("appservice", "service"));
    $("[name='lbl_created']").html(doc.getDocOnline("transversal", "DateCreated"));
    $("[name='lbl_creator']").html(doc.getDocOnline("transversal", "UsrCreated"));
    $("[name='lbl_lastModified']").html(doc.getDocOnline("transversal", "DateModif"));
    $("[name='lbl_lastModifier']").html(doc.getDocOnline("transversal", "UsrModif"));
    $("[name='lbl_datecreated']").html(doc.getDocOnline("transversal", "DateCreated"));
    $("[name='lbl_usrcreated']").html(doc.getDocOnline("transversal", "UsrCreated"));
    $("[name='lbl_datemodif']").html(doc.getDocOnline("transversal", "DateModif"));
    $("[name='lbl_usrmodif']").html(doc.getDocOnline("transversal", "UsrModif"));
	
	$("#editSoapLibraryButton").off("click");
	$("#editSoapLibraryButton").click(function() {
		confirmApplicationObjectModalHandler("EDIT");
	});
	$("#addSoapLibraryButton").off("click");
	$("#addSoapLibraryButton").click(function() {
		confirmApplicationObjectModalHandler("ADD");
	});
	
}

/***
 * Open the modal with testcase information.
 * @param {String} service - type selected
 * @returns {null}
 */
function editAppServiceClick(service) {

    var doc = new Doc();
    $("[name='editSoapLibraryField']").html(doc.getDocLabel("page_appservice", "editSoapLibrary_field"));

    $("#editSoapLibraryButton").off("click");
    $("#editSoapLibraryButton").click(function () {
        confirmAppServiceModalHandler("EDIT");
    });

    // Prepare all Events handler of the modal.
    prepareAppServiceModal();

    $('#editSoapLibraryButton').attr('class', 'btn btn-primary');
    $('#editSoapLibraryButton').removeProp('hidden');
    $('#duplicateSoapLibraryButton').attr('class', '');
    $('#duplicateSoapLibraryButton').attr('hidden', 'hidden');
    $('#addSoapLibraryButton').attr('class', '');
    $('#addSoapLibraryButton').attr('hidden', 'hidden');

    feedAppServiceModal(service, "editSoapLibraryModal", "EDIT");
}

/***
 * Open the modal with testcase information.
 * @param {String} service - type selected
 * @returns {null}
 */
function duplicateAppServiceClick(service) {
    $("#duplicateSoapLibraryButton").off("click");
    $("#duplicateSoapLibraryButton").click(function () {
        confirmAppServiceModalHandler("DUPLICATE");
    });

    // Prepare all Events handler of the modal.
    prepareAppServiceModal();

    $('#editSoapLibraryButton').attr('class', '');
    $('#editSoapLibraryButton').attr('hidden', 'hidden');
    $('#duplicateSoapLibraryButton').attr('class', 'btn btn-primary');
    $('#duplicateSoapLibraryButton').removeProp('hidden');
    $('#addSoapLibraryButton').attr('class', '');
    $('#addSoapLibraryButton').attr('hidden', 'hidden');
    
    
    feedAppServiceModal(service, "editSoapLibraryModal", "DUPLICATE");
}

/***
 * Open the modal in order to create a new testcase.
 * @returns {null}
 */
function addAppServiceClick(service) {
    $("#addSoapLibraryButton").off("click");
    $("#addSoapLibraryButton").click(function () {
        confirmAppServiceModalHandler("ADD");
    });

    // Prepare all Events handler of the modal.
    prepareAppServiceModal();

    $('#editSoapLibraryButton').attr('class', '');
    $('#editSoapLibraryButton').attr('hidden', 'hidden');
    $('#duplicateSoapLibraryButton').attr('class', '');
    $('#duplicateSoapLibraryButton').attr('hidden', 'hidden');
    $('#addSoapLibraryButton').attr('class', 'btn btn-primary');
    $('#addSoapLibraryButton').removeProp('hidden');
    

    feedAppServiceModal(service, "editSoapLibraryModal", "ADD");
}

/***
 * Function that initialise the modal with event handlers.
 * @returns {null}
 */
function prepareAppServiceModal() {

    // when type is changed we enable / disable type field.
    $("#editSoapLibraryModal #type").off("change");
    $("#editSoapLibraryModal #type").change(function () {
        refreshDisplayOnTypeChange($(this).val());
    });

    // Adding rows in edit Modal.
    $('#addContent').off("click");
    $("#addContent").click(addNewContentRow);
    $('#addHeader').off("click");
    $("#addHeader").click(addNewHeaderRow);

}


/***
 * Function that support the modal confirmation. Will call servlet to comit the transaction.
 * @param {String} mode - either ADD, EDIT or DUPLICATE in order to define the purpose of the modal.
 * @returns {null}
 */
function confirmAppServiceModalHandler(mode) {
    clearResponseMessage($('#editSoapLibraryModal'));

    var formEdit = $('#editSoapLibraryModal #editSoapLibraryModalForm');

    showLoaderInModal('#editSoapLibraryModal');

    // Enable the test combo before submit the form.
    if (mode === 'EDIT') {
        formEdit.find("#service").removeAttr("disabled");
    }
    // Calculate servlet name to call.
    var myServlet = "UpdateAppService";
    if ((mode === "ADD") || (mode === "DUPLICATE")) {
        myServlet = "CreateAppService";
    }

    // Get the header data from the form.
    var data = convertSerialToJSONObject(formEdit.serialize());

    //Add envelope, not in the form
    var editor = ace.edit($("#editSoapLibraryModal #srvRequest")[0]);
    data.srvRequest = encodeURIComponent(editor.getSession().getDocument().getValue());

    // Getting Data from Content TAB
    var table1 = $("#contentTableBody tr");
    var table_content = [];
    for (var i = 0; i < table1.length; i++) {
        table_content.push($(table1[i]).data("content"));
    }
    // Getting Data from Header TAB
    var table2 = $("#headerTableBody tr");
    var table_header = [];
    for (var i = 0; i < table2.length; i++) {
        table_header.push($(table2[i]).data("header"));
        
        var toto = [];
    }


    $.ajax({
        url: myServlet,
        async: true,
        method: "POST",
        data: {
            service: data.service,
            application: data.application,
            type: data.type,
            method: data.method,
            servicePath: data.servicePath,
            operation: data.operation,
            attachementurl: data.attachementurl,
            description: data.description,
            group: data.group,
            serviceRequest: data.srvRequest,
            contentList: JSON.stringify(table_content),
            headerList: JSON.stringify(table_header)
        },
        success: function (data) {
            data = JSON.parse(data);
            
            if (getAlertType(data.messageType) === "success") {
                var oTable = $("#soapLibrarysTable").dataTable();
                oTable.fnDraw(true);
                $('#editSoapLibraryModal').data("Saved", true);
                $('#editSoapLibraryModal').modal('hide');
                
                showMessage(data);
            } else {
                showMessage(data, $('#editSoapLibraryModal'));
            }
            
            hideLoaderInModal('#editSoapLibraryModal');
        },
        error: showUnexpectedError
    });
    if (mode === 'EDIT') { // Disable back the test combo before submit the form.
        formEdit.find("#service").prop("disabled", "disabled");
    }

}

function refreshDisplayOnTypeChange(newValue) {
    if (newValue === "SOAP") {
        // If SOAP service, no need to feed the method.
        $('#editSoapLibraryModal #method').prop("disabled", true);
        $('#editSoapLibraryModal #operation').prop("readonly", false);
        $('#editSoapLibraryModal #attachementurl').prop("readonly", false);
    } else {
        $('#editSoapLibraryModal #method').prop("disabled", false);
        $('#editSoapLibraryModal #operation').prop("readonly", true);
        $('#editSoapLibraryModal #attachementurl').prop("readonly", true);
    }
}


/***
 * Feed the TestCase modal with all the data from the TestCase.
 * @param {String} serviceName - type selected
 * @param {String} modalId - type selected
 * @param {String} mode - either ADD, EDIT or DUPLICATE in order to define the purpose of the modal.
 * @returns {null}
 */
function feedAppServiceModal(serviceName, modalId, mode) {
    clearResponseMessageMainPage();
    var formEdit = $('#' + modalId);
    
    if(mode === "DUPLICATE" || mode === "EDIT"){
    	
	    $.ajax({
	        url: "ReadAppService?service=" + serviceName,
	        async: true,
	        method: "GET",
	        success: function (data) {
	            if (data.messageType === "OK") {
	
	                // Feed the data to the screen and manage authorities.
	                var service = data.contentTable;
	                feedAppServiceModalData(service, modalId, mode, service.hasPermissions);
	
	                // Force a change event on method field.
	                refreshDisplayOnTypeChange(service.type);
	
	                formEdit.modal('show');
	            } else {
	                showUnexpectedError();
	            }
	        },
	        error: showUnexpectedError
	    });
	
	}else{
		var serviceObj1 = {};
		var hasPermissions = true;
		serviceObj1.service = serviceName;
		serviceObj1.application = "";
		serviceObj1.type = "REST";
		serviceObj1.method = "GET";
		serviceObj1.servicePath = "";
		serviceObj1.operation = "";
		serviceObj1.attachementurl = "";
		serviceObj1.description = "";
		serviceObj1.group = "";
		serviceObj1.serviceRequest = "";
		serviceObj1.contentList = "";
		serviceObj1.headerList = "";
		
			
		feedAppServiceModalData(serviceObj1, modalId, mode, hasPermissions);
		refreshDisplayOnTypeChange(serviceObj1.type);
		formEdit.modal('show');
		
	}
}


/***
 * Feed the TestCase modal with all the data from the TestCase.
 * @param {String} service - service object to be loaded.
 * @param {String} modalId - id of the modal form where to feed the data.
 * @param {String} mode - either ADD, EDIT or DUPLICATE in order to define the purpose of the modal.
 * @param {String} hasPermissionsUpdate - boolean if premition is granted.
 * @returns {null}
 */
function feedAppServiceModalData(service, modalId, mode, hasPermissionsUpdate) {
    var formEdit = $('#' + modalId);
    var doc = new Doc();

    //Destroy the previous Ace object.
    ace.edit($("#editSoapLibraryModal #srvRequest")[0]).destroy();

    // Data Feed.
    if (mode === "EDIT") {
        $("[name='editSoapLibraryField']").html(doc.getDocOnline("page_appservice", "button_edit"));
        formEdit.find("#service").prop("value", service.service);
        formEdit.find("#usrcreated").prop("value", service.UsrCreated);
        formEdit.find("#datecreated").prop("value", service.DateCreated);
        formEdit.find("#usrmodif").prop("value", service.UsrModif);
        formEdit.find("#datemodif").prop("value", service.DateModif);
    } else { // DUPLICATE or ADD
        formEdit.find("#usrcreated").prop("value", "");
        formEdit.find("#datecreated").prop("value", "");
        formEdit.find("#usrmodif").prop("value", "");
        formEdit.find("#datemodif").prop("value", "");
        if (mode === "ADD") {
            $("[name='editSoapLibraryField']").html(doc.getDocOnline("page_appservice", "button_create"));
            formEdit.find("#service").prop("value", service.service);
        } else { // DUPLICATE
            $("[name='editSoapLibraryField']").html(doc.getDocOnline("page_appservice", "button_duplicate"));
            formEdit.find("#service").prop("value", service.service);
            formEdit.find("#service").prop("value", "");
        }
    }
    if (isEmpty(service)) {
        formEdit.find("#originalService").prop("value", "");
        formEdit.find("#application").prop("value", "");
        formEdit.find("#type").prop("value", "REST");
        refreshDisplayOnTypeChange("REST");
        formEdit.find("#method").prop("value", "GET");
        formEdit.find("#servicePath").prop("value", "");
        formEdit.find("#attachementurl").prop("value", "");
        formEdit.find("#srvRequest").text("");
        formEdit.find("#group").prop("value", "");
        formEdit.find("#operation").prop("value", "");
        formEdit.find("#description").prop("value", "");
    } else {
        formEdit.find("#application").val(service.application);
        formEdit.find("#type").val(service.type);
        formEdit.find("#method").val(service.method);
        formEdit.find("#servicePath").prop("value", service.servicePath);
        formEdit.find("#attachementurl").prop("value", service.attachementURL);
        formEdit.find("#srvRequest").text(service.serviceRequest);
        formEdit.find("#group").prop("value", service.group);
        formEdit.find("#operation").prop("value", service.operation);
        formEdit.find("#description").prop("value", service.description);

        // Feed the content table.
        feedAppServiceModalDataContent(service.contentList);

        // Feed the header table.
        feedAppServiceModalDataHeader(service.headerList);
    }
    //Highlight envelop on modal loading
    var editor = ace.edit($("#editSoapLibraryModal #srvRequest")[0]);
    editor.setTheme("ace/theme/chrome");
    editor.getSession().setMode(defineAceMode(editor.getSession().getDocument().getValue()));
    editor.setOptions({
        maxLines: Infinity
    });

    //On ADD, try to autodetect Ace mode until it is defined
    if (mode === "ADD") {
        $($("#editSoapLibraryModal #srvRequest").get(0)).keyup(function () {
            if (editor.getSession().getMode().$id === "ace/mode/text") {
                editor.getSession().setMode(defineAceMode(editor.getSession().getDocument().getValue()));
            }
        });

    }

    // Authorities
    if (mode === "EDIT") {
        formEdit.find("#service").prop("readonly", "readonly");
    } else {
        formEdit.find("#service").removeAttr("readonly");
        formEdit.find("#service").removeProp("readonly");
        formEdit.find("#service").removeAttr("disabled");
    }
    //We desactivate or activate the access to the fields depending on if user has the credentials to edit.
    if (!(hasPermissionsUpdate)) { // If readonly, we readonly all fields
        formEdit.find("#application").prop("readonly", "readonly");
        formEdit.find("#type").prop("disabled", "disabled");
        formEdit.find("#method").prop("disabled", "disabled");
        formEdit.find("#servicePath").prop("readonly", true);
        formEdit.find("#attachementurl").prop("readonly", true);
        formEdit.find("#srvRequest").prop("readonly", "readonly");
        formEdit.find("#description").prop("readonly", "readonly");
        // We hide Save button.
        $('#editSoapLibraryButton').attr('class', '');
        $('#editSoapLibraryButton').attr('hidden', 'hidden');
    } else {
        formEdit.find("#application").removeProp("readonly");
        formEdit.find("#type").removeProp("disabled");
        formEdit.find("#method").removeProp("disabled");
        formEdit.find("#servicePath").prop("readonly", false);
        formEdit.find("#attachementurl").prop("readonly", false);
        formEdit.find("#srvRequest").removeProp("readonly");
        formEdit.find("#description").removeProp("disabled");
    }


}

function feedAppServiceModalDataContent(ContentList) {
    $('#contentTableBody tr').remove();
    if(!isEmpty(ContentList)){
    	$.each(ContentList, function (idx, obj) {
            obj.toDelete = false;
            console.debug(obj);
            appendContentRow(obj);
        });
    } 
}

function appendContentRow(content) {
    var doc = new Doc();

    var deleteBtn = $("<button type=\"button\"></button>").addClass("btn btn-default btn-xs").append($("<span></span>").addClass("glyphicon glyphicon-trash"));
    var activeSelect = getSelectInvariant("APPSERVICECONTENTACT", false);
    var sortInput = $("<input  maxlength=\"4\" placeholder=\"-- " + doc.getDocLabel("appservicecontent", "Sort") + " --\">").addClass("form-control input-sm").val(content.sort);
    var keyInput = $("<input  maxlength=\"255\" placeholder=\"-- " + doc.getDocLabel("appservicecontent", "Key") + " --\">").addClass("form-control input-sm").val(content.key);
    var valueInput = $("<textarea rows='1'  placeholder=\"-- " + doc.getDocLabel("appservicecontent", "Value") + " --\"></textarea>").addClass("form-control input-sm").val(content.value);
    var descriptionInput = $("<input  maxlength=\"200\" placeholder=\"-- " + doc.getDocLabel("appservicecontent", "Description") + " --\">").addClass("form-control input-sm").val(content.description);
    var table = $("#contentTableBody");

    var row = $("<tr></tr>");
    var deleteBtnRow = $("<td></td>").append(deleteBtn);
    var active = $("<td></td>").append(activeSelect.val(content.active));
    var sortName = $("<td></td>").append(sortInput);
    var keyName = $("<td></td>").append(keyInput);
    var valueName = $("<td></td>").append(valueInput);
    var descriptionName = $("<td></td>").append(descriptionInput);
    deleteBtn.click(function () {
        content.toDelete = (content.toDelete) ? false : true;

        if (content.toDelete) {
            row.addClass("danger");
        } else {
            row.removeClass("danger");
        }
    });
    activeSelect.change(function () {
        content.active = $(this).val();
    });
    sortInput.change(function () {
        content.sort = $(this).val();
    });
    keyInput.change(function () {
        content.key = $(this).val();
    });
    valueInput.change(function () {
        content.value = $(this).val();
    });
    descriptionInput.change(function () {
        content.description = $(this).val();
    });
    row.append(deleteBtnRow);
    row.append(active);
    row.append(sortName);
    row.append(keyName);
    row.append(valueName);
    row.append(descriptionName);
    row.data("content", content);
    table.append(row);
}

function addNewContentRow() {
    var newContent = {
        active: "Y",
        sort: 10,
        key: "",
        value: "",
        description: "",
        toDelete: false
    };
    appendContentRow(newContent);
}

function feedAppServiceModalDataHeader(headerList) {
	
	$('#headerTableBody tr').remove();
	if(!isEmpty(headerList)){
		$.each(headerList, function (idx, obj) {
	        obj.toDelete = false;
	        appendHeaderRow(obj);
	    });
	}   
}

function appendHeaderRow(content) {
    var doc = new Doc();
    var deleteBtn = $("<button type=\"button\"></button>").addClass("btn btn-default btn-xs").append($("<span></span>").addClass("glyphicon glyphicon-trash"));
    var activeSelect = getSelectInvariant("APPSERVICECONTENTACT", false);
    var sortInput = $("<input  maxlength=\"4\" placeholder=\"-- " + doc.getDocLabel("appservicecontent", "Sort") + " --\">").addClass("form-control input-sm").val(content.sort);
    var keyInput = $("<input  maxlength=\"255\" placeholder=\"-- " + doc.getDocLabel("appservicecontent", "Key") + " --\">").addClass("form-control input-sm").val(content.key);
    var valueInput = $("<textarea rows='1'  placeholder=\"-- " + doc.getDocLabel("appservicecontent", "Value") + " --\"></textarea>").addClass("form-control input-sm").val(content.value);
    var descriptionInput = $("<input  maxlength=\"200\" placeholder=\"-- " + doc.getDocLabel("appservicecontent", "Description") + " --\">").addClass("form-control input-sm").val(content.description);
    var table = $("#headerTableBody");

    var row = $("<tr></tr>");
    var deleteBtnRow = $("<td></td>").append(deleteBtn);
    var active = $("<td></td>").append(activeSelect.val(content.active));
    var sortName = $("<td></td>").append(sortInput);
    var keyName = $("<td></td>").append(keyInput);
    var valueName = $("<td></td>").append(valueInput);
    var descriptionName = $("<td></td>").append(descriptionInput);
    deleteBtn.click(function () {
        content.toDelete = (content.toDelete) ? false : true;

        if (content.toDelete) {
            row.addClass("danger");
        } else {
        }
    });
    activeSelect.change(function () {
        content.active = $(this).val();
    });
    sortInput.change(function () {
        content.sort = $(this).val();
    });
    keyInput.change(function () {
        content.key = $(this).val();
    });
    valueInput.change(function () {
        content.value = $(this).val();
    });
    descriptionInput.change(function () {
        content.description = $(this).val();
    });
    row.append(deleteBtnRow);
    row.append(active);
    row.append(sortName);
    row.append(keyName);
    row.append(valueName);
    row.append(descriptionName);
    row.data("header", content);
    table.append(row);
}

function addNewHeaderRow() {
    var newHeader = {
        active: "Y",
        sort: 10,
        key: "",
        value: "",
        description: "",
        toDelete: false
    };
    appendHeaderRow(newHeader);
}
