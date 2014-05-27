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
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>Execution Reporting : Status</title>

    <link rel="stylesheet" type="text/css" href="css/crb_style.css">
    <link rel="stylesheet" type="text/css" href="css/jquery.dataTables.css">
    <link rel="stylesheet" type="text/css" href="css/jquery-ui.css">
    <link rel="stylesheet" type="text/css" href="css/dataTables_jui.css">
    <script type="text/javascript" src="js/jquery-1.9.1.min.js"></script>
    <script type="text/javascript" src="js/jquery-ui-1.10.2.js"></script>
    <script type="text/javascript" src="js/jquery.dataTables.min.js"></script>
    <script type="text/javascript" src="js/FixedHeader.js"></script>

    <script type="text/javascript">
        var oTable;
        $(document).ready(function(){
            //columns will be added based on the form
            $('#Comment').before("<th colspan='2'>PT</th>");
            $('#Country').append("<th>Firefox</th>").append("<th>Chrome</th>");

            oTable = $('#reporting').dataTable({
                "bServerSide": true,
                "sAjaxSource": "GetReport",
                "bJQueryUI": true,
                "bProcessing": true,
                "bFilter": false,
                "bInfo": false,
                "iDisplayLength" : -1,
                "fnInitComplete": function () {
                    new FixedHeader( oTable, {
                        left:   true,
                        zTop: 98
                    });

                    $('.FixedHeader_Left table tr#Country th').remove();
                }
            });
        });
    </script>
    <style>
        div.FixedHeader_Cloned th,
        div.FixedHeader_Cloned td {
            background-color: white !important;
        }
    </style>
</head>
<body>
    <p class="dttTitle">Reporting Status</p>
    <div>
        <table id="reporting" class="display">
            <thead>
                <tr>
                    <th rowspan="2">Test</th>
                    <th rowspan="2">TestCase</th>
                    <th rowspan="2">Application</th>
                    <th rowspan="2">Description</th>
                    <th rowspan="2">Priority</th>
                    <th rowspan="2">Status</th>
                    <th rowspan="2" id="Comment">Comment</th>
                    <th rowspan="2">Bug ID</th>
                    <th rowspan="2">Group</th>
                </tr>
                <tr id="Country"></tr>
            </thead>
            <tbody></tbody>
        </table>
    </div>
</body>
</html>