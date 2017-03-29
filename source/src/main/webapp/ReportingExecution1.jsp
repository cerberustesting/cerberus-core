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
<%-- 
  Document   : ReportingExecution
  Created on : Nov., 21th,  2105
  Author     : cerberus
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%@ include file="include/dependenciesInclusions.html" %>
        <link rel="stylesheet" href="css/pages/ReportingExecutionByTag.css" type="text/css"/>
        <script type="text/javascript" src="js/pages/ReportingExecution_temp.js"></script>
        <title id="pageTitle">Execution Reporting: Status</title>
    </head>
    <body>
        <%@ include file="include/header.html" %>
        <div class="container-fluid center" id="page-layout">
            <div class="alert alert-warning"><strong>BETA</strong> This page is in beta, some features may not be available or fully functional </div>
            <%@ include file="include/messagesArea.html"%>
            <h1 class="page-title-line" id="title">Execution Reporting: Status</h1>
            <div class="row">
                <form id="executionReportingForm" role="form">
                    <div class="col-xs-12" id="FiltersPanel">
                        <div class="panel panel-default">
                            <div class="panel-heading card" data-toggle="collapse" data-target="#filtersArea">
                                <span class="glyphicon glyphicon-filter"></span>
                                <label id="filtersLbl">Filters</label>
                                <span class="toggle glyphicon glyphicon-chevron-right pull-right"></span>
                            </div>
                            <div class="panel-body collapse in" id="filtersArea">
                                <div class="row marginBottom20">
                                    <div class="col-xs-12">
                                        <!--<div class="col-xs-12">-->
                                        <h3 id="testCaseFilters_lbl">Testcase Filters (Displayed Rows)</h3>
                                    </div>    
                                    <div class="col-xs-12 marginBottom20">
                                        <!--<div class="col-xs-3">
                                            <label>System</label>
                                            <div id="systemFilters">  
                                                <select class="multiselectelement form-control" multiple="multiple" id="system">
                                                </select>
                                            </div>    
                                        </div>-->
                                        <div class="col-xs-3">
                                            <label><span id="test_lbl">Test</span></label>
                                            <div id="testFilters">  
                                                <select class="multiselectelement form-control" multiple="multiple" id="test">
                                                </select>
                                            </div>    
                                        </div>
                                        <div class="col-xs-3">
                                            <label><span id="application_lbl">Application</span></label>
                                            <div id="applicationFilters">  
                                                <select class="multiselectelement form-control" multiple="multiple" id="application">
                                                </select>
                                            </div>    
                                        </div>
                                        <div class="col-xs-3">
                                            <label><span id="project_lbl">Project</span> <span class="glyphicon glyphicon-star favorite"></span></label>
                                            <div id="projectFilters">  
                                                <select class="multiselectelement form-control" multiple="multiple" id="project">
                                                </select>
                                            </div>    
                                        </div>                                   
                                    </div>

                                    <div class="col-xs-12 filterReportingExecution">
                                        <div>
                                            <label><span id="tcstatus_lbl">TC Status</span> <span class="glyphicon glyphicon-star favorite"></span></label>
                                            <div id="tcStatusFilters">                                                
                                                <!--<label class="checkbox-inline"><input name="tcstatusAll" id="tcstatusAll" type="checkbox"/>Check/Un-check all</label>-->
                                            </div>
                                        </div>
                                    </div>
                                    <div class="col-xs-12 filterReportingExecution">
                                        <label><span id="group_lbl">Group</span> <span class="glyphicon glyphicon-star favorite"></span></label>
                                        <div id="groupFilters">                                                
                                            <!--<label class="checkbox-inline"><input name="groupAll" id="groupAll" type="checkbox"/>Check/Un-check all</label>-->
                                        </div>
                                    </div>
                                    <div class="col-xs-12 filterReportingExecution">
                                        <div>
                                            <label><span id="tcactive_lbl">Active</span> <span class="glyphicon glyphicon-star favorite"></span></label>
                                            <div id="tcactiveFilters">                                                
                                                <!--<label class="checkbox-inline"><input name="tcactiveAll" id="tcactiveAll" type="checkbox"/>Check/Un-check all</label>-->
                                            </div>
                                        </div>
                                    </div>
                                    <div class="col-xs-12 filterReportingExecution">
                                        <div>
                                            <label><span id="priority_lbl">Priority</span> <span class="glyphicon glyphicon-star favorite"></span></label>
                                            <div id="priorityFilters">                                                
                                                <!--<label class="checkbox-inline"><input name="priorityAll" id="priorityAll" type="checkbox"/>Check/Un-check all</label>-->
                                            </div>
                                        </div>
                                    </div>


                                    <!--</div>-->
                                </div>
                                <div class="row marginBottom20">
                                    <div class="col-xs-12">                                    
                                        <div class="col-xs-3">
                                            <label><span id="targetsprint_lbl">Target Sprint</span></label>
                                            <div id="targetsprintFilters">                                                
                                                <select class="multiselectelement form-control" multiple="multiple" id="targetsprint">
                                                </select>
                                            </div>
                                        </div>
                                        <div class="col-xs-3">
                                            <label><span id="targetrevision_lbl">Target Revision</span></label>
                                            <div id="targetrevisionFilters">                                                
                                                <select class="multiselectelement form-control" multiple="multiple" id="targetrevision">
                                                </select>
                                            </div>
                                        </div>

                                        <div class="col-xs-3">
                                            <label><span id="creator_lbl">Creator</span></label>
                                            <div id="creatorFilters">                                                
                                                <select class="multiselectelement form-control" multiple="multiple" id="creator">
                                                </select>
                                            </div>
                                        </div>
                                        <div class="col-xs-3">                                    
                                            <label><span id="implementer_lbl">Implementer</span></label>
                                            <div id="implementerFilters">                                                
                                                <select class="multiselectelement form-control" multiple="multiple" id="implementer">
                                                </select> 
                                            </div>
                                        </div>


                                    </div>
                                </div>
                                <div class="row marginBottom20">
                                    <div class="col-xs-12">                                    
                                        <div class="col-xs-3">
                                            <label><span id="ticket_lbl">Ticket</span></label>
                                            <div id="ticket">                                                
                                                <input  id="ticket" name="ticket" class="form-control"/>
                                            </div>
                                        </div>
                                        <div class="col-xs-3">
                                            <label><span id="bugid_lbl">Bug ID</span></label>
                                            <div id="ticket">                                                
                                                <input  id="bugid" name="bugid" class="form-control"/>
                                            </div>
                                        </div>

                                    </div>
                                </div>
                                <div class="row marginBottom20">
                                    <div class="col-xs-12">
                                        <h3 id="executionFilters_lbl">Execution Context Filters (Displayed Columns)</h3>
                                    </div>    
                                    <div class="col-xs-12 filterReportingExecution">
                                        <label><span id="country_lbl">Country</span><span class="glyphicon glyphicon-star favorite"></span></label>

                                        <div id="countryFilters">                                                
                                            <!--<label class="checkbox-inline"><input name="countryAll" id="browserAll" type="checkbox"/>Check/Un-check all</label>-->
                                        </div>
                                    </div>                                        
                                    <div class="col-xs-12 filterReportingExecution">
                                        <label><span id="browser_lbl">Browser</span><span class="glyphicon glyphicon-star favorite"></span></label>
                                        <div id="browserFilters">                                                
                                            <!--<label class="checkbox-inline"><input name="browserAll" id="browserAll" type="checkbox"/>Check/Un-check all</label>-->
                                        </div>
                                    </div>                                    
                                </div>   
                                <div class="row">
                                    <div class="col-xs-12">
                                        <h3 id="testCaseExecutionFilters_lbl">Testcase Execution Filters (Displayed Content)</h3>
                                    </div>
                                    <div class="col-xs-12">
                                        <div class="col-xs-3">
                                            <label><span id="environment_lbl">Environment</span> <span class="glyphicon glyphicon-star favorite"></span></label>
                                            <div id="environmentFilters">                                                
                                                <select class="multiselectelement form-control" multiple="multiple" id="environment">
                                                </select> 
                                            </div>
                                        </div>
                                        <div class="col-xs-3">
                                            <label><span id="build_lbl">Build</span></label>
                                            <div id="buildFilters">                                                
                                                <select class="multiselectelement form-control" multiple="multiple" id="build">
                                                </select> 
                                            </div>
                                        </div>
                                        <div class="col-xs-3">
                                            <label><span id="revision_lbl">Revision</span></label>
                                            <div id="revisionFilters">                                                
                                                <select class="multiselectelement form-control" multiple="multiple" id="revision">
                                                </select> 
                                            </div>
                                        </div>
                                        <div class="col-xs-3">
                                            <label><span id="comment_lbl">Comment</span> <span class="glyphicon glyphicon-star favorite"></span></label>
                                            <div id="commentFilters">                                                
                                                <input  id="comment" name="comment" class="form-control"/>
                                            </div>
                                        </div>
                                    </div>
                                </div>        
                                <div class="row marginBottom20">
                                    <div class="col-xs-12">
                                        <div class="col-xs-3">
                                            <label><span id="ip_lbl">IP</span> <span class="glyphicon glyphicon-star favorite"></span></label>
                                            <div id="ipFilters">                                                
                                                <input  id="ip" name="ip" class="form-control" maxlenght="150"/>
                                            </div>
                                        </div>
                                        <div class="col-xs-3">
                                            <label><span id="port_lbl">Port</span> <span class="glyphicon glyphicon-star favorite"></span></label>
                                            <div id="portFilters">                                                
                                                <input  id="port" name="port" class="form-control" maxlenght="45"/>
                                            </div>
                                        </div>

                                        <div class="col-xs-3">
                                            <label><span id="tag_lbl">Tag</span> <span class="glyphicon glyphicon-star favorite"></span></label>
                                            <div id="tagFilters">                                                
                                                <input  id="tag" name="tag" class="form-control" maxlenght="50"/>
                                            </div>
                                        </div>
                                        <div class="col-xs-3">                                    
                                            <label><span id="browserversion_lbl">Browser version</span> <span class="glyphicon glyphicon-star favorite"></span></label>
                                            <div id="browserversionFilters">                                                
                                                <input  id="browserversion" name="browserversion" class="form-control" maxlenght="200"/>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="col-xs-12 filterReportingExecution">
                                        <label><span id="tcestatus_lbl">Execution Status</span><span class="glyphicon glyphicon-star favorite"></span></label>
                                        <div id="tcestatusFilters">                                                
                                            <!--<label class="checkbox-inline"><input name="browserAll" id="browserAll" type="checkbox"/>Check/Un-check all</label>-->
                                        </div>
                                    </div>   

                                </div>
                                <div class="marginTop20 pull-left">
                                    <button type="button" class="btn btn-primary" id="searchExecutionsButton">Search</button>
                                    <button type="button" class="btn btn-default" id="resetButton">Reset</button>
                                </div>                                
                                <div class="marginTop20 pull-right">                                   
                                    <button type="button" class="btn btn-default" id="selectFiltersButton">Apply my default filters</button>
                                    <button type="button" class="btn btn-default" id="setFiltersButton">Set as my default filter</button>
                                </div>
                            </div>
                        </div>

                    </div>          
                </form>    
            </div>


            <div class="row">
                <div class="col-xs-12" id="ListPanel">

                    <div class="panel panel-default">
                        <div class="panel-heading card">
                            <span class="glyphicon glyphicon-list"></span>
                            <label id="resultList">List</label>
                        </div>
                        <button type="button" class="btn btn-default pull-right marginRight5 marginTop5" id="getURLButton" disabled="disabled">Get URL for quick access</button>
                        <div class="panel-body marginTop25" id="listReport">                            
                            <div id="tabsContainer" class="center ">
                                <ul id="tabs" class="nav nav-tabs" data-tabs="tabs">
                                    <li class="active"><a data-toggle="tab" id="tab1Text" href="#tabs-1">Table</a></li>
                                    <li><a data-toggle="tab" href="#tabs-2" id="tab2Text">Totals per Execution Status </a></li>
                                    <li><a data-toggle="tab" href="#tabs-3" id="tab3Text">Totals per TC Status</a></li>
                                    <li><a data-toggle="tab" href="#tabs-4" id="tab4Text">Totals per Group</a></li>
                                </ul>
                                <div class="tab-content">   
                                    <div class="center marginTop25 tab-pane fade in active" id="tabs-1">
                                        <div id="tableArea">


                                            <table id="executionTable" class="table table-hover display invisible" 
                                                   name="executionTable">
                                                <thead id="executionTableHeader" name="executionTableHeader">
                                                    <tr>
                                                        <th>Test</th>
                                                        <th>TestCase</th>
                                                        <th>Application</th>
                                                        <th>Bug ID</th>
                                                        <th>Ticket</th>
                                                        <th>Group</th>
                                                        <th>Priority</th>
                                                        <th id="headerStatus">Status</th>
                                                    </tr>
                                                </thead>
                                            </table>
                                            <div id="afterTableDiv" class="marginBottom20"></div>
                                        </div>
                                    </div>
                                    <div class="center tab-pane fade marginTop25" id="tabs-2">
                                        <div id="statisticsPerControlStatusArea" >
                                            <table id="statisticsPerControlStatus" class="table table-hover display">
                                                <thead id="statisticsPerControlStatusHeader"></thead>
                                                <tfoot id="statisticsPerControlStatusFoot"></tfoot>
                                            </table>
                                        </div>
                                    </div>                                
                                    <div class="center tab-pane fade marginTop25" id="tabs-3">
                                        <div id="statisticsPerTCStatusArea" >
                                            <table id="statisticsPerTCStatus" class="table table-hover display">
                                                <thead id="statisticsPerTCStatusHeader"></thead>
                                                <tfoot id="statisticsPerTCStatusFoot"></tfoot>
                                            </table>
                                        </div>
                                    </div>      
                                    <div class="center tab-pane fade marginTop25" id="tabs-4">
                                        <div id="statisticsPerTCGroupArea" >
                                            <table id="statisticsPerTCGroup" name="statisticsPerTCGroup"  class="table table-hover display">                                            
                                                <thead id="statisticsPerTCGroupHeader"></thead>
                                                <tfoot id="statisticsPerTCGroupFoot"></tfoot>
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
