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
<%@page import="java.net.URLEncoder"%>
<%@ page import="org.cerberus.service.*" %>
<%@ page import="org.cerberus.entity.*" %>
<%@ page import="org.cerberus.util.ParameterParserUtil" %>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ include file="include/function.jsp" %>
<%

    IDocumentationService docService = appContext.getBean(IDocumentationService.class);
    ITestCaseService testCaseService = appContext.getBean(ITestCaseService.class);
    IManualTestCaseService manualTestCaseService = appContext.getBean(IManualTestCaseService.class);

    try {
%>
    <br><br>
    <form id="AddTestBatteryContent"></form>
    <div>
    <table class="backDiv" style="width:100%;">
        <tr>
            <td id="wob" style="width:40%;">
                <div style="height:200px; overflow: auto">
                <table id="reportingExec" class="arrondTable dataTable" style="text-align: left;border-collapse:collapse;display:table" border="1px" cellpadding="0" cellspacing="1">
                    <thead>
                    <tr id="headerFirst"  class="filters">
                        <td ><input type="checkbox" name="checkAllTestCases" id="checkAllTestCases" onclick="selectAll();"><label for="checkAllTestCases">All</label></td>
                        <td ><%=docService.findLabelHTML("test", "test", "Test")%></td>
                        <td ><%=docService.findLabelHTML("testcase", "testcase", "TestCase")%></td>
                        <td ><%=docService.findLabelHTML("application", "application", "Aplication")%></td>
                        <td ><%=docService.findLabelHTML("testcase", "description", "Description")%></td>
                    </tr>
                    </thead>
                    <tbody>
                    <%

                        TCase searchTCase = new TCase();
                        searchTCase.setTest(request.getParameter("Test"));
                        searchTCase.setProject(request.getParameter("Project"));
                        String system = request.getParameter("System");
                        searchTCase.setApplication(request.getParameter("Application"));
                        searchTCase.setActive(request.getParameter("TcActive"));
                        searchTCase.setPriority(ParameterParserUtil.parseIntegerParam(request.getParameter("Priority"), -1));
                        searchTCase.setStatus(request.getParameter("Status"));
                        searchTCase.setGroup(request.getParameter("Group"));
                        searchTCase.setTargetSprint(request.getParameter("TargetBuild"));
                        searchTCase.setTargetRevision(request.getParameter("TargetRev"));
                        searchTCase.setCreator(request.getParameter("Creator"));
                        searchTCase.setImplementer(request.getParameter("Implementer"));
                        searchTCase.setFunction(request.getParameter("Function"));
                        String[] test = request.getParameter("Test")==null?null:request.getParameterValues("Test");
                        String[] project = request.getParameter("Project")==null?null:request.getParameterValues("Project");
                        String[] app = request.getParameter("Application")==null?null:request.getParameterValues("Application");
                        String[] active = request.getParameter("TcActive")==null?null:request.getParameterValues("TcActive");
                        String[] priority = request.getParameter("Priority")==null?null:request.getParameterValues("Priority");
                        String[] status = request.getParameter("Status")==null?null:request.getParameterValues("Status");
                        String[] group = request.getParameter("Group")==null?null:request.getParameterValues("Group");
                        String[] targetBuild = request.getParameter("TargetBuild")==null?null:request.getParameterValues("TargetBuild");
                        String[] targetRev = request.getParameter("TargetRev")==null?null:request.getParameterValues("TargetRev");
                        String[] creator = request.getParameter("Creator")==null?null:request.getParameterValues("Creator");
                        String[] implementer = request.getParameter("Implementer")==null?null:request.getParameterValues("Implementer");
                        String[] function = request.getParameter("Function")==null?null:request.getParameterValues("Function");
                        String[] campaign = request.getParameter("Campaign")==null?null:request.getParameterValues("Campaign");
                        String[] battery = request.getParameter("Battery")==null?null:request.getParameterValues("Battery");
                        
                        int indexColor = 0;
                        for (TCase tCase : testCaseService.findByCriteria(test, project, app, active, priority, status, group, targetBuild, targetRev, creator, implementer, function, campaign, battery)) {
                            if(!tCase.getGroup().equalsIgnoreCase("PRIVATE")) {
                                indexColor++;
                    %>
                        <tr class="testCaseExecutionResult <%=(indexColor%2 == 1) ? "even" : "odd"%>" name="resultLine" id="row_<%=tCase.getTest().replace(" ", "")%>_<%=tCase.getTestCase()%>">
                        <td ><input class="selecttestcase" type="checkbox" name="testcaseselected" data-test="<%=tCase.getTest().replace(" ", "")%>" data-testcase="<%=tCase.getTestCase()%>" value="<%="Test="+URLEncoder.encode(tCase.getTest(), "UTF-8")+"&TestCase="+URLEncoder.encode(tCase.getTestCase(), "UTF-8") %>"></td>
                        <td  onclick="addToSelectedTest()"><%=tCase.getTest()%></td>
                        <td ><a href="TestCase.jsp?Load=Load&Test=<%=tCase.getTest()%>&TestCase=<%=tCase.getTestCase()%>"> <%=tCase.getTestCase()%></a></td>
                        <td ><%=tCase.getApplication()%></td>
                        <td ><%=tCase.getShortDescription()%></td>
                        </tr>
                    <%
                            }
                        }
                    %>
                </tbody>
                </table>
                </div>
            </td>
            <td class="wob" style="width:1%;"><button type="button" id="button" name="submit" onclick="addToSelectedTest()">>></button></td>
            <td id="wob" style="width:40%;">
                <div class="filters" style="float:right; display:block; width:100%;height:200px; overflow:auto">
                    <p style="float:left" class="dttTitle">SelectedTest</p>
                    <div style="clear:both; background-color:white">
                                <div size="16" id="testSelected" name="testSelected"
                                        style="width: 600px">
                                </div>
                            </div>
                </div>
                
            </td>
        </tr>
    </table>
    </div>
   
    <script>
        var isSelectedAll = false;
        function selectAll(){
            isSelectedAll = !isSelectedAll;
            $('input.selecttestcase').prop('checked',isSelectedAll);
        }
        
        // prepare all forms for ajax submission
        $('#AddTestBatteryContent').on('submit', function(e) {
            $('#displayResult').html('<img src="./images/loading.gif"> loading...');
            e.preventDefault(); // <-- important
            $(this).ajaxSubmit({
                target: '#displayResult'
            });
        });

    </script>
    <%
        } catch (Exception e) {
            out.println(e);
        }
    %>
