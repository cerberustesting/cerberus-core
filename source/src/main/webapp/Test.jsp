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
<%@page import="org.cerberus.service.impl.TestService"%>
<%@page import="org.cerberus.entity.Test"%>
<%@page import="org.cerberus.dao.ITestDAO"%>
<%@page import="org.cerberus.service.ITestService"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">
<% Date DatePageStart = new Date() ; %>
<html>
    <head>
        <meta content="text/html; charset=UTF-8" http-equiv="content-type">
        <title>Test</title>

        <link rel="stylesheet" type="text/css" href="css/crb_style.css">
        <link rel="shortcut icon" type="image/x-icon" href="images/favicon.ico" />
    </head>
    <body>
        <%@ include file="include/function.jsp" %>
        <%@ include file="include/header.jsp" %>
        <div id="body">
            <%
            
                ITestService testService;
                ITestDAO testDao;
            
                /*
                 * Database connexion
                 */
                Connection conn = db.connect();
                try {
                    // 			System.out.println("Test : " + request.getParameter ( "stestbox" ));
                    testService = appContext.getBean(TestService.class);


                    Statement stmt = conn.createStatement();
                    Statement stmt21 = conn.createStatement();
                    Statement stmt22 = conn.createStatement();

                    String testSelected = "";
                    if (request.getParameter("stestbox") != null) {
                        testSelected = request.getParameter("stestbox");
                    }

                    String fil_tc = "";
                    String fil_tcdesc = "";
                    if (!testSelected.equals("")) {
                        ResultSet rs_test_filter = stmt.executeQuery("SELECT Test, Description "
                                + " FROM test " + " WHERE Test = '"
                                + testSelected + "'");

                        if (rs_test_filter.next()) {
                            fil_tc = rs_test_filter.getString("Test");
                            fil_tcdesc = rs_test_filter.getString("Description");
                        }

                        rs_test_filter.close();
                    }

                    /*
                     * Test : Test, Description, Active
                     */
                    List<Test> tests = testService.getListOfTest();
            %>
            <!-- Select List -->
            <div id="select">
                <table id="arrond">
                    <tr>
                        <td id="arrond"  >
                            <table>
                                <tr>
                                    <td class="wob">
                                        <table border="0">
                                            <tr>
                                                <td id="wob" style="font-weight: bold; width: 100px"> <%out.print(dbDocS(conn, "test", "test", "Test"));%></td>
                                                <td id="selectboxtestpage" class="wob" >
                                                    <form action="Test.jsp" method="post" name="selectTest">
                                                        <select id="stestbox" name="stestbox" style="width: 300px">
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
                                                                if (fil_tc.equalsIgnoreCase(test.getTest())) {
                                                                    out.print("selected=\"selected\"");
                                                                }
                                                                    %> value="<%=test.getTest()%>"> <%=test.getTest() + " - " + test.getDescription()%> </option>
                                                            <%
                                                                }
                                                            %></select> <input id="loadbutton" class="button" type="submit" value="Load">

                                                    </form></td>
                                            </tr>
                                        </table></td></tr></table></td></tr></table>
            </div><%
                /*
                 * Test & TestCase
                 */
                int rs_test_select_cpt_incr_field_length = 1; // Used for max forms length
                ResultSet rs_test_select = stmt.executeQuery("SELECT DISTINCT Test, Description, Active, Automated"
                        + " FROM test "
                        + " WHERE test = '"
                        + testSelected + "'");

                Statement stmt_testcase = conn.createStatement();

                int rs_testcase_cpt_incr_field_length = 5; // Used for max forms length
                ResultSet rs_testcase = stmt_testcase.executeQuery("SELECT DISTINCT t1.Test, t1.Description, Active, "
                        + "testcase, t2.Description, BehaviorOrValueExpected, priority, t2.status, t2.group, "
                        + "t2.tcActive, t2.Application, t2.Project, t2.Ticket, t2.group, t2.Origine, t2.RefOrigine, t2.howto,"
                        + " t2.comment, t2.TCDateCrea, t2.frombuild, t2.fromrev, t2.tobuild, t2.torev, t2.bugid, t2.targetbuild,"
                        + " t2.targetrev, t2.creator, t2.implementer, t2.lastmodifier, t2.activeQA, t2.activeUAT, t2.activePROD"
                        + " FROM test t1, testcase t2"
                        + " WHERE t1.test = t2.test "
                        + " AND t1.test = '" + testSelected + "'");
            %>

            <%
                String test_test = "";
                String test_description = "";
                String test_active = "";
                String test_automated = "";
                String test_activated__man = "";
                String test_automated__man = "";

                if (rs_test_select.next()) {
                    test_test = rs_test_select.getString("Test");
                    test_description = rs_test_select.getString("Description");
                    test_active = rs_test_select.getString("Active");
                    test_automated = rs_test_select.getString("Automated");
                    test_activated__man = "Y";
                    if (!test_active.trim().equalsIgnoreCase("N")) {
                        test_activated__man = "N";
                    }

                    test_automated__man = "Y";
                    if (!test_automated.trim().equalsIgnoreCase("N")) {
                        test_automated__man = "N";
                    }
            %>

            <form method="post" name="DeleteTest"> 
                <table  id="generalparameter" class ="arrond" style="text-align: left; display:none" border="0" cellpadding="2" cellspacing="2" >
                    <tr>
                        <td class="wob">
                            <h3 style="color: blue">Test Parameters</h3>
                            <table>
                                <tr>
                                    <td class="wob" style="font-weight: bold; width: 110px"><%out.print(dbDocS(conn, "test", "test", "Test"));%></td>

                                    <td class="wob"><input style="font-weight: bold; width: 200px" name="test_test" id="test_test"
                                                           maxlength="<%=rs_test_select.getMetaData().getColumnDisplaySize(
                                                                rs_test_select_cpt_incr_field_length++)%>"
                                                           value="<%=test_test%>"
                                                           onchange="EnableAddTestButton('add_test', 'submit_changes', updateTest.test_test.value, '<%=test_test%>' );"></td>
                                </tr>
                                <tr>
                                    <td id="wob" style="font-weight: bold; width: 110px"><%out.print(dbDocS(conn, "test", "description", "Description"));%></td>
                                    <td class="wob" ><input id="test_description" style="width: 900px" name="test_description" value="<%=test_description%>"
                                                            maxlength="<%=rs_test_select.getMetaData().getColumnDisplaySize(
                                                                rs_test_select_cpt_incr_field_length++)%>"></td>
                                </tr>

                                <tr>
                                    <td id="wob" style="font-weight: bold; width: 110px"><%out.print(dbDocS(conn, "test", "active", "Active"));%></td>
                                    <td class="wob" ><select id="test_active" style="width: 40px;" name="test_active">
                                            <option selected="selected" value="<%=test_active%>"><%=test_active%></option>
                                            <option value="<%=test_activated__man%>"><%=test_activated__man%></option>
                                        </select></td>
                                </tr>
                                <tr>
                                    <td id="wob" style="font-weight: bold; width: 110px"><%out.print(dbDocS(conn, "test", "automated", "Automated"));%></td>
                                    <td class="wob" ><select id="test_automated" style="width: 40px;" name="test_automated">
                                            <option selected="selected" value="<%=test_automated%>"><%=test_automated%></option>
                                            <option value="<%=test_automated__man%>"><%=test_automated__man%></option>
                                        </select></td>
                                </tr>
                            </table></td><td id="wob" valign="top"><input id="button2" type="button" value="-" onclick="javascript:setInvisible();">
                        </td></tr><tr><td class="wob"><table><tr>
                                    <td class="wob"><input id="deletetestbutton" class="button" name="delete_test" value="Delete Test" type="submit" onclick="redirectionTestCase(2, '<%=test_test%>')"></td>
                                    <td class="wob"><input id="savetestbutton" class="button" name="save_test" value="Save Test Modification" type="submit" onclick="redirectionTestCase(0, '<%=test_test%>')"></td>

                                </tr></table></td></tr></table></form>
            <table  id="parametergeneral" class="arrond" style="text-align: left; display:table" border="0" cellpadding="2" cellspacing="2" >
                <tr><td class="wob">
                        <table>
                            <tr><td class="wob" style="font-weight: bold; width: 150px" name="test" id="test">\\ <%=test_test%> //</td>
                                <td class="wob" id="description" style="width: 840px" name="description">\\ <%=test_description%> //</td>
                                <td id="wob" style="font-weight: bold; width: 50px">\\ Active: </td>
                                <td class="wob" id="active" style="width: 40px;" name="active"><%=test_active%> //</td>
                                <td id="wob" style="font-weight: bold; width: 80px">\\ Automated: </td>
                                <td class="wob" id="automated" style="width: 40px;" name="automated"><%=test_automated%> //</td>
                                <td class="wob"><input id="button1" type="button" value="+" onclick="javascript:setVisible();"></td>	</tr>
                        </table></td></tr></table>   

            <form method="post" name="DeleteTestCase" action="DeleteTestCase">               

                <br><table id="arrond"><tr><td>
                            <div id="table">
                                <h3 style="color: blue">TestCase List</h3>
                                <!--                             <div style="overflow:scroll; border: 1px solid gray; height:300px;">-->
                                <table id="testcasetable" class="tableau"  style="text-align: left; border-collapse: collapse" border="0px" cellpadding="0px" cellspacing="0px">
                                    <!--					<tr id="header" style="position:relative;top:expression(this.offsetParent.scrollTop-2);">-->
                                    <tr id="header">   
                                        <td style="width: 10px"><%out.print(dbDocS(conn, "test", "delete", "Delete"));%></td>
                                        <td style="width: 200px" colspan="2">Testcase Information</td>
                                        <td style="width: 800px" colspan="2">Testcase Parameters</td>
                                        <td style="width: 100px">Activation Criterias</td>
                                    </tr>
                                    <%
                                        // Set default values if any testcase exists
                                        int maxlength_testcase_desc = 400;
                                        int maxlength_testcase_behavior = 250;
                                        int maxlength_testcase_status = 30;
                                        int maxlength_testcase_group = 30;
                                        int i = 0;
                                        int j = 0;
                                        if (rs_testcase.next()) {

                                            // variables used for javascript tab line adding
                                            //	maxlength_testcase_desc = rs_testcase.getMetaData()
                                            //			.getColumnDisplaySize(5);
                                            //	maxlength_testcase_behavior = rs_testcase.getMetaData()
                                            //			.getColumnDisplaySize(6);
                                            //	maxlength_testcase_status = rs_testcase.getMetaData()
                                            //			.getColumnDisplaySize(10);
                                            //	maxlength_testcase_group = rs_testcase.getMetaData()
                                            //			.getColumnDisplaySize(10);

                                            do {

                                                /*
                                                 * Set String value for
                                                 * Select Read Only
                                                 */
                                                
                                                String tcActive_toselect = "N";
                                                if (!rs_testcase.getString("t2.tcActive").trim().equals("Y")) {
                                                    tcActive_toselect = "Y";
                                                }
                                                String readonly = "";
                                                String disabled = "";
                                                if (!request.isUserInRole("Admin") && rs_testcase.getString("t2.status").equalsIgnoreCase("WORKING")) {
                                                    readonly = "readonly=\"readonly\"";
                                                    disabled = "disabled";
                                                }

                                                Statement stmt2 = conn.createStatement();
                                                ResultSet rs_testcasecountry = stmt2.executeQuery("SELECT DISTINCT country "
                                                        + " FROM testcasecountry "
                                                        + " WHERE test ='"
                                                        + rs_testcase.getString("t1.Test")
                                                        + "'" + " AND testcase ='"
                                                        + rs_testcase.getString("testcase")
                                                        + "'");

                                                String countries = "";
                                                while (rs_testcasecountry.next()) {
                                                    countries += rs_testcasecountry.getString("country") + "-";
                                                }
                                                rs_testcasecountry.close();
                                                stmt2.close();

                                                String color = "";
                                                j = i % 2;
                                                if (j == 1) {
                                                    color = "#f3f6fa";
                                                } else {
                                                    color = "White";
                                                }

                                                String bugID = "BugID: ";
                                                bugID += rs_testcase.getString("t2.bugid");
                                                String ticket = "Ticket: ";
                                                ticket += rs_testcase.getString("t2.ticket");
                                                String project = "Project: ";
                                                project += rs_testcase.getString("t2.project");
                                                String app = "Application: ";
                                                app += rs_testcase.getString("t2.application");
                                                String origine = "Origine: ";
                                                origine += rs_testcase.getString("t2.origine");
                                                String group = "Group: ";
                                                group += rs_testcase.getString("t2.group");
                                                String fromBuild = "FromBuild: ";
                                                fromBuild += rs_testcase.getString("t2.frombuild");
                                                String toBuild = "ToBuild: ";
                                                toBuild += rs_testcase.getString("t2.tobuild");
                                                String fromRev = "FromRev: ";
                                                fromRev += rs_testcase.getString("t2.fromrev");
                                                String toRev = "ToRev: ";
                                                toRev += rs_testcase.getString("t2.torev");
                                                String priority = "Priority: ";
                                                priority += rs_testcase.getString("priority");
                                                String activeqa = "ActiveQA: ";
                                                activeqa += rs_testcase.getString("t2.activeQA");
                                                String activeuat = "ActiveUAT: ";
                                                activeuat += rs_testcase.getString("t2.activeUAT");
                                                String activeprod = "ActivePROD: ";
                                                activeprod += rs_testcase.getString("t2.activePROD");
                                                String refOrigine = "RefOrigine: ";
                                                refOrigine += rs_testcase.getString("t2.reforigine");
                                                String status = "Status: ";
                                                status += rs_testcase.getString("t2.status");
                                                String tcActive = "Active: ";
                                                tcActive += rs_testcase.getString("t2.tcActive");
                                                String creator = "Creator: ";
                                                creator += rs_testcase.getString("t2.creator");


                                                i++;

                                    %>
                                    <tr style="background-color: <%=color%>">
                                        <td>

                                            <table><tr><td>
                                                        <input id="test_testcase_delete" name="test_testcase_delete" type="checkbox" value="<%=rs_testcase.getString("t1.Test")%> - <%=rs_testcase.getString("testcase")%>"></td>
                                                </tr><tr><td style="text-align: center">
                                                        <a style="font-size: xx-small;" href="TestCase.jsp?Test=<%=rs_testcase.getString("T1.Test")%>&TestCase=<%=rs_testcase.getString("testcase")%>&Load=Load">edit</a></td>
                                                </tr></table>
                                        </td><td valign="top">
                                            <table><tr>    
                                                    <td id="testcase_testcase1" class="wob" style="font-weight: bold; width: 150px;" name="testcase_testcase" readonly="readonly">
                                                        TC: <%=rs_testcase.getString("testcase")%></td></tr><tr>
                                                        <% if (StringUtils.isNotBlank(rs_testcase.getString("t2.origine"))) {%>
                                                    <td id="testcase_origine" class="wob" style=" width: 150px;" name="testcase_origine">
                                                        <%=StringUtils.isBlank(rs_testcase.getString("t2.origine")) ? "" : origine%></td></tr><tr>
                                                        <% }
                                            if (StringUtils.isNotBlank(rs_testcase.getString("t2.reforigine"))) {%>
                                                    <td id="testcase_reforigine" class="wob" style="width: 150px;" name="testcase_refOrigine">
                                                        <%=StringUtils.isBlank(rs_testcase.getString("t2.reforigine")) ? "" : refOrigine%></td></tr><tr>
                                                        <%}
                                            if (StringUtils.isNotBlank(rs_testcase.getString("t2.creator"))) {%>
                                                    <td id="testcase_creator" class="wob" style="width: 150px;" name="testcase_creator">
                                                        <%=StringUtils.isBlank(rs_testcase.getString("t2.creator")) ? "" : creator%></td></tr><tr>
                                                        <%}%>
                                                    <td id="testcase_activeqa" class="wob" style="width: 150px;" name="testcase_activeqa">
                                                        <%=StringUtils.isBlank(rs_testcase.getString("t2.activeQA")) ? "" : activeqa%></td>
                                                    <td id="testcase_activeuat" class="wob" style="width: 150px;" name="testcase_activeuat">
                                                        <%=StringUtils.isBlank(rs_testcase.getString("t2.activeUAT")) ? "" : activeuat%></td>
                                                    <td id="testcase_activeprod" class="wob" style="width: 150px;" name="testcase_activeprod">
                                                        <%=StringUtils.isBlank(rs_testcase.getString("t2.activePROD")) ? "" : activeprod%></td>
                                                </tr></table>      
                                        </td><td valign="top">
                                            <table><tr>
                                                    <%
                                            if (StringUtils.isNotBlank(rs_testcase.getString("t2.application"))) {%> 
                                                    <td id="testcase_application" class="wob" name="testcase_application" style="width: 250px">
                                                        <%=StringUtils.isBlank(rs_testcase.getString("t2.application")) ? "" : app%></td></tr><tr>
                                                        <%}
                                             if (StringUtils.isNotBlank(rs_testcase.getString("t2.bugid"))) {%>
                                                    <td id="testcase_bugID" class="wob" style="width: 150px;" name="testcase_bugID">
                                                        <%=StringUtils.isBlank(rs_testcase.getString("t2.bugid")) ? "" : bugID%></td></tr><tr>
                                                        <%}
                                            if (StringUtils.isNotBlank(rs_testcase.getString("t2.project"))) {%> 
                                                    <td id="testcase_project" class="wob" style="width: 150px;" name="testcase_project">
                                                        <%=StringUtils.isBlank(rs_testcase.getString("t2.project")) ? "" : project%></td></tr><tr>
                                                        <%}
                                            if (StringUtils.isNotBlank(rs_testcase.getString("t2.ticket"))) {%>
                                                    <td id="testcase_ticket" class="wob" style="width: 150px;" name="testcase_ticket">
                                                        <%=StringUtils.isBlank(rs_testcase.getString("t2.ticket")) ? "" : ticket%></td></tr><tr>
                                                        <%}
                                            if (StringUtils.isNotBlank(rs_testcase.getString("priority"))) {%> 
                                                    <td id="testcase_priority" class="wob" style="width: 150px;" name="testcase_priority">
                                                        <%=StringUtils.isBlank(rs_testcase.getString("priority")) ? "" : priority%></td></tr><tr>
                                                    <%}%></tr></table>   
                                        </td><td valign="top">
                                            <table><tr>
                                                    <%
                                            if (StringUtils.isNotBlank(rs_testcase.getString("t2.group"))) {%> 

                                                    <td id="testcase_group" class="wob" style="width: 250px;" name="testcase_group">
                                                        <%=StringUtils.isBlank(rs_testcase.getString("t2.group")) ? "" : group%></td></tr><tr>
                                                        <%}
                                             if (StringUtils.isNotBlank(rs_testcase.getString("t2.status"))) {%>
                                                    <td id="testcase_status" class="wob" style="width: 250px;" name="testcase_status">
                                                        <%=StringUtils.isBlank(rs_testcase.getString("t2.status")) ? "" : status%></td></tr><tr>
                                                        <%}%>
                                                    <td id="testcase_countries" class="wob"  style="width: 250px; font-size: x-small;" name="testcase_countries">
                                                        <%=countries%></td>                    
                                                </tr></table>     
                                        </td><td valign="top">
                                            <table><tr>
                                                    <td id="testcase_description" class="wob" style="width: 600px; font-weight: bold" name="testcase_description">
                                                        <%=rs_testcase.getString("t2.Description")%></td></tr><tr>
                                                    <td class="wob"><textarea  id="testcase_valueexpec" class="wob" rows="1" style="width: 600px; background-color: <%=color%>" name="testcase_valueexpec"
                                                                               readonly="readonly"><%=rs_testcase.getString("BehaviorOrValueExpected")%></textarea></td></tr><tr>
                                                </tr></table>
                                        </td><td valign="top">
                                            <table><tr>
                                                    <%
                                                    if (StringUtils.isNotBlank(rs_testcase.getString("t2.tcActive"))) {%>
                                                    <td id="testcase_tcActive" class="wob" style="width: 150px;" name="testcase_tcActive">
                                                        <%=StringUtils.isBlank(rs_testcase.getString("t2.tcActive")) ? "" : tcActive%></td>

                                                    <%}
                                            if (StringUtils.isNotBlank(rs_testcase.getString("t2.frombuild"))) {%>
                                                    <td id="testcase_frombuild" class="wob" style="width: 100px;" name="testcase_fromBuild">
                                                        <%=StringUtils.isBlank(rs_testcase.getString("t2.frombuild")) ? "" : fromBuild%></td></tr><tr>
                                                        <%}
                                            if (StringUtils.isNotBlank(rs_testcase.getString("t2.fromrev"))) {%>
                                                    <td id="testcase_fromrev" class="wob" style="width: 100px;" name="testcase_fromRev">
                                                        <%=StringUtils.isBlank(rs_testcase.getString("t2.fromrev")) ? "" : fromRev%></td></tr><tr>
                                                        <%}
                                            if (StringUtils.isNotBlank(rs_testcase.getString("t2.tobuild"))) {%>
                                                    <td id="testcase_tobuild" class="wob" style="width: 100px;" name="testcase_toBuild">
                                                        <%=StringUtils.isBlank(rs_testcase.getString("t2.tobuild")) ? "" : toBuild%></td></tr><tr>
                                                        <%}%>
                                                    <td id="testcase_torev" class="wob" style=" width: 100px;" name="testcase_toRev">
                                                        <%=StringUtils.isBlank(rs_testcase.getString("t2.torev")) ? "" : toRev%></td> 
                                                </tr></table>
                                        </td></tr>   

                                    <%
                                                rs_testcase_cpt_incr_field_length = 5; // Reset counter for max length

                                            } while (rs_testcase.next());/*
                                             * End testcase loop
                                             */
                                        }
                                    %>
                                </table>
                                <!--                        </div>-->

                                <%
                                    /*
                                     * End
                                     * Test
                                     * Request
                                     */

                                    rs_testcase.close();
                                    rs_test_select.close();
                                    stmt.close();
                                    stmt21.close();
                                    stmt22.close();
                                    stmt_testcase.close();
                                %>
                                <br> 
                                <input class="button" name="submit_changes" id="submit_changes" value="Delete TestCase" type="submit"> 
                            </div></td></tr></table>
            </form>
            <%
                }
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
<br><% out.print(display_footer(DatePageStart)); %>
    </body>
</html>
