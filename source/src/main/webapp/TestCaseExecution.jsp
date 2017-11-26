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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta content="text/html; charset=UTF-8" http-equiv="content-type">
        <%@ include file="include/global/dependenciesInclusions.html" %>
        <title id="pageTitle">Execution Detail</title>        
        <script type="text/javascript" src="dependencies/Tinymce-4.2.6/tinymce.min.js"></script>
        <script type="text/javascript" src="js/pages/TestCaseExecution.js"></script>
        <script type="text/javascript" src="js/transversalobject/TestCaseExecutionQueue.js"></script>
        <script type="text/javascript" src="js/transversalobject/TestCase.js"></script>
        <link rel="stylesheet" type="text/css" href="css/pages/TestCaseExecution.css">
        <link rel="stylesheet" type="text/css" href="dependencies/Bootstrap-inversebutton/inversebutton.css">
    </head>
    <body>
        <%@ include file="include/global/header.html"%>
        <%@ include file="include/utils/modal-generic.html"%>
        <%@ include file="include/pages/testcasescript/manageProperties.html"%>
        <div id="page-layout" class="container-fluid center">
            <%@ include file="include/global/messagesArea.html"%>
            <%@ include file="include/transversalobject/TestCaseExecutionQueue.html"%>
            <%@ include file="include/transversalobject/TestCase.html"%>
            <h1 class="page-title-line">Execution Detail</h1>
            <div class="panel panel-default" id="testCaseConfig">
                <div>
                    <div id="divPanelDefault" class="panel-default" style="z-index:10; top: 0">
                        <div class="panel-heading" id="executionHeader"  style="z-index:2; top: 0">
                            <div class="progress">
                                <div id="progress-bar" class="progress-bar" role="progressbar" aria-valuenow="0" aria-valuemin="0" aria-valuemax="100">
                                    <span class="sr-only"></span>
                                </div>
                            </div>
                            <div class="col-lg-6 pull-left">
                                <div class="">
                                    <span id="idlabel"></span>
                                    <span> - </span>
                                    <span id="test"></span>
                                    <span> - </span>
                                    <span id="testcase"></span>
                                    <span> - </span>
                                    <span id="country"></span>
                                    <span> - </span>
                                    <span id="environment"></span>
                                    <span> - </span>
                                    <span id="controlstatus" style="font-weight: 900"></span>
                                </div>
                                <div class="">
                                    <span id="tcDescription" style="font-size:.9em;margin:0px;line-height:1;height:.95em;">Descr</span>
                                </div>
                                <div class="">
                                    <span id="exReturnMessage" style="font-size:.9em;margin:0px;line-height:1;height:.95em;">Descr</span>
                                </div>
                            </div>
                            <div class="col-lg-6" style="padding: 0px;">
                                <div id="RefreshQueueButton">
                                    <button id="editTag" class="btn btn-default">Refresh</button>
                                </div>
                                <div id="TestCaseButton">
                                    <a href="#" class="btn btn-default pull-right" id="saveTestCaseExecution" data-toggle="tooltip" style="margin-left: 1px; display: none;"><span class="glyphicon glyphicon-save"></span> Save</a>
                                    <div class="btn-group pull-right">
                                        <a href="#" class="btn btn-default" id="runTestCase" data-toggle="tooltip" style="margin-left: 1px;"><span class="glyphicon glyphicon-fast-backward"></span> Run</a>
                                        <a type="button" class="btn btn-default dropdown-toggle"  data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                                            <span class="caret"></span>
                                            <span class="sr-only">Toggle Dropdown</span>
                                        </a>
                                        <ul class="dropdown-menu">
                                            <li><a href="#" class="btn btn-default" id="ExecutionQueueDup"><span class="glyphicon glyphicon-tasks"></span> Duplicate new Execution</a></li>
                                        </ul>
                                    </div>
                                    <div class="btn-group pull-right">
                                        <a href="#" class="btn btn-default pull-left" id="lastExecution" style="margin-left: 1px"><span class="glyphicon glyphicon-fast-backward"></span> Last Executions</a>
                                        <a type="button" class="btn btn-default dropdown-toggle"  data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                                            <span class="caret"></span>
                                            <span class="sr-only">Toggle Dropdown</span>
                                        </a>
                                        <ul class="dropdown-menu">
                                            <li><a href="#" class="btn btn-default" id="lastExecutionwithEnvCountry"><span class="glyphicon glyphicon-fast-backward"></span> Last Executions With Country Env</a></li>
                                            <li><a href="#" class="btn btn-default" id="ExecutionByTag" style="margin-left: 1px; margin-right: 1px;"><span class="glyphicon glyphicon-tasks"></span> See Execution By Tag</a></li>
                                            <li><a href="#" class="btn btn-default" id="ExecutionQueue"><span class="glyphicon glyphicon-tasks"></span> See Queue Parameters</a></li>
                                            <li><a href="#" class="btn btn-default" id="ExecutionQueueByTag"><span class="glyphicon glyphicon-tasks"></span> See Queue By Tag</a></li>
                                        </ul>
                                    </div>
                                    <div class="btn-group pull-right">
                                        <a href="#" class="btn btn-default pull-left" id="editTcInfo" ><span class="glyphicon glyphicon-pencil"></span> Edit Test Case</a>
                                        <a type="button" id="editTcToggleButton" class="btn btn-default dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                                            <span class="caret"></span>
                                            <span class="sr-only">Toggle Dropdown</span>
                                        </a>
                                        <ul class="dropdown-menu">
                                            <li><a href="#" class="btn btn-default" id="editTcStepInfo"><span class="glyphicon glyphicon-pencil"></span> Edit Test Case from the current Step</a></li>
                                            <li><a href="#" class="btn btn-default" id="editTcHeader"><span class="glyphicon glyphicon-pencil"></span> Edit Test Case Header</a></li>
                                        </ul>
                                    </div>

                                    <div class="side-item pull-right"></div>

                                </div>
                            </div>
                            <div class="clearfix"></div>
                        </div>
                        <div id="NavtabsScriptEdit" background-color: white">
                             <ul id="tabsScriptEdit" class="nav nav-tabs" data-tabs="tabs">
                                <li class="active"><a data-toggle="tab" href="#tabSteps" id="editTabStep" name="tabSteps">Steps</a></li>
                                <li><a data-toggle="tab" href="#tabProperties" id="editTabProperties" name="tabProperties">Properties</a></li>
                                <li><a data-toggle="tab" href="#tabDetail" id="editTabDetail" name="tabDetail">Execution Details</a></li>
                                <li><a data-toggle="tab" href="#tabEnv" id="editTabEnv" name="tabEnv">Environment</a></li>
                                <li><a data-toggle="tab" href="#tabRobot" id="editTabRobot" name="tabRobot">Robot</a></li>
                            </ul>
                        </div>
                    </div>
                </div>
                <div class="panel-body" id="testCaseDetails">
                    <div class="tab-content">
                        <div class="center marginTop25 tab-pane fade in active" id="tabSteps">
                            <div id="handler" class="row" style="margin: 0px; margin-top: 10px;">
                                <nav class="col-lg-3" id="nav-execution" style="z-index:1;">
                                    <div id="list-wrapper" style="top:107px;">
                                        <div id="steps">
                                            <ul class="list-group step-list side-item" id="stepList" style="max-height: 500px;overflow-y: auto"></ul>
                                        </div>
                                    </div>
                                </nav>
                                <div class="col-lg-9 well marginTop5" id="contentWrapper" style="min-height: 200px;">
                                    <div id="stepContent">
                                        <div class="row step">
                                            <div class="content col-lg-12">
                                                <div  id="stepHeader" style="margin-bottom: 15px;">
                                                    <div id="stepInfo" class="row" style="display: none;">
                                                    </div>
                                                </div>
                                                <div class="fieldRow marginTop25" id="stepHiddenRow" style="display: none;">
                                                    <div class="row">
                                                        <div class="col-lg-2 form-group">
                                                            <label>Return Code</label>
                                                            <input class="form-control input-sm" readonly id="stepRC">
                                                        </div>
                                                        <div class="col-lg-10 form-group">
                                                            <label>Description</label>
                                                            <input class="form-control input-sm" readonly id="stepDescription">
                                                        </div>
                                                    </div>
                                                    <div class="row">
                                                        <div class="col-lg-1 form-group">
                                                            <label>Sort</label>
                                                            <input class="form-control input-sm" readonly id="stepSort">
                                                        </div>
                                                        <div class="col-lg-1 form-group">
                                                            <label>Index</label>
                                                            <input class="form-control input-sm" readonly id="stepIndex">
                                                        </div>
                                                        <div class="col-lg-5 form-group">
                                                            <label>Loop</label>
                                                            <input class="form-control input-sm" readonly id="stepLoop">
                                                        </div>
                                                        <div class="col-lg-5 form-group">
                                                            <label>Time elapsed</label>
                                                            <input class="form-control input-sm" readonly id="stepElapsed">
                                                        </div>
                                                    </div>
                                                    <div class="row">
                                                        <div class="col-lg-2 form-group">
                                                        </div>
                                                        <div class="col-lg-5 form-group">
                                                            <label>Param1 Init</label>
                                                            <input class="form-control input-sm" readonly id="stepConditionVal1Init">
                                                        </div>
                                                        <div class="col-lg-5 form-group">
                                                            <label>Param2 Init</label>
                                                            <input class="form-control input-sm" readonly id="stepConditionVal2Init">
                                                        </div>
                                                    </div>
                                                    <div class="row">
                                                        <div class="col-lg-2 form-group">
                                                            <label>Condition Operation</label>
                                                            <input class="form-control input-sm" readonly id="stepConditionOper">
                                                        </div>
                                                        <div class="col-lg-5 form-group">
                                                            <label>Param1</label>
                                                            <input class="form-control input-sm" readonly id="stepConditionVal1">
                                                        </div>
                                                        <div class="col-lg-5 form-group">
                                                            <label>Param2</label>
                                                            <input class="form-control input-sm" readonly id="stepConditionVal2">
                                                        </div>
                                                    </div>
                                                    <div class="row">
                                                        <div class="col-lg-12 form-group">
                                                            <label>Return Message</label>
                                                            <input class="form-control input-sm" readonly id="stepMessage">
                                                        </div>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                    <div id="actionContainer"></div>
                                </div>
                            </div>
                        </div>
                        <div class="center marginTop25 tab-pane fade" id="tabProperties">
                            <div id="propertiesModal">
                                <div class="property-table">
                                    <div class="" id="propPanelWrapper">
                                        <div class="panel-body collapse in" id="propertiesPanel">
                                            <div id="propTable" class="list-group">
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div class="center marginTop25 tab-pane fade" id="tabDetail">
                            <div class="row">
                                <div class="col-sm-2">
                                    <div class="form-group">
                                        <label for="controlstatus2">Control Status</label>
                                        <input type="text" class="form-control" id="controlstatus2" placeholder="Control Status" readonly>
                                    </div>
                                </div>
                                <div class="col-sm-2">
                                    <div class="form-group">
                                        <label for="id">ID</label>
                                        <input type="text" class="form-control" id="id" placeholder="ID" readonly>
                                    </div>
                                </div>
                                <div class="col-sm-8">
                                    <div class="form-group">
                                        <label for="controlmessage">Control Message</label>
                                        <input type="text" class="form-control" id="controlmessage" placeholder="Control Message" readonly>
                                    </div>
                                </div>
                            </div>
                            <div class="row">
                                <div class="col-sm-3">
                                    <div class="form-group">
                                        <label for="start">Start</label>
                                        <input type="text" class="form-control" id="start" placeholder="Start" readonly>
                                    </div>
                                </div>
                                <div class="col-sm-3">
                                    <div class="form-group">
                                        <label for="end">End</label>
                                        <input type="text" class="form-control" id="end" placeholder="End" readonly>
                                    </div>
                                </div>
                                <div class="col-sm-3">
                                    <div class="form-group">
                                        <label for="executor">Executor</label>
                                        <input type="text" class="form-control" id="executor" placeholder="Executor" readonly>
                                    </div>
                                </div>
                                <div class="col-sm-3">
                                    <div class="form-group">
                                        <label for="status">Status</label>
                                        <input type="text" class="form-control" id="status" placeholder="Status" readonly>
                                    </div>
                                </div> 
                            </div>
                            <div class="row">
                                <div class="col-sm-4">
                                    <div class="form-group">
                                        <label for="cerberusversion">Cerberus Version</label>
                                        <input type="text" class="form-control" id="cerberusversion" placeholder="Cerberus Version" readonly>
                                    </div>
                                </div>
                                <div class="col-sm-4">
                                    <div class="form-group">
                                        <label>Bug ID</label>
                                        <div id="bugID"></div>
                                    </div>
                                </div>
                            </div>
                            <div class="row">
                                <div class="col-sm-8">
                                    <div class="form-group">
                                        <label for="tag">Tag</label>
                                        <div class="input-group">
                                            <input type="text" class="form-control" id="tag" placeholder="Tag" readonly>
                                            <span class="input-group-btn">
                                                <button id="editTag" class="btn btn-default">Edit</button>
                                                <button id="saveTag" class="btn btn-primary" style="display : none;">Save</button>
                                            </span>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <div class="row">
                                <div class="col-sm-2">
                                    <div class="form-group">
                                    </div>
                                </div>
                                <div class="col-sm-5">
                                    <div class="form-group">
                                        <label for="conditionVal1InitTC">conditionVal1InitTC</label>
                                        <input type="text" class="form-control" id="conditionVal1InitTC" readonly>
                                    </div>
                                </div>
                                <div class="col-sm-5">
                                    <div class="form-group">
                                        <label for="conditionVal2InitTC">conditionVal2InitTC</label>
                                        <input type="text" class="form-control" id="conditionVal2InitTC" readonly>
                                    </div>
                                </div>
                            </div>
                            <div class="row">
                                <div class="col-sm-2">
                                    <div class="form-group">
                                        <label for="conditionOperTC">conditionOperTC</label>
                                        <input type="text" class="form-control" id="conditionOperTC" readonly>
                                    </div>
                                </div>
                                <div class="col-sm-5">
                                    <div class="form-group">
                                        <label for="conditionVal1TC">conditionVal1TC</label>
                                        <input type="text" class="form-control" id="conditionVal1TC" readonly>
                                    </div>
                                </div>
                                <div class="col-sm-5">
                                    <div class="form-group">
                                        <label for="conditionVal2TC">conditionVal2TC</label>
                                        <input type="text" class="form-control" id="conditionVal1TC" readonly>
                                    </div>
                                </div>
                            </div>


                        </div>
                        <div class="center marginTop25 tab-pane fade" id="tabRobot">
                            <div class="row">
                                <div class="col-sm-3">
                                    <div class="form-group">
                                        <label for="ip">IP</label>
                                        <input type="text" class="form-control" id="ip" placeholder="IP" readonly>
                                    </div>
                                </div>
                                <div class="col-sm-3">
                                    <div class="form-group">
                                        <label for="port">Port</label>
                                        <input type="text" class="form-control" id="port" placeholder="Port" readonly>
                                    </div>
                                </div>
                            </div>
                            <div class="row">
                                <div class="col-sm-3">
                                    <div class="form-group">
                                        <label for="platform">Platform</label>
                                        <input type="text" class="form-control" id="platform" placeholder="Platform" readonly>
                                    </div>
                                </div>
                                <div class="col-sm-3">
                                    <div class="form-group">
                                        <label for="browser">Browser</label>
                                        <input type="text" class="form-control" id="browser" placeholder="Browser" readonly>
                                    </div>
                                </div>
                                <div class="col-sm-3">
                                    <div class="form-group">
                                        <label for="version">Version</label>
                                        <input type="text" class="form-control" id="version" placeholder="Version" readonly>
                                    </div>
                                </div>
                                <div class="col-sm-3">
                                    <div class="form-group">
                                        <label for="browserfull">Browser Full Version</label>
                                        <input type="text" class="form-control" id="browserfull" placeholder="Browser Full Version" readonly>
                                    </div>
                                </div>
                            </div>
                            <div class="row">
                                <div class="col-sm-3">
                                    <div class="form-group">
                                        <label for="userAgent">User Agent</label>
                                        <input type="text" class="form-control" id="userAgent" placeholder="userAgent" readonly>
                                    </div>
                                </div>
                                <div class="col-sm-3">
                                    <div class="form-group">
                                        <label for="screenSize">Screen Size</label>
                                        <input type="text" class="form-control" id="screenSize" placeholder="Screen Size" readonly>
                                    </div>
                                </div>
                                <div class="col-sm-3">
                                    <div class="form-group">
                                        <label for="verbose">Verbose</label>
                                        <input type="text" class="form-control" id="verbose" placeholder="Verbose" readonly>
                                    </div>
                                </div>
                            </div>
                            <div class="row">
                                <div class="col-sm-6" id="tcFileContentField">
                                </div>
                            </div>
                        </div>
                        <div class="center marginTop25 tab-pane fade" id="tabEnv">
                            <div class="row">
                                <div class="col-sm-3">
                                    <div class="form-group">
                                        <label for="application">Application</label>
                                        <input type="text" class="form-control" id="application" placeholder="Application" readonly>
                                    </div>
                                </div>
                                <div class="col-sm-3">
                                    <div class="form-group">
                                        <label for="country">Country</label>
                                        <input type="text" class="form-control" id="country" placeholder="Country" readonly>
                                    </div>
                                </div>
                                <div class="col-sm-2">
                                    <div class="form-group">
                                        <label for="environment">Environment</label>
                                        <input type="text" class="form-control" id="environment" placeholder="Environment" readonly>
                                    </div>
                                </div>
                                <div class="col-sm-2">
                                    <div class="form-group">
                                        <label for="environmentData">Environment Data</label>
                                        <input type="text" class="form-control" id="environmentData" placeholder="Environment Data" readonly>
                                    </div>
                                </div>
                            </div>
                            <div class="row">
                                <div class="col-sm-3">
                                    <div class="form-group">
                                        <label for="build">Build</label>
                                        <input type="text" class="form-control" id="build" placeholder="Build" readonly>
                                    </div>
                                </div>
                                <div class="col-sm-3">
                                    <div class="form-group">
                                        <label for="revision">Revision</label>
                                        <input type="text" class="form-control" id="revision" placeholder="Revision" readonly>
                                    </div>
                                </div>
                                <div class="col-sm-6">
                                    <div class="form-group">
                                        <label for="url">URL</label>
                                        <input type="text" class="form-control" id="url" placeholder="URL" readonly>
                                    </div>
                                </div>
                            </div>
                        </div>

                    </div>

                </div>
            </div>

            <footer class="footer">
                <div id="footer" style="display: inline-block"></div>
            </footer>
        </div>
    </div>
</body>
</html>
