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
    }, undefined, doc.getDocLabel("page_appservice", "title_remove"), doc.getDocLabel("page_appservice", "message_remove").replace('%SERVICE%', service), service, undefined, undefined, undefined);
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
                return $("<div></div>").append($("<pre name='envelopeField' style='height:20px; overflow:hidden; text-overflow:clip; border: 0px; padding:0; margin:0'></pre>").text(obj['serviceRequest'])).html();
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
    $.each($("pre[name='envelopeField']"), function (i, e) {
        //Highlight envelop on modal loading
        var editor = ace.edit($(e).get(0));
        editor.setTheme("ace/theme/chrome");
        editor.getSession().setMode(defineAceMode(editor.getSession().getDocument().getValue()));
        editor.setOptions({
            maxLines: 1,
            showLineNumbers: false,
            showGutter: false,
            highlightActiveLine: false,
            highlightGutterLine: false,
            readOnly: true
        });
        editor.renderer.$cursorLayer.element.style.opacity = 0;
    });
}

