<%--
  ~ Cerberus Copyright (C) 2013 vertigo17
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
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  ~ GNU General Public License for more details.
  ~
  ~ You should have received a copy of the GNU General Public License
  ~ along with Cerberus. If not, see <http://www.gnu.org/licenses/>.
--%>
<%@page import="java.util.Date"%>
<%@page import="org.springframework.web.context.support.WebApplicationContextUtils"%>
<%@page import="org.springframework.context.ApplicationContext"%>
<%@page import="org.springframework.web.context.WebApplicationContext"%>
<%@page import="org.cerberus.entity.Invariant"%>
<%@page import="java.util.List"%>
<%@page import="org.cerberus.service.IInvariantService"%>
<%@page import="org.cerberus.service.IDatabaseVersioningService"%>
<% Date DatePageStart = new Date();%>

<%@page contentType="text/html" pageEncoding="UTF-8"%><!DOCTYPE HTML>
<!DOCTYPE html>
<html>
    <head>
        <meta content="text/html; charset=UTF-8" http-equiv="content-type">
        <title>Cerberus Homepage</title>
        <%@ include file="include/dependenciesInclusions.html" %>
        <script type="text/javascript" src="js/pages/Homepage.js"></script>
    </head>
    <body>
        <%@ include file="include/header.html"%>

        <%
            ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());

            IDatabaseVersioningService DatabaseVersioningService = appContext.getBean(IDatabaseVersioningService.class);
            if (!(DatabaseVersioningService.isDatabaseUptodate()) && request.isUserInRole("Administrator")) {%>
        <script>
            var r = confirm("WARNING : Database Not Uptodate >> Redirect to the DatabaseMaintenance page ?");
            if (r == true)
            {
                location.href = './DatabaseMaintenance.jsp';
            }
        </script>

        <% }
        %>
        <style>
             
             .DataTables_sort_wrapper { font-size:9px }
             
         </style>
        <div class="container-fluid center" id="page-layout">

            <h1 class="page-title-line" id="title">Welcome to Cerberus Application</h1>
            <div id="homeTableDiv" class="well">
                <table id="homePageTable" class="table table-hover display" name="homePageTable"></table>
                <div class="marginBottom20"></div>
            </div>
            <footer class="footer">
                <div class="container-fluid" id="footer"></div>
            </footer>
        </div>
        <br>
    </body>
</html>
