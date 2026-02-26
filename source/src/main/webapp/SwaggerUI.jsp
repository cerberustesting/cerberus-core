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
<html class="h-full">
    <head>
        <meta name="active-menu" content="developer">
        <meta name="active-submenu" content="SwaggerUI.jsp">
        <meta content="text/html; charset=UTF-8" http-equiv="content-type">
        <%@ include file="include/global/dependenciesInclusions.html" %>
        <script type="text/javascript" src="js/pages/SwaggerUI.js?v=${appVersion}"></script>
        <link rel="stylesheet" type="text/css" href="dependencies/SwaggerUI-5.26.0/swagger-ui.css" />
        <link rel="stylesheet" type="text/css" href="dependencies/SwaggerUI-5.26.0/index.css" />
        <style>
            .swagger-ui .topbar { display: none !important; }
            .swagger-ui .information-container { display: none !important; }
            .swagger-ui .scheme-container { display: none !important; }
            .swagger-ui .models { display: none !important; }
        </style>

        <title id="pageTitle">Swagger API</title>
    </head>
    <body x-data x-cloak class="crb_body">
        <jsp:include page="include/global/header2.html"/>
        <jsp:include page="include/global/modalInclusions.jsp"/>
        <main class="crb_main" :class="$store.sidebar.expanded ? 'crb_main_sidebar-expanded' : 'crb_main_sidebar-collapsed'">
            <%@ include file="include/global/messagesArea.html"%>
            <%@ include file="include/utils/modal-confirmation.html"%>
            <%@ include file="include/pages/test/editTest.html" %>
            <%@ include file="include/pages/test/addTest.html" %>

            <h1 class="page-title-line" id="title">Swagger API</h1>
            <div class="crb_card">
                <div id="testList">
                    <div id="swagger-ui"></div>
                    <script src="dependencies/SwaggerUI-5.26.0/swagger-ui-bundle.js" charset="UTF-8"> </script>
                    <script src="dependencies/SwaggerUI-5.26.0/swagger-ui-standalone-preset.js" charset="UTF-8"> </script>
                    <script src="dependencies/SwaggerUI-5.26.0/swagger-initializer.js" charset="UTF-8"> </script>
                </div>
            </div>
            <footer class="footer">
                <div class="container-fluid" id="footer"></div>
            </footer>
        </main>
    </body>
</html>