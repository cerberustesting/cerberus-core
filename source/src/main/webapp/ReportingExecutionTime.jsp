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
<%@page import="org.apache.commons.lang3.StringUtils"%>
<%@page import="java.lang.*" %>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">
<% Date DatePageStart = new Date();%>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Execution Reporting : Time</title>
        <link rel="stylesheet" type="text/css" href="css/crb_style.css">
        <link rel="shortcut icon" type="image/x-icon" href="images/favicon.ico" />

        <script type="text/javascript">
            var track = 0;
        </script>

    </head>
    <body>

        <%@ include file="include/function.jsp" %>
        <%@ include file="include/header.jsp" %>

        <div id="body">

            <%
                IBuildRevisionInvariantService buildRevisionInvariantService = appContext.getBean(BuildRevisionInvariantService.class);

                String tcclauses = " WHERE 1=1 ";
                String execclauses = " 1=1 ";
                String exetcepagelist = "";
                String avgclauses = " 1=1 ";
                String avgtcepagelist = "";

                String MySystem = request.getAttribute("MySystem").toString();
                if (request.getParameter("system") != null && request.getParameter("system").compareTo("") != 0) {
                    MySystem = request.getParameter("system");
                }

                String tag;
                if (request.getParameter("Tag") != null && request.getParameter("Tag").compareTo("") != 0) {
                    tag = request.getParameter("Tag");
                    execclauses = execclauses + " AND Tag = '" + tag + "'";
                    exetcepagelist = exetcepagelist + "&tag=" + tag;
                } else {
                    tag = new String("");
                }
                String tagRef;
                if (request.getParameter("TagRef") != null && request.getParameter("TagRef").compareTo("") != 0) {
                    tagRef = request.getParameter("TagRef");
                    avgclauses = avgclauses + " AND Tag = '" + tagRef + "'";
                    avgtcepagelist = avgtcepagelist + "&tag=" + tagRef;
                } else {
                    tagRef = new String("");
                }

                String group;
                if (request.getParameter("Group") != null && request.getParameter("Group").compareTo("All") != 0) {
                    group = request.getParameter("Group");
                    tcclauses = tcclauses + " AND group = '" + request.getParameter("Group") + "'";
                } else {
                    group = new String("%%");
                }

                String port;
                if (request.getParameter("Port") != null && request.getParameter("Port").compareTo("") != 0) {
                    port = request.getParameter("Port");
                    execclauses = execclauses + " AND Port = '" + request.getParameter("Port") + "'";
                    exetcepagelist = exetcepagelist + "&port=" + port;
                } else {
                    port = new String("");
                }
                String portRef;
                if (request.getParameter("PortRef") != null && request.getParameter("PortRef").compareTo("") != 0) {
                    portRef = request.getParameter("PortRef");
                    avgclauses = avgclauses + " AND Port = '" + request.getParameter("PortRef") + "'";
                    avgtcepagelist = avgtcepagelist + "&port=" + portRef;
                } else {
                    portRef = new String("");
                }

                String ip;
                if (request.getParameter("Ip") != null && request.getParameter("Ip").compareTo("") != 0) {
                    ip = request.getParameter("Ip");
                    execclauses = execclauses + " AND Ip = '" + request.getParameter("Ip") + "'";
                    exetcepagelist = exetcepagelist + "&ip=" + ip;
                } else {
                    ip = new String("");
                }
                String ipRef;
                if (request.getParameter("IpRef") != null && request.getParameter("IpRef").compareTo("") != 0) {
                    ipRef = request.getParameter("IpRef");
                    avgclauses = avgclauses + " AND Ip = '" + request.getParameter("IpRef") + "'";
                    avgtcepagelist = avgtcepagelist + "&ip=" + ipRef;
                } else {
                    ipRef = new String("");
                }

                String browser;
                if (request.getParameter("browser") != null && request.getParameter("browser").compareTo("") != 0) {
                    browser = request.getParameter("browser");
                    execclauses = execclauses + " AND browser = '" + request.getParameter("browser") + "'";
                } else {
                    browser = new String("");
                }
                String browserRef;
                if (request.getParameter("browserRef") != null && request.getParameter("browserRef").compareTo("") != 0) {
                    browserRef = request.getParameter("browserRef");
                    avgclauses = avgclauses + " AND browserRef = '" + request.getParameter("browserRef") + "'";
                } else {
                    browserRef = new String("");
                }

                String logpath;
                if (request.getParameter("logpath") != null && request.getParameter("logpath").compareTo("") != 0) {
                    logpath = request.getParameter("logpath");
                    execclauses = execclauses + " AND logpath = '" + request.getParameter("logpath") + "'";
                } else {
                    logpath = new String("");
                }

                String tcActive;
                if (request.getParameter("TcActive") != null) {
                    tcActive = request.getParameter("TcActive");
                    tcclauses = tcclauses + " AND TcActive = '" + request.getParameter("TcActive") + "'";
                    if (request.getParameter("TcActive").compareTo("A") == 0) {
                        tcActive = "%%";
                    }
                } else {
                    tcActive = new String("Y");
                }

                String readOnly;
                if (request.getParameter("ReadOnly") != null && request.getParameter("ReadOnly").compareTo("A") != 0) {
                    readOnly = request.getParameter("ReadOnly");
                    tcclauses = tcclauses + " AND ReadOnly = '" + request.getParameter("ReadOnly") + "'";
                } else {
                    readOnly = new String("%%");
                }

                String priority;
                if (request.getParameter("Priority") != null && request.getParameter("Priority").compareTo("All") != 0) {
                    priority = request.getParameter("Priority");
                    tcclauses = tcclauses + " AND Priority = '" + request.getParameter("Priority") + "'";
                    if (request.getParameter("Priority").compareTo("All") == 0) {
                        priority = "%%";
                    }
                } else {
                    priority = new String("%%");
                }

                String environment;
                if (request.getParameter("Environment") != null && request.getParameter("Environment").compareTo("All") != 0) {
                    environment = request.getParameter("Environment");
                    execclauses = execclauses + " AND Environment = '" + request.getParameter("Environment") + "'";
                    exetcepagelist = exetcepagelist + "&environment=" + environment;
                } else {
                    environment = new String("%%");
                }
                String environmentRef;
                if (request.getParameter("EnvironmentRef") != null && request.getParameter("EnvironmentRef").compareTo("All") != 0) {
                    environmentRef = request.getParameter("EnvironmentRef");
                    avgclauses = avgclauses + " AND Environment = '" + request.getParameter("EnvironmentRef") + "'";
                    avgtcepagelist = avgtcepagelist + "&environment=" + environmentRef;
                } else {
                    environmentRef = new String("%%");
                }

                String revision;
                if (request.getParameter("Revision") != null && request.getParameter("Revision").compareTo("All") != 0) {
                    revision = request.getParameter("Revision");
                    execclauses = execclauses + " AND Revision = '" + request.getParameter("Revision") + "'";
                    exetcepagelist = exetcepagelist + "&revision=" + revision;
                } else {
                    revision = new String("%%");
                }
                String revisionRef;
                if (request.getParameter("RevisionRef") != null && request.getParameter("RevisionRef").compareTo("All") != 0) {
                    revisionRef = request.getParameter("RevisionRef");
                    avgclauses = avgclauses + " AND Revision = '" + request.getParameter("RevisionRef") + "'";
                    avgtcepagelist = avgtcepagelist + "&revision=" + revisionRef;
                } else {
                    revisionRef = new String("%%");
                }

                String build;
                if (request.getParameter("Build") != null && request.getParameter("Build").compareTo("All") != 0) {
                    build = request.getParameter("Build");
                    execclauses = execclauses + " AND Build = '" + request.getParameter("Build") + "'";
                    exetcepagelist = exetcepagelist + "&build=" + build;
                } else {
                    build = new String("%%");
                }

                String buildRef;
                if (request.getParameter("BuildRef") != null && request.getParameter("BuildRef").compareTo("All") != 0) {
                    buildRef = request.getParameter("BuildRef");
                    avgclauses = avgclauses + " AND Build = '" + request.getParameter("BuildRef") + "'";
                    avgtcepagelist = avgtcepagelist + "&build=" + buildRef;
                } else {
                    buildRef = new String("%%");
                }

                String project;
                if (request.getParameter("Project") != null && request.getParameter("Project").compareTo("All") != 0) {
                    project = request.getParameter("Project");
                    tcclauses = tcclauses + " AND Project = '" + request.getParameter("Project") + "'";
                } else {
                    project = new String("%%");
                }

                String app;
                if (request.getParameter("Application") != null && request.getParameter("Application").compareTo("All") != 0) {
                    app = request.getParameter("Application");
                    tcclauses = tcclauses + " AND Application = '" + request.getParameter("Application") + "'";
                } else {
                    app = new String("%%");
                }

                String status;
                if (request.getParameter("Status") != null && request.getParameter("Status").compareTo("All") != 0) {
                    status = request.getParameter("Status");
                    tcclauses = tcclauses + " AND Status = '" + request.getParameter("Status") + "'";
                } else {
                    status = new String("%%");
                }

                String test;
                if (request.getParameter("Test") != null && request.getParameter("Test").compareTo("All") != 0) {
                    test = request.getParameter("Test");
                    tcclauses = tcclauses + " AND test = '" + request.getParameter("Test") + "'";
                } else {
                    test = new String("%%");
                }
                String testcase;
                if (request.getParameter("TestCase") != null && request.getParameter("TestCase").compareTo("All") != 0) {
                    testcase = request.getParameter("TestCase");
                    tcclauses = tcclauses + " AND testcase = '" + request.getParameter("TestCase") + "'";
                } else {
                    testcase = new String("%%");
                }

                String[] country_list = null;
                // LinkedList<Country> country;
                if (request.getParameter("Country") != null) {
                    country_list = request.getParameterValues("Country");

                } else {
                    country_list = new String[0];
                }

                Boolean apply;
                if (request.getParameter("Apply") != null
                        && request.getParameter("Apply").compareTo("Apply") == 0) {
                    apply = true;
                } else {
                    apply = false;
                }

                Connection conn = null;
                try {

                    conn = db.connect();

                    Statement stmt = conn.createStatement();


                    Statement stmt1 = conn.createStatement();
                    ResultSet rs_testcasecountrygeneral = stmt1.executeQuery("SELECT value "
                            + " FROM invariant "
                            + " WHERE idname ='COUNTRY'"
                            + " ORDER BY sort asc");

            %>

            <form action="ReportingExecutionTime.jsp" method="GET" name="Apply">
                <table id="arrond">
                    <tr>
                        <td  id="arrond">
                            <table><tr><td class="wob"><h3 style="color:blue">Filters</h3></td></tr></table>
                            <table>
                                <tr>
                                    <td class="wob">
                                        <table border="0px">
                                            <tr><td class="wob" COLSPAN="8"><h4 style="color:black">TestCase Parameters</h4></td></tr>              
                                            <tr>
                                                <td id="wob" style="width: 110px"><%out.print(dbDocS(conn, "runnerpage", "Test", "Test"));%></td>
                                                <td id="wob" style="width: 110px"><%out.print(dbDocS(conn, "runnerpage", "Project", "Project"));%></td>
                                                <td id="wob" style="width: 110px"><%out.print(dbDocS(conn, "runnerpage", "Application", "Application"));%></td>
                                                <td id="wob" style="width: 110px"><%out.print(dbDocS(conn, "runnerpage", "Read Only", "Read Only"));%></td>
                                                <td id="wob" style="width: 110px"><%out.print(dbDocS(conn, "runnerpage", "TestCaseActive", "TestCase Active"));%></td>
                                                <td id="wob" style="width: 70px"><%out.print(dbDocS(conn, "runnerpage", "Priority", "Priority"));%></td>
                                                <td id="wob" style="width: 110px"><%out.print(dbDocS(conn, "testcase", "Status", "Status"));%></td>
                                                <td id="wob" style="width: 110px"><%out.print(dbDocS(conn, "testcase", "Group", "Group"));%></td>
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
                                                    <% out.print(ComboProject(conn, "project", "width: 100px;", "project", "", project, "", true, "All", "-- ALL --"));%>
                                                </td>
                                                <td id="wob"><select id="application" style="width: 110px"  name="Application">
                                                        <option value="All">-- ALL --</option>
                                                        <% ResultSet rsApp = stmt.executeQuery("SELECT DISTINCT Application FROM application Order by sort asc");
                                                            while (rsApp.next()) {%>
                                                        <option value="<%= rsApp.getString(1)%>" <%=app.compareTo(rsApp.getString(1)) == 0 ? " SELECTED " : ""%>><%= rsApp.getString(1)%></option>
                                                        <% }%>
                                                    </select></td>
                                                <td id="wob"><select style="width: 60px" id="readonly" name="ReadOnly">
                                                        <option value="A" <%=readOnly.compareTo("A") == 0 ? " SELECTED " : ""%>>-- ALL --</option>
                                                        <option value="Y" <%=readOnly.compareTo("Y") == 0 ? " SELECTED " : ""%>>Y</option>
                                                        <option value="N" <%=readOnly.compareTo("N") == 0 ? " SELECTED " : ""%>>N</option>
                                                    </select></td>
                                                <td id="wob"><select style="width: 60px" id="active_tc" name="TcActive">
                                                        <option value="A" <%=tcActive.compareTo("A") == 0 ? " SELECTED " : ""%>>-- ALL --</option>
                                                        <option value="Y" <%=tcActive.compareTo("Y") == 0 ? " SELECTED " : ""%>>Y</option>
                                                        <option value="N" <%=tcActive.compareTo("N") == 0 ? " SELECTED " : ""%>>N</option>
                                                    </select></td>
                                                <td id="wob"><select style="width: 110px" id="priority" name="Priority">
                                                        <option value="All">-- ALL --</option>
                                                        <% ResultSet rsPri = stmt.executeQuery("SELECT value from invariant where id = 15 order by sort");
                                                            while (rsPri.next()) {%>
                                                        <option value="<%= rsPri.getString(1)%>" <%=priority.compareTo(rsPri.getString(1)) == 0 ? " SELECTED " : ""%>><%= rsPri.getString(1)%></option>
                                                        <% }%>
                                                    </select></td>
                                                <td id="wob"><select style="width: 110px" id="status" name="Status">
                                                        <option value="All">-- ALL --</option>
                                                        <% ResultSet rsStatus = stmt.executeQuery("SELECT value from invariant where id = 1 order by sort");
                                                            while (rsStatus.next()) {%>
                                                        <option value="<%= rsStatus.getString(1)%>" <%=status.compareTo(rsStatus.getString(1)) == 0 ? " SELECTED " : ""%>><%= rsStatus.getString(1)%></option>
                                                        <% }%>
                                                    </select></td>
                                                <td id="wob"><select style="width: 110px" id="group" name="Group">
                                                        <option value="All">-- ALL --</option>
                                                        <% ResultSet rsGroup = stmt.executeQuery("SELECT value from invariant where idname = 'Group' order by sort");
                                                            while (rsGroup.next()) {%>
                                                        <option value="<%= rsGroup.getString(1)%>" <%=group.compareTo(rsGroup.getString(1)) == 0 ? " SELECTED " : ""%>><%= rsGroup.getString(1)%></option>
                                                        <% }%>
                                                    </select></td>
                                            </tr> 
                                        </table>
                                    </td>
                                    <td class="wob">
                                        <table>
                                            <tr><td id ="arrond">
                                                    <table>
                                                        <tr><td class="wob" colspan="4"><h4 style="color:black">Country</h4></td></tr>
                                                        <tr>
                                                            <%

                                                                rs_testcasecountrygeneral.first();
                                                                do {%>
                                                            <td class="wob" style="font-size : x-small ; width: 10px;"><%=rs_testcasecountrygeneral.getString("value")%></td>
                                                            <% 		} while (rs_testcasecountrygeneral.next());%>
                                                        </tr>
                                                        <tr>
                                                            <%

                                                                rs_testcasecountrygeneral.first();
                                                                do {
                                                            %>
                                                            <td class="wob"><input value="<%=rs_testcasecountrygeneral.getString("value")%>" type="checkbox" 
                                                                                   <% for (int i = 0; i < country_list.length; i++) {
                                                                                           if (country_list[i].equals(rs_testcasecountrygeneral.getString("value"))) {%>  CHECKED  <% }
                                                                                               }%>
                                                                                   name="Country" ></td>
                                                                <% //onclick="return false"
                                                                    } while (rs_testcasecountrygeneral.next());
                                                                %>


                                                            <td id="wob"><input id="button" type="button" value="All" onclick="selectAll('country',true)"><input id="button" type="button" value="None" onclick="selectAll('country',false)"></td>
                                                        </tr>
                                                    </table>
                                                </td></tr>
                                        </table>
                                    </td>
                                </tr>
                            </table>
                            <table>
                                <tr>
                                    <td id="arrondgreen">
                                        <table><tr><td class="wob"><h4 style="color:green">Execution Parameters to compare</h4></td></tr></table>
                                        <table border="0px">
                                            <tr><td class="wob" colspan="7"></td></tr>
                                            <tr>
                                                <td id="wob" style="width: 90px"><%out.print(dbDocS(conn, "runnerpage", "Environment", "Environment"));%></td>
                                                <td id="wob" style="width: 90px"><%out.print(dbDocS(conn, "runnerpage", "Build", "Build"));%></td>
                                                <td id="wob" style="width: 90px"><%out.print(dbDocS(conn, "runnerpage", "Revision", "Revision"));%></td>
                                                <td id="wob" style="width: 90px"><%out.print(dbDocS(conn, "testcaseexecution", "IP", "Ip"));%></td>
                                                <td id="wob" style="width: 90px"><%out.print(dbDocS(conn, "testcaseexecution", "Port", "Port"));%></td>
                                                <td id="wob" style="width: 90px"><%out.print(dbDocS(conn, "runnerpage", "Tag", "Tag"));%></td>
                                            </tr>

                                            <tr>
                                                <td id="wob"><select id="environment" name="Environment" style="width:90px">
                                                        <option style="width: 90px" value="All">-- ALL --</option>
                                                        <% ResultSet rsEnv = stmt.executeQuery("SELECT value from invariant where id = 5 order by sort");
                                                            while (rsEnv.next()) {%>
                                                        <option style="width: 90px" value="<%= rsEnv.getString(1)%>" <%=environment.compareTo(rsEnv.getString(1)) == 0 ? " SELECTED " : ""%>><%= rsEnv.getString(1)%></option>
                                                        <% }%>
                                                    </select></td>
                                                <td id="wob">
                                                    <select id="build" name="Build" style="width: 70px" >
                                                        <option style="width: 100px" value="All">-- ALL --</option>
                                                        <%
                                                            List<BuildRevisionInvariant> listBuildRev = buildRevisionInvariantService.findAllBuildRevisionInvariantBySystemLevel(MySystem, 1);
                                                            for (BuildRevisionInvariant myBR : listBuildRev) {
                                                        %><option style="width: 100px" value="<%= myBR.getVersionName()%>" <%=build.compareTo(myBR.getVersionName()) == 0 ? " SELECTED " : ""%>><%= myBR.getVersionName()%></option>
                                                        <% }
                                                        %></select>
                                                </td>
                                                <td id="wob">
                                                    <select id="revision" name="Revision" style="width: 70px" >
                                                        <option style="width: 100px" value="All">-- ALL --</option>
                                                        <%
                                                            listBuildRev = buildRevisionInvariantService.findAllBuildRevisionInvariantBySystemLevel(MySystem, 2);
                                                            for (BuildRevisionInvariant myBR : listBuildRev) {
                                                        %><option style="width: 100px" value="<%= myBR.getVersionName()%>" <%=revision.compareTo(myBR.getVersionName()) == 0 ? " SELECTED " : ""%>><%= myBR.getVersionName()%></option>
                                                        <% }
                                                        %></select>
                                                </td>
                                                <td id="wob"><input style="font-weight: bold; width: 90px" name="Ip" id="Ip" value="<%=ip%>"></td>
                                                <td id="wob"><input style="font-weight: bold; width: 60px" name="Port" id="Port" value="<%=port%>"></td>
                                                <td id="wob"><input style="font-weight: bold; width: 150px" name="Tag" id="Tag" value="<%=tag%>"></td>
                                            </tr>
                                        </table>
                                    </td>
                                    <td id="arrondred">
                                        <table><tr><td class="wob"><h4 style="color:red">Execution Parameters of Reference</h4></td></tr></table>
                                        <table border="0px">
                                            <tr><td class="wob" colspan="7"></td></tr>
                                            <tr>
                                                <td id="wob" style="width: 90px"><%out.print(dbDocS(conn, "runnerpage", "Environment", "Environment"));%></td>
                                                <td id="wob" style="width: 90px"><%out.print(dbDocS(conn, "runnerpage", "Build", "Build"));%></td>
                                                <td id="wob" style="width: 90px"><%out.print(dbDocS(conn, "runnerpage", "Revision", "Revision"));%></td>
                                                <td id="wob" style="width: 90px"><%out.print(dbDocS(conn, "testcaseexecution", "IP", "Ip"));%></td>
                                                <td id="wob" style="width: 90px"><%out.print(dbDocS(conn, "testcaseexecution", "Port", "Port"));%></td>
                                                <td id="wob" style="width: 90px"><%out.print(dbDocS(conn, "runnerpage", "Tag", "Tag"));%></td>
                                            </tr>

                                            <tr>
                                                <td id="wob"><select id="environmentRef" name="EnvironmentRef" style="width:90px">
                                                        <option style="width: 90px" value="All">-- ALL --</option>
                                                        <% ResultSet rsEnvRef = stmt.executeQuery("SELECT value from invariant where id = 5 order by sort");
                                                            while (rsEnvRef.next()) {%>
                                                        <option style="width: 90px" value="<%= rsEnvRef.getString(1)%>" <%=environmentRef.compareTo(rsEnvRef.getString(1)) == 0 ? " SELECTED " : ""%>><%= rsEnvRef.getString(1)%></option>
                                                        <% }%>
                                                    </select></td>
                                                <td id="wob">
                                                    <select id="buildRef" name="BuildRef" style="width: 70px" >
                                                        <option style="width: 70px" value="All">-- ALL --</option>
                                                        <%
                                                            listBuildRev = buildRevisionInvariantService.findAllBuildRevisionInvariantBySystemLevel(MySystem, 1);
                                                            for (BuildRevisionInvariant myBR : listBuildRev) {
                                                        %><option style="width: 100px" value="<%= myBR.getVersionName()%>" <%=buildRef.compareTo(myBR.getVersionName()) == 0 ? " SELECTED " : ""%>><%= myBR.getVersionName()%></option>
                                                        <% }
                                                        %></select>
                                                </td>
                                                <td id="wob">
                                                    <select id="revisionRef" name="RevisionRef" style="width: 70px" >
                                                        <option style="width: 100px" value="All">-- ALL --</option>
                                                        <%
                                                            listBuildRev = buildRevisionInvariantService.findAllBuildRevisionInvariantBySystemLevel(MySystem, 2);
                                                            for (BuildRevisionInvariant myBR : listBuildRev) {
                                                        %><option style="width: 100px" value="<%= myBR.getVersionName()%>" <%=revisionRef.compareTo(myBR.getVersionName()) == 0 ? " SELECTED " : ""%>><%= myBR.getVersionName()%></option>
                                                        <% }
                                                        %></select>
                                                </td>
                                                <td id="wob"><input style="font-weight: bold; width: 90px" name="IpRef" id="ipRef" value="<%=ipRef%>"></td>
                                                <td id="wob"><input style="font-weight: bold; width: 60px" name="PortRef" id="portRef" value="<%=portRef%>"></td>
                                                <td id="wob"><input style="font-weight: bold; width: 150px" name="TagRef" id="tagRef" value="<%=tagRef%>"></td>
                                            </tr>
                                        </table> 
                                    </td>
                                </tr>
                            </table>

                            <%%>
                            <table border="0px" >
                                <tr>
                                    <td id="wob">
                                        <input id="button" type="submit" name="Apply" value="Apply">
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table> 
            </form>

            <br><br>

            <%
                if (apply) {
            %>
            <table id="arrond">
                <tr>
                    <td>
                        <table style="text-align: left;border-collapse:collapse;" border="1px" cellpadding="0" cellspacing="1">
                            <tr id="header">
                                <td style="width: 30px"><%out.print(dbDocS(conn, "testcase", "test", "Test"));%></td>
                                <td style="width: 30px"><%out.print(dbDocS(conn, "testcase", "testcase", "TestCase"));%></td>
                                <td style="width: 30px"><%out.print(dbDocS(conn, "testcase", "application", "Aplication"));%></td>
                                <td style="width: 30px"><%out.print(dbDocS(conn, "testcase", "description", "Description"));%></td>
                                <td style="width: 30px"><%out.print(dbDocS(conn, "testcase", "priority", "Priority"));%></td>
                                <%
                                    //rs_testcasecountrygeneral.first();								
                                    //do {
                                    for (int i = 0; i < country_list.length; i++) {
                                %> 
                                <td class="header"> <%=country_list[i]%> </td>
                                <td class="header" style="font-size : x-small ;">Time Comparison</td>
                                <%
                                    }
                                    // } while (rs_testcasecountrygeneral.next());

                                %>
                            </tr>
                            <%
                                int j = 0;

                                Statement stmt2 = conn.createStatement();
                                ResultSet rs_time = stmt2.executeQuery("select tc.test, tc.testcase, "
                                        + " tc.application, tc.description, tc.Status, "
                                        + " tc.priority"
                                        + " from testcase tc "
                                        + tcclauses
                                        + "");

                                Statement stmt8 = conn.createStatement();
                                ResultSet rs_test = stmt8.executeQuery("select distinct test "
                                        + " from testcase "
                                        + tcclauses
                                        + "");
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
                                <%
                                    for (int i = 0; i < country_list.length; i++) {
                                %> 
                                <td colspan="2" style="text-align: center"> <%=country_list[i]%> </td>
                                <%}%>
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
                                <%
                                    for (int i = 0; i < country_list.length; i++) {
                                %> 
                                <td class="INF" colspan="2" style="text-align: center"> <%=country_list[i]%> </td>
                                <%}%>
                            </tr>

                            <%
                                    j = 0;
                                }

                            %>
                            <tr>
                                <td class="INF" style="width: 30px"><%=rs_time.getString("tc.test")%></td>
                                <td class="INF" style="width: 30px"><a href="TestCase.jsp?Load=Load&Test=<%=rs_time.getString("tc.test")%>&TestCase=<%=rs_time.getString("tc.testcase")%>"> <%=rs_time.getString("tc.testcase")%></a></td>
                                <td class="INF" style="width: 30px"><%=rs_time.getString("tc.application")%></td>
                                <td class="INF" style="width: 30px"><%=rs_time.getString("tc.description")%></td>
                                <td class="INF" style="width: 30px"><%=rs_time.getString("tc.Priority")%></td>
                                <%

                                    rs_testcasecountrygeneral.first();

                                    Statement stmt4 = conn.createStatement();
                                    ResultSet rs_count = stmt4.executeQuery("select country "
                                            + " from testcasecountry where "
                                            + " test = '"
                                            + rs_time.getString("tc.Test")
                                            + "' and testcase = '"
                                            + rs_time.getString("tc.testcase")
                                            + "' order by country asc");
                                    if (rs_count.first()) {
                                        String elapsed = "";
                                        for (int i = 0; i < country_list.length; i++) {
                                            //do {
                                            if (country_list[i].equals(rs_count.getString("country"))) {

                                                Statement stmt3 = conn.createStatement();
                                                ResultSet rs_exec = stmt3.executeQuery("select test, testcase, "
                                                        //+ " te.`end`, te.`start`, `end`-`start` as elaps "
                                                        + "cast(avg(UNIX_TIMESTAMP(`end`)-UNIX_TIMESTAMP(`start`)) as decimal(5,2)) as elaps"
                                                        + " from testcaseexecution where "
                                                        //+ " ID = ("
                                                        //+ " select max(ID) from testcaseexecution "
                                                        //+ " where "
                                                        + execclauses
                                                        + " and test = '"
                                                        + rs_time.getString("tc.Test")
                                                        + "' and testcase = '"
                                                        + rs_time.getString("tc.testcase")
                                                        + "' and country = '"
                                                        + country_list[i]
                                                        + "' and controlstatus = 'OK'");
                                                String ID = null;
                                                Statement stmt6 = conn.createStatement();
                                                ResultSet rs_id = stmt6.executeQuery("select max(ID) as ID "
                                                        + " from testcaseexecution where "
                                                        + execclauses
                                                        + " and test = '"
                                                        + rs_time.getString("tc.Test")
                                                        + "' and testcase = '"
                                                        + rs_time.getString("tc.testcase")
                                                        + "' and country = '"
                                                        + country_list[i]
                                                        + "' and controlstatus = 'OK'");
                                                //+ "")");
                                                if (rs_exec.first()) {
                                                    elapsed = "";
                                                    //rs_exec.first();

                                                    //if (StringUtils.isNotBlank(rs_exec.getString("te.test")) == true)


                                                    //   do{
                                                    Statement stmt5 = conn.createStatement();
                                                    ResultSet rs_avg = stmt5.executeQuery("select cast(avg(UNIX_TIMESTAMP(`end`)-UNIX_TIMESTAMP(`start`)) as decimal(5,2)) as avg"
                                                            + " from testcaseexecution where "
                                                            + avgclauses
                                                            + " and test = '"
                                                            + rs_time.getString("tc.Test")
                                                            + "' and testcase = '"
                                                            + rs_time.getString("tc.testcase")
                                                            + "' and country = '"
                                                            + country_list[i]
                                                            + "' and controlstatus = 'OK'"
                                                            + "");
                                                    //double average = 0.00;

                                                    String average = "titi";
                                                    elapsed = rs_exec.getString("elaps");
                                                    String cssTIME = "EQ";
                                                    String csslink = "";
                                                    if (rs_avg.first());
                                                    //if (StringUtils.isNotBlank(rs_avg.getString("avg")) == true)
                                                    {

                                                        //DecimalFormat df = new DecimalFormat();
                                                        //average = Double.parseDouble(rs_avg.getString("avg"));
                                                        //average.setMaximumFractionDigits(2);
                                                        average = rs_avg.getString("avg");
                                                        // out.println(average);
                                                        // double elapstime = Double.valueOf(rs_exec.getString("elaps"));
                                                        if ((StringUtils.isNotBlank(average) == true) && (StringUtils.isNotBlank(elapsed) == true)) {
                                                            if ((Double.parseDouble(average) / Double.parseDouble(elapsed)) < 0.7) {
                                                                cssTIME = "UPPER";
                                                            } else {
                                                                if ((Double.parseDouble(average) / Double.parseDouble(elapsed)) >= 0.7 && (Double.parseDouble(average) / Double.parseDouble(elapsed)) < 0.9) {
                                                                    cssTIME = "UP";
                                                                } else {
                                                                    if ((Double.parseDouble(average) / Double.parseDouble(elapsed)) >= 0.9 && (Double.parseDouble(average) / Double.parseDouble(elapsed)) <= 1.1) {
                                                                        cssTIME = "EQ";
                                                                    } else {
                                                                        if ((Double.parseDouble(average) / Double.parseDouble(elapsed)) > 1.1 && (Double.parseDouble(average) / Double.parseDouble(elapsed)) <= 1.3) {
                                                                            cssTIME = "DO";
                                                                        } else {
                                                                            if ((Double.parseDouble(average) / Double.parseDouble(elapsed)) > 1.3) {
                                                                                cssTIME = "DOWN";
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        } else {
                                                            if (StringUtils.isNotBlank(average) == true) {
                                                                average = rs_avg.getString("avg");
                                                                elapsed = "NA";
                                                            } else {
                                                                if (StringUtils.isNotBlank(elapsed) == true) {
                                                                    average = "NA";
                                                                } else {
                                                                    elapsed = "NA";
                                                                    average = "NA";
                                                                }
                                                            }
                                                            cssTIME = "NA";
                                                        }
                                                    }

                                                    if (rs_id.first()) {
                                                        ID = rs_id.getString("ID");
                                                    }
                                                    //  else {average = "toto";}

                                %> 
                                <td class="INF"> 
                                    <a href="ExecutionDetail.jsp?id_tc=<%=ID%>" title="Last Execution.">
                                        <img src="images/<%=cssTIME%>.png" border="0"/></a>
                                </td>
                                <td class="INF" style="font-size : x-small"><%
                                    if (elapsed.equalsIgnoreCase("NA")) {%>
                                    NA /
                                    <%       } else {%>
                                    <a href="ExecutionDetailList.jsp?test=<%=rs_time.getString("tc.Test")%>&testcase=<%=rs_time.getString("tc.testcase")%>&country=<%=country_list[i]%>&controlStatus=OK<%=exetcepagelist%>"><%=elapsed%></a>s /                 
                                    <%            }
                                        if (average.equalsIgnoreCase("NA")) {%>
                                    NA
                                    <%       } else {%>
                                    <a href="ExecutionDetailList.jsp?test=<%=rs_time.getString("tc.Test")%>&testcase=<%=rs_time.getString("tc.testcase")%>&country=<%=country_list[i]%>&controlStatus=OK<%=avgtcepagelist%>"><%=average%></a>s               
                                    <%            }
                                    %>
                                </td>
                                <%
                                    // } while (rs_exec.next());
                                } else {
                                %>
                                <td class="EQ"> 
                                    <%= country_list[i]%> </td>
                                <td> / </td>
                                <%
                                    }
                                    if (rs_count.isLast()) {
                                    } else {
                                        rs_count.next();
                                    }
                                } else {
                                %>
                                <td class="NOINF"></td>
                                <td class="NOINF"></td>
                                <%   }
                                    }
                                    //} while (rs_testcasecountrygeneral.next());
                                %>
                                <%
                                } else {
                                    for (int i = 0; i < country_list.length; i++) {
                                %>
                                <td class="NOINF"></td>
                                <td class="NOINF"></td>
                                <%     }
                                    }%>
                            </tr>
                            <%
                                            j++;
                                        } while (rs_time.next());
                                }
                            %>
                        </table>
                    </td>
                </tr>
            </table>
            <%
                        stmt.close();
                        stmt1.close();
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

        </div>
        <br><% out.print(display_footer(DatePageStart));%>
    </body>
</html>
