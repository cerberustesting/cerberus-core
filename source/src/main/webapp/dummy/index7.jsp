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
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    </head>
    <script type="text/javascript" src="../dependencies/jQuery-2.2.3/jquery-2.2.3.min.js"></script>
    <%
        long MAX_DELAY = 60000;
        long DEFAULT_DELAY = 1000;

        long timeToWait = DEFAULT_DELAY;
        if (request.getParameter("delay") != null) {
            timeToWait = Long.valueOf(request.getParameter("delay").toString());
        }
        if (timeToWait > MAX_DELAY) {
            timeToWait = MAX_DELAY;
        }
    %>

    <body>
        <h1>Page test 7 - open URL after delay</h1>
        <br>
        <br>
        <button onclick="myFunction()">Open a new URL after a given delay</button>
        Delay will be <%=timeToWait%>

    </body>
    <script>

        function sleep(ms) {
            return new Promise(resolve => setTimeout(resolve, ms));
        }

        function myFunction() {
            console.info("delay");
            sleep(<%=timeToWait%>).then(() => {
                console.log('World!');
                window.open("index1.jsp", "_self");

            });
        }

    </script>
</html>
