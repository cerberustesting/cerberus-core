<%--
  ~ Cerberus  Copyright (C) 2013  vertigo17
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
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~ GNU General Public License for more details.
  ~
  ~ You should have received a copy of the GNU General Public License
  ~ along with Cerberus.  If not, see <http://www.gnu.org/licenses/>.
--%>
<%@page import="java.util.Date"%>

<% Date DatePageStart = new Date();%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%@ include file="include/dependenciesInclusions.html" %>
        <title>Parameter</title>
        <script type="text/javascript" src="js/pages/Parameter.js"></script>
    </head>
    <body>
        <%@ include file="include/header.html"%>
        <div id="homeTableDiv" class="panel panel-default">
            <div class="panel-heading card" data-toggle="collapse" data-target="#applicationPanel">
                <span class="toggle glyphicon glyphicon-chevron-right pull-right"></span>
                <span class="fa fa-retweet fa-fw"></span>
                <label id="Parameter">Parameter</label>
            </div>
            <div class="panel-body collapse in" id="applicationPanel">
                <table id="parameterTable" class="table table-bordered table-hover display" name="homePageTable"></table>
                <div class="marginBottom20"></div>
            </div>
        </div>
        <br>

        <footer class="footer">
            <div class="container-fluid" id="footer"></div>
        </footer>
    </body>
</html>
