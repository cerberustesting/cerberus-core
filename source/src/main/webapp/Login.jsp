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
<html class="crb_html dark">
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%@ include file="include/global/dependenciesInclusions.html" %>
        <script type='text/javascript' src='js/pages/Login.js'></script>
        <script type='text/javascript' src='js/global/global.js'></script>
        <script src="dependencies/Tsparticles-2.12.0/tsparticles.min.js"></script>
        <title>Login</title>
    </head>
    <body x-data x-cloak class="crb_body">
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



        <div class="w-full min-h-screen flex items-center justify-center crb_main">
            <div id="tsparticles" class="absolute inset-0 z-0 pointer-events-none"></div>
            <div class="relative z-10 flex items-center justify-center w-full">
            <div class="crb_card w-1/2 p-6">

                <!-- Messages Area -->
                <div class="mb-4">
                    <%@ include file="include/global/messagesArea.html"%>
                    <div id="error" class="hidden text-red-500"><%=display%></div>
                </div>

                <div class="flex flex-col md:flex-row gap-6">

                    <!-- Logo -->
                    <div class="md:w-1/2 flex justify-center items-center">
                        <!--<img src="images/Logo-cerberus_login.png" class="logo-login" :class="$store.user.theme === 'light' ? 'block' : 'hidden'" alt="Cerberus Logo">-->
                        <img src="images/Logo-cerberus_menu.png" class="logo-login" alt="Cerberus Logo">
                    </div>

                    <!-- Form Section -->
                    <div class="md:w-1/2">
                        <form method="post" action="j_security_check" id="login-box" class="space-y-4">

                            <h2 class="text-2xl font-bold">Cerberus Login</h2>
                            <em class="text-sm">V<%=cerberusVersion%></em><br>
                            <span class="text-blue-500 text-sm">
            <%= cerberusWelcomeMessage%>
          </span>

                            <!-- Username -->
                            <div>
                                <label class="block mb-1">Username:</label>
                                <div class="flex items-center border rounded">
              <span class="px-2 text-gray-500">
                <span class="glyphicon glyphicon-user"></span>
              </span>
                                    <input id="username" name="j_username" class="flex-1 p-2 outline-none" title="Username" placeholder="Username" value="" maxlength="50" autofocus>
                                </div>
                            </div>

                            <!-- Password -->
                            <div>
                                <label class="block mb-1">Password:</label>
                                <div class="flex items-center border rounded">
              <span class="px-2 text-gray-500">
                <span class="glyphicon glyphicon-lock"></span>
              </span>
                                    <input name="j_password" type="password" class="flex-1 p-2 outline-none" placeholder="Password" title="Password" maxlength="20" autocomplete>
                                </div>
                            </div>

                            <!-- Login Button -->
                            <div class="flex justify-end">
                                <button id="Login" name="Login" type="submit" class="btn btn-primary w-1/2" onclick="sessionStorage.clear()">Login</button>
                            </div>

                            <!-- Forgot Password Link -->
                            <div class="flex justify-end">
                                <button id="forgot-password" type="button" class="text-blue-500 hover:underline" onclick="showForgotPasswordFormulary()">Forgot password</button>
                            </div>

                        </form>

                        <!-- Forgot Password Form -->
                        <div id="forgot-password-box" class="hidden mt-4">
                            <form id="forgotpassword-box" class="space-y-4">
                                <h2 class="text-2xl font-bold">Forgot password</h2>
                                <em class="text-sm">V<%=cerberusVersion%></em><br>
                                <span class="text-blue-500 text-sm">
              Please enter your username. An email will be sent to your associated email address with a link to reset your password. <br />
              <%= cerberusWelcomeMessage%>
            </span>

                                <!-- Username -->
                                <div>
                                    <label class="block mb-1">Username:</label>
                                    <div class="flex items-center border rounded">
                <span class="px-2 text-gray-500">
                  <span class="glyphicon glyphicon-user"></span>
                </span>
                                        <input name="login" id="loginForgotPassword" class="flex-1 p-2 outline-none" placeholder="Username" autofocus>
                                    </div>
                                </div>

                                <!-- Buttons -->
                                <div class="flex justify-end gap-2">
                                    <button id="RecoverPassword" name="RecoverPassword" type="button" class="btn btn-primary" onclick="forgotPassword()">Send me an email</button>
                                    <button id="cancel-forgot-password" type="button" class="text-blue-500 hover:underline" onclick="showLoginBoxFormulary()">Cancel</button>
                                </div>
                            </form>
                        </div>

                    </div>
                </div>

            </div>
            </div>
        </div>

    </body>
    <script>
        document.addEventListener("DOMContentLoaded", function () {
            tsParticles.load("tsparticles", {
                fullScreen: {
                    enable: false
                },
                background: {
                    color: {
                        value: "transparent"
                    }
                },
                particles: {
                    number: {
                        value: 80,
                        density: {
                            enable: true,
                            area: 900
                        }
                    },
                    color: {
                        value: "#3b82f6"
                    },
                    links: {
                        enable: true,
                        distance: 140,
                        color: "#3b82f6",
                        opacity: 0.35,
                        width: 1
                    },
                    move: {
                        enable: true,
                        speed: 0.4,
                        outModes: {
                            default: "bounce"
                        }
                    },
                    size: {
                        value: 2
                    },
                    opacity: {
                        value: 0.8
                    }
                },
                detectRetina: true
            });
        });
    </script>
</html>
<%
    } catch (Exception ex) {
        // This exception should only happen when the database is empty. In 
        // that case we redirect to the page that will automatically create the database.        
        request.getRequestDispatcher("/DatabaseMaintenance.jsp").forward(request, response);
    }
%>