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
        <link rel="stylesheet" href="css/ReportingExecutionByTag.css" type="text/css"/>
        <link rel="stylesheet" href="css/select2.css" type="text/css"/>
        <script type="text/javascript" src="js/d3.min.js"></script>
        <script type="text/javascript" src="js/pages/ReportingExecutionByTag.js"></script>
        <title id="pageTitle">Campaign Reporting</title>
    </head>
    <body>
        <%@ include file="include/header.html" %>
        <div class="container-fluid center" id="page-layout">
            <h1 class="page-title-line" id="title">Execution reporting by tag</h1>
            <div class="row">
                <div class="col-lg-6" id="FiltersPanel">
                    <div class="panel panel-default">
                        <div class="panel-heading">
                            <h3 class="panel-title" name="Filters">Filters</h3>
                        </div>
                        <div class="panel-body">
                            <div class="row">
                                <div class="col-lg-12">
                                    <label for="selectTag">Tag :</label>
                                    <div class="row" style="margin: 5px;">
                                        <div class="input-group">
                                            <select class="form-control col-lg-7" name="Tag" id="selectTag"></select>
                                            <div class="input-group-btn">
                                                <button type="button" class="btn btn-default" style="margin-left: 10px;" id="loadbutton" onclick="loadReportByStatusTable()">Load</button>
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
                        <div class="panel-heading">
                            <h3 class="panel-title" name="reportByStatus">Report by Status</h3>
                        </div>
                        <div class="panel-body">
                            <div class="row">
                                <table class="table col-xs-6" id="ReportByStatusTable" style="width: 50%;">
                                    <thead>
                                        <tr>
                                            <th>Execution status</th>
                                            <th>TestCase Number</th>
                                        </tr>
                                    </thead>
                                    <tbody></tbody>
                                </table>
                                <div class="col-xs-6" id="chart"></div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="row" id="ReportByFunctionPanel">
                <div class="col-lg-12">
                    <div class="panel panel-default">
                        <div class="panel-heading">
                            <h3 class="panel-title">Report by Function</h3>
                        </div>
                        <div class="panel-body"></div>
                    </div>
                </div>
            </div>
            <div class="row">
                <div class="col-lg-12" id="ListPanel">
                    <div class="panel panel-default">
                        <div class="panel-heading">
                            <h3 class="panel-title">List</h3>
                            <label class="checkbox-inline"><input type="checkbox" value="">Checkbox</label>
                        </div>
                        <div class="panel-body"></div>
                    </div>
                </div>
            </div>
            <footer class="footer">
                <div class="container-fluid" id="footer"></div>
            </footer>
        </footer>
    </div>
</body>
</html>
