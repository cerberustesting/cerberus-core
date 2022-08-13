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
    <script type="text/javascript" src="dependencies/Moment-2.24.0/moment.min.js"></script>
    <script type="text/javascript" src="dependencies/Moment-2.24.0/locale/fr.js"></script>
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

    <div class="row">
        <div class="col-lg-6" id="LastTagExecPanel">
            <div class="panel panel-default">
                <div class="panel-heading card clearfix" data-toggle="collapse" data-target="#tagExecStatus">
                    <span class="toggle glyphicon glyphicon-chevron-right pull-right"></span>
                    <div class="btn-group pull-right">
                        <button id="refreshTags" class="btn btn-default btn-xs marginRight10"
                                onclick="stopPropagation(event); loadTagExec();"><span
                                class="glyphicon glyphicon-refresh"></span> <label id="refresh">Refresh</label></button>
                        <button id="tagSettings" class="btn btn-default btn-xs"><span
                                class="glyphicon glyphicon-cog"></span> <label id="tagSettingsLabel">Settings</label>
                        </button>
                    </div>
                    <span class="fa fa-tag fa-fw"></span>
                    <label id="lastTagExec">Last tag executions</label>
                </div>
                <div class="panel-body collapse in" id="tagExecStatus">

                </div>
            </div>
        </div>
        <div class="col-lg-6 hidden-sm hidden-xs" id="MainActionsPanel">
            <div class="panel panel-default" style="padding: 30px;">

                <div class="row">
                    <div class="row" id="tuto-line">
                        <div class="col-sm-6 hidden-sm hidden-xs marginTop10 marginBottom10 text-center"
                             data-dismiss="modal" id="shortcut1">
                            <a href="./TestCaseList.jsp" class="btn btn-primary">
                                <div class="card" style="width: 18rem;">
                                    <span class="card-img-top glyphicon glyphicon-pencil marginBottom20 marginTop20"
                                          style="font-size:50px;"></span>
                                    <div class="card-body"><p class="card-text">Create/Modify a Test</p>
                                    </div>
                                </div>
                            </a>
                        </div>
                        <div class="col-sm-6 hidden-sm hidden-xs marginTop10 marginBottom10 text-center"
                             data-dismiss="modal" id="shortcut2">
                            <a href="./RunTests.jsp" class="btn btn-primary">
                                <div class="card" style="width: 18rem;">
                                    <span class="card-img-top glyphicon glyphicon-cog marginBottom20 marginTop20"
                                          style="font-size:50px;"></span>
                                    <div class="card-body"><p class="card-text">Run a Test or a Campaign</p></div>
                                </div>
                            </a>
                        </div>
                    </div>
                    <div class="row">
                        <div class="col-sm-3 hidden-sm hidden-xs marginTop10 marginBottom10 text-center"
                             data-dismiss="modal" id="shortcut3">
                            <a href="Application.jsp" class="btn btn-primary">
                                <div class="card" style="width: 12rem;">
                                    <span class="card-img-top glyphicon glyphicon-wrench  marginBottom20 marginTop20"
                                          style="font-size:50px;"></span>
                                    <div class="card-body"><p class="card-text">Create/Modify <br>an Application</p>
                                    </div>
                                </div>
                            </a>
                        </div>
                        <div class="col-sm-3 hidden-sm hidden-xs marginTop10 marginBottom10 text-center"
                             data-dismiss="modal" id="shortcut4">
                            <a href="AppServiceList.jsp" class="btn btn-primary">
                                <div class="card" style="width: 12rem;">
                                    <span class="card-img-top glyphicon glyphicon-wrench  marginBottom20 marginTop20"
                                          style="font-size:50px;"></span>
                                    <div class="card-body"><p class="card-text">Create/Modify <br>a Service</p></div>
                                </div>
                            </a>
                        </div>
                        <div class="col-sm-3 hidden-sm hidden-xs marginTop10 marginBottom10 text-center"
                             data-dismiss="modal" id="shortcut5">
                            <a href="TestDataLibList.jsp" class="btn btn-primary">
                                <div class="card" style="width: 12rem;">
                                    <span class="card-img-top glyphicon glyphicon-wrench  marginBottom20 marginTop20"
                                          style="font-size:50px;"></span>
                                    <div class="card-body"><p class="card-text">Create/Modify <br>a Data Library</p>
                                    </div>
                                </div>
                            </a>
                        </div>
                        <div class="col-sm-3 hidden-sm hidden-xs marginTop10 marginBottom10 text-center"
                             data-dismiss="modal" id="shortcut6">
                            <a href="./documentation/D1/documentation_en.html" target="_blank" class="btn btn-info">
                                <div class="card" style="width: 12rem;">
                                    <span class="card-img-top glyphicon glyphicon-question-sign  marginBottom20 marginTop20"
                                          style="font-size:50px;"></span>
                                    <div class="card-body"><p class="card-text">&nbsp;<br>Documentation</p></div>
                                </div>
                            </a>
                        </div>
                    </div>
                </div>

            </div>
        </div>
    </div>
    <div class="row">
        <div class="col-lg-6" id="LastTagExecPanel">
            <div id="panelHistory" class="panel panel-default" style="display: block;">
                <div class="panel-heading card" data-toggle="collapse" data-target="#histoChart1">
                    <span class="toggle glyphicon glyphicon-chevron-right pull-right"></span>
                    <div class="btn-group pull-right">
                        <button id="refreshTags" class="btn btn-default btn-xs marginRight10"
                                onclick="stopPropagation(event); loadTagHistoBar();"><span
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
        <div class="col-lg-6" id="TcStatPanel">
            <div id="panelTcHistory" class="panel panel-default" style="display: block;">
                <div class="panel-heading card" data-toggle="collapse" data-target="#histoChart2">
                    <span class="toggle glyphicon glyphicon-chevron-right pull-right"></span>
                    <div class="btn-group pull-right">
                        <button id="refreshTcs" class="btn btn-default btn-xs marginRight10"
                                onclick="stopPropagation(event); loadTcHistoBar();"><span
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
    </div>

    <div id="homeTableDiv" class="panel panel-default">
        <div class="panel-heading card" data-toggle="collapse" data-target="#applicationPanel">
            <span class="toggle glyphicon glyphicon-chevron-right pull-right"></span>
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
                <div class="panel panel-default">
                    <div class="panel-heading card" data-toggle="collapse" data-target="#Changelog41500">
                        <span class="fa fa-pie-chart fa-fw"></span>
                        <label id="changelogLabel">Changelog</label>
                        <span class="toggle glyphicon glyphicon-chevron-right pull-right"></span>
                    </div>
                    <div class="panel-body collapse in" id="Changelog41500">
                        <iframe id="documentationFrame" style="width:100%" frameborder="0" scrolling="yes"/>
                        </iframe>
                    </div>
                </div>
            </div>
        </div>
        <div class="col-lg-6">
            <div id="ReportByStatusPanel">
                <div class="panel panel-default">
                    <div class="panel-heading card" data-toggle="collapse" data-target="#EnvStatus">
                        <span class="fa fa-pie-chart fa-fw"></span>
                        <label id="reportStatus">Environment Status</label>
                        <span class="toggle glyphicon glyphicon-chevron-right pull-right"></span>
                    </div>
                    <div class="panel-body collapse in" id="EnvStatus">
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


    <footer class="footer">
        <div class="container-fluid" id="footer"></div>
    </footer>
</div>
</body>
</html>
