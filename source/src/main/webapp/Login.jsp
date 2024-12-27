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
<%@page import="org.cerberus.core.database.IDatabaseVersioningService"%>
<%@page import="org.cerberus.core.crud.factory.impl.FactoryLogEvent"%>
<%@page import="org.cerberus.core.crud.factory.IFactoryLogEvent"%>
<%@page import="org.cerberus.core.crud.service.impl.LogEventService"%>
<%@page import="org.cerberus.core.crud.service.ILogEventService"%>
<%@page import="org.cerberus.core.version.Infos"%>
<%@page import="org.cerberus.core.crud.service.IParameterService"%>
<%@page import="org.springframework.web.context.support.WebApplicationContextUtils"%>
<%@page import="org.springframework.context.ApplicationContext"%>
<%@ page import="org.cerberus.core.crud.entity.LogEvent" %>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%@ include file="include/global/dependenciesInclusions.html" %>
        <script type='text/javascript' src='js/pages/Login.js'></script>
        <script type='text/javascript' src='js/global/global.js'></script>
        <title>Login</title>
    </head>
    <body style="background-color: #fff">
        <%@ include file="include/utils/modal-confirmation.html"%>

        <script type="text/javascript">
            envTuning("<%=System.getProperty("org.cerberus.environment")%>");
        </script>

        <%
            ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
            IParameterService myParameterService = appContext.getBean(IParameterService.class);
            IDatabaseVersioningService databaseVersionService = appContext.getBean(IDatabaseVersioningService.class);
            try {
                String cerberusSupportEmail = myParameterService.getParameterStringByKey("cerberus_support_email", "", "");
                String cerberusWelcomeMessage = myParameterService.getParameterStringByKey("cerberus_loginpage_welcomemessagehtml", "", "");
                cerberusWelcomeMessage = cerberusWelcomeMessage.replace("%SUPPORTEMAIL%", cerberusSupportEmail);
                String errorMessage = "";
                String display = "";
                String cerberusVersion = Infos.getInstance().getProjectVersion() + "-" + databaseVersionService.getSqlVersion();

                if (request.getParameter("error") != null && request.getParameter("error").equalsIgnoreCase("1")) {
                    errorMessage = "User or password invalid !!!";
                    display = "1";
                    /**
                     * Adding Log entry.
                     */
                    ILogEventService logEventService = appContext.getBean(LogEventService.class);
                    IFactoryLogEvent factoryLogEvent = appContext.getBean(FactoryLogEvent.class);
                    logEventService.create(factoryLogEvent.create(0, 0, request.getParameter("j_username"), null, "/Login.jsp", "LOGINERROR", LogEvent.STATUS_ERROR, "Invalid Password for : " + request.getParameter("j_username"), request.getRemoteAddr(), request.getLocalAddr()));
                }
        %>



        <div class="body-login">
            <div class="col-md-3"></div>


            <div class="col-md-6 panel panel-default panel-login" >

                <div class="col-md-12">
                    <%@ include file="include/global/messagesArea.html"%>
                    <div id="error" style="display:none"><%=display%></div>
                </div>

                <div class="col-md-12 login-box-form">
                    <div class="col-md-6">
                        <img src="images/Logo-cerberus_login.png" class="img-responsive center-block logo-login"></img>
                    </div>
                    <div class="col-md-6">

                        <form method="post" action="j_security_check" id="login-box">

                            <H2>Cerberus Login</H2>
                            <em>V<%=cerberusVersion%></em><br>
                            <span class="text-info">
                                <%= cerberusWelcomeMessage%>
                            </span>
                            <br><br>


                            <div class="form-group">
                                <label>
                                    Username:
                                </label>
                                <div class="input-group">
                                    <span class="input-group-addon " id="user-icon">	
                                        <span class="glyphicon glyphicon-user"></span>
                                    </span>
                                    <input id="username" name="j_username" class="form-control" title="Username" placeholder="Username" value="" size="50" maxlength="50" aria-describedby="user-icon" autofocus>
                                </div>
                            </div>
                            <div class="form-group">
                                <label>
                                    Password:
                                </label>
                                <div class="input-group">
                                    <span class="input-group-addon " id="user-icon">	
                                        <span class="glyphicon glyphicon-lock"></span>
                                    </span>
                                    <input name="j_password" autocomplete class="form-control" type="password" placeholder="Password" title="Password" value="" size="30" maxlength="20">
                                </div>
                            </div>

                            <div class="row">
                                <div class="col-xs-12">
                                    <div class="form-group">
                                        <button id="Login" name="Login" class="btn btn-primary col-md-6 col-xs-12 pull-right" value="Submit" alt="Submit" onclick="sessionStorage.clear()">Login</button>
                                    </div>
                                </div>
                            </div>
                            <div class="row">
                                <div class="col-md-12">
                                    <div class="form-group">
                                        <button id="forgot-password" type="button" name="forgotPassword" class="btn-link pull-right" onclick="showForgotPasswordFormulary()">Forgot password</button>
                                    </div>
                                </div>
                            </div>
                        </form>


                        <div class="row">
                            <div id="forgot-password-box" style="display:none">
                                <form action="#" id="forgotpassword-box">
                                    <div class="col-md-12">
                                        <H2>Forgot password</H2>
                                        <em>V<%=cerberusVersion%></em><br>
                                        <span class="text-info">
                                            Please enter your username. An email will be sent to your associated email address with a link to reset your password. <br />
                                            <%= cerberusWelcomeMessage%>
                                        </span>
                                    </div>
                                    <div class="col-md-12">
                                        <div class="form-group">
                                            <label>
                                                Username:
                                            </label>
                                            <div class="input-group">
                                                <span class="input-group-addon " id="user-icon2">
                                                    <span class="glyphicon glyphicon-user"></span>
                                                </span>
                                                <input name="login" class="form-control" id="loginForgotPassword" title="Username" placeholder="Username" value="" size="30" aria-describedby="user-icon2" autofocus>
                                            </div>
                                        </div>
                                    </div>

                                    <div class="col-xs-12">
                                        <button id="RecoverPassword" name="RecoverPassword" value="Submit" class="btn btn-primary col-md-6 col-xs-12 pull-right" onclick="forgotPassword()">Send me an email</button>
                                    </div>

                                    <div class="col-xs-12">
                                        <button id="cancel-forgot-password" type="button" name="cancelForgotPassword" class="btn-link pull-right" onclick="showLoginBoxFormulary()">Cancel</button>
                                    </div>

                                </form>
                            </div>
                        </div>

                    </div>
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