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
<%-- 
    Document   : Error
    Created on : 19 août 2014, 07:19:16
    Author     : bcivel
--%>
<%@page import="java.util.Date"%>
<%@page import="java.text.DateFormat"%>
<%@page import="org.cerberus.core.version.Infos"%>
<%!
    Date DatePageStart = new Date();

    String display_footer(Date DatePageStart) {
        Date mydate = new Date();
        long Duration = mydate.getTime() - DatePageStart.getTime();
        String footer = "Page started generating on <b><span id=\"foot-loaddatetime\">" + DatePageStart.toString() + "</span></b>"
                + " by <b><span id=\"foot-projectname\">" + Infos.getInstance().getProjectName() + "</span></b>"
                + " <b><span id=\"foot-version\">" + Infos.getInstance().getProjectVersion() + "</span></b>"
                + " in <b><span id=\"foot-env\">" + System.getProperty("org.cerberus.environment") + "</span></b>"
                + " and took <b><span id=\"foot-duration\">" + Duration + "</span>ms</b>"
                + " - Open a bug or ask for any new feature <a target=\"_blank\"  href=\"https://github.com/vertigo17/Cerberus/issues/new?body=Cerberus%20Version%20:%20" + Infos.getInstance().getProjectVersion() + "\">here</a>.";
        return footer;
    }

%>


<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link rel="stylesheet" type="text/css" href="css/global/crb_style.css">

        <link rel="icon" type="image/png" href="images/favicon.ico.png"><!-- Major Browsers -->
        <!--[if IE]><link rel="shortcut icon" type="image/x-icon" href="images/favicon.ico"/><![endif]--><!-- Internet Explorer-->

        <title>Error</title>
    </head>
    <body>
        <br><br>
        <h1 style="text-align: center">Oops...</h1>
        <br><br>
        <h2 style="text-align: center">You don't have any authority to access to that Cerberus page (yet).</h2>
        <h2 style="text-align: center">Ask your administrator to add you the necessary roles.</h2>
        <br><br>
        <br>
        <h4 style="text-align: center">You can try to :</h4><br>
        <h4 style="text-align: center"><a href="./Logout.jsp">Logout</a></h4><br>
        <h4 style="text-align: center"><a href="./">Back to homepage</a></h4><br>
        <br>
        <br>
        <div style="text-align: center"><%
                out.print(display_footer(DatePageStart));%>            
        </div>
    </body>
</html>
