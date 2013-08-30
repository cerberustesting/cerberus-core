<%-- 
    Document   : ChangePassword
    Created on : 1/Mar/2012, 15:18:32
    Author     : ip100003
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
