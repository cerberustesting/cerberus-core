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
<%@page import="java.sql.Connection"%>
<%@page import="java.sql.ResultSet"%>
<%@page import="java.sql.Statement"%>

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">
<% Date DatePageStart = new Date();%>
<html>
    <head>
        <meta content="text/html; charset=UTF-8" http-equiv="content-type">
        <title>TestCase Creation</title>
        <link rel="stylesheet" type="text/css" href="css/crb_style.css">
        <link rel="shortcut icon" type="image/x-icon" href="images/favicon.ico" />
        <script type="text/javascript" src="js/jquery-1.10.2.min.js"></script>
        <!--must import to use elRTE plugin-->
        <script type="text/javascript" src="js/jquery-migrate-1.2.1.min.js"></script>
        <script type="text/javascript" src="js/jquery-ui-1.10.2.custom.min.js"></script>
        <script type="text/javascript" src="js/elrte.min.js"></script>
        <script type="text/javascript" src="js/i18n/elrte.en.js"></script>
        <style media="screen" type="text/css">
            @import "css/smoothness/jquery-ui-1.10.2.custom.min.css";
            @import "css/elrte.min.css";
        </style>
        <link rel="stylesheet" type="text/css" href="css/crb_style.css">
        <link rel="stylesheet" type="text/css" href="css/elrte.min.css">

        <script type="text/javascript">
            $().ready(function() {
                elRTE.prototype.options.toolbars.cerberus = ['style', 'alignment', 'colors', 'format', 'indent', 'lists', 'links'];
                var opts = {
                    lang         : 'en',
                    styleWithCSS : false,
                    width        : 615,
                    height       : 200,
                    toolbar      : 'cerberus',
                    allowSource  : false,
                    cssfiles     : ['css/crb_style.css']
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
                Connection conn = db.connect();
                try {

                    String testselected;
                    if (request.getParameter("createTest") != null
                            && request.getParameter("createTest").compareTo("All") != 0) {
                        testselected = request.getParameter("createTest");
                    } else {
                        testselected = new String("%%");
                    }

                    Statement stmt30 = conn.createStatement();
                    Statement stQueryTestCase = conn.createStatement();
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
                                    <td class="wob" style="width: 300px"><%out.print(dbDocS(conn, "test", "test", "Test"));%></td>
                                </tr>
                                <tr>
                                    <td class="wob">
                                        <select id="createTest" name="createTest" style="width: 300px ; font-weight: bold " OnChange="document.testSelected.submit()">
                                            <%	if (testselected.compareTo("%%") == 0) {
                                            %><option style="width: 200px" value="All">-- Choose Test --</option>
                                            <%}
                                                ResultSet rsTestS = stmt30.executeQuery("SELECT DISTINCT Test, active FROM test where Test IS NOT NULL Order by Test asc");
                                                String optstyle = "";
                                                while (rsTestS.next()) {
                                                    if (rsTestS.getString("active").equalsIgnoreCase("Y")) {
                                                        optstyle = "font-weight:bold;";
                                                    } else {
                                                        optstyle = "font-weight:lighter;";
                                                    }
                                            %><option style="width: 200px;<%=optstyle%>" value="<%=rsTestS.getString("Test")%>" <%=testselected.compareTo(rsTestS.getString("Test")) == 0 ? " SELECTED " : ""%>><%=rsTestS.getString("Test")%></option>
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
                                                <td class="wob" style="width: 100px"><%out.print(dbDocS(conn, "testcase", "testcase", "TestCase"));%></td>
                                                <td class="wob" style="width: 100px"><%out.print(dbDocS(conn, "testcase", "Origine", "Origin"));%></td>
                                                <td class="wob" style="width: 100px"><%out.print(dbDocS(conn, "testcase", "RefOrigine", "RefOrigine"));%></td>
                                                <td class="wob" style="width: 100px; visibility:hidden"><%out.print(dbDocS(conn, "testcase", "Creator", "creator"));%></td>
                                                <td class="wob" style="width: 100px"><%out.print(dbDocS(conn, "project", "idproject", "Project"));%></td>
                                                <td class="wob" style="width: 100px"><%out.print(dbDocS(conn, "testcase", "ticket", "Ticket"));%></td>
                                                <td class="wob" style="width: 70px"><%out.print(dbDocS(conn, "testcase", "BugID", "BugID"));%></td>
                                            </tr>
                                            <%
                                                String tcnumber = "";
                                                if (testselected.compareTo("%%") == 0) {
                                                } else {
                                                    int testcasenumber = 0;


                                                    ResultSet rsTestCase = stQueryTestCase.executeQuery("SELECT  Max( Testcase ) + 1 as MAXTC FROM testcase where test = '"
                                                            + testselected + "'");
                                                    rsTestCase.first();
                                                    if (StringUtils.isNotBlank(rsTestCase.getString("MAXTC")) == true) {

                                                        testcasenumber = Integer.valueOf(rsTestCase.getString("MAXTC"));

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
                                                <td class="wob"><input id="createTestcase" name="createTestcase" style="width: 100px; font-weight: bold"value="<%=tcnumber%>">
                                                </td>
                                                <td class="wob">
                                                    <select id="createOrigine" style="width: 100px;" name="createOrigine">
                                                        <option value="All">-- Origin --</option>
                                                        <%              ResultSet rsOri = stmt30.executeQuery(" SELECT value from invariant where idname = 'ORIGIN'");
                                                            while (rsOri.next()) {%>
                                                        <option value="<%=rsOri.getString("value")%>"><%=rsOri.getString("value")%></option><%
                                                            }%>
                                                    </select>
                                                </td>
                                                <td class="wob"><input id="createRefOrigine" style="width: 90px;" name="createRefOrigine"></td>
                                                <td class="wob" style="visibility:hidden"><input id="createCreator" style="width: 90px;" name="createCreator"></td>
                                                <td class="wob">
                                                    <% out.print(ComboProject(conn, "createProject", "width: 90px", "createProject", "", "", "", false, "", ""));%>
                                                </td>
                                                <td class="wob"><input id="createTicket" style="width: 90px;" name="createTicket"></td>
                                                <td class="wob"><input id="createBugID" style="width: 70px;" name="createBugID"></td>
                                            </tr>
                                        </table>
                                    </td>
                                </tr>
                                <%
                                    Statement stmt36 = conn.createStatement();
                                    ResultSet rs_tccountgen = stmt36.executeQuery("SELECT value "
                                            + " FROM invariant "
                                            + " WHERE idname ='COUNTRY'"
                                            + " ORDER BY sort asc");%>

                                <tr>
                                    <td class="separation">
                                        <table style="text-align: left; border-collapse: collapse" border="0px" cellpadding="0px" cellspacing="0px">
                                            <tr>
                                                <td class="wob">
                                                    <table class="wob" style="text-align: left; border-collapse: collapse" border="0px" cellpadding="0px" cellspacing="0px">
                                                        <tr><td class="wob"><h4 style="color : blue">TestCase Parameters</h4></td></tr>
                                                        <tr id="header">
                                                            <td class="wob" style="width: 150px"><%out.print(dbDocS(conn, "application", "application", "Application"));%></td>
                                                            <td class="wob" style="width: 90px"><%out.print(dbDocS(conn, "testcase", "ActiveQA", "RunQA"));%></td>
                                                            <td class="wob" style="width: 90px"><%out.print(dbDocS(conn, "testcase", "ActiveUAT", "RunUAT"));%></td>
                                                            <td class="wob" style="width: 90px"><%out.print(dbDocS(conn, "testcase", "ActivePROD", "RunPROD"));%></td>
                                                            <td class="wob" style="width: 90px"><%out.print(dbDocS(conn, "invariant", "PRIORITY", "Priority"));%></td>
                                                            <td class="wob" style="width: 150px"><%out.print(dbDocS(conn, "testcase", "group", "Group"));%></td>
                                                            <td class="wob" style="width: 150px"><%out.print(dbDocS(conn, "testcase", "status", "Status"));%></td>
                                                            <%
                                                                rs_tccountgen.first();
                                                                do {%>
                                                            <td class="wob" style="font-size : x-small ; width: 20px; text-align: center"><%=rs_tccountgen.getString("value")%> <input type="hidden" name="testcase_country_all" value="<%=rs_tccountgen.getString("value")%>"></td>
                                                                <% 		} while (rs_tccountgen.next());
                                                                %>
                                                        </tr>
                                                        <tr>
                                                            <td class="wob"><select id="createApplication" name="createApplication" style="width: 140px"><%
                                                                ResultSet rsApp = stmt30.executeQuery(" SELECT distinct application from application where application != '' order by sort ");
                                                                while (rsApp.next()) {
                                                                    %><option value="<%=rsApp.getString("application")%>"><%=rsApp.getString("application")%></option><%
                                                                        }
                                                                    %></select></td>
                                                            <td class="wob"><%=ComboInvariant(conn, "createRunQA", "width: 75px", "createRunQA", "runqa", "26", "", "", null)%></td>
                                                            <td class="wob"><%=ComboInvariant(conn, "createRunUAT", "width: 75px", "createRunUAT", "runuat", "27", "", "", null)%></td>
                                                            <td class="wob"><%=ComboInvariant(conn, "createRunPROD", "width: 75px", "createRunPROD", "runprod", "28", "", "", null)%></td>
                                                            <td class="wob"><%=ComboInvariant(conn, "createPriority", "width: 90px", "createPriority", "priority", "15", "", "", null)%></td>
                                                            <td class="wob"><%=ComboInvariant(conn, "createGroup", "width: 140px", "createGroup", "editgroup", "2", "", "", null)%></td>
                                                            <td class="wob"><%=ComboInvariant(conn, "createStatus", "width: 140px", "createStatus", "editStatus", "1", "", "", null)%></td>
                                                            <%
                                                                rs_tccountgen.first();
                                                                do {
                                                            %> 
                                                            <td class="wob"><input value="<%=rs_tccountgen.getString("value")%>" type="checkbox" name="createTestcase_country_general" id="createTestcase_country_general"></td>
                                                                <%} while (rs_tccountgen.next());%>
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
                                                            <td class="wob" style="width: 1200px"><%out.print(dbDocS(conn, "testcase", "description", "Description"));%></td>
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
                                                            <td class="wob" style="width: 600px"><%out.print(dbDocS(conn, "testcase", "BehaviorOrValueExpected", "Value Expected"));%></td>
                                                            <td class="wob" style="width: 600px"><%out.print(dbDocS(conn, "testcase", "HowTo", "HowTo"));%></td>
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
                                                <td class="wob"><input type="submit" name="submitCreation" value="Create Test Case" onclick="$('#howtoDetail').val($('#createHowTo').elrte('val'));$('#valueDetail').val($('#createBehaviorOrValueExpected').elrte('val'));">
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
                } catch (Exception e) {
                    out.println("<br> error message : " + e.getMessage() + " "
                            + e.toString() + "<br>");
                } finally {
                    try {
                        conn.close();
                    } catch (Exception ex) {
                    }
                }
            %>
        </div>
        <br><% out.print(display_footer(DatePageStart));%>
    </body>
</html>
