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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta content="text/html; charset=UTF-8" http-equiv="content-type">
        <%@ include file="include/global/dependenciesInclusions.html" %>
        <title id="pageTitle">Test Case</title>
        <script type="text/javascript" src="dependencies/Tinymce-6.7.0/tinymce.min.js"></script>
        <script type="text/javascript" src="dependencies/Bootstrap-treeview-1.2.0/js/bootstrap-treeview.js"></script>
        <script type="text/javascript" src="dependencies/Ace-1.2.6/ext-language_tools.js"></script>
<!--        <script type="text/javascript" src="dependencies/Ace-1.38.0/ext-language_tools.js"></script>-->
        <script type="text/javascript" src="js/pages/TestCaseScript.js"></script>
        <script type="text/javascript" src="js/transversalobject/TestCase.js"></script>
        <script type="text/javascript" src="js/transversalobject/ApplicationObject.js"></script>
        <script type="text/javascript" src="js/transversalobject/TestDataLib.js"></script>
        <script type="text/javascript" src="js/transversalobject/AppService.js"></script>
        <script type="text/javascript" src="js/transversalobject/Application.js"></script>
        <script type="text/javascript" src="js/transversalobject/TestCaseExecutionQueue.js"></script>
        <script type="text/javascript" src="js/transversalobject/TestCaseSimpleExecution.js"></script>
        <link rel="stylesheet" type="text/css" href="css/pages/TestCaseScript.css">
    </head>
    <body>
        <%@ include file="include/global/header.html"%>
        <div id="page-layout" class="container-fluid center">
            <%@ include file="include/global/messagesArea.html"%>
            <%@ include file="include/transversalobject/TestCase.html"%>
            <%@ include file="include/utils/modal-confirmation.html"%>
            <%@ include file="include/pages/testcasescript/manageProperties.html"%>
            <%@ include file="include/pages/testcasescript/addStep.html"%>
            <%@ include file="include/pages/testcasescript/manageActionControlOptions.html"%>
            <%@ include file="include/pages/testcasescript/manageStepOptions.html"%>
            <%@ include file="include/transversalobject/ApplicationObject.html"%>
            <%@ include file="include/transversalobject/TestDataLib.html"%>
            <%@ include file="include/transversalobject/AppService.html"%>
            <%@ include file="include/transversalobject/Application.html"%>
            <%@ include file="include/transversalobject/Property.html"%>
            <%@ include file="include/transversalobject/TestCaseExecutionQueue.html"%>
            <%@ include file="include/transversalobject/TestCaseSimpleExecution.html"%>
            <%@ include file="include/utils/modal-generic.html"%>

            <h1 class="page-title-line">Test Case Script</h1>
            <div class="panel no-border" style="margin-top: 10px;">
                <div style="min-height:150px;background-color:#f5f6fa">
                    <div id="divPanelDefault" class="panel-default" style="z-index:100; top: 0;height:150px;background-color:#f5f6fa">
                        <div class="panel-heading" style="border-radius: 10px;margin-bottom: 0px;background-color: white;border: 1px solid #eee;" id="testCaseTitle">
                            <div class="" style="width:100%">
                                <div class="col-lg-5" style="padding: 0px;">
                                    <div class="row">
                                        <div class="testTestCase col-lg-8 col-xs-6" style="margin-top:4px; margin-bottom: 4px;">
                                            <select id="test"></select>
                                        </div>
                                        <div class="testTestCase col-lg-4 col-xs-6 pull-right" style="margin-top:4px; margin-bottom: 4px;">
                                            <img id="AppLogo"  class="pull-right" style="height:20px; overflow:hidden; text-overflow:clip; border: 0px; padding:0; margin:0; margin-left: 10px"></img>
                                            <span id="AppName" class="pull-right" style="white-space: nowrap ;"> </span>
                                        </div>
                                    </div>

                                    <select id="testCaseSelect" style="display:none;"></select>
                                </div>
                                <div class="col-lg-7" style="padding: 0px;">
                                    <div id="TestCaseButton" style="display:none;">

                                        <div class="btn-group pull-right" role="group" aria-label="Button group with nested dropdown" style="margin-left:10px; margin-top: 10px;">

                                            <div class="btn-group marginRight5">
                                                <button id="btnGroupDrop1" style="border-radius:4px" type="button" class="btn btn-secondary dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                                                    <span class="glyphicon glyphicon-option-horizontal"></span>
                                                </button>
                                                <div class="dropdown-menu" aria-labelledby="btnGroupDrop1">
                                                    <a><button class="btn btn-default pull-left" id="saveScriptAs" style="margin-left: 5px; margin-left: 5px;" ><span class="glyphicon glyphicon-floppy-disk"></span>Save As</button></a>
                                                    <a><button class="btn btn-default pull-left" id="deleteTestCase" style="margin-left: 5px; margin-left: 5px;" disabled><span class="glyphicon glyphicon-trash"></span>Delete</button></a>
                                                    <a><button class="btn btn-default pull-left" id="seeLastExec" style="margin-left: 5px; margin-right: 5px;"><span class="glyphicon glyphicon-list"></span> Last Executions</button></a>
                                                    <a><button class="btn btn-default pull-left" id="seeTest" style="margin-left: 5px; margin-right: 5px;"><span class="glyphicon glyphicon-list"></span> Test</button></a>
                                                    <a><button class="btn btn-default pull-left" id="seeLogs" style="margin-left: 5px; margin-right: 5px;"><span class="glyphicon glyphicon-list"></span> Logs</button></a>
                                                </div>
                                            </div>
                                            <div class="btn-group">
                                                <a><button class="btn btn-default pull-left" id="seeLastExecUniq"><span class="glyphicon glyphicon-cog"></span> Last Exe</button></a>
                                            </div>
                                            <div class="btn-group ">
                                                <a><button class="btn btn-default pull-right" id="editTcInfo" style="margin-left: 5px; margin-right: 5px;"><span class="glyphicon glyphicon-edit"></span> Test Case Header</button></a>
                                            </div>
                                            <div class="btn-group" id="runTestCasePopover">
                                                <a><button class="btn btn-default pull-right" id="runTestCase" style="margin-left: 5px; margin-left: 5px;" data-toggle="tooltip" ><span class="glyphicon glyphicon-play"></span></button></a>
                                            </div>
                                            <div class="btn-group">
                                                <a><button class="btn btn-default pull-right" id="rerunFromQueueandSee" data-original-title="" data-html="true" style="margin-left: 5px; margin-left: 5px;"><span class="glyphicon glyphicon-forward"></span></button></a>
                                            </div>
                                            <div class="btn-group">
                                                <a><button class="btn btn-default" id="saveScript" disabled style="margin-left: 5px;"><span class="glyphicon glyphicon-save"></span></button></a>
                                            </div>
                                        </div>

                                    </div>
                                </div>
                                <div class="clearfix"></div>
                            </div>
                        </div>
                        <div style="height:60px; background-color: #f5f6fa; padding-top:20px">
                            <ul id="tabsScriptEdit" class="nav nav-tabs" data-tabs="tabs">
                                <li class="active"><a data-toggle="tab" href="#tabSteps" id="editTabStep" class="tabSteps" name="tabSteps">Steps</a></li>
                                <li><a data-toggle="tab" href="#tabProperties" id="editTabProperties" class="tabSteps"  name="tabProperties">Properties</a></li>
                                <li><a data-toggle="tab" href="#tabInheritedProperties" id="editTabInheritedProperties" class="tabSteps"  name="tabInheritedProperties">InheritedProperties</a></li>
                                <!--                                <li><a data-toggle="tab" href="#tabSchema" id="editTabSchema" name="tabSchema">Schema</a></li>-->
                            </ul>
                        </div>
                    </div>
                </div>
                <div class="panel-body" id="tcBody" style="display:none;background-color:white">
                    <div class="tab-content">
                        <div class="center tab-pane fade in active" id="tabSteps">
                            <nav class="col-lg-4 col-md-12" id="nav-execution" style="z-index:1;padding-left: 0px;">
                                <div id="list-wrapper" style="top:107px;">
                                    <div id="stepsWrapper">
                                        <!--<h4>Steps</h4>-->
                                        <ul class="list-group step-list side-item nav nav-pills" id="steps" style="max-height: 600px;overflow-y: auto"></ul>
                                    </div>
                                    <div id="tcButton">
                                        <!--<h4>Actions</h4>-->
                                        <button class="btn btn-block addStep-btn marginTop25" id="addStep" disabled>Add Step</button>
                                        <!--                                        <button class="btn btn-info btn-block marginTop25" id="duplicateStep" disabled>Duplicate Step</button>-->
                                    </div>
                                </div>
                            </nav>
                            <div class="col-lg-8 col-md-12" id="contentWrapper" style="padding-left: 30px;border-left: 1px solid rgb(39,188,253);">
                                <div id="stepHeader" style="margin-bottom: 15px; display:none;">
                                    <div class="row step">
                                        <div class="content col-lg-10">
                                            <div class="fieldRow row" id="UseStepRow" style="display: none;">
                                            </div>
                                            <div style="margin-top:15px;" class="input-group marginBottom20">
                                                <span class="input-group-addon" style="font-weight: 700;border-radius:4px;border:1px solid #ccc" id="stepId"></span>
                                                <input aria-describedby="stepId" class="description form-control crb-autocomplete-variable" id="stepDescription" placeholder="Step" style="border: 0px;width: 100%; font-size: 20px;">
                                            </div>
                                        </div>
                                        <div class="col-lg-2" style="padding: 0px;">
                                            <div class="fieldRow row" id="UseStepRowButton" style="display: none; color: transparent;">

                                            </div>
                                            <div style="margin-right: auto; margin-left: auto; margin-top: 15px;" id="stepButtons">
                                                <button class="btn btn-default btn-dark" title="Is Use Step" data-toggle="tooltip" id="isUseStep" style="display: none;">
                                                    <span class="glyphicon glyphicon-lock"></span>
                                                </button>
                                                <button class="btn btn-default" title="Is Library" data-toggle="tooltip" id="isLib">
                                                    <span class="glyphicon glyphicon-book"></span>
                                                </button>
                                                <button class="btn add-btn config-btn" id="stepPlus">
                                                    <span class="glyphicon glyphicon-cog"></span>
                                                </button>
                                                <button class="btn add-btn deleteItem-btn" id="deleteStep" disabled>
                                                    <span class="glyphicon glyphicon-trash"></span>
                                                </button>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                                <div id="actionContainer"></div>
                                <div style="margin-left: -15px; margin-right: -15px; margin-top: 15px; display: none;" id="addActionBottomBtn">
                                    <button id="addActionBottom" class="btn btn-block btnLightBlue" disabled onclick="addActionFromBottomButton()">Add Action</button>
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
                                            <nav class="nav nav-pills nav-stacked" id="propertiesPanel">
                                                <div id="propTable" class="list-group">
                                                </div>
                                            </nav>
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
