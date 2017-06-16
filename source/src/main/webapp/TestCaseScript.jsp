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
        <%@ include file="include/dependenciesInclusions.html" %>
        <title id="pageTitle">Test Case</title>        
        <script type="text/javascript" src="dependencies/Tinymce-4.2.6/tinymce.min.js"></script>
        <script type="text/javascript" src="js/pages/transversalobject/ApplicationObject.js"></script>
        <script type="text/javascript" src="js/pages/transversalobject/TestCase.js"></script>
        <script type="text/javascript" src="js/pages/TestCaseScript.js"></script>
        <script type="text/javascript" src="dependencies/Ace-1.2.6/ext-language_tools.js"></script>
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
                <div style="min-height:150px">
                    <div id="divPanelDefault" class="panel-default" style="z-index:100; top: 0;height:150px">
                        <div class="panel-heading" id="testCaseTitle">
                            <div class="" style="width:100%">
                                <div class="col-lg-4" style="padding: 0px;">
                                    <div class="testTestCase" style="margin-top:4px; margin-bottom: 4px;"><select id="test"></select></div>
                                </div>
                                <div class="col-lg-8" style="padding: 0px;">
                                    <div id="TestCaseButton" style="display:none;">
                                        <div class="btn-group pull-right">
                                            <button class="btn btn-default" id="saveScript" disabled style="margin-left: 1px;"><span class="glyphicon glyphicon-save"></span> Save</button>
                                            <button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                                                <span class="caret"></span>
                                                <span class="sr-only">Toggle Dropdown</span>
                                            </button>
                                            <ul class="dropdown-menu">
                                                <li><a href="#" id="saveScriptAs"><span class="glyphicon glyphicon-floppy-disk"></span> Save As</a></li>
                                            </ul>
                                        </div>
                                        <div class="btn-group pull-right">
                                            <a href="#" class="btn btn-default" id="runTestCase" data-toggle="tooltip" style="margin-left: 1px;"><span class="glyphicon glyphicon-play"></span> Run</a>
                                            <a type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                                                <span class="caret"></span>
                                                <span class="sr-only">Toggle Dropdown</span>
                                            </a>
                                            <ul class="dropdown-menu">
                                                <li><a href="#"><button class="btn btn-default pull-right" id="rerunTestCase" style="margin-left: 0px; margin-right: 0px;"><span class="glyphicon glyphicon-forward"></span> ReRun</button></a></li>
                                            </ul>
                                        </div>
                                        <a><button class="btn btn-default pull-right" id="seeLastExec" style="margin-left: 1px; margin-right: 1px;"><span class="glyphicon glyphicon-fast-backward"></span> Last Executions</button></a>
                                        <a><button class="btn btn-default pull-right" id="seeLogs" style="margin-left: 1px; margin-right: 1px;"><span class="glyphicon glyphicon-book"></span> Logs</button></a>
                                        <button class="btn btn-default pull-right" id="editTcInfo"> Edit Test Case</button>
                                        <button class="btn btn-default pull-right" id="deleteTestCase" disabled> Delete Test Case</button>
                                        <div class="side-item pull-right"></div>

                                    </div>
                                </div>
                                <div class="clearfix"></div>
                                <select id="testCaseSelect" style="display:none;"></select>
                            </div>
                        </div>
                        <div style="height:40px; background-color: white">
                            <ul id="tabsScriptEdit" class="nav nav-tabs" data-tabs="tabs">
                                <li class="active"><a data-toggle="tab" href="#tabSteps" id="editTabStep" name="tabSteps">Steps</a></li>
                                <li><a data-toggle="tab" href="#tabProperties" id="editTabProperties" name="tabProperties">Properties</a></li>
                                <li><a data-toggle="tab" href="#tabInheritedProperties" id="editTabInheritedProperties" name="tabInheritedProperties">InheritedProperties</a></li>
                                <li><a data-toggle="tab" href="#tabSchema" id="editTabSchema" name="tabSchema">Schema</a></li>
                            </ul>
                        </div>
                    </div>
                </div>
                <div class="panel-body" id="tcBody" style="display:none;">

                    <div class="tab-content">
                        <div class="center marginTop25 tab-pane fade in active" id="tabSteps">
                            <nav class="col-lg-3" id="nav-execution" style="z-index:1;">
                                <div id="list-wrapper" style="top:107px;">
                                    <div id="stepListWrapper">
                                        <!--<h4>Steps</h4>-->
                                        <ul class="list-group step-list side-item nav nav-pills" id="stepList" style="max-height: 600px;overflow-y: auto"></ul>
                                    </div>
                                    <div id="tcButton">
                                        <!--<h4>Actions</h4>-->
                                        <button class="btn btn-info btn-block marginTop25" id="addStep" disabled>Add Step</button>
                                        <!--                                        <div id="manageProperties" style="margin-bottom: 5px; margin-top: 5px;">
                                                                                    <button class="btn btn-warning btn-block" id="manageProp">Manage Properties</button>
                                                                                </div>-->
                                    </div>
                                </div>
                            </nav>
                            <div class="col-lg-9 well marginTop5" id="contentWrapper" style="min-height: 200px;">
                                <div id="stepHeader" style="margin-bottom: 15px; display:none;">
                                    <div class="row step">
                                        <div class="marginLeft25 content col-lg-9">
                                            <div class="fieldRow row" id="UseStepRow" style="display: none;">
                                            </div>
                                            <div style="margin-top:15px;" class="input-group marginBottom20">
                                                <span class="input-group-addon" style="font-weight: 700;" id="stepId"></span>
                                                <input aria-describedby="stepId" class="description form-control" id="stepDescription" placeholder="Step" style="width: 100%; font-size: 20px; font-weight: 900;">
                                            </div>
                                            <div class="fieldRow row marginTop25" id="stepHiddenRow" style="display: none;">
                                                <div class="col-lg-3 form-group">
                                                    <label>Step Loop:</label>
                                                    <select class="form-control input-sm" id="stepLoop"></select>
                                                </div>
                                                <div class="col-lg-3 form-group">
                                                    <label>Step Condition Operation:</label>
                                                    <select class="form-control input-sm" id="stepConditionOper"></select>
                                                </div>
                                                <div class="col-lg-3 form-group">
                                                    <label>Step Condition Parameter:</label>
                                                    <input class="form-control input-sm" id="stepConditionVal1">
                                                </div>
                                                <div class="col-lg-3 form-group">
                                                    <label>Step Condition Parameter:</label>
                                                    <input class="form-control input-sm" id="stepConditionVal2">
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
                                                <button class="btn btn-danger" id="deleteStep" disabled>
                                                    <span class="glyphicon glyphicon-trash"></span>
                                                </button>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                                <div id="actionContainer"></div>
                                <div style="margin-left: -15px; margin-right: -15px; margin-top: 15px; display: none;" id="addActionBottomBtn">
                                    <button id="addActionBottom" class="btn btn-block btn-primary" disabled onclick="addActionAndFocus()">Add Action</button>
                                </div>
                            </div>
                        </div>

                        <div class="center marginTop25 tab-pane fade" id="tabProperties">
                            
                            
                            <nav class="col-lg-3" id="nav-property" style="z-index:1;">
                                <div id="list-wrapper-prop" style="top:107px;">
                                    <div id="propListWrapper">
                                        <ul class="list-group property-list side-item nav nav-pills" id="propList" style="max-height: 600px;overflow-y: auto"></ul>
                                    </div>
                                    <div id="propButton">
                                        <button class="btn btn-warning btn-block marginTop25" id="addProperty">Add Property</button>
                                    </div>
                                </div>
                            </nav>
                            <div class="col-lg-9 well marginTop5" id="contentWrapperProperty" style="min-height: 200px;max-height: 600px;overflow-y: auto">
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
                        </div>
                        <div class="center marginTop25 tab-pane fade" id="tabInheritedProperties">
                            <nav class="col-lg-3" id="nav-property" style="z-index:1;">
                                <div id="list-wrapper-prop" style="top:107px;">
                                    <div id="inheritPropListWrapper">
                                        <!--<h4>Steps</h4>-->
                                        <ul class="list-group property-list side-item nav nav-pills" id="inheritPropList" style="max-height: 600px;overflow-y: auto"></ul>
                                    </div>
                                </div>
                            </nav>
                            <div class="col-lg-9 well marginTop5" id="contentWrapperProperty" style="min-height: 200px;max-height: 600px;overflow-y: auto">
                                <div id="propertiesModal">
                                    <div class="" id="inheritedPropPanelWrapper">
                                        <div class="panel-body collapse" id="inheritedPropertiesPanel">
                                            <div id="inheritedPropPanel" class="list-group">
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div class="center marginTop25 tab-pane fade" id="tabSchema">
                            <div id="schemaDiv"></div>
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
