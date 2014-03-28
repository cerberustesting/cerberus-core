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
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<% Date DatePageStart = new Date();%>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link rel="stylesheet" type="text/css" href="css/crb_style.css">
        <link rel="stylesheet" type="text/css" href="css/jquery.dataTables.css">
        <link rel="stylesheet" type="text/css" href="css/jquery-ui.css">
        <link rel="stylesheet" type="text/css" href="css/dataTables_jui.css">
        <link rel="stylesheet" type="text/css" href="css/jquery-ui.css">
        <link rel="shortcut icon" type="image/x-icon" href="images/favicon.ico">
        <script type="text/javascript" src="js/jquery-1.9.1.min.js"></script>
        <script type="text/javascript" src="js/jquery-ui-1.10.2.js"></script>
        <script type="text/javascript" src="js/jquery.jeditable.mini.js"></script>
        <script type="text/javascript" src="js/jquery.dataTables.min.js"></script>
        <script type="text/javascript" src="js/jquery.dataTables.editable.js"></script>
        <script type="text/javascript" src="js/jquery.validate.min.js"></script>
        <title>Applications</title>

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
                IDocumentationService docService = appContext.getBean(IDocumentationService.class);

        %>
        <script type="text/javascript">      
            $(document).ready(function(){
                $('#applicationsTable').dataTable({
                    "aLengthMenu": [
                        [20, 50, 100, 200, -1],
                        [20, 50, 100, 200, "All"]
                    ], 
                    "iDisplayLength" : 20,
                    "bServerSide": false,
                    "sAjaxSource": "GetApplication?System=<%=request.getAttribute("MySystem")%>",
                    "bJQueryUI": true,
                    "bProcessing": true,
                    "sPaginationType": "full_numbers",
                    "bSearchable": false, 
                    "aTargets": [ 0 ],
                    "aoColumns": [
                        {"sName": "Application", "sWidth": "10%"},
                        {"sName": "System", "sWidth": "5%"},
                        {"sName": "SubSystem", "sWidth": "5%"},
                        {"sName": "Description", "sWidth": "10%"},
                        {"sName": "Type", "sWidth": "10%"},
                        {"sName": "Maven Group ID", "sWidth": "5%"},
                        {"sName": "Deploy Type", "sWidth": "5%"},
                        {"sName": "sort", "sWidth": "5%"},
                        {"sName": "svn URL", "sWidth": "15%"},
                        {"sName": "Bug Tracker URL", "sWidth": "15%"},
                        {"sName": "New Bug URL", "sWidth": "15%"}
                    ]
                }
            ).makeEditable({
                    sAddURL: "CreateApplication",
                    sAddHttpMethod: "POST",
                    oAddNewRowButtonOptions: {
                        label: "<b>Create Application</b>",
                        background: "#AAAAAA",
                        icons: {primary: 'ui-icon-plus'}
                    },
                    sDeleteHttpMethod: "POST",
                    sDeleteURL: "DeleteApplication",
                    sAddDeleteToolbarSelector: ".dataTables_length",
                    oDeleteRowButtonOptions: {
                        label: "Remove",
                        icons: {primary: 'ui-icon-trash'}
                    },
                    oAddNewRowFormOptions: {
                        title: 'Add Application Entry',
                        show: "blind",
                        hide: "explode",
                        width: "700px"
                    },
                    sUpdateURL: "UpdateApplication",
                    fnOnEdited: function(status){
                        $(".dataTables_processing").css('visibility', 'hidden');
                    },
                    "aoColumns": [
                        null,
                        {
                            loadtext: 'loading...',
                            type: 'select',
                            loadurl: 'GetInvariantList?idName=SYSTEM',
                            loadtype: 'GET',
                            submit:'Save changes'
                        },
                        {
                            submit:'Save changes'
                        },
                        {
                            submit:'Save changes'
                        },
                        {
                            loadtext: 'loading...',
                            type: 'select',
                            loadurl: 'GetInvariantList?idName=APPLITYPE',
                            loadtype: 'GET',
                            submit:'Save changes'
                        },
                        {
                            submit:'Save changes'
                        },
                        {
                            loadtext: 'loading...',
                            type: 'select',
                            loadurl: 'GetDeployTypeList',
                            loadtype: 'GET',
                            submit:'Save changes'
                        },
                        {
                            submit:'Save changes'
                        },
                        {
                            type: 'textarea',
                            submit:'Save changes'
                        },
                        {
                            type: 'textarea',
                            submit:'Save changes'
                        },
                        {
                            type: 'textarea',
                            submit:'Save changes'
                        }
                    ]
                });
            });
        </script>
        <p class="dttTitle">Application</p>
        <div style="width: 100%; font: 90% sans-serif">
            <table id="applicationsTable" class="display">
                <thead>
                    <tr>
                        <th><%=docService.findLabel("Application", "Application", "")%></th>
                        <th><%=docService.findLabel("Application", "System", "")%></th>
                        <th><%=docService.findLabel("Application", "subsystem", "")%></th>
                        <th><%=docService.findLabel("Application", "description", "")%></th>
                        <th><%=docService.findLabel("Application", "type", "")%></th>
                        <th><%=docService.findLabel("Application", "mavengroupid", "")%></th>
                        <th><%=docService.findLabel("Application", "deploytype", "")%></th>
                        <th><%=docService.findLabel("Application", "sort", "")%></th>
                        <th><%=docService.findLabel("Application", "svnurl", "")%></th>
                        <th><%=docService.findLabel("Application", "bugtrackerurl", "")%></th>
                        <th><%=docService.findLabel("Application", "bugtrackernewurl", "")%></th>
                    </tr>
                </thead>
                <tbody>
                </tbody>
            </table>
        </div>
        <div>
            <form id="formAddNewRow" action="#" title="Add Application" style="width:600px" method="post">
                <label for="Application" style="font-weight:bold"><%=docService.findLabelHTML("Application", "Application", "")%></label>
                <input id="Application" name="Application" style="width:150px;" 
                       class="ncdetailstext" rel="0" >
                <br><br>
                <label for="System" style="font-weight:bold"><%=docService.findLabelHTML("Application", "System", "")%></label>
                <input id="System" name="System" style="width:150px;" 
                       class="System" rel="1" value="<%=request.getAttribute("MySystem")%>" readonly>
                <label for="SubSystem" style="font-weight:bold"><%=docService.findLabelHTML("Application", "subsystem", "")%></label>
                <input id="SubSystem" name="SubSystem" style="width:100px;" 
                       class="ncdetailstext" rel="2" >
                <label for="Type" style="font-weight:bold"><%=docService.findLabelHTML("Application", "type", "")%></label>
                <%=ComboInvariantAjax(conn, "Type", "", "Type", "4", "APPLITYPE", "", "", false)%>
                <br>
                <br>
                <label for="Description" style="font-weight:bold"><%=docService.findLabelHTML("Application", "description", "")%></label>
                <input id="Description" name="Description" style="width:400px;" 
                       class="ncdetailstext" rel="3" >
                <br>
                <label for="Sort" style="font-weight:bold"><%=docService.findLabelHTML("Application", "sort", "")%></label>
                <input id="Sort" name="Sort" style="width:100px;" 
                       class="ncdetailstext" rel="7" >
                <br><br>
                <label for="MavenGroupID" style="font-weight:bold"><%=docService.findLabelHTML("Application", "mavengroupid", "")%></label>
                <input id="MavenGroupID" name="MavenGroupID" style="width:400px;" 
                       class="ncdetailstext" rel="5" >
                <br>
                <label for="DeployType" style="font-weight:bold"><%=docService.findLabelHTML("Application", "deploytype", "")%></label>
                <%=ComboDeployTypeAjax(conn, "DeployType", "", "DeployType", "6", "", "")%>
                <br><br>
                <label for="SVNURL" style="font-weight:bold"><%=docService.findLabelHTML("Application", "svnurl", "")%></label>
                <input id="SVNURL" name="SVNURL" style="width:600px;" 
                       class="ncdetailstext" rel="8" >
                <br><br>
                <label for="BugTrackerURL" style="font-weight:bold"><%=docService.findLabelHTML("Application", "bugtrackerurl", "")%></label>
                <input id="BugTrackerURL" name="BugTrackerURL" style="width:600px;" 
                       class="ncdetailstext" rel="9" >
                <br><br>
                <label for="NewBugURL" style="font-weight:bold"><%=docService.findLabelHTML("Application", "bugtrackernewurl", "")%></label>
                <input id="NewBugURL" name="NewBugURL" style="width:600px;" 
                       class="ncdetailstext" rel="10" >
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
