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
<%@page import="java.sql.Connection"%>
<%@page import="java.sql.ResultSet"%>
<%@page import="java.sql.Statement"%>
<%@page import="org.cerberus.util.StringUtil"%>
<%@page import="org.cerberus.service.IDocumentationService"%>
<%@page import="org.cerberus.util.SqlUtil"%>
<%@page import="org.cerberus.entity.BuildRevisionInvariant"%>
<%@page import="org.cerberus.service.impl.BuildRevisionInvariantService"%>
<%@page import="org.cerberus.service.IBuildRevisionInvariantService"%>
<%@page import="org.cerberus.service.impl.ApplicationService"%>
<%@page import="org.cerberus.entity.Application"%>
<%@page import="org.cerberus.service.IApplicationService"%>
<%
    Date DatePageStart = new Date();
%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Sprint Content</title>
        <link rel="stylesheet" 
              type="text/css" href="css/crb_style.css"
              />
        <link rel="shortcut icon" type="image/x-icon" href="images/favicon.ico">
    </head>
    <body>
        <%@ include file="include/function.jsp" %>
        <%@ include file="include/header.jsp" %>
        <%
            Connection conn = db.connect();

            try {

                IApplicationService applicationService = appContext.getBean(ApplicationService.class);
                IBuildRevisionInvariantService buildRevisionInvariantService = appContext.getBean(BuildRevisionInvariantService.class);
                IDocumentationService docService = appContext.getBean(IDocumentationService.class);

                String MySystem = request.getAttribute("MySystem").toString();
                String myLang = request.getAttribute("MyLang").toString();
                if (request.getParameter("system") != null && request.getParameter("system").compareTo("") != 0) {
                    MySystem = request.getParameter("system");
                }
                List<Application> appliList = applicationService.convert(applicationService.readBySystem(MySystem));
                String appliInSQL = SqlUtil.getInSQLClause(appliList);

                Statement stmtBuild = conn.createStatement();
                Statement stmtApp = conn.createStatement();

                /* Parameter Setup */
                String build = "NONE";
                if (request.getParameter("build") != null && request.getParameter("build").compareTo("") != 0) {
                    build = request.getParameter("build");
                } else {
                    ResultSet rsBR = stmtBuild.executeQuery("SELECT max(Build) mb FROM buildrevisionparameters where build!='NONE' and build is not null and application " + appliInSQL);
                    if (rsBR.first() && rsBR.getString("mb") != null) {
                        build = rsBR.getString("mb");
                    }
                }
                String revision = "NONE";
                if (request.getParameter("revision") != null && request.getParameter("revision").compareTo("") != 0) {
                    revision = request.getParameter("revision");
                } else {
                    ResultSet rsREV = stmtBuild.executeQuery("SELECT max(Revision) mr FROM buildrevisionparameters "
                            + " where build = '" + build + "' and application " + appliInSQL);
                    if (rsREV.first() && rsREV.getString("mr") != null) {
                        revision = rsREV.getString("mr");
                    }
                }

                Statement stmtRev = conn.createStatement();
        %>                    <form method="GET" name="BuildContent" id="buildcontent">
            <table class="tablef"> 
                <tr>
                    <td><a href="?build=NONE&revision=NONE">Pending Release</a></td>
                    <td><a href="BuildContent_old.jsp">Latest Release</a></td>
                    <td> 
                <ftxt><%=docService.findLabelHTML("buildrevisioninvariant", "versionname01", "", myLang)%></ftxt> 
                <select id="build" name="build" style="width: 100px" OnChange ="document.buildcontent.submit()">
                    <option style="width: 100px" value="NONE" <%=build.compareTo("NONE") == 0 ? " SELECTED " : ""%>>-- NONE --</option>
                    <%
                        List<BuildRevisionInvariant> listBuildRev = buildRevisionInvariantService.findAllBuildRevisionInvariantBySystemLevel(MySystem, 1);
                        for (BuildRevisionInvariant myBR : listBuildRev) {
                    %><option style="width: 100px" value="<%=myBR.getVersionName()%>" <%=build.compareTo(myBR.getVersionName()) == 0 ? " SELECTED " : ""%>><%=myBR.getVersionName()%></option>
                    <%
                        }
                    %></select>
                <ftxt><%=docService.findLabelHTML("buildrevisioninvariant", "versionname02", "", myLang)%></ftxt> 
                <select id="revision" name="revision" style="width: 100px" OnChange ="document.buildcontent.submit()">
                    <option style="width: 100px" value="ALL" <%=revision.compareTo("ALL") == 0 ? " SELECTED " : ""%>>-- ALL --</option>
                    <option style="width: 100px" value="NONE" <%=revision.compareTo("NONE") == 0 ? " SELECTED " : ""%>>-- NONE --</option>
                    <%
                        listBuildRev = buildRevisionInvariantService.findAllBuildRevisionInvariantBySystemLevel(MySystem, 2);
                        for (BuildRevisionInvariant myBR : listBuildRev) {
                    %><option style="width: 100px" value="<%=myBR.getVersionName()%>" <%=revision.compareTo(myBR.getVersionName()) == 0 ? " SELECTED " : ""%>><%=myBR.getVersionName()%></option>
                    <%
                        }
                    %></select>
                <input type="submit" name="FilterApply" value="Apply">
                </td>
                </tr>
            </table>
        </form><br>
        <%
            stmtBuild.close();
            stmtRev.close();

            Statement stmtBR = conn.createStatement();
            String BR;

            BR = "SELECT DISTINCT b.ID, b.Build, b.Revision, b.Application, b.Release, b.Link, b.ReleaseOwner, b.Project,b.TicketIDFixed, b.BugIDFixed,b.Subject "
                    + "FROM `buildrevisionparameters` b "
                    + "WHERE 1=1 "
                    + " AND application " + appliInSQL;
            if (!build.trim().equalsIgnoreCase("ALL")) {
                BR += " and Build='" + build + "' ";
            }
            if (!revision.trim().equalsIgnoreCase("ALL")) {
                BR += " and Revision='" + revision + "' ";
            }
            BR += " ORDER by Build desc, Revision desc, Application ASC, `Release` ASC";
            //                out.print(BR);
            ResultSet rsBR = stmtBR.executeQuery(BR);
        %>   
        <form method="post" name="UpdateBuildContent" action="UpdateBuildRevisionParameter">
            <input style="display:none" name="ubcBuildFilter" value="<%=build%>"></input>
            <input style="display:none" name="ubcRevisionFilter" value="<%=revision%>"></input>
            <table  id="buildcontenttable"  style="text-align: left; border-collapse:collapse ; border-color: gainsboro" border="1">
                <tr id="header">
                    <td><%=docService.findLabelHTML("page_buildcontent", "delete", "", myLang)%></td>
                    <td><%=docService.findLabelHTML("buildrevisioninvariant", "versionname01", "", myLang)%></td>
                    <td><%=docService.findLabelHTML("buildrevisioninvariant", "versionname02", "", myLang)%></td>
                    <td><%=docService.findLabelHTML("application", "Application", "", myLang)%></td>
                    <td><%=docService.findLabelHTML("buildrevisionparameters", "Release", "", myLang)%></td>
                    <td></img>Project</td>
                    <td></img>Ticket</td>
                    <td></img>Bug</td>
                    <td>Subject</td>
                    <td><%=docService.findLabelHTML("buildrevisionparameters", "ReleaseOwner", "", myLang)%></td>
                    <td colspan="2"><%=docService.findLabelHTML("buildrevisionparameters", "Link", "", myLang)%></td>


<!--                <td><%=docService.findLabelHTML("buildrevisionparameters", "Link", "", myLang)%></td>-->
                </tr>
                <%
                    int a = 1;
                    String backColor = "white";
                    if (rsBR.first()) {
                        do {
                            //Background color Management
                            a++;
                            int b;
                            b = a % 2;
                            if (b == 1) {
                                backColor = "#f3f6fa";
                            } else {
                                backColor = "White";
                            }

                            // 
                            String[] ticketLinks = new String[0];
                            String linkToTickets = "";
                            if (!StringUtil.isNullOrEmpty(rsBR.getString("b.subject"))) {
                                ticketLinks = rsBR.getString("b.subject").split(", ");
                                for (int i = 0; i < ticketLinks.length; i++) {
                                    linkToTickets = linkToTickets + "<a href=\"TestCaseSearch.jsp?ScTicket=" + ticketLinks[i] + "\">" + ticketLinks[i] + " </a>";
                                }
                            }
                            String[] bugLinks = new String[0];
                            String linkToBugs = "";
                            if (!StringUtil.isNullOrEmpty(rsBR.getString("b.subject"))) {
                                bugLinks = rsBR.getString("b.subject").split(", ");
                                for (int i = 0; i < bugLinks.length; i++) {
                                    linkToBugs = linkToBugs + "<a href=\"TestCaseSearch.jsp?ScBugID=" + bugLinks[i] + "\">" + bugLinks[i] + " </a>";
                                }
                            }
                            Statement stmtProj = conn.createStatement();
                %>
                <tr>
                    <td class="wob" style="background-color:<%=backColor%>"><input name="ubcDelete" type="checkbox" style="width:10px ; background-color:<%=backColor%>" 
                                                                                   value="<%=rsBR.getString("b.ID")%>"></td>
                    <td class="wob" style="background-color:<%=backColor%>">
                        <select id="ubcBuild" name="ubcBuild" style="width:60px ; background-color:<%=backColor%>; font-size:x-small;border:0px">
                            <option style="width: 100px" value="NONE" <%=revision.compareTo("NONE") == 0 ? " SELECTED " : ""%>>-- NONE --</option>
                            <%
                                listBuildRev = buildRevisionInvariantService.findAllBuildRevisionInvariantBySystemLevel(MySystem, 1);
                                for (BuildRevisionInvariant myBR : listBuildRev) {
                            %><option style="width: 100px" value="<%=myBR.getVersionName()%>" <%=rsBR.getString("b.build").compareTo(myBR.getVersionName()) == 0 ? " SELECTED " : ""%>><%=myBR.getVersionName()%></option>
                            <%
                                }
                            %></select>
                    </td>
                    <td class="wob" style="background-color:<%=backColor%>">
                        <select id="ubcRevision" name="ubcRevision" style="width:40px ; background-color:<%=backColor%>; font-size:x-small;border:0px">
                            <option style="width: 100px" value="NONE" <%=revision.compareTo("NONE") == 0 ? " SELECTED " : ""%>>-- NONE --</option>
                            <%
                                listBuildRev = buildRevisionInvariantService.findAllBuildRevisionInvariantBySystemLevel(MySystem, 2);
                                for (BuildRevisionInvariant myBR : listBuildRev) {
                            %><option style="width: 100px" value="<%=myBR.getVersionName()%>" <%=rsBR.getString("b.revision").compareTo(myBR.getVersionName()) == 0 ? " SELECTED " : ""%>><%=myBR.getVersionName()%></option>
                            <%
                                }
                            %></select>
                    </td>
                    <td class="wob" style="background-color:<%=backColor%>">
                        <select id="ubcApplication" name="ubcApplication" class="wob" style="width:170px; font-size:x-small;background-color:<%=backColor%>"><%
                            ResultSet rsApp = stmtProj.executeQuery(" SELECT distinct application from application where application != '' and application " + appliInSQL + " order by sort ");
                            rsApp.first();
                            do {
                            %><option value="<%=rsApp.getString("application")%>" <%=rsApp.getString("application").compareTo(rsBR.getString("b.Application")) == 0 ? " SELECTED " : ""%>><%=rsApp.getString("application")%></option><%
                            } while (rsApp.next());
                            %></select>
                        <input style="display:none" name="ubcReleaseID" value="<%=rsBR.getString("b.ID")%>"></td>
                    <td class="wob" style="background-color:<%=backColor%>"><input class="wob" name="ubcRelease" style="width:100px ; background-color:<%=backColor%>; font-size:x-small" value="<%=rsBR.getString("b.Release")%>"></td>
                    <td class="wob" style="background-color:<%=backColor%>">
                        <select class="wob" name="ubcProject" value="<%=rsBR.getString("b.Project")%>" style="width: 50px; background-color:<%=backColor%>; font-size:x-small"><%
                            ResultSet rsProj = stmtProj.executeQuery(" SELECT idproject, VCCode, Description from project order by idproject ");
                            while (rsProj.next()) {
                            %><option value="<%=rsProj.getString("idproject")%>"<%=rsBR.getString("b.Project").compareTo(rsProj.getString("idproject")) == 0 ? " SELECTED " : ""%>><%=rsProj.getString("idproject")%> [<%=rsProj.getString("VCCode")%>] <%=rsProj.getString("Description")%></option><%
                            }
                            %></select>
                    </td>
                    <td class="wob" style="background-color:<%=backColor%>"><input class="wob" name="ubcTicketIDFixed" value="<%=rsBR.getString("b.TicketIDFixed")%>" style="width: 50px; background-color:<%=backColor%>; font-size:x-small"></td>
                    <td class="wob" style="background-color:<%=backColor%>"><input class="wob" name="ubcBugIDFixed" value="<%=rsBR.getString("b.BugIDFixed")%>" style="width: 50px; background-color:<%=backColor%>; font-size:x-small"></td>
                    <td class="wob" style="background-color:<%=backColor%>"><textarea class="wob" name="ubcSubject" value="<%=rsBR.getString("b.Subject")%>" rows="1" style="width: 300px; background-color:<%=backColor%>; font-size:x-small"><%=rsBR.getString("b.Subject")%></textarea></td>
                    <td class="wob" style="background-color:<%=backColor%>">
                        <select class="wob" name="ubcReleaseOwner" style="width: 100px; background-color:<%=backColor%>; font-size:x-small">
                            <option value="" ></option><%
                                ResultSet rsOwner = stmtProj.executeQuery(" SELECT u.Login, u.Name FROM user u join userSystem us on u.login = us.login where us.`system` = '" + MySystem + "' group by us.login;");
                                while (rsOwner.next()) {
                            %><option value="<%=rsOwner.getString("Login")%>"<%=rsBR.getString("b.ReleaseOwner").compareTo(rsOwner.getString("Login")) == 0 ? " SELECTED " : ""%>><%=rsOwner.getString("Name")%></option><%
                                }
                                %>
                        </select>
                    </td>
                    <td class="wob" style="width:22px; background-color:<%=backColor%>">
                        <input style="display:inline; height:20px; width:20px; background-color: <%=backColor%>; color:blue; font-weight:bolder" title="Link" class="smallbutton" type="button" value="L" onclick="popup('<%=rsBR.getString("b.Link")%>')">
                    </td>
                    <td class="wob" style="background-color:<%=backColor%>"><textarea class="wob" name="ubcLink" value="<%=rsBR.getString("b.Link")%>" rows="1" style="width: 250px; background-color:<%=backColor%>; font-size:x-small" ><%=rsBR.getString("b.Link")%></textarea></td>
                </tr><%
                            stmtProj.close();
                        } while (rsBR.next());
                    }
                %></table>
            <input type="button" value="New Line" onclick="addBuildContent('buildcontenttable')"></td></tr>
            <select id="buildcontent_build_" name="buildcontent_build_" style="width:60px ; visibility:hidden; font-size:x-small;border:0px">
                <option style="width: 100px" value="NONE" <%=revision.compareTo("NONE") == 0 ? " SELECTED " : ""%>>-- NONE --</option>
                <%
                    listBuildRev = buildRevisionInvariantService.findAllBuildRevisionInvariantBySystemLevel(MySystem, 1);
                    for (BuildRevisionInvariant myBR : listBuildRev) {
                %><option style="width: 100px" value="<%=myBR.getVersionName()%>" <%=build.compareTo(myBR.getVersionName()) == 0 ? " SELECTED " : ""%>><%=myBR.getVersionName()%></option>
                <%
                    }
                %></select>
            <select id="buildcontent_revision_" name="buildcontent_revision_" style="width:40px ; visibility:hidden; font-size:x-small;border:0px">
                <option style="width: 100px" value="NONE" <%=revision.compareTo("NONE") == 0 ? " SELECTED " : ""%>>-- NONE --</option>
                <%
                    listBuildRev = buildRevisionInvariantService.findAllBuildRevisionInvariantBySystemLevel(MySystem, 2);
                    for (BuildRevisionInvariant myBR : listBuildRev) {
                %><option style="width: 100px" value="<%=myBR.getVersionName()%>" <%=revision.compareTo(myBR.getVersionName()) == 0 ? " SELECTED " : ""%>><%=myBR.getVersionName()%></option>
                <%
                    }
                %></select>
            <select id="buildcontent_application_" name="buildcontent_application_" style="visibility:hidden"><%
                ResultSet rsApp = stmtApp.executeQuery(" SELECT distinct application from application where application != '' and application " + appliInSQL + " order by sort ");
                while (rsApp.next()) {
                %><option value="<%=rsApp.getString("application")%>" <%=rsApp.getString("application").compareTo("AS400") == 0 ? " SELECTED " : ""%>><%=rsApp.getString("application")%></option><%
                }
                %></select>
            <select id="ubcReleaseOwner_" name="ubcReleaseOwner_" style="visibility:hidden">
                <option value="" ></option><%
                    Statement stmtProj = conn.createStatement();
                    ResultSet rsOwner = stmtProj.executeQuery(" SELECT u.Login, u.Name FROM user u join userSystem us on u.login = us.login where us.`system` = '" + MySystem + "' group by us.login;");
                    while (rsOwner.next()) {
                %><option value="<%=rsOwner.getString("Login")%>"><%=rsOwner.getString("Name")%></option><%
                    }
                    %>
            </select>
            <select id="ubcProject_" name="ubcProject_" style="visibility:hidden"><%
                ResultSet rsProj = stmtProj.executeQuery(" SELECT idproject, VCCode, Description from project order by idproject ");
                while (rsProj.next()) {
                %><option value="<%=rsProj.getString("idproject")%>"><%=rsProj.getString("idproject")%> [<%=rsProj.getString("VCCode")%>] <%=rsProj.getString("Description")%></option><%
                    }
                    stmtProj.close();
                %>
            </select>

            <br><input type="submit" name="Save" value="Save">
        </form>
        <%
                rsApp.close();
                rsBR.close();
                rsOwner.close();
                rsProj.close();

                stmtApp.close();
                stmtBR.close();
                stmtBuild.close();
                stmtProj.close();
                stmtRev.close();

            } catch (Exception e) {
                MyLogger.log("BuildContent_old.jsp", Level.FATAL, Infos.getInstance().getProjectNameAndVersion() + " - Exception catched." + e.toString());
                out.println("<br> error message : " + e.getMessage() + " " + e.toString() + "<br>");
            } finally {
                try {
                    conn.close();
                } catch (Exception ex) {
                    MyLogger.log("BuildContent_old.jsp", Level.FATAL, Infos.getInstance().getProjectNameAndVersion() + " - Exception catched." + ex.toString());
                }
            }
        %>

        <br><% out.print(display_footer(DatePageStart));%>
    </body>
</html>
