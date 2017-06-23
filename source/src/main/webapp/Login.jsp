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
    <body>

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

       
       
       <div class="body-login">
       		<div class="col-md-2"></div>
       
       
            <div class="col-md-8 panel panel-default panel-login" >

				<div class="col-md-12">
				    <%@ include file="include/messagesArea.html"%>
            		<div id="error" style="display:none"><%=display%></div>
            	</div>
            	
            	<div class="col-md-12">
            		<div class="col-md-6">
            			<img src="images/logo250.png" class="img-responsive center-block logo-login"></img>
            		</div>
            		<div class="col-md-6">
		                <H2>Cerberus Login</H2>
		                <em>V<%=cerberusVersion%></em><br>
		                <span class="text-info">
		                If you don't have login, please contact <%= CerberusSupportEmail%>
		                </span>	

		                <br>
		                <form method="post" action="j_security_check" id="login-box">
	    	                <div class="form-group">
	        	                <label>
	            	                Username:
	                	        </label>
	                    	   	<div class="input-group">
		                       	 	<span class="input-group-addon " id="user-icon">	
		                       	 		<span class="glyphicon glyphicon-user"></span>
		                       	 	</span>
		                        	<input name="j_username" class="form-control" title="Username" placeholder="Username" value="" size="30" maxlength="10" aria-describedby="user-icon" autofocus>
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
		                        	<input name="j_password" class="form-control" type="password" placeholder="Password" title="Password" value="" size="30" maxlength="20">
								</div>
	                    	</div>
	                    	<button id="Login" name="Login" class="btn btn-primary" value="Submit" alt="Submit" onclick="sessionStorage.clear()">Login</button>
	                    	<button id="forgot-password" type="button" name="forgotPassword" class="btn btn-warning" onclick="showForgotPasswordFormulary()">Forgot password</button>
	                	</form>
	                				                			       
				        <div id="forgot-password-box" style="display:none">
				        	<div class="form-group">
		                       	<label>
		                            Username:
		                        </label>
		                        <div class="input-group">
		                       	 	<span class="input-group-addon " id="user-icon2">	
		                       	 		<span class="glyphicon glyphicon-user"></span>
		                       	 	</span>
		                        	<input name="login" class="form-control" id="loginForgotPassword" title="Username" placeholder="Username" value="" size="30" maxlength="10" aria-describedby="user-icon2" autofocus>
								</div>
	                    	</div>
			                
			                <br><br>
			                
			                <button id="RecoverPassword" name="RecoverPassword" class="btn btn-primary" onclick="forgotPassword()">Forgot password</button>
			                			               
			                <a href="./" class="btn btn-warning">Homepage</a>
			                
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