<%-- 
    Document   : RunTest2
    Created on : 14 oct. 2015, 16:07:31
    Author     : cerberus
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%@ include file="include/dependenciesInclusions.html" %>
        <script type="text/javascript" src="js/pages/RunTest.js"></script>
        <title>Run Test</title>
    </head>
    <body>
        <%@ include file="include/header.html"%>
        <div class="container-fluid center" id="page-layout">
            <div class="alert alert-warning"><strong>BETA</strong> This page is in beta, some features may not be avalaible or fully functionnal </div>
            <%@ include file="include/messagesArea.html"%>
            <%@ include file="include/runtest/TestCaseNotValid.html"%>
            <h1 class="page-title-line">Run Test</h1>
            <div class="col-lg-9">
                <div class="panel panel-default">
                    <div class="panel-heading">
                        Selection type
                    </div>
                    <div class="panel-body">
                        <div class="row">
                            <div class="col-lg-4">
                                <label class="bold">Environment :</label>
                                <label class="radio-inline">
                                    <input name="typeSelect" value="filters" checked type="radio">Select a list of test
                                </label>
                                <label class="radio-inline">
                                    <input name="typeSelect" value="campaign" type="radio">Select a campaign
                                </label>
                            </div>
                            <form id="campaignSelection" style="display: none;">
                                <div class="form-group col-lg-8">
                                    <div class="input-group">
                                        <select class="form-control input-sm" id="campaignSelect"></select>
                                        <div class="input-group-btn">
                                            <button type="button" class="btn btn-primary" id="loadCampaignBtn">Load</button>
                                        </div>
                                    </div>
                                </div>
                            </form>
                        </div>
                    </div>
                </div>
                <div class="panel panel-default">
                    <div class="panel-heading">
                        Choose Test
                    </div>
                    <div class="panel-body" id="chooseTest">
                        <div class="panel panel-default" id="filtersPanelContainer">
                            <div class="panel-heading card" data-toggle="collapse" data-target="#filtersPanel">
                                <span class="toggle glyphicon glyphicon-chevron-right pull-right"></span>
                                Filters
                            </div>
                            <div class="panel-body collapse" id="filtersPanel">
                                <form id="filters">
                                    <div class="row">                                    
                                        <div class="form-group col-xs-2">
                                            <label id="lbl_test" for="testFilter">Test</label>                                     
                                            <select class="multiselectelement form-control" multiple="multiple" id="testFilter"></select>
                                        </div>
                                        <div class="form-group col-xs-2">
                                            <label id="lbl_project" for="projectFilter">Project</label>
                                            <select class="multiselectelement form-control" multiple="multiple" id="projectFilter"></select>
                                        </div>
                                        <div class="form-group col-xs-2">
                                            <label id="lbl_application" for="applicationFilter">Application</label>
                                            <select class="multiselectelement form-control" multiple="" id="applicationFilter"></select>
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
                                            <label id="lbl_group" for="groupFilter">Group</label>                                     
                                            <select class="multiselectelement form-control" multiple="multiple" id="groupFilter"></select>
                                        </div>
                                    </div>
                                    <div class="row">
                                        <div class="form-group col-xs-2">
                                            <label id="lbl_campaign" for="campaignFilter">Campaign</label>                                     
                                            <select class="multiselectelement form-control" multiple="multiple" id="campaignFilter"></select>
                                        </div>
                                        <div class="form-group col-xs-2">
                                            <label id="lbl_testBattery" for="testBatteryFilter">Test Battery</label>                                     
                                            <select class="multiselectelement form-control" multiple="multiple" id="testBatteryFilter"></select>
                                        </div>
                                        <div class="form-group col-xs-2">
                                            <label id="lbl_priority" for="priorityFilter">Priority</label>                                     
                                            <select class="multiselectelement form-control" multiple="multiple" id="priorityFilter"></select>
                                        </div>
                                        <div class="form-group col-xs-2">
                                            <label id="lbl_status" for="statusFilter">Status</label>                                     
                                            <select class="multiselectelement form-control" multiple="multiple" id="statusFilter"></select>
                                        </div>
                                        <div class="form-group col-xs-2">
                                            <label id="lbl_targetRev" for="targetRevFilter">Target Revision</label>                                     
                                            <select class="multiselectelement form-control" multiple="multiple" id="targetRevFilter"></select>
                                        </div>
                                        <div class="form-group col-xs-2">
                                            <label id="lbl_targetSprint" for="targetSprintFilter">Target Sprint</label>                                     
                                            <select class="multiselectelement form-control" multiple="multiple" id="targetSprintFilter"></select>
                                        </div>
                                    </div>
                                </form>
                                <button type="button" class="btn btn-primary" id="loadFiltersBtn">Search</button>
                                <button type="button" class="btn btn-default" id="resetbutton">Reset Filters</button>
                            </div>
                        </div>
                        <div class="row">
                            <div class="form-group col-lg-11">
                                <select multiple id="testCaseList" class="form-control" style="height: 300px;"></select>
                            </div>
                            <div class="col-lg-1">
                                <div class="row" style="margin-top: 100px;">
                                    <button type="button" class="btn btn-default" id="selectAll"> Select All </button>
                                </div>

                                <!--                                <div class="row" style="margin-top: 100px;">
                                                                    <button type="button" class="btn btn-default" id="addQueue"> > </button>
                                                                </div>
                                                                <div class="row" style="margin-top: 5px;">
                                                                    <button type="button" class="btn btn-default" id="addAllQueue"> >> </button>
                                                                </div>-->
                                <div id="error" class="error-msg"></div>
                            </div>
                        </div>
                        <div class="row">
                            <div class="col-lg-6">
                                <label class="bold">Environment :</label>
                                <label class="radio-inline">
                                    <input name="envSettings" value="auto" checked type="radio">Automatic
                                </label>
                                <label class="radio-inline">
                                    <input name="envSettings" value="manual" type="radio">Manual
                                </label>
                                <form id="envSettingsAuto">
                                    <select multiple class="form-control" name="environment"></select>
                                </form>
                                <form id="envSettingsMan" style="display: none;" class="form-horizontal">
                                    <div class="form-group">
                                        <label for="myhost" class="col-sm-3 control-label bold">My Host</label>
                                        <div class="col-sm-9">
                                            <input type="text" class="form-control input-sm" id="myhost" name="myhost"/>
                                        </div>
                                    </div>
                                    <div class="form-group">
                                        <label for="mycontextroot" class="col-sm-3 control-label bold">My Context Root</label>
                                        <div class="col-sm-9">
                                            <input type="text" class="form-control input-sm" id="mycontextroot" name="mycontextroot"/>
                                        </div>
                                    </div>
                                    <div class="form-group">
                                        <label for="myloginrelativeurl" class="col-sm-3 control-label bold">My Login Relative URL</label>
                                        <div class="col-sm-9">
                                            <input type="text" class="form-control input-sm" id="myloginrelativeurl" name="myloginrelativeurl"/>
                                        </div>
                                    </div>
                                    <div class="form-group">
                                        <label for="myenvdata" class="col-sm-3 control-label bold">My Data Environment</label>
                                        <div class="col-sm-9">
                                            <select class="form-control input-sm" id="myenvdata" name="environment"></select>
                                        </div>
                                    </div>
                                </form>
                            </div>
                            <div class="col-lg-6">
                                <label for="countryList" id="countryListLabel" class="bold">Country List :</label>
                                <div id="countryList" name="countryList" style="padding-top: 4%;"></div>
                            </div>
                        </div>
                        <li class="list-group-item list-group-item-info col-lg-offset-3 col-lg-6" id="potential" style="margin-top: 25px;">
                            <span class="badge" id="potentialNumber">0</span>
                            Potential additions to the queue
                        </li>
                        <div class="row">
                            <div class="col-lg-offset-3 col-lg-6 text-center" style="margin-top: 25px;">
                                <button type="button" class="btn btn-primary" id="addQueue"> Add selection to queue </button>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="row">
                    <div class="col-lg-6">
                        <div class="panel panel-default">
                            <div class="panel-heading">
                                Robot settings
                            </div>
                            <div class="panel-body" id="robotSettings">
                                <form class="form-horizontal" id="robotSettingsForm">
                                    <div class="form-group">
                                        <label for="robotConfig" class="col-sm-3 control-label bold">Select Robot Config</label>
                                        <div class="col-sm-9">
                                            <select class="form-control input-sm" id="robotConfig" name="robotConfig"></select>
                                        </div>
                                    </div>
                                    <div class="form-group">
                                        <label for="seleniumIP" class="col-sm-3 control-label bold">Selenium Server IP</label>
                                        <div class="col-sm-9">
                                            <input type="text" class="form-control input-sm" id="seleniumIP" name="ss_ip"/>
                                        </div>
                                    </div>
                                    <div class="form-group">
                                        <label for="seleniumPort" class="col-sm-3 control-label bold">Selenium Server Port</label>
                                        <div class="col-sm-9">
                                            <input type="text" class="form-control input-sm" id="seleniumPort" name="ss_p"/>
                                        </div>
                                    </div>
                                    <div class="form-group">
                                        <label for="browser" class="col-sm-3 control-label bold">Browser</label>
                                        <div class="col-sm-9">
                                            <select class="form-control input-sm" id="browser" name="browser" multiple></select>
                                        </div>
                                    </div>
                                    <div class="form-group">
                                        <label for="version" class="col-sm-3 control-label">Version (Optional)</label>
                                        <div class="col-sm-9">
                                            <input type="text" class="form-control input-sm" id="version" name="BrowserVersion"/>
                                        </div>
                                    </div>
                                    <div class="form-group">
                                        <label for="platform" class="col-sm-3 control-label">Platform (Optional)</label>
                                        <div class="col-sm-9">
                                            <select class="form-control input-sm" id="platform" name="Platform"></select>
                                        </div>
                                    </div>
                                    <div class="form-group">
                                        <label for="screenSize" class="col-sm-3 control-label bold">Screen Size</label>
                                        <div class="col-sm-9">
                                            <select class="form-control input-sm" id="screenSize" name="screenSize"></select>
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
                        <div class="panel panel-default">
                            <div class="panel-heading">
                                Execution settings
                            </div>
                            <div class="panel-body" id="executionSettings">
                                <form class="form-horizontal"id="executionSettingsForm">
                                    <div class="form-group">
                                        <label for="tag" class="col-sm-3 control-label bold">Tag</label>
                                        <div class="col-sm-9">
                                            <input type="text" class="form-control input-sm" id="tag" name="Tag"/>
                                        </div>
                                    </div>
                                    <div class="form-group">
                                        <label for="outputFormat" class="col-sm-3 control-label bold">Output Format</label>
                                        <div class="col-sm-9">
                                            <select class="form-control input-sm" id="outputFormat" name="OutputFormat"></select>
                                        </div>
                                    </div>
                                    <div class="form-group">
                                        <label for="verbose" class="col-sm-3 control-label bold">Verbose</label>
                                        <div class="col-sm-9">
                                            <select class="form-control input-sm" id="verbose" name="Verbose"></select>
                                        </div>
                                    </div>
                                    <div class="form-group">
                                        <label for="screenshot" class="col-sm-3 control-label bold">Screenshot</label>
                                        <div class="col-sm-9">
                                            <select class="form-control input-sm" id="screenshot" name="Screenshot"></select>
                                        </div>
                                    </div>
                                    <div class="form-group">
                                        <label for="pageSource" class="col-sm-3 control-label bold">Page Source</label>
                                        <div class="col-sm-9">
                                            <select class="form-control input-sm" id="pageSource" name="PageSource"></select>
                                        </div>
                                    </div>
                                    <div class="form-group">
                                        <label for="seleniumLog" class="col-sm-3 control-label bold">Selenium Log</label>
                                        <div class="col-sm-9">
                                            <select class="form-control input-sm" id="seleniumLog" name="SeleniumLog"></select>
                                        </div>
                                    </div>
                                    <div class="form-group">
                                        <label for="synchroneous" class="col-sm-3 control-label bold">Synchroneous</label>
                                        <div class="col-sm-9">
                                            <select class="form-control input-sm" id="synchroneous" name="Synchroneous"></select>
                                        </div>
                                    </div>
                                    <div class="form-group">
                                        <label for="timeout" class="col-sm-3 control-label bold">Timeout</label>
                                        <div class="col-sm-9">
                                            <input type="text" class="form-control input-sm" id="timeout" name="timeout"/>
                                        </div>
                                    </div>
                                    <div class="form-group">
                                        <label for="retries" class="col-sm-3 control-label bold">Retries</label>
                                        <div class="col-sm-9">
                                            <select class="form-control input-sm" id="retries" name="retries"></select>
                                        </div>
                                    </div>
                                    <div class="form-group">
                                        <label for="manualExecution" class="col-sm-3 control-label bold">Manual Execution</label>
                                        <div class="col-sm-9">
                                            <select class="form-control input-sm" id="manualExecution" name="manualExecution"></select>
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
                    <input type="hidden" name="Browser" id="browserATQ">
                    <input type="hidden" name="manualURL" id="manualURLATQ">
                    <input type="hidden" name="myhost" id="myhostATQ">
                    <input type="hidden" name="mycontextroot" id="mycontextrootATQ">
                    <input type="hidden" name="myloginrelativeurl" id="myloginrelativeurlATQ">
                    <input type="hidden" name="myenvdata" id="myenvdataATQ">
                    <input type="hidden" name="robot" id="manualRobotATQ">
                    <input type="hidden" name="ss_ip" id="ss_ipATQ">
                    <input type="hidden" name="ss_p" id="ss_pATQ">
                    <input type="hidden" name="version" id="versionATQ">
                    <input type="hidden" name="platform" id="platformATQ">
                    <input type="hidden" name="Tag" id="tagATQ">
                    <input type="hidden" name="outputformat" id="outputformatATQ">
                    <input type="hidden" name="verbose" id="verboseATQ">
                    <input type="hidden" name="screenshot" id="screenshotATQ">
                    <input type="hidden" name="pageSource" id="pageSourceATQ">
                    <input type="hidden" name="seleniumLog" id="seleniumLogATQ">
                    <input type="hidden" name="synchroneous" id="synchroneousATQ">
                    <input type="hidden" name="timeout" id="timeoutATQ">
                    <input type="hidden" name="retries" id="retriesATQ">
                    <input type="hidden" name="manualExecution" id="manualExecutionATQ">
                    <input type="hidden" name="statusPage" id="screenSizeATQ">
                </form>

                <footer class="footer">
                    <div class="container-fluid" id="footer"></div>
                </footer>
            </div>
            <div class="col-lg-3 pull-right">
                <li class="list-group-item list-group-item-danger" style="display: none;" id="notValid">
                    <span class="badge" id="notValidNumber" style="cursor: pointer;"></span>
                    Some executions couldn't be added to the queue
                </li>
                <li class="list-group-item list-group-item-success" id="valid">
                    <span class="badge" id="validNumber">0</span>
                    Executions in queue
                </li>
                <div class="panel panel-default">
                    <div class="panel-heading card" data-toggle="collapse" data-target="#queuePanel">
                        <span class="toggle glyphicon glyphicon-chevron-right pull-right"></span>
                        <button class="btn btn-default btn-xs pull-right" id="resetQueue">Reset Queue</button>
                        Queue
                    </div>

                    <div class="panel-body collapse in" id="queuePanel">
                        <ul class="list-group" id="queue" style="height: 870px; overflow: hidden; overflow-y: scroll;border: 1px solid #CCC; border-radius: 4px;"></ul>
                    </div>
                </div>
                <div style="padding-top: 15px;">
                    <button type="button" class="btn btn-primary btn-lg btn-block" id="run">Run</button>
                </div>
            </div>
        </div>
    </body>
</html>
