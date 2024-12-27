/**
 * Cerberus Copyright (C) 2013 - 2025 cerberustesting
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
package org.cerberus.core.service.notifications.email.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.cerberus.core.crud.entity.Application;
import org.cerberus.core.crud.service.IApplicationService;
import org.cerberus.core.crud.service.IParameterService;
import org.cerberus.core.database.DatabaseSpring;
import org.cerberus.core.service.notifications.email.IEmailBodyGeneration;
import org.cerberus.core.util.StringUtil;
import org.cerberus.core.version.Infos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author vertigo17
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
            buildContentTable += "<table>";
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
            if (!StringUtil.isEmptyOrNull(lastRevision)) { // If lasRevision not defined, we take everything.
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
                        if (!StringUtil.isEmptyOrNull(rsBC.getString("Link"))) {
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
                        if (StringUtil.isEmptyOrNull(contentBugURL)) {
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
                buildContentTable += "</tbody></table><br>";
                buildContentTemplate = buildContentTable;
            }catch (Exception e) {
                LOG.warn(Infos.getInstance().getProjectNameAndVersion() + " - Exception catched.", e);
            }
        } catch (Exception e) {
            LOG.warn(Infos.getInstance().getProjectNameAndVersion() + " - Exception catched.", e);
        }
        return buildContentTemplate;

    }

}
