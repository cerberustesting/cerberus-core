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
<%@page import="java.net.URLDecoder"%>
<%@page import="org.apache.commons.lang3.StringUtils"%>
<%@ page import="org.cerberus.service.IDocumentationService" %>
<%@ page import="org.cerberus.service.ITestService" %>
<%@ page import="org.cerberus.service.ITestCaseService" %>
<%@ page import="org.cerberus.service.IApplicationService" %>
<%@ page import="org.cerberus.entity.Application" %>
<%@ page import="org.cerberus.entity.Test" %>
<% Date DatePageStart = new Date();%>

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta content="text/html; charset=UTF-8" http-equiv="content-type">
        <title>TestCase Creation</title>
        <script type="text/javascript" src="js/jquery-1.9.1.min.js"></script>
        <script type="text/javascript" src="js/jquery-migrate-1.2.1.min.js"></script>
        <script type="text/javascript" src="js/jquery-ui-1.10.2.js"></script>
        <script type="text/javascript" src="js/elrte.min.js"></script>
        <script type="text/javascript" src="js/i18n/elrte.en.js"></script>
        <script type="text/javascript" src="js/elfinder.min.js"></script>
        <link rel="stylesheet" type="text/css" href="css/jquery-ui.css">
        <link rel="stylesheet" type="text/css" href="css/elrte.min.css">
        <link rel="stylesheet" type="text/css" href="css/crb_style.css">
        <link rel="stylesheet" type="text/css" href="css/elfinder.min.css">
        <link rel="stylesheet" type="text/css" href="css/theme.css">
        <link rel="shortcut icon" type="image/x-icon" href="images/favicon.ico" />
        <script type="text/javascript">
            $().ready(function() {
                elRTE.prototype.options.toolbars.cerberus = ['style', 'alignment', 'colors', 'format', 'indent', 'lists', 'links'];
                var opts = {
                    lang: 'en',
                    styleWithCSS: false,
                    width: 615,
                    height: 200,
                    toolbar: 'cerberus',
                    allowSource: false,
                    cssfiles: ['css/crb_style.css']
                };

                $('#createHowTo').elrte(opts);
                $('#createBehaviorOrValueExpected').elrte(opts);
            });
        </script>
    </head>
    <body>
        <%@ include file="include/function.jsp" %>
        <%@ include file="include/header.jsp" %>
        <div id="body">
            <%
                IApplicationService applicationService = appContext.getBean(IApplicationService.class);
                IDocumentationService docService = appContext.getBean(IDocumentationService.class);
                IInvariantService invariantService = appContext.getBean(IInvariantService.class);
                ITestService testService = appContext.getBean(ITestService.class);
                ITestCaseService testCaseService = appContext.getBean(ITestCaseService.class);
                try {

                    String testselected = "";
                    if (request.getParameter("createTest") != null
                            && request.getParameter("createTest").compareTo("All") != 0) {
                        testselected = request.getParameter("createTest");
                    } else {
                        Cookie[] cookies = request.getCookies();
                            if (cookies != null) { 
                             for (Cookie cookie : cookies) {
                               if (cookie.getName().equals("TestPageCreateTest")) {
                                 testselected = URLDecoder.decode(cookie.getValue(), "UTF-8");
                                }
                              }
                            } 
                    }

            %>
            <table id="createTcTable" class="arrond" style="display : table">
                <tr>
                    <td class="separation">   
                        <form action="TestCaseCreate.jsp" method="post" name="testSelected">
                            <table  class="wob" style="text-align: left; border-collapse: collapse" border="0px" cellpadding="0px" cellspacing="0px">
                                <tr>
                                    <td colspan="2" class="wob"><h4 style="color : blue">Test Information</h4></td>
                                </tr>
                                <tr id="header"> 
                                    <td class="wob" style="width: 300px"><%=docService.findLabelHTML("test", "test", "Test")%></td>
                                </tr>
                                <tr>
                                    <td class="wob">
                                        <select id="createTest" name="createTest" style="width: 300px ; font-weight: bold " OnChange="document.testSelected.submit(); SetCookie('TestPageCreateTest', this.value)">
                                            <%	if (testselected.compareTo("%%") == 0) {
                                            %><option style="width: 200px" value="All">-- Choose Test --</option>
                                            <%}
                                                String optstyle;
                                                for (Test test : testService.getListOfTest()) {
                                                    if (test.getActive().equalsIgnoreCase("Y")) {
                                                        optstyle = "font-weight:bold;";
                                                    } else {
                                                        optstyle = "font-weight:lighter;";
                                                    }%>
                                            <option style="width: 200px;<%=optstyle%>" value="<%=test.getTest()%>" <%=testselected.compareTo(test.getTest()) == 0 ? " SELECTED " : ""%>><%=test.getTest()%></option>
                                            <%}%>
                                        </select>
                                    </td>
                                </tr>
                            </table>
                        </form>
                    </td></tr><tr><td id="wob">
                        <form method="post" name="CreateTestCase" action="CreateTestCase">
                            <table>
                                <tr>
                                    <td class="separation">
                                        <table>
                                            <tr>
                                                <td colspan="6" class="wob"><h4 style="color : blue">Testcase Information</h4></td>
                                            </tr>
                                            <tr id="header">
                                                <td class="wob" style="width: 100px"><%=docService.findLabelHTML("testcase", "testcase", "TestCase")%></td>
                                                <td class="wob" style="width: 100px"><%=docService.findLabelHTML("testcase", "Origine", "Origin")%></td>
                                                <td class="wob" style="width: 100px"><%=docService.findLabelHTML("testcase", "RefOrigine", "RefOrigine")%></td>
                                                <td class="wob" style="width: 100px; visibility:hidden"><%docService.findLabelHTML("testcase", "Creator", "creator");%></td>
                                                <td class="wob" style="width: 100px"><%=docService.findLabelHTML("project", "idproject", "Project")%></td>
                                                <td class="wob" style="width: 100px"><%=docService.findLabelHTML("testcase", "ticket", "Ticket")%></td>
                                                <td class="wob" style="width: 70px"><%=docService.findLabelHTML("testcase", "BugID", "BugID")%></td>
                                            </tr>
                                            <%
                                                String tcnumber = "";
                                                if (testselected.compareTo("%%") == 0) {
                                                } else {
                                                    String maxTemp = testCaseService.getMaxNumberTestCase(testselected);

                                                    if (StringUtils.isNotBlank(maxTemp)) {

                                                        int testcasenumber = Integer.valueOf(maxTemp) + 1;

                                                        if (testcasenumber < 10) {
                                                            tcnumber = "000".concat(String.valueOf(testcasenumber)).concat("A");
                                                        }

                                                        if (testcasenumber >= 10 && testcasenumber < 99) {
                                                            tcnumber = "00".concat(String.valueOf(testcasenumber)).concat("A");
                                                        }

                                                        if (testcasenumber >= 100 && testcasenumber < 999) {
                                                            tcnumber = "0".concat(String.valueOf(testcasenumber)).concat("A");
                                                        }

                                                        if (testcasenumber >= 1000) {
                                                            tcnumber = String.valueOf(testcasenumber).concat("A");
                                                        }
                                                    } else {
                                                        tcnumber = "0001A";
                                                    }
                                                }

                                            %>
                                            <tr>
                                                <td class="wob"><input id="createTestcase" name="createTestcase" style="width: 100px; font-weight: bold" value="<%=tcnumber%>">
                                                </td>
                                                <td class="wob">
                                                    <select id="createOrigine" style="width: 100px;" name="createOrigine" onchange="SetCookie('TestPageCreateOrigin', this.value) ">
                                                        <option value="All">-- Origin --</option>
                                                        <%
                                                            for (Invariant inv : invariantService.findListOfInvariantById("ORIGIN")) {
                                                        %>
                                                        <option value="<%=inv.getValue()%>"><%=inv.getValue()%></option>
                                                        <%
                                                            }
                                                        %>
                                                    </select>
                                                </td>
                                                <td class="wob"><input id="createRefOrigine" style="width: 90px;" name="createRefOrigine"></td>
                                                <td class="wob" style="visibility:hidden"><input id="createCreator" style="width: 90px;" name="createCreator"></td>
                                                <td class="wob">
                                                    <%=ComboProject(appContext, "createProject", "width: 90px", "createProject", "", "", "SetCookie('TestPageCreateProject', this.value)", true, "", "No Project Defined.")%>
                                                </td>
                                                <td class="wob"><input id="createTicket" style="width: 90px;" name="createTicket"></td>
                                                <td class="wob"><input id="createBugID" style="width: 70px;" name="createBugID"></td>
                                            </tr>
                                        </table>
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
                                                            <%
                                                                List<Invariant> invariantList = invariantService.findListOfInvariantById("COUNTRY");
                                                                for (Invariant inv : invariantList) {
                                                            %>
                                                            <td class="wob" style="font-size : x-small ; width: 20px; text-align: center"><%=inv.getValue()%> <input type="hidden" name="testcase_country_all" value="<%=inv.getValue()%>"></td>
                                                                <%
                                                                    }
                                                                %>
                                                        </tr>
                                                        <tr>
                                                            <td class="wob"><select id="createApplication" name="createApplication" style="width: 140px" onchange="SetCookie('TestPageCreateApplication', this.value)">
                                                                    <%
                                                                        for (Application app : applicationService.findAllApplication()) {
                                                                    %>
                                                                    <option value="<%=app.getApplication()%>"><%=app.getApplication()%></option>
                                                                    <%
                                                                        }
                                                                    %>
                                                                </select></td>
                                                            <td class="wob"><%=ComboInvariant(appContext, "createRunQA", "width: 75px", "createRunQA", "runqa", "RUNQA", "", "SetCookie('TestPageCreateRunQA', this.value) ", null)%></td>
                                                            <td class="wob"><%=ComboInvariant(appContext, "createRunUAT", "width: 75px", "createRunUAT", "runuat", "RUNUAT", "", "SetCookie('TestPageCreateRunUAT', this.value) ", null)%></td>
                                                            <td class="wob"><%=ComboInvariant(appContext, "createRunPROD", "width: 75px", "createRunPROD", "runprod", "RUNPROD", "", "SetCookie('TestPageCreateRunPROD', this.value) ", null)%></td>
                                                            <td class="wob"><%=ComboInvariant(appContext, "createPriority", "width: 90px", "createPriority", "priority", "PRIORITY", "", "SetCookie('TestPageCreatePriority', this.value) ", null)%></td>
                                                            <td class="wob"><%=ComboInvariant(appContext, "createGroup", "width: 140px", "createGroup", "editgroup", "GROUP", "", "SetCookie('TestPageCreateGroup', this.value) ", null)%></td>
                                                            <td class="wob"><%=ComboInvariant(appContext, "createStatus", "width: 140px", "createStatus", "editStatus", "TCSTATUS", "", "SetCookie('TestPageCreateStatus', this.value) ", null)%></td>
                                                            <%
                                                                for (Invariant inv : invariantList) {
                                                            %> 
                                                            <td class="wob"><input value="<%=inv.getValue()%>" type="checkbox" name="createTestcase_country_general" id="createTestcase_country_general_<%=inv.getValue()%>" onchange="SetCookie('TestPageCreateCountry'+this.value, this.checked===true?'TRUE':'FALSE')"></td>
                                                                <%
                                                                    }
                                                                %>
                                                        </tr>
                                                    </table>
                                                </td>
                                            </tr>
                                        </table>
                                        <table>
                                            <tr>
                                                <td class="wob" style="text-align: left; vertical-align : top ; border-collapse: collapse">
                                                    <table class="wob"  style="text-align: left; border-collapse: collapse" border="0px" cellpadding="0px" cellspacing="0px">
                                                        <tr id="header">
                                                            <td class="wob" style="width: 1200px"><%out.print(docService.findLabelHTML("testcase", "description", "Description"));%></td>
                                                        </tr><tr>
                                                            <td class="wob"><input id="createDescription" style="width: 1200px;" name="createDescription"></td>
                                                        </tr>
                                                        <tr  id="header">

                                                        </tr>
                                                    </table>
                                                </td>
                                            <tr></tr>
                                            <td class="wob" style="text-align: left; vertical-align : top ; border-collapse: collapse">
                                                <table>   
                                                    <tr id="header">
                                                        <td class="wob" style="width: 600px"><%out.print(docService.findLabelHTML("testcase", "BehaviorOrValueExpected", "Value Expected"));%></td>
                                                        <td class="wob" style="width: 600px"><%out.print(docService.findLabelHTML("testcase", "HowTo", "HowTo"));%></td>
                                                    </tr>
                                                    <tr>
                                                        <td class="wob" style="text-align: left; border-collapse: collapse">
                                                            <textarea id="createBehaviorOrValueExpected" rows="9" style="width: 600px;" name="createBehaviorOrValueExpected"></textarea>
                                                            <input type="hidden" id="valueDetail" name="valueDetail" value="">
                                                        </td>
                                                        <td class="wob">
                                                            <textarea id="createHowTo" rows="9" style="width: 600px;" name="createHowTo"></textarea>
                                                            <input type="hidden" id="howtoDetail" name="howtoDetail" value="">
                                                        </td>
                                                    </tr>
                                                </table><br>
                                            </td>
                                </tr>
                            </table>
                    </td>
                </tr>

                <tr>
                    <td class="wob">
                        <input type="hidden" id="createTestSelect" name="createTestSelect" value="<%=testselected%>"> 
                        <table>
                            <tr>
                                <td class="wob"><input type="submit" name="submitCreation" value="Create Test Case" onclick="$('#howtoDetail').val($('#createHowTo').elrte('val'));
                                        $('#valueDetail').val($('#createBehaviorOrValueExpected').elrte('val'));">
                                </td>
                            </tr>
                        </table>
                    </td></tr>
            </table>
        </form>
    </td>
</tr>
</table>
<%
    } catch (CerberusException ex) {
        MyLogger.log("TestCaseCreate.jsp", Level.ERROR, "Cerberus exception : " + ex.toString());
        out.println("</script>");
        out.print("<script type='text/javascript'>alert(\"Unfortunately an error as occurred, try reload the page.\\n");
        out.print("Detail error: " + ex.getMessageError().getDescription() + "\");</script>");
    }
%>
</div>
<script type="text/javascript">
    $(document).ready(function() {
        var cookies = new Array;
        cookies.push(['#createGroup', GetCookie('TestPageCreateGroup')]);
        cookies.push(['#createStatus', GetCookie('TestPageCreateStatus')]);
        cookies.push(['#createPriority', GetCookie('TestPageCreatePriority')]);
        cookies.push(['#createRunPROD', GetCookie('TestPageCreateRunPROD')]);
        cookies.push(['#createRunUAT', GetCookie('TestPageCreateRunUAT')]);
        cookies.push(['#createRunQA', GetCookie('TestPageCreateRunQA')]);
        cookies.push(['#createApplication', GetCookie('TestPageCreateApplication')]);
        cookies.push(['#createProject', GetCookie('TestPageCreateProject')]);
        cookies.push(['#createOrigine', GetCookie('TestPageCreateOrigin')]);
        
        for (var a = 0; a < cookies.length; a++) {
            $(cookies[a][0]).find('option').each(function(i, opt) {
                if (opt.value === cookies[a][1]) {
                    $(opt).attr('selected', 'selected');
                }
            });

        }
        var countries = document.getElementsByName("createTestcase_country_general");
        for(var c=0 ; c<countries.length;c++){
            var name = '#'+countries[c].id;
            if (GetCookie('TestPageCreateCountry'+countries[c].value)==="TRUE"){
            $(name).attr('checked', 'checked');
        }
        }
    });

</script>

<br><% out.print(display_footer(DatePageStart));%>
</body>
</html>
