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
        <title>Hello World</title>
    </head>
    <body>
        <h1>Hello World!</h1>
        <h2>Third Page of test index</h2>
        <%
            long MAX_DELAY = 60000;
            long DEFAULT_DELAY = 1000;
            
            long timeToWait = DEFAULT_DELAY;
            if (request.getParameter("delay") != null) {
                timeToWait = Long.valueOf(request.getParameter("delay").toString());
            }
            if (timeToWait>MAX_DELAY) {timeToWait=MAX_DELAY;}
            Thread.sleep(timeToWait);
        %>
        Waited : <%=timeToWait%> ms (delay parameter=<%=request.getParameter("delay")%> | MAX_DELAY=<%=MAX_DELAY%> | DEFAULT_DELAY=<%=DEFAULT_DELAY%>)
        <br><br><br><br><br><br><br><br><br>
        <br><br><br><br><br><br><br><br><br>
        <br><br><br><br><br><br><br><br><br>
        <br><br><br><br><br><br><br><br><br>
        <br><br><br><br><br><br><br><br><br>
        <br><br><br><br><br><br><br><br><br>
        <br><br><br><br><br><br><br><br><br>
        <form name="selectFormulary2" action="index2.jsp">
            <p>Below is part to test Input and validate form</p>
            <input type="text" name="selectedValue2" id="selectedValue2">
            <input id="selectedValue2Submit" type="submit" value="Submit">
        </form>
    </body>
</html>
