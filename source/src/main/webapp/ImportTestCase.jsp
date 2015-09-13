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

<%@page import="org.cerberus.entity.TestCaseStep"%>
<%@page import="org.cerberus.entity.TCase"%>
<%@page import="org.cerberus.service.ITestCaseStepService"%>
<%@page import="org.cerberus.service.ITestCaseService"%>
<%@page import="org.cerberus.service.IDocumentationService"%>
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
        testcase = "%%";
    }

    String step;
    if (request.getParameter("Step") != null
            && request.getParameter("Step").compareTo("All") != 0) {
        step = request.getParameter("Step");
    } else {
        step = "%%";
    }

    IDocumentationService docService = appContext.getBean(IDocumentationService.class);
    ITestCaseService testCaseService = appContext.getBean(ITestCaseService.class);
    ITestCaseStepService testCaseStepService = appContext.getBean(ITestCaseStepService.class);

    String optstyle;

%>
<tr>
    <td class="wob"><span style="font-weight: bold"><%out.print(docService.findLabelHTML("testcase", "testcase", "OriginTestCase", "en"));%></span>
        <select id="fromTestCase" name="FromTestCase" onchange="getTestCasesForImportStep()">
            <%
                if (test.compareTo("%%") == 0) {
            %><option value="All">-- Choose Test First --</option><%                    } else {%>
            <option value="---">-- Choose TestCase --</option>
            <%
                for (TCase tCase : testCaseService.findTestCaseByTest(test)) {
                    if (tCase.getActive().equalsIgnoreCase("Y")) {
                        optstyle = "font-weight:bold;";
                    } else {
                        optstyle = "font-weight:lighter;";
                    }
            %><option style="<%=optstyle%>" value="<%=tCase.getTestCase()%>" <%=testcase.compareTo(tCase.getTestCase()) == 0 ? " SELECTED " : ""%>><%=tCase.getTestCase()%>  [<%=tCase.getApplication()%>]  : <%=tCase.getShortDescription()%></option><%
                }
            }
            %>
        </select>
</tr>
<tr>
    <td  class="wob">
        <select id="fromStep" name="FromStep" onchange="feedDefaultDescription()">
            <%
                if (testcase.compareTo("%%") == 0) {
            %><option value="All">-- Choose Test Case First --</option><%                    } else {%>
            <option value="---">-- Choose Step --</option>
            <%
                for (TestCaseStep testCaseStep : testCaseStepService.getListOfSteps(test, testcase)) {
            %><option data-desc="<%=testCaseStep.getDescription()%>" value="<%=testCaseStep.getStep()%>" <%=step.compareTo(testCaseStep.getTest()) == 0 ? " SELECTED " : ""%>>[<%=testCaseStep.getStep()%>] <%=testCaseStep.getDescription()%></option><%
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

