<%--

    Cerberus Copyright (C) 2013 - 2017 cerberustesting
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
    Created on : 31 Marsh 2020
    Author     : cerberus
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%@ include file="include/global/dependenciesInclusions.html" %>
        <script type="text/javascript" src="dependencies/Moment-2.24.0/moment.min.js"></script>
        <script type="text/javascript" src="dependencies/Chart.js-2.9.3/Chart.min.js"></script>
        <script type="text/javascript" src="js/pages/ReportingExecutionOverTime.js"></script>
        <script type="text/javascript" src="js/transversalobject/TestCaseExecutionQueue.js"></script>
        <script type="text/javascript" src="js/transversalobject/TestCase.js"></script>
        <title id="pageTitle">Campaign Reporting</title>
    </head>
    <body>
        <%@ include file="include/global/header.html" %>
        <div class="container-fluid center" id="page-layout">
            <%@ include file="include/global/messagesArea.html"%>
            <%@ include file="include/transversalobject/TestCaseExecutionQueue.html"%>
            <%@ include file="include/transversalobject/TestCase.html"%>
            <%@ include file="include/pages/testcampaign/viewStatcampaign.html"%>

            <h1 class="page-title-line" id="title">Execution Over Time</h1>

            <div class="" id="FiltersPanel">
                <div class="panel panel-default">
                    <div class="panel-heading card">
                        <span class="fa fa-tag fa-fw"></span>
                        <label id="filters">Filters</label>
                    </div>
                    <div class="panel-body">
                        <label for="selectTest">Test Folder :</label>
                        <div class="row" id="tagFilter">
                            <div class="input-group">
                                <select class="form-control col-lg-7" name="Tag" id="selectTag"></select>
                                <div class="input-group-btn">
                                    <button type="button" class="btn btn-default" style="margin-left: 10px;min-height: " id="loadbutton" onclick="loadPerfGraph()">Load</button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="row" id="ReportByFunctionPanel">
                <div class="col-lg-12">
                    <div class="panel panel-default">
                        <div class="panel-heading card" data-toggle="collapse" data-target="#perfChart">
                            <span class="fa fa-bar-chart fa-fw"></span>
                            <label id="reportFunction">Performance Graph</label>
                            <span class="toggle glyphicon glyphicon-chevron-right pull-right"></span>
                        </div>
                        <div class="panel-body collapse in" id="perfChart">
                            <div class="row">
                                <div class="col-xs-12" id="ChartRequests">
                                    <canvas id="canvasRequests" style="display: block;" class=""></canvas>
                                </div>
                            </div>
                            <div class="row">
                                <div class="col-xs-12" id="ChartSize">
                                    <canvas id="canvasSize" style="display: block; width: 1121px; height: 560px;" width="1121" height="560" class=""></canvas>
                                </div>
                            </div>
                            <div class="row">
                                <div class="col-xs-12" id="ChartTime">
                                    <canvas id="canvasTime" style="display: block; width: 1121px; height: 560px;" width="1121" height="560" class=""></canvas>
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
