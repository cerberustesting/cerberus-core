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

    String step;
    if (request.getParameter("Step") != null
            && request.getParameter("Step").compareTo("All") != 0) {
        step = request.getParameter("Step");
    } else {
        step = new String("%%");
    }

    Connection conn = db.connect();
    IDocumentationService docService = appContext.getBean(IDocumentationService.class);

    String optstyle;
    Statement stQueryTestCase = conn.createStatement();
    Statement stQueryTestCaseStep = conn.createStatement();

%>
<tr>
    <td class="wob"><span style="font-weight: bold"><%out.print(docService.findLabelHTML("testcase", "testcase", "OriginTestCase"));%></span>
        <select id="fromTestCase" name="FromTestCase" onchange="getTestCasesForImportStep()">
            <%
                if (test.compareTo("%%") == 0) {
            %><option value="All">-- Choose Test First --</option><%                    } else {
                String sql = "SELECT TestCase, Application,  Description, tcactive FROM testcase where TestCase IS NOT NULL and test like '" + test + "'Order by TestCase asc";
                ResultSet rsTestCase = stQueryTestCase.executeQuery(sql);
                while (rsTestCase.next()) {
                    if (rsTestCase.getString("tcactive").equalsIgnoreCase("Y")) {
                        optstyle = "font-weight:bold;";
                    } else {
                        optstyle = "font-weight:lighter;";
                    }
            %><option style="<%=optstyle%>" value="<%=rsTestCase.getString("TestCase")%>" <%=testcase.compareTo(rsTestCase.getString("TestCase")) == 0 ? " SELECTED " : ""%>><%=rsTestCase.getString("TestCase")%>  [<%=rsTestCase.getString("Application")%>]  : <%=rsTestCase.getString("Description")%></option><%
                    }
                }
            %>
        </select>
</tr>
<tr>
    <td  class="wob">
        <select id="fromStep" name="FromStep">
            <%
                if (testcase.compareTo("%%") == 0) {
            %><option value="All">-- Choose Test Case First --</option><%                    } else {

                String sql = "SELECT Step, Description FROM testcasestep WHERE Test like '" + test + "' and TestCase like '" + testcase + "' Order by Step asc";
                //String sql = "SELECT TestCase, Application,  Description, tcactive FROM testcase where TestCase IS NOT NULL and test like '" + test + "'Order by TestCase asc";

                ResultSet rsTestCaseStep = stQueryTestCaseStep.executeQuery(sql);
                while (rsTestCaseStep.next()) {
            %><option value="<%=rsTestCaseStep.getString("Step")%>" <%=step.compareTo(rsTestCaseStep.getString("Step")) == 0 ? " SELECTED " : ""%>>[<%=rsTestCaseStep.getString("Step")%>] <%=rsTestCaseStep.getString("Description")%></option><%
                    }
                }
            %>
        </select>
    </td>
</tr>
<tr>
    <td  class="wob">
        import Properties <input id="ImportProperty" name="ImportProperty" type="checkbox" value="Y">
    </td>
</tr>

