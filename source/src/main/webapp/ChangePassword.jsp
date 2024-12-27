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
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%@ include file="include/global/dependenciesInclusions.html" %>
        <title>Change Password</title>
        <script type="text/javascript" src="js/pages/ChangePassword.js"></script>
    </head>
    <body>

        <%@ include file="include/global/messagesArea.html"%>
        <div class="body-login">
            <div class="col-md-2"></div>

            <div class="col-md-8 panel panel-default panel-login" >

                <div class="col-md-12">
                    <%@ include file="include/global/messagesArea.html"%>
                    <div id="error" style="display:none"><%="0"/*display*/%></div>
                </div>

                <div class="col-md-12 login-box-form">

                    <div class="col-md-6">
                        <img src="images/Logo-cerberus_250.png" class="img-responsive center-block logo-login"></img>
                    </div>
                    <div class="col-md-6">

                        <form id="changePasswordForm" title="Change Password" method="post">
                            <H3>Password Recovery</H3>
                            <span>
                                Dear,<br><br>
                                A request password request has been submitted to your account.
                                Please feed the reset password formulary to acheive the change.
                                <br><br>
                            </span>

                            <input type="hidden" name="login" id="login" value="<%=request.getUserPrincipal() == null ? request.getParameter("login") : request.getUserPrincipal().getName()%>">
                            <input type="hidden" name="resetPasswordToken" id="resetPasswordToken"/>

                            <div class="form-group"  id="currentPasswordDiv">
                                <label>
                                    Current Password:
                                </label>
                                <div class="input-group">
                                    <span class="input-group-addon " id="user-icon">
		                       	 		<span class="glyphicon glyphicon-lock"></span>
		                       	 	</span>
                                    <input class="form-control" type="password" name="currentPassword" id="currentPassword" maxlength="20"/>
                                </div>
                            </div>

                            <div class="form-group">
                                 <label>
                                    New password:
                                 </label>
                                 <input  class="form-control" type="password" name="newPassword" id="newPassword" maxlength="20"/>
                            </div>

                            <div class="form-group">
                                <label>
                                    New password confirmation:
                                </label>
                                <input class="form-control" type="password" name="confirmPassword" id="confirmPassword" maxlength="20"/>
                            </div>
                            <a href="./" class="btn btn-default">Cancel</a>
                            <button type ="button" class="btn btn-primary" id="changePassword">Change Password</button>
                        </form>
                    </div>
        </div>
    </body>
</html>
