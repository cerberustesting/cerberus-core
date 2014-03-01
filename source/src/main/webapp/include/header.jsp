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
<%@page import="org.cerberus.service.IInvariantService"%>
<%@page import="org.cerberus.service.IUserService"%>
<%@page import="org.springframework.web.context.support.WebApplicationContextUtils"%>
<%@page import="org.springframework.context.ApplicationContext"%>
<%@page import="org.cerberus.entity.User"%>
<%@page import="org.cerberus.util.ParameterParserUtil"%>
<%@page import="org.cerberus.refactor.Country"%>
<%@page import="java.util.Collection"%>
<%@page import="java.util.Iterator"%>
<%@page import="com.mysql.jdbc.ResultSetImpl"%>
<%@page import="java.sql.SQLException"%>
<%@page import="java.sql.Connection"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>
<%@page import="java.sql.ResultSet"%>
<%@page import="java.sql.Statement"%>
<%@page import="java.util.Date"%>
<%@page import="java.text.DateFormat"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="org.cerberus.version.Version"%>
<script type='text/javascript' src='js/Form.js'></script>

<div id="menu">
    <table >
        <tr>
            <td id="wob" rowspan="2">
                <a href="Homepage"><img src="images/logo-1.png" valign="Top" alt="Cerberus" /></a>
            </td>
            <td id="wob" style="width: 100%;">
            </td>
        </tr>
        <tr>
            <td id="wob" valign="Bottom">
                <div id="navcontainer">
                    <ul id="navlist">
                        <% if (request.getUserPrincipal() != null && (request.isUserInRole("TestRO"))) {%>
                        <li id="active"><a id="current" name="menu" href="#" style="width:100px">Test</a>
                            <ul class="subnav" id="subnavlist">
                                <li id="subactive"><a name="menu" id="menuEditTest" href="Test.jsp" style="width:130px">Edit Test</a></li>
                                <% if (request.getUserPrincipal() != null && (request.isUserInRole("Test"))) {%>
                                <li id="subactive"><a name="menu" id="menuCreateTest" href="TestCreate.jsp" style="width:130px">Create Test</a></li>
                                <% }%>
                            </ul>
                        </li>
                        <li id="active"><a id="current" name="menu" href="#" style="width:100px">TestCase</a>
                            <ul class="subnav" id="subnavlist">
                                <li id="subactive"><a name="menu" id="menuEditTestCase" href="TestCase.jsp" style="width:130px">Edit TestCase</a></li>
                                <% if (request.getUserPrincipal() != null && (request.isUserInRole("Test"))) {%>
                                <li id="subactive"><a name="menu" id="menuCreateTestCase" href="TestCaseCreate.jsp" style="width:130px">Create TestCase</a></li>
                                <% }%>
                                <li id="subactive"><a name="menu" id="menuSearchTestCase" href="TestCaseSearch.jsp" style="width:130px">Search TestCase</a></li>
                            </ul>
                        </li>
                        <% }%>
                        <% if (request.getUserPrincipal() != null && (request.isUserInRole("Test"))) {%>
                        <li id="active"><a id="current" name="menu" href="#" style="width:100px">Data</a>
                            <ul class="subnav" id="subnavlist">
                                <li id="subactive"><a name="menu" id="menuSqlLibrary" href="SqlLibrary.jsp" style="width:130px">SQL Library</a></li>
                                <li id="subactive"><a name="menu" id="menuTestData" href="TestData.jsp" style="width:130px">Test Data</a></li>
                            </ul>
                        </li>
                        <% }%>
                        <%  if (request.getUserPrincipal() != null && (request.isUserInRole("RunTest"))) {%>
                        <li id="active"><a id="current" name="menu" href="#" style="width:100px">Run</a>
                            <ul class="subnav" id="subnavlist">
                                <li id="subactive"><a name="menu" id="menuRunTestCase" href="RunTests.jsp" style="width:130px">Run Tests</a></li>
                                <%--                              <li><a name="menu" id="menuResumeTestCase" href="ResumeTests.jsp" style="width:130px">Resume Tests</a></li> --%>
                                <li id="subactive"><a name="menu" id="menuRunManualTestCase" href="RunManualTestCase.jsp" style="width:130px">Run Manual Tests</a></li>
                            </ul>
                        </li>
                        <% }%>
                        <% if (request.getUserPrincipal() != null && (request.isUserInRole("TestRO"))) {%>
                        <li id="active"><a id="current" name="menu" href="#" style="width:100px">Execution<br>Reporting</a>
                            <ul class="subnav" id="subnavlist">
                                <li id="subactive"><a name="menu" id="menuExecutionPerBuildRevision" href="ExecutionPerBuildRevision" style="width:150px">Execution Per Build/Rev</a></li>
                                <li id="subactive"><a name="menu" id="menuReportingExecutionStatus" href="ReportingExecution.jsp" style="width:150px">Execution Status</a></li>
                                <li id="subactive"><a name="menu" id="menuReportingExecutionTime" href="ReportingExecutionTime.jsp" style="width:150px">Execution Time</a></li>
                                <li id="subactive"><a name="menu" id="menuReportingExecutionDetail" href="ExecutionDetailList.jsp" style="width:150px">Execution Detail</a></li>
                            </ul>
                        </li>
                        <% }%>
                        <% if (request.getUserPrincipal() != null && request.isUserInRole("IntegratorRO")) {%>
                        <li id="active"><a id="current" name="menu" href="#" style="width:100px">Integration</a>
                            <ul class="subnav" id="subnavlist">
                                <li id="subactive"><a name="menu" id="menuIntegrationStatus" href="IntegrationStatus.jsp" style="width:130px">Integration Status</a></li>
                                <li id="subactive"><a name="menu" id="menuApplications" href="Application.jsp" style="width:130px">Applications</a></li>
                                <li id="subactive"><a name="menu" id="menuEnvironments" href="EnvironmentList.jsp" style="width:130px">Environments</a></li>
                                <li id="subactive"><a name="menu" id="menuBuildRevision" href="BuildRevDefinition.jsp" style="width:130px">Build/Rev Definition</a></li>
                                <li id="subactive"><a name="menu" id="menuBuildContent" href="BuildContent.jsp" style="width:130px">Build Content</a></li>
                            </ul>
                        </li>
                        <% }%>
                        <% if (request.getUserPrincipal() != null && request.isUserInRole("Administrator")) {%>
                        <li id="active"><a id="current" name="menuAdmin" href="#" style="width:100px">Admin</a>
                            <ul class="subnav" id="subnavlist">
                                <li id="subactive"><a name="menu" id="menuUsersManager" href="UserManager.jsp" style="width:130px">Users Manager</a></li>
                                <li id="subactive"><a name="menu" id="menuLogViewer" href="LogViewer.jsp" style="width:130px">Log Viewer</a></li>
                                <li id="subactive"><a name="menu" id="menuDatabaseMaintenance" href="DatabaseMaintenance.jsp" style="width:130px">Database Maintenance</a></li>
                                <li id="subactive"><a name="menu" id="menuParameter" href="Parameter.jsp" style="width:130px">Parameters</a></li>
                            </ul>
                        </li>
                        <% }%>
                    </ul></div>
	    </td>
            <% if (request.getUserPrincipal() != null) {%>
            <td class="loginIcon">
                <h6>Welcome</h6>
                <p style="text-align: right" ><%= request.getUserPrincipal().getName()%></p>
                <form action="" method="post" name="SysFilter" id="SysFilter">
                    <%
                        String MySystem = ParameterParserUtil.parseStringParam(request.getParameter("MySystem"), "");

                        String MyUser = ParameterParserUtil.parseStringParam(request.getUserPrincipal().getName(), "");
                        ApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(this.getServletConfig().getServletContext());
                        IUserService myUserService = context.getBean(IUserService.class);
                        if (!(MyUser.equals(""))) {
                            User MyUserobj = myUserService.findUserByKey(MyUser);

                            // Update MyDefaultSystem if different from user.
                            if (MySystem.equals("")) {
                                MySystem = MyUserobj.getDefaultSystem();
                            } else {
                                if (!(MyUserobj.getDefaultSystem().equals(MySystem))) {
                                    MyUserobj.setDefaultSystem(MySystem);
                                    myUserService.updateUser(MyUserobj);
                                }
                            }
                        }
                        request.setAttribute("MySystem", MySystem);
                    %>                
                    <select id="MySystem" style="" name="MySystem" onchange="document.SysFilter.submit()">
                        <%
                            IInvariantService myInvariantService = context.getBean(IInvariantService.class);
                            List<Invariant> MyInvariantList = myInvariantService.findListOfInvariantById("SYSTEM");
                            for (Invariant myInvariant : MyInvariantList) {
                        %>
                        <option value="<%=myInvariant.getValue()%>"<% if (MySystem.equalsIgnoreCase(myInvariant.getValue())) {
                                out.print(" SELECTED");
                            }%>><%=myInvariant.getValue()%></option><%
                                }
                        %>
                    </select>
                </form>
                <a href="Logout.jsp">
                    <div id="logout" style="width: 75px; padding-left: 25px">
                        <img src="images/logout.png">
                        <span>logout</span>
                    </div>
                </a>
            </td>
            <% } else {%>
            <td class="loginIcon">
                <a href="Homepage">
                    <img src="images/LoginIcon.png" />
                    <span>Login</span>
                </a>
            </td>
            <% }%>
        </tr>
    </table>
</div>


<script type="text/javascript">
    menuColoring(null);
</script>

<script type="text/javascript">
    EnvTuning("<%=System.getProperty("org.cerberus.environment")%>");
</script>