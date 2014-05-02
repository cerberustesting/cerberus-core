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
<% Date DatePageStart = new Date();%>

<!DOCTYPE html>
<html>
    <head>
        <title>Robot</title>
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
                var oTable = $('#robotList').dataTable({
                    "aaSorting": [[1, "asc"]],
                    "bServerSide": true,
                    "sAjaxSource": "FindAllRobot",
                    "bJQueryUI": true,
                    "bProcessing": true,
                    "bPaginate": true,
                    "bAutoWidth": false,
                    "sPaginationType": "full_numbers",
                    "bSearchable": true,
                    "aTargets": [0],
                    "iDisplayLength": 25,
                    "aoColumns": [
                        {"sName": "ID", "sWidth": "10%", "bVisible":false},
                        {"sName": "Name", "sWidth": "10%"},
                        {"sName": "Ip", "sWidth": "10%"},
                        {"sName": "Port", "sWidth": "10%"},
                        {"sName": "Platform", "sWidth": "10%"},
                        {"sName": "Browser", "sWidth": "10%"},
                        {"sName": "Version", "sWidth": "10%"},
                        {"sName": "Description", "sWidth": "10%"},
                        
                    ]
                }
            ).makeEditable({
                    sAddURL: "CreateRobot",
                    sAddHttpMethod: "POST",
                    oAddNewRowButtonOptions: {
                        label: "<b>Create Robot...</b>",
                        background: "#AAAAAA",
                        icons: {primary: 'ui-icon-plus'}
                    },
                    sDeleteHttpMethod: "POST",
                    sDeleteURL: "DeleteRobot",
                    sAddDeleteToolbarSelector: ".dataTables_length",
                    oDeleteRowButtonOptions: {
                        label: "Remove",
                        icons: {primary: 'ui-icon-trash'}
                    },
                    sUpdateURL: "UpdateRobot",
                    fnOnEdited: function(status) {
                        $(".dataTables_processing").css('visibility', 'hidden');
                    },
                    oAddNewRowFormOptions: {
                        title: 'Add Robot',
                        show: "blind",
                        hide: "explode",
                        width: "900px"
                    },
                    "aoColumns": [
                        null,
                        {onblur: 'submit',
                            placeholder: ''}, 
                        {onblur: 'submit',
                            placeholder: ''},
                        {loadtext: 'loading...',
                            type: 'select',
                            onblur: 'submit',
                            loadurl: 'GetInvariantList?idName=platform',
                            loadtype:'GET'}, 
                        {loadtext: 'loading...',
                            type: 'select',
                            onblur: 'submit',
                            loadurl: 'GetInvariantList?idName=browser',
                            loadtype:'GET'},
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
        <%
            /*
             * Database connexion
             */
            Connection conn = db.connect();
            try {
                IDocumentationService docService = appContext.getBean(IDocumentationService.class);

        %>

        <p class="dttTitle">Robot</p>
        <div style="width: 100%; font: 90% sans-serif">
            <table id="robotList" class="display">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Name</th>
                        <th>Ip</th>
                        <th>Port</th>
                        <th>Platform</th>
                        <th>Browser</th>
                        <th>Version</th>
                        <th>Description</th>
                        
                    </tr>
                </thead>
                <tbody>
                </tbody>
            </table>
        </div>
        <div>
            <form id="formAddNewRow" action="#" title="Add SQL Data" style="width:350px" method="post">
                <label for="ID" style="font-weight:bold; display:none">ID</label>
                <input id="ID" name="ID" style="width:350px;display:none" 
                       class="ncdetailstext" rel="0" >
                <label for="Name" style="font-weight:bold; width:100px;">Name</label>
                <input id="Name" name="Name" style="width:350px;" 
                       class="ncdetailstext" rel="1" placeholder="Example : MyRobot">
                <br />
                <br />
                <label for="Ip" style="font-weight:bold">Ip</label>
                <input id="Ip" name="Ip" style="width:350px;" 
                       class="ncdetailstext" rel="2" placeholder="Example : 127.0.0.1">
                <br />
                <br />
                <label for="Port" style="font-weight:bold">Port</label>
                <input id="Port" name="Port" style="width:350px;" 
                       class="ncdetailstext" rel="3" placeholder="Example : 5555">
                <br />
                <br />
                <label for="Platform" style="font-weight:bold">Platform</label>
                <select id="Platform" name="Platform" style="width:350px;" 
                        class="ncdetailstext" rel="4"></select>
                <br />
                <br />
                <label for="Browser" style="font-weight:bold">Browser</label>
                <select id="Browser" name="Browser" style="width:350px;" 
                        class="ncdetailstext" rel="5" ></select>
                <br />
                <br />
                <label for="Version" style="font-weight:bold">Version</label>
                <input id="Version" name="Version" style="width:350px;" rows="5" 
                       class="ncdetailstext" rel="6" placeholder="Browser Version : 27 for example">
                <br />
                <br />
                <label for="Description" style="font-weight:bold">Description</label>
                <input id="Description" name="Description" style="width:350px;" 
                       class="ncdetailstext" rel="7" placeholder="Example : Used for Regression Testing">
                <br />
                <br />
                <div style="width: 250px; float:right">
                    <button id="btnAddNewRowOk">Add</button>
                    <button id="btnAddNewRowCancel">Cancel</button>
                </div>
            </form>
            <%
                } catch (Exception e) {
                    out.println(e);
                } finally {
                    try {
                        conn.close();
                    } catch (Exception ex) {
                    }
                }
            %>
        </div>
         <br><%
            out.print(display_footer(DatePageStart));
        %>
    <script type="text/javascript">
            (document).ready($.getJSON('FindInvariantByID?idName=browser', function(data) {
                $("#Browser").empty();

                for (var i = 0; i < data.length; i++) {
                    $("#Browser").append($("<option></option>")
                            .attr("value", data[i].value)
                            .text(data[i].value + " ( " + data[i].description + " )"));
                }

            }));

        </script>
        <script type="text/javascript">
            (document).ready($.getJSON('FindInvariantByID?idName=platform', function(data) {
                $("#Platform").empty();

                    
                for (var i = 0; i < data.length; i++) {
                    $("#Platform").append($("<option></option>")
                            .attr("value", data[i].value)
                            .text(data[i].value + " ( " + data[i].description + " )"));
                }

            }));

        </script>
    </body>
</html>