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
        <script type="text/javascript" src="dependencies/Moment-2.30.1/moment-with-locales.min.js"></script>
        <script type="text/javascript" src="dependencies/Chart.js-2.9.3/Chart.min.js"></script>
        <script type="text/javascript" src="dependencies/Bootstrap-datetimepicker-4.17.47/bootstrap-datetimepicker.min.js"></script>
        <script type="text/javascript" src="js/pages/TestCaseExecutionQueueList.js"></script>
        <script type="text/javascript" src="js/transversalobject/TestCaseExecutionQueue.js"></script>
        <script type="text/javascript" src="js/transversalobject/Parameter.js"></script>
        <script type="text/javascript" src="js/transversalobject/Invariant.js"></script>
        <script type="text/javascript" src="js/transversalobject/Application.js"></script>
        <title id="pageTitle">Executions in Queue</title>
        <link rel="stylesheet" type="text/css" href="css/pages/TestCaseExecutionQueue.css"/>
    </head>
    <body>
        <%@ include file="include/global/header.html" %>
        <div class="container-fluid center" id="page-layout">
            <%@ include file="include/global/messagesArea.html"%>
            <%@ include file="include/utils/modal-confirmation.html"%>
            <%@ include file="include/pages/testcaseexecutionqueue/massActionExecutionPending.html"%>
            <%@ include file="include/transversalobject/TestCaseExecutionQueue.html"%>
            <%@ include file="include/transversalobject/Parameter.html"%>
            <%@ include file="include/transversalobject/Invariant.html"%>
            <%@ include file="include/transversalobject/Application.html"%>

            <h1 class="page-title-line" id="title">Executions in Queue</h1>

            <ul id="tabsScriptEdit" class="nav nav-tabs" data-tabs="tabs">
                <li><a data-toggle="tab" href="#tabDetails" id="editTabDetails" name="tabDetails">Executions in queue</a></li>
                <li><a data-toggle="tab" href="#tabFollowUp" id="editTabFollowUp" name="tabFollowUp">Pools Follow Up</a></li>
                <li><a data-toggle="tab" href="#tabQueueHistory" id="editTabQueueHistory" name="tabQueueHistory">Queue History</a></li>
                <li><a data-toggle="tab" href="#tabJobStatus" id="editTabJobStatus" name="tabJobStatus">Queue Job Status</a></li>
            </ul>


            <div class="tab-content">

                <div class="center tab-pane fade" id="tabDetails">
                    <div class="panel panel-default">
                        <form id="massActionForm" name="massActionForm"  title="" role="form">
                            <div class="panel-body" id="executionList">
                                <table id="executionsTable" class="table table-bordered table-hover display" name="executionsTable"></table>
                                <div class="marginBottom20"></div>
                            </div>
                        </form>
                    </div>
                </div>

                <div class="center tab-pane fade" id="tabFollowUp">
                    <div class="panel panel-default">
                        <div class="panel-body">
                            <div class='marginBottom10'>
                                <button type="button" class="btn btn-default" style="margin-left: 10px;" id="refreshFollowUpbutton" onclick="displayAndRefresh_followup()"><span class="glyphicon glyphicon-refresh"></span> Refresh</button>
                            </div>
                            <div id="followUpTableList">
                                <table id="followUpTable" class="table table-bordered table-hover display" name="followUpTable"></table>
                                <div class="marginBottom20"></div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="center tab-pane fade" id="tabJobStatus">
                    <div class="panel panel-default">
                        <div class="panel-body">
                            <div class='marginBottom10'>
                                <button type="button" class="btn btn-default" style="margin-left: 10px;" id="refreshJobStatusbutton" onclick="displayAndRefresh_jobStatus()"><span class="glyphicon glyphicon-refresh"></span> Refresh</button>
                            </div>

                            <div class="panel panel-default"  id="QueueJobActive" style="padding:10px;background-color: #fafafa;">
                                <div class="row">
                                    <div class="form-group col-sm-6">
                                        <table>
                                            <tbody>
                                                <tr>
                                                    <th class="text-center">
                                                        <h2 id="jobActiveStatus" class="glyphicon pull-left text-info" style="font-size:2em"></h2>
                                                    </th>
                                                    <th class="text-center" >
                                                        <button type="button" class="btn btn-default" style="margin-left: 10px;" id="modifyParambutton" onclick="enableDisableJob();"><span id="playpausebutton" class="glyphicon glyphicon-play"></span> Activate / Desactivate Queue Job</button>
                                                    </th>
                                                </tr>
                                            </tbody>
                                        </table>
                                        <input type="text" class="hidden form-control" name="jobActive" id="jobActive" aria-describedby="basic-addon1" readonly>
                                    </div>
                                    <div class="form-group col-sm-6">
                                        <label for="instanceJobActive" name="jobActiveField">Instance Job Activated</label>
                                        <input type="text" class="form-control" name="instanceJobActive" id="instanceJobActive" aria-describedby="basic-addon1" readonly>
                                    </div>
                                </div>
                            </div>

                            <div class="panel panel-default" id="QueueJobStatus" style="padding:10px;background-color: #fafafa;">
                                <div class="row">
                                    <div class="form-group col-sm-6">
                                        <label for="jobRunning" name="jobRunningField">Is Queue Job currently running ?</label>
                                        <input type="text" class="form-control" name="jobRunning" id="jobRunning" aria-describedby="basic-addon1" readonly>
                                    </div>
                                    <div class="form-group col-sm-6">
                                        <label for="jobStart" name="jobStartField">Last Queue Job start</label>
                                        <input type="text" class="form-control" name="jobStart" id="jobStart" aria-describedby="basic-addon1" readonly>
                                    </div>
                                </div>
                                <div class="row">
                                    <div class="form-group col-sm-12">
                                        <button type="button" class="btn btn-default" style="margin-left: 10px;" id="refreshForceExebutton" onclick="forceExecution()"><span class="glyphicon glyphicon-play"></span> Force Execution</button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="center tab-pane fade" id="tabQueueHistory">
                    <div class="" id="FiltersPanel">

                        <div class="panel panel-default">

                            <div class="panel-heading card">
                                <span class="fa fa-tag fa-fw"></span>
                                <label id="filters">Filters</label>
                            </div>

                            <div class="panel-body" id="qsFilterPanel">

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
                                            <button class="btn btn-default pull-left" id="last2Hours" style="margin-left: 5px; margin-right: 5px;" onclick="setTimeRange(10)"><span class=""></span> Previous Hour</button>
                                            <button class="btn btn-default pull-left" id="last6Hours" style="margin-left: 5px; margin-right: 5px;" onclick="setTimeRange(11)"><span class=""></span> Previous 6 Hours</button>
                                            <button class="btn btn-default pull-left" id="currentDay" style="margin-left: 5px; margin-right: 5px;" onclick="setTimeRange(6)"><span class=""></span> Current Day</button>
                                            <button class="btn btn-default pull-left" id="last1Week" style="margin-left: 5px; margin-right: 5px;" onclick="setTimeRange(5)"><span class=""></span> Previous Week</button>
                                            <button class="btn btn-default pull-left" id="last1Months" style="margin-left: 5px; margin-right: 5px;" onclick="setTimeRange(1)"><span class=""></span> Previous Month</button>
                                            <button class="btn btn-default pull-left" id="last3Months" style="margin-left: 5px; margin-right: 5px;" onclick="setTimeRange(2)"><span class=""></span> Previous 3 Months</button>
                                            <button class="btn btn-default pull-left" id="last6Months" style="margin-left: 5px; margin-right: 5px;" onclick="setTimeRange(3)"><span class=""></span> Previous 6 Months</button>
                                            <button class="btn btn-default pull-left" id="last12Months" style="margin-left: 5px; margin-right: 5px;" onclick="setTimeRange(4)"><span class=""></span> Previous Year</button>
                                        </div>
                                    </div>

                                    <div class='col-sm-2 col-md-2'>
                                        <div class="input-group-btn ">
                                            <button type="button" class="btn btn-primary marginTop20" style="margin-left: 10px;min-height: " id="loadbutton" onclick="loadStatGraph();">Load</button>
                                        </div>
                                    </div>
                                </div>

                            </div>

                        </div>
                    </div>

                    <div class="row" id="ReportQueueStatPanel">
                        <div class="col-lg-12">

                            <div id="panelQueueStat" class="panel panel-default" style="display: none">
                                <div class="panel-heading card" data-toggle="collapse" data-target="#perfChart1">
                                    <span class="fa fa-bar-chart fa-fw"></span>
                                    <label id="lblQueueStat">Queue Execution Status</label>
                                    <span class="toggle glyphicon glyphicon-chevron-right pull-right"></span>
                                </div>
                                <div class="panel-body collapse in" id="perfChart1">
                                    <div class="row">
                                        <div class="col-xs-12" id="ChartQueueStat" style="height: 400px">
                                            <canvas id="canvasQueueStat"></canvas>
                                        </div>
                                    </div>
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