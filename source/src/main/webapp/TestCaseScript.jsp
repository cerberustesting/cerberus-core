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
        <title id="pageTitle">Test Case</title>        
        <script type="text/javascript" src="dependencies/Tinymce-4.2.6/tinymce.min.js"></script>
        <script type="text/javascript" src="js/pages/transversalobject/ApplicationObject.js"></script>
        <script type="text/javascript" src="js/pages/transversalobject/TestCase.js"></script>
        <script type="text/javascript" src="js/pages/TestCaseScript.js"></script>
        <link rel="stylesheet" type="text/css" href="css/pages/TestCaseScript.css">
    </head>
    <body>
        <%@ include file="include/header.html"%>
        <div id="page-layout" class="container-fluid center">
            <%@ include file="include/messagesArea.html"%>
            <%@ include file="include/transversalobject/TestCase.html"%>
            <%@ include file="include/utils/modal-confirmation.html"%>
            <%@ include file="include/testcasescript/manageProperties.html"%>
            <%@ include file="include/testcasescript/addStep.html"%>
            <%@ include file="include/transversalobject/addApplicationObject.html"%>

            <h1 class="page-title-line">Test Case Script</h1>
            <div class="panel panel-default" style="margin-top: 10px;">
                <div class="panel-default" style="height:93px;">
                    <div class="panel-heading" id="testCaseTitle" style="z-index:2; top: 0">
                        <div class="" style="width:100%">
                            <div class="col-lg-4" style="padding: 0px;">
                                <h3 class="testTestCase" style="margin-top:4px; margin-bottom: 4px;"><select id="test"></select></h3>
                            </div>
                            <div class="col-lg-8" style="padding: 0px;">
                                <div id="TestCaseButton" style="display:none;">
                                    <button class="btn btn-primary pull-right" id="saveScript" style="margin-left: 1px; margin-right: 1px;"><span class="glyphicon glyphicon-save"></span> Save</button>
                                    <button class="btn btn-default pull-right" id="editTcInfo" style="margin-left: 1px; margin-right: 1px;"><span class="glyphicon glyphicon-pencil"></span> Edit</button>
                                    <button class="btn btn-default pull-right" id="runTestCase" style="margin-left: 1px; margin-right: 1px;"><span class="glyphicon glyphicon-play"></span> Run</button>
                                    <button class="btn btn-default pull-right" id="rerunTestCase" style="margin-left: 1px; margin-right: 1px;" data-toggle="tooltip"><span class="glyphicon glyphicon-forward"></span> Rerun the last configuration</button>
                                    <button class="btn btn-default pull-right" id="seeLastExec" style="margin-left: 1px; margin-right: 1px;"><span class="glyphicon glyphicon-fast-backward"></span> Last Executions</button>
                                    <button class="btn btn-default pull-right" id="seeLogs" style="margin-left: 1px; margin-right: 1px;"><span class="glyphicon glyphicon-book"></span> Logs</button>
                                    <a><button class="btn btn-default pull-right" id="runOld" style="margin-left: 1px; margin-right: 1px;"><span class="glyphicon glyphicon-bookmark"></span> Old Page</button></a>
                                    <div class="side-item pull-right"></div>

                                </div>
                            </div>
                            <div class="clearfix"></div>
                            <select id="testCaseSelect" style="display:none;"></select>
                        </div>
                    </div>
                </div>
                <div class="panel-body" style="display:none;">
                    <nav class="col-lg-3" id="nav-execution" style="z-index:1;">
                        <div id="list-wrapper" style="top:107px;">
                            <div>
                                <h3>Steps</h3>
                                <ul class="list-group step-list side-item" id="stepList" style="max-height: 600px;overflow-y: auto"></ul>
                                <button class="btn btn-info btn-block" id="addStep">Add Step</button>
                                <div id="manageProperties" style="margin-bottom: 5px; margin-top: 5px;">
                                    <button class="btn btn-warning btn-block" id="manageProp">Manage Properties</button>
                                </div>
                            </div>

                        </div>
                    </nav>
                    <div class="col-lg-9 well marginTop20" id="contentWrapper" style="min-height: 200px;">
                        <div id="stepHeader" style="margin-bottom: 15px; display:none;">
                            <div class="row step">
                                <div class="col-lg-1">

                                </div>
                                <div class="content col-lg-9">
                                    <div class="fieldRow row" id="UseStepRow" style="display: none;">

                                    </div>
                                    <div style="margin-top:15px;" class="fieldRow row form-group">
                                        <input class="description form-control" id="stepDescription" placeholder="Step" style="width: 100%; font-size: 20px; font-weight: 900;">
                                    </div>
                                    <div class="fieldRow row" id="stepHiddenRow" style="display: none;">
                                        <div class="col-lg-3 form-group">
                                            <label>Step Condtition Operation:</label>
                                            <select class="form-control input-sm" id="stepConditionOper"></select>
                                        </div>
                                        <div class="col-lg-5 form-group">
                                            <label>Step Condtition Parameter:</label>
                                            <input class="form-control input-sm" id="stepConditionVal1">
                                        </div>
                                    </div>
                                </div>
                                <div class="col-lg-2" style="padding: 0px;">
                                    <div class="fieldRow row" id="UseStepRowButton" style="display: none; color: transparent;">

                                    </div>
                                    <div style="margin-right: auto; margin-left: auto; margin-top: 15px; width: 130px;" id="stepButtons">
                                        <button class="btn btn-default btn-dark" title="Is Use Step" data-toggle="tooltip" id="isUseStep" style="display: none;">
                                            <span class="glyphicon glyphicon-lock"></span>
                                        </button>
                                        <button class="btn btn-default" title="Is Library" data-toggle="tooltip" id="isLib">
                                            <span class="glyphicon glyphicon-book"></span>
                                        </button>
                                        <button class="btn btn-default" id="stepPlus">
                                            <span class="glyphicon glyphicon-chevron-down"></span>
                                        </button>
                                        <button class="btn btn-danger" id="deleteStep">
                                            <span class="glyphicon glyphicon-trash"></span>
                                        </button>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div id="actionContainer"></div>
                        <div style="margin-left: -15px; margin-right: -15px; margin-top: 15px; display: none;" id="addActionBottomBtn">
                            <button class="btn btn-block btn-primary" onclick="addActionAndFocus()">Add Action</button>
                        </div>
                    </div>
                </div>
                <datalist id="objects"></datalist>
            </div>
            <footer class="footer">
                <div id="footer" style="display: inline-block"></div>
            </footer>
        </div>
    </body>
</html>
