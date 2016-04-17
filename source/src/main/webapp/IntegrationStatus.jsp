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
        <script type="text/javascript" src="js/pages/IntegrationStatus.js"></script>
        <title id="pageTitle">Integration Status</title>
    </head>
    <body>
        <%@ include file="include/header.html" %>
        <div class="container-fluid center" id="page-layout">
            <%@ include file="include/messagesArea.html"%>
            <h1 class="page-title-line" id="title">Integration Status</h1>
            <div class="row">
                <div class="col-lg-9" id="FiltersPanel">
                    <div class="panel panel-default">
                        <div class="panel-heading card" data-toggle="collapse" data-target="#DeployHisto">
                            <span class="glyphicon glyphicon-list"></span>
                            <label id="reportChanges">Last deploy Operations</label>
                            <span class="toggle glyphicon glyphicon-chevron-right pull-right"></span>
                        </div>
                        <div class="panel-body collapse in" id="DeployHisto">
                            <div class="row">
                                <div class="col-lg-12" id="filterContainer">
                                    <div class="row" id="tagFilter">
                                        <div class="col-xs-3">
                                            <label for="selectEngGp" id="selectEngGpLabel">Environment Group :</label>
                                            <select class="form-control col-lg-7" name="selectEngGp" id="selectEngGp"></select>
                                        </div>
                                        <div class="col-xs-3">
                                            <label for="selectSince" id="selectSinceLabel">Since (days) :</label>
                                            <select class="form-control col-lg-7" name="selectSince" id="selectSince"></select>
                                        </div>
                                        <div class="col-xs-3">
                                            <button type="button" class="btn btn-default" style="margin-left: 10px;" id="loadLastModifbutton">Load</button>
                                        </div>
                                    </div>
                                    <div class="row" id="tagFilter">
                                        <table class="table table-bordered table-hover nomarginbottom" id="histoTable">
                                            <thead id="histoTableHead">
                                            </thead>
                                            <tbody id="histoTableBody">
                                            </tbody>
                                        </table>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="col-lg-3" id="ReportByStatusPanel">
                    <div class="panel panel-default">
                        <div class="panel-heading card" data-toggle="collapse" data-target="#EnvStatus">
                            <span class="glyphicon glyphicon-list"></span>
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
