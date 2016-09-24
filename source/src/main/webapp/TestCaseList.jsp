<%-- 
    Document   : Test2
    Created on : 23 sept. 2015, 16:07:19
    Author     : cerberus
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta content="text/html; charset=UTF-8" http-equiv="content-type">
        <%@ include file="include/dependenciesInclusions.html" %>
        <script type="text/javascript" src="dependencies/Tinymce-4.2.6/tinymce.min.js"></script>
        <script type="text/javascript" src="js/pages/TestCaseList.js"></script>
        <script type="text/javascript" src="js/pages/transversalobject/TestCase.js"></script>
        <title id="pageTitle">Test Case List</title>
    </head>
    <body>
        <%@ include file="include/header.html"%>
        <div class="container-fluid center" id="page-layout">
            <%@ include file="include/messagesArea.html"%>
            <%@ include file="include/utils/modal-confirmation.html"%>
            <%@ include file="include/transversalobject/TestCase.html"%>
            <%@ include file="include/testcaselist/massActionTestCase.html"%>
            <%@ include file="include/testcasescript/manageLabel.html"%>

            <h1 class="page-title-line" id="title">Test Case List</h1>

            <div class="row">
                <div class="col-lg-6" id="FiltersPanel">
                    <div class="panel panel-default">
                        <div class="panel-heading">
                            <span class="glyphicon glyphicon-filter"></span>
                            <label id="filters">Filters</label>
                        </div>
                        <div class="panel-body">
                            <label for="selectTest" name="testField">Test :</label>
                            <div class="form-inline">
                                <select class="form-control" id="selectTest" style="width: 90%"></select>
                                <button type="button" class="btn btn-default" name="btnLoad" id="btnLoad" onclick="loadTable()">Load</button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="panel panel-default">
                <div class="panel-heading">
                    <span class="glyphicon glyphicon-list"></span>
                    <label id="testCaseListLabel">Test Case List</label>
                </div>
                <div class="panel-body" id="testCaseList">
                    <table id="testCaseTable" class="table table-bordered table-hover display" name="testCaseTable"></table>
                    <div class="marginBottom20"></div>
                </div>
            </div>

            <footer class="footer">
                <div class="container-fluid" id="footer"></div>
            </footer>
        </div>
    </body>
</html>
