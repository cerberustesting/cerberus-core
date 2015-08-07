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
<% Date DatePageStart = new Date();
%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <title>Projects</title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%@ include file="include/dependenciesInclusions.html" %>
        <script type="text/javascript" src="js/pages/Project.js"></script>
    </head>
    <body>
        <%@ include file="include/function.jsp" %>
        <%@ include file="include/header.jsp" %>
        <%
            IDocumentationService docService = appContext.getBean(IDocumentationService.class);
        %>
        <div class="container center" id="page-layout">
            <%@ include file="include/messagesArea.html"%>
            <%@ include file="include/utils/modal-confirmation.html"%>
            <%@ include file="include/project/addProject.html"%> 
            <%@ include file="include/project/editProject.html"%> 

            <h1 class="page-title-line">Project</h1>
            <div id="project" class="well">
                <table id="projectsTable" class="table table-hover display" name="projectsTable">
                    <thead>
                        <tr>
                            <th id="action">Actions</th>
                            <th id="idproject"><%=docService.findLabelHTML("Project","idProject","table", "en")%></th>
                            <th id="code"><%=docService.findLabelHTML("Project","code","table", "en")%></th>
                            <th id="description"><%=docService.findLabelHTML("Project","Description","table", "en")%></th>
                            <th id="active"><%=docService.findLabelHTML("Project","active","table", "en")%></th>
                            <th id="dateCreation"><%=docService.findLabelHTML("Project","dateCreation","table", "en")%></th>
                        </tr>
                    </thead>
                </table>
                <div class="marginBottom20"></div>
            </div>
            <br><%
                out.print(display_footer(DatePageStart));
            %>
        </div>
    </body>
</html>
