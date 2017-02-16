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

    //configure and create the dataTable
    var configurations = new TableConfigurationsServerSide("soapLibrarysTable", "ReadAppService", "contentTable", aoColumnsFunc("soapLibrarysTable"), [1, 'asc']);
    createDataTableWithPermissions(configurations, renderOptionsForAppService, "#soapLibraryList", undefined, true);

}

function displayPageLabel() {
    var doc = new Doc();

    $("#title").html(doc.getDocLabel("appservice", "service"));

    displayAppServiceLabel(doc);

    displayHeaderLabel(doc);

    displayFooter(doc);

    displayGlobalLabel(doc);

    displayInvariantList("type", "SRVTYPE", false);
    displayInvariantList("method", "SRVMETHOD", false);
    displayApplicationList("application", "", "");
//    $('#application').select2();
}

function renderOptionsForAppService(data) {
    var doc = new Doc();
    if (data["hasPermissions"]) {
        if ($("#createSoapLibraryButton").length === 0) {
            var contentToAdd = "<div class='marginBottom10'><button id='createSoapLibraryButton' type='button' class='btn btn-default'>\n\
            <span class='glyphicon glyphicon-plus-sign'></span> " + doc.getDocLabel("page_appservice", "button_create") + "</button></div>";
            $("#soapLibrarysTable_wrapper div#soapLibrarysTable_length").before(contentToAdd);
            $('#soapLibraryList #createSoapLibraryButton').click(addAppServiceClick);
        }
    } else {
        if ($("#blankSpace").length === 0) {
            var contentToAdd = "<div class='marginBottom10' style='height:34px;' id='blankSpace'></div>";
            $("#soapLibrarysTable_wrapper div#soapLibrarysTable_length").before(contentToAdd);
        }
    }
}

//function editEntryModalCloseHandler() {
//    // reset form values
//    $('#editSoapLibraryModal #editSoapLibraryModalForm')[0].reset();
//    // remove all errors on the form fields
//    $(this).find('div.has-error').removeClass("has-error");
//    // clear the response messages of the modal
//    clearResponseMessage($('#editSoapLibraryModal'));
//}

//function addEntryClick() {
//    clearResponseMessageMainPage();
//    /**
//     * Clear previous form
//     */
//    $("#addSoapLibraryModal #idname").empty();
//    $('#addSoapLibraryModal #envelope').empty();
//    /**
//     * On edition, get the caret position, refresh the envelope to have 
//     * syntax coloration in real time, then set the caret position.
//     */
//    $('#addSoapLibraryModal #envelope').on("keyup", function (e) {
//        //Get the position of the carret
//        var pos = $(this).caret('pos');
//
//        //On Firefox only, when pressing enter, it create a <br> tag.
//        //So, if the <br> tag is present, replace it with <span>&#13;</span>
//        if ($("#addSoapLibraryModal #envelope br").length !== 0) {
//            $("#addSoapLibraryModal #envelope br").replaceWith("<span>&#13;</span>");
//            pos++;
//        }
//        //Apply syntax coloration
//        Prism.highlightElement($("#addSoapLibraryModal #envelope")[0]);
//        //Set the caret position to the initia one.
//        $(this).caret('pos', pos);
//    });
//
//    //On click on <pre> tag, focus on <code> tag to make the modification into this element,
//    //Add class on container to highlight field
//    $('#addSoapLibraryModal #envelopeContainer').on("click", function (e) {
//        $('#addSoapLibraryModal #envelopeContainer').addClass('highlightedContainer');
//        $('#addSoapLibraryModal #envelope').focus();
//    });
//
//    //Remove class to stop highlight envelop field
//    $('#addSoapLibraryModal #envelope').on('blur', function () {
//        $('#addSoapLibraryModal #envelopeContainer').removeClass('highlightedContainer');
//    });
//
//    $('#addSoapLibraryModal').modal('show');
//}

//function addEntryModalSaveHandler() {
//    clearResponseMessage($('#addSoapLibraryModal'));
//    var formEdit = $('#addSoapLibraryModal #addSoapLibraryModalForm');
//
//    // Get the header data from the form.
//    var data = convertSerialToJSONObject(formEdit.serialize());
//    //Add envelope, not in the form
//    data.envelope = encodeURIComponent($("#addSoapLibraryModalForm #envelope").text());
//
//    showLoaderInModal('#addSoapLibraryModal');
//    $.ajax({
//        url: "CreateAppService",
//        async: true,
//        method: "POST",
//        data: {
//            name: data.name,
//            type: data.type,
//            ServicePath: data.servicepath,
//            Method: data.method,
//            ParsingAnswer: data.parsinganswer,
//            Description: data.description,
//            Envelope: data.envelope
//        },
//        success: function (data) {
//            data = JSON.parse(data);
//            hideLoaderInModal('#addSoapLibraryModal');
//            if (getAlertType(data.messageType) === 'success') {
//                var oTable = $("#soapLibrarysTable").dataTable();
//                oTable.fnDraw(true);
//                $('#addSoapLibraryModal').modal('hide');
//                showMessage(data);
//            } else {
//                showMessage(data, $('#addSoapLibraryModal'));
//            }
//        },
//        error: showUnexpectedError
//    });
//
//}

//function addEntryModalCloseHandler() {
//    // reset form values
//    $('#addSoapLibraryModal #addSoapLibraryModalForm')[0].reset();
//    // remove all errors on the form fields
//    $(this).find('div.has-error').removeClass("has-error");
//    // clear the response messages of the modal
//    clearResponseMessage($('#addSoapLibraryModal'));
//}

function removeEntryClick(service) {
    var doc = new Doc();
    showModalConfirmation(function (ev) {
        var name = $('#confirmationModal #hiddenField1').prop("value");
        $.ajax({
            url: "DeleteAppService?service=" + name,
            async: true,
            method: "GET",
            success: function (data) {
                hideLoaderInModal('#removeSoapLibraryModal');
                var oTable = $("#soapLibrarysTable").dataTable();
                oTable.fnDraw(true);
                $('#removeSoapLibraryModal').modal('hide');
            },
            error: showUnexpectedError
        });

        $('#confirmationModal').modal('hide');
    }, doc.getDocLabel("page_appservice", "title_remove"), doc.getDocLabel("page_appservice", "message_remove").replace('%SERVICE%', service), service, undefined, undefined, undefined);
}

function aoColumnsFunc(tableId) {
    var doc = new Doc();
    var aoColumns = [
        {
            "data": null,
            "bSortable": false,
            "bSearchable": false,
            "title": doc.getDocLabel("page_global", "columnAction"),
            "mRender": function (data, type, obj) {
                var hasPermissions = $("#" + tableId).attr("hasPermissions");

                var editEntry = '<button id="editAppService" onclick="editAppServiceClick(\'' + obj["service"] + '\');"\n\
                                        class="editAppService btn btn-default btn-xs margin-right5" \n\
                                        name="editAppService" title="' + doc.getDocLabel("page_appservice", "button_edit") + '" type="button">\n\
                                        <span class="glyphicon glyphicon-pencil"></span></button>';

                var duplicateEntry = '<button id="duplicateAppService" onclick="duplicateAppServiceClick(\'' + escapeHtml(obj["service"]) + '\');"\n\
                                        class="duplicateEntry btn btn-default btn-xs margin-right5" \n\
                                        name="duplicateAppService" data-toggle="tooltip"  title="' + doc.getDocLabel("page_appservice", "btn_duplicate") + '" type="button">\n\
                                        <span class="glyphicon glyphicon-duplicate"></span></button>';

                var removeEntry = '<button id="removeAppService" onclick="removeEntryClick(\'' + obj["service"] + '\');"\n\
                                        class="removeAppService btn btn-default btn-xs margin-right5" \n\
                                        name="removeAppService" title="' + doc.getDocLabel("page_appservice", "button_remove") + '" type="button">\n\
                                        <span class="glyphicon glyphicon-trash"></span></button>';

                var viewEntry = '<button id="viewAppService" onclick="editAppServiceClick(\'' + obj["service"] + '\');"\n\
                                    class="editAppService btn btn-default btn-xs margin-right5" \n\
                                    name="viewAppService" title="' + doc.getDocLabel("page_application", "button_edit") + '" type="button">\n\
                                    <span class="glyphicon glyphicon-eye-open"></span></button>';

                if (hasPermissions === "true") { //only draws the options if the user has the correct privileges
                    return '<div class="center btn-group width150">' + editEntry + duplicateEntry + removeEntry + '</div>';
                }
                return '<div class="center btn-group width150">' + viewEntry + '</div>';

            },
            "width": "75px"
        },
        {
            "sName": "srv.Service",
            "data": "service",
            "sWidth": "150px",
            "title": doc.getDocLabel("appservice", "service")
        },
        {
            "sName": "srv.Application",
            "data": "application",
            "sWidth": "150px",
            "title": doc.getDocLabel("appservice", "application")
        },
        {
            "sName": "srv.Type",
            "data": "type",
            "sWidth": "150px",
            "title": doc.getDocLabel("appservice", "type")
        },
        {
            "sName": "ServicePath",
            "data": "servicePath",
            "sWidth": "150px",
            "title": doc.getDocLabel("appservice", "servicePath")
        },
        {
            "sName": "srv.ServiceRequest",
            "data": "serviceRequest",
            "title": doc.getDocLabel("appservice", "srvRequest"),
            "sWidth": "350px",
            "mRender": function (data, type, obj) {
                return $("<div></div>").append($("<pre style='height:20px; overflow:hidden; text-overflow:clip; border: 0px; padding:0; margin:0'></pre>").append($("<code name='envelopeField' class='language-markup'></code>").text(obj['serviceRequest']))).html();
            }
        },
        {
            "sName": "srv.operation",
            "data": "operation",
            "sWidth": "150px",
            "title": doc.getDocLabel("appservice", "operation")
        },
        {
            "sName": "srv.Method",
            "data": "method",
            "sWidth": "150px",
            "title": doc.getDocLabel("appservice", "method")
        },
        {
            "sName": "srv.group",
            "data": "group",
            "sWidth": "150px",
            "title": doc.getDocLabel("appservice", "group")
        },
        {
            "sName": "srv.Description",
            "data": "description",
            "sWidth": "150px",
            "title": doc.getDocLabel("appservice", "description")
        },
        {
            "sName": "srv.DateCreated",
            "data": "DateCreated",
            "sWidth": "150px",
            "title": doc.getDocOnline("transversal", "DateCreated")
        },
        {
            "sName": "srv.UsrCreated",
            "data": "UsrCreated",
            "sWidth": "70px",
            "title": doc.getDocOnline("transversal", "UsrCreated")
        },
        {
            "sName": "srv.DateModif",
            "data": "DateModif",
            "sWidth": "150px",
            "title": doc.getDocOnline("transversal", "DateModif")
        },
        {
            "sName": "srv.UsrModif",
            "data": "UsrModif",
            "sWidth": "70px",
            "title": doc.getDocOnline("transversal", "UsrModif")
        }
    ];
    return aoColumns;
}

/**
 * After table feeds, 
 * @returns {undefined}
 */
function afterTableLoad() {
    $.each($("code[name='envelopeField']"), function (i, e) {
        Prism.highlightElement($(e).get(0));
    });
}

