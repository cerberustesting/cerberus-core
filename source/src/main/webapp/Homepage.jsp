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
<html class="h-full">
<head>
    <meta content="text/html; charset=UTF-8" http-equiv="content-type">
    <title>Cerberus Homepage</title>
    <%@ include file="include/global/dependenciesInclusions.html" %>
    <script type="text/javascript" src="dependencies/Moment-2.30.1/moment-with-locales.min.js"></script>
    <script type="text/javascript" src="dependencies/Chart.js-2.9.3/Chart.min.js"></script>
    <script type="text/javascript" src="js/pages/Homepage.js"></script>
</head>
<body x-data x-cloak class="crb_body">
<jsp:include page="include/global/header2.html"/>
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
<main class="crb_main" :class="$store.sidebar.expanded ? 'crb_main_sidebar-expanded' : 'crb_main_sidebar-collapsed'">
    <div>
        <%@ include file="include/global/messagesArea.html" %>
        <div class="grid grid-cols-1 md:grid-cols-5 gap-6">
            <div class="crb_card" id="sc1">
                    <span class="flex items-center font-medium text-lg mb-2">
                        <i class="fa fa-windows mr-2 text-blue-600"></i>
                        <span> Applications</span>
                    </span>
                <a href="./ApplicationList.jsp">
                    <p id="hp_ApplicationNumber" class="text-3xl font-bold text-blue-600 mt-0">–</p>
                </a>
                <p class="text-xs font-medium mt-0">configured applications</p>
            </div>
            <div class="crb_card" id="sc2">
                    <span class="flex items-center font-medium text-lg mb-2">
                        <i class="fa fa-list mr-2 text-blue-600"></i>
                        <span> Test Cases</span>
                    </span>
                <a href="./TestCaseList.jsp">
                    <p id="hp_TestcaseNumber" class="text-3xl font-bold text-blue-600 mt-0">–</p>
                </a>
                <p class="text-xs font-medium mt-0">created testcases</p>
            </div>
            <div class="crb_card" id="sc3">
                    <span class="flex items-center font-medium text-lg mb-2">
                        <i class="fa fa-plug mr-2 text-blue-600"></i>
                        <span> Services</span>
                    </span>
                <a href="./AppServiceList.jsp">
                    <p id="hp_ServiceNumber" class="text-3xl font-bold text-blue-600 mt-0">–</p>
                </a>
                <p class="text-xs font-medium mt-0">configured services</p>
            </div>
            <div class="col-span-2 crb_card" id="sc4">
                <div id="hp_TestExecutionNumberParent">
                        <span class="flex items-center font-medium text-lg mb-2">
                        <i class="fa fa-youtube-play mr-2 text-blue-600"></i>
                        <span> Test Execution</span>
                    </span>
                </div>
                <div class="grid grid-cols-2">
                    <div>
                        <a href="./RunTests.jsp">
                            <p id="hp_TestExecutionNumber" class="text-3xl font-bold text-blue-600 mt-0">–</p>
                        </a>
                        <p class="text-xs font-medium mt-0">launched tests</p>
                    </div>
                    <div id="exeRunningPanel" style="margin-top: 5px; padding-top: 10px; color: var(--crb-black-color); display: none">
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
        <div class="row">
            <div class="col-lg-12" id="LastTagExecPanel">
                <div class="crb_card">
                    <div class="flex justify-between items-center" data-target="#tagExecStatus">
                    <span class="flex items-center font-medium text-lg">
                        <i class="fa fa-tag fa-fw mr-2 text-blue-600"></i>
                        <span>Last tag executions</span>
                    </span>
                        <!-- Bouton configuration -->
                        <div class="btn-group">
                            <button id="configTags"
                                    class="btn btn-default btn-xs"
                                    onclick="stopPropagation(event); toggleConfigPanel();">
                                <i class="fa fa-cog"></i> <span>Config</span>
                            </button>
                        </div>

                    </div>

                    <!-- Zone configuration (hidden par défaut) -->
                    <div id="tagConfigPanel" class="hidden mt-4 p-4 bg-gray-50 dark:bg-gray-900 rounded-lg border border-gray-200">
                        <div class="grid grid-cols-2 gap-4">

                            <!-- Max campagnes -->
                            <div>
                                <label class="block text-sm font-medium mb-1">Nombre de campagnes</label>
                                <input id="conf_maxCampaign" type="number" min="1" max="20"
                                       class="w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm" />
                            </div>

                            <!-- Max exécutions -->
                            <div>
                                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Exécutions par campagne</label>
                                <input id="conf_maxPerCampaign" type="number" min="1" max="20"
                                       class="w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm" />
                            </div>

                            <!-- Taille du resultset -->
                            <div>
                                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Taille du resultset</label>
                                <input id="conf_resultSetSize" type="number" min="10" max="5000"
                                       class="w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm" />
                            </div>

                            <!-- Affichage noCampaign -->
                            <div class="flex items-center gap-2 mt-6">
                                <input id="conf_displayNoCampaign" type="checkbox"
                                       class="rounded border-gray-300 text-blue-600 focus:ring-blue-500" />
                                <label for="conf_displayNoCampaign" class="text-sm text-gray-700 dark:text-gray-300">Afficher "no campaign"</label>
                            </div>

                            <!-- Affichage nextCampaign -->
                            <div class="flex items-center gap-2 mt-6">
                                <input id="conf_displayNextCampaign" type="checkbox"
                                       class="rounded border-gray-300 text-blue-600 focus:ring-blue-500" />
                                <label for="conf_displayNextCampaign" class="text-sm text-gray-700 dark:text-gray-300">Afficher "next campaign"</label>
                            </div>

                            <!-- Liste des tags -->
                            <div class="col-span-2">
                                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Filtre des tags (séparés par ,)</label>
                                <input id="conf_tagFilterList" type="text"
                                       class="w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm" />
                            </div>
                        </div>

                        <!-- Boutons -->
                        <div class="flex justify-end gap-2 mt-4">
                            <button onclick="saveConfigPanel()" class="px-4 py-2 bg-blue-600 text-white text-sm rounded-md hover:bg-blue-700">
                                Sauvegarder
                            </button>
                            <button onclick="toggleConfigPanel()" class="px-4 py-2 bg-gray-200 text-gray-700 text-sm rounded-md hover:bg-gray-300">
                                Fermer
                            </button>
                        </div>
                    </div>

                    <div id="tagExecStatus" class="mt-4"></div>
                </div>
            </div>
        </div>


        <!-- Zone d'affichage -->
        <div id="tagExecStatus"></div>
    </div>
    </div>
    </div>
    <div class="row">
        <div class="col-lg-6" id="LastTagExecPanel">
            <div id="panelHistory" class="crb_card p-4" style="display: block;">
                <div class="flex justify-between items-center mb-4">
            <span class="flex items-center font-medium text-lg">
                <i class="fa fa-bar-chart fa-fw mr-2 text-blue-600"></i>
                <span>Execution History</span>
            </span>

                    <!-- Bouton config avec Alpine -->
                    <div x-data="{ open: false, period: localStorage.getItem('execHistoryPeriod') || '1m' }" class="relative">
                        <button @click="open = !open" class="btn btn-default btn-xs flex items-center gap-1">
                            <i class="fa fa-cog"></i> <span>Config</span>
                        </button>

                        <div x-show="open"
                             x-transition.opacity
                             @click.outside="open = false"
                             class="absolute right-0 mt-2 w-48 bg-white border rounded shadow-lg p-3 z-50">
                            <select x-model="period" class="form-select form-select-sm mb-2 w-full">
                                <option value="1w">Last week</option>
                                <option value="2w">Last 2 weeks</option>
                                <option value="1m">Last month</option>
                                <option value="2m">Last 2 months</option>
                                <option value="3m">Last 3 months</option>
                            </select>
                            <button @click="
                        localStorage.setItem('execHistoryPeriod', period);
                        open = false;
                        loadExecutionsHistoBar();
                    " class="btn btn-sm btn-primary w-full">Save</button>
                        </div>
                    </div>
                </div>

                <div id="histoChart1">
                    <canvas id="canvasHistExePerStatus"></canvas>
                </div>
            </div>
        </div>

        <div class="col-lg-6" id="TcStatPanel">
            <div id="panelTcHistory" class="crb_card p-4" style="display: block;">
                <div class="flex justify-between items-center mb-4" data-target="#histoChart2">
        <span class="flex items-center font-medium text-lg">
            <i class="fa fa-bar-chart fa-fw mr-2 text-blue-600"></i>
            <span>Testcase History Status</span>
        </span>

                    <!-- Bouton config Alpine -->
                    <div x-data="{ open: false, period: localStorage.getItem('tcHistoryPeriod') || '1m' }" class="relative">
                        <button @click="open = !open" class="btn btn-default btn-xs flex items-center gap-1">
                            <i class="fa fa-cog"></i> <span>Config</span>
                        </button>

                        <div x-show="open"
                             x-transition.opacity
                             @click.outside="open = false"
                             class="absolute right-0 mt-2 w-48 bg-white border rounded shadow-lg p-3 z-50">
                            <select x-model="period" class="form-select form-select-sm mb-2 w-full">
                                <option value="1w">Last week</option>
                                <option value="2w">Last 2 weeks</option>
                                <option value="1m">Last month</option>
                                <option value="2m">Last 2 months</option>
                                <option value="3m">Last 3 months</option>
                            </select>
                            <button @click="
                    localStorage.setItem('tcHistoryPeriod', period);
                    open = false;
                    loadTestcaseHistoGraph();
                " class="btn btn-sm btn-primary w-full">Save</button>
                        </div>
                    </div>
                </div>

                <div id="histoChart2">
                    <canvas id="canvasHistTcPerStatus"></canvas>
                </div>
            </div>

        </div>

    </div>

    <div id="homeTableDiv" class="crb_card">
        <div class="" data-target="#applicationPanel">
                <span class="flex items-center font-medium text-lg mb-2">
                        <i class="fa fa-retweet fa-fw mr-2 text-blue-600"></i>
                        <span> Test Case Status by Application</span>
                </span>
        </div>
        <div class="" id="applicationPanel">
            <table id="homePageTable" class="table table-hover display" name="homePageTable"></table>
            <div class="marginBottom20"></div>
        </div>
    </div>

    <div class="row hidden">
        <div class="col-lg-6">
            <div id="ReportByStatusPanel">
                <div class="crb_card">
                    <div class="" data-target="#EnvStatus">
                        <span class="fa fa-pie-chart fa-fw"></span>
                        <label id="reportStatus">Environment Status</label>
                    </div>
                    <div class="" id="EnvStatus">
                        <div id="homePageTable1_wrapper" class="dataTables_scroll" style="position: relative">
                            <div class="row">
                                <div class="col-xs-12" id="EnvByBuildRevisionTable">
                                    <table class="table dataTable table-hover nomarginbottom" id="envTable">
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
        <div class="col-lg-6">
            <div id="ChangelogPanel">
                <div class="crb_card">
                    <div class="" data-target="#Changelog42000">
                        <span class="fa fa-pie-chart fa-fw"></span>
                        <label id="changelogLabel">Changelog</label>
                    </div>
                    <div class="" id="Changelog42000">
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
</main>
</body>
</html>
