<%-- 
    Document   : Test2
    Created on : 22 sept. 2015, 10:54:19
    Author     : cerberus
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta content="text/html; charset=UTF-8" http-equiv="content-type">
        <%@ include file="include/dependenciesInclusions.html" %>
        <script type="text/javascript" src="js/pages/Test.js"></script>
        <title id="pageTitle">Test</title>
    </head>
    <body>
        <%@ include file="include/header.html"%>
        <div class="container-fluid center" id="page-layout">
            <%@ include file="include/messagesArea.html"%>
            <%@ include file="include/utils/modal-confirmation.html"%>
            <%@ include file="include/test/editTest.html" %>
            <%@ include file="include/test/addTest.html" %>

            <h1 class="page-title-line" id="title">Test</h1>
            <div class="panel panel-default">
                <div class="panel-heading">
                    <span class="glyphicon glyphicon-list"></span>
                    <label id="testListLabel">Test List</label>
                </div>
                <div class="panel-body" id="testList">
                    <table id="testTable" class="table table-bordered table-hover display" name="testTable">
                    </table>
                    <div class="marginBottom20"></div>
                </div>
            </div>
            <footer class="footer">
                <div class="container-fluid" id="footer"></div>
            </footer>
        </div>
    </body>
</html>
