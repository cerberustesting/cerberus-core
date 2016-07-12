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
<%@page import="org.json.JSONException"%>
<%@page import="org.json.JSONObject"%>
<%@page import="org.apache.commons.lang3.StringEscapeUtils"%>
<%@page import="org.cerberus.crud.entity.TestCaseExecutionwwwSum"%>
<%@page import="org.cerberus.crud.service.ITestCaseExecutionwwwSumService"%>
<%@page import="org.cerberus.crud.entity.TestCaseExecution"%>
<%@page import="org.cerberus.crud.service.ITestCaseExecutionService"%>
<%@page import="java.sql.Statement"%>
<%@page import="java.sql.ResultSet"%>
<%@page import="org.apache.log4j.Level"%>
<%@page import="org.springframework.web.context.support.WebApplicationContextUtils"%>
<%@page import="org.cerberus.crud.entity.TestCaseStepActionControl"%>
<%@page import="org.cerberus.crud.entity.TestCaseStepAction"%>
<%@page import="org.cerberus.crud.entity.TestCaseStep"%>
<%@page import="org.cerberus.crud.entity.TCase"%>
<%@page import="org.cerberus.crud.entity.TestCase"%>
<%@page import="org.cerberus.crud.entity.TestCaseExecutionSysVer"%>
<%@page import="org.cerberus.crud.entity.TestCaseStepActionControlExecution"%>
<%@page import="org.cerberus.crud.entity.TestCaseStepActionExecution"%>
<%@page import="org.cerberus.crud.entity.TestCaseStepExecution"%>
<%@page import="org.cerberus.crud.entity.TestCaseExecutionData"%>
<%@page import="org.cerberus.crud.service.IDocumentationService"%>
<%@page import="org.cerberus.crud.service.ITestCaseStepActionControlService"%>
<%@page import="org.cerberus.crud.service.ITestCaseStepActionService"%>
<%@page import="org.cerberus.crud.service.ITestCaseStepService"%>
<%@page import="org.cerberus.crud.service.ITestCaseService"%>
<%@page import="org.cerberus.crud.service.ITestCaseExecutionSysVerService"%>
<%@page import="org.cerberus.crud.service.IApplicationService"%>
<%@page import="org.cerberus.crud.service.IParameterService"%>
<%@page import="org.cerberus.crud.service.ITestCaseStepActionControlExecutionService"%>
<%@page import="org.cerberus.crud.service.ITestCaseStepActionExecutionService"%>
<%@page import="org.cerberus.crud.service.ITestCaseStepExecutionService"%>
<%@page import="org.cerberus.crud.service.ITestCaseExecutionDataService"%>
<%@page import="org.cerberus.util.StringUtil"%>
<%@page import="org.cerberus.util.DateUtil"%>
<%@page import="org.cerberus.util.ParameterParserUtil"%>
<%@page import="org.cerberus.log.MyLogger"%>
<% Date DatePageStart = new Date();%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>

        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="refresh" HTTP-EQUIV="Refresh">
        <link rel="stylesheet" type="text/css" href="css/crb_style.css">
        <link rel="shortcut icon" type="image/x-icon" href="images/favicon.ico" />
        <link rel="stylesheet" type="text/css" href="css/jquery-ui.css" >
        <link rel="stylesheet" type="text/css" href="js/jqplot/jquery.jqplot.min.css" >
        <link type="text/css" rel="stylesheet" href="js/jqplot/syntaxhighlighter/styles/shCoreDefault.min.css" >
        <link type="text/css" rel="stylesheet" href="js/jqplot/syntaxhighlighter/styles/shThemejqPlot.min.css" >
        <link type="text/css" rel="stylesheet" href="js/zoombox/zoombox.css" >
        <script type="text/javascript" src="js/Form.js"></script>
        <script type="text/javascript" src="js/jquery-1.9.1.min.js"></script>
        <script type="text/javascript" src="js/jquery-migrate-1.2.1.min.js"></script>
        <script type="text/javascript" src="js/jquery-ui-1.10.2.js"></script>
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
        <script type="text/javascript" src="js/zoombox/zoombox.js"></script>
        <script type="text/javascript" src="js/diff_match_patch/diff_match_patch.js"></script>
        <title>Execution Detail</title>


    </head>
    <body>

        <%@ include file="include/function.jsp" %>
        <%@ include file="include/header.jsp" %>



        <div id="body">
            <%
                IDocumentationService docService = appContext.getBean(IDocumentationService.class);

                /*
                     * Filter requests
                 */
                IParameterService myParameterService = appContext.getBean(IParameterService.class);
                IApplicationService myApplicationService = appContext.getBean(IApplicationService.class);
                ITestCaseService testCaseService = appContext.getBean(ITestCaseService.class);
                ITestCaseExecutionService testCaseExecutionService = appContext.getBean(ITestCaseExecutionService.class);

                String PictureURL = myParameterService.findParameterByKey("cerberus_picture_url", "").getValue();

                String myLang = request.getAttribute("MyLang").toString();

                /*
                     * Manage Filters
                 */
                String id_filter = "";
                long iD;
                if (request.getParameter("id_tc").equalsIgnoreCase("last")) {
                    // Getting the last execution.
                    TestCaseExecution last_exe;
                    last_exe = testCaseExecutionService.convert(testCaseExecutionService.readLastByCriteria(null));
                    id_filter = String.valueOf(last_exe.getId());
                    iD = last_exe.getId();
                } else {
                    iD = ParameterParserUtil.parseLongParam(request.getParameter("id_tc"), 0);
                    if (request.getParameter("id_tc") != null) {
                        id_filter = request.getParameter("id_tc");
                    }
                }
                String test = "";
                String testCase = "";
                String testCaseDesc = "";
                String country = "";
                String build = "";
                String revision = "";
                String browser = "";
                String browserFullVersion = "";
                String exedate = "";
                String appSystem = "";
            %>
            <div class="filters" style="float:left; width:100%; height:30px">
                <div style="float:left; width:100px"><p class="dttTitle">Filters</p></div>
                <div style="float:left; width:1000px;font-weight: bold;">
                    <form action="ExecutionDetail.jsp" method="get" name="ExecFilters" id="ExecFilters">
                        Id &nbsp;&nbsp;&nbsp;
                        <input name="id_tc" id="id_tc" style="width: 350px" OnChange ="document.ExecFilters.submit()" value="<%= id_filter%>">
                        <input type="submit" value="Apply">
                    </form>
                </div>
            </div>
            <%

                /*
                 * Get Execution Information
                 */
                TestCaseExecution testCaseExecution = testCaseExecutionService.findTCExecutionByKey(Long.parseLong(id_filter));

                if (testCaseExecution != null) {
                    test = testCaseExecution.getTest();
                    testCase = testCaseExecution.getTestCase();

                    TCase tCase = testCaseService.findTestCaseByKey(test, testCase);

                    String max_id = String.valueOf(testCaseExecution.getId());

                    String myApplication = tCase.getApplication();
                    String environment = testCaseExecution.getEnvironment();

                    String comment = tCase.getComment();
                    String bugid = tCase.getBugID();
                    String newBugURL = "";
                    String executor = testCaseExecution.getExecutor();
                    String controlStatus = testCaseExecution.getControlStatus();

                    testCaseDesc = tCase.getShortDescription();
                    country = testCaseExecution.getCountry();

                    build = testCaseExecution.getBuild();
                    revision = testCaseExecution.getRevision();
                    browser = testCaseExecution.getBrowser();
                    exedate = DateUtil.getFormatedMySQLTimestamp(testCaseExecution.getStart());

                    if (executor == null) {
                        executor = "";
                    }
                    browserFullVersion = testCaseExecution.getBrowserFullVersion();

                    String tcGroup = tCase.getGroup();

                    newBugURL = myApplicationService.convert(myApplicationService.readByKey(myApplication)).getBugTrackerNewUrl();
                    if (!StringUtil.isNullOrEmpty(newBugURL)) {
                        newBugURL = newBugURL.replace("%EXEID%", id_filter);
                        newBugURL = newBugURL.replace("%EXEDATE%", exedate);
                        newBugURL = newBugURL.replace("%TEST%", test);
                        newBugURL = newBugURL.replace("%TESTCASE%", testCase);
                        newBugURL = newBugURL.replace("%TESTCASEDESC%", testCaseDesc);
                        newBugURL = newBugURL.replace("%COUNTRY%", country);
                        newBugURL = newBugURL.replace("%ENV%", environment);
                        newBugURL = newBugURL.replace("%BUILD%", build);
                        newBugURL = newBugURL.replace("%REV%", revision);
                        newBugURL = newBugURL.replace("%BROWSER%", browser);
                        newBugURL = newBugURL.replace("%BROWSERFULLVERSION%", browserFullVersion);
                    }

            %>
            <div style="clear:both" id="table">
                <br>

                <input id="statushidden" value="<%=controlStatus%>" hidden="hidden">
                <table class="tableBorder wrapAll" style="text-align: left" border="1" >
                    <tr id="header" style="font-style: italic">
                        <td style="font-weight: bold; width: 7%"><%out.print(docService.findLabelHTML("testcaseexecution", "id", "ID", myLang));%></td>
                        <td style="font-weight: bold; width: 7%"><%out.print(docService.findLabelHTML("test", "test", "Test", myLang));%></td>
                        <td style="font-weight: bold; width: 9%"><%out.print(docService.findLabelHTML("testcase", "testcase", "TestCase", myLang));%></td>
                        <td style="font-weight: bold; width: 7%"><%out.print(docService.findLabelHTML("invariant", "country", "Country", myLang));%></td>
                        <td style="font-weight: bold; width: 7%"><%out.print(docService.findLabelHTML("invariant", "environment", "Environment", myLang));%></td>
                        <td style="font-weight: bold; width: 7%"><%out.print(docService.findLabelHTML("page_executiondetail", "buildrevision", "", myLang));%></td>
                        <td style="font-weight: bold; width: 7%"><%out.print(docService.findLabelHTML("page_executiondetail", "buildrevisionlink", "", myLang));%></td>
                        <td style="font-weight: bold; width: 7%"><%out.print(docService.findLabelHTML("application", "Application", "Application", myLang));%></td>
                        <td style="font-weight: bold; width: 7%"><%out.print(docService.findLabelHTML("testcaseexecution", "URL", "URL", myLang));%></td>
                        <td style="font-weight: bold; width: 7%"><%out.print(docService.findLabelHTML("testcaseexecution", "IP", "Ip", myLang));
                            out.print("<br>");
                            out.print(docService.findLabelHTML("testcaseexecution", "port", "Port", myLang));%></td>
                        <td style="font-weight: bold; width: 7%"><%out.print(docService.findLabelHTML("testcaseexecution", "browser", "Browser", myLang));
                            out.print("<br>[");
                            out.print(docService.findLabelHTML("testcaseexecution", "browserFullVersion", "", myLang));%>]</td>
                        <td style="font-weight: bold; width: 7%"><%out.print(docService.findLabelHTML("testcaseexecution", "start", "Start", myLang));%></td>
                        <td style="font-weight: bold; width: 7%"><%out.print(docService.findLabelHTML("testcaseexecution", "end", "End", myLang));%></td>
                        <td style="font-weight: bold; width: 7%"><%out.print(docService.findLabelHTML("testcaseexecution", "controlstatus", "ControlStatus", myLang));%></td>
                    </tr>            
                    <tr>

                        <td><span id="exeid"><%= max_id%></span></td>
                        <td id="testValue"><b><%= test%></b></td>
                        <td id="testcaseValue"><b><%= testCase%></b><br><%= testCaseDesc%></td>
                        <td id="countryValue"><b><%= country%></b></td>
                        <td><b><%= environment%></b></td>
                        <td>[<%=appSystem%>]<br><%= build%> / <%= revision%></td>
                        <td>
                            <table>
                                <%
                                    ITestCaseExecutionSysVerService testCaseExecutionSysVerService = appContext.getBean(ITestCaseExecutionSysVerService.class);
                                    List<TestCaseExecutionSysVer> listSysVer = testCaseExecutionSysVerService.findTestCaseExecutionSysVerById(testCaseExecution.getId());
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
                        <td><%= myApplication%></td>
                        <td><%= testCaseExecution.getUrl()%></td>
                        <td><span id="exeip"><%= testCaseExecution.getIp()%></span><br><span id="exeport"><%= testCaseExecution.getPort()%></span></td>
                        <td><span id="exebrowser"><%= browser%></span><br>[<span id="exebrowserver"><%= browserFullVersion%></span>]</td>
                        <td><%= exedate%></td>
                        <%
                            // If status is pending, there will be no end timestamp feeded 
                            // and we should not even try to display it.
                            if ("PE".equalsIgnoreCase(controlStatus)) {
                        %><td>...</td><%
                        } else {
                        %><td><%= DateUtil.getFormatedMySQLTimestamp(testCaseExecution.getEnd())%></td><%
                            }
                        %><td class="<%=controlStatus%>"><a class="<%=controlStatus%>F"><span id="res_status"><%=controlStatus%></span></a></td>
                    </tr>
                    <tr style="font-style: italic">
                        <td style="font-weight: bold;" colspan=2><%out.print(docService.findLabelHTML("testcaseexecution", "tag", "Tag", myLang));%></td>
                        <td style="font-weight: bold;">Link to ExecutionPerTag</td>
                        <td style="font-weight: bold;" colspan=6><%out.print(docService.findLabelHTML("testcaseexecution", "controlmessage", "Message", myLang));%></td>
                        <td style="font-weight: bold;"><%out.print(docService.findLabelHTML("page_runtests", "ScreenSize", "ScreenSize", myLang));%></td>
                        <td style="font-weight: bold;"><%out.print(docService.findLabelHTML("testcaseexecution", "Executor", "Executor", myLang));%></td>
                        <td style="font-weight: bold;"><%out.print(docService.findLabelHTML("testcaseexecution", "verbose", "Verbose", myLang));%></td>
                        <td style="font-weight: bold;"><%out.print(docService.findLabelHTML("testcaseexecution", "status", "Status", myLang));%></td>
                        <td style="font-weight: bold;"><%out.print(docService.findLabelHTML("testcaseexecution", "crbversion", "Engine Version", myLang));%></td>
                    </tr>
                    <tr>
                        <td colspan=2><span id="exetag"><code><pre><%= testCaseExecution.getTag() == null ? "" : StringEscapeUtils.escapeHtml4(testCaseExecution.getTag())%>
                                                                </pre> 
                                </code><input type="button" name="editTag" value="edit" onclick="openChangeTagPopin('<%=id_filter%>')">
                            </span>
                                                                
                        </td>
                        <%
                            String tagEncoded = testCaseExecution.getTag();
                            if (testCaseExecution.getTag() != null) {
                                tagEncoded = StringUtil.encodeAsJavaScriptURIComponent(testCaseExecution.getTag());
                            }
                        %>
                        <td><span id="exetaglink"><%= testCaseExecution.getTag() == null ? ""
                                : "<a href='ReportingExecutionByTag.jsp?enc=1&Tag=" + tagEncoded.replace("'", "%27") + "'>link</a>"%></span></td>
                        <td colspan=6><span id="exemsg"><%= testCaseExecution.getControlMessage() == null ? "" : testCaseExecution.getControlMessage()%></span></td>
                        <td><span id="exescreensize"><%=testCaseExecution.getScreenSize()%></span></td>
                        <td><span id="exemsg"><%=executor%></span></td>
                        <td><span id="exeverbose"><%= String.valueOf(testCaseExecution.getVerbose())%></span></td>
                        <td><span id="exestatus"><%= testCaseExecution.getStatus() == null ? "" : testCaseExecution.getStatus()%></span></td>
                        <td><span id="execrbversion"><%= testCaseExecution.getCrbVersion() == null ? "" : testCaseExecution.getCrbVersion()%></span></td>
                    </tr>
                    <tr style="font-style: italic">
                        <td style="font-weight: bold;" colspan=9><%out.print(docService.findLabelHTML("testcase", "Comment", "Comment", myLang));%></td>
                        <td style="font-weight: bold;" colspan=1><%out.print(docService.findLabelHTML("page_executiondetail", "SeleniumLog", "Selenium Log", myLang));%></td>
                        <td style="font-weight: bold;" colspan=4><%out.print(docService.findLabelHTML("testcase", "BugID", "Bug ID", myLang));%></td>
                    </tr>
                    <tr>
                        <td colspan=9>
                            <span id="comment"><%= comment == null ? "" : comment%></span>
                            <br>
                            <a href="TestCase.jsp?Test=<%=test%>&TestCase=<%=testCase%>&Load=Load&Tinf=Y">Modify the Test Case comment.</a>
                        </td>
                        <td colspan=1>
                            <span id="seleniumLog">
                                <a href="<%=PictureURL + max_id + "/"%>selenium_log.txt">Logs</a>
                            </span>
                        </td>
                        <td colspan=4><span id="bugid"><%
                            if (StringUtil.isNullOrEmpty(bugid)) {
                                %><a href="<%= newBugURL%>" target='_blank' title="title">Open a bug.</a><%
                                } else {

                                    String bugURL = myApplicationService.convert(myApplicationService.readByKey(myApplication)).getBugTrackerUrl();
                                    if (StringUtil.isNullOrEmpty(bugURL)) {
                                %><%=bugid%><%
                                } else {
                                    bugURL = bugURL.replace("%BUGID%", bugid);
                                %><a href="<%= bugURL%>" target='_blank' title="title"><%=bugid%></a><%
                                        }
                                    }
                                %>
                        </td>
                    </tr>
                </table>
            </div>
            <input type="button" value="Stop Auto Refresh" onclick="document.getElementById('refreshAuto').value = 'Stop'">
            <br/><br/>
            <div id="tablewwwsum">
                <%
                    ITestCaseExecutionwwwSumService tcewwwsumService = appContext.getBean(ITestCaseExecutionwwwSumService.class);

                    List<TestCaseExecutionwwwSum> tcewwwsumdetails = tcewwwsumService.getAllDetailsFromTCEwwwSum(Integer.valueOf(id_filter));
                    if (tcewwwsumdetails != null) {
                        for (TestCaseExecutionwwwSum wwwsumdetails : tcewwwsumdetails) {
                %>
                <div id="tablewwwsum">

                    <table class="tableBorder" style="text-align: left" border="1" >
                        <tr id="header" style="font-style: italic">
                            <td style="font-weight: bold; width: 140px" colspan="2"><%out.print(docService.findLabelHTML("testcaseexecutionwwwsum", "tot_nbhits", "tot_nbhits", myLang));%></td>
                            <td style="font-weight: bold; width: 140px" colspan="2"><%out.print(docService.findLabelHTML("testcaseexecutionwwwsum", "tot_tps", "tot_tps", myLang));%></td>
                            <td style="font-weight: bold; width: 140px" colspan="2"><%out.print(docService.findLabelHTML("testcaseexecutionwwwsum", "tot_size", "tot_size", myLang));%></td>
                            <td style="font-weight: bold; width: 140px" colspan="2"><%out.print(docService.findLabelHTML("testcaseexecutionwwwsum", "nb_rc2xx", "nb_rc2xx", myLang));%></td>
                            <td style="font-weight: bold; width: 140px" colspan="2"><%out.print(docService.findLabelHTML("testcaseexecutionwwwsum", "nb_rc3xx", "nb_rc3xx", myLang));%></td>
                            <td style="font-weight: bold; width: 140px" colspan="2"><%out.print(docService.findLabelHTML("testcaseexecutionwwwsum", "nb_rc4xx", "nb_rc4xx", myLang));%></td>
                            <td style="font-weight: bold; width: 140px" colspan="2"><%out.print(docService.findLabelHTML("testcaseexecutionwwwsum", "nb_rc5xx", "nb_rc5xx", myLang));%></td>
                            <td style="font-weight: bold; width: 140px" colspan="2"><%out.print(docService.findLabelHTML("testcaseexecutionwwwsum", "img_nb", "img_nb", myLang));%></td>
                            <td style="font-weight: bold; width: 140px" colspan="2"><%out.print(docService.findLabelHTML("testcaseexecutionwwwsum", "img_tps", "img_tps", myLang));%></td>
                            <td style="font-weight: bold; width: 140px" colspan="2"><%out.print(docService.findLabelHTML("testcaseexecutionwwwsum", "img_size_tot", "img_size_tot", myLang));%></td>
                            <td style="font-weight: bold; width: 140px" colspan="2"><%out.print(docService.findLabelHTML("testcaseexecutionwwwsum", "img_size_max", "img_size_max", myLang));%></td>
                            <td style="font-weight: bold; width: 140px" colspan="2"><%out.print(docService.findLabelHTML("testcaseexecutionwwwsum", "js_nb", "js_nb", myLang));%></td>
                            <td style="font-weight: bold; width: 140px" colspan="2"><%out.print(docService.findLabelHTML("testcaseexecutionwwwsum", "js_tps", "js_tps", myLang));%></td>
                            <td style="font-weight: bold; width: 140px" colspan="2"><%out.print(docService.findLabelHTML("testcaseexecutionwwwsum", "js_size_tot", "js_size_tot", myLang));%></td>
                            <td style="font-weight: bold; width: 140px" colspan="2"><%out.print(docService.findLabelHTML("testcaseexecutionwwwsum", "js_size_max", "js_size_max", myLang));%></td>
                            <td style="font-weight: bold; width: 140px" colspan="2"><%out.print(docService.findLabelHTML("testcaseexecutionwwwsum", "css_nb", "css_nb", myLang));%></td>
                            <td style="font-weight: bold; width: 140px" colspan="2"><%out.print(docService.findLabelHTML("testcaseexecutionwwwsum", "css_tps", "css_tps", myLang));%></td>
                            <td style="font-weight: bold; width: 140px" colspan="2"><%out.print(docService.findLabelHTML("testcaseexecutionwwwsum", "css_size_tot", "css_size_tot", myLang));%></td>
                            <td style="font-weight: bold; width: 140px" colspan="2"><%out.print(docService.findLabelHTML("testcaseexecutionwwwsum", "css_size_max", "css_size_max", myLang));%></td>
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
                    <%if (testCaseExecution.getVerbose() == 2) {%>
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

                    <%
                        String myStep = "";
                        String myAction = "";
                        Integer myKey = 0;

                        ITestCaseStepExecutionService testCaseStepExecutionService = appContext.getBean(ITestCaseStepExecutionService.class);
                        ITestCaseStepService testCaseStepService = appContext.getBean(ITestCaseStepService.class);

                        List<TestCaseStepExecution> stepList = testCaseStepExecutionService.findTestCaseStepExecutionById(iD);%>
                    <p class="dttTitle">TestCase Execution Result</p>
                    <table id="stepTable" class="tableBorder" style="border-collapse: collapse">
                        <%
                            for (TestCaseStepExecution myStepData : stepList) {
                                if (!myStepData.getTest().equals("Pre Testing")) {
                                    myKey++;
                                }
                                TestCaseStep myTCS;
                                myTCS = testCaseStepService.findTestCaseStep(myStepData.getTest(), myStepData.getTestCase(), myStepData.getStep());
                                String myTCSDesc = "";
                                if (!(myTCS == null)) {
                                    myTCSDesc = myTCS.getDescription();
                                }
                                String styleMainTestCase1 = "";
                                String styleMainTestCase2 = "";
                                if ((myStepData.getTest().equals(test)) && (myStepData.getTestCase().equals(testCase))) {
                                    styleMainTestCase1 = "<b>";
                                    styleMainTestCase2 = "</b>";
                                }
                                String conditionalDisplay = "inline";
                                String conditionalHidden = "none";
                                if (myStepData.getReturnCode().equals("OK")) {
                                    conditionalDisplay = "none";
                                    conditionalHidden = "inline";
                                }

                                String stepIdentifier = myStepData.getTest() + myStepData.getTestCase() + myStepData.getStep();

                        %>
                        <tr class="tableHeader">
                            <td style="width:20px" class="<%=myStepData.getReturnCode()%>"><a class="<%=myStepData.getReturnCode()%>F"><%=myStepData.getReturnCode()%></a></td>
                            <td align="left"><%=styleMainTestCase1%><%=myTCSDesc%><%=styleMainTestCase2%>    (  
                                <%=myStepData.getTimeElapsed()%> s )
                                <a id="dropDownUpArrow<%=stepIdentifier%>" style="display:<%=conditionalDisplay%>"
                                   onclick="javascript:switchTableVisibleInvisible('dropDownDownArrow<%=stepIdentifier%>', 'dropDownUpArrow<%=stepIdentifier%>');
                                           switchTableVisibleInvisible('dropDownDownArrow<%=stepIdentifier%>', 'actionTable<%=stepIdentifier%>');
                                           "><img src="images/dropdownup.gif"/></a>

                                <a id="dropDownDownArrow<%=stepIdentifier%>" style="display:<%=conditionalHidden%>"
                                   onclick="javascript:switchTableVisibleInvisible('actionTable<%=stepIdentifier%>', 'dropDownDownArrow<%=stepIdentifier%>');
                                           switchTableVisibleInvisible('dropDownUpArrow<%=stepIdentifier%>', 'dropDownDownArrow<%=stepIdentifier%>')"><img src="images/dropdown.gif"/></a>
                            </td>
                            <td align="right"><a href="./TestCase.jsp?Test=<%=myStepData.getTest()%>&TestCase=<%=myStepData.getTestCase()%>&Load=Load#stepAnchor_<%=myKey%>"><%=DateUtil.getFormatedDate(myStepData.getFullStart())%>  >>  
                                    <%=DateUtil.getFormatedDate(myStepData.getFullEnd())%> (
                                    <%=styleMainTestCase1%><%=myStepData.getTest()%><%=styleMainTestCase2%> / 
                                    <%=styleMainTestCase1%><%=myStepData.getTestCase()%><%=styleMainTestCase2%> / 
                                    <%=styleMainTestCase1%>Step <%=myStepData.getSort()%><%=styleMainTestCase2%> )
                                </a>
                            </td>
                        </tr>
                        <tr>
                            <td colspan="9">
                                <%
                                    ITestCaseStepActionExecutionService testCaseStepActionExecutionService = appContext.getBean(ITestCaseStepActionExecutionService.class);
                                    ITestCaseStepActionService testCaseStepActionService = appContext.getBean(ITestCaseStepActionService.class);

                                    List<TestCaseStepActionExecution> actionList = testCaseStepActionExecutionService.findTestCaseStepActionExecutionByCriteria(iD, myStepData.getTest(), myStepData.getTestCase(), myStepData.getStep());%>
                                <table id="actionTable<%=stepIdentifier%>"  style="border-collapse: collapse; display:<%=conditionalDisplay%>; width:100%">
                                    <%
                                        myStep = String.valueOf(myKey);
                                        for (TestCaseStepActionExecution myActionData : actionList) {
//                                  %>
                                    <tr class="tableContent">
                                        <td style="width:1%">&nbsp;&nbsp;&nbsp;&nbsp;</td>
                                        <td title="Action Return Code" style="width:1%" class="<%=myActionData.getReturnCode()%>"><span class="<%=myActionData.getReturnCode()%>F" id="ACTSTS-<%=myStep + "-" + myActionData.getSequence()%>"><%=myActionData.getReturnCode()%></span></td>
                                        <td title="Action Start Time" style="width:4%"><%=DateUtil.getFormatedDate(myActionData.getStartLong())%></td>
                                        <td title="Action Elapsed Time" style="width:5%"><%=DateUtil.getFormatedElapsed(myActionData.getStartLong(), myActionData.getEndLong())%></td>
                                        <td title="Action Sort" style="width:5%"><%=myActionData.getSort()%></td>
                                        <td title="Action Description" style="width:20%"><%=myActionData.getDescription()%></td>
                                        <td title="Action" style="width:20%"><b><%=myActionData.getAction()%></b></td>
                                        <td title="Action Object" style="width:20%"><code><pre><%=StringUtil.textToHtmlConvertingURLsToLinks(myActionData.getObject())%></pre></code></td>
                                        <td title="Action Property" style="width:20%"><code><pre><%=StringUtil.textToHtmlConvertingURLsToLinks(myActionData.getProperty())%></pre></code></td>
                                        <td title="Action Screenshot" style="width:2%"><%if (myActionData.getScreenshotFilename() != null) {%>
                                            <a href="<%=PictureURL%><%=myActionData.getScreenshotFilename().replaceAll("\\\\", "/")%>" id="ACTIMG-<%=myStep + "-" + myActionData.getSequence()%>" class="zoombox  zgallery1">img</a>
                                            <%}%>
                                        </td>
                                        <td title="Action Source File" style="width:2%"><%if (myActionData.getPageSourceFilename() != null) {%>
                                            <a href="<%=PictureURL%><%=myActionData.getPageSourceFilename().replaceAll("\\\\", "/")%>" id="ACTPS-<%=myStep + "-" + myActionData.getSequence()%>">src</a>
                                            <%}%>
                                        </td>
                                    </tr>
                                    <tr class="tableContent">
                                        <td style="width:1%">&nbsp;&nbsp;&nbsp;&nbsp;</td>
                                        <td title="Action Return Code" style="width:1%" class="<%=myActionData.getReturnCode()%>">>></td>
                                        <td title="Action Return Message" colspan="9" class="<%=myActionData.getReturnCode()%>F">
                                            <%
                                                //TODO:FN to refactor while converting the page -- this should be in a servlet or javascript file
                                                String returnMessage = myActionData.getReturnMessage();
                                                if (returnMessage.startsWith("{")) {
                                                    try {
                                                        JSONObject desc = new JSONObject(returnMessage);
                                                        String type = desc.get("type").toString();
                                                        StringBuilder finalMessage = new StringBuilder();
                                                        if (type.equals("SOAP")) {
                                                            finalMessage.append(desc.get("description").toString());
                                                            if (!StringUtil.isNullOrEmpty(desc.get("request").toString())) {
                                                                finalMessage.append(" <a target='_blank' href='").append(PictureURL);
                                                                finalMessage.append(desc.get("request").toString()).append("'>SOAP request </a>");
                                                            }
                                                            if (!StringUtil.isNullOrEmpty(desc.get("response").toString())) {
                                                                finalMessage.append(" <a target='_blank' href='").append(PictureURL);
                                                                finalMessage.append(desc.get("response").toString()).append("'>SOAP response</a>");
                                                            }

                                                        }

                                                        returnMessage = finalMessage.toString();
                                                    } catch (JSONException ex) {
                                                        //there is no need to parse 

                                                    }
                                                }
                                            %>
<code><pre><i><span id="ACTMES-<%=myStep + "-" + myActionData.getSequence()%>"><%=StringUtil.replaceUrlByLinkInString(returnMessage)%></span>
</i></pre></code></td>
                                    </tr>
                                    
                                    <tr>
                                        <td colspan="11">
                                            <%

                                                ITestCaseStepActionControlExecutionService testCaseStepActionControlExecutionService = appContext.getBean(ITestCaseStepActionControlExecutionService.class);
                                                ITestCaseStepActionControlService testCaseStepActionControlService = appContext.getBean(ITestCaseStepActionControlService.class);

                                                List<TestCaseStepActionControlExecution> controlList = testCaseStepActionControlExecutionService.findTestCaseStepActionControlExecutionByCriteria(iD, myActionData.getTest(), myActionData.getTestCase(), myActionData.getStep(), myActionData.getSequence());%>
                                            <table id="controlTable"  style="border-collapse: collapse">
                                                <%
                                                    myAction = myStep + "-" + myActionData.getSequence();

                                                    for (TestCaseStepActionControlExecution myControlData : controlList) {
//                                              %>
                                                <tr class="tableContent">
                                                    <td style="width:20px">&nbsp;&nbsp;&nbsp;&nbsp;</td>
                                                    <td style="width:20px">&nbsp;&nbsp;&nbsp;&nbsp;</td>
                                                    <td title="Control Return Code" class="<%=myControlData.getReturnCode()%>"><span class="<%=myControlData.getReturnCode()%>F" id="CTLSTS-<%=myAction + "-" + myControlData.getControl()%>"><%=myControlData.getReturnCode()%></span></td>
                                                    <td title="Control Start Time"><%=DateUtil.getFormatedDate(myControlData.getStartLong())%></td>
                                                    <td title="Control Elapsed Time"><%=DateUtil.getFormatedElapsed(myControlData.getStartLong(), myControlData.getEndLong())%></td>
                                                    <td title="Control Sort" data-id="<%=myAction + "-" + myControlData.getControl()%>" class="control <%=myControlData.getControl()%>"><%=myControlData.getSort()%></td>
                                                    <td title="Control Description"><%=myControlData.getDescription()%></td>
                                                    <td title="Control Function"><b><%=myControlData.getControlType()%></b></td>
                                                    <td title="Control Property" id="CTLPRP-<%=myAction + "-" + myControlData.getControl()%>"><%=StringUtil.textToHtmlConvertingURLsToLinks(myControlData.getControlProperty())%></td>
                                                    <td title="Control Value"><code><pre id="CTLVAL-<%=myAction + "-" + myControlData.getControl()%>"><%=StringUtil.textToHtmlConvertingURLsToLinks(myControlData.getControlValue())%></pre></code></td>
                                                    <td title="Fatal"><%=myControlData.getFatal()%></td>
                                                    <td title="Control Screenshot"><%if (myControlData.getScreenshotFilename() != null) {%>
                                                        <a href="<%=PictureURL%><%=myControlData.getScreenshotFilename().replaceAll("\\\\", "/")%>" class="zoombox  zgallery1">img</a>
                                                        <%}%>
                                                    </td>
                                                    <td title="Control Source File" style="width:10px"><%if (myControlData.getPageSourceFilename() != null) {%>
                                                        <a href="<%=PictureURL%><%=myControlData.getPageSourceFilename().replaceAll("\\\\", "/")%>" id="ACTPS-<%=myStep + "-" + myControlData.getSequence()%>">src</a>
                                                        <%}%>
                                                    </td>
                                                </tr>
                                                <tr class="tableContent">
                                        <td style="width:10px">&nbsp;&nbsp;&nbsp;&nbsp;</td>
                                        <td>&nbsp;&nbsp;&nbsp;&nbsp;</td>
                                        <td title="Control Return Code" style="width:20px" class="<%=myControlData.getReturnCode()%>">>></td>
                                        <td title="Control Return Message" colspan="10" class="<%=myControlData.getReturnCode()%>F">
                                            <code><pre><i><span id="CTLMES-<%=myAction + "-" + myControlData.getControl()%>"><%=StringUtil.replaceUrlByLinkInString(myControlData.getReturnMessage())%>
                                                </span></i></pre></code></td>
                                    </tr>
                                    <tr></tr>
                                                <%
                                                    }

                                                %>
                                            </table>
                                        </td>
                                    </tr>
                                    <%                                        }

                                    %>
                                </table>
                                <br>
                                <%                                    }

                                %>
                            </td>
                        </tr>                        
                    </table>
                    <br><br>
                    <p class="dttTitle">Properties</p>
                    <table id="dataTable" class="tableBorder">
                        <%                            for (TestCaseExecutionData myData : dataList) {
                        %>
                        <tr>
                            <td title="Status" class="<%=myData.getRC()%>"><span class="<%=myData.getRC()%>F" id="PROPSTS-<%=myData.getProperty()%>"><%=myData.getRC()%></span></td>
                            <td title="Start Time"><%=DateUtil.getFormatedDate(myData.getStartLong())%></td>
                            <td title="Elapsed Time"><%=DateUtil.getFormatedElapsed(myData.getStartLong(), myData.getEndLong())%></td>
                            <td title="Property name"><b><span id="PROP-<%=myData.getProperty()%>"><%=myData.getProperty()%></span></b> <span id="DESCRIPTION-<%=myData.getProperty()%>"><%=myData.getDescription() == null || myData.getDescription().isEmpty() ? "" : '(' + myData.getDescription() + ')'%></span></td>
                            <td title="Value"><code><b><i><span id="PROPVAL-<%=myData.getProperty()%>"><%=StringUtil.textToHtmlConvertingURLsToLinks(myData.getValue())%></span></i></b></code></td>
                            <td title="Type" style="font-size: x-small"><%=myData.getType()%></td>
                            <td title="Value1 / Value2" style="font-size: x-small"><code><%=StringUtil.textToHtmlConvertingURLsToLinks(myData.getValue1())%> / <%=myData.getValue2()%></code></td>
                                        <%
                                            String propMessage = myData.getrMessage();
                                            //TODO this should handled differently, in the servlet or javascript file
                                            if (propMessage.startsWith("{")) {
                                                try {
                                                    JSONObject desc = new JSONObject(propMessage);
                                                    propMessage = desc.get("description").toString();
                                                } catch (JSONException ex) {
                                                    //there is no need to parse 
                                                    //add some recovery code?
                                                }
                                            }
                                        %>
                            <td title="Message" ><code><span id="PROPMES-<%=myData.getProperty()%>"><%=StringUtil.textToHtmlConvertingURLsToLinks(propMessage)%></span></code></td>
                        </tr>
                        <%
                            }

                        %>                        
                    </table>
                <br><br>
                    <%  if (!(myApplication.equalsIgnoreCase(""))) {
                    %>
                <h4>Contextual Actions</h4>
                <table class="tableBorder"  style="text-align: left" border="1" >
                    <tr>
                            <%if (tcGroup.equalsIgnoreCase("MANUAL") && !"PE".equalsIgnoreCase(controlStatus)) {%>
                            <td> 
                                <button style="cursor:pointer; background:none; border:none;margin:0;padding:0;color: #008CDA;text-decoration: none;font-family: helvetica;font-size:11.2px " 
                                        onclick="openRunManualPopin('<%=test%>', '<%=testCase%>', '<%=environment%>', '<%=country%>', '', '<%=testCaseExecution.getTag() == null ? "" : tagEncoded%>', '<%=browser%>')" 
                                        title="Allows the user to execute the current test case. It is executed with the exact same configurations: tag, environment, country...">
                                    Run the same Test Case again
                                </button>
                            </td>
                            <%}%>    
                            <% if (tcGroup.equalsIgnoreCase("AUTOMATED")) {%>
                            <td><a href="RunTests.jsp?Test=<%=test%>&TestCase=<%=testCase%>&MySystem=<%=appSystem%>&Country=<%=country%>&Environment=<%=environment%>&browser=<%=browser%>&Tag=<%=testCaseExecution.getTag() == null ? "" : tagEncoded%>"
                                   title="Allows the user to execute the current test case. The user can modify the configurations associated with the execution: tag, environment, country... ">
                                    Run the same Test Case again</a><br>
                            <a href="RunTests1.jsp?test=<%=test%>&testcase=<%=testCase%>&country=<%=country%>&environment=<%=environment%>&browser=<%=browser%>&tag=<%=testCaseExecution.getTag() == null ? "" : tagEncoded%>"
                                   title="Allows the user to execute the current test case. The user can modify the configurations associated with the execution: tag, environment, country... ">
                                    Run the same Test Case again (Beta)</a></td>
                                    <% } else if (tcGroup.equalsIgnoreCase("MANUAL")) {%>
                        <td>
                            <a href="RunTests.jsp?Test=<%=test%>&TestCase=<%=testCase%>&MySystem=<%=appSystem%>&Country=<%=country%>&Environment=<%=environment%>&browser=<%=browser%>&Tag=<%=testCaseExecution.getTag() == null ? "" : tagEncoded%>&manualExecution=Y"
                                title="Allows the user to execute the current test case. The user can modify the configurations associated with the execution: tag, environment, country... ">
                                Edit configurations and Run the same Test Case again</a><br>
                            <a href="RunTests1.jsp?test=<%=test%>&testcase=<%=testCase%>&country=<%=country%>&environment=<%=environment%>&browser=<%=browser%>&tag=<%=testCaseExecution.getTag() == null ? "" : tagEncoded%>&manualExecution=Y"
                                title="Allows the user to execute the current test case. The user can modify the configurations associated with the execution: tag, environment, country... ">
                                Edit configurations and Run the same Test Case again (Beta)</a>
                        </td>
                            <%}%>                           
                        <td>
                            <a href="TestCase.jsp?Test=<%=test%>&TestCase=<%=testCase%>&Load=Load">Modify the Test Case.</a><br>
                            <a href="TestCaseScript.jsp?test=<%=test%>&testcase=<%=testCase%>">Modify the Test Case (Beta).</a>
                        </td>
                        <td>
                    <a href="ExecutionDetailList.jsp?test=<%=test%>&testcase=<%=testCase%>&MySystem=<%=appSystem%>">See Last Executions..</a>
                </td>
                        <td>
                                <%
                                    if (StringUtil.isNullOrEmpty(newBugURL)) {
                                %>
                            <a href="javascript:void(0)" title="Define the New Bug URL at the application level in order to open a bug from here.">Open a bug.</a> 
                                <%
                                } else {
                                %>
                            <a href="<%= newBugURL%>" target='_blank' title="title">Open a bug.</a> 
                                <%                                }

                                %>
                        </td>
                    </tr>
                </table>
                    <%  }
                    } else {
                    %>
                    <br><br><table id="arrond" style="text-align: left" border="1" >
                        <tr id="header" style="font-style: italic">
                            <td style="font-weight: bold; width: 140px"><b><i>Execution ID not found...</i></b></td>
                        </tr>
                    </table>
                    <%              }
                    %>

                <input style="display:none" id="refreshAuto">
            </div>
            <div id="popin" title="Manual Execution"></div>
            <script>
                    $(document).ready(function () {
                        var stat = document.getElementById("statushidden").value;
                        var idtc = document.getElementById("exeid").innerHTML;


                        if (stat === "PE") {
                            setTimeout(function () {
                                var refresh = document.getElementById("refreshAuto").value;
                                if (refresh !== 'Stop') {
                                    location.href = './ExecutionDetail.jsp?id_tc=' + idtc
                                }
                            }, 5000);

                        }

                        // Zoombox is the lightbox to display image
                        $('a.zoombox').zoombox({
                            theme: 'zoombox', //available themes : zoombox,lightbox, prettyphoto, darkprettyphoto, simple
                            opacity: 0.8, // Black overlay opacity
                            duration: 800, // Animation duration
                            animation: false, // Do we have to animate the box ?
                            width: 600, // Default width
                            height: 400, // Default height
                            gallery: true, // Allow gallery thumb view
                            overflow: true, // Allow gallery thumb view
                            autoplay: false                // Autoplay for video
                        });

                        // Image links displayed as a group
                        //$('a.zoombox').zoombox();

                        $('td.control').css('cursor', 'pointer');
                        $('td.control').click(function () {
                            dialogTheDiff($(this).data('id'));
                        });

                    });


                    function dialogTheDiff(controlId) {

                        var text1 = $('#CTLPRP-' + controlId).html();
                        var text2 = $('#CTLVAL-' + controlId).html();

                        // create match library object
                        var dmp = new diff_match_patch();

                        // instanciate edit cost to 6 for clean up semantic method
                        dmp.Diff_EditCost = 6;

                        // make the diff between text1 and text2
                        var d = dmp.diff_main(text1, text2);

                        // generate semantic diff on text and clean it
                        dmp.diff_cleanupSemantic(d);

                        // another type of diff (like semantic)
                        //dmp.diff_cleanupEfficiency(d);

                        // generate pretty html diff and display it in popin dialog
                        $('#dialogTheDiff').empty().html(dmp.diff_prettyHtml(d))
                        $('#dialogTheDiff').dialog();
                    }
                    </script>
                <br><%=display_footer(DatePageStart)%>
                <div id="dialogTheDiff"></div>
                <div id="setTagToExecution">
                    
                </div>
            </body>
            </html>
