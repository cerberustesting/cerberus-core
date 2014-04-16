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
        <script type="text/javascript" src="js/jquery.jeditable.mini.js"></script>
        <script type="text/javascript" src="js/jquery.dataTables.min.js"></script>
        <script type="text/javascript" src="js/jquery.dataTables.editable.js"></script>
        <script type="text/javascript" src="js/jquery.validate.min.js"></script>
    </head>
    <body>
        <%@ include file="include/function.jsp"%>
        <%@ include file="include/header.jsp"%>
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
        <script>
            $(document).ready(function(){
                var oTable = $('#listOfCampaigns').dataTable({
                    "aaSorting": [[0, "asc"]],
                    "bServerSide": true,
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
                    sAddDeleteToolbarSelector: ".dataTables_length",
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
            });
        </script>
            <form id="formAddNewRow" action="#" title="Add Campaign Entry" style="width:600px" method="post">
                <input type="hidden" value="-1" id="ID" name="ID" class="ncdetailstext" rel="0" >
                <label for="Campaign" style="font-weight:bold">Campaign</label>
                <input id="Campaign" name="Campaign" class="ncdetailstext" rel="1" >
                <br><br>
                <label for="Description" style="font-weight:bold">Description</label>
                <input id="Description" name="Description" class="ncdetailstext" rel="2" >
                <br><br>
                <button id="btnAddNewRowOk">Add</button>
                <button id="btnAddNewRowCancel">Cancel</button>
            </form>
     </body>
</html>
