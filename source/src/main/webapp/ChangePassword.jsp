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
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link rel="stylesheet" type="text/css" href="css/crb_style.css">
        <link rel="shortcut icon" type="image/x-icon" href="images/favicon.ico" />
        <title>Change Password</title>
        
        <script type="text/javascript">
            
        </script>
    </head>
    <body>
        <%@ include file="include/function.jsp" %>
        <%@ include file="include/header.jsp" %>
        <div>
            <form id="changePasswordForm" action="ChangeUserPassword" title="Change Password" method="post">
                <label><%=request.getUserPrincipal().getName() %></label>
                <input type="hidden" name="login" id="login" value="<%=request.getUserPrincipal().getName() %>">
                <br /><br />
                <label for="currentPassword">Current Password</label>
                <input type="password" name="currentPassword" id="currentPassword" maxlength="20" rel="0" />
                <br /><br />
                <label for="newPassword">New Password</label>
                <input type="password" name="newPassword" id="newPassword" maxlength="20" rel="1" />
                <br /><br />
                <label for="confirmPassword">Confirm Password</label>
                <input type="password" name="confirmPassword" id="confirmPassword" maxlength="20" rel="2" />
                <br /><br />
                <input id="submit" type="submit" value="Submit" />
            </form>
        </div>
    </body>
</html>
