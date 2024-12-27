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
        <script type="text/javascript" src="js/pages/BuildContent.js"></script>
        <title id="pageTitle">Build Content</title>
    </head>
    <body>
        <%@ include file="include/global/header.html" %>
        <div class="container-fluid center" id="page-layout">
            <%@ include file="include/global/messagesArea.html"%>
            <%@ include file="include/utils/modal-confirmation.html"%>
            <%@ include file="include/pages/buildcontent/addBuildContent.html"%>
            <%@ include file="include/pages/buildcontent/editBuildContent.html"%>
            <%@ include file="include/pages/buildcontent/massActionBuildContent.html"%>
            <%@ include file="include/pages/buildcontent/listInstallInstructions.html"%>

            <h1 class="page-title-line" id="title">Build Content</h1>

            <div class="row">
                <div class="col-lg-12" id="FiltersPanel">
                    <div class="panel panel-default">
                        <div class="panel-heading card">
                            <span class="glyphicon glyphicon-filter"></span>
                            <label id="filters" name="filtersField">Filters</label>
                        </div>
                        <div class="panel-body">
                            <div class="row">
                                <div class="col-md-3 ">
                                    <label for="selectSystem" name="systemField">System :</label>
                                    <select class="form-control" id="selectSystem" name="system" ></select>
                                </div>
                                <div class="col-md-3 ">
                                    <label for="selectApplication" name="applicationField">Application :</label>
                                    <select class="form-control" id="selectApplication" name="application" ></select>
                                </div>
                                <div class="col-md-3 ">
                                    <label for="selectBuild" name="buildField">Build :</label>
                                    <select class="form-control" id="selectBuild" name="buildf" ></select>
                                </div>
                                <div class="col-md-3 ">
                                    <label for="selectRevision" name="revisionField">Revision :</label>
                                    <select class="form-control" id="selectRevision" name="revisionf" ></select>
                                </div>
                            </div>
                            <div class="marginTop20">
<!--                                <div class=" ">-->
                                    <button type="button" class="btn btn-default" id="btnLoad" onclick="loadBCTable()" name="btnLoad">Load</button>
                                    <button type="button" class="btn btn-default" id="btnLoadAll" onclick="setAll()" name="btnLoadAll">Load All Build</button>
                                    <button type="button" class="btn btn-default" id="btnLoadPending" onclick="setPending()" name="btnLoadPending">Load Pending Build</button>
                                    <button type="button" class="btn btn-default" id="btnLoadLatest" onclick="setLatest()" name="btnLoadLatest">Load Latest Build</button>
                                    <button type="button" class="btn btn-default" id="btnViewInstall" onclick="displayInstallInstructions()" name="btnViewInstall">Preview Install instructions</button>
<!--                                </div>-->
                            </div>
                            <!--                                <div class="row">
                                                                <div class="marginLeft5">
                                                                </div>
                                                            </div>-->
                        </div>
                    </div>
                </div>
            </div>

            <div class="panel panel-default">
                <div class="panel-heading card">
                    <span class="glyphicon glyphicon-list"></span>
                    <label id="shortcuts" name="listField">Build Content List</label>
                </div>
                <form id="massActionForm" name="massActionForm"  title="" role="form">
                    <div class="panel-body" id="buildContentList">
                        <table id="buildrevisionparametersTable" class="table table-hover display" name="buildrevisionparametersTable"></table>
                        <div class="marginBottom20"></div>
                    </div>
                </form>
            </div>

            <footer class="footer">
                <div class="container-fluid" id="footer"></div>
            </footer>
        </div>
    </body>
</html>
