<%@page import="java.util.Enumeration"%>
<%@page import="java.sql.ResultSet"%>
<%@page import="org.cerberus.entity.Invariant"%>
<%@page import="java.sql.Statement"%>
<%@page import="java.sql.Connection"%>
<%@page import="java.util.Date"%>
<%@page import="org.cerberus.service.IInvariantService"%>
<%@page import="org.springframework.web.context.support.WebApplicationContextUtils"%>
<%@page import="org.springframework.context.ApplicationContext"%>
<%@page import="org.cerberus.database.DatabaseSpring"%>
<%@page import="org.cerberus.entity.TestCaseCountry"%>
<%@page import="org.cerberus.entity.Project"%>
<%@page import="org.cerberus.service.IProjectService"%>
<%@page import="org.cerberus.entity.Test"%>
<%@page import="org.cerberus.service.ITestService"%>
<%@page import="org.cerberus.service.ITestCaseService"%>
<%@page import="org.cerberus.service.ITestCaseCountryService"%>
<%@page import="org.cerberus.service.IDocumentationService"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
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
<%@page import="org.cerberus.log.MyLogger"%>
<%@page import="org.apache.log4j.Level"%>
<%@page import="org.cerberus.service.impl.BuildRevisionInvariantService"%>
<%@page import="org.cerberus.service.IBuildRevisionInvariantService"%>
<%@page import="org.cerberus.entity.BuildRevisionInvariant"%>
<%@page import="org.cerberus.service.impl.InvariantService"%>
<%@page import="org.cerberus.service.IApplicationService"%>
<%@page import="org.apache.commons.lang3.StringUtils"%>
<%@page import="java.util.Collections"%>
<%@page import="java.util.HashSet"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Set"%>
<%@page import="java.util.HashMap"%>
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
                    boolean recordPref;
                    if (request.getParameter("RecordPref") != null
                            && request.getParameter("RecordPref").equals("Y")) {
                        recordPref = true;
                    } else {
                        recordPref = false;
                    }
                
                    Statement stmt = conn.createStatement();
                    Statement stmt33 = conn.createStatement();
                    
                    IApplicationService myApplicationService = appContext.getBean(IApplicationService.class);
                    String SitdmossBugtrackingURL;
                    String SitdmossBugtrackingURL_tc;
                    SitdmossBugtrackingURL_tc = "";

                    Statement stmt5 = conn.createStatement();

                    String insertURL = "UPDATE user SET ReportingFavorite = 'Apply=Apply&"
                            + request.getQueryString() + "' where login = '"
                            + request.getUserPrincipal().getName()
                            + "'";
                    if (recordPref == true) {
                        stmt5.execute(insertURL);
                    }


            %>
            <!--
                tcclauses : <%=tcclauses%>
                execclauses : <%=execclauses%>
            -->
                <br><br>
                <table class="backDiv" style="width:100%">
                    <tr>
                        <td id="wob">
                            <table>
                                <tr>
                                    <td id="wob">
                                        <input id="ShowS" type="button" value="Show Summary" onclick="javascript:setInvisibleRep();" style="display:table">
                                        <input id="ShowD" type="button" value="Show Details" onclick="javascript:setVisibleRep();" style="display:none">
                                    </td>
                                    <td id="wob">Legend : </td>
                                    <td id="wob" class="FILTER" title="FILTER : Use this checkbox to filter status."><input type="checkbox" name="FILTER" class="filterDisplay" value="FILTER" onchange="filterDisplay($(this).is(':checked'))"><label title="FILTER">FILTER</label></td>
                                    <td id="wob" class="OK" title="OK : Test was fully executed and no bug are to be reported."><input type="checkbox" id="FOK" name="OK" value="OK" class="filterCheckbox" disabled="disabled" onchange="toogleDisplay(this)"><label class="OKF" title="OK">OK</label></td>
                                    <td id="wob" class="KO" title="KO : Test was executed and bug have been detected."><input type="checkbox" name="KO" id="FKO" value="KO" class="filterCheckbox" disabled="disabled" onchange="toogleDisplay(this)"><label  class="KOF" title="KO">KO</label></td>
                                    <td id="wob" class="NA" title="NA : Test could not be executed because some test data are not available."><input type="checkbox" id="FNA" class="filterCheckbox" disabled="disabled" name="NA" value="NA" onchange="toogleDisplay(this)"><label  title="NA" class="NAF">NA</label></td>
                                    <td id="wob" class="FA" title="FA : Test could not be executed because there is a bug on the test."><input type="checkbox" name="FA"  id="FFA" class="filterCheckbox" disabled="disabled" value="FA" onchange="toogleDisplay(this)"><label  class="FAF">FA</label></td>
                                    <td id="wob" class="PE" title="PE : Test execution is still running..."><input type="checkbox" name="PE" value="PE" class="filterCheckbox" id="FPE" disabled="disabled" onchange="toogleDisplay(this)"><label class="PEF">PE</label></td>
                                    <td id="wob" class="NotExecuted" title="Test Case has not been executed for that country."><a class="NotExecutedF">XX</a></td>
                                    <td id="wob" class="NOINF" title="Test Case not available for the country XX."><a class="NOINFF">XX</a></td>
                                    <td id="wob" ><a href="./ReportingExecution.jsp?Apply=Apply&<%=request.getQueryString() %>">URL for quick access</a></td>
                                </tr>
                            </table>
                        </td>
                    </tr>    
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
                                    <td  class="header"> 
                                        <%=country_list[i]%>/<%=columnBrowser[k]%> </td>
                                    <td  class="header" style="font-size : x-small ;">Reporting Execution</td>
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

                                        String cssStatus = "";
                                        String color = "black";
                                        List<TestCaseCountry> tccList = testCaseCountryService.findTestCaseCountryByTestTestCase(rs_time.getString("tc.Test"), rs_time.getString("tc.testcase"));

                                        Integer tccIncrement = 0;
                                        Integer brIncrement = 0;
                                        if (!tccList.isEmpty()) {
                                            cssStatus = "NE";
                                            color = "black";

                                            for (int i = 0; i < country_list.length; i++) {
                                                 for (int k = 0; k < columnBrowser.length; k++) {

                                                tccIncrement = 0;
                                                for(TestCaseCountry tcc : tccList){
                                                tccIncrement++;
                                                if (tcc.getCountry().equalsIgnoreCase(country_list[i])) {
                                                        break;
                                                    }
                                                }
                                                
                                                brIncrement = 0;
                                                for(Invariant inv : browserList){
                                                brIncrement++;
                                                if (inv.getValue().equalsIgnoreCase(columnBrowser[k])) {
                                                        break;
                                                    }
                                                }
                                                

                                        if (country_list[i].equals(tccList.get(tccIncrement-1).getCountry())
                                                && columnBrowser[k].equals(browserList.get(brIncrement-1).getValue())) {
                                                    Statement stmt3 = conn.createStatement();
                                                    String stmt3SQL = "SELECT DISTINCT tce.ID, tce.test, tce.testcase, tce.application, "
                                                            + "tce.ControlStatus, DATE_FORMAT(tce.Start,'%Y-%m-%d %H:%i') as Start, DATE_FORMAT(tce.End,'%Y-%m-%d %H:%i') as End "
                                                            + " from testcaseexecution tce "
                                                            + "JOIN testcaseexecutionsysver tcev "
                                                            + " ON tcev.id = tce.id "
                                                            + "WHERE "
                                                            + execclauses
                                                            + " and tce.test = '"
                                                            + rs_time.getString("tc.Test")
                                                            + "' and tce.testcase = '"
                                                            + rs_time.getString("tc.testcase")
                                                            + "' and tce.country = '"
                                                            + country_list[i]
                                                            + "' and tce.browser = '"
                                                            + columnBrowser[k]
                                                            + "' order by tce.ID desc LIMIT 1";
                                                    MyLogger.log("ReportingExecution.jsp", Level.DEBUG, stmt3SQL);

                                                    ResultSet rs_exec = stmt3.executeQuery(stmt3SQL);
                                                    if (rs_exec.first()) {
                                                        if (StringUtils.isNotBlank(rs_exec.getString("ID"))) {
                                                            cssStatus = "NotExecuted";
                                                            color = "black";
                                                            if (rs_exec.getString("ControlStatus").equals("OK")) {
                                                                cssStatus = "OK";
                                                                color = "green";
                                                            } else if (rs_exec.getString("ControlStatus").equals("KO")) {
                                                                cssStatus = "KO";
                                                                color = "darkred";
                                                            } else if (rs_exec.getString("ControlStatus").equals("NA")) {
                                                                cssStatus = "NA";
                                                                color = "darkyellow";
                                                            } else if (rs_exec.getString("ControlStatus").equals("FA")) {
                                                                cssStatus = "FA";
                                                                color = "darkmagenta";
                                                            } else if (rs_exec.getString("ControlStatus").equals("PE")) {
                                                                cssStatus = "PE";
                                                                color = "darkblue";
                                                            }

                                                            testlist = rs_time.getString("tc.test");
                                                            countrylist = country_list[i];
                                                            statuslist = rs_exec.getString("ControlStatus");
                                                            stats = testlist + "-" + countrylist + "-" + statuslist;
                                                            statistiques.add(i, stats);
                                    %> 
                                    <td class="<%=cssStatus%>"> 
                                        <a href="ExecutionDetail.jsp?id_tc=<%=rs_exec.getString("ID")%>" class="<%=cssStatus%>F"><%=rs_exec.getString("ControlStatus")%></a>
                                    </td>
                                    <td  style="font-size : x-small"> <%=rs_exec.getString("Start")%></td>
                                    <%
                                            stmt3.close();
                                        }
                                    } else {
                                        statistiques.add(i, rs_time.getString("tc.test") + "-" + country_list[i] + "-" + "NE");
                                        cssStatus = "NotExecuted";%>
                                    <td class="<%=cssStatus%>"><a href="RunTests.jsp?Test=<%=rs_time.getString("tc.test")%>&TestCase=<%=rs_time.getString("tc.testcase")%>&Country=<%=country_list[i]%>" class="<%=cssStatus%>F"><%= country_list[i]%></a></td>
                                    <td ></td>
                                    <%    }
                                        if (tccIncrement==tccList.size()-1){
                                                //rs_count.isLast() == true) {
                                        } else {
                                            tccIncrement++;
                                            //rs_count.next();
                                        }
                                    } else {
                                        // if (rs_count.getString("Country").compareTo(country_list[i]) > 0){
                                        //    if(rs_count.isLast()) {} else { rs_count.next(); }

                                        statistiques.add(i, rs_time.getString("tc.test") + "-" + country_list[i] + "-" + "NT");

                                    %>
                                    <td class="NOINF"></td><td class="NOINF"></td>
                                    <%   } }   
                                        }
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

                                </tr>

                                <%
                                } else {
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


                                <%    }
                                                j++;
//                                                stmt4.close();
                                            } while (rs_time.next());
                                    }
                                %>
                                </tbody>
                            </table>
                            <table id="execReporting" style="display: none" border="0px" cellpadding="0" cellspacing="0">
                                <tr id="header">
                                    <td></td>
                                    <%
                                        for (int i = 0; i < country_list.length; i++) {
                                    %>
                                    <td colspan="6" align="center" style="width: 60px ;"><%=country_list[i]%></td>
                                    <%
                                        }
                                    %>
                                </tr>
                                <tr id="header">
                                    <td>Tests</td>
                                    <%
                                        for (int i = 0; i < country_list.length; i++) {
                                    %>
                                    <td id="repsynthesis1" align="center" style="color : green">OK</td>
                                    <td id="repsynthesis2" align="center" style="color : red">KO</td>
                                    <td id="repsynthesis3" align="center" style="color : darkmagenta">FA</td>
                                    <td id="repsynthesis4" align="center" style="color : blue">PE</td>
                                    <td id="repsynthesis5" align="center" style="color : #999999">NE</td>
                                    <td id="repsynthesis6" align="center" style="color : black">TOTAL</td>
                                    <%                                                              }
                                    %>
                                </tr>
                                <%
                                    String[] statsdetails = {"", ""};
                                    for (int k = 0; k < country_list.length; k++) {
                                        String OK = "OK" + country_list[k];
                                        String KO = "KO" + country_list[k];
                                        String FA = "FA" + country_list[k];
                                        String PE = "PE" + country_list[k];
                                        String NE = "NE" + country_list[k];
                                        String NT = "NT" + country_list[k];
                                    }

                                    List<String> listtest = new ArrayList<String>();




                                    for (int i = 0; i < statistiques.size(); i++) {
                                        statsdetails = statistiques.get(i).split("-");
                                        listtest.add(statsdetails[0]);
                                    }
                                    Set set = new HashSet();
                                    set.addAll(listtest);
                                    ArrayList distinctList = new ArrayList(set);
                                    Collections.sort(distinctList);

                                    List<String> listTOTOK = new ArrayList<String>();
                                    List<String> listTOTKO = new ArrayList<String>();
                                    List<String> listTOTFA = new ArrayList<String>();
                                    List<String> listTOTPE = new ArrayList<String>();
                                    List<String> listTOTNE = new ArrayList<String>();
                                    List<String> listTOTNT = new ArrayList<String>();

                                    for (int l = 0; l < distinctList.size(); l++) {
                                        List<String> listOK = new ArrayList<String>();
                                        List<String> listKO = new ArrayList<String>();
                                        List<String> listFA = new ArrayList<String>();
                                        List<String> listPE = new ArrayList<String>();
                                        List<String> listNE = new ArrayList<String>();
                                        List<String> listNT = new ArrayList<String>();
                                        for (int i = 0; i < statistiques.size(); i++) {
                                            statsdetails = statistiques.get(i).split("-");
                                            String countrystatus = statsdetails[1] + "-" + statsdetails[2];

                                            if (distinctList.get(l).equals(statsdetails[0])) {
                                                if (statsdetails[2].equals("OK")) {
                                                    listOK.add(countrystatus);
                                                    listTOTOK.add(countrystatus);
                                                }
                                                if (statsdetails[2].equals("KO")) {
                                                    listKO.add(countrystatus);
                                                    listTOTKO.add(countrystatus);
                                                }
                                                if (statsdetails[2].equals("FA")) {
                                                    listFA.add(countrystatus);
                                                    listTOTFA.add(countrystatus);
                                                }
                                                if (statsdetails[2].equals("PE")) {
                                                    listPE.add(countrystatus);
                                                    listTOTPE.add(countrystatus);
                                                }
                                                if (statsdetails[2].equals("NE")) {
                                                    listNE.add(countrystatus);
                                                    listTOTNE.add(countrystatus);
                                                }
                                                if (statsdetails[2].equals("NT")) {
                                                    listNT.add(countrystatus);
                                                    listTOTNT.add(countrystatus);
                                                }

                                            }
                                        }

                                %>
                                <tr>
                                    <td><%=distinctList.get(l)%></td>
                                    <%
                                        for (int b = 0; b < country_list.length; b++) {
                                            List<String> listCTOK = new ArrayList<String>();
                                            List<String> listCTKO = new ArrayList<String>();
                                            List<String> listCTFA = new ArrayList<String>();
                                            List<String> listCTPE = new ArrayList<String>();
                                            List<String> listCTNE = new ArrayList<String>();
                                            List<String> listCTNT = new ArrayList<String>();

                                            for (int a = 0; a < listOK.size(); a++) {
                                                String[] CTOKdetails = listOK.get(a).split("-");
                                                if (country_list[b].equals(CTOKdetails[0])) {
                                                    if (CTOKdetails[1].equals("OK")) {
                                                        listCTOK.add(CTOKdetails[1]);
                                                    }
                                                }
                                            }

                                            for (int a = 0; a < listKO.size(); a++) {
                                                String[] CTKOdetails = listKO.get(a).split("-");
                                                if (country_list[b].equals(CTKOdetails[0])) {
                                                    if (CTKOdetails[1].equals("KO")) {
                                                        listCTKO.add(CTKOdetails[1]);
                                                    }
                                                }
                                            }

                                            for (int a = 0; a < listFA.size(); a++) {
                                                String[] CTFAdetails = listFA.get(a).split("-");
                                                if (country_list[b].equals(CTFAdetails[0])) {
                                                    if (CTFAdetails[1].equals("FA")) {
                                                        listCTFA.add(CTFAdetails[1]);
                                                    }
                                                }
                                            }

                                            for (int a = 0; a < listPE.size(); a++) {
                                                String[] CTPEdetails = listPE.get(a).split("-");
                                                if (country_list[b].equals(CTPEdetails[0])) {
                                                    if (CTPEdetails[1].equals("PE")) {
                                                        listCTPE.add(CTPEdetails[1]);
                                                    }
                                                }
                                            }

                                            for (int a = 0; a < listNE.size(); a++) {
                                                String[] CTNEdetails = listNE.get(a).split("-");
                                                if (country_list[b].equals(CTNEdetails[0])) {
                                                    if (CTNEdetails[1].equals("NE")) {
                                                        listCTNE.add(CTNEdetails[1]);
                                                    }
                                                }
                                            }

                                            for (int a = 0; a < listNT.size(); a++) {
                                                String[] CTNTdetails = listNT.get(a).split("-");
                                                if (country_list[b].equals(CTNTdetails[0])) {
                                                    if (CTNTdetails[1].equals("NT")) {
                                                        listCTNT.add(CTNTdetails[1]);
                                                    }
                                                }
                                            }
                                            String cssGen = "White";
                                            String cssleftTOP = "";
                                            String cssleftRIG = "";
                                            String cssmiddleTOP = "";
                                            String cssmiddleRIG = "";
                                            String cssmiddleLEF = "";
                                            String cssrightTOP = "";
                                            String cssrightLEF = "";

                                            if (listCTKO.size() != 0) {
                                                cssGen = "#FF0000";
                                                cssleftTOP = "#FF0000";
                                                cssleftRIG = "#FF0000";
                                                cssmiddleTOP = "FF0000";
                                                cssmiddleRIG = "FF0000";
                                                cssmiddleLEF = "FF0000";
                                                cssrightTOP = "FF0000";
                                                cssrightLEF = "FF0000";
                                            } else {
                                                if (listCTNE.size() != 0) {
                                                    cssGen = "whitesmoke";
                                                    cssleftTOP = "whitesmoke";
                                                    cssleftRIG = "whitesmoke";
                                                    cssmiddleTOP = "whitesmoke";
                                                    cssmiddleRIG = "whitesmoke";
                                                    cssmiddleLEF = "whitesmoke";
                                                    cssrightTOP = "whitesmoke";
                                                    cssrightLEF = "whitesmoke";
                                                } else {
                                                    if (listCTOK.size() != 0) {
                                                        cssGen = "#00FF00";
                                                        cssleftTOP = "#00FF00";
                                                        cssleftRIG = "#00FF00";
                                                        cssmiddleTOP = "#00FF00";
                                                        cssmiddleRIG = "#00FF00";
                                                        cssmiddleLEF = "#00FF00";
                                                        cssrightTOP = "#00FF00";
                                                        cssrightLEF = "#00FF00";
                                                    } else {
                                                        cssGen = "White";
                                                        cssleftTOP = "White";
                                                        cssleftRIG = "White";
                                                        cssmiddleTOP = "White";
                                                        cssmiddleRIG = "White";
                                                        cssmiddleLEF = "White";
                                                        cssrightTOP = "White";
                                                        cssrightLEF = "White";
                                                    }
                                                }

                                            }

                                            int total = 0 + listCTOK.size() + listCTKO.size() + listCTFA.size() + listCTPE.size() + listCTNE.size();
                                    %><td id="repsynthesis1"  align="center" style="font : bold  ;
                                        ; color: green; border-top-color: <%=cssleftTOP%> ; border-right-color: <%=cssleftRIG%>  ">
                                        <%=listCTOK.size() != 0 ? listCTOK.size() : "0"%> 
                                    <td id="repsynthesis2"  align="center" style="font : bold; 
                                        ; color: red; border-top-color: <%=cssleftTOP%> ; border-right-color: <%=cssleftRIG%> ;border-left-color: <%=cssleftRIG%>  ">
                                        <%=listCTKO.size() != 0 ? listCTKO.size() : "0"%> 
                                    <td id="repsynthesis3"  align="center" style="font : bold ; 
                                        ; color: darkmagenta; border-top-color: <%=cssleftTOP%> ; border-left-color: <%=cssleftRIG%>  ">
                                        <%=listCTFA.size() != 0 ? listCTFA.size() : "0"%></td>
                                    <td id="repsynthesis4"  align="center" style="font : bold ; 
                                        ; color: blue; border-top-color: <%=cssleftTOP%> ; border-left-color: <%=cssleftRIG%>  ">
                                        <%=listCTPE.size() != 0 ? listCTPE.size() : "0"%></td>
                                    <td id="repsynthesis5"  align="center" style="font : bold ; 
                                        ; color: #999; border-top-color: <%=cssleftTOP%> ; border-left-color: <%=cssleftRIG%>  ">
                                        <%=listCTNE.size() != 0 ? listCTNE.size() : "0"%></td>
                                    <td id="repsynthesis5"  align="center" style="font : bold ; 
                                        ; color: black; border-top-color: <%=cssleftTOP%> ; border-left-color: <%=cssleftRIG%>  ">
                                        <%=total != 0 ? total : "0"%></td>
                                        <%
                                            }
                                        %>
                                </tr>


                                <%}%>
                                <tr id="header"><td>TOTAL</td>
                                    <%

                                        for (int i = 0; i < country_list.length; i++) {
                                            List<String> listCTTOTOK = new ArrayList<String>();
                                            List<String> listCTTOTKO = new ArrayList<String>();
                                            List<String> listCTTOTFA = new ArrayList<String>();
                                            List<String> listCTTOTPE = new ArrayList<String>();
                                            List<String> listCTTOTNE = new ArrayList<String>();
                                            List<String> listCTTOTNT = new ArrayList<String>();

                                            for (int a = 0; a < listTOTOK.size(); a++) {
                                                String[] CTOKdetails = listTOTOK.get(a).split("-");
                                                if (country_list[i].equals(CTOKdetails[0])) {
                                                    if (CTOKdetails[1].equals("OK")) {
                                                        listCTTOTOK.add(CTOKdetails[1]);
                                                    }
                                                }
                                            }

                                            for (int a = 0; a < listTOTKO.size(); a++) {
                                                String[] CTKOdetails = listTOTKO.get(a).split("-");
                                                if (country_list[i].equals(CTKOdetails[0])) {
                                                    if (CTKOdetails[1].equals("KO")) {
                                                        listCTTOTKO.add(CTKOdetails[1]);
                                                    }
                                                }
                                            }

                                            for (int a = 0; a < listTOTFA.size(); a++) {
                                                String[] CTFAdetails = listTOTFA.get(a).split("-");
                                                if (country_list[i].equals(CTFAdetails[0])) {
                                                    if (CTFAdetails[1].equals("FA")) {
                                                        listCTTOTFA.add(CTFAdetails[1]);
                                                    }
                                                }
                                            }

                                            for (int a = 0; a < listTOTPE.size(); a++) {
                                                String[] CTPEdetails = listTOTPE.get(a).split("-");
                                                if (country_list[i].equals(CTPEdetails[0])) {
                                                    if (CTPEdetails[1].equals("PE")) {
                                                        listCTTOTPE.add(CTPEdetails[1]);
                                                    }
                                                }
                                            }

                                            for (int a = 0; a < listTOTNE.size(); a++) {
                                                String[] CTNEdetails = listTOTNE.get(a).split("-");
                                                if (country_list[i].equals(CTNEdetails[0])) {
                                                    if (CTNEdetails[1].equals("NE")) {
                                                        listCTTOTNE.add(CTNEdetails[1]);
                                                    }
                                                }
                                            }

                                            int total = 0 + listCTTOTOK.size() + listCTTOTKO.size() + listCTTOTFA.size() + listCTTOTPE.size() + listCTTOTNE.size();

                                    %>
                                    <td align="center" style="color : green"><%=listCTTOTOK.size()%></td>
                                    <td align="center" style="color : red"><%=listCTTOTKO.size()%></td>
                                    <td align="center" style="color : darkmagenta"><%=listCTTOTFA.size()%></td>
                                    <td align="center" style="color : blue"><%=listCTTOTPE.size()%></td>
                                    <td align="center" style="color : #999"><%=listCTTOTNE.size()%></td>
                                    <td align="center" style="color : black"><%=total%></td>
                                    <%                                                              }
                                    %>
                                </tr>
                            </table>
                            <br>
                            <table id="groupReporting" style="display: none" border="0px" cellpadding="0" cellspacing="0">
                                <tr id="header">
                                    <td>Number of test case</td>
                                    <%
                                        for (int i = 0; i < listGroup.size(); i++) {
                                    %>
                                    <td id="status<%=i%>" align="center" ><%=listGroup.get(i)%></td>
                                    <%
                                        }
                                    %>
                                    <td>TOTAL</td>
                                </tr>
                                <%

                                    for (int index = 0; index < distinctList.size(); index++) {
                                        int totalTest = 0;
                                %><tr><td><%=distinctList.get(index)%></td><%

                                    for (int i = 0; i < listGroup.size(); i++) {
                                        if (statsGroupForTest.containsKey(distinctList.get(index) + listGroup.get(i))) {
                                            totalTest += statsGroupForTest.get(distinctList.get(index) + listGroup.get(i));
                                    %><td align="center"><%=statsGroupForTest.get(distinctList.get(index) + listGroup.get(i))%></td><%
                                    } else {
                                    %><td></td><%                                                            }
                                        }

                                    %><td align="center"><%=totalTest%></td></tr><%
                                        }
                                    %>
                                <tr id="header"><td>TOTAL</td><%
                                    int totalTest = 0;
                                    for (int i = 0; i < listGroup.size(); i++) {
                                        if (statsGroupForTest.containsKey("TOTAL" + listGroup.get(i))) {
                                            totalTest += statsGroupForTest.get("TOTAL" + listGroup.get(i));
                                    %><td align="center"><%=statsGroupForTest.get("TOTAL" + listGroup.get(i))%></td><%
                                    } else {
                                    %><td></td><%                                                            }
                                        }

                                    %><td align="center"><%=totalTest%></td></tr></table>
                            <br>
                            <table id="statusReporting" style="display: none" border="0px" cellpadding="0" cellspacing="0">
                                <tr id="header">
                                    <td>Number of test case</td>
                                    <%
                                        // Loading list of Status invariant sorted in the proper way.
                                        List<Invariant> myInv = myInvariantService.findListOfInvariantById("TCSTATUS");

                                        // Display all status in the proper order.
                                        for (int i = 0; i < myInv.size(); i++) {
                                    %>
                                    <td id="status<%=i%>" align="center" ><%=myInv.get(i).getValue()%></td>
                                    <%
                                        }
                                    %>
                                    <td>TOTAL</td>
                                </tr>
                                <%

                                    for (int index = 0; index < distinctList.size(); index++) {
                                        totalTest = 0;
                                %><tr><td><%=distinctList.get(index)%></td><%

                                    for (int i = 0; i < myInv.size(); i++) {

                                        // finding the real list of status from the page data inside the current complete status
                                        int mj = 0;
                                        while ((mj < listStatus.size()) && !(listStatus.get(mj).equals(myInv.get(i).getValue()))) {
                                            mj++;
                                        }
                                        if (mj >= listStatus.size()) { // Current status was not in the test data
                                    %><td></td><%                                                    } else {
                                        if (statsStatusForTest.containsKey(distinctList.get(index) + listStatus.get(mj))) {
                                            totalTest += statsStatusForTest.get(distinctList.get(index) + listStatus.get(mj));
                                    %><td align="center"><%=statsStatusForTest.get(distinctList.get(index) + listStatus.get(mj))%></td><%
                                    } else {
                                    %><td></td><%                                                                }

                                            }
                                        }
                                    %><td align="center"><%=totalTest%></td></tr><%
                                        }
                                    %>
                                <tr id="header"><td>TOTAL</td><%
                                    totalTest = 0;
                                    for (int i = 0; i < myInv.size(); i++) {

                                        // finding the real list of status from the page data inside the current complete status
                                        int mj = 0;
                                        while ((mj < listStatus.size()) && !(listStatus.get(mj).equals(myInv.get(i).getValue()))) {
                                            mj++;
                                        }
                                        if (mj >= listStatus.size()) { // Current status was not in the test data
                                    %><td></td><%                                                    } else {
                                                                            if (statsStatusForTest.containsKey("TOTAL" + listStatus.get(mj))) {
                                                                                totalTest += statsStatusForTest.get("TOTAL" + listStatus.get(mj));
                                    %><td align="center"><%=statsStatusForTest.get("TOTAL" + listStatus.get(mj))%></td><%
                                                                        } else {
                                    %><td></td><%                                                                }

                                                                                }
                                                                            }
                                    %><td align="center"><%=totalTest%></td></tr></table>
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
        <script>
            function filterDisplay(checked) {
                if(checked) {
                    $('tr#header').addClass('notVisible');
                    $('tr.testCaseExecutionResult').addClass('notVisible');
                    $('tr.reportDelimiter').addClass('notVisible');

                    $('input.filterCheckbox').removeAttr('disabled');
                    $('input.filterDisplay').attr('checked','checked');
                } else {
                    $('tr#header').removeClass('notVisible');
                    $('tr.testCaseExecutionResult').removeClass('notVisible');
                    $('tr.reportDelimiter').removeClass('notVisible');

                    $('input.filterCheckbox').attr('disabled','disabled');
                    $('input.filterDisplay').removeAttr('checked');
                }
            };
            function toogleDisplay(input) {
                input = $(input);
                var value = input.val();
                if(input.is(':checked')) {
                    $('tr.testCaseExecutionResult').has('td.'+value).addClass(value+'Visible');
                } else {
                    $('tr.testCaseExecutionResult').has('td.'+value).removeClass(value+'Visible');
                }
            };
                    
<%
    if (request.getParameter("FILTER") != null && !"".equals(request.getParameter("FILTER"))) {
        out.println("$(document).ready(function(){filterDisplay(true);");
        String[] filters = {"OK", "KO", "NA", "FA", "PE"};

        for (int i = 0; i < filters.length; i++) {
            if (request.getParameter(filters[i]) != null && filters[i].equals(request.getParameter(filters[i]))) {
                out.println("$('#F" + filters[i] + "').delay( 300 ).queue(function(){$(this).trigger('click');$(this).dequeue();});");
            }
        }
        out.println("});");
    }
%>
        </script>


