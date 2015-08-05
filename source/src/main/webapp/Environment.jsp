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
<%@page import="org.cerberus.entity.CountryEnvParam"%>
<%@page import="org.cerberus.entity.CountryEnvLink"%>
<%@page import="org.cerberus.service.IDocumentationService"%>
<%@page import="org.cerberus.service.ICountryEnvParamService"%>
<%@page import="org.cerberus.service.ICountryEnvLinkService"%>
<%
	Date DatePageStart = new Date();
%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Environment Management</title>
        <link rel="stylesheet" 
              type="text/css" href="css/crb_style.css"
              />
        <link rel="shortcut icon" type="image/x-icon" href="images/favicon.ico" />
    </head>

    <body>

        <%@ include file="include/function.jsp" %>
        <%@ include file="include/header.jsp" %>

        <%
        	Connection conn = db.connect();
                    IDocumentationService docService = appContext.getBean(IDocumentationService.class);
                    String myLang = request.getAttribute("MyLang").toString();

                    try {

                        /* Parameter Setup */
                        String MySystem = "";
                        if (request.getParameter("system") != null && request.getParameter("system").compareTo("") != 0) {
                            MySystem = request.getParameter("system");
                        }

                        String country;
                        Boolean country_def;
                        if (request.getParameter("country") != null && request.getParameter("country").compareTo("") != 0) {
                            country = request.getParameter("country");
                            country_def = false;
                        } else {
                            country = new String("ALL");
                            country_def = true;
                        }


                        String env;
                        Boolean env_def;
                        if (request.getParameter("env") != null && request.getParameter("env").compareTo("") != 0) {
                            env = request.getParameter("env");
                            env_def = false;
                        } else {
                            env = new String("ALL");
                            env_def = true;
                        }


                        /* Filter part */

                        Statement stmtEnvgp = conn.createStatement();
                        Statement stmtBuild = conn.createStatement();
                        Statement stmtRev = conn.createStatement();
                        Statement stmtNextRev = conn.createStatement();
                        Statement stmtChain = conn.createStatement();
                        Statement stmtActive = conn.createStatement();
                        Statement stmtType = conn.createStatement();




                        /* Page Display - START */

                        Statement stmtCE = conn.createStatement();
                        Statement stmtCEcnt = conn.createStatement();


                        /* Country loop */
                        String PCE;
                        String PCE_cnt;
                        String Build;
                        String Revision;
                        String Type;
                        int i, j;



                        // Enrironment country Detail Page

                        PCE = "SELECT DISTINCT c.system, c.Country, c.Environment, c.Build, c.Revision, c.Chain, c.Active, c.Type, "
                                + "c.DistribList, c.EMailBodyRevision, c.EmailBodyChain, c.EmailBodyDisableEnvironment, "
                                + "c.maintenanceact, c.maintenancestr, c.maintenanceend "
                                + "FROM `countryenvparam` c "
                                + "WHERE 1=1 "
                                + " and System='" + MySystem + "' "
                                + " and Country='" + country + "' "
                                + " and Environment='" + env + "' ";
                        ResultSet rsPCE = stmtCE.executeQuery(PCE);
                        if (rsPCE.first()) {
                            Build = rsPCE.getString("c.Build");
                            Revision = rsPCE.getString("c.Revision");
                            Type = rsPCE.getString("c.Type");

                            String CerberusReportingURL = "ReportingExecution.jsp?System=%system%&amp;TcActive=Y&amp;Priority=All&amp;Environment=%env%&amp;Build=%build%&amp;Revision=%rev%&amp;Country=%country%&amp;Status=WORKING&amp;Apply=Apply";
                            String final_CerberusReportingURL;
                            final_CerberusReportingURL = CerberusReportingURL.replaceAll("%country%", country);
                            final_CerberusReportingURL = final_CerberusReportingURL.replaceAll("%env%", env);
                            final_CerberusReportingURL = final_CerberusReportingURL.replaceAll("%appli%", "");
                            final_CerberusReportingURL = final_CerberusReportingURL.replaceAll("%system%", MySystem);

                            if (Build != null && !Build.trim().equalsIgnoreCase("")
                                    && !Build.trim().equalsIgnoreCase("null")) {
                                final_CerberusReportingURL = final_CerberusReportingURL.replaceAll("%build%", Build);
                                final_CerberusReportingURL = final_CerberusReportingURL.replaceAll("%rev%", Revision);
                            } else {
                                final_CerberusReportingURL = final_CerberusReportingURL.replaceAll("%build%", "");
                                final_CerberusReportingURL = final_CerberusReportingURL.replaceAll("%rev%", "");
                            }
        %>
        <br>
        <table>
            <tr>
                <td>
                    <table border>
                        <tr id="header">
                            <td><%=docService.findLabelHTML("application", "system", "", myLang)%></td>
                            <td><%=docService.findLabelHTML("invariant", "country", "", myLang)%></td>
                            <td><%=docService.findLabelHTML("invariant", "environment", "", myLang)%></td>
                            <td><%=docService.findLabelHTML("buildrevisioninvariant", "versionname01", "", myLang)%></td>
                            <td><%=docService.findLabelHTML("buildrevisioninvariant", "versionname02", "", myLang)%></td>
                            <td><%=docService.findLabelHTML("countryenvparam", "chain", "", myLang)%></td>
                            <td><%=docService.findLabelHTML("countryenvparam", "active", "", myLang)%></td>
                            <td><%=docService.findLabelHTML("countryenvparam", "Type", "", myLang)%></td>
                            <td> </td>
                        </tr>
                        <tr>
                            <td><b><%=rsPCE.getString("c.System")%></b></td>
                            <td><b><%=rsPCE.getString("c.Country")%></b></td>
                            <td><b><%=rsPCE.getString("c.Environment")%></b></td>
                            <td><%=Build%></td>
                            <td><%=Revision%></td>
                            <td><%=rsPCE.getString("c.Chain").trim()%></td>
                            <td><%=rsPCE.getString("c.Active")%></td>
                            <td><%=rsPCE.getString("c.Type")%></td>
                            <td><a href="<%=final_CerberusReportingURL%>" target="_blank">Test Result</a></td>
                        </tr>
                    </table>
                </td>
                <td>
                    <%
                    	appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());

                                            ICountryEnvLinkService countryEnvLinkService = appContext.getBean(ICountryEnvLinkService.class);
                                            ICountryEnvParamService countryEnvParamService = appContext.getBean(ICountryEnvParamService.class);

                                            List<CountryEnvLink> countryEnvLinks = countryEnvLinkService.findCountryEnvLinkByCriteria(rsPCE.getString("c.System"), rsPCE.getString("c.Country"), rsPCE.getString("c.Environment"));
                    %>
                    <table border>
                        <tr><td colspan="9" style="background-color:lightgrey">Environment dependency.</td>
                        </tr>
                        <tr id="header">
                            <td><%=docService.findLabelHTML("application", "system", "", myLang)%></td>
                            <td><%=docService.findLabelHTML("invariant", "country", "", myLang)%></td>
                            <td><%=docService.findLabelHTML("invariant", "environment", "", myLang)%></td>
                            <td><%=docService.findLabelHTML("buildrevisioninvariant", "versionname01", "", myLang)%></td>
                            <td><%=docService.findLabelHTML("buildrevisioninvariant", "versionname02", "", myLang)%></td>
                            <td><%=docService.findLabelHTML("countryenvparam", "chain", "", myLang)%></td>
                            <td><%=docService.findLabelHTML("countryenvparam", "active", "", myLang)%></td>
                            <td><%=docService.findLabelHTML("countryenvparam", "Type", "", myLang)%></td>
                        </tr>
                        <%
                        	for (CountryEnvLink myLinkBuild : countryEnvLinks) {
                                                        CountryEnvParam mycountEnvParam = countryEnvParamService.findCountryEnvParamByKey(myLinkBuild.getSystemLink(), myLinkBuild.getCountryLink(), myLinkBuild.getEnvironmentLink());
                        %>
                        <tr>
                            <td><%=myLinkBuild.getSystemLink()%></td>
                            <td><%=myLinkBuild.getCountryLink()%></td>
                            <td><%=myLinkBuild.getEnvironmentLink()%></td>
                            <td><%=mycountEnvParam.getBuild()%></td>
                            <td><%=mycountEnvParam.getRevision()%></td>
                            <td><%=mycountEnvParam.getChain()%></td>
                            <td><%=mycountEnvParam.isActive()%></td>
                            <td><%=mycountEnvParam.getType()%></td>
                        </tr>
                        <%
                        	}
                        %>
                    </table>
                </td>
            </tr>
        </table>
        <br>

        <table>  
            <tr>
                <td style="background-color:lightgrey">Application list and corresponding connectivity parameters</td>
                <td style="background-color:lightgrey">Connectivity parameters for all applications</td>
                <td style="background-color:lightgrey">Database parameters for Cerberus SQL Execution</td>
            </tr>
            <tr>

                <td style="vertical-align: top">

                    <table border>
                        <tr id="header">
                            <td><%=docService.findLabelHTML("application", "Application", "", myLang)%></td>
                            <td><%=docService.findLabelHTML("countryenvironmentparameters", "IP", "", myLang)%></td>
                            <td><%=docService.findLabelHTML("countryenvironmentparameters", "Domain", "", myLang)%></td>
                            <td><%=docService.findLabelHTML("countryenvironmentparameters", "URL", "", myLang)%></td>
                            <td><%=docService.findLabelHTML("countryenvironmentparameters", "URLLOGIN", "", myLang)%></td>
                        </tr>
                        <%
                        	Statement stmtCEP = conn.createStatement();
                                                    String CEP = "SELECT DISTINCT c.Application, c.IP, c.domain, c.URL, c.URLLOGIN "
                                                            + "FROM `countryenvironmentparameters` c "
                                                            + "JOIN application a ON a.application = c.application "
                                                            + "WHERE 1=1 "
                                                            + " and c.System='" + MySystem + "' "
                                                            + " and c.Country='" + country + "' "
                                                            + " and c.Environment='" + env + "' "
                                                            + " ORDER by a.sort ";
                                                    ResultSet rsCEP = stmtCEP.executeQuery(CEP);
                                                    while (rsCEP.next()) {
                        %>          <tr>

                            <td><%=rsCEP.getString("Application")%></td>
                            <td><%=rsCEP.getString("IP")%></td>
                            <td><%=rsCEP.getString("domain")%></td>

                            <td><%=rsCEP.getString("URL")%></td>
                            <td><%=rsCEP.getString("URLLOGIN")%></td>
                        </tr>
                        <%
                        	}
                        %>

                    </table>

                </td>

                <td style="vertical-align: top">

                    <table border>
                        <tr id="header">
                            <td><%=docService.findLabelHTML("host", "Server", "", myLang)%></td>
                            <td><%=docService.findLabelHTML("host", "Session", "", myLang)%></td>
                            <td><%=docService.findLabelHTML("host", "host", "", myLang)%></td>
                            <td><%=docService.findLabelHTML("host", "port", "", myLang)%></td>
                            <td><%=docService.findLabelHTML("host", "secure", "", myLang)%></td>
                            <td><%=docService.findLabelHTML("host", "active", "", myLang)%></td>
                            <td> </td>
                        </tr>
                        <%
                        	Statement stmtH = conn.createStatement();
                                                    String H = "SELECT DISTINCT Server, Session, host, port, secure, active "
                                                            + "FROM `host` c "
                                                            + "JOIN invariant i ON i.value = c.server and i.idname='SERVER' "
                                                            + "WHERE 1=1 "
                                                            + " and c.System='" + MySystem + "' "
                                                            + " and c.Country='" + country + "' "
                                                            + " and c.Environment='" + env + "' "
                                                            + " ORDER by i.sort, c.Session ";
                                                    ResultSet rsH = stmtH.executeQuery(H);
                                                    while (rsH.next()) {
                        %>          <tr>
                            <td><%=rsH.getString("Server")%></td>
                            <td><%=rsH.getString("Session")%></td>
                            <td><%=rsH.getString("host")%></td>
                            <td><%=rsH.getString("port")%></td>
                            <td><%=rsH.getString("secure")%></td>
                            <td><%=rsH.getString("active")%></td>
                        </tr>
                        <%
                        	}
                        %>
                    </table>

                </td>

                <td style="vertical-align: top">

                    <table border>
                        <tr id="header">
                            <td><%=docService.findLabelHTML("countryenvironmentdatabase", "Database", "", myLang)%></td>
                            <td><%=docService.findLabelHTML("countryenvironmentdatabase", "ConnectionPoolName", "", myLang)%></td>
                        </tr>
                        <%
                        	Statement stmtD = conn.createStatement();
                                                    String D = "SELECT DISTINCT `Database`, ConnectionPoolName "
                                                            + "FROM `countryenvironmentdatabase` c "
                                                            + "WHERE 1=1 "
                                                            + " and c.System='" + MySystem + "' "
                                                            + " and c.Country='" + country + "' "
                                                            + " and c.Environment='" + env + "' "
                                                            + " ORDER by `Database` ";
                                                    ResultSet rsD = stmtH.executeQuery(D);
                                                    while (rsD.next()) {
                        %>          <tr>
                            <td><%=rsD.getString("Database")%></td>
                            <td><%=rsD.getString("ConnectionPoolName")%></td>
                        </tr>
                        <%
                        	}
                        %>
                    </table>

                </td>


            </tr>
        </table>            

        <br></br>


        <table>
            <tr><td>
                    <table border style="height:240px">
                        <tr id="header" style="height:15px">
                            <td colspan="2">
                                Environment management
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <form method="get" action="Notification.jsp">
                                    <input type="hidden" name="event" value="disableenvironment"/>
                                    <input type="hidden" name="system" value="<%=MySystem%>"/>
                                    <input type="hidden" name="country" value="<%=country%>"/>
                                    <input type="hidden" name="env" value="<%=env%>"/>
                                    <input type="hidden" name="build" value="<%=Build%>"/>
                                    <input type="hidden" name="revision" value="<%=Revision%>"/>
                                    <button name="buttonDisableEnv" <%if (rsPCE.getString("c.Active").equalsIgnoreCase("N")) {
                                            out.print("disabled=\"true\"");
                                        }%> >Disable environment</button>
                                </form>
                                <br></td>
                        </tr>
                        <tr>
                            <td>
                                <form method="get" action="Notification.jsp">
                                    <input type="hidden" name="event" value="newbuildrevision"/>
                                    <input type="hidden" name="system" value="<%=MySystem%>"/>
                                    <input type="hidden" name="country" value="<%=country%>"/>
                                    <input type="hidden" name="env" value="<%=env%>"/>

                                    <ftxt>New Sprint</ftxt> <select id="build" name="build" style="width: 150px"><%
 	String BuildAct = "";
                                         String buildSQL = "SELECT versionname "
                                                 + "FROM buildrevisioninvariant "
                                                 + "WHERE level = 1 and system ='" + MySystem + "' ";
                                         if (Build != null && !Build.trim().equalsIgnoreCase("") && !Build.trim().equalsIgnoreCase("null")) {
                                             buildSQL += " and seq >= (SELECT seq from buildrevisioninvariant where level = 1 and system ='" + MySystem + "' and versionname='" + Build + "') ";
                                         }
                                         buildSQL += " ORDER BY seq ASC";
                                         ResultSet rsBuild = stmtBuild.executeQuery(buildSQL);
                                         while (rsBuild.next()) {
 %><option style="width: 150px" value="<%=rsBuild.getString(1)%>"><%=rsBuild.getString(1)%></option>
                                        <%
                                        	BuildAct = rsBuild.getString(1);
                                                                                    }
                                        %></select>
                                    <br><ftxt>New Revision</ftxt> <select id="revision" name="revision" style="width: 150px"><%
 	String RevAct = "";
                                         String RevSQL = "SELECT versionname "
                                                 + "FROM buildrevisioninvariant "
                                                 + "WHERE level = 2 and system ='" + MySystem + "' ";
                                         RevSQL += "ORDER BY seq ASC";
                                         ResultSet rsRev = stmtRev.executeQuery(RevSQL);
                                         String NextRevSQL = "SELECT versionname, seq from buildrevisioninvariant where level = 2 and system ='" + MySystem + "' ";
                                         if (Revision != null && !Revision.trim().equalsIgnoreCase("") && !Revision.trim().equalsIgnoreCase("null")) {
                                             NextRevSQL += " and seq > (SELECT seq from buildrevisioninvariant where level = 2 and system ='" + MySystem + "' and versionname='" + Revision + "') ";
                                         }
                                         NextRevSQL += " ORDER BY seq ASC";
                                         String NextRev = Revision;
                                         ResultSet rsNextRev = stmtNextRev.executeQuery(NextRevSQL);
                                         if (rsNextRev.first()) {
                                             NextRev = rsNextRev.getString("versionname");
                                         }
                                         while (rsRev.next()) {
 %><option style="width: 150px" value="<%=rsRev.getString(1)%>" <%=NextRev.compareTo(rsRev.getString(1)) == 0 ? " SELECTED " : ""%>><%=rsRev.getString(1)%></option>
                                        <%
                                        	RevAct = rsRev.getString(1);
                                                                                    }
                                        %></select>
                                    <br><button name="buttonNewBuildRev" <%if (rsPCE.getString("c.Active").equalsIgnoreCase("Y")) {
                                            out.print("disabled=\"true\"");
                                        }%> >New Sprint/Revision</button>
                                </form>
                                <br></td>
                        </tr>
                        <tr>
                            <td>
                                <form method="get" action="Notification.jsp">
                                    <input type="hidden" name="event" value="newchain"/>
                                    <input type="hidden" name="system" value="<%=MySystem%>"/>
                                    <input type="hidden" name="country" value="<%=country%>"/>
                                    <input type="hidden" name="env" value="<%=env%>"/>
                                    <input id="buildAct" name="build" type="hidden" value="<%=Build%>"/>
                                    <input id="revAct" name="revision" type="hidden" value="<%=Revision%>"/>
                                    <ftxt>New Chain</ftxt> 
                                    <select id="chain" style="width: 150px" name="chain">
                                        <%
                                        	Statement stmtQuery = conn.createStatement();
                                                                                    String sq = "SELECT Batch, Description from batchinvariant ";
                                                                                    ResultSet q = stmtQuery.executeQuery(sq);
                                        %><option value="" SELECTED></option><%
                                        	if (q.first())
                                                                                        do {
                                        %>
                                        <option value="<%=q.getString("batch")%>"><%=q.getString("batch")%> - <%=q.getString("Description")%></option>
                                        <%
                                        	} while (q.next());
                                        %>
                                    </select>
                                    <%
                                    	q.close();
                                                                            stmtQuery.close();
                                    %>
                                    <br><button name="buttonNewChain" <%if (!rsPCE.getString("c.Active").equalsIgnoreCase("Y") || 
                                            (!rsPCE.getString("c.Chain").trim().equalsIgnoreCase("Y") &&  !rsPCE.getString("c.Chain").trim().isEmpty())) {
                                            out.print("disabled=\"disabled\"");
                                        }%>>New Chain</button>
                                </form>
                            </td>
                        </tr>

                    </table>
                </td>
                <td>

                    <table>
                        <tr><td colspan="2" style="alignment-baseline: central; text-align: center; background-color: lightgrey">Last Modifications and Batch executions.</td></tr>
                        <tr>
                            <td style="vertical-align: top">
                                <table border style="height:240px">
                                    <tr id="header" style="height:15px">
                                        <td><%=docService.findLabelHTML("countryenvparam_log", "datecre", "", myLang)%></td>
                                        <td><%=docService.findLabelHTML("countryenvparam_log", "Description", "", myLang)%></td>
                                        <td><%=docService.findLabelHTML("buildrevisioninvariant", "versionname01", "", myLang)%></td>
                                        <td><%=docService.findLabelHTML("buildrevisioninvariant", "versionname02", "", myLang)%></td>
                                    </tr>
                                    <%
                                    	Statement stmtCEL = conn.createStatement();
                                                                            String CEL = "SELECT DISTINCT DATE_FORMAT(l.datecre,'%Y-%m-%d %H:%i') datecre, l.id, l.Country, l.Environment, l.Build, l.Revision, l.Chain, l.Description "
                                                                                    + "FROM `countryenvparam_log` l "
                                                                                    + "WHERE 1=1 "
                                                                                    + " and `System`='" + MySystem + "' "
                                                                                    + " and Country='" + country + "' "
                                                                                    + " and Environment='" + env + "' "
                                                                                    + " ORDER by l.id desc"
                                                                                    + " LIMIT 10";
                                                                            ResultSet rsCEL = stmtCEL.executeQuery(CEL);
                                                                            while (rsCEL.next()) {
                                    %>
                                    <tr>
                                        <td style="width :150px;"><%=rsCEL.getString("datecre")%></td>
                                        <td style="width :200px;"><%=rsCEL.getString("l.Description")%></td>
                                        <td style="width :70px;"><%=rsCEL.getString("l.Build") == null ? "" : rsCEL.getString("l.Build")%></td>
                                        <td style="width :70px;"><%=rsCEL.getString("l.Revision") == null ? "" : rsCEL.getString("l.Revision")%></td>
                                    </tr>
                                    <%
                                    	}
                                    %>
                                </table></td><td style="vertical-align: top">
                                <table border>
                                    <tr id="header" style="height:15px" valign="top">
                                        <td><%=docService.findLabelHTML("countryenvparam_log", "datecre", "", myLang)%></td>
                                        <td><%=docService.findLabelHTML("countryenvparam", "chain", "", myLang)%></td>
                                    </tr>
                                    <%
                                    	Statement stmtBAT = conn.createStatement();
                                                                            String BAT = "SELECT DISTINCT DATE_FORMAT(DateBatch,'%Y-%m-%d %H:%i') DateBatch, Batch "
                                                                                    + "FROM `buildrevisionbatch` l "
                                                                                    + "WHERE 1=1 "
                                                                                    + " and `System`='" + MySystem + "' "
                                                                                    + " and Country='" + country + "' "
                                                                                    + " and Environment='" + env + "' "
                                                                                    + " ORDER by DateBatch desc"
                                                                                    + " LIMIT 10";
                                                                            ResultSet rsBAT = stmtBAT.executeQuery(BAT);
                                                                            while (rsBAT.next()) {
                                    %>
                                    <tr valign="top">
                                        <td style="width :200px;"><%=rsBAT.getString("DateBatch") == null ? "" : rsBAT.getString("DateBatch")%></td>
                                        <td style="width :70px;"><%=rsBAT.getString("Batch") == null ? "" : rsBAT.getString("Batch")%></td>
                                    </tr>
                                    <%
                                    	}
                                    %></table></td>

                        </tr></table>

                </td>
            </tr>
        </table>

        <br>

        <form method="post" action="UpdateCountryEnv">
            <input type="hidden" name="system" value="<%=MySystem%>"/>
            <input type="hidden" name="country" value="<%=country%>"/>
            <input type="hidden" name="env" value="<%=env%>"/>
            <table border>
                <tr id="header">
                    <td colspan="2">
                        Country Environment Information
                    </td>
                </tr>
                <tr>
                    <td>
                <ftxt><%=docService.findLabelHTML("countryenvparam", "type", "", myLang)%></ftxt> 
                </td>
                <td>
                    <select id="type" name="type" style="width: 200px">
                        <option style="width: 200px" value="">-- Please Select a Value --</option>
                        <%
                        	ResultSet rsType = stmtType.executeQuery("SELECT value, description "
                                                            + "FROM invariant "
                                                            + "WHERE idname = 'ENVTYPE' "
                                                            + "ORDER BY sort ASC");
                                                    while (rsType.next()) {
                        %><option style="width: 200px" value="<%=rsType.getString(1)%>" <%=Type.compareTo(rsType.getString(1)) == 0 ? " SELECTED " : ""%>><%=rsType.getString(1)%></option>
                        <%
                        	}
                        %></select>
                </td>
                </tr>
                <tr>
                    <td>
                <ftxt><%=docService.findLabelHTML("countryenvparam", "DistribList", "", myLang)%></ftxt> 
                </td>
                <td>
                    <textarea id="distriblist" name="distriblist" cols="80" rows="10"><%=rsPCE.getString("c.DistribList") == null ? "" : rsPCE.getString("c.DistribList")%></textarea><br>
                </td>
                </tr>
                <tr>
                    <td>
                <ftxt><%=docService.findLabelHTML("countryenvparam", "EMailBodyDisableEnvironment", "", myLang)%></ftxt> 
                </td>
                <td>
                    <textarea id="bodydisenv" name="bodydisenv" cols="80" rows="10"><%=rsPCE.getString("c.EMailBodyDisableEnvironment") == null ? "" : rsPCE.getString("c.EMailBodyDisableEnvironment")%></textarea><br>
                </td>
                </tr>
                <tr>
                    <td>
                <ftxt><%=docService.findLabelHTML("countryenvparam", "EMailBodyRevision", "", myLang)%></ftxt> 
                </td>
                <td>
                    <textarea id="bodyrev" name="bodyrev" cols="80" rows="10"><%=rsPCE.getString("c.EMailBodyRevision") == null ? "" : rsPCE.getString("c.EMailBodyRevision")%></textarea><br>
                </td>
                </tr>
                <tr>
                    <td>
                <ftxt><%=docService.findLabelHTML("countryenvparam", "EMailBodyChain", "", myLang)%></ftxt> 
                </td>
                <td>
                    <textarea id="bodychain" name="bodychain" cols="80" rows="10"><%=rsPCE.getString("c.EMailBodyChain") == null ? "" : rsPCE.getString("c.EMailBodyChain")%></textarea><br>
                </td>
                </tr>
                <tr>
                    <td>
                <ftxt><%=docService.findLabelHTML("countryenvparam", "maintenanceact", "", myLang)%></ftxt> 
                </td>
                <td>
                    <%=rsPCE.getString("c.maintenanceact") == null ? "" : rsPCE.getString("c.maintenanceact")%>
                </td>
                </tr>
                <tr>
                    <td>
                <ftxt><%=docService.findLabelHTML("countryenvparam", "maintenancestr", "", myLang)%></ftxt> 
                </td>
                <td>
                    <%=rsPCE.getString("c.maintenancestr") == null ? "" : rsPCE.getString("c.maintenancestr")%>
                </td>
                </tr>
                <tr>
                    <td>
                <ftxt><%=docService.findLabelHTML("countryenvparam", "maintenanceend", "", myLang)%></ftxt> 
                </td>
                <td>
                    <%=rsPCE.getString("c.maintenanceend") == null ? "" : rsPCE.getString("c.maintenanceend")%>
                </td>
                </tr>
                <tr>
                    <td colspan="2">
                        <button name="buttonUpdateInfo">Update</button>
                    </td>
                </tr>
            </table>
        </form>

        <%
        	}



                                /* Page Display - END */

                                stmtActive.close();
                                stmtBuild.close();
                                stmtCE.close();
                                stmtCEcnt.close();
                                stmtChain.close();
                                stmtEnvgp.close();
                                stmtNextRev.close();
                                stmtRev.close();
                                stmtType.close();



                            } catch (Exception e) {
                                MyLogger.log("Environment.jsp", Level.FATAL, Infos.getInstance().getProjectNameAndVersion() + " - Exception catched." + e.toString());
                                out.println("<br> error message : " + e.getMessage() + " " + e.toString() + "<br>");
                            } finally {
                                try {
                                    conn.close();
                                } catch (Exception ex) {
                                    MyLogger.log("Environment.jsp", Level.FATAL, Infos.getInstance().getProjectNameAndVersion() + " - Exception catched." + ex.toString());
                                }
                            }
        %>
        <br><% out.print(display_footer(DatePageStart));%>
    </body>
</html>
