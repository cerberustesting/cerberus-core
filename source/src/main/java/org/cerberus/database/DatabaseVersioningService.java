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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.log4j.Level;
import org.cerberus.entity.MyVersion;
import org.cerberus.log.MyLogger;
import org.cerberus.service.IDatabaseVersioningService;
import org.cerberus.service.IMyVersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author vertigo
 */
@Service
public class DatabaseVersioningService implements IDatabaseVersioningService {

    @Autowired
    private IMyVersionService MyversionService;
    @Autowired
    private DatabaseSpring databaseSpring;

    @Override
    public String exeSQL(String SQLString) {
        PreparedStatement preStat;
        Connection connection = this.databaseSpring.connect();
        try {
            preStat = connection.prepareStatement(SQLString);
            try {
                preStat.execute();
                MyLogger.log(DatabaseVersioningService.class.getName(), Level.INFO, SQLString + " Executed successfully.");
            } catch (Exception exception1) {
                MyLogger.log(DatabaseVersioningService.class.getName(), Level.ERROR, exception1.toString());
                return exception1.toString();
            } finally {
                preStat.close();
            }
        } catch (Exception exception1) {
            MyLogger.log(DatabaseVersioningService.class.getName(), Level.ERROR, exception1.toString());
            return exception1.toString();
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(DatabaseVersioningService.class.getName(), Level.WARN, e.toString());
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
        MyLogger.log(DatabaseVersioningService.class.getName(), Level.INFO, "Database needs an upgrade - Script : " + SQLList.size() + " Database : " + MVersion.getValue());
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
        SQLS.append(",('cerberus_picture_path','/home/vertigo/dev/CerberusPictures/','Path to store the Cerberus Selenium Screenshot')");
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
        SQLS.append(",('COUNTRY','VI',19,4,'Generic country used by .com','280',NULL,NULL)");
        SQLS.append(",('COUNTRY','UA',25,4,'Ukrainia','290',NULL,NULL)");
        SQLS.append(",('COUNTRY','DE',40,4,'Germany','600',NULL,NULL)");
        SQLS.append(",('COUNTRY','AT',41,4,'Austria','600',NULL,NULL)");
        SQLS.append(",('COUNTRY','GR',42,4,'Greece','220',NULL,NULL)");
        SQLS.append(",('COUNTRY','RX',50,4,'Transversal Country used for Transversal Applications.','RBX',NULL,NULL)");
        SQLS.append(",('COUNTRY','FR',60,4,'France',NULL,NULL,NULL)");
        SQLS.append(",('ENVIRONMENT','DEV',0,5,'Developpement','DEV',NULL,NULL)");
        SQLS.append(",('ENVIRONMENT','CDIDEV',3,5,'Quality Assurance - Leiria','QAold',NULL,NULL)");
        SQLS.append(",('ENVIRONMENT','CDIQA',4,5,'Quality Assurance - Leiria','QAold',NULL,NULL)");
        SQLS.append(",('ENVIRONMENT','QA',5,5,'Quality Assurance','QA',NULL,NULL)");
        SQLS.append(",('ENVIRONMENT','QA1',6,5,'Quality Assurance - Roubaix 720 (C2)','QA',NULL,NULL)");
        SQLS.append(",('ENVIRONMENT','QA2',7,5,'Quality Assurance - Roubaix 720 (C2)','QA',NULL,NULL)");
        SQLS.append(",('ENVIRONMENT','QA3',8,5,'Quality Assurance - Roubaix 720 (C2)','QA',NULL,NULL)");
        SQLS.append(",('ENVIRONMENT','QA4',13,5,'Quality Assurance - Roubaix 720 (C4)','QA',NULL,NULL)");
        SQLS.append(",('ENVIRONMENT','QA5',14,5,'Quality Assurance - Roubaix 720 (C4)','QA',NULL,NULL)");
        SQLS.append(",('ENVIRONMENT','QA6',23,5,'Quality Assurance - Roubaix 720 (C4)','QA',NULL,NULL)");
        SQLS.append(",('ENVIRONMENT','QA7',24,5,'Quality Assurance - Roubaix 720 (C4)','QAold',NULL,NULL)");
        SQLS.append(",('ENVIRONMENT','UAT',30,5,'User Acceptance Test','UAT',NULL,NULL)");
        SQLS.append(",('ENVIRONMENT','PROD',50,5,'Production','PROD',NULL,NULL)");
        SQLS.append(",('ENVIRONMENT','PREPROD',60,5,'PreProduction','PROD',NULL,NULL)");
        SQLS.append(",('ENVIRONMENT','RQA',71,5,'Quality Assurance - Aubervilliers','QAold',NULL,NULL)");
        SQLS.append(",('ENVIRONMENT','PROTOPROD',72,5,'720 Production Prototype','QA',NULL,NULL)");
        SQLS.append(",('ENVIRONMENT','PROTOUAT',73,5,'720 UAT Prototype','QA',NULL,NULL)");
        SQLS.append(",('ENVIRONMENT','CDI',74,5,'CDI development - Roubaix 720','QA',NULL,NULL)");
        SQLS.append(",('ENVIRONMENT','RQA2',75,5,'Quality Assurance - Roubaix (v5r4)','QAold',NULL,NULL)");
        SQLS.append(",('ENVIRONMENT','UAT2',81,5,'UAT2 environment','UAT',NULL,NULL)");
        SQLS.append(",('ENVIRONMENT','UAT3',82,5,'UAT3 environment','UAT',NULL,NULL)");
        SQLS.append(",('ENVIRONMENT','UAT4',83,5,'UAT4 environment','UAT',NULL,NULL)");
        SQLS.append(",('ENVIRONMENT','UAT5',84,5,'UAT5 environment','UAT',NULL,NULL)");
        SQLS.append(",('ENVIRONMENT','PROD2',90,5,'Production temporarly for new theseus','PROD',NULL,'')");
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
        SQLS.append(",('BUILD','2011B2',9,8,'2011B2',NULL,NULL,NULL)");
        SQLS.append(",('BUILD','2011B3',10,8,'2011B3',NULL,NULL,NULL)");
        SQLS.append(",('BUILD','2012B1',11,8,'2012B1',NULL,NULL,NULL)");
        SQLS.append(",('BUILD','2012S1',12,8,'2012S1',NULL,NULL,NULL)");
        SQLS.append(",('BUILD','2012S2',13,8,'2012 Sprint 02',NULL,NULL,NULL)");
        SQLS.append(",('BUILD','2013S1',14,8,'2013 Sprint 01',NULL,NULL,NULL)");
        SQLS.append(",('REVISION','A00',0,9,'Pre QA Revision',NULL,NULL,NULL)");
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
        SQLS.append(",('REVISION','C12',1120,9,'R12 cancelled',NULL,NULL,NULL)");
        SQLS.append(",('REVISION','C13',1130,9,'R13 Cancelled',NULL,NULL,NULL)");
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
        SQLS.append(",('ORIGIN','VI',11,21,'Generic filiale',NULL,NULL,NULL)");
        SQLS.append(",('ORIGIN','RX',12,21,'Roubaix',NULL,NULL,NULL)");
        SQLS.append(",('ORIGIN','CDI',13,21,'CDITeam',NULL,NULL,NULL)");
        SQLS.append(",('ORIGIN','TIT',14,21,'Test and Integration Team',NULL,NULL,NULL)");
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

        SQLS = new StringBuilder();
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

        SQLS = new StringBuilder();
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
//-- ------------------------
        SQLS = new StringBuilder();
        SQLS.append("UPDATE `user` SET DefaultSystem='DEFAULT' where DefaultSystem is null;");
        SQLInstruction.add(SQLS.toString());

//-- Database structure to handle link between environment and history of Build rev per system for each execution.
//-- ------------------------
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
//-- ------------------------
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

//-- Update System Variables %XXX% to %SYS_XXX%.
//-- ------------------------
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
//-- ------------------------
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`) VALUES ('ACTION', 'takeScreenshot', 105, 12, 'takeScreenshot');");
        SQLInstruction.add(SQLS.toString());

//-- New Parameter for Selenium download link.
//-- ------------------------
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `parameter` (`param`, `value`, `description`) VALUES ('selenium_download_url', 'http://selenium.googlecode.com/files/selenium-server-standalone-2.35.0.jar', 'URL to download the selenium package from the web.');");
        SQLInstruction.add(SQLS.toString());

//-- New Documentation on detail execution page.
//-- ------------------------
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());
        SQLS = new StringBuilder();
        SQLS.append("SELECT 1 FROM dual;");
        SQLInstruction.add(SQLS.toString());

//-- Revision Field resized to 20 to fit standard size in testcase execution table.
//-- ------------------------
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `testcaseexecution` CHANGE COLUMN `Revision` `Revision` VARCHAR(20) NULL DEFAULT NULL ;");
        SQLInstruction.add(SQLS.toString());

//-- Replace \n by <br/> in HowTo textarea of TestCase
        SQLS = new StringBuilder();
        SQLS.append("UPDATE testcase SET HowTo=REPLACE(HowTo, '\\n', '<br/>');");
        SQLInstruction.add(SQLS.toString());

//-- Adding table that will host specific build revision lists per system.
//-- ------------------------
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
//-- ------------------------
        SQLS = new StringBuilder();
        SQLS.append("DELETE FROM `invariant` where id in (8,9);");
        SQLInstruction.add(SQLS.toString());

//-- New Parameter for Selenium timeout when waiting for an element.
//-- ------------------------
        SQLS = new StringBuilder();
        SQLS.append("INSERT INTO `parameter` (`param`, `value`, `description`) VALUES ('selenium_defaultWait', '90', 'Integer that correspond to the number of seconds that selenium will wait before give timeout, when searching for a element.');");
        SQLInstruction.add(SQLS.toString());

//-- Updating documentation.
//-- ------------------------
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
        SQLS.append("('', 'cerberus_notification_accountcreation_defaultPassword', 'Cerberus2014', 'Default Password when creating an account')");
        SQLS.append(",('', 'cerberus_notification_accountcreation_cc', 'Cerberus <no.reply@redoute.pt>', 'Copy List for used for Cerberus creation emailing')");
        SQLS.append(",('', 'cerberus_notification_accountcreation_subject', '[Cerberus] Welcome, your account has been created', 'Subject of account creation emailing')");
        SQLS.append(",('', 'cerberus_notification_accountcreation_body', 'Hello %NAME%<br><br>Your Cerberus account has been created<br><br>To connect Cerberus, please click <a href=\"http://192.168.134.35/Cerberus\">here</a> and use this credential : <br><br>login : %LOGIN%<br>password : %DEFAULT_PASSWORD%<br><br>At your first connection, you will be invited to modify your password<br><br>Enjoy the tool<br><br>','Cerberus creation emailing body')");
        SQLS.append(",('', 'cerberus_notification_accountcreation_from','Cerberus <no.reply@redoute.pt>', 'Cerberus email creation from')");
        SQLS.append(",('', 'cerberus_notification_accountcreation_activateNotification','N', 'Activation boolean for activating to send automatic email on account creation')");
        SQLInstruction.add(SQLS.toString());
        
//Add email column in user table
        SQLS = new StringBuilder();
        SQLS.append("ALTER TABLE `user` ADD COLUMN `Email` VARCHAR(100) NULL AFTER `DefaultSystem`");
        SQLInstruction.add(SQLS.toString());

        return SQLInstruction;
    }
}
