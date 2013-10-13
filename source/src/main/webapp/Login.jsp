<%-- 
    Document   : login
    Created on : 7/Fev/2012, 16:06:22
    Author     : ip100003
--%>
<%@page import="com.redcats.tst.log.MyLogger"%>
<%@page import="org.apache.log4j.Level"%>
<%@page import="com.redcats.tst.service.IParameterService"%>
<%@page import="org.springframework.web.context.support.WebApplicationContextUtils"%>
<%@page import="org.springframework.context.ApplicationContext"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>


<%
    ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
    IParameterService myParameterService = appContext.getBean(IParameterService.class);
    try {
        String CerberusSupportEmail = myParameterService.findParameterByKey("cerberus_support_email").getValue();
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link rel="stylesheet" type="text/css" href="css/crb_style.css">
        <link rel="shortcut icon" type="image/x-icon" href="images/favicon.ico">
        <title>Login</title>
    </head>
    <body>

        <script type='text/javascript' src='js/Form.js'></script>
        <script type="text/javascript">
            EnvTuning("<%=System.getProperty("org.cerberus.environment")%>");
        </script>

        <div style="padding-top: 7%; padding-left: 30%">
            <div id="login-box">
                <H2>Login</H2>
                Please login in order to change TestCases and run Tests.<br>
                If you don't have login, please contact <%= CerberusSupportEmail%>
                <br>
                <br>
                <form method="post" action="j_security_check">
                    <div class="login-box-name" style="margin-top:20px;">
                        Username:
                    </div>
                    <div class="login-box-field" style="margin-top:20px;">
                        <input name="j_username" class="form-login" title="Username" value="" size="30" maxlength="10">
                    </div>
                    <div class="login-box-name">
                        Password:
                    </div>
                    <div class="login-box-field">
                        <input name="j_password" type="password" class="form-login" title="Password" value="" size="30" maxlength="20">
                    </div>
                    <br>
                    <br>
                    <input id="Login" name="Login" type="image" src="images/login-btn.png" value="Submit" alt="Submit" style="margin-left:90px;">
                </form>
            </div>
        </div>
    </body>
</html>
<%
    } catch (Exception ex) {
        request.getRequestDispatcher("/DatabaseMaintenance.jsp?GO=Y").forward(request, response);
        MyLogger.log("Login.jsp", Level.FATAL, " Exception catched : " + ex);
    }
%>