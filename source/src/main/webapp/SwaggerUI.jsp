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
        <meta content="text/html; charset=UTF-8" http-equiv="content-type">
        <%@ include file="include/global/dependenciesInclusions.html" %>
        <script type="text/javascript" src="js/pages/SwaggerUI.js"></script>
        <link rel="stylesheet" type="text/css" href="dependencies/SwaggerUI-5.26.0/swagger-ui.css" />
        <link rel="stylesheet" type="text/css" href="dependencies/SwaggerUI-5.26.0/index.css" />
        <style>
            .swagger-ui .topbar { display: none !important; }
            .swagger-ui .information-container { display: none !important; }
            .swagger-ui .scheme-container { display: none !important; }
            .swagger-ui .models { display: none !important; }
        </style>

        <title id="pageTitle">Swagger UI</title>
    </head>
    <body>
        <%@ include file="include/global/header.html"%>
        <div class="container-fluid center" id="page-layout">
            <%@ include file="include/global/messagesArea.html"%>
            <%@ include file="include/utils/modal-confirmation.html"%>
            <%@ include file="include/pages/test/editTest.html" %>
            <%@ include file="include/pages/test/addTest.html" %>

            <h1 class="page-title-line" id="title">Swagger UI</h1>
            <div class="panel panel-default">
                <div class="panel-body" id="testList">
                    <div id="swagger-ui"></div>
                    <script src="dependencies/SwaggerUI-5.26.0/swagger-ui-bundle.js" charset="UTF-8"> </script>
                    <script src="dependencies/SwaggerUI-5.26.0/swagger-ui-standalone-preset.js" charset="UTF-8"> </script>
                    <script src="dependencies/SwaggerUI-5.26.0/swagger-initializer.js" charset="UTF-8"> </script>
                </div>
            </div>
            <footer class="footer">
                <div class="container-fluid" id="footer"></div>
            </footer>
        </div>
    </body>
</html>