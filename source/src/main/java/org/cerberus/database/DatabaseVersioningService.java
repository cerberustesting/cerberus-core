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
package org.cerberus.database;

import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.crud.entity.MyVersion;
import org.cerberus.crud.service.IMyVersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author vertigo
 */
@Service
public class DatabaseVersioningService implements IDatabaseVersioningService {

    private static final Logger LOG = LogManager.getLogger(DatabaseVersioningService.class);

    @Autowired
    private IMyVersionService MyversionService;
    @Autowired
    private DatabaseSpring databaseSpring;

    @Override
    public String exeSQL(String SQLString) {
        LOG.info("Starting Execution of '" + SQLString + "'");

        try (Connection connection = this.databaseSpring.connect();
                Statement preStat = connection.createStatement();) {
            preStat.execute(SQLString);
            LOG.info("'" + SQLString + "' Executed successfully.");
        } catch (Exception exception1) {
            LOG.error(exception1.toString());
            return exception1.toString();
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
        StringBuilder b;
        // Full script that create the cerberus database.
        ArrayList<String> a;

        // Start to build the SQL Script here.
        a = new ArrayList<>();

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
        b = new StringBuilder();
        b.append("CREATE TABLE `myversion` (");
        b.append(" `Key` varchar(45) NOT NULL DEFAULT '', `Value` int(11) DEFAULT NULL,");
        b.append(" PRIMARY KEY (`Key`)");
        b.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8");
        a.add(b.toString());

        b = new StringBuilder();
        b.append("INSERT INTO `myversion` (`Key`, `Value`) VALUES ('database', 0);");
        a.add(b.toString());

        b = new StringBuilder();
        b.append("CREATE TABLE `log` (");
        b.append("  `id` int(11) NOT NULL AUTO_INCREMENT,");
        b.append("  `datecre` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,");
        b.append("  `desc` varchar(20) DEFAULT NULL,");
        b.append("  `longdesc` varchar(400) DEFAULT NULL,");
        b.append("  `remoteIP` varchar(20) DEFAULT NULL,");
        b.append("  `localIP` varchar(20) DEFAULT NULL,");
        b.append("  PRIMARY KEY (`id`),");
        b.append("  KEY `datecre` (`datecre`)");
        b.append(") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;");
        a.add(b.toString());

        b = new StringBuilder();
        b.append("CREATE TABLE `user` (");
        b.append("  `UserID` int(10) unsigned NOT NULL AUTO_INCREMENT,");
        b.append("  `Login` varchar(10) NOT NULL,");
        b.append("  `Password` char(40) NOT NULL,");
        b.append("  `Name` varchar(25) NOT NULL,");
        b.append("  `Request` varchar(5) DEFAULT NULL,");
        b.append("  `ReportingFavorite` varchar(1000) DEFAULT NULL,");
        b.append("  `DefaultIP` varchar(45) DEFAULT NULL,");
        b.append("  PRIMARY KEY (`UserID`),");
        b.append("  UNIQUE KEY `ID1` (`Login`)");
        b.append(") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;");
        a.add(b.toString());

        b = new StringBuilder();
        b.append("INSERT INTO `user` VALUES (1,'admin','d033e22ae348aeb5660fc2140aec35850c4da997','Admin User','false',NULL,NULL)");
        b.append(",(2,'cerberus','b7e73576cd25a6756dfc25d9eb914ba235d4355d','Cerberus User','false',NULL,NULL);");
        a.add(b.toString());

        b = new StringBuilder();
        b.append("CREATE TABLE `usergroup` (");
        b.append("  `Login` varchar(10) NOT NULL,");
        b.append("  `GroupName` varchar(10) NOT NULL,");
        b.append("  PRIMARY KEY (`Login`,`GroupName`)");
        b.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8;");
        a.add(b.toString());

        b = new StringBuilder();
        b.append("INSERT INTO `usergroup` VALUES ('admin','Admin'),('admin','User'),('admin','Visitor'),('admin','Integrator'),('cerberus','User'),('cerberus','Visitor'),('cerberus','Integrator');");
        a.add(b.toString());

        b = new StringBuilder();
        b.append("CREATE TABLE `documentation` (");
        b.append("  `DocTable` varchar(50) NOT NULL,");
        b.append("  `DocField` varchar(45) NOT NULL,");
        b.append("  `DocValue` varchar(60) NOT NULL DEFAULT '',");
        b.append("  `DocLabel` varchar(60) DEFAULT NULL,");
        b.append("  `DocDesc` varchar(10000) DEFAULT NULL,");
        b.append("  PRIMARY KEY (`DocTable`,`DocField`,`DocValue`) USING BTREE");
        b.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8;");
        a.add(b.toString());

        b = new StringBuilder();
        b.append("CREATE TABLE `parameter` (");
        b.append("  `param` varchar(100) NOT NULL,");
        b.append("  `value` varchar(10000) NOT NULL,");
        b.append("  `description` varchar(5000) NOT NULL,");
        b.append("  PRIMARY KEY (`param`)");
        b.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8;");
        a.add(b.toString());

        b = new StringBuilder();
        b.append("INSERT INTO `parameter` VALUES ('cerberus_homepage_nbbuildhistorydetail','5','Define the number of build/revision that are displayed in the homepage.')");
        b.append(",('cerberus_picture_path','/opt/cerberus-screenshots/','Path to store the Cerberus Selenium Screenshot')");
        b.append(",('cerberus_picture_url','http://localhost/CerberusPictures/','Link to the Cerberus Selenium Screenshot. The following variable can be used : %ID% and %SCREENSHOT%')");
        b.append(",('cerberus_reporting_url','http://IP/Cerberus/ReportingExecution.jsp?Application=%appli%&TcActive=Y&Priority=All&Environment=%env%&Build=%build%&Revision=%rev%&Country=%country%&Status=WORKING&Apply=Apply','URL to Cerberus reporting screen. the following variables can be used : %country%, %env%,  %appli%, %build% and %rev%.')");
        b.append(",('cerberus_selenium_plugins_path','/tmp/','Path to load firefox plugins (Firebug + netExport) to do network traffic')");
        b.append(",('cerberus_support_email','<a href=\"mailto:support@domain.com?Subject=Cerberus%20Account\" style=\"color: yellow\">Support</a>','Contact Email in order to ask for new user in Cerberus tool.')");
        b.append(",('cerberus_testexecutiondetailpage_nbmaxexe','100','Default maximum number of testcase execution displayed in testcase execution detail page.')");
        b.append(",('cerberus_testexecutiondetailpage_nbmaxexe_max','5000','Maximum number of testcase execution displayed in testcase execution detail page.')");
        b.append(",('CI_OK_prio1','1','Coef in order to calculate the OK/KO result for CI platform.')");
        b.append(",('CI_OK_prio2','0.5','Coef in order to calculate the OK/KO result for CI platform.')");
        b.append(",('CI_OK_prio3','0.2','Coef in order to calculate the OK/KO result for CI platform.')");
        b.append(",('CI_OK_prio4','0.1','Coef in order to calculate the OK/KO result for CI platform.')");
        b.append(",('CI_OK_prio5','0','Coef in order to calculate the OK/KO result for CI platform.')");
        b.append(",('index_alert_body','','Body for alerts')");
        b.append(",('index_alert_from','QUALITY Team <team@mail.com>','From team for alerts')");
        b.append(",('index_alert_subject','[BAM] Alert detected for %COUNTRY%','Subject for alerts')");
        b.append(",('index_alert_to','QUALITY Team <team@mail.com>','List of contact for alerts')");
        b.append(",('index_notification_body_between','<br><br>','Text to display between the element of the mail')");
        b.append(",('index_notification_body_end','Subscribe / unsubscribe and get more realtime graph <a href=\"http://IP/index/BusinessActivityMonitor.jsp\">here</a>. <font size=\"1\">(Not available on Internet)</font><br><br>If you have any question, please contact us at <a href=\"mailto:mail@mail.com\">mail@mail.com</a><br>Cumprimentos / Regards / Cordialement,<br>Test and Integration Team</body></html>','Test to display at the end')");
        b.append(",('index_notification_body_top','<html><body>Hello<br><br>Following is the activity monitored for %COUNTRY%, on the %DATEDEB%.<br><br>','Text to display at the top of the mail')");
        b.append(",('index_notification_subject','[BAM] Business Activity Monitor for %COUNTRY%','subject')");
        b.append(",('index_smtp_from','Team <team@mail.com>','smtp from used for notification')");
        b.append(",('index_smtp_host','smtp.mail.com','Smtp host used with notification')");
        b.append(",('index_smtp_port','25','smtp port used for notification ')");
        b.append(",('integration_notification_disableenvironment_body','Hello to all.<br><br>Use of environment %ENV% for country %COUNTRY% with Sprint %BUILD% (Revision %REVISION%) has been disabled, either to cancel the environment or to start deploying a new Sprint/revision.<br>Please don\\'t use the applications until you receive further notification.<br><br>If you have any question, please contact us at mail@mail.com<br><br>Cumprimentos / Regards / Cordialement,<br><br>Test and Integration Team','Default Mail Body on event disableenvironment.')");
        b.append(",('integration_notification_disableenvironment_cc','Team <team@mail.com>','Default Mail cc on event disableenvironment.')");
        b.append(",('integration_notification_disableenvironment_subject','[TIT] Env %ENV% for %COUNTRY% (with Sprint %BUILD% revision %REVISION%) has been disabled for Maintenance.','Default Mail Subject on event disableenvironment.')");
        b.append(",('integration_notification_disableenvironment_to','Team <team@mail.com>','Default Mail to on event disableenvironment.')");
        b.append(",('integration_notification_newbuildrevision_body','Hello to all.<br><br>Sprint %BUILD% with Revisions %REVISION% is now available in %ENV%.<br>To access the corresponding application use the link:<br><a href=\"http://IP/index/?active=Y&env=%ENV%&country=%COUNTRY%\">http://IP/index/?active=Y&env=%ENV%&country=%COUNTRY%</a><br><br>%BUILDCONTENT%<br>%TESTRECAP%<br>%TESTRECAPALL%<br>If you have any problem or question, please contact us at mail@mail.com<br><br>Cumprimentos / Regards / Cordialement,<br><br>Test and Integration Team','Default Mail Body on event newbuildrevision.')");
        b.append(",('integration_notification_newbuildrevision_cc','Team <team@mail.com>','Default Mail cc on event newbuildrevision.')");
        b.append(",('integration_notification_newbuildrevision_subject','[TIT] Sprint %BUILD% Revision %REVISION% is now ready to be used in %ENV% for %COUNTRY%.','Default Mail Subject on event newbuildrevision.')");
        b.append(",('integration_notification_newbuildrevision_to','Team <team@mail.com>','Default Mail to on event newchain.')");
        b.append(",('integration_notification_newchain_body','Hello to all.<br><br>A new Chain %CHAIN% has been executed in %ENV% for your country (%COUNTRY%).<br>Please perform your necessary test following that execution.<br><br>If you have any question, please contact us at mail@mail.com<br><br>Cumprimentos / Regards / Cordialement.','Default Mail Body on event newchain.')");
        b.append(",('integration_notification_newchain_cc','Team <team@mail.com>','Default Mail cc on event newchain.')");
        b.append(",('integration_notification_newchain_subject','[TIT] A New treatment %CHAIN% has been executed in %ENV% for %COUNTRY%.','Default Mail Subject on event newchain.')");
        b.append(",('integration_notification_newchain_to','Team <team@mail.com>','Default Mail to on event newchain.')");
        b.append(",('integration_smtp_from','Team <team@mail.com>','smtp from used for notification')");
        b.append(",('integration_smtp_host','mail.com','Smtp host used with notification')");
        b.append(",('integration_smtp_port','25','smtp port used for notification ')");
        b.append(",('jenkins_admin_password','toto','Jenkins Admin Password')");
        b.append(",('jenkins_admin_user','admin','Jenkins Admin Username')");
        b.append(",('jenkins_application_pipeline_url','http://IP:8210/view/Deploy/','Jenkins Application Pipeline URL. %APPLI% can be used to replace Application name.')");
        b.append(",('jenkins_deploy_pipeline_url','http://IP:8210/view/Deploy/','Jenkins Standard deploy Pipeline URL. ')");
        b.append(",('jenkins_deploy_url','http://IP:8210/job/STD-DEPLOY/buildWithParameters?token=buildit&DEPLOY_JOBNAME=%APPLI%&DEPLOY_BUILD=%JENKINSBUILDID%&DEPLOY_TYPE=%DEPLOYTYPE%&DEPLOY_ENV=%JENKINSAGENT%&SVN_REVISION=%RELEASE%','Link to Jenkins in order to trigger a standard deploy. %APPLI% %JENKINSBUILDID% %DEPLOYTYPE% %JENKINSAGENT% and %RELEASE% can be used.')");
        b.append(",('ticketing tool_bugtracking_url','http://IP/bugtracking/Lists/Bug%20Tracking/DispForm.aspx?ID=%bugid%&Source=http%3A%2F%2Fsitd_moss%2Fbugtracking%2FLists%2FBug%2520Tracking%2FAllOpenBugs.aspx','URL to SitdMoss Bug reporting screen. the following variable can be used : %bugid%.')");
        b.append(",('ticketing tool_newbugtracking_url','http://IP/bugtracking/Lists/Bug%20Tracking/NewForm.aspx?RootFolder=%2Fbugtracking%2FLists%2FBug%20Tracking&Source=http%3A%2F%2Fsitd_moss%2Fbugtracking%2FLists%2FBug%2520Tracking%2FAllOpenBugs.aspx','URL to SitdMoss Bug creation page.')");
        b.append(",('ticketing tool_ticketservice_url','http://IP/tickets/Lists/Tickets/DispForm.aspx?ID=%ticketid%','URL to SitdMoss Ticket Service page.')");
        b.append(",('sonar_application_dashboard_url','http://IP:8211/sonar/project/index/com.appli:%APPLI%','Sonar Application Dashboard URL. %APPLI% and %MAVENGROUPID% can be used to replace Application name.')");
        b.append(",('svn_application_url','http://IP/svn/SITD/%APPLI%','Link to SVN Repository. %APPLI% %TYPE% and %SYSTEM% can be used to replace Application name, type or system.');");
        a.add(b.toString());

        b = new StringBuilder();
        b.append("CREATE TABLE `invariant` (");
        b.append("  `idname` varchar(50) NOT NULL,");
        b.append("  `value` varchar(50) NOT NULL,");
        b.append("  `sort` int(10) unsigned NOT NULL,");
        b.append("  `id` int(10) unsigned NOT NULL,");
        b.append("  `description` varchar(100) NOT NULL,");
        b.append("  `gp1` varchar(45) DEFAULT NULL,");
        b.append("  `gp2` varchar(45) DEFAULT NULL,");
        b.append("  `gp3` varchar(45) DEFAULT NULL,");
        b.append("  PRIMARY KEY (`id`,`sort`) USING BTREE");
        b.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8;");
        a.add(b.toString());

        b = new StringBuilder();
        b.append("INSERT INTO `invariant` VALUES ('STATUS','STANDBY',1,1,'Not implemented yet',NULL,NULL,NULL)");
        b.append(",('STATUS','IN PROGRESS',2,1,'Being implemented',NULL,NULL,NULL)");
        b.append(",('STATUS','TO BE IMPLEMENTED',3,1,'To be implemented',NULL,NULL,NULL)");
        b.append(",('STATUS','TO BE VALIDATED',4,1,'To be validated',NULL,NULL,NULL)");
        b.append(",('STATUS','WORKING',5,1,'Validated and Working',NULL,NULL,NULL)");
        b.append(",('STATUS','TO BE DELETED',6,1,'Should be deleted',NULL,NULL,NULL)");
        b.append(",('GROUP','COMPARATIVE',1,2,'Group of comparison tests',NULL,NULL,NULL)");
        b.append(",('GROUP','INTERACTIVE',2,2,'Group of interactive tests',NULL,NULL,NULL)");
        b.append(",('GROUP','PRIVATE',3,2,'Group of tests which not appear in Cerberus',NULL,NULL,NULL)");
        b.append(",('GROUP','PROCESS',4,2,'Group of tests which need a batch',NULL,NULL,NULL)");
        b.append(",('GROUP','MANUAL',5,2,'Group of test which cannot be automatized',NULL,NULL,NULL)");
        b.append(",('GROUP','',6,2,'Group of tests which are not already defined',NULL,NULL,NULL)");
        b.append(",('COUNTRY','BE',10,4,'Belgium','800',NULL,NULL)");
        b.append(",('COUNTRY','CH',11,4,'Switzerland','500',NULL,NULL)");
        b.append(",('COUNTRY','PT',15,4,'Portugal','200',NULL,NULL)");
        b.append(",('COUNTRY','UK',17,4,'Great Britan','300',NULL,NULL)");
        b.append(",('COUNTRY','DE',40,4,'Germany','600',NULL,NULL)");
        b.append(",('COUNTRY','FR',60,4,'France',NULL,NULL,NULL)");
        b.append(",('ENVIRONMENT','DEV',0,5,'Developpement','DEV',NULL,NULL)");
        b.append(",('ENVIRONMENT','QA',5,5,'Quality Assurance','QA',NULL,NULL)");
        b.append(",('ENVIRONMENT','UAT',30,5,'User Acceptance Test','UAT',NULL,NULL)");
        b.append(",('ENVIRONMENT','PROD',50,5,'Production','PROD',NULL,NULL)");
        b.append(",('ENVIRONMENT','PREPROD',60,5,'PreProduction','PROD',NULL,NULL)");
        b.append(",('SERVER','PRIMARY',1,6,'Primary Server',NULL,NULL,NULL)");
        b.append(",('SERVER','BACKUP1',2,6,'Backup 1',NULL,NULL,NULL)");
        b.append(",('SERVER','BACKUP2',3,6,'Backup 2',NULL,NULL,NULL)");
        b.append(",('SESSION','1',1,7,'Session 1',NULL,NULL,NULL)");
        b.append(",('SESSION','2',2,7,'Session 2',NULL,NULL,NULL)");
        b.append(",('SESSION','3',3,7,'Session 3',NULL,NULL,NULL)");
        b.append(",('SESSION','4',4,7,'Session 4',NULL,NULL,NULL)");
        b.append(",('SESSION','5',5,7,'Session 5',NULL,NULL,NULL)");
        b.append(",('SESSION','6',6,7,'Session 6',NULL,NULL,NULL)");
        b.append(",('SESSION','7',7,7,'Session 7',NULL,NULL,NULL)");
        b.append(",('SESSION','8',8,7,'Session 8',NULL,NULL,NULL)");
        b.append(",('SESSION','9',9,7,'Session 9',NULL,NULL,NULL)");
        b.append(",('SESSION','10',10,7,'Session 10',NULL,NULL,NULL)");
        b.append(",('BUILD','2012S2',13,8,'2012 Sprint 02',NULL,NULL,NULL)");
        b.append(",('BUILD','2013S1',14,8,'2013 Sprint 01',NULL,NULL,NULL)");
        b.append(",('REVISION','R00',1,9,'R00',NULL,NULL,NULL)");
        b.append(",('REVISION','R01',10,9,'R01',NULL,NULL,NULL)");
        b.append(",('REVISION','R02',20,9,'R02',NULL,NULL,NULL)");
        b.append(",('REVISION','R03',30,9,'R03',NULL,NULL,NULL)");
        b.append(",('REVISION','R04',40,9,'R04',NULL,NULL,NULL)");
        b.append(",('REVISION','R05',50,9,'R05',NULL,NULL,NULL)");
        b.append(",('REVISION','R06',60,9,'R06',NULL,NULL,NULL)");
        b.append(",('REVISION','R07',70,9,'R07',NULL,NULL,NULL)");
        b.append(",('REVISION','R08',80,9,'R08',NULL,NULL,NULL)");
        b.append(",('REVISION','R09',90,9,'R09',NULL,NULL,NULL)");
        b.append(",('REVISION','R10',100,9,'R10',NULL,NULL,NULL)");
        b.append(",('REVISION','R11',110,9,'R11',NULL,NULL,NULL)");
        b.append(",('REVISION','R12',120,9,'R12',NULL,NULL,NULL)");
        b.append(",('REVISION','R13',130,9,'R13',NULL,NULL,NULL)");
        b.append(",('REVISION','R14',140,9,'R14',NULL,NULL,NULL)");
        b.append(",('REVISION','R15',150,9,'R15',NULL,NULL,NULL)");
        b.append(",('REVISION','R16',160,9,'R16',NULL,NULL,NULL)");
        b.append(",('REVISION','R17',170,9,'R17',NULL,NULL,NULL)");
        b.append(",('REVISION','R18',180,9,'R18',NULL,NULL,NULL)");
        b.append(",('REVISION','R19',190,9,'R19',NULL,NULL,NULL)");
        b.append(",('REVISION','R20',200,9,'R20',NULL,NULL,NULL)");
        b.append(",('REVISION','R21',210,9,'R21',NULL,NULL,NULL)");
        b.append(",('REVISION','R22',220,9,'R22',NULL,NULL,NULL)");
        b.append(",('REVISION','R23',230,9,'R23',NULL,NULL,NULL)");
        b.append(",('REVISION','R24',240,9,'R24',NULL,NULL,NULL)");
        b.append(",('REVISION','R25',250,9,'R25',NULL,NULL,NULL)");
        b.append(",('REVISION','R26',260,9,'R26',NULL,NULL,NULL)");
        b.append(",('REVISION','R27',270,9,'R27',NULL,NULL,NULL)");
        b.append(",('REVISION','R28',280,9,'R28',NULL,NULL,NULL)");
        b.append(",('REVISION','R29',290,9,'R29',NULL,NULL,NULL)");
        b.append(",('REVISION','R30',300,9,'R30',NULL,NULL,NULL)");
        b.append(",('REVISION','R31',310,9,'R31',NULL,NULL,NULL)");
        b.append(",('REVISION','R32',320,9,'R32',NULL,NULL,NULL)");
        b.append(",('REVISION','R33',330,9,'R33',NULL,NULL,NULL)");
        b.append(",('REVISION','R34',340,9,'R34',NULL,NULL,NULL)");
        b.append(",('REVISION','R35',350,9,'R35',NULL,NULL,NULL)");
        b.append(",('REVISION','R36',360,9,'R36',NULL,NULL,NULL)");
        b.append(",('REVISION','R37',370,9,'R37',NULL,NULL,NULL)");
        b.append(",('REVISION','R38',380,9,'R38',NULL,NULL,NULL)");
        b.append(",('REVISION','R39',390,9,'R39',NULL,NULL,NULL)");
        b.append(",('REVISION','R40',400,9,'R40',NULL,NULL,NULL)");
        b.append(",('REVISION','R41',410,9,'R41',NULL,NULL,NULL)");
        b.append(",('REVISION','R42',420,9,'R42',NULL,NULL,NULL)");
        b.append(",('REVISION','R43',430,9,'R43',NULL,NULL,NULL)");
        b.append(",('REVISION','R44',440,9,'R44',NULL,NULL,NULL)");
        b.append(",('REVISION','R45',450,9,'R45',NULL,NULL,NULL)");
        b.append(",('REVISION','R46',460,9,'R46',NULL,NULL,NULL)");
        b.append(",('REVISION','R47',470,9,'R47',NULL,NULL,NULL)");
        b.append(",('REVISION','R48',480,9,'R48',NULL,NULL,NULL)");
        b.append(",('REVISION','R49',490,9,'R49',NULL,NULL,NULL)");
        b.append(",('REVISION','R50',500,9,'R50',NULL,NULL,NULL)");
        b.append(",('REVISION','R51',510,9,'R51',NULL,NULL,NULL)");
        b.append(",('REVISION','R52',520,9,'R52',NULL,NULL,NULL)");
        b.append(",('REVISION','R53',530,9,'R53',NULL,NULL,NULL)");
        b.append(",('REVISION','R54',540,9,'R54',NULL,NULL,NULL)");
        b.append(",('REVISION','R55',550,9,'R55',NULL,NULL,NULL)");
        b.append(",('REVISION','R56',560,9,'R56',NULL,NULL,NULL)");
        b.append(",('REVISION','R57',570,9,'R57',NULL,NULL,NULL)");
        b.append(",('REVISION','R58',580,9,'R58',NULL,NULL,NULL)");
        b.append(",('REVISION','R59',590,9,'R59',NULL,NULL,NULL)");
        b.append(",('REVISION','R60',600,9,'R60',NULL,NULL,NULL)");
        b.append(",('REVISION','R61',610,9,'R61',NULL,NULL,NULL)");
        b.append(",('REVISION','R62',620,9,'R62',NULL,NULL,NULL)");
        b.append(",('ENVTYPE','STD',1,10,'Regression and evolution Standard Testing.',NULL,NULL,NULL)");
        b.append(",('ENVTYPE','COMPARISON',2,10,'Comparison Testing. No GUI Tests are allowed.',NULL,NULL,NULL)");
        b.append(",('ENVACTIVE','Y',1,11,'Active',NULL,NULL,NULL)");
        b.append(",('ENVACTIVE','N',2,11,'Disable',NULL,NULL,NULL)");
        b.append(",('ACTION','addSelection',10,12,'addSelection',NULL,NULL,NULL)");
        b.append(",('ACTION','calculateProperty',20,12,'calculateProperty',NULL,NULL,NULL)");
        b.append(",('ACTION','click',30,12,'click',NULL,NULL,NULL)");
        b.append(",('ACTION','clickAndWait',40,12,'clickAndWait',NULL,NULL,NULL)");
        b.append(",('ACTION','doubleClick',45,12,'doubleClick',NULL,NULL,NULL)");
        b.append(",('ACTION','enter',50,12,'enter',NULL,NULL,NULL)");
        b.append(",('ACTION','keypress',55,12,'keypress',NULL,NULL,NULL)");
        b.append(",('ACTION','openUrlWithBase',60,12,'openUrlWithBase',NULL,NULL,NULL)");
        b.append(",('ACTION','removeSelection',70,12,'removeSelection',NULL,NULL,NULL)");
        b.append(",('ACTION','select',80,12,'select',NULL,NULL,NULL)");
        b.append(",('ACTION','selectAndWait',90,12,'selectAndWait',NULL,NULL,NULL)");
        b.append(",('ACTION','store',100,12,'store',NULL,NULL,NULL)");
        b.append(",('ACTION','type',110,12,'type',NULL,NULL,NULL)");
        b.append(",('ACTION','URLLOGIN',120,12,'URLLOGIN',NULL,NULL,NULL)");
        b.append(",('ACTION','verifyTextPresent',130,12,'verifyTextPresent',NULL,NULL,NULL)");
        b.append(",('ACTION','verifyTitle',140,12,'verifyTitle',NULL,NULL,NULL)");
        b.append(",('ACTION','verifyValue',150,12,'verifyValue',NULL,NULL,NULL)");
        b.append(",('ACTION','wait',160,12,'wait',NULL,NULL,NULL)");
        b.append(",('ACTION','waitForPage',170,12,'waitForPage',NULL,NULL,NULL)");
        b.append(",('CONTROL','PropertyIsEqualTo',10,13,'PropertyIsEqualTo',NULL,NULL,NULL)");
        b.append(",('CONTROL','PropertyIsGreaterThan',12,13,'PropertyIsGreaterThan',NULL,NULL,NULL)");
        b.append(",('CONTROL','PropertyIsMinorThan',14,13,'PropertyIsMinorThan',NULL,NULL,NULL)");
        b.append(",('CONTROL','verifyElementPresent',20,13,'verifyElementPresent',NULL,NULL,NULL)");
        b.append(",('CONTROL','verifyElementVisible',30,13,'verifyElementVisible',NULL,NULL,NULL)");
        b.append(",('CONTROL','verifyText',40,13,'verifyText',NULL,NULL,NULL)");
        b.append(",('CONTROL','verifyTextPresent',50,13,'verifyTextPresent',NULL,NULL,NULL)");
        b.append(",('CONTROL','verifytitle',60,13,'verifytitle',NULL,NULL,NULL)");
        b.append(",('CONTROL','verifyurl',70,13,'verifyurl',NULL,NULL,NULL)");
        b.append(",('CONTROL','verifyContainText',80,13,'Verify Contain Text',NULL,NULL,NULL)");
        b.append(",('CHAIN','0',1,14,'0',NULL,NULL,NULL)");
        b.append(",('CHAIN','1',2,14,'1',NULL,NULL,NULL)");
        b.append(",('PRIORITY','0',1,15,'No Priority defined',NULL,NULL,NULL)");
        b.append(",('PRIORITY','1',2,15,'Critical Priority',NULL,NULL,NULL)");
        b.append(",('PRIORITY','2',3,15,'High Priority',NULL,NULL,NULL)");
        b.append(",('PRIORITY','3',4,15,'Mid Priority',NULL,NULL,NULL)");
        b.append(",('PRIORITY','4',5,15,'Low Priority',NULL,NULL,NULL)");
        b.append(",('PRIORITY','5',6,15,'Lower Priority or cosmetic',NULL,NULL,NULL)");
        b.append(",('PRIORITY','99',7,15,'99',NULL,NULL,NULL)");
        b.append(",('TCACTIVE','Y',1,16,'Yes',NULL,NULL,NULL)");
        b.append(",('TCACTIVE','N',2,16,'No',NULL,NULL,NULL)");
        b.append(",('TCREADONLY','N',1,17,'No',NULL,NULL,NULL)");
        b.append(",('TCREADONLY','Y',2,17,'Yes',NULL,NULL,NULL)");
        b.append(",('CTRLFATAL','Y',1,18,'Yes',NULL,NULL,NULL)");
        b.append(",('CTRLFATAL','N',2,18,'No',NULL,NULL,NULL)");
        b.append(",('PROPERTYTYPE','SQL',1,19,'SQL Query',NULL,NULL,NULL)");
        b.append(",('PROPERTYTYPE','HTML',2,19,'HTML ID Field',NULL,NULL,NULL)");
        b.append(",('PROPERTYTYPE','TEXT',3,19,'Fix Text value',NULL,NULL,NULL)");
        b.append(",('PROPERTYTYPE','LIB_SQL',4,19,'Using an SQL from the library',NULL,NULL,NULL)");
        b.append(",('PROPERTYNATURE','STATIC',1,20,'Static',NULL,NULL,NULL)");
        b.append(",('PROPERTYNATURE','RANDOM',2,20,'Random',NULL,NULL,NULL)");
        b.append(",('PROPERTYNATURE','RANDOMNEW',3,20,'Random New',NULL,NULL,NULL)");
        b.append(",('ORIGIN','AT',1,21,'Austria',NULL,NULL,NULL)");
        b.append(",('ORIGIN','BE',2,21,'Belgium',NULL,NULL,NULL)");
        b.append(",('ORIGIN','CH',3,21,'Switzerland',NULL,NULL,NULL)");
        b.append(",('ORIGIN','ES',4,21,'Spain',NULL,NULL,NULL)");
        b.append(",('ORIGIN','GR',5,21,'Greece',NULL,NULL,NULL)");
        b.append(",('ORIGIN','IT',6,21,'Italy',NULL,NULL,NULL)");
        b.append(",('ORIGIN','PT',7,21,'Portugal',NULL,NULL,NULL)");
        b.append(",('ORIGIN','RU',8,21,'Russia',NULL,NULL,NULL)");
        b.append(",('ORIGIN','UA',9,21,'Ukrainia',NULL,NULL,NULL)");
        b.append(",('ORIGIN','UK',10,21,'Great Britain',NULL,NULL,NULL)");
        b.append(",('ORIGIN','DE',15,21,'Germany',NULL,NULL,NULL)");
        b.append(",('ORIGIN','FR',16,21,'France',NULL,NULL,NULL)");
        b.append(",('PROPERTYDATABASE','EXAMPLE',1,22,'Example Fake Database',NULL,NULL,NULL)");
        b.append(",('OUTPUTFORMAT','gui',1,24,'GUI HTLM output','','',NULL)");
        b.append(",('OUTPUTFORMAT','compact',2,24,'Compact single line output.',NULL,NULL,NULL)");
        b.append(",('OUTPUTFORMAT','verbose-txt',3,24,'Verbose key=value format.',NULL,NULL,NULL)");
        b.append(",('VERBOSE','0',1,25,'Minimum log','','',NULL)");
        b.append(",('VERBOSE','1',2,25,'Standard log','','',NULL)");
        b.append(",('VERBOSE','2',3,25,'Maximum log',NULL,NULL,NULL)");
        b.append(",('RUNQA','Y',1,26,'Test can run in QA enviroment',NULL,NULL,NULL)");
        b.append(",('RUNQA','N',2,26,'Test cannot run in QA enviroment',NULL,NULL,NULL)");
        b.append(",('RUNUAT','Y',1,27,'Test can run in UAT environment',NULL,NULL,NULL)");
        b.append(",('RUNUAT','N',2,27,'Test cannot run in UAT environment',NULL,NULL,NULL)");
        b.append(",('RUNPROD','N',1,28,'Test cannot run in PROD environment',NULL,NULL,NULL)");
        b.append(",('RUNPROD','Y',2,28,'Test can run in PROD environment',NULL,NULL,NULL)");
        b.append(",('FILTERNBDAYS','14',1,29,'14 Days (2 weeks)',NULL,NULL,NULL)");
        b.append(",('FILTERNBDAYS','30',2,29,'30 Days (1 month)',NULL,NULL,NULL)");
        b.append(",('FILTERNBDAYS','182',3,29,'182 Days (6 months)',NULL,NULL,NULL)");
        b.append(",('FILTERNBDAYS','365',4,29,'365 Days (1 year)',NULL,NULL,NULL)");
        b.append(",('TCESTATUS','OK',1,35,'Test was fully executed and no bug are to be reported.',NULL,NULL,NULL)");
        b.append(",('TCESTATUS','KO',2,35,'Test was executed and bug have been detected.',NULL,NULL,NULL)");
        b.append(",('TCESTATUS','PE',3,35,'Test execution is still running...',NULL,NULL,NULL)");
        b.append(",('TCESTATUS','FA',4,35,'Test could not be executed because there is a bug on the test.',NULL,NULL,NULL)");
        b.append(",('TCESTATUS','NA',5,35,'Test could not be executed because some test data are not available.',NULL,NULL,NULL)");
        b.append(",('MAXEXEC','50',1,36,'50',NULL,NULL,NULL)");
        b.append(",('MAXEXEC','100',2,36,'100',NULL,NULL,NULL)");
        b.append(",('MAXEXEC','200',3,36,'200',NULL,NULL,NULL)");
        b.append(",('MAXEXEC','500',4,36,'500',NULL,NULL,NULL)");
        b.append(",('MAXEXEC','1000',5,36,'1000',NULL,NULL,NULL);");
        a.add(b.toString());

        b = new StringBuilder();
        b.append("CREATE TABLE `tag` (");
        b.append("  `id` int(11) NOT NULL AUTO_INCREMENT,");
        b.append("  `Tag` varchar(145) NOT NULL,");
        b.append("  `TagDateCre` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,");
        b.append("  PRIMARY KEY (`id`),");
        b.append("  UNIQUE KEY `Tag_UNIQUE` (`Tag`)");
        b.append(") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;");
        a.add(b.toString());

        b = new StringBuilder();
        b.append("CREATE TABLE `deploytype` (");
        b.append("  `deploytype` varchar(50) NOT NULL,");
        b.append("  `description` varchar(200) DEFAULT '',");
        b.append("  PRIMARY KEY (`deploytype`)");
        b.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8");
        a.add(b.toString());

        b = new StringBuilder();
        b.append("CREATE TABLE `application` (");
        b.append("  `Application` varchar(45) NOT NULL,");
        b.append("  `description` varchar(200) DEFAULT NULL,");
        b.append("  `internal` varchar(1) NOT NULL COMMENT 'Application',");
        b.append("  `sort` int(11) NOT NULL,");
        b.append("  `type` varchar(10) DEFAULT NULL,");
        b.append("  `system` varchar(45) NOT NULL DEFAULT '',");
        b.append("  `svnurl` varchar(150) DEFAULT NULL,");
        b.append("  `deploytype` varchar(50) DEFAULT NULL,");
        b.append("  `mavengroupid` varchar(50) DEFAULT '',");
        b.append("  PRIMARY KEY (`Application`),");
        b.append("  KEY `FK_application` (`deploytype`),");
        b.append("  CONSTRAINT `FK_application` FOREIGN KEY (`deploytype`) REFERENCES `deploytype` (`deploytype`) ON DELETE CASCADE ON UPDATE CASCADE");
        b.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8;");
        a.add(b.toString());

        b = new StringBuilder();
        b.append("CREATE TABLE `project` (");
        b.append("  `idproject` varchar(45) NOT NULL,");
        b.append("  `VCCode` varchar(20) DEFAULT NULL,");
        b.append("  `Description` varchar(45) DEFAULT NULL,");
        b.append("  `active` varchar(1) DEFAULT 'Y',");
        b.append("  `datecre` timestamp NULL DEFAULT CURRENT_TIMESTAMP,");
        b.append("  PRIMARY KEY (`idproject`)");
        b.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8;");
        a.add(b.toString());

        b = new StringBuilder();
        b.append("CREATE TABLE `batchinvariant` (");
        b.append("  `Batch` varchar(1) NOT NULL DEFAULT '',");
        b.append("  `IncIni` varchar(45) DEFAULT NULL,");
        b.append("  `Unit` varchar(45) DEFAULT NULL,");
        b.append("  `Description` varchar(45) DEFAULT NULL,");
        b.append("  PRIMARY KEY (`Batch`) USING BTREE");
        b.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8;");
        a.add(b.toString());

        b = new StringBuilder();
        b.append("CREATE TABLE `test` (");
        b.append("  `Test` varchar(45) NOT NULL,");
        b.append("  `Description` varchar(300) NOT NULL,");
        b.append("  `Active` varchar(1) NOT NULL,");
        b.append("  `Automated` varchar(1) NOT NULL,");
        b.append("  `TDateCrea` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,");
        b.append("  PRIMARY KEY (`Test`),");
        b.append("  KEY `ix_Test_Active` (`Test`,`Active`)");
        b.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8;");
        a.add(b.toString());

        b = new StringBuilder();
        b.append("INSERT INTO `application` VALUES ('Google','Google Website','N',240,'GUI','DEFAULT','',NULL,'');");
        a.add(b.toString());

        b = new StringBuilder();
        b.append("INSERT INTO `test` VALUES ('Examples','Example Tests','Y','Y','2012-06-19 09:56:06'),('Performance Monitor','Performance Monitor Tests','Y','Y','2012-06-19 09:56:06'),('Business Activity Monitor','Business Activity Monitor Tests','Y','Y','2012-06-19 09:56:06'),('Pre Testing','Preliminary Tests','Y','Y','0000-00-00 00:00:00');");
        a.add(b.toString());

        b = new StringBuilder();
        b.append("CREATE TABLE `testcase` (");
        b.append("  `Test` varchar(45) NOT NULL,");
        b.append("  `TestCase` varchar(45) NOT NULL,");
        b.append("  `Application` varchar(45) DEFAULT NULL,");
        b.append("  `Project` varchar(45) DEFAULT NULL,");
        b.append("  `Ticket` varchar(20) DEFAULT '',");
        b.append("  `Description` varchar(500) NOT NULL,");
        b.append("  `BehaviorOrValueExpected` varchar(2500) NOT NULL,");
        b.append("  `ReadOnly` varchar(1) DEFAULT 'N',");
        b.append("  `ChainNumberNeeded` int(10) unsigned DEFAULT NULL,");
        b.append("  `Priority` int(1) unsigned NOT NULL,");
        b.append("  `Status` varchar(25) NOT NULL,");
        b.append("  `TcActive` varchar(1) NOT NULL,");
        b.append("  `Group` varchar(45) DEFAULT NULL,");
        b.append("  `Origine` varchar(45) DEFAULT NULL,");
        b.append("  `RefOrigine` varchar(45) DEFAULT NULL,");
        b.append("  `HowTo` varchar(2500) DEFAULT NULL,");
        b.append("  `Comment` varchar(500) DEFAULT NULL,");
        b.append("  `TCDateCrea` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,");
        b.append("  `FromBuild` varchar(10) DEFAULT NULL,");
        b.append("  `FromRev` varchar(20) DEFAULT NULL,");
        b.append("  `ToBuild` varchar(10) DEFAULT NULL,");
        b.append("  `ToRev` varchar(20) DEFAULT NULL,");
        b.append("  `BugID` varchar(10) DEFAULT NULL,");
        b.append("  `TargetBuild` varchar(10) DEFAULT NULL,");
        b.append("  `TargetRev` varchar(20) DEFAULT NULL,");
        b.append("  `Creator` varchar(45) DEFAULT NULL,");
        b.append("  `Implementer` varchar(45) DEFAULT NULL,");
        b.append("  `LastModifier` varchar(45) DEFAULT NULL,");
        b.append("  `Sla` varchar(45) DEFAULT NULL,");
        b.append("  `activeQA` varchar(1) DEFAULT 'Y',");
        b.append("  `activeUAT` varchar(1) DEFAULT 'Y',");
        b.append("  `activePROD` varchar(1) DEFAULT 'N',");
        b.append("  PRIMARY KEY (`Test`,`TestCase`),");
        b.append("  KEY `Index_2` (`Group`),");
        b.append("  KEY `Index_3` (`Test`,`TestCase`,`Application`,`TcActive`,`Group`),");
        b.append("  KEY `FK_testcase_2` (`Application`),");
        b.append("  KEY `FK_testcase_3` (`Project`),");
        b.append("  CONSTRAINT `FK_testcase_1` FOREIGN KEY (`Test`) REFERENCES `test` (`Test`) ON DELETE CASCADE ON UPDATE CASCADE,");
        b.append("  CONSTRAINT `FK_testcase_2` FOREIGN KEY (`Application`) REFERENCES `application` (`Application`) ON DELETE CASCADE ON UPDATE CASCADE,");
        b.append("  CONSTRAINT `FK_testcase_3` FOREIGN KEY (`Project`) REFERENCES `project` (`idproject`) ON DELETE CASCADE ON UPDATE CASCADE");
        b.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8;");
        a.add(b.toString());

        b = new StringBuilder();
        b.append("CREATE TABLE `testcasecountry` (");
        b.append("  `Test` varchar(45) NOT NULL,");
        b.append("  `TestCase` varchar(45) NOT NULL,");
        b.append("  `Country` varchar(2) NOT NULL,");
        b.append("  PRIMARY KEY (`Test`,`TestCase`,`Country`),");
        b.append("  CONSTRAINT `FK_testcasecountry_1` FOREIGN KEY (`Test`, `TestCase`) REFERENCES `testcase` (`Test`, `TestCase`) ON DELETE CASCADE ON UPDATE CASCADE");
        b.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8;");
        a.add(b.toString());

        b = new StringBuilder();
        b.append("CREATE TABLE `testcasestep` (");
        b.append("  `Test` varchar(45) NOT NULL,");
        b.append("  `TestCase` varchar(45) NOT NULL,");
        b.append("  `Step` int(10) unsigned NOT NULL,");
        b.append("  `Description` varchar(150) NOT NULL,");
        b.append("  PRIMARY KEY (`Test`,`TestCase`,`Step`),");
        b.append("  CONSTRAINT `FK_testcasestep_1` FOREIGN KEY (`Test`, `TestCase`) REFERENCES `testcase` (`Test`, `TestCase`) ON DELETE CASCADE ON UPDATE CASCADE");
        b.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8;");
        a.add(b.toString());

        b = new StringBuilder();
        b.append("CREATE TABLE `testcasestepbatch` (");
        b.append("  `Test` varchar(45) NOT NULL,");
        b.append("  `TestCase` varchar(45) NOT NULL,");
        b.append("  `Step` varchar(45) NOT NULL,");
        b.append("  `Batch` varchar(1) NOT NULL DEFAULT '',");
        b.append("  PRIMARY KEY (`Test`,`TestCase`,`Step`,`Batch`) USING BTREE,");
        b.append("  KEY `fk_testcasestepbatch_1` (`Batch`),");
        b.append("  CONSTRAINT `FK_testcasestepbatchl_1` FOREIGN KEY (`Test`, `TestCase`) REFERENCES `testcase` (`Test`, `TestCase`) ON DELETE CASCADE ON UPDATE CASCADE,");
        b.append("  CONSTRAINT `FK_testcasestepbatch_2` FOREIGN KEY (`Batch`) REFERENCES `batchinvariant` (`Batch`) ON DELETE CASCADE ON UPDATE CASCADE");
        b.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8;");
        a.add(b.toString());

        b = new StringBuilder();
        b.append("CREATE TABLE `testcasecountryproperties` (");
        b.append("  `Test` varchar(45) NOT NULL,");
        b.append("  `TestCase` varchar(45) NOT NULL,");
        b.append("  `Country` varchar(2) NOT NULL,");
        b.append("  `Property` varchar(150) NOT NULL,");
        b.append("  `Type` varchar(45) NOT NULL,");
        b.append("  `Database` varchar(45) DEFAULT NULL,");
        b.append("  `Value` varchar(2500) NOT NULL,");
        b.append("  `Length` int(10) unsigned NOT NULL,");
        b.append("  `RowLimit` int(10) unsigned NOT NULL,");
        b.append("  `Nature` varchar(45) NOT NULL,");
        b.append("  PRIMARY KEY (`Test`,`TestCase`,`Country`,`Property`) USING BTREE,");
        b.append("  CONSTRAINT `FK_testcasecountryproperties_1` FOREIGN KEY (`Test`, `TestCase`, `Country`) REFERENCES `testcasecountry` (`Test`, `TestCase`, `Country`) ON DELETE CASCADE ON UPDATE CASCADE");
        b.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8;");
        a.add(b.toString());

        b = new StringBuilder();
        b.append("CREATE TABLE `testcasestepaction` (");
        b.append("  `Test` varchar(45) NOT NULL,");
        b.append("  `TestCase` varchar(45) NOT NULL,");
        b.append("  `Step` int(10) unsigned NOT NULL,");
        b.append("  `Sequence` int(10) unsigned NOT NULL,");
        b.append("  `Action` varchar(45) NOT NULL DEFAULT '',");
        b.append("  `Object` varchar(200) NOT NULL DEFAULT '',");
        b.append("  `Property` varchar(45) DEFAULT NULL,");
        b.append("  PRIMARY KEY (`Test`,`TestCase`,`Step`,`Sequence`),");
        b.append("  CONSTRAINT `FK_testcasestepaction_1` FOREIGN KEY (`Test`, `TestCase`, `Step`) REFERENCES `testcasestep` (`Test`, `TestCase`, `Step`) ON DELETE CASCADE ON UPDATE CASCADE");
        b.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8;");
        a.add(b.toString());

        b = new StringBuilder();
        b.append("CREATE TABLE `testcasestepactioncontrol` (");
        b.append("  `Test` varchar(45) NOT NULL,");
        b.append("  `TestCase` varchar(45) NOT NULL,");
        b.append("  `Step` int(10) unsigned NOT NULL,");
        b.append("  `Sequence` int(10) unsigned NOT NULL,");
        b.append("  `Control` int(10) unsigned NOT NULL,");
        b.append("  `Type` varchar(200) NOT NULL DEFAULT '',");
        b.append("  `ControlValue` varchar(200) NOT NULL DEFAULT '',");
        b.append("  `ControlProperty` varchar(200) DEFAULT NULL,");
        b.append("  `Fatal` varchar(1) DEFAULT 'Y',");
        b.append("  PRIMARY KEY (`Test`,`Sequence`,`Step`,`TestCase`,`Control`) USING BTREE,");
        b.append("  KEY `FK_testcasestepcontrol_1` (`Test`,`TestCase`,`Step`,`Sequence`),");
        b.append("  CONSTRAINT `FK_testcasestepcontrol_1` FOREIGN KEY (`Test`, `TestCase`, `Step`, `Sequence`) REFERENCES `testcasestepaction` (`Test`, `TestCase`, `Step`, `Sequence`) ON DELETE CASCADE ON UPDATE CASCADE");
        b.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8;");
        a.add(b.toString());

        b = new StringBuilder();
        b.append("CREATE TABLE `sqllibrary` (");
        b.append("  `Type` varchar(45) NOT NULL,");
        b.append("  `Name` varchar(45) NOT NULL,");
        b.append("  `Script` varchar(2500) NOT NULL,");
        b.append("  `Description` varchar(1000) DEFAULT NULL,");
        b.append("  PRIMARY KEY (`Name`)");
        b.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8;");
        a.add(b.toString());

        b = new StringBuilder();
        b.append("CREATE TABLE `countryenvparam` (");
        b.append("  `Country` varchar(2) NOT NULL,");
        b.append("  `Environment` varchar(45) NOT NULL,");
        b.append("  `Build` varchar(10) DEFAULT NULL,");
        b.append("  `Revision` varchar(20) DEFAULT NULL,");
        b.append("  `Chain` varchar(20) DEFAULT NULL,");
        b.append("  `DistribList` text,");
        b.append("  `EMailBodyRevision` text,");
        b.append("  `Type` varchar(20) DEFAULT NULL,");
        b.append("  `EMailBodyChain` text,");
        b.append("  `EMailBodyDisableEnvironment` text,");
        b.append("  `active` varchar(1) NOT NULL DEFAULT 'N',");
        b.append("  `maintenanceact` varchar(1) DEFAULT 'N',");
        b.append("  `maintenancestr` time DEFAULT NULL,");
        b.append("  `maintenanceend` time DEFAULT NULL,");
        b.append("  PRIMARY KEY (`Country`,`Environment`)");
        b.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8;");
        a.add(b.toString());

        b = new StringBuilder();
        b.append("CREATE TABLE `countryenvironmentparameters` (");
        b.append("  `Country` varchar(2) NOT NULL,");
        b.append("  `Environment` varchar(45) NOT NULL,");
        b.append("  `Application` varchar(45) NOT NULL,");
        b.append("  `IP` varchar(45) NOT NULL,");
        b.append("  `URL` varchar(150) NOT NULL,");
        b.append("  `URLLOGIN` varchar(150) DEFAULT NULL,");
        b.append("  `JdbcUser` varchar(45) DEFAULT NULL,");
        b.append("  `JdbcPass` varchar(45) DEFAULT NULL,");
        b.append("  `JdbcIP` varchar(45) DEFAULT NULL,");
        b.append("  `JdbcPort` int(10) unsigned DEFAULT NULL,");
        b.append("  `as400LIB` varchar(10) DEFAULT NULL,");
        b.append("  PRIMARY KEY (`Country`,`Environment`,`Application`),");
        b.append("  KEY `FK_countryenvironmentparameters_1` (`Country`,`Environment`),");
        b.append("  KEY `FK_countryenvironmentparameters_3` (`Application`),");
        b.append("  CONSTRAINT `FK_countryenvironmentparameters_1` FOREIGN KEY (`Country`, `Environment`) REFERENCES `countryenvparam` (`Country`, `Environment`) ON DELETE CASCADE ON UPDATE CASCADE,");
        b.append("  CONSTRAINT `FK_countryenvironmentparameters_3` FOREIGN KEY (`Application`) REFERENCES `application` (`Application`) ON DELETE CASCADE ON UPDATE CASCADE");
        b.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8;");
        a.add(b.toString());

        b = new StringBuilder();
        b.append("CREATE TABLE `countryenvironmentdatabase` (");
        b.append("  `Database` varchar(45) NOT NULL,");
        b.append("  `Environment` varchar(45) NOT NULL,");
        b.append("  `Country` varchar(2) NOT NULL,");
        b.append("  `ConnectionPoolName` varchar(25) NOT NULL,");
        b.append("  PRIMARY KEY (`Database`,`Environment`,`Country`),");
        b.append("  KEY `FK_countryenvironmentdatabase_1` (`Country`,`Environment`),");
        b.append("  CONSTRAINT `FK_countryenvironmentdatabase_1` FOREIGN KEY (`Country`, `Environment`) REFERENCES `countryenvparam` (`Country`, `Environment`) ON DELETE CASCADE ON UPDATE CASCADE");
        b.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8;");
        a.add(b.toString());

        b = new StringBuilder();
        b.append("CREATE TABLE `host` (");
        b.append("  `Country` varchar(2) NOT NULL,");
        b.append("  `Session` varchar(20) NOT NULL,");
        b.append("  `Environment` varchar(45) NOT NULL,");
        b.append("  `Server` varchar(20) NOT NULL,");
        b.append("  `host` varchar(20) DEFAULT NULL,");
        b.append("  `secure` varchar(1) DEFAULT 'N',");
        b.append("  `port` varchar(20) DEFAULT NULL,");
        b.append("  `active` varchar(1) DEFAULT 'Y',");
        b.append("  PRIMARY KEY (`Country`,`Session`,`Environment`,`Server`) USING BTREE,");
        b.append("  KEY `FK_host_1` (`Country`,`Environment`),");
        b.append("  CONSTRAINT `FK_host_1` FOREIGN KEY (`Country`, `Environment`) REFERENCES `countryenvparam` (`Country`, `Environment`) ON DELETE CASCADE ON UPDATE CASCADE");
        b.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8;");
        a.add(b.toString());

        b = new StringBuilder();
        b.append("CREATE TABLE `countryenvparam_log` (");
        b.append("  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,");
        b.append("  `Country` varchar(2) NOT NULL,");
        b.append("  `Environment` varchar(45) NOT NULL,");
        b.append("  `Build` varchar(10) DEFAULT NULL,");
        b.append("  `Revision` varchar(20) DEFAULT NULL,");
        b.append("  `Chain` int(10) unsigned DEFAULT NULL,");
        b.append("  `Description` varchar(150) DEFAULT NULL,");
        b.append("  `datecre` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,");
        b.append("  PRIMARY KEY (`id`),");
        b.append("  KEY `ID1` (`Country`,`Environment`),");
        b.append("  KEY `FK_countryenvparam_log_1` (`Country`,`Environment`),");
        b.append("  CONSTRAINT `FK_countryenvparam_log_1` FOREIGN KEY (`Country`, `Environment`) REFERENCES `countryenvparam` (`Country`, `Environment`) ON DELETE CASCADE ON UPDATE CASCADE");
        b.append(") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;");
        a.add(b.toString());

        b = new StringBuilder();
        b.append("CREATE TABLE `buildrevisionbatch` (");
        b.append("  `ID` int(11) NOT NULL AUTO_INCREMENT,");
        b.append("  `Batch` varchar(1) NOT NULL,");
        b.append("  `Country` varchar(2) DEFAULT NULL,");
        b.append("  `Build` varchar(45) DEFAULT NULL,");
        b.append("  `Revision` varchar(45) DEFAULT NULL,");
        b.append("  `Environment` varchar(45) DEFAULT NULL,");
        b.append("  `DateBatch` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,");
        b.append("  PRIMARY KEY (`ID`) USING BTREE,");
        b.append("  KEY `FK_buildrevisionbatch_1` (`Batch`),");
        b.append("  KEY `FK_buildrevisionbatch_2` (`Country`,`Environment`),");
        b.append("  CONSTRAINT `FK_buildrevisionbatch_1` FOREIGN KEY (`Batch`) REFERENCES `batchinvariant` (`Batch`) ON DELETE CASCADE ON UPDATE CASCADE,");
        b.append("  CONSTRAINT `FK_buildrevisionbatch_2` FOREIGN KEY (`Country`, `Environment`) REFERENCES `countryenvparam` (`Country`, `Environment`) ON DELETE CASCADE ON UPDATE CASCADE");
        b.append(") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;");
        a.add(b.toString());

        b = new StringBuilder();
        b.append("CREATE TABLE `buildrevisionparameters` (");
        b.append("  `ID` int(11) NOT NULL AUTO_INCREMENT,");
        b.append("  `Build` varchar(10) DEFAULT NULL,");
        b.append("  `Revision` varchar(20) DEFAULT NULL,");
        b.append("  `Release` varchar(40) DEFAULT NULL,");
        b.append("  `Application` varchar(45) DEFAULT NULL,");
        b.append("  `Project` varchar(45) DEFAULT '',");
        b.append("  `TicketIDFixed` varchar(45) DEFAULT '',");
        b.append("  `BugIDFixed` varchar(45) DEFAULT '',");
        b.append("  `Link` varchar(300) DEFAULT '',");
        b.append("  `ReleaseOwner` varchar(100) NOT NULL DEFAULT '',");
        b.append("  `Subject` varchar(1000) DEFAULT '',");
        b.append("  `datecre` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,");
        b.append("  `jenkinsbuildid` varchar(200) DEFAULT '',");
        b.append("  `mavengroupid` varchar(200) DEFAULT '',");
        b.append("  `mavenartifactid` varchar(200) DEFAULT '',");
        b.append("  `mavenversion` varchar(200) DEFAULT '',");
        b.append("  PRIMARY KEY (`ID`),");
        b.append("  KEY `FK1` (`Application`),");
        b.append("  CONSTRAINT `FK1` FOREIGN KEY (`Application`) REFERENCES `application` (`Application`) ON DELETE CASCADE ON UPDATE CASCADE");
        b.append(") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;");
        a.add(b.toString());

        b = new StringBuilder();
        b.append("CREATE TABLE `logevent` (");
        b.append("  `LogEventID` int(10) unsigned NOT NULL AUTO_INCREMENT,");
        b.append("  `UserID` int(10) unsigned NOT NULL,");
        b.append("  `Time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,");
        b.append("  `Page` varchar(25) DEFAULT NULL,");
        b.append("  `Action` varchar(50) DEFAULT NULL,");
        b.append("  `Log` varchar(500) DEFAULT NULL,");
        b.append("  PRIMARY KEY (`LogEventID`)");
        b.append(") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;");
        a.add(b.toString());

        b = new StringBuilder();
        b.append("CREATE TABLE `logeventchange` (");
        b.append("  `LogEventChangeID` int(10) unsigned NOT NULL AUTO_INCREMENT,");
        b.append("  `LogEventID` int(10) unsigned NOT NULL,");
        b.append("  `LogTable` varchar(50) DEFAULT NULL,");
        b.append("  `LogBefore` varchar(5000) DEFAULT NULL,");
        b.append("  `LogAfter` varchar(5000) DEFAULT NULL,");
        b.append("  `datecre` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,");
        b.append("  PRIMARY KEY (`LogEventChangeID`)");
        b.append(") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;");
        a.add(b.toString());

        b = new StringBuilder();
        b.append("CREATE TABLE `testcaseexecution` (");
        b.append("  `ID` bigint(20) unsigned NOT NULL AUTO_INCREMENT,");
        b.append("  `Test` varchar(45) NOT NULL,");
        b.append("  `TestCase` varchar(45) NOT NULL,");
        b.append("  `Build` varchar(10) DEFAULT NULL,");
        b.append("  `Revision` varchar(5) DEFAULT NULL,");
        b.append("  `Environment` varchar(45) DEFAULT NULL,");
        b.append("  `Country` varchar(2) DEFAULT NULL,");
        b.append("  `Browser` varchar(20) DEFAULT NULL,");
        b.append("  `Start` timestamp NULL DEFAULT CURRENT_TIMESTAMP,");
        b.append("  `End` timestamp NULL DEFAULT '0000-00-00 00:00:00',");
        b.append("  `ControlStatus` varchar(2) DEFAULT NULL,");
        b.append("  `Application` varchar(45) DEFAULT NULL,");
        b.append("  `IP` varchar(45) DEFAULT NULL,");
        b.append("  `URL` varchar(150) DEFAULT NULL,");
        b.append("  `Port` varchar(45) DEFAULT NULL,");
        b.append("  `Tag` varchar(50) DEFAULT NULL,");
        b.append("  `Finished` varchar(1) DEFAULT NULL,");
        b.append("  `Verbose` varchar(1) DEFAULT NULL,");
        b.append("  `Status` varchar(25) DEFAULT NULL,");
        b.append("  PRIMARY KEY (`ID`),");
        b.append("  KEY `FK_TestCaseExecution_1` (`Test`,`TestCase`),");
        b.append("  KEY `fk_testcaseexecution_2` (`Tag`),");
        b.append("  KEY `index_1` (`Start`),");
        b.append("  KEY `IX_test_testcase_country` (`Test`,`TestCase`,`Country`,`Start`,`ControlStatus`),");
        b.append("  KEY `index_buildrev` (`Build`,`Revision`),");
        b.append("  KEY `FK_testcaseexecution_3` (`Application`),");
        b.append("  KEY `fk_test` (`Test`),");
        b.append("  KEY `ix_TestcaseExecution` (`Test`,`TestCase`,`Build`,`Revision`,`Environment`,`Country`,`ID`),");
        b.append("  CONSTRAINT `FK_testcaseexecution_1` FOREIGN KEY (`Test`, `TestCase`) REFERENCES `testcase` (`Test`, `TestCase`) ON DELETE CASCADE ON UPDATE CASCADE,");
        b.append("  CONSTRAINT `FK_testcaseexecution_3` FOREIGN KEY (`Application`) REFERENCES `application` (`Application`) ON DELETE CASCADE ON UPDATE CASCADE");
        b.append(") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;");
        a.add(b.toString());

        b = new StringBuilder();
        b.append("CREATE TABLE `testcaseexecutiondata` (");
        b.append("  `ID` bigint(20) unsigned NOT NULL AUTO_INCREMENT,");
        b.append("  `Property` varchar(150) NOT NULL,");
        b.append("  `Value` varchar(150) NOT NULL,");
        b.append("  `Type` varchar(200) DEFAULT NULL,");
        b.append("  `Object` varchar(2500) DEFAULT NULL,");
        b.append("  `RC` varchar(10) DEFAULT NULL,");
        b.append("  `Start` timestamp NULL DEFAULT NULL,");
        b.append("  `End` timestamp NULL DEFAULT NULL,");
        b.append("  `StartLong` bigint(20) DEFAULT NULL,");
        b.append("  `EndLong` bigint(20) DEFAULT NULL,");
        b.append("  PRIMARY KEY (`ID`,`Property`),");
        b.append("  KEY `propertystart` (`Property`,`Start`),");
        b.append("  KEY `index_1` (`Start`),");
        b.append("  CONSTRAINT `FK_TestCaseExecutionData_1` FOREIGN KEY (`ID`) REFERENCES `testcaseexecution` (`ID`) ON DELETE CASCADE");
        b.append(") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;");
        a.add(b.toString());

        b = new StringBuilder();
        b.append("CREATE TABLE `testcaseexecutionwwwdet` (");
        b.append("  `ID` bigint(20) NOT NULL AUTO_INCREMENT,");
        b.append("  `ExecID` bigint(20) unsigned NOT NULL,");
        b.append("  `Start` varchar(45) DEFAULT NULL,");
        b.append("  `url` varchar(500) DEFAULT NULL,");
        b.append("  `End` varchar(45) DEFAULT NULL,");
        b.append("  `ext` varchar(10) DEFAULT NULL,");
        b.append("  `statusCode` int(11) DEFAULT NULL,");
        b.append("  `method` varchar(10) DEFAULT NULL,");
        b.append("  `bytes` int(11) DEFAULT NULL,");
        b.append("  `timeInMillis` int(11) DEFAULT NULL,");
        b.append("  `ReqHeader_Host` varchar(45) DEFAULT NULL,");
        b.append("  `ResHeader_ContentType` varchar(45) DEFAULT NULL,");
        b.append("  `ReqPage` varchar(500) DEFAULT NULL,");
        b.append("  PRIMARY KEY (`ID`),");
        b.append("  KEY `FK_testcaseexecutionwwwdet_1` (`ExecID`),");
        b.append("  CONSTRAINT `FK_testcaseexecutionwwwdet_1` FOREIGN KEY (`ExecID`) REFERENCES `testcaseexecution` (`ID`) ON DELETE CASCADE ON UPDATE CASCADE");
        b.append(") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;");
        a.add(b.toString());

        b = new StringBuilder();
        b.append("CREATE TABLE `testcaseexecutionwwwsum` (");
        b.append("  `ID` bigint(20) unsigned NOT NULL,");
        b.append("  `tot_nbhits` int(11) DEFAULT NULL,");
        b.append("  `tot_tps` int(11) DEFAULT NULL,");
        b.append("  `tot_size` int(11) DEFAULT NULL,");
        b.append("  `nb_rc2xx` int(11) DEFAULT NULL,");
        b.append("  `nb_rc3xx` int(11) DEFAULT NULL,");
        b.append("  `nb_rc4xx` int(11) DEFAULT NULL,");
        b.append("  `nb_rc5xx` int(11) DEFAULT NULL,");
        b.append("  `img_nb` int(11) DEFAULT NULL,");
        b.append("  `img_tps` int(11) DEFAULT NULL,");
        b.append("  `img_size_tot` int(11) DEFAULT NULL,");
        b.append("  `img_size_max` int(11) DEFAULT NULL,");
        b.append("  `js_nb` int(11) DEFAULT NULL,");
        b.append("  `js_tps` int(11) DEFAULT NULL,");
        b.append("  `js_size_tot` int(11) DEFAULT NULL,");
        b.append("  `js_size_max` int(11) DEFAULT NULL,");
        b.append("  `css_nb` int(11) DEFAULT NULL,");
        b.append("  `css_tps` int(11) DEFAULT NULL,");
        b.append("  `css_size_tot` int(11) DEFAULT NULL,");
        b.append("  `css_size_max` int(11) DEFAULT NULL,");
        b.append("  `img_size_max_url` varchar(500) DEFAULT NULL,");
        b.append("  `js_size_max_url` varchar(500) DEFAULT NULL,");
        b.append("  `css_size_max_url` varchar(500) DEFAULT NULL,");
        b.append("  PRIMARY KEY (`ID`),");
        b.append("  KEY `FK_testcaseexecutionwwwsum_1` (`ID`),");
        b.append("  CONSTRAINT `FK_testcaseexecutionwwwsum_1` FOREIGN KEY (`ID`) REFERENCES `testcaseexecution` (`ID`) ON DELETE CASCADE ON UPDATE CASCADE");
        b.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8;");
        a.add(b.toString());

        b = new StringBuilder();
        b.append("CREATE TABLE `testcasestepactionexecution` (");
        b.append("  `ID` bigint(20) NOT NULL,");
        b.append("  `Step` int(10) NOT NULL,");
        b.append("  `Sequence` int(10) NOT NULL,");
        b.append("  `Action` varchar(45) NOT NULL,");
        b.append("  `Object` varchar(200) DEFAULT NULL,");
        b.append("  `Property` varchar(45) DEFAULT NULL,");
        b.append("  `Start` timestamp NULL DEFAULT NULL,");
        b.append("  `End` timestamp NULL DEFAULT NULL,");
        b.append("  `StartLong` bigint(20) DEFAULT NULL,");
        b.append("  `EndLong` bigint(20) DEFAULT NULL,");
        b.append("  PRIMARY KEY (`ID`,`Step`,`Sequence`,`Action`)");
        b.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8;");
        a.add(b.toString());

        b = new StringBuilder();
        b.append("CREATE TABLE `testcasestepexecution` (");
        b.append("  `ID` bigint(20) unsigned NOT NULL,");
        b.append("  `Step` int(10) unsigned NOT NULL,");
        b.append("  `BatNumExe` varchar(45) DEFAULT NULL,");
        b.append("  `Start` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,");
        b.append("  `End` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',");
        b.append("  `FullStart` bigint(20) unsigned DEFAULT NULL,");
        b.append("  `FullEnd` bigint(20) unsigned DEFAULT NULL,");
        b.append("  `TimeElapsed` decimal(10,3) DEFAULT NULL,");
        b.append("  `ReturnCode` varchar(2) DEFAULT NULL,");
        b.append("  PRIMARY KEY (`ID`,`Step`),");
        b.append("  CONSTRAINT `FK_testcasestepexecution_1` FOREIGN KEY (`ID`) REFERENCES `testcaseexecution` (`ID`) ON DELETE CASCADE");
        b.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8;");
        a.add(b.toString());

        b = new StringBuilder();
        b.append("CREATE TABLE `testcasestepactioncontrolexecution` (");
        b.append("  `ID` bigint(20) unsigned NOT NULL,");
        b.append("  `Step` int(10) unsigned NOT NULL,");
        b.append("  `Sequence` int(10) unsigned NOT NULL,");
        b.append("  `Control` int(10) unsigned NOT NULL,");
        b.append("  `ReturnCode` varchar(2) NOT NULL,");
        b.append("  `ControlType` varchar(200) DEFAULT NULL,");
        b.append("  `ControlProperty` varchar(2500) DEFAULT NULL,");
        b.append("  `ControlValue` varchar(200) DEFAULT NULL,");
        b.append("  `Fatal` varchar(1) DEFAULT NULL,");
        b.append("  `Start` timestamp NULL DEFAULT NULL,");
        b.append("  `End` timestamp NULL DEFAULT NULL,");
        b.append("  `StartLong` bigint(20) DEFAULT NULL,");
        b.append("  `EndLong` bigint(20) DEFAULT NULL,");
        b.append("  PRIMARY KEY (`ID`,`Step`,`Sequence`,`Control`) USING BTREE,");
        b.append("  CONSTRAINT `FK_testcasestepcontrolexecution_1` FOREIGN KEY (`ID`, `Step`) REFERENCES `testcasestepexecution` (`ID`, `Step`) ON DELETE CASCADE");
        b.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8;");
        a.add(b.toString());

        b = new StringBuilder();
        b.append("CREATE TABLE `comparisonstatusdata` (");
        b.append("  `idcomparisonstatusdata` int(11) NOT NULL AUTO_INCREMENT,");
        b.append("  `Execution_ID` bigint(20) unsigned DEFAULT NULL,");
        b.append("  `Property` varchar(45) DEFAULT NULL,");
        b.append("  `Property_A` varchar(45) DEFAULT NULL,");
        b.append("  `Property_B` varchar(45) DEFAULT NULL,");
        b.append("  `Property_C` varchar(45) DEFAULT NULL,");
        b.append("  `Status` varchar(45) DEFAULT NULL,");
        b.append("  `Comments` varchar(1000) DEFAULT NULL,");
        b.append("  PRIMARY KEY (`idcomparisonstatusdata`),");
        b.append("  KEY `FK_comparisonstatusdata_1` (`Execution_ID`),");
        b.append("  CONSTRAINT `FK_comparisonstatusdata_1` FOREIGN KEY (`Execution_ID`) REFERENCES `testcaseexecution` (`ID`) ON DELETE CASCADE ON UPDATE CASCADE");
        b.append(") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;");
        a.add(b.toString());

        b = new StringBuilder();
        b.append("CREATE TABLE `comparisonstatus` (");
        b.append("  `idcomparisonstatus` int(11) NOT NULL AUTO_INCREMENT,");
        b.append("  `Execution_ID` bigint(20) unsigned DEFAULT NULL,");
        b.append("  `Country` varchar(2) DEFAULT NULL,");
        b.append("  `Environment` varchar(45) DEFAULT NULL,");
        b.append("  `InvoicingDate` varchar(45) DEFAULT NULL,");
        b.append("  `TestedChain` varchar(45) DEFAULT NULL,");
        b.append("  `Start` varchar(45) DEFAULT NULL,");
        b.append("  `End` varchar(45) DEFAULT NULL,");
        b.append("  PRIMARY KEY (`idcomparisonstatus`),");
        b.append("  KEY `FK_comparisonstatus_1` (`Execution_ID`),");
        b.append("  CONSTRAINT `FK_comparisonstatus_1` FOREIGN KEY (`Execution_ID`) REFERENCES `testcaseexecution` (`ID`) ON DELETE CASCADE ON UPDATE CASCADE");
        b.append(") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;");
        a.add(b.toString());

        b = new StringBuilder();
        b.append("INSERT INTO `project` (`idproject`, `VCCode`, `Description`, `active`) VALUES (' ', ' ', 'None', 'N');");
        a.add(b.toString());

        b = new StringBuilder();
        b.append("INSERT INTO `testcase` VALUES ('Examples','0001A','Google',' ','','Search for Cerberus Website','','Y',NULL,1,'WORKING','Y','INTERACTIVE','FR','','','','2012-06-19 09:56:40','','','','','','','','cerberus','cerberus','cerberus',NULL,'Y','Y','Y')");
        a.add(b.toString());

        b = new StringBuilder();
        b.append("INSERT INTO `testcasecountry` VALUES ('Examples','0001A','FR')");
        a.add(b.toString());

        b = new StringBuilder();
        b.append("INSERT INTO `testcasestep` VALUES ('Examples','0001A',1,'Search')");
        a.add(b.toString());

        b = new StringBuilder();
        b.append("INSERT INTO `testcasecountryproperties` VALUES ('Examples','0001A','FR','MYTEXT','text','','cerberus automated testing',0,0,'STATIC'), ('Examples','0001A','FR','WAIT','text','','5000',0,0,'STATIC')");
        a.add(b.toString());

        b = new StringBuilder();
        b.append("INSERT INTO `testcasestepaction` VALUES ('Examples','0001A',1,10,'openUrlLogin','','')");
        b.append(",('Examples','0001A',1,20,'type','lst-ib','MYTEXT')");
        b.append(",('Examples','0001A',1,30,'click','name=btnK','')");
        a.add(b.toString());

        b = new StringBuilder();
        b.append("INSERT INTO `testcasestepactioncontrol` VALUES ('Examples','0001A',1,30,1,'verifyTextInPage','','Welcome to Cerberus Website','Y')");
        a.add(b.toString());

        a.add(new StringBuilder("SELECT 1 FROM dual;").toString());

        b = new StringBuilder();
        b.append("CREATE TABLE `abonnement` (");
        b.append("  `idabonnement` int(11) NOT NULL AUTO_INCREMENT,");
        b.append("  `email` varchar(45) DEFAULT NULL,");
        b.append("  `notification` varchar(1000) DEFAULT NULL,");
        b.append("  `frequency` varchar(45) DEFAULT NULL,");
        b.append("  `LastNotification` varchar(45) DEFAULT NULL,");
        b.append("  PRIMARY KEY (`idabonnement`)");
        b.append(") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8");
        a.add(b.toString());

        b = new StringBuilder();
        b.append("CREATE TABLE `countryenvdeploytype` (");
        b.append("  `Country` varchar(2) NOT NULL,");
        b.append("  `Environment` varchar(45) NOT NULL,");
        b.append("  `deploytype` varchar(50) NOT NULL,");
        b.append("  `JenkinsAgent` varchar(50) NOT NULL DEFAULT '',");
        b.append("  PRIMARY KEY (`Country`,`Environment`,`deploytype`,`JenkinsAgent`),");
        b.append("  KEY `FK_countryenvdeploytype_1` (`Country`,`Environment`),");
        b.append("  KEY `FK_countryenvdeploytype_2` (`deploytype`),");
        b.append("  CONSTRAINT `FK_countryenvdeploytype_1` FOREIGN KEY (`deploytype`) REFERENCES `deploytype` (`deploytype`) ON DELETE CASCADE ON UPDATE CASCADE,");
        b.append("  CONSTRAINT `FK_countryenvdeploytype_2` FOREIGN KEY (`Country`, `Environment`) REFERENCES `countryenvparam` (`Country`, `Environment`) ON DELETE CASCADE ON UPDATE CASCADE");
        b.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8");
        a.add(b.toString());

        b = new StringBuilder();
        b.append("CREATE TABLE `logglassfish` (");
        b.append("  `idlogglassfish` int(11) NOT NULL AUTO_INCREMENT,");
        b.append("  `TIMESTAMP` varchar(45) DEFAULT 'CURRENT_TIMESTAMP',");
        b.append("  `PARAMETER` varchar(2000) DEFAULT NULL,");
        b.append("  `VALUE` varchar(2000) DEFAULT NULL,");
        b.append("  PRIMARY KEY (`idlogglassfish`)");
        b.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8");
        a.add(b.toString());

        b = new StringBuilder(); // To be removed as not to be used by Cerberus.
        b.append("CREATE TABLE `qualitynonconformities` (");
        b.append("  `idqualitynonconformities` int(11) NOT NULL AUTO_INCREMENT,");
        b.append("  `Country` varchar(45) DEFAULT NULL,");
        b.append("  `Application` varchar(45) DEFAULT NULL,");
        b.append("  `ProblemCategory` varchar(100) DEFAULT NULL,");
        b.append(" `ProblemDescription` varchar(2500) DEFAULT NULL,");
        b.append(" `StartDate` varchar(45) DEFAULT NULL,");
        b.append("  `StartTime` varchar(45) DEFAULT NULL,");
        b.append("  `EndDate` varchar(45) DEFAULT NULL,");
        b.append("  `EndTime` varchar(45) DEFAULT NULL,");
        b.append(" `TeamContacted` varchar(250) DEFAULT NULL,");
        b.append("  `Actions` varchar(2500) DEFAULT NULL,");
        b.append("  `RootCauseCategory` varchar(100) DEFAULT NULL,");
        b.append("  `RootCauseDescription` varchar(2500) DEFAULT NULL,");
        b.append("  `ImpactOrCost` varchar(45) DEFAULT NULL,");
        b.append("  `Responsabilities` varchar(250) DEFAULT NULL,");
        b.append("  `Status` varchar(45) DEFAULT NULL,");
        b.append("  `Comments` varchar(1000) DEFAULT NULL,");
        b.append("  `Severity` varchar(45) DEFAULT NULL,");
        b.append("  PRIMARY KEY (`idqualitynonconformities`)");
        b.append(") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8");
        a.add(b.toString());

        b = new StringBuilder(); // To be removed as not to be used by Cerberus.
        b.append("CREATE TABLE `qualitynonconformitiesimpact` (");
        b.append("  `idqualitynonconformitiesimpact` bigint(20) NOT NULL AUTO_INCREMENT,");
        b.append("  `idqualitynonconformities` int(11) DEFAULT NULL,");
        b.append("  `Country` varchar(45) DEFAULT NULL,");
        b.append("  `Application` varchar(45) DEFAULT NULL,");
        b.append("  `StartDate` varchar(45) DEFAULT NULL,");
        b.append("  `StartTime` varchar(45) DEFAULT NULL,");
        b.append("  `EndDate` varchar(45) DEFAULT NULL,");
        b.append("  `EndTime` varchar(45) DEFAULT NULL,");
        b.append("  `ImpactOrCost` varchar(250) DEFAULT NULL,");
        b.append("  PRIMARY KEY (`idqualitynonconformitiesimpact`)");
        b.append(") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8");
        a.add(b.toString());

//-- Adding subsystem column
//--------------------------
        b = new StringBuilder();
        b.append("ALTER TABLE `application` CHANGE COLUMN `System` `System` VARCHAR(45) NOT NULL DEFAULT 'DEFAULT'  ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `application` ADD COLUMN `SubSystem` VARCHAR(45) NOT NULL DEFAULT ''  AFTER `System` ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE application SET subsystem=system;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE application SET system='DEFAULT';");
        a.add(b.toString());
        a.add(new StringBuilder("SELECT 1 FROM dual;").toString());

//-- dropping tag table 
//--------------------------
        b = new StringBuilder();
        b.append("DROP TABLE `tag`;");
        a.add(b.toString());

//-- Cerberus Engine Version inside execution table.
//--------------------------
        b = new StringBuilder();
        b.append("ALTER TABLE `testcaseexecution` ADD COLUMN `CrbVersion` VARCHAR(45) NULL DEFAULT NULL  AFTER `Status` ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());

//-- Screenshot filename stored inside execution table. That allow to determine if screenshot is taken or not.
//--------------------------
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepactioncontrolexecution` ADD COLUMN `ScreenshotFilename` VARCHAR(45) NULL DEFAULT NULL  AFTER `EndLong` ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepactionexecution` ADD COLUMN `ScreenshotFilename` VARCHAR(45) NULL DEFAULT NULL  AFTER `EndLong` ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());

//-- Test and TestCase information inside the execution tables. That will allow to have the full tracability on the pretestcase executed.
//--------------------------
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepexecution` ADD COLUMN `Test` VARCHAR(45) NULL DEFAULT NULL  AFTER `Step` , ADD COLUMN `TestCase` VARCHAR(45) NULL DEFAULT NULL  AFTER `Test` ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepexecution` CHANGE COLUMN `Step` `Step` INT(10) UNSIGNED NOT NULL  AFTER `TestCase` ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepactionexecution` ADD COLUMN `Test` VARCHAR(45) NULL DEFAULT NULL  AFTER `ID` , ADD COLUMN `TestCase` VARCHAR(45) NULL DEFAULT NULL  AFTER `Test` ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepactioncontrolexecution` ADD COLUMN `Test` VARCHAR(45) NULL DEFAULT NULL  AFTER `ID` , ADD COLUMN `TestCase` VARCHAR(45) NULL DEFAULT NULL  AFTER `Test` ;");
        a.add(b.toString());

//-- Cleaning Index names and Foreign Key contrains
//
        b = new StringBuilder();
        b.append("ALTER TABLE `application` DROP INDEX `FK_application` , ADD INDEX `FK_application_01` (`deploytype` ASC) ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `application` DROP FOREIGN KEY `FK_application` ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `application` ADD CONSTRAINT `FK_application_01` FOREIGN KEY (`deploytype` ) REFERENCES `deploytype` (`deploytype` ) ON DELETE CASCADE ON UPDATE CASCADE;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `buildrevisionbatch` DROP INDEX `FK_buildrevisionbatch_1` , ADD INDEX `FK_buildrevisionbatch_01` (`Batch` ASC) , DROP INDEX `FK_buildrevisionbatch_2` , ADD INDEX `FK_buildrevisionbatch_02` (`Country` ASC, `Environment` ASC) ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `buildrevisionbatch` DROP FOREIGN KEY `FK_buildrevisionbatch_1` , DROP FOREIGN KEY `FK_buildrevisionbatch_2` ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `buildrevisionbatch`   ADD CONSTRAINT `FK_buildrevisionbatch_01`  FOREIGN KEY (`Batch` )  REFERENCES `batchinvariant` (`Batch` )  ON DELETE CASCADE  ON UPDATE CASCADE,   ADD CONSTRAINT `FK_buildrevisionbatch_02`  FOREIGN KEY (`Country` , `Environment` )  REFERENCES `countryenvparam` (`Country` , `Environment` )  ON DELETE CASCADE  ON UPDATE CASCADE;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `buildrevisionparameters` DROP INDEX `FK1` , ADD INDEX `FK_buildrevisionparameters_01` (`Application` ASC) ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `buildrevisionparameters` DROP FOREIGN KEY `FK1` ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `buildrevisionparameters`   ADD CONSTRAINT `FK_buildrevisionparameters_01`  FOREIGN KEY (`Application` )  REFERENCES `application` (`Application` )  ON DELETE CASCADE  ON UPDATE CASCADE;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `comparisonstatus` DROP FOREIGN KEY `FK_comparisonstatus_1` ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `comparisonstatus`   ADD CONSTRAINT `FK_comparisonstatus_01`  FOREIGN KEY (`Execution_ID` )  REFERENCES `testcaseexecution` (`ID` )  ON DELETE CASCADE  ON UPDATE CASCADE, DROP INDEX `FK_comparisonstatus_1` , ADD INDEX `FK_comparisonstatus_01` (`Execution_ID` ASC) ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `comparisonstatusdata` DROP FOREIGN KEY `FK_comparisonstatusdata_1` ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `comparisonstatusdata`   ADD CONSTRAINT `FK_comparisonstatusdata_01`  FOREIGN KEY (`Execution_ID` )  REFERENCES `testcaseexecution` (`ID` )  ON DELETE CASCADE  ON UPDATE CASCADE, DROP INDEX `FK_comparisonstatusdata_1` , ADD INDEX `FK_comparisonstatusdata_01` (`Execution_ID` ASC) ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `countryenvdeploytype` DROP FOREIGN KEY `FK_countryenvdeploytype_1` , DROP FOREIGN KEY `FK_countryenvdeploytype_2` ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `countryenvdeploytype`   ADD CONSTRAINT `FK_countryenvdeploytype_01`  FOREIGN KEY (`deploytype` )  REFERENCES `deploytype` (`deploytype` )  ON DELETE CASCADE  ON UPDATE CASCADE,   ADD CONSTRAINT `FK_countryenvdeploytype_02`  FOREIGN KEY (`Country` , `Environment` )  REFERENCES `countryenvparam` (`Country` , `Environment` )  ON DELETE CASCADE  ON UPDATE CASCADE, DROP INDEX `FK_countryenvdeploytype_1` , ADD INDEX `FK_countryenvdeploytype_01` (`Country` ASC, `Environment` ASC) , DROP INDEX `FK_countryenvdeploytype_2` , ADD INDEX `FK_countryenvdeploytype_02` (`deploytype` ASC) ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `countryenvironmentdatabase` DROP FOREIGN KEY `FK_countryenvironmentdatabase_1` ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `countryenvironmentdatabase`   ADD CONSTRAINT `FK_countryenvironmentdatabase_01`  FOREIGN KEY (`Country` , `Environment` )  REFERENCES `countryenvparam` (`Country` , `Environment` )  ON DELETE CASCADE  ON UPDATE CASCADE, DROP INDEX `FK_countryenvironmentdatabase_1` , ADD INDEX `FK_countryenvironmentdatabase_01` (`Country` ASC, `Environment` ASC) ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `countryenvironmentparameters` DROP FOREIGN KEY `FK_countryenvironmentparameters_1` , DROP FOREIGN KEY `FK_countryenvironmentparameters_3` ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `countryenvironmentparameters`   ADD CONSTRAINT `FK_countryenvironmentparameters_01`  FOREIGN KEY (`Country` , `Environment` )  REFERENCES `countryenvparam` (`Country` , `Environment` )  ON DELETE CASCADE  ON UPDATE CASCADE,   ADD CONSTRAINT `FK_countryenvironmentparameters_02`  FOREIGN KEY (`Application` )  REFERENCES `application` (`Application` )  ON DELETE CASCADE  ON UPDATE CASCADE, DROP INDEX `FK_countryenvironmentparameters_1` , ADD INDEX `FK_countryenvironmentparameters_01` (`Country` ASC, `Environment` ASC) , DROP INDEX `FK_countryenvironmentparameters_3` , ADD INDEX `FK_countryenvironmentparameters_02` (`Application` ASC) ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `countryenvparam_log` DROP FOREIGN KEY `FK_countryenvparam_log_1` ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `countryenvparam_log`   ADD CONSTRAINT `FK_countryenvparam_log_01`  FOREIGN KEY (`Country` , `Environment` )  REFERENCES `countryenvparam` (`Country` , `Environment` )  ON DELETE CASCADE  ON UPDATE CASCADE, DROP INDEX `FK_countryenvparam_log_1` , ADD INDEX `FK_countryenvparam_log_01` (`Country` ASC, `Environment` ASC) , DROP INDEX `ID1` ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `host` DROP FOREIGN KEY `FK_host_1` ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `host`   ADD CONSTRAINT `FK_host_01`  FOREIGN KEY (`Country` , `Environment` )  REFERENCES `countryenvparam` (`Country` , `Environment` )  ON DELETE CASCADE  ON UPDATE CASCADE, DROP INDEX `FK_host_1` , ADD INDEX `FK_host_01` (`Country` ASC, `Environment` ASC) ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `log` DROP INDEX `datecre` , ADD INDEX `IX_log_01` (`datecre` ASC) ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `test` DROP INDEX `ix_Test_Active` , ADD INDEX `IX_test_01` (`Test` ASC, `Active` ASC) ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcase` DROP INDEX `Index_2` , ADD INDEX `IX_testcase_01` (`Group` ASC) ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcase` DROP INDEX `Index_3` , ADD INDEX `IX_testcase_02` (`Test` ASC, `TestCase` ASC, `Application` ASC, `TcActive` ASC, `Group` ASC) ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcase` DROP INDEX `FK_testcase_2` , ADD INDEX `IX_testcase_03` (`Application` ASC) ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcase` DROP INDEX `FK_testcase_3` , ADD INDEX `IX_testcase_04` (`Project` ASC) ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcase` DROP FOREIGN KEY `FK_testcase_1` ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcase`   ADD CONSTRAINT `FK_testcase_01`  FOREIGN KEY (`Test` )  REFERENCES `test` (`Test` )  ON DELETE CASCADE  ON UPDATE CASCADE;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE testcase SET Application=null where Application='';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcase` DROP FOREIGN KEY `FK_testcase_2` ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcase`   ADD CONSTRAINT `FK_testcase_02`  FOREIGN KEY (`Application` )  REFERENCES `application` (`Application` )  ON DELETE CASCADE  ON UPDATE CASCADE;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcase` DROP FOREIGN KEY `FK_testcase_3` ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcase`   ADD CONSTRAINT `FK_testcase_03`  FOREIGN KEY (`Project` )  REFERENCES `project` (`idproject` )  ON DELETE CASCADE  ON UPDATE CASCADE;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("DELETE FROM testcase USING testcase left outer join test ON testcase.test = test.test where test.test is null;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasecountry` DROP FOREIGN KEY `FK_testcasecountry_1` ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasecountry`   ADD CONSTRAINT `FK_testcasecountry_01`  FOREIGN KEY (`Test` , `TestCase` )  REFERENCES `testcase` (`Test` , `TestCase` )  ON DELETE CASCADE  ON UPDATE CASCADE;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasecountryproperties` DROP FOREIGN KEY `FK_testcasecountryproperties_1` ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasecountryproperties`   ADD CONSTRAINT `FK_testcasecountryproperties_01`  FOREIGN KEY (`Test` , `TestCase` , `Country` )  REFERENCES `testcasecountry` (`Test` , `TestCase` , `Country` )  ON DELETE CASCADE  ON UPDATE CASCADE;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcaseexecution` DROP FOREIGN KEY `FK_testcaseexecution_1` ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcaseexecution`   ADD CONSTRAINT `FK_testcaseexecution_01`  FOREIGN KEY (`Test` , `TestCase` )  REFERENCES `testcase` (`Test` , `TestCase` )  ON DELETE CASCADE  ON UPDATE CASCADE;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcaseexecution` DROP FOREIGN KEY `FK_testcaseexecution_3` ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcaseexecution`   ADD CONSTRAINT `FK_testcaseexecution_02`  FOREIGN KEY (`application`)  REFERENCES `application` (`application`)  ON DELETE CASCADE  ON UPDATE CASCADE;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcaseexecution` DROP INDEX `FK_TestCaseExecution_1` , ADD INDEX `IX_testcaseexecution_01` (`Test` ASC, `TestCase` ASC) ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcaseexecution` DROP INDEX `fk_testcaseexecution_2` , ADD INDEX `IX_testcaseexecution_02` (`Tag` ASC) , DROP INDEX `index_1` , ADD INDEX `IX_testcaseexecution_03` (`Start` ASC) , DROP INDEX `IX_test_testcase_country` , ADD INDEX `IX_testcaseexecution_04` (`Test` ASC, `TestCase` ASC, `Country` ASC, `Start` ASC, `ControlStatus` ASC) , DROP INDEX `index_buildrev` , ADD INDEX `IX_testcaseexecution_05` (`Build` ASC, `Revision` ASC) , DROP INDEX `fk_test` , ADD INDEX `IX_testcaseexecution_06` (`Test` ASC) , DROP INDEX `ix_TestcaseExecution` , ADD INDEX `IX_testcaseexecution_07` (`Test` ASC, `TestCase` ASC, `Build` ASC, `Revision` ASC, `Environment` ASC, `Country` ASC, `ID` ASC) , DROP INDEX `FK_testcaseexecution_3` , ADD INDEX `IX_testcaseexecution_08` (`Application` ASC) ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcaseexecutiondata` DROP INDEX `propertystart` , ADD INDEX `IX_testcaseexecutiondata_01` (`Property` ASC, `Start` ASC) , DROP INDEX `index_1` , ADD INDEX `IX_testcaseexecutiondata_02` (`Start` ASC) ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcaseexecutiondata` DROP FOREIGN KEY `FK_TestCaseExecutionData_1` ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcaseexecutiondata`   ADD CONSTRAINT `FK_testcaseexecutiondata_01`  FOREIGN KEY (`ID` )  REFERENCES `testcaseexecution` (`ID` )  ON DELETE CASCADE  ON UPDATE CASCADE;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcaseexecutionwwwdet` DROP FOREIGN KEY `FK_testcaseexecutionwwwdet_1` ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcaseexecutionwwwdet`   ADD CONSTRAINT `FK_testcaseexecutionwwwdet_01`  FOREIGN KEY (`ExecID` )  REFERENCES `testcaseexecution` (`ID` )  ON DELETE CASCADE  ON UPDATE CASCADE;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcaseexecutionwwwdet` DROP INDEX `FK_testcaseexecutionwwwdet_1` , ADD INDEX `FK_testcaseexecutionwwwdet_01` (`ExecID` ASC) ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcaseexecutionwwwsum` DROP FOREIGN KEY `FK_testcaseexecutionwwwsum_1` ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcaseexecutionwwwsum`   ADD CONSTRAINT `FK_testcaseexecutionwwwsum_01`  FOREIGN KEY (`ID` )  REFERENCES `testcaseexecution` (`ID` )  ON DELETE CASCADE ON UPDATE CASCADE, DROP INDEX `FK_testcaseexecutionwwwsum_1` ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestep` DROP FOREIGN KEY `FK_testcasestep_1` ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestep`   ADD CONSTRAINT `FK_testcasestep_01`  FOREIGN KEY (`Test` , `TestCase` )  REFERENCES `testcase` (`Test` , `TestCase` )  ON DELETE CASCADE  ON UPDATE CASCADE;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("  ALTER TABLE `testcasestepaction` DROP FOREIGN KEY `FK_testcasestepaction_1` ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepaction`   ADD CONSTRAINT `FK_testcasestepaction_01`  FOREIGN KEY (`Test` , `TestCase` , `Step` )  REFERENCES `testcasestep` (`Test` , `TestCase` , `Step` )  ON DELETE CASCADE  ON UPDATE CASCADE;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepactioncontrol` DROP FOREIGN KEY `FK_testcasestepcontrol_1` ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepactioncontrol`   ADD CONSTRAINT `FK_testcasestepactioncontrol_01`  FOREIGN KEY (`Test` , `TestCase` , `Step` , `Sequence` )  REFERENCES `testcasestepaction` (`Test` , `TestCase` , `Step` , `Sequence` )  ON DELETE CASCADE  ON UPDATE CASCADE, DROP INDEX `FK_testcasestepcontrol_1` ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepactioncontrolexecution` DROP FOREIGN KEY `FK_testcasestepcontrolexecution_1` ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepactioncontrolexecution`   ADD CONSTRAINT `FK_testcasestepactioncontrolexecution_01`  FOREIGN KEY (`ID` , `Step` )  REFERENCES `testcasestepexecution` (`ID` , `Step` )  ON DELETE CASCADE  ON UPDATE CASCADE;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepbatch` DROP FOREIGN KEY `FK_testcasestepbatchl_1` ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepbatch`   ADD CONSTRAINT `FK_testcasestepbatch_01`  FOREIGN KEY (`Test` , `TestCase` )  REFERENCES `testcase` (`Test` , `TestCase` )  ON DELETE CASCADE  ON UPDATE CASCADE;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepbatch` DROP FOREIGN KEY `FK_testcasestepbatch_2` ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepbatch`   ADD CONSTRAINT `FK_testcasestepbatch_02`  FOREIGN KEY (`Batch` )  REFERENCES `batchinvariant` (`Batch` )  ON DELETE CASCADE  ON UPDATE CASCADE;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepbatch` DROP INDEX `fk_testcasestepbatch_1` , ADD INDEX `FK_testcasestepbatch_02` (`Batch` ASC) ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepbatch` DROP INDEX `FK_testcasestepbatch_02` , ADD INDEX `IX_testcasestepbatch_01` (`Batch` ASC) ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("DELETE FROM testcasestepexecution, testcaseexecution USING testcasestepexecution left outer join testcaseexecution  ON testcasestepexecution.ID = testcaseexecution.ID where testcaseexecution.ID is null;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepexecution` DROP FOREIGN KEY `FK_testcasestepexecution_1` ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepexecution`   ADD CONSTRAINT `FK_testcasestepexecution_01`  FOREIGN KEY (`ID` )  REFERENCES `testcaseexecution` (`ID` )  ON DELETE CASCADE  ON UPDATE CASCADE;  ");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `user` DROP INDEX `ID1` , ADD UNIQUE INDEX `IX_user_01` (`Login` ASC) ;");
        a.add(b.toString());

//-- New CA Status in invariant and documentation table.
//
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`) VALUES ('TCESTATUS', 'CA', 6, 35, 'Test could not be done because of technical issues.');");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());

//-- New Cerberus Message store at the level of the execution - Header, Action and Control Level.
//
        b = new StringBuilder();
        b.append("ALTER TABLE `testcaseexecution` ADD COLUMN `ControlMessage` VARCHAR(500) NULL DEFAULT NULL  AFTER `ControlStatus` ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepactioncontrolexecution` ADD COLUMN `ReturnMessage` VARCHAR(500) NULL DEFAULT NULL  AFTER `ReturnCode` ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepactionexecution` ADD COLUMN `ReturnCode` VARCHAR(2) NULL DEFAULT NULL  AFTER `Sequence` , ADD COLUMN `ReturnMessage` VARCHAR(500) NULL DEFAULT NULL  AFTER `ReturnCode` ;");
        a.add(b.toString());

//-- New Integrity Link inside between User Group and User table.
//
        b = new StringBuilder();
        b.append("ALTER TABLE `usergroup` ADD CONSTRAINT `FK_usergroup_01`  FOREIGN KEY (`Login` )  REFERENCES `user` (`Login` )  ON DELETE CASCADE ON UPDATE CASCADE;");
        a.add(b.toString());

//-- New Parameter for Performance Monitoring Servlet.
//
        b = new StringBuilder();
        b.append("INSERT INTO `parameter` (`param`, `value`, `description`) VALUES ('cerberus_performancemonitor_nbminutes', '5', 'Integer that correspond to the number of minutes where the number of executions are collected on the servlet that manage the monitoring of the executions.');");
        a.add(b.toString());

        //-- New Parameter for link to selenium extensions firebug and netexport.
        //-
        b = new StringBuilder();
        b.append("INSERT INTO `parameter` (`param`, `value`, `description`) VALUES ('cerberus_selenium_firefoxextension_firebug', 'D:\\\\CerberusDocuments\\\\firebug-fx.xpi', 'Link to the firefox extension FIREBUG file needed to track network traffic')");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("INSERT INTO `parameter` (`param`, `value`, `description`) VALUES ('cerberus_selenium_firefoxextension_netexport', 'D:\\\\CerberusDocuments\\\\netExport.xpi', 'Link to the firefox extension NETEXPORT file needed to export network traffic')");
        a.add(b.toString());

        //-- New Invariant Browser to feed combobox.
        //-
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`) VALUES ('BROWSER', 'FIREFOX', 1, 37, 'Firefox Browser')");
        a.add(b.toString());

//-- Removing Performance Monitoring Servlet Parameter as it has been moved to the call of the URL. The number of minutes cannot be the same accross all requests.
//
        b = new StringBuilder();
        b.append("DELETE FROM `parameter` WHERE `param`='cerberus_performancemonitor_nbminutes';");
        a.add(b.toString());

//-- Cleaning invariant table in idname STATUS idname was used twice on 2 invariant group 1 and 33.
//
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET `idname`='TCSTATUS' WHERE `id`='1' and`sort`='1';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET `idname`='TCSTATUS' WHERE `id`='1' and`sort`='2';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET `idname`='TCSTATUS' WHERE `id`='1' and`sort`='3';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET `idname`='TCSTATUS' WHERE `id`='1' and`sort`='4';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET `idname`='TCSTATUS' WHERE `id`='1' and`sort`='5';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET `idname`='TCSTATUS' WHERE `id`='1' and`sort`='6';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());

//-- New invariant for execution detail list page.
//
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`) VALUES ('EXECNBMIN', '5', 10, 38, '5 Minutes');");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`) VALUES ('EXECNBMIN', '10', 20, 38, '10 Minutes');");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`) VALUES ('EXECNBMIN', '15', 30, 38, '15 Minutes');");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`) VALUES ('EXECNBMIN', '20', 40, 38, '20 Minutes');");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`) VALUES ('EXECNBMIN', '30', 50, 38, '30 Minutes');");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`) VALUES ('EXECNBMIN', '45', 60, 38, '45 Minutes');");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`) VALUES ('EXECNBMIN', '60', 70, 38, '1 Hour');");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`) VALUES ('EXECNBMIN', '90', 80, 38, '1 Hour 30 Minutes');");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`) VALUES ('EXECNBMIN', '120', 90, 38, '2 Hours');");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`) VALUES ('EXECNBMIN', '180', 100, 38, '3 Hours');");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`) VALUES ('EXECNBMIN', '0', 1, 38, 'No Limit');");
        a.add(b.toString());

//-- New Cerberus Message store at the level of the execution - Header, Action and Control Level.
//
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());

//-- New Cerberus Action  mouseOver and  mouseOverAndWait and remove of URLLOGIN, verifyTextPresent verifyTitle, verifyValue
//
        b = new StringBuilder();
        b.append("DELETE FROM `invariant` WHERE `id`='12' and`sort`='120'");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("DELETE FROM `invariant` WHERE `id`='12' and`sort`='130'");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("DELETE FROM `invariant` WHERE `id`='12' and`sort`='140'");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("DELETE FROM `invariant` WHERE `id`='12' and`sort`='150'");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`) VALUES ('ACTION', 'mouseOver', 57, 12, 'mouseOver')");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`) VALUES ('ACTION', 'mouseOverAndWait', 58, 12, 'mouseOverAndWait')");
        a.add(b.toString());

//-- New Documentation for verbose and status on the execution table.
//
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());

//-- New DefaultSystem and Team inside User table.
//
        b = new StringBuilder();
        b.append("ALTER TABLE `user` ADD COLUMN `Team` VARCHAR(45) NULL  AFTER `Name` , ADD COLUMN `DefaultSystem` VARCHAR(45) NULL  AFTER `DefaultIP` , CHANGE COLUMN `Request` `Request` VARCHAR(5) NULL DEFAULT NULL  AFTER `Password` ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());

//-- Documentation updated on verbose and added on screenshot option.
//
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());

//-- Screenshot invariant values.
//
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`)");
        b.append(" VALUES ('SCREENSHOT', '0', 10, 39, 'No Screenshot')");
        b.append(",('SCREENSHOT', '1', 20, 39, 'Screenshot on error')");
        b.append(",('SCREENSHOT', '2', 30, 39, 'Screenshot on every action');");
        a.add(b.toString());

//-- Added Test and testcase columns to Action/control/step Execution tables.
//-- Added RC and RCMessage to all execution tables + Property Data table.
//
        b = new StringBuilder();
        b.append("ALTER TABLE `testcaseexecutiondata` ADD COLUMN `RMessage` VARCHAR(500) NULL DEFAULT ''  AFTER `RC` ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepactioncontrolexecution` DROP FOREIGN KEY `FK_testcasestepactioncontrolexecution_01`;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepexecution` CHANGE COLUMN `Test` `Test` VARCHAR(45) NOT NULL DEFAULT '' , CHANGE COLUMN `TestCase` `TestCase` VARCHAR(45) NOT NULL DEFAULT '' ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepexecution` CHANGE COLUMN `ID` `ID` BIGINT(20) UNSIGNED NOT NULL  , CHANGE COLUMN `Step` `Step` INT(10) UNSIGNED NOT NULL  ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepexecution` DROP FOREIGN KEY `FK_testcasestepexecution_01`;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepexecution` ADD CONSTRAINT `FK_testcasestepexecution_01`   FOREIGN KEY (`ID` ) REFERENCES `testcaseexecution` (`ID` ) ON DELETE CASCADE ON UPDATE CASCADE ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepexecution` DROP PRIMARY KEY , ADD PRIMARY KEY (`ID`, `Test`, `TestCase`, `Step`) ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepactioncontrolexecution` CHANGE COLUMN `Test` `Test` VARCHAR(45) NOT NULL DEFAULT '' , CHANGE COLUMN `TestCase` `TestCase` VARCHAR(45) NOT NULL DEFAULT '' ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepactioncontrolexecution`  DROP PRIMARY KEY , ADD PRIMARY KEY (`ID`, `Test`, `TestCase`, `Step`, `Sequence`, `Control`) ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepactioncontrolexecution` ADD CONSTRAINT `FK_testcasestepactioncontrolexecution_01` FOREIGN KEY (`ID` , `Test` , `TestCase` , `Step` ) REFERENCES `testcasestepexecution` (`ID` , `Test` , `TestCase` , `Step` ) ON DELETE CASCADE ON UPDATE CASCADE ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepactionexecution` CHANGE COLUMN `ID` `ID` BIGINT(20) UNSIGNED NOT NULL  , CHANGE COLUMN `Test` `Test` VARCHAR(45) NOT NULL DEFAULT '' , CHANGE COLUMN `TestCase` `TestCase` VARCHAR(45) NOT NULL DEFAULT '' , CHANGE COLUMN `Step` `Step` INT(10) UNSIGNED NOT NULL ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `testcasestepactionexecution` SET Sequence=51 WHERE Step=0 and Sequence=50 and Action='Wait';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepactionexecution`  DROP PRIMARY KEY , ADD PRIMARY KEY (`ID`, `Test`, `TestCase`, `Step`, `Sequence`) ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("DELETE FROM testcasestepactionexecution WHERE ID in ( SELECT ID FROM ( SELECT a.ID FROM testcasestepactionexecution a LEFT OUTER JOIN testcasestepexecution b ON a.ID=b.ID and a.Test=b.Test and a.TestCase=b.TestCase and a.Step=b.Step WHERE b.ID is null) as toto);");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepactionexecution` ADD CONSTRAINT `FK_testcasestepactionexecution_01` FOREIGN KEY (`ID` , `Test` , `TestCase` , `Step` ) REFERENCES `testcasestepexecution` (`ID` , `Test` , `TestCase` , `Step` ) ON DELETE CASCADE ON UPDATE CASCADE;");
        a.add(b.toString());

//-- Resizing Screenshot filename to biggest possible value. 
//
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepactioncontrolexecution` CHANGE COLUMN `ScreenshotFilename` `ScreenshotFilename` VARCHAR(150) NULL DEFAULT NULL  ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepactionexecution` CHANGE COLUMN `ScreenshotFilename` `ScreenshotFilename` VARCHAR(150) NULL DEFAULT NULL  ;");
        a.add(b.toString());

//-- Correcting verifyurl to verifyURL and verifytitle to VerifyTitle in controls. 
//
        b = new StringBuilder();
        b.append("UPDATE testcasestepactioncontrol SET type='verifyUrl' where type='verifyurl';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE testcasestepactioncontrol SET type='verifyTitle' where type='verifytitle';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE invariant SET value='verifyUrl', description ='verifyUrl' where value='verifyurl' and idname='CONTROL';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE invariant SET value='verifyTitle', description ='verifyTitle' where value='verifytitle' and idname='CONTROL';");
        a.add(b.toString());

//-- Making controls standard. 
//
        b = new StringBuilder();
        b.append("UPDATE invariant SET value = 'verifyPropertyEqual', description = 'verifyPropertyEqual' where value='PropertyIsEqualTo' and idname='CONTROL';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE testcasestepactioncontrol SET type='verifyPropertyEqual' where type='PropertyIsEqualTo';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE invariant SET value = 'verifyPropertyGreater', description = 'verifyPropertyGreater' where value='PropertyIsGreaterThan' and idname='CONTROL';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE testcasestepactioncontrol SET type='verifyPropertyGreater' where type='PropertyIsGreaterThan';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE invariant SET value = 'verifyPropertyMinor', description = 'verifyPropertyMinor' where value='PropertyIsMinorThan' and idname='CONTROL';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE testcasestepactioncontrol SET type='verifyPropertyMinor' where type='PropertyIsMinorThan';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`) VALUES ('CONTROL', 'verifyPropertyDifferent', 11, 13, 'verifyPropertyDifferent');");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`) VALUES ('CONTROL', 'verifyElementNotPresent', 21, 13, 'verifyElementNotPresent');");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`) VALUES ('ACTION', 'openUrlLogin', 61, 12, 'openUrlLogin');");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE testcasestepaction SET action='openUrlLogin' where action='URLLOGIN';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET `value`='firefox' WHERE `id`='37' and`sort`='1';");
        a.add(b.toString());

//-- New parameter used by netexport. 
//
        b = new StringBuilder();
        b.append("INSERT INTO `parameter` (`param`, `value`, `description`) VALUES ('cerberus_url', 'http://localhost:8080/Cerberus', 'URL to Cerberus used in order to call back cerberus from NetExport plugin. This parameter is mandatory for saving the firebug detail information back to cerberus. ex : http://host:port/contextroot');");
        a.add(b.toString());

//-- Making controls standard. 
//
        b = new StringBuilder();
        b.append("UPDATE invariant SET value = 'verifyStringEqual', description = 'verifyStringEqual' where value='verifyPropertyEqual' and idname='CONTROL';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE testcasestepactioncontrol SET type='verifyStringEqual' where type='verifyPropertyEqual';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE invariant SET value = 'verifyStringDifferent', description = 'verifyStringDifferent' where value='verifyPropertyDifferent' and idname='CONTROL';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE testcasestepactioncontrol SET type='verifyStringDifferent' where type='verifyPropertyDifferent';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE invariant SET value = 'verifyIntegerGreater', description = 'verifyIntegerGreater' where value='verifyPropertyGreater' and idname='CONTROL';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE testcasestepactioncontrol SET type='verifyIntegerGreater' where type='verifyPropertyGreater';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE invariant SET value = 'verifyIntegerMinor', description = 'verifyIntegerMinor' where value='verifyPropertyMinor' and idname='CONTROL';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE testcasestepactioncontrol SET type='verifyIntegerMinor' where type='verifyPropertyMinor';");
        a.add(b.toString());

//-- Making Properties standard. 
//
        b = new StringBuilder();
        b.append("UPDATE invariant SET value = 'executeSql', sort=20 where value='SQL' and idname='PROPERTYTYPE';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE invariant SET value = 'executeSqlFromLib', sort=25 where value='LIB_SQL' and idname='PROPERTYTYPE';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE invariant SET value = 'getFromHtmlVisible', sort=35, description='Getting from an HTML visible field in the current page.' where value='HTML' and idname='PROPERTYTYPE';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE invariant SET value = 'text', sort=40 where value='TEXT' and idname='PROPERTYTYPE';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`) VALUES ('PROPERTYTYPE', 'getFromHtml', 30, 19, 'Getting from an html field in the current page.');");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE testcasecountryproperties SET type='text' where type='TEXT';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE testcasecountryproperties SET type='executeSqlFromLib' where type='LIB_SQL';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE testcasecountryproperties SET type='executeSql' where type='SQL';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE testcasecountryproperties SET type='getFromHtmlVisible' where type='HTML';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`) VALUES ('PROPERTYNATURE', 'NOTINUSE', 4, 20, 'Not In Use');");
        a.add(b.toString());

//-- New Control. 
//
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`) VALUES ('CONTROL', 'verifyTextNotPresent', 51, 13, 'verifyTextNotPresent');");
        a.add(b.toString());

//-- Team and system invariant initialisation.
//
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`) ");
        b.append(" VALUES ('TEAM', 'France', 10, 40, 'France Team'),");
        b.append("  ('TEAM', 'Portugal', 20, 40, 'Portugal Team'),");
        b.append("  ('SYSTEM', 'DEFAULT', 10, 41, 'System1 System'),");
        b.append("  ('SYSTEM', 'SYS2', 20, 41, 'System2 System')");
        a.add(b.toString());

//-- Changing Request column inside user table to fit boolean management standard.
//
        b = new StringBuilder();
        b.append("UPDATE `user` SET Request='Y' where Request='true';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `user` SET Request='N' where Request='false';");
        a.add(b.toString());

//-- Cleaning comparaison status tables.
//
        b = new StringBuilder();
        b.append("DROP TABLE `comparisonstatusdata`;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("DROP TABLE `comparisonstatus`;");
        a.add(b.toString());

//-- Documentation on application table.
//
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());

//-- Log Event table redesign.
//
        b = new StringBuilder();
        b.append("DROP TABLE `logeventchange`; ");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `logevent` ADD COLUMN `Login` VARCHAR(30) NOT NULL DEFAULT '' AFTER `UserID`, CHANGE COLUMN `Time` `Time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, ADD COLUMN `remoteIP` VARCHAR(20) NULL DEFAULT NULL  AFTER `Log` , ADD COLUMN `localIP` VARCHAR(20) NULL DEFAULT NULL  AFTER `remoteIP`;");
        a.add(b.toString());

//-- User group definition
//
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`, `gp1`, `gp2`, `gp3`) VALUES ");
        b.append("('USERGROUP', 'Visitor', 5, 42, 'Visitor', null, null, null),");
        b.append("('USERGROUP', 'Integrator', 10, 42, 'Integrator', null, null, null),");
        b.append("('USERGROUP', 'User', 15, 42, 'User', null, null, null),");
        b.append("('USERGROUP', 'Admin', 20, 42, 'Admin', null, null, null)");
        a.add(b.toString());

//-- New Column for Bug Tracking.
//
        b = new StringBuilder();
        b.append("ALTER TABLE `application` ADD COLUMN `BugTrackerUrl` VARCHAR(300) NULL DEFAULT ''  AFTER `svnurl` , ADD COLUMN `BugTrackerNewUrl` VARCHAR(300) NULL DEFAULT ''  AFTER `BugTrackerUrl` ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`, `gp1`, `gp2`, `gp3`) VALUES ");
        b.append("('APPLITYPE', 'GUI', 5, 43, 'GUI application', null, null, null),");
        b.append("('APPLITYPE', 'BAT', 10, 43, 'Batch Application', null, null, null),");
        b.append("('APPLITYPE', 'SRV', 15, 43, 'Service Application', null, null, null),");
        b.append("('APPLITYPE', 'NONE', 20, 43, 'Any Other Type of application', null, null, null)");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE application SET deploytype=null where deploytype is null;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("DELETE FROM `parameter` WHERE `param`='sitdmoss_bugtracking_url';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("DELETE FROM `parameter` WHERE `param`='sitdmoss_newbugtracking_url';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("DELETE FROM `parameter` WHERE `param`='cerberus_selenium_plugins_path';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("DELETE FROM `parameter` WHERE `param`='svn_application_url';");
        a.add(b.toString());

//-- New Controls for string comparaison.
//
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET `sort`=16 WHERE `id`='13' and`sort`='12';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET `sort`=17 WHERE `id`='13' and`sort`='14';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`) VALUES ");
        b.append("  ('CONTROL', 'verifyStringGreater', 12, 13, 'verifyStringGreater')");
        b.append(" ,('CONTROL', 'verifyStringMinor', 13, 13, 'verifyStringMinor');");
        a.add(b.toString());

//-- Cleaning on TextInPage control.
//
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET `value`='verifyTextInPage', `description`='verifyTextInPage' WHERE `id`='13' and`sort`='50';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `testcasestepactioncontrol` SET `type`='verifyTextInPage' WHERE `type`='verifyTextPresent';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET `value`='verifyTextNotInPage', `description`='verifyTextNotInPage' WHERE `id`='13' and`sort`='51';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `testcasestepactioncontrol` SET `type`='verifyTextNotInPage' WHERE `type`='verifyTextNotPresent';");
        a.add(b.toString());

//-- Cleaning on VerifyText --> VerifyTextInElement control.
//
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET `value`='verifyTextInElement', `description`='verifyTextInElement' WHERE `id`='13' and`sort`='40';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `testcasestepactioncontrol` SET `type`='verifyTextInElement' WHERE `type`='VerifyText';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET `value`='verifyRegexInElement', `description`='verifyRegexInElement', sort='43' WHERE `id`='13' and`sort`='80';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `testcasestepactioncontrol` SET `type`='verifyRegexInElement' WHERE `type`='verifyContainText';");
        a.add(b.toString());

//-- Enlarging BehaviorOrValueExpected and HowTo columns to TEXT (64K).
//
        b = new StringBuilder();
        b.append("ALTER TABLE `testcase` CHANGE COLUMN `BehaviorOrValueExpected` `BehaviorOrValueExpected` TEXT NULL , CHANGE COLUMN `HowTo` `HowTo` TEXT NULL ;");
        a.add(b.toString());

//-- Change length of Property column of TestCaseStepActionExecution from 45 to 200
//
        b = new StringBuilder();
        b.append("ALTER TABLE testcasestepactionexecution CHANGE Property Property varchar(200);");
        a.add(b.toString());

//-- Add invariant LANGUAGE
//
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`, `gp1`) VALUES ");
        b.append("  ('LANGUAGE', '', 1, 44, 'Default language', 'en')");
        b.append(" ,('LANGUAGE', 'BE', 5, 44, 'Belgium language', 'fr-be')");
        b.append(" ,('LANGUAGE', 'CH', 10, 44, 'Switzerland language', 'fr-ch')");
        b.append(" ,('LANGUAGE', 'ES', 15, 44, 'Spain language', 'es')");
        b.append(" ,('LANGUAGE', 'FR', 20, 44, 'France language', 'fr')");
        b.append(" ,('LANGUAGE', 'IT', 25, 44, 'Italy language', 'it')");
        b.append(" ,('LANGUAGE', 'PT', 30, 44, 'Portugal language', 'pt')");
        b.append(" ,('LANGUAGE', 'RU', 35, 44, 'Russia language', 'ru')");
        b.append(" ,('LANGUAGE', 'UK', 40, 44, 'Great Britain language', 'gb')");
        b.append(" ,('LANGUAGE', 'VI', 45, 44, 'Generic language', 'en');");
        a.add(b.toString());

//-- Cerberus can't find elements inside iframe
//
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`) VALUES ");
        b.append("('ACTION','focusToIframe',52,12,'focusToIframe'),");
        b.append("('ACTION','focusDefaultIframe',53,12,'focusDefaultIframe');");
        a.add(b.toString());

//-- Documentation on new Bug URL
//
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());

//-- Harmonize the column order of Country/Environment.
//
        b = new StringBuilder();
        b.append("ALTER TABLE `countryenvironmentdatabase` ");
        b.append(" CHANGE COLUMN `Country` `Country` VARCHAR(2) NOT NULL  FIRST , ");
        b.append(" CHANGE COLUMN `Environment` `Environment` VARCHAR(45) NOT NULL  AFTER `Country` , ");
        b.append(" DROP PRIMARY KEY , ADD PRIMARY KEY (`Country`, `Environment`, `Database`) ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `host` ");
        b.append(" CHANGE COLUMN `Environment` `Environment` VARCHAR(45) NOT NULL  AFTER `Country` , ");
        b.append(" DROP PRIMARY KEY , ADD PRIMARY KEY USING BTREE (`Country`, `Environment`, `Session`, `Server`) ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `buildrevisionbatch` ");
        b.append(" CHANGE COLUMN `Environment` `Environment` VARCHAR(45) NULL DEFAULT NULL  AFTER `Country` ;");
        a.add(b.toString());

//-- Change invariant LANGUAGE to GP2 of invariant COUNTRY
//
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET gp2 = 'fr-be' WHERE idname = 'COUNTRY' and value = 'BE';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET gp2 = 'fr-ch' WHERE idname = 'COUNTRY' and value = 'CH';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET gp2 = 'es' WHERE idname = 'COUNTRY' and value = 'ES';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET gp2 = 'it' WHERE idname = 'COUNTRY' and value = 'IT';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET gp2 = 'pt-pt' WHERE idname = 'COUNTRY' and value = 'PT';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET gp2 = 'ru' WHERE idname = 'COUNTRY' and value = 'RU';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET gp2 = 'en-gb' WHERE idname = 'COUNTRY' and value = 'UK';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET gp2 = 'en-gb' WHERE idname = 'COUNTRY' and value = 'VI';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET gp2 = 'ru' WHERE idname = 'COUNTRY' and value = 'RU';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET gp2 = 'fr' WHERE idname = 'COUNTRY' and value = 'FR';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET gp2 = 'en-gb' WHERE idname = 'COUNTRY' and value = 'RX';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("DELETE FROM `invariant` WHERE idname = 'LANGUAGE'");
        a.add(b.toString());

//-- Cleaning countryenvironmentparameters table with useless columns
//
        b = new StringBuilder();
        b.append("ALTER TABLE `countryenvironmentparameters` DROP COLUMN `as400LIB` , DROP COLUMN `JdbcPort` , DROP COLUMN `JdbcIP` , DROP COLUMN `JdbcPass` , DROP COLUMN `JdbcUser` ;");
        a.add(b.toString());

//-- Adding System level in database model.
//
        b = new StringBuilder();
        b.append("ALTER TABLE `countryenvparam` ADD COLUMN `system` VARCHAR(45) NOT NULL DEFAULT ''  FIRST ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `countryenvdeploytype` ADD COLUMN `system` VARCHAR(45) NOT NULL DEFAULT '' FIRST ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `countryenvdeploytype` DROP FOREIGN KEY `FK_countryenvdeploytype_02` ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `countryenvironmentparameters` ADD COLUMN `system` VARCHAR(45) NOT NULL DEFAULT '' FIRST ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `countryenvironmentparameters` DROP FOREIGN KEY `FK_countryenvironmentparameters_01` ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `countryenvironmentdatabase` ADD COLUMN `system` VARCHAR(45) NOT NULL DEFAULT '' FIRST ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `countryenvironmentdatabase` DROP FOREIGN KEY `FK_countryenvironmentdatabase_01` ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `host` ADD COLUMN `system` VARCHAR(45) NOT NULL DEFAULT '' FIRST ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `host` DROP FOREIGN KEY `FK_host_01` ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `countryenvparam_log` ADD COLUMN `system` VARCHAR(45) NOT NULL DEFAULT ''  AFTER `id` ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `countryenvparam_log` DROP FOREIGN KEY `FK_countryenvparam_log_01` ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `countryenvparam_log` DROP INDEX `FK_countryenvparam_log_01` ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `buildrevisionbatch` ADD COLUMN `system` VARCHAR(45) NOT NULL DEFAULT ''  AFTER `Batch` ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `buildrevisionbatch` DROP FOREIGN KEY `FK_buildrevisionbatch_02` ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `buildrevisionbatch` DROP INDEX `FK_buildrevisionbatch_02` ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `countryenvparam`  DROP PRIMARY KEY , ADD PRIMARY KEY (`system`, `Country`, `Environment`) ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `countryenvdeploytype` DROP PRIMARY KEY , ADD PRIMARY KEY (`system`, `Country`, `Environment`, `deploytype`, `JenkinsAgent`) ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `countryenvdeploytype`");
        b.append("  ADD CONSTRAINT `FK_countryenvdeploytype_1` FOREIGN KEY (`system` , `Country` , `Environment` )");
        b.append("  REFERENCES `countryenvparam` (`system` , `Country` , `Environment` ) ON DELETE CASCADE ON UPDATE CASCADE;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `countryenvdeploytype` ");
        b.append(" DROP INDEX `FK_countryenvdeploytype_01` , ADD INDEX `FK_countryenvdeploytype_01` (`system` ASC, `Country` ASC, `Environment` ASC) ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `countryenvdeploytype` DROP FOREIGN KEY `FK_countryenvdeploytype_01` ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `countryenvdeploytype` ");
        b.append("  ADD CONSTRAINT `FK_countryenvdeploytype_02`");
        b.append("  FOREIGN KEY (`deploytype` )");
        b.append("  REFERENCES `deploytype` (`deploytype` )");
        b.append("  ON DELETE CASCADE");
        b.append("  ON UPDATE CASCADE;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `countryenvdeploytype` DROP FOREIGN KEY `FK_countryenvdeploytype_1` ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `countryenvdeploytype` ");
        b.append("  ADD CONSTRAINT `FK_countryenvdeploytype_01`");
        b.append("  FOREIGN KEY (`system` , `Country` , `Environment` )");
        b.append("  REFERENCES `countryenvparam` (`system` , `Country` , `Environment` )");
        b.append("  ON DELETE CASCADE");
        b.append("  ON UPDATE CASCADE;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `countryenvironmentparameters` DROP PRIMARY KEY , ADD PRIMARY KEY (`system`, `Country`, `Environment`, `Application`) ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `countryenvironmentparameters` ");
        b.append("  ADD CONSTRAINT `FK_countryenvironmentparameters_01` FOREIGN KEY (`system` , `Country` , `Environment` )");
        b.append("  REFERENCES `countryenvparam` (`system` , `Country` , `Environment` ) ON DELETE CASCADE ON UPDATE CASCADE;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `countryenvironmentparameters` ");
        b.append("DROP INDEX `FK_countryenvironmentparameters_01` , ADD INDEX `FK_countryenvironmentparameters_01` (`system` ASC, `Country` ASC, `Environment` ASC) ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `buildrevisionbatch` ");
        b.append("  ADD CONSTRAINT `FK_buildrevisionbatch_02`");
        b.append("  FOREIGN KEY (`system` , `Country` , `Environment` )");
        b.append("  REFERENCES `countryenvparam` (`system` , `Country` , `Environment` )");
        b.append("  ON DELETE CASCADE   ON UPDATE CASCADE");
        b.append(", ADD INDEX `FK_buildrevisionbatch_02` (`system` ASC, `Country` ASC, `Environment` ASC) ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `countryenvironmentdatabase` ");
        b.append("DROP PRIMARY KEY , ADD PRIMARY KEY (`system`, `Country`, `Environment`, `Database`) ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `countryenvironmentdatabase` ");
        b.append("  ADD CONSTRAINT `FK_countryenvironmentdatabase_01`");
        b.append("  FOREIGN KEY (`system` , `Country` , `Environment` )");
        b.append("  REFERENCES `countryenvparam` (`system` , `Country` , `Environment` )");
        b.append("  ON DELETE CASCADE ON UPDATE CASCADE");
        b.append(", DROP INDEX  `FK_countryenvironmentdatabase_01` , ADD INDEX `FK_countryenvironmentdatabase_01` (`system` ASC, `Country` ASC, `Environment` ASC) ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `host` ");
        b.append("DROP PRIMARY KEY , ADD PRIMARY KEY (`system`, `Country`, `Environment`, `Session`, `Server`) ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `host` ");
        b.append("DROP INDEX `FK_host_01` , ADD INDEX `FK_host_01` (`system` ASC, `Country` ASC, `Environment` ASC) ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `host` ");
        b.append("  ADD CONSTRAINT `FK_host_01`");
        b.append("  FOREIGN KEY (`system` , `Country` , `Environment` )");
        b.append("  REFERENCES `countryenvparam` (`system` , `Country` , `Environment` )");
        b.append("  ON DELETE CASCADE ON UPDATE CASCADE ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `countryenvparam_log` ");
        b.append("  ADD CONSTRAINT `FK_countryenvparam_log_01`");
        b.append("  FOREIGN KEY (`system` , `Country` , `Environment` )");
        b.append("  REFERENCES `countryenvparam` (`system` , `Country` , `Environment` )");
        b.append("  ON DELETE CASCADE ON UPDATE CASCADE");
        b.append(", ADD INDEX `FK_countryenvparam_log_01` (`system` ASC, `Country` ASC, `Environment` ASC) ;");
        a.add(b.toString());

//-- Enlarge data execution column in order to keep track of full SQL executed.
//
        b = new StringBuilder();
        b.append("ALTER TABLE `testcaseexecutiondata` CHANGE COLUMN `Value` `Value` VARCHAR(3000) NOT NULL  , CHANGE COLUMN `RMessage` `RMessage` VARCHAR(3000) NULL DEFAULT ''  ;");
        a.add(b.toString());

//-- Insert default environment in order to get examples running.
//
        b = new StringBuilder();
        b.append("INSERT INTO `countryenvparam` (`system`, `Country`, `Environment`, `Build`, `Revision`, `Chain`, `DistribList`, `EMailBodyRevision`, `Type`, `EMailBodyChain`, `EMailBodyDisableEnvironment`, `active`, `maintenanceact`) VALUES ('DEFAULT', 'FR', 'PROD', '', '', '', '', '', 'STD', '', '', 'Y', 'N');");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("INSERT INTO `countryenvironmentparameters` (`system`, `Country`, `Environment`, `Application`, `IP`, `URL`, `URLLOGIN`) VALUES ('DEFAULT', 'FR', 'PROD', 'Google', 'www.google.fr', '/', '');");
        a.add(b.toString());

//-- Force default system to DEFAULT.
// 330
        b = new StringBuilder();
        b.append("UPDATE `user` SET DefaultSystem='DEFAULT' where DefaultSystem is null;");
        a.add(b.toString());

//
//
//-- Cerberus 0.9.0 Stops here.
//
//
//-- Database structure to handle link between environment and history of Build rev per system for each execution.
// 331
        b = new StringBuilder();
        b.append("CREATE  TABLE `testcaseexecutionsysver` (");
        b.append("  `ID` BIGINT UNSIGNED NOT NULL ,");
        b.append("  `system` VARCHAR(45) NOT NULL ,");
        b.append("  `Build` VARCHAR(10) NULL ,");
        b.append("  `Revision` VARCHAR(20) NULL ,");
        b.append("  PRIMARY KEY (`ID`, `system`) ,");
        b.append("  INDEX `FK_testcaseexecutionsysver_01` (`ID` ASC) ,");
        b.append("  CONSTRAINT `FK_testcaseexecutionsysver_01`");
        b.append("    FOREIGN KEY (`ID` )");
        b.append("    REFERENCES `testcaseexecution` (`ID` )");
        b.append("    ON DELETE CASCADE ON UPDATE CASCADE);");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("CREATE  TABLE `countryenvlink` (");
        b.append("  `system` VARCHAR(45) NOT NULL DEFAULT '' ,");
        b.append("  `Country` VARCHAR(2) NOT NULL ,");
        b.append("  `Environment` VARCHAR(45) NOT NULL ,");
        b.append("  `systemLink` VARCHAR(45) NOT NULL DEFAULT '' ,");
        b.append("  `CountryLink` VARCHAR(2) NOT NULL ,");
        b.append("  `EnvironmentLink` VARCHAR(45) NOT NULL ,");
        b.append("  PRIMARY KEY (`system`, `Country`, `Environment`,`systemLink`, `CountryLink`, `EnvironmentLink`) ,");
        b.append("  INDEX `FK_countryenvlink_01` (`system` ASC, `Country` ASC, `Environment` ASC) ,");
        b.append("  INDEX `FK_countryenvlink_02` (`systemLink` ASC, `CountryLink` ASC, `EnvironmentLink` ASC) ,");
        b.append("  CONSTRAINT `FK_countryenvlink_01`");
        b.append("    FOREIGN KEY (`system` , `Country` , `Environment` )");
        b.append("    REFERENCES `countryenvparam` (`system` , `Country` , `Environment` )");
        b.append("    ON DELETE CASCADE ON UPDATE CASCADE,");
        b.append("  CONSTRAINT `FK_countryenvlink_02`");
        b.append("    FOREIGN KEY (`systemLink` , `CountryLink` , `EnvironmentLink` )");
        b.append("    REFERENCES `countryenvparam` (`system` , `Country` , `Environment` )");
        b.append("    ON DELETE CASCADE ON UPDATE CASCADE)  ENGINE=InnoDB DEFAULT CHARSET=utf8 ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `countryenvlink` ");
        b.append("DROP PRIMARY KEY ");
        b.append(", ADD PRIMARY KEY (`system`, `Country`, `Environment`, `systemLink`) ;");
        a.add(b.toString());

//-- New Documentation on homepage.
// 334
        b = new StringBuilder();
        b.append("DELETE FROM `invariant` WHERE `id`='23' and`sort`='6';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());

//-- Update System Variables %XXX% to %SYS_XXX%.
// 336
        b = new StringBuilder();
        b.append("UPDATE testcasestepactioncontrol ");
        b.append("SET ControlValue=REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(ControlValue,\'%ENV%\',\'%SYS_ENV%\'),\'%ENVGP%\',\'%SYS_ENVGP%\'),\'%COUNTRY%\',\'%SYS_COUNTRY%\'),\'%COUNTRYGP1%\',\'%SYS_COUNTRYGP1%\'),\'%SSIP%\',\'%SYS_SSIP%\'),\'%SSPORT%\',\'%SYS_SSPORT%\'),\'%TAG%\',\'%SYS_TAG%\'),\'%TODAY-yyyy%\',\'%SYS_TODAY-yyyy%\'),\'%TODAY-MM%\',\'%SYS_TODAY-MM%\'),\'%TODAY-dd%\',\'%SYS_TODAY-dd%\'),\'%TODAY-HH%\',\'%SYS_TODAY-HH%\'),\'%TODAY-mm%\',\'%SYS_TODAY-mm%\'),\'%TODAY-ss%\',\'%SYS_TODAY-ss%\'),\'%YESTERDAY-yyyy%\',\'%SYS_YESTERDAY-yyyy%\'),\'%YESTERDAY-MM%\',\'%SYS_YESTERDAY-MM%\'),\'%YESTERDAY-dd%\',\'%SYS_YESTERDAY-dd%\'),\'%YESTERDAY-HH%\',\'%SYS_YESTERDAY-HH%\'),\'%YESTERDAY-mm%\',\'%SYS_YESTERDAY-mm%\'),\'%YESTERDAY-ss%\',\'%SYS_YESTERDAY-ss%\')");
        b.append(", ControlProperty=REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(ControlProperty,\'%ENV%\',\'%SYS_ENV%\'),\'%ENVGP%\',\'%SYS_ENVGP%\'),\'%COUNTRY%\',\'%SYS_COUNTRY%\'),\'%COUNTRYGP1%\',\'%SYS_COUNTRYGP1%\'),\'%SSIP%\',\'%SYS_SSIP%\'),\'%SSPORT%\',\'%SYS_SSPORT%\'),\'%TAG%\',\'%SYS_TAG%\'),\'%TODAY-yyyy%\',\'%SYS_TODAY-yyyy%\'),\'%TODAY-MM%\',\'%SYS_TODAY-MM%\'),\'%TODAY-dd%\',\'%SYS_TODAY-dd%\'),\'%TODAY-HH%\',\'%SYS_TODAY-HH%\'),\'%TODAY-mm%\',\'%SYS_TODAY-mm%\'),\'%TODAY-ss%\',\'%SYS_TODAY-ss%\'),\'%YESTERDAY-yyyy%\',\'%SYS_YESTERDAY-yyyy%\'),\'%YESTERDAY-MM%\',\'%SYS_YESTERDAY-MM%\'),\'%YESTERDAY-dd%\',\'%SYS_YESTERDAY-dd%\'),\'%YESTERDAY-HH%\',\'%SYS_YESTERDAY-HH%\'),\'%YESTERDAY-mm%\',\'%SYS_YESTERDAY-mm%\'),\'%YESTERDAY-ss%\',\'%SYS_YESTERDAY-ss%\');");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE testcasestepaction ");
        b.append("SET Object=REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(Object,\'%ENV%\',\'%SYS_ENV%\'),\'%ENVGP%\',\'%SYS_ENVGP%\'),\'%COUNTRY%\',\'%SYS_COUNTRY%\'),\'%COUNTRYGP1%\',\'%SYS_COUNTRYGP1%\'),\'%SSIP%\',\'%SYS_SSIP%\'),\'%SSPORT%\',\'%SYS_SSPORT%\'),\'%TAG%\',\'%SYS_TAG%\'),\'%TODAY-yyyy%\',\'%SYS_TODAY-yyyy%\'),\'%TODAY-MM%\',\'%SYS_TODAY-MM%\'),\'%TODAY-dd%\',\'%SYS_TODAY-dd%\'),\'%TODAY-HH%\',\'%SYS_TODAY-HH%\'),\'%TODAY-mm%\',\'%SYS_TODAY-mm%\'),\'%TODAY-ss%\',\'%SYS_TODAY-ss%\'),\'%YESTERDAY-yyyy%\',\'%SYS_YESTERDAY-yyyy%\'),\'%YESTERDAY-MM%\',\'%SYS_YESTERDAY-MM%\'),\'%YESTERDAY-dd%\',\'%SYS_YESTERDAY-dd%\'),\'%YESTERDAY-HH%\',\'%SYS_YESTERDAY-HH%\'),\'%YESTERDAY-mm%\',\'%SYS_YESTERDAY-mm%\'),\'%YESTERDAY-ss%\',\'%SYS_YESTERDAY-ss%\')");
        b.append(", property=REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(property,\'%ENV%\',\'%SYS_ENV%\'),\'%ENVGP%\',\'%SYS_ENVGP%\'),\'%COUNTRY%\',\'%SYS_COUNTRY%\'),\'%COUNTRYGP1%\',\'%SYS_COUNTRYGP1%\'),\'%SSIP%\',\'%SYS_SSIP%\'),\'%SSPORT%\',\'%SYS_SSPORT%\'),\'%TAG%\',\'%SYS_TAG%\'),\'%TODAY-yyyy%\',\'%SYS_TODAY-yyyy%\'),\'%TODAY-MM%\',\'%SYS_TODAY-MM%\'),\'%TODAY-dd%\',\'%SYS_TODAY-dd%\'),\'%TODAY-HH%\',\'%SYS_TODAY-HH%\'),\'%TODAY-mm%\',\'%SYS_TODAY-mm%\'),\'%TODAY-ss%\',\'%SYS_TODAY-ss%\'),\'%YESTERDAY-yyyy%\',\'%SYS_YESTERDAY-yyyy%\'),\'%YESTERDAY-MM%\',\'%SYS_YESTERDAY-MM%\'),\'%YESTERDAY-dd%\',\'%SYS_YESTERDAY-dd%\'),\'%YESTERDAY-HH%\',\'%SYS_YESTERDAY-HH%\'),\'%YESTERDAY-mm%\',\'%SYS_YESTERDAY-mm%\'),\'%YESTERDAY-ss%\',\'%SYS_YESTERDAY-ss%\');");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE testcasecountryproperties ");
        b.append("SET Value=REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(Value,\'%ENV%\',\'%SYS_ENV%\'),\'%ENVGP%\',\'%SYS_ENVGP%\'),\'%COUNTRY%\',\'%SYS_COUNTRY%\'),\'%COUNTRYGP1%\',\'%SYS_COUNTRYGP1%\'),\'%SSIP%\',\'%SYS_SSIP%\'),\'%SSPORT%\',\'%SYS_SSPORT%\'),\'%TAG%\',\'%SYS_TAG%\'),\'%TODAY-yyyy%\',\'%SYS_TODAY-yyyy%\'),\'%TODAY-MM%\',\'%SYS_TODAY-MM%\'),\'%TODAY-dd%\',\'%SYS_TODAY-dd%\'),\'%TODAY-HH%\',\'%SYS_TODAY-HH%\'),\'%TODAY-mm%\',\'%SYS_TODAY-mm%\'),\'%TODAY-ss%\',\'%SYS_TODAY-ss%\'),\'%YESTERDAY-yyyy%\',\'%SYS_YESTERDAY-yyyy%\'),\'%YESTERDAY-MM%\',\'%SYS_YESTERDAY-MM%\'),\'%YESTERDAY-dd%\',\'%SYS_YESTERDAY-dd%\'),\'%YESTERDAY-HH%\',\'%SYS_YESTERDAY-HH%\'),\'%YESTERDAY-mm%\',\'%SYS_YESTERDAY-mm%\'),\'%YESTERDAY-ss%\',\'%SYS_YESTERDAY-ss%\');");
        a.add(b.toString());

//-- Added takeScreenshot action.
// 339
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`) VALUES ('ACTION', 'takeScreenshot', 105, 12, 'takeScreenshot');");
        a.add(b.toString());

//-- New Parameter for Selenium download link.
// 340
        b = new StringBuilder();
        b.append("INSERT INTO `parameter` (`param`, `value`, `description`) VALUES ('selenium_download_url', 'http://selenium.googlecode.com/files/selenium-server-standalone-2.35.0.jar', 'URL to download the selenium package from the web.');");
        a.add(b.toString());

//-- New Documentation on detail execution page.
// 341
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());

//-- Revision Field resized to 20 to fit standard size in testcase execution table.
// 343
        b = new StringBuilder();
        b.append("ALTER TABLE `testcaseexecution` CHANGE COLUMN `Revision` `Revision` VARCHAR(20) NULL DEFAULT NULL ;");
        a.add(b.toString());

//-- Replace \n by <br/> in HowTo textarea of TestCase
// 344
        b = new StringBuilder();
        b.append("UPDATE testcase SET HowTo=REPLACE(HowTo, '\\n', '<br/>');");
        a.add(b.toString());

//-- Adding table that will host specific build revision lists per system.
// 345
        b = new StringBuilder();
        b.append(" CREATE  TABLE `buildrevisioninvariant` (");
        b.append("  `system` VARCHAR(45) NOT NULL DEFAULT '' ,");
        b.append("  `level` INT NOT NULL ,");
        b.append("  `seq` INT NOT NULL ,");
        b.append("  `versionname` VARCHAR(20) NULL ,");
        b.append("  PRIMARY KEY (`system`, `level`, `seq`),");
        b.append("  UNIQUE INDEX `IX_buildrevisioninvariant_01` (`system`,`level`,`versionname`) );");
        a.add(b.toString());

//-- Cleaning Build and Revision from invariant table.
// 346
        b = new StringBuilder();
        b.append("DELETE FROM `invariant` where id in (8,9);");
        a.add(b.toString());

//-- New Parameter for Selenium timeout when waiting for an element.
// 347
        b = new StringBuilder();
        b.append("INSERT INTO `parameter` (`param`, `value`, `description`) VALUES ('selenium_defaultWait', '90', 'Integer that correspond to the number of seconds that selenium will wait before give timeout, when searching for a element.');");
        a.add(b.toString());

//-- Updating documentation.
// 348
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());

//-- Clean Group occurence.
//
        b = new StringBuilder(); // INTERACTIVE becomes AUTOMATED
        b.append("UPDATE `invariant` SET `value`='AUTOMATED', `sort`='20' WHERE `id`='2' and`sort`='2';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `testcase` SET `Group`='AUTOMATED' WHERE `Group`='INTERACTIVE';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET `sort`='10' WHERE `id`='2' and`sort`='6';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET `sort`='30' WHERE `id`='2' and`sort`='5';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET `sort`='40' WHERE `id`='2' and`sort`='3';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET `sort`='50' WHERE `id`='2' and`sort`='4';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET `sort`='70' WHERE `id`='2' and`sort`='1';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());

//-- Adding system column to parameter table.
//
        b = new StringBuilder();
        b.append("ALTER TABLE `parameter` ADD COLUMN `system` VARCHAR(45) NOT NULL  FIRST ");
        b.append(", DROP PRIMARY KEY , ADD PRIMARY KEY (`system`, `param`) ; ");
        a.add(b.toString());

//-- Adding Index for performance optimisation and renaming other index for MySQL compliance (Index must have different names from Foreign Keys).
//
        b = new StringBuilder();
        b.append("ALTER TABLE `buildrevisionparameters` ");
        b.append(" ADD INDEX `IX_buildrevisionparameters_02` (`Build` ASC, `Revision` ASC, `Application` ASC) ");
        b.append(" ,DROP INDEX `FK_buildrevisionparameters_01`, ADD INDEX `FK_buildrevisionparameters_01_IX` (`Application` ASC) ; ");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcaseexecutionsysver` ");
        b.append(" DROP INDEX `FK_testcaseexecutionsysver_01` , ADD INDEX `FK_testcaseexecutionsysver_01_IX` (`ID` ASC) ");
        b.append(" , ADD INDEX `IX_testcaseexecutionsysver_02` (`system` ASC, `Build` ASC, `Revision` ASC) ;");
        a.add(b.toString());

        b = new StringBuilder();
        b.append("ALTER TABLE `application` ");
        b.append(" DROP INDEX `FK_application_01` , ADD INDEX `FK_application_01_IX` (`deploytype` ASC) ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `buildrevisionbatch` ");
        b.append(" DROP INDEX `FK_buildrevisionbatch_01` , ADD INDEX `FK_buildrevisionbatch_01_IX` (`Batch` ASC) ");
        b.append(" , DROP INDEX `FK_buildrevisionbatch_02` , ADD INDEX `FK_buildrevisionbatch_02_IX` (`system` ASC, `Country` ASC, `Environment` ASC) ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `countryenvdeploytype` ");
        b.append(" DROP INDEX `FK_countryenvdeploytype_02` , ADD INDEX `FK_countryenvdeploytype_02_IX` (`deploytype` ASC) ");
        b.append(" , DROP INDEX `FK_countryenvdeploytype_01` , ADD INDEX `FK_countryenvdeploytype_01_IX` (`system` ASC, `Country` ASC, `Environment` ASC) ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `countryenvironmentdatabase` ");
        b.append(" DROP INDEX `FK_countryenvironmentdatabase_01` , ADD INDEX `FK_countryenvironmentdatabase_01_IX` (`system` ASC, `Country` ASC, `Environment` ASC) ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `countryenvironmentparameters` ");
        b.append(" DROP INDEX `FK_countryenvironmentparameters_02` , ADD INDEX `FK_countryenvironmentparameters_02_IX` (`Application` ASC) ");
        b.append(" , DROP INDEX `FK_countryenvironmentparameters_01` , ADD INDEX `FK_countryenvironmentparameters_01_IX` (`system` ASC, `Country` ASC, `Environment` ASC) ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `countryenvparam_log` ");
        b.append(" DROP INDEX `FK_countryenvparam_log_01` , ADD INDEX `FK_countryenvparam_log_01_IX` (`system` ASC, `Country` ASC, `Environment` ASC) ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `host` ");
        b.append(" DROP INDEX `FK_host_01` , ADD INDEX `FK_host_01_IX` (`system` ASC, `Country` ASC, `Environment` ASC) ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcaseexecutionwwwdet` ");
        b.append(" DROP INDEX `FK_testcaseexecutionwwwdet_01` , ADD INDEX `FK_testcaseexecutionwwwdet_01_IX` (`ExecID` ASC) ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepactioncontrol` ");
        b.append(" DROP INDEX `FK_testcasestepactioncontrol_01` , ADD INDEX `FK_testcasestepactioncontrol_01_IX` (`Test` ASC, `TestCase` ASC, `Step` ASC, `Sequence` ASC) ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `countryenvlink` ");
        b.append(" DROP INDEX `FK_countryenvlink_01` , ADD INDEX `FK_countryenvlink_01_IX` (`system` ASC, `Country` ASC, `Environment` ASC) ");
        b.append(" , DROP INDEX `FK_countryenvlink_02` , ADD INDEX `FK_countryenvlink_02_IX` (`systemLink` ASC, `CountryLink` ASC, `EnvironmentLink` ASC) ;");
        a.add(b.toString());

// Browser IE and Chrome added to the invariant table
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`) VALUES ('BROWSER', 'iexplorer', 2, 37, 'Internet Explorer Browser');");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`) VALUES ('BROWSER', 'chrome', 3, 37, 'Chrome Browser');");
        a.add(b.toString());

// MouseUp And MouseDown Added to the invariant table
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET `sort`='54' WHERE `id`='12' and`sort`='55';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`) VALUES ('ACTION', 'mouseDown', 55, 12, 'Selenium Action mouseDown');");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`) VALUES ('ACTION', 'mouseUp', 56, 12, 'Selenium Action mouseDown');");
        a.add(b.toString());

// New usergroups added to the invariant table
        b = new StringBuilder();
        b.append("ALTER TABLE `invariant` CHANGE COLUMN `description` `description` VARCHAR(250) NOT NULL ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`) VALUES ");
        b.append(" ('USERGROUP', 'TestRO', '100', '42', 'Has read only access to the information related to test cases and also has access to execution reporting options.')");
        b.append(" ,('USERGROUP', 'Test', '110', '42', 'Can modify non WORKING test cases but cannot delete test cases.')");
        b.append(" ,('USERGROUP', 'TestAdmin', '120', '42', 'Can modify or delete any test case (including Pre Testing test cases). Can also create or delete a test.')");
        b.append(" ,('USERGROUP', 'RunTest', '200', '42', 'Can run both Manual and Automated test cases from GUI.')");
        b.append(" ,('USERGROUP', 'IntegratorRO', '300', '42', 'Has access to the integration status.')");
        b.append(" ,('USERGROUP', 'IntegratorNewChain', '350', '42', 'Can register the end of the chain execution. Has read only access to the other informations on the same page.')");
        b.append(" ,('USERGROUP', 'IntegratorDeploy', '360', '42', 'Can disable or enable environments and register new build / revision.')");
        b.append(" ,('USERGROUP', 'Integrator', '310', '42', 'Can add an application. Can change parameters of the environments.')");
        b.append(" ,('USERGROUP', 'Administrator', '400', '42', 'Can create, modify or delete users. Has access to log Event and Database Maintenance. Can change Parameter values.');");
        a.add(b.toString());

// GroupName column resized in order to support the new group list.
        b = new StringBuilder();
        b.append("ALTER TABLE `usergroup` CHANGE COLUMN `GroupName` `GroupName` VARCHAR(45) NOT NULL  ;");
        a.add(b.toString());

// Creating the new groups from the previous groups.
        b = new StringBuilder();
        b.append("INSERT INTO usergroup SELECT distinct Login, 'TestRO' FROM usergroup where GroupName in ('User','Visitor','Admin');");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("INSERT INTO usergroup SELECT distinct Login, 'Test' FROM usergroup where GroupName in ('User','Admin');");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("INSERT INTO usergroup SELECT distinct Login, 'TestAdmin' FROM usergroup where GroupName in ('Admin');");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("INSERT INTO usergroup SELECT distinct Login, 'RunTest' FROM usergroup where GroupName in ('User','Admin');");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("INSERT INTO usergroup SELECT distinct Login, 'IntegratorRO' FROM usergroup where GroupName in ('Visitor','Integrator');");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("INSERT INTO usergroup SELECT distinct Login, 'IntegratorNewChain' FROM usergroup where GroupName in ('Integrator');");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("INSERT INTO usergroup SELECT distinct Login, 'IntegratorDeploy' FROM usergroup where GroupName in ('Integrator');");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("INSERT INTO usergroup SELECT distinct Login, 'Administrator' FROM usergroup where GroupName in ('Admin');");
        a.add(b.toString());

// Removing the old groups.
        b = new StringBuilder();
        b.append("DELETE FROM `invariant` WHERE `id`='42' and `sort` in ('5','10','15','20');");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("DELETE FROM `usergroup` where GroupName in ('Admin','User','Visitor');");
        a.add(b.toString());

// Group definition documentation.
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());

// Adding Description column in actions and control with associated documentation.
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepactioncontrol` ADD COLUMN `ControlDescription` VARCHAR(255) NOT NULL DEFAULT ''  AFTER `ControlProperty` ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepaction` ADD COLUMN `Description` VARCHAR(255) NOT NULL DEFAULT ''  AFTER `Property` ;");
        a.add(b.toString());

        // Creating table to host test data inside Cerberus (used when we cannot dynamically retreive data from the system).
        b = new StringBuilder();
        b.append("CREATE TABLE `testdata` (");
        b.append("  `key` varchar(200) NOT NULL ,");
        b.append("  `value` varchar(5000) NOT NULL DEFAULT '',");
        b.append("  PRIMARY KEY (`key`)");
        b.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8");
        a.add(b.toString());

        //Add parameters for the cerberus acount creation emailing
        b = new StringBuilder();
        b.append("INSERT INTO `parameter` (`system`, `param`, `value`, `description`) VALUES ");
        b.append("('', 'cerberus_accountcreation_defaultpassword', 'Cerberus2014', 'Default Password when creating an account.')");
        b.append(",('', 'cerberus_notification_accountcreation_cc', 'Cerberus <no.reply@cerberus-testing.org>', 'Copy List used for Cerberus account creation notification email.')");
        b.append(",('', 'cerberus_notification_accountcreation_subject', '[Cerberus] Welcome, your account has been created', 'Subject of Cerberus account creation notification email.')");
        b.append(",('', 'cerberus_notification_accountcreation_body', 'Hello %NAME%<br><br>Your Cerberus account has been created<br><br>To connect Cerberus, please click <a href=\"http://cerberus_server/Cerberus\">here</a> and use this credential : <br><br>login : %LOGIN%<br>password : %DEFAULT_PASSWORD%<br><br>At your first connection, you will be invited to modify your password<br><br>Enjoy the tool<br><br>','Cerberus account creation notification email body. %LOGIN%, %NAME% and %DEFAULT_PASSWORD% can be used as variables.')");
        b.append(",('', 'cerberus_notification_accountcreation_from','Cerberus <no.reply@cerberus-testing.org>', 'From field of Cerberus account creation notification email.')");
        b.append(",('', 'cerberus_notification_accountcreation_activatenotification','N', 'Activation boolean for sending automatic email on account creation. Y value will activate the notifications. Any other value will not.')");
        a.add(b.toString());

        //Add email column in user table
        b = new StringBuilder();
        b.append("ALTER TABLE `user` ADD COLUMN `Email` VARCHAR(100) NULL AFTER `DefaultSystem`");
        a.add(b.toString());

        // Removing internal column inside application table.
        b = new StringBuilder();
        b.append("ALTER TABLE `application` DROP COLUMN `internal` ;");
        a.add(b.toString());

        // Fixing a typo ACTON --> ACTION in invariant table.
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET `idname`='ACTION' WHERE `id`='12' and`sort`='45';");
        a.add(b.toString());

// removing addSelection action that did not exist and putting Unknown action in stead.
        b = new StringBuilder();
        b.append("UPDATE `invariant` set  `value`='Unknown', `description`='Unknown' where `idname`='ACTION' and `value`='addSelection';");
        a.add(b.toString());

// Adding switchToWindow action.
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` VALUES ('ACTION','switchToWindow',180,12,'switchToWindow',NULL,NULL,NULL);");
        a.add(b.toString());

// New updated Documentation.
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());

// Adding getFromTestData property type.
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`) VALUES ('PROPERTYTYPE', 'getFromTestData', '10', '19', 'Getting from the test Data library using the Key');");
        a.add(b.toString());

// Reordering status.
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET `sort`='60' WHERE `id`='1' and`sort`='6';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET `sort`='50' WHERE `id`='1' and`sort`='5';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET `sort`='40' WHERE `id`='1' and`sort`='4';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET `sort`='20' WHERE `id`='1' and`sort`='3';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET `sort`='30' WHERE `id`='1' and`sort`='2';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET `sort`='10' WHERE `id`='1' and`sort`='1';");
        a.add(b.toString());

// New updated Documentation.
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());

// Adding the full version of Browser inside the execution table.
        b = new StringBuilder();
        b.append("ALTER TABLE `testcaseexecution` ADD COLUMN `BrowserFullVersion` VARCHAR(100) NULL DEFAULT ''  AFTER `Browser` ;");
        a.add(b.toString());

// New updated Documentation.
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());

// URL to download drivers.
        b = new StringBuilder();
        b.append("INSERT INTO `parameter` (`system`, `param`, `value`, `description`) VALUES ('', 'selenium_chromedriver_download_url', 'http://chromedriver.storage.googleapis.com/index.html', 'Download URL for Selenium Chrome webdrivers.') ");
        b.append(",('', 'selenium_iedriver_download_url', 'http://code.google.com/p/selenium/downloads/list','Download URL for Internet Explorer webdrivers.');");
        a.add(b.toString());

// Add verifyElementNotVisible to control
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`, `gp1`, `gp2`, `gp3`) ");
        b.append("VALUES ('CONTROL', 'verifyElementNotVisible', 31, 13, 'verifyElementNotVisible', NULL, NULL, NULL);");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `documentation` CHANGE COLUMN `DocLabel` `DocLabel` VARCHAR(100) NULL DEFAULT NULL  , CHANGE COLUMN `DocDesc` `DocDesc` TEXT NULL DEFAULT NULL  ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());

// Reordering status.
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET `sort`='31' WHERE `id`='1' and`sort`='20';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET `sort`='21' WHERE `id`='1' and`sort`='30';");
        a.add(b.toString());

// Documentation update on new variable of New bug URL.
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());

// Resized URL links in application table.
        b = new StringBuilder();
        b.append("ALTER TABLE `application` CHANGE COLUMN `BugTrackerUrl` `BugTrackerUrl` VARCHAR(5000) NULL DEFAULT ''  , CHANGE COLUMN `BugTrackerNewUrl` `BugTrackerNewUrl` VARCHAR(5000) NULL DEFAULT '' ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());

// Adding a very short description in invariant table.
        b = new StringBuilder();
        b.append("ALTER TABLE `invariant` ADD COLUMN `VeryShortDesc` VARCHAR(45) NULL DEFAULT '' AFTER `description` ");
        a.add(b.toString());

// Initialise gp1 and VeryShortDesc for TCStatus.
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET `VeryShortDesc`=description WHERE `id`='1' ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET `gp1`='Y' WHERE `id`='1' ;");
        a.add(b.toString());

// Add manageDialog to action and verifyTextInDialog to control and verifyStringContains to control.
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`, `gp1`, `gp2`, `gp3`) ");
        b.append("VALUES ('ACTION','manageDialog',200,12,'manageDialog',NULL,NULL,NULL), ");
        b.append(" ('CONTROL', 'verifyTextInDialog', 80, 13, 'verifyTextInDialog', NULL, NULL, NULL), ");
        b.append(" ('CONTROL', 'verifyStringContains', 14, 13, 'verifyStringContains', NULL, NULL, NULL);");
        a.add(b.toString());

// Renamed value to value1 and added value2 in testcasecountryproperties and testcaseexecutiondata tables      
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasecountryproperties` CHANGE COLUMN `Value` `Value1` VARCHAR(2500) NULL DEFAULT '' ,");
        b.append("ADD COLUMN `Value2` VARCHAR(2500) NULL DEFAULT '' AFTER `Value1`;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcaseexecutiondata` CHANGE COLUMN `Object` `Value1` VARCHAR(3000) NULL DEFAULT NULL ,");
        b.append("ADD COLUMN `Value2` VARCHAR(3000) NULL DEFAULT NULL AFTER `Value1`");
        a.add(b.toString());

// Split IE browsers to IE9/IE10/IE11
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET `sort`='10' WHERE `id`='37' and`sort`='1';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET `sort`='20' WHERE `id`='37' and`sort`='3';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`, `VeryShortDesc`) ");
        b.append(" VALUES ('BROWSER', 'IE9', '30', '37', 'Internet Explorer 9 Browser', ''),");
        b.append("        ('BROWSER', 'IE10', '40', '37', 'Internet Explorer 10 Browser', ''),");
        b.append("        ('BROWSER', 'IE11', '50', '37', 'Internet Explorer 11 Browser', '');");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("DELETE FROM `invariant` WHERE `id`='37' and`sort`='2';");
        a.add(b.toString());

// Adding invariant for Public and private invariant.
// 434
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`, `VeryShortDesc`) ");
        b.append(" VALUES ('INVARIANTPRIVATE', 'ACTION', '10', '44', '', ''),");
        b.append("        ('INVARIANTPRIVATE', 'APPLITYPE', '20', '44', '', ''),");
        b.append("        ('INVARIANTPRIVATE', 'BROWSER', '30', '44', '', ''),");
        b.append("        ('INVARIANTPRIVATE', 'CHAIN', '40', '44', '', ''),");
        b.append("        ('INVARIANTPRIVATE', 'CONTROL', '50', '44', '', ''),");
        b.append("        ('INVARIANTPRIVATE', 'CTRLFATAL', '70', '44', '', ''),");
        b.append("        ('INVARIANTPRIVATE', 'ENVACTIVE', '80', '44', '', ''),");
        b.append("        ('INVARIANTPRIVATE', 'ENVTYPE', '100', '44', '', ''),");
        b.append("        ('INVARIANTPRIVATE', 'GROUP', '130', '44', '', ''),");
        b.append("        ('INVARIANTPRIVATE', 'OUTPUTFORMAT', '170', '44', '', ''),");
        b.append("        ('INVARIANTPRIVATE', 'PROPERTYNATURE', '220', '44', '', ''),");
        b.append("        ('INVARIANTPRIVATE', 'PROPERTYTYPE', '230', '44', '', ''),");
        b.append("        ('INVARIANTPRIVATE', 'RUNPROD', '260', '44', '', ''),");
        b.append("        ('INVARIANTPRIVATE', 'RUNQA', '270', '44', '', ''),");
        b.append("        ('INVARIANTPRIVATE', 'RUNUAT', '280', '44', '', ''),");
        b.append("        ('INVARIANTPRIVATE', 'SCREENSHOT', '290', '44', '', ''),");
        b.append("        ('INVARIANTPRIVATE', 'SERVER', '300', '44', '', ''),");
        b.append("        ('INVARIANTPRIVATE', 'SESSION', '310', '44', '', ''),");
        b.append("        ('INVARIANTPRIVATE', 'TCACTIVE', '340', '44', '', ''),");
        b.append("        ('INVARIANTPRIVATE', 'TCESTATUS', '350', '44', '', ''),");
        b.append("        ('INVARIANTPRIVATE', 'TCREADONLY', '360', '44', '', ''),");
        b.append("        ('INVARIANTPRIVATE', 'USERGROUP', '390', '44', '', ''),");
        b.append("        ('INVARIANTPRIVATE', 'VERBOSE', '400', '44', '', ''),");
        b.append("        ('INVARIANTPRIVATE', 'INVARIANTPRIVATE', '410', '44', '', ''),");
        b.append("        ('INVARIANTPRIVATE', 'INVARIANTPUBLIC', '420', '44', '', ''),");
        b.append("        ('INVARIANTPUBLIC', 'COUNTRY', '60', '45', '', ''),");
        b.append("        ('INVARIANTPUBLIC', 'ENVIRONMENT', '90', '45', '', ''),");
        b.append("        ('INVARIANTPUBLIC', 'EXECNBMIN', '110', '45', '', ''),");
        b.append("        ('INVARIANTPUBLIC', 'FILTERNBDAYS', '120', '45', '', ''),");
        b.append("        ('INVARIANTPUBLIC', 'MAXEXEC', '140', '45', '', ''),");
        b.append("        ('INVARIANTPUBLIC', 'ORIGIN', '160', '45', '', ''),");
        b.append("        ('INVARIANTPUBLIC', 'PRIORITY', '180', '45', '', ''),");
        b.append("        ('INVARIANTPUBLIC', 'PROPERTYDATABASE', '210', '45', '', ''),");
        b.append("        ('INVARIANTPUBLIC', 'SYSTEM', '330', '45', '', ''),");
        b.append("        ('INVARIANTPUBLIC', 'TCSTATUS', '370', '45', '', ''),");
        b.append("        ('INVARIANTPUBLIC', 'TEAM', '380', '45', '', '');");
        a.add(b.toString());

// Removing id column to invariant table.
// 435
        b = new StringBuilder();
        b.append("ALTER TABLE `invariant` DROP PRIMARY KEY , DROP COLUMN `id` , CHANGE COLUMN `sort` `sort` INT(10) NOT NULL DEFAULT 0 ");
        b.append(" , ADD PRIMARY KEY (`idname`, `value`) , ADD INDEX `IX_invariant_01` (`idname` ASC, `sort` ASC) ;");
        a.add(b.toString());

// Adding getFromSoap property type.
// 436
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`) ");
        b.append(" VALUES ('PROPERTYTYPE', 'executeSoapFromLib', '27', 'Getting from the SOAP request using the query');");
        a.add(b.toString());

// Adding table to host soaplibrary.
// 437
        b = new StringBuilder();
        b.append("CREATE TABLE `soaplibrary` (");
        b.append("  `Name` VARCHAR(45) ,");
        b.append("  `Type` VARCHAR(45) ,");
        b.append("  `ServicePath` VARCHAR(250) ,");
        b.append("  `Method` VARCHAR(45) ,");
        b.append("  `Envelope` TEXT ,");
        b.append("  `ParsingAnswer` TEXT ,");
        b.append("  `Description` VARCHAR(1000) ,");
        b.append("  PRIMARY KEY (`Name`)");
        b.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8");
        a.add(b.toString());

// Adding Project Active Combo invariant.
// 438
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`) ");
        b.append(" VALUES ('INVARIANTPRIVATE', 'PROJECTACTIVE', '21', ''),");
        b.append("        ('PROJECTACTIVE', 'Y', '10', 'Active'),");
        b.append("        ('PROJECTACTIVE', 'N', '20', 'Disable');");
        a.add(b.toString());

// New updated Documentation.
// 439
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());

// Chenged project constrain on testcase table so that in case a project is removed, the testcases are not removed.
// 441
        b = new StringBuilder();
        b.append("ALTER TABLE `testcase` DROP FOREIGN KEY `FK_testcase_03` ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcase` ADD CONSTRAINT `FK_testcase_03` FOREIGN KEY (`Project`) REFERENCES `project` (`idproject`) ON DELETE SET NULL ON UPDATE CASCADE;");
        a.add(b.toString());

// Added Description column in TestData Table
// 443
        b = new StringBuilder();
        b.append("ALTER TABLE `testdata` ADD COLUMN `Description` VARCHAR(1000) NULL DEFAULT ''  AFTER `value` ;");
        a.add(b.toString());

//Add parameters for the enabling or disabling the logs of public calls.
// 444
        b = new StringBuilder();
        b.append("INSERT INTO `parameter` (`system`, `param`, `value`, `description`) VALUES ");
        b.append("('', 'cerberus_log_publiccalls', 'N', 'Enable [Y] or Disable [N] the loging of all the calls done to Cerberus public servlets.')");
        a.add(b.toString());

//Removed unused logglassfish table.
// 445
        b = new StringBuilder();
        b.append("DROP TABLE `logglassfish`;");
        a.add(b.toString());

//Add invariant getAttributeFromHtml.
// 446
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) VALUES ('PROPERTYTYPE', 'getAttributeFromHtml', '38', 'Getting Attribute value from an HTML field in the current page.', '');");
        a.add(b.toString());

//Add documentation for new type of property getAttributeFromHtml.
// 447
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());

//Add browser in an index of testcaseexecution table.
// 448
        b = new StringBuilder();
        b.append("ALTER TABLE `testcaseexecution` DROP INDEX `IX_testcaseexecution_04` ,ADD INDEX `IX_testcaseexecution_04` (`Test` ASC, `TestCase` ASC, `Country` ASC, `Browser` ASC, `Start` ASC, `ControlStatus` ASC);");
        a.add(b.toString());

//Add Campaing management tables.
// 449
        b = new StringBuilder();
        b.append("CREATE TABLE `testbattery` (");
        b.append("  `testbatteryID` int(10) unsigned NOT NULL AUTO_INCREMENT,");
        b.append("  `testbattery` varchar(45) NOT NULL,");
        b.append("  `Description` varchar(300) NOT NULL DEFAULT '',");
        b.append("  PRIMARY KEY (`testbatteryID`),");
        b.append("  UNIQUE KEY `IX_testbattery_01` (`testbattery`)");
        b.append(") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;");
        a.add(b.toString());

// 450
        b = new StringBuilder();
        b.append("CREATE TABLE `testbatterycontent` (");
        b.append("  `testbatterycontentID` int(10) unsigned NOT NULL AUTO_INCREMENT,");
        b.append("  `testbattery` varchar(45) NOT NULL,");
        b.append("  `Test` varchar(45) NOT NULL,");
        b.append("  `TestCase` varchar(45) NOT NULL,");
        b.append("  PRIMARY KEY (`testbatterycontentID`),");
        b.append("  UNIQUE KEY `IX_testbatterycontent_01` (`testbattery`, `Test`, `TestCase`),");
        b.append("  KEY `IX_testbatterycontent_02` (`testbattery`),");
        b.append("  KEY `IX_testbatterycontent_03` (`Test`, `TestCase`),");
        b.append("  CONSTRAINT `FK_testbatterycontent_01` FOREIGN KEY (`testbattery`) REFERENCES `testbattery` (`testbattery`) ON DELETE CASCADE ON UPDATE CASCADE,");
        b.append("  CONSTRAINT `FK_testbatterycontent_02` FOREIGN KEY (`Test`,`TestCase`) REFERENCES `testcase` (`Test`,`TestCase`) ON DELETE CASCADE ON UPDATE CASCADE");
        b.append(") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;");
        a.add(b.toString());

// 451
        b = new StringBuilder();
        b.append("CREATE TABLE `campaign` (");
        b.append("  `campaignID` int(10) unsigned NOT NULL AUTO_INCREMENT,");
        b.append("  `campaign` varchar(45) NOT NULL,");
        b.append("  `Description` varchar(300) NOT NULL DEFAULT '',");
        b.append("  PRIMARY KEY (`campaignID`),");
        b.append("  UNIQUE KEY `IX_campaign_01` (`campaign`)");
        b.append(") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;");
        a.add(b.toString());

// 452
        b = new StringBuilder();
        b.append("CREATE TABLE `campaignparameter` (");
        b.append("  `campaignparameterID` int(10) unsigned NOT NULL AUTO_INCREMENT,");
        b.append("  `campaign` varchar(45) NOT NULL,");
        b.append("  `Parameter` varchar(100) NOT NULL,");
        b.append("  `Value` varchar(100) NOT NULL,");
        b.append("  PRIMARY KEY (`campaignparameterID`),");
        b.append("  UNIQUE KEY `IX_campaignparameter_01` (`campaign`, `Parameter`),");
        b.append("  KEY `IX_campaignparameter_02` (`campaign`),");
        b.append("  CONSTRAINT `FK_campaignparameter_01` FOREIGN KEY (`campaign`) REFERENCES `campaign` (`campaign`) ON DELETE CASCADE ON UPDATE CASCADE");
        b.append(") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;");
        a.add(b.toString());

// 453
        b = new StringBuilder();
        b.append("CREATE TABLE `campaigncontent` (");
        b.append("  `campaigncontentID` int(10) unsigned NOT NULL AUTO_INCREMENT,");
        b.append("  `campaign` varchar(45) NOT NULL,");
        b.append("  `testbattery` varchar(45) NOT NULL,");
        b.append("  PRIMARY KEY (`campaigncontentID`),");
        b.append("  UNIQUE KEY `IX_campaigncontent_01` (`campaign`, `testbattery`),");
        b.append("  KEY `IX_campaigncontent_02` (`campaign`),");
        b.append("  CONSTRAINT `FK_campaigncontent_01` FOREIGN KEY (`campaign`) REFERENCES `campaign` (`campaign`) ON DELETE CASCADE ON UPDATE CASCADE,");
        b.append("  CONSTRAINT `FK_campaigncontent_02` FOREIGN KEY (`testbattery`) REFERENCES `testbattery` (`testbattery`) ON DELETE CASCADE ON UPDATE CASCADE");
        b.append(") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;");
        a.add(b.toString());

//Add Database inside SQL Library table.
// 454
        b = new StringBuilder();
        b.append("ALTER TABLE `sqllibrary` CHANGE COLUMN `Name` `Name` VARCHAR(45) NOT NULL FIRST,");
        b.append("  ADD COLUMN `Database` VARCHAR(45) NULL DEFAULT '' AFTER `Type` ;");
        a.add(b.toString());

//Create table Robot.
// 455
        b = new StringBuilder();
        b.append("CREATE TABLE `robot` (");
        b.append("`robotID` int(10) NOT NULL AUTO_INCREMENT,");
        b.append("`robot` varchar(100) NOT NULL,");
        b.append("`host` varchar(150) NOT NULL DEFAULT '',");
        b.append("`port` varchar(20) NOT NULL DEFAULT '',");
        b.append("`platform` varchar(45) NOT NULL DEFAULT '',");
        b.append("`browser` varchar(45) NOT NULL DEFAULT '',");
        b.append("`version` varchar(45) NOT NULL DEFAULT '',");
        b.append("`active` varchar(1) NOT NULL DEFAULT 'Y',");
        b.append("`description` varchar(250) NOT NULL DEFAULT '',");
        b.append(" PRIMARY KEY (`robotID`),");
        b.append(" UNIQUE KEY IX_robot_01 (`robot`)");
        b.append(") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;");
        a.add(b.toString());

//Update User set null value of defaultIP to empty.
// 456
        b = new StringBuilder();
        b.append("UPDATE `user` ");
        b.append("SET `DefaultIP`='' where `DefaultIP` IS NULL;");
        a.add(b.toString());

//Modify User table adding robot preferences.
// 457
        b = new StringBuilder();
        b.append("ALTER TABLE `user` ");
        b.append("CHANGE COLUMN `DefaultIP` `robotHost` VARCHAR(150) NOT NULL DEFAULT '',");
        b.append("ADD COLUMN `robotPort` VARCHAR(20) NOT NULL DEFAULT '' AFTER `robotHost`,");
        b.append("ADD COLUMN `robotPlatform` VARCHAR(45) NOT NULL DEFAULT '' AFTER `robotPort`,");
        b.append("ADD COLUMN `robotBrowser` VARCHAR(45) NOT NULL DEFAULT '' AFTER `robotPlatform`,");
        b.append("ADD COLUMN `robotVersion` VARCHAR(45) NOT NULL DEFAULT '' AFTER `robotBrowser`, ");
        b.append("ADD COLUMN `robot` VARCHAR(100) NOT NULL DEFAULT '' AFTER `robotVersion`;");
        a.add(b.toString());

//Insert Platform invariant.
// 458
        //TODO Add private invariant
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` ");
        b.append("(`idname`,`value`,`sort`,`description`,`VeryShortDesc`,`gp1`,`gp2`,`gp3`) VALUES  ");
        b.append("('PLATFORM','LINUX',30,'Linux Platform','',NULL,NULL,NULL),");
        b.append("('PLATFORM','MAC',40,'Mac Platform','',NULL,NULL,NULL),");
        b.append("('PLATFORM','WINDOWS',70,'Windows Platform','',NULL,NULL,NULL),");
        b.append("('PLATFORM','ANDROID',10,'Android Platform','',NULL,NULL,NULL),");
        b.append("('PLATFORM','UNIX',50,'Unix Platform','',NULL,NULL,NULL),");
        b.append("('PLATFORM','VISTA',60,'Windows Vista Platform','',NULL,NULL,NULL),");
        b.append("('PLATFORM','WIN8',80,'Windows 8 Platform','',NULL,NULL,NULL),");
        b.append("('PLATFORM','XP',90,'Windows XP Platform','',NULL,NULL,NULL),");
        b.append("('BROWSER','IE',20,'Internet Explorer Browser','',NULL,NULL,NULL),");
        b.append("('BROWSER','android',70,'Android browser','',NULL,NULL,NULL),");
        b.append("('BROWSER','ipad',80,'ipad browser','',NULL,NULL,NULL),");
        b.append("('BROWSER','iphone',90,'iphone browser','',NULL,NULL,NULL),");
        b.append("('BROWSER','opera',60,'Opera browser','',NULL,NULL,NULL),");
        b.append("('BROWSER','safari',60,'Safari browser','',NULL,NULL,NULL),");
        b.append("('ROBOTACTIVE','N',2,'Disable','',NULL,NULL,NULL),");
        b.append("('ROBOTACTIVE','Y',1,'Active','',NULL,NULL,NULL),");
        b.append("('INVARIANTPRIVATE','ROBOTACTIVE',430,'','',NULL,NULL,NULL),");
        b.append("('INVARIANTPRIVATE','PLATFORM','35','','',NULL,NULL,NULL);");
        a.add(b.toString());

//DELETE old browser invariant.
// 459
        b = new StringBuilder();
        b.append("DELETE FROM `invariant` where `idname`='BROWSER' and `value` in ('IE9','IE10','IE11');");
        a.add(b.toString());

//Add Version and Platform column in testcaseExecution table.
// 460
        b = new StringBuilder();
        b.append("ALTER TABLE `testcaseexecution` ");
        b.append("ADD COLUMN `Version` VARCHAR(20) NOT NULL DEFAULT '' AFTER `Browser`,");
        b.append("ADD COLUMN `Platform` VARCHAR(20) NOT NULL DEFAULT '' AFTER `Version`,");
        b.append("CHANGE COLUMN `IP` `IP` VARCHAR(150) NULL DEFAULT NULL ;");
        a.add(b.toString());

//Insert Default Robot.
// 461
        b = new StringBuilder();
        b.append("INSERT INTO `robot` (`robot` ,`host` ,`port` ,`platform` ,`browser` ,`version` , `active` ,`description`)");
        b.append("VALUES ('MyRobot', '127.0.0.1', '4444', 'LINUX', 'firefox', '28', 'Y', 'My Robot');");
        a.add(b.toString());

//Insert parameter cerberus_picture_testcase_path.
// 462
        b = new StringBuilder();
        b.append("INSERT INTO `parameter` (`system`, `param`, `value`, `description`) VALUES ");
        b.append("('', 'cerberus_picture_testcase_path', '', 'Path to store the Cerberus Value and HowTo pictures of TestCase page');");
        a.add(b.toString());

//Change IP on countryEnvironmentParameters accordingly to other tables (user, testcaseexecution and robot).
// 463        
        b = new StringBuilder();
        b.append("ALTER TABLE `countryenvironmentparameters` ");
        b.append("CHANGE COLUMN `IP` `IP` VARCHAR(150) NOT NULL DEFAULT '';");
        a.add(b.toString());

//Add Invariant for campaign parameters.
// 464 
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`, `gp1`) VALUES ");
        b.append("('CAMPAIGN_PARAMETER', 'BROWSER', '10', 'Browser use to execute campaign', 'Browser', 'INVARIANTPRIVATE'),");
        b.append("('CAMPAIGN_PARAMETER', 'COUNTRY', '20', 'Country selected for campaign', 'Country', 'INVARIANTPUBLIC'),");
        b.append("('CAMPAIGN_PARAMETER', 'ENVIRONMENT', '30', 'Which environment used to execute campaign', 'Environment', 'INVARIANTPUBLIC'),");
        b.append("('INVARIANTPRIVATE','CAMPAIGN_PARAMETER','440','','',NULL);");
        a.add(b.toString());

//Add Invariant for new control verify element in element.
// 465 
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`, `gp1`, `gp2`, `gp3`) VALUES ");
        b.append("('CONTROL', 'verifyElementInElement', 32, 'verifyElementInElement', '', NULL, NULL, NULL);");
        a.add(b.toString());

//Add Documentation for new control verify element in element.
// 466 
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());

//Remove null on each field of countryenvparam.
// 467 >  477
        b = new StringBuilder();
        b.append("update `countryenvparam` set  `Build` = '' where `Build` is null;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("update `countryenvparam` set  `Revision` = '' where `Revision` is null;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("update `countryenvparam` set  `Chain` = '' where `Chain` is null;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("update `countryenvparam` set  `DistribList` = '' where `DistribList` is null;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("update `countryenvparam` set  `EMailBodyRevision` = '' where `EMailBodyRevision` is null;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("update `countryenvparam` set  `Type` = '' where `Type` is null;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("update `countryenvparam` set  `EMailBodyChain` = '' where `EMailBodyChain` is null;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("update `countryenvparam` set  `EMailBodyDisableEnvironment` = '' where `EMailBodyDisableEnvironment` is null;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("update `countryenvparam` set  `maintenanceact` = '' where `maintenanceact` is null;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("update `countryenvparam` set  `maintenanceend` = '0' where `maintenanceend` is null;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("update `countryenvparam` set `maintenancestr` = '0' where `maintenancestr` is null;");
        a.add(b.toString());

//Alter table countryenvparam to put default value empty instead of NULL
// 478
        b = new StringBuilder();
        b.append("ALTER TABLE `countryenvparam`");
        b.append("CHANGE COLUMN `Build` `Build` VARCHAR(10) NOT NULL DEFAULT '' ,");
        b.append("CHANGE COLUMN `Revision` `Revision` VARCHAR(20) NOT NULL DEFAULT '' ,");
        b.append("CHANGE COLUMN `Chain` `Chain` VARCHAR(20) NOT NULL DEFAULT '' ,");
        b.append("CHANGE COLUMN `DistribList` `DistribList` TEXT NOT NULL ,");
        b.append("CHANGE COLUMN `EMailBodyRevision` `EMailBodyRevision` TEXT NOT NULL  ,");
        b.append("CHANGE COLUMN `Type` `Type` VARCHAR(20) NOT NULL DEFAULT '' ,");
        b.append("CHANGE COLUMN `EMailBodyChain` `EMailBodyChain` TEXT NOT NULL ,");
        b.append("CHANGE COLUMN `EMailBodyDisableEnvironment` `EMailBodyDisableEnvironment` TEXT NOT NULL ,");
        b.append("CHANGE COLUMN `maintenanceact` `maintenanceact` VARCHAR(1) NOT NULL DEFAULT 'N' ,");
        b.append("CHANGE COLUMN `maintenancestr` `maintenancestr` TIME NOT NULL DEFAULT 0 ,");
        b.append("CHANGE COLUMN `maintenanceend` `maintenanceend` TIME NOT NULL DEFAULT 0 ;");
        a.add(b.toString());

//Alter table countryenvparam to put default value empty instead of NULL
// 479       
        b = new StringBuilder();
        b.append("ALTER TABLE `campaignparameter` DROP INDEX `IX_campaignparameter_01` , ");
        b.append("ADD UNIQUE INDEX `IX_campaignparameter_01` (`campaign` ASC, `Parameter` ASC, `Value` ASC);");
        a.add(b.toString());

//Add invariant action openURL
// 480       
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` ");
        b.append(" VALUES ('ACTION', 'openUrl', '65', 'openUrl', '', NULL,NULL,NULL);");
        a.add(b.toString());

//Add documentation related to action openURL
// 481       
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());

//Add function column in testcase table
// 482      
        b = new StringBuilder();
        b.append("ALTER TABLE `testcase` ");
        b.append("ADD COLUMN `function` VARCHAR(500) NULL DEFAULT '' AFTER `activePROD`;");
        a.add(b.toString());

//Add documentation for function column in testcase table
// 483      
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());

//Add parameter for management of function column in testcase table
// 484-485
        b = new StringBuilder();
        b.append("INSERT INTO `parameter` (`system`, `param`, `value`, `description`) VALUES ");
        b.append("('', 'cerberus_testcase_function_urlForListOfFunction', '/URL/TO/FUNCTION/SERVICE', 'URL to feed the function field with proposal for autocompletion. URL should respond JSON format');");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("INSERT INTO `parameter` (`system`, `param`, `value`, `description`) VALUES ");
        b.append("('', 'cerberus_testcase_function_booleanListOfFunction', 'N', 'boolean to activate autocompletion on function fields.');");
        a.add(b.toString());

//Add documentation for timeout and synchroneous field
// 486      
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());

//Add invariant synchroneous
// 487      
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` VALUES ");
        b.append(" ('SYNCHRONEOUS', 'N', '2', 'Redirect to the execution before the end of the execution', '', NULL, NULL, NULL),");
        b.append(" ('SYNCHRONEOUS', 'Y', '1', 'Redirect to the execution after the end of the execution', '', NULL, NULL, NULL);");
        a.add(b.toString());

//Add invariant private synchroneous
// 488       
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) VALUES ('INVARIANTPRIVATE', 'SYNCHRONEOUS', '430', '', '');");
        a.add(b.toString());

//Add invariant action callSoapWithBase
// 489       
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) VALUES ('ACTION', 'callSoapWithBase', '190', 'callSoapWithBase', '');");
        a.add(b.toString());

//Add invariant CONTROL verifyXmlTreeStructure
// 490       
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) VALUES ('CONTROL', 'verifyXmlTreeStructure', '90', 'verifyXmlTreeStructure', '');");
        a.add(b.toString());

//Add invariant action mouseDownMouseUp
// 491       
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) VALUES ('ACTION', 'mouseDownMouseUp', '200', 'mouseDownMouseUp', '');");
        a.add(b.toString());

//Update documentation for new properties %SYS_TODAY-doy% and %SYS_YESTERDAY-doy%
// 492
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());

//Add use step columns in testcasestep
// 493       
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestep` ");
        b.append("ADD COLUMN `useStep` VARCHAR(1) NULL DEFAULT 'N' AFTER `Description`, ");
        b.append("ADD COLUMN `useStepTest` VARCHAR(45) NOT NULL DEFAULT '' AFTER `useStep`, ");
        b.append("ADD COLUMN `useStepTestCase` VARCHAR(45) NOT NULL DEFAULT '' AFTER `useStepTest`, ");
        b.append("ADD COLUMN `useStepStep` INT(10) NOT NULL  AFTER `useStepTestCase`; ");
        a.add(b.toString());

//Add control isElementClickable and isElementNotClickable
// 494       
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) VALUES ");
        b.append(" ('CONTROL','verifyElementClickable',35,'isElementClickable',''),");
        b.append(" ('CONTROL','verifyElementNotClickable',36,'isElementNotClickable','')");
        a.add(b.toString());

//Add documentation isElementClickable , isElementNotClickable, callSoapWithBase
// 495       
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());

//Remove mouseUpMouseDown
// 496       
        b = new StringBuilder();
        b.append("DELETE FROM `invariant` WHERE `idname`='ACTION' and `value`='mouseDownMouseUp';");
        a.add(b.toString());

//Update documentation for new properties %SYS_EXECUTIONID%
// 497
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());

//Add action getPageSource
// 498       
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) VALUES ('ACTION', 'getPageSource', '210', 'getPageSource', '');");
        a.add(b.toString());

//Add documentation getPageSource
// 499      
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());

//Add invariant callSoap and getFromXml
// 500      
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) VALUES ");
        b.append(" ('ACTION', 'callSoap', '189', 'callSoap', ''),");
        b.append(" ('PROPERTYTYPE', 'getFromXml', '50', 'getFromXml', '');");
        a.add(b.toString());

//Add documentation getFromXml
// 501      
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());

//Add documentation getFromCookie
// 502      
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());

//Add invariant getFromCookie
// 503      
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) VALUES ");
        b.append(" ('PROPERTYTYPE', 'getFromCookie', '60', 'getFromCookie', '');");
        a.add(b.toString());

//Add documentation seleniumLog and pageSource
// 504      
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());

//Add invariant seleniumLog and pageSource
// 505      
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) VALUES ");
        b.append(" ('INVARIANTPRIVATE', 'PAGESOURCE', '440', '', ''),");
        b.append(" ('INVARIANTPRIVATE', 'SELENIUMLOG', '450', '', ''),");
        b.append(" ('PAGESOURCE', '0', '10', 'Never get Page Source', ''),");
        b.append(" ('PAGESOURCE', '1', '20', 'Get Page Source on error only', ''),");
        b.append(" ('PAGESOURCE', '2', '30', 'Get Page Source after each action', ''),");
        b.append(" ('SELENIUMLOG', '0', '10', 'Never record Selenium Log', ''),");
        b.append(" ('SELENIUMLOG', '1', '20', 'Record Selenium Log on error only', ''),");
        b.append(" ('SELENIUMLOG', '2', '30', 'Record Selenium Log on testcase', '');");
        a.add(b.toString());

//Add PageSource filename on testcasestepactionexecution table
// 506      
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepactionexecution` ");
        b.append("ADD COLUMN `PageSourceFileName` VARCHAR(150) NULL DEFAULT NULL AFTER `ScreenshotFilename`;");
        a.add(b.toString());

//Add PageSource filename on testcasestepactioncontrolexecution table
// 507      
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepactioncontrolexecution` ");
        b.append("ADD COLUMN `PageSourceFilename` VARCHAR(150) NULL DEFAULT NULL AFTER `ScreenshotFilename`;");
        a.add(b.toString());

//Add Selenium Log in documentation
// 508      
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());

//Default value 0 for use step 
// 509      
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestep` ");
        b.append("CHANGE COLUMN `useStepStep` `useStepStep` INT(10) NOT NULL DEFAULT '0' ;");
        a.add(b.toString());

//Create table usersystem 
// 510   
        b = new StringBuilder();
        b.append("CREATE TABLE `usersystem` (");
        b.append("`Login` VARCHAR(10) NOT NULL,");
        b.append("`System` VARCHAR(45) NOT NULL,");
        b.append("PRIMARY KEY (`Login`, `System`))  ENGINE=InnoDB DEFAULT CHARSET=utf8;");
        a.add(b.toString());

//Create table usersystem 
// 511   
        b = new StringBuilder();
        b.append("insert into usersystem ");
        b.append("select u.login, i.value from user u, invariant i where i.idname='SYSTEM';");
        a.add(b.toString());

//Default value in sort on application table 
// 512   
        b = new StringBuilder();
        b.append("ALTER TABLE `application` ");
        b.append("CHANGE COLUMN `sort` `sort` INT(11) NOT NULL DEFAULT 10 ;");
        a.add(b.toString());

//Add application type WS
// 513
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`) VALUES ");
        b.append("('APPLITYPE', 'WS', '30', 'Web Service Application');");
        a.add(b.toString());

//Add executor (user login or selenium) in testcaseexecution table
// 514
        b = new StringBuilder();
        b.append("ALTER TABLE testcaseexecution ");
        b.append("ADD COLUMN `Executor` VARCHAR(10) NULL;");
        a.add(b.toString());

// New updated Documentation.
// 515
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());

// Add invariant getDifferencesFromXml.
// 516
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`) VALUES ");
        b.append("('PROPERTYTYPE', 'getDifferencesFromXml', '51', 'Get differences from XML files');");
        a.add(b.toString());

// Add invariant getDifferencesFromXml.
// 517
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`) VALUES ");
        b.append("('ACTION', 'removeDifference', '220', 'Remove differences from the given pattern');");
        a.add(b.toString());

// Add colums to use test data at application / environment / country level.
// 518
        b = new StringBuilder();
        b.append("ALTER TABLE `testdata` ");
        b.append("ADD COLUMN `Application` VARCHAR(45) NOT NULL DEFAULT '' AFTER `Description`,");
        b.append("ADD COLUMN `Country` VARCHAR(2) NOT NULL DEFAULT '' AFTER `Application`,");
        b.append("ADD COLUMN `Environment` VARCHAR(45) NOT NULL DEFAULT '' AFTER `Country`;");
        a.add(b.toString());

// Add update primary key to test data at key / application / environment / country level.
// 519
        b = new StringBuilder();
        b.append("ALTER TABLE `testdata` ");
        b.append("DROP PRIMARY KEY, ADD PRIMARY KEY (`key`, `Environment`, `Country`, `Application`);");
        a.add(b.toString());

// Increase soaplibray's Name column size from 45 to 255.
// 520
        b = new StringBuilder();
        b.append("ALTER TABLE `soaplibrary` ");
        b.append("CHANGE COLUMN `Name` `Name` VARCHAR(255) NOT NULL DEFAULT '' ;");
        a.add(b.toString());

// Increase soaplibray's Envelope column type from TEXT to MEDIUMTEXT.
// 521
        b = new StringBuilder();
        b.append("ALTER TABLE `soaplibrary` ");
        b.append("CHANGE COLUMN `Envelope` `Envelope` MEDIUMTEXT NULL DEFAULT NULL ;");
        a.add(b.toString());

// Add foreign key to usersystem table.
// 522-524
        b = new StringBuilder();
        b.append("DROP TABLE `usersystem` ;");
        a.add(b.toString());

        b = new StringBuilder();
        b.append("CREATE TABLE `usersystem` (");
        b.append("  `Login` VARCHAR(10) NOT NULL,");
        b.append("  `System` VARCHAR(45) NOT NULL,");
        b.append(" PRIMARY KEY (`Login`, `System`), ");
        b.append(" CONSTRAINT `FK_usersystem_01` FOREIGN KEY (`Login` ) REFERENCES `user` (`Login`) ON DELETE CASCADE ON UPDATE CASCADE ");
        b.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8 ;");
        a.add(b.toString());

        b = new StringBuilder();
        b.append("INSERT INTO usersystem ");
        b.append(" SELECT u.login, i.value FROM user u, invariant i WHERE i.idname='SYSTEM';");
        a.add(b.toString());

// Creating new tables for test data.
// 525-526
        b = new StringBuilder();
        b.append("CREATE TABLE `testdatalib` (");
        b.append("  `Name` varchar(200) NOT NULL,");
        b.append("  `system` varchar(45) NOT NULL DEFAULT '',");
        b.append("  `Environment` varchar(45) NOT NULL DEFAULT '',");
        b.append("  `Country` varchar(2) NOT NULL DEFAULT '',");
        b.append("  `Group` varchar(200) NOT NULL DEFAULT '',");
        b.append("  `Type` varchar(45) NOT NULL DEFAULT '',");
        b.append("  `Database` varchar(45) NOT NULL DEFAULT '',");
        b.append("  `Script` varchar(2500) NOT NULL DEFAULT '',");
        b.append("  `ServicePath` varchar(250) NOT NULL DEFAULT '',");
        b.append("  `Method` varchar(45) NOT NULL DEFAULT '',");
        b.append("  `Envelope` text,");
        b.append("  `Description` varchar(1000) NOT NULL DEFAULT '',");
        b.append("  PRIMARY KEY (`Name`,`system`,`Environment`,`Country`)");
        b.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8;");
        a.add(b.toString());

        b = new StringBuilder();
        b.append("CREATE TABLE `testdatalibdata` (");
        b.append("  `Name` varchar(200) NOT NULL,");
        b.append("  `system` varchar(45) NOT NULL DEFAULT '',");
        b.append("  `Environment` varchar(45) NOT NULL DEFAULT '',");
        b.append("  `Country` varchar(2) NOT NULL DEFAULT '',");
        b.append("  `SubData` varchar(200) NOT NULL DEFAULT '',");
        b.append("  `Value` text,");
        b.append("  `Column` varchar(255) NOT NULL DEFAULT '',");
        b.append("  `ParsingAnswer` text ,");
        b.append("  `Description` varchar(1000)  NOT NULL DEFAULT '',");
        b.append("  PRIMARY KEY (`Name`,`system`,`Environment`,`Country`,`SubData`),");
        b.append("  CONSTRAINT `FK_testdatalibdata_01` FOREIGN KEY (`Name`,`system`,`Environment`,`Country`) REFERENCES `testdatalib` (`Name`,`system`,`Environment`,`Country`) ON DELETE CASCADE ON UPDATE CASCADE");
        b.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8;");
        a.add(b.toString());

// Temporary init data.
// 527-532
        b = new StringBuilder();
        b.append("INSERT INTO testdatalib (`system`,`Country`,`Environment`,`Name`, `Type`, `Description`, `Envelope`) ");
        b.append(" SELECT '', `Country`, `Environment`, `key`, 'STATIC', IFNULL(td.`Description`,''), '' from testdata td");
        b.append(" ON DUPLICATE KEY UPDATE Description = IFNULL(td.`Description`,'');");
        a.add(b.toString());

        b = new StringBuilder();
        b.append("INSERT INTO testdatalibdata (`system`,`Country`,`Environment`,`Name`, `SubData`, `Value`, `Description`, `ParsingAnswer`) ");
        b.append(" SELECT '', `Country`, `Environment`, `key`, '', IFNULL(td.`value`,''), IFNULL(td.`Description`,''), '' from testdata td");
        b.append(" ON DUPLICATE KEY UPDATE `Value` = IFNULL(td.`value`,''), Description = IFNULL(td.`Description`,'');");
        a.add(b.toString());

        b = new StringBuilder();
        b.append("INSERT INTO testdatalib (`system`,`Country`,`Environment`,`Name`, `Group`, `Type`, `Database`, `Script`, `Description`, `Envelope`) ");
        b.append(" SELECT '', '', '', `Name`, IFNULL(`Type`,''), 'SQL', IFNULL(`Database`,''), IFNULL(`Script`,''), IFNULL(description,'') , '' from sqllibrary sl");
        b.append(" ON DUPLICATE KEY UPDATE `Group`=IFNULL(sl.Type,''), `Database`=IFNULL(sl.`Database`,''), `Script`=IFNULL(sl.`Script`,''), Description=IFNULL(sl.Description,'');");
        a.add(b.toString());

        b = new StringBuilder();
        b.append("INSERT INTO testdatalibdata (`system`,`Country`,`Environment`,`Name`, `SubData`, `Column`, `Description`, `ParsingAnswer`, `Value`) ");
        b.append(" SELECT '', '', '', `Name`, '', '', IFNULL(description,''), '', '' from sqllibrary sl");
        b.append(" ON DUPLICATE KEY UPDATE Description=IFNULL(sl.Description,'');");
        a.add(b.toString());

        b = new StringBuilder();
        b.append("INSERT INTO testdatalib (`system`,`Country`,`Environment`,`Name`, `Group`, `Type`, `ServicePath`, `Method`, `Envelope`, `Description`) ");
        b.append(" SELECT '', '', '', `Name`, IFNULL(`Type`,''), 'SOAP', IFNULL(`ServicePath`,''), IFNULL(`Method`,''), IFNULL(Envelope,''), IFNULL(description,'') from soaplibrary sl");
        b.append(" ON DUPLICATE KEY UPDATE `Group`=IFNULL(sl.Type,''), `ServicePath`=IFNULL(sl.`ServicePath`,''), `Method`=IFNULL(sl.`Method`,''), `Envelope`=IFNULL(sl.`Envelope`,''), Description=IFNULL(sl.Description,'');");
        a.add(b.toString());

        b = new StringBuilder();
        b.append("INSERT INTO testdatalibdata (`system`,`Country`,`Environment`,`Name`, `SubData`, `ParsingAnswer`, `Description`, `Value`) ");
        b.append(" SELECT '', '', '', `Name`, '', IFNULL(ParsingAnswer,''), IFNULL(description, ''), '' from soaplibrary sl");
        b.append(" ON DUPLICATE KEY UPDATE `ParsingAnswer`=IFNULL(sl.ParsingAnswer,''), Description=IFNULL(sl.Description,'');");
        a.add(b.toString());

// Creating invariant TESTDATATYPE.
// 533
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) VALUES ");
        b.append(" ('INVARIANTPRIVATE', 'TESTDATATYPE', '460', '', ''),");
        b.append(" ('TESTDATATYPE', 'STATIC', '10', 'Static test data.', ''),");
        b.append(" ('TESTDATATYPE', 'SQL', '20', 'Dynamic test data from SQL execution.', ''),");
        b.append(" ('TESTDATATYPE', 'SOAP', '30', 'Dynamic test data from SOAP Webservice call.', '');");
        a.add(b.toString());

// Creating technical id between testdatalib and testdatalibdata tables.
// 534-539
        b = new StringBuilder();
        b.append("ALTER TABLE `testdatalib` ");
        b.append("ADD COLUMN `TestDataLibID` INT UNSIGNED NOT NULL AUTO_INCREMENT FIRST,");
        b.append("DROP PRIMARY KEY,");
        b.append("ADD PRIMARY KEY (`TestDataLibID`),");
        b.append("ADD UNIQUE INDEX `IX_testdatalib_01` (`Name` ASC, `system` ASC, `Environment` ASC, `Country` ASC);");
        a.add(b.toString());

        b = new StringBuilder();
        b.append("ALTER TABLE `testdatalibdata` ");
        b.append("ADD COLUMN `TestDataLibID` INT UNSIGNED NOT NULL DEFAULT 0 FIRST;");
        a.add(b.toString());

        b = new StringBuilder();
        b.append("UPDATE `testdatalibdata` ld, `testdatalib` l SET ld.TestDataLibID=l.TestDataLibID");
        b.append(" WHERE ld.`Name`=l.`Name` and ld.`system`=l.`system` and ld.`Environment`=l.`Environment` and ld.`Country`=l.`Country`;");
        a.add(b.toString());

        b = new StringBuilder();
        b.append("ALTER TABLE `testdatalibdata` ");
        b.append("DROP FOREIGN KEY `FK_testdatalibdata_01`;");
        a.add(b.toString());

        b = new StringBuilder();
        b.append("ALTER TABLE `testdatalibdata` ");
        b.append("DROP COLUMN `Country`, DROP COLUMN `Environment`, DROP COLUMN `system`, DROP COLUMN `Name`, ");
        b.append("DROP PRIMARY KEY, ADD PRIMARY KEY (`TestDataLibID`, `SubData`);");
        a.add(b.toString());

        b = new StringBuilder();
        b.append("ALTER TABLE `testdatalibdata` ");
        b.append("ADD CONSTRAINT `FK_testdatalibdata_01`");
        b.append("  FOREIGN KEY (`TestDataLibID`)");
        b.append("  REFERENCES `testdatalib` (`TestDataLibID`) ON DELETE CASCADE ON UPDATE CASCADE;");
        a.add(b.toString());

// Cleaning EXECNBMIN invariant as not used anymore following filter on before date.
// 540
        b = new StringBuilder();
        b.append("DELETE FROM invariant where idname='EXECNBMIN' or (idname='INVARIANTPUBLIC' and value='EXECNBMIN');");
        a.add(b.toString());

//Update documentation for new properties %SYS_ELAPSED-EXESTART% and %SYS_ELAPSED-STEPSTART%
// 541
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());

//Removed Sla columns from testcaseexecution table
// 542
        b = new StringBuilder();
        b.append("ALTER TABLE `testcase` DROP COLUMN `Sla`;");
        a.add(b.toString());

// Resizing invariant table column.
// 543
        b = new StringBuilder();
        b.append("ALTER TABLE `invariant` ");
        b.append("CHANGE COLUMN `value` `value` VARCHAR(255) NOT NULL , ");
        b.append("CHANGE COLUMN `description` `description` VARCHAR(255) NOT NULL , ");
        b.append("CHANGE COLUMN `gp1` `gp1` VARCHAR(255) NULL DEFAULT NULL , ");
        b.append("CHANGE COLUMN `gp2` `gp2` VARCHAR(255) NULL DEFAULT NULL , ");
        b.append("CHANGE COLUMN `gp3` `gp3` VARCHAR(255) NULL DEFAULT NULL");
        a.add(b.toString());

// Insert new private invariant value for APPLITYPE
// 544
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) ");
        b.append("VALUES ('APPLITYPE', 'APK', '40', 'Android Application', '')");
        a.add(b.toString());

// Add column inlibrary in testcasestep table
// 545
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestep` ");
        b.append("ADD COLUMN `inlibrary` VARCHAR(1) NULL DEFAULT 'N' AFTER `useStepStep`;");
        a.add(b.toString());

// Add table testcaseexecutionqueue
// 546
        b = new StringBuilder();
        b.append("CREATE TABLE `testcaseexecutionqueue` (");
        b.append("  `ID` bigint(20) unsigned NOT NULL AUTO_INCREMENT,");
        b.append("  `Test` varchar(45) NOT NULL,");
        b.append("  `TestCase` varchar(45) NOT NULL,");
        b.append("  `Country` varchar(2) NOT NULL,");
        b.append("  `Environment` varchar(45) NOT NULL,");
        b.append("  `Robot` varchar(45) DEFAULT NULL,");
        b.append("  `RobotIP` varchar(150) DEFAULT NULL,");
        b.append("  `RobotPort` varchar(20) DEFAULT NULL,");
        b.append("  `Browser` varchar(45) NOT NULL,");
        b.append("  `BrowserVersion` varchar(20) DEFAULT NULL,");
        b.append("  `Platform` varchar(45) DEFAULT NULL,");
        b.append("  `ManualURL` tinyint(1) NOT NULL DEFAULT '0',");
        b.append("  `ManualHost` varchar(255) DEFAULT NULL,");
        b.append("  `ManualContextRoot` varchar(255) DEFAULT NULL,");
        b.append("  `ManualLoginRelativeURL` varchar(255) DEFAULT NULL,");
        b.append("  `ManualEnvData` varchar(255) DEFAULT NULL,");
        b.append("  `Tag` varchar(255) NOT NULL,");
        b.append("  `OutputFormat` varchar(20) NOT NULL DEFAULT 'gui',");
        b.append("  `Screenshot` int(11) NOT NULL DEFAULT '0',");
        b.append("  `Verbose` int(11) NOT NULL DEFAULT '0',");
        b.append("  `Timeout` mediumtext,");
        b.append("  `Synchroneous` tinyint(1) NOT NULL DEFAULT '0',");
        b.append("  `PageSource` int(11) NOT NULL DEFAULT '1',");
        b.append("  `SeleniumLog` int(11) NOT NULL DEFAULT '1',");
        b.append("  `RequestDate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,");
        b.append("  `Proceeded` tinyint(1) NOT NULL DEFAULT '0',");
        b.append("  PRIMARY KEY (`ID`),");
        b.append("  KEY `IX_testcaseexecution_01` (`Test`,`TestCase`,`Country`),");
        b.append("  KEY `IX_testcaseexecution_02` (`Tag`),");
        b.append("  CONSTRAINT `FK_testcaseexecutionqueue_01` FOREIGN KEY (`Test`, `TestCase`, `Country`) REFERENCES `testcasecountry` (`Test`, `TestCase`, `Country`) ON DELETE CASCADE ON UPDATE CASCADE");
        b.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8;");
        a.add(b.toString());

// Add documentation for ManualExecution parameter in run page
// 547
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());

// Add invariant ManualExecution
// 548    	
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) VALUES ");
        b.append("('INVARIANTPRIVATE', 'MANUALEXECUTION', '470', '', ''),");
        b.append("('MANUALEXECUTION', 'Y', '2', 'Manual Execution', ''),");
        b.append("('MANUALEXECUTION', 'N', '1', 'Automatic Execution', '');");
        a.add(b.toString());

// Add Start index on execution table in order to speedup purge process.
// 549 552    	
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepactioncontrolexecution` ADD INDEX `IX_testcasestepactioncontrolexecution_01` (`Start` ASC);");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepactionexecution` ADD INDEX `IX_testcasestepactionexecution_01` (`Start` ASC);");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepexecution` ADD INDEX `IX_testcasestepexecution_01` (`Start` ASC);");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcaseexecutionwwwdet` ADD INDEX `IX_testcaseexecutionwwwdet_01` (`Start` ASC);");
        a.add(b.toString());

// Add Invariant for new control verify element is equal to another.
// 553
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`) VALUES ");
        b.append("('CONTROL', 'verifyElementEquals', 44, 'verifyElementEquals');");
        a.add(b.toString());

// Update control verify element is equal to another sorting.
// 554
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET `sort`='32' WHERE `idname`='CONTROL' and`value`='verifyElementEquals';");
        a.add(b.toString());

// Add invariant for new controls verifyElementDifferent, verifyIntegerEquals and verifyIntegerDifferent.
// 555
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`) VALUES ");
        b.append("('CONTROL', 'verifyElementDifferent', 33, 'verifyElementDifferent'),");
        b.append("('CONTROL', 'verifyIntegerEquals', 18, 'verifyIntegerEquals'),");
        b.append("('CONTROL', 'verifyIntegerDifferent', 19, 'verifyIntegerDifferent');");
        a.add(b.toString());

//Add documentation for new previously added controls.
// 556
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());

// Increase soaplibrary's Method column size to 255 characters.
// 557
        b = new StringBuilder();
        b.append("ALTER TABLE `soaplibrary` CHANGE COLUMN `Method` `Method` VARCHAR(255) NULL DEFAULT NULL ;");
        a.add(b.toString());

// Add Invariant for new control verify text not in element.
//  558
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`) VALUES ");
        b.append("('CONTROL', 'verifyTextNotInElement', 41, 'verifyTextNotInElement');");
        a.add(b.toString());

// Add documentation for new previously added controls.
//  559
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());

// Add ReturnMessage on stepExecution table.
//  560
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepexecution` ");
        b.append(" ADD COLUMN `ReturnMessage` VARCHAR(500) NULL DEFAULT NULL AFTER `ReturnCode`;");
        a.add(b.toString());

// Add last_updaed column.
//  561 >> 567                
        b = new StringBuilder();
        b.append("ALTER TABLE `test` ");
        b.append("ADD COLUMN `last_modified` TIMESTAMP NULL DEFAULT 0;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcase` ");
        b.append("ADD COLUMN `last_modified` TIMESTAMP NULL DEFAULT 0;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasecountry` ");
        b.append("ADD COLUMN `last_modified` TIMESTAMP NULL DEFAULT 0;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasecountryproperties` ");
        b.append("ADD COLUMN `last_modified` TIMESTAMP NULL DEFAULT 0;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestep` ");
        b.append("ADD COLUMN `last_modified` TIMESTAMP NULL DEFAULT 0;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepaction` ");
        b.append("ADD COLUMN `last_modified` TIMESTAMP NULL DEFAULT 0;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepactioncontrol` ");
        b.append("ADD COLUMN `last_modified` TIMESTAMP NULL DEFAULT 0;");
        a.add(b.toString());

// Add ScreenshotFilename column on testcasestepaction and testcasestepactioncontrol tables.
//  568 >> 569                
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepaction` ");
        b.append("ADD COLUMN `ScreenshotFileName` VARCHAR(150) NULL DEFAULT NULL AFTER `Description`;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepactioncontrol` ");
        b.append("ADD COLUMN `ScreenshotFileName` VARCHAR(150) NULL DEFAULT NULL AFTER `Fatal`;");
        a.add(b.toString());

// Add propertytype getFromJson in Invariant table.
//  570 
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) ");
        b.append(" VALUES ('PROPERTYTYPE', 'getFromJson', '70', 'Getting value from a Json file', '');");
        a.add(b.toString());

// Add documentation for getFromJson property.
//  571 
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());

// Add Solr_url parameter.
//  572 
        b = new StringBuilder();
        b.append("INSERT INTO `parameter` (`system`, `param`,`value`, `description`) VALUES ('', 'solr_url','', 'URL of Solr search Engine used on Search Testcase Page. Value is empty if no Solr implementation is available');");
        a.add(b.toString());

// Add Thread Pool Size parameter.
//  573                
        b = new StringBuilder();
        b.append("INSERT INTO `parameter` (`system`, `param`, `value`, `description`) VALUES ('', 'cerberus_execution_threadpool_size', '10', 'Number of Simultaneous execution handled by Cerberus');");
        a.add(b.toString());

// Add Column Comment,Retries and manualExecution in TestCaseExecutionQueue table.
//  574                
        b = new StringBuilder();
        b.append("ALTER TABLE `testcaseexecutionqueue` ");
        b.append("ADD COLUMN `comment` VARCHAR(250) NULL DEFAULT NULL AFTER `proceeded`, ");
        b.append("ADD COLUMN `retries` TINYINT(1) NOT NULL DEFAULT '0' AFTER `comment`,");
        b.append("ADD COLUMN `manualexecution` VARCHAR(1) NOT NULL DEFAULT 'N' AFTER `retries`;");
        a.add(b.toString());

// Add Column Comment in TestCaseExecutionQueue table.
//  575                
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) VALUES ('OUTPUTFORMAT', 'redirectToReport', '4', 'Go to ReportByTag page', '');");
        a.add(b.toString());

// Add Column Comment in TestCaseExecutionQueue table.
//  576               
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());

// Add Column Comment in TestCaseExecutionQueue table.
//  577               
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) VALUES ");
        b.append("('INVARIANTPRIVATE', 'RETRIES', '470', '', ''), ");
        b.append("('RETRIES', '0', '10', 'Do not retry in case of Not OK', ''), ");
        b.append("('RETRIES', '1', '20', 'Retry 1 time in case of Not OK', ''), ");
        b.append("('RETRIES', '2', '30', 'Retry 2 times in case of Not OK', ''), ");
        b.append("('RETRIES', '3', '40', 'Retry 3 times in case of Not OK', '');");
        a.add(b.toString());

// Add Column UserAgent in Robot Table.
//  578               
        b = new StringBuilder();
        b.append("ALTER TABLE `robot` ");
        b.append("ADD COLUMN `useragent` VARCHAR(250) NOT NULL DEFAULT '' AFTER `active`;");
        a.add(b.toString());

// Add Column Domain in countryenvironmentparameters Table.
//  579 -> 581
        b = new StringBuilder();
        b.append("ALTER TABLE `countryenvironmentparameters` ");
        b.append("ADD COLUMN `domain` VARCHAR(150) NOT NULL DEFAULT '' AFTER `IP`;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());

// Add new property type that is able to retrieve data values from a property that is specified in the library
//  582-583               
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) ");
        b.append(" VALUES ('PROPERTYTYPE', 'getFromDataLib', '75', 'Determines the data value associated with a library entry', 'Data value'); ");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`) ");
        b.append(" VALUES ('USERGROUP', 'TestDataManager', '130', 'User that can manage the testdatalibrary'); ");
        a.add(b.toString());

// Enlarge Property column in testcasestepaction table.
//  584
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepaction` ");
        b.append(" CHANGE COLUMN `Property` `Property` VARCHAR(150) NULL DEFAULT NULL ");
        a.add(b.toString());

// Creating the new group 'TestDataManager' from the group 'Test'.
//  585
        b = new StringBuilder();
        b.append("INSERT INTO usergroup SELECT distinct Login, 'TestDataManager' FROM usergroup where GroupName in ('Test');");
        a.add(b.toString());

// Adding Language code to documentation table in order to support multi language GUI.
//  586
        b = new StringBuilder();
        b.append("ALTER TABLE `documentation` ADD COLUMN `Lang` VARCHAR(45) NOT NULL DEFAULT 'en' AFTER `DocValue`, DROP PRIMARY KEY, ADD PRIMARY KEY (`DocTable`, `DocField`, `DocValue`, `Lang`);");
        a.add(b.toString());

// Adding FUNCTION as Public invariant.
//  587
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`) ");
        b.append(" VALUES ('INVARIANTPUBLIC', 'FUNCTION', '400', '');");
        a.add(b.toString());

// Adding LANGUAGE invariant.
//  588
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) ");
        b.append(" VALUES ('INVARIANTPRIVATE', 'LANGUAGE', '500', '', ''),");
        b.append("        ('LANGUAGE', 'en', '100', 'English', 'English');");
        a.add(b.toString());

// Adding Language column to the user table.
//  589
        b = new StringBuilder();
        b.append("ALTER TABLE `user` ADD COLUMN `Language` VARCHAR(45) NULL DEFAULT 'en' AFTER `Team`;  ");
        a.add(b.toString());

// New updated Documentation.
// 000 590 - 591
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());

// Adding GetFromJS property type.
//  592
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) ");
        b.append(" VALUES ('PROPERTYTYPE', 'getFromJS', '37', 'Getting data from javascript variable', '');");
        a.add(b.toString());

// Adding Invariant sizeScreen.
//  593
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) ");
        b.append(" VALUES ('INVARIANTPUBLIC', 'SCREENSIZE', '400', '', ''),");
        b.append("('SCREENSIZE', '320*480', '10', '320 px /  480 px', ''),");
        b.append("('SCREENSIZE', '360*640', '20', '360 px /  640 px', ''),");
        b.append("('SCREENSIZE', '1024*768', '30', '1024 px /  768 px', ''),");
        b.append("('SCREENSIZE', '1280*600', '40', '1280 px /  600 px', ''),");
        b.append("('SCREENSIZE', '1280*800', '50', '1280 px /  800 px', ''),");
        b.append("('SCREENSIZE', '1280*980', '60', '1280 px /  980 px', ''),");
        b.append("('SCREENSIZE', '1920*900', '70', '1920 px /  900 px', '');");
        a.add(b.toString());

// Adding Invariant sizeScreen.
//  594
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());

// Adding sizeScreen into testcaseexecution table.
//  595
        b = new StringBuilder();
        b.append("ALTER TABLE `testcaseexecution`  ");
        b.append("ADD COLUMN `screensize` VARCHAR(45) NULL DEFAULT NULL AFTER `Executor`;");
        a.add(b.toString());

// Adding global documentation for confirmation buttons and dataTable.
//  596
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());

// Adding global documentation for Header.
//  597
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());

        // Adding global documentation for Header.
        //  598
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());

        // Adding documentation for Footer.
        //  599 -- 601
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());

        // Adding documentation for Footer.
        //  602
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());

// New updated Documentation.
// 603-604
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());

// New updated Documentation.
// 605-606
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());

// New updated Documentation.
// 607-608
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());

// New updated Documentation.
// 609-610
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());

        // New Documentation for upload dialog and for the multiselect component.
        // 611
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());

        // New Documentation for the page Test Data Library - EN version
        // 612
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());

        // Increase log table limitation.
        // 613
        b = new StringBuilder();
        b.append("ALTER TABLE `logevent` CHANGE COLUMN `LogEventID` `LogEventID` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT ;");
        a.add(b.toString());

        // Homogenise column sizes.
        // 614
        b = new StringBuilder();
        b.append("ALTER TABLE `testcaseexecutionqueue` ");
        b.append("CHANGE COLUMN `Robot` `Robot` VARCHAR(100) NULL DEFAULT NULL ,");
        b.append("CHANGE COLUMN `BrowserVersion` `BrowserVersion` VARCHAR(45) NULL DEFAULT NULL ;");
        a.add(b.toString());

        // Homogenise column sizes.
        // 615
        b = new StringBuilder();
        b.append("ALTER TABLE `testcaseexecution` ");
        b.append("CHANGE COLUMN `Browser` `Browser` VARCHAR(45) NULL DEFAULT NULL ,");
        b.append("CHANGE COLUMN `Version` `Version` VARCHAR(45) NOT NULL DEFAULT '' ,");
        b.append("CHANGE COLUMN `Platform` `Platform` VARCHAR(45) NOT NULL DEFAULT '' ,");
        b.append("CHANGE COLUMN `BrowserFullVersion` `BrowserFullVersion` VARCHAR(200) NULL DEFAULT '' ;");
        a.add(b.toString());

        // Change Deploy Type Action on delete to avoid cascade All Applications and TestCases.
        // 616-617
        b = new StringBuilder();
        b.append("ALTER TABLE `application` ");
        b.append("DROP FOREIGN KEY `FK_application_01`;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `application` ");
        b.append("ADD CONSTRAINT `FK_application_01`");
        b.append("  FOREIGN KEY (`deploytype`) REFERENCES `deploytype` (`deploytype`) ON DELETE SET NULL ON UPDATE CASCADE;");
        a.add(b.toString());

        // New Documentation for the page TestCase - EN version
        // 618
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());

        // Documentation entries for Log Viewer page.
        // 619
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());

        // Documentation entries for Reporting by tag page.
        // 620
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());

        // Documentation entries for Reporting by tag page.
        // 621
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());

        // Documentation entries for Robot page.
        // 622
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());

        // New updated Documentation.
        // 623-624
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());

        // Documentation entries for Homepage.
        // 625
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());

        // Documentation entries for Homepage.
        // 626
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());

        // Documentation entries for Homepage.
        // 627-629
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());

        // Added private invariant for test active and automated.
        // 630
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (idname, value, sort, description, VeryShortDesc) VALUES ");
        b.append("('INVARIANTPRIVATE', 'TESTACTIVE', '510', '', ''),");
        b.append("('INVARIANTPRIVATE', 'TESTAUTOMATED', '520', '', ''),");
        b.append("('TESTACTIVE', 'Y', '10', 'Active', ''),");
        b.append("('TESTACTIVE', 'N', '20', 'Disable', ''),");
        b.append("('TESTAUTOMATED', 'Y', '10', 'Automated', ''),");
        b.append("('TESTAUTOMATED', 'N', '20', 'Not automated', '');");
        a.add(b.toString());

        // Documentation entries for Test page.
        // 631
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());

        // Documentation entries for Test page.
        // 632-633
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());

        // Documentation entries for Test Case page - useStep option.
        // 634
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());

        // Documentation entries for Test Case page - tooltips for controls and actions.
        // 635
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());

        // Enlarge Page column on Logevent table in order to support log of long Servlet.
        // 636
        b = new StringBuilder();
        b.append("ALTER TABLE `logevent` CHANGE COLUMN `Page` `Page` VARCHAR(200) NULL DEFAULT NULL ;");
        a.add(b.toString());

        // Documentation entries for Reporting By Tag
        // 637
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());

        // Insert invariant executeSqlUpdate, executeSqlStoredProcedure and skipAction
        // 638
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) ");
        b.append(" VALUES ('ACTION', 'executeSqlUpdate', '230', 'Execute SQL Script (Update, Delete, Insert)', ''),");
        b.append(" ('ACTION', 'executeSqlStoredProcedure', '240', 'Execute Stored Procedure', ''),");
        b.append(" ('ACTION', 'skipAction', '250', 'Skip Action', '');");
        a.add(b.toString());

        // Documentation entries for executeSqlUpdate, executeSqlStoredProcedure and skipAction
        //  639
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());

        // Documentation entries update for reporting by tag
        //  640-641
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());

        // Enlarge Method column
        //  642
        b = new StringBuilder();
        b.append("ALTER TABLE `testdatalib` CHANGE COLUMN `Method` `Method` VARCHAR(255) NOT NULL DEFAULT '' ;");
        a.add(b.toString());

        // Enlarge Doc Label column
        //  643
        b = new StringBuilder();
        b.append("ALTER TABLE `documentation` CHANGE COLUMN `DocLabel` `DocLabel` VARCHAR(300) NULL DEFAULT NULL ;");
        a.add(b.toString());

        // New updated Documentation.
        // 643-644
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());

        // Max size for screenshot
        //  645
        b = new StringBuilder();
        b.append("INSERT INTO `parameter` (`system`, `param`, `value`, `description`) VALUES ('', 'cerberus_screenshot_max_size', '1048576', 'Max size in bytes for a screenshot take while test case execution');");
        a.add(b.toString());

        // New documentation entries for TestCaseList 
        // 646
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());

        // New documentation entries for header
        // 647-649
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());

        // Removed empty group.
        // 650-651
        b = new StringBuilder();
        b.append("DELETE FROM `invariant` WHERE `idname`='GROUP' and`value`='';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE testcase SET `group`='MANUAL' WHERE `group` = '' or `group` is null;");
        a.add(b.toString());

        // Adding doc.
        // 652
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());

        // New updated Documentation.
        // 652-653
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());

        // Adding Creator column into countryenvparam_log table, adding an index based on Build Revision and adding a Description field in countryenvparam table.
        // 654-656
        b = new StringBuilder();
        b.append("ALTER TABLE `countryenvparam_log` ADD COLUMN `Creator` VARCHAR(10) NULL DEFAULT NULL AFTER `datecre`;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `countryenvparam_log` ADD INDEX `FK_countryenvparam_log_02_IX` (`system` ASC, `Build` ASC, `Revision` ASC );");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `countryenvparam` ADD COLUMN `Description` VARCHAR(200) NOT NULL DEFAULT '' AFTER `Environment`;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());

        // Documentation for ReportExecutionByTag summaryTable and export data
        // 657
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());

        // Documentation for the test data library 
        // 658
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());

        // Documentation update
        // 659
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());

        // Adding Technical Key to testdatalibdata table
        // 660
        b = new StringBuilder();
        b.append("ALTER TABLE `testdatalibdata` ");
        b.append(" ADD COLUMN `TestDataLibDataID` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT FIRST, ");
        b.append(" DROP PRIMARY KEY, ");
        b.append(" ADD PRIMARY KEY (`TestDataLibDataID`), ");
        b.append(" ADD UNIQUE INDEX `IX_testdatalibdata_01` (`TestDataLibID` ASC, `SubData` ASC); ");
        a.add(b.toString());

        // Documentation for duplicate test data library entry
        // 664
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());

        // New updated Documentation.
        // 665-666
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());

        // New invariant.
        // 667
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());

        // Tracability on Testdatalib object.
        // 668
        b = new StringBuilder();
        b.append("ALTER TABLE `testdatalib` ");
        b.append("ADD COLUMN `Creator` VARCHAR(45) NOT NULL DEFAULT '' AFTER `Description`,");
        b.append("ADD COLUMN `Created` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER `Creator`,");
        b.append("ADD COLUMN `LastModifier` VARCHAR(45) NOT NULL DEFAULT '' AFTER `Created`,");
        b.append("ADD COLUMN `LastModified` TIMESTAMP NOT NULL DEFAULT '2000-01-01 00:00:00' AFTER `LastModifier`;");
        a.add(b.toString());

        // New updated Documentation.
        // 669-670
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());

        // Updated Beta on getFromDataLib property.
        // 671
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET `value`='getFromDataLib_BETA', `description`='[Beta] Determines the data value associated with a library entry' ");
        b.append(" WHERE `idname`='PROPERTYTYPE' and`value`='getFromDataLib'; ");
        a.add(b.toString());

        // Adding Beta version of actions callSoapWithBase_BETA callSoap_BETA.
        // 672
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) ");
        b.append("VALUES ('ACTION', 'callSoap_BETA', '900', '[BETA] callSoap', ''),");
        b.append("    ('ACTION', 'callSoapWithBase_BETA', '910', '[BETA] callSoapWithBase', '');");
        a.add(b.toString());

        // Adding takeScreenshot control to replace the action.
        // 673
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) ");
        b.append("VALUES ('CONTROL', 'takeScreenshot', '100', 'Take a screenshot.', '');");
        a.add(b.toString());

        // Update Action descrition on deprecated actions.
        // 674-677
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET `description`='[DEPRECATED] takeScreenshot' WHERE `idname`='ACTION' and`value`='takeScreenshot';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET `description`='[DEPRECATED] clickAndWait' WHERE `idname`='ACTION' and`value`='clickAndWait';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET `description`='[DEPRECATED] enter' WHERE `idname`='ACTION' and`value`='enter';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET `description`='[DEPRECATED] selectAndWait' WHERE `idname`='ACTION' and`value`='selectAndWait';");
        a.add(b.toString());

        // New updated Documentation.
        // 678-679
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("SELECT 1 FROM dual;");
        a.add(b.toString());

        // Increased country and application column size..
        // 680-720
        b = new StringBuilder();
        b.append("ALTER TABLE `testdata` ");
        b.append(" CHANGE COLUMN `Application` `Application` VARCHAR(200) NOT NULL DEFAULT '' ,");
        b.append(" CHANGE COLUMN `Country` `Country` VARCHAR(45) NOT NULL DEFAULT '' ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcase` ");
        b.append(" DROP FOREIGN KEY `FK_testcase_02`;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcase` ");
        b.append(" CHANGE COLUMN `Application` `Application` VARCHAR(200) NULL DEFAULT NULL ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcaseexecution` ");
        b.append(" DROP FOREIGN KEY `FK_testcaseexecution_02`;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcaseexecution` ");
        b.append(" CHANGE COLUMN `Country` `Country` VARCHAR(45) NULL DEFAULT NULL ,");
        b.append(" CHANGE COLUMN `Application` `Application` VARCHAR(200) NULL DEFAULT NULL ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `countryenvironmentparameters` ");
        b.append(" DROP FOREIGN KEY `FK_countryenvironmentparameters_01`,");
        b.append(" DROP FOREIGN KEY `FK_countryenvironmentparameters_02`;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `countryenvironmentparameters` ");
        b.append(" CHANGE COLUMN `Country` `Country` VARCHAR(45) NOT NULL ,");
        b.append(" CHANGE COLUMN `Application` `Application` VARCHAR(200) NOT NULL ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `buildrevisionparameters` ");
        b.append(" DROP FOREIGN KEY `FK_buildrevisionparameters_01`;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `buildrevisionparameters` ");
        b.append(" CHANGE COLUMN `Application` `Application` VARCHAR(200) NULL DEFAULT NULL ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `application` ");
        b.append(" CHANGE COLUMN `Application` `Application` VARCHAR(200) NOT NULL ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcaseexecutionqueue` ");
        b.append(" DROP FOREIGN KEY `FK_testcaseexecutionqueue_01`;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcaseexecutionqueue` ");
        b.append(" CHANGE COLUMN `Country` `Country` VARCHAR(45) NOT NULL ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasecountryproperties` ");
        b.append(" DROP FOREIGN KEY `FK_testcasecountryproperties_01`;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasecountryproperties` ");
        b.append("CHANGE COLUMN `Country` `Country` VARCHAR(45) NOT NULL ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasecountry` ");
        b.append("CHANGE COLUMN `Country` `Country` VARCHAR(45) NOT NULL ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testdatalib` ");
        b.append("CHANGE COLUMN `Country` `Country` VARCHAR(45) NOT NULL DEFAULT '' ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `buildrevisionbatch` ");
        b.append("DROP FOREIGN KEY `FK_buildrevisionbatch_02`;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `buildrevisionbatch` ");
        b.append("CHANGE COLUMN `Country` `Country` VARCHAR(45) NULL DEFAULT NULL ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `countryenvdeploytype` ");
        b.append("DROP FOREIGN KEY `FK_countryenvdeploytype_01`;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `countryenvdeploytype` ");
        b.append("CHANGE COLUMN `Country` `Country` VARCHAR(45) NOT NULL ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `countryenvironmentdatabase` ");
        b.append("DROP FOREIGN KEY `FK_countryenvironmentdatabase_01`;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `countryenvironmentdatabase` ");
        b.append("CHANGE COLUMN `Country` `Country` VARCHAR(45) NOT NULL ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `countryenvparam` ");
        b.append("CHANGE COLUMN `Country` `Country` VARCHAR(45) NOT NULL ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `host` ");
        b.append("DROP FOREIGN KEY `FK_host_01`;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `host` ");
        b.append("CHANGE COLUMN `Country` `Country` VARCHAR(45) NOT NULL ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `countryenvparam_log` ");
        b.append("DROP FOREIGN KEY `FK_countryenvparam_log_01`;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `countryenvparam_log` ");
        b.append("CHANGE COLUMN `Country` `Country` VARCHAR(45) NOT NULL ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `countryenvlink` ");
        b.append("DROP FOREIGN KEY `FK_countryenvlink_01`;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `countryenvlink` ");
        b.append("CHANGE COLUMN `Country` `Country` VARCHAR(45) NOT NULL ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `countryenvlink` ");
        b.append("ADD CONSTRAINT `FK_countryenvlink_01` FOREIGN KEY (`system` , `Country` , `Environment`) REFERENCES `countryenvparam` (`system` , `Country` , `Environment`) ON DELETE CASCADE ON UPDATE CASCADE;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcase` ");
        b.append(" ADD CONSTRAINT `FK_testcase_02` FOREIGN KEY (`Application`) REFERENCES `application` (`Application`) ON DELETE CASCADE ON UPDATE CASCADE;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcaseexecution` ");
        b.append(" ADD CONSTRAINT `FK_testcaseexecution_02` FOREIGN KEY (`Application`) REFERENCES `application` (`Application`) ON DELETE CASCADE ON UPDATE CASCADE;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `countryenvironmentparameters` ");
        b.append(" ADD CONSTRAINT `FK_countryenvironmentparameters_01` FOREIGN KEY (`system` , `Country` , `Environment`) REFERENCES `countryenvparam` (`system` , `Country` , `Environment`) ON DELETE CASCADE ON UPDATE CASCADE,");
        b.append(" ADD CONSTRAINT `FK_countryenvironmentparameters_02` FOREIGN KEY (`Application`) REFERENCES `application` (`Application`) ON DELETE CASCADE ON UPDATE CASCADE;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `buildrevisionparameters` ");
        b.append(" ADD CONSTRAINT `FK_buildrevisionparameters_01` FOREIGN KEY (`Application`) REFERENCES `application` (`Application`) ON DELETE CASCADE ON UPDATE CASCADE;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcaseexecutionqueue` ");
        b.append(" ADD CONSTRAINT `FK_testcaseexecutionqueue_01` FOREIGN KEY (`Test` , `TestCase` , `Country`) REFERENCES `testcasecountry` (`Test` , `TestCase` , `Country`) ON DELETE CASCADE ON UPDATE CASCADE;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasecountryproperties` ");
        b.append("ADD CONSTRAINT `FK_testcasecountryproperties_01` FOREIGN KEY (`Test` , `TestCase` , `Country`) REFERENCES `testcasecountry` (`Test` , `TestCase` , `Country`) ON DELETE CASCADE ON UPDATE CASCADE;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `buildrevisionbatch` ");
        b.append("ADD CONSTRAINT `FK_buildrevisionbatch_02` FOREIGN KEY (`system` , `Country` , `Environment`) REFERENCES `countryenvparam` (`system` , `Country` , `Environment`) ON DELETE CASCADE ON UPDATE CASCADE;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `countryenvdeploytype` ");
        b.append("ADD CONSTRAINT `FK_countryenvdeploytype_01` FOREIGN KEY (`system` , `Country` , `Environment`) REFERENCES `countryenvparam` (`system` , `Country` , `Environment`) ON DELETE CASCADE ON UPDATE CASCADE;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `countryenvironmentdatabase` ");
        b.append("ADD CONSTRAINT `FK_countryenvironmentdatabase_01` FOREIGN KEY (`system` , `Country` , `Environment`) REFERENCES `countryenvparam` (`system` , `Country` , `Environment`) ON DELETE CASCADE ON UPDATE CASCADE;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `host` ");
        b.append("ADD CONSTRAINT `FK_host_01` FOREIGN KEY (`system` , `Country` , `Environment`) REFERENCES `countryenvparam` (`system` , `Country` , `Environment`) ON DELETE CASCADE ON UPDATE CASCADE;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `countryenvparam_log` ");
        b.append("ADD CONSTRAINT `FK_countryenvparam_log_01` FOREIGN KEY (`system` , `Country` , `Environment`) REFERENCES `countryenvparam` (`system` , `Country` , `Environment`) ON DELETE CASCADE ON UPDATE CASCADE;");
        a.add(b.toString());

        // Adding time index on log table IX_logevent_01.
        // 721
        b = new StringBuilder();
        b.append("ALTER TABLE `logevent` ");
        b.append(" ADD INDEX `IX_logevent_01` (`Time` ASC);");
        a.add(b.toString());

        // rename getFromDataLib to getFromDataLib_BETA.
        // 722
        b = new StringBuilder();
        b.append("UPDATE `testcasecountryproperties` ");
        b.append(" SET type='getFromDataLib_BETA' where type='getFromDataLib';");
        a.add(b.toString());

        // Clean data on wrong timestamp.
        // 723
        b = new StringBuilder();
        b.append("UPDATE `testdatalib` ");
        b.append(" SET Created = '2000-01-01 00:00:00' WHERE Created = '0000-00-00 00:00:00';");
        a.add(b.toString());

        // New updated Documentation.
        // 724-725
        a.add(new StringBuilder("SELECT 1 FROM dual;").toString());
        a.add(new StringBuilder("SELECT 1 FROM dual;").toString());

        // New updated Documentation.
        // 726-727
        a.add(new StringBuilder("SELECT 1 FROM dual;").toString());
        a.add(new StringBuilder("SELECT 1 FROM dual;").toString());

        // Enlarging Release column.
        // 728
        b = new StringBuilder();
        b.append("ALTER TABLE `buildrevisionparameters` CHANGE COLUMN `Release` `Release` VARCHAR(200) NULL DEFAULT NULL ; ");
        a.add(b.toString());

        // Add collumn repositoryUrl to the buildrevisionparameters table
        // 729
        b = new StringBuilder();
        b.append("ALTER TABLE `buildrevisionparameters` ");
        b.append("ADD COLUMN `repositoryurl` VARCHAR(1000) NULL DEFAULT '' AFTER `mavenversion`;");
        a.add(b.toString());

        // Add documentation for repositoryUrl
        // 730
        b = new StringBuilder();
        b.append("INSERT INTO `documentation` (`DocTable`, `DocField`, `DocValue`, `Lang`, `DocLabel`, `DocDesc`) ");
        b.append(" VALUES ('buildrevisionparameters', 'repositoryUrl', '', 'en', 'Repository URL', 'This information corresponds to the URL where the current build of the <code class=\\'doc-crbvvoca\\'>application</code> can be downloaded.<br>It allow to retrieve it in a repository such as Nexus.')");
        b.append(",('buildrevisionparameters', 'repositoryUrl', '', 'fr', 'URL du Dépot', 'Cette information correspond à l\\'URL d\\'où le build de l\\'<code class=\\'doc-crbvvoca\\'>application</code> peut-être téléchargé.<br>Cela permet de retrouver un build spécifique dans un dépot de livrable de type Nexus.');");
        a.add(b.toString());

        // Changing batchinvariant to a new structure.
        // 731-738
        b = new StringBuilder();
        b.append("ALTER TABLE `buildrevisionbatch` DROP FOREIGN KEY `FK_buildrevisionbatch_01`;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepbatch` DROP FOREIGN KEY `FK_testcasestepbatch_02`;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `batchinvariant` ADD COLUMN `system` VARCHAR(45) NOT NULL FIRST, DROP COLUMN `Unit`, DROP COLUMN `IncIni`, CHANGE COLUMN `Batch` `Batch` VARCHAR(100) NOT NULL DEFAULT '', CHANGE COLUMN `Description` `Description` VARCHAR(200) NULL DEFAULT NULL ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `buildrevisionbatch` CHANGE COLUMN `Batch` `Batch` VARCHAR(100) NOT NULL ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `buildrevisionbatch` ADD CONSTRAINT `FK_buildrevisionbatch_01` FOREIGN KEY (`Batch`) REFERENCES `batchinvariant` (`Batch`) ON DELETE CASCADE ON UPDATE CASCADE;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepbatch` CHANGE COLUMN `Batch` `Batch` VARCHAR(100) NOT NULL DEFAULT '' ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepbatch` ADD CONSTRAINT `FK_testcasestepbatch_02` FOREIGN KEY (`Batch`) REFERENCES `batchinvariant` (`Batch`) ON DELETE CASCADE ON UPDATE CASCADE;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("insert into batchinvariant select value, concat(`value`,b.batch), b.description from batchinvariant b join invariant where idname='SYSTEM';");
        a.add(b.toString());

        // New updated Documentation.
        // 739-740
        a.add(new StringBuilder("SELECT 1 FROM dual;").toString());
        a.add(new StringBuilder("SELECT 1 FROM dual;").toString());

        // New updated Documentation.
        // 741-742
        a.add(new StringBuilder("SELECT 1 FROM dual;").toString());
        a.add(new StringBuilder("SELECT 1 FROM dual;").toString());

        // New updated Documentation.
        // 743-744
        a.add(new StringBuilder("SELECT 1 FROM dual;").toString());
        a.add(new StringBuilder("SELECT 1 FROM dual;").toString());

        // Remove old Actions that were never implemented.
        // 745-746
        b = new StringBuilder();
        b.append("DELETE FROM `invariant` WHERE `idname`='ACTION' and`value` in ('store','removeSelection','waitForPage');");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE testcasestepaction set Action='Unknown' where Action in ('store','removeSelection','waitForPage');");
        a.add(b.toString());

        // New updated Documentation.
        // 747-748
        a.add(new StringBuilder("SELECT 1 FROM dual;").toString());
        a.add(new StringBuilder("SELECT 1 FROM dual;").toString());

        // New parameter cerberus_testdatalib_fetchmax.
        // 749
        b = new StringBuilder();
        b.append("INSERT INTO `parameter` (`system`, `param`, `value`, `description`) VALUES ('', 'cerberus_testdatalib_fetchmax', '100', 'Maximum number of fetched records that Cerberus will perform when retrieving a data from SQL Data Library.');");
        a.add(b.toString());

        // New updated Documentation.
        // 750-751
        a.add(new StringBuilder("SELECT 1 FROM dual;").toString());
        a.add(new StringBuilder("SELECT 1 FROM dual;").toString());

        // Removed and clean takeScreenshot action.
        // 752-753
        b = new StringBuilder();
        b.append("DELETE FROM `invariant` WHERE `idname`='ACTION' and `value`='takeScreenshot';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE testcasestepaction SET Action='skipAction' WHERE Action='takeScreenshot';");
        a.add(b.toString());

        // Added Environment group invariants.
        // 754
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) VALUES ");
        b.append("    ('ENVGP', 'DEV', '100', 'Development Environments', 'DEV'),");
        b.append("    ('ENVGP', 'QA', '200', 'Quality Assurance Environments', 'QA'),");
        b.append("    ('ENVGP', 'UAT', '300', 'User Acceptance Test Environments', 'UAT'),");
        b.append("    ('ENVGP', 'PROD', '400', 'Production Environments', 'PROD'),");
        b.append("    ('INVARIANTPRIVATE', 'ENVGP', '530', '', '');");
        a.add(b.toString());

        // Rename Action skipAction to doNothing.
        // 755-764
        b = new StringBuilder();
        b.append("UPDATE testcasestepaction SET Action='doNothing' WHERE Action='skipAction';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET `value`='doNothing', `description`='doNothing' WHERE `idname`='ACTION' and`value`='skipAction';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET `value`='mouseLeftButtonPress', `sort`='37' WHERE `idname`='ACTION' and`value`='mouseDown';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET `value`='mouseLeftButtonRelease', `sort`='38', `description`='Selenium Action mouseUp' WHERE `idname`='ACTION' and`value`='mouseUp';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET `sort`='49' WHERE `idname`='ACTION' and`value`='keypress';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET `sort`='31' WHERE `idname`='ACTION' and`value`='clickAndWait';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET `sort`='35' WHERE `idname`='ACTION' and`value`='doubleClick';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET `sort`='55' WHERE `idname`='ACTION' and`value`='switchToWindow';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET `sort`='59' WHERE `idname`='ACTION' and`value`='manageDialog';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET sort=sort*10 where `idname` in ('ACTION', 'CONTROL');");
        a.add(b.toString());

        // New updated Documentation.
        // 765-766
        a.add(new StringBuilder("SELECT 1 FROM dual;").toString());
        a.add(new StringBuilder("SELECT 1 FROM dual;").toString());

        // Updated CHAIN invariant.
        // 767-768
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET `value`='Y', `description`='Yes' WHERE `idname`='CHAIN' and`value`='0';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET `value`='N', `description`='No' WHERE `idname`='CHAIN' and`value`='1';");
        a.add(b.toString());

        // Add the hideKeyboard action.
        // 769
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) VALUES ('ACTION', 'hideKeyboard', '1200', 'hideKeyboard', '');");
        a.add(b.toString());

        // Add the Unknown Control.
        // 770
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) VALUES ('CONTROL', 'Unknown', '10', 'Unknown', '');");
        a.add(b.toString());

        // Add the hideKeyboard and update the keyPress action documentation.
        // 771-772
        a.add(new StringBuilder("SELECT 1 FROM dual;").toString());
        a.add(new StringBuilder("SELECT 1 FROM dual;").toString());

        // Add the swipe action.
        // 773-775
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) VALUES ('ACTION', 'swipe', '1300', 'Swipe mobile screen', '');");
        a.add(b.toString());
        a.add(new StringBuilder("SELECT 1 FROM dual;").toString());
        b = new StringBuilder();
        b.append("INSERT INTO `parameter` (`system`, `param`, `value`, `description`) VALUES ('', 'appium_swipeDuration', '2000', 'The duration for the Appium swipe action');");
        a.add(b.toString());

        // Add the cerberus_notinuse_timeout parameter.
        // 776
        b = new StringBuilder();
        b.append("INSERT INTO `parameter` (`system`, `param`, `value`, `description`) VALUES ('', 'cerberus_notinuse_timeout', '600', 'Integer that correspond to the number of seconds after which, any pending execution (status=PE) will not be considered as pending.');");
        a.add(b.toString());

        // Remove unicity constrain on TestDataLib.
        // 777
        b = new StringBuilder();
        b.append("ALTER TABLE `testdatalib` ");
        b.append("DROP INDEX `IX_testdatalib_01` ,");
        b.append("ADD INDEX `IX_testdatalib_01` (`Name` ASC, `system` ASC, `Environment` ASC, `Country` ASC) ;");
        a.add(b.toString());

        // Add the RobotCapability table
        // 778-779
        b = new StringBuilder();
        b.append("CREATE TABLE `robotcapability` (\n"
                + "  `id` int(11) NOT NULL AUTO_INCREMENT,\n"
                + "  `robot` varchar(100) NOT NULL,\n"
                + "  `capability` varchar(45) NOT NULL,\n"
                + "  `value` varchar(255) NOT NULL,\n"
                + "  PRIMARY KEY (`id`),\n"
                + "  UNIQUE KEY `uq_capability_value_idx` (`capability`,`value`,`robot`),\n"
                + "  KEY `fk_robot_idx` (`robot`),\n"
                + "  CONSTRAINT `fk_robot` FOREIGN KEY (`robot`) REFERENCES `robot` (`robot`) ON DELETE CASCADE ON UPDATE CASCADE\n"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8;\n");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("INSERT INTO `robotcapability` (`robot`, `value`, `capability`)  \n"
                + "\tSELECT `robot`, `platform`, 'platform' AS `capability` FROM `robot`\n"
                + "    UNION\n"
                + "    SELECT `robot`, `browser`, 'browser' AS `capability` FROM `robot`\n"
                + "    UNION\n"
                + "    SELECT `robot`, `version`, 'version' AS `capability` FROM `robot`");
        a.add(b.toString());

        // Apply changes on RobotCapability indexes/keys to follow naming convention
        // 780-782
        b = new StringBuilder();
        b.append("ALTER TABLE `robotcapability` \n"
                + "DROP FOREIGN KEY `fk_robot`;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `robotcapability` \n"
                + "DROP INDEX `uq_capability_value_idx` ,\n"
                + "ADD UNIQUE INDEX `IX_robotcapability_01` (`capability` ASC, `value` ASC, `robot` ASC),\n"
                + "DROP INDEX `fk_robot_idx` ,\n"
                + "ADD INDEX `IX_robotcapability_02` (`robot` ASC);");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `robotcapability` \n"
                + "ADD CONSTRAINT `FK_robotcapability_01`\n"
                + "  FOREIGN KEY (`robot`)\n"
                + "  REFERENCES `robot` (`robot`)\n"
                + "  ON DELETE CASCADE\n"
                + "  ON UPDATE CASCADE;");
        a.add(b.toString());

        //Add IPA application type inside 783
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`) ");
        b.append("VALUES ('APPLITYPE', 'IPA', '50', 'IOS Application');");
        a.add(b.toString());

        // Reverting changes on RobotCapability table
        // 784
        b = new StringBuilder();
        b.append("DELETE FROM `robotcapability`;");
        a.add(b.toString());

        // Update testcaseexecution and testcasestepexecution to set default end to null.
        // Update last_modified timestamp default value
        // 785 - 794
        b = new StringBuilder();
        b.append("ALTER TABLE `testcaseexecution` ");
        b.append("CHANGE COLUMN `End` `End` TIMESTAMP NOT NULL DEFAULT '1970-01-01 01:01:01',");
        b.append("CHANGE COLUMN `Start` `Start` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepexecution` ");
        b.append("CHANGE COLUMN `End` `End` TIMESTAMP NOT NULL DEFAULT '1970-01-01 01:01:01',");
        b.append("CHANGE COLUMN `Start` `Start` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testdatalib` ");
        b.append("CHANGE COLUMN `LastModified` `LastModified` TIMESTAMP NOT NULL DEFAULT '1970-01-01 01:01:01';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `test` ");
        b.append("CHANGE COLUMN `last_modified` `last_modified` TIMESTAMP NOT NULL DEFAULT '1970-01-01 01:01:01';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcase` ");
        b.append("CHANGE COLUMN `last_modified` `last_modified` TIMESTAMP NOT NULL DEFAULT '1970-01-01 01:01:01';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasecountry` ");
        b.append("CHANGE COLUMN `last_modified` `last_modified` TIMESTAMP NOT NULL DEFAULT '1970-01-01 01:01:01';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasecountryproperties` ");
        b.append("CHANGE COLUMN `last_modified` `last_modified` TIMESTAMP NOT NULL DEFAULT '1970-01-01 01:01:01';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestep` ");
        b.append("CHANGE COLUMN `last_modified` `last_modified` TIMESTAMP NOT NULL DEFAULT '1970-01-01 01:01:01';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepaction` ");
        b.append("CHANGE COLUMN `last_modified` `last_modified` TIMESTAMP NOT NULL DEFAULT '1970-01-01 01:01:01';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepactioncontrol` ");
        b.append("CHANGE COLUMN `last_modified` `last_modified` TIMESTAMP NOT NULL DEFAULT '1970-01-01 01:01:01';");
        a.add(b.toString());

        // Add description in testcasestepexecution, testcasestepactionexecution
        // and testcasestepactioncontrolexecution tables
        // 795 - 798
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepexecution` ");
        b.append("ADD COLUMN `Description` VARCHAR(150) NOT NULL DEFAULT '' AFTER `ReturnMessage`;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepactionexecution` ");
        b.append("ADD COLUMN `Description` VARCHAR(255) NOT NULL DEFAULT '' AFTER `PageSourceFileName`;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepactioncontrolexecution`  ");
        b.append("ADD COLUMN `Description` VARCHAR(255) NOT NULL DEFAULT '' AFTER `PageSourceFilename`;");
        a.add(b.toString());

        //
        b = new StringBuilder();
        b.append("UPDATE `testdatalib` ");
        b.append("SET `LastModified` =  '1970-01-01 01:01:01' WHERE `LastModified` = '0000-00-00 00:00:00';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `test` ");
        b.append("SET `last_modified` =  '1970-01-01 01:01:01' WHERE `last_modified` = '0000-00-00 00:00:00';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `testcase` ");
        b.append("SET `last_modified` =  '1970-01-01 01:01:01' WHERE `last_modified` = '0000-00-00 00:00:00';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `testcasecountry` ");
        b.append("SET `last_modified` =  '1970-01-01 01:01:01' WHERE `last_modified` = '0000-00-00 00:00:00';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `testcasecountryproperties` ");
        b.append("SET `last_modified` =  '1970-01-01 01:01:01' WHERE `last_modified` = '0000-00-00 00:00:00';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `testcasestep` ");
        b.append("SET `last_modified` =  '1970-01-01 01:01:01' WHERE `last_modified` = '0000-00-00 00:00:00';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `testcasestepaction` ");
        b.append("SET `last_modified` =  '1970-01-01 01:01:01' WHERE `last_modified` = '0000-00-00 00:00:00';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `testcasestepactioncontrol` ");
        b.append("SET `last_modified` =  '1970-01-01 01:01:01' WHERE `last_modified` = '0000-00-00 00:00:00';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `testcase` ");
        b.append("SET `TCDateCrea` =  '1970-01-01 01:01:01' WHERE `TCDateCrea` = '0000-00-00 00:00:00';");
        a.add(b.toString());

        // Add main robot capability invariants
        // 807
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) VALUES ");
        b.append("('INVARIANTPUBLIC', 'CAPABILITY', '500', 'Robot capabilities', ''), ");
        b.append("('CAPABILITY', 'automationName', '1', 'Automation name, e.g.: Appium)', ''), ");
        b.append("('CAPABILITY', 'deviceName', '2', 'Device name (useful for Appium)', ''), ");
        b.append("('CAPABILITY', 'app', '3', 'Application name (useful for Appium)', ''), ");
        b.append("('CAPABILITY', 'platformName', '4', 'Platform name (useful for Appium)', ''), ");
        b.append("('CAPABILITY', 'platformVersion', '5', 'Platform version (useful for Appium)', ''), ");
        b.append("('CAPABILITY', 'browserName', '6', 'Browser name (useful for Appium)', ''), ");
        b.append("('CAPABILITY', 'autoWebview', '7', 'If auto web view has to be enabled (useful for Appium, e.g.: true) ', '');");
        a.add(b.toString());

        // Add documentation on robot capability
        // 808
        a.add(new StringBuilder("SELECT 1 FROM dual;").toString());

        // Correct property to add the /text() in xpath.
        // 809
        b = new StringBuilder();
        b.append("UPDATE testcasecountryproperties SET value2 = concat(value2, '/text()')");
        b.append(" WHERE `type` = 'getFromXML' and value2 not like '%ext()';    ");
        a.add(b.toString());

        // Adding missing index in order to support RANDOMNEW and NOTINUSE
        // 810
        b = new StringBuilder();
        b.append("ALTER TABLE `testcaseexecution` ");
        b.append(" ADD INDEX `IX_testcaseexecution_09` (`Country` ASC, `Environment` ASC, `ControlStatus` ASC), "); // Used for NOTINUSE   
        b.append(" ADD INDEX `IX_testcaseexecution_10` (`Test` ASC, `TestCase` ASC, `Environment` ASC, `Country` ASC, `Build` ASC) ;"); // Used for RANDOMNEW
        a.add(b.toString());

        // Adding Soap URL on database table
        // 811
        b = new StringBuilder();
        b.append("ALTER TABLE `countryenvironmentdatabase` ");
        b.append("ADD COLUMN `SoapUrl` VARCHAR(200) NOT NULL DEFAULT ''  AFTER `ConnectionPoolName`;");
        a.add(b.toString());

        // Adding DatabaseUrl on testdatalib table
        // 812
        b = new StringBuilder();
        b.append("ALTER TABLE `testdatalib` ");
        b.append("ADD COLUMN `DatabaseUrl` VARCHAR(45) NOT NULL DEFAULT '' AFTER `Script`;");
        a.add(b.toString());

        // Adding Action skipAction
        // 813
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`) ");
        b.append("VALUES ('ACTION', 'skipAction', '2600', 'skipAction');");
        a.add(b.toString());

        // Adding Reset Password Email Parameters
        // 814
        b = new StringBuilder();
        b.append("INSERT INTO `parameter` (`system`, `param`, `value`, `description`) ");
        b.append("VALUES ('', 'cerberus_notification_forgotpassword_subject', '[Cerberus] Reset your password', 'Subject of Cerberus forgot password notification email.')");
        b.append(", ('', 'cerberus_notification_forgotpassword_body', 'Hello %NAME%<br><br>We\\'ve received a request to reset your Cerberus password.<br><br>%LINK%<br><br>If you didn\\'t request a password reset, not to worry, just ignore this email and your current password will continue to work.<br><br>Cheers,<br>The Cerberus Team', 'Cerberus forgot password notification email body. %LOGIN%, %NAME% and %LINK% can be used as variables.');");
        a.add(b.toString());

        // Adding Column ResetPasswordToken in User Table
        // 815
        b = new StringBuilder();
        b.append("ALTER TABLE `user` ");
        b.append("ADD COLUMN `ResetPasswordToken` CHAR(40) NOT NULL DEFAULT '' AFTER `Password`;");
        a.add(b.toString());

        // Add Sort column to test case step related tables (#569)
        // 816 - 827 
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestep` ADD COLUMN `Sort` INT(10) UNSIGNED AFTER `Step`;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `testcasestep` SET `Sort` = `Step`;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepaction` ADD COLUMN `Sort` INT(10) UNSIGNED AFTER `Sequence`;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `testcasestepaction` SET `Sort` = `Sequence`;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepactioncontrol` ADD COLUMN `Sort` INT(10) UNSIGNED AFTER `Control`;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `testcasestepactioncontrol` SET `Sort` = `Control`;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepactioncontrolexecution` ADD COLUMN `Sort` INT(10) UNSIGNED AFTER `Control`;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `testcasestepactioncontrolexecution` SET `Sort` = `Control`;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepactionexecution` ADD COLUMN `Sort` INT(10) UNSIGNED AFTER `Sequence`;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `testcasestepactionexecution` SET `Sort` = `Sequence`;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepexecution` ADD COLUMN `Sort` INT(10) UNSIGNED AFTER `Step`;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `testcasestepexecution` SET `Sort` = `Step`;");
        a.add(b.toString());

        // Removed callSoapWithBase_BETA and callSoap_BETA actions.
        // 828
        b = new StringBuilder();
        b.append("DELETE from invariant where idname='ACTION' and value in ('callSoapWithBase_BETA','callSoap_BETA');");
        a.add(b.toString());

        // Added flag in order to support forcing Execution status at action level.
        // 829-831
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepaction` ADD COLUMN `ForceExeStatus` VARCHAR(45) NOT NULL DEFAULT '' AFTER `Property`;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) VALUES");
        b.append("  ('ACTIONFORCEEXESTATUS', '', '10', 'Standard behaviour.', 'Std Behaviour')");
        b.append(", ('ACTIONFORCEEXESTATUS', 'PE', '20', 'Force the Execution to continue running not impacting the final status whatever the result of the action is.', 'Continue')");
        b.append(", ('INVARIANTPRIVATE', 'ACTIONFORCEEXESTATUS', '540', '', '');");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepactionexecution` ADD COLUMN `ForceExeStatus` VARCHAR(45) NOT NULL DEFAULT '' AFTER `Property`;");
        a.add(b.toString());

        // New updated Documentation.
        // 832-833
        a.add(new StringBuilder("SELECT 1 FROM dual;").toString());
        a.add(new StringBuilder("SELECT 1 FROM dual;").toString());

        // New cerberus_automaticexecution_enable parameter.
        // 834
        b = new StringBuilder();
        b.append("INSERT INTO `parameter` (`system`, `param`, `value`, `description`) VALUES ('', 'cerberus_automaticexecution_enable', 'Y', 'Activation boolean in order to activate the automatic executions.Y value will allow execution. Any other value will stop the execution returning an error message..');");
        a.add(b.toString());

        // Updated Description of cerberus_reporting_url parameter.
        // 835
        b = new StringBuilder();
        b.append("UPDATE `parameter` SET `description`='URL to Cerberus reporting screen. the following variables can be used : %COUNTRY%, %ENV%,  %APPLI%, %BUILD% and %REV%.' WHERE `system`='' and`param`='cerberus_reporting_url';");
        a.add(b.toString());

        // Updated Description of cerberus_reporting_url parameter.
        // 836-839
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepactioncontrol` CHANGE COLUMN `ControlValue` `ControlValue` VARCHAR(2500) NOT NULL DEFAULT ''  ,CHANGE COLUMN `ControlProperty` `ControlProperty` VARCHAR(2500) NULL DEFAULT NULL ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepactioncontrolexecution` CHANGE COLUMN `ControlValue` `ControlValue` VARCHAR(2500) NULL DEFAULT NULL ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepaction` CHANGE COLUMN `Object` `Object` VARCHAR(2500) NOT NULL DEFAULT ''  ,CHANGE COLUMN `Property` `Property` VARCHAR(2500) NULL DEFAULT NULL ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepactionexecution` CHANGE COLUMN `Object` `Object` VARCHAR(2500) NULL DEFAULT NULL  ,CHANGE COLUMN `Property` `Property` VARCHAR(2500) NULL DEFAULT NULL ;");
        a.add(b.toString());

        // Add userPreferences column in user table.
        // 840
        b = new StringBuilder();
        b.append("ALTER TABLE `user` ");
        b.append("ADD COLUMN `UserPreferences` TEXT NOT NULL AFTER `Email`;");
        a.add(b.toString());

        // Add the getFromGroovy property type.
        // 841
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) VALUES ('PROPERTYTYPE', 'getFromGroovy', '80', 'Getting value from a Groovy script', '');");
        a.add(b.toString());

        // Add filter information for tooltip in documentation table.
        // 842
        a.add(new StringBuilder("SELECT 1 FROM dual;").toString());

        // Add comment on properties.
        // 843-845
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasecountryproperties` ");
        b.append("ADD COLUMN `Description` VARCHAR(255) NULL DEFAULT '' AFTER `Property`;");
        a.add(b.toString());
        a.add(new StringBuilder("SELECT 1 FROM dual;").toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcaseexecutiondata` ");
        b.append("ADD COLUMN `Description` VARCHAR(255) NULL DEFAULT '' AFTER `Property`;");
        a.add(b.toString());

        // Add documentation to the getFromGroovy property type.
        // 846
        a.add(new StringBuilder("SELECT 1 FROM dual;").toString());

        // Clean URLLOGIN column in countryenvironmentparameters table.
        // 847-848
        b = new StringBuilder();
        b.append("UPDATE countryenvironmentparameters SET URLLOGIN = '' WHERE URLLOGIN is null;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `countryenvironmentparameters` CHANGE COLUMN `URLLOGIN` `URLLOGIN` VARCHAR(150) NOT NULL DEFAULT '' ;");
        a.add(b.toString());

        // Add columns in testdatalib and testdatatlibdata to related to CSV type.
        // Add CSV TESTDATATYPE invariant
        // Add documentation
        // 849-853
        b = new StringBuilder();
        b.append("ALTER TABLE `testdatalib` ");
        b.append("ADD COLUMN `CsvUrl` VARCHAR(250) NOT NULL DEFAULT '' AFTER `Envelope`;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testdatalibdata` ");
        b.append("ADD COLUMN `ColumnPosition` VARCHAR(45) NOT NULL DEFAULT '' AFTER `ParsingAnswer`;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`) ");
        b.append("VALUES ('TESTDATATYPE', 'CSV', '40', 'Dynamic test data from CSV file');");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testdatalib` ");
        b.append("ADD COLUMN `Separator` VARCHAR(45) NOT NULL DEFAULT '' AFTER `CsvUrl`;");
        a.add(b.toString());
        a.add(new StringBuilder("SELECT 1 FROM dual;").toString());

        // label table creation
        // 854
        b = new StringBuilder();
        b.append("CREATE TABLE `label` (");
        b.append("`Id` INT NOT NULL AUTO_INCREMENT,");
        b.append("`System` VARCHAR(45) NOT NULL DEFAULT '',");
        b.append("`Label` VARCHAR(100) NOT NULL DEFAULT '',");
        b.append("`Color` VARCHAR(45) NOT NULL DEFAULT '',");
        b.append("`ParentLabel` VARCHAR(100) NOT NULL DEFAULT '',");
        b.append("`Description` VARCHAR(250) NOT NULL DEFAULT '',");
        b.append("`UsrCreated` VARCHAR(45) NOT NULL DEFAULT '',");
        b.append("`DateCreated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,");
        b.append("`UsrModif` VARCHAR(45) NOT NULL DEFAULT '',");
        b.append("`DateModif` TIMESTAMP NOT NULL DEFAULT '1970-01-01 01:01:01', ");
        b.append(" PRIMARY KEY (`id`), ");
        b.append(" UNIQUE INDEX `IX_label_01` (`system` ASC, `label` ASC))");
        b.append(" ENGINE=InnoDB DEFAULT CHARSET=utf8;");
        a.add(b.toString());

        // testcaselabel table creation
        // 855
        b = new StringBuilder();
        b.append("CREATE TABLE `testcaselabel` (`Id` INT NOT NULL AUTO_INCREMENT,`Test` varchar(45) NOT NULL,`TestCase` varchar(45) NOT NULL,`LabelId` INT NOT NULL,");
        b.append("`UsrCreated` VARCHAR(45) NOT NULL DEFAULT '',");
        b.append("`DateCreated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,");
        b.append("`UsrModif` VARCHAR(45) NOT NULL DEFAULT '',");
        b.append("`DateModif` TIMESTAMP NOT NULL DEFAULT '1970-01-01 01:01:01', ");
        b.append(" PRIMARY KEY (`Id`),");
        b.append(" UNIQUE KEY `IX_testcaselabel_03` (`Test`,`TestCase`,`LabelId`),");
        b.append(" KEY `IX_testcaselabel_01` (`Test`,`TestCase`),");
        b.append(" KEY `IX_testcaselabel_02` (`LabelId`),");
        b.append(" CONSTRAINT `FK_testcaselabel_01` FOREIGN KEY (`Test`, `TestCase`) REFERENCES `testcase` (`Test`, `TestCase`) ON DELETE CASCADE ON UPDATE CASCADE,");
        b.append(" CONSTRAINT `FK_testcaselabel_02` FOREIGN KEY (`LabelId`) REFERENCES `label` (`Id`) ON DELETE CASCADE ON UPDATE CASCADE) ");
        b.append(" ENGINE=InnoDB DEFAULT CHARSET=utf8;");
        a.add(b.toString());

        // Documentation on label
        // 856
        a.add(new StringBuilder("SELECT 1 FROM dual;").toString());

        // Add a sample tag
        // 857
        b = new StringBuilder();
        b.append("INSERT INTO label (`system`,`label`, `color`,`UsrCreated`, `UsrModif`) ");
        b.append("SELECT `value` , 'MyFirstLabel', '#000000' , 'admin' , 'admin' from invariant where idname = 'SYSTEM'");
        a.add(b.toString());

        // Add the "rightClick" action
        // 858-859
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) VALUES ('ACTION', 'rightClick', '310', 'Right click on an element', 'Right click');\n");
        a.add(b.toString());
        a.add(new StringBuilder("SELECT 1 FROM dual;").toString());

        // Documentation on new Test case list page buttons
        // 860
        a.add(new StringBuilder("SELECT 1 FROM dual;").toString());

        // New sql timeout parameters.
        // 861
        b = new StringBuilder();
        b.append("INSERT INTO `parameter` (`system`, `param`, `value`, `description`) VALUES ");
        b.append(" ('', 'cerberus_propertyexternalsql_timeout', '60', 'Integer that correspond to the number of seconds after which, any SQL executed from a property calculation will fail.')");
        b.append(",('', 'cerberus_actionexecutesqlupdate_timeout', '60', 'Integer that correspond to the number of seconds after which, any SQL triggered from action executeSqlUpdate will fail.')");
        a.add(b.toString());

        // New Index column in testcaseexecutiondata in order to support multirow property.
        // 862
        b = new StringBuilder();
        b.append("ALTER TABLE `testcaseexecutiondata` ");
        b.append("ADD COLUMN `Index` INT NOT NULL DEFAULT 1 AFTER `Property`,");
        b.append("DROP PRIMARY KEY, ADD PRIMARY KEY (`ID`, `Property`, `Index`) ;");
        a.add(b.toString());

        // Adding generic variable columns on application environment table (countryenvironmentparameters).
        // 863
        b = new StringBuilder();
        b.append("ALTER TABLE `countryenvironmentparameters` ");
        b.append("ADD COLUMN `Var1` VARCHAR(200) NOT NULL DEFAULT '' AFTER `URLLOGIN`,");
        b.append("ADD COLUMN `Var2` VARCHAR(200) NOT NULL DEFAULT '' AFTER `Var1`,");
        b.append("ADD COLUMN `Var3` VARCHAR(200) NOT NULL DEFAULT '' AFTER `Var2`,");
        b.append("ADD COLUMN `Var4` VARCHAR(200) NOT NULL DEFAULT '' AFTER `Var3`;");
        a.add(b.toString());

        // Make getFromDataLib official.
        // 864
        b = new StringBuilder();
        b.append("UPDATE testcasecountryproperties SET `Type` = 'getFromDataLib' where `Type` = 'getFromDataLib_BETA';");
        a.add(b.toString());

        // New updated Documentation.
        // 865-866
        a.add(new StringBuilder("SELECT 1 FROM dual;").toString());
        a.add(new StringBuilder("SELECT 1 FROM dual;").toString());

        // Make getFromDataLib official.
        // 867
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET `value`='getFromDataLib' WHERE `idname`='PROPERTYTYPE' and `value`='getFromDataLib_BETA';");
        a.add(b.toString());

        // Adding Url Source for CSV datasource..
        // 868-869
        b = new StringBuilder();
        b.append("ALTER TABLE `countryenvironmentdatabase` ADD COLUMN `CsvUrl` VARCHAR(200) NOT NULL DEFAULT '' AFTER `SoapUrl`;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testdatalib` ADD COLUMN `DatabaseCsv` VARCHAR(45) NOT NULL DEFAULT '' AFTER `Envelope`;");
        a.add(b.toString());

        // Rename STATIC to INTERNAL in TestDataLib.
        // 870-871
        b = new StringBuilder();
        b.append("UPDATE `testdatalib` SET Type='INTERNAL' WHERE `Type`='STATIC';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET `value`='INTERNAL', `description`='Internal Cerberus test data.' WHERE `idname`='TESTDATATYPE' and`value`='STATIC';");
        a.add(b.toString());

        // New table to host all file saved during execution.
        // 872
        b = new StringBuilder();
        b.append("CREATE TABLE `testcaseexecutionfile` (");
        b.append(" `ID` BIGINT(20) NOT NULL AUTO_INCREMENT ,");
        b.append(" `ExeID` BIGINT(20) unsigned NOT NULL ,");
        b.append(" `Level` VARCHAR(150) NOT NULL DEFAULT '' ,");
        b.append(" `FileDesc` VARCHAR(100) NOT NULL DEFAULT '' ,");
        b.append(" `Filename` VARCHAR(150) NOT NULL DEFAULT '' ,");
        b.append(" `FileType` VARCHAR(45) NOT NULL DEFAULT '' ,");
        b.append(" `UsrCreated` VARCHAR(45) NOT NULL DEFAULT '',");
        b.append(" `DateCreated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,");
        b.append(" `UsrModif` VARCHAR(45) NOT NULL DEFAULT '',");
        b.append(" `DateModif` TIMESTAMP NOT NULL DEFAULT '1970-01-01 01:01:01', ");
        b.append(" PRIMARY KEY (`ID`) ,");
        b.append(" UNIQUE INDEX `IX_testcaseexecutionfile_01` (`ExeID` ASC, `Level` ASC, `FileDesc` ASC) ,");
        b.append(" CONSTRAINT `FK_testcaseexecutionfile_01` FOREIGN KEY (`ExeID`) REFERENCES `testcaseexecution` (`ID`) ON DELETE CASCADE ON UPDATE CASCADE");
        b.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8;");
        a.add(b.toString());

        // Updated cerberus_picture_path parameter.
        // 873-874
        b = new StringBuilder();
        b.append("UPDATE `parameter` SET `param`='cerberus_mediastorage_path', `description`='Path to store the Cerberus Media files (like Selenium Screenshot or SOAP requests and responses).' WHERE `param`='cerberus_picture_path';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `parameter` SET `param`='cerberus_mediastorage_url', `description`='Link (URL) to the Cerberus Media Files. That link should point to cerberus_mediastorage_path location.' WHERE `system`='' and`param`='cerberus_picture_url';");
        a.add(b.toString());

        // Migrate old Screenshot and PageSource fields to new table.
        // 875-878
        b = new StringBuilder();
        b.append("INSERT into testcaseexecutionfile (`exeid`, `level`, `FileDesc`, `Filename`, `FileType`, `UsrCreated`)");
        b.append("select ID ExeID, concat(test,\"-\", testcase,\"-\", Step,\"-\", Sequence) level, 'Screenshot' FileDesc, replace(ScreenshotFilename, '\\\\', '/') Filename");
        b.append(" ,ucase(right(ScreenshotFilename, 3)) FileType, 'RecoverSQL' UsrCreated from testcasestepactionexecution where ScreenshotFilename is not null and TO_DAYS(NOW()) - TO_DAYS(Start) <= 10;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("INSERT into testcaseexecutionfile (`exeid`, `level`, `FileDesc`, `Filename`, `FileType`, `UsrCreated`)");
        b.append("select ID ExeID, concat(test,\"-\", testcase,\"-\", Step,\"-\", Sequence) level, 'PageSource' FileDesc, replace(PageSourceFileName, '\\\\', '/') Filename");
        b.append(" ,ucase(right(PageSourceFileName, 3)) FileType, 'RecoverSQL' UsrCreated from testcasestepactionexecution where PageSourceFileName is not null and TO_DAYS(NOW()) - TO_DAYS(Start) <= 10;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("INSERT into testcaseexecutionfile (`exeid`, `level`, `FileDesc`, `Filename`, `FileType`, `UsrCreated`)");
        b.append("select ID ExeID, concat(test,\"-\", testcase,\"-\", Step,\"-\", Sequence,\"-\", Control) level, 'Screenshot' FileDesc, replace(ScreenshotFilename, '\\\\', '/') Filename");
        b.append(" ,ucase(right(ScreenshotFilename, 3)) FileType, 'RecoverSQL' UsrCreated from testcasestepactioncontrolexecution where ScreenshotFilename is not null and TO_DAYS(NOW()) - TO_DAYS(Start) <= 10;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("INSERT into testcaseexecutionfile (`exeid`, `level`, `FileDesc`, `Filename`, `FileType`, `UsrCreated`)");
        b.append("select ID ExeID, concat(test,\"-\", testcase,\"-\", Step,\"-\", Sequence,\"-\", Control) level, 'PageSource' FileDesc, replace(PageSourceFileName, '\\\\', '/') Filename");
        b.append(" ,ucase(right(PageSourceFileName, 3)) FileType, 'RecoverSQL' UsrCreated from testcasestepactioncontrolexecution where PageSourceFileName is not null and TO_DAYS(NOW()) - TO_DAYS(Start) <= 10;");
        a.add(b.toString());

        // New sql timeout parameters.
        // 879
        b = new StringBuilder();
        b.append("INSERT INTO `parameter` (`system`, `param`, `value`, `description`) VALUES ");
        b.append(" ('', 'cerberus_actionexecutesqlstoredprocedure_timeout', '60', 'Integer that correspond to the number of seconds after which, any SQL triggered from action executeSqlStoredProcedure will fail.')");
        a.add(b.toString());

        // Removed PageSource and Screenshot columns from execution tables.
        // 880-881
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepactioncontrolexecution` DROP COLUMN `PageSourceFilename`, DROP COLUMN `ScreenshotFilename`;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepactionexecution` DROP COLUMN `PageSourceFileName`, DROP COLUMN `ScreenshotFilename`;");
        a.add(b.toString());

        // New sql document parameter.
        // 882
        a.add(new StringBuilder("SELECT 1 FROM dual;").toString());

        // Reorganised Actions.
        // 883-892
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET `sort`='10' WHERE `idname`='ACTION' and`value`='Unknown';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET `sort`='25010' WHERE `idname`='ACTION' and`value`='skipAction';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET `sort`='24900' WHERE `idname`='ACTION' and`value`='calculateProperty';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET `sort`='99999', `description`='[DEPRECATED] getPageSource' WHERE `idname`='ACTION' and`value`='getPageSource';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET `sort`='3900' WHERE `idname`='ACTION' and`value`='rightClick';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET `sort`='3850' WHERE `idname`='ACTION' and`value`='doubleClick';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET `sort`='1000' WHERE `idname`='ACTION' and`value`='keypress';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET `sort`='5400' WHERE `idname`='ACTION' and`value`='switchToWindow';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET `sort`='5500' WHERE `idname`='ACTION' and`value`='manageDialog';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("DELETE FROM `invariant` WHERE `idname`='ACTION' and `value` in ('clickAndWait','enter','selectAndWait');");
        a.add(b.toString());

        // Reorganised Controls.
        // 893-898
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) VALUES ('CONTROL', 'getPageSource', '10100', 'Save the Page Source.', '');");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET `sort`='1500' WHERE `idname`='CONTROL' and`value`='verifyIntegerEquals';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET `sort`='1550' WHERE `idname`='CONTROL' and`value`='verifyIntegerDifferent';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET `sort`='3250' WHERE `idname`='CONTROL' and`value`='verifyElementDifferent';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET `sort`='3350' WHERE `idname`='CONTROL' and`value`='verifyElementInElement';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) VALUES ('CONTROL', 'skipControl', '15000', 'Skip the control.', '');");
        a.add(b.toString());

        // Reorganised Properties.
        // 899-908
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET `sort`='5' WHERE `idname`='PROPERTYTYPE' and`value`='text';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET `description`='Determines the data value associated with a library entry' WHERE `idname`='PROPERTYTYPE' and`value`='getFromDataLib';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET `description`='[DEPRECATED] Getting from the SOAP request using the query' WHERE `idname`='PROPERTYTYPE' and`value`='executeSoapFromLib';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET `description`='[DEPRECATED] Using an SQL from the library' WHERE `idname`='PROPERTYTYPE' and`value`='executeSqlFromLib';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET `description`='[DEPRECATED] Getting from the test Data library using the Key' WHERE `idname`='PROPERTYTYPE' and`value`='getFromTestData';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET `sort`='9999' WHERE `idname`='PROPERTYTYPE' and`value`='getFromTestData';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET `sort`='9999' WHERE `idname`='PROPERTYTYPE' and`value`='executeSqlFromLib';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET `sort`='9999' WHERE `idname`='PROPERTYTYPE' and`value`='executeSoapFromLib';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET `sort`='10' WHERE `idname`='PROPERTYTYPE' and`value`='getFromDataLib';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET `sort`='40' WHERE `idname`='PROPERTYTYPE' and`value`='getFromCookie';");
        a.add(b.toString());

        // New updated Documentation.
        // 909-910
        a.add(new StringBuilder("SELECT 1 FROM dual;").toString());
        a.add(new StringBuilder("SELECT 1 FROM dual;").toString());

        // New updated Documentation.
        // 911-912
        a.add(new StringBuilder("SELECT 1 FROM dual;").toString());
        a.add(new StringBuilder("SELECT 1 FROM dual;").toString());

        // New Action model with conditionOper and ConditionVal1.
        // 913-919
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepaction` ");
        b.append("ADD COLUMN `ConditionOper` VARCHAR(45) NOT NULL DEFAULT '' AFTER `Sort`,");
        b.append("CHANGE COLUMN `Property` `ConditionVal1` VARCHAR(2500) NULL DEFAULT '' AFTER `ConditionOper`,");
        b.append("CHANGE COLUMN `Object` `Value1` VARCHAR(2500) NOT NULL DEFAULT '' ,");
        b.append("ADD COLUMN `Value2` VARCHAR(2500) NOT NULL DEFAULT '' AFTER `Value1`;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepactionexecution` ");
        b.append("CHANGE COLUMN `Description` `Description` VARCHAR(255) NOT NULL DEFAULT '' AFTER `ForceExeStatus`,");
        b.append("CHANGE COLUMN `ReturnCode` `ReturnCode` VARCHAR(2) NULL DEFAULT NULL AFTER `Description`,");
        b.append("CHANGE COLUMN `ReturnMessage` `ReturnMessage` VARCHAR(500) NULL DEFAULT NULL AFTER `ReturnCode`,");
        b.append("CHANGE COLUMN `Object` `Value1` VARCHAR(2500) NULL DEFAULT '' ,");
        b.append("CHANGE COLUMN `Property` `Value2` VARCHAR(2500) NULL DEFAULT '' ,");
        b.append("ADD COLUMN `ConditionOper` VARCHAR(45) AFTER `Sort`,");
        b.append("ADD COLUMN `ConditionVal1` VARCHAR(2500) AFTER `ConditionOper`,");
        b.append("ADD COLUMN `Value1Init` VARCHAR(2500) NULL DEFAULT '' AFTER `Action`,");
        b.append("ADD COLUMN `Value2Init` VARCHAR(2500) NULL DEFAULT '' AFTER `Value1Init`;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE testcasestepaction SET ConditionOper = 'ifPropertyExist' where ConditionVal1<>''; ");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE testcasestepaction SET ConditionOper = 'always' where ConditionOper=''; ");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) VALUES");
        b.append("  ('ACTIONCONDITIONOPER', 'always', '100', 'Always.', '')");
        b.append(", ('ACTIONCONDITIONOPER', 'ifPropertyExist', '200', 'Only execute if property exist for the execution.', '')");
        b.append(", ('ACTIONCONDITIONOPER', 'never', '9999', 'Never execute the action.', '')");
        b.append(", ('INVARIANTPRIVATE', 'ACTIONCONDITIONOPER', '550', '', '');");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE testcasestepaction SET Value2 = concat('%', ConditionVal1, '%') where ConditionVal1<>'' and action not in ('calculateProperty'); ");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE testcasestepaction SET Value1 = ConditionVal1, Value2='' where action in ('calculateProperty'); ");
        a.add(b.toString());

        // New updated Documentation.
        // 920-921
        b = new StringBuilder();
        b.append("select 1 from DUAL;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("select 1 from DUAL;");
        a.add(b.toString());

        // New updated Documentation.
        // 922
        b = new StringBuilder();
        b.append("select 1 from DUAL;");
        a.add(b.toString());

        // Corrected ConditionVal1 in order to remove (.
        // 923
        b = new StringBuilder();
        b.append("UPDATE testcasestepaction SET ConditionVal1 = left(ConditionVal1,locate('(',ConditionVal1)-1) WHERE conditionval1 like '%(%';");
        a.add(b.toString());

        // Add menuDocumentation and menuHelp in documentation table.
        // 924
        b = new StringBuilder();
        b.append("select 1 from DUAL;");
        a.add(b.toString());

        // New updated Documentation.
        // 925
        b = new StringBuilder();
        b.append("select 1 from DUAL;");
        a.add(b.toString());

        // New updated Documentation.
        // 926
        b = new StringBuilder();
        b.append("select 1 from DUAL;");
        a.add(b.toString());

        // Removed all field and added userAgent on testCase table.
        // 927
        b = new StringBuilder();
        b.append("ALTER TABLE `testcase` ");
        b.append("DROP COLUMN `ChainNumberNeeded`,");
        b.append("DROP COLUMN `ReadOnly`,");
        b.append("ADD COLUMN `useragent` VARCHAR(250) NULL DEFAULT '' AFTER `function`,");
        b.append("CHANGE COLUMN `Creator` `UsrCreated` VARCHAR(45) NOT NULL DEFAULT '' AFTER `useragent`,");
        b.append("CHANGE COLUMN `TCDateCrea` `DateCreated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER `UsrCreated`,");
        b.append("CHANGE COLUMN `LastModifier` `UsrModif` VARCHAR(45) NULL DEFAULT '' AFTER `DateCreated`,");
        b.append("CHANGE COLUMN `last_modified` `DateModif` TIMESTAMP NOT NULL DEFAULT '1970-01-01 01:01:01' ;");
        a.add(b.toString());

        // New updated Documentation.
        // 928
        b = new StringBuilder();
        b.append("select 1 from DUAL;");
        a.add(b.toString());

        // New updated Documentation.
        // 929
        b = new StringBuilder();
        b.append("select 1 from DUAL;");
        a.add(b.toString());

        // Remove COMPARATIVE and PROCESS Groups.
        // 930 - 932
        b = new StringBuilder();
        b.append("DELETE FROM `invariant` WHERE `idname`='GROUP' and`value` in ('COMPARATIVE','PROCESS');");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE testcase SET `group`='MANUAL' WHERE `group` in ('COMPARATIVE', 'PROCESS');");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("select 1 from DUAL;");
        a.add(b.toString());

        // New updated Documentation.
        // 933
        b = new StringBuilder();
        b.append("select 1 from DUAL;");
        a.add(b.toString());

        // New updated Documentation.
        // 934
        b = new StringBuilder();
        b.append("select 1 from DUAL;");
        a.add(b.toString());

        // Altering testcase table to put notnull with default empty value in most columns.
        // 935-936
        b = new StringBuilder();
        b.append("UPDATE testcase set ");
        b.append("`BehaviorOrValueExpected` = coalesce(`BehaviorOrValueExpected`, ''), ");
        b.append("`howto` = coalesce(`howto`, ''), ");
        b.append("`Group` = coalesce(`Group`,''),");
        b.append("`Origine` = coalesce(`Origine`,''),");
        b.append("`RefOrigine` = coalesce(`RefOrigine`,''),");
        b.append("`Comment` = coalesce(`Comment`,''),");
        b.append("`FromBuild` = coalesce(`FromBuild`,''),");
        b.append("`FromRev` = coalesce(`FromRev`,''),");
        b.append("`ToBuild` = coalesce(`ToBuild`,''),");
        b.append("`ToRev` = coalesce(`ToRev`,''),");
        b.append("`BugID` = coalesce(`BugID`,''),");
        b.append("`TargetBuild` = coalesce(`TargetBuild`,''),");
        b.append("`TargetRev` = coalesce(`TargetRev`,''),");
        b.append("`Implementer` = coalesce(`Implementer`,'')");
        b.append("where `BehaviorOrValueExpected` is null or `howto` is null or `Group` is null or `Origine` is null or `RefOrigine` is null or `Comment` is null or `FromBuild` is null or `FromRev` is null or `ToBuild` is null or `ToRev` is null or `BugID` is null or `TargetBuild` is null or `TargetRev` is null or `Implementer` is null");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcase` ");
        b.append("CHANGE COLUMN `BehaviorOrValueExpected` `BehaviorOrValueExpected` TEXT NOT NULL ,");
        b.append("CHANGE COLUMN `Group` `Group` VARCHAR(45) NOT NULL DEFAULT ''  ,");
        b.append("CHANGE COLUMN `Origine` `Origine` VARCHAR(45) NOT NULL DEFAULT ''  ,");
        b.append("CHANGE COLUMN `RefOrigine` `RefOrigine` VARCHAR(45) NOT NULL DEFAULT ''  ,");
        b.append("CHANGE COLUMN `HowTo` `HowTo` TEXT NOT NULL  ,");
        b.append("CHANGE COLUMN `Comment` `Comment` VARCHAR(500) NOT NULL DEFAULT ''  ,");
        b.append("CHANGE COLUMN `FromBuild` `FromBuild` VARCHAR(10) NOT NULL DEFAULT ''  ,");
        b.append("CHANGE COLUMN `FromRev` `FromRev` VARCHAR(20) NOT NULL DEFAULT ''  ,");
        b.append("CHANGE COLUMN `ToBuild` `ToBuild` VARCHAR(10) NOT NULL DEFAULT ''  ,");
        b.append("CHANGE COLUMN `ToRev` `ToRev` VARCHAR(20) NOT NULL DEFAULT ''  ,");
        b.append("CHANGE COLUMN `BugID` `BugID` VARCHAR(10) NOT NULL DEFAULT ''  ,");
        b.append("CHANGE COLUMN `TargetBuild` `TargetBuild` VARCHAR(10) NOT NULL DEFAULT ''  ,");
        b.append("CHANGE COLUMN `TargetRev` `TargetRev` VARCHAR(20) NOT NULL DEFAULT ''  ,");
        b.append("CHANGE COLUMN `Implementer` `Implementer` VARCHAR(45) NOT NULL DEFAULT ''  ;");
        a.add(b.toString());

        // New Parameter for Property calculation retry.
        // 937-938
        b = new StringBuilder();
        b.append("INSERT INTO `parameter` (`system`, `param`, `value`, `description`) VALUES ('', 'cerberus_property_maxretry', '50', 'Integer that correspond to the maximum number of retry allowed when calculating a property.');");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("INSERT INTO `parameter` (`system`, `param`, `value`, `description`) VALUES ('', 'cerberus_property_maxretrytotalduration', '1800000', 'Integer (in ms) that correspond to the maximum duration of the property calculation. In case the period is greated than this parameter, the period value will be replaced by this parameter with 1 single retry. If number of retries x period is greated than this parameter, the number of retry will be reduced to fit the constrain.');");
        a.add(b.toString());

        // New updated Documentation.
        // 939
        b = new StringBuilder();
        b.append("select 1 from DUAL;");
        a.add(b.toString());

        // New updated Documentation.
        // 940
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasecountryproperties` ");
        b.append("ADD COLUMN `RetryNb` INT(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '' AFTER `Nature`,");
        b.append("ADD COLUMN `RetryPeriod` INT(10) UNSIGNED NOT NULL DEFAULT 10000 COMMENT '' AFTER `RetryNb`;");
        a.add(b.toString());

        // New updated Documentation.
        // 941-942
        b = new StringBuilder();
        b.append("select 1 from DUAL;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("select 1 from DUAL;");
        a.add(b.toString());

        // Resize Script column.
        // 943
        b = new StringBuilder();
        b.append("ALTER TABLE `testdatalib` ");
        b.append("CHANGE COLUMN `Script` `Script` TEXT NOT NULL ;");
        a.add(b.toString());

        // Updated Documentation
        // 944
        b = new StringBuilder();
        b.append("select 1 from DUAL;");
        a.add(b.toString());

        // Add timeout parameters replacing the existing one.
        // 945-947
        b = new StringBuilder();
        b.append("INSERT INTO `parameter` (`system`, `param`, `value`, `description`) VALUES ");
        b.append("('', 'cerberus_selenium_pageLoadTimeout', '45000', 'Integer that correspond to the number of milliseconds that selenium will wait before give timeout, when loading a page.'),");
        b.append("('', 'cerberus_selenium_implicitlyWait', '0', 'Integer that correspond to the number of milliseconds that selenium will implicitely wait when searching an element.'),");
        b.append("('', 'cerberus_selenium_setScriptTimeout', '45000', 'Integer that correspond to the number of milliseconds that selenium will wait before give timeout, when executing a Javascript Script.'),");
        b.append("('', 'cerberus_action_wait_default', '45000', 'Integer that correspond to the number of milliseconds that cerberus will wait by default using the wait action.'),");
        b.append("('', 'cerberus_selenium_wait_element', '45000', 'Integer that correspond to the number of milliseconds that selenium will wait before give timeout, when searching an element.'),");
        b.append("('', 'cerberus_appium_wait_element', '45000', 'Integer that correspond to the number of milliseconds that appium will wait before give timeout, when searching an element.');");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE parameter p2 set `value` = (select * from (select `value` * 1000 from parameter p1 where p1.`param` = 'selenium_defaultWait' and p1.`system` = '') p3 ) ");
        b.append("where p2.`param` in ('cerberus_selenium_wait_element', 'cerberus_selenium_setScriptTimeout', 'cerberus_selenium_pageLoadTimeout','cerberus_appium_wait_element' , 'cerberus_action_wait_default');");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("DELETE FROM parameter where `param` = 'selenium_defaultWait' ");
        a.add(b.toString());

        // Cleaned testcaseexecutiondata table keeping all values of testcasecountryproperty.
        // 948
        b = new StringBuilder();
        b.append("ALTER TABLE `testcaseexecutiondata` ");
        b.append("CHANGE COLUMN `Type` `Type` VARCHAR(45) NULL DEFAULT NULL AFTER `Index`,");
        b.append("CHANGE COLUMN `RC` `RC` VARCHAR(10) NULL DEFAULT NULL AFTER `EndLong`,");
        b.append("CHANGE COLUMN `RMessage` `RMessage` TEXT NULL AFTER `RC`,");
        b.append("CHANGE COLUMN `Description` `Description` VARCHAR(255) NULL DEFAULT '' AFTER `RMessage`,");
        b.append("CHANGE COLUMN `Value` `Value` TEXT NOT NULL ,");
        b.append("CHANGE COLUMN `Value1` `Value1` TEXT NULL ,");
        b.append("CHANGE COLUMN `Value2` `Value2` TEXT NULL ,");
        b.append("ADD COLUMN `Database` VARCHAR(45) NULL AFTER `Value`,");
        b.append("ADD COLUMN `Value1Init` TEXT NULL AFTER `Database`,");
        b.append("ADD COLUMN `Value2Init` TEXT NULL AFTER `Value1Init`,");
        b.append("ADD COLUMN `Length` INT(10) NULL AFTER `Value2`,");
        b.append("ADD COLUMN `RowLimit` INT(10) NULL AFTER `Length`,");
        b.append("ADD COLUMN `Nature` VARCHAR(45) NULL AFTER `RowLimit`,");
        b.append("ADD COLUMN `RetryNb` INT(10) NULL AFTER `Nature`,");
        b.append("ADD COLUMN `RetryPeriod` INT(10) NULL AFTER `RetryNb`;");
        a.add(b.toString());

        // Cleaned testcasestepactioncontrol table.
        // 949-953
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepactioncontrol` ");
        b.append("CHANGE COLUMN `Control` `ControlSequence` INT(10) UNSIGNED NOT NULL ,");
        b.append("CHANGE COLUMN `Type` `Control` VARCHAR(200) NOT NULL DEFAULT '' ,");
        b.append("CHANGE COLUMN `ControlProperty` `Value1` TEXT NULL AFTER `Control`,");
        b.append("CHANGE COLUMN `ControlValue` `Value2` TEXT NULL  AFTER `Value1`,");
        b.append("CHANGE COLUMN `ControlDescription` `Description` VARCHAR(255) NOT NULL DEFAULT '' ,");
        b.append("CHANGE COLUMN `Fatal` `Fatal` VARCHAR(1) NULL DEFAULT 'Y' AFTER `Value2`,");
        b.append("DROP PRIMARY KEY, ADD PRIMARY KEY USING BTREE (`Test`, `TestCase`, `Step`, `Sequence`, `ControlSequence`) ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepactioncontrolexecution` ");
        b.append("CHANGE COLUMN `Control` `ControlSequence` INT(10) UNSIGNED NOT NULL ,");
        b.append("CHANGE COLUMN `ControlType` `Control` VARCHAR(200) NULL DEFAULT NULL ,");
        b.append("ADD COLUMN `Value1Init` TEXT NULL AFTER `Control`,");
        b.append("ADD COLUMN `Value2Init` TEXT NULL AFTER `Value1Init`,");
        b.append("CHANGE COLUMN `ControlProperty` `Value1` TEXT NULL AFTER `Value2Init`,");
        b.append("CHANGE COLUMN `ControlValue` `Value2` TEXT NULL ,");
        b.append("CHANGE COLUMN `Description` `Description` VARCHAR(255) NOT NULL DEFAULT '' AFTER `Fatal`,");
        b.append("CHANGE COLUMN `ReturnCode` `ReturnCode` VARCHAR(2) NOT NULL AFTER `Description`,");
        b.append("CHANGE COLUMN `ReturnMessage` `ReturnMessage` TEXT NULL AFTER `ReturnCode`;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasecountryproperties` ");
        b.append("CHANGE COLUMN `Description` `Description` VARCHAR(255) NULL AFTER `RetryPeriod`,");
        b.append("CHANGE COLUMN `Value1` `Value1` TEXT NULL  ,");
        b.append("CHANGE COLUMN `Value2` `Value2` TEXT NULL  ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepaction` ");
        b.append("CHANGE COLUMN `ConditionVal1` `ConditionVal1` TEXT NULL  ,");
        b.append("CHANGE COLUMN `Value1` `Value1` TEXT NOT NULL  ,");
        b.append("CHANGE COLUMN `Value2` `Value2` TEXT NOT NULL  ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepactionexecution` ");
        b.append("CHANGE COLUMN `ConditionVal1` `ConditionVal1` TEXT NULL  ,");
        b.append("CHANGE COLUMN `Value1Init` `Value1Init` TEXT NULL  ,");
        b.append("CHANGE COLUMN `Value2Init` `Value2Init` TEXT NULL  ,");
        b.append("CHANGE COLUMN `Value1` `Value1` TEXT NULL  ,");
        b.append("CHANGE COLUMN `Value2` `Value2` TEXT NULL  ,");
        b.append("CHANGE COLUMN `ReturnMessage` `ReturnMessage` TEXT NULL ;");
        a.add(b.toString());

        // Add value2 usage to the calculateProperty action.
        // 954
        b = new StringBuilder();
        b.append("select 1 from DUAL;");
        a.add(b.toString());

        // Remove HTML Escape encoding in soap library
        // 955
        b = new StringBuilder();
        b.append("Update soaplibrary set `envelope` = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(`envelope`, '&amp;', '&'),'&lt;','<'),'&gt;','>'),'&apos;','\\''),'&quot;','\\\"')");
        a.add(b.toString());

        // New updated Documentation.
        // 956-957
        b = new StringBuilder();
        b.append("select 1 from DUAL;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("select 1 from DUAL;");
        a.add(b.toString());

        // New updated Documentation.
        // 958
        b = new StringBuilder();
        b.append("select 1 from DUAL;");
        a.add(b.toString());

        // Add path to picture for appliation object in paramaters
        // 959
        b = new StringBuilder();
        b.append("INSERT INTO `parameter` VALUES ");
        b.append("('','cerberus_applicationobject_path','','Path whare you will store all the files you upload in application object');");
        a.add(b.toString());

        // Create Table Application Object
        // 960
        b = new StringBuilder();
        b.append("CREATE TABLE `applicationobject` ("
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
        a.add(b.toString());

        // Documentation update.
        // 961-962
        b = new StringBuilder();
        b.append("select 1 from DUAL;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("select 1 from DUAL;");
        a.add(b.toString());

        // PArameter new ExecutionDetail Page
        // 963
        b = new StringBuilder();
        b.append("INSERT INTO `parameter` VALUES ('','cerberus_executiondetail_use','Y','Do you want to use the new Execution Detail Page (Y or N)')");
        a.add(b.toString());

        // Add tracability fields in testcasestep table.
        // 964
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestep` ");
        b.append("DROP COLUMN `last_modified`,");
        b.append("ADD COLUMN `UsrCreated` VARCHAR(45) NOT NULL DEFAULT '' AFTER `inlibrary`,");
        b.append("ADD COLUMN `DateCreated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER `UsrCreated`,");
        b.append("ADD COLUMN `UsrModif` VARCHAR(45) NULL DEFAULT '' AFTER `DateCreated`,");
        b.append("ADD COLUMN `DateModif` TIMESTAMP NOT NULL DEFAULT '1970-01-01 01:01:01' ;");
        a.add(b.toString());

        // Clean Steps that use steps that also use step (we remove the link).
        // 965
        b = new StringBuilder();
        b.append("UPDATE testcasestep tcs SET DateModif=now(), UsrModif = 'DatabaseMaintenanceV964', useStep='N', UseStepTest='', UseStepTestCase='', UseStepStep=-1 where concat(tcs.test, '||', tcs.testcase, '||', tcs.step, '||') in (select concat(toto.test, '||', toto.testcase, '||', toto.step, '||') from (select tcsa.* from testcasestep tcsa join testcasestep tcs1 on tcs1.test=tcsa.useSteptest and tcs1.testcase=tcsa.useSteptestcase and tcs1.step=tcsa.useStepstep where tcsa.useStep = 'Y' and tcs1.useStep='Y' order by tcs1.test, tcs1.testcase, tcs1.step) as toto );");
        a.add(b.toString());

        // Clean Steps that are used but not flagged as inLibrary (we flag them as can be used inLibrary='Y').
        // 966
        b = new StringBuilder();
        b.append("UPDATE testcasestep tcs SET DateModif=now(), UsrModif = 'DatabaseMaintenanceV965', inLibrary='Y' where concat(tcs.test, '||', tcs.testcase, '||', tcs.step, '||') in (select concat(toto.test, '||', toto.testcase, '||', toto.step, '||') from (select tcs1.* from testcasestep tcsa join testcasestep tcs1 on tcs1.test=tcsa.useSteptest and tcs1.testcase=tcsa.useSteptestcase and tcs1.step=tcsa.useStepstep where tcsa.useStep = 'Y' and tcs1.inLibrary!='Y' order by tcs1.test, tcs1.testcase, tcs1.step) as toto );");
        a.add(b.toString());

        // Documentation typo.
        // 967
        b = new StringBuilder();
        b.append("select 1 from DUAL;");
        a.add(b.toString());

        // New Control model with conditionOper and ConditionVal1.
        // 968-971
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepactioncontrol` ");
        b.append("ADD COLUMN `ConditionOper` VARCHAR(45) NOT NULL DEFAULT '' AFTER `Sort`,");
        b.append("ADD COLUMN `ConditionVal1` TEXT AFTER `ConditionOper`;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepactioncontrolexecution` ");
        b.append("ADD COLUMN `ConditionOper` VARCHAR(45) AFTER `Sort`,");
        b.append("ADD COLUMN `ConditionVal1` TEXT AFTER `ConditionOper`;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE testcasestepactioncontrol SET ConditionOper = 'always' where ConditionOper=''; ");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) VALUES");
        b.append("  ('CONTROLCONDITIONOPER', 'always', '100', 'Always.', '')");
        b.append(", ('CONTROLCONDITIONOPER', 'ifPropertyExist', '200', 'Only execute if property exist for the execution.', '')");
        b.append(", ('CONTROLCONDITIONOPER', 'never', '9999', 'Never execute the control.', '')");
        b.append(", ('INVARIANTPRIVATE', 'CONTROLCONDITIONOPER', '560', '', '');");
        a.add(b.toString());

        // Resize login.
        // 972-978
        b = new StringBuilder();
        b.append("ALTER TABLE `usersystem` DROP FOREIGN KEY `FK_usersystem_01`;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `usersystem` CHANGE COLUMN `Login` `Login` VARCHAR(255) NOT NULL ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `usergroup` DROP FOREIGN KEY `FK_usergroup_01`;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `usergroup` CHANGE COLUMN `Login` `Login` VARCHAR(255) NOT NULL ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `user` CHANGE COLUMN `Login` `Login` VARCHAR(255) NOT NULL ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `usersystem` ADD CONSTRAINT `FK_usersystem_01` FOREIGN KEY (`Login`) REFERENCES `user` (`Login`) ON DELETE CASCADE ON UPDATE CASCADE;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `usergroup` ADD CONSTRAINT `FK_usergroup_01` FOREIGN KEY (`Login`) REFERENCES `user` (`Login`) ON DELETE CASCADE ON UPDATE CASCADE;");
        a.add(b.toString());

        // Add path to picture for appliation object in paramaters
        // 979
        b = new StringBuilder();
        b.append("INSERT INTO `parameter` VALUES ");
        b.append("('','cerberus_featureflipping_activatewebsocketpush','Y','Boolean that enable/disable the websocket push.');");
        a.add(b.toString());

        // New Step model with conditionOper and ConditionVal1.
        // 980-983
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestep` ");
        b.append("ADD COLUMN `ConditionOper` VARCHAR(45) NOT NULL DEFAULT '' AFTER `Sort`,");
        b.append("ADD COLUMN `ConditionVal1` TEXT AFTER `ConditionOper`;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepexecution` ");
        b.append("ADD COLUMN `ConditionOper` VARCHAR(45) AFTER `Sort`,");
        b.append("ADD COLUMN `ConditionVal1` TEXT AFTER `ConditionOper`;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE testcasestep SET ConditionOper = 'always' where ConditionOper=''; ");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) VALUES");
        b.append("  ('STEPCONDITIONOPER', 'always', '100', 'Always.', '')");
        b.append(", ('STEPCONDITIONOPER', 'ifPropertyExist', '200', 'Only execute if property exist for the execution.', '')");
        b.append(", ('STEPCONDITIONOPER', 'never', '9999', 'Never execute the control.', '')");
        b.append(", ('INVARIANTPRIVATE', 'STEPCONDITIONOPER', '570', '', '');");
        a.add(b.toString());

        // New testcase model with conditionOper and ConditionVal1.
        // 984-990
        b = new StringBuilder();
        b.append("ALTER TABLE `testcase` ");
        b.append("ADD COLUMN `ConditionOper` VARCHAR(45) NOT NULL DEFAULT '' AFTER `TcActive`,");
        b.append("ADD COLUMN `ConditionVal1` TEXT AFTER `ConditionOper`;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE testcase SET ConditionOper = 'always' where ConditionOper=''; ");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) VALUES");
        b.append("  ('TESTCASECONDITIONOPER', 'always', '100', 'Always.', '')");
        b.append(", ('TESTCASECONDITIONOPER', 'ifPropertyExist', '200', 'Only execute if property exist for the execution.', '')");
        b.append(", ('TESTCASECONDITIONOPER', 'never', '9999', 'Never execute the control.', '')");
        b.append(", ('INVARIANTPRIVATE', 'TESTCASECONDITIONOPER', '580', '', '');");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE testcase SET ConditionVal1 = '' where ConditionVal1 is null; ");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE testcasestep SET ConditionVal1 = '' where ConditionVal1 is null; ");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE testcasestepexecution SET ConditionVal1 = '' where ConditionVal1 is null; ");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE testcasestepactioncontrol SET ConditionVal1 = '' where ConditionVal1 is null; ");
        a.add(b.toString());

        // Removed skipAction and skipControl and replaced by conditionOper = never.
        // 991-994
        b = new StringBuilder();
        b.append("DELETE from invariant where idname = 'ACTION' and value = 'skipAction';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("DELETE from invariant where idname = 'CONTROL' and value = 'skipControl';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE testcasestepaction Set ConditionOper = 'never', Action = 'Unknown' where Action = 'skipAction';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE testcasestepactioncontrol Set ConditionOper = 'never', Control = 'Unknown' where Control = 'skipControl';");
        a.add(b.toString());

        // New Appium capabtilities for IOS testing
        // 995-997
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) SELECT * from (SELECT 'CAPABILITY', 'udid', '8', 'Unique Device IDentifier (useful for IOS testing)', '') AS tmp WHERE NOT EXISTS ( SELECT `value` FROM `invariant` WHERE idname='CAPABILITY' and `value`='udid') LIMIT 1;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) SELECT * from (SELECT 'CAPABILITY', 'xcodeConfigFile', '9', 'Path to the Xcode Configuration File containing information about application sign (useful for IOS testing)', '') AS tmp WHERE NOT EXISTS ( SELECT `value` FROM `invariant` WHERE idname='CAPABILITY' and `value`='xcodeConfigFile') LIMIT 1;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) SELECT * from (SELECT 'CAPABILITY', 'realDeviceLogger', '10', 'Path to the logger for real IOS devices (useful for IOS testing)', '') AS tmp WHERE NOT EXISTS ( SELECT `value` FROM `invariant` WHERE idname='CAPABILITY' and  `value`='realDeviceLogger') LIMIT 1;");
        a.add(b.toString());

        // Updated Documentation
        // 998
        b = new StringBuilder();
        b.append("select 1 from DUAL;");
        a.add(b.toString());

        // New ConditionVal2 columns
        // 999-1009
        b = new StringBuilder();
        b.append("ALTER TABLE `testcase` ADD COLUMN `ConditionVal2` TEXT NULL AFTER `ConditionVal1`;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestep` ADD COLUMN `ConditionVal2` TEXT NULL AFTER `ConditionVal1`;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepaction` ADD COLUMN `ConditionVal2` TEXT NULL AFTER `ConditionVal1`;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepactioncontrol` ADD COLUMN `ConditionVal2` TEXT NULL AFTER `ConditionVal1`;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepexecution` ADD COLUMN `ConditionVal2` TEXT NULL AFTER `ConditionVal1`;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepactionexecution` ADD COLUMN `ConditionVal2` TEXT NULL AFTER `ConditionVal1`;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepactioncontrolexecution` ADD COLUMN `ConditionVal2` TEXT NULL AFTER `ConditionVal1`;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `testcase` SET `ConditionVal2` = '';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `testcasestep` SET `ConditionVal2` = '';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `testcasestepaction` SET `ConditionVal2` = '';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `testcasestepactioncontrol` SET `ConditionVal2` = '';");
        a.add(b.toString());

        //Update doc - 1010
        b = new StringBuilder();
        b.append("select 1 from DUAL;");
        a.add(b.toString());

        //Update doc - 1011
        b = new StringBuilder();
        b.append("select 1 from DUAL;");
        a.add(b.toString());

        //Adding new condition at all levels
        // 1012-1015
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` VALUES ");
        b.append("('ACTIONCONDITIONOPER', 'ifNumericEqual', 300, 'Only execute if value1 equals value2.', '', '', '', '')");
        b.append(",('ACTIONCONDITIONOPER', 'ifNumericDifferent', 310, 'Only execute if value1 is different from value2.', '', '', '', '')");
        b.append(",('ACTIONCONDITIONOPER', 'ifNumericGreater', 320, 'Only execute if value1 greater than value2.', '', '', '', '')");
        b.append(",('ACTIONCONDITIONOPER', 'ifNumericGreaterOrEqual', 330, 'Only execute if value1 greater or equal than value2.', '', '', '', '')");
        b.append(",('ACTIONCONDITIONOPER', 'ifNumericMinor', 340, 'Only execute if value1 lower than value2.', '', '', '', '')");
        b.append(",('ACTIONCONDITIONOPER', 'ifNumericMinorOrEqual', 350, 'Only execute if value1 lower or equal than value2.', '', '', '', '')");
        b.append(",('ACTIONCONDITIONOPER', 'ifStringEqual', 400, 'Only execute if value1 equals value2.', '', '', '', '')");
        b.append(",('ACTIONCONDITIONOPER', 'ifStringDifferent', 410, 'Only execute if value1 different from value2.', '', '', '', '')");
        b.append(",('ACTIONCONDITIONOPER', 'ifStringGreater', 420, 'Only execute if value1 greater than value2.', '', '', '', '')");
        b.append(",('ACTIONCONDITIONOPER', 'ifStringMinor', 430, 'Only execute if value1 lower than value2.', '', '', '', '')");
        b.append(",('ACTIONCONDITIONOPER', 'ifStringContains', 440, 'Only execute if value1 contains value2.', '', '', '', '');");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` VALUES ");
        b.append("('STEPCONDITIONOPER', 'ifNumericEqual', 300, 'Only execute if value1 equals value2.', '', '', '', '')");
        b.append(",('STEPCONDITIONOPER', 'ifNumericDifferent', 310, 'Only execute if value1 is different from value2.', '', '', '', '')");
        b.append(",('STEPCONDITIONOPER', 'ifNumericGreater', 320, 'Only execute if value1 greater than value2.', '', '', '', '')");
        b.append(",('STEPCONDITIONOPER', 'ifNumericGreaterOrEqual', 330, 'Only execute if value1 greater or equal than value2.', '', '', '', '')");
        b.append(",('STEPCONDITIONOPER', 'ifNumericMinor', 340, 'Only execute if value1 lower than value2.', '', '', '', '')");
        b.append(",('STEPCONDITIONOPER', 'ifNumericMinorOrEqual', 350, 'Only execute if value1 lower or equal than value2.', '', '', '', '')");
        b.append(",('STEPCONDITIONOPER', 'ifStringEqual', 400, 'Only execute if value1 equals value2.', '', '', '', '')");
        b.append(",('STEPCONDITIONOPER', 'ifStringDifferent', 410, 'Only execute if value1 different from value2.', '', '', '', '')");
        b.append(",('STEPCONDITIONOPER', 'ifStringGreater', 420, 'Only execute if value1 greater than value2.', '', '', '', '')");
        b.append(",('STEPCONDITIONOPER', 'ifStringMinor', 430, 'Only execute if value1 lower than value2.', '', '', '', '')");
        b.append(",('STEPCONDITIONOPER', 'ifStringContains', 440, 'Only execute if value1 contains value2.', '', '', '', '');");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` VALUES ");
        b.append("('CONTROLCONDITIONOPER', 'ifNumericEqual', 300, 'Only execute if value1 equals value2.', '', '', '', '')");
        b.append(",('CONTROLCONDITIONOPER', 'ifNumericDifferent', 310, 'Only execute if value1 is different from value2.', '', '', '', '')");
        b.append(",('CONTROLCONDITIONOPER', 'ifNumericGreater', 320, 'Only execute if value1 greater than value2.', '', '', '', '')");
        b.append(",('CONTROLCONDITIONOPER', 'ifNumericGreaterOrEqual', 330, 'Only execute if value1 greater or equal than value2.', '', '', '', '')");
        b.append(",('CONTROLCONDITIONOPER', 'ifNumericMinor', 340, 'Only execute if value1 lower than value2.', '', '', '', '')");
        b.append(",('CONTROLCONDITIONOPER', 'ifNumericMinorOrEqual', 350, 'Only execute if value1 lower or equal than value2.', '', '', '', '')");
        b.append(",('CONTROLCONDITIONOPER', 'ifStringEqual', 400, 'Only execute if value1 equals value2.', '', '', '', '')");
        b.append(",('CONTROLCONDITIONOPER', 'ifStringDifferent', 410, 'Only execute if value1 different from value2.', '', '', '', '')");
        b.append(",('CONTROLCONDITIONOPER', 'ifStringGreater', 420, 'Only execute if value1 greater than value2.', '', '', '', '')");
        b.append(",('CONTROLCONDITIONOPER', 'ifStringMinor', 430, 'Only execute if value1 lower than value2.', '', '', '', '')");
        b.append(",('CONTROLCONDITIONOPER', 'ifStringContains', 440, 'Only execute if value1 contains value2.', '', '', '', '');");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` VALUES ");
        b.append("('TESTCASECONDITIONOPER', 'ifNumericEqual', 300, 'Only execute if value1 equals value2.', '', '', '', '')");
        b.append(",('TESTCASECONDITIONOPER', 'ifNumericDifferent', 310, 'Only execute if value1 is different from value2.', '', '', '', '')");
        b.append(",('TESTCASECONDITIONOPER', 'ifNumericGreater', 320, 'Only execute if value1 greater than value2.', '', '', '', '')");
        b.append(",('TESTCASECONDITIONOPER', 'ifNumericGreaterOrEqual', 330, 'Only execute if value1 greater or equal than value2.', '', '', '', '')");
        b.append(",('TESTCASECONDITIONOPER', 'ifNumericMinor', 340, 'Only execute if value1 lower than value2.', '', '', '', '')");
        b.append(",('TESTCASECONDITIONOPER', 'ifNumericMinorOrEqual', 350, 'Only execute if value1 lower or equal than value2.', '', '', '', '')");
        b.append(",('TESTCASECONDITIONOPER', 'ifStringEqual', 400, 'Only execute if value1 equals value2.', '', '', '', '')");
        b.append(",('TESTCASECONDITIONOPER', 'ifStringDifferent', 410, 'Only execute if value1 different from value2.', '', '', '', '')");
        b.append(",('TESTCASECONDITIONOPER', 'ifStringGreater', 420, 'Only execute if value1 greater than value2.', '', '', '', '')");
        b.append(",('TESTCASECONDITIONOPER', 'ifStringMinor', 430, 'Only execute if value1 lower than value2.', '', '', '', '')");
        b.append(",('TESTCASECONDITIONOPER', 'ifStringContains', 440, 'Only execute if value1 contains value2.', '', '', '', '');");
        a.add(b.toString());

        //Adding FAT Client Application Type
        // 1016
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` VALUES ");
        b.append("('APPLITYPE', 'FAT', '60', 'FAT client application', '', '', '', '')");
        a.add(b.toString());

        //Adding index column on execution step in order to prepare changes for looping steps
        // 1017-1028
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepactioncontrolexecution` ");
        b.append("DROP FOREIGN KEY `FK_testcasestepactioncontrolexecution_01`;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepactionexecution` ");
        b.append("DROP FOREIGN KEY `FK_testcasestepactionexecution_01`;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepexecution`");
        b.append("ADD COLUMN `index` INT(11) NOT NULL DEFAULT '1' AFTER `Step`,");
        b.append("DROP PRIMARY KEY,");
        b.append("ADD PRIMARY KEY (`ID`, `Test`, `TestCase`, `Step`, `index`) ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepactioncontrolexecution` ");
        b.append("ADD COLUMN `index` INT(11) NOT NULL DEFAULT '1' AFTER `Step`;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepactioncontrolexecution` ADD CONSTRAINT `FK_testcasestepactioncontrolexecution_01` FOREIGN KEY (`ID` , `Test` , `TestCase` , `Step`, `index` ) REFERENCES `testcasestepexecution` (`ID` , `Test` , `TestCase` , `Step` , `index` ) ON DELETE CASCADE ON UPDATE CASCADE ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepactionexecution` ");
        b.append("ADD COLUMN `index` INT(11) NOT NULL DEFAULT '1' AFTER `Step`;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepactionexecution` ADD CONSTRAINT `FK_testcasestepactionexecution_01` FOREIGN KEY (`ID` , `Test` , `TestCase` , `Step` , `index`) REFERENCES `testcasestepexecution` (`ID` , `Test` , `TestCase` , `Step` , `index`) ON DELETE CASCADE ON UPDATE CASCADE;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestep` ");
        b.append("ADD COLUMN `Loop` VARCHAR(45) NOT NULL DEFAULT '' AFTER `Sort`;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepactionexecution` ");
        b.append("DROP PRIMARY KEY,");
        b.append("ADD PRIMARY KEY (`ID`, `Test`, `TestCase`, `Step`, `index`, `Sequence`)  ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepactioncontrolexecution` ");
        b.append("DROP PRIMARY KEY,");
        b.append("ADD PRIMARY KEY (`ID`, `Test`, `TestCase`, `Step`, `index`, `Sequence`, `ControlSequence`) ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` VALUES ");
        b.append("('STEPLOOP', 'onceIfConditionTrue', 100, 'We execute the step once only if the condiion is true.', '', '', '', '')");
        b.append(",('STEPLOOP', 'onceIfConditionFalse', 200, 'We execute the step once only if the condiion is false.', '', '', '', '')");
        b.append(",('STEPLOOP', 'doWhileConditionTrue', 300, 'We execute the step and then execute it again and again as long as condition is true.', '', '', '', '')");
        b.append(",('STEPLOOP', 'doWhileConditionFalse', 400, 'We execute the step and then execute it again and again as long as condition is false.', '', '', '', '')");
        b.append(",('STEPLOOP', 'whileConditionTrueDo', 500, 'We execute the step as long the condition is true.', '', '', '', '')");
        b.append(",('STEPLOOP', 'whileConditionFalseDo', 600, 'We execute the step as long the condition is false.', '', '', '', '')");
        b.append(",('INVARIANTPRIVATE', 'STEPLOOP', '590', '', '', '', '', '');");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `testcasestep` SET `Loop` = 'onceIfConditionTrue' WHERE `Loop` = '';");
        a.add(b.toString());

        //Adding Screensize to robot table
        // 1029
        b = new StringBuilder();
        b.append("ALTER TABLE `robot` ");
        b.append("ADD COLUMN `screensize` VARCHAR(250) NOT NULL DEFAULT '' AFTER `useragent`;");
        a.add(b.toString());

        //Adding Screensize to robot table
        // 1030
        b = new StringBuilder();
        b.append("select 1 from DUAL;");
        a.add(b.toString());

        // Parameter in order to limit the number of loop operation allowed in loop operation
        // 1031
        b = new StringBuilder();
        b.append("INSERT INTO `parameter` VALUES ('','cerberus_loopstep_max','20','Integer value that correspond to the max number of step loop authorised.<br>This parameter can be configured at the system level.')");
        a.add(b.toString());

        // Add poolSize attribute to CountryEnvironmentParameters
        // 1032-1034
        b = new StringBuilder();
        b.append("select 1 from DUAL;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("select 1 from DUAL;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `countryenvironmentparameters` ");
        b.append("ADD COLUMN `poolSize` INT NULL AFTER `Var4`;");
        a.add(b.toString());

        // Parameter in order to limit the frequency of the websocket push
        // 1035
        b = new StringBuilder();
        b.append("INSERT INTO `parameter` VALUES ('','cerberus_featureflipping_websocketpushperiod','5000','Integer value that correspond to the nb of ms between every websocket push.')");
        a.add(b.toString());

        // Add the State column to the TestCaseExecutionQueue table and fill it with default value
        // 1036-1038
        b = new StringBuilder();
        b.append("ALTER TABLE `testcaseexecutionqueue` ");
        b.append("ADD COLUMN `State` VARCHAR(9) NOT NULL DEFAULT 'WAITING' AFTER `manualexecution`;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `testcaseexecutionqueue` SET `State` = 'WAITING' WHERE `Proceeded` = 0;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `testcaseexecutionqueue` SET `State` = 'EXECUTING' WHERE `Proceeded` = 1;");
        a.add(b.toString());

        // Adding ConditionInit columns in all tables.
        // 1039-1042
        b = new StringBuilder();
        b.append("ALTER TABLE `testcaseexecution` ");
        b.append("ADD COLUMN `EnvironmentData` VARCHAR(45) NULL DEFAULT ''  AFTER `Environment`,");
        b.append("ADD COLUMN `ConditionOper` VARCHAR(45) NULL DEFAULT ''  AFTER `screensize`,");
        b.append("ADD COLUMN `ConditionVal1Init` TEXT NULL AFTER `ConditionOper`,");
        b.append("ADD COLUMN `ConditionVal2Init` TEXT NULL AFTER `ConditionVal1Init`,");
        b.append("ADD COLUMN `ConditionVal1` TEXT NULL AFTER `ConditionVal2Init`,");
        b.append("ADD COLUMN `ConditionVal2` TEXT NULL AFTER `ConditionVal1`;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepexecution` ");
        b.append("ADD COLUMN `ConditionVal1Init` TEXT NULL AFTER `ConditionOper`,");
        b.append("ADD COLUMN `ConditionVal2Init` TEXT NULL AFTER `ConditionVal1Init`;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepactionexecution` ");
        b.append("ADD COLUMN `ConditionVal1Init` TEXT NULL AFTER `ConditionOper`,");
        b.append("ADD COLUMN `ConditionVal2Init` TEXT NULL AFTER `ConditionVal1Init`;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepactioncontrolexecution` ");
        b.append("ADD COLUMN `ConditionVal1Init` TEXT NULL AFTER `ConditionOper`,");
        b.append("ADD COLUMN `ConditionVal2Init` TEXT NULL AFTER `ConditionVal1Init`;");
        a.add(b.toString());

        // Add the new State column header on the Execution pending page
        // 1043
        b = new StringBuilder();
        b.append("select 1 from DUAL;");
        a.add(b.toString());

        // Add the new ManualExecution column header on the Execution pending page
        // 1044
        b = new StringBuilder();
        b.append("ALTER TABLE `testcaseexecution`");
        b.append("ADD COLUMN `ManualExecution` VARCHAR(1) NULL DEFAULT '' AFTER `ConditionVal2`;");
        a.add(b.toString());

        // Document the new 'edittcstep' field from ExecutionDetail page
        // 1045
        b = new StringBuilder();
        b.append("select 1 from DUAL;");
        a.add(b.toString());

        // Parameter in order to tune the level of the property definition
        // 1046
        b = new StringBuilder();
        b.append("INSERT INTO `parameter` VALUES ('','cerberus_property_countrylevelheritage','N','Boolean that activate the heritage of the property calculation at the country level. if N, a property will be considered as not available on country XXX when it does not exist for XXX and exist for any other country but XXX at testcase level (even if it has been defined at usestep or pretest level for that country XXX). If Y, it will be considered as defined for country XXX as long as it has been defined for that country at testcase, usestep or pretest level.')");
        a.add(b.toString());

        // Document the title field from TestCaseExecution page
        // 1047
        b = new StringBuilder();
        b.append("select 1 from DUAL;");
        a.add(b.toString());

        // Add documentation for Execution Detail page
        // 1048
        b = new StringBuilder();
        b.append("select 1 from DUAL;");
        a.add(b.toString());

        // Increase the testcaseexecutionqueue's comment column size
        // 1049
        b = new StringBuilder();
        b.append("ALTER TABLE `testcaseexecutionqueue` ");
        b.append("CHANGE COLUMN `comment` `comment` TEXT NULL DEFAULT NULL ;");
        a.add(b.toString());

        // Add the Comment column header documentation
        // 1050
        b = new StringBuilder();
        b.append("select 1 from DUAL;");
        a.add(b.toString());

        // Add the loop column in step execution table
        // 1051
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepexecution`  ");
        b.append("ADD COLUMN `loop` VARCHAR(45) NULL DEFAULT '' AFTER `Sort`;");
        a.add(b.toString());

        // Add missing columns to ExecutionPending page
        // 1052
        b = new StringBuilder();
        b.append("select 1 from DUAL;");
        a.add(b.toString());

        // Add documentation for button remove on invariant
        // 1053
        b = new StringBuilder();
        b.append("select 1 from DUAL;");
        a.add(b.toString());

        // Change and add database structure in order to support testing of services (no longuer specific to soap library)
        // 1054-1063
        b = new StringBuilder();
        b.append("ALTER TABLE `soaplibrary` ");
        b.append("CHANGE COLUMN `Name` `Service` VARCHAR(255) NOT NULL DEFAULT ''  ,");
        b.append("CHANGE COLUMN `Type` `Group` VARCHAR(45) NULL DEFAULT ''  ,");
        b.append("CHANGE COLUMN `Method` `Operation` VARCHAR(255) NULL DEFAULT ''  ,");
        b.append("CHANGE COLUMN `Envelope` `ServiceRequest` MEDIUMTEXT NULL DEFAULT NULL  , RENAME TO  `appservice` ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `appservice` ");
        b.append("CHANGE COLUMN `Group` `Group` VARCHAR(45) NULL DEFAULT '' AFTER `ParsingAnswer`,");
        b.append("ADD COLUMN `Application` VARCHAR(200) NULL DEFAULT '' AFTER `Service`,");
        b.append("ADD COLUMN `Type` VARCHAR(45) NOT NULL DEFAULT ''  AFTER `Application`,");
        b.append("ADD COLUMN `Method` VARCHAR(45) NOT NULL DEFAULT '' AFTER `Type`;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` VALUES ");
        b.append("('SRVTYPE', 'SOAP', 100, 'SOAP Service.', '', '', '', '')");
        b.append(",('SRVTYPE', 'REST', 200, 'REST Service.', '', '', '', '')");
        b.append(",('SRVMETHOD', 'GET', 100, 'GET http method.', '', '', '', '')");
        b.append(",('SRVMETHOD', 'POST', 200, 'POST http method.', '', '', '', '')");
        b.append(",('INVARIANTPRIVATE', 'SRVTYPE', '600', '', '', '', '', '')");
        b.append(",('INVARIANTPRIVATE', 'SRVMETHOD', '610', '', '', '', '', '');");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `appservice` SET `Type`='SOAP', `application` = null;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `appservice` ADD INDEX `FK_appservice_01` (`Application` ASC) ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `appservice` ");
        b.append("ADD CONSTRAINT `FK_appservice_01` FOREIGN KEY (`Application`) REFERENCES `application` (`Application`) ON DELETE CASCADE ON UPDATE CASCADE;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `appservice` ");
        b.append("ADD COLUMN `UsrCreated` VARCHAR(45) NOT NULL DEFAULT '' AFTER `Description`,");
        b.append("ADD COLUMN `DateCreated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER `UsrCreated`,");
        b.append("ADD COLUMN `UsrModif` VARCHAR(45) NULL DEFAULT '' AFTER `DateCreated`,");
        b.append("ADD COLUMN `DateModif` TIMESTAMP NOT NULL DEFAULT '1970-01-01 01:01:01' ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("update testcasestepaction SET `action` = 'callService' where `action` in ('callSoap','callSoapWithBase');");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` VALUES ");
        b.append("('ACTION', 'callService', 17000, 'Call Service.', '', '', '', '');");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("DELETE FROM `invariant` WHERE `idname` = 'ACTION' and `value` in ('callSoap','callSoapWithBase');");
        a.add(b.toString());

        // New updated Documentation.
        // 1064-1065
        b = new StringBuilder();
        b.append("select 1 from DUAL;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("select 1 from DUAL;");
        a.add(b.toString());

        // Update DateCreated from appservice to remove Zero Date
        // 1066
        b = new StringBuilder();
        b.append("UPDATE appservice SET DateCreated = '1970-01-01 01:01:01' WHERE DateCreated = '0000-00-00 00:00:00'");
        a.add(b.toString());

        // Added content and header Service tables.
        // 1067-1070
        b = new StringBuilder();
        b.append("CREATE TABLE `appservicecontent` (");
        b.append("  `Service` VARCHAR(255) NOT NULL ,");
        b.append("  `Key` VARCHAR(255) NOT NULL ,");
        b.append("  `Value` TEXT NULL ,");
        b.append("  `Sort` INT NULL DEFAULT 0 ,");
        b.append("  `Active` VARCHAR(45) NULL DEFAULT 'Y' ,");
        b.append("  `UsrCreated` VARCHAR(45) NOT NULL DEFAULT '' ,");
        b.append("  `DateCreated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ,");
        b.append("  `UsrModif` VARCHAR(45) NULL DEFAULT '' ,");
        b.append("  `DateModif` TIMESTAMP NOT NULL DEFAULT '1970-01-01 01:01:01' ,");
        b.append("  PRIMARY KEY (`Service`, `Key`), ");
        b.append("   CONSTRAINT `FK_appservicecontent_01` FOREIGN KEY (`Service`) REFERENCES `appservice` (`Service`) ON DELETE CASCADE ON UPDATE CASCADE)");
        b.append("  ENGINE=InnoDB DEFAULT CHARSET=utf8; ");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("CREATE TABLE `appserviceheader` (");
        b.append("  `Service` VARCHAR(255) NOT NULL ,");
        b.append("  `Key` VARCHAR(255) NOT NULL ,");
        b.append("  `Value` TEXT NULL ,");
        b.append("  `Sort` INT NULL DEFAULT 0 ,");
        b.append("  `Active` VARCHAR(45) NULL DEFAULT 'Y' ,");
        b.append("  `UsrCreated` VARCHAR(45) NOT NULL DEFAULT '' ,");
        b.append("  `DateCreated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ,");
        b.append("  `UsrModif` VARCHAR(45) NULL DEFAULT '' ,");
        b.append("  `DateModif` TIMESTAMP NOT NULL DEFAULT '1970-01-01 01:01:01' ,");
        b.append("  PRIMARY KEY (`Service`, `Key`), ");
        b.append("  CONSTRAINT `FK_appserviceheader_01` FOREIGN KEY (`Service`) REFERENCES `appservice` (`Service`) ON DELETE CASCADE ON UPDATE CASCADE)");
        b.append("  ENGINE=InnoDB DEFAULT CHARSET=utf8; ");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `appserviceheader` ");
        b.append("  ADD COLUMN `Description` VARCHAR(255) NOT NULL DEFAULT '' AFTER `Active`;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `appservicecontent` ");
        b.append("  ADD COLUMN `Description` VARCHAR(255) NOT NULL DEFAULT '' AFTER `Active`;");
        a.add(b.toString());

        // Added tracability on application table.
        // 1071
        b = new StringBuilder();
        b.append("ALTER TABLE `application` ");
        b.append("ADD COLUMN `UsrCreated` VARCHAR(45) NOT NULL DEFAULT '' AFTER `mavengroupid`,");
        b.append("ADD COLUMN `DateCreated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER `UsrCreated`,");
        b.append("ADD COLUMN `UsrModif` VARCHAR(45) NULL DEFAULT '' AFTER `DateCreated`,");
        b.append("ADD COLUMN `DateModif` TIMESTAMP NOT NULL DEFAULT '1970-01-01 01:01:01' ;");
        a.add(b.toString());

        // 2 new Parameters used for CallService Action.
        // 1072
        b = new StringBuilder();
        b.append("INSERT INTO `parameter` VALUES ");
        b.append(" ('','cerberus_callservice_enablehttpheadertoken','Y','Boolean that activate the addition of a header entry cerberus_token with execution id value on every serice call.'), ");
        b.append(" ('','cerberus_callservice_timeoutms','60000','timeout in ms second used for any service call.')");
        a.add(b.toString());

        // Remove WS type of application and convert it to SRV + Reorder Application Type in menu.
        // 1073-1078
        b = new StringBuilder();
        b.append("UPDATE application SET `Type` = 'SRV' WHERE `Type` = 'WS'; ");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("DELETE FROM `invariant` WHERE `idname`='APPLITYPE' and`value`='WS'; ");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET `sort`='900' WHERE `idname`='APPLITYPE' and`value`='NONE';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET `sort`='800' WHERE `idname`='APPLITYPE' and`value`='BAT';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET `description`='Web GUI application' WHERE `idname`='APPLITYPE' and`value`='GUI';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET `description`='Service Application (REST or SOAP)' WHERE `idname`='APPLITYPE' and`value`='SRV';");
        a.add(b.toString());

        // Documentation on executions in queue page.
        // 1079-1082
        b = new StringBuilder();
        b.append("select 1 from DUAL;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("select 1 from DUAL;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("select 1 from DUAL;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("select 1 from DUAL;");
        a.add(b.toString());

        // Add the mass action on the execution pending page
        // 1083-1084
        b = new StringBuilder();
        b.append("select 1 from DUAL;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("select 1 from DUAL;");
        a.add(b.toString());

        // Add missing documentation on execution pending table
        // 1085
        b = new StringBuilder();
        b.append("select 1 from DUAL;");
        a.add(b.toString());

        // Remove the unecessary TestCaseExecutionQueue's Proceeded column
        // 1086
        b = new StringBuilder();
        b.append("ALTER TABLE `testcaseexecutionqueue` DROP COLUMN `Proceeded`;");
        a.add(b.toString());

        // Invert Value1 and Value2 from 'getFromJSON' and 'getFromXML' Properties.
        // 1087-1091
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasecountryproperties` ADD COLUMN `valueTemp` TEXT NULL AFTER `last_modified`;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE testcasecountryproperties  SET valueTemp=Value2 where `Type` in ('getFromJSON', 'getFromXML');");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE testcasecountryproperties  SET Value2=Value1 where `Type` in ('getFromJSON', 'getFromXML');");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE testcasecountryproperties  SET Value1=valueTemp where `Type` in ('getFromJSON', 'getFromXML');");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasecountryproperties` DROP COLUMN `valueTemp` ;");
        a.add(b.toString());

        // Added invariant for Content and Header service.
        // 1092
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` VALUES ");
        b.append("('APPSERVICECONTENTACT', 'Y', 100, 'Yes', '', '', '', '')");
        b.append(",('APPSERVICECONTENTACT', 'N', 200, 'No', '', '', '', '')");
        b.append(",('APPSERVICEHEADERACT', 'Y', 100, 'Yes', '', '', '', '')");
        b.append(",('APPSERVICEHEADERACT', 'N', 200, 'No', '', '', '', '')");
        b.append(",('INVARIANTPRIVATE', 'APPSERVICECONTENTACT', '620', '', '', '', '', '')");
        b.append(",('INVARIANTPRIVATE', 'APPSERVICEHEADERACT', '630', '', '', '', '', '');");
        a.add(b.toString());

        // Changed control from Integer to Numeric..
        // 1093-1102
        b = new StringBuilder();
        b.append("UPDATE testcasestepactioncontrol SET Control = 'verifyNumericEquals' where Control in ('verifyIntegerEquals');");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE testcasestepactioncontrol SET Control = 'verifyNumericDifferent' where Control in ('verifyIntegerDifferent');");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE testcasestepactioncontrol SET Control = 'verifyNumericGreater' where Control in ('verifyIntegerGreater');");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE testcasestepactioncontrol SET Control = 'verifyNumericMinor' where Control in ('verifyIntegerMinor');");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET `value`='verifyNumericEquals', `description`='verifyNumericEquals' WHERE `idname`='CONTROL' and`value`='verifyIntegerEquals';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET `value`='verifyNumericDifferent', `description`='verifyNumericDifferent' WHERE `idname`='CONTROL' and`value`='verifyIntegerDifferent';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET `value`='verifyNumericGreater', `description`='verifyNumericGreater' WHERE `idname`='CONTROL' and`value`='verifyIntegerGreater';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET `value`='verifyNumericMinor', `description`='verifyNumericMinor' WHERE `idname`='CONTROL' and`value`='verifyIntegerMinor';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`) VALUES ('CONTROL', 'verifyNumericGreaterOrEqual', '1610', 'verifyNumericGreaterOrEqual');");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`) VALUES ('CONTROL', 'verifyNumericMinorOrEqual', '1710', 'verifyNumericMinorOrEqual');");
        a.add(b.toString());

        // Adding Service Columns to testdatalib table.
        // 1103-1105
        b = new StringBuilder();
        b.append("ALTER TABLE `testdatalib` ADD COLUMN `Service` VARCHAR(255) NULL DEFAULT null AFTER `DatabaseUrl`;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testdatalib` ADD INDEX `IX_testdatalib_02` (`Service` ASC);");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testdatalib` ADD CONSTRAINT `FK_testdatalib_01` FOREIGN KEY (`Service`) REFERENCES `appservice` (`Service`) ON DELETE CASCADE ON UPDATE CASCADE;");
        a.add(b.toString());

        // Adding new Element present Condition.
        // 1106
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` VALUES ");
        b.append("('ACTIONCONDITIONOPER', 'ifElementPresent', 250, 'Only execute if Element is present.', '', '', '', '')");
        b.append(",('STEPCONDITIONOPER', 'ifElementPresent', 250, 'Only execute if Element is present.', '', '', '', '')");
        b.append(",('CONTROLCONDITIONOPER', 'ifElementPresent', 250, 'Only execute if Element is present.', '', '', '', '')");
        a.add(b.toString());

        // Parameter in order to tune the timeout on click action
        // 1107
        b = new StringBuilder();
        b.append("INSERT INTO `parameter` VALUES ('','cerberus_selenium_action_click_timeout','90000','timeout in ms second used during selenium click action.')");
        a.add(b.toString());

        // Parameter in order to tune the number of tag displayed inside the homepage.
        // 1108
        b = new StringBuilder();
        b.append("INSERT INTO `parameter` VALUES ('','cerberus_homepage_nbdisplayedtag','5','Number of tag summary displayed inside homepage.')");
        a.add(b.toString());

        // Modification of the size of the Port.
        // 1109 - 1111
        b = new StringBuilder();
        b.append("ALTER TABLE `testcaseexecution` ");
        b.append("CHANGE COLUMN `Port` `Port` VARCHAR(150) NULL DEFAULT NULL ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `robot` ");
        b.append("CHANGE COLUMN `Port` `Port` VARCHAR(150) NULL DEFAULT NULL ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcaseexecutionqueue` ");
        b.append("CHANGE COLUMN `RobotPort` `RobotPort` VARCHAR(150) NULL DEFAULT NULL ;");
        a.add(b.toString());

        // Insert invariants CAPABILITIES if not already defined.
        // 1112 - 1115
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`) SELECT 'CAPABILITY', 'acceptInsecureCerts', 10, 'Whether the session should accept insecure SSL certs by default.' FROM DUAL");
        b.append(" WHERE NOT EXISTS (SELECT * FROM `invariant` where `idname`='CAPABILITY' AND `value` = 'acceptInsecureCerts');");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`,`description`) SELECT 'CAPABILITY', 'acceptSslCerts', 10,'Whether the session should accept all SSL certs by default.' FROM DUAL");
        b.append(" WHERE NOT EXISTS (SELECT * FROM `invariant` where `idname`='CAPABILITY' AND `value` = 'acceptSslCerts');");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`,`description`) SELECT 'CAPABILITY', 'appActivity', 10,'Activity name for the Android activity you want to launch from your package. This often needs to be preceded by a . (e.g., .MainActivity instead of MainActivity).' FROM DUAL");
        b.append(" WHERE NOT EXISTS (SELECT * FROM `invariant` where `idname`='CAPABILITY' AND `value` = 'appActivity');");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`,`description`) SELECT 'CAPABILITY', 'appWaitActivity', 10,'Activity name for the Android activity you want to wait for.' FROM DUAL");
        b.append(" WHERE NOT EXISTS (SELECT * FROM `invariant` where `idname`='CAPABILITY' AND `value` = 'appWaitActivity');");
        a.add(b.toString());

        //Add userAgent in TestCaseExecution Table and documentation Table
        // 1116 - 1117
        b = new StringBuilder();
        b.append("ALTER TABLE `testcaseexecution` ");
        b.append("ADD COLUMN `UserAgent` VARCHAR(250) NULL DEFAULT NULL AFTER `screensize`;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("select 1 from DUAL;");
        a.add(b.toString());

        //Add OutputFormat verbose-json
        // 1118
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) ");
        b.append("VALUES ('OUTPUTFORMAT', 'verbose-json', '5', 'Verbose json format', '');");
        a.add(b.toString());

        // Let testcaseexecutionqueue's Browser column be null
        // 1119
        b = new StringBuilder();
        b.append("ALTER TABLE `testcaseexecutionqueue` ");
        b.append("CHANGE COLUMN `Browser` `Browser` VARCHAR(45) NULL DEFAULT NULL ;");
        a.add(b.toString());

        // Parameter smtp username and password
        // 1120
        b = new StringBuilder();
        b.append("INSERT INTO `parameter` ");
        b.append("VALUES ('','integration_smtp_username','','Username to be used in case of SMTP with Authentication. Empty if no authentication required.')");
        b.append(",('','integration_smtp_password','','Password to be used in case of SMTP with Authentication. Empty if no authentication required.')");
        a.add(b.toString());

        //Add gp4 to gp9 group in invariant table.
        // 1121
        b = new StringBuilder();
        b.append("ALTER TABLE `invariant` ");
        b.append(" ADD COLUMN `gp4` VARCHAR(45) NULL DEFAULT NULL AFTER `gp3`, ");
        b.append(" ADD COLUMN `gp5` VARCHAR(45) NULL DEFAULT NULL AFTER `gp4`, ");
        b.append(" ADD COLUMN `gp6` VARCHAR(45) NULL DEFAULT NULL AFTER `gp5`, ");
        b.append(" ADD COLUMN `gp7` VARCHAR(45) NULL DEFAULT NULL AFTER `gp6`, ");
        b.append(" ADD COLUMN `gp8` VARCHAR(45) NULL DEFAULT NULL AFTER `gp7`, ");
        b.append(" ADD COLUMN `gp9` VARCHAR(45) NULL DEFAULT NULL AFTER `gp8` ;");
        a.add(b.toString());

        // Parameter proxy for callService in rest
        // 1122
        b = new StringBuilder();
        b.append("INSERT INTO `parameter` ");
        b.append("VALUES ('','cerberus_callservicerest_proxyactive','N','Y if you want to activate proxy for REST CallService.')");
        b.append(",('','cerberus_callservicerest_proxyhost','proxy','Hostname of the proxy that will be used for REST CallService.')");
        b.append(",('','cerberus_callservicerest_proxyport','80','Port Number of the proxy that will be used for REST CallService.')");
        b.append(",('','cerberus_callservicerest_proxyauthentificationactive','N','Y if you want to activate proxy authentification for REST CallService.')");
        b.append(",('','cerberus_callservicerest_proxyuser','user','Username to be used in case of REST Call Service with Authentication.')");
        b.append(",('','cerberus_callservicerest_proxypassword','password','Password to be used in case of REST Call Service with Authentication.')");
        a.add(b.toString());

        // Rename TestDataLib Type from SOAP to SERVICE.
        // 1123-1124
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET `value`='SERVICE', `description`='Dynamic test data from SERVICE Webservice call.' WHERE `idname`='TESTDATATYPE' and`value`='SOAP'; ");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `testdatalib` SET `Type`='SERVICE' WHERE `Type`='SOAP'; ");
        a.add(b.toString());

        // New updated Documentation.
        // 1125-1126
        b = new StringBuilder();
        b.append("select 1 from DUAL;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("select 1 from DUAL;");
        a.add(b.toString());

        // New updated Documentation.
        // 1127-1128
        b = new StringBuilder();
        b.append("select 1 from DUAL;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("select 1 from DUAL;");
        a.add(b.toString());

        //Add testcase description to execution table.
        // 1129
        b = new StringBuilder();
        b.append("ALTER TABLE `testcaseexecution` ");
        b.append(" ADD COLUMN `Description` VARCHAR(500) NULL DEFAULT NULL AFTER `testcase` ");
        a.add(b.toString());

        //Add campaign content based on label.
        // 1130
        b = new StringBuilder();
        b.append("CREATE TABLE `campaignlabel` (");
        b.append("  `campaignlabelID` int(10) unsigned NOT NULL AUTO_INCREMENT,");
        b.append("  `campaign` varchar(45) NOT NULL,");
        b.append("  `labelId` INT(11) ,");
        b.append("  PRIMARY KEY (`campaignlabelID`),");
        b.append("  UNIQUE KEY `IX_campaignlabel_01` (`campaign`, `labelId`),");
        b.append("  KEY `IX_campaignlabel_02` (`campaign`),");
        b.append("  CONSTRAINT `FK_campaignlabel_01` FOREIGN KEY (`campaign`) REFERENCES `campaign` (`campaign`) ON DELETE CASCADE ON UPDATE CASCADE,");
        b.append("  CONSTRAINT `FK_campaignlabel_02` FOREIGN KEY (`labelId`) REFERENCES `label` (`Id`) ON DELETE CASCADE ON UPDATE CASCADE");
        b.append(") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;");
        a.add(b.toString());

        // New updated Documentation.
        // 1131-1132
        b = new StringBuilder();
        b.append("select 1 from DUAL;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("select 1 from DUAL;");
        a.add(b.toString());

        // Rename proxy parameters.
        // 1133-1144
        b = new StringBuilder();
        b.append("UPDATE `parameter` SET `description`='Y if you want to activate proxy.' WHERE `system`='' and`param`='cerberus_callservicerest_proxyactive';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `parameter` SET `description`='Y if you want to activate proxy authentification.' WHERE `system`='' and`param`='cerberus_callservicerest_proxyauthentificationactive';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `parameter` SET `description`='Hostname of the proxy.' WHERE `system`='' and`param`='cerberus_callservicerest_proxyhost';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `parameter` SET `description`='Password to be used in case of proxy with Authentication.' WHERE `system`='' and`param`='cerberus_callservicerest_proxypassword';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `parameter` SET `description`='Port Number of the proxy.' WHERE `system`='' and`param`='cerberus_callservicerest_proxyport';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `parameter` SET `description`='Username to be used in case of proxy with Authentication.' WHERE `system`='' and`param`='cerberus_callservicerest_proxyuser';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `parameter` SET `param`='cerberus_proxy_active' WHERE `param`='cerberus_callservicerest_proxyactive';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `parameter` SET `param`='cerberus_proxyauthentification_active' WHERE `param`='cerberus_callservicerest_proxyauthentificationactive';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `parameter` SET `param`='cerberus_proxy_host' WHERE `param`='cerberus_callservicerest_proxyhost';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `parameter` SET `param`='cerberus_proxyauthentification_password' WHERE `param`='cerberus_callservicerest_proxypassword';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `parameter` SET `param`='cerberus_proxy_port' WHERE `param`='cerberus_callservicerest_proxyport';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `parameter` SET `param`='cerberus_proxyauthentification_user' WHERE `param`='cerberus_callservicerest_proxyuser';");
        a.add(b.toString());

        // Add tracability fields in campaignlabel table.
        // 1145
        b = new StringBuilder();
        b.append("ALTER TABLE `campaignlabel` ");
        b.append("ADD COLUMN `UsrCreated` VARCHAR(45) NOT NULL DEFAULT '' AFTER `LabelId`,");
        b.append("ADD COLUMN `DateCreated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER `UsrCreated`,");
        b.append("ADD COLUMN `UsrModif` VARCHAR(45) NULL DEFAULT '' AFTER `DateCreated`,");
        b.append("ADD COLUMN `DateModif` TIMESTAMP NOT NULL DEFAULT '1970-01-01 01:01:01' ;");
        a.add(b.toString());

        // Removed log table.
        // 1146
        b = new StringBuilder();
        b.append("DROP TABLE `log`; ");
        a.add(b.toString());

        // Parameter proxy for callService in rest
        // 1147
        b = new StringBuilder();
        b.append("INSERT INTO `parameter` ");
        b.append("VALUES ('','cerberus_proxy_nonproxyhosts','localhost,127.0.0.1,192.168.1.*','The list of hosts that should be reached directly, bypassing the proxy. This is a list of patterns separated by \\',\\'. The patterns may start or end with a \\'*\\' for wildcards. Any host matching one of these patterns will be reached through a direct connection instead of through a proxy.');");
        a.add(b.toString());

        // Removed testdata table.
        // 1148-1150
        b = new StringBuilder();
        b.append("DROP TABLE `testdata`; ");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `testcasecountryproperties` SET `Type`='Unknown' where `Type` = 'getFromTestData';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("DELETE FROM `invariant` WHERE `idname`='PROPERTYTYPE' and`value`='getFromTestData';");
        a.add(b.toString());

        // Adding SOAP attachement in appservice.
        // 1151
        b = new StringBuilder();
        b.append("ALTER TABLE `appservice` ");
        b.append("DROP COLUMN `ParsingAnswer`, ");
        b.append("ADD COLUMN `AttachementURL` VARCHAR(255) NULL DEFAULT '' AFTER `Operation`;");
        a.add(b.toString());

        // Adding ScreenSize column in testcase table.
        // 1152
        b = new StringBuilder();
        b.append("ALTER TABLE `testcase` ADD COLUMN `screensize` VARCHAR(250) NOT NULL DEFAULT '' AFTER `useragent`; ");
        a.add(b.toString());

        // Cleaning type properties.
        // 1153
        b = new StringBuilder();
        b.append("UPDATE testcasecountryproperties SET `type` = 'text' where type='Unknown'; ");
        a.add(b.toString());

        // Added USERAGENT invariant.
        // 1154
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` VALUES ");
        b.append("('USERAGENT', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36', 100, 'Chrome Generic Win10', '', '', '', '', '', '', '', '', '', '')");
        b.append(",('USERAGENT', 'Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36', 100, 'Chrome Generic Win7', '', '', '', '', '', '', '', '', '', '')");
        b.append(",('USERAGENT', 'Mozilla/5.0 (Linux; Android 6.0.1; SM-G920V Build/MMB29K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.98 Mobile Safari/537.36', 100, 'Samsung Galaxy S6', '', '', '', '', '', '', '', '', '', '')");
        b.append(",('USERAGENT', 'Mozilla/5.0 (Linux; Android 6.0.1; Nexus 6P Build/MMB29P) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.83 Mobile Safari/537.36', 100, 'Nexus 6P', '', '', '', '', '', '', '', '', '', '')");
        b.append(",('USERAGENT', 'Mozilla/5.0 (Linux; Android 5.0.2; SAMSUNG SM-T550 Build/LRX22G) AppleWebKit/537.36 (KHTML, like Gecko) SamsungBrowser/3.3 Chrome/38.0.2125.102 Safari/537.36', 100, 'Samsung Galaxy Tab A', '', '', '', '', '', '', '', '', '', '')");
        b.append(",('USERAGENT', 'Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)', 100, 'Google bot', '', '', '', '', '', '', '', '', '', '')");
        b.append(",('USERAGENT', 'Mozilla/5.0 (compatible; bingbot/2.0; +http://www.bing.com/bingbot.htm)', 100, 'Bing bot', '', '', '', '', '', '', '', '', '', '')");
        b.append(",('USERAGENT', 'Mozilla/5.0 (iPhone; CPU iPhone OS 5_0 like Mac OS X) AppleWebKit/534.46 (KHTML, like Gecko) Version/5.1 Mobile/9A334 Safari/7534.48.3', 100, 'iPhone', '', '', '', '', '', '', '', '', '', '')");
        b.append(",('USERAGENT', 'Mozilla/5.0 (iPad; CPU OS 5_0 like Mac OS X) AppleWebKit/534.46 (KHTML, like Gecko) Version/5.1 Mobile/9A334 Safari/7534.48.3', 100, 'iPad', '', '', '', '', '', '', '', '', '', '')");
        b.append(",('INVARIANTPUBLIC', 'USERAGENT', '600', '', '', '', '', '', '', '', '', '', '', '');");
        a.add(b.toString());

        // Delete no longuer used parameter cerberus_homepage_nbbuildhistorydetail.
        // 1155
        b = new StringBuilder();
        b.append("DELETE from parameter where param='cerberus_homepage_nbbuildhistorydetail';");
        a.add(b.toString());

        // Added Label User Group (copied from from Test Group) allowing to remove access to update, delete and create Label at user level.
        // 1156-1157
        b = new StringBuilder();
        b.append("INSERT INTO usergroup Select Login, 'Label' from usergroup where GroupName = 'Test';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) VALUES ('USERGROUP', 'Label', '160', 'Can Create, update and delete Labels.', '');");
        a.add(b.toString());

        // Added Label User Group (copied from from Test Group) allowing to remove access to update, delete and create Label at user level.
        // 1158-1159
        b = new StringBuilder();
        b.append("INSERT INTO usergroup Select Login, 'TestStepLibrary' from usergroup where GroupName = 'Test';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) VALUES ('USERGROUP', 'TestStepLibrary', '115', 'Can modify Step Library and flag Step as Library.', '');");
        a.add(b.toString());

        // New design Login page
        // 1160
        b = new StringBuilder(); // replace color yellow by no color 
        b.append("UPDATE `parameter` SET value=replace(value,'style=\"color: yellow\"','') where param='cerberus_support_email';");
        a.add(b.toString());

        // Added ScreenSize on Execution Queue table.
        // 1161
        b = new StringBuilder();
        b.append("ALTER TABLE `testcaseexecutionqueue` ");
        b.append("ADD COLUMN `ScreenSize` VARCHAR(45) NULL DEFAULT NULL AFTER `Platform`;");
        a.add(b.toString());

        // Delete no longuer used parameter cerberus_executiondetail_use.
        // 1162
        b = new StringBuilder();
        b.append("DELETE from parameter where param='cerberus_executiondetail_use';");
        a.add(b.toString());

        // Cleaned Test table.
        // 1163
        b = new StringBuilder();
        b.append("UPDATE test SET TDateCrea = '1970-01-01 01:01:01' where TDateCrea = '0000-00-00 00:00:00';");
        a.add(b.toString());

        // Cleaned (reorder) CONTROL INVARIANT table.
        // 1164-1167
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET sort = sort*10 where idname='CONTROL' and sort < 1500 and sort > 10;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE invariant SET sort = sort*10 where idname='ACTION' and sort <= 2500 and sort > 10;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET `sort`='4000' WHERE `idname`='ACTION' and`value`='mouseOver';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET `sort`='4100' WHERE `idname`='ACTION' and`value`='mouseOverAndWait';");
        a.add(b.toString());

        // Parameter proxy for callService in rest
        // 1168
        b = new StringBuilder();
        b.append("INSERT INTO `parameter` ");
        b.append("VALUES ('','cerberus_testcase_defaultselectedcountry','ALL','Parameter that define the default list of countries selected when creating a new testcase. \\'ALL\\' select all countries. Leave the parameter empty to select none. You can also specify a list of countries separated by \\',\\' in order to select some.');");
        a.add(b.toString());

        // Removed forein key on testcaseexecution.
        // 1169
        b = new StringBuilder();
        b.append("ALTER TABLE `testcaseexecution` DROP FOREIGN KEY `FK_testcaseexecution_02`, DROP FOREIGN KEY `FK_testcaseexecution_01`; ");
        a.add(b.toString());

        // Added link between testcaseexecution and testcaseexecutionqueue.
        // 1170-1173
        b = new StringBuilder();
        b.append("ALTER TABLE `testcaseexecution` ADD COLUMN `QueueID` BIGINT(20) NULL DEFAULT NULL AFTER `ManualExecution`;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcaseexecutionqueue` ADD COLUMN `ExeID` BIGINT(20) NULL DEFAULT NULL AFTER `State`;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcaseexecution` DROP COLUMN `Verbose`, DROP COLUMN `Finished`, ");
        b.append(" ADD COLUMN `UsrCreated` VARCHAR(45) NOT NULL DEFAULT '' AFTER `QueueID`,");
        b.append(" ADD COLUMN `DateCreated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER `UsrCreated`,");
        b.append(" ADD COLUMN `UsrModif` VARCHAR(45) NULL DEFAULT '' AFTER `DateCreated`,");
        b.append(" ADD COLUMN `DateModif` TIMESTAMP NOT NULL DEFAULT '1970-01-01 01:01:01' AFTER `UsrModif` ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcaseexecutionqueue` DROP COLUMN `OutputFormat`, DROP COLUMN `Synchroneous`, ");
        b.append(" CHANGE COLUMN `manualexecution` `manualexecution` VARCHAR(1) NOT NULL DEFAULT 'N' AFTER `SeleniumLog`, ");
        b.append(" CHANGE COLUMN `retries` `retries` TINYINT(1) NOT NULL DEFAULT '0' AFTER `manualexecution`, ");
        b.append(" CHANGE COLUMN `State` `State` VARCHAR(9) NOT NULL DEFAULT 'WAITING' AFTER `RequestDate`, ");
        b.append(" ADD COLUMN `UsrCreated` VARCHAR(45) NOT NULL DEFAULT '' AFTER `ExeID`,");
        b.append(" ADD COLUMN `DateCreated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER `UsrCreated`,");
        b.append(" ADD COLUMN `UsrModif` VARCHAR(45) NULL DEFAULT '' AFTER `DateCreated`,");
        b.append(" ADD COLUMN `DateModif` TIMESTAMP NOT NULL DEFAULT '1970-01-01 01:01:01'  AFTER `UsrModif` ;");
        a.add(b.toString());

        // New updated Documentation.
        // 1174-1175
        b = new StringBuilder();
        b.append("select 1 from DUAL;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("select 1 from DUAL;");
        a.add(b.toString());

        // Removed testcase link from testcaseexeecutionqueue.
        // 1176-1177
        b = new StringBuilder();
        b.append("ALTER TABLE `testcaseexecutionqueue` DROP FOREIGN KEY `FK_testcaseexecutionqueue_01`;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcaseexecutionqueue` ADD INDEX `IX_testcaseexecution_03` (`DateCreated` ASC);");
        a.add(b.toString());

        // New updated Documentation.
        // 1178-1179
        b = new StringBuilder();
        b.append("select 1 from DUAL;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("select 1 from DUAL;");
        a.add(b.toString());

        // Add 3 new Action into invariant
        // 1180
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) VALUES ");
        b.append("('ACTION', 'openApp', '6600', 'Open Application', ''),");
        b.append("('ACTION', 'closeApp', '6700', 'Close Application', ''),");
        b.append("('ACTION', 'waitVanish', '16500', 'Wait for an element that disapear', '')");
        a.add(b.toString());

        // Added MANUALURL invariant.
        // 1181
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` VALUES ");
        b.append("('MANUALURL', '0', 100, 'Do not activate Application URL Manual definition', '', '', '', '', '', '', '', '', '', '')");
        b.append(",('MANUALURL', '1', 200, 'Activate Application URL Manual definition', '', '', '', '', '', '', '', '', '', '')");
        b.append(",('INVARIANTPUBLIC', 'MANUALURL', '650', '', '', '', '', '', '', '', '', '', '', '');");
        a.add(b.toString());

        // Parameter timeout for call internal cerberus URL when trigering execution from the queue
        // 1182
        b = new StringBuilder();
        b.append("INSERT INTO `parameter` ");
        b.append("VALUES ('','cerberus_queueexecution_timeout','600000','Parameter that define the time cerberus will wait in ms when triggering an execution from the queue.');");
        a.add(b.toString());

        // Adding index to Execution Queue
        // 1183
        b = new StringBuilder();
        b.append("ALTER TABLE `testcaseexecutionqueue` ADD INDEX `IX_testcaseexecution_04` (`State` ASC);");
        a.add(b.toString());

        // Adding index to Execution Queue
        // 1184
        b = new StringBuilder();
        b.append("ALTER TABLE `robot` ADD COLUMN `poolsize` INT(11) NOT NULL DEFAULT 0 AFTER `description`;");
        a.add(b.toString());

        // Adding index to Execution Queue
        // 1185
        b = new StringBuilder();
        b.append("ALTER TABLE `appservice` CHANGE COLUMN `Method` `Method` VARCHAR(255) NOT NULL DEFAULT '';");
        a.add(b.toString());

        // Value in order to secure the non parallel process of the queue.
        // 1185-1188
        b = new StringBuilder();
        b.append("ALTER TABLE `myversion` ADD COLUMN `ValueString` VARCHAR(200) NULL DEFAULT NULL AFTER `Value`;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("INSERT INTO `myversion` (`Key`, `ValueString`) VALUES ('queueprocessingjobrunning', 'N');");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("INSERT INTO `myversion` (`Key`) VALUES ('queueprocessingjobstart');");
        a.add(b.toString());

        // Added ROBOTHOST invariant.
        // 1189-1190
        b = new StringBuilder();
        b.append("UPDATE `invariant` SET `idname`='INVARIANTPRIVATE' WHERE `idname`='INVARIANTPUBLIC' and`value`='MANUALURL';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` VALUES ");
        b.append("('ROBOTHOST', 'localhost', 100, 'Localhost Robot', '', '10', '', '', '', '', '', '', '', '')");
        b.append(",('INVARIANTPUBLIC', 'ROBOTHOST', '650', '', '', '', '', '', '', '', '', '', '', '');");
        a.add(b.toString());

        // Parameter for configuring default robot host constrain.
        // 1191-1192
        b = new StringBuilder();
        b.append("INSERT INTO `parameter` ");
        b.append("VALUES ('','cerberus_queueexecution_defaultrobothost_threadpoolsize','10','Default number of simultaneous execution allowed for Robot host constrain (only used when host entry does not exist in invariant table).');");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `parameter` SET `param`='cerberus_queueexecution_global_threadpoolsize' WHERE `param`='cerberus_execution_threadpool_size';");
        a.add(b.toString());

        // New updated Documentation.
        // 1193-1194
        b = new StringBuilder();
        b.append("select 1 from DUAL;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("select 1 from DUAL;");
        a.add(b.toString());

        // Added priority + Debug Flag column.
        // 1195
        b = new StringBuilder();
        b.append("ALTER TABLE `testcaseexecutionqueue`  CHANGE COLUMN `State` `State` VARCHAR(9) NOT NULL DEFAULT 'QUEUED' ,");
        b.append("ADD COLUMN `Priority` INT DEFAULT 1000 AFTER `State`, ADD COLUMN `DebugFlag` VARCHAR(1) NULL DEFAULT 'N' AFTER `comment`;");
        a.add(b.toString());

        // Removing PoolSize column on robot table.
        // 1196
        b = new StringBuilder();
        b.append("ALTER TABLE `robot` DROP COLUMN `poolsize`;");
        a.add(b.toString());

        // New updated Documentation.
        // 1197-1198
        b = new StringBuilder();
        b.append("select 1 from DUAL;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("select 1 from DUAL;");
        a.add(b.toString());

        // Added QUEUEDEBUGFLAG invariant.
        // 1199
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` VALUES ");
        b.append("('QUEUEDEBUGFLAG', 'N', 100, 'No debug message.', '', '', '', '', '', '', '', '', '', '')");
        b.append(",('QUEUEDEBUGFLAG', 'Y', 200, 'Activate debug message.', '', '', '', '', '', '', '', '', '', '')");
        b.append(",('INVARIANTPRIVATE', 'QUEUEDEBUGFLAG', '650', '', '', '', '', '', '', '', '', '', '', '');");
        a.add(b.toString());

        // Parameter for pausing job queue treatement.
        // 1200
        b = new StringBuilder();
        b.append("INSERT INTO `parameter` ");
        b.append("VALUES ('','cerberus_queueexecution_enable','Y','Activation boolean in order to activate the job that process the execution queue.Y value will activate the job. N will stop it, leaving all the executions in QUEUED State.');");
        a.add(b.toString());

        // New updated Documentation.
        // 1201-1202
        b = new StringBuilder();
        b.append("select 1 from DUAL;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("select 1 from DUAL;");
        a.add(b.toString());

        // Enlarge UserPref column.
        // 1203
        b = new StringBuilder();
        b.append("ALTER TABLE `user` CHANGE COLUMN `UserPreferences` `UserPreferences` LONGTEXT NOT NULL ;");
        a.add(b.toString());

        // Created Tag table.
        // 1204
        b = new StringBuilder();
        b.append("CREATE TABLE `tag` (");
        b.append("  `id` INT NOT NULL,");
        b.append("  `Tag` VARCHAR(50) NOT NULL DEFAULT '',");
        b.append("  `Description` VARCHAR(300) NOT NULL DEFAULT '',");
        b.append("  `Campaign` VARCHAR(45) NULL DEFAULT NULL,");
        b.append("  `UsrCreated` VARCHAR(45) NOT NULL DEFAULT '',");
        b.append("  `DateCreated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,");
        b.append("  `UsrModif` VARCHAR(45) NULL DEFAULT '',");
        b.append("  `DateModif` TIMESTAMP NOT NULL DEFAULT '1970-01-01 01:01:01',");
        b.append("  PRIMARY KEY (`id`),");
        b.append("  INDEX `IX_tag_01` (`Tag` ASC),");
        b.append("  INDEX `IX_tag_02` (`Campaign` ASC));");
        a.add(b.toString());

        // Traca fields inside invariant table.
        // 1205
        b = new StringBuilder();
        b.append("ALTER TABLE `invariant` ");
        b.append(" ADD COLUMN `UsrCreated` VARCHAR(45) NOT NULL DEFAULT '' AFTER `gp9`,");
        b.append(" ADD COLUMN `DateCreated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER `UsrCreated`,");
        b.append(" ADD COLUMN `UsrModif` VARCHAR(45) NULL DEFAULT '' AFTER `DateCreated`,");
        b.append(" ADD COLUMN `DateModif` TIMESTAMP NOT NULL DEFAULT '1970-01-01 01:01:01' AFTER `UsrModif` ;");
        a.add(b.toString());

        // New updated Documentation.
        // 1206-1207
        b = new StringBuilder();
        b.append("select 1 from DUAL;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("select 1 from DUAL;");
        a.add(b.toString());

        // Add docAnchor column in documentation.
        // 1208
        b = new StringBuilder();
        b.append("ALTER TABLE `documentation` ");
        b.append("ADD COLUMN `DocAnchor` VARCHAR(300) NULL DEFAULT NULL AFTER `DocDesc`;");
        a.add(b.toString());

        // New updated Documentation.
        // 1209-1210
        b = new StringBuilder();
        b.append("select 1 from DUAL;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("select 1 from DUAL;");
        a.add(b.toString());

        // AutoIncrement on tag table & Init tag table data.
        // 1211-1214
        b = new StringBuilder();
        b.append("ALTER TABLE `tag` CHANGE COLUMN `id` `id` INT(11) NOT NULL AUTO_INCREMENT ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `tag` DROP INDEX `IX_tag_01` , ADD UNIQUE INDEX `IX_tag_01` (`Tag` ASC);");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("INSERT INTO `tag` (`Tag`) (select distinct tag from testcaseexecution a where tag != \"\") ON DUPLICATE KEY UPDATE Tag=a.tag;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("INSERT INTO `tag` (`Tag`) (select distinct tag from testcaseexecutionqueue a where tag != \"\") ON DUPLICATE KEY UPDATE Tag=a.tag;");
        a.add(b.toString());

        // Created Tag table.
        // 1215-1221
        b = new StringBuilder();
        b.append("DROP TABLE `tag`;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("CREATE TABLE `tag` (");
        b.append("  `id` INT(11) NOT NULL AUTO_INCREMENT,");
        b.append("  `Tag` VARCHAR(50) NOT NULL DEFAULT '',");
        b.append("  `Description` VARCHAR(300) NOT NULL DEFAULT '',");
        b.append("  `Campaign` VARCHAR(45) NULL DEFAULT NULL,");
        b.append("  `UsrCreated` VARCHAR(45) NOT NULL DEFAULT '',");
        b.append("  `DateCreated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,");
        b.append("  `UsrModif` VARCHAR(45) NULL DEFAULT '',");
        b.append("  `DateModif` TIMESTAMP NOT NULL DEFAULT '1970-01-01 01:01:01',");
        b.append("  PRIMARY KEY (`id`),");
        b.append("  UNIQUE INDEX `IX_tag_01` (`Tag` ASC),");
        b.append("  INDEX `IX_tag_02` (`Campaign` ASC)");
        b.append(") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `tag` ADD CONSTRAINT `FK_tag_1`");
        b.append(" FOREIGN KEY (`Campaign`) REFERENCES `campaign` (`campaign`)  ON DELETE SET NULL  ON UPDATE CASCADE;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("INSERT INTO `tag` (`Tag`) (select distinct tag from testcaseexecutionqueue a where tag != \"\")  order by id asc ON DUPLICATE KEY UPDATE Tag=a.tag;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("INSERT INTO `tag` (`Tag`) (select distinct tag from testcaseexecution a where tag != \"\")  order by id asc ON DUPLICATE KEY UPDATE Tag=a.tag;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("INSERT INTO `tag` (`Tag`) (select distinct tag from testcaseexecutionqueue a where tag != \"\")  order by id asc ON DUPLICATE KEY UPDATE Tag=a.tag;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("INSERT INTO `tag` (`Tag`) (select distinct tag from testcaseexecution a where tag != \"\")  order by id asc ON DUPLICATE KEY UPDATE Tag=a.tag;");
        a.add(b.toString());

        // New updated Documentation.
        // 1222-1223
        b = new StringBuilder();
        b.append("select 1 from DUAL;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("select 1 from DUAL;");
        a.add(b.toString());

        // Add Français invariant.
        // 1224
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `veryshortdesc`) VALUES ('LANGUAGE', 'fr', 200, 'Francais', 'Francais');");
        a.add(b.toString());

        // Enlarge Tag column size.
        // 1225-1226
        b = new StringBuilder();
        b.append("ALTER TABLE `tag` CHANGE COLUMN `Tag` `Tag` VARCHAR(255) NOT NULL DEFAULT '' ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcaseexecution` CHANGE COLUMN `Tag` `Tag` VARCHAR(255) NULL DEFAULT NULL ;");
        a.add(b.toString());

        // Adding Distrib list to Campaign and Tag.
        // 1227-1229
        b = new StringBuilder();
        b.append("ALTER TABLE `tag` ");
        b.append("ADD COLUMN `DateEndQueue` TIMESTAMP NOT NULL DEFAULT '1970-01-01 01:01:01' AFTER `Campaign`; ");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `campaign` ");
        b.append("ADD COLUMN `DistribList` TEXT NOT NULL AFTER `campaign`, ");
        b.append("ADD COLUMN `NotifyEndTagExecution` VARCHAR(5) NOT NULL DEFAULT 'N' AFTER `DistribList`;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `campaign` ");
        b.append("ADD COLUMN `NotifyStartTagExecution` VARCHAR(5) NOT NULL DEFAULT 'N' AFTER `DistribList` ; ");
        a.add(b.toString());

        //Add parameters for the cerberus tag execution notification
        // 1230
        b = new StringBuilder();
        b.append("INSERT INTO `parameter` (`system`, `param`, `value`, `description`) VALUES ");
        b.append("('', 'cerberus_notification_tagexecutionstart_subject', '[Cerberus] Tag Execution %TAG% [%CAMPAIGN%] started.', 'Subject of Cerberus start of tag execution notification email. %TAG% and %CAMPAIGN% can be used as variables.')");
        b.append(",('', 'cerberus_notification_tagexecutionstart_body', 'Hello,<br><br>The Cerberus Tag Execution %TAG% from campaign %CAMPAIGN% has just started.<br><br>You can follow its execution <a href=\"%URLTAGREPORT%\">here</a>.','Cerberus start of tag execution notification email body. %TAG%, %URLTAGREPORT% and %CAMPAIGN% can be used as variables.')");
        b.append(",('', 'cerberus_notification_tagexecutionstart_from','Cerberus <no.reply@cerberus-testing.org>', 'From field of Cerberus start of tag execution notification email.')");
        b.append(",('', 'cerberus_notification_tagexecutionend_subject', '[Cerberus] Tag Execution %TAG% [%CAMPAIGN%] finished.', 'Subject of Cerberus end of tag execution notification email. %TAG% and %CAMPAIGN% can be used as variables.')");
        b.append(",('', 'cerberus_notification_tagexecutionend_body', 'Hello,<br><br>The Cerberus Tag Execution %TAG% from campaign %CAMPAIGN% has just finished.<br><br>You can analyse the result <a href=\"%URLTAGREPORT%\">here</a>.','Cerberus End of tag execution notification email body. %TAG%, %URLTAGREPORT% and %CAMPAIGN% can be used as variables.')");
        b.append(",('', 'cerberus_notification_tagexecutionend_from','Cerberus <no.reply@cerberus-testing.org>', 'From field of Cerberus end of tag execution notification email.')");
        a.add(b.toString());

        // New updated Documentation.
        // 1231-1232
        b = new StringBuilder();
        b.append("select 1 from DUAL;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("select 1 from DUAL;");
        a.add(b.toString());

        // Adding potencial missed key subdata. #1505
        // 1233
        b = new StringBuilder();
        b.append("INSERT INTO `testdatalibdata` (`TestDataLibID`, `SubData`, `Value`, `Column`, `ParsingAnswer`, `ColumnPosition`, `Description`) ");
        b.append(" SELECT a1.testdatalibid, '', '', '', '', '', '' FROM testdatalib a1 ");
        b.append(" LEFT OUTER JOIN ( ");
        b.append("  SELECT a.testdatalibid FROM testdatalib a JOIN testdatalibdata b ON a.testdatalibid = b.testdatalibid and b.subdata='' ");
        b.append(" ) as toto");
        b.append(" ON toto.testdatalibid = a1.testdatalibID ");
        b.append("WHERE toto.testdatalibid is null;");
        a.add(b.toString());

        // New updated Documentation.
        // 1234-1235
        b = new StringBuilder();
        b.append("select 1 from DUAL;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("select 1 from DUAL;");
        a.add(b.toString());

        // New updated Documentation.
        // 1236-1237
        b = new StringBuilder();
        b.append("select 1 from DUAL;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("select 1 from DUAL;");
        a.add(b.toString());

        // New Parameter for login message.
        // 1238-1239
        b = new StringBuilder();
        b.append("INSERT INTO `parameter` (`system`, `param`, `value`, `description`) ");
        b.append(" SELECT '', 'cerberus_loginpage_welcomemessagehtml', concat('If you don\\'t have login, please contact ' , p.`value`) , 'Message that will appear in login page. %SUPPORTEMAIL% will be replaced by parameter cerberus_support_email.'");
        b.append(" FROM parameter p");
        b.append(" WHERE param = 'cerberus_support_email' and system=''; ");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `parameter` SET `value`='', `description`='Support Email for Cerberus.' WHERE `system`='' and`param`='cerberus_support_email';");
        a.add(b.toString());

        // Clean old Parameter no longuer used.
        // 1240
        b = new StringBuilder();
        b.append("DELETE FROM parameter where param in ('cerberus_mediastorage_url','cerberus_picture_testcase_path','cerberus_reporting_url','cerberus_selenium_firefoxextension_firebug','cerberus_selenium_firefoxextension_netexport','cerberus_testcase_function_booleanListOfFunction','cerberus_testcase_function_urlForListOfFunction','cerberus_testexecutiondetailpage_nbmaxexe','cerberus_testexecutiondetailpage_nbmaxexe_max','index_alert_subject','index_alert_from','index_alert_body','index_alert_to','index_notification_body_between','index_notification_body_end','index_notification_body_top','index_notification_subject','index_smtp_from','index_smtp_host','index_smtp_port','jenkins_application_pipeline_url','jenkins_deploy_pipeline_url','selenium_chromedriver_download_url','selenium_download_url','selenium_iedriver_download_url','solr_url','sonar_application_dashboard_url','ticketing tool_bugtracking_url','ticketing tool_newbugtracking_url','ticketing tool_ticketservice_url');");
        a.add(b.toString());

        // Force data integrity on useStep.
        // 1241-1248
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestep` ");
        b.append("CHANGE COLUMN `useStepStep` `useStepStep` INT(10) NULL DEFAULT NULL ;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE testcasestep set usestepstep = null where usestepstep < 0;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestep` ");
        b.append("CHANGE COLUMN `useStepTest` `useStepTest` VARCHAR(45) NULL DEFAULT NULL ,");
        b.append("CHANGE COLUMN `useStepTestCase` `useStepTestCase` VARCHAR(45) NULL DEFAULT NULL ,");
        b.append("CHANGE COLUMN `useStepStep` `useStepStep` INT(10) UNSIGNED NULL DEFAULT NULL ,");
        b.append("ADD INDEX `IX_testcasestep_01` (`useStepTest` ASC, `useStepTestCase` ASC, `useStepStep` ASC);");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE testcasestep set usesteptest = null where usesteptest='';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE testcasestep set usesteptestcase = null where usesteptestcase='';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE testcasestep set usestepstep = null where usesteptest is null;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE testcasestep c");
        b.append(" SET UsrModif='DatabaseVersioningService', useStep='N', useStepTest=null, useStepTestCase=null, useStepStep=null, DateModif = now()");
        b.append("WHERE EXISTS ");
        b.append("( Select 1 from (");
        b.append("select a.test, a.testcase, a.step from testcasestep a");
        b.append(" left outer join testcasestep b on a.usesteptest=b.test and a.usesteptestcase=b.testcase and a.usestepstep=b.step");
        b.append(" where b.test is null and a.usesteptest is not null and a.usesteptest != ''");
        b.append(") as t where t.test=c.test and t.testcase=c.testcase and t.step=c.step and c.usesteptest is not null and c.usesteptest != '');");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestep` ");
        b.append("ADD CONSTRAINT `FK_testcasestep_02`");
        b.append("  FOREIGN KEY (`useStepTest` , `useStepTestCase`)");
        b.append("  REFERENCES `testcase` (`Test` , `TestCase` )");
        b.append("  ON DELETE SET NULL  ON UPDATE CASCADE;");
        a.add(b.toString());

        // ADD Put and Delete Http Method in invariants
        // 1249
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`) VALUES ");
        b.append("  ('SRVMETHOD', 'DELETE', 300 , 'DELETE http method')");
        b.append(" ,('SRVMETHOD', 'PUT', 400, 'PUT http method');");
        a.add(b.toString());

        // ADD Patch Http Method in invariants
        // 1250
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`) VALUES ");
        b.append("  ('SRVMETHOD', 'PATCH', 500 , 'PATCH http method')");
        a.add(b.toString());

        // ADD private invariant CAMPAIGN_TCCRITERIA
        // 1251
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`) VALUES ");
        b.append("  ('INVARIANTPRIVATE', 'CAMPAIGN_TCCRITERIA', 450 , '')");
        a.add(b.toString());

        // ADD private four invariants for all criterias
        // 1252
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`) VALUES ");
        b.append("  ('CAMPAIGN_TCCRITERIA', 'PRIORITY', 10 , '')");
        b.append("  ,('CAMPAIGN_TCCRITERIA', 'STATUS', 20 , '')");
        b.append("  ,('CAMPAIGN_TCCRITERIA', 'SYSTEM', 30 , '')");
        b.append("  ,('CAMPAIGN_TCCRITERIA', 'APPLICATION', 40 , '')");
        a.add(b.toString());

        // ADD a parameter for maximum testcase to be returned
        // 1253
        b = new StringBuilder();
        b.append("INSERT INTO `parameter` (`system`,`param`, `value`, `description`) VALUES ");
        b.append("  ('','cerberus_testcase_maxreturn', '1000', 'Integer that correspond to the maximum of testcase that cerberus can return')");
        a.add(b.toString());

        // ADD user password for robot host
        // 1254-1255
        b = new StringBuilder();
        b.append("ALTER TABLE `robot` add column host_user varchar(255)");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `robot` add column host_password varchar(255)");
        a.add(b.toString());

        // Update cerberus_testcase_maxreturn parameter.
        // 1256
        b = new StringBuilder();
        b.append("UPDATE `parameter` SET `param`='cerberus_campaign_maxtestcase', `description`='Integer that correspond to the maximum number of testcase that a Cerberus campaign can contain.' WHERE `system`='' and `param`='cerberus_testcase_maxreturn';");
        a.add(b.toString());

        //-- Enrich parameter cerberus_notification_tagexecutionend_body with extra variable.
        // 1257
        b = new StringBuilder();
        b.append("UPDATE parameter SET description='Cerberus End of tag execution notification email body. %TAG%, %URLTAGREPORT%, %CAMPAIGN%, %TAGDURATION%, %TAGSTART%, %TAGEND%, %TAGGLOBALSTATUS% and %TAGTCDETAIL% can be used as variables.', value=REPLACE(value, 'You can analyse the result', '<table><thead><tr style=\"background-color:#cad3f1; font-style:bold\"><td>Start</td><td>End</td><td>Duration</td></tr></thead><tbody><tr><td>%TAGSTART%</td><td>%TAGEND%</td><td>%TAGDURATION% min</td></tr></tbody></table><br><br>Global Status : <br>%TAGGLOBALSTATUS%<br><br>Non OK TestCases : <br>%TAGTCDETAIL%<br><br>You can analyse the result') WHERE param='cerberus_notification_tagexecutionend_body';");
        a.add(b.toString());

        // New updated Documentation.
        // 1258-1259
        b = new StringBuilder();
        b.append("select 1 from DUAL;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("select 1 from DUAL;");
        a.add(b.toString());

        // rename parameters in order to fit standard
        // 1260-1263
        b = new StringBuilder();
        b.append("update parameter set param = replace(param, 'integration_', 'cerberus_') where param like 'integration_%';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("update parameter set param = replace(param, 'jenkins_', 'cerberus_jenkins') where param like 'jenkins_%';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("update parameter set param = 'cerberus_appium_swipe_duration' where param = 'appium_swipeDuration';");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("update parameter set param = replace(param, 'CI_OK_', 'cerberus_ci_okcoef') where param like 'CI_OK%';");
        a.add(b.toString());

        // moved Battery to Label
        // 1264-1268
        b = new StringBuilder();
        b.append("ALTER TABLE `label` ADD COLUMN `Type` VARCHAR(45) NOT NULL DEFAULT 'STICKER' AFTER `Label`;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("INSERT INTO label (`system`,`Label`,`Type`,`Color`,`ParentLabel`, `Description`, `UsrCreated`)");
        b.append(" SELECT '', testbattery, 'BATTERY', '#CCCCCC', '', Description, 'DatabaseVersioningV1264' from testbattery");
        b.append(" ON DUPLICATE KEY UPDATE `UsrModif` = 'DatabaseVersioningV1264', DateModif = now();");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("INSERT INTO testcaselabel (`Test`,`TestCase`,`LabelId`, `UsrCreated`)");
        b.append(" SELECT Test, TestCase, l.id, 'DatabaseVersioningV1264' from testbatterycontent b");
        b.append("  join label l where b.testbattery = l.Label");
        b.append("  ON DUPLICATE KEY UPDATE `UsrModif` = 'DatabaseVersioningV1264', DateModif = now();");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("INSERT INTO campaignlabel (`campaign`,`LabelId`, `UsrCreated`)");
        b.append(" SELECT campaign, l.id, 'DatabaseVersioningV1264' from campaigncontent b");
        b.append("  join label l where b.testbattery = l.Label");
        b.append("  ON DUPLICATE KEY UPDATE `UsrModif` = 'DatabaseVersioningV1264', DateModif = now();");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`) VALUES ");
        b.append("('LABELTYPE', 'STICKER', 100, 'Sticker.')");
        b.append(",('LABELTYPE', 'BATTERY', 200, 'Battery.')");
        b.append(",('LABELTYPE', 'REQUIREMENT', 300, 'Requirement.')");
        b.append(",('INVARIANTPRIVATE', 'LABELTYPE', 700, '');");
        a.add(b.toString());

        // ADD a parameter for the path to store csv file
        // 1269
        b = new StringBuilder();
        b.append("INSERT INTO `parameter` (`system`,`param`, `value`, `description`) VALUES ");
        b.append("  ('','cerberus_testdatalibcsv_path', '/path/to/csv', 'Default path for the csv file location')");
        a.add(b.toString());

        // ADD requirement additional data
        // 1270-1271
        b = new StringBuilder();
        b.append("ALTER TABLE `label` ");
        b.append("ADD COLUMN `ReqType` VARCHAR(100) NOT NULL DEFAULT '' AFTER `ParentLabel`,");
        b.append("ADD COLUMN `ReqStatus` VARCHAR(100) NOT NULL DEFAULT '' AFTER `ReqType`,");
        b.append("ADD COLUMN `ReqCriticity` VARCHAR(100) NOT NULL DEFAULT '' AFTER `ReqStatus`,");
        b.append("ADD COLUMN `LongDesc` TEXT NOT NULL AFTER `Description`;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`) VALUES  ");
        b.append(" ('REQUIREMENTTYPE', 'Unknown', 100, '')");
        b.append(",('REQUIREMENTTYPE', 'Ergonomy', 110, '')");
        b.append(",('REQUIREMENTTYPE', 'Evolutivity', 120, '')");
        b.append(",('REQUIREMENTTYPE', 'Functional', 130, '')");
        b.append(",('REQUIREMENTTYPE', 'Internationalization', 140, '')");
        b.append(",('REQUIREMENTTYPE', 'Legal', 150, '')");
        b.append(",('REQUIREMENTTYPE', 'Maintenance', 160, '')");
        b.append(",('REQUIREMENTTYPE', 'Operation', 170, '')");
        b.append(",('REQUIREMENTTYPE', 'Portability', 180, '')");
        b.append(",('REQUIREMENTTYPE', 'Performance', 190, '')");
        b.append(",('REQUIREMENTTYPE', 'Scalability', 200, '')");
        b.append(",('REQUIREMENTTYPE', 'Security', 210, '')");
        b.append(",('REQUIREMENTSTATUS', 'Unknown', 100, '')");
        b.append(",('REQUIREMENTSTATUS', 'Approved', 200, '')");
        b.append(",('REQUIREMENTSTATUS', 'In Progress', 300, '')");
        b.append(",('REQUIREMENTSTATUS', 'Verified', 400, '')");
        b.append(",('REQUIREMENTSTATUS', 'Obsolete', 500, '')");
        b.append(",('REQUIREMENTCRITICITY', 'Unknown', 100, '')");
        b.append(",('REQUIREMENTCRITICITY', 'Low', 200, '')");
        b.append(",('REQUIREMENTCRITICITY', 'Medium', 300, '')");
        b.append(",('REQUIREMENTCRITICITY', 'High', 400, '')");
        b.append(",('INVARIANTPUBLIC', 'REQUIREMENTTYPE', '700', '')");
        b.append(",('INVARIANTPUBLIC', 'REQUIREMENTSTATUS', '750', '')");
        b.append(",('INVARIANTPUBLIC', 'REQUIREMENTCRITICITY', '800', '');");
        a.add(b.toString());

        // New updated Documentation.
        // 1272-1273
        b = new StringBuilder();
        b.append("select 1 from DUAL;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("select 1 from DUAL;");
        a.add(b.toString());

        // New updated Documentation.
        // 1274-1275
        b = new StringBuilder();
        b.append("select 1 from DUAL;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("select 1 from DUAL;");
        a.add(b.toString());

        // ADD a parameter for the path to store manual exe files
        // 1276-1277
        b = new StringBuilder();
        b.append("INSERT INTO `parameter` (`system`,`param`, `value`, `description`) VALUES ");
        b.append("  ('','cerberus_exemanualmedia_path', '/path/to/exemanualmedia', 'Path to store the Cerberus Media files for Manual executions.')");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("UPDATE `parameter` SET `param`='cerberus_exeautomedia_path', `description`='Path to store the Cerberus Media files for Automatic executions (like Selenium Screenshot or SOAP requests and responses).' WHERE `param`='cerberus_mediastorage_path';");
        a.add(b.toString());

        // New updated Documentation.
        // 1278-1279
        b = new StringBuilder();
        b.append("select 1 from DUAL;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("select 1 from DUAL;");
        a.add(b.toString());

        // New updated Documentation.
        // 1280-1281
        b = new StringBuilder();
        b.append("select 1 from DUAL;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("select 1 from DUAL;");
        a.add(b.toString());

        // New Invariant to activate campaign notification
        // 1282
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`) VALUES  ");
        b.append(" ('CAMPAIGNSTARTNOTIF', 'Y', 100, 'Yes')");
        b.append(",('CAMPAIGNSTARTNOTIF', 'N', 200, 'No')");
        b.append(",('CAMPAIGNENDNOTIF', 'Y', 100, 'Yes')");
        b.append(",('CAMPAIGNENDNOTIF', 'N', 200, 'No')");
        b.append(",('CAMPAIGNENDNOTIF', 'CIKO', 300, 'Only when Continuous Integration result is KO.')");
        b.append(",('INVARIANTPRIVATE', 'CAMPAIGNSTARTNOTIF', '750', '')");
        b.append(",('INVARIANTPRIVATE', 'CAMPAIGNENDNOTIF', '800', '');");
        a.add(b.toString());

        // Enrich email notification.
        // 1283
        b = new StringBuilder(); // adding table with CI result. 
        b.append("UPDATE `parameter` SET ");
        b.append(" description='Cerberus End of tag execution notification email body. %TAG%, %URLTAGREPORT%, %CAMPAIGN%, %TAGDURATION%, %TAGSTART%, %TAGEND%, %CIRESULT%, %CISCORE%, %TAGGLOBALSTATUS% and %TAGTCDETAIL% can be used as variables.'");
        b.append(" , value=replace(value,'%TAGDURATION% min</td></tr></tbody></table>','%TAGDURATION% min</td></tr></tbody></table><table><thead><tr style=\"background-color:#cad3f1; font-style:bold\"><td>CI Result</td><td>CI Score</td></tr></thead><tbody><tr><td>%CIRESULT%</td><td>%CISCORE%</td></tr></tbody></table>') ");
        b.append(" where param='cerberus_notification_tagexecutionend_body';");
        a.add(b.toString());

        // Add invariant filter type.
        // 1284-1285
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) ");
        b.append(" VALUES ('INVARIANTPRIVATE','FILETYPE', '710','All type of file', 'file type')");
        a.add(b.toString());

        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) VALUES ");
        b.append("('FILETYPE', 'PNG', '6600', '', ''),");
        b.append("('FILETYPE', 'JPG', '6700', '', ''),");
        b.append("('FILETYPE', 'XML', '16500', '', ''),");
        b.append("('FILETYPE', 'JSON', '18500', '', ''),");
        b.append("('FILETYPE', 'TXT', '22500', '', '')");
        a.add(b.toString());

        // 1286
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) VALUES ");
        b.append("('FILETYPE', 'PDF', '23500', '', ''),");
        b.append("('FILETYPE', 'BIN', '24500', '', '')");
        a.add(b.toString());

        // Change datatype of testcasecountryproperties column `Length` to text
        // 1287
        b = new StringBuilder();
        b.append("ALTER TABLE testcasecountryproperties ");
        b.append("MODIFY COLUMN Length text");
        a.add(b.toString());

        // Clean keyPress value1 and value2 for IPA and APK applications.
        // 1288
        b = new StringBuilder();
        b.append("UPDATE testcasestepaction a1 SET value2=value1, value1='', last_modified = now() ");
        b.append("WHERE EXISTS ( select 1 from (");
        b.append("select a.test, a.testcase, a.step, a.sequence, a.value1, a.value2, a.last_modified from testcasestepaction a");
        b.append(" join testcase t on t.test=a.test and t.testcase=a.testcase");
        b.append(" join application ap on ap.application=t.application");
        b.append(" where ap.type in ('APK', 'IPA') and Action = 'keyPress'");
        b.append(") as t where t.test=a1.test and t.testcase=a1.testcase and t.step=a1.step and t.sequence=a1.sequence);");
        a.add(b.toString());

        // Modify table testcaseexecutiondata in order to support cache entry.
        // 1289
        b = new StringBuilder();
        b.append("ALTER TABLE testcaseexecutiondata ");
        b.append("ADD COLUMN `System` varchar(45) NOT NULL DEFAULT ' ' AFTER `index`, ");
        b.append("ADD COLUMN `Environment` varchar(45) NOT NULL DEFAULT ' ' AFTER `System`, ");
        b.append("ADD COLUMN `Country` varchar(45) NOT NULL DEFAULT ' ' AFTER `Environment`, ");
        b.append("ADD COLUMN `LengthInit` text AFTER `Value2`, ");
        b.append("ADD COLUMN `JsonResult` text AFTER `value`, ");
        b.append("ADD COLUMN `DataLib` varchar(45) NOT NULL DEFAULT ' ' AFTER `JsonResult`, ");
        b.append("MODIFY Length TEXT");
        a.add(b.toString());

        // Modify table testcasestepexecution
        // 1290
        b = new StringBuilder();
        b.append("ALTER TABLE `testcasestepexecution`  CHANGE COLUMN `ReturnMessage` `ReturnMessage` TEXT ;");
        a.add(b.toString());

        // Modify table testcaseexecutiondata adding cache flag
        // 1291
        b = new StringBuilder();
        b.append("ALTER TABLE `testcaseexecutiondata` ADD COLUMN `FromCache` VARCHAR(45) NULL DEFAULT 'N' AFTER `JsonResult`,");
        b.append(" ADD INDEX `IX_testcaseexecutiondata_03` (`System` ASC, `Environment` ASC, `Country` ASC, `FromCache` ASC, `Property` ASC, `Index` ASC, `Start` ASC);");
        a.add(b.toString());

        // Modify table testdatalib adding cacheExpire
        // 1292
        b = new StringBuilder("ALTER TABLE `testcasecountryproperties` ADD COLUMN `CacheExpire` INT NULL DEFAULT 0 AFTER `Nature`;");
        a.add(b.toString());

        // Drop deprecated tables.
        // 1293-1294
        b = new StringBuilder("DROP TABLE `abonnement`, `qualitynonconformities`, `qualitynonconformitiesimpact`;");
        a.add(b.toString());
        b = new StringBuilder("DROP TABLE `testbatterycontent`, `campaigncontent`, `testbattery`;");
        a.add(b.toString());

        // Adjust colum size for login information on execution table..
        // 1295
        b = new StringBuilder();
        b.append("ALTER TABLE `testcaseexecution` ");
        b.append("CHANGE COLUMN `Executor` `Executor` VARCHAR(255) NULL DEFAULT NULL ,");
        b.append("CHANGE COLUMN `UsrCreated` `UsrCreated` VARCHAR(255) NOT NULL DEFAULT '' ,");
        b.append("CHANGE COLUMN `UsrModif` `UsrModif` VARCHAR(255) NULL DEFAULT '' ;");
        a.add(b.toString());

        // Add the "executeJS" action
        // 1296
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) VALUES ('ACTION', 'executeJS', '6550', 'Execute Javascript', 'Execute JS');");
        a.add(b.toString());

        // ADD private invariant CAMPAIGN_TCCRITERIA : "GROUP"
        // 1297
        b = new StringBuilder();
        b.append("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`) VALUES ");
        b.append("  ('CAMPAIGN_TCCRITERIA', 'GROUP', 100 , '')");
        a.add(b.toString());

        // Modify the size of column datalib on testcaseexecutiondata
        // 1298
        b = new StringBuilder();
        b.append("ALTER TABLE testcaseexecutiondata ");
        b.append("MODIFY COLUMN datalib VARCHAR(200)");
        a.add(b.toString());

        // Add Column testCaseVersion on testcase table
        // 1299
        b = new StringBuilder();
        b.append("ALTER TABLE testcase ");
        b.append("ADD COLUMN TestCaseVersion int(10) DEFAULT 0 AFTER `screensize`");
        a.add(b.toString());

        // Add Column testCaseVersion on testcaseexecution table
        // 1300
        b = new StringBuilder();
        b.append("ALTER TABLE testcaseexecution ");
        b.append("ADD COLUMN TestCaseVersion int(10) DEFAULT 0 AFTER `QueueID`");
        a.add(b.toString());

        // Adding robotDeclination on testcaseexecution table.
        // 1301-1305
        b = new StringBuilder();
        b.append("ALTER TABLE `robot` ");
        b.append("CHANGE COLUMN `host_user` `host_user` VARCHAR(255) NULL DEFAULT NULL AFTER `Port`,");
        b.append("CHANGE COLUMN `host_password` `host_password` VARCHAR(255) NULL DEFAULT NULL AFTER `host_user`,");
        b.append("ADD COLUMN `robotdecli` VARCHAR(100) NOT NULL DEFAULT '' AFTER `screensize`;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcaseexecution` ");
        b.append("ADD COLUMN `System` VARCHAR(45) NOT NULL DEFAULT '' AFTER `ID`,");
        b.append("ADD COLUMN `RobotDecli` VARCHAR(100) NOT NULL DEFAULT '' AFTER `Country`;");
        a.add(b.toString());
        b = new StringBuilder("UPDATE testcaseexecution SET robotdecli=browser;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("ALTER TABLE `testcaseexecution` ");
        b.append("DROP INDEX `IX_testcaseexecution_04` ,");
        b.append("ADD INDEX `IX_testcaseexecution_04` (`Test` ASC, `TestCase` ASC, `Country` ASC, `RobotDecli` ASC, `Start` ASC, `ControlStatus` ASC),");
        b.append("DROP INDEX `IX_testcaseexecution_05` ,");
        b.append("ADD INDEX `IX_testcaseexecution_05` (`System` ASC),");
        b.append("DROP INDEX `IX_testcaseexecution_06` ;");
        a.add(b.toString());
        b = new StringBuilder("INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) VALUES ('CAMPAIGN_PARAMETER', 'ROBOT', '40', 'Robot used for execution.', 'Robot');");
        a.add(b.toString());

        // Cleaning invariant.
        // 1306-1307
        b = new StringBuilder("update invariant set gp1=null, gp2=null, gp3=null where idname in ('CAMPAIGN_PARAMETER', 'ACTIONCONDITIONOPER', 'ACTION', 'APPLITYPE', 'APPSERVICECONTENTACT', 'APPSERVICEHEADERACT', 'CONTROLCONDITIONOPER', 'INVARIANTPRIVATE', 'OUTPUTFORMAT', 'STEPLOOP', 'STEPCONDITIONOPER', 'SRVTYPE', 'SRVMETHOD', 'TESTCASECONDITIONOPER', 'VERBOSE');");
        a.add(b.toString());
        b = new StringBuilder("delete from invariant where idname in ('MNTACTIVE','NCONFSTATUS','PROBLEMCATEGORY','PROPERTYBAM','RESPONSABILITY','ROOTCAUSECATEGORY','SEVERITY');");
        a.add(b.toString());

        // New updated Documentation.
        // 1308-1309
        b = new StringBuilder("DELETE FROM `documentation`;");
        a.add(b.toString());
        b = new StringBuilder();
        b.append("INSERT INTO `documentation` VALUES ('application','Application','','en','Application','','_application_attributes')");
        b.append(",('application','Application','','fr','Application','','_application_attributes')");
        b.append(",('application','bugtrackernewurl','','en','New Bug URL','This correspond to the URL that points to the page where a new bug can be created on the Bug system of the <code class=\\'doc-crbvvoca\\'>application</code>.<br><br><table cellspacing=0 cellpadding=3><th class=\\'ex\\' colspan=\\'2\\'>The following variables can be used inside the URL</th><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%TEST%</code></td><td class=\\'ex\\'>Test</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%TESTCASE%</code></td><td class=\\'ex\\'>Test case reference</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%TESTCASEDESC%</code></td><td class=\\'ex\\'>Description of the test case</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%EXEID%</code></td><td class=\\'ex\\'>Execution ID</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%EXEDATE%</code></td><td class=\\'ex\\'>Start date and time of the execution.</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%ENV%</code></td><td class=\\'ex\\'>Environment</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%COUNTRY%</code></td><td class=\\'ex\\'>Country</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%BUILD%</code></td><td class=\\'ex\\'>Build</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%REV%</code></td><td class=\\'ex\\'>Revision</td></tr></table>','_application_attributes')");
        b.append(",('application','bugtrackernewurl','','fr','URL pour nouveau Bug','Correspond à l\\'URL qui pointe vers la page de création de bug du Bug Tracker de l\\'<code class=\\'doc-crbvvoca\\'>application</code>.<br><br><table cellspacing=0 cellpadding=3><th class=\\'ex\\' colspan=\\'2\\'>Les variables suivantes peuvent être utilisées dans l\\'URL</th><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%TEST%</code></td><td class=\\'ex\\'>Test</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%TESTCASE%</code></td><td class=\\'ex\\'>Reference du cas de test</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%TESTCASEDESC%</code></td><td class=\\'ex\\'>Description du cas de test</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%EXEID%</code></td><td class=\\'ex\\'>ID de l\\'execution</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%EXEDATE%</code></td><td class=\\'ex\\'>Date et heure du debut de l\\'execution.</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%ENV%</code></td><td class=\\'ex\\'>Environnement</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%COUNTRY%</code></td><td class=\\'ex\\'>Pays</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%BUILD%</code></td><td class=\\'ex\\'>Build</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%REV%</code></td><td class=\\'ex\\'>Revision</td></tr></table>','_application_attributes')");
        b.append(",('application','bugtrackerurl','','en','Bug Tracker URL','This correspond to the URL of the Bug reporting system of the <code class=\\'doc-crbvvoca\\'>application</code>.<br><br><table cellspacing=0 cellpadding=3><th class=\\'ex\\' colspan=\\'2\\'>The following variables can be used inside the URL</th><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%BUGID%</code></td><td class=\\'ex\\'>ID of the Bug</td></tr></table>','_application_attributes')");
        b.append(",('application','bugtrackerurl','','fr','URL du Bug Tracker','Correspond à l\\'URL du Bug Tracker de l\\'<code class=\\'doc-crbvvoca\\'>application</code>.<br><br><table cellspacing=0 cellpadding=3><th class=\\'ex\\' colspan=\\'2\\'>Les variables suivantes peuvent être utilisées dans l\\'URL</th><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%BUGID%</code></td><td class=\\'ex\\'>ID du Bug</td></tr></table>','_application_attributes')");
        b.append(",('application','deploytype','','en','Deploy Type','This information groups the <code class=\\'doc-crbvvoca\\'>application</code> by typology of deployement process.<br>It can be used as a variable in the parameter <code class=\\'doc-parameter\\'>cerberus_jenkinsdeploy_url</code> that correspond to the URL that calls a continious integration system such as Jenkins.','_application_attributes')");
        b.append(",('application','deploytype','','fr','Type de deploiement','Cette information groupe les <code class=\\'doc-crbvvoca\\'>applications</code> par typologie de deploiement.<br>Peut être utilisé comme variable dans le parametre <code class=\\'doc-parameter\\'>cerberus_jenkinsdeploy_url</code> qui correspond à l\\'URL appelée vers un systeme d\\'intégration continue de type Jenkins.','_application_attributes')");
        b.append(",('application','Description','','en','Description','This is the short Description of the <code class=\\'doc-crbvvoca\\'>application</code>.','_application_attributes')");
        b.append(",('application','Description','','fr','Description','Description courte de l\\'<code class=\\'doc-crbvvoca\\'>application</code>.','_application_attributes')");
        b.append(",('application','mavengroupid','','en','Maven Group ID','','_application_attributes')");
        b.append(",('application','mavengroupid','','fr','Identifiant du groupe Maven','','_application_attributes')");
        b.append(",('application','sort','','en','Sort','This correspond to an integer value that is used as a sorting criteria for various combo box within Cerberus GUI.','_application_attributes')");
        b.append(",('application','sort','','fr','identifiant de Tri','Correspond à une valeur entière et utilisée pour trier les differentes valeurs dans l\\'interface graphique.','_application_attributes')");
        b.append(",('application','subsystem','','en','Subsystem','A <code class=\\'doc-crbvvoca\\'>Subsystem</code> define a group of <code class=\\'doc-crbvvoca\\'>application</code> inside a <code class=\\'doc-crbvvoca\\'>system</code>.','_application_attributes')");
        b.append(",('application','subsystem','','fr','Sous Système','Le <code class=\\'doc-crbvvoca\\'>Sous système</code> regroupe un ensemble d\\'<code class=\\'doc-crbvvoca\\'>application</code> au sein d\\'un même <code class=\\'doc-crbvvoca\\'>système</code>.','_application_attributes')");
        b.append(",('application','svnurl','','en','SVN URL','This correspond to the URL of the svn repository of the <code class=\\'doc-crbvvoca\\'>application</code>.','_application_attributes')");
        b.append(",('application','svnurl','','fr','URL du SVN','Correspond à l\\'URL du SVN de l\\'<code class=\\'doc-crbvvoca\\'>application</code>.','_application_attributes')");
        b.append(",('application','system','','en','System','A <code class=\\'doc-crbvvoca\\'>system</code> is a group of <code class=\\'doc-crbvvoca\\'>application</code> for which all changes sometimes require to be done all together.<br> Most of the time those <code class=\\'doc-crbvvoca\\'>applications</code> all connect to a single database and share the same data structure.','_application_attributes')");
        b.append(",('application','system','','fr','Système','Un <code class=\\'doc-crbvvoca\\'>système</code> est un groupe d\\'<code class=\\'doc-crbvvoca\\'>applications</code> pour lesquels il y a de temps en temps necessité de faire les changements en même temps.<br> La plupart du temps ces <code class=\\'doc-crbvvoca\\'>applications</code> partagent une même base de donnée et donc une structure de donnée unique.','_application_attributes')");
        b.append(",('application','type','','en','Type','The Type of the <code class=\\'doc-crbvvoca\\'>application</code> define whether the <code class=\\'doc-crbvvoca\\'>application</code> is a GUI, a Service or a Batch Treatment.<br>An automated <code class=\\'doc-crbvvoca\\'>testcase</code> based on a GUI <code class=\\'doc-crbvvoca\\'>application</code> will require a selenium server to execute.','_application_attributes')");
        b.append(",('application','type','','fr','Type','Le type de l\\'<code class=\\'doc-crbvvoca\\'>application</code> defini si l\\'<code class=\\'doc-crbvvoca\\'>application</code> est une interface graphique (GUI), un fournisseur de Service ou un traitement batch.<br>Un <code class=\\'doc-crbvvoca\\'>cas de test</code> automatisé basé sur une <code class=\\'doc-crbvvoca\\'>application</code> de type GUI necessitera un serveur Selenium pour s\\'executer.','_application_attributes')");
        b.append(",('applicationObject','Object','','en','Object','',NULL)");
        b.append(",('applicationObject','Object','','fr','Objet','',NULL)");
        b.append(",('applicationObject','ScreenshotFileName','','en','Image Filename','',NULL)");
        b.append(",('applicationObject','ScreenshotFileName','','fr','Nom de fichier de l\\'image','',NULL)");
        b.append(",('applicationObject','Value','','en','Value','',NULL)");
        b.append(",('applicationObject','Value','','fr','Valeur','',NULL)");
        b.append(",('appservice','application','','en','Application','','_service_library')");
        b.append(",('appservice','application','','fr','Application','','_librairie_de_services')");
        b.append(",('appservice','description','','en','Description','','_service_library')");
        b.append(",('appservice','description','','fr','Description','','_librairie_de_services')");
        b.append(",('appservice','group','','en','Group','','_service_library')");
        b.append(",('appservice','group','','fr','Groupe','','_librairie_de_services')");
        b.append(",('appservice','method','','en','Method','','_service_library')");
        b.append(",('appservice','method','','fr','Méthode','','_librairie_de_services')");
        b.append(",('appservice','operation','','en','Operation','','_service_library')");
        b.append(",('appservice','operation','','fr','Operation','','_librairie_de_services')");
        b.append(",('appservice','service','','en','Service','','_service_library')");
        b.append(",('appservice','service','','fr','Service','','_librairie_de_services')");
        b.append(",('appservice','servicePath','','en','Service Path','','_service_library')");
        b.append(",('appservice','servicePath','','fr','Chemin du service','','_librairie_de_services')");
        b.append(",('appservice','srvRequest','','en','Request','','_service_library')");
        b.append(",('appservice','srvRequest','','fr','Requête','','_librairie_de_services')");
        b.append(",('appservice','type','','en','Type','','_service_library')");
        b.append(",('appservice','type','','fr','Type','','_librairie_de_services')");
        b.append(",('batchinvariant','Batch','','en','Batch','','_batch_invariant_attributes')");
        b.append(",('batchinvariant','Batch','','fr','Batch','','_batch_invariant_attributes')");
        b.append(",('batchinvariant','Description','','en','Description','Description of the batch.','_batch_invariant_attributes')");
        b.append(",('batchinvariant','Description','','fr','Description','Description du batch','_batch_invariant_attributes')");
        b.append(",('batchinvariant','system','','en','System','System of the batch','_batch_invariant_attributes')");
        b.append(",('batchinvariant','system','','fr','System','System du batch','_batch_invariant_attributes')");
        b.append(",('buildrevisionbatch','batch','','en','Batch','',NULL)");
        b.append(",('buildrevisionbatch','batch','','fr','Batch','',NULL)");
        b.append(",('buildrevisionbatch','build','','en','Build','Build during the excution of the Batch Event.',NULL)");
        b.append(",('buildrevisionbatch','build','','fr','Build','Build lors de l\\'éxecution de l\\'événement Batch.',NULL)");
        b.append(",('buildrevisionbatch','dateBatch','','en','Date','',NULL)");
        b.append(",('buildrevisionbatch','dateBatch','','fr','Date','',NULL)");
        b.append(",('buildrevisionbatch','revision','','en','Revision','Revision during the excution of the Batch Event.',NULL)");
        b.append(",('buildrevisionbatch','revision','','fr','Revision','Revision lors de l\\'éxecution de l\\'événement Batch.',NULL)");
        b.append(",('buildrevisioninvariant','level','','en','Level','','_build_revision_invariant_attributes')");
        b.append(",('buildrevisioninvariant','level','','fr','Niveau','','_build_revision_invariant_attributes')");
        b.append(",('buildrevisioninvariant','seq','','en','Sequence','','_build_revision_invariant_attributes')");
        b.append(",('buildrevisioninvariant','seq','','fr','Séquence','','_build_revision_invariant_attributes')");
        b.append(",('buildrevisioninvariant','system','','en','System','','_build_revision_invariant_attributes')");
        b.append(",('buildrevisioninvariant','system','','fr','Système','','_build_revision_invariant_attributes')");
        b.append(",('buildrevisioninvariant','versionName','','en','Version Name','','_build_revision_invariant_attributes')");
        b.append(",('buildrevisioninvariant','versionName','','fr','Nom de la Version','','_build_revision_invariant_attributes')");
        b.append(",('buildrevisioninvariant','versionname01','','en','Build','','_build_revision_invariant_attributes')");
        b.append(",('buildrevisioninvariant','versionname01','','fr','Build','','_build_revision_invariant_attributes')");
        b.append(",('buildrevisioninvariant','versionname02','','en','Revision','','_build_revision_invariant_attributes')");
        b.append(",('buildrevisioninvariant','versionname02','','fr','Revision','','_build_revision_invariant_attributes')");
        b.append(",('buildrevisionparameters','application','','en','Application','','_build_content')");
        b.append(",('buildrevisionparameters','application','','fr','Application','','_contenu_des_builds')");
        b.append(",('buildrevisionparameters','BugIDFixed','','en','Associated Bug ID','This is the bug ID which has been solved with the <code class=\\'doc-crbvvoca\\'>release</code>.','_build_content')");
        b.append(",('buildrevisionparameters','BugIDFixed','','fr','ID du Bug associé','ID du bug dont la release est associée.','_contenu_des_builds')");
        b.append(",('buildrevisionparameters','Build','','en','Build','','_build_content')");
        b.append(",('buildrevisionparameters','Build','','fr','Build','','_contenu_des_builds')");
        b.append(",('buildrevisionparameters','datecre','','en','Creation Date','','_build_content')");
        b.append(",('buildrevisionparameters','datecre','','fr','Date de Création','','_contenu_des_builds')");
        b.append(",('buildrevisionparameters','id','','en','ID','','_build_content')");
        b.append(",('buildrevisionparameters','id','','fr','ID','','_contenu_des_builds')");
        b.append(",('buildrevisionparameters','jenkinsBuildId','','en','Jenkins Build ID','','_build_content')");
        b.append(",('buildrevisionparameters','jenkinsBuildId','','fr','ID du build Jenkins','','_contenu_des_builds')");
        b.append(",('buildrevisionparameters','Link','','en','Link','This is the link to the detailed content of the <code class=\\'doc-crbvvoca\\'>release</code>.','_build_content')");
        b.append(",('buildrevisionparameters','Link','','fr','Lien','Lien vers le detail de la <code class=\\'doc-crbvvoca\\'>release</code>.','_contenu_des_builds')");
        b.append(",('buildrevisionparameters','mavenArtifactId','','en','Maven Artifact ID','','_build_content')");
        b.append(",('buildrevisionparameters','mavenArtifactId','','fr','Maven Artifact ID','','_contenu_des_builds')");
        b.append(",('buildrevisionparameters','mavenGroupId','','en','Maven Group ID','','_build_content')");
        b.append(",('buildrevisionparameters','mavenGroupId','','fr','ID du groupe Maven','','_contenu_des_builds')");
        b.append(",('buildrevisionparameters','mavenVersion','','en','Maven Version','','_build_content')");
        b.append(",('buildrevisionparameters','mavenVersion','','fr','Maven Version','','_contenu_des_builds')");
        b.append(",('buildrevisionparameters','project','','en','Project','','_build_content')");
        b.append(",('buildrevisionparameters','project','','fr','Projet','','_contenu_des_builds')");
        b.append(",('buildrevisionparameters','Release','','en','Release','A <code class=\\'doc-crbvvoca\\'>release</code> is a single change done on the <code class=\\'doc-crbvvoca\\'>application</code>.','_build_content')");
        b.append(",('buildrevisionparameters','Release','','fr','Release','A <code class=\\'doc-crbvvoca\\'>release</code> is a single change done on the <code class=\\'doc-crbvvoca\\'>application</code>.','_contenu_des_builds')");
        b.append(",('buildrevisionparameters','ReleaseOwner','','en','Owner','This is the name of the person who is responsible for the <code class=\\'doc-crbvvoca\\'>release</code>.','_build_content')");
        b.append(",('buildrevisionparameters','ReleaseOwner','','fr','Responsable','Nom de la personne responsable de la <code class=\\'doc-crbvvoca\\'>release</code>.','_contenu_des_builds')");
        b.append(",('buildrevisionparameters','repositoryUrl','','en','Repository URL','This information corresponds to the URL where the current build of the <code class=\\'doc-crbvvoca\\'>application</code> can be downloaded.<br>It allow to retrieve it in a repository such as Nexus.','_build_content')");
        b.append(",('buildrevisionparameters','repositoryUrl','','fr','URL du Dépot','Cette information correspond à l\\'URL d\\'où le build de l\\'<code class=\\'doc-crbvvoca\\'>application</code> peut-être téléchargé.<br>Cela permet de retrouver un build spécifique dans un dépot de livrable de type Nexus.','_contenu_des_builds')");
        b.append(",('buildrevisionparameters','Revision','','en','Revision','','_build_content')");
        b.append(",('buildrevisionparameters','Revision','','fr','Revision','','_contenu_des_builds')");
        b.append(",('buildrevisionparameters','subject','','en','Description','','_build_content')");
        b.append(",('buildrevisionparameters','subject','','fr','Description','','_contenu_des_builds')");
        b.append(",('buildrevisionparameters','TicketIDFixed','','en','Associated Ticket ID','This is the Ticket ID which has been delivered with the <code class=\\'doc-crbvvoca\\'>release</code>.','_build_content')");
        b.append(",('buildrevisionparameters','TicketIDFixed','','fr','ID du Ticket associé','ID du ticket dont la release est associée.','_contenu_des_builds')");
        b.append(",('countryenvdeploytype','JenkinsAgent','','en','Jenkins Agent','','_deploy_type')");
        b.append(",('countryenvdeploytype','JenkinsAgent','','fr','Agent Jenkins','','_type_de_déploiement')");
        b.append(",('countryenvironmentdatabase','ConnectionPoolName','','en','JDBC Resource','This is the name of the JDBC Resource used to connect to the corresponding <code class=\\'doc-crbvvoca\\'>database</code> on the <code class=\\'doc-crbvvoca\\'>country</code> / <code class=\\'doc-crbvvoca\\'>environment</code>.<br>The JDBC Resource (prefixed by <code class=\\'doc-fixed\\'>jdbc/</code> ) needs to be configured and associated to a connection pool on the application server that host the Cerberus application.<br><br>Example :<br><doc class=\"examples\"><table cellspacing=0 cellpadding=3><th class=\\'ex\\'>JDBC Resource</th><th class=\\'ex\\'>Application server Resource name</th><tr>\n<td class=\\'ex\\'>MyConnection</td>\n<td class=\\'ex\\'>jdbc/MyConnection</td>\n</tr></table>\n</doc>','_environment')");
        b.append(",('countryenvironmentdatabase','ConnectionPoolName','','fr','Ressource JDBC','Nom de la ressource JDBC utilisée pour se connecter à la <code class=\\'doc-crbvvoca\\'>base de donnée</code> correspondant au <code class=\\'doc-crbvvoca\\'>pays</code> / <code class=\\'doc-crbvvoca\\'>environnement</code>.<br>La ressource JDBC (préfixée par <code class=\\'doc-fixed\\'>jdbc/</code> ) doit être configuré dans le serveur l\\'application qui héberge Cerberus et associé à un pool de connexion.<br><br>Exemple :<br><doc class=\"examples\"><table cellspacing=0 cellpadding=3><th class=\\'ex\\'>JDBC Ressource</th><th class=\\'ex\\'>Application server Ressource name</th><tr>\n<td class=\\'ex\\'>MyConnection</td>\n<td class=\\'ex\\'>jdbc/MyConnection</td>\n</tr></table>\n</doc>','_environnement')");
        b.append(",('countryenvironmentdatabase','Database','','en','Database','','_environment')");
        b.append(",('countryenvironmentdatabase','Database','','fr','Base de donnée','','_environnement')");
        b.append(",('countryenvironmentparameters','domain','','en','Domain','Domain of the Application. Can be used inside any test execution with %SYS_APP_DOMAIN% variable.','_environment')");
        b.append(",('countryenvironmentparameters','domain','','fr','Domaine','Domaine Internet de l\\'application. Peut être utilisé pendant l\\'execution des tests avec la variable %SYS_APP_DOMAIN%.','_environnement')");
        b.append(",('countryenvironmentparameters','IP','','en','Host','Ressource location of the application.<br><br>Examples for GUI, IPA and APK applications:<br><doc class=\"examples\"><code class=\\'doc-url\\'>www.domain.com</code><br><code class=\\'doc-url\\'>192.168.1.1:80</code><br><code class=\\'doc-url\\'>user:pass@www.domain.com:8080</code><br><code class=\\'doc-url\\'>user:pass@192.168.1.1:80</code><br><code class=\\'doc-url\\'>http://www.laredoute.fr</code><br><code class=\\'doc-url\\'>https://www.facebook.com</code><br></doc><br>Examples for FAT applications:<br><doc class=\"examples\"><code class=\\'doc-url\\'>/usr/bin/kate</code><br><code class=\\'doc-url\\'>C:Programsprog.exe</code><br></doc><br>NB : For GUI applications, if the protocol is not specified, the default selected is http://<br>In case you want to test an https:// application, this ressource location must begin by https://.','_environment')");
        b.append(",('countryenvironmentparameters','IP','','fr','Hote','Chemin de l\\'application.<br><br>Exemples pour applications GUI, IPA et APK :<br><doc class=\"examples\"><code class=\\'doc-url\\'>www.domain.com</code><br><code class=\\'doc-url\\'>192.168.1.1:80</code><br><code class=\\'doc-url\\'>user:password@www.domain.com:8080</code><br><code class=\\'doc-url\\'>user:password@192.168.1.1:80</code><br><code class=\\'doc-url\\'>http://www.laredoute.fr</code><br><code class=\\'doc-url\\'>https://www.facebook.com</code><br></doc><br>Exemples pour les applications FAT :<br><doc class=\"examples\"><code class=\\'doc-url\\'>/usr/bin/kate</code><br><code class=\\'doc-url\\'>C:Programsprog.exe</code><br></doc><br>NB : Pour les applications GUI, si le protocole n\\'est pas specifié, le protocople par default utilisé est http://<br>En cas de test d\\'une application en https, il faut commencer l\\'URL par https://.','_environnement')");
        b.append(",('countryenvironmentparameters','poolSize','','en','Pool size','Maximal number of testcases that can be executed in same time by a single Cerberus instance','_environment')");
        b.append(",('countryenvironmentparameters','poolSize','','fr','Parallelisation','Nombre maximal, par instances Cerberus, de tests pouvant être exécutés en parallèle','_environnement')");
        b.append(",('countryenvironmentparameters','URL','','en','Context Root','Root URL used to access the application. Equivalent to context root.<br>This path will always be added to the information specified in the testcase.<br><br>Example :<br><doc class=\"examples\"><table cellspacing=0 cellpadding=3><th class=\\'ex\\'>URL</th><th class=\\'ex\\'>Description</th><tr><td class=\\'ex\\'><code class=\\'doc-url\\'>/Cerberus-1.0.1-SNAPSHOT/</code></td><td class=\\'ex\\'>When opening <code class=\\'doc-url\\'>login.jsp</code>, Cerberus will open <code class=\\'doc-url\\'>/Cerberus-1.0.1-SNAPSHOT/login.jsp</code> URL</td></tr></table></doc>','_environment')");
        b.append(",('countryenvironmentparameters','URL','','fr','Context Root','URL Racine de l\\'application. Equivalent du Context Root.<br>Ce chemin sera systematiquement ajouté aux chemin specifiés dans chaque cas de test.<br><br>Exemple :<br><doc class=\"examples\"><table cellspacing=0 cellpadding=3><th class=\\'ex\\'>URL</th><th class=\\'ex\\'>Description</th><tr><td class=\\'ex\\'><code class=\\'doc-url\\'>/Cerberus-1.0.1-SNAPSHOT/</code></td><td class=\\'ex\\'>Lorsque l\\'on ouvrira <code class=\\'doc-url\\'>login.jsp</code>, Cerberus ouvrira  <code class=\\'doc-url\\'>/Cerberus-1.0.1-SNAPSHOT/login.jsp</code> URL</td></tr></table></doc>','_environnement')");
        b.append(",('countryenvironmentparameters','URLLOGIN','','en','Login URL','Path to login page. This path is used only when calling the <code class=\\'doc-action\\'>openUrlLogin</code> Action.','_environment')");
        b.append(",('countryenvironmentparameters','URLLOGIN','','fr','URL de Login','Chemin vers la page de login. Ce chemin peut être utilisé à partir de l\\'action <code class=\\'doc-action\\'>openUrlLogin</code>.','_environnement')");
        b.append(",('countryenvironmentparameters','Var1','','en','Variable 1','Variable can be used inside testcases with %SYS_APP_VAR1% variable.','_environment')");
        b.append(",('countryenvironmentparameters','Var1','','fr','Variable 1','Variable qui peut être utilisée dans les test avec : %SYS_APP_VAR1%','_environnement')");
        b.append(",('countryenvironmentparameters','Var2','','en','Variable 2','Variable can be used inside testcases with %SYS_APP_VAR2% variable.','_environment')");
        b.append(",('countryenvironmentparameters','Var2','','fr','Variable 2','Variable qui peut être utilisée dans les test avec : %SYS_APP_VAR2%','_environnement')");
        b.append(",('countryenvironmentparameters','Var3','','en','Variable 3','Variable can be used inside testcases with %SYS_APP_VAR3% variable.','_environment')");
        b.append(",('countryenvironmentparameters','Var3','','fr','Variable 3','Variable qui peut être utilisée dans les test avec : %SYS_APP_VAR3%','_environnement')");
        b.append(",('countryenvironmentparameters','Var4','','en','Variable 4','Variable can be used inside testcases with %SYS_APP_VAR4% variable.','_environment')");
        b.append(",('countryenvironmentparameters','Var4','','fr','Variable 4','Variable qui peut être utilisée dans les test avec : %SYS_APP_VAR4%','_environnement')");
        b.append(",('countryenvlink','CountryLink','','en','Country linked','','_environment')");
        b.append(",('countryenvlink','CountryLink','','fr','Pays lié','','_environnement')");
        b.append(",('countryenvlink','EnvironmentLink','','en','Environment linked','','_environment')");
        b.append(",('countryenvlink','EnvironmentLink','','fr','Environnement lié','','_environnement')");
        b.append(",('countryenvlink','systemLink','','en','System','','_environment')");
        b.append(",('countryenvlink','systemLink','','fr','System','','_environnement')");
        b.append(",('countryenvparam','active','','en','Active','Define if the <code class=\\'doc-crbvvoca\\'>environment</code> is active or not. A <code class=\\'doc-crbvvoca\\'>test case</code> cannot be executed against an <code class=\\'doc-crbvvoca\\'>environment</code> that is not  active.','_environment')");
        b.append(",('countryenvparam','active','','fr','Actif','','_environnement')");
        b.append(",('countryenvparam','chain','','en','Chain','','_environment')");
        b.append(",('countryenvparam','chain','','fr','Chaine','','_environnement')");
        b.append(",('countryenvparam','Description','','en','Description','','_environment')");
        b.append(",('countryenvparam','Description','','fr','Description','','_environnement')");
        b.append(",('countryenvparam','DistribList','','en','Recipent list of Notification Email','This is the list of email adresses that will receive the notification on any environment event.<br><br>In case that value is not feeded, the following parameters are used (depending on the related event) :<br><code class=\\'doc-parameter\\'>cerberus_notification_disableenvironment_to</code><br><code class=\\'doc-parameter\\'>cerberus_notification_newbuildrevision_to</code><br><code class=\\'doc-parameter\\'>cerberus_notification_newchain_to</code>','_environment')");
        b.append(",('countryenvparam','DistribList','','fr','Emails des destinataires','','_environnement')");
        b.append(",('countryenvparam','EMailBodyChain','','en','EMail Body on New Chain Executed Event','This is the Body of the mail that will be generated when a new Treatment has been executed on the <code class=\\'doc-crbvvoca\\'>environment</code>.<br><br><table cellspacing=0 cellpadding=3><th class=\\'ex\\' colspan=\\'2\\'>The following variable can be used</th><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYSTEM%</code></td><td class=\\'ex\\'>System value</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%COUNTRY%</code></td><td class=\\'ex\\'>Country code</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%ENV%</code></td><td class=\\'ex\\'>Environment code</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%BUILD%</code></td><td class=\\'ex\\'>Current Build version name</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%REVISION%</code></td><td class=\\'ex\\'>Current Revision version name</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%CHAIN%</code></td><td class=\\'ex\\'>Chain value that has been executed</td></tr></table><br>In case that value is not feeded, the following parameter is used :<br><code class=\\'doc-parameter\\'>cerberus_notification_newchain_body</code>','_environment')");
        b.append(",('countryenvparam','EMailBodyChain','','fr','Corps du message en cas de nouvelle Chaine','','_environnement')");
        b.append(",('countryenvparam','EMailBodyDisableEnvironment','','en','EMail Body on Disable Environment Event','This is the Body of the mail that will be generated when <code class=\\'doc-crbvvoca\\'>environment</code> is disabled for installation purpose.<br><br><table cellspacing=0 cellpadding=3><th class=\\'ex\\' colspan=\\'2\\'>The following variable can be used</th><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYSTEM%</code></td><td class=\\'ex\\'>System value</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%COUNTRY%</code></td><td class=\\'ex\\'>Country code</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%ENV%</code></td><td class=\\'ex\\'>Environment code</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%BUILD%</code></td><td class=\\'ex\\'>Current Build version name</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%REVISION%</code></td><td class=\\'ex\\'>Current Revision version name</td></tr></table><br>In case that value is not feeded, the following parameter is used :<br><code class=\\'doc-parameter\\'>cerberus_notification_disableenvironment_body</code>','_environment')");
        b.append(",('countryenvparam','EMailBodyDisableEnvironment','','fr','Corps du message en cas de désactivation','','_environnement')");
        b.append(",('countryenvparam','EMailBodyRevision','','en','EMail Body on New Build/Revision Event','This is the Body of the mail that will be generated when a new Build/Revision is installed on the <code class=\\'doc-crbvvoca\\'>environment</code>.<br><br><table cellspacing=0 cellpadding=3><th class=\\'ex\\' colspan=\\'2\\'>The following variable can be used</th><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYSTEM%</code></td><td class=\\'ex\\'>System value</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%COUNTRY%</code></td><td class=\\'ex\\'>Country code</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%ENV%</code></td><td class=\\'ex\\'>Environment code</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%BUILD%</code></td><td class=\\'ex\\'>Current Build version name</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%REVISION%</code></td><td class=\\'ex\\'>Current Revision version name</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%BUILDCONTENT%</code></td><td class=\\'ex\\'>Detailed content of the sprint/revision.<br>That include the list of release of every application.</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%TESTRECAP%</code></td><td class=\\'ex\\'>A summary of test cases executed for that build and revision for the country.</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%TESTRECAPALL%</code></td><td class=\\'ex\\'>A summary of test cases executed for that build and revision for all the countries.</td></tr></table><br><br>In case that value is not feeded, the following parameter is used :<br><code class=\\'doc-parameter\\'>cerberus_notification_newbuildrevision_body</code>','_environment')");
        b.append(",('countryenvparam','EMailBodyRevision','','fr','Corps du message en cas de nouvelle Revision','','_environnement')");
        b.append(",('countryenvparam','maintenanceact','','en','Maintenance Activation','This is the activation flag of the daily maintenance period.<br>In case the flag is activated, start and end times needs to be specified.<br>During a maintenance period, the <code class=\\'doc-crbvvoca\\'>environment</code> is considered as disable and Cerberus will prevent the test case from beeing executed.','_environment')");
        b.append(",('countryenvparam','maintenanceact','','fr','Activation de la plage de maintenance','','_environnement')");
        b.append(",('countryenvparam','maintenanceend','','en','Maintenance End Time','This is the time when the daily maintenance period ends.<br>If start time is before end time then, any test execution request submitted between start and end will be discarded with an explicit error message that will report the maintenance period and time of the submission.<br>If start is after end then any test execution request submitted between end and start will be possible. All the overs will be discarded with an explicit error message that will report the maintenance period and time of the submission.<br><br>Examples :<br><doc class=\"examples\"><table cellspacing=0 cellpadding=3><th class=\\'ex\\'>flag</th><th class=\\'ex\\'>start</th><th class=\\'ex\\'>end</th><th class=\\'ex\\'>Result</th><tr><td class=\\'ex\\'>Yes</td><td class=\\'ex\\'>23:00:00</td><td class=\\'ex\\'><b>23:30:00</b></td><td class=\\'ex\\'>Any execution between 23H00 and 23H30 will be discarded.</td></tr><tr><td class=\\'ex\\'>Yes</td><td class=\\'ex\\'>23:00:00</td><td class=\\'ex\\'><b>02:30:00</b></td><td class=\\'ex\\'>Any execution between 23H00 and 2H30 will be discarded.</td></tr><tr><td class=\\'ex\\'>No</td><td class=\\'ex\\'>23:00:00</td><td class=\\'ex\\'><b>23:30:00</b></td><td class=\\'ex\\'>All executions will be authorised.</td></tr></table></doc>','_environment')");
        b.append(",('countryenvparam','maintenanceend','','fr','Heure de fin de la plage de maintenance','','_environnement')");
        b.append(",('countryenvparam','maintenancestr','','en','Maintenance Start Time','This is the time when the daily maintenance period starts.<br>If start is before end then, any test execution request submitted between start and end will be discarded with an explicit error message that will report the maintenance period and time of the submission.<br>If start is after end then any test execution request submitted between end and start will be possible. All the overs will be discarded with an explicit error message that will report the maintenance period and time of the submission.<br><br>Examples :<br><doc class=\"examples\"><table cellspacing=0 cellpadding=3><th class=\\'ex\\'>flag</th><th class=\\'ex\\'>start</th><th class=\\'ex\\'>end</th><th class=\\'ex\\'>Result</th><tr><td class=\\'ex\\'>Yes</td><td class=\\'ex\\'><b>23:00:00</b></td><td class=\\'ex\\'>23:30:00</td><td class=\\'ex\\'>Any execution between 23H00 and 23H30 will be discarded.</td></tr><tr><td class=\\'ex\\'>Yes</td><td class=\\'ex\\'><b>23:00:00</b></td><td class=\\'ex\\'>02:30:00</td><td class=\\'ex\\'>Any execution between 23H00 and 2H30 will be discarded.</td></tr><tr><td class=\\'ex\\'>No</td><td class=\\'ex\\'><b>23:00:00</b></td><td class=\\'ex\\'>23:30:00</td><td class=\\'ex\\'>All executions will be authorised.</td></tr></table></doc>','_environment')");
        b.append(",('countryenvparam','maintenancestr','','fr','Heure de début de la plage de maintenance','','_environnement')");
        b.append(",('countryenvparam','system','','en','System','','_environment')");
        b.append(",('countryenvparam','system','','fr','Système','','_environnement')");
        b.append(",('countryenvparam','Type','','en','Type','The type of the <code class=\\'doc-crbvvoca\\'>environment</code> define what is the <code class=\\'doc-crbvvoca\\'>environment</code> used for.<br><br><p>\\'STD\\' Standard Testing is allowed in the <code class=\\'doc-crbvvoca\\'>environment</code>.</p><p>\\'COMPARISON\\' Only Comparison test case is allowed to be executed on the <code class=\\'doc-crbvvoca\\'>environment</code>. No other test cases is allowed to execute. This is to avoid modifying any data on the <code class=\\'doc-crbvvoca\\'>environment</code> and not beeing able to analyse easilly the differences between 2 Build/Revisions.</p>','_environment')");
        b.append(",('countryenvparam','Type','','fr','Type','','_environnement')");
        b.append(",('countryenvparam_log','Creator','','en','User','',NULL)");
        b.append(",('countryenvparam_log','Creator','','fr','Utilisateur','',NULL)");
        b.append(",('countryenvparam_log','datecre','','en','Date & Time','',NULL)");
        b.append(",('countryenvparam_log','datecre','','fr','Date & Heure','',NULL)");
        b.append(",('countryenvparam_log','Description','','en','Description','',NULL)");
        b.append(",('countryenvparam_log','Description','','fr','Description','',NULL)");
        b.append(",('dataTable','colVis','','en','Show/Hide columns','',NULL)");
        b.append(",('dataTable','colVis','','fr','Afficher/Cacher les colonnes','',NULL)");
        b.append(",('dataTable','sEmptyTable','','en','No data available in table','',NULL)");
        b.append(",('dataTable','sEmptyTable','','fr','Aucune donn&eacute;e disponible dans le tableau','',NULL)");
        b.append(",('dataTable','sFirst','','en','First','',NULL)");
        b.append(",('dataTable','sFirst','','fr','Premier','',NULL)");
        b.append(",('dataTable','sInfo','','en','Showing _START_ to _END_ of _TOTAL_ entries','',NULL)");
        b.append(",('dataTable','sInfo','','fr','Affichage de l\\'&eacute;l&eacute;ment _START_ &agrave; _END_ sur _TOTAL_ &eacute;l&eacute;ments','',NULL)");
        b.append(",('dataTable','sInfoEmpty','','en','Showing 0 to 0 of 0 entries','',NULL)");
        b.append(",('dataTable','sInfoEmpty','','fr','Affichage de l\\'&eacute;l&eacute;ment 0 &agrave; 0 sur 0 &eacute;l&eacute;ments','',NULL)");
        b.append(",('dataTable','sInfoFiltered','','en','(filtered from _MAX_ total entries)','',NULL)");
        b.append(",('dataTable','sInfoFiltered','','fr','(filtr&eacute; de _MAX_ &eacute;l&eacute;ments au total)','',NULL)");
        b.append(",('dataTable','sInfoPostFix','','en','','',NULL)");
        b.append(",('dataTable','sInfoPostFix','','fr','','',NULL)");
        b.append(",('dataTable','sInfoThousands','','en',',','',NULL)");
        b.append(",('dataTable','sInfoThousands','','fr',',','',NULL)");
        b.append(",('dataTable','sLast','','en','Last','',NULL)");
        b.append(",('dataTable','sLast','','fr','Dernier','',NULL)");
        b.append(",('dataTable','sLengthMenu','','en','_MENU_','',NULL)");
        b.append(",('dataTable','sLengthMenu','','fr','_MENU_','',NULL)");
        b.append(",('dataTable','sLoadingRecords','','en','Loading...','',NULL)");
        b.append(",('dataTable','sLoadingRecords','','fr','Chargement en cours...','',NULL)");
        b.append(",('dataTable','sNext','','en','Next','',NULL)");
        b.append(",('dataTable','sNext','','fr','Suivant','',NULL)");
        b.append(",('dataTable','sPrevious','','en','Previous','',NULL)");
        b.append(",('dataTable','sPrevious','','fr','Pr&eacute;c&eacute;dent','',NULL)");
        b.append(",('dataTable','sProcessing','','en','Processing...','',NULL)");
        b.append(",('dataTable','sProcessing','','fr','Traitement en cours...','',NULL)");
        b.append(",('dataTable','sSearch','','en','_INPUT_','',NULL)");
        b.append(",('dataTable','sSearch','','fr','_INPUT_','',NULL)");
        b.append(",('dataTable','sSearchPlaceholder','','en','Search...','',NULL)");
        b.append(",('dataTable','sSearchPlaceholder','','fr','Rechercher...','',NULL)");
        b.append(",('dataTable','sSortAscending','','en',': activate to sort column ascending','',NULL)");
        b.append(",('dataTable','sSortAscending','','fr',': activer pour trier la colonne par ordre croissant','',NULL)");
        b.append(",('dataTable','sSortDescending','','en',': activate to sort column descending','',NULL)");
        b.append(",('dataTable','sSortDescending','','fr',': activer pour trier la colonne par ordre d&eacute;croissant','',NULL)");
        b.append(",('dataTable','sZeroRecords','','en','No matching records found','',NULL)");
        b.append(",('dataTable','sZeroRecords','','fr','Aucun &eacute;l&eacute;ment &agrave; afficher','',NULL)");
        b.append(",('deploytype','deploytype','','en','Deployment Type','',NULL)");
        b.append(",('deploytype','deploytype','','fr','Type de deploiement','',NULL)");
        b.append(",('deploytype','description','','en','Description','',NULL)");
        b.append(",('deploytype','description','','fr','Description','',NULL)");
        b.append(",('homepage','btn_addTag','','en','Add Tag','',NULL)");
        b.append(",('homepage','btn_addTag','','fr','Ajouter le tag','',NULL)");
        b.append(",('homepage','btn_settings','','en','Customize','',NULL)");
        b.append(",('homepage','btn_settings','','fr','Personnaliser','',NULL)");
        b.append(",('homepage','lastTagExecution','','en','Last Tags Executions','Display the statistics of the last tag executed or the tags set in the settings',NULL)");
        b.append(",('homepage','lastTagExecution','','fr','Dernières exécutions de tags','Affiche les statistiques d\\'exécutions des derniers tags exécutés ou des tags définis dans les options',NULL)");
        b.append(",('homepage','modal_title','','en','Tag Filter','This modal permits you to add tags to the last tags executions report',NULL)");
        b.append(",('homepage','modal_title','','fr','Filtre des tags','Ce modal vous permet d\\'ajouter des tags au rapport des dernières exécutions de tags',NULL)");
        b.append(",('homepage','testCaseStatusByApp','','en','Test Case Status by Application','Workflow status of the test case by Application',NULL)");
        b.append(",('homepage','testCaseStatusByApp','','fr','Statut des Cas de Test par Application','Status de l\\'avancement des cas de tests par Application',NULL)");
        b.append(",('homepage','title','','en','Welcome to Cerberus application','',NULL)");
        b.append(",('homepage','title','','fr','Bienvenue dans l\\'application Cerberus','',NULL)");
        b.append(",('host','active','','en','Active','',NULL)");
        b.append(",('host','host','','en','Host','',NULL)");
        b.append(",('host','port','','en','port','',NULL)");
        b.append(",('host','secure','','en','secure','',NULL)");
        b.append(",('host','secure','','fr','garantie',NULL,NULL)");
        b.append(",('host','Server','','en','Server','This is used to define different host on different server for resilence purpose. You can use that for PRIMARY and BACKUP access.',NULL)");
        b.append(",('host','Server','','fr','Serveur',NULL,NULL)");
        b.append(",('host','Session','','en','Session','',NULL)");
        b.append(",('host','Session','','fr','Session',NULL,NULL)");
        b.append(",('invariant','COUNTRY','','en','Country','A <code class=\\'doc-crbvvoca\\'>country</code> is a declination of a <code class=\\'doc-crbvvoca\\'>system</code> in an <code class=\\'doc-crbvvoca\\'>environment</code> with a specific configuration.<br>This is called <code class=\\'doc-crbvvoca\\'>country</code> because for <code class=\\'doc-crbvvoca\\'>systems</code> that support multiple countries, every <code class=\\'doc-crbvvoca\\'>country</code> is deployed on different <code class=\\'doc-crbvvoca\\'>environments</code>. Each of them can have the same version of the <code class=\\'doc-crbvvoca\\'>application</code> but with different configuration. As a consequence, some <code class=\\'doc-crbvvoca\\'>test case</code> may or may not be relevant on that <code class=\\'doc-crbvvoca\\'>country</code>.',NULL)");
        b.append(",('invariant','COUNTRY','','fr','Pays','Un <code class=\\'doc-crbvvoca\\'>pays</code> est une declinaison d\\'un <code class=\\'doc-crbvvoca\\'>système</code> dans un <code class=\\'doc-crbvvoca\\'>environnement</code> avec une configuration specifique.<br>Ca porte le nom de <code class=\\'doc-crbvvoca\\'>pays</code> car pour les <code class=\\'doc-crbvvoca\\'>systèmes</code> qui supportent plusieurs pays, chaque <code class=\\'doc-crbvvoca\\'>pays</code> est deployé sur un <code class=\\'doc-crbvvoca\\'>environnement</code> different. Chacun d\\'entre eux peut avoir la même version de l\\'<code class=\\'doc-crbvvoca\\'>application</code> mais avec differentes configuration. En conséquence, certain <code class=\\'doc-crbvvoca\\'>cas de test</code> peuvent ou non etre pertinant sur ce <code class=\\'doc-crbvvoca\\'>pays</code>.',NULL)");
        b.append(",('invariant','ENVGP','','en','Environment Group','',NULL)");
        b.append(",('invariant','ENVGP','','fr','Groupe d\\'Environnement','',NULL)");
        b.append(",('invariant','ENVIRONMENT','','en','Environment','',NULL)");
        b.append(",('invariant','ENVIRONMENT','','fr','Environnement','',NULL)");
        b.append(",('invariant','FILTERNBDAYS','','en','Nb Days','Number of days to Filter the history table in the integration status.',NULL)");
        b.append(",('invariant','FILTERNBDAYS','','fr','Nb Jours','',NULL)");
        b.append(",('invariant','GROUP','','en','Group','The group is a property of a <code class=\\'doc-crbvvoca\\'>test case</code> that can take the following values : <br><br><b>AUTOMATED</b> : The <code class=\\'doc-crbvvoca\\'>test case</code> is fully automated and does not require any manual action.<br><b>MANUAL</b> : The <code class=\\'doc-crbvvoca\\'>test case</code> has to be manually executed.<br><b>PRIVATE</b> : The <code class=\\'doc-crbvvoca\\'>test case</code> exist for technical reason and will never appear on the reporting area. For example : <code class=\\'doc-fixed\\'>Pre Testing</code> test cases that are used for login purpose should all be PRIVATE.',NULL)");
        b.append(",('invariant','GROUP','','fr','Groupe','',NULL)");
        b.append(",('invariant','PRIORITY','','en','Priority','It is the priority level of the functionnality which is tested.',NULL)");
        b.append(",('invariant','PRIORITY','','fr','Priorité','',NULL)");
        b.append(",('invariant','SYSTEM','','en','System','',NULL)");
        b.append(",('invariant','SYSTEM','','fr','Système','',NULL)");
        b.append(",('label','color','','en','Color','<p>This field is the color that will be applyied to the label. It could be any value understable by the brower.</p><p>Examples: blue |  #00FF00 | #000 </p>',NULL)");
        b.append(",('label','color','','fr','Couleur','<p>Cette valeur représente la couleur appliquée au label. Cela peut-être toute valeur reconnue comme couleur par le navigateur.</p><p>Exemples: blue |  #00FF00 | #000 </p>',NULL)");
        b.append(",('label','description','','en','Description','<p>Description of the label.</p>',NULL)");
        b.append(",('label','description','','fr','Description','<p>Description du label.</p>',NULL)");
        b.append(",('label','id','','en','ID','',NULL)");
        b.append(",('label','id','','fr','ID','',NULL)");
        b.append(",('label','label','','en','Label','<p>This value is the label attached to testcase for the purpose of identification or to give other information.</p>',NULL)");
        b.append(",('label','label','','fr','Label','<p>Cette valeur représente le label qui sera attaché au testcase à des fins d\\'identification ou de regroupement.</p>',NULL)");
        b.append(",('label','longdesc','','en','Long Description','',NULL)");
        b.append(",('label','longdesc','','fr','Description Longue','',NULL)");
        b.append(",('label','parentid','','en','Parent LabelID','<p>This value represent the labelID of the parent label. This allow to group or create hierachy in label</p>',NULL)");
        b.append(",('label','parentid','','fr','ID du label parent','<p>Cette valeur est l\\'ID du label parent. Cela permet de grouper ou de créer une hiérarchie au sein des labels</p>',NULL)");
        b.append(",('label','reqcriticity','','en','Criticity','',NULL)");
        b.append(",('label','reqcriticity','','fr','Criticité','',NULL)");
        b.append(",('label','reqstatus','','en','Status','',NULL)");
        b.append(",('label','reqstatus','','fr','Status','',NULL)");
        b.append(",('label','reqtype','','en','Requirement Type','',NULL)");
        b.append(",('label','reqtype','','fr','Type d\\'Exigeance','',NULL)");
        b.append(",('label','system','','en','System','',NULL)");
        b.append(",('label','system','','fr','Système','',NULL)");
        b.append(",('label','type','','en','Type','<p>This field is the type of label. Labels can be STICKER, REQUIREMENT and BATTERY.</p><p>STICKER: Appears in testcase list page and also Reporting By Tag page.</p><p>BATTERY: Will only appear in testcase list page. You can use that type of label is you want to group your testcases in a hidden mode.',NULL)");
        b.append(",('label','type','','fr','Type','<p>Correspond au type de Label. Prend une des valeurs : STICKER, REQUIREMENT et BATTERY.</p><p>STICKER: Apparait dans la liste des cas de tests mais aussi dans la page de Reporting By Tag.</p><p>BATTERY: N\\'apparait que dans la liste des cas des tests. Ce type est à utiliser pour grouper des cas de t',NULL)");
        b.append(",('logevent','action','','en','Action','Type of the action performed',NULL)");
        b.append(",('logevent','action','','fr','Action','Type de l\\'action effetué',NULL)");
        b.append(",('logevent','localip','','en','Local IP','IP of the Cerberus server that provided the service.',NULL)");
        b.append(",('logevent','localip','','fr','IP Locale','IP du serveur Cerberus qui a répondu à la requette.',NULL)");
        b.append(",('logevent','log','','en','Log','Log message of the action.',NULL)");
        b.append(",('logevent','log','','fr','Message','Message lié à l\\'action.',NULL)");
        b.append(",('logevent','logeventid','','en','Log Entry ID','Unique identifier of the log entry.',NULL)");
        b.append(",('logevent','logeventid','','fr','Identifiant du log','Identifiant unique de l\\'entrée de log.',NULL)");
        b.append(",('logevent','login','','en','Login','Login of the user who performed the action.',NULL)");
        b.append(",('logevent','login','','fr','Utilisateur','Nom de l\\'utilisateur qui a effectué l\\'action.',NULL)");
        b.append(",('logevent','page','','en','Page','Name of the page where the action was performed.',NULL)");
        b.append(",('logevent','page','','fr','Page','Nom de la page où l\\'action a été effectuée.',NULL)");
        b.append(",('logevent','remoteip','','en','Remote IP','IP from which the user connected to Cerberus to perform the action.',NULL)");
        b.append(",('logevent','remoteip','','fr','IP Distante','IP à partir de laquelle l\\'utilisateur s\\'est connecté à Cerberus.',NULL)");
        b.append(",('logevent','time','','en','Time','Timestamp of the log message.',NULL)");
        b.append(",('logevent','time','','fr','Date','Date à laquelle l\\'action a été effectuée.',NULL)");
        b.append(",('modal_upload','btn_cancel','','en','Cancel','',NULL)");
        b.append(",('modal_upload','btn_cancel','','fr','Annuler','',NULL)");
        b.append(",('modal_upload','btn_choose','','en','Choose File','',NULL)");
        b.append(",('modal_upload','btn_choose','','fr','Choisir','',NULL)");
        b.append(",('modal_upload','btn_upload','','en','Upload','',NULL)");
        b.append(",('modal_upload','btn_upload','','fr','Uploader','',NULL)");
        b.append(",('modal_upload','title','','en','Upload File','',NULL)");
        b.append(",('modal_upload','title','','fr','Uploader Fichier','',NULL)");
        b.append(",('multiselect','all_selected','','en','All selected','',NULL)");
        b.append(",('multiselect','all_selected','','fr','Tous selectionner','',NULL)");
        b.append(",('multiselect','none_selected','','en','None selected','',NULL)");
        b.append(",('multiselect','none_selected','','fr','Selectionner aucun','',NULL)");
        b.append(",('multiselect','nselected','','en','selected','',NULL)");
        b.append(",('multiselect','nselected','','fr','Selectionné','',NULL)");
        b.append(",('multiselect','search','','en','Search','',NULL)");
        b.append(",('multiselect','search','','fr','Chercher','',NULL)");
        b.append(",('multiselect','select_all','','en','Select all','',NULL)");
        b.append(",('multiselect','select_all','','fr','Selectionner tous','',NULL)");
        b.append(",('myversion','key','','en','Key','This is the reference of the component inside Cerberus that we want to keep track of the version.',NULL)");
        b.append(",('myversion','key','','fr','Clé',NULL,NULL)");
        b.append(",('myversion','value','','en','Value','This is the version that correspond to the key.',NULL)");
        b.append(",('page_application','button_create','','en','Create new Application','',NULL)");
        b.append(",('page_application','button_create','','fr','Créer une nouvelle Application','',NULL)");
        b.append(",('page_application','button_delete','','en','Delete Application','',NULL)");
        b.append(",('page_application','button_delete','','fr','Supprimer l\\'Application','',NULL)");
        b.append(",('page_application','button_edit','','en','Edit Application','',NULL)");
        b.append(",('page_application','button_edit','','fr','Modifier l\\'Application','',NULL)");
        b.append(",('page_application','button_manage','','en','Manage Objects','',NULL)");
        b.append(",('page_application','button_manage','','fr','Gerer les Objets','',NULL)");
        b.append(",('page_application','message_delete','','en','Do you want to delete application <b>\\'%ENTRY%\\'</b> ?<br>WARNING : All corresponding Test Cases will be removed as well !!!','',NULL)");
        b.append(",('page_application','message_delete','','fr','Confirmez vous la suppression de l\\'application <b>\\'%ENTRY%\\'</b> ?<br> ATTENTION : Tous les Cas de Test associés seront également supprimés !!!','',NULL)");
        b.append(",('page_application','tabDef','','en','Definition','',NULL)");
        b.append(",('page_application','tabDef','','fr','Definition','',NULL)");
        b.append(",('page_application','tabEnv','','en','Environments','',NULL)");
        b.append(",('page_application','tabEnv','','fr','Environnements','',NULL)");
        b.append(",('page_application','table_application','','en','Application List',NULL,NULL)");
        b.append(",('page_application','table_application','','fr','Liste des Applications',NULL,NULL)");
        b.append(",('page_application','title','','en','APPLICATION','This page can be used to manage the applications.','_application')");
        b.append(",('page_application','title','','fr','APPLICATION','Cette page permet de gérer et créer des applications.','_application')");
        b.append(",('page_applicationObject','Application','','en','Application','',NULL)");
        b.append(",('page_applicationObject','Application','','fr','Application','',NULL)");
        b.append(",('page_applicationObject','applicationfield','','en','Application','',NULL)");
        b.append(",('page_applicationObject','applicationfield','','fr','Application','',NULL)");
        b.append(",('page_applicationObject','button_add','','en','Add','',NULL)");
        b.append(",('page_applicationObject','button_add','','fr','Ajouter','',NULL)");
        b.append(",('page_applicationObject','button_close','','en','Close','',NULL)");
        b.append(",('page_applicationObject','button_close','','fr','Fermer','',NULL)");
        b.append(",('page_applicationObject','button_create','','en','Create an Application Object','',NULL)");
        b.append(",('page_applicationObject','button_create','','fr','Créer un objet d\\'application','',NULL)");
        b.append(",('page_applicationObject','button_delete','','en','Delete Object','',NULL)");
        b.append(",('page_applicationObject','button_delete','','fr','Supprimer l\\'objet','',NULL)");
        b.append(",('page_applicationObject','button_edit','','en','Edit Object','',NULL)");
        b.append(",('page_applicationObject','button_edit','','fr','Modifier l\\'objet','',NULL)");
        b.append(",('page_applicationObject','createapplicationobjectfield','','en','Create Application Object','',NULL)");
        b.append(",('page_applicationObject','createapplicationobjectfield','','fr','Créer un object d\\'application','',NULL)");
        b.append(",('page_applicationObject','DateCreated','','en','Creation date','',NULL)");
        b.append(",('page_applicationObject','DateCreated','','fr','Date de création','',NULL)");
        b.append(",('page_applicationObject','DateModif','','en','Last modification date','',NULL)");
        b.append(",('page_applicationObject','DateModif','','fr','Date de dernière modification','',NULL)");
        b.append(",('page_applicationObject','editapplicationobjectfield','','en','Edit Application Object','',NULL)");
        b.append(",('page_applicationObject','editapplicationobjectfield','','fr','Modifier un objet d\\'application','',NULL)");
        b.append(",('page_applicationObject','message_delete','','en','Do you want to delete Application Object <b>\\'%ENTRY%\\'</b> ?','',NULL)");
        b.append(",('page_applicationObject','message_delete','','fr','Confirmez vous la suppression de l\\'objet <b>\\'%ENTRY%\\'</b> ?','',NULL)");
        b.append(",('page_applicationObject','Object','','en','Object','',NULL)");
        b.append(",('page_applicationObject','Object','','fr','Objet','',NULL)");
        b.append(",('page_applicationObject','objectfield','','en','Object','',NULL)");
        b.append(",('page_applicationObject','objectfield','','fr','Objet','',NULL)");
        b.append(",('page_applicationObject','ScreenshotFileName','','en','File Name','',NULL)");
        b.append(",('page_applicationObject','ScreenshotFileName','','fr','Nom du fichier','',NULL)");
        b.append(",('page_applicationObject','screenshotfilenamefield','','en','FileName','',NULL)");
        b.append(",('page_applicationObject','screenshotfilenamefield','','fr','Nom du ficher','',NULL)");
        b.append(",('page_applicationObject','table_applicationobject','','en','Application Object List',NULL,NULL)");
        b.append(",('page_applicationObject','table_applicationobject','','fr','Liste des Objets d\\'Application',NULL,NULL)");
        b.append(",('page_applicationObject','title','','en','Application Object','This page can be used to manage the application objects.','_application_object')");
        b.append(",('page_applicationObject','title','','fr','Objet d\\'application','Cette page permet de gérer et créer des objets d\\'application.','_objet_d_application')");
        b.append(",('page_applicationObject','UsrCreated','','en','Creator','',NULL)");
        b.append(",('page_applicationObject','UsrCreated','','fr','Createur','',NULL)");
        b.append(",('page_applicationObject','UsrModif','','en','Last Modificator','',NULL)");
        b.append(",('page_applicationObject','UsrModif','','fr','Dernier Editeur','',NULL)");
        b.append(",('page_applicationObject','Value','','en','Value','',NULL)");
        b.append(",('page_applicationObject','Value','','fr','Valeur','',NULL)");
        b.append(",('page_applicationObject','valuefield','','en','Value','',NULL)");
        b.append(",('page_applicationObject','valuefield','','fr','Valeur','',NULL)");
        b.append(",('page_appservice','addSoapLibrary_field','','en','Add Service','',NULL)");
        b.append(",('page_appservice','addSoapLibrary_field','','fr','Ajouter un Service','',NULL)");
        b.append(",('page_appservice','allSoapLibrarys','','en','Service Library','',NULL)");
        b.append(",('page_appservice','allSoapLibrarys','','fr','Bibliotheque de Service','',NULL)");
        b.append(",('page_appservice','button_col','','en','Actions','',NULL)");
        b.append(",('page_appservice','button_col','','fr','Actions','',NULL)");
        b.append(",('page_appservice','button_create','','en','Add Service','',NULL)");
        b.append(",('page_appservice','button_create','','fr','Ajouter un Service','',NULL)");
        b.append(",('page_appservice','button_duplicate','','en','Duplicate Service',NULL,NULL)");
        b.append(",('page_appservice','button_duplicate','','fr','Dupliquer Service',NULL,NULL)");
        b.append(",('page_appservice','button_edit','','en','Edit Service','',NULL)");
        b.append(",('page_appservice','button_edit','','fr','Editer le Service','',NULL)");
        b.append(",('page_appservice','button_remove','','en','Delete Service','',NULL)");
        b.append(",('page_appservice','button_remove','','fr','Supprimer le Service','',NULL)");
        b.append(",('page_appservice','close_btn','','en','Close','',NULL)");
        b.append(",('page_appservice','close_btn','','fr','Fermer','',NULL)");
        b.append(",('page_appservice','editSoapLibrary_field','','en','Edit Service','',NULL)");
        b.append(",('page_appservice','editSoapLibrary_field','','fr','Editer le Service','',NULL)");
        b.append(",('page_appservice','message_remove','','en','Are you sure to delete the service \\'%SERVICE%\\' ?','',NULL)");
        b.append(",('page_appservice','message_remove','','fr','Êtes-vous sûrs de supprimer le service \\'%SERVICE%\\' ?','',NULL)");
        b.append(",('page_appservice','save_btn','','en','Save Service','',NULL)");
        b.append(",('page_appservice','save_btn','','fr','Sauvegarder le Service','',NULL)");
        b.append(",('page_appservice','table_service','','en','Service',NULL,NULL)");
        b.append(",('page_appservice','table_service','','fr','Service',NULL,NULL)");
        b.append(",('page_appservice','title_remove','','en','Delete Service','',NULL)");
        b.append(",('page_appservice','title_remove','','fr','Supprimer le Service','',NULL)");
        b.append(",('page_batchinvariant','button_create','','en','Create new Batch','',NULL)");
        b.append(",('page_batchinvariant','button_create','','fr','Créer un nouveau Batch','',NULL)");
        b.append(",('page_batchinvariant','button_delete','','en','Delete Batch','',NULL)");
        b.append(",('page_batchinvariant','button_delete','','fr','Supprimer le Batch','',NULL)");
        b.append(",('page_batchinvariant','button_edit','','en','Edit Batch','',NULL)");
        b.append(",('page_batchinvariant','button_edit','','fr','Modifier le Batch','',NULL)");
        b.append(",('page_batchinvariant','message_delete','','en','Do you want to delete Batch <b>\\'%ENTRY%\\'</b> ?<br>WARNING1 : All corresponding Batch execution history will be deleted !!!','',NULL)");
        b.append(",('page_batchinvariant','message_delete','','fr','Confirmez vous la suppression du Batch <b>\\'%ENTRY%\\'</b> ?<br> ATTENTION1 : Tous les Historiques d\\'executions seront suprimés !!!','',NULL)");
        b.append(",('page_batchinvariant','table_batch','','en','Batch',NULL,NULL)");
        b.append(",('page_batchinvariant','table_batch','','fr','Liste des Batchs',NULL,NULL)");
        b.append(",('page_batchinvariant','title','','en','BATCH','This page can be used in order to manage the batch per system.','_batch')");
        b.append(",('page_batchinvariant','title','','fr','BATCH','Cette page permet de gérer et créer des batch pour chaque systeme.','_chaine')");
        b.append(",('page_buildcontent','buildFrom','','en','From Build/Rev','',NULL)");
        b.append(",('page_buildcontent','buildFrom','','fr','De Build/Rev','',NULL)");
        b.append(",('page_buildcontent','buildTo','','en','To Build/Rev','',NULL)");
        b.append(",('page_buildcontent','buildTo','','fr','Vers Build/Rev','',NULL)");
        b.append(",('page_buildcontent','buttonInstallInstruction','','en','See Installation Instructions','',NULL)");
        b.append(",('page_buildcontent','buttonInstallInstruction','','fr','Voir Instructions d\\'installation','',NULL)");
        b.append(",('page_buildcontent','buttonLoadAll','','en','Load All Build','',NULL)");
        b.append(",('page_buildcontent','buttonLoadAll','','fr','Charger tous','',NULL)");
        b.append(",('page_buildcontent','buttonLoadLatest','','en','Load latest Build','',NULL)");
        b.append(",('page_buildcontent','buttonLoadLatest','','fr','Charger le dernier Build','',NULL)");
        b.append(",('page_buildcontent','buttonLoadPending','','en','Load pending Build','',NULL)");
        b.append(",('page_buildcontent','buttonLoadPending','','fr','Charger le Build en cours','',NULL)");
        b.append(",('page_buildcontent','button_create','','en','Create new Build Content','',NULL)");
        b.append(",('page_buildcontent','button_create','','fr','Créer un nouveau contenu au Build','',NULL)");
        b.append(",('page_buildcontent','button_delete','','en','Delete Build Content','',NULL)");
        b.append(",('page_buildcontent','button_delete','','fr','Supprimer l\\'entrée du contenu du Build','',NULL)");
        b.append(",('page_buildcontent','button_edit','','en','Edit Build Content','',NULL)");
        b.append(",('page_buildcontent','button_edit','','fr','Editer le contenu du Build','',NULL)");
        b.append(",('page_buildcontent','delete','','en','Dlt','Select this checkbox and then save changes in order to delete the row.',NULL)");
        b.append(",('page_buildcontent','delete','','fr','Dlt','Select this checkbox and then save changes in order to delete the row.',NULL)");
        b.append(",('page_buildcontent','filters','','en','Filters','',NULL)");
        b.append(",('page_buildcontent','filters','','fr','Filtres','',NULL)");
        b.append(",('page_buildcontent','InstallInstructions','','en','Installation instructions','',NULL)");
        b.append(",('page_buildcontent','InstallInstructions','','fr','Instructions d\\'installation','',NULL)");
        b.append(",('page_buildcontent','list','','en','Build Content List','',NULL)");
        b.append(",('page_buildcontent','list','','fr','Liste du contenu du Build','',NULL)");
        b.append(",('page_buildcontent','massAction','','en','Massively update the selected release','',NULL)");
        b.append(",('page_buildcontent','massAction','','fr','Mise à jour massive des releases selectionnées','',NULL)");
        b.append(",('page_buildcontent','message_delete','','en','Do you want to delete release entry <b>\\'%ENTRY%\\'</b> ?<br> NB : It correspond to the release <b>\\'%RELEASE%\\'</b> of application <b>\\'%APPLI%\\'</b> of Build <b>\\'%BUILD%\\'</b> Revision <b>\\'%REVISION%\\'</b>.','',NULL)");
        b.append(",('page_buildcontent','message_delete','','fr','Confirmez vous la suppression de l\\'entrée <b>\\'%ENTRY%\\'</b> ?<br> NB : correspond à la release <b>\\'%RELEASE%\\'</b> de l\\'application <b>\\'%APPLI%\\'</b> du Build <b>\\'%BUILD%\\'</b> Revision <b>\\'%REVISION%\\'</b>.','',NULL)");
        b.append(",('page_buildcontent','message_instruction','','en','Please specify a build and a revision to get the installation instructions!',NULL,NULL)");
        b.append(",('page_buildcontent','message_instruction','','fr','Merci de specifier un build et une revision avant d\\'obtenir les instructions d\\'installation! ',NULL,NULL)");
        b.append(",('page_buildcontent','message_massAction','','en','Massively update the selected release',NULL,NULL)");
        b.append(",('page_buildcontent','message_massAction','','fr','Mettre à jours en masse les releases selectionnées',NULL,NULL)");
        b.append(",('page_buildcontent','standardfilters','','en','Standard Filters','',NULL)");
        b.append(",('page_buildcontent','standardfilters','','fr','Standard Filters','',NULL)");
        b.append(",('page_buildcontent','title','','en','BUILD CONTENT','This page can be used to manage the Build Content.','_build_content')");
        b.append(",('page_buildcontent','title','','fr','CONTENU DES BUILDS','Cette page permet de gérer le contenu d\\'un build.','_contenu_des_builds')");
        b.append(",('page_buildrevdefinition','button_create','','en','Create new Build Revision Definition','',NULL)");
        b.append(",('page_buildrevdefinition','button_create','','fr','Créer une nouvelle definition de build ou revision','',NULL)");
        b.append(",('page_buildrevdefinition','button_delete','','en','Delete Build Revision Definition','',NULL)");
        b.append(",('page_buildrevdefinition','button_delete','','fr','Supprimer la definition de build ou revision','',NULL)");
        b.append(",('page_buildrevdefinition','button_edit','','en','Edit Build Revision Definition','',NULL)");
        b.append(",('page_buildrevdefinition','button_edit','','fr','Editer une nouvelle definition de build ou revision','',NULL)");
        b.append(",('page_buildrevdefinition','table_BuildRevision','','en','Build Revision List',NULL,NULL)");
        b.append(",('page_buildrevdefinition','table_BuildRevision','','fr','Liste des Builds de Révision',NULL,NULL)");
        b.append(",('page_buildrevdefinition','title','','en','BUILD REVISION DEFINITION','This page can be used to manage the definition of Builds and Revisions.','_build_revision')");
        b.append(",('page_buildrevdefinition','title','','fr','DEFINITION DES BUILDS REVISIONS','Cette page permet de gérer la definition d\\'un build.','_build_revision')");
        b.append(",('page_databasemaintenance','messageStatus','','en','Database is now uptodate. Enjoy the tool.',NULL,NULL)");
        b.append(",('page_databasemaintenance','messageStatus','','fr','La Base de Données est à jour',NULL,NULL)");
        b.append(",('page_databasemaintenance','SQLdisplay','','en','Show all SQL',NULL,NULL)");
        b.append(",('page_databasemaintenance','SQLdisplay','','fr','Afficher tous les SQL',NULL,NULL)");
        b.append(",('page_databasemaintenance','SQLdisplay_link','','en','here',NULL,NULL)");
        b.append(",('page_databasemaintenance','SQLdisplay_link','','fr','ici',NULL,NULL)");
        b.append(",('page_deploytype','button_create','','en','Create new Deployment Type','',NULL)");
        b.append(",('page_deploytype','button_create','','fr','Créer un nouveau Type de Deploiement','',NULL)");
        b.append(",('page_deploytype','button_delete','','en','Delete Deployment Type','',NULL)");
        b.append(",('page_deploytype','button_delete','','fr','Supprimer le Type de Deploiement','',NULL)");
        b.append(",('page_deploytype','button_edit','','en','Edit Deployment Type','',NULL)");
        b.append(",('page_deploytype','button_edit','','fr','Modifier le Type de Deploiement','',NULL)");
        b.append(",('page_deploytype','message_delete','','en','Do you want to delete Deploy Type <b>\\'%ENTRY%\\'</b> ?<br>WARNING1 : All corresponding Application will lose the link to that deleted Deploy Type !!!<br>WARNING2 : All associated Jenkins Agent on corresponding Environments will be deleted !!!','',NULL)");
        b.append(",('page_deploytype','message_delete','','fr','Confirmez vous la suppression du Type de Deploiement <b>\\'%ENTRY%\\'</b> ?<br> ATTENTION1 : Toutes les Applications associées vont perdre le lien avec ce Deploy Type !!!<br>ATTENTION2 : Tous les Jenkins Agent associés et Environnements associés seront supprimés !!!','',NULL)");
        b.append(",('page_deploytype','table_deploytype','','en','Deploy Type List',NULL,NULL)");
        b.append(",('page_deploytype','table_deploytype','','fr','Type de Déploiement',NULL,NULL)");
        b.append(",('page_deploytype','title','','en','DEPLOYMENT TYPE','This page can be used in order to manage the deployment types.','_deploy_type')");
        b.append(",('page_deploytype','title','','fr','TYPE DE DEPLOIEMENT','Cette page permet de gérer et créer des types de deploiements.','_type_de_déploiement')");
        b.append(",('page_environment','buttonPreviewNotification','','en','Preview Notification','',NULL)");
        b.append(",('page_environment','buttonPreviewNotification','','fr','Prévisualiser la notification','',NULL)");
        b.append(",('page_environment','button_create','','en','Create a new Environment','',NULL)");
        b.append(",('page_environment','button_create','','fr','Créer un nouvel Environnement','',NULL)");
        b.append(",('page_environment','button_delete','','en','Delete Environment','',NULL)");
        b.append(",('page_environment','button_delete','','fr','Supprimer l\\'Environnement','',NULL)");
        b.append(",('page_environment','button_disable','','en','Disable Environment','',NULL)");
        b.append(",('page_environment','button_disable','','fr','Désactiver l\\'Environnement','',NULL)");
        b.append(",('page_environment','button_disable1','','en','Disable and Send Notification','',NULL)");
        b.append(",('page_environment','button_disable1','','fr','Désactiver et envoyer la Notification','',NULL)");
        b.append(",('page_environment','button_edit','','en','Edit Environment','',NULL)");
        b.append(",('page_environment','button_edit','','fr','Modifier l\\'Environnement','',NULL)");
        b.append(",('page_environment','button_enable','','en','Enable Environment with new Build Revision','',NULL)");
        b.append(",('page_environment','button_enable','','fr','Activer l\\'Environnement avec un nouveau Build et Revision','',NULL)");
        b.append(",('page_environment','button_enable1','','en','Enable and Sent Notification','',NULL)");
        b.append(",('page_environment','button_enable1','','fr','Activer et Envoyer la Notification','',NULL)");
        b.append(",('page_environment','button_newChain','','en','New Event Chain','',NULL)");
        b.append(",('page_environment','button_newChain','','fr','Nouvel Evenement de Chaine','',NULL)");
        b.append(",('page_environment','button_newChain1','','en','Create New Chain Event and Send Notification','',NULL)");
        b.append(",('page_environment','button_newChain1','','fr','Créer un nouvel événement de Chaine et Envoyer la Notification','',NULL)");
        b.append(",('page_environment','button_view','','en','View Environment','',NULL)");
        b.append(",('page_environment','button_view','','fr','Voir Environnement','',NULL)");
        b.append(",('page_environment','cc','','en','CC','',NULL)");
        b.append(",('page_environment','cc','','fr','Copie','',NULL)");
        b.append(",('page_environment','currentBuild','','en','Current Build','',NULL)");
        b.append(",('page_environment','currentBuild','','fr','Build courant','',NULL)");
        b.append(",('page_environment','currentRevision','','en','Current Revision','',NULL)");
        b.append(",('page_environment','currentRevision','','fr','Revision courante','',NULL)");
        b.append(",('page_environment','list','','en','Environment list','',NULL)");
        b.append(",('page_environment','list','','fr','Liste des environnements','',NULL)");
        b.append(",('page_environment','listChange','','en','Change list','',NULL)");
        b.append(",('page_environment','listChange','','fr','Liste des changements','',NULL)");
        b.append(",('page_environment','listEvent','','en','Batch list','',NULL)");
        b.append(",('page_environment','listEvent','','fr','Liste des batchs','',NULL)");
        b.append(",('page_environment','message_delete','','en','Do you want to delete environment <b>\\'%ENVIRONMENT%\\'</b> from country <b>\\'%COUNTRY%\\'</b> and system <b>\\'%SYSTEM%\\'</b> ?<br>WARNING : All corresponding parameters such as list of applications, databases and other environments dependencies will be removed !!!','',NULL)");
        b.append(",('page_environment','message_delete','','fr','Confirmez vous la suppression de l\\'environnement <b>\\'%ENVIRONMENT%\\'</b> du pays <b>\\'%COUNTRY%\\'</b> du système <b>\\'%SYSTEM%\\'</b> ?<br> ATTENTION : Tous les parametres associées tel que la liste des applications, database et autres dependances d\\'environnements seront supprimés !!!','',NULL)");
        b.append(",('page_environment','newBuild','','en','New Build','',NULL)");
        b.append(",('page_environment','newBuild','','fr','Nouveau Build','',NULL)");
        b.append(",('page_environment','newRevision','','en','New Revision','',NULL)");
        b.append(",('page_environment','newRevision','','fr','Nouvelle Revision','',NULL)");
        b.append(",('page_environment','subject','','en','Subject','',NULL)");
        b.append(",('page_environment','subject','','fr','Sujet','',NULL)");
        b.append(",('page_environment','tabApplication','','en','Applications','',NULL)");
        b.append(",('page_environment','tabApplication','','fr','Applications','',NULL)");
        b.append(",('page_environment','tabBuild','','en','Build/Revision','',NULL)");
        b.append(",('page_environment','tabBuild','','fr','Build/Revision','',NULL)");
        b.append(",('page_environment','tabChain','','en','Chain','',NULL)");
        b.append(",('page_environment','tabChain','','fr','Chain','',NULL)");
        b.append(",('page_environment','tabDatabase','','en','Databases','',NULL)");
        b.append(",('page_environment','tabDatabase','','fr','Bases de Données','',NULL)");
        b.append(",('page_environment','tabDefinition','','en','Definition','',NULL)");
        b.append(",('page_environment','tabDefinition','','fr','Definition','',NULL)");
        b.append(",('page_environment','tabDependencies','','en','Dependencies','',NULL)");
        b.append(",('page_environment','tabDependencies','','fr','Dépendances','',NULL)");
        b.append(",('page_environment','tabDeploy','','en','Deploy Types','',NULL)");
        b.append(",('page_environment','tabDeploy','','fr','Type de déploiements','',NULL)");
        b.append(",('page_environment','tabInstallInstruction','','en','Installation Instructions','',NULL)");
        b.append(",('page_environment','tabInstallInstruction','','fr','Instruction d\\'installation','',NULL)");
        b.append(",('page_environment','tabNotif','','en','Specific Notifications','',NULL)");
        b.append(",('page_environment','tabNotif','','fr','Notifications Spécifiques','',NULL)");
        b.append(",('page_environment','tabPreview','','en','EMail Preview','',NULL)");
        b.append(",('page_environment','tabPreview','','fr','Previsu de l\\'Email','',NULL)");
        b.append(",('page_environment','title','','en','ENVIRONMENT','This page can be used to manage the environments.','_environment')");
        b.append(",('page_environment','title','','fr','ENVIRONNEMENT','Cette page permet de gérer et créer des environnements.','_environnement')");
        b.append(",('page_environment','to','','en','To','',NULL)");
        b.append(",('page_environment','to','','fr','Destinataire','',NULL)");
        b.append(",('page_executiondetail','action','','en','Action','',NULL)");
        b.append(",('page_executiondetail','action','','fr','Action','',NULL)");
        b.append(",('page_executiondetail','application','','en','Application','',NULL)");
        b.append(",('page_executiondetail','application','','fr','Application','',NULL)");
        b.append(",('page_executiondetail','browser','','en','Browser','',NULL)");
        b.append(",('page_executiondetail','browser','','fr','Navigateur','',NULL)");
        b.append(",('page_executiondetail','browserfull','','en','Browser full version','',NULL)");
        b.append(",('page_executiondetail','browserfull','','fr','Navigateur version complete','',NULL)");
        b.append(",('page_executiondetail','build','','en','Build','',NULL)");
        b.append(",('page_executiondetail','build','','fr','Build','',NULL)");
        b.append(",('page_executiondetail','buildrevision','','en','BuildRev','Build and Revision of the <code class=\\'doc-crbvvoca\\'>environment</code> of the <code class=\\'doc-crbvvoca\\'>system</code> of the <code class=\\'doc-crbvvoca\\'>application</code> that has been tested.',NULL)");
        b.append(",('page_executiondetail','buildrevision','','fr','BuildRev','',NULL)");
        b.append(",('page_executiondetail','buildrevisionlink','','en','BuildRev Linked','Build and Revision of the <code class=\\'doc-crbvvoca\\'>environment</code> of the linked <code class=\\'doc-crbvvoca\\'>system</code>. The linked systems are defined in the \\'Environment Dependancy\\' section of the <code class=\\'doc-crbvvoca\\'>environment</code> page.',NULL)");
        b.append(",('page_executiondetail','buildrevisionlink','','fr','BuildRev liée','',NULL)");
        b.append(",('page_executiondetail','cerberusversion','','en','Cerberus Version','',NULL)");
        b.append(",('page_executiondetail','cerberusversion','','fr','Version de Cerberus','',NULL)");
        b.append(",('page_executiondetail','conditionOper','','en','Condition Operator','',NULL)");
        b.append(",('page_executiondetail','conditionOper','','fr','Condition Opérateur','',NULL)");
        b.append(",('page_executiondetail','conditionVal1','','en','Condition Value1','',NULL)");
        b.append(",('page_executiondetail','conditionVal1','','fr','Condition Value1','',NULL)");
        b.append(",('page_executiondetail','conditionVal1Init','','en','Condition Value1 Initial','',NULL)");
        b.append(",('page_executiondetail','conditionVal1Init','','fr','Condition Value1 Initiale','',NULL)");
        b.append(",('page_executiondetail','conditionVal2','','en','Condition Value2','',NULL)");
        b.append(",('page_executiondetail','conditionVal2','','fr','Condition Value2','',NULL)");
        b.append(",('page_executiondetail','conditionVal2Init','','en','Condition Value2 Initial','',NULL)");
        b.append(",('page_executiondetail','conditionVal2Init','','fr','Condition Value2 Initiale','',NULL)");
        b.append(",('page_executiondetail','controlmessage','','en','Control Message','',NULL)");
        b.append(",('page_executiondetail','controlmessage','','fr','Message du control','',NULL)");
        b.append(",('page_executiondetail','controlstatus','','en','Control Status','',NULL)");
        b.append(",('page_executiondetail','controlstatus','','fr','Statut','',NULL)");
        b.append(",('page_executiondetail','control_type','','en','Control Type','',NULL)");
        b.append(",('page_executiondetail','control_type','','fr','Type de Control','',NULL)");
        b.append(",('page_executiondetail','country','','en','Country','',NULL)");
        b.append(",('page_executiondetail','country','','fr','Pays','',NULL)");
        b.append(",('page_executiondetail','description','','en','Description','',NULL)");
        b.append(",('page_executiondetail','description','','fr','Description','',NULL)");
        b.append(",('page_executiondetail','edittc','','en','Edit Test Case','',NULL)");
        b.append(",('page_executiondetail','edittc','','fr','Editer le TestCase','',NULL)");
        b.append(",('page_executiondetail','edittch','','en','Edit TestCase Header','',NULL)");
        b.append(",('page_executiondetail','edittch','','fr','Modifier l\\'Entete du TestCase','',NULL)");
        b.append(",('page_executiondetail','edittcstep','','en','Edit Test Case (From Current Step)','',NULL)");
        b.append(",('page_executiondetail','edittcstep','','fr','Editer le TestCase (à partir de l\\'Etape Courante)','',NULL)");
        b.append(",('page_executiondetail','end','','en','End','',NULL)");
        b.append(",('page_executiondetail','end','','fr','Fin','',NULL)");
        b.append(",('page_executiondetail','environment','','en','Environment','',NULL)");
        b.append(",('page_executiondetail','environment','','fr','Environnement','',NULL)");
        b.append(",('page_executiondetail','executor','','en','Executor','',NULL)");
        b.append(",('page_executiondetail','executor','','fr','Executeur','',NULL)");
        b.append(",('page_executiondetail','fatal','','en','Fatal','',NULL)");
        b.append(",('page_executiondetail','fatal','','fr','Fatal','',NULL)");
        b.append(",('page_executiondetail','finished','','en','Finished','',NULL)");
        b.append(",('page_executiondetail','finished','','fr','Fini','',NULL)");
        b.append(",('page_executiondetail','forceexec','','en','Force Execution','',NULL)");
        b.append(",('page_executiondetail','forceexec','','fr','Forcer L\\'Execution','',NULL)");
        b.append(",('page_executiondetail','goto','','en','Go To','',NULL)");
        b.append(",('page_executiondetail','goto','','fr','Aller','',NULL)");
        b.append(",('page_executiondetail','id','','en','ID','',NULL)");
        b.append(",('page_executiondetail','id','','fr','ID','',NULL)");
        b.append(",('page_executiondetail','ip','','en','IP','',NULL)");
        b.append(",('page_executiondetail','ip','','fr','IP','',NULL)");
        b.append(",('page_executiondetail','lastexecution','','en','Executions List','',NULL)");
        b.append(",('page_executiondetail','lastexecution','','fr','Liste d\\'Executions','',NULL)");
        b.append(",('page_executiondetail','lastexecutionwithenvcountry','','en','Executions List (same Environment & Country)','',NULL)");
        b.append(",('page_executiondetail','lastexecutionwithenvcountry','','fr','Liste d\\'Executions (même Environnement & Pays)','',NULL)");
        b.append(",('page_executiondetail','more_detail','','en','More details','',NULL)");
        b.append(",('page_executiondetail','more_detail','','fr','Plus de détails','',NULL)");
        b.append(",('page_executiondetail','platform','','en','Platform','',NULL)");
        b.append(",('page_executiondetail','platform','','fr','Platforme','',NULL)");
        b.append(",('page_executiondetail','port','','en','Port','',NULL)");
        b.append(",('page_executiondetail','port','','fr','Port','',NULL)");
        b.append(",('page_executiondetail','queueId','','en','Execution Queue ID',NULL,NULL)");
        b.append(",('page_executiondetail','queueId','','fr','ID de la queue',NULL,NULL)");
        b.append(",('page_executiondetail','reruntc','','en','ReRun TestCase','',NULL)");
        b.append(",('page_executiondetail','reruntc','','fr','ReExecuter le TestCase','',NULL)");
        b.append(",('page_executiondetail','reruntcqueue','','en','ReRun From Queue','',NULL)");
        b.append(",('page_executiondetail','reruntcqueue','','fr','ReExecuter de la Queue','',NULL)");
        b.append(",('page_executiondetail','return_code','','en','Return Code','',NULL)");
        b.append(",('page_executiondetail','return_code','','fr','Code de retour','',NULL)");
        b.append(",('page_executiondetail','return_message','','en','Return Message','',NULL)");
        b.append(",('page_executiondetail','return_message','','fr','Message de retour','',NULL)");
        b.append(",('page_executiondetail','revision','','en','Revision','',NULL)");
        b.append(",('page_executiondetail','revision','','fr','Revision','',NULL)");
        b.append(",('page_executiondetail','run','','en','Run','',NULL)");
        b.append(",('page_executiondetail','run','','fr','Executer','',NULL)");
        b.append(",('page_executiondetail','runtc','','en','Run TestCase','',NULL)");
        b.append(",('page_executiondetail','runtc','','fr','Executer TestCase','',NULL)");
        b.append(",('page_executiondetail','save','','en','Save Execution','',NULL)");
        b.append(",('page_executiondetail','save','','fr','Sauvegarder Execution','',NULL)");
        b.append(",('page_executiondetail','screensize','','en','Screen Size','',NULL)");
        b.append(",('page_executiondetail','screensize','','fr','Taille de l\\'ecran','',NULL)");
        b.append(",('page_executiondetail','see_executionq','','en','Queue Parameters','',NULL)");
        b.append(",('page_executiondetail','see_executionq','','fr','Parametre de la Queue','',NULL)");
        b.append(",('page_executiondetail','see_executionq_tag','','en','Queue List by Tag','',NULL)");
        b.append(",('page_executiondetail','see_executionq_tag','','fr','Liste des Queues par Tag','',NULL)");
        b.append(",('page_executiondetail','see_execution_tag','','en','Report By Tag','',NULL)");
        b.append(",('page_executiondetail','see_execution_tag','','fr','Rapport d\\'Execution par Tag','',NULL)");
        b.append(",('page_executiondetail','SeleniumLog','','en','Media Files','Link to the media execution files (ex : selenium logs).',NULL)");
        b.append(",('page_executiondetail','sort','','en','Sort','',NULL)");
        b.append(",('page_executiondetail','sort','','fr','Ordre','',NULL)");
        b.append(",('page_executiondetail','start','','en','Start','',NULL)");
        b.append(",('page_executiondetail','start','','fr','Début','',NULL)");
        b.append(",('page_executiondetail','status','','en','Status','',NULL)");
        b.append(",('page_executiondetail','status','','fr','Status','',NULL)");
        b.append(",('page_executiondetail','steps','','en','Steps','',NULL)");
        b.append(",('page_executiondetail','steps','','fr','Etapes','',NULL)");
        b.append(",('page_executiondetail','tag','','en','Tag','',NULL)");
        b.append(",('page_executiondetail','tag','','fr','Tag','',NULL)");
        b.append(",('page_executiondetail','time','','en','Time','',NULL)");
        b.append(",('page_executiondetail','time','','fr','Temps','',NULL)");
        b.append(",('page_executiondetail','title','','en','Execution Detail','',NULL)");
        b.append(",('page_executiondetail','title','','fr','Detail de l\\'Execution','',NULL)");
        b.append(",('page_executiondetail','url','','en','URL','',NULL)");
        b.append(",('page_executiondetail','url','','fr','URL','',NULL)");
        b.append(",('page_executiondetail','userAgent','','en','UserAgent','User Agent required for this execution',NULL)");
        b.append(",('page_executiondetail','userAgent','','fr','UserAgent','User Agent envoyé au navigateur web pour cette execution',NULL)");
        b.append(",('page_executiondetail','value1','','en','Value 1','',NULL)");
        b.append(",('page_executiondetail','value1','','fr','Valeur 1','',NULL)");
        b.append(",('page_executiondetail','value1init','','en','Value 1 Initial','',NULL)");
        b.append(",('page_executiondetail','value1init','','fr','Valeur 1 Initiale','',NULL)");
        b.append(",('page_executiondetail','value2','','en','Value 2','',NULL)");
        b.append(",('page_executiondetail','value2','','fr','Valeur 2','',NULL)");
        b.append(",('page_executiondetail','value2init','','en','Value 2 Initial','',NULL)");
        b.append(",('page_executiondetail','value2init','','fr','Valeur 2 Initiale','',NULL)");
        b.append(",('page_executiondetail','verbose','','en','Verbose','',NULL)");
        b.append(",('page_executiondetail','verbose','','fr','Loquacité','',NULL)");
        b.append(",('page_executiondetail','version','','en','Version','',NULL)");
        b.append(",('page_executiondetail','version','','fr','Version','',NULL)");
        b.append(",('page_executiondetail','viewExecution','','en','View Execution','',NULL)");
        b.append(",('page_executiondetail','viewExecution','','fr','Voir l\\'Execution','',NULL)");
        b.append(",('page_exeperbuildrevision','Days','','en','Days','Number of days with this revision for this build.',NULL)");
        b.append(",('page_exeperbuildrevision','Days','','fr','Jours',NULL,NULL)");
        b.append(",('page_exeperbuildrevision','NbAPP','','en','Nb Appli','Number of distinct <code class=\\'doc-crbvvoca\\'>application</code> that has been tested.',NULL)");
        b.append(",('page_exeperbuildrevision','NbAPP','','fr','Nb Appli',NULL,NULL)");
        b.append(",('page_exeperbuildrevision','NbExecution','','en','Exec','Number of <code class=\\'doc-crbvvoca\\'>test case</code> execution.',NULL)");
        b.append(",('page_exeperbuildrevision','NbExecution','','fr','Exec',NULL,NULL)");
        b.append(",('page_exeperbuildrevision','NbOK','','en','OK','Number of execution OK',NULL)");
        b.append(",('page_exeperbuildrevision','NbOK','','fr','OK',NULL,NULL)");
        b.append(",('page_exeperbuildrevision','NbTC','','en','Nb TC','Number of distinct <code class=\\'doc-crbvvoca\\'>test case</code> executed',NULL)");
        b.append(",('page_exeperbuildrevision','NbTC','','fr','Nb CT',NULL,NULL)");
        b.append(",('page_exeperbuildrevision','nb_exe_per_tc','','en','Exec/TC','Average number of execution per <code class=\\'doc-crbvvoca\\'>test case</code>.',NULL)");
        b.append(",('page_exeperbuildrevision','nb_exe_per_tc','','fr','Exec/CT',NULL,NULL)");
        b.append(",('page_exeperbuildrevision','nb_tc_per_day','','en','Exec/TC/Day','Number of execution per <code class=\\'doc-crbvvoca\\'>test case</code> and per day',NULL)");
        b.append(",('page_exeperbuildrevision','nb_tc_per_day','','fr','Exec/Ct/Jour',NULL,NULL)");
        b.append(",('page_exeperbuildrevision','OK_percentage','','en','%OK','Number of OK / number of execution',NULL)");
        b.append(",('page_exeperbuildrevision','OK_percentage','','fr','%OK',NULL,NULL)");
        b.append(",('page_exeperbuildrevision','RegressionExecutionStatus','','en','Regression Execution Status','This section report the execution statistics of regression testcases by the last sprint / Revision.<br>Criterias :<br>- On the applications that belong to current system.<br>- Test cases had to be in WORKING status at the time of the execution.<br>- Monitoring test cases are excluded<br>  (ie not <i>\\'Performance Monitor\\'</i> and not <i>\\'Business Activity Monitor\\'</i> and not <i>\\'Data Integrity Monitor\\'</i>)',NULL)");
        b.append(",('page_exeperbuildrevision','RegressionExecutionStatus','','fr','Statistiques d\\'Execution en Régression',NULL,NULL)");
        b.append(",('page_exeperbuildrevision','RegressionExecutionStatus1','','en','Regression Execution Status on External Applications','This section report the execution statistics of regression testcases by the last sprint / Revision.<br>Criterias :<br>- On the applications that <b>does not</b> belong to current system.<br>- Test cases had to be in WORKING status at the time of the execution.<br>- Monitoring test cases are excluded<br>  (ie not <i>\\'Performance Monitor\\'</i> and not <i>\\'Business Activity Monitor\\'</i> and not <i>\\'Data Integrity Monitor\\'</i>)',NULL)");
        b.append(",('page_exeperbuildrevision','RegressionExecutionStatus1','','fr','Statistiques d\\'Execution en Régression sur des Applications Externes',NULL,NULL)");
        b.append(",('page_global','beta_message','','en','This page is in beta, some features may not be available or fully functional.','',NULL)");
        b.append(",('page_global','beta_message','','fr','Cette page est en beta, certaines fonctionnalités peuvent être indisponnible ou non complètes.','',NULL)");
        b.append(",('page_global','btn_add','','en','Add','',NULL)");
        b.append(",('page_global','btn_add','','fr','Ajouter','',NULL)");
        b.append(",('page_global','btn_cancel','','en','Cancel','',NULL)");
        b.append(",('page_global','btn_cancel','','fr','Annuler','',NULL)");
        b.append(",('page_global','btn_clearfilter','','en','','',NULL)");
        b.append(",('page_global','btn_clearfilter','','fr','','',NULL)");
        b.append(",('page_global','btn_duplicate','','en','Duplicate','',NULL)");
        b.append(",('page_global','btn_duplicate','','fr','Dupliquer','',NULL)");
        b.append(",('page_global','btn_edit','','en','Edit',NULL,NULL)");
        b.append(",('page_global','btn_edit','','fr','Editer',NULL,NULL)");
        b.append(",('page_global','btn_export','','en','Export','',NULL)");
        b.append(",('page_global','btn_export','','fr','Export',NULL,NULL)");
        b.append(",('page_global','btn_restoreuserpreferences','','en','Load','',NULL)");
        b.append(",('page_global','btn_restoreuserpreferences','','fr','Charger','',NULL)");
        b.append(",('page_global','btn_savetableconfig','','en','Save','',NULL)");
        b.append(",('page_global','btn_savetableconfig','','fr','Sauvegarder','',NULL)");
        b.append(",('page_global','btn_showHideColumns','','en','Show/Hide','',NULL)");
        b.append(",('page_global','btn_showHideColumns','','fr','Afficher/Cacher','',NULL)");
        b.append(",('page_global','buttonAdd','','en','Save','',NULL)");
        b.append(",('page_global','buttonAdd','','fr','Sauvegarder','',NULL)");
        b.append(",('page_global','buttonClose','','en','Close','',NULL)");
        b.append(",('page_global','buttonClose','','fr','Fermer','',NULL)");
        b.append(",('page_global','buttonConfirm','','en','Yes','',NULL)");
        b.append(",('page_global','buttonConfirm','','fr','Oui','',NULL)");
        b.append(",('page_global','buttonDismiss','','en','No','',NULL)");
        b.append(",('page_global','buttonDismiss','','fr','Non','',NULL)");
        b.append(",('page_global','buttonLoad','','en','Load','',NULL)");
        b.append(",('page_global','buttonLoad','','fr','Charger','',NULL)");
        b.append(",('page_global','button_massAction','','en','Mass Action',NULL,NULL)");
        b.append(",('page_global','button_massAction','','fr','Action en masse',NULL,NULL)");
        b.append(",('page_global','columnAction','','en','Actions','',NULL)");
        b.append(",('page_global','columnAction','','fr','Actions','',NULL)");
        b.append(",('page_global','export_data','','en','Export Data','',NULL)");
        b.append(",('page_global','export_data','','fr','Export de Données',NULL,NULL)");
        b.append(",('page_global','filters','','en','Filters','',NULL)");
        b.append(",('page_global','filters','','fr','Filtres','',NULL)");
        b.append(",('page_global','footer_bug','','en','Open a bug or ask for any new feature <a target=\"_blank\"  href=\"%LINK%\">here</a>.','',NULL)");
        b.append(",('page_global','footer_bug','','fr','Ouvrir un bug ou envoyer une demande d\\'évolution <a target=\"_blank\"  href=\"%LINK%\">ici</a>.','',NULL)");
        b.append(",('page_global','footer_text','','en','Page started generating on %DATE% by %VERSION% in %ENV% and took %TIMING%ms','',NULL)");
        b.append(",('page_global','footer_text','','fr','Page générée le %DATE% par %VERSION% en environnement : %ENV% et a pris %TIMING%ms','',NULL)");
        b.append(",('page_global','invalid_extension_message','','en','Please select a file with the extension ','',NULL)");
        b.append(",('page_global','invalid_extension_message','','fr','Merci de selectionner un fichier avec l\\'extension ','',NULL)");
        b.append(",('page_global','lbl_all','','en','All','',NULL)");
        b.append(",('page_global','lbl_all','','fr','Tous','',NULL)");
        b.append(",('page_global','message_delete','','en','Do you want to delete <b>\\'%ENTRY%\\'</b> %TABLE% ?','',NULL)");
        b.append(",('page_global','message_delete','','fr','Voulez vous supprimer le %TABLE% <b>\\'%ENTRY%\\'</b> ?','',NULL)");
        b.append(",('page_global','message_massActionError','','en','Please select at least 1 line before trying to perform a mass action!',NULL,NULL)");
        b.append(",('page_global','message_massActionError','','fr','Merci de selectionner au moins 1 élément avant de faire une action en masse !',NULL,NULL)");
        b.append(",('page_global','old_page','','en','Old Page','',NULL)");
        b.append(",('page_global','old_page','','fr','Ancienne Page','',NULL)");
        b.append(",('page_global','processing','','en','Processing…','',NULL)");
        b.append(",('page_global','processing','','fr','Traitement en cours...','',NULL)");
        b.append(",('page_global','refresh','','en','Refresh',NULL,NULL)");
        b.append(",('page_global','refresh','','fr','Rafraichir',NULL,NULL)");
        b.append(",('page_global','tooltip_clearfilter','','en','Clear filters applied','',NULL)");
        b.append(",('page_global','tooltip_clearfilter','','fr','Effacer les filtres appliqués','',NULL)");
        b.append(",('page_global','tooltip_column_filter_empty','','en','Filter',NULL,NULL)");
        b.append(",('page_global','tooltip_column_filter_empty','','fr','Filtre',NULL,NULL)");
        b.append(",('page_global','tooltip_column_filter_filtered','','en','Filtered value(s)',NULL,NULL)");
        b.append(",('page_global','tooltip_column_filter_filtered','','fr','Valeur(s) filtrée(s)',NULL,NULL)");
        b.append(",('page_global','tooltip_delete_item','','en','This item will be removed from the database','',NULL)");
        b.append(",('page_global','tooltip_delete_item','','fr','Cette ligne sera supprimée de la base','',NULL)");
        b.append(",('page_global','tooltip_mark_remove','','en','Mark item to be removed from the database','',NULL)");
        b.append(",('page_global','tooltip_mark_remove','','fr','Selectionner la ligne pour la supprimer de la base','',NULL)");
        b.append(",('page_global','tooltip_massAction','','en','Select All or None to perform Mass Action',NULL,NULL)");
        b.append(",('page_global','tooltip_massAction','','fr','Selectionner ou Déselectionner tous pour Action en masse',NULL,NULL)");
        b.append(",('page_global','tooltip_massActionLine','','en','Select to perform Mass Action on that line',NULL,NULL)");
        b.append(",('page_global','tooltip_massActionLine','','fr','Selection pour Action en masse',NULL,NULL)");
        b.append(",('page_global','tooltip_restoreuserpreferences','','en','Restore the table configuration','Restore the table configuration from the user preferences. Filters and column display are available in user preferences',NULL)");
        b.append(",('page_global','tooltip_restoreuserpreferences','','fr','Restaurer la configuration de la table','Restaurer la configuration de la table à partir des préférences utilisateur. Les filtres et l\\'affichage des colonnes sont chargés à partir des préférences utilisateur',NULL)");
        b.append(",('page_global','tooltip_savetableconfig','','en','Save the table configuration','Save the table configuration. Filters and column display is stored in user preferences',NULL)");
        b.append(",('page_global','tooltip_savetableconfig','','fr','Sauvegarder la configuration de la table','Sauvegarder la configuration de la table. Les filtres et l\\'affichage des colonnes sont sauvegardés dans les préférences utilisateur',NULL)");
        b.append(",('page_global','tooltip_showHideColumns','','en','Show/hide columns','',NULL)");
        b.append(",('page_global','tooltip_showHideColumns','','fr','Afficher/cacher des colonnes','',NULL)");
        b.append(",('page_global','unexpected_error_message','','en','Unable to perform the task. An unexpected error has happened!','',NULL)");
        b.append(",('page_global','unexpected_error_message','','fr','Impossible de finaliser l\\'operation. Une erreur inattendue est survenue','',NULL)");
        b.append(",('page_global','warning','','en','Warning','',NULL)");
        b.append(",('page_global','warning','','fr','Attention','',NULL)");
        b.append(",('page_header','logout','','en','Logout','',NULL)");
        b.append(",('page_header','logout','','fr','Déconnexion','',NULL)");
        b.append(",('page_header','menuAdmin','','en','Administration','',NULL)");
        b.append(",('page_header','menuAdmin','','fr','Administration','',NULL)");
        b.append(",('page_header','menuApplicationObjects','','en','Application Object','',NULL)");
        b.append(",('page_header','menuApplicationObjects','','fr','Objet d\\'application','',NULL)");
        b.append(",('page_header','menuApplications','','en','Application','',NULL)");
        b.append(",('page_header','menuApplications','','fr','Application','',NULL)");
        b.append(",('page_header','menuAppService','','en','Service Library','',NULL)");
        b.append(",('page_header','menuAppService','','fr','Bibliothèque de Service','',NULL)");
        b.append(",('page_header','menuBatchInvariant','','en','Batch','',NULL)");
        b.append(",('page_header','menuBatchInvariant','','fr','Batch','',NULL)");
        b.append(",('page_header','menuBuildContent','','en','Build Content','',NULL)");
        b.append(",('page_header','menuBuildContent','','fr','Contenu des Builds','',NULL)");
        b.append(",('page_header','menuBuildRevision','','en','Build Revision Definition','',NULL)");
        b.append(",('page_header','menuBuildRevision','','fr','Définition des Builds Revisions','',NULL)");
        b.append(",('page_header','menuData','','en','Data','',NULL)");
        b.append(",('page_header','menuData','','fr','Données','',NULL)");
        b.append(",('page_header','menuDatabaseMaintenance','','en','Database Maintenance','',NULL)");
        b.append(",('page_header','menuDatabaseMaintenance','','fr','Maintenance de la base de données','',NULL)");
        b.append(",('page_header','menuDeployType','','en','Deploiement Type','',NULL)");
        b.append(",('page_header','menuDeployType','','fr','Type de Déploiement','',NULL)");
        b.append(",('page_header','menuDocumentation','','en','Documentation',NULL,NULL)");
        b.append(",('page_header','menuDocumentation','','fr','Documentation',NULL,NULL)");
        b.append(",('page_header','menuEditTest','','en','Test','',NULL)");
        b.append(",('page_header','menuEditTest','','fr','Test','',NULL)");
        b.append(",('page_header','menuEditTestCase','','en','Edit TestCase','',NULL)");
        b.append(",('page_header','menuEditTestCase','','fr','Editer un Cas de Test','',NULL)");
        b.append(",('page_header','menuEnvironmentManagement','','en','Environment Management','',NULL)");
        b.append(",('page_header','menuEnvironmentManagement','','fr','Gestion des Environnements','',NULL)");
        b.append(",('page_header','menuEnvironments','','en','Environment','',NULL)");
        b.append(",('page_header','menuEnvironments','','fr','Environnement','',NULL)");
        b.append(",('page_header','menuHelp','','en','Help',NULL,NULL)");
        b.append(",('page_header','menuHelp','','fr','Aide',NULL,NULL)");
        b.append(",('page_header','menuIntegration','','en','Integration','',NULL)");
        b.append(",('page_header','menuIntegration','','fr','Intégration','',NULL)");
        b.append(",('page_header','menuIntegrationStatus','','en','Integration Status','',NULL)");
        b.append(",('page_header','menuIntegrationStatus','','fr','Etat d\\'Intégration','',NULL)");
        b.append(",('page_header','menuInvariants','','en','Invariants','',NULL)");
        b.append(",('page_header','menuInvariants','','fr','Invariants','',NULL)");
        b.append(",('page_header','menuLabel','','en','Label','',NULL)");
        b.append(",('page_header','menuLabel','','fr','Label','',NULL)");
        b.append(",('page_header','menuLogViewer','','en','Log Viewer','',NULL)");
        b.append(",('page_header','menuLogViewer','','fr','Log','',NULL)");
        b.append(",('page_header','menuParameter','','en','Parameters','',NULL)");
        b.append(",('page_header','menuParameter','','fr','Paramètres','',NULL)");
        b.append(",('page_header','menuProject','','en','Project','',NULL)");
        b.append(",('page_header','menuProject','','fr','Projet','',NULL)");
        b.append(",('page_header','menuReportingExecutionByTag','','en','Execution Report By Tag','',NULL)");
        b.append(",('page_header','menuReportingExecutionByTag','','fr','Rapport d\\'Execution par Tag','',NULL)");
        b.append(",('page_header','menuReportingExecutionList','','en','Execution Report','',NULL)");
        b.append(",('page_header','menuReportingExecutionList','','fr','Rapport d\\'Execution','',NULL)");
        b.append(",('page_header','menuReportingExecutionThreadMonitoring','','en','Cerberus Monitoring','',NULL)");
        b.append(",('page_header','menuReportingExecutionThreadMonitoring','','fr','Monitoring Cerberus','',NULL)");
        b.append(",('page_header','menuRobot','','en','Robot','',NULL)");
        b.append(",('page_header','menuRobot','','fr','Robot','',NULL)");
        b.append(",('page_header','menuRun','','en','Run','',NULL)");
        b.append(",('page_header','menuRun','','fr','Executer','',NULL)");
        b.append(",('page_header','menuRunTestCase','','en','Run Test Case','',NULL)");
        b.append(",('page_header','menuRunTestCase','','fr','Executer un Cas de Test','',NULL)");
        b.append(",('page_header','menuRunTestSeePendingExecution','','en','Executions in queue','',NULL)");
        b.append(",('page_header','menuRunTestSeePendingExecution','','fr','Fil d\\'attente d\\'éxécutions','',NULL)");
        b.append(",('page_header','menuSqlLibrary','','en','SQL Library','',NULL)");
        b.append(",('page_header','menuSqlLibrary','','fr','Bibliothèque de script SQL','',NULL)");
        b.append(",('page_header','menuTest','','en','Test','',NULL)");
        b.append(",('page_header','menuTest','','fr','Test','',NULL)");
        b.append(",('page_header','menuTestBattery','','en','TestCase Battery','',NULL)");
        b.append(",('page_header','menuTestBattery','','fr','Batterie de Cas de Test','',NULL)");
        b.append(",('page_header','menuTestCampaign','','en','Test Campaign','',NULL)");
        b.append(",('page_header','menuTestCampaign','','fr','Campagne de Test','',NULL)");
        b.append(",('page_header','menuTestCase','','en','TestCase','',NULL)");
        b.append(",('page_header','menuTestCase','','fr','Cas De Test','',NULL)");
        b.append(",('page_header','menuTestCaseList','','en','TestCase List','',NULL)");
        b.append(",('page_header','menuTestCaseList','','fr','Liste des Cas de Test','',NULL)");
        b.append(",('page_header','menuTestDataLib','','en','Data Library','',NULL)");
        b.append(",('page_header','menuTestDataLib','','fr','Bibliothèque de Données','',NULL)");
        b.append(",('page_header','menuUsersManager','','en','User Management','',NULL)");
        b.append(",('page_header','menuUsersManager','','fr','Gestion des Utilisateurs','',NULL)");
        b.append(",('page_integrationstatus','DEV','','en','DEV','Nb of DEV active <code class=\\'doc-crbvvoca\\'>environments</code> on that Specific Version.',NULL)");
        b.append(",('page_integrationstatus','DEV','','fr','DEV','',NULL)");
        b.append(",('page_integrationstatus','environmentStatus','','en','Environment Status','',NULL)");
        b.append(",('page_integrationstatus','environmentStatus','','fr','Statut des environnements','',NULL)");
        b.append(",('page_integrationstatus','lastChanges','','en','Last Changes','',NULL)");
        b.append(",('page_integrationstatus','lastChanges','','fr','Derniers Changements','',NULL)");
        b.append(",('page_integrationstatus','PROD','','en','PROD','Nb of PROD active <code class=\\'doc-crbvvoca\\'>environments</code> on that Specific Version.',NULL)");
        b.append(",('page_integrationstatus','PROD','','fr','PROD','',NULL)");
        b.append(",('page_integrationstatus','QA','','en','QA','Nb of QA active <code class=\\'doc-crbvvoca\\'>environments</code> on that Specific Version.',NULL)");
        b.append(",('page_integrationstatus','QA','','fr','QA','',NULL)");
        b.append(",('page_integrationstatus','title','','en','INTEGRATION STATUS','',NULL)");
        b.append(",('page_integrationstatus','title','','fr','ETAT D\\'INTEGRATION','',NULL)");
        b.append(",('page_integrationstatus','UAT','','en','UAT','Nb of UAT active <code class=\\'doc-crbvvoca\\'>environments</code> on that Specific Version.',NULL)");
        b.append(",('page_integrationstatus','UAT','','fr','UAT','',NULL)");
        b.append(",('page_invariant','addinvariant_field','','en','Add Invariant','',NULL)");
        b.append(",('page_invariant','addinvariant_field','','fr','Ajouter un Invariant','',NULL)");
        b.append(",('page_invariant','allInvariants','','en','Invariants','',NULL)");
        b.append(",('page_invariant','allInvariants','','fr','Invariants','',NULL)");
        b.append(",('page_invariant','button_col','','en','Actions','',NULL)");
        b.append(",('page_invariant','button_col','','fr','Actions','',NULL)");
        b.append(",('page_invariant','button_create','','en','Create Invariant','',NULL)");
        b.append(",('page_invariant','button_create','','fr','Créer un Invariant','',NULL)");
        b.append(",('page_invariant','button_duplicate','','en','Duplicate Invariant','',NULL)");
        b.append(",('page_invariant','button_duplicate','','fr','Dupliquer un Invariant','',NULL)");
        b.append(",('page_invariant','button_edit','','en','Edit Invariant','',NULL)");
        b.append(",('page_invariant','button_edit','','fr','Editer l\\'Invariant','',NULL)");
        b.append(",('page_invariant','button_remove','','en','Delete Invariant',NULL,NULL)");
        b.append(",('page_invariant','button_remove','','fr','Supprimer l\\'Invariant',NULL,NULL)");
        b.append(",('page_invariant','description','','en','Description','',NULL)");
        b.append(",('page_invariant','description','','fr','Description','',NULL)");
        b.append(",('page_invariant','editinvariant_field','','en','Edit Invariant','',NULL)");
        b.append(",('page_invariant','editinvariant_field','','fr','Editer un Invariant','',NULL)");
        b.append(",('page_invariant','gp1','','en','Group 1','',NULL)");
        b.append(",('page_invariant','gp1','','fr','Groupe 1','',NULL)");
        b.append(",('page_invariant','gp2','','en','Group 2','',NULL)");
        b.append(",('page_invariant','gp2','','fr','Groupe 2','',NULL)");
        b.append(",('page_invariant','gp3','','en','Group 3','',NULL)");
        b.append(",('page_invariant','gp3','','fr','Groupe 3','',NULL)");
        b.append(",('page_invariant','gp4','','en','Group 4','',NULL)");
        b.append(",('page_invariant','gp4','','fr','Groupe 4','',NULL)");
        b.append(",('page_invariant','gp5','','en','Group 5','',NULL)");
        b.append(",('page_invariant','gp5','','fr','Groupe 5','',NULL)");
        b.append(",('page_invariant','gp6','','en','Group 6','',NULL)");
        b.append(",('page_invariant','gp6','','fr','Groupe 6','',NULL)");
        b.append(",('page_invariant','gp7','','en','Group 7','',NULL)");
        b.append(",('page_invariant','gp7','','fr','Groupe 7','',NULL)");
        b.append(",('page_invariant','gp8','','en','Group 8','',NULL)");
        b.append(",('page_invariant','gp8','','fr','Groupe 8','',NULL)");
        b.append(",('page_invariant','gp9','','en','Group 9','',NULL)");
        b.append(",('page_invariant','gp9','','fr','Groupe 9','',NULL)");
        b.append(",('page_invariant','idname','','en','IdName','',NULL)");
        b.append(",('page_invariant','idname','','fr','IdName','',NULL)");
        b.append(",('page_invariant','message_remove','','en','Are you sure?','',NULL)");
        b.append(",('page_invariant','message_remove','','fr','Etes vous sûrs?','',NULL)");
        b.append(",('page_invariant','private','','en','Private','',NULL)");
        b.append(",('page_invariant','private','','fr','Privés','',NULL)");
        b.append(",('page_invariant','private_invariant','','en','Private Invariants','',NULL)");
        b.append(",('page_invariant','private_invariant','','fr','Invariants privés','',NULL)");
        b.append(",('page_invariant','public','','en','Public','',NULL)");
        b.append(",('page_invariant','public','','fr','Publiques','',NULL)");
        b.append(",('page_invariant','public_invariant','','en','Public Invariants','',NULL)");
        b.append(",('page_invariant','public_invariant','','fr','Invariants publiques','',NULL)");
        b.append(",('page_invariant','sort','','en','Sort','',NULL)");
        b.append(",('page_invariant','sort','','fr','Tri','',NULL)");
        b.append(",('page_invariant','title_remove','','en','Delete an Invariant','',NULL)");
        b.append(",('page_invariant','title_remove','','fr','Supprimer un Invariant','',NULL)");
        b.append(",('page_invariant','value','','en','Value','',NULL)");
        b.append(",('page_invariant','value','','fr','Valeur','',NULL)");
        b.append(",('page_invariant','veryShortDesc','','en','Very Short Description','',NULL)");
        b.append(",('page_invariant','veryShortDesc','','fr','Petite Description','',NULL)");
        b.append(",('page_label','btn_create','','en','Create Label','',NULL)");
        b.append(",('page_label','btn_create','','fr','Créer un Label','',NULL)");
        b.append(",('page_label','btn_delete','','en','Delete Label','',NULL)");
        b.append(",('page_label','btn_delete','','fr','Effacer le Label','',NULL)");
        b.append(",('page_label','btn_edit','','en','Edit Label','',NULL)");
        b.append(",('page_label','btn_edit','','fr','Editer le Label','',NULL)");
        b.append(",('page_label','btn_view','','en','View Label','',NULL)");
        b.append(",('page_label','btn_view','','fr','Voir le Label','',NULL)");
        b.append(",('page_label','display','','en','Display','Display the generated label from the label and the color defined',NULL)");
        b.append(",('page_label','display','','fr','Affichage','Affiche le label généré à partir du label et de la couleur définis',NULL)");
        b.append(",('page_label','table_label','','en','Label List','',NULL)");
        b.append(",('page_label','table_label','','fr','Liste de Labels','',NULL)");
        b.append(",('page_label','title','','en','LABEL','This page can be used in order to manage the labels.','_label')");
        b.append(",('page_label','title','','fr','LABEL','Cette page permet de gérer et créer des labels.','_label')");
        b.append(",('page_logviewer','button_view','','en','Log entry detail','',NULL)");
        b.append(",('page_logviewer','button_view','','fr','Detail du log','',NULL)");
        b.append(",('page_logviewer','table_logviewer','','en','Log Viewer',NULL,NULL)");
        b.append(",('page_logviewer','table_logviewer','','fr','Liste des Modifications',NULL,NULL)");
        b.append(",('page_logviewer','title','','en','LOG VIEWER','This page displays all the log messages from Cerberus.','_log_viewer')");
        b.append(",('page_logviewer','title','','fr','JOURNAL DE MODIFICATIONS','Cette page affiche tout les messages de log de Cerberus.','_journal_de_modifications')");
        b.append(",('page_notification','Body','','en','Body','',NULL)");
        b.append(",('page_notification','Body','','fr','Corps',NULL,NULL)");
        b.append(",('page_notification','Cc','','en','Copy','',NULL)");
        b.append(",('page_notification','Subject','','en','Subject','',NULL)");
        b.append(",('page_notification','To','','en','To','',NULL)");
        b.append(",('page_parameter','allParameters','','en','Parameters','',NULL)");
        b.append(",('page_parameter','allParameters','','fr','Paramètres','',NULL)");
        b.append(",('page_parameter','button_col','','en','Actions','',NULL)");
        b.append(",('page_parameter','button_col','','fr','Actions','',NULL)");
        b.append(",('page_parameter','button_edit','','en','Edit Parameter','',NULL)");
        b.append(",('page_parameter','button_edit','','fr','Editer le parmètre','',NULL)");
        b.append(",('page_parameter','cerberus_col','','en','Cerberus Value','',NULL)");
        b.append(",('page_parameter','cerberus_col','','fr','Valeur de Cerberus','',NULL)");
        b.append(",('page_parameter','cerberus_field','','en','Cerberus Value','',NULL)");
        b.append(",('page_parameter','cerberus_field','','fr','Valeur de Cerberus','',NULL)");
        b.append(",('page_parameter','close_btn','','en','Close','',NULL)");
        b.append(",('page_parameter','close_btn','','fr','Fermer','',NULL)");
        b.append(",('page_parameter','description_col','','en','Description','',NULL)");
        b.append(",('page_parameter','description_col','','fr','Description','',NULL)");
        b.append(",('page_parameter','description_field','','en','Description','',NULL)");
        b.append(",('page_parameter','description_field','','fr','Description','',NULL)");
        b.append(",('page_parameter','editparameter_field','','en','Edit Parameter','',NULL)");
        b.append(",('page_parameter','editparameter_field','','fr','Modifier le paramètre','',NULL)");
        b.append(",('page_parameter','parameter_col','','en','Parameter','',NULL)");
        b.append(",('page_parameter','parameter_col','','fr','Paramètre','',NULL)");
        b.append(",('page_parameter','parameter_field','','en','Parameter','',NULL)");
        b.append(",('page_parameter','parameter_field','','fr','Paramètre','',NULL)");
        b.append(",('page_parameter','save_btn','','en','Save','',NULL)");
        b.append(",('page_parameter','save_btn','','fr','Sauvegarder','',NULL)");
        b.append(",('page_parameter','system_col','','en','System Value','',NULL)");
        b.append(",('page_parameter','system_col','','fr','Valeur du système','',NULL)");
        b.append(",('page_parameter','system_field','','en','System Value','',NULL)");
        b.append(",('page_parameter','system_field','','fr','Valeur du Système','',NULL)");
        b.append(",('page_parameter','table_parameter','','en','Parameter',NULL,NULL)");
        b.append(",('page_parameter','table_parameter','','fr','Paramètres',NULL,NULL)");
        b.append(",('page_project','button_create','','en','Create new Project','',NULL)");
        b.append(",('page_project','button_create','','fr','Créer un nouveau Projet','',NULL)");
        b.append(",('page_project','button_delete','','en','Delete Project','',NULL)");
        b.append(",('page_project','button_delete','','fr','Supprimer le Projet','',NULL)");
        b.append(",('page_project','button_edit','','en','Edit Project','',NULL)");
        b.append(",('page_project','button_edit','','fr','Modifier le Projet','',NULL)");
        b.append(",('page_project','message_delete','','en','Do you want to delete Project <b>\\'%ENTRY%\\'</b> ?<br>WARNING : All corresponding Test Cases will lose the link to that deleted Project !!!','',NULL)");
        b.append(",('page_project','message_delete','','fr','Confirmez vous la suppression du projet <b>\\'%ENTRY%\\'</b> ?<br> ATTENTION : Tous les Cas de Tests associés vont perdre le lien avec ce projet !!!','',NULL)");
        b.append(",('page_project','table_project','','en','Project List',NULL,NULL)");
        b.append(",('page_project','table_project','','fr','Liste des Projets',NULL,NULL)");
        b.append(",('page_project','title','','en','PROJECT','This page can be used in order to manage the projets.','_project')");
        b.append(",('page_project','title','','fr','PROJET','Cette page permet de gérer et créer des projets.','_projet')");
        b.append(",('page_reportbytag','btn_select_table','','en','Select table','',NULL)");
        b.append(",('page_reportbytag','btn_select_table','','fr','Select de la table',NULL,NULL)");
        b.append(",('page_reportbytag','button_load','','en','Load','',NULL)");
        b.append(",('page_reportbytag','button_load','','fr','Charger','',NULL)");
        b.append(",('page_reportbytag','button_reload','','en','Reload','',NULL)");
        b.append(",('page_reportbytag','button_reload','','fr','Recharger','',NULL)");
        b.append(",('page_reportbytag','filters','','en','Filters','Filters for the report',NULL)");
        b.append(",('page_reportbytag','filters','','fr','Filtres','Filtres du rapport',NULL)");
        b.append(",('page_reportbytag','report_envcountrybrowser','','en','Report By Application Environment Country Browser','Report of the execution filtering by Application Environement Country and Browser',NULL)");
        b.append(",('page_reportbytag','report_envcountrybrowser','','fr','Rapport par Application / Environnement / Pays / Navigateur','Rapport d\\'execution filtré par Application Envrionnment Pays et Navigateur',NULL)");
        b.append(",('page_reportbytag','report_function','','en','Report by Function','A bar chart with the number of execution and their status for each function',NULL)");
        b.append(",('page_reportbytag','report_function','','fr','Rapport par Fonction','diagramme en bâtons contenant le nombre d\\'execution par status pour chaque fonction',NULL)");
        b.append(",('page_reportbytag','report_list','','en','List','A list of all test case execution for each Environement, Country and Browser',NULL)");
        b.append(",('page_reportbytag','report_list','','fr','Liste','Une liste de toute les executions de cas de test par environnement, pays et navigateur',NULL)");
        b.append(",('page_reportbytag','report_status','','en','Report by Status','A report containing the number of execution for each status with their percentage and a chart associated',NULL)");
        b.append(",('page_reportbytag','report_status','','fr','Rapport par Status','Rapport contenant le nombre d\\'execution pour chaque status avec leur pourcentage et un graphique associé',NULL)");
        b.append(",('page_reportbytag','summary_table','','en','Summary Table','Summary of the total and percentages values for each result code (OK, KO, FA, NA, NE, PE, CA). Table is sorted by Application, Country and Environment.',NULL)");
        b.append(",('page_reportbytag','summary_table','','fr','Résumé de la table',NULL,NULL)");
        b.append(",('page_reportbytag','title','','en','Execution reporting by tag','This page generate a report of all the execution for a choosen tag',NULL)");
        b.append(",('page_reportbytag','title','','fr','Rapport d\\'execution par Tag','Cette page génère un rapport de toutes les executions pour un tag choisi',NULL)");
        b.append(",('page_robot','button_create','','en','Create new Robot','',NULL)");
        b.append(",('page_robot','button_create','','fr','Créer un nouveau Robot','',NULL)");
        b.append(",('page_robot','button_delete','','en','Delete Robot','',NULL)");
        b.append(",('page_robot','button_delete','','fr','Supprimer le Robot','',NULL)");
        b.append(",('page_robot','button_edit','','en','Edit Robot','',NULL)");
        b.append(",('page_robot','button_edit','','fr','Modifier le Robot','',NULL)");
        b.append(",('page_robot','table_robotlist','','en','Robot List',NULL,NULL)");
        b.append(",('page_robot','table_robotlist','','fr','Liste des Robots',NULL,NULL)");
        b.append(",('page_robot','title','','en','ROBOT','This page can be used in order to manage the robots.','_robot')");
        b.append(",('page_robot','title','','fr','ROBOT','Cette page permet de gérer et créer des Robots.','_robot')");
        b.append(",('page_runtest','addtoqueue','','en','Add to queue','',NULL)");
        b.append(",('page_runtest','addtoqueue','','fr','Ajouter à la liste','',NULL)");
        b.append(",('page_runtest','addtoqueueandrun','','en','Add to queue and run','',NULL)");
        b.append(",('page_runtest','addtoqueueandrun','','fr','Ajouter à la liste et executer','',NULL)");
        b.append(",('page_runtest','application','','en','Application','',NULL)");
        b.append(",('page_runtest','application','','fr','Application','',NULL)");
        b.append(",('page_runtest','automatic','','en','Automatic','',NULL)");
        b.append(",('page_runtest','automatic','','fr','Automatique','',NULL)");
        b.append(",('page_runtest','browser','','en','Browser','',NULL)");
        b.append(",('page_runtest','browser','','fr','Navigateur','',NULL)");
        b.append(",('page_runtest','campaign','','en','Campaign','',NULL)");
        b.append(",('page_runtest','campaign','','fr','Campagne','',NULL)");
        b.append(",('page_runtest','ChooseTest','','en','Choose Test Case / Environment / Country','',NULL)");
        b.append(",('page_runtest','ChooseTest','','fr','Selectionner Cas de test / Environnement / Pays','',NULL)");
        b.append(",('page_runtest','choose_test','','en','Choose Test','',NULL)");
        b.append(",('page_runtest','choose_test','','fr','Sélectionner vos test','',NULL)");
        b.append(",('page_runtest','countryList','','en','Country :','',NULL)");
        b.append(",('page_runtest','countryList','','fr','Liste de pays :','',NULL)");
        b.append(",('page_runtest','creator','','en','Creator','',NULL)");
        b.append(",('page_runtest','creator','','fr','Créateur','',NULL)");
        b.append(",('page_runtest','custom_config','','en','-- Custom Configuration --','',NULL)");
        b.append(",('page_runtest','custom_config','','fr','-- Configuration personnalisée --','',NULL)");
        b.append(",('page_runtest','default','','en','Default','',NULL)");
        b.append(",('page_runtest','default','','fr','Defaut','',NULL)");
        b.append(",('page_runtest','default_full_screen','','en','Default - Full Screen','',NULL)");
        b.append(",('page_runtest','default_full_screen','','fr','Defaut - Plein Ecran','',NULL)");
        b.append(",('page_runtest','empty_queue','','en','The Execution Queue is empty !','',NULL)");
        b.append(",('page_runtest','empty_queue','','fr','La liste d\\'execution est vide !','',NULL)");
        b.append(",('page_runtest','envList','','en','Environment :',NULL,NULL)");
        b.append(",('page_runtest','envList','','fr','Environnement : ',NULL,NULL)");
        b.append(",('page_runtest','execution_settings','','en','Execution Settings','',NULL)");
        b.append(",('page_runtest','execution_settings','','fr','Paramètres d\\'execution','',NULL)");
        b.append(",('page_runtest','filters','','en','Extended Test Case Filters','',NULL)");
        b.append(",('page_runtest','filters','','fr','Filtres Cas de test','',NULL)");
        b.append(",('page_runtest','group','','en','Group','',NULL)");
        b.append(",('page_runtest','group','','fr','Groupe','',NULL)");
        b.append(",('page_runtest','implementer','','en','Implementer','',NULL)");
        b.append(",('page_runtest','implementer','','fr','Implementeur','',NULL)");
        b.append(",('page_runtest','load','','en','Load','',NULL)");
        b.append(",('page_runtest','load','','fr','Charger','',NULL)");
        b.append(",('page_runtest','manual','','en','Manual','',NULL)");
        b.append(",('page_runtest','manual','','fr','Manuel','',NULL)");
        b.append(",('page_runtest','manual_execution','','en','Manual Execution','',NULL)");
        b.append(",('page_runtest','manual_execution','','fr','Execution Manuelle','',NULL)");
        b.append(",('page_runtest','more_than_one_execution_requested','','en','More than 1 excution and no Tag specified','',NULL)");
        b.append(",('page_runtest','more_than_one_execution_requested','','fr','Plus d\\'une execution séléctionnées et aucun tag n\\'a été spécifié','',NULL)");
        b.append(",('page_runtest','mycontextroot','','en','My Context Root','',NULL)");
        b.append(",('page_runtest','mycontextroot','','fr','Ma racine de contexte','',NULL)");
        b.append(",('page_runtest','myenvdata','','en','My environment data','',NULL)");
        b.append(",('page_runtest','myenvdata','','fr','Mes données d\\'environnement','',NULL)");
        b.append(",('page_runtest','myhost','','en','My host','',NULL)");
        b.append(",('page_runtest','myhost','','fr','Mon hôte','',NULL)");
        b.append(",('page_runtest','myloginrelativeurl','','en','My login relative url','',NULL)");
        b.append(",('page_runtest','myloginrelativeurl','','fr','Mon url de login relative','',NULL)");
        b.append(",('page_runtest','notValid','','en','Some executions couldn\\'t be added to the queue','',NULL)");
        b.append(",('page_runtest','notValid','','fr','Des executions n\\'ont pas pu être ajoutées à la liste','',NULL)");
        b.append(",('page_runtest','outputformat','','en','Output Format','',NULL)");
        b.append(",('page_runtest','outputformat','','fr','Format de sortie','',NULL)");
        b.append(",('page_runtest','pagesource','','en','Page Source','',NULL)");
        b.append(",('page_runtest','pagesource','','fr','Page Source','',NULL)");
        b.append(",('page_runtest','platform','','en','Platform','',NULL)");
        b.append(",('page_runtest','platform','','fr','Plateforme','',NULL)");
        b.append(",('page_runtest','potential','','en','Potential','',NULL)");
        b.append(",('page_runtest','potential','','fr','Potentiel','',NULL)");
        b.append(",('page_runtest','priority','','en','Priority','',NULL)");
        b.append(",('page_runtest','priority','','fr','Priorité','',NULL)");
        b.append(",('page_runtest','project','','en','Project','',NULL)");
        b.append(",('page_runtest','project','','fr','Projet','',NULL)");
        b.append(",('page_runtest','queue','','en','Queue','',NULL)");
        b.append(",('page_runtest','queue','','fr','Liste','',NULL)");
        b.append(",('page_runtest','reset_queue','','en','Reset queue','',NULL)");
        b.append(",('page_runtest','reset_queue','','fr','Vider la liste','',NULL)");
        b.append(",('page_runtest','retries','','en','Retries','',NULL)");
        b.append(",('page_runtest','retries','','fr','Essais','',NULL)");
        b.append(",('page_runtest','robot_settings','','en','Robot Settings','',NULL)");
        b.append(",('page_runtest','robot_settings','','fr','Paramètres du Robot','',NULL)");
        b.append(",('page_runtest','run','','en','Run','',NULL)");
        b.append(",('page_runtest','run','','fr','Executer','',NULL)");
        b.append(",('page_runtest','saverobotpref','','en','Save Robot Preferencies','',NULL)");
        b.append(",('page_runtest','saverobotpref','','fr','Enregistrer les préférences du robot','',NULL)");
        b.append(",('page_runtest','save_execution_params','','en','Save Execution Parameters','',NULL)");
        b.append(",('page_runtest','save_execution_params','','fr','Sauvegarder les paramètres d\\'execution','',NULL)");
        b.append(",('page_runtest','screenshot','','en','Screenshot','',NULL)");
        b.append(",('page_runtest','screenshot','','fr','Screenshot','',NULL)");
        b.append(",('page_runtest','screensize','','en','Screen size','',NULL)");
        b.append(",('page_runtest','screensize','','fr','Taille d\\'écran','',NULL)");
        b.append(",('page_runtest','selection_type','','en','Selection type','',NULL)");
        b.append(",('page_runtest','selection_type','','fr','Type de selection','',NULL)");
        b.append(",('page_runtest','select_all','','en','Select All','',NULL)");
        b.append(",('page_runtest','select_all','','fr','Tout Sélectionner','',NULL)");
        b.append(",('page_runtest','select_campaign','','en','Select a campaign','',NULL)");
        b.append(",('page_runtest','select_campaign','','fr','Sélectionner une campagne','',NULL)");
        b.append(",('page_runtest','select_list_test','','en','Select a list of test','',NULL)");
        b.append(",('page_runtest','select_list_test','','fr','Sélectionner une list de test','',NULL)");
        b.append(",('page_runtest','select_one_country','','en','Select at least one Country !','',NULL)");
        b.append(",('page_runtest','select_one_country','','fr','Sélectionner au moins un Pays !','',NULL)");
        b.append(",('page_runtest','select_one_env','','en','Select at least one Environment !','',NULL)");
        b.append(",('page_runtest','select_one_env','','fr','Sélectionner au moins un Environnement !','',NULL)");
        b.append(",('page_runtest','select_one_testcase','','en','Select at least one TestCase !','',NULL)");
        b.append(",('page_runtest','select_one_testcase','','fr','Sélectionner au moins un cas de test !','',NULL)");
        b.append(",('page_runtest','select_robot','','en','Select a robot','',NULL)");
        b.append(",('page_runtest','select_robot','','fr','Sélectionner un robot','',NULL)");
        b.append(",('page_runtest','seleniumlog','','en','Selenium Log','',NULL)");
        b.append(",('page_runtest','seleniumlog','','fr','Log de Selenium','',NULL)");
        b.append(",('page_runtest','selenium_ip','','en','Selenium Server IP','',NULL)");
        b.append(",('page_runtest','selenium_ip','','fr','IP du serveur Selenium','',NULL)");
        b.append(",('page_runtest','selenium_port','','en','Selenium Server Port','',NULL)");
        b.append(",('page_runtest','selenium_port','','fr','Port du serveur Selenium','',NULL)");
        b.append(",('page_runtest','size','','en','Size','',NULL)");
        b.append(",('page_runtest','size','','fr','Taille','',NULL)");
        b.append(",('page_runtest','status','','en','Status','',NULL)");
        b.append(",('page_runtest','status','','fr','Status','',NULL)");
        b.append(",('page_runtest','synchroneous','','en','Synchroneous','',NULL)");
        b.append(",('page_runtest','synchroneous','','fr','Synchrone','',NULL)");
        b.append(",('page_runtest','tag','','en','Tag','',NULL)");
        b.append(",('page_runtest','tag','','fr','Tag','',NULL)");
        b.append(",('page_runtest','targetrev','','en','Target Revision','',NULL)");
        b.append(",('page_runtest','targetrev','','fr','Révision Cible','',NULL)");
        b.append(",('page_runtest','targetsprint','','en','Target Sprint','',NULL)");
        b.append(",('page_runtest','targetsprint','','fr','Sprint Cible','',NULL)");
        b.append(",('page_runtest','test','','en','Test','',NULL)");
        b.append(",('page_runtest','test','','fr','Test','',NULL)");
        b.append(",('page_runtest','testbattery','','en','Test Battery','',NULL)");
        b.append(",('page_runtest','testbattery','','fr','Batterie de test','',NULL)");
        b.append(",('page_runtest','testcaseList','','en','Test Case :',NULL,NULL)");
        b.append(",('page_runtest','testcaseList','','fr','Cas de Test : ',NULL,NULL)");
        b.append(",('page_runtest','timeout','','en','Timeout','',NULL)");
        b.append(",('page_runtest','timeout','','fr','Temporisation','',NULL)");
        b.append(",('page_runtest','title','','en','Run Test','',NULL)");
        b.append(",('page_runtest','title','','fr','Executer un test','',NULL)");
        b.append(",('page_runtest','valid','','en','Executions in queue','',NULL)");
        b.append(",('page_runtest','valid','','fr','Executions dans la liste','',NULL)");
        b.append(",('page_runtest','verbose','','en','Verbose','',NULL)");
        b.append(",('page_runtest','verbose','','fr','Loquacité','',NULL)");
        b.append(",('page_runtest','version','','en','Version','',NULL)");
        b.append(",('page_runtest','version','','fr','Version','',NULL)");
        b.append(",('page_runtests','Browser','','en','Browser','This is the browser on which the <code class=\\'doc-crbvvoca\\'>test case</code> will be executed. <br><br>Firefox is set as the default browser as it is automatically embed in the selenium Server.<br><br>You can use other browsers IE9, IE10, IE11 and chrome using the drivers associated.<br>Please, read the <i>Example scripts to start your local selenium server</i> for more information',NULL)");
        b.append(",('page_runtests','manualExecution','','en','ManualExecution','Manual Execution is the way to execute the <code class=\\'doc-crbvvoca\\'>test case</code>. It could be YES to manually execute all kind of <code class=\\'doc-crbvvoca\\'>test case</code>, or NO to execute if automatically.',NULL)");
        b.append(",('page_runtests','manualExecution','','fr','Execution manuelle',NULL,NULL)");
        b.append(",('page_runtests','outputformat','','en','Output Format','This is the format of the output.<br><br><b>gui</b> : output is a web page. If test can be executed, the output will redirect to the test execution detail page.<br><b>compact</b> : output is plain text in a single line. This is more convenient when the test case is executed in batch mode.<br><b>verbose-txt</b> : output is a plain text with key=value format. This is also for batch mode but when the output needs to be parsed to get detailed information.',NULL)");
        b.append(",('page_runtests','PageSource','','en','PageSource','This define whether Page Source will be recorded during the execution of the test.<br><br><b>0</b> : No Page Source are recorded. This is to be used when a massive amout of tests are performed.<br><b>1</b> : Page Source are taken only when action or control provide unexpected result.<br><b>2</b> : Page Source are always taken on every selenium action. This is to be used only on very specific cases where all actions needs to take page source (For debug mode for example.',NULL)");
        b.append(",('page_runtests','Retries','','en','Retries','Define the number of Retry of the Execution in case of Not OK status.<br>\nAs soon a OK status if reached, the engine stop to retry to execute the testcase.',NULL)");
        b.append(",('page_runtests','screenshot','','en','Screenshot','This define whether screenshots will be taken during the execution of the test.<br><br><b>0</b> : No screenshots are taken. This is to be used when a massive amout of tests are performed.<br><b>1</b> : Screenshots are taken only when action or control provide unexpected result.<br><b>2</b> : Screenshots are always taken on every selenium action. This is to be used only on very specific cases where all actions needs a screenshot.',NULL)");
        b.append(",('page_runtests','screensize','','en','Screen Size','This is the size of the browser screen that will be set for the execution.<br><br>Default Values are set inside the invariant SCREENSIZE that can be configured on Edit Public invariant screen..<br>Value must be two Integer splitted by a <b>*</b> mark.<br><i>For Exemple : 1024*768</i><br><br>If you need to add other Values, please contact your Cerberus Administrator.',NULL)");
        b.append(",('page_runtests','SeleniumLog','','en','SeleniumLog','This define whether Selenium Log will be recorded during the execution of the test.<br><br><b>0</b> : No  Selenium Log are recorded. This is to be used when a massive amout of tests are performed.<br><b>1</b> : Selenium Log are taken only when action or control provide unexpected result.<br><b>2</b> : Selenium Log are always taken on execution. This is to be used only on very specific cases where all actions needs to take Selenium Log (For debug mode for example.',NULL)");
        b.append(",('page_runtests','SeleniumServerIP','','en','Selenium Server IP','Selenium Server IP is the IP of the computer where the selenium server is running.<br>This also correspond to the IP where the brower will execute the <code class=\\'doc-crbvvoca\\'>test case</code>.',NULL)");
        b.append(",('page_runtests','SeleniumServerIP','','fr','IP du serveur Selenium',NULL,NULL)");
        b.append(",('page_runtests','SeleniumServerPort','','en','Selenium Server Port','Selenium Server Port is the port which will be used to run the <code class=\\'doc-crbvvoca\\'>test case</code>.',NULL)");
        b.append(",('page_runtests','SeleniumServerPort','','fr','Port du serveur Selenium',NULL,NULL)");
        b.append(",('page_runtests','Synchroneous','','en','Synchroneous','This is parameter to define if user mut be redirected to the reporting during the execution.<br><br>By default, synchroneous will be set to Y, meaning the redirection will be at the end of the execution.',NULL)");
        b.append(",('page_runtests','Timeout','','en','Timeout','This is the timeout used for the execution.<br><br>If empty, the default value will be the one set in the parameter table.',NULL)");
        b.append(",('page_sqlLibrary','addSqlLibrary_field','','en','Add Library','',NULL)");
        b.append(",('page_sqlLibrary','addSqlLibrary_field','','fr','Ajouter une librairie','',NULL)");
        b.append(",('page_sqlLibrary','allSqlLibrarys','','en','SQL Libraries','',NULL)");
        b.append(",('page_sqlLibrary','allSqlLibrarys','','fr','Librairies SQL','',NULL)");
        b.append(",('page_sqlLibrary','button_col','','en','Actions','',NULL)");
        b.append(",('page_sqlLibrary','button_col','','fr','Actions','',NULL)");
        b.append(",('page_sqlLibrary','button_create','','en','Add Library','',NULL)");
        b.append(",('page_sqlLibrary','button_create','','fr','Ajouter une librairie','',NULL)");
        b.append(",('page_sqlLibrary','button_edit','','en','Edit Library','',NULL)");
        b.append(",('page_sqlLibrary','button_edit','','fr','Editer la librairie','',NULL)");
        b.append(",('page_sqlLibrary','button_remove','','en','Delete Library','',NULL)");
        b.append(",('page_sqlLibrary','button_remove','','fr','Supprimer la librairie','',NULL)");
        b.append(",('page_sqlLibrary','close_btn','','en','Close','',NULL)");
        b.append(",('page_sqlLibrary','close_btn','','fr','Fermer','',NULL)");
        b.append(",('page_sqlLibrary','database_col','','en','Database','',NULL)");
        b.append(",('page_sqlLibrary','database_col','','fr','Base de donnée','',NULL)");
        b.append(",('page_sqlLibrary','database_field','','en','Database','',NULL)");
        b.append(",('page_sqlLibrary','database_field','','fr','Base de donnée','',NULL)");
        b.append(",('page_sqlLibrary','description_col','','en','Description','',NULL)");
        b.append(",('page_sqlLibrary','description_col','','fr','Description','',NULL)");
        b.append(",('page_sqlLibrary','description_field','','en','Description','',NULL)");
        b.append(",('page_sqlLibrary','description_field','','fr','Description','',NULL)");
        b.append(",('page_sqlLibrary','editSqlLibrary_field','','en','Edit Library','',NULL)");
        b.append(",('page_sqlLibrary','editSqlLibrary_field','','fr','Editer la librairie','',NULL)");
        b.append(",('page_sqlLibrary','idname_field','','en','Name','',NULL)");
        b.append(",('page_sqlLibrary','idname_field','','fr','Nom','',NULL)");
        b.append(",('page_sqlLibrary','message_remove','','en','Are you sure?','',NULL)");
        b.append(",('page_sqlLibrary','message_remove','','fr','Êtes-vous sûrs?','',NULL)");
        b.append(",('page_sqlLibrary','save_btn','','en','Save Library','',NULL)");
        b.append(",('page_sqlLibrary','save_btn','','fr','Sauvegarder la librairie','',NULL)");
        b.append(",('page_sqlLibrary','script_col','','en','Script','',NULL)");
        b.append(",('page_sqlLibrary','script_col','','fr','Script','',NULL)");
        b.append(",('page_sqlLibrary','script_field','','en','Script','',NULL)");
        b.append(",('page_sqlLibrary','script_field','','fr','Script','',NULL)");
        b.append(",('page_sqlLibrary','sqlLibrary','','en','SQL Library','',NULL)");
        b.append(",('page_sqlLibrary','sqlLibrary','','fr','Librairie SQL','',NULL)");
        b.append(",('page_sqlLibrary','sqlLibrary_col','','en','Name','',NULL)");
        b.append(",('page_sqlLibrary','sqlLibrary_col','','fr','Nom','',NULL)");
        b.append(",('page_sqlLibrary','sqlLibrary_field','','en','SQL Librairy','',NULL)");
        b.append(",('page_sqlLibrary','sqlLibrary_field','','fr','Librairie SQL','',NULL)");
        b.append(",('page_sqlLibrary','title_remove','','en','Delete Library','',NULL)");
        b.append(",('page_sqlLibrary','title_remove','','fr','Supprimer la librairie','',NULL)");
        b.append(",('page_sqlLibrary','type_col','','en','Type','',NULL)");
        b.append(",('page_sqlLibrary','type_col','','fr','Type','',NULL)");
        b.append(",('page_sqlLibrary','type_field','','en','Type','',NULL)");
        b.append(",('page_sqlLibrary','type_field','','fr','Type','',NULL)");
        b.append(",('page_test','btn_create','','en','Create Test','',NULL)");
        b.append(",('page_test','btn_create','','fr','Créer un Test','',NULL)");
        b.append(",('page_test','btn_edit','','en','Edit Test','',NULL)");
        b.append(",('page_test','btn_edit','','fr','Modifier le Test','',NULL)");
        b.append(",('page_test','btn_tclist','','en','Go To TestCase List','',NULL)");
        b.append(",('page_test','btn_tclist','','fr','Aller à la page des Cas de Test','',NULL)");
        b.append(",('page_test','button_delete','','en','Delete Test','',NULL)");
        b.append(",('page_test','button_delete','','fr','Supprimer le Test','',NULL)");
        b.append(",('page_test','delete','','en','Dlt','Select this checkbox and then save changes in order to delete the row.',NULL)");
        b.append(",('page_test','message_delete','','en','Do you want to delete Test <b>\\'%ENTRY%\\'</b> ?<br>WARNING : All corresponding Test Cases will be removed as well !!!','',NULL)");
        b.append(",('page_test','message_delete','','fr','Confirmez vous la suppression du Test <b>\\'%ENTRY%\\'</b> ?<br> ATTENTION : Tous les Cas de Test associés seront également supprimés !!!','',NULL)");
        b.append(",('page_test','table_testlist','','en','Test List',NULL,NULL)");
        b.append(",('page_test','table_testlist','','fr','Liste de Tests',NULL,NULL)");
        b.append(",('page_testbattery','addtestbattery_field','','en','Add Battery','',NULL)");
        b.append(",('page_testbattery','addtestbattery_field','','fr','Ajouter la Batterie','',NULL)");
        b.append(",('page_testbattery','addtestcase_tab','','en','Add Test Case','',NULL)");
        b.append(",('page_testbattery','addtestcase_tab','','fr','Ajouter un cas de test','',NULL)");
        b.append(",('page_testbattery','add_btn','','en','Add','',NULL)");
        b.append(",('page_testbattery','add_btn','','fr','Ajouter','',NULL)");
        b.append(",('page_testbattery','allTestbatterys','','en','Test Battery','',NULL)");
        b.append(",('page_testbattery','allTestbatterys','','fr','Batterie de Test','',NULL)");
        b.append(",('page_testbattery','back_btn','','en','Back','',NULL)");
        b.append(",('page_testbattery','back_btn','','fr','Précédent','',NULL)");
        b.append(",('page_testbattery','battery_field','','en','Battery','',NULL)");
        b.append(",('page_testbattery','battery_field','','fr','Batterie','',NULL)");
        b.append(",('page_testbattery','button_add','','en','Add Test Case','',NULL)");
        b.append(",('page_testbattery','button_add','','fr','Ajouter un Cas de Test','',NULL)");
        b.append(",('page_testbattery','button_col','','en','Actions','',NULL)");
        b.append(",('page_testbattery','button_col','','fr','Actions','',NULL)");
        b.append(",('page_testbattery','button_create','','en','Create Battery','',NULL)");
        b.append(",('page_testbattery','button_create','','fr','Créer une Batterie','',NULL)");
        b.append(",('page_testbattery','button_edit','','en','Edit Battery','',NULL)");
        b.append(",('page_testbattery','button_edit','','fr','Editer la Batterie','',NULL)");
        b.append(",('page_testbattery','button_remove','','en','Delete Battery','',NULL)");
        b.append(",('page_testbattery','button_remove','','fr','Supprimer la Batterie','',NULL)");
        b.append(",('page_testbattery','close_btn','','en','Close','',NULL)");
        b.append(",('page_testbattery','close_btn','','fr','Fermer','',NULL)");
        b.append(",('page_testbattery','description_col','','en','Description','',NULL)");
        b.append(",('page_testbattery','description_col','','fr','Description','',NULL)");
        b.append(",('page_testbattery','description_field','','en','Description','',NULL)");
        b.append(",('page_testbattery','description_field','','fr','Description','',NULL)");
        b.append(",('page_testbattery','description_tab','','en','Description','',NULL)");
        b.append(",('page_testbattery','description_tab','','fr','Description','',NULL)");
        b.append(",('page_testbattery','edittestbattery_field','','en','Edit Battery','',NULL)");
        b.append(",('page_testbattery','edittestbattery_field','','fr','Modifier la Batterie','',NULL)");
        b.append(",('page_testbattery','message_remove','','en','Are you sure to delete the battery ?','',NULL)");
        b.append(",('page_testbattery','message_remove','','fr','Êtes-vous sûrs de supprimer la batterie ?','',NULL)");
        b.append(",('page_testbattery','save_btn','','en','Save','',NULL)");
        b.append(",('page_testbattery','save_btn','','fr','Sauvegarder','',NULL)");
        b.append(",('page_testbattery','table_testbattery','','en','Batterie de Test',NULL,NULL)");
        b.append(",('page_testbattery','table_testbattery','','fr','Batterie de Test',NULL,NULL)");
        b.append(",('page_testbattery','testbattery_col','','en','Test Battery','',NULL)");
        b.append(",('page_testbattery','testbattery_col','','fr','Batterie de test','',NULL)");
        b.append(",('page_testbattery','testcampaign_col','','en','Test Battery','',NULL)");
        b.append(",('page_testbattery','testcampaign_col','','fr','Batterie de test','',NULL)");
        b.append(",('page_testbattery','testcase_col','','en','Test Case ID','',NULL)");
        b.append(",('page_testbattery','testcase_col','','fr','ID du Cas de Test','',NULL)");
        b.append(",('page_testbattery','testcase_tab','','en','Test Case','',NULL)");
        b.append(",('page_testbattery','testcase_tab','','fr','Cas de test','',NULL)");
        b.append(",('page_testbattery','test_col','','en','Test','',NULL)");
        b.append(",('page_testbattery','test_col','','fr','Test','',NULL)");
        b.append(",('page_testbattery','title_remove','','en','Delete Battery','',NULL)");
        b.append(",('page_testbattery','title_remove','','fr','Supprimer la Batterie','',NULL)");
        b.append(",('page_testcampaign','addtestcampaign_field','','en','Add Campaign','',NULL)");
        b.append(",('page_testcampaign','addtestcampaign_field','','fr','Ajouter la Campagne','',NULL)");
        b.append(",('page_testcampaign','add_btn','','en','Add','',NULL)");
        b.append(",('page_testcampaign','add_btn','','fr','Ajouter','',NULL)");
        b.append(",('page_testcampaign','allTestcampaigns','','en','Test Campaigns','',NULL)");
        b.append(",('page_testcampaign','allTestcampaigns','','fr','Campagnes de Test','',NULL)");
        b.append(",('page_testcampaign','battery_tab','','en','Battery','',NULL)");
        b.append(",('page_testcampaign','battery_tab','','fr','Batterie','',NULL)");
        b.append(",('page_testcampaign','button_col','','en','Actions','',NULL)");
        b.append(",('page_testcampaign','button_col','','fr','Actions','',NULL)");
        b.append(",('page_testcampaign','button_create','','en','Create Campaign','',NULL)");
        b.append(",('page_testcampaign','button_create','','fr','Créer une Campagne','',NULL)");
        b.append(",('page_testcampaign','button_edit','','en','Edit Campaign','',NULL)");
        b.append(",('page_testcampaign','button_edit','','fr','Editer la Campagne','',NULL)");
        b.append(",('page_testcampaign','button_remove','','en','Delete Campaign','',NULL)");
        b.append(",('page_testcampaign','button_remove','','fr','Supprimer la Campagne','',NULL)");
        b.append(",('page_testcampaign','button_view','','en','View Campaign','',NULL)");
        b.append(",('page_testcampaign','button_view','','fr','Voir la Campagne','',NULL)");
        b.append(",('page_testcampaign','campaign_field','','en','Campaign','',NULL)");
        b.append(",('page_testcampaign','campaign_field','','fr','Campagne','',NULL)");
        b.append(",('page_testcampaign','close_btn','','en','Close','',NULL)");
        b.append(",('page_testcampaign','close_btn','','fr','Fermer','',NULL)");
        b.append(",('page_testcampaign','description_col','','en','Description','',NULL)");
        b.append(",('page_testcampaign','description_col','','fr','Description','',NULL)");
        b.append(",('page_testcampaign','description_field','','en','Description','',NULL)");
        b.append(",('page_testcampaign','description_field','','fr','Description','',NULL)");
        b.append(",('page_testcampaign','description_tab','','en','Description','',NULL)");
        b.append(",('page_testcampaign','description_tab','','fr','Description','',NULL)");
        b.append(",('page_testcampaign','edittestcampaign_field','','en','Edit Campaign','',NULL)");
        b.append(",('page_testcampaign','edittestcampaign_field','','fr','Modifier la Campagne','',NULL)");
        b.append(",('page_testcampaign','message_remove','','en','Are you sure?','',NULL)");
        b.append(",('page_testcampaign','message_remove','','fr','Êtes-vous sûrs?','',NULL)");
        b.append(",('page_testcampaign','parameter_col','','en','Parameter','',NULL)");
        b.append(",('page_testcampaign','parameter_col','','fr','Paramètre','',NULL)");
        b.append(",('page_testcampaign','parameter_tab','','en','Parameter','',NULL)");
        b.append(",('page_testcampaign','parameter_tab','','fr','Paramètre','',NULL)");
        b.append(",('page_testcampaign','save_btn','','en','Save','',NULL)");
        b.append(",('page_testcampaign','save_btn','','fr','Sauvegarder','',NULL)");
        b.append(",('page_testcampaign','table_testcampaign','','en','Test Campaigns',NULL,NULL)");
        b.append(",('page_testcampaign','table_testcampaign','','fr','Campagnes de Test',NULL,NULL)");
        b.append(",('page_testcampaign','testbattery_col','','en','Test Battery','',NULL)");
        b.append(",('page_testcampaign','testbattery_col','','fr','Batterie de test','',NULL)");
        b.append(",('page_testcampaign','testcampaign_col','','en','Test Campaign','',NULL)");
        b.append(",('page_testcampaign','testcampaign_col','','fr','Campagne de test','',NULL)");
        b.append(",('page_testcampaign','testcase_col','','en','Test Case','',NULL)");
        b.append(",('page_testcampaign','testcase_col','','fr','Cas de Test','',NULL)");
        b.append(",('page_testcampaign','test_col','','en','Test','',NULL)");
        b.append(",('page_testcampaign','test_col','','fr','Test','',NULL)");
        b.append(",('page_testcampaign','title_remove','','en','Delete Campaign','',NULL)");
        b.append(",('page_testcampaign','title_remove','','fr','Supprimer la Campagne','',NULL)");
        b.append(",('page_testcampaign','value_col','','en','Value','',NULL)");
        b.append(",('page_testcampaign','value_col','','fr','Valeur','',NULL)");
        b.append(",('page_testcampaign','viewtestcampaign_field','','en','Campaign','',NULL)");
        b.append(",('page_testcampaign','viewtestcampaign_field','','fr','Campagne','',NULL)");
        b.append(",('page_testcase','BugIDLink','','en','Link','',NULL)");
        b.append(",('page_testcase','BugIDLink','','fr','Lien',NULL,NULL)");
        b.append(",('page_testcase','delete','','en','Dlt','Select this checkbox and then save changes in order to delete the row.',NULL)");
        b.append(",('page_testcase','delete','','fr','Sup','',NULL)");
        b.append(",('page_testcase','dpd_choose_step','','en','-- Choose Step  --','',NULL)");
        b.append(",('page_testcase','dpd_choose_step','','fr','-- Choisissez une Etape --',NULL,NULL)");
        b.append(",('page_testcase','dpd_choose_test','','en','-- Choose Test --','',NULL)");
        b.append(",('page_testcase','dpd_choose_test','','fr','-- Choisissez une Test --',NULL,NULL)");
        b.append(",('page_testcase','dpd_choose_testcase','','en','-- Choose Test Case --','',NULL)");
        b.append(",('page_testcase','dpd_choose_testcase','','fr','-- Choisissez un cas de test --',NULL,NULL)");
        b.append(",('page_testcase','laststatus','','en','Last Execution Status','',NULL)");
        b.append(",('page_testcase','laststatus','','fr','Dernier statut d\\'éxecution',NULL,NULL)");
        b.append(",('page_testcase','lbl_copied_from','','en','Copied from:','',NULL)");
        b.append(",('page_testcase','lbl_copied_from','','fr','Copié depuis:',NULL,NULL)");
        b.append(",('page_testcase','link_edit_step','','en','Edit Used Step','',NULL)");
        b.append(",('page_testcase','link_edit_step','','fr','Editer l\\'étape importée',NULL,NULL)");
        b.append(",('page_testcase','message_delete','','en','Do you want to delete Test Case <b>\\'%ENTRY%\\'</b> ?','',NULL)");
        b.append(",('page_testcase','message_delete','','fr','Confirmez vous la suppression du Cas de Test <b>\\'%ENTRY%\\'</b> ?','',NULL)");
        b.append(",('page_testcase','tooltip_addAction','','en','Add Action','',NULL)");
        b.append(",('page_testcase','tooltip_addAction','','fr','Ajouter une Action',NULL,NULL)");
        b.append(",('page_testcase','tooltip_addControl','','en','Add Control','',NULL)");
        b.append(",('page_testcase','tooltip_addControl','','fr','Ajouter un contrôle',NULL,NULL)");
        b.append(",('page_testcase','tooltip_clicktocreate','','en','Property %P% is missing! Click to create a property!','',NULL)");
        b.append(",('page_testcase','tooltip_clicktocreate','','fr','Propriété %P% est manquante. Cliquer pour créer cette propriété',NULL,NULL)");
        b.append(",('page_testcase','tooltip_clicktooverride','','en','Property %P% is defined in the test: %T% - %TC% [step: %S%]. Click to override property!','',NULL)");
        b.append(",('page_testcase','tooltip_clicktooverride','','fr','Propriété %P% est définie dans le Test: %T% - %TC% [étape: %S%]. Cliquer pour réécrire cette propriété!',NULL,NULL)");
        b.append(",('page_testcase','tooltip_infooverriden','','en','Property %P% was overridden in the current Test Case. Original test case: %T% - %TC% [step: %S%]','',NULL)");
        b.append(",('page_testcase','tooltip_infooverriden','','fr','La propriété %P% a été réécrite dans ce cas de test. Cas de test original: %T% - %TC% [étape: %S%]',NULL,NULL)");
        b.append(",('page_testcase','tooltip_is_useStep','','en','This step can not be used as library, because it uses another step!','',NULL)");
        b.append(",('page_testcase','tooltip_is_useStep','','fr','Cette étape ne peut être utilisée en tant que librairie, parce qu\\'elle est utilisée par une autre étape',NULL,NULL)");
        b.append(",('page_testcase','tooltip_select_entry','','en','Select an entry from library','',NULL)");
        b.append(",('page_testcase','tooltip_select_entry','','fr','Selectionner une entrée depuis la librairie',NULL,NULL)");
        b.append(",('page_testcase','tooltip_step_used','','en','This step is being used by another step(s)!','',NULL)");
        b.append(",('page_testcase','tooltip_step_used','','fr','Cette étape est utilisée par une étape',NULL,NULL)");
        b.append(",('page_testcase','txt_property_not_defined','','en','** Property not defined **','',NULL)");
        b.append(",('page_testcase','txt_property_not_defined','','fr','** Propriété non définie **',NULL,NULL)");
        b.append(",('page_testcase','undefined_error_message','','en','There are undefined properties! Please check them before proceed.','',NULL)");
        b.append(",('page_testcase','undefined_error_message','','fr','Il y a des propriétés indéfinies, veuillez les vérifier avant de procéder',NULL,NULL)");
        b.append(",('page_testcaseexecution','table_executionlist','','en','Executions List',NULL,NULL)");
        b.append(",('page_testcaseexecution','table_executionlist','','fr','Liste des executions',NULL,NULL)");
        b.append(",('page_testcaseexecution','title','','en','Test Case Execution','',NULL)");
        b.append(",('page_testcaseexecution','title','','fr','Execution des Cas de Test','',NULL)");
        b.append(",('page_testcaseexecutionqueue','allExecution','','en','Executions in queue','',NULL)");
        b.append(",('page_testcaseexecutionqueue','allExecution','','fr','File d\\'exécutions','',NULL)");
        b.append(",('page_testcaseexecutionqueue','browserVersion_col','','en','Browser version','',NULL)");
        b.append(",('page_testcaseexecutionqueue','browserVersion_col','','fr','Version du navigateur','',NULL)");
        b.append(",('page_testcaseexecutionqueue','browser_col','','en','Browser','',NULL)");
        b.append(",('page_testcaseexecutionqueue','browser_col','','fr','Navigateur','',NULL)");
        b.append(",('page_testcaseexecutionqueue','button_filterExecuting','','en','Filter Executing',NULL,NULL)");
        b.append(",('page_testcaseexecutionqueue','button_filterExecuting','','fr','Filtrer En Execution',NULL,NULL)");
        b.append(",('page_testcaseexecutionqueue','button_filterPending','','en','Filter Pending',NULL,NULL)");
        b.append(",('page_testcaseexecutionqueue','button_filterPending','','fr','Filtrer En Attente',NULL,NULL)");
        b.append(",('page_testcaseexecutionqueue','comment_col','','en','Comment','',NULL)");
        b.append(",('page_testcaseexecutionqueue','comment_col','','fr','Commentaire','',NULL)");
        b.append(",('page_testcaseexecutionqueue','constrain','','en','Constrain',NULL,NULL)");
        b.append(",('page_testcaseexecutionqueue','constrain','','fr','Contrainte',NULL,NULL)");
        b.append(",('page_testcaseexecutionqueue','country_col','','en','Country','',NULL)");
        b.append(",('page_testcaseexecutionqueue','country_col','','fr','Pays','',NULL)");
        b.append(",('page_testcaseexecutionqueue','environment_col','','en','Environment','',NULL)");
        b.append(",('page_testcaseexecutionqueue','environment_col','','fr','Environnement','',NULL)");
        b.append(",('page_testcaseexecutionqueue','exeId','','en','Execution ID','',NULL)");
        b.append(",('page_testcaseexecutionqueue','exeId','','fr','ID de l\\'éxécution','',NULL)");
        b.append(",('page_testcaseexecutionqueue','id_col','','en','ID','',NULL)");
        b.append(",('page_testcaseexecutionqueue','id_col','','fr','ID','',NULL)");
        b.append(",('page_testcaseexecutionqueue','manualContextRoot_col','','en','Manual execution context root','',NULL)");
        b.append(",('page_testcaseexecutionqueue','manualContextRoot_col','','fr','Contexte racine de l\\'execution manuelle','',NULL)");
        b.append(",('page_testcaseexecutionqueue','manualEnvData_col','','en','Manual execution environment data','',NULL)");
        b.append(",('page_testcaseexecutionqueue','manualEnvData_col','','fr','Données d\\'environnement de l\\'execution manuelle','',NULL)");
        b.append(",('page_testcaseexecutionqueue','manualExecution_col','','en','Manual execution','',NULL)");
        b.append(",('page_testcaseexecutionqueue','manualExecution_col','','fr','Execution manuelle','',NULL)");
        b.append(",('page_testcaseexecutionqueue','manualHost_col','','en','Manual execution host','',NULL)");
        b.append(",('page_testcaseexecutionqueue','manualHost_col','','fr','Domaine de l\\'execution manuelle','',NULL)");
        b.append(",('page_testcaseexecutionqueue','manualLoginRelativeURL_col','','en','Manual execution login relative URL','',NULL)");
        b.append(",('page_testcaseexecutionqueue','manualLoginRelativeURL_col','','fr','URL relative de connexion pour l\\'execution manuelle','',NULL)");
        b.append(",('page_testcaseexecutionqueue','manualURL_col','','en','Manual execution URL','',NULL)");
        b.append(",('page_testcaseexecutionqueue','manualURL_col','','fr','URL de l\\'execution manuelle','',NULL)");
        b.append(",('page_testcaseexecutionqueue','nbInQueue','','en','Execution nb in the Queue',NULL,NULL)");
        b.append(",('page_testcaseexecutionqueue','nbInQueue','','fr','Nb d\\'éxécutions en queue',NULL,NULL)");
        b.append(",('page_testcaseexecutionqueue','nbPoolSize','','en','Max Pool Size',NULL,NULL)");
        b.append(",('page_testcaseexecutionqueue','nbPoolSize','','fr','Taille max du Pool',NULL,NULL)");
        b.append(",('page_testcaseexecutionqueue','nbRunning','','en','Execution nb Running',NULL,NULL)");
        b.append(",('page_testcaseexecutionqueue','nbRunning','','fr','Nb d\\'éxécutions en cours',NULL,NULL)");
        b.append(",('page_testcaseexecutionqueue','oversaturation','','en','Execution nb on saturated queue',NULL,NULL)");
        b.append(",('page_testcaseexecutionqueue','oversaturation','','fr','Nb d\\'éxécutions dans pool saturés',NULL,NULL)");
        b.append(",('page_testcaseexecutionqueue','pageSource_col','','en','Page source','',NULL)");
        b.append(",('page_testcaseexecutionqueue','pageSource_col','','fr','Code source','',NULL)");
        b.append(",('page_testcaseexecutionqueue','platform_col','','en','Platform','',NULL)");
        b.append(",('page_testcaseexecutionqueue','platform_col','','fr','Plateforme','',NULL)");
        b.append(",('page_testcaseexecutionqueue','processed_col','','en','Proceeded','',NULL)");
        b.append(",('page_testcaseexecutionqueue','processed_col','','fr','Traité','',NULL)");
        b.append(",('page_testcaseexecutionqueue','requestDate_col','','en','Last State Date','',NULL)");
        b.append(",('page_testcaseexecutionqueue','requestDate_col','','fr','Date du dernier Etat','',NULL)");
        b.append(",('page_testcaseexecutionqueue','retries_col','','en','Retries','',NULL)");
        b.append(",('page_testcaseexecutionqueue','retries_col','','fr','Tentatives','',NULL)");
        b.append(",('page_testcaseexecutionqueue','robotIP_col','','en','Robot host','',NULL)");
        b.append(",('page_testcaseexecutionqueue','robotIP_col','','fr','Domaine du Robot','',NULL)");
        b.append(",('page_testcaseexecutionqueue','robotPort_col','','en','Robot port','',NULL)");
        b.append(",('page_testcaseexecutionqueue','robotPort_col','','fr','Port du Robot','',NULL)");
        b.append(",('page_testcaseexecutionqueue','robot_col','','en','Robot','',NULL)");
        b.append(",('page_testcaseexecutionqueue','robot_col','','fr','Robot','',NULL)");
        b.append(",('page_testcaseexecutionqueue','saturation','','en','Saturation %',NULL,NULL)");
        b.append(",('page_testcaseexecutionqueue','saturation','','fr','% de saturation',NULL,NULL)");
        b.append(",('page_testcaseexecutionqueue','screenshot_col','','en','Screenshots','',NULL)");
        b.append(",('page_testcaseexecutionqueue','screenshot_col','','fr','Captures d\\'écrans','',NULL)");
        b.append(",('page_testcaseexecutionqueue','seleniumLog_col','','en','Selenium logs','',NULL)");
        b.append(",('page_testcaseexecutionqueue','seleniumLog_col','','fr','Journalisation Selenium','',NULL)");
        b.append(",('page_testcaseexecutionqueue','state_col','','en','State','',NULL)");
        b.append(",('page_testcaseexecutionqueue','state_col','','fr','Etat','',NULL)");
        b.append(",('page_testcaseexecutionqueue','tag_col','','en','Tag','',NULL)");
        b.append(",('page_testcaseexecutionqueue','tag_col','','fr','Tag','',NULL)");
        b.append(",('page_testcaseexecutionqueue','testcase_col','','en','Test Case','',NULL)");
        b.append(",('page_testcaseexecutionqueue','testcase_col','','fr','Cas de Test','',NULL)");
        b.append(",('page_testcaseexecutionqueue','test_col','','en','Test','',NULL)");
        b.append(",('page_testcaseexecutionqueue','test_col','','fr','Test','',NULL)");
        b.append(",('page_testcaseexecutionqueue','timeout_col','','en','Timeout','',NULL)");
        b.append(",('page_testcaseexecutionqueue','timeout_col','','fr','Dépassement de temps','',NULL)");
        b.append(",('page_testcaseexecutionqueue','tooltip_dupentry','','en','Duplicate Queue entry','',NULL)");
        b.append(",('page_testcaseexecutionqueue','tooltip_dupentry','','fr','Dupliquer l\\'éxécution en queue','',NULL)");
        b.append(",('page_testcaseexecutionqueue','tooltip_editentry','','en','Edit Queue entry','',NULL)");
        b.append(",('page_testcaseexecutionqueue','tooltip_editentry','','fr','Editer l\\'exécution en queue','',NULL)");
        b.append(",('page_testcaseexecutionqueue','tooltip_viewentry','','en','View Queue entry','',NULL)");
        b.append(",('page_testcaseexecutionqueue','tooltip_viewentry','','fr','Voir l\\'exécution en queue','',NULL)");
        b.append(",('page_testcaseexecutionqueue','verbose_col','','en','Verbose','',NULL)");
        b.append(",('page_testcaseexecutionqueue','verbose_col','','fr','Verbeux','',NULL)");
        b.append(",('page_testcaselist','activationCriteria','','en','Activation Criteria','',NULL)");
        b.append(",('page_testcaselist','activationCriteria','','fr','Critères d\\'activation','',NULL)");
        b.append(",('page_testcaselist','btn_create','','en','Create Test Case',NULL,NULL)");
        b.append(",('page_testcaselist','btn_create','','fr','Créer un Cas de Test',NULL,NULL)");
        b.append(",('page_testcaselist','btn_delete','','en','Delete Test Case','',NULL)");
        b.append(",('page_testcaselist','btn_delete','','fr','Supprimer le Cas de Test','',NULL)");
        b.append(",('page_testcaselist','btn_duplicate','','en','Duplicate Test Case','',NULL)");
        b.append(",('page_testcaselist','btn_duplicate','','fr','Dupliquer le Cas de Test','',NULL)");
        b.append(",('page_testcaselist','btn_edit','','en','Edit Test Case Header','',NULL)");
        b.append(",('page_testcaselist','btn_edit','','fr','Modifer l\\'Entete du Cas de Test','',NULL)");
        b.append(",('page_testcaselist','btn_editLabel','','en','Edit Test Case labels','',NULL)");
        b.append(",('page_testcaselist','btn_editLabel','','fr','Modifier les étiquettes du Cas de Test','',NULL)");
        b.append(",('page_testcaselist','btn_editScript','','en','Go to Test Case Script Page','',NULL)");
        b.append(",('page_testcaselist','btn_editScript','','fr','Aller à la page du Script du Cas de Test','',NULL)");
        b.append(",('page_testcaselist','btn_runTest','','en','Run Test Case','',NULL)");
        b.append(",('page_testcaselist','btn_runTest','','fr','Exécuter le Cas de Test','',NULL)");
        b.append(",('page_testcaselist','btn_view','','en','View Test Case','',NULL)");
        b.append(",('page_testcaselist','btn_view','','fr','Voir Cas de Test','',NULL)");
        b.append(",('page_testcaselist','filters','','en','Filters','Test filter',NULL)");
        b.append(",('page_testcaselist','filters','','fr','Filtres','Filtre des tests',NULL)");
        b.append(",('page_testcaselist','link','','en','Bug Link','',NULL)");
        b.append(",('page_testcaselist','link','','fr','Lien vers le Bug','',NULL)");
        b.append(",('page_testcaselist','massAction','','en','Massively update the selected test cases','',NULL)");
        b.append(",('page_testcaselist','massAction','','fr','Mise à jour en masse les Cas de Test selectionnés','',NULL)");
        b.append(",('page_testcaselist','tabLabel','','en','Label Management',NULL,NULL)");
        b.append(",('page_testcaselist','tabLabel','','fr','Gestion des Labels',NULL,NULL)");
        b.append(",('page_testcaselist','tabUpdate','','en','Update',NULL,NULL)");
        b.append(",('page_testcaselist','tabUpdate','','fr','Mise à jour',NULL,NULL)");
        b.append(",('page_testcaselist','testCaseInfo','','en','Test Case Info','',NULL)");
        b.append(",('page_testcaselist','testCaseInfo','','fr','Test Case Info','',NULL)");
        b.append(",('page_testcaselist','testcaselist','','en','Test Case List','List of all the test case of the selected test',NULL)");
        b.append(",('page_testcaselist','testcaselist','','fr','Liste des Cas de Test','Liste de tout les cas de test du test selectionné',NULL)");
        b.append(",('page_testcaselist','testCaseParameter','','en','Test Case Parameter','',NULL)");
        b.append(",('page_testcaselist','testCaseParameter','','fr','Parametres du Cas de Test','',NULL)");
        b.append(",('page_testcaselist','testInfo','','en','Test Info','',NULL)");
        b.append(",('page_testcaselist','testInfo','','fr','Test Info','',NULL)");
        b.append(",('page_testcasescript','action_field','','en','Action','',NULL)");
        b.append(",('page_testcasescript','action_field','','fr','Action','',NULL)");
        b.append(",('page_testcasescript','add_action','','en','Add Action','',NULL)");
        b.append(",('page_testcasescript','add_action','','fr','Ajouter une Action','',NULL)");
        b.append(",('page_testcasescript','add_step','','en','Add step','',NULL)");
        b.append(",('page_testcasescript','add_step','','fr','Ajouter une Etape','',NULL)");
        b.append(",('page_testcasescript','cant_detach_library','','en','You can\\'t detach this library because it is used in these steps : ','',NULL)");
        b.append(",('page_testcasescript','cant_detach_library','','fr','Vous ne pouvez pas détacher cette librairie car elle est utilisée dans ces étapes : ','',NULL)");
        b.append(",('page_testcasescript','condition_operation_field','','en','Condition Operation','',NULL)");
        b.append(",('page_testcasescript','condition_operation_field','','fr','Condition d execution','',NULL)");
        b.append(",('page_testcasescript','condition_parameter_field','','en','Condition Parameter','',NULL)");
        b.append(",('page_testcasescript','condition_parameter_field','','fr','Paramètre de la condition','',NULL)");
        b.append(",('page_testcasescript','control_field','','en','Control','',NULL)");
        b.append(",('page_testcasescript','control_field','','fr','Control','',NULL)");
        b.append(",('page_testcasescript','db_field','','en','Database','',NULL)");
        b.append(",('page_testcasescript','db_field','','fr','Base de donnée','',NULL)");
        b.append(",('page_testcasescript','delete','','en','Delete','',NULL)");
        b.append(",('page_testcasescript','delete','','fr','Supprimer','',NULL)");
        b.append(",('page_testcasescript','describe_action','','en','Describe Action','',NULL)");
        b.append(",('page_testcasescript','describe_action','','fr','Décrivez cette action','',NULL)");
        b.append(",('page_testcasescript','describe_control','','en','Describe Control','',NULL)");
        b.append(",('page_testcasescript','describe_control','','fr','Décrivez ce control','',NULL)");
        b.append(",('page_testcasescript','description_field','','en','Description','',NULL)");
        b.append(",('page_testcasescript','description_field','','fr','Description','',NULL)");
        b.append(",('page_testcasescript','edit_testcase','','en','Edit TestCase Header','',NULL)");
        b.append(",('page_testcasescript','edit_testcase','','fr','Modifier l\\'Entete du TestCase','',NULL)");
        b.append(",('page_testcasescript','fatal_field','','en','Fatal','',NULL)");
        b.append(",('page_testcasescript','fatal_field','','fr','Fatal','',NULL)");
        b.append(",('page_testcasescript','feed_propertydescription','','en','Feed Property Description','',NULL)");
        b.append(",('page_testcasescript','feed_propertydescription','','fr','Remplissez la description','',NULL)");
        b.append(",('page_testcasescript','feed_propertyname','','en','Feed Property name','',NULL)");
        b.append(",('page_testcasescript','feed_propertyname','','fr','Remplissez le nom de la propriété','',NULL)");
        b.append(",('page_testcasescript','force_execution_field','','en','Force Execution','',NULL)");
        b.append(",('page_testcasescript','force_execution_field','','fr','Forcer l\\'execution','',NULL)");
        b.append(",('page_testcasescript','goto','','en','Go To','',NULL)");
        b.append(",('page_testcasescript','goto','','fr','Aller','',NULL)");
        b.append(",('page_testcasescript','imported_from','','en','Imported from','',NULL)");
        b.append(",('page_testcasescript','imported_from','','fr','Importé depuis','',NULL)");
        b.append(",('page_testcasescript','index','','en','Index','',NULL)");
        b.append(",('page_testcasescript','index','','fr','Index','',NULL)");
        b.append(",('page_testcasescript','length','','en','Length','',NULL)");
        b.append(",('page_testcasescript','length','','fr','Taille','',NULL)");
        b.append(",('page_testcasescript','length_field','','en','Length','',NULL)");
        b.append(",('page_testcasescript','length_field','','fr','Taille','',NULL)");
        b.append(",('page_testcasescript','manage_prop','','en','Manage Properties','',NULL)");
        b.append(",('page_testcasescript','manage_prop','','fr','Gestion des Propriétés','',NULL)");
        b.append(",('page_testcasescript','nature_field','','en','Nature','',NULL)");
        b.append(",('page_testcasescript','nature_field','','fr','Nature','',NULL)");
        b.append(",('page_testcasescript','not_application_object','','en','It is not an Application Object','',NULL)");
        b.append(",('page_testcasescript','not_application_object','','fr','Ce n\\'est pas un object de l\\'application','',NULL)");
        b.append(",('page_testcasescript','not_property','','en','It is not a property','',NULL)");
        b.append(",('page_testcasescript','not_property','','fr','Ce n\\'est pas une propriété','',NULL)");
        b.append(",('page_testcasescript','property_field','','en','Property','',NULL)");
        b.append(",('page_testcasescript','property_field','','fr','Propriété','',NULL)");
        b.append(",('page_testcasescript','rc','','en','Result Code','',NULL)");
        b.append(",('page_testcasescript','rc','','fr','Code Retour','',NULL)");
        b.append(",('page_testcasescript','rerunqueue_testcase','','en','Run From Queue','',NULL)");
        b.append(",('page_testcasescript','rerunqueue_testcase','','fr','Executer de la Queue','',NULL)");
        b.append(",('page_testcasescript','rerun_testcase','','en','Rerun TestCase','',NULL)");
        b.append(",('page_testcasescript','rerun_testcase','','fr','ReExecuter le TestCase','',NULL)");
        b.append(",('page_testcasescript','retrynb','','en','Retry Number','',NULL)");
        b.append(",('page_testcasescript','retrynb','','fr','Nombre d\\'essais','',NULL)");
        b.append(",('page_testcasescript','retryperiod','','en','Retry Period','',NULL)");
        b.append(",('page_testcasescript','retryperiod','','fr','Periode d\\'essai','',NULL)");
        b.append(",('page_testcasescript','rMessage','','en','Result Message','',NULL)");
        b.append(",('page_testcasescript','rMessage','','fr','Message de Retour','',NULL)");
        b.append(",('page_testcasescript','rowlimit_field','','en','Row limit','',NULL)");
        b.append(",('page_testcasescript','rowlimit_field','','fr','Nombre de lignes limite','',NULL)");
        b.append(",('page_testcasescript','row_limit','','en','Row Limit','',NULL)");
        b.append(",('page_testcasescript','row_limit','','fr','Nombre de lignes limite','',NULL)");
        b.append(",('page_testcasescript','run','','en','Run','',NULL)");
        b.append(",('page_testcasescript','run','','fr','Executer','',NULL)");
        b.append(",('page_testcasescript','run_old','','en','Old page','',NULL)");
        b.append(",('page_testcasescript','run_old','','fr','Ancienne page','',NULL)");
        b.append(",('page_testcasescript','run_testcase','','en','Run TestCase','',NULL)");
        b.append(",('page_testcasescript','run_testcase','','fr','Executer le TestCase','',NULL)");
        b.append(",('page_testcasescript','saveas_script','','en','Save As','',NULL)");
        b.append(",('page_testcasescript','saveas_script','','fr','Sauvergarder Sous','',NULL)");
        b.append(",('page_testcasescript','save_script','','en','Save Test Case','',NULL)");
        b.append(",('page_testcasescript','save_script','','fr','Sauvegarder TestCase','',NULL)");
        b.append(",('page_testcasescript','see_lastexec','','en','Executions List','',NULL)");
        b.append(",('page_testcasescript','see_lastexec','','fr','Liste d\\'Exécutions','',NULL)");
        b.append(",('page_testcasescript','see_lastexecuniq','','en','Last Execution','',NULL)");
        b.append(",('page_testcasescript','see_lastexecuniq','','fr','Dernière Execution','',NULL)");
        b.append(",('page_testcasescript','see_logs','','en','Logs','',NULL)");
        b.append(",('page_testcasescript','see_logs','','fr','Logs','',NULL)");
        b.append(",('page_testcasescript','see_test','','en','Test Case List','',NULL)");
        b.append(",('page_testcasescript','see_test','','fr','Liste des TestCases','',NULL)");
        b.append(",('page_testcasescript','select_test','','en','Select a test','',NULL)");
        b.append(",('page_testcasescript','select_test','','fr','Sélectionner un test','',NULL)");
        b.append(",('page_testcasescript','select_testcase','','en','Select a TestCase','',NULL)");
        b.append(",('page_testcasescript','select_testcase','','fr','Sélectionner un TestCase','',NULL)");
        b.append(",('page_testcasescript','steps_title','','en','Steps','',NULL)");
        b.append(",('page_testcasescript','steps_title','','fr','Etapes','',NULL)");
        b.append(",('page_testcasescript','step_condition_operation','','en','Step Condition Operation','',NULL)");
        b.append(",('page_testcasescript','step_condition_operation','','fr','Condition d\\'exécution de l\\'étape','',NULL)");
        b.append(",('page_testcasescript','step_condition_value1','','en','Step Condition Parameter','',NULL)");
        b.append(",('page_testcasescript','step_condition_value1','','fr','Paramètre de condition','',NULL)");
        b.append(",('page_testcasescript','testcasescript_title','','en','Test Case Script','',NULL)");
        b.append(",('page_testcasescript','testcasescript_title','','fr','Script du Test Case','',NULL)");
        b.append(",('page_testcasescript','type_field','','en','Type','',NULL)");
        b.append(",('page_testcasescript','type_field','','fr','Type','',NULL)");
        b.append(",('page_testcasescript','unlink_useStep','','en','Unlink Used step','',NULL)");
        b.append(",('page_testcasescript','unlink_useStep','','fr','Délier l\\'étape de sa librairie','',NULL)");
        b.append(",('page_testcasescript','unlink_useStep_warning','','en','Are you sure you want to unlink this used step ? You can\\'t undo this.','',NULL)");
        b.append(",('page_testcasescript','unlink_useStep_warning','','fr','Etes vous sur de vouloir délier cette étape de sa librairie ? Cette action n\\'est pas annulable.','',NULL)");
        b.append(",('page_testcasescript','value1init_field','','en','Value 1 Initial','',NULL)");
        b.append(",('page_testcasescript','value1init_field','','fr','Valeur 1 Initiale','',NULL)");
        b.append(",('page_testcasescript','value1_field','','en','Value 1','',NULL)");
        b.append(",('page_testcasescript','value1_field','','fr','Value 1','',NULL)");
        b.append(",('page_testcasescript','value2init_field','','en','Value 2 Initial','',NULL)");
        b.append(",('page_testcasescript','value2init_field','','fr','Valeur 2 Initiale','',NULL)");
        b.append(",('page_testcasescript','value2_field','','en','Value 2','',NULL)");
        b.append(",('page_testcasescript','value2_field','','fr','Value 2','',NULL)");
        b.append(",('page_testcasescript','value_field','','en','Value','',NULL)");
        b.append(",('page_testcasescript','value_field','','fr','Valeur','',NULL)");
        b.append(",('page_testcasescript','warning_nocountry','','en','That Testcase has no country selected, please add at east one country (in \\'Activation Criteria\\' Tab of the testcase) in order to be able to add and define properties.','',NULL)");
        b.append(",('page_testcasescript','warning_nocountry','','fr','Ce Cas de Test n\\'a auncu pays de défini. Merci d\\'en ajouter en minimum 1 (depuis \\'Activation Criteria\\' au niveau de l\\'entete du cas de test) avant d\\'ajouter une propriété.','',NULL)");
        b.append(",('page_testcasescript','warning_no_country','','en','There is no country selected for at least one property. If you save it will be removed. Do you still want to save ?','',NULL)");
        b.append(",('page_testcasescript','warning_no_country','','fr','Il y a au moins une propriété sans pays de selectionné. Si vous sauvegardez elle sera supprimée. Voulez-vous vraiment sauvegarder ?','',NULL)");
        b.append(",('page_testcasescript','warning_one_empty_prop','','en','There is at least one property with no name defined. If you save it will be removed. Do you still want to save ?','',NULL)");
        b.append(",('page_testcasescript','warning_one_empty_prop','','fr','Il y a au moins une propriétée sans pays. Si vous sauvegardez elle sera supprimée. Voulez-vous vraiment sauvegarder ?','',NULL)");
        b.append(",('page_testcasesearch','text','','en','Text','Insert here the text that will search against the following Fields of every <code class=\\'doc-crbvvoca\\'>test case</code> :<br>- Short Description,<br>- Detailed description / Value Expected,<br>- HowTo<br>- Comment<br><br>NB : Search is case insensitive.',NULL)");
        b.append(",('page_testcasesearch','text','','fr','Texte',NULL,NULL)");
        b.append(",('page_testcase_m_addPicture','error_message_empty','','en','The URL value is empty!','',NULL)");
        b.append(",('page_testcase_m_addPicture','error_message_empty','','fr','La valeur de l\\'URL est vide',NULL,NULL)");
        b.append(",('page_testcase_m_addPicture','lbl_feedurl','','en','Feed URL','',NULL)");
        b.append(",('page_testcase_m_addPicture','lbl_feedurl','','fr','Fournir URL',NULL,NULL)");
        b.append(",('page_testcase_m_addPicture','title','','en','Add URL for picture','',NULL)");
        b.append(",('page_testcase_m_showPicture','btn_remove','','en','Delete','',NULL)");
        b.append(",('page_testcase_m_showPicture','btn_remove','','fr','Retirer',NULL,NULL)");
        b.append(",('page_testcase_m_showPicture','title','','en','Selected Picture','',NULL)");
        b.append(",('page_testdatalib','btn_create','','en','Create New Data Library','',NULL)");
        b.append(",('page_testdatalib','btn_create','','fr','Créer une nouvelle entrée',NULL,NULL)");
        b.append(",('page_testdatalib','btn_import','','en','Import from XML file','',NULL)");
        b.append(",('page_testdatalib','btn_import','','fr','Importer d\\'un fichier XML',NULL,NULL)");
        b.append(",('page_testdatalib','duplicated_message','','en','Please check the subdata entries. There are entries with duplicated names, a total of: ','',NULL)");
        b.append(",('page_testdatalib','duplicated_message','','fr','Veuillez vérifier les sous données, il y a au moins un doublon de nom',NULL,NULL)");
        b.append(",('page_testdatalib','empty_name_message','','en','Please specify the name of the Data Library !','',NULL)");
        b.append(",('page_testdatalib','empty_name_message','','fr','Veuillez spécifier le nom de l\\'entrée',NULL,NULL)");
        b.append(",('page_testdatalib','empty_subdata_message','','en','Please check the subdata entries. There are entries without sub-data name, a total of: ','',NULL)");
        b.append(",('page_testdatalib','empty_subdata_message','','fr','Veuillez vérifier s\\'il vous plait les sous données, il manque au moins un nom d\\'entrée',NULL,NULL)");
        b.append(",('page_testdatalib','message_delete','','en','Do you want to delete Test Data Library <b>\\'%ENTRY%\\'</b> <br>of system <b>\\'%SYSTEM%\\'</b>, country <b>\\'%COUNTRY%\\'</b> and environment <b>\\'%ENVIRONMENT%\\'</b> (ID : %ID%) ?','',NULL)");
        b.append(",('page_testdatalib','message_delete','','fr','Confirmez-vous la suppression de la donnée de test <b>\\'%ENTRY%\\'</b> <br>du système <b>\\'%SYSTEM%\\'</b>, pays <b>\\'%COUNTRY%\\'</b> et environnement <b>\\'%ENVIRONMENT%\\'</b> (ID : %ID%) ?','',NULL)");
        b.append(",('page_testdatalib','m_tab1_text','','en','Definition','',NULL)");
        b.append(",('page_testdatalib','m_tab1_text','','fr','Defintion','',NULL)");
        b.append(",('page_testdatalib','m_tab2_text','','en','SubData','',NULL)");
        b.append(",('page_testdatalib','m_tab2_text','','fr','SubData','',NULL)");
        b.append(",('page_testdatalib','m_tab3_text','','en','Tracability','',NULL)");
        b.append(",('page_testdatalib','m_tab3_text','','fr','Traçabité','',NULL)");
        b.append(",('page_testdatalib','page_title','','en','Test Data Library','',NULL)");
        b.append(",('page_testdatalib','page_title','','fr','Librairie de données de tests',NULL,NULL)");
        b.append(",('page_testdatalib','table_testdatalib','','en','Test Data List',NULL,NULL)");
        b.append(",('page_testdatalib','table_testdatalib','','fr','Liste de Données',NULL,NULL)");
        b.append(",('page_testdatalib','title','','en','Test Data Library','<p>The <u>Test Data Library</u> is a repository of test data that centralises and eases the Test Data Management process. Additionally, it eases the creation of test cases because it allows the reuse of data that is recurrently defined. </p> <p>Cerberus allows the definition of three types of entries: <b>STATIC</b>, <b>SQL</b> and <b>SERVICE</b>.</p><p>The definition of each library entry comprises two steps: <ul><li>The definition of the library entry. </li><li>The definition of its sub-data entries.</li></ul></p>','_data_library')");
        b.append(",('page_testdatalib','title','','fr','Librairie de données',NULL,'_librairie_de_données')");
        b.append(",('page_testdatalib','title_csv_configurations','','en','CSV configurations','',NULL)");
        b.append(",('page_testdatalib','title_csv_configurations','','fr','Configuration CSV','',NULL)");
        b.append(",('page_testdatalib','title_service_configurations','','en','SERVICE configurations','',NULL)");
        b.append(",('page_testdatalib','title_service_configurations','','fr','SERVICE configurations',NULL,NULL)");
        b.append(",('page_testdatalib','title_soap_configurations','','fr','Configurations SOAP',NULL,NULL)");
        b.append(",('page_testdatalib','title_sql_configurations','','en','SQL configurations','',NULL)");
        b.append(",('page_testdatalib','title_sql_configurations','','fr','Configurations SQL',NULL,NULL)");
        b.append(",('page_testdatalib','tooltip_delete','','en','Delete Data Library','',NULL)");
        b.append(",('page_testdatalib','tooltip_delete','','fr','Supprimer cette entrée',NULL,NULL)");
        b.append(",('page_testdatalib','tooltip_duplicateEntry','','en','Duplicate this Data Library','',NULL)");
        b.append(",('page_testdatalib','tooltip_duplicateEntry','','fr','Dupliquer cette entrée',NULL,NULL)");
        b.append(",('page_testdatalib','tooltip_editentry','','en','Edit Data Library','',NULL)");
        b.append(",('page_testdatalib','tooltip_editentry','','fr','Editer l\\'entrée',NULL,NULL)");
        b.append(",('page_testdatalib','tooltip_editsubdata','','en','Edit sub-data entries','',NULL)");
        b.append(",('page_testdatalib','tooltip_editsubdata','','fr','Editer les entrées des sous-données',NULL,NULL)");
        b.append(",('page_testdatalib','tooltip_gettestcases','','en','Get list of test cases that use this entry','',NULL)");
        b.append(",('page_testdatalib','tooltip_gettestcases','','fr','Voir la liste des cas de tests qui utilisent cette entrée',NULL,NULL)");
        b.append(",('page_testdatalib','tooltip_viewsubdata','','en','View sub-data entries','',NULL)");
        b.append(",('page_testdatalib','tooltip_viewsubdata','','fr','Voir les entrées des sous données',NULL,NULL)");
        b.append(",('page_testdatalib_delete','title','','en','Delete Test Data Library Entry','',NULL)");
        b.append(",('page_testdatalib_m_createlib','title','','en','Create New Test Data Library Entry','',NULL)");
        b.append(",('page_testdatalib_m_duplicatelib','title','','en','Duplicate Test Data Library','',NULL)");
        b.append(",('page_testdatalib_m_gettestcases','group_title','','en','Test cases affected by this entry','',NULL)");
        b.append(",('page_testdatalib_m_gettestcases','group_title','','fr','Cas de test affectés par cette entrée',NULL,NULL)");
        b.append(",('page_testdatalib_m_gettestcases','nrProperties','','en','#properties:','',NULL)");
        b.append(",('page_testdatalib_m_gettestcases','nrProperties','','fr','#propriétés:',NULL,NULL)");
        b.append(",('page_testdatalib_m_gettestcases','nrTestCases','','en','#test cases:','',NULL)");
        b.append(",('page_testdatalib_m_gettestcases','nrTestCases','','fr','#cas de tests:',NULL,NULL)");
        b.append(",('page_testdatalib_m_gettestcases','nrTests','','en','#tests: ','',NULL)");
        b.append(",('page_testdatalib_m_gettestcases','nrTests','','fr','#tests: ',NULL,NULL)");
        b.append(",('page_testdatalib_m_gettestcases','title','','en','List of test cases affected by this entry','',NULL)");
        b.append(",('page_testdatalib_m_listtestdatalibdata','title','','en','List of sub-data entries','',NULL)");
        b.append(",('page_testdatalib_m_managetestdatalibdata','actions','','en','Actions','<p> List of available actions  for the current user: </p><p><table border=\\'1\\'><tr><th class=\\'ex\\'>Button</th><th class=\\'ex\\'>Function</th><th class=\\'ex\\'>Description</th></tr><tr><td><span class=\\'glyphicon glyphicon-trash\\'></span></span></td><td>Delete</td><td>Allows the user to delete the sub-data entry. If the sub-data entry is a new, then it will be removed from the GUI. If the sub-data entry was loaded from the database, then it will be marked as to be deleted from the database.</td></tr><tr><td><span class=\\'glyphicon glyphicon-remove\\'></span></td><td>Mark to be deleted</td><td>Marks the sub-data entry to be deleted from the database.</td></tr></table></p>',NULL)");
        b.append(",('page_testdatalib_m_managetestdatalibdata','link_add_new','','en','Add new sub-data','',NULL)");
        b.append(",('page_testdatalib_m_managetestdatalibdata','link_add_new','','fr','Ajouter une nouvelle sous-donnée',NULL,NULL)");
        b.append(",('page_testdatalib_m_managetestdatalibdata','link_add_new_title','','en','Add a new row in the list of sub-data entries','',NULL)");
        b.append(",('page_testdatalib_m_managetestdatalibdata','link_add_new_title','','fr','Ajouter une nouvelle ligne dans la liste des sous-données',NULL,NULL)");
        b.append(",('page_testdatalib_m_managetestdatalibdata','title','','en','Manage list of sub-data entries','',NULL)");
        b.append(",('page_testdatalib_m_managetestdatalibdata','tooltip_defaultsubdata','','en','This is the default sub-data entry. It cannot be deleted nor its name can be modified.','',NULL)");
        b.append(",('page_testdatalib_m_managetestdatalibdata','tooltip_defaultsubdata','','fr','C\\'est l\\'entrée de la sous-donnée par defaut, ne peut être modifiée ni supprimée',NULL,NULL)");
        b.append(",('page_testdatalib_m_updatelib','title','','en','Edit Test Data Library Entry','',NULL)");
        b.append(",('page_user','adduser_field','','en','Add User','',NULL)");
        b.append(",('page_user','adduser_field','','fr','Ajouter un utilisateur','',NULL)");
        b.append(",('page_user','add_btn','','en','Add','',NULL)");
        b.append(",('page_user','add_btn','','fr','Ajouter','',NULL)");
        b.append(",('page_user','allUsers','','en','Users','',NULL)");
        b.append(",('page_user','allUsers','','fr','Utilisateurs','',NULL)");
        b.append(",('page_user','button_col','','en','Actions','',NULL)");
        b.append(",('page_user','button_col','','fr','Actions','',NULL)");
        b.append(",('page_user','button_create','','en','Create User','',NULL)");
        b.append(",('page_user','button_create','','fr','Créer un Utilisateur','',NULL)");
        b.append(",('page_user','button_edit','','en','Edit User','',NULL)");
        b.append(",('page_user','button_edit','','fr','Editer l\\'Utilisateur','',NULL)");
        b.append(",('page_user','button_remove','','en','Delete User','',NULL)");
        b.append(",('page_user','button_remove','','fr','Supprimer l\\'Utilisateur','',NULL)");
        b.append(",('page_user','close_btn','','en','Close','',NULL)");
        b.append(",('page_user','close_btn','','fr','Fermer','',NULL)");
        b.append(",('page_user','defaultsystem_col','','en','Default System','',NULL)");
        b.append(",('page_user','defaultsystem_col','','fr','Système par défaut','',NULL)");
        b.append(",('page_user','defaultsystem_field','','en','Default System','',NULL)");
        b.append(",('page_user','defaultsystem_field','','fr','Système par défaut','',NULL)");
        b.append(",('page_user','edituser_field','','en','Edit User','',NULL)");
        b.append(",('page_user','edituser_field','','fr','Modifier l\\'Utilisateur','',NULL)");
        b.append(",('page_user','email_col','','en','Email','',NULL)");
        b.append(",('page_user','email_col','','fr','Email','',NULL)");
        b.append(",('page_user','email_field','','en','Email','',NULL)");
        b.append(",('page_user','email_field','','fr','Email','',NULL)");
        b.append(",('page_user','groups_col','','en','Groups','',NULL)");
        b.append(",('page_user','groups_col','','fr','Groupes','',NULL)");
        b.append(",('page_user','groups_field','','en','Groups','',NULL)");
        b.append(",('page_user','groups_field','','fr','Groupes','',NULL)");
        b.append(",('page_user','information_tab','','en','Information','',NULL)");
        b.append(",('page_user','information_tab','','fr','Information','',NULL)");
        b.append(",('page_user','login_col','','en','Login','',NULL)");
        b.append(",('page_user','login_col','','fr','Identifiant','',NULL)");
        b.append(",('page_user','login_field','','en','Login','',NULL)");
        b.append(",('page_user','login_field','','fr','Identifiant','',NULL)");
        b.append(",('page_user','message_remove','','en','Are you sure?','',NULL)");
        b.append(",('page_user','message_remove','','fr','Êtes-vous sûrs?','',NULL)");
        b.append(",('page_user','name_col','','en','Name','',NULL)");
        b.append(",('page_user','name_col','','fr','Nom','',NULL)");
        b.append(",('page_user','name_field','','en','Name','',NULL)");
        b.append(",('page_user','name_field','','fr','Nom','',NULL)");
        b.append(",('page_user','request_col','','en','Request','',NULL)");
        b.append(",('page_user','request_col','','fr','Requête','',NULL)");
        b.append(",('page_user','request_field','','en','Request','',NULL)");
        b.append(",('page_user','request_field','','fr','Requête','',NULL)");
        b.append(",('page_user','save_btn','','en','Save','',NULL)");
        b.append(",('page_user','save_btn','','fr','Sauvegarder','',NULL)");
        b.append(",('page_user','systems_col','','en','Systems','',NULL)");
        b.append(",('page_user','systems_col','','fr','Systèmes','',NULL)");
        b.append(",('page_user','systems_field','','en','System','',NULL)");
        b.append(",('page_user','systems_field','','fr','Système','',NULL)");
        b.append(",('page_user','systems_tab','','en','Systems','',NULL)");
        b.append(",('page_user','systems_tab','','fr','Systèmes','',NULL)");
        b.append(",('page_user','table_user','','en','Users',NULL,NULL)");
        b.append(",('page_user','table_user','','fr','Utilisateurs',NULL,NULL)");
        b.append(",('page_user','team_col','','en','Team','',NULL)");
        b.append(",('page_user','team_col','','fr','Equipe','',NULL)");
        b.append(",('page_user','team_field','','en','Team','',NULL)");
        b.append(",('page_user','team_field','','fr','Equipe','',NULL)");
        b.append(",('page_user','title_remove','','en','Delete User','',NULL)");
        b.append(",('page_user','title_remove','','fr','Supprimer l\\'Utilisateur','',NULL)");
        b.append(",('project','active','','en','Active','This is a boolean that define if the project is active or not.',NULL)");
        b.append(",('project','active','','fr','Actif','Booléen qui défini si un projet est actif.',NULL)");
        b.append(",('project','code','','en','Code','This is the code of the project. ',NULL)");
        b.append(",('project','code','','fr','Code','Code du projet.',NULL)");
        b.append(",('project','dateCreation','','en','Created','This is the date when the project has been created.',NULL)");
        b.append(",('project','dateCreation','','fr','Date de Création','Date de création du projet.',NULL)");
        b.append(",('project','description','','en','Description','This is the description of the project',NULL)");
        b.append(",('project','description','','fr','Description','Description du projet',NULL)");
        b.append(",('project','idproject','','en','Project','This is the id of the project that provided the implementation of the <code class=\\'doc-crbvvoca\\'>test case</code>.',NULL)");
        b.append(",('project','idproject','','fr','Projet','Identifiant du projet qui est à l\\'origine de la création d\\'un <code class=\\'doc-crbvvoca\\'>cas de test</code>.',NULL)");
        b.append(",('robot','active','','en','Active','Define if the robot is active or not.',NULL)");
        b.append(",('robot','active','','fr','Actif','Defini si le robot est actif ou non.',NULL)");
        b.append(",('robot','browser','','en','Browser','Broswer of the robot.',NULL)");
        b.append(",('robot','browser','','fr','Navigateur','Navitateur du robot.',NULL)");
        b.append(",('robot','capabilityCapability','','en','Capability','Capability name.',NULL)");
        b.append(",('robot','capabilityCapability','','fr','Capabilité','Nom de la capabilité.',NULL)");
        b.append(",('robot','capabilityValue','','en','Value','Capability value.',NULL)");
        b.append(",('robot','capabilityValue','','fr','Valeur','Valeur de la capabilité.',NULL)");
        b.append(",('robot','description','','en','Description','Robot Description.',NULL)");
        b.append(",('robot','description','','fr','Description','Description du robot.',NULL)");
        b.append(",('robot','host','','en','Hostname','IP Adress or host that host the selenium server that will execute the test case.',NULL)");
        b.append(",('robot','host','','fr','Hostname','Address IP ou nom de server qui heberge le robot et qui sera utilisé lors l\\'excution du cas de test.',NULL)");
        b.append(",('robot','platform','','en','Platform','Operating system of the robot.',NULL)");
        b.append(",('robot','platform','','fr','Platforme','System d\\'exploitation du robot.',NULL)");
        b.append(",('robot','port','','en','Port number','Port number of the robot.',NULL)");
        b.append(",('robot','port','','fr','Numero de port','Numero du port à utiliser pour acceder au Root.',NULL)");
        b.append(",('robot','robot','','en','Robot','Name of the Robot. A robot define the server that will execute an automated test case. It can be used when starting an execution without having to feed information such as host, port platform or browser.',NULL)");
        b.append(",('robot','robot','','fr','Robot','Nom du Robot. Le Robot est le serveur en charge de l\\'execution d\\'un test automatisé. Il permet de lancer une execution de test sans avoir à renseigner l\\'IP, le port, le navigateur ou OS à utiliser.',NULL)");
        b.append(",('robot','robotdecli','','en','Robot Declination','The Robot Declination is attached to each execution that is executed with that robot. It is displayed (along with environment and country) in reportByTag page. If empty, declination will take the name of the robot. it support the following variables : %SCREENSIZE%, %BROWSER%, %BROWSERVERSION%, %PLATFORM%.',NULL)");
        b.append(",('robot','robotdecli','','fr','Declinaison','La Declinaison est attachée à chaque execution qui a utilisé le robot. Elle est affichée avec l\\'environnement et le pays dans la page de Report par Tag. Prend la valeur du nom du robot si vide. Elle supporte les variables suivantes : %SCREENSIZE%, %BROWSER%, %BROWSERVERSION%, %PLATFORM%.',NULL)");
        b.append(",('robot','robotID','','en','Robot ID','Technical identifier of the Robot.',NULL)");
        b.append(",('robot','robotID','','fr','ID du Robot','Identifiant technique invariant du Robot.',NULL)");
        b.append(",('robot','screensize','','en','Screen Size','This is the size of the browser screen that will be set for the execution.<br><br>Default Values are set inside the invariant SCREENSIZE that can be configured on Edit Public invariant screen..<br>Value must be two Integer splitted by a <b>*</b> mark.<br><i>For Exemple : 1024*768</i><br><br>If you need to add other Values, please contact your Cerberus Administrator.',NULL)");
        b.append(",('robot','screensize','','fr','Taille d\\'écran','Cette valeur correspond à la taille d\\'écran qui sera utilisé lors de l\\'execution.<br><br>Les valeurs sont définies dans la table d\\'invariant et peuvent être complétées si besoin via la page d\\'invariant.<br>Les valeur doivent être deux entiers séparé par une <b>*</b>.<br><i>Par Example : 1024*768</i><br><br>Pour ajouter de nouvelles valeurs, contactez votre administrateur Cerberus.',NULL)");
        b.append(",('robot','useragent','','en','User Agent','User Agent of the robot.',NULL)");
        b.append(",('robot','useragent','','fr','User Agent','User Agent du Robot.',NULL)");
        b.append(",('robot','version','','en','Version','Brower Version of the robot.',NULL)");
        b.append(",('robot','version','','fr','Version','Version du navigateur du Robot.',NULL)");
        b.append(",('test','Active','','en','Active','Define if the <code class=\\'doc-crbvvoca\\'>test</code> is active.<br>If <code class=\\'doc-crbvvoca\\'>test</code> is not active, no execution is possible on any of the associated <code class=\\'doc-crbvvoca\\'>test case</code>.','_test')");
        b.append(",('test','Active','','fr','Actif','Booléen qui définit si le <code class=\\'doc-crbvvoca\\'>test</code> est actif.<br>Si le <code class=\\'doc-crbvvoca\\'>test</code> n\\'est pas actif, aucune execution des <code class=\\'doc-crbvvoca\\'>Cas de tests</code> n\\'est possible.','_test')");
        b.append(",('test','Automated','','en','Automated','Define if the test is automated or not.','_test')");
        b.append(",('test','Automated','','fr','Automatisé','Boléen qui définit si le test est automatisé ou non','_test')");
        b.append(",('test','dateCreation','','en','Creation date','The date when the test have been created','_test')");
        b.append(",('test','dateCreation','','fr','Date de création','Date à laquelle le test a été créé','_test')");
        b.append(",('test','Description','','en','Test Description','This is the description of the <code class=\\'doc-crbvvoca\\'>test</code>.','_test')");
        b.append(",('test','Description','','fr','Description du test','Description du <code class=\\'doc-crbvvoca\\'>test</code>.','_test')");
        b.append(",('test','Test','','en','Test','A <code class=\\'doc-crbvvoca\\'>test</code> is grouping some <code class=\\'doc-crbvvoca\\'>test case</code> together. The criteria that groups the <code class=\\'doc-crbvvoca\\'>test cases</code> can be an application page or a feature.','_test')");
        b.append(",('test','Test','','fr','Test','Un <code class=\\'doc-crbvvoca\\'>test</code> regroupe plusieurs <code class=\\'doc-crbvvoca\\'>Cas de tests</code> ensemble.','_test')");
        b.append(",('testcampaign','distribList','','en','Distribution List','',NULL)");
        b.append(",('testcampaign','distribList','','fr','Liste de distribution','',NULL)");
        b.append(",('testcampaign','notifyEndTagExecution','','en','Notify the end of execution','',NULL)");
        b.append(",('testcampaign','notifyEndTagExecution','','fr','Notifier la fin de l\\'execution','',NULL)");
        b.append(",('testcampaign','notifyStartTagExecution','','en','Notify the start of execution','',NULL)");
        b.append(",('testcampaign','notifyStartTagExecution','','fr','Notifier le debut de l\\'execution','',NULL)");
        b.append(",('testcase','activePROD','','en','Active PROD','Define whether the <code class=\\'doc-crbvvoca\\'>test case</code> can be executed in PROD environments.<br>If the environment gp1 (attached to the invariant) is PROD and Active PROD is No, the <code class=\\'doc-crbvvoca\\'>test case</code> will never be executed.','_testcase')");
        b.append(",('testcase','activePROD','','fr','Actif PROD','','_cas_de_test')");
        b.append(",('testcase','activeQA','','en','Active QA','Define whether the <code class=\\'doc-crbvvoca\\'>test case</code> can be executed in QA environments.<br>If the environment gp1 (attached to the invariant) is QA and Active QA is No, the <code class=\\'doc-crbvvoca\\'>test case</code> will never be executed.','_testcase')");
        b.append(",('testcase','activeQA','','fr','Actif QA','','_cas_de_test')");
        b.append(",('testcase','activeUAT','','en','Active UAT','Define whether the <code class=\\'doc-crbvvoca\\'>test case</code> can be executed in UAT environments.<br>If the environment gp1 (attached to the invariant) is UAT and Active UAT is No, the <code class=\\'doc-crbvvoca\\'>test case</code> will never be executed.','_testcase')");
        b.append(",('testcase','activeUAT','','fr','Actif UAT','','_cas_de_test')");
        b.append(",('testcase','BehaviorOrValueExpected','','en','Detailed Description / Value Expected','It is a full description of the <code class=\\'doc-crbvvoca\\'>application</code> feature that we expect to be tested with that <code class=\\'doc-crbvvoca\\'>test case</code>.','_testcase')");
        b.append(",('testcase','BehaviorOrValueExpected','','fr','Description détaillée / Valeur attendue','','_cas_de_test')");
        b.append(",('testcase','BugID','','en','Bug ID','This is the bug ID that will fix the pending KO.','_testcase')");
        b.append(",('testcase','BugID','','fr','BugID','','_cas_de_test')");
        b.append(",('testcase','Comment','','en','Comment','This is where to add any interesting comment about the <code class=\\'doc-crbvvoca\\'>test case</code>.','_testcase')");
        b.append(",('testcase','Comment','','fr','Commentaire','This is where to add any interesting comment about the <code class=\\'doc-crbvvoca\\'>test case</code>.','_cas_de_test')");
        b.append(",('testcase','ConditionOper','','en','Condition Operator','',NULL)");
        b.append(",('testcase','ConditionOper','','fr','Condition Opérateur','',NULL)");
        b.append(",('testcase','ConditionVal1','','en','Condition Value1','',NULL)");
        b.append(",('testcase','ConditionVal1','','fr','Condition Value1','',NULL)");
        b.append(",('testcase','ConditionVal2','','en','Condition Value2','',NULL)");
        b.append(",('testcase','ConditionVal2','','fr','Condition Value2','',NULL)");
        b.append(",('testcase','countryList','','en','Country List','The list of countries where the test case is defined','_testcase')");
        b.append(",('testcase','countryList','','fr','Liste des pays','La liste des pays sur lesquels le cas de test est défini','_cas_de_test')");
        b.append(",('testcase','Creator','','en','Creator','This is the name of the Cerberus user who created the <code class=\\'doc-crbvvoca\\'>test case</code>.','_testcase')");
        b.append(",('testcase','Creator','','fr','Créateur','','_cas_de_test')");
        b.append(",('testcase','Description','','en','Test case short description','It is a synthetic description of what the <code class=\\'doc-crbvvoca\\'>test case</code> do.','_testcase')");
        b.append(",('testcase','Description','','fr','Description courte','','_cas_de_test')");
        b.append(",('testcase','FromBuild','','en','From Sprint',' ','_testcase')");
        b.append(",('testcase','FromBuild','','fr','Depuis le Sprint','','_cas_de_test')");
        b.append(",('testcase','FromRev','','en','From Rev',' ','_testcase')");
        b.append(",('testcase','FromRev','','fr','Depuis la Révision','','_cas_de_test')");
        b.append(",('testcase','Function','','en','Function','The function is the functionnality that the <code class=\\'doc-crbvvoca\\'>test case</code> is testing.','_testcase')");
        b.append(",('testcase','Function','','fr','Fonction','','_cas_de_test')");
        b.append(",('testcase','HowTo','','en','How To','<i>How to</i> field is used to define the step by step procedure used in order to execute the <code class=\\'doc-crbvvoca\\'>test case</code>. This is mainly used for MANUAL group <code class=\\'doc-crbvvoca\\'>test cases</code>.','_testcase')");
        b.append(",('testcase','HowTo','','fr','Comment Reproduire','','_cas_de_test')");
        b.append(",('testcase','Implementer','','en','Implementer','This is the name of the Cerberus user who implemented the <code class=\\'doc-crbvvoca\\'>test case</code>.','_testcase')");
        b.append(",('testcase','Implementer','','fr','Réalisateur','Nom de l\\'utilisateur Cerberus qui a implementé le cas de test.','_testcase')");
        b.append(",('testcase','LastModifier','','en','LastModifier','This is the name of the Cerberus user who last modified the <code class=\\'doc-crbvvoca\\'>test case</code>.','_testcase')");
        b.append(",('testcase','LastModifier','','fr','Dernier Modificateur','','_cas_de_test')");
        b.append(",('testcase','Origine','','en','Origin','This is the country or the team that identified the scenario of the <code class=\\'doc-crbvvoca\\'>test case</code>.','_testcase')");
        b.append(",('testcase','Origine','','fr','Origine',NULL,'_cas_de_test')");
        b.append(",('testcase','RefOrigine','','en','RefOrigin','This is the external reference of the <code class=\\'doc-crbvvoca\\'>test case</code> when coming from outside Cerberus.','_testcase')");
        b.append(",('testcase','RefOrigine','','fr','RefOrigine','','_cas_de_test')");
        b.append(",('testcase','Status','','en','Status','It is the workflow status of the <code class=\\'doc-crbvvoca\\'>test case</code> used to follow-up the implementation of the tests.<br>It can take any values depending on the workflow that manage the <code class=\\'doc-crbvvoca\\'>test case</code> life cycle.<br><br>The first status defined on the invariant table (based on the sequence) will be the default value for any new <code class=\\'doc-crbvvoca\\'>test case</code>.<br>The only status that is mandatory to define and create is the WORKING status that correspond to fully working and stable <code class=\\'doc-crbvvoca\\'>test case</code>.','_testcase')");
        b.append(",('testcase','Status','','fr','Status','','_cas_de_test')");
        b.append(",('testcase','TargetBuild','','en','Target Sprint','This is the Target Build that should fix the bug. Until we reach that Build, the <code class=\\'doc-crbvvoca\\'>test case</code> execution will be discarded.','_testcase')");
        b.append(",('testcase','TargetBuild','','fr','Sprint Cible','','_cas_de_test')");
        b.append(",('testcase','TargetRev','','en','Target Rev','This is the Revision that should fix the bug. Until we reach that Revision, the <code class=\\'doc-crbvvoca\\'>test case</code> execution will be discarded.','_testcase')");
        b.append(",('testcase','TargetRev','','fr','Revision Cible','Correpond à la Revision à partir de laquelle le bug est corrigé.','_testcase')");
        b.append(",('testcase','TcActive','','en','Act','This field define if the test is active or not. A <code class=\\'doc-crbvvoca\\'>test case</code> that is not active cannot be executed.','_testcase')");
        b.append(",('testcase','TcActive','','fr','Actif','','_cas_de_test')");
        b.append(",('testcase','TCDateCrea','','en','Creation Date','This is the <code class=\\'doc-crbvvoca\\'>test case</code> creation date.','_testcase')");
        b.append(",('testcase','TCDateCrea','','fr','Date de création','','_cas_de_test')");
        b.append(",('testcase','TestCase','','en','Testcase ID','A <code class=\\'doc-crbvvoca\\'>test case</code> is a scenario that test a specific feature of an <code class=\\'doc-crbvvoca\\'>application</code>.','_testcase')");
        b.append(",('testcase','TestCase','','fr','ID du Cas de test','','_cas_de_test')");
        b.append(",('testcase','TestCaseVersion','','en','Version','Version number of the testcase.','_testcase')");
        b.append(",('testcase','TestCaseVersion','','fr','Version','Numéro de version du cas de test.','_cas_de_test')");
        b.append(",('testcase','ticket','','en','Ticket','The is the Ticket Number that provided the implementation of the <code class=\\'doc-crbvvoca\\'>test case</code>.','_testcase')");
        b.append(",('testcase','ticket','','fr','Ticket','','_cas_de_test')");
        b.append(",('testcase','ToBuild','','en','To Sprint',' ','_testcase')");
        b.append(",('testcase','ToBuild','','fr','Jusqu\\'au Sprint','','_cas_de_test')");
        b.append(",('testcase','ToRev','','en','To Rev',' ','_testcase')");
        b.append(",('testcase','ToRev','','fr','Jusqu\\'à la Revision','','_cas_de_test')");
        b.append(",('testcasecountryproperties','Database','','en','DTB','Database where the SQL will be executed.<br>This is only applicable if the property type is <code class=\\'doc-fixed\\'>executeSql</code> or <code class=\\'doc-fixed\\'>executeSqlFromLib</code>.',NULL)");
        b.append(",('testcasecountryproperties','Description','','en','Description','Description of the property.',NULL)");
        b.append(",('testcasecountryproperties','Length','','en','Length','It is the length parameter of the property.<br>The parameter usage depend on the <code class=\\'doc-fixed\\'>type</code> of the property.',NULL)");
        b.append(",('testcasecountryproperties','Nature','','en','Nature','Nature is the parameter which define the unicity of the property calculation for this testcase in every execution.<br><br>It can take the following values :',NULL)");
        b.append(",('testcasecountryproperties','Nature','','fr','Nature',NULL,NULL)");
        b.append(",('testcasecountryproperties','Nature','NOTINUSE','en','Not currently beeing used values.','<code class=\\'doc-fixed\\'>NOTINUSE</code> : When the property return a list of value, NOTINUSE will take value(s) on the list that is not already used on a pending execution.<br><br>Criterias : Same property name, same country, same environment with execution status = PE (exclusing execution older than n minutes).<br> n can be cnfigured with cerberus_notinuse_timeout parameter.',NULL)");
        b.append(",('testcasecountryproperties','Nature','NOTINUSE','fr','Valeur non utilisée actuellement',NULL,NULL)");
        b.append(",('testcasecountryproperties','Nature','RANDOM','en','Random values.','<code class=\\'doc-fixed\\'>RANDOM</code> : When the property return a list of value, RANDOM will take a random value(s) on the list. It could be the same accross 2 different executions.',NULL)");
        b.append(",('testcasecountryproperties','Nature','RANDOM','fr','Valeurs aléatoires',NULL,NULL)");
        b.append(",('testcasecountryproperties','Nature','RANDOMNEW','en','Always unique random values.','<code class=\\'doc-fixed\\'>RANDOMNEW</code> : When the property return a list of values, RANDOMNEW will take random value(s) on the list that were not already used.<br><br>Criterias : Same Propertyname, same country, same environment, same test, same testcase and same build.<br>NB : revision is not part of the criterias.',NULL)");
        b.append(",('testcasecountryproperties','Nature','RANDOMNEW','fr','Valeur unique aléatoire',NULL,NULL)");
        b.append(",('testcasecountryproperties','Nature','STATIC','en','No unicity rule.','<code class=\\'doc-fixed\\'>STATIC</code> : No rules are defined. The property could be the same for all the executions',NULL)");
        b.append(",('testcasecountryproperties','Nature','STATIC','fr','Aucune règle d\\'unicité',NULL,NULL)");
        b.append(",('testcasecountryproperties','Property','','en','Property','This is the reference of the property.<br><br>A property is a data string that can be calculated in any moment during the <code class=\\'doc-crbvvoca\\'>test case</code>. Property can be defined directly inside the <code class=\\'doc-crbvvoca\\'>test case</code> but also calculated dynamically from an SQL or even read from the current html page.<br><br>When property is attached to an action, the associated action is executed only if the property exist in the country.<br><br>Once a property has been calculated, its value can be reused in any other property, action and controls using % before and after its reference.',NULL)");
        b.append(",('testcasecountryproperties','Property','','fr','Propriété',NULL,NULL)");
        b.append(",('testcasecountryproperties','RetryNb','','en','Retry','Integer that correspond to the number of time the calculation of the property will be done until the property gets a valid result.',NULL)");
        b.append(",('testcasecountryproperties','RetryPeriod','','en','Period','Integer that specify the amount of time (in ms) that Cerberus will wait between 2 retry attempt of the property calculation.',NULL)");
        b.append(",('testcasecountryproperties','RowLimit','','en','RowLimit','When the property calculation return a list of value, Rowlimit will limit the number of rows considered for random purposes.<br><br>For example, in 100 possible random values if rowLimit is 50, only the 50 first values will be accounted for random.',NULL)");
        b.append(",('testcasecountryproperties','RowLimit','','fr','Limite de lignes ',NULL,NULL)");
        b.append(",('testcasecountryproperties','Type','','en','Type1','This is the type of command which will be used to calculate the property.<br><br>It can take the following values :',NULL)");
        b.append(",('testcasecountryproperties','Type','executeSoapFromLib','en','[DEPRECATED] Get a value from a SOAP Request','TBD.',NULL)");
        b.append(",('testcasecountryproperties','Type','executeSql','en','Get a value from an SQL execution.','<code class=\\'doc-fixed\\'>executeSQL</code> will allow you to execute an SQL Query (Value) on the DTB Database.<br><br>Usage :<br><doc class=\"usage\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>Field</th><th class=\\'ex\\'>Usage</th><tr><td class=\\'ex\\'>DTB</td><td class=\\'ex\\'>Value of the database that correspond to the connection pool where the SQL will be executed.</td></tr><tr><td class=\\'ex\\'>Value</td><td class=\\'ex\\'>SQL instruction to be executed. All system and property variables can be used.</td></tr><tr><td class=\\'ex\\'>Length</td><td class=\\'ex\\'>Not used</td></tr><tr><td class=\\'ex\\'>RowLimit</td><td class=\\'ex\\'>Used depending on the Nature.</td></tr><tr><td class=\\'ex\\'>Nature</td><td class=\\'ex\\'>Nature to be used for unicity constrain.</td></tr></table></doc><br><br>Example :<br><doc class=\"examples\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>DTB</th><th class=\\'ex\\'>Value</th><th class=\\'ex\\'>Nature</th><th class=\\'ex\\'>Description</th><tr><td class=\\'ex\\'>AS400Data</td><td class=\\'ex\\'><code class=\\'doc-sql\\'>SELECT customer FROM table;</code></td><td class=\\'ex\\'>RANDOM</td><td class=\\'ex\\'>One of the customer value returned by the SQL will be used.</td></tr><tr><td class=\\'ex\\'>MySQLData</td><td class=\\'ex\\'><code class=\\'doc-sql\\'>SELECT user FROM toto;</code></td><td class=\\'ex\\'>STATIC</td><td class=\\'ex\\'>The first <code class=\\'doc-fixed\\'>user</code> of <code class=\\'doc-fixed\\'>toto</code> table will be used.</td></tr></table></doc>',NULL)");
        b.append(",('testcasecountryproperties','Type','executeSqlFromLib','en','[DEPRECATED] Get a value from an SQL execution from the library.','<code class=\\'doc-fixed\\'>executeSQLFromLib</code> will allow you to execute an SQL Query on the DTB Database from a library of SQL.<br>This type has the same behaviour as executeSQL type except that the SQLs are gathered from a library of SQL.<br>NB : This feature is DEPRECATED and should not be used. Please use GetFromTestDataLib in stead.<br><br>Usage :<br><doc class=\"usage\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>Field</th><th class=\\'ex\\'>Usage</th><tr><td class=\\'ex\\'>DTB</td><td class=\\'ex\\'>Value of the database that correspond to the connection pool where the SQL will be executed.</td></tr><tr><td class=\\'ex\\'>Value</td><td class=\\'ex\\'>Reference of the SQL instruction stored inside SQL Library.</td></tr><tr><td class=\\'ex\\'>Length</td><td class=\\'ex\\'>Not used</td></tr><tr><td class=\\'ex\\'>RowLimit</td><td class=\\'ex\\'>Used depending on the Nature.</td></tr><tr><td class=\\'ex\\'>Nature</td><td class=\\'ex\\'>Nature to be used for unicity constrain.</td></tr></table></doc><br><br>Example :<br><doc class=\"examples\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>DTB</th><th class=\\'ex\\'>Value</th><th class=\\'ex\\'>Nature</th><th class=\\'ex\\'>Description</th><tr><td class=\\'ex\\'>AS400Data</td><td class=\\'ex\\'>CUSTOMER1</td><td class=\\'ex\\'>RANDOM</td><td class=\\'ex\\'>One of the customer value returned by the SQL attached to CUSTOMER1 will be used.</td></tr><tr><td class=\\'ex\\'>MySQLData</td><td class=\\'ex\\'>USER1</td><td class=\\'ex\\'>STATIC</td><td class=\\'ex\\'>The first value of the 1st column of the SQL attached to USER1 SQL will be used.</td></tr></table></doc>',NULL)");
        b.append(",('testcasecountryproperties','Type','getAttributeFromHtml','en','Get an attribute value from an HTML field in the current page.','<code class=\\'doc-fixed\\'>getAttributeFromHtml</code> will allow you to take an attribute value from an html field on the current webpage.</br>Cerberus will automatically wait for the field to start to appear before getting the attribute value.<br>The different attributes identifier that can be used in order to find the HTML field are : id, name, class, css, xpath, link, and data-cerberus.<br>Syntax is as follow :<br><code class=\\'doc-sql\\'>identifier=html-value</code><br><br>Usage :<br><doc class=\"usage\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>Field</th><th class=\\'ex\\'>Usage</th><tr><td class=\\'ex\\'>DTB</td><td class=\\'ex\\'>Not used.</td></tr><tr><td class=\\'ex\\'>Value</td><td class=\\'ex\\'>IDENTIFIER=HTML-VALUE</td></tr><tr><td class=\\'ex\\'>Value2</td><td class=\\'ex\\'>ATTRIBUTE</td></tr><tr><td class=\\'ex\\'>Length</td><td class=\\'ex\\'>Not used</td></tr><tr><td class=\\'ex\\'>RowLimit</td><td class=\\'ex\\'>Not used.</td></tr><tr><td class=\\'ex\\'>Nature</td><td class=\\'ex\\'>Not used.</td></tr></table></doc><br><br>Examples :<br><doc class=\"examples\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>HTML</th><th class=\\'ex\\'>Value</th><th class=\\'ex\\'>Value2</th><th class=\\'ex\\'>Result</th><tr><td class=\\'ex\\'><textarea rows=\"3\" style=\"width: 245px;\" readonly><div class=\"Main\"><img id=\"Name\" src=\"toto.jpeg\"><span id=\"env\">PRODUCTION</span></div></textarea></td><td class=\\'ex\\'>id=Name</td><td class=\\'ex\\'>src</td><td class=\\'ex\\'>toto.jpeg</td></tr><tr><td class=\\'ex\\'><textarea rows=\"3\" style=\"width: 245px;\" readonly><input data-cerberus=\"ctl00\" name=\"inputName\">toto</input></textarea></td><td class=\\'ex\\'>data-cerberus=ctl00</td><td class=\\'ex\\'>name</td><td class=\\'ex\\'>inputName</td></tr></table></doc>',NULL)");
        b.append(",('testcasecountryproperties','Type','getDifferencesFromXml','en','Get differences from 2 XML files','TBD.',NULL)");
        b.append(",('testcasecountryproperties','Type','getFromCookie','en','Get a value from Cookie.','<code class=\\'doc-fixed\\'>getFromCookie</code> will allow you to get information on cookie.<br><br>Usage :<br><doc class=\"usage\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>Field</th><th class=\\'ex\\'>Usage</th><tr><td class=\\'ex\\'>DTB</td><td class=\\'ex\\'>Not used.</td></tr><tr><td class=\\'ex\\'>Value1</td><td class=\\'ex\\'>Cookie Name.<br>In case the cookie is not found, empty string will be returned.</td></tr><tr><td class=\\'ex\\'>Value2</td><td class=\\'ex\\'>Information on cookie.<br>It could be name, value, expiry, domain, path, isHttpOnly, isSecure.</td></tr><tr><td class=\\'ex\\'>Length</td><td class=\\'ex\\'>Not used</td></tr><tr><td class=\\'ex\\'>RowLimit</td><td class=\\'ex\\'>Not used.</td></tr><tr><td class=\\'ex\\'>Nature</td><td class=\\'ex\\'>Not used.</td></tr></table></doc><br><br>Examples :<br><doc class=\"examples\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>Value1</th><th class=\\'ex\\'>Value2</th><th class=\\'ex\\'>Result</th><tr><td class=\\'ex\\'>COOKIE_NAME</td><td class=\\'ex\\'>value</td><td class=\\'ex\\'>COOKIE_VALUE</td></tr><tr><td class=\\'ex\\'>COOKIE_NAME2</td><td class=\\'ex\\'>Expiry</td><td class=\\'ex\\'>01-01-2015</td></tr><tr><td class=\\'ex\\'>COOKIE_NAME3</td><td class=\\'ex\\'>host</td><td class=\\'ex\\'>www.cerberus-testing.org</td></tr></table></doc>',NULL)");
        b.append(",('testcasecountryproperties','Type','getFromDataLib','en','Get an object from the Data Library.','<code class=\\'doc-fixed\\'>getFromDataLib</code> will allow you to calculate a full object that include a list of string.</br>The return of the object can be used with either of the following syntax : %PROPERTY.subdata% or %PROPERTY(subdata)%.<br>Multiples rows can be retreived and you can access it using the following syntax : %PROPERTY.3.subdata% or %PROPERTY(3)(subdata)%<br>Use the Data library screen in order to configure the data library.<br><br>Usage :<br><doc class=\"usage\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>Field</th><th class=\\'ex\\'>Usage</th><tr><td class=\\'ex\\'>DTB</td><td class=\\'ex\\'>Not used.</td></tr><tr><td class=\\'ex\\'>Value</td><td class=\\'ex\\'>Data Library Name.</td></tr><tr><td class=\\'ex\\'>Length</td><td class=\\'ex\\'>Number of rows the object will retreive. Use %PROPERTY.n.subdata% in order to get the corresponding row.<br>In case not enougth data can be retreive, the property will report a NA status.</td></tr><tr><td class=\\'ex\\'>RowLimit</td><td class=\\'ex\\'>Max number of rows that will be fetch from the data source. If 0 the parameter cerberus_testdatalib_fetchmax is used.</td></tr><tr><td class=\\'ex\\'>Nature</td><td class=\\'ex\\'>Nature to be used for unicity constrain. STATIC, RANDOM, RANDOMNEW and NOTINUSE can be used.</td></tr></table></doc><br><br>Examples :<br><doc class=\"examples\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>Value</th><th class=\\'ex\\'>Length</th><th class=\\'ex\\'>rowLimit</th><th class=\\'ex\\'>Nature</th><th class=\\'ex\\'>Result</th><tr><td class=\\'ex\\'>toto</td><td class=\\'ex\\'>0</td><td class=\\'ex\\'>0</td><td class=\\'ex\\'>STATIC</td><td class=\\'ex\\'>1 row from \\'toto\\' Library.</td></tr><tr><td class=\\'ex\\'>toto%SYS_COUNTRY%titi</td><td class=\\'ex\\'>10</td><td class=\\'ex\\'>0</td><td class=\\'ex\\'>STATIC</td><td class=\\'ex\\'>10 rows of data from \\'totoPTtiti\\' Library.</td></tr><tr><td class=\\'ex\\'>toto</td><td class=\\'ex\\'>5</td><td class=\\'ex\\'>50</td><td class=\\'ex\\'>RANDOM</td><td class=\\'ex\\'>5 different rows picked by random in the 50 rows retreived from \\'toto\\' Library.</td></tr></table></doc>',NULL)");
        b.append(",('testcasecountryproperties','Type','getFromGroovy','en','Get a value from a Groovy expression.','<code class=\\'doc-fixed\\'>getFromGroovy</code> will allow you to calculate a string from a Groovy execution.\n<br/>\nUsing this feature, you can use the full power of Groovy without the need to be related to a web context as the <a href=\"/Cerberus/Documentation.jsp?DocTable=testcasecountryproperties&DocField=type&DocValue=getFromJS&Lang=en\"><code>getFromJS</code></a> property type.\n<br/>\n<br/>\nUsage:\n<br/>\n<doc class=\\'usage\\'>\n  <table cellspacing=\\'0\\' cellpadding=\\'2\\'>\n    <tr>\n      <th class=\\'ex\\'>Field</th>\n      <th class=\\'ex\\'>Usage</th>\n    </tr>\n    <tr>\n      <td class=\\'ex\\'>DTB</td>\n      <td class=\\'ex\\'>Not used.</td>\n    </tr>\n    <tr>\n      <td class=\\'ex\\'>Value</td>\n      <td class=\\'ex\\'>Groovy expression to execute.</td>\n    </tr>\n    <tr>\n      <td class=\\'ex\\'>Length</td>\n      <td class=\\'ex\\'>Not used.</td>\n    </tr>\n    <tr>\n      <td class=\\'ex\\'>RowLimit</td>\n      <td class=\\'ex\\'>Not used.</td>\n    </tr>\n    <tr>\n      <td class=\\'ex\\'>Nature</td>\n      <td class=\\'ex\\'>Not used.</td>\n    </tr>\n  </table>\n</doc>\n<br/>\n<br/>\nFor examples:\n<br/>\n<doc class=\\'examples\\'>\n  <table cellspacing=\\'0\\' cellpadding=\\'2\\'>\n    <tr>\n      <th class=\\'ex\\'>Value</th>\n      <th class=\\'ex\\'>Result</th>\n    </tr>\n    <tr>\n      <td class=\\'ex\\'><code class=\\'doc-fixed\\'>1+1</code></td>\n      <td class=\\'ex\\'>2</td>\n    </tr>\n    <tr>\n      <td class=\\'ex\\'><code class=\\'doc-fixed\\'>\"foobar\".replace(\"foo\", \"oof\")</code></td>\n      <td class=\\'ex\\'><code class=\\'doc-fixed\\'>oofbar</code></td>\n    </tr>\n    <tr>\n      <td class=\\'ex\\'><code class=\\'doc-fixed\\'>\"foo\".toUpperCase().equals(\"FOO\")</code></td>\n      <td class=\\'ex\\'><code class=\\'doc-fixed\\'>true</code></td>\n    </tr>\n    <tr>\n      <td class=\\'ex\\'><code class=\\'doc-fixed\\'>123 == 123<code></td>\n      <td class=\\'ex\\'><code class=\\'doc-fixed\\'>true<code></td>\n    </tr>\n    <tr>\n      <td class=\\'ex\\'>\n        <code class=\\'doc-fixed\\'>\n        def square = { number -> number * number };\n        square(2)\n      </code>\n      </td>\n      <td class=\\'ex\\'><code class=\\'doc-fixed\\'>4</code></td>\n    </tr>\n  </table>\n</doc>\n<br/>\n<br/>\nFor more information, you can access to the fully documentation from the <a href=\"http://groovy-lang.org/documentation.html\">official Groovy website</a>.\n',NULL)");
        b.append(",('testcasecountryproperties','Type','getFromHtml','en','Get a value from the current web page.','<code class=\\'doc-fixed\\'>getFromHtml</code> will allow you to take a value from an html field on the current webpage.</br>Cerberus will automatically wait for the field to start to appear before getting the value.<br>Different attributes identifier can be used in order to find the field : id, name, class, css, xpath, link, and data-cerberus.<br>Syntax is as follow :<br><code class=\\'doc-sql\\'>identifier=html-value</code><br><br>NB : getFromHtml will get the value of the field even if it does not appear on the web page (this is not the standard behaviour of Selenium).<br><br>Usage :<br><doc class=\"usage\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>Field</th><th class=\\'ex\\'>Usage</th><tr><td class=\\'ex\\'>DTB</td><td class=\\'ex\\'>Not used.</td></tr><tr><td class=\\'ex\\'>Value</td><td class=\\'ex\\'>IDENTIFIER=HTML-VALUE</td></tr><tr><td class=\\'ex\\'>Length</td><td class=\\'ex\\'>Not used</td></tr><tr><td class=\\'ex\\'>RowLimit</td><td class=\\'ex\\'>Not used.</td></tr><tr><td class=\\'ex\\'>Nature</td><td class=\\'ex\\'>Not used.</td></tr></table></doc><br><br>Examples :<br><doc class=\"examples\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>HTML</th><th class=\\'ex\\'>Value</th><th class=\\'ex\\'>Result</th><tr><td class=\\'ex\\'><textarea rows=\"3\" style=\"width: 245px;\" readonly><div class=\"Main\"><span id=\"Name\">FRONT1</span><span id=\"env\">PRODUCTION</span></div></textarea></td><td class=\\'ex\\'>id=Name</td><td class=\\'ex\\'>FRONT1</td></tr><tr><td class=\\'ex\\'><textarea rows=\"3\" style=\"width: 245px;\" readonly><input name=\"crb-name\" type=\"hidden\" value=\"CERBERUS\"/></textarea></td><td class=\\'ex\\'>name=crb-name</td><td class=\\'ex\\'>CERBERUS</td></tr><tr><td class=\\'ex\\'><textarea rows=\"3\" style=\"width: 245px;\" readonly><input data-cerberus=\"ctl00\">toto</input></textarea></td><td class=\\'ex\\'>data-cerberus=ctl00</td><td class=\\'ex\\'>toto</td></tr></table></doc>',NULL)");
        b.append(",('testcasecountryproperties','Type','getFromHtmlVisible','en','Get a visible value from the current web page.','<code class=\\'doc-fixed\\'>getFromHtmlVisible</code> will allow you to take a visible value from an html field on the current webpage.</br>Cerberus will automatically wait for the field to start to appear before getting the value.<br>Different attributes identifier can be used in order to find the field : id, name, class, css, xpath, link, and data-cerberus.<br>Syntax is as follow :<br><code class=\\'doc-sql\\'>identifier=html-value</code><br><br>NB : getFromHtmlVisible will return an empty value if the field does not appear on the web page (this is the standard behaviour of Selenium).<br><br>Usage :<br><doc class=\"usage\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>Field</th><th class=\\'ex\\'>Usage</th><tr><td class=\\'ex\\'>DTB</td><td class=\\'ex\\'>Not used.</td></tr><tr><td class=\\'ex\\'>Value</td><td class=\\'ex\\'>IDENTIFIER=HTML-VALUE</td></tr><tr><td class=\\'ex\\'>Length</td><td class=\\'ex\\'>Not used</td></tr><tr><td class=\\'ex\\'>RowLimit</td><td class=\\'ex\\'>Not used.</td></tr><tr><td class=\\'ex\\'>Nature</td><td class=\\'ex\\'>Not used.</td></tr></table></doc><br><br>Examples :<br><doc class=\"examples\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>HTML</th><th class=\\'ex\\'>Value</th><th class=\\'ex\\'>Result</th><tr><td class=\\'ex\\'><textarea rows=\"3\" style=\"width: 245px;\" readonly><div class=\"Main\"><span id=\"Name\">FRONT1</span><span id=\"env\">PRODUCTION</span></div></textarea></td><td class=\\'ex\\'>id=Name</td><td class=\\'ex\\'></td></tr><tr><td class=\\'ex\\'><textarea rows=\"3\" style=\"width: 245px;\" readonly><input name=\"crb-name\" type=\"hidden\" value=\"CERBERUS\"/></textarea></td><td class=\\'ex\\'>name=crb-name</td><td class=\\'ex\\'></td></tr><tr><td class=\\'ex\\'><textarea rows=\"3\" style=\"width: 245px;\" readonly><input data-cerberus=\"ctl00\">toto</input></textarea></td><td class=\\'ex\\'>data-cerberus=ctl00</td><td class=\\'ex\\'>toto</td></tr></table></doc>',NULL)");
        b.append(",('testcasecountryproperties','Type','getFromJS','en','Get a value from the output of a javascript execution.','<code class=\\'doc-fixed\\'>getFromJS</code> will allow you to calculate a string from a javascript execution.</br>Using this feature, you can use the full power of javascript in order to calculate values in the context of the current page.<br><br>Usage :<br><doc class=\"usage\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>Field</th><th class=\\'ex\\'>Usage</th><tr><td class=\\'ex\\'>DTB</td><td class=\\'ex\\'>Not used.</td></tr><tr><td class=\\'ex\\'>Value</td><td class=\\'ex\\'>Javascript to execute.<br>In case the javascript return no value, empty string will be returned.</td></tr><tr><td class=\\'ex\\'>Length</td><td class=\\'ex\\'>Not used</td></tr><tr><td class=\\'ex\\'>RowLimit</td><td class=\\'ex\\'>Not used.</td></tr><tr><td class=\\'ex\\'>Nature</td><td class=\\'ex\\'>Not used.</td></tr></table></doc><br><br>Examples :<br><doc class=\"examples\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>Value</th><th class=\\'ex\\'>Result</th><tr><td class=\\'ex\\'>return s.products == undefined || s.products.toLowerCase().split(\\'event16=\\').length == 1 ? \\'\\' : \\'fail_case\\';</td><td class=\\'ex\\'>fail_case</td></tr><tr><td class=\\'ex\\'>return GetCookie(\\'UserIdentificationId\\') == undefined ? \\'\\' : GetCookie(\\'UserIdentificationId\\');</td><td class=\\'ex\\'></td></tr><tr><td class=\\'ex\\'>return s.eVar5 == undefined ? \\'\\' : s.eVar5;</td><td class=\\'ex\\'></td></tr><tr><td class=\\'ex\\'>return new Date().getTime().toString() + \"@cerberus-testing.org\";</td><td class=\\'ex\\'>1391154967143@cerberus-testing.org</td></tr></table></doc>',NULL)");
        b.append(",('testcasecountryproperties','Type','getFromJson','en','Get a value from a Json File','<code class=\\'doc-fixed\\'>getFromJson</code> will allow you to take an element value from a Json File.</br>Cerberus will download the Json File (Calling the URL in the Value1 field) and parse it to return the element value expected (Specified in the Value2 field).<br><br>Usage :<br><doc class=\"usage\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>Field</th><th class=\\'ex\\'>Usage</th><tr><td class=\\'ex\\'>DTB</td><td class=\\'ex\\'>Not used.</td></tr><tr><td class=\\'ex\\'>Value</td><td class=\\'ex\\'>URL of the Json File</td></tr><tr><td class=\\'ex\\'>Value2</td><td class=\\'ex\\'>Element to Find</td></tr><tr><td class=\\'ex\\'>Length</td><td class=\\'ex\\'>Not used</td></tr><tr><td class=\\'ex\\'>RowLimit</td><td class=\\'ex\\'>Not used.</td></tr><tr><td class=\\'ex\\'>Nature</td><td class=\\'ex\\'>Not used.</td></tr></table></doc><br><br>Examples :<br><doc class=\"examples\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>Json File</th><th class=\\'ex\\'>Value</th><th class=\\'ex\\'>Value2</th><th class=\\'ex\\'>Result</th><tr><td class=\\'ex\\'><textarea rows=\"3\" style=\"width: 245px;\" readonly>{\"text1\": \"yes\",\"text2\": 1234, \"array\":[\"first\", \"second\", \"third\"]}</textarea></td><td class=\\'ex\\'>http://url_of_the_json/file.json</td><td class=\\'ex\\'>text2</td><td class=\\'ex\\'>1234</td></tr><tr><td class=\\'ex\\'><textarea rows=\"3\" style=\"width: 245px;\" readonly>{\"text1\": \"yes\",\"text2\": 1234, \"array\":[\"first\", \"second\", \"third\"]}</textarea></td><td class=\\'ex\\'>http://url_of_the_json/file.json</td><td class=\\'ex\\'>array[1]</td><td class=\\'ex\\'>second</td></tr></table></doc>',NULL)");
        b.append(",('testcasecountryproperties','Type','getFromTestData','en','[DEPRECATED] Get a value from Cerberus Test Data.','<code class=\\'doc-fixed\\'>getFromDataLib</code> will allow you to calculate a full object that include a list of string.</br>The return of the object can be used with either of the following syntax : %PROPERTY.subdata% or %PROPERTY(subdata)%.<br>Multiples rows can be retreived and you can access it using the following syntax : %PROPERTY.3.subdata% or %PROPERTY(3)(subdata)%<br>Use the Data library screen in order to configure the data library.<br><br>Usage :<br><doc class=\"usage\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>Field</th><th class=\\'ex\\'>Usage</th><tr><td class=\\'ex\\'>DTB</td><td class=\\'ex\\'>Not used.</td></tr><tr><td class=\\'ex\\'>Value</td><td class=\\'ex\\'>Text.</td></tr><tr><td class=\\'ex\\'>Number of rows the object will retreive. Use %PROPERTY.n.subdata% in order to get the corresponding row.<br>In case not enought dat can be retreive, the property will report a NA status.</td><td class=\\'ex\\'>Size of the string if Nature is STATIC.</td></tr><tr><td class=\\'ex\\'>RowLimit</td><td class=\\'ex\\'>Limit the data retreive from the source.</td></tr><tr><td class=\\'ex\\'>Nature</td><td class=\\'ex\\'>Nature to be used for unicity constrain.STATIC, RANDOM, RANDOMNEW and NOTINUSE can be used.</td></tr></table></doc><br><br>Examples :<br><doc class=\"examples\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>Value</th><th class=\\'ex\\'>Nature</th><th class=\\'ex\\'>Length</th><th class=\\'ex\\'>Result</th><tr><td class=\\'ex\\'>toto</td><td class=\\'ex\\'>STATIC</td><td class=\\'ex\\'>0</td><td class=\\'ex\\'>toto</td></tr><tr><td class=\\'ex\\'>toto%SYS_COUNTRY%titi</td><td class=\\'ex\\'>STATIC</td><td class=\\'ex\\'>0</td><td class=\\'ex\\'>totoPTtiti</td></tr><tr><td class=\\'ex\\'>toto</td><td class=\\'ex\\'>RANDOM</td><td class=\\'ex\\'>5</td><td class=\\'ex\\'>a5Gx3</td></tr></table></doc>',NULL)");
        b.append(",('testcasecountryproperties','Type','getFromXML','en','Get a value from an XML file.','<code class=\\'doc-fixed\\'>getFromXml</code> will allow you to get value from an XML file specifying the URL of the file and the xpath to eecute to get the data.<br><br>Usage :<br><doc class=\"usage\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>Field</th><th class=\\'ex\\'>Usage</th><tr><td class=\\'ex\\'>DTB</td><td class=\\'ex\\'>Not used.</td></tr><tr><td class=\\'ex\\'>Value1</td><td class=\\'ex\\'>URL to the Xml file to parse.</td></tr><tr><td class=\\'ex\\'>Value2</td><td class=\\'ex\\'>xpath information to get data.</td></tr><tr><td class=\\'ex\\'>Length</td><td class=\\'ex\\'>Not used</td></tr><tr><td class=\\'ex\\'>RowLimit</td><td class=\\'ex\\'>Not used.</td></tr><tr><td class=\\'ex\\'>Nature</td><td class=\\'ex\\'>Not used.</td></tr></table></doc><br><br>Examples :<br>Parsing a file www.cerberus-testing.org/test.xml which contains an xml structure with ResponseCode element equals to OK and ResponseValue equals to 12345, it should be configured that way:<br><ResponseCode<doc class=\"examples\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>Value1</th><th class=\\'ex\\'>Value2</th><th class=\\'ex\\'>Result</th><tr><td class=\\'ex\\'>www.cerberus-testing.org/test.xml</td><td class=\\'ex\\'>//ResponseCode</td><td class=\\'ex\\'>OK</td></tr><tr><td class=\\'ex\\'>www.cerberus-testing.org/test.xml</td><td class=\\'ex\\'>//ResponseValue</td><td class=\\'ex\\'>12345</td></tr></table></doc>',NULL)");
        b.append(",('testcasecountryproperties','Type','text','en','Simple text.','<code class=\\'doc-fixed\\'>text</code> will allow you to calculate a string.</br>Using the Nature, random string can also be calculated.<br><br>Usage :<br><doc class=\"usage\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>Field</th><th class=\\'ex\\'>Usage</th><tr><td class=\\'ex\\'>DTB</td><td class=\\'ex\\'>Not used.</td></tr><tr><td class=\\'ex\\'>Value</td><td class=\\'ex\\'>Text.</td></tr><tr><td class=\\'ex\\'>Length</td><td class=\\'ex\\'>Size of the string if Nature is STATIC.</td></tr><tr><td class=\\'ex\\'>RowLimit</td><td class=\\'ex\\'>Not used.</td></tr><tr><td class=\\'ex\\'>Nature</td><td class=\\'ex\\'>Nature to be used for unicity constrain. Only Static and RANDOM (or RANDOMNEW) are used.</td></tr></table></doc><br><br>Examples :<br><doc class=\"examples\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>Value</th><th class=\\'ex\\'>Nature</th><th class=\\'ex\\'>Length</th><th class=\\'ex\\'>Result</th><tr><td class=\\'ex\\'>toto</td><td class=\\'ex\\'>STATIC</td><td class=\\'ex\\'>0</td><td class=\\'ex\\'>toto</td></tr><tr><td class=\\'ex\\'>toto%SYS_COUNTRY%titi</td><td class=\\'ex\\'>STATIC</td><td class=\\'ex\\'>0</td><td class=\\'ex\\'>totoPTtiti</td></tr><tr><td class=\\'ex\\'>toto</td><td class=\\'ex\\'>RANDOM</td><td class=\\'ex\\'>5</td><td class=\\'ex\\'>a5Gx3</td></tr></table></doc>',NULL)");
        b.append(",('testcasecountryproperties','Value','','en','Value','Value of the property. Depend on the <code class=\\'doc-fixed\\'>type</code> of property chosen.<br><br>Get more information on <code class=\\'doc-fixed\\'>type</code> field.<br><br><table cellspacing=0 cellpadding=3><th class=\\'ex\\' colspan=\\'2\\'>The following system variables can be used</th><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_SYSTEM%</code></td><td class=\\'ex\\'>System value</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_APPLI%</code></td><td class=\\'ex\\'>Application reference</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_APP_DOMAIN%</code></td><td class=\\'ex\\'>Domain of the Application</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_APP_VAR1%</code></td><td class=\\'ex\\'>VAR1 of the application on the environment.</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_APP_VAR2%</code></td><td class=\\'ex\\'>VAR2 of the application on the environment.</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_APP_VAR3%</code></td><td class=\\'ex\\'>VAR3 of the application on the environment.</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_APP_VAR4%</code></td><td class=\\'ex\\'>VAR4 of the application on the environment.</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_ENV%</code></td><td class=\\'ex\\'>Environment value</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_ENVGP%</code></td><td class=\\'ex\\'>Environment group code</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_COUNTRY%</code></td><td class=\\'ex\\'>Country code</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_TEST%</code></td><td class=\\'ex\\'>Test.</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_TESTCASE%</code></td><td class=\\'ex\\'>TestCase</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_COUNTRYGP1%</code></td><td class=\\'ex\\'>Country group1 value</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_SSIP%</code></td><td class=\\'ex\\'>Selenium server IP</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_SSPORT%</code></td><td class=\\'ex\\'>Selenium server port</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_BROWSER%</code></td><td class=\\'ex\\'>Browser name of the current execution.</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_TAG%</code></td><td class=\\'ex\\'>Execution tag</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_EXECUTIONID%</code></td><td class=\\'ex\\'>Execution ID</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_EXESTART%</code></td><td class=\\'ex\\'>Start date and time of the execution with format : 2016-12-31 21:24:53.008.</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_EXESTORAGEURL%</code></td><td class=\\'ex\\'>Path where media are stored (based from the exeid).</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_STEP.n.RETURNCODE%</code></td><td class=\\'ex\\'>Return Code of the step n. n being the execution sequence of the step (sort).</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_TODAY-yyyy%</code></td><td class=\\'ex\\'>Year of today</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_TODAY-MM%</code></td><td class=\\'ex\\'>Month of today</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_TODAY-dd%</code></td><td class=\\'ex\\'>Day of today</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_TODAY-doy%</code></td><td class=\\'ex\\'>Day of today from the beginning of the year</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_TODAY-HH%</code></td><td class=\\'ex\\'>Hour of today</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_TODAY-mm%</code></td><td class=\\'ex\\'>Minute of today</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_TODAY-ss%</code></td><td class=\\'ex\\'>Second of today</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_YESTERDAY-yyyy%</code></td><td class=\\'ex\\'>Year of yesterday</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_YESTERDAY-MM%</code></td><td class=\\'ex\\'>Month of yesterday</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_YESTERDAY-dd%</code></td><td class=\\'ex\\'>Day of yesterday</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_TODAY-doy%</code></td><td class=\\'ex\\'>Day of yesterday from the beginning of the year</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_YESTERDAY-HH%</code></td><td class=\\'ex\\'>Hour of yesterday</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_YESTERDAY-mm%</code></td><td class=\\'ex\\'>Minute of yesterday</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_YESTERDAY-ss%</code></td><td class=\\'ex\\'>Second of yesterday</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_ELAPSED-EXESTART%</code></td><td class=\\'ex\\'>Number of milisecond since the start of the execution.</td></tr><tr><td class=\\'ex\\'><code class=\\'doc-variable\\'>%SYS_ELAPSED-STEPSTART%</code></td><td class=\\'ex\\'>Number of milisecond since the start of the execution of the current step.</td></tr></table>',NULL)");
        b.append(",('testcaseexecution','Browser','','en','Browser','This is the browser that was used to run the <code class=\\'doc-crbvvoca\\'>test case</code> (only used if that was a GUI application <code class=\\'doc-crbvvoca\\'>test case</code>).',NULL)");
        b.append(",('testcaseexecution','BrowserFullVersion','','en','Browser Version','This is the full version information of the browser that was used to run the <code class=\\'doc-crbvvoca\\'>test case</code> (only used if that was a GUI application <code class=\\'doc-crbvvoca\\'>test case</code>).',NULL)");
        b.append(",('testcaseexecution','BrowserFullVersion','','fr','Version du navigateur',NULL,NULL)");
        b.append(",('testcaseexecution','Build','','en','Sprint','Name of the Build/sprint.',NULL)");
        b.append(",('testcaseexecution','ControlMessage','','en','ControlMessage','This is the message reported by Cerberus on the execution of the <code class=\\'doc-crbvvoca\\'>test case</code>.',NULL)");
        b.append(",('testcaseexecution','controlstatus','','en','RC','This is the return code of the Execution. It can take the following values :<br><br><b>OK</b> : The <code class=\\'doc-crbvvoca\\'>test case</code> has been executed and everything happened as expected.<br><b>KO</b> : The <code class=\\'doc-crbvvoca\\'>test case</code> has been executed and reported an error that will create a bug.<br><b>NA</b> : The <code class=\\'doc-crbvvoca\\'>test case</code> has been executed but some data to perform the test could not be collected (SQL returning empty resultset).<br><b>FA</b> : The <code class=\\'doc-crbvvoca\\'>test case</code> failed to execute because there were an error inside the test such as an SQL error. The <code class=\\'doc-crbvvoca\\'>test case</code> needs to be corrected.<br><b>CA</b> : The <code class=\\'doc-crbvvoca\\'>test case</code> has been cancelled. It failed during the execution because of technical issues (ex. Lost of connection issue to selenium during the execution)<br><b>PE</b> : The execution is still running and not finished yet or has been interupted.',NULL)");
        b.append(",('testcaseexecution','crbversion','','en','Cerberus Version','This is the version of the Cerberus Engine that executed the <code class=\\'doc-crbvvoca\\'>test case</code>.<br>This data has been created for tracability purpose as the behavious of Cerberus could varry from one version to another.',NULL)");
        b.append(",('testcaseexecution','crbversion','','fr','Version de Cerberus',NULL,NULL)");
        b.append(",('testcaseexecution','end','','en','End',' ',NULL)");
        b.append(",('testcaseexecution','executor','','en','Executor user','This is the name of the executor user that executed the <code class=\\'doc-crbvvoca\\'>test case</code>.<br>This data has been created for tracability purpose as the result of Cerberus Test could varry between human and selenium test.',NULL)");
        b.append(",('testcaseexecution','id','','en','Execution ID',' ',NULL)");
        b.append(",('testcaseexecution','IP','','en','IP','This is the ip of the machine of the Selenium Server where the <code class=\\'doc-crbvvoca\\'>test case</code> executed.',NULL)");
        b.append(",('testcaseexecution','Port','','en','Port','This is the port used to contact the Selenium Server where the <code class=\\'doc-crbvvoca\\'>test case</code> executed.',NULL)");
        b.append(",('testcaseexecution','Revision','','en','Revision','Number of the Revision',NULL)");
        b.append(",('testcaseexecution','screensize','','en','Screen Size','This is the real size of the browser screen that performed the execution of the <code class=\\'doc-crbvvoca\\'>test case</code>.',NULL)");
        b.append(",('testcaseexecution','start','','en','Start',' ',NULL)");
        b.append(",('testcaseexecution','status','','en','TC Status','This correspond to the status of the <code class=\\'doc-crbvvoca\\'>test case</code> when it was executed.<br>This is used to identify executions done on stable <code class=\\'doc-crbvvoca\\'>test case</code> compared to the ones done on draft version. ',NULL)");
        b.append(",('testcaseexecution','tag','','en','Tag','The Tag is just a string defined by the user that will be recorded with the execution. Its purpose is to help to find back some specific executions.',NULL)");
        b.append(",('testcaseexecution','URL','','en','URL','Full URL used to connect to the application.',NULL)");
        b.append(",('testcaseexecution','verbose','','en','Verbose','This correspond to the level if information that Cerberus will keep when performing the execution. It can take the following values :<br><br><b>0</b> : The test will keep minimum login information in order to preserve the response times. This is to be used when a massive amout of tests are performed. No details on action will be saved.<br><b>1</b> : This is the standard level of log. Detailed action execution information will also be stored.<br><b>2</b> : This is the highest level of detailed information that can be chosen. Detailed web traffic information will be stored. This is to be used only on very specific cases where all hits information of an execution are required.<br><br>NB : Verbose level higher that 0 rely on Network traffic (only available on firefox browser).',NULL)");
        b.append(",('testcaseexecutiondata','database','','en','Database','',NULL)");
        b.append(",('testcaseexecutiondata','database','','fr','Base de Donnée','',NULL)");
        b.append(",('testcaseexecutiondata','index','','en','Index','',NULL)");
        b.append(",('testcaseexecutiondata','index','','fr','Index','',NULL)");
        b.append(",('testcaseexecutiondata','length','','en','Length','',NULL)");
        b.append(",('testcaseexecutiondata','length','','fr','Longueur','',NULL)");
        b.append(",('testcaseexecutiondata','nature','','en','Nature','',NULL)");
        b.append(",('testcaseexecutiondata','nature','','fr','Nature','',NULL)");
        b.append(",('testcaseexecutiondata','retry','','en','Retry','',NULL)");
        b.append(",('testcaseexecutiondata','retry','','fr','Retry','',NULL)");
        b.append(",('testcaseexecutiondata','retryperiod','','en','Retry Period','',NULL)");
        b.append(",('testcaseexecutiondata','retryperiod','','fr','Periode de Retry','',NULL)");
        b.append(",('testcaseexecutiondata','rowlimit','','en','Row Limit','',NULL)");
        b.append(",('testcaseexecutiondata','rowlimit','','fr','Row Limit','',NULL)");
        b.append(",('testcaseexecutiondata','type','','en','Type','',NULL)");
        b.append(",('testcaseexecutiondata','type','','fr','Type','',NULL)");
        b.append(",('testcaseexecutiondata','Value','','en','Property Value','This is the Value of the calculated Property.',NULL)");
        b.append(",('testcaseexecutiondata','Value','','fr','Valeur','',NULL)");
        b.append(",('testcaseexecutionqueue','debugFlag','','en','Activate Debug Mode',NULL,NULL)");
        b.append(",('testcaseexecutionqueue','debugFlag','','fr','Activation du mode debug',NULL,NULL)");
        b.append(",('testcaseexecutionqueue','priority','','en','Priority',NULL,NULL)");
        b.append(",('testcaseexecutionqueue','priority','','fr','Priorité',NULL,NULL)");
        b.append(",('testcaseexecutionwwwsum','css_nb','','en','Css_nb','Number of css downloaded for all the scenario',NULL)");
        b.append(",('testcaseexecutionwwwsum','css_nb','','fr','Css_nb',NULL,NULL)");
        b.append(",('testcaseexecutionwwwsum','css_size_max','','en','Css_size_max','Size of the biggest css dowloaded during the scenario',NULL)");
        b.append(",('testcaseexecutionwwwsum','css_size_max','','fr','Css_taille_max',NULL,NULL)");
        b.append(",('testcaseexecutionwwwsum','css_size_tot','','en','Css_size_tot','Total size of the css for the scenario',NULL)");
        b.append(",('testcaseexecutionwwwsum','css_size_tot','','fr','Css_taille_tot',NULL,NULL)");
        b.append(",('testcaseexecutionwwwsum','css_tps','','en','Css_tps','Cumulated time for download css for all the scenario',NULL)");
        b.append(",('testcaseexecutionwwwsum','css_tps','','fr','Css_tps',NULL,NULL)");
        b.append(",('testcaseexecutionwwwsum','img_nb','','en','Img_nb','Number of pictures downloaded for all the scenario',NULL)");
        b.append(",('testcaseexecutionwwwsum','img_nb','','fr','Img_nb',NULL,NULL)");
        b.append(",('testcaseexecutionwwwsum','img_size_max','','en','Img_size_max','Size of the biggest Picture dowloaded during the scenario',NULL)");
        b.append(",('testcaseexecutionwwwsum','img_size_max','','fr','Img_taille_max',NULL,NULL)");
        b.append(",('testcaseexecutionwwwsum','img_size_tot','','en','Img_size_tot','Total size of the Pictures for the scenario',NULL)");
        b.append(",('testcaseexecutionwwwsum','img_size_tot','','fr','Img_taille_tot',NULL,NULL)");
        b.append(",('testcaseexecutionwwwsum','img_tps','','en','Img_tps','Cumulated time for downloaded pictures for all the scenario',NULL)");
        b.append(",('testcaseexecutionwwwsum','img_tps','','fr','Img_tps',NULL,NULL)");
        b.append(",('testcaseexecutionwwwsum','js_nb','','en','Js_nb','Number of javascript downloaded for all the scenario',NULL)");
        b.append(",('testcaseexecutionwwwsum','js_nb','','fr','Js_nb',NULL,NULL)");
        b.append(",('testcaseexecutionwwwsum','js_size_max','','en','Js_size_max','Size of the biggest javascript dowloaded during the scenario',NULL)");
        b.append(",('testcaseexecutionwwwsum','js_size_max','','fr','Js_taille_max',NULL,NULL)");
        b.append(",('testcaseexecutionwwwsum','js_size_tot','','en','Js_size_tot','Total size of the javascript for the scenario',NULL)");
        b.append(",('testcaseexecutionwwwsum','js_size_tot','','fr','Js_taille_tot',NULL,NULL)");
        b.append(",('testcaseexecutionwwwsum','js_tps','','en','Js_tps','Cumulated time for downloaded javascripts for all the scenario',NULL)");
        b.append(",('testcaseexecutionwwwsum','js_tps','','fr','Js_tps','Cumulated time for downloaded javascripts for all the scenario',NULL)");
        b.append(",('testcaseexecutionwwwsum','nb_rc2xx','','en','Nb_rc2xx','Number of return code hits between 200 and 300',NULL)");
        b.append(",('testcaseexecutionwwwsum','nb_rc2xx','','fr','Nb_cr2xx',NULL,NULL)");
        b.append(",('testcaseexecutionwwwsum','nb_rc3xx','','en','Nb_rc3xx','Number of return code hits between 300 and 400',NULL)");
        b.append(",('testcaseexecutionwwwsum','nb_rc3xx','','fr','Nb_cr3xx',NULL,NULL)");
        b.append(",('testcaseexecutionwwwsum','nb_rc4xx','','en','Nb_rc4xx','Number of return code hits between 400 and 500',NULL)");
        b.append(",('testcaseexecutionwwwsum','nb_rc4xx','','fr','Nb_cr4xx',NULL,NULL)");
        b.append(",('testcaseexecutionwwwsum','nb_rc5xx','','en','Nb_rc5xx','Number of return code hits higher than 500',NULL)");
        b.append(",('testcaseexecutionwwwsum','nb_rc5xx','','fr','Nb_cr5xx',NULL,NULL)");
        b.append(",('testcaseexecutionwwwsum','tot_nbhits','','en','Tot_nbhits','Total number of hits of a scenario',NULL)");
        b.append(",('testcaseexecutionwwwsum','tot_nbhits','','fr','Tot_nbhits',NULL,NULL)");
        b.append(",('testcaseexecutionwwwsum','tot_size','','en','Tot_size','Total size of all the elements',NULL)");
        b.append(",('testcaseexecutionwwwsum','tot_size','','fr','Tot_taille',NULL,NULL)");
        b.append(",('testcaseexecutionwwwsum','tot_tps','','en','Tot_tps','Total time cumulated for the download of all the elements',NULL)");
        b.append(",('testcaseexecutionwwwsum','tot_tps','','fr','Tot_tps',NULL,NULL)");
        b.append(",('testcaselabel','labelId','','en','Label ID','',NULL)");
        b.append(",('testcaselabel','labelId','','fr','ID du label','',NULL)");
        b.append(",('testcasestep','step','','en','Step','A step is a group of actions.',NULL)");
        b.append(",('testcasestepaction','Action','','en','Action','It is the action that will be executed by Cerberus.<br><br>It can take the following values :',NULL)");
        b.append(",('testcasestepaction','Action','calculateProperty','en','Calculate a Cerberus property.','<code class=\\'doc-fixed\\'>calculateProperty</code> will allow you to calculate a property defined in the property section of the test case.\n\n<br/><br/>\n\nUsage :<br/>\n\n<doc class=\"usage\">\n <table>\n  <tr>\n   <th class=\\'ex\\'>Field</th>\n   <th class=\\'ex\\'>Usage</th>\n  </tr>\n  <tr>\n   <td class=\\'ex\\'>Value1</td>\n   <td class=\\'ex\\'>Property name to be calculated.</td>\n  </tr>\n  <tr>\n   <td class=\\'ex\\'>Value2</td>\n   <td class=\\'ex\\'>[Optional] Property name from which get value to affect property from Value1. Useful to override the one defined from the property section.</td>\n  </tr>\n </table>\n</doc>\n\n<br/><br/>\n\nExamples :<br/>\n\n<doc class=\"examples\">\n <table>\n  <tr>\n   <th class=\\'ex\\'>Value1</th>\n   <th class=\\'ex\\'>Value2</th>\n   <th class=\\'ex\\'>Result</th>\n  </tr>\n  <tr>\n   <td class=\\'ex\\'>PROPERTY_NAME</td>\n   <td class=\\'ex\\'></td>\n   <td class=\\'ex\\'>PROPERTY_NAME will be calculated</td>\n  </tr>\n  <tr>\n   <td class=\\'ex\\'>PROPERTY_NAME</td>\n   <td class=\\'ex\\'>OTHER_PROPERTY_NAME</td>\n   <td class=\\'ex\\'>PROPERTY_NAME will be affected by the calculated value of OTHER_PROPERTY_NAME</td>\n  </tr>\n </table>\n</doc>',NULL)");
        b.append(",('testcasestepaction','Action','callSoap','en','Call Soap.','TBD',NULL)");
        b.append(",('testcasestepaction','Action','callSoapWithBase','en','Call Soap with Base','<code class=\\'doc-fixed\\'>callSoapWithBase</code> will allow you to make a SOAP call (Stored on the <a href=\"./SoapLibrary.jsp\">SoapLibrary</a>) using the servicePath stored at the countryenvrionmentparameters level. That allow to call the soap on the environment of the execution.<br><br> The result will be stored in the memory. On this result, you can make some control (verify the presence or the content of the elements for exemple) or get some information using property getFromXML<br><br>Usage :<br><doc class=\"usage\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>Field</th><th class=\\'ex\\'>Usage</th><tr><td class=\\'ex\\'>Value1</td><td class=\\'ex\\'>Name of the SOAP from the SOAPLibrary.</td></tr><tr><td class=\\'ex\\'>Value2</td><td class=\\'ex\\'></td></tr></table></doc><br><br>Examples :<br><doc class=\"examples\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>Value1</th><th class=\\'ex\\'>Value2</th><th class=\\'ex\\'>Result</th><tr><td class=\\'ex\\'>WEATHER</td><td class=\\'ex\\'></td><td class=\\'ex\\'>WEATHER soapCall will be made.</td></tr></table></doc>',NULL)");
        b.append(",('testcasestepaction','Action','click','en','Clicking on a button.','<code class=\\'doc-fixed\\'>click</code> will allow you to click on an element inside the current page.<br><br>Usage :<br><doc class=\"usage\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>Field</th><th class=\\'ex\\'>Usage</th><tr><td class=\\'ex\\'>Value1</td><td class=\\'ex\\'>Identifier and name of the element to click in the form of : identifier=html_reference.</td></tr><tr><td class=\\'ex\\'>Value2</td><td class=\\'ex\\'></td></tr></table></doc><br><br>Examples :<br><doc class=\"examples\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>Value1</th><th class=\\'ex\\'>Value2</th><th class=\\'ex\\'>Result</th><tr><td class=\\'ex\\'>id=html_reference</td><td class=\\'ex\\'></td><td class=\\'ex\\'>element that has id equal to html_reference will be clicked</td></tr></table></doc>',NULL)");
        b.append(",('testcasestepaction','Action','doNothing','en','Just perform no action.','<code class=\\'doc-fixed\\'>doNothing</code> will just perfom no action. Can be used in case of control that must be done without action before.<br><br>Usage :<br><doc class=\"usage\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>Field</th><th class=\\'ex\\'>Usage</th><tr><td class=\\'ex\\'>Value1</td><td class=\\'ex\\'></td></tr><tr><td class=\\'ex\\'>Value2</td><td class=\\'ex\\'></td></tr></table></doc><br><br>Examples :<br><doc class=\"examples\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>Value1</th><th class=\\'ex\\'>Value2</th><th class=\\'ex\\'>Result</th><tr><td class=\\'ex\\'></td><td class=\\'ex\\'></td><td class=\\'ex\\'>No action will be executed and engine will go to the next action or control</td></tr></table></doc>',NULL)");
        b.append(",('testcasestepaction','Action','doubleClick','en','Double clicking on a button.','<code class=\\'doc-fixed\\'>doubleClick</code> will allow you to double click on an element inside the current page.<br><br>Usage :<br><doc class=\"usage\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>Field</th><th class=\\'ex\\'>Usage</th><tr><td class=\\'ex\\'>Value1</td><td class=\\'ex\\'>Identifier and name of the element to double click in the form of : identifier=html_reference.</td></tr><tr><td class=\\'ex\\'>Value2</td><td class=\\'ex\\'></td></tr></table></doc><br><br>Examples :<br><doc class=\"examples\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>Value1</th><th class=\\'ex\\'>Value2</th><th class=\\'ex\\'>Result</th><tr><td class=\\'ex\\'>id=html_reference</td><td class=\\'ex\\'></td><td class=\\'ex\\'>element that has id equal to html_reference will be double clicked.</td></tr></table></doc>',NULL)");
        b.append(",('testcasestepaction','Action','executeSqlStoredProcedure','en','Execute SQL Stored Procedure','<code class=\\'doc-fixed\\'>executeSqlStoredProcedure</code> will allow you to execute SQL stored procedure.<br>Parameter cerberus_actionexecutesqlstoredprocedure_timeout can be used in order to tune the timeout.<br><br>Usage :<br><doc class=\"usage\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>Field</th><th class=\\'ex\\'>Usage</th><tr><td class=\\'ex\\'>Value1</td><td class=\\'ex\\'>Name of the Database to connect to.</td></tr><tr><td class=\\'ex\\'>Value2</td><td class=\\'ex\\'>Property name of the procedure to execute. The property should be a text one.</td></tr></table></doc><br><br>Examples :<br><doc class=\"examples\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>Value1</th><th class=\\'ex\\'>Value2</th><th class=\\'ex\\'>Result</th><tr><td class=\\'ex\\'>CRB</td><td class=\\'ex\\'>PROPERTY_NAME</td><td class=\\'ex\\'>The procedure name declared in the property PROPERTY_NAME will be executed on database CRB through connection pool that has been configured in JDBC Ressource of the corresponding CRB database on the corresponding environment.</td></tr></table></doc>',NULL)");
        b.append(",('testcasestepaction','Action','executeSqlUpdate','en','Execute SQL update','<code class=\\'doc-fixed\\'>executeSqlUpdate</code> will allow you to execute SQL update (insert,delete,update).<br>Parameter cerberus_actionexecutesqlupdate_timeout can be used in order to tune the timeout.<br><br>Usage :<br><doc class=\"usage\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>Field</th><th class=\\'ex\\'>Usage</th><tr><td class=\\'ex\\'>Value1</td><td class=\\'ex\\'>Name of the Database to connect to.</td></tr><tr><td class=\\'ex\\'>Value2</td><td class=\\'ex\\'>Property name of the script to execute. The property should be a text one.</td></tr></table></doc><br><br>Examples :<br><doc class=\"examples\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>Value1</th><th class=\\'ex\\'>Value2</th><th class=\\'ex\\'>Result</th><tr><td class=\\'ex\\'>CRB</td><td class=\\'ex\\'>PROPERTY_NAME</td><td class=\\'ex\\'>The SQL declared in the property PROPERTY_NAME will be executed on database CRB through connection pool that has been configured in JDBC Ressource of the corresponding CRB database on the corresponding environment.</td></tr></table></doc>',NULL)");
        b.append(",('testcasestepaction','Action','focusDefaultIframe','en','Focus on the default frame.','TBD',NULL)");
        b.append(",('testcasestepaction','Action','focusToIframe','en','Focus to a specific frame.','TBD',NULL)");
        b.append(",('testcasestepaction','Action','getPageSource','en','[DEPRECATED] getPageSource','<code class=\\'doc-fixed\\'>getPageSource</code> will allow you to record the source of the page opened.<br>Action is DEPRECATED. Please use the getPageSource control in stead.<br><br> The result will be stored in a file which will be available in the execution detail<br><br>Usage :<br><doc class=\"usage\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>Field</th><th class=\\'ex\\'>Usage</th><tr><td class=\\'ex\\'>Value1</td><td class=\\'ex\\'></td></tr><tr><td class=\\'ex\\'>Value2</td><td class=\\'ex\\'></td></tr></table></doc><br><br>Examples :<br><doc class=\"examples\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>Value1</th><th class=\\'ex\\'>Value2</th><th class=\\'ex\\'>Result</th><tr><td class=\\'ex\\'></td><td class=\\'ex\\'> </td><td class=\\'ex\\'>Source will be recorded</td></tr></table></doc>',NULL)");
        b.append(",('testcasestepaction','Action','hideKeyboard','en','Hide keyboard.','Hide the currently visible keyboard.',NULL)");
        b.append(",('testcasestepaction','Action','keypress','en','Press a specific key.','<code class=\\'doc-fixed\\'>keypress</code> will allow you to press any key in the current web page.<br><br>Usage :<br><doc class=\"usage\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>Field</th><th class=\\'ex\\'>Usage</th><tr><td class=\\'ex\\'>Value1</td><td class=\\'ex\\'>Keycode of the key to press.</td></tr><tr><td class=\\'ex\\'>Value2</td><td class=\\'ex\\'></td></tr></table></doc><br><br>Examples :<br><doc class=\"examples\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>Value1</th><th class=\\'ex\\'>Value2</th><th class=\\'ex\\'>Result</th><tr><td class=\\'ex\\'>ENTER</td><td class=\\'ex\\'></td><td class=\\'ex\\'>ENTER key will be pressed.</td></tr><tr><td class=\\'ex\\'>SEARCH</td><td class=\\'ex\\'></td><td class=\\'ex\\'>SEARCH key will be pressed.</td></tr></table></doc>',NULL)");
        b.append(",('testcasestepaction','Action','manageDialog','en','Manage javascript dialog opened by application, specified ok to accept it or cancel to dismiss','<b>manageDialog</b><br><br>Let possibility to testcase to handle javascript dialog <br>Specify <b>ok</b> value to accept it or <b>cancel</b> to dismiss.',NULL)");
        b.append(",('testcasestepaction','Action','mouseLeftButtonPress','en','Click mouse button and hold it clicked. ','TBD',NULL)");
        b.append(",('testcasestepaction','Action','mouseLeftButtonRelease','en','Release clicked mouse button. ','TBD',NULL)");
        b.append(",('testcasestepaction','Action','mouseOver','en','Mouse cursor over an object.','TBD',NULL)");
        b.append(",('testcasestepaction','Action','mouseOverAndWait','en','Mouse cursor over an object and wait for a certain time.','TBD',NULL)");
        b.append(",('testcasestepaction','Action','openUrl','en','Open a URL','<code class=\\'doc-fixed\\'>openUrl</code> will allow you to open a specific URL.<br><br>Usage :<br><doc class=\"usage\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>Field</th><th class=\\'ex\\'>Usage</th><tr><td class=\\'ex\\'>Value1</td><td class=\\'ex\\'>Absolute URL to open. </td></tr><tr><td class=\\'ex\\'>Value2</td><td class=\\'ex\\'></td></tr></table></doc><br><br>Examples :<br><doc class=\"examples\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>Value1</th><th class=\\'ex\\'>Value2</th><th class=\\'ex\\'>Result</th><tr><td class=\\'ex\\'>http://www.cerberus-testing.org/contextroot/login/login.aspx</td><td class=\\'ex\\'></td><td class=\\'ex\\'>www.cerberus-testing.org/contextroot/login/login.aspx URL will be open.</td></tr></table></doc>',NULL)");
        b.append(",('testcasestepaction','Action','openUrlLogin','en','Open the Login URL.','TBD',NULL)");
        b.append(",('testcasestepaction','Action','openUrlWithBase','en','Open a URL.','<code class=\\'doc-fixed\\'>openUrlWithBase</code> will allow you to open a specific URL.<br><br>Usage :<br><doc class=\"usage\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>Field</th><th class=\\'ex\\'>Usage</th><tr><td class=\\'ex\\'>Value1</td><td class=\\'ex\\'>Relative URL to open. hostname and context root will automatically be prefixed.</td></tr><tr><td class=\\'ex\\'>Value2</td><td class=\\'ex\\'></td></tr></table></doc><br><br>Examples :<br><doc class=\"examples\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>Value1</th><th class=\\'ex\\'>Value2</th><th class=\\'ex\\'>Result</th><tr><td class=\\'ex\\'>login/login.aspx</td><td class=\\'ex\\'></td><td class=\\'ex\\'>www.cerberus-testing.org/contextroot/login/login.aspx URL will be open.</td></tr></table></doc>',NULL)");
        b.append(",('testcasestepaction','Action','removeDifference','en','Remove Differences.','TBD',NULL)");
        b.append(",('testcasestepaction','Action','rightClick','en','Right click on an element.','<code class=\\'doc-fixed\\'>rightClick</code> will allow you to right click on an element inside the current page.<br><br>Usage :<br>\n<doc class=\"usage\">\n    <table cellspacing=0 cellpadding=2>\n        <th class=\\'ex\\'>Field</th>\n        <th class=\\'ex\\'>Usage</th>\n        <tr>\n            <td class=\\'ex\\'>Value1</td>\n            <td class=\\'ex\\'>Identifier and name of the element to right click in the form of : identifier=html_reference.</td>\n        </tr>\n        <tr>\n            <td class=\\'ex\\'>Value2</td>\n            <td class=\\'ex\\'>\n            </td>\n        </tr>\n    </table>\n</doc><br><br>Examples :<br>\n<doc class=\"examples\">\n    <table cellspacing=0 cellpadding=2>\n        <th class=\\'ex\\'>Value1</th>\n        <th class=\\'ex\\'>Value2</th>\n        <th class=\\'ex\\'>Result</th>\n        <tr>\n            <td class=\\'ex\\'>id=html_reference</td>\n            <td class=\\'ex\\'></td>\n            <td class=\\'ex\\'>element that has id equal to html_reference will be right clicked</td>\n        </tr>\n    </table>\n</doc>',NULL)");
        b.append(",('testcasestepaction','Action','select','en','Select a value on a combo.','<b>select :</b> When the action expected is to select a value from a select box.<br><br><dd><u><i>How to feed it :</i></u> <br><br><dd><i>Action =</i> select, <i>Value =</i> the <i>id</i> of the select box.  and <i>Property =</i> the property containing the value to select.<br>It could be label=TheExactNameOfTheValue or value=the first letter or the place number of the value expected in the select box<br>For example : label=WEB   , value=W   , value=3 if the WEB is the third value in the selectbox<br><br><br>',NULL)");
        b.append(",('testcasestepaction','Action','skipAction','en','Skip this action.','<code class=\\'doc-fixed\\'>skipAction</code> will skip the action. Can be used in case of temporary disabling the associated controls.<br><br>Usage :<br><doc class=\"usage\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>Field</th><th class=\\'ex\\'>Usage</th><tr><td class=\\'ex\\'>Value1</td><td class=\\'ex\\'></td></tr><tr><td class=\\'ex\\'>Value2</td><td class=\\'ex\\'></td></tr></table></doc><br><br>Examples :<br><doc class=\"examples\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>Value1</th><th class=\\'ex\\'>Value2</th><th class=\\'ex\\'>Result</th><tr><td class=\\'ex\\'></td><td class=\\'ex\\'></td><td class=\\'ex\\'>No action will be executed and engine will go to the next action.</td></tr></table></doc>',NULL)");
        b.append(",('testcasestepaction','Action','swipe','en','Swipe the screen.','<code class=\\'doc-fixed\\'>swipe</code> will allow you to swipe a mobile screen to a specific direction.<br><br>Usage :<br><doc class=\"usage\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>Field</th><th class=\\'ex\\'>Usage</th><tr><td class=\\'ex\\'>Value1</td><td class=\\'ex\\'>Direction to swipe (UP, DOWN, RIGHT, LEFT or CUSTOM). In case of UP, DOWN, RIGHT and LEFT, swipe is done by computing from 1/3 to 2/3 of the screen resolution.</td></tr><tr><td class=\\'ex\\'>Value2</td><td class=\\'ex\\'>Only in case of CUSTOM swipe direction, specify the custom direction thanks to the following format: x1;y1;x2;y2, where x1 and y1 are the coordinates of the start position on the screen and x2 and y2 are the coordinates of the end position on the screen.</td></tr></table></doc><br><br>Examples :<br><doc class=\"examples\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>Value1</th><th class=\\'ex\\'>Value2</th><th class=\\'ex\\'>Result</th><tr><td class=\\'ex\\'>UP</td><td class=\\'ex\\'></td><td class=\\'ex\\'>Swipe is done from down to up (so the page go down).</td></tr><tr><td class=\\'ex\\'>CUSTOM</td><td class=\\'ex\\'>100;200;300;400</td></code><td class=\\'ex\\'>Swipe goes from (x1 = 100; y1 = 200) to (x2 = 300; y2 = 400) on the screen.</td></tr></table></doc>',NULL)");
        b.append(",('testcasestepaction','Action','switchToWindow','en','Switching the focus to a window.','When the Test case need to switch to another window (like popup dialog) this action is used. Just specify title of other window in objet to switch to this window.',NULL)");
        b.append(",('testcasestepaction','Action','type','en','Put a data in a field.','<b>type :</b> When the action expected is to type something into a field.<br><br><dd><u><i>How to feed it :</i></u> <br><br><dd><i>Action =</i> type, <i>Value =</i> the <i>id</i> of the field  and <i>Property =</i> the property containing the value to type.<br><br><br>',NULL)");
        b.append(",('testcasestepaction','Action','Unknown','en','Unknown action.','This is the default action defined inside Cerberus.<br>It can be used when the action has not been identified or clarified yet.<br>NB : It is not implemented and will report a FA status on the corresponding execution.',NULL)");
        b.append(",('testcasestepaction','Action','wait','en','Wait for a certain amount of time.','<b>wait :</b> When the action expected is to wait 5 seconds.<br><br><dd><u><i>How to feed it :</i></u> <br><br><dd><i>Action =</i> wait, <i>Value =</i> null  and  <i>Property =</i> null.<br><br><br>',NULL)");
        b.append(",('testcasestepaction','description','','en','Description','This is the functional desciption of the action.',NULL)");
        b.append(",('testcasestepaction','ForceExeStatus','','en','Exe RC','<p>This parameter can be used in order to change the behaviour of the Cerberus execution engine.</p><p>If the field is empty, there will be no impact on the behaviour of the engine.</p>If PE, the execution will continue (stay pending) after the execution of the action no matter what is the result of the action. This value can be used in case an action needs to be done to perform the test but should not impact the result of the test if it fails (Ex : closing a marketing layer on a website).',NULL)");
        b.append(",('testcasestepaction','ForceExeStatus','','fr','Exe CR',NULL,NULL)");
        b.append(",('testcasestepaction','image','','en','Picture','',NULL)");
        b.append(",('testcasestepaction','image','','fr','Image',NULL,NULL)");
        b.append(",('testcasestepaction','Sequence','','en','Sequence','Sequence of execution of the actions inside the step.',NULL)");
        b.append(",('testcasestepaction','Value1','','en','Val1','This is the information that is used to perform the action.<br>The same variable as property value field can be used (See <a href=\"?DocTable=testcasecountryproperties&DocField=Value\">doc</a>)<br>This information needs to be feed according to the action chosen.<br><br>Get more information on <code class=\\'doc-fixed\\'>action</code> field.',NULL)");
        b.append(",('testcasestepaction','Value2','','en','Val2','This is the information that is used to perform the action.<br>The same variable as property value field can be used (See <a href=\"?DocTable=testcasecountryproperties&DocField=Value\">doc</a>)<br>This information needs to be feed according to the action chosen.<br><br>Get more information on <code class=\\'doc-fixed\\'>action</code> field.',NULL)");
        b.append(",('testcasestepactioncontrol','ConditionOper','','en','Cond','',NULL)");
        b.append(",('testcasestepactioncontrol','ConditionVal1','','en','Val1','',NULL)");
        b.append(",('testcasestepactioncontrol','Control','','en','Control','It is the control that will be executed by Cerberus.<br><br>It can take the following values :<br>',NULL)");
        b.append(",('testcasestepactioncontrol','Control','','fr','Vrai si l\\'URL de la page courante est égale à la chaîne de caractères',NULL,NULL)");
        b.append(",('testcasestepactioncontrol','Control','getPageSource','en','Save source of the page.','TBD',NULL)");
        b.append(",('testcasestepactioncontrol','Control','skipControl','en','Skip the control.','TBD',NULL)");
        b.append(",('testcasestepactioncontrol','Control','takeScreenshot','en','Take a screenshot.','TBD',NULL)");
        b.append(",('testcasestepactioncontrol','Control','Unknown','en','Default Control in Cerberus','TBD',NULL)");
        b.append(",('testcasestepactioncontrol','Control','verifyElementClickable','en','True if element is clickable.','<b>verifyElementClickable</b><br><br>Verify if an element is clickable.<br><br><i>Control Property :</i> Element container<br><br>',NULL)");
        b.append(",('testcasestepactioncontrol','Control','verifyElementDifferent','en','True if the ControlProp does not contains the same element ControlValue.','<b>verifyElementDifferent</b><br><br>Verify if the element is different from an another in an XML file.<br><br><i>Control Property :</i> XPath to the element<br><br><i>Control Value :</i> The element to be different<br><br>',NULL)");
        b.append(",('testcasestepactioncontrol','Control','verifyElementEquals','en','True if the ControlProp contains the same element ControlValue.','<b>verifyElementEquals</b><br><br>Verify if the element equals to another in an XML file.<br><br><i>Control Property :</i> XPath to the element<br><br><i>Control Value :</i> The expected element<br><br>',NULL)");
        b.append(",('testcasestepactioncontrol','Control','verifyElementInElement','en','True if the ControlProp contains an element ControlValue.','<b>verifyElementInElement</b><br><br>Verify if an element is contained in another on the webpage.<br><br><i>Control Property :</i> Element container<br><br><i>Control Value :</i> Element contained in the element Container<br><br>',NULL)");
        b.append(",('testcasestepactioncontrol','Control','verifyElementNotClickable','en','True if element is not clickable.','<b>verifyElementNotClickable</b><br><br>Verify if an element is not clickable.<br><br><i>Control Property :</i> Element container<br><br>',NULL)");
        b.append(",('testcasestepactioncontrol','Control','verifyElementNotPresent','en','True if element is not found on the current page.','TBD',NULL)");
        b.append(",('testcasestepactioncontrol','Control','verifyElementNotVisible','en','True if element is present but not visible on the current page.','<b>verifyElementNotVisible</b><br><br>Verify if the HTML element specified exists, is not visible and has text on it',NULL)");
        b.append(",('testcasestepactioncontrol','Control','verifyElementPresent','en','True if element is found on the current page.','<b>verifyElementPresent</b><br><br>Verify if an specific element is present on the web page <br><br>',NULL)");
        b.append(",('testcasestepactioncontrol','Control','verifyElementVisible','en','True if element is visible on the current page.','<b>verifyElementVisible</b><br><br>Verify if the HTML element specified is exists, is visible and has text on it<br><br>',NULL)");
        b.append(",('testcasestepactioncontrol','Control','verifyIntegerDifferent','en','True if the ControlProp is different than the integer ControlValue.','<b>verifyIntegerDifferent</b><br><br>Verify if two integers are differents.<br><br><i>Control Property :</i> The first integer<br><br><i>Control Value :</i> The second integer<br><br>',NULL)");
        b.append(",('testcasestepactioncontrol','Control','verifyIntegerEquals','en','True if the ControlProp is equal to the integer ControlValue.','<b>verifyIntegerEquals</b><br><br>Verify if two integers are equals.<br><br><i>Control Property :</i> The first integer<br><br><i>Control Value :</i> The second integer<br><br>',NULL)");
        b.append(",('testcasestepactioncontrol','Control','verifyIntegerGreater','en','True if an integer is greater than another.','TBD',NULL)");
        b.append(",('testcasestepactioncontrol','Control','verifyIntegerMinor','en','True if an integer is lower than another.','TBD',NULL)");
        b.append(",('testcasestepactioncontrol','Control','verifyRegexInElement','en','True if a regex match the content of a field.','<code class=\\'doc-fixed\\'>verifyRegexInElement</code> will return true if a regex match the content of a field.<br><br>Usage :<br><doc class=\"usage\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>Field</th><th class=\\'ex\\'>Usage</th><tr><td class=\\'ex\\'>Value1</td><td class=\\'ex\\'>Field name</td></tr><tr><td class=\\'ex\\'>Value2</td><td class=\\'ex\\'>Regex</td></tr></table></doc><br><br>Examples :<br><doc class=\"examples\"><table cellspacing=0 cellpadding=2><th class=\\'ex\\'>Value1</th><th class=\\'ex\\'>Value2</th><th class=\\'ex\\'>Result</th><tr><td class=\\'ex\\'>xpath=.//*[@class=\\'breadcrumbs\\']</td><td class=\\'ex\\'>.*becoming seller.*</td><td class=\\'ex\\'>true if the data inside the field contains \\'becoming seller\\'.</td></tr></table></doc><br>NB : Standard Java regex can be used. Further details <a href=\"https://docs.oracle.com/javase/7/docs/api/java/util/regex/Pattern.html\">here</a>.',NULL)");
        b.append(",('testcasestepactioncontrol','Control','verifyStringContains','en','True if Property String contains value String.','<b>verifyStringContains</b><br><br>Verify if the value is contains in the propery<br><b>This test is case sensitive</b>',NULL)");
        b.append(",('testcasestepactioncontrol','Control','verifyStringDifferent','en','True if 2 strings are different.','TBD',NULL)");
        b.append(",('testcasestepactioncontrol','Control','verifyStringEqual','en','True if 2 strings are equal.','TBD',NULL)");
        b.append(",('testcasestepactioncontrol','Control','verifyStringGreater','en','True if a string is greater than another.','TBD',NULL)");
        b.append(",('testcasestepactioncontrol','Control','verifyStringMinor','en','True if a string is before another.','TBD',NULL)");
        b.append(",('testcasestepactioncontrol','Control','verifyTextInDialog','en','True if property or value is equal to dialog text retrieved.','<b>verifyTextInDialog</b><br><br>Verify if the text in dialog is equal to property or dialog.',NULL)");
        b.append(",('testcasestepactioncontrol','Control','verifyTextInElement','en','True if a text is inside a field.','TBD',NULL)");
        b.append(",('testcasestepactioncontrol','Control','verifyTextInPage','en','True if a text is inside the source of the current page.','TBD',NULL)");
        b.append(",('testcasestepactioncontrol','Control','verifyTextNotInElement','en','True if a text is not inside a field.','<b>verifyTextNotInElement</b><br><br>True if a text is not inside a field.<br><br><i>Control Property :</i> The field location<br><br><i>Control Value :</i> The text to test against the value from the field locatin<br><br>',NULL)");
        b.append(",('testcasestepactioncontrol','Control','verifyTextNotInPage','en','True if a text is not inside the source of the current page.','TBD',NULL)");
        b.append(",('testcasestepactioncontrol','Control','verifyTitle','en','True if the title of the current page equal to a string.','<b>verifytitle</b><br><br>Verify if the title of the webpage is the same than the value specified<br><br>',NULL)");
        b.append(",('testcasestepactioncontrol','Control','verifyUrl','en','True if the URL of the current page equal to a string.','<b>verifyurl</b><br><br>Verify if the URL of the webpage is the same than the value specified<br><br><i>Control Value :</i>should be null<br><br><i>Control Property :</i> URL expected (without the base)<br><br>',NULL)");
        b.append(",('testcasestepactioncontrol','Control','verifyXmlTreeStructure','en','Check if XML tree Structure is correct.','TBD',NULL)");
        b.append(",('testcasestepactioncontrol','ControlSequence','','en','CtrlNum','This is the number of the control.<br>If you have more than one control attached to an action, use this value to order their execution.',NULL)");
        b.append(",('testcasestepactioncontrol','ControlSequence','','fr','CtrlNum',NULL,NULL)");
        b.append(",('testcasestepactioncontrol','Description','','en','Description','This is the functional desciption of the control.',NULL)");
        b.append(",('testcasestepactioncontrol','Fatal','','en','Fatal','This define if the control is fatal.<br><br>If the control is fatal and KO, the execution will stop and execution status will report KO.<br>If the control is not fatal and KO, the execution will continue but execution status will still report a KO.',NULL)");
        b.append(",('testcasestepactioncontrol','Sequence','','en','Sequence','It is the number of the sequence in which the control will be performed.<br><br>NB : Controls are performed after each action.',NULL)");
        b.append(",('testcasestepactioncontrol','Sequence','','fr','Séquence',NULL,NULL)");
        b.append(",('testcasestepactioncontrol','Step','','en','Step','',NULL)");
        b.append(",('testcasestepactioncontrol','Step','','fr','Etape',NULL,NULL)");
        b.append(",('testcasestepactioncontrol','Value1','','en','Val1','This is the property that is going to be used inside the control.<br>The same variable as property value field can be used (See <a href=\"?DocTable=testcasecountryproperties&DocField=Value\">doc</a>)<br><br>Get more information on <code class=\\'doc-fixed\\'>type</code> field.',NULL)");
        b.append(",('testcasestepactioncontrol','Value2','','en','Val2','This is the value that is going to be used inside the control.<br>The same variable as property value field can be used (See <a href=\"?DocTable=testcasecountryproperties&DocField=Value\">doc</a>)<br><br>Get more information on <code class=\\'doc-fixed\\'>type</code> field.',NULL)");
        b.append(",('testcasestepactioncontrolexecution','ReturnCode','','en','Control Return Code','Return Code of the Control.',NULL)");
        b.append(",('testcasestepactioncontrolexecution','ReturnMessage','','en','Return Message','This is the return message on that specific control.',NULL)");
        b.append(",('testcasestepactionexecution','ForceExeStatus','','en','Force Execution RC','This is the value of the Force Execution Status data used by the engine during the execution of the testcase.',NULL)");
        b.append(",('testcasestepactionexecution','ForceExeStatus','','fr','Force Execution CR',NULL,NULL)");
        b.append(",('testcasestepactionexecution','ReturnCode','','en','Action Return Code','This is the return code of the action.',NULL)");
        b.append(",('testcasestepactionexecution','ReturnCode','','fr','Code retour de l\\'action','C\\'est le code retour de l\\'action',NULL)");
        b.append(",('testcasestepactionexecution','ReturnMessage','','fr','Message retourné','C\\'est le message retourné par le contrôle',NULL)");
        b.append(",('testcasestepactionexecution','screenshotfilename','','en','Screenshot Filename','This is the filename of the screenshot.<br>It is null if no screenshots were taken.',NULL)");
        b.append(",('testdatalib','actions','','en','Actions','<p> List of available actions for the current user: </p><p><table border=\\'1\\'><tr><th class=\\'ex\\'>Button</th><th class=\\'ex\\'>Function</th><th class=\\'ex\\'>Description</th></tr><tr><td><span class=\\'glyphicon glyphicon-pencil\\'></span></td><td>Edit Entry</td><td>Allows the update of the library entry: system, environment, country, type, group, database, script, method, service path, envelope and description. The name is not editable.</td></tr><tr><td><span class=\\'glyphicon glyphicon-trash\\'></span></td><td>Delete Entry</td><td>Allows the deletion of a library entry (and its sub-data entries). Only the entries that are not being used are possible to be deleted. </td></tr> <tr><td><span class=\\'glyphicon glyphicon-list-alt\\'></span></td><td>Edit Sub-Data Entries</td><td>Allows the management of the sub-data set specified for the library entry. </td></tr><tr><td>TC</td><td>Get List of test cases that use this entry</td><td>Allows the visualisation of the test cases that are currently using the library entry.</td></tr></table></p>  ','_data_library')");
        b.append(",('testdatalib','actions','','fr','Actions',NULL,'_librairie_de_données')");
        b.append(",('testdatalib','actions_nopermissions','','en','Actions','<p> List of available actions for the current user: </p><p><table border=\\'1\\'><tr><th class=\\'ex\\'>Button</th><th class=\\'ex\\'>Function</th><th class=\\'ex\\'>Description</th></tr><tr><td><span class=\\'glyphicon glyphicon-list-alt\\'></span></td><td>Sub-Data Entries</td><td>Allows the visualisation of the sub-data set specified for the library entry. </td></tr><tr><td>TC</td><td>Get list of test cases that use this entry</td><td>Allows the visualisation of the test cases that are currently using the library entry.</td></tr></table></p>  ','_data_library')");
        b.append(",('testdatalib','actions_nopermissions','','fr','Actions',NULL,'_librairie_de_données')");
        b.append(",('testdatalib','country','','en','Country','<p>Country where the entry is available. If not specified, then the data entry apply to ALL countries. </p>','_data_library')");
        b.append(",('testdatalib','country','','fr','Pays',NULL,'_librairie_de_données')");
        b.append(",('testdatalib','created','','en','Creation Date','','_data_library')");
        b.append(",('testdatalib','created','','fr','Date de création',NULL,'_librairie_de_données')");
        b.append(",('testdatalib','creator','','en','Creator','User who created the data.','_data_library')");
        b.append(",('testdatalib','creator','','fr','Créateur',NULL,'_librairie_de_données')");
        b.append(",('testdatalib','csvUrl','','en','CSV URL','<p>CSV URL specifies the URL where the CSV can be reached.</p>','_data_library')");
        b.append(",('testdatalib','csvUrl','','fr','URL du CSV','<p>L’URL du CSV représente l’URL du fichier CSV à décrypter.</p>','_librairie_de_données')");
        b.append(",('testdatalib','database','','en','Database','<p>Specifies the database where the <i>script</i> attribute should be executed. </p> You can then configure the JDBC Connection pool on that database at the environment level. That allows to create testdata independant from the environement where the testcase is executed.','_data_library')");
        b.append(",('testdatalib','database','','fr','Base de Données',NULL,'_librairie_de_données')");
        b.append(",('testdatalib','databaseCsv','','en','Database','<p>Specifies the database where the <i>Service Path</i> will be requested. </p> You can then configure the left part of the Service URL on that database at the environment level. That allows to create testdata independant from the environment where the testcase is executed.','_data_library')");
        b.append(",('testdatalib','databaseCsv','','fr','Base de données',NULL,'_librairie_de_données')");
        b.append(",('testdatalib','databaseUrl','','en','Database','<p>Specifies the database where the <i>Service Path</i> will be requested. </p> You can then configure the left part of the Service URL on that database at the environment level. That allows to create testdata independant from the environment where the testcase is executed.','_data_library')");
        b.append(",('testdatalib','databaseUrl','','fr','Base de données','','_librairie_de_données')");
        b.append(",('testdatalib','description','','en','Description','<p>Textual description of the entry.</p>','_data_library')");
        b.append(",('testdatalib','description','','fr','Description',NULL,'_librairie_de_données')");
        b.append(",('testdatalib','envelope','','en','Envelope','<p>Envelope that should be sent in the SOAP request.</p>','_data_library')");
        b.append(",('testdatalib','envelope','','fr','Enveloppe','<p>Enveloppe qui doit être envoyée dans la requête SOAP</p>','_librairie_de_données')");
        b.append(",('testdatalib','environment','','en','Environment','<p>Environment where the entry is available. If not specified, then the data entry apply to ALL environments.</p>','_data_library')");
        b.append(",('testdatalib','environment','','fr','Environnement',NULL,'_librairie_de_données')");
        b.append(",('testdatalib','group','','en','Group','<p>Name that groups entries that are at some extent correlated. It is an <b>optional</b> attribute.</p>','_data_library')");
        b.append(",('testdatalib','group','','fr','Groupe',NULL,'_librairie_de_données')");
        b.append(",('testdatalib','lastmodified','','en','Modification Date','','_data_library')");
        b.append(",('testdatalib','lastmodified','','fr','Dernière modification',NULL,'_librairie_de_données')");
        b.append(",('testdatalib','lastmodifier','','en','Last Modifier','User who last modified the data.','_data_library')");
        b.append(",('testdatalib','lastmodifier','','fr','Dernier modificateur','Utilisateur qui a modifié en dernier la donnée','_librairie_de_données')");
        b.append(",('testdatalib','method','','en','Operation','<p>Operation that is invoked by the SOAP request/call.</p>','_data_library')");
        b.append(",('testdatalib','method','','fr','Operation','<p>Operation à appeler pour la Requête SOAP.</p>','_librairie_de_données')");
        b.append(",('testdatalib','name','','en','Name','<p>Name of the entry. It is a <b>mandatory</b> attribute.</p><p><b><u>Note</u></b>: The combination of <u>Name</u>, <u>System</u>, <u>Environment</u> and <u>Country</u> can be duplicated when the type is STATIC in order to allow a list of data to be available.</p>','_data_library')");
        b.append(",('testdatalib','name','','fr','Nom','<p>Nom de la donnée</p>','_librairie_de_données')");
        b.append(",('testdatalib','script','','en','Script','<p>SQL commands that should be executed to retrieve test data.</p><p>Examples:</p><table><tr><td>select * from table;</td></tr><tr><td>select * from table where column = %COLUMN%;</td></tr></table>','_data_library')");
        b.append(",('testdatalib','script','','fr','Scenario',NULL,'_librairie_de_données')");
        b.append(",('testdatalib','separator','','en','Separator','<p>Separator used parsing a CSV.</p>','_data_library')");
        b.append(",('testdatalib','separator','','fr','Séparateur','<p>Séparateur à utiliser pour le décryptage du CSV.</p>','_librairie_de_données')");
        b.append(",('testdatalib','service','','en','Service','','_data_library')");
        b.append(",('testdatalib','service','','fr','Service',NULL,'_librairie_de_données')");
        b.append(",('testdatalib','servicepath','','en','Service Path','<p>Location of the service.</p><p>Examples:</p><table><tr><td>http://mydomain/mywebservicelocation</td></tr><tr><td>mywebservicelocation</td></tr><tr><td>http://%MY_DYNAMIC_IP%/mywebservicelocation</td></tr><tr><td>%LOCATION%</td></tr></table>','_data_library')");
        b.append(",('testdatalib','servicepath','','fr','Chemin du Service',NULL,'_librairie_de_données')");
        b.append(",('testdatalib','system','','en','System','<p>System where the entry is available. If not specified, then the data entry apply to ALL systems.</p>','_data_library')");
        b.append(",('testdatalib','system','','fr','Système',NULL,'_librairie_de_données')");
        b.append(",('testdatalib','testdatalibid','','en','ID','<p>Unique identifier of the test data library entry</p>','_data_library')");
        b.append(",('testdatalib','testdatalibid','','fr','Identifiant','<p>Identifiant unique de librairie de donnée</p>','_librairie_de_données')");
        b.append(",('testdatalib','type','','en','Type','<p>Entry Type - Cerberus allows the definition of 4 types: INTERNAL, SQL, CSV and SERVICE.</p><table border=\\'1\\'> <tr><th class=\\'ex\\'>Type</th><th class=\\'ex\\'>Description</th></tr> <tr><td>INTERNAL</td><td>Static test data - in each execution the values used by the test cases are statically definied directly in Cerberus.</td></tr> <tr><td>SQL</td><td> Test data obtained from a SQL execution – values depend on what the SQL return on the corresponding environment.</td></tr><tr><td>SERVICE</td><td>Test data obtained from a SERVICE call – values depend on the result of the service call. Service needs to be configured on Service screen inside application menu.</td></tr><tr><td>CSV</td><td>Test data obtained from a CSV file structure privided by a URL. Values depend on the result of the service call to CSV file.</td></tr></table>','_data_library')");
        b.append(",('testdatalib','type','','fr','Type',NULL,'_librairie_de_données')");
        b.append(",('testdatalibdata','column','','en','Column','<p>Column name representing the value that should be obtained after executing a SQL instruction (select).</p>','_data_library')");
        b.append(",('testdatalibdata','column','','fr','Colonne','<p>Nom de la colonne représentant la vakeur qui devrait être obtenu après l\\'execution du SQL</p>','_librairie_de_données')");
        b.append(",('testdatalibdata','columnPosition','','en','Column Position','<p>Column position [1,2,3…] representing the value that should be obtained after parsing a CSV file.</p>','_data_library')");
        b.append(",('testdatalibdata','columnPosition','','fr','Position','<p>Position [1,2,3…] de la valeur à obtenir lors du décryptage du CSV.</p>','_librairie_de_données')");
        b.append(",('testdatalibdata','description','','en','Description','<p>Textual description for the sub-data entry.</p>','_data_library')");
        b.append(",('testdatalibdata','description','','fr','Description',NULL,'_librairie_de_données')");
        b.append(",('testdatalibdata','parsingAnswer','','en','Parsing Answer','<p>XPath or JsonPath expression that allows the user to parse data from the SERVICE response.</p>','_data_library')");
        b.append(",('testdatalibdata','parsingAnswer','','fr','Analyse de la réponse',NULL,'_librairie_de_données')");
        b.append(",('testdatalibdata','subData','','en','Sub-data ','<p>Unique name for a sub-data entry. For a test data library entry, this value should be unique.</p>','_data_library')");
        b.append(",('testdatalibdata','subData','','fr','Sous-donnée',NULL,'_librairie_de_données')");
        b.append(",('testdatalibdata','value','','en','Value','<p>STATIC value.</p>','_data_library')");
        b.append(",('testdatalibdata','value','','fr','Valeur',NULL,'_librairie_de_données')");
        b.append(",('transversal','DateCreated','','en','Creation Date','Date of the creation of the object.','_data_library')");
        b.append(",('transversal','DateCreated','','fr','Date de Création','Date de création de l\\'objet.','_librairie_de_données')");
        b.append(",('transversal','DateModif','','en','Modification Date','Last modification Date of the object.','_data_library')");
        b.append(",('transversal','DateModif','','fr','Date de Modification','Date de dernière modification de l\\'objet.','_librairie_de_données')");
        b.append(",('transversal','UsrCreated','','en','Created by','User who created the object.','_data_library')");
        b.append(",('transversal','UsrCreated','','fr','Créé par','Utilisateur ayant créé l\\'objet.','_librairie_de_données')");
        b.append(",('transversal','UsrModif','','en','Modified by','Last modification date of the object.','_data_library')");
        b.append(",('transversal','UsrModif','','fr','Modifié par','Date de dernière modification de l\\'objet.','_librairie_de_données')");
        b.append(",('user','DefaultSystem','','en','Default System','This is the default <code class=\\'doc-crbvvoca\\'>system</code> the user works on the most. It is used to default the perimeter of <code class=\\'doc-crbvvoca\\'>test case</code> or <code class=\\'doc-crbvvoca\\'>applications</code> displayed on some Cerberus pages.','_user_management')");
        b.append(",('user','DefaultSystem','','fr','Système par defaut','Système par defaut','_management_des_utilisateurs')");
        b.append(",('user','Team','','en','Team','This is the team of the user.','_user_management')");
        b.append(",('user','Team','','fr','Equipe','Correspond à l\\'équipe de l\\'utilisateur','_management_des_utilisateurs')");
        b.append(",('usergroup','GroupName','','en','Group Name','Authorities are managed by group. In order to be granted to a set of feature, you must belong to the corresponding group.<br>Every user can of course belong to as many group as necessary in order to get access to as many feature as required.<br>In order to get the full access to the system you must belong to every group.<br>Some groups are linked together on the test perimeter and integration perimeter.<br><br><b>Test perimeter :</b><br><br><code class=\\'doc-fixed\\'>TestRO</code>: Has read only access to the information related to test cases and also has access to execution reporting options.<br><br><code class=\\'doc-fixed\\'>Test</code>: Can modify non WORKING test cases but cannot delete test cases.<br><br><code class=\\'doc-fixed\\'>TestAdmin</code>: Can modify or delete any test case (including Pre Testing test cases). Can also create or delete a test.<br><br>The minimum group you need to belong is <code class=\\'doc-fixed\\'>TestRO</code> that will give you access in read only to all test data (including its execution reporting page).<br>If you want to be able to modify the testcases (except the WORKING ones), you need <code class=\\'doc-fixed\\'>Test</code> group on top of <code class=\\'doc-fixed\\'>TestRO</code> group.<br>If you want the full access to all testcase (including beeing able to delete any testcase), you will need <code class=\\'doc-fixed\\'>TestAdmin</code> on top of <code class=\\'doc-fixed\\'>TestRO</code> and <code class=\\'doc-fixed\\'>Test</code> group.<br><br><b>Test Data perimeter :</b><br><br><code class=\\'doc-fixed\\'>TestDataManager</code>: Can modify the test data..<br><br><b>Test Execution perimeter :</b><br><br><code class=\\'doc-fixed\\'>RunTest</code>: Can run both Manual and Automated test cases from GUI.<br><br><b>Integration perimeter :</b><br><br><code class=\\'doc-fixed\\'>IntegratorRO</code>: Has access to the integration status.<br><br><code class=\\'doc-fixed\\'>Integrator</code>: Can add an application. Can change parameters of the environments.<br><br><code class=\\'doc-fixed\\'>IntegratorNewChain</code>: Can register the end of the chain execution. Has read only access to the other informations on the same page.<br><br><code class=\\'doc-fixed\\'>IntegratorDeploy</code>: Can disable or enable environments and register new build / revision.<br><br>The minimum group you need to belong is <code class=\\'doc-fixed\\'>IntegratorRO</code> that will give you access in read only to all environment data.<br>If you want to be able to modify the environment data, you need <code class=\\'doc-fixed\\'>Integrator</code> group on top of <code class=\\'doc-fixed\\'>IntegratorRO</code> group.<br><code class=\\'doc-fixed\\'>IntegratorNewChain</code> and <code class=\\'doc-fixed\\'>IntegratorDeploy</code> are used on top of <code class=\\'doc-fixed\\'>Integrator</code> Group to be able to create a new chain on an environment or perform a deploy operation.<br><br><b>Administration perimeter :</b><br><br><code class=\\'doc-fixed\\'>Administrator</code>: Can create, modify or delete users. Has access to log Event and Database Maintenance. Can change Parameter values.','_user_management')");
        b.append(",('usergroup','GroupName','','fr','Nom du groupe',NULL,'_management_des_utilisateurs')");
        a.add(b.toString());
        
        // Cleaned Actions.
        // 1310-1312
        b = new StringBuilder("DELETE FROM invariant where idname='ACTION' and value in ('getPageSource');");
        a.add(b.toString());
        b = new StringBuilder("UPDATE `invariant` SET `sort`='99999', `description`='[DEPRECATED] Remove differences from the given pattern' WHERE `idname`='ACTION' and`value`='removeDifference';");
        a.add(b.toString());
        b = new StringBuilder("UPDATE `invariant` SET `sort`='99999', `description`='[DEPRECATED] mouseOverAndWait' WHERE `idname`='ACTION' and`value`='mouseOverAndWait';");
        a.add(b.toString());

        // Cleaned Property type.
        // 1313-1314
        b = new StringBuilder("UPDATE `invariant` SET `value`='getFromSql' WHERE `idname`='PROPERTYTYPE' and`value`='executeSql';");
        a.add(b.toString());
        b = new StringBuilder("UPDATE testcasecountryproperties set type = 'getFromSql' where type = 'executeSql';");
        a.add(b.toString());
        

        return a;
    }

}
