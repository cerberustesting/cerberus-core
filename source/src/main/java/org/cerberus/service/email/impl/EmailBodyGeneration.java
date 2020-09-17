/**
 * Cerberus Copyright (C) 2013 - 2017 cerberustesting
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This file is part of Cerberus.
 *
 * Cerberus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Cerberus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Cerberus.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.cerberus.service.email.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.cerberus.crud.entity.Application;
import org.cerberus.crud.service.IApplicationService;
import org.cerberus.crud.service.IParameterService;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.service.email.IEmailBodyGeneration;
import org.cerberus.util.SqlUtil;
import org.cerberus.util.StringUtil;
import org.cerberus.version.Infos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 */
@Service
public class EmailBodyGeneration implements IEmailBodyGeneration {

    private static final Logger LOG = LogManager.getLogger(EmailBodyGeneration.class);
    
    @Autowired
    private IParameterService parameterService;
    @Autowired
    private IApplicationService applicationService;
    @Autowired
    private DatabaseSpring databaseSpring;

    @Override
    public String GenerateBuildContentTable(String system, String build, String revision, String lastBuild, String lastRevision) {

        String buildContentTemplate = "";
        String buildContentTable = "";

        try (Connection conn = databaseSpring.connect();
        		Statement stmtBuildContent = conn.createStatement();) {
            
            String bugURL = "";

            List<Application> appliList = applicationService.convert(applicationService.readBySystem(Arrays.asList(system)));

            buildContentTable = "Here are the last modifications since last change (" + lastBuild + "/" + lastRevision + ") :";
            buildContentTable = buildContentTable + "<table>";
            buildContentTable = buildContentTable + "<thead><tr style=\"background-color:#cad3f1; font-style:bold\"><td>"
                    + "Sprint/Rev</td><td>Application</td><td>Project</td><td>Bug</td><td>Ticket</td><td>People in Charge</td><td>Release Documentation</td></tr></thead><tbody>";

            StringBuilder contentSQLSB = new StringBuilder("SELECT b.`Build`, b.`Revision`, b.`Release` , b.`Link` , ")
                    .append(" b.`Application`, b.`ReleaseOwner`, b.`BugIDFixed`, b.`TicketIDFixed`, b.`subject`, b.`Project`")
                    .append(", u.Name, a.BugTrackerUrl ")
                    .append(" from buildrevisionparameters b ")
                    .append(" left outer join user u on u.Login=b.ReleaseOwner ")
                    .append(" left outer join application a on a.application=b.application ")
                    .append(" join buildrevisioninvariant bri on bri.versionname = b.revision and bri.`system` = '").append(system).append("'  and bri.`level` = 2 ")
                    .append(" where build = '").append(build).append("' and a.system = '").append(system).append("' ");
            if (!StringUtil.isNullOrEmpty(lastRevision)) { // If lasRevision not defined, we take everything.
                contentSQLSB.append(" and bri.seq > (select seq from buildrevisioninvariant where `system` = '").append(system).append("' and `level` = 2 and `versionname` = '").append(lastRevision).append("' )  ");
            }
            contentSQLSB.append(" and bri.seq <= (select seq from buildrevisioninvariant where `system` = '").append(system).append("' and `level` = 2 and `versionname` = '").append(revision).append("' )  ")
                    .append(" order by b.Build, bri.seq, b.Application, b.datecre,")
                    .append(" b.TicketIDFixed, b.BugIDFixed, b.`Release`").toString();

            String contentSQL = contentSQLSB.toString();

            LOG.debug(Infos.getInstance().getProjectNameAndVersion() + " - SQL : " + contentSQL);

            try(ResultSet rsBC = stmtBuildContent.executeQuery(contentSQL)){
            	if (rsBC.first()) {
                    String bckColor = "#f3f6fa";
                    int a = 1;
                    do {
                        a++;
                        int b;
                        b = a % 2;
                        if (b == 1) {
                            bckColor = "#e1e7f3";
                        } else {
                            bckColor = "White";
                        }

                        String contentBugURL = "";
                        String contentBuild = "";
                        String contentAppli = "";
                        String contentRev = "";
                        String subject = "";
                        String release = "";
                        String releaseOwner = "";
                        String BugIDFixed = " ";
                        String TicketIDFixed = " ";
                        String Project = " ";

                        if (rsBC.getString("a.BugTrackerUrl") != null) {
                            contentBugURL = rsBC.getString("a.BugTrackerUrl");
                        }
                        if (rsBC.getString("b.build") != null) {
                            contentBuild = rsBC.getString("b.build");
                        }
                        if (rsBC.getString("b.Application") != null) {
                            contentAppli = rsBC.getString("b.Application");
                        }
                        if (rsBC.getString("b.Revision") != null) {
                            contentRev = rsBC.getString("b.Revision");
                        }
                        if (rsBC.getString("subject") != null) {
                            subject = rsBC.getString("subject");
                        }
                        if (rsBC.getString("Release") != null) {
                            release = rsBC.getString("Release");
                        }
                        if (rsBC.getString("Name") != null) {
                            releaseOwner = rsBC.getString("Name");
                        } else {
                            releaseOwner = rsBC.getString("ReleaseOwner");
                        }
                        if (!StringUtil.isNullOrEmpty(rsBC.getString("Link"))) {
                            release = "<a target=\"_blank\" href=\"" + rsBC.getString("Link") + "\">" + release + "</a>";
                        }
                        if (rsBC.getString("BugIDFixed") != null) {
                            BugIDFixed = rsBC.getString("BugIDFixed");
                        }
                        if (rsBC.getString("TicketIDFixed") != null) {
                            TicketIDFixed = rsBC.getString("TicketIDFixed");
                        }
                        if (rsBC.getString("Project") != null) {
                            Project = rsBC.getString("Project");
                        }

                        buildContentTable = buildContentTable + "<tr style=\"background-color:" + bckColor + "; font-size:80%\">"
                                + "<td  rowspan=\"2\">" + contentBuild + "/" + contentRev + "</td>"
                                + "<td>" + contentAppli + "</td>"
                                + "<td>" + Project + "</td>";
                        if (StringUtil.isNullOrEmpty(contentBugURL)) {
                            buildContentTable = buildContentTable + "<td>" + BugIDFixed + "</td>";
                        } else {
                            buildContentTable = buildContentTable + "<td><a target=\"_blank\" href=\"" + contentBugURL.replace("%BUGID%", BugIDFixed) + "\">" + BugIDFixed + "</a></td>";
                        }
                        buildContentTable = buildContentTable + "<td>" + TicketIDFixed + "</td>"
                                + "<td>" + releaseOwner + "</td>"
                                + "<td>" + release + "</td>"
                                + "</tr>"
                                + "<tr style=\"background-color:" + bckColor + "; font-size:80%\">"
                                + "<td colspan=\"6\">" + subject + "</td>"
                                + "</tr>";

                    } while (rsBC.next());

                }
                buildContentTable = buildContentTable + "</tbody></table><br>";
                buildContentTemplate = buildContentTable;
            }catch (Exception e) {
                LOG.warn(Infos.getInstance().getProjectNameAndVersion() + " - Exception catched.", e);
            }
        } catch (Exception e) {
            LOG.warn(Infos.getInstance().getProjectNameAndVersion() + " - Exception catched.", e);
        }
        return buildContentTemplate;

    }

    @Override
    public String GenerateTestRecapTable(String system, String build, String revision, String country) {

        String TestRecapTable;

        try (Connection conn = databaseSpring.connect();
        		Statement stmtBuildContent = conn.createStatement();
                Statement stmtCountryList = conn.createStatement();) {

            List<Application> appliList = applicationService.convert(applicationService.readBySystem(Arrays.asList(system)));
            String inSQL = SqlUtil.getInSQLClause(appliList);

            String contentSQL = "SELECT i.gp1, count(*) nb_exe, OK.c nb_exe_OK, format(OK.c/count(*)*100,0)  per_OK"
                    + "     , DTC.c nb_dtc, DAPP.c nb_dapp"
                    + " FROM testcaseexecution t"
                    + " JOIN invariant i on i.value=t.Environment and i.idname='ENVIRONMENT'"
                    + " LEFT OUTER JOIN ( "
                    + " SELECT i.gp1 gp1, count(*) c"
                    + " FROM testcaseexecution t1 "
                    + " JOIN invariant i on i.value=t1.Environment and i.idname='ENVIRONMENT'"
                    + " WHERE t1.ControlStatus= 'OK' and t1.Build='" + build + "' and t1.Revision='" + revision + "'";
            if (country.equalsIgnoreCase("ALL") == false) {
                contentSQL = contentSQL + " and t1.country='" + country + "'";
            }
            contentSQL = contentSQL + " and Environment not in ('PROD','DEV') "
                    + " and (status='WORKING' or status is null) "
                    + " and application " + inSQL
                    + " GROUP BY gp1 "
                    + " order by gp1) as OK"
                    + " ON OK.gp1=i.gp1"
                    + " LEFT OUTER JOIN ( "
                    + " select toto.gp1 gp1, count(*) c from "
                    + " (SELECT i.gp1 gp1,t1.test, t1.testcase "
                    + " FROM testcaseexecution t1 "
                    + " JOIN invariant i on i.value=t1.Environment and i.idname='ENVIRONMENT'"
                    + " WHERE t1.ControlStatus in ('OK','KO') and t1.Build='" + build + "' and t1.Revision='" + revision + "'";
            if (country.equalsIgnoreCase("ALL") == false) {
                contentSQL = contentSQL + " and t1.country='" + country + "'";
            }
            contentSQL = contentSQL + " and Environment not in ('PROD','DEV') "
                    + " and (status='WORKING' or status is null) "
                    + " and application " + inSQL
                    + " GROUP BY gp1 , t1.test, t1.testcase"
                    + " order by gp1 , t1.test, t1.testcase ) AS toto"
                    + " group by gp1) as DTC"
                    + " ON DTC.gp1=i.gp1"
                    + " LEFT OUTER JOIN ( "
                    + " select toto.gp1 gp1, count(*) c from "
                    + " (SELECT i.gp1 gp1,t1.application "
                    + " FROM testcaseexecution t1 "
                    + " JOIN invariant i on i.value=t1.Environment and i.idname='ENVIRONMENT'"
                    + " WHERE t1.ControlStatus in ('OK','KO') and t1.Build='" + build + "' and t1.Revision='" + revision + "'";
            if (country.equalsIgnoreCase("ALL") == false) {
                contentSQL = contentSQL + " and t1.country='" + country + "'";
            }
            contentSQL = contentSQL + " and Environment not in ('PROD','DEV') "
                    + " and (status='WORKING' or status is null) "
                    + " and application " + inSQL
                    + " GROUP BY gp1 , t1.application"
                    + " order by gp1 , t1.application ) AS toto"
                    + " group by gp1) as DAPP"
                    + " ON DAPP.gp1=i.gp1"
                    + " where 1=1"
                    + " and application " + inSQL
                    + " and t.ControlStatus in ('OK','KO') and t.Build='" + build + "' and t.Revision='" + revision + "' ";
            if (country.equalsIgnoreCase("ALL") == false) {
                contentSQL = contentSQL + " and t.country='" + country + "'";
            }
            contentSQL = contentSQL + " and Environment not in ('PROD','DEV') "
                    + " and (status='WORKING' or status is null) "
                    + " group by i.gp1 order by i.sort;";

            LOG.debug(Infos.getInstance().getProjectNameAndVersion() + " - SQL : " + contentSQL);
            String CountryListSQL = "SELECT value from invariant where idname='COUNTRY';";
            try(ResultSet rsBC = stmtBuildContent.executeQuery(contentSQL);
            		ResultSet rsCountry = stmtCountryList.executeQuery(CountryListSQL);){
            	StringBuilder CountryList = new StringBuilder();
                while (rsCountry.next()) {
                    CountryList.append(rsCountry.getString("value"));
                    CountryList.append("&Country=");
                }

                if (rsBC.first()) {

                    if (country.equalsIgnoreCase("ALL")) {
                        TestRecapTable = "Here is the Test Execution Recap accross all countries for " + build + "/" + revision + " :";
                    } else {
                        TestRecapTable = "Here is the Test Execution Recap for your country for " + build + "/" + revision + " :";
                    }
                    TestRecapTable = TestRecapTable + "<table>";
                    TestRecapTable = TestRecapTable + "<tr style=\"background-color:#cad3f1; font-style:bold\">"
                            + "<td>Env</td><td>Nb Exe</td><td>% OK</td><td>Distinct TestCases</td><td>Distinct Applications</td></tr>";

                    String bckColor = "#f3f6fa";
                    int a = 1;
                    StringBuilder buf = new StringBuilder();
                    do {
                        a++;
                        int b;
                        b = a % 2;
                        if (b == 1) {
                            bckColor = "#e1e7f3";
                        } else {
                            bckColor = "White";
                        }

                        String contentEnv = "";
                        String contentNBExe = "";
                        String contentPerOK = "";
                        String contentNBDTC = "";
                        String contentNBDAPP = "";

                        if (rsBC.getString("gp1") != null) {
                            contentEnv = rsBC.getString("gp1");
                        }
                        if (rsBC.getString("nb_exe") != null) {
                            contentNBExe = rsBC.getString("nb_exe");
                        }
                        if (rsBC.getString("per_OK") != null) {
                            contentPerOK = rsBC.getString("per_OK");
                        }
                        if (rsBC.getString("nb_dtc") != null) {
                            contentNBDTC = rsBC.getString("nb_dtc");
                        }
                        if (rsBC.getString("nb_dapp") != null) {
                            contentNBDAPP = rsBC.getString("nb_dapp");
                        }

//                        TestRecapTable = TestRecapTable + "<tr style=\"background-color:" + bckColor + "; font-size:80%\"><td>"
                        buf.append("<tr style=\"background-color:").append(bckColor).append("; font-size:80%\"><td>")
                                .append(contentEnv).append("</td><td>")
                                .append(contentNBExe).append("</td><td>")
                                .append(contentPerOK).append("</td><td>")
                                .append(contentNBDTC).append("</td><td>")
                                .append(contentNBDAPP).append("</td></tr>");
                    } while (rsBC.next());

                    TestRecapTable += buf.toString() + "</table><br>";

                } else if (country.equalsIgnoreCase("ALL")) {
                    TestRecapTable = "Unfortunatly, no test have been executed for any country for " + build + "/" + revision + " :-(<br><br>";
                } else {
                    TestRecapTable = "Unfortunatly, no test have been executed for your country for " + build + "/" + revision + " :-(<br><br>";
                }
            }catch (Exception e) {
                LOG.warn(Infos.getInstance().getProjectNameAndVersion() + " - Exception catched.", e);
                TestRecapTable = e.getMessage();
            }
        } catch (Exception e) {
            LOG.warn(Infos.getInstance().getProjectNameAndVersion() + " - Exception catched.", e);
            TestRecapTable = e.getMessage();
        }

        return TestRecapTable;

    }

}
