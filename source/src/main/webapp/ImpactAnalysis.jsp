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
        <script type="text/javascript" src="dependencies/Tinymce-6.7.0/tinymce.min.js"></script>
        <script type="text/javascript" src="js/pages/ImpactAnalysis.js"></script>
        <script type="text/javascript" src="js/transversalobject/TestCase.js"></script>
        <title id="pageTitle">Impact Analysis</title>
    </head>
    <body>

        <%@ include file="include/global/header.html" %>
        <div class="container-fluid center" id="page-layout">
            <%@ include file="include/global/messagesArea.html"%>
            <%@ include file="include/utils/modal-confirmation.html"%>
            <%@ include file="include/transversalobject/TestCase.html"%>

            <h1 class="page-title-line" id="title">Impact Analysis</h1>

            <div class="row">
                <div class="col-lg-12" id="FiltersPanel">
                    <div class="panel panel-default">
                        <div class="panel-body">
                            <label class="input-group">
                                <input class="form-control input-md" id="searchQ" name="searchQ" placeholder="Type any references here .... (xpath, variable, property,...)" onkeydown="searchKeyDown()">
                                <span class="input-group-btn">
                                    <button type="button" class="btn btn-default input-md" id="btnEmpty" onclick="emptySearch()" name="btnEmpty">X</button>
                                    <button type="button" class="btn btn-default input-md" id="btnSearch" onclick="loadAllTables()" name="btnLoad">Search</button>
                                </span>
                            </label>
                        </div>
                    </div>
                </div>
            </div>

            <ul id="tabsIA" class="nav nav-tabs" data-tabs="tabs">
                <li class="active"><a data-toggle="tab" href="#tabTestCases" id="headerTabTestCases" name="tabTC">Test Cases</a>
                </li>
                <li style="display: none"><a data-toggle="tab" href="#tabDataLib" id="headerTabDataLib" name="tabDL">Data Libraries</a></li>
                <li style="display: none"><a data-toggle="tab" href="#tabApplications" id="headerTabApplications" name="tabAPP">Applications</a></li>
                <li style="display: none"><a data-toggle="tab" href="#tabServices" id="headerTabServices" name="tabSRV">Service Libraries</a></li>
            </ul>

            <div class="tab-content">
                <div class="center tab-pane fade in active" id="tabTestCases">
                    <div class="panel panel-default">
                        <div class="panel-body" id="tcList">
                            <table id="tcTable" class="table table-bordered table-hover display" name="tcTable"></table>
                        </div>
                    </div>
                </div>
                <div class="center tab-pane fade in" id="tabDataLib">
                    <div class="panel panel-default">
                        <div class="panel-body" id="dlList">
                            <table id="dlTable" class="table table-bordered table-hover display" name="dlTable"></table>
                        </div>
                    </div>
                </div>
                <div class="center tab-pane fade in" id="tabApplications">
                    <div class="panel panel-default">
                        <div class="panel-body" id="appList">
                            <table id="appTable" class="table table-bordered table-hover display" name="appTable"></table>
                        </div>
                    </div>
                </div>
                <div class="center tab-pane fade in" id="tabServices">
                    <div class="panel panel-default">
                        <div class="panel-body" id="srvList">
                            <table id="srvTable" class="table table-bordered table-hover display" name="srvTable"></table>
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
