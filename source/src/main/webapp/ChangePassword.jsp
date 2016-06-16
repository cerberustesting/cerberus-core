<%--
  ~ Cerberus  Copyright (C) 2013  vertigo17
  ~ DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
  ~
  ~ This file is part of Cerberus.
  ~
  ~ Cerberus is free software: you can redistribute it and/or modify
  ~ it under the terms of the GNU General Public License as published by
  ~ the Free Software Foundation, either version 3 of the License, or
  ~ (at your option) any later version.
  ~
  ~ Cerberus is distributed in the hope that it will be useful,
  ~ but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~ GNU General Public License for more details.
  ~
  ~ You should have received a copy of the GNU General Public License
  ~ along with Cerberus.  If not, see <http://www.gnu.org/licenses/>.
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%@ include file="include/dependenciesInclusions.html" %>
        <title>Change Password</title>
        <script type="text/javascript" src="js/pages/ChangePassword.js"></script>
    </head>
    <body>

        <%@ include file="include/messagesArea.html"%>
        <div style="padding-top: 7%; padding-left: 30%">
            <div id="change-password-box" class="login-box" >
                <form id="changePasswordForm" title="Change Password" method="post">
                    <H3>Password Recovery</H3>
                    <br><br>
                    Dear,<br><br> 
                    A request password request has been submitted to your account.
                    Please feed the reset password formulary to acheive the change.
                    <br><br>
                    <input type="hidden" name="login" id="login" value="<%=request.getUserPrincipal() == null ? request.getParameter("login") : request.getUserPrincipal().getName()%>">
                    <input type="hidden" name="resetPasswordToken" id="resetPasswordToken"/>

                    <div id="currentPasswordLabel" class="form-group col-xs-5" style="margin-top:10px;">
                        Current Password:
                    </div>
                    <div id="currentPasswordDiv" class="form-group col-xs-7">
                        <input class="form-login" type="password" name="currentPassword" id="currentPassword" maxlength="20"/>
                    </div>
                    <div class="form-group col-xs-5" style="margin-top:10px;">
                        New password:
                    </div>
                    <div class="form-group col-xs-7">
                        <input class="form-login" type="password" name="newPassword" id="newPassword" maxlength="20"/>
                    </div>
                    <div class="form-group col-xs-5" style="margin-top:10px;">
                        New password confirmation:
                    </div>
                    <div class="form-group col-xs-7">
                        <input class="form-login" type="password" name="confirmPassword" id="confirmPassword" maxlength="20"/>
                    </div>
                    <button type ="button" class="btn btn-primary col-xs-6" id="changePassword">Change Password</button>
                </form>
                <div class="col-xs-12">
                    <a href="./">homepage</a>
                </div>
            </div>
        </div>
    </body>
</html>
