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
        $('[data-toggle="popover"]').popover({
            'placement': 'auto',
            'container': 'body'}
        );

        $('#addLabelModal #parentLabel').change(function () {
            changeLabelParent("addLabelModal");
        });
        $('#editLabelModal #parentLabel').change(function () {
            changeLabelParent("editLabelModal");
        });

        generateLabelTree();

        var doc = new Doc();

        $('#labelList #createLabelButtonTreeR').click(function () {
            addEntryClick("REQUIREMENT");
            refreshParentLabelCombo("REQUIREMENT");
            showHideRequirementPanelAdd();
        });
        $('#labelList #expandAllTreeR').click(function () {
            $('#mainTreeR').treeview('expandAll', {levels: 20, silent: true});
        });
        $('#labelList #collapseAllTreeR').click(function () {
            $('#mainTreeR').treeview('collapseAll', {levels: 20, silent: true});
        });

        $('#labelList #createLabelButtonTreeS').click(function () {
            addEntryClick("STICKER");
            refreshParentLabelCombo("STICKER");
            showHideRequirementPanelAdd();
        });
        $('#labelList #expandAllTreeS').click(function () {
            $('#mainTreeS').treeview('expandAll', {levels: 20, silent: true});
        });
        $('#labelList #collapseAllTreeS').click(function () {
            $('#mainTreeS').treeview('collapseAll', {levels: 20, silent: true});
        });

        $('#labelList #createLabelButtonTreeB').click(function () {
            addEntryClick("BATTERY");
            refreshParentLabelCombo("BATTERY");
            showHideRequirementPanelAdd();
        });
        $('#labelList #expandAllTreeB').click(function () {
            $('#mainTreeB').treeview('expandAll', {levels: 20, silent: true});
        });
        $('#labelList #collapseAllTreeB').click(function () {
            $('#mainTreeB').treeview('collapseAll', {levels: 20, silent: true});
        });

    });
});

function initPage() {
    displayPageLabel();
    // handle the click for specific action buttons
    $("#addLabelButton").click(addEntryModalSaveHandler);
    $("#editLabelButton").click(editEntryModalSaveHandler);

    //clear the modals fields when closed
    $('#addLabelModal').on('hidden.bs.modal', addEntryModalCloseHandler);
    $('#editLabelModal').on('hidden.bs.modal', editEntryModalCloseHandler);

    $('#editLabelModal #editLabelModalForm #type').on('change', showHideRequirementPanelEdit);

    $('#addLabelModal #addLabelModalForm #type').on('change', showHideRequirementPanelAdd);

    tinymce.init({
        selector: ".wysiwyg"
    });

    //configure and create the dataTable
    var configurations = new TableConfigurationsServerSide("labelsTable", "ReadLabel?system=" + getUser().defaultSystem, "contentTable", aoColumnsFunc("labelsTable"), [2, 'asc']);
    createDataTableWithPermissions(configurations, renderOptionsForLabel, "#labelList", undefined, true);
}

function displayPageLabel() {
    var doc = new Doc();

    displayHeaderLabel(doc);
    $("#pageTitle").html(doc.getDocLabel("page_label", "title"));
    $("#title").html(doc.getDocOnline("page_label", "title"));
    $("[name='createLabelField']").html(doc.getDocLabel("page_label", "btn_create"));
    $("[name='confirmationField']").html(doc.getDocLabel("page_label", "btn_delete"));
    $("[name='editLabelField']").html(doc.getDocLabel("page_label", "btn_edit"));
    $("[name='buttonAdd']").html(doc.getDocLabel("page_global", "buttonAdd"));
    $("[name='buttonClose']").html(doc.getDocLabel("page_global", "buttonClose"));
    $("[name='buttonConfirm']").html(doc.getDocLabel("page_global", "buttonConfirm"));
    $("[name='buttonDismiss']").html(doc.getDocLabel("page_global", "buttonDismiss"));
    $("[name='labelField']").html(doc.getDocOnline("label", "label"));
    $("[name='descriptionField']").html(doc.getDocOnline("label", "description"));
    $("[name='colorField']").html(doc.getDocOnline("label", "color"));
    $("[name='typeField']").html(doc.getDocOnline("label", "type"));
    $("[name='reqtypeField']").html(doc.getDocOnline("label", "reqtype"));
    $("[name='reqstatusField']").html(doc.getDocOnline("label", "reqstatus"));
    $("[name='reqcriticityField']").html(doc.getDocOnline("label", "reqcriticity"));
    $("[name='longdescField']").html(doc.getDocOnline("label", "longdesc"));
    $("[name='parentLabelField']").html(doc.getDocOnline("label", "parentid"));
    $("[name='tabsEdit1']").html(doc.getDocOnline("page_label", "tabDef"));
    $("[name='tabsEdit2']").html(doc.getDocOnline("page_label", "tabEnv"));

    displayInvariantList("system", "SYSTEM", false, '', '');
    displayInvariantList("type", "LABELTYPE", false);
    displayInvariantList("reqtype", "REQUIREMENTTYPE", false);
    displayInvariantList("reqstatus", "REQUIREMENTSTATUS", false);
    displayInvariantList("reqcriticity", "REQUIREMENTCRITICITY", false);

    refreshParentLabelCombo($("#type").val());
    $("#type").change(function () {
        refreshParentLabelCombo($("#type").val());
    });
    displayFooter(doc);
}

function generateLabelTree() {
    $.when($.ajax("ReadLabel?system=" + getUser().defaultSystem + "&withHierarchy=true")).then(function (data) {

        $('#mainTreeS').treeview({data: data.labelHierarchy.stickers});
        $('#mainTreeB').treeview({data: data.labelHierarchy.batteries});
        $('#mainTreeR').treeview({data: data.labelHierarchy.requirements});

    });
}

function refreshParentLabelCombo(type) {
    $("[name='parentLabel']").select2(getComboConfigLabel(type, getUser().defaultSystem));
}

function renderOptionsForLabel(data) {
    var doc = new Doc();
    //check if user has permissions to perform the add and import operations
    if (data["hasPermissions"]) {
        if ($("#createLabelButton").length === 0) {
            var contentToAdd = "<div class='marginBottom10'><button id='createLabelButton' type='button' class='btn btn-default'>\n\
            <span class='glyphicon glyphicon-plus-sign'></span> " + doc.getDocLabel("page_label", "btn_create") + "</button></div>";
            $("#labelsTable_wrapper div#labelsTable_length").before(contentToAdd);
            $('#labelList #createLabelButton').click(addEntryClick);
        }
    }
}

function changeLabelParent(modal) {

    var doc = new Doc();

    $('#' + modal + ' #parentLabel').parent().find(".input-group-btn").remove();

    if (!isEmpty($('#' + modal + ' #parentLabel').val())) {
        var emptyEntry = '<span class="input-group-btn" style="vertical-align:bottom!important"><button id="emptyEntry" onclick="emptyService();"\n\
            class="buttonObject btn btn-default " \n\
           title="Empty" type="button">\n\
            <span class="glyphicon glyphicon-remove"></span></button></span>';
        $('#' + modal + ' #parentLabel').parent().append(emptyEntry);

    }
}

function emptyService() {
    $('#addLabelModal #parentLabel').val(null).trigger('change');
    $('#editLabelModal #parentLabel').val(null).trigger('change');
}


function showHideRequirementPanelEdit() {

    refreshParentLabelCombo($('#editLabelModal #editLabelModalForm #type').val());
    if ($('#editLabelModal #editLabelModalForm #type').val() === "REQUIREMENT") {
        $("#editLabelModal #panelReq").show();
    } else {
        $("#editLabelModal #panelReq").hide();
        $("#editLabelModal #panelReq #reqtype").val("");
        $("#editLabelModal #panelReq #reqstatus").val("");
        $("#editLabelModal #panelReq #reqcriticity").val("");
    }

}

function showHideRequirementPanelAdd() {

    refreshParentLabelCombo($('#addLabelModal #addLabelModalForm #type').val());
    if ($('#addLabelModal #addLabelModalForm #type').val() === "REQUIREMENT") {
        $("#addLabelModal #panelReq").show();
    } else {
        $("#addLabelModal #panelReq").hide();
        $("#editLabelModal #panelReq #reqtype").val("");
        $("#editLabelModal #panelReq #reqstatus").val("");
        $("#editLabelModal #panelReq #reqcriticity").val("");
    }

}

function deleteEntryHandlerClick() {
    var idLabel = $('#confirmationModal').find('#hiddenField1').prop("value");
    var jqxhr = $.post("DeleteLabel", {id: idLabel}, "json");
    $.when(jqxhr).then(function (data) {
        var messageType = getAlertType(data.messageType);
        if (messageType === "success") {
            // Redraw the datatable
            var oTable = $("#labelsTable").dataTable();
            oTable.fnDraw(true);
            var info = oTable.fnGetData().length;
            if (info === 1) {//page has only one row, then returns to the previous page
                oTable.fnPageChange('previous');
            }

        }
        // Show message in the main page
        showMessageMainPage(messageType, data.message, false);
        //close confirmation window
        $('#confirmationModal').modal('hide');
    }).fail(handleErrorAjaxAfterTimeout);
}

function deleteEntryClick(id, label) {
    clearResponseMessageMainPage();
    var doc = new Doc();
    var messageComplete = doc.getDocLabel("page_global", "message_delete");
    messageComplete = messageComplete.replace("%ENTRY%", id + " - " + label);
    messageComplete = messageComplete.replace("%TABLE%", " label ");
    showModalConfirmation(deleteEntryHandlerClick, undefined, doc.getDocLabel("page_label", "btn_delete"), messageComplete, id, "", "", "");
}

function addEntryModalSaveHandler() {
    clearResponseMessage($('#addLabelModal'));
    var formAdd = $("#addLabelModal #addLabelModalForm");
    var nameElement = formAdd.find("#addLabelModalForm");
    var nameElementEmpty = nameElement.prop("value") === '';
    if (nameElementEmpty) {
        var localMessage = new Message("danger", "Please specify label!");
        nameElement.parents("div.form-group").addClass("has-error");
        showMessage(localMessage, $('#addLabelModal'));
    } else {
        nameElement.parents("div.form-group").removeClass("has-error");
    }

// verif if all mendatory fields are not empty
    if (nameElementEmpty)
        return;
    // Get the header data from the form.
    var dataForm = convertSerialToJSONObject(formAdd.serialize());
    showLoaderInModal('#addLabelModal');
    var jqxhr = $.post("CreateLabel", dataForm);
    $.when(jqxhr).then(function (data) {
        hideLoaderInModal('#addLabelModal');
//        console.log(data.messageType);
        if (getAlertType(data.messageType) === 'success') {
            var oTable = $("#labelsTable").dataTable();
            oTable.fnDraw(true);
            showMessage(data);
            $('#addLabelModal').modal('hide');
        } else {
            showMessage(data, $('#addLabelModal'));
        }
    }).fail(handleErrorAjaxAfterTimeout);
}

function addEntryModalCloseHandler() {
// reset form values
    $('#addLabelModal #addLabelModalForm')[0].reset();
    // remove all errors on the form fields
    $(this).find('div.has-error').removeClass("has-error");
    // clear the response messages of the modal
    clearResponseMessage($('#addLabelModal'));
}

function addEntryClick(type) {
    clearResponseMessageMainPage();
    // When creating a new label, System takes the default value of the 
    // system already selected in header.
    var formAdd = $('#addLabelModal');
    formAdd.find("#system").prop("value", getUser().defaultSystem);
    $('#addLabelModal').modal('show');
    //ColorPicker
    $("[name='colorDiv']").colorpicker();
    $("[name='colorDiv']").colorpicker('setValue', '#000000');
    if (type !== undefined) {
        $("[name='type']").val(type);
    }
}

function editEntryModalSaveHandler() {
    clearResponseMessage($('#editLabelModal'));
    var formEdit = $('#editLabelModal #editLabelModalForm');
    tinyMCE.triggerSave();
    // Get the header data from the form.
    var data = convertSerialToJSONObject(formEdit.serialize());
    showLoaderInModal('#editLabelModal');
    $.ajax({
        url: "UpdateLabel",
        async: true,
        method: "POST",
        data: {id: data.id,
            label: data.label,
            color: data.color,
            parentLabel: data.parentLabel,
            system: data.system,
            type: data.type,
            longdesc: data.longdesc,
            reqtype: data.reqtype,
            reqstatus: data.reqstatus,
            reqcriticity: data.reqcriticity,
            description: data.description},
        success: function (data) {
            hideLoaderInModal('#editLabelModal');
            if (getAlertType(data.messageType) === "success") {
                var oTable = $("#labelsTable").dataTable();
                oTable.fnDraw(true);
                $('#editLabelModal').modal('hide');
                showMessage(data);
            } else {
                showMessage(data, $('#editLabelModal'));
            }
        },
        error: showUnexpectedError
    });
}

function editEntryModalCloseHandler() {
// reset form values
    $('#editLabelModal #editLabelModalForm')[0].reset();
    // remove all errors on the form fields
    $(this).find('div.has-error').removeClass("has-error");
    // clear the response messages of the modal
    clearResponseMessage($('#editLabelModal'));
}

function editEntryClick(id, system) {
    clearResponseMessageMainPage();
    var jqxhr = $.getJSON("ReadLabel", "id=" + id);
    $.when(jqxhr).then(function (data) {
        var obj = data["contentTable"];
        var formEdit = $('#editLabelModal');
        formEdit.find("#id").prop("value", id);
        formEdit.find("#label").prop("value", obj["label"]);
        formEdit.find("#color").prop("value", obj["color"]);
        formEdit.find("#type").prop("value", obj["type"]);
        formEdit.find("#description").prop("value", obj["description"]);
        formEdit.find("#longdesc").prop("value", obj["longDesc"]);
        formEdit.find("#reqtype").prop("value", obj["reqType"]);
        formEdit.find("#reqstatus").prop("value", obj["reqStatus"]);
        formEdit.find("#reqcriticity").prop("value", obj["reqCriticity"]);
        formEdit.find("#system").prop("value", obj["system"]);
        if (tinyMCE.get('longdesc') != null)
            tinyMCE.get('longdesc').setContent(obj["longDesc"]);
        if (!(data["hasPermissions"])) { // If readonly, we only readonly all fields
            formEdit.find("#label").prop("readonly", "readonly");
            formEdit.find("#color").prop("readonly", "readonly");
            formEdit.find("#parentLabel").prop("disabled", "disabled");
            formEdit.find("#description").prop("disabled", "disabled");
            formEdit.find("#system").prop("disabled", "disabled");
            $('#editLabelButton').attr('class', '');
            $('#editLabelButton').attr('hidden', 'hidden');
        }

        $("#editLabelModal #editLabelModalForm #parentLabel").val(obj.parentLabelID === 0 ? "" : obj.parentLabelID).trigger('change');


        // ColorPicker
        $("[name='colorDiv']").colorpicker();
        $("[name='colorDiv']").colorpicker('setValue', obj["color"]);
        showHideRequirementPanelEdit();
        formEdit.modal('show');
    });
}

function aoColumnsFunc(tableId) {
    var doc = new Doc();
    var aoColumns = [
        {"data": null,
            "title": doc.getDocLabel("page_global", "columnAction"),
            "bSortable": false,
            "bSearchable": false,
            "sWidth": "50px",
            "mRender": function (data, type, obj) {
                var hasPermissions = $("#" + tableId).attr("hasPermissions");
                var editLabel = '<button id="editLabel" onclick="editEntryClick(\'' + obj["id"] + '\', \'' + obj["system"] + '\');"\n\
                                    class="editLabel btn btn-default btn-xs margin-right5" \n\
                                    name="editLabel" title="' + doc.getDocLabel("page_label", "btn_edit") + '" type="button">\n\
                                    <span class="glyphicon glyphicon-pencil"></span></button>';
                var viewLabel = '<button id="editLabel" onclick="editEntryClick(\'' + obj["id"] + '\', \'' + obj["system"] + '\');"\n\
                                    class="editLabel btn btn-default btn-xs margin-right5" \n\
                                    name="editLabel" title="' + doc.getDocLabel("page_label", "btn_view") + '" type="button">\n\
                                    <span class="glyphicon glyphicon-eye-open"></span></button>';
                var deleteLabel = '<button id="deleteLabel" onclick="deleteEntryClick(\'' + obj["id"] + '\',\'' + obj["label"] + '\');" \n\
                                    class="deleteLabel btn btn-default btn-xs margin-right5" \n\
                                    name="deleteLabel" title="' + doc.getDocLabel("page_label", "btn_delete") + '" type="button">\n\
                                    <span class="glyphicon glyphicon-trash"></span></button>';
                if (hasPermissions === "true") { //only draws the options if the user has the correct privileges
                    return '<div class="center btn-group width150">' + editLabel + deleteLabel + '</div>';
                }
                return '<div class="center btn-group width150">' + viewLabel + '</div>';
            }
        },
        {"data": "id",
            "like": true,
            "sWidth": "30px",
            "sName": "id",
            "title": doc.getDocOnline("label", "id")},
        {"data": "system",
            "sWidth": "50px",
            "sName": "system",
            "title": doc.getDocOnline("label", "system")},
        {"data": "label",
            "sWidth": "50px",
            "sName": "label",
            "title": doc.getDocOnline("label", "label")},
        {"data": "longDesc",
            "like": true,
            "sWidth": "100px",
            "sName": "longDesc",
            "title": doc.getDocOnline("label", "longdesc")},
        {"data": "description",
            "like": true,
            "sWidth": "100px",
            "sName": "description",
            "title": doc.getDocOnline("label", "description")},
        {"data": "type",
            "sWidth": "50px",
            "sName": "type",
            "title": doc.getDocOnline("label", "type")},
        {"data": "color",
            "sWidth": "30px",
            "like": true,
            "sName": "color",
            "title": doc.getDocOnline("label", "color")},
        {"data": "display",
            "sWidth": "80px",
            "sName": "display",
            "title": doc.getDocOnline("page_label", "display"),
            "bSortable": false,
            "bSearchable": false,
            "render": function (data, type, full, meta) {
                return '<span class="label label-primary" style="background-color:' + data.color + '">' + data.label + '</span> ';
            }
        },
        {"sName": "parentLabelid",
            "sWidth": "80px",
            "title": doc.getDocOnline("label", "parentid"),
            "data": function (data, type, full, meta) {
                if (data.labelParentObject !== undefined) {
                    //return '<span class="label label-primary" style="background-color:' + data.display.color + '">' + data.display.label + '</span> ';
                    return '<div style="float:left"><span class="label label-primary" onclick="filterOnLabel(this)" style="cursor:pointer;background-color:' + data.labelParentObject.color + '" data-toggle="tooltip" data-labelid="' + data.labelParentObject.id + '" title="' + data.labelParentObject.description + '">' + data.labelParentObject.label + '</span></div> ';
                } else {

                    return '';
                }
            }},
        {"data": "reqType",
            "sWidth": "30px",
            "sName": "reqType",
            "title": doc.getDocOnline("label", "reqtype")},
        {"data": "reqStatus",
            "sWidth": "30px",
            "sName": "reqStatus",
            "title": doc.getDocOnline("label", "reqstatus")},
        {"data": "reqCriticity",
            "sWidth": "30px",
            "sName": "reqCriticity",
            "title": doc.getDocOnline("label", "reqcriticity")},
        {"data": "usrCreated",
            "sWidth": "30px",
            "sName": "usrCreated",
            "title": doc.getDocOnline("transversal", "UsrCreated")},
        {"data": "dateCreated",
            "like": true,
            "sWidth": "80px",
            "sName": "dateCreated",
            "title": doc.getDocOnline("transversal", "DateCreated")},
        {"data": "usrModif",
            "sWidth": "30px",
            "sName": "usrModif",
            "title": doc.getDocOnline("transversal", "UsrModif")
        },
        {"data": "dateModif",
            "like": true,
            "sWidth": "80px",
            "sName": "dateModif",
            "title": doc.getDocOnline("transversal", "DateModif")
        }

    ];
    return aoColumns;
}

function filterOnLabel(element) {
    var newLabel = $(element).attr('data-labelid');
    var colIndex = $(element).parent().parent().get(0).cellIndex;
    $("#labelsTable").dataTable().fnFilter(newLabel, colIndex);
}

function afterTableLoad() {
    generateLabelTree();
}

