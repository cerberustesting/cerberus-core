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
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <%@ include file="include/dependenciesInclusions.html" %>
    <title>Execution in Queue</title>
    <script type="text/javascript" src="js/pages/ExecutionPending.js"></script>
</head>
<body>
<%@ include file="include/header.html" %>
<div class="container-fluid center" id="page-layout">
    <%@ include file="include/messagesArea.html"%>
    <%@ include file="include/utils/modal-confirmation.html"%>

    <h1 class="page-title-line" id="title">Execution in Queue</h1>
    <div class="panel panel-default">
        <div class="panel-heading" id="executionListLabel">
            <span class="glyphicon glyphicon-list"></span>
            Execution in Queue
        </div>
        <div class="panel-body" id="executionList">
            <table id="executionsTable" class="table table-bordered table-hover display" name="executionsTable"></table>
            <div class="marginBottom20"></div>
        </div>
    </div>
    <footer class="footer">
        <div class="container-fluid" id="footer"></div>
    </footer>
</div>
</body>
</html>