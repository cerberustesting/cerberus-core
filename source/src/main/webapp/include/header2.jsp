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

<%@page import="java.util.Set"%>
<%@page import="org.cerberus.entity.UserSystem"%>
<%@page import="org.apache.log4j.Level"%>
<%@page import="org.cerberus.log.MyLogger"%>
<%@page import="org.cerberus.service.IDatabaseVersioningService"%>
<%@page import="org.cerberus.entity.Invariant"%>
<%@page import="org.cerberus.service.IInvariantService"%>
<%@page import="org.cerberus.service.IUserService"%>
<%@page import="org.springframework.web.context.support.WebApplicationContextUtils"%>
<%@page import="org.springframework.context.ApplicationContext"%>
<%@page import="org.cerberus.entity.User"%>
<%@page import="org.cerberus.util.ParameterParserUtil"%>
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
<%@page import="org.cerberus.version.Infos"%>
<script type='text/javascript' src='js/Form.js'></script>


<div role="navigation" class="navbar navbar-inverse navbar-static-top">
    <div class="container">
        <div class="navbar-header navbar-left">
            <a class="navbar-brand" href="Homepage.jsp">
                <img id="logo" alt="Cerberus" src="images/logo.png"/>
            </a>
        </div>
        <!-- button added for responsivness of the header-->
         <span class="sr-only">Toggle navigation</span>  
        <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse">
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
        </button>
        <div id="navbar" class="collapse navbar-collapse">
            <ul class="nav navbar-nav" id="navlist">
                <% if (request.getUserPrincipal() != null && (request.isUserInRole("TestRO"))) {%>
                <li class="dropdown"><a id ="menu-Test" class="dropdown-toggle" aria-expanded="true" aria-haspopup="true" role="button" data-toggle="dropdown" href="#">Test <span class="caret"></span></a>
                    <ul class="dropdown-menu" id="subnavlist">
                        <li id="subactive"><a name="menu" id="menuEditTest" href="Test.jsp">Edit Test</a></li>
                            <% if (request.getUserPrincipal() != null && (request.isUserInRole("TestAdmin"))) {%>
                        <li id="subactive"><a name="menu" id="menuCreateTest" href="TestCreate.jsp">Create Test</a></li>
                            <% }%>
                        <li id="subactive"><a name="menu" id="menuTestPerApplication" href="TestPerApplication.jsp">Test Per Application</a></li>
                        <li id="subactive"><a name="menu" id="menuTestCampaign" href="TestCampaign.jsp">Test Campaign</a></li>
                    </ul>
                </li>
                <li class="dropdown"><a id="menu-TestCase" class="dropdown-toggle" aria-expanded="true" aria-haspopup="true" role="button" data-toggle="dropdown" href="#">TestCase <span class="caret"></span></a>
                    <ul class="dropdown-menu" id="subnavlist">
                        <li id="subactive"><a name="menu" id="menuEditTestCase" href="TestCase.jsp">Edit TestCase</a></li>
                            <% if (request.getUserPrincipal() != null && (request.isUserInRole("Test"))) {%>
                        <li id="subactive"><a name="menu" id="menuCreateTestCase" href="TestCaseCreate.jsp">Create TestCase</a></li>
                            <% }%>
                        <li id="subactive"><a name="menu" id="menuSearchTestCase" href="TestCaseSearch.jsp">Search TestCase</a></li>
                        <li id="subactive"><a name="menu" id="menuTestBattery" href="TestBattery.jsp">Test Battery</a></li>
                    </ul>
                </li>
                <% }%>
                <% if (request.getUserPrincipal() != null && (request.isUserInRole("Test"))) {%>
                <li class="dropdown"><a id="menu-Data" class="dropdown-toggle" aria-expanded="true" aria-haspopup="true" role="button" data-toggle="dropdown" href="#">Data <span class="caret"></span></a>
                    <ul class="dropdown-menu" id="subnavlist">
                        <li id="subactive"><a name="menu" id="menuSqlLibrary" href="SqlLibrary.jsp">SQL Library</a></li>
                        <li id="subactive"><a name="menu" id="menuSoapLibrary" href="SoapLibrary.jsp">SOAP Library</a></li>
                        <li id="subactive"><a name="menu" id="menuTestData" href="TestData.jsp">Test Data</a></li>
                        <li id="subactive"><a name="menu" id="menuTestDataLib" href="TestDataLib.jsp">Library</a></li>
                    </ul>
                </li>
                <% }%>
                <%  if (request.getUserPrincipal() != null && (request.isUserInRole("RunTest"))) {%>
                <li class="dropdown"><a id="menu-Run" class="dropdown-toggle" aria-expanded="true" aria-haspopup="true" role="button" data-toggle="dropdown" href="#">Run <span class="caret"></span></a>
                    <ul class="dropdown-menu" id="subnavlist">
                        <li id="subactive"><a name="menu" id="menuRunTestCase" href="RunTests.jsp">Run Tests</a></li>
                            <%--  <li><a name="menu" id="menuResumeTestCase" href="ResumeTests.jsp" style="width:130px">Resume Tests</a></li> --%>
                        <li id="subactive"><a name="menu" id="menuRunManualTestCase" href="RunManualTestCase.jsp">Run Manual Tests</a></li>
                        <li id="subactive"><a name="menu" id="menuRunTestTriggerBatchExecution" href="RunTestsTriggerBatchExecution.jsp">Run Multiple Tests</a></li>
                        <li id="subactive"><a name="menu" id="menuRunTestSeePendingExecution" href="ExecutionPending.jsp">See Execution In Queue</a></li>
                    </ul>
                </li>
                <% }%>
                <% if (request.getUserPrincipal() != null && (request.isUserInRole("TestRO"))) {%>
                <li class="dropdown"><a id="menu-ExecutionReport" class="dropdown-toggle" aria-expanded="true" aria-haspopup="true" role="button" data-toggle="dropdown" href="#">Execution Reporting <span class="caret"></span></a>
                    <ul class="dropdown-menu" id="subnavlist">
                        <li id="subactive"><a name="menu" id="menuExecutionPerBuildRevision" href="ExecutionPerBuildRevision">Execution Per Build/Rev</a></li>
                        <li id="subactive"><a name="menu" id="menuReportingExecutionStatus" href="ReportingExecution.jsp">Execution Status</a></li>
                        <li id="subactive"><a name="menu" id="menuReportingExecutionTime" href="ReportingExecutionTime.jsp">Execution Time</a></li>
                        <li id="subactive"><a name="menu" id="menuReportingExecutionDetail" href="ExecutionDetailList.jsp">Execution Detail</a></li>
                        <li id="subactive"><a name="menu" id="menuReportingExecutionByTag" href="ReportingExecutionByTag.jsp">Execution Report By Tag</a></li>
                    </ul>
                </li>
                <% }%>
                <% if (request.getUserPrincipal() != null && request.isUserInRole("IntegratorRO")) {%>
                <li class="dropdown"><a id="menu-Integration" class="dropdown-toggle" aria-expanded="true" aria-haspopup="true" role="button" data-toggle="dropdown" href="#">Integration <span class="caret"></span></a>
                    <ul class="dropdown-menu" id="subnavlist">
                        <li id="subactive"><a name="menu" id="menuIntegrationStatus" href="IntegrationStatus.jsp">Integration Status</a></li>
                        <li id="subactive"><a name="menu" id="menuApplications" href="Application.jsp">Applications</a></li>
                        <li id="subactive"><a name="menu" id="menuEnvironments" href="EnvironmentList.jsp">Environments</a></li>
                        <li id="subactive"><a name="menu" id="menuEnvironmentManagement" href="EnvironmentManagement.jsp">Environment Management</a></li>
                        <li id="subactive"><a name="menu" id="menuBuildRevision" href="BuildRevDefinition.jsp">Build/Rev Definition</a></li>
                        <li id="subactive"><a name="menu" id="menuBuildContent" href="BuildContent.jsp">Build Content</a></li>
                        <li id="subactive"><a name="menu" id="menuRobot" href="Robot.jsp">Robot</a></li>
                        <li id="subactive"><a name="menu" id="menuProject" href="Project.jsp">Project</a></li>
                    </ul>
                </li>
                <% }%>
                <% if (request.getUserPrincipal() != null && request.isUserInRole("Administrator")) {%>
                <li class="dropdown"><a id="menu-Admin" class="dropdown-toggle" aria-expanded="true" aria-haspopup="true" role="button" data-toggle="dropdown" href="#">Admin <span class="caret"></span></a>
                    <ul class="dropdown-menu" id="subnavlist">
                        <li id="subactive"><a name="menu" id="menuUsersManager" href="UserManager.jsp">Users Manager</a></li>
                        <li id="subactive"><a name="menu" id="menuLogViewer" href="LogViewer.jsp">Log Viewer</a></li>
                        <li id="subactive"><a name="menu" id="menuDatabaseMaintenance" href="DatabaseMaintenance.jsp">Database Maintenance</a></li>
                        <li id="subactive"><a name="menu" id="menuParameter" href="Parameter.jsp">Parameters</a></li>
                        <li id="subactive"><a name="menu" id="menuInvariantPublic" href="InvariantPublic.jsp">Edit Public Invariants</a></li>
                        <li id="subactive"><a name="menu" id="menuInvariantPrivate" href="InvariantPrivate.jsp">See Private Invariants</a></li>
                        <li id="subactive"><a name="menu" id="menuReportingExecutionThreadMonitoring" href="ExecutionThreadMonitoring.jsp">Monitoring</a></li>
                    </ul>
                </li>
                <% }%>
            </ul>

            <ul class="nav navbar-nav navbar-right">
                <% if (request.getUserPrincipal() != null) {%>
                <li>
                    <a id="formSystem">
                        <!--<p style="color:white">System</p>-->
                        <span class=" glyphicon glyphicon-folder-open" style="color: white; margin-right: 5px;"></span>
                        <form style="display: inline-block; margin-left: 5px;" action="" method="post" name="SysFilter" id="SysFilter">
                            <%
                                String MySystem = ParameterParserUtil.parseStringParam(request.getParameter("MySystem"), "");

                                String MyUser = ParameterParserUtil.parseStringParam(request.getUserPrincipal().getName(), "");
                                ApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(this.getServletConfig().getServletContext());
                                IUserService myUserService = context.getBean(IUserService.class);
                                IDatabaseVersioningService DatabaseVersioningService = context.getBean(IDatabaseVersioningService.class);
                                // We access and update the user object only if database is uptodate. This is to prenvent 500 error 
                                //   when adding new column on user table and trying to access this column even if it does not exist yet.
                                //   Than means that system cannot be saved until database has been updated by administrator.
                                if (DatabaseVersioningService.isDatabaseUptodate()) {
                                    if (!(MyUser.equals(""))) {

                                        // We load the user object.
                                        User MyUserobj = myUserService.findUserByKeyWithDependencies(MyUser);

                                        // If we are not already in changepassword page and user needs to change its password,
                                        //    --> we redirect to Change Password page.
                                        if (!(request.getRequestURI().contains("ChangePassword.jsp"))) {
                                            if (MyUserobj.getRequest().equalsIgnoreCase("Y")) {
                                                request.getRequestDispatcher("/ChangePassword.jsp").forward(request, response);
                                            }
                                        }

                                        // Update MyDefaultSystem if different from user.
                                        if (MySystem.equals("")) {
                                            MySystem = MyUserobj.getDefaultSystem();
                                        } else {
                                            if (!(MyUserobj.getDefaultSystem().equals(MySystem))) {
                                                List<String> systems = new ArrayList<String>();
                                                for (UserSystem us : MyUserobj.getUserSystems()) {
                                                    systems.add(us.getSystem());
                                                }
                                                if (!systems.contains(MySystem)) {
                            %>
                            <script>alert("You're not allowed to navigate on this part !\n\nPlease, contact your Cerberus Administrator to modify your account permission.\n\nYou'll be redirected to your Default System.");
                                location.href = location;
                            </script><%
                                                } else {

                                                    MyUserobj.setDefaultSystem(MySystem);
                                                    myUserService.updateUser(MyUserobj);
                                                }
                                            }
                                        }

                                    }
                                }

                                request.setAttribute("MySystem", MySystem);
                            %>                
                            <select class="form-control" id="MySystem" style="" name="MySystem" onchange="document.SysFilter.submit()">
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
                    </a>
                </li>
                <li class="dropdown">
                    <a id="userInfo" class="dropdown-toggle" aria-expanded="true" aria-haspopup="true" role="button" data-toggle="dropdown" href="#">
                        <span class="glyphicon glyphicon-user" style="color: white"></span>
                        <p><%= request.getUserPrincipal().getName()%></p><span class="caret"></span></a>
                    <ul class="dropdown-menu" id="subnavlist">
                        <li id="subactive">
                            <a id="Logout" href="Logout.jsp">
                                <div id="logout">
                                    <img src="images/logout.png"/>
                                    <span>logout</span>
                                </div>
                            </a>
                        </li>
                    </ul>
                </li>
                <% } else {%>
                <li id="userInfo" style="float:right; width:100px">
                    <a href="Homepage.jsp">
                        <div id="login" style="width: 75px;">
                            <img src="images/LoginIcon1.png" />
                            <span>Login</span>
                        </div>
                    </a>
                    <% }%>
                </li>
            </ul>  
        </div>
    </div>
</div>
<!--    <div style="clear:both">
        <p class="dttTitle">Cerberus : Framework for Automated Testing</p>
    </div>-->
<br><br>
<script type="text/javascript">
    menuColoring(null);
</script>

<script type="text/javascript">
    EnvTuning("<%=System.getProperty("org.cerberus.environment")%>");
</script>
