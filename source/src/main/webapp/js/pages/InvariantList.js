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
    });
});

function initPage() {
    displayPageLabel();

    //clear the modals fields when closed
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
        var configurationsPriv = new TableConfigurationsServerSide("invariantsPrivateTable", "ReadInvariant?access=PRIVATE", "contentTable", aoColumnsFuncPrivate(), [1, 'asc']);
        createDataTableWithPermissionsNew(configurationsPriv, renderOptionsForApplication2, "#invariantPrivateList", undefined, true);
    }
}

function displayPublicTable() {
    if ($.fn.dataTable.isDataTable('#invariantsTable')) {
        $('#invariantsTable').DataTable();
    } else {
        //configure and create the dataTable
        var configurations = new TableConfigurationsServerSide("invariantsTable", "ReadInvariant?access=PUBLIC", "contentTable", aoColumnsFuncPublic(), [1, 'asc']);
        createDataTableWithPermissionsNew(configurations, renderOptionsForApplication, "#invariantList", undefined, true);
    }
}

function displayPageLabel() {
    var doc = new Doc();

    $("#title").html(doc.getDocLabel("page_invariant", "allInvariants"));
    $("[name='editInvariantField']").html(doc.getDocLabel("page_invariant", "editinvariant_field"));
    $("[name='addInvariantField']").html(doc.getDocLabel("page_invariant", "addinvariant_field"));
    $("#invariantListLabel").html("<span class='glyphicon glyphicon-list'></span> " + doc.getDocLabel("page_invariant", "public_invariant"))
    $("#invariantPrivateListLabel").html("<span class='glyphicon glyphicon-list'></span> " + doc.getDocLabel("page_invariant", "private_invariant"))
    $("a[href='#public']").html(doc.getDocLabel("page_invariant", "public"));
    $("a[href='#private']").html(doc.getDocLabel("page_invariant", "private"));

    //displayHeaderLabel(doc);

    $("[name='systemField']").html(doc.getDocLabel("page_invariant", "system_field") + " (" + getSys() + ")");

    displayFooter(doc);
    displayGlobalLabel(doc);
}

function renderOptionsForApplication(data) {
    var doc = new Doc();

    if ($("#createInvariantButton").length === 0) {
        var contentToAdd = `
            <button id='createInvariantButton' type='button'
                class='bg-sky-400 hover:bg-sky-500 flex items-center space-x-1 px-3 py-1 rounded mr-2 h-10 w-auto'>
                <i class='glyphicon glyphicon-plus-sign'></i>
                <span>` + doc.getDocLabel("page_invariant", "button_create") + `</span>
            </button>
        `;

        // Cherche ton _buttonWrapper
        var $wrapper = $("#invariantsTable_buttonWrapper");

        if ($wrapper.length) {
            // Ajoute le bouton au **début** du wrapper
            $wrapper.prepend(contentToAdd);
        } else {
            // fallback si le wrapper n’existe pas encore
            console.warn("Wrapper #invariantsTable_buttonWrapper introuvable, insertion avant length");
            $("#invariantsTable_wrapper div#invariantsTable_length").before("<div id='invariantsTable_buttonWrapper'>" + contentToAdd + "</div>");
        }

        $("#createInvariantButton").off("click").on("click", function () {
            openModalInvariant(undefined, undefined, "ADD");
        });
    }
}

function renderOptionsForApplication2(data) {
    var doc = new Doc();
    if ($("#blankSpace").length === 0) {
        var contentToAdd = "<div class='marginBottom10' id='blankSpace'></div>";
        $("#invariantsPrivateTable_wrapper div#invariantsPrivateTable_length").before(contentToAdd);
    }
}

function removeEntryClick(param, value) {
    var doc = new Doc();
    showModalConfirmation(deleteEntryHandlerClick, undefined, doc.getDocLabel("page_invariant", "message_remove"), doc.getDocLabel("page_invariant", "message_remove"), param, value, "", "");
}

function deleteEntryHandlerClick() {
    var param = $('#confirmationModal #hiddenField1').prop("value");
    var value = $('#confirmationModal #hiddenField2').prop("value");
    var jqxhr = $.post("DeleteInvariant", {idName: param, value: value}, "json");
    $.when(jqxhr).then(function (data) {
        var messageType = getAlertType(data.messageType);
        if (messageType === "success") {
            //redraw the datatable
            var oTable = $("#invariantsTable").dataTable();
            oTable.fnDraw(false);
            var info = oTable.fnGetData().length;
            cleanCacheInvariant(param);

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

function aoColumnsFuncPublic(tableId) {
    var doc = new Doc();
    var aoColumns = [
        {
            "data": null,
            "bSortable": false,
            "bSearchable": false,
            "className": "w-[30px] text-center", // largeur fixée côté table
            "title": doc.getDocLabel("page_invariant", "button_col"),
            "render": function (data, type, obj, meta) {
                var newValue = escapeHtml(obj["value"]);
                var row = "row_" + meta.row;

                return `
            <div x-data="{ open: false, pos: {top: 0, left: 0}, timer: null, row: '${row}' }" class="inline-block">
                
                <!-- Bouton "…" -->
                <button @mouseenter="
                            clearTimeout(timer);
                            open = true;
                            const rect = $el.getBoundingClientRect();
                            pos = { 
                                top: rect.top + window.scrollY, 
                                left: rect.right + window.scrollX 
                            };
                        "
                        @mouseleave="timer = setTimeout(() => open = false, 200)"
                        :id="'invariant_action_' + row"
                        class="p-1 rounded hover:bg-gray-200 dark:hover:bg-gray-700">
                    <i data-lucide="ellipsis" class="w-4 h-4"></i>
                </button>

                <!-- Tooltip via teleport -->
                <template x-teleport="body">
                    <div x-show="open"
                         x-transition
                         @mouseenter="clearTimeout(timer); open=true"
                         @mouseleave="open=false"
                         x-init="$nextTick(() => { if (window.lucide) lucide.createIcons(); })"
                         class="absolute z-50 w-44 bg-slate-50 dark:bg-slate-900 border border-gray-200 dark:border-gray-700 dark:text-slate-50 text-slate-900 rounded-lg shadow-lg"
                         :style="'top:'+(pos.top)+'px; left:'+(pos.left - $el.offsetWidth)+'px;'">

                        <div class="px-3 py-2 flex items-center gap-2 hover:bg-gray-100 dark:hover:bg-gray-800 cursor-pointer"
                             :id="'invariant_action_edit_' + row"
                             onclick="openModalInvariant('${obj["idName"]}','${newValue}','EDIT')">
                            <i data-lucide="pencil" class="w-4 h-4"></i>
                            ${doc.getDocLabel("page_invariant", "button_edit")}
                        </div>

                        <div class="px-3 py-2 flex items-center gap-2 hover:bg-gray-100 dark:hover:bg-gray-800 cursor-pointer"
                             :id="'invariant_action_duplicate_' + row"
                             onclick="openModalInvariant('${obj["idName"]}','${newValue}','DUPLICATE')">
                            <i data-lucide="copy" class="w-4 h-4"></i>
                            ${doc.getDocLabel("page_invariant", "button_duplicate")}
                        </div>

                        <div class="px-3 py-2 flex items-center gap-2 hover:bg-gray-100 dark:hover:bg-gray-800 cursor-pointer"
                             :id="'invariant_action_remove_' + row"
                             onclick="removeEntryClick('${obj["idName"]}','${obj["value"]}')">
                            <i data-lucide="trash-2" class="w-4 h-4"></i>
                            ${doc.getDocLabel("page_invariant", "button_remove")}
                        </div>
                    </div>
                </template>
            </div>
        `;
            }
        },
        {
            "data": "idName",
            "sName": "idname",
            "sWidth": "60px",
            "title": doc.getDocLabel("page_invariant", "idname")
        },
        {
            "data": "value",
            "like": true,
            "sName": "value",
            "sWidth": "60px",
            "title": doc.getDocLabel("page_invariant", "value")
        },
        {
            "data": "sort",
            "visible": false,
            "sName": "sort",
            "sWidth": "60px",
            "title": doc.getDocLabel("page_invariant", "sort")
        },
        {
            "data": "description",
            "like": true,
            "sName": "description",
            "sWidth": "100px",
            "title": doc.getDocLabel("page_invariant", "description")
        },
        {
            "data": "veryShortDesc",
            "visible": false,
            "sName": "VeryShortDesc",
            "sWidth": "60px",
            "title": doc.getDocLabel("page_invariant", "veryShortDesc")
        },
        {"data": "gp1", "sName": "gp1", "sWidth": "60px", "title": doc.getDocLabel("page_invariant", "gp1")},
        {"data": "gp2", "sName": "gp2", "sWidth": "60px", "title": doc.getDocLabel("page_invariant", "gp2")},
        {"data": "gp3", "visible": false, "sName": "gp3", "sWidth": "60px", "title": doc.getDocLabel("page_invariant", "gp3")},
        {"data": "gp4", "visible": false, "sName": "gp4", "sWidth": "60px", "title": doc.getDocLabel("page_invariant", "gp4")},
        {"data": "gp5", "visible": false, "sName": "gp5", "sWidth": "60px", "title": doc.getDocLabel("page_invariant", "gp5")},
        {"data": "gp6", "visible": false, "sName": "gp6", "sWidth": "60px", "title": doc.getDocLabel("page_invariant", "gp6")},
        {"data": "gp7", "visible": false, "sName": "gp7", "sWidth": "60px", "title": doc.getDocLabel("page_invariant", "gp7")},
        {"data": "gp8", "visible": false, "sName": "gp8", "sWidth": "60px", "title": doc.getDocLabel("page_invariant", "gp8")},
        {"data": "gp9", "visible": false, "sName": "gp9", "sWidth": "60px", "title": doc.getDocLabel("page_invariant", "gp9")}
    ];
    return aoColumns;
}

function aoColumnsFuncPrivate(tableId) {
    var doc = new Doc();
    var aoColumns = [
        {
            "data": null,
            "bSortable": false,
            "bSearchable": false,
            "sWidth": "50px",
            "title": doc.getDocLabel("page_invariant", "button_col"),
            "mRender": function (data, type, obj) {
                var viewInvariant = '<button id="editInvariant" onclick="openModalInvariant(\'' + obj["idName"] + '\',\'' + obj["value"] + '\', \'EDIT\');"\n\
                                    class="btn btn-default btn-xs margin-right5" \n\
                                    name="viewInvariant" title="' + doc.getDocLabel("page_invariant", "button_edit") + '" type="button">\n\
                                    <span class="glyphicon glyphicon-eye-open"></span></button>';

                return '<div class="center btn-group width150">' + viewInvariant + '</div>';

            }
        },
        {
            "data": "idName",
            "sName": "idname",
            "sWidth": "60px",
            "title": doc.getDocLabel("page_invariant", "idname")
        },
        {
            "data": "value",
            "sName": "value",
            "sWidth": "60px",
            "title": doc.getDocLabel("page_invariant", "value")
        },
        {
            "data": "sort",
            "visible": false,
            "sName": "sort",
            "sWidth": "60px",
            "title": doc.getDocLabel("page_invariant", "sort")
        },
        {
            "data": "description",
            "sName": "description",
            "sWidth": "100px",
            "title": doc.getDocLabel("page_invariant", "description")
        },
        {
            "data": "veryShortDesc",
            "visible": false,
            "sName": "VeryShortDesc",
            "sWidth": "60px",
            "title": doc.getDocLabel("page_invariant", "veryShortDesc")
        },
        {"data": "gp1", "sName": "gp1", "sWidth": "60px", "title": doc.getDocLabel("page_invariant", "gp1")},
        {"data": "gp2", "sName": "gp2", "sWidth": "60px", "title": doc.getDocLabel("page_invariant", "gp2")},
        {"data": "gp3", "visible": false, "sName": "gp3", "sWidth": "60px", "title": doc.getDocLabel("page_invariant", "gp3")},
        {"data": "gp4", "visible": false, "sName": "gp4", "sWidth": "60px", "title": doc.getDocLabel("page_invariant", "gp4")},
        {"data": "gp5", "visible": false, "sName": "gp5", "sWidth": "60px", "title": doc.getDocLabel("page_invariant", "gp5")},
        {"data": "gp6", "visible": false, "sName": "gp6", "sWidth": "60px", "title": doc.getDocLabel("page_invariant", "gp6")},
        {"data": "gp7", "visible": false, "sName": "gp7", "sWidth": "60px", "title": doc.getDocLabel("page_invariant", "gp7")},
        {"data": "gp8", "visible": false, "sName": "gp8", "sWidth": "60px", "title": doc.getDocLabel("page_invariant", "gp8")},
        {"data": "gp9", "visible": false, "sName": "gp9", "sWidth": "60px", "title": doc.getDocLabel("page_invariant", "gp9")}
    ];
    return aoColumns;
}
