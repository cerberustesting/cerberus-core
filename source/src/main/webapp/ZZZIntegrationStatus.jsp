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
<%@page import="java.util.Collection"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.sql.SQLException"%>
<%@page import="java.sql.Connection"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>
<%@page import="java.sql.ResultSet"%>
<%@page import="java.sql.Statement"%>
<%@page import="org.cerberus.crud.service.IDocumentationService"%>
<%@page import="org.apache.log4j.Level"%>
<%@page import="org.apache.log4j.Logger"%>
<%
	Date DatePageStart = new Date();
%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Integration Status</title>
        <link rel="stylesheet" 
              type="text/css" href="css/crb_style.css"
              />
        <link rel="shortcut icon" type="image/x-icon" href="images/favicon.ico" />
    </head>

    <body>

        <%@ include file="include/function.jsp" %>
        <%@ include file="include/header.jsp" %>

        <%
        	String NBDAYS = "14";
                            if (request.getParameter("nbdays") != null) {
                                NBDAYS = request.getParameter("nbdays");
                            }

                            String envgp;
                            Boolean envgp_def;
                            if (request.getParameter("envgp") != null && request.getParameter("envgp").compareTo("") != 0) {
                                envgp = request.getParameter("envgp");;
                                envgp_def = false;
                            } else {
                                envgp = new String("ALL");
                                envgp_def = true;
                            }

                            String MySystem = ParameterParserUtil.parseStringParam(request.getAttribute("MySystem").toString(), "");
                            Logger.getLogger("IntegrationStatus.jsp").log(Level.DEBUG, Infos.getInstance().getProjectNameAndVersion() + " - System : " + MySystem);
                            
                            String myLang = request.getAttribute("MyLang").toString();

        %>

        <%
        	Connection conn = db.connect();
                            IDocumentationService docService = appContext.getBean(IDocumentationService.class);

                            try {

                                Statement stmtEnvgp = conn.createStatement();
        %>

        <form method="get"  name="EnvFilters" id="EnvFilters">
            <table>
                <tr>
                    <td style="alignment-baseline: central; text-align: center; background-color: lightgrey">Last modifications done on environments since the last <%=NBDAYS%> days<br>
                <ftxt><%=docService.findLabelHTML("invariant", "environmentgp", "", myLang)%></ftxt> <select id="envgp" name="envgp" style="width: 80px" OnChange ="document.EnvFilters.submit()">
                    <option style="width: 200px" value="ALL">-- ALL --</option>
                    <%
                    	ResultSet rsEnvgp = stmtEnvgp.executeQuery("SELECT distinct gp1 "
                                                                        + "FROM invariant "
                                                                        + "WHERE idname = 'ENVIRONMENT' "
                                                                        + "ORDER BY sort ASC");
                                                                while (rsEnvgp.next()) {
                    %><option style="width: 200px" value="<%=rsEnvgp.getString(1)%>" <%=envgp.compareTo(rsEnvgp.getString(1)) == 0 ? " SELECTED " : ""%>><%=rsEnvgp.getString(1)%></option>
                    <%
                    	}
                    %></select>
                <ftxt><%=docService.findLabelHTML("invariant", "FILTERNBDAYS", "", myLang)%></ftxt> <select id="nbdays" name="nbdays" style="width: 80px" OnChange ="document.EnvFilters.submit()">
                    <%
                    	ResultSet rsNbDays = stmtEnvgp.executeQuery("SELECT value, description "
                                                                        + "FROM invariant "
                                                                        + "WHERE idname = 'FILTERNBDAYS' "
                                                                        + "ORDER BY sort ASC");
                                                                while (rsNbDays.next()) {
                    %><option style="width: 200px" value="<%=rsNbDays.getString(1)%>" <%=NBDAYS.compareTo(rsNbDays.getString(1)) == 0 ? " SELECTED " : ""%>><%=rsNbDays.getString(1)%> - <%=rsNbDays.getString(2)%></option>
                    <%
                    	}
                    %></select>
                </td>
                <td style="alignment-baseline: central; text-align: center; background-color: lightgrey">Environment Status</td>
                </tr>
                <tr><td valign="top">
                        </form>



                        <table>
                            <tr>
                                <%
                                	int i = 0;
                                                                                                    String ClogSQL;
                                                                                                    ResultSet rsClog;
                                                                                                    Statement stmtClog = conn.createStatement();
                                                                                                    String ClogdSQL;
                                                                                                    ResultSet rsClogd;
                                                                                                    Statement stmtClogd = conn.createStatement();
                                                                                                    ClogSQL = "SELECT distinct cl.country FROM countryenvparam_log cl ";
                                                                                                    ClogSQL += "JOIN invariant i on i.value=cl.country and i.idname='COUNTRY' "
                                                                                                            + " where TO_DAYS(NOW()) - TO_DAYS(cl.datecre) <= " + NBDAYS
                                                                                                            + " and build != '' and `System`='" + MySystem + "'"
                                                                                                            + " order by i.sort ";
                                                                                                    rsClog = stmtClog.executeQuery(ClogSQL);
                                                                                                    while (rsClog.next()) {
                                %> <td valign="top" style="font-size: small"><%=rsClog.getString("cl.country")%><br>
                                    <%
                                    	ClogdSQL = "SELECT date_format(cl.datecre,'%d/%m') d, cl.Environment, cl.Description, cl.Build, cl.Revision, cl.chain "
                                                                                                                        + "FROM countryenvparam_log cl "
                                                                                                                        + "JOIN invariant i on i.value=cl.Environment and i.idname='ENVIRONMENT' ";

                                                                                                                ClogdSQL += " WHERE cl.country = '" + rsClog.getString("cl.country") + "' "
                                                                                                                        + " AND TO_DAYS(NOW()) - TO_DAYS(cl.datecre) <= " + NBDAYS
                                                                                                                        + " and cl.build != '' and `System`='" + MySystem + "'";
                                                                                                                if (envgp.equalsIgnoreCase("ALL") == false) {
                                                                                                                    ClogdSQL += " and i.gp1 = '" + envgp + "'";
                                                                                                                }
                                                                                                                ClogdSQL += " ORDER BY cl.id desc ";
                                                                                                                rsClogd = stmtClogd.executeQuery(ClogdSQL);
                                                                                                                while (rsClogd.next()) {
                                    %>                        <b><%=rsClogd.getString("d")%></b> [<%=rsClogd.getString("Environment")%>] <%=rsClogd.getString("Build")%><%=rsClogd.getString("Revision")%><br>
                                    <%
                                    	}
                                    %>

                                </td>
                                <%
                                	}
                                %>
                            </tr>
                        </table>

                    </td><td valign="top">

                        <%
                        	String BRSQL;
                                                                            ResultSet rsBuild;
                                                                            Statement stmtBuild = conn.createStatement();
                                                                            BRSQL = "SELECT distinct c.Build, c.Revision, PROD.cnt PROD, UAT.cnt UAT, QA.cnt QA, DEV.cnt DEV "
                                                                                    + "FROM `countryenvparam` c "
                                                                                    + "left outer join ( "
                                                                                    + "select Build, Revision, count(*) cnt from countryenvparam "
                                                                                    + "JOIN invariant i ON i.value=Environment and i.idname='ENVIRONMENT' "
                                                                                    + "where gp1='PROD' and build is not null and build<>'' and Active='Y' and `System`='" + MySystem + "' "
                                                                                    + "GROUP BY Build, Revision) as PROD "
                                                                                    + "on PROD.Build=c.Build and PROD.Revision=c.Revision "
                                                                                    + "left outer join ( "
                                                                                    + "select Build, Revision, count(*) cnt from countryenvparam "
                                                                                    + "JOIN invariant i ON i.value=Environment and i.idname='ENVIRONMENT' "
                                                                                    + "where gp1='UAT' and build is not null and build<>'' and Active='Y' and `System`='" + MySystem + "' "
                                                                                    + "GROUP BY Build, Revision) as UAT "
                                                                                    + "on UAT.Build=c.Build and UAT.Revision=c.Revision "
                                                                                    + "left outer join ( "
                                                                                    + "select Build, Revision, count(*) cnt from countryenvparam "
                                                                                    + "JOIN invariant i ON i.value=Environment and i.idname='ENVIRONMENT' "
                                                                                    + "where gp1='QA' and build is not null and build<>'' and Active='Y' and `System`='" + MySystem + "' "
                                                                                    + "GROUP BY Build, Revision) as QA "
                                                                                    + "on QA.Build=c.Build and QA.Revision=c.Revision "
                                                                                    + "left outer join ( "
                                                                                    + "select Build, Revision, count(*) cnt from countryenvparam "
                                                                                    + "JOIN invariant i ON i.value=Environment and i.idname='ENVIRONMENT' "
                                                                                    + "where gp1='DEV' and build is not null and build<>'' and Active='Y' and `System`='" + MySystem + "' "
                                                                                    + "GROUP BY Build, Revision) as DEV "
                                                                                    + "on DEV.Build=c.Build and DEV.Revision=c.Revision "
                                                                                    + "WHERE c.build is not null and c.build not in ('','NA') and Active='Y' and `System`='" + MySystem + "' "
                                                                                    + "order by c.Build asc, c.Revision asc";
%>
                        <table  style="text-align: left; border-collapse:collapse ; border-color: gainsboro" border="1">
                            <tr id="header">
                                <td><b><%=docService.findLabelHTML("buildrevisioninvariant", "versionname01", "", myLang)%></b></td>
                                <td><b><%=docService.findLabelHTML("buildrevisioninvariant", "versionname02", "", myLang)%></b></td>
                                <td><%=docService.findLabelHTML("page_integrationstatus", "DEV", "", myLang)%></td>
                                <td><%=docService.findLabelHTML("page_integrationstatus", "QA", "", myLang)%></td>
                                <td><%=docService.findLabelHTML("page_integrationstatus", "UAT", "", myLang)%></td>
                                <td><%=docService.findLabelHTML("page_integrationstatus", "PROD", "", myLang)%></td>
                            </tr><%
                            	rsBuild = stmtBuild.executeQuery(BRSQL);
                                                                                        while (rsBuild.next()) {
                            %>
                            <tr>
                                <td><b><%=rsBuild.getString("Build")%></b></td>
                                <td><b><%=rsBuild.getString("Revision")%></b></td>
                                <td><a href="EnvironmentList.jsp?envgp=DEV&active=Y&build=<%=rsBuild.getString("Build")%>&revision=<%=rsBuild.getString("Revision")%>"><%=rsBuild.getString("DEV") != null ? rsBuild.getString("DEV") : ""%></a></td>
                                <td><a href="EnvironmentList.jsp?envgp=QA&active=Y&build=<%=rsBuild.getString("Build")%>&revision=<%=rsBuild.getString("Revision")%>"><%=rsBuild.getString("QA") != null ? rsBuild.getString("QA") : ""%></a></td>
                                <td><a href="EnvironmentList.jsp?envgp=UAT&active=Y&build=<%=rsBuild.getString("Build")%>&revision=<%=rsBuild.getString("Revision")%>"><%=rsBuild.getString("UAT") != null ? rsBuild.getString("UAT") : ""%></a></td>
                                <td><a href="EnvironmentList.jsp?envgp=PROD&active=Y&build=<%=rsBuild.getString("Build")%>&revision=<%=rsBuild.getString("Revision")%>"><%=rsBuild.getString("PROD") != null ? rsBuild.getString("PROD") : ""%></a></td>
                            </tr>

                            <%
                            	}
                            %>
                        </table>

                    </td>
                </tr>
            </table>
            <%
            	stmtBuild.close();
                                            stmtClog.close();
                                            stmtClogd.close();
                                            stmtEnvgp.close();
                                            rsBuild.close();
                                            rsClog.close();
                                            rsEnvgp.close();

                                        } catch (Exception e) {
                                            Logger.getLogger("IntegrationStatus.jsp").log(Level.FATAL, Infos.getInstance().getProjectNameAndVersion() + " - Exception catched.", e);
                                            out.println("<br> error message : " + e.getMessage() + " " + e.toString() + "<br>");
                                        } finally {
                                            try {
                                                conn.close();
                                            } catch (Exception ex) {
                                                Logger.getLogger("IntegrationStatus.jsp").log(Level.FATAL, Infos.getInstance().getProjectNameAndVersion() + " - Exception catched on close.", ex);
                                            }
                                        }
            %>

            <br><% out.print(display_footer(DatePageStart));%>
            </body>
            </html>
