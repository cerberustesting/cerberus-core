/* DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
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
package org.cerberus.database;

import org.cerberus.crud.entity.MyVersion;
import org.cerberus.crud.service.IMyVersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * @author vertigo
 */
@Service
public class DatabaseVersioningService implements IDatabaseVersioningService {

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(DatabaseVersioningService.class);

    @Autowired
    private IMyVersionService MyversionService;
    @Autowired
    private DatabaseSpring databaseSpring;

    @Override
    public String exeSQL(String SQLString) {
        LOG.info("Starting Execution of '" + SQLString + "'");
        Statement preStat;
        Connection connection = this.databaseSpring.connect();
        try {
            preStat = connection.createStatement();
            try {
                preStat.execute(SQLString);
                LOG.info("'" + SQLString + "' Executed successfully.");
            } catch (Exception exception1) {
                LOG.error(exception1.toString());
                return exception1.toString();
            } finally {
                preStat.close();
            }
        } catch (Exception exception1) {
            LOG.error(exception1.toString());
            return exception1.toString();
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn(e.toString());
            }
        }
        return "OK";
    }

    @Override
    public boolean isDatabaseUptodate() {
        // Getting the Full script to update the database.
        ArrayList<String> SQLList;
        SQLList = this.getSQLScript();
        // Get version from the database 
        MyVersion MVersion;
        MVersion = MyversionService.findMyVersionByKey("database");
        // compare both to see if version is uptodate.
        if (SQLList.size() == MVersion.getValue()) {
            return true;
        }
        LOG.info("Database needs an upgrade - Script : " + SQLList.size() + " Database : " + MVersion.getValue());
        return false;
    }

    @Override
    public ArrayList<String> getSQLScript() {
        // Temporary string that will store the SQL Command before putting in the array.
        StringBuilder SQLS;
        // Full script that create the cerberus database.
        ArrayList<String> SQLInstruction;

        // Start to build the SQL Script here.
        SQLInstruction = new ArrayList<String>();

        // ***********************************************
        // ***********************************************
        // SQL Script Instructions.
        // ***********************************************
        // ***********************************************
        // - Every Query must be independant.<ul>
        //    - Drop and Create index of the table / columns inside the same SQL
        //    - Drop and creation of Foreign Key inside the same SQL
        // - SQL must be fast (even on big tables)
        //    - 1 Index or Foreign Key at a time.
        //    - Beware of big tables that may result a timeout on the GUI side.
        // - Limit the number of SQL required in this class.
        //    - When inserting some data in table, group them inside the same SQL
        // - Never introduce an SQL between 2 SQL. 
        //    - it messup the seq of SQL to execute in all users that moved to 
        //      earlier version
        // - Only modify the SQL to fix an SQL issue but not to change a 
        //   structure or enrich some data on an existing SQL. You need to 
        //   create a new one to secure that it gets executed in all env.
        // ***********************************************
        // ***********************************************
        SQLS = new StringBuilder();
        SQLS.append("CREATE TABLE `myversion` (");
        SQLS.append(" `Key` varchar(45) NOT NULL DEFAULT '', `Value` int(11) DEFAULT NULL,");
        SQLS.append(" PRIMARY KEY (`Key`)");
        SQLS.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8");
        SQLInstruction.add(SQLS.toString());

        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `myversion` (`Key`, `Value`) VALUES ('database', 0);");
        SQLInstruction.add(SQLS.toString());

        SQLS = new StringBuilder();
        SQLS.append("CREATE TABLE `log` (");
        SQLS.append("  `id` int(11) NOT NULL AUTO_INCREMENT,");
        SQLS.append("  `datecre` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,");
        SQLS.append("  `desc` varchar(20) DEFAULT NULL,");
        SQLS.append("  `longdesc` varchar(400) DEFAULT NULL,");
        SQLS.append("  `remoteIP` varchar(20) DEFAULT NULL,");
        SQLS.append("  `localIP` varchar(20) DEFAULT NULL,");
        SQLS.append("  PRIMARY KEY (`id`),");
        SQLS.append("  KEY `datecre` (`datecre`)");
        SQLS.append(") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;");
        SQLInstruction.add(SQLS.toString());

        SQLS = new StringBuilder();
        SQLS.append("CREATE TABLE `user` (");
        SQLS.append("  `UserID` int(10) unsigned NOT NULL AUTO_INCREMENT,");
        SQLS.append("  `Login` varchar(10) NOT NULL,");
        SQLS.append("  `Password` char(40) NOT NULL,");
        SQLS.append("  `Name` varchar(25) NOT NULL,");
        SQLS.append("  `Request` varchar(5) DEFAULT NULL,");
        SQLS.append("  `ReportingFavorite` varchar(1000) DEFAULT NULL,");
        SQLS.append("  `DefaultIP` varchar(45) DEFAULT NULL,");
        SQLS.append("  PRIMARY KEY (`UserID`),");
        SQLS.append("  UNIQUE KEY `ID1` (`Login`)");
        SQLS.append(") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;");
        SQLInstruction.add(SQLS.toString());

        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `user` VALUES (1,'admin','d033e22ae348aeb5660fc2140aec35850c4da997','Admin User','false',NULL,NULL)");
        SQLS.append(",(2,'cerberus','b7e73576cd25a6756dfc25d9eb914ba235d4355d','Cerberus User','false',NULL,NULL);");
        SQLInstruction.add(SQLS.toString());

        SQLS = new StringBuilder();
        SQLS.append("CREATE TABLE `usergroup` (");
        SQLS.append("  `Login` varchar(10) NOT NULL,");
        SQLS.append("  `GroupName` varchar(10) NOT NULL,");
        SQLS.append("  PRIMARY KEY (`Login`,`GroupName`)");
        SQLS.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8;");
        SQLInstruction.add(SQLS.toString());

        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `usergroup` VALUES ('admin','Admin'),('admin','User'),('admin','Visitor'),('admin','Integrator'),('cerberus','User'),('cerberus','Visitor'),('cerberus','Integrator');");
        SQLInstruction.add(SQLS.toString());

        SQLS = new StringBuilder();
        SQLS.append("CREATE TABLE `documentation` (");
        SQLS.append("  `DocTable` varchar(50) NOT NULL,");
        SQLS.append("  `DocField` varchar(45) NOT NULL,");
        SQLS.append("  `DocValue` varchar(60) NOT NULL DEFAULT '',");
        SQLS.append("  `DocLabel` varchar(60) DEFAULT NULL,");
        SQLS.append("  `DocDesc` varchar(10000) DEFAULT NULL,");
        SQLS.append("  PRIMARY KEY (`DocTable`,`DocField`,`DocValue`) USING BTREE");
        SQLS.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8;");
        SQLInstruction.add(SQLS.toString());

        SQLS = new StringBuilder();
        SQLS.append("CREATE TABLE `parameter` (");
        SQLS.append("  `param` varchar(100) NOT NULL,");
        SQLS.append("  `value` varchar(10000) NOT NULL,");
        SQLS.append("  `description` varchar(5000) NOT NULL,");
        SQLS.append("  PRIMARY KEY (`param`)");
        SQLS.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8;");
        SQLInstruction.add(SQLS.toString());

        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `parameter` VALUES ('cerberus_homepage_nbbuildhistorydetail','5','Define the number of build/revision that are displayed in the homepage.')");
        SQLS.append(",('cerberus_picture_path','/opt/cerberus-screenshots/','Path to store the Cerberus Selenium Screenshot')");
        SQLS.append(",('cerberus_picture_url','http://localhost/CerberusPictures/','Link to the Cerberus Selenium Screenshot. The following variable can be used : %ID% and %SCREENSHOT%')");
        SQLS.append(",('cerberus_reporting_url','http://IP/Cerberus/ReportingExecution.jsp?Application=%appli%&TcActive=Y&Priority=All&Environment=%env%&Build=%build%&Revision=%rev%&Country=%country%&Status=WORKING&Apply=Apply','URL to Cerberus reporting screen. the following variables can be used : %country%, %env%,  %appli%, %build% and %rev%.')");
        SQLS.append(",('cerberus_selenium_plugins_path','/tmp/','Path to load firefox plugins (Firebug + netExport) to do network traffic')");
        SQLS.append(",('cerberus_support_email','<a href=\"mailto:support@domain.com?Subject=Cerberus%20Account\" style=\"color: yellow\">Support</a>','Contact Email in order to ask for new user in Cerberus tool.')");
        SQLS.append(",('cerberus_testexecutiondetailpage_nbmaxexe','100','Default maximum number of testcase execution displayed in testcase execution detail page.')");
        SQLS.append(",('cerberus_testexecutiondetailpage_nbmaxexe_max','5000','Maximum number of testcase execution displayed in testcase execution detail page.')");
        SQLS.append(",('CI_OK_prio1','1','Coef in order to calculate the OK/KO result for CI platform.')");
        SQLS.append(",('CI_OK_prio2','0.5','Coef in order to calculate the OK/KO result for CI platform.')");
        SQLS.append(",('CI_OK_prio3','0.2','Coef in order to calculate the OK/KO result for CI platform.')");
        SQLS.append(",('CI_OK_prio4','0.1','Coef in order to calculate the OK/KO result for CI platform.')");
        SQLS.append(",('CI_OK_prio5','0','Coef in order to calculate the OK/KO result for CI platform.')");
        SQLS.append(",('index_alert_body','','Body for alerts')");
        SQLS.append(",('index_alert_from','QUALITY Team <team@mail.com>','From team for alerts')");
        SQLS.append(",('index_alert_subject','[BAM] Alert detected for %COUNTRY%','Subject for alerts')");
        SQLS.append(",('index_alert_to','QUALITY Team <team@mail.com>','List of contact for alerts')");
        SQLS.append(",('index_notification_body_between','<br><br>','Text to display between the element of the mail')");
        SQLS.append(",('index_notification_body_end','Subscribe / unsubscribe and get more realtime graph <a href=\"http://IP/index/BusinessActivityMonitor.jsp\">here</a>. <font size=\"1\">(Not available on Internet)</font><br><br>If you have any question, please contact us at <a href=\"mailto:mail@mail.com\">mail@mail.com</a><br>Cumprimentos / Regards / Cordialement,<br>Test and Integration Team</body></html>','Test to display at the end')");
        SQLS.append(",('index_notification_body_top','<html><body>Hello<br><br>Following is the activity monitored for %COUNTRY%, on the %DATEDEB%.<br><br>','Text to display at the top of the mail')");
        SQLS.append(",('index_notification_subject','[BAM] Business Activity Monitor for %COUNTRY%','subject')");
        SQLS.append(",('index_smtp_from','Team <team@mail.com>','smtp from used for notification')");
        SQLS.append(",('index_smtp_host','smtp.mail.com','Smtp host used with notification')");
        SQLS.append(",('index_smtp_port','25','smtp port used for notification ')");
        SQLS.append(",('integration_notification_disableenvironment_body','Hello to all.<br><br>Use of environment %ENV% for country %COUNTRY% with Sprint %BUILD% (Revision %REVISION%) has been disabled, either to cancel the environment or to start deploying a new Sprint/revision.<br>Please don\\'t use the VC applications until you receive further notification.<br><br>If you have any question, please contact us at mail@mail.com<br><br>Cumprimentos / Regards / Cordialement,<br><br>Test and Integration Team','Default Mail Body on event disableenvironment.')");
        SQLS.append(",('integration_notification_disableenvironment_cc','Team <team@mail.com>','Default Mail cc on event disableenvironment.')");
        SQLS.append(",('integration_notification_disableenvironment_subject','[TIT] Env %ENV% for %COUNTRY% (with Sprint %BUILD% revision %REVISION%) has been disabled for Maintenance.','Default Mail Subject on event disableenvironment.')");
        SQLS.append(",('integration_notification_disableenvironment_to','Team <team@mail.com>','Default Mail to on event disableenvironment.')");
        SQLS.append(",('integration_notification_newbuildrevision_body','Hello to all.<br><br>Sprint %BUILD% with Revisions %REVISION% is now available in %ENV%.<br>To access the corresponding application use the link:<br><a href=\"http://IP/index/?active=Y&env=%ENV%&country=%COUNTRY%\">http://IP/index/?active=Y&env=%ENV%&country=%COUNTRY%</a><br><br>%BUILDCONTENT%<br>%TESTRECAP%<br>%TESTRECAPALL%<br>If you have any problem or question, please contact us at mail@mail.com<br><br>Cumprimentos / Regards / Cordialement,<br><br>Test and Integration Team','Default Mail Body on event newbuildrevision.')");
        SQLS.append(",('integration_notification_newbuildrevision_cc','Team <team@mail.com>','Default Mail cc on event newbuildrevision.')");
        SQLS.append(",('integration_notification_newbuildrevision_subject','[TIT] Sprint %BUILD% Revision %REVISION% is now ready to be used in %ENV% for %COUNTRY%.','Default Mail Subject on event newbuildrevision.')");
        SQLS.append(",('integration_notification_newbuildrevision_to','Team <team@mail.com>','Default Mail to on event newchain.')");
        SQLS.append(",('integration_notification_newchain_body','Hello to all.<br><br>A new Chain %CHAIN% has been executed in %ENV% for your country (%COUNTRY%).<br>Please perform your necessary test following that execution.<br><br>If you have any question, please contact us at mail@mail.com<br><br>Cumprimentos / Regards / Cordialement.','Default Mail Body on event newchain.')");
        SQLS.append(",('integration_notification_newchain_cc','Team <team@mail.com>','Default Mail cc on event newchain.')");
        SQLS.append(",('integration_notification_newchain_subject','[TIT] A New treatment %CHAIN% has been executed in %ENV% for %COUNTRY%.','Default Mail Subject on event newchain.')");
        SQLS.append(",('integration_notification_newchain_to','Team <team@mail.com>','Default Mail to on event newchain.')");
        SQLS.append(",('integration_smtp_from','Team <team@mail.com>','smtp from used for notification')");
        SQLS.append(",('integration_smtp_host','mail.com','Smtp host used with notification')");
        SQLS.append(",('integration_smtp_port','25','smtp port used for notification ')");
        SQLS.append(",('jenkins_admin_password','toto','Jenkins Admin Password')");
        SQLS.append(",('jenkins_admin_user','admin','Jenkins Admin Username')");
        SQLS.append(",('jenkins_application_pipeline_url','http://IP:8210/view/Deploy/','Jenkins Application Pipeline URL. %APPLI% can be used to replace Application name.')");
        SQLS.append(",('jenkins_deploy_pipeline_url','http://IP:8210/view/Deploy/','Jenkins Standard deploy Pipeline URL. ')");
        SQLS.append(",('jenkins_deploy_url','http://IP:8210/job/STD-DEPLOY/buildWithParameters?token=buildit&DEPLOY_JOBNAME=%APPLI%&DEPLOY_BUILD=%JENKINSBUILDID%&DEPLOY_TYPE=%DEPLOYTYPE%&DEPLOY_ENV=%JENKINSAGENT%&SVN_REVISION=%RELEASE%','Link to Jenkins in order to trigger a standard deploy. %APPLI% %JENKINSBUILDID% %DEPLOYTYPE% %JENKINSAGENT% and %RELEASE% can be used.')");
        SQLS.append(",('ticketing tool_bugtracking_url','http://IP/bugtracking/Lists/Bug%20Tracking/DispForm.aspx?ID=%bugid%&Source=http%3A%2F%2Fsitd_moss%2Fbugtracking%2FLists%2FBug%2520Tracking%2FAllOpenBugs.aspx','URL to SitdMoss Bug reporting screen. the following variable can be used : %bugid%.')");
        SQLS.append(",('ticketing tool_newbugtracking_url','http://IP/bugtracking/Lists/Bug%20Tracking/NewForm.aspx?RootFolder=%2Fbugtracking%2FLists%2FBug%20Tracking&Source=http%3A%2F%2Fsitd_moss%2Fbugtracking%2FLists%2FBug%2520Tracking%2FAllOpenBugs.aspx','URL to SitdMoss Bug creation page.')");
        SQLS.append(",('ticketing tool_ticketservice_url','http://IP/tickets/Lists/Tickets/DispForm.aspx?ID=%ticketid%','URL to SitdMoss Ticket Service page.')");
        SQLS.append(",('sonar_application_dashboard_url','http://IP:8211/sonar/project/index/com.appli:%APPLI%','Sonar Application Dashboard URL. %APPLI% and %MAVENGROUPID% can be used to replace Application name.')");
        SQLS.append(",('svn_application_url','http://IP/svn/SITD/%APPLI%','Link to SVN Repository. %APPLI% %TYPE% and %SYSTEM% can be used to replace Application name, type or system.');");
        SQLInstruction.add(SQLS.toString());

        SQLS = new StringBuilder();
        SQLS.append("CREATE TABLE `invariant` (");
        SQLS.append("  `idname` varchar(50) NOT NULL,");
        SQLS.append("  `value` varchar(50) NOT NULL,");
        SQLS.append("  `sort` int(10) unsigned NOT NULL,");
        SQLS.append("  `id` int(10) unsigned NOT NULL,");
        SQLS.append("  `description` varchar(100) NOT NULL,");
        SQLS.append("  `gp1` varchar(45) DEFAULT NULL,");
        SQLS.append("  `gp2` varchar(45) DEFAULT NULL,");
        SQLS.append("  `gp3` varchar(45) DEFAULT NULL,");
        SQLS.append("  PRIMARY KEY (`id`,`sort`) USING BTREE");
        SQLS.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8;");
        SQLInstruction.add(SQLS.toString());

        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` VALUES ('STATUS','STANDBY',1,1,'Not implemented yet',NULL,NULL,NULL)");
        SQLS.append(",('STATUS','IN PROGRESS',2,1,'Being implemented',NULL,NULL,NULL)");
        SQLS.append(",('STATUS','TO BE IMPLEMENTED',3,1,'To be implemented',NULL,NULL,NULL)");
        SQLS.append(",('STATUS','TO BE VALIDATED',4,1,'To be validated',NULL,NULL,NULL)");
        SQLS.append(",('STATUS','WORKING',5,1,'Validated and Working',NULL,NULL,NULL)");
        SQLS.append(",('STATUS','TO BE DELETED',6,1,'Should be deleted',NULL,NULL,NULL)");
        SQLS.append(",('GROUP','COMPARATIVE',1,2,'Group of comparison tests',NULL,NULL,NULL)");
        SQLS.append(",('GROUP','INTERACTIVE',2,2,'Group of interactive tests',NULL,NULL,NULL)");
        SQLS.append(",('GROUP','PRIVATE',3,2,'Group of tests which not appear in Cerberus',NULL,NULL,NULL)");
        SQLS.append(",('GROUP','PROCESS',4,2,'Group of tests which need a batch',NULL,NULL,NULL)");
        SQLS.append(",('GROUP','MANUAL',5,2,'Group of test which cannot be automatized',NULL,NULL,NULL)");
        SQLS.append(",('GROUP','',6,2,'Group of tests which are not already defined',NULL,NULL,NULL)");
        SQLS.append(",('COUNTRY','BE',10,4,'Belgium','800',NULL,NULL)");
        SQLS.append(",('COUNTRY','CH',11,4,'Switzerland','500',NULL,NULL)");
        SQLS.append(",('COUNTRY','ES',13,4,'Spain','900',NULL,NULL)");
        SQLS.append(",('COUNTRY','IT',14,4,'Italy','205',NULL,NULL)");
        SQLS.append(",('COUNTRY','PT',15,4,'Portugal','200',NULL,NULL)");
        SQLS.append(",('COUNTRY','RU',16,4,'Russia','240',NULL,NULL)");
        SQLS.append(",('COUNTRY','UK',17,4,'Great Britan','300',NULL,NULL)");
        SQLS.append(",('COUNTRY','UA',25,4,'Ukrainia','290',NULL,NULL)");
        SQLS.append(",('COUNTRY','DE',40,4,'Germany','600',NULL,NULL)");
        SQLS.append(",('COUNTRY','AT',41,4,'Austria','600',NULL,NULL)");
        SQLS.append(",('COUNTRY','GR',42,4,'Greece','220',NULL,NULL)");
        SQLS.append(",('COUNTRY','RX',50,4,'Transversal Country used for Transversal Applications.','RBX',NULL,NULL)");
        SQLS.append(",('COUNTRY','FR',60,4,'France',NULL,NULL,NULL)");
        SQLS.append(",('ENVIRONMENT','DEV',0,5,'Developpement','DEV',NULL,NULL)");
        SQLS.append(",('ENVIRONMENT','QA',5,5,'Quality Assurance','QA',NULL,NULL)");
        SQLS.append(",('ENVIRONMENT','UAT',30,5,'User Acceptance Test','UAT',NULL,NULL)");
        SQLS.append(",('ENVIRONMENT','PROD',50,5,'Production','PROD',NULL,NULL)");
        SQLS.append(",('ENVIRONMENT','PREPROD',60,5,'PreProduction','PROD',NULL,NULL)");
        SQLS.append(",('SERVER','PRIMARY',1,6,'Primary Server',NULL,NULL,NULL)");
        SQLS.append(",('SERVER','BACKUP1',2,6,'Backup 1',NULL,NULL,NULL)");
        SQLS.append(",('SERVER','BACKUP2',3,6,'Backup 2',NULL,NULL,NULL)");
        SQLS.append(",('SESSION','1',1,7,'Session 1',NULL,NULL,NULL)");
        SQLS.append(",('SESSION','2',2,7,'Session 2',NULL,NULL,NULL)");
        SQLS.append(",('SESSION','3',3,7,'Session 3',NULL,NULL,NULL)");
        SQLS.append(",('SESSION','4',4,7,'Session 4',NULL,NULL,NULL)");
        SQLS.append(",('SESSION','5',5,7,'Session 5',NULL,NULL,NULL)");
        SQLS.append(",('SESSION','6',6,7,'Session 6',NULL,NULL,NULL)");
        SQLS.append(",('SESSION','7',7,7,'Session 7',NULL,NULL,NULL)");
        SQLS.append(",('SESSION','8',8,7,'Session 8',NULL,NULL,NULL)");
        SQLS.append(",('SESSION','9',9,7,'Session 9',NULL,NULL,NULL)");
        SQLS.append(",('SESSION','10',10,7,'Session 10',NULL,NULL,NULL)");
        SQLS.append(",('BUILD','2012S2',13,8,'2012 Sprint 02',NULL,NULL,NULL)");
        SQLS.append(",('BUILD','2013S1',14,8,'2013 Sprint 01',NULL,NULL,NULL)");
        SQLS.append(",('REVISION','R00',1,9,'R00',NULL,NULL,NULL)");
        SQLS.append(",('REVISION','R01',10,9,'R01',NULL,NULL,NULL)");
        SQLS.append(",('REVISION','R02',20,9,'R02',NULL,NULL,NULL)");
        SQLS.append(",('REVISION','R03',30,9,'R03',NULL,NULL,NULL)");
        SQLS.append(",('REVISION','R04',40,9,'R04',NULL,NULL,NULL)");
        SQLS.append(",('REVISION','R05',50,9,'R05',NULL,NULL,NULL)");
        SQLS.append(",('REVISION','R06',60,9,'R06',NULL,NULL,NULL)");
        SQLS.append(",('REVISION','R07',70,9,'R07',NULL,NULL,NULL)");
        SQLS.append(",('REVISION','R08',80,9,'R08',NULL,NULL,NULL)");
        SQLS.append(",('REVISION','R09',90,9,'R09',NULL,NULL,NULL)");
        SQLS.append(",('REVISION','R10',100,9,'R10',NULL,NULL,NULL)");
        SQLS.append(",('REVISION','R11',110,9,'R11',NULL,NULL,NULL)");
        SQLS.append(",('REVISION','R12',120,9,'R12',NULL,NULL,NULL)");
        SQLS.append(",('REVISION','R13',130,9,'R13',NULL,NULL,NULL)");
        SQLS.append(",('REVISION','R14',140,9,'R14',NULL,NULL,NULL)");
        SQLS.append(",('REVISION','R15',150,9,'R15',NULL,NULL,NULL)");
        SQLS.append(",('REVISION','R16',160,9,'R16',NULL,NULL,NULL)");
        SQLS.append(",('REVISION','R17',170,9,'R17',NULL,NULL,NULL)");
        SQLS.append(",('REVISION','R18',180,9,'R18',NULL,NULL,NULL)");
        SQLS.append(",('REVISION','R19',190,9,'R19',NULL,NULL,NULL)");
        SQLS.append(",('REVISION','R20',200,9,'R20',NULL,NULL,NULL)");
        SQLS.append(",('REVISION','R21',210,9,'R21',NULL,NULL,NULL)");
        SQLS.append(",('REVISION','R22',220,9,'R22',NULL,NULL,NULL)");
        SQLS.append(",('REVISION','R23',230,9,'R23',NULL,NULL,NULL)");
        SQLS.append(",('REVISION','R24',240,9,'R24',NULL,NULL,NULL)");
        SQLS.append(",('REVISION','R25',250,9,'R25',NULL,NULL,NULL)");
        SQLS.append(",('REVISION','R26',260,9,'R26',NULL,NULL,NULL)");
        SQLS.append(",('REVISION','R27',270,9,'R27',NULL,NULL,NULL)");
        SQLS.append(",('REVISION','R28',280,9,'R28',NULL,NULL,NULL)");
        SQLS.append(",('REVISION','R29',290,9,'R29',NULL,NULL,NULL)");
        SQLS.append(",('REVISION','R30',300,9,'R30',NULL,NULL,NULL)");
        SQLS.append(",('REVISION','R31',310,9,'R31',NULL,NULL,NULL)");
        SQLS.append(",('REVISION','R32',320,9,'R32',NULL,NULL,NULL)");
        SQLS.append(",('REVISION','R33',330,9,'R33',NULL,NULL,NULL)");
        SQLS.append(",('REVISION','R34',340,9,'R34',NULL,NULL,NULL)");
        SQLS.append(",('REVISION','R35',350,9,'R35',NULL,NULL,NULL)");
        SQLS.append(",('REVISION','R36',360,9,'R36',NULL,NULL,NULL)");
        SQLS.append(",('REVISION','R37',370,9,'R37',NULL,NULL,NULL)");
        SQLS.append(",('REVISION','R38',380,9,'R38',NULL,NULL,NULL)");
        SQLS.append(",('REVISION','R39',390,9,'R39',NULL,NULL,NULL)");
        SQLS.append(",('REVISION','R40',400,9,'R40',NULL,NULL,NULL)");
        SQLS.append(",('REVISION','R41',410,9,'R41',NULL,NULL,NULL)");
        SQLS.append(",('REVISION','R42',420,9,'R42',NULL,NULL,NULL)");
        SQLS.append(",('REVISION','R43',430,9,'R43',NULL,NULL,NULL)");
        SQLS.append(",('REVISION','R44',440,9,'R44',NULL,NULL,NULL)");
        SQLS.append(",('REVISION','R45',450,9,'R45',NULL,NULL,NULL)");
        SQLS.append(",('REVISION','R46',460,9,'R46',NULL,NULL,NULL)");
        SQLS.append(",('REVISION','R47',470,9,'R47',NULL,NULL,NULL)");
        SQLS.append(",('REVISION','R48',480,9,'R48',NULL,NULL,NULL)");
        SQLS.append(",('REVISION','R49',490,9,'R49',NULL,NULL,NULL)");
        SQLS.append(",('REVISION','R50',500,9,'R50',NULL,NULL,NULL)");
        SQLS.append(",('REVISION','R51',510,9,'R51',NULL,NULL,NULL)");
        SQLS.append(",('REVISION','R52',520,9,'R52',NULL,NULL,NULL)");
        SQLS.append(",('REVISION','R53',530,9,'R53',NULL,NULL,NULL)");
        SQLS.append(",('REVISION','R54',540,9,'R54',NULL,NULL,NULL)");
        SQLS.append(",('REVISION','R55',550,9,'R55',NULL,NULL,NULL)");
        SQLS.append(",('REVISION','R56',560,9,'R56',NULL,NULL,NULL)");
        SQLS.append(",('REVISION','R57',570,9,'R57',NULL,NULL,NULL)");
        SQLS.append(",('REVISION','R58',580,9,'R58',NULL,NULL,NULL)");
        SQLS.append(",('REVISION','R59',590,9,'R59',NULL,NULL,NULL)");
        SQLS.append(",('REVISION','R60',600,9,'R60',NULL,NULL,NULL)");
        SQLS.append(",('REVISION','R61',610,9,'R61',NULL,NULL,NULL)");
        SQLS.append(",('REVISION','R62',620,9,'R62',NULL,NULL,NULL)");
        SQLS.append(",('ENVTYPE','STD',1,10,'Regression and evolution Standard Testing.',NULL,NULL,NULL)");
        SQLS.append(",('ENVTYPE','COMPARISON',2,10,'Comparison Testing. No GUI Tests are allowed.',NULL,NULL,NULL)");
        SQLS.append(",('ENVACTIVE','Y',1,11,'Active',NULL,NULL,NULL)");
        SQLS.append(",('ENVACTIVE','N',2,11,'Disable',NULL,NULL,NULL)");
        SQLS.append(",('ACTION','addSelection',10,12,'addSelection',NULL,NULL,NULL)");
        SQLS.append(",('ACTION','calculateProperty',20,12,'calculateProperty',NULL,NULL,NULL)");
        SQLS.append(",('ACTION','click',30,12,'click',NULL,NULL,NULL)");
        SQLS.append(",('ACTION','clickAndWait',40,12,'clickAndWait',NULL,NULL,NULL)");
        SQLS.append(",('ACTION','doubleClick',45,12,'doubleClick',NULL,NULL,NULL)");
        SQLS.append(",('ACTION','enter',50,12,'enter',NULL,NULL,NULL)");
        SQLS.append(",('ACTION','keypress',55,12,'keypress',NULL,NULL,NULL)");
        SQLS.append(",('ACTION','openUrlWithBase',60,12,'openUrlWithBase',NULL,NULL,NULL)");
        SQLS.append(",('ACTION','removeSelection',70,12,'removeSelection',NULL,NULL,NULL)");
        SQLS.append(",('ACTION','select',80,12,'select',NULL,NULL,NULL)");
        SQLS.append(",('ACTION','selectAndWait',90,12,'selectAndWait',NULL,NULL,NULL)");
        SQLS.append(",('ACTION','store',100,12,'store',NULL,NULL,NULL)");
        SQLS.append(",('ACTION','type',110,12,'type',NULL,NULL,NULL)");
        SQLS.append(",('ACTION','URLLOGIN',120,12,'URLLOGIN',NULL,NULL,NULL)");
        SQLS.append(",('ACTION','verifyTextPresent',130,12,'verifyTextPresent',NULL,NULL,NULL)");
        SQLS.append(",('ACTION','verifyTitle',140,12,'verifyTitle',NULL,NULL,NULL)");
        SQLS.append(",('ACTION','verifyValue',150,12,'verifyValue',NULL,NULL,NULL)");
        SQLS.append(",('ACTION','wait',160,12,'wait',NULL,NULL,NULL)");
        SQLS.append(",('ACTION','waitForPage',170,12,'waitForPage',NULL,NULL,NULL)");
        SQLS.append(",('CONTROL','PropertyIsEqualTo',10,13,'PropertyIsEqualTo',NULL,NULL,NULL)");
        SQLS.append(",('CONTROL','PropertyIsGreaterThan',12,13,'PropertyIsGreaterThan',NULL,NULL,NULL)");
        SQLS.append(",('CONTROL','PropertyIsMinorThan',14,13,'PropertyIsMinorThan',NULL,NULL,NULL)");
        SQLS.append(",('CONTROL','verifyElementPresent',20,13,'verifyElementPresent',NULL,NULL,NULL)");
        SQLS.append(",('CONTROL','verifyElementVisible',30,13,'verifyElementVisible',NULL,NULL,NULL)");
        SQLS.append(",('CONTROL','verifyText',40,13,'verifyText',NULL,NULL,NULL)");
        SQLS.append(",('CONTROL','verifyTextPresent',50,13,'verifyTextPresent',NULL,NULL,NULL)");
        SQLS.append(",('CONTROL','verifytitle',60,13,'verifytitle',NULL,NULL,NULL)");
        SQLS.append(",('CONTROL','verifyurl',70,13,'verifyurl',NULL,NULL,NULL)");
        SQLS.append(",('CONTROL','verifyContainText',80,13,'Verify Contain Text',NULL,NULL,NULL)");
        SQLS.append(",('CHAIN','0',1,14,'0',NULL,NULL,NULL)");
        SQLS.append(",('CHAIN','1',2,14,'1',NULL,NULL,NULL)");
        SQLS.append(",('PRIORITY','0',1,15,'No Priority defined',NULL,NULL,NULL)");
        SQLS.append(",('PRIORITY','1',2,15,'Critical Priority',NULL,NULL,NULL)");
        SQLS.append(",('PRIORITY','2',3,15,'High Priority',NULL,NULL,NULL)");
        SQLS.append(",('PRIORITY','3',4,15,'Mid Priority',NULL,NULL,NULL)");
        SQLS.append(",('PRIORITY','4',5,15,'Low Priority',NULL,NULL,NULL)");
        SQLS.append(",('PRIORITY','5',6,15,'Lower Priority or cosmetic',NULL,NULL,NULL)");
        SQLS.append(",('PRIORITY','99',7,15,'99',NULL,NULL,NULL)");
        SQLS.append(",('TCACTIVE','Y',1,16,'Yes',NULL,NULL,NULL)");
        SQLS.append(",('TCACTIVE','N',2,16,'No',NULL,NULL,NULL)");
        SQLS.append(",('TCREADONLY','N',1,17,'No',NULL,NULL,NULL)");
        SQLS.append(",('TCREADONLY','Y',2,17,'Yes',NULL,NULL,NULL)");
        SQLS.append(",('CTRLFATAL','Y',1,18,'Yes',NULL,NULL,NULL)");
        SQLS.append(",('CTRLFATAL','N',2,18,'No',NULL,NULL,NULL)");
        SQLS.append(",('PROPERTYTYPE','SQL',1,19,'SQL Query',NULL,NULL,NULL)");
        SQLS.append(",('PROPERTYTYPE','HTML',2,19,'HTML ID Field',NULL,NULL,NULL)");
        SQLS.append(",('PROPERTYTYPE','TEXT',3,19,'Fix Text value',NULL,NULL,NULL)");
        SQLS.append(",('PROPERTYTYPE','LIB_SQL',4,19,'Using an SQL from the library',NULL,NULL,NULL)");
        SQLS.append(",('PROPERTYNATURE','STATIC',1,20,'Static',NULL,NULL,NULL)");
        SQLS.append(",('PROPERTYNATURE','RANDOM',2,20,'Random',NULL,NULL,NULL)");
        SQLS.append(",('PROPERTYNATURE','RANDOMNEW',3,20,'Random New',NULL,NULL,NULL)");
        SQLS.append(",('ORIGIN','AT',1,21,'Austria',NULL,NULL,NULL)");
        SQLS.append(",('ORIGIN','BE',2,21,'Belgium',NULL,NULL,NULL)");
        SQLS.append(",('ORIGIN','CH',3,21,'Switzerland',NULL,NULL,NULL)");
        SQLS.append(",('ORIGIN','ES',4,21,'Spain',NULL,NULL,NULL)");
        SQLS.append(",('ORIGIN','GR',5,21,'Greece',NULL,NULL,NULL)");
        SQLS.append(",('ORIGIN','IT',6,21,'Italy',NULL,NULL,NULL)");
        SQLS.append(",('ORIGIN','PT',7,21,'Portugal',NULL,NULL,NULL)");
        SQLS.append(",('ORIGIN','RU',8,21,'Russia',NULL,NULL,NULL)");
        SQLS.append(",('ORIGIN','UA',9,21,'Ukrainia',NULL,NULL,NULL)");
        SQLS.append(",('ORIGIN','UK',10,21,'Great Britain',NULL,NULL,NULL)");
        SQLS.append(",('ORIGIN','DE',15,21,'Germany',NULL,NULL,NULL)");
        SQLS.append(",('ORIGIN','FR',16,21,'France',NULL,NULL,NULL)");
        SQLS.append(",('PROPERTYDATABASE','VC',1,22,'VC Database',NULL,NULL,NULL)");
        SQLS.append(",('PROPERTYDATABASE','ICS',2,22,'ICSDatabase',NULL,NULL,NULL)");
        SQLS.append(",('PROPERTYDATABASE','IDW',3,22,'IDW Database',NULL,NULL,NULL)");
        SQLS.append(",('PROPERTYDATABASE','CRB',4,22,'CERBERUS Database',NULL,NULL,NULL)");
        SQLS.append(",('PROPERTYDATABASE','IRT',5,22,'IRT Database',NULL,NULL,NULL)");
        SQLS.append(",('PROPERTYBAM','NBC',2,23,'Number of Orders','line','sum',NULL)");
        SQLS.append(",('PROPERTYBAM','AOL',4,23,'Number of Orders in the last 10 minutes','table','sum',NULL)");
        SQLS.append(",('PROPERTYBAM','API',5,23,'Number of API call in the last 10 minutes','line','sum',NULL)");
        SQLS.append(",('PROPERTYBAM','APT',6,23,'Average of Duration of API call in the last 10 minutes','line','avg','1.6')");
        SQLS.append(",('PROPERTYBAM','NBA',7,23,'Number of API longer than 1 second in the last 10 minutes','','sum',NULL)");
        SQLS.append(",('PROPERTYBAM','APE',8,23,'Number of API Errors in the last 10 minutes','line','sum','20')");
        SQLS.append(",('PROPERTYBAM','AVT',9,23,'Average of duration of a simple VCCRM scenario','line','avg',NULL)");
        SQLS.append(",('PROPERTYBAM','APT',10,23,'Average of Duration of API call in the last 10 minutes','table','avg','1.6')");
        SQLS.append(",('PROPERTYBAM','BAT',11,23,'Batch','gantt',NULL,NULL)");
        SQLS.append(",('PROPERTYBAM','BKP',12,23,'Backup','gantt',NULL,NULL)");
        SQLS.append(",('PROPERTYBAM','DTW',13,23,'Dataware','gantt',NULL,NULL)");
        SQLS.append(",('PROPERTYBAM','FST',14,23,'Fast Chain','gantt',NULL,NULL)");
        SQLS.append(",('PROPERTYBAM','IMG',15,23,'Selling Data File','gantt',NULL,NULL)");
        SQLS.append(",('PROPERTYBAM','MOR',16,23,'Morning Chain','gantt',NULL,NULL)");
        SQLS.append(",('PROPERTYBAM','WEB',17,23,'Product Data File','gantt',NULL,NULL)");
        SQLS.append(",('PROPERTYBAM','SIZ',18,23,'Size of the homepage','line','sum',NULL)");
        SQLS.append(",('PROPERTYBAM','LOG',19,23,'Web : Login Duration','line','avg','150')");
        SQLS.append(",('PROPERTYBAM','SIS',20,23,'Web : Search : Total size of pages','line','avg',NULL)");
        SQLS.append(",('PROPERTYBAM','NAV',21,23,'Web : Search : Duration','line','avg',NULL)");
        SQLS.append(",('PROPERTYBAM','PLP',22,23,'Web : PLP Duration','line','avg','100')");
        SQLS.append(",('PROPERTYBAM','PDP',23,23,'Web : PDP Duration','line','avg','150')");
        SQLS.append(",('PROPERTYBAM','CHE',24,23,'Web : Checkout Duration','line','avg',NULL)");
        SQLS.append(",('PROPERTYBAM','APC',25,23,'APC : API Error code 12 & 17','line','sum','50')");
        SQLS.append(",('PROPERTYBAM','MTE',26,23,'Web : Megatab ELLOS Duration','line','avg',NULL)");
        SQLS.append(",('PROPERTYBAM','PLD',27,23,'Web : PLP DRESSES Duration','line','avg',NULL)");
        SQLS.append(",('PROPERTYBAM','OMP',28,23,'Web : Outlet-MiniPDP Duration','line','avg',NULL)");
        SQLS.append(",('PROPERTYBAM','DBC',29,23,'Demand ','line','sum',NULL)");
        SQLS.append(",('PROPERTYBAM','MAR',30,23,'Margin in the last 10 minutes','line','sum',NULL)");
        SQLS.append(",('PROPERTYBAM','APD',31,23,'APD : API Error code 20','line','sum',NULL)");
        SQLS.append(",('PROPERTYBAM','DOR',32,23,'Performance : Direct Order','line','avg',NULL)");
        SQLS.append(",('PROPERTYBAM','EBP',33,23,'Performance : EBoutique Pull','line','avg',NULL)");
        SQLS.append(",('PROPERTYBAM','LOH',34,23,'Web : Login Duration','LINE','AVG',NULL)");
        SQLS.append(",('OUTPUTFORMAT','gui',1,24,'GUI HTLM output','','',NULL)");
        SQLS.append(",('OUTPUTFORMAT','compact',2,24,'Compact single line output.',NULL,NULL,NULL)");
        SQLS.append(",('OUTPUTFORMAT','verbose-txt',3,24,'Verbose key=value format.',NULL,NULL,NULL)");
        SQLS.append(",('VERBOSE','0',1,25,'Minimum log','','',NULL)");
        SQLS.append(",('VERBOSE','1',2,25,'Standard log','','',NULL)");
        SQLS.append(",('VERBOSE','2',3,25,'Maximum log',NULL,NULL,NULL)");
        SQLS.append(",('RUNQA','Y',1,26,'Test can run in QA enviroment',NULL,NULL,NULL)");
        SQLS.append(",('RUNQA','N',2,26,'Test cannot run in QA enviroment',NULL,NULL,NULL)");
        SQLS.append(",('RUNUAT','Y',1,27,'Test can run in UAT environment',NULL,NULL,NULL)");
        SQLS.append(",('RUNUAT','N',2,27,'Test cannot run in UAT environment',NULL,NULL,NULL)");
        SQLS.append(",('RUNPROD','N',1,28,'Test cannot run in PROD environment',NULL,NULL,NULL)");
        SQLS.append(",('RUNPROD','Y',2,28,'Test can run in PROD environment',NULL,NULL,NULL)");
        SQLS.append(",('FILTERNBDAYS','14',1,29,'14 Days (2 weeks)',NULL,NULL,NULL)");
        SQLS.append(",('FILTERNBDAYS','30',2,29,'30 Days (1 month)',NULL,NULL,NULL)");
        SQLS.append(",('FILTERNBDAYS','182',3,29,'182 Days (6 months)',NULL,NULL,NULL)");
        SQLS.append(",('FILTERNBDAYS','365',4,29,'365 Days (1 year)',NULL,NULL,NULL)");
        SQLS.append(",('PROBLEMCATEGORY','ERROR PAGES',10,30,'High amount of error pages','QUALITY',NULL,NULL)");
        SQLS.append(",('PROBLEMCATEGORY','PERFORMANCE',15,30,'Performance issue','QUALITY',NULL,NULL)");
        SQLS.append(",('PROBLEMCATEGORY','UNAVAILABILITY',20,30,'System Unavailable','QUALITY',NULL,NULL)");
        SQLS.append(",('PROBLEMCATEGORY','CONTENT ERROR',25,30,'Content Error','QUALITY',NULL,NULL)");
        SQLS.append(",('PROBLEMCATEGORY','API ERRORS',30,30,'API ERRORS',NULL,NULL,NULL)");
        SQLS.append(",('ROOTCAUSECATEGORY','HUMAN ERROR',1,31,'Problem due to wrong manipulation','PROCESS',NULL,NULL)");
        SQLS.append(",('ROOTCAUSECATEGORY','DEVELLOPMENT ERROR',2,31,'Problem with the code',NULL,NULL,NULL)");
        SQLS.append(",('ROOTCAUSECATEGORY','SERVER ERROR',3,31,'Technical issue',NULL,NULL,NULL)");
        SQLS.append(",('ROOTCAUSECATEGORY','COMMUNICATION ISSUE',4,31,'Communication',NULL,NULL,NULL)");
        SQLS.append(",('ROOTCAUSECATEGORY','PROCESS ERROR',5,31,'Problem with the process implemented','QUALITY',NULL,NULL)");
        SQLS.append(",('ROOTCAUSECATEGORY','MAINTENANCE',6,31,'Application Maintenance','QUALITY',NULL,NULL)");
        SQLS.append(",('RESPONSABILITY','[VC] TIT',1,32,'Tit Team','VC',NULL,NULL)");
        SQLS.append(",('RESPONSABILITY','[VC] CDI',20,32,'CDITeam','VC',NULL,NULL)");
        SQLS.append(",('RESPONSABILITY','[VC] QUALITY TEAM',25,32,'Quality Team','VC',NULL,NULL)");
        SQLS.append(",('RESPONSABILITY','[VC] UK TEAM',26,32,'UK TEAM','VC',NULL,NULL)");
        SQLS.append(",('RESPONSABILITY','[EXT] ESB',30,32,'ESB Team','EXT',NULL,NULL)");
        SQLS.append(",('RESPONSABILITY','[EXT] IT FRANCE',35,32,'IT France','EXT',NULL,NULL)");
        SQLS.append(",('RESPONSABILITY','[WEB] MILLENA',40,32,'Millena','WEB',NULL,NULL)");
        SQLS.append(",('RESPONSABILITY','[WEB] MEMO',50,32,'Memo','WEB',NULL,NULL)");
        SQLS.append(",('RESPONSABILITY','[WEB] THESEUS',60,32,'Theseus','WEB',NULL,NULL)");
        SQLS.append(",('RESPONSABILITY','[WEB] STUDIO',65,32,'Studio','WEB',NULL,NULL)");
        SQLS.append(",('RESPONSABILITY','[USERS] BE',70,32,'Belgium','USERS',NULL,NULL)");
        SQLS.append(",('RESPONSABILITY','[USERS] CH',71,32,'Switzerland','USERS',NULL,NULL)");
        SQLS.append(",('RESPONSABILITY','[USERS] ES',72,32,'Spain','USERS',NULL,NULL)");
        SQLS.append(",('RESPONSABILITY','[USERS] IT',73,32,'Italy','USERS',NULL,NULL)");
        SQLS.append(",('RESPONSABILITY','[USERS] PT',74,32,'Portugal','USERS',NULL,NULL)");
        SQLS.append(",('RESPONSABILITY','[USERS] RU',75,32,'Russia','USERS',NULL,NULL)");
        SQLS.append(",('RESPONSABILITY','[USERS] UA',76,32,'Ukrainia','USERS',NULL,NULL)");
        SQLS.append(",('RESPONSABILITY','[USERS] UK',77,32,'United Kingdom','USERS',NULL,NULL)");
        SQLS.append(",('RESPONSABILITY','[USERS] VI',78,32,'Generic','USERS',NULL,NULL)");
        SQLS.append(",('RESPONSABILITY','[USERS] DE',79,32,'Germany','USERS',NULL,NULL)");
        SQLS.append(",('RESPONSABILITY','[SUPPLIER] ATOS',80,32,'Atos','SUPPLIER',NULL,NULL)");
        SQLS.append(",('RESPONSABILITY','[SUPPLIER] LINKBYNET',90,32,'Link By Net','SUPPLIER',NULL,NULL)");
        SQLS.append(",('RESPONSABILITY','[SUPPLIER] TELINDUS',100,32,'Teloindus','SUPPLIER',NULL,NULL)");
        SQLS.append(",('RESPONSABILITY','[SUPPLIER] EXTERNAL',101,32,'External Supplier','SUPPLIER',NULL,NULL)");
        SQLS.append(",('STATUS','OPEN',1,33,'Non conformities is still in investigation',NULL,NULL,NULL)");
        SQLS.append(",('STATUS','CLOSED',2,33,'Non conformity is closed',NULL,NULL,NULL)");
        SQLS.append(",('SEVERITY','1',10,34,'The Most critical : Unavailability',NULL,NULL,NULL)");
        SQLS.append(",('SEVERITY','2',20,34,'Bad Customer experience : Slowness or error page',NULL,NULL,NULL)");
        SQLS.append(",('SEVERITY','3',30,34,'No customer impact but impact for internal resources',NULL,NULL,NULL)");
        SQLS.append(",('SEVERITY','4',40,34,'Low severity',NULL,NULL,NULL)");
        SQLS.append(",('TCESTATUS','OK',1,35,'Test was fully executed and no bug are to be reported.',NULL,NULL,NULL)");
        SQLS.append(",('TCESTATUS','KO',2,35,'Test was executed and bug have been detected.',NULL,NULL,NULL)");
        SQLS.append(",('TCESTATUS','PE',3,35,'Test execution is still running...',NULL,NULL,NULL)");
        SQLS.append(",('TCESTATUS','FA',4,35,'Test could not be executed because there is a bug on the test.',NULL,NULL,NULL)");
        SQLS.append(",('TCESTATUS','NA',5,35,'Test could not be executed because some test data are not available.',NULL,NULL,NULL)");
        SQLS.append(",('MAXEXEC','50',1,36,'50',NULL,NULL,NULL)");
        SQLS.append(",('MAXEXEC','100',2,36,'100',NULL,NULL,NULL)");
        SQLS.append(",('MAXEXEC','200',3,36,'200',NULL,NULL,NULL)");
        SQLS.append(",('MAXEXEC','500',4,36,'500',NULL,NULL,NULL)");
        SQLS.append(",('MAXEXEC','1000',5,36,'1000',NULL,NULL,NULL);");
        SQLInstruction.add(SQLS.toString());

        SQLS = new StringBuilder();
        SQLS.append("CREATE TABLE `tag` (");
        SQLS.append("  `id` int(11) NOT NULL AUTO_INCREMENT,");
        SQLS.append("  `Tag` varchar(145) NOT NULL,");
        SQLS.append("  `TagDateCre` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,");
        SQLS.append("  PRIMARY KEY (`id`),");
        SQLS.append("  UNIQUE KEY `Tag_UNIQUE` (`Tag`)");
        SQLS.append(") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;");
        SQLInstruction.add(SQLS.toString());

        SQLS = new StringBuilder();
        SQLS.append("CREATE TABLE `deploytype` (");
        SQLS.append("  `deploytype` varchar(50) NOT NULL,");
        SQLS.append("  `description` varchar(200) DEFAULT '',");
        SQLS.append("  PRIMARY KEY (`deploytype`)");
        SQLS.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8");
        SQLInstruction.add(SQLS.toString());

        SQLS = new StringBuilder();
        SQLS.append("CREATE TABLE `application` (");
        SQLS.append("  `Application` varchar(45) NOT NULL,");
        SQLS.append("  `description` varchar(200) DEFAULT NULL,");
        SQLS.append("  `internal` varchar(1) NOT NULL COMMENT 'VC Application',");
        SQLS.append("  `sort` int(11) NOT NULL,");
        SQLS.append("  `type` varchar(10) DEFAULT NULL,");
        SQLS.append("  `system` varchar(45) NOT NULL DEFAULT '',");
        SQLS.append("  `svnurl` varchar(150) DEFAULT NULL,");
        SQLS.append("  `deploytype` varchar(50) DEFAULT NULL,");
        SQLS.append("  `mavengroupid` varchar(50) DEFAULT '',");
        SQLS.append("  PRIMARY KEY (`Application`),");
        SQLS.append("  KEY `FK_application` (`deploytype`),");
        SQLS.append("  CONSTRAINT `FK_application` FOREIGN KEY (`deploytype`) REFERENCES `deploytype` (`deploytype`) ON DELETE CASCADE ON UPDATE CASCADE");
        SQLS.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8;");
        SQLInstruction.add(SQLS.toString());

        SQLS = new StringBuilder();
        SQLS.append("CREATE TABLE `project` (");
        SQLS.append("  `idproject` varchar(45) NOT NULL,");
        SQLS.append("  `VCCode` varchar(20) DEFAULT NULL,");
        SQLS.append("  `Description` varchar(45) DEFAULT NULL,");
        SQLS.append("  `active` varchar(1) DEFAULT 'Y',");
        SQLS.append("  `datecre` timestamp NULL DEFAULT CURRENT_TIMESTAMP,");
        SQLS.append("  PRIMARY KEY (`idproject`)");
        SQLS.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8;");
        SQLInstruction.add(SQLS.toString());

        SQLS = new StringBuilder();
        SQLS.append("CREATE TABLE `batchinvariant` (");
        SQLS.append("  `Batch` varchar(1) NOT NULL DEFAULT '',");
        SQLS.append("  `IncIni` varchar(45) DEFAULT NULL,");
        SQLS.append("  `Unit` varchar(45) DEFAULT NULL,");
        SQLS.append("  `Description` varchar(45) DEFAULT NULL,");
        SQLS.append("  PRIMARY KEY (`Batch`) USING BTREE");
        SQLS.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8;");
        SQLInstruction.add(SQLS.toString());

        SQLS = new StringBuilder();
        SQLS.append("CREATE TABLE `test` (");
        SQLS.append("  `Test` varchar(45) NOT NULL,");
        SQLS.append("  `Description` varchar(300) NOT NULL,");
        SQLS.append("  `Active` varchar(1) NOT NULL,");
        SQLS.append("  `Automated` varchar(1) NOT NULL,");
        SQLS.append("  `TDateCrea` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,");
        SQLS.append("  PRIMARY KEY (`Test`),");
        SQLS.append("  KEY `ix_Test_Active` (`Test`,`Active`)");
        SQLS.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8;");
        SQLInstruction.add(SQLS.toString());

        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `application` VALUES ('Google','Google Website','N',240,'GUI','DEFAULT','',NULL,'');");
        SQLInstruction.add(SQLS.toString());

        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `test` VALUES ('Examples','Example Tests','Y','Y','2012-06-19 09:56:06'),('Performance Monitor','Performance Monitor Tests','Y','Y','2012-06-19 09:56:06'),('Business Activity Monitor','Business Activity Monitor Tests','Y','Y','2012-06-19 09:56:06'),('Pre Testing','Preliminary Tests','Y','Y','0000-00-00 00:00:00');");
        SQLInstruction.add(SQLS.toString());

        SQLS = new StringBuilder();
        SQLS.append("CREATE TABLE `testcase` (");
        SQLS.append("  `Test` varchar(45) NOT NULL,");
        SQLS.append("  `TestCase` varchar(45) NOT NULL,");
        SQLS.append("  `Application` varchar(45) DEFAULT NULL,");
        SQLS.append("  `Project` varchar(45) DEFAULT NULL,");
        SQLS.append("  `Ticket` varchar(20) DEFAULT '',");
        SQLS.append("  `Description` varchar(500) NOT NULL,");
        SQLS.append("  `BehaviorOrValueExpected` varchar(2500) NOT NULL,");
        SQLS.append("  `ReadOnly` varchar(1) DEFAULT 'N',");
        SQLS.append("  `ChainNumberNeeded` int(10) unsigned DEFAULT NULL,");
        SQLS.append("  `Priority` int(1) unsigned NOT NULL,");
        SQLS.append("  `Status` varchar(25) NOT NULL,");
        SQLS.append("  `TcActive` varchar(1) NOT NULL,");
        SQLS.append("  `Group` varchar(45) DEFAULT NULL,");
        SQLS.append("  `Origine` varchar(45) DEFAULT NULL,");
        SQLS.append("  `RefOrigine` varchar(45) DEFAULT NULL,");
        SQLS.append("  `HowTo` varchar(2500) DEFAULT NULL,");
        SQLS.append("  `Comment` varchar(500) DEFAULT NULL,");
        SQLS.append("  `TCDateCrea` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,");
        SQLS.append("  `FromBuild` varchar(10) DEFAULT NULL,");
        SQLS.append("  `FromRev` varchar(20) DEFAULT NULL,");
        SQLS.append("  `ToBuild` varchar(10) DEFAULT NULL,");
        SQLS.append("  `ToRev` varchar(20) DEFAULT NULL,");
        SQLS.append("  `BugID` varchar(10) DEFAULT NULL,");
        SQLS.append("  `TargetBuild` varchar(10) DEFAULT NULL,");
        SQLS.append("  `TargetRev` varchar(20) DEFAULT NULL,");
        SQLS.append("  `Creator` varchar(45) DEFAULT NULL,");
        SQLS.append("  `Implementer` varchar(45) DEFAULT NULL,");
        SQLS.append("  `LastModifier` varchar(45) DEFAULT NULL,");
        SQLS.append("  `Sla` varchar(45) DEFAULT NULL,");
        SQLS.append("  `activeQA` varchar(1) DEFAULT 'Y',");
        SQLS.append("  `activeUAT` varchar(1) DEFAULT 'Y',");
        SQLS.append("  `activePROD` varchar(1) DEFAULT 'N',");
        SQLS.append("  PRIMARY KEY (`Test`,`TestCase`),");
        SQLS.append("  KEY `Index_2` (`Group`),");
        SQLS.append("  KEY `Index_3` (`Test`,`TestCase`,`Application`,`TcActive`,`Group`),");
        SQLS.append("  KEY `FK_testcase_2` (`Application`),");
        SQLS.append("  KEY `FK_testcase_3` (`Project`),");
        SQLS.append("  CONSTRAINT `FK_testcase_1` FOREIGN KEY (`Test`) REFERENCES `test` (`Test`) ON DELETE CASCADE ON UPDATE CASCADE,");
        SQLS.append("  CONSTRAINT `FK_testcase_2` FOREIGN KEY (`Application`) REFERENCES `application` (`Application`) ON DELETE CASCADE ON UPDATE CASCADE,");
        SQLS.append("  CONSTRAINT `FK_testcase_3` FOREIGN KEY (`Project`) REFERENCES `project` (`idproject`) ON DELETE CASCADE ON UPDATE CASCADE");
        SQLS.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8;");
        SQLInstruction.add(SQLS.toString());

        SQLS = new StringBuilder();
        SQLS.append("CREATE TABLE `testcasecountry` (");
        SQLS.append("  `Test` varchar(45) NOT NULL,");
        SQLS.append("  `TestCase` varchar(45) NOT NULL,");
        SQLS.append("  `Country` varchar(2) NOT NULL,");
        SQLS.append("  PRIMARY KEY (`Test`,`TestCase`,`Country`),");
        SQLS.append("  CONSTRAINT `FK_testcasecountry_1` FOREIGN KEY (`Test`, `TestCase`) REFERENCES `testcase` (`Test`, `TestCase`) ON DELETE CASCADE ON UPDATE CASCADE");
        SQLS.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8;");
        SQLInstruction.add(SQLS.toString());

        SQLS = new StringBuilder();
        SQLS.append("CREATE TABLE `testcasestep` (");
        SQLS.append("  `Test` varchar(45) NOT NULL,");
        SQLS.append("  `TestCase` varchar(45) NOT NULL,");
        SQLS.append("  `Step` int(10) unsigned NOT NULL,");
        SQLS.append("  `Description` varchar(150) NOT NULL,");
        SQLS.append("  PRIMARY KEY (`Test`,`TestCase`,`Step`),");
        SQLS.append("  CONSTRAINT `FK_testcasestep_1` FOREIGN KEY (`Test`, `TestCase`) REFERENCES `testcase` (`Test`, `TestCase`) ON DELETE CASCADE ON UPDATE CASCADE");
        SQLS.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8;");
        SQLInstruction.add(SQLS.toString());

        SQLS = new StringBuilder();
        SQLS.append("CREATE TABLE `testcasestepbatch` (");
        SQLS.append("  `Test` varchar(45) NOT NULL,");
        SQLS.append("  `TestCase` varchar(45) NOT NULL,");
        SQLS.append("  `Step` varchar(45) NOT NULL,");
        SQLS.append("  `Batch` varchar(1) NOT NULL DEFAULT '',");
        SQLS.append("  PRIMARY KEY (`Test`,`TestCase`,`Step`,`Batch`) USING BTREE,");
        SQLS.append("  KEY `fk_testcasestepbatch_1` (`Batch`),");
        SQLS.append("  CONSTRAINT `FK_testcasestepbatchl_1` FOREIGN KEY (`Test`, `TestCase`) REFERENCES `testcase` (`Test`, `TestCase`) ON DELETE CASCADE ON UPDATE CASCADE,");
        SQLS.append("  CONSTRAINT `FK_testcasestepbatch_2` FOREIGN KEY (`Batch`) REFERENCES `batchinvariant` (`Batch`) ON DELETE CASCADE ON UPDATE CASCADE");
        SQLS.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8;");
        SQLInstruction.add(SQLS.toString());

        SQLS = new StringBuilder();
        SQLS.append("CREATE TABLE `testcasecountryproperties` (");
        SQLS.append("  `Test` varchar(45) NOT NULL,");
        SQLS.append("  `TestCase` varchar(45) NOT NULL,");
        SQLS.append("  `Country` varchar(2) NOT NULL,");
        SQLS.append("  `Property` varchar(150) NOT NULL,");
        SQLS.append("  `Type` varchar(45) NOT NULL,");
        SQLS.append("  `Database` varchar(45) DEFAULT NULL,");
        SQLS.append("  `Value` varchar(2500) NOT NULL,");
        SQLS.append("  `Length` int(10) unsigned NOT NULL,");
        SQLS.append("  `RowLimit` int(10) unsigned NOT NULL,");
        SQLS.append("  `Nature` varchar(45) NOT NULL,");
        SQLS.append("  PRIMARY KEY (`Test`,`TestCase`,`Country`,`Property`) USING BTREE,");
        SQLS.append("  CONSTRAINT `FK_testcasecountryproperties_1` FOREIGN KEY (`Test`, `TestCase`, `Country`) REFERENCES `testcasecountry` (`Test`, `TestCase`, `Country`) ON DELETE CASCADE ON UPDATE CASCADE");
        SQLS.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8;");
        SQLInstruction.add(SQLS.toString());

        SQLS = new StringBuilder();
        SQLS.append("CREATE TABLE `testcasestepaction` (");
        SQLS.append("  `Test` varchar(45) NOT NULL,");
        SQLS.append("  `TestCase` varchar(45) NOT NULL,");
        SQLS.append("  `Step` int(10) unsigned NOT NULL,");
        SQLS.append("  `Sequence` int(10) unsigned NOT NULL,");
        SQLS.append("  `Action` varchar(45) NOT NULL DEFAULT '',");
        SQLS.append("  `Object` varchar(200) NOT NULL DEFAULT '',");
        SQLS.append("  `Property` varchar(45) DEFAULT NULL,");
        SQLS.append("  PRIMARY KEY (`Test`,`TestCase`,`Step`,`Sequence`),");
        SQLS.append("  CONSTRAINT `FK_testcasestepaction_1` FOREIGN KEY (`Test`, `TestCase`, `Step`) REFERENCES `testcasestep` (`Test`, `TestCase`, `Step`) ON DELETE CASCADE ON UPDATE CASCADE");
        SQLS.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8;");
        SQLInstruction.add(SQLS.toString());

        SQLS = new StringBuilder();
        SQLS.append("CREATE TABLE `testcasestepactioncontrol` (");
        SQLS.append("  `Test` varchar(45) NOT NULL,");
        SQLS.append("  `TestCase` varchar(45) NOT NULL,");
        SQLS.append("  `Step` int(10) unsigned NOT NULL,");
        SQLS.append("  `Sequence` int(10) unsigned NOT NULL,");
        SQLS.append("  `Control` int(10) unsigned NOT NULL,");
        SQLS.append("  `Type` varchar(200) NOT NULL DEFAULT '',");
        SQLS.append("  `ControlValue` varchar(200) NOT NULL DEFAULT '',");
        SQLS.append("  `ControlProperty` varchar(200) DEFAULT NULL,");
        SQLS.append("  `Fatal` varchar(1) DEFAULT 'Y',");
        SQLS.append("  PRIMARY KEY (`Test`,`Sequence`,`Step`,`TestCase`,`Control`) USING BTREE,");
        SQLS.append("  KEY `FK_testcasestepcontrol_1` (`Test`,`TestCase`,`Step`,`Sequence`),");
        SQLS.append("  CONSTRAINT `FK_testcasestepcontrol_1` FOREIGN KEY (`Test`, `TestCase`, `Step`, `Sequence`) REFERENCES `testcasestepaction` (`Test`, `TestCase`, `Step`, `Sequence`) ON DELETE CASCADE ON UPDATE CASCADE");
        SQLS.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8;");
        SQLInstruction.add(SQLS.toString());

        SQLS = new StringBuilder();
        SQLS.append("CREATE TABLE `sqllibrary` (");
        SQLS.append("  `Type` varchar(45) NOT NULL,");
        SQLS.append("  `Name` varchar(45) NOT NULL,");
        SQLS.append("  `Script` varchar(2500) NOT NULL,");
        SQLS.append("  `Description` varchar(1000) DEFAULT NULL,");
        SQLS.append("  PRIMARY KEY (`Name`)");
        SQLS.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8;");
        SQLInstruction.add(SQLS.toString());

        SQLS = new StringBuilder();
        SQLS.append("CREATE TABLE `countryenvparam` (");
        SQLS.append("  `Country` varchar(2) NOT NULL,");
        SQLS.append("  `Environment` varchar(45) NOT NULL,");
        SQLS.append("  `Build` varchar(10) DEFAULT NULL,");
        SQLS.append("  `Revision` varchar(20) DEFAULT NULL,");
        SQLS.append("  `Chain` varchar(20) DEFAULT NULL,");
        SQLS.append("  `DistribList` text,");
        SQLS.append("  `EMailBodyRevision` text,");
        SQLS.append("  `Type` varchar(20) DEFAULT NULL,");
        SQLS.append("  `EMailBodyChain` text,");
        SQLS.append("  `EMailBodyDisableEnvironment` text,");
        SQLS.append("  `active` varchar(1) NOT NULL DEFAULT 'N',");
        SQLS.append("  `maintenanceact` varchar(1) DEFAULT 'N',");
        SQLS.append("  `maintenancestr` time DEFAULT NULL,");
        SQLS.append("  `maintenanceend` time DEFAULT NULL,");
        SQLS.append("  PRIMARY KEY (`Country`,`Environment`)");
        SQLS.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8;");
        SQLInstruction.add(SQLS.toString());

        SQLS = new StringBuilder();
        SQLS.append("CREATE TABLE `countryenvironmentparameters` (");
        SQLS.append("  `Country` varchar(2) NOT NULL,");
        SQLS.append("  `Environment` varchar(45) NOT NULL,");
        SQLS.append("  `Application` varchar(45) NOT NULL,");
        SQLS.append("  `IP` varchar(45) NOT NULL,");
        SQLS.append("  `URL` varchar(150) NOT NULL,");
        SQLS.append("  `URLLOGIN` varchar(150) DEFAULT NULL,");
        SQLS.append("  `JdbcUser` varchar(45) DEFAULT NULL,");
        SQLS.append("  `JdbcPass` varchar(45) DEFAULT NULL,");
        SQLS.append("  `JdbcIP` varchar(45) DEFAULT NULL,");
        SQLS.append("  `JdbcPort` int(10) unsigned DEFAULT NULL,");
        SQLS.append("  `as400LIB` varchar(10) DEFAULT NULL,");
        SQLS.append("  PRIMARY KEY (`Country`,`Environment`,`Application`),");
        SQLS.append("  KEY `FK_countryenvironmentparameters_1` (`Country`,`Environment`),");
        SQLS.append("  KEY `FK_countryenvironmentparameters_3` (`Application`),");
        SQLS.append("  CONSTRAINT `FK_countryenvironmentparameters_1` FOREIGN KEY (`Country`, `Environment`) REFERENCES `countryenvparam` (`Country`, `Environment`) ON DELETE CASCADE ON UPDATE CASCADE,");
        SQLS.append("  CONSTRAINT `FK_countryenvironmentparameters_3` FOREIGN KEY (`Application`) REFERENCES `application` (`Application`) ON DELETE CASCADE ON UPDATE CASCADE");
        SQLS.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8;");
        SQLInstruction.add(SQLS.toString());

        SQLS = new StringBuilder();
        SQLS.append("CREATE TABLE `countryenvironmentdatabase` (");
        SQLS.append("  `Database` varchar(45) NOT NULL,");
        SQLS.append("  `Environment` varchar(45) NOT NULL,");
        SQLS.append("  `Country` varchar(2) NOT NULL,");
        SQLS.append("  `ConnectionPoolName` varchar(25) NOT NULL,");
        SQLS.append("  PRIMARY KEY (`Database`,`Environment`,`Country`),");
        SQLS.append("  KEY `FK_countryenvironmentdatabase_1` (`Country`,`Environment`),");
        SQLS.append("  CONSTRAINT `FK_countryenvironmentdatabase_1` FOREIGN KEY (`Country`, `Environment`) REFERENCES `countryenvparam` (`Country`, `Environment`) ON DELETE CASCADE ON UPDATE CASCADE");
        SQLS.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8;");
        SQLInstruction.add(SQLS.toString());

        SQLS = new StringBuilder();
        SQLS.append("CREATE TABLE `host` (");
        SQLS.append("  `Country` varchar(2) NOT NULL,");
        SQLS.append("  `Session` varchar(20) NOT NULL,");
        SQLS.append("  `Environment` varchar(45) NOT NULL,");
        SQLS.append("  `Server` varchar(20) NOT NULL,");
        SQLS.append("  `host` varchar(20) DEFAULT NULL,");
        SQLS.append("  `secure` varchar(1) DEFAULT 'N',");
        SQLS.append("  `port` varchar(20) DEFAULT NULL,");
        SQLS.append("  `active` varchar(1) DEFAULT 'Y',");
        SQLS.append("  PRIMARY KEY (`Country`,`Session`,`Environment`,`Server`) USING BTREE,");
        SQLS.append("  KEY `FK_host_1` (`Country`,`Environment`),");
        SQLS.append("  CONSTRAINT `FK_host_1` FOREIGN KEY (`Country`, `Environment`) REFERENCES `countryenvparam` (`Country`, `Environment`) ON DELETE CASCADE ON UPDATE CASCADE");
        SQLS.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8;");
        SQLInstruction.add(SQLS.toString());

        SQLS = new StringBuilder();
        SQLS.append("CREATE TABLE `countryenvparam_log` (");
        SQLS.append("  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,");
        SQLS.append("  `Country` varchar(2) NOT NULL,");
        SQLS.append("  `Environment` varchar(45) NOT NULL,");
        SQLS.append("  `Build` varchar(10) DEFAULT NULL,");
        SQLS.append("  `Revision` varchar(20) DEFAULT NULL,");
        SQLS.append("  `Chain` int(10) unsigned DEFAULT NULL,");
        SQLS.append("  `Description` varchar(150) DEFAULT NULL,");
        SQLS.append("  `datecre` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,");
        SQLS.append("  PRIMARY KEY (`id`),");
        SQLS.append("  KEY `ID1` (`Country`,`Environment`),");
        SQLS.append("  KEY `FK_countryenvparam_log_1` (`Country`,`Environment`),");
        SQLS.append("  CONSTRAINT `FK_countryenvparam_log_1` FOREIGN KEY (`Country`, `Environment`) REFERENCES `countryenvparam` (`Country`, `Environment`) ON DELETE CASCADE ON UPDATE CASCADE");
        SQLS.append(") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;");
        SQLInstruction.add(SQLS.toString());

        SQLS = new StringBuilder();
        SQLS.append("CREATE TABLE `buildrevisionbatch` (");
        SQLS.append("  `ID` int(11) NOT NULL AUTO_INCREMENT,");
        SQLS.append("  `Batch` varchar(1) NOT NULL,");
        SQLS.append("  `Country` varchar(2) DEFAULT NULL,");
        SQLS.append("  `Build` varchar(45) DEFAULT NULL,");
        SQLS.append("  `Revision` varchar(45) DEFAULT NULL,");
        SQLS.append("  `Environment` varchar(45) DEFAULT NULL,");
        SQLS.append("  `DateBatch` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,");
        SQLS.append("  PRIMARY KEY (`ID`) USING BTREE,");
        SQLS.append("  KEY `FK_buildrevisionbatch_1` (`Batch`),");
        SQLS.append("  KEY `FK_buildrevisionbatch_2` (`Country`,`Environment`),");
        SQLS.append("  CONSTRAINT `FK_buildrevisionbatch_1` FOREIGN KEY (`Batch`) REFERENCES `batchinvariant` (`Batch`) ON DELETE CASCADE ON UPDATE CASCADE,");
        SQLS.append("  CONSTRAINT `FK_buildrevisionbatch_2` FOREIGN KEY (`Country`, `Environment`) REFERENCES `countryenvparam` (`Country`, `Environment`) ON DELETE CASCADE ON UPDATE CASCADE");
        SQLS.append(") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;");
        SQLInstruction.add(SQLS.toString());

        SQLS = new StringBuilder();
        SQLS.append("CREATE TABLE `buildrevisionparameters` (");
        SQLS.append("  `ID` int(11) NOT NULL AUTO_INCREMENT,");
        SQLS.append("  `Build` varchar(10) DEFAULT NULL,");
        SQLS.append("  `Revision` varchar(20) DEFAULT NULL,");
        SQLS.append("  `Release` varchar(40) DEFAULT NULL,");
        SQLS.append("  `Application` varchar(45) DEFAULT NULL,");
        SQLS.append("  `Project` varchar(45) DEFAULT '',");
        SQLS.append("  `TicketIDFixed` varchar(45) DEFAULT '',");
        SQLS.append("  `BugIDFixed` varchar(45) DEFAULT '',");
        SQLS.append("  `Link` varchar(300) DEFAULT '',");
        SQLS.append("  `ReleaseOwner` varchar(100) NOT NULL DEFAULT '',");
        SQLS.append("  `Subject` varchar(1000) DEFAULT '',");
        SQLS.append("  `datecre` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,");
        SQLS.append("  `jenkinsbuildid` varchar(200) DEFAULT '',");
        SQLS.append("  `mavengroupid` varchar(200) DEFAULT '',");
        SQLS.append("  `mavenartifactid` varchar(200) DEFAULT '',");
        SQLS.append("  `mavenversion` varchar(200) DEFAULT '',");
        SQLS.append("  PRIMARY KEY (`ID`),");
        SQLS.append("  KEY `FK1` (`Application`),");
        SQLS.append("  CONSTRAINT `FK1` FOREIGN KEY (`Application`) REFERENCES `application` (`Application`) ON DELETE CASCADE ON UPDATE CASCADE");
        SQLS.append(") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;");
        SQLInstruction.add(SQLS.toString());

        SQLS = new StringBuilder();
        SQLS.append("CREATE TABLE `logevent` (");
        SQLS.append("  `LogEventID` int(10) unsigned NOT NULL AUTO_INCREMENT,");
        SQLS.append("  `UserID` int(10) unsigned NOT NULL,");
        SQLS.append("  `Time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,");
        SQLS.append("  `Page` varchar(25) DEFAULT NULL,");
        SQLS.append("  `Action` varchar(50) DEFAULT NULL,");
        SQLS.append("  `Log` varchar(500) DEFAULT NULL,");
        SQLS.append("  PRIMARY KEY (`LogEventID`)");
        SQLS.append(") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;");
        SQLInstruction.add(SQLS.toString());

        SQLS = new StringBuilder();
        SQLS.append("CREATE TABLE `logeventchange` (");
        SQLS.append("  `LogEventChangeID` int(10) unsigned NOT NULL AUTO_INCREMENT,");
        SQLS.append("  `LogEventID` int(10) unsigned NOT NULL,");
        SQLS.append("  `LogTable` varchar(50) DEFAULT NULL,");
        SQLS.append("  `LogBefore` varchar(5000) DEFAULT NULL,");
        SQLS.append("  `LogAfter` varchar(5000) DEFAULT NULL,");
        SQLS.append("  `datecre` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,");
        SQLS.append("  PRIMARY KEY (`LogEventChangeID`)");
        SQLS.append(") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;");
        SQLInstruction.add(SQLS.toString());

        SQLS = new StringBuilder();
        SQLS.append("CREATE TABLE `testcaseexecution` (");
        SQLS.append("  `ID` bigint(20) unsigned NOT NULL AUTO_INCREMENT,");
        SQLS.append("  `Test` varchar(45) NOT NULL,");
        SQLS.append("  `TestCase` varchar(45) NOT NULL,");
        SQLS.append("  `Build` varchar(10) DEFAULT NULL,");
        SQLS.append("  `Revision` varchar(5) DEFAULT NULL,");
        SQLS.append("  `Environment` varchar(45) DEFAULT NULL,");
        SQLS.append("  `Country` varchar(2) DEFAULT NULL,");
        SQLS.append("  `Browser` varchar(20) DEFAULT NULL,");
        SQLS.append("  `Start` timestamp NULL DEFAULT CURRENT_TIMESTAMP,");
        SQLS.append("  `End` timestamp NULL DEFAULT '0000-00-00 00:00:00',");
        SQLS.append("  `ControlStatus` varchar(2) DEFAULT NULL,");
        SQLS.append("  `Application` varchar(45) DEFAULT NULL,");
        SQLS.append("  `IP` varchar(45) DEFAULT NULL,");
        SQLS.append("  `URL` varchar(150) DEFAULT NULL,");
        SQLS.append("  `Port` varchar(45) DEFAULT NULL,");
        SQLS.append("  `Tag` varchar(50) DEFAULT NULL,");
        SQLS.append("  `Finished` varchar(1) DEFAULT NULL,");
        SQLS.append("  `Verbose` varchar(1) DEFAULT NULL,");
        SQLS.append("  `Status` varchar(25) DEFAULT NULL,");
        SQLS.append("  PRIMARY KEY (`ID`),");
        SQLS.append("  KEY `FK_TestCaseExecution_1` (`Test`,`TestCase`),");
        SQLS.append("  KEY `fk_testcaseexecution_2` (`Tag`),");
        SQLS.append("  KEY `index_1` (`Start`),");
        SQLS.append("  KEY `IX_test_testcase_country` (`Test`,`TestCase`,`Country`,`Start`,`ControlStatus`),");
        SQLS.append("  KEY `index_buildrev` (`Build`,`Revision`),");
        SQLS.append("  KEY `FK_testcaseexecution_3` (`Application`),");
        SQLS.append("  KEY `fk_test` (`Test`),");
        SQLS.append("  KEY `ix_TestcaseExecution` (`Test`,`TestCase`,`Build`,`Revision`,`Environment`,`Country`,`ID`),");
        SQLS.append("  CONSTRAINT `FK_testcaseexecution_1` FOREIGN KEY (`Test`, `TestCase`) REFERENCES `testcase` (`Test`, `TestCase`) ON DELETE CASCADE ON UPDATE CASCADE,");
        SQLS.append("  CONSTRAINT `FK_testcaseexecution_3` FOREIGN KEY (`Application`) REFERENCES `application` (`Application`) ON DELETE CASCADE ON UPDATE CASCADE");
        SQLS.append(") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;");
        SQLInstruction.add(SQLS.toString());

        SQLS = new StringBuilder();
        SQLS.append("CREATE TABLE `testcaseexecutiondata` (");
        SQLS.append("  `ID` bigint(20) unsigned NOT NULL AUTO_INCREMENT,");
        SQLS.append("  `Property` varchar(150) NOT NULL,");
        SQLS.append("  `Value` varchar(150) NOT NULL,");
        SQLS.append("  `Type` varchar(200) DEFAULT NULL,");
        SQLS.append("  `Object` varchar(2500) DEFAULT NULL,");
        SQLS.append("  `RC` varchar(10) DEFAULT NULL,");
        SQLS.append("  `Start` timestamp NULL DEFAULT NULL,");
        SQLS.append("  `End` timestamp NULL DEFAULT NULL,");
        SQLS.append("  `StartLong` bigint(20) DEFAULT NULL,");
        SQLS.append("  `EndLong` bigint(20) DEFAULT NULL,");
        SQLS.append("  PRIMARY KEY (`ID`,`Property`),");
        SQLS.append("  KEY `propertystart` (`Property`,`Start`),");
        SQLS.append("  KEY `index_1` (`Start`),");
        SQLS.append("  CONSTRAINT `FK_TestCaseExecutionData_1` FOREIGN KEY (`ID`) REFERENCES `testcaseexecution` (`ID`) ON DELETE CASCADE");
        SQLS.append(") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;");
        SQLInstruction.add(SQLS.toString());

        SQLS = new StringBuilder();
        SQLS.append("CREATE TABLE `testcaseexecutionwwwdet` (");
        SQLS.append("  `ID` bigint(20) NOT NULL AUTO_INCREMENT,");
        SQLS.append("  `ExecID` bigint(20) unsigned NOT NULL,");
        SQLS.append("  `Start` varchar(45) DEFAULT NULL,");
        SQLS.append("  `url` varchar(500) DEFAULT NULL,");
        SQLS.append("  `End` varchar(45) DEFAULT NULL,");
        SQLS.append("  `ext` varchar(10) DEFAULT NULL,");
        SQLS.append("  `statusCode` int(11) DEFAULT NULL,");
        SQLS.append("  `method` varchar(10) DEFAULT NULL,");
        SQLS.append("  `bytes` int(11) DEFAULT NULL,");
        SQLS.append("  `timeInMillis` int(11) DEFAULT NULL,");
        SQLS.append("  `ReqHeader_Host` varchar(45) DEFAULT NULL,");
        SQLS.append("  `ResHeader_ContentType` varchar(45) DEFAULT NULL,");
        SQLS.append("  `ReqPage` varchar(500) DEFAULT NULL,");
        SQLS.append("  PRIMARY KEY (`ID`),");
        SQLS.append("  KEY `FK_testcaseexecutionwwwdet_1` (`ExecID`),");
        SQLS.append("  CONSTRAINT `FK_testcaseexecutionwwwdet_1` FOREIGN KEY (`ExecID`) REFERENCES `testcaseexecution` (`ID`) ON DELETE CASCADE ON UPDATE CASCADE");
        SQLS.append(") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;");
        SQLInstruction.add(SQLS.toString());

        SQLS = new StringBuilder();
        SQLS.append("CREATE TABLE `testcaseexecutionwwwsum` (");
        SQLS.append("  `ID` bigint(20) unsigned NOT NULL,");
        SQLS.append("  `tot_nbhits` int(11) DEFAULT NULL,");
        SQLS.append("  `tot_tps` int(11) DEFAULT NULL,");
        SQLS.append("  `tot_size` int(11) DEFAULT NULL,");
        SQLS.append("  `nb_rc2xx` int(11) DEFAULT NULL,");
        SQLS.append("  `nb_rc3xx` int(11) DEFAULT NULL,");
        SQLS.append("  `nb_rc4xx` int(11) DEFAULT NULL,");
        SQLS.append("  `nb_rc5xx` int(11) DEFAULT NULL,");
        SQLS.append("  `img_nb` int(11) DEFAULT NULL,");
        SQLS.append("  `img_tps` int(11) DEFAULT NULL,");
        SQLS.append("  `img_size_tot` int(11) DEFAULT NULL,");
        SQLS.append("  `img_size_max` int(11) DEFAULT NULL,");
        SQLS.append("  `js_nb` int(11) DEFAULT NULL,");
        SQLS.append("  `js_tps` int(11) DEFAULT NULL,");
        SQLS.append("  `js_size_tot` int(11) DEFAULT NULL,");
        SQLS.append("  `js_size_max` int(11) DEFAULT NULL,");
        SQLS.append("  `css_nb` int(11) DEFAULT NULL,");
        SQLS.append("  `css_tps` int(11) DEFAULT NULL,");
        SQLS.append("  `css_size_tot` int(11) DEFAULT NULL,");
        SQLS.append("  `css_size_max` int(11) DEFAULT NULL,");
        SQLS.append("  `img_size_max_url` varchar(500) DEFAULT NULL,");
        SQLS.append("  `js_size_max_url` varchar(500) DEFAULT NULL,");
        SQLS.append("  `css_size_max_url` varchar(500) DEFAULT NULL,");
        SQLS.append("  PRIMARY KEY (`ID`),");
        SQLS.append("  KEY `FK_testcaseexecutionwwwsum_1` (`ID`),");
        SQLS.append("  CONSTRAINT `FK_testcaseexecutionwwwsum_1` FOREIGN KEY (`ID`) REFERENCES `testcaseexecution` (`ID`) ON DELETE CASCADE ON UPDATE CASCADE");
        SQLS.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8;");
        SQLInstruction.add(SQLS.toString());

        SQLS = new StringBuilder();
        SQLS.append("CREATE TABLE `testcasestepactionexecution` (");
        SQLS.append("  `ID` bigint(20) NOT NULL,");
        SQLS.append("  `Step` int(10) NOT NULL,");
        SQLS.append("  `Sequence` int(10) NOT NULL,");
        SQLS.append("  `Action` varchar(45) NOT NULL,");
        SQLS.append("  `Object` varchar(200) DEFAULT NULL,");
        SQLS.append("  `Property` varchar(45) DEFAULT NULL,");
        SQLS.append("  `Start` timestamp NULL DEFAULT NULL,");
        SQLS.append("  `End` timestamp NULL DEFAULT NULL,");
        SQLS.append("  `StartLong` bigint(20) DEFAULT NULL,");
        SQLS.append("  `EndLong` bigint(20) DEFAULT NULL,");
        SQLS.append("  PRIMARY KEY (`ID`,`Step`,`Sequence`,`Action`)");
        SQLS.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8;");
        SQLInstruction.add(SQLS.toString());

        SQLS = new StringBuilder();
        SQLS.append("CREATE TABLE `testcasestepexecution` (");
        SQLS.append("  `ID` bigint(20) unsigned NOT NULL,");
        SQLS.append("  `Step` int(10) unsigned NOT NULL,");
        SQLS.append("  `BatNumExe` varchar(45) DEFAULT NULL,");
        SQLS.append("  `Start` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,");
        SQLS.append("  `End` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',");
        SQLS.append("  `FullStart` bigint(20) unsigned DEFAULT NULL,");
        SQLS.append("  `FullEnd` bigint(20) unsigned DEFAULT NULL,");
        SQLS.append("  `TimeElapsed` decimal(10,3) DEFAULT NULL,");
        SQLS.append("  `ReturnCode` varchar(2) DEFAULT NULL,");
        SQLS.append("  PRIMARY KEY (`ID`,`Step`),");
        SQLS.append("  CONSTRAINT `FK_testcasestepexecution_1` FOREIGN KEY (`ID`) REFERENCES `testcaseexecution` (`ID`) ON DELETE CASCADE");
        SQLS.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8;");
        SQLInstruction.add(SQLS.toString());

        SQLS = new StringBuilder();
        SQLS.append("CREATE TABLE `testcasestepactioncontrolexecution` (");
        SQLS.append("  `ID` bigint(20) unsigned NOT NULL,");
        SQLS.append("  `Step` int(10) unsigned NOT NULL,");
        SQLS.append("  `Sequence` int(10) unsigned NOT NULL,");
        SQLS.append("  `Control` int(10) unsigned NOT NULL,");
        SQLS.append("  `ReturnCode` varchar(2) NOT NULL,");
        SQLS.append("  `ControlType` varchar(200) DEFAULT NULL,");
        SQLS.append("  `ControlProperty` varchar(2500) DEFAULT NULL,");
        SQLS.append("  `ControlValue` varchar(200) DEFAULT NULL,");
        SQLS.append("  `Fatal` varchar(1) DEFAULT NULL,");
        SQLS.append("  `Start` timestamp NULL DEFAULT NULL,");
        SQLS.append("  `End` timestamp NULL DEFAULT NULL,");
        SQLS.append("  `StartLong` bigint(20) DEFAULT NULL,");
        SQLS.append("  `EndLong` bigint(20) DEFAULT NULL,");
        SQLS.append("  PRIMARY KEY (`ID`,`Step`,`Sequence`,`Control`) USING BTREE,");
        SQLS.append("  CONSTRAINT `FK_testcasestepcontrolexecution_1` FOREIGN KEY (`ID`, `Step`) REFERENCES `testcasestepexecution` (`ID`, `Step`) ON DELETE CASCADE");
        SQLS.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8;");
        SQLInstruction.add(SQLS.toString());

        SQLS = new StringBuilder();
        SQLS.append("CREATE TABLE `comparisonstatusdata` (");
        SQLS.append("  `idcomparisonstatusdata` int(11) NOT NULL AUTO_INCREMENT,");
        SQLS.append("  `Execution_ID` bigint(20) unsigned DEFAULT NULL,");
        SQLS.append("  `Property` varchar(45) DEFAULT NULL,");
        SQLS.append("  `Property_A` varchar(45) DEFAULT NULL,");
        SQLS.append("  `Property_B` varchar(45) DEFAULT NULL,");
        SQLS.append("  `Property_C` varchar(45) DEFAULT NULL,");
        SQLS.append("  `Status` varchar(45) DEFAULT NULL,");
        SQLS.append("  `Comments` varchar(1000) DEFAULT NULL,");
        SQLS.append("  PRIMARY KEY (`idcomparisonstatusdata`),");
        SQLS.append("  KEY `FK_comparisonstatusdata_1` (`Execution_ID`),");
        SQLS.append("  CONSTRAINT `FK_comparisonstatusdata_1` FOREIGN KEY (`Execution_ID`) REFERENCES `testcaseexecution` (`ID`) ON DELETE CASCADE ON UPDATE CASCADE");
        SQLS.append(") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;");
        SQLInstruction.add(SQLS.toString());

        SQLS = new StringBuilder();
        SQLS.append("CREATE TABLE `comparisonstatus` (");
        SQLS.append("  `idcomparisonstatus` int(11) NOT NULL AUTO_INCREMENT,");
        SQLS.append("  `Execution_ID` bigint(20) unsigned DEFAULT NULL,");
        SQLS.append("  `Country` varchar(2) DEFAULT NULL,");
        SQLS.append("  `Environment` varchar(45) DEFAULT NULL,");
        SQLS.append("  `InvoicingDate` varchar(45) DEFAULT NULL,");
        SQLS.append("  `TestedChain` varchar(45) DEFAULT NULL,");
        SQLS.append("  `Start` varchar(45) DEFAULT NULL,");
        SQLS.append("  `End` varchar(45) DEFAULT NULL,");
        SQLS.append("  PRIMARY KEY (`idcomparisonstatus`),");
        SQLS.append("  KEY `FK_comparisonstatus_1` (`Execution_ID`),");
        SQLS.append("  CONSTRAINT `FK_comparisonstatus_1` FOREIGN KEY (`Execution_ID`) REFERENCES `testcaseexecution` (`ID`) ON DELETE CASCADE ON UPDATE CASCADE");
        SQLS.append(") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;");
        SQLInstruction.add(SQLS.toString());

        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `project` (`idproject`, `VCCode`, `Description`, `active`) VALUES (' ', ' ', 'None', 'N');");
        SQLInstruction.add(SQLS.toString());

        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `testcase` VALUES ('Examples','0001A','Google',' ','','Search for Cerberus Website','','Y',NULL,1,'WORKING','Y','INTERACTIVE','RX','','','','2012-06-19 09:56:40','','','','','','','','cerberus','cerberus','cerberus',NULL,'Y','Y','Y')");
        SQLInstruction.add(SQLS.toString());

        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `testcasecountry` VALUES ('Examples','0001A','RX')");
        SQLInstruction.add(SQLS.toString());

        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `testcasestep` VALUES ('Examples','0001A',1,'Search')");
        SQLInstruction.add(SQLS.toString());

        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `testcasecountryproperties` VALUES ('Examples','0001A','RX','MYTEXT','text','VC','cerberus automated testing',0,0,'STATIC'), ('Examples','0001A','RX','WAIT','text','VC','5000',0,0,'STATIC')");
        SQLInstruction.add(SQLS.toString());

        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `testcasestepaction` VALUES ('Examples','0001A',1,10,'openUrlLogin','','')");
        SQLS.append(",('Examples','0001A',1,20,'type','id=gbqfq','MYTEXT')");
        SQLS.append(",('Examples','0001A',1,30,'clickAndWait','id=gbqfb','WAIT')");
        SQLInstruction.add(SQLS.toString());

        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `testcasestepactioncontrol` VALUES ('Examples','0001A',1,30,1,'verifyRegexInElement','Welcome to Cerberus Website','xpath=//div[@id=\\'search\\']/div/ol/li/div/div/div/span','Y')");
        SQLInstruction.add(SQLS.toString());

        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

        SQLS = new StringBuilder();
        SQLS.append("CREATE TABLE `abonnement` (");
        SQLS.append("  `idabonnement` int(11) NOT NULL AUTO_INCREMENT,");
        SQLS.append("  `email` varchar(45) DEFAULT NULL,");
        SQLS.append("  `notification` varchar(1000) DEFAULT NULL,");
        SQLS.append("  `frequency` varchar(45) DEFAULT NULL,");
        SQLS.append("  `LastNotification` varchar(45) DEFAULT NULL,");
        SQLS.append("  PRIMARY KEY (`idabonnement`)");
        SQLS.append(") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8");
        SQLInstruction.add(SQLS.toString());

        SQLS = new StringBuilder();
        SQLS.append("CREATE TABLE `countryenvdeploytype` (");
        SQLS.append("  `Country` varchar(2) NOT NULL,");
        SQLS.append("  `Environment` varchar(45) NOT NULL,");
        SQLS.append("  `deploytype` varchar(50) NOT NULL,");
        SQLS.append("  `JenkinsAgent` varchar(50) NOT NULL DEFAULT '',");
        SQLS.append("  PRIMARY KEY (`Country`,`Environment`,`deploytype`,`JenkinsAgent`),");
        SQLS.append("  KEY `FK_countryenvdeploytype_1` (`Country`,`Environment`),");
        SQLS.append("  KEY `FK_countryenvdeploytype_2` (`deploytype`),");
        SQLS.append("  CONSTRAINT `FK_countryenvdeploytype_1` FOREIGN KEY (`deploytype`) REFERENCES `deploytype` (`deploytype`) ON DELETE CASCADE ON UPDATE CASCADE,");
        SQLS.append("  CONSTRAINT `FK_countryenvdeploytype_2` FOREIGN KEY (`Country`, `Environment`) REFERENCES `countryenvparam` (`Country`, `Environment`) ON DELETE CASCADE ON UPDATE CASCADE");
        SQLS.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8");
        SQLInstruction.add(SQLS.toString());

        SQLS = new StringBuilder();
        SQLS.append("CREATE TABLE `logglassfish` (");
        SQLS.append("  `idlogglassfish` int(11) NOT NULL AUTO_INCREMENT,");
        SQLS.append("  `TIMESTAMP` varchar(45) DEFAULT 'CURRENT_TIMESTAMP',");
        SQLS.append("  `PARAMETER` varchar(2000) DEFAULT NULL,");
        SQLS.append("  `VALUE` varchar(2000) DEFAULT NULL,");
        SQLS.append("  PRIMARY KEY (`idlogglassfish`)");
        SQLS.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8");
        SQLInstruction.add(SQLS.toString());

        SQLS = new StringBuilder(); // To be removed as not to be used by Cerberus.
        SQLS.append("CREATE TABLE `qualitynonconformities` (");
        SQLS.append("  `idqualitynonconformities` int(11) NOT NULL AUTO_INCREMENT,");
        SQLS.append("  `Country` varchar(45) DEFAULT NULL,");
        SQLS.append("  `Application` varchar(45) DEFAULT NULL,");
        SQLS.append("  `ProblemCategory` varchar(100) DEFAULT NULL,");
        SQLS.append(" `ProblemDescription` varchar(2500) DEFAULT NULL,");
        SQLS.append(" `StartDate` varchar(45) DEFAULT NULL,");
        SQLS.append("  `StartTime` varchar(45) DEFAULT NULL,");
        SQLS.append("  `EndDate` varchar(45) DEFAULT NULL,");
        SQLS.append("  `EndTime` varchar(45) DEFAULT NULL,");
        SQLS.append(" `TeamContacted` varchar(250) DEFAULT NULL,");
        SQLS.append("  `Actions` varchar(2500) DEFAULT NULL,");
        SQLS.append("  `RootCauseCategory` varchar(100) DEFAULT NULL,");
        SQLS.append("  `RootCauseDescription` varchar(2500) DEFAULT NULL,");
        SQLS.append("  `ImpactOrCost` varchar(45) DEFAULT NULL,");
        SQLS.append("  `Responsabilities` varchar(250) DEFAULT NULL,");
        SQLS.append("  `Status` varchar(45) DEFAULT NULL,");
        SQLS.append("  `Comments` varchar(1000) DEFAULT NULL,");
        SQLS.append("  `Severity` varchar(45) DEFAULT NULL,");
        SQLS.append("  PRIMARY KEY (`idqualitynonconformities`)");
        SQLS.append(") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8");
        SQLInstruction.add(SQLS.toString());

        SQLS = new StringBuilder(); // To be removed as not to be used by Cerberus.
        SQLS.append("CREATE TABLE `qualitynonconformitiesimpact` (");
        SQLS.append("  `idqualitynonconformitiesimpact` bigint(20) NOT NULL AUTO_INCREMENT,");
        SQLS.append("  `idqualitynonconformities` int(11) DEFAULT NULL,");
        SQLS.append("  `Country` varchar(45) DEFAULT NULL,");
        SQLS.append("  `Application` varchar(45) DEFAULT NULL,");
        SQLS.append("  `StartDate` varchar(45) DEFAULT NULL,");
        SQLS.append("  `StartTime` varchar(45) DEFAULT NULL,");
        SQLS.append("  `EndDate` varchar(45) DEFAULT NULL,");
        SQLS.append("  `EndTime` varchar(45) DEFAULT NULL,");
        SQLS.append("  `ImpactOrCost` varchar(250) DEFAULT NULL,");
        SQLS.append("  PRIMARY KEY (`idqualitynonconformitiesimpact`)");
        SQLS.append(") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8");
        SQLInstruction.add(SQLS.toString());

//-- Adding subsystem column
//--------------------------
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `application` CHANGE COLUMN `System` `System` VARCHAR(45) NOT NULL DEFAULT 'DEFAULT'  ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `application` ADD COLUMN `SubSystem` VARCHAR(45) NOT NULL DEFAULT ''  AFTER `System` ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE application SET subsystem=system;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE application SET system='DEFAULT';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

//-- dropping tag table 
//--------------------------
        SQLS = new StringBuilder();
        SQLS.append("DROP TABLE `tag`;");
        SQLInstruction.add(SQLS.toString());

//-- Cerberus Engine Version inside execution table.
//--------------------------
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcaseexecution` ADD COLUMN `CrbVersion` VARCHAR(45) NULL DEFAULT NULL  AFTER `Status` ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

//-- Screenshot filename stored inside execution table. That allow to determine if screenshot is taken or not.
//--------------------------
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepactioncontrolexecution` ADD COLUMN `ScreenshotFilename` VARCHAR(45) NULL DEFAULT NULL  AFTER `EndLong` ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepactionexecution` ADD COLUMN `ScreenshotFilename` VARCHAR(45) NULL DEFAULT NULL  AFTER `EndLong` ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

//-- Test and TestCase information inside the execution tables. That will allow to have the full tracability on the pretestcase executed.
//--------------------------
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepexecution` ADD COLUMN `Test` VARCHAR(45) NULL DEFAULT NULL  AFTER `Step` , ADD COLUMN `TestCase` VARCHAR(45) NULL DEFAULT NULL  AFTER `Test` ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepexecution` CHANGE COLUMN `Step` `Step` INT(10) UNSIGNED NOT NULL  AFTER `TestCase` ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepactionexecution` ADD COLUMN `Test` VARCHAR(45) NULL DEFAULT NULL  AFTER `ID` , ADD COLUMN `TestCase` VARCHAR(45) NULL DEFAULT NULL  AFTER `Test` ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepactioncontrolexecution` ADD COLUMN `Test` VARCHAR(45) NULL DEFAULT NULL  AFTER `ID` , ADD COLUMN `TestCase` VARCHAR(45) NULL DEFAULT NULL  AFTER `Test` ;");
        SQLInstruction.add(SQLS.toString());

//-- Cleaning Index names and Foreign Key contrains
//-- ------------------------
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `application` DROP INDEX `FK_application` , ADD INDEX `FK_application_01` (`deploytype` ASC) ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `application` DROP FOREIGN KEY `FK_application` ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `application` ADD CONSTRAINT `FK_application_01` FOREIGN KEY (`deploytype` ) REFERENCES `deploytype` (`deploytype` ) ON DELETE CASCADE ON UPDATE CASCADE;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `buildrevisionbatch` DROP INDEX `FK_buildrevisionbatch_1` , ADD INDEX `FK_buildrevisionbatch_01` (`Batch` ASC) , DROP INDEX `FK_buildrevisionbatch_2` , ADD INDEX `FK_buildrevisionbatch_02` (`Country` ASC, `Environment` ASC) ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `buildrevisionbatch` DROP FOREIGN KEY `FK_buildrevisionbatch_1` , DROP FOREIGN KEY `FK_buildrevisionbatch_2` ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `buildrevisionbatch`   ADD CONSTRAINT `FK_buildrevisionbatch_01`  FOREIGN KEY (`Batch` )  REFERENCES `batchinvariant` (`Batch` )  ON DELETE CASCADE  ON UPDATE CASCADE,   ADD CONSTRAINT `FK_buildrevisionbatch_02`  FOREIGN KEY (`Country` , `Environment` )  REFERENCES `countryenvparam` (`Country` , `Environment` )  ON DELETE CASCADE  ON UPDATE CASCADE;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `buildrevisionparameters` DROP INDEX `FK1` , ADD INDEX `FK_buildrevisionparameters_01` (`Application` ASC) ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `buildrevisionparameters` DROP FOREIGN KEY `FK1` ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `buildrevisionparameters`   ADD CONSTRAINT `FK_buildrevisionparameters_01`  FOREIGN KEY (`Application` )  REFERENCES `application` (`Application` )  ON DELETE CASCADE  ON UPDATE CASCADE;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `comparisonstatus` DROP FOREIGN KEY `FK_comparisonstatus_1` ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `comparisonstatus`   ADD CONSTRAINT `FK_comparisonstatus_01`  FOREIGN KEY (`Execution_ID` )  REFERENCES `testcaseexecution` (`ID` )  ON DELETE CASCADE  ON UPDATE CASCADE, DROP INDEX `FK_comparisonstatus_1` , ADD INDEX `FK_comparisonstatus_01` (`Execution_ID` ASC) ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `comparisonstatusdata` DROP FOREIGN KEY `FK_comparisonstatusdata_1` ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `comparisonstatusdata`   ADD CONSTRAINT `FK_comparisonstatusdata_01`  FOREIGN KEY (`Execution_ID` )  REFERENCES `testcaseexecution` (`ID` )  ON DELETE CASCADE  ON UPDATE CASCADE, DROP INDEX `FK_comparisonstatusdata_1` , ADD INDEX `FK_comparisonstatusdata_01` (`Execution_ID` ASC) ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `countryenvdeploytype` DROP FOREIGN KEY `FK_countryenvdeploytype_1` , DROP FOREIGN KEY `FK_countryenvdeploytype_2` ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `countryenvdeploytype`   ADD CONSTRAINT `FK_countryenvdeploytype_01`  FOREIGN KEY (`deploytype` )  REFERENCES `deploytype` (`deploytype` )  ON DELETE CASCADE  ON UPDATE CASCADE,   ADD CONSTRAINT `FK_countryenvdeploytype_02`  FOREIGN KEY (`Country` , `Environment` )  REFERENCES `countryenvparam` (`Country` , `Environment` )  ON DELETE CASCADE  ON UPDATE CASCADE, DROP INDEX `FK_countryenvdeploytype_1` , ADD INDEX `FK_countryenvdeploytype_01` (`Country` ASC, `Environment` ASC) , DROP INDEX `FK_countryenvdeploytype_2` , ADD INDEX `FK_countryenvdeploytype_02` (`deploytype` ASC) ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `countryenvironmentdatabase` DROP FOREIGN KEY `FK_countryenvironmentdatabase_1` ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `countryenvironmentdatabase`   ADD CONSTRAINT `FK_countryenvironmentdatabase_01`  FOREIGN KEY (`Country` , `Environment` )  REFERENCES `countryenvparam` (`Country` , `Environment` )  ON DELETE CASCADE  ON UPDATE CASCADE, DROP INDEX `FK_countryenvironmentdatabase_1` , ADD INDEX `FK_countryenvironmentdatabase_01` (`Country` ASC, `Environment` ASC) ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `countryenvironmentparameters` DROP FOREIGN KEY `FK_countryenvironmentparameters_1` , DROP FOREIGN KEY `FK_countryenvironmentparameters_3` ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `countryenvironmentparameters`   ADD CONSTRAINT `FK_countryenvironmentparameters_01`  FOREIGN KEY (`Country` , `Environment` )  REFERENCES `countryenvparam` (`Country` , `Environment` )  ON DELETE CASCADE  ON UPDATE CASCADE,   ADD CONSTRAINT `FK_countryenvironmentparameters_02`  FOREIGN KEY (`Application` )  REFERENCES `application` (`Application` )  ON DELETE CASCADE  ON UPDATE CASCADE, DROP INDEX `FK_countryenvironmentparameters_1` , ADD INDEX `FK_countryenvironmentparameters_01` (`Country` ASC, `Environment` ASC) , DROP INDEX `FK_countryenvironmentparameters_3` , ADD INDEX `FK_countryenvironmentparameters_02` (`Application` ASC) ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `countryenvparam_log` DROP FOREIGN KEY `FK_countryenvparam_log_1` ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `countryenvparam_log`   ADD CONSTRAINT `FK_countryenvparam_log_01`  FOREIGN KEY (`Country` , `Environment` )  REFERENCES `countryenvparam` (`Country` , `Environment` )  ON DELETE CASCADE  ON UPDATE CASCADE, DROP INDEX `FK_countryenvparam_log_1` , ADD INDEX `FK_countryenvparam_log_01` (`Country` ASC, `Environment` ASC) , DROP INDEX `ID1` ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `host` DROP FOREIGN KEY `FK_host_1` ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `host`   ADD CONSTRAINT `FK_host_01`  FOREIGN KEY (`Country` , `Environment` )  REFERENCES `countryenvparam` (`Country` , `Environment` )  ON DELETE CASCADE  ON UPDATE CASCADE, DROP INDEX `FK_host_1` , ADD INDEX `FK_host_01` (`Country` ASC, `Environment` ASC) ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `log` DROP INDEX `datecre` , ADD INDEX `IX_log_01` (`datecre` ASC) ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `test` DROP INDEX `ix_Test_Active` , ADD INDEX `IX_test_01` (`Test` ASC, `Active` ASC) ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcase` DROP INDEX `Index_2` , ADD INDEX `IX_testcase_01` (`Group` ASC) ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcase` DROP INDEX `Index_3` , ADD INDEX `IX_testcase_02` (`Test` ASC, `TestCase` ASC, `Application` ASC, `TcActive` ASC, `Group` ASC) ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcase` DROP INDEX `FK_testcase_2` , ADD INDEX `IX_testcase_03` (`Application` ASC) ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcase` DROP INDEX `FK_testcase_3` , ADD INDEX `IX_testcase_04` (`Project` ASC) ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcase` DROP FOREIGN KEY `FK_testcase_1` ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcase`   ADD CONSTRAINT `FK_testcase_01`  FOREIGN KEY (`Test` )  REFERENCES `test` (`Test` )  ON DELETE CASCADE  ON UPDATE CASCADE;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE testcase SET Application=null where Application='';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcase` DROP FOREIGN KEY `FK_testcase_2` ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcase`   ADD CONSTRAINT `FK_testcase_02`  FOREIGN KEY (`Application` )  REFERENCES `application` (`Application` )  ON DELETE CASCADE  ON UPDATE CASCADE;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcase` DROP FOREIGN KEY `FK_testcase_3` ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcase`   ADD CONSTRAINT `FK_testcase_03`  FOREIGN KEY (`Project` )  REFERENCES `project` (`idproject` )  ON DELETE CASCADE  ON UPDATE CASCADE;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("DELETE FROM testcase USING testcase left outer join test ON testcase.test = test.test where test.test is null;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasecountry` DROP FOREIGN KEY `FK_testcasecountry_1` ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasecountry`   ADD CONSTRAINT `FK_testcasecountry_01`  FOREIGN KEY (`Test` , `TestCase` )  REFERENCES `testcase` (`Test` , `TestCase` )  ON DELETE CASCADE  ON UPDATE CASCADE;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasecountryproperties` DROP FOREIGN KEY `FK_testcasecountryproperties_1` ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasecountryproperties`   ADD CONSTRAINT `FK_testcasecountryproperties_01`  FOREIGN KEY (`Test` , `TestCase` , `Country` )  REFERENCES `testcasecountry` (`Test` , `TestCase` , `Country` )  ON DELETE CASCADE  ON UPDATE CASCADE;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcaseexecution` DROP FOREIGN KEY `FK_testcaseexecution_1` ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcaseexecution`   ADD CONSTRAINT `FK_testcaseexecution_01`  FOREIGN KEY (`Test` , `TestCase` )  REFERENCES `testcase` (`Test` , `TestCase` )  ON DELETE CASCADE  ON UPDATE CASCADE;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcaseexecution` DROP FOREIGN KEY `FK_testcaseexecution_3` ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcaseexecution`   ADD CONSTRAINT `FK_testcaseexecution_02`  FOREIGN KEY (`application`)  REFERENCES `application` (`application`)  ON DELETE CASCADE  ON UPDATE CASCADE;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcaseexecution` DROP INDEX `FK_TestCaseExecution_1` , ADD INDEX `IX_testcaseexecution_01` (`Test` ASC, `TestCase` ASC) ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcaseexecution` DROP INDEX `fk_testcaseexecution_2` , ADD INDEX `IX_testcaseexecution_02` (`Tag` ASC) , DROP INDEX `index_1` , ADD INDEX `IX_testcaseexecution_03` (`Start` ASC) , DROP INDEX `IX_test_testcase_country` , ADD INDEX `IX_testcaseexecution_04` (`Test` ASC, `TestCase` ASC, `Country` ASC, `Start` ASC, `ControlStatus` ASC) , DROP INDEX `index_buildrev` , ADD INDEX `IX_testcaseexecution_05` (`Build` ASC, `Revision` ASC) , DROP INDEX `fk_test` , ADD INDEX `IX_testcaseexecution_06` (`Test` ASC) , DROP INDEX `ix_TestcaseExecution` , ADD INDEX `IX_testcaseexecution_07` (`Test` ASC, `TestCase` ASC, `Build` ASC, `Revision` ASC, `Environment` ASC, `Country` ASC, `ID` ASC) , DROP INDEX `FK_testcaseexecution_3` , ADD INDEX `IX_testcaseexecution_08` (`Application` ASC) ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcaseexecutiondata` DROP INDEX `propertystart` , ADD INDEX `IX_testcaseexecutiondata_01` (`Property` ASC, `Start` ASC) , DROP INDEX `index_1` , ADD INDEX `IX_testcaseexecutiondata_02` (`Start` ASC) ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcaseexecutiondata` DROP FOREIGN KEY `FK_TestCaseExecutionData_1` ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcaseexecutiondata`   ADD CONSTRAINT `FK_testcaseexecutiondata_01`  FOREIGN KEY (`ID` )  REFERENCES `testcaseexecution` (`ID` )  ON DELETE CASCADE  ON UPDATE CASCADE;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcaseexecutionwwwdet` DROP FOREIGN KEY `FK_testcaseexecutionwwwdet_1` ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcaseexecutionwwwdet`   ADD CONSTRAINT `FK_testcaseexecutionwwwdet_01`  FOREIGN KEY (`ExecID` )  REFERENCES `testcaseexecution` (`ID` )  ON DELETE CASCADE  ON UPDATE CASCADE;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcaseexecutionwwwdet` DROP INDEX `FK_testcaseexecutionwwwdet_1` , ADD INDEX `FK_testcaseexecutionwwwdet_01` (`ExecID` ASC) ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcaseexecutionwwwsum` DROP FOREIGN KEY `FK_testcaseexecutionwwwsum_1` ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcaseexecutionwwwsum`   ADD CONSTRAINT `FK_testcaseexecutionwwwsum_01`  FOREIGN KEY (`ID` )  REFERENCES `testcaseexecution` (`ID` )  ON DELETE CASCADE ON UPDATE CASCADE, DROP INDEX `FK_testcaseexecutionwwwsum_1` ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestep` DROP FOREIGN KEY `FK_testcasestep_1` ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestep`   ADD CONSTRAINT `FK_testcasestep_01`  FOREIGN KEY (`Test` , `TestCase` )  REFERENCES `testcase` (`Test` , `TestCase` )  ON DELETE CASCADE  ON UPDATE CASCADE;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("  ALTER TABLE `testcasestepaction` DROP FOREIGN KEY `FK_testcasestepaction_1` ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepaction`   ADD CONSTRAINT `FK_testcasestepaction_01`  FOREIGN KEY (`Test` , `TestCase` , `Step` )  REFERENCES `testcasestep` (`Test` , `TestCase` , `Step` )  ON DELETE CASCADE  ON UPDATE CASCADE;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepactioncontrol` DROP FOREIGN KEY `FK_testcasestepcontrol_1` ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepactioncontrol`   ADD CONSTRAINT `FK_testcasestepactioncontrol_01`  FOREIGN KEY (`Test` , `TestCase` , `Step` , `Sequence` )  REFERENCES `testcasestepaction` (`Test` , `TestCase` , `Step` , `Sequence` )  ON DELETE CASCADE  ON UPDATE CASCADE, DROP INDEX `FK_testcasestepcontrol_1` ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepactioncontrolexecution` DROP FOREIGN KEY `FK_testcasestepcontrolexecution_1` ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepactioncontrolexecution`   ADD CONSTRAINT `FK_testcasestepactioncontrolexecution_01`  FOREIGN KEY (`ID` , `Step` )  REFERENCES `testcasestepexecution` (`ID` , `Step` )  ON DELETE CASCADE  ON UPDATE CASCADE;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepbatch` DROP FOREIGN KEY `FK_testcasestepbatchl_1` ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepbatch`   ADD CONSTRAINT `FK_testcasestepbatch_01`  FOREIGN KEY (`Test` , `TestCase` )  REFERENCES `testcase` (`Test` , `TestCase` )  ON DELETE CASCADE  ON UPDATE CASCADE;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepbatch` DROP FOREIGN KEY `FK_testcasestepbatch_2` ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepbatch`   ADD CONSTRAINT `FK_testcasestepbatch_02`  FOREIGN KEY (`Batch` )  REFERENCES `batchinvariant` (`Batch` )  ON DELETE CASCADE  ON UPDATE CASCADE;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepbatch` DROP INDEX `fk_testcasestepbatch_1` , ADD INDEX `FK_testcasestepbatch_02` (`Batch` ASC) ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepbatch` DROP INDEX `FK_testcasestepbatch_02` , ADD INDEX `IX_testcasestepbatch_01` (`Batch` ASC) ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("DELETE FROM testcasestepexecution, testcaseexecution USING testcasestepexecution left outer join testcaseexecution  ON testcasestepexecution.ID = testcaseexecution.ID where testcaseexecution.ID is null;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepexecution` DROP FOREIGN KEY `FK_testcasestepexecution_1` ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepexecution`   ADD CONSTRAINT `FK_testcasestepexecution_01`  FOREIGN KEY (`ID` )  REFERENCES `testcaseexecution` (`ID` )  ON DELETE CASCADE  ON UPDATE CASCADE;  ");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `user` DROP INDEX `ID1` , ADD UNIQUE INDEX `IX_user_01` (`Login` ASC) ;");
        SQLInstruction.add(SQLS.toString());

//-- New CA Status in invariant and documentation table.
//-- ------------------------
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`) VALUES ('TCESTATUS', 'CA', 6, 35, 'Test could not be done because of technical issues.');");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

//-- New Cerberus Message store at the level of the execution - Header, Action and Control Level.
//-- ------------------------
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcaseexecution` ADD COLUMN `ControlMessage` VARCHAR(500) NULL DEFAULT NULL  AFTER `ControlStatus` ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepactioncontrolexecution` ADD COLUMN `ReturnMessage` VARCHAR(500) NULL DEFAULT NULL  AFTER `ReturnCode` ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepactionexecution` ADD COLUMN `ReturnCode` VARCHAR(2) NULL DEFAULT NULL  AFTER `Sequence` , ADD COLUMN `ReturnMessage` VARCHAR(500) NULL DEFAULT NULL  AFTER `ReturnCode` ;");
        SQLInstruction.add(SQLS.toString());

//-- New Integrity Link inside between User Group and User table.
//-- ------------------------
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `usergroup` ADD CONSTRAINT `FK_usergroup_01`  FOREIGN KEY (`Login` )  REFERENCES `user` (`Login` )  ON DELETE CASCADE ON UPDATE CASCADE;");
        SQLInstruction.add(SQLS.toString());

//-- New Parameter for Performance Monitoring Servlet.
//-- ------------------------
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `parameter` (`param`, `value`, `description`) VALUES ('cerberus_performancemonitor_nbminutes', '5', 'Integer that correspond to the number of minutes where the number of executions are collected on the servlet that manage the monitoring of the executions.');");
        SQLInstruction.add(SQLS.toString());

        //-- New Parameter for link to selenium extensions firebug and netexport.
        //-- -------------------------
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `parameter` (`param`, `value`, `description`) VALUES ('cerberus_selenium_firefoxextension_firebug', 'D:\\\\CerberusDocuments\\\\firebug-fx.xpi', 'Link to the firefox extension FIREBUG file needed to track network traffic')");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `parameter` (`param`, `value`, `description`) VALUES ('cerberus_selenium_firefoxextension_netexport', 'D:\\\\CerberusDocuments\\\\netExport.xpi', 'Link to the firefox extension NETEXPORT file needed to export network traffic')");
        SQLInstruction.add(SQLS.toString());

        //-- New Invariant Browser to feed combobox.
        //-- -------------------------
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`) VALUES ('BROWSER', 'FIREFOX', 1, 37, 'Firefox Browser')");
        SQLInstruction.add(SQLS.toString());

//-- Removing Performance Monitoring Servlet Parameter as it has been moved to the call of the URL. The number of minutes cannot be the same accross all requests.
//-- ------------------------
        SQLS = new StringBuilder();
        SQLS.append("DELETE FROM `parameter` WHERE `param`='cerberus_performancemonitor_nbminutes';");
        SQLInstruction.add(SQLS.toString());

//-- Cleaning invariant table in idname STATUS idname was used twice on 2 invariant group 1 and 33.
//-- ------------------------
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `invariant` SET `idname`='TCSTATUS' WHERE `id`='1' and`sort`='1';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `invariant` SET `idname`='TCSTATUS' WHERE `id`='1' and`sort`='2';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `invariant` SET `idname`='TCSTATUS' WHERE `id`='1' and`sort`='3';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `invariant` SET `idname`='TCSTATUS' WHERE `id`='1' and`sort`='4';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `invariant` SET `idname`='TCSTATUS' WHERE `id`='1' and`sort`='5';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `invariant` SET `idname`='TCSTATUS' WHERE `id`='1' and`sort`='6';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `invariant` SET `idname`='NCONFSTATUS' WHERE `id`='33' and`sort`='1';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `invariant` SET `idname`='NCONFSTATUS' WHERE `id`='33' and`sort`='2';");
        SQLInstruction.add(SQLS.toString());

//-- New invariant for execution detail list page.
//-- ------------------------
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`) VALUES ('EXECNBMIN', '5', 10, 38, '5 Minutes');");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`) VALUES ('EXECNBMIN', '10', 20, 38, '10 Minutes');");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`) VALUES ('EXECNBMIN', '15', 30, 38, '15 Minutes');");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`) VALUES ('EXECNBMIN', '20', 40, 38, '20 Minutes');");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`) VALUES ('EXECNBMIN', '30', 50, 38, '30 Minutes');");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`) VALUES ('EXECNBMIN', '45', 60, 38, '45 Minutes');");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`) VALUES ('EXECNBMIN', '60', 70, 38, '1 Hour');");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`) VALUES ('EXECNBMIN', '90', 80, 38, '1 Hour 30 Minutes');");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`) VALUES ('EXECNBMIN', '120', 90, 38, '2 Hours');");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`) VALUES ('EXECNBMIN', '180', 100, 38, '3 Hours');");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`) VALUES ('EXECNBMIN', '0', 1, 38, 'No Limit');");
        SQLInstruction.add(SQLS.toString());

//-- New Cerberus Message store at the level of the execution - Header, Action and Control Level.
//-- ------------------------
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

//-- New Cerberus Action  mouseOver and  mouseOverAndWait and remove of URLLOGIN, verifyTextPresent verifyTitle, verifyValue
//-- ------------------------
        SQLS = new StringBuilder();
        SQLS.append("DELETE FROM `invariant` WHERE `id`='12' and`sort`='120'");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("DELETE FROM `invariant` WHERE `id`='12' and`sort`='130'");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("DELETE FROM `invariant` WHERE `id`='12' and`sort`='140'");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("DELETE FROM `invariant` WHERE `id`='12' and`sort`='150'");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`) VALUES ('ACTION', 'mouseOver', 57, 12, 'mouseOver')");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`) VALUES ('ACTION', 'mouseOverAndWait', 58, 12, 'mouseOverAndWait')");
        SQLInstruction.add(SQLS.toString());

//-- New Documentation for verbose and status on the execution table.
//-- ------------------------
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

//-- New DefaultSystem and Team inside User table.
//-- ------------------------
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `user` ADD COLUMN `Team` VARCHAR(45) NULL  AFTER `Name` , ADD COLUMN `DefaultSystem` VARCHAR(45) NULL  AFTER `DefaultIP` , CHANGE COLUMN `Request` `Request` VARCHAR(5) NULL DEFAULT NULL  AFTER `Password` ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

//-- Documentation updated on verbose and added on screenshot option.
//-- ------------------------
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

//-- Screenshot invariant values.
//-- ------------------------
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`)");
        SQLS.append(" VALUES ('SCREENSHOT', '0', 10, 39, 'No Screenshot')");
        SQLS.append(",('SCREENSHOT', '1', 20, 39, 'Screenshot on error')");
        SQLS.append(",('SCREENSHOT', '2', 30, 39, 'Screenshot on every action');");
        SQLInstruction.add(SQLS.toString());

//-- Added Test and testcase columns to Action/control/step Execution tables.
//-- Added RC and RCMessage to all execution tables + Property Data table.
//-- ------------------------
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcaseexecutiondata` ADD COLUMN `RMessage` VARCHAR(500) NULL DEFAULT ''  AFTER `RC` ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepactioncontrolexecution` DROP FOREIGN KEY `FK_testcasestepactioncontrolexecution_01`;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepexecution` CHANGE COLUMN `Test` `Test` VARCHAR(45) NOT NULL DEFAULT '' , CHANGE COLUMN `TestCase` `TestCase` VARCHAR(45) NOT NULL DEFAULT '' ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepexecution` CHANGE COLUMN `ID` `ID` BIGINT(20) UNSIGNED NOT NULL  , CHANGE COLUMN `Step` `Step` INT(10) UNSIGNED NOT NULL  ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepexecution` DROP FOREIGN KEY `FK_testcasestepexecution_01`;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepexecution` ADD CONSTRAINT `FK_testcasestepexecution_01`   FOREIGN KEY (`ID` ) REFERENCES `testcaseexecution` (`ID` ) ON DELETE CASCADE ON UPDATE CASCADE ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepexecution` DROP PRIMARY KEY , ADD PRIMARY KEY (`ID`, `Test`, `TestCase`, `Step`) ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepactioncontrolexecution` CHANGE COLUMN `Test` `Test` VARCHAR(45) NOT NULL DEFAULT '' , CHANGE COLUMN `TestCase` `TestCase` VARCHAR(45) NOT NULL DEFAULT '' ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepactioncontrolexecution`  DROP PRIMARY KEY , ADD PRIMARY KEY (`ID`, `Test`, `TestCase`, `Step`, `Sequence`, `Control`) ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepactioncontrolexecution` ADD CONSTRAINT `FK_testcasestepactioncontrolexecution_01` FOREIGN KEY (`ID` , `Test` , `TestCase` , `Step` ) REFERENCES `testcasestepexecution` (`ID` , `Test` , `TestCase` , `Step` ) ON DELETE CASCADE ON UPDATE CASCADE ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepactionexecution` CHANGE COLUMN `ID` `ID` BIGINT(20) UNSIGNED NOT NULL  , CHANGE COLUMN `Test` `Test` VARCHAR(45) NOT NULL DEFAULT '' , CHANGE COLUMN `TestCase` `TestCase` VARCHAR(45) NOT NULL DEFAULT '' , CHANGE COLUMN `Step` `Step` INT(10) UNSIGNED NOT NULL ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `testcasestepactionexecution` SET Sequence=51 WHERE Step=0 and Sequence=50 and Action='Wait';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepactionexecution`  DROP PRIMARY KEY , ADD PRIMARY KEY (`ID`, `Test`, `TestCase`, `Step`, `Sequence`) ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("DELETE FROM testcasestepactionexecution WHERE ID in ( SELECT ID FROM ( SELECT a.ID FROM testcasestepactionexecution a LEFT OUTER JOIN testcasestepexecution b ON a.ID=b.ID and a.Test=b.Test and a.TestCase=b.TestCase and a.Step=b.Step WHERE b.ID is null) as toto);");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepactionexecution` ADD CONSTRAINT `FK_testcasestepactionexecution_01` FOREIGN KEY (`ID` , `Test` , `TestCase` , `Step` ) REFERENCES `testcasestepexecution` (`ID` , `Test` , `TestCase` , `Step` ) ON DELETE CASCADE ON UPDATE CASCADE;");
        SQLInstruction.add(SQLS.toString());

//-- Resizing Screenshot filename to biggest possible value. 
//-- ------------------------
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepactioncontrolexecution` CHANGE COLUMN `ScreenshotFilename` `ScreenshotFilename` VARCHAR(150) NULL DEFAULT NULL  ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepactionexecution` CHANGE COLUMN `ScreenshotFilename` `ScreenshotFilename` VARCHAR(150) NULL DEFAULT NULL  ;");
        SQLInstruction.add(SQLS.toString());

//-- Correcting verifyurl to verifyURL and verifytitle to VerifyTitle in controls. 
//-- ------------------------
        SQLS = new StringBuilder();
        SQLS.append("UPDATE testcasestepactioncontrol SET type='verifyUrl' where type='verifyurl';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE testcasestepactioncontrol SET type='verifyTitle' where type='verifytitle';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE invariant SET value='verifyUrl', description ='verifyUrl' where value='verifyurl' and idname='CONTROL';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE invariant SET value='verifyTitle', description ='verifyTitle' where value='verifytitle' and idname='CONTROL';");
        SQLInstruction.add(SQLS.toString());

//-- Making controls standard. 
//-- ------------------------
        SQLS = new StringBuilder();
        SQLS.append("UPDATE invariant SET value = 'verifyPropertyEqual', description = 'verifyPropertyEqual' where value='PropertyIsEqualTo' and idname='CONTROL';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE testcasestepactioncontrol SET type='verifyPropertyEqual' where type='PropertyIsEqualTo';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE invariant SET value = 'verifyPropertyGreater', description = 'verifyPropertyGreater' where value='PropertyIsGreaterThan' and idname='CONTROL';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE testcasestepactioncontrol SET type='verifyPropertyGreater' where type='PropertyIsGreaterThan';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE invariant SET value = 'verifyPropertyMinor', description = 'verifyPropertyMinor' where value='PropertyIsMinorThan' and idname='CONTROL';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE testcasestepactioncontrol SET type='verifyPropertyMinor' where type='PropertyIsMinorThan';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`) VALUES ('CONTROL', 'verifyPropertyDifferent', 11, 13, 'verifyPropertyDifferent');");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`) VALUES ('CONTROL', 'verifyElementNotPresent', 21, 13, 'verifyElementNotPresent');");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`) VALUES ('ACTION', 'openUrlLogin', 61, 12, 'openUrlLogin');");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE testcasestepaction SET action='openUrlLogin' where action='URLLOGIN';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `invariant` SET `value`='firefox' WHERE `id`='37' and`sort`='1';");
        SQLInstruction.add(SQLS.toString());

//-- New parameter used by netexport. 
//-- ------------------------
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `parameter` (`param`, `value`, `description`) VALUES ('cerberus_url', 'http://localhost:8080/GuiCerberusV2-2.0.0-SNAPSHOT', 'URL to Cerberus used in order to call back cerberus from NetExport plugin. This parameter is mandatory for saving the firebug detail information back to cerberus. ex : http://host:port/contextroot');");
        SQLInstruction.add(SQLS.toString());

//-- Making controls standard. 
//-- ------------------------
        SQLS = new StringBuilder();
        SQLS.append("UPDATE invariant SET value = 'verifyStringEqual', description = 'verifyStringEqual' where value='verifyPropertyEqual' and idname='CONTROL';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE testcasestepactioncontrol SET type='verifyStringEqual' where type='verifyPropertyEqual';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE invariant SET value = 'verifyStringDifferent', description = 'verifyStringDifferent' where value='verifyPropertyDifferent' and idname='CONTROL';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE testcasestepactioncontrol SET type='verifyStringDifferent' where type='verifyPropertyDifferent';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE invariant SET value = 'verifyIntegerGreater', description = 'verifyIntegerGreater' where value='verifyPropertyGreater' and idname='CONTROL';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE testcasestepactioncontrol SET type='verifyIntegerGreater' where type='verifyPropertyGreater';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE invariant SET value = 'verifyIntegerMinor', description = 'verifyIntegerMinor' where value='verifyPropertyMinor' and idname='CONTROL';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE testcasestepactioncontrol SET type='verifyIntegerMinor' where type='verifyPropertyMinor';");
        SQLInstruction.add(SQLS.toString());

//-- Making Properties standard. 
//-- ------------------------
        SQLS = new StringBuilder();
        SQLS.append("UPDATE invariant SET value = 'executeSql', sort=20 where value='SQL' and idname='PROPERTYTYPE';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE invariant SET value = 'executeSqlFromLib', sort=25 where value='LIB_SQL' and idname='PROPERTYTYPE';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE invariant SET value = 'getFromHtmlVisible', sort=35, description='Getting from an HTML visible field in the current page.' where value='HTML' and idname='PROPERTYTYPE';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE invariant SET value = 'text', sort=40 where value='TEXT' and idname='PROPERTYTYPE';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`) VALUES ('PROPERTYTYPE', 'getFromHtml', 30, 19, 'Getting from an html field in the current page.');");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE testcasecountryproperties SET type='text' where type='TEXT';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE testcasecountryproperties SET type='executeSqlFromLib' where type='LIB_SQL';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE testcasecountryproperties SET type='executeSql' where type='SQL';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE testcasecountryproperties SET type='getFromHtmlVisible' where type='HTML';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`) VALUES ('PROPERTYNATURE', 'NOTINUSE', 4, 20, 'Not In Use');");
        SQLInstruction.add(SQLS.toString());

//-- New Control. 
//-- ------------------------
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`) VALUES ('CONTROL', 'verifyTextNotPresent', 51, 13, 'verifyTextNotPresent');");
        SQLInstruction.add(SQLS.toString());

//-- Team and system invariant initialisation.
//-- ------------------------
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`) ");
        SQLS.append(" VALUES ('TEAM', 'France', 10, 40, 'France Team'),");
        SQLS.append("  ('TEAM', 'Portugal', 20, 40, 'Portugal Team'),");
        SQLS.append("  ('SYSTEM', 'DEFAULT', 10, 41, 'System1 System'),");
        SQLS.append("  ('SYSTEM', 'SYS2', 20, 41, 'System2 System')");
        SQLInstruction.add(SQLS.toString());

//-- Changing Request column inside user table to fit boolean management standard.
//-- ------------------------
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `user` SET Request='Y' where Request='true';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `user` SET Request='N' where Request='false';");
        SQLInstruction.add(SQLS.toString());

//-- Cleaning comparaison status tables.
//-- ------------------------
        SQLS = new StringBuilder();
        SQLS.append("DROP TABLE `comparisonstatusdata`;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("DROP TABLE `comparisonstatus`;");
        SQLInstruction.add(SQLS.toString());

//-- Documentation on application table.
//-- ------------------------
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

//-- Log Event table redesign.
//-- ------------------------
        SQLS = new StringBuilder();
        SQLS.append("DROP TABLE `logeventchange`; ");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `logevent` ADD COLUMN `Login` VARCHAR(30) NOT NULL DEFAULT '' AFTER `UserID`, CHANGE COLUMN `Time` `Time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, ADD COLUMN `remoteIP` VARCHAR(20) NULL DEFAULT NULL  AFTER `Log` , ADD COLUMN `localIP` VARCHAR(20) NULL DEFAULT NULL  AFTER `remoteIP`;");
        SQLInstruction.add(SQLS.toString());

//-- User group definition
//-- ------------------------
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`, `gp1`, `gp2`, `gp3`) VALUES ");
        SQLS.append("('USERGROUP', 'Visitor', 5, 42, 'Visitor', null, null, null),");
        SQLS.append("('USERGROUP', 'Integrator', 10, 42, 'Integrator', null, null, null),");
        SQLS.append("('USERGROUP', 'User', 15, 42, 'User', null, null, null),");
        SQLS.append("('USERGROUP', 'Admin', 20, 42, 'Admin', null, null, null)");
        SQLInstruction.add(SQLS.toString());

//-- New Column for Bug Tracking.
//-- ------------------------
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `application` ADD COLUMN `BugTrackerUrl` VARCHAR(300) NULL DEFAULT ''  AFTER `svnurl` , ADD COLUMN `BugTrackerNewUrl` VARCHAR(300) NULL DEFAULT ''  AFTER `BugTrackerUrl` ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`, `gp1`, `gp2`, `gp3`) VALUES ");
        SQLS.append("('APPLITYPE', 'GUI', 5, 43, 'GUI application', null, null, null),");
        SQLS.append("('APPLITYPE', 'BAT', 10, 43, 'Batch Application', null, null, null),");
        SQLS.append("('APPLITYPE', 'SRV', 15, 43, 'Service Application', null, null, null),");
        SQLS.append("('APPLITYPE', 'NONE', 20, 43, 'Any Other Type of application', null, null, null)");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE application SET deploytype=null where deploytype is null;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("DELETE FROM `parameter` WHERE `param`='sitdmoss_bugtracking_url';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("DELETE FROM `parameter` WHERE `param`='sitdmoss_newbugtracking_url';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("DELETE FROM `parameter` WHERE `param`='cerberus_selenium_plugins_path';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("DELETE FROM `parameter` WHERE `param`='svn_application_url';");
        SQLInstruction.add(SQLS.toString());

//-- New Controls for string comparaison.
//-- ------------------------
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `invariant` SET `sort`=16 WHERE `id`='13' and`sort`='12';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `invariant` SET `sort`=17 WHERE `id`='13' and`sort`='14';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`) VALUES ");
        SQLS.append("  ('CONTROL', 'verifyStringGreater', 12, 13, 'verifyStringGreater')");
        SQLS.append(" ,('CONTROL', 'verifyStringMinor', 13, 13, 'verifyStringMinor');");
        SQLInstruction.add(SQLS.toString());

//-- Cleaning on TextInPage control.
//-- ------------------------
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `invariant` SET `value`='verifyTextInPage', `description`='verifyTextInPage' WHERE `id`='13' and`sort`='50';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `testcasestepactioncontrol` SET `type`='verifyTextInPage' WHERE `type`='verifyTextPresent';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `invariant` SET `value`='verifyTextNotInPage', `description`='verifyTextNotInPage' WHERE `id`='13' and`sort`='51';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `testcasestepactioncontrol` SET `type`='verifyTextNotInPage' WHERE `type`='verifyTextNotPresent';");
        SQLInstruction.add(SQLS.toString());

//-- Cleaning on VerifyText --> VerifyTextInElement control.
//-- ------------------------
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `invariant` SET `value`='verifyTextInElement', `description`='verifyTextInElement' WHERE `id`='13' and`sort`='40';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `testcasestepactioncontrol` SET `type`='verifyTextInElement' WHERE `type`='VerifyText';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `invariant` SET `value`='verifyRegexInElement', `description`='verifyRegexInElement', sort='43' WHERE `id`='13' and`sort`='80';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `testcasestepactioncontrol` SET `type`='verifyRegexInElement' WHERE `type`='verifyContainText';");
        SQLInstruction.add(SQLS.toString());

//-- Enlarging BehaviorOrValueExpected and HowTo columns to TEXT (64K).
//-- ------------------------
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcase` CHANGE COLUMN `BehaviorOrValueExpected` `BehaviorOrValueExpected` TEXT NULL , CHANGE COLUMN `HowTo` `HowTo` TEXT NULL ;");
        SQLInstruction.add(SQLS.toString());

//-- Change length of Property column of TestCaseStepActionExecution from 45 to 200
//-- ------------------------
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE testcasestepactionexecution CHANGE Property Property varchar(200);");
        SQLInstruction.add(SQLS.toString());

//-- Add invariant LANGUAGE
//-- ------------------------
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`, `gp1`) VALUES ");
        SQLS.append("  ('LANGUAGE', '', 1, 44, 'Default language', 'en')");
        SQLS.append(" ,('LANGUAGE', 'BE', 5, 44, 'Belgium language', 'fr-be')");
        SQLS.append(" ,('LANGUAGE', 'CH', 10, 44, 'Switzerland language', 'fr-ch')");
        SQLS.append(" ,('LANGUAGE', 'ES', 15, 44, 'Spain language', 'es')");
        SQLS.append(" ,('LANGUAGE', 'FR', 20, 44, 'France language', 'fr')");
        SQLS.append(" ,('LANGUAGE', 'IT', 25, 44, 'Italy language', 'it')");
        SQLS.append(" ,('LANGUAGE', 'PT', 30, 44, 'Portugal language', 'pt')");
        SQLS.append(" ,('LANGUAGE', 'RU', 35, 44, 'Russia language', 'ru')");
        SQLS.append(" ,('LANGUAGE', 'UK', 40, 44, 'Great Britain language', 'gb')");
        SQLS.append(" ,('LANGUAGE', 'VI', 45, 44, 'Generic language', 'en');");
        SQLInstruction.add(SQLS.toString());

//-- Cerberus can't find elements inside iframe
//-- ------------------------
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`) VALUES ");
        SQLS.append("('ACTION','focusToIframe',52,12,'focusToIframe'),");
        SQLS.append("('ACTION','focusDefaultIframe',53,12,'focusDefaultIframe');");
        SQLInstruction.add(SQLS.toString());

//-- Documentation on new Bug URL
//-- ------------------------
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

//-- Harmonize the column order of Country/Environment.
//-- ------------------------
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `countryenvironmentdatabase` ");
        SQLS.append(" CHANGE COLUMN `Country` `Country` VARCHAR(2) NOT NULL  FIRST , ");
        SQLS.append(" CHANGE COLUMN `Environment` `Environment` VARCHAR(45) NOT NULL  AFTER `Country` , ");
        SQLS.append(" DROP PRIMARY KEY , ADD PRIMARY KEY (`Country`, `Environment`, `Database`) ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `host` ");
        SQLS.append(" CHANGE COLUMN `Environment` `Environment` VARCHAR(45) NOT NULL  AFTER `Country` , ");
        SQLS.append(" DROP PRIMARY KEY , ADD PRIMARY KEY USING BTREE (`Country`, `Environment`, `Session`, `Server`) ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `buildrevisionbatch` ");
        SQLS.append(" CHANGE COLUMN `Environment` `Environment` VARCHAR(45) NULL DEFAULT NULL  AFTER `Country` ;");
        SQLInstruction.add(SQLS.toString());

//-- Change invariant LANGUAGE to GP2 of invariant COUNTRY
//-- ------------------------
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `invariant` SET gp2 = 'fr-be' WHERE idname = 'COUNTRY' and value = 'BE';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `invariant` SET gp2 = 'fr-ch' WHERE idname = 'COUNTRY' and value = 'CH';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `invariant` SET gp2 = 'es' WHERE idname = 'COUNTRY' and value = 'ES';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `invariant` SET gp2 = 'it' WHERE idname = 'COUNTRY' and value = 'IT';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `invariant` SET gp2 = 'pt-pt' WHERE idname = 'COUNTRY' and value = 'PT';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `invariant` SET gp2 = 'ru' WHERE idname = 'COUNTRY' and value = 'RU';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `invariant` SET gp2 = 'en-gb' WHERE idname = 'COUNTRY' and value = 'UK';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `invariant` SET gp2 = 'en-gb' WHERE idname = 'COUNTRY' and value = 'VI';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `invariant` SET gp2 = 'ru' WHERE idname = 'COUNTRY' and value = 'RU';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `invariant` SET gp2 = 'fr' WHERE idname = 'COUNTRY' and value = 'FR';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `invariant` SET gp2 = 'en-gb' WHERE idname = 'COUNTRY' and value = 'RX';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("DELETE FROM `invariant` WHERE idname = 'LANGUAGE'");
        SQLInstruction.add(SQLS.toString());

//-- Cleaning countryenvironmentparameters table with useless columns
//-- ------------------------
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `countryenvironmentparameters` DROP COLUMN `as400LIB` , DROP COLUMN `JdbcPort` , DROP COLUMN `JdbcIP` , DROP COLUMN `JdbcPass` , DROP COLUMN `JdbcUser` ;");
        SQLInstruction.add(SQLS.toString());

//-- Adding System level in database model.
//-- ------------------------
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `countryenvparam` ADD COLUMN `system` VARCHAR(45) NOT NULL DEFAULT ''  FIRST ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `countryenvdeploytype` ADD COLUMN `system` VARCHAR(45) NOT NULL DEFAULT '' FIRST ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `countryenvdeploytype` DROP FOREIGN KEY `FK_countryenvdeploytype_02` ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `countryenvironmentparameters` ADD COLUMN `system` VARCHAR(45) NOT NULL DEFAULT '' FIRST ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `countryenvironmentparameters` DROP FOREIGN KEY `FK_countryenvironmentparameters_01` ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `countryenvironmentdatabase` ADD COLUMN `system` VARCHAR(45) NOT NULL DEFAULT '' FIRST ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `countryenvironmentdatabase` DROP FOREIGN KEY `FK_countryenvironmentdatabase_01` ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `host` ADD COLUMN `system` VARCHAR(45) NOT NULL DEFAULT '' FIRST ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `host` DROP FOREIGN KEY `FK_host_01` ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `countryenvparam_log` ADD COLUMN `system` VARCHAR(45) NOT NULL DEFAULT ''  AFTER `id` ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `countryenvparam_log` DROP FOREIGN KEY `FK_countryenvparam_log_01` ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `countryenvparam_log` DROP INDEX `FK_countryenvparam_log_01` ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `buildrevisionbatch` ADD COLUMN `system` VARCHAR(45) NOT NULL DEFAULT ''  AFTER `Batch` ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `buildrevisionbatch` DROP FOREIGN KEY `FK_buildrevisionbatch_02` ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `buildrevisionbatch` DROP INDEX `FK_buildrevisionbatch_02` ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `countryenvparam`  DROP PRIMARY KEY , ADD PRIMARY KEY (`system`, `Country`, `Environment`) ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `countryenvdeploytype` DROP PRIMARY KEY , ADD PRIMARY KEY (`system`, `Country`, `Environment`, `deploytype`, `JenkinsAgent`) ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `countryenvdeploytype`");
        SQLS.append("  ADD CONSTRAINT `FK_countryenvdeploytype_1` FOREIGN KEY (`system` , `Country` , `Environment` )");
        SQLS.append("  REFERENCES `countryenvparam` (`system` , `Country` , `Environment` ) ON DELETE CASCADE ON UPDATE CASCADE;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `countryenvdeploytype` ");
        SQLS.append(" DROP INDEX `FK_countryenvdeploytype_01` , ADD INDEX `FK_countryenvdeploytype_01` (`system` ASC, `Country` ASC, `Environment` ASC) ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `countryenvdeploytype` DROP FOREIGN KEY `FK_countryenvdeploytype_01` ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `countryenvdeploytype` ");
        SQLS.append("  ADD CONSTRAINT `FK_countryenvdeploytype_02`");
        SQLS.append("  FOREIGN KEY (`deploytype` )");
        SQLS.append("  REFERENCES `deploytype` (`deploytype` )");
        SQLS.append("  ON DELETE CASCADE");
        SQLS.append("  ON UPDATE CASCADE;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `countryenvdeploytype` DROP FOREIGN KEY `FK_countryenvdeploytype_1` ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `countryenvdeploytype` ");
        SQLS.append("  ADD CONSTRAINT `FK_countryenvdeploytype_01`");
        SQLS.append("  FOREIGN KEY (`system` , `Country` , `Environment` )");
        SQLS.append("  REFERENCES `countryenvparam` (`system` , `Country` , `Environment` )");
        SQLS.append("  ON DELETE CASCADE");
        SQLS.append("  ON UPDATE CASCADE;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `countryenvironmentparameters` DROP PRIMARY KEY , ADD PRIMARY KEY (`system`, `Country`, `Environment`, `Application`) ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `countryenvironmentparameters` ");
        SQLS.append("  ADD CONSTRAINT `FK_countryenvironmentparameters_01` FOREIGN KEY (`system` , `Country` , `Environment` )");
        SQLS.append("  REFERENCES `countryenvparam` (`system` , `Country` , `Environment` ) ON DELETE CASCADE ON UPDATE CASCADE;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `countryenvironmentparameters` ");
        SQLS.append("DROP INDEX `FK_countryenvironmentparameters_01` , ADD INDEX `FK_countryenvironmentparameters_01` (`system` ASC, `Country` ASC, `Environment` ASC) ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `buildrevisionbatch` ");
        SQLS.append("  ADD CONSTRAINT `FK_buildrevisionbatch_02`");
        SQLS.append("  FOREIGN KEY (`system` , `Country` , `Environment` )");
        SQLS.append("  REFERENCES `countryenvparam` (`system` , `Country` , `Environment` )");
        SQLS.append("  ON DELETE CASCADE   ON UPDATE CASCADE");
        SQLS.append(", ADD INDEX `FK_buildrevisionbatch_02` (`system` ASC, `Country` ASC, `Environment` ASC) ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `countryenvironmentdatabase` ");
        SQLS.append("DROP PRIMARY KEY , ADD PRIMARY KEY (`system`, `Country`, `Environment`, `Database`) ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `countryenvironmentdatabase` ");
        SQLS.append("  ADD CONSTRAINT `FK_countryenvironmentdatabase_01`");
        SQLS.append("  FOREIGN KEY (`system` , `Country` , `Environment` )");
        SQLS.append("  REFERENCES `countryenvparam` (`system` , `Country` , `Environment` )");
        SQLS.append("  ON DELETE CASCADE ON UPDATE CASCADE");
        SQLS.append(", DROP INDEX  `FK_countryenvironmentdatabase_01` , ADD INDEX `FK_countryenvironmentdatabase_01` (`system` ASC, `Country` ASC, `Environment` ASC) ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `host` ");
        SQLS.append("DROP PRIMARY KEY , ADD PRIMARY KEY (`system`, `Country`, `Environment`, `Session`, `Server`) ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `host` ");
        SQLS.append("DROP INDEX `FK_host_01` , ADD INDEX `FK_host_01` (`system` ASC, `Country` ASC, `Environment` ASC) ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `host` ");
        SQLS.append("  ADD CONSTRAINT `FK_host_01`");
        SQLS.append("  FOREIGN KEY (`system` , `Country` , `Environment` )");
        SQLS.append("  REFERENCES `countryenvparam` (`system` , `Country` , `Environment` )");
        SQLS.append("  ON DELETE CASCADE ON UPDATE CASCADE ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `countryenvparam_log` ");
        SQLS.append("  ADD CONSTRAINT `FK_countryenvparam_log_01`");
        SQLS.append("  FOREIGN KEY (`system` , `Country` , `Environment` )");
        SQLS.append("  REFERENCES `countryenvparam` (`system` , `Country` , `Environment` )");
        SQLS.append("  ON DELETE CASCADE ON UPDATE CASCADE");
        SQLS.append(", ADD INDEX `FK_countryenvparam_log_01` (`system` ASC, `Country` ASC, `Environment` ASC) ;");
        SQLInstruction.add(SQLS.toString());

//-- Enlarge data execution column in order to keep track of full SQL executed.
//-- ------------------------
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcaseexecutiondata` CHANGE COLUMN `Value` `Value` VARCHAR(3000) NOT NULL  , CHANGE COLUMN `RMessage` `RMessage` VARCHAR(3000) NULL DEFAULT ''  ;");
        SQLInstruction.add(SQLS.toString());

//-- Insert default environment in order to get examples running.
//-- ------------------------
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `countryenvparam` (`system`, `Country`, `Environment`, `Build`, `Revision`, `Chain`, `DistribList`, `EMailBodyRevision`, `Type`, `EMailBodyChain`, `EMailBodyDisableEnvironment`, `active`, `maintenanceact`) VALUES ('DEFAULT', 'RX', 'PROD', '', '', '', '', '', 'STD', '', '', 'Y', 'N');");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `countryenvironmentparameters` (`system`, `Country`, `Environment`, `Application`, `IP`, `URL`, `URLLOGIN`) VALUES ('DEFAULT', 'RX', 'PROD', 'Google', 'www.google.com', '/', '');");
        SQLInstruction.add(SQLS.toString());

//-- Force default system to DEFAULT.
//-- ------------------------ 330
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `user` SET DefaultSystem='DEFAULT' where DefaultSystem is null;");
        SQLInstruction.add(SQLS.toString());

//-- ------------------------
//-- ------------------------
//-- Cerberus 0.9.0 Stops here.
//-- ------------------------
//-- ------------------------
//-- Database structure to handle link between environment and history of Build rev per system for each execution.
//-- ------------------------ 331
        SQLS = new StringBuilder();
        SQLS.append("CREATE  TABLE `testcaseexecutionsysver` (");
        SQLS.append("  `ID` BIGINT UNSIGNED NOT NULL ,");
        SQLS.append("  `system` VARCHAR(45) NOT NULL ,");
        SQLS.append("  `Build` VARCHAR(10) NULL ,");
        SQLS.append("  `Revision` VARCHAR(20) NULL ,");
        SQLS.append("  PRIMARY KEY (`ID`, `system`) ,");
        SQLS.append("  INDEX `FK_testcaseexecutionsysver_01` (`ID` ASC) ,");
        SQLS.append("  CONSTRAINT `FK_testcaseexecutionsysver_01`");
        SQLS.append("    FOREIGN KEY (`ID` )");
        SQLS.append("    REFERENCES `testcaseexecution` (`ID` )");
        SQLS.append("    ON DELETE CASCADE ON UPDATE CASCADE);");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("CREATE  TABLE `countryenvlink` (");
        SQLS.append("  `system` VARCHAR(45) NOT NULL DEFAULT '' ,");
        SQLS.append("  `Country` VARCHAR(2) NOT NULL ,");
        SQLS.append("  `Environment` VARCHAR(45) NOT NULL ,");
        SQLS.append("  `systemLink` VARCHAR(45) NOT NULL DEFAULT '' ,");
        SQLS.append("  `CountryLink` VARCHAR(2) NOT NULL ,");
        SQLS.append("  `EnvironmentLink` VARCHAR(45) NOT NULL ,");
        SQLS.append("  PRIMARY KEY (`system`, `Country`, `Environment`,`systemLink`, `CountryLink`, `EnvironmentLink`) ,");
        SQLS.append("  INDEX `FK_countryenvlink_01` (`system` ASC, `Country` ASC, `Environment` ASC) ,");
        SQLS.append("  INDEX `FK_countryenvlink_02` (`systemLink` ASC, `CountryLink` ASC, `EnvironmentLink` ASC) ,");
        SQLS.append("  CONSTRAINT `FK_countryenvlink_01`");
        SQLS.append("    FOREIGN KEY (`system` , `Country` , `Environment` )");
        SQLS.append("    REFERENCES `countryenvparam` (`system` , `Country` , `Environment` )");
        SQLS.append("    ON DELETE CASCADE ON UPDATE CASCADE,");
        SQLS.append("  CONSTRAINT `FK_countryenvlink_02`");
        SQLS.append("    FOREIGN KEY (`systemLink` , `CountryLink` , `EnvironmentLink` )");
        SQLS.append("    REFERENCES `countryenvparam` (`system` , `Country` , `Environment` )");
        SQLS.append("    ON DELETE CASCADE ON UPDATE CASCADE)  ENGINE=InnoDB DEFAULT CHARSET=utf8 ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `countryenvlink` ");
        SQLS.append("DROP PRIMARY KEY ");
        SQLS.append(", ADD PRIMARY KEY (`system`, `Country`, `Environment`, `systemLink`) ;");
        SQLInstruction.add(SQLS.toString());

//-- New Documentation on homepage.
//-- ------------------------ 334
        SQLS = new StringBuilder();
        SQLS.append("DELETE FROM `invariant` WHERE `id`='23' and`sort`='6';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

//-- Update System Variables %XXX% to %SYS_XXX%.
//-- ------------------------ 336
        SQLS = new StringBuilder();
        SQLS.append("UPDATE testcasestepactioncontrol ");
        SQLS.append("SET ControlValue=REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(ControlValue,\'%ENV%\',\'%SYS_ENV%\'),\'%ENVGP%\',\'%SYS_ENVGP%\'),\'%COUNTRY%\',\'%SYS_COUNTRY%\'),\'%COUNTRYGP1%\',\'%SYS_COUNTRYGP1%\'),\'%SSIP%\',\'%SYS_SSIP%\'),\'%SSPORT%\',\'%SYS_SSPORT%\'),\'%TAG%\',\'%SYS_TAG%\'),\'%TODAY-yyyy%\',\'%SYS_TODAY-yyyy%\'),\'%TODAY-MM%\',\'%SYS_TODAY-MM%\'),\'%TODAY-dd%\',\'%SYS_TODAY-dd%\'),\'%TODAY-HH%\',\'%SYS_TODAY-HH%\'),\'%TODAY-mm%\',\'%SYS_TODAY-mm%\'),\'%TODAY-ss%\',\'%SYS_TODAY-ss%\'),\'%YESTERDAY-yyyy%\',\'%SYS_YESTERDAY-yyyy%\'),\'%YESTERDAY-MM%\',\'%SYS_YESTERDAY-MM%\'),\'%YESTERDAY-dd%\',\'%SYS_YESTERDAY-dd%\'),\'%YESTERDAY-HH%\',\'%SYS_YESTERDAY-HH%\'),\'%YESTERDAY-mm%\',\'%SYS_YESTERDAY-mm%\'),\'%YESTERDAY-ss%\',\'%SYS_YESTERDAY-ss%\')");
        SQLS.append(", ControlProperty=REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(ControlProperty,\'%ENV%\',\'%SYS_ENV%\'),\'%ENVGP%\',\'%SYS_ENVGP%\'),\'%COUNTRY%\',\'%SYS_COUNTRY%\'),\'%COUNTRYGP1%\',\'%SYS_COUNTRYGP1%\'),\'%SSIP%\',\'%SYS_SSIP%\'),\'%SSPORT%\',\'%SYS_SSPORT%\'),\'%TAG%\',\'%SYS_TAG%\'),\'%TODAY-yyyy%\',\'%SYS_TODAY-yyyy%\'),\'%TODAY-MM%\',\'%SYS_TODAY-MM%\'),\'%TODAY-dd%\',\'%SYS_TODAY-dd%\'),\'%TODAY-HH%\',\'%SYS_TODAY-HH%\'),\'%TODAY-mm%\',\'%SYS_TODAY-mm%\'),\'%TODAY-ss%\',\'%SYS_TODAY-ss%\'),\'%YESTERDAY-yyyy%\',\'%SYS_YESTERDAY-yyyy%\'),\'%YESTERDAY-MM%\',\'%SYS_YESTERDAY-MM%\'),\'%YESTERDAY-dd%\',\'%SYS_YESTERDAY-dd%\'),\'%YESTERDAY-HH%\',\'%SYS_YESTERDAY-HH%\'),\'%YESTERDAY-mm%\',\'%SYS_YESTERDAY-mm%\'),\'%YESTERDAY-ss%\',\'%SYS_YESTERDAY-ss%\');");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE testcasestepaction ");
        SQLS.append("SET Object=REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(Object,\'%ENV%\',\'%SYS_ENV%\'),\'%ENVGP%\',\'%SYS_ENVGP%\'),\'%COUNTRY%\',\'%SYS_COUNTRY%\'),\'%COUNTRYGP1%\',\'%SYS_COUNTRYGP1%\'),\'%SSIP%\',\'%SYS_SSIP%\'),\'%SSPORT%\',\'%SYS_SSPORT%\'),\'%TAG%\',\'%SYS_TAG%\'),\'%TODAY-yyyy%\',\'%SYS_TODAY-yyyy%\'),\'%TODAY-MM%\',\'%SYS_TODAY-MM%\'),\'%TODAY-dd%\',\'%SYS_TODAY-dd%\'),\'%TODAY-HH%\',\'%SYS_TODAY-HH%\'),\'%TODAY-mm%\',\'%SYS_TODAY-mm%\'),\'%TODAY-ss%\',\'%SYS_TODAY-ss%\'),\'%YESTERDAY-yyyy%\',\'%SYS_YESTERDAY-yyyy%\'),\'%YESTERDAY-MM%\',\'%SYS_YESTERDAY-MM%\'),\'%YESTERDAY-dd%\',\'%SYS_YESTERDAY-dd%\'),\'%YESTERDAY-HH%\',\'%SYS_YESTERDAY-HH%\'),\'%YESTERDAY-mm%\',\'%SYS_YESTERDAY-mm%\'),\'%YESTERDAY-ss%\',\'%SYS_YESTERDAY-ss%\')");
        SQLS.append(", property=REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(property,\'%ENV%\',\'%SYS_ENV%\'),\'%ENVGP%\',\'%SYS_ENVGP%\'),\'%COUNTRY%\',\'%SYS_COUNTRY%\'),\'%COUNTRYGP1%\',\'%SYS_COUNTRYGP1%\'),\'%SSIP%\',\'%SYS_SSIP%\'),\'%SSPORT%\',\'%SYS_SSPORT%\'),\'%TAG%\',\'%SYS_TAG%\'),\'%TODAY-yyyy%\',\'%SYS_TODAY-yyyy%\'),\'%TODAY-MM%\',\'%SYS_TODAY-MM%\'),\'%TODAY-dd%\',\'%SYS_TODAY-dd%\'),\'%TODAY-HH%\',\'%SYS_TODAY-HH%\'),\'%TODAY-mm%\',\'%SYS_TODAY-mm%\'),\'%TODAY-ss%\',\'%SYS_TODAY-ss%\'),\'%YESTERDAY-yyyy%\',\'%SYS_YESTERDAY-yyyy%\'),\'%YESTERDAY-MM%\',\'%SYS_YESTERDAY-MM%\'),\'%YESTERDAY-dd%\',\'%SYS_YESTERDAY-dd%\'),\'%YESTERDAY-HH%\',\'%SYS_YESTERDAY-HH%\'),\'%YESTERDAY-mm%\',\'%SYS_YESTERDAY-mm%\'),\'%YESTERDAY-ss%\',\'%SYS_YESTERDAY-ss%\');");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE testcasecountryproperties ");
        SQLS.append("SET Value=REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(Value,\'%ENV%\',\'%SYS_ENV%\'),\'%ENVGP%\',\'%SYS_ENVGP%\'),\'%COUNTRY%\',\'%SYS_COUNTRY%\'),\'%COUNTRYGP1%\',\'%SYS_COUNTRYGP1%\'),\'%SSIP%\',\'%SYS_SSIP%\'),\'%SSPORT%\',\'%SYS_SSPORT%\'),\'%TAG%\',\'%SYS_TAG%\'),\'%TODAY-yyyy%\',\'%SYS_TODAY-yyyy%\'),\'%TODAY-MM%\',\'%SYS_TODAY-MM%\'),\'%TODAY-dd%\',\'%SYS_TODAY-dd%\'),\'%TODAY-HH%\',\'%SYS_TODAY-HH%\'),\'%TODAY-mm%\',\'%SYS_TODAY-mm%\'),\'%TODAY-ss%\',\'%SYS_TODAY-ss%\'),\'%YESTERDAY-yyyy%\',\'%SYS_YESTERDAY-yyyy%\'),\'%YESTERDAY-MM%\',\'%SYS_YESTERDAY-MM%\'),\'%YESTERDAY-dd%\',\'%SYS_YESTERDAY-dd%\'),\'%YESTERDAY-HH%\',\'%SYS_YESTERDAY-HH%\'),\'%YESTERDAY-mm%\',\'%SYS_YESTERDAY-mm%\'),\'%YESTERDAY-ss%\',\'%SYS_YESTERDAY-ss%\');");
        SQLInstruction.add(SQLS.toString());

//-- Added takeScreenshot action.
//-- ------------------------ 339
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`) VALUES ('ACTION', 'takeScreenshot', 105, 12, 'takeScreenshot');");
        SQLInstruction.add(SQLS.toString());

//-- New Parameter for Selenium download link.
//-- ------------------------ 340
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `parameter` (`param`, `value`, `description`) VALUES ('selenium_download_url', 'http://selenium.googlecode.com/files/selenium-server-standalone-2.35.0.jar', 'URL to download the selenium package from the web.');");
        SQLInstruction.add(SQLS.toString());

//-- New Documentation on detail execution page.
//-- ------------------------ 341
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

//-- Revision Field resized to 20 to fit standard size in testcase execution table.
//-- ------------------------ 343
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcaseexecution` CHANGE COLUMN `Revision` `Revision` VARCHAR(20) NULL DEFAULT NULL ;");
        SQLInstruction.add(SQLS.toString());

//-- Replace \n by <br/> in HowTo textarea of TestCase
//-- ------------------------ 344
        SQLS = new StringBuilder();
        SQLS.append("UPDATE testcase SET HowTo=REPLACE(HowTo, '\\n', '<br/>');");
        SQLInstruction.add(SQLS.toString());

//-- Adding table that will host specific build revision lists per system.
//-- ------------------------ 345
        SQLS = new StringBuilder();
        SQLS.append(" CREATE  TABLE `buildrevisioninvariant` (");
        SQLS.append("  `system` VARCHAR(45) NOT NULL DEFAULT '' ,");
        SQLS.append("  `level` INT NOT NULL ,");
        SQLS.append("  `seq` INT NOT NULL ,");
        SQLS.append("  `versionname` VARCHAR(20) NULL ,");
        SQLS.append("  PRIMARY KEY (`system`, `level`, `seq`),");
        SQLS.append("  UNIQUE INDEX `IX_buildrevisioninvariant_01` (`system`,`level`,`versionname`) );");
        SQLInstruction.add(SQLS.toString());

//-- Cleaning Build and Revision from invariant table.
//-- ------------------------ 346
        SQLS = new StringBuilder();
        SQLS.append("DELETE FROM `invariant` where id in (8,9);");
        SQLInstruction.add(SQLS.toString());

//-- New Parameter for Selenium timeout when waiting for an element.
//-- ------------------------ 347
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `parameter` (`param`, `value`, `description`) VALUES ('selenium_defaultWait', '90', 'Integer that correspond to the number of seconds that selenium will wait before give timeout, when searching for a element.');");
        SQLInstruction.add(SQLS.toString());

//-- Updating documentation.
//-- ------------------------ 348
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

//-- Clean Group occurence.
//-- ------------------------
        SQLS = new StringBuilder(); // INTERACTIVE becomes AUTOMATED
        SQLS.append("UPDATE `invariant` SET `value`='AUTOMATED', `sort`='20' WHERE `id`='2' and`sort`='2';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `testcase` SET `Group`='AUTOMATED' WHERE `Group`='INTERACTIVE';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `invariant` SET `sort`='10' WHERE `id`='2' and`sort`='6';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `invariant` SET `sort`='30' WHERE `id`='2' and`sort`='5';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `invariant` SET `sort`='40' WHERE `id`='2' and`sort`='3';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `invariant` SET `sort`='50' WHERE `id`='2' and`sort`='4';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `invariant` SET `sort`='70' WHERE `id`='2' and`sort`='1';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

//-- Adding system column to parameter table.
//-- ------------------------
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `parameter` ADD COLUMN `system` VARCHAR(45) NOT NULL  FIRST ");
        SQLS.append(", DROP PRIMARY KEY , ADD PRIMARY KEY (`system`, `param`) ; ");
        SQLInstruction.add(SQLS.toString());

//-- Adding Index for performance optimisation and renaming other index for MySQL compliance (Index must have different names from Foreign Keys).
//-- ------------------------
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `buildrevisionparameters` ");
        SQLS.append(" ADD INDEX `IX_buildrevisionparameters_02` (`Build` ASC, `Revision` ASC, `Application` ASC) ");
        SQLS.append(" ,DROP INDEX `FK_buildrevisionparameters_01`, ADD INDEX `FK_buildrevisionparameters_01_IX` (`Application` ASC) ; ");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcaseexecutionsysver` ");
        SQLS.append(" DROP INDEX `FK_testcaseexecutionsysver_01` , ADD INDEX `FK_testcaseexecutionsysver_01_IX` (`ID` ASC) ");
        SQLS.append(" , ADD INDEX `IX_testcaseexecutionsysver_02` (`system` ASC, `Build` ASC, `Revision` ASC) ;");
        SQLInstruction.add(SQLS.toString());

        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `application` ");
        SQLS.append(" DROP INDEX `FK_application_01` , ADD INDEX `FK_application_01_IX` (`deploytype` ASC) ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `buildrevisionbatch` ");
        SQLS.append(" DROP INDEX `FK_buildrevisionbatch_01` , ADD INDEX `FK_buildrevisionbatch_01_IX` (`Batch` ASC) ");
        SQLS.append(" , DROP INDEX `FK_buildrevisionbatch_02` , ADD INDEX `FK_buildrevisionbatch_02_IX` (`system` ASC, `Country` ASC, `Environment` ASC) ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `countryenvdeploytype` ");
        SQLS.append(" DROP INDEX `FK_countryenvdeploytype_02` , ADD INDEX `FK_countryenvdeploytype_02_IX` (`deploytype` ASC) ");
        SQLS.append(" , DROP INDEX `FK_countryenvdeploytype_01` , ADD INDEX `FK_countryenvdeploytype_01_IX` (`system` ASC, `Country` ASC, `Environment` ASC) ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `countryenvironmentdatabase` ");
        SQLS.append(" DROP INDEX `FK_countryenvironmentdatabase_01` , ADD INDEX `FK_countryenvironmentdatabase_01_IX` (`system` ASC, `Country` ASC, `Environment` ASC) ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `countryenvironmentparameters` ");
        SQLS.append(" DROP INDEX `FK_countryenvironmentparameters_02` , ADD INDEX `FK_countryenvironmentparameters_02_IX` (`Application` ASC) ");
        SQLS.append(" , DROP INDEX `FK_countryenvironmentparameters_01` , ADD INDEX `FK_countryenvironmentparameters_01_IX` (`system` ASC, `Country` ASC, `Environment` ASC) ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `countryenvparam_log` ");
        SQLS.append(" DROP INDEX `FK_countryenvparam_log_01` , ADD INDEX `FK_countryenvparam_log_01_IX` (`system` ASC, `Country` ASC, `Environment` ASC) ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `host` ");
        SQLS.append(" DROP INDEX `FK_host_01` , ADD INDEX `FK_host_01_IX` (`system` ASC, `Country` ASC, `Environment` ASC) ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcaseexecutionwwwdet` ");
        SQLS.append(" DROP INDEX `FK_testcaseexecutionwwwdet_01` , ADD INDEX `FK_testcaseexecutionwwwdet_01_IX` (`ExecID` ASC) ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepactioncontrol` ");
        SQLS.append(" DROP INDEX `FK_testcasestepactioncontrol_01` , ADD INDEX `FK_testcasestepactioncontrol_01_IX` (`Test` ASC, `TestCase` ASC, `Step` ASC, `Sequence` ASC) ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `countryenvlink` ");
        SQLS.append(" DROP INDEX `FK_countryenvlink_01` , ADD INDEX `FK_countryenvlink_01_IX` (`system` ASC, `Country` ASC, `Environment` ASC) ");
        SQLS.append(" , DROP INDEX `FK_countryenvlink_02` , ADD INDEX `FK_countryenvlink_02_IX` (`systemLink` ASC, `CountryLink` ASC, `EnvironmentLink` ASC) ;");
        SQLInstruction.add(SQLS.toString());

// Browser IE and Chrome added to the invariant table
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`) VALUES ('BROWSER', 'iexplorer', 2, 37, 'Internet Explorer Browser');");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`) VALUES ('BROWSER', 'chrome', 3, 37, 'Chrome Browser');");
        SQLInstruction.add(SQLS.toString());

// MouseUp And MouseDown Added to the invariant table
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `invariant` SET `sort`='54' WHERE `id`='12' and`sort`='55';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`) VALUES ('ACTION', 'mouseDown', 55, 12, 'Selenium Action mouseDown');");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`) VALUES ('ACTION', 'mouseUp', 56, 12, 'Selenium Action mouseDown');");
        SQLInstruction.add(SQLS.toString());

// New usergroups added to the invariant table
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `invariant` CHANGE COLUMN `description` `description` VARCHAR(250) NOT NULL ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`) VALUES ");
        SQLS.append(" ('USERGROUP', 'TestRO', '100', '42', 'Has read only access to the information related to test cases and also has access to execution reporting options.')");
        SQLS.append(" ,('USERGROUP', 'Test', '110', '42', 'Can modify non WORKING test cases but cannot delete test cases.')");
        SQLS.append(" ,('USERGROUP', 'TestAdmin', '120', '42', 'Can modify or delete any test case (including Pre Testing test cases). Can also create or delete a test.')");
        SQLS.append(" ,('USERGROUP', 'RunTest', '200', '42', 'Can run both Manual and Automated test cases from GUI.')");
        SQLS.append(" ,('USERGROUP', 'IntegratorRO', '300', '42', 'Has access to the integration status.')");
        SQLS.append(" ,('USERGROUP', 'IntegratorNewChain', '350', '42', 'Can register the end of the chain execution. Has read only access to the other informations on the same page.')");
        SQLS.append(" ,('USERGROUP', 'IntegratorDeploy', '360', '42', 'Can disable or enable environments and register new build / revision.')");
        SQLS.append(" ,('USERGROUP', 'Integrator', '310', '42', 'Can add an application. Can change parameters of the environments.')");
        SQLS.append(" ,('USERGROUP', 'Administrator', '400', '42', 'Can create, modify or delete users. Has access to log Event and Database Maintenance. Can change Parameter values.');");
        SQLInstruction.add(SQLS.toString());

// GroupName column resized in order to support the new group list.
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `usergroup` CHANGE COLUMN `GroupName` `GroupName` VARCHAR(45) NOT NULL  ;");
        SQLInstruction.add(SQLS.toString());

// Creating the new groups from the previous groups.
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO usergroup SELECT distinct Login, 'TestRO' FROM usergroup where GroupName in ('User','Visitor','Admin');");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO usergroup SELECT distinct Login, 'Test' FROM usergroup where GroupName in ('User','Admin');");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO usergroup SELECT distinct Login, 'TestAdmin' FROM usergroup where GroupName in ('Admin');");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO usergroup SELECT distinct Login, 'RunTest' FROM usergroup where GroupName in ('User','Admin');");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO usergroup SELECT distinct Login, 'IntegratorRO' FROM usergroup where GroupName in ('Visitor','Integrator');");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO usergroup SELECT distinct Login, 'IntegratorNewChain' FROM usergroup where GroupName in ('Integrator');");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO usergroup SELECT distinct Login, 'IntegratorDeploy' FROM usergroup where GroupName in ('Integrator');");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO usergroup SELECT distinct Login, 'Administrator' FROM usergroup where GroupName in ('Admin');");
        SQLInstruction.add(SQLS.toString());

// Removing the old groups.
        SQLS = new StringBuilder();
        SQLS.append("DELETE FROM `invariant` WHERE `id`='42' and `sort` in ('5','10','15','20');");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("DELETE FROM `usergroup` where GroupName in ('Admin','User','Visitor');");
        SQLInstruction.add(SQLS.toString());

// Group definition documentation.
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

// Adding Description column in actions and control with associated documentation.
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepactioncontrol` ADD COLUMN `ControlDescription` VARCHAR(255) NOT NULL DEFAULT ''  AFTER `ControlProperty` ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepaction` ADD COLUMN `Description` VARCHAR(255) NOT NULL DEFAULT ''  AFTER `Property` ;");
        SQLInstruction.add(SQLS.toString());

// Creating table to host test data inside Cerberus (used when we cannot dynamically retreive data from the system).
        SQLS = new StringBuilder();
        SQLS.append("CREATE TABLE `testdata` (");
        SQLS.append("  `key` varchar(200) NOT NULL ,");
        SQLS.append("  `value` varchar(5000) NOT NULL DEFAULT '',");
        SQLS.append("  PRIMARY KEY (`key`)");
        SQLS.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8");
        SQLInstruction.add(SQLS.toString());

//Add parameters for the cerberus acount creation emailing
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `parameter` (`system`, `param`, `value`, `description`) VALUES ");
        SQLS.append("('', 'cerberus_accountcreation_defaultpassword', 'Cerberus2014', 'Default Password when creating an account.')");
        SQLS.append(",('', 'cerberus_notification_accountcreation_cc', 'Cerberus <no.reply@cerberus-testing.org>', 'Copy List used for Cerberus account creation notification email.')");
        SQLS.append(",('', 'cerberus_notification_accountcreation_subject', '[Cerberus] Welcome, your account has been created', 'Subject of Cerberus account creation notification email.')");
        SQLS.append(",('', 'cerberus_notification_accountcreation_body', 'Hello %NAME%<br><br>Your Cerberus account has been created<br><br>To connect Cerberus, please click <a href=\"http://cerberus_server/Cerberus\">here</a> and use this credential : <br><br>login : %LOGIN%<br>password : %DEFAULT_PASSWORD%<br><br>At your first connection, you will be invited to modify your password<br><br>Enjoy the tool<br><br>','Cerberus account creation notification email body. %LOGIN%, %NAME% and %DEFAULT_PASSWORD% can be used as variables.')");
        SQLS.append(",('', 'cerberus_notification_accountcreation_from','Cerberus <no.reply@cerberus-testing.org>', 'From field of Cerberus account creation notification email.')");
        SQLS.append(",('', 'cerberus_notification_accountcreation_activatenotification','N', 'Activation boolean for sending automatic email on account creation. Y value will activate the notifications. Any other value will not.')");
        SQLInstruction.add(SQLS.toString());

//Add email column in user table
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `user` ADD COLUMN `Email` VARCHAR(100) NULL AFTER `DefaultSystem`");
        SQLInstruction.add(SQLS.toString());

// Removing internal column inside application table.
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `application` DROP COLUMN `internal` ;");
        SQLInstruction.add(SQLS.toString());

// Fixing a typo ACTON --> ACTION in invariant table.
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `invariant` SET `idname`='ACTION' WHERE `id`='12' and`sort`='45';");
        SQLInstruction.add(SQLS.toString());

// removing addSelection action that did not exist and putting Unknown action in stead.
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `invariant` set  `value`='Unknown', `description`='Unknown' where `idname`='ACTION' and `value`='addSelection';");
        SQLInstruction.add(SQLS.toString());

// Adding switchToWindow action.
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` VALUES ('ACTION','switchToWindow',180,12,'switchToWindow',NULL,NULL,NULL);");
        SQLInstruction.add(SQLS.toString());

// New updated Documentation.
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

// Adding getFromTestData property type.
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`) VALUES ('PROPERTYTYPE', 'getFromTestData', '10', '19', 'Getting from the test Data library using the Key');");
        SQLInstruction.add(SQLS.toString());

// Reordering status.
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `invariant` SET `sort`='60' WHERE `id`='1' and`sort`='6';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `invariant` SET `sort`='50' WHERE `id`='1' and`sort`='5';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `invariant` SET `sort`='40' WHERE `id`='1' and`sort`='4';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `invariant` SET `sort`='20' WHERE `id`='1' and`sort`='3';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `invariant` SET `sort`='30' WHERE `id`='1' and`sort`='2';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `invariant` SET `sort`='10' WHERE `id`='1' and`sort`='1';");
        SQLInstruction.add(SQLS.toString());

// New updated Documentation.
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

// Adding the full version of Browser inside the execution table.
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcaseexecution` ADD COLUMN `BrowserFullVersion` VARCHAR(100) NULL DEFAULT ''  AFTER `Browser` ;");
        SQLInstruction.add(SQLS.toString());

// New updated Documentation.
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

// URL to download drivers.
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `parameter` (`system`, `param`, `value`, `description`) VALUES ('', 'selenium_chromedriver_download_url', 'http://chromedriver.storage.googleapis.com/index.html', 'Download URL for Selenium Chrome webdrivers.') ");
        SQLS.append(",('', 'selenium_iedriver_download_url', 'http://code.google.com/p/selenium/downloads/list','Download URL for Internet Explorer webdrivers.');");
        SQLInstruction.add(SQLS.toString());

// Add verifyElementNotVisible to control
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`, `gp1`, `gp2`, `gp3`) ");
        SQLS.append("VALUES ('CONTROL', 'verifyElementNotVisible', 31, 13, 'verifyElementNotVisible', NULL, NULL, NULL);");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `documentation` CHANGE COLUMN `DocLabel` `DocLabel` VARCHAR(100) NULL DEFAULT NULL  , CHANGE COLUMN `DocDesc` `DocDesc` TEXT NULL DEFAULT NULL  ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

// Reordering status.
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `invariant` SET `sort`='31' WHERE `id`='1' and`sort`='20';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `invariant` SET `sort`='21' WHERE `id`='1' and`sort`='30';");
        SQLInstruction.add(SQLS.toString());

// Documentation update on new variable of New bug URL.
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

// Resized URL links in application table.
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `application` CHANGE COLUMN `BugTrackerUrl` `BugTrackerUrl` VARCHAR(5000) NULL DEFAULT ''  , CHANGE COLUMN `BugTrackerNewUrl` `BugTrackerNewUrl` VARCHAR(5000) NULL DEFAULT '' ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

// Adding a very short description in invariant table.
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `invariant` ADD COLUMN `VeryShortDesc` VARCHAR(45) NULL DEFAULT '' AFTER `description` ");
        SQLInstruction.add(SQLS.toString());

// Initialise gp1 and VeryShortDesc for TCStatus.
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `invariant` SET `VeryShortDesc`=description WHERE `id`='1' ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `invariant` SET `gp1`='Y' WHERE `id`='1' ;");
        SQLInstruction.add(SQLS.toString());

// Add manageDialog to action and verifyTextInDialog to control and verifyStringContains to control.
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`, `gp1`, `gp2`, `gp3`) ");
        SQLS.append("VALUES ('ACTION','manageDialog',200,12,'manageDialog',NULL,NULL,NULL), ");
        SQLS.append(" ('CONTROL', 'verifyTextInDialog', 80, 13, 'verifyTextInDialog', NULL, NULL, NULL), ");
        SQLS.append(" ('CONTROL', 'verifyStringContains', 14, 13, 'verifyStringContains', NULL, NULL, NULL);");
        SQLInstruction.add(SQLS.toString());

// Renamed value to value1 and added value2 in testcasecountryproperties and testcaseexecutiondata tables      
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasecountryproperties` CHANGE COLUMN `Value` `Value1` VARCHAR(2500) NULL DEFAULT '' ,");
        SQLS.append("ADD COLUMN `Value2` VARCHAR(2500) NULL DEFAULT '' AFTER `Value1`;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcaseexecutiondata` CHANGE COLUMN `Object` `Value1` VARCHAR(3000) NULL DEFAULT NULL ,");
        SQLS.append("ADD COLUMN `Value2` VARCHAR(3000) NULL DEFAULT NULL AFTER `Value1`");
        SQLInstruction.add(SQLS.toString());

// Split IE browsers to IE9/IE10/IE11
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `invariant` SET `sort`='10' WHERE `id`='37' and`sort`='1';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `invariant` SET `sort`='20' WHERE `id`='37' and`sort`='3';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`, `VeryShortDesc`) ");
        SQLS.append(" VALUES ('BROWSER', 'IE9', '30', '37', 'Internet Explorer 9 Browser', ''),");
        SQLS.append("        ('BROWSER', 'IE10', '40', '37', 'Internet Explorer 10 Browser', ''),");
        SQLS.append("        ('BROWSER', 'IE11', '50', '37', 'Internet Explorer 11 Browser', '');");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("DELETE FROM `invariant` WHERE `id`='37' and`sort`='2';");
        SQLInstruction.add(SQLS.toString());

// Adding invariant for Public and private invariant.
//-- ------------------------ 434
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`, `VeryShortDesc`) ");
        SQLS.append(" VALUES ('INVARIANTPRIVATE', 'ACTION', '10', '44', '', ''),");
        SQLS.append("        ('INVARIANTPRIVATE', 'APPLITYPE', '20', '44', '', ''),");
        SQLS.append("        ('INVARIANTPRIVATE', 'BROWSER', '30', '44', '', ''),");
        SQLS.append("        ('INVARIANTPRIVATE', 'CHAIN', '40', '44', '', ''),");
        SQLS.append("        ('INVARIANTPRIVATE', 'CONTROL', '50', '44', '', ''),");
        SQLS.append("        ('INVARIANTPRIVATE', 'CTRLFATAL', '70', '44', '', ''),");
        SQLS.append("        ('INVARIANTPRIVATE', 'ENVACTIVE', '80', '44', '', ''),");
        SQLS.append("        ('INVARIANTPRIVATE', 'ENVTYPE', '100', '44', '', ''),");
        SQLS.append("        ('INVARIANTPRIVATE', 'GROUP', '130', '44', '', ''),");
        SQLS.append("        ('INVARIANTPRIVATE', 'OUTPUTFORMAT', '170', '44', '', ''),");
        SQLS.append("        ('INVARIANTPRIVATE', 'PROPERTYNATURE', '220', '44', '', ''),");
        SQLS.append("        ('INVARIANTPRIVATE', 'PROPERTYTYPE', '230', '44', '', ''),");
        SQLS.append("        ('INVARIANTPRIVATE', 'RUNPROD', '260', '44', '', ''),");
        SQLS.append("        ('INVARIANTPRIVATE', 'RUNQA', '270', '44', '', ''),");
        SQLS.append("        ('INVARIANTPRIVATE', 'RUNUAT', '280', '44', '', ''),");
        SQLS.append("        ('INVARIANTPRIVATE', 'SCREENSHOT', '290', '44', '', ''),");
        SQLS.append("        ('INVARIANTPRIVATE', 'SERVER', '300', '44', '', ''),");
        SQLS.append("        ('INVARIANTPRIVATE', 'SESSION', '310', '44', '', ''),");
        SQLS.append("        ('INVARIANTPRIVATE', 'TCACTIVE', '340', '44', '', ''),");
        SQLS.append("        ('INVARIANTPRIVATE', 'TCESTATUS', '350', '44', '', ''),");
        SQLS.append("        ('INVARIANTPRIVATE', 'TCREADONLY', '360', '44', '', ''),");
        SQLS.append("        ('INVARIANTPRIVATE', 'USERGROUP', '390', '44', '', ''),");
        SQLS.append("        ('INVARIANTPRIVATE', 'VERBOSE', '400', '44', '', ''),");
        SQLS.append("        ('INVARIANTPRIVATE', 'INVARIANTPRIVATE', '410', '44', '', ''),");
        SQLS.append("        ('INVARIANTPRIVATE', 'INVARIANTPUBLIC', '420', '44', '', ''),");
        SQLS.append("        ('INVARIANTPUBLIC', 'COUNTRY', '60', '45', '', ''),");
        SQLS.append("        ('INVARIANTPUBLIC', 'ENVIRONMENT', '90', '45', '', ''),");
        SQLS.append("        ('INVARIANTPUBLIC', 'EXECNBMIN', '110', '45', '', ''),");
        SQLS.append("        ('INVARIANTPUBLIC', 'FILTERNBDAYS', '120', '45', '', ''),");
        SQLS.append("        ('INVARIANTPUBLIC', 'MAXEXEC', '140', '45', '', ''),");
        SQLS.append("        ('INVARIANTPUBLIC', 'ORIGIN', '160', '45', '', ''),");
        SQLS.append("        ('INVARIANTPUBLIC', 'PRIORITY', '180', '45', '', ''),");
        SQLS.append("        ('INVARIANTPUBLIC', 'PROPERTYDATABASE', '210', '45', '', ''),");
        SQLS.append("        ('INVARIANTPUBLIC', 'SYSTEM', '330', '45', '', ''),");
        SQLS.append("        ('INVARIANTPUBLIC', 'TCSTATUS', '370', '45', '', ''),");
        SQLS.append("        ('INVARIANTPUBLIC', 'TEAM', '380', '45', '', '');");
        SQLInstruction.add(SQLS.toString());

// Removing id column to invariant table.
//-- ------------------------ 435
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `invariant` DROP PRIMARY KEY , DROP COLUMN `id` , CHANGE COLUMN `sort` `sort` INT(10) NOT NULL DEFAULT 0 ");
        SQLS.append(" , ADD PRIMARY KEY (`idname`, `value`) , ADD INDEX `IX_invariant_01` (`idname` ASC, `sort` ASC) ;");
        SQLInstruction.add(SQLS.toString());

// Adding getFromSoap property type.
//-- ------------------------ 436
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`) ");
        SQLS.append(" VALUES ('PROPERTYTYPE', 'executeSoapFromLib', '27', 'Getting from the SOAP request using the query');");
        SQLInstruction.add(SQLS.toString());

// Adding table to host soaplibrary.
//-- ------------------------ 437
        SQLS = new StringBuilder();
        SQLS.append("CREATE TABLE `soaplibrary` (");
        SQLS.append("  `Name` VARCHAR(45) ,");
        SQLS.append("  `Type` VARCHAR(45) ,");
        SQLS.append("  `ServicePath` VARCHAR(250) ,");
        SQLS.append("  `Method` VARCHAR(45) ,");
        SQLS.append("  `Envelope` TEXT ,");
        SQLS.append("  `ParsingAnswer` TEXT ,");
        SQLS.append("  `Description` VARCHAR(1000) ,");
        SQLS.append("  PRIMARY KEY (`Name`)");
        SQLS.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8");
        SQLInstruction.add(SQLS.toString());

// Adding Project Active Combo invariant.
//-- ------------------------ 438
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`) ");
        SQLS.append(" VALUES ('INVARIANTPRIVATE', 'PROJECTACTIVE', '21', ''),");
        SQLS.append("        ('PROJECTACTIVE', 'Y', '10', 'Active'),");
        SQLS.append("        ('PROJECTACTIVE', 'N', '20', 'Disable');");
        SQLInstruction.add(SQLS.toString());

// New updated Documentation.
//-- ------------------------ 439
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

// Chenged project constrain on testcase table so that in case a project is removed, the testcases are not removed.
//-- ------------------------ 441
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcase` DROP FOREIGN KEY `FK_testcase_03` ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcase` ADD CONSTRAINT `FK_testcase_03` FOREIGN KEY (`Project`) REFERENCES `project` (`idproject`) ON DELETE SET NULL ON UPDATE CASCADE;");
        SQLInstruction.add(SQLS.toString());

// Added Description column in TestData Table
//-- ------------------------ 443
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testdata` ADD COLUMN `Description` VARCHAR(1000) NULL DEFAULT ''  AFTER `value` ;");
        SQLInstruction.add(SQLS.toString());

//Add parameters for the enabling or disabling the logs of public calls.
//-- ------------------------ 444
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `parameter` (`system`, `param`, `value`, `description`) VALUES ");
        SQLS.append("('', 'cerberus_log_publiccalls', 'N', 'Enable [Y] or Disable [N] the loging of all the calls done to Cerberus public servlets.')");
        SQLInstruction.add(SQLS.toString());

//Removed unused logglassfish table.
//-- ------------------------ 445
        SQLS = new StringBuilder();
        SQLS.append("DROP TABLE `logglassfish`;");
        SQLInstruction.add(SQLS.toString());

//Add invariant getAttributeFromHtml.
//-- ------------------------ 446
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) VALUES ('PROPERTYTYPE', 'getAttributeFromHtml', '38', 'Getting Attribute value from an HTML field in the current page.', '');");
        SQLInstruction.add(SQLS.toString());

//Add documentation for new type of property getAttributeFromHtml.
//-- ------------------------ 447
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

//Add browser in an index of testcaseexecution table.
//-- ------------------------ 448
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcaseexecution` DROP INDEX `IX_testcaseexecution_04` ,ADD INDEX `IX_testcaseexecution_04` (`Test` ASC, `TestCase` ASC, `Country` ASC, `Browser` ASC, `Start` ASC, `ControlStatus` ASC);");
        SQLInstruction.add(SQLS.toString());

//Add Campaing management tables.
//-- ------------------------ 449
        SQLS = new StringBuilder();
        SQLS.append("CREATE TABLE `testbattery` (");
        SQLS.append("  `testbatteryID` int(10) unsigned NOT NULL AUTO_INCREMENT,");
        SQLS.append("  `testbattery` varchar(45) NOT NULL,");
        SQLS.append("  `Description` varchar(300) NOT NULL DEFAULT '',");
        SQLS.append("  PRIMARY KEY (`testbatteryID`),");
        SQLS.append("  UNIQUE KEY `IX_testbattery_01` (`testbattery`)");
        SQLS.append(") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;");
        SQLInstruction.add(SQLS.toString());

//-- ------------------------ 450
        SQLS = new StringBuilder();
        SQLS.append("CREATE TABLE `testbatterycontent` (");
        SQLS.append("  `testbatterycontentID` int(10) unsigned NOT NULL AUTO_INCREMENT,");
        SQLS.append("  `testbattery` varchar(45) NOT NULL,");
        SQLS.append("  `Test` varchar(45) NOT NULL,");
        SQLS.append("  `TestCase` varchar(45) NOT NULL,");
        SQLS.append("  PRIMARY KEY (`testbatterycontentID`),");
        SQLS.append("  UNIQUE KEY `IX_testbatterycontent_01` (`testbattery`, `Test`, `TestCase`),");
        SQLS.append("  KEY `IX_testbatterycontent_02` (`testbattery`),");
        SQLS.append("  KEY `IX_testbatterycontent_03` (`Test`, `TestCase`),");
        SQLS.append("  CONSTRAINT `FK_testbatterycontent_01` FOREIGN KEY (`testbattery`) REFERENCES `testbattery` (`testbattery`) ON DELETE CASCADE ON UPDATE CASCADE,");
        SQLS.append("  CONSTRAINT `FK_testbatterycontent_02` FOREIGN KEY (`Test`,`TestCase`) REFERENCES `testcase` (`Test`,`TestCase`) ON DELETE CASCADE ON UPDATE CASCADE");
        SQLS.append(") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;");
        SQLInstruction.add(SQLS.toString());

//-- ------------------------ 451
        SQLS = new StringBuilder();
        SQLS.append("CREATE TABLE `campaign` (");
        SQLS.append("  `campaignID` int(10) unsigned NOT NULL AUTO_INCREMENT,");
        SQLS.append("  `campaign` varchar(45) NOT NULL,");
        SQLS.append("  `Description` varchar(300) NOT NULL DEFAULT '',");
        SQLS.append("  PRIMARY KEY (`campaignID`),");
        SQLS.append("  UNIQUE KEY `IX_campaign_01` (`campaign`)");
        SQLS.append(") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;");
        SQLInstruction.add(SQLS.toString());

//-- ------------------------ 452
        SQLS = new StringBuilder();
        SQLS.append("CREATE TABLE `campaignparameter` (");
        SQLS.append("  `campaignparameterID` int(10) unsigned NOT NULL AUTO_INCREMENT,");
        SQLS.append("  `campaign` varchar(45) NOT NULL,");
        SQLS.append("  `Parameter` varchar(100) NOT NULL,");
        SQLS.append("  `Value` varchar(100) NOT NULL,");
        SQLS.append("  PRIMARY KEY (`campaignparameterID`),");
        SQLS.append("  UNIQUE KEY `IX_campaignparameter_01` (`campaign`, `Parameter`),");
        SQLS.append("  KEY `IX_campaignparameter_02` (`campaign`),");
        SQLS.append("  CONSTRAINT `FK_campaignparameter_01` FOREIGN KEY (`campaign`) REFERENCES `campaign` (`campaign`) ON DELETE CASCADE ON UPDATE CASCADE");
        SQLS.append(") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;");
        SQLInstruction.add(SQLS.toString());

//-- ------------------------ 453
        SQLS = new StringBuilder();
        SQLS.append("CREATE TABLE `campaigncontent` (");
        SQLS.append("  `campaigncontentID` int(10) unsigned NOT NULL AUTO_INCREMENT,");
        SQLS.append("  `campaign` varchar(45) NOT NULL,");
        SQLS.append("  `testbattery` varchar(45) NOT NULL,");
        SQLS.append("  PRIMARY KEY (`campaigncontentID`),");
        SQLS.append("  UNIQUE KEY `IX_campaigncontent_01` (`campaign`, `testbattery`),");
        SQLS.append("  KEY `IX_campaigncontent_02` (`campaign`),");
        SQLS.append("  CONSTRAINT `FK_campaigncontent_01` FOREIGN KEY (`campaign`) REFERENCES `campaign` (`campaign`) ON DELETE CASCADE ON UPDATE CASCADE,");
        SQLS.append("  CONSTRAINT `FK_campaigncontent_02` FOREIGN KEY (`testbattery`) REFERENCES `testbattery` (`testbattery`) ON DELETE CASCADE ON UPDATE CASCADE");
        SQLS.append(") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;");
        SQLInstruction.add(SQLS.toString());

//Add Database inside SQL Library table.
//-- ------------------------ 454
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `sqllibrary` CHANGE COLUMN `Name` `Name` VARCHAR(45) NOT NULL FIRST,");
        SQLS.append("  ADD COLUMN `Database` VARCHAR(45) NULL DEFAULT '' AFTER `Type` ;");
        SQLInstruction.add(SQLS.toString());

//Create table Robot.
//-- ------------------------ 455
        SQLS = new StringBuilder();
        SQLS.append("CREATE TABLE `robot` (");
        SQLS.append("`robotID` int(10) NOT NULL AUTO_INCREMENT,");
        SQLS.append("`robot` varchar(100) NOT NULL,");
        SQLS.append("`host` varchar(150) NOT NULL DEFAULT '',");
        SQLS.append("`port` varchar(20) NOT NULL DEFAULT '',");
        SQLS.append("`platform` varchar(45) NOT NULL DEFAULT '',");
        SQLS.append("`browser` varchar(45) NOT NULL DEFAULT '',");
        SQLS.append("`version` varchar(45) NOT NULL DEFAULT '',");
        SQLS.append("`active` varchar(1) NOT NULL DEFAULT 'Y',");
        SQLS.append("`description` varchar(250) NOT NULL DEFAULT '',");
        SQLS.append(" PRIMARY KEY (`robotID`),");
        SQLS.append(" UNIQUE KEY IX_robot_01 (`robot`)");
        SQLS.append(") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;");
        SQLInstruction.add(SQLS.toString());

//Update User set null value of defaultIP to empty.
//-- ------------------------ 456
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `user` ");
        SQLS.append("SET `DefaultIP`='' where `DefaultIP` IS NULL;");
        SQLInstruction.add(SQLS.toString());

//Modify User table adding robot preferences.
//-- ------------------------ 457
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `user` ");
        SQLS.append("CHANGE COLUMN `DefaultIP` `robotHost` VARCHAR(150) NOT NULL DEFAULT '',");
        SQLS.append("ADD COLUMN `robotPort` VARCHAR(20) NOT NULL DEFAULT '' AFTER `robotHost`,");
        SQLS.append("ADD COLUMN `robotPlatform` VARCHAR(45) NOT NULL DEFAULT '' AFTER `robotPort`,");
        SQLS.append("ADD COLUMN `robotBrowser` VARCHAR(45) NOT NULL DEFAULT '' AFTER `robotPlatform`,");
        SQLS.append("ADD COLUMN `robotVersion` VARCHAR(45) NOT NULL DEFAULT '' AFTER `robotBrowser`, ");
        SQLS.append("ADD COLUMN `robot` VARCHAR(100) NOT NULL DEFAULT '' AFTER `robotVersion`;");
        SQLInstruction.add(SQLS.toString());

//Insert Platform invariant.
//-- ------------------------ 458
        //TODO Add private invariant
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` ");
        SQLS.append("(`idname`,`value`,`sort`,`description`,`VeryShortDesc`,`gp1`,`gp2`,`gp3`) VALUES  ");
        SQLS.append("('PLATFORM','LINUX',30,'Linux Platform','',NULL,NULL,NULL),");
        SQLS.append("('PLATFORM','MAC',40,'Mac Platform','',NULL,NULL,NULL),");
        SQLS.append("('PLATFORM','WINDOWS',70,'Windows Platform','',NULL,NULL,NULL),");
        SQLS.append("('PLATFORM','ANDROID',10,'Android Platform','',NULL,NULL,NULL),");
        SQLS.append("('PLATFORM','UNIX',50,'Unix Platform','',NULL,NULL,NULL),");
        SQLS.append("('PLATFORM','VISTA',60,'Windows Vista Platform','',NULL,NULL,NULL),");
        SQLS.append("('PLATFORM','WIN8',80,'Windows 8 Platform','',NULL,NULL,NULL),");
        SQLS.append("('PLATFORM','XP',90,'Windows XP Platform','',NULL,NULL,NULL),");
        SQLS.append("('BROWSER','IE',20,'Internet Explorer Browser','',NULL,NULL,NULL),");
        SQLS.append("('BROWSER','android',70,'Android browser','',NULL,NULL,NULL),");
        SQLS.append("('BROWSER','ipad',80,'ipad browser','',NULL,NULL,NULL),");
        SQLS.append("('BROWSER','iphone',90,'iphone browser','',NULL,NULL,NULL),");
        SQLS.append("('BROWSER','opera',60,'Opera browser','',NULL,NULL,NULL),");
        SQLS.append("('BROWSER','safari',60,'Safari browser','',NULL,NULL,NULL),");
        SQLS.append("('ROBOTACTIVE','N',2,'Disable','',NULL,NULL,NULL),");
        SQLS.append("('ROBOTACTIVE','Y',1,'Active','',NULL,NULL,NULL),");
        SQLS.append("('INVARIANTPRIVATE','ROBOTACTIVE',430,'','',NULL,NULL,NULL),");
        SQLS.append("('INVARIANTPRIVATE','PLATFORM','35','','',NULL,NULL,NULL);");
        SQLInstruction.add(SQLS.toString());

//DELETE old browser invariant.
//-- ------------------------ 459
        SQLS = new StringBuilder();
        SQLS.append("DELETE FROM `invariant` where `idname`='BROWSER' and `value` in ('IE9','IE10','IE11');");
        SQLInstruction.add(SQLS.toString());

//Add Version and Platform column in testcaseExecution table.
//-- ------------------------ 460
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcaseexecution` ");
        SQLS.append("ADD COLUMN `Version` VARCHAR(20) NOT NULL DEFAULT '' AFTER `Browser`,");
        SQLS.append("ADD COLUMN `Platform` VARCHAR(20) NOT NULL DEFAULT '' AFTER `Version`,");
        SQLS.append("CHANGE COLUMN `IP` `IP` VARCHAR(150) NULL DEFAULT NULL ;");
        SQLInstruction.add(SQLS.toString());

//Insert Default Robot.
//-- ------------------------ 461
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `robot` (`robot` ,`host` ,`port` ,`platform` ,`browser` ,`version` , `active` ,`description`)");
        SQLS.append("VALUES ('MyRobot', '127.0.0.1', '5555', 'LINUX', 'firefox', '28', 'Y', 'My Robot');");
        SQLInstruction.add(SQLS.toString());

//Insert parameter cerberus_picture_testcase_path.
//-- ------------------------ 462
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `parameter` (`system`, `param`, `value`, `description`) VALUES ");
        SQLS.append("('', 'cerberus_picture_testcase_path', '', 'Path to store the Cerberus Value and HowTo pictures of TestCase page');");
        SQLInstruction.add(SQLS.toString());

//Change IP on countryEnvironmentParameters accordingly to other tables (user, testcaseexecution and robot).
//-- ------------------------ 463        
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `countryenvironmentparameters` ");
        SQLS.append("CHANGE COLUMN `IP` `IP` VARCHAR(150) NOT NULL DEFAULT '';");
        SQLInstruction.add(SQLS.toString());

//Add Invariant for campaign parameters.
//-- ------------------------ 464 
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`, `gp1`) VALUES ");
        SQLS.append("('CAMPAIGN_PARAMETER', 'BROWSER', '10', 'Browser use to execute campaign', 'Browser', 'INVARIANTPRIVATE'),");
        SQLS.append("('CAMPAIGN_PARAMETER', 'COUNTRY', '20', 'Country selected for campaign', 'Country', 'INVARIANTPUBLIC'),");
        SQLS.append("('CAMPAIGN_PARAMETER', 'ENVIRONMENT', '30', 'Which environment used to execute campaign', 'Environment', 'INVARIANTPUBLIC'),");
        SQLS.append("('INVARIANTPRIVATE','CAMPAIGN_PARAMETER','440','','',NULL);");
        SQLInstruction.add(SQLS.toString());

//Add Invariant for new control verify element in element.
//-- ------------------------ 465 
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`, `gp1`, `gp2`, `gp3`) VALUES ");
        SQLS.append("('CONTROL', 'verifyElementInElement', 32, 'verifyElementInElement', '', NULL, NULL, NULL);");
        SQLInstruction.add(SQLS.toString());

//Add Documentation for new control verify element in element.
//-- ------------------------ 466 
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

//Remove null on each field of countryenvparam.
//-- ------------------------ 467 >  477
        SQLS = new StringBuilder();
        SQLS.append("update `countryenvparam` set  `Build` = '' where `Build` is null;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("update `countryenvparam` set  `Revision` = '' where `Revision` is null;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("update `countryenvparam` set  `Chain` = '' where `Chain` is null;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("update `countryenvparam` set  `DistribList` = '' where `DistribList` is null;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("update `countryenvparam` set  `EMailBodyRevision` = '' where `EMailBodyRevision` is null;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("update `countryenvparam` set  `Type` = '' where `Type` is null;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("update `countryenvparam` set  `EMailBodyChain` = '' where `EMailBodyChain` is null;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("update `countryenvparam` set  `EMailBodyDisableEnvironment` = '' where `EMailBodyDisableEnvironment` is null;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("update `countryenvparam` set  `maintenanceact` = '' where `maintenanceact` is null;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("update `countryenvparam` set  `maintenanceend` = '0' where `maintenanceend` is null;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("update `countryenvparam` set `maintenancestr` = '0' where `maintenancestr` is null;");
        SQLInstruction.add(SQLS.toString());

//Alter table countryenvparam to put default value empty instead of NULL
//-- ------------------------ 478
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `countryenvparam`");
        SQLS.append("CHANGE COLUMN `Build` `Build` VARCHAR(10) NOT NULL DEFAULT '' ,");
        SQLS.append("CHANGE COLUMN `Revision` `Revision` VARCHAR(20) NOT NULL DEFAULT '' ,");
        SQLS.append("CHANGE COLUMN `Chain` `Chain` VARCHAR(20) NOT NULL DEFAULT '' ,");
        SQLS.append("CHANGE COLUMN `DistribList` `DistribList` TEXT NOT NULL ,");
        SQLS.append("CHANGE COLUMN `EMailBodyRevision` `EMailBodyRevision` TEXT NOT NULL  ,");
        SQLS.append("CHANGE COLUMN `Type` `Type` VARCHAR(20) NOT NULL DEFAULT '' ,");
        SQLS.append("CHANGE COLUMN `EMailBodyChain` `EMailBodyChain` TEXT NOT NULL ,");
        SQLS.append("CHANGE COLUMN `EMailBodyDisableEnvironment` `EMailBodyDisableEnvironment` TEXT NOT NULL ,");
        SQLS.append("CHANGE COLUMN `maintenanceact` `maintenanceact` VARCHAR(1) NOT NULL DEFAULT 'N' ,");
        SQLS.append("CHANGE COLUMN `maintenancestr` `maintenancestr` TIME NOT NULL DEFAULT 0 ,");
        SQLS.append("CHANGE COLUMN `maintenanceend` `maintenanceend` TIME NOT NULL DEFAULT 0 ;");
        SQLInstruction.add(SQLS.toString());

//Alter table countryenvparam to put default value empty instead of NULL
//-- ------------------------ 479       
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `campaignparameter` DROP INDEX `IX_campaignparameter_01` , ");
        SQLS.append("ADD UNIQUE INDEX `IX_campaignparameter_01` (`campaign` ASC, `Parameter` ASC, `Value` ASC);");
        SQLInstruction.add(SQLS.toString());

//Add invariant action openURL
//-- ------------------------ 480       
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` ");
        SQLS.append(" VALUES ('ACTION', 'openUrl', '65', 'openUrl', '', NULL,NULL,NULL);");
        SQLInstruction.add(SQLS.toString());

//Add documentation related to action openURL
//-- ------------------------ 481       
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

//Add function column in testcase table
//-- ------------------------ 482      
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcase` ");
        SQLS.append("ADD COLUMN `function` VARCHAR(500) NULL DEFAULT '' AFTER `activePROD`;");
        SQLInstruction.add(SQLS.toString());

//Add documentation for function column in testcase table
//-- ------------------------ 483      
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

//Add parameter for management of function column in testcase table
//-- ------------------------ 484-485
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `parameter` (`system`, `param`, `value`, `description`) VALUES ");
        SQLS.append("('', 'cerberus_testcase_function_urlForListOfFunction', '/URL/TO/FUNCTION/SERVICE', 'URL to feed the function field with proposal for autocompletion. URL should respond JSON format');");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `parameter` (`system`, `param`, `value`, `description`) VALUES ");
        SQLS.append("('', 'cerberus_testcase_function_booleanListOfFunction', 'N', 'boolean to activate autocompletion on function fields.');");
        SQLInstruction.add(SQLS.toString());

//Add documentation for timeout and synchroneous field
//-- ------------------------ 486      
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

//Add invariant synchroneous
//-- ------------------------ 487      
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` VALUES ");
        SQLS.append(" ('SYNCHRONEOUS', 'N', '2', 'Redirect to the execution before the end of the execution', '', NULL, NULL, NULL),");
        SQLS.append(" ('SYNCHRONEOUS', 'Y', '1', 'Redirect to the execution after the end of the execution', '', NULL, NULL, NULL);");
        SQLInstruction.add(SQLS.toString());

//Add invariant private synchroneous
//-- ------------------------ 488       
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) VALUES ('INVARIANTPRIVATE', 'SYNCHRONEOUS', '430', '', '');");
        SQLInstruction.add(SQLS.toString());

//Add invariant action callSoapWithBase
//-- ------------------------ 489       
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) VALUES ('ACTION', 'callSoapWithBase', '190', 'callSoapWithBase', '');");
        SQLInstruction.add(SQLS.toString());

//Add invariant CONTROL verifyXmlTreeStructure
//-- ------------------------ 490       
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) VALUES ('CONTROL', 'verifyXmlTreeStructure', '90', 'verifyXmlTreeStructure', '');");
        SQLInstruction.add(SQLS.toString());

//Add invariant action mouseDownMouseUp
//-- ------------------------ 491       
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) VALUES ('ACTION', 'mouseDownMouseUp', '200', 'mouseDownMouseUp', '');");
        SQLInstruction.add(SQLS.toString());

//Update documentation for new properties %SYS_TODAY-doy% and %SYS_YESTERDAY-doy%
//-- ------------------------ 492
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

//Add use step columns in testcasestep
//-- ------------------------ 493       
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestep` ");
        SQLS.append("ADD COLUMN `useStep` VARCHAR(1) NULL DEFAULT 'N' AFTER `Description`, ");
        SQLS.append("ADD COLUMN `useStepTest` VARCHAR(45) NOT NULL DEFAULT '' AFTER `useStep`, ");
        SQLS.append("ADD COLUMN `useStepTestCase` VARCHAR(45) NOT NULL DEFAULT '' AFTER `useStepTest`, ");
        SQLS.append("ADD COLUMN `useStepStep` INT(10) NOT NULL  AFTER `useStepTestCase`; ");
        SQLInstruction.add(SQLS.toString());

//Add control isElementClickable and isElementNotClickable
//-- ------------------------ 494       
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) VALUES ");
        SQLS.append(" ('CONTROL','verifyElementClickable',35,'isElementClickable',''),");
        SQLS.append(" ('CONTROL','verifyElementNotClickable',36,'isElementNotClickable','')");
        SQLInstruction.add(SQLS.toString());

//Add documentation isElementClickable , isElementNotClickable, callSoapWithBase
//-- ------------------------ 495       
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

//Remove mouseUpMouseDown
//-- ------------------------ 496       
        SQLS = new StringBuilder();
        SQLS.append("DELETE FROM `invariant` WHERE `idname`='ACTION' and `value`='mouseDownMouseUp';");
        SQLInstruction.add(SQLS.toString());

//Update documentation for new properties %SYS_EXECUTIONID%
//-- ------------------------ 497
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

//Add action getPageSource
//-- ------------------------ 498       
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) VALUES ('ACTION', 'getPageSource', '210', 'getPageSource', '');");
        SQLInstruction.add(SQLS.toString());

//Add documentation getPageSource
//-- ------------------------ 499      
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

//Add invariant callSoap and getFromXml
//-- ------------------------ 500      
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) VALUES ");
        SQLS.append(" ('ACTION', 'callSoap', '189', 'callSoap', ''),");
        SQLS.append(" ('PROPERTYTYPE', 'getFromXml', '50', 'getFromXml', '');");
        SQLInstruction.add(SQLS.toString());

//Add documentation getFromXml
//-- ------------------------ 501      
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

//Add documentation getFromCookie
//-- ------------------------ 502      
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

//Add invariant getFromCookie
//-- ------------------------ 503      
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) VALUES ");
        SQLS.append(" ('PROPERTYTYPE', 'getFromCookie', '60', 'getFromCookie', '');");
        SQLInstruction.add(SQLS.toString());

//Add documentation seleniumLog and pageSource
//-- ------------------------ 504      
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

//Add invariant seleniumLog and pageSource
//-- ------------------------ 505      
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) VALUES ");
        SQLS.append(" ('INVARIANTPRIVATE', 'PAGESOURCE', '440', '', ''),");
        SQLS.append(" ('INVARIANTPRIVATE', 'SELENIUMLOG', '450', '', ''),");
        SQLS.append(" ('PAGESOURCE', '0', '10', 'Never get Page Source', ''),");
        SQLS.append(" ('PAGESOURCE', '1', '20', 'Get Page Source on error only', ''),");
        SQLS.append(" ('PAGESOURCE', '2', '30', 'Get Page Source after each action', ''),");
        SQLS.append(" ('SELENIUMLOG', '0', '10', 'Never record Selenium Log', ''),");
        SQLS.append(" ('SELENIUMLOG', '1', '20', 'Record Selenium Log on error only', ''),");
        SQLS.append(" ('SELENIUMLOG', '2', '30', 'Record Selenium Log on testcase', '');");
        SQLInstruction.add(SQLS.toString());

//Add PageSource filename on testcasestepactionexecution table
//-- ------------------------ 506      
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepactionexecution` ");
        SQLS.append("ADD COLUMN `PageSourceFileName` VARCHAR(150) NULL DEFAULT NULL AFTER `ScreenshotFilename`;");
        SQLInstruction.add(SQLS.toString());

//Add PageSource filename on testcasestepactioncontrolexecution table
//-- ------------------------ 507      
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepactioncontrolexecution` ");
        SQLS.append("ADD COLUMN `PageSourceFilename` VARCHAR(150) NULL DEFAULT NULL AFTER `ScreenshotFilename`;");
        SQLInstruction.add(SQLS.toString());

//Add Selenium Log in documentation
//-- ------------------------ 508      
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

//Default value 0 for use step 
//-- ------------------------ 509      
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestep` ");
        SQLS.append("CHANGE COLUMN `useStepStep` `useStepStep` INT(10) NOT NULL DEFAULT '0' ;");
        SQLInstruction.add(SQLS.toString());

//Create table usersystem 
//-- ------------------------ 510   
        SQLS = new StringBuilder();
        SQLS.append("CREATE TABLE `usersystem` (");
        SQLS.append("`Login` VARCHAR(10) NOT NULL,");
        SQLS.append("`System` VARCHAR(45) NOT NULL,");
        SQLS.append("PRIMARY KEY (`Login`, `System`))  ENGINE=InnoDB DEFAULT CHARSET=utf8;");
        SQLInstruction.add(SQLS.toString());

//Create table usersystem 
//-- ------------------------ 511   
        SQLS = new StringBuilder();
        SQLS.append("insert into usersystem ");
        SQLS.append("select u.login, i.value from user u, invariant i where i.idname='SYSTEM';");
        SQLInstruction.add(SQLS.toString());

//Default value in sort on application table 
//-- ------------------------ 512   
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `application` ");
        SQLS.append("CHANGE COLUMN `sort` `sort` INT(11) NOT NULL DEFAULT 10 ;");
        SQLInstruction.add(SQLS.toString());

//Add application type WS
//-- ------------------------ 513
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`) VALUES ");
        SQLS.append("('APPLITYPE', 'WS', '30', 'Web Service Application');");
        SQLInstruction.add(SQLS.toString());

//Add executor (user login or selenium) in testcaseexecution table
//-- ------------------------ 514
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE testcaseexecution ");
        SQLS.append("ADD COLUMN `Executor` VARCHAR(10) NULL;");
        SQLInstruction.add(SQLS.toString());

// New updated Documentation.
//-- ------------------------ 515
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

// Add invariant getDifferencesFromXml.
//-- ------------------------ 516
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`) VALUES ");
        SQLS.append("('PROPERTYTYPE', 'getDifferencesFromXml', '51', 'Get differences from XML files');");
        SQLInstruction.add(SQLS.toString());

// Add invariant getDifferencesFromXml.
//-- ------------------------ 517
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`) VALUES ");
        SQLS.append("('ACTION', 'removeDifference', '220', 'Remove differences from the given pattern');");
        SQLInstruction.add(SQLS.toString());

// Add colums to use test data at application / environment / country level.
//-- ------------------------ 518
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testdata` ");
        SQLS.append("ADD COLUMN `Application` VARCHAR(45) NOT NULL DEFAULT '' AFTER `Description`,");
        SQLS.append("ADD COLUMN `Country` VARCHAR(2) NOT NULL DEFAULT '' AFTER `Application`,");
        SQLS.append("ADD COLUMN `Environment` VARCHAR(45) NOT NULL DEFAULT '' AFTER `Country`;");
        SQLInstruction.add(SQLS.toString());

// Add update primary key to test data at key / application / environment / country level.
//-- ------------------------ 519
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testdata` ");
        SQLS.append("DROP PRIMARY KEY, ADD PRIMARY KEY (`key`, `Environment`, `Country`, `Application`);");
        SQLInstruction.add(SQLS.toString());

// Increase soaplibray's Name column size from 45 to 255.
//-- ------------------------ 520
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `soaplibrary` ");
        SQLS.append("CHANGE COLUMN `Name` `Name` VARCHAR(255) NOT NULL DEFAULT '' ;");
        SQLInstruction.add(SQLS.toString());

// Increase soaplibray's Envelope column type from TEXT to MEDIUMTEXT.
//-- ------------------------ 521
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `soaplibrary` ");
        SQLS.append("CHANGE COLUMN `Envelope` `Envelope` MEDIUMTEXT NULL DEFAULT NULL ;");
        SQLInstruction.add(SQLS.toString());

// Add foreign key to usersystem table.
//-- ------------------------ 522-524
        SQLS = new StringBuilder();
        SQLS.append("DROP TABLE `usersystem` ;");
        SQLInstruction.add(SQLS.toString());

        SQLS = new StringBuilder();
        SQLS.append("CREATE TABLE `usersystem` (");
        SQLS.append("  `Login` VARCHAR(10) NOT NULL,");
        SQLS.append("  `System` VARCHAR(45) NOT NULL,");
        SQLS.append(" PRIMARY KEY (`Login`, `System`), ");
        SQLS.append(" CONSTRAINT `FK_usersystem_01` FOREIGN KEY (`Login` ) REFERENCES `user` (`Login`) ON DELETE CASCADE ON UPDATE CASCADE ");
        SQLS.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8 ;");
        SQLInstruction.add(SQLS.toString());

        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO usersystem ");
        SQLS.append(" SELECT u.login, i.value FROM user u, invariant i WHERE i.idname='SYSTEM';");
        SQLInstruction.add(SQLS.toString());

// Creating new tables for test data.
//-- ------------------------ 525-526
        SQLS = new StringBuilder();
        SQLS.append("CREATE TABLE `testdatalib` (");
        SQLS.append("  `Name` varchar(200) NOT NULL,");
        SQLS.append("  `system` varchar(45) NOT NULL DEFAULT '',");
        SQLS.append("  `Environment` varchar(45) NOT NULL DEFAULT '',");
        SQLS.append("  `Country` varchar(2) NOT NULL DEFAULT '',");
        SQLS.append("  `Group` varchar(200) NOT NULL DEFAULT '',");
        SQLS.append("  `Type` varchar(45) NOT NULL DEFAULT '',");
        SQLS.append("  `Database` varchar(45) NOT NULL DEFAULT '',");
        SQLS.append("  `Script` varchar(2500) NOT NULL DEFAULT '',");
        SQLS.append("  `ServicePath` varchar(250) NOT NULL DEFAULT '',");
        SQLS.append("  `Method` varchar(45) NOT NULL DEFAULT '',");
        SQLS.append("  `Envelope` text,");
        SQLS.append("  `Description` varchar(1000) NOT NULL DEFAULT '',");
        SQLS.append("  PRIMARY KEY (`Name`,`system`,`Environment`,`Country`)");
        SQLS.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8;");
        SQLInstruction.add(SQLS.toString());

        SQLS = new StringBuilder();
        SQLS.append("CREATE TABLE `testdatalibdata` (");
        SQLS.append("  `Name` varchar(200) NOT NULL,");
        SQLS.append("  `system` varchar(45) NOT NULL DEFAULT '',");
        SQLS.append("  `Environment` varchar(45) NOT NULL DEFAULT '',");
        SQLS.append("  `Country` varchar(2) NOT NULL DEFAULT '',");
        SQLS.append("  `SubData` varchar(200) NOT NULL DEFAULT '',");
        SQLS.append("  `Value` text,");
        SQLS.append("  `Column` varchar(255) NOT NULL DEFAULT '',");
        SQLS.append("  `ParsingAnswer` text ,");
        SQLS.append("  `Description` varchar(1000)  NOT NULL DEFAULT '',");
        SQLS.append("  PRIMARY KEY (`Name`,`system`,`Environment`,`Country`,`SubData`),");
        SQLS.append("  CONSTRAINT `FK_testdatalibdata_01` FOREIGN KEY (`Name`,`system`,`Environment`,`Country`) REFERENCES `testdatalib` (`Name`,`system`,`Environment`,`Country`) ON DELETE CASCADE ON UPDATE CASCADE");
        SQLS.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8;");
        SQLInstruction.add(SQLS.toString());

// Temporary init data.
//-- ------------------------ 527-532
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO testdatalib (`system`,`Country`,`Environment`,`Name`, `Type`, `Description`, `Envelope`) ");
        SQLS.append(" SELECT '', `Country`, `Environment`, `key`, 'STATIC', IFNULL(td.`Description`,''), '' from testdata td");
        SQLS.append(" ON DUPLICATE KEY UPDATE Description = IFNULL(td.`Description`,'');");
        SQLInstruction.add(SQLS.toString());

        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO testdatalibdata (`system`,`Country`,`Environment`,`Name`, `SubData`, `Value`, `Description`, `ParsingAnswer`) ");
        SQLS.append(" SELECT '', `Country`, `Environment`, `key`, '', IFNULL(td.`value`,''), IFNULL(td.`Description`,''), '' from testdata td");
        SQLS.append(" ON DUPLICATE KEY UPDATE `Value` = IFNULL(td.`value`,''), Description = IFNULL(td.`Description`,'');");
        SQLInstruction.add(SQLS.toString());

        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO testdatalib (`system`,`Country`,`Environment`,`Name`, `Group`, `Type`, `Database`, `Script`, `Description`, `Envelope`) ");
        SQLS.append(" SELECT '', '', '', `Name`, IFNULL(`Type`,''), 'SQL', IFNULL(`Database`,''), IFNULL(`Script`,''), IFNULL(description,'') , '' from sqllibrary sl");
        SQLS.append(" ON DUPLICATE KEY UPDATE `Group`=IFNULL(sl.Type,''), `Database`=IFNULL(sl.`Database`,''), `Script`=IFNULL(sl.`Script`,''), Description=IFNULL(sl.Description,'');");
        SQLInstruction.add(SQLS.toString());

        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO testdatalibdata (`system`,`Country`,`Environment`,`Name`, `SubData`, `Column`, `Description`, `ParsingAnswer`, `Value`) ");
        SQLS.append(" SELECT '', '', '', `Name`, '', '', IFNULL(description,''), '', '' from sqllibrary sl");
        SQLS.append(" ON DUPLICATE KEY UPDATE Description=IFNULL(sl.Description,'');");
        SQLInstruction.add(SQLS.toString());

        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO testdatalib (`system`,`Country`,`Environment`,`Name`, `Group`, `Type`, `ServicePath`, `Method`, `Envelope`, `Description`) ");
        SQLS.append(" SELECT '', '', '', `Name`, IFNULL(`Type`,''), 'SOAP', IFNULL(`ServicePath`,''), IFNULL(`Method`,''), IFNULL(Envelope,''), IFNULL(description,'') from soaplibrary sl");
        SQLS.append(" ON DUPLICATE KEY UPDATE `Group`=IFNULL(sl.Type,''), `ServicePath`=IFNULL(sl.`ServicePath`,''), `Method`=IFNULL(sl.`Method`,''), `Envelope`=IFNULL(sl.`Envelope`,''), Description=IFNULL(sl.Description,'');");
        SQLInstruction.add(SQLS.toString());

        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO testdatalibdata (`system`,`Country`,`Environment`,`Name`, `SubData`, `ParsingAnswer`, `Description`, `Value`) ");
        SQLS.append(" SELECT '', '', '', `Name`, '', IFNULL(ParsingAnswer,''), IFNULL(description, ''), '' from soaplibrary sl");
        SQLS.append(" ON DUPLICATE KEY UPDATE `ParsingAnswer`=IFNULL(sl.ParsingAnswer,''), Description=IFNULL(sl.Description,'');");
        SQLInstruction.add(SQLS.toString());

// Creating invariant TESTDATATYPE.
//-- ------------------------ 533
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) VALUES ");
        SQLS.append(" ('INVARIANTPRIVATE', 'TESTDATATYPE', '460', '', ''),");
        SQLS.append(" ('TESTDATATYPE', 'STATIC', '10', 'Static test data.', ''),");
        SQLS.append(" ('TESTDATATYPE', 'SQL', '20', 'Dynamic test data from SQL execution.', ''),");
        SQLS.append(" ('TESTDATATYPE', 'SOAP', '30', 'Dynamic test data from SOAP Webservice call.', '');");
        SQLInstruction.add(SQLS.toString());

// Creating technical id between testdatalib and testdatalibdata tables.
//-- ------------------------ 534-539
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testdatalib` ");
        SQLS.append("ADD COLUMN `TestDataLibID` INT UNSIGNED NOT NULL AUTO_INCREMENT FIRST,");
        SQLS.append("DROP PRIMARY KEY,");
        SQLS.append("ADD PRIMARY KEY (`TestDataLibID`),");
        SQLS.append("ADD UNIQUE INDEX `IX_testdatalib_01` (`Name` ASC, `system` ASC, `Environment` ASC, `Country` ASC);");
        SQLInstruction.add(SQLS.toString());

        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testdatalibdata` ");
        SQLS.append("ADD COLUMN `TestDataLibID` INT UNSIGNED NOT NULL DEFAULT 0 FIRST;");
        SQLInstruction.add(SQLS.toString());

        SQLS = new StringBuilder();
        SQLS.append("UPDATE `testdatalibdata` ld, `testdatalib` l SET ld.TestDataLibID=l.TestDataLibID");
        SQLS.append(" WHERE ld.`Name`=l.`Name` and ld.`system`=l.`system` and ld.`Environment`=l.`Environment` and ld.`Country`=l.`Country`;");
        SQLInstruction.add(SQLS.toString());

        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testdatalibdata` ");
        SQLS.append("DROP FOREIGN KEY `FK_testdatalibdata_01`;");
        SQLInstruction.add(SQLS.toString());

        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testdatalibdata` ");
        SQLS.append("DROP COLUMN `Country`, DROP COLUMN `Environment`, DROP COLUMN `system`, DROP COLUMN `Name`, ");
        SQLS.append("DROP PRIMARY KEY, ADD PRIMARY KEY (`TestDataLibID`, `SubData`);");
        SQLInstruction.add(SQLS.toString());

        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testdatalibdata` ");
        SQLS.append("ADD CONSTRAINT `FK_testdatalibdata_01`");
        SQLS.append("  FOREIGN KEY (`TestDataLibID`)");
        SQLS.append("  REFERENCES `testdatalib` (`TestDataLibID`) ON DELETE CASCADE ON UPDATE CASCADE;");
        SQLInstruction.add(SQLS.toString());

// Cleaning EXECNBMIN invariant as not used anymore following filter on before date.
//-- ------------------------ 540
        SQLS = new StringBuilder();
        SQLS.append("DELETE FROM invariant where idname='EXECNBMIN' or (idname='INVARIANTPUBLIC' and value='EXECNBMIN');");
        SQLInstruction.add(SQLS.toString());

//Update documentation for new properties %SYS_ELAPSED-EXESTART% and %SYS_ELAPSED-STEPSTART%
//-- ------------------------ 541
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

//Removed Sla columns from testcaseexecution table
//-- ------------------------ 542
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcase` DROP COLUMN `Sla`;");
        SQLInstruction.add(SQLS.toString());

// Resizing invariant table column.
//-- ------------------------ 543
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `invariant` ");
        SQLS.append("CHANGE COLUMN `value` `value` VARCHAR(255) NOT NULL , ");
        SQLS.append("CHANGE COLUMN `description` `description` VARCHAR(255) NOT NULL , ");
        SQLS.append("CHANGE COLUMN `gp1` `gp1` VARCHAR(255) NULL DEFAULT NULL , ");
        SQLS.append("CHANGE COLUMN `gp2` `gp2` VARCHAR(255) NULL DEFAULT NULL , ");
        SQLS.append("CHANGE COLUMN `gp3` `gp3` VARCHAR(255) NULL DEFAULT NULL");
        SQLInstruction.add(SQLS.toString());

// Insert new private invariant value for APPLITYPE
//-- ------------------------ 544
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) ");
        SQLS.append("VALUES ('APPLITYPE', 'APK', '40', 'Android Application', '')");
        SQLInstruction.add(SQLS.toString());

// Add column inlibrary in testcasestep table
//-- ------------------------ 545
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestep` ");
        SQLS.append("ADD COLUMN `inlibrary` VARCHAR(1) NULL DEFAULT 'N' AFTER `useStepStep`;");
        SQLInstruction.add(SQLS.toString());

// Add table testcaseexecutionqueue
//-- ------------------------ 546
        SQLS = new StringBuilder();
        SQLS.append("CREATE TABLE `testcaseexecutionqueue` (");
        SQLS.append("  `ID` bigint(20) unsigned NOT NULL AUTO_INCREMENT,");
        SQLS.append("  `Test` varchar(45) NOT NULL,");
        SQLS.append("  `TestCase` varchar(45) NOT NULL,");
        SQLS.append("  `Country` varchar(2) NOT NULL,");
        SQLS.append("  `Environment` varchar(45) NOT NULL,");
        SQLS.append("  `Robot` varchar(45) DEFAULT NULL,");
        SQLS.append("  `RobotIP` varchar(150) DEFAULT NULL,");
        SQLS.append("  `RobotPort` varchar(20) DEFAULT NULL,");
        SQLS.append("  `Browser` varchar(45) NOT NULL,");
        SQLS.append("  `BrowserVersion` varchar(20) DEFAULT NULL,");
        SQLS.append("  `Platform` varchar(45) DEFAULT NULL,");
        SQLS.append("  `ManualURL` tinyint(1) NOT NULL DEFAULT '0',");
        SQLS.append("  `ManualHost` varchar(255) DEFAULT NULL,");
        SQLS.append("  `ManualContextRoot` varchar(255) DEFAULT NULL,");
        SQLS.append("  `ManualLoginRelativeURL` varchar(255) DEFAULT NULL,");
        SQLS.append("  `ManualEnvData` varchar(255) DEFAULT NULL,");
        SQLS.append("  `Tag` varchar(255) NOT NULL,");
        SQLS.append("  `OutputFormat` varchar(20) NOT NULL DEFAULT 'gui',");
        SQLS.append("  `Screenshot` int(11) NOT NULL DEFAULT '0',");
        SQLS.append("  `Verbose` int(11) NOT NULL DEFAULT '0',");
        SQLS.append("  `Timeout` mediumtext,");
        SQLS.append("  `Synchroneous` tinyint(1) NOT NULL DEFAULT '0',");
        SQLS.append("  `PageSource` int(11) NOT NULL DEFAULT '1',");
        SQLS.append("  `SeleniumLog` int(11) NOT NULL DEFAULT '1',");
        SQLS.append("  `RequestDate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,");
        SQLS.append("  `Proceeded` tinyint(1) NOT NULL DEFAULT '0',");
        SQLS.append("  PRIMARY KEY (`ID`),");
        SQLS.append("  KEY `IX_testcaseexecution_01` (`Test`,`TestCase`,`Country`),");
        SQLS.append("  KEY `IX_testcaseexecution_02` (`Tag`),");
        SQLS.append("  CONSTRAINT `FK_testcaseexecutionqueue_01` FOREIGN KEY (`Test`, `TestCase`, `Country`) REFERENCES `testcasecountry` (`Test`, `TestCase`, `Country`) ON DELETE CASCADE ON UPDATE CASCADE");
        SQLS.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8;");
        SQLInstruction.add(SQLS.toString());

// Add documentation for ManualExecution parameter in run page
//-- ------------------------ 547
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

// Add invariant ManualExecution
//-- ------------------------ 548    	
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) VALUES ");
        SQLS.append("('INVARIANTPRIVATE', 'MANUALEXECUTION', '470', '', ''),");
        SQLS.append("('MANUALEXECUTION', 'Y', '2', 'Manual Execution', ''),");
        SQLS.append("('MANUALEXECUTION', 'N', '1', 'Automatic Execution', '');");
        SQLInstruction.add(SQLS.toString());

// Add Start index on execution table in order to speedup purge process.
//-- ------------------------ 549 552    	
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepactioncontrolexecution` ADD INDEX `IX_testcasestepactioncontrolexecution_01` (`Start` ASC);");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepactionexecution` ADD INDEX `IX_testcasestepactionexecution_01` (`Start` ASC);");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepexecution` ADD INDEX `IX_testcasestepexecution_01` (`Start` ASC);");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcaseexecutionwwwdet` ADD INDEX `IX_testcaseexecutionwwwdet_01` (`Start` ASC);");
        SQLInstruction.add(SQLS.toString());

// Add Invariant for new control verify element is equal to another.
//-- ------------------------ 553
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`) VALUES ");
        SQLS.append("('CONTROL', 'verifyElementEquals', 44, 'verifyElementEquals');");
        SQLInstruction.add(SQLS.toString());

// Update control verify element is equal to another sorting.
//-- ------------------------ 554
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `invariant` SET `sort`='32' WHERE `idname`='CONTROL' and`value`='verifyElementEquals';");
        SQLInstruction.add(SQLS.toString());

// Add invariant for new controls verifyElementDifferent, verifyIntegerEquals and verifyIntegerDifferent.
//-- ------------------------ 555
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`) VALUES ");
        SQLS.append("('CONTROL', 'verifyElementDifferent', 33, 'verifyElementDifferent'),");
        SQLS.append("('CONTROL', 'verifyIntegerEquals', 18, 'verifyIntegerEquals'),");
        SQLS.append("('CONTROL', 'verifyIntegerDifferent', 19, 'verifyIntegerDifferent');");
        SQLInstruction.add(SQLS.toString());

//Add documentation for new previously added controls.
//-- ------------------------ 556
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

// Increase soaplibrary's Method column size to 255 characters.
//-- ------------------------ 557
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `soaplibrary` CHANGE COLUMN `Method` `Method` VARCHAR(255) NULL DEFAULT NULL ;");
        SQLInstruction.add(SQLS.toString());

// Add Invariant for new control verify text not in element.
// -- ------------------------ 558
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`) VALUES ");
        SQLS.append("('CONTROL', 'verifyTextNotInElement', 41, 'verifyTextNotInElement');");
        SQLInstruction.add(SQLS.toString());

// Add documentation for new previously added controls.
// -- ------------------------ 559
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

// Add ReturnMessage on stepExecution table.
// -- ------------------------ 560
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepexecution` ");
        SQLS.append(" ADD COLUMN `ReturnMessage` VARCHAR(500) NULL DEFAULT NULL AFTER `ReturnCode`;");
        SQLInstruction.add(SQLS.toString());

// Add last_updaed column.
// -- ------------------------ 561 >> 567                
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `test` ");
        SQLS.append("ADD COLUMN `last_modified` TIMESTAMP NULL DEFAULT 0;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcase` ");
        SQLS.append("ADD COLUMN `last_modified` TIMESTAMP NULL DEFAULT 0;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasecountry` ");
        SQLS.append("ADD COLUMN `last_modified` TIMESTAMP NULL DEFAULT 0;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasecountryproperties` ");
        SQLS.append("ADD COLUMN `last_modified` TIMESTAMP NULL DEFAULT 0;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestep` ");
        SQLS.append("ADD COLUMN `last_modified` TIMESTAMP NULL DEFAULT 0;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepaction` ");
        SQLS.append("ADD COLUMN `last_modified` TIMESTAMP NULL DEFAULT 0;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepactioncontrol` ");
        SQLS.append("ADD COLUMN `last_modified` TIMESTAMP NULL DEFAULT 0;");
        SQLInstruction.add(SQLS.toString());

// Add ScreenshotFilename column on testcasestepaction and testcasestepactioncontrol tables.
// -- ------------------------ 568 >> 569                
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepaction` ");
        SQLS.append("ADD COLUMN `ScreenshotFileName` VARCHAR(150) NULL DEFAULT NULL AFTER `Description`;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepactioncontrol` ");
        SQLS.append("ADD COLUMN `ScreenshotFileName` VARCHAR(150) NULL DEFAULT NULL AFTER `Fatal`;");
        SQLInstruction.add(SQLS.toString());

// Add propertytype getFromJson in Invariant table.
// -- ------------------------ 570 
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) ");
        SQLS.append(" VALUES ('PROPERTYTYPE', 'getFromJson', '70', 'Getting value from a Json file', '');");
        SQLInstruction.add(SQLS.toString());

// Add documentation for getFromJson property.
// -- ------------------------ 571 
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

// Add Solr_url parameter.
// -- ------------------------ 572 
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `parameter` (`system`, `param`,`value`, `description`) VALUES ('', 'solr_url','', 'URL of Solr search Engine used on Search Testcase Page. Value is empty if no Solr implementation is available');");
        SQLInstruction.add(SQLS.toString());

// Add Thread Pool Size parameter.
// -- ------------------------ 573                
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `parameter` (`system`, `param`, `value`, `description`) VALUES ('', 'cerberus_execution_threadpool_size', '10', 'Number of Simultaneous execution handled by Cerberus');");
        SQLInstruction.add(SQLS.toString());

// Add Column Comment,Retries and manualExecution in TestCaseExecutionQueue table.
// -- ------------------------ 574                
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcaseexecutionqueue` ");
        SQLS.append("ADD COLUMN `comment` VARCHAR(250) NULL DEFAULT NULL AFTER `proceeded`, ");
        SQLS.append("ADD COLUMN `retries` TINYINT(1) NOT NULL DEFAULT '0' AFTER `comment`,");
        SQLS.append("ADD COLUMN `manualexecution` VARCHAR(1) NOT NULL DEFAULT 'N' AFTER `retries`;");
        SQLInstruction.add(SQLS.toString());

// Add Column Comment in TestCaseExecutionQueue table.
// -- ------------------------ 575                
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) VALUES ('OUTPUTFORMAT', 'redirectToReport', '4', 'Go to ReportByTag page', '');");
        SQLInstruction.add(SQLS.toString());

// Add Column Comment in TestCaseExecutionQueue table.
// -- ------------------------ 576               
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

// Add Column Comment in TestCaseExecutionQueue table.
// -- ------------------------ 577               
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) VALUES ");
        SQLS.append("('INVARIANTPRIVATE', 'RETRIES', '470', '', ''), ");
        SQLS.append("('RETRIES', '0', '10', 'Do not retry in case of Not OK', ''), ");
        SQLS.append("('RETRIES', '1', '20', 'Retry 1 time in case of Not OK', ''), ");
        SQLS.append("('RETRIES', '2', '30', 'Retry 2 times in case of Not OK', ''), ");
        SQLS.append("('RETRIES', '3', '40', 'Retry 3 times in case of Not OK', '');");
        SQLInstruction.add(SQLS.toString());

// Add Column UserAgent in Robot Table.
// -- ------------------------ 578               
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `robot` ");
        SQLS.append("ADD COLUMN `useragent` VARCHAR(250) NOT NULL DEFAULT '' AFTER `active`;");
        SQLInstruction.add(SQLS.toString());

// Add Column Domain in countryenvironmentparameters Table.
// -- ------------------------ 579 -> 581
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `countryenvironmentparameters` ");
        SQLS.append("ADD COLUMN `domain` VARCHAR(150) NOT NULL DEFAULT '' AFTER `IP`;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

// Add new property type that is able to retrieve data values from a property that is specified in the library
// -- ------------------------ 582-583               
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) ");
        SQLS.append(" VALUES ('PROPERTYTYPE', 'getFromDataLib', '75', 'Determines the data value associated with a library entry', 'Data value'); ");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`) ");
        SQLS.append(" VALUES ('USERGROUP', 'TestDataManager', '130', 'User that can manage the testdatalibrary'); ");
        SQLInstruction.add(SQLS.toString());

// Enlarge Property column in testcasestepaction table.
// -- ------------------------ 584
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepaction` ");
        SQLS.append(" CHANGE COLUMN `Property` `Property` VARCHAR(150) NULL DEFAULT NULL ");
        SQLInstruction.add(SQLS.toString());

// Creating the new group 'TestDataManager' from the group 'Test'.
// -- ------------------------ 585
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO usergroup SELECT distinct Login, 'TestDataManager' FROM usergroup where GroupName in ('Test');");
        SQLInstruction.add(SQLS.toString());

// Adding Language code to documentation table in order to support multi language GUI.
// -- ------------------------ 586
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `documentation` ADD COLUMN `Lang` VARCHAR(45) NOT NULL DEFAULT 'en' AFTER `DocValue`, DROP PRIMARY KEY, ADD PRIMARY KEY (`DocTable`, `DocField`, `DocValue`, `Lang`);");
        SQLInstruction.add(SQLS.toString());

// Adding FUNCTION as Public invariant.
// -- ------------------------ 587
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`) ");
        SQLS.append(" VALUES ('INVARIANTPUBLIC', 'FUNCTION', '400', '');");
        SQLInstruction.add(SQLS.toString());

// Adding LANGUAGE invariant.
// -- ------------------------ 588
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) ");
        SQLS.append(" VALUES ('INVARIANTPRIVATE', 'LANGUAGE', '500', '', ''),");
        SQLS.append("        ('LANGUAGE', 'en', '100', 'English', 'English');");
        SQLInstruction.add(SQLS.toString());

// Adding Language column to the user table.
// -- ------------------------ 589
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `user` ADD COLUMN `Language` VARCHAR(45) NULL DEFAULT 'en' AFTER `Team`;  ");
        SQLInstruction.add(SQLS.toString());

// New updated Documentation.
//-- ------------------------ 000 590 - 591
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

// Adding GetFromJS property type.
// -- ------------------------ 592
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) ");
        SQLS.append(" VALUES ('PROPERTYTYPE', 'getFromJS', '37', 'Getting data from javascript variable', '');");
        SQLInstruction.add(SQLS.toString());

// Adding Invariant sizeScreen.
// -- ------------------------ 593
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) ");
        SQLS.append(" VALUES ('INVARIANTPUBLIC', 'SCREENSIZE', '400', '', ''),");
        SQLS.append("('SCREENSIZE', '320*480', '10', '320 px /  480 px', ''),");
        SQLS.append("('SCREENSIZE', '360*640', '20', '360 px /  640 px', ''),");
        SQLS.append("('SCREENSIZE', '1024*768', '30', '1024 px /  768 px', ''),");
        SQLS.append("('SCREENSIZE', '1280*600', '40', '1280 px /  600 px', ''),");
        SQLS.append("('SCREENSIZE', '1280*800', '50', '1280 px /  800 px', ''),");
        SQLS.append("('SCREENSIZE', '1280*980', '60', '1280 px /  980 px', ''),");
        SQLS.append("('SCREENSIZE', '1920*900', '70', '1920 px /  900 px', '');");
        SQLInstruction.add(SQLS.toString());

// Adding Invariant sizeScreen.
// -- ------------------------ 594
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

// Adding sizeScreen into testcaseexecution table.
// -- ------------------------ 595
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcaseexecution`  ");
        SQLS.append("ADD COLUMN `screensize` VARCHAR(45) NULL DEFAULT NULL AFTER `Executor`;");
        SQLInstruction.add(SQLS.toString());

// Adding global documentation for confirmation buttons and dataTable.
// -- ------------------------ 596
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

// Adding global documentation for Header.
// -- ------------------------ 597
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

        // Adding global documentation for Header.
        // -- ------------------------ 598
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

        // Adding documentation for Footer.
        // -- ------------------------ 599 -- 601
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

        // Adding documentation for Footer.
        // -- ------------------------ 602
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

// New updated Documentation.
//-- ------------------------ 603-604
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

// New updated Documentation.
//-- ------------------------ 605-606
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

// New updated Documentation.
//-- ------------------------ 607-608
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

// New updated Documentation.
//-- ------------------------ 609-610
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

        // New Documentation for upload dialog and for the multiselect component.
        //-- ------------------------ 611
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

        // New Documentation for the page Test Data Library - EN version
        //-- ------------------------ 612
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

        // Increase log table limitation.
        //-- ------------------------ 613
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `logevent` CHANGE COLUMN `LogEventID` `LogEventID` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT ;");
        SQLInstruction.add(SQLS.toString());

        // Homogenise column sizes.
        //-- ------------------------ 614
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcaseexecutionqueue` ");
        SQLS.append("CHANGE COLUMN `Robot` `Robot` VARCHAR(100) NULL DEFAULT NULL ,");
        SQLS.append("CHANGE COLUMN `BrowserVersion` `BrowserVersion` VARCHAR(45) NULL DEFAULT NULL ;");
        SQLInstruction.add(SQLS.toString());

        // Homogenise column sizes.
        //-- ------------------------ 615
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcaseexecution` ");
        SQLS.append("CHANGE COLUMN `Browser` `Browser` VARCHAR(45) NULL DEFAULT NULL ,");
        SQLS.append("CHANGE COLUMN `Version` `Version` VARCHAR(45) NOT NULL DEFAULT '' ,");
        SQLS.append("CHANGE COLUMN `Platform` `Platform` VARCHAR(45) NOT NULL DEFAULT '' ,");
        SQLS.append("CHANGE COLUMN `BrowserFullVersion` `BrowserFullVersion` VARCHAR(200) NULL DEFAULT '' ;");
        SQLInstruction.add(SQLS.toString());

        // Change Deploy Type Action on delete to avoid cascade All Applications and TestCases.
        //-- ------------------------ 616-617
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `application` ");
        SQLS.append("DROP FOREIGN KEY `FK_application_01`;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `application` ");
        SQLS.append("ADD CONSTRAINT `FK_application_01`");
        SQLS.append("  FOREIGN KEY (`deploytype`) REFERENCES `deploytype` (`deploytype`) ON DELETE SET NULL ON UPDATE CASCADE;");
        SQLInstruction.add(SQLS.toString());

        // New Documentation for the page TestCase - EN version
        //-- ------------------------ 618
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

        // Documentation entries for Log Viewer page.
        //-- ------------------------ 619
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

        // Documentation entries for Reporting by tag page.
        //-- ------------------------ 620
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

        // Documentation entries for Reporting by tag page.
        //-- ------------------------ 621
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

        // Documentation entries for Robot page.
        //-- ------------------------ 622
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

        // New updated Documentation.
        //-- ------------------------ 623-624
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

        // Documentation entries for Homepage.
        //-- ------------------------ 625
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

        // Documentation entries for Homepage.
        //-- ------------------------ 626
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

        // Documentation entries for Homepage.
        //-- ------------------------ 627-629
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

        // Added private invariant for test active and automated.
        //-- ------------------------ 630
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (idname, value, sort, description, VeryShortDesc) VALUES ");
        SQLS.append("('INVARIANTPRIVATE', 'TESTACTIVE', '510', '', ''),");
        SQLS.append("('INVARIANTPRIVATE', 'TESTAUTOMATED', '520', '', ''),");
        SQLS.append("('TESTACTIVE', 'Y', '10', 'Active', ''),");
        SQLS.append("('TESTACTIVE', 'N', '20', 'Disable', ''),");
        SQLS.append("('TESTAUTOMATED', 'Y', '10', 'Automated', ''),");
        SQLS.append("('TESTAUTOMATED', 'N', '20', 'Not automated', '');");
        SQLInstruction.add(SQLS.toString());

        // Documentation entries for Test page.
        //-- ------------------------ 631
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

        // Documentation entries for Test page.
        //-- ------------------------ 632-633
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

        // Documentation entries for Test Case page - useStep option.
        //-- ------------------------ 634
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

        // Documentation entries for Test Case page - tooltips for controls and actions.
        //-- ------------------------ 635
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

        // Enlarge Page column on Logevent table in order to support log of long Servlet.
        //-- ------------------------ 636
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `logevent` CHANGE COLUMN `Page` `Page` VARCHAR(200) NULL DEFAULT NULL ;");
        SQLInstruction.add(SQLS.toString());

        // Documentation entries for Reporting By Tag
        //-- ------------------------ 637
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

        // Insert invariant executeSqlUpdate, executeSqlStoredProcedure and skipAction
        //-- ------------------------ 638
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) ");
        SQLS.append(" VALUES ('ACTION', 'executeSqlUpdate', '230', 'Execute SQL Script (Update, Delete, Insert)', ''),");
        SQLS.append(" ('ACTION', 'executeSqlStoredProcedure', '240', 'Execute Stored Procedure', ''),");
        SQLS.append(" ('ACTION', 'skipAction', '250', 'Skip Action', '');");
        SQLInstruction.add(SQLS.toString());

        // Documentation entries for executeSqlUpdate, executeSqlStoredProcedure and skipAction
        //-- ------------------------  639
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

        // Documentation entries update for reporting by tag
        //-- ------------------------  640-641
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

        // Enlarge Method column
        //-- ------------------------  642
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testdatalib` CHANGE COLUMN `Method` `Method` VARCHAR(255) NOT NULL DEFAULT '' ;");
        SQLInstruction.add(SQLS.toString());

        // Enlarge Doc Label column
        //-- ------------------------  643
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `documentation` CHANGE COLUMN `DocLabel` `DocLabel` VARCHAR(300) NULL DEFAULT NULL ;");
        SQLInstruction.add(SQLS.toString());

        // New updated Documentation.
        //-- ------------------------ 643-644
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

        // Max size for screenshot
        //-- ------------------------  645
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `parameter` (`system`, `param`, `value`, `description`) VALUES ('', 'cerberus_screenshot_max_size', '1048576', 'Max size in bytes for a screenshot take while test case execution');");
        SQLInstruction.add(SQLS.toString());

        // New documentation entries for TestCaseList 
        //-- ------------------------ 646
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

        // New documentation entries for header
        //-- ------------------------ 647-649
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

        // Removed empty group.
        //-- ------------------------ 650-651
        SQLS = new StringBuilder();
        SQLS.append("DELETE FROM `invariant` WHERE `idname`='GROUP' and`value`='';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE testcase SET `group`='MANUAL' WHERE `group` = '' or `group` is null;");
        SQLInstruction.add(SQLS.toString());

        // Adding doc.
        //-- ------------------------ 652
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

        // New updated Documentation.
        //-- ------------------------ 652-653
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

        // Adding Creator column into countryenvparam_log table, adding an index based on Build Revision and adding a Description field in countryenvparam table.
        //-- ------------------------ 654-656
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `countryenvparam_log` ADD COLUMN `Creator` VARCHAR(10) NULL DEFAULT NULL AFTER `datecre`;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `countryenvparam_log` ADD INDEX `FK_countryenvparam_log_02_IX` (`system` ASC, `Build` ASC, `Revision` ASC );");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `countryenvparam` ADD COLUMN `Description` VARCHAR(200) NOT NULL DEFAULT '' AFTER `Environment`;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

        // Documentation for ReportExecutionByTag summaryTable and export data
        //-- ------------------------ 657
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

        // Documentation for the test data library 
        //-- ------------------------ 658
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

        // Documentation update
        //-- ------------------------ 659
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

        // Adding Technical Key to testdatalibdata table
        //-- ------------------------ 660
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testdatalibdata` ");
        SQLS.append(" ADD COLUMN `TestDataLibDataID` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT FIRST, ");
        SQLS.append(" DROP PRIMARY KEY, ");
        SQLS.append(" ADD PRIMARY KEY (`TestDataLibDataID`), ");
        SQLS.append(" ADD UNIQUE INDEX `IX_testdatalibdata_01` (`TestDataLibID` ASC, `SubData` ASC); ");
        SQLInstruction.add(SQLS.toString());

        // Documentation for duplicate test data library entry
        //-- ------------------------ 664
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

        // New updated Documentation.
        //-- ------------------------ 665-666
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

        // New invariant.
        //-- ------------------------ 667
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`)");
        SQLS.append("VALUES ('MNTACTIVE', 'N', '20', 'Disable', ''),");
        SQLS.append("       ('MNTACTIVE', 'Y', '10', 'Active', '');");
        SQLInstruction.add(SQLS.toString());

        // Tracability on Testdatalib object.
        //-- ------------------------ 668
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testdatalib` ");
        SQLS.append("ADD COLUMN `Creator` VARCHAR(45) NOT NULL DEFAULT '' AFTER `Description`,");
        SQLS.append("ADD COLUMN `Created` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER `Creator`,");
        SQLS.append("ADD COLUMN `LastModifier` VARCHAR(45) NOT NULL DEFAULT '' AFTER `Created`,");
        SQLS.append("ADD COLUMN `LastModified` TIMESTAMP NOT NULL DEFAULT '2000-01-01 00:00:00' AFTER `LastModifier`;");
        SQLInstruction.add(SQLS.toString());

        // New updated Documentation.
        //-- ------------------------ 669-670
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

        // Updated Beta on getFromDataLib property.
        //-- ------------------------ 671
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `invariant` SET `value`='getFromDataLib_BETA', `description`='[Beta] Determines the data value associated with a library entry' ");
        SQLS.append(" WHERE `idname`='PROPERTYTYPE' and`value`='getFromDataLib'; ");
        SQLInstruction.add(SQLS.toString());

        // Adding Beta version of actions callSoapWithBase_BETA callSoap_BETA.
        //-- ------------------------ 672
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) ");
        SQLS.append("VALUES ('ACTION', 'callSoap_BETA', '900', '[BETA] callSoap', ''),");
        SQLS.append("    ('ACTION', 'callSoapWithBase_BETA', '910', '[BETA] callSoapWithBase', '');");
        SQLInstruction.add(SQLS.toString());

        // Adding takeScreenshot control to replace the action.
        //-- ------------------------ 673
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) ");
        SQLS.append("VALUES ('CONTROL', 'takeScreenshot', '100', 'Take a screenshot.', '');");
        SQLInstruction.add(SQLS.toString());

        // Update Action descrition on deprecated actions.
        //-- ------------------------ 674-677
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `invariant` SET `description`='[DEPRECATED] takeScreenshot' WHERE `idname`='ACTION' and`value`='takeScreenshot';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `invariant` SET `description`='[DEPRECATED] clickAndWait' WHERE `idname`='ACTION' and`value`='clickAndWait';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `invariant` SET `description`='[DEPRECATED] enter' WHERE `idname`='ACTION' and`value`='enter';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `invariant` SET `description`='[DEPRECATED] selectAndWait' WHERE `idname`='ACTION' and`value`='selectAndWait';");
        SQLInstruction.add(SQLS.toString());

        // New updated Documentation.
        //-- ------------------------ 678-679
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

        // Increased country and application column size..
        //-- ------------------------ 680-720
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testdata` ");
        SQLS.append(" CHANGE COLUMN `Application` `Application` VARCHAR(200) NOT NULL DEFAULT '' ,");
        SQLS.append(" CHANGE COLUMN `Country` `Country` VARCHAR(45) NOT NULL DEFAULT '' ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcase` ");
        SQLS.append(" DROP FOREIGN KEY `FK_testcase_02`;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcase` ");
        SQLS.append(" CHANGE COLUMN `Application` `Application` VARCHAR(200) NULL DEFAULT NULL ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcaseexecution` ");
        SQLS.append(" DROP FOREIGN KEY `FK_testcaseexecution_02`;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcaseexecution` ");
        SQLS.append(" CHANGE COLUMN `Country` `Country` VARCHAR(45) NULL DEFAULT NULL ,");
        SQLS.append(" CHANGE COLUMN `Application` `Application` VARCHAR(200) NULL DEFAULT NULL ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `countryenvironmentparameters` ");
        SQLS.append(" DROP FOREIGN KEY `FK_countryenvironmentparameters_01`,");
        SQLS.append(" DROP FOREIGN KEY `FK_countryenvironmentparameters_02`;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `countryenvironmentparameters` ");
        SQLS.append(" CHANGE COLUMN `Country` `Country` VARCHAR(45) NOT NULL ,");
        SQLS.append(" CHANGE COLUMN `Application` `Application` VARCHAR(200) NOT NULL ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `buildrevisionparameters` ");
        SQLS.append(" DROP FOREIGN KEY `FK_buildrevisionparameters_01`;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `buildrevisionparameters` ");
        SQLS.append(" CHANGE COLUMN `Application` `Application` VARCHAR(200) NULL DEFAULT NULL ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `application` ");
        SQLS.append(" CHANGE COLUMN `Application` `Application` VARCHAR(200) NOT NULL ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcaseexecutionqueue` ");
        SQLS.append(" DROP FOREIGN KEY `FK_testcaseexecutionqueue_01`;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcaseexecutionqueue` ");
        SQLS.append(" CHANGE COLUMN `Country` `Country` VARCHAR(45) NOT NULL ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasecountryproperties` ");
        SQLS.append(" DROP FOREIGN KEY `FK_testcasecountryproperties_01`;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasecountryproperties` ");
        SQLS.append("CHANGE COLUMN `Country` `Country` VARCHAR(45) NOT NULL ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasecountry` ");
        SQLS.append("CHANGE COLUMN `Country` `Country` VARCHAR(45) NOT NULL ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testdatalib` ");
        SQLS.append("CHANGE COLUMN `Country` `Country` VARCHAR(45) NOT NULL DEFAULT '' ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `buildrevisionbatch` ");
        SQLS.append("DROP FOREIGN KEY `FK_buildrevisionbatch_02`;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `buildrevisionbatch` ");
        SQLS.append("CHANGE COLUMN `Country` `Country` VARCHAR(45) NULL DEFAULT NULL ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `countryenvdeploytype` ");
        SQLS.append("DROP FOREIGN KEY `FK_countryenvdeploytype_01`;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `countryenvdeploytype` ");
        SQLS.append("CHANGE COLUMN `Country` `Country` VARCHAR(45) NOT NULL ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `countryenvironmentdatabase` ");
        SQLS.append("DROP FOREIGN KEY `FK_countryenvironmentdatabase_01`;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `countryenvironmentdatabase` ");
        SQLS.append("CHANGE COLUMN `Country` `Country` VARCHAR(45) NOT NULL ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `countryenvparam` ");
        SQLS.append("CHANGE COLUMN `Country` `Country` VARCHAR(45) NOT NULL ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `host` ");
        SQLS.append("DROP FOREIGN KEY `FK_host_01`;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `host` ");
        SQLS.append("CHANGE COLUMN `Country` `Country` VARCHAR(45) NOT NULL ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `countryenvparam_log` ");
        SQLS.append("DROP FOREIGN KEY `FK_countryenvparam_log_01`;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `countryenvparam_log` ");
        SQLS.append("CHANGE COLUMN `Country` `Country` VARCHAR(45) NOT NULL ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `countryenvlink` ");
        SQLS.append("DROP FOREIGN KEY `FK_countryenvlink_01`;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `countryenvlink` ");
        SQLS.append("CHANGE COLUMN `Country` `Country` VARCHAR(45) NOT NULL ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `countryenvlink` ");
        SQLS.append("ADD CONSTRAINT `FK_countryenvlink_01` FOREIGN KEY (`system` , `Country` , `Environment`) REFERENCES `countryenvparam` (`system` , `Country` , `Environment`) ON DELETE CASCADE ON UPDATE CASCADE;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcase` ");
        SQLS.append(" ADD CONSTRAINT `FK_testcase_02` FOREIGN KEY (`Application`) REFERENCES `application` (`Application`) ON DELETE CASCADE ON UPDATE CASCADE;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcaseexecution` ");
        SQLS.append(" ADD CONSTRAINT `FK_testcaseexecution_02` FOREIGN KEY (`Application`) REFERENCES `application` (`Application`) ON DELETE CASCADE ON UPDATE CASCADE;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `countryenvironmentparameters` ");
        SQLS.append(" ADD CONSTRAINT `FK_countryenvironmentparameters_01` FOREIGN KEY (`system` , `Country` , `Environment`) REFERENCES `countryenvparam` (`system` , `Country` , `Environment`) ON DELETE CASCADE ON UPDATE CASCADE,");
        SQLS.append(" ADD CONSTRAINT `FK_countryenvironmentparameters_02` FOREIGN KEY (`Application`) REFERENCES `application` (`Application`) ON DELETE CASCADE ON UPDATE CASCADE;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `buildrevisionparameters` ");
        SQLS.append(" ADD CONSTRAINT `FK_buildrevisionparameters_01` FOREIGN KEY (`Application`) REFERENCES `application` (`Application`) ON DELETE CASCADE ON UPDATE CASCADE;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcaseexecutionqueue` ");
        SQLS.append(" ADD CONSTRAINT `FK_testcaseexecutionqueue_01` FOREIGN KEY (`Test` , `TestCase` , `Country`) REFERENCES `testcasecountry` (`Test` , `TestCase` , `Country`) ON DELETE CASCADE ON UPDATE CASCADE;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasecountryproperties` ");
        SQLS.append("ADD CONSTRAINT `FK_testcasecountryproperties_01` FOREIGN KEY (`Test` , `TestCase` , `Country`) REFERENCES `testcasecountry` (`Test` , `TestCase` , `Country`) ON DELETE CASCADE ON UPDATE CASCADE;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `buildrevisionbatch` ");
        SQLS.append("ADD CONSTRAINT `FK_buildrevisionbatch_02` FOREIGN KEY (`system` , `Country` , `Environment`) REFERENCES `countryenvparam` (`system` , `Country` , `Environment`) ON DELETE CASCADE ON UPDATE CASCADE;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `countryenvdeploytype` ");
        SQLS.append("ADD CONSTRAINT `FK_countryenvdeploytype_01` FOREIGN KEY (`system` , `Country` , `Environment`) REFERENCES `countryenvparam` (`system` , `Country` , `Environment`) ON DELETE CASCADE ON UPDATE CASCADE;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `countryenvironmentdatabase` ");
        SQLS.append("ADD CONSTRAINT `FK_countryenvironmentdatabase_01` FOREIGN KEY (`system` , `Country` , `Environment`) REFERENCES `countryenvparam` (`system` , `Country` , `Environment`) ON DELETE CASCADE ON UPDATE CASCADE;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `host` ");
        SQLS.append("ADD CONSTRAINT `FK_host_01` FOREIGN KEY (`system` , `Country` , `Environment`) REFERENCES `countryenvparam` (`system` , `Country` , `Environment`) ON DELETE CASCADE ON UPDATE CASCADE;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `countryenvparam_log` ");
        SQLS.append("ADD CONSTRAINT `FK_countryenvparam_log_01` FOREIGN KEY (`system` , `Country` , `Environment`) REFERENCES `countryenvparam` (`system` , `Country` , `Environment`) ON DELETE CASCADE ON UPDATE CASCADE;");
        SQLInstruction.add(SQLS.toString());

        // Adding time index on log table IX_logevent_01.
        //-- ------------------------ 721
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `logevent` ");
        SQLS.append(" ADD INDEX `IX_logevent_01` (`Time` ASC);");
        SQLInstruction.add(SQLS.toString());

        // rename getFromDataLib to getFromDataLib_BETA.
        //-- ------------------------ 722
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `testcasecountryproperties` ");
        SQLS.append(" SET type='getFromDataLib_BETA' where type='getFromDataLib';");
        SQLInstruction.add(SQLS.toString());

        // Clean data on wrong timestamp.
        //-- ------------------------ 723
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `testdatalib` ");
        SQLS.append(" SET Created = '2000-01-01 00:00:00' WHERE Created = '0000-00-00 00:00:00';");
        SQLInstruction.add(SQLS.toString());

        // New updated Documentation.
        //-- ------------------------ 724-725
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

        // New updated Documentation.
        //-- ------------------------ 726-727
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

        // Enlarging Release column.
        //-- ------------------------ 728
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `buildrevisionparameters` CHANGE COLUMN `Release` `Release` VARCHAR(200) NULL DEFAULT NULL ; ");
        SQLInstruction.add(SQLS.toString());

        // Add collumn repositoryUrl to the buildrevisionparameters table
        //-- ------------------------ 729
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `buildrevisionparameters` ");
        SQLS.append("ADD COLUMN `repositoryurl` VARCHAR(1000) NULL DEFAULT '' AFTER `mavenversion`;");
        SQLInstruction.add(SQLS.toString());

        // Add documentation for repositoryUrl
        //-- ------------------------ 730
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `documentation` (`DocTable`, `DocField`, `DocValue`, `Lang`, `DocLabel`, `DocDesc`) ");
        SQLS.append(" VALUES ('buildrevisionparameters', 'repositoryUrl', '', 'en', 'Repository URL', 'This information corresponds to the URL where the current build of the <code class=\\'doc-crbvvoca\\'>application</code> can be downloaded.<br>It allow to retrieve it in a repository such as Nexus.')");
        SQLS.append(",('buildrevisionparameters', 'repositoryUrl', '', 'fr', 'URL du Dépot', 'Cette information correspond à l\\'URL d\\'où le build de l\\'<code class=\\'doc-crbvvoca\\'>application</code> peut-être téléchargé.<br>Cela permet de retrouver un build spécifique dans un dépot de livrable de type Nexus.');");
        SQLInstruction.add(SQLS.toString());

        // Changing batchinvariant to a new structure.
        //-- ------------------------ 731-738
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `buildrevisionbatch` DROP FOREIGN KEY `FK_buildrevisionbatch_01`;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepbatch` DROP FOREIGN KEY `FK_testcasestepbatch_02`;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `batchinvariant` ADD COLUMN `system` VARCHAR(45) NOT NULL FIRST, DROP COLUMN `Unit`, DROP COLUMN `IncIni`, CHANGE COLUMN `Batch` `Batch` VARCHAR(100) NOT NULL DEFAULT '', CHANGE COLUMN `Description` `Description` VARCHAR(200) NULL DEFAULT NULL ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `buildrevisionbatch` CHANGE COLUMN `Batch` `Batch` VARCHAR(100) NOT NULL ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `buildrevisionbatch` ADD CONSTRAINT `FK_buildrevisionbatch_01` FOREIGN KEY (`Batch`) REFERENCES `batchinvariant` (`Batch`) ON DELETE CASCADE ON UPDATE CASCADE;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepbatch` CHANGE COLUMN `Batch` `Batch` VARCHAR(100) NOT NULL DEFAULT '' ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepbatch` ADD CONSTRAINT `FK_testcasestepbatch_02` FOREIGN KEY (`Batch`) REFERENCES `batchinvariant` (`Batch`) ON DELETE CASCADE ON UPDATE CASCADE;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("insert into batchinvariant select value, concat(`value`,b.batch), b.description from batchinvariant b join invariant where idname='SYSTEM';");
        SQLInstruction.add(SQLS.toString());

        // New updated Documentation.
        //-- ------------------------ 739-740
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

        // New updated Documentation.
        //-- ------------------------ 741-742
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

        // New updated Documentation.
        //-- ------------------------ 743-744
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

        // Remove old Actions that were never implemented.
        //-- ------------------------ 745-746
        SQLS = new StringBuilder();
        SQLS.append("DELETE FROM `invariant` WHERE `idname`='ACTION' and`value` in ('store','removeSelection','waitForPage');");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE testcasestepaction set Action='Unknown' where Action in ('store','removeSelection','waitForPage');");
        SQLInstruction.add(SQLS.toString());

        // New updated Documentation.
        //-- ------------------------ 747-748
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

        // New parameter cerberus_testdatalib_fetchmax.
        //-- ------------------------ 749
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `parameter` (`system`, `param`, `value`, `description`) VALUES ('', 'cerberus_testdatalib_fetchmax', '100', 'Maximum number of fetched records that Cerberus will perform when retrieving a data from SQL Data Library.');");
        SQLInstruction.add(SQLS.toString());

        // New updated Documentation.
        //-- ------------------------ 750-751
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

        // Removed and clean takeScreenshot action.
        //-- ------------------------ 752-753
        SQLS = new StringBuilder();
        SQLS.append("DELETE FROM `invariant` WHERE `idname`='ACTION' and `value`='takeScreenshot';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE testcasestepaction SET Action='skipAction' WHERE Action='takeScreenshot';");
        SQLInstruction.add(SQLS.toString());

        // Added Environment group invariants.
        //-- ------------------------ 754
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) VALUES ");
        SQLS.append("    ('ENVGP', 'DEV', '100', 'Development Environments', 'DEV'),");
        SQLS.append("    ('ENVGP', 'QA', '200', 'Quality Assurance Environments', 'QA'),");
        SQLS.append("    ('ENVGP', 'UAT', '300', 'User Acceptance Test Environments', 'UAT'),");
        SQLS.append("    ('ENVGP', 'PROD', '400', 'Production Environments', 'PROD'),");
        SQLS.append("    ('INVARIANTPRIVATE', 'ENVGP', '530', '', '');");
        SQLInstruction.add(SQLS.toString());

        // Rename Action skipAction to doNothing.
        //-- ------------------------ 755-764
        SQLS = new StringBuilder();
        SQLS.append("UPDATE testcasestepaction SET Action='doNothing' WHERE Action='skipAction';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `invariant` SET `value`='doNothing', `description`='doNothing' WHERE `idname`='ACTION' and`value`='skipAction';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `invariant` SET `value`='mouseLeftButtonPress', `sort`='37' WHERE `idname`='ACTION' and`value`='mouseDown';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `invariant` SET `value`='mouseLeftButtonRelease', `sort`='38', `description`='Selenium Action mouseUp' WHERE `idname`='ACTION' and`value`='mouseUp';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `invariant` SET `sort`='49' WHERE `idname`='ACTION' and`value`='keypress';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `invariant` SET `sort`='31' WHERE `idname`='ACTION' and`value`='clickAndWait';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `invariant` SET `sort`='35' WHERE `idname`='ACTION' and`value`='doubleClick';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `invariant` SET `sort`='55' WHERE `idname`='ACTION' and`value`='switchToWindow';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `invariant` SET `sort`='59' WHERE `idname`='ACTION' and`value`='manageDialog';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `invariant` SET sort=sort*10 where `idname` in ('ACTION', 'CONTROL');");
        SQLInstruction.add(SQLS.toString());

        // New updated Documentation.
        //-- ------------------------ 765-766
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

        // Updated CHAIN invariant.
        //-- ------------------------ 767-768
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `invariant` SET `value`='Y', `description`='Yes' WHERE `idname`='CHAIN' and`value`='0';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `invariant` SET `value`='N', `description`='No' WHERE `idname`='CHAIN' and`value`='1';");
        SQLInstruction.add(SQLS.toString());

        // Add the hideKeyboard action.
        //-- ------------------------ 769
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) VALUES ('ACTION', 'hideKeyboard', '1200', 'hideKeyboard', '');");
        SQLInstruction.add(SQLS.toString());

        // Add the Unknown Control.
        //-- ------------------------ 770
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) VALUES ('CONTROL', 'Unknown', '10', 'Unknown', '');");
        SQLInstruction.add(SQLS.toString());

        // Add the hideKeyboard and update the keyPress action documentation.
        //-- ------------------------ 771-772
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

        // Add the swipe action.
        //-- ------------------------ 773-775
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) VALUES ('ACTION', 'swipe', '1300', 'Swipe mobile screen', '');");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `parameter` (`system`, `param`, `value`, `description`) VALUES ('', 'appium_swipeDuration', '2000', 'The duration for the Appium swipe action');");
        SQLInstruction.add(SQLS.toString());

        // Add the cerberus_notinuse_timeout parameter.
        //-- ------------------------ 776
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `parameter` (`system`, `param`, `value`, `description`) VALUES ('', 'cerberus_notinuse_timeout', '600', 'Integer that correspond to the number of seconds after which, any pending execution (status=PE) will not be considered as pending.');");
        SQLInstruction.add(SQLS.toString());

        // Remove unicity constrain on TestDataLib.
        //-- ------------------------ 777
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testdatalib` ");
        SQLS.append("DROP INDEX `IX_testdatalib_01` ,");
        SQLS.append("ADD INDEX `IX_testdatalib_01` (`Name` ASC, `system` ASC, `Environment` ASC, `Country` ASC) ;");
        SQLInstruction.add(SQLS.toString());

        // Add the RobotCapability table
        //-- ------------------------ 778-779
        SQLS = new StringBuilder();
        SQLS.append("CREATE TABLE `robotcapability` (\n"
                + "  `id` int(11) NOT NULL AUTO_INCREMENT,\n"
                + "  `robot` varchar(100) NOT NULL,\n"
                + "  `capability` varchar(45) NOT NULL,\n"
                + "  `value` varchar(255) NOT NULL,\n"
                + "  PRIMARY KEY (`id`),\n"
                + "  UNIQUE KEY `uq_capability_value_idx` (`capability`,`value`,`robot`),\n"
                + "  KEY `fk_robot_idx` (`robot`),\n"
                + "  CONSTRAINT `fk_robot` FOREIGN KEY (`robot`) REFERENCES `robot` (`robot`) ON DELETE CASCADE ON UPDATE CASCADE\n"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8;\n");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `robotcapability` (`robot`, `value`, `capability`)  \n"
                + "\tSELECT `robot`, `platform`, 'platform' AS `capability` FROM `robot`\n"
                + "    UNION\n"
                + "    SELECT `robot`, `browser`, 'browser' AS `capability` FROM `robot`\n"
                + "    UNION\n"
                + "    SELECT `robot`, `version`, 'version' AS `capability` FROM `robot`");
        SQLInstruction.add(SQLS.toString());

        // Apply changes on RobotCapability indexes/keys to follow naming convention
        //-- ------------------------ 780-782
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `robotcapability` \n"
                + "DROP FOREIGN KEY `fk_robot`;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `robotcapability` \n"
                + "DROP INDEX `uq_capability_value_idx` ,\n"
                + "ADD UNIQUE INDEX `IX_robotcapability_01` (`capability` ASC, `value` ASC, `robot` ASC),\n"
                + "DROP INDEX `fk_robot_idx` ,\n"
                + "ADD INDEX `IX_robotcapability_02` (`robot` ASC);");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `robotcapability` \n"
                + "ADD CONSTRAINT `FK_robotcapability_01`\n"
                + "  FOREIGN KEY (`robot`)\n"
                + "  REFERENCES `robot` (`robot`)\n"
                + "  ON DELETE CASCADE\n"
                + "  ON UPDATE CASCADE;");
        SQLInstruction.add(SQLS.toString());

        //Add IPA application type inside 783
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`) ");
        SQLS.append("VALUES ('APPLITYPE', 'IPA', '50', 'IOS Application');");
        SQLInstruction.add(SQLS.toString());

        // Reverting changes on RobotCapability table
        //-- ------------------------ 784
        SQLS = new StringBuilder();
        SQLS.append("DELETE FROM `robotcapability`;");
        SQLInstruction.add(SQLS.toString());

        // Update testcaseexecution and testcasestepexecution to set default end to null.
        // Update last_modified timestamp default value
        //-- ------------------------ 785 - 794
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcaseexecution` ");
        SQLS.append("CHANGE COLUMN `End` `End` TIMESTAMP NOT NULL DEFAULT '1970-01-01 01:01:01',");
        SQLS.append("CHANGE COLUMN `Start` `Start` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepexecution` ");
        SQLS.append("CHANGE COLUMN `End` `End` TIMESTAMP NOT NULL DEFAULT '1970-01-01 01:01:01',");
        SQLS.append("CHANGE COLUMN `Start` `Start` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testdatalib` ");
        SQLS.append("CHANGE COLUMN `LastModified` `LastModified` TIMESTAMP NOT NULL DEFAULT '1970-01-01 01:01:01';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `test` ");
        SQLS.append("CHANGE COLUMN `last_modified` `last_modified` TIMESTAMP NOT NULL DEFAULT '1970-01-01 01:01:01';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcase` ");
        SQLS.append("CHANGE COLUMN `last_modified` `last_modified` TIMESTAMP NOT NULL DEFAULT '1970-01-01 01:01:01';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasecountry` ");
        SQLS.append("CHANGE COLUMN `last_modified` `last_modified` TIMESTAMP NOT NULL DEFAULT '1970-01-01 01:01:01';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasecountryproperties` ");
        SQLS.append("CHANGE COLUMN `last_modified` `last_modified` TIMESTAMP NOT NULL DEFAULT '1970-01-01 01:01:01';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestep` ");
        SQLS.append("CHANGE COLUMN `last_modified` `last_modified` TIMESTAMP NOT NULL DEFAULT '1970-01-01 01:01:01';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepaction` ");
        SQLS.append("CHANGE COLUMN `last_modified` `last_modified` TIMESTAMP NOT NULL DEFAULT '1970-01-01 01:01:01';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepactioncontrol` ");
        SQLS.append("CHANGE COLUMN `last_modified` `last_modified` TIMESTAMP NOT NULL DEFAULT '1970-01-01 01:01:01';");
        SQLInstruction.add(SQLS.toString());

        // Add description in testcasestepexecution, testcasestepactionexecution
        // and testcasestepactioncontrolexecution tables
        //-- ------------------------ 795 - 798
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepexecution` ");
        SQLS.append("ADD COLUMN `Description` VARCHAR(150) NOT NULL DEFAULT '' AFTER `ReturnMessage`;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepactionexecution` ");
        SQLS.append("ADD COLUMN `Description` VARCHAR(255) NOT NULL DEFAULT '' AFTER `PageSourceFileName`;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepactioncontrolexecution`  ");
        SQLS.append("ADD COLUMN `Description` VARCHAR(255) NOT NULL DEFAULT '' AFTER `PageSourceFilename`;");
        SQLInstruction.add(SQLS.toString());

        //
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `testdatalib` ");
        SQLS.append("SET `LastModified` =  '1970-01-01 01:01:01' WHERE `LastModified` = '0000-00-00 00:00:00';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `test` ");
        SQLS.append("SET `last_modified` =  '1970-01-01 01:01:01' WHERE `last_modified` = '0000-00-00 00:00:00';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `testcase` ");
        SQLS.append("SET `last_modified` =  '1970-01-01 01:01:01' WHERE `last_modified` = '0000-00-00 00:00:00';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `testcasecountry` ");
        SQLS.append("SET `last_modified` =  '1970-01-01 01:01:01' WHERE `last_modified` = '0000-00-00 00:00:00';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `testcasecountryproperties` ");
        SQLS.append("SET `last_modified` =  '1970-01-01 01:01:01' WHERE `last_modified` = '0000-00-00 00:00:00';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `testcasestep` ");
        SQLS.append("SET `last_modified` =  '1970-01-01 01:01:01' WHERE `last_modified` = '0000-00-00 00:00:00';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `testcasestepaction` ");
        SQLS.append("SET `last_modified` =  '1970-01-01 01:01:01' WHERE `last_modified` = '0000-00-00 00:00:00';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `testcasestepactioncontrol` ");
        SQLS.append("SET `last_modified` =  '1970-01-01 01:01:01' WHERE `last_modified` = '0000-00-00 00:00:00';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `testcase` ");
        SQLS.append("SET `TCDateCrea` =  '1970-01-01 01:01:01' WHERE `TCDateCrea` = '0000-00-00 00:00:00';");
        SQLInstruction.add(SQLS.toString());

        // Add main robot capability invariants
        //-- ------------------------ 807
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) VALUES ");
        SQLS.append("('INVARIANTPUBLIC', 'CAPABILITY', '500', 'Robot capabilities', ''), ");
        SQLS.append("('CAPABILITY', 'automationName', '1', 'Automation name, e.g.: Appium)', ''), ");
        SQLS.append("('CAPABILITY', 'deviceName', '2', 'Device name (useful for Appium)', ''), ");
        SQLS.append("('CAPABILITY', 'app', '3', 'Application name (useful for Appium)', ''), ");
        SQLS.append("('CAPABILITY', 'platformName', '4', 'Platform name (useful for Appium)', ''), ");
        SQLS.append("('CAPABILITY', 'platformVersion', '5', 'Platform version (useful for Appium)', ''), ");
        SQLS.append("('CAPABILITY', 'browserName', '6', 'Browser name (useful for Appium)', ''), ");
        SQLS.append("('CAPABILITY', 'autoWebview', '7', 'If auto web view has to be enabled (useful for Appium, e.g.: true) ', '');");
        SQLInstruction.add(SQLS.toString());

        // Add documentation on robot capability
        //-- ------------------------ 808
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

        // Correct property to add the /text() in xpath.
        //-- ------------------------ 809
        SQLS = new StringBuilder();
        SQLS.append("UPDATE testcasecountryproperties SET value2 = concat(value2, '/text()')");
        SQLS.append(" WHERE `type` = 'getFromXML' and value2 not like '%ext()';    ");
        SQLInstruction.add(SQLS.toString());

        // Adding missing index in order to support RANDOMNEW and NOTINUSE
        //-- ------------------------ 810
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcaseexecution` ");
        SQLS.append(" ADD INDEX `IX_testcaseexecution_09` (`Country` ASC, `Environment` ASC, `ControlStatus` ASC), "); // Used for NOTINUSE   
        SQLS.append(" ADD INDEX `IX_testcaseexecution_10` (`Test` ASC, `TestCase` ASC, `Environment` ASC, `Country` ASC, `Build` ASC) ;"); // Used for RANDOMNEW
        SQLInstruction.add(SQLS.toString());

        // Adding Soap URL on database table
        //-- ------------------------ 811
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `countryenvironmentdatabase` ");
        SQLS.append("ADD COLUMN `SoapUrl` VARCHAR(200) NOT NULL DEFAULT ''  AFTER `ConnectionPoolName`;");
        SQLInstruction.add(SQLS.toString());

        // Adding DatabaseUrl on testdatalib table
        //-- ------------------------ 812
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testdatalib` ");
        SQLS.append("ADD COLUMN `DatabaseUrl` VARCHAR(45) NOT NULL DEFAULT '' AFTER `Script`;");
        SQLInstruction.add(SQLS.toString());

        // Adding Action skipAction
        //-- ------------------------ 813
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`) ");
        SQLS.append("VALUES ('ACTION', 'skipAction', '2600', 'skipAction');");
        SQLInstruction.add(SQLS.toString());

        // Adding Reset Password Email Parameters
        //-- ------------------------ 814
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `parameter` (`system`, `param`, `value`, `description`) ");
        SQLS.append("VALUES ('', 'cerberus_notification_forgotpassword_subject', '[Cerberus] Reset your password', 'Subject of Cerberus forgot password notification email.')");
        SQLS.append(", ('', 'cerberus_notification_forgotpassword_body', 'Hello %NAME%<br><br>We\\'ve received a request to reset your Cerberus password.<br><br>%LINK%<br><br>If you didn\\'t request a password reset, not to worry, just ignore this email and your current password will continue to work.<br><br>Cheers,<br>The Cerberus Team', 'Cerberus forgot password notification email body. %LOGIN%, %NAME% and %LINK% can be used as variables.');");
        SQLInstruction.add(SQLS.toString());

        // Adding Column ResetPasswordToken in User Table
        //-- ------------------------ 815
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `user` ");
        SQLS.append("ADD COLUMN `ResetPasswordToken` CHAR(40) NOT NULL DEFAULT '' AFTER `Password`;");
        SQLInstruction.add(SQLS.toString());

        // Add Sort column to test case step related tables (#569)
        //-- ------------------------ 816 - 827 
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestep` ADD COLUMN `Sort` INT(10) UNSIGNED AFTER `Step`;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `testcasestep` SET `Sort` = `Step`;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepaction` ADD COLUMN `Sort` INT(10) UNSIGNED AFTER `Sequence`;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `testcasestepaction` SET `Sort` = `Sequence`;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepactioncontrol` ADD COLUMN `Sort` INT(10) UNSIGNED AFTER `Control`;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `testcasestepactioncontrol` SET `Sort` = `Control`;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepactioncontrolexecution` ADD COLUMN `Sort` INT(10) UNSIGNED AFTER `Control`;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `testcasestepactioncontrolexecution` SET `Sort` = `Control`;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepactionexecution` ADD COLUMN `Sort` INT(10) UNSIGNED AFTER `Sequence`;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `testcasestepactionexecution` SET `Sort` = `Sequence`;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepexecution` ADD COLUMN `Sort` INT(10) UNSIGNED AFTER `Step`;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `testcasestepexecution` SET `Sort` = `Step`;");
        SQLInstruction.add(SQLS.toString());

        // Removed callSoapWithBase_BETA and callSoap_BETA actions.
        //-- ------------------------ 828
        SQLS = new StringBuilder();
        SQLS.append("DELETE from invariant where idname='ACTION' and value in ('callSoapWithBase_BETA','callSoap_BETA');");
        SQLInstruction.add(SQLS.toString());

        // Added flag in order to support forcing Execution status at action level.
        //-- ------------------------ 829-831
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepaction` ADD COLUMN `ForceExeStatus` VARCHAR(45) NOT NULL DEFAULT '' AFTER `Property`;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) VALUES");
        SQLS.append("  ('ACTIONFORCEEXESTATUS', '', '10', 'Standard behaviour.', 'Std Behaviour')");
        SQLS.append(", ('ACTIONFORCEEXESTATUS', 'PE', '20', 'Force the Execution to continue running not impacting the final status whatever the result of the action is.', 'Continue')");
        SQLS.append(", ('INVARIANTPRIVATE', 'ACTIONFORCEEXESTATUS', '540', '', '');");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepactionexecution` ADD COLUMN `ForceExeStatus` VARCHAR(45) NOT NULL DEFAULT '' AFTER `Property`;");
        SQLInstruction.add(SQLS.toString());

        // New updated Documentation.
        //-- ------------------------ 832-833
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

        // New cerberus_automaticexecution_enable parameter.
        //-- ------------------------ 834
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `parameter` (`system`, `param`, `value`, `description`) VALUES ('', 'cerberus_automaticexecution_enable', 'Y', 'Activation boolean in order to activate the automatic executions.Y value will allow execution. Any other value will stop the execution returning an error message..');");
        SQLInstruction.add(SQLS.toString());

        // Updated Description of cerberus_reporting_url parameter.
        //-- ------------------------ 835
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `parameter` SET `description`='URL to Cerberus reporting screen. the following variables can be used : %COUNTRY%, %ENV%,  %APPLI%, %BUILD% and %REV%.' WHERE `system`='' and`param`='cerberus_reporting_url';");
        SQLInstruction.add(SQLS.toString());

        // Updated Description of cerberus_reporting_url parameter.
        //-- ------------------------ 836-839
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepactioncontrol` CHANGE COLUMN `ControlValue` `ControlValue` VARCHAR(2500) NOT NULL DEFAULT ''  ,CHANGE COLUMN `ControlProperty` `ControlProperty` VARCHAR(2500) NULL DEFAULT NULL ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepactioncontrolexecution` CHANGE COLUMN `ControlValue` `ControlValue` VARCHAR(2500) NULL DEFAULT NULL ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepaction` CHANGE COLUMN `Object` `Object` VARCHAR(2500) NOT NULL DEFAULT ''  ,CHANGE COLUMN `Property` `Property` VARCHAR(2500) NULL DEFAULT NULL ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepactionexecution` CHANGE COLUMN `Object` `Object` VARCHAR(2500) NULL DEFAULT NULL  ,CHANGE COLUMN `Property` `Property` VARCHAR(2500) NULL DEFAULT NULL ;");
        SQLInstruction.add(SQLS.toString());

        // Add userPreferences column in user table.
        //-- ------------------------ 840
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `user` ");
        SQLS.append("ADD COLUMN `UserPreferences` TEXT NOT NULL AFTER `Email`;");
        SQLInstruction.add(SQLS.toString());

        // Add the getFromGroovy property type.
        //-- ------------------------ 841
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) VALUES ('PROPERTYTYPE', 'getFromGroovy', '80', 'Getting value from a Groovy script', '');");
        SQLInstruction.add(SQLS.toString());

        // Add filter information for tooltip in documentation table.
        //-- ------------------------ 842
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

        // Add comment on properties.
        //-- ------------------------ 843-845
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasecountryproperties` ");
        SQLS.append("ADD COLUMN `Description` VARCHAR(255) NULL DEFAULT '' AFTER `Property`;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcaseexecutiondata` ");
        SQLS.append("ADD COLUMN `Description` VARCHAR(255) NULL DEFAULT '' AFTER `Property`;");
        SQLInstruction.add(SQLS.toString());

        // Add documentation to the getFromGroovy property type.
        //-- ------------------------ 846
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

        // Clean URLLOGIN column in countryenvironmentparameters table.
        //-- ------------------------ 847-848
        SQLS = new StringBuilder();
        SQLS.append("UPDATE countryenvironmentparameters SET URLLOGIN = '' WHERE URLLOGIN is null;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `countryenvironmentparameters` CHANGE COLUMN `URLLOGIN` `URLLOGIN` VARCHAR(150) NOT NULL DEFAULT '' ;");
        SQLInstruction.add(SQLS.toString());

        // Add columns in testdatalib and testdatatlibdata to related to CSV type.
        // Add CSV TESTDATATYPE invariant
        // Add documentation
        //-- ------------------------ 849-853
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testdatalib` ");
        SQLS.append("ADD COLUMN `CsvUrl` VARCHAR(250) NOT NULL DEFAULT '' AFTER `Envelope`;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testdatalibdata` ");
        SQLS.append("ADD COLUMN `ColumnPosition` VARCHAR(45) NOT NULL DEFAULT '' AFTER `ParsingAnswer`;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`) ");
        SQLS.append("VALUES ('TESTDATATYPE', 'CSV', '40', 'Dynamic test data from CSV file');");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testdatalib` ");
        SQLS.append("ADD COLUMN `Separator` VARCHAR(45) NOT NULL DEFAULT '' AFTER `CsvUrl`;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

        // label table creation
        //-- ------------------------ 854
        SQLS = new StringBuilder();
        SQLS.append("CREATE TABLE `label` (");
        SQLS.append("`Id` INT NOT NULL AUTO_INCREMENT,");
        SQLS.append("`System` VARCHAR(45) NOT NULL DEFAULT '',");
        SQLS.append("`Label` VARCHAR(100) NOT NULL DEFAULT '',");
        SQLS.append("`Color` VARCHAR(45) NOT NULL DEFAULT '',");
        SQLS.append("`ParentLabel` VARCHAR(100) NOT NULL DEFAULT '',");
        SQLS.append("`Description` VARCHAR(250) NOT NULL DEFAULT '',");
        SQLS.append("`UsrCreated` VARCHAR(45) NOT NULL DEFAULT '',");
        SQLS.append("`DateCreated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,");
        SQLS.append("`UsrModif` VARCHAR(45) NOT NULL DEFAULT '',");
        SQLS.append("`DateModif` TIMESTAMP NOT NULL DEFAULT '1970-01-01 01:01:01', ");
        SQLS.append(" PRIMARY KEY (`id`), ");
        SQLS.append(" UNIQUE INDEX `IX_label_01` (`system` ASC, `label` ASC))");
        SQLS.append(" ENGINE=InnoDB DEFAULT CHARSET=utf8;");
        SQLInstruction.add(SQLS.toString());

        // testcaselabel table creation
        //-- ------------------------ 855
        SQLS = new StringBuilder();
        SQLS.append("CREATE TABLE `testcaselabel` (`Id` INT NOT NULL AUTO_INCREMENT,`Test` varchar(45) NOT NULL,`TestCase` varchar(45) NOT NULL,`LabelId` INT NOT NULL,");
        SQLS.append("`UsrCreated` VARCHAR(45) NOT NULL DEFAULT '',");
        SQLS.append("`DateCreated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,");
        SQLS.append("`UsrModif` VARCHAR(45) NOT NULL DEFAULT '',");
        SQLS.append("`DateModif` TIMESTAMP NOT NULL DEFAULT '1970-01-01 01:01:01', ");
        SQLS.append(" PRIMARY KEY (`Id`),");
        SQLS.append(" UNIQUE KEY `IX_testcaselabel_03` (`Test`,`TestCase`,`LabelId`),");
        SQLS.append(" KEY `IX_testcaselabel_01` (`Test`,`TestCase`),");
        SQLS.append(" KEY `IX_testcaselabel_02` (`LabelId`),");
        SQLS.append(" CONSTRAINT `FK_testcaselabel_01` FOREIGN KEY (`Test`, `TestCase`) REFERENCES `testcase` (`Test`, `TestCase`) ON DELETE CASCADE ON UPDATE CASCADE,");
        SQLS.append(" CONSTRAINT `FK_testcaselabel_02` FOREIGN KEY (`LabelId`) REFERENCES `label` (`Id`) ON DELETE CASCADE ON UPDATE CASCADE) ");
        SQLS.append(" ENGINE=InnoDB DEFAULT CHARSET=utf8;");
        SQLInstruction.add(SQLS.toString());

        // Documentation on label
        //-- ------------------------ 856
        SQLS = new StringBuilder();
        SQLS.append("select 1 from DUAL;");
        SQLInstruction.add(SQLS.toString());

        // Add a sample tag
        //-- ------------------------ 857
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO label (`system`,`label`, `color`,`UsrCreated`, `UsrModif`) ");
        SQLS.append("SELECT `value` , 'MyFirstLabel', '#000000' , 'admin' , 'admin' from invariant where idname = 'SYSTEM'");
        SQLInstruction.add(SQLS.toString());

        // Add the "rightClick" action
        //-- ------------------------ 858-859
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) VALUES ('ACTION', 'rightClick', '310', 'Right click on an element', 'Right click');\n");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

        // Documentation on new Test case list page buttons
        //-- ------------------------ 860
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

        // New sql timeout parameters.
        //-- ------------------------ 861
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `parameter` (`system`, `param`, `value`, `description`) VALUES ");
        SQLS.append(" ('', 'cerberus_propertyexternalsql_timeout', '60', 'Integer that correspond to the number of seconds after which, any SQL executed from a property calculation will fail.')");
        SQLS.append(",('', 'cerberus_actionexecutesqlupdate_timeout', '60', 'Integer that correspond to the number of seconds after which, any SQL triggered from action executeSqlUpdate will fail.')");
        SQLInstruction.add(SQLS.toString());

        // New Index column in testcaseexecutiondata in order to support multirow property.
        //-- ------------------------ 862
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcaseexecutiondata` ");
        SQLS.append("ADD COLUMN `Index` INT NOT NULL DEFAULT 1 AFTER `Property`,");
        SQLS.append("DROP PRIMARY KEY, ADD PRIMARY KEY (`ID`, `Property`, `Index`) ;");
        SQLInstruction.add(SQLS.toString());

        // Adding generic variable columns on application environment table (countryenvironmentparameters).
        //-- ------------------------ 863
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `countryenvironmentparameters` ");
        SQLS.append("ADD COLUMN `Var1` VARCHAR(200) NOT NULL DEFAULT '' AFTER `URLLOGIN`,");
        SQLS.append("ADD COLUMN `Var2` VARCHAR(200) NOT NULL DEFAULT '' AFTER `Var1`,");
        SQLS.append("ADD COLUMN `Var3` VARCHAR(200) NOT NULL DEFAULT '' AFTER `Var2`,");
        SQLS.append("ADD COLUMN `Var4` VARCHAR(200) NOT NULL DEFAULT '' AFTER `Var3`;");
        SQLInstruction.add(SQLS.toString());

        // Make getFromDataLib official.
        //-- ------------------------ 864
        SQLS = new StringBuilder();
        SQLS.append("UPDATE testcasecountryproperties SET `Type` = 'getFromDataLib' where `Type` = 'getFromDataLib_BETA';");
        SQLInstruction.add(SQLS.toString());

        // New updated Documentation.
        //-- ------------------------ 865-866
        SQLS = new StringBuilder();
        SQLS.append("select 1 from DUAL;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("select 1 from DUAL;");
        SQLInstruction.add(SQLS.toString());

        // Make getFromDataLib official.
        //-- ------------------------ 867
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `invariant` SET `value`='getFromDataLib' WHERE `idname`='PROPERTYTYPE' and `value`='getFromDataLib_BETA';");
        SQLInstruction.add(SQLS.toString());

        // Adding Url Source for CSV datasource..
        //-- ------------------------ 868-869
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `countryenvironmentdatabase` ADD COLUMN `CsvUrl` VARCHAR(200) NOT NULL DEFAULT '' AFTER `SoapUrl`;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testdatalib` ADD COLUMN `DatabaseCsv` VARCHAR(45) NOT NULL DEFAULT '' AFTER `Envelope`;");
        SQLInstruction.add(SQLS.toString());

        // Rename STATIC to INTERNAL in TestDataLib.
        //-- ------------------------ 870-871
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `testdatalib` SET Type='INTERNAL' WHERE `Type`='STATIC';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `invariant` SET `value`='INTERNAL', `description`='Internal Cerberus test data.' WHERE `idname`='TESTDATATYPE' and`value`='STATIC';");
        SQLInstruction.add(SQLS.toString());

        // New table to host all file saved during execution.
        //-- ------------------------ 872
        SQLS = new StringBuilder();
        SQLS.append("CREATE TABLE `testcaseexecutionfile` (");
        SQLS.append(" `ID` BIGINT(20) NOT NULL AUTO_INCREMENT ,");
        SQLS.append(" `ExeID` BIGINT(20) unsigned NOT NULL ,");
        SQLS.append(" `Level` VARCHAR(150) NOT NULL DEFAULT '' ,");
        SQLS.append(" `FileDesc` VARCHAR(100) NOT NULL DEFAULT '' ,");
        SQLS.append(" `Filename` VARCHAR(150) NOT NULL DEFAULT '' ,");
        SQLS.append(" `FileType` VARCHAR(45) NOT NULL DEFAULT '' ,");
        SQLS.append(" `UsrCreated` VARCHAR(45) NOT NULL DEFAULT '',");
        SQLS.append(" `DateCreated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,");
        SQLS.append(" `UsrModif` VARCHAR(45) NOT NULL DEFAULT '',");
        SQLS.append(" `DateModif` TIMESTAMP NOT NULL DEFAULT '1970-01-01 01:01:01', ");
        SQLS.append(" PRIMARY KEY (`ID`) ,");
        SQLS.append(" UNIQUE INDEX `IX_testcaseexecutionfile_01` (`ExeID` ASC, `Level` ASC, `FileDesc` ASC) ,");
        SQLS.append(" CONSTRAINT `FK_testcaseexecutionfile_01` FOREIGN KEY (`ExeID`) REFERENCES `testcaseexecution` (`ID`) ON DELETE CASCADE ON UPDATE CASCADE");
        SQLS.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8;");
        SQLInstruction.add(SQLS.toString());

        // Updated cerberus_picture_path parameter.
        //-- ------------------------ 873-874
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `parameter` SET `param`='cerberus_mediastorage_path', `description`='Path to store the Cerberus Media files (like Selenium Screenshot or SOAP requests and responses).' WHERE `param`='cerberus_picture_path';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `parameter` SET `param`='cerberus_mediastorage_url', `description`='Link (URL) to the Cerberus Media Files. That link should point to cerberus_mediastorage_path location.' WHERE `system`='' and`param`='cerberus_picture_url';");
        SQLInstruction.add(SQLS.toString());

        // Migrate old Screenshot and PageSource fields to new table.
        //-- ------------------------ 875-878
        SQLS = new StringBuilder();
        SQLS.append("INSERT into testcaseexecutionfile (`exeid`, `level`, `FileDesc`, `Filename`, `FileType`, `UsrCreated`)");
        SQLS.append("select ID ExeID, concat(test,\"-\", testcase,\"-\", Step,\"-\", Sequence) level, 'Screenshot' FileDesc, replace(ScreenshotFilename, '\\\\', '/') Filename");
        SQLS.append(" ,ucase(right(ScreenshotFilename, 3)) FileType, 'RecoverSQL' UsrCreated from testcasestepactionexecution where ScreenshotFilename is not null and TO_DAYS(NOW()) - TO_DAYS(Start) <= 10;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("INSERT into testcaseexecutionfile (`exeid`, `level`, `FileDesc`, `Filename`, `FileType`, `UsrCreated`)");
        SQLS.append("select ID ExeID, concat(test,\"-\", testcase,\"-\", Step,\"-\", Sequence) level, 'PageSource' FileDesc, replace(PageSourceFileName, '\\\\', '/') Filename");
        SQLS.append(" ,ucase(right(PageSourceFileName, 3)) FileType, 'RecoverSQL' UsrCreated from testcasestepactionexecution where PageSourceFileName is not null and TO_DAYS(NOW()) - TO_DAYS(Start) <= 10;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("INSERT into testcaseexecutionfile (`exeid`, `level`, `FileDesc`, `Filename`, `FileType`, `UsrCreated`)");
        SQLS.append("select ID ExeID, concat(test,\"-\", testcase,\"-\", Step,\"-\", Sequence,\"-\", Control) level, 'Screenshot' FileDesc, replace(ScreenshotFilename, '\\\\', '/') Filename");
        SQLS.append(" ,ucase(right(ScreenshotFilename, 3)) FileType, 'RecoverSQL' UsrCreated from testcasestepactioncontrolexecution where ScreenshotFilename is not null and TO_DAYS(NOW()) - TO_DAYS(Start) <= 10;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("INSERT into testcaseexecutionfile (`exeid`, `level`, `FileDesc`, `Filename`, `FileType`, `UsrCreated`)");
        SQLS.append("select ID ExeID, concat(test,\"-\", testcase,\"-\", Step,\"-\", Sequence,\"-\", Control) level, 'PageSource' FileDesc, replace(PageSourceFileName, '\\\\', '/') Filename");
        SQLS.append(" ,ucase(right(PageSourceFileName, 3)) FileType, 'RecoverSQL' UsrCreated from testcasestepactioncontrolexecution where PageSourceFileName is not null and TO_DAYS(NOW()) - TO_DAYS(Start) <= 10;");
        SQLInstruction.add(SQLS.toString());

        // New sql timeout parameters.
        //-- ------------------------ 879
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `parameter` (`system`, `param`, `value`, `description`) VALUES ");
        SQLS.append(" ('', 'cerberus_actionexecutesqlstoredprocedure_timeout', '60', 'Integer that correspond to the number of seconds after which, any SQL triggered from action executeSqlStoredProcedure will fail.')");
        SQLInstruction.add(SQLS.toString());

        // Removed PageSource and Screenshot columns from execution tables.
        //-- ------------------------ 880-881
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepactioncontrolexecution` DROP COLUMN `PageSourceFilename`, DROP COLUMN `ScreenshotFilename`;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepactionexecution` DROP COLUMN `PageSourceFileName`, DROP COLUMN `ScreenshotFilename`;");
        SQLInstruction.add(SQLS.toString());

        // New sql document parameter.
        //-- ------------------------ 882
        SQLS = new StringBuilder();
        SQLS.append("select 1 from DUAL;");
        SQLInstruction.add(SQLS.toString());

        // Reorganised Actions.
        //-- ------------------------ 883-892
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `invariant` SET `sort`='10' WHERE `idname`='ACTION' and`value`='Unknown';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `invariant` SET `sort`='25010' WHERE `idname`='ACTION' and`value`='skipAction';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `invariant` SET `sort`='24900' WHERE `idname`='ACTION' and`value`='calculateProperty';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `invariant` SET `sort`='99999', `description`='[DEPRECATED] getPageSource' WHERE `idname`='ACTION' and`value`='getPageSource';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `invariant` SET `sort`='3900' WHERE `idname`='ACTION' and`value`='rightClick';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `invariant` SET `sort`='3850' WHERE `idname`='ACTION' and`value`='doubleClick';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `invariant` SET `sort`='1000' WHERE `idname`='ACTION' and`value`='keypress';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `invariant` SET `sort`='5400' WHERE `idname`='ACTION' and`value`='switchToWindow';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `invariant` SET `sort`='5500' WHERE `idname`='ACTION' and`value`='manageDialog';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("DELETE FROM `invariant` WHERE `idname`='ACTION' and `value` in ('clickAndWait','enter','selectAndWait');");
        SQLInstruction.add(SQLS.toString());

        // Reorganised Controls.
        //-- ------------------------ 893-898
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) VALUES ('CONTROL', 'getPageSource', '10100', 'Save the Page Source.', '');");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `invariant` SET `sort`='1500' WHERE `idname`='CONTROL' and`value`='verifyIntegerEquals';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `invariant` SET `sort`='1550' WHERE `idname`='CONTROL' and`value`='verifyIntegerDifferent';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `invariant` SET `sort`='3250' WHERE `idname`='CONTROL' and`value`='verifyElementDifferent';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `invariant` SET `sort`='3350' WHERE `idname`='CONTROL' and`value`='verifyElementInElement';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) VALUES ('CONTROL', 'skipControl', '15000', 'Skip the control.', '');");
        SQLInstruction.add(SQLS.toString());

        // Reorganised Properties.
        //-- ------------------------ 899-908
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `invariant` SET `sort`='5' WHERE `idname`='PROPERTYTYPE' and`value`='text';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `invariant` SET `description`='Determines the data value associated with a library entry' WHERE `idname`='PROPERTYTYPE' and`value`='getFromDataLib';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `invariant` SET `description`='[DEPRECATED] Getting from the SOAP request using the query' WHERE `idname`='PROPERTYTYPE' and`value`='executeSoapFromLib';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `invariant` SET `description`='[DEPRECATED] Using an SQL from the library' WHERE `idname`='PROPERTYTYPE' and`value`='executeSqlFromLib';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `invariant` SET `description`='[DEPRECATED] Getting from the test Data library using the Key' WHERE `idname`='PROPERTYTYPE' and`value`='getFromTestData';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `invariant` SET `sort`='9999' WHERE `idname`='PROPERTYTYPE' and`value`='getFromTestData';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `invariant` SET `sort`='9999' WHERE `idname`='PROPERTYTYPE' and`value`='executeSqlFromLib';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `invariant` SET `sort`='9999' WHERE `idname`='PROPERTYTYPE' and`value`='executeSoapFromLib';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `invariant` SET `sort`='10' WHERE `idname`='PROPERTYTYPE' and`value`='getFromDataLib';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `invariant` SET `sort`='40' WHERE `idname`='PROPERTYTYPE' and`value`='getFromCookie';");
        SQLInstruction.add(SQLS.toString());

        // New updated Documentation.
        //-- ------------------------ 909-910
        SQLS = new StringBuilder();
        SQLS.append("select 1 from DUAL;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("select 1 from DUAL;");
        SQLInstruction.add(SQLS.toString());

        // New updated Documentation.
        //-- ------------------------ 911-912
        SQLS = new StringBuilder();
        SQLS.append("select 1 from DUAL;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("select 1 from DUAL;");
        SQLInstruction.add(SQLS.toString());

        // New Action model with conditionOper and ConditionVal1.
        //-- ------------------------ 913-919
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepaction` ");
        SQLS.append("ADD COLUMN `ConditionOper` VARCHAR(45) NOT NULL DEFAULT '' AFTER `Sort`,");
        SQLS.append("CHANGE COLUMN `Property` `ConditionVal1` VARCHAR(2500) NULL DEFAULT '' AFTER `ConditionOper`,");
        SQLS.append("CHANGE COLUMN `Object` `Value1` VARCHAR(2500) NOT NULL DEFAULT '' ,");
        SQLS.append("ADD COLUMN `Value2` VARCHAR(2500) NOT NULL DEFAULT '' AFTER `Value1`;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepactionexecution` ");
        SQLS.append("CHANGE COLUMN `Description` `Description` VARCHAR(255) NOT NULL DEFAULT '' AFTER `ForceExeStatus`,");
        SQLS.append("CHANGE COLUMN `ReturnCode` `ReturnCode` VARCHAR(2) NULL DEFAULT NULL AFTER `Description`,");
        SQLS.append("CHANGE COLUMN `ReturnMessage` `ReturnMessage` VARCHAR(500) NULL DEFAULT NULL AFTER `ReturnCode`,");
        SQLS.append("CHANGE COLUMN `Object` `Value1` VARCHAR(2500) NULL DEFAULT '' ,");
        SQLS.append("CHANGE COLUMN `Property` `Value2` VARCHAR(2500) NULL DEFAULT '' ,");
        SQLS.append("ADD COLUMN `ConditionOper` VARCHAR(45) AFTER `Sort`,");
        SQLS.append("ADD COLUMN `ConditionVal1` VARCHAR(2500) AFTER `ConditionOper`,");
        SQLS.append("ADD COLUMN `Value1Init` VARCHAR(2500) NULL DEFAULT '' AFTER `Action`,");
        SQLS.append("ADD COLUMN `Value2Init` VARCHAR(2500) NULL DEFAULT '' AFTER `Value1Init`;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE testcasestepaction SET ConditionOper = 'ifPropertyExist' where ConditionVal1<>''; ");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE testcasestepaction SET ConditionOper = 'always' where ConditionOper=''; ");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) VALUES");
        SQLS.append("  ('ACTIONCONDITIONOPER', 'always', '100', 'Always.', '')");
        SQLS.append(", ('ACTIONCONDITIONOPER', 'ifPropertyExist', '200', 'Only execute if property exist for the execution.', '')");
        SQLS.append(", ('ACTIONCONDITIONOPER', 'never', '9999', 'Never execute the action.', '')");
        SQLS.append(", ('INVARIANTPRIVATE', 'ACTIONCONDITIONOPER', '550', '', '');");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE testcasestepaction SET Value2 = concat('%', ConditionVal1, '%') where ConditionVal1<>'' and action not in ('calculateProperty'); ");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE testcasestepaction SET Value1 = ConditionVal1, Value2='' where action in ('calculateProperty'); ");
        SQLInstruction.add(SQLS.toString());

        // New updated Documentation.
        //-- ------------------------ 920-921
        SQLS = new StringBuilder();
        SQLS.append("select 1 from DUAL;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("select 1 from DUAL;");
        SQLInstruction.add(SQLS.toString());

        // New updated Documentation.
        //-- ------------------------ 922
        SQLS = new StringBuilder();
        SQLS.append("select 1 from DUAL;");
        SQLInstruction.add(SQLS.toString());

        // Corrected ConditionVal1 in order to remove (.
        //-- ------------------------ 923
        SQLS = new StringBuilder();
        SQLS.append("UPDATE testcasestepaction SET ConditionVal1 = left(ConditionVal1,locate('(',ConditionVal1)-1) WHERE conditionval1 like '%(%';");
        SQLInstruction.add(SQLS.toString());

        // Add menuDocumentation and menuHelp in documentation table.
        //-- ------------------------ 924
        SQLS = new StringBuilder();
        SQLS.append("select 1 from DUAL;");
        SQLInstruction.add(SQLS.toString());

        // New updated Documentation.
        //-- ------------------------ 925
        SQLS = new StringBuilder();
        SQLS.append("select 1 from DUAL;");
        SQLInstruction.add(SQLS.toString());

        // New updated Documentation.
        //-- ------------------------ 926
        SQLS = new StringBuilder();
        SQLS.append("select 1 from DUAL;");
        SQLInstruction.add(SQLS.toString());

        // Removed all field and added userAgent on testCase table.
        //-- ------------------------ 927
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcase` ");
        SQLS.append("DROP COLUMN `ChainNumberNeeded`,");
        SQLS.append("DROP COLUMN `ReadOnly`,");
        SQLS.append("ADD COLUMN `useragent` VARCHAR(250) NULL DEFAULT '' AFTER `function`,");
        SQLS.append("CHANGE COLUMN `Creator` `UsrCreated` VARCHAR(45) NOT NULL DEFAULT '' AFTER `useragent`,");
        SQLS.append("CHANGE COLUMN `TCDateCrea` `DateCreated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER `UsrCreated`,");
        SQLS.append("CHANGE COLUMN `LastModifier` `UsrModif` VARCHAR(45) NULL DEFAULT '' AFTER `DateCreated`,");
        SQLS.append("CHANGE COLUMN `last_modified` `DateModif` TIMESTAMP NOT NULL DEFAULT '1970-01-01 01:01:01' ;");
        SQLInstruction.add(SQLS.toString());

        // New updated Documentation.
        //-- ------------------------ 928
        SQLS = new StringBuilder();
        SQLS.append("select 1 from DUAL;");
        SQLInstruction.add(SQLS.toString());

        // New updated Documentation.
        //-- ------------------------ 929
        SQLS = new StringBuilder();
        SQLS.append("select 1 from DUAL;");
        SQLInstruction.add(SQLS.toString());

        // Remove COMPARATIVE and PROCESS Groups.
        //-- ------------------------ 930 - 932
        SQLS = new StringBuilder();
        SQLS.append("DELETE FROM `invariant` WHERE `idname`='GROUP' and`value` in ('COMPARATIVE','PROCESS');");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE testcase SET `group`='MANUAL' WHERE `group` in ('COMPARATIVE', 'PROCESS');");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("select 1 from DUAL;");
        SQLInstruction.add(SQLS.toString());

        // New updated Documentation.
        //-- ------------------------ 933
        SQLS = new StringBuilder();
        SQLS.append("select 1 from DUAL;");
        SQLInstruction.add(SQLS.toString());

        // New updated Documentation.
        //-- ------------------------ 934
        SQLS = new StringBuilder();
        SQLS.append("select 1 from DUAL;");
        SQLInstruction.add(SQLS.toString());

        // Altering testcase table to put notnull with default empty value in most columns.
        //-- ------------------------ 935-936
        SQLS = new StringBuilder();
        SQLS.append("UPDATE testcase set ");
        SQLS.append("`BehaviorOrValueExpected` = coalesce(`BehaviorOrValueExpected`, ''), ");
        SQLS.append("`howto` = coalesce(`howto`, ''), ");
        SQLS.append("`Group` = coalesce(`Group`,''),");
        SQLS.append("`Origine` = coalesce(`Origine`,''),");
        SQLS.append("`RefOrigine` = coalesce(`RefOrigine`,''),");
        SQLS.append("`Comment` = coalesce(`Comment`,''),");
        SQLS.append("`FromBuild` = coalesce(`FromBuild`,''),");
        SQLS.append("`FromRev` = coalesce(`FromRev`,''),");
        SQLS.append("`ToBuild` = coalesce(`ToBuild`,''),");
        SQLS.append("`ToRev` = coalesce(`ToRev`,''),");
        SQLS.append("`BugID` = coalesce(`BugID`,''),");
        SQLS.append("`TargetBuild` = coalesce(`TargetBuild`,''),");
        SQLS.append("`TargetRev` = coalesce(`TargetRev`,''),");
        SQLS.append("`Implementer` = coalesce(`Implementer`,'')");
        SQLS.append("where `BehaviorOrValueExpected` is null or `howto` is null or `Group` is null or `Origine` is null or `RefOrigine` is null or `Comment` is null or `FromBuild` is null or `FromRev` is null or `ToBuild` is null or `ToRev` is null or `BugID` is null or `TargetBuild` is null or `TargetRev` is null or `Implementer` is null");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcase` ");
        SQLS.append("CHANGE COLUMN `BehaviorOrValueExpected` `BehaviorOrValueExpected` TEXT NOT NULL ,");
        SQLS.append("CHANGE COLUMN `Group` `Group` VARCHAR(45) NOT NULL DEFAULT ''  ,");
        SQLS.append("CHANGE COLUMN `Origine` `Origine` VARCHAR(45) NOT NULL DEFAULT ''  ,");
        SQLS.append("CHANGE COLUMN `RefOrigine` `RefOrigine` VARCHAR(45) NOT NULL DEFAULT ''  ,");
        SQLS.append("CHANGE COLUMN `HowTo` `HowTo` TEXT NOT NULL  ,");
        SQLS.append("CHANGE COLUMN `Comment` `Comment` VARCHAR(500) NOT NULL DEFAULT ''  ,");
        SQLS.append("CHANGE COLUMN `FromBuild` `FromBuild` VARCHAR(10) NOT NULL DEFAULT ''  ,");
        SQLS.append("CHANGE COLUMN `FromRev` `FromRev` VARCHAR(20) NOT NULL DEFAULT ''  ,");
        SQLS.append("CHANGE COLUMN `ToBuild` `ToBuild` VARCHAR(10) NOT NULL DEFAULT ''  ,");
        SQLS.append("CHANGE COLUMN `ToRev` `ToRev` VARCHAR(20) NOT NULL DEFAULT ''  ,");
        SQLS.append("CHANGE COLUMN `BugID` `BugID` VARCHAR(10) NOT NULL DEFAULT ''  ,");
        SQLS.append("CHANGE COLUMN `TargetBuild` `TargetBuild` VARCHAR(10) NOT NULL DEFAULT ''  ,");
        SQLS.append("CHANGE COLUMN `TargetRev` `TargetRev` VARCHAR(20) NOT NULL DEFAULT ''  ,");
        SQLS.append("CHANGE COLUMN `Implementer` `Implementer` VARCHAR(45) NOT NULL DEFAULT ''  ;");
        SQLInstruction.add(SQLS.toString());

        // New Parameter for Property calculation retry.
        //-- ------------------------ 937-938
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `parameter` (`system`, `param`, `value`, `description`) VALUES ('', 'cerberus_property_maxretry', '50', 'Integer that correspond to the maximum number of retry allowed when calculating a property.');");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `parameter` (`system`, `param`, `value`, `description`) VALUES ('', 'cerberus_property_maxretrytotalduration', '1800000', 'Integer (in ms) that correspond to the maximum duration of the property calculation. In case the period is greated than this parameter, the period value will be replaced by this parameter with 1 single retry. If number of retries x period is greated than this parameter, the number of retry will be reduced to fit the constrain.');");
        SQLInstruction.add(SQLS.toString());

        // New updated Documentation.
        //-- ------------------------ 939
        SQLS = new StringBuilder();
        SQLS.append("select 1 from DUAL;");
        SQLInstruction.add(SQLS.toString());

        // New updated Documentation.
        //-- ------------------------ 940
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasecountryproperties` ");
        SQLS.append("ADD COLUMN `RetryNb` INT(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '' AFTER `Nature`,");
        SQLS.append("ADD COLUMN `RetryPeriod` INT(10) UNSIGNED NOT NULL DEFAULT 10000 COMMENT '' AFTER `RetryNb`;");
        SQLInstruction.add(SQLS.toString());

        // New updated Documentation.
        //-- ------------------------ 941-942
        SQLS = new StringBuilder();
        SQLS.append("select 1 from DUAL;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("select 1 from DUAL;");
        SQLInstruction.add(SQLS.toString());

        // Resize Script column.
        //-- ------------------------ 943
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testdatalib` ");
        SQLS.append("CHANGE COLUMN `Script` `Script` TEXT NOT NULL ;");
        SQLInstruction.add(SQLS.toString());

        // Updated Documentation
        //-- ------------------------ 944
        SQLS = new StringBuilder();
        SQLS.append("select 1 from DUAL;");
        SQLInstruction.add(SQLS.toString());

        // Add timeout parameters replacing the existing one.
        //-- ------------------------ 945-947
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `parameter` (`system`, `param`, `value`, `description`) VALUES ");
        SQLS.append("('', 'cerberus_selenium_pageLoadTimeout', '45000', 'Integer that correspond to the number of milliseconds that selenium will wait before give timeout, when loading a page.'),");
        SQLS.append("('', 'cerberus_selenium_implicitlyWait', '0', 'Integer that correspond to the number of milliseconds that selenium will implicitely wait when searching an element.'),");
        SQLS.append("('', 'cerberus_selenium_setScriptTimeout', '45000', 'Integer that correspond to the number of milliseconds that selenium will wait before give timeout, when executing a Javascript Script.'),");
        SQLS.append("('', 'cerberus_action_wait_default', '45000', 'Integer that correspond to the number of milliseconds that cerberus will wait by default using the wait action.'),");
        SQLS.append("('', 'cerberus_selenium_wait_element', '45000', 'Integer that correspond to the number of milliseconds that selenium will wait before give timeout, when searching an element.'),");
        SQLS.append("('', 'cerberus_appium_wait_element', '45000', 'Integer that correspond to the number of milliseconds that appium will wait before give timeout, when searching an element.');");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE parameter p2 set `value` = (select * from (select `value` * 1000 from parameter p1 where p1.`param` = 'selenium_defaultWait' and p1.`system` = '') p3 ) ");
        SQLS.append("where p2.`param` in ('cerberus_selenium_wait_element', 'cerberus_selenium_setScriptTimeout', 'cerberus_selenium_pageLoadTimeout','cerberus_appium_wait_element' , 'cerberus_action_wait_default');");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("DELETE FROM parameter where `param` = 'selenium_defaultWait' ");
        SQLInstruction.add(SQLS.toString());

        // Cleaned testcaseexecutiondata table keeping all values of testcasecountryproperty.
        //-- ------------------------ 948
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcaseexecutiondata` ");
        SQLS.append("CHANGE COLUMN `Type` `Type` VARCHAR(45) NULL DEFAULT NULL AFTER `Index`,");
        SQLS.append("CHANGE COLUMN `RC` `RC` VARCHAR(10) NULL DEFAULT NULL AFTER `EndLong`,");
        SQLS.append("CHANGE COLUMN `RMessage` `RMessage` TEXT NULL AFTER `RC`,");
        SQLS.append("CHANGE COLUMN `Description` `Description` VARCHAR(255) NULL DEFAULT '' AFTER `RMessage`,");
        SQLS.append("CHANGE COLUMN `Value` `Value` TEXT NOT NULL ,");
        SQLS.append("CHANGE COLUMN `Value1` `Value1` TEXT NULL ,");
        SQLS.append("CHANGE COLUMN `Value2` `Value2` TEXT NULL ,");
        SQLS.append("ADD COLUMN `Database` VARCHAR(45) NULL AFTER `Value`,");
        SQLS.append("ADD COLUMN `Value1Init` TEXT NULL AFTER `Database`,");
        SQLS.append("ADD COLUMN `Value2Init` TEXT NULL AFTER `Value1Init`,");
        SQLS.append("ADD COLUMN `Length` INT(10) NULL AFTER `Value2`,");
        SQLS.append("ADD COLUMN `RowLimit` INT(10) NULL AFTER `Length`,");
        SQLS.append("ADD COLUMN `Nature` VARCHAR(45) NULL AFTER `RowLimit`,");
        SQLS.append("ADD COLUMN `RetryNb` INT(10) NULL AFTER `Nature`,");
        SQLS.append("ADD COLUMN `RetryPeriod` INT(10) NULL AFTER `RetryNb`;");
        SQLInstruction.add(SQLS.toString());

        // Cleaned testcasestepactioncontrol table.
        //-- ------------------------ 949-953
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepactioncontrol` ");
        SQLS.append("CHANGE COLUMN `Control` `ControlSequence` INT(10) UNSIGNED NOT NULL ,");
        SQLS.append("CHANGE COLUMN `Type` `Control` VARCHAR(200) NOT NULL DEFAULT '' ,");
        SQLS.append("CHANGE COLUMN `ControlProperty` `Value1` TEXT NULL AFTER `Control`,");
        SQLS.append("CHANGE COLUMN `ControlValue` `Value2` TEXT NULL  AFTER `Value1`,");
        SQLS.append("CHANGE COLUMN `ControlDescription` `Description` VARCHAR(255) NOT NULL DEFAULT '' ,");
        SQLS.append("CHANGE COLUMN `Fatal` `Fatal` VARCHAR(1) NULL DEFAULT 'Y' AFTER `Value2`,");
        SQLS.append("DROP PRIMARY KEY, ADD PRIMARY KEY USING BTREE (`Test`, `TestCase`, `Step`, `Sequence`, `ControlSequence`) ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepactioncontrolexecution` ");
        SQLS.append("CHANGE COLUMN `Control` `ControlSequence` INT(10) UNSIGNED NOT NULL ,");
        SQLS.append("CHANGE COLUMN `ControlType` `Control` VARCHAR(200) NULL DEFAULT NULL ,");
        SQLS.append("ADD COLUMN `Value1Init` TEXT NULL AFTER `Control`,");
        SQLS.append("ADD COLUMN `Value2Init` TEXT NULL AFTER `Value1Init`,");
        SQLS.append("CHANGE COLUMN `ControlProperty` `Value1` TEXT NULL AFTER `Value2Init`,");
        SQLS.append("CHANGE COLUMN `ControlValue` `Value2` TEXT NULL ,");
        SQLS.append("CHANGE COLUMN `Description` `Description` VARCHAR(255) NOT NULL DEFAULT '' AFTER `Fatal`,");
        SQLS.append("CHANGE COLUMN `ReturnCode` `ReturnCode` VARCHAR(2) NOT NULL AFTER `Description`,");
        SQLS.append("CHANGE COLUMN `ReturnMessage` `ReturnMessage` TEXT NULL AFTER `ReturnCode`;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasecountryproperties` ");
        SQLS.append("CHANGE COLUMN `Description` `Description` VARCHAR(255) NULL AFTER `RetryPeriod`,");
        SQLS.append("CHANGE COLUMN `Value1` `Value1` TEXT NULL  ,");
        SQLS.append("CHANGE COLUMN `Value2` `Value2` TEXT NULL  ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepaction` ");
        SQLS.append("CHANGE COLUMN `ConditionVal1` `ConditionVal1` TEXT NULL  ,");
        SQLS.append("CHANGE COLUMN `Value1` `Value1` TEXT NOT NULL  ,");
        SQLS.append("CHANGE COLUMN `Value2` `Value2` TEXT NOT NULL  ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepactionexecution` ");
        SQLS.append("CHANGE COLUMN `ConditionVal1` `ConditionVal1` TEXT NULL  ,");
        SQLS.append("CHANGE COLUMN `Value1Init` `Value1Init` TEXT NULL  ,");
        SQLS.append("CHANGE COLUMN `Value2Init` `Value2Init` TEXT NULL  ,");
        SQLS.append("CHANGE COLUMN `Value1` `Value1` TEXT NULL  ,");
        SQLS.append("CHANGE COLUMN `Value2` `Value2` TEXT NULL  ,");
        SQLS.append("CHANGE COLUMN `ReturnMessage` `ReturnMessage` TEXT NULL ;");
        SQLInstruction.add(SQLS.toString());

        // Add value2 usage to the calculateProperty action.
        //-- ------------------------ 954
        SQLS = new StringBuilder();
        SQLS.append("select 1 from DUAL;");
        SQLInstruction.add(SQLS.toString());

        // Remove HTML Escape encoding in soap library
        //-- ------------------------ 955
        SQLS = new StringBuilder();
        SQLS.append("Update soaplibrary set `envelope` = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(`envelope`, '&amp;', '&'),'&lt;','<'),'&gt;','>'),'&apos;','\\''),'&quot;','\\\"')");
        SQLInstruction.add(SQLS.toString());

        // New updated Documentation.
        //-- ------------------------ 956-957
        SQLS = new StringBuilder();
        SQLS.append("DELETE FROM `documentation`;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `documentation` VALUES ('application','Application','','en','Application','')");
        SQLS.append(",('application','Application','','fr','Application','')");
        SQLS.append(",('application','bugtrackernewurl','','en','New Bug URL','This correspond to the URL that points to the page where a new bug can be created on the Bug system of the <code class=\\'doc-crbvvoca\\'>application</code>.<br><br><table cellspacing=0 cellpadding=3><th class=\\'ex\\' colspan=\\'2\\'>The following variables can be used inside the URL</th><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%TEST%</code></td><td class=\\'ex\\'>Test</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%TESTCASE%</code></td><td class=\\'ex\\'>Test case reference</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%TESTCASEDESC%</code></td><td class=\\'ex\\'>Description of the test case</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%EXEID%</code></td><td class=\\'ex\\'>Execution ID</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%EXEDATE%</code></td><td class=\\'ex\\'>Start date and time of the execution.</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%ENV%</code></td><td class=\\'ex\\'>Environment</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%COUNTRY%</code></td><td class=\\'ex\\'>Country</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%BUILD%</code></td><td class=\\'ex\\'>Build</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%REV%</code></td><td class=\\'ex\\'>Revision</td></tr></table>')");
        SQLS.append(",('application','bugtrackernewurl','','fr','URL pour nouveau Bug','Correspond à l\\'URL qui pointe vers la page de création de bug du Bug Tracker de l\\'<code class=\\'doc-crbvvoca\\'>application</code>.<br><br><table cellspacing=0 cellpadding=3><th class=\\'ex\\' colspan=\\'2\\'>Les variables suivantes peuvent être utilisées dans l\\'URL</th><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%TEST%</code></td><td class=\\'ex\\'>Test</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%TESTCASE%</code></td><td class=\\'ex\\'>Reference du cas de test</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%TESTCASEDESC%</code></td><td class=\\'ex\\'>Description du cas de test</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%EXEID%</code></td><td class=\\'ex\\'>ID de l\\'execution</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%EXEDATE%</code></td><td class=\\'ex\\'>Date et heure du debut de l\\'execution.</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%ENV%</code></td><td class=\\'ex\\'>Environnement</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%COUNTRY%</code></td><td class=\\'ex\\'>Pays</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%BUILD%</code></td><td class=\\'ex\\'>Build</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%REV%</code></td><td class=\\'ex\\'>Revision</td></tr></table>')");
        SQLS.append(",('application','bugtrackerurl','','en','Bug Tracker URL','This correspond to the URL of the Bug reporting system of the <code class=\\'doc-crbvvoca\\'>application</code>.<br><br><table cellspacing=0 cellpadding=3><th class=\\'ex\\' colspan=\\'2\\'>The following variables can be used inside the URL</th><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%BUGID%</code></td><td class=\\'ex\\'>ID of the Bug</td></tr></table>')");
        SQLS.append(",('application','bugtrackerurl','','fr','URL du Bug Tracker','Correspond à l\\'URL du Bug Tracker de l\\'<code class=\\'doc-crbvvoca\\'>application</code>.<br><br><table cellspacing=0 cellpadding=3><th class=\\'ex\\' colspan=\\'2\\'>Les variables suivantes peuvent être utilisées dans l\\'URL</th><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%BUGID%</code></td><td class=\\'ex\\'>ID du Bug</td></tr></table>')");
        SQLS.append(",('application','deploytype','','en','Deploy Type','This information groups the <code class=\\'doc-crbvvoca\\'>application</code> by typology of deployement process.<br>It can be used as a variable in the parameter <code class=\\'doc-parameter\\'>jenkins_deploy_url</code> that correspond to the URL that calls a continious integration system such as Jenkins.')");
        SQLS.append(",('application','deploytype','','fr','Type de deploiement','Cette information groupe les <code class=\\'doc-crbvvoca\\'>applications</code> par typologie de deploiement.<br>Peut être utilisé comme variable dans le parametre <code class=\\'doc-parameter\\'>jenkins_deploy_url</code> qui correspond à l\\'URL appelée vers un systeme d\\'intégration continue de type Jenkins.')");
        SQLS.append(",('application','Description','','en','Description','This is the short Description of the <code class=\\'doc-crbvvoca\\'>application</code>.')");
        SQLS.append(",('application','Description','','fr','Description','Description courte de l\\'<code class=\\'doc-crbvvoca\\'>application</code>.')");
        SQLS.append(",('application','mavengroupid','','en','Maven Group ID','')");
        SQLS.append(",('application','mavengroupid','','fr','Identifiant du group Maven','')");
        SQLS.append(",('application','sort','','en','Sort','This correspond to an integer value that is used as a sorting criteria for various combo box within Cerberus GUI.')");
        SQLS.append(",('application','sort','','fr','identifiant de Tri','Correspond à une valeur entière et utilisée pour trier les differentes valeurs dans l\\'interface graphique.')");
        SQLS.append(",('application','subsystem','','en','Subsystem','A <code class=\\'doc-crbvvoca\\'>Subsystem</code> define a group of <code class=\\'doc-crbvvoca\\'>application</code> inside a <code class=\\'doc-crbvvoca\\'>system</code>.')");
        SQLS.append(",('application','subsystem','','fr','Sous Système','Le <code class=\\'doc-crbvvoca\\'>Sous système</code> regroupe un ensemble d\\'<code class=\\'doc-crbvvoca\\'>application</code> au sein d\\'un même <code class=\\'doc-crbvvoca\\'>système</code>.')");
        SQLS.append(",('application','svnurl','','en','SVN URL','This correspond to the URL of the svn repository of the <code class=\\'doc-crbvvoca\\'>application</code>.')");
        SQLS.append(",('application','svnurl','','fr','URL du SVN','Correspond à l\\'URL du SVN de l\\'<code class=\\'doc-crbvvoca\\'>application</code>.')");
        SQLS.append(",('application','system','','en','System','A <code class=\\'doc-crbvvoca\\'>system</code> is a group of <code class=\\'doc-crbvvoca\\'>application</code> for which all changes sometimes require to be done all together.<br> Most of the time those <code class=\\'doc-crbvvoca\\'>applications</code> all connect to a single database and share the same data structure.')");
        SQLS.append(",('application','system','','fr','Système','Un <code class=\\'doc-crbvvoca\\'>système</code> est un groupe d\\'<code class=\\'doc-crbvvoca\\'>applications</code> pour lesquels il y a de temps en temps necessité de faire les changements en même temps.<br> La plupart du temps ces <code class=\\'doc-crbvvoca\\'>applications</code> partagent une même base de donnée et donc une structure de donnée unique.')");
        SQLS.append(",('application','type','','en','Type','The Type of the <code class=\\'doc-crbvvoca\\'>application</code> define whether the <code class=\\'doc-crbvvoca\\'>application</code> is a GUI, a Service or a Batch Treatment.<br>An automated <code class=\\'doc-crbvvoca\\'>testcase</code> based on a GUI <code class=\\'doc-crbvvoca\\'>application</code> will require a selenium server to execute.')");
        SQLS.append(",('application','type','','fr','Type','Le type de l\\'<code class=\\'doc-crbvvoca\\'>application</code> defini si l\\'<code class=\\'doc-crbvvoca\\'>application</code> est une interface graphique (GUI), un fournisseur de Service ou un traitement batch.<br>Un <code class=\\'doc-crbvvoca\\'>cas de test</code> automatisé basé sur une <code class=\\'doc-crbvvoca\\'>application</code> de type GUI necessitera un serveur Selenium pour s\\'executer.')");
        SQLS.append(",('batchinvariant','Batch','','en','Batch','')");
        SQLS.append(",('batchinvariant','Batch','','fr','Batch','')");
        SQLS.append(",('batchinvariant','Description','','en','Description','Description of the batch.')");
        SQLS.append(",('batchinvariant','Description','','fr','Description','Description du batch')");
        SQLS.append(",('batchinvariant','system','','en','System','System of the batch')");
        SQLS.append(",('batchinvariant','system','','fr','System','System du batch')");
        SQLS.append(",('buildrevisionbatch','batch','','en','Batch','')");
        SQLS.append(",('buildrevisionbatch','batch','','fr','Batch','')");
        SQLS.append(",('buildrevisionbatch','build','','en','Build','Build during the excution of the Batch Event.')");
        SQLS.append(",('buildrevisionbatch','build','','fr','Build','Build lors de l\\'éxecution de l\\'événement Batch.')");
        SQLS.append(",('buildrevisionbatch','dateBatch','','en','Date','')");
        SQLS.append(",('buildrevisionbatch','dateBatch','','fr','Date','')");
        SQLS.append(",('buildrevisionbatch','revision','','en','Revision','Revision during the excution of the Batch Event.')");
        SQLS.append(",('buildrevisionbatch','revision','','fr','Revision','Revision lors de l\\'éxecution de l\\'événement Batch.')");
        SQLS.append(",('buildrevisioninvariant','level','','en','Level','')");
        SQLS.append(",('buildrevisioninvariant','level','','fr','Niveau','')");
        SQLS.append(",('buildrevisioninvariant','seq','','en','Sequence','')");
        SQLS.append(",('buildrevisioninvariant','seq','','fr','Séquence','')");
        SQLS.append(",('buildrevisioninvariant','system','','en','System','')");
        SQLS.append(",('buildrevisioninvariant','system','','fr','Système','')");
        SQLS.append(",('buildrevisioninvariant','versionName','','en','Version Name','')");
        SQLS.append(",('buildrevisioninvariant','versionName','','fr','Nom de la Version','')");
        SQLS.append(",('buildrevisioninvariant','versionname01','','en','Build','')");
        SQLS.append(",('buildrevisioninvariant','versionname01','','fr','Build','')");
        SQLS.append(",('buildrevisioninvariant','versionname02','','en','Revision','')");
        SQLS.append(",('buildrevisioninvariant','versionname02','','fr','Revision','')");
        SQLS.append(",('buildrevisionparameters','application','','en','Application','')");
        SQLS.append(",('buildrevisionparameters','application','','fr','Application','')");
        SQLS.append(",('buildrevisionparameters','BugIDFixed','','en','Associated Bug ID','This is the bug ID which has been solved with the <code class=\\'doc-crbvvoca\\'>release</code>.')");
        SQLS.append(",('buildrevisionparameters','BugIDFixed','','fr','ID du Bug associé','ID du bug dont la release est associée.')");
        SQLS.append(",('buildrevisionparameters','Build','','en','Build','')");
        SQLS.append(",('buildrevisionparameters','Build','','fr','Build','')");
        SQLS.append(",('buildrevisionparameters','datecre','','en','Creation Date','')");
        SQLS.append(",('buildrevisionparameters','datecre','','fr','Date de Création','')");
        SQLS.append(",('buildrevisionparameters','id','','en','ID','')");
        SQLS.append(",('buildrevisionparameters','id','','fr','ID','')");
        SQLS.append(",('buildrevisionparameters','jenkinsBuildId','','en','Jenkins Build ID','')");
        SQLS.append(",('buildrevisionparameters','jenkinsBuildId','','fr','Jenkins Build ID','')");
        SQLS.append(",('buildrevisionparameters','Link','','en','Link','This is the link to the detailed content of the <code class=\\'doc-crbvvoca\\'>release</code>.')");
        SQLS.append(",('buildrevisionparameters','Link','','fr','Lien','Lien vers le detail de la <code class=\\'doc-crbvvoca\\'>release</code>.')");
        SQLS.append(",('buildrevisionparameters','mavenArtifactId','','en','Maven Artifact ID','')");
        SQLS.append(",('buildrevisionparameters','mavenArtifactId','','fr','Maven Artifact ID','')");
        SQLS.append(",('buildrevisionparameters','mavenGroupId','','en','Maven Group ID','')");
        SQLS.append(",('buildrevisionparameters','mavenGroupId','','fr','Maven Group ID','')");
        SQLS.append(",('buildrevisionparameters','mavenVersion','','en','Maven Version','')");
        SQLS.append(",('buildrevisionparameters','mavenVersion','','fr','Maven Version','')");
        SQLS.append(",('buildrevisionparameters','project','','en','Project','')");
        SQLS.append(",('buildrevisionparameters','project','','fr','Projet','')");
        SQLS.append(",('buildrevisionparameters','Release','','en','Release','A <code class=\\'doc-crbvvoca\\'>release</code> is a single change done on the <code class=\\'doc-crbvvoca\\'>application</code>.')");
        SQLS.append(",('buildrevisionparameters','Release','','fr','Release','A <code class=\\'doc-crbvvoca\\'>release</code> is a single change done on the <code class=\\'doc-crbvvoca\\'>application</code>.')");
        SQLS.append(",('buildrevisionparameters','ReleaseOwner','','en','Owner','This is the name of the person who is responsible for the <code class=\\'doc-crbvvoca\\'>release</code>.')");
        SQLS.append(",('buildrevisionparameters','ReleaseOwner','','fr','Responsable','Nom de la personne responsable de la <code class=\\'doc-crbvvoca\\'>release</code>.')");
        SQLS.append(",('buildrevisionparameters','repositoryUrl','','en','Repository URL','This information corresponds to the URL where the current build of the <code class=\\'doc-crbvvoca\\'>application</code> can be downloaded.<br>It allow to retrieve it in a repository such as Nexus.')");
        SQLS.append(",('buildrevisionparameters','repositoryUrl','','fr','URL du Dépot','Cette information correspond à l\\'URL d\\'où le build de l\\'<code class=\\'doc-crbvvoca\\'>application</code> peut-être téléchargé.<br>Cela permet de retrouver un build spécifique dans un dépot de livrable de type Nexus.')");
        SQLS.append(",('buildrevisionparameters','Revision','','en','Revision','')");
        SQLS.append(",('buildrevisionparameters','Revision','','fr','Revision','')");
        SQLS.append(",('buildrevisionparameters','subject','','en','Description','')");
        SQLS.append(",('buildrevisionparameters','subject','','fr','Description','')");
        SQLS.append(",('buildrevisionparameters','TicketIDFixed','','en','Associated Ticket ID','This is the Ticket ID which has been delivered with the <code class=\\'doc-crbvvoca\\'>release</code>.')");
        SQLS.append(",('buildrevisionparameters','TicketIDFixed','','fr','ID du Ticket associé','ID du ticket dont la release est associée.')");
        SQLS.append(",('countryenvdeploytype','JenkinsAgent','','en','Jenkins Agent','')");
        SQLS.append(",('countryenvdeploytype','JenkinsAgent','','fr','Agent Jenkins','')");
        SQLS.append(",('countryenvironmentdatabase','ConnectionPoolName','','en','JDBC Ressource','This is the name of the JDBC Ressource used to connect to the corresponding <code class=\\'doc-crbvvoca\\'>database</code> on the <code class=\\'doc-crbvvoca\\'>country</code> / <code class=\\'doc-crbvvoca\\'>environment</code>.<br>The JDBC Ressource (prefixed by <code class=\\'doc-fixed\\'>jdbc/</code> ) needs to be configured and associated to a connection pool on the application server that host the Cerberus application.<br><br>Example :<br><doc class=\"examples\"><table cellspacing=0 cellpadding=3><th class=\\'ex\\'>JDBC Ressource</th><th class=\\'ex\\'>Application server Ressource name</th><tr>\n<td class=\\'ex\\'>MyConnection</td>\n<td class=\\'ex\\'>jdbc/MyConnection</td>\n</tr></table>\n</doc>')");
        SQLS.append(",('countryenvironmentdatabase','ConnectionPoolName','','fr','Ressource JDBC','Nom de la ressource JDBC utilisée pour se connecter à la <code class=\\'doc-crbvvoca\\'>base de donnée</code> correspondant au <code class=\\'doc-crbvvoca\\'>pays</code> / <code class=\\'doc-crbvvoca\\'>environnement</code>.<br>La ressource JDBC (préfixée par <code class=\\'doc-fixed\\'>jdbc/</code> ) doit être configuré dans le serveur l\\'application qui héberge Cerberus et associé à un pool de connexion.<br><br>Exemple :<br><doc class=\"examples\"><table cellspacing=0 cellpadding=3><th class=\\'ex\\'>JDBC Ressource</th><th class=\\'ex\\'>Application server Ressource name</th><tr>\n<td class=\\'ex\\'>MyConnection</td>\n<td class=\\'ex\\'>jdbc/MyConnection</td>\n</tr></table>\n</doc>')");
        SQLS.append(",('countryenvironmentdatabase','Database','','en','Database','')");
        SQLS.append(",('countryenvironmentdatabase','Database','','fr','Base de donnée','')");
        SQLS.append(",('countryenvironmentparameters','domain','','en','Domain','Domain of the Application. Can be used inside any test execution with %SYS_APP_DOMAIN% variable.')");
        SQLS.append(",('countryenvironmentparameters','domain','','fr','Domaine','Domaine Internet de l\\'application. Peut être utilisé pendant l\\'execution des tests avec la variable %SYS_APP_DOMAIN%.')");
        SQLS.append(",('countryenvironmentparameters','IP','','en','Host','Ressource location of the application.<br><br>Examples :<br><doc class=\"examples\"><code class=\\'doc-url\\'>www.domain.com</code><br><code class=\\'doc-url\\'>192.168.1.1:80</code><br><code class=\\'doc-url\\'>user:password@www.domain.com:8080</code><br><code class=\\'doc-url\\'>user:password@192.168.1.1:80</code><br><code class=\\'doc-url\\'>http://www.laredoute.fr</code><br><code class=\\'doc-url\\'>https://www.facebook.com</code><br></doc><br>NB : If the protocol is not specified, the default selected is http://<br>In case you want to test an https:// application, this ressource location must begin by https://.')");
        SQLS.append(",('countryenvironmentparameters','IP','','fr','Hote','Chemin de l\\'application.<br><br>Exemples :<br><doc class=\"examples\"><code class=\\'doc-url\\'>www.domain.com</code><br><code class=\\'doc-url\\'>192.168.1.1:80</code><br><code class=\\'doc-url\\'>user:password@www.domain.com:8080</code><br><code class=\\'doc-url\\'>user:password@192.168.1.1:80</code><br><code class=\\'doc-url\\'>http://www.laredoute.fr</code><br><code class=\\'doc-url\\'>https://www.facebook.com</code><br></doc><br>NB : Si le protocole n\\'est pas specifié, le protocople par default utilisé est http://<br>En cas de test d\\'une application en https, il faut commencer l\\'URL par https://.')");
        SQLS.append(",('countryenvironmentparameters','URL','','en','Context Root','Root URL used to access the application. Equivalent to context root.<br>This path will always be added to the information specified in the testcase.<br><br>Example :<br><doc class=\"examples\"><table cellspacing=0 cellpadding=3><th class=\\'ex\\'>URL</th><th class=\\'ex\\'>Description</th><tr><td class=\\'ex\\'><code class=\\'doc-url\\'>/Cerberus-1.0.1-SNAPSHOT/</code></td><td class=\\'ex\\'>When opening <code class=\\'doc-url\\'>login.jsp</code>, Cerberus will open <code class=\\'doc-url\\'>/Cerberus-1.0.1-SNAPSHOT/login.jsp</code> URL</td></tr></table></doc>')");
        SQLS.append(",('countryenvironmentparameters','URL','','fr','Context Root','URL Racine de l\\'application. Equivalent du Context Root.<br>Ce chemin sera systematiquement ajouté aux chemin specifiés dans chaque cas de test.<br><br>Exemple :<br><doc class=\"examples\"><table cellspacing=0 cellpadding=3><th class=\\'ex\\'>URL</th><th class=\\'ex\\'>Description</th><tr><td class=\\'ex\\'><code class=\\'doc-url\\'>/Cerberus-1.0.1-SNAPSHOT/</code></td><td class=\\'ex\\'>Lorsque l\\'on ouvrira <code class=\\'doc-url\\'>login.jsp</code>, Cerberus ouvrira  <code class=\\'doc-url\\'>/Cerberus-1.0.1-SNAPSHOT/login.jsp</code> URL</td></tr></table></doc>')");
        SQLS.append(",('countryenvironmentparameters','URLLOGIN','','en','Login URL','Path to login page. This path is used only when calling the <code class=\\'doc-action\\'>openUrlLogin</code> Action.')");
        SQLS.append(",('countryenvironmentparameters','URLLOGIN','','fr','URL de Login','Chemin vers la page de login. Ce chemin peut être utilisé à partir de l\\'action <code class=\\'doc-action\\'>openUrlLogin</code>.')");
        SQLS.append(",('countryenvironmentparameters','Var1','','en','Variable 1','Variable can be used inside testcases with %SYS_APP_VAR1% variable.')");
        SQLS.append(",('countryenvironmentparameters','Var1','','fr','Variable 1','Variable qui peut être utilisée dans les test avec : %SYS_APP_VAR1%')");
        SQLS.append(",('countryenvironmentparameters','Var2','','en','Variable 2','Variable can be used inside testcases with %SYS_APP_VAR2% variable.')");
        SQLS.append(",('countryenvironmentparameters','Var2','','fr','Variable 2','Variable qui peut être utilisée dans les test avec : %SYS_APP_VAR2%')");
        SQLS.append(",('countryenvironmentparameters','Var3','','en','Variable 3','Variable can be used inside testcases with %SYS_APP_VAR3% variable.')");
        SQLS.append(",('countryenvironmentparameters','Var3','','fr','Variable 3','Variable qui peut être utilisée dans les test avec : %SYS_APP_VAR3%')");
        SQLS.append(",('countryenvironmentparameters','Var4','','en','Variable 4','Variable can be used inside testcases with %SYS_APP_VAR4% variable.')");
        SQLS.append(",('countryenvironmentparameters','Var4','','fr','Variable 4','Variable qui peut être utilisée dans les test avec : %SYS_APP_VAR4%')");
        SQLS.append(",('countryenvlink','CountryLink','','en','Country linked','')");
        SQLS.append(",('countryenvlink','CountryLink','','fr','Pays lié','')");
        SQLS.append(",('countryenvlink','EnvironmentLink','','en','Environment linked','')");
        SQLS.append(",('countryenvlink','EnvironmentLink','','fr','Environnement lié','')");
        SQLS.append(",('countryenvlink','systemLink','','en','System','')");
        SQLS.append(",('countryenvlink','systemLink','','fr','System','')");
        SQLS.append(",('countryenvparam','active','','en','Active','Define if the <code class=\\'doc-crbvvoca\\'>environment</code> is active or not. A <code class=\\'doc-crbvvoca\\'>test case</code> cannot be executed against an <code class=\\'doc-crbvvoca\\'>environment</code> that is not  active.')");
        SQLS.append(",('countryenvparam','active','','fr','Actif','')");
        SQLS.append(",('countryenvparam','chain','','en','Chain','')");
        SQLS.append(",('countryenvparam','chain','','fr','Chaine','')");
        SQLS.append(",('countryenvparam','Description','','en','Description','')");
        SQLS.append(",('countryenvparam','Description','','fr','Description','')");
        SQLS.append(",('countryenvparam','DistribList','','en','Recipent list of Notification Email','This is the list of email adresses that will receive the notification on any environment event.<br><br>In case that value is not feeded, the following parameters are used (depending on the related event) :<br><code class=\\'doc-parameter\\'>integration_notification_disableenvironment_to</code><br><code class=\\'doc-parameter\\'>integration_notification_newbuildrevision_to</code><br><code class=\\'doc-parameter\\'>integration_notification_newchain_to</code>')");
        SQLS.append(",('countryenvparam','DistribList','','fr','Emails des destinataires','')");
        SQLS.append(",('countryenvparam','EMailBodyChain','','en','EMail Body on New Chain Executed Event','This is the Body of the mail that will be generated when a new Treatment has been executed on the <code class=\\'doc-crbvvoca\\'>environment</code>.<br><br><table cellspacing=0 cellpadding=3><th class=\\'ex\\' colspan=\\'2\\'>The following variable can be used</th><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYSTEM%</code></td><td class=\\'ex\\'>System value</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%COUNTRY%</code></td><td class=\\'ex\\'>Country code</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%ENV%</code></td><td class=\\'ex\\'>Environment code</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%BUILD%</code></td><td class=\\'ex\\'>Current Build version name</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%REVISION%</code></td><td class=\\'ex\\'>Current Revision version name</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%CHAIN%</code></td><td class=\\'ex\\'>Chain value that has been executed</td></tr></table><br>In case that value is not feeded, the following parameter is used :<br><code class=\\'doc-parameter\\'>integration_notification_newchain_body</code>')");
        SQLS.append(",('countryenvparam','EMailBodyChain','','fr','Corps du message en cas de nouvelle Chaine','')");
        SQLS.append(",('countryenvparam','EMailBodyDisableEnvironment','','en','EMail Body on Disable Environment Event','This is the Body of the mail that will be generated when <code class=\\'doc-crbvvoca\\'>environment</code> is disabled for installation purpose.<br><br><table cellspacing=0 cellpadding=3><th class=\\'ex\\' colspan=\\'2\\'>The following variable can be used</th><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYSTEM%</code></td><td class=\\'ex\\'>System value</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%COUNTRY%</code></td><td class=\\'ex\\'>Country code</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%ENV%</code></td><td class=\\'ex\\'>Environment code</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%BUILD%</code></td><td class=\\'ex\\'>Current Build version name</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%REVISION%</code></td><td class=\\'ex\\'>Current Revision version name</td></tr></table><br>In case that value is not feeded, the following parameter is used :<br><code class=\\'doc-parameter\\'>integration_notification_disableenvironment_body</code>')");
        SQLS.append(",('countryenvparam','EMailBodyDisableEnvironment','','fr','Corps du message en cas de désactivation','')");
        SQLS.append(",('countryenvparam','EMailBodyRevision','','en','EMail Body on New Build/Revision Event','This is the Body of the mail that will be generated when a new Build/Revision is installed on the <code class=\\'doc-crbvvoca\\'>environment</code>.<br><br><table cellspacing=0 cellpadding=3><th class=\\'ex\\' colspan=\\'2\\'>The following variable can be used</th><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYSTEM%</code></td><td class=\\'ex\\'>System value</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%COUNTRY%</code></td><td class=\\'ex\\'>Country code</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%ENV%</code></td><td class=\\'ex\\'>Environment code</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%BUILD%</code></td><td class=\\'ex\\'>Current Build version name</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%REVISION%</code></td><td class=\\'ex\\'>Current Revision version name</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%BUILDCONTENT%</code></td><td class=\\'ex\\'>Detailed content of the sprint/revision.<br>That include the list of release of every application.</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%TESTRECAP%</code></td><td class=\\'ex\\'>A summary of test cases executed for that build and revision for the country.</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%TESTRECAPALL%</code></td><td class=\\'ex\\'>A summary of test cases executed for that build and revision for all the countries.</td></tr></table><br><br>In case that value is not feeded, the following parameter is used :<br><code class=\\'doc-parameter\\'>integration_notification_newbuildrevision_body</code>')");
        SQLS.append(",('countryenvparam','EMailBodyRevision','','fr','Corps du message en cas de nouvelle Revision','')");
        SQLS.append(",('countryenvparam','maintenanceact','','en','Maintenance Activation','This is the activation flag of the daily maintenance period.<br>In case the flag is activated, start and end times needs to be specified.<br>During a maintenance period, the <code class=\\'doc-crbvvoca\\'>environment</code> is considered as disable and Cerberus will prevent the test case from beeing executed.')");
        SQLS.append(",('countryenvparam','maintenanceact','','fr','Activation de la plage de maintenance','')");
        SQLS.append(",('countryenvparam','maintenanceend','','en','Maintenance End Time','This is the time when the daily maintenance period ends.<br>If start time is before end time then, any test execution request submitted between start and end will be discarded with an explicit error message that will report the maintenance period and time of the submission.<br>If start is after end then any test execution request submitted between end and start will be possible. All the overs will be discarded with an explicit error message that will report the maintenance period and time of the submission.<br><br>Examples :<br><doc class=\"examples\"><table cellspacing=0 cellpadding=3><th class=\\'ex\\'>flag</th><th class=\\'ex\\'>start</th><th class=\\'ex\\'>end</th><th class=\\'ex\\'>Result</th><tr><td class=\\'ex\\'>Yes</td><td class=\\'ex\\'>23:00:00</td><td class=\\'ex\\'><b>23:30:00</b></td><td class=\\'ex\\'>Any execution between 23H00 and 23H30 will be discarded.</td></tr><tr><td class=\\'ex\\'>Yes</td><td class=\\'ex\\'>23:00:00</td><td class=\\'ex\\'><b>02:30:00</b></td><td class=\\'ex\\'>Any execution between 23H00 and 2H30 will be discarded.</td></tr><tr><td class=\\'ex\\'>No</td><td class=\\'ex\\'>23:00:00</td><td class=\\'ex\\'><b>23:30:00</b></td><td class=\\'ex\\'>All executions will be authorised.</td></tr></table></doc>')");
        SQLS.append(",('countryenvparam','maintenanceend','','fr','Heure de fin de la plage de maintenance','')");
        SQLS.append(",('countryenvparam','maintenancestr','','en','Maintenance Start Time','This is the time when the daily maintenance period starts.<br>If start is before end then, any test execution request submitted between start and end will be discarded with an explicit error message that will report the maintenance period and time of the submission.<br>If start is after end then any test execution request submitted between end and start will be possible. All the overs will be discarded with an explicit error message that will report the maintenance period and time of the submission.<br><br>Examples :<br><doc class=\"examples\"><table cellspacing=0 cellpadding=3><th class=\\'ex\\'>flag</th><th class=\\'ex\\'>start</th><th class=\\'ex\\'>end</th><th class=\\'ex\\'>Result</th><tr><td class=\\'ex\\'>Yes</td><td class=\\'ex\\'><b>23:00:00</b></td><td class=\\'ex\\'>23:30:00</td><td class=\\'ex\\'>Any execution between 23H00 and 23H30 will be discarded.</td></tr><tr><td class=\\'ex\\'>Yes</td><td class=\\'ex\\'><b>23:00:00</b></td><td class=\\'ex\\'>02:30:00</td><td class=\\'ex\\'>Any execution between 23H00 and 2H30 will be discarded.</td></tr><tr><td class=\\'ex\\'>No</td><td class=\\'ex\\'><b>23:00:00</b></td><td class=\\'ex\\'>23:30:00</td><td class=\\'ex\\'>All executions will be authorised.</td></tr></table></doc>')");
        SQLS.append(",('countryenvparam','maintenancestr','','fr','Heure de début de la plage de maintenance','')");
        SQLS.append(",('countryenvparam','system','','en','System','')");
        SQLS.append(",('countryenvparam','system','','fr','Système','')");
        SQLS.append(",('countryenvparam','Type','','en','Type','The type of the <code class=\\'doc-crbvvoca\\'>environment</code> define what is the <code class=\\'doc-crbvvoca\\'>environment</code> used for.<br><br><p>\\'STD\\' Standard Testing is allowed in the <code class=\\'doc-crbvvoca\\'>environment</code>.</p><p>\\'COMPARISON\\' Only Comparison test case is allowed to be executed on the <code class=\\'doc-crbvvoca\\'>environment</code>. No other test cases is allowed to execute. This is to avoid modifying any data on the <code class=\\'doc-crbvvoca\\'>environment</code> and not beeing able to analyse easilly the differences between 2 Build/Revisions.</p>')");
        SQLS.append(",('countryenvparam','Type','','fr','Type','')");
        SQLS.append(",('countryenvparam_log','Creator','','en','User','')");
        SQLS.append(",('countryenvparam_log','Creator','','fr','Utilisateur','')");
        SQLS.append(",('countryenvparam_log','datecre','','en','Date & Time','')");
        SQLS.append(",('countryenvparam_log','datecre','','fr','Date & Heure','')");
        SQLS.append(",('countryenvparam_log','Description','','en','Description','')");
        SQLS.append(",('countryenvparam_log','Description','','fr','Description','')");
        SQLS.append(",('dataTable','colVis','','en','Show/Hide columns','')");
        SQLS.append(",('dataTable','colVis','','fr','Afficher/Cacher les colonnes','')");
        SQLS.append(",('dataTable','sEmptyTable','','en','No data available in table','')");
        SQLS.append(",('dataTable','sEmptyTable','','fr','Aucune donn&eacute;e disponible dans le tableau','')");
        SQLS.append(",('dataTable','sFirst','','en','First','')");
        SQLS.append(",('dataTable','sFirst','','fr','Premier','')");
        SQLS.append(",('dataTable','sInfo','','en','Showing _START_ to _END_ of _TOTAL_ entries','')");
        SQLS.append(",('dataTable','sInfo','','fr','Affichage de l\\'&eacute;l&eacute;ment _START_ &agrave; _END_ sur _TOTAL_ &eacute;l&eacute;ments','')");
        SQLS.append(",('dataTable','sInfoEmpty','','en','Showing 0 to 0 of 0 entries','')");
        SQLS.append(",('dataTable','sInfoEmpty','','fr','Affichage de l\\'&eacute;l&eacute;ment 0 &agrave; 0 sur 0 &eacute;l&eacute;ments','')");
        SQLS.append(",('dataTable','sInfoFiltered','','en','(filtered from _MAX_ total entries)','')");
        SQLS.append(",('dataTable','sInfoFiltered','','fr','(filtr&eacute; de _MAX_ &eacute;l&eacute;ments au total)','')");
        SQLS.append(",('dataTable','sInfoPostFix','','en','','')");
        SQLS.append(",('dataTable','sInfoPostFix','','fr','','')");
        SQLS.append(",('dataTable','sInfoThousands','','en',',','')");
        SQLS.append(",('dataTable','sInfoThousands','','fr',',','')");
        SQLS.append(",('dataTable','sLast','','en','Last','')");
        SQLS.append(",('dataTable','sLast','','fr','Dernier','')");
        SQLS.append(",('dataTable','sLengthMenu','','en','_MENU_','')");
        SQLS.append(",('dataTable','sLengthMenu','','fr','_MENU_','')");
        SQLS.append(",('dataTable','sLoadingRecords','','en','Loading...','')");
        SQLS.append(",('dataTable','sLoadingRecords','','fr','Chargement en cours...','')");
        SQLS.append(",('dataTable','sNext','','en','Next','')");
        SQLS.append(",('dataTable','sNext','','fr','Suivant','')");
        SQLS.append(",('dataTable','sPrevious','','en','Previous','')");
        SQLS.append(",('dataTable','sPrevious','','fr','Pr&eacute;c&eacute;dent','')");
        SQLS.append(",('dataTable','sProcessing','','en','Processing...','')");
        SQLS.append(",('dataTable','sProcessing','','fr','Traitement en cours...','')");
        SQLS.append(",('dataTable','sSearch','','en','_INPUT_','')");
        SQLS.append(",('dataTable','sSearch','','fr','_INPUT_','')");
        SQLS.append(",('dataTable','sSearchPlaceholder','','en','Search...','')");
        SQLS.append(",('dataTable','sSearchPlaceholder','','fr','Rechercher...','')");
        SQLS.append(",('dataTable','sSortAscending','','en',': activate to sort column ascending','')");
        SQLS.append(",('dataTable','sSortAscending','','fr',': activer pour trier la colonne par ordre croissant','')");
        SQLS.append(",('dataTable','sSortDescending','','en',': activate to sort column descending','')");
        SQLS.append(",('dataTable','sSortDescending','','fr',': activer pour trier la colonne par ordre d&eacute;croissant','')");
        SQLS.append(",('dataTable','sZeroRecords','','en','No matching records found','')");
        SQLS.append(",('dataTable','sZeroRecords','','fr','Aucun &eacute;l&eacute;ment &agrave; afficher','')");
        SQLS.append(",('deploytype','deploytype','','en','Deployment Type','')");
        SQLS.append(",('deploytype','deploytype','','fr','Type de deploiement','')");
        SQLS.append(",('deploytype','description','','en','Description','')");
        SQLS.append(",('deploytype','description','','fr','Description','')");
        SQLS.append(",('homepage','btn_addTag','','en','Add Tag','')");
        SQLS.append(",('homepage','btn_addTag','','fr','Ajouter le tag','')");
        SQLS.append(",('homepage','btn_settings','','en','Customize','')");
        SQLS.append(",('homepage','btn_settings','','fr','Personnaliser','')");
        SQLS.append(",('homepage','lastTagExecution','','en','Last Tags Executions','Display the statistics of the last tag executed or the tags set in the settings')");
        SQLS.append(",('homepage','lastTagExecution','','fr','Dernières exécutions de tags','Affiche les statistiques d\\'exécutions des derniers tags exécutés ou des tags définis dans les options')");
        SQLS.append(",('homepage','modal_title','','en','Tag Filter','This modal permits you to add tags to the last tags executions report')");
        SQLS.append(",('homepage','modal_title','','fr','Filtre des tags','Ce modal vous permet d\\'ajouter des tags au rapport des dernières exécutions de tags')");
        SQLS.append(",('homepage','testCaseStatusByApp','','en','Test Case Status by Application','Workflow status of the test case by Application')");
        SQLS.append(",('homepage','testCaseStatusByApp','','fr','Status des Cas de Tests par Application','Status de l\\'avancement des cas de tests par Application')");
        SQLS.append(",('homepage','title','','en','Welcome to Cerberus application','')");
        SQLS.append(",('homepage','title','','fr','Bienvenue dans l\\'application Cerberus','')");
        SQLS.append(",('host','active','','en','Active','')");
        SQLS.append(",('host','host','','en','Host','')");
        SQLS.append(",('host','port','','en','port','')");
        SQLS.append(",('host','secure','','en','secure','')");
        SQLS.append(",('host','Server','','en','Server','This is used to define different host on different server for resilence purpose. You can use that for PRIMARY and BACKUP access.')");
        SQLS.append(",('host','Session','','en','Session','')");
        SQLS.append(",('invariant','COUNTRY','','en','Country','A <code class=\\'doc-crbvvoca\\'>country</code> is a declination of a <code class=\\'doc-crbvvoca\\'>system</code> in an <code class=\\'doc-crbvvoca\\'>environment</code> with a specific configuration.<br>This is called <code class=\\'doc-crbvvoca\\'>country</code> because for <code class=\\'doc-crbvvoca\\'>systems</code> that support multiple countries, every <code class=\\'doc-crbvvoca\\'>country</code> is deployed on different <code class=\\'doc-crbvvoca\\'>environments</code>. Each of them can have the same version of the <code class=\\'doc-crbvvoca\\'>application</code> but with different configuration. As a consequence, some <code class=\\'doc-crbvvoca\\'>test case</code> may or may not be relevant on that <code class=\\'doc-crbvvoca\\'>country</code>.')");
        SQLS.append(",('invariant','COUNTRY','','fr','Pays','Un <code class=\\'doc-crbvvoca\\'>pays</code> est une declinaison d\\'un <code class=\\'doc-crbvvoca\\'>système</code> dans un <code class=\\'doc-crbvvoca\\'>environnement</code> avec une configuration specifique.<br>Ca porte le nom de <code class=\\'doc-crbvvoca\\'>pays</code> car pour les <code class=\\'doc-crbvvoca\\'>systèmes</code> qui supportent plusieurs pays, chaque <code class=\\'doc-crbvvoca\\'>pays</code> est deployé sur un <code class=\\'doc-crbvvoca\\'>environnement</code> different. Chacun d\\'entre eux peut avoir la même version de l\\'<code class=\\'doc-crbvvoca\\'>application</code> mais avec differentes configuration. En conséquence, certain <code class=\\'doc-crbvvoca\\'>cas de test</code> peuvent ou non etre pertinant sur ce <code class=\\'doc-crbvvoca\\'>pays</code>.')");
        SQLS.append(",('invariant','ENVGP','','en','Environment Group','')");
        SQLS.append(",('invariant','ENVGP','','fr','Groupe d\\'Environnement','')");
        SQLS.append(",('invariant','ENVIRONMENT','','en','Environment','')");
        SQLS.append(",('invariant','ENVIRONMENT','','fr','Environnement','')");
        SQLS.append(",('invariant','FILTERNBDAYS','','en','Nb Days','Number of days to Filter the history table in the integration status.')");
        SQLS.append(",('invariant','FILTERNBDAYS','','fr','Nb Jours','')");
        SQLS.append(",('invariant','GROUP','','en','Group','The group is a property of a <code class=\\'doc-crbvvoca\\'>test case</code> that can take the following values : <br><br><b>AUTOMATED</b> : The <code class=\\'doc-crbvvoca\\'>test case</code> is fully automated and does not require any manual action.<br><b>MANUAL</b> : The <code class=\\'doc-crbvvoca\\'>test case</code> has to be manually executed.<br><b>PRIVATE</b> : The <code class=\\'doc-crbvvoca\\'>test case</code> exist for technical reason and will never appear on the reporting area. For example : <code class=\\'doc-fixed\\'>Pre Testing</code> test cases that are used for login purpose should all be PRIVATE.')");
        SQLS.append(",('invariant','GROUP','','fr','Groupe','')");
        SQLS.append(",('invariant','PRIORITY','','en','Priority','It is the priority level of the functionnality which is tested.')");
        SQLS.append(",('invariant','PRIORITY','','fr','Priorité','')");
        SQLS.append(",('invariant','SYSTEM','','en','System','')");
        SQLS.append(",('invariant','SYSTEM','','fr','Système','')");
        SQLS.append(",('label','color','','en','Color','<p>This field is the color that will be applyied to the label. It could be any value understable by the brower.</p><p>Examples: blue |  #00FF00 | #000 </p>')");
        SQLS.append(",('label','color','','fr','Couleur','<p>Cette valeur représente la couleur appliquée au label. Cela peut-être toute valeur reconnue comme couleur par le navigateur.</p><p>Exemples: blue |  #00FF00 | #000 </p>')");
        SQLS.append(",('label','description','','en','Description','<p>Description of the label.</p>')");
        SQLS.append(",('label','description','','fr','Description','<p>Description du label.</p>')");
        SQLS.append(",('label','id','','en','ID','')");
        SQLS.append(",('label','id','','fr','ID','')");
        SQLS.append(",('label','label','','en','Label','<p>This value is the label attached to testcase for the purpose of identification or to give other information.</p>')");
        SQLS.append(",('label','label','','fr','Label','<p>Cette valeur représente le label qui sera attaché au testcase à des fins d\\'identification ou de regroupement.</p>')");
        SQLS.append(",('label','parentid','','en','Parent LabelID','<p>This value represent the labelID of the parent label. This allow to group or create hierachy in label</p>')");
        SQLS.append(",('label','parentid','','fr','ID du label parent','<p>Cette valeur est l\\'ID du label parent. Cela permet de grouper ou de créer une hiérarchie au sein des labels</p>')");
        SQLS.append(",('label','system','','en','System','')");
        SQLS.append(",('label','system','','fr','Système','')");
        SQLS.append(",('logevent','action','','en','Action','Type of the action performed')");
        SQLS.append(",('logevent','action','','fr','Action','Type de l\\'action effetué')");
        SQLS.append(",('logevent','localip','','en','Local IP','IP of the Cerberus server that provided the service.')");
        SQLS.append(",('logevent','localip','','fr','IP Locale','IP du serveur Cerberus qui a répondu à la requette.')");
        SQLS.append(",('logevent','log','','en','Log','Log message of the action.')");
        SQLS.append(",('logevent','log','','fr','Message','Message lié à l\\'action.')");
        SQLS.append(",('logevent','logeventid','','en','Log Entry ID','Unique identifier of the log entry.')");
        SQLS.append(",('logevent','logeventid','','fr','Identifiant du log','Identifiant unique de l\\'entrée de log.')");
        SQLS.append(",('logevent','login','','en','Login','Login of the user who performed the action.')");
        SQLS.append(",('logevent','login','','fr','Utilisateur','Nom de l\\'utilisateur qui a effectué l\\'action.')");
        SQLS.append(",('logevent','page','','en','Page','Name of the page where the action was performed.')");
        SQLS.append(",('logevent','page','','fr','Page','Nom de la page où l\\'action a été effectuée.')");
        SQLS.append(",('logevent','remoteip','','en','Remote IP','IP from which the user connected to Cerberus to perform the action.')");
        SQLS.append(",('logevent','remoteip','','fr','IP Distante','IP à partir de laquelle l\\'utilisateur s\\'est connecté à Cerberus.')");
        SQLS.append(",('logevent','time','','en','Time','Timestamp of the log message.')");
        SQLS.append(",('logevent','time','','fr','Date','Date à laquelle l\\'action a été effectuée.')");
        SQLS.append(",('modal_upload','btn_cancel','','en','Cancel','')");
        SQLS.append(",('modal_upload','btn_cancel','','fr','Annuler','')");
        SQLS.append(",('modal_upload','btn_choose','','en','Choose File','')");
        SQLS.append(",('modal_upload','btn_choose','','fr','Choisir','')");
        SQLS.append(",('modal_upload','btn_upload','','en','Upload','')");
        SQLS.append(",('modal_upload','btn_upload','','fr','Uploader','')");
        SQLS.append(",('modal_upload','title','','en','Upload File','')");
        SQLS.append(",('modal_upload','title','','fr','Uploader Fichier','')");
        SQLS.append(",('multiselect','all_selected','','en','All selected','')");
        SQLS.append(",('multiselect','all_selected','','fr','Tous selectionner','')");
        SQLS.append(",('multiselect','none_selected','','en','None selected','')");
        SQLS.append(",('multiselect','none_selected','','fr','Selectionner aucun','')");
        SQLS.append(",('multiselect','nselected','','en','selected','')");
        SQLS.append(",('multiselect','nselected','','fr','Selectionné','')");
        SQLS.append(",('multiselect','search','','en','Search','')");
        SQLS.append(",('multiselect','search','','fr','Chercher','')");
        SQLS.append(",('multiselect','select_all','','en','Select all','')");
        SQLS.append(",('multiselect','select_all','','fr','Selectionner tous','')");
        SQLS.append(",('myversion','key','','en','Key','This is the reference of the component inside Cerberus that we want to keep track of the version.')");
        SQLS.append(",('myversion','value','','en','Value','This is the version that correspond to the key.')");
        SQLS.append(",('page_application','button_create','','en','Create new Application','')");
        SQLS.append(",('page_application','button_create','','fr','Créer une nouvelle Application','')");
        SQLS.append(",('page_application','button_delete','','en','Delete Application','')");
        SQLS.append(",('page_application','button_delete','','fr','Supprimer l\\'Application','')");
        SQLS.append(",('page_application','button_edit','','en','Edit Application','')");
        SQLS.append(",('page_application','button_edit','','fr','Modifier l\\'Application','')");
        SQLS.append(",('page_application','message_delete','','en','Do you want to delete application <b>\\'%ENTRY%\\'</b> ?<br>WARNING1 : All corresponding Test Cases will be removed as well !!!<br>WARNING2 : All associated Test Cases executions will also be removed !!!','')");
        SQLS.append(",('page_application','message_delete','','fr','Confirmez vous la suppression de l\\'application <b>\\'%ENTRY%\\'</b> ?<br> ATTENTION1 : Tous les Cas de Test associés seront également supprimés !!!<br>ATTENTION2 : Toutes les Executions associées seront également supprimées !!!','')");
        SQLS.append(",('page_application','tabDef','','en','Definition','')");
        SQLS.append(",('page_application','tabDef','','fr','Definition','')");
        SQLS.append(",('page_application','tabEnv','','en','Environments','')");
        SQLS.append(",('page_application','tabEnv','','fr','Environnements','')");
        SQLS.append(",('page_application','title','','en','APPLICATION','This page can be used to manage the applications.')");
        SQLS.append(",('page_application','title','','fr','APPLICATION','Cette page permet de gérer et créer des applications.')");
        SQLS.append(",('page_batchinvariant','button_create','','en','Create new Batch','')");
        SQLS.append(",('page_batchinvariant','button_create','','fr','Créer un nouveau Batch','')");
        SQLS.append(",('page_batchinvariant','button_delete','','en','Delete Batch','')");
        SQLS.append(",('page_batchinvariant','button_delete','','fr','Supprimer le Batch','')");
        SQLS.append(",('page_batchinvariant','button_edit','','en','Edit Batch','')");
        SQLS.append(",('page_batchinvariant','button_edit','','fr','Modifier le Batch','')");
        SQLS.append(",('page_batchinvariant','message_delete','','en','Do you want to delete Batch <b>\\'%ENTRY%\\'</b> ?<br>WARNING1 : All corresponding Batch execution history will be deleted !!!','')");
        SQLS.append(",('page_batchinvariant','message_delete','','fr','Confirmez vous la suppression du Batch <b>\\'%ENTRY%\\'</b> ?<br> ATTENTION1 : Tous les Historiques d\\'executions seront suprimés !!!','')");
        SQLS.append(",('page_batchinvariant','title','','en','BATCH','This page can be used in order to manage the batch per system.')");
        SQLS.append(",('page_batchinvariant','title','','fr','BATCH','Cette page permet de gérer et créer des batch pour chaque systeme.')");
        SQLS.append(",('page_buildcontent','buildFrom','','en','From Build/Rev','')");
        SQLS.append(",('page_buildcontent','buildFrom','','fr','De Build/Rev','')");
        SQLS.append(",('page_buildcontent','buildTo','','en','To Build/Rev','')");
        SQLS.append(",('page_buildcontent','buildTo','','fr','Vers Build/Rev','')");
        SQLS.append(",('page_buildcontent','buttonInstallInstruction','','en','See Installation Instructions','')");
        SQLS.append(",('page_buildcontent','buttonInstallInstruction','','fr','Voir Instructions d\\'installation','')");
        SQLS.append(",('page_buildcontent','buttonLoadAll','','en','Load All Build','')");
        SQLS.append(",('page_buildcontent','buttonLoadAll','','fr','Charger tous','')");
        SQLS.append(",('page_buildcontent','buttonLoadLatest','','en','Load latest Build','')");
        SQLS.append(",('page_buildcontent','buttonLoadLatest','','fr','Charger le dernier Build','')");
        SQLS.append(",('page_buildcontent','buttonLoadPending','','en','Load pending Build','')");
        SQLS.append(",('page_buildcontent','buttonLoadPending','','fr','Charger le Build encours','')");
        SQLS.append(",('page_buildcontent','button_create','','en','Create new Build Content','')");
        SQLS.append(",('page_buildcontent','button_create','','fr','Créer un nouveau contenu au Build','')");
        SQLS.append(",('page_buildcontent','button_delete','','en','Delete Build Content','')");
        SQLS.append(",('page_buildcontent','button_delete','','fr','Supprimer l\\'entrée du contenu du Build','')");
        SQLS.append(",('page_buildcontent','button_edit','','en','Edit Build Content','')");
        SQLS.append(",('page_buildcontent','button_edit','','fr','Editer le contenu du Build','')");
        SQLS.append(",('page_buildcontent','delete','','en','Dlt','Select this checkbox and then save changes in order to delete the row.')");
        SQLS.append(",('page_buildcontent','delete','','fr','Dlt','Select this checkbox and then save changes in order to delete the row.')");
        SQLS.append(",('page_buildcontent','filters','','en','Filters','')");
        SQLS.append(",('page_buildcontent','filters','','fr','Filtres','')");
        SQLS.append(",('page_buildcontent','InstallInstructions','','en','Installation instructions','')");
        SQLS.append(",('page_buildcontent','InstallInstructions','','fr','Instructions d\\'installation','')");
        SQLS.append(",('page_buildcontent','list','','en','Build Content List','')");
        SQLS.append(",('page_buildcontent','list','','fr','Liste du contenu du Build','')");
        SQLS.append(",('page_buildcontent','massAction','','en','Massively update the selected release','')");
        SQLS.append(",('page_buildcontent','massAction','','fr','Mise à jour massive des releases selectionnées','')");
        SQLS.append(",('page_buildcontent','message_delete','','en','Do you want to delete release entry <b>\\'%ENTRY%\\'</b> ?<br> NB : It correspond to the release <b>\\'%RELEASE%\\'</b> of application <b>\\'%APPLI%\\'</b> of Build <b>\\'%BUILD%\\'</b> Revision <b>\\'%REVISION%\\'</b>.','')");
        SQLS.append(",('page_buildcontent','message_delete','','fr','Confirmez vous la suppression de l\\'entrée <b>\\'%ENTRY%\\'</b> ?<br> NB : correspond à la release <b>\\'%RELEASE%\\'</b> de l\\'application <b>\\'%APPLI%\\'</b> du Build <b>\\'%BUILD%\\'</b> Revision <b>\\'%REVISION%\\'</b>.','')");
        SQLS.append(",('page_buildcontent','message_instruction','','en','Please specify a build and a revision to get the installation instructions!',NULL)");
        SQLS.append(",('page_buildcontent','message_instruction','','fr','Merci de specifier un build et une revision avant d\\'obtenir les instructions d\\'installation! ',NULL)");
        SQLS.append(",('page_buildcontent','message_massAction','','en','Massively update the selected release',NULL)");
        SQLS.append(",('page_buildcontent','message_massAction','','fr','Mettre à jours en masse les releases selectionnées',NULL)");
        SQLS.append(",('page_buildcontent','message_massActionError1','','en','Please select at least 1 line before trying to perform a mass action!',NULL)");
        SQLS.append(",('page_buildcontent','message_massActionError1','','fr','Merci de selection 1 élément au minimum avant de faire une action en masse !',NULL)");
        SQLS.append(",('page_buildcontent','standardfilters','','en','Standard Filters','')");
        SQLS.append(",('page_buildcontent','standardfilters','','fr','Standard Filters','')");
        SQLS.append(",('page_buildcontent','title','','en','BUILD CONTENT','This page can be used to manage the Build Content.')");
        SQLS.append(",('page_buildcontent','title','','fr','CONTENU DES BUILDS','Cette page permet de gérer le contenu d\\'un build.')");
        SQLS.append(",('page_buildrevdefinition','button_create','','en','Create new Build Revision Definition','')");
        SQLS.append(",('page_buildrevdefinition','button_create','','fr','Créer une nouvelle definition de build ou revision','')");
        SQLS.append(",('page_buildrevdefinition','button_delete','','en','Delete Build Revision Definition','')");
        SQLS.append(",('page_buildrevdefinition','button_delete','','fr','Supprimer la definition de build ou revision','')");
        SQLS.append(",('page_buildrevdefinition','button_edit','','en','Edit Build Revision Definition','')");
        SQLS.append(",('page_buildrevdefinition','button_edit','','fr','Editer une nouvelle definition de build ou revision','')");
        SQLS.append(",('page_buildrevdefinition','title','','en','BUILD REVISION DEFINITION','This page can be used to manage the definition of Builds and Revisions.')");
        SQLS.append(",('page_buildrevdefinition','title','','fr','DEFINITION DES BUILDS REVISIONS','Cette page permet de gérer la definition d\\'un build.')");
        SQLS.append(",('page_deploytype','button_create','','en','Create new Deployment Type','')");
        SQLS.append(",('page_deploytype','button_create','','fr','Créer un nouveau Type de Deploiement','')");
        SQLS.append(",('page_deploytype','button_delete','','en','Delete Deployment Type','')");
        SQLS.append(",('page_deploytype','button_delete','','fr','Supprimer le Type de Deploiement','')");
        SQLS.append(",('page_deploytype','button_edit','','en','Edit Deployment Type','')");
        SQLS.append(",('page_deploytype','button_edit','','fr','Modifier le Type de Deploiement','')");
        SQLS.append(",('page_deploytype','message_delete','','en','Do you want to delete Deploy Type <b>\\'%ENTRY%\\'</b> ?<br>WARNING1 : All corresponding Application will lose the link to that deleted Deploy Type !!!<br>WARNING2 : All associated Jenkins Agent on corresponding Environments will be deleted !!!','')");
        SQLS.append(",('page_deploytype','message_delete','','fr','Confirmez vous la suppression du Type de Deploiement <b>\\'%ENTRY%\\'</b> ?<br> ATTENTION1 : Toutes les Applications associées vont perdre le lien avec ce Deploy Type !!!<br>ATTENTION2 : Tous les Jenkins Agent associés et Environnements associés seront supprimés !!!','')");
        SQLS.append(",('page_deploytype','title','','en','DEPLOYMENT TYPE','This page can be used in order to manage the deployment types.')");
        SQLS.append(",('page_deploytype','title','','fr','TYPE DE DEPLOIEMENT','Cette page permet de gérer et créer des types de deploiements.')");
        SQLS.append(",('page_environment','buttonPreviewNotification','','en','Preview Notification','')");
        SQLS.append(",('page_environment','buttonPreviewNotification','','fr','Prévisualiser la notification','')");
        SQLS.append(",('page_environment','button_create','','en','Create a new Environment','')");
        SQLS.append(",('page_environment','button_create','','fr','Créer un nouvel Environnement','')");
        SQLS.append(",('page_environment','button_delete','','en','Delete Environment','')");
        SQLS.append(",('page_environment','button_delete','','fr','Supprimer l\\'Environnement','')");
        SQLS.append(",('page_environment','button_disable','','en','Disable Environment','')");
        SQLS.append(",('page_environment','button_disable','','fr','Désactiver l\\'Environnement','')");
        SQLS.append(",('page_environment','button_disable1','','en','Disable and Send Notification','')");
        SQLS.append(",('page_environment','button_disable1','','fr','Désactiver et envoyer la Notification','')");
        SQLS.append(",('page_environment','button_edit','','en','Edit Environment','')");
        SQLS.append(",('page_environment','button_edit','','fr','Modifier l\\'Environnement','')");
        SQLS.append(",('page_environment','button_enable','','en','Enable Environment with new Build Revision','')");
        SQLS.append(",('page_environment','button_enable','','fr','Activer l\\'Environnement avec un nouveau Build et Revision','')");
        SQLS.append(",('page_environment','button_enable1','','en','Enable and Sent Notification','')");
        SQLS.append(",('page_environment','button_enable1','','fr','Activer et Envoyer la Notification','')");
        SQLS.append(",('page_environment','button_newChain','','en','New Event Chain','')");
        SQLS.append(",('page_environment','button_newChain','','fr','Nouvel Evenement de Chaine','')");
        SQLS.append(",('page_environment','button_newChain1','','en','Create New Chain Event and Send Notification','')");
        SQLS.append(",('page_environment','button_newChain1','','fr','Créer un nouvel événement de Chaine et Envoyer la Notification','')");
        SQLS.append(",('page_environment','button_view','','en','View Environment','')");
        SQLS.append(",('page_environment','button_view','','fr','Voir Environnement','')");
        SQLS.append(",('page_environment','cc','','en','CC','')");
        SQLS.append(",('page_environment','cc','','fr','Copie','')");
        SQLS.append(",('page_environment','currentBuild','','en','Current Build','')");
        SQLS.append(",('page_environment','currentBuild','','fr','Build courant','')");
        SQLS.append(",('page_environment','currentRevision','','en','Current Revision','')");
        SQLS.append(",('page_environment','currentRevision','','fr','Revision courante','')");
        SQLS.append(",('page_environment','list','','en','Environment list','')");
        SQLS.append(",('page_environment','list','','fr','Liste des environnements','')");
        SQLS.append(",('page_environment','listChange','','en','Change list','')");
        SQLS.append(",('page_environment','listChange','','fr','Liste des changements','')");
        SQLS.append(",('page_environment','listEvent','','en','Batch list','')");
        SQLS.append(",('page_environment','listEvent','','fr','Liste des batchs','')");
        SQLS.append(",('page_environment','message_delete','','en','Do you want to delete environment <b>\\'%ENVIRONMENT%\\'</b> from country <b>\\'%COUNTRY%\\'</b> and system <b>\\'%SYSTEM%\\'</b> ?<br>WARNING : All corresponding parameters such as list of applications, databases and other environments dependencies will be removed !!!','')");
        SQLS.append(",('page_environment','message_delete','','fr','Confirmez vous la suppression de l\\'environnement <b>\\'%ENVIRONMENT%\\'</b> du pays <b>\\'%COUNTRY%\\'</b> du système <b>\\'%SYSTEM%\\'</b> ?<br> ATTENTION : Tous les parametres associées tel que la liste des applications, database et autres dependances d\\'environnements seront supprimés !!!','')");
        SQLS.append(",('page_environment','newBuild','','en','New Build','')");
        SQLS.append(",('page_environment','newBuild','','fr','Nouveau Build','')");
        SQLS.append(",('page_environment','newRevision','','en','New Revision','')");
        SQLS.append(",('page_environment','newRevision','','fr','Nouvelle Revision','')");
        SQLS.append(",('page_environment','subject','','en','Subject','')");
        SQLS.append(",('page_environment','subject','','fr','Sujet','')");
        SQLS.append(",('page_environment','tabApplication','','en','Applications','')");
        SQLS.append(",('page_environment','tabApplication','','fr','Applications','')");
        SQLS.append(",('page_environment','tabBuild','','en','Build/Revision','')");
        SQLS.append(",('page_environment','tabBuild','','fr','Build/Revision','')");
        SQLS.append(",('page_environment','tabChain','','en','Chain','')");
        SQLS.append(",('page_environment','tabChain','','fr','Chain','')");
        SQLS.append(",('page_environment','tabDatabase','','en','Databases','')");
        SQLS.append(",('page_environment','tabDatabase','','fr','Bases de Données','')");
        SQLS.append(",('page_environment','tabDefinition','','en','Definition','')");
        SQLS.append(",('page_environment','tabDefinition','','fr','Definition','')");
        SQLS.append(",('page_environment','tabDependencies','','en','Dependencies','')");
        SQLS.append(",('page_environment','tabDependencies','','fr','Dépendances','')");
        SQLS.append(",('page_environment','tabDeploy','','en','Deploy Types','')");
        SQLS.append(",('page_environment','tabDeploy','','fr','Type de déploiements','')");
        SQLS.append(",('page_environment','tabInstallInstruction','','en','Installation Instructions','')");
        SQLS.append(",('page_environment','tabInstallInstruction','','fr','Instruction d\\'installation','')");
        SQLS.append(",('page_environment','tabNotif','','en','Specific Notifications','')");
        SQLS.append(",('page_environment','tabNotif','','fr','Notifications Spécifiques','')");
        SQLS.append(",('page_environment','tabPreview','','en','EMail Preview','')");
        SQLS.append(",('page_environment','tabPreview','','fr','Previsu de l\\'Email','')");
        SQLS.append(",('page_environment','title','','en','ENVIRONMENT','This page can be used to manage the environments.')");
        SQLS.append(",('page_environment','title','','fr','ENVIRONNEMENT','Cette page permet de gérer et créer des environnements.')");
        SQLS.append(",('page_environment','to','','en','To','')");
        SQLS.append(",('page_environment','to','','fr','Destinataire','')");
        SQLS.append(",('page_executiondetail','buildrevision','','en','BuildRev','Build and Revision of the <code class=\\'doc-crbvvoca\\'>environment</code> of the <code class=\\'doc-crbvvoca\\'>system</code> of the <code class=\\'doc-crbvvoca\\'>application</code> that has been tested.')");
        SQLS.append(",('page_executiondetail','buildrevisionlink','','en','BuildRev Linked','Build and Revision of the <code class=\\'doc-crbvvoca\\'>environment</code> of the linked <code class=\\'doc-crbvvoca\\'>system</code>. The linked systems are defined in the \\'Environment Dependancy\\' section of the <code class=\\'doc-crbvvoca\\'>environment</code> page.')");
        SQLS.append(",('page_executiondetail','SeleniumLog','','en','Media Files','Link to the media execution files (ex : selenium logs).')");
        SQLS.append(",('page_exeperbuildrevision','Days','','en','Days','Number of days with this revision for this build.')");
        SQLS.append(",('page_exeperbuildrevision','NbAPP','','en','Nb Appli','Number of distinct <code class=\\'doc-crbvvoca\\'>application</code> that has been tested.')");
        SQLS.append(",('page_exeperbuildrevision','NbExecution','','en','Exec','Number of <code class=\\'doc-crbvvoca\\'>test case</code> execution.')");
        SQLS.append(",('page_exeperbuildrevision','NbOK','','en','OK','Number of execution OK')");
        SQLS.append(",('page_exeperbuildrevision','NbTC','','en','Nb TC','Number of distinct <code class=\\'doc-crbvvoca\\'>test case</code> executed')");
        SQLS.append(",('page_exeperbuildrevision','nb_exe_per_tc','','en','Exec/TC','Average number of execution per <code class=\\'doc-crbvvoca\\'>test case</code>.')");
        SQLS.append(",('page_exeperbuildrevision','nb_tc_per_day','','en','Exec/TC/Day','Number of execution per <code class=\\'doc-crbvvoca\\'>test case</code> and per day')");
        SQLS.append(",('page_exeperbuildrevision','OK_percentage','','en','%OK','Number of OK / number of execution')");
        SQLS.append(",('page_exeperbuildrevision','RegressionExecutionStatus','','en','Regression Execution Status','This section report the execution statistics of regression testcases by the last sprint / Revision.<br>Criterias :<br>- On the applications that belong to current system.<br>- Test cases had to be in WORKING status at the time of the execution.<br>- Monitoring test cases are excluded<br>  (ie not <i>\\'Performance Monitor\\'</i> and not <i>\\'Business Activity Monitor\\'</i> and not <i>\\'Data Integrity Monitor\\'</i>)')");
        SQLS.append(",('page_exeperbuildrevision','RegressionExecutionStatus1','','en','Regression Execution Status on External Applications','This section report the execution statistics of regression testcases by the last sprint / Revision.<br>Criterias :<br>- On the applications that <b>does not</b> belong to current system.<br>- Test cases had to be in WORKING status at the time of the execution.<br>- Monitoring test cases are excluded<br>  (ie not <i>\\'Performance Monitor\\'</i> and not <i>\\'Business Activity Monitor\\'</i> and not <i>\\'Data Integrity Monitor\\'</i>)')");
        SQLS.append(",('page_global','btn_add','','en','Add','')");
        SQLS.append(",('page_global','btn_add','','fr','Ajouter','')");
        SQLS.append(",('page_global','btn_cancel','','en','Cancel','')");
        SQLS.append(",('page_global','btn_cancel','','fr','Annuler','')");
        SQLS.append(",('page_global','btn_clearfilter','','en','','')");
        SQLS.append(",('page_global','btn_clearfilter','','fr','','')");
        SQLS.append(",('page_global','btn_duplicate','','en','Duplicate','')");
        SQLS.append(",('page_global','btn_duplicate','','fr','Dupliquer','')");
        SQLS.append(",('page_global','btn_export','','en','Export','')");
        SQLS.append(",('page_global','btn_restoreuserpreferences','','en','Load','')");
        SQLS.append(",('page_global','btn_restoreuserpreferences','','fr','Charger','')");
        SQLS.append(",('page_global','btn_savetableconfig','','en','Save','')");
        SQLS.append(",('page_global','btn_savetableconfig','','fr','Sauvegarder','')");
        SQLS.append(",('page_global','btn_showHideColumns','','en','Show/Hide','')");
        SQLS.append(",('page_global','btn_showHideColumns','','fr','Afficher/Cacher','')");
        SQLS.append(",('page_global','buttonAdd','','en','Save','')");
        SQLS.append(",('page_global','buttonAdd','','fr','Sauvegarder','')");
        SQLS.append(",('page_global','buttonClose','','en','Close','')");
        SQLS.append(",('page_global','buttonClose','','fr','Fermer','')");
        SQLS.append(",('page_global','buttonConfirm','','en','Yes','')");
        SQLS.append(",('page_global','buttonConfirm','','fr','Oui','')");
        SQLS.append(",('page_global','buttonDismiss','','en','No','')");
        SQLS.append(",('page_global','buttonDismiss','','fr','Non','')");
        SQLS.append(",('page_global','buttonLoad','','en','Load','')");
        SQLS.append(",('page_global','buttonLoad','','fr','Charger','')");
        SQLS.append(",('page_global','button_massAction','','en','Mass Action',NULL)");
        SQLS.append(",('page_global','button_massAction','','fr','Action en masse',NULL)");
        SQLS.append(",('page_global','columnAction','','en','Actions','')");
        SQLS.append(",('page_global','columnAction','','fr','Actions','')");
        SQLS.append(",('page_global','export_data','','en','Export Data','')");
        SQLS.append(",('page_global','filters','','en','Filters','')");
        SQLS.append(",('page_global','filters','','fr','Filtres','')");
        SQLS.append(",('page_global','footer_bug','','en','Open a bug or ask for any new feature <a target=\"_blank\"  href=\"%LINK%\">here</a>.','')");
        SQLS.append(",('page_global','footer_bug','','fr','Ouvrir un bug ou envoyer une demande d\\'évolution <a target=\"_blank\"  href=\"%LINK%\">ici</a>.','')");
        SQLS.append(",('page_global','footer_text','','en','Page started generating on %DATE% by %VERSION% in %ENV% and took %TIMING%ms','')");
        SQLS.append(",('page_global','footer_text','','fr','Page générée le %DATE% par %VERSION% en environment : %ENV% et a pris %TIMING%ms','')");
        SQLS.append(",('page_global','invalid_extension_message','','en','Please select a file with the extension ','')");
        SQLS.append(",('page_global','invalid_extension_message','','fr','Merci de selectionner un fichier avec l\\'extension ','')");
        SQLS.append(",('page_global','lbl_all','','en','All','')");
        SQLS.append(",('page_global','lbl_all','','fr','Tous','')");
        SQLS.append(",('page_global','message_delete','','en','Do you want to delete <b>\\'%ENTRY%\\'</b> %TABLE% ?','')");
        SQLS.append(",('page_global','message_delete','','fr','Voulez vous supprimer le %TABLE% <b>\\'%ENTRY%\\'</b> ?','')");
        SQLS.append(",('page_global','processing','','en','Processing…','')");
        SQLS.append(",('page_global','processing','','fr','Traitement en cours...','')");
        SQLS.append(",('page_global','tooltip_clearfilter','','en','Clear filters applied','')");
        SQLS.append(",('page_global','tooltip_clearfilter','','fr','Effacer les filtres appliqués','')");
        SQLS.append(",('page_global','tooltip_column_filter_empty','','en','Filter',NULL)");
        SQLS.append(",('page_global','tooltip_column_filter_empty','','fr','Filtre',NULL)");
        SQLS.append(",('page_global','tooltip_column_filter_filtered','','en','Filtered value(s)',NULL)");
        SQLS.append(",('page_global','tooltip_column_filter_filtered','','fr','Valeur(s) filtrée(s)',NULL)");
        SQLS.append(",('page_global','tooltip_delete_item','','en','This item will be removed from the database','')");
        SQLS.append(",('page_global','tooltip_delete_item','','fr','Cette ligne sera supprimée de la base','')");
        SQLS.append(",('page_global','tooltip_mark_remove','','en','Mark item to be removed from the database','')");
        SQLS.append(",('page_global','tooltip_mark_remove','','fr','Selectionner la ligne pour la supprimer de la base','')");
        SQLS.append(",('page_global','tooltip_massAction','','en','Select All or None to perform Mass Action',NULL)");
        SQLS.append(",('page_global','tooltip_massAction','','fr','Selectionner ou Déselectionner tous pour Action en masse',NULL)");
        SQLS.append(",('page_global','tooltip_massActionLine','','en','Select to perform Mass Action on that line',NULL)");
        SQLS.append(",('page_global','tooltip_massActionLine','','fr','Selection pour Action en masse',NULL)");
        SQLS.append(",('page_global','tooltip_restoreuserpreferences','','en','Restore the table configuration','Restore the table configuration from the user preferences. Filters and column display are available in user preferences')");
        SQLS.append(",('page_global','tooltip_restoreuserpreferences','','fr','Restaurer la configuration de la table','Restaurer la configuration de la table à partir des préférences utilisateur. Les filtres et l\\'affichage des colonnes sont chargés à partir des préférences utilisateur')");
        SQLS.append(",('page_global','tooltip_savetableconfig','','en','Save the table configuration.','Save the table configuration. Filters and column display is stored in user preferences')");
        SQLS.append(",('page_global','tooltip_savetableconfig','','fr','Sauvegarder la configuration de la table.','Sauvegarder la configuration de la table. Les filtres et l\\'affichage des colonnes sont sauvegardés dans les préférences utilisateur')");
        SQLS.append(",('page_global','tooltip_showHideColumns','','en','Show/hide columns','')");
        SQLS.append(",('page_global','tooltip_showHideColumns','','fr','Afficher/cacher des colonnes','')");
        SQLS.append(",('page_global','unexpected_error_message','','en','Unable to perform the task. An unexpected error has happened!','')");
        SQLS.append(",('page_global','unexpected_error_message','','fr','Impossible de finaliser l\\'operation. Une erreur inattendue est survenue','')");
        SQLS.append(",('page_header','logout','','en','Logout','')");
        SQLS.append(",('page_header','logout','','fr','Déconnexion','')");
        SQLS.append(",('page_header','menuAdmin','','en','Administration','')");
        SQLS.append(",('page_header','menuAdmin','','fr','Administration','')");
        SQLS.append(",('page_header','menuApplications','','en','Application','')");
        SQLS.append(",('page_header','menuApplications','','fr','Application','')");
        SQLS.append(",('page_header','menuBatchInvariant','','en','Batch','')");
        SQLS.append(",('page_header','menuBatchInvariant','','fr','Batch','')");
        SQLS.append(",('page_header','menuBuildContent','','en','Build Content','')");
        SQLS.append(",('page_header','menuBuildContent','','fr','Contenu des Builds','')");
        SQLS.append(",('page_header','menuBuildRevision','','en','Build Revision Definition','')");
        SQLS.append(",('page_header','menuBuildRevision','','fr','Définition des Builds Revisions','')");
        SQLS.append(",('page_header','menuCreateTest','','en','Create Test','')");
        SQLS.append(",('page_header','menuCreateTest','','fr','Créér un Test','')");
        SQLS.append(",('page_header','menuCreateTestCase','','en','Create TestCase','')");
        SQLS.append(",('page_header','menuCreateTestCase','','fr','Créér un Cas de Test','')");
        SQLS.append(",('page_header','menuData','','en','Data','')");
        SQLS.append(",('page_header','menuData','','fr','Données','')");
        SQLS.append(",('page_header','menuDatabaseMaintenance','','en','Database Maintenance','')");
        SQLS.append(",('page_header','menuDatabaseMaintenance','','fr','Maintenance de la base de données','')");
        SQLS.append(",('page_header','menuDeployType','','en','Deploiement Type','')");
        SQLS.append(",('page_header','menuDeployType','','fr','Type de Deploiement','')");
        SQLS.append(",('page_header','menuDocumentation','','en','Documentation',NULL)");
        SQLS.append(",('page_header','menuDocumentation','','fr','Documentation',NULL)");
        SQLS.append(",('page_header','menuEditTest','','en','Test','')");
        SQLS.append(",('page_header','menuEditTest','','fr','Test','')");
        SQLS.append(",('page_header','menuEditTestCase','','en','Edit TestCase','')");
        SQLS.append(",('page_header','menuEditTestCase','','fr','Editer un Cas de Test','')");
        SQLS.append(",('page_header','menuEnvironmentManagement','','en','Environment Management','')");
        SQLS.append(",('page_header','menuEnvironmentManagement','','fr','Gestion des Environnements','')");
        SQLS.append(",('page_header','menuEnvironments','','en','Environment','')");
        SQLS.append(",('page_header','menuEnvironments','','fr','Environnement','')");
        SQLS.append(",('page_header','menuExecutionPerBuildRevision','','en','Execution per Build/Rev','')");
        SQLS.append(",('page_header','menuExecutionPerBuildRevision','','fr','Execution par Build/Rev','')");
        SQLS.append(",('page_header','menuExecutionReporting','','en','Execution Reporting','')");
        SQLS.append(",('page_header','menuExecutionReporting','','fr','Rapport d\\'Execution','')");
        SQLS.append(",('page_header','menuHelp','','en','Help',NULL)");
        SQLS.append(",('page_header','menuHelp','','fr','Aide',NULL)");
        SQLS.append(",('page_header','menuIntegration','','en','Integration','')");
        SQLS.append(",('page_header','menuIntegration','','fr','Intégration','')");
        SQLS.append(",('page_header','menuIntegrationStatus','','en','Integration Status','')");
        SQLS.append(",('page_header','menuIntegrationStatus','','fr','Etat d\\'Intégration','')");
        SQLS.append(",('page_header','menuInvariantPrivate','','en','See Private Invariants','')");
        SQLS.append(",('page_header','menuInvariantPrivate','','fr','Voir les Invariants Privés','')");
        SQLS.append(",('page_header','menuInvariantPublic','','en','Public Invariants Management','')");
        SQLS.append(",('page_header','menuInvariantPublic','','fr','Gestion des Invariants Publics','')");
        SQLS.append(",('page_header','menuInvariants','','en','Invariants','')");
        SQLS.append(",('page_header','menuInvariants','','fr','Invariants','')");
        SQLS.append(",('page_header','menuLabel','','en','Label','')");
        SQLS.append(",('page_header','menuLabel','','fr','Label','')");
        SQLS.append(",('page_header','menuLogViewer','','en','Log Viewer','')");
        SQLS.append(",('page_header','menuLogViewer','','fr','Journal de Modifications','')");
        SQLS.append(",('page_header','menuParameter','','en','Parameters','')");
        SQLS.append(",('page_header','menuParameter','','fr','Paramètres','')");
        SQLS.append(",('page_header','menuProject','','en','Project','')");
        SQLS.append(",('page_header','menuProject','','fr','Projet','')");
        SQLS.append(",('page_header','menuReportingExecutionByTag','','en','Execution Report By Tag','')");
        SQLS.append(",('page_header','menuReportingExecutionByTag','','fr','Rapport d\\'Execution par Tag','')");
        SQLS.append(",('page_header','menuReportingExecutionDetail','','en','Execution Detail','')");
        SQLS.append(",('page_header','menuReportingExecutionDetail','','fr','Détails d\\'Execution','')");
        SQLS.append(",('page_header','menuReportingExecutionStatus','','en','Execution Status','')");
        SQLS.append(",('page_header','menuReportingExecutionStatus','','fr','Etats d\\'Execution','')");
        SQLS.append(",('page_header','menuReportingExecutionThreadMonitoring','','en','Cerberus Monitoring','')");
        SQLS.append(",('page_header','menuReportingExecutionThreadMonitoring','','fr','Monitoring Cerberus','')");
        SQLS.append(",('page_header','menuReportingExecutionTime','','en','Execution Time','')");
        SQLS.append(",('page_header','menuReportingExecutionTime','','fr','Temps d\\'Execution','')");
        SQLS.append(",('page_header','menuRobot','','en','Robot','')");
        SQLS.append(",('page_header','menuRobot','','fr','Robot','')");
        SQLS.append(",('page_header','menuRun','','en','Run','')");
        SQLS.append(",('page_header','menuRun','','fr','Executer','')");
        SQLS.append(",('page_header','menuRunTest','','en','Run Test Case','')");
        SQLS.append(",('page_header','menuRunTest','','fr','Executer un Cas de Test','')");
        SQLS.append(",('page_header','menuRunTestCase','','en','Run Test Case','')");
        SQLS.append(",('page_header','menuRunTestCase','','fr','Executer un Cas de Test','')");
        SQLS.append(",('page_header','menuRunTestSeePendingExecution','','en','See Execution In Queue','')");
        SQLS.append(",('page_header','menuRunTestSeePendingExecution','','fr','Execution en Attente','')");
        SQLS.append(",('page_header','menuRunTestTriggerBatchExecution','','en','Run Multiple Test','')");
        SQLS.append(",('page_header','menuRunTestTriggerBatchExecution','','fr','Executer plusieurs Cas de Test','')");
        SQLS.append(",('page_header','menuSearchTestCase','','en','Search TestCase','')");
        SQLS.append(",('page_header','menuSearchTestCase','','fr','Rechercher un Cas de Test','')");
        SQLS.append(",('page_header','menuSoapLibrary','','en','SOAP Library','')");
        SQLS.append(",('page_header','menuSoapLibrary','','fr','Bibliothèque de WebService SOAP','')");
        SQLS.append(",('page_header','menuSqlLibrary','','en','SQL Library','')");
        SQLS.append(",('page_header','menuSqlLibrary','','fr','Bibliothèque de script SQL','')");
        SQLS.append(",('page_header','menuTest','','en','Test','')");
        SQLS.append(",('page_header','menuTest','','fr','Test','')");
        SQLS.append(",('page_header','menuTestBattery','','en','TestCase Battery','')");
        SQLS.append(",('page_header','menuTestBattery','','fr','Batterie de Cas de Test','')");
        SQLS.append(",('page_header','menuTestCampaign','','en','Test Campaign','')");
        SQLS.append(",('page_header','menuTestCampaign','','fr','Campagne de Test','')");
        SQLS.append(",('page_header','menuTestCase','','en','TestCase','')");
        SQLS.append(",('page_header','menuTestCase','','fr','Cas De Test','')");
        SQLS.append(",('page_header','menuTestCaseList','','en','TestCase List','')");
        SQLS.append(",('page_header','menuTestCaseList','','fr','Liste des Cas de Test','')");
        SQLS.append(",('page_header','menuTestData','','en','Test Data','')");
        SQLS.append(",('page_header','menuTestData','','fr','Données de Test','')");
        SQLS.append(",('page_header','menuTestDataLib','','en','Data Library','')");
        SQLS.append(",('page_header','menuTestDataLib','','fr','Bibliothèque de Données','')");
        SQLS.append(",('page_header','menuTestPerApplication','','en','Test Per Application','')");
        SQLS.append(",('page_header','menuTestPerApplication','','fr','Liste de Tests par Application','')");
        SQLS.append(",('page_header','menuUsersManager','','en','User Management','')");
        SQLS.append(",('page_header','menuUsersManager','','fr','Gestion des Utilisateurs','')");
        SQLS.append(",('page_integrationstatus','DEV','','en','DEV','Nb of DEV active <code class=\\'doc-crbvvoca\\'>environments</code> on that Specific Version.')");
        SQLS.append(",('page_integrationstatus','DEV','','fr','DEV','')");
        SQLS.append(",('page_integrationstatus','environmentStatus','','en','Environment Status','')");
        SQLS.append(",('page_integrationstatus','environmentStatus','','fr','Statut des environnements','')");
        SQLS.append(",('page_integrationstatus','lastChanges','','en','Last Changes','')");
        SQLS.append(",('page_integrationstatus','lastChanges','','fr','Derniers Changements','')");
        SQLS.append(",('page_integrationstatus','PROD','','en','PROD','Nb of PROD active <code class=\\'doc-crbvvoca\\'>environments</code> on that Specific Version.')");
        SQLS.append(",('page_integrationstatus','PROD','','fr','PROD','')");
        SQLS.append(",('page_integrationstatus','QA','','en','QA','Nb of QA active <code class=\\'doc-crbvvoca\\'>environments</code> on that Specific Version.')");
        SQLS.append(",('page_integrationstatus','QA','','fr','QA','')");
        SQLS.append(",('page_integrationstatus','title','','en','INTEGRATION STATUS','')");
        SQLS.append(",('page_integrationstatus','title','','fr','ETAT D\\'INTEGRATION','')");
        SQLS.append(",('page_integrationstatus','UAT','','en','UAT','Nb of UAT active <code class=\\'doc-crbvvoca\\'>environments</code> on that Specific Version.')");
        SQLS.append(",('page_integrationstatus','UAT','','fr','UAT','')");
        SQLS.append(",('page_invariant','addinvariant_field','','en','Add Invariant','')");
        SQLS.append(",('page_invariant','addinvariant_field','','fr','Ajouter un Invariant','')");
        SQLS.append(",('page_invariant','allInvariants','','en','Invariants','')");
        SQLS.append(",('page_invariant','allInvariants','','fr','Invariants','')");
        SQLS.append(",('page_invariant','button_col','','en','Actions','')");
        SQLS.append(",('page_invariant','button_col','','fr','Actions','')");
        SQLS.append(",('page_invariant','button_create','','en','Create Invariant','')");
        SQLS.append(",('page_invariant','button_create','','fr','Créer un Invariant','')");
        SQLS.append(",('page_invariant','button_edit','','en','Edit Invariant','')");
        SQLS.append(",('page_invariant','button_edit','','fr','Editer l Invariant','')");
        SQLS.append(",('page_invariant','description','','en','Description','')");
        SQLS.append(",('page_invariant','description','','fr','Description','')");
        SQLS.append(",('page_invariant','editinvariant_field','','en','Edit Invariant','')");
        SQLS.append(",('page_invariant','editinvariant_field','','fr','Editer un Invariant','')");
        SQLS.append(",('page_invariant','gp1','','en','Group 1','')");
        SQLS.append(",('page_invariant','gp1','','fr','Groupe 1','')");
        SQLS.append(",('page_invariant','gp2','','en','Group 2','')");
        SQLS.append(",('page_invariant','gp2','','fr','Groupe 2','')");
        SQLS.append(",('page_invariant','gp3','','en','Group 3','')");
        SQLS.append(",('page_invariant','gp3','','fr','Groupe 3','')");
        SQLS.append(",('page_invariant','idname','','en','IdName','')");
        SQLS.append(",('page_invariant','idname','','fr','IdName','')");
        SQLS.append(",('page_invariant','message_remove','','en','Are you sure?','')");
        SQLS.append(",('page_invariant','message_remove','','fr','Etes vous sûrs?','')");
        SQLS.append(",('page_invariant','private','','en','Private','')");
        SQLS.append(",('page_invariant','private','','fr','Privés','')");
        SQLS.append(",('page_invariant','private_invariant','','en','Private InvariantS','')");
        SQLS.append(",('page_invariant','private_invariant','','fr','Invariants privés','')");
        SQLS.append(",('page_invariant','public','','en','Public','')");
        SQLS.append(",('page_invariant','public','','fr','Publiques','')");
        SQLS.append(",('page_invariant','public_invariant','','en','Public InvariantS','')");
        SQLS.append(",('page_invariant','public_invariant','','fr','Invariants publiques','')");
        SQLS.append(",('page_invariant','sort','','en','Sort','')");
        SQLS.append(",('page_invariant','sort','','fr','Tri','')");
        SQLS.append(",('page_invariant','title_remove','','en','Delete an Invariant','')");
        SQLS.append(",('page_invariant','title_remove','','fr','Supprimer un Invariant','')");
        SQLS.append(",('page_invariant','value','','en','Value','')");
        SQLS.append(",('page_invariant','value','','fr','Valeur','')");
        SQLS.append(",('page_invariant','veryShortDesc','','en','Very Short Description','')");
        SQLS.append(",('page_invariant','veryShortDesc','','fr','Petite Description','')");
        SQLS.append(",('page_label','btn_create','','en','Create Label','')");
        SQLS.append(",('page_label','btn_create','','fr','Créer un Label','')");
        SQLS.append(",('page_label','btn_delete','','en','Delete Label','')");
        SQLS.append(",('page_label','btn_delete','','fr','Effacer le Label','')");
        SQLS.append(",('page_label','btn_edit','','en','Edit Label','')");
        SQLS.append(",('page_label','btn_edit','','fr','Editer le Label','')");
        SQLS.append(",('page_label','btn_view','','en','View Label','')");
        SQLS.append(",('page_label','btn_view','','fr','Voir le Label','')");
        SQLS.append(",('page_label','display','','en','Display','Display the generated label from the label and the color defined')");
        SQLS.append(",('page_label','display','','fr','Affichage','Affiche le label généré à partir du label et de la couleur définis')");
        SQLS.append(",('page_label','title','','en','LABEL','This page can be used in order to manage the labels.')");
        SQLS.append(",('page_label','title','','fr','LABEL','Cette page permet de gérer et créer des labels.')");
        SQLS.append(",('page_logviewer','button_view','','en','Log entry detail','')");
        SQLS.append(",('page_logviewer','button_view','','fr','Detail du log','')");
        SQLS.append(",('page_logviewer','title','','en','LOG VIEWER','This page displays all the log messages from Cerberus.')");
        SQLS.append(",('page_logviewer','title','','fr','JOURNAL DE MODIFICATIONS','Cette page affiche tout les messages de log de Cerberus.')");
        SQLS.append(",('page_notification','Body','','en','Body','')");
        SQLS.append(",('page_notification','Cc','','en','Copy','')");
        SQLS.append(",('page_notification','Subject','','en','Subject','')");
        SQLS.append(",('page_notification','To','','en','To','')");
        SQLS.append(",('page_parameter','allParameters','','en','Parameters','')");
        SQLS.append(",('page_parameter','allParameters','','fr','Paramètres','')");
        SQLS.append(",('page_parameter','button_col','','en','Actions','')");
        SQLS.append(",('page_parameter','button_col','','fr','Actions','')");
        SQLS.append(",('page_parameter','button_edit','','en','Edit Parameter','')");
        SQLS.append(",('page_parameter','button_edit','','fr','Editer le parmètre','')");
        SQLS.append(",('page_parameter','cerberus_col','','en','Cerberus Value','')");
        SQLS.append(",('page_parameter','cerberus_col','','fr','Valeur de Cerberus','')");
        SQLS.append(",('page_parameter','cerberus_field','','en','Cerberus Value','')");
        SQLS.append(",('page_parameter','cerberus_field','','fr','Valeur de Cerberus','')");
        SQLS.append(",('page_parameter','close_btn','','en','Close','')");
        SQLS.append(",('page_parameter','close_btn','','fr','Fermer','')");
        SQLS.append(",('page_parameter','description_col','','en','Description','')");
        SQLS.append(",('page_parameter','description_col','','fr','Description','')");
        SQLS.append(",('page_parameter','description_field','','en','Description','')");
        SQLS.append(",('page_parameter','description_field','','fr','Description','')");
        SQLS.append(",('page_parameter','editparameter_field','','en','Edit Parameter','')");
        SQLS.append(",('page_parameter','editparameter_field','','fr','Modifier le paramètre','')");
        SQLS.append(",('page_parameter','parameter_col','','en','Parameter','')");
        SQLS.append(",('page_parameter','parameter_col','','fr','Paramètre','')");
        SQLS.append(",('page_parameter','parameter_field','','en','Parameter','')");
        SQLS.append(",('page_parameter','parameter_field','','fr','Paramètre','')");
        SQLS.append(",('page_parameter','save_btn','','en','Save','')");
        SQLS.append(",('page_parameter','save_btn','','fr','Sauvegarder','')");
        SQLS.append(",('page_parameter','system_col','','en','System Value','')");
        SQLS.append(",('page_parameter','system_col','','fr','Valeur du système','')");
        SQLS.append(",('page_parameter','system_field','','en','System Value','')");
        SQLS.append(",('page_parameter','system_field','','fr','Valeur du Système','')");
        SQLS.append(",('page_project','button_create','','en','Create new Project','')");
        SQLS.append(",('page_project','button_create','','fr','Créer un nouveau Projet','')");
        SQLS.append(",('page_project','button_delete','','en','Delete Project','')");
        SQLS.append(",('page_project','button_delete','','fr','Supprimer le Projet','')");
        SQLS.append(",('page_project','button_edit','','en','Edit Project','')");
        SQLS.append(",('page_project','button_edit','','fr','Modifier le Projet','')");
        SQLS.append(",('page_project','message_delete','','en','Do you want to delete Project <b>\\'%ENTRY%\\'</b> ?<br>WARNING : All corresponding Test Cases will lose the link to that deleted Project !!!','')");
        SQLS.append(",('page_project','message_delete','','fr','Confirmez vous la suppression du projet <b>\\'%ENTRY%\\'</b> ?<br> ATTENTION : Tous les Cas de Tests associés vont perdre le lien avec ce projet !!!','')");
        SQLS.append(",('page_project','title','','en','PROJECT','This page can be used in order to manage the projets.')");
        SQLS.append(",('page_project','title','','fr','PROJET','Cette page permet de gérer et créer des projets.')");
        SQLS.append(",('page_reportbytag','btn_select_table','','en','Select table','')");
        SQLS.append(",('page_reportbytag','button_load','','en','Load','')");
        SQLS.append(",('page_reportbytag','button_load','','fr','Charger','')");
        SQLS.append(",('page_reportbytag','button_reload','','en','Reload','')");
        SQLS.append(",('page_reportbytag','button_reload','','fr','Recharger','')");
        SQLS.append(",('page_reportbytag','filters','','en','Filters','Filters for the report')");
        SQLS.append(",('page_reportbytag','filters','','fr','Filtres','Filtres du rapport')");
        SQLS.append(",('page_reportbytag','report_envcountrybrowser','','en','Report By Application Environement Country Browser','Report of the execution filtering by Application Environement Country and Browser')");
        SQLS.append(",('page_reportbytag','report_envcountrybrowser','','fr','Rapport par Application Envrionnement Pays Navigateur','Rapport d\\'execution filtré par Application Envrionnment Pays et Navigateur')");
        SQLS.append(",('page_reportbytag','report_function','','en','Report by Function','A bar chart with the number of execution and their status for each function')");
        SQLS.append(",('page_reportbytag','report_function','','fr','Rapport par Fonction','diagramme en bâtons contenant le nombre d\\'execution par status pour chaque fonction')");
        SQLS.append(",('page_reportbytag','report_list','','en','List','A list of all test case execution for each Environement, Country and Browser')");
        SQLS.append(",('page_reportbytag','report_list','','fr','Liste','Une liste de toute les executions de cas de test par environnement, pays et navigateur')");
        SQLS.append(",('page_reportbytag','report_status','','en','Report by Status','A report containing the number of execution for each status with their percentage and a chart associated')");
        SQLS.append(",('page_reportbytag','report_status','','fr','Rapport par Status','Rapport contenant le nombre d\\'execution pour chaque status avec leur pourcentage et un graphique associé')");
        SQLS.append(",('page_reportbytag','summary_table','','en','Summary Table','Summary of the total and percentages values for each result code (OK, KO, FA, NA, NE, PE, CA). Table is sorted by Application, Country and Environment.')");
        SQLS.append(",('page_reportbytag','title','','en','Execution reporting by tag','This page generate a report of all the execution for a choosen tag')");
        SQLS.append(",('page_reportbytag','title','','fr','Rapport d\\'execution par Tag','Cette page génère un rapport de toutes les executions pour un tag choisi')");
        SQLS.append(",('page_robot','button_create','','en','Create new Robot','')");
        SQLS.append(",('page_robot','button_create','','fr','Créer un nouveau Robot','')");
        SQLS.append(",('page_robot','button_delete','','en','Delete Robot','')");
        SQLS.append(",('page_robot','button_delete','','fr','Supprimer le Robot','')");
        SQLS.append(",('page_robot','button_edit','','en','Edit Robot','')");
        SQLS.append(",('page_robot','button_edit','','fr','Modifier le Robot','')");
        SQLS.append(",('page_robot','title','','en','ROBOT','This page can be used in order to manage the robots.')");
        SQLS.append(",('page_robot','title','','fr','ROBOT','Cette page permet de gérer et créer des Robots.')");
        SQLS.append(",('page_runtests','Browser','','en','Browser','This is the browser on which the <code class=\\'doc-crbvvoca\\'>test case</code> will be executed. <br><br>Firefox is set as the default browser as it is automatically embed in the selenium Server.<br><br>You can use other browsers IE9, IE10, IE11 and chrome using the drivers associated.<br>Please, read the <i>Example scripts to start your local selenium server</i> for more information')");
        SQLS.append(",('page_runtests','manualExecution','','en','ManualExecution','Manual Execution is the way to execute the <code class=\\'doc-crbvvoca\\'>test case</code>. It could be YES to manually execute all kind of <code class=\\'doc-crbvvoca\\'>test case</code>, or NO to execute if automatically.')");
        SQLS.append(",('page_runtests','outputformat','','en','Output Format','This is the format of the output.<br><br><b>gui</b> : output is a web page. If test can be executed, the output will redirect to the test execution detail page.<br><b>compact</b> : output is plain text in a single line. This is more convenient when the test case is executed in batch mode.<br><b>verbose-txt</b> : output is a plain text with key=value format. This is also for batch mode but when the output needs to be parsed to get detailed information.')");
        SQLS.append(",('page_runtests','PageSource','','en','PageSource','This define whether Page Source will be recorded during the execution of the test.<br><br><b>0</b> : No Page Source are recorded. This is to be used when a massive amout of tests are performed.<br><b>1</b> : Page Source are taken only when action or control provide unexpected result.<br><b>2</b> : Page Source are always taken on every selenium action. This is to be used only on very specific cases where all actions needs to take page source (For debug mode for example.')");
        SQLS.append(",('page_runtests','Retries','','en','Retries','Define the number of Retry of the Execution in case of Not OK status.<br>\nAs soon a OK status if reached, the engine stop to retry to execute the testcase.')");
        SQLS.append(",('page_runtests','screenshot','','en','Screenshot','This define whether screenshots will be taken during the execution of the test.<br><br><b>0</b> : No screenshots are taken. This is to be used when a massive amout of tests are performed.<br><b>1</b> : Screenshots are taken only when action or control provide unexpected result.<br><b>2</b> : Screenshots are always taken on every selenium action. This is to be used only on very specific cases where all actions needs a screenshot.')");
        SQLS.append(",('page_runtests','screensize','','en','Screen Size','This is the size of the browser screen that will be set for the execution.<br><br>Default Values are set inside the invariant SCREENSIZE that can be configured on Edit Public invariant screen..<br>Value must be two Integer splitted by a <b>*</b> mark.<br><i>For Exemple : 1024*768</i><br><br>If you need to add other Values, please contact your Cerberus Administrator.')");
        SQLS.append(",('page_runtests','SeleniumLog','','en','SeleniumLog','This define whether Selenium Log will be recorded during the execution of the test.<br><br><b>0</b> : No  Selenium Log are recorded. This is to be used when a massive amout of tests are performed.<br><b>1</b> : Selenium Log are taken only when action or control provide unexpected result.<br><b>2</b> : Selenium Log are always taken on execution. This is to be used only on very specific cases where all actions needs to take Selenium Log (For debug mode for example.')");
        SQLS.append(",('page_runtests','SeleniumServerIP','','en','Selenium Server IP','Selenium Server IP is the IP of the computer where the selenium server is running.<br>This also correspond to the IP where the brower will execute the <code class=\\'doc-crbvvoca\\'>test case</code>.')");
        SQLS.append(",('page_runtests','SeleniumServerPort','','en','Selenium Server Port','Selenium Server Port is the port which will be used to run the <code class=\\'doc-crbvvoca\\'>test case</code>.')");
        SQLS.append(",('page_runtests','Synchroneous','','en','Synchroneous','This is parameter to define if user mut be redirected to the reporting during the execution.<br><br>By default, synchroneous will be set to Y, meaning the redirection will be at the end of the execution.')");
        SQLS.append(",('page_runtests','Timeout','','en','Timeout','This is the timeout used for the execution.<br><br>If empty, the default value will be the one set in the parameter table.')");
        SQLS.append(",('page_soapLibrary','addSoapLibrary_field','','en','Add Library','')");
        SQLS.append(",('page_soapLibrary','addSoapLibrary_field','','fr','Ajouter une librairie','')");
        SQLS.append(",('page_soapLibrary','allSoapLibrarys','','en','SOAP Libraries','')");
        SQLS.append(",('page_soapLibrary','allSoapLibrarys','','fr','Librairies SOAP','')");
        SQLS.append(",('page_soapLibrary','button_col','','en','Actions','')");
        SQLS.append(",('page_soapLibrary','button_col','','fr','Actions','')");
        SQLS.append(",('page_soapLibrary','button_create','','en','Add Library','')");
        SQLS.append(",('page_soapLibrary','button_create','','fr','Ajouter une librairie','')");
        SQLS.append(",('page_soapLibrary','button_edit','','en','Edit Library','')");
        SQLS.append(",('page_soapLibrary','button_edit','','fr','Editer la librairie','')");
        SQLS.append(",('page_soapLibrary','button_remove','','en','Remove Library','')");
        SQLS.append(",('page_soapLibrary','button_remove','','fr','Supprimer la librairie','')");
        SQLS.append(",('page_soapLibrary','close_btn','','en','Close','')");
        SQLS.append(",('page_soapLibrary','close_btn','','fr','Fermer','')");
        SQLS.append(",('page_soapLibrary','description_col','','en','Description','')");
        SQLS.append(",('page_soapLibrary','description_col','','fr','Description','')");
        SQLS.append(",('page_soapLibrary','description_field','','en','Description','')");
        SQLS.append(",('page_soapLibrary','description_field','','fr','Description','')");
        SQLS.append(",('page_soapLibrary','editSoapLibrary_field','','en','Edit Library','')");
        SQLS.append(",('page_soapLibrary','editSoapLibrary_field','','fr','Editer la librairie','')");
        SQLS.append(",('page_soapLibrary','envelope_col','','en','Envelope','')");
        SQLS.append(",('page_soapLibrary','envelope_col','','fr','Enveloppe','')");
        SQLS.append(",('page_soapLibrary','envelope_field','','en','Envelope','')");
        SQLS.append(",('page_soapLibrary','envelope_field','','fr','Enveloppe','')");
        SQLS.append(",('page_soapLibrary','idname_field','','en','Name','')");
        SQLS.append(",('page_soapLibrary','idname_field','','fr','Nom','')");
        SQLS.append(",('page_soapLibrary','message_remove','','en','Are you sure?','')");
        SQLS.append(",('page_soapLibrary','message_remove','','fr','Êtes-vous sûrs?','')");
        SQLS.append(",('page_soapLibrary','method_col','','en','Method','')");
        SQLS.append(",('page_soapLibrary','method_col','','fr','Méthode','')");
        SQLS.append(",('page_soapLibrary','method_field','','en','Method','')");
        SQLS.append(",('page_soapLibrary','method_field','','fr','Méthode','')");
        SQLS.append(",('page_soapLibrary','parsinganswer_col','','en','Parsing Answer','')");
        SQLS.append(",('page_soapLibrary','parsinganswer_col','','fr','Réponse Analysée','')");
        SQLS.append(",('page_soapLibrary','parsingAnswer_field','','en','Parsing Answer','')");
        SQLS.append(",('page_soapLibrary','parsingAnswer_field','','fr','Réponse analysée','')");
        SQLS.append(",('page_soapLibrary','save_btn','','en','Save Library','')");
        SQLS.append(",('page_soapLibrary','save_btn','','fr','Sauvegarder la librairie','')");
        SQLS.append(",('page_soapLibrary','servicepath_col','','en','Service Path','')");
        SQLS.append(",('page_soapLibrary','servicepath_col','','fr','Chemin du service','')");
        SQLS.append(",('page_soapLibrary','servicePath_field','','en','Service Path','')");
        SQLS.append(",('page_soapLibrary','servicePath_field','','fr','Chemin du service','')");
        SQLS.append(",('page_soapLibrary','soapLibrary','','en','SOAP Library','')");
        SQLS.append(",('page_soapLibrary','soapLibrary','','fr','Librairie SOAP','')");
        SQLS.append(",('page_soapLibrary','soapLibrary_col','','en','Name','')");
        SQLS.append(",('page_soapLibrary','soapLibrary_col','','fr','Nom','')");
        SQLS.append(",('page_soapLibrary','soapLibrary_field','','en','SOAP Librairy','')");
        SQLS.append(",('page_soapLibrary','soapLibrary_field','','fr','Librairie SOAP','')");
        SQLS.append(",('page_soapLibrary','title_remove','','en','Remove Library','')");
        SQLS.append(",('page_soapLibrary','title_remove','','fr','Supprimer la librairie','')");
        SQLS.append(",('page_soapLibrary','type_col','','en','Type','')");
        SQLS.append(",('page_soapLibrary','type_col','','fr','Type','')");
        SQLS.append(",('page_soapLibrary','type_field','','en','Type','')");
        SQLS.append(",('page_soapLibrary','type_field','','fr','Type','')");
        SQLS.append(",('page_sqlLibrary','addSqlLibrary_field','','en','Add Library','')");
        SQLS.append(",('page_sqlLibrary','addSqlLibrary_field','','fr','Ajouter une librairie','')");
        SQLS.append(",('page_sqlLibrary','allSqlLibrarys','','en','SQL Libraries','')");
        SQLS.append(",('page_sqlLibrary','allSqlLibrarys','','fr','Librairies SQL','')");
        SQLS.append(",('page_sqlLibrary','button_col','','en','Actions','')");
        SQLS.append(",('page_sqlLibrary','button_col','','fr','Actions','')");
        SQLS.append(",('page_sqlLibrary','button_create','','en','Add Library','')");
        SQLS.append(",('page_sqlLibrary','button_create','','fr','Ajouter une librairie','')");
        SQLS.append(",('page_sqlLibrary','button_edit','','en','Edit Library','')");
        SQLS.append(",('page_sqlLibrary','button_edit','','fr','Editer la librairie','')");
        SQLS.append(",('page_sqlLibrary','button_remove','','en','Remove Library','')");
        SQLS.append(",('page_sqlLibrary','button_remove','','fr','Supprimer la librairie','')");
        SQLS.append(",('page_sqlLibrary','close_btn','','en','Close','')");
        SQLS.append(",('page_sqlLibrary','close_btn','','fr','Fermer','')");
        SQLS.append(",('page_sqlLibrary','database_col','','en','Database','')");
        SQLS.append(",('page_sqlLibrary','database_col','','fr','Base de donnée','')");
        SQLS.append(",('page_sqlLibrary','database_field','','en','Database','')");
        SQLS.append(",('page_sqlLibrary','database_field','','fr','Base de donnée','')");
        SQLS.append(",('page_sqlLibrary','description_col','','en','Description','')");
        SQLS.append(",('page_sqlLibrary','description_col','','fr','Description','')");
        SQLS.append(",('page_sqlLibrary','description_field','','en','Description','')");
        SQLS.append(",('page_sqlLibrary','description_field','','fr','Description','')");
        SQLS.append(",('page_sqlLibrary','editSqlLibrary_field','','en','Edit Library','')");
        SQLS.append(",('page_sqlLibrary','editSqlLibrary_field','','fr','Editer la librairie','')");
        SQLS.append(",('page_sqlLibrary','idname_field','','en','Name','')");
        SQLS.append(",('page_sqlLibrary','idname_field','','fr','Nom','')");
        SQLS.append(",('page_sqlLibrary','message_remove','','en','Are you sure?','')");
        SQLS.append(",('page_sqlLibrary','message_remove','','fr','Êtes-vous sûrs?','')");
        SQLS.append(",('page_sqlLibrary','save_btn','','en','Save Library','')");
        SQLS.append(",('page_sqlLibrary','save_btn','','fr','Sauvegarder la librairie','')");
        SQLS.append(",('page_sqlLibrary','script_col','','en','Script','')");
        SQLS.append(",('page_sqlLibrary','script_col','','fr','Script','')");
        SQLS.append(",('page_sqlLibrary','script_field','','en','Script','')");
        SQLS.append(",('page_sqlLibrary','script_field','','fr','Script','')");
        SQLS.append(",('page_sqlLibrary','sqlLibrary','','en','SQL Library','')");
        SQLS.append(",('page_sqlLibrary','sqlLibrary','','fr','Librairie SQL','')");
        SQLS.append(",('page_sqlLibrary','sqlLibrary_col','','en','Name','')");
        SQLS.append(",('page_sqlLibrary','sqlLibrary_col','','fr','Nom','')");
        SQLS.append(",('page_sqlLibrary','sqlLibrary_field','','en','SQL Librairy','')");
        SQLS.append(",('page_sqlLibrary','sqlLibrary_field','','fr','Librairie SQL','')");
        SQLS.append(",('page_sqlLibrary','title_remove','','en','Remove Library','')");
        SQLS.append(",('page_sqlLibrary','title_remove','','fr','Supprimer la librairie','')");
        SQLS.append(",('page_sqlLibrary','type_col','','en','Type','')");
        SQLS.append(",('page_sqlLibrary','type_col','','fr','Type','')");
        SQLS.append(",('page_sqlLibrary','type_field','','en','Type','')");
        SQLS.append(",('page_sqlLibrary','type_field','','fr','Type','')");
        SQLS.append(",('page_test','btn_create','','en','Create Test','')");
        SQLS.append(",('page_test','btn_create','','fr','Créer un Test','')");
        SQLS.append(",('page_test','btn_edit','','en','Edit Test','')");
        SQLS.append(",('page_test','btn_edit','','fr','Modifier le Test','')");
        SQLS.append(",('page_test','button_delete','','en','Delete Test','')");
        SQLS.append(",('page_test','button_delete','','fr','Supprimer le Test','')");
        SQLS.append(",('page_test','delete','','en','Dlt','Select this checkbox and then save changes in order to delete the row.')");
        SQLS.append(",('page_test','message_delete','','en','Do you want to delete Test <b>\\'%ENTRY%\\'</b> ?<br>WARNING1 : All corresponding Test Cases will be removed as well !!!<br>WARNING2 : All associated Test Cases executions will also be removed !!!','')");
        SQLS.append(",('page_test','message_delete','','fr','Confirmez vous la suppression du Test <b>\\'%ENTRY%\\'</b> ?<br> ATTENTION1 : Tous les Cas de Test associés seront également supprimés !!!<br>ATTENTION2 : Toutes les Executions associées seront également supprimées !!!','')");
        SQLS.append(",('page_testbattery','addtestbattery_field','','en','Add Battery','')");
        SQLS.append(",('page_testbattery','addtestbattery_field','','fr','Ajouter la Batterie','')");
        SQLS.append(",('page_testbattery','addtestcase_tab','','en','Add Test Case','')");
        SQLS.append(",('page_testbattery','addtestcase_tab','','fr','Ajouter un cas de test','')");
        SQLS.append(",('page_testbattery','add_btn','','en','Add','')");
        SQLS.append(",('page_testbattery','add_btn','','fr','Ajouter','')");
        SQLS.append(",('page_testbattery','allTestbatterys','','en','Test Battery','')");
        SQLS.append(",('page_testbattery','allTestbatterys','','fr','Batterie de Test','')");
        SQLS.append(",('page_testbattery','back_btn','','en','Back','')");
        SQLS.append(",('page_testbattery','back_btn','','fr','Précédent','')");
        SQLS.append(",('page_testbattery','battery_field','','en','Battery','')");
        SQLS.append(",('page_testbattery','battery_field','','fr','Batterie','')");
        SQLS.append(",('page_testbattery','button_add','','en','Add Test Case','')");
        SQLS.append(",('page_testbattery','button_add','','fr','Ajouter un Cas de Test','')");
        SQLS.append(",('page_testbattery','button_col','','en','Actions','')");
        SQLS.append(",('page_testbattery','button_col','','fr','Actions','')");
        SQLS.append(",('page_testbattery','button_create','','en','Create Battery','')");
        SQLS.append(",('page_testbattery','button_create','','fr','Créer une Batterie','')");
        SQLS.append(",('page_testbattery','button_edit','','en','Edit Battery','')");
        SQLS.append(",('page_testbattery','button_edit','','fr','Editer la Batterie','')");
        SQLS.append(",('page_testbattery','button_remove','','en','Remove Battery','')");
        SQLS.append(",('page_testbattery','button_remove','','fr','Supprimer la Batterie','')");
        SQLS.append(",('page_testbattery','close_btn','','en','Close','')");
        SQLS.append(",('page_testbattery','close_btn','','fr','Fermer','')");
        SQLS.append(",('page_testbattery','description_col','','en','Description','')");
        SQLS.append(",('page_testbattery','description_col','','fr','Description','')");
        SQLS.append(",('page_testbattery','description_field','','en','Description','')");
        SQLS.append(",('page_testbattery','description_field','','fr','Description','')");
        SQLS.append(",('page_testbattery','description_tab','','en','Description','')");
        SQLS.append(",('page_testbattery','description_tab','','fr','Description','')");
        SQLS.append(",('page_testbattery','edittestbattery_field','','en','Edit Battery','')");
        SQLS.append(",('page_testbattery','edittestbattery_field','','fr','Modifier la Batterie','')");
        SQLS.append(",('page_testbattery','message_remove','','en','Are you sure?','')");
        SQLS.append(",('page_testbattery','message_remove','','fr','Êtes-vous sûrs?','')");
        SQLS.append(",('page_testbattery','save_btn','','en','Save','')");
        SQLS.append(",('page_testbattery','save_btn','','fr','Sauvegarder','')");
        SQLS.append(",('page_testbattery','testbattery_col','','en','Test Battery','')");
        SQLS.append(",('page_testbattery','testbattery_col','','fr','Batterie de test','')");
        SQLS.append(",('page_testbattery','testcampaign_col','','en','Test Battery','')");
        SQLS.append(",('page_testbattery','testcampaign_col','','fr','Batterie de test','')");
        SQLS.append(",('page_testbattery','testcase_col','','en','Test Case','')");
        SQLS.append(",('page_testbattery','testcase_col','','fr','Cas de Test','')");
        SQLS.append(",('page_testbattery','testcase_tab','','en','Test Case','')");
        SQLS.append(",('page_testbattery','testcase_tab','','fr','Cas de test','')");
        SQLS.append(",('page_testbattery','test_col','','en','Test','')");
        SQLS.append(",('page_testbattery','test_col','','fr','Test','')");
        SQLS.append(",('page_testbattery','title_remove','','en','Remove Battery','')");
        SQLS.append(",('page_testbattery','title_remove','','fr','Supprimer la Batterie','')");
        SQLS.append(",('page_testcampaign','addtestcampaign_field','','en','Add Campaign','')");
        SQLS.append(",('page_testcampaign','addtestcampaign_field','','fr','Ajouter la Campagne','')");
        SQLS.append(",('page_testcampaign','add_btn','','en','Add','')");
        SQLS.append(",('page_testcampaign','add_btn','','fr','Ajouter','')");
        SQLS.append(",('page_testcampaign','allTestcampaigns','','en','Test Campaigns','')");
        SQLS.append(",('page_testcampaign','allTestcampaigns','','fr','Campagnes de Test','')");
        SQLS.append(",('page_testcampaign','battery_tab','','en','Battery','')");
        SQLS.append(",('page_testcampaign','battery_tab','','fr','Batterie','')");
        SQLS.append(",('page_testcampaign','button_col','','en','Actions','')");
        SQLS.append(",('page_testcampaign','button_col','','fr','Actions','')");
        SQLS.append(",('page_testcampaign','button_create','','en','Create Campaign','')");
        SQLS.append(",('page_testcampaign','button_create','','fr','Créer une Campagne','')");
        SQLS.append(",('page_testcampaign','button_edit','','en','Edit Campaign','')");
        SQLS.append(",('page_testcampaign','button_edit','','fr','Editer la Campagne','')");
        SQLS.append(",('page_testcampaign','button_remove','','en','Remove Campaign','')");
        SQLS.append(",('page_testcampaign','button_remove','','fr','Supprimer la Campagne','')");
        SQLS.append(",('page_testcampaign','button_view','','en','View Campaign','')");
        SQLS.append(",('page_testcampaign','button_view','','fr','Voir la Campagne','')");
        SQLS.append(",('page_testcampaign','campaign_field','','en','Campaign','')");
        SQLS.append(",('page_testcampaign','campaign_field','','fr','Campagne','')");
        SQLS.append(",('page_testcampaign','close_btn','','en','Close','')");
        SQLS.append(",('page_testcampaign','close_btn','','fr','Fermer','')");
        SQLS.append(",('page_testcampaign','description_col','','en','Description','')");
        SQLS.append(",('page_testcampaign','description_col','','fr','Description','')");
        SQLS.append(",('page_testcampaign','description_field','','en','Description','')");
        SQLS.append(",('page_testcampaign','description_field','','fr','Description','')");
        SQLS.append(",('page_testcampaign','description_tab','','en','Description','')");
        SQLS.append(",('page_testcampaign','description_tab','','fr','Description','')");
        SQLS.append(",('page_testcampaign','edittestcampaign_field','','en','Edit Campaign','')");
        SQLS.append(",('page_testcampaign','edittestcampaign_field','','fr','Modifier la Campagne','')");
        SQLS.append(",('page_testcampaign','message_remove','','en','Are you sure?','')");
        SQLS.append(",('page_testcampaign','message_remove','','fr','Êtes-vous sûrs?','')");
        SQLS.append(",('page_testcampaign','parameter_col','','en','Parameter','')");
        SQLS.append(",('page_testcampaign','parameter_col','','fr','Paramètre','')");
        SQLS.append(",('page_testcampaign','parameter_tab','','en','Parameter','')");
        SQLS.append(",('page_testcampaign','parameter_tab','','fr','Paramètre','')");
        SQLS.append(",('page_testcampaign','save_btn','','en','Save','')");
        SQLS.append(",('page_testcampaign','save_btn','','fr','Sauvegarder','')");
        SQLS.append(",('page_testcampaign','testbattery_col','','en','Test Battery','')");
        SQLS.append(",('page_testcampaign','testbattery_col','','fr','Batterie de test','')");
        SQLS.append(",('page_testcampaign','testcampaign_col','','en','Test Campaign','')");
        SQLS.append(",('page_testcampaign','testcampaign_col','','fr','Campagne de test','')");
        SQLS.append(",('page_testcampaign','testcase_col','','en','Test Case','')");
        SQLS.append(",('page_testcampaign','testcase_col','','fr','Cas de Test','')");
        SQLS.append(",('page_testcampaign','test_col','','en','Test','')");
        SQLS.append(",('page_testcampaign','test_col','','fr','Test','')");
        SQLS.append(",('page_testcampaign','title_remove','','en','Remove Campaign','')");
        SQLS.append(",('page_testcampaign','title_remove','','fr','Supprimer la Campagne','')");
        SQLS.append(",('page_testcampaign','value_col','','en','Value','')");
        SQLS.append(",('page_testcampaign','value_col','','fr','Valeur','')");
        SQLS.append(",('page_testcampaign','viewtestcampaign_field','','en','Campaign','')");
        SQLS.append(",('page_testcampaign','viewtestcampaign_field','','fr','Campagne','')");
        SQLS.append(",('page_testcase','BugIDLink','','en','Link','')");
        SQLS.append(",('page_testcase','delete','','en','Dlt','Select this checkbox and then save changes in order to delete the row.')");
        SQLS.append(",('page_testcase','delete','','fr','Sup','')");
        SQLS.append(",('page_testcase','dpd_choose_step','','en','-- Choose Step  --','')");
        SQLS.append(",('page_testcase','dpd_choose_test','','en','-- Choose Test --','')");
        SQLS.append(",('page_testcase','dpd_choose_testcase','','en','-- Choose Test Case --','')");
        SQLS.append(",('page_testcase','laststatus','','en','Last Execution Status','')");
        SQLS.append(",('page_testcase','lbl_copied_from','','en','Copied from:','')");
        SQLS.append(",('page_testcase','link_edit_step','','en','Edit Used Step','')");
        SQLS.append(",('page_testcase','message_delete','','en','Do you want to delete Test Case <b>\\'%ENTRY%\\'</b> ?<br>WARNING : All associated Executions will also be removed !!!','')");
        SQLS.append(",('page_testcase','message_delete','','fr','Confirmez vous la suppression du Cas de Test <b>\\'%ENTRY%\\'</b> ?<br>ATTENTION : Toutes les Executions associées seront également supprimées !!!','')");
        SQLS.append(",('page_testcase','tooltip_addAction','','en','Add Action','')");
        SQLS.append(",('page_testcase','tooltip_addControl','','en','Add Control','')");
        SQLS.append(",('page_testcase','tooltip_clicktocreate','','en','Property %P% is missing! Click to create a property!','')");
        SQLS.append(",('page_testcase','tooltip_clicktooverride','','en','Property %P% is defined in the test: %T% - %TC% [step: %S%]. Click to override property!','')");
        SQLS.append(",('page_testcase','tooltip_infooverriden','','en','Property %P% was overridden in the current Test Case. Original test case: %T% - %TC% [step: %S%]','')");
        SQLS.append(",('page_testcase','tooltip_is_useStep','','en','This step can not be used as library, because it uses another step!','')");
        SQLS.append(",('page_testcase','tooltip_select_entry','','en','Select an entry from library','')");
        SQLS.append(",('page_testcase','tooltip_step_used','','en','This step is being used by another step(s)!','')");
        SQLS.append(",('page_testcase','txt_property_not_defined','','en','** Property not defined **','')");
        SQLS.append(",('page_testcase','undefined_error_message','','en','There are undefined properties! Please check them before proceed.','')");
        SQLS.append(",('page_testcaseexecutionqueue','allExecution','','en','Execution Queue','')");
        SQLS.append(",('page_testcaseexecutionqueue','allExecution','','fr','File d exécution','')");
        SQLS.append(",('page_testcaseexecutionqueue','browser_col','','en','Browser','')");
        SQLS.append(",('page_testcaseexecutionqueue','browser_col','','fr','Navigateur','')");
        SQLS.append(",('page_testcaseexecutionqueue','country_col','','en','Country','')");
        SQLS.append(",('page_testcaseexecutionqueue','country_col','','fr','Pays','')");
        SQLS.append(",('page_testcaseexecutionqueue','environment_col','','en','Environment','')");
        SQLS.append(",('page_testcaseexecutionqueue','environment_col','','fr','Environement','')");
        SQLS.append(",('page_testcaseexecutionqueue','id_col','','en','ID','')");
        SQLS.append(",('page_testcaseexecutionqueue','id_col','','fr','ID','')");
        SQLS.append(",('page_testcaseexecutionqueue','processed_col','','en','Proceeded','')");
        SQLS.append(",('page_testcaseexecutionqueue','processed_col','','fr','Traité','')");
        SQLS.append(",('page_testcaseexecutionqueue','tag_col','','en','Tag','')");
        SQLS.append(",('page_testcaseexecutionqueue','tag_col','','fr','Tag','')");
        SQLS.append(",('page_testcaseexecutionqueue','testcase_col','','en','Test Case','')");
        SQLS.append(",('page_testcaseexecutionqueue','testcase_col','','fr','Cas de Test','')");
        SQLS.append(",('page_testcaseexecutionqueue','test_col','','en','Test','')");
        SQLS.append(",('page_testcaseexecutionqueue','test_col','','fr','Test','')");
        SQLS.append(",('page_testcaselist','activationCriteria','','en','Activation Criteria','')");
        SQLS.append(",('page_testcaselist','activationCriteria','','fr','Critères d\\'activation','')");
        SQLS.append(",('page_testcaselist','btn_create','','en','Create Test Case',NULL)");
        SQLS.append(",('page_testcaselist','btn_create','','fr','Créer un Cas de Test',NULL)");
        SQLS.append(",('page_testcaselist','btn_delete','','en','Delete Test Case','')");
        SQLS.append(",('page_testcaselist','btn_delete','','fr','Supprimer Cas de Test','')");
        SQLS.append(",('page_testcaselist','btn_duplicate','','en','Duplicate Test Case','')");
        SQLS.append(",('page_testcaselist','btn_duplicate','','fr','Dupliquer le Cas de Test','')");
        SQLS.append(",('page_testcaselist','btn_edit','','en','Edit Test Case','')");
        SQLS.append(",('page_testcaselist','btn_edit','','fr','Modifer le Cas de Test','')");
        SQLS.append(",('page_testcaselist','btn_editLabel','','en','Edit Test Case labels','')");
        SQLS.append(",('page_testcaselist','btn_editLabel','','fr','Modifier les étiquettes du Cas de Test','')");
        SQLS.append(",('page_testcaselist','btn_editScript','','en','Edit Test Case Script','')");
        SQLS.append(",('page_testcaselist','btn_editScript','','fr','Editer le script du Cas de Test','')");
        SQLS.append(",('page_testcaselist','btn_runTest','','en','Run Test Case','')");
        SQLS.append(",('page_testcaselist','btn_runTest','','fr','Exécuter le Cas de Test','')");
        SQLS.append(",('page_testcaselist','btn_view','','en','View Test Case','')");
        SQLS.append(",('page_testcaselist','btn_view','','fr','Voir Cas de Test','')");
        SQLS.append(",('page_testcaselist','filters','','en','Filters','Test filter')");
        SQLS.append(",('page_testcaselist','filters','','fr','Filtres','Filtre des tests')");
        SQLS.append(",('page_testcaselist','link','','en','Bug Link','')");
        SQLS.append(",('page_testcaselist','link','','fr','Lien vers le Bug','')");
        SQLS.append(",('page_testcaselist','testCaseInfo','','en','Test Case Info','')");
        SQLS.append(",('page_testcaselist','testCaseInfo','','fr','Test Case Info','')");
        SQLS.append(",('page_testcaselist','testcaselist','','en','Test Case List','List of all the test case of the selected test')");
        SQLS.append(",('page_testcaselist','testcaselist','','fr','Liste des cas de test','Liste de tout les cas de test du test selectionné')");
        SQLS.append(",('page_testcaselist','testCaseParameter','','en','Test Case Parameter','')");
        SQLS.append(",('page_testcaselist','testCaseParameter','','fr','Parametres du Cas de Test','')");
        SQLS.append(",('page_testcaselist','testInfo','','en','Test Info','')");
        SQLS.append(",('page_testcaselist','testInfo','','fr','Test Info','')");
        SQLS.append(",('page_testcasesearch','text','','en','Text','Insert here the text that will search against the following Fields of every <code class=\\'doc-crbvvoca\\'>test case</code> :<br>- Short Description,<br>- Detailed description / Value Expected,<br>- HowTo<br>- Comment<br><br>NB : Search is case insensitive.')");
        SQLS.append(",('page_testcase_m_addPicture','error_message_empty','','en','The URL value is empty!','')");
        SQLS.append(",('page_testcase_m_addPicture','lbl_feedurl','','en','Feed URL','')");
        SQLS.append(",('page_testcase_m_addPicture','title','','en','Add URL for picture','')");
        SQLS.append(",('page_testcase_m_showPicture','btn_remove','','en','Remove','')");
        SQLS.append(",('page_testcase_m_showPicture','title','','en','Selected Picture','')");
        SQLS.append(",('page_testdatalib','btn_create','','en','Create New Entry','')");
        SQLS.append(",('page_testdatalib','btn_import','','en','Import from XML file','')");
        SQLS.append(",('page_testdatalib','duplicated_message','','en','Please check the subdata entries. There are entries with duplicated names, a total of: ','')");
        SQLS.append(",('page_testdatalib','empty_name_message','','en','Please specify the name of the entry!','')");
        SQLS.append(",('page_testdatalib','empty_subdata_message','','en','Please check the subdata entries. There are entries without sub-data name, a total of: ','')");
        SQLS.append(",('page_testdatalib','message_delete','','en','Do you want to delete Test Data Library <b>\\'%ENTRY%\\'</b> <br>of system <b>\\'%SYSTEM%\\'</b>, country <b>\\'%COUNTRY%\\'</b> and environment <b>\\'%ENVIRONMENT%\\'</b> (ID : %ID%) ?','')");
        SQLS.append(",('page_testdatalib','message_delete','','fr','Confirmez-vous la suppression de la donnée de test <b>\\'%ENTRY%\\'</b> <br>du système <b>\\'%SYSTEM%\\'</b>, pays <b>\\'%COUNTRY%\\'</b> et environnement <b>\\'%ENVIRONMENT%\\'</b> (ID : %ID%) ?','')");
        SQLS.append(",('page_testdatalib','m_tab1_text','','en','Definition','')");
        SQLS.append(",('page_testdatalib','m_tab1_text','','fr','Defintion','')");
        SQLS.append(",('page_testdatalib','m_tab2_text','','en','SubData','')");
        SQLS.append(",('page_testdatalib','m_tab2_text','','fr','SubData','')");
        SQLS.append(",('page_testdatalib','m_tab3_text','','en','Tracability','')");
        SQLS.append(",('page_testdatalib','m_tab3_text','','fr','Traçabité','')");
        SQLS.append(",('page_testdatalib','page_title','','en','Test Data Library','')");
        SQLS.append(",('page_testdatalib','title','','en','Test Data Library','<p>The <u>Test Data Library</u> is a repository of test data that centralises and eases the Test Data Management process. Additionally, it eases the creation of test cases because it allows the reuse of data that is recurrently defined. </p> <p>Cerberus allows the definition of three types of entries: <b>STATIC</b>, <b>SQL</b> and <b>SOAP</b>.</p><p>The definition of each library entry comprises two steps: <ul><li>The definition of the library entry. </li><li>The definition of its sub-data entries.</li></ul></p>')");
        SQLS.append(",('page_testdatalib','title_csv_configurations','','en','CSV configurations','')");
        SQLS.append(",('page_testdatalib','title_csv_configurations','','fr','Configuration CSV','')");
        SQLS.append(",('page_testdatalib','title_soap_configurations','','en','SOAP configurations','')");
        SQLS.append(",('page_testdatalib','title_sql_configurations','','en','SQL configurations','')");
        SQLS.append(",('page_testdatalib','tooltip_delete','','en','Delete entry.','')");
        SQLS.append(",('page_testdatalib','tooltip_duplicateEntry','','en','Duplicate this entry.','')");
        SQLS.append(",('page_testdatalib','tooltip_editentry','','en','Edit entry.','')");
        SQLS.append(",('page_testdatalib','tooltip_editsubdata','','en','Edit sub-data entries.','')");
        SQLS.append(",('page_testdatalib','tooltip_gettestcases','','en','Get list of test cases that use this entry.','')");
        SQLS.append(",('page_testdatalib','tooltip_viewsubdata','','en','View sub-data entries.','')");
        SQLS.append(",('page_testdatalib_delete','title','','en','Delete Test Data Library Entry','')");
        SQLS.append(",('page_testdatalib_m_createlib','title','','en','Create New Test Data Library Entry','')");
        SQLS.append(",('page_testdatalib_m_duplicatelib','title','','en','Duplicate Test Data Library','')");
        SQLS.append(",('page_testdatalib_m_gettestcases','group_title','','en','Test cases affected by this entry','')");
        SQLS.append(",('page_testdatalib_m_gettestcases','nrProperties','','en','#properties:','')");
        SQLS.append(",('page_testdatalib_m_gettestcases','nrTestCases','','en','#test cases:','')");
        SQLS.append(",('page_testdatalib_m_gettestcases','nrTests','','en','#tests: ','')");
        SQLS.append(",('page_testdatalib_m_gettestcases','title','','en','List of test cases affected by this entry','')");
        SQLS.append(",('page_testdatalib_m_listtestdatalibdata','title','','en','List of sub-data entries','')");
        SQLS.append(",('page_testdatalib_m_managetestdatalibdata','actions','','en','Actions','<p> List of available actions  for the current user: </p><p><table border=\\'1\\'><tr><th class=\\'ex\\'>Button</th><th class=\\'ex\\'>Function</th><th class=\\'ex\\'>Description</th></tr><tr><td><span class=\\'glyphicon glyphicon-trash\\'></span></span></td><td>Delete</td><td>Allows the user to delete the sub-data entry. If the sub-data entry is a new, then it will be removed from the GUI. If the sub-data entry was loaded from the database, then it will be marked as to be deleted from the database.</td></tr><tr><td><span class=\\'glyphicon glyphicon-remove\\'></span></td><td>Mark to be deleted</td><td>Marks the sub-data entry to be deleted from the database.</td></tr></table></p>')");
        SQLS.append(",('page_testdatalib_m_managetestdatalibdata','link_add_new','','en','Add new sub-data','')");
        SQLS.append(",('page_testdatalib_m_managetestdatalibdata','link_add_new_title','','en','Add a new row in the list of sub-data entries.','')");
        SQLS.append(",('page_testdatalib_m_managetestdatalibdata','title','','en','Manage list of sub-data entries','')");
        SQLS.append(",('page_testdatalib_m_managetestdatalibdata','tooltip_defaultsubdata','','en','This is the default sub-data entry. It cannot be deleted nor its name can be modified.','')");
        SQLS.append(",('page_testdatalib_m_updatelib','title','','en','Edit Test Data Library Entry','')");
        SQLS.append(",('page_user','adduser_field','','en','Add User','')");
        SQLS.append(",('page_user','adduser_field','','fr','Ajouter un utilisateur','')");
        SQLS.append(",('page_user','add_btn','','en','Add','')");
        SQLS.append(",('page_user','add_btn','','fr','Ajouter','')");
        SQLS.append(",('page_user','allUsers','','en','Users','')");
        SQLS.append(",('page_user','allUsers','','fr','Utilisateurs','')");
        SQLS.append(",('page_user','button_col','','en','Actions','')");
        SQLS.append(",('page_user','button_col','','fr','Actions','')");
        SQLS.append(",('page_user','button_create','','en','Create User','')");
        SQLS.append(",('page_user','button_create','','fr','Créer un Utilisateur','')");
        SQLS.append(",('page_user','button_edit','','en','Edit User','')");
        SQLS.append(",('page_user','button_edit','','fr','Editer l Utilisateur','')");
        SQLS.append(",('page_user','button_remove','','en','Remove User','')");
        SQLS.append(",('page_user','button_remove','','fr','Supprimer l Utilisateur','')");
        SQLS.append(",('page_user','close_btn','','en','Close','')");
        SQLS.append(",('page_user','close_btn','','fr','Fermer','')");
        SQLS.append(",('page_user','defaultsystem_col','','en','Default System','')");
        SQLS.append(",('page_user','defaultsystem_col','','fr','Système par défaut','')");
        SQLS.append(",('page_user','defaultsystem_field','','en','Default System','')");
        SQLS.append(",('page_user','defaultsystem_field','','fr','Système par défaut','')");
        SQLS.append(",('page_user','edituser_field','','en','Edit User','')");
        SQLS.append(",('page_user','edituser_field','','fr','Modifier l utilisateur','')");
        SQLS.append(",('page_user','email_col','','en','Email','')");
        SQLS.append(",('page_user','email_col','','fr','Email','')");
        SQLS.append(",('page_user','email_field','','en','Email','')");
        SQLS.append(",('page_user','email_field','','fr','Email','')");
        SQLS.append(",('page_user','groups_col','','en','Groups','')");
        SQLS.append(",('page_user','groups_col','','fr','Groupes','')");
        SQLS.append(",('page_user','groups_field','','en','Groups','')");
        SQLS.append(",('page_user','groups_field','','fr','Groupes','')");
        SQLS.append(",('page_user','information_tab','','en','Information','')");
        SQLS.append(",('page_user','information_tab','','fr','Information','')");
        SQLS.append(",('page_user','login_col','','en','Login','')");
        SQLS.append(",('page_user','login_col','','fr','Identifiant','')");
        SQLS.append(",('page_user','login_field','','en','Login','')");
        SQLS.append(",('page_user','login_field','','fr','Identifiant','')");
        SQLS.append(",('page_user','message_remove','','en','Are you sure?','')");
        SQLS.append(",('page_user','message_remove','','fr','Êtes-vous sûrs?','')");
        SQLS.append(",('page_user','name_col','','en','Name','')");
        SQLS.append(",('page_user','name_col','','fr','Nom','')");
        SQLS.append(",('page_user','name_field','','en','Name','')");
        SQLS.append(",('page_user','name_field','','fr','Nom','')");
        SQLS.append(",('page_user','request_col','','en','Request','')");
        SQLS.append(",('page_user','request_col','','fr','Requête','')");
        SQLS.append(",('page_user','request_field','','en','Request','')");
        SQLS.append(",('page_user','request_field','','fr','Requête','')");
        SQLS.append(",('page_user','save_btn','','en','Save','')");
        SQLS.append(",('page_user','save_btn','','fr','Sauvegarder','')");
        SQLS.append(",('page_user','systems_col','','en','Systems','')");
        SQLS.append(",('page_user','systems_col','','fr','Systèmes','')");
        SQLS.append(",('page_user','systems_field','','en','System','')");
        SQLS.append(",('page_user','systems_field','','fr','Système','')");
        SQLS.append(",('page_user','systems_tab','','en','Systems','')");
        SQLS.append(",('page_user','systems_tab','','fr','Systèmes','')");
        SQLS.append(",('page_user','team_col','','en','Team','')");
        SQLS.append(",('page_user','team_col','','fr','Equipe','')");
        SQLS.append(",('page_user','team_field','','en','Team','')");
        SQLS.append(",('page_user','team_field','','fr','Equipe','')");
        SQLS.append(",('page_user','title_remove','','en','Remove User','')");
        SQLS.append(",('page_user','title_remove','','fr','Supprimer l Utilisateur','')");
        SQLS.append(",('project','active','','en','Active','This is a boolean that define if the project is active or not.')");
        SQLS.append(",('project','active','','fr','Actif','Booléen qui défini si un projet est actif.')");
        SQLS.append(",('project','code','','en','Code','This is the code of the project. ')");
        SQLS.append(",('project','code','','fr','Code','Code du projet.')");
        SQLS.append(",('project','dateCreation','','en','Created','This is the date when the project has been created.')");
        SQLS.append(",('project','dateCreation','','fr','Date de Création','Date de création du projet.')");
        SQLS.append(",('project','description','','en','Description','This is the description of the project')");
        SQLS.append(",('project','description','','fr','Description','Description du projet')");
        SQLS.append(",('project','idproject','','en','Project','This is the id of the project that provided the implementation of the <code class=\\'doc-crbvvoca\\'>test case</code>.')");
        SQLS.append(",('project','idproject','','fr','Projet','Identifiant du projet qui est à l\\'origine de la création d\\'un <code class=\\'doc-crbvvoca\\'>cas de test</code>.')");
        SQLS.append(",('robot','active','','en','Active','Define if the robot is active or not.')");
        SQLS.append(",('robot','active','','fr','Actif','Defini si le robot est actif ou non.')");
        SQLS.append(",('robot','browser','','en','Browser','Broswer of the robot.')");
        SQLS.append(",('robot','browser','','fr','Navigateur','Navitateur du robot.')");
        SQLS.append(",('robot','capabilityCapability','','en','Capability','Capability name.')");
        SQLS.append(",('robot','capabilityCapability','','fr','Capabilité','Nom de la capabilité.')");
        SQLS.append(",('robot','capabilityValue','','en','Value','Capability value.')");
        SQLS.append(",('robot','capabilityValue','','fr','Valeur','Valeur de la capabilité.')");
        SQLS.append(",('robot','description','','en','Description','Robot Description.')");
        SQLS.append(",('robot','description','','fr','Description','Description du robot.')");
        SQLS.append(",('robot','host','','en','Hostname','IP Adress or host that host the selenium server that will execute the test case.')");
        SQLS.append(",('robot','host','','fr','Hostname','Address IP ou nom de server qui heberge le robot et qui sera utilisé lors l\\'excution du cas de test.')");
        SQLS.append(",('robot','platform','','en','Platform','Operating system of the robot.')");
        SQLS.append(",('robot','platform','','fr','Platforme','System d\\'exploitation du robot.')");
        SQLS.append(",('robot','port','','en','Port number','Port number of the robot.')");
        SQLS.append(",('robot','port','','fr','Numero de port','Numero du port à utiliser pour acceder au Root.')");
        SQLS.append(",('robot','robot','','en','Robot','Name of the Robot. A robot define the server that will execute an automated test case. It can be used when starting an execution without having to feed information such as host, port platform or browser.')");
        SQLS.append(",('robot','robot','','fr','Robot','Nom du Robot. Le Robot est le serveur en charge de l\\'execution d\\'un test automatisé. Il permet de lancer une execution de test sans avoir à renseigner l\\'IP, le port, le navigateur ou OS à utiliser.')");
        SQLS.append(",('robot','robotID','','en','Robot ID','Technical identifier of the Robot.')");
        SQLS.append(",('robot','robotID','','fr','ID du Robot','Identifiant technique invariant du Robot.')");
        SQLS.append(",('robot','useragent','','en','User Agent','User Agent of the robot.')");
        SQLS.append(",('robot','useragent','','fr','User Agent','User Agent du Robot.')");
        SQLS.append(",('robot','version','','en','Version','Brower Version of the robot.')");
        SQLS.append(",('robot','version','','fr','Version','Version du navigateur du Robot.')");
        SQLS.append(",('test','Active','','en','Active','Define if the <code class=\\'doc-crbvvoca\\'>test</code> is active.<br>If <code class=\\'doc-crbvvoca\\'>test</code> is not active, no execution is possible on any of the associated <code class=\\'doc-crbvvoca\\'>test case</code>.')");
        SQLS.append(",('test','Active','','fr','Actif','Booléen qui définit si le <code class=\\'doc-crbvvoca\\'>test</code> est actif.<br>Si le <code class=\\'doc-crbvvoca\\'>test</code> n\\'est pas actif, aucune execution des <code class=\\'doc-crbvvoca\\'>Cas de tests</code> n\\'est possible.')");
        SQLS.append(",('test','Automated','','en','Automated','Define if the test is automated or not.')");
        SQLS.append(",('test','Automated','','fr','Automatisé','Boléen qui définit si le test est automatisé ou non')");
        SQLS.append(",('test','dateCreation','','en','Creation date','The date when the test have been created')");
        SQLS.append(",('test','dateCreation','','fr','Date de création','Date à laquelle le test a été créé')");
        SQLS.append(",('test','Description','','en','Test Description','This is the description of the <code class=\\'doc-crbvvoca\\'>test</code>.')");
        SQLS.append(",('test','Description','','fr','Description du test','Description du <code class=\\'doc-crbvvoca\\'>test</code>.')");
        SQLS.append(",('test','Test','','en','Test','A <code class=\\'doc-crbvvoca\\'>test</code> is grouping some <code class=\\'doc-crbvvoca\\'>test case</code> together. The criteria that groups the <code class=\\'doc-crbvvoca\\'>test cases</code> can be an application page or a feature.')");
        SQLS.append(",('test','Test','','fr','Test','Un <code class=\\'doc-crbvvoca\\'>test</code> regroupe plusieurs <code class=\\'doc-crbvvoca\\'>Cas de tests</code> ensemble.')");
        SQLS.append(",('testcase','activePROD','','en','Active PROD','Define whether the <code class=\\'doc-crbvvoca\\'>test case</code> can be executed in PROD environments.<br>If the environment gp1 (attached to the invariant) is PROD and Active PROD is No, the <code class=\\'doc-crbvvoca\\'>test case</code> will never be executed.')");
        SQLS.append(",('testcase','activeQA','','en','Active QA','Define whether the <code class=\\'doc-crbvvoca\\'>test case</code> can be executed in QA environments.<br>If the environment gp1 (attached to the invariant) is QA and Active QA is No, the <code class=\\'doc-crbvvoca\\'>test case</code> will never be executed.')");
        SQLS.append(",('testcase','activeUAT','','en','Active UAT','Define whether the <code class=\\'doc-crbvvoca\\'>test case</code> can be executed in UAT environments.<br>If the environment gp1 (attached to the invariant) is UAT and Active UAT is No, the <code class=\\'doc-crbvvoca\\'>test case</code> will never be executed.')");
        SQLS.append(",('testcase','BehaviorOrValueExpected','','en','Detailed Description / Value Expected','It is a full description of the <code class=\\'doc-crbvvoca\\'>application</code> feature that we expect to be tested with that <code class=\\'doc-crbvvoca\\'>test case</code>.')");
        SQLS.append(",('testcase','BugID','','en','Bug ID','This is the bug ID that will fix the pending KO.')");
        SQLS.append(",('testcase','BugID','','fr','BugID','')");
        SQLS.append(",('testcase','Comment','','en','Comment','This is where to add any interesting comment about the <code class=\\'doc-crbvvoca\\'>test case</code>.')");
        SQLS.append(",('testcase','countryList','','en','Country List','The list of countries where the test case is defined')");
        SQLS.append(",('testcase','countryList','','fr','Liste des pays','La liste des pays sur lesquels le cas de test est défini')");
        SQLS.append(",('testcase','Creator','','en','Creator','This is the name of the Cerberus user who created the <code class=\\'doc-crbvvoca\\'>test case</code>.')");
        SQLS.append(",('testcase','Description','','en','Test case short description','It is a synthetic description of what the <code class=\\'doc-crbvvoca\\'>test case</code> do.')");
        SQLS.append(",('testcase','Description','','fr','Description','')");
        SQLS.append(",('testcase','FromBuild','','en','From Sprint',' ')");
        SQLS.append(",('testcase','FromRev','','en','From Rev',' ')");
        SQLS.append(",('testcase','Function','','en','Function','The function is the functionnality that the <code class=\\'doc-crbvvoca\\'>test case</code> is testing.')");
        SQLS.append(",('testcase','Function','','fr','Fonction','')");
        SQLS.append(",('testcase','HowTo','','en','How To','<i>How to</i> field is used to define the step by step procedure used in order to execute the <code class=\\'doc-crbvvoca\\'>test case</code>. This is mainly used for MANUAL group <code class=\\'doc-crbvvoca\\'>test cases</code>.')");
        SQLS.append(",('testcase','Implementer','','en','Implementer','This is the name of the Cerberus user who implemented the <code class=\\'doc-crbvvoca\\'>test case</code>.')");
        SQLS.append(",('testcase','LastModifier','','en','LastModifier','This is the name of the Cerberus user who last modified the <code class=\\'doc-crbvvoca\\'>test case</code>.')");
        SQLS.append(",('testcase','Origine','','en','Origin','This is the country or the team that identified the scenario of the <code class=\\'doc-crbvvoca\\'>test case</code>.')");
        SQLS.append(",('testcase','RefOrigine','','en','RefOrigin','This is the external reference of the <code class=\\'doc-crbvvoca\\'>test case</code> when coming from outside Cerberus.')");
        SQLS.append(",('testcase','Status','','en','Status','It is the workflow status of the <code class=\\'doc-crbvvoca\\'>test case</code> used to follow-up the implementation of the tests.<br>It can take any values depending on the workflow that manage the <code class=\\'doc-crbvvoca\\'>test case</code> life cycle.<br><br>The first status defined on the invariant table (based on the sequence) will be the default value for any new <code class=\\'doc-crbvvoca\\'>test case</code>.<br>The only status that is mandatory to define and create is the WORKING status that correspond to fully working and stable <code class=\\'doc-crbvvoca\\'>test case</code>.')");
        SQLS.append(",('testcase','Status','','fr','Status','')");
        SQLS.append(",('testcase','TargetBuild','','en','Target Sprint','This is the Target Build that should fix the bug. Until we reach that Build, the <code class=\\'doc-crbvvoca\\'>test case</code> execution will be discarded.')");
        SQLS.append(",('testcase','TargetRev','','en','Target Rev','This is the Revision that should fix the bug. Until we reach that Revision, the <code class=\\'doc-crbvvoca\\'>test case</code> execution will be discarded.')");
        SQLS.append(",('testcase','TcActive','','en','Act','This field define if the test is active or not. A <code class=\\'doc-crbvvoca\\'>test case</code> that is not active cannot be executed.')");
        SQLS.append(",('testcase','TCDateCrea','','en','Creation Date','This is the <code class=\\'doc-crbvvoca\\'>test case</code> creation date.')");
        SQLS.append(",('testcase','TestCase','','en','Testcase','A <code class=\\'doc-crbvvoca\\'>test case</code> is a scenario that test a specific feature of an <code class=\\'doc-crbvvoca\\'>application</code>.')");
        SQLS.append(",('testcase','TestCase','','fr','Cas de test','')");
        SQLS.append(",('testcase','ticket','','en','Ticket','The is the Ticket Number that provided the implementation of the <code class=\\'doc-crbvvoca\\'>test case</code>.')");
        SQLS.append(",('testcase','ToBuild','','en','To Sprint',' ')");
        SQLS.append(",('testcase','ToRev','','en','To Rev',' ')");
        SQLS.append(",('testcasecountryproperties','Database','','en','DTB','Database where the SQL will be executed.<br>This is only applicable if the property type is <code class=\\'doc-fixed\\'>executeSql</code> or <code class=\\'doc-fixed\\'>executeSqlFromLib</code>.')");
        SQLS.append(",('testcasecountryproperties','Description','','en','Description','Description of the property.')");
        SQLS.append(",('testcasecountryproperties','Length','','en','Length','It is the length parameter of the property.<br>The parameter usage depend on the <code class=\\'doc-fixed\\'>type</code> of the property.')");
        SQLS.append(",('testcasecountryproperties','Nature','','en','Nature','Nature is the parameter which define the unicity of the property calculation for this testcase in every execution.<br><br>It can take the following values :')");
        SQLS.append(",('testcasecountryproperties','Nature','NOTINUSE','en','Not currently beeing used values.','<code class=\\'doc-fixed\\'>NOTINUSE</code> : When the property return a list of value, NOTINUSE will take value(s) on the list that is not already used on a pending execution.<br><br>Criterias : Same property name, same country, same environment with execution status = PE (exclusing execution older than n minutes).<br> n can be cnfigured with cerberus_notinuse_timeout parameter.')");
        SQLS.append(",('testcasecountryproperties','Nature','RANDOM','en','Random values.','<code class=\\'doc-fixed\\'>RANDOM</code> : When the property return a list of value, RANDOM will take a random value(s) on the list. It could be the same accross 2 different executions.')");
        SQLS.append(",('testcasecountryproperties','Nature','RANDOMNEW','en','Always unique random values.','<code class=\\'doc-fixed\\'>RANDOMNEW</code> : When the property return a list of values, RANDOMNEW will take random value(s) on the list that were not already used.<br><br>Criterias : Same Propertyname, same country, same environment, same test, same testcase and same build.<br>NB : revision is not part of the criterias.')");
        SQLS.append(",('testcasecountryproperties','Nature','STATIC','en','No unicity rule.','<code class=\\'doc-fixed\\'>STATIC</code> : No rules are defined. The property could be the same for all the executions')");
        SQLS.append(",('testcasecountryproperties','Property','','en','Property','This is the reference of the property.<br><br>A property is a data string that can be calculated in any moment during the <code class=\\'doc-crbvvoca\\'>test case</code>. Property can be defined directly inside the <code class=\\'doc-crbvvoca\\'>test case</code> but also calculated dynamically from an SQL or even read from the current html page.<br><br>When property is attached to an action, the associated action is executed only if the property exist in the country.<br><br>Once a property has been calculated, its value can be reused in any other property, action and controls using % before and after its reference.')");
        SQLS.append(",('testcasecountryproperties','RetryNb','','en','Retry','Integer that correspond to the number of time the calculation of the property will be done until the property gets a valid result.')");
        SQLS.append(",('testcasecountryproperties','RetryPeriod','','en','Period','Integer that specify the amount of time (in ms) that Cerberus will wait between 2 retry attempt of the property calculation.')");
        SQLS.append(",('testcasecountryproperties','RowLimit','','en','RowLimit','When the property calculation return a list of value, Rowlimit will limit the number of rows considered for random purposes.<br><br>For example, in 100 possible random values if rowLimit is 50, only the 50 first values will be accounted for random.')");
        SQLS.append(",('testcasecountryproperties','Type','','en','Type1','This is the type of command which will be used to calculate the property.<br><br>It can take the following values :')");
        SQLS.append(",('testcasecountryproperties','Type','executeSoapFromLib','en','[DEPRECATED] Get a value from a SOAP Request','TBD.')");
        SQLS.append(",('testcasecountryproperties','Type','executeSql','en','Get a value from an SQL execution.','<code class=\\'doc-fixed\\'>executeSQL</code> will allow you to execute an SQL Query (Value) on the DTB Database.<br><br>Usage :<br><doc class=\"usage\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>Field</th><th class=\\'ex\\'>Usage</th><tr><td class=\\'ex\\'>DTB</td><td class=\\'ex\\'>Value of the database that correspond to the connection pool where the SQL will be executed.</td></tr><tr><td class=\\'ex\\'>Value</td><td class=\\'ex\\'>SQL instruction to be executed. All system and property variables can be used.</td></tr><tr><td class=\\'ex\\'>Length</td><td class=\\'ex\\'>Not used</td></tr><tr><td class=\\'ex\\'>RowLimit</td><td class=\\'ex\\'>Used depending on the Nature.</td></tr><tr><td class=\\'ex\\'>Nature</td><td class=\\'ex\\'>Nature to be used for unicity constrain.</td></tr></table></doc><br><br>Example :<br><doc class=\"examples\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>DTB</th><th class=\\'ex\\'>Value</th><th class=\\'ex\\'>Nature</th><th class=\\'ex\\'>Description</th><tr><td class=\\'ex\\'>AS400Data</td><td class=\\'ex\\'><code class=\\'doc-sql\\'>SELECT customer FROM table;</code></td><td class=\\'ex\\'>RANDOM</td><td class=\\'ex\\'>One of the customer value returned by the SQL will be used.</td></tr><tr><td class=\\'ex\\'>MySQLData</td><td class=\\'ex\\'><code class=\\'doc-sql\\'>SELECT user FROM toto;</code></td><td class=\\'ex\\'>STATIC</td><td class=\\'ex\\'>The first <code class=\\'doc-fixed\\'>user</code> of <code class=\\'doc-fixed\\'>toto</code> table will be used.</td></tr></table></doc>')");
        SQLS.append(",('testcasecountryproperties','Type','executeSqlFromLib','en','[DEPRECATED] Get a value from an SQL execution from the library.','<code class=\\'doc-fixed\\'>executeSQLFromLib</code> will allow you to execute an SQL Query on the DTB Database from a library of SQL.<br>This type has the same behaviour as executeSQL type except that the SQLs are gathered from a library of SQL.<br>NB : This feature is DEPRECATED and should not be used. Please use GetFromTestDataLib in stead.<br><br>Usage :<br><doc class=\"usage\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>Field</th><th class=\\'ex\\'>Usage</th><tr><td class=\\'ex\\'>DTB</td><td class=\\'ex\\'>Value of the database that correspond to the connection pool where the SQL will be executed.</td></tr><tr><td class=\\'ex\\'>Value</td><td class=\\'ex\\'>Reference of the SQL instruction stored inside SQL Library.</td></tr><tr><td class=\\'ex\\'>Length</td><td class=\\'ex\\'>Not used</td></tr><tr><td class=\\'ex\\'>RowLimit</td><td class=\\'ex\\'>Used depending on the Nature.</td></tr><tr><td class=\\'ex\\'>Nature</td><td class=\\'ex\\'>Nature to be used for unicity constrain.</td></tr></table></doc><br><br>Example :<br><doc class=\"examples\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>DTB</th><th class=\\'ex\\'>Value</th><th class=\\'ex\\'>Nature</th><th class=\\'ex\\'>Description</th><tr><td class=\\'ex\\'>AS400Data</td><td class=\\'ex\\'>CUSTOMER1</td><td class=\\'ex\\'>RANDOM</td><td class=\\'ex\\'>One of the customer value returned by the SQL attached to CUSTOMER1 will be used.</td></tr><tr><td class=\\'ex\\'>MySQLData</td><td class=\\'ex\\'>USER1</td><td class=\\'ex\\'>STATIC</td><td class=\\'ex\\'>The first value of the 1st column of the SQL attached to USER1 SQL will be used.</td></tr></table></doc>')");
        SQLS.append(",('testcasecountryproperties','Type','getAttributeFromHtml','en','Get an attribute value from an HTML field in the current page.','<code class=\\'doc-fixed\\'>getAttributeFromHtml</code> will allow you to take an attribute value from an html field on the current webpage.</br>Cerberus will automatically wait for the field to start to appear before getting the attribute value.<br>The different attributes identifier that can be used in order to find the HTML field are : id, name, class, css, xpath, link, and data-cerberus.<br>Syntax is as follow :<br><code class=\\'doc-sql\\'>identifier=html-value</code><br><br>Usage :<br><doc class=\"usage\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>Field</th><th class=\\'ex\\'>Usage</th><tr><td class=\\'ex\\'>DTB</td><td class=\\'ex\\'>Not used.</td></tr><tr><td class=\\'ex\\'>Value</td><td class=\\'ex\\'>IDENTIFIER=HTML-VALUE</td></tr><tr><td class=\\'ex\\'>Value2</td><td class=\\'ex\\'>ATTRIBUTE</td></tr><tr><td class=\\'ex\\'>Length</td><td class=\\'ex\\'>Not used</td></tr><tr><td class=\\'ex\\'>RowLimit</td><td class=\\'ex\\'>Not used.</td></tr><tr><td class=\\'ex\\'>Nature</td><td class=\\'ex\\'>Not used.</td></tr></table></doc><br><br>Examples :<br><doc class=\"examples\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>HTML</th><th class=\\'ex\\'>Value</th><th class=\\'ex\\'>Value2</th><th class=\\'ex\\'>Result</th><tr><td class=\\'ex\\'><textarea rows=\"3\" style=\"width: 245px;\" readonly><div class=\"Main\"><img id=\"Name\" src=\"toto.jpeg\"><span id=\"env\">PRODUCTION</span></div></textarea></td><td class=\\'ex\\'>id=Name</td><td class=\\'ex\\'>src</td><td class=\\'ex\\'>toto.jpeg</td></tr><tr><td class=\\'ex\\'><textarea rows=\"3\" style=\"width: 245px;\" readonly><input data-cerberus=\"ctl00\" name=\"inputName\">toto</input></textarea></td><td class=\\'ex\\'>data-cerberus=ctl00</td><td class=\\'ex\\'>name</td><td class=\\'ex\\'>inputName</td></tr></table></doc>')");
        SQLS.append(",('testcasecountryproperties','Type','getDifferencesFromXml','en','Get differences from 2 XML files','TBD.')");
        SQLS.append(",('testcasecountryproperties','Type','getFromCookie','en','Get a value from Cookie.','<code class=\\'doc-fixed\\'>getFromCookie</code> will allow you to get information on cookie.<br><br>Usage :<br><doc class=\"usage\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>Field</th><th class=\\'ex\\'>Usage</th><tr><td class=\\'ex\\'>DTB</td><td class=\\'ex\\'>Not used.</td></tr><tr><td class=\\'ex\\'>Value1</td><td class=\\'ex\\'>Cookie Name.<br>In case the cookie is not found, empty string will be returned.</td></tr><tr><td class=\\'ex\\'>Value2</td><td class=\\'ex\\'>Information on cookie.<br>It could be name, value, expiry, domain, path, isHttpOnly, isSecure.</td></tr><tr><td class=\\'ex\\'>Length</td><td class=\\'ex\\'>Not used</td></tr><tr><td class=\\'ex\\'>RowLimit</td><td class=\\'ex\\'>Not used.</td></tr><tr><td class=\\'ex\\'>Nature</td><td class=\\'ex\\'>Not used.</td></tr></table></doc><br><br>Examples :<br><doc class=\"examples\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>Value1</th><th class=\\'ex\\'>Value2</th><th class=\\'ex\\'>Result</th><tr><td class=\\'ex\\'>COOKIE_NAME</td><td class=\\'ex\\'>value</td><td class=\\'ex\\'>COOKIE_VALUE</td></tr><tr><td class=\\'ex\\'>COOKIE_NAME2</td><td class=\\'ex\\'>Expiry</td><td class=\\'ex\\'>01-01-2015</td></tr><tr><td class=\\'ex\\'>COOKIE_NAME3</td><td class=\\'ex\\'>host</td><td class=\\'ex\\'>www.cerberus-testing.org</td></tr></table></doc>')");
        SQLS.append(",('testcasecountryproperties','Type','getFromDataLib','en','Get an object from the Data Library.','<code class=\\'doc-fixed\\'>getFromDataLib</code> will allow you to calculate a full object that include a list of string.</br>The return of the object can be used with either of the following syntax : %PROPERTY.subdata% or %PROPERTY(subdata)%.<br>Multiples rows can be retreived and you can access it using the following syntax : %PROPERTY.3.subdata% or %PROPERTY(3)(subdata)%<br>Use the Data library screen in order to configure the data library.<br><br>Usage :<br><doc class=\"usage\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>Field</th><th class=\\'ex\\'>Usage</th><tr><td class=\\'ex\\'>DTB</td><td class=\\'ex\\'>Not used.</td></tr><tr><td class=\\'ex\\'>Value</td><td class=\\'ex\\'>Data Library Name.</td></tr><tr><td class=\\'ex\\'>Length</td><td class=\\'ex\\'>Number of rows the object will retreive. Use %PROPERTY.n.subdata% in order to get the corresponding row.<br>In case not enougth data can be retreive, the property will report a NA status.</td></tr><tr><td class=\\'ex\\'>RowLimit</td><td class=\\'ex\\'>Max number of rows that will be fetch from the data source. If 0 the parameter cerberus_testdatalib_fetchmax is used.</td></tr><tr><td class=\\'ex\\'>Nature</td><td class=\\'ex\\'>Nature to be used for unicity constrain. STATIC, RANDOM, RANDOMNEW and NOTINUSE can be used.</td></tr></table></doc><br><br>Examples :<br><doc class=\"examples\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>Value</th><th class=\\'ex\\'>Length</th><th class=\\'ex\\'>rowLimit</th><th class=\\'ex\\'>Nature</th><th class=\\'ex\\'>Result</th><tr><td class=\\'ex\\'>toto</td><td class=\\'ex\\'>0</td><td class=\\'ex\\'>0</td><td class=\\'ex\\'>STATIC</td><td class=\\'ex\\'>1 row from \\'toto\\' Library.</td></tr><tr><td class=\\'ex\\'>toto%SYS_COUNTRY%titi</td><td class=\\'ex\\'>10</td><td class=\\'ex\\'>0</td><td class=\\'ex\\'>STATIC</td><td class=\\'ex\\'>10 rows of data from \\'totoPTtiti\\' Library.</td></tr><tr><td class=\\'ex\\'>toto</td><td class=\\'ex\\'>5</td><td class=\\'ex\\'>50</td><td class=\\'ex\\'>RANDOM</td><td class=\\'ex\\'>5 different rows picked by random in the 50 rows retreived from \\'toto\\' Library.</td></tr></table></doc>')");
        SQLS.append(",('testcasecountryproperties','Type','getFromGroovy','en','Get a value from a Groovy expression.','<code class=\\'doc-fixed\\'>getFromGroovy</code> will allow you to calculate a string from a Groovy execution.\n<br/>\nUsing this feature, you can use the full power of Groovy without the need to be related to a web context as the <a href=\"/Cerberus/Documentation.jsp?DocTable=testcasecountryproperties&DocField=type&DocValue=getFromJS&Lang=en\"><code>getFromJS</code></a> property type.\n<br/>\n<br/>\nUsage:\n<br/>\n<doc class=\\'usage\\'>\n  <table cellspacing=\\'0\\' cellpadding=\\'2\\'>\n    <tr>\n      <th class=\\'ex\\'>Field</th>\n      <th class=\\'ex\\'>Usage</th>\n    </tr>\n    <tr>\n      <td class=\\'ex\\'>DTB</td>\n      <td class=\\'ex\\'>Not used.</td>\n    </tr>\n    <tr>\n      <td class=\\'ex\\'>Value</td>\n      <td class=\\'ex\\'>Groovy expression to execute.</td>\n    </tr>\n    <tr>\n      <td class=\\'ex\\'>Length</td>\n      <td class=\\'ex\\'>Not used.</td>\n    </tr>\n    <tr>\n      <td class=\\'ex\\'>RowLimit</td>\n      <td class=\\'ex\\'>Not used.</td>\n    </tr>\n    <tr>\n      <td class=\\'ex\\'>Nature</td>\n      <td class=\\'ex\\'>Not used.</td>\n    </tr>\n  </table>\n</doc>\n<br/>\n<br/>\nFor examples:\n<br/>\n<doc class=\\'examples\\'>\n  <table cellspacing=\\'0\\' cellpadding=\\'2\\'>\n    <tr>\n      <th class=\\'ex\\'>Value</th>\n      <th class=\\'ex\\'>Result</th>\n    </tr>\n    <tr>\n      <td class=\\'ex\\'><code class=\\'doc-fixed\\'>1+1</code></td>\n      <td class=\\'ex\\'>2</td>\n    </tr>\n    <tr>\n      <td class=\\'ex\\'><code class=\\'doc-fixed\\'>\"foobar\".replace(\"foo\", \"oof\")</code></td>\n      <td class=\\'ex\\'><code class=\\'doc-fixed\\'>oofbar</code></td>\n    </tr>\n    <tr>\n      <td class=\\'ex\\'><code class=\\'doc-fixed\\'>\"foo\".toUpperCase().equals(\"FOO\")</code></td>\n      <td class=\\'ex\\'><code class=\\'doc-fixed\\'>true</code></td>\n    </tr>\n    <tr>\n      <td class=\\'ex\\'><code class=\\'doc-fixed\\'>123 == 123<code></td>\n      <td class=\\'ex\\'><code class=\\'doc-fixed\\'>true<code></td>\n    </tr>\n    <tr>\n      <td class=\\'ex\\'>\n        <code class=\\'doc-fixed\\'>\n        def square = { number -> number * number };\n        square(2)\n      </code>\n      </td>\n      <td class=\\'ex\\'><code class=\\'doc-fixed\\'>4</code></td>\n    </tr>\n  </table>\n</doc>\n<br/>\n<br/>\nFor more information, you can access to the fully documentation from the <a href=\"http://groovy-lang.org/documentation.html\">official Groovy website</a>.\n')");
        SQLS.append(",('testcasecountryproperties','Type','getFromHtml','en','Get a value from the current web page.','<code class=\\'doc-fixed\\'>getFromHtml</code> will allow you to take a value from an html field on the current webpage.</br>Cerberus will automatically wait for the field to start to appear before getting the value.<br>Different attributes identifier can be used in order to find the field : id, name, class, css, xpath, link, and data-cerberus.<br>Syntax is as follow :<br><code class=\\'doc-sql\\'>identifier=html-value</code><br><br>NB : getFromHtml will get the value of the field even if it does not appear on the web page (this is not the standard behaviour of Selenium).<br><br>Usage :<br><doc class=\"usage\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>Field</th><th class=\\'ex\\'>Usage</th><tr><td class=\\'ex\\'>DTB</td><td class=\\'ex\\'>Not used.</td></tr><tr><td class=\\'ex\\'>Value</td><td class=\\'ex\\'>IDENTIFIER=HTML-VALUE</td></tr><tr><td class=\\'ex\\'>Length</td><td class=\\'ex\\'>Not used</td></tr><tr><td class=\\'ex\\'>RowLimit</td><td class=\\'ex\\'>Not used.</td></tr><tr><td class=\\'ex\\'>Nature</td><td class=\\'ex\\'>Not used.</td></tr></table></doc><br><br>Examples :<br><doc class=\"examples\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>HTML</th><th class=\\'ex\\'>Value</th><th class=\\'ex\\'>Result</th><tr><td class=\\'ex\\'><textarea rows=\"3\" style=\"width: 245px;\" readonly><div class=\"Main\"><span id=\"Name\">FRONT1</span><span id=\"env\">PRODUCTION</span></div></textarea></td><td class=\\'ex\\'>id=Name</td><td class=\\'ex\\'>FRONT1</td></tr><tr><td class=\\'ex\\'><textarea rows=\"3\" style=\"width: 245px;\" readonly><input name=\"crb-name\" type=\"hidden\" value=\"CERBERUS\"/></textarea></td><td class=\\'ex\\'>name=crb-name</td><td class=\\'ex\\'>CERBERUS</td></tr><tr><td class=\\'ex\\'><textarea rows=\"3\" style=\"width: 245px;\" readonly><input data-cerberus=\"ctl00\">toto</input></textarea></td><td class=\\'ex\\'>data-cerberus=ctl00</td><td class=\\'ex\\'>toto</td></tr></table></doc>')");
        SQLS.append(",('testcasecountryproperties','Type','getFromHtmlVisible','en','Get a visible value from the current web page.','<code class=\\'doc-fixed\\'>getFromHtmlVisible</code> will allow you to take a visible value from an html field on the current webpage.</br>Cerberus will automatically wait for the field to start to appear before getting the value.<br>Different attributes identifier can be used in order to find the field : id, name, class, css, xpath, link, and data-cerberus.<br>Syntax is as follow :<br><code class=\\'doc-sql\\'>identifier=html-value</code><br><br>NB : getFromHtmlVisible will return an empty value if the field does not appear on the web page (this is the standard behaviour of Selenium).<br><br>Usage :<br><doc class=\"usage\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>Field</th><th class=\\'ex\\'>Usage</th><tr><td class=\\'ex\\'>DTB</td><td class=\\'ex\\'>Not used.</td></tr><tr><td class=\\'ex\\'>Value</td><td class=\\'ex\\'>IDENTIFIER=HTML-VALUE</td></tr><tr><td class=\\'ex\\'>Length</td><td class=\\'ex\\'>Not used</td></tr><tr><td class=\\'ex\\'>RowLimit</td><td class=\\'ex\\'>Not used.</td></tr><tr><td class=\\'ex\\'>Nature</td><td class=\\'ex\\'>Not used.</td></tr></table></doc><br><br>Examples :<br><doc class=\"examples\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>HTML</th><th class=\\'ex\\'>Value</th><th class=\\'ex\\'>Result</th><tr><td class=\\'ex\\'><textarea rows=\"3\" style=\"width: 245px;\" readonly><div class=\"Main\"><span id=\"Name\">FRONT1</span><span id=\"env\">PRODUCTION</span></div></textarea></td><td class=\\'ex\\'>id=Name</td><td class=\\'ex\\'></td></tr><tr><td class=\\'ex\\'><textarea rows=\"3\" style=\"width: 245px;\" readonly><input name=\"crb-name\" type=\"hidden\" value=\"CERBERUS\"/></textarea></td><td class=\\'ex\\'>name=crb-name</td><td class=\\'ex\\'></td></tr><tr><td class=\\'ex\\'><textarea rows=\"3\" style=\"width: 245px;\" readonly><input data-cerberus=\"ctl00\">toto</input></textarea></td><td class=\\'ex\\'>data-cerberus=ctl00</td><td class=\\'ex\\'>toto</td></tr></table></doc>')");
        SQLS.append(",('testcasecountryproperties','Type','getFromJS','en','Get a value from the output of a javascript execution.','<code class=\\'doc-fixed\\'>getFromJS</code> will allow you to calculate a string from a javascript execution.</br>Using this feature, you can use the full power of javascript in order to calculate values in the context of the current page.<br><br>Usage :<br><doc class=\"usage\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>Field</th><th class=\\'ex\\'>Usage</th><tr><td class=\\'ex\\'>DTB</td><td class=\\'ex\\'>Not used.</td></tr><tr><td class=\\'ex\\'>Value</td><td class=\\'ex\\'>Javascript to execute.<br>In case the javascript return no value, empty string will be returned.</td></tr><tr><td class=\\'ex\\'>Length</td><td class=\\'ex\\'>Not used</td></tr><tr><td class=\\'ex\\'>RowLimit</td><td class=\\'ex\\'>Not used.</td></tr><tr><td class=\\'ex\\'>Nature</td><td class=\\'ex\\'>Not used.</td></tr></table></doc><br><br>Examples :<br><doc class=\"examples\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>Value</th><th class=\\'ex\\'>Result</th><tr><td class=\\'ex\\'>return s.products == undefined || s.products.toLowerCase().split(\\'event16=\\').length == 1 ? \\'\\' : \\'fail_case\\';</td><td class=\\'ex\\'>fail_case</td></tr><tr><td class=\\'ex\\'>return GetCookie(\\'UserIdentificationId\\') == undefined ? \\'\\' : GetCookie(\\'UserIdentificationId\\');</td><td class=\\'ex\\'></td></tr><tr><td class=\\'ex\\'>return s.eVar5 == undefined ? \\'\\' : s.eVar5;</td><td class=\\'ex\\'></td></tr><tr><td class=\\'ex\\'>return new Date().getTime().toString() + \"@cerberus-testing.org\";</td><td class=\\'ex\\'>1391154967143@cerberus-testing.org</td></tr></table></doc>')");
        SQLS.append(",('testcasecountryproperties','Type','getFromJson','en','Get a value from a Json File','<code class=\\'doc-fixed\\'>getFromJson</code> will allow you to take an element value from a Json File.</br>Cerberus will download the Json File (Calling the URL in the Value1 field) and parse it to return the element value expected (Specified in the Value2 field).<br><br>Usage :<br><doc class=\"usage\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>Field</th><th class=\\'ex\\'>Usage</th><tr><td class=\\'ex\\'>DTB</td><td class=\\'ex\\'>Not used.</td></tr><tr><td class=\\'ex\\'>Value</td><td class=\\'ex\\'>URL of the Json File</td></tr><tr><td class=\\'ex\\'>Value2</td><td class=\\'ex\\'>Element to Find</td></tr><tr><td class=\\'ex\\'>Length</td><td class=\\'ex\\'>Not used</td></tr><tr><td class=\\'ex\\'>RowLimit</td><td class=\\'ex\\'>Not used.</td></tr><tr><td class=\\'ex\\'>Nature</td><td class=\\'ex\\'>Not used.</td></tr></table></doc><br><br>Examples :<br><doc class=\"examples\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>Json File</th><th class=\\'ex\\'>Value</th><th class=\\'ex\\'>Value2</th><th class=\\'ex\\'>Result</th><tr><td class=\\'ex\\'><textarea rows=\"3\" style=\"width: 245px;\" readonly>{\"text1\": \"yes\",\"text2\": 1234, \"array\":[\"first\", \"second\", \"third\"]}</textarea></td><td class=\\'ex\\'>http://url_of_the_json/file.json</td><td class=\\'ex\\'>text2</td><td class=\\'ex\\'>1234</td></tr><tr><td class=\\'ex\\'><textarea rows=\"3\" style=\"width: 245px;\" readonly>{\"text1\": \"yes\",\"text2\": 1234, \"array\":[\"first\", \"second\", \"third\"]}</textarea></td><td class=\\'ex\\'>http://url_of_the_json/file.json</td><td class=\\'ex\\'>array[1]</td><td class=\\'ex\\'>second</td></tr></table></doc>')");
        SQLS.append(",('testcasecountryproperties','Type','getFromTestData','en','[DEPRECATED] Get a value from Cerberus Test Data.','<code class=\\'doc-fixed\\'>getFromDataLib</code> will allow you to calculate a full object that include a list of string.</br>The return of the object can be used with either of the following syntax : %PROPERTY.subdata% or %PROPERTY(subdata)%.<br>Multiples rows can be retreived and you can access it using the following syntax : %PROPERTY.3.subdata% or %PROPERTY(3)(subdata)%<br>Use the Data library screen in order to configure the data library.<br><br>Usage :<br><doc class=\"usage\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>Field</th><th class=\\'ex\\'>Usage</th><tr><td class=\\'ex\\'>DTB</td><td class=\\'ex\\'>Not used.</td></tr><tr><td class=\\'ex\\'>Value</td><td class=\\'ex\\'>Text.</td></tr><tr><td class=\\'ex\\'>Number of rows the object will retreive. Use %PROPERTY.n.subdata% in order to get the corresponding row.<br>In case not enought dat can be retreive, the property will report a NA status.</td><td class=\\'ex\\'>Size of the string if Nature is STATIC.</td></tr><tr><td class=\\'ex\\'>RowLimit</td><td class=\\'ex\\'>Limit the data retreive from the source.</td></tr><tr><td class=\\'ex\\'>Nature</td><td class=\\'ex\\'>Nature to be used for unicity constrain.STATIC, RANDOM, RANDOMNEW and NOTINUSE can be used.</td></tr></table></doc><br><br>Examples :<br><doc class=\"examples\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>Value</th><th class=\\'ex\\'>Nature</th><th class=\\'ex\\'>Length</th><th class=\\'ex\\'>Result</th><tr><td class=\\'ex\\'>toto</td><td class=\\'ex\\'>STATIC</td><td class=\\'ex\\'>0</td><td class=\\'ex\\'>toto</td></tr><tr><td class=\\'ex\\'>toto%SYS_COUNTRY%titi</td><td class=\\'ex\\'>STATIC</td><td class=\\'ex\\'>0</td><td class=\\'ex\\'>totoPTtiti</td></tr><tr><td class=\\'ex\\'>toto</td><td class=\\'ex\\'>RANDOM</td><td class=\\'ex\\'>5</td><td class=\\'ex\\'>a5Gx3</td></tr></table></doc>')");
        SQLS.append(",('testcasecountryproperties','Type','getFromXML','en','Get a value from an XML file.','<code class=\\'doc-fixed\\'>getFromXml</code> will allow you to get value from an XML file specifying the URL of the file and the xpath to eecute to get the data.<br><br>Usage :<br><doc class=\"usage\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>Field</th><th class=\\'ex\\'>Usage</th><tr><td class=\\'ex\\'>DTB</td><td class=\\'ex\\'>Not used.</td></tr><tr><td class=\\'ex\\'>Value1</td><td class=\\'ex\\'>URL to the Xml file to parse.</td></tr><tr><td class=\\'ex\\'>Value2</td><td class=\\'ex\\'>xpath information to get data.</td></tr><tr><td class=\\'ex\\'>Length</td><td class=\\'ex\\'>Not used</td></tr><tr><td class=\\'ex\\'>RowLimit</td><td class=\\'ex\\'>Not used.</td></tr><tr><td class=\\'ex\\'>Nature</td><td class=\\'ex\\'>Not used.</td></tr></table></doc><br><br>Examples :<br>Parsing a file www.cerberus-testing.org/test.xml which contains an xml structure with ResponseCode element equals to OK and ResponseValue equals to 12345, it should be configured that way:<br><ResponseCode<doc class=\"examples\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>Value1</th><th class=\\'ex\\'>Value2</th><th class=\\'ex\\'>Result</th><tr><td class=\\'ex\\'>www.cerberus-testing.org/test.xml</td><td class=\\'ex\\'>//ResponseCode</td><td class=\\'ex\\'>OK</td></tr><tr><td class=\\'ex\\'>www.cerberus-testing.org/test.xml</td><td class=\\'ex\\'>//ResponseValue</td><td class=\\'ex\\'>12345</td></tr></table></doc>')");
        SQLS.append(",('testcasecountryproperties','Type','text','en','Simple text.','<code class=\\'doc-fixed\\'>text</code> will allow you to calculate a string.</br>Using the Nature, random string can also be calculated.<br><br>Usage :<br><doc class=\"usage\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>Field</th><th class=\\'ex\\'>Usage</th><tr><td class=\\'ex\\'>DTB</td><td class=\\'ex\\'>Not used.</td></tr><tr><td class=\\'ex\\'>Value</td><td class=\\'ex\\'>Text.</td></tr><tr><td class=\\'ex\\'>Length</td><td class=\\'ex\\'>Size of the string if Nature is STATIC.</td></tr><tr><td class=\\'ex\\'>RowLimit</td><td class=\\'ex\\'>Not used.</td></tr><tr><td class=\\'ex\\'>Nature</td><td class=\\'ex\\'>Nature to be used for unicity constrain. Only Static and RANDOM (or RANDOMNEW) are used.</td></tr></table></doc><br><br>Examples :<br><doc class=\"examples\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>Value</th><th class=\\'ex\\'>Nature</th><th class=\\'ex\\'>Length</th><th class=\\'ex\\'>Result</th><tr><td class=\\'ex\\'>toto</td><td class=\\'ex\\'>STATIC</td><td class=\\'ex\\'>0</td><td class=\\'ex\\'>toto</td></tr><tr><td class=\\'ex\\'>toto%SYS_COUNTRY%titi</td><td class=\\'ex\\'>STATIC</td><td class=\\'ex\\'>0</td><td class=\\'ex\\'>totoPTtiti</td></tr><tr><td class=\\'ex\\'>toto</td><td class=\\'ex\\'>RANDOM</td><td class=\\'ex\\'>5</td><td class=\\'ex\\'>a5Gx3</td></tr></table></doc>')");
        SQLS.append(",('testcasecountryproperties','Value','','en','Value','Value of the property. Depend on the <code class=\\'doc-fixed\\'>type</code> of property chosen.<br><br>Get more information on <code class=\\'doc-fixed\\'>type</code> field.<br><br><table cellspacing=0 cellpadding=3><th class=\\'ex\\' colspan=\\'2\\'>The following system variables can be used</th><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_SYSTEM%</code></td><td class=\\'ex\\'>System value</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_APPLI%</code></td><td class=\\'ex\\'>Application reference</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_APP_DOMAIN%</code></td><td class=\\'ex\\'>Domain of the Application</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_APP_VAR1%</code></td><td class=\\'ex\\'>VAR1 of the application on the environment.</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_APP_VAR2%</code></td><td class=\\'ex\\'>VAR2 of the application on the environment.</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_APP_VAR3%</code></td><td class=\\'ex\\'>VAR3 of the application on the environment.</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_APP_VAR4%</code></td><td class=\\'ex\\'>VAR4 of the application on the environment.</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_ENV%</code></td><td class=\\'ex\\'>Environment value</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_ENVGP%</code></td><td class=\\'ex\\'>Environment group code</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_COUNTRY%</code></td><td class=\\'ex\\'>Country code</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_TESTCASE%</code></td><td class=\\'ex\\'>TestCase</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_COUNTRYGP1%</code></td><td class=\\'ex\\'>Country group1 value</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_SSIP%</code></td><td class=\\'ex\\'>Selenium server IP</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_SSPORT%</code></td><td class=\\'ex\\'>Selenium server port</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_TAG%</code></td><td class=\\'ex\\'>Execution tag</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_EXECUTIONID%</code></td><td class=\\'ex\\'>Execution ID</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_EXESTORAGEURL%</code></td><td class=\\'ex\\'>Path where media are stored (based from the exeid).</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_TODAY-yyyy%</code></td><td class=\\'ex\\'>Year of today</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_TODAY-MM%</code></td><td class=\\'ex\\'>Month of today</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_TODAY-dd%</code></td><td class=\\'ex\\'>Day of today</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_TODAY-doy%</code></td><td class=\\'ex\\'>Day of today from the beginning of the year</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_TODAY-HH%</code></td><td class=\\'ex\\'>Hour of today</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_TODAY-mm%</code></td><td class=\\'ex\\'>Minute of today</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_TODAY-ss%</code></td><td class=\\'ex\\'>Second of today</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_YESTERDAY-yyyy%</code></td><td class=\\'ex\\'>Year of yesterday</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_YESTERDAY-MM%</code></td><td class=\\'ex\\'>Month of yesterday</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_YESTERDAY-dd%</code></td><td class=\\'ex\\'>Day of yesterday</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_TODAY-doy%</code></td><td class=\\'ex\\'>Day of yesterday from the beginning of the year</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_YESTERDAY-HH%</code></td><td class=\\'ex\\'>Hour of yesterday</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_YESTERDAY-mm%</code></td><td class=\\'ex\\'>Minute of yesterday</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_YESTERDAY-ss%</code></td><td class=\\'ex\\'>Second of yesterday</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_ELAPSED-EXESTART%</code></td><td class=\\'ex\\'>Number of milisecond since the start of the execution.</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_ELAPSED-STEPSTART%</code></td><td class=\\'ex\\'>Number of milisecond since the start of the execution of the current step.</td></tr></table>')");
        SQLS.append(",('testcaseexecution','Browser','','en','Browser','This is the browser that was used to run the <code class=\\'doc-crbvvoca\\'>test case</code> (only used if that was a GUI application <code class=\\'doc-crbvvoca\\'>test case</code>).')");
        SQLS.append(",('testcaseexecution','BrowserFullVersion','','en','Browser Version','This is the full version information of the browser that was used to run the <code class=\\'doc-crbvvoca\\'>test case</code> (only used if that was a GUI application <code class=\\'doc-crbvvoca\\'>test case</code>).')");
        SQLS.append(",('testcaseexecution','Build','','en','Sprint','Name of the Build/sprint.')");
        SQLS.append(",('testcaseexecution','ControlMessage','','en','ControlMessage','This is the message reported by Cerberus on the execution of the <code class=\\'doc-crbvvoca\\'>test case</code>.')");
        SQLS.append(",('testcaseexecution','controlstatus','','en','RC','This is the return code of the Execution. It can take the following values :<br><br><b>OK</b> : The <code class=\\'doc-crbvvoca\\'>test case</code> has been executed and everything happened as expected.<br><b>KO</b> : The <code class=\\'doc-crbvvoca\\'>test case</code> has been executed and reported an error that will create a bug.<br><b>NA</b> : The <code class=\\'doc-crbvvoca\\'>test case</code> has been executed but some data to perform the test could not be collected (SQL returning empty resultset).<br><b>FA</b> : The <code class=\\'doc-crbvvoca\\'>test case</code> failed to execute because there were an error inside the test such as an SQL error. The <code class=\\'doc-crbvvoca\\'>test case</code> needs to be corrected.<br><b>CA</b> : The <code class=\\'doc-crbvvoca\\'>test case</code> has been cancelled. It failed during the execution because of technical issues (ex. Lost of connection issue to selenium during the execution)<br><b>PE</b> : The execution is still running and not finished yet or has been interupted.')");
        SQLS.append(",('testcaseexecution','crbversion','','en','Cerberus Version','This is the version of the Cerberus Engine that executed the <code class=\\'doc-crbvvoca\\'>test case</code>.<br>This data has been created for tracability purpose as the behavious of Cerberus could varry from one version to another.')");
        SQLS.append(",('testcaseexecution','end','','en','End',' ')");
        SQLS.append(",('testcaseexecution','executor','','en','Executor user','This is the name of the executor user that executed the <code class=\\'doc-crbvvoca\\'>test case</code>.<br>This data has been created for tracability purpose as the result of Cerberus Test could varry between human and selenium test.')");
        SQLS.append(",('testcaseexecution','id','','en','Execution ID',' ')");
        SQLS.append(",('testcaseexecution','IP','','en','IP','This is the ip of the machine of the Selenium Server where the <code class=\\'doc-crbvvoca\\'>test case</code> executed.')");
        SQLS.append(",('testcaseexecution','Port','','en','Port','This is the port used to contact the Selenium Server where the <code class=\\'doc-crbvvoca\\'>test case</code> executed.')");
        SQLS.append(",('testcaseexecution','Revision','','en','Revision','Number of the Revision')");
        SQLS.append(",('testcaseexecution','screensize','','en','Screen Size','This is the real size of the browser screen that performed the execution of the <code class=\\'doc-crbvvoca\\'>test case</code>.')");
        SQLS.append(",('testcaseexecution','start','','en','Start',' ')");
        SQLS.append(",('testcaseexecution','status','','en','TC Status','This correspond to the status of the <code class=\\'doc-crbvvoca\\'>test case</code> when it was executed.<br>This is used to identify executions done on stable <code class=\\'doc-crbvvoca\\'>test case</code> compared to the ones done on draft version. ')");
        SQLS.append(",('testcaseexecution','tag','','en','Tag','The Tag is just a string defined by the user that will be recorded with the execution. Its purpose is to help to find back some specific executions.')");
        SQLS.append(",('testcaseexecution','URL','','en','URL','Full URL used to connect to the application.')");
        SQLS.append(",('testcaseexecution','verbose','','en','Verbose','This correspond to the level if information that Cerberus will keep when performing the execution. It can take the following values :<br><br><b>0</b> : The test will keep minimum login information in order to preserve the response times. This is to be used when a massive amout of tests are performed. No details on action will be saved.<br><b>1</b> : This is the standard level of log. Detailed action execution information will also be stored.<br><b>2</b> : This is the highest level of detailed information that can be chosen. Detailed web traffic information will be stored. This is to be used only on very specific cases where all hits information of an execution are required.<br><br>NB : Verbose level higher that 0 rely on Network traffic (only available on firefox browser).')");
        SQLS.append(",('testcaseexecutiondata','Value','','en','Property Value','This is the Value of the calculated Property.')");
        SQLS.append(",('testcaseexecutionwwwsum','css_nb','','en','Css_nb','Number of css downloaded for all the scenario')");
        SQLS.append(",('testcaseexecutionwwwsum','css_size_max','','en','Css_size_max','Size of the biggest css dowloaded during the scenario')");
        SQLS.append(",('testcaseexecutionwwwsum','css_size_tot','','en','Css_size_tot','Total size of the css for the scenario')");
        SQLS.append(",('testcaseexecutionwwwsum','css_tps','','en','Css_tps','Cumulated time for download css for all the scenario')");
        SQLS.append(",('testcaseexecutionwwwsum','img_nb','','en','Img_nb','Number of pictures downloaded for all the scenario')");
        SQLS.append(",('testcaseexecutionwwwsum','img_size_max','','en','Img_size_max','Size of the biggest Picture dowloaded during the scenario')");
        SQLS.append(",('testcaseexecutionwwwsum','img_size_tot','','en','Img_size_tot','Total size of the Pictures for the scenario')");
        SQLS.append(",('testcaseexecutionwwwsum','img_tps','','en','Img_tps','Cumulated time for downloaded pictures for all the scenario')");
        SQLS.append(",('testcaseexecutionwwwsum','js_nb','','en','Js_nb','Number of javascript downloaded for all the scenario')");
        SQLS.append(",('testcaseexecutionwwwsum','js_size_max','','en','Js_size_max','Size of the biggest javascript dowloaded during the scenario')");
        SQLS.append(",('testcaseexecutionwwwsum','js_size_tot','','en','Js_size_tot','Total size of the javascript for the scenario')");
        SQLS.append(",('testcaseexecutionwwwsum','js_tps','','en','Js_tps','Cumulated time for downloaded javascripts for all the scenario')");
        SQLS.append(",('testcaseexecutionwwwsum','nb_rc2xx','','en','Nb_rc2xx','Number of return code hits between 200 and 300')");
        SQLS.append(",('testcaseexecutionwwwsum','nb_rc3xx','','en','Nb_rc3xx','Number of return code hits between 300 and 400')");
        SQLS.append(",('testcaseexecutionwwwsum','nb_rc4xx','','en','Nb_rc4xx','Number of return code hits between 400 and 500')");
        SQLS.append(",('testcaseexecutionwwwsum','nb_rc5xx','','en','Nb_rc5xx','Number of return code hits higher than 500')");
        SQLS.append(",('testcaseexecutionwwwsum','tot_nbhits','','en','Tot_nbhits','Total number of hits of a scenario')");
        SQLS.append(",('testcaseexecutionwwwsum','tot_size','','en','Tot_size','Total size of all the elements')");
        SQLS.append(",('testcaseexecutionwwwsum','tot_tps','','en','Tot_tps','Total time cumulated for the download of all the elements')");
        SQLS.append(",('testcaselabel','labelId','','en','Label ID','')");
        SQLS.append(",('testcaselabel','labelId','','fr','ID du label','')");
        SQLS.append(",('testcasestep','step','','en','Step','A step is a group of actions.')");
        SQLS.append(",('testcasestepaction','Action','','en','Action','It is the action that will be executed by Cerberus.<br><br>It can take the following values :')");
        SQLS.append(",('testcasestepaction','Action','calculateProperty','en','Calculate a Cerberus property.','<code class=\\'doc-fixed\\'>calculateProperty</code> will allow you to calculate a property defined in the property section of the test case.\n\n<br/><br/>\n\nUsage :<br/>\n\n<doc class=\"usage\">\n <table>\n  <tr>\n   <th class=\\'ex\\'>Field</th>\n   <th class=\\'ex\\'>Usage</th>\n  </tr>\n  <tr>\n   <td class=\\'ex\\'>Value1</td>\n   <td class=\\'ex\\'>Property name to be calculated.</td>\n  </tr>\n  <tr>\n   <td class=\\'ex\\'>Value2</td>\n   <td class=\\'ex\\'>[Optional] Property name from which get value to affect property from Value1. Useful to override the one defined from the property section.</td>\n  </tr>\n </table>\n</doc>\n\n<br/><br/>\n\nExamples :<br/>\n\n<doc class=\"examples\">\n <table>\n  <tr>\n   <th class=\\'ex\\'>Value1</th>\n   <th class=\\'ex\\'>Value2</th>\n   <th class=\\'ex\\'>Result</th>\n  </tr>\n  <tr>\n   <td class=\\'ex\\'>PROPERTY_NAME</td>\n   <td class=\\'ex\\'></td>\n   <td class=\\'ex\\'>PROPERTY_NAME will be calculated</td>\n  </tr>\n  <tr>\n   <td class=\\'ex\\'>PROPERTY_NAME</td>\n   <td class=\\'ex\\'>OTHER_PROPERTY_NAME</td>\n   <td class=\\'ex\\'>PROPERTY_NAME will be affected by the calculated value of OTHER_PROPERTY_NAME</td>\n  </tr>\n </table>\n</doc>')");
        SQLS.append(",('testcasestepaction','Action','callSoap','en','Call Soap.','TBD')");
        SQLS.append(",('testcasestepaction','Action','callSoapWithBase','en','Call Soap with Base','<code class=\\'doc-fixed\\'>callSoapWithBase</code> will allow you to make a SOAP call (Stored on the <a href=\"./SoapLibrary.jsp\">SoapLibrary</a>) using the servicePath stored at the countryenvrionmentparameters level. That allow to call the soap on the environment of the execution.<br><br> The result will be stored in the memory. On this result, you can make some control (verify the presence or the content of the elements for exemple) or get some information using property getFromXML<br><br>Usage :<br><doc class=\"usage\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>Field</th><th class=\\'ex\\'>Usage</th><tr><td class=\\'ex\\'>Value1</td><td class=\\'ex\\'>Name of the SOAP from the SOAPLibrary.</td></tr><tr><td class=\\'ex\\'>Value2</td><td class=\\'ex\\'></td></tr></table></doc><br><br>Examples :<br><doc class=\"examples\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>Value1</th><th class=\\'ex\\'>Value2</th><th class=\\'ex\\'>Result</th><tr><td class=\\'ex\\'>WEATHER</td><td class=\\'ex\\'></td><td class=\\'ex\\'>WEATHER soapCall will be made.</td></tr></table></doc>')");
        SQLS.append(",('testcasestepaction','Action','click','en','Clicking on a button.','<code class=\\'doc-fixed\\'>click</code> will allow you to click on an element inside the current page.<br><br>Usage :<br><doc class=\"usage\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>Field</th><th class=\\'ex\\'>Usage</th><tr><td class=\\'ex\\'>Value1</td><td class=\\'ex\\'>Identifier and name of the element to click in the form of : identifier=html_reference.</td></tr><tr><td class=\\'ex\\'>Value2</td><td class=\\'ex\\'></td></tr></table></doc><br><br>Examples :<br><doc class=\"examples\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>Value1</th><th class=\\'ex\\'>Value2</th><th class=\\'ex\\'>Result</th><tr><td class=\\'ex\\'>id=html_reference</td><td class=\\'ex\\'></td><td class=\\'ex\\'>element that has id equal to html_reference will be clicked</td></tr></table></doc>')");
        SQLS.append(",('testcasestepaction','Action','doNothing','en','Just perform no action.','<code class=\\'doc-fixed\\'>doNothing</code> will just perfom no action. Can be used in case of control that must be done without action before.<br><br>Usage :<br><doc class=\"usage\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>Field</th><th class=\\'ex\\'>Usage</th><tr><td class=\\'ex\\'>Value1</td><td class=\\'ex\\'></td></tr><tr><td class=\\'ex\\'>Value2</td><td class=\\'ex\\'></td></tr></table></doc><br><br>Examples :<br><doc class=\"examples\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>Value1</th><th class=\\'ex\\'>Value2</th><th class=\\'ex\\'>Result</th><tr><td class=\\'ex\\'></td><td class=\\'ex\\'></td><td class=\\'ex\\'>No action will be executed and engine will go to the next action or control</td></tr></table></doc>')");
        SQLS.append(",('testcasestepaction','Action','doubleClick','en','Double clicking on a button.','<code class=\\'doc-fixed\\'>doubleClick</code> will allow you to double click on an element inside the current page.<br><br>Usage :<br><doc class=\"usage\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>Field</th><th class=\\'ex\\'>Usage</th><tr><td class=\\'ex\\'>Value1</td><td class=\\'ex\\'>Identifier and name of the element to double click in the form of : identifier=html_reference.</td></tr><tr><td class=\\'ex\\'>Value2</td><td class=\\'ex\\'></td></tr></table></doc><br><br>Examples :<br><doc class=\"examples\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>Value1</th><th class=\\'ex\\'>Value2</th><th class=\\'ex\\'>Result</th><tr><td class=\\'ex\\'>id=html_reference</td><td class=\\'ex\\'></td><td class=\\'ex\\'>element that has id equal to html_reference will be double clicked.</td></tr></table></doc>')");
        SQLS.append(",('testcasestepaction','Action','executeSqlStoredProcedure','en','Execute SQL Stored Procedure','<code class=\\'doc-fixed\\'>executeSqlStoredProcedure</code> will allow you to execute SQL stored procedure.<br>Parameter cerberus_actionexecutesqlstoredprocedure_timeout can be used in order to tune the timeout.<br><br>Usage :<br><doc class=\"usage\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>Field</th><th class=\\'ex\\'>Usage</th><tr><td class=\\'ex\\'>Value1</td><td class=\\'ex\\'>Name of the Database to connect to.</td></tr><tr><td class=\\'ex\\'>Value2</td><td class=\\'ex\\'>Property name of the procedure to execute. The property should be a text one.</td></tr></table></doc><br><br>Examples :<br><doc class=\"examples\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>Value1</th><th class=\\'ex\\'>Value2</th><th class=\\'ex\\'>Result</th><tr><td class=\\'ex\\'>CRB</td><td class=\\'ex\\'>PROPERTY_NAME</td><td class=\\'ex\\'>The procedure name declared in the property PROPERTY_NAME will be executed on database CRB through connection pool that has been configured in JDBC Ressource of the corresponding CRB database on the corresponding environment.</td></tr></table></doc>')");
        SQLS.append(",('testcasestepaction','Action','executeSqlUpdate','en','Execute SQL update','<code class=\\'doc-fixed\\'>executeSqlUpdate</code> will allow you to execute SQL update (insert,delete,update).<br>Parameter cerberus_actionexecutesqlupdate_timeout can be used in order to tune the timeout.<br><br>Usage :<br><doc class=\"usage\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>Field</th><th class=\\'ex\\'>Usage</th><tr><td class=\\'ex\\'>Value1</td><td class=\\'ex\\'>Name of the Database to connect to.</td></tr><tr><td class=\\'ex\\'>Value2</td><td class=\\'ex\\'>Property name of the script to execute. The property should be a text one.</td></tr></table></doc><br><br>Examples :<br><doc class=\"examples\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>Value1</th><th class=\\'ex\\'>Value2</th><th class=\\'ex\\'>Result</th><tr><td class=\\'ex\\'>CRB</td><td class=\\'ex\\'>PROPERTY_NAME</td><td class=\\'ex\\'>The SQL declared in the property PROPERTY_NAME will be executed on database CRB through connection pool that has been configured in JDBC Ressource of the corresponding CRB database on the corresponding environment.</td></tr></table></doc>')");
        SQLS.append(",('testcasestepaction','Action','focusDefaultIframe','en','Focus on the default frame.','TBD')");
        SQLS.append(",('testcasestepaction','Action','focusToIframe','en','Focus to a specific frame.','TBD')");
        SQLS.append(",('testcasestepaction','Action','getPageSource','en','[DEPRECATED] getPageSource','<code class=\\'doc-fixed\\'>getPageSource</code> will allow you to record the source of the page opened.<br>Action is DEPRECATED. Please use the getPageSource control in stead.<br><br> The result will be stored in a file which will be available in the execution detail<br><br>Usage :<br><doc class=\"usage\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>Field</th><th class=\\'ex\\'>Usage</th><tr><td class=\\'ex\\'>Value1</td><td class=\\'ex\\'></td></tr><tr><td class=\\'ex\\'>Value2</td><td class=\\'ex\\'></td></tr></table></doc><br><br>Examples :<br><doc class=\"examples\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>Value1</th><th class=\\'ex\\'>Value2</th><th class=\\'ex\\'>Result</th><tr><td class=\\'ex\\'></td><td class=\\'ex\\'> </td><td class=\\'ex\\'>Source will be recorded</td></tr></table></doc>')");
        SQLS.append(",('testcasestepaction','Action','hideKeyboard','en','Hide keyboard.','Hide the currently visible keyboard.')");
        SQLS.append(",('testcasestepaction','Action','keypress','en','Press a specific key.','<code class=\\'doc-fixed\\'>keypress</code> will allow you to press any key in the current web page.<br><br>Usage :<br><doc class=\"usage\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>Field</th><th class=\\'ex\\'>Usage</th><tr><td class=\\'ex\\'>Value1</td><td class=\\'ex\\'>Keycode of the key to press.</td></tr><tr><td class=\\'ex\\'>Value2</td><td class=\\'ex\\'></td></tr></table></doc><br><br>Examples :<br><doc class=\"examples\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>Value1</th><th class=\\'ex\\'>Value2</th><th class=\\'ex\\'>Result</th><tr><td class=\\'ex\\'>ENTER</td><td class=\\'ex\\'></td><td class=\\'ex\\'>ENTER key will be pressed.</td></tr><tr><td class=\\'ex\\'>SEARCH</td><td class=\\'ex\\'></td><td class=\\'ex\\'>SEARCH key will be pressed.</td></tr></table></doc>')");
        SQLS.append(",('testcasestepaction','Action','manageDialog','en','Manage javascript dialog opened by application, specified ok to accept it or cancel to dismiss','<b>manageDialog</b><br><br>Let possibility to testcase to handle javascript dialog <br>Specify <b>ok</b> value to accept it or <b>cancel</b> to dismiss.')");
        SQLS.append(",('testcasestepaction','Action','mouseLeftButtonPress','en','Click mouse button and hold it clicked. ','TBD')");
        SQLS.append(",('testcasestepaction','Action','mouseLeftButtonRelease','en','Release clicked mouse button. ','TBD')");
        SQLS.append(",('testcasestepaction','Action','mouseOver','en','Mouse cursor over an object.','TBD')");
        SQLS.append(",('testcasestepaction','Action','mouseOverAndWait','en','Mouse cursor over an object and wait for a certain time.','TBD')");
        SQLS.append(",('testcasestepaction','Action','openUrl','en','Open a URL','<code class=\\'doc-fixed\\'>openUrl</code> will allow you to open a specific URL.<br><br>Usage :<br><doc class=\"usage\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>Field</th><th class=\\'ex\\'>Usage</th><tr><td class=\\'ex\\'>Value1</td><td class=\\'ex\\'>Absolute URL to open. </td></tr><tr><td class=\\'ex\\'>Value2</td><td class=\\'ex\\'></td></tr></table></doc><br><br>Examples :<br><doc class=\"examples\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>Value1</th><th class=\\'ex\\'>Value2</th><th class=\\'ex\\'>Result</th><tr><td class=\\'ex\\'>http://www.cerberus-testing.org/contextroot/login/login.aspx</td><td class=\\'ex\\'></td><td class=\\'ex\\'>www.cerberus-testing.org/contextroot/login/login.aspx URL will be open.</td></tr></table></doc>')");
        SQLS.append(",('testcasestepaction','Action','openUrlLogin','en','Open the Login URL.','TBD')");
        SQLS.append(",('testcasestepaction','Action','openUrlWithBase','en','Open a URL.','<code class=\\'doc-fixed\\'>openUrlWithBase</code> will allow you to open a specific URL.<br><br>Usage :<br><doc class=\"usage\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>Field</th><th class=\\'ex\\'>Usage</th><tr><td class=\\'ex\\'>Value1</td><td class=\\'ex\\'>Relative URL to open. hostname and context root will automatically be prefixed.</td></tr><tr><td class=\\'ex\\'>Value2</td><td class=\\'ex\\'></td></tr></table></doc><br><br>Examples :<br><doc class=\"examples\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>Value1</th><th class=\\'ex\\'>Value2</th><th class=\\'ex\\'>Result</th><tr><td class=\\'ex\\'>login/login.aspx</td><td class=\\'ex\\'></td><td class=\\'ex\\'>www.cerberus-testing.org/contextroot/login/login.aspx URL will be open.</td></tr></table></doc>')");
        SQLS.append(",('testcasestepaction','Action','removeDifference','en','Remove Differences.','TBD')");
        SQLS.append(",('testcasestepaction','Action','rightClick','en','Right click on an element.','<code class=\\'doc-fixed\\'>rightClick</code> will allow you to right click on an element inside the current page.<br><br>Usage :<br>\n<doc class=\"usage\">\n    <table cellspacing=0 cellpadding=2>\n        <th class=\\'ex\\'>Field</th>\n        <th class=\\'ex\\'>Usage</th>\n        <tr>\n            <td class=\\'ex\\'>Value1</td>\n            <td class=\\'ex\\'>Identifier and name of the element to right click in the form of : identifier=html_reference.</td>\n        </tr>\n        <tr>\n            <td class=\\'ex\\'>Value2</td>\n            <td class=\\'ex\\'>\n            </td>\n        </tr>\n    </table>\n</doc><br><br>Examples :<br>\n<doc class=\"examples\">\n    <table cellspacing=0 cellpadding=2>\n        <th class=\\'ex\\'>Value1</th>\n        <th class=\\'ex\\'>Value2</th>\n        <th class=\\'ex\\'>Result</th>\n        <tr>\n            <td class=\\'ex\\'>id=html_reference</td>\n            <td class=\\'ex\\'></td>\n            <td class=\\'ex\\'>element that has id equal to html_reference will be right clicked</td>\n        </tr>\n    </table>\n</doc>')");
        SQLS.append(",('testcasestepaction','Action','select','en','Select a value on a combo.','<b>select :</b> When the action expected is to select a value from a select box.<br><br><dd><u><i>How to feed it :</i></u> <br><br><dd><i>Action =</i> select, <i>Value =</i> the <i>id</i> of the select box.  and <i>Property =</i> the property containing the value to select.<br>It could be label=TheExactNameOfTheValue or value=the first letter or the place number of the value expected in the select box<br>For example : label=WEB   , value=W   , value=3 if the WEB is the third value in the selectbox<br><br><br>')");
        SQLS.append(",('testcasestepaction','Action','skipAction','en','Skip this action.','<code class=\\'doc-fixed\\'>skipAction</code> will skip the action. Can be used in case of temporary disabling the associated controls.<br><br>Usage :<br><doc class=\"usage\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>Field</th><th class=\\'ex\\'>Usage</th><tr><td class=\\'ex\\'>Value1</td><td class=\\'ex\\'></td></tr><tr><td class=\\'ex\\'>Value2</td><td class=\\'ex\\'></td></tr></table></doc><br><br>Examples :<br><doc class=\"examples\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>Value1</th><th class=\\'ex\\'>Value2</th><th class=\\'ex\\'>Result</th><tr><td class=\\'ex\\'></td><td class=\\'ex\\'></td><td class=\\'ex\\'>No action will be executed and engine will go to the next action.</td></tr></table></doc>')");
        SQLS.append(",('testcasestepaction','Action','swipe','en','Swipe the screen.','<code class=\\'doc-fixed\\'>swipe</code> will allow you to swipe a mobile screen to a specific direction.<br><br>Usage :<br><doc class=\"usage\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>Field</th><th class=\\'ex\\'>Usage</th><tr><td class=\\'ex\\'>Value1</td><td class=\\'ex\\'>Direction to swipe (UP, DOWN, RIGHT, LEFT or CUSTOM). In case of UP, DOWN, RIGHT and LEFT, swipe is done by computing from 1/3 to 2/3 of the screen resolution.</td></tr><tr><td class=\\'ex\\'>Value2</td><td class=\\'ex\\'>Only in case of CUSTOM swipe direction, specify the custom direction thanks to the following format: x1;y1;x2;y2, where x1 and y1 are the coordinates of the start position on the screen and x2 and y2 are the coordinates of the end position on the screen.</td></tr></table></doc><br><br>Examples :<br><doc class=\"examples\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>Value1</th><th class=\\'ex\\'>Value2</th><th class=\\'ex\\'>Result</th><tr><td class=\\'ex\\'>UP</td><td class=\\'ex\\'></td><td class=\\'ex\\'>Swipe is done from down to up (so the page go down).</td></tr><tr><td class=\\'ex\\'>CUSTOM</td><td class=\\'ex\\'>100;200;300;400</td></code><td class=\\'ex\\'>Swipe goes from (x1 = 100; y1 = 200) to (x2 = 300; y2 = 400) on the screen.</td></tr></table></doc>')");
        SQLS.append(",('testcasestepaction','Action','switchToWindow','en','Switching the focus to a window.','When the Test case need to switch to another window (like popup dialog) this action is used. Just specify title of other window in objet to switch to this window.')");
        SQLS.append(",('testcasestepaction','Action','type','en','Put a data in a field.','<b>type :</b> When the action expected is to type something into a field.<br><br><dd><u><i>How to feed it :</i></u> <br><br><dd><i>Action =</i> type, <i>Value =</i> the <i>id</i> of the field  and <i>Property =</i> the property containing the value to type.<br><br><br>')");
        SQLS.append(",('testcasestepaction','Action','Unknown','en','Unknown action.','This is the default action defined inside Cerberus.<br>It can be used when the action has not been identified or clarified yet.<br>NB : It is not implemented and will report a FA status on the corresponding execution.')");
        SQLS.append(",('testcasestepaction','Action','wait','en','Wait for a certain amount of time.','<b>wait :</b> When the action expected is to wait 5 seconds.<br><br><dd><u><i>How to feed it :</i></u> <br><br><dd><i>Action =</i> wait, <i>Value =</i> null  and  <i>Property =</i> null.<br><br><br>')");
        SQLS.append(",('testcasestepaction','description','','en','Description','This is the functional desciption of the action.')");
        SQLS.append(",('testcasestepaction','ForceExeStatus','','en','Exe RC','<p>This parameter can be used in order to change the behaviour of the Cerberus execution engine.</p><p>If the field is empty, there will be no impact on the behaviour of the engine.</p>If PE, the execution will continue (stay pending) after the execution of the action no matter what is the result of the action. This value can be used in case an action needs to be done to perform the test but should not impact the result of the test if it fails (Ex : closing a marketing layer on a website).')");
        SQLS.append(",('testcasestepaction','image','','en','Picture','')");
        SQLS.append(",('testcasestepaction','Sequence','','en','Sequence','Sequence of execution of the actions inside the step.')");
        SQLS.append(",('testcasestepaction','Value1','','en','Val1','This is the information that is used to perform the action.<br>The same variable as property value field can be used (See <a href=\"?DocTable=testcasecountryproperties&DocField=Value\">doc</a>)<br>This information needs to be feed according to the action chosen.<br><br>Get more information on <code class=\\'doc-fixed\\'>action</code> field.')");
        SQLS.append(",('testcasestepaction','Value2','','en','Val2','This is the information that is used to perform the action.<br>The same variable as property value field can be used (See <a href=\"?DocTable=testcasecountryproperties&DocField=Value\">doc</a>)<br>This information needs to be feed according to the action chosen.<br><br>Get more information on <code class=\\'doc-fixed\\'>action</code> field.')");
        SQLS.append(",('testcasestepactioncontrol','Control','','en','Control','It is the control that will be executed by Cerberus.<br><br>It can take the following values :<br>')");
        SQLS.append(",('testcasestepactioncontrol','Control','getPageSource','en','Save source of the page.','TBD')");
        SQLS.append(",('testcasestepactioncontrol','Control','skipControl','en','Skip the control.','TBD')");
        SQLS.append(",('testcasestepactioncontrol','Control','takeScreenshot','en','Take a screenshot.','TBD')");
        SQLS.append(",('testcasestepactioncontrol','Control','Unknown','en','Default Control in Cerberus','TBD')");
        SQLS.append(",('testcasestepactioncontrol','Control','verifyElementClickable','en','True if element is clickable.','<b>verifyElementClickable</b><br><br>Verify if an element is clickable.<br><br><i>Control Property :</i> Element container<br><br>')");
        SQLS.append(",('testcasestepactioncontrol','Control','verifyElementDifferent','en','True if the ControlProp does not contains the same element ControlValue.','<b>verifyElementDifferent</b><br><br>Verify if the element is different from an another in an XML file.<br><br><i>Control Property :</i> XPath to the element<br><br><i>Control Value :</i> The element to be different<br><br>')");
        SQLS.append(",('testcasestepactioncontrol','Control','verifyElementEquals','en','True if the ControlProp contains the same element ControlValue.','<b>verifyElementEquals</b><br><br>Verify if the element equals to another in an XML file.<br><br><i>Control Property :</i> XPath to the element<br><br><i>Control Value :</i> The expected element<br><br>')");
        SQLS.append(",('testcasestepactioncontrol','Control','verifyElementInElement','en','True if the ControlProp contains an element ControlValue.','<b>verifyElementInElement</b><br><br>Verify if an element is contained in another on the webpage.<br><br><i>Control Property :</i> Element container<br><br><i>Control Value :</i> Element contained in the element Container<br><br>')");
        SQLS.append(",('testcasestepactioncontrol','Control','verifyElementNotClickable','en','True if element is not clickable.','<b>verifyElementNotClickable</b><br><br>Verify if an element is not clickable.<br><br><i>Control Property :</i> Element container<br><br>')");
        SQLS.append(",('testcasestepactioncontrol','Control','verifyElementNotPresent','en','True if element is not found on the current page.','TBD')");
        SQLS.append(",('testcasestepactioncontrol','Control','verifyElementNotVisible','en','True if element is present but not visible on the current page.','<b>verifyElementNotVisible</b><br><br>Verify if the HTML element specified exists, is not visible and has text on it')");
        SQLS.append(",('testcasestepactioncontrol','Control','verifyElementPresent','en','True if element is found on the current page.','<b>verifyElementPresent</b><br><br>Verify if an specific element is present on the web page <br><br>')");
        SQLS.append(",('testcasestepactioncontrol','Control','verifyElementVisible','en','True if element is visible on the current page.','<b>verifyElementVisible</b><br><br>Verify if the HTML element specified is exists, is visible and has text on it<br><br>')");
        SQLS.append(",('testcasestepactioncontrol','Control','verifyIntegerDifferent','en','True if the ControlProp is different than the integer ControlValue.','<b>verifyIntegerDifferent</b><br><br>Verify if two integers are differents.<br><br><i>Control Property :</i> The first integer<br><br><i>Control Value :</i> The second integer<br><br>')");
        SQLS.append(",('testcasestepactioncontrol','Control','verifyIntegerEquals','en','True if the ControlProp is equal to the integer ControlValue.','<b>verifyIntegerEquals</b><br><br>Verify if two integers are equals.<br><br><i>Control Property :</i> The first integer<br><br><i>Control Value :</i> The second integer<br><br>')");
        SQLS.append(",('testcasestepactioncontrol','Control','verifyIntegerGreater','en','True if an integer is greater than another.','TBD')");
        SQLS.append(",('testcasestepactioncontrol','Control','verifyIntegerMinor','en','True if an integer is lower than another.','TBD')");
        SQLS.append(",('testcasestepactioncontrol','Control','verifyRegexInElement','en','True if a regex match the content of a field.','TBD')");
        SQLS.append(",('testcasestepactioncontrol','Control','verifyStringContains','en','True if Property String contains value String.','<b>verifyStringContains</b><br><br>Verify if the value is contains in the propery<br><b>This test is case sensitive</b>')");
        SQLS.append(",('testcasestepactioncontrol','Control','verifyStringDifferent','en','True if 2 strings are different.','TBD')");
        SQLS.append(",('testcasestepactioncontrol','Control','verifyStringEqual','en','True if 2 strings are equal.','TBD')");
        SQLS.append(",('testcasestepactioncontrol','Control','verifyStringGreater','en','True if a string is greater than another.','TBD')");
        SQLS.append(",('testcasestepactioncontrol','Control','verifyStringMinor','en','True if a string is before another.','TBD')");
        SQLS.append(",('testcasestepactioncontrol','Control','verifyTextInDialog','en','True if property or value is equal to dialog text retrieved.','<b>verifyTextInDialog</b><br><br>Verify if the text in dialog is equal to property or dialog.')");
        SQLS.append(",('testcasestepactioncontrol','Control','verifyTextInElement','en','True if a text is inside a field.','TBD')");
        SQLS.append(",('testcasestepactioncontrol','Control','verifyTextInPage','en','True if a text is inside the source of the current page.','TBD')");
        SQLS.append(",('testcasestepactioncontrol','Control','verifyTextNotInElement','en','True if a text is not inside a field.','<b>verifyTextNotInElement</b><br><br>True if a text is not inside a field.<br><br><i>Control Property :</i> The field location<br><br><i>Control Value :</i> The text to test against the value from the field locatin<br><br>')");
        SQLS.append(",('testcasestepactioncontrol','Control','verifyTextNotInPage','en','True if a text is not inside the source of the current page.','TBD')");
        SQLS.append(",('testcasestepactioncontrol','Control','verifyTitle','en','True if the title of the current page equal to a string.','<b>verifytitle</b><br><br>Verify if the title of the webpage is the same than the value specified<br><br>')");
        SQLS.append(",('testcasestepactioncontrol','Control','verifyUrl','en','True if the URL of the current page equal to a string.','<b>verifyurl</b><br><br>Verify if the URL of the webpage is the same than the value specified<br><br><i>Control Value :</i>should be null<br><br><i>Control Property :</i> URL expected (without the base)<br><br>')");
        SQLS.append(",('testcasestepactioncontrol','Control','verifyXmlTreeStructure','en','Check if XML tree Structure is correct.','TBD')");
        SQLS.append(",('testcasestepactioncontrol','ControlSequence','','en','CtrlNum','This is the number of the control.<br>If you have more than one control attached to an action, use this value to order their execution.')");
        SQLS.append(",('testcasestepactioncontrol','Description','','en','Description','This is the functional desciption of the control.')");
        SQLS.append(",('testcasestepactioncontrol','Fatal','','en','Fatal','This define if the control is fatal.<br><br>If the control is fatal and KO, the execution will stop and execution status will report KO.<br>If the control is not fatal and KO, the execution will continue but execution status will still report a KO.')");
        SQLS.append(",('testcasestepactioncontrol','Sequence','','en','Sequence','It is the number of the sequence in which the control will be performed.<br><br>NB : Controls are performed after each action.')");
        SQLS.append(",('testcasestepactioncontrol','Step','','en','Step','')");
        SQLS.append(",('testcasestepactioncontrol','Value1','','en','Val1','This is the property that is going to be used inside the control.<br>The same variable as property value field can be used (See <a href=\"?DocTable=testcasecountryproperties&DocField=Value\">doc</a>)<br><br>Get more information on <code class=\\'doc-fixed\\'>type</code> field.')");
        SQLS.append(",('testcasestepactioncontrol','Value2','','en','Val2','This is the value that is going to be used inside the control.<br>The same variable as property value field can be used (See <a href=\"?DocTable=testcasecountryproperties&DocField=Value\">doc</a>)<br><br>Get more information on <code class=\\'doc-fixed\\'>type</code> field.')");
        SQLS.append(",('testcasestepactioncontrolexecution','ReturnCode','','en','Control Return Code','Return Code of the Control.')");
        SQLS.append(",('testcasestepactioncontrolexecution','ReturnMessage','','en','Return Message','This is the return message on that specific control.')");
        SQLS.append(",('testcasestepactionexecution','ForceExeStatus','','en','Force Execution RC','This is the value of the Force Execution Status data used by the engine during the execution of the testcase.')");
        SQLS.append(",('testcasestepactionexecution','ReturnCode','','en','Action Return Code','This is the return code of the action.')");
        SQLS.append(",('testcasestepactionexecution','screenshotfilename','','en','Screenshot Filename','This is the filename of the screenshot.<br>It is null if no screenshots were taken.')");
        SQLS.append(",('testdatalib','actions','','en','Actions','<p> List of available actions for the current user: </p><p><table border=\\'1\\'><tr><th class=\\'ex\\'>Button</th><th class=\\'ex\\'>Function</th><th class=\\'ex\\'>Description</th></tr><tr><td><span class=\\'glyphicon glyphicon-pencil\\'></span></td><td>Edit Entry</td><td>Allows the update of the library entry: system, environment, country, type, group, database, script, method, service path, envelope and description. The name is not editable.</td></tr><tr><td><span class=\\'glyphicon glyphicon-trash\\'></span></td><td>Delete Entry</td><td>Allows the deletion of a library entry (and its sub-data entries). Only the entries that are not being used are possible to be deleted. </td></tr> <tr><td><span class=\\'glyphicon glyphicon-list-alt\\'></span></td><td>Edit Sub-Data Entries</td><td>Allows the management of the sub-data set specified for the library entry. </td></tr><tr><td>TC</td><td>Get List of test cases that use this entry</td><td>Allows the visualisation of the test cases that are currently using the library entry.</td></tr></table></p>  ')");
        SQLS.append(",('testdatalib','actions_nopermissions','','en','Actions','<p> List of available actions for the current user: </p><p><table border=\\'1\\'><tr><th class=\\'ex\\'>Button</th><th class=\\'ex\\'>Function</th><th class=\\'ex\\'>Description</th></tr><tr><td><span class=\\'glyphicon glyphicon-list-alt\\'></span></td><td>Sub-Data Entries</td><td>Allows the visualisation of the sub-data set specified for the library entry. </td></tr><tr><td>TC</td><td>Get list of test cases that use this entry</td><td>Allows the visualisation of the test cases that are currently using the library entry.</td></tr></table></p>  ')");
        SQLS.append(",('testdatalib','country','','en','Country','<p>Country where the entry is available. If not specified, then the data entry apply to ALL countries. </p>')");
        SQLS.append(",('testdatalib','created','','en','Creation Date','')");
        SQLS.append(",('testdatalib','creator','','en','Creator','User who created the data.')");
        SQLS.append(",('testdatalib','csvUrl','','en','CSV URL','<p>CSV URL specifies the URL where the CSV can be reached.</p>')");
        SQLS.append(",('testdatalib','csvUrl','','fr','URL du CSV','<p>L’URL du CSV représente l’URL du fichier CSV à décrypter.</p>')");
        SQLS.append(",('testdatalib','database','','en','Database','<p>Specifies the database where the <i>script</i> attribute should be executed. </p> You can then configure the JDBC Connection pool on that database at the environment level. That allows to create testdata independant from the environement where the testcase is executed.')");
        SQLS.append(",('testdatalib','databaseCsv','','en','Database','<p>Specifies the database where the <i>Service Path</i> will be requested. </p> You can then configure the left part of the Service URL on that database at the environment level. That allows to create testdata independant from the environment where the testcase is executed.')");
        SQLS.append(",('testdatalib','databaseUrl','','en','Database','<p>Specifies the database where the <i>Service Path</i> will be requested. </p> You can then configure the left part of the Service URL on that database at the environment level. That allows to create testdata independant from the environment where the testcase is executed.')");
        SQLS.append(",('testdatalib','description','','en','Description','<p>Textual description of the entry.</p>')");
        SQLS.append(",('testdatalib','envelope','','en','Envelope','<p>Envelope that should be sent in the SOAP request.</p>')");
        SQLS.append(",('testdatalib','environment','','en','Environment','<p>Environment where the entry is available. If not specified, then the data entry apply to ALL environments.</p>')");
        SQLS.append(",('testdatalib','group','','en','Group','<p>Name that groups entries that are at some extent correlated. It is an <b>optional</b> attribute.</p>')");
        SQLS.append(",('testdatalib','lastmodified','','en','Modification Date','')");
        SQLS.append(",('testdatalib','lastmodifier','','en','Last Modifier','User who last modified the data.')");
        SQLS.append(",('testdatalib','method','','en','Method','<p>Method that is invoked by the SOAP request/call.</p>')");
        SQLS.append(",('testdatalib','name','','en','Name','<p>Name of the entry. It is a <b>mandatory</b> attribute.</p><p><b><u>Note</u></b>: The combination of <u>Name</u>, <u>System</u>, <u>Environment</u> and <u>Country</u> can be duplicated when the type is STATIC in order to allow a list of data to be available.</p>')");
        SQLS.append(",('testdatalib','script','','en','Script','<p>SQL commands that should be executed to retrieve test data.</p><p>Examples:</p><table><tr><td>select * from table;</td></tr><tr><td>select * from table where column = %COLUMN%;</td></tr></table>')");
        SQLS.append(",('testdatalib','separator','','en','Separator','<p>Separator used parsing a CSV.</p>')");
        SQLS.append(",('testdatalib','separator','','fr','Séparateur','<p>Séparateur à utiliser pour le décryptage du CSV.</p>')");
        SQLS.append(",('testdatalib','servicepath','','en','Service Path','<p>Location of the service.</p><p>Examples:</p><table><tr><td>http://mydomain/mywebservicelocation</td></tr><tr><td>mywebservicelocation</td></tr><tr><td>http://%MY_DYNAMIC_IP%/mywebservicelocation</td></tr><tr><td>%LOCATION%</td></tr></table>')");
        SQLS.append(",('testdatalib','system','','en','System','<p>System where the entry is available. If not specified, then the data entry apply to ALL systems.</p>')");
        SQLS.append(",('testdatalib','testdatalibid','','en','ID','<p>Unique identifier of the test data library entry</p>')");
        SQLS.append(",('testdatalib','type','','en','Type','<p>Entry Type - Cerberus allows the definition of 4 types: INTERNAL, SQL, CSV and SOAP.</p><table border=\\'1\\'> <tr><th class=\\'ex\\'>Type</th><th class=\\'ex\\'>Description</th></tr> <tr><td>INTERNAL</td><td>Static test data - in each execution the values used by the test cases are statically definied directly in Cerberus.</td></tr> <tr><td>SQL</td><td> Test data obtained from a SQL execution – values depend on what the SQL return on the corresponding environment.</td></tr><tr><td>SOAP</td><td>Test data obtained from a SOAP call – values depend on the result of the web service call.</td></tr><tr><td>CSV</td><td>Test data obtained from a CSV file structure privided by a URL. Values depend on the result of the service call to CSV file.</td></tr></table>')");
        SQLS.append(",('testdatalibdata','column','','en','Column','<p>Column name representing the value that should be obtained after executing a SQL instruction (select).</p>')");
        SQLS.append(",('testdatalibdata','columnPosition','','en','Column Position','<p>Column position [1,2,3…] representing the value that should be obtained after parsing a CSV file.</p>')");
        SQLS.append(",('testdatalibdata','columnPosition','','fr','Position','<p>Position [1,2,3…] de la valeur à obtenir lors du décryptage du CSV.</p>')");
        SQLS.append(",('testdatalibdata','description','','en','Description','<p>Textual description for the sub-data entry.</p>')");
        SQLS.append(",('testdatalibdata','parsingAnswer','','en','Parsing Answer','<p>XPath expression that allows the user to parse data from the SOAP response.</p>')");
        SQLS.append(",('testdatalibdata','subData','','en','Sub-data ','<p>Unique name for a sub-data entry. For a test data library entry, this value should be unique.</p>')");
        SQLS.append(",('testdatalibdata','value','','en','Value','<p>STATIC value.</p>')");
        SQLS.append(",('transversal','DateCreated','','en','Creation Date','Date of the creation of the object.')");
        SQLS.append(",('transversal','DateCreated','','fr','Date de Création','Date de création de l\\'objet.')");
        SQLS.append(",('transversal','DateModif','','en','Modification Date','Last modification Date of the object.')");
        SQLS.append(",('transversal','DateModif','','fr','Date de Modification','Date de dernière modification de l\\'objet.')");
        SQLS.append(",('transversal','UsrCreated','','en','Created by','User who created the object.')");
        SQLS.append(",('transversal','UsrCreated','','fr','Créé par','Utilisateur ayant créé l\\'objet.')");
        SQLS.append(",('transversal','UsrModif','','en','Modified by','Last modification date of the object.')");
        SQLS.append(",('transversal','UsrModif','','fr','Modifié par','Date de dernière modification de l\\'objet.')");
        SQLS.append(",('user','DefaultSystem','','en','Default System','This is the default <code class=\\'doc-crbvvoca\\'>system</code> the user works on the most. It is used to default the perimeter of <code class=\\'doc-crbvvoca\\'>test case</code> or <code class=\\'doc-crbvvoca\\'>applications</code> displayed on some Cerberus pages.')");
        SQLS.append(",('user','Team','','en','Team','This is the team of the user.')");
        SQLS.append(",('usergroup','GroupName','','en','Group Name','Authorities are managed by group. In order to be granted to a set of feature, you must belong to the corresponding group.<br>Every user can of course belong to as many group as necessary in order to get access to as many feature as required.<br>In order to get the full access to the system you must belong to every group.<br>Some groups are linked together on the test perimeter and integration perimeter.<br><br><b>Test perimeter :</b><br><br><code class=\\'doc-fixed\\'>TestRO</code>: Has read only access to the information related to test cases and also has access to execution reporting options.<br><br><code class=\\'doc-fixed\\'>Test</code>: Can modify non WORKING test cases but cannot delete test cases.<br><br><code class=\\'doc-fixed\\'>TestAdmin</code>: Can modify or delete any test case (including Pre Testing test cases). Can also create or delete a test.<br><br>The minimum group you need to belong is <code class=\\'doc-fixed\\'>TestRO</code> that will give you access in read only to all test data (including its execution reporting page).<br>If you want to be able to modify the testcases (except the WORKING ones), you need <code class=\\'doc-fixed\\'>Test</code> group on top of <code class=\\'doc-fixed\\'>TestRO</code> group.<br>If you want the full access to all testcase (including beeing able to delete any testcase), you will need <code class=\\'doc-fixed\\'>TestAdmin</code> on top of <code class=\\'doc-fixed\\'>TestRO</code> and <code class=\\'doc-fixed\\'>Test</code> group.<br><br><b>Test Data perimeter :</b><br><br><code class=\\'doc-fixed\\'>TestDataManager</code>: Can modify the test data..<br><br><b>Test Execution perimeter :</b><br><br><code class=\\'doc-fixed\\'>RunTest</code>: Can run both Manual and Automated test cases from GUI.<br><br><b>Integration perimeter :</b><br><br><code class=\\'doc-fixed\\'>IntegratorRO</code>: Has access to the integration status.<br><br><code class=\\'doc-fixed\\'>Integrator</code>: Can add an application. Can change parameters of the environments.<br><br><code class=\\'doc-fixed\\'>IntegratorNewChain</code>: Can register the end of the chain execution. Has read only access to the other informations on the same page.<br><br><code class=\\'doc-fixed\\'>IntegratorDeploy</code>: Can disable or enable environments and register new build / revision.<br><br>The minimum group you need to belong is <code class=\\'doc-fixed\\'>IntegratorRO</code> that will give you access in read only to all environment data.<br>If you want to be able to modify the environment data, you need <code class=\\'doc-fixed\\'>Integrator</code> group on top of <code class=\\'doc-fixed\\'>IntegratorRO</code> group.<br><code class=\\'doc-fixed\\'>IntegratorNewChain</code> and <code class=\\'doc-fixed\\'>IntegratorDeploy</code> are used on top of <code class=\\'doc-fixed\\'>Integrator</code> Group to be able to create a new chain on an environment or perform a deploy operation.<br><br><b>Administration perimeter :</b><br><br><code class=\\'doc-fixed\\'>Administrator</code>: Can create, modify or delete users. Has access to log Event and Database Maintenance. Can change Parameter values.')");
        SQLInstruction.add(SQLS.toString());

        // New updated Documentation.
        //-- ------------------------ 958
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `documentation` VALUES ");
        SQLS.append("('page_header','menuApplicationObjects','','en','Application Object','')");
        SQLS.append(",('page_header','menuApplicationObjects','','fr','Objet d application','')");
        SQLS.append(",('page_applicationObject','title','','en','Application Object','')");
        SQLS.append(",('page_applicationObject','title','','fr','Objet d application','')");
        SQLS.append(",('page_applicationObject','button_create','','en','Create an Application Object','')");
        SQLS.append(",('page_applicationObject','button_create','','fr','Créer un objet d application','')");
        SQLS.append(",('page_applicationObject','button_delete','','en','Delete Object','')");
        SQLS.append(",('page_applicationObject','button_delete','','fr','Supprimer l objet','')");
        SQLS.append(",('page_applicationObject','message_delete','','en','Are you sure?','')");
        SQLS.append(",('page_applicationObject','message_delete','','fr','Etes-vous sûrs?','')");
        SQLS.append(",('page_applicationObject','button_edit','','en','Edit Object','')");
        SQLS.append(",('page_applicationObject','button_edit','','fr','Modifier l objet','')");
        SQLS.append(",('page_applicationObject','applicationfield','','en','Application','')");
        SQLS.append(",('page_applicationObject','applicationfield','','fr','Application','')");
        SQLS.append(",('page_applicationObject','createapplicationobjectfield','','en','Create Application Object','')");
        SQLS.append(",('page_applicationObject','createapplicationobjectfield','','fr','Créer un object d application','')");
        SQLS.append(",('page_applicationObject','editapplicationobjectfield','','en','Edit Application Object','')");
        SQLS.append(",('page_applicationObject','editapplicationobjectfield','','fr','Modifier un objet d application','')");
        SQLS.append(",('page_applicationObject','objectfield','','en','Object','')");
        SQLS.append(",('page_applicationObject','objectfield','','fr','Objet','')");
        SQLS.append(",('page_applicationObject','valuefield','','en','Value','')");
        SQLS.append(",('page_applicationObject','valuefield','','fr','Valeur','')");
        SQLS.append(",('page_applicationObject','screenshotfilenamefield','','en','FileName','')");
        SQLS.append(",('page_applicationObject','screenshotfilenamefield','','fr','Nom du ficher','')");
        SQLS.append(",('page_applicationObject','button_close','','en','Close','')");
        SQLS.append(",('page_applicationObject','button_close','','fr','Fermer','')");
        SQLS.append(",('page_applicationObject','button_add','','en','Add','')");
        SQLS.append(",('page_applicationObject','button_add','','fr','Ajouter','')");
        SQLS.append(",('page_applicationObject','Application','','en','Application','')");
        SQLS.append(",('page_applicationObject','Application','','fr','Application','')");
        SQLS.append(",('page_applicationObject','Object','','en','Object','')");
        SQLS.append(",('page_applicationObject','Object','','fr','Objet','')");
        SQLS.append(",('page_applicationObject','Value','','en','Value','')");
        SQLS.append(",('page_applicationObject','Value','','fr','Valeur','')");
        SQLS.append(",('page_applicationObject','ScreenshotFileName','','en','File Name','')");
        SQLS.append(",('page_applicationObject','ScreenshotFileName','','fr','Nom du fichier','')");
        SQLS.append(",('page_applicationObject','UsrCreated','','en','Creator','')");
        SQLS.append(",('page_applicationObject','UsrCreated','','fr','Createur','')");
        SQLS.append(",('page_applicationObject','DateCreated','','en','Creation date','')");
        SQLS.append(",('page_applicationObject','DateCreated','','fr','Date de création','')");
        SQLS.append(",('page_applicationObject','UsrModif','','en','Last Modificator','')");
        SQLS.append(",('page_applicationObject','UsrModif','','fr','Dernier Editeur','')");
        SQLS.append(",('page_applicationObject','DateModif','','en','Last modification date','')");
        SQLS.append(",('page_applicationObject','DateModif','','fr','Date de dernière modification','');");
        SQLInstruction.add(SQLS.toString());

        // Add path to picture for appliation object in paramaters
        //-- ------------------------ 959
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `parameter` VALUES ");
        SQLS.append("('','cerberus_applicationobject_path','','Path whare you will store all the files you upload in application object');");
        SQLInstruction.add(SQLS.toString());

        // Create Table Application Object
        //-- ------------------------ 960
        SQLS = new StringBuilder();
        SQLS.append("CREATE TABLE `applicationobject` ("
                + "  `ID` int(11) NOT NULL AUTO_INCREMENT,"
                + "  `Application` varchar(200) NOT NULL,"
                + "  `Object` varchar(150) NOT NULL,"
                + "  `Value` text,"
                + "  `ScreenshotFileName` varchar(250) DEFAULT NULL,"
                + "  `UsrCreated` varchar(45) DEFAULT NULL,"
                + "  `DateCreated` timestamp NULL DEFAULT NULL,"
                + "  `UsrModif` varchar(45) DEFAULT NULL,"
                + "  `DateModif` timestamp NULL DEFAULT NULL,"
                + "  PRIMARY KEY (`Application`,`Object`),"
                + "  UNIQUE KEY `ID_UNIQUE` (`ID`),"
                + "  CONSTRAINT `fk_applicationobject_1` FOREIGN KEY (`Application`) REFERENCES `application` (`Application`) ON DELETE CASCADE ON UPDATE CASCADE"
                + ") ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8;");
        SQLInstruction.add(SQLS.toString());

        // Documentation update.
        //-- ------------------------ 961-962
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `documentation` SET `DocDesc`='Value of the property. Depend on the <code class=\\'doc-fixed\\'>type</code> of property chosen.<br><br>Get more information on <code class=\\'doc-fixed\\'>type</code> field.<br><br><table cellspacing=0 cellpadding=3><th class=\\'ex\\' colspan=\\'2\\'>The following system variables can be used</th><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_SYSTEM%</code></td><td class=\\'ex\\'>System value</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_APPLI%</code></td><td class=\\'ex\\'>Application reference</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_APP_DOMAIN%</code></td><td class=\\'ex\\'>Domain of the Application</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_APP_VAR1%</code></td><td class=\\'ex\\'>VAR1 of the application on the environment.</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_APP_VAR2%</code></td><td class=\\'ex\\'>VAR2 of the application on the environment.</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_APP_VAR3%</code></td><td class=\\'ex\\'>VAR3 of the application on the environment.</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_APP_VAR4%</code></td><td class=\\'ex\\'>VAR4 of the application on the environment.</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_ENV%</code></td><td class=\\'ex\\'>Environment value</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_ENVGP%</code></td><td class=\\'ex\\'>Environment group code</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_COUNTRY%</code></td><td class=\\'ex\\'>Country code</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_TEST%</code></td><td class=\\'ex\\'>Test.</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_TESTCASE%</code></td><td class=\\'ex\\'>TestCase</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_COUNTRYGP1%</code></td><td class=\\'ex\\'>Country group1 value</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_SSIP%</code></td><td class=\\'ex\\'>Selenium server IP</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_SSPORT%</code></td><td class=\\'ex\\'>Selenium server port</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_BROWSER%</code></td><td class=\\'ex\\'>Browser name of the current execution.</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_TAG%</code></td><td class=\\'ex\\'>Execution tag</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_EXECUTIONID%</code></td><td class=\\'ex\\'>Execution ID</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_EXESTART%</code></td><td class=\\'ex\\'>Start date and time of the execution with format : 2016-12-31 21:24:53.008.</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_EXESTORAGEURL%</code></td><td class=\\'ex\\'>Path where media are stored (based from the exeid).</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_STEP.n.RETURNCODE%</code></td><td class=\\'ex\\'>Return Code of the step n. n being the execution sequence of the step (sort).</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_TODAY-yyyy%</code></td><td class=\\'ex\\'>Year of today</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_TODAY-MM%</code></td><td class=\\'ex\\'>Month of today</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_TODAY-dd%</code></td><td class=\\'ex\\'>Day of today</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_TODAY-doy%</code></td><td class=\\'ex\\'>Day of today from the beginning of the year</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_TODAY-HH%</code></td><td class=\\'ex\\'>Hour of today</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_TODAY-mm%</code></td><td class=\\'ex\\'>Minute of today</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_TODAY-ss%</code></td><td class=\\'ex\\'>Second of today</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_YESTERDAY-yyyy%</code></td><td class=\\'ex\\'>Year of yesterday</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_YESTERDAY-MM%</code></td><td class=\\'ex\\'>Month of yesterday</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_YESTERDAY-dd%</code></td><td class=\\'ex\\'>Day of yesterday</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_TODAY-doy%</code></td><td class=\\'ex\\'>Day of yesterday from the beginning of the year</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_YESTERDAY-HH%</code></td><td class=\\'ex\\'>Hour of yesterday</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_YESTERDAY-mm%</code></td><td class=\\'ex\\'>Minute of yesterday</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_YESTERDAY-ss%</code></td><td class=\\'ex\\'>Second of yesterday</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_ELAPSED-EXESTART%</code></td><td class=\\'ex\\'>Number of milisecond since the start of the execution.</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_ELAPSED-STEPSTART%</code></td><td class=\\'ex\\'>Number of milisecond since the start of the execution of the current step.</td></tr></table>' WHERE `DocTable`='testcasecountryproperties' and`DocField`='Value' and`DocValue`='' and`Lang`='en';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `documentation` SET `DocDesc`='<code class=\\'doc-fixed\\'>verifyRegexInElement</code> will return true if a regex match the content of a field.<br><br>Usage :<br><doc class=\\\"usage\\\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>Field</th><th class=\\'ex\\'>Usage</th><tr><td class=\\'ex\\'>Value1</td><td class=\\'ex\\'>Field name</td></tr><tr><td class=\\'ex\\'>Value2</td><td class=\\'ex\\'>Regex</td></tr></table></doc><br><br>Examples :<br><doc class=\\\"examples\\\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>Value1</th><th class=\\'ex\\'>Value2</th><th class=\\'ex\\'>Result</th><tr><td class=\\'ex\\'>xpath=.//*[@class=\\'breadcrumbs\\']</td><td class=\\'ex\\'>.*becoming seller.*</td><td class=\\'ex\\'>true if the data inside the field contains \\'becoming seller\\'.</td></tr></table></doc><br>NB : Standard Java regex can be used. Further details <a href=\\\"https://docs.oracle.com/javase/7/docs/api/java/util/regex/Pattern.html\\\">here</a>.' WHERE `DocTable`='testcasestepactioncontrol' and`DocField`='Control' and`DocValue`='verifyRegexInElement' and`Lang`='en';");
        SQLInstruction.add(SQLS.toString());

        // PArameter new ExecutionDetail Page
        //-- ------------------------ 963
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `parameter` VALUES ('','cerberus_executiondetail_use','Y','Do you want to use the new Execution Detail Page (Y or N)')");
        SQLInstruction.add(SQLS.toString());

        // Add tracability fields in testcasestep table.
        //-- ------------------------ 964
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestep` ");
        SQLS.append("DROP COLUMN `last_modified`,");
        SQLS.append("ADD COLUMN `UsrCreated` VARCHAR(45) NOT NULL DEFAULT '' AFTER `inlibrary`,");
        SQLS.append("ADD COLUMN `DateCreated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER `UsrCreated`,");
        SQLS.append("ADD COLUMN `UsrModif` VARCHAR(45) NULL DEFAULT '' AFTER `DateCreated`,");
        SQLS.append("ADD COLUMN `DateModif` TIMESTAMP NOT NULL DEFAULT '1970-01-01 01:01:01' ;");
        SQLInstruction.add(SQLS.toString());

        // Clean Steps that use steps that also use step (we remove the link).
        //-- ------------------------ 965
        SQLS = new StringBuilder();
        SQLS.append("UPDATE testcasestep tcs SET DateModif=now(), UsrModif = 'DatabaseMaintenanceV964', useStep='N', UseStepTest='', UseStepTestCase='', UseStepStep=-1 where concat(tcs.test, '||', tcs.testcase, '||', tcs.step, '||') in (select concat(toto.test, '||', toto.testcase, '||', toto.step, '||') from (select tcsa.* from testcasestep tcsa join testcasestep tcs1 on tcs1.test=tcsa.useSteptest and tcs1.testcase=tcsa.useSteptestcase and tcs1.step=tcsa.useStepstep where tcsa.useStep = 'Y' and tcs1.useStep='Y' order by tcs1.test, tcs1.testcase, tcs1.step) as toto );");
        SQLInstruction.add(SQLS.toString());

        // Clean Steps that are used but not flagged as inLibrary (we flag them as can be used inLibrary='Y').
        //-- ------------------------ 966
        SQLS = new StringBuilder();
        SQLS.append("UPDATE testcasestep tcs SET DateModif=now(), UsrModif = 'DatabaseMaintenanceV965', inLibrary='Y' where concat(tcs.test, '||', tcs.testcase, '||', tcs.step, '||') in (select concat(toto.test, '||', toto.testcase, '||', toto.step, '||') from (select tcs1.* from testcasestep tcsa join testcasestep tcs1 on tcs1.test=tcsa.useSteptest and tcs1.testcase=tcsa.useSteptestcase and tcs1.step=tcsa.useStepstep where tcsa.useStep = 'Y' and tcs1.inLibrary!='Y' order by tcs1.test, tcs1.testcase, tcs1.step) as toto );");
        SQLInstruction.add(SQLS.toString());

        // Documentation typo.
        //-- ------------------------ 967
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `documentation` SET `DocLabel`='JDBC Resource', `DocDesc`='This is the name of the JDBC Resource used to connect to the corresponding <code class=\\'doc-crbvvoca\\'>database</code> on the <code class=\\'doc-crbvvoca\\'>country</code> / <code class=\\'doc-crbvvoca\\'>environment</code>.<br>The JDBC Resource (prefixed by <code class=\\'doc-fixed\\'>jdbc/</code> ) needs to be configured and associated to a connection pool on the application server that host the Cerberus application.<br><br>Example :<br><doc class=\\\"examples\\\"><table cellspacing=0 cellpadding=3><th class=\\'ex\\'>JDBC Resource</th><th class=\\'ex\\'>Application server Resource name</th><tr>\\n<td class=\\'ex\\'>MyConnection</td>\\n<td class=\\'ex\\'>jdbc/MyConnection</td>\\n</tr></table>\\n</doc>' WHERE `DocTable`='countryenvironmentdatabase' and`DocField`='ConnectionPoolName' and`DocValue`='' and`Lang`='en';");
        SQLInstruction.add(SQLS.toString());

        // New Control model with conditionOper and ConditionVal1.
        //-- ------------------------ 968-971
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepactioncontrol` ");
        SQLS.append("ADD COLUMN `ConditionOper` VARCHAR(45) NOT NULL DEFAULT '' AFTER `Sort`,");
        SQLS.append("ADD COLUMN `ConditionVal1` TEXT AFTER `ConditionOper`;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepactioncontrolexecution` ");
        SQLS.append("ADD COLUMN `ConditionOper` VARCHAR(45) AFTER `Sort`,");
        SQLS.append("ADD COLUMN `ConditionVal1` TEXT AFTER `ConditionOper`;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE testcasestepactioncontrol SET ConditionOper = 'always' where ConditionOper=''; ");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) VALUES");
        SQLS.append("  ('CONTROLCONDITIONOPER', 'always', '100', 'Always.', '')");
        SQLS.append(", ('CONTROLCONDITIONOPER', 'ifPropertyExist', '200', 'Only execute if property exist for the execution.', '')");
        SQLS.append(", ('CONTROLCONDITIONOPER', 'never', '9999', 'Never execute the control.', '')");
        SQLS.append(", ('INVARIANTPRIVATE', 'CONTROLCONDITIONOPER', '560', '', '');");
        SQLInstruction.add(SQLS.toString());

        // Resize login.
        //-- ------------------------ 972-978
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `usersystem` DROP FOREIGN KEY `FK_usersystem_01`;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `usersystem` CHANGE COLUMN `Login` `Login` VARCHAR(255) NOT NULL ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `usergroup` DROP FOREIGN KEY `FK_usergroup_01`;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `usergroup` CHANGE COLUMN `Login` `Login` VARCHAR(255) NOT NULL ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `user` CHANGE COLUMN `Login` `Login` VARCHAR(255) NOT NULL ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `usersystem` ADD CONSTRAINT `FK_usersystem_01` FOREIGN KEY (`Login`) REFERENCES `user` (`Login`) ON DELETE CASCADE ON UPDATE CASCADE;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `usergroup` ADD CONSTRAINT `FK_usergroup_01` FOREIGN KEY (`Login`) REFERENCES `cerberus`.`user` (`Login`) ON DELETE CASCADE ON UPDATE CASCADE;");
        SQLInstruction.add(SQLS.toString());

        // Add path to picture for appliation object in paramaters
        //-- ------------------------ 979
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `parameter` VALUES ");
        SQLS.append("('','cerberus_featureflipping_activatewebsocketpush','Y','Boolean that enable/disable the websocket push.');");
        SQLInstruction.add(SQLS.toString());

        // New Step model with conditionOper and ConditionVal1.
        //-- ------------------------ 980-983
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestep` ");
        SQLS.append("ADD COLUMN `ConditionOper` VARCHAR(45) NOT NULL DEFAULT '' AFTER `Sort`,");
        SQLS.append("ADD COLUMN `ConditionVal1` TEXT AFTER `ConditionOper`;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepexecution` ");
        SQLS.append("ADD COLUMN `ConditionOper` VARCHAR(45) AFTER `Sort`,");
        SQLS.append("ADD COLUMN `ConditionVal1` TEXT AFTER `ConditionOper`;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE testcasestep SET ConditionOper = 'always' where ConditionOper=''; ");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) VALUES");
        SQLS.append("  ('STEPCONDITIONOPER', 'always', '100', 'Always.', '')");
        SQLS.append(", ('STEPCONDITIONOPER', 'ifPropertyExist', '200', 'Only execute if property exist for the execution.', '')");
        SQLS.append(", ('STEPCONDITIONOPER', 'never', '9999', 'Never execute the control.', '')");
        SQLS.append(", ('INVARIANTPRIVATE', 'STEPCONDITIONOPER', '570', '', '');");
        SQLInstruction.add(SQLS.toString());

        // New testcase model with conditionOper and ConditionVal1.
        //-- ------------------------ 984-990
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcase` ");
        SQLS.append("ADD COLUMN `ConditionOper` VARCHAR(45) NOT NULL DEFAULT '' AFTER `TcActive`,");
        SQLS.append("ADD COLUMN `ConditionVal1` TEXT AFTER `ConditionOper`;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE testcase SET ConditionOper = 'always' where ConditionOper=''; ");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) VALUES");
        SQLS.append("  ('TESTCASECONDITIONOPER', 'always', '100', 'Always.', '')");
        SQLS.append(", ('TESTCASECONDITIONOPER', 'ifPropertyExist', '200', 'Only execute if property exist for the execution.', '')");
        SQLS.append(", ('TESTCASECONDITIONOPER', 'never', '9999', 'Never execute the control.', '')");
        SQLS.append(", ('INVARIANTPRIVATE', 'TESTCASECONDITIONOPER', '580', '', '');");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE testcase SET ConditionVal1 = '' where ConditionVal1 is null; ");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE testcasestep SET ConditionVal1 = '' where ConditionVal1 is null; ");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE testcasestepexecution SET ConditionVal1 = '' where ConditionVal1 is null; ");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE testcasestepactioncontrol SET ConditionVal1 = '' where ConditionVal1 is null; ");
        SQLInstruction.add(SQLS.toString());

        // Removed skipAction and skipControl and replaced by conditionOper = never.
        //-- ------------------------ 991-994
        SQLS = new StringBuilder();
        SQLS.append("DELETE from invariant where idname = 'ACTION' and value = 'skipAction';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("DELETE from invariant where idname = 'CONTROL' and value = 'skipControl';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE testcasestepaction Set ConditionOper = 'never', Action = 'Unknown' where Action = 'skipAction';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE testcasestepactioncontrol Set ConditionOper = 'never', Control = 'Unknown' where Control = 'skipControl';");
        SQLInstruction.add(SQLS.toString());

        // New Appium capabtilities for IOS testing
        // 995-997
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) SELECT * from (SELECT 'CAPABILITY', 'udid', '8', 'Unique Device IDentifier (useful for IOS testing)', '') AS tmp WHERE NOT EXISTS ( SELECT `value` FROM `invariant` WHERE idname='CAPABILITY' and `value`='udid') LIMIT 1;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) SELECT * from (SELECT 'CAPABILITY', 'xcodeConfigFile', '9', 'Path to the Xcode Configuration File containing information about application sign (useful for IOS testing)', '') AS tmp WHERE NOT EXISTS ( SELECT `value` FROM `invariant` WHERE idname='CAPABILITY' and `value`='xcodeConfigFile') LIMIT 1;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) SELECT * from (SELECT 'CAPABILITY', 'realDeviceLogger', '10', 'Path to the logger for real IOS devices (useful for IOS testing)', '') AS tmp WHERE NOT EXISTS ( SELECT `value` FROM `invariant` WHERE idname='CAPABILITY' and  `value`='realDeviceLogger') LIMIT 1;");
        SQLInstruction.add(SQLS.toString());

        // Updated Documentation
        //-- ------------------------ 998
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `documentation` VALUES ");
        SQLS.append("('page_executiondetail','title','','en','Execution Detail','')");
        SQLS.append(",('page_executiondetail','title','','fr','Detail de l Execution','')");
        SQLS.append(",('page_global','beta_message','','en','This page is in beta, some features may not be available or fully functional.','')");
        SQLS.append(",('page_global','beta_message','','fr','Cette page est en beta, certaines fonctionnalités peuvent être indisponnible ou non complètes.','')");
        SQLS.append(",('page_global','old_page','','en','Old Page','')");
        SQLS.append(",('page_global','old_page','','fr','Ancienne Page','')");
        SQLS.append(",('page_executiondetail','see_execution_tag','','en','See Execution By Tag','')");
        SQLS.append(",('page_executiondetail','see_execution_tag','','fr','Voir l execution par Tag','')");
        SQLS.append(",('page_executiondetail','more_detail','','en','More details','')");
        SQLS.append(",('page_executiondetail','more_detail','','fr','Plus de détails','')");
        SQLS.append(",('page_executiondetail','application','','en','Application','')");
        SQLS.append(",('page_executiondetail','application','','fr','Application','')");
        SQLS.append(",('page_executiondetail','browser','','en','Browser','')");
        SQLS.append(",('page_executiondetail','browser','','fr','Navigateur','')");
        SQLS.append(",('page_executiondetail','browserfull','','en','Browser full version','')");
        SQLS.append(",('page_executiondetail','browserfull','','fr','Navigateur version complete','')");
        SQLS.append(",('page_executiondetail','country','','en','Country','')");
        SQLS.append(",('page_executiondetail','country','','fr','Pays','')");
        SQLS.append(",('page_executiondetail','environment','','en','Environment','')");
        SQLS.append(",('page_executiondetail','environment','','fr','Environement','')");
        SQLS.append(",('page_executiondetail','status','','en','Status','')");
        SQLS.append(",('page_executiondetail','status','','fr','Status','')");
        SQLS.append(",('page_executiondetail','controlstatus','','en','Control Status','')");
        SQLS.append(",('page_executiondetail','controlstatus','','fr','Status du control','')");
        SQLS.append(",('page_executiondetail','controlmessage','','en','Control Message','')");
        SQLS.append(",('page_executiondetail','controlmessage','','fr','Message du control','')");
        SQLS.append(",('page_executiondetail','ip','','en','IP','')");
        SQLS.append(",('page_executiondetail','ip','','fr','IP','')");
        SQLS.append(",('page_executiondetail','port','','en','Port','')");
        SQLS.append(",('page_executiondetail','port','','fr','Port','')");
        SQLS.append(",('page_executiondetail','platform','','en','Platform','')");
        SQLS.append(",('page_executiondetail','platform','','fr','Platforme','')");
        SQLS.append(",('page_executiondetail','cerberusversion','','en','Cerberus Version','')");
        SQLS.append(",('page_executiondetail','cerberusversion','','fr','Version de Cerberus','')");
        SQLS.append(",('page_executiondetail','executor','','en','Executor','')");
        SQLS.append(",('page_executiondetail','executor','','fr','Executeur','')");
        SQLS.append(",('page_executiondetail','url','','en','URL','')");
        SQLS.append(",('page_executiondetail','url','','fr','URL','')");
        SQLS.append(",('page_executiondetail','start','','en','Start','')");
        SQLS.append(",('page_executiondetail','start','','fr','Début','')");
        SQLS.append(",('page_executiondetail','end','','en','End','')");
        SQLS.append(",('page_executiondetail','end','','fr','Fin','')");
        SQLS.append(",('page_executiondetail','finished','','en','Finished','')");
        SQLS.append(",('page_executiondetail','finished','','fr','Fini','')");
        SQLS.append(",('page_executiondetail','id','','en','ID','')");
        SQLS.append(",('page_executiondetail','id','','fr','ID','')");
        SQLS.append(",('page_executiondetail','revision','','en','Revision','')");
        SQLS.append(",('page_executiondetail','revision','','fr','Revision','')");
        SQLS.append(",('page_executiondetail','screensize','','en','Screen Size','')");
        SQLS.append(",('page_executiondetail','screensize','','fr','Taille de l ecran','')");
        SQLS.append(",('page_executiondetail','tag','','en','Tag','')");
        SQLS.append(",('page_executiondetail','tag','','fr','Tag','')");
        SQLS.append(",('page_executiondetail','verbose','','en','Verbose','')");
        SQLS.append(",('page_executiondetail','verbose','','fr','Loquacité','')");
        SQLS.append(",('page_executiondetail','build','','en','Build','')");
        SQLS.append(",('page_executiondetail','build','','fr','Build','')");
        SQLS.append(",('page_executiondetail','version','','en','Version','')");
        SQLS.append(",('page_executiondetail','version','','fr','Version','')");
        SQLS.append(",('page_executiondetail','steps','','en','Steps','')");
        SQLS.append(",('page_executiondetail','steps','','fr','Etapes','')");
        SQLS.append(",('page_executiondetail','edittc','','en','Edit Test Case','')");
        SQLS.append(",('page_executiondetail','edittc','','fr','Modifier le Cas de Test','')");
        SQLS.append(",('page_executiondetail','runtc','','en','Run this Test Case again','')");
        SQLS.append(",('page_executiondetail','runtc','','fr','Executer ce Cas de Test encore','')");
        SQLS.append(",('page_executiondetail','lastexecution','','en','Last Executions','')");
        SQLS.append(",('page_executiondetail','lastexecution','','fr','Dernières exécutions','')");
        SQLS.append(",('page_executiondetail','action','','en','Action','')");
        SQLS.append(",('page_executiondetail','action','','fr','Action','')");
        SQLS.append(",('page_executiondetail','description','','en','Description','')");
        SQLS.append(",('page_executiondetail','description','','fr','Description','')");
        SQLS.append(",('page_executiondetail','value1','','en','Value 1','')");
        SQLS.append(",('page_executiondetail','value1','','fr','Valeur 1','')");
        SQLS.append(",('page_executiondetail','time','','en','Time','')");
        SQLS.append(",('page_executiondetail','time','','fr','Temps','')");
        SQLS.append(",('page_executiondetail','value2','','en','Value 2','')");
        SQLS.append(",('page_executiondetail','value2','','fr','Valeur 2','')");
        SQLS.append(",('page_executiondetail','return_code','','en','Return Code','')");
        SQLS.append(",('page_executiondetail','return_code','','fr','Code de retour','')");
        SQLS.append(",('page_executiondetail','return_message','','en','Return Message','')");
        SQLS.append(",('page_executiondetail','return_message','','fr','Message de retour','')");
        SQLS.append(",('page_executiondetail','sort','','en','Sort','')");
        SQLS.append(",('page_executiondetail','sort','','fr','Ordre','')");
        SQLS.append(",('page_executiondetail','control_type','','en','Control Type','')");
        SQLS.append(",('page_executiondetail','control_type','','fr','Type de Control','')");
        SQLS.append(",('page_executiondetail','fatal','','en','Fatal','')");
        SQLS.append(",('page_executiondetail','fatal','','fr','Fatal','')");
        SQLS.append(",('page_runtest','title','','en','Run Test','')");
        SQLS.append(",('page_runtest','title','','fr','Executer un test','')");
        SQLS.append(",('page_runtest','selection_type','','en','Selection type','')");
        SQLS.append(",('page_runtest','selection_type','','fr','Type de selection','')");
        SQLS.append(",('page_runtest','select_list_test','','en','Select a list of test','')");
        SQLS.append(",('page_runtest','select_list_test','','fr','Sélectionnez une list de test','')");
        SQLS.append(",('page_runtest','select_campaign','','en','Select a campaign','')");
        SQLS.append(",('page_runtest','select_campaign','','fr','Sélectionnez une campagne','')");
        SQLS.append(",('page_runtest','load','','en','Load','')");
        SQLS.append(",('page_runtest','load','','fr','Charger','')");
        SQLS.append(",('page_runtest','choose_test','','en','Choose Test','')");
        SQLS.append(",('page_runtest','choose_test','','fr','Sélectionnez vos test','')");
        SQLS.append(",('page_runtest','filters','','en','Filters','')");
        SQLS.append(",('page_runtest','filters','','fr','Filtres','')");
        SQLS.append(",('page_runtest','test','','en','Test','')");
        SQLS.append(",('page_runtest','test','','fr','Test','')");
        SQLS.append(",('page_runtest','project','','en','Project','')");
        SQLS.append(",('page_runtest','project','','fr','Projet','')");
        SQLS.append(",('page_runtest','application','','en','Application','')");
        SQLS.append(",('page_runtest','application','','fr','Application','')");
        SQLS.append(",('page_runtest','creator','','en','Creator','')");
        SQLS.append(",('page_runtest','creator','','fr','Créateur','')");
        SQLS.append(",('page_runtest','implementer','','en','Implementer','')");
        SQLS.append(",('page_runtest','implementer','','fr','Implementeur','')");
        SQLS.append(",('page_runtest','group','','en','Group','')");
        SQLS.append(",('page_runtest','group','','fr','Groupe','')");
        SQLS.append(",('page_runtest','campaign','','en','Campaign','')");
        SQLS.append(",('page_runtest','campaign','','fr','Campagne','')");
        SQLS.append(",('page_runtest','testbattery','','en','Test Battery','')");
        SQLS.append(",('page_runtest','testbattery','','fr','Batterie de test','')");
        SQLS.append(",('page_runtest','priority','','en','Priority','')");
        SQLS.append(",('page_runtest','priority','','fr','Priorité','')");
        SQLS.append(",('page_runtest','status','','en','Status','')");
        SQLS.append(",('page_runtest','status','','fr','Status','')");
        SQLS.append(",('page_runtest','targetrev','','en','Target Revision','')");
        SQLS.append(",('page_runtest','targetrev','','fr','Révision Cible','')");
        SQLS.append(",('page_runtest','targetsprint','','en','Target Sprint','')");
        SQLS.append(",('page_runtest','targetsprint','','fr','Sprint Cible','')");
        SQLS.append(",('page_runtest','size','','en','Size','')");
        SQLS.append(",('page_runtest','size','','fr','Taille','')");
        SQLS.append(",('page_runtest','select_all','','en','Select All','')");
        SQLS.append(",('page_runtest','select_all','','fr','Tout Sélectionner','')");
        SQLS.append(",('page_runtest','automatic','','en','Automatic','')");
        SQLS.append(",('page_runtest','automatic','','fr','Automatique','')");
        SQLS.append(",('page_runtest','manual','','en','Manual','')");
        SQLS.append(",('page_runtest','manual','','fr','Manuel','')");
        SQLS.append(",('page_runtest','myhost','','en','My host','')");
        SQLS.append(",('page_runtest','myhost','','fr','Mon hôte','')");
        SQLS.append(",('page_runtest','mycontextroot','','en','My Context Root','')");
        SQLS.append(",('page_runtest','mycontextroot','','fr','Ma racine de contexte','')");
        SQLS.append(",('page_runtest','myloginrelativeurl','','en','My login relative url','')");
        SQLS.append(",('page_runtest','myloginrelativeurl','','fr','Mon url de login relative','')");
        SQLS.append(",('page_runtest','myenvdata','','en','My environment data','')");
        SQLS.append(",('page_runtest','myenvdata','','fr','Mes données d environment','')");
        SQLS.append(",('page_runtest','countryList','','en','Country List','')");
        SQLS.append(",('page_runtest','countryList','','fr','Liste de pays','')");
        SQLS.append(",('page_runtest','potential','','en','Potential','')");
        SQLS.append(",('page_runtest','potential','','fr','Potentiel','')");
        SQLS.append(",('page_runtest','addtoqueue','','en','Add to queue','')");
        SQLS.append(",('page_runtest','addtoqueue','','fr','Ajouter à la liste','')");
        SQLS.append(",('page_runtest','addtoqueueandrun','','en','Add to queue and run','')");
        SQLS.append(",('page_runtest','addtoqueueandrun','','fr','Ajouter à la liste et executer','')");
        SQLS.append(",('page_runtest','robot_settings','','en','Robot Settings','')");
        SQLS.append(",('page_runtest','robot_settings','','fr','Paramètre du Robot','')");
        SQLS.append(",('page_runtest','select_robot','','en','Select a robot','')");
        SQLS.append(",('page_runtest','select_robot','','fr','Sélectionnez un robot','')");
        SQLS.append(",('page_runtest','selenium_ip','','en','Selenium Server IP','')");
        SQLS.append(",('page_runtest','selenium_ip','','fr','IP du serveur Selenium','')");
        SQLS.append(",('page_runtest','selenium_port','','en','Selenium Server Port','')");
        SQLS.append(",('page_runtest','selenium_port','','fr','Port du serveur Selenium','')");
        SQLS.append(",('page_runtest','browser','','en','Browser','')");
        SQLS.append(",('page_runtest','browser','','fr','Navigateur','')");
        SQLS.append(",('page_runtest','version','','en','Version','')");
        SQLS.append(",('page_runtest','version','','fr','Version','')");
        SQLS.append(",('page_runtest','platform','','en','Platform','')");
        SQLS.append(",('page_runtest','platform','','fr','Plateforme','')");
        SQLS.append(",('page_runtest','screensize','','en','Screen size','')");
        SQLS.append(",('page_runtest','screensize','','fr','Taille d écran','')");
        SQLS.append(",('page_runtest','saverobotpref','','en','Save Robot Preferencies','')");
        SQLS.append(",('page_runtest','saverobotpref','','fr','Enregistrer les préférences du robot','')");
        SQLS.append(",('page_runtest','execution_settings','','en','Execution Settings','')");
        SQLS.append(",('page_runtest','execution_settings','','fr','Paramètres d execution','')");
        SQLS.append(",('page_runtest','tag','','en','Tag','')");
        SQLS.append(",('page_runtest','tag','','fr','Tag','')");
        SQLS.append(",('page_runtest','outputformat','','en','Output Format','')");
        SQLS.append(",('page_runtest','outputformat','','fr','Format de sortie','')");
        SQLS.append(",('page_runtest','verbose','','en','Verbose','')");
        SQLS.append(",('page_runtest','verbose','','fr','Loquacité','')");
        SQLS.append(",('page_runtest','screenshot','','en','Screenshot','')");
        SQLS.append(",('page_runtest','screenshot','','fr','Screenshot','')");
        SQLS.append(",('page_runtest','pagesource','','en','Page Source','')");
        SQLS.append(",('page_runtest','pagesource','','fr','Page Source','')");
        SQLS.append(",('page_runtest','seleniumlog','','en','Selenium Log','')");
        SQLS.append(",('page_runtest','seleniumlog','','fr','Log de Selenium','')");
        SQLS.append(",('page_runtest','synchroneous','','en','Synchroneous','')");
        SQLS.append(",('page_runtest','synchroneous','','fr','Synchrone','')");
        SQLS.append(",('page_runtest','timeout','','en','Timeout','')");
        SQLS.append(",('page_runtest','timeout','','fr','Temporisation','')");
        SQLS.append(",('page_runtest','retries','','en','Retries','')");
        SQLS.append(",('page_runtest','retries','','fr','Essais','')");
        SQLS.append(",('page_runtest','manual_execution','','en','Manual Execution','')");
        SQLS.append(",('page_runtest','manual_execution','','fr','Execution Manuelle','')");
        SQLS.append(",('page_runtest','save_execution_params','','en','Save Execution Parameters','')");
        SQLS.append(",('page_runtest','save_execution_params','','fr','Sauvegarder les paramètres d execution','')");
        SQLS.append(",('page_runtest','notValid','','en','Some executions couldn t be added to the queue','')");
        SQLS.append(",('page_runtest','notValid','','fr','Des executions n ont pas pu être ajoutées à la liste','')");
        SQLS.append(",('page_runtest','valid','','en','Executions in queue','')");
        SQLS.append(",('page_runtest','valid','','fr','Executions dans la liste','')");
        SQLS.append(",('page_runtest','reset_queue','','en','Reset queue','')");
        SQLS.append(",('page_runtest','reset_queue','','fr','Vider la liste','')");
        SQLS.append(",('page_runtest','queue','','en','Queue','')");
        SQLS.append(",('page_runtest','queue','','fr','Liste','')");
        SQLS.append(",('page_runtest','run','','en','Run','')");
        SQLS.append(",('page_runtest','run','','fr','Executer','')");
        SQLS.append(",('page_runtest','empty_queue','','en','The Execution Queue is empty !','')");
        SQLS.append(",('page_runtest','empty_queue','','fr','La liste d execution est vide !','')");
        SQLS.append(",('page_runtest','more_than_one_execution_requested','','en','More than 1 excution and no Tag specified','')");
        SQLS.append(",('page_runtest','more_than_one_execution_requested','','fr','Plus d une execution séléctionnées et aucun tag n a été spécifié','')");
        SQLS.append(",('page_runtest','select_one_testcase','','en','Select at least one TestCase !','')");
        SQLS.append(",('page_runtest','select_one_testcase','','fr','Sélectionnez au moins un cas de test !','')");
        SQLS.append(",('page_runtest','select_one_env','','en','Select at least one Environment !','')");
        SQLS.append(",('page_runtest','select_one_env','','fr','Sélectionnez au moins un Environment !','')");
        SQLS.append(",('page_runtest','select_one_country','','en','Select at least one Country !','')");
        SQLS.append(",('page_runtest','select_one_country','','fr','Sélectionnez au moins un Pays !','')");
        SQLS.append(",('page_runtest','custom_config','','en','-- Custom Configuration --','')");
        SQLS.append(",('page_runtest','custom_config','','fr','-- Configuration personnalisée --','')");
        SQLS.append(",('page_runtest','default','','en','Default','')");
        SQLS.append(",('page_runtest','default','','fr','Defaut','')");
        SQLS.append(",('page_runtest','default_full_screen','','en','Default - Full Screen','')");
        SQLS.append(",('page_runtest','default_full_screen','','fr','Defaut - Plein Ecran','')");
        SQLS.append(",('page_testcasescript','select_test','','en','Select a test','')");
        SQLS.append(",('page_testcasescript','select_test','','fr','Sélectionnez un test','')");
        SQLS.append(",('page_testcasescript','select_testcase','','en','Select a TestCase','')");
        SQLS.append(",('page_testcasescript','select_testcase','','fr','Sélectionnez un TestCase','')");
        SQLS.append(",('page_testcasescript','testcasescript_title','','en','Test Case Script','')");
        SQLS.append(",('page_testcasescript','testcasescript_title','','fr','Script du Test Case','')");
        SQLS.append(",('page_testcasescript','steps_title','','en','Steps','')");
        SQLS.append(",('page_testcasescript','steps_title','','fr','Etapes','')");
        SQLS.append(",('page_testcasescript','save_script','','en','Save','')");
        SQLS.append(",('page_testcasescript','save_script','','fr','Sauvegarder','')");
        SQLS.append(",('page_testcasescript','edit_testcase','','en','Edit TestCase','')");
        SQLS.append(",('page_testcasescript','edit_testcase','','fr','Modifier le TestCase','')");
        SQLS.append(",('page_testcasescript','run_testcase','','en','Run TestCase','')");
        SQLS.append(",('page_testcasescript','run_testcase','','fr','Executer le TestCase','')");
        SQLS.append(",('page_testcasescript','rerun_testcase','','en','Rerun TestCase','')");
        SQLS.append(",('page_testcasescript','rerun_testcase','','fr','ReExecuter le TestCase','')");
        SQLS.append(",('page_testcasescript','see_lastexec','','en','See last Executions','')");
        SQLS.append(",('page_testcasescript','see_lastexec','','fr','Dernières Exécutions','')");
        SQLS.append(",('page_testcasescript','see_logs','','en','Logs','')");
        SQLS.append(",('page_testcasescript','see_logs','','fr','Logs','')");
        SQLS.append(",('page_testcasescript','run_old','','en','Old page','')");
        SQLS.append(",('page_testcasescript','run_old','','fr','Ancienne page','')");
        SQLS.append(",('page_testcasescript','add_step','','en','Add step','')");
        SQLS.append(",('page_testcasescript','add_step','','fr','Ajouter une Etape','')");
        SQLS.append(",('page_testcasescript','manage_prop','','en','Manage Properties','')");
        SQLS.append(",('page_testcasescript','manage_prop','','fr','Gestion des Propriétés','')");
        SQLS.append(",('page_testcasescript','add_action','','en','Add Action','')");
        SQLS.append(",('page_testcasescript','add_action','','fr','Ajouter une Action','')");
        SQLS.append(",('page_testcasescript','step_condition_operation','','en','Step Condition Operation','')");
        SQLS.append(",('page_testcasescript','step_condition_operation','','fr','Condition d exécution de l étape','')");
        SQLS.append(",('page_testcasescript','step_condition_value1','','en','Step Condition Parameter','')");
        SQLS.append(",('page_testcasescript','step_condition_value1','','fr','Paramètre de condition','')");
        SQLS.append(",('page_testcasescript','feed_propertyname','','en','Feed Property name','')");
        SQLS.append(",('page_testcasescript','feed_propertyname','','fr','Remplissez le nom de la propriété','')");
        SQLS.append(",('page_testcasescript','feed_propertydescription','','en','Feed Property Description','')");
        SQLS.append(",('page_testcasescript','feed_propertydescription','','fr','Remplissez la description','')");
        SQLS.append(",('page_testcasescript','length','','en','Length','')");
        SQLS.append(",('page_testcasescript','length','','fr','Taille','')");
        SQLS.append(",('page_testcasescript','row_limit','','en','Row Limit','')");
        SQLS.append(",('page_testcasescript','row_limit','','fr','Nombre de lignes limite','')");
        SQLS.append(",('page_testcasescript','property_field','','en','Property : ','')");
        SQLS.append(",('page_testcasescript','property_field','','fr','Propriété : ','')");
        SQLS.append(",('page_testcasescript','description_field','','en','Description : ','')");
        SQLS.append(",('page_testcasescript','description_field','','fr','Description : ','')");
        SQLS.append(",('page_testcasescript','type_field','','en','Type : ','')");
        SQLS.append(",('page_testcasescript','type_field','','fr','Type : ','')");
        SQLS.append(",('page_testcasescript','db_field','','en','Database : ','')");
        SQLS.append(",('page_testcasescript','db_field','','fr','Base de donnée : ','')");
        SQLS.append(",('page_testcasescript','value1_field','','en','Value1 : ','')");
        SQLS.append(",('page_testcasescript','value1_field','','fr','Value1 : ','')");
        SQLS.append(",('page_testcasescript','value2_field','','en','Value2 : ','')");
        SQLS.append(",('page_testcasescript','value2_field','','fr','Value2 : ','')");
        SQLS.append(",('page_testcasescript','length_field','','en','Length : ','')");
        SQLS.append(",('page_testcasescript','length_field','','fr','Taille : ','')");
        SQLS.append(",('page_testcasescript','rowlimit_field','','en','Row limit : ','')");
        SQLS.append(",('page_testcasescript','rowlimit_field','','fr','Nombre de lignes limite : ','')");
        SQLS.append(",('page_testcasescript','nature_field','','en','Nature : ','')");
        SQLS.append(",('page_testcasescript','nature_field','','fr','Nature : ','')");
        SQLS.append(",('page_global','warning','','en','Warning','')");
        SQLS.append(",('page_global','warning','','fr','Attention','')");
        SQLS.append(",('page_testcasescript','cant_detach_library','','en','You can t detach this library because it is used in these steps : ','')");
        SQLS.append(",('page_testcasescript','cant_detach_library','','fr','Vous ne pouvez pas détacher cette librairie car elle est utilisée dans ces étapes : ','')");
        SQLS.append(",('page_testcasescript','imported_from','','en','Imported from','')");
        SQLS.append(",('page_testcasescript','imported_from','','fr','Importé depuis','')");
        SQLS.append(",('page_testcasescript','unlink_useStep','','en','Unlink Used step','')");
        SQLS.append(",('page_testcasescript','unlink_useStep','','fr','Délier l étape de sa librairie','')");
        SQLS.append(",('page_testcasescript','unlink_useStep_warning','','en','You are going to unlink this used step. You can t undo this.','')");
        SQLS.append(",('page_testcasescript','unlink_useStep_warning','','fr','Vous aller délier cette étape de sa librairie. Cette action n est pas annulable','')");
        SQLS.append(",('page_testcasescript','describe_action','','en','Describe Action','')");
        SQLS.append(",('page_testcasescript','describe_action','','fr','Décrivez cette action','')");
        SQLS.append(",('page_testcasescript','action_field','','en','Action : ','')");
        SQLS.append(",('page_testcasescript','action_field','','fr','Action : ','')");
        SQLS.append(",('page_testcasescript','condition_operation_field','','en','Condition Operation : ','')");
        SQLS.append(",('page_testcasescript','condition_operation_field','','fr','Condition d execution : ','')");
        SQLS.append(",('page_testcasescript','condition_parameter_field','','en','Condition Parameter : ','')");
        SQLS.append(",('page_testcasescript','condition_parameter_field','','fr','Paramètre de la condition : ','')");
        SQLS.append(",('page_testcasescript','force_execution_field','','en','Force Execution : ','')");
        SQLS.append(",('page_testcasescript','force_execution_field','','fr','Forcer l execution : ','')");
        SQLS.append(",('page_testcasescript','describe_control','','en','Describe Control','')");
        SQLS.append(",('page_testcasescript','describe_control','','fr','Décrivez ce control','')");
        SQLS.append(",('page_testcasescript','control_field','','en','Control : ','')");
        SQLS.append(",('page_testcasescript','control_field','','fr','Control : ','')");
        SQLS.append(",('page_testcasescript','not_application_object','','en','It is not an Application Object','')");
        SQLS.append(",('page_testcasescript','not_application_object','','fr','Ce n est pas un object de l application','')");
        SQLS.append(",('page_testcasescript','not_property','','en','It is not a property','')");
        SQLS.append(",('page_testcasescript','not_property','','fr','Ce n est pas une propriété','')");
        SQLS.append(",('page_testcasescript','warning_no_country','','en','There is no country for at least one parameter. If you save it will be destroyed.','')");
        SQLS.append(",('page_testcasescript','warning_no_country','','fr','Il y a au moins un paramètre sans pays. Si vous sauvegardez il sera supprimé.','');");
        SQLInstruction.add(SQLS.toString());

        // New ConditionVal2 columns
        // 999-1009
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcase` ADD COLUMN `ConditionVal2` TEXT NULL AFTER `ConditionVal1`;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestep` ADD COLUMN `ConditionVal2` TEXT NULL AFTER `ConditionVal1`;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepaction` ADD COLUMN `ConditionVal2` TEXT NULL AFTER `ConditionVal1`;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepactioncontrol` ADD COLUMN `ConditionVal2` TEXT NULL AFTER `ConditionVal1`;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepexecution` ADD COLUMN `ConditionVal2` TEXT NULL AFTER `ConditionVal1`;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepactionexecution` ADD COLUMN `ConditionVal2` TEXT NULL AFTER `ConditionVal1`;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepactioncontrolexecution` ADD COLUMN `ConditionVal2` TEXT NULL AFTER `ConditionVal1`;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `testcase` SET `ConditionVal2` = '';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `testcasestep` SET `ConditionVal2` = '';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `testcasestepaction` SET `ConditionVal2` = '';");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `testcasestepactioncontrol` SET `ConditionVal2` = '';");
        SQLInstruction.add(SQLS.toString());

        //Update doc - 1010
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `documentation` VALUES ");
        SQLS.append("('page_testcasescript','rMessage','','en','Result Message :','')");
        SQLS.append(",('page_testcasescript','rMessage','','fr','Message de Retour :','')");
        SQLS.append(",('page_testcasescript','value1init_field','','en','Value 1 Initial :','')");
        SQLS.append(",('page_testcasescript','value1init_field','','fr','Valeur 1 Initiale :','')");
        SQLS.append(",('page_testcasescript','value2init_field','','en','Value 2 Initial :','')");
        SQLS.append(",('page_testcasescript','value2init_field','','fr','Valeur 2 Initiale :','')");
        SQLS.append(",('page_testcasescript','value_field','','en','Value :','')");
        SQLS.append(",('page_testcasescript','value_field','','fr','Valeur :','')");
        SQLS.append(",('page_testcasescript','rc','','en','Reslut Code :','')");
        SQLS.append(",('page_testcasescript','rc','','fr','Code de Retour :','')");
        SQLS.append(",('page_testcasescript','retrynb','','en','Retry Number :','')");
        SQLS.append(",('page_testcasescript','retrynb','','fr','Nombre d essais :','')");
        SQLS.append(",('page_testcasescript','retryperiod','','en','Retry Period :','')");
        SQLS.append(",('page_testcasescript','retryperiod','','fr','Periode d essai :','')");
        SQLS.append(",('page_testcasescript','index','','en','Index : ','')");
        SQLS.append(",('page_testcasescript','index','','fr','Index : ','');");
        SQLInstruction.add(SQLS.toString());

        //Update doc - 1011
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `documentation` VALUES ");
        SQLS.append("('page_testcasescript','fatal_field','','en','Fatal :','')");
        SQLS.append(",('page_testcasescript','fatal_field','','fr','Fatal :','')");
        SQLS.append(",('page_executiondetail','value1init','','en','Value 1 Initial','')");
        SQLS.append(",('page_executiondetail','value1init','','fr','Valeur 1 Initiale','')");
        SQLS.append(",('page_executiondetail','value2init','','en','Value 2 Initial','')");
        SQLS.append(",('page_executiondetail','value2init','','fr','Valeur 2 Initiale','')");
        SQLS.append(",('page_executiondetail','lastexecutionwithenvcountry','','en','Last Execution with Environment & Country','')");
        SQLS.append(",('page_executiondetail','lastexecutionwithenvcountry','','fr','Dernières Exécutions même Environement & Pays','');");
        SQLInstruction.add(SQLS.toString());

        //Adding new condition at all levels
        // 1012-1015
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` VALUES ");
        SQLS.append("('ACTIONCONDITIONOPER', 'ifNumericEqual', 300, 'Only execute if value1 equals value2.', '', '', '', '')");
        SQLS.append(",('ACTIONCONDITIONOPER', 'ifNumericDifferent', 310, 'Only execute if value1 is different from value2.', '', '', '', '')");
        SQLS.append(",('ACTIONCONDITIONOPER', 'ifNumericGreater', 320, 'Only execute if value1 greater than value2.', '', '', '', '')");
        SQLS.append(",('ACTIONCONDITIONOPER', 'ifNumericGreaterOrEqual', 330, 'Only execute if value1 greater or equal than value2.', '', '', '', '')");
        SQLS.append(",('ACTIONCONDITIONOPER', 'ifNumericMinor', 340, 'Only execute if value1 lower than value2.', '', '', '', '')");
        SQLS.append(",('ACTIONCONDITIONOPER', 'ifNumericMinorOrEqual', 350, 'Only execute if value1 lower or equal than value2.', '', '', '', '')");
        SQLS.append(",('ACTIONCONDITIONOPER', 'ifStringEqual', 400, 'Only execute if value1 equals value2.', '', '', '', '')");
        SQLS.append(",('ACTIONCONDITIONOPER', 'ifStringDifferent', 410, 'Only execute if value1 different from value2.', '', '', '', '')");
        SQLS.append(",('ACTIONCONDITIONOPER', 'ifStringGreater', 420, 'Only execute if value1 greater than value2.', '', '', '', '')");
        SQLS.append(",('ACTIONCONDITIONOPER', 'ifStringMinor', 430, 'Only execute if value1 lower than value2.', '', '', '', '')");
        SQLS.append(",('ACTIONCONDITIONOPER', 'ifStringContains', 440, 'Only execute if value1 contains value2.', '', '', '', '');");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` VALUES ");
        SQLS.append("('STEPCONDITIONOPER', 'ifNumericEqual', 300, 'Only execute if value1 equals value2.', '', '', '', '')");
        SQLS.append(",('STEPCONDITIONOPER', 'ifNumericDifferent', 310, 'Only execute if value1 is different from value2.', '', '', '', '')");
        SQLS.append(",('STEPCONDITIONOPER', 'ifNumericGreater', 320, 'Only execute if value1 greater than value2.', '', '', '', '')");
        SQLS.append(",('STEPCONDITIONOPER', 'ifNumericGreaterOrEqual', 330, 'Only execute if value1 greater or equal than value2.', '', '', '', '')");
        SQLS.append(",('STEPCONDITIONOPER', 'ifNumericMinor', 340, 'Only execute if value1 lower than value2.', '', '', '', '')");
        SQLS.append(",('STEPCONDITIONOPER', 'ifNumericMinorOrEqual', 350, 'Only execute if value1 lower or equal than value2.', '', '', '', '')");
        SQLS.append(",('STEPCONDITIONOPER', 'ifStringEqual', 400, 'Only execute if value1 equals value2.', '', '', '', '')");
        SQLS.append(",('STEPCONDITIONOPER', 'ifStringDifferent', 410, 'Only execute if value1 different from value2.', '', '', '', '')");
        SQLS.append(",('STEPCONDITIONOPER', 'ifStringGreater', 420, 'Only execute if value1 greater than value2.', '', '', '', '')");
        SQLS.append(",('STEPCONDITIONOPER', 'ifStringMinor', 430, 'Only execute if value1 lower than value2.', '', '', '', '')");
        SQLS.append(",('STEPCONDITIONOPER', 'ifStringContains', 440, 'Only execute if value1 contains value2.', '', '', '', '');");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` VALUES ");
        SQLS.append("('CONTROLCONDITIONOPER', 'ifNumericEqual', 300, 'Only execute if value1 equals value2.', '', '', '', '')");
        SQLS.append(",('CONTROLCONDITIONOPER', 'ifNumericDifferent', 310, 'Only execute if value1 is different from value2.', '', '', '', '')");
        SQLS.append(",('CONTROLCONDITIONOPER', 'ifNumericGreater', 320, 'Only execute if value1 greater than value2.', '', '', '', '')");
        SQLS.append(",('CONTROLCONDITIONOPER', 'ifNumericGreaterOrEqual', 330, 'Only execute if value1 greater or equal than value2.', '', '', '', '')");
        SQLS.append(",('CONTROLCONDITIONOPER', 'ifNumericMinor', 340, 'Only execute if value1 lower than value2.', '', '', '', '')");
        SQLS.append(",('CONTROLCONDITIONOPER', 'ifNumericMinorOrEqual', 350, 'Only execute if value1 lower or equal than value2.', '', '', '', '')");
        SQLS.append(",('CONTROLCONDITIONOPER', 'ifStringEqual', 400, 'Only execute if value1 equals value2.', '', '', '', '')");
        SQLS.append(",('CONTROLCONDITIONOPER', 'ifStringDifferent', 410, 'Only execute if value1 different from value2.', '', '', '', '')");
        SQLS.append(",('CONTROLCONDITIONOPER', 'ifStringGreater', 420, 'Only execute if value1 greater than value2.', '', '', '', '')");
        SQLS.append(",('CONTROLCONDITIONOPER', 'ifStringMinor', 430, 'Only execute if value1 lower than value2.', '', '', '', '')");
        SQLS.append(",('CONTROLCONDITIONOPER', 'ifStringContains', 440, 'Only execute if value1 contains value2.', '', '', '', '');");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` VALUES ");
        SQLS.append("('TESTCASECONDITIONOPER', 'ifNumericEqual', 300, 'Only execute if value1 equals value2.', '', '', '', '')");
        SQLS.append(",('TESTCASECONDITIONOPER', 'ifNumericDifferent', 310, 'Only execute if value1 is different from value2.', '', '', '', '')");
        SQLS.append(",('TESTCASECONDITIONOPER', 'ifNumericGreater', 320, 'Only execute if value1 greater than value2.', '', '', '', '')");
        SQLS.append(",('TESTCASECONDITIONOPER', 'ifNumericGreaterOrEqual', 330, 'Only execute if value1 greater or equal than value2.', '', '', '', '')");
        SQLS.append(",('TESTCASECONDITIONOPER', 'ifNumericMinor', 340, 'Only execute if value1 lower than value2.', '', '', '', '')");
        SQLS.append(",('TESTCASECONDITIONOPER', 'ifNumericMinorOrEqual', 350, 'Only execute if value1 lower or equal than value2.', '', '', '', '')");
        SQLS.append(",('TESTCASECONDITIONOPER', 'ifStringEqual', 400, 'Only execute if value1 equals value2.', '', '', '', '')");
        SQLS.append(",('TESTCASECONDITIONOPER', 'ifStringDifferent', 410, 'Only execute if value1 different from value2.', '', '', '', '')");
        SQLS.append(",('TESTCASECONDITIONOPER', 'ifStringGreater', 420, 'Only execute if value1 greater than value2.', '', '', '', '')");
        SQLS.append(",('TESTCASECONDITIONOPER', 'ifStringMinor', 430, 'Only execute if value1 lower than value2.', '', '', '', '')");
        SQLS.append(",('TESTCASECONDITIONOPER', 'ifStringContains', 440, 'Only execute if value1 contains value2.', '', '', '', '');");
        SQLInstruction.add(SQLS.toString());

        //Adding FAT Client Application Type
        // 1016
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` VALUES ");
        SQLS.append("('APPLITYPE', 'FAT', '60', 'FAT client application', '', '', '', '')");
        SQLInstruction.add(SQLS.toString());

        //Adding index column on execution step in order to prepare changes for looping steps
        // 1017-1028
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepactioncontrolexecution` ");
        SQLS.append("DROP FOREIGN KEY `FK_testcasestepactioncontrolexecution_01`;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepactionexecution` ");
        SQLS.append("DROP FOREIGN KEY `FK_testcasestepactionexecution_01`;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepexecution`");
        SQLS.append("ADD COLUMN `index` INT(11) NOT NULL DEFAULT '1' AFTER `Step`,");
        SQLS.append("DROP PRIMARY KEY,");
        SQLS.append("ADD PRIMARY KEY (`ID`, `Test`, `TestCase`, `Step`, `index`) ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepactioncontrolexecution` ");
        SQLS.append("ADD COLUMN `index` INT(11) NOT NULL DEFAULT '1' AFTER `Step`;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepactioncontrolexecution` ADD CONSTRAINT `FK_testcasestepactioncontrolexecution_01` FOREIGN KEY (`ID` , `Test` , `TestCase` , `Step`, `index` ) REFERENCES `testcasestepexecution` (`ID` , `Test` , `TestCase` , `Step` , `index` ) ON DELETE CASCADE ON UPDATE CASCADE ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepactionexecution` ");
        SQLS.append("ADD COLUMN `index` INT(11) NOT NULL DEFAULT '1' AFTER `Step`;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepactionexecution` ADD CONSTRAINT `FK_testcasestepactionexecution_01` FOREIGN KEY (`ID` , `Test` , `TestCase` , `Step` , `index`) REFERENCES `testcasestepexecution` (`ID` , `Test` , `TestCase` , `Step` , `index`) ON DELETE CASCADE ON UPDATE CASCADE;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestep` ");
        SQLS.append("ADD COLUMN `Loop` VARCHAR(45) NOT NULL DEFAULT '' AFTER `Sort`;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepactionexecution` ");
        SQLS.append("DROP PRIMARY KEY,");
        SQLS.append("ADD PRIMARY KEY (`ID`, `Test`, `TestCase`, `Step`, `index`, `Sequence`)  ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcasestepactioncontrolexecution` ");
        SQLS.append("DROP PRIMARY KEY,");
        SQLS.append("ADD PRIMARY KEY (`ID`, `Test`, `TestCase`, `Step`, `index`, `Sequence`, `ControlSequence`) ;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` VALUES ");
        SQLS.append("('STEPLOOP', 'onceIfConditionTrue', 100, 'We execute the step once only if the condiion is true.', '', '', '', '')");
        SQLS.append(",('STEPLOOP', 'onceIfConditionFalse', 200, 'We execute the step once only if the condiion is false.', '', '', '', '')");
        SQLS.append(",('STEPLOOP', 'doWhileConditionTrue', 300, 'We execute the step and then execute it again and again as long as condition is true.', '', '', '', '')");
        SQLS.append(",('STEPLOOP', 'doWhileConditionFalse', 400, 'We execute the step and then execute it again and again as long as condition is false.', '', '', '', '')");
        SQLS.append(",('STEPLOOP', 'whileConditionTrueDo', 500, 'We execute the step as long the condition is true.', '', '', '', '')");
        SQLS.append(",('STEPLOOP', 'whileConditionFalseDo', 600, 'We execute the step as long the condition is false.', '', '', '', '')");
        SQLS.append(",('INVARIANTPRIVATE', 'STEPLOOP', '590', '', '', '', '', '');");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `testcasestep` SET `Loop` = 'onceIfConditionTrue' WHERE `Loop` = '';");
        SQLInstruction.add(SQLS.toString());

        //Adding Screensize to robot table
        // 1029
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `robot` ");
        SQLS.append("ADD COLUMN `screensize` VARCHAR(250) NOT NULL DEFAULT '' AFTER `useragent`;");
        SQLInstruction.add(SQLS.toString());

        //Adding Screensize to robot table
        // 1030
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `documentation` (`DocTable`, `DocField`, `DocValue`, `Lang`, `DocLabel`, `DocDesc`) VALUES ");
        SQLS.append("('robot', 'screensize', '', 'en', 'Screen Size', 'This is the size of the browser screen that will be set for the execution.<br><br>Default Values are set inside the invariant SCREENSIZE that can be configured on Edit Public invariant screen..<br>Value must be two Integer splitted by a <b>*</b> mark.<br><i>For Exemple : 1024*768</i><br><br>If you need to add other Values, please contact your Cerberus Administrator.'),");
        SQLS.append("('robot', 'screensize', '', 'fr', 'Taille d écran', 'Cette valeur correspond à la taille d\\'écran qui sera utilisé lors de l\\'execution.<br><br>Les valeurs sont définies dans la table d\\'invariant et peuvent être complétées si besoin via la page d\\'invariant.<br>Les valeur doivent être deux entiers séparé par une <b>*</b>.<br><i>Par Example : 1024*768</i><br><br>Pour ajouter de nouvelles valeurs, contactez votre administrateur Cerberus.');");
        SQLInstruction.add(SQLS.toString());

        // Parameter in order to limit the number of loop operation allowed in loop operation
        //-- ------------------------ 1031
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `parameter` VALUES ('','cerberus_loopstep_max','20','Integer value that correspond to the max number of step loop authorised.<br>This parameter can be configured at the system level.')");
        SQLInstruction.add(SQLS.toString());

        return SQLInstruction;
    }

}
