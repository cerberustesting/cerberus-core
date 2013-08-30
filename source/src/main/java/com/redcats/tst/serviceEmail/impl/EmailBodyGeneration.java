/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.serviceEmail.impl;

import com.redcats.tst.service.IParameterService;
import com.redcats.tst.serviceEmail.IEmailBodyGeneration;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import version.Version;

/**
 *
 * @author bcivel
 */
@Service
public class EmailBodyGeneration implements IEmailBodyGeneration {
    
    @Autowired
    private IParameterService parameterService;

    @Override
    public String GenerateBuildContentTable(String build, String revision, String lastBuild, String lastRevision, Connection conn) {

        String buildContentTemplate = "";
        String buildContentTable = "";

        try {
            Statement stmtBuildContent = conn.createStatement();

            String bugURL = "";
            String ticketURL = "";
            /* Pick the datas from the database */
            bugURL = parameterService.findParameterByKey("sitdmoss_bugtracking_url").getValue();
            ticketURL = parameterService.findParameterByKey("sitdmoss_ticketservice_url").getValue();


            buildContentTable = "Here are the last modifications since last change (" + lastBuild + "/" + lastRevision + ") :";
            buildContentTable = buildContentTable + "<table>";
            buildContentTable = buildContentTable + "<tr style=\"background-color:#cad3f1; font-style:bold\"><td>"
                    + "Sprint/Rev</td><td>Application</td><td>Subject</td><td>Project</td><td>Bug</td><td>Ticket</td><td>People in Charge</td><td>Release Documentation</td></tr>";


            String contentSQL = "SELECT b.`Build`, b.`Revision`, b.`Release` , b.`Link` , "
                    + " b.`Application`, b.`ReleaseOwner`, b.`BugIDFixed`, b.`TicketIDFixed`, b.`subject`, b.`Project`"
                    + ", p.`VCCode`, u.Name "
                    + " from buildrevisionparameters b "
                    + " left outer join project p on p.idproject=b.project "
                    + " left outer join user u on u.Login=b.ReleaseOwner "
                    + " where build = '" + build + "'";
            if (lastBuild.equalsIgnoreCase(build)) {
                contentSQL += " and revision > '" + lastRevision + "'";
            }
            contentSQL += " and revision <= '" + revision + "'"
                    + " order by b.Build, b.Revision, b.Application, b.Project, b.TicketIDFixed, b.BugIDFixed, b.Release";

            ResultSet rsBC = stmtBuildContent.executeQuery(contentSQL);

            rsBC.first();
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


                String contentBuild = "";
                String contentAppli = "";
                String contentRev = "";
                String subject = "";
                String release = "";
                String releaseOwner = "";
                String BugIDFixed = " ";
                String TicketIDFixed = " ";
                String Project = " ";
                String ProjectVC = " ";


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
                if (rsBC.getString("Link") != null) {
                    release = "<a href=\"" + rsBC.getString("Link") + "\">" + release + "</a>";
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
                if (rsBC.getString("p.VCCode") != null) {
                    ProjectVC = Project + " (" + rsBC.getString("p.VCCode") + ")";
                }

                buildContentTable = buildContentTable + "<tr style=\"background-color:" + bckColor + "; font-size:80%\"><td>"
                        + contentBuild + "/" + contentRev + "</td><td>"
                        + contentAppli + "</td><td>"
                        + subject + "</td><td>"
                        + ProjectVC + "</td><td>"
                        + "<a href=\"" + bugURL.replace("%bugid%", BugIDFixed) + "\">" + BugIDFixed + "</a></td><td>"
                        + "<a href=\"" + ticketURL.replace("%ticketid%", TicketIDFixed) + "\">" + TicketIDFixed + "</a></td><td>"
                        + releaseOwner + "</td><td>"
                        + release + "</td></tr>";
            } while (rsBC.next());

            buildContentTable = buildContentTable + "</table><br>";

            rsBC.close();
            stmtBuildContent.close();
            buildContentTemplate = buildContentTable;

        } catch (Exception e) {
            Logger.getLogger(EmailBodyGeneration.class.getName()).log(Level.SEVERE, Version.PROJECT_NAME_VERSION + " - Exception catched.", e);
        }

        return buildContentTemplate;

    }

    @Override
    public String GenerateTestRecapTable(String build, String revision, String country, Connection conn) {

        String TestRecapTable ;

        try {
            Statement stmtBuildContent = conn.createStatement();
            Statement stmtCountryList = conn.createStatement();

            String contentSQL = "SELECT i.gp1, count(*) nb_exe, OK.c nb_exe_OK, format(OK.c/count(*)*100,0)  per_OK"
                    + "     , DTC.c nb_dtc, DAPP.c nb_dapp"
                    + " FROM testcaseexecution t"
                    + " JOIN invariant i on i.value=t.Environment and i.id=5"
                    + " LEFT OUTER JOIN ( "
                    + " SELECT i.gp1 gp1, count(*) c"
                    + " FROM testcaseexecution t1 "
                    + " JOIN invariant i on i.value=t1.Environment and i.id=5"
                    + " WHERE t1.ControlStatus= 'OK' and t1.Build='" + build + "' and t1.Revision='" + revision + "'";
            if (country.equalsIgnoreCase("ALL") == false) {
                contentSQL = contentSQL + " and t1.country='" + country + "'";
            }
            contentSQL = contentSQL + " and test not in ('Business Activity Monitor','Performance Monitor') and Environment not in ('PROD','DEV') "
                    + " and (status='WORKING' or status is null) "
                    + " GROUP BY gp1 "
                    + " order by gp1) as OK"
                    + " ON OK.gp1=i.gp1"
                    + " LEFT OUTER JOIN ( "
                    + " select toto.gp1 gp1, count(*) c from "
                    + " (SELECT i.gp1 gp1,t1.test, t1.testcase "
                    + " FROM testcaseexecution t1 "
                    + " JOIN invariant i on i.value=t1.Environment and i.id=5"
                    + " WHERE t1.ControlStatus in ('OK','KO') and t1.Build='" + build + "' and t1.Revision='" + revision + "'";
            if (country.equalsIgnoreCase("ALL") == false) {
                contentSQL = contentSQL + " and t1.country='" + country + "'";
            }
            contentSQL = contentSQL + " and test not in ('Business Activity Monitor','Performance Monitor') and Environment not in ('PROD','DEV') "
                    + " and (status='WORKING' or status is null) "
                    + " GROUP BY gp1 , t1.test, t1.testcase"
                    + " order by gp1 , t1.test, t1.testcase ) AS toto"
                    + " group by gp1) as DTC"
                    + " ON DTC.gp1=i.gp1"
                    + " LEFT OUTER JOIN ( "
                    + " select toto.gp1 gp1, count(*) c from "
                    + " (SELECT i.gp1 gp1,t1.application "
                    + " FROM testcaseexecution t1 "
                    + " JOIN invariant i on i.value=t1.Environment and i.id=5"
                    + " WHERE t1.ControlStatus in ('OK','KO') and t1.Build='" + build + "' and t1.Revision='" + revision + "'";
            if (country.equalsIgnoreCase("ALL") == false) {
                contentSQL = contentSQL + " and t1.country='" + country + "'";
            }
            contentSQL = contentSQL + " and test not in ('Business Activity Monitor','Performance Monitor') and Environment not in ('PROD','DEV') "
                    + " and (status='WORKING' or status is null) "
                    + " GROUP BY gp1 , t1.application"
                    + " order by gp1 , t1.application ) AS toto"
                    + " group by gp1) as DAPP"
                    + " ON DAPP.gp1=i.gp1"
                    + " where 1=1"
                    + " and t.ControlStatus in ('OK','KO') and t.Build='" + build + "' and t.Revision='" + revision + "' ";
            if (country.equalsIgnoreCase("ALL") == false) {
                contentSQL = contentSQL + " and t.country='" + country + "'";
            }
            contentSQL = contentSQL + " and t.test not in ('Business Activity Monitor','Performance Monitor')  and Environment not in ('PROD','DEV') "
                    + " and (status='WORKING' or status is null) "
                    + " group by i.gp1 order by i.sort;";

            ResultSet rsBC = stmtBuildContent.executeQuery(contentSQL);
            String Cerberus_URL = parameterService.findParameterByKey("cerberus_reporting_url").getValue();;
            Cerberus_URL = Cerberus_URL.replaceAll("%env%", "");
            Cerberus_URL = Cerberus_URL.replaceAll("%appli%", "");
            Cerberus_URL = Cerberus_URL.replaceAll("%build%", build);
            Cerberus_URL = Cerberus_URL.replaceAll("%rev%", revision);

            String CountryListSQL = "SELECT value from invariant where id=4;";
            ResultSet rsCountry = stmtCountryList.executeQuery(CountryListSQL);
            StringBuilder CountryList = new StringBuilder();
            while (rsCountry.next()) {
                CountryList.append(rsCountry.getString("value"));
                CountryList.append("&Country=");
            }


            String Cerberus_URL_ALL = Cerberus_URL.replaceAll("%country%", CountryList.toString());
            Cerberus_URL = Cerberus_URL.replaceAll("%country%", country);

            if (rsBC.first()) {

                if (country.equalsIgnoreCase("ALL")) {
                    TestRecapTable = "Here is the Test Execution Recap accross all countries for <a href=\"" + Cerberus_URL_ALL + "\">" + build + "/" + revision + "</a> :";
                } else {
                    TestRecapTable = "Here is the Test Execution Recap for your country for <a href=\"" + Cerberus_URL + "\">" + build + "/" + revision + "</a> :";
                }
                TestRecapTable = TestRecapTable + "<table>";
                TestRecapTable = TestRecapTable + "<tr style=\"background-color:#cad3f1; font-style:bold\">"
                        + "<td>Env</td><td>Nb Exe</td><td>% OK</td><td>Distinct TestCases</td><td>Distinct Applications</td></tr>";



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

                    TestRecapTable = TestRecapTable + "<tr style=\"background-color:" + bckColor + "; font-size:80%\"><td>"
                            + contentEnv + "</td><td>"
                            + contentNBExe + "</td><td>"
                            + contentPerOK + "</td><td>"
                            + contentNBDTC + "</td><td>"
                            + contentNBDAPP + "</td></tr>";
                } while (rsBC.next());

                TestRecapTable = TestRecapTable + "</table><br>";

            } else {
                if (country.equalsIgnoreCase("ALL")) {
                    TestRecapTable = "Unfortunatly, no test have been executed for any country for <a href=\"" + Cerberus_URL_ALL + "\">" + build + "/" + revision + "</a> :-(<br><br>";
                } else {
                    TestRecapTable = "Unfortunatly, no test have been executed for your country for <a href=\"" + Cerberus_URL + "\">" + build + "/" + revision + "</a> :-(<br><br>";
                }
            }

            rsBC.close();
            rsCountry.close();
            stmtBuildContent.close();
            stmtCountryList.close();

        } catch (Exception e) {
            Logger.getLogger(EmailBodyGeneration.class.getName()).log(Level.SEVERE, Version.PROJECT_NAME_VERSION + " - Exception catched.", e);
            TestRecapTable = e.getMessage();
        }

        return TestRecapTable;

    }
}
