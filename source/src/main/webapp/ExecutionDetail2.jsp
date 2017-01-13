<%--
  ~ Cerberus  Copyright (C) 2013  vertigo17
  ~ DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
  ~
  ~ This file is part of Cerberus.
  ~
  ~ Cerberus is free software: you can redistribute it and/or modify
  ~ it under the terms of the GNU General Public License as published by
  ~ the Free Software Foundation, either version 3 of the License, or
  ~ (at your option) any later version.
  ~
  ~ Cerberus is distributed in the hope that it will be useful,
  ~ but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~ GNU General Public License for more details.
  ~
  ~ You should have received a copy of the GNU General Public License
  ~ along with Cerberus.  If not, see <http://www.gnu.org/licenses/>.
--%>


<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta content="text/html; charset=UTF-8" http-equiv="content-type">
        <%@ include file="include/dependenciesInclusions.html" %>
        <title id="pageTitle">Execution Detail</title>        
        <script type="text/javascript" src="dependencies/Tinymce-4.2.6/tinymce.min.js"></script>
        <script type="text/javascript" src="js/pages/ExecutionDetail.js"></script>
        <link rel="stylesheet" type="text/css" href="css/pages/ExecutionDetail.css">
    </head>
    <body>
        <%@ include file="include/header.html"%>
        <%@ include file="include/utils/modal-generic.html"%>
        <%@ include file="include/testcasescript/manageProperties.html"%>
        <div id="page-layout" class="container-fluid center">
            <%@ include file="include/messagesArea.html"%>
            <h1 class="page-title-line">Execution Detail</h1>
            <div class="panel panel-default" id="testCaseConfig">
                <div class="panel-heading" id="executionHeader"  style="z-index:2; top: 0">
                    <div class="progress">
                        <div id="progress-bar" class="progress-bar" role="progressbar" aria-valuenow="0" aria-valuemin="0" aria-valuemax="100">
                            <span class="sr-only"></span>
                        </div>
                    </div>
                    <div class="col-lg-4 pull-left">
                        <div class="">
                            <span id="idlabel"></span>
                            <span> - </span>
                            <span id="test"></span>
                            <span> - </span>
                            <span id="testcase"></span>
                            <span> - </span>
                            <span id="controlstatus" style="font-weight: 900"></span>
                            <span> - </span>
                        </div>
                    </div>
                    <div class="col-lg-8" style="padding: 0px;">
                        <div id="TestCaseButton">
                            <a target="_blank"><button class="btn btn-default pull-right" id="runTestCase" data-toggle="tooltip" style="margin-left: 1px;"><span class="glyphicon glyphicon-play"></span> Run</button></a>
                            <div class="btn-group pull-right">
                                        <button class="btn btn-default" id="lastExecution" style="margin-left: 1px"><span class="glyphicon glyphicon-fast-backward"></span> Last Executions</button>
                                        <button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                                            <span class="caret"></span>
                                            <span class="sr-only">Toggle Dropdown</span>
                                        </button>
                                        <ul class="dropdown-menu">
                                            <li><a href="#"><button class="btn btn-default pull-right" id="lastExecutionwithEnvCountry"><span class="glyphicon glyphicon-fast-backward"></span> Last Executions With Country Env</button></a></li>
                                        </ul>
                                    </div>
                            <button class="btn btn-default pull-right" id="editTcInfo"><span class="glyphicon glyphicon-pencil"></span> Edit Test Case</button>
                            <a target="_blank"><button class="btn btn-default pull-right" id="ExecutionByTag" style="margin-left: 1px; margin-right: 1px;"><span class="glyphicon glyphicon-fast-backward"></span> See Execution By Tag</button></a>
                            <div class="side-item pull-right"></div>

                        </div>
                    </div>
                    <div class="clearfix"></div>
                </div>
                <div class="panel-body" id="testCaseDetails">
                    <ul id="tabsScriptEdit" class="nav nav-tabs" data-tabs="tabs">
                        <li class="active"><a data-toggle="tab" href="#tabSteps" id="editTabStep" name="tabSteps">Steps</a></li>
                        <li><a data-toggle="tab" href="#tabProperties" id="editTabProperties" name="tabProperties">Properties</a></li>
                        <li><a data-toggle="tab" href="#tabDetail" id="editTabDetail" name="tabDetail">Execution Details</a></li>
                        <li><a data-toggle="tab" href="#tabEnv" id="editTabEnv" name="tabEnv">Environment</a></li>
                        <li><a data-toggle="tab" href="#tabRobot" id="editTabRobot" name="tabRobot">Robot</a></li>
                    </ul>
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
                                        <div>
                                            <div id="stepInfo" class="row" style="display: none;">
                                            </div>
                                        </div>
                                        <div id="actionContainer"></div>
                                    </div>
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
                            </div>
                            <div class="row">
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
                                <div class="col-sm-3">
                                    <div class="form-group">
                                        <label for="environment">Environment</label>
                                        <input type="text" class="form-control" id="environment" placeholder="Environment" readonly>
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

                    <footer class="footer">
                        <div id="footer" style="display: inline-block"></div>
                    </footer>
                </div>
            </div>
        </div>
    </div>
</body>
</html>
