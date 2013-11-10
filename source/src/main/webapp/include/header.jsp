<%-- 

Document   : menu
    Created on : 10 déc. 2010, 11:43:27
    Author     : acraske
--%>
<%@page import="com.redcats.tst.entity.Invariant"%>
<%@page import="com.redcats.tst.service.IInvariantService"%>
<%@page import="com.redcats.tst.service.IUserService"%>
<%@page import="org.springframework.web.context.support.WebApplicationContextUtils"%>
<%@page import="org.springframework.context.ApplicationContext"%>
<%@page import="com.redcats.tst.entity.User"%>
<%@page import="com.redcats.tst.util.ParameterParserUtil"%>
<%@page import="com.redcats.tst.refactor.Country"%>
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
<%@page import="version.Version"%>
<script type='text/javascript' src='js/Form.js'></script>

<div id="menu">
    <table >
        <tr>
            <td id="wob" rowspan="2">
                <a href="Homepage"><img src="images/cerberus.png" valign="Top" alt="Cerberus" /></a>
            </td>
            <td id="wob" style="width: 100%;">
            </td>
        </tr>
        <tr>
            <td id="wob" valign="Bottom">
                <div id="navcontainer">
                    <ul id="navlist">
                        <% if (request.getUserPrincipal() != null && (request.isUserInRole("User"))) {%>
                        <li id="active"><a id="current" name="menu" href="#" style="width:130px">Test</a>
                            <ul id="subnavlist">
                                <li id="subactive"><a name="menu" id="menuEditTest" href="Test.jsp" style="width:130px">Edit Test</a></li>
                                <li id="subactive"><a name="menu" id="menuCreateTest" href="TestCreate.jsp" style="width:130px">Create Test</a></li>
                            </ul>
                        </li>
                        <% }%>
                        <li id="active"><a id="current" name="menu" href="#" style="width:130px">TestCase</a>
                            <ul id="subnavlist">
                                <li id="subactive"><a name="menu" id="menuEditTestCase" href="TestCase.jsp" style="width:130px">Edit TestCase</a></li>
                                <li id="subactive"><a name="menu" id="menuCreateTestCase" href="TestCaseCreate.jsp" style="width:130px">Create TestCase</a></li>
                                <li id="subactive"><a name="menu" id="menuSearchTestCase" href="TestCaseSearch.jsp" style="width:130px">Search TestCase</a></li>
                            </ul>
                        </li>
                        <%  if (request.getUserPrincipal() != null && (request.isUserInRole("User"))) {%>
                        <li id="active"><a id="current" name="menu" href="#" style="width:130px">Run</a>
                            <ul id="subnavlist">
                                <li id="subactive"><a name="menu" id="menuRunTestCase" href="RunTests.jsp" style="width:130px">Run Tests</a></li>
                                <%--                              <li><a name="menu" id="menuResumeTestCase" href="ResumeTests.jsp" style="width:130px">Resume Tests</a></li> --%>
                            </ul>
                        </li>
                        <% }%>
                        <li id="active"><a id="current" name="menu" href="#" style="width:130px">Execution Reporting</a>
                            <ul id="subnavlist">
                                <li id="subactive"><a name="menu" id="menuExecutionPerBuildRevision" href="ExecutionPerBuildRevision" style="width:130px">Execution Per Build/Rev</a></li>
                                <li id="subactive"><a name="menu" id="menuReportingExecutionStatus" href="ReportingExecution.jsp" style="width:130px">Execution Status</a></li>
                                <li id="subactive"><a name="menu" id="menuReportingExecutionTime" href="ReportingExecutionTime.jsp" style="width:130px">Execution Time</a></li>
                                <li id="subactive"><a name="menu" id="menuReportingExecutionDetail" href="ExecutionDetailList.jsp" style="width:130px">Execution Detail</a></li>
                            </ul>
                        </li>
                        <% if (request.getUserPrincipal() != null && request.isUserInRole("Integrator")) {%>
                        <li id="active"><a id="current" name="menuIntegration" href="#" style="width:130px">Integration</a>
                            <ul id="subnavlist">
                                <li id="subactive"><a name="menu" id="menuIntegrationStatus" href="IntegrationStatus.jsp" style="width:130px">Integration Status</a></li>
                                <li id="subactive"><a name="menu" id="menuApplications" href="Application.jsp" style="width:130px">Applications</a></li>
                                <li id="subactive"><a name="menu" id="menuEnvironments" href="EnvironmentList.jsp" style="width:130px">Environments</a></li>
                                <li id="subactive"><a name="menu" id="menuBuildRevision" href="BuildRevDefinition.jsp" style="width:130px">Build/Rev Definition</a></li>
                                <li id="subactive"><a name="menu" id="menuBuildContent" href="BuildContent.jsp" style="width:130px">Build Content</a></li>
                            </ul>
                        </li>
                        <% }%>
                        <% if (request.getUserPrincipal() != null && request.isUserInRole("Admin")) {%>
                        <li id="active"><a id="current" name="menuAdmin" href="#" style="width:130px">Admin</a>
                            <ul id="subnavlist">
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
                <br/>
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