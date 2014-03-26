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
<%@page import="org.cerberus.service.IDocumentationService"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<% Date DatePageStart = new Date();%>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link rel="stylesheet" type="text/css" href="css/crb_style.css">
        <style media="screen" type="text/css">
            @import "css/demo_page.css";
            @import "css/demo_table.css";
            @import "css/demo_table_jui.css";
            @import "css/themes/base/jquery-ui.css";
            @import "css/themes/smoothness/jquery-ui-1.7.2.custom.css";
        </style>
        <link rel="shortcut icon" type="image/x-icon" href="images/favicon.ico">
        <script type="text/javascript" src="js/jquery-1.9.1.min.js"></script>
        <script type="text/javascript" src="js/jquery-ui-1.10.2.custom.min.js"></script>
        <script type="text/javascript" src="js/jquery.jeditable.mini.js"></script>
        <script type="text/javascript" src="js/jquery.dataTables.min.js"></script>
        <script type="text/javascript" src="js/jquery.dataTables.editable.js"></script>
        <script type="text/javascript" src="js/jquery.validate.min.js"></script>
        <title>Build Revision Definition</title>

    </head>
    <body>
        <%@ include file="include/function.jsp" %>
        <%@ include file="include/header.jsp" %>
        <%
            IDocumentationService docService = appContext.getBean(IDocumentationService.class);
        %>
        <script type="text/javascript">
            $(document).ready(function(){
                $('#buildrevTable').dataTable({
                    "aLengthMenu": [
                        [20, 50, 100, 200, -1],
                        [20, 50, 100, 200, "All"]
                    ], 
                    "iDisplayLength" : 50,
                    "bServerSide": false,
                    "sAjaxSource": "GetBuildRevisionInvariant?System=<%=request.getAttribute("MySystem")%>",
                    "bJQueryUI": true,
                    "bProcessing": true,
                    "aTargets": [0],
                    "sPaginationType": "full_numbers",
                    "aaSorting": [[ 1, "asc" ]],
                    "aoColumns": [
                        {"sName": "key", "bVisible": false, "sWidth": "0%"},
                        {"sName": "system", "sWidth": "20%"},
                        {"sName": "Level", "sWidth": "20%"},
                        {"sName": "Sequence", "sWidth": "20%"},
                        {"sName": "VersionName", "sWidth": "40%"}
                    ]
                }
            ).makeEditable({
                    sAddURL: "CreateBuildRevisionInvariant",
                    sAddHttpMethod: "POST",
                    oAddNewRowButtonOptions: {
                        label: "Add...",
                        icons: {primary:'ui-icon-plus'}
                    },
                    sDeleteHttpMethod: "POST",
                    sDeleteURL: "DeleteBuildRevisionInvariant",
                    sAddDeleteToolbarSelector: ".dataTables_length",
                    oDeleteRowButtonOptions: {
                        label: "Remove",
                        icons: {primary: 'ui-icon-trash'}
                    },
                    oAddNewRowFormOptions: {
                        title: 'Add Build/Revision Entry',
                        show: "blind",
                        hide: "explode",
                        width: "600px"
                    },
                    sUpdateURL: "UpdateBuildRevisionInvariant",
                    fnOnEdited: function(status){
                        $(".dataTables_processing").css('visibility', 'hidden');
                    },
                    "aoColumns": [
                        null,
                        null,
                        null,
                        {
                            submit:'Save changes'
                        }
                    ]
                });
            });
        </script>
        <p class="dttTitle">Build / Revision Definition</p>
        <div style="width: 100%; font: 90% sans-serif">
            <table id="buildrevTable" class="display">
                <thead>
                    <tr>
                        <th>Key</th>
                        <th><%=docService.findLabel("application", "SYSTEM", "")%></th>
                        <th><%=docService.findLabel("buildrevisioninvariant", "level", "Level")%></th>
                        <th><%=docService.findLabel("buildrevisioninvariant", "seq", "Seq")%></th>
                        <th><%=docService.findLabel("buildrevisioninvariant", "versionname", "Version Name")%></th>
                    </tr>
                </thead>
                <tbody>
                </tbody>
            </table>
        </div>
        <div>
            <form id="formAddNewRow" action="#" title="Add Build Rev Invariant" style="width:600px" method="post">
                <div style="width: 200px; float:left; display: none">
                    <label for="InvKey" style="font-weight:bold">InvKey</label>
                    <input id="InvKey" name="InvKey" style="width:100px;" 
                           class="ncdetailstext" rel="0" >
                </div>
                <label for="System" style="font-weight:bold"><%=docService.findLabelHTML("application", "SYSTEM", "")%></label>
                <input id="System" name="System" style="width:150px;" 
                       class="System" rel="1" value="<%=request.getAttribute("MySystem")%>" readonly>
                <br><br>
                <label for="Level" style="font-weight:bold"><%=docService.findLabelHTML("buildrevisioninvariant", "level", "")%></label>
                <select id="Level" name="Level" style="width:100px;" 
                        class="Level" rel="2" ><option value="1">1</option><option value="2">2</option></select>
                <br>
                <label for="Seq" style="font-weight:bold"><%=docService.findLabelHTML("buildrevisioninvariant", "Seq", "")%></label>
                <input id="Seq" name="Seq" style="width:50px;" 
                       class="Seq" rel="3" >
                <br>
                <label for="VersionName" style="font-weight:bold"><%=docService.findLabelHTML("buildrevisioninvariant", "versionname", "")%></label>
                <input id="VersionName" name="VersionName" style="width:400px;" 
                       class="VersionName" rel="4" >
                <br><br>
                <button id="btnAddNewRowOk">Add</button>
                <button id="btnAddNewRowCancel">Cancel</button>
            </form>
        </div>
        <br><% out.print(display_footer(DatePageStart));%>
    </body>
</html>
