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
        <title>Public Invariant</title>
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
                var oTable = $('#invariantPublicList').dataTable({
                    "aaSorting": [[1, "asc"]],
                    "bServerSide": true,
                    "sAjaxSource": "FindAllInvariantPublic",
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
                        {"sName": "ID", "sWidth": "5%"},
                        {"sName": "IdName", "sWidth": "10%"},
                        {"sName": "sort", "sWidth": "5%"},
                        {"sName": "value", "sWidth": "10%"},
                        {"sName": "description", "sWidth": "25%"},
                        {"sName": "veryShortdesc", "sWidth": "15%"},
                        {"sName": "gp1", "sWidth": "10%"},
                        {"sName": "gp2", "sWidth": "10%"},
                        {"sName": "gp3", "sWidth": "10%"}
                        
                    ]
                }
                ).makeEditable({
                    sAddURL: "CreateInvariant",
                    sAddHttpMethod: "POST",
                    oAddNewRowButtonOptions: {
                        label: "<b>Create Invariant entry...</b>",
                        background: "#AAAAAA",
                        icons: {primary: 'ui-icon-plus'}
                    },
                    sDeleteHttpMethod: "POST",
                    sDeleteURL: "DeleteInvariant",
                    sAddDeleteToolbarSelector: ".dataTables_length",
                    oDeleteRowButtonOptions: {
                        label: "Remove",
                        icons: {primary: 'ui-icon-trash'}
                    },
                    sUpdateURL: "UpdateInvariant",
                    fnOnEdited: function(status) {
                        $(".dataTables_processing").css('visibility', 'hidden');
                    },
                    oAddNewRowFormOptions: {
                        title: 'Add Incariant Entry',
                        show: "blind",
                        hide: "explode",
                        width: "1000px"
                    },
                    "aoColumns": [
                        null,
                        null,
                        null,
                        {onblur: 'submit',
                            placeholder: ''},
                        {onblur: 'submit',
                            placeholder: ''},
                        {onblur: 'submit',
                            placeholder: ''},
                        {onblur: 'submit',
                            placeholder: ''},
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

        <br>
        <div style="width: 80%;  font: 90% sans-serif">
            <table id="invariantPublicList" class="display">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>ID Name</th>
                        <th>Sort</th>
                        <th>Value</th>
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
        <div>
            <form id="formAddNewRow" action="#" title="Add Public Invariant" style="width:350px" method="post">
                <div style="width: 150px; float:left">
                    <label for="ID" style="font-weight:bold">ID</label>
                    <input id="ID" name="ID" style="width:50px;" 
                           class="ncdetailstext" rel="0" >
                </div>
                <div style="width: 200px; float:left">
                    <label for="IDName" style="font-weight:bold">ID Name</label>
                    <input id="IDName" name="IDName" style="width:100px;" 
                           class="ncdetailstext" rel="1" >
                </div>
                <div style="width: 150px; float:left">
                    <label for="Sort" style="font-weight:bold">Sort</label>
                    <input id="Sort" name="Sort" style="width:50px;" 
                           class="ncdetailstext" rel="2" ><br>
                </div>
                 <div style="width: 250px; float:left">
                    <label for="Value" style="font-weight:bold">Value</label>
                    <input id="Value" name="Value" style="width:150px;" 
                           class="ncdetailstext" rel="3" >
                </div>
                <div style="width: 300px; float:left">
                    <label for="Description" style="font-weight:bold">Description</label>
                    <input id="Description" name="Description" style="width:200px;" 
                           class="ncdetailstext" rel="4" >
                </div>
                <div style="width: 150px; float:left">
                    <label for="VeryShortDesc" style="font-weight:bold">Very Short Desc.</label>
                    <input id="VeryShortDesc" name="VeryShortDesc" style="width:100px;" 
                           class="ncdetailstext" rel="5" >
                </div>
                <br><br>
                <div style="width: 150px; float:left">
                    <label for="gp1" style="font-weight:bold">Group 1</label>
                    <input id="gp1" name="gp1" style="width:50px;" 
                           class="ncdetailstext" rel="6" >
                </div>
                <div style="width: 150px; float:left">
                    <label for="gp2" style="font-weight:bold">Group 2</label>
                    <input id="gp2" name="gp2" style="width:50px;" 
                           class="ncdetailstext" rel="7" >
                </div>
                <div style="width: 150px; float:left">
                    <label for="gp3" style="font-weight:bold">Group 3</label>
                    <input id="gp3" name="gp3" style="width:50px;" 
                           class="ncdetailstext" rel="8" >
                </div>
                <br />
                <button id="btnAddNewRowOk">Add</button>
                <button id="btnAddNewRowCancel">Cancel</button>
            </form>
        </div>
        <br><%
            out.print(display_footer(DatePageStart));
        %>
    </body>
</html>