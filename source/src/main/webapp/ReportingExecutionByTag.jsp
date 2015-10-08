<%-- 
    Document   : ReportingExecutionByTag2
    Created on : 3 août 2015, 11:02:49
    Author     : cerberus
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%@ include file="include/dependenciesInclusions.html" %>
        <link rel="stylesheet" href="css/pages/ReportingExecutionByTag.css" type="text/css"/>
        <script type="text/javascript" src="js/d3.min.js"></script>
        <script type="text/javascript" src="js/d3tip.js"></script>
        <script type="text/javascript" src="js/pages/ReportingExecutionByTag.js"></script>
        <title id="pageTitle">Campaign Reporting</title>
    </head>
    <body>
        <%@ include file="include/header.html" %>
        <div class="container-fluid center" id="page-layout">
            <%@ include file="include/messagesArea.html"%>
            <h1 class="page-title-line" id="title">Execution reporting by tag</h1>
            <div class="row">
                <div class="col-lg-6" id="FiltersPanel">
                    <div class="panel panel-default">
                        <div class="panel-heading card">
                            <span class="fa fa-tag fa-fw"></span>
                            <label id="filters">Filters</label>
                        </div>
                        <div class="panel-body">
                            <div class="row">
                                <div class="col-lg-12" id="filterContainer">
                                    <label for="selectTag">Tag :</label>
                                    <div class="row" id="tagFilter">
                                        <div class="input-group">
                                            <select class="form-control col-lg-7" name="Tag" id="selectTag"></select>
                                            <div class="input-group-btn">
                                                <button type="button" class="btn btn-default" style="margin-left: 10px;" id="loadbutton" onclick="loadReport()">Load</button>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="col-lg-6" id="ReportByStatusPanel">
                    <div class="panel panel-default">
                        <div class="panel-heading card" data-toggle="collapse" data-target="#ReportByStatus">
                            <span class="fa fa-pie-chart fa-fw"></span>
                            <label id="reportStatus">Report by Status</label>
                            <span class="toggle glyphicon glyphicon-chevron-right pull-right"></span>
                        </div>
                        <div class="panel-body collapse in" id="ReportByStatus">
                            <div class="row">
                                <div class="col-xs-6" id="ReportByStatusTable"></div>
                                <div class="col-xs-6" id="statusChart"></div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="row" id="ReportByFunctionPanel">
                <div class="col-lg-12">
                    <div class="panel panel-default">
                        <div class="panel-heading card" data-toggle="collapse" data-target="#functionChart">
                            <span class="fa fa-bar-chart fa-fw"></span>
                            <label id="reportFunction">Report by Function</label>
                            <span class="toggle glyphicon glyphicon-chevron-right pull-right"></span>
                        </div>
                        <div class="panel-body collapse in" id="functionChart"></div>
                    </div>
                </div>
            </div>
            <div class="row">
                <div class="col-lg-12" id="reportByEnvCountryBrowser">
                    <div class="panel panel-default">
                        <div class="panel-heading card" data-toggle="collapse" data-target="#reportEnvCountryBrowser">
                            <label id="envCountryBrowser">Report by EnvCountryBrowser</label>
                            <span class="toggle glyphicon glyphicon-chevron-right pull-right"></span>
                        </div>
                        <div class="panel-body collapse in" id="reportEnvCountryBrowser">
                            <select id="envCountryBrowserSelect" class="form-control" style="width: 15%; margin-bottom: 10px;"></select>
                            <div id="progressEnvCountryBrowser">
                                <div class="progress"></div>
                            </div>
                            <div id="statusByEnvCountryBrowser">
                                <div class="statusEnvCountryBrowser" id="EnvCountryBrowserOK"><span class='color-box statusOK'></span>OK : <span class="value">0</span></div>
                                <div class="statusEnvCountryBrowser" id="EnvCountryBrowserKO"><span class='color-box statusKO'></span>KO : <span class="value">0</span></div>
                                <div class="statusEnvCountryBrowser" id="EnvCountryBrowserFA"><span class='color-box statusFA'></span>FA : <span class="value">0</span></div>
                                <div class="statusEnvCountryBrowser" id="EnvCountryBrowserNA"><span class='color-box statusNA'></span>NA : <span class="value">0</span></div>
                                <div class="statusEnvCountryBrowser" id="EnvCountryBrowserNE"><span class='color-box statusNE'></span>NE : <span class="value">0</span></div>
                                <div class="statusEnvCountryBrowser" id="EnvCountryBrowserPE"><span class='color-box statusPE'></span>PE : <span class="value">0</span></div>
                                <div class="statusEnvCountryBrowser" id="EnvCountryBrowserCA"><span class='color-box statusCA'></span>CA : <span class="value">0</span></div>
                                <div class="statusEnvCountryBrowser pull-right">Executions : <span class="value" id="totalExec">0</span></div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="row">
                <div class="col-lg-12" id="ListPanel">
                    <div class="panel panel-default">
                        <div class="panel-heading card" data-toggle="collapse" data-target="#listReport">
                            <span class="glyphicon glyphicon-list"></span>
                            <label id="List">List</label>
                            <span class="toggle glyphicon glyphicon-chevron-right pull-right"></span>
                        </div>
                        <div class="panel-body collapse in" id="listReport">
                            <button type="button" class="btn btn-default pull-right" onclick="loadReportList()" id="reloadbutton">Reload</button>
                            <label id="countryLabel" class="bold">Country :</label>
                            <div class="form-group" id="countryFilter">

                            </div>
                            <label id="statusLabel" class="bold">Status :</label>
                            <div class="form-group marginBottom20" id="statusFilter">
                                <label class="checkbox-inline">
                                    <input type="checkbox" name="OK" checked/>
                                    OK
                                </label>
                                <label class="checkbox-inline">
                                    <input type="checkbox" name="KO" checked/>
                                    KO
                                </label>
                                <label class="checkbox-inline">
                                    <input type="checkbox" name="FA" checked/>
                                    FA
                                </label>
                                <label class="checkbox-inline">
                                    <input type="checkbox" name="NA" checked/>
                                    NA
                                </label>
                                <label class="checkbox-inline">
                                    <input type="checkbox" name="NE" checked/>
                                    NE
                                </label>
                                <label class="checkbox-inline">
                                    <input type="checkbox" name="PE" checked/>
                                    PE
                                </label>
                                <label class="checkbox-inline">
                                    <input type="checkbox" name="CA" checked/>
                                    CA
                                </label>
                            </div>
                            <div id="tableArea">
                                <table id="listTable" class="table table-hover display" name="listTable"></table>
                                <div class="marginBottom20"></div>
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
