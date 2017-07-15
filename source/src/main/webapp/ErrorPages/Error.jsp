<%--

    Cerberus Copyright (C) 2013 - 2017 cerberustesting
    DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.

    This file is part of Cerberus.

    Cerberus is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Cerberus is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Cerberus.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%-- 
    Document   : Error
    Created on : 19 août 2014, 07:19:16
    Author     : bcivel
--%>
<% Date DatePageStart = new Date();
   %>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link rel="stylesheet" type="text/css" href="css/global/crb_style.css">
        <link rel="shortcut icon" type="image/x-icon" href="images/favicon.ico">
        <title>Error</title>
    </head>
    <body>
        <%@ include file="../include/global/function.jsp" %>
        <%
            if ((request.getParameter("error") != null) && (request.getParameter("error").equals("403"))) {
        %>
        <h1>Not Authorised Resource...</h1>
        <h3> You don't have enough privilege to open this page, Please ask your Cerberus administrator to grant the access.</h3>
        <%
        } else {
        %>
        <h1>Oops...</h1>
        <h3>Sorry but an unexpected error occurred.</h3>
        <%
            }
        %>
        <br><br>
        <h4>Error Code : <%=request.getParameter("error")%></h4>
        <h4><%=request.getAttribute("javax.servlet.error.message")%></h4>
        <br><%
            out.print(display_footer(DatePageStart));
        %>
    </body>
</html>
