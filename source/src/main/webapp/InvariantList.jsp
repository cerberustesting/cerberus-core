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
        <title>Invariant</title>
        <script type="text/javascript" src="js/pages/InvariantList.js"></script>
        <script type="text/javascript" src="js/transversalobject/Invariant.js"></script>
    </head>
    <body>
        <%@ include file="include/global/header.html" %>
        <div class="container-fluid center" id="page-layout">
            <%@ include file="include/global/messagesArea.html"%>
            <%@ include file="include/utils/modal-confirmation.html"%>
            <%@ include file="include/transversalobject/Invariant.html"%>

            <h1 class="page-title-line" id="title">Invariants</h1>

            <ul class="nav nav-tabs">
                <li class="active"><a data-toggle="tab" href="#public">Public</a></li>
                <li><a data-toggle="tab" href="#private">Private</a></li>
            </ul>

            <div class="tab-content">
                <div id="public" class="tab-pane fade in active">
                    <div class="panel panel-default">
                        <div class="panel-body" id="invariantList">
                            <table id="invariantsTable" class="table table-bordered table-hover display" name="invariantsTable"></table>
                            <div class="marginBottom20"></div>
                        </div>
                    </div>
                </div>
                <div id="private" class="tab-pane fade">
                    <div class="panel panel-default">
                        <div class="panel-body" id="invariantPrivateList">
                            <table id="invariantsPrivateTable" class="table table-bordered table-hover display" name="invariantsPrivateTable"></table>
                            <div class="marginBottom20"></div>
                        </div>
                    </div>
                </div>
            </div>
            
            <footer class="footer">
                <div class="container-fluid" id="footer"></div>
            </footer>
            
        </div>
    </body>
</html>