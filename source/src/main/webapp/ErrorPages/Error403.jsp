<%-- 
    Document   : Error403
    Created on : 19 août 2014, 07:19:16
    Author     : bcivel
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <link rel="stylesheet" type="text/css" href="css/crb_style.css">
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Error403</title>
    </head>
    <body>
        <%@ include file="../include/header.jsp" %>
        <h1>Error</h1>
        <h3>An error occure!
        <br><br>
        <h4><%=request.getAttribute("javax.servlet.error.message")%></h4>
    </body>
</html>
