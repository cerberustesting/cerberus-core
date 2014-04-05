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
<%@page import="java.util.Calendar"%>
<%@page import="java.sql.Statement"%>
<%@page import="java.sql.ResultSet"%>
<%@page import="org.apache.log4j.Level"%>
<%@page import="org.cerberus.entity.BuildRevisionInvariant"%>
<%@page import="org.cerberus.entity.Application"%>
<%@page import="org.cerberus.service.impl.BuildRevisionInvariantService"%>
<%@page import="org.cerberus.service.IBuildRevisionInvariantService"%>
<%@page import="org.cerberus.service.impl.ApplicationService"%>
<%@page import="org.cerberus.service.IApplicationService"%>
<%@page import="org.cerberus.service.IParameterService"%>
<%@page import="org.cerberus.refactor.GeneratePerformanceString"%>
<%@page import="org.cerberus.log.MyLogger"%>
<%@page import="org.cerberus.util.SqlUtil"%>
<% Date DatePageStart = new Date();%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>

        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link rel="stylesheet" type="text/css" href="css/crb_style.css">
        <link rel="shortcut icon" type="image/x-icon" href="images/favicon.ico" />
        <link rel="stylesheet" type="text/css" href="js/jqplot/jquery.jqplot.min.css" >
        <link rel="stylesheet" type="text/css" href="js/jqplot/syntaxhighlighter/styles/shCoreDefault.min.css" >
        <link rel="stylesheet" type="text/css" href="js/jqplot/syntaxhighlighter/styles/shThemejqPlot.min.css" >
        <script type="text/javascript" src="js/jquery-1.9.1.min.js"></script>  
        <script type="text/javascript" src="js/jquery-migrate-1.2.1.min.js"></script>
        <script type="text/javascript" src="js/jqplot/jquery.jqplot.min.js"></script>
        <script type="text/javascript" src="js/jqplot/plugins/jqplot.dateAxisRenderer.js"></script>
        <script type="text/javascript" src="js/jqplot/plugins/jqplot.cursor.min.js"></script>
        <script type="text/javascript" src="js/jqplot/plugins/jqplot.highlighter.js"></script>
        <script type="text/javascript" src="js/jqplot/plugins/jqplot.enhancedLegendRenderer.js"></script>
        <script type="text/javascript" src="js/jqplot/plugins/jqplot.json2.js"></script>
        <script type="text/javascript" src="js/jqplot/plugins/jqplot.canvasTextRenderer.min.js"></script>
        <script type="text/javascript" src="js/jqplot/plugins/jqplot.canvasAxisLabelRenderer.min.js"></script>	
        <script type="text/javascript" src="js/jqplot/plugins/jqplot.canvasAxisTickRenderer.js"></script>
        <script type="text/javascript" src="js/jqplot/plugins/jqplot.barRenderer.js"></script>

        <title>Execution Detail List</title>


    </head>
    <body>

        <%@ include file="include/function.jsp" %>
        <%@ include file="include/header.jsp" %>

        <div id="body">
            <%
                Connection conn = null;
                try {
                    conn = db.connect();

                    IParameterService myParameterService = appContext.getBean(IParameterService.class);
                    IApplicationService applicationService = appContext.getBean(ApplicationService.class);
                    IBuildRevisionInvariantService buildRevisionInvariantService = appContext.getBean(BuildRevisionInvariantService.class);

                    /*
                     * Filter requests
                     */
                    Statement stmt0 = conn.createStatement();
                    Statement stmt1 = conn.createStatement();
                    Statement stmt2 = conn.createStatement();
                    Statement stmt3 = conn.createStatement();


                    /*
                     * Get values if post parameters sended
                     */
                    String MySystem = request.getAttribute("MySystem").toString();
                    if (request.getParameter("system") != null && request.getParameter("system").compareTo("") != 0) {
                        MySystem = request.getParameter("system");
                    }

                    String test = "";
                    if (request.getParameter("test") != null) {
                        test = request.getParameter("test");
                    }
                    ResultSet rs_test_filter = stmt1.executeQuery("SELECT Test, Active FROM test ");

                    String testCase = "";
                    if (request.getParameter("testcase") != null) {
                        testCase = request.getParameter("testcase");
                    }
                    String TC_SQL = "";
                    ResultSet rs_testCase_filter = null;
                    if (test.equalsIgnoreCase("") == false) {
                        TC_SQL = "SELECT tc.TestCase, tc.Description, tc.Application, tc.tcactive FROM testcase tc ";
                        TC_SQL += " WHERE tc.Test = '" + test + "'";
                        rs_testCase_filter = stmt2.executeQuery(TC_SQL);
                    }

                    String country = "";
                    if (request.getParameter("country") != null) {
                        country = request.getParameter("country");
                    }

                    String build = "";
                    if (request.getParameter("build") != null) {
                        build = request.getParameter("build");
                    }

                    String revision = "";
                    if (request.getParameter("revision") != null) {
                        revision = request.getParameter("revision");
                    }

                    String environment = "";
                    if (request.getParameter("environment") != null) {
                        environment = request.getParameter("environment");
                    }

                    String controlStatus = "";
                    if (request.getParameter("controlStatus") != null) {
                        controlStatus = request.getParameter("controlStatus");
                    }

                    String systemFlt = "";
                    if (request.getParameter("systemFlt") != null) {
                        systemFlt = request.getParameter("systemFlt");
                    } else {
                        if (!(request.getAttribute("MySystem").toString().equalsIgnoreCase(""))) {
                            systemFlt = request.getAttribute("MySystem").toString();
                        }
                    }

                    String Application = "";
                    if (request.getParameter("application") != null) {
                        Application = request.getParameter("application");
                    }
                    ResultSet rs_application_filter = stmt3.executeQuery("SELECT DISTINCT Application, Description, system FROM application ORDER BY sort ");

                    String IP = "";
                    if (request.getParameter("IP") != null) {
                        IP = request.getParameter("IP");
                    }

                    String port = "";
                    if (request.getParameter("port") != null) {
                        port = request.getParameter("port");
                    }

                    String tag = "";
                    if (request.getParameter("tag") != null) {
                        tag = request.getParameter("tag");
                    }

                    String tcstatus = "";
                    if (request.getParameter("tcstatus") != null) {
                        tcstatus = request.getParameter("tcstatus");
                    }

                    String BAMExcluded = "";
                    if (request.getParameter("BAMExcluded") != null) {
                        BAMExcluded = request.getParameter("BAMExcluded");
                    }

                    String PerfExcluded = "";
                    if (request.getParameter("PerfExcluded") != null) {
                        PerfExcluded = request.getParameter("PerfExcluded");
                    }


                    int execmax = 100;
                    execmax = Integer.valueOf(myParameterService.findParameterByKey("cerberus_testexecutiondetailpage_nbmaxexe","").getValue());
                    int execmax_max = 100;
                    execmax_max = Integer.valueOf(myParameterService.findParameterByKey("cerberus_testexecutiondetailpage_nbmaxexe_max","").getValue());
                    if (request.getParameter("execmax") != null) {
                        if (Integer.valueOf(request.getParameter("execmax")) < execmax_max) {
                            execmax = Integer.valueOf(request.getParameter("execmax"));
                        } else {
                            execmax = execmax_max;
                        }
                    }

                    int minutemax = 0;
                    if (request.getParameter("minutemax") != null) {
                        minutemax = Integer.valueOf(request.getParameter("minutemax"));
                    }
                    // Calculating today - n minutes for the check.
                    Date aujourdhui = new Date(); // Getting now.
                    SimpleDateFormat formater = null; // Define the MySQL Format.
                    formater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                    Calendar cal = Calendar.getInstance();
                    cal.setTime(aujourdhui);
                    cal.add(Calendar.MINUTE, -minutemax);
                    String DateLimit = formater.format(cal.getTime());


                    int i = 0;
                    String optstyle = "";
            %>
            <table id="arrond">
                <tr>
                    <td id="arrond"><h3 style="color: blue">Filters</h3>
                        <form action="ExecutionDetailList.jsp" method="get" name="ExecFilters" id="ExecFilters">
                            <table>
                                <tr>
                                    <td>
                                        Test Case&nbsp;&nbsp;&nbsp;
                                        <select name="test" id="test" style="width: 200px;" OnChange ="document.ExecFilters.submit()">
                                            <option style="width: 200px" value="">-- Choose Test --</option>
                                            <%
                                                while (rs_test_filter.next()) {
                                                    if (rs_test_filter.getString("active").equalsIgnoreCase("Y")) {
                                                        optstyle = "font-weight:bold;";
                                                    } else {
                                                        optstyle = "font-weight:lighter;";
                                                    }
                                            %><option style="width: 200px;<%=optstyle%>" <%
                                                if (test.equalsIgnoreCase(rs_test_filter.getString("test"))) {
                                                    out.print("selected=\"selected\"");
                                                }
                                                    %> value="<%= rs_test_filter.getString("Test")%>"><%= rs_test_filter.getString("Test")%></option>
                                            <%
                                                }
                                                rs_test_filter.close();
                                            %>
                                        </select>&nbsp;&nbsp;&nbsp;
                                        <select name="testcase" id="testcase" style="width: 200px;" OnChange ="document.ExecFilters.submit()">
                                            <option style="width: 500px" value="">-- Choose Test Case --</option>
                                            <% if (rs_testCase_filter != null) {
                                                    while (rs_testCase_filter.next()) {
                                                        if (rs_testCase_filter.getString("tcactive").equalsIgnoreCase("Y")) {
                                                            optstyle = "font-weight:bold;";
                                                        } else {
                                                            optstyle = "font-weight:lighter;";
                                                        }
                                            %><option style="width: 500px;<%=optstyle%>" <%
                                                if (testCase.equalsIgnoreCase(rs_testCase_filter.getString("testcase"))) {
                                                    out.print("selected=\"selected\"");
                                                }
                                                    %> value="<%= rs_testCase_filter.getString("TestCase")%>"><%= rs_testCase_filter.getString("TestCase")%> [<%= rs_testCase_filter.getString("Application")%>] <%= rs_testCase_filter.getString("Description")%></option>
                                            <%
                                                    }
                                                    rs_testCase_filter.close();
                                                }
                                            %>
                                        </select>&nbsp;&nbsp;&nbsp;

                                        Country&nbsp;&nbsp;&nbsp;
                                        <%=ComboInvariant(conn, "country", "width: 50px", "Country", "Country", "COUNTRY", country, "document.ExecFilters.submit()", "")%>&nbsp;&nbsp;&nbsp;
                                        Environment&nbsp;&nbsp;&nbsp;
                                        <%=ComboInvariant(conn, "environment", "width: 60px", "Environment", "Environment", "ENVIRONMENT", environment, "document.ExecFilters.submit()", "")%>&nbsp;&nbsp;&nbsp;
                                        BAM Test Excluded&nbsp;:&nbsp;
                                        <% if (BAMExcluded.equalsIgnoreCase("1")) {%>
                                        <input type="checkbox" name="BAMExcluded" value="1" OnChange ="document.ExecFilters.submit()" checked><% } else {%>
                                        <input type="checkbox" name="BAMExcluded" value="1" OnChange ="document.ExecFilters.submit()">    
                                        <% }%>
                                        Performance Monitor Excluded&nbsp;:&nbsp;
                                        <% if (PerfExcluded.equalsIgnoreCase("1")) {%>
                                        <input type="checkbox" name="PerfExcluded" value="1" OnChange ="document.ExecFilters.submit()" checked><% } else {%>
                                        <input type="checkbox" name="PerfExcluded" value="1" OnChange ="document.ExecFilters.submit()">    
                                        <% }%>
                                        <br>Build&nbsp;&nbsp;&nbsp;
                                        <select id="Build" name="build" style="width: 60px" onchange="document.ExecFilters.submit()">
                                            <option style="width: 100px" value="" <%=build.compareTo("NONE") == 0 ? " SELECTED " : ""%>>----</option>
                                            <%
                                                List<BuildRevisionInvariant> listBuildRev = buildRevisionInvariantService.findAllBuildRevisionInvariantBySystemLevel(MySystem, 1);
                                                for (BuildRevisionInvariant myBR : listBuildRev) {
                                            %><option style="width: 100px" value="<%= myBR.getVersionName()%>" <%=build.compareTo(myBR.getVersionName()) == 0 ? " SELECTED " : ""%>><%= myBR.getVersionName()%></option>
                                            <% }
                                            %></select>&nbsp;&nbsp;&nbsp;
                                        Revision&nbsp;&nbsp;&nbsp;
                                        <select id="Revision" name="revision" style="width:50px" onchange="document.ExecFilters.submit()">
                                            <option style="width: 100px" value="" <%=revision.compareTo("NONE") == 0 ? " SELECTED " : ""%>>----</option>
                                            <%
                                                listBuildRev = buildRevisionInvariantService.findAllBuildRevisionInvariantBySystemLevel(MySystem, 2);
                                                for (BuildRevisionInvariant myBR : listBuildRev) {
                                            %><option style="width: 100px" value="<%= myBR.getVersionName()%>" <%=revision.compareTo(myBR.getVersionName()) == 0 ? " SELECTED " : ""%>><%= myBR.getVersionName()%></option>
                                            <% }
                                            %></select>&nbsp;&nbsp;&nbsp;
                                        Status&nbsp;&nbsp;&nbsp;
                                        <%=ComboInvariant(conn, "controlStatus", "width: 50px", "ControlStatus", "ControlStatus", "TCESTATUS", controlStatus, "document.ExecFilters.submit()", "")%>&nbsp;&nbsp;&nbsp;
                                        System&nbsp;&nbsp;&nbsp;
                                        <%=ComboInvariant(conn, "systemFlt", "width: 80px", "SystemFlt", "SystemFlt", "SYSTEM", systemFlt, "document.ExecFilters.submit()", "")%>&nbsp;&nbsp;&nbsp;
                                        Application&nbsp;&nbsp;&nbsp;
                                        <select name="application" id="application" style="width: 200px" OnChange ="document.ExecFilters.submit()">
                                            <option style="width: 500px" value="">-- Choose Application --</option>
                                            <% if (rs_application_filter != null) {
                                                    while (rs_application_filter.next()) {
                                            %><option style="width: 500px" <%
                                                if (Application.equalsIgnoreCase(rs_application_filter.getString("application"))) {
                                                    out.print("selected=\"selected\"");
                                                }
                                                    %> value="<%= rs_application_filter.getString("application")%>"><%= rs_application_filter.getString("application")%> [<%= rs_application_filter.getString("system")%>]</option>
                                            <%
                                                    }
                                                    rs_application_filter.close();
                                                }
                                            %>
                                        </select>&nbsp;&nbsp;&nbsp;
                                        Test Case Status on Execution&nbsp;&nbsp;&nbsp;
                                        <%=ComboInvariant(conn, "tcstatus", "width: 100px", "tcstatus", "tcstatus", "TCSTATUS", tcstatus, "document.ExecFilters.submit()", "")%>&nbsp;&nbsp;&nbsp;
                                        <br>
                                        IP&nbsp;&nbsp;&nbsp;<input style="font-weight: bold; width: 200px" name="IP" id="IP" value="<%=IP%>">
                                        port&nbsp;&nbsp;&nbsp;<input style="font-weight: bold; width: 200px" name="port" id="port" value="<%=port%>">
                                        Tag&nbsp;&nbsp;&nbsp;<input style="font-weight: bold; width: 200px" name="tag" id="tag" value="<%=tag%>">
                                        See exec from the last minutes&nbsp;&nbsp;&nbsp;
                                        <%=ComboInvariant(conn, "minutemax", "width: 50px", "minutemax", "minutemax", "EXECNBMIN", String.valueOf(minutemax), "document.ExecFilters.submit()", null)%>&nbsp;&nbsp;&nbsp;
                                        Max Nb of Exec returned&nbsp;&nbsp;&nbsp;
                                        <%=ComboInvariant(conn, "execmax", "width: 50px", "execmax", "execmax", "MAXEXEC", String.valueOf(execmax), "document.ExecFilters.submit()", null)%>&nbsp;&nbsp;&nbsp;
                                    </td>
                                    <td>
                                        <input type="submit" value="Apply">&nbsp;&nbsp;
                                    </td>
                                </tr>
                            </table>
                        </form>
                    </td>
                </tr>
            </table>
            <%

                /*
                 * Get Execution Information
                 */
                String ExeclistSQL = "";
                String ExeclistWhereSQL = "";
                ExeclistSQL = "SELECT tce.Id, tce.Test, tce.TestCase, tc.Description, "
                        + "tce.Build, tce.Revision, tce.Environment, tce.Country, tce.Browser, "
                        + "tce.Start, tce.End, tce.ControlStatus, tce.Application, "
                        + "tce.Ip, tce.URL, UNIX_TIMESTAMP(tce.End)-UNIX_TIMESTAMP(tce.Start) time_elapsed "
                        + " FROM "
                        + "(SELECT * FROM testcaseexecution tce "
                        + " WHERE 1=1 ";
                if (test.equalsIgnoreCase("") == false) {
                    ExeclistWhereSQL += " and tce.test='" + test + "'";
                }
                if (testCase.equalsIgnoreCase("") == false) {
                    ExeclistWhereSQL += " and tce.testcase='" + testCase + "'";
                }
                if (country.equalsIgnoreCase("") == false) {
                    ExeclistWhereSQL += " and tce.Country='" + country + "'";
                }
                if (build.equalsIgnoreCase("") == false) {
                    ExeclistWhereSQL += " and tce.Build='" + build + "'";
                }
                if (revision.equalsIgnoreCase("") == false) {
                    ExeclistWhereSQL += " and tce.Revision='" + revision + "'";
                }
                if (environment.equalsIgnoreCase("") == false) {
                    ExeclistWhereSQL += " and tce.Environment='" + environment + "'";
                }
                if (controlStatus.equalsIgnoreCase("") == false) {
                    ExeclistWhereSQL += " and tce.ControlStatus='" + controlStatus + "'";
                }
                if (systemFlt.equalsIgnoreCase("") == false) {
                    List<Application> appliList = applicationService.findApplicationBySystem(systemFlt);
                    String inSQL = SqlUtil.getInSQLClause(appliList);
                    if (!(inSQL.equalsIgnoreCase(""))) {
                        ExeclistWhereSQL += " and tce.Application ";
                        ExeclistWhereSQL += inSQL;
                    } else {
                        ExeclistWhereSQL += " and 1=0 ";
                    }
                }
                if (Application.equalsIgnoreCase("") == false) {
                    ExeclistWhereSQL += " and tce.Application='" + Application + "'";
                }
                if (IP.equalsIgnoreCase("") == false) {
                    ExeclistWhereSQL += " and tce.ip='" + IP + "'";
                }
                if (port.equalsIgnoreCase("") == false) {
                    ExeclistWhereSQL += " and tce.port='" + port + "'";
                }
                if (tag.equalsIgnoreCase("") == false) {
                    ExeclistWhereSQL += " and tce.tag='" + tag + "'";
                }
                if (PerfExcluded.equalsIgnoreCase("") == false) {
                    ExeclistWhereSQL += " and tce.test!='Performance Monitor'";
                }
                if (BAMExcluded.equalsIgnoreCase("") == false) {
                    ExeclistWhereSQL += " and tce.test!='Business Activity Monitor'";
                }
                if (tcstatus.equalsIgnoreCase("") == false) {
                    ExeclistWhereSQL += " and tce.status='" + tcstatus + "'";
                }
                if (minutemax > 0) {
                    ExeclistWhereSQL += " and tce.start>'" + DateLimit + "' ";
                }
                ExeclistSQL += ExeclistWhereSQL + " ORDER BY tce.id desc LIMIT " + execmax + " "
                        + ") tce "
                        + "LEFT OUTER JOIN testcase tc "
                        + " ON tc.test=tce.test AND tc.testcase=tce.testcase ";
                ResultSet rs_inf = stmt0.executeQuery(ExeclistSQL);

                String data = "";
                int j = 0;
                int jmax = 0;
                String title = "";
                rs_inf.last();
                jmax = rs_inf.getRow();
//                        out.print(ExeclistSQL);
                if (rs_inf.first()) {
                    Calendar cal_exestart = Calendar.getInstance();
                    Calendar cal_exeend = Calendar.getInstance();
                    cal_exeend.set(Integer.valueOf(rs_inf.getString("Start").substring(0, 4)), Integer.valueOf(rs_inf.getString("Start").substring(5, 7)), Integer.valueOf(rs_inf.getString("Start").substring(8, 10)), Integer.valueOf(rs_inf.getString("Start").substring(11, 13)), Integer.valueOf(rs_inf.getString("Start").substring(14, 16)), Integer.valueOf(rs_inf.getString("Start").substring(17, 19)));
            %>
            <table id="arrond">
                <tr>
                    <td valign="top"  width="20%"><%
                        do {
                            if (j == jmax / 5 || j == 2 * jmax / 5 || j == 3 * jmax / 5 || j == 4 * jmax / 5) {
                        %>
                    </td>
                    <td valign="top"  width="20%"><%                                    }
                        title = rs_inf.getString("test");
                        title += " - " + rs_inf.getString("testcase");
//                        title += " - " + rs_inf.getString("start");
                        title += " " + rs_inf.getString("description");
                        title += " - " + rs_inf.getString("country");
                        title += " - " + rs_inf.getString("environment");
                        if (!(rs_inf.getString("ControlStatus").equalsIgnoreCase("PE"))) {
                            title += " - " + rs_inf.getString("time_elapsed") + "s";
                        } else {
                            title += " - Pending...";
                        }%>
                        <a href="ExecutionDetail.jsp?id_tc=<%= rs_inf.getString("id")%>" title="<%= title%>"><%= rs_inf.getString("Start")%></a> <b><span class="<%= rs_inf.getString("ControlStatus")%>F"><%= rs_inf.getString("ControlStatus")%></span></b> [<%= rs_inf.getString("application")%>]<br><%
                                j++;
                                cal_exestart.set(Integer.valueOf(rs_inf.getString("Start").substring(0, 4)), Integer.valueOf(rs_inf.getString("Start").substring(5, 7)), Integer.valueOf(rs_inf.getString("Start").substring(8, 10)), Integer.valueOf(rs_inf.getString("Start").substring(11, 13)), Integer.valueOf(rs_inf.getString("Start").substring(14, 16)), Integer.valueOf(rs_inf.getString("Start").substring(17, 19)));

                            } while (rs_inf.next());%>
                    </td>
                </tr>
            </table>

            <table  id="arrond">
                <tr>
                    <td><b>Agregated Statistics<br><br><%= j%> Executions in <%= (cal_exeend.getTimeInMillis() - cal_exestart.getTimeInMillis()) / 60000%> minutes</b><br><%
                        long myDuration = (cal_exeend.getTimeInMillis() - cal_exestart.getTimeInMillis()) / 60000;
                        if (!(myDuration == 0)) {
                            out.print(j / (myDuration) + " Exec/m");
                        }
                            %></td>
                    <td>Detail per ROBOT IP</td>
                    <td><%
// Table to report the number of exe per Robot IP.
                        ExeclistSQL = "SELECT IP, count(*) c FROM ( "
                                + "SELECT tce.id, tce.application, tce.IP, tce.controlstatus  FROM testcaseexecution tce "
                                + "WHERE 1=1 "
                                + ExeclistWhereSQL
                                + "ORDER BY tce.id desc LIMIT " + execmax + " ) as toto "
                                + " GROUP by IP; ";
                        ResultSet rs_IPinf = stmt0.executeQuery(ExeclistSQL);
                        rs_IPinf.first();
                        %>
                        <table><%
                            do {
                            %>
                            <tr>
                                <td valign="top"><%= rs_IPinf.getString("IP")%></td>
                                <td valign="top"><%= rs_IPinf.getString("c")%></td>
                            </tr><%
                                } while (rs_IPinf.next());%>
                        </table>
                    </td>
                    <td>Detail per Application</td>
                    <td><%
// Table to report the number of exe per Application.
                        ExeclistSQL = "SELECT Application, count(*) c FROM ( "
                                + "SELECT tce.id, tce.application, tce.IP, tce.controlstatus  FROM testcaseexecution tce "
                                + "WHERE 1=1 "
                                + ExeclistWhereSQL
                                + "ORDER BY tce.id desc LIMIT " + execmax + " ) as toto "
                                + " GROUP by Application; ";
                        ResultSet rs_APinf = stmt0.executeQuery(ExeclistSQL);
                        rs_APinf.first();
                        %>
                        <table><%
                            do {
                            %>
                            <tr>
                                <td valign="top"><%= rs_APinf.getString("Application")%></td>
                                <td valign="top"><%= rs_APinf.getString("c")%></td>
                            </tr><%
                                } while (rs_APinf.next());%>
                        </table>
                    </td>
                    <td>Detail per Country</td>
                    <td><%
// Table to report the number of exe per Country.
                        ExeclistSQL = "SELECT Country, count(*) c FROM ( "
                                + "SELECT tce.id, tce.Country FROM testcaseexecution tce "
                                + "WHERE 1=1 "
                                + ExeclistWhereSQL
                                + "ORDER BY tce.id desc LIMIT " + execmax + " ) as toto "
                                + " GROUP by Country; ";
                        ResultSet rs_COinf = stmt0.executeQuery(ExeclistSQL);
                        rs_COinf.first();
                        %>
                        <table><%
                            do {
                            %>
                            <tr>
                                <td valign="top"><%= rs_COinf.getString("Country")%></td>
                                <td valign="top"><%= rs_COinf.getString("c")%></td>
                            </tr><%
                                } while (rs_COinf.next());%>
                        </table>
                    </td>
                    <td>Detail per ControlStatus</td>
                    <td><%
// Table to report the number of exe per ControlStatus.
                        ExeclistSQL = "SELECT ControlStatus, count(*) c FROM ( "
                                + "SELECT tce.id,  tce.controlstatus  FROM testcaseexecution tce "
                                + "WHERE 1=1 "
                                + ExeclistWhereSQL
                                + "ORDER BY tce.id desc LIMIT " + execmax + " ) as toto "
                                + " GROUP by ControlStatus; ";
                        ResultSet rs_CSinf = stmt0.executeQuery(ExeclistSQL);
                        rs_CSinf.first();
                        %>
                        <table><%
                            do {
                            %>
                            <tr>
                                <td valign="top"><%= rs_CSinf.getString("ControlStatus")%></td>
                                <td valign="top"><%= rs_CSinf.getString("c")%></td>
                            </tr><%
                                } while (rs_CSinf.next());%>
                        </table>
                    </td>
                </tr>
            </table>

            <%
                rs_IPinf.close();
                rs_APinf.close();
            } else {%>
            <br><b>no test cases execution found...</b>  
            <%}

                rs_inf.close();

                if (!test.equals("") && !testCase.equals("") && !country.equals("")) {

                    GeneratePerformanceString gps = new GeneratePerformanceString();
                    data = gps.gps(conn, test, testCase, country);

            %>
        </div>

        <input id="data" value="<%=data%>" style="display:none">
        <input id="testValue" value="<%=test%>" style="display:none">
        <input id="testCaseValue" value="<%=testCase%>" style="display:none">
        <input id="countryValue" value="<%=country%>" style="display:none">
        <table>
            <tr>
                <td style="width:1200px"><%=testCase%> 
                    <div id="chart" name="toto" style="height:200px; width:1200px; display:block" >
                    </div>
                    <div id="testchart" name="toto" style="height:200px; width:1200px; display:block" >
                    </div>
                </td>
            </tr>
        </table>
        <br>

        <script class="code" type="text/javascript">

            $(document).ready(function(){
                var input = window.document.getElementById("data").value.split("/d/");
                var maxValue = input[0];
                var dataList = input[1];
                var list1 = dataList.split("/k/");
                var datafin = new Array();
 
                for ( var k = 0 ; k < 2 ; k++ ){
                    var datas = list1[k].split("/p/");
                    var data2 = new Array();
                    for ( var c = 0 ; c < datas.length ; c++){
                        var data3 = new Array();
                        data3.push(datas[c].split(",")[0]);
                        data3.push(datas[c].split(",")[1]);
                        data3.push(datas[c].split(",")[2]);
                        data2.push(data3);
                    }
                    datafin.push(data2);
                }
                //alert(datafin);
                var plot = $.jqplot (  'chart' , datafin , {
                    title: 'TestCase Duration for <%=test%> <%=testCase%> () in <%=country%>' ,
                    legend: { show: true
                    },
    
    
                    grid: {
                        background: '#f3f3f3',
                        gridLineColor: '#accf9b'
                    },
                    cursor:{
                        show: true,
                        zoom:true,
                        showTooltip:false
                    } ,
                    axes: {
                        xaxis: { //customisation de l'axe x
                            renderer: $.jqplot.DateAxisRenderer
                        },
                        yaxis:{
                            min:0
                            //                        ,max:maxValue
                        }  
                    }
                    ,
                
                    axesDefaults:{useSeriesColor: false}
                    ,
                    series:[{showLine:false, markerOptions:{style:'filledDiamond'}, label :'OK'},
                        {showLine:false, markerOptions:{style:'filledDiamond'}, label:'KO'}],
                    seriesColors:["#22780F", "#ff5800"],
                    //cursor:{show:true, zoom:true, showTooltip:false}, 
                    axesDefaults:{useSeriesColor: false},
    
                    highlighter: { //vignette lors du survol des point caracteristique de la courbe
                        sizeAdjust: 10,
                        show:true,
                        tooltipLocation: 'ne',
                        useAxesFormatters: true,
                        formatString: '<b>%s >> %s seconds</b>'
                    }
                }); 

                $('#chart').bind('jqplotDataClick',
                function (ev, seriesIndex, pointIndex, datas) {
                    window.location.href='ExecutionDetail.jsp?id_tc='+datas[2];
                } );
            });
        </script>

        <script class="code" type="text/javascript">

            $(document).ready(function(){
       
                var test = document.getElementById("testValue").getAttribute("value");
                var testcase = document.getElementById("testCaseValue").getAttribute("value");
                var country = document.getElementById("countryValue").getAttribute("value");
       
                var ajaxDataRenderer = function(url, plot, options) {
                    var ret = null;
                    $.ajax({
                        async: false,
                        url: url,
                        dataType:"json",
                        success: function(data) {
                            ret = data;}
                    });
                    return ret;
                };
 
                // The url for our json data
                var jsonurl = "./TestCaseActionExecutionDetail?test="+test+"&testcase="+testcase+"&country="+country;
                //var legend = "./TestCaseActionExecutionDetail?test="+test+"&testcase="+testcase+"&country="+country;
  
                var plot2 = $.jqplot (  'testchart' , jsonurl ,  {
        
                    dataRenderer: ajaxDataRenderer,
                    stackSeries: true,
                    seriesDefaults:{
                        renderer:$.jqplot.BarRenderer,
                        rendererOptions: {
                            // Put a 30 pixel margin between bars.
                            barWidth: 5,
                            // Highlight bars when mouse button pressed.
                            // Disables default highlighting on mouse over.
                            highlightMouseDown: true   
                        },
                        pointLabels: {show: true}
                    },
     
                    title: 'Sequence Duration' ,
                    legend: {
                        renderer: $.jqplot.EnhancedLegendRenderer,
                        show:true,
                        location: 's',
                        placement:'outside',
                        yoffset: 30,
                        rendererOptions:{
                            numberRows: 2
                        }
                    },
    
                    grid: {
                        background: '#f3f3f3',
                        gridLineColor: '#accf9b'
                    },
                    cursor:{
                        show: true,
                        zoom:true,
                        showTooltip:false
                    } ,
                    axes: {
                        xaxis: { //customisation de l'axe x
                            renderer: $.jqplot.DateAxisRenderer
                        
                        },
                        yaxis:{
                            min:0,
                            tickOptions: { showMark: false
                                //                            , formatString: "%'d" 
                            }
                        }  
                    }
                    ,
                
                    axesDefaults:{useSeriesColor: false},
    
                    highlighter: { //vignette lors du survol des point caracteristique de la courbe
                        //sizeAdjust: 10,
                        show:true,
                        tooltipLocation: 'ne',
                        useAxesFormatters: true,
                        formatString: '<b>%s >> %s seconds</b>'
                    }
                }); 

            });
        </script>

        <%  }

                stmt0.close();
                stmt1.close();
                stmt2.close();
                stmt3.close();

            } catch (Exception e) {
                out.println("<br> error message : " + e.getMessage() + " " + e.toString() + "<br>");
                MyLogger.log("ExecutionDetail.jsp", Level.FATAL, " Exception catched." + e);
            } finally {
                try {
                    conn.close();
                } catch (Exception ex) {
                    MyLogger.log("ExecutionDetail.jsp", Level.FATAL, " Exception catched on close." + ex);
                }
            }

        %>
    </div>
    <br><% out.print(display_footer(DatePageStart));%>
</body>
</html>
