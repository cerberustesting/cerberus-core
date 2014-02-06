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
<%@page import="org.cerberus.util.StringUtil"%>
<%@page import="org.cerberus.entity.TestCaseExecutionSysVer"%>
<%@page import="org.cerberus.service.ITestCaseExecutionSysVerService"%>
<%@page import="org.cerberus.service.IApplicationService"%>
<%@page import="org.cerberus.service.IParameterService"%>
<%@page import="org.cerberus.util.DateUtil"%>
<%@page import="org.cerberus.entity.TestCaseStepActionControlExecution"%>
<%@page import="org.cerberus.service.ITestCaseStepActionControlExecutionService"%>
<%@page import="org.cerberus.entity.TestCaseStepActionExecution"%>
<%@page import="org.cerberus.service.ITestCaseStepActionExecutionService"%>
<%@page import="org.cerberus.service.ITestCaseStepExecutionService"%>
<%@page import="org.cerberus.entity.TestCaseStepExecution"%>
<%@page import="org.cerberus.util.ParameterParserUtil"%>
<%@page import="org.cerberus.entity.TestCaseExecutionData"%>
<%@page import="org.cerberus.service.ITestCaseExecutionDataService"%>
<%@page import="org.cerberus.refactor.TestcaseExecutionwwwSum"%>
<%@page import="org.cerberus.refactor.ITCEwwwSumService"%>
<%@page import="org.cerberus.log.MyLogger"%>
<%@page import="org.apache.log4j.Level"%>
<%@page import="org.springframework.web.context.support.WebApplicationContextUtils"%>
<%@page import="java.sql.Statement"%>
<%@page import="java.sql.ResultSet"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">

<% Date DatePageStart = new Date();%>
<html>
    <head>

        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link rel="stylesheet" type="text/css" href="css/crb_style.css">
        <link rel="shortcut icon" type="image/x-icon" href="images/favicon.ico" />
        <link rel="stylesheet" type="text/css" href="js/jqplot/jquery.jqplot.min.css" >
        <link type="text/css" rel="stylesheet" href="js/jqplot/syntaxhighlighter/styles/shCoreDefault.min.css" >
        <link type="text/css" rel="stylesheet" href="js/jqplot/syntaxhighlighter/styles/shThemejqPlot.min.css" >

        <title>Execution Detail</title>


    </head>
    <body>

        <%@ include file="include/function.jsp" %>
        <%@ include file="include/header.jsp" %>

        <script type="text/javascript" src="js/jqplot/jquery.min.js"></script>   
        <script type="text/javascript" src="js/jqplot/jquery.jqplot.min.js"></script>
        <script type="text/javascript" src="js/jqplot/plugins/jqplot.dateAxisRenderer.min.js"></script>
        <script type="text/javascript" src="js/jqplot/plugins/jqplot.cursor.min.js"></script>
        <script type="text/javascript" src="js/jqplot/plugins/jqplot.highlighter.min.js"></script>
        <script type="text/javascript" src="js/jqplot/plugins/jqplot.enhancedLegendRenderer.min.js"></script>
        <script type="text/javascript" src="js/jqplot/plugins/jqplot.json2.min.js"></script>
        <script type="text/javascript" src="js/jqplot/plugins/jqplot.canvasTextRenderer.min.js"></script>
        <script type="text/javascript" src="js/jqplot/plugins/jqplot.canvasAxisLabelRenderer.min.js"></script>
        <script type="text/javascript" src="js/jqplot/plugins/jqplot.canvasAxisTickRenderer.min.js"></script>
        <script type="text/javascript" src="js/jqplot/plugins/jqplot.barRenderer.min.js"></script>


        <div id="body">
            <%
                Connection conn = db.connect();
                try {


                    /*
                     * Filter requests
                     */
                    Statement stmt0 = conn.createStatement();

                    IParameterService myParameterService = appContext.getBean(IParameterService.class);
                    IApplicationService myApplicationService = appContext.getBean(IApplicationService.class);

                    String PictureURL;
                    String MyPictureURL;
                    PictureURL = myParameterService.findParameterByKey("cerberus_picture_url", "").getValue();

                    /*
                     * Manage Filters
                     */
                    long iD = ParameterParserUtil.parseLongParam(request.getParameter("id_tc"), 0);
                    String id_filter = "";
                    if (request.getParameter("id_tc") != null) {
                        id_filter = request.getParameter("id_tc");
                    }
                    String test = "";
                    String testCase = "";
                    String testCaseDesc = "";
                    String country = "";
                    String build = "";
                    String revision = "";

            %>
            <table id="arrond">
                <tr>
                    <td id="arrond"><h3 style="color: blue">Filters</h3>
                        <form action="ExecutionDetail.jsp" method="get" name="ExecFilters" id="ExecFilters">
                            Id &nbsp;&nbsp;&nbsp;
                            <input name="id_tc" id="id_tc" style="width: 350px" OnChange ="document.ExecFilters.submit()" value="<%= id_filter%>">
                            <input type="submit" value="Apply">
                        </form>
                    </td>
                </tr>
            </table>
            <%




                /*
                 * Get Execution Information
                 */
                ResultSet rs_inf = stmt0.executeQuery("SELECT tce.Id, tce.Test, tce.TestCase, tc.Description, "
                        + "tce.Build, tce.Revision, tce.Environment, tce.Country, tce.Browser, "
                        + "tce.Start, tce.End, tce.ControlStatus, tce.Application, "
                        + "tce.Ip, tce.URL, UNIX_TIMESTAMP(tce.End)-UNIX_TIMESTAMP(tce.Start) time_elapsed, "
                        + "tce.port, tce.tag, tce.verbose, tce.controlmessage, tce.status, tce.CrbVersion "
                        + " FROM testcaseexecution tce "
                        + " JOIN testcase tc "
                        + " ON tc.test=tce.test and tc.testcase=tce.testcase "
                        + " WHERE id = '" + id_filter + "'");

                String max_id = "-1";
                String data = "";
                String myApplication = "";
                String environment = "";


                if (rs_inf.first()) {

                    max_id = rs_inf.getString("Id");
                    myApplication = rs_inf.getString("Application");
                    testCase = rs_inf.getString("TestCase");
                    testCaseDesc = rs_inf.getString("Description");
                    country = rs_inf.getString("Country");
                    environment = rs_inf.getString("Environment");
                    build = rs_inf.getString("Build");
                    revision = rs_inf.getString("Revision");
                    IApplicationService applicationService = appContext.getBean(IApplicationService.class);
                    String appSystem = applicationService.findApplicationByKey(myApplication).getSystem();
            %>
            <br>
            <div id="table">

                <table id="arrond" style="text-align: left" border="1" >
                    <tr id="header" style="font-style: italic">
                        <td style="font-weight: bold; width: 140px"><%out.print(dbDocS(conn, "testcaseexecution", "id", "ID"));%></td>
                        <td style="font-weight: bold; width: 140px"><%out.print(dbDocS(conn, "test", "test", "Test"));%></td>
                        <td style="font-weight: bold; width: 140px"><%out.print(dbDocS(conn, "testcase", "testcase", "TestCase"));%></td>
                        <td style="font-weight: bold; width: 140px"><%out.print(dbDocS(conn, "invariant", "country", "Country"));%></td>
                        <td style="font-weight: bold; width: 140px"><%out.print(dbDocS(conn, "invariant", "environment", "Environment"));%></td>
                        <td style="font-weight: bold; width: 140px"><%out.print(dbDocS(conn, "page_executiondetail", "buildrevision", ""));%></td>
                        <td style="font-weight: bold; width: 140px"><%out.print(dbDocS(conn, "page_executiondetail", "buildrevisionlink", ""));%></td>
                        <td style="font-weight: bold; width: 140px"><%out.print(dbDocS(conn, "application", "Application", "Application"));%></td>
                        <td style="font-weight: bold; width: 140px"><%out.print(dbDocS(conn, "testcaseexecution", "URL", "URL"));%></td>
                        <td style="font-weight: bold; width: 140px"><%out.print(dbDocS(conn, "testcaseexecution", "IP", "Ip"));
                            out.print(" / ");
                            out.print(dbDocS(conn, "testcaseexecution", "port", "Port"));%></td>
                        <td style="font-weight: bold; width: 140px"><%out.print(dbDocS(conn, "testcaseexecution", "browser", "Browser"));%></td>
                        <td style="font-weight: bold; width: 140px"><%out.print(dbDocS(conn, "testcaseexecution", "start", "Start"));%></td>
                        <td style="font-weight: bold; width: 140px"><%out.print(dbDocS(conn, "testcaseexecution", "end", "End"));%></td>
                        <td style="font-weight: bold; width: 140px"><%out.print(dbDocS(conn, "testcaseexecution", "controlstatus", "ControlStatus"));%></td>
                    </tr>            
                    <tr>

                        <td><span id="exeid"><%= rs_inf.getString("Id")%></span></td>
                        <td id="testValue"><b><%= test = rs_inf.getString("Test")%></b></td>
                        <td id="testcaseValue"><b><%= testCase%></b><br><%= testCaseDesc%></td>
                        <td id="countryValue"><b><%= country%></b></td>
                        <td><b><%= environment%></b></td>
                        <td>[<%=appSystem%>]<br><%= rs_inf.getString("Build")%> / <%= rs_inf.getString("Revision")%></td>
                        <td>
                            <table>
                                <%
                                    ITestCaseExecutionSysVerService testCaseExecutionSysVerService = appContext.getBean(ITestCaseExecutionSysVerService.class);
                                    List<TestCaseExecutionSysVer> listSysVer = testCaseExecutionSysVerService.findTestCaseExecutionSysVerById(Long.valueOf(rs_inf.getString("Id")));
                                    for (TestCaseExecutionSysVer mySysVer : listSysVer) {
                                        if (!(appSystem.equals(mySysVer.getSystem()))) {
                                %>
                                <tr>
                                    <td>
                                        [<%= mySysVer.getSystem()%>]<br><%= mySysVer.getBuild()%> / <%= mySysVer.getRevision()%>
                                    </td>
                                </tr>
                                <%
                                        }
                                    }
                                %>
                            </table>
                        </td>
                        <td><%= rs_inf.getString("Application")%></td>
                        <td><%= rs_inf.getString("URL")%></td>
                        <td><span id="exeip"><%= rs_inf.getString("Ip")%></span> / <span id="exeport"><%= rs_inf.getString("port")%></span></td>
                        <td><%= rs_inf.getString("Browser")%></td>
                        <td><%= rs_inf.getString("Start")%></td>
                        <%
                            // If status is pending, there will be no end timestamp feeded 
                            // and we should not even try to display it.%>
                        <% if (rs_inf.getString("ControlStatus").equalsIgnoreCase("OK")) {
                        %><td><%= rs_inf.getString("End")%></td>
                        <td class="OK"><a class="OKF"><span id="res_status"><%= rs_inf.getString("ControlStatus")%></span></a><br><a style="color :green"><span id="res_elapsedtime"><%= rs_inf.getString("time_elapsed")%></span> s</a></td><%
                        } else if (rs_inf.getString("ControlStatus").equalsIgnoreCase("KO")) {
                            %><td><%= rs_inf.getString("End")%></td>
                        <td class="KO"><a class="KOF"><span id="res_status"><%= rs_inf.getString("ControlStatus")%></span></a></td><%
                        } else if (rs_inf.getString("ControlStatus").equalsIgnoreCase("NA")) {
                        %><td><%= rs_inf.getString("End")%></td>
                        <td class="NA"><a class="NAF"><span id="res_status"><%= rs_inf.getString("ControlStatus")%></span></a></td><%
                        } else if (rs_inf.getString("ControlStatus").equalsIgnoreCase("FA")) {
                        %><td><%= rs_inf.getString("End")%></td>
                        <td class="FA"><a class="FAF"><span id="res_status"><%= rs_inf.getString("ControlStatus")%></span></a></td><%
                        } else if (rs_inf.getString("ControlStatus").equalsIgnoreCase("PE")) {
                        %><td>...</td>
                        <td class="PE"><a class="PEF"><span id="res_status"><%= rs_inf.getString("ControlStatus")%></span></a></td><%
                        } else {
                        %><td><%= rs_inf.getString("End")%></td>
                        <td><span id="res_status"><%= rs_inf.getString("ControlStatus")%></span></td><%
                            }%>
                    </tr>
                    <tr id="header" style="font-style: italic">
                        <td style="font-weight: bold; width: 140px" colspan=3><%out.print(dbDocS(conn, "testcaseexecution", "tag", "Tag"));%></td>
                        <td style="font-weight: bold; width: 140px" colspan=8><%out.print(dbDocS(conn, "testcaseexecution", "controlmessage", "Message"));%></td>
                        <td style="font-weight: bold; width: 140px"><%out.print(dbDocS(conn, "testcaseexecution", "verbose", "Verbose"));%></td>
                        <td style="font-weight: bold; width: 140px"><%out.print(dbDocS(conn, "testcaseexecution", "status", "Status"));%></td>
                        <td style="font-weight: bold; width: 140px"><%out.print(dbDocS(conn, "testcaseexecution", "crbversion", "Engine Version"));%></td>
                    </tr>
                    <tr>
                        <td colspan=3><span id="exetag"><%= rs_inf.getString("tag") == null ? "" : rs_inf.getString("tag")%></span></td>
                        <td colspan=8><span id="exemsg"><%= rs_inf.getString("ControlMessage") == null ? "" : rs_inf.getString("ControlMessage")%></span></td>
                        <td><span id="exeverbose"><%= rs_inf.getString("verbose") == null ? "" : rs_inf.getString("verbose")%></span></td>
                        <td><span id="exestatus"><%= rs_inf.getString("status") == null ? "" : rs_inf.getString("status")%></span></td>
                        <td><span id="execrbversion"><%= rs_inf.getString("crbversion") == null ? "" : rs_inf.getString("crbversion")%></span></td>
                    </tr>
                </table>
            </div>

            <br/><br/>
            <div id="tablewwwsum">
                <%

                    ITCEwwwSumService tcewwwsumService = appContext.getBean(ITCEwwwSumService.class);

                    List<TestcaseExecutionwwwSum> tcewwwsumdetails = tcewwwsumService.getAllDetailsFromTCEwwwSum(Integer.valueOf(id_filter));
                    if (tcewwwsumdetails != null) {
                        for (TestcaseExecutionwwwSum wwwsumdetails : tcewwwsumdetails) {
                %>
                <div id="tablewwwsum">

                    <table id="arrond" style="text-align: left" border="1" >
                        <tr id="header" style="font-style: italic">
                            <td style="font-weight: bold; width: 140px" colspan="2"><%out.print(dbDocS(conn, "testcaseexecutionwwwsum", "tot_nbhits", "tot_nbhits"));%></td>
                            <td style="font-weight: bold; width: 140px" colspan="2"><%out.print(dbDocS(conn, "testcaseexecutionwwwsum", "tot_tps", "tot_tps"));%></td>
                            <td style="font-weight: bold; width: 140px" colspan="2"><%out.print(dbDocS(conn, "testcaseexecutionwwwsum", "tot_size", "tot_size"));%></td>
                            <td style="font-weight: bold; width: 140px" colspan="2"><%out.print(dbDocS(conn, "testcaseexecutionwwwsum", "nb_rc2xx", "nb_rc2xx"));%></td>
                            <td style="font-weight: bold; width: 140px" colspan="2"><%out.print(dbDocS(conn, "testcaseexecutionwwwsum", "nb_rc3xx", "nb_rc3xx"));%></td>
                            <td style="font-weight: bold; width: 140px" colspan="2"><%out.print(dbDocS(conn, "testcaseexecutionwwwsum", "nb_rc4xx", "nb_rc4xx"));%></td>
                            <td style="font-weight: bold; width: 140px" colspan="2"><%out.print(dbDocS(conn, "testcaseexecutionwwwsum", "nb_rc5xx", "nb_rc5xx"));%></td>
                            <td style="font-weight: bold; width: 140px" colspan="2"><%out.print(dbDocS(conn, "testcaseexecutionwwwsum", "img_nb", "img_nb"));%></td>
                            <td style="font-weight: bold; width: 140px" colspan="2"><%out.print(dbDocS(conn, "testcaseexecutionwwwsum", "img_tps", "img_tps"));%></td>
                            <td style="font-weight: bold; width: 140px" colspan="2"><%out.print(dbDocS(conn, "testcaseexecutionwwwsum", "img_size_tot", "img_size_tot"));%></td>
                            <td style="font-weight: bold; width: 140px" colspan="2"><%out.print(dbDocS(conn, "testcaseexecutionwwwsum", "img_size_max", "img_size_max"));%></td>
                            <td style="font-weight: bold; width: 140px" colspan="2"><%out.print(dbDocS(conn, "testcaseexecutionwwwsum", "js_nb", "js_nb"));%></td>
                            <td style="font-weight: bold; width: 140px" colspan="2"><%out.print(dbDocS(conn, "testcaseexecutionwwwsum", "js_tps", "js_tps"));%></td>
                            <td style="font-weight: bold; width: 140px" colspan="2"><%out.print(dbDocS(conn, "testcaseexecutionwwwsum", "js_size_tot", "js_size_tot"));%></td>
                            <td style="font-weight: bold; width: 140px" colspan="2"><%out.print(dbDocS(conn, "testcaseexecutionwwwsum", "js_size_max", "js_size_max"));%></td>
                            <td style="font-weight: bold; width: 140px" colspan="2"><%out.print(dbDocS(conn, "testcaseexecutionwwwsum", "css_nb", "css_nb"));%></td>
                            <td style="font-weight: bold; width: 140px" colspan="2"><%out.print(dbDocS(conn, "testcaseexecutionwwwsum", "css_tps", "css_tps"));%></td>
                            <td style="font-weight: bold; width: 140px" colspan="2"><%out.print(dbDocS(conn, "testcaseexecutionwwwsum", "css_size_tot", "css_size_tot"));%></td>
                            <td style="font-weight: bold; width: 140px" colspan="2"><%out.print(dbDocS(conn, "testcaseexecutionwwwsum", "css_size_max", "css_size_max"));%></td>
                        </tr>            
                        <tr>

                            <td style="width:120px"><span id="tot_nbhits"><%= wwwsumdetails.getTot_nbhits()%></span></td>
                            <td style="width:20px"><input style="display:inline; height:20px; width:20px;  color: orange; font-weight:bolder" class="smallbutton" type="button" value="h" title="Show historic data" onclick="javascript:popupGraph('GenerateGraph?test=<%=test%>&testcase=<%=testCase%>&country=<%=country%>&parameter=tot_nbhits')"></td>
                            <td style="width:120px"><span id="tot_tps"><%= wwwsumdetails.getTot_tps()%></span></td>
                            <td style="width:20px"><input style="display:inline; height:20px; width:20px;  color: orange; font-weight:bolder" class="smallbutton" type="button" value="h" title="Show historic data" onclick="javascript:popupGraph('GenerateGraph?test=<%=test%>&testcase=<%=testCase%>&country=<%=country%>&parameter=tot_tps')"></td>
                            <td style="width:120px"><span id="tot_size"><%= wwwsumdetails.getTot_size()%></span></td>
                            <td style="width:20px"><input style="display:inline; height:20px; width:20px;  color: orange; font-weight:bolder" class="smallbutton" type="button" value="h" title="Show historic data" onclick="javascript:popupGraph('GenerateGraph?test=<%=test%>&testcase=<%=testCase%>&country=<%=country%>&parameter=tot_size')"></td>
                            <td style="width:120px"><span id="nb_rc2xx"><%= wwwsumdetails.getNb_rc2xx()%></span></td>
                            <td style="width:20px"><input style="display:inline; height:20px; width:20px;  color: orange; font-weight:bolder" class="smallbutton" type="button" value="h" title="Show historic data" onclick="javascript:popupGraph('GenerateGraph?test=<%=test%>&testcase=<%=testCase%>&country=<%=country%>&parameter=nb_rc2xx')"></td>
                            <td style="width:120px"><span id="nb_rc3xx"><%= wwwsumdetails.getNb_rc3xx()%></span></td>
                            <td style="width:20px"><input style="display:inline; height:20px; width:20px;  color: orange; font-weight:bolder" class="smallbutton" type="button" value="h" title="Show historic data" onclick="javascript:popupGraph('GenerateGraph?test=<%=test%>&testcase=<%=testCase%>&country=<%=country%>&parameter=nb_rc3xx')"></td>
                            <td style="width:120px"><span id="nb_rc4xx"><%= wwwsumdetails.getNb_rc4xx()%></span></td>
                            <td style="width:20px"><input style="display:inline; height:20px; width:20px;  color: orange; font-weight:bolder" class="smallbutton" type="button" value="h" title="Show historic data" onclick="javascript:popupGraph('GenerateGraph?test=<%=test%>&testcase=<%=testCase%>&country=<%=country%>&parameter=nb_rc4xx')"></td>
                            <td style="width:120px"><span id="nb_rc5xx"><%= wwwsumdetails.getNb_rc5xx()%></span></td>
                            <td style="width:20px"><input style="display:inline; height:20px; width:20px;  color: orange; font-weight:bolder" class="smallbutton" type="button" value="h" title="Show historic data" onclick="javascript:popupGraph('GenerateGraph?test=<%=test%>&testcase=<%=testCase%>&country=<%=country%>&parameter=nb_rc5xx')"></td>
                            <td style="width:120px"><span id="img_nb"><%= wwwsumdetails.getImg_nb()%></span></td>
                            <td style="width:20px"><input style="display:inline; height:20px; width:20px;  color: orange; font-weight:bolder" class="smallbutton" type="button" value="h" title="Show historic data" onclick="javascript:popupGraph('GenerateGraph?test=<%=test%>&testcase=<%=testCase%>&country=<%=country%>&parameter=img_nb')"></td>
                            <td style="width:120px"><span id="img_tps"><%= wwwsumdetails.getImg_tps()%></span></td>
                            <td style="width:20px"><input style="display:inline; height:20px; width:20px;  color: orange; font-weight:bolder" class="smallbutton" type="button" value="h" title="Show historic data" onclick="javascript:popupGraph('GenerateGraph?test=<%=test%>&testcase=<%=testCase%>&country=<%=country%>&parameter=img_tps')"></td>
                            <td style="width:120px"><span id="img_size_tot"><%= wwwsumdetails.getImg_size_tot()%></span></td>
                            <td style="width:20px"><input style="display:inline; height:20px; width:20px;  color: orange; font-weight:bolder" class="smallbutton" type="button" value="h" title="Show historic data" onclick="javascript:popupGraph('GenerateGraph?test=<%=test%>&testcase=<%=testCase%>&country=<%=country%>&parameter=img_size_tot')"></td>
                            <td style="width:120px"><span id="img_size_max"><%= wwwsumdetails.getImg_size_max()%></span></td>
                            <td style="width:20px"><input style="display:inline; height:20px; width:20px;  color: orange; font-weight:bolder" class="smallbutton" type="button" value="h" title="Show historic data" onclick="javascript:popupGraph('GenerateGraph?test=<%=test%>&testcase=<%=testCase%>&country=<%=country%>&parameter=img_size_max')"></td>
                            <td style="width:120px"><span id="js_nb"><%= wwwsumdetails.getJs_nb()%></span></td>
                            <td style="width:20px"><input style="display:inline; height:20px; width:20px;  color: orange; font-weight:bolder" class="smallbutton" type="button" value="h" title="Show historic data" onclick="javascript:popupGraph('GenerateGraph?test=<%=test%>&testcase=<%=testCase%>&country=<%=country%>&parameter=js_nb')"></td>
                            <td style="width:120px"><span id="js_tps"><%= wwwsumdetails.getJs_tps()%></span></td>
                            <td style="width:20px"><input style="display:inline; height:20px; width:20px;  color: orange; font-weight:bolder" class="smallbutton" type="button" value="h" title="Show historic data" onclick="javascript:popupGraph('GenerateGraph?test=<%=test%>&testcase=<%=testCase%>&country=<%=country%>&parameter=js_tps')"></td>
                            <td style="width:120px"><span id="js_size_tot"><%= wwwsumdetails.getJs_size_tot()%></span></td>
                            <td style="width:20px"><input style="display:inline; height:20px; width:20px;  color: orange; font-weight:bolder" class="smallbutton" type="button" value="h" title="Show historic data" onclick="javascript:popupGraph('GenerateGraph?test=<%=test%>&testcase=<%=testCase%>&country=<%=country%>&parameter=js_size_tot')"></td>
                            <td style="width:120px"><span id="js_size_max"><%= wwwsumdetails.getJs_size_max()%></span></td>
                            <td style="width:20px"><input style="display:inline; height:20px; width:20px;  color: orange; font-weight:bolder" class="smallbutton" type="button" value="h" title="Show historic data" onclick="javascript:popupGraph('GenerateGraph?test=<%=test%>&testcase=<%=testCase%>&country=<%=country%>&parameter=js_size_max')"></td>
                            <td style="width:120px"><span id="css_nb"><%= wwwsumdetails.getCss_nb()%></span></td>
                            <td style="width:20px"><input style="display:inline; height:20px; width:20px;  color: orange; font-weight:bolder" class="smallbutton" type="button" value="h" title="Show historic data" onclick="javascript:popupGraph('GenerateGraph?test=<%=test%>&testcase=<%=testCase%>&country=<%=country%>&parameter=css_nb')"></td>
                            <td style="width:120px"><span id="css_tps"><%= wwwsumdetails.getCss_tps()%></span></td>
                            <td style="width:20px"><input style="display:inline; height:20px; width:20px;  color: orange; font-weight:bolder" class="smallbutton" type="button" value="h" title="Show historic data" onclick="javascript:popupGraph('GenerateGraph?test=<%=test%>&testcase=<%=testCase%>&country=<%=country%>&parameter=css_tps')"></td>
                            <td style="width:120px"><span id="css_size_tot"><%= wwwsumdetails.getCss_size_tot()%></span></td>
                            <td style="width:20px"><input style="display:inline; height:20px; width:20px;  color: orange; font-weight:bolder" class="smallbutton" type="button" value="h" title="Show historic data" onclick="javascript:popupGraph('GenerateGraph?test=<%=test%>&testcase=<%=testCase%>&country=<%=country%>&parameter=css_size_tot')"></td>
                            <td style="width:120px"><span id="css_size_max"><%= wwwsumdetails.getCss_size_max()%></span></td>
                            <td style="width:20px"><input style="display:inline; height:20px; width:20px;  color: orange; font-weight:bolder" class="smallbutton" type="button" value="h" title="Show historic data" onclick="javascript:popupGraph('GenerateGraph?test=<%=test%>&testcase=<%=testCase%>&country=<%=country%>&parameter=css_size_max')"></td>
                        </tr>

                    </table>
                    <br><br>
                    <%String verbose = rs_inf.getString("tce.verbose") == null ? "0" : rs_inf.getString("tce.verbose");
                        if (verbose.equals("2")) {%>
                    <input type="button" value ="Show Network Trafic Detail" onclick="javascript:popupNT('NetworkTraficDetail.jsp?id=<%=id_filter%>')">
                    <%}
                            }
                        }%>
                </div>
                <div id="resultDiv">
                    <%

                        appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());

                        ITestCaseExecutionDataService testCaseExecutionDataService = appContext.getBean(ITestCaseExecutionDataService.class);

                        List<TestCaseExecutionData> dataList = testCaseExecutionDataService.findTestCaseExecutionDataById(iD);%>
                    <table id="dataTable" class="arrondTable">
                        <%
                            for (TestCaseExecutionData myData : dataList) {
                        %>
                        <tr>
                            <td><%=DateUtil.getFormatedDate(myData.getStartLong())%></td>
                            <td><%=DateUtil.getFormatedElapsed(myData.getStartLong(), myData.getEndLong())%></td>
                            <td><b><span id="PROP-<%=myData.getProperty()%>"><%=myData.getProperty()%></span></b></td>
                            <td><b><i><span id="PROPVAL-<%=myData.getProperty()%>"><%=myData.getValue()%></span></i></b></td>
                            <td style="font-size: x-small"><%=myData.getType()%></td>
                            <td style="font-size: x-small"><%=myData.getObject()%></td>
                            <td class="<%=myData.getRC()%>"><span class="<%=myData.getRC()%>F" id="PROPSTS-<%=myData.getProperty()%>"><%=myData.getRC()%></span></td>
                            <td><span id="PROPMES-<%=myData.getProperty()%>"><%=myData.getrMessage()%></span></td>
                        </tr>
                        <%
                            }
                            String myStep = "";
                            String myAction = "";
                            Integer myKey = 0;
                        %>                        
                    </table>
                    <br><br>
                    <%

                        ITestCaseStepExecutionService testCaseStepExecutionService = appContext.getBean(ITestCaseStepExecutionService.class);

                        List<TestCaseStepExecution> stepList = testCaseStepExecutionService.findTestCaseStepExecutionById(iD);%>
                    <table id="stepTable" class="arrondTable">
                        <%
                            for (TestCaseStepExecution myStepData : stepList) {
                                myKey++;
                        %>
                        <tr>
                            <td><%=DateUtil.getFormatedDate(myStepData.getFullStart())%></td>
                            <td><%=DateUtil.getFormatedDate(myStepData.getFullEnd())%></td>
                            <td><%=myStepData.getTest()%></td>
                            <td><%=myStepData.getTestCase()%></td>
                            <td><%=myStepData.getStep()%></td>
                            <td class="<%=myStepData.getReturnCode()%>"><a class="<%=myStepData.getReturnCode()%>F"><%=myStepData.getReturnCode()%></a></td>
                            <td><%=myStepData.getTimeElapsed()%> s</td>
                        </tr>
                        <tr>
                            <td colspan="8">
                                <%
                                    ITestCaseStepActionExecutionService testCaseStepActionExecutionService = appContext.getBean(ITestCaseStepActionExecutionService.class);

                                    List<TestCaseStepActionExecution> actionList = testCaseStepActionExecutionService.findTestCaseStepActionExecutionByCriteria(iD, myStepData.getTest(), myStepData.getTestCase(), myStepData.getStep());%>
                                <table id="actionTable" >
                                    <%
                                        myStep = String.valueOf(myKey);
                                        for (TestCaseStepActionExecution myActionData : actionList) {%>
                                    <tr>
                                        <td>&nbsp;&nbsp;&nbsp;&nbsp;</td>
                                        <td><%=DateUtil.getFormatedDate(myActionData.getStartLong())%></td>
                                        <td><%=DateUtil.getFormatedElapsed(myActionData.getStartLong(), myActionData.getEndLong())%></td>
                                        <td><%=myActionData.getSequence()%></td>
                                        <td><b><%=myActionData.getAction()%></b></td>
                                        <td><%=myActionData.getObject()%></td>
                                        <td><%=myActionData.getProperty()%></td>
                                        <td class="<%=myActionData.getReturnCode()%>"><span class="<%=myActionData.getReturnCode()%>F" id="ACTSTS-<%=myStep + "-" + myActionData.getSequence()%>"><%=myActionData.getReturnCode()%></span></td>
                                        <td><i><span id="ACTMES-<%=myStep + "-" + myActionData.getSequence()%>"><%=myActionData.getReturnMessage()%></span></i></td>
                                        <td><%if (myActionData.getScreenshotFilename() != null) {%>
                                            <a href="<%=PictureURL%><%=myActionData.getScreenshotFilename()%>" id="ACTIMG-<%=myStep + "-" + myActionData.getSequence()%>">img</a>
                                            <%}%>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td colspan="9">
                                            <%

                                                ITestCaseStepActionControlExecutionService testCaseStepActionControlExecutionService = appContext.getBean(ITestCaseStepActionControlExecutionService.class);

                                                List<TestCaseStepActionControlExecution> controlList = testCaseStepActionControlExecutionService.findTestCaseStepActionControlExecutionByCriteria(iD, myActionData.getTest(), myActionData.getTestCase(), myActionData.getStep(), myActionData.getSequence());%>
                                            <table id="controlTable" >
                                                <%
                                                    myAction = myStep + "-" + myActionData.getSequence();

                                                    for (TestCaseStepActionControlExecution myControlData : controlList) {%>
                                                <tr>
                                                    <td>&nbsp;&nbsp;&nbsp;&nbsp;</td>
                                                    <td><%=DateUtil.getFormatedDate(myControlData.getStartLong())%></td>
                                                    <td><%=DateUtil.getFormatedElapsed(myControlData.getStartLong(), myControlData.getEndLong())%></td>
                                                    <td><%=myControlData.getControl()%></td>
                                                    <td><b><%=myControlData.getControlType()%></b></td>
                                                    <td><%=myControlData.getControlProperty()%></td>
                                                    <td><%=myControlData.getControlValue()%></td>
                                                    <td><%=myControlData.getFatal()%></td>
                                                    <td class="<%=myControlData.getReturnCode()%>"><span class="<%=myControlData.getReturnCode()%>F" id="CTLSTS-<%=myAction + "-" + myControlData.getControl()%>"><%=myControlData.getReturnCode()%></span></td>
                                                    <td><i><span id="CTLMES-<%=myAction + "-" + myControlData.getControl()%>"><%=myControlData.getReturnMessage()%></span></i></td>
                                                    <td><%if (myControlData.getScreenshotFilename() != null) {%>
                                                        <a href="<%=PictureURL%><%=myControlData.getScreenshotFilename()%>">img</a>
                                                        <%}%>
                                                    </td>
                                                </tr>
                                                <%
                                                    }

                                                %>
                                            </table>
                                        </td>
                                    </tr>
                                    <%
                                        }

                                    %>
                                </table>
                                <br>
                                <%
                                    }

                                %>
                            </td>
                        </tr>                        
                    </table>
                    <%



//                            GeneratePerformanceString gps = new GeneratePerformanceString();
//                            data = gps.gps(conn, test, testCase, country);
                        data = "";

                    } else {
                    %>
                    <br><br><table id="arrond" style="text-align: left" border="1" >
                        <tr id="header" style="font-style: italic">
                            <td style="font-weight: bold; width: 140px"><b><i>Execution ID not found...</i></b></td>
                        </tr>
                    </table>
                    <%                        }
                        rs_inf.close();

                        if (!data.equals("")) {
                    %>
                </div><input id="data" value="<%=data%>" style="display:none">
                <table>
                    <tr>
                        <td style="width:1200px"><%=testCase%> 
                            <div id="chart" name="chart" style="height:200px; width:1200px; display:block" >
                            </div>
                            <div id="testchart" name="testchart" style="height:200px; width:1200px; display:block" >
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
                            title: 'TestCase Duration' ,
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
       
                        var test = document.getElementById("testValue").innerHTML;
                        var testcase = document.getElementById("testcaseValue").innerHTML;
                        var country = document.getElementById("countryValue").innerHTML;
       
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
                        var legend = "./TestCaseActionExecutionDetail?test="+test+"&testcase="+testcase+"&country="+country;
  
                        var plot2 = $.jqplot (  'testchart' , jsonurl ,  {
        
                            dataRenderer: ajaxDataRenderer,
                            stackSeries: true,
                            seriesDefaults:{
                                renderer:$.jqplot.BarRenderer,
                                rendererOptions: {
                                    barWidth: 5,
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
                                xaxis: { 
                                    renderer: $.jqplot.DateAxisRenderer
                                },
                                yaxis:{
                                    min:0,
                                    tickOptions: { showMark: false
                                    }
                                }
                            }
                            ,
                
                            axesDefaults:{useSeriesColor: false},
    
                            highlighter: { 
                                show:true,
                                tooltipLocation: 'ne',
                                useAxesFormatters: true,
                                formatString: '<b>%s >> %s seconds</b>'
                            }
                        }); 

                    });
                </script>

                <%  }
                %>
                <br><br>
                <%  if (!(myApplication.equalsIgnoreCase(""))) {
                %>
                <h4>Contextual Actions</h4>
                <table id="arrond" style="text-align: left" border="1" >
                    <tr>
                        <td>
                            <a href="RunTests.jsp?Test=<%=test%>&TestCase=<%=testCase%>&Country=<%=country%>">Run the same Test Case again.</a>
                        </td>
                        <td>
                            <a href="TestCase.jsp?Test=<%=test%>&TestCase=<%=testCase%>&Load=Load">Modify the Test Case.</a>
                        </td>
                        <td>
                            <%
                                String newBugURL = myApplicationService.findApplicationByKey(myApplication).getBugTrackerNewUrl();
                                if (StringUtil.isNullOrEmpty(newBugURL))  {
  %>
                            <a href="javascript:void(0)" title="Define the New Bug URL at the application level in order to open a bug from here.">Open a bug.</a> 
<%                              } else {
                                    newBugURL = newBugURL.replaceAll("%EXEID%", id_filter);
                                    newBugURL = newBugURL.replaceAll("%TEST%", test);
                                    newBugURL = newBugURL.replaceAll("%TESTCASE%", testCase);
                                    newBugURL = newBugURL.replaceAll("%TESTCASEDESC%", testCaseDesc);
                                    newBugURL = newBugURL.replaceAll("%COUNTRY%", country);
                                    newBugURL = newBugURL.replaceAll("%ENV%", environment);
                                    newBugURL = newBugURL.replaceAll("%BUILD%", build);
                                    newBugURL = newBugURL.replaceAll("%REV%", revision);%>
                            <a href="<%= newBugURL%>" target='_blank' title="title">Open a bug.</a> 
<%                                }

                            %>
                        </td>
                    </tr>
                </table>
                <%  }
                %>


                <%
                        stmt0.close();

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
