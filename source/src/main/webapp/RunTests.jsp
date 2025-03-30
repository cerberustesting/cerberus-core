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
    Document   : RunTests1
    Created on : 14 oct. 2015, 16:07:31
    Author     : cerberus
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%@ include file="include/global/dependenciesInclusions.html" %>
        <link rel="stylesheet" type="text/css" href="css/pages/RunTests.css"/>
        <script type="text/javascript" src="js/pages/RunTest.js"></script>
        <script type="text/javascript" src="js/transversalobject/Robot.js"></script>
        <title id="pageTitle">Run Test</title>
    </head>
    <body>
        <%@ include file="include/global/header.html"%>
        <%@ include file="include/utils/modal-confirmation.html"%>


        <div id="runTestCaseBlockSticky" class="btn-group btn-group-lg" role="group">
            <button type="button" class="feedback btn btn-secondary " id="runTestCase">Run TestCase</button>
            <button type="button" class="feedback btn btn-primary " id="runTestCaseAndSee">Run TestCase (and See Result)</button>
        </div>

        <div class="container-fluid center" id="page-layout">

            <div id="DialogMessagesArea">
                <div class="alert" id="DialogMessagesAlert"  style="display:none;">
                    <strong><span class="alert-description" id="DialogAlertDescription"></span></strong>
                    <button type="button" class="close" data-hide="alert"  aria-hidden="true">
                        <span class="glyphicon glyphicon-remove alert-right alert-close pull-right"></span>
                    </button>
                </div>
            </div>
            <%@ include file="include/global/messagesArea.html"%>
            <%@ include file="include/pages/runtests/TestCaseNotValid.html"%>
            <%@ include file="include/transversalobject/Robot.html"%>
            <h1 class="page-title-line">Run Test</h1>
            <div class="row">
                <div class="col-lg-12">

                    <div class="panel panel-default" id="selectionPanel">
                        <div class="panel-heading">
                            Selection Type
                        </div>
                        <div class="panel-body">
                            <div class="row">
                                <div class="col-sm-5">
                                    <div class="btn-group">
                                        <button type="button" id="SelectionManual" class="btn btn-success">Manual Selection</button>
                                        <button type="button" id="SelectionCampaign" class="btn btn-default">Campaign Selection</button>
                                    </div>
                                </div>
                                <form id="campaignSelection"  style="display: none;">
                                    <div class="form-group col-sm-5">
                                        <div class="input-group">
                                            <select class="form-control" id="campaignSelect" style="width: 300px"></select>
                                            <button type="button" class="btn btn-primary" id="loadCampaignBtn">Load</button>
                                        </div>
                                    </div>
                                </form>
                            </div>
                        </div>
                    </div>

                    <div class="panel panel-default" id="TestPanel">
                        <div class="panel-heading" id="ChooseTestHeader">
                            Choose Test Case / Environment / Country
                        </div>
                        <div class="panel-body" id="chooseTest">
                            <div class="panel panel-default" id="filtersPanelContainer">
                                <div class="panel-heading card" data-toggle="collapse" data-target="#filtersPanel" id="FilterPanelHeader">
                                    <span class="toggle glyphicon glyphicon-chevron-right pull-right"></span>
                                    <span id="FilterHeader">Extended Test Case Filters</span>
                                </div>
                                <div class="panel-body collapse defaultNotExpanded" id="filtersPanel">
                                    <form id="filters">
                                        <div class="row">
                                            <div class="form-group col-xs-2">
                                                <label id="lbl_test" for="testFilter">Test</label>
                                                <select class="multiselectelement form-control" multiple="multiple" id="testFilter"></select>
                                            </div>
                                            <div class="form-group col-xs-2">
                                                <i><label id="lbl_label" for="labelFilter">Label</label></i>
                                                <select class="multiselectelement form-control" multiple="multiple" id="labelidFilter"></select>
                                            </div>
                                            <div class="form-group col-xs-2">
                                                <label id="lbl_status" for="statusFilter">Status</label>
                                                <select class="multiselectelement form-control" multiple="multiple" id="statusFilter"></select>
                                            </div>
                                            <div class="form-group col-xs-2">
                                                <label id="lbl_creator" for="creatorFilter">Creator</label>
                                                <select class="multiselectelement form-control" multiple="" id="creatorFilter"></select>
                                            </div>
                                            <div class="form-group col-xs-2">
                                                <label id="lbl_implementer" for="implementerFilter">Implementer</label>
                                                <select class="multiselectelement form-control" multiple="" id="implementerFilter"></select>
                                            </div>
                                            <div class="form-group col-xs-2">
                                                <label id="lbl_type" for="typeFilter">Type</label>
                                                <select class="multiselectelement form-control" multiple="multiple" id="typeFilter"></select>
                                            </div>
                                        </div>
                                        <div class="row">
                                            <div class="form-group col-xs-2">
                                                <label id="lbl_priority" for="priorityFilter">Priority</label>
                                                <select class="multiselectelement form-control" multiple="multiple" id="priorityFilter"></select>
                                            </div>
                                            <div class="form-group col-xs-2">
                                                <label id="lbl_system" for="systemFilter">System</label>
                                                <select class="multiselectelement form-control" multiple="multiple" id="systemFilter"></select>
                                            </div>
                                            <div class="form-group col-xs-2">
                                                <label id="lbl_application" for="applicationFilter">Application</label>
                                                <select class="multiselectelement form-control" multiple="" id="applicationFilter"></select>
                                            </div>
                                            <div class="form-group col-xs-2">
                                                <i><label id="lbl_targetMajor" for="targetMajorFilter">Target Major</label></i>
                                                <select class="multiselectelement form-control" multiple="multiple" id="targetMajorFilter"></select>
                                            </div>
                                            <div class="form-group col-xs-2">
                                                <i><label id="lbl_targetMinor" for="targetMinorFilter">Target Minor</label></i>
                                                <select class="multiselectelement form-control" multiple="multiple" id="targetMinorFilter"></select>
                                            </div>
                                        </div>
                                        <div class="row">
                                            <div class="form-group col-xs-2">
                                                <label id="lbl_campaign" for="campaignFilter">Campaign</label>
                                                <select class="multiselectelement form-control" multiple="multiple" id="campaignFilter"></select>
                                            </div>
                                            <div class="form-group col-xs-2">
                                                <label id="lbl_size" for="lengthFilter">Result size</label>
                                                <select class="form-control" id="lengthFilter"></select>
                                            </div>
                                            <div class="form-group col-xs-8">
                                                <button type="button" class="btn btn-primary btn-lg btn-block" id="loadFiltersBtn">Search</button>
                                            </div>
                                        </div>
                                    </form>
                                </div>
                            </div>
                            <div class="row">
                                <div class="form-group col-xs-11">
                                    <label for="testcaseList" id="testcaseListLabel" class="bold">Test Case :</label>
                                    <select multiple id="testCaseList" class="form-control" style="height: 300px;"></select>
                                </div>
                                <div class="col-xs-1">
                                    <div class="row" style="margin-top: 120px;">
                                        <button id="testcaseSelectAll" class="glyphicon glyphicon-check" title="Select All"></button>
                                        <button id="testcaseSelectNone" class="glyphicon glyphicon-unchecked" title="Select None"></button>
                                    </div>
                                </div>
                            </div>
                            <div class="row">

                                <div id="envSettingsBlock" class="col-lg-6" style="margin-bottom: 15px;">
                                    <label for="envList" id="envListLabel" class="bold">Environment :</label>
                                    <label class="radio-inline">
                                        <input name="envSettings" value="auto" checked type="radio"/><span>Automatic</span>
                                    </label>
                                    <label class="radio-inline">
                                        <input name="envSettings" value="manual" type="radio"/><span>Manual</span>
                                    </label>
                                    <form id="envSettingsAuto">
                                        <select multiple class="form-control" name="environment"></select>
                                    </form>
                                    <form id="envSettingsMan" style="display: none;" class="form-horizontal">
                                        <div class="form-group">
                                            <label for="myhost" class="col-sm-3 control-label bold">My Host</label>
                                            <div class="col-sm-9">
                                                <input type="text" class="form-control" id="myhost" name="myhost"/>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label for="mycontextroot" class="col-sm-3 control-label bold">My Context Root</label>
                                            <div class="col-sm-9">
                                                <input type="text" class="form-control" id="mycontextroot" name="mycontextroot"/>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label for="myloginrelativeurl" class="col-sm-3 control-label bold">My Login Relative URL</label>
                                            <div class="col-sm-9">
                                                <input type="text" class="form-control" id="myloginrelativeurl" name="myloginrelativeurl"/>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label for="myenvdata" class="col-sm-3 control-label bold">My Data Environment</label>
                                            <div class="col-sm-9">
                                                <select class="form-control" id="myenvdata" name="environment"></select>
                                            </div>
                                        </div>
                                    </form>
                                </div>

                                <div id="countrySettingsBlock" class="col-lg-6">
                                    <div class="row">
                                        <div class="form-group col-xs-11">
                                            <label for="countryList" id="countryListLabel" class="bold">Country :</label>
                                            <div id="countryList" name="countryList" ></div>
                                        </div>
                                        <div class="col-xs-1">
                                            <div class="row" style="margin-top: 30px;">
                                                <button id="countrySelectAll" class="glyphicon glyphicon-check" title="Select All"></button>
                                                <button id="countrySelectNone" class="glyphicon glyphicon-unchecked" title="Select None"></button>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                                
                            </div>

                        </div>
                    </div>

                </div>

            </div>
            <div class="row">
                <div class="col-lg-12">

                    <div class="row">
                        <div class="col-lg-6">
                            <div class="panel panel-default" id="RobotPanel">

                                <div class="panel-heading card" data-toggle="collapse" data-target="#robotSettings">
                                    <label id="rbtLabel">Robot settings</label>
                                    <span class="toggle glyphicon glyphicon-chevron-right pull-right"></span>
                                </div>
                                <div class="panel-body collapse in" id="robotSettings">
                                    <form class="form-horizontal" id="robotSettingsForm">
                                        <div class="form-group">
                                            <label for="robot" class="col-sm-3 control-label bold">Select Robot Config</label>
                                            <div class="col-sm-6">
                                                <select class="form-control" id="robot" name="robot" multiple></select>
                                                <button type="button" id="robotEdit" class="glyphicon glyphicon-edit btn" title="Edit Robot"></button>
                                                <button type="button" id="robotCreate" class="glyphicon glyphicon-plus-sign btn" title="Create a new Robot"></button>
                                            </div>
                                            <div class="col-sm-1" style="margin-top: 0px;">
                                            </div>
                                            <div class="col-sm-1" style="margin-top: 0px;">
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label for="seleniumIP" class="col-sm-3 control-label bold">Selenium Server IP</label>
                                            <div class="col-sm-9">
                                                <input type="text" class="form-control" id="seleniumIP" name="ss_ip"/>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label for="seleniumPort" class="col-sm-3 control-label bold">Selenium Server Port</label>
                                            <div class="col-sm-9">
                                                <input type="text" class="form-control" id="seleniumPort" name="ss_p"/>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label for="browser" class="col-sm-3 control-label bold">Browser</label>
                                            <div class="col-sm-9">
                                                <select class="form-control" id="browser" name="browser"></select>
                                            </div>
                                        </div>
                                    </form>
                                    <div class="col-sm-offset-3 col-sm-9">
                                        <button class="btn btn-default btn-sm pull-right" id="saveRobotPreferences">Record my Robot Preferences</button>
                                    </div>
                                </div>

                            </div>
                        </div>
                        <div class="col-lg-6">
                            <div class="panel panel-default" id="executionPanel">
                                <div class="panel-heading card" data-toggle="collapse" data-target="#executionSettings">
                                    <label id="exeLabel">Execution settings</label>
                                    <span class="toggle glyphicon glyphicon-chevron-right pull-right"></span>
                                </div>
                                <div class="panel-body collapse in" id="executionSettings">
                                    <form class="form-horizontal"id="executionSettingsForm">
                                        <div class="form-group">
                                            <label for="tag" class="col-sm-3 control-label bold">Tag</label>
                                            <div class="col-sm-9">
                                                <input type="text" class="form-control" id="tag" name="Tag" maxlength="255"/>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label for="verbose" class="col-sm-3 control-label bold">Verbose</label>
                                            <!--                                            <span class="toggle glyphicon glyphicon-list pull-left"></span>-->
                                            <div class="col-sm-9">
                                                <select class="form-control" id="verbose" name="Verbose"></select>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label for="screenshot" class="col-sm-3 control-label bold">Screenshot</label>
                                            <div class="col-sm-9">
                                                <select class="form-control" id="screenshot" name="Screenshot"></select>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label for="video" class="col-sm-3 control-label bold">Video</label>
                                            <div class="col-sm-9">
                                                <select class="form-control" id="video" name="Video"></select>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label for="pageSource" class="col-sm-3 control-label bold">Page Source</label>
                                            <div class="col-sm-9">
                                                <select class="form-control" id="pageSource" name="PageSource"></select>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label for="seleniumLog" class="col-sm-3 control-label bold">Robot Log</label>
                                            <div class="col-sm-9">
                                                <select class="form-control" id="seleniumLog" name="SeleniumLog"></select>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label for="consoleLog" class="col-sm-3 control-label bold">Console Log</label>
                                            <div class="col-sm-9">
                                                <select class="form-control" id="consoleLog" name="ConsoleLog"></select>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label for="timeout" class="col-sm-3 control-label bold">Timeout</label>
                                            <div class="col-sm-9">
                                                <input type="text" class="form-control" id="timeout" name="timeout"/>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label for="retries" class="col-sm-3 control-label bold">Retries</label>
                                            <div class="col-sm-9">
                                                <select class="form-control" id="retries" name="retries"></select>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label for="priority" class="col-sm-3 control-label bold">Priority</label>
                                            <div class="col-sm-9">
                                                <input type="text"  class="form-control" id="priority" name="priority"/>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label for="manualExecution" class="col-sm-3 control-label bold">Manual Execution</label>
                                            <div class="col-sm-9">
                                                <select class="form-control" id="manualExecution" name="manualExecution"></select>
                                            </div>
                                        </div>
                                    </form>
                                    <div class="col-sm-offset-3 col-sm-9">
                                        <button type="" class="btn btn-default btn-sm pull-right" id="saveExecutionParams">Record my Execution Parameters</button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <form method="get" action="RunTestCase" id="RunTestCase">
                        <input type="hidden" name="Test" id="testATQ">
                        <input type="hidden" name="TestCase" id="testcaseATQ">
                        <input type="hidden" name="Country" id="countryATQ">
                        <input type="hidden" name="Environment" id="envATQ">
                        <input type="hidden" name="browser" id="browserATQ">
                        <input type="hidden" name="manualURL" id="manualURLATQ">
                        <input type="hidden" name="myhost" id="myhostATQ">
                        <input type="hidden" name="mycontextroot" id="mycontextrootATQ">
                        <input type="hidden" name="myloginrelativeurl" id="myloginrelativeurlATQ">
                        <input type="hidden" name="myenvdata" id="myenvdataATQ">
                        <input type="hidden" name="robot" id="manualRobotATQ">
                        <input type="hidden" name="ss_ip" id="ss_ipATQ">
                        <input type="hidden" name="ss_p" id="ss_pATQ">
                        <input type="hidden" name="version" id="versionATQ">
                        <input type="hidden" name="Tag" id="tagATQ">
                        <input type="hidden" name="outputformat" id="outputformatATQ" value="gui">
                        <input type="hidden" name="verbose" id="verboseATQ">
                        <input type="hidden" name="screenshot" id="screenshotATQ">
                        <input type="hidden" name="video" id="videoATQ">
                        <input type="hidden" name="pageSource" id="pageSourceATQ">
                        <input type="hidden" name="seleniumLog" id="seleniumLogATQ">
                        <input type="hidden" name="consoleLog" id="consoleLogATQ">
                        <input type="hidden" name="timeout" id="timeoutATQ">
                        <input type="hidden" name="retries" id="retriesATQ">
                        <input type="hidden" name="manualExecution" id="manualExecutionATQ">
                    </form>

                </div>
            </div>

            <!--            <div class="row" id="runCampaignBlock" style="display: none;">
                            <div class="col-lg-6">
                                <button type="button" class="btn btn-primary btn-lg btn-block"  id="runCampaign">Run Campaign</button>
                            </div>
                            <div class="col-lg-6">
                                <button type="button" class="btn btn-primary btn-lg btn-block" id="runCampaignAndSee">Run Campaign (and See Result)</button>
                            </div>
                        </div>
                        <div class="row" id="runTestCaseBlock">
                            <div class="col-lg-6">
                                <button type="button" class="btn btn-primary btn-lg btn-block" id="runTestCase">Run Test Case</button>
                            </div>
                            <div class="col-lg-6">
                                <button type="button" class="btn btn-primary btn-lg btn-block" id="runTestCaseAndSee">Run Test Case (and See Result)</button>
                            </div>
                        </div>-->

            <footer class="footer marginTop25">
                <div class="col-xs-6 container-fluid" id="footer"></div>
            </footer>
        </div>
    </body>
</html>
