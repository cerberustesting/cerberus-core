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

    //clear the modals fields when closed
    $('#editInvariantModal').on('hidden.bs.modal', editEntryModalCloseHandler);

    //configure and create the dataTable
    var configurations = new TableConfigurationsServerSide("invariantsTable", "ReadInvariant?system=" + getSys(), "contentTable", aoColumnsFunc(), [1, 'asc']);
    createDataTableWithPermissions(configurations, renderOptionsForApplication, "#invariantList");
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
    if ($("#blankSpace").length === 0) {
        var contentToAdd = "<div class='marginBottom10' style='height:25px;' id='blankSpace'></div>";
        $("#invariantsTable_wrapper div#invariantsTable_length").before(contentToAdd);
    }
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
                formEdit.find("#veryShortDescField").prop("value", data.invariant.veryShortDescField);
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

function editEntryModalSaveHandler() {
    clearResponseMessage($('#editInvariantModal'));
    var formEdit = $('#editInvariantModal #editInvariantModalForm');

    var sa = formEdit.serializeArray();
    var data = {}
    for(var i in sa){
        data[sa[i].name] = sa[i].value;
    }
    // Get the header data from the form.
    //var data = convertSerialToJSONObject(formEdit.serialize());
    console.log(data);
    showLoaderInModal('#editInvariantModal');
    $.ajax({
        url: "UpdateInvariant2?system="+getSys(),
        async: true,
        method: "POST",
        data: {id: data.invariant,
            valueCerberus: data.cerberusValue,
            valueSystem: data.systemValue,
            system: getSys()},
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

function editEntryModalCloseHandler() {
    // reset form values
    $('#editInvariantModal #editInvariantModalForm')[0].reset();
    // remove all errors on the form fields
    $(this).find('div.has-error').removeClass("has-error");
    // clear the response messages of the modal
    clearResponseMessage($('#editInvariantModal'));
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

                return '<div class="center btn-group width150">' + editInvariant + '</div>';

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
