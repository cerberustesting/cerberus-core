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
        <script type="text/javascript" src="dependencies/Bootstrap-treeview-1.2.0/js/bootstrap-treeview.js"></script>
        <script type="text/javascript" src="dependencies/Chart.js-2.9.3/Chart.min.js"></script>
        <script type="text/javascript" src="js/pages/TestCaseExecution.js"></script>
        <script type="text/javascript" src="js/transversalobject/TestCaseExecutionQueue.js"></script>
        <script type="text/javascript" src="js/transversalobject/TestCase.js"></script>
        <script type="text/javascript" src="js/transversalobject/File.js"></script>
        <link rel="stylesheet" type="text/css" href="css/pages/TestCaseExecution.css">
    </head>
    <body>
        <%@ include file="include/global/header.html"%>
        <%@ include file="include/utils/modal-generic.html"%>
        <%@ include file="include/pages/testcasescript/manageProperties.html"%>
        <div id="page-layout" class="container-fluid center">
            <%@ include file="include/global/messagesArea.html"%>
            <%@ include file="include/transversalobject/TestCaseExecutionQueue.html"%>
            <%@ include file="include/transversalobject/TestCase.html"%>
            <%@ include file="include/transversalobject/File.html"%>
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
                            <div class="row">
                                <div class="col-lg-7 pull-left">
                                    <div class="text-nowrap">
                                        <span id="idlabel"></span>
                                        <span name="Separator">Loading...</span>
                                        <span id="test"></span>
                                        <span name="Separator"> </span>
                                        <span id="testcase"></span>
                                        <span name="Separator"> </span>
                                        <span id="country"></span>
                                        <span name="Separator"> </span>
                                        <span id="environment"></span>
                                        <span name="Separator"> </span>
                                        <span id="controlstatus" style="font-weight: 900"></span>
                                    </div>
                                    <div class="text-wrap">
                                        <span id="tcDescription" style="font-size:.9em;margin:0px;line-height:1;height:.95em;"></span>
                                    </div>
                                    <div class="text-wrap" id="returnMessage">
                                        <span id="exReturnMessage" style="font-size:.9em;margin:0px;line-height:1;height:.95em;font-weight: 900;word-wrap: break-word"></span>
                                    </div>
                                </div>
                            <div class="col-lg-5" style="padding: 0px;">
                                    <div id="RefreshQueueButton">
                                        <button id="refreshQueue" class="btn btn-default">Refresh</button>
                                        <button id="editQueue" class="btn btn-default">Edit Queue Entry</button>
                                    </div>
                                    <div id="TestCaseButton">

                                        <div class="btn-group pull-right" role="group" aria-label="Button group with nested dropdown" style="margin-top: 10px;">

                                            <div class="btn-group ">
                                                <button id="btnGroupDrop1" type="button" class="btn btn-secondary dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                                                    Go To <span class="caret"></span>
                                                </button>
                                                <div class="dropdown-menu" aria-labelledby="btnGroupDrop1">
                                                    <a><button class="btn btn-default pull-left" id="lastExecution" style="margin-left: 5px; margin-right: 5px;"><span class="glyphicon glyphicon-list"></span> Last Executions</button></a>
                                                    <a><button class="btn btn-default pull-left" id="lastExecutionoT" style="margin-left: 5px; margin-right: 5px;"><span class="glyphicon glyphicon-list"></span> Reporting over Time</button></a>
                                                    <a><button class="btn btn-default pull-left" id="lastExecutionwithEnvCountry" style="margin-left: 5px; margin-right: 5px;"><span class="glyphicon glyphicon-list"></span> Last Executions With Country Env</button></a>
                                                    <!--                                                <a><button class="btn btn-default pull-left" id="lastExecutionoTwithEnvCountry" style="margin-left: 5px; margin-right: 5px;"><span class="glyphicon glyphicon-list"></span> Reporting over time With Country Env</button></a>-->
                                                    <a><button class="btn btn-default pull-left" id="ExecutionByTag" style="margin-left: 5px; margin-right: 5px;"><span class="glyphicon glyphicon-tasks"></span> Campaign Report</button></a>
                                                    <a><button class="btn btn-default pull-left" id="ExecutionQueue" style="margin-left: 5px; margin-right: 5px;"><span class="glyphicon glyphicon-eye-open"></span> See Queue Parameters</button></a>
                                                    <a><button class="btn btn-default pull-left" id="ExecutionQueueByTag" style="margin-left: 5px; margin-right: 5px;"><span class="glyphicon glyphicon-list"></span> See Queue By Tag</button></a>
                                                    <a><button class="btn btn-default pull-left" id="sessionLinkHeader" style="margin-left: 5px; margin-right: 5px;">Link External Provider</button></a>
                                                </div>
                                            </div>

                                            <div class="btn-group">
                                                <button id="btnGroupDrop2" type="button" class="btn btn-secondary dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                                                    Run <span class="caret"></span>
                                                </button>
                                                <div class="dropdown-menu" aria-labelledby="btnGroupDrop2">
                                                    <a><button class="btn btn-default pull-left" id="runTestCase" style="margin-left: 5px; margin-left: 5px;" data-toggle="tooltip" ><span class="glyphicon glyphicon-play"></span>Run</button></a>
                                                    <a><button class="btn btn-default pull-left" id="rerunTestCase" style="margin-left: 5px; margin-left: 5px;"><span class="glyphicon glyphicon-forward"></span>ReRun</button></a>
                                                    <a><button class="btn btn-default pull-left" id="rerunFromQueue" style="margin-left: 5px; margin-left: 5px;"><span class="glyphicon glyphicon-forward"></span>From Queue</button></a>
                                                    <a><button class="btn btn-default pull-left" id="rerunFromQueueandSee" style="margin-left: 5px; margin-left: 5px;"><span class="glyphicon glyphicon-forward"></span>From Queue & See</button></a>
                                                </div>
                                            </div>

                                            <div class="btn-group">
                                                <a type="button" class="btn btn-default" id="editTcInfo" ><span class="glyphicon glyphicon-new-window"></span> Edit Test Case</a>
                                                <button id="btnGroupDrop4" type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                                                    <span class="caret"></span>
                                                    <span class="sr-only">Toggle Dropdown</span>
                                                </button>
                                                <div class="dropdown-menu">
                                                    <a><button class="btn btn-default" id="editTcStepInfo" style="margin-left: 5px; margin-left: 5px;" ><span class="glyphicon glyphicon-new-window"></span> Edit Test Case from the current Step</button></a>
                                                    <a><button class="btn btn-default" id="editTcHeader" style="margin-left: 5px; margin-left: 5px;" ><span class="glyphicon glyphicon-pencil"></span> Edit Test Case Header</button></a>
                                                </div>
                                            </div>


                                            <div class="btn-group ">
                                                <button class="btn btn-default" id="saveTestCaseExecution" disabled style="margin-left: 1px;"><span class="glyphicon glyphicon-save"></span> Save</button>
                                                <button id="btnGroupDrop3" type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false" disabled="">
                                                    <span class="caret"></span>
                                                    <span class="sr-only">Toggle Dropdown</span>
                                                </button>
                                                <div class="dropdown-menu"  aria-labelledby="btnGroupDrop3">
                                                    <a><button class="btn btn-default pull-left" id="deleteTestCaseExecution" style="margin-left: 5px; margin-left: 5px;" disabled><span class="glyphicon glyphicon-trash"></span>Delete</button></a>
                                                </div>
                                            </div>

                                        </div>

                                        <div class="side-item pull-right"></div>

                                    </div>
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
                                <li><a data-toggle="tab" href="#tabDep" id="editTabDep" name="tabDep">Dependencies</a></li>
                                <li><a data-toggle="tab" href="#tabNetwork" id="editTabNetwork" name="tabNetwork" style="display: none;">Network</a></li>
                                <li><a data-toggle="tab" href="#tabTraca" id="editTabTraca" name="tabTraca">Traceability</a></li>
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
                                            <ul class="list-group step-list side-item" id="steps" style="max-height: 500px;overflow-y: auto"></ul>
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
                                                    <div class="row" id="stepRow1" >
                                                        <div class="col-lg-2 form-group">
                                                            <label>Return Code</label>
                                                            <input class="form-control input-sm" readonly id="stepRC">
                                                        </div>
                                                        <div class="col-lg-10 form-group">
                                                            <label>Description</label>
                                                            <input class="form-control input-sm" readonly id="stepDescription">
                                                        </div>
                                                    </div>
                                                    <div class="row" id="stepRow2">
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
                                                    <div class="row" id="stepRow3">
                                                        <div class="col-lg-2">
                                                        </div>
                                                        <div class="col-lg-4 form-group">
                                                            <label>Param1 Init</label>
                                                            <input class="form-control input-sm" readonly id="stepConditionVal1Init">
                                                        </div>
                                                        <div class="col-lg-4 form-group">
                                                            <label>Param2 Init</label>
                                                            <input class="form-control input-sm" readonly id="stepConditionVal2Init">
                                                        </div>
                                                        <div class="col-lg-2 form-group">
                                                            <label>Param3 Init</label>
                                                            <input class="form-control input-sm" readonly id="stepConditionVal3Init">
                                                        </div>
                                                    </div>
                                                    <div class="row" id="stepRow4">
                                                        <div class="col-lg-2 form-group">
                                                            <label>Condition Operation</label>
                                                            <input class="form-control input-sm" readonly id="stepConditionOperator">
                                                        </div>
                                                        <div class="col-lg-4 form-group">
                                                            <label>Param1</label>
                                                            <input class="form-control input-sm" readonly id="stepConditionVal1">
                                                        </div>
                                                        <div class="col-lg-4 form-group">
                                                            <label>Param2</label>
                                                            <input class="form-control input-sm" readonly id="stepConditionVal2">
                                                        </div>
                                                        <div class="col-lg-2 form-group">
                                                            <label>Param3</label>
                                                            <input class="form-control input-sm" readonly id="stepConditionVal3">
                                                        </div>
                                                    </div>
                                                    <div class="row" id="stepRow5">
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
                                            <div class="row" id="secondaryPropTableHeader" class="list-group" style="display:none;">
                                                <div class="col-sm-6">
                                                    <button id="showSecondaryProp" type="button" class="btn btn-default center-block"><span class="glyphicon glyphicon-collapse-down"></span> Show <span id="secondaryPropCount"></span> Secondary Properties</button>
                                                </div>
                                                <div class="col-sm-6">
                                                    <button id="hideSecondaryProp" type="button" class="btn btn-default center-block"><span class="glyphicon glyphicon-collapse-up"></span> Hide Secondary Properties</button>
                                                </div>
                                            </div>
                                            <div id="secondaryPropTable" style="margin-top:20px;">
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
                                        <label for="id">ID</label>
                                        <input type="text" class="form-control" id="id" placeholder="ID" readonly>
                                    </div>
                                </div>
                                <div class="col-sm-2">
                                    <div class="form-group">
                                        <label for="controlstatus2">Control Status</label>
                                        <input type="text" class="form-control" id="controlstatus2" placeholder="Control Status" readonly>
                                    </div>
                                </div>
                                <div class="col-sm-8">
                                    <div class="form-group">
                                        <label for="controlmessage">Control Message</label>
                                        <textarea class="form-control" id="controlmessage" readonly></textarea>
                                    </div>
                                </div>
                            </div>
                            <div class="row">
                                <div class="col-sm-4">
                                    <div class="form-group">
                                        <label>Bugs</label>
                                        <div id="bugs"></div>
                                    </div>
                                </div>

                                <div class="col-sm-8">
                                    <div class="form-group">
                                        <label for="tag">Tag</label>
                                        <div class="input-group">
                                            <input type="text" class="form-control" id="tag" placeholder="Tag" readonly>
                                            <span class="input-group-btn">
                                                <button id="editTags" class="btn btn-default">Edit</button>
                                                <button id="saveTag" class="btn btn-primary" style="display : none;">Save</button>
                                            </span>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <div class="row">
                                <div class="col-sm-4">
                                    <div class="form-group">
                                        <label for="start">Start</label>
                                        <input type="text" class="form-control" id="start" placeholder="Start" readonly>
                                    </div>
                                </div>
                                <div class="col-sm-4">
                                    <div class="form-group">
                                        <label for="end">End</label>
                                        <input type="text" class="form-control" id="end" placeholder="End" readonly>
                                    </div>
                                </div>
                                <div class="col-sm-2">
                                    <div class="form-group">
                                        <label for="executor">Executor</label>
                                        <input type="text" class="form-control" id="executor" placeholder="Executor" readonly>
                                    </div>
                                </div>
                                <div class="col-sm-2">
                                    <div class="form-group">
                                        <label for="cerberusversion">Cerberus Version</label>
                                        <input type="text" class="form-control" id="cerberusversion" placeholder="Cerberus Version" readonly>
                                    </div>
                                </div>
                            </div>

                            <div class="row">
                                <div class="col-sm-4">
                                    <div class="form-group">
                                        <label for="exetest">Test</label>
                                        <input type="text" class="form-control" id="exetest"  readonly>
                                    </div>
                                </div>
                                <div class="col-sm-3">
                                    <div class="form-group">
                                        <label for="exetestcase">TestCase</label>
                                        <input type="text" class="form-control" id="exetestcase" readonly>
                                    </div>
                                </div>
                                <div class="col-sm-2">
                                    <div class="form-group">
                                        <label for="testcaseversion">Version</label>
                                        <input type="text" class="form-control" id="testcaseversion" readonly>
                                    </div>
                                </div>
                                <div class="col-sm-3">
                                    <div class="form-group">
                                        <label for="status">Status</label>
                                        <input type="text" class="form-control" id="status" readonly>
                                    </div>
                                </div>
                            </div>

                            <div class="row">
                            </div>

                            <div class="row" id="condrow1">
                                <div class="col-sm-2">
                                    <div class="form-group">
                                    </div>
                                </div>
                                <div class="col-sm-4">
                                    <div class="form-group">
                                        <label for="conditionVal1InitTC">conditionVal1InitTC</label>
                                        <input type="text" class="form-control" id="conditionVal1InitTC" readonly>
                                    </div>
                                </div>
                                <div class="col-sm-4">
                                    <div class="form-group">
                                        <label for="conditionVal2InitTC">conditionVal2InitTC</label>
                                        <input type="text" class="form-control" id="conditionVal2InitTC" readonly>
                                    </div>
                                </div>
                                <div class="col-sm-2">
                                    <div class="form-group">
                                        <label for="conditionVal3InitTC">conditionVal3InitTC</label>
                                        <input type="text" class="form-control" id="conditionVal3InitTC" readonly>
                                    </div>
                                </div>
                            </div>
                            <div class="row" id="condrow2">
                                <div class="col-sm-2">
                                    <div class="form-group">
                                        <label for="conditionOperatorTC">conditionOperatorTC</label>
                                        <input type="text" class="form-control" id="conditionOperatorTC" readonly>
                                    </div>
                                </div>
                                <div class="col-sm-4">
                                    <div class="form-group">
                                        <label for="conditionVal1TC">conditionVal1TC</label>
                                        <input type="text" class="form-control" id="conditionVal1TC" readonly>
                                    </div>
                                </div>
                                <div class="col-sm-4">
                                    <div class="form-group">
                                        <label for="conditionVal2TC">conditionVal2TC</label>
                                        <input type="text" class="form-control" id="conditionVal1TC" readonly>
                                    </div>
                                </div>
                                <div class="col-sm-2">
                                    <div class="form-group">
                                        <label for="conditionVal3TC">conditionVal3TC</label>
                                        <input type="text" class="form-control" id="conditionVal3TC" readonly>
                                    </div>
                                </div>
                            </div>
                            <div class="row">
                                <div class="col-sm-2">
                                    <div class="form-group">
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div class="center marginTop25 tab-pane fade" id="tabRobot">
                            <div class="row">
                                <div class="col-sm-3">
                                    <div class="form-group">
                                        <label for="robot">Robot</label>
                                        <input type="text" class="form-control" id="robot" placeholder="Robot" readonly>
                                    </div>
                                </div>
                                <div class="col-sm-3">
                                    <div class="form-group">
                                        <label for="robotdecli">Robot Declination</label>
                                        <input type="text" class="form-control" id="robotdecli" readonly>
                                    </div>
                                </div>
                            </div>
                            <div class="row">
                                <div class="col-sm-3">
                                    <div class="form-group">
                                        <label for="robotexe">Robot Executor</label>
                                        <input type="text" class="form-control" id="robotexe" placeholder="Robot Executor" readonly>
                                    </div>
                                </div>
                                <div class="col-sm-3">
                                    <div class="form-group">
                                        <label for="robothost">Robot Host</label>
                                        <input type="text" class="form-control" id="robothost" placeholder="Robot Host" readonly>
                                    </div>
                                </div>
                                <div class="col-sm-2">
                                    <div class="form-group">
                                        <label for="robotport">Robot Port</label>
                                        <input type="text" class="form-control" id="robotport" placeholder="Robot Port" readonly>
                                    </div>
                                </div>
                                <div class="col-sm-3">
                                    <div class="form-group">
                                        <label for="robotsessionid">Robot Session</label>
                                        <input type="text" class="form-control" id="robotsessionid" placeholder="Robot Session Id" readonly>
                                    </div>
                                </div>
                                <div class="col-sm-1">
                                    <div class="form-group">
                                        <label for="sessionLink"></label>
                                        <a><button class="btn btn-default pull-left" id="sessionLink" style="margin-left: 5px; margin-right: 5px;">Link External Provider</button></a>
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
                            </div>
                            <div class="row">
                                <div class="col-sm-6">
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
                            </div>
                            <div class="row">
                                <div class="col-sm-6" id="tcFileContentField">
                                </div>
                            </div>
                        </div>

                        <div class="center marginTop25 tab-pane fade" id="tabEnv">
                            <div class="row">
                                <div class="col-sm-2">
                                    <div class="form-group">
                                        <label for="system">System</label>
                                        <input type="text" class="form-control" id="system" readonly>
                                    </div>
                                </div>
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

                        <div class="center marginTop25 tab-pane fade" id="tabDep">
                            <div id="listPanelDep">
                                <div class="row">
                                    <div class="col-sm-12">
                                        <table class="table table-bordered table-hover nomarginbottom" id="depTable">
                                            <tbody id="depTableBody">
                                            </tbody>
                                        </table>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div class="center marginTop25 tab-pane fade" id="tabNetwork">

                            <div class="panel panel-default" id="NS1Panel">
                                <div class="panel-heading card" data-toggle="collapse" data-target="#NS1">
                                    <span class="fa fa-pie-chart fa-fw"></span>
                                    <label id="ns1Label">Global Statistics</label>
                                    <span class="toggle glyphicon glyphicon-chevron-right pull-right"></span>
                                </div>
                                <div class="panel-body collapse in" id="NS1">
                                    <div class="row">
                                        <div class="col-sm-6">
                                            <canvas id="myChart1"></canvas>
                                        </div>
                                        <div class="col-sm-6">
                                            <canvas id="myChart2"></canvas>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <div class="panel panel-default" id="NS2Panel">
                                <div class="panel-heading card" data-toggle="collapse" data-target="#NS2">
                                    <span class="fa fa-pie-chart fa-fw"></span>
                                    <label id="ns2Label">Statistics Per Third Party</label>
                                    <span class="toggle glyphicon glyphicon-chevron-right pull-right"></span>
                                </div>
                                <div class="panel-body collapse in" id="NS2">

                                    <div class="row" >
                                        <div class="col-sm-1" id="radioSort">
                                            <label id="ns2Label"><span class="glyphicon glyphicon-sort"></span> Sort Third Party</label>
                                            <button class="btn btn-default pull-left" id="sortSize" style="margin-left: 5px; margin-right: 5px;">by Size</button>
                                            <button class="btn btn-default pull-left" id="sortRequest" style="margin-left: 5px; margin-right: 5px;">by Request</button>
                                            <button class="btn btn-default pull-left" id="sortTime" style="margin-left: 5px; margin-right: 5px;">by Max Time</button>
                                        </div>
                                        <div class="col-sm-11">
                                            <canvas id="myChart3"></canvas>
                                        </div>
                                    </div>

                                    <div class="row" id="detailUnknown">
                                        <ul class="list-group marginTop25" id="detailUnknownList">
                                        </ul>
                                    </div>
                                    <div class="row">
                                        <div class="col-sm-12">
                                            <canvas id="myChart4"></canvas>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <div class="panel panel-default" id="NS3Panel">
                                <div class="panel-heading card" data-toggle="collapse" data-target="#NS3">
                                    <span class="fa fa-pie-chart fa-fw"></span>
                                    <label id="ns3Label">Requests List</label>
                                    <span class="toggle glyphicon glyphicon-chevron-right pull-right"></span>
                                </div>
                                <div class="panel-body collapse in" id="NS3">
                                    <table id="requestTable" class="table table-bordered table-hover display" name="requestTable"></table>
                                </div>
                            </div>

                        </div>

                        <div class="center marginTop25 tab-pane fade" id="tabTraca">
                            <div id="listPanelTraca">
                                <div class="row">
                                    <div class="form-group col-sm-6">
                                        <label name="lbl_datecreated" for="datecreated">datecreated</label>
                                        <div>
                                            <input id="datecreated" name="datecreated" class="form-control" readonly="readonly" />
                                        </div>
                                    </div>
                                    <div class="form-group col-sm-6">
                                        <label name="lbl_usrcreated" for="usrcreated">usrcreated</label>
                                        <div>
                                            <input id="usrcreated" name="usrcreated" class="form-control" readonly="readonly" />
                                        </div>
                                    </div>
                                </div>
                                <div class="row">
                                    <div class="form-group col-sm-6">
                                        <label name="lbl_datemodif" for="datemodif">datemodif</label>
                                        <div>
                                            <input id="datemodif" name="datemodif" class="form-control" readonly="readonly" />
                                        </div>
                                    </div>
                                    <div class="form-group col-sm-6">
                                        <label name="lbl_usrmodif" for="usrmodif">usrmodif</label>
                                        <div>
                                            <input id="usrmodif" name="usrmodif" class="form-control" readonly="readonly" />
                                        </div>
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
