<%-- 
    Document   : TestCase
    Created on : 20 mai 2011, 13:41:49
    Author     : acraske
--%>
<%@page import="com.redcats.tst.service.IParameterService"%>
<%@page import="org.springframework.web.context.support.WebApplicationContextUtils"%>
<%@page import="org.springframework.context.ApplicationContext"%>
<%@page import="org.springframework.context.ApplicationContext"%>
<%@page import="org.apache.commons.lang3.StringUtils"%>
<%@page import="com.mysql.jdbc.ResultSetImpl"%>
<%@page import="java.sql.SQLException"%>
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
        <title>TestCase Search</title>

        <link rel="stylesheet" type="text/css" href="css/crb_style.css">
        <link rel="shortcut icon" type="image/x-icon" href="images/favicon.ico" />

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
                    Statement stmt = conn.createStatement();
                    Statement stmt1 = conn.createStatement();
                    Statement stmt2 = conn.createStatement();
                    Statement stmt21 = conn.createStatement();
                    Statement stmt22 = conn.createStatement();
                    Statement stmt70 = conn.createStatement();
                    Statement stmt71 = conn.createStatement();

                    String whereclause = "Where 1=1 ";

                    String scGroup;
                    if (request.getParameter("ScGroup") != null && request.getParameter("ScGroup").compareTo("All") != 0) {
                        scGroup = request.getParameter("ScGroup");
                        whereclause = whereclause + " AND `group` = '" + scGroup + "'";
                    } else {
                        scGroup = new String("%%");
                    }


                    String scTcActive;
                    if (request.getParameter("ScTcActive") != null && request.getParameter("ScTcActive").compareTo("A") != 0) {

                        scTcActive = request.getParameter("ScTcActive");

                        if (request.getParameter("ScTcActive").compareTo("A") == 0) {
                            scTcActive = "%%";
                            whereclause = whereclause + " AND tcActive = '" + scTcActive + "'";
                        }
                    } else {
                        scTcActive = new String("Y");
                    }

                    String scPriority;
                    if (request.getParameter("ScPriority") != null) {
                        if (request.getParameter("ScPriority").compareTo("All") != 0) {
                            scPriority = request.getParameter("ScPriority");
                            whereclause = whereclause + " AND priority = '" + scPriority + "'";
                        } else {
                            scPriority = "%%";
                        }

                    } else {
                        scPriority = new String("%%");
                    }


                    String scProject;
                    if (request.getParameter("ScProject") != null && request.getParameter("ScProject").compareTo("All") != 0) {
                        scProject = request.getParameter("ScProject");
                        whereclause = whereclause + " AND project = '" + scProject + "'";
                    } else {
                        scProject = new String("%%");
                    }

                    String scTest;
                    if (request.getParameter("ScTest") != null && request.getParameter("ScTest").compareTo("All") != 0) {
                        scTest = request.getParameter("ScTest");
                        whereclause = whereclause + " AND test = '" + scTest + "'";
                    } else {
                        scTest = new String("%%");
                    }

                    String scSystem;
                    if (request.getParameter("ScSystem") != null && request.getParameter("ScSystem").compareTo("All") != 0) {
                        scSystem = request.getParameter("ScSystem");
                        whereclause = whereclause + " AND a.system = '" + scSystem + "'";
                    } else {
                        scSystem = new String("%%");
                    }

                    String scApplication;
                    if (request.getParameter("ScApplication") != null && request.getParameter("ScApplication").compareTo("All") != 0) {
                        scApplication = request.getParameter("ScApplication");
                        whereclause = whereclause + " AND t2.application = '" + scApplication + "'";
                    } else {
                        scApplication = new String("%%");
                    }

                    String scStatus;
                    if (request.getParameter("ScStatus") != null && request.getParameter("ScStatus").compareTo("All") != 0) {
                        scStatus = request.getParameter("ScStatus");
                        whereclause = whereclause + " AND status = '" + scStatus + "'";
                    } else {
                        scStatus = new String("%%");
                    }

                    String scTargetBuild = "";
                    if (request.getParameter("ScTargetBuild") != null) {
                        if (request.getParameter("ScTargetBuild").compareTo("All") == 0) {
                            scTargetBuild = "All";
                        } else {
                            if (request.getParameter("ScTargetBuild").equals("NTB")) {
                                scTargetBuild = "";
                            } else {
                                scTargetBuild = request.getParameter("ScTargetBuild");
                                whereclause = whereclause + " AND TargetBuild = '" + scTargetBuild + "'";
                            }
                        }
                    } else {
                        scTargetBuild = "All";
                        //tcclauses = tcclauses + " AND TargetBuild = '' ";
                    }

                    String scTargetRev = "";
                    if (request.getParameter("ScTargetRev") != null) {
                        if (request.getParameter("ScTargetRev").compareTo("All") == 0) {
                            scTargetRev = "All";
                        } else {
                            if (request.getParameter("ScTargetRev").equals("NTR")) {
                                scTargetRev = "";
                            } else {
                                scTargetRev = request.getParameter("ScTargetRev");
                                whereclause = whereclause + " AND TargetRev = '" + scTargetRev + "'";
                            }
                        }
                    } else {
                        scTargetRev = "All";
                        //tcclauses = tcclauses + " AND TargetRev = '' ";
                    }

                    String scOrigine;
                    if (request.getParameter("ScOrigine") != null && request.getParameter("ScOrigine").compareTo("All") != 0) {
                        scOrigine = request.getParameter("ScOrigine");
                        whereclause = whereclause + " AND Origine = '" + scOrigine + "'";
                    } else {
                        scOrigine = new String("%%");
                    }

                    String scRefOrigine;
                    if (request.getParameter("ScRefOrigine") != null && request.getParameter("ScRefOrigine").compareTo("All") != 0) {
                        scRefOrigine = request.getParameter("ScRefOrigine");
                        whereclause = whereclause + " AND reforigine = '" + scRefOrigine + "'";
                    } else {
                        scRefOrigine = new String("%%");
                    }

                    String scBugID;
                    if (request.getParameter("ScBugID") != null && request.getParameter("ScBugID").compareTo("All") != 0) {
                        scBugID = request.getParameter("ScBugID");
                        whereclause = whereclause + " AND BugID = '" + scBugID + "'";
                    } else {
                        scBugID = new String("%%");
                    }

                    String scTicket;
                    if (request.getParameter("ScTicket") != null && request.getParameter("ScTicket").compareTo("All") != 0) {
                        scTicket = request.getParameter("ScTicket");
                        whereclause = whereclause + " AND Ticket = '" + scTicket + "'";
                    } else {
                        scTicket = new String("%%");
                    }

                    String scCreator;
                    if (request.getParameter("ScCreator") != null && request.getParameter("ScCreator").compareTo("All") != 0) {
                        scCreator = request.getParameter("ScCreator");
                        whereclause = whereclause + " AND Creator = '" + scCreator + "'";
                    } else {
                        scCreator = new String("%%");
                    }

                    String scPROD;
                    if (request.getParameter("ScPROD") != null && request.getParameter("ScPROD").compareTo("All") != 0) {
                        scPROD = request.getParameter("ScPROD");
                        whereclause = whereclause + " AND activePROD = '" + scPROD + "'";
                    } else {
                        scPROD = new String("%%");
                    }

                    String scQA;
                    if (request.getParameter("ScQA") != null && request.getParameter("ScQA").compareTo("All") != 0) {
                        scQA = request.getParameter("ScQA");
                        whereclause = whereclause + " AND activeQA = '" + scQA + "'";
                    } else {
                        scQA = new String("%%");
                    }

                    String scUAT;
                    if (request.getParameter("ScUAT") != null && request.getParameter("ScUAT").compareTo("All") != 0) {
                        scUAT = request.getParameter("ScUAT");
                        whereclause = whereclause + " AND activeUAT = '" + scUAT + "'";
                    } else {
                        scUAT = new String("%%");
                    }

                    String scText;
                    if (request.getParameter("ScText") != null && request.getParameter("ScText").compareTo("All") != 0) {
                        scText = request.getParameter("ScText");
                        whereclause = whereclause + " AND ( t2.description like '%" + scText + "%' "
                                + "or t2.BehaviorOrValueExpected like '%" + scText + "%' "
                                + "or t2.HowTo like '%" + scText + "%' "
                                + "or t2.Comment like '%" + scText + "%') ";
                    } else {
                        scText = new String("");
                    }

                    String scActive;
                    if (request.getParameter("ScActive") != null && request.getParameter("ScActive").compareTo("All") != 0) {
                        scActive = request.getParameter("ScActive");
                        whereclause = whereclause + " AND TCactive = '" + scActive + "'";
                    } else {
                        scActive = new String("%%");
                    }

                    String scFBuild;
                    if (request.getParameter("ScFBuild") != null && request.getParameter("ScFBuild").compareTo("All") != 0) {
                        scFBuild = request.getParameter("ScFBuild");
                        whereclause = whereclause + " AND FromBuild = '" + scFBuild + "'";
                    } else {
                        scFBuild = new String("%%");
                    }

                    String scFRev;
                    if (request.getParameter("ScFRev") != null && request.getParameter("ScFRev").compareTo("All") != 0) {
                        scFRev = request.getParameter("ScFRev");
                        whereclause = whereclause + " AND FromRev = '" + scFRev + "'";
                    } else {
                        scFRev = new String("%%");
                    }

                    String scTBuild;
                    if (request.getParameter("ScTBuild") != null && request.getParameter("ScTBuild").compareTo("All") != 0) {
                        scTBuild = request.getParameter("ScTBuild");
                        whereclause = whereclause + " AND ToBuild = '" + scTBuild + "'";
                    } else {
                        scTBuild = new String("%%");
                    }

                    String scTRev;
                    if (request.getParameter("ScTRev") != null && request.getParameter("ScTRev").compareTo("All") != 0) {
                        scTRev = request.getParameter("ScTRev");
                        whereclause = whereclause + " AND ToRev = '" + scTRev + "'";
                    } else {
                        scTRev = new String("%%");
                    }

                    Boolean search;
                    if (request.getParameter("Search") != null
                            && request.getParameter("Search").compareTo("Y") == 0) {
                        search = true;
                    } else {
                        search = false;
                    }

            %>
            <form action="TestCaseSearch.jsp" method="get" name="selectTest">
                <table id="arrond">
                    <tr><td id="arrond"  >
                            <table><tr><td class="wob"><table><tr>
                                                <td id="wob">
                                                    <table>
                                                        <tr id="header">
                                                            <td class="wob" style="width: 100px"><%out.print(dbDocS(conn, "test", "test", "Test"));%></td>
                                                            <td class="wob" style="width: 100px"><%out.print(dbDocS(conn, "testcase", "project", "Project"));%></td>
                                                            <td class="wob" style="width: 100px"><%out.print(dbDocS(conn, "testcase", "ticket", "Ticket"));%></td>
                                                            <td class="wob" style="width: 70px"><%out.print(dbDocS(conn, "testcase", "BugID", ""));%></td>
                                                            <td class="wob" style="width: 100px"><%out.print(dbDocS(conn, "testcase", "origine", "Origine"));%></td>
                                                            <td class="wob" style="width: 100px"><%out.print(dbDocS(conn, "testcase", "creator", "Creator"));%></td>
                                                            <td class="wob" style="width: 150px"><%out.print(dbDocS(conn, "application", "system", "System"));%></td>
                                                            <td class="wob" style="width: 150px"><%out.print(dbDocS(conn, "testcase", "application", "Application"));%></td>
                                                            <td class="wob" style="width: 150px"><%out.print(dbDocS(conn, "testcase", "priority", ""));%></td>
                                                            <td class="wob" style="width: 150px"><%out.print(dbDocS(conn, "testcase", "status", ""));%></td>
                                                        </tr>
                                                        <tr>
                                                            <td class="wob">
                                                                <select id="ScTest" name="ScTest" style="width: 100px">
                                                                    <option value="All">-- ALL --</option><%
                                                                        String optstyle = "";
                                                                        ResultSet rsTest = stmt70.executeQuery("SELECT test , description, active from test ORDER BY test ");
                                                                        while (rsTest.next()) {
                                                                            if (rsTest.getString("active").equalsIgnoreCase("Y")) {
                                                                                optstyle = "font-weight:bold;";
                                                                            } else {
                                                                                optstyle = "font-weight:lighter;";
                                                                            }
                                                                    %><option style="width: 500px;<%=optstyle%>" value="<%=rsTest.getString("test")%>"<%= scTest.compareTo(rsTest.getString("test")) == 0 ? " SELECTED " : ""%>><%=rsTest.getString("Test")%></option><%
                                                                        }
                                                                    %></select>
                                                            </td>
                                                            <td class="wob">
                                                                <% out.print(ComboProject(conn, "ScProject", "width: 90px", "ScProject", "", scProject, "", true, "All", "-- ALL --"));%>
                                                            </td>
                                                            <td class="wob">
                                                                <select id="ScTicket" name="ScTicket" style="width: 100px">
                                                                    <option value="All">-- ALL --</option><%
                                                                        ResultSet rsTicket = stmt70.executeQuery("SELECT Ticket FROM testcase WHERE Ticket is not null GROUP BY ticket ORDER BY ticket");
                                                                        while (rsTicket.next()) {
                                                                    %><option value="<%=rsTicket.getString("Ticket")%>"<%= scTicket.compareTo(rsTicket.getString("Ticket")) == 0 ? " SELECTED " : ""%>><%=rsTicket.getString("Ticket")%></option><%
                                                                        }
                                                                    %></select>
                                                            </td>
                                                            <td class="wob">
                                                                <select id="ScBugID" name="ScBugID" style="width: 70px">
                                                                    <option value="All">-- ALL --</option><%
                                                                        ResultSet rsBugID = stmt70.executeQuery(" SELECT distinct BugID from testcase where bugid is not null ORDER BY bugid ");
                                                                        while (rsBugID.next()) {
                                                                    %><option value="<%=rsBugID.getString("BugID")%>"<%=scBugID.compareTo(rsBugID.getString("BugID")) == 0 ? " SELECTED " : ""%>><%=rsBugID.getString("BugID")%></option><%
                                                                        }
                                                                    %></select>
                                                            </td>
                                                            <td class="wob"><select id="ScOrigine" name="ScOrigine" style="width: 90px;">
                                                                    <option value="All">-- ALL --</option><%
                                                                        ResultSet rsOri = stmt70.executeQuery(" SELECT Distinct Origine from testcase where origine is not null ORDER BY origine ");
                                                                        while (rsOri.next()) {
                                                                    %><option value="<%=rsOri.getString("Origine")%>"<%=scOrigine.compareTo(rsOri.getString("Origine")) == 0 ? " SELECTED " : ""%>><%=rsOri.getString("Origine")%></option><%
                                                                        }
                                                                    %></select></td>
                                                            <td class="wob"><select id="ScCreator" name="ScCreator" style="width: 90px;">
                                                                    <option value="All">-- ALL --</option><%
                                                                        ResultSet rsCreator = stmt70.executeQuery(" SELECT Distinct Creator from testcase where creator is not null ORDER BY creator ");
                                                                        while (rsCreator.next()) {
                                                                    %><option value="<%=rsCreator.getString("Creator")%>"<%=scCreator.compareTo(rsCreator.getString("Creator")) == 0 ? " SELECTED " : ""%>><%=rsCreator.getString("Creator")%></option><%
                                                                        }
                                                                    %></select></td>
                                                            <td class="wob"><select id="ScSystem" name="ScSystem" style="width: 90px">
                                                                    <option value="All">-- ALL --</option><%
                                                                        ResultSet rsSys = stmt70.executeQuery("SELECT distinct system from application order by system ");
                                                                        while (rsSys.next()) {
                                                                    %><option value="<%=rsSys.getString("system")%>"<%=scSystem.compareTo(rsSys.getString("system")) == 0 ? " SELECTED " : ""%>><%=rsSys.getString("system")%></option><%
                                                                        }
                                                                    %></select></td>
                                                            <td class="wob"><select id="ScApplication" name="ScApplication" style="width: 140px">
                                                                    <option value="All">-- ALL --</option><%
                                                                        ResultSet rsAppl = stmt70.executeQuery(" SELECT distinct application, system from application where application != '' order by sort ");
                                                                        while (rsAppl.next()) {
                                                                    %><option value="<%=rsAppl.getString("application")%>"<%=scApplication.compareTo(rsAppl.getString("application")) == 0 ? " SELECTED " : ""%>><%=rsAppl.getString("application")%> [<%=rsAppl.getString("system")%>]</option><%
                                                                        }
                                                                    %></select></td>
                                                            <td class="wob"><select id="ScPriority" name="ScPriority" style="width: 60px">
                                                                    <option value="All">-- ALL --</option><%
                                                                        ResultSet rsPrio = stmt70.executeQuery(" SELECT value, description from invariant where id=15 order by sort ");
                                                                        while (rsPrio.next()) {
                                                                    %><option value="<%=rsPrio.getString("value")%>"<%=scPriority.compareTo(rsPrio.getString("value")) == 0 ? " SELECTED " : ""%>><%=rsPrio.getString("value")%> - <%=rsPrio.getString("description")%></option><%
                                                                        }
                                                                    %></select></td>
                                                            <td class="wob"><select id="ScStatus" name="ScStatus" style="width: 140px">
                                                                    <option value="All">-- ALL --</option><%
                                                                        ResultSet rsStat = stmt70.executeQuery(" SELECT value, description from invariant where id=1 order by sort ");
                                                                        while (rsStat.next()) {
                                                                    %><option value="<%=rsStat.getString("value")%>"<%=scStatus.compareTo(rsStat.getString("value")) == 0 ? " SELECTED " : ""%>><%=rsStat.getString("value")%> - <%=rsStat.getString("description")%></option><%
                                                                        }
                                                                    %></select></td>
                                                        </tr>
                                                    </table>
                                                    <table>
                                                        <tr id="header">
                                                            <td class="wob" style="width: 150px"><%out.print(dbDocS(conn, "testcase", "group", ""));%></td>
                                                            <td class="wob" style="width: 150px"><%out.print(dbDocS(conn, "testcase", "activePROD", ""));%></td>
                                                            <td class="wob" style="width: 150px"><%out.print(dbDocS(conn, "testcase", "activeQA", ""));%></td>
                                                            <td class="wob" style="width: 150px"><%out.print(dbDocS(conn, "testcase", "activeUAT", ""));%></td>
                                                            <td class="wob" style="width: 150px"><%out.print(dbDocS(conn, "page_testcasesearch", "text", ""));%></td>
                                                        </tr> 
                                                        <tr>
                                                            <td class="wob"><select id="ScGroup" name="ScGroup" style="width: 140px">
                                                                    <option value="All">-- ALL --</option><%
                                                                        ResultSet rsGroup = stmt70.executeQuery(" SELECT value, description from invariant where id=2 order by sort ");
                                                                        while (rsGroup.next()) {
                                                                    %><option value="<%=rsGroup.getString("value")%>"<%=scGroup.compareTo(rsGroup.getString("value")) == 0 ? " SELECTED " : ""%>><%=rsGroup.getString("value")%> - <%=rsGroup.getString("description")%></option><%
                                                                        }
                                                                    %></select></td>
                                                            <td class="wob"><select id="ScPROD" name="ScPROD" style="width: 90px">
                                                                    <option value="All">-- ALL --</option><%
                                                                        ResultSet rsPROD = stmt70.executeQuery(" SELECT value, description from invariant where id=28 order by sort ");
                                                                        while (rsPROD.next()) {
                                                                    %><option value="<%=rsPROD.getString("value")%>"<%=scPROD.compareTo(rsPROD.getString("value")) == 0 ? " SELECTED " : ""%>><%=rsPROD.getString("value")%> - <%=rsPROD.getString("description")%></option><%
                                                                        }
                                                                    %></select></td>
                                                            <td class="wob"><select id="ScQA" name="ScQA" style="width: 90px">
                                                                    <option value="All">-- ALL --</option><%
                                                                        ResultSet rsQA = stmt70.executeQuery(" SELECT value, description from invariant where id=26 order by sort ");
                                                                        while (rsQA.next()) {
                                                                    %><option value="<%=rsQA.getString("value")%>"<%=scQA.compareTo(rsQA.getString("value")) == 0 ? " SELECTED " : ""%>><%=rsQA.getString("value")%> - <%=rsQA.getString("description")%></option><%
                                                                        }
                                                                    %></select></td>
                                                            <td class="wob"><select id="ScUAT" name="ScUAT" style="width: 90px">
                                                                    <option value="All">-- ALL --</option><%
                                                                        ResultSet rsUAT = stmt70.executeQuery(" SELECT value, description from invariant where id=27 order by sort ");
                                                                        while (rsUAT.next()) {
                                                                    %><option value="<%=rsUAT.getString("value")%>"<%=scUAT.compareTo(rsUAT.getString("value")) == 0 ? " SELECTED " : ""%>><%=rsUAT.getString("value")%> - <%=rsUAT.getString("description")%></option><%
                                                                        }
                                                                    %></select></td>
                                                            <td class="wob">
                                                                <input id="ScText" name="ScText" value="<%=scText%>" size="80"><%
                                                                %></select></td>
                                                        </tr>
                                                    </table>
                                                    <table><tr id="header">
                                                            <td class="wob" style="width: 150px"><%out.print(dbDocS(conn, "testcase", "tcactive", ""));%></td>
                                                            <td class="wob" style="width: 150px"><%out.print(dbDocS(conn, "testcase", "FromBuild", ""));%></td>
                                                            <td class="wob" style="width: 150px"><%out.print(dbDocS(conn, "testcase", "FromRev", ""));%></td>
                                                            <td class="wob" style="width: 150px"><%out.print(dbDocS(conn, "testcase", "ToBuild", ""));%></td>
                                                            <td class="wob" style="width: 150px"><%out.print(dbDocS(conn, "testcase", "ToRev", ""));%></td>
                                                            <td class="wob" style="width: 150px"><%out.print(dbDocS(conn, "testcase", "TargetBuild", ""));%></td>
                                                            <td class="wob" style="width: 150px"><%out.print(dbDocS(conn, "testcase", "TargetRev", ""));%></td>
                                                        </tr> 
                                                        <tr>
                                                            <td class="wob"><select id="ScActive" name="ScActive" style="width: 140px">
                                                                    <option value="All">-- ALL --</option><%
                                                                        ResultSet rsActive = stmt70.executeQuery(" SELECT value, description from invariant where id=16 order by sort ");
                                                                        while (rsActive.next()) {
                                                                    %><option value="<%=rsActive.getString("value")%>"<%=scActive.compareTo(rsActive.getString("value")) == 0 ? " SELECTED " : ""%>><%=rsActive.getString("value")%> - <%=rsActive.getString("description")%></option><%
                                                                        }
                                                                    %></select></td>
                                                            <td class="wob"><select id="ScFBuild" name="ScFBuild" style="width: 90px">
                                                                    <option value="All">-- ALL --</option><%
                                                                        ResultSet rsFB = stmt70.executeQuery(" SELECT distinct FromBuild from testcase where FromBuild is not null order by FromBuild ");
                                                                        while (rsFB.next()) {
                                                                    %><option value="<%=rsFB.getString("FromBuild")%>"<%=scFBuild.compareTo(rsFB.getString("FromBuild")) == 0 ? " SELECTED " : ""%>><%=rsFB.getString("FromBuild")%></option><%
                                                                        }
                                                                    %></select></td>
                                                            <td class="wob"><select id="ScFRev" name="ScFRev" style="width: 90px">
                                                                    <option value="All">-- ALL --</option><%
                                                                        ResultSet rsFR = stmt70.executeQuery(" SELECT distinct FromRev from testcase where FromRev is not null order by FromRev ");
                                                                        while (rsFR.next()) {
                                                                    %><option value="<%=rsFR.getString("FromRev")%>"<%=scFRev.compareTo(rsFR.getString("FromRev")) == 0 ? " SELECTED " : ""%>><%=rsFR.getString("FromRev")%></option><%
                                                                        }
                                                                    %></select></td>
                                                            <td class="wob"><select id="ScTBuild" name="ScTBuild" style="width: 90px">
                                                                    <option value="All">-- ALL --</option><%
                                                                        ResultSet rsTB = stmt70.executeQuery(" SELECT distinct ToBuild from testcase where ToBuild is not null order by ToBuild ");
                                                                        while (rsTB.next()) {
                                                                    %><option value="<%=rsTB.getString("ToBuild")%>"<%=scTBuild.compareTo(rsTB.getString("ToBuild")) == 0 ? " SELECTED " : ""%>><%=rsTB.getString("ToBuild")%></option><%
                                                                        }
                                                                    %></select></td>
                                                            <td class="wob"><select id="ScTRev" name="ScTRev" style="width: 90px">
                                                                    <option value="All">-- ALL --</option><%
                                                                        ResultSet rsTR = stmt70.executeQuery(" SELECT distinct ToRev from testcase where ToRev is not null order by ToRev ");
                                                                        while (rsTR.next()) {
                                                                    %><option value="<%=rsTR.getString("ToRev")%>"<%=scTRev.compareTo(rsTR.getString("ToRev")) == 0 ? " SELECTED " : ""%>><%=rsTR.getString("ToRev")%></option><%
                                                                        }
                                                                    %></select></td>
                                                            <td class="wob"><select id="ScTargetBuild" name="ScTargetBuild" style="width: 90px">
                                                                    <option value="All">-- ALL --</option><%
                                                                        ResultSet rsTaB = stmt70.executeQuery(" SELECT distinct TargetBuild from testcase where TargetBuild is not null order by TargetBuild ");
                                                                        while (rsTaB.next()) {
                                                                    %><option value="<%=rsTaB.getString("TargetBuild")%>"<%=scTargetBuild.compareTo(rsTaB.getString("TargetBuild")) == 0 ? " SELECTED " : ""%>><%=rsTaB.getString("TargetBuild")%></option><%
                                                                        }
                                                                    %></select></td>
                                                            <td class="wob"><select id="ScTargetRev" name="ScTargetRev" style="width: 90px">
                                                                    <option value="All">-- ALL --</option><%
                                                                        ResultSet rsTaR = stmt70.executeQuery(" SELECT distinct TargetRev from testcase where TargetRev is not null order by TargetRev ");
                                                                        while (rsTaR.next()) {
                                                                    %><option value="<%=rsTaR.getString("TargetRev")%>"<%=scTargetRev.compareTo(rsTaR.getString("TargetRev")) == 0 ? " SELECTED " : ""%>><%=rsTaR.getString("TargetRev")%></option><%
                                                                        }
                                                                    %></select></td>
                                                        </tr>
                                                    </table>
                                                </td>
                                                <td id="wob">
                                                    <input id="submit" class="submit" type="submit" value="Search Testcase"></td>
                                                <td id="wob"><input name="Search" value="Y" style="visibility:hidden"></td>
                                                <td id="wob"><input name="SearchTc" value="Y" style="visibility:hidden"></td>
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

                if (search) {%>            

            <%
                String SQLSearch = "SELECT t2.Test, "
                        + "t2.testcase, t2.Description, t2.BehaviorOrValueExpected, t2.readonly, t2.priority, t2.status, t2.group, "
                        + "t2.tcActive, t2.Application, t2.Project, t2.Ticket, t2.group, t2.Origine, t2.RefOrigine, t2.howto,"
                        + " t2.comment, t2.TCDateCrea, t2.frombuild, t2.fromrev, t2.tobuild, t2.torev, t2.bugid, t2.targetbuild,"
                        + " t2.targetrev, t2.creator, t2.implementer, t2.lastmodifier, t2.activeQA, t2.activeUAT, t2.activePROD"
                        + " FROM testcase t2"
                        + " LEFT OUTER JOIN Application a ON a.application=t2.application "
                        + whereclause;
                //                                out.print(SQLSearch);
                ResultSet rs_testcase = stmt71.executeQuery(SQLSearch);
                int j = 0;
                int i = 0;
                int max_rows = 100;
                if (rs_testcase.first()) {
                    boolean OK = rs_testcase.last();
                    int nb_tot = rs_testcase.getRow();
                    OK = rs_testcase.first();
            %>

            <br>
            <table id="arrond">
                <tr>
                    <td>
                        <div id="table">
                            <% if (nb_tot > max_rows) {
                            %><h3 style="color: blue">TestCase List - <%=nb_tot%> results. Only the <%=max_rows%> first rows displayed.</h3><%
                            } else {
                            %><h3 style="color: blue">TestCase List - <%=nb_tot%> results.</h3><%
                                }%>
                            <table id="testcasetable" class="tableau"  style="text-align: left; border-collapse: collapse" border="0px" cellpadding="0px" cellspacing="0px">
                                <tr id="header">  
                                    <td></td>
                                    <td style="width: 200px" colspan="2">Testcase Information</td>
                                    <td style="width: 800px" colspan="2">Testcase Parameters</td>
                                    <td style="width: 100px">Activation Criterias</td>
                                </tr>
                                <%
                                    do {

                                        Statement stmt72 = conn.createStatement();
                                        ResultSet rs_testcasecountry = stmt72.executeQuery("SELECT DISTINCT country "
                                                + " FROM testcasecountry "
                                                + " WHERE test ='"
                                                + rs_testcase.getString("t2.Test")
                                                + "'" + " AND testcase ='"
                                                + rs_testcase.getString("t2.testcase")
                                                + "'");

                                        String countries = "";
                                        while (rs_testcasecountry.next()) {
                                            countries += rs_testcasecountry.getString("country") + "-";
                                        }
                                        rs_testcasecountry.close();
                                        stmt72.close();

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
                                        String project2 = "Project: ";
                                        project2 += rs_testcase.getString("t2.project");
                                        String app2 = "Application: ";
                                        app2 += rs_testcase.getString("t2.application");
                                        String origine = "Origine: ";
                                        origine += rs_testcase.getString("t2.origine");
                                        String group2 = "Group: ";
                                        group2 += rs_testcase.getString("t2.group");
                                        String fromBuild = "FromBuild: ";
                                        fromBuild += rs_testcase.getString("t2.frombuild");
                                        String toBuild = "ToBuild: ";
                                        toBuild += rs_testcase.getString("t2.tobuild");
                                        String fromRev = "FromRev: ";
                                        fromRev += rs_testcase.getString("t2.fromrev");
                                        String toRev = "ToRev: ";
                                        toRev += rs_testcase.getString("t2.torev");
                                        String priority2 = "Priority: ";
                                        priority2 += rs_testcase.getString("t2.priority");
                                        String activeQA = "ActiveQA: ";
                                        activeQA += rs_testcase.getString("t2.activeQA");
                                        String activeUAT = "ActiveUAT: ";
                                        activeUAT += rs_testcase.getString("t2.activeUAT");
                                        String activePROD = "ActivePROD: ";
                                        activePROD += rs_testcase.getString("t2.activePROD");
                                        String refOrigine = "RefOrigine: ";
                                        refOrigine += rs_testcase.getString("t2.reforigine");
                                        String status2 = "Status: ";
                                        status2 += rs_testcase.getString("t2.status");
                                        String tcActive2 = "Active: ";
                                        tcActive2 += rs_testcase.getString("t2.tcActive");
                                        String creator = "Creator: ";
                                        creator += rs_testcase.getString("t2.creator");


                                        i++;

                                %>
                                <tr style="background-color: <%=color%>">
                                    <td style="text-align: center">
                                        <a style="font-size: xx-small;" href="TestCase.jsp?Test=<%=rs_testcase.getString("T2.Test")%>&TestCase=<%=rs_testcase.getString("t2.testcase")%>&Load=Load">edit</a></td>
                                    <td valign="top">
                                        <table>
                                            <tr>
                                                <td colspan="2" id="testcase_testcase1" class="wob" style="font-weight: bold; width: 150px;" name="testcase_testcase" readonly="readonly">
                                                    <%=rs_testcase.getString("t2.test")%> </td>
                                                <td id="testcase_testcase1" class="wob" style="font-weight: bold; width: 150px;" name="testcase_testcase" readonly="readonly">
                                                    <%=rs_testcase.getString("t2.testcase")%></td>
                                            </tr>
                                            <tr>
                                                <% if (StringUtils.isNotBlank(rs_testcase.getString("t2.origine"))) {%>
                                                <td id="testcase_origine" class="wob" style=" width: 150px;" name="testcase_origine">
                                                    <%=StringUtils.isBlank(rs_testcase.getString("t2.origine")) ? "" : origine%></td>
                                            </tr>
                                            <tr>
                                                <% }
                                                    if (StringUtils.isNotBlank(rs_testcase.getString("t2.reforigine"))) {%>
                                                <td id="testcase_reforigine" class="wob" style="width: 150px;" name="testcase_refOrigine">
                                                    <%=StringUtils.isBlank(rs_testcase.getString("t2.reforigine")) ? "" : refOrigine%></td>
                                            </tr>
                                            <tr>
                                                <%}
                                                    if (StringUtils.isNotBlank(rs_testcase.getString("t2.creator"))) {%>
                                                <td id="testcase_creator" class="wob" style="width: 150px;" name="testcase_creator">
                                                    <%=StringUtils.isBlank(rs_testcase.getString("t2.creator")) ? "" : creator%></td>
                                            </tr>
                                            <tr>
                                                <%}%>
                                                <td id="testcase_activeqa" class="wob" style="width: 150px;" name="testcase_activeqa">
                                                    <%=StringUtils.isBlank(rs_testcase.getString("t2.activeQA")) ? "" : activeQA%></td>
                                                <td id="testcase_activeuat" class="wob" style="width: 150px;" name="testcase_activeuat">
                                                    <%=StringUtils.isBlank(rs_testcase.getString("t2.activeUAT")) ? "" : activeUAT%></td>
                                                <td id="testcase_activeprod" class="wob" style="width: 150px;" name="testcase_activeprod">
                                                    <%=StringUtils.isBlank(rs_testcase.getString("t2.activePROD")) ? "" : activePROD%></td>
                                            </tr>
                                        </table>      
                                    </td><td valign="top">
                                        <table><tr>
                                                <%
                                                    if (StringUtils.isNotBlank(rs_testcase.getString("t2.application"))) {%> 
                                                <td id="testcase_application" class="wob" name="testcase_application" style="width: 250px">
                                                    <%=StringUtils.isBlank(rs_testcase.getString("t2.application")) ? "" : app2%></td>
                                            </tr>
                                            <tr>
                                                <%}
                                                    if (StringUtils.isNotBlank(rs_testcase.getString("t2.bugid"))) {%>
                                                <td id="testcase_bugID" class="wob" style="width: 150px;" name="testcase_bugID">
                                                    <%=StringUtils.isBlank(rs_testcase.getString("t2.bugid")) ? "" : bugID%></td>
                                            </tr>
                                            <tr>
                                                <%}
                                                    if (StringUtils.isNotBlank(rs_testcase.getString("t2.project"))) {%> 
                                                <td id="testcase_project" class="wob" style="width: 150px;" name="testcase_project">
                                                    <%=StringUtils.isBlank(rs_testcase.getString("t2.project")) ? "" : project2%></td>
                                            </tr>
                                            <tr>
                                                <%}
                                                    if (StringUtils.isNotBlank(rs_testcase.getString("t2.ticket"))) {%>
                                                <td id="testcase_ticket" class="wob" style="width: 150px;" name="testcase_ticket">
                                                    <%=StringUtils.isBlank(rs_testcase.getString("t2.ticket")) ? "" : ticket%></td>
                                            </tr>
                                            <tr>
                                                <%}
                                                    if (StringUtils.isNotBlank(rs_testcase.getString("t2.priority"))) {%> 
                                                <td id="testcase_priority" class="wob" style="width: 150px;" name="testcase_priority">
                                                    <%=StringUtils.isBlank(rs_testcase.getString("t2.priority")) ? "" : priority2%></td>
                                            </tr>
                                            <tr>
                                                <%}%></tr></table>   
                                    </td><td valign="top">
                                        <table><tr>
                                                <%
                                                    if (StringUtils.isNotBlank(rs_testcase.getString("t2.group"))) {%> 

                                                <td id="testcase_group" class="wob" style="width: 250px;" name="testcase_group">
                                                    <%=StringUtils.isBlank(rs_testcase.getString("t2.group")) ? "" : group2%></td></tr><tr>
                                                    <%}
                                                        if (StringUtils.isNotBlank(rs_testcase.getString("t2.status"))) {%>
                                                <td id="testcase_status" class="wob" style="width: 250px;" name="testcase_status">
                                                    <%=StringUtils.isBlank(rs_testcase.getString("t2.status")) ? "" : status2%></td></tr><tr>
                                                    <%}%>
                                                <td id="testcase_countries" class="wob"  style="width: 250px; font-size: x-small;" name="testcase_countries">
                                                    <%=countries%></td>                    
                                            </tr></table>     
                                    </td><td valign="top">
                                        <table><tr>
                                                <td id="testcase_description" class="wob" style="width: 600px; font-weight: bold" name="testcase_description">
                                                    <%=rs_testcase.getString("t2.Description")%></td></tr><tr>
                                                <td class="wob"><textarea  id="testcase_valueexpec" class="wob" rows="1" style="width: 600px; background-color: <%=color%>" name="testcase_valueexpec"
                                                                           readonly="readonly"><%=rs_testcase.getString("t2.BehaviorOrValueExpected")%></textarea></td></tr><tr>
                                            </tr></table>
                                    </td><td valign="top">
                                        <table><tr>
                                                <%
                                                    if (StringUtils.isNotBlank(rs_testcase.getString("t2.tcActive"))) {%>
                                                <td id="testcase_tcActive" class="wob" style="width: 150px;" name="testcase_tcActive">
                                                    <%=StringUtils.isBlank(rs_testcase.getString("t2.tcActive")) ? "" : tcActive2%></td>

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
                                            </tr>
                                        </table>
                                    </td>
                                </tr>   

                                <%

                                    } while ((rs_testcase.next()) && (i < max_rows));/*
                                     * End testcase loop
                                     */
                                } else {%>
                                <br>
                                <table id="arrond">
                                    <tr>
                                        <td>
                                            <div id="table">
                                                <h3 style="color: blue"> No TestCase Found...</h3>
                                            </div>
                                        </td>
                                    </tr>
                                </table>
                                <%    }
                                %>
                            </table>
                            <%
                                /*
                                 * End
                                 * Test
                                 * Request
                                 */

                                stmt.close();
                                stmt71.close();
                                stmt22.close();
                            %>
                            <br> 
                        </div>
                    </td>
                </tr>
            </table>
            <%}
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
