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
        <link rel="stylesheet" type="text/css" href="css/pages/ReportingMonitor.css">
        <script type="text/javascript" src="js/pages/ReportingMonitor.js"></script>
        <title id="pageTitle">Monitor</title>
    </head>
    <body>
        <%@ include file="include/global/header.html" %>
        <div class="container-fluid center" id="page-layout">
            <%@ include file="include/global/messagesArea.html"%>

            <h1 class="page-title-line" id="title">Monitor</h1>

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
                                    <div class='col-md-2 col-sm-4'>
                                        <div class="form-group">
                                            <label for="systemSelect">System</label>
                                            <select multiple="multiple" class="form-control" id="systemSelect"></select>
                                        </div>
                                    </div>
                                    <div class='col-md-2 col-sm-4'>
                                        <div class="form-group">
                                            <label for="campaignSelect">Campaign</label>
                                            <select multiple="multiple" class="form-control" id="campaignSelect"></select>
                                        </div>
                                    </div>
                                    <div class='col-md-2 col-sm-4'>
                                        <div class="form-group" style="display: block">
                                            <label for="envSelect">Environment</label>
                                            <select class="multiselectelement form-control" multiple="multiple" id="envSelect"></select>
                                        </div>
                                    </div>
                                    <div class='col-md-2 col-sm-4'>
                                        <div class="form-group" style="display: block">
                                            <label for="countrySelect">Country</label>
                                            <select class="multiselectelement form-control" multiple="multiple" id="countrySelect"></select>
                                        </div>
                                    </div>
                                    <div class="form-group form-group-xs col-md-2 col-sm-4 col-xs-6">
                                        <label for="displayMuted">Display Muted</label>
                                        <input class="form-control input-xs" id="displayMuted" name="displayMuted" type="checkbox" value="1">
                                    </div>                                    
                                    <div class="form-group form-group-xs col-md-2 col-sm-4 col-xs-6">
                                        <label for="displayRetry">Display Retries</label>
                                        <input class="form-control input-xs" id="displayRetry" name="displayRetry" type="checkbox" value="1">
                                    </div>                                    
                                </div>

                                <div class="row">

                                    <div class='col-md-8  col-sm-12'>
                                        <div class="form-group" style="display: block">
                                            <label for="systemSelect">Select columns to display</label>
                                            <div class="btn-group btn-toggle marginTop5" id="layoutMode"> 
                                                <button class="btn btn-col btn-system btn-xs btn-default " onclick="toggleCol(this, 'system');">System</button>
                                                <button class="btn btn-col btn-application btn-xs btn-default" onclick="toggleCol(this, 'application');">Application</button>
                                                <button class="btn btn-col btn-test btn-xs btn-xs btn-default" onclick="toggleCol(this, 'test');">Test Folder</button>
                                                <button class="btn btn-col btn-testCase btn-xs btn-xs btn-default" onclick="toggleCol(this, 'testCase');">Testcase ID</button>
                                                <button class="btn btn-col btn-country btn-xs btn-xs btn-default" onclick="toggleCol(this, 'country');">Country</button>
                                                <button class="btn btn-col btn-environment btn-xs btn-xs btn-default" onclick="toggleCol(this, 'environment');">Environment</button>
                                                <button class="btn btn-col btn-robot btn-xs btn-xs btn-default" onclick="toggleCol(this, 'robot');">Robot</button>
                                                <button class="btn btn-col btn-campaign btn-xs btn-xs btn-default" onclick="toggleCol(this, 'campaign');">Campaign</button>
                                                <button class="btn btn-auto btn-xs btn-xs btn-default" style="margin-left:15px;font-weight:bold" onclick="toggleColAutomode(this);">Auto Mode</button>
                                            </div>
                                        </div>
                                    </div>
                                    <div class='col-md-2  col-xs-6'>
                                        <label for="maxPreviousExe">Previous Executions displayed</label>
                                        <input type="integer" class="form-control" id="maxPreviousExe"/>
                                    </div>
                                    <div class='col-md-2  col-xs-6'>
                                        <label for="displayHorizonMin">Horizon display in min</label>
                                        <input type="integer" class="form-control" id="displayHorizonMin"/>
                                    </div>


                                </div>

                                <div class="row">
                                    <div class='col-sm-12 col-md-12'>
                                        <div class="input-group-btn ">
                                            <button type="button" class="btn btn-primary btn-block marginTop20" id="loadbutton" onclick="loadBoard();">Load</button>
                                        </div>
                                    </div>
                                </div>


                                <!--                                <div class="row">
                                                                    <div class='col-sm-12 col-md-12'>
                                                                        <div class="input-group-btn ">
                                                                            <button type="button" class="btn btn-primary btn-block marginTop20" id="loadbutton" onclick="loadKPIGraphBars(true);">Load</button>
                                                                        </div>
                                                                    </div>
                                                                </div>-->



                            </div>

                        </div>
                    </div>
                </div>

            </div>

            <div class="row">

                <div class="col-lg-12" id="MonitorPanel">

                    <div class="panel panel-default">
                        <div class="panel-heading card">

                            <span class="fa fa-bar-chart fa-fw"></span>
                            <label id="filters" >Monitoring Status</label>
                        </div>

                        <div class="panel-body collapse in" style="overflow-y: scroll;"  id="monitoringChart">
                            <div class="row">
                                <div class="col-sm-6 global-counter" id="MonitorHeader">
                                    Last refresh = xx s
                                </div>
                                <div class="col-sm-3" id="MonitorHeaderCounter">
                                    xx Total
                                </div>
                                <div class="btn-group pull-right">
                                    <button id="reload" class="btn btn-default btn-xs marginRight10"
                                            onclick="loadBoard();"><span
                                            class="glyphicon glyphicon-refresh"></span> <label id="goFullscreen">Reload</label>
                                    </button>
                                    <button id="goFullscreen" class="btn btn-default btn-xs marginRight10 togglefullscreen"
                                            onclick="goFullscreen();"><span
                                            class="glyphicon glyphicon-fullscreen"></span> <label id="goFullscreen">Fullscreen</label>
                                    </button>
                                </div>
                            </div>

                            <div id="progressMonitor"  class="row">
                                <div class="col-lg-12">

                                    <div class="progress marginTop20" id="statusProgress" style="height:15px" >
                                    </div>
                                </div>
                            </div>


                            <table id="tableMonitor" style="width: 100%;">
                            </table>

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
