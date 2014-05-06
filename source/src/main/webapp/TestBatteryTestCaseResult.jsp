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
<<<<<<< HEAD
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
                IInvariantService myInvariantService = appContext.getBean(IInvariantService.class);
                List<Invariant> browserList = myInvariantService.findListOfInvariantById("BROWSER");
    
                HashMap<String, Integer> statsStatusForTest = new HashMap<String, Integer>();
                List<String> listStatus = new ArrayList<String>();

                HashMap<String, Integer> statsGroupForTest = new HashMap<String, Integer>();
                List<String> listGroup = new ArrayList<String>();

                String tcclauses = " WHERE 1=1 ";
                String execclauses = " 1=1 ";

                ITestCaseCountryService testCaseCountryService = appContext.getBean(ITestCaseCountryService.class);
                
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

                String[] country_list = null;
                if (request.getParameter("Country") != null) {
                    country_list = request.getParameterValues("Country");
                } else {
                    country_list = new String[0];
                }
                
                String[] columnBrowser = null;
                if (request.getParameter("Browser") != null) {
                    columnBrowser = request.getParameterValues("Browser");
                } else {
                    columnBrowser = new String[0];
                }

                List<String> statistiques = new ArrayList<String>();

                Connection conn = db.connect();
                IDocumentationService docService = appContext.getBean(IDocumentationService.class);

                try {
                    Statement stmt = conn.createStatement();
                    Statement stmt33 = conn.createStatement();
                    
                    IApplicationService myApplicationService = appContext.getBean(IApplicationService.class);
                    String SitdmossBugtrackingURL;
                    String SitdmossBugtrackingURL_tc;
                    SitdmossBugtrackingURL_tc = "";



            %>
            <!--
                tcclauses : <%=tcclauses%>
                execclauses : <%=execclauses%>
            -->
                <br><br>
                <table class="backDiv" style="width:100%">
                    <tr>
                        <td id="wob">
                            <table id="reportingExec" class="arrondTable dataTable" style="text-align: left;border-collapse:collapse;display:table" border="1px" cellpadding="0" cellspacing="1">
                                <thead>
                                <tr id="headerFirst"  class="filters">
                                    <td ><%out.print(docService.findLabelHTML("test", "test", "Test"));%></td>
                                    <td ><%out.print(docService.findLabelHTML("testcase", "testcase", "TestCase"));%></td>
                                    <td ><%out.print(docService.findLabelHTML("application", "application", "Aplication"));%></td>
                                    <td ><%out.print(docService.findLabelHTML("testcase", "description", "Description"));%></td>
                                    <td ><%out.print(docService.findLabelHTML("invariant", "PRIORITY", "Priority"));%></td>
                                    <td ><%out.print(docService.findLabelHTML("testcase", "status", "Status"));%></td>
                                    <%
                                        //rs_testcasecountrygeneral.first();								
                                        //do {
                                        for (int i = 0; i < country_list.length; i++) {
                                            for (int k = 0; k < columnBrowser.length; k++) {
                                    %> 
                                    <td colspan="2" class="header"> 
                                        <%=country_list[i]%>/<%=columnBrowser[k]%> </td>
                                    <!--<td  class="header" style="font-size : x-small ;">Reporting Execution</td>-->
                                    <%
                                        }}
                                        // } while (rs_testcasecountrygeneral.next());

                                    %>
                                    <td><%out.print(docService.findLabelHTML("testcase", "comment", "Comment"));%></td> 
                                    <td  ><%out.print(docService.findLabelHTML("testcase", "bugID", "BugID"));%></td>
                                    <td  ><%out.print(docService.findLabelHTML( "invariant", "GROUP", "Group"));%></td>
                                </tr>
                                </thead>
                                <tbody>
                                <%
                                    // out.println(tcclauses);
                                    // out.println(avgclauses);
                                    //out.println(execclauses);
                                    int j = 0;
                                    String stats = "";
                                    String testlist = "";
                                    String countrylist = "";
                                    String statuslist = "";
                                    Statement stmt2 = conn.createStatement();
                                    String query = "select tc.test, tc.testcase, "
                                            + " tc.application, tc.description, tc.behaviororvalueexpected, tc.Status, "
                                            + " tc.priority, tc.comment, tc.bugID, tc.TargetBuild, tc.TargetRev, tc.Group "
                                            + " from testcase tc "
                                            + " join application a on a.application = tc.application "
                                            + tcclauses
                                            + " and tc.group != 'PRIVATE' "
                                            + " order by tc.test, tc.testcase ";
                                    ResultSet rs_time = stmt2.executeQuery(query);

                                    Statement stmt8 = conn.createStatement();
                                    ResultSet rs_test = stmt8.executeQuery("select distinct test "
                                            + " from testcase tc "
                                            + " join application a on a.application = tc.application "
                                            + tcclauses
                                            + " and tc.group != 'PRIVATE' "
                                            + " order by test ");
                                    
                                    
                                    if (rs_time.first()) {
                                        int indexColor = 0;
                                        //if (StringUtils.isNotBlank(rs_time.getString("tc.test")) == true){ 
                                        if (rs_test.first())
                                            do {
                                                indexColor = indexColor + 1;
                                                if (!rs_test.getString("test").equals(rs_time.getString("tc.test"))) {%>
                                <tr id="header">
                                    <td colspan="6"></td>
                                    <%
                                        for (int i = 0; i < country_list.length; i++) {
                                            for (int k = 0; k < columnBrowser.length; k++) {
                                    %> 
                                    <td colspan="2" style="text-align: center"> 
                                        <%=country_list[i]%>/<%=columnBrowser[k]%> </td>
                                        <%}}%>
                                    <td colspan="3"></td>
                                </tr>

                                <%
                                        j = 0;
                                        rs_test.next();
                                    }
                                    if (j == 12) {%>
                                <tr style="font-size : x-small ;" class="reportDelimiter">
                                    <td colspan="6" ></td>
                                    <%
                                        for (int i = 0; i < country_list.length; i++) {
                                            for (int k = 0; k < columnBrowser.length; k++) {
                                    %> 
                                    <td colspan="2" style="text-align: center"> 
                                        <%=country_list[i]%>/<%=columnBrowser[k]%> </td>

                                    <%}}%>
                                    <td colspan="3" ></td>
                                </tr>

                                <%
                                        j = 0;
                                    }
                                %>
                                <tr class="testCaseExecutionResult <%=(indexColor%2 == 1) ? "even" : "odd"%>">
                                    <td ><%=rs_time.getString("tc.test")%></td>
                                    <td ><a href="TestCase.jsp?Load=Load&Test=<%=rs_time.getString("tc.test")%>&TestCase=<%=rs_time.getString("tc.testcase")%>"> <%=rs_time.getString("tc.testcase")%></a></td>
                                    <td ><%=rs_time.getString("tc.application")%></td>
                                    <td ><%=rs_time.getString("tc.description")%></td>
                                    <td ><%=rs_time.getString("tc.Priority")%></td>
                                    <td ><%=rs_time.getString("tc.Status")%></td>
                                    <%
                                        // Collecting status stats for current test. 
                                        if (!listStatus.contains(rs_time.getString("tc.Status"))) {
                                            listStatus.add(rs_time.getString("tc.Status"));
                                        }
                                        if (statsStatusForTest.containsKey(rs_time.getString("tc.test") + rs_time.getString("tc.Status"))) {
                                            statsStatusForTest.put(rs_time.getString("tc.test") + rs_time.getString("tc.Status"), statsStatusForTest.get(rs_time.getString("tc.test") + rs_time.getString("tc.Status")) + 1);
                                        } else {
                                            statsStatusForTest.put(rs_time.getString("tc.test") + rs_time.getString("tc.Status"), 1);
                                        }
                                        if (statsStatusForTest.containsKey("TOTAL" + rs_time.getString("tc.Status"))) {
                                            statsStatusForTest.put("TOTAL" + rs_time.getString("tc.Status"), statsStatusForTest.get("TOTAL" + rs_time.getString("tc.Status")) + 1);
                                        } else {
                                            statsStatusForTest.put("TOTAL" + rs_time.getString("tc.Status"), 1);
                                        }

                                        // Collecting group stats for current test. 
                                        if (!listGroup.contains(rs_time.getString("tc.Group"))) {
                                            listGroup.add(rs_time.getString("tc.Group"));
                                        }
                                        if (statsGroupForTest.containsKey(rs_time.getString("tc.test") + rs_time.getString("tc.Group"))) {
                                            statsGroupForTest.put(rs_time.getString("tc.test") + rs_time.getString("tc.Group"), statsGroupForTest.get(rs_time.getString("tc.test") + rs_time.getString("tc.Group")) + 1);
                                        } else {
                                            statsGroupForTest.put(rs_time.getString("tc.test") + rs_time.getString("tc.Group"), 1);
                                        }
                                        if (statsGroupForTest.containsKey("TOTAL" + rs_time.getString("tc.Group"))) {
                                            statsGroupForTest.put("TOTAL" + rs_time.getString("tc.Group"), statsGroupForTest.get("TOTAL" + rs_time.getString("tc.Group")) + 1);
                                        } else {
                                            statsGroupForTest.put("TOTAL" + rs_time.getString("tc.Group"), 1);
                                        }

                                    // do{
                                    for (int i = 0; i < country_list.length; i++) {
                                %><td class="NOINF"></td><td class="NOINF"></td><%                                            }
                                %>
                                <td ><%
                                    if (rs_time.getString("tc.Comment") != null) {%><%=rs_time.getString("tc.Comment")%><%}%></td>
                                <td ><%
                                    if ((rs_time.getString("tc.BugID") != null)
                                            && (rs_time.getString("tc.BugID").compareToIgnoreCase("") != 0)
                                            && (rs_time.getString("tc.BugID").compareToIgnoreCase("null") != 0)) {
                                        SitdmossBugtrackingURL = myApplicationService.findApplicationByKey(rs_time.getString("application")).getBugTrackerUrl();
                                        SitdmossBugtrackingURL_tc = SitdmossBugtrackingURL.replaceAll("%BUGID%", rs_time.getString("tc.BugID"));
                                    } else {
                                        SitdmossBugtrackingURL_tc = "";
                                    }
                                    if (SitdmossBugtrackingURL_tc.equalsIgnoreCase("") == false) {%><a href="<%=SitdmossBugtrackingURL_tc%>" target="_blank"><%=rs_time.getString("tc.BugID")%></a><%
                                        }
                                        if ((rs_time.getString("tc.TargetBuild") != null) && (rs_time.getString("tc.TargetBuild").equalsIgnoreCase("") == false)) {
                                    %> for <%=rs_time.getString("tc.TargetBuild")%>/<%=rs_time.getString("tc.TargetRev")%><%
                                        }%></td>
                                <td ><%
                                    if (rs_time.getString("tc.Group") != null) {%><%=rs_time.getString("tc.Group")%><%}%></td>


                                <% 
                                                j++;
//                                                stmt4.close();
                                            } while (rs_time.next());
                                    }
                                %>
                                </tbody>
                            </table>
                        </td>
                    </tr>
                </table>
                <%
                            stmt.close();
                            stmt33.close();
                            stmt2.close();
                            stmt8.close();


                    } catch (Exception e) {
                        out.println(e);
                    } finally {
                        try {
                            conn.close();
                        } catch (Exception ex) {
                        }
                    }

                %>



=======
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
        function selectAll(){
            $('input.selecttestcase').prop('checked',!$('input.selecttestcase').prop('checked'));
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
>>>>>>> origin/master
