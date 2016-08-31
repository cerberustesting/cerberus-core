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

    // handle the click for specific action buttons
    $("#editInvariantButton").click(editEntryModalSaveHandler);
    $("#addInvariantButton").click(addEntryModalSaveHandler);

    //clear the modals fields when closed
    $('#editInvariantModal').on('hidden.bs.modal', editEntryModalCloseHandler);
    $('#addInvariantModal').on('hidden.bs.modal', addEntryModalCloseHandler);
    $('a[data-toggle="tab"]').on('shown.bs.tab', function (e) {
        console.log(e); // activated tab
        if(e.target.innerText == "Private"){
            displayPrivateTable();
        }else{
            displayPublicTable();
        }
    });
    displayPublicTable();
}

function displayPrivateTable(){
    if ( $.fn.dataTable.isDataTable( '#invariantsPrivateTable' ) ) {
        $('#invariantsPrivateTable').DataTable();
    }
    else {
        //configure and create the dataTable
        var configurationsPriv = new TableConfigurationsServerSide("invariantsPrivateTable", "ReadInvariant?access=PRIVATE", "contentTable", aoColumnsFunc2(), [0, 'asc']);
        createDataTableWithPermissions(configurationsPriv, renderOptionsForApplication2, "#invariantPrivateList");
    }
}

function displayPublicTable(){
    if ( $.fn.dataTable.isDataTable( '#invariantsTable' ) ) {
        $('#invariantsTable').DataTable();
    }
    else {
        //configure and create the dataTable
        var configurations = new TableConfigurationsServerSide("invariantsTable", "ReadInvariant?access=PUBLIC", "contentTable", aoColumnsFunc(), [1, 'asc']);
        createDataTableWithPermissions(configurations, renderOptionsForApplication, "#invariantList");
    }
}

function displayPageLabel() {
    var doc = new Doc();

    $("#title").html(doc.getDocLabel("page_invariant", "allInvariants"));
    $("[name='editInvariantField']").html(doc.getDocLabel("page_invariant", "editinvariant_field"));
    $("[name='invariantField']").html(doc.getDocLabel("page_invariant", "invariant_field"));
    $("[name='cerberusField']").html(doc.getDocLabel("page_invariant", "cerberus_field"));
    $("[name='descriptionField']").html(doc.getDocLabel("page_invariant", "description_field"));
    $("[name='buttonClose']").html(doc.getDocLabel("page_invariant", "close_btn"));
    $("[name='buttonAdd']").html(doc.getDocLabel("page_invariant", "save_btn"));

    displayHeaderLabel(doc);

    $("[name='systemField']").html(doc.getDocLabel("page_invariant", "system_field") + " (" + getSys() + ")");

    displayFooter(doc);
    displayGlobalLabel(doc);
}

function renderOptionsForApplication(data) {
    var doc = new Doc();
    if ($("#createInvariantButton").length === 0) {
        var contentToAdd = "<div class='marginBottom10'><button id='createInvariantButton' type='button' class='btn btn-default'>\n\
            <span class='glyphicon glyphicon-plus-sign'></span> " + doc.getDocLabel("page_invariant", "button_create") + "</button></div>";
        $("#invariantsTable_wrapper div#invariantsTable_length").before(contentToAdd);
        $('#invariantList #createInvariantButton').click(addEntryClick);
    }
}

function renderOptionsForApplication2(data) {
    var doc = new Doc();
    if ($("#blankSpace").length === 0) {
        var contentToAdd = "<div class='marginBottom10' style='height:25px;' id='blankSpace'></div>";
        $("#invariantsPrivateTable_wrapper div#invariantsPrivateTable_length").before(contentToAdd);
    }
}

function addEntryClick() {
    clearResponseMessageMainPage();
    $("#addInvariantModal #idname").empty();
    getInvariantList("INVARIANTPUBLIC",function(data){
        for(var i in data){
            $("#addInvariantModal #idname").append($("<option>"+i+"</option>"))
        }
    });

    $('#addInvariantModal').modal('show');
}

function editEntryClick(param, value) {
    var formEdit = $('#editInvariantModal');

    $.ajax({
        url: "ReadInvariant?idName="+param+"&value="+value,
        async: true,
        method: "GET",
        success: function (data) {
            console.log(data);
            if(data.messageType === "OK") {
                formEdit.find("#idname").prop("value", data.invariant.idName);
                formEdit.find("#value").prop("value", data.invariant.value);
                formEdit.find("#sort").prop("value", data.invariant.sort);
                formEdit.find("#description").prop("value", data.invariant.description);
                formEdit.find("#veryShortDesc").prop("value", data.invariant.veryShortDesc);
                formEdit.find("#gp1").prop("value", data.invariant.gp1);
                formEdit.find("#gp2").prop("value", data.invariant.gp2);
                formEdit.find("#gp3").prop("value", data.invariant.gp3);

                formEdit.modal('show');
            }else{
                showUnexpectedError();
            }
        },
        error: showUnexpectedError
    });
}

function removeEntryClick(param, value) {
    var doc = new Doc();
    showModalConfirmation(function(ev){
        var param = $('#confirmationModal #hiddenField1').prop("value");
        var value = $('#confirmationModal #hiddenField2').prop("value");
        $.ajax({
            url: "DeleteInvariant2?idName="+param+"&value="+value,
            async: true,
            method: "GET",
            success: function (data) {
                console.log(data.messageType);
                if(data.messageType === "OK") {
                    formEdit.find("#idname").prop("value", data.invariant.idName);
                    formEdit.find("#value").prop("value", data.invariant.value);
                    formEdit.find("#sort").prop("value", data.invariant.sort);
                    formEdit.find("#description").prop("value", data.invariant.description);
                    formEdit.find("#veryShortDesc").prop("value", data.invariant.veryShortDesc);
                    formEdit.find("#gp1").prop("value", data.invariant.gp1);
                    formEdit.find("#gp2").prop("value", data.invariant.gp2);
                    formEdit.find("#gp3").prop("value", data.invariant.gp3);

                    formEdit.modal('show');
                }else{
                    showUnexpectedError();
                }
            },
            error: showUnexpectedError
        });

        $('#confirmationModal').modal('hide');
    }, doc.getDocLabel("page_invariant", "title_remove") , doc.getDocLabel("page_invariant", "message_remove"), param, value, undefined, undefined);
}
function editEntryModalSaveHandler() {
    clearResponseMessage($('#editInvariantModal'));
    var formEdit = $('#editInvariantModal #editInvariantModalForm');

    var sa = formEdit.serializeArray();
    var data = {}
    for (var i in sa) {
        data[sa[i].name] = sa[i].value;
    }
    // Get the header data from the form.
    //var data = convertSerialToJSONObject(formEdit.serialize());
    console.log(data);
    showLoaderInModal('#editInvariantModal');
    $.ajax({
        url: "UpdateInvariant2",
        async: true,
        method: "POST",
        data: {
            idName: data.idname,
            value: data.value,
            sort: data.sort,
            description: data.description,
            veryShortDesc: data.veryShortDesc,
            gp1: data.gp1,
            gp2: data.gp2,
            gp3: data.gp3
        },
        success: function (data) {
            hideLoaderInModal('#editInvariantModal');
            var oTable = $("#invariantsTable").dataTable();
            oTable.fnDraw(true);
            $('#editInvariantModal').modal('hide');
            showMessage(data);
        },
        error: showUnexpectedError
    });
}

function addEntryModalSaveHandler() {
    clearResponseMessage($('#addInvariantModal'));
    var formEdit = $('#addInvariantModal #addInvariantModalForm');

    var sa = formEdit.serializeArray();
    var data = {}
    for (var i in sa) {
        data[sa[i].name] = sa[i].value;
    }
    // Get the header data from the form.
    //var data = convertSerialToJSONObject(formEdit.serialize());
    console.log(data);
    showLoaderInModal('#addInvariantModal');
    $.ajax({
        url: "CreateInvariant2",
        async: true,
        method: "POST",
        data: {
            Idname: data.idname,
            Value: data.value,
            Sort: data.sort,
            Description: data.description,
            VeryShortDesc: data.veryShortDesc,
            gp1: data.gp1,
            gp2: data.gp2,
            gp3: data.gp3
        },
        success: function (data) {
            hideLoaderInModal('#addInvariantModal');
            var oTable = $("#invariantsTable").dataTable();
            oTable.fnDraw(true);
            $('#addInvariantModal').modal('hide');
            showMessage(data);
        },
        error: showUnexpectedError
    });


}

function editEntryModalCloseHandler() {
    // reset form values
    $('#editInvariantModal #editInvariantModalForm')[0].reset();
    // remove all errors on the form fields
    $(this).find('div.has-error').removeClass("has-error");
    // clear the response messages of the modal
    clearResponseMessage($('#editInvariantModal'));
}

function addEntryModalCloseHandler() {
    // reset form values
    $('#addInvariantModal #addInvariantModalForm')[0].reset();
    // remove all errors on the form fields
    $(this).find('div.has-error').removeClass("has-error");
    // clear the response messages of the modal
    clearResponseMessage($('#addInvariantModal'));
}

function getSys() {
    var sel = document.getElementById("MySystem");
    var selectedIndex = sel.selectedIndex;
    return sel.options[selectedIndex].value;
}

function aoColumnsFunc(tableId) {
    var doc = new Doc();
    var aoColumns = [
        {"data": null,
            "bSortable": false,
            "bSearchable": false,
            "title": doc.getDocLabel("page_invariant", "button_col"),
            "mRender": function (data, type, obj) {
                var hasPermissions = $("#" + tableId).attr("hasPermissions");

                var editInvariant = '<button id="editInvariant" onclick="editEntryClick(\'' + obj["idName"] + '\',\'' + obj["value"] + '\');"\n\
                                        class="editApplication btn btn-default btn-xs margin-right5" \n\
                                        name="editInvariant" title="' + doc.getDocLabel("page_invariant", "button_edit") + '" type="button">\n\
                                        <span class="glyphicon glyphicon-pencil"></span></button>';
                var removeInvariant = '<button id="removeInvariant" onclick="removeEntryClick(\'' + obj["idName"] + '\',\'' + obj["value"] + '\');"\n\
                                        class="removeInvariant btn btn-default btn-xs margin-right5" \n\
                                        name="removeInvariant" title="' + doc.getDocLabel("page_invariant", "button_remove") + '" type="button">\n\
                                        <span class="glyphicon glyphicon-remove"></span></button>';

                return '<div class="center btn-group width150">' + editInvariant + removeInvariant + '</div>';

            },
            "width": "50px"
        },
        {"data": "idName", "sName": "idname", "title": doc.getDocLabel("page_invariant", "idname")},
        {"data": "value", "sName": "value", "title": doc.getDocLabel("page_invariant", "value")},
        {"data": "sort", "sName": "sort", "title": doc.getDocLabel("page_invariant", "sort")},
        {"data": "description", "description": "value", "title": doc.getDocLabel("page_invariant", "description")},
        {"data": "veryShortDesc", "sName": "VeryShortDesc", "title": doc.getDocLabel("page_invariant", "VeryShortDesc")},
        {"data": "gp1", "sName": "gp1", "title": doc.getDocLabel("page_invariant", "gp1")},
        {"data": "gp2", "sName": "gp2", "title": doc.getDocLabel("page_invariant", "gp2")},
        {"data": "gp3", "sName": "gp3", "title": doc.getDocLabel("page_invariant", "gp3")}
    ];
    return aoColumns;
}

function aoColumnsFunc2(tableId) {
    var doc = new Doc();
    var aoColumns = [
        {"data": "idName", "sName": "idname", "title": doc.getDocLabel("page_invariant", "idname")},
        {"data": "value", "sName": "value", "title": doc.getDocLabel("page_invariant", "value")},
        {"data": "sort", "sName": "sort", "title": doc.getDocLabel("page_invariant", "sort")},
        {"data": "description", "description": "value", "title": doc.getDocLabel("page_invariant", "description")},
        {"data": "veryShortDesc", "sName": "VeryShortDesc", "title": doc.getDocLabel("page_invariant", "VeryShortDesc")},
        {"data": "gp1", "sName": "gp1", "title": doc.getDocLabel("page_invariant", "gp1")},
        {"data": "gp2", "sName": "gp2", "title": doc.getDocLabel("page_invariant", "gp2")},
        {"data": "gp3", "sName": "gp3", "title": doc.getDocLabel("page_invariant", "gp3")}
    ];
    return aoColumns;
}
