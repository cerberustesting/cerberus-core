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
    Created on : 31 Marsh 2020
    Author     : cerberus
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%@ include file="include/global/dependenciesInclusions.html" %>
        <script type="text/javascript" src="dependencies/Moment-2.30.1/moment-with-locales.min.js"></script>
        <script type="text/javascript" src="dependencies/Chart.js-2.9.3/Chart.min.js"></script>
        <script type="text/javascript" src="dependencies/Bootstrap-datetimepicker-4.17.47/bootstrap-datetimepicker.min.js"></script>
        <script type="text/javascript" src="js/pages/ReportingCampaignOverTime.js"></script>
        <title id="pageTitle">Campaign History</title>
    </head>
    <body>
        <%@ include file="include/global/header.html" %>
        <div class="container-fluid center" id="page-layout">
            <%@ include file="include/global/messagesArea.html"%>

            <h1 class="page-title-line" id="title">Campaign History</h1>

            <div class="row" >
                
                <div class="col-lg-9" id="FiltersPanel">
                    <div class="panel panel-default">
                        <div class="panel-heading card">
                            <span class="fa fa-tag fa-fw"></span>
                            <label id="filters">Filters</label>
                        </div>

                        <div class="panel-body" id="otFilterPanel">

                            <div class="">

                                <div class="row">
                                    <div class='col-md-8'>
                                        <div class="form-group">
                                            <label for="campaignSelect">Campaign</label>
                                            <select multiple="multiple" class="form-control" id="campaignSelect"></select>
                                        </div>
                                    </div>
                                    <div class='col-md-4'>
                                        <div class="form-group">
                                            <label for="envSelect">Environment</label>
                                            <select class="multiselectelement form-control" multiple="multiple" id="envSelect"></select>
                                        </div>
                                    </div>

                                </div>

                                <div class="row">
                                    <div class='col-md-4'>
                                        <div class="form-group">
                                            <label for="gp1Select">Group 1</label>
                                            <select multiple="multiple" class="form-control" id="gp1Select"></select>
                                        </div>
                                    </div>
                                    <div class='col-md-4'>
                                        <div class="form-group">
                                            <label for="gp2Select">Group 2</label>
                                            <select multiple="multiple" class="form-control" id="gp2Select"></select>
                                        </div>
                                    </div>
                                    <div class='col-md-4'>
                                        <div class="form-group">
                                            <label for="gp3Select">Group 3</label>
                                            <select multiple="multiple" class="form-control" id="gp3Select"></select>
                                        </div>
                                    </div>
                                </div>
                                <div class="row">
                                    <div class='col-sm-4 col-md-4'>
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
                                    <div class='col-sm-4 col-md-4'>
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

                                    <div class="col-sm-2 col-md-2 btn-group marginTop20">
                                        <button id="btnGroupDrop1" type="button" class="btn btn-secondary dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                                            Preset Range<span class="caret"></span>
                                        </button>
                                        <div class="dropdown-menu" aria-labelledby="btnGroupDrop1">
                                            <button class="btn btn-default btn-block pull-left" id="last1Week" onclick="setTimeRange(6)"><span class=""></span> Current Day</button>
                                            <button class="btn btn-default btn-block pull-left" id="last1Week" onclick="setTimeRange(5)"><span class=""></span> Previous Week</button>
                                            <button class="btn btn-default btn-block pull-left" id="last1Months" onclick="setTimeRange(1)"><span class=""></span> Last 30 Days</button>
                                            <button class="btn btn-default btn-block pull-left" id="last1Months" onclick="setTimeRange(7)"><span class=""></span> This Month</button>
                                            <button class="btn btn-default btn-block pull-left" id="last1Months" onclick="setTimeRange(8)"><span class=""></span> Last Calendar Month</button>
                                            <button class="btn btn-default btn-block pull-left" id="last1Months" onclick="setTimeRange(9)"><span class=""></span> Previous Calendar Month</button>
                                            <button class="btn btn-default btn-block pull-left" id="last3Months" onclick="setTimeRange(2)"><span class=""></span> Last 3 Months</button>
                                            <button class="btn btn-default btn-block pull-left" id="last6Months" onclick="setTimeRange(3)"><span class=""></span> Last 6 Months</button>
                                            <button class="btn btn-default btn-block pull-left" id="last12Months" onclick="setTimeRange(4)"><span class=""></span> Last 12 Months</button>
                                        </div>
                                    </div>

                                </div>

                                <div class="row">
                                    <div class='col-md-3'>
                                        <div class="form-group">
                                            <label for="countrySelect">Country</label>
                                            <select class="multiselectelement form-control" multiple="multiple" id="countrySelect"></select>
                                        </div>
                                    </div>
                                    <div class='col-md-3'>
                                        <div class="form-group">
                                            <label for="robotSelect">Robot Decli</label>
                                            <select class="multiselectelement form-control" multiple="multiple" id="robotSelect"></select>
                                        </div>
                                    </div>
                                    <div class='col-md-3'>
                                        <div class="form-group">
                                            <label for="ciResultSelect">CI Result</label>
                                            <select class="multiselectelement form-control" multiple="multiple" id="ciResultSelect"></select>
                                        </div>
                                    </div>
                                    <div class='col-sm-2 col-md-3'>
                                        <div class="input-group-btn ">
                                            <button type="button" class="btn btn-primary btn-block marginTop20" id="loadbutton" onclick="loadPerfGraph(true);">Load</button>
                                        </div>
                                    </div>
                                </div>

                                <div id="perfFilters" style="display: none;" class="row">
                                    <div class='col-md-4'>
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
                                    <div class='col-md-4'>
                                        <div class="form-group">
                                            <label for="types">Media Types</label>
                                            <select class="multiselectelement form-control" multiple="multiple" id="types"></select>
                                        </div>
                                    </div>
                                </div>

                            </div>

                        </div>
                    </div>
                </div>


                <div class="col-lg-3" id="KPIPanel">
                    <div class="panel panel-default">
                        <div class="panel-body collapse in" id="availabiltyChart">
                            <div class="row">
                                <div class="col-xs-8 paddingRight0" id="ChartAvailabilty1" >
                                    <canvas id="canvasAvailability1"></canvas>
                                </div>
                                <div class="col-xs-4 paddingLeft0"  >
                                    <h2 class="statistic-counter" id="ChartAvailabilty1Counter"></h2>
                                    <p class="statistic-counter" id="ChartAvailabilty1CounterDet"></p>
                                </div>
                            </div>
                            <div class="row">
                                <div class="col-xs-8 paddingRight0" id="ChartAvailabilty2" >
                                    <canvas id="canvasAvailability2"></canvas>
                                </div>
                                <div class="col-xs-4 paddingLeft0"  >
                                    <h2 class="statistic-counter" id="ChartAvailabilty2Counter"></h2>
                                    <p class="statistic-counter" id="ChartAvailabilty2CounterDet"></p>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

            </div>



            <div class="row" id="ReportByTestFolderPanel">
                <div class="col-lg-12">

                    <div id="panelTagDStat" class="panel panel-default" style="display: none">
                        <div class="panel-heading card" data-toggle="collapse" data-target="#perfChart1">
                            <span class="fa fa-bar-chart fa-fw"></span>
                            <label id="lblTagDStat">Campaign Duration</label>
                            <span class="toggle glyphicon glyphicon-chevron-right pull-right"></span>
                        </div>
                        <div class="panel-body collapse in" id="perfChart1">
                            <div class="row">
                                <div class="col-xs-12" id="ChartTagDStat" style="height: 400px">
                                    <canvas id="canvasTagDStat"></canvas>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div id="panelTagEStat" class="panel panel-default" style="display: none">
                        <div class="panel-heading card" data-toggle="collapse" data-target="#perfChart3">
                            <span class="fa fa-bar-chart fa-fw"></span>
                            <label id="lblTagEStat">Campaign TestCase coverage & total execution number</label>
                            <span class="toggle glyphicon glyphicon-chevron-right pull-right"></span>
                        </div>
                        <div class="panel-body collapse in" id="perfChart3">
                            <div class="row">
                                <div class="col-xs-12" id="ChartTagEStat" style="height: 400px">
                                    <canvas id="canvasTagEStat"></canvas>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div id="panelTagSStat" class="panel panel-default" style="display: none">
                        <div class="panel-heading card" data-toggle="collapse" data-target="#perfChart2">
                            <span class="fa fa-bar-chart fa-fw"></span>
                            <label id="lblTagSStat">Campaign CI Score & Threshold</label>
                            <span class="toggle glyphicon glyphicon-chevron-right pull-right"></span>
                        </div>
                        <div class="panel-body collapse in" id="perfChart2">
                            <div class="row">
                                <div class="col-xs-12" id="ChartTagSStat" style="height: 400px">
                                    <canvas id="canvasTagSStat"></canvas>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div id="panelTagStatBar" class="panel panel-default" style="display: none">
                        <div class="panel-heading card" data-toggle="collapse" data-target="#perfChart4">
                            <span class="fa fa-bar-chart fa-fw"></span>
                            <label id="lblTagStatBar">Campaign Execution per status</label>
                            <span class="toggle glyphicon glyphicon-chevron-right pull-right"></span>
                        </div>
                        <div class="panel-body collapse in" id="perfChart4">
                            <div class="row">
                                <div class="col-xs-12" id="ChartTagBar" style="height: 400px">
                                    <canvas id="canvasTagBar"></canvas>
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
