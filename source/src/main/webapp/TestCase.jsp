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
<%@page import="org.cerberus.entity.BuildRevisionInvariant"%>
<%@page import="org.cerberus.service.impl.BuildRevisionInvariantService"%>
<%@page import="org.cerberus.service.IBuildRevisionInvariantService"%>
<%@page import="org.cerberus.service.IApplicationService"%>
<%@page import="org.apache.commons.lang3.StringUtils"%>
<%@page import="java.sql.Connection"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>
<%@page import="java.sql.ResultSet"%>
<%@page import="java.sql.Statement"%>
<%@page import="java.net.URLEncoder"%>

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">
<% Date DatePageStart = new Date();%>
<html>
    <head>
        <meta content="text/html; charset=UTF-8" http-equiv="content-type">
        <title>TestCase</title>
        
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
        <link rel="shortcut icon" type="image/x-icon" href="images/favicon.ico" />

        <script type="text/javascript">
            function checkFieldDuplicate ( elementTest, elementTestCase )
            {
                if (window.document.getElementById('editTest').value == elementTest && window.document.getElementById('editTestCase').value == elementTestCase) {
                    disableFieldDuplicate();
                }
                else {
                    enableFieldDuplicate();
                }	
            }

            function enableFieldDuplicate (  )
            {
	
                window.document.getElementById('submitButtonDuplicate').disabled = false;
                window.document.getElementById('submitButtonChanges').disabled = true;
            }

            function disableFieldDuplicate (  )
            {
	
                window.document.getElementById('submitButtonDuplicate').disabled = true;
                if (window.track > 0) {
                    window.document.getElementById('submitButtonChanges').disabled = false;
                }
            }

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
                var bool = $('#generalparameter').is(':visible');

                //plugin must be added with input visible - error NS_ERROR_FAILURE: Failure
                if(!bool){
                    $('#generalparameter').show();
                }
                $('#howto').elrte(opts);
                $('#value').elrte(opts);
                //plugin must be added with input visible - error NS_ERROR_FAILURE: Failure
                if(!bool){
                    $('#generalparameter').hide();
                }
            });
        </script>


        <script type="text/javascript">
            var track = 0;
            function trackChanges(originalValue, newValue, element) {
                if (originalValue != newValue) {
                    window.track = window.track + 1;
                } else {
                    window.track = window.track - 1;
                }
	
                if (window.track > 0) {
                    if (window.document.getElementById('submitButtonDuplicate').disabled)
                        enableField(element);
                } else {
                    window.track = 0;
                    disableField(element);
                }
            }
        </script>

    </head>
    <body>
        <%@ include file="include/function.jsp" %>
        <%@ include file="include/header.jsp" %>
        <div id="body">
            <%
                Connection conn = db.connect();


                try {

                    /*
                     * Filter requests
                     */
                    IApplicationService myApplicationService = appContext.getBean(IApplicationService.class);
                    IBuildRevisionInvariantService buildRevisionInvariantService = appContext.getBean(BuildRevisionInvariantService.class);

                    String SitdmossBugtrackingURL;
                    SitdmossBugtrackingURL = "";

                    /*
                     * Filter requests
                     */
                    Statement stmt = conn.createStatement();
                    Statement stmt1 = conn.createStatement();
                    Statement stmt2 = conn.createStatement();
                    Statement stmt21 = conn.createStatement();
                    Statement stmt22 = conn.createStatement();
                    Statement stmt23 = conn.createStatement();
                    Statement stmt70 = conn.createStatement();
                    Statement stmt71 = conn.createStatement();
                    Statement stmtLastExe = conn.createStatement();
                    String proplist = "";

                    String MySystem = request.getAttribute("MySystem").toString();
                    if (request.getParameter("system") != null && request.getParameter("system").compareTo("") != 0) {
                        MySystem = request.getParameter("system");
                    }

                    String group;
                    if (request.getParameter("Group") != null
                            && request.getParameter("Group").compareTo("All") != 0) {
                        group = request.getParameter("Group");
                    } else {
                        group = new String("%%");
                    }

                    String status;
                    if (request.getParameter("Status") != null
                            && request.getParameter("Status").compareTo("All") != 0) {
                        status = request.getParameter("Status");
                    } else {
                        status = new String("%%");
                    }

                    String test;
                    if (request.getParameter("Test") != null
                            && request.getParameter("Test").compareTo("All") != 0) {
                        test = request.getParameter("Test");
                    } else {
                        test = new String("%%");
                    }

                    String testselected;
                    if (request.getParameter("createTest") != null
                            && request.getParameter("createTest").compareTo("All") != 0) {
                        testselected = request.getParameter("createTest");
                    } else {
                        testselected = new String("%%");

                    }
                    String testcase;
                    if (request.getParameter("TestCase") != null
                            && request.getParameter("TestCase").compareTo("All") != 0) {
                        testcase = request.getParameter("TestCase");
                    } else {
                        testcase = new String("%%");
                    }

                    Boolean tinf;
                    if (request.getParameter("Tinf") != null
                            && request.getParameter("Tinf").compareTo("Y") == 0) {
                        tinf = true;
                    } else {
                        tinf = false;
                    }

                    Boolean searchTc;
                    if (request.getParameter("SearchTc") != null
                            && request.getParameter("SearchTc").compareTo("Y") == 0) {
                        searchTc = true;
                    } else {
                        searchTc = false;
                    }

                    Boolean search;
                    if (request.getParameter("Search") != null
                            && request.getParameter("Search").compareTo("Y") == 0) {
                        search = true;
                    } else {
                        search = false;
                    }

                    Boolean load;
                    if (request.getParameter("Load") != null
                            && request.getParameter("Load").compareTo("Load") == 0) {
                        load = true;
                    } else {
                        load = false;
                    }

                    if (!load
                            && request.getParameter("submitDuplicate") != null
                            && request.getParameter("submitDuplicate").compareTo(
                            "Duplicate") == 0) {
                        load = true;
                    }

                    Statement stQueryTest = conn.createStatement();
                    Statement stQueryTestCase = conn.createStatement();

            %>
            <form action="TestCase.jsp" method="post" name="selectTestCase">
                <table id="arrond">
                    <tr>
                        <td id="arrond">
                            <table>
                                <tr>
                                    <td class="wob">
                                        <table border="0px">
                                            <tr>
                                                <td id="wob" style="width: 50px; font-weight: bold;"><%out.print(dbDocS(conn, "test", "test", "Test"));%></td>
                                                <td  class="wob">
                                                    <select id="filtertest" name="Test" style="width: 200px" OnChange="document.selectTestCase.submit()">
                                                        <%
                                                            String optstyle = "";
                                                            if (test.compareTo("%%") == 0) {
                                                        %><option style="width: 200px" value="All">-- Choose Test --</option>
                                                        <%                                                            }
                                                            ResultSet rsTest = stQueryTest.executeQuery("SELECT Test, active FROM test where Test IS NOT NULL Order by Test asc");
                                                            while (rsTest.next()) {
                                                                if (rsTest.getString("active").equalsIgnoreCase("Y")) {
                                                                    optstyle = "font-weight:bold;";
                                                                } else {
                                                                    optstyle = "font-weight:lighter;";
                                                                }
                                                        %><option style="width: 200px;<%=optstyle%>" value="<%=rsTest.getString("Test")%>" <%=test.compareTo(rsTest.getString("Test")) == 0 ? " SELECTED " : ""%>><%=rsTest.getString("Test")%></option>
                                                        <%
                                                            }
                                                        %>
                                                    </select>
                                                </td>
                                                <td class="wob" style="width: 70px; font-weight: bold;"><%out.print(dbDocS(conn, "testcase", "testcase", "TestCase"));%></td>
                                                <td  class="wob">
                                                    <select id="filtertestcase" name="TestCase" style="width: 500px" OnChange="document.selectTestCase.submit()">
                                                        <%
                                                            if (test.compareTo("%%") == 0) {
                                                        %><option style="width: 200px" value="All">-- Choose Test First --</option>
                                                        <%                                                        } else {
                                                            String sql = "SELECT TestCase, Application,  Description, tcactive FROM testcase where TestCase IS NOT NULL and test like '" + test + "'Order by TestCase asc";
                                                            ResultSet rsTestCase = stQueryTestCase.executeQuery(sql);
                                                            while (rsTestCase.next()) {
                                                                if (rsTestCase.getString("tcactive").equalsIgnoreCase("Y")) {
                                                                    optstyle = "font-weight:bold;";
                                                                } else {
                                                                    optstyle = "font-weight:lighter;";
                                                                }
                                                        %><option style="width: 500px;<%=optstyle%>" value="<%=rsTestCase.getString("TestCase")%>" <%=testcase.compareTo(rsTestCase.getString("TestCase")) == 0 ? " SELECTED " : ""%>><%=rsTestCase.getString("TestCase")%>  [<%=rsTestCase.getString("Application")%>]  : <%=rsTestCase.getString("Description")%></option>
                                                        <%
                                                                }
                                                            }
                                                        %>
                                                    </select>
                                                </td>
                                                <td  class="wob"><input id="loadbutton" class="button" type="submit" name="Load" value="Load"></td>
                                            </tr>
                                        </table>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>    
            </form> 
            <br>
            <%

                ResultSet rs_testcase_general_info = stmt.executeQuery("SELECT t.Test, t.Description, "
                        + "tc.testcase, tc.Description, tc.BehaviorOrValueExpected, Priority, "
                        + "tc.Application, tc.TcActive ,tc.Project, tc.group, tc.status, tc.Comment, tc.HowTo, "
                        + "tc.Ticket, tc.Origine, tc.RefOrigine, tc.FromBuild, tc.FromRev, tc.ToBuild, tc.ToRev, "
                        + "tc.BugID, tc.TargetBuild, tc.TargetRev, tc.creator, tc.implementer, tc.lastModifier, "
                        + "tc.activeQA, tc.activeUAT, tc.activePROD"
                        + " FROM test t, testcase tc"
                        + " WHERE t.test = tc.test"
                        + " AND tc.Test = '" + test + "'"
                        + " AND tc.TestCase = '" + testcase + "'");

                if (rs_testcase_general_info.next()) {

                    test = rs_testcase_general_info.getString("Test");
                    testcase = rs_testcase_general_info.getString("TestCase");
                    group = rs_testcase_general_info.getString("Group");
                    status = rs_testcase_general_info.getString("Status");

                    /**
                     * We can edit the testcase only if User role is TestAdmin
                     * or if role is Test and testcase is not WORKING
                     */
                    boolean canEdit = false;
                    if (request.getUserPrincipal() != null
                            && (request.isUserInRole("TestAdmin")) || ((request.isUserInRole("Test")) && !(status.equalsIgnoreCase("WORKING")))) {
                        canEdit = true;
                    }


            %>


            <table id="generalparameter" class="arrond"
                   <%if (tinf == false) {%> style="display : none" <%} else {%>style="display : table"<%}%> >
                <tr>
                    <td class="separation">
                        <%  if (canEdit) {%>
                        <form method="post" name="DuplicateTestCase" action="DuplicateTestCase">
                            <% }%>
                            <table  class="wob" style="text-align: left; border-collapse: collapse" border="0px" cellpadding="0px" cellspacing="0px">
                                <tr><td colspan="2" class="wob"><h4 style="color : blue">Test Information</h4></td>
                                    <td id="wob"></td><td id="wob"></td>
                                    <td id="wob" align="right"><input id="button2" style="height:18px; width:10px" type="button" value="-" onclick="javascript:setInvisible();"></td>
                                </tr>    
                                <tr id="header"> 
                                    <td class="wob" style="width: 100px"><%out.print(dbDocS(conn, "test", "test", "Test"));%></td>
                                    <td class="wob" style="width: 100px"><%out.print(dbDocS(conn, "testcase", "testcase", "TestCase"));%></td>
                                    <td class="wob" style="width: 960px"><%out.print(dbDocS(conn, "test", "description", "Description"));%></td>
                                </tr>
                                <tr>

                                    <td class="wob"><input id="editTest" style="width: 90px; font-weight: bold;" name="editTest" id="DuplicateTest"
                                                           value="<%=rs_testcase_general_info.getString("Test")%>" onchange="checkFieldDuplicate();"></td>
                                    <td class="wob"><input id="editTestCase" style="width: 90px; font-weight: bold;" name="editTestCase" id="DuplicateTestCase"
                                                           value="<%=rs_testcase_general_info.getString("TestCase")%>"
                                                           onchange="checkFieldDuplicate('<%=rs_testcase_general_info.getString("Test")%>','<%=rs_testcase_general_info.getString("TestCase")%>');">
                                    </td>
                                    <td class="wob"><input id="editDescription" style="width: 950px; background-color: #DCDCDC" name="editDescription" readonly="readonly"
                                                           value="<%=rs_testcase_general_info.getString("Description")%>"></td>
                                    <td class="wob">
                                        <%  if (canEdit) {%>
                                        <input rowspan="2" style=" valign: center" class="_Duplicate" type="submit" name="submitDuplicate"
                                               value="Duplicate" id="submitButtonDuplicate" disabled="disabled">
                                        <% }%>
                                    </td>
                                </tr>
                            </table><br>
                            <%  if (canEdit) {%>
                            <input type="hidden" id="Test" name="Test" value="<%=test%>"> <input type="hidden" id="TestCase" name="TestCase"
                                                                                                 value="<%=testcase%>">
                        </form>
                        <% }%>
                    </td>
                </tr>


                <%  if (canEdit) {%>

                <form method="post" name="UpdateTestCase" action="UpdateTestCase">
                    <% }%> 
                    <tr><td class="separation">
                            <table style="text-align: left; border-collapse: collapse" border="0px" cellpadding="0px" cellspacing="0px">
                                <tr><td class="wob">
                                        <table class="wob" style="text-align: left; border-collapse: collapse" border="0px" cellpadding="0px" cellspacing="0px">
                                            <tr><td class="wob" colspan="2"><h4 style="color : blue">TestCase Information</h4></td></tr>
                                            <tr id="header">  
                                                <td class="wob" style="width: 100px"><%out.print(dbDocS(conn, "testcase", "origine", "Origin"));%></td>
                                                <td class="wob" style="width: 100px"><%out.print(dbDocS(conn, "testcase", "RefOrigine", "RefOrigine"));%></td>
                                                <td class="wob" style="width: 100px"><%out.print(dbDocS(conn, "testcase", "Creator", "creator"));%></td>
                                                <td class="wob" style="width: 100px"><%out.print(dbDocS(conn, "testcase", "Implementer", "implementer"));%></td>
                                                <td class="wob" style="width: 100px"><%out.print(dbDocS(conn, "testcase", "LastModifier", "lastModifier"));%></td>
                                                <td class="wob" style="width: 100px"><%out.print(dbDocS(conn, "testcase", "project", "Project"));%></td>
                                                <td class="wob" style="width: 100px"><%out.print(dbDocS(conn, "testcase", "ticket", "Ticket"));%></td>

                                            </tr>
                                            <tr>
                                                <td class="wob"><input readonly="readonly" id="origine" style="width: 90px; background-color: #DCDCDC" name="editOrigine" value="<%=rs_testcase_general_info.getString("Origine")%>"></td>
                                                <td class="wob"><input readonly="readonly" id="reforigine" style="width: 90px;  background-color: #DCDCDC" name="editRefOrigine" value="<%=rs_testcase_general_info.getString("RefOrigine")%>"></td>
                                                <td class="wob"><input readonly="readonly" id="creator" style="width: 90px; background-color: #DCDCDC" name="editCreator" value="<%=rs_testcase_general_info.getString("tc.creator")%>"></td>
                                                <td class="wob"><input id="implementer" style="width: 90px;" name="editImplementer" value="<%=rs_testcase_general_info.getString("tc.implementer") == null ? "" : rs_testcase_general_info.getString("tc.implementer")%>"></td>
                                                <td class="wob"><input readonly="readonly" id="lastModifier" style="width: 90px; background-color: #DCDCDC" name="editLastModifier" value="<%=rs_testcase_general_info.getString("tc.lastModifier")%>"></td>
                                                <td class="wob">
                                                    <% out.print(ComboProject(conn, "editProject", "width: 90px", "project", "", rs_testcase_general_info.getString("tc.project"), "", false, "", ""));%>
                                                </td>
                                                <td class="wob"><input id="ticket" style="width: 90px;" name="editTicket" value="<%=rs_testcase_general_info.getString("Ticket") == null ? "" : rs_testcase_general_info.getString("Ticket")%>"></td>
                                            </tr>
                                        </table>
                                    </td>
                                </tr>
                            </table><br>
                        </td>
                    </tr>
                    <%
                        Statement stmt24 = conn.createStatement();
                        Statement stmt25 = conn.createStatement();
                        Statement stmt26 = conn.createStatement();
                        ResultSet rs_testcasecountrygeneral = stmt24.executeQuery("SELECT value "
                                + " FROM invariant "
                                + " WHERE idname ='COUNTRY'"
                                + " ORDER BY sort asc");

                        ResultSet rs_countrygeneral = stmt25.executeQuery("SELECT Country "
                                + " FROM testcasecountry t "
                                + " join invariant i on i.value=t.country and i.id=4 "
                                + " WHERE Test = '"
                                + rs_testcase_general_info.getString("t.Test")
                                + "'"
                                + " AND TestCase = '"
                                + rs_testcase_general_info.getString("tc.testcase") + "'"
                                + " order by i.sort ");


                        // if (rs_countrygeneral.first()){
                        String countrygeneral = "SELECT a.Test, a.Testcase";
                        int k = 0;
                        rs_testcasecountrygeneral.first();
                        do {
                            countrygeneral = countrygeneral + ", " + rs_testcasecountrygeneral.getString("value");
                        } while (rs_testcasecountrygeneral.next());


                        countrygeneral = countrygeneral + " FROM testcasecountry a ";
                        rs_testcasecountrygeneral.first();
                        do {
                            countrygeneral = countrygeneral + "left outer join (SELECT z"
                                    + k + ".test, z"
                                    + k + ".testcase, z"
                                    + k + ".Country as "
                                    + rs_testcasecountrygeneral.getString("value")
                                    + " from testcasecountry z"
                                    + k + " WHERE Test='"
                                    + rs_testcase_general_info.getString("t.Test")
                                    + "' and TestCase='"
                                    + rs_testcase_general_info.getString("tc.testcase")
                                    + "' and Country= '"
                                    + rs_testcasecountrygeneral.getString("value") + "')b"
                                    + k + " on a.test=b"
                                    + k + ".test and a.testcase=b"
                                    + k + ".testcase ";
                            k++;
                        } while (rs_testcasecountrygeneral.next());

                        countrygeneral = countrygeneral + " WHERE a.test='" + rs_testcase_general_info.getString("t.Test")
                                + "' and a.testcase='"
                                + rs_testcase_general_info.getString("tc.testcase") + "' "
                                + "group by a.test";

                        ResultSet rs_country_gen = stmt26.executeQuery(countrygeneral);



                        String countries = "";
                        if (rs_countrygeneral.first()) {
                            rs_countrygeneral.first();
                            do {
                                countries += rs_countrygeneral.getString("country") + "-";
                            } while (rs_countrygeneral.next());
                        }
                    %>
                    <tr>
                        <td class="separation">
                            <table style="text-align: left; border-collapse: collapse" border="0px" cellpadding="0px" cellspacing="0px">
                                <tr>
                                    <td class="wob">
                                        <table class="wob" style="text-align: left; border-collapse: collapse" border="0px" cellpadding="0px" cellspacing="0px">
                                            <tr><td class="wob"><h4 style="color : blue">TestCase Parameters</h4></td></tr>
                                            <tr id="header">
                                                <td class="wob" style="width: 150px"><%out.print(dbDocS(conn, "testcase", "application", "Application"));%></td>
                                                <td class="wob" style="width: 90px"><%out.print(dbDocS(conn, "testcase", "runqa", "RunQA"));%></td>
                                                <td class="wob" style="width: 90px"><%out.print(dbDocS(conn, "testcase", "runuat", "RunUAT"));%></td>
                                                <td class="wob" style="width: 90px"><%out.print(dbDocS(conn, "testcase", "runprod", "RunPROD"));%></td>
                                                <td class="wob" style="width: 90px"><%out.print(dbDocS(conn, "testcase", "priority", "Priority"));%></td>
                                                <td class="wob" style="width: 150px"><%out.print(dbDocS(conn, "testcase", "group", "Group"));%></td>
                                                <td class="wob" style="width: 150px"><%out.print(dbDocS(conn, "testcase", "status", "Status"));%></td>
                                                <%
                                                    rs_testcasecountrygeneral.first();
                                                    do {%>
                                                <td class="wob" style="font-size : x-small ; width: 260px;"><%=rs_testcasecountrygeneral.getString("value")%> <input type="hidden" name="testcase_country_all" value="<%=rs_testcasecountrygeneral.getString("value")%>"></td>
                                                    <% 		} while (rs_testcasecountrygeneral.next());
                                                    %>
                                            </tr>
                                            <tr>
                                                <td class="wob"><select id="application" name="editApplication" style="width: 140px"><%
                                                    ResultSet rsApp = stmt21.executeQuery(" SELECT distinct application from application where application != '' order by sort ");
                                                    while (rsApp.next()) {
                                                        %><option value="<%=rsApp.getString("application")%>"<%=rs_testcase_general_info.getString("Application").compareTo(rsApp.getString("application")) == 0 ? " SELECTED " : ""%>><%=rsApp.getString("application")%></option><%
                                                            }
                                                        %></select></td>
                                                <td class="wob"><%=ComboInvariant(conn, "editRunQA", "width: 75px", "editRunQA", "runqa", "26", rs_testcase_general_info.getString("activeQA"), "", null)%></td>
                                                <td class="wob"><%=ComboInvariant(conn, "editRunUAT", "width: 75px", "editRunUAT", "runuat", "27", rs_testcase_general_info.getString("activeUAT"), "", null)%></td>
                                                <td class="wob"><%=ComboInvariant(conn, "editRunPROD", "width: 75px", "editRunPROD", "runprod", "28", rs_testcase_general_info.getString("activePROD"), "", null)%></td>
                                                <td class="wob"><%=ComboInvariant(conn, "editPriority", "width: 75px", "editPriority", "priority", "15", rs_testcase_general_info.getString("Priority"), "", null)%></td>
                                                <td class="wob"><%=ComboInvariant(conn, "editGroup", "width: 140px", "editGroup", "editgroup", "2", group, "", null)%></td>
                                                <td class="wob"><%=ComboInvariant(conn, "editStatus", "width: 140px", "editStatus", "editStatus", "1", status, "", null)%></td>
                                                <%
                                                    if (rs_country_gen.next()) {
                                                        rs_country_gen.first();
                                                        rs_testcasecountrygeneral.first();
                                                        do {
                                                %>
                                                <td class="wob" style="width:1px"><input value="<%=rs_testcasecountrygeneral.getString("value")%>" type="checkbox" <% if (StringUtils.isNotBlank(rs_country_gen.getString(rs_testcasecountrygeneral.getString("value")))) {%>  CHECKED  <% }%>
                                                                                         name="testcase_country_general"></td> 
                                                    <%} while (rs_testcasecountrygeneral.next());
                                                    } else {
                                                        rs_testcasecountrygeneral.first();
                                                        do {
                                                    %> 
                                                <td class="wob" style="width:1px"><input value="<%=rs_testcasecountrygeneral.getString("value")%>" type="checkbox" name="testcase_country_general"></td>
                                                    <%} while (rs_testcasecountrygeneral.next());
                                                        }%>

                                            </tr>
                                        </table>
                                    </td>
                                </tr>
                            </table>
                            <%
                                String howTo = rs_testcase_general_info.getString("HowTo");
                                if (howTo == null || howTo.compareTo("null") == 0) {
                                    howTo = new String(" ");
                                } else {
                                    howTo = howTo.replace(">", "&gt;");
                                }
                                String behavior = rs_testcase_general_info.getString("BehaviorOrValueExpected");
                                if (behavior == null || behavior.compareTo("null") == 0) {
                                    behavior = new String(" ");
                                } else {
                                    behavior = behavior.replace(">", "&gt;");
                                }
                            %> 
                            <table>
                                <tr>
                                    <td class="wob" style="text-align: left; vertical-align : top ; border-collapse: collapse">
                                        <table class="wob"  style="text-align: left; border-collapse: collapse" border="0px" cellpadding="0px" cellspacing="0px">
                                            <tr id="header">
                                                <td class="wob" style="width: 1200px"><%out.print(dbDocS(conn, "testcase", "description", "Description"));%></td>
                                            </tr><tr>
                                                <td class="wob"><input id="desc" style="width: 1200px;" name="editDescription"
                                                                       value="<%=rs_testcase_general_info.getString("tc.Description")%>"></td>
                                            </tr>
                                        </table>
                                    </td>
                                </tr><tr>
                                    <td class="wob" style="text-align: left; vertical-align : top ; border-collapse: collapse">
                                        <table>   
                                            <tr id="header">
                                                <td class="wob" style="width: 600px"><%out.print(dbDocS(conn, "testcase", "ValueExpected", "Value Expected"));%></td>
                                                <td class="wob" style="width: 600px"><%out.print(dbDocS(conn, "testcase", "HowTo", "HowTo"));%></td>
                                            </tr>
                                            <tr>
                                                <td class="wob" style="text-align: left; border-collapse: collapse">

                                                    <textarea id="value" rows="9" style="width: 600px;" name="BehaviorOrValueExpected" value="<%=behavior.trim()%>"
                                                              onchange="trackChanges(this.value,'<%=URLEncoder.encode(behavior, "UTF-8")%>', 'submitButtonChanges')" ><%=behavior%></textarea>
                                                    <input type="hidden" id="valueDetail" name="valueDetail" value="">
                                                </td>
                                                <td class="wob">
                                                    <textarea id="howto" rows="9" style="width: 600px;" name="HowTo" value="<%=howTo.trim()%>"
                                                              onchange="trackChanges(this.value,'<%=URLEncoder.encode(howTo, "UTF-8")%>', 'submitButtonChanges')" ><%=howTo%></textarea>
                                                    <input id="howtoDetail" name="howtoDetail" type="hidden" value="" />
                                                </td>
                                            </tr>
                                        </table><br>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                    <%

                        // We are getting here the last execution that was done on the testcase with its associated status.
                        String LastExeSQL;
                        String LastExeMessage;
                        LastExeMessage = "<i>Never Executed</i>";
                        LastExeSQL = "SELECT ID, Environment, Country, Build, Revision, End, ControlStatus  FROM `testcaseexecution` "
                                + " WHERE test='" + rs_testcase_general_info.getString("Test") + "' "
                                + "and TestCase='" + rs_testcase_general_info.getString("TestCase") + "' "
                                + "and ID= "
                                + " (SELECT MAX(ID) from `testcaseexecution` "
                                + "WHERE test='" + rs_testcase_general_info.getString("Test") + "' "
                                + "and TestCase='" + rs_testcase_general_info.getString("TestCase") + "' "
                                + "and ControlStatus!='PE')";
                        ResultSet rs_testcase_last_exe = stmtLastExe.executeQuery(LastExeSQL);
                        if (rs_testcase_last_exe.first()) {
                            LastExeMessage = "Last <a width : 390px ; href=\"ExecutionDetail.jsp?id_tc=" + rs_testcase_last_exe.getString("ID") + "\">Execution</a> was ";
                            if (rs_testcase_last_exe.getString("ControlStatus").compareToIgnoreCase("OK") == 0) {
                                LastExeMessage = LastExeMessage + "<a style=\"color : green\">" + rs_testcase_last_exe.getString("ControlStatus") + "</a>";
                            } else {
                                LastExeMessage = LastExeMessage + "<a style=\"color : red\">" + rs_testcase_last_exe.getString("ControlStatus") + "</a>";
                            }
                            LastExeMessage = LastExeMessage + " in "
                                    + rs_testcase_last_exe.getString("Environment") + " in "
                                    + rs_testcase_last_exe.getString("Country") + " on "
                                    + rs_testcase_last_exe.getString("End");
                        }
                        if ((rs_testcase_general_info.getString("tc.BugID") != null)
                                && (rs_testcase_general_info.getString("tc.BugID").compareToIgnoreCase("") != 0)
                                && (rs_testcase_general_info.getString("tc.BugID").compareToIgnoreCase("null") != 0)) {
                            SitdmossBugtrackingURL = myApplicationService.findApplicationByKey(rs_testcase_general_info.getString("tc.Application")).getBugTrackerUrl();
                            SitdmossBugtrackingURL = SitdmossBugtrackingURL.replaceAll("%bugid%", rs_testcase_general_info.getString("tc.BugID"));
                        }
                    %>
                    <tr>
                        <td class="separation">
                            <table class="wob" style="text-align: left; border-collapse: collapse" border="0px" cellpadding="0px" cellspacing="0px">
                                <tr><td colspan="9" class="wob"><h4 style="color : blue">Activation Criterias</h4>
                                <tr id="header">
                                    <td class="wob" style="width: 50px"><%out.print(dbDocS(conn, "testcase", "tcactive", "Active"));%></td>
                                    <td class="wob" style="width: 90px"><%out.print(dbDocS(conn, "testcase", "FromBuild", ""));%></td>
                                    <td class="wob" style="width: 100px"><%out.print(dbDocS(conn, "testcase", "FromRev", ""));%></td>
                                    <td class="wob" style="width: 90px"><%out.print(dbDocS(conn, "testcase", "ToBuild", ""));%></td>
                                    <td class="wob" style="width: 100px"><%out.print(dbDocS(conn, "testcase", "ToRev", ""));%></td>
                                    <td class="wob" style="width: 390px"><%out.print(dbDocS(conn, "page_testcase", "laststatus", ""));%></td>
                                    <td class="wob" style="width: 70px"><%out.print(dbDocS(conn, "testcase", "BugID", ""));%></td>
                                    <td class="wob" style="width: 50px"><%out.print(dbDocS(conn, "page_testcase", "BugIDLink", ""));%></td>
                                    <td class="wob" style="width: 80px"><%out.print(dbDocS(conn, "testcase", "TargetBuild", ""));%></td>
                                    <td class="wob" style="width: 80px"><%out.print(dbDocS(conn, "testcase", "TargetRev", ""));%></td>

                                </tr>
                                <tr>
                                    <td class="wob"><%=ComboInvariant(conn, "editTcActive", "width: 50px", "editTcActive", "active", "16", rs_testcase_general_info.getString("TcActive"), "", null)%></td>
                                    <td class="wob">
                                        <select id="editFromBuild" name="editFromBuild" class="active" style="width: 70px" >
                                            <% String fromBuild = "";
                                                if (rs_testcase_general_info.getString("tc.FromBuild") != null) {
                                                    fromBuild = rs_testcase_general_info.getString("tc.FromBuild");
                                                }
                                                String fromRev = "";
                                                if (rs_testcase_general_info.getString("tc.FromRev") != null) {
                                                    fromRev = rs_testcase_general_info.getString("tc.FromRev");
                                                }
                                                String toBuild = "";
                                                if (rs_testcase_general_info.getString("tc.ToBuild") != null) {
                                                    toBuild = rs_testcase_general_info.getString("tc.ToBuild");
                                                }
                                                String toRev = "";
                                                if (rs_testcase_general_info.getString("tc.ToRev") != null) {
                                                    toRev = rs_testcase_general_info.getString("tc.ToRev");
                                                }
                                                String targetBuild = "";
                                                if (rs_testcase_general_info.getString("tc.TargetBuild") != null) {
                                                    targetBuild = rs_testcase_general_info.getString("tc.TargetBuild");
                                                }
                                                String targetRev = "";
                                                if (rs_testcase_general_info.getString("tc.TargetRev") != null) {
                                                    targetRev = rs_testcase_general_info.getString("tc.TargetRev");
                                                }
                                            %>
                                            <option style="width: 100px" value="" <%=fromBuild.compareTo("") == 0 ? " SELECTED " : ""%>>----</option>
                                            <%
                                                List<BuildRevisionInvariant> listBuildRev = buildRevisionInvariantService.findAllBuildRevisionInvariantBySystemLevel(MySystem, 1);
                                                for (BuildRevisionInvariant myBR : listBuildRev) {
                                            %><option style="width: 100px" value="<%= myBR.getVersionName()%>" <%=fromBuild.compareTo(myBR.getVersionName()) == 0 ? " SELECTED " : ""%>><%= myBR.getVersionName()%></option>
                                            <% }
                                            %></select>
                                    </td>
                                    <td class="wob">
                                        <select id="editFromRev" name="editFromRev" class="active" style="width: 50px" >
                                            <option style="width: 100px" value="" <%=fromRev.compareTo("") == 0 ? " SELECTED " : ""%>>----</option>
                                            <%
                                                listBuildRev = buildRevisionInvariantService.findAllBuildRevisionInvariantBySystemLevel(MySystem, 2);
                                                for (BuildRevisionInvariant myBR : listBuildRev) {
                                            %><option style="width: 100px" value="<%= myBR.getVersionName()%>" <%=fromRev.compareTo(myBR.getVersionName()) == 0 ? " SELECTED " : ""%>><%= myBR.getVersionName()%></option>
                                            <% }
                                            %></select>
                                    </td>
                                    <td class="wob">
                                        <select id="editToBuild" name="editToBuild" class="active" style="width: 70px" >
                                            <option style="width: 100px" value="" <%=toBuild.compareTo("") == 0 ? " SELECTED " : ""%>>----</option>
                                            <%
                                                listBuildRev = buildRevisionInvariantService.findAllBuildRevisionInvariantBySystemLevel(MySystem, 1);
                                                for (BuildRevisionInvariant myBR : listBuildRev) {
                                            %><option style="width: 100px" value="<%= myBR.getVersionName()%>" <%=toBuild.compareTo(myBR.getVersionName()) == 0 ? " SELECTED " : ""%>><%= myBR.getVersionName()%></option>
                                            <% }
                                            %></select>
                                    </td>
                                    <td class="wob">
                                        <select id="editToRev" name="editToRev" class="active" style="width: 50px" >
                                            <option style="width: 100px" value="" <%=toRev.compareTo("") == 0 ? " SELECTED " : ""%>>----</option>
                                            <%
                                                listBuildRev = buildRevisionInvariantService.findAllBuildRevisionInvariantBySystemLevel(MySystem, 2);
                                                for (BuildRevisionInvariant myBR : listBuildRev) {
                                            %><option style="width: 100px" value="<%= myBR.getVersionName()%>" <%=toRev.compareTo(myBR.getVersionName()) == 0 ? " SELECTED " : ""%>><%= myBR.getVersionName()%></option>
                                            <% }
                                            %></select>
                                    </td>
                                    <td class="wob"><%=LastExeMessage%></td>
                                    <td class="wob"><input id="BugID" style="width: 70px;" name="editBugID" value="<%=rs_testcase_general_info.getString("tc.BugID") == null ? "" : rs_testcase_general_info.getString("tc.BugID")%>"></td>
                                    <td class="wob"><% if (rs_testcase_general_info.getString("tc.BugID") != null) {%><a href="<%= SitdmossBugtrackingURL%>"><%=rs_testcase_general_info.getString("tc.BugID")%></a><%}%></td>
                                    <td class="wob">
                                        <select id="editTargetBuild" name="editTargetBuild" class="active" style="width: 70px" >
                                            <option style="width: 100px" value="" <%=targetBuild.compareTo("") == 0 ? " SELECTED " : ""%>>----</option>
                                            <%
                                                listBuildRev = buildRevisionInvariantService.findAllBuildRevisionInvariantBySystemLevel(MySystem, 1);
                                                for (BuildRevisionInvariant myBR : listBuildRev) {
                                            %><option style="width: 100px" value="<%= myBR.getVersionName()%>" <%=targetBuild.compareTo(myBR.getVersionName()) == 0 ? " SELECTED " : ""%>><%= myBR.getVersionName()%></option>
                                            <% }
                                            %></select>
                                    </td>
                                    <td class="wob">
                                        <select id="editTargetRev" name="editTargetRev" class="active" style="width: 50px" >
                                            <option style="width: 100px" value="" <%=targetRev.compareTo("") == 0 ? " SELECTED " : ""%>>----</option>
                                            <%
                                                listBuildRev = buildRevisionInvariantService.findAllBuildRevisionInvariantBySystemLevel(MySystem, 2);
                                                for (BuildRevisionInvariant myBR : listBuildRev) {
                                            %><option style="width: 100px" value="<%= myBR.getVersionName()%>" <%=targetRev.compareTo(myBR.getVersionName()) == 0 ? " SELECTED " : ""%>><%= myBR.getVersionName()%></option>
                                            <% }
                                            %></select>
                                    </td>

                                </tr>
                            </table>
                            <table>
                                <tr id ="header">
                                    <td class="wob" style="width: 650px"><%out.print(dbDocS(conn, "testcase", "comment", "Comment"));%></td>
                                </tr><tr> 
                                    <td class="wob"><input id="comment" style="width: 640px;" name="editComment" 
                                                           value="<%=rs_testcase_general_info.getString("tc.Comment") == null ? "" : rs_testcase_general_info.getString("tc.Comment")%>"></td></tr>

                            </table>
                        </td>
                    </tr>
                    <tr>
                        <td class="wob">

                            <%  if (canEdit) {%>

                            <input type="hidden" id="Test" name="Test" value="<%=test%>"> 
                            <input type="hidden" id="TestCase" name="TestCase" value="<%=testcase%>">
                            <table>
                                <tr>
                                    <td class="wob"><input type="submit" name="submitInformation" value="Save TestCase Info" id="submitButtonInformation" onclick="$('#howtoDetail').val($('#howto').elrte('val'));$('#valueDetail').val($('#value').elrte('val'));"></td>
                                </tr>
                            </table>
                </form>

                <% }%>
                </td>
                </tr>
            </table>
            <table id="parametergeneral" class="arrond" <%if (tinf == false) {%> style="display : table" <%} else {%>style="display : none"<%}%> >
                <tr><td id="wob" style="width: 150px"><h4 style="color : blue">General Parameters:</h4></td>
                    <td id="wob" style="width: 150px">APP: [<%=rs_testcase_general_info.getString("Application")%>]  </td>
                    <td id="wob" style="width: 160px">GROUP: [<%=rs_testcase_general_info.getString("tc.group")%>]  </td>
                    <td id="wob" style="width: 200px">STATUS: [<%=rs_testcase_general_info.getString("tc.status")%>]  </td>
                    <td id="wob" style="width: 60px">ACT: [<%=rs_testcase_general_info.getString("TcActive")%>]  </td>
                    <td id="wob" style="width: 170px">Last Exe: [<%=LastExeMessage%>]  </td>
                    <td id="wob" style="width: 300px">Countries: [<%=countries%>]</td>
                    <td id="wob" align="right"><input id="button1" style="height:18px; width:10px" type="button" value="+" onclick="javascript:setVisible();"></td>
                </tr>
            </table>
            <div id="table">
                <%  if (canEdit) {%>
                <form id="select_country_properties" action="TestCase.jsp"
                      method="post">
                    <% }%>

                    <%
                        /*
                         * Get different country properties defined
                         */
                        Statement stmt5 = conn.createStatement();
                        ResultSet rs_testcasecountry = stmt5.executeQuery("SELECT DISTINCT Country"
                                + " FROM testcasecountry t "
                                + " join invariant i on i.value=t.country and i.id=4 "
                                + " WHERE Test = '"
                                + rs_testcase_general_info.getString("t.Test")
                                + "'"
                                + " AND TestCase = '"
                                + rs_testcase_general_info.getString("tc.testcase") + "'"
                                + " order by i.sort ");

                        if (rs_testcasecountry.next()) {
                            /*
                             * Get all countries defines for this
                             * testcase and put them into combo box Use
                             * for JS line adding
                             */
                            //Define the size of the row country and Value  
                            rs_testcasecountry.last();
                            int size = rs_testcasecountry.getRow() * 16 + 30;
                            int size2 = 570 + 80 - size;

                            // Define the list of country available for this test
                            rs_testcasecountry.first();
                            String tc_countries = "";
                            do {
                                tc_countries += rs_testcasecountry.getString("Country") + "-";
                            } while (rs_testcasecountry.next());
                    %>
                    <br>
                    <input type="hidden" id="tc_countries_for_js_add"
                           value="<%=tc_countries%>">
                    <%
                        rs_testcasecountry.first();
                    %>
                    <input type="hidden" name="testcase_for_country_hidden"
                           value="<%=rs_testcase_general_info.getString("t.Test")
                                   + " - "
                                   + rs_testcase_general_info.getString("tc.testcase")%>">
                </form>
                <%
                    /*
                     * Get country selected for properties display, init
                     * with the first value
                     */
                    Statement stmt7 = conn.createStatement();
                    Statement stmt18 = conn.createStatement();

                    ResultSet rs_tccountry = stmt18.executeQuery("SELECT DISTINCT Country"
                            + " FROM testcasecountry t "
                            + " join invariant i on i.value=t.country and i.id=4 "
                            + " WHERE Test = '"
                            + rs_testcase_general_info.getString("t.Test")
                            + "'"
                            + " AND TestCase = '"
                            + rs_testcase_general_info.getString("tc.testcase") + "'"
                            + " order by i.sort");
                    int rs_properties_maxlength_cpt = 1;
                    /*
                     * ResultSet rs_properties = stmt6
                     * .executeQuery("SELECT Country, Property, Type,
                     * Value, " + "Length, RowLimit, Nature " + " FROM
                     * TestCaseCountryProperties" + " WHERE Test = '" +
                     * rs_testcase_general_info .getString("t.Test") +
                     * "'" + " AND TestCase = '" +
                     * rs_testcase_general_info
                     * .getString("tc.testcase") + "'" + " ORDER BY
                     * Country Asc, Type Desc");
                     */
                    /*
                     * + " AND Country = '" +
                     * testcase_country_property_selected + "'");
                     */



                %>                          
                <%  if (canEdit) {%>
                <form method="post" name="UpdateTestCaseDetail" action="UpdateTestCaseDetail">
                    <% }%>                                   <%--Countries checkbox for adding property (javascript) --%>
                    <input type="hidden" name="testcase_hidden"
                           value="<%=rs_testcase_general_info.getString("t.Test")
                                   + " - "
                                   + rs_testcase_general_info.getString("tc.testcase")%>">

                    <table id="propertytable" class="arrond" style="display : table">
                        <tr>
                            <td id="wob"><h3>TestCase Automation Script</h3></td>
                            <td id="wob"><input id="button3" style="height:18px; width:10px" type="button" value="-" onclick="javascript:setInvisibleP();"></td>
                        </tr>
                        <tr>
                            <td id="wob"><h4>Properties</h4></td>
                        </tr>
                        <tr>
                            <td id="wob"><table><tr><td id="wob" style="width:10px"></td><td id="leftlined"  style="width:10px"></td><td id="underlined">
                                            <table id="testcaseproperties_table" style="text-align: left; border-collapse: collapse"
                                                   border="0">
                                                <tr id="header">
                                                    <td style="width: 30px"><%out.print(dbDocS(conn, "testcasecountryproperties", "delete", "Delete"));%></td>
                                                    <td style="width: 100px"><%out.print(dbDocS(conn, "testcasecountryproperties", "property", "Property"));%></td>
                                                    <td style="width: <%=size%>px"><%out.print(dbDocS(conn, "testcasecountryproperties", "country", "Country"));%></td>
                                                    <td style="width: 120px"><%out.print(dbDocS(conn, "testcasecountryproperties", "type", "Type"));%></td>
                                                    <td style="width: 40px"><%out.print(dbDocS(conn, "testcasecountryproperties", "database", "Database"));%></td>
                                                    <td style="width: <%=size2%>px"><%out.print(dbDocS(conn, "testcasecountryproperties", "value", "Value"));%>
                                                    <td style="width: 40px"><%out.print(dbDocS(conn, "testcasecountryproperties", "length", "Length"));%></td>
                                                    <td style="width: 40px"><%out.print(dbDocS(conn, "testcasecountryproperties", "rowlimit", "RowLimit"));%></td>
                                                    <td style="width: 80px"><%out.print(dbDocS(conn, "testcasecountryproperties", "nature", "Nature"));%></td>
                                                </tr>
                                                <%
                                                    Statement stmt6 = conn.createStatement();
                                                    Statement stmt80 = conn.createStatement();
                                                    String color = "white";
                                                    String color2 = "white";
                                                    String properties_dtb_display = "";
                                                    String properties_dtbdisplay = "";
                                                    int i = 1;
                                                    int j = 1;
                                                    int rowNumber = 0;
                                                    int testcase_proproperties_maxlength_property = 150;
                                                    int testcase_proproperties_maxlength_value = 2500;
                                                    int testcase_proproperties_maxlength_length = 10;
                                                    int testcase_proproperties_maxlength_rowlimit = 10;
                                                    int testcase_proproperties_maxlength_country = 2; // Default max length values for javascript adding if any property set by default

                                                    ResultSet rs_property = stmt6.executeQuery("SELECT Test, TestCase, Country from testcasecountry "
                                                            + "WHERE Test ='"
                                                            + rs_testcase_general_info.getString("t.Test")
                                                            + "' and TestCase ='"
                                                            + rs_testcase_general_info.getString("tc.testcase") + "'"
                                                            + " group by Country");
                                                    if (rs_property.first()) {
                                                        String coun = "SELECT a.Test, a.Testcase, a.Property, a.Type, a.Database,  a.Value, a.Length, a.RowLimit, a.Nature";
                                                        do {
                                                            coun = coun + ", " + rs_property.getString("Country");
                                                        } while (rs_property.next());

                                                        rs_property.first();

                                                        coun = coun + " FROM testcasecountryproperties a ";

                                                        do {
                                                            coun = coun + "left outer join (SELECT z"
                                                                    + j + ".Property, z"
                                                                    + j + ".Type, z"
                                                                    + j + ".Value, z"
                                                                    + j + ".Country as "
                                                                    + rs_property.getString("Country")
                                                                    + " from testcasecountryproperties z"
                                                                    + j + " WHERE Test='"
                                                                    + rs_property.getString("Test")
                                                                    + "' and TestCase='"
                                                                    + rs_property.getString("TestCase") + "' and Country= '"
                                                                    + rs_property.getString("Country") + "')b"
                                                                    + j + " on a.Property=b"
                                                                    + j + ".Property and a.Type=b"
                                                                    + j + ".Type and a.Value=b"
                                                                    + j + ".Value ";
                                                            j++;
                                                        } while (rs_property.next());

                                                        rs_property.first();

                                                        coun = coun + " WHERE Test='" + rs_property.getString("Test")
                                                                + "' and TestCase='"
                                                                + rs_property.getString("TestCase") + "' group by Property, Type, Value";
                                                        ResultSet rs_properties = stmt7.executeQuery(coun);


                                                        int numberOfRow = 0;

                                                        if (rs_properties.first()) {
                                                            do {

                                                                String type_init[] = {"text", "executeSql", "getFromHtmlVisible", "getFromHtml"}; // type for select list
                                                                List<String> type_toselect = new ArrayList<String>();
                                                                type_toselect.add(rs_properties.getString(
                                                                        "a.Type").toUpperCase());

                                                                for (String type_cur : type_init) {
                                                                    if (!type_cur.equals(type_toselect.get(0))) {
                                                                        type_toselect.add(type_cur);
                                                                    }
                                                                }

                                                                String nature_init[] = {"STATIC", "RANDOM",
                                                                    "RANDOMNEW"}; // nature for select list
                                                                List<String> nature_toselect = new ArrayList<String>();
                                                                nature_toselect.add(rs_properties.getString(
                                                                        "a.Nature").toUpperCase());

                                                                for (String nature_cur : nature_init) {
                                                                    if (!nature_cur.equals(nature_toselect.get(0))) {
                                                                        nature_toselect.add(nature_cur);
                                                                    }
                                                                }
                                                                rowNumber = rowNumber + 1;
                                                                proplist = proplist + "" + rs_properties.getString("Property") + "  /  ";

                                                                String sqlDesc = "";
                                                                if (rs_properties.getString("a.Type").equals("executeSqlFromLib")) {

                                                                    String sqlLib = (" SELECT Script from sqllibrary where name = '"
                                                                            + rs_properties.getString("a.Value") + "'");

                                                                    ResultSet rs_sqllib = stmt80.executeQuery(sqlLib);
                                                                    if (rs_sqllib.first()) {
                                                                        sqlDesc = rs_sqllib.getString("Script").replace("'", "\'");
                                                                    }
                                                                }

                                                                if (rs_properties.getString("a.Type").equals("executeSqlFromLib") || rs_properties.getString("a.Type").equals("executeSql")) {
                                                                    properties_dtbdisplay = "inline";
                                                                    properties_dtb_display = "none";
                                                                } else {
                                                                    properties_dtbdisplay = "none";
                                                                    properties_dtb_display = "inline";

                                                                }

                                                                rs_tccountry.first();
                                                                String delete_value = rs_properties.getString("a.Property");
                                                                do {
                                                                    if (rs_properties.getString(rs_tccountry.getString("Country")) != null) {
                                                                        delete_value = delete_value + " - " + rs_properties.getString(rs_tccountry.getString("Country"));
                                                                    }
                                                                } while (rs_tccountry.next());

                                                                int nbline = rs_properties.getString("a.Value").split("\n").length;
                                                                String valueID = rowNumber + "-" + rs_properties.getString("a.Property");
                                                                String typeID = "type" + valueID;


                                                                String showEntireValueB1 = "showEntireValueB1" + valueID;
                                                                String showEntireValueB2 = "showEntireValueB2" + valueID;
                                                                String sqlDetails = "sqlDetails" + valueID;
                                                                String sqlDetailsB1 = "sqlDetailsB1" + valueID;
                                                                String sqlDetailsB2 = "sqlDetailsB2" + valueID;
                                                                String properties_dtbID = "properties_dtb" + valueID;
                                                                String properties_dtb_ID = "properties_dtb_" + valueID;
                                                                String showSqlDetail = "";
                                                                i++;

                                                                j = i % 2;
                                                                if (j == 1) {
                                                                    color = "#f3f6fa";
                                                                    color2 = "white";
                                                                } else {
                                                                    color = "White";
                                                                    color2 = "#f3f6fa";
                                                                }
                                                %>
                                                <tr style="background-color : <%=color%>">
                                                    <td>
                                                        <%  if (canEdit) {%>
                                                        <input name="properties_delete" type="checkbox" style="width: 30px"
                                                               value="<%=delete_value%>"
                                                               onchange="trackChanges(this.defaultChecked, this.checked, 'SavePropertyChanges')">
                                                        <%}%>
                                                        <input type="hidden" name="property_hidden" value="<%=rowNumber%>">
                                                        <% rs_tccountry.first();
                                                            do {%>
                                                        <input type="hidden" name="old_property_hidden" value="<%=rowNumber%> - <%=rs_tccountry.getString("Country")%> - <%=rs_properties.getString("a.Property")%>">
                                                        <% 		} while (rs_tccountry.next());%></td>

                                                    <td><input class="wob" style="width: 100px; font-weight: bold; background-color : <%=color%>"
                                                               name="properties_property"
                                                               value="<%=rs_properties.getString("a.Property")%>"
                                                               onchange="trackChanges(this.value, '<%=rs_properties.getString("a.Property")%>', 'SavePropertyChanges')"></td>
                                                    <td style="font-size : x-small ; width: <%=size%>px;"><table><tr>
                                                                <% rs_tccountry.first();
                                                                    do {%>
                                                                <td class="wob"><%=rs_tccountry.getString("Country")%></td> 
                                                                <% 		} while (rs_tccountry.next());
                                                                %></tr><tr><%
                                                                    rs_tccountry.first();
                                                                    do {
                                                                %>
                                                                <td class="wob"><input value="<%=rowNumber%> - <%=rs_tccountry.getString("Country")%>" type="checkbox" <% if (StringUtils.isNotBlank(rs_properties.getString(rs_tccountry.getString("Country")))) {%>  CHECKED  <% }%>
                                                                                       name="properties_country" onchange="trackChanges(this.value, '<%=rs_properties.getString(rs_tccountry.getString("Country"))%>', 'SavePropertyChanges')"></td>
                                                                    <% //onclick="return false"
                                                                        } while (rs_tccountry.next());
                                                                        rs_tccountry.first();
                                                                    %>
                                                            </tr></table></td>
                                                    <td><%=ComboInvariant(conn, "properties_type", "width: 120px; background-color:" + color, typeID, "wob", "19", rs_properties.getString("a.Type"), "activateDatabaseBox(this.value, '" + properties_dtbID + "' , '" + properties_dtb_ID + "' )", null)%></td>
                                                    <td><%=ComboInvariant(conn, "properties_dtb", "width: 40px; display: " + properties_dtbdisplay + " ; background-color:" + color, properties_dtbID, "wob", "22", rs_properties.getString("a.Database"), "", null)%>
                                                        <input id="<%=properties_dtb_ID%>" style="display:<%=properties_dtb_display%>;  width:39px; background-color: <%=color%>;text-align:center; color: green; font-weight:bolder" class="wob"  value="---">
                                                    </td>

                                                    <td><table><tr><td class="wob" rowspan="2"><textarea id="<%=valueID%>" rows="2" class="wob" style="width: <%=size2%>px; background-color : <%=color%>; " name="properties_value"
                                                                                                         value="<%=rs_properties.getString("a.Value")%>"><%=rs_properties.getString("a.Value")%></textarea>
                                                                    <% if (rs_properties.getString("a.Type").equals("executeSqlFromLib")) {%>
                                                                    <textarea id="<%=sqlDetails%>" rows="5" class="wob" style="display:none ; width: <%=size2%>px; background-color : <%=color%>; color:grey" 
                                                                              readonly="readonly" value="<%=sqlDesc%>"><%=sqlDesc%></textarea>
                                                                    <%}%>
                                                                </td>
                                                                <td class="wob"><input style="display:inline; height:20px; width:20px; background-color: <%=color%>; color:blue; font-weight:bolder" title="Open SQL Library" class="smallbutton" type="button" value="L" onclick="openSqlLibraryPopup('SqlLib.jsp?Lign=', '<%=valueID%>');">
                                                                </td>

                                                            </tr><tr>
                                                                <% if (nbline > 3) {%>
                                                                <td class="wob" style="background-color: <%=color%>; text-align: center; border-left-color:white">
                                                                    <input style="display:inline; height:20px; width:20px; background-color: <%=color%>; color: green; font-weight:bolder" class="smallbutton" title="Show the Full Sql" type="button" value="+" id="<%=showEntireValueB1%>" onclick="showEntireValue('<%=valueID%>', '<%=nbline%>', '<%=showEntireValueB1%>', '<%=showEntireValueB2%>');">
                                                                    <input style="display:none; height:20px; width:20px; background-color: <%=color%>; color: red; font-weight:bolder" class="smallbutton" title="Hide Details" type="button" value="-" id="<%=showEntireValueB2%>" onclick="showLessValue('<%=valueID%>' , '<%=showEntireValueB1%>', '<%=showEntireValueB2%>');">
                                                                </td><%} else {%>
                                                                <td class="wob" style="background-color: <%=color%>; text-align: center; border-left-color:white">

                                                                    <% if (rs_properties.getString("a.Type").equals("executeSqlFromLib")) {%>
                                                                    <input style="display:inline; height:20px; width:20px; background-color: <%=color%>; color: orange; font-weight:bolder" class="smallbutton" type="button" value="e" title="Show the SQL" id="<%=sqlDetailsB1%>" onclick="showSqlDetails('<%=sqlDetails%>', '<%=sqlDetailsB1%>', '<%=sqlDetailsB2%>');">
                                                                    <input style="display:none; height:20px; width:20px; background-color: <%=color%>; color: orange; font-weight:bolder" class="smallbutton" type="button" value="-" title="Hide the SQL" id="<%=sqlDetailsB2%>" onclick="hideSqlDetails('<%=sqlDetails%>', '<%=sqlDetailsB1%>', '<%=sqlDetailsB2%>');">
                                                                </td><%}%><%}%>
                                                            </tr></table></td>
                                                    <td><input class="wob" style="width: 40px; background-color : <%=color%>" name="properties_length"
                                                               value="<%=rs_properties.getString("a.Length")%>"
                                                               onchange="trackChanges(this.value, '<%=rs_properties.getString("a.Length")%>', 'SavePropertyChanges')"
                                                               maxlength="<%=testcase_proproperties_maxlength_length%>">
                                                    </td>
                                                    <td><input class="wob" style="width: 40px; background-color : <%=color%>" name="properties_rowlimit"
                                                               value="<%=rs_properties.getString("a.RowLimit")%>"
                                                               onchange="trackChanges(this.value, '<%=rs_properties.getString("a.RowLimit")%>', 'SavePropertyChanges')"
                                                               maxlength="<%=testcase_proproperties_maxlength_rowlimit%>">
                                                    </td>
                                                    <td><%=ComboInvariant(conn, "properties_nature", "width: 80px; background-color:" + color, "properties_nature", "wob", "20", rs_properties.getString("a.Nature"), "trackChanges(0, this.selectedIndex, 'submitButtonChanges')", null)%></td>
                                                </tr>
                                                <%
                                                                rs_properties_maxlength_cpt = 1; // Reset counter for max length properties

                                                            } while (rs_properties.next());
                                                            rs_properties.close();
                                                        } else {
                                                            rowNumber = rowNumber + 1;
                                                        }
                                                    }
                                                %>
                                            </table><br>
                                            <%  if (canEdit) {%>
                                            <input type="button" value="Add Property" id="AddProperty"
                                                   onclick="addTestCaseProperties('testcaseproperties_table', <%=testcase_proproperties_maxlength_country%>,<%=testcase_proproperties_maxlength_property%>, <%=testcase_proproperties_maxlength_value%>, <%=testcase_proproperties_maxlength_length%>, <%=testcase_proproperties_maxlength_rowlimit%>, <%=rowNumber%>, <%=size%>, <%=size2%> ) ; enableField('SavePropertyChanges'); disableField('AddProperty');">
                                            <input type="submit" value="Save Changes" 
                                                   id="SavePropertyChanges">              
                                            <input type="hidden" id="Test" name="Test" value="<%=test%>">
                                            <input type="hidden" id="TestCase" name="TestCase"
                                                   value="<%=testcase%>">
                                            <input type="hidden" name="testcase_hidden"
                                                   value="<%=rs_testcase_general_info.getString("t.Test")
                                                           + " - "
                                                           + rs_testcase_general_info.getString("tc.testcase")%>">
                                            <input type="hidden" id="CountryList" name="CountryList" value="<%=countries%>">
                                            <%=ComboInvariant(conn, "new_properties_type_new_properties_value", "width: 70px;visibility:hidden", "new_properties_type_new_properties_value", "new_properties_type_new_properties_value", "19", "", "", null)%>
                                            <%=ComboInvariant(conn, "properties_dtb_", "width: 40px;visibility:hidden", "properties_dtb_", "properties_dtb_", "22", "", "", null)%>
                                            <%=ComboInvariant(conn, "properties_nature_", "width: 80px;visibility:hidden", "properties_nature_", "properties_nature_", "20", "", "", null)%>
                                            <input type="hidden" name="testcase_hidden"
                                                   value="<%=rs_testcase_general_info.getString("t.Test")
                                                           + " - "
                                                           + rs_testcase_general_info.getString("tc.testcase")%>">
                                            <input type="hidden" name="testcase_country_hidden"
                                                   value="<%=countries%>">
                                            <% }%></td></table>
                                <p id ="toto" style="font-size : x-small ; width: <%=size%>px;visibility:hidden">
                                    <% rs_tccountry.first();
                                        rowNumber = rowNumber + 1;
                                        do {%>
                                    <%=rs_tccountry.getString("Country")%> 
                                    <%
                                        } while (rs_tccountry.next());%><br><%
                                        rs_tccountry.first();
                                        do {
                                    %>
                                    <input value="<%=rowNumber%> - <%=rs_tccountry.getString("Country")%>" type="checkbox" id="properties_country" 
                                           name="properties_country" >
                                    <% //onclick="return false"
                                        } while (rs_tccountry.next());
                                        rs_tccountry.first();
                                    %>
                                </p>



                            </td></tr>
                        <tr><td id="wob">
                                <h4>Steps</h4>
                                <table><tr><td id="wob" style="width:10px"></td><td id="leftlined">

                                            <div id="table1">

                                                <%
                                                    /*
                                                     * Step / Actions request
                                                     */
                                                    ResultSet rs_step = stmt5.executeQuery("SELECT DISTINCT Test, Testcase, Step, Description "
                                                            + " FROM testcasestep "
                                                            + " WHERE test = '"
                                                            + rs_testcase_general_info.getString("t.Test")
                                                            + "' "
                                                            + " AND testcase = '"
                                                            + rs_testcase_general_info.getString("tc.testcase") + "'");

                                                    int testcase_step_maxlength_desc = 150; // Max length for javascritp if any default value
                                                    int step_loop_number = 0;

                                                    int testcase_stepaction_maxlength_sequence = 10; // Default max length values for javascript adding if any actions set by default
                                                    int testcase_stepaction_maxlength_action = 45;
                                                    int testcase_stepaction_maxlength_object = 100;
                                                    int testcase_stepaction_maxlength_property = 45;
                                                    Integer i1 = 0;
                                                    boolean isStepExist = false;
                                                    if(rs_step.isBeforeFirst()){
                                                        isStepExist = true;
                                                    }
                                                    while (rs_step.next()) { /*
                                                         * Loop on steps
                                                         */
                                                        i1 = i1 + 1;

                                                        testcase_step_maxlength_desc = rs_step.getMetaData().getColumnDisplaySize(4);
                                                %>
                                                <table style="text-align: left; border-collapse: collapse">

                                                    <tr>
                                                        <td id="wob" style="width: 30px; text-align: center; height:20px">

                                                            <%  if (canEdit) {%>
                                                            <input type="checkbox" name="testcasestep_delete" style="font-weight: bold; width:20px"
                                                                   value="<%=rs_step.getString("step")%>">
                                                            <% }%>
                                                        </td><td id="wob">
                                                            <%--Step--%>
                                                            &nbsp;&nbsp;<%=rs_step.getString("step")%>&nbsp;&nbsp; <input
                                                                type="hidden" name="testcasestep_hidden" style="font-weight: bold; width:20px"
                                                                value="<%=rs_step.getString("step")%>"></td><td id="wob">
                                                            <input
                                                                size="100%" style="font-weight: bold; width: 500px" name="step_description"
                                                                value="<%=rs_step.getString("description")%>"
                                                                maxlength="<%=rs_step.getMetaData().getColumnDisplaySize(4)%>">
                                                        </td>
                                                        <%if (0 > 1) {%>					
                                                        <td id="wob"> Batch : 
                                                            <%
                                                                Statement stmt15 = conn.createStatement();
                                                                Statement stmt16 = conn.createStatement();
                                                                ResultSet rs_batch2;
                                                                ResultSet rs_batch = stmt15.executeQuery("Select Batch from batchinvariant");
                                                            %>
                                                            <%     while (rs_batch.next()) {

                                                                    Boolean found = false;
                                                                    rs_batch2 = stmt16.executeQuery("Select Batch from testcasestepbatch where test = '" + rs_step.getString("Test") + "' AND testcase = '" + rs_step.getString("TestCase") + "' AND Step = '" + rs_step.getString("Step") + "' ");%>
                                                            <% while (rs_batch2.next()) {
                                                                    if (rs_batch.getString(1).compareTo(rs_batch2.getString(1)) == 0) {
                                                                        found = true;
                                                                    }

                                                                }

                                                            %>                            		
                                                            <input type="checkbox" id="batch-<%=rs_step.getString("Step")%>" name="batch-<%=rs_step.getString("Step")%>" <%= found ? " checked " : ""%> value="<%= rs_batch.getString(1)%>" onclick="enableField('submitButtonAction');"> <%= rs_batch.getString(1)%>


                                                            <%                                }

                                                            %>

                                                        </td>
                                                        <%}%>
                                                    </tr>


                                                </table>

                                                <table><tr><td id="wob" style="width:10px"></td><td id="leftlined"></td>
                                                        <td id="wob">
                                                            <h5>Actions</h5>

                                                            <%
                                                                Statement stmt66 = conn.createStatement();

                                                                int rs_stepaction_maxlength_cpt = 4;
                                                                ResultSet rs_stepaction = stmt6.executeQuery("SELECT DISTINCT Test, TestCase, Step, Sequence, Action, Object, Property "
                                                                        + " FROM testcasestepaction"
                                                                        + " WHERE Test = '"
                                                                        + rs_step.getString("Test")
                                                                        + "' "
                                                                        + " AND TestCase = '"
                                                                        + rs_step.getString("TestCase")
                                                                        + "' "
                                                                        + " AND Step = "
                                                                        + rs_step.getString("Step") + " ");
                                                            %>
                                                            <table id="<%=step_loop_number%>" style="text-align: left; border-collapse: collapse">
                                                                <tr id="header">
                                                                    <td style="width: 30px"><%out.print(dbDocS(conn, "testcasecountryproperties", "delete", "Delete"));%></td>
                                                                    <td style="width: 60px"><%out.print(dbDocS(conn, "testcasestepaction", "sequence", "Sequence"));%></td>
                                                                    <td style="width: 150px"><%out.print(dbDocS(conn, "testcasestepaction", "action", "Action"));%></td>
                                                                    <td style="width: 680px"><%out.print(dbDocS(conn, "testcasestepaction", "object", "Object"));%></td>
                                                                    <td style="width: 210px"><%out.print(dbDocS(conn, "testcasestepaction", "property", "Property"));%></td>
                                                                </tr>
                                                                <%
                                                                    int a = 1;
                                                                    String actionColor = "";
                                                                    while (rs_stepaction.next()) { /*
                                                                         * Loop on actions
                                                                         */

                                                                        testcase_stepaction_maxlength_sequence = rs_stepaction.getMetaData().getColumnDisplaySize(4);
                                                                        testcase_stepaction_maxlength_action = rs_stepaction.getMetaData().getColumnDisplaySize(5);
                                                                        testcase_stepaction_maxlength_object = rs_stepaction.getMetaData().getColumnDisplaySize(6);
                                                                        testcase_stepaction_maxlength_property = rs_stepaction.getMetaData().getColumnDisplaySize(7);

                                                                        a++;
                                                                        int b;
                                                                        b = a % 2;
                                                                        if (b == 1) {
                                                                            actionColor = "#f3f6fa";
                                                                        } else {
                                                                            actionColor = "White";
                                                                        }
                                                                %>
                                                                <tr>
                                                                    <td style="background-color: <%=actionColor%>">
                                                                        <%  if (canEdit) {%>
                                                                        <input class="wob" type="checkbox" name="actions_delete" style="width: 30px; background-color: <%=actionColor%>"
                                                                               value="<%=rs_stepaction.getString("Step") + "-" + rs_stepaction.getString("Sequence")%>"
                                                                               onchange="trackChanges(this.defaultChecked, this.checked, 'submitButtonAction')">
                                                                        <% }%>
                                                                        <input
                                                                            type="hidden" name="stepnumber_hidden" style="width: 30px"
                                                                            value="<%=rs_stepaction.getString("Step")%>"
                                                                            >
                                                                    </td>
                                                                    <td style="background-color: <%=actionColor%>"><input class="wob" style="width: 60px; font-weight: bold; background-color: <%=actionColor%>; height:20px"
                                                                                                                          value="<%=rs_stepaction.getString("Sequence")%>"
                                                                                                                          name="actions_sequence" readonly="readonly"
                                                                                                                          maxlength="<%=rs_stepaction.getMetaData().getColumnDisplaySize(
                                                                                                                                  rs_stepaction_maxlength_cpt++)%>">
                                                                    </td>
                                                                    <td  style="background-color: <%=actionColor%>"><%=ComboInvariant(conn, "actions_action", "width: 150px; background-color:" + actionColor, "actions_action", "wob", "12", rs_stepaction.getString("Action"), "trackChanges(0, this.selectedIndex, 'submitButtonAction')", null)%></td>
                                                                    <td  style="background-color: <%=actionColor%>"><input class="wob" style="width: 680px; background-color: <%=actionColor%>"
                                                                                                                           value="<%=rs_stepaction.getString("Object")%>"
                                                                                                                           name="actions_object"
                                                                                                                           onchange="trackChanges(this.value, '<%=rs_stepaction.getString("Object")%>', 'submitButtonAction')">
                                                                    </td>
                                                                    <td style="background-color: <%=actionColor%>"><input class="wob" style="width: 210px; background-color: <%=actionColor%>"
                                                                                                                          value="<%=rs_stepaction.getString("Property")%>"
                                                                                                                          name="actions_property"
                                                                                                                          onchange="trackChanges(this.value, '<%=rs_stepaction.getString("Property")%>', 'submitButtonAction')"
                                                                                                                          maxlength="<%=rs_stepaction.getMetaData().getColumnDisplaySize(
                                                                                                                                  rs_stepaction_maxlength_cpt++)%>">
                                                                    </td>
                                                                </tr>
                                                                <%
                                                                        rs_stepaction_maxlength_cpt = 4; // Reset maxlength for actions

                                                                    } /*
                                                                     * End actions loop
                                                                     */
                                                                    rs_stepaction.close();
                                                                    stmt66.close();
                                                                %>
                                                            </table>
                                                            <%  if (canEdit) {%>
                                                            <table><tr><td id="wob"><input type="button" value="Add Action"
                                                                                           onclick="addTestCaseAction('<%=step_loop_number%>', <%=testcase_stepaction_maxlength_sequence%>, <%=testcase_stepaction_maxlength_action%>, <%=testcase_stepaction_maxlength_object%>, <%=testcase_stepaction_maxlength_property%>) ; enableField('submitButtonAction');">
                                                                    <td id="wob"><input type="button" value="import HTML Scenario" onclick="importer('ImportHTML.jsp?Test=<%=test%>&Testcase=<%=testcase%>&Step=<%=rs_step.getString("step")%>')"></td>
                                                                </td><td id="wob"><input value="Save Changes" id="submitButtonAction" name="submitChanges"
                                                                                         type="submit" >
                                                                <%=ComboInvariant(conn, "actions_action_", "width: 150px;visibility:hidden", "actions_action_", "actions_action_", "12", "", "", null)%></td></tr></table></td></tr></table> <br>
                                                                <% }%>
                                                                <%
                                                                        step_loop_number++;
                                                                    } /*
                                                                     * End Step loop
                                                                     */
                                                                    rs_step.close();
                                                                %>
                                    </div>

                                    <%  if (canEdit) {%>
                                    <div id="hide_div"></div>
                                    <table><tr><td id="wob"><input type="button" value="Add Step" id="AddStepButton" style="display:inline"
                                                                   onclick="addStep('hide_div', '<%=step_loop_number%>', <%=testcase_step_maxlength_desc%>, <%=testcase_stepaction_maxlength_sequence%>, <%=testcase_stepaction_maxlength_action%>, <%=testcase_stepaction_maxlength_object%>, <%=testcase_stepaction_maxlength_property%>) ; enableField('submitButtonAction'); hidebutton('AddStepButton')">

                                                <% }%>
                                            </td></tr> </table>  </td></tr></table>          
                    </td></tr>

                <%
                                    if (isStepExist){
                    ResultSet rs_controls = stmt5.executeQuery("SELECT DISTINCT Test, Testcase, Step, Sequence, Control, Type, ControlValue, ControlProperty, Fatal "
                            + " FROM testcasestepactioncontrol "
                            + " WHERE test = '"
                            + rs_testcase_general_info.getString("t.Test")
                            + "' "
                            + " AND testcase = '"
                            + rs_testcase_general_info.getString("tc.testcase") + "'");

                    int testcase_control_maxlength_step = 10; // Default max length values for javascript adding if any controls define for the testcase
                    int testcase_control_maxlength_sequence = 10;
                    int testcase_control_maxlength_control = 10;
                    int testcase_control_maxlength_type = 200;
                    int testcase_control_maxlength_value = 200;
                    int testcase_control_maxlength_property = 200;
                %>
                <tr><td class="wob"><div id="table">
                            <br><br><h4>Controls</h4>
                            <table><tr><td id="wob" style="width:10px"></td><td id="leftlined"  style="width:10px"></td><td id="underlined">
                                        <table id="control_table" style="text-align: left; border-collapse: collapse">
                                            <tbody>
                                                <tr id="header">
                                                    <td style="width: 30px"><%out.print(dbDocS(conn, "testcasecountryproperties", "delete", "Delete"));%></td>
                                                    <td style="width: 30px"><%out.print(dbDocS(conn, "testcasestepactioncontrol", "step", "Step"));%></td>
                                                    <td style="width: 60px"><%out.print(dbDocS(conn, "testcasestepactioncontrol", "sequence", "Sequence"));%></td>
                                                    <td style="width: 60px"><%out.print(dbDocS(conn, "testcasestepactioncontrol", "control", "Control"));%></td>
                                                    <td style="width: 200px"><%out.print(dbDocS(conn, "testcasestepactioncontrol", "type", "Type"));%></td>
                                                    <td style="width: 350px"><%out.print(dbDocS(conn, "testcasestepactioncontrol", "ControleProperty", "Control Property"));%></td>
                                                    <td style="width: 330px"><%out.print(dbDocS(conn, "testcasestepactioncontrol", "ControleValue", "Control Value"));%></td>
                                                    <td style="width: 40px"><%out.print(dbDocS(conn, "testcasestepactioncontrol", "Fatal", "Fatal"));%></td>

                                                </tr>
                                                <%
                                                    int d = 1;
                                                    String controlColor = "white";
                                                    int cpt_control_maxlength = 3;
                                                    while (rs_controls.next()) {
                                                        d++;
                                                        int e;
                                                        e = d % 2;
                                                        if (e == 1) {
                                                            controlColor = "#f3f6fa";
                                                        } else {
                                                            controlColor = "White";
                                                        }

                                                        testcase_control_maxlength_step = rs_controls.getMetaData().getColumnDisplaySize(
                                                                cpt_control_maxlength++);
                                                        testcase_control_maxlength_sequence = rs_controls.getMetaData().getColumnDisplaySize(
                                                                cpt_control_maxlength++);
                                                        testcase_control_maxlength_control = rs_controls.getMetaData().getColumnDisplaySize(
                                                                cpt_control_maxlength++);
                                                        testcase_control_maxlength_type = rs_controls.getMetaData().getColumnDisplaySize(
                                                                cpt_control_maxlength++);
                                                        testcase_control_maxlength_value = rs_controls.getMetaData().getColumnDisplaySize(
                                                                cpt_control_maxlength++);
                                                        testcase_control_maxlength_property = rs_controls.getMetaData().getColumnDisplaySize(
                                                                cpt_control_maxlength++);
                                                %>
                                                <tr>
                                                    <td style="text-align: center; background-color: <%=controlColor%>">
                                                        <%  if (canEdit) {%>
                                                        <input class="wob" type="checkbox" name="controls_delete" 
                                                               value="<%=rs_controls.getString("Step") + '-'
                                                                       + rs_controls.getString("Sequence") + '-'
                                                                       + rs_controls.getString("Control")%>"
                                                               onchange="trackChanges(this.defaultChecked, this.checked, 'submitButtonChanges')" />
                                                        <% }%>
                                                    </td>
                                                    <td style="background-color: <%=controlColor%>"><input class="wob" style="width: 30px; font-weight: bold; height:20px ;background-color: <%=controlColor%>"
                                                                                                           value="<%=rs_controls.getString("Step")%>"
                                                                                                           name="controls_step" readonly="readonly"
                                                                                                           maxlength="<%=testcase_control_maxlength_step%>"></td>
                                                    <td style="background-color: <%=controlColor%>"><input class="wob" style="width: 60px; font-weight: bold;background-color: <%=controlColor%>"
                                                                                                           value="<%=rs_controls.getString("Sequence")%>"
                                                                                                           name="controls_sequence" readonly="readonly"
                                                                                                           maxlength="<%=testcase_control_maxlength_sequence%>"></td>
                                                    <td style="background-color: <%=controlColor%>"><input class="wob" style="width: 60px; font-weight: bold;background-color: <%=controlColor%>"
                                                                                                           value="<%=rs_controls.getString("Control")%>"
                                                                                                           name="controls_control" readonly="readonly"
                                                                                                           maxlength="<%=testcase_control_maxlength_control%>"></td>
                                                    <td style="background-color: <%=controlColor%>"><%=ComboInvariant(conn, "controls_type", "width: 200px", "controls_type", "wob", "13", rs_controls.getString("Type"), "trackChanges(this.value, '" + rs_controls.getString("Type") + "', 'submitButtonChanges')", null)%></td>
                                                    <td style="background-color: <%=controlColor%>"><input class="wob" style="width: 350px;background-color: <%=controlColor%>"
                                                                                                           value="<%=rs_controls.getString("ControlProperty")%>"
                                                                                                           name="controls_controlproperty"
                                                                                                           onchange="trackChanges(this.value, '<%=rs_controls.getString("ControlProperty")%>', 'submitButtonChanges')"
                                                                                                           maxlength="<%=testcase_control_maxlength_property%>"></td>
                                                    <td style="background-color: <%=controlColor%>"><input class="wob" style="width: 360px;background-color: <%=controlColor%>"
                                                                                                           value="<%=rs_controls.getString("ControlValue")%>"
                                                                                                           name="controls_controlvalue"
                                                                                                           onchange="trackChanges(this.value, '<%=rs_controls.getString("ControlValue")%>', 'submitButtonChanges')"
                                                                                                           maxlength="<%=testcase_control_maxlength_value%>"></td>
                                                    <td style="background-color: <%=controlColor%>"><%=ComboInvariant(conn, "controls_fatal", "width: 40px", "controls_fatal", "wob", "18", rs_controls.getString("Fatal"), "trackChanges(this.value, '" + rs_controls.getString("Fatal") + "', 'submitButtonChanges')", null)%></td>
                                                </tr>

                                            </tbody>
                                            <%
                                                    cpt_control_maxlength = 3;
                                                }


                                                ResultSet rs_controls1 = stmt5.executeQuery("SELECT value from invariant where id = 4");

                                                String[] sarray = new String[100];
                                                int m = 0;
                                                if (rs_controls1.next()) {
                                                    sarray[m] = rs_controls1.getString("value");
                                                    m++;
                                                }

                                            %>
                                        </table>
                                        <%  if (canEdit) {%>
                                        <%=ComboInvariant(conn, "controls_type_", "width: 200px;visibility:hidden", "controls_type_", "controls_type_", "13", "", "", null)%>
                                        <%=ComboInvariant(conn, "controls_fatal_", "width: 40px;visibility:hidden", "controls_fatal_", "controls_fatal_", "18", "", "", null)%>
                                        <table><tr><td id="wob"><input type="button"
                                                                       value="Add Control"
                                                                       onclick="addTestCaseControl('control_table', <%=testcase_control_maxlength_step%>, <%=testcase_control_maxlength_sequence%>, <%=testcase_control_maxlength_control%>, <%=testcase_control_maxlength_type%>, <%=testcase_control_maxlength_value%>, <%=testcase_control_maxlength_property%>) ; enableField('submitButtonChanges');">
                                                </td><td id="wob"><input	value="Save changes" id="submitButtonChanges" name="submitChanges"
                                                                         type="submit" ></td></tr></table>
                                                    <% }%>
                                    </td></tr></table>        
                        </div></td></tr></table><% }%>
                        <%  if (canEdit) {%>
            <input type="hidden" id="Test" name="Test" value="<%=test%>">
            <input type="hidden" id="TestCase" name="TestCase"
                   value="<%=testcase%>">

            <input type="hidden" name="testcase_hidden"
                   value="<%=rs_testcase_general_info.getString("t.Test")
                           + " - "
                           + rs_testcase_general_info.getString("tc.testcase")%>">
            <table id="tableproperty" class="arrond" style="display : none" >
                <tr>
                    <td><h4 style="color : blue">TestCase Automation Script:</h4></td>
                    <td>Properties = <%=proplist%></td>
                    <td td align="right"><input id="button4" style="height:18px; width:10px" type="button" value="+" onclick="javascript:setVisibleP();"></td>
                </tr>
            </table>	
        </form>
                    <br>
        <% }%>
        
        <table id="arrond" style="text-align: left" border="1" >
            <tr><td colspan="2"><h4>Contextual Actions</h4></td></tr>
            <tr>
                <td>
                    <a href="RunTests.jsp?Test=<%=rs_testcase_general_info.getString("Test")%>&TestCase=<%=rs_testcase_general_info.getString("TestCase")%>">Run this TestCase.</a>
                </td>
                <td>
                    <a href="ExecutionDetailList.jsp?test=<%=rs_testcase_general_info.getString("Test")%>&testcase=<%=rs_testcase_general_info.getString("TestCase")%>">See Last Executions..</a>
                </td>
            </tr>
        </table>
        <% 
        } else {
        %> <br><table id="nocountrydefined" class="arrond">
            <tr><td class="wob"></td></tr><tr>
                <td class="wob"><h3> To add Properties,Actions and controls, select at least one country in the general parameters </h3></td>
            </tr><tr><td class="wob"></td></tr>
        </table><%                                    }

            rs_testcasecountry.close(); /*
             * Close Country Parameters request
             */
        %>
        <%
                }
                rs_testcase_general_info.close();

                if (stmt != null) {
                    stmt.close();
                }
                if (stmt1 != null) {
                    stmt1.close();
                }
                if (stmt2 != null) {
                    stmt2.close();
                }
                if (stmt21 != null) {
                    stmt21.close();
                }
                if (stmt22 != null) {
                    stmt22.close();
                }

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
</div>
<br><% out.print(display_footer(DatePageStart));%>
</body>
</html>
