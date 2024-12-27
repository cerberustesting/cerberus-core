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
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%@ include file="include/global/dependenciesInclusions.html" %>
        <title id="pageTitle">Campaign statistics</title>
        <script type="text/javascript" src="dependencies/Tinymce-6.7.0/tinymce.min.js"></script>
        <script type="text/javascript" src="dependencies/Moment-2.30.1/moment-with-locales.min.js"></script>
        <script type="text/javascript" src="dependencies/Bootstrap-datetimepicker-4.17.47/bootstrap-datetimepicker.min.js"></script>
        <link rel="stylesheet" href="dependencies/Bootstrap-datetimepicker-4.17.47/bootstrap-datetimepicker.min.css" />
        <script type="text/javascript" src="js/transversalobject/Campaign.js"></script>
        <script type="text/javascript" src="js/pages/ReportingCampaignStatistics.js"></script>
    </head>
    <body>
        <%@ include file="include/global/header.html" %>
        <div class="container-fluid center" id="page-layout">
            <%@ include file="include/global/messagesArea.html"%>
            <%@ include file="include/utils/modal-confirmation.html"%>
            <%@ include file="include/pages/testcampaign/viewStatcampaign.html"%>
            <h1 class="page-title-line" id="title">Campaign Statistics </h1>
            <div class="panel panel-default">
                <div class="panel-body" id="filters">
                    <div class="row" id="envCountryFilters" style="display: none;">
                        <div class='col-md-4'>
                            <div class="form-group">
                                <label for="environmentSelect" id="labelEnvironmentSelect">Environnement</label>
                                <select id="environmentSelect" class="form-control" multiple="multiple">
                                </select>
                            </div>
                        </div>
                        <div class='col-md-4'>
                            <div class="form-group">
                                <label for="countrySelect" id="labelCountrySelect">Pays</label>
                                <select id="countrySelect" class="form-control" multiple="multiple">
                                </select>
                            </div>
                        </div>
                    </div>
                    <div class="row" id="systemAppGroup1Filters">
                        <div class='col-md-4'>
                            <div class="form-group">
                                <label for="systemSelect" id="labelSystemSelect">Système</label>
                                <select id="systemSelect" class="form-control" multiple="multiple">
                                </select>
                            </div>
                        </div>
                        <div class='col-md-4'>
                            <div class="form-group">
                                <label for="applicationSelect" id="labelApplicationSelect">Application</label>
                                <select id="applicationSelect" class="form-control" multiple="multiple">
                                </select>
                            </div>
                        </div>
                        <div class='col-md-4'>
                            <div class="form-group">
                                <label for="group1Select" id="labelGroup1Select">Group 1</label>
                                <select id="group1Select" class="form-control" multiple="multiple">
                                </select>
                            </div>
                        </div>
                    </div>
                    <div class="row">
                        <div class='col-md-4'>
                            <div class="form-group">
                                <label for="fromPicker" id="labelFromPicker">From</label>
                                <div class='input-group date' id='fromPicker'>
                                    <input type='text' class="form-control" />
                                    <span class="input-group-addon">
                                    <span class="glyphicon glyphicon-calendar"></span>
                                </span>
                                </div>
                            </div>
                        </div>
                        <div class='col-md-4'>
                            <div class="form-group">
                                <label for="toPicker" id="labelToPicker">To</label>
                                <div class='input-group date' id='toPicker'>
                                    <input type='text' class="form-control" />
                                    <span class="input-group-addon">
                                    <span class="glyphicon glyphicon-calendar"></span>
                                </span>
                                </div>
                            </div>
                        </div>
                        <div class="col-md-4">
                            <div class="input-group-btn ">
                                <button type="button" class="btn btn-primary btn-block marginTop20" id="loadButton" style="border-radius: 4px;">Load</button>
                            </div>
                            <div class="input-group-btn " style="display: none;">
                                <button type="button" class="btn btn-primary btn-block marginTop20" id="loadDetailButton" style="border-radius: 4px;">Load</button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="panel panel-default" style="position: relative;">
                <div id="loading" style="display: none; text-align: center; position: absolute; left: 50%; top: 50%; transform: translate(-50%, -50%); z-index: 1000;">
                    <img src="images/loading.gif" alt="Loading...">
                </div>
                <div class="panel-body" id="tagStatisticList">
                    <table id="tagStatisticTable" class="table table-bordered table-hover display" name="tagStatisticTable"></table>
                    <div class="marginBottom20"></div>
                </div>
                <div class="panel-body" id="tagStatisticDetailList"  style="display: none;">
                    <table id="tagStatisticDetailTable" class="table table-bordered table-hover display" name="tagStatisticDetailTable"></table>
                    <div class="marginBottom20"></div>
                </div>
            </div>
            <footer class="footer">
                <div class="container-fluid" id="footer"></div>
            </footer>
        </div>
        <script>
        </script>
    </body>
</html>