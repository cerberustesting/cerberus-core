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
        <script type="text/javascript" src="dependencies/Tinymce-6.7.0/tinymce.min.js"></script>
        <script type="text/javascript" src="dependencies/Bootstrap-treeview-1.2.0/js/bootstrap-treeview.js"></script>
        <script type="text/javascript" src="js/pages/Label.js"></script>
        <title id="pageTitle">Label</title>
    </head>
    <body>

        <%@ include file="include/global/header.html" %>
        <div class="container-fluid center" id="page-layout">
            <%@ include file="include/global/messagesArea.html"%>
            <%@ include file="include/utils/modal-confirmation.html"%>
            <%@ include file="include/pages/label/addLabel.html"%> 
            <%@ include file="include/pages/label/editLabel.html"%> 

            <h1 class="page-title-line" id="title">Label</h1>

            <ul id="tabsScriptEdit" class="nav nav-tabs" data-tabs="tabs">
                <li class="active"><a data-toggle="tab" href="#tabDetails" id="editTabDetails" name="tabDetails">List</a></li>
                <li><a data-toggle="tab" href="#tabTreeR" id="editTabTressR" name="tabTree">Requirement Tree</a></li>
                <li><a data-toggle="tab" href="#tabTreeS" id="editTabTressS" name="tabTree">Sticker Tree</a></li>
                <li><a data-toggle="tab" href="#tabTreeB" id="editTabTressB" name="tabTree">Battery Tree</a></li>
            </ul>

            <div class="tab-content">
                <div class="center tab-pane fade in active" id="tabDetails">
                    <div class="panel panel-default">
                        <div class="panel-body" id="labelList">
                            <table id="labelsTable" class="table table-bordered table-hover display" name="labelsTable"></table>
                            <!--                            <div class="marginBottom20"></div>-->
                        </div>
                    </div>
                </div>
                <div class="center tab-pane fade in" id="tabTreeR">
                    <div class="panel panel-default">
                        <div class="panel-body" id="labelRList">
                            <div class="row">
                                <div class='marginBottom20 marginLeft30'>
                                    <button id='refreshButtonTreeR' type='button' class='btn btn-default pull-left marginLeft15'>
                                        <span class='glyphicon glyphicon-refresh'></span> Refresh</button>
                                    <button id='createLabelButtonTreeR' type='button' class='btn btn-default pull-left'>
                                        <span class='glyphicon glyphicon-plus-sign'></span> Create</button>
                                    <button id='collapseAllTreeR' type='button' class='btn btn-default pull-left'>
                                        <span class='glyphicon glyphicon-collapse-up'></span> Collapse All</button>
                                    <button id='expandAllTreeR' type='button' class='btn btn-default pull-left'>
                                        <span class='glyphicon glyphicon-collapse-down'></span> Expand All</button>
                                </div>
                            </div>
                        </div>
                        <div class='marginTop20' id="mainTreeR"></div>
                    </div>
                </div>
                <div class="center tab-pane fade in" id="tabTreeS">
                    <div class="panel panel-default">
                        <div class="panel-body" id="labelSList">
                            <div class="row">
                                <div class='marginBottom20 marginLeft20'>
                                    <button id='refreshButtonTreeS' type='button' class='btn btn-default pull-left marginLeft15'>
                                        <span class='glyphicon glyphicon-refresh'></span> Refresh</button>
                                    <button id='createLabelButtonTreeS' type='button' class='btn btn-default pull-left'>
                                        <span class='glyphicon glyphicon-plus-sign'></span> Create</button>
                                    <button id='collapseAllTreeS' type='button' class='btn btn-default pull-left'>
                                        <span class='glyphicon glyphicon-collapse-up'></span> Collapse All</button>
                                    <button id='expandAllTreeS' type='button' class='btn btn-default pull-left'>
                                        <span class='glyphicon glyphicon-collapse-down'></span> Expand All</button>
                                </div>
                            </div>
                            <div class='marginTop20' id="mainTreeS"></div>
                        </div>
                    </div>
                </div>
                <div class="center tab-pane fade in" id="tabTreeB">
                    <div class="panel panel-default">
                        <div class="panel-body" id="labelBList">
                            <div class="row">
                                <div class='marginBottom20 marginLeft20'>
                                    <button id='refreshButtonTreeB' type='button' class='btn btn-default pull-left marginLeft15'>
                                        <span class='glyphicon glyphicon-refresh'></span> Refresh</button>
                                    <button id='createLabelButtonTreeB' type='button' class='btn btn-default pull-left'>
                                        <span class='glyphicon glyphicon-plus-sign'></span> Create</button>
                                    <button id='collapseAllTreeB' type='button' class='btn btn-default pull-left'>
                                        <span class='glyphicon glyphicon-collapse-up'></span> Collapse All</button>
                                    <button id='expandAllTreeB' type='button' class='btn btn-default pull-left'>
                                        <span class='glyphicon glyphicon-collapse-down'></span> Expand All</button>
                                </div>
                            </div>
                            <div class='marginTop20' id="mainTreeB"></div>
                        </div>
                    </div>
                </div>
            </div>
            <!--                </div>-->
            <!--            </div>-->
            <footer class="footer">
                <div class="container-fluid" id="footer"></div>
            </footer>
        </div>
    </body>
</html>
