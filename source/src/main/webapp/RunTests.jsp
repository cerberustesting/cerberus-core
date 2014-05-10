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
<%@page import="org.cerberus.entity.Robot"%>
<%@page import="org.cerberus.service.IRobotService"%>
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
                    IDocumentationService docService = appContext.getBean(IDocumentationService.class);
                    IUserService userService = appContext.getBean(IUserService.class);
                    IRobotService robService = appContext.getBean(IRobotService.class);
                    IApplicationService applicationService = appContext.getBean(ApplicationService.class);
                    IParameterService myParameterService = appContext.getBean(IParameterService.class);

                    try {
                        User usr = userService.findUserByKey(request.getUserPrincipal().getName());

                        //Update User Preferences
                        if (StringUtils.isNotBlank(request.getParameter("RobotHost"))) {
                            usr.setRobotHost(request.getParameter("ss_ip") == null ? "" : request.getParameter("ss_ip"));
                            usr.setRobotPort(request.getParameter("ss_p") == null ? "" : request.getParameter("ss_p"));
                            usr.setRobotPlatform(request.getParameter("platform") == null ? "" : request.getParameter("platform"));
                            usr.setRobotBrowser(request.getParameter("browser") == null ? "" : request.getParameter("browser"));
                            usr.setRobotVersion(request.getParameter("version") == null ? "" : request.getParameter("version"));
                            userService.updateUser(usr);
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
                                        || sName.compareTo("verbose") == 0 || sName.compareTo("screenshot") == 0
                                        || sName.compareTo("platform") == 0 || sName.compareTo("os") == 0
                                        || sName.compareTo("robot") == 0) {
                                    String[] sMultiple = request.getParameterValues(sName);

                                    for (int i = 0; i < sMultiple.length; i++) {
                                        params.append("&" + sName + "=" + sMultiple[i] + "");
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
                                ssIP = usr.getRobotHost();
                            }
                        }

                        String ssPort;
                        if (request.getParameter("ss_p") != null && request.getParameter("ss_p").compareTo("") != 0) {
                            ssPort = request.getParameter("ss_p");
                        } else {
                            ssPort = usr.getRobotPort();
                        }

                        String robot;
                        if (request.getParameter("robot") != null && request.getParameter("robot").compareTo("") != 0) {
                            robot = request.getParameter("robot");
                        } else {
                            robot = String.valueOf(usr.getRobotPort());
                        }

                        String browser = "";
                        String browserClass = "selectRobot";
                        if (request.getParameter("browser") != null && request.getParameter("browser").compareTo("") != 0) {
                            browser = request.getParameter("browser");;
                        } else {
                            browser = usr.getRobotBrowser();
                        }
                        if (!browser.equals("")) {
                            browserClass = "selectRobotSelected";
                        }

                        String platform = "";
                        String platformClass = "selectRobot";
                        if (request.getParameter("platform") != null && request.getParameter("platform").compareTo("") != 0) {
                            platform = request.getParameter("platform");;
                        } else {
                            platform = usr.getRobotPlatform();
                        }
                        if (!platform.equals("")) {
                            platformClass = "selectRobotSelected";
                        }

                        String version = "";
                        String versionClass = "selectRobot";
                        if (request.getParameter("version") != null && request.getParameter("version").compareTo("") != 0) {
                            version = request.getParameter("version");;
                        } else {
                            version = usr.getRobotVersion();
                        }
                        if (!version.equals("")) {
                            versionClass = "selectRobotSelected";
                        }

                        StringBuilder sqlOpts = new StringBuilder();

                        String environment;
                        if (request.getParameter("Environment") != null && request.getParameter("Environment").compareTo("All") != 0) {
                            environment = request.getParameter("Environment");
                        } else {
                            environment = new String("%%");
                        }

                        String test;
                        if (request.getParameter("Test") != null && request.getParameter("Test").compareTo("All") != 0) {
                            test = request.getParameter("Test");
                        } else {
                            test = new String("%%");
                        }

                        String testcase;
                        if (request.getParameter("TestCase") != null && request.getParameter("TestCase").compareTo("All") != 0) {
                            testcase = request.getParameter("TestCase");
                        } else {
                            testcase = new String("%%");
                        }

                        String country;
                        if (request.getParameter("Country") != null && request.getParameter("Country").compareTo("All") != 0) {
                            country = request.getParameter("Country");
                        } else {
                            country = new String("%%");
                        }

                        String tag;
                        if (request.getParameter("Tag") != null && request.getParameter("Tag").compareTo("All") != 0) {
                            tag = request.getParameter("Tag");
                        } else {
                            tag = new String("None");
                        }
                        
                        String verbose;
                        if (request.getParameter("verbose") != null && request.getParameter("verbose").compareTo("") != 0) {
                            verbose = request.getParameter("verbose");
                        } else {
                            verbose = new String("1");
                        }
                        
                        String screenshot;
                        if (request.getParameter("screenshot") != null && request.getParameter("screenshot").compareTo("") != 0) {
                            screenshot = request.getParameter("screenshot");
                        } else {
                            screenshot = new String("1");
                        }

                        String enable = "";

                        String MySystem = request.getAttribute("MySystem").toString();
                        if (request.getParameter("system") != null && request.getParameter("system").compareTo("") != 0) {
                            MySystem = request.getParameter("system");
                        }

                        List<Application> appliList = applicationService.findApplicationBySystem(MySystem);
                        // Generate "in" where clause from List.
                        String appliInSQL = SqlUtil.getInSQLClause(appliList);

                        MyLogger.log("RunTests.jsp", Level.DEBUG, "System : '" + MySystem + "' - Application in clause : '" + appliInSQL + "'");

                        String seleniumUrl = "";

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

                        List<Robot> robots = robService.findAllRobot();


                %>
                <input hidden="hidden" id="defTest" value="<%=test%>">
                <input hidden="hidden" id="defTestCase" value="<%=testcase%>">
                <input hidden="hidden" id="defCountry" value="<%=country%>">
                <input hidden="hidden" id="defPlatform" value="<%=platform%>">
                <input hidden="hidden" id="defBrowser" value="<%=browser%>">
                <input hidden="hidden" id="defVerbose" value="<%=verbose%>">
                <input hidden="hidden" id="defScreenshot" value="<%=screenshot%>">
                <div class="filters" style="clear:both; width:100%">
                    <p style="float:left" class="dttTitle">Choose Test</p>
                    <div id="dropDownDownArrow" style="float:left">
                        <a onclick="javascript:switchDivVisibleInvisible('testParameterDetail', 'testParameterSummary');
                                switchDivVisibleInvisible('dropDownUpArrow', 'dropDownDownArrow')">
                            <img src="images/dropdown.gif"/>
                        </a>
                    </div>
                    <div id="dropDownUpArrow" style="display:none; float:left">
                        <a onclick="javascript:switchDivVisibleInvisible('testParameterSummary', 'testParameterDetail');
                                switchDivVisibleInvisible('dropDownDownArrow', 'dropDownUpArrow')">
                            <img style="transform: rotate(180deg);" src="images/dropdown.gif"/>
                        </a>
                    </div>
                    <div  id="testParameterDetail" style="clear:both; width:100%">
                        <div style="float:left">
                            <div style="clear:both; width:150px; text-align: left"><% out.print(docService.findLabelHTML("test", "Test", "Test"));%>
                            </div>
                            <div style="clear:both">
                                <select size="16" id="test" name="Test"
                                        style="width: 200px" onchange="getTestCaseList()">
                                </select>
                            </div>
                        </div>
                        <div style="float:left">
                            <div style="clear:both; width:150px; text-align: left"><% out.print(docService.findLabelHTML("testcase", "TestCase", "Test Case"));%></div>
                            <div style="clear:both">
                                <select size="16" id="testcase" name="TestCase" style="width: 600px"
                                        onchange="getCountryList()">
                                </select>
                            </div>
                        </div>
                        <div style="float:left">
                            <div style="clear:both; width:60px; text-align: left"><% out.print(docService.findLabelHTML("invariant", "Country", "Country"));%></div>
                            <div style="clear:both">
                                <select size="16" id="country" name="Country" style="width: 60px"
                                        onchange="getApplicationList()">
                                </select>
                            </div>
                        </div>
                    </div>
                    <div  style="clear:both; width:100%">
                    </div>
                </div>
                <br>
                <br>
                <div class="filters" style="clear:both; width:100%">
                    <div  style="clear:both; width:100%">
                        <div style="clear:both">
                            <div style="float:left; width:100%; text-align: left">
                                <p style="float:left" class="dttTitle">Choose Environment</p>
                                <div style="float:left; width:100px; text-align: left">
                                    <input type="radio" name="manualURL" value="N" onclick="setEnvAutomatic();
                                            switchDivVisibleInvisible('automatedEnvironmentDiv', 'manualEnvironmentDiv')" checked>Automatic
                                </div>
                                <div style="float:left">
                                    <input type="radio" name="manualURL" value="Y" onclick="setEnvManual();
                                            switchDivVisibleInvisible('manualEnvironmentDiv', 'automatedEnvironmentDiv')">Manual
                                </div>
                            </div>
                            <div style="clear:both">
                                <div id="automatedEnvironmentDiv" style="float:left">
                                    <div style="float:left; width:60px; text-align: left">
                                        <span style="font-weight: normal;font-size: smaller"><% out.print(docService.findLabelHTML("invariant", "Environment", "Environment"));%>
                                        </span>
                                    </div>
                                    <div style="float:left">
                                        <select id="environment" name="Environment" style="width: 400px">
                                        </select>
                                    </div>
                                </div>
                                <div id="manualEnvironmentDiv" style="float:left; display:none">
                                    <div style="clear:both">
                                        <span style="font-weight: normal;font-size: smaller">My Host</span>
                                        <input type="text" style="width: 100px" name="myhost" id="myhost" disabled value="localhost:8080">
                                        <span style="font-weight: normal;font-size: smaller">My Context Root</span>
                                        <input type="text" style="width: 200px" name="mycontextroot" id="mycontextroot" disabled value="/myapp/">
                                        <span style="font-weight: normal;font-size: smaller">My Login Relative URL</span>
                                        <input type="text" style="width: 200px" name="myloginrelativeurl" id="myloginrelativeurl" disabled value="login.jsp?comcode=200">
                                        <span style="font-weight: normal;font-size: smaller">Data Environment</span>
                                        <select id="myenvdata" name="myenvdata" style="width: 200px" disabled></select>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div  style="clear:both; width:100%">
                    </div>
                </div>
                <br>
                <br> 
                <div class="filters" style="float:left; width:49%;height:180px">
                    <div style="clear:both">
                        <p style="float:left;" class="dttTitle">Choose Robot</p>
                        <div style="float:left; text-align: left">
                            <input type="radio" name="manualRobot" value="Y" onclick="switchDivVisibleInvisible('manualRobotDiv', 'automatedRobotDiv');
                                    setRobotManual()" checked>Manual
                        </div>
                        <div style="float:left">
                            <input type="radio" name="manualRobot" value="N" onclick="switchDivVisibleInvisible('automatedRobotDiv', 'manualRobotDiv');
                                    setRobotAutomatic()">Automatic
                        </div>
                    </div>
                    <div>
                        <div id="manualRobotDiv">
                            <div style="clear:both">
                                <div style="float:left; width:150px; text-align:left"><% out.print(docService.findLabelHTML("page_runtests", "SeleniumServerIP", "Selenium Server IP "));%>
                                </div>
                                <div style="float:left">
                                    <input type="text" name="ss_ip" id="ss_ip" value="<%= ssIP%>" style="float:left; width:150px;" />
                                </div>
                            </div>
                            <div style="clear:both">
                                <div style="float:left; width:150px; text-align:left"><% out.print(docService.findLabelHTML("page_runtests", "SeleniumServerPort", "Selenium Server Port "));%>
                                </div>
                                <div style="float:left">
                                    <input type="text" name="ss_p" id="ss_p" value="<%= ssPort%>" style="float:left; width:150px;"/>
                                </div>
                            </div>
                            <div style="clear:both">
                                <div style="float:left; width:150px; text-align:left"><% out.print(docService.findLabelHTML("page_runtests", "Browser", "Browser"));%>
                                </div>
                                <div style="float:left">
                                    <select id="browser" name="browser" style="width: 150px;" class="<%=browserClass%>">
                                    </select>
                                </div>
                            </div>
                            <div style="clear:both">
                                <div style="float:left; width:150px; text-align:left">Version (Optional)
                                </div>
                                <div style="float:left">
                                    <input id="version" name="version" placeholder="Example : 28.0" value="<%=version%>" style="width: 150px;">
                                </div>
                            </div>
                            <div style="clear:both">
                                <div style="float:left; width:150px; text-align:left">Platform (Optional)
                                </div>
                                <div style="float:left">
                                    <select id="platform" name="platform" class="<%=platformClass%>" style="width: 150px;" onchange="changeStyleWhenSelected('platform')">
                                    </select>
                                </div>
                            </div>
                            <div style="clear:both">
                            </div>
                            <div id="recordButtonDiv" style="clear:both">
                                <input id="button" class="button" type="button" onclick="recordRobotPreference()" <%=enable%> name="RobotHost" value="Record my Robot Preferences" >
                            </div>
                        </div>
                        <div id="automatedRobotDiv" style="display:none; clear:both">
                            <div style="float:left">
                                <select id="robot" name="robot" disabled="true" style="width: 550px;">
                                    <% for (Robot rob : robots) { %>
                                    <option style="width: 550px;" 
                                            <% if (robot.equalsIgnoreCase(rob.getRobot())) { %>
                                            selected="selected"
                                            <% }%> 
                                            <% if (rob.getRobot().equals("Y")) { %>
                                            style="font-weight:bolder"
                                            <% }%> 
                                            value="<%=rob.getRobot()%>"> 
                                        <%="[ " + rob.getRobot() + " ] " + rob.getHost() + ":" + rob.getPort() + " " + rob.getBrowser() + " V" + rob.getVersion() + " on " + rob.getPlatform() + "."%>
                                    </option>
                                    <% }%>
                                </select>
                            </div>
                        </div>

                        <div style="clear:both; text-align:left;font-size: smaller">
                            INSTRUCTIONS ON HOW TO RUN YOUR LOCAL SELENIUM SERVER :
                            <br>
                            Download the compatible version of Selenium <a href="<%=seleniumUrl%>">here</a>. Drivers : <a href="<%=seleniumIEUrl%>">IE</a> <a href="<%=seleniumChromeUrl%>">Chrome</a>
                            <br>
                            Example scripts to start your local selenium server : <a href="ressources/start-selenium.sh">Linux</a> / <a href="ressources/start-selenium.bat">Windows</a>
                        </div>  

                    </div>
                </div>
                <div class="filters" style="float:right; width:49%;height:180px">
                    <p style="float:left" class="dttTitle">Set Execution Parameter
                    </p>
                    <div style="clear:both">
                        <div style="float:left; width:150px; text-align:left"><% out.print(docService.findLabelHTML("testcaseexecution", "tag", "Tag"));%>
                        </div>
                        <div style="float:left">
                            <input id="tag" name="Tag" style="width: 200px">
                        </div>
                    </div>
                    <div style="clear:both">
                        <div style="float:left;width:150px; text-align:left"><% out.print(docService.findLabelHTML("page_runtests", "outputformat", ""));%>
                        </div>
                        <div style="float:left">
                            <select id="outputformat" name="outputformat" style="width: 200px">
                            </select>
                        </div>
                    </div>
                    <div style="clear:both">
                        <div style="float:left;width:150px;text-align:left "><% out.print(docService.findLabelHTML("testcaseexecution", "verbose", ""));%>
                        </div>
                        <div style="float:left">
                            <select id="verbose" name="verbose" style="width: 200px">
                            </select>
                        </div>
                    </div>
                    <div style="clear:both">
                        <div style="float:left;width:150px; text-align:left"><% out.print(docService.findLabelHTML("page_runtests", "screenshot", ""));%>
                        </div>
                        <div style="float:left">
                            <select id="screenshot" name="screenshot" style="width: 200px">
                            </select>
                        </div>
                    </div>
                </div>
                <div style="clear:both">          
                    <br>
                </div>
                <div style="float:left">
                    <input type="submit" class="buttonPlay" id="buttonRun" style="font-size: large;" name="statusPage" value="Run">
                </div>
                <br>
                <% if (test.compareTo("%%") != 0 && testcase.compareTo("%%") != 0 && country.compareTo("%%") != 0) {  %>
                <table border="0px">
                </table>
            </form>
            <%              }
                } catch (Exception e) {
                    out.println(e);
                } finally {
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
        <script type="text/javascript">

            var sys = document.getElementById("MySystem");
            var systemSelected = sys.options[sys.selectedIndex].value;
            var testS = document.getElementById("defTest").value;
            var tcS = document.getElementById("defTestCase").value;
            var countryS = document.getElementById("defCountry").value;

            (document).ready($.getJSON('GetTestBySystem?system=' + systemSelected, function(data) {
                $("#test").empty();

                for (var i = 0; i < data.testsList.length; i++) {
                    $("#test").append($("<option></option>")
                            .attr("value", data.testsList[i])
                            .text(data.testsList[i]));
                }
                    $("#test").find('option').each(function(i, opt) {
                        if (opt.value === testS)
                            $(opt).attr('selected', 'selected');
                    });


            }));

        </script>
        <script type="text/javascript">
            function getTestCaseList() {
                var sys = document.getElementById("MySystem");
                var systemSelected = sys.options[sys.selectedIndex].value;
                var b = document.getElementById("test");
                var testSelected = b.options[b.selectedIndex].value;

                $.getJSON('GetTestCaseForTest?system=' + systemSelected + '&test=' + testSelected, function(data) {
                    $("#testcase").empty();
                    $("#country").empty();
                    $("#environment").empty();
                    $("#myenvdata").empty();

                    for (var i = 0; i < data.testCaseList.length; i++) {
                        $("#testcase").append($("<option></option>")
                                .attr("value", data.testCaseList[i].testCase)
                                .attr("data-testcase", data.testCaseList[i].testCase)
                                .attr("data-application", data.testCaseList[i].application)
                                .text(data.testCaseList[i].description));
                    }
//                    $("#test").find('option').each(function(i, opt) {
//                        if (opt.value === fieldSelected)
//                            $(opt).attr('selected', 'selected');
//                    }

                });
            }
            ;
        </script>
        <script type="text/javascript">
            function getCountryList() {
                var b = document.getElementById("test");
                var testSelected = b.options[b.selectedIndex].value;
                var c = document.getElementById("testcase");
                var testCaseSelected = c.options[c.selectedIndex].getAttribute('data-testcase');

                $.getJSON('GetCountryForTestCase?test=' + testSelected + '&testCase=' + testCaseSelected, function(data) {
                    $("#country").empty();
                    $("#environment").empty();
                    $("#myenvdata").empty();

                    for (var i = 0; i < data.countriesList.length; i++) {
                        $("#country").append($("<option></option>")
                                .attr("value", data.countriesList[i])
                                .text(data.countriesList[i]));
                    }
//                    $("#test").find('option').each(function(i, opt) {
//                        if (opt.value === fieldSelected)
//                            $(opt).attr('selected', 'selected');
//                    }

                });
            }
            ;
        </script>

        <script type="text/javascript">
            function getApplicationList() {
                var sys = document.getElementById("MySystem");
                var systemSelected = sys.options[sys.selectedIndex].value;
                var app = document.getElementById("testcase");
                var applicationSelected = app.options[app.selectedIndex].getAttribute('data-application');
                var cou = document.getElementById("country");
                var countrySelected = cou.options[cou.selectedIndex].value;

                $.getJSON('findEnvironmentByCriteria?system=' + systemSelected + '&country=' + countrySelected + '&application=' + applicationSelected, function(data) {
                    $("#environment").empty();
                    $("#myenvdata").empty();

                    for (var i = 0; i < data.length; i++) {
                        $("#environment").append($("<option></option>")
                                .attr("value", data[i].environment)
                                .text(data[i].environment + " with Build :" + data[i].build + " and Revision :" + data[i].revision));
                        $("#myenvdata").append($("<option></option>")
                                .attr("value", data[i].environment)
                                .text(data[i].environment + " with Build :" + data[i].build + " and Revision :" + data[i].revision));
                    }
//                    $("#environment").find('option').each(function(i, opt) {
//                        if (opt.value === fieldSelected)
//                            $(opt).attr('selected', 'selected');
//                    }

                });
            }
            ;
        </script>
        <script type="text/javascript">
            (document).ready($.getJSON('FindInvariantByID?idName=outputformat', function(data) {
                $("#outputformat").empty();

                for (var i = 0; i < data.length; i++) {
                    $("#outputformat").append($("<option></option>")
                            .attr("value", data[i].value)
                            .text(data[i].value + " ( " + data[i].description + " )"));
                }

            }));

        </script>
        <script type="text/javascript">
            (document).ready($.getJSON('FindInvariantByID?idName=verbose', function(data) {
                $("#verbose").empty();
                var pl = document.getElementById("defVerbose").value;
                
                for (var i = 0; i < data.length; i++) {
                    $("#verbose").append($("<option></option>")
                            .attr("value", data[i].value)
                            .text(data[i].value + " ( " + data[i].description + " )"));
                }
                
                $("#verbose").find('option').each(function(i, opt) {
                    if (opt.value === pl) {
                        $(opt).attr('selected', 'selected');
                    }


                });

            }));

        </script>
        <script type="text/javascript">
            (document).ready($.getJSON('FindInvariantByID?idName=screenshot', function(data) {
                $("#screenshot").empty();
                var pl = document.getElementById("defScreenshot").value;

                for (var i = 0; i < data.length; i++) {
                    $("#screenshot").append($("<option></option>")
                            .attr("value", data[i].value)
                            .text(data[i].value + " ( " + data[i].description + " )"));
                }

$("#screenshot").find('option').each(function(i, opt) {
                    if (opt.value === pl) {
                        $(opt).attr('selected', 'selected');
                    }


                });
            }));

        </script>
        <script type="text/javascript">
            (document).ready($.getJSON('FindInvariantByID?idName=browser', function(data) {
                $("#browser").empty();
                var pl = document.getElementById("defBrowser").value;

                for (var i = 0; i < data.length; i++) {
                    $("#browser").append($("<option></option>")
                            .attr("value", data[i].value)
                            .text(data[i].value + " ( " + data[i].description + " )"));
                }
                $("#browser").find('option').each(function(i, opt) {
                    if (opt.value === pl) {
                        $(opt).attr('selected', 'selected');
                    }


                });
            }));

        </script>
        <script type="text/javascript">
            (document).ready($.getJSON('FindInvariantByID?idName=platform', function(data) {
                $("#platform").empty();
                var pl = document.getElementById("defPlatform").value;

                $("#platform").append($("<option></option>")
                        .attr("value", "")
                        .text("Optional"));

                for (var i = 0; i < data.length; i++) {
                    $("#platform").append($("<option></option>")
                            .attr("value", data[i].value)
                            .text(data[i].value + " ( " + data[i].description + " )"));
                }

                $("#platform").find('option').each(function(i, opt) {
                    if (opt.value === pl) {
                        $(opt).attr('selected', 'selected');
                    }


                });
            }));

        </script>
        <script>
            function changeStyleWhenSelected(field) {
                var b = document.getElementById(field);
                var c = b.options[b.selectedIndex].value;

                if (c !== '') {
                    document.getElementById(field).setAttribute('class', 'selectRobotSelected');
                } else {
                    document.getElementById(field).setAttribute('class', 'selectRobot');
                }
            }
        </script>
        <script>
            function recordRobotPreference() {
                var ip = document.getElementById("ss_ip").value;
                var p = document.getElementById("ss_p").value;
                var br = document.getElementById("browser");
                var b = br.options[br.selectedIndex].value;
                var v = document.getElementById("version").value;
                var pla = document.getElementById("platform");
                var pl = pla.options[pla.selectedIndex].value;
                $("#recordButtonDiv").append('<img id="loader" src="images/loading.gif">');
                var xhttp = new XMLHttpRequest();
                xhttp.open("GET", "UpdateUserRobotPreference?ss_ip=" + ip + "&ss_p=" + p + "&browser=" + b + "&version=" + v + "&platform=" + pl, false);
                xhttp.send();
                var xmlDoc = xhttp.responseText;
                $("#loader").remove();
            }
        </script>
    </body>
</html>
