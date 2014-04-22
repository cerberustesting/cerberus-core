<%--
  ~ Cerberus  Copyright (C) 2013  vertigo17
  ~ DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
  ~
  ~ This file is part of Cerberus.
  ~
  ~ Cerberus is free software: you can redistribute it and/or modify
  ~ it under the terms of the GNU General Public License as published by
  ~ the Free Software Foundation, either version 3 of the License, or
  ~ (at your option) any later version.
  ~
  ~ Cerberus is distributed in the hope that it will be useful,
  ~ but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~ GNU General Public License for more details.
  ~
  ~ You should have received a copy of the GNU General Public License
  ~ along with Cerberus.  If not, see <http://www.gnu.org/licenses/>.
--%>
<%@page import="org.cerberus.entity.Application"%>
<%@page import="org.cerberus.service.IDocumentationService"%>
<%@page import="org.cerberus.service.IApplicationService"%>
<%@page import="org.cerberus.service.IDatabaseVersioningService"%>
<% Date DatePageStart = new Date();%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta content="text/html; charset=UTF-8" http-equiv="content-type">
        <title>Test Campaign</title>
        <link rel="stylesheet" type="text/css" href="css/crb_style.css">
        <link rel="stylesheet" type="text/css" href="css/jquery.dataTables.css">
        <link rel="stylesheet" type="text/css" href="css/jquery-ui.css">
        <link rel="stylesheet" type="text/css" href="css/dataTables_jui.css">
        <link rel="shortcut icon" type="image/x-icon" href="images/favicon.ico">
        <script type="text/javascript" src="js/jquery-1.9.1.min.js"></script>
        <script type="text/javascript" src="js/jquery-ui-1.10.2.js"></script>
        <script type="text/javascript" src="js/jquery.dataTables.js"></script>
        <script type="text/javascript" src="js/jquery.jeditable.mini.js"></script>
        <script type="text/javascript" src="js/jquery.dataTables.editable.js"></script>
        <script type="text/javascript" src="js/jquery.validate.min.js"></script>
        
        <style>
            .halfsize {
                width: 48%;
                margin: 3px;
                vertical-align: top;
                display: inline-block;
                *zoom: 1;
                *display: inline;
            }
            
            .formForDataTable {
                display: none;
            }
            
            #campaigns {
                margin: 10px 0;
            }
        </style>
    </head>
    <body>
        <%@ include file="include/function.jsp"%>
        <%@ include file="include/header.jsp"%>
        <div id="campaigns">
        List of Test Campaigns :
        <table  class="display" id="listOfCampaigns" name="listOfCampaigns">
            <thead>
                <tr>
                    <th>ID</th>
                    <th>Campaign</th>
                    <th>Description</th>
                </tr>
            </thead>
        </table>
        </div>
        <div>
            <div id="parameters" class="halfsize">
                <table  class="display" id="listOfParameters" name="listOfParameters">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Campaign</th>
                            <th>Parameter</th>
                            <th>Value</th>
                        </tr>
                    </thead>
                </table>
            </div>
            <div id="contents" class="halfsize">
                <table  class="display" id="listOfContents" name="listOfContents">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Campaign</th>
                            <th>Test Battery</th>
                        </tr>
                    </thead>
                </table>
            </div>
        </div>
        <script>
            var oTable, oTableParameter, oTableContent;
            function refreshCampaigns() {
                $('#parameters').hide();
                $('#contents').hide();
                
                oTable = $('#listOfCampaigns').dataTable({
                    "aaSorting": [[0, "asc"]],
                    "bServerSide": false,
                    "sAjaxSource": "GetCampaign?action=findAllCampaign",
                    "sAjaxDataProp": "Campaigns",
                    "bJQueryUI": true,
                    "bProcessing": true,
                    "bPaginate": true,
                    "bAutoWidth": false,
                    "sPaginationType": "full_numbers",
                    "bSearchable": true,
                    "aTargets": [0],
                    "iDisplayLength": 25,
                    "aoColumns": [
                        {"sName": "ID", "bVisible": false},
                        {"sName": "Campaign", "sWidth": "30%"},
                        {"sName": "Description", "sWidth": "70%"}
                    ]
                }).makeEditable({
                    sAddURL: "AddCampaign",
                    sAddNewRowFormId: "formAddNewCampaign",
                    sAddNewRowButtonId: "btnAddNewCampaign",
                    sAddNewRowOkButtonId: "btnAddNewCampaignOk",
                    sAddNewRowCancelButtonId: "btnAddNewCampaignCancel",
                    sDeleteRowButtonId: "btnDeleteCampaign",
                    sAddDeleteToolbarSelector: "#listOfCampaigns_length",
                    sAddHttpMethod: "POST",
                    oAddNewRowButtonOptions: {
                        label: "<b>Create Campaign</b>",
                        background: "#AAAAAA",
                        icons: {primary: 'ui-icon-plus'}
                    },
                    oAddNewRowFormOptions: {
                        title: 'Add Campaign Entry',
                        show: "blind",
                        hide: "explode",
                        width: "700px"
                    },
                    sDeleteHttpMethod: "POST",
                    sDeleteURL: "DeleteCampaign",
                    oDeleteRowButtonOptions: {
                        label: "Remove",
                        icons: {primary:'ui-icon-trash'}
                    },
                    sUpdateURL: "UpdateCampaign",
                    fnOnEdited: function(status){
                        $(".dataTables_processing").css('visibility', 'hidden');
                    },
                    "aoColumns": [
                        null,
                        {
                            indicator   : 'Saving...',
                            tooltip     : 'Double Click to edit...',
                            style       : 'display: inline',
                            onblur      : 'submit'
                        },
                        {
                            indicator   : 'Saving...',
                            tooltip     : 'Double Click to edit...',
                            style       : 'display: inline',
                            onblur      : 'submit'
                        }
                    ]
                });
                
                /* Add a click handler to the rows - this could be used as a callback */
                $('#listOfCampaigns tbody').click(function(event) {
                        refreshParameters($(event.target.parentNode).attr("id"));
                        refreshContents($(event.target.parentNode).attr("id"));
                });
            };
            
            function refreshParameters(id) {
                $('#parameters').hide();
                
                $('#CampaignIdForParameter').attr('value',id);
                
                // This example is fairly pointless in reality, but shows how fnDestroy can be used
                if(oTableParameter) {
                    $('#listOfParameters tbody').empty();
                    $('#listOfParameters').dataTable().fnDestroy();
                }

                $.getJSON("GetCampaign","action=findAllCampaignParameter&campaign="+id,function(data){
                    oTableParameter = $('#listOfParameters').dataTable({
                        "aaSorting": [[0, "asc"]],
                        "bServerSide": false,
                        "bJQueryUI": true,
                        "bProcessing": true,
                        "bDestroy": true,
                        "bPaginate": true,
                        "bAutoWidth": false,
                        "sPaginationType": "full_numbers",
                        "bSearchable": true,
                        "aTargets": [0],
                        "iDisplayLength": 25,
                        "aoColumns": [
                            {"sName": "ID", "bVisible": false},
                            {"sName": "Campaign", "bVisible": false},
                            {"sName": "Parameter", "sWidth": "50%"},
                            {"sName": "Value", "sWidth": "50%"}
                        ],
                        "aaData" : data.CampaignParameters
                    }).makeEditable({
                        sAddURL: "AddCampaignParameter",
                        sAddNewRowFormId: "formAddNewParameter",
                        sAddNewRowButtonId: "btnAddNewParameter",
                        sAddNewRowOkButtonId: "btnAddNewParameterOk",
                        sAddNewRowCancelButtonId: "btnAddNewParameterCancel",
                        sAddDeleteToolbarSelector: "#listOfParameters_length",
                        sDeleteRowButtonId: "btnDeleteParameter",

                        sAddHttpMethod: "POST",
                        oAddNewRowButtonOptions: {
                            label: "<b>Create Parameter</b>",
                            background: "#AAAAAA",
                            icons: {primary: 'ui-icon-plus'}
                        },
                        oAddNewRowFormOptions: {
                            title: 'Add Parameter',
                            show: "blind",
                            hide: "explode",
                            width: "700px"
                        },
                        sDeleteHttpMethod: "POST",
                        sDeleteURL: "DeleteCampaignParameter",
                        oDeleteRowButtonOptions: {
                            label: "Remove",
                            icons: {primary:'ui-icon-trash'}
                        },
                        sUpdateURL: "UpdateCampaignParameter",
                        fnOnEdited: function(status){
                            $(".dataTables_processing").css('visibility', 'hidden');
                        },
                        "aoColumns": [
                            null,
                            {
                                indicator   : 'Saving...',
                                tooltip     : 'Double Click to edit...',
                                style       : 'display: inline',
                                onblur      : 'submit'
                            }
                        ]
                    });
                    $('#parameters').show();
                });
                


            };
            
            function refreshContents(id) {
                $('#contents').hide();

                $('#CampaignIdForContent').attr('value',id);
                
                // This example is fairly pointless in reality, but shows how fnDestroy can be used
                if(oTableContent) {
                    $('#listOfContents tbody').empty();
                    $('#listOfContents').dataTable().fnDestroy();
                }

                $.getJSON("GetCampaign","action=findAllCampaignContent&campaign="+id,function(data){
                    oTableContent = $('#listOfContents').dataTable({
                        "aaSorting": [[0, "asc"]],
                        "bServerSide": false,
                        "bJQueryUI": true,
                        "bProcessing": true,
                        "bDestroy": true,
                        "bPaginate": true,
                        "bAutoWidth": false,
                        "sPaginationType": "full_numbers",
                        "bSearchable": true,
                        "aTargets": [0],
                        "iDisplayLength": 25,
                        "aoColumns": [
                            {"sName": "ID", "bVisible": false},
                            {"sName": "Campaign", "bVisible": false},
                            {"sName": "Test Battery"}
                        ],
                        "aaData" : data.CampaignContents
                    }).makeEditable({
                        sAddURL: "AddCampaignContent",
                        sAddNewRowFormId: "formAddNewContent",
                        sAddNewRowButtonId: "btnAddNewContent",
                        sAddNewRowOkButtonId: "btnAddNewContentOk",
                        sAddNewRowCancelButtonId: "btnAddNewContentCancel",
                        sAddDeleteToolbarSelector: "#listOfContents_length",
                        sDeleteRowButtonId: "btnDeleteContent",

                        sAddHttpMethod: "POST",
                        oAddNewRowButtonOptions: {
                            label: "<b>Create Content</b>",
                            background: "#AAAAAA",
                            icons: {primary: 'ui-icon-plus'}
                        },
                        oAddNewRowFormOptions: {
                            title: 'Add Content',
                            show: "blind",
                            hide: "explode",
                            width: "700px"
                        },
                        sDeleteHttpMethod: "POST",
                        sDeleteURL: "DeleteCampaignContent",
                        oDeleteRowButtonOptions: {
                            label: "Remove",
                            icons: {primary:'ui-icon-trash'}
                        },
                        sUpdateURL: "UpdateCampaignContent",
                        fnOnEdited: function(status){
                            $(".dataTables_processing").css('visibility', 'hidden');
                        },
                        "aoColumns": [
                            {
                                indicator   : 'Saving...',
                                tooltip     : 'Double Click to edit...',
                                style       : 'display: inline',
                                onblur      : 'submit'
                            }
                        ]
                    });
                    $('#contents').show();
                });
                


            };
            
            
            $(document).ready(function(){
                refreshCampaigns();
            });
        </script>
            <form id="formAddNewCampaign" class="formForDataTable" action="#" title="Add Campaign Entry" style="width:600px" method="post">
                <input type="hidden" value="-1" id="ID" name="ID" class="ncdetailstext" rel="0" >
                <label for="Campaign" style="font-weight:bold">Campaign</label>
                <input id="Campaign" name="Campaign" class="ncdetailstext" rel="1" >
                <br><br>
                <label for="Description" style="font-weight:bold">Description</label>
                <input id="Description" name="Description" class="ncdetailstext" rel="2" >
            </form>
            <form id="formAddNewParameter" class="formForDataTable" action="#" title="Add Parameter Entry" style="width:600px" method="post">
                <input type="hidden" value="-1" id="IDParameter" name="ID" class="ncdetailstext" rel="0" >
                <input type="hidden" value="-1" id="CampaignIdForParameter" name="Campaign" class="ncdetailstext" rel="1">
                <br><br>
                <label for="Parameter" style="font-weight:bold">Parameter</label>
                <input id="Parameter" name="Parameter" class="ncdetailstext" rel="2" >
                <br><br>
                <label for="Value" style="font-weight:bold">Value</label>
                <input id="Value" name="Value" class="ncdetailstext" rel="3" >
            </form>
            <form id="formAddNewContent" class="formForDataTable" action="#" title="Add Content Entry" style="width:600px" method="post">
                <input type="hidden" value="-1" id="IDContent" name="ID" class="ncdetailstext" rel="0" >
                <input type="hidden" value="-1" id="CampaignIdForContent" name="Campaign" class="ncdetailstext" rel="1">
                <br><br>
                <label for="TestBattery" style="font-weight:bold">TestBattery</label>
                <input id="TestBattery" name="TestBattery" class="ncdetailstext" rel="2" >
            </form>
     </body>
</html>
