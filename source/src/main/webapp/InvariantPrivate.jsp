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

<!DOCTYPE html>
<% Date DatePageStart = new Date() ; %>
<html>
    <head>
        <title>Private Invariant</title>
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

        <script type="text/javascript">

            $(document).ready(function() {
                var oTable = $('#invariantPrivateList').dataTable({
                    "aaSorting": [[0, "asc"]],
                    "bServerSide": true,
                    "sAjaxSource": "FindAllInvariantPrivate",
                    "bJQueryUI": true,
                    "bProcessing": true,
                    "bPaginate": true,
                    "bAutoWidth": false,
                    "sPaginationType": "full_numbers",
                    "bSearchable": true,
                    "aTargets": [0],
                    "aLengthMenu": [
                        [10, 25, 50, 100, -1],
                        [10, 25, 50, 100, "All"]
                    ], 
                    "iDisplayLength": 25,
                    "aoColumns": [
                        {"sName": "IdName", "sWidth": "10%"},
                        {"sName": "value", "sWidth": "10%"},
                        {"sName": "sort", "sWidth": "5%"},
                        {"sName": "description", "sWidth": "30%"},
                        {"sName": "veryShortdesc", "sWidth": "15%"},
                        {"sName": "gp1", "sWidth": "10%"},
                        {"sName": "gp2", "sWidth": "10%"},
                        {"sName": "gp3", "sWidth": "10%"}
                        
                    ]
                }
                ).makeEditable({
                    "aoColumns": [
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null

                    ]
                })
            });


        </script>

    </head>
    <body  id="wrapper">
        <%@ include file="include/function.jsp" %>
        <%@ include file="include/header.jsp" %>
        <p class="dttTitle">Private Invariants</p>
        <div style="width: 100%;  font: 90% sans-serif">
            <table id="invariantPrivateList" class="display">
                <thead>
                    <tr>
                        <th>ID Name</th>
                        <th>Value</th>
                        <th>Sort</th>
                        <th>Description</th>
                        <th>Very Short Desc</th>
                        <th>Group 1</th>
                        <th>Group 2</th>
                        <th>Group 3</th>
                    </tr>
                </thead>
                <tbody>
                </tbody>
            </table>
        </div>
        <br><%
            out.print(display_footer(DatePageStart));
        %>
    </body>
</html>