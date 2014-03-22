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
<% Date DatePageStart = new Date() ; %>
<html>
    <head>
        <title>Projects</title>
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
            $(document).ready(function(){
                $('#projectsTable').dataTable({
                    "aaSorting": [[0, "asc"]],
                    "aLengthMenu": [
                        [20, 50, 100, 200, -1],
                        [20, 50, 100, 200, "All"]
                    ], 
                    "iDisplayLength" : 20,
                    "bServerSide": false,
                    "sAjaxSource": "GetProject",
                    "bJQueryUI": true,
                    "bProcessing": true,
                    "sPaginationType": "full_numbers",
                    "bSearchable": true, 
                    "aTargets": [ 0 ],
                    "aoColumns": [
                        {"sName": "IdProject", "sWidth": "15%"},
                        {"sName": "Code", "sWidth": "15%"},
                        {"sName": "Description", "sWidth": "45%"},
                        {"sName": "Active", "sWidth": "10%"},
                        {"sName": "dateCreation", "sWidth": "15%"}
                    ]
                }
            ).makeEditable({
                    sAddURL: "CreateProject",
                    sAddHttpMethod: "POST",
                    oAddNewRowButtonOptions: {
                        label: "<b>Create Project</b>",
                        background: "#AAAAAA",
                        icons: {primary: 'ui-icon-plus'}
                    },
                    sDeleteHttpMethod: "POST",
                    sDeleteURL: "DeleteProject",
                    sAddDeleteToolbarSelector: ".dataTables_length",
                    oDeleteRowButtonOptions: {
                        label: "Remove",
                        icons: {primary: 'ui-icon-trash'}
                    },
                    sUpdateURL: "UpdateProject",
                    oAddNewRowFormOptions: {
                        title: 'Add Invariant Entry',
                        show: "blind",
                        hide: "explode",
                        width: "600px"
                    },
                    "aoColumns": [
                        null,
                        {
                            submit:'Save changes'
                        },
                        {
                            submit:'Save changes'
                        },
                        {
                            loadtext: 'loading...',
                            type: 'select',
                            loadurl: 'GetInvariantList?idName=PROJECTACTIVE',
                            loadtype: 'GET',
                            submit:'Save changes'
                        },
                        null
                    ]
                });
            });
        </script>

    </head>
    <body>
        <%@ include file="include/function.jsp" %>
        <%@ include file="include/header.jsp" %>
            <%
                /*
                 * Database connexion
                 */
                Connection conn = db.connect();
                try {

            %>
        <div style="width: 80%; padding: 25px; font: 90% sans-serif">
            <table id="projectsTable" class="display">
                <thead>
                    <tr>
                        <th><%=dbDocS("Project","idProject","")%></th>
                        <th><%=dbDocS("Project","code","")%></th>
                        <th><%=dbDocS("Project","Description","")%></th>
                        <th><%=dbDocS("Project","Active","")%></th>
                        <th><%=dbDocS("Project","dateCreation","")%></th>
                    </tr>
                </thead>
                <tbody>
                </tbody>
            </table>
        </div>
    <div>
            <form id="formAddNewRow" action="#" title="Add Project" style="width:600px" method="post">
                    <label for="IDProject" style="font-weight:bold">Project ID</label>
                    <input id="IDProject" name="IDProject" style="width:150px;" 
                           class="ncdetailstext" rel="0" >
                <br><br>
                    <label for="Code" style="font-weight:bold">Code</label>
                    <input id="Code" name="Code" style="width:100px;" 
                           class="ncdetailstext" rel="1" >
                <br>
                    <label for="Description" style="font-weight:bold">Description</label>
                    <input id="Description" name="Description" style="width:400px;" 
                           class="ncdetailstext" rel="2" >
                <br>
                    <label for="Active" style="font-weight:bold">Active</label>
                    <%=ComboInvariantAjax(conn, "Active", "", "Active", "3", "PROJECTACTIVE", "", "", false)%>
                <div style="width: 200px; float:left; display: none">
                    <label for="dateCreation" style="font-weight:bold">dateCreation</label>
                    <input id="dateCreation" name="dateCreation" style="width:100px;" 
                           class="ncdetailstext" rel="4" >
                </div>
                <br><br>
                <button id="btnAddNewRowOk">Add</button>
                <button id="btnAddNewRowCancel">Cancel</button>
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
    </body>
</html>
