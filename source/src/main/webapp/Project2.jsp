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
<% Date DatePageStart = new Date() ; %>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <title>Projects</title>
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
                    "bProcessing": false,
                    "sPaginationType": "full_numbers",
                    "bSearchable": true, 
                    "aTargets": [ 0 ],
                    "aoColumns": [
                        {"sName": "idProject", "sWidth": "15%"},
                        {"sName": "VCCode", "sWidth": "15%"},
                        {"sName": "Description", "sWidth": "45%"},
                        {"sName": "Active", "sWidth": "10%"},
                        {"sName": "dateCre", "sWidth": "15%"}
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
                    oAddNewRowFormOptions: {
                        title: 'Add Invariant Entry',
                        show: "blind",
                        hide: "explode",
                        width: "600px"
                    },
                    sUpdateURL: "UpdateProject",
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
                IDocumentationService docService = appContext.getBean(IDocumentationService.class);
                String myLang = request.getAttribute("MyLang").toString();

            %>
        <p class="dttTitle">Project</p>
        <div style="width: 100%;  font: 90% sans-serif">
            <table id="projectsTable" class="display">
                <thead>
                    <tr>
                        <th><%=docService.findLabel("Project","idProject","", myLang)%></th>
                        <th><%=docService.findLabel("Project","code","", myLang)%></th>
                        <th><%=docService.findLabel("Project","Description","", myLang)%></th>
                        <th><%=docService.findLabel("Project","Active","", myLang)%></th>
                        <th><%=docService.findLabel("Project","dateCreation","", myLang)%></th>
                    </tr>
                </thead>
                <tbody>
                </tbody>
            </table>
        </div>
    <div>
            <form id="formAddNewRow" action="#" title="Add Project" style="width:600px" method="post">
                <label for="IDProject" style="font-weight:bold"><%=docService.findLabelHTML("Project","idProject","", myLang)%></label>
                    <input id="IDProject" name="idProject" style="width:150px;" 
                           class="ncdetailstext" rel="0" >
                <br><br>
                    <label for="Code" style="font-weight:bold"><%=docService.findLabelHTML("Project","code","", myLang)%></label>
                    <input id="Code" name="VCCode" style="width:100px;" 
                           class="ncdetailstext" rel="1" >
                <br>
                    <label for="Description" style="font-weight:bold"><%=docService.findLabelHTML("Project","Description","", myLang)%></label>
                    <input id="Description" name="Description" style="width:400px;" 
                           class="ncdetailstext" rel="2" >
                <br>
                    <label for="Active" style="font-weight:bold"><%=docService.findLabelHTML("Project","Active","", myLang)%></label>
                    <%=ComboInvariantAjax(appContext, "Active", "", "Active", "3", "PROJECTACTIVE", "", "", false)%>
                <div style="width: 200px; float:left; display: none">
                    <label for="dateCreation" style="font-weight:bold"><%=docService.findLabelHTML("Project","dateCreation","", myLang)%></label>
                    <input id="dateCreation" name="dateCre" style="width:100px;" 
                           class="ncdetailstext" rel="4" >
                </div>
                <br><br>
                <button id="btnAddNewRowOk">Add</button>
                <button id="btnAddNewRowCancel">Cancel</button>
            </form>
    </div>
        <br><%
            out.print(display_footer(DatePageStart));
        %>
    </body>
</html>
