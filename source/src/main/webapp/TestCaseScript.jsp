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
        <script type="text/javascript" src="js/tinymce/tinymce.min.js"></script>
        <script type="text/javascript" src="js/pages/TestCaseScript.js"></script>
        <link rel="stylesheet" type="text/css" href="css/pages/TestCaseScript.css">
    </head>
    <body>
        <%@ include file="include/header.html"%>
        <div id="page-layout" class="container-fluid center">
            <%@ include file="include/messagesArea.html"%>
            <%@ include file="include/testcaselist/editTestCase.html"%>

            <h1 class="page-title-line">Test Case Script</h1>
            <h1 class="testTestCase"><span id="test">THESEUS</span> / <span id="testCase">0001A</span> - <span id="description">Description of the Test Case</span></h1>
            <div class="row" style="margin-top: 10px;">
                <div class="col-lg-3">
                    <h3>Steps</h3>
                    <ul class="list-group step-list side-item" id="stepList"></ul>
                    <button class="btn btn-primary btn-block" id="addStep">Add step</button>
                </div>
                <div class="col-lg-8 well" style="min-height: 200px;">
                    <div class="step-header clearfix">
                        <div class="row">
                            <div id="stepDescription" class="col-lg-9"></div>
                            <div class="col-lg-3" id="editBtnArea" style="display: none;">
                                <div class="btn-group pull-right">
                                    <button class="btn btn-default" id="editBtn"><span class="glyphicon glyphicon-pencil"></span></button>
                                    <button class="btn btn-default" id="deleteStep"><span class="glyphicon glyphicon-trash"></span></button>
                                </div>
                            </div>
                        </div>
                        <div id="editStep" style="display: none;" class="row">
                            <div class="input-group">
                                <input type="text" class="form-control" id="editStepDescription">
                                <div class="input-group-btn">
                                    <button class="btn btn-default" id="saveStep">Save</button>
                                    <button class="btn btn-default" id="cancelEdit">Cancel</button>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div id="actionContainer"></div>
                </div>
                <div class="col-lg-1">
                    <div class="separator-left" data-spy="affix">
                        <h3> Actions </h3>
                        <div class="side-item">Last Execution was <a style="color : green">OK</a> in PREPROD in FR on Fri Nov 13 17:43:44 CET 2015<a><i> (Run it again) </i></a></div>
                        <button class="btn btn-block btn-primary side-item" id="editTcInfo">Edit Test Case info</button>
                        <button class="btn btn-block btn-primary side-item">Save Script</button>
                        <button class="btn btn-block btn-primary side-item">Run this Test Case</button>
                    </div>
                </div>
            </div>
            <footer class="footer">
                <div id="footer" style="display: inline-block"></div>
            </footer>
        </div>
    </body>
</html>
