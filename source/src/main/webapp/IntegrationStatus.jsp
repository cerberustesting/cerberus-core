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
<%-- 
    Document   : ReportingExecutionByTag2
    Created on : 3 août 2015, 11:02:49
    Author     : cerberus
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%@ include file="include/global/dependenciesInclusions.html" %>
        <script type="text/javascript" src="js/pages/IntegrationStatus.js"></script>
        <title id="pageTitle">Integration Status</title>
    </head>
    <body>
        <%@ include file="include/global/header.html" %>
        <div class="container-fluid center" id="page-layout">
            <%@ include file="include/global/messagesArea.html"%>
            <h1 class="page-title-line" id="title">Integration Status</h1>
            <div class="row">
                <div class="col-lg-12" id="FiltersPanel">
                    <div class="panel panel-default">
                        <div class="panel-heading card" data-toggle="collapse" data-target="#DeployHisto">
                            <span class="glyphicon glyphicon-list"></span>
                            <label id="reportChanges">Last deploy Operations</label>
                            <span class="toggle glyphicon glyphicon-chevron-right pull-right"></span>
                        </div>
                        <div class="panel-body collapse in" id="DeployHisto">
                            <div class="row">
                                <div class="col-lg-12" id="filterContainer">
                                    <div class="row" id="tagFilter">
                                        <div class="col-sm-5">
                                            <label for="selectEngGp" id="selectEngGpLabel">Environment Group :</label>
                                            <select class="form-control" name="selectEngGp" id="selectEngGp"></select>
                                        </div>
                                        <div class="col-sm-5">
                                            <label for="selectSince" id="selectSinceLabel">Since (days) :</label>
                                            <select class="form-control" name="selectSince" id="selectSince"></select>
                                        </div>
                                        <div class="col-xs-2 marginTop20">
                                            <button type="button" class="btn btn-default" style="margin-left: 10px;" id="loadLastModifbutton">Load</button>
                                        </div>
                                    </div>
                                    <div class="row" id="tagFilter">
                                        <table class="table table-bordered table-hover nomarginbottom dataTable" id="histoTable">
                                            <thead id="histoTableHead">
                                            </thead>
                                            <tbody id="histoTableBody">
                                            </tbody>
                                        </table>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="col-lg-6 col-md-9" id="ReportByStatusPanel">
                    <div class="panel panel-default">
                        <div class="panel-heading card" data-toggle="collapse" data-target="#EnvStatus">
                            <span class="glyphicon glyphicon-list"></span>
                            <label id="reportStatus">Environment Status</label>
                            <span class="toggle glyphicon glyphicon-chevron-right pull-right"></span>
                        </div>
                        <div class="panel-body collapse in" id="EnvStatus">
                            <div class="row">
                                <div class="col-xs-12" id="EnvByBuildRevisionTable">
                                    <table class="table table-bordered table-hover nomarginbottom dataTable" id="envTable">
                                        <thead>
                                            <tr>
                                                <th class="text-center" id="systemHeader" name="systemHeader">System</th>
                                                <th class="text-center" id="buildHeader" name="buildHeader">Build</th>
                                                <th class="text-center" id="revisionHeader" name="revisionHeader">Revision</th>
                                                <th class="text-center" id="devHeader" name="devHeader">DEV</th>
                                                <th class="text-center" id="qaHeader" name="qaHeader">QA</th>
                                                <th class="text-center" id="uatHeader" name="uatHeader">UAT</th>
                                                <th class="text-center" id="prodHeader" name="prodHeader">PROD</th>
                                            </tr>
                                        </thead>
                                        <tbody id="envTableBody">
                                        </tbody>
                                    </table>
                                </div>
                            </div>
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
