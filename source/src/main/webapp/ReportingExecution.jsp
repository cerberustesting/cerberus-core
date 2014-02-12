<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">
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
<% Date DatePageStart = new Date();%>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Execution Reporting : Status</title>
        <link rel="stylesheet" type="text/css" href="css/crb_style.css">
        <link rel="shortcut icon" type="image/x-icon" href="images/favicon.ico" />
    </head>
    <body>
        <%@ include file="include/function.jsp" %>
        <%@ include file="include/header.jsp" %>
        <div id="body">
            <%

                HashMap<String, Integer> statsStatusForTest = new HashMap<String, Integer>();
                List<String> listStatus = new ArrayList<String>();

                HashMap<String, Integer> statsGroupForTest = new HashMap<String, Integer>();
                List<String> listGroup = new ArrayList<String>();

                String tcclauses = " WHERE 1=1 ";
                String execclauses = " 1=1 ";
                String URL = "Apply=Apply";
                String insertURL = "";
                String enable = "disabled";

                IInvariantService invariantService = appContext.getBean(InvariantService.class);
                IBuildRevisionInvariantService buildRevisionInvariantService = appContext.getBean(BuildRevisionInvariantService.class);

                String MySystem = request.getAttribute("MySystem").toString();
                if (request.getParameter("system") != null && request.getParameter("system").compareTo("") != 0) {
                    MySystem = request.getParameter("system");
                }

                String tag;
                if (request.getParameter("Tag") != null && request.getParameter("Tag").compareTo("") != 0) {
                    tag = request.getParameter("Tag");
                    URL = URL + "&Tag=" + tag;
                    execclauses = execclauses + " AND tce.Tag = '" + tag + "'";
                } else {
                    tag = new String("");
                }

                String systemExe;
                String systemBR; // Used for filtering Build and Revision.
                if (request.getParameter("SystemExe") != null && request.getParameter("SystemExe").compareTo("All") != 0) {
                    systemExe = request.getParameter("SystemExe");
                    systemBR = systemExe;
                    URL = URL + "&SystemExe=" + systemExe;
                    execclauses = execclauses + " AND tcev.`System` = '" + systemExe + "'";
                } else {
                    systemExe = new String("");
                    systemBR = MySystem;
                }

                String group;
                if (request.getParameter("Group") != null && request.getParameter("Group").compareTo("All") != 0) {
                    group = request.getParameter("Group");
                    tcclauses = tcclauses + " AND `Group` = '" + request.getParameter("Group") + "'";
                    URL = URL + "&Group=" + group;
                } else {
                    group = new String("%%");
                }

                String port;
                if (request.getParameter("Port") != null && request.getParameter("Port").compareTo("") != 0) {
                    port = request.getParameter("Port");
                    execclauses = execclauses + " AND tce.Port = '" + request.getParameter("Port") + "'";
                    URL = URL + "&Port=" + port;
                } else {
                    port = new String("");
                }
                String ip;
                if (request.getParameter("Ip") != null && request.getParameter("Ip").compareTo("") != 0) {
                    ip = request.getParameter("Ip");
                    execclauses = execclauses + " AND tce.Ip = '" + request.getParameter("Ip") + "'";
                    URL = URL + "&Ip=" + ip;
                } else {
                    ip = new String("");
                }
                String browser;
                if (request.getParameter("browser") != null && request.getParameter("browser").compareTo("") != 0) {
                    browser = request.getParameter("browser");
                    execclauses = execclauses + " AND tce.browser = '" + request.getParameter("browser") + "'";
                    URL = URL + "&Browser=" + browser;
                } else {
                    browser = new String("*firefox");
                }

                String logpath;
                if (request.getParameter("logpath") != null && request.getParameter("logpath").compareTo("") != 0) {
                    logpath = request.getParameter("logpath");
                    execclauses = execclauses + " AND tce.logpath = '" + request.getParameter("logpath") + "'";
                } else {
                    logpath = new String("logpath");
                }

                String tcActive;
                if (request.getParameter("TcActive") != null) {
                    if (request.getParameter("TcActive").compareTo("A") == 0) {
                        tcActive = "%%";
                    } else {
                        tcActive = request.getParameter("TcActive");
                        tcclauses = tcclauses + " AND TcActive = '" + request.getParameter("TcActive") + "'";
                        URL = URL + "&TcActive=" + tcActive;
                    }
                } else {
                    tcActive = new String("Y");
                }

                String readOnly;
                if (request.getParameter("ReadOnly") != null && request.getParameter("ReadOnly").compareTo("A") != 0) {
                    readOnly = request.getParameter("ReadOnly");
                    tcclauses = tcclauses + " AND ReadOnly = '" + request.getParameter("ReadOnly") + "'";
                    URL = URL + "&ReadOnly=" + readOnly;
                } else {
                    readOnly = new String("%%");
                }

                String priority;
                if (request.getParameter("Priority") != null) {
                    if (request.getParameter("Priority").compareTo("All") != 0) {
                        priority = request.getParameter("Priority");
                        tcclauses = tcclauses + " AND Priority = '" + request.getParameter("Priority") + "'";
                        URL = URL + "&Priority=" + priority;
                    } else {
                        priority = "%%";
                    }

                } else {
                    priority = new String("%%");
                }

                String environment;
                if (request.getParameter("Environment") != null && request.getParameter("Environment").compareTo("All") != 0) {
                    environment = request.getParameter("Environment");
                    execclauses = execclauses + " AND tce.Environment = '" + request.getParameter("Environment") + "'";
                    URL = URL + "&Environment=" + environment;
                } else {
                    environment = new String("%%");
                }
                String revision;
                if (request.getParameter("Revision") != null && request.getParameter("Revision").compareTo("All") != 0) {
                    revision = request.getParameter("Revision");
                    execclauses = execclauses + " AND tcev.Revision = '" + request.getParameter("Revision") + "'";
                    URL = URL + "&Revision=" + revision;
                } else {
                    revision = new String("%%");
                }
                String creator;
                if (request.getParameter("Creator") != null && request.getParameter("Creator").compareTo("All") != 0) {
                    creator = request.getParameter("Creator");
                    tcclauses = tcclauses + " AND Creator = '" + request.getParameter("Creator") + "'";
                    URL = URL + "&Creator=" + creator;
                } else {
                    creator = new String("%%");
                }
                String implementer;
                if (request.getParameter("Implementer") != null && request.getParameter("Implementer").compareTo("All") != 0) {
                    implementer = request.getParameter("Implementer");
                    tcclauses = tcclauses + " AND Implementer = '" + request.getParameter("Implementer") + "'";
                    URL = URL + "&Implementer=" + implementer;
                } else {
                    implementer = new String("%%");
                }
                String build;
                if (request.getParameter("Build") != null && request.getParameter("Build").compareTo("All") != 0) {
                    build = request.getParameter("Build");
                    execclauses = execclauses + " AND tce.Build = '" + request.getParameter("Build") + "'";
                    URL = URL + "&Build=" + build;
                } else {
                    build = new String("%%");
                }

                String[] projects;
                String project = "";
                if (request.getParameterValues("Project") != null && (request.getParameterValues("Project")[0]).compareTo("All") != 0) {
                    projects = request.getParameterValues("Project");
                    if (projects != null && projects.length > 0) {
                        tcclauses += " AND (";
                        for (int index = 0; index < projects.length; index++) {
                            tcclauses += " Project = '" + projects[index] + "' ";
                            URL += "&Project=" + projects[index];
                            project += projects[index] + ",";

                            if (index < (projects.length - 1)) {
                                tcclauses += " OR ";
                            }
                        }
                        tcclauses += ") ";
                    }
                } else {
                    projects = new String[1];
                    projects[0] = new String("%%");
                }

                String app;
                if (request.getParameter("Application") != null && request.getParameter("Application").compareTo("All") != 0) {
                    app = request.getParameter("Application");
                    tcclauses = tcclauses + " AND tc.Application = '" + request.getParameter("Application") + "'";
                    URL = URL + "&Application=" + app;
                } else {
                    app = new String("%%");
                }

                String system;
                if (request.getParameter("System") != null && request.getParameter("System").compareTo("All") != 0) {
                    system = request.getParameter("System");
                    tcclauses = tcclauses + " AND a.System = '" + request.getParameter("System") + "'";
                    URL = URL + "&System=" + system;
                } else {
                    system = new String("%%");
                }


                String[] allstatus;
                String status = "";
                if (request.getParameterValues("Status") != null && (request.getParameterValues("Status")[0]).compareTo("All") != 0) {
                    allstatus = request.getParameterValues("Status");
                    if (allstatus != null && allstatus.length > 0) {
                        tcclauses += " AND (";
                        for (int index = 0; index < allstatus.length; index++) {
                            tcclauses += " Status = '" + allstatus[index] + "' ";
                            URL += "&Status=" + allstatus[index];
                            status += allstatus[index] + ",";

                            if (index < (allstatus.length - 1)) {
                                tcclauses += " OR ";
                            }
                        }
                        tcclauses += ") ";
                    }
                } else {
                    allstatus = new String[1];
                    allstatus[0] = new String("%%");
                }

                String targetBuild = "";
                if (request.getParameter("TargetBuild") != null) {
                    if (request.getParameter("TargetBuild").compareTo("All") == 0) {
                        targetBuild = "All";
                    } else {
                        if (request.getParameter("TargetBuild").equals("NTB")) {
                            targetBuild = "";
                            tcclauses = tcclauses + " AND TargetBuild = '' ";
                            URL = URL + "&TargetBuild=" + targetBuild;
                        } else {
                            targetBuild = request.getParameter("TargetBuild");
                            tcclauses = tcclauses + " AND TargetBuild = '" + request.getParameter("TargetBuild") + "'";
                            URL = URL + "&TargetBuild=" + targetBuild;
                        }
                    }
                } else {
                    targetBuild = "All";
                    //tcclauses = tcclauses + " AND TargetBuild = '' ";
                }

                String targetRev = "";
                if (request.getParameter("TargetRev") != null) {
                    if (request.getParameter("TargetRev").compareTo("All") == 0) {
                        targetRev = "All";
                    } else {
                        if (request.getParameter("TargetRev").equals("NTR")) {
                            targetRev = "";
                            tcclauses = tcclauses + " AND TargetRev = '' ";
                            URL = URL + "&TargetRev=" + targetRev;
                        } else {
                            targetRev = request.getParameter("TargetRev");
                            tcclauses = tcclauses + " AND TargetRev = '" + request.getParameter("TargetRev") + "'";
                            URL = URL + "&TargetRev=" + targetRev;
                        }
                    }
                } else {
                    targetRev = "All";
                    //tcclauses = tcclauses + " AND TargetRev = '' ";
                }

                String test;
                if (request.getParameter("Test") != null && request.getParameter("Test").compareTo("All") != 0) {
                    test = request.getParameter("Test");
                    tcclauses = tcclauses + " AND test = '" + request.getParameter("Test") + "'";
                    URL = URL + "&Test=" + test;
                } else {
                    test = new String("%%");
                }
                String testcase;
                if (request.getParameter("TestCase") != null && request.getParameter("TestCase").compareTo("All") != 0) {
                    testcase = request.getParameter("TestCase");
                    tcclauses = tcclauses + " AND testcase = '" + request.getParameter("testcase") + "'";
                } else {
                    testcase = new String("%%");
                }
                String[] country_list = null;
                if (request.getParameter("Country") != null) {
                    country_list = request.getParameterValues("Country");
                } else {
                    country_list = new String[0];
                }

                for (int i = 0; i < country_list.length; i++) {
                    URL = URL + "&Country=" + country_list[i];
                }

                List<String> statistiques = new ArrayList<String>();

                Boolean apply;
                if (request.getParameter("Apply") != null
                        && request.getParameter("Apply").compareTo("Apply") == 0) {
                    apply = true;
                } else {
                    apply = false;
                }

                Boolean recordPref;
                if (request.getParameter("RecordPref") != null
                        && request.getParameter("RecordPref").compareTo("Y") == 0) {
                    recordPref = true;
                } else {
                    recordPref = false;
                }
                String reportingFavorite = "ReportingExecution.jsp?";

                Connection conn = db.connect();
                try {

                    Statement stmt = conn.createStatement();
                    Statement stmt33 = conn.createStatement();

                    IApplicationService myApplicationService = appContext.getBean(IApplicationService.class);
                    String SitdmossBugtrackingURL;
                    String SitdmossBugtrackingURL_tc;
                    SitdmossBugtrackingURL_tc = "";

                    Statement stmt1 = conn.createStatement();
                    ResultSet rs_testcasecountrygeneral = stmt1.executeQuery("SELECT value "
                            + " FROM invariant "
                            + " WHERE idname ='COUNTRY'"
                            + " ORDER BY sort asc");
                    ResultSet rsPref = stmt33.executeQuery("SELECT ReportingFavorite from user where "
                            + " login = '"
                            + request.getUserPrincipal().getName()
                            + "'");

                    Statement stmt5 = conn.createStatement();

                    insertURL = "UPDATE user SET ReportingFavorite = '"
                            + URL + "' where login = '"
                            + request.getUserPrincipal().getName()
                            + "'";
                    if (recordPref == true) {
                        stmt5.execute(insertURL);
                    }


            %>
            <form method="GET" name="Apply" action="ReportingExecution.jsp">
                <table id="arrond">
                    <tr><td id="arrond"><h3 style="color:blue">Filters</h3>
                            <table border="0px">
                                <tr>
                                    <td id="wob" style="width: 110px"><%out.print(dbDocS(conn, "test", "Test", "Test"));%></td>
                                    <td id="wob" style="width: 70px"><%out.print(dbDocS(conn, "project", "idproject", "Project"));%></td>
                                    <td id="wob" style="width: 60px"><%out.print(dbDocS(conn, "application", "System", "System"));%></td>
                                    <td id="wob" style="width: 100px"><%out.print(dbDocS(conn, "application", "Application", "Application"));%></td>
                                    <td id="wob" style="width: 70px"><%out.print(dbDocS(conn, "testcase", "tcactive", "TestCase Active"));%></td>
                                    <td id="wob" style="width: 70px"><%out.print(dbDocS(conn, "invariant", "PRIORITY", "Priority"));%></td>
                                    <td id="wob" style="width: 110px"><%out.print(dbDocS(conn, "testcase", "Status", "Status"));%></td>
                                    <td id="wob" style="width: 110px"><%out.print(dbDocS(conn, "invariant", "GROUP", "Group"));%></td>
                                    <td id="wob" style="width: 110px"><%out.print(dbDocS(conn, "testcase", "targetBuild", "targetBuild"));%></td>                        
                                    <td id="wob" style="width: 110px"><%out.print(dbDocS(conn, "testcase", "targetRev", "targetRev"));%></td>                        
                                    <td id="wob" style="width: 100px"><%out.print(dbDocS(conn, "testcase", "creator", "Creator"));%></td>
                                    <td id="wob" style="width: 100px"><%out.print(dbDocS(conn, "testcase", "implementer", "implementer"));%></td>
                                </tr>

                                <tr>                        
                                    <td id="wob">
                                        <select id="test" style="width: 110px"  name="Test">
                                            <option value="All">-- ALL --</option><%
                                                String optstyle = "";
                                                ResultSet rsTest = stmt.executeQuery("SELECT Test, active FROM test where Test IS NOT NULL Order by Test asc");
                                                while (rsTest.next()) {
                                                    if (rsTest.getString("active").equalsIgnoreCase("Y")) {
                                                        optstyle = "font-weight:bold;";
                                                    } else {
                                                        optstyle = "font-weight:lighter;";
                                                    }%>
                                            <option style="width: 200px;<%=optstyle%>" value="<%= rsTest.getString(1)%>" <%=test.compareTo(rsTest.getString(1)) == 0 ? " SELECTED " : ""%>><%= rsTest.getString(1)%></option><%
                                                }%>
                                        </select>
                                    </td>
                                    <td id="wob">
                                        <select multiple  size="3" id="project" style="width: 170px" name="Project">
                                            <option value="All">-- ALL --</option>
                                            <%
                                                String sq = "SELECT idproject, VCCode, Description, active FROM project ORDER BY idproject";
                                                ResultSet q = stmt.executeQuery(sq);
                                                String ret = "";
                                                while (q.next()) {
                                                    ret = ret + " <option value=\"" + q.getString("idproject") + "\"";
                                                    ret = ret + " style=\"width: 200px;";
                                                    if (q.getString("active").equalsIgnoreCase("Y")) {
                                                        ret = ret + "font-weight:bold;";
                                                    }
                                                    ret = ret + "\"";

                                                    if ((project != null) && (project.indexOf(q.getString("idproject") + ",") >= 0)) {
                                                        ret = ret + " SELECTED ";
                                                    }
                                                    ret = ret + ">" + q.getString("idproject") + " " + q.getString("Description");
                                                    ret = ret + "</option>";
                                                }%>
                                            <%=ret%>
                                        </select>
                                    </td>
                                    <td id="wob">
                                        <select id="system" style="width: 50px" name="System">
                                            <option value="All">-- ALL --</option><%
                                                ResultSet rsSys = stmt.executeQuery("SELECT DISTINCT System FROM application Order by System asc");
                                                while (rsSys.next()) {%>
                                            <option value="<%= rsSys.getString("System")%>" <%=system.compareTo(rsSys.getString("System")) == 0 ? " SELECTED " : ""%>><%= rsSys.getString("System")%></option><%
                                                }%>
                                        </select>
                                    </td>
                                    <td id="wob">
                                        <select id="application" style="width: 100px"  name="Application">
                                            <option value="All">-- ALL --</option><%
                                                ResultSet rsApp = stmt.executeQuery("SELECT Application , System FROM application Order by Sort asc");
                                                while (rsApp.next()) {%>
                                            <option value="<%= rsApp.getString("Application")%>" <%=app.compareTo(rsApp.getString("Application")) == 0 ? " SELECTED " : ""%>><%= rsApp.getString("Application")%> [<%= rsApp.getString("System")%>]</option><%
                                                }%>
                                        </select>
                                    </td>
                                    <td id="wob">
                                        <select style="width: 70px" id="active_tc" name="TcActive">
                                            <option value="A" <%=tcActive.compareTo("A") == 0 ? " SELECTED " : ""%>>-- ALL --</option>
                                            <option value="Y" <%=tcActive.compareTo("Y") == 0 ? " SELECTED " : ""%>>Y</option>
                                            <option value="N" <%=tcActive.compareTo("N") == 0 ? " SELECTED " : ""%>>N</option>
                                        </select>
                                    </td>
                                    <td id="wob">
                                        <select style="width: 70px" id="priority" name="Priority">
                                            <option value="All">-- ALL --</option><%
                                                ResultSet rsPri = stmt.executeQuery("SELECT DISTINCT value FROM invariant WHERE id=15 Order by sort asc");
                                                while (rsPri.next()) {%>
                                            <option value="<%= rsPri.getString(1)%>" <%=priority.compareTo(rsPri.getString(1)) == 0 ? " SELECTED " : ""%>><%= rsPri.getString(1)%></option><%
                                                }%>
                                        </select>
                                    </td>
                                    <td id="wob">
                                        <select  multiple style="width: 110px" id="status" name="Status">
                                            <option value="All">-- ALL --</option><%
                                                ResultSet rsStatus = stmt.executeQuery("SELECT value from invariant where id = 1 order by sort asc");
                                                while (rsStatus.next()) {%>
                                            <option value="<%= rsStatus.getString(1)%>" <%=status.indexOf(rsStatus.getString(1)) >= 0 ? " SELECTED " : ""%>><%= rsStatus.getString(1)%></option><%
                                                }%>
                                        </select>
                                    </td>

                                    <td id="wob">
                                        <select style="width: 110px" id="group" name="Group">
                                            <option value="All">-- ALL --</option><%
                                                ResultSet rsGroup = stmt.executeQuery("SELECT value from invariant where id = 2 order by sort");
                                                while (rsGroup.next()) {%>
                                            <option value="<%= rsGroup.getString(1)%>" <%=group.compareTo(rsGroup.getString(1)) == 0 ? " SELECTED " : ""%>><%= rsGroup.getString(1)%></option><%
                                                }%>
                                        </select>
                                    </td>
                                    <td id="wob">
                                        <select style="width: 110px" id="targetBuild" name="TargetBuild">
                                            <option value="All" <%=targetBuild.equals("All") == true ? " SELECTED " : ""%>>-- ALL --</option>
                                            <option value="NTB" <%=targetBuild.equals("") == true ? " SELECTED " : ""%>>--No Target Build--</option>
                                            <% ResultSet rsTargetBuild = stmt.executeQuery("SELECT value from invariant where idname = 'Build' order by sort");
                                                while (rsTargetBuild.next()) {%>
                                            <option value="<%= rsTargetBuild.getString(1)%>" <%=targetBuild.compareTo(rsTargetBuild.getString(1)) == 0 ? " SELECTED " : ""%>><%= rsTargetBuild.getString(1)%></option><%
                                                }%>
                                        </select>
                                    </td>
                                    <td id="wob">
                                        <select style="width: 110px" id="targetRev" name="TargetRev">
                                            <option value="All" <%=targetRev.compareTo("All") == 0 ? " SELECTED " : ""%>>-- ALL --</option>
                                            <option value="NTR" <%=targetRev.compareTo("") == 0 ? " SELECTED " : ""%>>--No Target Rev--</option>
                                            <% ResultSet rsTargetRev = stmt.executeQuery("SELECT value from invariant where idname = 'Revision' order by sort");
                                                while (rsTargetRev.next()) {%>
                                            <option value="<%= rsTargetRev.getString(1)%>" <%=targetRev.compareTo(rsTargetRev.getString(1)) == 0 ? " SELECTED " : ""%>><%= rsTargetRev.getString(1)%></option><%
                                                }%>
                                        </select>
                                    </td>
                                    <td id="wob">
                                        <select style="width: 100px" id="creator" name="Creator">
                                            <option value="All" <%=creator.compareTo("All") == 0 ? " SELECTED " : ""%>>-- ALL --</option><%
                                                ResultSet rsCreator = stmt.executeQuery("SELECT login from user");
                                                while (rsCreator.next()) {%>
                                            <option value="<%= rsCreator.getString(1)%>" <%=creator.compareTo(rsCreator.getString(1)) == 0 ? " SELECTED " : ""%>><%= rsCreator.getString(1)%></option><%
                                                }%>
                                        </select>
                                    </td>
                                    <td id="wob">
                                        <select style="width: 100px" id="implementer" name="Implementer">
                                            <option value="All" <%=implementer.compareTo("All") == 0 ? " SELECTED " : ""%>>-- ALL --</option><%
                                                rsCreator.first();
                                                while (rsCreator.next()) {%>
                                            <option value="<%= rsCreator.getString(1)%>" <%=implementer.compareTo(rsCreator.getString(1)) == 0 ? " SELECTED " : ""%>><%= rsCreator.getString(1)%></option><%
                                                }%>
                                        </select>
                                    </td>
                                </tr>
                            </table>
                            <table border="0px">
                                <tr><td colspan="7" class="wob"></td></tr>
                                <tr>
                                    <td id="wob" style="width: 130px"><%out.print(dbDocS(conn, "invariant", "Environment", "Environment"));%></td>
                                    <td id="wob" style="width: 70px"><%out.print(dbDocS(conn, "application", "system", "System"));%></td>
                                    <td id="wob" style="width: 130px"><%out.print(dbDocS(conn, "buildrevisioninvariant", "versionname01", "Build"));%></td>
                                    <td id="wob" style="width: 130px"><%out.print(dbDocS(conn, "buildrevisioninvariant", "versionname02", "Revision"));%></td>
                                    <td id="wob" style="width: 130px"><%out.print(dbDocS(conn, "testcaseexecution", "IP", "Ip"));%></td>
                                    <td id="wob" style="width: 130px"><%out.print(dbDocS(conn, "testcaseexecution", "Port", "Port"));%></td>
                                    <td id="wob" style="width: 130px"><%out.print(dbDocS(conn, "testcaseexecution", "tag", "Tag"));%></td>
                                </tr>

                                <tr>
                                    <td id="wob">
                                        <select id="environment" name="Environment" style="width: 130px">
                                            <option style="width: 130px" value="All">-- ALL --</option>
                                            <% ResultSet rsEnv = stmt.executeQuery("SELECT value from invariant where id = 5 order by sort");
                                                while (rsEnv.next()) {%>
                                            <option style="width: 130px" value="<%= rsEnv.getString(1)%>" <%=environment.compareTo(rsEnv.getString(1)) == 0 ? " SELECTED " : ""%>><%= rsEnv.getString(1)%></option><%
                                                }%>
                                        </select>
                                    </td>
                                    <td id="wob">
                                        <select id="systemExe" name="SystemExe" style="width: 70px" >
                                            <option style="width: 100px" value="All">-- ALL --</option>
                                            <%
                                                List<Invariant> systemList = invariantService.findListOfInvariantById("SYSTEM");
                                                for (Invariant myInv : systemList) {
                                            %><option style="width: 100px" value="<%= myInv.getValue()%>" <%=systemExe.compareTo(myInv.getValue()) == 0 ? " SELECTED " : ""%>><%= myInv.getValue()%></option>
                                            <% }
                                            %></select>
                                    </td>
                                    <td id="wob">
                                        <select id="build" name="Build" style="width: 130px" >
                                            <option style="width: 130px" value="All">-- ALL --</option>
                                            <%
                                                List<BuildRevisionInvariant> listBuildRev = buildRevisionInvariantService.findAllBuildRevisionInvariantBySystemLevel(systemBR, 1);
                                                for (BuildRevisionInvariant myBR : listBuildRev) {
                                            %><option style="width: 100px" value="<%= myBR.getVersionName()%>" <%=build.compareTo(myBR.getVersionName()) == 0 ? " SELECTED " : ""%>><%= myBR.getVersionName()%></option>
                                            <% }
                                            %></select>
                                    </td>
                                    <td id="wob">
                                        <select id="revision" name="Revision" style="width: 130px" >
                                            <option style="width: 130px" value="All">-- ALL --</option>
                                            <%
                                                listBuildRev = buildRevisionInvariantService.findAllBuildRevisionInvariantBySystemLevel(systemBR, 2);
                                                for (BuildRevisionInvariant myBR : listBuildRev) {
                                            %><option style="width: 100px" value="<%= myBR.getVersionName()%>" <%=revision.compareTo(myBR.getVersionName()) == 0 ? " SELECTED " : ""%>><%= myBR.getVersionName()%></option>
                                            <% }
                                            %></select>
                                    </td>
                                    <td id="wob"><input style="font-weight: bold; width: 130px" name="Ip" id="Ip" value="<%=ip%>"></td>
                                    <td id="wob"><input style="font-weight: bold; width: 60px" name="Port" id="Port" value="<%=port%>"></td>
                                    <td id="wob"><input style="font-weight: bold; width: 130px" name="Tag" id="Tag" value="<%=tag%>"></td>
                                </tr>
                            </table>
                            <%
                            %>
                            <table border="0px">
                                <tr><td></td></tr>
                                <tr>
                                    <td id ="arrond">
                                        <table>
                                            <tr><td class="wob" colspan="4"><h4 style="color:black">Country</h4></td></tr>
                                            <tr><%

                                                rs_testcasecountrygeneral.first();
                                                do {%>
                                                <td class="wob" style="font-size : x-small ; width: 10px;"><%=rs_testcasecountrygeneral.getString("value")%></td><%
                                                    } while (rs_testcasecountrygeneral.next());
                                                %></tr>
                                            <tr><%

                                                rs_testcasecountrygeneral.first();
                                                do {
                                                %>
                                                <td class="wob"><input value="<%=rs_testcasecountrygeneral.getString("value")%>" type="checkbox" <%
                                                    for (int i = 0; i < country_list.length; i++) {
                                                        if (country_list[i].equals(rs_testcasecountrygeneral.getString("value"))) {%> CHECKED <%}
                                                            }%> name="Country" ></td><%
                                                                           } while (rs_testcasecountrygeneral.next());
                                                    %>
                                                <td id="wob"><input id="button" type="button" value="All" onclick="selectAll('country',true)"><input id="button" type="button" value="None" onclick="selectAll('country',false)"></td>
                                            </tr>
                                        </table>
                                    </td>
                                </tr>
                                <tr>
                                    <td id="wob">
                                        <table>
                                            <tr>
                                                <td class="wob">
                                                    <input id="button" type="submit" name="Apply" value="Apply">
                                                </td>
                                                <%if (!apply) {
                                                        if (rsPref.first()) {
                                                            if (StringUtils.isNotBlank(rsPref.getString("ReportingFavorite"))) {
                                                                reportingFavorite = reportingFavorite + rsPref.getString("ReportingFavorite");
                                                            }
                                                %>
                                                <td class="wob">
                                                    <input id="button" type="button" name="defaultFilter" value="Select My Default Filters" onclick="loadReporting('<%=reportingFavorite%>')">           
                                                </td><% }
                                                } else {
                                                    if (rsPref.first()) {
                                                        if (StringUtils.isNotBlank(rsPref.getString("ReportingFavorite"))) {
                                                            reportingFavorite = reportingFavorite + rsPref.getString("ReportingFavorite");
                                                            if (URL.compareTo(rsPref.getString("ReportingFavorite")) != 0) {

                                                                enable = "";
                                                            }
                                                        } else {
                                                            enable = "";
                                                        }
                                                %>
                                                <td class="wob">
                                                    <input id="button" type="button" <%=enable%> value="Set As My Default Filter" onclick="loadReporting('ReportingExecution.jsp?RecordPref=Y&<%=URL%>')">
                                                </td><%}
                                                    }%>
                                            </tr>
                                        </table>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>                                   

                <br><br>
                <%
                    if (apply) {
                %>
                <table  class="arrond" >
                    <tr>
                        <td id="wob">
                            <table>
                                <tr>
                                    <td id="wob">
                                        <input id="ShowS" type="button" value="Show Summary" onclick="javascript:setInvisibleRep();" style="display:table">
                                        <input id="ShowD" type="button" value="Show Details" onclick="javascript:setVisibleRep();" style="display:none">
                                    </td>
                                    <td id="wob">Legend : </td>
                                    <td id="wob" class="OK" title="OK : Test was fully executed and no bug are to be reported."><a class="OKF">OK</a></td>
                                    <td id="wob" class="KO" title="KO : Test was executed and bug have been detected."><a class="KOF">KO</a></td>
                                    <td id="wob" class="NA" title="NA : Test could not be executed because some test data are not available."><a class="NAF">NA</a></td>
                                    <td id="wob" class="FA" title="FA : Test could not be executed because there is a bug on the test."><a class="FAF">FA</a></td>
                                    <td id="wob" class="PE" title="PE : Test execution is still running..."><a class="PEF">PE</a></td>
                                    <td id="wob" class="NotExecuted" title="Test Case has not been executed for that country."><a class="NotExecutedF">XX</a></td>
                                    <td id="wob" class="NOINF" title="Test Case not available for the country XX."><a class="NOINFF">XX</a></td>
                                </tr>
                            </table>
                        </td>
                    </tr>    
                    <tr>
                        <td id="wob">
                            <table id="reportingExec" style="text-align: left;border-collapse:collapse;display:table" border="1px" cellpadding="0" cellspacing="1">
                                <tr id="header">
                                    <td style="width:10%"><%out.print(dbDocS(conn, "test", "test", "Test"));%></td>
                                    <td style="width:5%"><%out.print(dbDocS(conn, "testcase", "testcase", "TestCase"));%></td>
                                    <td style="width:5%"><%out.print(dbDocS(conn, "application", "application", "Aplication"));%></td>
                                    <td style="width:20%"><%out.print(dbDocS(conn, "testcase", "description", "Description"));%></td>
                                    <td style="width:2%"><%out.print(dbDocS(conn, "invariant", "PRIORITY", "Priority"));%></td>
                                    <td style="width:5%"><%out.print(dbDocS(conn, "testcase", "status", "Status"));%></td>
                                    <%
                                        //rs_testcasecountrygeneral.first();								
                                        //do {
                                        for (int i = 0; i < country_list.length; i++) {
                                    %> 
                                    <td style="width:2%" class="header"> 
                                        <%=country_list[i]%> </td>
                                    <td style="width:7%" class="header" style="font-size : x-small ;">Reporting Execution</td>
                                    <%
                                        }
                                        // } while (rs_testcasecountrygeneral.next());

                                    %>
                                    <td><%out.print(dbDocS(conn, "testcase", "comment", "Comment"));%></td> 
                                    <td style="width:5%" ><%out.print(dbDocS(conn, "testcase", "bugID", "BugID"));%></td>
                                </tr>
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
                                    ResultSet rs_time = stmt2.executeQuery("select tc.test, tc.testcase, "
                                            + " tc.application, tc.description, tc.behaviororvalueexpected, tc.Status, "
                                            + " tc.priority, tc.comment, tc.bugID, tc.TargetBuild, tc.TargetRev, tc.group "
                                            + " from testcase tc "
                                            + " join application a on a.application = tc.application "
                                            + tcclauses
                                            + " and tc.group != 'PRIVATE' "
                                            + " order by tc.test, tc.testcase ");

                                    Statement stmt8 = conn.createStatement();
                                    ResultSet rs_test = stmt8.executeQuery("select distinct test "
                                            + " from testcase tc "
                                            + " join application a on a.application = tc.application "
                                            + tcclauses
                                            + " and tc.group != 'PRIVATE' "
                                            + " order by test ");
                                    if (rs_time.first()) {
                                        //if (StringUtils.isNotBlank(rs_time.getString("tc.test")) == true){ 
                                        if (rs_test.first())
                                            do {
                                                if (!rs_test.getString("test").equals(rs_time.getString("tc.test"))) {%>
                                <tr id="header">
                                    <td></td>
                                    <td></td>
                                    <td></td>
                                    <td></td>
                                    <td></td>
                                    <td></td>
                                    <%
                                        for (int i = 0; i < country_list.length; i++) {
                                    %> 
                                    <td colspan="2" style="text-align: center"> 
                                        <%=country_list[i]%> </td>
                                        <%}%>
                                    <td></td>
                                    <td></td>
                                </tr>

                                <%
                                        j = 0;
                                        rs_test.next();
                                    }
                                    if (j == 12) {%>
                                <tr style="font-size : x-small ;">
                                    <td class="INF"></td>
                                    <td class="INF"></td>
                                    <td class="INF"></td>
                                    <td class="INF"></td>
                                    <td class="INF"></td>
                                    <td class="INF"></td>
                                    <%
                                        for (int i = 0; i < country_list.length; i++) {
                                    %> 
                                    <td class="INF" colspan="2" style="text-align: center"> 
                                        <%=country_list[i]%> </td>

                                    <%}%>
                                    <td class="INF"></td>
                                    <td class="INF"></td>
                                </tr>

                                <%
                                        j = 0;
                                    }
                                %>
                                <tr>
                                    <td class="INF"><%=rs_time.getString("tc.test")%></td>
                                    <td class="INF"><a href="TestCase.jsp?Load=Load&Test=<%=rs_time.getString("tc.test")%>&TestCase=<%=rs_time.getString("tc.testcase")%>"> <%=rs_time.getString("tc.testcase")%></a></td>
                                    <td class="INF"><%=rs_time.getString("tc.application")%></td>
                                    <td class="INF"><%=rs_time.getString("tc.description")%></td>
                                    <td class="INF"><%=rs_time.getString("tc.Priority")%></td>
                                    <td class="INF"><%=rs_time.getString("tc.Status")%></td>
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

                                        // Collecting group stats for current test. 
                                        if (!listGroup.contains(rs_time.getString("tc.Group"))) {
                                            listGroup.add(rs_time.getString("tc.Group"));
                                        }
                                        if (statsGroupForTest.containsKey(rs_time.getString("tc.test") + rs_time.getString("tc.Group"))) {
                                            statsGroupForTest.put(rs_time.getString("tc.test") + rs_time.getString("tc.Group"), statsGroupForTest.get(rs_time.getString("tc.test") + rs_time.getString("tc.Group")) + 1);
                                        } else {
                                            statsGroupForTest.put(rs_time.getString("tc.test") + rs_time.getString("tc.Group"), 1);
                                        }

                                        rs_testcasecountrygeneral.first();
                                        String cssStatus = "";
                                        String color = "black";
                                        Statement stmt4 = conn.createStatement();
                                        ResultSet rs_count = stmt4.executeQuery("select country "
                                                + " from testcasecountry where "
                                                + " test = '"
                                                + rs_time.getString("tc.Test")
                                                + "' and testcase = '"
                                                + rs_time.getString("tc.testcase")
                                                + "' order by country asc");
                                        if (rs_count.first()) {
                                            cssStatus = "NE";
                                            color = "black";

                                            for (int i = 0; i < country_list.length; i++) {

                                                rs_count.first();
                                                while (!rs_count.isLast()) {
                                                    if (rs_count.getString("Country").equalsIgnoreCase(country_list[i])) {
                                                        break;
                                                    }
                                                    rs_count.next();
                                                }

                                                if (country_list[i].equals(rs_count.getString("country"))) {
                                                    //out.println(execclauses);
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
                                    <td class="INF" style="font-size : x-small"> <%=rs_exec.getString("Start")%></td>
                                    <%
                                            stmt3.close();
                                        }
                                    } else {
                                        statistiques.add(i, rs_time.getString("tc.test") + "-" + country_list[i] + "-" + "NE");
                                        cssStatus = "NotExecuted";%>
                                    <td class="<%=cssStatus%>"><a href="RunTests.jsp?Test=<%=rs_time.getString("tc.test")%>&TestCase=<%=rs_time.getString("tc.testcase")%>&Country=<%=country_list[i]%>" class="<%=cssStatus%>F"><%= country_list[i]%></a></td>
                                    <td class="INF"></td>
                                    <%    }
                                        if (rs_count.isLast() == true) {
                                        } else {
                                            rs_count.next();
                                        }
                                    } else {
                                        // if (rs_count.getString("Country").compareTo(country_list[i]) > 0){
                                        //    if(rs_count.isLast()) {} else { rs_count.next(); }

                                        statistiques.add(i, rs_time.getString("tc.test") + "-" + country_list[i] + "-" + "NT");

                                    %>
                                    <td class="NOINF"></td><td class="NOINF"></td>
                                    <%   }// }   
                                        }
                                    %>
                                        <td class="INF"><%
                                        if (rs_time.getString("tc.Comment") != null) {%><%=rs_time.getString("tc.Comment")%><%}%></td>
                                    <td class="INF"><%
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
                                </tr>

                                <%
                                } else {
                                    // do{
                                    for (int i = 0; i < country_list.length; i++) {
                                %>
                                <td class="NOINF"></td><td class="NOINF"></td>
                                <%                                                              }
                                %>
                                <td class="INF"><%
                                    if (rs_time.getString("tc.Comment") != null) {%><%=rs_time.getString("tc.Comment")%><%}%></td>
                                <td class="INF"><%
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

                                <%    }
                                                j++;
                                                stmt4.close();
                                            } while (rs_time.next());
                                    }
                                %>
                            </table>
                            <table id="execReporting" style="display: none" border="0px" cellpadding="0" cellspacing="0">
                                <tr id="header">
                                    <td></td>
                                    <%
                                        for (int i = 0; i < country_list.length; i++) {
                                    %>
                                    <td colspan="3" align="center" style="width: 60px ;"><%=country_list[i]%></td>
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
                                    <td id="repsynthesis3" align="center" style="color : #999999">NE</td>
                                    <%                                                              }
                                    %>
                                </tr>
                                <%
                                    String[] statsdetails = {"", ""};
                                    for (int k = 0; k < country_list.length; k++) {
                                        String OK = "OK" + country_list[k];
                                        String KO = "KO" + country_list[k];
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
                                    List<String> listTOTNE = new ArrayList<String>();
                                    List<String> listTOTNT = new ArrayList<String>();

                                    for (int l = 0; l < distinctList.size(); l++) {
                                        List<String> listOK = new ArrayList<String>();
                                        List<String> listKO = new ArrayList<String>();
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

                                    %>                          <td id="repsynthesis1" class="INF" align="center" style="font : bold  ;
                                        ; background-color: <%=cssGen%>; border-top-color: <%=cssleftTOP%> ; border-right-color: <%=cssleftRIG%>  ">
                                        <%=listCTOK.size() != 0 ? listCTOK.size() : ""%> 
                                    <td id="repsynthesis2" class="INF" align="center" style="font : bold; 
                                        ; background-color: <%=cssGen%>; border-top-color: <%=cssleftTOP%> ; border-right-color: <%=cssleftRIG%> ;border-left-color: <%=cssleftRIG%>  ">
                                        <%=listCTKO.size() != 0 ? listCTKO.size() : ""%> 
                                    <td id="repsynthesis3" class="INF" align="center" style="font : bold ; 
                                        ; background-color: <%=cssGen%>; border-top-color: <%=cssleftTOP%> ; border-left-color: <%=cssleftRIG%>  ">
                                        <%=listCTNE.size() != 0 ? listCTNE.size() : ""%></td>
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

                                            for (int a = 0; a < listTOTNE.size(); a++) {
                                                String[] CTNEdetails = listTOTNE.get(a).split("-");
                                                if (country_list[i].equals(CTNEdetails[0])) {
                                                    if (CTNEdetails[1].equals("NE")) {
                                                        listCTTOTNE.add(CTNEdetails[1]);
                                                    }
                                                }
                                            }


                                    %>
                                    <td align="center" style="color : green"><%=listCTTOTOK.size()%></td>
                                    <td align="center" style="color : red"><%=listCTTOTKO.size()%></td>
                                    <td align="center" style="color : #999999"><%=listCTTOTNE.size()%></td>
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
                            </table>
                            <br>
                            <table id="statusReporting" style="display: none" border="0px" cellpadding="0" cellspacing="0">
                                <tr id="header">
                                    <td>Number of test case</td>
                                    <%
                                        // Loading list of Status invariant sorted in the proper way.
                                        IInvariantService myInvariantService = appContext.getBean(IInvariantService.class);
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
                                        int totalTest = 0;
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
                            </table>
                        </td>
                    </tr>
                </table>
                <%
                            stmt.close();
                            stmt1.close();
                            stmt33.close();
                            stmt5.close();
                            stmt2.close();
                            stmt8.close();

                        }

                    } catch (Exception e) {
                        out.println(e);
                    } finally {
                        try {
                            conn.close();
                        } catch (Exception ex) {
                        }
                    }

                %>

            </form>
        </div>

        <br><% out.print(display_footer(DatePageStart));%>
    </body>
</html>
