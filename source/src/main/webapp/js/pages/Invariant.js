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
$.when($.getScript("js/global/global.js")).then(function () {
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
        if (e.target.href.indexOf("private") !== -1) {
            displayPrivateTable();
        } else {
            displayPublicTable();
        }
    });
    displayPublicTable();
}

function displayPrivateTable() {
    if ($.fn.dataTable.isDataTable('#invariantsPrivateTable')) {
        $('#invariantsPrivateTable').DataTable();
    } else {
        //configure and create the dataTable
        var configurationsPriv = new TableConfigurationsServerSide("invariantsPrivateTable", "ReadInvariant?access=PRIVATE", "contentTable", aoColumnsFunc2(), [1, 'asc']);
        createDataTableWithPermissions(configurationsPriv, renderOptionsForApplication2, "#invariantPrivateList", undefined, true);
    }
}

function displayPublicTable() {
    if ($.fn.dataTable.isDataTable('#invariantsTable')) {
        $('#invariantsTable').DataTable();
    } else {
        //configure and create the dataTable
        var configurations = new TableConfigurationsServerSide("invariantsTable", "ReadInvariant?access=PUBLIC", "contentTable", aoColumnsFunc(), [1, 'asc']);
        createDataTableWithPermissions(configurations, renderOptionsForApplication, "#invariantList", undefined, true);
    }
}

function displayPageLabel() {
    var doc = new Doc();

    $("#title").html(doc.getDocLabel("page_invariant", "allInvariants"));
    $("[name='editInvariantField']").html(doc.getDocLabel("page_invariant", "editinvariant_field"));
    $("[name='addInvariantField']").html(doc.getDocLabel("page_invariant", "addinvariant_field"));
    $("[name='descriptionField']").html(doc.getDocLabel("page_invariant", "description"));
    $("[name='idnameField']").html(doc.getDocLabel("page_invariant", "idname"));
    $("[name='ValueField']").html(doc.getDocLabel("page_invariant", "value"));
    $("[name='sortField']").html(doc.getDocLabel("page_invariant", "sort"));
    $("[name='veryShortDescField']").html(doc.getDocLabel("page_invariant", "veryShortDesc"));
    $("[name='gp1Field']").html(doc.getDocLabel("page_invariant", "gp1"));
    $("[name='gp2Field']").html(doc.getDocLabel("page_invariant", "gp2"));
    $("[name='gp3Field']").html(doc.getDocLabel("page_invariant", "gp3"));
    $("[name='gp4Field']").html(doc.getDocLabel("page_invariant", "gp4"));
    $("[name='gp5Field']").html(doc.getDocLabel("page_invariant", "gp5"));
    $("[name='gp6Field']").html(doc.getDocLabel("page_invariant", "gp6"));
    $("[name='gp7Field']").html(doc.getDocLabel("page_invariant", "gp7"));
    $("[name='gp8Field']").html(doc.getDocLabel("page_invariant", "gp8"));
    $("[name='gp9Field']").html(doc.getDocLabel("page_invariant", "gp9"));
    $("[name='buttonClose']").html(doc.getDocLabel("page_invariant", "close_btn"));
    $("[name='buttonAdd']").html(doc.getDocLabel("page_invariant", "save_btn"));
    $("#invariantListLabel").html("<span class='glyphicon glyphicon-list'></span> " + doc.getDocLabel("page_invariant", "public_invariant"))
    $("#invariantPrivateListLabel").html("<span class='glyphicon glyphicon-list'></span> " + doc.getDocLabel("page_invariant", "private_invariant"))
    $("a[href='#public']").html(doc.getDocLabel("page_invariant", "public"));
    $("a[href='#private']").html(doc.getDocLabel("page_invariant", "private"));

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
        var contentToAdd = "<div class='marginBottom10' style='height:34px;' id='blankSpace'></div>";
        $("#invariantsPrivateTable_wrapper div#invariantsPrivateTable_length").before(contentToAdd);
    }
}

function addEntryClick() {
    clearResponseMessageMainPage();
    $("#addInvariantModal #idname").empty();
    getInvariantList("INVARIANTPUBLIC", function (data) {
        for (var i in data) {
            $("#addInvariantModal #idname").append($("<option>" + i + "</option>"))
        }
    });

    $('#addInvariantModal').modal('show');
}

function editEntryClick(param, value) {
    var formEdit = $('#editInvariantModal');

    $.ajax({
        url: "ReadInvariant?idName=" + param + "&value=" + value,
        async: true,
        method: "GET",
        success: function (data) {
            if (data.messageType === "OK") {
                formEdit.find("#idname").prop("value", data.invariant.idName);
                formEdit.find("#value").prop("value", data.invariant.value);
                formEdit.find("#sort").prop("value", data.invariant.sort);
                formEdit.find("#description").prop("value", data.invariant.description);
                formEdit.find("#veryShortDesc").prop("value", data.invariant.veryShortDesc);
                formEdit.find("#gp1").prop("value", data.invariant.gp1);
                formEdit.find("#gp2").prop("value", data.invariant.gp2);
                formEdit.find("#gp3").prop("value", data.invariant.gp3);
                formEdit.find("#gp4").prop("value", data.invariant.gp4);
                formEdit.find("#gp5").prop("value", data.invariant.gp5);
                formEdit.find("#gp6").prop("value", data.invariant.gp6);
                formEdit.find("#gp7").prop("value", data.invariant.gp7);
                formEdit.find("#gp8").prop("value", data.invariant.gp8);
                formEdit.find("#gp9").prop("value", data.invariant.gp9);

                formEdit.modal('show');
            } else {
                showUnexpectedError();
            }
        },
        error: showUnexpectedError
    });
}

function viewEntryClick(param, value) {
    var formEdit = $('#editInvariantModal');

    $.ajax({
        url: "ReadInvariant?idName=" + param + "&value=" + value,
        async: true,
        method: "GET",
        success: function (data) {
            if (data.messageType === "OK") {
                formEdit.find("#idname").prop("value", data.invariant.idName);
                formEdit.find("#value").prop("value", data.invariant.value);
                formEdit.find("#sort").prop("value", data.invariant.sort);
                formEdit.find("#description").prop("value", data.invariant.description);
                formEdit.find("#veryShortDesc").prop("value", data.invariant.veryShortDesc);
                formEdit.find("#gp1").prop("value", data.invariant.gp1);
                formEdit.find("#gp2").prop("value", data.invariant.gp2);
                formEdit.find("#gp3").prop("value", data.invariant.gp3);
                formEdit.find("#gp4").prop("value", data.invariant.gp4);
                formEdit.find("#gp5").prop("value", data.invariant.gp5);
                formEdit.find("#gp6").prop("value", data.invariant.gp6);
                formEdit.find("#gp7").prop("value", data.invariant.gp7);
                formEdit.find("#gp8").prop("value", data.invariant.gp8);
                formEdit.find("#gp9").prop("value", data.invariant.gp9);

                formEdit.find("#idname").prop("readonly", "readonly");
                formEdit.find("#value").prop("readonly", "readonly");
                formEdit.find("#sort").prop("readonly", "readonly");
                formEdit.find("#description").prop("readonly", "readonly");
                formEdit.find("#veryShortDesc").prop("readonly", "readonly");
                formEdit.find("#gp1").prop("readonly", "readonly");
                formEdit.find("#gp2").prop("readonly", "readonly");
                formEdit.find("#gp3").prop("readonly", "readonly");

                $('#editInvariantButton').attr('class', '');
                $('#editInvariantButton').attr('hidden', 'hidden');

                formEdit.modal('show');
            } else {
                showUnexpectedError();
            }
        },
        error: showUnexpectedError
    });
}

function removeEntryClick(param, value) {
    var doc = new Doc();
    showModalConfirmation(deleteEntryHandlerClick, doc.getDocLabel("page_invariant", "message_remove"), doc.getDocLabel("page_invariant", "message_remove"), param, value, "", "");
}

function deleteEntryHandlerClick() {
    var param = $('#confirmationModal #hiddenField1').prop("value");
    var value = $('#confirmationModal #hiddenField2').prop("value");
    var jqxhr = $.post("DeleteInvariant2", {idName:param,value:value}, "json");
    $.when(jqxhr).then(function (data) {
        console.log(data);
        console.log(data.messageType);
        var messageType = getAlertType(data.messageType);
        console.log(messageType);
        if (messageType === "success") {
            //redraw the datatable
            var oTable = $("#invariantsTable").dataTable();
            oTable.fnDraw(true);
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
            gp3: data.gp3,
            gp4: data.gp4,
            gp5: data.gp5,
            gp6: data.gp6,
            gp7: data.gp7,
            gp8: data.gp8,
            gp9: data.gp9
        },
        success: function (data) {
            data = JSON.parse(data);
            hideLoaderInModal('#editInvariantModal');
            if (getAlertType(data.messageType) === 'success') {
                var oTable = $("#invariantsTable").dataTable();
                oTable.fnDraw(true);
                $('#editInvariantModal').modal('hide');
                showMessage(data);
            } else {
                showMessage(data, $('#editInvariantModal'));
            }
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
            gp3: data.gp3,
            gp4: data.gp4,
            gp5: data.gp5,
            gp6: data.gp6,
            gp7: data.gp7,
            gp8: data.gp8,
            gp9: data.gp9
        },
        success: function (data) {
            data = JSON.parse(data);
            hideLoaderInModal('#addInvariantModal');
            if (getAlertType(data.messageType) === 'success') {
                var oTable = $("#invariantsTable").dataTable();
                oTable.fnDraw(true);
                $('#addInvariantModal').modal('hide');
                showMessage(data);
            } else {
                showMessage(data, $('#addInvariantModal'));
            }
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
                                        <span class="glyphicon glyphicon-trash"></span></button>';

                return '<div class="center btn-group width150">' + editInvariant + removeInvariant + '</div>';

            },
            "width": "50px"
        },
        {"data": "idName", "sName": "idname", "title": doc.getDocLabel("page_invariant", "idname")},
        {"data": "value", "sName": "value", "title": doc.getDocLabel("page_invariant", "value")},
        {"data": "sort", "sName": "sort", "title": doc.getDocLabel("page_invariant", "sort")},
        {"data": "description", "sName": "description", "title": doc.getDocLabel("page_invariant", "description")},
        {"data": "veryShortDesc", "sName": "VeryShortDesc", "title": doc.getDocLabel("page_invariant", "veryShortDesc")},
        {"data": "gp1", "sName": "gp1", "title": doc.getDocLabel("page_invariant", "gp1")},
        {"data": "gp2", "sName": "gp2", "title": doc.getDocLabel("page_invariant", "gp2")},
        {"data": "gp3", "sName": "gp3", "title": doc.getDocLabel("page_invariant", "gp3")},
        {"data": "gp4", "sName": "gp4", "title": doc.getDocLabel("page_invariant", "gp4")},
        {"data": "gp5", "sName": "gp5", "title": doc.getDocLabel("page_invariant", "gp5")},
        {"data": "gp6", "sName": "gp6", "title": doc.getDocLabel("page_invariant", "gp6")},
        {"data": "gp7", "sName": "gp7", "title": doc.getDocLabel("page_invariant", "gp7")},
        {"data": "gp8", "sName": "gp8", "title": doc.getDocLabel("page_invariant", "gp8")},
        {"data": "gp9", "sName": "gp9", "title": doc.getDocLabel("page_invariant", "gp9")}
    ];
    return aoColumns;
}

function aoColumnsFunc2(tableId) {
    var doc = new Doc();
    var aoColumns = [
        {"data": null,
            "bSortable": false,
            "bSearchable": false,
            "title": doc.getDocLabel("page_invariant", "button_col"),
            "mRender": function (data, type, obj) {
                var viewInvariant = '<button id="editInvariant" onclick="viewEntryClick(\'' + obj["idName"] + '\',\'' + obj["value"] + '\');"\n\
                                    class="editApplication btn btn-default btn-xs margin-right5" \n\
                                    name="viewInvariant" title="' + doc.getDocLabel("page_invariant", "button_edit") + '" type="button">\n\
                                    <span class="glyphicon glyphicon-eye-open"></span></button>';

                return '<div class="center btn-group width150">' + viewInvariant + '</div>';

            },
            "width": "50px"
        },
        {"data": "idName", "sName": "idname", "title": doc.getDocLabel("page_invariant", "idname")},
        {"data": "value", "sName": "value", "title": doc.getDocLabel("page_invariant", "value")},
        {"data": "sort", "sName": "sort", "title": doc.getDocLabel("page_invariant", "sort")},
        {"data": "description", "sName": "description", "title": doc.getDocLabel("page_invariant", "description")},
        {"data": "veryShortDesc", "sName": "VeryShortDesc", "title": doc.getDocLabel("page_invariant", "veryShortDesc")},
        {"data": "gp1", "sName": "gp1", "title": doc.getDocLabel("page_invariant", "gp1")},
        {"data": "gp2", "sName": "gp2", "title": doc.getDocLabel("page_invariant", "gp2")},
        {"data": "gp3", "sName": "gp3", "title": doc.getDocLabel("page_invariant", "gp3")},
        {"data": "gp4", "sName": "gp4", "title": doc.getDocLabel("page_invariant", "gp4")},
        {"data": "gp5", "sName": "gp5", "title": doc.getDocLabel("page_invariant", "gp5")},
        {"data": "gp6", "sName": "gp6", "title": doc.getDocLabel("page_invariant", "gp6")},
        {"data": "gp7", "sName": "gp7", "title": doc.getDocLabel("page_invariant", "gp7")},
        {"data": "gp8", "sName": "gp8", "title": doc.getDocLabel("page_invariant", "gp8")},
        {"data": "gp9", "sName": "gp9", "title": doc.getDocLabel("page_invariant", "gp9")}
    ];
    return aoColumns;
}
