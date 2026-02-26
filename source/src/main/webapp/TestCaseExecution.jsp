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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html class="h-full">
    <head>
        <meta name="active-menu" content="reporting">
        <meta name="active-submenu" content="TestCaseExecution.jsp">
        <meta name="active-page" content="TestCaseExecution.jsp">
        <meta name="page" content="Test Case Execution">
        <<meta content="text/html; charset=UTF-8" http-equiv="content-type">
        <%@ include file="include/global/dependenciesInclusions.html" %>
        <title id="pageTitle">Execution Detail</title>
        <script type="text/javascript" src="dependencies/Tinymce-6.7.0/tinymce.min.js"></script>
        <script type="text/javascript" src="dependencies/Bootstrap-treeview-1.2.0/js/bootstrap-treeview.js"></script>
        <script type="text/javascript" src="dependencies/Chart.js-2.9.3/Chart.min.js"></script>
        <script type="text/javascript" src="js/pages/TestCaseExecution.js?v=${appVersion}"></script>
        <script type="text/javascript" src="js/transversalobject/TestCaseExecutionQueue.js?v=${appVersion}"></script>
        <script type="text/javascript" src="js/transversalobject/TestCase.js?v=${appVersion}"></script>
        <script type="text/javascript" src="js/transversalobject/File.js?v=${appVersion}"></script>
        <script type="text/javascript" src="js/transversalobject/Robot.js?v=${appVersion}"></script>
        <script type="text/javascript" src="js/transversalobject/Application.js?v=${appVersion}"></script>
        <script type="text/javascript" src="js/transversalobject/TestCaseSimpleExecution.js?v=${appVersion}"></script>
        <link rel="stylesheet" type="text/css" href="css/pages/TestCaseExecution.css?v=${appVersion}">
        <link href="https://cdn.jsdelivr.net/css-toggle-switch/latest/toggle-switch.css" rel="stylesheet"/>

    </head>
    <body x-data x-cloak class="crb_body">
        <jsp:include page="include/global/header2.html"/>
        <jsp:include page="include/global/modalInclusions.jsp"/>
        <%@ include file="include/utils/modal-generic.html" %>
        <%@ include file="include/pages/testcasescript/manageProperties.html" %>
        <%@ include file="include/transversalobject/TestCaseExecutionQueue.html" %>
        <%@ include file="include/transversalobject/TestCase.html" %>
        <%@ include file="include/transversalobject/File.html" %>
        <jsp:include page="include/transversal/TestCaseSimpleExecution.html"/>
        <%@ include file="include/transversalobject/Robot.html" %>
        <%@ include file="include/transversalobject/Application.html" %>
        <%@ include file="include/transversal/Invariant.html" %>
        <jsp:include page="include/templates/selectDropdown.html"/>
        <main class="crb_main" :class="$store.sidebar.expanded ? 'crb_main_sidebar-expanded' : 'crb_main_sidebar-collapsed'">

            <%@ include file="include/global/messagesArea.html" %>
            <h1 class="page-title-line">Execution Detail</h1>
            <div id="testCaseConfig">
                <div>
                    <div id="divPanelDefault">
                        <div class="crb_card sticky top-0 z-50" id="executionHeader">
                            <div class="row">
                                <div class="col-lg-12">
                                    <div class="input-group">
                                        <div id="false-negative-bar" class="progress" style="height: 22px; margin-bottom: 0px; display: none;">
                                            <div class="progress-bar statusOK" role="progressbar" style="width: 100%;" aria-valuenow="0" aria-valuemin="0" aria-valuemax="100">
                                                <span class="sr-only"></span>FALSE NEGATIVE
                                            </div>
                                        </div>
                                        <div class="progress" style="margin-bottom: 0px;">
                                            <div id="progress-bar" style="margin-bottom: 0px;" class="progress-bar statusPE btn-group" role="progressbar" aria-valuenow="0" aria-valuemin="0" aria-valuemax="100">
                                                <span class="sr-only"></span>
                                            </div>
                                        </div>
                                        <span class="input-group-btn">
                                            <button id="falseNegative" name="falseNegative" class="btn btn-xs" title="Declare/Undeclare this execution as a False Negative"><span class="glyphicon glyphicon-ok"></span></button>
                                        </span>
                                    </div>
                                </div>
                            </div>
                            <div class="row">
                                <div class="col-lg-5 pull-left">
                                    <div class="text-rap">
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
                                        <span id="duration" class="" data-toggle='tooltip' data-original-title=''></span>
                                    </div>
                                    <div class="text-wrap">
                                        <span id="AppName"> </span>
                                        <img id="AppLogo" style="height:20px; overflow:hidden; text-overflow:clip; border: 0px; padding:0; margin:0"></img>
                                        <span id="tcDescription"
                                              style="font-size:.9em;margin:0px;line-height:1;height:.95em;"></span>
                                        <span id="externalRef" style="display: none;cursor:pointer" class="label label-default">New</span>
                                    </div>
                                    <div class="text-wrap" id="returnMessage">
                                        <span id="exReturnMessage"
                                              style="font-size:.9em;margin:0px;line-height:1;height:.95em;font-weight: 900;word-wrap: break-word"></span>
                                    </div>
                                    <div class="text-wrap" id="returnMessage">
                                        <img id="exBrowserLogo"
                                             style="height:20px; overflow:hidden; text-overflow:clip; border: 0px; padding:0; margin:0"
                                             src="">
                                        <span id="exBrowser"
                                              style="font-size:.9em;margin:0px;line-height:1;height:.95em;font-weight: 900;word-wrap: break-word"></span>
                                        <img id="exOSLogo"
                                             style="height:20px; overflow:hidden; text-overflow:clip; border: 0px; padding:0; margin:0"
                                             src="">
                                        <span id="exOS"
                                              style="font-size:.9em;margin:0px;line-height:1;height:.95em;font-weight: 900;word-wrap: break-word"></span>
                                    </div>
                                </div>
                                <div class="col-lg-7" style="padding: 0px;">
                                    <div id="RefreshQueueButton">
                                        <button id="refreshQueue" class="btn btn-default">Refresh</button>
                                        <button id="editQueue" class="btn btn-default">Edit Queue Entry</button>
                                    </div>
                                    <div id="TestCaseButton">
                                        <div class="btn-group pull-right" role="group"
                                             aria-label="Button group with nested dropdown" style="margin-top: 10px;">

                                            <div class="btn-group marginRight5">
                                                <button id="btnGroupDrop1" style="border-radius:4px" type="button"
                                                        class="btn btn-default dropdown-toggle" data-toggle="dropdown"
                                                        aria-haspopup="true" aria-expanded="false">
                                                    <span class="glyphicon glyphicon-option-horizontal"></span>
                                                </button>
                                                <div class="dropdown-menu" aria-labelledby="btnGroupDrop1">
                                                    <a>
                                                        <button class="btn btn-default pull-left" id="deleteTestCaseExecution"
                                                                style="margin-left: 5px; margin-left: 5px;" disabled>
                                                            <span class="glyphicon glyphicon-trash"></span>Delete
                                                        </button>
                                                    </a>
                                                    <a>
                                                        <button class="btn btn-default pull-left" id="lastExecution"
                                                                style="margin-left: 5px; margin-right: 5px;">
                                                            <span class="glyphicon glyphicon-list"></span> Last Executions
                                                        </button>
                                                    </a>
                                                    <a>
                                                        <button class="btn btn-default pull-left" id="lastExecutionwithEnvCountry"
                                                                style="margin-left: 5px; margin-right: 5px;">
                                                            <span class="glyphicon glyphicon-list"></span> Last Executions With Country Env
                                                        </button>
                                                    </a>
                                                    <a>
                                                        <button class="btn btn-default pull-left" id="ExecutionByTag"
                                                                style="margin-left: 5px; margin-right: 5px;">
                                                            <span class="glyphicon glyphicon-tasks"></span> Campaign Report
                                                        </button>
                                                    </a>
                                                    <a>
                                                        <button class="btn btn-default pull-left" id="ExecutionQueue"
                                                                style="margin-left: 5px; margin-right: 5px;">
                                                            <span class="glyphicon glyphicon-eye-open"></span> See Queue Parameters
                                                        </button>
                                                    </a>
                                                    <a>
                                                        <button class="btn btn-default pull-left" id="ExecutionQueueByTag"
                                                                style="margin-left: 5px; margin-right: 5px;">
                                                            <span class="glyphicon glyphicon-list"></span> See Queue By Tag
                                                        </button>
                                                    </a>
                                                    <a>
                                                        <button class="btn btn-default pull-left" id="sessionLinkHeader"
                                                                style="margin-left: 5px; margin-right: 5px;">Link External Provider
                                                        </button>
                                                    </a>
                                                </div>
                                            </div>
                                            <div class="btn-group ">
                                                <a>
                                                    <button class="btn btn-default" id="editTcStepInfo"
                                                            style="margin-left: 5px;">
                                                        <span class="glyphicon glyphicon-pencil"></span> Edit Test Case from the current Step
                                                    </button>
                                                </a>
                                            </div>
                                            <div class="btn-group ">
                                                <a>
                                                    <button class="btn btn-default" id="editTcHeader"
                                                            style="margin-left: 5px; margin-right: 5px;">
                                                        <span class="glyphicon glyphicon-edit"></span> Edit Test Case Header
                                                    </button>
                                                </a>
                                            </div>
                                            <div class="btn-group" id="runTestCasePopover">
                                                <a>
                                                    <button class="btn btn-default pull-right" id="runTestCase"
                                                            style="margin-left: 5px;" data-toggle="tooltip">
                                                        <span class="glyphicon glyphicon-play"></span>
                                                    </button>
                                                </a>
                                            </div>
                                            <div class="btn-group">
                                                <a>
                                                    <button class="btn btn-default pull-right" id="rerunFromQueueandSee"
                                                            style="margin-left: 5px;">
                                                        <span class="glyphicon glyphicon-forward"></span>
                                                    </button>
                                                </a>
                                            </div>
                                            <div class="btn-group">
                                                <a>
                                                    <button class="btn btn-default pull-left" id="lastExecutionoT"
                                                            style="margin-left: 5px; margin-right: 5px;">
                                                        <span class="glyphicon glyphicon-stats"></span> Reporting over Time
                                                    </button>
                                                </a>
                                            </div>
                                            <div class="btn-group">
                                                <a>
                                                    <button class="btn btn-default" id="saveTestCaseExecution" disabled
                                                            style="margin-left: 5px;display:none">
                                                        <span class="glyphicon glyphicon-save"></span>
                                                    </button>
                                                </a>
                                            </div>
                                        </div>
                                        <div class="side-item pull-right"></div>
                                    </div>
                                </div>
                            </div>
                            <div class="clearfix"></div>
                        </div>
                    </div>
                </div>
                <div x-data="{ tab: 'tabSteps' }" class="w-full" id="testCaseDetails">
                <!-- Tabs -->
                            <div class="w-full flex bg-slate-200 dark:bg-slate-700 p-1 rounded-lg shadow-sm mb-8 h-10">
                                <!-- Steps -->
                                <button @click="tab = 'tabSteps'"
                                        :class="tab === 'tabSteps' ? 'bg-slate-50 font-semibold dark:bg-slate-900' : 'bg-slate-200 dark:bg-slate-700 text-slate-700 hover:text-slate-900 dark:text-slate-300 dark:hover:text-white'"
                                        class="flex-1 flex items-center justify-center gap-2 px-4 py-3 rounded-md transition-colors duration-200">
                                    <i data-lucide="list-tree" class="w-4 h-4"></i>Steps
                                </button>
                                <!-- Properties -->
                                <button @click="tab = 'tabProperties'"
                                        :class="tab === 'tabProperties' ? 'bg-slate-50 font-semibold dark:bg-slate-900' : 'bg-slate-200 dark:bg-slate-700 text-slate-700 hover:text-slate-900 dark:text-slate-300 dark:hover:text-white'"
                                        class="flex-1 flex items-center justify-center gap-2 px-4 py-3 rounded-md transition-colors duration-200">
                                    <i data-lucide="database" class="w-4 h-4"></i>Properties
                                </button>
                                <!-- Detail -->
                                <button @click="tab = 'tabDetail'"
                                        :class="tab === 'tabDetail' ? 'bg-slate-50 font-semibold dark:bg-slate-900' : 'bg-slate-200 dark:bg-slate-700 text-slate-700 hover:text-slate-900 dark:text-slate-300 dark:hover:text-white'"
                                        class="flex-1 flex items-center justify-center gap-2 px-4 py-3 rounded-md transition-colors duration-200">
                                    <i data-lucide="bug" class="w-4 h-4"></i>Bug
                                </button>
                                <!-- Environment -->
                                <button @click="tab = 'tabEnv'"
                                        :class="tab === 'tabEnv' ? 'bg-slate-50 font-semibold dark:bg-slate-900' : 'bg-slate-200 dark:bg-slate-700 text-slate-700 hover:text-slate-900 dark:text-slate-300 dark:hover:text-white'"
                                        class="flex-1 flex items-center justify-center gap-2 px-4 py-3 rounded-md transition-colors duration-200">
                                    <i data-lucide="earth" class="w-4 h-4"></i>Environment
                                </button>
                                <!-- Robot -->
                                <button @click="tab = 'tabRobot'"
                                        :class="tab === 'tabRobot' ? 'bg-slate-50 font-semibold dark:bg-slate-900' : 'bg-slate-200 dark:bg-slate-700 text-slate-700 hover:text-slate-900 dark:text-slate-300 dark:hover:text-white'"
                                        class="flex-1 flex items-center justify-center gap-2 px-4 py-3 rounded-md transition-colors duration-200">
                                    <i data-lucide="bot" class="w-4 h-4"></i>Robot
                                </button>
                                <!-- Robot -->
                                <button @click="tab = 'tabDep'"
                                        :class="tab === 'tabDep' ? 'bg-slate-50 font-semibold dark:bg-slate-900' : 'bg-slate-200 dark:bg-slate-700 text-slate-700 hover:text-slate-900 dark:text-slate-300 dark:hover:text-white'"
                                        class="flex-1 flex items-center justify-center gap-2 px-4 py-3 rounded-md transition-colors duration-200">
                                    <i data-lucide="link" class="w-4 h-4"></i>Dependencies
                                </button>
                                <!-- Network -->
                                <button @click="tab = 'tabNetwork'"
                                    :class="tab === 'tabNetwork' ? 'bg-slate-50 font-semibold dark:bg-slate-900' : 'bg-slate-200 dark:bg-slate-700 text-slate-700 hover:text-slate-900 dark:text-slate-300 dark:hover:text-white'"
                                    class="flex-1 flex items-center justify-center gap-2 px-4 py-3 rounded-md transition-colors duration-200">
                                    <i data-lucide="network" class="w-4 h-4"></i>Network
                                </button>
                                <!-- Network -->
                                <button @click="tab = 'tabTraca'"
                                        :class="tab === 'tabTraca' ? 'bg-slate-50 font-semibold dark:bg-slate-900' : 'bg-slate-200 dark:bg-slate-700 text-slate-700 hover:text-slate-900 dark:text-slate-300 dark:hover:text-white'"
                                        class="flex-1 flex items-center justify-center gap-2 px-4 py-3 rounded-md transition-colors duration-200">
                                    <i data-lucide="user" class="w-4 h-4"></i>Traceability
                                </button>
                            </div>
                    <div class="tab-content">

                        <!-- Steps -->
                    <div x-show="tab === 'tabSteps'" class="w-full">
                        <div id="tabSteps" class="flex flex-wrap">
                                <nav class="col-lg-4 col-md-12 marginBottom20" id="nav-execution" style="z-index:1;">
                                    <div id="list-wrapper" style="top:107px;">
                                        <div id="steps">
                                            <ul class="list-group step-list side-item" id="steps"
                                                style="max-height: 500px;overflow-y: auto"></ul>
                                        </div>
                                    </div>
                                </nav>
                                <div class="col-lg-8 col-md-12" id="contentWrapper"
                                     style="padding-left: 30px;border-left: 1px solid rgb(39,188,253);">
                                    <div id="stepConditionMessage">
                                        <div class="row step">
                                            <div class="content col-lg-12">
                                                <div id="stepConditionMessageContent" class="row text-center"></div>
                                            </div>
                                        </div>
                                    </div>
                                    <div id="stepContent" style="display:none">
                                        <div class="row step">
                                            <div class="content col-lg-12">
                                                <div id="stepHeader" style="margin-bottom: 15px;">
                                                    <div id="stepInfo" class="row" style="display: none;">
                                                    </div>
                                                </div>
                                                <div class="fieldRow marginTop25" id="stepHiddenRow" style="display: none;">
                                                    <div class="row" id="stepRow1">
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
                                                            <input class="form-control input-sm" readonly
                                                                   id="stepConditionVal1Init">
                                                        </div>
                                                        <div class="col-lg-4 form-group">
                                                            <label>Param2 Init</label>
                                                            <input class="form-control input-sm" readonly
                                                                   id="stepConditionVal2Init">
                                                        </div>
                                                        <div class="col-lg-2 form-group">
                                                            <label>Param3 Init</label>
                                                            <input class="form-control input-sm" readonly
                                                                   id="stepConditionVal3Init">
                                                        </div>
                                                    </div>
                                                    <div class="row" id="stepRow4">
                                                        <div class="col-lg-2 form-group">
                                                            <label>Condition Operation</label>
                                                            <input class="form-control input-sm" readonly
                                                                   id="stepConditionOperator">
                                                        </div>
                                                        <div class="col-lg-4 form-group">
                                                            <label>Param1</label>
                                                            <input class="form-control input-sm" readonly
                                                                   id="stepConditionVal1">
                                                        </div>
                                                        <div class="col-lg-4 form-group">
                                                            <label>Param2</label>
                                                            <input class="form-control input-sm" readonly
                                                                   id="stepConditionVal2">
                                                        </div>
                                                        <div class="col-lg-2 form-group">
                                                            <label>Param3</label>
                                                            <input class="form-control input-sm" readonly
                                                                   id="stepConditionVal3">
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

                        <div x-show="tab === 'tabProperties'" class="w-full">
                            <div id="tabProperties">
                                <div class="property-table">
                                    <div class="" id="propPanelWrapper">
                                        <div class="panel-body in" id="propertiesPanel">
                                            <div id="propTable" class="list-group">
                                            </div>
                                            <div class="row" id="secondaryPropTableHeader" class="list-group"
                                                 style="display:none;">
                                                <div class="col-sm-6">
                                                    <button id="showSecondaryProp" type="button"
                                                            class="btn btn-default center-block"><span
                                                            class="glyphicon glyphicon-collapse-down"></span> Show <span
                                                            id="secondaryPropCount"></span> Secondary Properties
                                                    </button>
                                                </div>
                                                <div class="col-sm-6">
                                                    <button id="hideSecondaryProp" type="button"
                                                            class="btn btn-default center-block"><span
                                                            class="glyphicon glyphicon-collapse-up"></span> Hide Secondary
                                                        Properties
                                                    </button>
                                                </div>
                                            </div>
                                            <div id="secondaryPropTable" style="margin-top:20px;">
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div x-show="tab === 'tabDetail'" class="w-full">
                        <div id="tabDetail">
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
                                        <div class="input-group">
                                            <input type="text" class="form-control" id="controlstatus2" placeholder="Control Status" readonly>
                                            <span class="input-group-btn">
                                                <button id="falseNegative" name="falseNegative" class="btn btn-default" title="Declare/Undeclare this execution as a False Negative"><span class="glyphicon glyphicon-ok"></span></button>
                                            </span>
                                        </div>
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
                                <div id="bugsSection" class="col-lg-6">
                                    <div class="form-group">
                                        <label>Bugs</label>
                                        <div id="bugs" style="font-size:14px"></div>
                                        <div id="bugButtons"></div>
                                    </div>
                                </div>

                                <div class="col-lg-6">
                                    <div class="form-group">
                                        <label for="tag">Tag</label>
                                        <div class="input-group">
                                            <input type="text" class="form-control" id="tag" placeholder="Tag" readonly>
                                            <span class="input-group-btn">
                                                <button id="editTags" class="btn btn-default">Edit</button>
                                                <button id="saveTag" class="btn btn-primary" style="display : none;">Save</button>
                                                <a href=""><button id="openTag" class="btn btn-default"><span class="glyphicon glyphicon-new-window"></span> Open</button></a>
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
                                        <input type="text" class="form-control" id="cerberusversion"
                                               placeholder="Cerberus Version" readonly>
                                    </div>
                                </div>
                            </div>

                            <div class="row">
                                <div class="col-sm-4">
                                    <div class="form-group">
                                        <label for="exetest">Test</label>
                                        <input type="text" class="form-control" id="exetest" readonly>
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
                                        <input type="text" class="form-control" id="conditionVal2TC" readonly>
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
                            <div class="row">
                                <div class="col-sm-6" id="tcDetailFileContentField">
                                </div>
                            </div>
                        </div>
                        </div>

                        <!-- Robot -->
                        <div x-show="tab === 'tabRobot'" class="w-full">
                        <div id="tabRobot">
                            <div class="row">
                                <div class="col-sm-3">
                                    <div class="form-group">
                                        <label for="robot">Robot</label>
                                        <div class="input-group">
                                            <input type="text" class="form-control" id="robot" placeholder="Robot" readonly>
                                            <span class="input-group-btn">
                                                <button id="editRobot" class="btn btn-default"><span class="glyphicon glyphicon-pencil"></span></button>
                                            </span>
                                        </div>
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
                                        <input type="text" class="form-control" id="robotexe" placeholder="Robot Executor"
                                               readonly>
                                    </div>
                                </div>
                                <div class="col-sm-3">
                                    <div class="form-group">
                                        <label for="robothost">Robot Host</label>
                                        <input type="text" class="form-control" id="robothost" placeholder="Robot Host"
                                               readonly>
                                    </div>
                                </div>
                                <div class="col-sm-2">
                                    <div class="form-group">
                                        <label for="robotport">Robot Port</label>
                                        <input type="text" class="form-control" id="robotport" placeholder="Robot Port"
                                               readonly>
                                    </div>
                                </div>
                                <div class="col-sm-4">
                                    <div class="form-group">
                                        <label for="robotsessionid">Robot Session</label>
                                        <div class="input-group">
                                            <input type="text" class="form-control" id="robotsessionid"
                                                   placeholder="Robot Session Id" readonly>
                                            <span class="input-group-btn">
                                                <a><button class="btn btn-default pull-left" id="sessionLink">Link External Provider</button></a>
                                            </span>
                                        </div>
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
                                        <input type="text" class="form-control" id="screenSize" placeholder="Screen Size"
                                               readonly>
                                    </div>
                                </div>
                            </div>
                            <div class="row">
                                <div class="col-sm-6" id="tcFileContentField">
                                </div>
                            </div>
                        </div>
                        </div>

                         <!-- Environment -->
                        <div x-show="tab === 'tabEnv'" class="w-full">
                        <div id="tabEnv">
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
                                        <div class="input-group">
                                            <input type="text" class="form-control" id="application" placeholder="Application" readonly>
                                            <span class="input-group-btn">
                                                <button id="editApplication" class="btn btn-default"><span class="glyphicon glyphicon-pencil"></span></button>
                                            </span>
                                        </div>
                                    </div>
                                </div>
                                <div class="col-sm-3">
                                    <div class="form-group">
                                        <label for="country">Country</label>
                                        <div class="input-group">
                                            <input type="text" class="form-control" id="country" placeholder="Country" readonly>
                                            <span class="input-group-btn">
                                                <button id="editCountry" class="btn btn-default"><span class="glyphicon glyphicon-pencil"></span></button>
                                            </span>
                                        </div>
                                    </div>
                                </div>
                                <div class="col-sm-2">
                                    <div class="form-group">
                                        <label for="environment">Environment</label>
                                        <div class="input-group">
                                            <input type="text" class="form-control" id="environment" placeholder="Environment" readonly>
                                            <span class="input-group-btn">
                                                <button id="editEnvironment" class="btn btn-default"><span class="glyphicon glyphicon-pencil"></span></button>
                                            </span>
                                        </div>
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

                <!-- Dependencies -->
                        <div x-show="tab === 'tabDep'" class="w-full">
                        <div id="tabDep">
                            <div id="listPanelDep">
                                <div class="row">
                                    <div class="col-sm-12">
                                        <table class="table table-hover nomarginbottom" id="depTable">
                                            <tbody id="depTableBody">
                                            </tbody>
                                        </table>
                                    </div>
                                </div>
                            </div>
                        </div>
                        </div>

                        <!-- Network -->
                        <div x-show="tab === 'tabNetwork'" class="w-full">
                        <div id="tabNetwork">

                            <div class="crb_card" id="filterContainer">
                                <div class="" id="FilterIndex">
                                    <label for="selectIndex">Index :</label>
                                    <div class="row" id="indexFilter">
                                        <div class="input-group">
                                            <select class="form-control col-lg-12" name="Index" id="selectIndex"
                                                    multiple="multiple"></select>
                                            <div class="input-group-btn">
                                                <button type="button" class="btn btn-default" style="margin-left: 10px;"
                                                        id="loadbutton" onclick="updateAllGraphs()">Load
                                                </button>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <div class="grid grid-cols-1 md:grid-cols-3 gap-6" id="NS1">

                                <!-- CARD 1 -->
                                <div class="crb_card p-4 flex flex-col">
                                    <div class="flex-1 flex items-center justify-center">
                                        <canvas id="myChart1"></canvas>
                                    </div>

                                    <!-- ghost footer -->
                                    <div class="h-[42px] invisible"></div>
                                </div>

                                <!-- CARD 2 -->
                                <div class="crb_card p-4 flex flex-col">
                                    <div class="flex-1 flex items-center justify-center">
                                        <canvas id="myChart2"></canvas>
                                    </div>

                                    <!-- ghost footer -->
                                    <div class="h-[42px] invisible"></div>
                                </div>

                                <!-- CARD 3 (référence) -->
                                <div class="crb_card p-4 flex flex-col">
                                    <div class="flex-1">
                                        <div class="flex gap-2 mb-2">
                                            <button id="sortSize" class="btn btn-default">by Size</button>
                                            <button id="sortRequest" class="btn btn-default">by Request</button>
                                            <button id="sortTime" class="btn btn-default">by Max Time</button>
                                        </div>
                                        <canvas id="myChart3"></canvas>
                                    </div>
                                </div>

                            </div>



                            <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mt-6" id="NS2Panel">

                                <!-- LEFT : Unknown list (1/3) -->
                                <div class="crb_card p-4 flex flex-col md:col-span-1 h-[420px]">

                                    <!-- Header -->
                                    <div class="flex items-center gap-2 font-medium text-lg mb-3 shrink-0">
                                        <i data-lucide="help-circle" class="w-4 h-4 text-blue-600"></i>
                                        <span>Unknown Requests</span>
                                    </div>

                                    <!-- Scrollable content -->
                                    <div class="flex-1 overflow-y-auto pr-1">
                                        <ul id="detailUnknownList" class="space-y-1 text-sm"></ul>
                                    </div>

                                </div>

                                <div class="crb_card p-4 flex flex-col md:col-span-2 h-[420px]">

                                    <!-- Header -->
                                    <div class="flex items-center gap-2 font-medium text-lg mb-3 shrink-0">
                                        <svg class="w-4 h-4 text-blue-600" ...></svg>
                                        <span>Unknown Requests Distribution</span>
                                    </div>

                                    <!-- Chart container -->
                                    <div class="flex-1 relative overflow-hidden">
                                        <canvas id="myChart4" class="w-full h-full"></canvas>
                                    </div>

                                </div>

                            </div>


                            <div class="panel panel-default hidden" id="NS3Panel">
                                <div class="panel-heading card" data-target="#NS3">
                                    <span class="fa fa-pie-chart fa-fw"></span>
                                    <label id="ns3Label">Requests List</label>
                                </div>
                                <div class="panel-body in" id="NS3">
                                    <table id="requestTable" class="table table-hover display"
                                           name="requestTable"></table>
                                </div>
                            </div>

                            <div x-show="tab === 'tabNetwork'" x-data="harViewer()" x-ref="harViewer" class="mt-4">
                                <template x-if="loading">
                                    <div class="text-slate-400 italic">
                                        Loading network HAR…
                                    </div>
                                </template>

                                <template x-if="!har && !loading">
                                    <div class="text-slate-400 italic">
                                        No network data available
                                    </div>
                                </template>

                                <div x-show="har" class="flex w-full mt-4 gap-6">

                                    <!-- LEFT : LIST -->
                                    <div class="crb_card w-2/3 flex flex-col">

                                        <!-- Filters -->
                                        <div class="p-3 flex gap-2 items-center">
                                            <input x-model="filter"
                                                   placeholder="Filter URL..."
                                                   class="flex h-10 items-center justify-between w-full rounded-md border px-3 py-2 text-sm
                                                   dark:bg-slate-800 dark:border-slate-600 dark:text-slate-300 dark:hover:bg-slate-700
                                                   bg-white border-slate-300 text-slate-900 hover:bg-slate-100 transition-all"/>

                                            <select x-model="statusFilter" class="flex h-10 items-center justify-between w-full rounded-md border px-3 py-2 text-sm
                                                   dark:bg-slate-800 dark:border-slate-600 dark:text-slate-300 dark:hover:bg-slate-700
                                                   bg-white border-slate-300 text-slate-900 hover:bg-slate-100 transition-all">
                                                <option value="all">All</option>
                                                <option value="2xx">2xx</option>
                                                <option value="4xx">4xx</option>
                                                <option value="5xx">5xx</option>
                                            </select>

                                            <select x-model="sortBy" class="flex h-10 items-center justify-between w-full rounded-md border px-3 py-2 text-sm
                                                   dark:bg-slate-800 dark:border-slate-600 dark:text-slate-300 dark:hover:bg-slate-700
                                                   bg-white border-slate-300 text-slate-900 hover:bg-slate-100 transition-all">
                                                <option value="time">Time</option>
                                                <option value="status">Status</option>
                                                <option value="size">Size</option>
                                            </select>
                                        </div>

                                        <!-- Table -->
                                        <div class="flex-1 overflow-auto">
                                            <table class="w-full text-xs">
                                                <thead class="sticky top-0">
                                                    <tr>
                                                        <th class="px-2 py-2 text-left">Method</th>
                                                        <th class="px-2 py-2 text-left">URL</th>
                                                        <th class="px-2 py-2">Status</th>
                                                        <th class="px-2 py-2">Time</th>
                                                        <th class="px-2 py-2">Size</th>
                                                    </tr>
                                                </thead>
                                                <tbody>
                                                    <template x-for="entry in filteredEntries" :key="entry._id">
                                                        <tr @click="select(entry)"
                                                            :class="selected === entry ? 'bg-sky-100/40 dark:bg-sky-900/40' : 'dark:hover:bg-slate-800 hover:bg-slate-200'"
                                                            class="h-8 cursor-pointer border-b dark:border-slate-800 border-slate-200">
                                                            <td x-text="entry.request.method" class="px-2 py-1"></td>
                                                            <td x-text="entry.request.url" class="px-2 py-1 truncate max-w-[400px]"></td>
                                                            <td class="px-2 py-1 text-center">
                                                                <span x-text="entry.response.status" :class="statusClass(entry.response.status)" class="px-2 py-1 rounded-lg text-[10px]"></span>
                                                            </td>
                                                            <td x-text="entry.time + ' ms'" class="px-2 py-1 text-center"></td>
                                                            <td x-text="entry.response.content.size + ' B'" class="px-2 py-1 text-center"></td>
                                                        </tr>
                                                    </template>
                                                </tbody>
                                            </table>
                                        </div>
                                    </div>

                                    <!-- RIGHT : DETAILS -->
                                    <div class="crb_card w-1/3 p-4 overflow-auto crb-pre">

                                        <h2 class="text-sm font-bold mb-2">Request</h2>
                                        <pre x-html="highlightJson(selected.request)"
                                            class="json-view text-xs bg-slate-800 p-2 rounded mb-4 overflow-auto"></pre>

                                        <h2 class="text-sm font-bold mb-2">Response</h2>
                                        <pre x-html="highlightJson(selected.response)"
                                            class="json-view text-xs bg-slate-800 p-2 rounded overflow-auto"></pre>

                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- Traceability -->
                        <div x-show="tab === 'tabTraca'" class="w-full">
                        <div id="tabTraca">
                            <div id="listPanelTraca">
                                <div class="row">
                                    <div class="form-group col-sm-6">
                                        <label name="lbl_datecreated" for="datecreated">datecreated</label>
                                        <div>
                                            <input id="datecreated" name="datecreated" class="form-control"
                                                   readonly="readonly"/>
                                        </div>
                                    </div>
                                    <div class="form-group col-sm-6">
                                        <label name="lbl_usrcreated" for="usrcreated">usrcreated</label>
                                        <div>
                                            <input id="usrcreated" name="usrcreated" class="form-control" readonly="readonly"/>
                                        </div>
                                    </div>
                                </div>
                                <div class="row">
                                    <div class="form-group col-sm-6">
                                        <label name="lbl_datemodif" for="datemodif">datemodif</label>
                                        <div>
                                            <input id="datemodif" name="datemodif" class="form-control" readonly="readonly"/>
                                        </div>
                                    </div>
                                    <div class="form-group col-sm-6">
                                        <label name="lbl_usrmodif" for="usrmodif">usrmodif</label>
                                        <div>
                                            <input id="usrmodif" name="usrmodif" class="form-control" readonly="readonly"/>
                                        </div>
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
            <jsp:include page="include/global/aiBottomBar.html"/>
        </main>
    </div>
</body>
</html>
