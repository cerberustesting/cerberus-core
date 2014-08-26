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
            }

            $().ready(function() {
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
                IDocumentationService docService = appContext.getBean(IDocumentationService.class);
                boolean booleanFunction = false;

                try {
                    /*
                     * Filter requests
                     */
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

                    booleanFunction = StringUtil.parseBoolean(parameterService.findParameterByKey("cerberus_testcase_function_booleanListOfFunction", "").getValue());
                    String listOfFunction = "";
                    if (booleanFunction) {
                        Parameter functions = parameterService.findParameterByKey("cerberus_testcase_function_urlForListOfFunction", "");
                        listOfFunction = functions.getValue();
                    }
                    String SitdmossBugtrackingURL;
                    SitdmossBugtrackingURL = "";
                    String appSystem = "";

                    /*
                     * Filter requests
                     */
                    String proplist = "";

                    String MySystem = request.getAttribute("MySystem").toString();
                    if (request.getParameter("system") != null && request.getParameter("system").compareTo("") != 0) {
                        MySystem = request.getParameter("system");
                    }

                    String group;
                    if (request.getParameter("Group") != null
                            && request.getParameter("Group").compareTo("All") != 0) {
                        group = request.getParameter("Group");
                    } else {
                        group = new String("%%");
                    }

                    String status;
                    if (request.getParameter("Status") != null
                            && request.getParameter("Status").compareTo("All") != 0) {
                        status = request.getParameter("Status");
                    } else {
                        status = new String("%%");
                    }

                    String test;
                    if (request.getParameter("Test") != null
                            && request.getParameter("Test").compareTo("All") != 0) {
                        test = request.getParameter("Test");
                    } else {
                        test = new String("%%");
                    }

                    String testselected;
                    if (request.getParameter("createTest") != null
                            && request.getParameter("createTest").compareTo("All") != 0) {
                        testselected = request.getParameter("createTest");
                    } else {
                        testselected = new String("%%");

                    }
                    String testcase;
                    if (request.getParameter("TestCase") != null
                            && request.getParameter("TestCase").compareTo("All") != 0) {
                        testcase = request.getParameter("TestCase");
                    } else {
                        testcase = new String("%%");
                    }

                    Boolean tinf;
                    if (request.getParameter("Tinf") != null
                            && request.getParameter("Tinf").compareTo("Y") == 0) {
                        tinf = true;
                    } else {
                        tinf = false;
                    }

                    Boolean searchTc;
                    if (request.getParameter("SearchTc") != null
                            && request.getParameter("SearchTc").compareTo("Y") == 0) {
                        searchTc = true;
                    } else {
                        searchTc = false;
                    }

                    Boolean search;
                    if (request.getParameter("Search") != null
                            && request.getParameter("Search").compareTo("Y") == 0) {
                        search = true;
                    } else {
                        search = false;
                    }

                    Boolean load;
                    if (request.getParameter("Load") != null
                            && request.getParameter("Load").compareTo("Load") == 0) {
                        load = true;
                    } else {
                        load = false;
                    }

                    if (!load
                            && request.getParameter("submitDuplicate") != null
                            && request.getParameter("submitDuplicate").compareTo(
                                    "Duplicate") == 0) {
                        load = true;
                    }

                    //Raise alert if Flash Message fed
                    if (request.getAttribute("flashMessage") != null
                            && !request.getAttribute("flashMessage").equals("")) {
                        String message = (String) request.getAttribute("flashMessage");
                        request.removeAttribute("flashMessage");
            %>
            <script>alertMessage('<%=message%>');</script>
            <% }%>
            <input id="urlForListOffunction" value="<%=listOfFunction%>" style="display:none">
            <form action="TestCase.jsp" method="post" name="selectTestCase" id="selectTestCase">
                <div>
                    <div><%out.print(docService.findLabelHTML("test", "test", "Test"));%></div>
                    <div><select id="filtertest" name="Test" style="width: 200px" OnChange="document.selectTestCase.submit()">
                            <%
                                String optstyle = "";
                                if (test.compareTo("%%") == 0) {
                            %><option style="width: 200px" value="All">-- Choose Test --</option>
                            <%                                                            }
                                List<Test> tests = testService.getListOfTest();
                                for (Test tst : tests) {
                                    if (tst.getActive().equals("Y")) {
                                        optstyle = "font-weight:bold;";
                                    } else {
                                        optstyle = "font-weight:lighter;";
                                    }
                            %><option style="width: 200px;<%=optstyle%>" value="<%=tst.getTest()%>" <%=test.compareTo(tst.getTest()) == 0 ? " SELECTED " : ""%>><%=tst.getTest()%></option>
                            <%
                                }
                            %>
                        </select>
                    </div>
                    <div><%out.print(docService.findLabelHTML("testcase", "testcase", "TestCase"));%></div>
                    <div>
                        <select id="filtertestcase" name="TestCase" style="width: 850px" OnChange="document.selectTestCase.submit()">
                            <%
                                if (test.compareTo("%%") == 0) {
                            %><option style="width: 200px" value="All">-- Choose Test First --</option>
                            <%                                                        } else {
                                List<TCase> tcList = testCaseService.findTestCaseByTest(test);
                                for (TCase tc : tcList) {
                                    if (tc.getActive().equalsIgnoreCase("Y")) {
                                        optstyle = "font-weight:bold;";
                                    } else {
                                        optstyle = "font-weight:lighter;";
                                    }
                            %><option style="width: 500px;<%=optstyle%>" value="<%=tc.getTestCase()%>" <%=testcase.compareTo(tc.getTestCase()) == 0 ? " SELECTED " : ""%>><%=tc.getTestCase()%>  [<%=tc.getApplication()%>]  : <%=tc.getShortDescription()%></option>
                            <%
                                    }
                                }
                            %>
                        </select>
                    </div>
                    <div><input id="loadbutton" class="button" type="submit" name="Load" value="Load"></div>
                </div>    
            </form> 
            <br>
            <%
                TCase tcase = testCaseService.findTestCaseByKey(test, testcase);

                group = tcase.getGroup();
                status = tcase.getStatus();
                //TODO        //String dateCrea = tcase.getDateCreation()!=null?tcase.getDateCreation():"-- unknown --";

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
                 * We can edit the testcase only if User role is TestAdmin or if
                 * role is Test and testcase is not WORKING
                 */
                boolean canEdit = false;
                if (request.getUserPrincipal() != null
                        && (request.isUserInRole("TestAdmin")) || ((request.isUserInRole("Test")) && !(status.equalsIgnoreCase("WORKING")))) {
                    canEdit = true;
                }

                boolean canDuplicate = false;
                if (request.getUserPrincipal() != null && request.isUserInRole("Test")) {
                    canDuplicate = true;
                }

                boolean canDelete = false;
                if (request.getUserPrincipal() != null && request.isUserInRole("TestAdmin")) {
                    canDelete = true;
                }

            %>

            <table id="generalparameter" class="arrond"
                   <%if (tinf == false) {%> style="display : none" <%} else {%>style="display : table"<%}%> >
                <tr>
                    <td class="separation">
                        <%  if (canDuplicate) {%>
                        <form method="post" name="DuplicateTestCase" action="DuplicateTestCase">
                            <% }%>
                            <table  class="wob" style="text-align: left; border-collapse: collapse" border="0px" cellpadding="0px" cellspacing="0px">
                                <tr><td colspan="2" class="wob"><h4 style="color : blue">Test Information</h4></td>
                                    <td id="wob"></td><td id="wob"></td>
                                    <td id="wob" align="right"><input id="button2" style="height:18px; width:10px" type="button" value="-" onclick="javascript:setInvisible();"></td>
                                </tr>    
                                <tr id="header"> 
                                    <td class="wob" style="width: 100px"><%out.print(docService.findLabelHTML("test", "test", "Test"));%></td>
                                    <td class="wob" style="width: 100px"><%out.print(docService.findLabelHTML("testcase", "testcase", "TestCase"));%></td>
                                    <td class="wob" style="width: 960px"><%out.print(docService.findLabelHTML("test", "description", "Description"));%></td>
                                </tr>
                                <tr>

                                    <td class="wob"><input id="editTest" style="width: 90px; font-weight: bold;" name="editTest" id="DuplicateTest"
                                                           value="<%=tcase.getTest()%>" onchange="checkFieldDuplicate();"></td>
                                    <td class="wob"><input id="editTestCase" style="width: 90px; font-weight: bold;" name="editTestCase" id="DuplicateTestCase"
                                                           value="<%=tcase.getTestCase()%>"
                                                           onchange="checkFieldDuplicate('<%=tcase.getTest()%>', '<%=tcase.getTestCase()%>');">
                                    </td>
                                    <td class="wob"><input id="editDescription" style="width: 950px; background-color: #DCDCDC" name="editDescription" readonly="readonly"
                                                           value="<%=tcase.getShortDescription()%>"></td>
                                    <td class="wob">
                                        <%  if (canDuplicate) {%>
                                        <input rowspan="2" style=" valign: center" class="_Duplicate" type="submit" name="submitDuplicate"
                                               value="Duplicate" id="submitButtonDuplicate" disabled="disabled">
                                        <% }%>
                                    </td>
                                </tr>
                            </table><br>
                            <%  if (canDuplicate) {%>
                            <input type="hidden" id="Test" name="Test" value="<%=test%>"> <input type="hidden" id="TestCase" name="TestCase"
                                                                                                 value="<%=testcase%>">

                        </form>
                        <% }%>
                        <%  if (canDelete) {%>
                        <input type="button" id="deleteTC" name="deleteTC" value="delete" onclick="javascript:deleteTestCase('<%=test%>', '<%=testcase%>', 'TestCase.jsp')">
                        <input type="button" id="exportTC" name="exportTC" value="exportTestCase" onclick="javascript:exportTestCase('<%=test%>', '<%=testcase%>', 'TestCase.jsp')">
                        <div id="deleteTCDiv"></div>
                        <% }%>
                    </td>
                </tr>

                <%  if (canEdit) {%>

                <form method="post" name="UpdateTestCase" action="UpdateTestCase">
                    <% }%> 

                    <tr>
                        <td class="separation">
                            <table style="text-align: left; border-collapse: collapse" border="0px" cellpadding="0px" cellspacing="0px">
                                <tr><td class="wob">
                                        <table class="wob" style="text-align: left; border-collapse: collapse" border="0px" cellpadding="0px" cellspacing="0px">
                                            <tr><td class="wob" colspan="2"><h4 style="color : blue">TestCase Information</h4></td></tr>
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
                                                <td class="wob"><input readonly="readonly" id="origine" style="width: 90px; background-color: #DCDCDC" name="editOrigine" value="<%=tcase.getOrigin()%>"></td>
                                                <td class="wob"><input readonly="readonly" id="reforigine" style="width: 90px;  background-color: #DCDCDC" name="editRefOrigine" value="<%=tcase.getRefOrigin()%>"></td>
                                                <td class="wob"><%//dateCrea%></td>
                                                <td class="wob"><input readonly="readonly" id="creator" style="width: 90px; background-color: #DCDCDC" name="editCreator" value="<%=tcase.getCreator()%>"></td>
                                                <td class="wob"><input id="implementer" style="width: 90px;" name="editImplementer" value="<%=tcase.getImplementer() == null ? "" : tcase.getImplementer()%>"></td>
                                                <td class="wob"><input readonly="readonly" id="lastModifier" style="width: 90px; background-color: #DCDCDC" name="editLastModifier" value="<%=tcase.getLastModifier()%>"></td>
                                                <td class="wob">
                                                    <% out.print(ComboProject("editProject", "width: 90px", "project", "", tcase.getProject(), "", true, "", "No Project Defined."));%>
                                                </td>
                                                <td class="wob"><input id="ticket" style="width: 90px;" name="editTicket" value="<%=tcase.getTicket() == null ? "" : tcase.getTicket()%>"></td>
                                                <td class="wob"><input id="function" style="width: 390px;" list="functions" name="function" value="<%=tcase.getFunction() == null ? "" : tcase.getFunction()%>"></td>
                                            </tr>
                                        </table>
                                    </td>
                                </tr>
                            </table><br>
                        </td>
                    </tr>
                    <%
                        List<Invariant> countryListInvariant = invariantService.findListOfInvariantById("COUNTRY");
                        List<String> countryListTestcase = testCaseCountryService.findListOfCountryByTestTestCase(test, testcase);
                    %>
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
                                                <%
                                                    for (Invariant country : countryListInvariant) {%>
                                                <td class="wob" style="font-size : x-small ; width: 260px;"><%=country.getValue()%> <input type="hidden" name="testcase_country_all" value="<%=country.getValue()%>"></td>
                                                    <% } %>
                                            </tr>
                                            <tr>
                                                <td class="wob"><select id="application" name="editApplication" style="width: 140px"><%
                                                    for (Application app : myApplicationService.findAllApplication()) {
                                                        %><option value="<%=app.getApplication()%>"<%=tcase.getApplication().compareTo(app.getApplication()) == 0 ? " SELECTED " : ""%>><%=app.getApplication()%></option>
                                                        <% }%>
                                                    </select></td>
                                                <td class="wob"><%=ComboInvariant("editRunQA", "width: 75px", "editRunQA", "runqa", "RUNQA", tcase.getRunQA(), "", null)%></td>
                                                <td class="wob"><%=ComboInvariant("editRunUAT", "width: 75px", "editRunUAT", "runuat", "RUNUAT", tcase.getRunUAT(), "", null)%></td>
                                                <td class="wob"><%=ComboInvariant("editRunPROD", "width: 75px", "editRunPROD", "runprod", "RUNPROD", tcase.getRunPROD(), "", null)%></td>
                                                <td class="wob"><%=ComboInvariant("editPriority", "width: 75px", "editPriority", "priority", "PRIORITY", String.valueOf(tcase.getPriority()), "", null)%></td>
                                                <td class="wob"><%=ComboInvariant("editGroup", "width: 140px", "editGroup", "editgroup", "GROUP", group, "", null)%></td>
                                                <td class="wob"><%=ComboInvariant("editStatus", "width: 140px", "editStatus", "editStatus", "TCSTATUS", status, "", null)%></td>
                                                <%
                                                    for (Invariant countryL : countryListInvariant) {
                                                %>
                                                <td class="wob" style="width:1px"><input value="<%=countryL.getValue()%>" type="checkbox" <% if (countryListTestcase.contains(countryL.getValue())) {%>  CHECKED  <% }%>
                                                                                         name="testcase_country_general" onclick="javascript:checkDeletePropertiesUncheckingCountry(this.value)"></td> 
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
                                                <td class="wob"><input id="desc" style="width: 1200px;" name="editDescription"
                                                                       value="<%=tcase.getDescription()%>"></td>
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
                    <%

                        // We are getting here the last execution that was done on the testcase with its associated status.
                        String LastExeMessage;
                        TestCaseExecution tce = testCaseExecutionService.findLastTestCaseExecutionNotPE(test, testcase);
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
                                <tr><td colspan="9" class="wob"><h4 style="color : blue">Activation Criterias</h4>
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
                                    <td class="wob"><%=ComboInvariant("editTcActive", "width: 50px", "editTcActive", "active", "TCACTIVE", tcase.getActive(), "", null)%></td>
                                    <td class="wob">
                                        <select id="editFromBuild" name="editFromBuild" class="active" style="width: 70px" >
                                            <% String fromBuild = "";
                                                if (tcase.getFromSprint() != null) {
                                                    fromBuild = tcase.getFromSprint();
                                                }
                                                String fromRev = "";
                                                if (tcase.getFromRevision() != null) {
                                                    fromRev = tcase.getFromRevision();
                                                }
                                                String toBuild = "";
                                                if (tcase.getToSprint() != null) {
                                                    toBuild = tcase.getToSprint();
                                                }
                                                String toRev = "";
                                                if (tcase.getToRevision() != null) {
                                                    toRev = tcase.getToRevision();
                                                }
                                                String targetBuild = "";
                                                if (tcase.getTargetSprint() != null) {
                                                    targetBuild = tcase.getTargetSprint();
                                                }
                                                String targetRev = "";
                                                if (tcase.getTargetRevision() != null) {
                                                    targetRev = tcase.getTargetRevision();
                                                }
                                            %>
                                            <option style="width: 100px" value="" <%=fromBuild.compareTo("") == 0 ? " SELECTED " : ""%>>----</option>
                                            <%
                                                List<BuildRevisionInvariant> listBuildRev = buildRevisionInvariantService.findAllBuildRevisionInvariantBySystemLevel(MySystem, 1);
                                                for (BuildRevisionInvariant myBR : listBuildRev) {
                                            %><option style="width: 100px" value="<%= myBR.getVersionName()%>" <%=fromBuild.compareTo(myBR.getVersionName()) == 0 ? " SELECTED " : ""%>><%= myBR.getVersionName()%></option>
                                            <% }
                                            %></select>
                                    </td>
                                    <td class="wob">
                                        <select id="editFromRev" name="editFromRev" class="active" style="width: 50px" >
                                            <option style="width: 100px" value="" <%=fromRev.compareTo("") == 0 ? " SELECTED " : ""%>>----</option>
                                            <%
                                                listBuildRev = buildRevisionInvariantService.findAllBuildRevisionInvariantBySystemLevel(MySystem, 2);
                                                for (BuildRevisionInvariant myBR : listBuildRev) {
                                            %><option style="width: 100px" value="<%= myBR.getVersionName()%>" <%=fromRev.compareTo(myBR.getVersionName()) == 0 ? " SELECTED " : ""%>><%= myBR.getVersionName()%></option>
                                            <% }
                                            %></select>
                                    </td>
                                    <td class="wob">
                                        <select id="editToBuild" name="editToBuild" class="active" style="width: 70px" >
                                            <option style="width: 100px" value="" <%=toBuild.compareTo("") == 0 ? " SELECTED " : ""%>>----</option>
                                            <%
                                                listBuildRev = buildRevisionInvariantService.findAllBuildRevisionInvariantBySystemLevel(MySystem, 1);
                                                for (BuildRevisionInvariant myBR : listBuildRev) {
                                            %><option style="width: 100px" value="<%= myBR.getVersionName()%>" <%=toBuild.compareTo(myBR.getVersionName()) == 0 ? " SELECTED " : ""%>><%= myBR.getVersionName()%></option>
                                            <% }
                                            %></select>
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
                                            <% }
                                            %></select>
                                    </td>
                                    <td class="wob">
                                        <select id="editTargetRev" name="editTargetRev" class="active" style="width: 50px" >
                                            <option style="width: 100px" value="" <%=targetRev.compareTo("") == 0 ? " SELECTED " : ""%>>----</option>
                                            <%
                                                listBuildRev = buildRevisionInvariantService.findAllBuildRevisionInvariantBySystemLevel(MySystem, 2);
                                                for (BuildRevisionInvariant myBR : listBuildRev) {
                                            %><option style="width: 100px" value="<%= myBR.getVersionName()%>" <%=targetRev.compareTo(myBR.getVersionName()) == 0 ? " SELECTED " : ""%>><%= myBR.getVersionName()%></option>
                                            <% }
                                            %></select>
                                    </td>

                                </tr>
                            </table>
                            <table>
                                <tr id ="header">
                                    <td class="wob" style="width: 650px"><%out.print(docService.findLabelHTML("testcase", "comment", "Comment"));%></td>
                                </tr><tr> 
                                    <td class="wob"><input id="comment" style="width: 1200px;" name="editComment" 
                                                           value="<%=tcase.getComment() == null ? "" : tcase.getComment()%>"></td></tr>

                            </table>
                        </td>
                    </tr>
                    <%  if (canEdit) {%>
                    <tr>
                        <td class="wob">


                            <input type="hidden" id="Test" name="Test" value="<%=test%>"> 
                            <input type="hidden" id="TestCase" name="TestCase" value="<%=testcase%>">
                            <table>
                                <tr>
                                    <td class="wob"><input type="submit" name="submitInformation" value="Save TestCase Info" id="submitButtonInformation" onclick="$('#howtoDetail').val($('#howto').elrte('val'));
                                            $('#valueDetail').val($('#value').elrte('val'));"></td>
                                </tr>
                            </table>

                        </td>
                    </tr>
                    <datalist id="functions">
                    </datalist>
                </form>

                <% }%>

            </table>
            <table id="parametergeneral" class="arrond" <%if (tinf == false) {%> style="display : table" <%} else {%>style="display : none"<%}%> >
                <tr><td id="wob" style="width: 150px"><h4 style="color : blue">General Parameters:</h4></td>
                    <td id="wob" style="width: 150px">APP: [<%=tcase.getApplication()%>]  </td>
                    <td id="wob" style="width: 160px">GROUP: [<%=tcase.getGroup()%>]  </td>
                    <td id="wob" style="width: 200px">STATUS: [<%=tcase.getStatus()%>]  </td>
                    <td id="wob" style="width: 60px">ACT: [<%=tcase.getActive()%>]  </td>
                    <td id="wob" style="width: 170px">Last Exe: [<%=LastExeMessage%>]  </td>
                    <td id="wob" style="width: 300px">Countries: [<%//countries%>]</td>
                    <td id="wob" align="right"><input id="button1" style="height:18px; width:10px" type="button" value="+" onclick="javascript:setVisible();"></td>
                </tr>
            </table>
            <div id="table">
                <%  if (canEdit) {%>
                <form id="select_country_properties" action="TestCase.jsp"
                      method="post">
                    <% }%>
                    
                    
                    <%  
                        int size = countryListTestcase.size()* 16 + 30;
                        int size2 = 570 + 80 - size;
                        int size3 = 0;
                        int size4 = size2;

                        // Define the list of country available for this test
                        String tc_countries = "";
                        for (String c : countryListTestcase) {
                            tc_countries += c + "-";
                        }
                    %>
                    <br>
                    <input type="hidden" id="tc_countries_for_js_add"
                           value="<%=tc_countries%>">
                    <input type="hidden" name="testcase_for_country_hidden"
                           value="<%=tcase.getTest() + " - " + tcase.getTestCase()%>">
                </form>
                <%  if (canEdit) {%>
                <form method="post" name="UpdateTestCaseDetail" id="UpdateTestCaseDetail" onsubmit="return checkForm();" action="UpdateTestCaseDetail">
                    <% }%>                                 
                    <%
                        String color = "white";
                        int i = 1;
                        int j = 1;
                        int rowNumber = 0;
                    %>
                    <input type="hidden" name="testcase_hidden"
                           value="<%=tcase.getTest() + " - " + tcase.getTestCase()%>">
                    <table id="propertytable" class="arrond" style="display : table">
                        <tr>
                            <td id="wob"><h3>TestCase Automation Script</h3></td>
                            <td id="wob"><input id="button3" style="height:18px; width:10px" type="button" value="F" onclick="javascript:showOnlyFunctional();"></td>
                        </tr>
                        <tr><td id="wob">
                                <h4>Steps</h4>
                                <table><tr><td id="wob" style="width:10px"></td><td id="leftlined">

                                            <div id="table1">

                                                <%
                                                    int i1 = 0;
                                                    List<TestCaseStep> tcsList = tcsService.getListOfSteps(test, testcase);
                                                    for (TestCaseStep tcs : tcsList) {
                                                        i1++;
                                                %>
                                                <table style="text-align: left; border-collapse: collapse">
                                                    <tr>
                                                        <td id="wob" style="width: 30px; text-align: center; height:20px">
                                                            <a name="stepAnchor_<%=i1%>"></a>
                                                            <%  if (canEdit) {%>
                                                            <input type="checkbox" name="testcasestep_delete" style="font-weight: bold; width:20px"
                                                                   value="<%=tcs.getStep()%>">
                                                            <% }%>
                                                        </td>
                                                        <td id="wob">
                                                            <%--Step--%>
                                                            &nbsp;&nbsp;Step <%=tcs.getStep()%>&nbsp;&nbsp; <input
                                                                type="hidden" name="testcasestep_hidden" style="font-weight: bold; width:20px"
                                                                value="<%=tcs.getStep()%>"></td><td id="wob">
                                                            <input
                                                                size="100%" style="font-weight: bold; width: 500px" name="step_description"
                                                                value="<%=tcs.getDescription()%>">
                                                        </td> <% if (tcs.getUseStep().equals("Y")) {
                                                        %>
                                                        <td class="wob"><span> Copied from : </span></td>
                                                        <td id="wob">
                                                            <input
                                                                size="100%" style="font-weight: bold; width: 200px" name="useStepTest"
                                                                value="<%=tcs.getUseStepTest()%>">
                                                        </td><td id="wob">
                                                            <input
                                                                size="100%" style="font-weight: bold; width: 50px" name="useStepTestCase"
                                                                value="<%=tcs.getUseStepTestCase()%>">
                                                        </td><td id="wob">
                                                            <input
                                                                size="100%" style="font-weight: bold; width: 50px" name="useStepStep"
                                                                value="<%=tcs.getUseStepStep()%>">
                                                        </td>
                                                        <td class="wob">
                                                            <a href="TestCase.jsp?Test=<%=tcs.getUseStepTest()%>&TestCase=<%=tcs.getUseStepTestCase()%>">Edit Used Step</a>
                                                        </td>
                                                        <%}%>
                                                    </tr>
                                                </table>
                                                <table><tr><td id="wob" style="width:10px"></td><td id="leftlined"></td>
                                                        <td id="wob"><table><tr><td class="wob">
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
                                                                                int a = 1;
                                                                                String actionColor = "";
                                                                                String actionFontColor = "black";
                                                                                for (TestCaseStepAction tcsa : tcsaList) {

                                                                                    a++;
                                                                                    int b;
                                                                                    b = a % 2;
                                                                                    if (b == 1) {
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
                                                                                    <%  if (canEdit) {%>
                                                                                    <input  class="wob" type="checkbox" name="actions_delete<%=complementName%>" style="width: 30px; background-color: <%=actionColor%>"
                                                                                            value="<%=tcsa.getStep() + "-" + tcsa.getSequence()%>"
                                                                                            onchange="trackChanges(this.defaultChecked, this.checked, 'submitButtonAction')"
                                                                                            <%=isReadonly%>>
                                                                                    <% }%>
                                                                                    <input type="hidden" name="stepnumber_hidden<%=complementName%>" value="<%=tcsa.getStep()%>" >
                                                                                </td>
                                                                                <td style="background-color: <%=actionColor%>"><input class="wob" style="width: 60px; font-weight: bold; background-color: <%=actionColor%>; height:20px; color:<%=actionFontColor%>"
                                                                                                                                      value="<%=tcsa.getSequence()%>"
                                                                                                                                      name="actions_sequence<%=complementName%>" readonly="readonly">
                                                                                </td>
                                                                                <td style="background-color: <%=actionColor%>"><%=ComboInvariant("actions_action" + complementName, "width: 136px; background-color:" + actionColor + ";color:" + actionFontColor, "actions_action", "wob", "ACTION", tcsa.getAction(), "trackChanges(0, this.selectedIndex, 'submitButtonAction')", null)%></td>
                                                                                <td class="technical_part" style="background-color: <%=actionColor%>"><input class="wob" style="width: 350px; background-color: <%=actionColor%>; color:<%=actionFontColor%>"
                                                                                                                                                             value="<%=tcsa.getObject()%>"
                                                                                                                                                             name="actions_object<%=complementName%>" <%=isReadonly%>
                                                                                                                                                             onchange="trackChanges(this.value, '<%=tcsa.getObject()%>', 'submitButtonAction')">
                                                                                </td>
                                                                                <td class="technical_part" style="background-color: <%=actionColor%>"><input  class="wob property_value" style="width: 210px; background-color: <%=actionColor%>; color:<%=actionFontColor%>"
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
                                                                                                                                                              name="actions_property<%=complementName%>" <%=isReadonly%>
                                                                                                                                                              onchange="trackChanges(this.value, '<%=tcsa.getProperty()%>', 'submitButtonAction')">
                                                                                </td>
                                                                                <td class="functional_description" style="background-color: <%=actionColor%>"><input class="wob" class="functional_description" style="width: 100%; background-color: <%=actionColor%>; color:<%=actionFontColor%>"
                                                                                                                                                                     value="<%=tcsa.getDescription()%>"
                                                                                                                                                                     name="actions_description<%=complementName%>" <%=isReadonly%>
                                                                                                                                                                     maxlength="1000"
                                                                                                                                                                     onchange="trackChanges(this.value, '<%=tcsa.getDescription()%>', 'submitButtonAction')">
                                                                                </td>
                                                                            </tr>
                                                                            <%

                                                                                } /*
                                                                                 * End actions loop
                                                                                 */


                                                                            %>
                                                                        </table>
                                                                        <%  if (canEdit && !useStep) {%>
                                                                        <table><tr><td id="wob"><input type="button" value="Add Action"
                                                                                                       onclick="addTestCaseAction('Action<%=tcs.getStep()%>', '<%=tcs.getStep()%>');
                                                                                                               enableField('submitButtonAction');">
                                                                                <td id="wob"><input type="button" value="import HTML Scenario" onclick="importer('ImportHTML.jsp?Test=<%=test%>&Testcase=<%=testcase%>&Step=<%=tcs.getStep()%>')"></td>
                                                                            </td><td id="wob"><input value="Save Changes" onclick="submitTestCaseModification('stepAnchor_<%=i1%>');" id="submitButtonAction" name="submitChanges"
                                                                                                     type="button" >
                                                                            <%=ComboInvariant("actions_action_", "width: 150px;visibility:hidden", "actions_action_", "actions_action_", "ACTION", "", "", null)%></td></tr></table></td></tr>
                                                                            <% }%>



                                                        <%
                                                            List<TestCaseStepActionControl> tcsacList = tcsacService.findControlByTestTestCaseStep(testForQuery, testcaseForQuery, stepForQuery);

                                                        %>
                                                        <tr><td class="wob"><div id="table">
                                                                    <h5>         Controls</h5>
                                                                    <table><tr><td id="underlined">
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
                                                                                            int d = 1;
                                                                                            String controlColor = "white";
                                                                                            for (TestCaseStepActionControl tcsac : tcsacList) {
                                                                                                d++;
                                                                                                int e;
                                                                                                e = d % 2;
                                                                                                if (e == 1) {
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
                                                                                                <input class="wob" type="checkbox" name="controls_delete<%=complementName%>" 
                                                                                                       value="<%=tcsac.getStep() + '-'
                                                                                                               + tcsac.getSequence() + '-'
                                                                                                               + tcsac.getControl()%>"
                                                                                                       onchange="trackChanges(this.defaultChecked, this.checked, 'submitButtonChanges')" />
                                                                                                <% }%>
                                                                                                <input type="hidden" value="<%=tcsac.getStep()%>" name="controls_step<%=complementName%>" readonly="readonly">
                                                                                            </td>
                                                                                            <td style="background-color: <%=controlColor%>;height:20px"><input class="wob" style="width: 60px; font-weight: bold;background-color: <%=controlColor%>; color:<%=actionFontColor%>"
                                                                                                                                                               value="<%=tcsac.getSequence()%>"
                                                                                                                                                               name="controls_sequence<%=complementName%>" readonly="readonly"></td>
                                                                                            <td style="background-color: <%=controlColor%>"><input class="wob" style="width: 60px; font-weight: bold;background-color: <%=controlColor%>; color:<%=actionFontColor%>"
                                                                                                                                                   value="<%=tcsac.getControl()%>"
                                                                                                                                                   name="controls_control<%=complementName%>" readonly="readonly"></td>
                                                                                            <td style="background-color: <%=controlColor%>"><%=ComboInvariant("controls_type" + complementName, "width: 150px; background-color:" + controlColor + ";color:" + actionFontColor, "controls_type", "wob", "CONTROL", tcsac.getType(), "trackChanges(this.value, '" + tcsac.getType() + "', 'submitButtonChanges')", null)%></td>
                                                                                            <td class="technical_part" style="background-color: <%=controlColor%>"><input class="wob" style="width: 260px;background-color: <%=controlColor%>; color:<%=actionFontColor%>"
                                                                                                                                                                          value="<%=tcsac.getControlProperty()%>"
                                                                                                                                                                          name="controls_controlproperty<%=complementName%>"
                                                                                                                                                                          onchange="trackChanges(this.value, '<%=tcsac.getControlProperty()%>', 'submitButtonChanges')"></td>
                                                                                            <td class="technical_part" style="background-color: <%=controlColor%>"><input class="wob" style="width: 180px;background-color: <%=controlColor%>; color:<%=actionFontColor%>"
                                                                                                                                                                          value="<%=tcsac.getControlValue()%>"
                                                                                                                                                                          name="controls_controlvalue<%=complementName%>"
                                                                                                                                                                          onchange="trackChanges(this.value, '<%=tcsac.getDescription()%>', 'submitButtonChanges')"></td>
                                                                                            <td class="technical_part" style="background-color: <%=controlColor%>"><%=ComboInvariant("controls_fatal" + complementName, "width: 40px; background-color:" + controlColor + ";color:" + actionFontColor, "controls_fatal", "wob", "CTRLFATAL", tcsac.getFatal(), "trackChanges(this.value, '" + tcsac.getFatal() + "', 'submitButtonChanges')", null)%></td>
                                                                                            <td class="functional_description_control" style="background-color: <%=controlColor%>"><input class="wob" class="functional_description_control" style="width: 100%;background-color: <%=controlColor%>; color:<%=actionFontColor%>"
                                                                                                                                                                                          value="<%=tcsac.getDescription()%>"
                                                                                                                                                                                          name="controls_controldescription<%=complementName%>"
                                                                                                                                                                                          maxlength="1000"
                                                                                                                                                                                          onchange="trackChanges(this.value, '<%=tcsac.getDescription()%>', 'submitButtonChanges')"></td>
                                                                                        </tr>

                                                                                    </tbody>
                                                                                    <%

                                                                                        }


                                                                                    %>
                                                                                </table>
                                                                                <%  if (canEdit && !useStep) {%>
                                                                                <%=ComboInvariant("controls_type_", "width: 200px;visibility:hidden", "controls_type_", "controls_type_", "CONTROL", "", "", null)%>
                                                                                <%=ComboInvariant("controls_fatal_", "width: 40px;visibility:hidden", "controls_fatal_", "controls_fatal_", "CTRLFATAL", "", "", null)%>
                                                                                <table><tr><td id="wob"><input type="button"
                                                                                                               value="Add Control"
                                                                                                               onclick="addTestCaseControl('control_table<%=tcs.getStep()%>',<%=tcs.getStep()%>);
                                                                                                                       enableField('submitButtonChanges');">
                                                                                        </td><td id="wob"><input value="Save Changes" onclick="submitTestCaseModification('#stepAnchor_<%=i1%>');" id="submitButtonAction" name="submitChanges"
                                                                                                                 type="button" ></td></tr></table>
                                                                                            <% }%>
                                                                            </td></tr></table>        
                                                                </div></td></tr></table><tr><td class="wob" style="height:30px"></td></tr></td></tr>


                                            <%
                                                }
                                                /*
                                                 * End Step loop
                                                 */

                                            %>
                                    </div>
                                </td></tr></table>
                                <%  if (canEdit) {%>
                        <div id="hide_div"></div>
                        <table style="width: 100%"><tr><td id="wob"><input type="button" value="Add Step" id="AddStepButton" style="display:inline"
                                                                           onclick="addStep('hide_div');
                                                                                   enableField('submitButtonAction');
                                                                                   hidebutton('AddStepButton')">

                                    <input type="button" value="Import Step" id="ImportStepButton" style="display:inline"
                                           onclick="displayImportStep('importStep()')">
                                    <input type="button" value="Use Step" id="UseStepButton" style="display:inline"
                                           onclick="displayImportStep('useStep()')">


                                </td></tr>
                            <tr><td class="wob">
                                    <table border="0px" id="ImportStepTable" style="display: none; width: 100%">
                                        <tr>
                                            <td  class="wob" style="font-weight: bold;">From :
                                                <select id="fromTest" name="FromTest" onChange="getTestCasesForImportStep()">
                                                    <%
                                                    %><option value="All">-- Choose Test --</option><%
                                                        for (Test tst : testService.getListOfTest()) {
                                                            if (tst.getActive().equalsIgnoreCase("Y")) {
                                                                optstyle = "font-weight:bold;";
                                                            } else {
                                                                optstyle = "font-weight:lighter;";
                                                            }
                                                    %><option style="<%=optstyle%>" value="<%=tst.getTest()%>" ><%=tst.getTest()%></option><%
                                                        }
                                                    %>
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
                            </tr> </table>
                            <% }%>
                    </td></tr>
            </table>          
            <br><br>


            <input type="hidden" id="Test" name="Test" value="<%=test%>">
            <input type="hidden" id="TestCase" name="TestCase"
                   value="<%=testcase%>">

            <input type="hidden" name="testcase_hidden"
                   value="<%=test + " - " + testcase%>">
            <table id="tableproperty" class="arrond" style="display : none" >
                <tr>
                    <td><h4 style="color : blue">TestCase Automation Script:</h4></td>
                    <td>Properties = <%=proplist%></td>
                    <td td align="right"><input id="button4" style="height:18px; width:10px" type="button" value="+" onclick="javascript:setVisibleP();"></td>
                </tr>
            </table>
            </tr>
            <tr>
                <td id="wob"><h4>Properties</h4></td>
            </tr>
            <tr>
                <td id="wob"><table><tr><td id="wob" style="width:10px"></td><td id="leftlined"  style="width:10px"></td><td id="underlined">
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
                                    <%
                                        List<TestCaseCountryProperties> tccpList = tccpService.findListOfPropertyPerTestTestCase(test, testcase);
                                            if (tccpList != null) {
                                    %><div id="cache_properties">
                                        <%=ComboInvariant("properties_dtb_type_ID", "display: none;", "properties_dtb_type_ID", "wob", "PROPERTYDATABASE", tccpList.get(1).getDatabase(), "", null)%>
                                    </div><%

                                        for (TestCaseCountryProperties tccp : tccpList) {

                                            List<String> type_toselect = new ArrayList<String>();
                                            type_toselect.add(tccp.getType().toUpperCase());

                                            List<String> nature_toselect = new ArrayList<String>();
                                            nature_toselect.add(tccp.getNature().toUpperCase());

                                            rowNumber = rowNumber + 1;
                                            proplist = proplist + "" + tccp.getProperty() + "  /  ";

                                            String sqlDesc = "";
                                            if (tccp.getType().equals("executeSqlFromLib")) {
                                                SqlLibrary sqllib = libService.findSqlLibraryByKey(tccp.getValue1().replaceAll("'", "''"));
                                            sqlDesc = sqllib.getScript().replace("'", "\'");
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
                                            String typeID = "type" + valueID;

                                            String showEntireValueB1 = "showEntireValueB1" + valueID;
                                            String showEntireValueB2 = "showEntireValueB2" + valueID;
                                            String sqlDetails = "sqlDetails" + valueID;
                                            String sqlDetailsB1 = "sqlDetailsB1" + valueID;
                                            String sqlDetailsB2 = "sqlDetailsB2" + valueID;
                                            String properties_dtbID = "properties_dtb" + valueID;
                                            String showSqlDetail = "";
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
                                            <input name="properties_delete" type="checkbox" style="width: 30px"
                                                   value="<%//delete_value%>"
                                                   onchange="trackChanges(this.defaultChecked, this.checked, 'SavePropertyChanges')">
                                            <%}%>
                                            <input type="hidden" name="property_hidden" value="<%//rowNumber%>">
                                            <input type="hidden" name="old_property_hidden" value="">
                                            </td>
                                        <td><input class="wob" style="width: 100px; font-weight: bold; background-color : <%=color%>"
                                                   name="properties_property"
                                                   value="<%=tccp.getProperty()%>"
                                                   onchange="trackChanges(this.value, '<%=tccp.getProperty()%>', 'SavePropertyChanges')"></td>
                                        <td style="font-size : x-small ; width: <%=size%>px;"><table><tr>
                                                    <%  for (String c : countryListTestcase) {%>
                                                    <td class="wob"><%=c%></td> 
                                                    <% 		} 
                                                    %></tr><tr><%
                                                        for (String c : countryListTestcase) {
                                                    %>
                                                    <td class="wob"><input value="<%=rowNumber%> - <%=c%>" type="checkbox" <% if (tccp.getCountry().equals(c)) {%>  CHECKED  <% }%>
                                                                           class="properties_id_<%=rowNumber%>"
                                                                           name="properties_country" onchange="trackChanges(this.value, '<%=c%>', 'SavePropertyChanges')"></td>
                                                        <% //onclick="return false"
                                                            } 
                                                        %>
                                                </tr></table></td>
                                        <td><%=ComboInvariant("properties_type", "width: 120px; background-color:" + color, typeID, "wob", "PROPERTYTYPE", tccp.getType(), "activateDatabaseBox(this.value, '" + properties_dtbID + "' ,'" + properties_dtbID + "' );activateValue2(this.value, 'tdValue2_" + rowNumber + "', '" + valueID + "','" + valueID + "_2','" + size2 + "')", null)%></td>
                                        <td>
                                            <%
                                                if (tccp.getType().equals("executeSqlFromLib") || tccp.getType().equals("executeSql") || tccp.getType().equals("executeSoapFromLib")) {
                                            %>
                                            <%=ComboInvariant("properties_dtb", "width: 40px; display: inline ; background-color:" + color, properties_dtbID, "wob", "PROPERTYDATABASE", tccp.getDatabase(), "", null)%>
                                            <%
                                            } else {
                                            %><select name="properties_dtb" style="width: 40px; display: inline ; background-color:<%=color%>" class="wob" id="<%=properties_dtbID%>">
                                                <option value="---">---</option>
                                            </select><%
                                                }
                                            %>
                                        </td>

                                        <td><table><tr><td class="wob" rowspan="2"><textarea id="<%=valueID%>" rows="2" class="wob" style="width: <%=size4%>px; background-color : <%=color%>; " name="properties_value"
                                                                                             value="<%=tccp.getValue1()%>"><%=tccp.getValue1()%></textarea>
                                                        <% if (tccp.getType().equals("executeSqlFromLib")) {%>
                                                        <textarea id="<%=valueID%>" rows="5" class="wob" style="display:none ; width: <%=size4%>px; background-color : <%=color%>; color:grey" 
                                                                  readonly="readonly" value="<%=sqlDesc%>"><%=sqlDesc%></textarea>
                                                        <%}%>
                                                    </td>
                                                    <td class="wob" id="tdValue2_<%=rowNumber%>" rowspan="2" style="display:<%=styleValue2%>"><textarea id="<%=valueID%>_2" rows="2" class="wob" style="width: <%=size3%>px; background-color : <%=color%>;" name="properties_value2"
                                                                                                                                                        value="<%=tccp.getValue2()%>"><%=tccp.getValue2()%></textarea>

                                                    </td>
                                                    <%
                                                        if (tccp.getType().equals("executeSqlFromLib")
                                                                || tccp.getType().equals("executeSql")) {
                                                    %>
                                                    <td class="wob"><input style="display:inline; height:20px; width:20px; background-color: <%=color%>; color:blue; font-weight:bolder" title="Open SQL Library" class="smallbutton" type="button" value="L" name="opensql-library"  onclick="openSqlLibraryPopin('<%=valueID%>')"></td>
                                                        <% }%>
                                                        <%
                                                            if (tccp.getType().equals("executeSqlFromLib")
                                                                    || tccp.getType().equals("executeSql")
                                                                    || tccp.getType().equals("getFromTestData")
                                                                    || tccp.getType().equals("executeSoapFromLib")) {
                                                        %>
                                                    <td class="wob"><input style="display:inline; height:20px; width:20px; background-color: <%=color%>; color:green; font-weight:bolder" title="View property" class="smallbutton" type="button" value="V" name="openview-library"  onclick="openViewPropertyPopin('<%=valueID%>', '<%=test%>', '<%=testcase%>')"></td>
                                                        <%}%>

                                                </tr><tr>
                                                    <% if (nbline > 3) {%>
                                                    <td class="wob" style="background-color: <%=color%>; text-align: center; border-left-color:white">
                                                        <input style="display:inline; height:20px; width:20px; background-color: <%=color%>; color: green; font-weight:bolder" class="smallbutton" title="Show the Full Sql" type="button" value="+" id="<%=showEntireValueB1%>" onclick="showEntireValue('<%=valueID%>', '<%=nbline%>', '<%=showEntireValueB1%>', '<%=showEntireValueB2%>');">
                                                        <input style="display:none; height:20px; width:20px; background-color: <%=color%>; color: red; font-weight:bolder" class="smallbutton" title="Hide Details" type="button" value="-" id="<%=showEntireValueB2%>" onclick="showLessValue('<%=valueID%>', '<%=showEntireValueB1%>', '<%=showEntireValueB2%>');">
                                                    </td><%} else {%>
                                                    <td class="wob" style="background-color: <%=color%>; text-align: center; border-left-color:white">

                                                        <% if (tccp.getType().equals("executeSqlFromLib")) {%>
                                                        <input style="display:inline; height:20px; width:20px; background-color: <%=color%>; color: orange; font-weight:bolder" class="smallbutton" type="button" value="e" title="Show the SQL" id="<%=sqlDetailsB1%>" onclick="showSqlDetails('<%=sqlDetails%>', '<%=sqlDetailsB1%>', '<%=sqlDetailsB2%>');">
                                                        <input style="display:none; height:20px; width:20px; background-color: <%=color%>; color: orange; font-weight:bolder" class="smallbutton" type="button" value="-" title="Hide the SQL" id="<%=sqlDetailsB2%>" onclick="hideSqlDetails('<%=sqlDetails%>', '<%=sqlDetailsB1%>', '<%=sqlDetailsB2%>');">
                                                    </td><%}%><%}%>
                                                </tr></table></td>
                                        <td><input class="wob" style="width: 40px; background-color : <%=color%>" name="properties_length"
                                                   value="<%=tccp.getLength()%>"
                                                   onchange="trackChanges(this.value, '<%=tccp.getLength()%>', 'SavePropertyChanges')">
                                        </td>
                                        <td><input class="wob" style="width: 40px; background-color : <%=color%>" name="properties_rowlimit"
                                                   value="<%=tccp.getRowLimit()%>"
                                                   onchange="trackChanges(this.value, '<%=tccp.getRowLimit()%>', 'SavePropertyChanges')">
                                        </td>
                                        <td><%=ComboInvariant("properties_nature", "width: 80px; background-color:" + color, "properties_nature", "wob", "PROPERTYNATURE", tccp.getNature(), "trackChanges(0, this.selectedIndex, 'submitButtonChanges')", null)%></td>
                                    </tr>
                                    <%}%>
                                </table><br>
                                <%  if (canEdit) {%>
                                <input type="button" value="Add Property" id="AddProperty"
                                       onclick="addTestCaseProperties('testcaseproperties_table', <%=rowNumber%>, <%=size%>, <%=size2%>);
                                               enableField('SavePropertyChanges');
                                               disableField('AddProperty');">
                                <input type="submit" value="Save Changes" 
                                       id="SavePropertyChanges">              
                                <input type="hidden" id="Test" name="Test" value="<%=test%>">
                                <input type="hidden" id="TestCase" name="TestCase"
                                       value="<%=testcase%>">
                                <input type="hidden" name="testcase_hidden"
                                       value="<%=test+ " - "+ testcase%>">
                                <input type="hidden" id="CountryList" name="CountryList" value="<%//countries%>">
                                <%=ComboInvariant("new_properties_type_new_properties_value", "width: 70px;visibility:hidden", "new_properties_type_new_properties_value", "new_properties_type_new_properties_value", "PROPERTYTYPE", "", "", null)%>
                                <%=ComboInvariant("properties_dtb_", "width: 40px;visibility:hidden", "properties_dtb_", "properties_dtb_", "PROPERTYDATABASE", "", "", null)%>
                                <%=ComboInvariant("properties_nature_", "width: 80px;visibility:hidden", "properties_nature_", "properties_nature_", "PROPERTYNATURE", "", "", null)%>
                                <input type="hidden" name="testcase_hidden"
                                       value="<%=test+ " - "+testcase%>">
                                <input type="hidden" name="testcase_country_hidden"
                                       value="<%//countries%>">
                                <% }%></td></table>
                    <p id ="toto" style="font-size : x-small ; width: <%=size%>px;visibility:hidden">
                        <%     for (String c : countryListTestcase) {%>
                        <%=c%> 
                        <%
                            } %><br><%
                                
                                for (String c : countryListTestcase) {
                        %>
                        <input value="<%=rowNumber%> - <%=c%>" type="checkbox" id="properties_country" 
                               name="properties_country" >
                        <% //onclick="return false"
                            }
                            
                        %>
                    </p>
                </td></tr>
            </table>
        </form>
        <br>

        <table id="arrond" style="text-align: left" border="1" >
            <tr><td colspan="3"><h4>Contextual Actions</h4></td></tr>
            <tr>

                <% if (tcase.getGroup().equalsIgnoreCase("AUTOMATED")) {%>
                <td><a href="RunTests.jsp?Test=<%=test%>&TestCase=<%=testcase%>&MySystem=<%=appSystem%>">Run this Test Case.</a></td>
                <%        }

                    else if (tcase.getGroup().equalsIgnoreCase("MANUAL")) {%>
                <td><a href="RunManualTestCase.jsp?Test=<%=test%>&TestCase=<%=testcase%>&MySystem=<%=appSystem%>">Run this Test Case.</a></td>
                <%        }%>    
                <td>
                    <a href="ExecutionDetailList.jsp?test=<%=test%>&testcase=<%=testcase%>&MySystem=<%=appSystem%>">See Last Executions..</a>
                </td>
                <%if (request.getUserPrincipal () 
                        != null && request.isUserInRole("TestAdmin")) {
                %>
                <td>
                    <a href="LogViewer.jsp?Test=<%=test%>&TestCase=<%=testcase%>">See Log Viewer...</a>
                </td>
                <% } %>
            </tr>
        </table>
        <%
        } else {
        %> <br><table id="nocountrydefined" class="arrond">
            <tr><td class="wob"></td></tr><tr>
                <td class="wob"><h3> To add Properties,Actions and controls, select at least one country in the general parameters </h3></td>
            </tr><tr><td class="wob"></td></tr>
        </table>
            <%   }  %>
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

            }catch(Exception e) {
                out.println("<br> error message : " + e.getMessage() + " "
                        + e.toString() + "<br>");
            } finally {
                try {
                    
                } catch (Exception ex) {
                }
            }
        %>

    </div>
</div>
<% if (booleanFunction

    
    

    ) {%>
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
<%}%>
<div id="popin"></div>
<br><% out.print (display_footer
(DatePageStart));%>
</body>
</html>
