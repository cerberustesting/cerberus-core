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
    Document   : ReportingAutomateScore
    Created on : 8 May 2025
    Author     : vertigo17
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
        <script type="text/javascript" src="js/pages/ReportingAutomateScore.js"></script>
        <title id="pageTitle">Automate Score</title>
    </head>
    <body>
        <%@ include file="include/global/header.html" %>
        <div class="container-fluid center" id="page-layout">
            <%@ include file="include/global/messagesArea.html"%>

            <h1 class="page-title-line" id="title">Automate Score</h1>

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
                                    <div class='col-sm-4 col-lg-3'>
                                        <div class="form-group">
                                            <label for="gp1Select">Group 1</label>
                                            <select multiple="multiple" class="form-control" id="gp1Select"></select>
                                        </div>
                                    </div>
                                    <div class='col-sm-4 col-lg-3'>
                                        <div class="form-group">
                                            <label for="gp2Select">Group 2</label>
                                            <select multiple="multiple" class="form-control" id="gp2Select"></select>
                                        </div>
                                    </div>
                                    <div class='col-sm-4 col-lg-3'>
                                        <div class="form-group">
                                            <label for="gp3Select">Group 3</label>
                                            <select multiple="multiple" class="form-control" id="gp3Select"></select>
                                        </div>
                                    </div>
                                    <div class='col-sm-12 col-lg-3'>
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
                                </div>

                                <div class="row">
                                    <div class='col-sm-12 col-md-12'>
                                        <div class="input-group-btn ">
                                            <button type="button" class="btn btn-primary btn-block marginTop20" id="loadbutton" onclick="loadKPIGraphBars(true);">Load</button>
                                        </div>
                                    </div>
                                </div>
                                
                            </div>

                        </div>
                    </div>
                </div>


                <div class="col-lg-3" id="KPIPanel">
                    <div class="panel panel-default">
                        <div class="panel-body collapse in" id="automateScoreChart">
                            <div class="row">
                                <div class="col-xs-12 ascommentOK" id="freqChartComment1" style="">
                                    Automate Score
                                </div>
                            </div>
                            <div class="row">

                                <div class="col-xs-12 " id="freqChartComment1" style="text-align: center;">
                                    <div class="btn-group btn-toggle marginTop20 marginBottom20" id="displayByEnv" style="font-size: 30px"> 
                                        <button id="ASA" class="btn btn-OFF btn-xl btn-default" style="font-size: 30px">A</button>
                                        <button id="ASB" class="btn btn-OFF btn-xl btn-default" style="font-size: 30px">B</button>
                                        <button id="ASC" class="btn btn-ON  btn-xl btn-default" style="font-size: 30px">C</button>
                                        <button id="ASD" class="btn btn-OFF btn-xl btn-default" style="font-size: 30px">D</button>
                                        <button id="ASE" class="btn btn-OFF btn-xl btn-default" style="font-size: 30px">E</button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

            </div>



            <div class="row" id="ReportKPIPanel">

                <div class="col-lg-3">
                    <div id="panelFreqChart" class="panel panel-default" style="display: block">
                        <div class="panel-heading card" data-toggle="collapse" data-target="#freqChart">
                            <span class="fa fa-bar-chart fa-fw"></span>
                            <label id="lblFreq">Execution Frequency</label>
                            <span class="toggle glyphicon glyphicon-chevron-right pull-right"></span>
                        </div>
                        <div class="panel-body collapse in" id="freqChart">
                            <div class="row">
                                <div class="col-xs-12 ascommentOK" name="L1" id="freqChartComment1" style="">
                                    
                                </div>
                                <div class="col-xs-12 ascommentL2KO" name="L2" id="freqChartComment2" style="">
                                    <span class="glyphicon glyphicon-arrow-up" aria-hidden="true"></span>
                                </div>
                            </div>
                            <div class="row">
                                <div class="col-xs-12" id="ChartFreqStat" style="height: 150px">
                                    <canvas id="canvasFreqStat"></canvas>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col-lg-3">
                    <div id="panelReliabilityChart" class="panel panel-default" style="display: block">
                        <div class="panel-heading card" data-toggle="collapse" data-target="#relChart">
                            <span class="fa fa-bar-chart fa-fw"></span>
                            <label id="lblRel">Reliability</label>
                            <span class="toggle glyphicon glyphicon-chevron-right pull-right"></span>
                        </div>
                        <div class="panel-body collapse in" id="relChart">
                            <div class="row">
                                <div class="col-xs-12 ascommentWARNING" name="L1" id="freqChartComment1" style="">
                                    2% flaky tests
                                </div>
                                <div class="col-xs-12 ascommentL2OK" name="L2" id="freqChartComment2" style="">
                                    <span class="glyphicon glyphicon-arrow-down" aria-hidden="true"></span> - 1%
                                </div>
                            </div>
                            <div class="row">
                                <div class="col-xs-12" id="ChartRelStat" style="height: 150px">
                                    <canvas id="canvasRelStat"></canvas>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col-lg-3">
                    <div id="panelDurationChart" class="panel panel-default" style="display: block">
                        <div class="panel-heading card" data-toggle="collapse" data-target="#durChart">
                            <span class="fa fa-bar-chart fa-fw"></span>
                            <label id="lblDur">Duration</label>
                            <span class="toggle glyphicon glyphicon-chevron-right pull-right"></span>
                        </div>
                        <div class="panel-body collapse in" id="durChart">
                            <div class="row">
                                <div class="col-xs-12 ascommentOK" name="L1" id="freqChartComment1" style="">
                                    20 min avg
                                </div>
                                <div class="col-xs-12 ascommentL2WARNING" name="L2" id="freqChartComment2" style="">
                                    <span class="glyphicon glyphicon-arrow-up" aria-hidden="true"></span> + 5%
                                </div>
                            </div>
                            <div class="row">
                                <div class="col-xs-12" id="ChartDurStat" style="height: 150px">
                                    <canvas id="canvasDurStat"></canvas>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col-lg-3">
                    <div id="panelMaintenanceChart" class="panel panel-default" style="display: block">
                        <div class="panel-heading card" data-toggle="collapse" data-target="#mntChart">
                            <span class="fa fa-bar-chart fa-fw"></span>
                            <label id="lblMnt">Maintenance</label>
                            <span class="toggle glyphicon glyphicon-chevron-right pull-right"></span>
                        </div>
                        <div class="panel-body collapse in" id="mntChart">
                            <div class="row">
                                <div class="col-xs-12 ascommentOK" name="L1" id="freqChartComment1" style="">
                                    8 hours
                                </div>
                                <div class="col-xs-12 ascommentL2OK" name="L2" id="freqChartComment2" style="">
                                    <span class="glyphicon glyphicon-arrow-down" aria-hidden="true"></span> + 20%
                                </div>
                            </div>
                            <div class="row">
                                <div class="col-xs-12" id="ChartTagBar" style="height: 150px">
                                    <canvas id="canvasMntStat"></canvas>
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
