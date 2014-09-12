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
<%@page import="java.net.URLEncoder"%>
<%@page import="java.net.URI"%>
<%@page import="java.sql.ResultSet"%>
<%@page import="java.sql.Statement"%>
<%@page import="java.sql.Connection"%>
<%@page import="java.util.Enumeration"%>
<%@page import="java.util.Date"%>
<%@page import="java.util.Collections"%>
<%@page import="java.util.HashSet"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Set"%>
<%@page import="java.util.HashMap"%>
<%@page import="org.springframework.web.context.support.WebApplicationContextUtils"%>
<%@page import="org.springframework.context.ApplicationContext"%>
<%@page import="org.apache.log4j.Level"%>
<%@page import="org.apache.commons.lang3.StringUtils"%>
<%@page import="org.cerberus.database.DatabaseSpring"%>
<%@page import="org.cerberus.entity.Invariant"%>
<%@page import="org.cerberus.entity.TestCaseCountry"%>
<%@page import="org.cerberus.entity.Project"%>
<%@page import="org.cerberus.entity.Test"%>
<%@page import="org.cerberus.entity.BuildRevisionInvariant"%>
<%@page import="org.cerberus.service.IInvariantService"%>
<%@page import="org.cerberus.service.IProjectService"%>
<%@page import="org.cerberus.service.ITestService"%>
<%@page import="org.cerberus.service.ITestCaseService"%>
<%@page import="org.cerberus.service.ITestCaseCountryService"%>
<%@page import="org.cerberus.service.IDocumentationService"%>
<%@page import="org.cerberus.service.impl.BuildRevisionInvariantService"%>
<%@page import="org.cerberus.service.IBuildRevisionInvariantService"%>
<%@page import="org.cerberus.service.impl.InvariantService"%>
<%@page import="org.cerberus.service.IApplicationService"%>
<%@page import="org.cerberus.log.MyLogger"%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ include file="include/function.jsp" %>
<%
    String tcclauses = " WHERE 1=1 ";
    String execclauses = " 1=1 ";

    String[] tcParameterNames = {
                "Test",
                "Project",
                "System",
                "Application",
                "TcActive",
                "Priority",
                "Status",
                "Group",
                "TargetBuild",
                "TargetRev",
                "Creator",
                "Implementer"
    };

    String[] tcColumnNames = {
                "Test",
                "Project",
                "a.System",
                "tc.Application",
                "TcActive",
                "Priority",
                "Status",
                "`Group`",
                "TargetBuild",
                "TargetRev",
                "Creator",
                "Implementer"
    };

    String[] execParameterNames = {
                "Environment",
                "SystemExe",
                "Build",
                "Revision",
                "Ip",
                "Port",
                "Tag",
                "Browser",
                "BrowserFullVersion",
                "ExeStatus",
                "Country"};

    String[] execColumnNames = {
                "tce.Environment",
                "tcev.`System`",
                "tce.Build",
                "tcev.Revision",
                "tce.Ip",
                "tce.Port",
                "tce.Tag",
                "tce.browser",
                "tce.BrowserFullVersion",
                "Status",
                "Country"};


    tcclauses += generateWhereClausesForParametersAndColumns(tcParameterNames,tcColumnNames, request);
    execclauses += generateWhereClausesForParametersAndColumns(execParameterNames,execColumnNames, request);


    if (request.getParameter("logpath") != null && request.getParameter("logpath").compareTo("") != 0) {
        execclauses = execclauses + " AND tce.logpath = '" + request.getParameter("logpath") + "'";
    }

    if (request.getParameter("ReadOnly") != null && request.getParameter("ReadOnly").compareTo("A") != 0) {
        tcclauses = tcclauses + " AND ReadOnly = '" + request.getParameter("ReadOnly") + "'";
    }

    String[] columnBrowser = null;
    if (request.getParameter("Browser") != null) {
        columnBrowser = request.getParameterValues("Browser");
    } else {
        columnBrowser = new String[0];
    }

    Connection conn = db.connect();
    IDocumentationService docService = appContext.getBean(IDocumentationService.class);

    try {
%>
<!--
    tcclauses : <%=tcclauses%>
    execclauses : <%=execclauses%>
-->
    <br><br>
    <form id="AddTestBatteryContent" action="./AddTestBatteryContent"></form>
    <button type="submit" id="submit" name="submit">Add Test Cases</button>
    <table class="backDiv" style="width:100%">
        <tr>
            <td id="wob">
                <table id="reportingExec" class="arrondTable dataTable" style="text-align: left;border-collapse:collapse;display:table" border="1px" cellpadding="0" cellspacing="1">
                    <thead>
                    <tr id="headerFirst"  class="filters">
                        <td ><input type="checkbox" name="checkAllTestCases" id="checkAllTestCases" onclick="selectAll();"><label for="checkAllTestCases">All</label></td>
                        <td ><%out.print(docService.findLabelHTML("test", "test", "Test"));%></td>
                        <td ><%out.print(docService.findLabelHTML("testcase", "testcase", "TestCase"));%></td>
                        <td ><%out.print(docService.findLabelHTML("application", "application", "Aplication"));%></td>
                        <td ><%out.print(docService.findLabelHTML("testcase", "description", "Description"));%></td>
                        <td ><%out.print(docService.findLabelHTML("testcase", "BehaviorOrValueExpected", "Detailed Description"));%></td>
                    </tr>
                    </thead>
                    <tbody>
                    <%
                        Statement stmt2 = conn.createStatement();
                        String query = "select tc.test, tc.testcase, "
                                + " tc.application, tc.description, tc.behaviororvalueexpected"
                                + " from testcase tc "
                                + " join application a on a.application = tc.application "
                                + tcclauses
                                + " and tc.group != 'PRIVATE' "
                                + " order by tc.test, tc.testcase ";
                        ResultSet rs_time = stmt2.executeQuery(query);

                        int indexColor = 0;
                        if (rs_time != null && rs_time.first()) {
                            do {
                                indexColor = indexColor + 1;
                                %>
                        <tr class="testCaseExecutionResult <%=(indexColor%2 == 1) ? "even" : "odd"%>">
                            <td ><input class="selecttestcase" type="checkbox" name="testcaseselected" value="<%="Test="+URLEncoder.encode(rs_time.getString("tc.test"), "UTF-8")+"&TestCase="+URLEncoder.encode(rs_time.getString("tc.testcase"), "UTF-8") %>"></td>
                            <td ><%=rs_time.getString("tc.test")%></td>
                            <td ><a href="TestCase.jsp?Load=Load&Test=<%=rs_time.getString("tc.test")%>&TestCase=<%=rs_time.getString("tc.testcase")%>"> <%=rs_time.getString("tc.testcase")%></a></td>
                            <td ><%=rs_time.getString("tc.application")%></td>
                            <td ><%=rs_time.getString("tc.description")%></td>
                            <td ><%=rs_time.getString("tc.behaviororvalueexpected")%></td>
                                <% 
                            } while (rs_time.next());
                        }
                    %>
                    </tbody>
                </table>
            </td>
        </tr>
    </table>
    <button type="submit" id="submit" name="submit">Add Test Cases</button>
    <script>
        var isSelectedAll = false;
        function selectAll(){
            isSelectedAll = !isSelectedAll;
            $('input.selecttestcase').prop('checked',isSelectedAll);
        }
        
        // prepare all forms for ajax submission
        $('#AddTestBatteryContent').on('submit', function(e) {
            $('#displayResult').html('<img src="./images/loading.gif"> loading...');
            e.preventDefault(); // <-- important
            $(this).ajaxSubmit({
                target: '#displayResult'
            });
        });

    </script>
    <%
            stmt2.close();
        } catch (Exception e) {
            out.println(e);
        } finally {
            try {
                conn.close();
            } catch (Exception ex) {
                out.println(ex);
            }
        }

    %>
