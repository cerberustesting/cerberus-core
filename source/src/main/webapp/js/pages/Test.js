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

        var config = new TableConfigurationsServerSide("testTable", "ReadTest", "contentTable", aoColumnsFunc(), [1, 'asc']);
        var table = createDataTableWithPermissionsNew(config, renderOptionsForTest, "#testList", undefined, true);

        $('#testTable').on('draw.dt', function() {
            $(this).find('tbody tr').addClass('group');
            if (window.lucide) lucide.createIcons();
        });

        $('[data-toggle="popover"]').popover({
            'placement': 'auto',
            'container': 'body'}
        );
    });
});

function initPage() {
    var doc = new Doc();

    displayGlobalLabel(doc);
    displayPageLabel(doc);
    displayFooter(doc);
    displayInvariantList("testFolderIsActive", "TESTACTIVE", false);
}

function displayPageLabel(doc) {
    $("#pageTitle").html(doc.getDocLabel("test", "Test"));
    $("#title").html(doc.getDocLabel("test", "Test"));
    $("[name='addEntryField']").html(doc.getDocLabel("page_test", "btn_create"));
    $("[name='confirmationField']").html(doc.getDocLabel("page_test", "button_delete"));
    $("[name='editEntryField']").html(doc.getDocLabel("page_test", "btn_edit"));
    $("[name='testField']").html(doc.getDocOnline("test", "Test"));
    $("[name='activeField']").html(doc.getDocOnline("test", "isActive"));
    $("[name='descriptionField']").html(doc.getDocOnline("test", "description"));
}

function renderOptionsForTest(data) {
    var doc = new Doc();

    if (data["hasPermissions"]) {
        if ($("#createTestButton").length === 0) {
            var contentToAdd = "";
            contentToAdd += `
            <button id='createTestButton' type='button'
                class='bg-sky-400 hover:bg-sky-500 flex items-center space-x-1 px-3 py-1 rounded-lg h-10 w-auto'>
                <i data-lucide="plus" class="w-4 h-4"></i>
                <span>${doc.getDocLabel("page_test", "btn_create")}</span>
            </button>
            `;

            var $wrapper = $("#testTable_buttonWrapper");
            if ($wrapper.length) {
                $wrapper.append(contentToAdd);
            } else {
                console.warn("Wrapper #testTable_buttonWrapper introuvable, insertion avant length");
                $("#testTable_wrapper div#testTable_length").before("<div id='testTable_buttonWrapper' class='flex w-full gap-2'>" + contentToAdd + "</div>");
            }
            $('#testList #createTestButton').click(addEntryClick);
        }
    }
}

function deleteEntryClick(entry) {
    clearResponseMessageMainPage();
    var doc = new Doc();
    var messageComplete = doc.getDocLabel("page_test", "message_delete");
    messageComplete = messageComplete.replace("%ENTRY%", entry);
    crbConfirmDelete({
        title: doc.getDocLabel("page_test", "button_delete"),
        html: messageComplete,
        preConfirm: function() {
            return $.post("DeleteTest", {test: entry}, "json").then(function (data) {
                var messageType = getAlertType(data.messageType);
                if (messageType === "success") {
                    var oTable = $("#testTable").dataTable();
                    oTable.fnDraw(false);
                }
                showMessageMainPage(messageType, data.message, false);
                return data;
            }).fail(handleErrorAjaxAfterTimeout);
        }
    });
}

function addEntryClick() {
    clearResponseMessageMainPage();
    window.dispatchEvent(new CustomEvent('test-folder-modal-open', { detail: { mode: 'ADD' } }));
}

function editEntryClick(test) {
    clearResponseMessageMainPage();
    window.dispatchEvent(new CustomEvent('test-folder-modal-open', { detail: { mode: 'EDIT', test: test } }));
}

function aoColumnsFunc() {
    var doc = new Doc();

    var aoColumns = [
        {
            "data": null,
            "sWidth": "100px",
            "bSortable": false,
            "bSearchable": false,
            "title": doc.getDocOnline("page_global", "columnAction"),
            "mRender": function (data, type, obj, meta) {
                var hasPermissions = data["hasPermissions"];
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
                    list: `<i data-lucide="external-link" class="w-4 h-4"></i>`
                };

                let buttons = [];

                // Edit / View
                buttons.push(actionButton({
                    id: `editEntry_${row}`,
                    name: "editEntry",
                    title: hasPermissions ? doc.getDocLabel("page_test", "btn_edit") : doc.getDocLabel("page_test", "btn_edit"),
                    onClick: `editEntryClick('${escapeHtml(obj["test"])}')`,
                    icon: hasPermissions ? icons.edit : icons.view
                }));

                // Delete
                if (hasPermissions) {
                    buttons.push(actionButton({
                        id: `deleteEntry_${row}`,
                        name: "deleteEntry",
                        title: doc.getDocLabel("page_test", "button_delete"),
                        onClick: `deleteEntryClick('${escapeHtml(obj["test"])}')`,
                        icon: icons.delete,
                        extraClass: "group-hover:!text-red-500"
                    }));
                }

                // Test Cases link
                buttons.push(actionButton({
                    id: `tcLink_${row}`,
                    name: "testCaseLink",
                    title: doc.getDocLabel("page_test", "btn_tclist"),
                    href: `./TestCaseList.jsp?test=${encodeURIComponent(obj["test"])}`,
                    icon: icons.list,
                    extraClass: "group-hover:!text-blue-500"
                }));

                var html = `<div class="flex items-center gap-1">${buttons.join('')}</div>`;
                setTimeout(function() { if (window.lucide) lucide.createIcons(); }, 50);
                return html;
            }
        },
        {
            "data": "test",
            "sName": "test",
            "sWidth": "80px",
            "title": doc.getDocOnline("test", "Test")
        },
        {
            "data": "description",
            "sName": "description",
            "like": true,
            "sWidth": "100px",
            "title": doc.getDocOnline("test", "description")
        },
        {
            "data": "isActive",
            "sName": "isActive",
            "sWidth": "30px",
            "title": doc.getDocOnline("test", "isActive"),
            "className": "center",
            "mRender": function (data, type, obj) {
                if (data) {
                    return '<input type="checkbox" checked disabled />';
                } else {
                    return '<input type="checkbox" disabled />';
                }
            }
        },
        {
            "data": "dateCreated",
            "visible": false,
            "sName": "tes.dateCreated",
            "like": true,
            "title": doc.getDocOnline("transversal", "DateCreated"),
            "sWidth": "150px",
            "sDefaultContent": "",
            "mRender": function (data, type, oObj) {
                return getDate(oObj["dateCreated"]);
            }
        },
        {
            "data": "usrCreated",
            "visible": false,
            "sName": "tes.usrCreated",
            "title": doc.getDocOnline("transversal", "UsrCreated"),
            "sWidth": "100px",
            "sDefaultContent": ""
        },
        {
            "data": "dateModif",
            "visible": false,
            "like": true,
            "sName": "tes.dateModif",
            "title": doc.getDocOnline("transversal", "DateModif"),
            "sWidth": "150px",
            "sDefaultContent": "",
            "mRender": function (data, type, oObj) {
                return getDate(oObj["dateModif"]);
            }

        },
        {
            "data": "usrModif",
            "visible": false,
            "sName": "tes.usrModif",
            "title": doc.getDocOnline("transversal", "UsrModif"),
            "sWidth": "100px",
            "sDefaultContent": ""
        }
    ];
    return aoColumns;
}
