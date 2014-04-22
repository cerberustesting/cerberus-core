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
<%@page import="org.cerberus.entity.Invariant"%>
<%@page import="org.cerberus.entity.User"%>
<%@page import="org.cerberus.service.IUserService"%>
<%@page import="org.cerberus.service.IInvariantService"%>
<%@page import="java.util.TreeMap"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.Collections"%>
<%@page import="java.util.HashSet"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Set"%>
<%@page import="java.util.HashMap"%>
<%@page import="org.cerberus.entity.TestCaseCountry"%>
<%@page import="org.cerberus.entity.Project"%>
<%@page import="org.cerberus.entity.Test"%>
<%@page import="org.cerberus.entity.BuildRevisionInvariant"%>
<%@page import="org.cerberus.service.IProjectService"%>
<%@page import="org.cerberus.service.ITestService"%>
<%@page import="org.cerberus.service.ITestCaseService"%>
<%@page import="org.cerberus.service.ITestCaseCountryService"%>
<%@page import="org.cerberus.service.IDocumentationService"%>
<%@page import="org.cerberus.service.impl.BuildRevisionInvariantService"%>
<%@page import="org.cerberus.service.IBuildRevisionInvariantService"%>
<%@page import="org.cerberus.service.impl.InvariantService"%>
<%@page import="org.cerberus.service.IApplicationService"%>
<%@page import="org.cerberus.log.MyLogger"%>
<%@page import="org.apache.log4j.Level"%>
<%@page import="org.apache.commons.lang3.StringUtils"%>
<% Date DatePageStart = new Date();%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>

        <%@ include file="include/function.jsp" %>
            <%

                TreeMap<String, String> options = new TreeMap<String, String>();

                IInvariantService invariantService = appContext.getBean(InvariantService.class);
                ITestService testService = appContext.getBean(ITestService.class);
                List<Test> testList = testService.getListOfTest();

                IProjectService projectService = appContext.getBean(IProjectService.class);
                List<Project> projectList = projectService.findAllProject();

                Connection conn = db.connect();
                IDocumentationService docService = appContext.getBean(IDocumentationService.class);

                try {

                    Statement stmt = conn.createStatement();
                    List<Invariant> invariantTCStatus = invariantService.findListOfInvariantById("TCSTATUS");

            %>
            <!--form method="GET" name="Apply" id="Apply" action="ReportingExecutionResult.jsp"-->
                    <div id="filtersList" style="clear:both;">
                    <br><div class="underlinedDiv"></div>
                        <p style="text-align:left" class="dttTitle">Testcase Filters (Displayed Rows)</p>
                        <div style="float:left">
                            <div style="float:left">
                                <div style="width:150px; text-align: left"><%out.print(docService.findLabelHTML("test", "Test", "Test"));%></div>
                                <div>
                                    <%
                                        options.clear();
                                        for (Test testL : testList) {
                                            options.put(testL.getTest(), testL.getTest());
                                        }
                                    %>
                                    <%=generateMultiSelect("Test", request.getParameterValues("Test"), options,
                                            "Select a test", "Select Test", "# of # Test selected", 1, true)%>
                                </div>
                            </div>
                            <div style="float:left">
                                <div style="width:150px; text-align: left"><%out.print(docService.findLabelHTML("project", "idproject", "Project"));%></div>
                                <div>
                                    <%
                                        options.clear();
                                        for (Project project : projectList) {
                                            if (project.getIdProject() != null && !"".equals(project.getIdProject().trim())) {
                                                options.put(project.getIdProject(), project.getIdProject() + " - " + project.getDescription());
                                            }
                                        }


                                    %>
                                    <%=generateMultiSelect("Project", request.getParameterValues("Project"), options,
                                            "Select a project", "Select Project", "# of # Project selected", 1, true)%>
                                </div>
                            </div>
                            <div style="float:left">
                                <div style="clear:both; width:150px; text-align: left"><%out.print(docService.findLabelHTML("application", "System", "System"));%></div>
                                <div style="clear:both">
                                    <%
                                        ResultSet rsSys = stmt.executeQuery("SELECT DISTINCT System FROM application Order by System asc");
                                        options.clear();
                                        while (rsSys.next()) {
                                            options.put(rsSys.getString("System"), rsSys.getString("System"));
                                        }%>
                                    <%=generateMultiSelect("System", request.getParameterValues("System"), options,
                                            "Select a sytem", "Select System", "# of # System selected", 1, true)%>
                                </div>
                            </div>
                            <div style="float:left">
                                <div style="clear:both; width:150px; text-align: left"><%out.print(docService.findLabelHTML("application", "Application", "Application"));%></div>
                                <div style="clear:both">
                                    <%
                                        ResultSet rsApp = stmt.executeQuery("SELECT Application , System FROM application Order by Sort asc");
                                        options.clear();
                                        while (rsApp.next()) {
                                            options.put(rsApp.getString("Application"), rsApp.getString("Application") + " [" + rsApp.getString("System") + "]");
                                        }
                                    %>
                                    <%=generateMultiSelect("Application", request.getParameterValues("Application"), options,
                                            "Select an application", "Select Application", "# of # Application selected", 1, true)%>
                                </div>
                            </div>
                            <div style="float:left">
                                <div style="clear:both; width:150px; text-align: left"><%out.print(docService.findLabelHTML("testcase", "tcactive", "TestCase Active"));%></div>
                                <div style="clear:both">
                                    <%
                                        options.clear();
                                        options.put("Y", "Yes");
                                        options.put("N", "No");
                                    %>
                                    <%=generateMultiSelect("TcActive", request.getParameterValues("TcActive"), options,
                                            "Select Activation state", "Select Activation", "# of # Activation state selected", 1, true)%> 
                                </div>
                            </div>
                            <div style="float:left">
                                <div style="clear:both; width:150px; text-align: left"><%out.print(docService.findLabelHTML("invariant", "PRIORITY", "Priority"));%></div>
                                <div style="clear:both">
                                    <%
                                        ResultSet rsPri = stmt.executeQuery("SELECT DISTINCT value FROM invariant WHERE idname='PRIORITY' Order by sort asc");
                                        options.clear();
                                        while (rsPri.next()) {
                                            options.put(rsPri.getString(1), rsPri.getString(1));
                                        }
                                    %>
                                    <%=generateMultiSelect("Priority", request.getParameterValues("Priority"), options,
                                            "Select a Priority", "Select Priority", "# of # Priority selected", 1, true)%>
                                </div>
                            </div>
                            <div style="float:left">
                                <div style="clear:both; width:150px; text-align: left"><%out.print(docService.findLabelHTML("testcase", "Status", "Status"));%></div>
                                <div style="clear:both">
                                    <%
                                        options.clear();
                                        for (Invariant statusInv : invariantTCStatus) {
                                            options.put(statusInv.getValue(), statusInv.getValue());
                                        }
                                    %>
                                    <%=generateMultiSelect("Status", request.getParameterValues("Status"), options,
                                            "Select an option", "Select Status", "# of # Status selected", 1, true)%>
                                </div>
                            </div>
                            <div style="float:left">
                                <div style="clear:both; width:150px; text-align: left"><%out.print(docService.findLabelHTML("invariant", "GROUP", "Group"));%></div>
                                <div style="clear:both">
                                    <%
                                        options.clear();
                                        ResultSet rsGroup = stmt.executeQuery("SELECT value from invariant where idname = 'GROUP' order by sort");
                                        while (rsGroup.next()) {
                                            if (rsGroup.getString(1) != null && !"".equals(rsGroup.getString(1).trim())) {
                                                options.put(rsGroup.getString(1), rsGroup.getString(1));
                                            }
                                        }
                                    %>
                                    <%=generateMultiSelect("Group", request.getParameterValues("Group"), options,
                                            "Select a Group", "Select Group", "# of # Group selected", 1, true)%> 
                                </div>
                            </div>
                            <div style="float:left">
                                <div style="clear:both; width:150px; text-align: left"><%out.print(docService.findLabelHTML("testcase", "targetBuild", "targetBuild"));%></div>
                                <div style="clear:both">
                                    <%
                                        options.clear();
                                        options.put("NTB", "-- No Target Build --");
                                        ResultSet rsTargetBuild = stmt.executeQuery("SELECT value from invariant where idname = 'BUILD' order by sort");
                                        while (rsTargetBuild.next()) {
                                            if (rsTargetBuild.getString(1) != null && !"".equals(rsTargetBuild.getString(1).trim())) {
                                                options.put(rsTargetBuild.getString(1), rsTargetBuild.getString(1));
                                            }
                                        }
                                    %>
                                    <%=generateMultiSelect("TargetBuild", request.getParameterValues("TargetBuild"), options,
                                            "Select a Target Build", "Select Target Build", "# of # Target Build selected", 1, true)%> 
                                </div>
                            </div>
                            <div style="float:left">
                                <div style="clear:both; width:150px; text-align: left"><%out.print(docService.findLabelHTML("testcase", "targetRev", "targetRev"));%></div>
                                <div style="clear:both">
                                    <%
                                        options.clear();
                                        options.put("NTR", "-- No Target Rev --");
                                        ResultSet rsTargetRev = stmt.executeQuery("SELECT value from invariant where idname = 'REVISION' order by sort");
                                        while (rsTargetRev.next()) {
                                            if (rsTargetRev.getString(1) != null && !"".equals(rsTargetRev.getString(1).trim())) {
                                                options.put(rsTargetRev.getString(1), rsTargetRev.getString(1));
                                            }
                                        }
                                    %>
                                    <%=generateMultiSelect("TargetRev", request.getParameterValues("TargetRev"), options,
                                            "Select a Target Rev", "Select Target Rev", "# of # Target Rev selected", 1, true)%> 
                                </div>
                            </div>
                            <div style="float:left">
                                <div style="clear:both; width:150px; text-align: left"><%out.print(docService.findLabelHTML("testcase", "creator", "Creator"));%></div>
                                <div style="clear:both">
                                    <%
                                        options.clear();
                                        ResultSet rsCreator = stmt.executeQuery("SELECT login from user");
                                        while (rsCreator.next()) {
                                            if (rsCreator.getString(1) != null && !"".equals(rsCreator.getString(1).trim())) {
                                                options.put(rsCreator.getString(1), rsCreator.getString(1));
                                            }
                                        }
                                    %>
                                    <%=generateMultiSelect("Creator", request.getParameterValues("Creator"), options,
                                            "Select a Creator", "Select Creator", "# of # Creator selected", 1, true)%> 
                                </div>
                            </div>
                            <div style="float:left">
                                <div style="clear:both; width:150px; text-align: left"><%out.print(docService.findLabelHTML("testcase", "implementer", "implementer"));%></div>
                                <div style="clear:both">
                                    <%=generateMultiSelect("Implementer", request.getParameterValues("Implementer"), options,
                                            "Select an Implementer", "Select Implementer", "# of # Implementer selected", 1, true)%> 
                                </div>
                            </div>
                        </div>
                           
                <div id="displayResult">
                    <br>
                    <br>
                    <br>
                    <br>
                    <br>
                    <br>
                </div>
                <%                    } catch (Exception e) {
                        out.println(e);
                    } finally {
                        try {
                            conn.close();
                        } catch (Exception ex) {
                        }
                    }

                %>
                
            </div>
        <!--/form-->

        <script type="text/javascript">
            $(document).ready(function() {
                $(".multiSelectOptions").each(function() {
                    var currentElement = $(this);
                    currentElement.multiselect({
                        multiple: true,
                        minWidth: 150,
                        header: currentElement.data('header'),
                        noneSelectedText: currentElement.data('none-selected-text'),
                        selectedText: currentElement.data('selected-text'),
                        selectedList: currentElement.data('selected-list')
                    });
                });
            });
        </script>

        <script type="text/javascript">
            $(document).ready(function() {

                // prepare all forms for ajax submission
                $('#Apply').on('submit', function(e) {
                    $('#displayResult').html('<img src="./images/loading.gif"> loading...');
                    e.preventDefault(); // <-- important
                    $(this).ajaxSubmit({
                        target: '#displayResult'
                    });
                });

            <%                    if ("Apply".equals(request.getParameter("Apply"))) {
            %>
                $('#Apply').submit();
            <%
                }
            %>

                

            });
        </script>

        
