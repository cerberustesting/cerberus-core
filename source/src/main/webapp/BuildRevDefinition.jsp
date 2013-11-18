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
                    "sPaginationType": "full_numbers",
                    "aaSorting": [[ 1, "asc" ]],
                    "aoColumns": [
                        {"mDataProp": "system", "sName": "system"},
                        {"mDataProp": "level", "sName": "Level"},
                        {"mDataProp": "seq", "sName": "Sequence"},
                        {"mDataProp": "versionName", "sName": "Version Name"}
                    ]
                }
            ).makeEditable({
                    sAddURL: "AddBuildRevisionInvariant",
                    sAddHttpMethod: "POST",
                    oAddNewRowButtonOptions: {
                        label: "Add...",
                        icons: {primary:'ui-icon-plus'}
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
        <div style="width: 80%; padding: 25px; font: 90% sans-serif">
            <table id="buildrevTable" class="display">
                <thead>
                    <tr>
                        <th>System</th>
                        <th>Level</th>
                        <th>Seq</th>
                        <th>Version Name</th>
                    </tr>
                </thead>
                <tbody>
                </tbody>
            </table>
        </div>
        <br><% out.print(display_footer(DatePageStart));%>
    </body>
</html>
