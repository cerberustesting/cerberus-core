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
<%@page import="org.apache.commons.lang3.StringUtils"%>
<%@page import="org.cerberus.entity.TCase"%>
<%@page import="org.cerberus.entity.Test"%>
<%@page import="org.cerberus.service.ITestCaseService"%>
<%@page import="org.cerberus.service.IDocumentationService"%>
<%@page import="org.cerberus.service.ITestService"%>
<%@page import="org.cerberus.service.ITestCaseCountryService"%>
<%@page import="org.cerberus.service.impl.TestService"%>
<%@page import="org.cerberus.dao.ITestDAO"%>
<% Date DatePageStart = new Date();%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta content="text/html; charset=UTF-8" http-equiv="content-type">
        <title>Test</title>
        <link rel="stylesheet" type="text/css" href="css/crb_style.css">
        <link rel="stylesheet" type="text/css" href="css/jquery.dataTables.css">
        <link rel="stylesheet" type="text/css" href="css/jquery-ui.css">
        <link rel="stylesheet" type="text/css" href="css/dataTables_jui.css">
        <link rel="shortcut icon" type="image/x-icon" href="images/favicon.ico">
        <link type="text/css" rel="stylesheet" href="css/jquery.multiselect.css">
        <script type="text/javascript" src="js/jquery-1.9.1.min.js"></script>
        <script type="text/javascript" src="js/jquery-ui-1.10.2.js"></script>
        <script type="text/javascript" src="js/jquery.jeditable.mini.js"></script>
        <script type="text/javascript" src="js/jquery.dataTables.min.js"></script>
        <script type="text/javascript" src="js/jquery.dataTables.editable.js"></script>
        <script type="text/javascript" src="js/jquery.validate.min.js"></script>
        <script type="text/javascript" src="js/jquery.multiselect.js" charset="utf-8"></script>
    </head>
    <body>
        <%@ include file="include/function.jsp" %>
        <%@ include file="include/header.jsp" %>
        <div id="body">
            <%

                ITestService testService;
                ITestCaseService testcaseService;
                ITestCaseCountryService testcaseCountryService;

                /*
                 * Database connexion
                 */
                Connection conn = db.connect();
                IDocumentationService docService = appContext.getBean(IDocumentationService.class);

                try {
                    testService = appContext.getBean(ITestService.class);
                    testcaseService = appContext.getBean(ITestCaseService.class);
                    testcaseCountryService = appContext.getBean(ITestCaseCountryService.class);
                    List<Test> tests = testService.getListOfTest();

                    String testSelected = "";
                    if (request.getParameter("stestbox") != null) {
                        testSelected = request.getParameter("stestbox");
                    }

                    boolean canEdit = false;
                    if (request.getUserPrincipal() != null && request.isUserInRole("TestAdmin")) {
                        canEdit = true;
                    }
            %>
            <!-- Select List -->
            <div class="filters" style="float:left; width:100%; height:30px">
                <div style="float:left; width:100px"><p class="dttTitle">Filters</p></div>
                <div style="float:left; width:100px;font-weight: bold;"><%out.print(docService.findLabelHTML("test", "test", "Test"));%></div>
                <div id="selectboxtestpage" style="float:left">
                    <form action="Test.jsp" method="post" name="selectTest">
                        <select id="stestbox" name="stestbox" style="width: 500px">
                            <option style="width: 500px" value="NONE">-- Choose Test --</option>
                            <%
                                String optstyle = "";
                                for (Test test : tests) {
                                    if (test.getActive().equalsIgnoreCase("Y")) {
                                        optstyle = "font-weight:bold;";
                                    } else {
                                        optstyle = "font-weight:lighter;";
                                    }
                            %><option style="width: 500px;<%=optstyle%>" <%
                                if (testSelected.equalsIgnoreCase(test.getTest())) {
                                    out.print("selected=\"selected\"");
                                }
                                    %> value="<%=test.getTest()%>"> <%=test.getTest() + " - " + test.getDescription()%> </option>
                            <%
                                }
                            %></select>
                        <input id="loadTestbutton" style="height:23px" class="button" type="submit" value="Load">
                    </form>
                </div>
            </div>
            <%
                /*
                 * Test & TestCase
                 */
                if (!testSelected.equals("")) {
                    Test testSelect = testService.findTestByKey(testSelected);
                    List<TCase> testcaseList = testcaseService.findTestCaseByTest(testSelected);
            %>

            <br><div class="backDiv" style="clear:both; width:100%">
                <% if (canEdit) { %>
                <p style="float:left" class="dttTitle">Test Parameters</p>
                <div id="dropDownDownArrow" style="float:left"><a onclick="javascript:switchDivVisibleInvisible('testParameterDetail', 'testParameterSummary');
                        switchDivVisibleInvisible('dropDownUpArrow', 'dropDownDownArrow')"><img src="images/dropdown.gif"/></a>
                </div>
                <div id="dropDownUpArrow" style="display:none; float:left"><a onclick="javascript:switchDivVisibleInvisible('testParameterSummary', 'testParameterDetail');
                        switchDivVisibleInvisible('dropDownDownArrow', 'dropDownUpArrow')"><img style="transform: rotate(180deg);" src="images/dropdown.gif"/></a>
                </div>
                <div  id="testParameterDetail" style="display:none; clear:both; width:100%">
                    <form method="post" name="DeleteTest">
                        <div style="clear:both">
                            <div style="float:left; width:150px; text-align: left"><%out.print(docService.findLabelHTML("test", "test", "Test"));%></div>
                            <div style="float:left"><input style="font-weight: bold; width: 200px" name="test_test" id="test_test"
                                                           readonly="readonly" value="<%=testSelect.getTest()%>">
                            </div>
                        </div>
                        <div style="clear:both">
                            <div style="float:left; width:150px; text-align: left"><%out.print(docService.findLabelHTML("test", "description", "Description"));%></div>
                            <div style="float:left"><input id="test_description" style="width: 800px" name="test_description" value="<%=testSelect.getDescription()%>"
                                                           maxlength="300">
                            </div>
                        </div>
                        <div style="clear:both">
                            <div style="float:left; width:150px; text-align: left"><%out.print(docService.findLabelHTML("test", "active", "Active"));%></div>
                            <div style="float:left">
                                <select id="test_active" style="width: 40px;" name="test_active">
                                    <option value="N">No</option>
                                    <option <%="Y".equalsIgnoreCase(testSelect.getActive()) ? "selected='selected'" : "" %> value="Y">Yes</option>
                                </select>
                            </div>
                        </div>
                        <div style="clear:both">
                            <div style="float:left; width:150px; text-align: left"><%out.print(docService.findLabelHTML("test", "automated", "Automated"));%></div>
                            <div style="float:left"><select id="test_automated" style="width: 40px;" name="test_automated">
                                    <option value="N">No</option>
                                    <option <%="Y".equalsIgnoreCase(testSelect.getAutomated()) ? "selected='selected'" : "" %> value="Y">Yes</option>
                                </select>
                            </div>
                        </div>

                        <div style="clear:both">
                            <input id="deletetestbutton" class="button" name="delete_test" value="Delete Test" type="submit" onclick="redirectionTestCase(2, '<%=testSelect.getTest()%>')">
                            <input id="savetestbutton" class="button" name="save_test" value="Save Test Modification" type="submit" onclick="redirectionTestCase(0, '<%=testSelect.getTest()%>')">
                        </div></form></div>
                        <% }%>
                <div id="testParameterSummary" style="display:inline-block;width:100%; height:30px">
                    <div style="float:left;font-weight: bold; width: 25%" name="test" id="test">Test : <%=testSelect.getTest()%></div>
                    <div style="float:left;font-weight: bold; width: 25%" name="description">Description : <%=testSelect.getDescription()%></div>
                    <div style="float:left;font-weight: bold; width: 25%" name="active">Active : <%=testSelect.getActive()%></div>
                    <div style="float:left;font-weight: bold; width: 25%" name="automated">Automated : <%=testSelect.getAutomated()%></div>

                </div>
            </div><br><br>
            <form method="post" name="DeleteTestCaseFromTestPage" action="DeleteTestCaseFromTestPage">               
                <div style="clear:both;">
                    <div class="filters" style="float:left;width:100%; height:30px">
                        <p style="float:left; width:200px" class="dttTitle">TestCase List</p>
                        <% if (canEdit) {%>
                        <input style="float:left" class="button" name="submit_changes" disabled=disabled id="submit_changes" value="Delete TestCase" type="submit"> 
                        <input id="test_of_page" name="test_of_page" value="<%=testSelected%>" hidden='hidden'>
                        <%}%></div>
                    <div style="clear:both;">
                        <table id="testcasetable" style="clear:both; text-align: left; border-collapse: collapse" border="0px" cellpadding="0px" cellspacing="0px">
                            <!--					<tr id="header" style="position:relative;top:expression(this.offsetParent.scrollTop-2);">-->
                            <tr class="backDiv" style="height:30px">   
                                <td style="width: 1%"><%out.print(docService.findLabelHTML("page_test", "delete", "Delete"));%></td>
                                <td style="width: 20%" colspan="2">Testcase Information</td>
                                <td style="width: 69%" colspan="2">Testcase Parameters</td>
                                <td style="width: 10%">Activation Criterias</td>
                            </tr>
                            <%
                                int i = 0;
                                int j = 0;

                                for (TCase tcase : testcaseList) {

                                    List<String> countryList = testcaseCountryService.findListOfCountryByTestTestCase(tcase.getTest(), tcase.getTestCase());
                                    StringBuilder countries = new StringBuilder();
                                    for (String countryUnit : countryList) {
                                        countries.append(countryUnit);
                                        countries.append("-");
                                    }

                                    String classColor = "";
                                    j = i % 2;
                                    if (j == 1) {
                                        classColor = "tableRowPair";
                                    } else {
                                        classColor = "tableRowImpair";
                                    }

                                    i++;

                            %>
                            <tr class="<%=classColor%>">
                                <td>

                                    <table><tr>
                                            <td>
                                                <%if (canEdit) {%>
                                                <input id="test_testcase_delete" name="test_testcase_delete" type="checkbox" value="<%=tcase.getTestCase()%>" onchange="javascript:enableElement('submit_changes')">
                                                <%}%>
                                            </td>
                                        </tr><tr><td style="text-align: center">
                                                <a style="font-size: xx-small;" href="TestCase.jsp?Test=<%=tcase.getTest()%>&TestCase=<%=tcase.getTestCase()%>&Load=Load">edit</a></td>
                                        </tr></table>
                                </td><td valign="top">
                                    <table><tr>    
                                            <td id="testcase_testcase1" class="wob" style="font-weight: bold;" name="testcase_testcase" readonly="readonly">
                                                <%=tcase.getTest()%>: <%=tcase.getTestCase()%></td></tr><tr>
                                                <% if (StringUtils.isNotBlank(tcase.getOrigin())) {%>
                                            <td id="testcase_origine" class="wob" name="testcase_origine">
                                                <%=StringUtils.isBlank(tcase.getOrigin()) ? "" : tcase.getOrigin()%></td></tr><tr>
                                                <% }
                                                    if (StringUtils.isNotBlank(tcase.getRefOrigin())) {%>
                                            <td id="testcase_reforigine" class="wob" name="testcase_refOrigine">
                                                <%=StringUtils.isBlank(tcase.getRefOrigin()) ? "" : "Ref:"+tcase.getRefOrigin()%></td></tr><tr>
                                                <%}
                                                    if (StringUtils.isNotBlank(tcase.getCreator())) {%>
                                            <td id="testcase_creator" class="wob" name="testcase_creator">
                                                <%=StringUtils.isBlank(tcase.getCreator()) ? "" : "Creator:"+tcase.getCreator()%></td></tr><tr>
                                                <%}
                                                    if (StringUtils.isNotBlank(tcase.getRunQA())) {%>
                                            <td id="testcase_activeqa" class="wob" name="testcase_activeqa">
                                                <%=StringUtils.isBlank(tcase.getRunQA()) ? "" : "RunQA:"+tcase.getRunQA()%></td>
                                                <%}
                                                if (StringUtils.isNotBlank(tcase.getRunUAT())) {%>
                                            <td id="testcase_activeuat" class="wob" name="testcase_activeuat">
                                                <%=StringUtils.isBlank(tcase.getRunUAT()) ? "" : "RunUAT:"+tcase.getRunUAT()%></td>
                                                <%}
                                                if (StringUtils.isNotBlank(tcase.getRunPROD())) {%>
                                            <td id="testcase_activeprod" class="wob" name="testcase_activeprod">
                                                <%=StringUtils.isBlank(tcase.getRunPROD()) ? "" : "RunPROD:"+tcase.getRunPROD()%></td>
                                                <%}%>
                                        </tr></table>      
                                </td><td valign="top">
                                    <table><tr>
                                            <%
                                                if (StringUtils.isNotBlank(tcase.getApplication())) {%> 
                                            <td id="testcase_application" class="wob" name="testcase_application" style="width: 250px">
                                                <%=StringUtils.isBlank(tcase.getRunPROD()) ? "" : "Application:"+tcase.getApplication()%></td></tr><tr>
                                                <%}
                                                    if (StringUtils.isNotBlank(tcase.getBugID())) {%>
                                            <td id="testcase_bugID" class="wob" name="testcase_bugID">
                                                <%=StringUtils.isBlank(tcase.getBugID()) ? "" : "BugID:"+tcase.getBugID()%></td></tr><tr>
                                                <%}
                                                    if (StringUtils.isNotBlank(tcase.getProject())) {%> 
                                            <td id="testcase_project" class="wob" name="testcase_project">
                                                <%=StringUtils.isBlank(tcase.getProject()) ? "" : "Ticket:"+tcase.getProject()%></td></tr><tr>
                                                <%}
                                                    if (StringUtils.isNotBlank(tcase.getTicket())) {%>
                                            <td id="testcase_ticket" class="wob" name="testcase_ticket">
                                                <%=StringUtils.isBlank(tcase.getTicket()) ? "" : "Ticket:"+tcase.getTicket()%></td></tr><tr>
                                                <%}%>
                                            <td id="testcase_priority" class="wob" name="testcase_priority">
                                                <%="Priority:"+tcase.getPriority()%></td></tr><tr>
                                        </tr></table>   
                                </td><td valign="top">
                                    <table><tr>
                                            <%
                                                if (StringUtils.isNotBlank(tcase.getGroup())) {%> 

                                            <td id="testcase_group" class="wob" name="testcase_group">
                                                <%=StringUtils.isBlank(tcase.getGroup()) ? "" : "Group:"+tcase.getGroup()%></td></tr><tr>
                                                <%}
                                                    if (StringUtils.isNotBlank(tcase.getStatus())) {%>
                                            <td id="testcase_status" class="wob" name="testcase_status">
                                                <%=StringUtils.isBlank(tcase.getStatus()) ? "" : "Status:"+tcase.getStatus()%></td></tr><tr>
                                                <%}%>
                                            <td id="testcase_countries" class="wob"  style=" font-size: x-small;" name="testcase_countries">
                                                <%=countries%></td>                    
                                        </tr></table>     
                                </td><td style="width:100%" valign="top">
                                    <table><tr>
                                            <td id="testcase_description" class="wob" style="width:100%; font-weight: bold" name="testcase_description">
                                                <%=tcase.getShortDescription()%></td></tr><tr>
                                            <td class="wob" style="width:100%"><textarea  id="testcase_valueexpec" class="wob" rows="1" style="width:100%; background-color: transparent" name="testcase_valueexpec"
                                                                       readonly="readonly"><%=tcase.getDescription()%></textarea></td></tr><tr>
                                        </tr></table>
                                </td><td valign="top">
                                    <table><tr>
                                            <%
                                                if (StringUtils.isNotBlank(tcase.getActive())) {%>
                                            <td id="testcase_tcActive" class="wob" name="testcase_tcActive">
                                                <%=StringUtils.isBlank(tcase.getActive()) ? "" : "Active:"+tcase.getActive()%></td>

                                            <%}
                                                if (StringUtils.isNotBlank(tcase.getFromSprint())) {%>
                                            <td id="testcase_frombuild" class="wob" name="testcase_fromBuild">
                                                <%=StringUtils.isBlank(tcase.getFromSprint()) ? "" : "FromSprint:"+tcase.getFromSprint()%></td></tr><tr>
                                                <%}
                                                    if (StringUtils.isNotBlank(tcase.getFromRevision())) {%>
                                            <td id="testcase_fromrev" class="wob" name="testcase_fromRev">
                                                <%=StringUtils.isBlank(tcase.getFromRevision()) ? "" : "FromRev:"+tcase.getFromRevision()%></td></tr><tr>
                                                <%}
                                                    if (StringUtils.isNotBlank(tcase.getToSprint())) {%>
                                            <td id="testcase_tobuild" class="wob" name="testcase_toBuild">
                                                <%=StringUtils.isBlank(tcase.getToSprint()) ? "" : "ToSprint:"+tcase.getToSprint()%></td></tr><tr>
                                                <%}%>
                                            <td id="testcase_torev" class="wob" name="testcase_toRev">
                                                <%=StringUtils.isBlank(tcase.getToRevision()) ? "" : "ToRev:"+tcase.getToRevision()%></td> 
                                        </tr></table>
                                </td></tr>   

                            <%

                                }
                            %>
                        </table>
                    </div>

                    <%

                    %>
                    <br>

                </div></td></tr></table>
            </form>
            <%                }
            %>
        </div>
        <%
            } catch (Exception e) {
                out.println(e);
            } finally {
                try {
                    conn.close();
                } catch (Exception ex) {
                }
            }
        %>
        <br><% out.print(display_footer(DatePageStart));%>
        <script type="text/javascript">
            (document).ready($("#stestbox").multiselect({
                multiple: false,
                header: "Select an option",
                noneSelectedText: "Select an Option",
                selectedList: 1
            }));
        </script>
        <script type="text/javascript">
            (document).ready($("#test_active").multiselect({
                multiple: false,
                header: "Select an option",
                noneSelectedText: "Select an Option",
                selectedList: 1
            }));
        </script>
        <script type="text/javascript">
            (document).ready($("#test_automated").multiselect({
                multiple: false,
                header: "Select an option",
                noneSelectedText: "Select an Option",
                selectedList: 1
            }));
        </script>


    </body>
</html>
