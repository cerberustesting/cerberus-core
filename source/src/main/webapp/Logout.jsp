<%--

    Cerberus Copyright (C) 2013 - 2025 cerberustesting
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
<%@page import="org.springframework.web.context.support.WebApplicationContextUtils"%>
<%@page import="org.springframework.context.ApplicationContext"%>
<%@page import="org.cerberus.core.session.SessionCounter"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%@ include file="include/global/dependenciesInclusions.html" %>
        <link rel="stylesheet" type="text/css" href="css/global/crb_style.css">

        <link rel="icon" type="image/png" href="images/favicon.ico.png"><!-- Major Browsers -->
        <!--[if IE]><link rel="shortcut icon" type="image/x-icon" href="images/favicon.ico"/><![endif]--><!-- Internet Explorer-->

        <script type='text/javascript' src='js/global/global.js'></script>
        <script type='text/javascript' src='js/pages/Logout.js'></script>
        <META HTTP-EQUIV="refresh" CONTENT="1;URL=./">
        <title>Logout</title>
    </head>
    <body style="background-color: #fff">
        <script type="text/javascript">
            envTuning("<%=System.getProperty("org.cerberus.environment")%>");
            sessionStorage.clear();
            // JSESSIONID cookie to remove.
        </script>
        <div align="center" style="padding-top: 12%;">
            <h1>Logout Successfully</h1>
        </div>
    </body>
</html>
<%
    ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
    SessionCounter sc = appContext.getBean(SessionCounter.class);
    if (request.getUserPrincipal() != null) {
        sc.identificateUser(request.getSession().getId(), request.getUserPrincipal().getName());
        sc.destroyUser(request.getSession().getId());
    }
    request.getSession().invalidate();
%>
