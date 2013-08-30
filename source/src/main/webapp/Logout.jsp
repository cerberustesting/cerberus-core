<%-- 
    Document   : logout
    Created on : 9/Fev/2012, 16:12:24
    Author     : ip100003
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link rel="stylesheet" type="text/css" href="css/crb_style.css">
        <link rel="shortcut icon" type="image/x-icon" href="images/favicon.ico">
        <META HTTP-EQUIV="refresh" CONTENT="1;URL=Homepage">
        <title>Logout</title>
    </head>
    <body> 
        <%@ include file="include/function.jsp" %>
        <%@ include file="include/header.jsp" %>
        <div align="center" style="padding-top: 12%;">
            <h1>Logout Successfully</h1>
        </div>
    </body>
</html>
<% session.invalidate(); %>
