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
<html class="h-full">
    <head>
        <meta name="active-menu" content="maintain">
        <meta name="active-submenu" content="TestCaseList.jsp">
        <meta name="active-page" content="TestCaseScript.jsp">
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
    <body x-data x-cloak class="crb_body">
        <jsp:include page="include/global/header2.html"/>
        <jsp:include page="include/global/modalInclusions.jsp"/>
        <main class="crb_main" x-init="$store.sidebar.expanded = false" :class="$store.sidebar.expanded ? 'crb_main_sidebar-expanded' : 'crb_main_sidebar-collapsed'">
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

            <div class="flex gap-3">
                <button id="backToTestCaseListButton" type="button"
                    onclick="window.location.href='TestCaseList.jsp'"
                    class="inline-flex items-center justify-center h-9 w-9 rounded-full transition-colors
                           hover:bg-gray-200 dark:hover:bg-gray-700
                           focus-visible:outline-none focus-visible:ring-2
                           focus-visible:ring-sky-500 focus-visible:ring-offset-2">

                    <svg xmlns="http://www.w3.org/2000/svg"
                         viewBox="0 0 24 24" fill="none" stroke="currentColor"
                         stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="h-5 w-5">
                        <path d="m12 19-7-7 7-7"></path>
                        <path d="M19 12H5"></path>
                    </svg>
                </button>

                <h1 class="page-title-line">Test Case Script</h1>
            </div>

            <div>
                <div>
                    <div id="divPanelDefault">
                        <div class="crb_card sticky top-0 z-50" id="testCaseTitle">
                            <div>
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
                                                <button id="btnGroupDrop1" style="border-radius:4px" type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
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
                        <div x-data="testCaseScript()" x-init="init()" x-ref="tabsScriptEdit" class="w-full">
                            <!-- Tabs -->
                            <div class="w-full flex bg-slate-200 dark:bg-slate-700 p-1 rounded-lg shadow-sm mb-8 h-10">
                                <!-- Steps -->
                                <button @click="setTab('steps')" id="editTabSteps"
                                        :class="tab === 'steps' ? 'bg-slate-50 font-semibold dark:bg-slate-900' : 'bg-slate-200 dark:bg-slate-700 text-slate-700 hover:text-slate-900 dark:text-slate-300 dark:hover:text-white'"
                                        class="flex-1 flex items-center justify-center gap-2 px-4 py-3 rounded-md transition-colors duration-200">
                                    <i data-lucide="users" class="w-4 h-4"></i>Steps
                                </button>
                                <!-- Properties -->
                                <button @click="setTab('properties')"  id="editTabProperties"
                                        :class="tab === 'properties' ? 'bg-slate-50 font-semibold dark:bg-slate-900' : 'bg-slate-200 dark:bg-slate-700 text-slate-700 hover:text-slate-900 dark:text-slate-300 dark:hover:text-white'"
                                        class="flex-1 flex items-center justify-center gap-2 px-4 py-3 rounded-md transition-colors duration-200">
                                    <i data-lucide="lock" class="w-4 h-4"></i>Properties
                                </button>
                                <!-- Inherited Properties -->
                                <button @click="setTab('inheritedproperties')" id="editTabInheritedProperties"
                                        :class="tab === 'inheritedproperties' ? 'bg-slate-50 font-semibold dark:bg-slate-900' : 'bg-slate-200 dark:bg-slate-700 text-slate-700 hover:text-slate-900 dark:text-slate-300 dark:hover:text-white'"
                                        class="flex-1 flex items-center justify-center gap-2 px-4 py-3 rounded-md transition-colors duration-200">
                                    <i data-lucide="lock" class="w-4 h-4"></i>Inherited Properties
                                </button>
                            </div>
                            <!-- Steps -->
                            <div x-show="tab === 'steps'" class="">
                                <div id="tabSteps">
                                    <nav class="col-lg-4 col-md-12" id="nav-execution">
                                        <div id="list-wrapper" style="top:107px;">
                                            <div id="stepsWrapper">
                                                <!--<h4>Steps</h4>-->
                                                <ul class="list-group step-list side-item nav nav-pills" id="steps" style="max-height: 600px;overflow-y: auto"></ul>
                                            </div>
                                            <div id="tcButton">
                                                <!--<h4>Actions</h4>-->
                                                <button class="btn btn-block btnLightTurquoise marginTop25 marginBottom20" id="addStep" disabled>Add Step</button>
                                                <!--                                        <button class="btn btn-info btn-block marginTop25" id="duplicateStep" disabled>Duplicate Step</button>-->
                                            </div>
                                        </div>
                                    </nav>
                                    <div class="col-lg-8 col-md-12" id="contentWrapper">
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
                                                        <button class="btn add-btn btnLightOrange" id="stepPlus">
                                                            <span class="glyphicon glyphicon-cog"></span>
                                                        </button>
                                                        <button class="btn add-btn btnLightRed" id="deleteStep" disabled>
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
                            </div>
                            <!--  Properties -->
                            <div x-show="tab === 'properties'" class="">
                                <div id="tabProperties">
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
                            </div><!-- Inherited Properties -->
                            <div x-show="tab === 'inheritedproperties'" class="">
                                <div id="tabInheritedProperties">
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
                                                <div class="panel-body" id="inheritedPropertiesPanel">
                                                    <div id="inheritedPropPanel" class="list-group">
                                                    </div>
                                                </div>
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
            <jsp:include page="include/global/aiBottomBar.html"/>
        </main>
    </body>
</html>
