<%-- 
    Document   : Error
    Created on : 19 août 2014, 07:19:16
    Author     : bcivel
--%>
<% Date DatePageStart = new Date();%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link rel="stylesheet" type="text/css" href="css/crb_style.css">
        <link rel="shortcut icon" type="image/x-icon" href="images/favicon.ico">
        <title>Error</title>
    </head>
    <body>
        <%@ include file="../include/function.jsp" %>
        <%@ include file="../include/header.jsp" %>
        <%
        if (request.getParameter("error").equals("403")) 
                {
        %>
        <h1>Not Authorised Ressource...</h1>
        <h3> You don't have enough privilege to open this page, Please ask your Cerberus administrator to grant the access.</h3>
        <%
                
                } else {
        %>
        <h1>Oops...</h1>
        <h3>Sorry but an unexpected error occurred.</h3>
        <%
                        }
        %>
        <br><br>
        <h4>Error Code : <%=request.getParameter("error")%></h4>
        <h4><%=request.getAttribute("javax.servlet.error.message")%></h4>
        <br><%
            out.print(display_footer(DatePageStart));
        %>
    </body>
</html>
