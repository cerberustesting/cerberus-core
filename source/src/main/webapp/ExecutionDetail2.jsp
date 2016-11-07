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
        <script type="text/javascript" src="js/pages/ExecutionDetail1.js"></script>
        <link rel="stylesheet" type="text/css" href="css/pages/ExecutionDetail.css">
    </head>
    <body>
        <%@ include file="include/header.html"%>
        <div id="page-layout" class="container-fluid center">
            <div class="alert alert-warning">
                <strong>BETA</strong> This page is in beta, some features may not be available or fully functional.
                <button class="btn btn-warning side-item" id="runOld">Old Page</button>
            </div>
            <%@ include file="include/messagesArea.html"%>

            <div class="progress">
                <div id="progress-bar" class="progress-bar" role="progressbar" aria-valuenow="0" aria-valuemin="0" aria-valuemax="100">
                    <span class="sr-only"></span>
                </div>
            </div>

            <div class="panel panel-default" id="testCaseConfig">
                <div class="panel-heading">
                    <div class="pull-left">
                        <div class="">
                            <span id="test"></span>
                            <span> - </span>
                            <span id="testcase"></span>
                            <span> - </span>
                            <span id="controlstatus"></span>
                        </div>
                    </div>
                    <div class="pull-right" id="moredetails">
                        <a>
                            More details <span class="caret"></span>
                        </a>
                    </div>
                    <div class="clearfix"></div>
                </div>
                <div class="panel-body" id="testCaseDetails" style="display:none;">
                    <div class="row">
                        <div class="col-sm-4">
                            <div class="form-group">
                                <label for="application">Application</label>
                                <input type="text" class="form-control" id="application" placeholder="Application" readonly>
                            </div>
                        </div>
                        <div class="col-sm-4">
                            <div class="form-group">
                                <label for="browser">Browser</label>
                                <input type="text" class="form-control" id="browser" placeholder="Browser" readonly>
                            </div>
                        </div>
                        <div class="col-sm-4">
                            <div class="form-group">
                                <label for="browserfull">Browser Full Version</label>
                                <input type="text" class="form-control" id="browserfull" placeholder="Browser Full Version" readonly>
                            </div>
                        </div>
                    </div>
                    <div class="row">
                        <div class="col-sm-4">
                            <div class="form-group">
                                <label for="country">Country</label>
                                <input type="text" class="form-control" id="country" placeholder="Country" readonly>
                            </div>
                        </div>
                        <div class="col-sm-4">
                            <div class="form-group">
                                <label for="environment">Environment</label>
                                <input type="text" class="form-control" id="environment" placeholder="Environment" readonly>
                            </div>
                        </div>
                        <div class="col-sm-4">
                            <div class="form-group">
                                <label for="status">Status</label>
                                <input type="text" class="form-control" id="status" placeholder="Status" readonly>
                            </div>
                        </div>
                    </div>
                    <div class="row">
                        <div class="col-sm-4">
                            <div class="form-group">
                                <label for="controlstatus2">Control Status</label>
                                <input type="text" class="form-control" id="controlstatus2" placeholder="Control Status" readonly>
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
                        <div class="col-sm-4">
                            <div class="form-group">
                                <label for="ip">IP</label>
                                <input type="text" class="form-control" id="ip" placeholder="IP" readonly>
                            </div>
                        </div>
                        <div class="col-sm-4">
                            <div class="form-group">
                                <label for="port">Port</label>
                                <input type="text" class="form-control" id="port" placeholder="Port" readonly>
                            </div>
                        </div>
                        <div class="col-sm-4">
                            <div class="form-group">
                                <label for="platform">Platform</label>
                                <input type="text" class="form-control" id="platform" placeholder="Platform" readonly>
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
                                <label for="executor">Executor</label>
                                <input type="text" class="form-control" id="executor" placeholder="Executor" readonly>
                            </div>
                        </div>
                        <div class="col-sm-4">
                            <div class="form-group">
                                <label for="url">URL</label>
                                <input type="text" class="form-control" id="url" placeholder="URL" readonly>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div id="handler" class="row" style="margin: 0px; margin-top: 10px;">
                <nav class="col-lg-3" id="nav-execution">
                    <div id="list-wrapper">
                        <div>
                            <h3>Steps</h3>
                            <ul class="list-group step-list side-item" id="stepList" style="max-height: 500px;overflow-y: auto"></ul>
                        </div>
                        <div>
                            <div>
                                <h3> Actions </h3>
                                <button class="btn btn-block btn-primary side-item" id="editTcInfo">Edit Test Case</button>
                                <button class="btn btn-block btn-primary side-item" id="runTestCase">Run this Test Case Again</button>
                                <button class="btn btn-block btn-primary side-item" id="lastExecution">See last executions</button>
                            </div>
                        </div>
                    </div>
                </nav>
                <div class="col-lg-9" id="stepContent">
                    <div>
                        <div id="stepInfo" class="row" style="display: none;">
                        </div>
                    </div>
                    <div id="actionContainer"></div>
                </div>
            </div>
            <footer class="footer">
                <div id="footer" style="display: inline-block"></div>
            </footer>
        </div>
    </body>
</html>
