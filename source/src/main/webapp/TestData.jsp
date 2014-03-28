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
<% Date DatePageStart = new Date();%>
<html>
    <head>
        <title>Test Data</title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
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
        <script type="text/javascript">

            $(document).ready(function() {
                var oTable = $('#testDataList').dataTable({
                    "aaSorting": [[0, "asc"]],
                    "bServerSide": true,
                    "sAjaxSource": "FindAllTestData",
                    "bJQueryUI": true,
                    "bProcessing": true,
                    "bPaginate": true,
                    "bAutoWidth": false,
                    "sPaginationType": "full_numbers",
                    "bSearchable": true,
                    "aTargets": [0],
                    "iDisplayLength": 25,
                    "aoColumns": [
                        {"sName": "Key", "sWidth": "20%"},
                        {"sName": "Value", "sWidth": "30%", "sClass": "center"},
                        {"sName": "Description", "sWidth": "50%", "sClass": "center"}
                    ]
                }
            ).makeEditable({
                    sAddURL: "CreateTestData",
                    sAddHttpMethod: "POST",
                    oAddNewRowButtonOptions: {
                        label: "<b>Create Data...</b>",
                        background: "#AAAAAA",
                        icons: {primary: 'ui-icon-plus'}
                    },
                    sDeleteHttpMethod: "POST",
                    sDeleteURL: "DeleteTestData",
                    sAddDeleteToolbarSelector: ".dataTables_length",
                    oDeleteRowButtonOptions: {
                        label: "Remove",
                        icons: {primary: 'ui-icon-trash'}
                    },
                    sUpdateURL: "UpdateTestData",
                    fnOnEdited: function(status) {
                        $(".dataTables_processing").css('visibility', 'hidden');
                    },
                    oAddNewRowFormOptions: {
                        title: 'Add Data',
                        show: "blind",
                        hide: "explode",
                        width: "950px"
                    },
                    "aoColumns": [
                        null,
                        {onblur: 'submit',
                            placeholder: ''},
                        {onblur: 'submit',
                            placeholder: ''}

                    ]
                })
            });


        </script>

    </head>
    <body  id="wrapper">
        <%@ include file="include/function.jsp" %>
        <%@ include file="include/header.jsp" %>

        <p class="dttTitle">Test Data</p>
        <div style="width: 100%; font: 90% sans-serif">
            <table id="testDataList" class="display">
                <thead>
                    <tr>
                        <th>Key</th>
                        <th>Value</th>
                        <th>Description</th>
                    </tr>
                </thead>
                <tbody>
                </tbody>
            </table>
        </div>
        <div>
            <form id="formAddNewRow" action="#" title="Add Data" style="width:350px" method="post">
                <label for="Key" style="font-weight:bold">Key</label>
                <input id="Key" name="Key" style="width:300px;" 
                       class="ncdetailstext" rel="0" >
                <label for="Value" style="font-weight:bold">Value</label>
                <input id="Value" name="Value" style="width:500px;" 
                       class="ncdetailstext" rel="1" >
                <br />
                <br />
                <label for="Description" style="font-weight:bold">Description</label>
                <input id="Description" name="Description" style="width:800px;" 
                       class="ncdetailstext" rel="2" >
                <br />
                <br />
                <div style="width: 250px; float:right">
                    <button id="btnAddNewRowOk">Add</button>
                    <button id="btnAddNewRowCancel">Cancel</button>
                </div>
            </form>
        </div>
        <br><%
            out.print(display_footer(DatePageStart));
        %>
    </body>
</html>