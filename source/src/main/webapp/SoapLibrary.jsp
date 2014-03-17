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
        <title>SOAP Library</title>
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
                var oTable = $('#soapLibraryList').dataTable({
                    "aaSorting": [[1, "desc"]],
                    "bServerSide": true,
                    "sAjaxSource": "FindAllSoapLibrary",
                    "bJQueryUI": true,
                    "bProcessing": true,
                    "bPaginate": true,
                    "bAutoWidth": false,
                    "sPaginationType": "full_numbers",
                    "bSearchable": true,
                    "aTargets": [0],
                    "iDisplayLength": 25,
                    "aoColumns": [
                        {"sName": "Name", "sWidth": "10%"},
                        {"sName": "Type", "sWidth": "10%"},
                        {"sName": "Envelope", "sWidth": "40%", "format":"text"},
                        {"sName": "Description", "sWidth": "40%"},
                        {"sName": "ServicePath", "sWidth": "40%"},
                        {"sName": "Method", "sWidth": "40%"},
                        {"sName": "ParsingAnswer", "sWidth": "40%"}
                    ]
                }
                ).makeEditable({
                    sAddURL: "CreateSoapLibrary",
                    sAddHttpMethod: "POST",
                    oAddNewRowButtonOptions: {
                        label: "<b>Create SOAP Data...</b>",
                        background: "#AAAAAA",
                        icons: {primary: 'ui-icon-plus'}
                    },
                    sDeleteHttpMethod: "POST",
                    sDeleteURL: "DeleteSoapLibrary",
                    sAddDeleteToolbarSelector: ".dataTables_length",
                    oDeleteRowButtonOptions: {
                        label: "Remove",
                        icons: {primary: 'ui-icon-trash'}
                    },
                    sUpdateURL: "UpdateSoapLibrary",
                    fnOnEdited: function(status) {
                        $(".dataTables_processing").css('visibility', 'hidden');
                    },
                    oAddNewRowFormOptions: {
                        title: 'Add SOAP Data',
                        show: "blind",
                        hide: "explode",
                        width: "1000px"
                    },
                    "aoColumns": [
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
            <table id="soapLibraryList" class="display">
                <thead>
                    <tr>
                        <th>Name</th>
                        <th>Type</th>
                        <th>Envelope</th>
                        <th>Description</th>
                        <th>ServicePath</th>
                        <th>Method</th>
                        <th>ParsingAnswer</th>
                    </tr>
                </thead>
                <tbody>
                </tbody>
            </table>
        </div>
        <div>
            <form id="formAddNewRow" action="#" title="Add SOAP Data" style="width:350px" method="post">
                <div style="width: 250px; float:left">
                    <label for="Name" style="font-weight:bold">Name</label>
                    <input id="Name" name="Name" style="width:150px;" 
                           class="ncdetailstext" rel="0" >
                </div>
                <div style="width: 310px; float:left">
                    <label for="Type" style="font-weight:bold">Type</label>
                    <input id="Type" name="Type" style="width:210px;" 
                           class="ncdetailstext" rel="1" >
                </div>
                 <div style="width: 310px; float:left">
                    <label for="Envelope" style="font-weight:bold">Envelope</label>
                    <input id="Envelope" name="Envelope" style="width:210px;" 
                           class="ncdetailstext" rel="2" >
                </div>
                <div style="width: 250px; float:left">
                    <label for="Description" style="font-weight:bold">Description</label>
                    <input id="Description" name="Description" style="width:150px;" 
                           class="ncdetailstext" rel="3" >
                </div>
                  <div style="width: 250px; float:left">
                    <label for="ServicePath" style="font-weight:bold">ServicePath</label>
                    <input id="ServicePath" name="ServicePath" style="width:150px;" 
                           class="ncdetailstext" rel="4" >
                </div>
                <div style="width: 250px; float:left">
                    <label for="Method" style="font-weight:bold">Method</label>
                    <input id="Method" name="Method" style="width:150px;" 
                           class="ncdetailstext" rel="5" >
                </div>
                <div style="width: 250px; float:left">
                    <label for="ParsingAnswer" style="font-weight:bold">ParsingAnswer</label>
                    <input id="ParsingAnswer" name="ParsingAnswer" style="width:150px;" 
                           class="ncdetailstext" rel="6" >
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
