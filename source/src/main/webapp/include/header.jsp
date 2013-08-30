<%-- 

Document   : menu
    Created on : 10 déc. 2010, 11:43:27
    Author     : acraske
--%>

<%@page import="com.redcats.tst.util.ParameterParserUtil"%>
<%@page import="com.redcats.tst.refactor.Country"%>
<%@page import="com.redcats.tst.refactor.DbMysqlController"%>
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
                        <% if (request.getUserPrincipal() != null && !(request.isUserInRole("Visitor"))) {%>
                        <li id="active"><a id="current" name="menu" href="#" style="width:130px">Test</a>
                            <ul id="subnavlist">
                                <li id="subactive"> <a name="menu" id="menuEditTest" href="Test.jsp" style="width:130px">Edit Test</a></li>
                                <li><a name="menu" id="menuCreateTest" href="TestCreate.jsp" style="width:130px">Create Test</a></li>
                            </ul>
                        </li>
                        <% }%>
                        <li id="active"><a id="current" name="menu" href="#" style="width:130px">TestCase</a>
                            <ul id="subnavlist">
                                <li id="subactive"><a name="menu" id="menuEditTestCase" href="TestCase.jsp" href="#" style="width:130px">Edit TestCase</a></li>
                                <li><a name="menu" id="menuCreateTestCase" href="TestCaseCreate.jsp" style="width:130px">Create TestCase</a></li>
                                <li><a name="menu" id="menuSearchTestCase" href="TestCaseSearch.jsp" style="width:130px">Search TestCase</a></li>
                            </ul>
                        </li>
                        <%  if (request.getUserPrincipal() != null && !(request.isUserInRole("Visitor"))) {%>
                        <li id="active"><a id="current" name="menu" href="#" style="width:130px">Run</a>
                            <ul id="subnavlist">
                                <li id="subactive"><a name="menu" id="menuRunTestCase" href="RunTests.jsp" style="width:130px">Run Tests</a></li>
                                <li><a name="menu" id="menuResumeTestCase" href="ResumeTests.jsp" style="width:130px">Resume Tests</a></li>
                            </ul>
                        </li>
                        <% }%>
                        <li id="active"><a id="current" name="menu" href="#" style="width:130px">Execution Reporting</a>
                            <ul id="subnavlist">
                                <li id="subactive"><a name="menu" id="menuReportingExecutionStatus" href="ReportingExecution.jsp" style="width:130px">Execution Status</a></li>
                                <li><a name="menu" id="menuReportingExecutionTime" href="ReportingExecutionTime.jsp" style="width:130px">Execution Time</a></li>
                                <li><a name="menu" id="menuReportingExecutionDetail" href="ExecutionDetailList.jsp" style="width:130px">Execution Detail</a></li>
                            </ul>
                        </li>
                        <% if (request.getUserPrincipal() != null && request.isUserInRole("Integrator")) {%>
                        <li id="active"><a id="current" name="menuIntegration" href="#" style="width:130px">Integration</a>
                            <ul id="subnavlist">
                                <li id="subactive"><a name="menu" id="menuApplications" href="Application.jsp" style="width:130px">Applications</a></li>
                                <li id="subactive"><a name="menu" id="menuEnvironments" href="EnvironmentList.jsp" style="width:130px">Environments</a></li>
                                <li id="subactive"><a name="menu" id="menuSprintContent" href="SprintContent.jsp" style="width:130px">Sprint Content</a></li>
                            </ul>
                        </li>
                        <% }%>
                        <% if (request.getUserPrincipal() != null && request.isUserInRole("Admin")) {%>
                        <li id="active"><a id="current" name="menuAdmin" href="#" style="width:130px">Admin</a>
                            <ul id="subnavlist">
                                <li id="subactive"><a name="menu" id="menuUsersManager" href="UserManager.jsp" style="width:130px">Users Manager</a></li>
                                <li><a name="menu" id="menuLogViewer" href="LogViewer.jsp" style="width:130px">Log Viewer</a></li>
                                <li><a name="menu" id="menuDatabaseMaintenance" href="DatabaseMaintenance.jsp" style="width:130px">Database Maintenance</a></li>
                                <li><a name="menu" id="menuParameter" href="Parameter.jsp" style="width:130px">Parameters</a></li>
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
                        Connection conn = db.connect();
                        String MySystem=ParameterParserUtil.parseStringParam(request.getParameter("MySystem"), "");
                    %>                
                    <%=ComboInvariantAjax(conn, "MySystem", "", "MySystem", "4", "41", MySystem, "document.SysFilter.submit()", false)%>
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
