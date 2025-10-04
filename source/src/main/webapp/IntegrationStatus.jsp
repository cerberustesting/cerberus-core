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
<html class="h-full">
    <head>
        <meta name="active-menu" content="integration">
        <meta name="active-submenu" content="IntegrationStatus.jsp">
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%@ include file="include/global/dependenciesInclusions.html" %>
        <script type="text/javascript" src="js/pages/IntegrationStatus.js"></script>
        <title id="pageTitle">Integration Status</title>
    </head>
    <body x-data x-cloak class="crb_body">
        <jsp:include page="include/global/header2.html"/>
        <main class="crb_main" :class="$store.sidebar.expanded ? 'crb_main_sidebar-expanded' : 'crb_main_sidebar-collapsed'">
            <%@ include file="include/global/messagesArea.html"%>
            <h1 class="page-title-line" id="title">Integration Status</h1>
            <div class="row">
                <div class="col-lg-12" id="FiltersPanel">
                    <div class="crb_card">
                        <div>
                            <span class="glyphicon glyphicon-list"></span>
                            <label id="reportChanges">Last deploy Operations</label>
                        </div>
                        <div id="DeployHisto">
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
                                        <table class="table table-hover nomarginbottom dataTable" id="histoTable">
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
                    <div class="crb_card">
                        <div>
                            <span class="glyphicon glyphicon-list"></span>
                            <label id="reportStatus">Environment Status</label>
                        </div>
                        <div id="EnvStatus">
                            <div class="row">
                                <div class="col-xs-12" id="EnvByBuildRevisionTable">
                                    <table class="table table-hover nomarginbottom dataTable" id="envTable">
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
        </main>
    </body>
</html>
