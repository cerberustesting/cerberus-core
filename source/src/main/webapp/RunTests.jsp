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
<%@page import="java.util.Enumeration"%>
<%@page import="org.apache.log4j.Level"%>
<%@page import="org.apache.commons.lang3.StringUtils"%>
<%@page import="org.cerberus.entity.Application"%>
<%@page import="org.cerberus.service.IDocumentationService"%>
<%@page import="org.cerberus.service.IParameterService"%>
<%@page import="org.cerberus.service.IApplicationService"%>
<%@page import="org.cerberus.service.impl.ApplicationService"%>
<%@page import="org.cerberus.log.MyLogger"%>
<%@page import="org.cerberus.util.SqlUtil"%>
<% Date DatePageStart = new Date();%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Run Test Case</title>
        <link rel="stylesheet" type="text/css" href="css/crb_style.css">
        <link rel="shortcut icon" type="image/x-icon" href="images/favicon.ico" />
        <script type="text/javascript" src="js/jquery-1.9.1.min.js"></script>
    </head>
    <body>
        <%@ include file="include/function.jsp"%>
        <%@ include file="include/header.jsp"%>
        <div id="body">
            <form method="get" name="InsertPref">

            </form>
            <form method="get" name="RunTest" onsubmit="validateForm()">
                <%

                    String testcaseApplication = null;
                    Connection conn = null;
                    IDocumentationService docService = appContext.getBean(IDocumentationService.class);
                    IUserService userService = appContext.getBean(IUserService.class);

                    try {

                        conn = db.connect();

                        Statement stmt = conn.createStatement();

                        User usr = userService.findUserByKey(request.getUserPrincipal().getName());

                        if (StringUtils.isNotBlank(request.getParameter("DefaultIP"))) {
                            if (request.getParameter("ss_ip") != null && request.getParameter("ss_ip").compareTo("") != 0) {
                                usr.setDefaultIP(request.getParameter("ss_ip"));
                                userService.updateUser(usr);
                            }
                        }

                        if (request.getParameter("statusPage") != null && request.getParameter("statusPage").compareTo("Run") == 0) {

                            StringBuilder params = new StringBuilder();
                            params.append("RunTestCase?redirect=Y");
                            Enumeration<String> pList = request.getParameterNames();
                            while (pList.hasMoreElements()) {
                                String sName = pList.nextElement().toString();
                                if (sName.compareTo("Test") == 0 || sName.compareTo("TestCase") == 0
                                        || sName.compareTo("Country") == 0 || sName.compareTo("Environment") == 0
                                        || sName.compareTo("ss_ip") == 0 || sName.compareTo("ss_p") == 0
                                        || sName.compareTo("browser") == 0
                                        || sName.compareTo("manualURL") == 0
                                        || sName.compareTo("myhost") == 0 || sName.compareTo("mycontextroot") == 0 || sName.compareTo("myloginrelativeurl") == 0
                                        || sName.compareTo("myenvdata") == 0
                                        || sName.compareTo("Tag") == 0 || sName.compareTo("outputformat") == 0
                                        || sName.compareTo("verbose") == 0 || sName.compareTo("screenshot") == 0) {
                                    String[] sMultiple = request.getParameterValues(sName);
                                    {
                                        for (int i = 0; i < sMultiple.length; i++) {
                                            params.append("&" + sName + "=" + sMultiple[i] + "");
                                        }
                                    }
                                }
                            }
                            response.sendRedirect(params.toString());
                        }

                        String ssIP;
                        if (request.getParameter("ss_ip") != null && request.getParameter("ss_ip").compareTo("") != 0) {
                            ssIP = request.getParameter("ss_ip");
                        } else {
                            ssIP = request.getHeader("X-FORWARDED-FOR");
                            if (ssIP == null) {
                                ssIP = usr.getDefaultIP();
                            }
                        }

                        String ssPort;
                        if (request.getParameter(
                                "ss_p") != null && request.getParameter("ss_p").compareTo("") != 0) {
                            ssPort = request.getParameter("ss_p");
                        } else {
                            ssPort = "5555";
                        }

                        String browser;
                        if (request.getParameter(
                                "browser") != null && request.getParameter("browser").compareTo("") != 0) {
                            browser = request.getParameter("browser");;
                        } else {
                            browser = new String("firefox");
                        }

                        StringBuilder sqlOpts = new StringBuilder();

                        String environment;
                        if (request.getParameter(
                                "Environment") != null && request.getParameter("Environment").compareTo("All") != 0) {
                            environment = request.getParameter("Environment");
                        } else {
                            environment = new String("%%");
                        }

                        String test;
                        if (request.getParameter(
                                "Test") != null && request.getParameter("Test").compareTo("All") != 0) {
                            test = request.getParameter("Test");
                        } else {
                            test = new String("%%");
                        }

                        String testcase;
                        if (request.getParameter(
                                "TestCase") != null && request.getParameter("TestCase").compareTo("All") != 0) {
                            testcase = request.getParameter("TestCase");
                        } else {
                            testcase = new String("%%");
                        }

                        String country;
                        if (request.getParameter(
                                "Country") != null && request.getParameter("Country").compareTo("All") != 0) {
                            country = request.getParameter("Country");
                        } else {
                            country = new String("%%");
                        }

                        String tag;
                        if (request.getParameter(
                                "Tag") != null && request.getParameter("Tag").compareTo("All") != 0) {
                            tag = request.getParameter("Tag");
                        } else {
                            tag = new String("None");
                        }

                        String enable = "";

                        String MySystem = request.getAttribute("MySystem").toString();
                        if (request.getParameter("system") != null && request.getParameter("system").compareTo("") != 0) {
                            MySystem = request.getParameter("system");
                        }
                        IApplicationService applicationService = appContext.getBean(ApplicationService.class);
                        List<Application> appliList = applicationService.findApplicationBySystem(MySystem);
                        // Generate "in" where clause from List.
                        String appliInSQL = SqlUtil.getInSQLClause(appliList);

                        MyLogger.log("RunTests.jsp", Level.DEBUG, "System : '" + MySystem + "' - Application in clause : '" + appliInSQL + "'");

                        String seleniumUrl = "";
                        IParameterService myParameterService = appContext.getBean(IParameterService.class);
                        try {
                            seleniumUrl = myParameterService.findParameterByKey("selenium_download_url", "").getValue();
                        } catch (Exception ex) {
                            MyLogger.log("RunTests.jsp", Level.FATAL, " Exception catched : " + ex);
                        }
                        String seleniumChromeUrl = "";
                        try {
                            seleniumChromeUrl = myParameterService.findParameterByKey("selenium_chromedriver_download_url", "").getValue();
                        } catch (Exception ex) {
                            MyLogger.log("RunTests.jsp", Level.FATAL, " Exception catched : " + ex);
                        }
                        String seleniumIEUrl = "";
                        try {
                            seleniumIEUrl = myParameterService.findParameterByKey("selenium_iedriver_download_url", "").getValue();
                        } catch (Exception ex) {
                            MyLogger.log("RunTests.jsp", Level.FATAL, " Exception catched : " + ex);
                        }

                %>
                <div class="filters" style="clear:both; width:100%">
                    <p style="float:left" class="dttTitle">Choose Test</p>
                    <div id="dropDownDownArrow" style="float:left"><a onclick="javascript:switchDivVisibleInvisible('testParameterDetail', 'testParameterSummary');
                            switchDivVisibleInvisible('dropDownUpArrow', 'dropDownDownArrow')"><img src="images/dropdown.gif"/></a>
                    </div>
                    <div id="dropDownUpArrow" style="display:none; float:left"><a onclick="javascript:switchDivVisibleInvisible('testParameterSummary', 'testParameterDetail');
                            switchDivVisibleInvisible('dropDownDownArrow', 'dropDownUpArrow')"><img style="transform: rotate(180deg);" src="images/dropdown.gif"/></a>
                    </div>
                    <div  id="testParameterDetail" style="clear:both; width:100%">
                        <div style="float:left">
                            <div style="clear:both; width:150px; text-align: left"><% out.print(docService.findLabelHTML("test", "Test", "Test"));%></div>
                            <div style="clear:both"><select size="16" id="test" name="Test"
                                                            style="width: 200px" onchange="document.RunTest.submit()">
                                    <%
                                        ResultSet rsTest = stmt.executeQuery("SELECT DISTINCT t.Test FROM test t, testcase tc WHERE tc.test=t.test AND tc.tcactive='Y' AND t.active='Y' AND tc.application " + appliInSQL + " AND tc.group is not NULL AND tc.group not in ('PRIVATE') AND length(tc.group) > 1 ");
                                        while (rsTest.next()) {%>
                                    <option style="width: 300px" value="<%= rsTest.getString(1)%>"
                                            <%=test.compareTo(rsTest.getString(1)) == 0 ? " SELECTED " : ""%>><%= rsTest.getString(1)%></option>
                                    <% }%>
                                </select></div>
                        </div>
                        <div style="float:left">
                            <div style="clear:both; width:150px; text-align: left"><% out.print(docService.findLabelHTML("testcase", "TestCase", "Test Case"));%></div>
                            <div style="clear:both"><select size="16" id="testcase"
                                                            name="TestCase" style="width: 600px"
                                                            onchange="document.RunTest.submit()">
                                    <% ResultSet rsTestCase = stmt.executeQuery("SELECT DISTINCT tc.TestCase, tc.Description, tc.application FROM testcase tc WHERE tc.test = '" + test + "' AND tc.application " + appliInSQL + " AND tc.group is not NULL AND tc.group not in ('PRIVATE') AND length(tc.group) > 1 AND TcActive = 'Y'");
                                        while (rsTestCase.next()) {
                                    %>
                                    <option style="width: 600px"
                                            value="<%= rsTestCase.getString(1)%>"
                                            <%=testcase.compareTo(rsTestCase.getString(1)) == 0 ? "SELECTED " : ""%>>
                                        <%= rsTestCase.getString(1)%> [<%= rsTestCase.getString(3)%>]
                                        :
                                        <%= rsTestCase.getString(2)%></option>
                                        <%
                                                if (testcase.compareTo(rsTestCase.getString(1)) == 0) {
                                                    testcaseApplication = rsTestCase.getString(3);
                                                }
                                            }%>
                                </select></div>
                        </div>
                        <div style="float:left">
                            <div style="clear:both; width:60px; text-align: left"><% out.print(docService.findLabelHTML("invariant", "Country", "Country"));%></div>
                            <div style="clear:both"><select size="16" id="country"
                                                            name="Country" style="width: 60px"
                                                            onchange="document.RunTest.submit()">
                                    <% ResultSet rsCountry = stmt.executeQuery("SELECT DISTINCT c.Country FROM testcasecountry c "
                                                + " join invariant i on i.value=c.country and i.idname='COUNTRY' "
                                                + " WHERE c.test = '" + test + "' AND c.testcase = '" + testcase + "'"
                                                + " order by sort;");
                                        while (rsCountry.next()) {%>
                                    <option style="width: 300px" value="<%= rsCountry.getString(1)%>"
                                            <%=country.compareTo(rsCountry.getString(1)) == 0 ? " SELECTED " : ""%>><%= rsCountry.getString(1)%></option>
                                    <% }%>
                                </select></div>
                        </div></div><div  style="clear:both; width:100%"></div></div><br><br>
                <div class="filters" style="clear:both; width:100%">
                    <div  style="clear:both; width:100%">
                        <div style="clear:both">
                            <div style="float:left; width:100%; text-align: left">
                                <p style="float:left" class="dttTitle">Choose Environment</p>  
                                <div style="float:left; width:100px; text-align: left"><input type="radio" name="manualURL" value="N" onclick="setEnvAutomatic();
                                        switchDivVisibleInvisible('automatedEnvironmentDiv', 'manualEnvironmentDiv')" checked>Automatic</div>
                                <div style="float:left"><input type="radio" name="manualURL" value="Y" onclick="setEnvManual();
                                            switchDivVisibleInvisible('manualEnvironmentDiv', 'automatedEnvironmentDiv')">Manual</div>

                            </div>

                            <div style="clear:both">


                                <div id="automatedEnvironmentDiv" style="float:left">
                                    <div style="float:left; width:60px; text-align: left"><span style="font-weight: normal;font-size: smaller"><% out.print(docService.findLabelHTML("invariant", "Environment", "Environment"));%></span></div>
                                    <div style="float:left"><select id="environment" name="Environment" style="width: 400px">
                                            <%
                                                StringBuilder sql = new StringBuilder();
                                                sql.append("SELECT DISTINCT ce.Environment Environment, ce.Build Build, ");
                                                sql.append("    ce.Revision Revisionv ");
                                                sql.append("FROM countryenvironmentparameters cea, countryenvparam ce, invariant i ");
                                                sql.append("WHERE ce.system = cea.system AND ce.country = cea.country AND ce.environment = cea.environment ");
                                                sql.append("    AND cea.Application = '");
                                                sql.append(testcaseApplication);
                                                sql.append("' AND cea.country='");
                                                sql.append(country);
                                                sql.append("'");
                                                sql.append("    AND ce.active='Y' ");
                                                sql.append("    AND ce.system='" + MySystem + "' ");
                                                sql.append("    AND i.idname = 'ENVIRONMENT' AND i.Value = ce.Environment ");
                                                sql.append("ORDER BY i.sort ");

                                                if (!(testcaseApplication == null) && !(country.isEmpty()) && !(country.equalsIgnoreCase("%%"))) {
                                                    ResultSet rsEnv = stmt.executeQuery(sql.toString());
                                                    while (rsEnv.next()) {
                                            %>
                                            <option style="width: 400px" value="<%= rsEnv.getString(1)%>"
                                                    <%=environment.compareTo("%%") == 0 && rsEnv.getString(1).contains("UAT") ? " SELECTED " : ""%>
                                                    <%=environment.compareTo(rsEnv.getString(1)) == 0 ? " SELECTED " : ""%>><%= rsEnv.getString(1)%>
                                                With Build : <%= rsEnv.getString(2)%> And Revision : <%= rsEnv.getString(3)%></option>
                                                <%
                                                    }
                                                } else {
                                                %>
                                            <option style="width: 400px; text-decoration-color:lightgray">Select Test, Testcase and Country first</option><%}%></select></div>
                                </div>
                                <div id="manualEnvironmentDiv" style="float:left; display:none">
                                    <div style="clear:both"><span style="font-weight: normal;font-size: smaller">My Host</span>
                                        <input type="text" style="width: 100px" name="myhost" id="myhost" disabled value="localhost:8080">
                                        <span style="font-weight: normal;font-size: smaller">My Context Root</span>
                                        <input type="text" style="width: 200px" name="mycontextroot" id="mycontextroot" disabled value="/myapp/">
                                        <span style="font-weight: normal;font-size: smaller">My Login Relative URL</span>
                                        <input type="text" style="width: 200px" name="myloginrelativeurl" id="myloginrelativeurl" disabled value="login.jsp?comcode=200">
                                        <span style="font-weight: normal;font-size: smaller">Data Environment</span>
                                        <select id="myenvdata" name="myenvdata" style="width: 200px" disabled>
                                            <%
                                                if (!(testcaseApplication == null) && !(country.isEmpty()) && !(country.equalsIgnoreCase("%%"))) {
                                                    ResultSet rsEnv = stmt.executeQuery(sql.toString());
                                                    while (rsEnv.next()) {
                                            %>
                                            <option style="width: 400px" value="<%= rsEnv.getString(1)%>"
                                                    <%=environment.compareTo("%%") == 0 && rsEnv.getString(1).contains("UAT") ? " SELECTED " : ""%>
                                                    <%=environment.compareTo(rsEnv.getString(1)) == 0 ? " SELECTED " : ""%>><%= rsEnv.getString(1)%>
                                                With Build : <%= rsEnv.getString(2)%> And Revision : <%= rsEnv.getString(3)%></option>
                                                <%
                                                        }
                                                    }%>
                                        </select>

                                    </div>
                                </div>

                            </div>
                        </div>
                    </div>
                    <div  style="clear:both; width:100%"></div>
                </div>

                <br><br> 

                <div class="filters" style="float:left; width:49%;height:180px">
                    <div style="clear:both">
                    <p style="float:left" class="dttTitle">Choose Robot</p>
                    <div style="float:left; width:100px; text-align: left"><input type="radio" name="manualURL" value="N" onclick="switchDivVisibleInvisible('automatedEnvironmentDiv', 'manualEnvironmentDiv')" checked>Automatic</div>
                    <div style="float:left"><input type="radio" name="manualURL" value="Y" onclick="switchDivVisibleInvisible('manualEnvironmentDiv', 'automatedEnvironmentDiv')">Manual</div>
                    </div>
                    <div style="clear:both">
                         <div style="float:left; width:150px; text-align:left"><% out.print(docService.findLabelHTML("page_runtests", "SeleniumServerIP", "Selenium Server IP "));%></div>
                         <div style="float:left">
                        <input type="text" name="ss_ip" value="<%= ssIP%>" />
                        </div>
                    </div>
                    <div style="clear:both">
                    <div style="float:left; width:150px; text-align:left"><% out.print(docService.findLabelHTML("page_runtests", "SeleniumServerPort", "Selenium Server Port "));%></div>
                    <div style="float:left">
                            <input type="text" name="ss_p" value="<%= ssPort%>" />
                        </div>
                        </div>
                    <div style="clear:both">
                   <div style="float:left; width:150px; text-align:left"><% out.print(docService.findLabelHTML("page_runtests", "Browser", "Browser"));%></div>
                   <div style="float:left">
                            <%=ComboInvariant(conn, "browser", "width: 90px", "browser", "browser", "BROWSER", browser, "", null)%>
                        </div></div>
                    <div style="clear:both">
                        <div style="float:left; width:150px; text-align:left">BrowserVersion</div>
                        <div style="float:left">
                            <%=ComboInvariant(conn, "browser", "width: 90px", "browser", "browser", "BROWSER", browser, "", null)%>
                        </div></div>
                    <div style="clear:both"></div>
                    <div style="clear:both">
                        <div style="float:left; width:150px; text-align:left">Platform</div>
                        <div style="float:left">
                            <%=ComboInvariant(conn, "browser", "width: 90px", "browser", "browser", "BROWSER", browser, "", null)%>
                        </div></div>
                        <div style="clear:both">
                        <input id="button" class="button" type="submit" <%=enable%> name="DefaultIP" value="Set As My Default IP" >
                        </div>
                        <div style="clear:both; text-align:left;font-size: smaller">
                        INSTRUCTIONS ON HOW TO RUN YOUR LOCAL SELENIUM SERVER :<br>
                    Download the compatible version of Selenium <a href="<%=seleniumUrl%>">here</a>. Drivers : <a href="<%=seleniumIEUrl%>">IE</a> <a href="<%=seleniumChromeUrl%>">Chrome</a>
                     <br>Example scripts to start your local selenium server : <a href="ressources/start-selenium.sh">Linux</a> / <a href="ressources/start-selenium.bat">Windows</a>
                        </div>  
                    
                </div>
                        <div class="filters" style="float:right; width:49%;height:180px">
                             <p style="float:left" class="dttTitle">Set Execution Parameter</p>
                             <div style="clear:both">
                                        <div style="float:left; width:150px; text-align:left"><% out.print(docService.findLabelHTML("testcaseexecution", "tag", "Tag"));%></div>
                                        <div style="float:left">
                                            <input id="tag" name="Tag" style="width: 200px">
                                        </div></div>
                                        <div style="clear:both">
                                    <div style="float:left;width:150px; text-align:left"><% out.print(docService.findLabelHTML("page_runtests", "outputformat", ""));%></div>
                                        <div style="float:left">
                                            <%=ComboInvariant(conn, "outputformat", "width: 90px", "Format", "Format", "OUTPUTFORMAT", "gui", "", null)%>
                                        </div></div>
                                        <div style="clear:both">
                                        <div style="float:left;width:150px;text-align:left "><% out.print(docService.findLabelHTML("testcaseexecution", "verbose", ""));%></div>
                                        <div style="float:left">
                                            <%=ComboInvariant(conn, "verbose", "width: 90px", "Verbose", "Verbose", "VERBOSE", "0", "", null)%>
                                        </div></div>
                                        <div style="clear:both">
                                        <div style="float:left;width:150px; text-align:left"><% out.print(docService.findLabelHTML("page_runtests", "screenshot", ""));%></div>
                                        <div style="float:left">
                                            <%=ComboInvariant(conn, "screenshot", "width: 90px", "Screenshot", "Screenshot", "SCREENSHOT", "1", "", null)%>
                                        </div></div>
                                        
                        </div>
                                        <div style="clear:both">          

                    <br>
                                        </div>
                                        <div style="float:left"><input type="submit" class="buttonPlay" id="buttonRun" style="font-size: large;" name="statusPage" value="Run"></div>
                                   
                    

                    <br>


                    <% if (test.compareTo("%%") != 0 && testcase.compareTo("%%") != 0 && country.compareTo("%%") != 0) {
                    %>

                    <table border="0px">
                    </table>
            </form>

            <%                    }

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

        </div>

        <br><% out.print(display_footer(DatePageStart));%>

        <script type="text/javascript">
            function validateForm() {
                if ($("#myloginrelativeurl").val()) {
                    var val = $("#myloginrelativeurl").val().replace("&", "%26");
                    $("#myloginrelativeurl").val(val);
                }
                return true;
            }
        </script>
    </body>
</html>
