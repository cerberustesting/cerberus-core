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
        <script type="text/javascript" src="js/pages/project.js"></script>
    </head>
    <body>
        <%@ include file="include/function.jsp" %>
        <%@ include file="include/header2.jsp" %>
        <%
            IDocumentationService docService = appContext.getBean(IDocumentationService.class);
        %>
        <div class="container center" id="page-layout">
            <h1 class="page-title-line">Project</h1>
            <div id="project" class="well">
                <table id="projectsTable" class="table table-hover display">
                    <thead>
                        <tr>
                            <th id="projectFirstColumnHeader">Actions</th>
                            <th>Project</th>
                            <th>Code</th>
                            <th>Description</th>
                            <th>Active</th>
                            <th>Created</th>
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
