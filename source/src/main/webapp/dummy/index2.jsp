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
        <title>Sélecteur: Businessobject</title>
    </head>
    <body>
        <h1>Hello World!</h1>
        <h2 data-cerberus="secondIndexPageOfTest">Second page of test index</h2>
        <span data-cerberus="notVisibleElement" style="display: none;">Element not visible on page</span>
        <iframe src="frame.html"></iframe>
        <button data-cerberus="closePopupWindow" onclick="javascript:window.close();">Close Popup Window</button>
        <br><br>
        <input name="selectedValue2" id="selectedValue2" value="<%=request.getParameter("selectedValue2")%>">
    </body>
</html>
