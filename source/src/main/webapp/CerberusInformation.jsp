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
        <script type="text/javascript" src="js/pages/CerberusInformation.js"></script>
        <title id="pageTitle">Cerberus Information</title>
    </head>
    <body>
        <%@ include file="include/global/header.html" %>
        <div class="container-fluid center" id="page-layout">
            <%@ include file="include/global/messagesArea.html"%>
            <%@ include file="include/utils/modal-confirmation.html"%>

            <h1 class="page-title-line" id="title">Cerberus Information</h1>


            <div class="row">
                <div class="form-group col-xs-12">
                    <button type="button" class="btn btn-default" id="btnRefresh" name="btnRefresh"  onclick="feedContent()">Refresh</button>
                </div>
            </div>

            <div id="FiltersPanel">

                <div class="panel panel-default" id="panelActivity">
                    <div class="panel-heading card clearfix" data-toggle="collapse" data-target="#cerberusActivity">
                        <span class="fa fa-tag fa-fw"></span>
                        <label id="filters" name="filtersField">Cerberus Instance Activity</label>
                        <span class="toggle glyphicon glyphicon-chevron-right pull-right"></span>
                    </div>
                    <div class="panel-body collapse in" id="cerberusActivity">
                        <div id="cerberusActivity_wrapper" class="dataTables_scroll" style="position: relative">
                            <div class="row">
                                <div class="form-group col-xs-12">
                                    <table class="table table-bordered table-hover nomarginbottom dataTable" id="exeNbTable">
                                        <thead>
                                            <tr>
                                                <th class="text-center" id="systemHeader" name="systemHeader">Number of Pending Execution on that instance</th>
                                                <th class="text-center" id="systemHeader" name="systemHeader">Instance active to run executions</th>
                                            </tr>
                                        </thead>
                                        <tbody id="exeNbTableBody">
                                        </tbody>
                                    </table>
                                </div>
                            </div>
                            <div class="row">
                                <div class="form-group col-xs-12">
                                    <table class="table table-bordered table-hover nomarginbottom dataTable" id="exeTable">
                                        <thead>
                                            <tr>
                                                <th class="text-center">ID</th>
                                                <th class="text-center">Start</th>
                                                <th class="text-center">System</th>
                                                <th class="text-center">Application</th>
                                                <th class="text-center">Test</th>
                                                <th class="text-center">TestCase</th>
                                                <th class="text-center">Environment</th>
                                                <th class="text-center">Country</th>
                                                <th class="text-center">Robot</th>
                                                <th class="text-center">Tag</th>
                                            </tr>
                                        </thead>
                                        <tbody id="exeTableBody">
                                        </tbody>
                                    </table>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="row">
                <div class="form-group col-md-6">

                    <div class="" id="FiltersPanel">
                        <div class="panel panel-default" id="panelInformation">
                            <div class="panel-heading card" data-toggle="collapse"  data-target="#cerberusInformation">
                                <span class="fa fa-tag fa-fw"></span>
                                <label id="filters" name="filtersField">Cerberus Information</label>
                                <span class="toggle glyphicon glyphicon-chevron-right pull-right"></span>
                            </div>
                            <div class="panel-body collapse in" id="cerberusInformation">
                                <div id="cerberusInformation_wrapper" class="dataTables_scroll" style="position: relative">
                                    <div class="row">
                                        <div class="form-group col-xs-12">
                                            <table class="table table-bordered table-hover nomarginbottom dataTable" id="cerberusTable">
                                                <thead>
                                                    <tr>
                                                        <th class="text-center" id="projectHeader" name="systemHeader">Project</th>
                                                        <th class="text-center" id="versionHeader" name="countryHeader">Version</th>
                                                        <th class="text-center" id="buildHeader" name="countryHeader">Build</th>
                                                        <th class="text-center" id="dtbTargetHeader" name="countryHeader">Database Target Version</th>
                                                        <th class="text-center" id="dtbCurrentHeader" name="countryHeader">Database Current Version</th>
                                                        <th class="text-center" id="environmentHeader" name="environmentHeader">Environment</th>
                                                    </tr>
                                                </thead>
                                                <tbody id="cerberusTableBody">
                                                </tbody>
                                            </table>
                                        </div>
                                    </div>
                                    <div class="row">
                                        <div class="form-group col-xs-12">
                                            <table class="table table-bordered table-hover nomarginbottom dataTable" id="cerberusAuthTable">
                                                <thead>
                                                    <tr>
                                                        <th class="text-center" id="authHeader" name="authHeader">Authentification</th>
                                                        <th class="text-center" id="isKCHeader" name="isKCHeader">is Keycloak</th>
                                                        <th class="text-center" id="kcRealmHeader" name="kcRealmHeader">Keycloak Realm</th>
                                                        <th class="text-center" id="kcClientHeader" name="kcClientHeader">Keycloak Client</th>
                                                        <th class="text-center" id="kcUrlHeader" name="kcUrlHeader">Keycloak URL</th>
                                                    </tr>
                                                </thead>
                                                <tbody id="cerberusAuthTableBody">
                                                </tbody>
                                            </table>
                                        </div>
                                    </div>
                                    <div class="row">
                                        <div class="form-group col-xs-12">
                                            <table class="table table-bordered table-hover nomarginbottom dataTable" id="cerberusSaaSTable">
                                                <thead>
                                                    <tr>
                                                        <th class="text-center" id="saaS" name="saaS">SaaS</th>
                                                        <th class="text-center" id="isSaaS" name="isSaaS">is SaaS</th>
                                                        <th class="text-center" id="saasInstance" name="saasInstance">Instance</th>
                                                        <th class="text-center" id="saasParallelrun" name="saasParallelrun">Parallel Run</th>
                                                    </tr>
                                                </thead>
                                                <tbody id="cerberusSaaSTableBody">
                                                </tbody>
                                            </table>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="form-group col-md-6">
                    <div class="" id="FiltersPanel">
                        <div class="panel panel-default" id="paneljvmInformation">
                            <div class="panel-heading card" data-toggle="collapse"  data-target="#jvmInformation">
                                <span class="fa fa-tag fa-fw"></span>
                                <label id="filters" name="filtersField">JVM Information</label>
                                <span class="toggle glyphicon glyphicon-chevron-right pull-right"></span>
                            </div>
                            <div class="panel-body collapse in" id="jvmInformation">
                                <div id="jvmInformation_wrapper" class="dataTables_scroll" style="position: relative">
                                    <div class="row">
                                        <div class="form-group col-xs-12">
                                            <table class="table table-bordered table-hover nomarginbottom dataTable" id="jvmTable">
                                                <thead>
                                                    <tr>
                                                        <th class="text-center" id="systemHeader" name="systemHeader">JAVA Version</th>
                                                    </tr>
                                                </thead>
                                                <tbody id="jvmTableBody">
                                                </tbody>
                                            </table>
                                        </div>
                                    </div>
                                    <div class="row">
                                        <div class="form-group col-xs-12">
                                            <table class="table table-bordered table-hover nomarginbottom dataTable" id="appjvmTable">
                                                <thead>
                                                    <tr>
                                                        <th class="text-center" id="appVer" name="appVer">Application Server Version</th>
                                                    </tr>
                                                </thead>
                                                <tbody id="appjvmTableBody">
                                                </tbody>
                                            </table>
                                        </div>
                                    </div>
                                    <div class="row">
                                        <div class="form-group col-xs-12">
                                            <table class="table table-bordered table-hover nomarginbottom dataTable" id="jvmMemTable">
                                                <thead>
                                                    <tr>
                                                        <th class="text-center" id="systemHeader" name="systemHeader">Total Memory (Mb)</th>
                                                        <th class="text-center" id="systemHeader" name="systemHeader">Used Memory (Mb)</th>
                                                        <th class="text-center" id="systemHeader" name="systemHeader">Free Memory (Mb)</th>
                                                        <th class="text-center" id="systemHeader" name="systemHeader">Max Memory (Mb)</th>
                                                    </tr>
                                                </thead>
                                                <tbody id="jvmMemTableBody">
                                                </tbody>
                                            </table>
                                            <div class="progress">
                                                <div id="progress-barUsed" class="progress-bar statusKO" role="progressbar" style="width: 15%" aria-valuenow="15" aria-valuemin="0" aria-valuemax="100"></div>
                                                <div id="progress-barTotal" class="progress-bar statusOK" role="progressbar" style="width: 30%" aria-valuenow="30" aria-valuemin="0" aria-valuemax="100"></div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>

                        </div>
                    </div>
                </div>
            </div>


            <div class="row">
                <div class="form-group col-md-6">

                    <div id="FiltersPanel">
                        <div class="panel panel-default" id="paneldtbInformation">
                            <div class="panel-heading card" data-toggle="collapse"  data-target="#dtbInformation">
                                <span class="fa fa-tag fa-fw"></span>
                                <label id="filters" name="filtersField">Database Information</label>
                                <span class="toggle glyphicon glyphicon-chevron-right pull-right"></span>
                            </div>
                            <div class="panel-body collapse in" id="dtbInformation">
                                <div id="jvmInformation_wrapper" class="dataTables_scroll" style="position: relative">
                                    <div class="row">
                                        <div class="form-group col-xs-12">
                                            <table class="table table-bordered table-hover nomarginbottom dataTable" id="databaseTable">
                                                <thead>
                                                    <tr>
                                                        <th class="text-center" id="systemHeader" name="systemHeader">Database</th>
                                                        <th class="text-center" id="systemHeader" name="systemHeader">Database Version</th>
                                                        <th class="text-center" id="systemHeader" name="systemHeader">Major Version</th>
                                                        <th class="text-center" id="systemHeader" name="systemHeader">Minor Version</th>
                                                    </tr>
                                                </thead>
                                                <tbody id="databaseTableBody">
                                                </tbody>
                                            </table>
                                        </div>
                                    </div>
                                    <div class="row">
                                        <div class="form-group col-xs-12">
                                            <table class="table table-bordered table-hover nomarginbottom dataTable" id="driverTable">
                                                <thead>
                                                    <tr>
                                                        <th class="text-center" id="systemHeader" name="systemHeader">Driver Name</th>
                                                        <th class="text-center" id="systemHeader" name="systemHeader">Driver Version</th>
                                                        <th class="text-center" id="systemHeader" name="systemHeader">Major Version</th>
                                                        <th class="text-center" id="systemHeader" name="systemHeader">Minor Version</th>
                                                    </tr>
                                                </thead>
                                                <tbody id="driverTableBody">
                                                </tbody>
                                            </table>
                                        </div>
                                    </div>
                                    <div class="row">
                                        <div class="form-group col-xs-12">
                                            <table class="table table-bordered table-hover nomarginbottom dataTable" id="jdbcTable">
                                                <thead>
                                                    <tr>
                                                        <th class="text-center" id="systemHeader" name="systemHeader">JDBC Minor Version</th>
                                                        <th class="text-center" id="systemHeader" name="systemHeader">JDBC Major Version</th>
                                                    </tr>
                                                </thead>
                                                <tbody id="jdbcTableBody">
                                                </tbody>
                                            </table>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                </div>


                <div class="form-group col-md-6">

                    <div id="FiltersPanel">
                        <div class="panel panel-default" id="panelschInformation">
                            <div class="panel-heading card" data-toggle="collapse"  data-target="#schInformation">
                                <span class="fa fa-tag fa-fw"></span>
                                <label id="schedulerfilters" name="filtersField">Scheduler Instance Information</label>
                                <span class="toggle glyphicon glyphicon-chevron-right pull-right"></span>
                            </div>
                            <div class="panel-body collapse in" id="schInformation">
                                <div id="schInformation_wrapper" class="dataTables_scroll" style="position: relative">
                                    <div class="row">
                                        <div class="form-group col-xs-12">
                                            <table class="table table-bordered table-hover nomarginbottom dataTable" id="schedulerTable">
                                                <thead>
                                                    <tr>
                                                        <th class="text-center" id="systemHeader" name="systemHeader">Version</th>
                                                        <th class="text-center" id="systemHeader" name="systemHeader">Reload Is Running</th>
                                                    </tr>
                                                </thead>
                                                <tbody id="schedulerTableBody">
                                                </tbody>
                                            </table>
                                        </div>
                                    </div>
                                    <div class="row">
                                        <div class="form-group col-xs-12">
                                            <table class="table table-bordered table-hover nomarginbottom dataTable" id="schedulerDateTable">
                                                <thead>
                                                    <tr>
                                                        <th class="text-center" id="systemHeader" name="systemHeader">Server Date</th>
                                                        <th class="text-center" id="systemHeader" name="systemHeader">Server Timezone</th>
                                                    </tr>
                                                </thead>
                                                <tbody id="schedulerDateTableBody">
                                                </tbody>
                                            </table>
                                        </div>
                                    </div>
                                    <div class="row">
                                        <div class="form-group col-xs-12">
                                            <table class="table table-bordered table-hover nomarginbottom dataTable" id="schDetTable">
                                                <thead>
                                                    <tr>
                                                        <th class="text-center" id="systemHeader" name="systemHeader">Type</th>
                                                        <th class="text-center" id="systemHeader" name="systemHeader">Name</th>
                                                        <th class="text-center" id="systemHeader" name="systemHeader">Next Fire Time<br>Cron definition</th>
                                                        <th class="text-center" id="systemHeader" name="systemHeader">User Created</th>
                                                    </tr>
                                                </thead>
                                                <tbody id="schDetTableBody">
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


            <footer class="footer">
                <div class="container-fluid" id="footer"></div>
            </footer>
        </div>
    </body>
</html>
