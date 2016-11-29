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

function initPageModal(page) {
    displayModalLabel();

    // handle the click for specific action buttons
    $("#addApplicationObjectButton").click(function(){
        addApplicationObjectModalSaveHandler(page)
    });

    //clear the modals fields when closed
    $('#addApplicationObjectModal').on('hidden.bs.modal', addApplicationObjectModalCloseHandler);
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

function addApplicationObjectModalSaveHandler(page) {
    clearResponseMessage($('#addApplicationObjectModal'));
    var formAdd = $("#addApplicationObjectModal #addApplicationObjectModalForm :input");
    var file = $("#addApplicationObjectModal input[type=file]");
    // Get the header data from the form
    var sa = formAdd.serializeArray();
    var formData = new FormData();
    var data = {}
    for (var i in sa) {
        formData.append(sa[i].name, sa[i].value);
    }

    formData.append("file",file.prop("files")[0]);
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
//        console.log(data.messageType);
        if (getAlertType(data.messageType) === 'success') {
            if(page == "applicationObject") {
                var oTable = $("#applicationObjectsTable").dataTable();
                oTable.fnDraw(true);
            }else if(page == "testCaseScript"){
                $("div.step-action .content div.row.form-inline span:nth-child(n+2) input").trigger("change");
            }
            showMessage(data);
            $('#addApplicationObjectModal').modal('hide');

        } else {
            showMessage(data, $('#addApplicationObjectModal'));
        }
    }).fail(handleErrorAjaxAfterTimeout);
}

function addApplicationObjectModalCloseHandler() {
    // reset form values
    $('#addApplicationObjectModal #addApplicationObjectModalForm')[0].reset();
    // remove all errors on the form fields
    $(this).find('div.has-error').removeClass("has-error");
    // clear the response messages of the modal
    clearResponseMessage($('#addApplicationObjectModal'));
}

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
}
