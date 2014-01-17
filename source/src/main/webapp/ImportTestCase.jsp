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
<%@page import="org.cerberus.entity.BuildRevisionInvariant"%>
<%@page import="org.cerberus.service.impl.BuildRevisionInvariantService"%>
<%@page import="org.cerberus.service.IBuildRevisionInvariantService"%>
<%@page import="org.cerberus.service.IApplicationService"%>
<%@page import="org.apache.commons.lang3.StringUtils"%>
<%@page import="java.sql.Connection"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>
<%@page import="java.sql.ResultSet"%>
<%@page import="java.sql.Statement"%>
<%@page import="java.net.URLEncoder"%>

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">
<%--
    Document   : ImportTestCase
    Created on : 17 janv. 2014, 14:44:47
    Author     : flesur
--%>
<%@ include file="include/function.jsp" %>
<%
    String test = request.getParameter("Test");
    String testcase;
    if (request.getParameter("TestCase") != null
            && request.getParameter("TestCase").compareTo("All") != 0) {
        testcase = request.getParameter("TestCase");
    } else {
        testcase = new String("%%");
    }

    Connection conn = db.connect();
    
    String optstyle;
    Statement stQueryTestCase = conn.createStatement();

%>
<td class="wob" style="width: 70px; font-weight: bold;"><%out.print(dbDocS(conn, "testcase", "testcase", "OriginTestCase"));%></td>
<td  class="wob">
    <select id="fromTestCase" name="FromTestCase" style="width: 500px">
        <%
            if (test.compareTo("%%") == 0) {
                %><option style="width: 200px" value="All">-- Choose Test First --</option><%
            } else {
                String sql = "SELECT TestCase, Application,  Description, tcactive FROM testcase where TestCase IS NOT NULL and test like '" + test + "'Order by TestCase asc";
                ResultSet rsTestCase = stQueryTestCase.executeQuery(sql);
                while (rsTestCase.next()) {
                    if (rsTestCase.getString("tcactive").equalsIgnoreCase("Y")) {
                        optstyle = "font-weight:bold;";
                    } else {
                        optstyle = "font-weight:lighter;";
                    }
                    %><option style="width: 500px;<%=optstyle%>" value="<%=rsTestCase.getString("TestCase")%>" <%=testcase.compareTo(rsTestCase.getString("TestCase")) == 0 ? " SELECTED " : ""%>><%=rsTestCase.getString("TestCase")%>  [<%=rsTestCase.getString("Application")%>]  : <%=rsTestCase.getString("Description")%></option><%
                }
            }
        %>
    </select>
</td>
<td  class="wob">Step <input id="fromStep" type="text" name="FromStep" value="1"></td>
