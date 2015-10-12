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


<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta content="text/html; charset=UTF-8" http-equiv="content-type">
        <%@ include file="include/dependenciesInclusions.html" %>
        <title id="pageTitle">Test Case</title>        
        <script type="text/javascript" src="js/pages/TestCase_new_prototype.js"></script>
    </head>
    <body>
        <%@ include file="include/header.html"%>

        <div id="page-layout" class="container-fluid center">
            <div class="col-xs-12" id="searchArea">
                <div class="row">  
                    <div class="form-group col-xs-2">
                        <label id="lbl_system" for="system">System</label>
                        <input type="hidden" id="systemAll" name="systemAll"  value="false" />
                        <!--select id="system" class="form-control" >
                        </select>-->                                            
                        <select class="multiselectelement form-control" multiple="multiple" id="system" >
                        </select>  
                    </div>       
                    <div class="form-group col-xs-4">
                        <label id="lbl_test" for="test">Test</label>
                        <input type="hidden" id="testAll" name="testAll"  value="false" />
                        <select id="test" class="form-control" >
                        </select>                                            
                    </div>       
                    <div class="form-group col-xs-4">
                        <label id="lbl_testcase" for="testcase">Test Case</label>
                        <input type="hidden" id="testCaseALl" name="testCaseAll"  value="false" />
                        <select id="testcase" class="form-control" >
                        </select>                                            
                    </div>       
                </div>
                <div class="row col-xs-10" id="tcMoreFilters" >
                    <div class="panel panel-default">
                        <div class="panel-heading card" data-toggle="collapse" data-target="#tcHeader">
                            <!--<span class="fa fa-pie-chart fa-fw"></span>-->
                            <label id="lbl_details">More filters</label>
                            <span class="toggle glyphicon glyphicon-chevron-right pull-right"></span>
                        </div>
                        <div class="panel-body collapse" id="tcHeader">
                            <div class="row">
                                <p>details</p>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="row">  
                    <div class="form-group col-xs-2 right">
                        <button type="button" id="btnResetSearch" class="btn btn-default"> Reset Filters </button>
                        <button type="button" id="btnLoadTC" class="btn btn-primary"> Load </button>
                    </div>
                </div>    
                <div class="row separator">  </div>
            </div>
            
            
            <div id="mainContent" class="invisible">
                <div class="col-xs-12" id="tcHeaderPanel">
                    <div class="panel panel-default">
                        <div class="panel-heading card" data-toggle="collapse" data-target="#tcHeader">
                            <!--<span class="fa fa-pie-chart fa-fw"></span>-->
                            <label id="lbl_details">Details</label>
                            <span class="toggle glyphicon glyphicon-chevron-right pull-right"></span>
                        </div>
                        <div class="panel-body collapse" id="tcHeader">
                            <div class="row">
                                <p>details</p>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="col-xs-12" id="tcStepsPanel">
                    <div class="panel panel-default">
                        <div class="panel-heading card" data-toggle="collapse" data-target="#tcSteps">
                            <!--<span class="fa fa-pie-chart fa-fw"></span>-->
                            <label id="lbl_steps">Steps</label>
                            <span class="toggle glyphicon glyphicon-chevron-right pull-right"></span>
                        </div>
                        <div class="panel-body collapse" id="tcSteps">
                            <div class="row">
                                <p>list of steps</p>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="col-xs-12" id="tcPropertiesPanel">
                    <div class="panel panel-default">
                        <div class="panel-heading card" data-toggle="collapse" data-target="#tcProperties">
                            <!--<span class="fa fa-pie-chart fa-fw"></span>-->
                            <label id="lbl_properties">Properties</label>
                            <span class="toggle glyphicon glyphicon-chevron-right pull-right"></span>
                        </div>
                        <div class="panel-body collapse" id="tcProperties">
                            <div class="row">
                                <p>list of properties</p>
                            </div>
                        </div>
                    </div>
                </div> 
                </div> 
            </div>    
        </div>
    </body>
</html>
