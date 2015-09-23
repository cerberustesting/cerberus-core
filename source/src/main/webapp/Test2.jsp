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
        <title>Test</title>
    </head>
    <body>
        <%@ include file="include/header.html"%>
        <%@ include file="include/test/editTest.html" %>
        <%@ include file="include/test/addTest.html" %>
        <div class="container-fluid center" id="page-layout">
            <h1 class="page-title-line" id="title">Test</h1>

                <div class="well" id="testList">
                    <table id="testTable" class="table table-hover display" name="testTable"></table>
                    <div class="marginBottom20"></div>
                </div>

            <footer class="footer">
                <div class="container-fluid" id="footer"></div>
            </footer>
        </div>
    </body>
</html>
