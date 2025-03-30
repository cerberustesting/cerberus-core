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
        $('[data-toggle="popover"]').popover({
            'placement': 'auto',
            'container': 'body'}
        );

        // 
        $('#addLabelModal #parentLabel').change(function () {
            changeLabelParent("addLabelModal");
        });
        $('#editLabelModal #parentLabel').change(function () {
            changeLabelParent("editLabelModal");
        });

        var doc = new Doc();

        generateLabelTree();

        // Tree Requirements butons
        $('#createLabelButtonTreeR').click(function () {
            addEntryClick("REQUIREMENT");
//            refreshParentLabelCombo("REQUIREMENT", "editLabelModalForm");
            showHideRequirementPanelAdd();
        });
        $('#expandAllTreeR').click(function () {
            $('#mainTreeR').treeview('expandAll', {levels: 20, silent: true});
        });
        $('#collapseAllTreeR').click(function () {
            $('#mainTreeR').treeview('collapseAll', {levels: 20, silent: true});
        });
        $('#refreshButtonTreeR').click(function () {
            generateLabelTree();
        });

        // Tree Sticker butons
        $('#createLabelButtonTreeS').click(function () {
            addEntryClick("STICKER");
//            refreshParentLabelCombo("STICKER","editLabelModalForm");
            showHideRequirementPanelAdd();
        });
        $('#expandAllTreeS').click(function () {
            $('#mainTreeS').treeview('expandAll', {levels: 20, silent: true});
        });
        $('#collapseAllTreeS').click(function () {
            $('#mainTreeS').treeview('collapseAll', {levels: 20, silent: true});
        });
        $('#refreshButtonTreeS').click(function () {
            generateLabelTree();
        });

        // Tree Battery butons
        $('#createLabelButtonTreeB').click(function () {
            addEntryClick("BATTERY");
//            refreshParentLabelCombo("BATTERY","editLabelModalForm");
            showHideRequirementPanelAdd();
        });
        $('#expandAllTreeB').click(function () {
            $('#mainTreeB').treeview('expandAll', {levels: 20, silent: true});
        });
        $('#collapseAllTreeB').click(function () {
            $('#mainTreeB').treeview('collapseAll', {levels: 20, silent: true});
        });
        $('#refreshButtonTreeB').click(function () {
            generateLabelTree();
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

//    $('#editLabelModal #editLabelModalForm #type').on('change', showHideRequirementPanelEdit);
//
//    $('#addLabelModal #addLabelModalForm #type').on('change', showHideRequirementPanelAdd);
//
    tinymce.init({
        selector: ".wysiwyg"
    });

    //configure and create the dataTable
    var configurations = new TableConfigurationsServerSide("labelsTable", "ReadLabel?q=1" + getUser().defaultSystemsQuery, "contentTable", aoColumnsFunc("labelsTable"), [2, 'asc']);
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
    $("[name='buttonEdit']").html(doc.getDocLabel("page_global", "buttonEdit"));
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
    displayInvariantList("type", "LABELTYPE", false, undefined, undefined, undefined, undefined, "editLabelModal");
    displayInvariantList("type", "LABELTYPE", false, undefined, undefined, undefined, undefined, "addLabelModal");
    displayInvariantList("reqtype", "REQUIREMENTTYPE", false);
    displayInvariantList("reqstatus", "REQUIREMENTSTATUS", false);
    displayInvariantList("reqcriticity", "REQUIREMENTCRITICITY", false);

    refreshParentLabelCombo($("#type").val(), "editLabelModalForm");
    refreshParentLabelCombo($("#type").val(), "addLabelModalForm");
    $("#editLabelModalForm #type").change(function () {
        refreshParentLabelCombo($("#type").val(), "editLabelModalForm");
    });
    $("#addLabelModalForm #type").change(function () {
        refreshParentLabelCombo($("#type").val(), "addLabelModalForm");
    });
    displayFooter(doc);
}

function generateLabelTree() {
    $.when($.ajax("ReadLabel?q=1" + getUser().defaultSystemsQuery + "&withHierarchy=true&hasButtons=Y")).then(function (data) {

        $('#mainTreeS').treeview({data: data.labelHierarchy.stickers, enableLinks: false, showTags: true});
        $('#mainTreeB').treeview({data: data.labelHierarchy.batteries, enableLinks: false, showTags: true});
        $('#mainTreeR').treeview({data: data.labelHierarchy.requirements, enableLinks: false, showTags: true});

    });
}

function refreshParentLabelCombo(type, form) {
    $("#" + form + " #parentLabel").select2(getComboConfigLabel(type, getUser().defaultSystem));
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

    var par = $('#' + modal + ' #parentLabel').val();
    if (!isEmpty(par) && par !== 0) {
        var emptyEntry = '<span class="input-group-btn" style="vertical-align:bottom!important"><button id="emptyEntry" style="margin-left: 10px;" onclick="emptyService();"\n\
            class="buttonObject btn btn-sm btn-default " \n\
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

    refreshParentLabelCombo($('#editLabelModal #editLabelModalForm #type').val(), "editLabelModalForm");
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

    refreshParentLabelCombo($('#addLabelModal #addLabelModalForm #type').val(), "addLabelModalForm");
    if ($('#addLabelModal #addLabelModalForm #type').val() === "REQUIREMENT") {
        $("#addLabelModal #panelReq").show();
    } else {
        $("#addLabelModal #panelReq").hide();
        $("#addLabelModal #panelReq #reqtype").val("");
        $("#addLabelModal #panelReq #reqstatus").val("");
        $("#addLabelModal #panelReq #reqcriticity").val("");
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
            oTable.fnDraw(false);
            generateLabelTree();
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
            oTable.fnDraw(false);
            generateLabelTree();
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
                oTable.fnDraw(false);
                generateLabelTree();
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
        formEdit.find("#reqtype").prop("value", obj["requirementType"]);
        formEdit.find("#reqstatus").prop("value", obj["requirementStatus"]);
        formEdit.find("#reqcriticity").prop("value", obj["requirementCriticity"]);
        formEdit.find("#system").prop("value", obj["system"]);
        if (tinyMCE.get('longdesc') != null)
            tinyMCE.get('longdesc').setContent(obj["longDescription"]);
        if (!(data["hasPermissions"])) { // If readonly, we only readonly all fields
            formEdit.find("#label").prop("readonly", "readonly");
            formEdit.find("#color").prop("readonly", "readonly");
            formEdit.find("#parentLabel").prop("disabled", "disabled");
            formEdit.find("#description").prop("disabled", "disabled");
            formEdit.find("#system").prop("disabled", "disabled");
            $('#editLabelButton').attr('class', '');
            $('#editLabelButton').attr('hidden', 'hidden');
        }
//        console.info(obj.parentLabelID);

        var $option = $('<option></option>').text(0).val(0);
        if (obj.parentLabelID !== 0) {
            $option = $('<option></option>').text(obj.parentLabelID).val(obj.parentLabelID);
//        console.info($option);            
        }
        $("#editLabelModal #parentLabel").append($option).val(obj.parentLabelID).trigger('change'); // append the option and update Select2

        // ColorPicker
        $("[name='colorDiv']").colorpicker();
        $("[name='colorDiv']").colorpicker('setValue', obj["color"]);
        showHideRequirementPanelEdit();
        formEdit.modal('show');
    });
}


function getComboConfigLabel(labelType, system) {

    var config =
            {
                ajax: {
                    url: "ReadLabel?iSortCol_0=0&sSortDir_0=desc&sColumns=type&iDisplayLength=30&sSearch_0=" + labelType + "&system=" + system,
                    dataType: 'json',
                    delay: 250,
                    data: function (params) {
                        params.page = params.page || 1;
                        return {
                            sSearch: params.term, // search term
                            iDisplayStart: (params.page * 30) - 30
                        };
                    },
                    processResults: function (data, params) {
                        params.page = params.page || 1;
                        return {
                            results: $.map(data.contentTable, function (obj) {
                                return {id: obj.id, label: obj.label, color: obj.color, description: obj.description};
                            }),
                            pagination: {
                                more: (params.page * 30) < data.iTotalRecords
                            }
                        };
                    },
                    cache: true,
                    allowClear: true
                },
                width: "100%",
                escapeMarkup: function (markup) {
                    return markup;
                }, // let our custom formatter work
                minimumInputLength: 0,
                templateResult: comboConfigLabel_format, // omitted for brevity, see the source of this page
                templateSelection: comboConfigLabel_formatSelection // omitted for brevity, see the source of this page
            };

    return config;
}

function comboConfigLabel_format(label) {
    var markup = "<div class='select2-result-tag clearfix'>" +
            "<div style='float:left;'><span class='label label-primary' style='background-color:"
            + label.color + "' data-toggle='tooltip' data-labelid='"
            + label.id + "' title='"
            + label.description + "'>"
            + label.label + "</span></div>";

    markup += "</div>";

    return markup;
}

function comboConfigLabel_formatSelection(label) {
    var result = label.id;
    if (!isEmpty(label.label)) {
        result = "<div style='float:left;height: 34px'><span class='label label-primary' style='background-color:"
                + label.color + "' data-toggle='tooltip' data-labelid='"
                + label.id + "' title='"
                + label.description + "'>"
                + label.label + "</span></div>";
    }
    return result;
}


function aoColumnsFunc(tableId) {
    var doc = new Doc();
    var aoColumns = [
        {"data": null,
            "title": doc.getDocLabel("page_global", "columnAction"),
            "bSortable": false,
            "bSearchable": false,
            "sWidth": "80px",
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
                var tcLabel = '<a id="tcLabel" href="./TestCaseList.jsp?label=' + obj["label"] + '" \n\
                                    class="btn btn-primary btn-xs marginRight5" \n\
                                    name="tcLabel" title="' + doc.getDocLabel("page_label", "btn_tclist") + '" >\n\
                                    <span class="glyphicon glyphicon-list"></span></a>';
                if (hasPermissions === "true") { //only draws the options if the user has the correct privileges
                    return '<div class="center btn-group width150">' + editLabel + deleteLabel + tcLabel + '</div>';
                }
                return '<div class="center btn-group width150">' + viewLabel + '</div>';
            }
        },
        {"data": "id",
            "like": true,
            "sWidth": "30px",
            "sName": "lab.id",
            "title": doc.getDocOnline("label", "id")},
        {"data": "system",
            "sWidth": "50px",
            "sName": "system",
            "title": doc.getDocOnline("label", "system")},
        {"data": "label",
            "sWidth": "50px",
            "sName": "label",
            "title": doc.getDocOnline("label", "label")},
        {"data": "longDescription",
            "visible": false,
            "like": true,
            "sWidth": "100px",
            "sName": "longDescription",
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
            "visible": false,
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
            "visible": false,
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
        {"data": "requirementType",
            "visible": false,
            "sWidth": "30px",
            "sName": "requirementType",
            "title": doc.getDocOnline("label", "reqtype")},
        {"data": "requirementStatus",
            "visible": false,
            "sWidth": "30px",
            "sName": "requirementStatus",
            "title": doc.getDocOnline("label", "reqstatus")},
        {"data": "requirementCriticity",
            "visible": false,
            "sWidth": "30px",
            "sName": "requirementCriticity",
            "title": doc.getDocOnline("label", "reqcriticity")},
        {"data": "usrCreated",
            "visible": false,
            "sWidth": "30px",
            "sName": "usrCreated",
            "title": doc.getDocOnline("transversal", "UsrCreated")},
        {"data": "dateCreated",
            "visible": false,
            "like": true,
            "sWidth": "80px",
            "sName": "dateCreated",
            "title": doc.getDocOnline("transversal", "DateCreated"),
            "mRender": function (data, type, oObj) {
                return getDate(oObj["dateCreated"]);
            }},
        {"data": "usrModif",
            "visible": false,
            "sWidth": "30px",
            "sName": "usrModif",
            "title": doc.getDocOnline("transversal", "UsrModif")
        },
        {"data": "dateModif",
            "visible": false,
            "like": true,
            "sWidth": "80px",
            "sName": "dateModif",
            "title": doc.getDocOnline("transversal", "DateModif"),
            "mRender": function (data, type, oObj) {
                return getDate(oObj["dateModif"]);
            }
        }

    ];
    return aoColumns;
}

function filterOnLabel(element) {
    var newLabel = $(element).attr('data-labelid');
    var colIndex = $(element).parent().parent().get(0).cellIndex;
    $("#labelsTable").dataTable().fnFilter(newLabel, colIndex);
}

//function afterTableLoad() {
//    generateLabelTree();
//}

