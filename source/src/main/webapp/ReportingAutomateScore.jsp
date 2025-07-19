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

                <div class="col-lg-12" id="FiltersPanel">
                    <div class="panel panel-default">
                        <div class="panel-heading card">
                            <span class="fa fa-tag fa-fw"></span>
                            <label id="filters">Filters</label>
                        </div>

                        <div class="panel-body" id="otFilterPanel">

                            <div class="">

                                <div class="row">
                                    <div class='col-md-3'>
                                        <div class="form-group">
                                            <label for="systemSelect">System</label>
                                            <select multiple="multiple" class="form-control" id="systemSelect"></select>
                                        </div>
                                    </div>
                                    <div class='col-md-5'>
                                        <div class="form-group">
                                            <label for="campaignSelect">Campaign</label>
                                            <select multiple="multiple" class="form-control" id="campaignSelect"></select>
                                        </div>
                                    </div>
                                    <div class='col-md-3'>
                                        <div class="form-group">
                                            <label for="topicker">Until</label>
                                            <div class='input-group date' id='topicker'>
                                                <input type='text' class="form-control" />
                                                <span class="input-group-addon">
                                                    <span class="glyphicon glyphicon-calendar"></span>
                                                </span>
                                            </div>
                                        </div>
                                    </div>
                                    <div class='col-md-1'>
                                        <div class="form-group">
                                            <label for="trendWeeks">Trend Weeks</label>
                                            <div class='input-group date' >
                                                <input type='number' id='trendWeeks' class="form-control" />
                                            </div>
                                        </div>
                                    </div>
                                    <div class='col-md-4'>
                                        <div class="form-group" style="display: none">
                                            <label for="envSelect">Environment</label>
                                            <select class="multiselectelement form-control" multiple="multiple" id="envSelect"></select>
                                        </div>
                                    </div>

                                </div>

                                <div class="row" style="display: none">
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

            </div>

            <div class="row">

                <div class="col-lg-12" id="KPIPanel">

                    <div class="panel panel-default">
                        <div class="panel-heading card">
                            <span class="fa fa-bar-chart fa-fw"></span>
                            <label id="filters" >In Value Automate</label>
                        </div>



                        <div class="panel panel-default">
                            <div class="panel-body collapse in" id="automateScoreChart">
                                <div class="row">
                                    <div class="col-md-3">

                                        <div class="row">

                                            <div class="col-xs-12 " id="chartComment1" style="text-align: center;">
                                                <div class="btn-group btn-toggle marginTop20 marginBottom20" id="displayByEnv" style="font-size: 30px"> 
                                                    <button id="ASA" class="btn btn-OFF btn-xl btn-default ASButton" style="font-size: 30px">A</button>
                                                    <button id="ASB" class="btn btn-OFF btn-xl btn-default ASButton" style="font-size: 30px">B</button>
                                                    <button id="ASC" class="btn btn-ON  btn-xl btn-default ASButton" style="font-size: 30px">C</button>
                                                    <button id="ASD" class="btn btn-OFF btn-xl btn-default ASButton" style="font-size: 30px">D</button>
                                                    <button id="ASE" class="btn btn-OFF btn-xl btn-default ASButton" style="font-size: 30px">E</button>
                                                </div>
                                            </div>

                                        </div>
                                    </div>
                                    <div class="col-md-2">
                                        <div class="col-xs-6" style="padding-right: 0px;" id="chartTitle">
                                            <div class="asperimeter marginBottom20" name="title">
                                                <span id="scopeCampaigns" class="label label-default marginBottom10" style="font-size : 15px; margin-right:15px">X Campaigns</span><br>
                                            </div>
                                            <div class="asperimeter marginBottom20" name="title">
                                                <span id="scopeTests" class="label label-default" style="font-size : 15px; margin-right:15px">X Test cases</span>
                                            </div>
                                            <div class="asperimeter marginBottom20" name="title">
                                                <span id="scopeApplications" class="label label-default" style="font-size : 15px; margin-right:15px">X Applications</span>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="col-md-7">
                                        <div class="col-xs-12" style="padding-right: 0px;" id="chartTitle">
                                            <div class="" name="subtitle" style="font-size: large;">
                                                The Automate Score measures the effectiveness of your testing automation across four critical dimensions: execution frequency, stability, duration and maintenance effort.
                                            </div>
                                        </div>
                                    </div>

                                </div>
                            </div>
                        </div>


                    </div>
                </div>

            </div>

            <div class="row">

                <div class="col-lg-12" id="KPIDetailPanel">
                    <div class="panel panel-default">

                        <div class="panel-heading card">
                            <span class="fa fa-bar-chart fa-fw"></span>
                            <label id="lblFreq">Key Metrics</label>
                            <!--                        <span class="toggle glyphicon glyphicon-chevron-right pull-right"></span>-->
                        </div>
                        <div class="panel-body collapse in" style="padding:0px;"  id="kpiScoreChart">
                            <div class="row">

                                <div class="col-md-6 ">
                                    <div style="border-style: solid; border-color: lightgray;border-width: 2px;margin: 1em 2em 1em;">

                                        <div class="row">
                                            <div class="col-xs-6 " style="padding-right: 0px;" id="freqChartTitle">
                                                <div class="astitle" name="title">
                                                    TITLE
                                                </div>
                                                <div class="assubtitle" name="subtitle">
                                                    subTITLE
                                                </div>
                                                <div class="askpi" name="kpi">
                                                    X
                                                </div>
                                            </div>
                                            <div class="col-xs-6 " style="padding-left: 0px;">
                                                <div class=" " id="freqChartScore" style="text-align: center;">
                                                    <div class="btn-group btn-toggle marginTop5 marginBottom20"> 
                                                        <button name="ASA" class="btn btn-A ASButton">A</button>
                                                        <button name="ASB" class="btn btn-B ASButton">B</button>
                                                        <button name="ASC" class="btn btn-C ASButton">C</button>
                                                        <button name="ASD" class="btn btn-D ASButton">D</button>
                                                        <button name="ASE" class="btn btn-E ASButton">E</button>
                                                    </div>
                                                </div>
                                                <div id="freqChartVar" style="text-align: center;"> 
                                                    <span class="ASkpiVar" name="var"><img width="20px" style="border-right: 20px;" src=""></span>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>


                                <div class="col-md-6 ">
                                    <div style="border-style: solid; border-color: lightgray;border-width: 2px;margin: 1em 2em 1em;">
                                        <div class="row">
                                            <div class="col-xs-6" style="padding-right: 0px;" id="relChartTitle">
                                                <div class="astitle" name="title">
                                                    TITLE
                                                </div>
                                                <div class="assubtitle" name="subtitle">
                                                    subTITLE
                                                </div>
                                                <div class="askpi" name="kpi">
                                                    X
                                                </div>
                                            </div>
                                            <div class="col-xs-6 " style="padding-left: 0px;">
                                                <div class=" " id="relChartScore" style="text-align: center;">
                                                    <div class="btn-group btn-toggle marginTop5 marginBottom20"> 
                                                        <button name="ASA" class="btn btn-A ASButton">A</button>
                                                        <button name="ASB" class="btn btn-B ASButton">B</button>
                                                        <button name="ASC" class="btn btn-C ASButton">C</button>
                                                        <button name="ASD" class="btn btn-D ASButton">D</button>
                                                        <button name="ASE" class="btn btn-E ASButton">E</button>
                                                    </div>
                                                </div>
                                                <div id="relChartVar" style="text-align: center;"> 
                                                    <span class="ASkpiVar" name="var"><img width="20px" style="border-right: 20px;" src=""></span>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>

                                <div class="col-md-6 ">
                                    <div style="border-style: solid; border-color: lightgray;border-width: 2px;margin: 1em 2em 1em;">
                                        <div class="row">
                                            <div class="col-xs-6" style="padding-right: 0px;" id="durChartTitle">
                                                <div class="astitle" name="title">
                                                    TITLE
                                                </div>
                                                <div class="assubtitle" name="subtitle">
                                                    subTITLE
                                                </div>
                                                <div class="askpi" name="kpi">
                                                    X
                                                </div>
                                            </div>
                                            <div class="col-xs-6 " style="padding-left: 0px;">
                                                <div class=" " id="durChartScore" style="text-align: center;">
                                                    <div class="btn-group btn-toggle marginTop5 marginBottom20"> 
                                                        <button name="ASA" class="btn btn-A ASButton">A</button>
                                                        <button name="ASB" class="btn btn-B ASButton">B</button>
                                                        <button name="ASC" class="btn btn-C ASButton">C</button>
                                                        <button name="ASD" class="btn btn-D ASButton">D</button>
                                                        <button name="ASE" class="btn btn-E ASButton">E</button>
                                                    </div>
                                                </div>
                                                <div id="durChartVar" style="text-align: center;"> 
                                                    <span class="ASkpiVar" name="var"><img width="20px" style="border-right: 20px;" src=""></span>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>

                                <div class="col-md-6 ">
                                    <div style="border-style: solid; border-color: lightgray;border-width: 2px;margin: 1em 2em 1em;">
                                        <div class="row">
                                            <div class="col-xs-6" style="padding-right: 0px;" id="mntChartTitle">
                                                <div class="astitle" name="title">
                                                    TITLE
                                                </div>
                                                <div class="assubtitle" name="subtitle">
                                                    subTITLE
                                                </div>
                                                <div class="askpi" name="kpi">
                                                    X
                                                </div>
                                            </div>
                                            <div class="col-xs-6 " style="padding-left: 0px;">
                                                <div class=" " id="mntChartScore" style="text-align: center;">
                                                    <div class="btn-group btn-toggle marginTop5 marginBottom20"> 
                                                        <button name="ASA" class="btn btn-A ASButton">A</button>
                                                        <button name="ASB" class="btn btn-B ASButton">B</button>
                                                        <button name="ASC" class="btn btn-C ASButton">C</button>
                                                        <button name="ASD" class="btn btn-D ASButton">D</button>
                                                        <button name="ASE" class="btn btn-E ASButton">E</button>
                                                    </div>
                                                </div>
                                                <div id="mntChartVar" style="text-align: center;"> 
                                                    <span class="ASkpiVar" name="var"><img width="20px" style="border-right: 20px;" src=""></span>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>

                            </div>
                        </div>


                    </div>
                </div>

            </div>




            <div class="row" style="display: none">

                <div class="col-lg-12" id="KPIRecoPanel">
                    <div class="panel panel-default">

                        <div class="panel-heading card">
                            <span class="fa fa-bar-chart fa-fw"></span>
                            <label id="lblFreq">Recomendations</label>
                        </div>
                        <div class="panel-body collapse in" style="padding:0px;"  id="automateScoreReco">
                            <div class="row">

                                <div class="col-md-6">
                                    <div style="background-color: #eafaff;margin: 1em 2em 1em;">
                                        ddgd
                                    </div>
                                </div>


                                <div class="col-md-6">
                                    <div style="background-color: #eafaff;margin: 1em 2em 1em;">
                                        ddgd
                                    </div>
                                </div>

                                <div class="col-md-6">
                                    <div style="background-color: #eafaff;margin: 1em 2em 1em;">
                                        ddgd
                                    </div>
                                </div>

                                <div class="col-md-6">
                                    <div style="background-color: #eafaff;margin: 1em 2em 1em;">
                                        ddgd
                                    </div>
                                </div>

                            </div>
                        </div>


                    </div>
                </div>

            </div>



            <div class="row">

                <div class="col-lg-12" id="ReportKPIPanel">

                    <div id="panelTrendChart" class="panel panel-default" style="display: block">

                        <div class="panel-heading card" data-toggle="collapse" data-target="#trendChart">
                            <span class="fa fa-bar-chart fa-fw"></span>
                            <label id="lblTrends">Weeks Trends</label>
                            <span class="toggle glyphicon glyphicon-chevron-right pull-right"></span>
                        </div>

                        <div class="panel-body collapse in" id="trendChart">

                            <div class="col-md-6">
                                <div id="panelFrequencyChart" class="panel panel-default" style="display: block">
                                    <div class="panel-body" id="freqChart">

                                        <div class="row">
                                            <div class="col-xs-12 marginBottom5" id="lblFreq" style="">
                                                <label id="lblFreq">Execution Frequency</label>
                                            </div>
                                        </div>
                                        <div class="row">
                                            <div class="col-xs-12 marginBottom15" name="L1" id="freqChartComment1" style="">
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

                            <div class="col-md-6">
                                <div id="panelReliabilityChart" class="panel panel-default" style="display: block">
                                    <div class="panel-body" id="relChart">

                                        <div class="row">
                                            <div class="col-xs-12 marginBottom5" id="lblFreq" style="">
                                                <label id="lblRel">Stability</label>
                                            </div>
                                        </div>
                                        <div class="row">
                                            <div class="col-xs-12 marginBottom15" name="L1" id="freqChartComment1" style="">
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

                            <div class="col-md-6">
                                <div id="panelDurationChart" class="panel panel-default" style="display: block">
                                    <div class="panel-body" id="durChart">
                                        <div class="row">
                                            <div class="col-xs-12 marginBottom5" id="lblFreq" style="">
                                                <label id="lblDur">Duration</label>
                                            </div>
                                        </div>
                                        <div class="row">
                                            <div class="col-xs-12 marginBottom15" name="L1" id="freqChartComment1" style="">
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

                            <div class="col-md-6">
                                <div id="panelMaintenanceChart" class="panel panel-default" style="display: block">
                                    <div class="panel-body" id="mntChart">
                                        <div class="row">
                                            <div class="col-xs-12 marginBottom5" id="lblFreq" style="">
                                                <label id="lblMnt">Maintenance</label>
                                            </div>
                                        </div>
                                        <div class="row">
                                            <div class="col-xs-12 marginBottom15" name="L1" id="freqChartComment1" style="">
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

                    </div>

                </div>
            </div>


            <div class="row">

                <div class="col-lg-12" id="DetailKPIPanel">


                    <ul id="tabsScriptEdit" class="nav nav-tabs" data-tabs="tabs">
                        <li class="active"><a data-toggle="tab" href="#tabTestcases" id="testcaseDetails" name="tabTestcases">Test Cases</a></li>
                        <li><a data-toggle="tab" href="#tabCampaigns" id="campaignDetails" name="tabCampaigns">Campaigns</a></li>
                    </ul>

                    <div class="tab-content">

                        <div class="center tab-pane fade in active" id="tabTestcases">
                            <div class="panel panel-default">
                                <!--                            <div class="panel-heading card" data-toggle="collapse" data-target="#NS3">
                                                                <span class="fa fa-pie-chart fa-fw"></span>
                                                                <label id="ns3Label">Requests List</label>
                                                                <span class="toggle glyphicon glyphicon-chevron-right pull-right"></span>
                                                            </div>-->
                                <div class="panel-body collapse in" id="testcaseList">
                                    <table id="testcasesTable" class="table table-bordered table-hover display" name="testcasesTable"></table>
                                </div>
                            </div>
                        </div>

                        <div class="center tab-pane fade in" id="tabCampaigns">
                            <div class="panel panel-default">
                                <div class="panel-body" id="campaignList">
                                    <table id="campaignsTable" class="table table-bordered table-hover display" name="campaignsTable"></table>
                                </div>
                            </div>
                        </div>

                    </div>

                </div>
            </div>


            <div class="row">

                <footer class="footer">
                    <div class="container-fluid" id="footer"></div>
                </footer>
            </div>
    </body>
</html>
