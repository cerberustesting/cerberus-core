<%--

    Cerberus Copyright (C) 2013 - 2017 cerberustesting
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
<%@page import="org.cerberus.database.IDatabaseVersioningService"%>
<%@page import="org.cerberus.crud.factory.impl.FactoryLogEvent"%>
<%@page import="org.cerberus.crud.factory.IFactoryLogEvent"%>
<%@page import="org.cerberus.crud.service.impl.LogEventService"%>
<%@page import="org.cerberus.crud.service.ILogEventService"%>
<%@page import="org.cerberus.version.Infos"%>
<%@page import="org.cerberus.log.MyLogger"%>
<%@page import="org.cerberus.crud.service.IParameterService"%>
<%@page import="org.apache.log4j.Level"%>
<%@page import="org.springframework.web.context.support.WebApplicationContextUtils"%>
<%@page import="org.springframework.context.ApplicationContext"%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%@ include file="include/dependenciesInclusions.html" %>
        <script type='text/javascript' src='js/pages/Login.js'></script>
        <script type="text/javascript">
            EnvTuning("<%=System.getProperty("org.cerberus.environment")%>");
        </script>
        <title>Login</title>
    </head>
    <body style="background-color: white">

        <%
            ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
            IParameterService myParameterService = appContext.getBean(IParameterService.class);
            IDatabaseVersioningService databaseVersionService = appContext.getBean(IDatabaseVersioningService.class);
            try {
                String CerberusSupportEmail = myParameterService.findParameterByKey("cerberus_support_email", "").getValue();
                String errorMessage = "";
                String display = "";
                String cerberusVersion = Infos.getInstance().getProjectVersion() + "-" + databaseVersionService.getSQLScript().size();

                if (request.getParameter("error") != null && request.getParameter("error").equalsIgnoreCase("1")) {
                    errorMessage = "User or password invalid !!!";
                    display = "1";
                    /**
                     * Adding Log entry.
                     */
                    ILogEventService logEventService = appContext.getBean(LogEventService.class);
                    IFactoryLogEvent factoryLogEvent = appContext.getBean(FactoryLogEvent.class);
                    logEventService.create(factoryLogEvent.create(0, 0, request.getParameter("j_username"), null, "/Login.jsp", "LOGINERROR", "Invalid Password for : " + request.getParameter("j_username"), request.getRemoteAddr(), request.getLocalAddr()));
                }
        %>

        <%@ include file="include/messagesArea.html"%>
        <div id="error"><%=display%></div>
        <div style="padding-top: 7%; padding-left: 30%">
            <div id="login-box" class="login-box" >
                <H2>Cerberus Login</H2><br>V<%=cerberusVersion%><br><br>
                Please login in order to change TestCases and run Tests.<br>
                If you don't have login, please contact <%= CerberusSupportEmail%>
                <br>
                <br>
                <form method="post" action="j_security_check">
                    <div class="row">
                        <div class="form-group col-xs-3" style="margin-top:10px;">
                            Username:
                        </div>
                        <div class="form-group col-xs-9">
                            <input name="j_username" class="form-login" title="Username" value="" size="30" maxlength="10" autofocus>
                        </div>
                    </div>
                    <div class="row">
                        <div class="form-group col-xs-3" style="margin-top:10px;">
                            Password:
                        </div>
                        <div class="form-group col-xs-9">
                            <input name="j_password" class="form-login" type="password" title="Password" value="" size="30" maxlength="20">
                        </div>
                    </div>
                    <button id="Login" name="Login" class="btn btn-primary col-xs-12" value="Submit" alt="Submit" onclick="sessionStorage.clear()";>Login</button>
                </form>
                <br><br>
                <div class="col-xs-12">
                    <a onclick="showForgotPasswordFormulary()">forgot password</a>
                </div>
            </div>
            <div id="forgot-password-box" style="display: none" class="login-box">
                <H2>Cerberus Login</H2><br>V<%=cerberusVersion%><br><br>
                Please feed the field with your login. An email will be sent with the recovery information.<br>
                If you don't have login, please contact <%= CerberusSupportEmail%>
                <br>
                <br>
                <div class="row">
                    <div class="form-group col-xs-3" style="margin-top:10px;">
                        Username:
                    </div>
                    <div class="form-group col-xs-9">
                        <input name="login" id="loginForgotPassword" class="form-login" title="Username" value="" size="30" maxlength="10">
                    </div>
                </div>
                <br><br>
                <button id="RecoverPassword" name="RecoverPassword" class="btn btn-primary col-xs-12" onclick="forgotPassword()">Reset Password</button>
                <br><br>
                <div class="col-xs-12">
                    <a href="./">homepage</a>
                </div>
            </div>
        </div>
    </body>
</html>
<%
    } catch (Exception ex) {
        // This exception should only happen when the database is empty. In 
        // that case we redirect to the page that will automatically create the database.        
        request.getRequestDispatcher("/DatabaseMaintenance.jsp").forward(request, response);
    }
%>