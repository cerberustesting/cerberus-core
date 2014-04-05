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
<%@page import="org.cerberus.service.IDocumentationService"%>
<% Date DatePageStart = new Date() ; %>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
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
<%
    IDocumentationService docService = appContext.getBean(IDocumentationService.class);
%>

            <form method="post" name="CreateTest" action="CreateTest"> 
                <table  id ="arrond" style="text-align: left; display:table" border="0" cellpadding="2" cellspacing="2" >
                    <tr>
                        <td class="wob">
                            <h3 style="color: blue">Test Parameters</h3>
                            <table>
                                <tr>
                                    <td class="wob" style="font-weight: bold; width: 110px"><%out.print(docService.findLabelHTML("test", "test", "Test"));%></td>
                                    <td class="wob"><input style="font-weight: bold; width: 200px" name="createTest" id="createTest"</td>
                                </tr>
                                <tr>
                                    <td id="wob" style="font-weight: bold; width: 110px"><%out.print(docService.findLabelHTML("test", "description", "Description"));%></td>
                                    <td class="wob" ><input id="createDescription" style="width: 900px" name="createDescription"></td>
                                </tr>
                                <tr>
                                    <td id="wob" style="font-weight: bold; width: 110px"><%out.print(docService.findLabelHTML("test", "active", "Active"));%></td>
                                    <td class="wob" ><select id="createActive" style="width: 40px;" name="createActive">
                                            <option value="Y">Y</option>
                                            <option value="N">N</option>
                                        </select></td>
                                </tr>
                                <tr>
                                    <td id="wob" style="font-weight: bold; width: 110px"><%out.print(docService.findLabelHTML("test", "automated", "Automated"));%></td>
                                    <td class="wob" ><select id="createAutomated" style="width: 40px;" name="createAutomated">
                                            <option value="Y">Y</option>
                                            <option value="N">N</option>
                                        </select></td>
                                </tr>
                            </table></td></tr><tr><td class="wob"><table><tr>
                                    <td class="wob"><input class="button" name="add_test" id="add_test" value="Save Test" type="submit"></td>
                                </tr></table></td></tr></table></form>

        </div>

        <br><% out.print(display_footer(DatePageStart));%>
    </body>
</html>
