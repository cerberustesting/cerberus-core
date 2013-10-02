<%-- 
    Document   : Test
    Created on : 10 déc. 2010, 11:15:34
    Author     : acraske
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">
<% Date DatePageStart = new Date() ; %>
<html>
    <head>
        <meta content="text/html; charset=UTF-8" http-equiv="content-type">
        <title>Test Creation</title>

        <link rel="stylesheet" type="text/css" href="css/crb_style.css">
        <link rel="shortcut icon" type="image/x-icon" href="images/favicon.ico" />
    </head>
    <body>
        <%@ include file="include/function.jsp" %>
        <%@ include file="include/header.jsp" %>
        <div id="body">

        <form method="post" name="CreateTest" action="CreateTest"> 
            <table  id ="arrond" style="text-align: left; display:table" border="0" cellpadding="2" cellspacing="2" >
                <tr>
                    <td class="wob">
                        <h3 style="color: blue">Test Parameters</h3>
                        <table>
                            <tr>
                                <td class="wob" style="font-weight: bold; width: 110px"><%out.print(dbDocS("test", "test", "Test"));%></td>
                                <td class="wob"><input style="font-weight: bold; width: 200px" name="createTest" id="createTest"</td>
                            </tr>
                            <tr>
                                <td id="wob" style="font-weight: bold; width: 110px"><%out.print(dbDocS("test", "description", "Description"));%></td>
                                <td class="wob" ><input id="createDescription" style="width: 900px" name="createDescription"></td>
                            </tr>
                            <tr>
                                <td id="wob" style="font-weight: bold; width: 110px"><%out.print(dbDocS("test", "active", "Active"));%></td>
                                <td class="wob" ><select id="createActive" style="width: 40px;" name="createActive">
                                        <option value="Y">Y</option>
                                        <option value="N">N</option>
                                    </select></td>
                            </tr>
                            <tr>
                                <td id="wob" style="font-weight: bold; width: 110px"><%out.print(dbDocS("test", "automated", "Automated"));%></td>
                                <td class="wob" ><select id="createAutomated" style="width: 40px;" name="createAutomated">
                                        <option value="Y">Y</option>
                                        <option value="N">N</option>
                                    </select></td>
                            </tr>
                        </table></td></tr><tr><td class="wob"><table><tr>
                                <td class="wob"><input class="button" name="add_test" id="add_test" value="Save Test" type="submit"></td>
                            </tr></table></td></tr></table></form>
            
        </div>

<br><% out.print(display_footer(DatePageStart)); %>
    </body>
</html>
