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
var nbRow = 0;

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

    // Load the select needed in localStorage cache.
    getSelectInvariant("ENVIRONMENT", true);
    getSelectInvariant("COUNTRY", true);

    //configure and create the dataTable
    var configurations = new TableConfigurationsServerSide("applicationsTable", "ReadApplication", "contentTable", aoColumnsFunc("applicationsTable"), [1, 'asc']);
    createDataTableWithPermissions(configurations, renderOptionsForApplication, "#applicationList", undefined, true);
}

function displayPageLabel() {
    var doc = new Doc();

    displayHeaderLabel(doc);
    $("#pageTitle").html(doc.getDocLabel("page_application", "title"));
    $("#title").html(doc.getDocOnline("page_application", "title"));
    $("#applicationListLabel").html(doc.getDocLabel("page_application", "table_application"));
    $("[name='createApplicationField']").html(doc.getDocLabel("page_application", "button_create"));
    $("[name='confirmationField']").html(doc.getDocLabel("page_application", "button_delete"));
    $("[name='editApplicationField']").html(doc.getDocLabel("page_application", "button_edit"));
    $("[name='buttonAdd']").html(doc.getDocLabel("page_global", "buttonAdd"));
    $("[name='buttonEdit']").html(doc.getDocLabel("page_global", "buttonEdit"));
    $("[name='buttonClose']").html(doc.getDocLabel("page_global", "buttonClose"));
    $("[name='buttonConfirm']").html(doc.getDocLabel("page_global", "buttonConfirm"));
    $("[name='buttonDismiss']").html(doc.getDocLabel("page_global", "buttonDismiss"));
    $("#editAppDefinition").html(doc.getDocLabel("page_global", "tab_definition"));
    $("#editAppAdvanced").html(doc.getDocLabel("page_global", "tab_advanced"));
    $("[name='applicationField']").html(doc.getDocOnline("application", "Application"));
    $("[name='descriptionField']").html(doc.getDocOnline("application", "Description"));
    //$("[name='sortField']").html(doc.getDocOnline("application", "sort"));
    $("[name='typeField']").html(doc.getDocOnline("application", "type"));
    $("[name='systemField']").html(doc.getDocOnline("application", "system"));
    $("[name='poolSizeField']").html(doc.getDocOnline("application", "poolSize"));
    $("[name='subsystemField']").html(doc.getDocOnline("application", "subsystem"));
    $("[name='repourlField']").html(doc.getDocOnline("application", "repourl"));
    $("[name='bugtrackerurlField']").html(doc.getDocOnline("application", "bugtrackerurl"));
    $("[name='bugtrackernewurlField']").html(doc.getDocOnline("application", "bugtrackernewurl"));
    $("[name='deploytypeField']").html(doc.getDocOnline("application", "deploytype"));
    $("[name='mavengroupidField']").html(doc.getDocOnline("application", "mavengroupid"));

    $("[name='tabsEdit1']").html(doc.getDocOnline("page_application", "tabDef"));
    $("[name='tabsEdit2']").html(doc.getDocOnline("page_application", "tabEnv"));

    $("#environmentHeader").html(doc.getDocOnline("invariant", "ENVIRONMENT"));
    $("#countryHeader").html(doc.getDocOnline("invariant", "COUNTRY"));
    $("#ipHeader").html(doc.getDocOnline("countryenvironmentparameters", "IP")
            + '<br>' + doc.getDocOnline("countryenvironmentparameters", "URLLOGIN"));
    $("#urlHeader").html(doc.getDocOnline("countryenvironmentparameters", "URL")
            + '<br>' + doc.getDocOnline("countryenvironmentparameters", "domain"));
    $("#var1Header").html(doc.getDocOnline("countryenvironmentparameters", "Var1")
            + '<br>' + doc.getDocOnline("countryenvironmentparameters", "Var2"));
    $("#var3Header").html(doc.getDocOnline("countryenvironmentparameters", "Var3")
            + '<br>' + doc.getDocOnline("countryenvironmentparameters", "Var4"));
    $("#poolSizeHeader").html(doc.getDocOnline("countryenvironmentparameters", "poolSize"));
    $("#mobileData").html(doc.getDocOnline("countryenvironmentparameters", "mobileActivity")
            + '<br>' + doc.getDocOnline("countryenvironmentparameters", "mobilePackage"));

    displayFooter(doc);
}

function renderOptionsForApplication(data) {
    var doc = new Doc();
    //check if user has permissions to perform the add and import operations
    if (data["hasPermissions"]) {
        if ($("#createApplicationButton").length === 0) {
            var contentToAdd = "<div class='marginBottom10'><button id='createApplicationButton' type='button' class='btn btn-default'>\n\
            <span class='glyphicon glyphicon-plus-sign'></span> " + doc.getDocLabel("page_application", "button_create") + "</button></div>";

            $("#applicationsTable_wrapper div#applicationsTable_length").before(contentToAdd);
            $('#applicationList #createApplicationButton').click(function () {
                openModalApplication(undefined, "ADD", "ApplicationList");
            });
        }
    }
}

function deleteEntryHandlerClick() {
    var idApplication = $('#confirmationModal').find('#hiddenField1').prop("value");



    var jqxhr = $.post("DeleteApplication", {application: idApplication}, "json");
    $.when(jqxhr).then(function (data) {
        var messageType = getAlertType(data.messageType);
        if (messageType === "success") {
            //redraw the datatable
            var oTable = $("#applicationsTable").dataTable();
            oTable.fnDraw(false);
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

function deleteEntryClick(idApplication) {
    clearResponseMessageMainPage();
    var doc = new Doc();
    var messageComplete = doc.getDocLabel("page_application", "message_delete");
    messageComplete = messageComplete.replace("%ENTRY%", idApplication);
    showModalConfirmation(deleteEntryHandlerClick, undefined, doc.getDocLabel("page_application", "button_delete"), messageComplete, idApplication, "", "", "");
}

function aoColumnsFunc(tableId) {
    var doc = new Doc();
    var aoColumns = [
        {
            "data": null,
            "title": doc.getDocLabel("page_global", "columnAction"),
            "bSortable": false,
            "bSearchable": false,
            "sWidth": "70px",
            "mRender": function (data, type, obj) {
                var hasPermissions = $("#" + tableId).attr("hasPermissions");

                var editApplication = '<button id="editApplication" onclick="openModalApplication(\'' + encodeURIComponent(obj["application"]) + '\', \'EDIT\', \'ApplicationList\');"\n\
                                    class="editApplication btn btn-default btn-xs margin-right5" \n\
                                    name="editApplication" title="' + doc.getDocLabel("page_application", "button_edit") + '" type="button">\n\
                                    <span class="glyphicon glyphicon-pencil"></span></button>';
                var viewApplication = '<button id="editApplication" onclick="openModalApplication(\'' + encodeURIComponent(obj["application"]) + '\', \'EDIT\', \'ApplicationList\');"\n\
                                    class="editApplication btn btn-default btn-xs margin-right5" \n\
                                    name="editApplication" title="' + doc.getDocLabel("page_application", "button_edit") + '" type="button">\n\
                                    <span class="glyphicon glyphicon-eye-open"></span></button>';
                var duplicateApplication = '<button id="duplicateApplication" onclick="openModalApplication(\'' + encodeURIComponent(obj["application"]) + '\', \'DUPLICATE\', \'ApplicationList\');"\n\
                                    class="duplicateApplication btn btn-default btn-xs margin-right5" \n\
                                    name="duplicateApplication" title="' + doc.getDocLabel("page_application", "button_duplicate") + '" type="button">\n\
                                    <span class="glyphicon glyphicon-duplicate"></span></button>';
                var deleteApplication = '<button id="deleteApplication" onclick="deleteEntryClick(\'' + obj["application"] + '\');" \n\
                                    class="deleteApplication btn btn-default btn-xs margin-right5" \n\
                                    name="deleteApplication" title="' + doc.getDocLabel("page_application", "button_delete") + '" type="button">\n\
                                    <span class="glyphicon glyphicon-trash"></span></button>';
                if (hasPermissions === "true") { //only draws the options if the user has the correct privileges
                    return '<div class="center btn-group width150">' + editApplication + duplicateApplication + deleteApplication + '</div>';
                }
                return '<div class="center btn-group width150">' + viewApplication + '</div>';
            }
        },
        {
            "data": "application",
            "sName": "application",
            "sWidth": "60px",
            "title": doc.getDocOnline("application", "Application")
        },
        {
            "data": "description",
            "like": true,
            "sName": "description",
            "sWidth": "80px",
            "title": doc.getDocOnline("application", "Description")
        },
        {
            "data": "sort",
            "visible": false,
            "sName": "sort",
            "sWidth": "60px",
            "title": doc.getDocOnline("application", "sort")
        },
        {
            "data": "type",
            "sName": "type",
            "sWidth": "60px",
            "title": doc.getDocOnline("application", "type"),
            "mRender": function (data, type, obj) {
                console.info(obj.type)
                return $("<div></div>")
                        .append($("<img style='height:30px; overflow:hidden; text-overflow:clip; border: 0px; padding:0; margin:0'></img>").text(obj.type).attr('src', './images/logoapp-' + obj.type + '.png'))
                        .html() + " " + obj.type;
            }
        },
        {
            "data": "system",
            "sName": "system",
            "sWidth": "60px",
            "title": doc.getDocOnline("application", "system")
        },
        {
            "data": "subsystem",
            "visible": false,
            "sName": "subsystem",
            "sWidth": "60px",
            "title": doc.getDocOnline("application", "subsystem")
        },
        {
            "data": "poolSize",
            "sName": "poolSize",
            "sWidth": "30px",
            "title": doc.getDocOnline("application", "poolSize"),
        },
        {
            "data": "bugTrackerConnector",
            "visible": false,
            "sName": "bugTrackerConnector",
            "sWidth": "80px",
            "title": doc.getDocOnline("application", "bugTrackerConnector")
        },
        {
            "data": "bugTrackerNewUrl",
            "visible": false,
            "like": true,
            "sName": "bugTrackerNewUrl",
            "sWidth": "80px",
            "title": doc.getDocOnline("application", "bugtrackernewurl"),
            "mRender": function (data, type, oObj) {
                return drawURL(data);
            }
        },
        {
            "data": "bugTrackerUrl",
            "visible": false,
            "like": true,
            "sName": "bugTrackerUrl",
            "sWidth": "80px",
            "title": doc.getDocOnline("application", "bugtrackerurl"),
            "mRender": function (data, type, oObj) {
                return drawURL(data);
            }
        },
        {
            "data": "repoUrl",
            "visible": false,
            "like": true,
            "sName": "repoUrl",
            "sWidth": "80px",
            "title": doc.getDocOnline("application", "repourl"),
            "mRender": function (data, type, oObj) {
                return drawURL(data);
            }
        },
        {
            "data": "deploytype",
            "visible": false,
            "sName": "deploytype",
            "sWidth": "60px",
            "title": doc.getDocOnline("application", "deploytype")
        },
        {
            "data": "mavengroupid",
            "visible": false,
            "sName": "mavengroupid",
            "sWidth": "60px",
            "title": doc.getDocOnline("application", "mavengroupid")
        }
    ];
    return aoColumns;
}


