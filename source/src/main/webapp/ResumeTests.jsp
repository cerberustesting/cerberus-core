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
<%@page import="org.cerberus.refactor.BatchInfo"%>
<%@page import="org.apache.commons.lang3.StringUtils"%>
<%@page import="java.util.Enumeration"%>
<%@page import="java.util.LinkedList"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">
<% Date DatePageStart = new Date() ; %>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Resume Tests</title>
        <link rel="stylesheet" type="text/css" href="css/crb_style.css">
        <link rel="shortcut icon" type="image/x-icon" href="images/favicon.ico" />


    </head>
    <body>

        <%@ include file="include/function.jsp" %>
        <%@ include file="include/header.jsp" %>

        <div id="body">


            <form method="POST" name="ResumeTest">

                <%

                    String testcaseApplication = null;

                    Connection conn = db.connect();
                    try {



                        Statement stmt = conn.createStatement();
                        Statement stmt2 = conn.createStatement();




                        if (request.getParameter("statusPage") != null && request.getParameter("statusPage").compareTo("Resume") == 0) {

                            StringBuilder params = new StringBuilder();
                            params.append("BatchExecution?redirect=Y");
                            Enumeration<String> pList = request.getParameterNames();
                            while (pList.hasMoreElements()) {
                                String sName = pList.nextElement().toString();
                                if (sName.compareTo("Test") == 0 || sName.compareTo("TestCase") == 0
                                        || sName.compareTo("Country") == 0 || sName.compareTo("Environment") == 0
                                        || sName.compareTo("ss_ip") == 0 || sName.compareTo("ss_p") == 0
                                        || sName.compareTo("browser") == 0 || sName.compareTo("logpath") == 0 || sName.compareTo("Tag") == 0 || sName.compareTo("ResumeID") == 0) {
                                    String[] sMultiple = request.getParameterValues(sName);
                                    {
                                        for (int i = 0; i < sMultiple.length; i++) {
                                            params.append("&" + sName + "=" + sMultiple[i] + "");
                                        }
                                    }
                                }
                            }
                            response.sendRedirect(params.toString());
                        }


                        if (request.getParameter("statusPage") != null && request.getParameter("statusPage").compareTo("Delete") == 0) {

                            Enumeration<String> pList = request.getParameterNames();
                            while (pList.hasMoreElements()) {
                                String sName = pList.nextElement().toString();
                                if (sName.compareTo("ResumeID") == 0) {
                                    String[] sMultiple = request.getParameterValues(sName);
                                    {
                                        for (int i = 0; i < sMultiple.length; i++) {
                                            System.out.println("Going to delete : " + sMultiple[i]);
                                            Statement stmtDelete = conn.createStatement();
                                            String sqld = "UPDATE testcaseexecution set FINISHED = 'Y', ControlStatus = 'CC' WHERE ID = " + sMultiple[i];
                                            stmtDelete.executeUpdate(sqld);
                                        }
                                    }

                                }
                            }
                        }

                        if (request.getParameter("statusPage") != null && request.getParameter("statusPage").compareTo("ExportList") == 0) {
                        }

                        Boolean locked = true;

                        if (request.getParameter("statusPage") != null && request.getParameter("statusPage").compareTo("Lock") == 0) {
                            locked = true;
                        }

                        if (request.getParameter("statusPage") != null && request.getParameter("statusPage").compareTo("Unlock") == 0) {
                            locked = false;
                        }


                        String ssIP;
                        if (request.getParameter("ss_ip") != null && request.getParameter("ss_ip").compareTo("") != 0) {
                            ssIP = request.getParameter("ss_ip");
                        } else {
                            ssIP = request.getHeader("X-FORWARDED-FOR");
                            if (ssIP == null) {

                                String defaultIP = "SELECT DefaultIP from user where login = '"
                                        + request.getUserPrincipal().getName() + "'";

                                ResultSet rs_Ip = stmt2.executeQuery(defaultIP);
                                if (rs_Ip.first() && StringUtils.isNotBlank(rs_Ip.getString("DefaultIP"))) {
                                    ssIP = rs_Ip.getString("DefaultIP");
                                } else {
                                    ssIP = "";
                                }


                                //    ssIP = request.getRemoteHost();
                            }
                        }

                        String ssPort;
                        if (request.getParameter(
                                "ss_p") != null && request.getParameter("ss_p").compareTo("") != 0) {
                            ssPort = request.getParameter("ss_p");
                        } else {
                            ssPort = "5555";
                        }

                        String browser;
                        if (request.getParameter(
                                "browser") != null && request.getParameter("browser").compareTo("") != 0) {
                            browser = request.getParameter("browser");;
                        } else {
                            browser = new String("*firefox");
                        }

                        String logpath;
                        if (request.getParameter(
                                "logpath") != null && request.getParameter("logpath").compareTo("") != 0) {
                            logpath = request.getParameter("logpath");;
                        } else {
                            logpath = new String("logpath");
                        }
                        StringBuilder sqlOpts = new StringBuilder();

                        Boolean filter;
                        if (request.getParameter(
                                "Filter") != null && request.getParameter("Filter").compareTo("FilterON") == 0) {
                            filter = true;
                        } else {
                            filter = false;
                        }

                        String tcActive;
                        if (request.getParameter(
                                "TcActive") != null && request.getParameter("TcActive").compareTo("A") != 0) {
                            tcActive = request.getParameter("TcActive");;
                        } else {
                            tcActive = new String("%%");
                        }

                        String readOnly;
                        if (request.getParameter(
                                "ReadOnly") != null && request.getParameter("ReadOnly").compareTo("A") != 0) {
                            readOnly = request.getParameter("ReadOnly");
                        } else {
                            readOnly = new String("%%");
                        }

                        String priority;
                        if (request.getParameter(
                                "Priority") != null && request.getParameter("Priority").compareTo("All") != 0) {
                            priority = request.getParameter("Priority");
                        } else {
                            priority = new String("%%");
                        }

                        String environment;
                        if (request.getParameter(
                                "Environment") != null && request.getParameter("Environment").compareTo("All") != 0) {
                            environment = request.getParameter("Environment");
                        } else {
                            environment = new String("%%");
                        }

                        String project;
                        if (request.getParameter(
                                "Project") != null && request.getParameter("Project").compareTo("All") != 0) {
                            project = request.getParameter("Project");
                        } else {
                            project = new String("%%");
                        }

                        String app;
                        if (request.getParameter(
                                "Application") != null && request.getParameter("Application").compareTo("All") != 0) {
                            app = request.getParameter("Application");
                        } else {
                            app = new String("%%");
                        }


                        String test;
                        if (request.getParameter(
                                "Test") != null && request.getParameter("Test").compareTo("All") != 0) {
                            test = request.getParameter("Test");
                        } else {
                            test = new String("%%");
                        }

                        String testcase;
                        if (request.getParameter(
                                "TestCase") != null && request.getParameter("TestCase").compareTo("All") != 0) {
                            testcase = request.getParameter("TestCase");
                        } else {
                            testcase = new String("%%");
                        }

                        String country;
                        if (request.getParameter(
                                "Country") != null && request.getParameter("Country").compareTo("All") != 0) {
                            country = request.getParameter("Country");
                        } else {
                            country = new String("%%");
                        }

                        String tag;
                        if (request.getParameter(
                                "Tag") != null && request.getParameter("Tag").compareTo("All") != 0) {
                            tag = request.getParameter("Tag");
                        } else {
                            tag = new String("None");
                        }

                        
                %>


                <table>
                    <tr><td id="arrond"  ><h3 style="color: blue">Tool Parameters</h3><br>

                            <!--         <form method="post" name="ToolParameters" action="ToolParameters" >-->
                            <table border="0px">
                                <tr>                                         
                                    <td id="wob" style="font-weight: bold; width: 150px"><% out.print(dbDocS(conn, "page_runtests", "SeleniumServerIP", "Selenium Server IP "));%></td>
                                    <td id="wob"><input type="text" name="ss_ip" value="<%= ssIP%>" />
                                    </td>
                                </tr>
                                <tr>
                                    <td id="wob" style="font-weight: bold; width: 150px"><% out.print(dbDocS(conn, "page_runtests", "SeleniumServerPort", "Selenium Server Port "));%></td>
                                    <td id="wob"><input type="text" name="ss_p" value="<%= ssPort%>" />
                                    </td>
                                </tr>
                                <tr>
                                    <td id="wob" style="font-weight: bold; width: 150px"><% out.print(dbDocS(conn, "page_runtests", "Browser", "Browser"));%></td>
                                    <td id="wob"><input type="text" name="browser" value="<%= browser%>"
                                                        style="width: 450px" />
                                    </td>
                                </tr>
                                <tr>
                                    <td id="wob" style="font-weight: bold; width: 150px">Log Path</td>
                                    <td id="wob"><input
                                            type="text" name="logpath" value="<%= logpath%>"
                                            style="width: 450px" />
                                    </td>
                                </tr>


                            </table>
                        </td>
                    </tr>
                </table>
                <!--         </form>-->



                <!--         </form>-->


                <br>
                <table>                    <tr>

                        <td id="wob"><br /><input type="submit" name="ButtonRefresh" value="Refresh"></td>

                    </tr>
                </table>
                <br>
                <table>
                    <tr><td id="arrond">
                            <!--         <form method="post" name="Tests" action="Tests" >-->
                            <table border="0px">
                                <tr>
                                    <td COLSPAN=3 id="wob" style="font-weight: bold; width: 100px"><% out.print(dbDocS(conn, "testcase", "TestCase", "Test Case"));%></td>


                                    <%/*
                                                                     * if
                                                                     * (locked)
                                                                     * { %>
                                    <td id="wob"><input type="submit" name="statusPage" value="Unlock"></td>
                                        <% } %>
                                        <% if (!locked) { %>
                                    <td id="wob"><input type="submit" name="statusPage" value="Lock"></td>
                                        <% }
                                     */%>

                                </tr>
                                <tr>
                                    <td id="wob" COLSPAN=4><select multiple="yes" size="11" id="testcase"
                                                                   name="ResumeID" style="width: 600px" >
                                            <!-- 						onchange="document.ResumeTest.submit()"  >-->
                                            <% ResultSet rsTestCase = stmt.executeQuery("SELECT DISTINCT i.id, i.test, i.testcase, b.batnumexe, i.application, i.country, i.environment, i.tag, i.build  from testcaseexecution i, testcasestepexecution b where finished = 'N' and i.id = b.id");
                                                String executed = "";
                                                String total = "";
                                                Boolean exec = true;
                                                while (rsTestCase.next()) {
                                            %>
                                            <option style="width: 600px"
                                                    value="<%= rsTestCase.getString("id")%>"



                                                    <% if (rsTestCase.getString("BatNumExe") == null || rsTestCase.getString("BatNumExe").compareTo("null") == 0) {
                                                            if (locked) {
                                                                out.print("  DISABLED=\"BATNUM\" ");
                                                            }
                                                        }
                                                    %>




                                                    <%
                                                        Statement stmtB = conn.createStatement();
                                                        ResultSet rsB = stmtB.executeQuery("select step,returncode from testcasestepexecution where id = '" + rsTestCase.getString("ID") + " order by step asc'");


                                                        Statement stmtT = conn.createStatement();
                                                        ResultSet rsT = stmtT.executeQuery("Select count(*) from testcasestep where test = '" + rsTestCase.getString("Test") + "' and testcase = '" + rsTestCase.getString("TestCase") + "'");

                                                        while (rsT.next()) {
                                                            total = rsT.getString(1);
                                                        }
                                                        while (rsB.next()) {
                                                            executed = rsB.getString("Step");
                                                            if (rsB.getString("ReturnCode").compareTo("KO") == 0 || total.compareTo(executed) == 0) {
                                                                exec = false;
                                                                if (locked) {
                                                                    out.print("  DISABLED=\"KO\" ");
                                                                }
                                                            }
                                                        }


                                                        Statement stmtBN = conn.createStatement();
                                                        ResultSet rsBN = stmtBN.executeQuery("SELECT * FROM `buildrevisionbatch` where build = '" + rsTestCase.getString("Build") + "' and Environment = '" + rsTestCase.getString("Environment") + "' and Country = '" + rsTestCase.getString("Country") + "';");

                                                        LinkedList<BatchInfo> batchList = new LinkedList<BatchInfo>();
                                                        Statement stmtBI = conn.createStatement();
                                                        ResultSet rsBI = stmtBN.executeQuery("SELECT * FROM `batchinvariant`");
                                                        while (rsBI.next()) {
                                                            BatchInfo b = new BatchInfo();
                                                            b.setId(rsBI.getString("Batch"));
                                                            b.setIncIni(rsBI.getString("IncIni"));
                                                            b.setUnit(Integer.parseInt(rsBI.getString("Unit")));
                                                            batchList.add(b);
                                                        }
                                                        String batNumExe = new String("0");
                                                        Iterator<BatchInfo> it = batchList.iterator();
                                                        while (it.hasNext()) {

                                                            BatchInfo info = it.next();


                                                            Statement stmtIB = conn.createStatement();
                                                            String sql = "SELECT count(id) AS num FROM `buildrevisionbatch` where Batch = '"
                                                                    + info.getId() + "'";
                                                            ResultSet rs = stmtIB.executeQuery(sql);

                                                            batNumExe = info.calculateBatNumExe(rs, 0, batNumExe);
                                                        }

                                                        Integer currentStep = Integer.parseInt(executed) + 1;
                                                        Statement stmtBSelected = conn.createStatement();
                                                        String sqlSelectedBatchs = "SELECT Batch FROM `testcasestepbatch` where Test = '"
                                                                + rsTestCase.getString("Test")
                                                                + "' and TestCase = '"
                                                                + rsTestCase.getString("TestCase")
                                                                + "' and Step = '"
                                                                + currentStep + "'";
                                                        ResultSet rsSelected = stmtBSelected.executeQuery(sqlSelectedBatchs);
                                                        Boolean b = false;
                                                        while (rsSelected.next()) {
                                                            Iterator<BatchInfo> itB = batchList.iterator();
                                                            while (itB.hasNext()) {
                                                                System.out.println("-----------------------------------------------");
                                                                BatchInfo bat = itB.next();
                                                                if (rsSelected.getString("Batch").compareTo(bat.getId()) == 0) {
                                                                    Integer maxact = bat.getUnit();
                                                                    Integer maxexe = bat.getUnit();
                                                                    try {
                                                                        if (batNumExe.length() >= (bat.getUnit() + 1)) {
                                                                            maxact = bat.getUnit() + 1;
                                                                            System.out.println("act " + batNumExe + "  ->  point : " + (bat.getUnit() - 1) + " : " + maxact);
                                                                        }
                                                                        if (rsTestCase.getString("BatNumExe").length() >= (bat.getUnit() + 1)) {
                                                                            maxexe = bat.getUnit() + 1;
                                                                            System.out.println("exe " + rsTestCase.getString("BatNumExe") + "  ->  point : " + (bat.getUnit() - 1) + " : " + maxexe);
                                                                        }

                                                                        Integer act = Integer.parseInt(batNumExe.subSequence(bat.getUnit() - 1, maxact).toString());
                                                                        Integer exe = Integer.parseInt(rsTestCase.getString("BatNumExe").subSequence(bat.getUnit() - 1, maxexe).toString());
                                                                        System.out.println("Act : " + act + "              Exe : " + exe);
                                                                        if (act > exe) {
                                                                            b = true;
                                                                        }
                                                                    } catch (Exception ex) {
                                                                        b = false;
                                                                    }
                                                                }
                                                            }
                                                        }

                                                        Boolean byAct = false;
                                                        if (!b) {
                                                            out.print(" class=\"DISABLED\"");
                                                            if (locked) {
                                                                out.print(" disabled = \"disabled\""); // out.print("  DISABLED=\"ACT\" ");
                                                            }
                                                            exec = false;
                                                            byAct = true;
                                                        }


                                                    %>>						
                                                <%= rsTestCase.getString("Test")%> <%= rsTestCase.getString("TestCase")%> [<%= rsTestCase.getString("Application")%>] <%= rsTestCase.getString("Country")%>  [<%= rsTestCase.getString("Environment")%>]  <%= rsTestCase.getString("Tag")%>  [<%= rsTestCase.getString("ID")%>] <%= executed%> / <%= total%> Executed.
                                                <%= exec ? " Waiting to be resumed " : " Cannot be Resumed "%>
                                                <%
                                                    if (!exec && !byAct) {
                                                        Statement stmtDelete = conn.createStatement();
                                                        String sqld = "UPDATE testcaseexecution set FINISHED = 'Y' WHERE ID = " + rsTestCase.getString("ID");
                                                        stmtDelete.executeUpdate(sqld);
                                                    }
                                                %>

                                                <%     	if (testcase.compareTo(rsTestCase.getString(1)) == 0) {
                                                                    testcaseApplication = rsTestCase.getString(2);
                                                                }
                                                            }%>
                                        </select>

                                    </td>
                                </tr>
                                <tr>				
                                    <td id="wob"><br /><% if (locked) {%><input type="submit" name="statusPage" value="Resume"><% }
                                    if (!locked) {%><input type="submit" DISABLED="" name="statusPage" value="Resume"> <% }%></td>
                                    <td id="wob"><br /><input type="submit" name="statusPage" value="Delete"></td>
                                    <td id="wob"><br /><input type="submit" name="statusPage" value="ExportList"></td>
                                </tr>
                            </table>

                            <!--         </form>-->
                            <br>
                            <br>


                        </td>
                    </tr>
                </table>
            </form>

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

        </div>

<br><% out.print(display_footer(DatePageStart)); %>
    </body>
</html>
