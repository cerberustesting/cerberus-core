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
<%@page import="org.cerberus.entity.SqlLibrary"%>
<%@page import="org.cerberus.service.ISqlLibraryService"%>
<%@page import="org.cerberus.entity.TestCaseCountryProperties"%>
<%@page import="org.cerberus.service.ITestCaseCountryPropertiesService"%>
<%@page import="org.cerberus.entity.TestCaseStepActionControl"%>
<%@page import="org.cerberus.entity.TestCaseStepAction"%>
<%@page import="org.cerberus.entity.TestCaseStep"%>
<%@page import="org.cerberus.service.ITestCaseStepActionControlService"%>
<%@page import="org.cerberus.service.ITestCaseStepActionService"%>
<%@page import="org.cerberus.service.ITestCaseStepService"%>
<%@page import="org.cerberus.service.ITestCaseExecutionService"%>
<%@page import="org.cerberus.entity.TestCaseExecution"%>
<%@page import="org.cerberus.service.ITestCaseCountryService"%>
<%@page import="org.cerberus.entity.TCase"%>
<%@page import="org.cerberus.service.ITestCaseService"%>
<%@page import="org.cerberus.entity.Test"%>
<%@page import="org.cerberus.service.ITestService"%>
<%@page import="java.util.Enumeration"%>
<%@page import="org.cerberus.entity.Parameter"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>
<%@page import="java.sql.ResultSet"%>
<%@page import="java.sql.Statement"%>
<%@page import="java.sql.Connection"%>
<%@page import="java.net.URLEncoder"%>
<%@page import="org.cerberus.entity.BuildRevisionInvariant"%>
<%@page import="org.cerberus.entity.Application"%>
<%@page import="org.cerberus.service.IDocumentationService"%>
<%@page import="org.cerberus.service.impl.BuildRevisionInvariantService"%>
<%@page import="org.cerberus.service.IBuildRevisionInvariantService"%>
<%@page import="org.cerberus.service.IApplicationService"%>
<%@page import="org.cerberus.service.IParameterService"%>
<%@page import="org.cerberus.util.StringUtil"%>
<%@page import="org.apache.commons.lang3.StringUtils"%>
<% Date DatePageStart = new Date();%>

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta content="text/html; charset=UTF-8" http-equiv="content-type">
        <title>TestCase</title>

        <script type='text/javascript' src='js/Form_1.js'></script>
        <script type="text/javascript" src="js/jquery-1.9.1.min.js"></script>
        <script type="text/javascript" src="js/jquery-migrate-1.2.1.min.js"></script>
        <script type="text/javascript" src="js/jquery-ui-1.10.2.js"></script>
        <script type="text/javascript" src="js/elrte.min.js"></script>
        <script type="text/javascript" src="js/i18n/elrte.en.js"></script>
        <script type="text/javascript" src="js/elfinder.min.js"></script>
        <script type="text/javascript" src="js/elFinderSupportVer1.js"></script>
        <link rel="stylesheet" type="text/css" href="css/jquery-ui.css">
        <link rel="stylesheet" type="text/css" href="css/elrte.min.css">
        <link rel="stylesheet" type="text/css" href="css/crb_style.css">
        <link rel="stylesheet" type="text/css" href="css/elfinder.min.css">
        <link rel="stylesheet" type="text/css" href="css/theme.css">
        <link rel="shortcut icon" type="image/x-icon" href="images/favicon.ico" />

        <script type="text/javascript">
            var displayOnlyFunctional = false;
            function showOnlyFunctional() {
                displayOnlyFunctional = !displayOnlyFunctional;
                $('.functional_description').toggleClass('only_functional_description_size');
                $('.functional_description_control').toggleClass('only_functional_description_control_size');
                $('.technical_part').toggleClass('only_functional');
                SetCookie("displayOnlyFunctional",(displayOnlyFunctional ? "TRUE" : "FALSE"));
            }

            $().ready(function() {
                if("TRUE" == GetCookie("displayOnlyFunctional")) {
                    showOnlyFunctional();
                };

                elRTE.prototype.options.toolbars.cerberus = ['style', 'alignment', 'colors', 'images', 'format', 'indent', 'lists', 'links'];
                var opts = {
                    lang: 'en',
                    styleWithCSS: false,
                    width: 615,
                    height: 200,
                    toolbar: 'cerberus',
                    allowSource: false,
                    cssfiles: ['css/crb_style.css'],
                    fmOpen: function(callback) {
                        $('<div />').dialogelfinder({
                            url: 'PictureConnector',
                            transport: new elFinderSupportVer1(),
                            commandsOptions: {
                                getfile: {
                                    oncomplete: 'destroy' // destroy elFinder after file selection
                                }
                            },
                            getFileCallback: callback // pass callback to file manager
                        });
                    }
                };
                var bool = $('#generalparameter').is(':visible');

                //plugin must be added with input visible - error NS_ERROR_FAILURE: Failure
                if (!bool) {
                    $('#generalparameter').show();
                }
                $('#howto').elrte(opts);
                $('#value').elrte(opts);
                $('.el-rte').css('z-index', 0);
                //plugin must be added with input visible - error NS_ERROR_FAILURE: Failure
                if (!bool) {
                    $('#generalparameter').hide();
                }
            });
        </script>
        <script type="text/javascript">
            var track = 0;
            function trackChanges(originalValue, newValue, element) {
                if (originalValue != newValue) {
                    window.track = window.track + 1;
                } else {
                    window.track = window.track - 1;
                }
            }
        </script>
        <script>
            function alertMessage(message) {
                alert(message);
            }
        </script>
        <style>
            .only_functional {
                display: none;
            }

            .only_functional_description_size {
                width: 864px;
            }

            .only_functional_description_control_size {
                width: 787px;
            }

        </style>
    </head>
    <body>
        <%@ include file="include/function.jsp" %>
        <%@ include file="include/header.jsp" %>
        <div id="body">
            <%
                boolean booleanFunction = false;
                try {
                    /*
                     * Services
                     */
                    IDocumentationService docService = appContext.getBean(IDocumentationService.class);
                    IApplicationService myApplicationService = appContext.getBean(IApplicationService.class);
                    IParameterService parameterService = appContext.getBean(IParameterService.class);
                    IBuildRevisionInvariantService buildRevisionInvariantService = appContext.getBean(BuildRevisionInvariantService.class);
                    ITestService testService = appContext.getBean(ITestService.class);
                    ITestCaseService testCaseService = appContext.getBean(ITestCaseService.class);
                    ITestCaseCountryService testCaseCountryService = appContext.getBean(ITestCaseCountryService.class);
                    ITestCaseStepService tcsService = appContext.getBean(ITestCaseStepService.class);
                    ITestCaseStepActionService tcsaService = appContext.getBean(ITestCaseStepActionService.class);
                    ITestCaseStepActionControlService tcsacService = appContext.getBean(ITestCaseStepActionControlService.class);
                    ITestCaseCountryPropertiesService tccpService = appContext.getBean(ITestCaseCountryPropertiesService.class);
                    ISqlLibraryService libService = appContext.getBean(ISqlLibraryService.class);
                    ITestCaseExecutionService testCaseExecutionService = appContext.getBean(ITestCaseExecutionService.class);
                    IInvariantService invariantService = appContext.getBean(IInvariantService.class);

                    /**
                     * Function
                     */
                    booleanFunction = StringUtil.parseBoolean(parameterService.findParameterByKey("cerberus_testcase_function_booleanListOfFunction", "").getValue());
                    String listOfFunction = "";
                    if (booleanFunction) {
                        Parameter functions = parameterService.findParameterByKey("cerberus_testcase_function_urlForListOfFunction", "");
                        listOfFunction = functions.getValue();
                    }

                    /**
                     * String init
                     */
                    String SitdmossBugtrackingURL;
                    SitdmossBugtrackingURL = "";
                    String appSystem = "";
                    String proplist = "";

                    /*
                     * Get Parameters
                     */
                    String MySystem = request.getAttribute("MySystem").toString();
                    if (request.getParameter("system") != null && request.getParameter("system").compareTo("") != 0) {
                        MySystem = request.getParameter("system");
                    }

                    String group = getRequestParameterWildcardIfEmpty(request, "group");
                    String status = getRequestParameterWildcardIfEmpty(request, "status");
                    String test = getRequestParameterWildcardIfEmpty(request, "Test");
                    String testcase = getRequestParameterWildcardIfEmpty(request, "TestCase");
                    Boolean tinf = getBooleanParameterFalseIfEmpty(request, "Tinf");
            %>
            <input id="urlForListOffunction" value="<%=listOfFunction%>" style="display:none">
            <form action="TestCase_1.jsp" method="post" name="selectTestCase" id="selectTestCase">
                <div class="filters" style="float:left; width:100%; height:30px">
                    <div style="float:left; width:100px">
                        <p class="dttTitle">Filters
                        </p>
                    </div>
                    <div style="float:left; width:100px;font-weight: bold;"><%out.print(docService.findLabelHTML("test", "test", "Test"));%>
                    </div>
                    <div style="float:left">
                        <select id="filtertest" name="Test" style="width: 200px" OnChange="document.selectTestCase.submit()">
                            <%  if (test.compareTo("%%") == 0) { %>
                            <option style="width: 200px" value="All">-- Choose Test --
                            </option>
                            <%  }
                                List<Test> tests = testService.getListOfTest();
                                for (Test tst : tests) {%>
                            <option style="width: 200px;" class="font_weight_bold_<%=tst.getActive()%>" value="<%=tst.getTest()%>" <%=test.compareTo(tst.getTest()) == 0 ? " SELECTED " : ""%>><%=tst.getTest()%>
                            </option>
                            <% } %>
                        </select>
                    </div>
                    <div style="float:left"><%out.print(docService.findLabelHTML("testcase", "testcase", "TestCase"));%>
                    </div>
                    <div style="float:left">
                        <select id="filtertestcase" name="TestCase" style="width: 850px" OnChange="document.selectTestCase.submit()">
                            <% if (test.compareTo("%%") == 0) { %>
                            <option style="width: 850px" value="All">-- Choose Test First --
                            </option>
                            <%  } else {
                                List<TCase> tcList = testCaseService.findTestCaseByTest(test);
                                for (TCase tc : tcList) {%>
                            <option style="width: 850px;" class="font_weight_bold_<%=tc.getActive()%>" value="<%=tc.getTestCase()%>" <%=testcase.compareTo(tc.getTestCase()) == 0 ? " SELECTED " : ""%>><%=tc.getTestCase()%>  [<%=tc.getApplication()%>]  : <%=tc.getShortDescription()%>
                            </option>
                            <%  }
                                } %>
                        </select>
                    </div>
                    <div style="float:left">
                        <input id="loadbutton" class="button" type="submit" name="Load" value="Load">
                    </div>
                </div>
            </form>
            <br>
            <br>
            <%if (!test.equals("%%") && !testcase.equals("%%")) {
                    TCase tcase = testCaseService.findTestCaseByKey(test, testcase);
                    List<Invariant> countryListInvariant = invariantService.findListOfInvariantById("COUNTRY");
                    List<String> countryListTestcase = testCaseCountryService.findListOfCountryByTestTestCase(test, testcase);
                    TestCaseExecution tce = testCaseExecutionService.findLastTestCaseExecutionNotPE(test, testcase);
                    List<BuildRevisionInvariant> listBuildRev = buildRevisionInvariantService.findAllBuildRevisionInvariantBySystemLevel(MySystem, 1);
                    List<TestCaseCountryProperties> tccpList = tccpService.findDistinctPropertiesOfTestCase(test, testcase);

                    group = tcase.getGroup();
                    status = tcase.getStatus();
                    String dateCrea = tcase.getTcDateCrea() != null ? tcase.getTcDateCrea() : "-- unknown --";
                    // Define the list of country available for this test
                    String countries = "";
                    for (String c : countryListTestcase) {
                        countries += c + "-";
                    }

                    Application myApplication = null;
                    if (tcase.getApplication() != null) {
                        myApplication = myApplicationService.findApplicationByKey(tcase.getApplication());
                        appSystem = myApplication.getSystem();
                        SitdmossBugtrackingURL = myApplication.getBugTrackerUrl();
                    } else {
                        appSystem = "";
                        SitdmossBugtrackingURL = "";
                    }

                    /**
                     * We can edit the testcase only if User role is TestAdmin
                     * or if role is Test and testcase is not WORKING
                     */
                    boolean canEdit = false;
                    if (request.getUserPrincipal() != null
                            && (request.isUserInRole("TestAdmin")) || ((request.isUserInRole("Test")) && !(status.equalsIgnoreCase("WORKING")))) {
                        canEdit = true;
                    }

                    boolean canDelete = false;
                    if (request.getUserPrincipal() != null && request.isUserInRole("TestAdmin")) {
                        canDelete = true;
                    }

            %>
            <br>
            <form method="post" name="UpdateTestCase"  id="UpdateTestCase" action="UpdateTestCaseWithDependencies">
                <table id="generalparameter" class="arrond"
                       <%if (tinf == false) {%> style="display : none" <%} else {%>style="display : table"<%}%> >
                    <tr>
                        <td class="separation">
                            <table  class="wob" style="text-align: left; border-collapse: collapse" border="0px" cellpadding="0px" cellspacing="0px">
                                <tr>
                                    <td colspan="2" class="wob">
                                        <h4 style="color : blue">Test Information
                                        </h4>
                                    </td>
                                    <td id="wob">
                                    </td>
                                    <td id="wob">
                                    </td>
                                    <td id="wob" align="right">
                                        <input id="button2" style="height:18px; width:10px" type="button" value="-" onclick="javascript:setInvisible();">
                                    </td>
                                </tr>    
                                <tr id="header"> 
                                    <td class="wob" style="width: 100px"><%out.print(docService.findLabelHTML("test", "test", "Test"));%>
                                    </td>
                                    <td class="wob" style="width: 100px"><%out.print(docService.findLabelHTML("testcase", "testcase", "TestCase"));%>
                                    </td>
                                    <td class="wob" style="width: 960px"><%out.print(docService.findLabelHTML("test", "description", "Description"));%>
                                    </td>
                                </tr>
                                <tr>
                                    <td class="wob">
                                        <input id="informationTest" style="width: 90px; font-weight: bold;" name="informationTest" value="<%=tcase.getTest()%>">
                                        <input id="informationInitialTest" type="hidden" name="informationInitialTest" value="<%=tcase.getTest()%>">
                                    </td>
                                    <td class="wob">
                                        <input id="informationTestCase" style="width: 90px; font-weight: bold;" name="informationTestCase" value="<%=tcase.getTestCase()%>">
                                        <input id="informationInitialTestCase" type="hidden" name="informationInitialTestCase" value="<%=tcase.getTestCase()%>">
                                    </td>
                                    <td class="wob">
                                        <input id="informationTestDescription" style="width: 950px; background-color: #DCDCDC" name="informationTestDescription" readonly="readonly"
                                               value="<%=tcase.getShortDescription()%>">
                                    </td>
                                </tr>
                            </table>
                            <br>
                            <%  if (canDelete) {%>
                            <input type="button" id="deleteTC" name="deleteTC" value="delete" onclick="javascript:deleteTestCase('<%=test%>', '<%=testcase%>', 'TestCase.jsp')">
                            <input type="button" id="exportTC" name="exportTC" value="exportTestCase" onclick="javascript:exportTestCase('<%=test%>', '<%=testcase%>', 'TestCase.jsp')">
                            <div id="deleteTCDiv">
                            </div>
                            <% }%>
                        </td>
                    </tr>
                    <tr>
                        <td class="separation">
                            <table style="text-align: left; border-collapse: collapse" border="0px" cellpadding="0px" cellspacing="0px">
                                <tr>
                                    <td class="wob">
                                        <table class="wob" style="text-align: left; border-collapse: collapse" border="0px" cellpadding="0px" cellspacing="0px">
                                            <tr>
                                                <td class="wob" colspan="2"><h4 style="color : blue">TestCase Information</h4></td>
                                            </tr>
                                            <tr id="header">  
                                                <td class="wob" style="width: 100px"><%out.print(docService.findLabelHTML("testcase", "origine", "Origin"));%></td>
                                                <td class="wob" style="width: 100px"><%out.print(docService.findLabelHTML("testcase", "RefOrigine", "RefOrigine"));%></td>
                                                <td class="wob" style="width: 150px"><%out.print(docService.findLabelHTML("testcase", "TCDateCrea", "Creation date"));%></td>
                                                <td class="wob" style="width: 100px"><%out.print(docService.findLabelHTML("testcase", "Creator", "creator"));%></td>
                                                <td class="wob" style="width: 100px"><%out.print(docService.findLabelHTML("testcase", "Implementer", "implementer"));%></td>
                                                <td class="wob" style="width: 100px"><%out.print(docService.findLabelHTML("testcase", "LastModifier", "lastModifier"));%></td>
                                                <td class="wob" style="width: 100px"><%out.print(docService.findLabelHTML("project", "idproject", "Project"));%></td>
                                                <td class="wob" style="width: 100px"><%out.print(docService.findLabelHTML("testcase", "ticket", "Ticket"));%></td>
                                                <td class="wob" style="width: 400px"><%out.print(docService.findLabelHTML("testcase", "Function", "Function"));%></td>
                                            </tr>
                                            <tr>
                                                <td class="wob"><input id="editOrigin" style="width: 90px;" name="editOrigin" value="<%=tcase.getOrigin()%>"></td>
                                                <td class="wob"><input id="editRefOrigin" style="width: 90px;  background-color: #DCDCDC" name="editRefOrigin" value="<%=tcase.getRefOrigin()%>"></td>
                                                <td class="wob"><%=dateCrea%></td>
                                                <td class="wob"><input readonly="readonly" id="editCreator" style="width: 90px; background-color: #DCDCDC" name="editCreator" value="<%=tcase.getCreator()%>"></td>
                                                <td class="wob"><input id="editImplementer" style="width: 90px;" name="editImplementer" value="<%=tcase.getImplementer() == null ? "" : tcase.getImplementer()%>"></td>
                                                <td class="wob"><input readonly="readonly" id="editLastModifier" style="width: 90px; background-color: #DCDCDC" name="editLastModifier" value="<%=tcase.getLastModifier()%>"></td>
                                                <td class="wob">
                                                    <% out.print(ComboProject(appContext, "editProject", "width: 90px", "editProject", "", tcase.getProject(), "", true, "", "No Project Defined."));%>
                                                </td>
                                                <td class="wob"><input id="editTicket" style="width: 90px;" name="editTicket" value="<%=tcase.getTicket() == null ? "" : tcase.getTicket()%>"></td>
                                                <td class="wob"><input id="editFunction" style="width: 390px;" list="functions" name="editFunction" value="<%=tcase.getFunction() == null ? "" : tcase.getFunction()%>"></td>
                                            </tr>
                                        </table>
                                    </td>
                                </tr>
                            </table>
                            <br>
                        </td>
                    </tr>
                    <tr>
                        <td class="separation">
                            <table style="text-align: left; border-collapse: collapse" border="0px" cellpadding="0px" cellspacing="0px">
                                <tr>
                                    <td class="wob">
                                        <table class="wob" style="text-align: left; border-collapse: collapse" border="0px" cellpadding="0px" cellspacing="0px">
                                            <tr><td class="wob"><h4 style="color : blue">TestCase Parameters</h4></td></tr>
                                            <tr id="header">
                                                <td class="wob" style="width: 150px"><%out.print(docService.findLabelHTML("application", "application", "Application"));%></td>
                                                <td class="wob" style="width: 90px"><%out.print(docService.findLabelHTML("testcase", "ActiveQA", "RunQA"));%></td>
                                                <td class="wob" style="width: 90px"><%out.print(docService.findLabelHTML("testcase", "ActiveUAT", "RunUAT"));%></td>
                                                <td class="wob" style="width: 90px"><%out.print(docService.findLabelHTML("testcase", "ActivePROD", "RunPROD"));%></td>
                                                <td class="wob" style="width: 90px"><%out.print(docService.findLabelHTML("invariant", "PRIORITY", "Priority"));%></td>
                                                <td class="wob" style="width: 150px"><%out.print(docService.findLabelHTML("invariant", "GROUP", "Group"));%></td>
                                                <td class="wob" style="width: 150px"><%out.print(docService.findLabelHTML("testcase", "status", "Status"));%></td>
                                                <% for (Invariant country : countryListInvariant) {%>
                                                <td class="wob" style="font-size : x-small ; width: 260px;"><%=country.getValue()%> <input type="hidden" name="testcase_country_all" value="<%=country.getValue()%>"></td>
                                                    <% } %>
                                            </tr>
                                            <tr>
                                                <td class="wob"><select id="editApplication" name="editApplication" style="width: 140px"><%
                                                    for (Application app : myApplicationService.findAllApplication()) {
                                                        %><option value="<%=app.getApplication()%>"<%=tcase.getApplication().compareTo(app.getApplication()) == 0 ? " SELECTED " : ""%>><%=app.getApplication()%></option>
                                                        <% }%>
                                                    </select></td>
                                                <td class="wob"><%=ComboInvariant(appContext, "editRunQA", "width: 75px", "editRunQA", "runqa", "RUNQA", tcase.getRunQA(), "", null)%></td>
                                                <td class="wob"><%=ComboInvariant(appContext, "editRunUAT", "width: 75px", "editRunUAT", "runuat", "RUNUAT", tcase.getRunUAT(), "", null)%></td>
                                                <td class="wob"><%=ComboInvariant(appContext, "editRunPROD", "width: 75px", "editRunPROD", "runprod", "RUNPROD", tcase.getRunPROD(), "", null)%></td>
                                                <td class="wob"><%=ComboInvariant(appContext, "editPriority", "width: 75px", "editPriority", "priority", "PRIORITY", String.valueOf(tcase.getPriority()), "", null)%></td>
                                                <td class="wob"><%=ComboInvariant(appContext, "editGroup", "width: 140px", "editGroup", "editgroup", "GROUP", group, "", null)%></td>
                                                <td class="wob"><%=ComboInvariant(appContext, "editStatus", "width: 140px", "editStatus", "editStatus", "TCSTATUS", status, "", null)%></td>
                                                <%  for (Invariant countryL : countryListInvariant) {%>
                                                <td class="wob" style="width:1px"><input value="<%=countryL.getValue()%>" type="checkbox" <% if (countryListTestcase.contains(countryL.getValue())) {%>  CHECKED  <% }%>
                                                                                         name="editTestCaseCountry" onclick="javascript:checkDeletePropertiesUncheckingCountry(this.value)"></td> 
                                                    <%} %>
                                            </tr>
                                        </table>
                                    </td>
                                </tr>
                            </table>
                            <%
                                String howTo = tcase.getHowTo();
                                if (howTo == null || howTo.compareTo("null") == 0) {
                                    howTo = new String(" ");
                                } else {
                                    howTo = howTo.replace(">", "&gt;");
                                }
                                String behavior = tcase.getDescription();
                                if (behavior == null || behavior.compareTo("null") == 0) {
                                    behavior = new String(" ");
                                } else {
                                    behavior = behavior.replace(">", "&gt;");
                                }
                            %> 
                            <table>
                                <tr>
                                    <td class="wob" style="text-align: left; vertical-align : top ; border-collapse: collapse">
                                        <table class="wob"  style="text-align: left; border-collapse: collapse" border="0px" cellpadding="0px" cellspacing="0px">
                                            <tr id="header">
                                                <td class="wob" style="width: 1200px"><%out.print(docService.findLabelHTML("testcase", "description", "Description"));%></td>
                                            </tr><tr>
                                                <td class="wob"><input id="editDescription" style="width: 1200px;" name="editDescription"
                                                                       value="<%=tcase.getShortDescription()%>"></td>
                                            </tr>
                                        </table>
                                    </td>
                                </tr><tr>
                                    <td class="wob" style="text-align: left; vertical-align : top ; border-collapse: collapse">
                                        <table>   
                                            <tr id="header">
                                                <td class="wob" style="width: 600px"><%out.print(docService.findLabelHTML("testcase", "BehaviorOrValueExpected", "Value Expected"));%></td>
                                                <td class="wob" style="width: 600px"><%out.print(docService.findLabelHTML("testcase", "HowTo", "HowTo"));%></td>
                                            </tr>
                                            <tr>
                                                <td class="wob" style="text-align: left; border-collapse: collapse">

                                                    <textarea id="value" rows="9" style="width: 600px;" name="BehaviorOrValueExpected" value="<%=behavior.trim()%>"
                                                              onchange="trackChanges(this.value, '<%=URLEncoder.encode(behavior, "UTF-8")%>', 'submitButtonChanges')" ><%=behavior%></textarea>
                                                    <input type="hidden" id="valueDetail" name="valueDetail" value="">
                                                </td>
                                                <td class="wob">
                                                    <textarea id="howto" rows="9" style="width: 600px;" name="HowTo" value="<%=howTo.trim()%>"
                                                              onchange="trackChanges(this.value, '<%=URLEncoder.encode(howTo, "UTF-8")%>', 'submitButtonChanges')" ><%=howTo%></textarea>
                                                    <input id="howtoDetail" name="howtoDetail" type="hidden" value="" />
                                                </td>
                                            </tr>
                                        </table><br>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                    <%  // We are getting here the last execution that was done on the testcase with its associated status.
                        String LastExeMessage;
                        LastExeMessage = "<i>Never Executed</i>";
                        if (tce != null) {
                            LastExeMessage = "Last <a width : 390px ; href=\"ExecutionDetail.jsp?id_tc=" + tce.getId() + "\">Execution</a> was ";
                            if (tce.getControlStatus().compareToIgnoreCase("OK") == 0) {
                                LastExeMessage = LastExeMessage + "<a style=\"color : green\">" + tce.getControlStatus() + "</a>";
                            } else {
                                LastExeMessage = LastExeMessage + "<a style=\"color : red\">" + tce.getControlStatus() + "</a>";
                            }
                            LastExeMessage = LastExeMessage + " in "
                                    + tce.getEnvironment() + " in "
                                    + tce.getCountry() + " on "
                                    + tce.getEnd()
                                    + "<a width : 390px ; href=\"RunTests.jsp?Test=" + test + "&TestCase=" + testcase + "&MySystem=" + appSystem
                                    + "&Country=" + tce.getCountry() + "&Environment=" + tce.getEnvironment() + "\"><i> (Run it again) </i></a>";
                        }
                        if ((tcase.getBugID() != null)
                                && (tcase.getBugID().compareToIgnoreCase("") != 0)
                                && (tcase.getBugID().compareToIgnoreCase("null") != 0)) {
                            SitdmossBugtrackingURL = SitdmossBugtrackingURL.replaceAll("%BUGID%", tcase.getBugID());
                        }
                    %>
                    <tr>
                        <td class="separation">
                            <table class="wob" style="text-align: left; border-collapse: collapse" border="0px" cellpadding="0px" cellspacing="0px">
                                <tr>
                                    <td colspan="9" class="wob">
                                        <h4 style="color : blue">Activation Criterias</h4>
                                <tr id="header">
                                    <td class="wob" style="width: 50px"><%out.print(docService.findLabelHTML("testcase", "tcactive", "Active"));%></td>
                                    <td class="wob" style="width: 90px"><%out.print(docService.findLabelHTML("testcase", "FromBuild", ""));%></td>
                                    <td class="wob" style="width: 100px"><%out.print(docService.findLabelHTML("testcase", "FromRev", ""));%></td>
                                    <td class="wob" style="width: 90px"><%out.print(docService.findLabelHTML("testcase", "ToBuild", ""));%></td>
                                    <td class="wob" style="width: 100px"><%out.print(docService.findLabelHTML("testcase", "ToRev", ""));%></td>
                                    <td class="wob" style="width: 390px"><%out.print(docService.findLabelHTML("page_testcase", "laststatus", ""));%></td>
                                    <td class="wob" style="width: 70px"><%out.print(docService.findLabelHTML("testcase", "BugID", ""));%></td>
                                    <td class="wob" style="width: 50px"><%out.print(docService.findLabelHTML("page_testcase", "BugIDLink", ""));%></td>
                                    <td class="wob" style="width: 80px"><%out.print(docService.findLabelHTML("testcase", "TargetBuild", ""));%></td>
                                    <td class="wob" style="width: 80px"><%out.print(docService.findLabelHTML("testcase", "TargetRev", ""));%></td>
                                </tr>
                                <tr>
                                    <td class="wob"><%=ComboInvariant(appContext, "editTcActive", "width: 50px", "editTcActive", "active", "TCACTIVE", tcase.getActive(), "", null)%></td>
                                    <td class="wob">
                                        <select id="editFromBuild" name="editFromBuild" class="active" style="width: 70px" >
                                            <%  String fromBuild = ParameterParserUtil.parseStringParam(tcase.getFromSprint(), "");
                                                String fromRev = ParameterParserUtil.parseStringParam(tcase.getFromRevision(), "");
                                                String toBuild = ParameterParserUtil.parseStringParam(tcase.getToSprint(), "");
                                                String toRev = ParameterParserUtil.parseStringParam(tcase.getToRevision(), "");
                                                String targetBuild = ParameterParserUtil.parseStringParam(tcase.getTargetSprint(), "");
                                                String targetRev = ParameterParserUtil.parseStringParam(tcase.getTargetRevision(), "");
                                            %>
                                            <option style="width: 100px" value="" <%=fromBuild.compareTo("") == 0 ? " SELECTED " : ""%>>----</option>
                                            <% for (BuildRevisionInvariant myBR : listBuildRev) {%>
                                            <option style="width: 100px" value="<%= myBR.getVersionName()%>" <%=fromBuild.compareTo(myBR.getVersionName()) == 0 ? " SELECTED " : ""%>><%= myBR.getVersionName()%></option>
                                            <% }%>
                                        </select>
                                    </td>
                                    <td class="wob">
                                        <select id="editFromRev" name="editFromRev" class="active" style="width: 50px" >
                                            <option style="width: 100px" value="" <%=fromRev.compareTo("") == 0 ? " SELECTED " : ""%>>----</option>
                                            <%
                                                listBuildRev = buildRevisionInvariantService.findAllBuildRevisionInvariantBySystemLevel(MySystem, 2);
                                                for (BuildRevisionInvariant myBR : listBuildRev) {
                                            %><option style="width: 100px" value="<%= myBR.getVersionName()%>" <%=fromRev.compareTo(myBR.getVersionName()) == 0 ? " SELECTED " : ""%>><%= myBR.getVersionName()%></option>
                                            <% }%>
                                        </select>
                                    </td>
                                    <td class="wob">
                                        <select id="editToBuild" name="editToBuild" class="active" style="width: 70px" >
                                            <option style="width: 100px" value="" <%=toBuild.compareTo("") == 0 ? " SELECTED " : ""%>>----</option>
                                            <%
                                                listBuildRev = buildRevisionInvariantService.findAllBuildRevisionInvariantBySystemLevel(MySystem, 1);
                                                for (BuildRevisionInvariant myBR : listBuildRev) {
                                            %><option style="width: 100px" value="<%= myBR.getVersionName()%>" <%=toBuild.compareTo(myBR.getVersionName()) == 0 ? " SELECTED " : ""%>><%= myBR.getVersionName()%></option>
                                            <% }%>
                                        </select>
                                    </td>
                                    <td class="wob">
                                        <select id="editToRev" name="editToRev" class="active" style="width: 50px" >
                                            <option style="width: 100px" value="" <%=toRev.compareTo("") == 0 ? " SELECTED " : ""%>>----</option>
                                            <%
                                                listBuildRev = buildRevisionInvariantService.findAllBuildRevisionInvariantBySystemLevel(MySystem, 2);
                                                for (BuildRevisionInvariant myBR : listBuildRev) {
                                            %><option style="width: 100px" value="<%= myBR.getVersionName()%>" <%=toRev.compareTo(myBR.getVersionName()) == 0 ? " SELECTED " : ""%>><%= myBR.getVersionName()%></option>
                                            <% }
                                            %></select>
                                    </td>
                                    <td class="wob"><%=LastExeMessage%></td>
                                    <td class="wob"><input id="BugID" style="width: 70px;" name="editBugID" value="<%=tcase.getBugID() == null ? "" : tcase.getBugID()%>"></td>
                                    <td class="wob"><% if (tcase.getBugID() != null) {%><a href="<%= SitdmossBugtrackingURL%>"><%=tcase.getBugID()%></a><%}%></td>
                                    <td class="wob">
                                        <select id="editTargetBuild" name="editTargetBuild" class="active" style="width: 70px" >
                                            <option style="width: 100px" value="" <%=targetBuild.compareTo("") == 0 ? " SELECTED " : ""%>>----</option>
                                            <%
                                                listBuildRev = buildRevisionInvariantService.findAllBuildRevisionInvariantBySystemLevel(MySystem, 1);
                                                for (BuildRevisionInvariant myBR : listBuildRev) {
                                            %><option style="width: 100px" value="<%= myBR.getVersionName()%>" <%=targetBuild.compareTo(myBR.getVersionName()) == 0 ? " SELECTED " : ""%>><%= myBR.getVersionName()%></option>
                                            <% }%>
                                        </select>
                                    </td>
                                    <td class="wob">
                                        <select id="editTargetRev" name="editTargetRev" class="active" style="width: 50px" >
                                            <option style="width: 100px" value="" <%=targetRev.compareTo("") == 0 ? " SELECTED " : ""%>>----</option>
                                            <%
                                                listBuildRev = buildRevisionInvariantService.findAllBuildRevisionInvariantBySystemLevel(MySystem, 2);
                                                for (BuildRevisionInvariant myBR : listBuildRev) {
                                            %><option style="width: 100px" value="<%= myBR.getVersionName()%>" <%=targetRev.compareTo(myBR.getVersionName()) == 0 ? " SELECTED " : ""%>><%= myBR.getVersionName()%></option>
                                            <% } %>
                                        </select>
                                    </td>
                                </tr>
                            </table>
                            <table>
                                <tr id ="header">
                                    <td class="wob" style="width: 650px"><%out.print(docService.findLabelHTML("testcase", "comment", "Comment"));%></td>
                                </tr>
                                <tr> 
                                    <td class="wob"><input id="comment" style="width: 1200px;" name="editComment" 
                                                           value="<%=tcase.getComment() == null ? "" : tcase.getComment()%>">
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                    <%  if (canEdit) {%>
                    <tr>
                        <td class="wob">
                            <table>
                                <tr>
                                    <td class="wob"><input type="submit" name="submitInformation" value="Save TestCase Info" id="submitButtonInformation" onclick="$('#howtoDetail').val($('#howto').elrte('val'));

                                            $('#valueDetail').val($('#value').elrte('val'));"></td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                    <% }%>
                    <datalist id="functions">
                    </datalist>
                </table>
                <table id="parametergeneral" class="arrond" <%if (tinf == false) {%> style="display : table" <%} else {%>style="display : none"<%}%> >
                    <tr><td id="wob" style="width: 150px"><h4 style="color : blue">General Parameters:</h4></td>
                        <td id="wob" style="width: 150px">APP: [<%=tcase.getApplication()%>]  </td>
                        <td id="wob" style="width: 160px">GROUP: [<%=tcase.getGroup()%>]  </td>
                        <td id="wob" style="width: 200px">STATUS: [<%=tcase.getStatus()%>]  </td>
                        <td id="wob" style="width: 60px">ACT: [<%=tcase.getActive()%>]  </td>
                        <td id="wob" style="width: 170px">Last Exe: [<%=LastExeMessage%>]  </td>
                        <td id="wob" style="width: 300px">Countries: [<%=countries%>]</td>
                        <td id="wob" align="right"><input id="button1" style="height:18px; width:10px" type="button" value="+" onclick="javascript:setVisible();"></td>
                    </tr>
                </table>
                <br>
                <%
                    int size = countryListTestcase.size() * 16 + 30;
                    int size2 = 570 + 80 - size;
                    int size3 = 0;
                    int size4 = size2;

                    String color = "white";
                    int i = 1;
                    int j = 1;
                    int rowNumber = 0;
                %>
                <table id="propertytable" class="arrond" style="display : table">
                    <tr>
                        <td id="wob">
                            <h3>TestCase Automation Script</h3>
                        </td>
                        <td id="wob">
                            <input id="button3" style="height:18px; width:10px" type="button" value="F" onclick="javascript:showOnlyFunctional();">
                        </td>
                    </tr>
                    <tr>
                        <td id="wob">
                            <h4>Steps</h4>
                            <table>
                                <tr>
                                    <td id="wob" style="width:10px"></td>
                                    <td id="leftlined">
                                        <%
                                            int incrementStep = 0;
                                            List<TestCaseStep> tcsList = tcsService.getListOfSteps(test, testcase);
                                            for (TestCaseStep tcs : tcsList) {
                                                incrementStep++;
                                        %>
                                        <table style="text-align: left; border-collapse: collapse">
                                            <tr>
                                                <td id="wob" style="width: 30px; text-align: center; height:20px">
                                                    <a name="stepAnchor_<%=incrementStep%>"></a>
                                                    <input type="checkbox" name="step_delete_<%=incrementStep%>" style="font-weight: bold; width:20px"
                                                           value="<%=tcs.getStep()%>">
                                                    <input type="hidden" name="step_increment" value="<%=incrementStep%>">
                                                    <input id="incrementStepNumber" value="<%=incrementStep%>" type="hidden">
                                                                            
                                                </td>
                                                <td id="wob">
                                                    &nbsp;&nbsp;Step&nbsp;&nbsp;
                                                    <input value="<%=tcs.getStep()%>" name="step_number_<%=incrementStep%>" style="font-weight: bold; width:20px">
                                                    <input type="hidden" name="initial_step_number_<%=incrementStep%>" value="<%=tcs.getStep()%>">
                                                </td>
                                                <td id="wob">
                                                    <input style="font-weight: bold; width: 500px" name="step_description_<%=incrementStep%>" value="<%=tcs.getDescription()%>">
                                                </td> 
                                                <% if (tcs.getUseStep().equals("Y")) {%>
                                                <td class="wob">
                                                    <span> Copied from : </span>
                                                </td>
                                                <td id="wob">
                                                    <input  size="100%" style="font-weight: bold; width: 200px" name="step_useStepTest_<%=incrementStep%>"
                                                            value="<%=tcs.getUseStepTest()%>">
                                                </td>
                                                <td id="wob">
                                                    <input size="100%" style="font-weight: bold; width: 50px" name="step_useStepTestCase_<%=incrementStep%>"
                                                           value="<%=tcs.getUseStepTestCase()%>">
                                                </td>
                                                <td id="wob">
                                                    <input size="100%" style="font-weight: bold; width: 50px" name="step_useStepStep_<%=incrementStep%>"
                                                           value="<%=tcs.getUseStepStep()%>">
                                                </td>
                                                <td class="wob">
                                                    <a href="TestCase.jsp?Test=<%=tcs.getUseStepTest()%>&TestCase=<%=tcs.getUseStepTestCase()%>">Edit Used Step</a>
                                                </td>
                                                <%}%>
                                            </tr>
                                        </table>
                                        <table>
                                            <tr>
                                                <td id="wob" style="width:10px"></td>
                                                <td id="leftlined"></td>
                                                <td id="wob">
                                                    <table>
                                                        <tr>
                                                            <td class="wob">
                                                                <h5>Actions</h5>
                                                                <%
                                                                    String testForQuery = "";
                                                                    String testcaseForQuery = "";
                                                                    int stepForQuery = 0;
                                                                    String isReadonly = "";
                                                                    boolean useStep = false;
                                                                    String complementName = "";

                                                                    if (!tcs.getUseStep().equals("Y")) {
                                                                        testForQuery = tcs.getTest();
                                                                        testcaseForQuery = tcs.getTestCase();
                                                                        stepForQuery = tcs.getStep();
                                                                    } else {
                                                                        testForQuery = tcs.getUseStepTest();
                                                                        testcaseForQuery = tcs.getUseStepTestCase();
                                                                        stepForQuery = tcs.getUseStepStep();
                                                                        isReadonly = "readonly";
                                                                        useStep = true;
                                                                        complementName = "Block";
                                                                    }
                                                                    List<TestCaseStepAction> tcsaList = tcsaService.getListOfAction(testForQuery, testcaseForQuery, stepForQuery);
                                                                %>
                                                                <table id="Action<%=tcs.getStep()%>" style="text-align: left; border-collapse: collapse" >
                                                                    <tr id="header">
                                                                        <td style="width: 30px"><%out.print(docService.findLabelHTML("page_testcase", "delete", "Delete"));%></td>
                                                                        <td style="width: 60px"><%out.print(docService.findLabelHTML("testcasestepaction", "sequence", "Sequence"));%></td>
                                                                        <td style="width: 136px"><%out.print(docService.findLabelHTML("testcasestepaction", "action", "Action"));%></td>
                                                                        <td class="technical_part" style="width: 350px"><%out.print(docService.findLabelHTML("testcasestepaction", "object", "Object"));%></td>
                                                                        <td class="technical_part" style="width: 210px"><%out.print(docService.findLabelHTML("testcasestepaction", "property", "Property"));%></td>
                                                                        <td class="functional_description" style="width: 296px"><%out.print(docService.findLabelHTML("testcasestepaction", "description", "Description"));%></td>
                                                                    </tr>
                                                                    <%
                                                                        String actionColor = "";
                                                                        String actionFontColor = "black";
                                                                        int incrementAction = 0;
                                                                        for (TestCaseStepAction tcsa : tcsaList) {

                                                                            incrementAction++;
                                                                            int b;
                                                                            b = incrementAction % 2;
                                                                            if (b != 1) {
                                                                                actionColor = "#f3f6fa";
                                                                            } else {
                                                                                actionColor = "White";
                                                                            }
                                                                            if (useStep) {
                                                                                actionColor = "#DCDCDC";
                                                                                actionFontColor = "grey";
                                                                            }
                                                                    %>
                                                                    <tr>
                                                                        <td style="background-color: <%=actionColor%>">
                                                                            <input  class="wob" type="checkbox" name="action_delete_<%=incrementAction%>" style="width: 30px; background-color: <%=actionColor%>"
                                                                                    value="<%=tcsa.getStep() + "-" + tcsa.getSequence()%>" <%=isReadonly%>>
                                                                            <input type="hidden" name="action_increment" value="<%=incrementAction%>" >
                                                                            <input type="hidden" name="action_step_<%=incrementAction%>" value="<%=tcsa.getStep()%>" >
                                                                        </td>
                                                                        <td style="background-color: <%=actionColor%>">
                                                                            <input class="wob" style="width: 60px; font-weight: bold; background-color: <%=actionColor%>; height:20px; color:<%=actionFontColor%>"
                                                                                   value="<%=tcsa.getSequence()%>"
                                                                                   name="action_sequence_<%=incrementAction%>" id="action_sequence_<%=incrementAction%>">
                                                                        </td>
                                                                        <td style="background-color: <%=actionColor%>"><%=ComboInvariant(appContext, "action_action_" + incrementAction, "width: 136px; background-color:" + actionColor + ";color:" + actionFontColor, "action_action_" + incrementAction, "wob", "ACTION", tcsa.getAction(), "trackChanges(0, this.selectedIndex, 'submitButtonAction')", null)%>
                                                                        </td>
                                                                        <td class="technical_part" style="background-color: <%=actionColor%>">
                                                                            <input class="wob" style="width: 350px; background-color: <%=actionColor%>; color:<%=actionFontColor%>"
                                                                                   value="<%=tcsa.getObject()%>"
                                                                                   name="action_object_<%=incrementAction%>" <%=isReadonly%>>
                                                                        </td>
                                                                        <td class="technical_part" style="background-color: <%=actionColor%>">
                                                                            <input  class="wob property_value" style="width: 210px; background-color: <%=actionColor%>; color:<%=actionFontColor%>"
                                                                                    value="<%=tcsa.getProperty()%>"
                                                                                    <%
                                                                                        if (useStep) {
                                                                                    %>
                                                                                    data-usestep-test="<%=testForQuery%>"
                                                                                    data-usestep-testcase="<%=testcaseForQuery%>"
                                                                                    data-usestep-step="<%=stepForQuery%>"
                                                                                    <%
                                                                                        }

                                                                                    %>
                                                                                    name="action_property_<%=incrementAction%>" <%=isReadonly%>>
                                                                        </td>
                                                                        <td class="functional_description" style="background-color: <%=actionColor%>">
                                                                            <input class="wob" class="functional_description" style="width: 100%; background-color: <%=actionColor%>; color:<%=actionFontColor%>"
                                                                                   value="<%=tcsa.getDescription()%>"
                                                                                   name="action_description_<%=incrementAction%>" <%=isReadonly%>
                                                                                   maxlength="1000">
                                                                        </td>
                                                                    </tr>
                                                                    <%} /*
                                                                         * End actions loop
                                                                         */%>
                                                                </table>
                                                                <%  if (canEdit) {%>
                                                                         
                                                                <table>
                                                                    <tr>
                                                                        <%if(!useStep){%>
                                                                        <td id="wob">
                                                                            <input id="incrementActionNumber" value="<%=incrementAction%>" type="hidden">
                                                                            <input type="button" value="Add Action"
                                                                                   onclick="addTestCaseActionNew('Action<%=tcs.getStep()%>', '<%=tcs.getStep()%>');
                                                                                           enableField('submitButtonAction');">
                                                                            <%=ComboInvariant(appContext, "action_action_temp" , "width: 136px; display:none", "action_action_temp", "wob", "ACTION", null, "", null)%>
                                                                        
                                                                        <td id="wob">
                                                                            <input type="button" value="import HTML Scenario" onclick="importer('ImportHTML.jsp?Test=<%=test%>&Testcase=<%=testcase%>&Step=<%=tcs.getStep()%>')">
                                                                        </td>
                                                                        <%}%>
                                                                        <td id="wob">
                                                                            <input value="Save Changes" onclick="submitTestCaseModificationNew('stepAnchor_<%=incrementAction%>');" id="submitButtonAction" name="submitChanges"
                                                                                   type="button" >
                                                                            <%=ComboInvariant(appContext, "actions_action_", "width: 150px;visibility:hidden", "actions_action_", "actions_action_", "ACTION", "", "", null)%>
                                                                        </td>
                                                                    </tr>
                                                                </table>
                                                                        <%}%>
                                                            </td>
                                                        </tr>
                                                        <% 
                                                            List<TestCaseStepActionControl> tcsacList = tcsacService.findControlByTestTestCaseStep(testForQuery, testcaseForQuery, stepForQuery);
                                                        %>
                                                        <tr>
                                                            <td class="wob">
                                                                <div id="table">
                                                                    <h5>         Controls</h5>
                                                                    <table>
                                                                        <tr>
                                                                            <td id="underlined">
                                                                                <table id="control_table<%=tcs.getStep()%>" style="text-align: left; border-collapse: collapse">
                                                                                    <tbody>
                                                                                        <tr id="header">
                                                                                            <td style="width: 30px"><%out.print(docService.findLabelHTML("page_testcase", "delete", "Delete"));%></td>
                                                                                            <td style="width: 60px"><%out.print(docService.findLabelHTML("testcasestepactioncontrol", "sequence", "Sequence"));%></td>
                                                                                            <td style="width: 60px"><%out.print(docService.findLabelHTML("testcasestepactioncontrol", "control", "Control"));%></td>
                                                                                            <td style="width: 150px"><%out.print(docService.findLabelHTML("testcasestepactioncontrol", "type", "Type"));%></td>
                                                                                            <td class="technical_part" style="width: 260px"><%out.print(docService.findLabelHTML("testcasestepactioncontrol", "ControleProperty", "Control Property"));%></td>
                                                                                            <td class="technical_part" style="width: 180px"><%out.print(docService.findLabelHTML("testcasestepactioncontrol", "ControleValue", "Control Value"));%></td>
                                                                                            <td class="technical_part" style="width: 40px"><%out.print(docService.findLabelHTML("testcasestepactioncontrol", "Fatal", "Fatal"));%></td>
                                                                                            <td class="functional_description" style="width: 296px"><%out.print(docService.findLabelHTML("testcasestepactioncontrol", "ControleDescription", "Control Description"));%></td>
                                                                                        </tr>
                                                                                        <%
                                                                                            int incrementControl = 0;
                                                                                            String controlColor = "white";
                                                                                            for (TestCaseStepActionControl tcsac : tcsacList) {
                                                                                                incrementControl++;
                                                                                                int e;
                                                                                                e = incrementControl % 2;
                                                                                                if (e != 1) {
                                                                                                    controlColor = "#f3f6fa";
                                                                                                } else {
                                                                                                    controlColor = "White";
                                                                                                }

                                                                                                if (useStep) {
                                                                                                    controlColor = "#DCDCDC";
                                                                                                }
                                                                                        %>
                                                                                        <tr>
                                                                                            <td style="text-align: center; background-color: <%=controlColor%>; color:<%=actionFontColor%>" 
                                                                                                class="controls_<%=tcsac.getStep() + '-'
                                                                                                        + tcsac.getSequence()%>">
                                                                                                <%  if (canEdit) {%>
                                                                                                <input class="wob" type="checkbox" name="controls_delete_<%=incrementControl%>" 
                                                                                                       value="<%=tcsac.getStep() + '-' + tcsac.getSequence() + '-' + tcsac.getControl()%>">
                                                                                                <% }%>
                                                                                                <input type="hidden" value="<%=incrementControl%>" name="control_increment">
                                                                                                <input type="hidden" value="<%=tcsac.getStep()%>" name="control_step_<%=incrementControl%>">
                                                                                            </td>
                                                                                            <td style="background-color: <%=controlColor%>;height:20px">
                                                                                                <input class="wob" style="width: 60px; font-weight: bold;background-color: <%=controlColor%>; color:<%=actionFontColor%>"
                                                                                                       value="<%=tcsac.getSequence()%>" name="control_sequence_<%=incrementControl%>" name="control_<%=tcsac.getSequence()%>" readonly="readonly">
                                                                                            </td>
                                                                                            <td style="background-color: <%=controlColor%>">
                                                                                                <input class="wob" style="width: 60px; font-weight: bold;background-color: <%=controlColor%>; color:<%=actionFontColor%>"
                                                                                                       value="<%=tcsac.getControl()%>" name="control_control_<%=incrementControl%>" readonly="readonly">
                                                                                            </td>
                                                                                            <td style="background-color: <%=controlColor%>"><%=ComboInvariant(appContext, "control_type_" + incrementControl, "width: 150px; background-color:" + controlColor + ";color:" + actionFontColor, "control_type_" + incrementControl, "wob", "CONTROL", tcsac.getType(), "trackChanges(this.value, '" + tcsac.getType() + "', 'submitButtonChanges')", null)%>
                                                                                            </td>
                                                                                            <td class="technical_part" style="background-color: <%=controlColor%>">
                                                                                                <input class="wob" style="width: 260px;background-color: <%=controlColor%>; color:<%=actionFontColor%>"
                                                                                                       value="<%=tcsac.getControlProperty()%>" name="control_property_<%=incrementControl%>">
                                                                                            </td>
                                                                                            <td class="technical_part" style="background-color: <%=controlColor%>">
                                                                                                <input class="wob" style="width: 180px;background-color: <%=controlColor%>; color:<%=actionFontColor%>"
                                                                                                       value="<%=tcsac.getControlValue()%>" name="control_value_<%=incrementControl%>">
                                                                                            </td>
                                                                                            <td class="technical_part" style="background-color: <%=controlColor%>"><%=ComboInvariant(appContext, "controls_fatal_" + incrementControl, "width: 40px; background-color:" + controlColor + ";color:" + actionFontColor, "controls_fatal_" + incrementControl, "wob", "CTRLFATAL", tcsac.getFatal(), "trackChanges(this.value, '" + tcsac.getFatal() + "', 'submitButtonChanges')", null)%>
                                                                                            </td>
                                                                                            <td class="functional_description_control" style="background-color: <%=controlColor%>">
                                                                                                <input class="wob" class="functional_description_control" style="width: 100%;background-color: <%=controlColor%>; color:<%=actionFontColor%>"
                                                                                                       value="<%=tcsac.getDescription()%>" name="control_description_<%=incrementControl%>" maxlength="1000">
                                                                                            </td>
                                                                                        </tr>
                                                                                    </tbody>
                                                                                    <% /**
                                                                                             * //
                                                                                             * End
                                                                                             * of
                                                                                             * control
                                                                                             * loop
                                                                                             */
                                                                                        } %>
                                                                                </table>
                                                                                <%=ComboInvariant(appContext, "controls_type_", "width: 200px;visibility:hidden", "controls_type_", "controls_type_", "CONTROL", "", "", null)%>
                                                                                <%=ComboInvariant(appContext, "controls_fatal_", "width: 40px;visibility:hidden", "controls_fatal_", "controls_fatal_", "CTRLFATAL", "", "", null)%>
                                                                                <table>
                                                                                    <tr>
                                                                                        <%  if (canEdit) {
                                                                         if(!useStep) {%>
                                                                                        <td id="wob">
                                                                                            <input type="button"
                                                                                                   value="Add Control"
                                                                                                   onclick="addTestCaseControlNew('control_table<%=tcs.getStep()%>', '<%=tcs.getStep()%>');
                                                                                                           enableField('submitButtonChanges');">
                                                                                        </td>
                                                                                        <%}%>
                                                                                        <td id="wob">
                                                                                            <input value="Save Changes" onclick="submitTestCaseModificationNew('#stepAnchor_<%=incrementAction%>');" id="submitButtonAction" name="submitChanges"
                                                                                                   type="button" >
                                                                                        </td>
                                                                                        <% }%>
                                                                                    </tr>
                                                                                </table>
                                                                                
                                                                            </td>
                                                                        </tr>
                                                                    </table>        
                                                                </div>
                                                            </td>
                                                        </tr>
                                                    </table>
                                                </td>
                                            </tr>
                                            <tr>
                                                <td class="wob" style="height:30px">
                                                </td>
                                            </tr>
                                            <% } %>
                                        </table>
                                        <%  if (canEdit) {%>
                                        <div id="hide_div"></div>
                                        <table style="width: 100%">
                                            <tr>
                                                <td id="wob">
                                                    <input type="button" value="Add Step" id="AddStepButton" style="display:inline"
                                                           onclick="addStepNew('hide_div');
                                                                   enableField('submitButtonAction');
                                                                   hidebutton('AddStepButton')">
                                                    <input type="button" value="Import Step" id="ImportStepButton" style="display:inline"
                                                           onclick="displayImportStep('importStep()')">
                                                    <input type="button" value="Use Step" id="UseStepButton" style="display:inline"
                                                           onclick="displayImportStep('useStep()')">
                                                </td>
                                            </tr>
                                            <tr>
                                                <td class="wob">
                                                    <table border="0px" id="ImportStepTable" style="display: none; width: 100%">
                                                        <tr>
                                                            <td  class="wob" style="font-weight: bold;">From :
                                                                <select id="fromTest" name="FromTest" onChange="getTestCasesForImportStep()">
                                                                    <option value="All">-- Choose Test --</option>
                                                                    <%  for (Test tst : testService.getListOfTest()) {%>
                                                                    <option class="font_weight_bold_<%=tst.getActive()%>" value="<%=tst.getTest()%>" ><%=tst.getTest()%></option>
                                                                    <% } %>
                                                                </select>
                                                            </td>
                                                        </tr>
                                                        <tr>
                                                            <td class="wob">
                                                                <table id="trImportTestCase" style="display: none; width: 100%"></table>
                                                            </td>
                                                        </tr>
                                                        <tr>
                                                            <td  class="wob" style="font-weight: bold;">To Step : <input type="text" class="wob" style="width: 60px; font-weight: bold;font-style: italic; color: #FF0000;" value="" name="import_step" id="import_step" ></td>
                                                        </tr>
                                                        <tr>
                                                            <td  class="wob" style="font-weight: bold;">Description : <input type="text" class="wob" style="width: 600px; font-weight: bold;font-style: italic; color: #FF0000;display:none" value="" name="import_description" id="import_description" ></td>
                                                        </tr>
                                                        <tr>
                                                            <td  class="wob" ><input id="importbutton" class="button" type="button" name="Import" value="Validate" onclick="importStep();"></td>
                                                        </tr>
                                                    </table>
                                                </td>
                                            </tr> 
                                        </table>
                                        <% }%>
                                    </td>
                                </tr>
                            </table>          
                            <br>
                            <br>
                        </td>
                    </tr>
                    <tr>
                        <td id="wob"><h4>Properties</h4>
                        </td>
                    </tr>
                    <tr>
                        <td id="wob">
                            <%
                                if (tccpList != null) {
                            %>
                            <table>
                                <tr>
                                    <td id="wob" style="width:10px">
                                    </td>
                                    <td id="leftlined"  style="width:10px">
                                    </td>
                                    <td id="underlined">
                                        <table id="testcaseproperties_table" style="text-align: left; border-collapse: collapse"
                                               border="0">
                                            <tr id="header">
                                                <td style="width: 30px"><%out.print(docService.findLabelHTML("page_testcase", "delete", "Delete"));%></td>
                                                <td style="width: 100px"><%out.print(docService.findLabelHTML("testcasecountryproperties", "property", "Property"));%></td>
                                                <td style="width: <%=size%>px"><%out.print(docService.findLabelHTML("invariant", "country", "Country"));%></td>
                                                <td style="width: 120px"><%out.print(docService.findLabelHTML("testcasecountryproperties", "type", "Type"));%></td>
                                                <td style="width: 40px"><%out.print(docService.findLabelHTML("testcasecountryproperties", "database", "Database"));%></td>
                                                <td style="width: <%=size2%>px"><%out.print(docService.findLabelHTML("testcasecountryproperties", "value", "Value"));%>
                                                <td style="width: 40px"><%out.print(docService.findLabelHTML("testcasecountryproperties", "length", "Length"));%></td>
                                                <td style="width: 40px"><%out.print(docService.findLabelHTML("testcasecountryproperties", "rowlimit", "RowLimit"));%></td>
                                                <td style="width: 80px"><%out.print(docService.findLabelHTML("testcasecountryproperties", "nature", "Nature"));%></td>
                                            </tr>
                                            <div id="cache_properties">
                                                <%//ComboInvariant(appContext, "properties_dtb_type_ID", "display: none;", "properties_dtb_type_ID", "wob", "PROPERTYDATABASE", tccpList.get(0).getDatabase(), "", null)%>
                                            </div><%

                                            int incrementProperty = 0;
                                                for (TestCaseCountryProperties tccp : tccpList) {
                                                    incrementProperty++;
                                                    List<String> countryOfProperty = tccpService.findCountryByProperty(tccp);

                                                    rowNumber = rowNumber + 1;
                                                    proplist = proplist + "" + tccp.getProperty() + "  /  ";

                                                    if (tccp.getType().equals("executeSqlFromLib")) {
                                                        SqlLibrary sqllib = libService.findSqlLibraryByKey(tccp.getValue1().replaceAll("'", "''"));
                                                    }

                                                    size3 = 0;
                                                    size4 = size2;
                                                    String styleValue2 = "none";
                                                    if (tccp.getType().equals("getAttributeFromHtml")
                                                            || tccp.getType().equals("getFromXml")
                                                            || tccp.getType().equals("getFromCookie")
                                                            || tccp.getType().equals("getDifferencesFromXml")) {
                                                        size3 = 1 * size2 / 3;
                                                        size4 = (2 * size2 / 3) - 5;
                                                        styleValue2 = "inline";
                                                    }

                                                    int nbline = tccp.getValue1().split("\n").length;
                                                    String valueID = rowNumber + "-" + tccp.getProperty();
                                                    
                                                    String showEntireValueB1 = "showEntireValueB1" + valueID;
                                                    String showEntireValueB2 = "showEntireValueB2" + valueID;
                                                    String sqlDetails = "sqlDetails" + valueID;
                                                    String sqlDetailsB1 = "sqlDetailsB1" + valueID;
                                                    String sqlDetailsB2 = "sqlDetailsB2" + valueID;
                                                    String properties_dtbID = "properties_dtb" + valueID;
                                                    i++;

                                                    j = i % 2;
                                                    if (j == 1) {
                                                        color = "#f3f6fa";
                                                    } else {
                                                        color = "White";
                                                    }
                                            %>
                                            <tr style="background-color : <%=color%>">
                                                <td>
                                                    <%  if (canEdit) {%>
                                                    <input name="properties_delete_<%=incrementProperty%>" type="checkbox" style="width: 30px" value="">
                                                    <%}%>
                                                    <input type="hidden" name="property_increment" value="<%=incrementProperty%>">
                                                </td>
                                                <td>
                                                    <input class="wob properties_id_<%=rowNumber%> property_name" style="width: 100px; font-weight: bold; background-color : <%=color%>"
                                                           name="properties_property_<%=incrementProperty%>" value="<%=tccp.getProperty()%>">
                                                </td>
                                                <td style="font-size : x-small ; width: <%=size%>px;">
                                                    <table>
                                                        <tr>
                                                            <%  for (String c : countryListTestcase) {%>
                                                            <td class="wob"><%=c%>
                                                            </td> 
                                                            <% 	} %>
                                                        </tr>
                                                        <tr>
                                                            <%
                                                                for (String c : countryListTestcase) {
                                                            %>
                                                            <td class="wob">
                                                                <input value="<%=c%>" type="checkbox" <% if (countryOfProperty.contains(c)) {%>  CHECKED  <% }%>
                                                                       class="properties_id_<%=rowNumber%>" name="properties_country_<%=incrementProperty%>">
                                                            </td>
                                                            <%  }%>
                                                        </tr>
                                                    </table>
                                                </td>
                                                <td><%=ComboInvariant(appContext, "properties_type_"+incrementProperty, "width: 120px; background-color:" + color, "properties_type_"+incrementProperty, "wob","PROPERTYTYPE" , tccp.getType(), "activateDatabaseBox(this.value, '" + properties_dtbID + "' ,'" + properties_dtbID + "' );activateValue2(this.value, 'tdValue2_" + rowNumber + "', '" + valueID + "','" + valueID + "_2','" + size2 + "')", null)%>
                                                </td>
                                                <td>
                                                    <%
                                                        if (tccp.getType().equals("executeSqlFromLib") || tccp.getType().equals("executeSql") || tccp.getType().equals("executeSoapFromLib")) {
                                                    %>
                                                    <%=ComboInvariant(appContext, "properties_dtb_"+incrementProperty, "width: 40px; display: inline ; background-color:" + color, "properties_dtb_"+incrementProperty, "wob", "PROPERTYDATABASE", tccp.getDatabase(), "", null)%>
                                                    <%
                                                    } else {%>
                                                    <select name="properties_dtb_<%=incrementProperty%>" style="width: 40px; display: inline ; background-color:<%=color%>" class="wob" id="properties_dtb_<%=incrementProperty%>">
                                                        <option value="">---</option>
                                                    </select>
                                                    <% }%>
                                                </td>
                                                <td>
                                                    <table>
                                                        <tr>
                                                            <td class="wob" rowspan="2">
                                                                <textarea id="properties_value1_<%=incrementProperty%>" rows="2" class="wob" style="width: <%=size4%>px; background-color : <%=color%>; " 
                                                                          name="properties_value1_<%=incrementProperty%>" value="<%=tccp.getValue1()%>"><%=tccp.getValue1()%></textarea>
                                                            </td>
                                                            <td class="wob" rowspan="2" style="display:<%=styleValue2%>">
                                                                <textarea id="properties_value2_<%=incrementProperty%>" rows="2" class="wob" style="width: <%=size3%>px; background-color : <%=color%>;"
                                                                          name="properties_value2_<%=incrementProperty%>" value="<%=tccp.getValue2()%>"><%=tccp.getValue2()%></textarea>
                                                            </td>
                                                            <%
                                                                if (tccp.getType().equals("executeSqlFromLib")
                                                                        || tccp.getType().equals("executeSql")) {
                                                            %>
                                                            <td class="wob">
                                                                <input style="display:inline; height:20px; width:20px; background-color: <%=color%>; color:blue; font-weight:bolder" title="Open SQL Library" class="smallbutton" type="button" value="L" name="opensql-library"  onclick="openSqlLibraryPopin('<%=valueID%>')">
                                                            </td>
                                                            <% }%>
                                                            <%
                                                                if (tccp.getType().equals("executeSqlFromLib")
                                                                        || tccp.getType().equals("executeSql")
                                                                        || tccp.getType().equals("getFromTestData")
                                                                        || tccp.getType().equals("executeSoapFromLib")) {
                                                            %>
                                                            <td class="wob">
                                                                <input style="display:inline; height:20px; width:20px; background-color: <%=color%>; color:green; font-weight:bolder" title="View property" class="smallbutton" type="button" value="V" name="openview-library"  onclick="openViewPropertyPopin('<%=valueID%>', '<%=test%>', '<%=testcase%>')">
                                                            </td>
                                                            <%}%>
                                                        </tr>
                                                        <tr>
                                                            <% if (nbline > 3) {%>
                                                            <td class="wob" style="background-color: <%=color%>; text-align: center; border-left-color:white">
                                                                <input style="display:inline; height:20px; width:20px; background-color: <%=color%>; color: green; font-weight:bolder" class="smallbutton" title="Show the Full Sql" type="button" value="+" id="<%=showEntireValueB1%>" onclick="showEntireValue('<%=valueID%>', '<%=nbline%>', '<%=showEntireValueB1%>', '<%=showEntireValueB2%>');">
                                                                <input style="display:none; height:20px; width:20px; background-color: <%=color%>; color: red; font-weight:bolder" class="smallbutton" title="Hide Details" type="button" value="-" id="<%=showEntireValueB2%>" onclick="showLessValue('<%=valueID%>', '<%=showEntireValueB1%>', '<%=showEntireValueB2%>');">
                                                            </td>
                                                            <%} else {%>
                                                            <td class="wob" style="background-color: <%=color%>; text-align: center; border-left-color:white">
                                                                <% if (tccp.getType().equals("executeSqlFromLib")) {%>
                                                                <input style="display:inline; height:20px; width:20px; background-color: <%=color%>; color: orange; font-weight:bolder" class="smallbutton" type="button" value="e" title="Show the SQL" id="<%=sqlDetailsB1%>" onclick="showSqlDetails('<%=sqlDetails%>', '<%=sqlDetailsB1%>', '<%=sqlDetailsB2%>');">
                                                                <input style="display:none; height:20px; width:20px; background-color: <%=color%>; color: orange; font-weight:bolder" class="smallbutton" type="button" value="-" title="Hide the SQL" id="<%=sqlDetailsB2%>" onclick="hideSqlDetails('<%=sqlDetails%>', '<%=sqlDetailsB1%>', '<%=sqlDetailsB2%>');">
                                                                <% } %>
                                                            </td>
                                                            <% }%>
                                                        </tr>
                                                    </table>
                                                </td>
                                                <td>
                                                    <input class="wob" style="width: 40px; background-color : <%=color%>" name="properties_length_<%=incrementProperty%>"
                                                           value="<%=tccp.getLength()%>" onchange="trackChanges(this.value, '<%=tccp.getLength()%>', 'SavePropertyChanges')">
                                                </td>
                                                <td>
                                                    <input class="wob" style="width: 40px; background-color : <%=color%>" name="properties_rowlimit_<%=incrementProperty%>"
                                                           value="<%=tccp.getRowLimit()%>" onchange="trackChanges(this.value, '<%=tccp.getRowLimit()%>', 'SavePropertyChanges')">
                                                </td>
                                                <td><%=ComboInvariant(appContext, "properties_nature_"+incrementProperty, "width: 80px; background-color:" + color, "properties_nature_"+incrementProperty, "wob", "PROPERTYNATURE", tccp.getNature(), "trackChanges(0, this.selectedIndex, 'submitButtonChanges')", null)%></td>
                                            </tr>
                                            <%}%>
                                        </table>
                                        <br>
                                        <%  if (canEdit) {%>
                                        <input type="button" value="Add Property" id="AddProperty"
                                               onclick="addTestCasePropertiesNew('testcaseproperties_table', <%=rowNumber%>, <%=size%>, <%=size2%>);
                                                       enableField('SavePropertyChanges');
                                                       disableField('AddProperty');">
                                        <input type="submit" value="Save Changes" id="SavePropertyChanges">              
                                        <input type="hidden" id="Test" name="Test" value="<%=test%>">
                                        <input type="hidden" id="TestCase" name="TestCase" value="<%=testcase%>">
                                        <input type="hidden" name="testcase_hidden" value="<%=test + " - " + testcase%>">
                                        <input type="hidden" id="CountryList" name="CountryList" value="<%=countries%>">
                                        <%=ComboInvariant(appContext, "new_properties_type_new_properties_value", "width: 70px;visibility:hidden", "new_properties_type_new_properties_value", "new_properties_type_new_properties_value", "PROPERTYTYPE", "", "", null)%>
                                        <%=ComboInvariant(appContext, "properties_dtb_", "width: 40px;visibility:hidden", "properties_dtb_", "properties_dtb_", "PROPERTYDATABASE", "", "", null)%>
                                        <%=ComboInvariant(appContext, "properties_nature_", "width: 80px;visibility:hidden", "properties_nature_", "properties_nature_", "PROPERTYNATURE", "", "", null)%>
                                        <input type="hidden" name="testcase_hidden" value="<%=test + " - " + testcase%>">
                                        <input type="hidden" name="testcase_country_hidden" value="<%=countries%>">
                                        <% }%>
                                    </td>
                                </tr>
                            </table>
                            <p id ="toto" style="font-size : x-small ; width: <%=size%>px; visibility:hidden">
                                <% for (String c : countryListTestcase) {%>
                                <%=c%> 
                                <% } %>
                                <br>
                                <% for (String c : countryListTestcase) {
                                %>
                                <input value="<%=rowNumber%> - <%=c%>" type="checkbox" id="properties_country" 
                                       name="properties_country" >
                                <% } %>
                            </p>
                            <%
                            } else {
                            %>
                            <table id="nocountrydefined" class="arrond">
                                <tr>
                                    <td class="wob"></td>
                                </tr>
                                <tr>
                                    <td class="wob">
                                        <h3> To add Properties,Actions and controls, select at least one country in the general parameters </h3>
                                    </td>
                                </tr>
                                <tr>
                                    <td class="wob"></td>
                                </tr>
                            </table>
                            <%   } %>
                        </td>
                    </tr>
                </table>
            </form>
            <br>
            <table id="arrond" style="text-align: left" border="1" >
                <tr>
                    <td colspan="3">
                        <h4>Contextual Actions</h4>
                    </td>
                </tr>
                <tr>
                    <% if (tcase.getGroup().equalsIgnoreCase("AUTOMATED")) {%>
                    <td>
                        <a href="RunTests.jsp?Test=<%=test%>&TestCase=<%=testcase%>&MySystem=<%=appSystem%>">Run this Test Case.</a>
                    </td>
                    <%        } else if (tcase.getGroup().equalsIgnoreCase("MANUAL")) {%>
                    <td>
                        <a href="RunManualTestCase.jsp?Test=<%=test%>&TestCase=<%=testcase%>&MySystem=<%=appSystem%>">Run this Test Case.</a>
                    </td>
                    <%        }%>    
                    <td>
                        <a href="ExecutionDetailList.jsp?test=<%=test%>&testcase=<%=testcase%>&MySystem=<%=appSystem%>">See Last Executions..</a>
                    </td>
                    <%if (request.getUserPrincipal()
                                != null && request.isUserInRole("TestAdmin")) {
                    %>
                    <td>
                        <a href="LogViewer.jsp?Test=<%=test%>&TestCase=<%=testcase%>">See Log Viewer...</a>
                    </td>
                    <% }%>
                </tr>
            </table>
            <script>
                $("input.property_value").each(function() {
                    //var jinput = $(this);
                    if (this.value && this.value !== "" && isNaN(this.value) && $("input.property_name[value='" + this.value + "']").length === 0) {
                        this.style.width = '192px';
                        $(this).before("<img class='property_ko' data-property-name='" + this.value + "' src='./images/ko.png' title='Property Missing' style='display:inline;' width='16px' height='16px' />");
                    }
                });

                $("img.property_ko").on("click", function(event) {
                    var propertyName = $(event.target).data("property-name");
                    var property = $("input.property_value[value='" + propertyName + "']");

                    if (property.data("usestep-step") != null
                            && property.data("usestep-step") != "") {
                        var useTest = property.data("usestep-test");
                        var useTestcase = property.data("usestep-testcase");
                        $.get("./ImportPropertyOfATestCaseToAnOtherTestCase", {"fromtest": useTest, "fromtestcase": useTestcase,
                            "totest": "<%=test%>", "totestcase": "<%=testcase%>",
                            "property": propertyName}
                        , function(data) {
                            $("#UpdateTestCaseDetail").submit();
                        }
                        );
                    } else {
                        $.get("./CreateNotDefinedProperty", {"totest": "<%=test%>", "totestcase": "<%=testcase%>",
                            "property": propertyName}
                        , function(data) {
                            $("#UpdateTestCaseDetail").submit();
                        });
                    }
                });
            </script>
            <%
                    }
                } catch (Exception e) {
                    out.println("<br> error message : " + e.getMessage() + " " + e.toString() + "<br>");
                }
            %>
        </div>
        <% if (booleanFunction) {%>
        <script type="text/javascript">
            $(document).ready(function() {
                $.getJSON($('#urlForListOffunction').val(), function(data) {
                    for (var i = 0; i < data.length; i++) {
                        $("#functions").append($("<option></option>")
                                .attr("value", data[i].value));
                    }
                });
            });
        </script>
        <%}%>
        <script>
            function checkDeletePropertiesUncheckingCountry(country) {
                for (var a = 0; a < document.getElementsByName('properties_delete').length; a++) {
                    if (document.getElementsByName('properties_delete')[a].value.contains(country)) {
                        alert("BEWARE : Unchecking this country will automatically delete the associated properties saving the testcase");
                    }
                }
                ;
            }
            ;
        </script>
        <div id="popin"></div>
        <br><% out.print(display_footer(DatePageStart));%>
    </body>
</html>
