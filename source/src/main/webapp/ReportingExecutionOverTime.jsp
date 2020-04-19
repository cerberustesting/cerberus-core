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
        <script type="text/javascript" src="dependencies/Moment-2.24.0/locale/fr.js"></script>
        <script type="text/javascript" src="dependencies/Chart.js-2.9.3/Chart.min.js"></script>
        <script type="text/javascript" src="dependencies/Bootstrap-datetimepicker-4.17.47/bootstrap-datetimepicker.min.js"></script>
        <script type="text/javascript" src="js/pages/ReportingExecutionOverTime.js"></script>
        <title id="pageTitle">Execution Over Time</title>
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
                    <div class="panel-body" id="otFilterPanel">

                        <div class="">

                            <div class="row">
                                <div class='col-md-6'>
                                    <div class="form-group">
                                        <label for="testSelect">Test Folder</label>
                                        <select class="form-control" id="testSelect"></select>
                                    </div>
                                </div>
                                <div class='col-md-6'>
                                    <div class="form-group">
                                        <label for="testCaseSelect">Test Case</label>
                                        <select class="form-control" id="testCaseSelect"></select>
                                    </div>
                                </div>
                            </div>

                            <div class="row">
                                <div class='col-sm-6 col-md-4'>
                                    <div class="form-group">
                                        <label for="frompicker">From</label>
                                        <div class='input-group date' id='frompicker'>
                                            <input type='text' class="form-control" />
                                            <span class="input-group-addon">
                                                <span class="glyphicon glyphicon-calendar"></span>
                                            </span>
                                        </div>
                                    </div>
                                </div>
                                <div class='col-sm-6 col-md-4'>
                                    <div class="form-group">
                                        <label for="topicker">To</label>
                                        <div class='input-group date' id='topicker'>
                                            <input type='text' class="form-control" />
                                            <span class="input-group-addon">
                                                <span class="glyphicon glyphicon-calendar"></span>
                                            </span>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <div class="row">
                                <div class='col-md-4'>
                                    <div class="form-group">
                                        <label for="countrySelect">Country</label>
                                        <select class="multiselectelement form-control" multiple="multiple" id="countrySelect"></select>
                                    </div>
                                </div>
                                <div class='col-md-4'>
                                    <div class="form-group">
                                        <label for="envSelect">Environment</label>
                                        <select class="multiselectelement form-control" multiple="multiple" id="envSelect"></select>
                                    </div>
                                </div>
                                <div class='col-md-4'>
                                    <div class="form-group">
                                        <label for="robotSelect">Robot Decli</label>
                                        <select class="multiselectelement form-control" multiple="multiple" id="robotSelect"></select>
                                    </div>
                                </div>
                            </div>

                            <div class="row">
                                <div class='col-md-3'>
                                    <div class="form-group">
                                        <label for="units">Units</label>
                                        <select class="multiselectelement form-control" multiple="multiple" id="units"></select>
                                    </div>
                                </div>
                                <div class='col-md-4'>
                                    <div class="form-group">
                                        <label for="parties">Third Parties</label>
                                        <select class="multiselectelement form-control" multiple="multiple" id="parties"></select>
                                    </div>
                                </div>
                                <div class='col-md-3'>
                                    <div class="form-group">
                                        <label for="types">Media Types</label>
                                        <select class="multiselectelement form-control" multiple="multiple" id="types"></select>
                                    </div>
                                </div>
                                <div class='col-md-2'>
                                    <div class="input-group-btn ">
                                        <button type="button" class="btn btn-default marginTop20" style="margin-left: 10px;min-height: " id="loadbutton" onclick="loadPerfGraph(true)">Load</button>
                                    </div>
                                </div>
                            </div>

                        </div>

                    </div>
                </div>
            </div>
            <div class="row" id="ReportByFunctionPanel">
                <div class="col-lg-12">
                    <div id="panelTestStat" class="panel panel-default" style="display: none">
                        <div class="panel-heading card" data-toggle="collapse" data-target="#perfChart0">
                            <span class="fa fa-bar-chart fa-fw"></span>
                            <label id="lblTestStat">TestCase Stats</label>
                            <span class="toggle glyphicon glyphicon-chevron-right pull-right"></span>
                        </div>
                        <div class="panel-body collapse in" id="perfChart0">
                            <div class="row">
                                <div class="col-xs-12" id="ChartTestStat">
                                    <canvas id="canvasTestStat" style="display: block;" class=""></canvas>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div id="panelPerfRequests" class="panel panel-default" style="display: none">
                        <div class="panel-heading card" data-toggle="collapse" data-target="#perfChart1">
                            <span class="fa fa-bar-chart fa-fw"></span>
                            <label id="lblPerfRequests">Performance Graph - Requests</label>
                            <span class="toggle glyphicon glyphicon-chevron-right pull-right"></span>
                        </div>
                        <div class="panel-body collapse in" id="perfChart1">
                            <div class="row">
                                <div class="col-xs-12" id="ChartRequests">
                                    <canvas id="canvasRequests" style="display: block;" class=""></canvas>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div id="panelPerfSize" class="panel panel-default" style="display: none">
                        <div class="panel-heading card" data-toggle="collapse" data-target="#perfChart2">
                            <span class="fa fa-bar-chart fa-fw"></span>
                            <label id="lblPerfSize">Performance Graph - Size</label>
                            <span class="toggle glyphicon glyphicon-chevron-right pull-right"></span>
                        </div>
                        <div class="panel-body collapse in" id="perfChart2">
                            <div class="row">
                                <div class="col-xs-12" id="ChartSize">
                                    <canvas id="canvasSize" style="display: block; width: 1121px; height: 560px;" width="1121" height="560" class=""></canvas>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div id="panelPerfTime" class="panel panel-default" style="display: none">
                        <div class="panel-heading card" data-toggle="collapse" data-target="#perfChart3">
                            <span class="fa fa-bar-chart fa-fw"></span>
                            <label id="lblPerfTime">Performance Graph - Time</label>
                            <span class="toggle glyphicon glyphicon-chevron-right pull-right"></span>
                        </div>
                        <div class="panel-body collapse in" id="perfChart3">
                            <div class="row">
                                <div class="col-xs-12" id="ChartTime">
                                    <canvas id="canvasTime" class=""></canvas>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div id="panelPerfParty" class="panel panel-default" style="display: none">
                        <div class="panel-heading card" data-toggle="collapse" data-target="#perfChart4">
                            <span class="fa fa-bar-chart fa-fw"></span>
                            <label id="lblPerfParty">Performance Graph - nb Third Parties</label>
                            <span class="toggle glyphicon glyphicon-chevron-right pull-right"></span>
                        </div>
                        <div class="panel-body collapse in" id="perfChart4">
                            <div class="row">
                                <div class="col-xs-12" id="ChartParty">
                                    <canvas id="canvasParty" class=""></canvas>
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
