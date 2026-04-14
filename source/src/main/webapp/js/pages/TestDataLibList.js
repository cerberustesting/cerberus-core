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


    });
});

function initPage() {

    var doc = new Doc();
    displayPageLabel();

    window.addEventListener('testdatalib-modal-close', function() { buttonCloseHandler({data: {extra: "#editTestLibData"}}); });

    $('#testCaseListModal').on('hidden.bs.modal', getTestCasesUsingModalCloseHandler);


    // Load Application Combo (used on service modal).
    displayApplicationList("application", "", "", "");

    // In itialise Modal Datalib.
    initModalDataLib();

    /*
     * Handles the change of the type when adding a new test data lib entry
     */

//    $("#editTestDataLibModal #service").change(function () {
//        activateSOAPServiceFields("#editTestDataLibModal", $(this).val());
//    });

    var configurations = new TableConfigurationsServerSide("listOfTestDataLib", "ReadTestDataLib", "contentTable", aoColumnsFuncTestDataLib("listOfTestDataLib"), [2, 'asc']);

    //creates the main table and draws the management buttons if the user has the permissions
    var table = createDataTableWithPermissionsNew(configurations, renderOptionsForTestDataLib, "#testDataLibList", undefined, true);
    refreshPopoverDocumentation("testDataLibList");

    $('#listOfTestDataLib').on('draw.dt', function() {
        $(this).find('tbody tr').addClass('group');
        if (window.lucide) lucide.createIcons();
    });

}


function displayPageLabel() {
    var doc = new Doc();

    //displayHeaderLabel(doc);
    displayGlobalLabel(doc);
    displayFooter(doc);
    $("#pageTitle").html(doc.getDocLabel("page_testdatalib", "page_title"));
    $("#title").html(doc.getDocOnline("page_testdatalib", "title"));
    displayFooter(doc);
}

function renderOptionsForTestDataLib(data) {
    var doc = new Doc();

    if ($("#createLibButton").length === 0) {
        var disabledCreate = data["hasPermissions"] ? "" : "disabled";

        var contentToAdd = "";

        // Bouton Create
        contentToAdd += `
            <button id='createLibButton' type='button'
                class='bg-sky-400 hover:bg-sky-500 flex items-center space-x-1 px-3 py-1 rounded-lg h-10 w-auto'
                ${disabledCreate}>
                <i data-lucide="plus" class="w-4 h-4"></i>
                <span>${doc.getDocLabel("page_testdatalib", "btn_create")}</span>
            </button>
        `;

        // Bouton Bulk Rename
        if (data["hasPermissions"]) {
            contentToAdd += `
                <button id='bulkRenameButton' type='button'
                    class='flex items-center gap-2 px-3 py-2 rounded-lg border border-gray-300 dark:border-gray-600 hover:border-sky-500 h-10'>
                    <i data-lucide="copy" class="w-4 h-4"></i>
                    <span>${doc.getDocLabel("page_testdatalib", "btn_bulkrename")}</span>
                </button>
            `;
        }

        // Insert into buttonWrapper
        var $wrapper = $("#listOfTestDataLib_buttonWrapper");
        if ($wrapper.length) {
            $wrapper.append(contentToAdd);
            if (window.lucide) lucide.createIcons();
        } else {
            $("#listOfTestDataLib_wrapper #listOfTestDataLib_length").before("<div id='listOfTestDataLib_buttonWrapper' class='flex w-full gap-2'>" + contentToAdd + "</div>");
            if (window.lucide) lucide.createIcons();
        }

        if (data["hasPermissions"]) {
            $("#createLibButton").off("click").on("click", function () {
                openModalDataLib(null, null, "ADD");
            });

            $("#bulkRenameButton").off("click").on("click", function () {
                openModalDataLibBulk();
            });
        }
    }
}

function openModalDataLibBulk() {

    clearResponseMessageMainPage();

    // from: initModalDataLib
    $('[data-toggle="popover"]').popover({
        'placement': 'auto',
        'z-index': 1060,
        'container': 'body'})

    var doc = new Doc();

    if ($('#bulkRenameDataLibModal').data("initLabel") === undefined) {
        $("[name='lbl_currentname']").html(doc.getDocOnline("testdatalib", "currentname"));
        $("[name='lbl_newname']").html(doc.getDocOnline("testdatalib", "newname"));
        $("#bulkRenameValidate").text(doc.getDocLabel("page_global", "btn_bulkrename"));
        $('#bulkRenameDataLibModal').data("initLabel", true);
    }

    $("#bulkRenameValidate").off("click");
    $('#bulkRenameValidate').click(function () {
        confirmDataLibBulkModalHandler();
    });

    window.dispatchEvent(new CustomEvent('bulkrename-modal-open'));
    setTimeout(function() { if (window.lucide) lucide.createIcons(); }, 100);
}

/**
 * Data Library Bulk Rename modal fields validation, must be: -defined, -not empty and without white space.
 * @returns {true or false}
 */
function isBlankOrContainsWhiteSpace(str) {
    // blank test: is null or undefined
    var regex1 = /^\s*$/;
    // contains white space test
    var regex2 = /^(.*\s+.*)+$/;
    if (!str || regex1.test(str)) {
        return true;
    } else if (regex2.test(str)) {
        return true;
    } else {
        return false;
    }
}

function confirmDataLibBulkModalHandler() {
    var bool = false;
    // get the input values
    var old_name = $('#dl_currentname').val();
    var new_name = $('#dl_newname').val();
    // get
    var formEdit = $('#bulkRenameDataLibModal');
    var old_nameElement = formEdit.find("#dl_currentname");
    var new_nameElement = formEdit.find("#dl_newname");
    // reset the error formatting
    if (old_nameElement.parents("div.form-group").hasClass("has-error")) {
        old_nameElement.parents("div.form-group").removeClass("has-error");
    }
    if (new_nameElement.parents("div.form-group").hasClass("has-error")) {
        new_nameElement.parents("div.form-group").removeClass("has-error");
    }
    // client-side validation
    if (!isBlankOrContainsWhiteSpace(old_name)) {
        if (!isBlankOrContainsWhiteSpace(new_name)) {
            var myServlet = "BulkRenameDataLib";
            bool = true;
        } else {
            var doc = new Doc();
            var localMessage = new Message("danger", doc.getDocLabel("page_testdatalib", "wrong_name_message"));
            new_nameElement.parents("div.form-group").addClass("has-error");
            showMessage(localMessage, $('#bulkRenameDataLibModal'));
        }
    } else {
        var doc = new Doc();
        var localMessage = new Message("danger", doc.getDocLabel("page_testdatalib", "wrong_name_message"));
        old_nameElement.parents("div.form-group").addClass("has-error");
        showMessage(localMessage, $('#bulkRenameDataLibModal'));
    }
    // bulk rename operation
    if (bool) {
        $.ajax({
            async: true,
            url: myServlet,
            method: "GET",
            data: 'oldname=' + old_name + '&newname=' + new_name,
            success: function (data) {
                hideLoaderInModal('#bulkRenameDataLibModal');
                if (getAlertType(data.messageType) === "success") {
                    window.dispatchEvent(new CustomEvent('bulkrename-modal-close'));
                    var oTable = $("#listOfTestDataLib").dataTable();
                    oTable.fnDraw(false);
                    var doc = new Doc();
                    showMessage(data);
                } else {
                    showMessage(data, $('#bulkRenameDataLibModal'));
                }
            },
            error: showUnexpectedError
        });
    }
}

function deleteTestDataLibHandlerClick() {
    // Legacy — kept for backward compat but no longer called directly
}

async function deleteTestDataLibClick(testDataLibID, name, system, environment, country, type) {
    var doc = new Doc();

    var systemLabel = system === '' ? doc.getDocLabel("page_global", "lbl_all") : system;
    var environmentLabel = environment === '' ? doc.getDocLabel("page_global", "lbl_all") : environment;
    var countryLabel = country === '' ? doc.getDocLabel("page_global", "lbl_all") : country;

    var messageComplete = doc.getDocLabel("page_testdatalib", "message_delete")
        .replace("%ENTRY%", name)
        .replace("%ID%", testDataLibID)
        .replace("%SYSTEM%", systemLabel)
        .replace("%ENVIRONMENT%", environmentLabel)
        .replace("%COUNTRY%", countryLabel);

    const result = await crbConfirmDelete({
        title: doc.getDocLabel("page_testdatalib_delete", "title"),
        html: messageComplete,
        confirmText: doc.getDocLabel("page_global", "btn_delete") || 'Delete',
        cancelText: doc.getDocLabel("page_global", "buttonClose") || 'Cancel',
        preConfirm: async () => {
            try {
                const resp = await fetch("DeleteTestDataLib", {
                    method: "POST",
                    headers: { "Content-Type": "application/x-www-form-urlencoded" },
                    body: "testdatalibid=" + testDataLibID
                });
                const data = await resp.json();
                if (getAlertType(data.messageType) !== "success") {
                    Swal.showValidationMessage(data.message || "Delete failed");
                    return null;
                }
                return data;
            } catch (e) {
                Swal.showValidationMessage("Unexpected error");
                return null;
            }
        }
    });

    if (result.isConfirmed && result.value) {
        var oTable = $("#listOfTestDataLib").dataTable();
        oTable.fnDraw(false);
        if (oTable.fnGetData().length === 1) {
            oTable.fnPageChange('previous');
        }
        notifyInPage("success", result.value.message || "Data Library deleted successfully");
    }
}

/**
 * Function that loads all test cases that are associated with the selected entry
 * @param {type} testDataLibID testdatalib id
 * @param {type} name entry name
 * @param {type} country where the entry is available
 */


function getTestCasesUsing(testDataLibID, name, country) {
    clearResponseMessageMainPage();
    showLoaderInModal('#testCaseListModal');
    var jqxhr = $.getJSON("ReadTestDataLib", "testdatalibid=" + testDataLibID + "&name=" + name + "&country=" + country);

    var doc = new Doc();

    $.when(jqxhr).then(function (result) {

        $('#testCaseListModal #totalTestCases').text(doc.getDocLabel("page_testdatalib_m_gettestcases", "nrTests") + " " + result["TestCasesList"].length);
        var htmlContent = "";

        $.each(result["TestCasesList"], function (idx, obj) {

            var item = '<b><a class="list-group-item ListItem" data-remote="true" href="#sub_cat' + idx + '" id="cat' + idx + '" data-toggle="collapse" \n\
            data-parent="#sub_cat' + idx + '"><span class="pull-left">' + obj[0] + '</span>\n\
                                        <span style="margin-left: 25px;" class="pull-right">' + doc.getDocLabel("page_testdatalib_m_gettestcases", "nrTestCases") + obj[2] + '</span>\n\
                                        <span class="menu-ico-collapse"><i class="fa fa-chevron-down"></i></span>\n\
                                    </a></b>';
            htmlContent += item;
            htmlContent += '<div class="collapse list-group-submenu" id="sub_cat' + idx + '">';


            $.each(obj[3], function (idx2, obj2) {
                var hrefTest = 'TestCaseScript.jsp?test=' + obj[0] + '&testcase=' + obj2.TestCaseNumber;
                htmlContent += '<span class="list-group-item sub-item ListItem" data-parent="#sub_cat' + idx + '" style="padding-left: 78px;height: 50px;">';
                htmlContent += '<span class="pull-left"><a href="' + hrefTest + '" target="_blank">' + obj2.TestCaseNumber + '- ' + obj2.TestCaseDescription + '</a></span>';
                htmlContent += '<span class="pull-right">' + doc.getDocLabel("page_testdatalib_m_gettestcases", "nrProperties") + " " + obj2.NrProperties + '</span><br/>';
                htmlContent += '<span class="pull-left"> ' + doc.getDocLabel("testcase", "Creator") + ": " + obj2.Creator + ' | '
                        + doc.getDocLabel("testcase", "IsActive") + ": " + obj2.Active + ' | ' + doc.getDocLabel("testcase", "Status") + ": " + obj2.Status + ' | ' +
                        doc.getDocLabel("invariant", "TESTCASE_TYPE") + ": " + obj2.Group + ' | ' + doc.getDocLabel("application", "Application") + ": " + obj2.Application + '</span>';
                htmlContent += '</span>';
            });

            htmlContent += '</div>';

        });
        if (htmlContent !== '') {
            $('#testCaseListModal #testCaseListGroup').append(htmlContent);
        }
        hideLoaderInModal('#testCaseListModal');
        window.dispatchEvent(new CustomEvent('testcaselist-modal-open'));

    }).fail(handleErrorAjaxAfterTimeout);

}


function buttonCloseHandler(event) {
    var modalID = event.data.extra;
    $(modalID).find("#name").attr("disabled", false);
    // reset form values
    $(modalID)[0].reset();
    // remove all errors on the form fields
    $(this).find('div.has-error').removeClass("has-error");
    // clear the response messages of the modal
    clearResponseMessage($(modalID));
}



/**
 * Handler that cleans the test case list modal when it is closed
 */
function getTestCasesUsingModalCloseHandler() {
    //we need to clear the item-groups that were inserted
    $('#testCaseListModal #testCaseListGroup a[id*="cat"]').remove();
    $('#testCaseListModal #testCaseListGroup div[id*="sub_cat"]').remove();
}

function aoColumnsFuncTestDataLib(tableId) {
    var doc = new Doc();

    var aoColumns = [
        {
            "sName": "tdl.TestDataLibID",
            "data": "testDataLibID",
            "bSortable": false,
            "bSearchable": false,
            "sWidth": "150px",
            "title": doc.getDocLabel("testdatalib", "actions"),
            "mRender": function (data, type, obj, meta) {
                var hasPermissions = ($("#" + tableId).attr("hasPermissions") === "true");
                var row = "row_" + meta.row;

                const baseBtnClass = "inline-flex aspect-square h-8 w-8 items-center justify-center rounded-md transition-all duration-200 " +
                    "text-slate-500 hover:bg-slate-200 dark:hover:bg-slate-800 " +
                    "opacity-20 group-hover:opacity-100 [&_svg]:size-4";

                function actionButton({ id, name, title, onClick, icon, extraClass = "", disabled = false }) {
                    const disabledClass = disabled ? "opacity-30 cursor-not-allowed" : "";
                    return `
                        <button id="${id}" name="${name}" type="button"
                            class="${baseBtnClass} ${extraClass} ${disabledClass}"
                            title="${title}"
                            ${disabled ? "disabled" : `onclick="${onClick}"`}>
                            ${icon}
                        </button>`;
                }

                const icons = {
                    edit: `<i data-lucide="${hasPermissions ? 'pencil' : 'eye'}" class="w-4 h-4"></i>`,
                    duplicate: `<i data-lucide="copy" class="w-4 h-4"></i>`,
                    delete: `<i data-lucide="trash-2" class="w-4 h-4"></i>`,
                    list: `<i data-lucide="list" class="w-4 h-4"></i>`
                };

                let buttons = [];

                // Edit / View
                buttons.push(actionButton({
                    id: `editTestDataLib_${row}`,
                    name: "editTestDataLib",
                    title: doc.getDocLabel("page_testdatalib", "tooltip_editentry"),
                    onClick: `openModalDataLib('${obj.testDataLibID}', '${obj.name}', 'EDIT', 'TestDataLibList', null)`,
                    icon: icons.edit
                }));

                // Duplicate
                if (hasPermissions) {
                    buttons.push(actionButton({
                        id: `duplicateTestDataLib_${row}`,
                        name: "duplicateTestDataLib",
                        title: doc.getDocLabel("page_testdatalib", "tooltip_duplicateEntry"),
                        onClick: `openModalDataLib('${obj.testDataLibID}', '${obj.name}', 'DUPLICATE', 'TestDataLibList', null)`,
                        icon: icons.duplicate
                    }));
                }

                // Delete
                if (hasPermissions) {
                    buttons.push(actionButton({
                        id: `deleteTestDataLib_${row}`,
                        name: "deleteTestDataLib",
                        title: doc.getDocLabel("page_testdatalib", "tooltip_delete"),
                        onClick: `deleteTestDataLibClick(${obj.testDataLibID}, '${obj.name}', '${obj.system}', '${obj.environment}', '${obj.country}', '${obj.type}')`,
                        icon: icons.delete,
                        extraClass: "group-hover:!text-red-500"
                    }));
                }

                // View Test Cases
                buttons.push(actionButton({
                    id: `viewTestCases_${row}`,
                    name: "getTestCasesUsing",
                    title: doc.getDocLabel("page_testdatalib", "tooltip_gettestcases"),
                    onClick: `getTestCasesUsing(${data}, '${obj.name}', '${obj.country}')`,
                    icon: icons.list
                }));

                return '<div class="flex items-center gap-0.5">' + buttons.join('') + '</div>';
            }
        },
        {
            "sName": "tdl.TestDataLibID",
            "like": true,
            "data": "testDataLibID",
            "sWidth": "50px",
            "title": doc.getDocOnline("testdatalib", "testdatalibid")
        },
        {
            "sName": "tdl.Name",
            "data": "name",
            "sWidth": "200px",
            "title": doc.getDocOnline("testdatalib", "name")
        },
        {
            "sName": "tdl.System",
            "data": "system",
            "sWidth": "70px",
            "title": doc.getDocOnline("testdatalib", "system")
        },
        {
            "sName": "tdl.Environment",
            "visible": false,
            "data": "environment",
            "sWidth": "70px",
            "title": doc.getDocOnline("testdatalib", "environment")
        },
        {
            "sName": "tdl.Country",
            "visible": false,
            "data": "country",
            "sWidth": "70px",
            "title": doc.getDocOnline("testdatalib", "country")
        },
        {
            "sName": "tdl.Group",
            "visible": false,
            "like": true,
            "data": "group",
            "sWidth": "100px",
            "title": doc.getDocOnline("testdatalib", "group")
        },
        {
            "sName": "tdl.Description",
            "like": true,
            "data": "description",
            "sWidth": "150px",
            "title": doc.getDocOnline("testdatalib", "description")
        },
        {
            "sName": "tdl.Type",
            "data": "type",
            "sWidth": "70px",
            "title": doc.getDocOnline("testdatalib", "type"),
            "mRender": function (data, type, obj) {
                if (obj.type === "SQL") {
                    return '<i class="fa fa-table" style="font-size:20px"></i> ' + obj.type;
                } else if (obj.type === "FILE") {
                    return '<i class="fa fa-file" style="font-size:20px"></i> ' + obj.type;
                } else if (obj.type === "INTERNAL") {
                    return '<img src="images/Logo-cerberus_250.png" width="20px" height="20px"></img> ' + obj.type;
                } else  {
                    return '<i class="fa fa-cloud-upload" style="font-size:20px"></i> ' + obj.type;
                }
            }
        },
        {
            "sName": "tdd.value",
            "like": true,
            "data": "subDataValue",
            "sWidth": "150px",
            "bSortable": false,
            "title": doc.getDocOnline("testdatalibdata", "value")
        },
        {
            "sName": "tdl.Database",
            "visible": false,
            "data": "database",
            "sWidth": "70px",
            "title": doc.getDocOnline("testdatalib", "database")
        },
        {
            "data": "script",
            "sName": "tdl.Script",
            "visible": false,
            "sWidth": "450px",
            "title": doc.getDocLabel("testdatalib", "script"),
            "mRender": function (data, type, obj) {
                return $("<div></div>").append($("<pre name='scriptField' style='height:20px; overflow:hidden; text-overflow:clip; border: 0px; padding:0; margin:0'></pre>").text(obj['script'])).html();
            }
        },
        {
            "sName": "tdl.DatabaseUrl",
            "visible": false,
            "data": "databaseUrl",
            "sWidth": "70px",
            "title": doc.getDocOnline("testdatalib", "databaseUrl")
        },
        {
            "sName": "tdl.Service",
            "visible": false,
            "data": "service",
            "sWidth": "150px",
            "title": doc.getDocOnline("testdatalib", "service")
        },
        {
            "sName": "tdl.ServicePath",
            "visible": false,
            "like": true,
            "data": "servicePath",
            "sWidth": "150px",
            "title": doc.getDocOnline("testdatalib", "servicepath")
        },
        {
            "sName": "tdl.method",
            "visible": false,
            "like": true,
            "data": "method",
            "sWidth": "150px",
            "title": doc.getDocOnline("testdatalib", "method")
        },
        {
            "data": "envelope",
            "sName": "tdl.envelope",
            "visible": false,
            "like": true,
            "title": doc.getDocLabel("testdatalib", "envelope"),
            "sWidth": "350px",
            "mRender": function (data, type, obj) {
                return $("<div></div>").append($("<pre name='envelopeField' style='height:20px; overflow:hidden; text-overflow:clip; border: 0px; padding:0; margin:0'></pre>").text(obj['envelope'])).html();
            }
        },
        {
            "sName": "tdl.DatabaseCsv",
            "visible": false,
            "data": "databaseCsv",
            "sWidth": "150px",
            "title": doc.getDocOnline("testdatalib", "databaseCsv")
        },
        {
            "sName": "tdl.csvUrl",
            "visible": false,
            "like": true,
            "data": "csvUrl",
            "sWidth": "150px",
            "title": doc.getDocOnline("testdatalib", "csvUrl")
        },
        {
            "sName": "tdl.separator",
            "visible": false,
            "data": "separator",
            "sWidth": "150px",
            "title": doc.getDocOnline("testdatalib", "separator")
        },
        {
            "sName": "tdl.Created",
            "visible": false,
            "like": true,
            "data": "created",
            "sWidth": "150px",
            "title": doc.getDocOnline("transversal", "DateCreated"),
            "mRender": function (data, type, oObj) {
                return getDate(oObj["created"]);
            }
        },
        {
            "sName": "tdl.Creator",
            "visible": false,
            "data": "creator",
            "sWidth": "70px",
            "title": doc.getDocOnline("testdatalib", "creator")
        },
        {
            "sName": "tdl.LastModified",
            "visible": false,
            "like": true,
            "data": "lastModified",
            "sWidth": "150px",
            "title": doc.getDocOnline("transversal", "DateModif"),
            "mRender": function (data, type, oObj) {
                return getDate(oObj["lastModified"]);
            }
        },
        {
            "sName": "tdl.LastModifier",
            "visible": false,
            "data": "lastModifier",
            "sWidth": "70px",
            "title": doc.getDocOnline("testdatalib", "lastmodifier")
        },
        {
            "sName": "tdd.column",
            "visible": false,
            "like": true,
            "data": "subDataColumn",
            "bSortable": false,
            "sWidth": "70px",
            "title": doc.getDocOnline("testdatalibdata", "column")
        },
        {
            "sName": "tdd.ParsingAnswer",
            "visible": false,
            "like": true,
            "data": "subDataParsingAnswer",
            "bSortable": false,
            "sWidth": "70px",
            "title": doc.getDocOnline("testdatalibdata", "parsingAnswer")
        },
        {
            "sName": "tdd.ColumnPosition",
            "visible": false,
            "data": "subDataColumnPosition",
            "bSortable": false,
            "sWidth": "70px",
            "title": doc.getDocOnline("testdatalibdata", "columnPosition")
        }
    ];

    return aoColumns;

}

