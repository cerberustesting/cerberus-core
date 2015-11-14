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
<%@page import="org.cerberus.crud.service.IDocumentationService"%>
<%@page import="org.cerberus.crud.service.IParameterService"%>
<%@page import="org.cerberus.crud.service.ICountryEnvParamService"%>
<%@page import="org.cerberus.crud.service.ICountryEnvDeployTypeService"%>
<%@page import="org.cerberus.crud.service.IBuildRevisionParametersService"%>
<%@page import="org.cerberus.crud.service.IApplicationService"%>
<%@page import="org.cerberus.service.email.IEmailGeneration"%>
<%@page import="org.cerberus.util.StringUtil"%>
<%@ page import="org.cerberus.crud.entity.CountryEnvParam" %>
<%@ page import="org.cerberus.crud.entity.BuildRevisionParameters" %>
<%@ page import="org.cerberus.crud.entity.Application" %>
<%
	Date DatePageStart = new Date();
%>

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
        	IDocumentationService docService = appContext.getBean(IDocumentationService.class);
                    ICountryEnvParamService countryEnvParamService = appContext.getBean(ICountryEnvParamService.class);
                    ICountryEnvDeployTypeService countryEnvDeployTypeService = appContext.getBean(ICountryEnvDeployTypeService.class);
                    IBuildRevisionParametersService buildRevisionParametersService = appContext.getBean(IBuildRevisionParametersService.class);
                    IApplicationService applicationService = appContext.getBean(IApplicationService.class);

                    try {

                        /* Parameter Setup */
                        
                        String myLang = request.getAttribute("MyLang").toString();


                        String system = "";
                        if (request.getParameter("system") != null && request.getParameter("system").compareTo("") != 0) {
                            system = request.getParameter("system");
                        }

                        String country;
                        if (request.getParameter("country") != null && request.getParameter("country").compareTo("") != 0) {
                            country = request.getParameter("country");
                        } else {
                            country = "ALL";
                        }

                        String env;
                        if (request.getParameter("env") != null && request.getParameter("env").compareTo("") != 0) {
                            env = request.getParameter("env");
                        } else {
                            env = "ALL";
                        }

                        String event;
                        if (request.getParameter("event") != null && request.getParameter("event").compareTo("") != 0) {
                            event = request.getParameter("event");

                        } else {
                            event = "NONE";
                        }

                        String build;
                        if (request.getParameter("build") != null && request.getParameter("build").compareTo("ALL") != 0) {
                            build = request.getParameter("build");
                        } else {
                            build = "NONE";
                        }

                        String revision;
                        if (request.getParameter("revision") != null && request.getParameter("revision").compareTo("ALL") != 0) {
                            revision = request.getParameter("revision");
                        } else {
                            revision = "NONE";
                        }

                        String chain;
                        if (request.getParameter("chain") != null && request.getParameter("chain").compareTo("") != 0) {
                            chain = request.getParameter("chain");
                        } else {
                            chain = "NONE";
                        }


                        // Generate the content of the email
                        IEmailGeneration myEmailGeneration = appContext.getBean(IEmailGeneration.class);
                        String eMailContent = "";
                        String formAction = "";

                        if (!StringUtil.isNullOrEmpty(event)) {

                            if (event.equals("newbuildrevision")) {
                                eMailContent = myEmailGeneration.EmailGenerationRevisionChange(system, country, env, build, revision);
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
                <td><%=docService.findLabelHTML("page_notification", "To", "", myLang)%></td>
                <td><table border><tr><td><%=to%></td></tr></table></td>
            </tr> 
            <tr>
                <td><%=docService.findLabelHTML("page_notification", "Cc", "", myLang)%></td>
                <td><table border><tr><td><%=cc%></td></tr></table></td>
            </tr> 
            <tr>
                <td><%=docService.findLabelHTML("page_notification", "Subject", "", myLang)%></td>
                <td><table border><tr><td><%=subject%></td></tr></table></td>
            </tr>
            <tr>
                <td><%=docService.findLabelHTML("page_notification", "Body", "", myLang)%></td>
                <td><table border><tr><td><%=body%></td></tr></table></td>
            </tr>
        </table>
        <br>
        <% // We disply the install instructions only if the event is to deploy a new build / revision.
                        if (!StringUtil.isNullOrEmpty(event)) {

                            if (event.equals("newbuildrevision")) {
        %>
        <table border>
            <tr>
                <td colspan="3" style="background-color: lightyellow; text-align: center">Application to deploy.</td>
            </tr>
            <tr><td>Application</td><td>Release</td><td>Deploy Links</td><td>View the Jenkins Pipe</td></tr>
            <%
            	String lastBuild;
                            String lastRev;

                            try{
                                CountryEnvParam countryEnvParam = countryEnvParamService.findCountryEnvParamByKey(system, country, env);
                                lastBuild = countryEnvParam.getBuild();
                                lastRev = countryEnvParam.getRevision();
                            } catch (CerberusException ex) {
                                lastBuild = "Empty";
                                lastRev = "Empty";
                            }

                            IParameterService myParameterService = appContext.getBean(IParameterService.class);

                            String JenkinsURL = myParameterService.findParameterByKey("jenkins_deploy_pipeline_url", "").getValue();
                            
                            List<BuildRevisionParameters> myList = null;
                            // We start by build Revision that have continious integration.
                            try{
                                myList = buildRevisionParametersService.convert(buildRevisionParametersService.readMaxSVNReleasePerApplication(system, build, revision, lastBuild, lastRev));
                            for (BuildRevisionParameters brp : myList) {

                                String final_JenkinsURL = JenkinsURL.replaceAll("%APPLI%", brp.getApplication());
            %>
            <tr>
                <td><%=brp.getApplication()%></td>
                <td><%=brp.getRelease()%></td>
                <td><%
                	// Looping on all Jenkins Agent for the country environment and deploytype values.
                                    Application app = applicationService.convert(applicationService.readByKey(brp.getApplication()));
                                    for (String JenkinsAgent : countryEnvDeployTypeService.findJenkinsAgentByKey(system, country, env, app.getDeploytype())) {
                                        String DeployURL = "JenkinsDeploy?application=" + brp.getApplication() + "&jenkinsagent=" + JenkinsAgent + "&country=" + country + "&deploytype=" + app.getDeploytype() + "&release=" + brp.getRelease() + "&jenkinsbuildid=" + brp.getJenkinsBuildId();
                %>
                        <a href='<%=DeployURL%>' target='_blank'><%=JenkinsAgent%></a>
                    <%
                    	}
                    %>
                </td>
                <td><a href='<%=final_JenkinsURL%>' target='_blank'>VIEW</a></td>
            </tr>
            <%
            	}
                            } catch (CerberusException ex) {
                            }
                            // We continue with release that have manual instruction in link.
                            myList = null;
                            try{
                                myList = buildRevisionParametersService.convert(buildRevisionParametersService.readNonSVNRelease(system, build, revision, lastBuild, lastRev));
                            for (BuildRevisionParameters brp : myList) {

                                String final_JenkinsURL = JenkinsURL.replaceAll("%APPLI%", brp.getApplication());
            %>
            <tr>
                <td><%=brp.getApplication()%></td>
                <td><%=brp.getRelease()%></td>
                <td><a href='<%=brp.getLink()%>' target='_blank'>INSTRUCTIONS </a></td>
                <td></td>
            </tr>
            <%
            	}
                            } catch (CerberusException ex) {
                            }
                            
            %>
        </table>
        <%
                            }
                        }
        %>
        <br>

        <form method="get" name="doEvent" action="<%=formAction%>">
            <table>
                <tr>
                    <td><input id="cancel" type="button" value="Cancel" onclick="window.location.href='Environment.jsp?system=<%=system%>&country=<%=country%>&env=<%=env%>'"></td>
                    <td><input id="validate" type="submit" value="Validate and Send Notification"></td>
                        <%
                        	if (!event.equals("disableenvironment")) {
                        %>
                    <td><input type="hidden" name="build" value="<%=build%>"></td>
                    <td><input type="hidden" name="revision" value="<%=revision%>"></td>
                    <td><input type="hidden" name="chain" value="<%=chain%>"></td>
                        <%
                        	}
                        %>
                    <td><input type="hidden" name="system" value="<%=system%>"></td>
                    <td><input type="hidden" name="country" value="<%=country%>"></td>
                    <td><input type="hidden" name="env" value="<%=env%>"></td>
                </tr>
            </table>
        </form>

        <br>
        <a href="Environment.jsp?system=<%=system%>&country=<%=country%>&env=<%=env%>">Continue to Current Environment</a>

        <%
        	} catch (Exception e) {
                                MyLogger.log("Notification.jsp", Level.FATAL, Infos.getInstance().getProjectNameAndVersion() + " - Exception catched. " + e.toString());
                                out.println("<br> error message : " + e.getMessage() + " " + e.toString() + "<br>");

                            }
        %>
        <br><% out.print(display_footer(DatePageStart));%>
    </body>
</html>
