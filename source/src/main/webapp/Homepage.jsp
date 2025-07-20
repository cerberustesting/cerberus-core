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
<%@page import="java.util.Date" %>
<%@page import="org.springframework.web.context.support.WebApplicationContextUtils" %>
<%@page import="org.springframework.context.ApplicationContext" %>
<%@page import="org.springframework.web.context.WebApplicationContext" %>
<%@page import="org.cerberus.core.crud.entity.Invariant" %>
<%@page import="org.cerberus.core.session.SessionCounter" %>
<%@page import="java.util.List" %>
<%@page import="org.cerberus.core.crud.service.IInvariantService" %>
<%@page import="org.cerberus.core.database.IDatabaseVersioningService" %>

<%@page contentType="text/html" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
    <head>
        <meta content="text/html; charset=UTF-8" http-equiv="content-type">
        <title>Cerberus Homepage</title>
        <%@ include file="include/global/dependenciesInclusions.html" %>
        <script type="text/javascript" src="dependencies/Moment-2.30.1/moment-with-locales.min.js"></script>
        <script type="text/javascript" src="dependencies/Chart.js-2.9.3/Chart.min.js"></script>
        <link rel="stylesheet" href="css/pages/Homepage.css" type="text/css"/>
        <link rel="stylesheet" href="css/pages/ReportingExecutionByTag.css" type="text/css"/>
        <script type="text/javascript" src="js/pages/Homepage.js"></script>
    </head>
    <body>
        <%@ include file="include/global/header.html" %>
        <%@ include file="include/pages/homepage/tagSettingsModal.html" %>
        <%@ include file="include/utils/modal-confirmation.html" %>

        <%
            ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());

            IDatabaseVersioningService DatabaseVersioningService = appContext.getBean(IDatabaseVersioningService.class);
            if (!(DatabaseVersioningService.isDatabaseUpToDate()) && request.isUserInRole("Administrator")) {%>
        <script>
            var r = confirm("WARNING : Database Not Uptodate >> Redirect to the DatabaseMaintenance page ?");
            if (r == true) {
                location.href = './DatabaseMaintenance.jsp';
            }
        </script>

        <% }
        %>
        <style>

            .DataTables_sort_wrapper {
                font-size: 9px
            }

        </style>
        <div class="container-fluid center" id="page-layout">
            <%@ include file="include/global/messagesArea.html" %>
            <h1 class="page-title-line" id="title">Welcome to Cerberus Application</h1>

            <div class="row marginBottom20 ">
                <div class="col-lg-2 col-md-4 col-sm-12 hidden-xs" id="sc1">
                    <div class="panel panel-default whiteCard">
                        <div class="row" style="height: 100px;">
                            <div class="col-sm-12">
                                <h5 class="marginLeft15"><span class="glyphicon glyphicon-cog"></span>  Application</h5>
                                <div class="marginLeft15 marginBottom10" id="hp_ApplicationNumber"></div>
                                <a href="./ApplicationList.jsp" class="marginLeft15">See or Create Application</a>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="col-lg-2 col-md-4 col-sm-12 hidden-xs" id="sc2">
                    <div class="panel panel-default whiteCard">
                        <div class="row" style="height: 100px;">
                            <div class="col-sm-12">
                                <h5 class="marginLeft15"><span class="glyphicon glyphicon-pencil"></span>  Test Cases</h5>
                                <div class="marginLeft15 marginBottom10" id="hp_TestcaseNumber"></div>
                                <a href="./TestCaseList.jsp" class="marginLeft15">See or Create TestCase</a>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="col-lg-2 col-md-4 col-sm-12 hidden-xs" id="sc3">
                    <div class="panel panel-default whiteCard">
                        <div class="row" style="height: 100px;">
                            <div class="col-sm-12">
                                <h5 class="marginLeft15"><span class="glyphicon glyphicon-play"></span>  Services</h5>
                                <div class="marginLeft15 marginBottom10" id="hp_ServiceNumber"></div>
                                <a href="./AppServiceList.jsp" class="marginLeft15">Edit & Call Service</a>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="col-lg-4 col-md-6 col-sm-12" id="sc4">
                    <div class="panel panel-default whiteCard">
                        <div class="row" style="height: 100px;">
                            <div class="col-sm-6 col-xs-6" id="hp_TestExecutionNumberParent">
                                <h5 class="marginLeft15"><span class="glyphicon glyphicon-play"></span>  Test Execution</h5>
                                <div class="marginLeft15 marginBottom10" id="hp_TestExecutionNumber"></div>
                                <a href="./RunTests.jsp" class="marginLeft15">Launch Test Case</a>
                            </div>
                            <div class="col-sm-5 col-xs-5 panel panelPE" id="exeRunningPanel" 
                                 style="margin-top: 5px; padding-top: 10px; background-color: lightgray; color: black; display: none">
                                <div class="row " style="height: 30px;">
                                    <div class="col-xs-3 status marginBottom10" style="">
                                        <span class="glyphicon pull-left  glyphicon-refresh spin" onclick="loadExeRunning();" title="click to refresh" style="margin-right: 5px;"></span>
                                    </div>
                                    <div class="col-xs-8 text-right " style="">
                                        <div class="total" style="" id="exeRunningPanelCnt">27
                                        </div>
                                    </div>
                                </div>
                                <div class="row" style="height: 20px;" id="queueStats">
                                    <div class='progress' style='height:12px;margin-left: 10px;margin-right: 10px'>
                                        <div id='progress-barUsed' class='progress-bar statusPE' role='progressbar' data-toggle='tooltip' data-placement='bottom' data-html='true' 
                                             data-original-title='' style='width: 0%;' aria-valuenow='0' aria-valuemin='0' aria-valuemax='100'></div>
                                        <div id='progress-barIdle' class='progress-bar statusWE' role='progressbar' data-toggle='tooltip' data-placement='bottom' data-html='true' 
                                             data-original-title='' style='width: 0%;' aria-valuenow='0' aria-valuemin='0' aria-valuemax='100'></div>
                                        <div id='progress-barQueue' class='progress-bar statusQU' role='progressbar' data-toggle='tooltip' data-placement='bottom' data-html='true' 
                                             data-original-title='' style='width: 0%;' aria-valuenow='0' aria-valuemin='0' aria-valuemax='100'></div>
                                    </div>
                                </div>
                                <div class="row" style="height: 20px;" id="exeRunningList">
                                </div>
                            </div>

                        </div>
                    </div>
                </div>
                <div class="col-lg-2 col-md-6 col-sm-12 hidden-xs" id="sc5">
                    <div class="panel panel-default whiteCard">
                        <div class="row">
                            <div class="col-sm-12" style="height: 100px;">
                                <h5 class="marginLeft15"><span class="glyphicon glyphicon-question-sign"></span>  Documentation</h5>
                                <div class="marginLeft15 marginBottom10"></div>
                                <a href="./documentation/D1/documentation_en.html" target="_blank" class="marginLeft15">See online documentation</a>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="row">
                <div class="col-lg-6" id="LastTagExecPanel">
                    <div class="panel panel-default whiteCard">
                        <div class="panel-heading card clearfix" data-target="#tagExecStatus">
                            <div class="btn-group pull-right">
                                <button id="refreshTags" class="btn btn-default btn-xs marginRight10"
                                        onclick="stopPropagation(event); loadLastTagResultList();"><span
                                        class="glyphicon glyphicon-refresh"></span> <label id="refresh">Refresh</label></button>
<!--                                <button id="tagSettings" class="btn btn-default btn-xs"><span
                                        class="glyphicon glyphicon-cog"></span> <label id="tagSettingsLabel">Settings</label>
                                </button>-->
                            </div>
                            <span class="fa fa-tag fa-fw"></span>
                            <label id="lastTagExec">Last tag executions</label>
                        </div>
                        <div class="panel-body collapse in" id="tagExecStatus">

                        </div>
                    </div>
                </div>
                <div class="col-lg-6" id="LastTagExecPanel">
                    <div id="panelHistory" class="panel panel-default whiteCard" style="display: block;">
                        <div class="panel-heading card" data-target="#histoChart1">
                            <div class="btn-group pull-right">
                                <button id="refreshTags" class="btn btn-default btn-xs marginRight10"
                                        onclick="stopPropagation(event); loadExecutionsHistoBar();"><span
                                        class="glyphicon glyphicon-refresh"></span> <label id="refresh">Refresh</label></button>
                            </div>
                            <span class="fa fa-bar-chart fa-fw"></span>
                            <label id="lblPerfParty">Execution History</label>
                        </div>
                        <div class="panel-body collapse in" id="histoChart1">
                            <canvas id="canvasHistExePerStatus" class=""></canvas>
                        </div>
                    </div>
                </div>
            </div>
            <div class="row">
                <div class="col-lg-6" id="TcStatPanel">
                    <div id="panelTcHistory" class="panel panel-default whiteCard" style="display: block;">
                        <div class="panel-heading card" data-target="#histoChart2">
                            <div class="btn-group pull-right">
                                <button id="refreshTcs" class="btn btn-default btn-xs marginRight10"
                                        onclick="stopPropagation(event); loadTestcaseHistoGraph();"><span
                                        class="glyphicon glyphicon-refresh"></span> <label id="refresh">Refresh</label></button>
                            </div>
                            <span class="fa fa-bar-chart fa-fw"></span>
                            <label id="lblPerfParty">Testcase History Status</label>
                        </div>
                        <div class="panel-body collapse in" id="histoChart2">
                            <canvas id="canvasHistTcPerStatus" class=""></canvas>
                        </div>
                    </div>
                </div>
                <div class="col-lg-6">
                    <div id="ReportByStatusPanel">
                        <div class="panel panel-default whiteCard">
                            <div class="panel-heading card" data-target="#EnvStatus">
                                <span class="fa fa-pie-chart fa-fw"></span>
                                <label id="reportStatus">Environment Status</label>
                            </div>
                            <div class="panel-body collapse in" id="EnvStatus">
                                <div id="homePageTable1_wrapper" class="dataTables_scroll" style="position: relative">
                                    <div class="row">
                                        <div class="col-xs-12" id="EnvByBuildRevisionTable">
                                            <table class="table dataTable table-bordered table-hover nomarginbottom" id="envTable">
                                                <thead>
                                                    <tr>
                                                        <th class="text-center" id="systemHeader" name="systemHeader">System</th>
                                                        <th class="text-center" id="buildHeader" name="buildHeader">Build</th>
                                                        <th class="text-center" id="revisionHeader" name="revisionHeader">Revision</th>
                                                        <th class="text-center" id="devHeader" name="devHeader">DEV</th>
                                                        <th class="text-center" id="qaHeader" name="qaHeader">QA</th>
                                                        <th class="text-center" id="uatHeader" name="uatHeader">UAT</th>
                                                        <th class="text-center" id="prodHeader" name="prodHeader">PROD</th>
                                                    </tr>
                                                </thead>
                                                <tbody id="envTableBody">
                                                </tbody>
                                            </table>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div id="homeTableDiv" class="panel panel-default whiteCard">
                <div class="panel-heading card" data-target="#applicationPanel">
                    <span class="fa fa-retweet fa-fw"></span>
                    <label id="testCaseStatusByApp">Test Case Status by Application</label>
                </div>
                <div class="panel-body collapse in" id="applicationPanel">
                    <table id="homePageTable" class="table table-bordered table-hover display" name="homePageTable"></table>
                    <div class="marginBottom20"></div>
                </div>
            </div>

            <div class="row">
                <div class="col-lg-6">
                    <div id="ChangelogPanel">
                        <div class="panel panel-default whiteCard">
                            <div class="panel-heading card" data-target="#Changelog42000">
                                <span class="fa fa-pie-chart fa-fw"></span>
                                <label id="changelogLabel">Changelog</label>
                            </div>
                            <div class="panel-body collapse in" id="Changelog42000">
                                <iframe id="documentationFrame" style="width:100%" frameborder="0" scrolling="yes"/>
                                </iframe>
                            </div>
                        </div>
                    </div>
                </div>

            </div>


            <footer class="footer">
                <div class="container-fluid" id="footer"></div>
            </footer>
        </div>
    </body>
</html>
