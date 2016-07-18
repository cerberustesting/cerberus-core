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
<%@page import="java.util.HashMap"%>
<%@page import="org.cerberus.crud.entity.CountryEnvParam"%>
<%@page import="org.cerberus.crud.service.ICountryEnvParamService"%>
<%@page import="org.cerberus.crud.entity.CountryEnvironmentParameters"%>
<%@page import="org.cerberus.crud.service.ICountryEnvironmentParametersService"%>
<%@page import="java.net.URLEncoder"%>
<%@page import="org.cerberus.crud.entity.Robot"%>
<%@page import="org.cerberus.util.SqlUtil"%>
<%@page import="org.cerberus.crud.entity.Application"%>
<%@page import="java.util.Enumeration"%>
<%@page import="org.cerberus.crud.service.IParameterService"%>
<%@page import="org.cerberus.crud.service.IApplicationService"%>
<%@page import="org.cerberus.crud.service.IRobotService"%>
<%@page import="org.cerberus.crud.service.IDocumentationService"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<% Date DatePageStart = new Date();%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Trigger Batch Execution</title>
        <%@ include file="include/dependenciesInclusions_old.html" %>
        <script type="text/javascript" src="dependencies/zz_OldDependencies/js/FixedHeader.js"></script>
        <script type="text/javascript" src="dependencies/zz_OldDependencies/js/jquery.form.js"></script>
        <!--Custom scripts inclusions-->
        <script type="text/javascript" src="js/pages/global/user.js"></script>
    </head>
    <body>
        <style>
            #contents {
                margin: 3px;
                vertical-align: top;
                display: inline-block;
                *zoom: 1;
                *display: inline;
                width: 100%;
            }

            .formForDataTable {
                display: none;
            }

            #testbatteries {
                margin: 10px 0;
            }
        </style>
        <%@ include file="include/function.jsp"%>
        <%@ include file="include/header.jsp"%></body>
    <div id="body">
        <form method="get" name="InsertPref">
        </form>
        <form method="post" name="AddToExecutionQueue" action="AddToExecutionQueue">
            <%
                IDocumentationService docService = appContext.getBean(IDocumentationService.class);
                IUserService userService = appContext.getBean(IUserService.class);
                IRobotService robService = appContext.getBean(IRobotService.class);
                IApplicationService applicationService = appContext.getBean(IApplicationService.class);
                IParameterService myParameterService = appContext.getBean(IParameterService.class);
                IInvariantService invariantService = appContext.getBean(IInvariantService.class);
                ICountryEnvParamService cepService = appContext.getBean(ICountryEnvParamService.class);
                
                AnswerList answerCountry = invariantService.readByIdname("COUNTRY");
                List<Invariant> countryListInvariant = (List<Invariant>)answerCountry.getDataList();
                String myLang = request.getAttribute("MyLang").toString();

                try {
                    User usr = userService.findUserByKey(request.getUserPrincipal().getName());

                    //Update User Preferences
                    if (StringUtils.isNotBlank(request.getParameter("RobotHost"))) {
                        usr.setRobotHost(request.getParameter("ss_ip") == null ? "" : request.getParameter("ss_ip"));
                        usr.setRobotPort(request.getParameter("ss_p") == null ? "" : request.getParameter("ss_p"));
                        usr.setRobotPlatform(request.getParameter("platform") == null ? "" : request.getParameter("platform"));
                        usr.setRobotBrowser(request.getParameter("Browser") == null ? "" : request.getParameter("Browser"));
                        usr.setRobotVersion(request.getParameter("version") == null ? "" : request.getParameter("version"));
                        userService.updateUser(usr);
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
                    if (request.getParameter("Browser") != null && request.getParameter("Browser").compareTo("") != 0) {
                        browser = request.getParameter("Browser");;
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

                    String retries;
                    if (request.getParameter("retries") != null && request.getParameter("retries").compareTo("") != 0) {
                        retries = request.getParameter("retries");
                    } else {
                        retries = new String("");
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
                        tag = StringUtil.encodeAsJavaScriptURIComponent(tag);
                    } else {
                        tag = new String("None");
                    }

                    String verbose;
                    if (request.getParameter("verbose") != null && request.getParameter("verbose").compareTo("") != 0) {
                        verbose = request.getParameter("verbose");
                    } else {
                        verbose = new String("0");
                    }

                    String screenshot;
                    if (request.getParameter("screenshot") != null && request.getParameter("screenshot").compareTo("") != 0) {
                        screenshot = request.getParameter("screenshot");
                    } else {
                        screenshot = new String("");
                    }

                    String pageSource;
                    if (request.getParameter("pageSource") != null && request.getParameter("pageSource").compareTo("") != 0) {
                        pageSource = request.getParameter("pageSource");
                    } else {
                        pageSource = new String("");
                    }

                    String seleniumLog;
                    if (request.getParameter("seleniumLog") != null && request.getParameter("seleniumLog").compareTo("") != 0) {
                        seleniumLog = request.getParameter("seleniumLog");
                    } else {
                        seleniumLog = new String("");
                    }

                    String synchroneous;
                    if (request.getParameter("synchroneous") != null && request.getParameter("synchroneous").compareTo("") != 0) {
                        synchroneous = request.getParameter("synchroneous");
                    } else {
                        synchroneous = new String("");
                    }

                    String manualExecution;
                    if (request.getParameter("manualExecution") != null && request.getParameter("manualExecution").compareTo("") != 0) {
                        manualExecution = request.getParameter("manualExecution");
                    } else {
                        manualExecution = new String("");
                    }

                    String timeout;
                    if (request.getParameter("timeout") != null && request.getParameter("timeout").compareTo("") != 0) {
                        timeout = request.getParameter("timeout");
                    } else {
                        timeout = new String("");
                    }

                    String enable = "";

                    String MySystem = request.getAttribute("MySystem").toString();
                    if (request.getParameter("system") != null && request.getParameter("system").compareTo("") != 0) {
                        MySystem = request.getParameter("system");
                    }

                    List<Application> appliList = applicationService.convert(applicationService.readBySystem(MySystem));
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

                    List<Robot> robots = robService.convert(robService.readAll());


            %>
            <input hidden="hidden" id="defTest" value="<%=test%>">
            <input hidden="hidden" id="defTestCase" value="<%=testcase%>">
            <input hidden="hidden" id="defCountry" value="<%=country%>">
            <input hidden="hidden" id="defEnvironment" value="<%=environment%>">
            <input hidden="hidden" id="defPlatform" value="<%=platform%>">
            <input hidden="hidden" id="defBrowser" value="<%=browser%>">
            <input hidden="hidden" id="defVerbose" value="<%=verbose%>">
            <input hidden="hidden" id="defScreenshot" value="<%=screenshot%>">
            <input hidden="hidden" id="defSynchroneous" value="<%=synchroneous%>">
            <input hidden="hidden" id="defTimeout" value="<%=timeout%>">
            <input hidden="hidden" id="defSeleniumLog" value="<%=seleniumLog%>">
            <input hidden="hidden" id="defPageSource" value="<%=pageSource%>">
            <input hidden="hidden" id="defManualExecution" value="<%=manualExecution%>">
            <input hidden="hidden" id="defRetries" value="<%=retries%>">
            <div style="display:block" class="filters"  id="testcasesearchdiv"></div>
            <br><br>
            <div style="display:inline-block; width:100%">
                <div class="filters" style="float:left; width:48%">
                    <div style="clear:both; display:block">
                        <% for (Invariant countryObj : countryListInvariant) {%>
                        <div style="float:left; width:30px">
                            <div class="wob" style="font-size : x-small ;"><%=countryObj.getValue()%>
                            </div>
                            <div class="wob" style="width:1px"><input value="<%=countryObj.getValue()%>" type="checkbox"
                                                                      name="Country">
                            </div>
                        </div>
                        <%} %>
                    </div>
                </div>

            </div>
            <br>
            <br>
            <div class="filters" style="clear:both; width:100%">
                <div  style="clear:both; width:100%">
                    <div style="clear:both">
                        <div style="float:left; width:100%; text-align: left">
                            <p style="float:left" class="dttTitle">Choose Environment&nbsp;&nbsp;</p>
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
                                    <span style="font-weight: normal;font-size: smaller"><% out.print(docService.findLabelHTML("invariant", "Environment", "Environment", myLang));%>
                                    </span>
                                </div>
                                <div style="float:left">
                                    <select id="environment" name="Environment" multiple style="width: 400px">
                                        <%
                                            CountryEnvParam countryEnvironmentParameter = new CountryEnvParam();
                                            countryEnvironmentParameter.setActive(true);
                                            countryEnvironmentParameter.setSystem(MySystem);
                                            HashMap envs = new HashMap();
                                            for (CountryEnvParam cep : cepService.findCountryEnvParamByCriteria(countryEnvironmentParameter)) {
                                                envs.put(cep.getEnvironment(), cep.getEnvironment());
                                            }
                                            List<String> envList = new ArrayList(envs.values());
                                            for (String env : envList) {
                                        %>
                                        <option value=<%=env%>><%=env%></option>
                                        <% } %>
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
                    <p style="float:left;" class="dttTitle">Choose Robot&nbsp;&nbsp;</p>
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
                            <div style="float:left; width:150px; text-align:left"><% out.print(docService.findLabelHTML("page_runtests", "SeleniumServerIP", "Selenium Server IP ", myLang));%>
                            </div>
                            <div style="float:left">
                                <input type="text" name="ss_ip" id="ss_ip" value="<%= ssIP%>" style="float:left; width:150px;" />
                            </div>
                        </div>
                        <div style="clear:both">
                            <div style="float:left; width:150px; text-align:left"><% out.print(docService.findLabelHTML("page_runtests", "SeleniumServerPort", "Selenium Server Port ", myLang));%>
                            </div>
                            <div style="float:left">
                                <input type="text" name="ss_p" id="ss_p" value="<%= ssPort%>" style="float:left; width:150px;"/>
                            </div>
                        </div>
                        <div style="clear:both">
                            <div style="float:left; width:150px; text-align:left"><% out.print(docService.findLabelHTML("page_runtests", "Browser", "Browser", myLang));%>
                            </div>
                            <div style="float:left">
                                <select id="Browser" multiple name="Browser" style="width: 150px;" class="<%=browserClass%>">
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
                    <div style="float:left; width:150px; text-align:left"><% out.print(docService.findLabelHTML("testcaseexecution", "tag", "Tag", myLang));%>
                    </div>
                    <div style="float:left">
                        <input id="tag" name="Tag" style="width: 200px">
                    </div>
                </div>
                <div style="clear:both">
                    <div style="float:left;width:150px; text-align:left"><% out.print(docService.findLabelHTML("page_runtests", "outputformat", "", myLang));%>
                    </div>
                    <div style="float:left">
                        <select id="outputformat" name="outputformat" style="width: 200px">
                        </select>
                    </div>
                </div>
                <div style="clear:both">
                    <div style="float:left;width:150px;text-align:left "><% out.print(docService.findLabelHTML("testcaseexecution", "verbose", "", myLang));%>
                    </div>
                    <div style="float:left">
                        <select id="verbose" name="verbose" style="width: 200px">
                        </select>
                    </div>
                </div>
                <div style="clear:both">
                    <div style="float:left;width:150px; text-align:left"><% out.print(docService.findLabelHTML("page_runtests", "screenshot", "", myLang));%>
                    </div>
                    <div style="float:left">
                        <select id="screenshot" name="screenshot" style="width: 200px">
                        </select>
                    </div>
                </div>
                <div style="clear:both">
                    <div style="float:left;width:150px; text-align:left"><% out.print(docService.findLabelHTML("page_runtests", "pageSource", "", myLang));%>
                    </div>
                    <div style="float:left">
                        <select id="pageSource" name="pageSource" style="width: 200px">
                        </select>
                    </div>
                </div>
                <div style="clear:both">
                    <div style="float:left;width:150px; text-align:left"><% out.print(docService.findLabelHTML("page_runtests", "seleniumLog", "", myLang));%>
                    </div>
                    <div style="float:left">
                        <select id="seleniumLog" name="seleniumLog" style="width: 200px">
                        </select>
                    </div>
                </div>
                <div style="clear:both">
                    <div style="float:left;width:150px; text-align:left"><% out.print(docService.findLabelHTML("page_runtests", "synchroneous", "", myLang));%>
                    </div>
                    <div style="float:left">
                        <select id="synchroneous" name="synchroneous" style="width: 200px">
                        </select>
                    </div>
                </div>
                <div style="clear:both">
                    <div style="float:left;width:150px; text-align:left"><% out.print(docService.findLabelHTML("page_runtests", "timeout", "", myLang));%>
                    </div>
                    <div style="float:left">
                        <input id="timeout" name="timeout" style="width: 200px">
                    </div>
                </div>
                <div style="clear:both">
                    <div style="float:left;width:150px; text-align:left"><% out.print(docService.findLabelHTML("page_runtests", "retries", "", myLang));%>
                    </div>
                    <div style="float:left">
                        <select id="retries" name="retries" style="width: 200px">
                        </select>
                    </div>
                </div>
                <div style="clear:both">
                    <div style="float:left;width:150px; text-align:left"><% out.print(docService.findLabelHTML("page_runtests", "manualExecution", "", myLang));%>
                    </div>
                    <div style="float:left">
                        <select id="manualExecution" name="manualExecution" style="width: 200px"></select>
                    </div>
                </div>
                <div id="recordButtonDiv" style="clear:both">
                    <input id="button" class="button" type="button" onclick="recordExecutionParam()" <%=enable%> name="execParam" value="Record my Execution Parameters" >
                </div>
            </div>
            <div style="clear:both">          
                <br>
            </div>
            <div style="float:left">
                <input type="submit" style="background-image: url(images/play.png);background-size: 100%; width: 40px; height: 40px; border: 0 none; top: 0px" id="buttonRun">
            </div>
            <div style="display:none; float:left">
                <input name="statusPage">
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
    <div id="popin" title="Manual Execution"></div>
    <br><br><br>
    <div style="clear:both"><% out.print(display_footer(DatePageStart));%></div>
    <script type="text/javascript">
        function checkTestcaseGroupAndPerformTest() {
            var test = $("#test option:selected").val();
            var testcase = $("#testcase option:selected").val();
            var env = $("#environment option:selected").val();
            var country = $("#country option:selected").val();
            var manualExec = $("#manualExecution option:selected").val();
            $.getJSON('GetTestCase?test=' + test + "&testcase=" + testcase, function(data) {


                if (manualExec === "Y") {
                    openRunManualPopin(test, testcase, env, country);
                } else {
                    if (data.group === "MANUAL") {
                        alert("You cannot automatically execute manual testcase");
                    } else {
                        $("[name='statusPage']").val("Run");
                        $("[name='RunTest']").submit();
                    }
                }

            });


        }
    </script>
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


        $(document).ready(function() {
            $.getJSON('GetTestBySystem?system=' + systemSelected, function(data) {
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
                if (tcS !== "%%") {
                    getTestCaseList();
                }

            });
        });



    </script>
    <script>
        function addToSelectedTest() {

            $.each($("input[name='testcaseselected']:checked"), function(index, element) {
                var tst = $(element).attr("data-test");
                var tstcse = $(element).attr("data-testcase");
                $("#testSelected").append($("<input></input>")
                        .attr("name", "SelectedTest")
                        .attr("style", "width:100%")
                        .attr("value", $(element).val())
                        .text($(element).val()));
                $("#row_" + tst + "_" + tstcse).remove();
            });
        }
    </script>
    <script type="text/javascript">
        function getTestCaseList() {
            var sys = document.getElementById("MySystem");
            var systemSelected = sys.options[sys.selectedIndex].value;
            var b = document.getElementById("test");
            var testSelected = b.options[b.selectedIndex].value;
            var tcS = document.getElementById("defTestCase").value;
            var countryS = document.getElementById("defCountry").value;

            $.getJSON('GetTestCaseForTest?system=' + systemSelected + '&test=' + testSelected, function(data) {
                $("#testcase").empty();
                $("#country").empty();
                $("#myenvdata").empty();

                for (var i = 0; i < data.testCaseList.length; i++) {
                    $("#testcase").append($("<option></option>")
                            .attr("value", data.testCaseList[i].testCase)
                            .attr("data-testcase", data.testCaseList[i].testCase)
                            .attr("data-application", data.testCaseList[i].application)
                            .text(data.testCaseList[i].description));
                }
                $("#testcase").find('option').each(function(i, opt) {
                    if (opt.value === tcS)
                        $(opt).attr('selected', 'selected');
                });

                getCountryList();
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
            var countryS = document.getElementById("defCountry").value;
            var env = document.getElementById("defEnvironment").value;

            $.getJSON('GetCountryForTestCase?test=' + testSelected + '&testCase=' + testCaseSelected, function(data) {
                $("#country").empty();
                $("#environment").empty();
                $("#myenvdata").empty();

                for (var i = 0; i < data.countriesList.length; i++) {
                    $("#country").append($("<option></option>")
                            .attr("value", data.countriesList[i])
                            .text(data.countriesList[i]));
                }
                $("#country").find('option').each(function(i, opt) {
                    if (opt.value === countryS)
                        $(opt).attr('selected', 'selected');
                });
                if (env !== "%%") {
                    getApplicationList();
                }

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
            var env = document.getElementById("defEnvironment").value;

            $.getJSON('findEnvironmentByCriteria?system=' + systemSelected + '&country=' + countrySelected + '&application=' + applicationSelected, function(data) {
                $("#environment").empty();
                $("#myenvdata").empty();

                for (var i = 0; i < data.length; i++) {
                    $("#environment").append($("<option></option>")
                            .attr("value", data[i].environment)
                            .text(data[i].environment + " [ " + data[i].ip + data[i].url + " ] with Build :" + data[i].build + " and Revision :" + data[i].revision));
                    $("#myenvdata").append($("<option></option>")
                            .attr("value", data[i].environment)
                            .text(data[i].environment + " [ " + data[i].ip + data[i].url + " ] with Build :" + data[i].build + " and Revision :" + data[i].revision));
                }
                $("#environment").find('option').each(function(i, opt) {
                    if (opt.value === env)
                        $(opt).attr('selected', 'selected');
                });

            });
        }
        ;
    </script>
    <script type="text/javascript">
        $(document).ready(function() {
            $.getJSON('FindInvariantByID?idName=RETRIES', function(data) {
                $("#retries").empty();
                var pl = document.getElementById("defRetries").value;

                for (var i = 0; i < data.length; i++) {
                    $("#retries").append($("<option></option>")
                            .attr("value", data[i].value)
                            .text(data[i].value + " ( " + data[i].description + " )"));
                }

                setCookie('ExecutionRetries', 'retries');

                var ExecutionRetries = getCookie('ExecutionRetries');

                if (ExecutionRetries === "" && pl === "") {
                    pl = "0";
                }



                $("#retries").find('option').each(function(i, opt) {
                    if (opt.value === pl) {
                        $(opt).attr('selected', 'selected');
                    }


                });
            })
        });

    </script>
    <script type="text/javascript">
        $(document).ready(function() {
            $.getJSON('FindInvariantByID?idName=OUTPUTFORMAT', function(data) {
                $("#outputformat").empty();

                for (var i = 0; i < data.length; i++) {
                    $("#outputformat").append($("<option></option>")
                            .attr("value", data[i].value)
                            .text(data[i].value + " ( " + data[i].description + " )"));
                }


                setCookie('OutputFormatPreference', 'outputformat');
            })
        });

    </script>
    <script type="text/javascript">
        $(document).ready(function() {
            $.getJSON('FindInvariantByID?idName=VERBOSE', function(data) {
                $("#verbose").empty();
                var pl = document.getElementById("defVerbose").value;

                for (var i = 0; i < data.length; i++) {
                    $("#verbose").append($("<option></option>")
                            .attr("value", data[i].value)
                            .text(data[i].value + " ( " + data[i].description + " )"));
                }

                setCookie('VerbosePreference', 'verbose');

                $("#verbose").find('option').each(function(i, opt) {
                    if (opt.value === pl) {
                        $(opt).attr('selected', 'selected');
                    }


                });

            })
        });

    </script>
    <script type="text/javascript">
        $(document).ready(function() {
            $.getJSON('FindInvariantByID?idName=SYNCHRONEOUS', function(data) {
                $("#synchroneous").empty();
                var pl = document.getElementById("defSynchroneous").value;

                for (var i = 0; i < data.length; i++) {
                    $("#synchroneous").append($("<option></option>")
                            .attr("value", data[i].value)
                            .text(data[i].value + " ( " + data[i].description + " )"));
                }

                setCookie('SynchroneousPreference', 'synchroneous');

                $("#synchroneous").find('option').each(function(i, opt) {
                    if (opt.value === pl) {
                        $(opt).attr('selected', 'selected');
                    }


                });

            })
        });

    </script>
    <script type="text/javascript">
        $(document).ready(function() {
            $.getJSON('FindInvariantByID?idName=MANUALEXECUTION', function(data) {
                $("#manualExecution").empty();

                var pl = document.getElementById("defManualExecution").value;

                for (var i = 0; i < data.length; i++) {
                    $("#manualExecution").append($("<option></option>")
                            .attr("value", data[i].value)
                            .text(data[i].value + " ( " + data[i].description + " )"));
                }

                setCookie('ManualExecutionPreference', 'manualExecution');

                $("#manualExecution").find('option').each(function(i, opt) {
                    if (opt.value === pl) {
                        $(opt).attr('selected', 'selected');
                    }


                });

            })
        });

    </script>
    <script type="text/javascript">
        $(document).ready(function() {
            $.getJSON('FindInvariantByID?idName=SCREENSHOT', function(data) {
                $("#screenshot").empty();
                var pl = document.getElementById("defScreenshot").value;

                for (var i = 0; i < data.length; i++) {
                    $("#screenshot").append($("<option></option>")
                            .attr("value", data[i].value)
                            .text(data[i].value + " ( " + data[i].description + " )"));
                }

                setCookie('ScreenshotPreference', 'screenshot');

                var screenCookie = getCookie('ScreenshotPreference');

                if (screenCookie === "" && pl === "") {
                    pl = "1";
                }



                $("#screenshot").find('option').each(function(i, opt) {
                    if (opt.value === pl) {
                        $(opt).attr('selected', 'selected');
                    }


                });
            })
        });

    </script>
    <script type="text/javascript">
        $(document).ready(function() {
            $.getJSON('FindInvariantByID?idName=PAGESOURCE', function(data) {
                $("#pageSource").empty();
                var pl = document.getElementById("defPageSource").value;

                for (var i = 0; i < data.length; i++) {
                    $("#pageSource").append($("<option></option>")
                            .attr("value", data[i].value)
                            .text(data[i].value + " ( " + data[i].description + " )"));
                }

                setCookie('PageSourcePreference', 'pageSource');

                var pageCookie = getCookie('PageSourcePreference');

                if (pageCookie === "" && pl === "") {
                    pl = "1";
                }



                $("#pageSource").find('option').each(function(i, opt) {
                    if (opt.value === pl) {
                        $(opt).attr('selected', 'selected');
                    }


                });
            })
        });

    </script>
    <script type="text/javascript">
        $(document).ready(function() {
            $.getJSON('FindInvariantByID?idName=SELENIUMLOG', function(data) {
                $("#seleniumLog").empty();
                var pl = document.getElementById("defSeleniumLog").value;

                for (var i = 0; i < data.length; i++) {
                    $("#seleniumLog").append($("<option></option>")
                            .attr("value", data[i].value)
                            .text(data[i].value + " ( " + data[i].description + " )"));
                }

                setCookie('SeleniumLogPreference', 'seleniumLog');

                var seleniumLogCookie = getCookie('SeleniumLogPreference');

                if (seleniumLogCookie === "" && pl === "") {
                    pl = "1";
                }



                $("#seleniumLog").find('option').each(function(i, opt) {
                    if (opt.value === pl) {
                        $(opt).attr('selected', 'selected');
                    }


                });
            })
        });

    </script>
    <script type="text/javascript">
        $(document).ready(function() {
            $.getJSON('FindInvariantByID?idName=BROWSER', function(data) {
                $("#Browser").empty();
                var pl = document.getElementById("defBrowser").value;

                for (var i = 0; i < data.length; i++) {
                    $("#Browser").append($("<option></option>")
                            .attr("value", data[i].value)
                            .text(data[i].value + " ( " + data[i].description + " )"));
                }
                $("#Browser").find('option').each(function(i, opt) {
                    if (opt.value === pl) {
                        $(opt).attr('selected', 'selected');
                    }


                });
            })
        });

    </script>
    <script type="text/javascript">
        $(document).ready(function() {
            $.getJSON('FindInvariantByID?idName=PLATFORM', function(data) {
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
            })
        });

    </script>
    <script type="text/javascript">
        $(document).ready(function() {
            $("#tag").empty();
            setCookie('TagPreference', 'tag');
        });

    </script>
    <script type="text/javascript">
        $(document).ready(function() {
            $("#timeout").empty();
            setCookie('TimeoutPreference', 'timeout');
        });

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
            var br = document.getElementById("Browser");
            var b = br.options[br.selectedIndex].value;
            var v = document.getElementById("version").value;
            var pla = document.getElementById("platform");
            var pl = pla.options[pla.selectedIndex].value;
            $("#recordButtonDiv").append('<img id="loader" src="images/loading.gif">');
            var xhttp = new XMLHttpRequest();
            xhttp.open("GET", "UpdateMyUserRobotPreference?ss_ip=" + ip + "&ss_p=" + p + "&browser=" + b + "&version=" + v + "&platform=" + pl, false);
            xhttp.send();
            var xmlDoc = xhttp.responseText;
            $("#loader").remove();
        }
    </script>
    <script>
        function recordExecutionParam() {
            var expiration_date = new Date();
            expiration_date.setFullYear(expiration_date.getFullYear() + 1);

            var prefTag = $("#tag").val();
            var prefOf = $("#outputformat").val();
            var prefVerb = $("#verbose").val();
            var prefScreen = $("#screenshot").val();
            var prefSynch = $("#synchroneous").val();
            var prefTimeOut = $("#timeout").val();
            var prefPS = $("#pageSource").val();
            var prefSL = $("#seleniumLog").val();
            var prefME = $("#manualExecution").val();
            var prefRt = $("#retries").val();
            document.cookie = "TagPreference=" + prefTag + ";expires=" + expiration_date.toGMTString();
            document.cookie = "OutputFormatPreference=" + prefOf + ";expires=" + expiration_date.toGMTString();
            document.cookie = "VerbosePreference=" + prefVerb + ";expires=" + expiration_date.toGMTString();
            document.cookie = "ScreenshotPreference=" + prefScreen + ";expires=" + expiration_date.toGMTString();
            document.cookie = "SynchroneousPreference=" + prefSynch + ";expires=" + expiration_date.toGMTString();
            document.cookie = "TimeoutPreference=" + prefTimeOut + ";expires=" + expiration_date.toGMTString();
            document.cookie = "PageSourcePreference=" + prefPS + ";expires=" + expiration_date.toGMTString();
            document.cookie = "SeleniumLogPreference=" + prefSL + ";expires=" + expiration_date.toGMTString();
            document.cookie = "ManualExecutionPreference=" + prefME + ";expires=" + expiration_date.toGMTString();
            document.cookie = "ExecutionRetries=" + prefRt + ";expires=" + expiration_date.toGMTString();
        }
    </script>
    <script>
        function setCookie(cookieName, element) {
            var name = cookieName + "=";
            var ca = document.cookie.split(';');
            for (var i = 0; i < ca.length; i++) {
                var c = ca[i].trim();
                var val = c.split('=')[1];
                if (c.indexOf(name) === 0) {
                    document.getElementById(element).value = val;
                }
            }
        }
    </script>
    <script>
        function getCookie(cname) {
            var name = cname + "=";
            var ca = document.cookie.split(';');
            for (var i = 0; i < ca.length; i++) {
                var c = ca[i].trim();
                if (c.indexOf(name) === 0)
                    return c.substring(name.length, c.length);
            }
            return "";
        }
    </script>
    <script>
        $(document).ready(function() {
            var system = $('#MySystem').val();
            var myLang = getUser().language;
            $('#testcasesearchdiv').load("RunTestsSearchFilters.jsp?system=" + system + "&MyLang=" + myLang, function() {


                $(".multiSelectOptions").each(function() {
                    var currentElement = $(this);

                    if (currentElement.attr("id") === "System") {
                        currentElement.multiselect({
                            multiple: true,
                            minWidth: 150,
//                        header: currentElement.data('header'),
                            noneSelectedText: currentElement.data('none-selected-text'),
                            selectedText: currentElement.data('selected-text'),
                            selectedList: currentElement.data('selected-list'),
                            beforeclose: function() {
                                var system = $("#System").val();
                                var appSelect = $("#Application");


                                if (system === null) {
                                    appSelect.find("option").removeAttr('disabled');
                                    appSelect.find("option").removeAttr('selected');
                                } else {
                                    if (oldSystem != null) {
                                        $.each(oldSystem, function(i, v) {
                                            if ($.inArray(v, system) === -1) {
                                                appSelect.find("option:contains('[" + v + "]')").removeAttr('selected');
                                            }
                                        });
                                    }

                                    if ($.inArray("All", system) >= 0) {
                                        appSelect.find("option").removeAttr('disabled');
                                    } else {
                                        appSelect.find("option").attr('disabled', 'disabled');
                                        $.each(system, function(i, v) {
                                            appSelect.find("option:contains('[" + v + "]')").removeAttr('disabled');
                                        });
                                    }
                                }
                                appSelect.multiselect("refresh");

                                oldSystem = system;
                            }
                        }).multiselectfilter();
                    } else {
                        currentElement.multiselect({
                            multiple: true,
                            minWidth: 150,
//                        header: currentElement.data('header'),
                            noneSelectedText: currentElement.data('none-selected-text'),
                            selectedText: currentElement.data('selected-text'),
                            selectedList: currentElement.data('selected-list')
                        }).multiselectfilter();
                    }
                });
            });
        });
    </script>
</body>
</html>
