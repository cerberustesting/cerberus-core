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
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%@ include file="include/global/dependenciesInclusions.html" %>
        <title>Executions in Queue</title>
        <link rel="stylesheet" type="text/css" href="css/pages/TestCaseExecutionQueue.css"/>
        <script type="text/javascript" src="dependencies/D3js-3.x.x/js/d3.min.js"></script>
        <script type="text/javascript" src="dependencies/D3-tip-0.6.7/js/index.js"></script>
        <script type="text/javascript" src="js/pages/TestCaseExecutionQueueList.js"></script>
        <script type="text/javascript" src="js/transversalobject/TestCaseExecutionQueue.js"></script>
        <script type="text/javascript" src="js/transversalobject/Parameter.js"></script>
        <script type="text/javascript" src="js/transversalobject/Invariant.js"></script>
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

            <h1 class="page-title-line" id="title">Executions in Queue</h1>
            <div class="panel panel-default">
                <div class="panel-heading" id="executionListLabel">
                    <span class="glyphicon glyphicon-list"></span>
                    Executions in Queue
                </div>
                <div class="panel-body" id="executionList">
                    <ul id="tabsScriptEdit" class="nav nav-tabs" data-tabs="tabs">
                        <li class="active"><a data-toggle="tab" href="#tabDetails" id="editTabDetails" name="tabDetails">Executions in queue</a></li>
                        <li><a data-toggle="tab" href="#tabFollowUp" id="editTabFollowUp" name="tabFollowUp">Pools Follow Up</a></li>
                        <li><a data-toggle="tab" href="#tabJobStatus" id="editTabJobStatus" name="tabJobStatus">Queue Job Status</a></li>
                    </ul>
                    <div class="tab-content">
                        <div class="center marginTop25 tab-pane fade in active" id="tabDetails">
                            <form id="massActionForm" name="massActionForm"  title="" role="form">
                                <table id="executionsTable" class="table table-bordered table-hover display" name="executionsTable"></table>
                            </form>
                        </div>
                        <div class="center marginTop25 tab-pane fade" id="tabFollowUp">
                            <div class='marginBottom10'>
                                <button type="button" class="btn btn-default" style="margin-left: 10px;" id="refreshFollowUpbutton" onclick="displayAndRefresh_followup()"><span class="glyphicon glyphicon-refresh"></span> Refresh</button>
                            </div>
                            <div id="followUpTableList">
                                <table id="followUpTable" class="table table-bordered table-hover display" name="followUpTable"></table>
                                <div class="marginBottom20"></div>
                            </div>
                        </div>
                        <div class="center marginTop25 tab-pane fade" id="tabJobStatus">
                            <div class='marginBottom10'>
                                <button type="button" class="btn btn-default" style="margin-left: 10px;" id="refreshJobStatusbutton" onclick="displayAndRefresh_jobStatus()"><span class="glyphicon glyphicon-refresh"></span> Refresh</button>
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

                            <div class="panel panel-default"  id="QueueJobActive" style="padding:10px;background-color: #fafafa;">
                                <div class="row">
                                    <div class="form-group col-sm-6">
                                        <label for="jobActive" name="jobActiveField">Job Activated</label>
                                        <input type="text" class="form-control" name="jobActive" id="jobActive" aria-describedby="basic-addon1" readonly>
                                    </div>
                                </div>
                                <div class="row">
                                    <div class="form-group col-sm-6">
                                        <button type="button" class="btn btn-default" style="margin-left: 10px;" id="modifyParambutton" onclick="enableDisableJob();"><span class="glyphicon glyphicon-play"></span> <span class="glyphicon glyphicon-pause"></span> Modify Parameter in order to Activate / Desactivate Queue Job</button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="marginBottom20"></div>
                </div>
            </div>
            <footer class="footer">
                <div class="container-fluid" id="footer"></div>
            </footer>
        </div>
    </body>
</html>