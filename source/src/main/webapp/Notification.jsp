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
<%@page import="org.cerberus.service.IDocumentationService"%>
<%@page import="org.cerberus.service.IParameterService"%>
<%@page import="org.cerberus.serviceEmail.IEmailGeneration"%>
<%@page import="org.cerberus.util.StringUtil"%>
<% Date DatePageStart = new Date();%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Notification Page</title>
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
            IDocumentationService docService = appContext.getBean(IDocumentationService.class);

            try {

                /* Parameter Setup */

                String system = "";
                if (request.getParameter("system") != null && request.getParameter("system").compareTo("") != 0) {
                    system = request.getParameter("system");
                }

                String country;
                if (request.getParameter("country") != null && request.getParameter("country").compareTo("") != 0) {
                    country = request.getParameter("country");
                } else {
                    country = new String("ALL");
                }

                String env;
                if (request.getParameter("env") != null && request.getParameter("env").compareTo("") != 0) {
                    env = request.getParameter("env");
                } else {
                    env = new String("ALL");
                }

                String event;
                if (request.getParameter("event") != null && request.getParameter("event").compareTo("") != 0) {
                    event = request.getParameter("event");

                } else {
                    event = new String("NONE");
                }

                String build;
                if (request.getParameter("build") != null && request.getParameter("build").compareTo("ALL") != 0) {
                    build = request.getParameter("build");
                } else {
                    build = new String("NONE");
                }

                String revision;
                if (request.getParameter("revision") != null && request.getParameter("revision").compareTo("ALL") != 0) {
                    revision = request.getParameter("revision");
                } else {
                    revision = new String("NONE");
                }

                String chain;
                if (request.getParameter("chain") != null && request.getParameter("chain").compareTo("") != 0) {
                    chain = request.getParameter("chain");
                } else {
                    chain = new String("NONE");
                }


                // Generate the content of the email
                IEmailGeneration myEmailGeneration = appContext.getBean(IEmailGeneration.class);
                String eMailContent = "";
                String formAction = "";

                if (!StringUtil.isNullOrEmpty(event)) {

                    if (event.equals("newbuildrevision")) {
                        eMailContent = myEmailGeneration.EmailGenerationRevisionChange(system, country, env, build, revision, conn);
                        formAction = "NewBuildRev";
                    }
                    if (event.equals("disableenvironment")) {
                        eMailContent = myEmailGeneration.EmailGenerationDisableEnv(system, country, env);
                        formAction = "DisableEnvironment";
                    }
                    if (event.equals("newchain")) {
                        eMailContent = myEmailGeneration.EmailGenerationNewChain(system, country, env, build, revision, chain);
                        formAction = "NewChain";
                    }
                }

                // Split the result to extract all the data
                String[] eMailContentTable = eMailContent.split("///");

                String to = eMailContentTable[0];
                String cc = eMailContentTable[1];
                String subject = eMailContentTable[2];
                String body = eMailContentTable[3];


        %>

        <table border>
            <tr>
                <td colspan="2" style="background-color: lightyellow; text-align: center">EMail Notification Preview</td>
            </tr>
            <tr>
                <td><%=docService.findLabelHTML("page_notification", "To", "")%></td>
                <td><table border><tr><td><%=to%></td></tr></table></td>
            </tr> 
            <tr>
                <td><%=docService.findLabelHTML("page_notification", "Cc", "")%></td>
                <td><table border><tr><td><%=cc%></td></tr></table></td>
            </tr> 
            <tr>
                <td><%=docService.findLabelHTML("page_notification", "Subject", "")%></td>
                <td><table border><tr><td><%=subject%></td></tr></table></td>
            </tr>
            <tr>
                <td><%=docService.findLabelHTML("page_notification", "Body", "")%></td>
                <td><table border><tr><td><%=body%></td></tr></table></td>
            </tr>
        </table>
        <br>
        <table border>
            <tr>
                <td colspan="3" style="background-color: lightyellow; text-align: center">Application to deploy.</td>
            </tr>
            <tr><td>Application</td><td>Release</td><td>Deploy with Jenkins</td><td>View the Jenkins Pipe</td></tr>
            <%

                String SQLBC;
                SQLBC = "SELECT Build, Revision "
                        + "FROM `countryenvparam` c "
                        + "WHERE 1=1 and "
                        + " `system`='" + system + "' and "
                        + " country='" + country + "' and "
                        + " environment ='" + env + "' ";

                Statement stmtBC = conn.createStatement();
                ResultSet rsBC = stmtBC.executeQuery(SQLBC);

                String lastBuild = "";
                String lastRev = "";

                if (rsBC.first()) {
                    lastBuild = rsBC.getString("Build");
                    lastRev = rsBC.getString("Revision");
                } else {
                    lastBuild = "NONE";
                    lastRev = "NONE";
                }
                stmtBC.close();
                rsBC.close();

                String AppliSQL = "SELECT distinct al.rel, al.application, brp.jenkinsbuildid, ap.deploytype from ( "
                        + "SELECT Application, max(`Release`) rel "
                        + " from buildrevisionparameters "
                        + " where build = '" + build + "'";
                if (lastBuild.equalsIgnoreCase(build)) {
                    AppliSQL += " and revision > '" + lastRev + "'";
                }
                AppliSQL += " and revision <= '" + revision + "'"
                        + " and `release` not like '%.%' and `release` not like '%e%' and `release` not like 'VC%' "
                        + "GROUP BY Application  ORDER BY Application) as al "
                        + "JOIN buildrevisionparameters brp "
                        + " ON brp.application=al.application and brp.release=al.rel and brp.build = '" + build + "'"
                        + "JOIN application ap "
                        + " ON ap.application=al.application "
                        + " and ap.system = '" + system + "'";

                Statement stmtA = conn.createStatement();
                ResultSet rsA = stmtA.executeQuery(AppliSQL);

                Statement stmtB = conn.createStatement();
                ResultSet rsB;

                IParameterService myParameterService = appContext.getBean(IParameterService.class);

                String JenkinsURL;
                JenkinsURL = myParameterService.findParameterByKey("jenkins_deploy_pipeline_url", "").getValue();
                String final_JenkinsURL = "";

                String DeployURL;
                String JenkinsAgent;
                while (rsA.next()) {
                    final_JenkinsURL = JenkinsURL.replaceAll("%APPLI%", rsA.getString("Application"));
            %>
            <tr>
                <td><%=rsA.getString("Application")%></td>
                <td><%=rsA.getString("rel")%></td>
                <td><%
                    // Looping on all Jenkins Agent for the country environment and deploytype values.

                    String JenkinsAgentSQL = "SELECT jenkinsagent "
                            + " FROM countryenvdeploytype "
                            + " WHERE country = '" + country + "'"
                            + " and `system` = '" + system + "'"
                            + " and environment = '" + env + "'"
                            + " and deploytype = '" + rsA.getString("ap.deploytype") + "'";
                    rsB = stmtB.executeQuery(JenkinsAgentSQL);
                    while (rsB.next()) {
                        JenkinsAgent = rsB.getString("jenkinsagent");
                        DeployURL = "JenkinsDeploy?application=" + rsA.getString("Application") + "&jenkinsagent=" + JenkinsAgent + "&country=" + country + "&deploytype=" + rsA.getString("ap.deploytype") + "&release=" + rsA.getString("rel") + "&jenkinsbuildid=" + rsA.getString("brp.jenkinsbuildid");
                    %>
                    <a href='<%=DeployURL%>' target='_blank'><%=JenkinsAgent%></a>
                    <% }%>
                </td>
                <td><a href='<%=final_JenkinsURL%>' target='_blank'>VIEW</a></td>
            </tr>
            <%
                    rsB.close();
                }
                stmtA.close();
                rsA.close();
                stmtB.close();
            %>
        </table>
        <br>

        <form method="get" name="doEvent" action="<%=formAction%>">
            <table>
                <tr>
                    <td><input id="cancel" type="button" value="Cancel" onclick="window.location.href='Environment.jsp?system=<%=system%>&country=<%=country%>&env=<%=env%>'"</td>
                    <td><input id="validate" type="submit" value="Validate and Send Notification"</td>
                        <% if (!event.equals("disableenvironment")) {%>
                    <td><input type="hidden" name="build" value="<%=build%>"></td>
                    <td><input type="hidden" name="revision" value="<%=revision%>"></td>
                    <td><input type="hidden" name="chain" value="<%=chain%>"></td>
                        <%}%>
                    <td><input type="hidden" name="system" value="<%=system%>"></td>
                    <td><input type="hidden" name="country" value="<%=country%>"></td>
                    <td><input type="hidden" name="env" value="<%=env%>"></td>
                </tr>
            </table>
        </form>

        <br>
        <a href="Environment.jsp?system=<%=system%>&country=<%=country%>&env=<%=env%>">Continue to Current Environment</a>

        <%
                stmtA.close();
                stmtBC.close();
                rsA.close();
                rsBC.close();
            } catch (Exception e) {
                MyLogger.log("Notification.jsp", Level.FATAL, Version.PROJECT_NAME_VERSION + " - Exception catched." + e.toString());
                out.println("<br> error message : " + e.getMessage() + " " + e.toString() + "<br>");

            } finally {
                try {
                    conn.close();
                } catch (Exception ex) {
                    MyLogger.log("Notification.jsp", Level.FATAL, Version.PROJECT_NAME_VERSION + " - Exception catched." + ex.toString());
                }
            }


        %>
        <br><% out.print(display_footer(DatePageStart));%>
    </body>
</html>
