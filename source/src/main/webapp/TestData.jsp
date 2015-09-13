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
<% Date DatePageStart = new Date();%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
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

            var deleteParams;

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
                    "bSearchable": false,
                    "iDisplayLength": 25,
                    "aoColumns": [
                        {"sName": "Key", "sWidth": "20%", "bVisible": false},
                        {"sName": "Key", "sWidth": "20%"},
                        {"sName": "Value", "sWidth": "30%", "sClass": "center"},
                        {"sName": "Description", "sWidth": "40%", "sClass": "center"},
                        {"sName": "Application", "sWidth": "5%", "sClass": "center"},
                        {"sName": "Environment", "sWidth": "3%", "sClass": "center"},
                        {"sName": "Country", "sWidth": "2%", "sClass": "center"}
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
                    fnOnDeleting: function(tr, id, fnDeleteRow) {
                        var param = "{ id: '" + id + "',"
                                + "Application: '" + $(tr).children()[3].innerHTML + "',"
                                + "Environment: '" + $(tr).children()[4].innerHTML + "',"
                                + "Country: '" + $(tr).children()[5].innerHTML + "' }";
                        fnDeleteRow(param);
                        return false;
                    },
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
                });
                
                
                $.get('./ReadApplication', '', function(data) {
                    $("#Application").empty();
                    $("#Application").append("<option value=\"\"> </option>");
                    for (index = 0; index < data.contentTable.length; index++) {
                        $("#Application").append("<option value=\"" + data.contentTable[index].application + "\"><b>" + data.contentTable[index].application + "<b> - " + data.contentTable[index].description + "</option>");
                    }
                });

                $.get('./FindInvariantByID', 'idName=ENVIRONMENT', function(data) {
                    $("#Environment").empty();
                    $("#Environment").append("<option value=\"\"> </option>");
                    for (index = 0; index < data.length; index++) {
                        $("#Environment").append("<option value=\"" + data[index].value + "\">" + data[index].value + " - " + data[index].description + "</option>");
                    }
                });

                $.get('./FindInvariantByID', 'idName=COUNTRY', function(data) {
                    $("#Country").empty();
                    $("#Country").append("<option value=\"\"> </option>");
                    for (index = 0; index < data.length; index++) {
                        $("#Country").append("<option value=\"" + data[index].value + "\">" + data[index].value + " - " + data[index].description + "</option>");
                    }
                });
                
                <%
                if(request.getParameter("Search") != null) {
                    String search = request.getParameter("Search");
                    %>$("#testDataList_filter input[type='search']").delay(1000).val("<%=search%>").trigger( "change" );<%
                }
                %>

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
                        <th>ID</th>
                        <th>Key</th>
                        <th>Value</th>
                        <th>Description</th>
                        <th>Application</th>
                        <th>Environment</th>
                        <th>Country</th>
                    </tr>
                </thead>
                <tbody>
                </tbody>
            </table>
        </div>
        <div>
            <form id="formAddNewRow" action="#" title="Add Data" style="width:350px" method="post">
                <label for="idNew" style="display:none">ID</label>
                <input id="idNew" name="idNew" style="display:none;" 
                       class="ncdetailstext" rel="0" >
                <label for="Key" style="font-weight:bold">Key</label>
                <input id="Key" name="Key" style="width:300px;" 
                       class="ncdetailstext" rel="1" >
                <label for="Value" style="font-weight:bold">Value</label>
                <input id="Value" name="Value" style="width:500px;" 
                       class="ncdetailstext" rel="2" >
                <br />
                <br />
                <label for="Description" style="font-weight:bold">Description</label>
                <input id="Description" name="Description" style="width:800px;" 
                       class="ncdetailstext" rel="3" >
                <br />
                <br />
                <label for="Application" style="font-weight:bold">Application</label>
                <select id="Application" name="Application" 
                        class="ncdetailstext" rel="4" >
                </select>
                <br />
                <br />
                <label for="Environment" style="font-weight:bold">Environment</label>
                <select id="Environment" name="Environment" 
                        class="ncdetailstext" rel="5" >
                </select>
                <br />
                <br />
                <label for="Country" style="font-weight:bold">Country</label>
                <select id="Country" name="Country"
                        class="ncdetailstext" rel="6" >
                </select>
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