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
<%@page import="org.cerberus.crud.entity.TCase"%>
<%@page import="org.cerberus.crud.service.ITestCaseService"%>
<%@page import="org.cerberus.crud.service.IDocumentationService"%>
<%@page import="java.net.URLEncoder"%>
<%@ page import="org.cerberus.util.ParameterParserUtil" %>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ include file="include/function.jsp" %>
<%!
    /**
     * Convert list of values from request parameter to string.
     *
     * @param req       http request with all form input
     * @param valueName name of the parameter to return
     * @return string containing all values of parameter or null if parameter null
     */
    private String getValues(HttpServletRequest req, String valueName) {
        StringBuilder whereClause = new StringBuilder();
        String[] values = req.getParameterValues(valueName);

        if (values != null) {
            whereClause.append(" '").append(values[0]);
            for (int i = 1; i < values.length; i++) {
                if (!"All".equalsIgnoreCase(values[i]) && !"".equalsIgnoreCase(values[i].trim())) {
                    whereClause.append("', '").append(values[i]);
                }
            }
            whereClause.append("' ");
            return whereClause.toString();
        }
        return null;
    }
%>
<%

    IDocumentationService docService = appContext.getBean(IDocumentationService.class);
    ITestCaseService testCaseService = appContext.getBean(ITestCaseService.class);
    try {
%>
    <br><br>
    <form id="AddTestBatteryContent" action="./AddTestBatteryContent"></form>
    <button type="submit" id="submit" name="submit">Add Test Cases</button>
    <table class="backDiv" style="width:100%">
        <tr>
            <td id="wob">
                <table id="reportingExec" class="arrondTable dataTable" style="text-align: left;border-collapse:collapse;display:table" border="1px" cellpadding="0" cellspacing="1">
                    <thead>
                    <tr id="headerFirst"  class="filters">
                        <td ><input type="checkbox" name="checkAllTestCases" id="checkAllTestCases" onclick="selectAll();"><label for="checkAllTestCases">All</label></td>
                        <td >Test</td>
                        <td >TestCase</td>
                        <td >Application</td>
                        <td >Description</td>
                        <td >Detailed Description</td>
                    </tr>
                    </thead>
                    <tbody>
                    <%

                        TCase searchTCase = new TCase();
                        searchTCase.setTest(getValues(request, "Test"));
                        searchTCase.setProject(getValues(request, "Project"));
                        String system = getValues(request, "System");
                        searchTCase.setApplication(getValues(request, "Application"));
                        searchTCase.setActive(getValues(request, "TcActive"));
                        searchTCase.setPriority(ParameterParserUtil.parseIntegerParam(request.getParameter("Priority"), -1));
                        searchTCase.setStatus(getValues(request, "Status"));
                        searchTCase.setGroup(getValues(request, "Group"));
                        searchTCase.setTargetSprint(getValues(request, "TargetBuild"));
                        searchTCase.setTargetRevision(getValues(request, "TargetRev"));
                        searchTCase.setCreator(getValues(request, "Creator"));
                        searchTCase.setImplementer(getValues(request, "Implementer"));

                        int indexColor = 0;
//                        for (TCase tCase : testCaseService.findTestCaseByAllCriteria(searchTCase, "", system)) {
                        for (TCase tCase : testCaseService.findTestCaseByGroupInCriteria(searchTCase, system)) {
                            if(!tCase.getGroup().equalsIgnoreCase("PRIVATE")) {
                                indexColor++;
                    %>
                        <tr class="testCaseExecutionResult <%=(indexColor%2 == 1) ? "even" : "odd"%>">
                        <td ><input class="selecttestcase" type="checkbox" name="testcaseselected" value="<%="Test="+URLEncoder.encode(tCase.getTest(), "UTF-8")+"&TestCase="+URLEncoder.encode(tCase.getTestCase(), "UTF-8") %>"></td>
                        <td ><%=tCase.getTest()%></td>
                        <td ><a href="TestCase.jsp?Load=Load&Test=<%=tCase.getTest()%>&TestCase=<%=tCase.getTestCase()%>"> <%=tCase.getTestCase()%></a></td>
                        <td ><%=tCase.getApplication()%></td>
                        <td ><%=tCase.getShortDescription()%></td>
                        <td ><%=tCase.getDescription()%></td>
                    <%
                            }
                        }
                    %>
                    </tbody>
                </table>
            </td>
        </tr>
    </table>
    <button type="submit" id="submit" name="submit">Add Test Cases</button>
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
