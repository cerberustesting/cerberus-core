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
        <script type="text/javascript" src="js/jquery-1.9.1.min.js"></script>
        <link rel="stylesheet" type="text/css" href="css/crb_style.css">
        <link rel="shortcut icon" type="image/x-icon" href="images/favicon.ico" />
        <title>Change Password</title>
        <script type="text/javascript" src="js/pages/ChangePassword.js"></script>
    </head>
    <body>
        <%@ include file="include/function.jsp" %>
        <%@ include file="include/header.jsp" %>
        <div style="width: 100%; font: 90% sans-serif">
            <table style="alignment-adjust: central"><tr><td style="alignment-adjust: central">
            <form id="changePasswordForm" title="Change Password" method="post">
                <h1>Hello <%=request.getUserPrincipal().getName() %>, you are requested to change your password.</h1>
                <input type="hidden" name="login" id="login" value="<%=request.getUserPrincipal().getName() %>">
                <br /><br />
                <label for="currentPassword">Current Password</label>
                <input type="password" name="currentPassword" id="currentPassword" maxlength="20" rel="0" />
                <br /><br />
                <label for="newPassword">New Password</label>
                <input type="password" name="newPassword" id="newPassword" maxlength="20" rel="1" />
                <br /><br />
                <label for="confirmPassword">Confirm New Password</label>
                <input type="password" name="confirmPassword" id="confirmPassword" maxlength="20" rel="2" />
                <br /><br />
                <!--<input id="submit" type="submit" value="Submit" />-->
                <button type ="button" id="changePassword">Change Password</button>
            </form>
                    </td></tr></table>
        </div>
    </body>
</html>
