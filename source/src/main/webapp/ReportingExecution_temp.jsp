<%--
  ~ Cerberus  Copyright (C) 2013  vertigo17
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
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~ GNU General Public License for more details.
  ~
  ~ You should have received a copy of the GNU General Public License
  ~ along with Cerberus.  If not, see <http://www.gnu.org/licenses/>.
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
        <script type="text/javascript" src="js/pages/ReportingExecution_temp.js"></script>
        <title id="pageTitle">Execution Reporting: Status</title>
    </head>
    <body>
        <%@ include file="include/header.html" %>
        <div class="container-fluid center" id="page-layout">
            <%@ include file="include/messagesArea.html"%>
            <h1 class="page-title-line" id="title">Execution Reporting: Status</h1>
            <div class="row">
                <form id="executionReportingForm" role="form">
                <div class="col-xs-12" id="FiltersPanel">
                    <div class="panel panel-default">
                        <div class="panel-heading card">
                            <span class="fa fa-tag fa-fw"></span>
                            <label id="filtersLbl">Filters</label>
                        </div>
                        <div class="panel-body">
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
                                        <label>Test</label>
                                        <div id="testFilters">  
                                            <select class="multiselectelement form-control" multiple="multiple" id="test">
                                            </select>
                                        </div>    
                                    </div>
                                    <div class="col-xs-3">
                                        <label>Application</label>
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
                                        <label>Target Sprint</label>
                                        <div id="targetsprintFilters">                                                
                                            <select class="multiselectelement form-control" multiple="multiple" id="targetsprint">
                                            </select>
                                        </div>
                                    </div>
                                    <div class="col-xs-3">
                                        <label>Target Revision</label>
                                        <div id="targetrevisionFilters">                                                
                                            <select class="multiselectelement form-control" multiple="multiple" id="targetrevision">
                                            </select>
                                        </div>
                                    </div>

                                    <div class="col-xs-3">
                                        <label>Creator</label>
                                        <div id="creatorFilters">                                                
                                            <select class="multiselectelement form-control" multiple="multiple" id="creator">
                                            </select>
                                        </div>
                                    </div>
                                    <div class="col-xs-3">                                    
                                        <label>Implementer</label>
                                        <div id="implementerFilters">                                                
                                            <select class="multiselectelement form-control" multiple="multiple" id="implementer">
                                            </select> 
                                        </div>
                                    </div>


                                </div>
                            </div>
                            <div class="row">
                                <div class="col-xs-12">
                                    <h3 id="testCaseExecutionFilters_lbl">Testcase Execution Filters (Displayed Content)</h3>
                                </div>
                                <div class="col-xs-12">
                                    <div class="col-xs-3">
                                            <label>Environment</label>
                                            <div id="environmentFilters">                                                
                                                <select class="multiselectelement form-control" multiple="multiple" id="environment">
                                                </select> 
                                            </div>
                                        </div>
                                        <div class="col-xs-3">
                                            <label>Build</label>
                                            <div id="buildFilters">                                                
                                                <select class="multiselectelement form-control" multiple="multiple" id="build">
                                                </select> 
                                            </div>
                                        </div>
                                        <div class="col-xs-3">
                                            <label>Revision</label>
                                            <div id="revisionFilters">                                                
                                                <select class="multiselectelement form-control" multiple="multiple" id="revision">
                                                </select> 
                                            </div>
                                        </div>
                                        <div class="col-xs-3">
                                            <label>Comment</label>
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
                                                <input  id="ip" name="ip" class="form-control"/>
                                            </div>
                                        </div>
                                        <div class="col-xs-3">
                                            <label><span id="port_lbl">Port</span> <span class="glyphicon glyphicon-star favorite"></span></label>
                                            <div id="portFilters">                                                
                                                <input  id="port" name="port" class="form-control"/>
                                            </div>
                                        </div>

                                        <div class="col-xs-3">
                                            <label><span id="tag_lbl">Tag</span> <span class="glyphicon glyphicon-star favorite"></span></label>
                                            <div id="tagFilters">                                                
                                                <input  id="tag" name="tag" class="form-control"/>
                                            </div>
                                        </div>
                                        <div class="col-xs-3">                                    
                                            <label><span id="browserversion_lbl">Browser version</span> <span class="glyphicon glyphicon-star favorite"></span></label>
                                            <div id="browserversionFilters">                                                
                                                <input  id="browserversion" name="browserversion" class="form-control"/>
                                            </div>
                                        </div>
                                </div>
                            </div>
                             <!--<div class="separator marginBottom10"></div>-->
                            <div class="row marginBottom20">
                                <div class="col-xs-12">
                                    <h3 id="executionFilters_lbl">Execution Context Filters (Displayed Columns)</h3>
                                </div>    
                                <div class="col-xs-12 filterReportingExecution">
                                    <label><span id="country_lbl">Country</span> <span class="glyphicon glyphicon-asterisk mandatory"> <span class="glyphicon glyphicon-star favorite"></span></label>

                                    <div id="countryFilters">                                                
                                        <!--<label class="checkbox-inline"><input name="countryAll" id="browserAll" type="checkbox"/>Check/Un-check all</label>-->
                                    </div>
                                </div>                                        
                                <div class="col-xs-12 filterReportingExecution">
                                    <label><span id="browser_lbl">Browser</span> <span class="glyphicon glyphicon-asterisk mandatory"> <span class="glyphicon glyphicon-star favorite"></span></label>
                                    <div id="browserFilters">                                                
                                        <!--<label class="checkbox-inline"><input name="browserAll" id="browserAll" type="checkbox"/>Check/Un-check all</label>-->
                                    </div>
                                </div>   
                                <div class="col-xs-12 filterReportingExecution">
                                    <label><span id="tcestatus_lbl">Execution Status</span> <span class="glyphicon glyphicon-star favorite"></span></label>
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
                        <div class="panel-body" id="listReport">
                            <div id="tabsContainer" class="center container ">
                                <ul id="tabs" class="nav nav-tabs" data-tabs="tabs">
                                <li class="active"><a data-toggle="tab" id="tab1Text" href="#tabs-1">Table</a></li>
                                <li><a data-toggle="tab" href="#tabs-2" id="tab2Text">Summary </a></li>
                                </ul>
                                <div class="tab-content">   
                                    <div class="center marginTop25 tab-pane fade in active" id="tabs-1">
                                         <div id="tableArea">
                                        <table id="listTable" class="table table-hover display" name="listTable"></table>
                                        <div class="marginBottom20"></div>
                                    </div>
                                    </div>
                                    <div class="center tab-pane fade marginTop25" id="tabs-2"></div>                                
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
