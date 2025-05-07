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
            'container': 'body'
        });
    });
});

function initPage() {
    displayPageLabel();

    // Load Application Combo (used on service modal).
    displayApplicationList("application", "", "", "");

    // configure and create the dataTable
    var configurations = new TableConfigurationsServerSide("soapLibrarysTable", "ReadAppService", "contentTable", aoColumnsFunc("soapLibrarysTable"), [1, 'asc']);
    createDataTableWithPermissions(configurations, renderOptionsForAppService, "#soapLibraryList", undefined, true);

    $('#testCaseListModal').on('hidden.bs.modal', getTestCasesUsingModalCloseHandler);
}

function displayPageLabel() {
    var doc = new Doc();

    $("#title").html(doc.getDocLabel("page_appservice", "title"));
    $("#pageTitle").html(doc.getDocLabel("page_appservice", "title"));
    displayHeaderLabel(doc);
    displayFooter(doc);
    displayGlobalLabel(doc);

}

function renderOptionsForAppService(data) {
    var doc = new Doc();
    if (data["hasPermissions"]) {
        if ($("#createSoapLibraryButton").length === 0) {
            var contentToAdd = "<div class='marginBottom10'><button id='createSoapLibraryButton' type='button' class='btn btn-default'>\n\
            <span class='glyphicon glyphicon-plus-sign'></span> "
                    + doc.getDocLabel("page_appservice", "button_create")
                    + "</button></div>";
            $("#soapLibrarysTable_wrapper div#soapLibrarysTable_length")
                    .before(contentToAdd);
            $('#soapLibraryList #createSoapLibraryButton').off("click");
            $('#soapLibraryList #createSoapLibraryButton').click(function () {
                openModalAppService(undefined, "ADD");
            });
        }
    } else {
        if ($("#blankSpace").length === 0) {
            var contentToAdd = "<div class='marginBottom10' id='blankSpace'></div>";
            $("#soapLibrarysTable_wrapper div#soapLibrarysTable_length")
                    .before(contentToAdd);
        }
    }
}

function removeEntryClick(service) {
    var doc = new Doc();
    showModalConfirmation(function (ev) {
        var name = $('#confirmationModal #hiddenField1').prop("value");
        $.ajax({
            url: "DeleteAppService?service=" + encodeURIComponent(name),
            async: true,
            dataType: "json",
            method: "GET",
            success: function (data) {

                var messageType = getAlertType(data.messageType);
                if (messageType === "success") {

                }
                hideLoaderInModal('#removeSoapLibraryModal');
                var oTable = $("#soapLibrarysTable").dataTable();
                oTable.fnDraw(false);
                $('#removeSoapLibraryModal').modal('hide');
                showMessageMainPage(messageType, data.message, false);

            },
            error: showUnexpectedError
        });

        $('#confirmationModal').modal('hide');
    }, undefined, doc.getDocLabel("page_appservice", "title_remove"), doc
            .getDocLabel("page_appservice", "message_remove").replace(
            '%SERVICE%', service), service, undefined, undefined,
            undefined);
}

/**
 * Handler that cleans the test case list modal when it is closed
 */
function getTestCasesUsingModalCloseHandler() {
    //we need to clear the item-groups that were inserted
    $('#testCaseListModal #testCaseListGroup a[id*="cat"]').remove();
    $('#testCaseListModal #testCaseListGroup div[id*="sub_cat"]').remove();
}

/**
 * Function that loads all test cases that are associated with the selected entry
 * @param {type} service service name
 */
function getTestCasesUsingService(service) {
    clearResponseMessageMainPage();
    showLoaderInModal('#testCaseListModal');
    var jqxhr = $.getJSON("ReadAppService", "service=" + service + "&testcase=Y");

    var doc = new Doc();

    $("#testCaseListModalLabel").text("List of test cases affected by the service : " + service)

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
                htmlContent += '<span class="pull-left"><a href="' + hrefTest + '" target="_blank">' + obj2.TestCaseNumber + '- ' + obj2.TestCaseDescription + '</a></span></br>';
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
        $('#testCaseListModal').modal('show');

    }).fail(handleErrorAjaxAfterTimeout);

}

function aoColumnsFunc(tableId) {
    var doc = new Doc();
    var aoColumns = [
        {
            "data": null,
            "bSortable": false,
            "bSearchable": false,
            "sWidth": "150px",
            "title": doc.getDocLabel("page_global", "columnAction"),
            "mRender": function (data, type, obj) {
                var hasPermissions = $("#" + tableId)
                        .attr("hasPermissions");

                var editEntry = '<button id="editEntry" onclick="openModalAppService(\''
                        + obj["service"]
                        + '\', \'EDIT\'  );"\n\
                                    class="editApplicationObject btn btn-default btn-xs margin-right5" \n\
                                    name="editApplicationObject" title="'
                        + doc.getDocLabel("page_appservice", "button_edit")
                        + '" type="button">\n\
                                    <span class="glyphicon glyphicon-pencil"></span></button>';
                var duplicateEntry = '<button class="btn btn-default btn-xs margin-right5" \n\
					                    	 name="duplicateApplicationObject" title="'
                        + doc.getDocLabel("page_testdatalib", "tooltip_duplicateEntry")
                        + '" type="button" onclick="openModalAppService(\'' + obj["service"] + '\', \'DUPLICATE\'  )">\n\
					                        <span class="glyphicon glyphicon-duplicate"></span></button>';
                var viewEntry = '<button id="editEntry" onclick="openModalAppService(\''
                        + obj["service"]
                        + '\',\'EDIT\');"\n\
                                    class="editApplicationObject btn btn-default btn-xs margin-right5" \n\
                                    name="editApplicationObject" title="'
                        + doc.getDocLabel("page_appservice", "button_edit")
                        + '" type="button">\n\
                                    <span class="glyphicon glyphicon-eye-open"></span></button>';
                var deleteEntry = '<button id="deleteEntry" onclick="deleteEntryClick(\''
                        + obj["service"]
                        + '\');" \n\
                                    class="deleteApplicationObject btn btn-default btn-xs margin-right5" \n\
                                    name="deleteApplicationObject" title="'
                        + doc.getDocLabel("page_appservice",
                                "button_delete")
                        + '" type="button">\n\
                                    <span class="glyphicon glyphicon-trash"></span></button>';


                var viewTestCase = '<button class="getTestCasesUsing btn  btn-default btn-xs margin-right5" \n\
                        name="getTestCasesUsing" title="' + doc.getDocLabel("page_testdatalib", "tooltip_gettestcases") + '" type="button" \n\
                        onclick="getTestCasesUsingService(' + "'" + obj.service + '\')"><span class="glyphicon glyphicon-list"></span></button>';


                if (hasPermissions === "true") {
                    return '<div class="center btn-group width250">'
                            + editEntry + duplicateEntry + deleteEntry + viewTestCase
                            + '</div>';
                }
                return '<div class="center btn-group width250">'
                        + viewEntry + '</div>';

            }
        },
        {
            "sName": "srv.collection",
            "data": "collection",
            "sWidth": "150px",
            "title": doc.getDocLabel("appservice", "collection")
        }, {
            "sName": "srv.Service",
            "data": "service",
            "sWidth": "150px",
            "title": doc.getDocLabel("appservice", "service")
        },
        {
            "sName": "srv.Type",
            "data": "type",
            "sWidth": "50px",
            "title": doc.getDocLabel("appservice", "type"),
            "mRender": function (data, type, obj) {
                return $("<div></div>")
                        .append($("<img style='height:20px; overflow:hidden; text-overflow:clip; border: 0px; padding:0; margin:0'></img>").text(obj.type).attr('src', './images/logo-' + obj.type + '.png'))
                        .html();
            }
        }, {
            "sName": "srv.Method",
            "visible": true,
            "data": "method",
            "sWidth": "50px",
            "title": doc.getDocLabel("appservice", "method")
        }, {
            "sName": "srv.Application",
            "data": "application",
            "sWidth": "150px",
            "title": doc.getDocLabel("appservice", "application")
        }, {
            "sName": "ServicePath",
            "like": true,
            "data": "servicePath",
            "sWidth": "150px",
            "title": doc.getDocLabel("appservice", "servicePath")
        }, {
            "sName": "BodyType",
            "visible": false,
            "data": "bodyType",
            "sWidth": "70px",
            "title": doc.getDocLabel("appservice", "bodyType")
        }, {
            "sName": "srv.ServiceRequest",
            "visible": false,
            "like": true,
            "data": "serviceRequest",
            "title": doc.getDocLabel("appservice", "srvRequest"),
            "sWidth": "350px",
            "mRender": function (data, type, obj) {
                return $("<div></div>")
                        .append(
                                $(
                                        "<pre name='envelopeField' style='height:20px; overflow:hidden; text-overflow:clip; border: 0px; padding:0; margin:0'></pre>")
                                .text(obj['serviceRequest']))
                        .html();
            }
        }, {
            "sName": "srv.operation",
            "visible": false,
            "like": true,
            "data": "operation",
            "sWidth": "150px",
            "title": doc.getDocLabel("appservice", "operation")
        }, {
            "sName": "srv.Description",
            "like": true,
            "data": "description",
            "sWidth": "200px",
            "title": doc.getDocLabel("appservice", "description")
        }, {
            "sName": "srv.authType",
            "visible": false,
            "data": "authType",
            "sWidth": "100px",
            "title": doc.getDocLabel("appservice", "authType")
        }, {
            "sName": "srv.kafkaTopic",
            "visible": false,
            "data": "kafkaTopic",
            "sWidth": "150px",
            "title": doc.getDocLabel("appservice", "kafkaTopic")
        }, {
            "sName": "srv.kafkaKey",
            "visible": false,
            "data": "kafkaKey",
            "sWidth": "150px",
            "title": doc.getDocLabel("appservice", "kafkaKey")
        }, {
            "sName": "srv.kafkaFilterPath",
            "visible": false,
            "data": "kafkaFilterPath",
            "sWidth": "150px",
            "title": doc.getDocLabel("appservice", "kafkaFilterPath")
        }, {
            "sName": "srv.kafkaFilterValue",
            "visible": false,
            "data": "kafkaFilterValue",
            "sWidth": "150px",
            "title": doc.getDocLabel("appservice", "kafkaFilterValue")
        }, {
            "sName": "srv.kafkaFilterHeaderPath",
            "visible": false,
            "data": "kafkaFilterHeaderPath",
            "sWidth": "150px",
            "title": doc.getDocLabel("appservice", "kafkaFilterHeaderPath")
        }, {
            "sName": "srv.kafkaFilterHeaderValue",
            "visible": false,
            "data": "kafkaFilterHeaderValue",
            "sWidth": "150px",
            "title": doc.getDocLabel("appservice", "kafkaFilterHeaderValue")
        }, {
            "sName": "srv.isAvroEnable",
            "visible": false,
            "data": "isAvroEnable",
            "sWidth": "50px",
            "title": doc.getDocLabel("appservice", "isAvroEnable")
        }, {
            "sName": "srv.schemaRegistryURL",
            "visible": false,
            "data": "schemaRegistryURL",
            "sWidth": "150px",
            "title": doc.getDocLabel("appservice", "schemaRegistryURL")
        }, {
            "sName": "srv.isAvroEnableKey",
            "visible": false,
            "data": "isAvroEnableKey",
            "sWidth": "50px",
            "title": doc.getDocLabel("appservice", "isAvroEnableKey")
        }, {
            "sName": "srv.avroSchemaKey",
            "visible": false,
            "data": "avroSchemaKey",
            "sWidth": "150px",
            "title": doc.getDocLabel("appservice", "avroSchemaKey")
        }, {
            "sName": "srv.isAvroEnableValue",
            "visible": false,
            "data": "isAvroEnableValue",
            "sWidth": "50px",
            "title": doc.getDocLabel("appservice", "isAvroEnableValue")
        }, {
            "sName": "srv.avroSchemaValue",
            "visible": false,
            "data": "avroSchemaValue",
            "sWidth": "150px",
            "title": doc.getDocLabel("appservice", "avroSchemaValue")
        }, {
            "sName": "srv.parentContentService",
            "visible": false,
            "data": "parentContentService",
            "sWidth": "150px",
            "title": doc.getDocLabel("appservice", "parentContentService")
        }, {
            "sName": "srv.Description",
            "like": true,
            "data": "description",
            "sWidth": "200px",
            "title": doc.getDocLabel("appservice", "description")
        }, {
            "sName": "srv.dateCreated",
            "visible": false,
            "like": true,
            "data": "dateCreated",
            "sWidth": "150px",
            "title": doc.getDocOnline("transversal", "DateCreated"),
            "mRender": function (data, type, oObj) {
                return getDate(oObj["DateCreated"]);
            }
        }, {
            "sName": "srv.usrCreated",
            "visible": false,
            "data": "usrCreated",
            "sWidth": "70px",
            "title": doc.getDocOnline("transversal", "UsrCreated")
        }, {
            "sName": "srv.dateModif",
            "visible": false,
            "like": true,
            "data": "dateModif",
            "sWidth": "150px",
            "title": doc.getDocOnline("transversal", "DateModif"),
            "mRender": function (data, type, oObj) {
                return getDate(oObj["DateModif"]);
            }
        }, {
            "sName": "srv.usrModif",
            "visible": false,
            "data": "usrModif",
            "sWidth": "70px",
            "title": doc.getDocOnline("transversal", "UsrModif")
        }];
    return aoColumns;
}

function deleteEntryHandlerClick() {
    var application = $('#confirmationModal').find('#hiddenField1').prop(
            "value");
    var object = $('#confirmationModal').find('#hiddenField2').prop("value");
    var jqxhr = $.post("DeleteApplicationService", {
        service: service
    }, "json");
    $.when(jqxhr).then(function (data) {
        var messageType = getAlertType(data.messageType);
        if (messageType === "success") {
            // redraw the datatable
            var oTable = $("#soapLibrarysTable").dataTable();
            oTable.fnDraw(false);
            var info = oTable.fnGetData().length;

            if (info === 1) {// page has only one row, then returns to the
                // previous page
                oTable.fnPageChange('previous');
            }
        }
        // show message in the main page
        showMessageMainPage(messageType, data.message, false);
        // close confirmation window
        $('#confirmationModal').modal('hide');
    }).fail(handleErrorAjaxAfterTimeout);
}

function deleteEntryClick(service) {
    clearResponseMessageMainPage();
    var doc = new Doc();
    var messageComplete = doc.getDocLabel("page_appservice", "message_delete");
    messageComplete = messageComplete.replace("%ENTRY%", service);
    removeEntryClick(service);
}

