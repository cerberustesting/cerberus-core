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

//    $('#editLabelModal #editLabelModalForm #type').on('change', showHideRequirementPanelEdit);
//
//    $('#addLabelModal #addLabelModalForm #type').on('change', showHideRequirementPanelAdd);
//
    tinymce.init({
        selector: ".wysiwyg",
        menubar: true,
        statusbar: false,
        toolbar: true,
        resize: true,
        height: 400,
        skin: 'oxide-dark'
    });

    //configure and create the dataTable
    var configurations = new TableConfigurationsServerSide("labelsTable", "ReadLabel?q=1" + getUser().defaultSystemsQuery, "contentTable", aoColumnsFunc("labelsTable"), [2, 'asc']);
    createDataTableWithPermissionsNew(configurations, renderOptionsForLabel, "#labelList", undefined, true);

    $('#labelsTable').on('draw.dt', function() {
        $(this).find('tbody tr').addClass('group');
        if (window.lucide) lucide.createIcons();
    });
}

function displayPageLabel() {
    var doc = new Doc();

   // displayHeaderLabel(doc);
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

    // Invariant lists are now loaded by labelDropdown Alpine components in the HTML

    refreshParentLabelCombo('', "editLabelModalForm");
    refreshParentLabelCombo('', "addLabelModalForm");
    $("#edit_type").change(function () {
        refreshParentLabelCombo($("#edit_type").val(), "editLabelModalForm");
        showHideRequirementPanelEdit();
    });
    $("#add_type").change(function () {
        refreshParentLabelCombo($("#add_type").val(), "addLabelModalForm");
        showHideRequirementPanelAdd();
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

    if (data["hasPermissions"] && $("#createLabelButton").length === 0) {
        var contentToAdd = `
            <button id='createLabelButton' type='button'
                class='bg-sky-400 hover:bg-sky-500 flex items-center space-x-1 px-3 py-1 rounded-lg h-10 w-auto'>
                <i data-lucide="plus" class="w-4 h-4"></i>
                <span>${doc.getDocLabel("page_label", "btn_create")}</span>
            </button>
        `;

        // Cherche ton _buttonWrapper
        var $wrapper = $("#labelsTable_buttonWrapper");

        if ($wrapper.length) {
            $wrapper.prepend(contentToAdd);
            if (window.lucide) lucide.createIcons();
        } else {
            $("#labelsTable_wrapper div#labelsTable_length").before("<div id='labelsTable_buttonWrapper' class='flex w-full gap-2'>" + contentToAdd + "</div>");
            if (window.lucide) lucide.createIcons();
        }

        $('#createLabelButton').off("click").click(addEntryClick);
    }
}

function changeLabelParent(modal) {
    var par = $('#' + modal + ' #parentLabel').val();
    var $clearBtn = $('#' + modal + ' .crb-clear-parent');
    if (!isEmpty(par) && par !== 0) {
        $clearBtn.show();
        $clearBtn.off('click').on('click', function() {
            $('#' + modal + ' #parentLabel').val(null).trigger('change');
        });
    } else {
        $clearBtn.hide();
    }
    if (window.lucide) lucide.createIcons();
}

function emptyService() {
    $('#addLabelModal #parentLabel').val(null).trigger('change');
    $('#editLabelModal #parentLabel').val(null).trigger('change');
}


function showHideRequirementPanelEdit() {
    refreshParentLabelCombo($('#edit_type').val(), "editLabelModalForm");
    if ($('#edit_type').val() === "REQUIREMENT") {
        $("#editLabelModal #panelReq").show();
    } else {
        $("#editLabelModal #panelReq").hide();
        $('#edit_reqtype').val('');
        $('#edit_reqstatus').val('');
        $('#edit_reqcriticity').val('');
        window.dispatchEvent(new CustomEvent('edit-label-reqtype-preselect', { detail: '' }));
        window.dispatchEvent(new CustomEvent('edit-label-reqstatus-preselect', { detail: '' }));
        window.dispatchEvent(new CustomEvent('edit-label-reqcriticity-preselect', { detail: '' }));
    }
}

function showHideRequirementPanelAdd() {
    refreshParentLabelCombo($('#add_type').val(), "addLabelModalForm");
    if ($('#add_type').val() === "REQUIREMENT") {
        $("#addLabelModal #panelReq").show();
    } else {
        $("#addLabelModal #panelReq").hide();
        $('#add_reqtype').val('');
        $('#add_reqstatus').val('');
        $('#add_reqcriticity').val('');
    }
}

function deleteEntryClick(id, label) {
    clearResponseMessageMainPage();
    var doc = new Doc();
    var messageComplete = doc.getDocLabel("page_global", "message_delete");
    messageComplete = messageComplete.replace("%ENTRY%", id + " - " + label);
    messageComplete = messageComplete.replace("%TABLE%", " label ");
    crbConfirmDelete({
        title: doc.getDocLabel("page_label", "btn_delete"),
        html: messageComplete,
        preConfirm: function() {
            return $.post("DeleteLabel", {id: id}, "json").then(function (data) {
                var messageType = getAlertType(data.messageType);
                if (messageType === "success") {
                    var oTable = $("#labelsTable").dataTable();
                    oTable.fnDraw(false);
                    generateLabelTree();
                }
                showMessageMainPage(messageType, data.message, false);
                return data;
            }).fail(handleErrorAjaxAfterTimeout);
        }
    });
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
            window.dispatchEvent(new CustomEvent('addlabel-modal-close'));
        } else {
            showMessage(data, $('#addLabelModal'));
        }
    }).fail(handleErrorAjaxAfterTimeout);
}

function addEntryModalCloseHandler() {
// reset form values
    var form = $('#addLabelModal #addLabelModalForm')[0];
    if (form) form.reset();
    // remove all errors on the form fields
    $('#addLabelModal').find('div.has-error').removeClass("has-error");
    // clear the response messages of the modal
    clearResponseMessage($('#addLabelModal'));
}

function addEntryClick(type) {
    clearResponseMessageMainPage();
    // Pre-select system via event for crbDropdown
    window.dispatchEvent(new CustomEvent('add-label-system-preselect', { detail: getUser().defaultSystem }));
    window.dispatchEvent(new CustomEvent('addlabel-modal-open'));
    //ColorPicker
    $("[name='colorDiv']").colorpicker();
    $("[name='colorDiv']").colorpicker('setValue', '#000000');
    if (type !== undefined) {
        window.dispatchEvent(new CustomEvent('add-label-type-preselect', { detail: type }));
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
                window.dispatchEvent(new CustomEvent('editlabel-modal-close'));
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
    var form = $('#editLabelModal #editLabelModalForm')[0];
    if (form) form.reset();
    // remove all errors on the form fields
    $('#editLabelModal').find('div.has-error').removeClass("has-error");
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
        formEdit.find("#description").prop("value", obj["description"]);
        formEdit.find("#longdesc").prop("value", obj["longDesc"]);
        // Use preselect events for crbDropdown fields
        window.dispatchEvent(new CustomEvent('edit-label-type-preselect', { detail: obj["type"] }));
        window.dispatchEvent(new CustomEvent('edit-label-system-preselect', { detail: obj["system"] }));
        window.dispatchEvent(new CustomEvent('edit-label-reqtype-preselect', { detail: obj["requirementType"] }));
        window.dispatchEvent(new CustomEvent('edit-label-reqstatus-preselect', { detail: obj["requirementStatus"] }));
        window.dispatchEvent(new CustomEvent('edit-label-reqcriticity-preselect', { detail: obj["requirementCriticity"] }));
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
        window.dispatchEvent(new CustomEvent('editlabel-modal-open'));
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
            "sWidth": "120px",
            "mRender": function (data, type, obj, meta) {
                var hasPermissions = ($("#" + tableId).attr("hasPermissions") === "true");
                var row = "row_" + (meta ? meta.row : 0);

                const baseBtnClass = "inline-flex aspect-square h-8 w-8 items-center justify-center rounded-md transition-all duration-200 " +
                    "text-slate-500 hover:bg-slate-200 dark:hover:bg-slate-800 " +
                    "opacity-20 group-hover:opacity-100 [&_svg]:size-4";

                function actionButton({ id, name, title, onClick, icon, href, extraClass = "" }) {
                    if (href) {
                        return `<a id="${id}" name="${name}" title="${title}" href="${href}" class="${baseBtnClass} ${extraClass}">${icon}</a>`;
                    }
                    return `<button id="${id}" name="${name}" type="button" class="${baseBtnClass} ${extraClass}" title="${title}" onclick="${onClick}">${icon}</button>`;
                }

                const icons = {
                    edit: `<i data-lucide="pencil" class="w-4 h-4"></i>`,
                    view: `<i data-lucide="eye" class="w-4 h-4"></i>`,
                    delete: `<i data-lucide="trash-2" class="w-4 h-4"></i>`,
                    list: `<i data-lucide="list" class="w-4 h-4"></i>`
                };

                let buttons = [];

                // Edit / View
                buttons.push(actionButton({
                    id: `editLabel_${row}`,
                    name: "editLabel",
                    title: hasPermissions ? doc.getDocLabel("page_label", "btn_edit") : doc.getDocLabel("page_label", "btn_view"),
                    onClick: `editEntryClick('${obj["id"]}', '${obj["system"]}')`,
                    icon: hasPermissions ? icons.edit : icons.view
                }));

                // Delete
                if (hasPermissions) {
                    buttons.push(actionButton({
                        id: `deleteLabel_${row}`,
                        name: "deleteLabel",
                        title: doc.getDocLabel("page_label", "btn_delete"),
                        onClick: `deleteEntryClick('${obj["id"]}','${obj["label"]}')`,
                        icon: icons.delete,
                        extraClass: "group-hover:!text-red-500"
                    }));
                }

                // Test Cases link
                buttons.push(actionButton({
                    id: `tcLabel_${row}`,
                    name: "tcLabel",
                    title: doc.getDocLabel("page_label", "btn_tclist"),
                    href: `./TestCaseList.jsp?label=${obj["label"]}`,
                    icon: icons.list,
                    extraClass: "group-hover:!text-blue-500"
                }));

                var html = `<div class="flex items-center gap-1">${buttons.join('')}</div>`;
                setTimeout(function() { if (window.lucide) lucide.createIcons(); }, 50);
                return html;
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
                return '<span class="label label-primary" style="background-color:' + data.color + ';color:' + data.fontColor + '">' + data.label + '</span> ';
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

