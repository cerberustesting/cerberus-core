<%--
  ~ Cerberus Copyright (C) 2013 vertigo17
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
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  ~ GNU General Public License for more details.
  ~
  ~ You should have received a copy of the GNU General Public License
  ~ along with Cerberus. If not, see <http://www.gnu.org/licenses/>.
--%>
<%@page import="java.util.Date"%>
<%@page import="org.springframework.web.context.support.WebApplicationContextUtils"%>
<%@page import="org.springframework.context.ApplicationContext"%>
<%@page import="org.springframework.web.context.WebApplicationContext"%>
<%@page import="org.cerberus.crud.entity.Invariant"%>
<%@page import="java.util.List"%>
<%@page import="org.cerberus.crud.service.IInvariantService"%>
<%@page import="org.cerberus.database.IDatabaseVersioningService"%>

<%@page contentType="text/html" pageEncoding="UTF-8"%><!DOCTYPE HTML>
<!DOCTYPE html>
<html>
    <head>
        <meta content="text/html; charset=UTF-8" http-equiv="content-type">
        <title>Cerberus Homepage</title>
        <%@ include file="include/dependenciesInclusions.html" %>
        <link rel="stylesheet" href="css/pages/Homepage.css" type="text/css"/>
        <link rel="stylesheet" href="css/pages/ReportingExecutionByTag.css" type="text/css"/>
        <script type="text/javascript" src="js/pages/Homepage.js"></script>
    </head>
    <body>
        <%@ include file="include/header.html"%>
        <%@ include file="include/homepage/tagSettingsModal.html" %>
        <%
            ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());

            IDatabaseVersioningService DatabaseVersioningService = appContext.getBean(IDatabaseVersioningService.class);
            if (!(DatabaseVersioningService.isDatabaseUptodate()) && request.isUserInRole("Administrator")) {%>
        <script>
            var r = confirm("WARNING : Database Not Uptodate >> Redirect to the DatabaseMaintenance page ?");
            if (r == true)
            {
                location.href = './DatabaseMaintenance.jsp';
            }
        </script>

        <% }
        %>
        <style>

            .DataTables_sort_wrapper { font-size:9px }

        </style>
        <div class="container-fluid center" id="page-layout">
            <%@ include file="include/messagesArea.html"%>
            <h1 class="page-title-line" id="title">Welcome to Cerberus Application</h1>
            <div id="homeTableDiv" class="panel panel-default">
                <div class="panel-heading card" data-toggle="collapse" data-target="#applicationPanel">
                    <span class="toggle glyphicon glyphicon-chevron-right pull-right"></span>
                    <span class="fa fa-retweet fa-fw"></span>
                    <label id="testCaseStatusByApp">Test Case Status by Application</label>
                </div>
                <div class="panel-body collapse in" id="applicationPanel">
                    <table id="homePageTable" class="table table-hover display" name="homePageTable"></table>
                    <div class="marginBottom20"></div>
                </div>
            </div>

            <div class="row">
                <div class="col-lg-6" id="LastTagExecPanel">
                    <div class="panel panel-default">
                        <div class="panel-heading card clearfix" data-toggle="collapse" data-target="#tagExecStatus">
                            <span class="toggle glyphicon glyphicon-chevron-right pull-right"></span>
                            <div class="btn-group pull-right">
                                <button id="tagSettings" class="btn btn-default btn-xs"><span class="glyphicon glyphicon-cog"></span> <label id="tagSettingsLabel">Settings</label></button>
                            </div>
                            <span class="fa fa-tag fa-fw"></span>
                            <label id="lastTagExec">Last tag executions</label>
                        </div>
                        <div class="panel-body collapse in" id="tagExecStatus">

                        </div>
                    </div>
                </div>
                <div class="col-lg-6" id="ReportByStatusPanel">
                    <div class="panel panel-default">
                        <div class="panel-heading card" data-toggle="collapse" data-target="#EnvStatus">
                            <span class="fa fa-pie-chart fa-fw"></span>
                            <label id="reportStatus">Environment Status</label>
                            <span class="toggle glyphicon glyphicon-chevron-right pull-right"></span>
                        </div>
                        <div class="panel-body collapse in" id="EnvStatus">
                            <div class="row">
                                <div class="col-xs-12" id="EnvByBuildRevisionTable">
                                    <table class="table table-bordered table-hover nomarginbottom" id="envTable">
                                        <thead>
                                            <tr>
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
            <footer class="footer">
                <div class="container-fluid" id="footer"></div>
            </footer>
        </div>
    </body>
</html>
