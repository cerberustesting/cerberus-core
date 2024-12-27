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
        <%@ include file="include/global/dependenciesInclusions.html" %>
        <title>Cerberus Under Maintenance</title>
        <style>
            #logo-cerberus {
                width: 100px;
                margin: 2em 2em 2em 2em;
            }

            #reloadButton {
                margin: 1em 0 2em 0;
            }

            #page-layout {
                position: relative;
            }

            footer {
                position: absolute;
                bottom: 0;
            }
        </style>
    </head>
    <body>
        <div class="container-fluid center" id="">
            <%@ include file="include/global/messagesArea.html"%>
            <h1 class="page-title-line" style="text-align: center" id="title">Cerberus is down for maintenance</h1>
            <table style="width: 100%"><tbody ><tr><td style="text-align: center"><a href="."><img style="align-content: center;" src="images/Logo-cerberus_250.png" id="logo-cerberus"></a></td></tr></tbody></table>
            
            <h2 style="text-align: center" >Please come back later</h2>

            <table style="width: 100%"><tbody ><tr><td style="text-align: center">            <button name="reloadButton" id="reloadButton" type="button" class="btn btn-default">
                Reload
            </button>
</td></tr></tbody></table>
            <p>If any issue, contact your Cerberus administrator at : <a id="adminMailLink" href=""></a></p>

            <footer class="footer">
                <div class="container-fluid" id="footer"></div>
            </footer>
        </div>
    </body>
</html>
