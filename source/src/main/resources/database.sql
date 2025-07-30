-- That file contain all SQL instructions in order to build a new Cerberus Database.
-- All lines that are empty or start by '--' are ignored
-- All lines that start by a space are considering inside the same SQL as the previous line.
-- 
-- - Every Query must be independant.
--    - Drop and Create index of the table / columns inside the same SQL
--    - Drop and creation of Foreign Key inside the same SQL
-- - SQL must be fast (even on big tables)
--    - 1 Index or Foreign Key at a time.
--    - Beware of big tables that may result a timeout on the GUI side.
-- - Limit the number of SQL required in this class.
--    - When inserting some data in table, group them inside the same SQL
-- - Never insert an SQL between 2 SQL. Always append new SQL in the botton and never modify an old SQL (what is done is done and should not be modified).
--    - it messup the seq of SQL to execute in all users that moved to earlier version
-- - Only modify the SQL to fix an SQL issue but not to change a structure or enrich some data on an existing SQL. You need to create a new one to secure that it gets executed in all env. 
-- 

-- 1
CREATE TABLE `myversion` ( `Key` varchar(45) NOT NULL DEFAULT '', `Value` int(11) DEFAULT NULL, PRIMARY KEY (`Key`))
  ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 2
INSERT INTO `myversion` (`Key`, `Value`)
  VALUES ('database', 0);

-- 3
CREATE TABLE `log` (  `id` int(11) NOT NULL AUTO_INCREMENT,  `datecre` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,  `desc` varchar(20) DEFAULT NULL,  `longdesc` varchar(400) DEFAULT NULL,  `remoteIP` varchar(20) DEFAULT NULL,  `localIP` varchar(20) DEFAULT NULL,  PRIMARY KEY (`id`),  KEY `datecre` (`datecre`))
  ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- 4
CREATE TABLE `user` (  `UserID` int(10) unsigned NOT NULL AUTO_INCREMENT,  `Login` varchar(10) NOT NULL,  `Password` char(40) NOT NULL,  `Name` varchar(25) NOT NULL,  `Request` varchar(5) DEFAULT NULL,  `ReportingFavorite` varchar(1000) DEFAULT NULL,  `DefaultIP` varchar(45) DEFAULT NULL,  PRIMARY KEY (`UserID`),  UNIQUE KEY `ID1` (`Login`))
  ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- 5
INSERT INTO `user`
  VALUES (1,'admin','d033e22ae348aeb5660fc2140aec35850c4da997','Admin User','false',NULL,NULL)
  ,(2,'cerberus','b7e73576cd25a6756dfc25d9eb914ba235d4355d','Cerberus User','false',NULL,NULL);

-- 6
CREATE TABLE `usergroup` (  `Login` varchar(10) NOT NULL,  `GroupName` varchar(10) NOT NULL,  PRIMARY KEY (`Login`,`GroupName`))
  ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 7
INSERT INTO `usergroup`
  VALUES ('admin','Admin')
  ,('admin','User')
  ,('admin','Visitor')
  ,('admin','Integrator')
  ,('cerberus','User')
  ,('cerberus','Visitor')
  ,('cerberus','Integrator');

-- 8
CREATE TABLE `documentation` (  `DocTable` varchar(50) NOT NULL,  `DocField` varchar(45) NOT NULL,  `DocValue` varchar(60) NOT NULL DEFAULT '',  `DocLabel` varchar(60) DEFAULT NULL,  `DocDesc` varchar(10000) DEFAULT NULL,  PRIMARY KEY (`DocTable`,`DocField`,`DocValue`) USING BTREE)
  ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 9
CREATE TABLE `parameter` (  `param` varchar(100) NOT NULL,  `value` varchar(10000) NOT NULL,  `description` varchar(5000) NOT NULL,  PRIMARY KEY (`param`))
  ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 10
INSERT INTO `parameter`
  VALUES ('cerberus_homepage_nbbuildhistorydetail','5','Define the number of build/revision that are displayed in the homepage.')
  ,('cerberus_picture_path','/opt/CerberusMedias/executions/','Path to store the Cerberus Selenium Screenshot')
  ,('cerberus_picture_url','http://localhost/CerberusPictures/','Link to the Cerberus Selenium Screenshot. The following variable can be used : %ID% and %SCREENSHOT%')
  ,('cerberus_reporting_url','http://IP/Cerberus/ReportingExecution.jsp?Application=%appli%&TcActive=Y&Priority=All&Environment=%env%&Build=%build%&Revision=%rev%&Country=%country%&Status=WORKING&Apply=Apply','URL to Cerberus reporting screen. the following variables can be used : %country%, %env%,  %appli%, %build% and %rev%.')
  ,('cerberus_selenium_plugins_path','/tmp/','Path to load firefox plugins (Firebug + netExport) to do network traffic')
  ,('cerberus_support_email','<a href="mailto:support@domain.com?Subject=Cerberus%20Account" style="color: yellow">Support</a>','Contact Email in order to ask for new user in Cerberus tool.')
  ,('cerberus_testexecutiondetailpage_nbmaxexe','100','Default maximum number of testcase execution displayed in testcase execution detail page.')
  ,('cerberus_testexecutiondetailpage_nbmaxexe_max','5000','Maximum number of testcase execution displayed in testcase execution detail page.')
  ,('CI_OK_prio1','1','Coef in order to calculate the OK/KO result for CI platform.')
  ,('CI_OK_prio2','0.5','Coef in order to calculate the OK/KO result for CI platform.')
  ,('CI_OK_prio3','0.2','Coef in order to calculate the OK/KO result for CI platform.')
  ,('CI_OK_prio4','0.1','Coef in order to calculate the OK/KO result for CI platform.')
  ,('CI_OK_prio5','0','Coef in order to calculate the OK/KO result for CI platform.')
  ,('index_alert_body','','Body for alerts')
  ,('index_alert_from','QUALITY Team <team@mail.com>','From team for alerts')
  ,('index_alert_subject','[BAM] Alert detected for %COUNTRY%','Subject for alerts')
  ,('index_alert_to','QUALITY Team <team@mail.com>','List of contact for alerts')
  ,('index_notification_body_between','<br><br>','Text to display between the element of the mail')
  ,('index_notification_body_end','Subscribe / unsubscribe and get more realtime graph <a href="http://IP/index/BusinessActivityMonitor.jsp">here</a>. <font size="1">(Not available on Internet)</font><br><br>If you have any question, please contact us at <a href="mailto:mail@mail.com">mail@mail.com</a><br>Cumprimentos / Regards / Cordialement,<br>Test and Integration Team</body></html>','Test to display at the end')
  ,('index_notification_body_top','<html><body>Hello<br><br>Following is the activity monitored for %COUNTRY%, on the %DATEDEB%.<br><br>','Text to display at the top of the mail')
  ,('index_notification_subject','[BAM] Business Activity Monitor for %COUNTRY%','subject')
  ,('index_smtp_from','Team <team@mail.com>','smtp from used for notification')
  ,('index_smtp_host','smtp.mail.com','Smtp host used with notification')
  ,('index_smtp_port','25','smtp port used for notification ')
  ,('integration_notification_disableenvironment_body','Hello to all.<br><br>Use of environment %ENV% for country %COUNTRY% with Sprint %BUILD% (Revision %REVISION%) has been disabled, either to cancel the environment or to start deploying a new Sprint/revision.<br>Please don\'t use the applications until you receive further notification.<br><br>If you have any question, please contact us at mail@mail.com<br><br>Cumprimentos / Regards / Cordialement,<br><br>Test and Integration Team','Default Mail Body on event disableenvironment.')
  ,('integration_notification_disableenvironment_cc','Team <team@mail.com>','Default Mail cc on event disableenvironment.')
  ,('integration_notification_disableenvironment_subject','[TIT] Env %ENV% for %COUNTRY% (with Sprint %BUILD% revision %REVISION%) has been disabled for Maintenance.','Default Mail Subject on event disableenvironment.')
  ,('integration_notification_disableenvironment_to','Team <team@mail.com>','Default Mail to on event disableenvironment.')
  ,('integration_notification_newbuildrevision_body','Hello to all.<br><br>Sprint %BUILD% with Revisions %REVISION% is now available in %ENV%.<br>To access the corresponding application use the link:<br><a href="http://IP/index/?active=Y&env=%ENV%&country=%COUNTRY%">http://IP/index/?active=Y&env=%ENV%&country=%COUNTRY%</a><br><br>%BUILDCONTENT%<br>%TESTRECAP%<br>%TESTRECAPALL%<br>If you have any problem or question, please contact us at mail@mail.com<br><br>Cumprimentos / Regards / Cordialement,<br><br>Test and Integration Team','Default Mail Body on event newbuildrevision.')
  ,('integration_notification_newbuildrevision_cc','Team <team@mail.com>','Default Mail cc on event newbuildrevision.')
  ,('integration_notification_newbuildrevision_subject','[TIT] Sprint %BUILD% Revision %REVISION% is now ready to be used in %ENV% for %COUNTRY%.','Default Mail Subject on event newbuildrevision.')
  ,('integration_notification_newbuildrevision_to','Team <team@mail.com>','Default Mail to on event newchain.')
  ,('integration_notification_newchain_body','Hello to all.<br><br>A new Chain %CHAIN% has been executed in %ENV% for your country (%COUNTRY%).<br>Please perform your necessary test following that execution.<br><br>If you have any question, please contact us at mail@mail.com<br><br>Cumprimentos / Regards / Cordialement.','Default Mail Body on event newchain.')
  ,('integration_notification_newchain_cc','Team <team@mail.com>','Default Mail cc on event newchain.')
  ,('integration_notification_newchain_subject','[TIT] A New treatment %CHAIN% has been executed in %ENV% for %COUNTRY%.','Default Mail Subject on event newchain.')
  ,('integration_notification_newchain_to','Team <team@mail.com>','Default Mail to on event newchain.')
  ,('integration_smtp_from','Team <team@mail.com>','smtp from used for notification')
  ,('integration_smtp_host','mail.com','Smtp host used with notification')
  ,('integration_smtp_port','25','smtp port used for notification ')
  ,('jenkins_admin_password','toto','Jenkins Admin Password')
  ,('jenkins_admin_user','admin','Jenkins Admin Username')
  ,('jenkins_application_pipeline_url','http://IP:8210/view/Deploy/','Jenkins Application Pipeline URL. %APPLI% can be used to replace Application name.')
  ,('jenkins_deploy_pipeline_url','http://IP:8210/view/Deploy/','Jenkins Standard deploy Pipeline URL. ')
  ,('jenkins_deploy_url','http://IP:8210/job/STD-DEPLOY/buildWithParameters?token=buildit&DEPLOY_JOBNAME=%APPLI%&DEPLOY_BUILD=%JENKINSBUILDID%&DEPLOY_TYPE=%DEPLOYTYPE%&DEPLOY_ENV=%JENKINSAGENT%&SVN_REVISION=%RELEASE%','Link to Jenkins in order to trigger a standard deploy. %APPLI% %JENKINSBUILDID% %DEPLOYTYPE% %JENKINSAGENT% and %RELEASE% can be used.')
  ,('ticketing tool_bugtracking_url','http://IP/bugtracking/Lists/Bug%20Tracking/DispForm.aspx?ID=%bugid%&Source=http%3A%2F%2Fsitd_moss%2Fbugtracking%2FLists%2FBug%2520Tracking%2FAllOpenBugs.aspx','URL to SitdMoss Bug reporting screen. the following variable can be used : %bugid%.')
  ,('ticketing tool_newbugtracking_url','http://IP/bugtracking/Lists/Bug%20Tracking/NewForm.aspx?RootFolder=%2Fbugtracking%2FLists%2FBug%20Tracking&Source=http%3A%2F%2Fsitd_moss%2Fbugtracking%2FLists%2FBug%2520Tracking%2FAllOpenBugs.aspx','URL to SitdMoss Bug creation page.')
  ,('ticketing tool_ticketservice_url','http://IP/tickets/Lists/Tickets/DispForm.aspx?ID=%ticketid%','URL to SitdMoss Ticket Service page.')
  ,('sonar_application_dashboard_url','http://IP:8211/sonar/project/index/com.appli:%APPLI%','Sonar Application Dashboard URL. %APPLI% and %MAVENGROUPID% can be used to replace Application name.')
  ,('svn_application_url','http://IP/svn/SITD/%APPLI%','Link to SVN Repository. %APPLI% %TYPE% and %SYSTEM% can be used to replace Application name, type or system.');

-- 11
CREATE TABLE `invariant` (  `idname` varchar(50) NOT NULL,  `value` varchar(50) NOT NULL,  `sort` int(10) unsigned NOT NULL,  `id` int(10) unsigned NOT NULL,  `description` varchar(100) NOT NULL,  `gp1` varchar(45) DEFAULT NULL,  `gp2` varchar(45) DEFAULT NULL,  `gp3` varchar(45) DEFAULT NULL,  PRIMARY KEY (`id`,`sort`) USING BTREE)
  ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 12
INSERT INTO `invariant`
  VALUES ('STATUS','STANDBY',1,1,'Not implemented yet',NULL,NULL,NULL)
  ,('STATUS','IN PROGRESS',2,1,'Being implemented',NULL,NULL,NULL)
  ,('STATUS','TO BE IMPLEMENTED',3,1,'To be implemented',NULL,NULL,NULL)
  ,('STATUS','TO BE VALIDATED',4,1,'To be validated',NULL,NULL,NULL)
  ,('STATUS','WORKING',5,1,'Validated and Working',NULL,NULL,NULL)
  ,('STATUS','TO BE DELETED',6,1,'Should be deleted',NULL,NULL,NULL)
  ,('GROUP','COMPARATIVE',1,2,'Group of comparison tests',NULL,NULL,NULL)
  ,('GROUP','INTERACTIVE',2,2,'Group of interactive tests',NULL,NULL,NULL)
  ,('GROUP','PRIVATE',3,2,'Group of tests which not appear in Cerberus',NULL,NULL,NULL)
  ,('GROUP','PROCESS',4,2,'Group of tests which need a batch',NULL,NULL,NULL)
  ,('GROUP','MANUAL',5,2,'Group of test which cannot be automatized',NULL,NULL,NULL)
  ,('GROUP','',6,2,'Group of tests which are not already defined',NULL,NULL,NULL)
  ,('COUNTRY','BE',10,4,'Belgium','800',NULL,NULL)
  ,('COUNTRY','CH',11,4,'Switzerland','500',NULL,NULL)
  ,('COUNTRY','PT',15,4,'Portugal','200',NULL,NULL)
  ,('COUNTRY','UK',17,4,'Great Britan','300',NULL,NULL)
  ,('COUNTRY','DE',40,4,'Germany','600',NULL,NULL)
  ,('COUNTRY','FR',60,4,'France',NULL,NULL,NULL)
  ,('ENVIRONMENT','DEV',0,5,'Developpement','DEV',NULL,NULL)
  ,('ENVIRONMENT','QA',5,5,'Quality Assurance','QA',NULL,NULL)
  ,('ENVIRONMENT','UAT',30,5,'User Acceptance Test','UAT',NULL,NULL)
  ,('ENVIRONMENT','PROD',50,5,'Production','PROD',NULL,NULL)
  ,('ENVIRONMENT','PREPROD',60,5,'PreProduction','PROD',NULL,NULL)
  ,('SERVER','PRIMARY',1,6,'Primary Server',NULL,NULL,NULL)
  ,('SERVER','BACKUP1',2,6,'Backup 1',NULL,NULL,NULL)
  ,('SERVER','BACKUP2',3,6,'Backup 2',NULL,NULL,NULL)
  ,('SESSION','1',1,7,'Session 1',NULL,NULL,NULL)
  ,('SESSION','2',2,7,'Session 2',NULL,NULL,NULL)
  ,('SESSION','3',3,7,'Session 3',NULL,NULL,NULL)
  ,('SESSION','4',4,7,'Session 4',NULL,NULL,NULL)
  ,('SESSION','5',5,7,'Session 5',NULL,NULL,NULL)
  ,('SESSION','6',6,7,'Session 6',NULL,NULL,NULL)
  ,('SESSION','7',7,7,'Session 7',NULL,NULL,NULL)
  ,('SESSION','8',8,7,'Session 8',NULL,NULL,NULL)
  ,('SESSION','9',9,7,'Session 9',NULL,NULL,NULL)
  ,('SESSION','10',10,7,'Session 10',NULL,NULL,NULL)
  ,('BUILD','2012S2',13,8,'2012 Sprint 02',NULL,NULL,NULL)
  ,('BUILD','2013S1',14,8,'2013 Sprint 01',NULL,NULL,NULL)
  ,('REVISION','R00',1,9,'R00',NULL,NULL,NULL)
  ,('REVISION','R01',10,9,'R01',NULL,NULL,NULL)
  ,('REVISION','R02',20,9,'R02',NULL,NULL,NULL)
  ,('REVISION','R03',30,9,'R03',NULL,NULL,NULL)
  ,('REVISION','R04',40,9,'R04',NULL,NULL,NULL)
  ,('REVISION','R05',50,9,'R05',NULL,NULL,NULL)
  ,('REVISION','R06',60,9,'R06',NULL,NULL,NULL)
  ,('REVISION','R07',70,9,'R07',NULL,NULL,NULL)
  ,('REVISION','R08',80,9,'R08',NULL,NULL,NULL)
  ,('REVISION','R09',90,9,'R09',NULL,NULL,NULL)
  ,('REVISION','R10',100,9,'R10',NULL,NULL,NULL)
  ,('REVISION','R11',110,9,'R11',NULL,NULL,NULL)
  ,('REVISION','R12',120,9,'R12',NULL,NULL,NULL)
  ,('REVISION','R13',130,9,'R13',NULL,NULL,NULL)
  ,('REVISION','R14',140,9,'R14',NULL,NULL,NULL)
  ,('REVISION','R15',150,9,'R15',NULL,NULL,NULL)
  ,('REVISION','R16',160,9,'R16',NULL,NULL,NULL)
  ,('REVISION','R17',170,9,'R17',NULL,NULL,NULL)
  ,('REVISION','R18',180,9,'R18',NULL,NULL,NULL)
  ,('REVISION','R19',190,9,'R19',NULL,NULL,NULL)
  ,('REVISION','R20',200,9,'R20',NULL,NULL,NULL)
  ,('REVISION','R21',210,9,'R21',NULL,NULL,NULL)
  ,('REVISION','R22',220,9,'R22',NULL,NULL,NULL)
  ,('REVISION','R23',230,9,'R23',NULL,NULL,NULL)
  ,('REVISION','R24',240,9,'R24',NULL,NULL,NULL)
  ,('REVISION','R25',250,9,'R25',NULL,NULL,NULL)
  ,('REVISION','R26',260,9,'R26',NULL,NULL,NULL)
  ,('REVISION','R27',270,9,'R27',NULL,NULL,NULL)
  ,('REVISION','R28',280,9,'R28',NULL,NULL,NULL)
  ,('REVISION','R29',290,9,'R29',NULL,NULL,NULL)
  ,('REVISION','R30',300,9,'R30',NULL,NULL,NULL)
  ,('REVISION','R31',310,9,'R31',NULL,NULL,NULL)
  ,('REVISION','R32',320,9,'R32',NULL,NULL,NULL)
  ,('REVISION','R33',330,9,'R33',NULL,NULL,NULL)
  ,('REVISION','R34',340,9,'R34',NULL,NULL,NULL)
  ,('REVISION','R35',350,9,'R35',NULL,NULL,NULL)
  ,('REVISION','R36',360,9,'R36',NULL,NULL,NULL)
  ,('REVISION','R37',370,9,'R37',NULL,NULL,NULL)
  ,('REVISION','R38',380,9,'R38',NULL,NULL,NULL)
  ,('REVISION','R39',390,9,'R39',NULL,NULL,NULL)
  ,('REVISION','R40',400,9,'R40',NULL,NULL,NULL)
  ,('REVISION','R41',410,9,'R41',NULL,NULL,NULL)
  ,('REVISION','R42',420,9,'R42',NULL,NULL,NULL)
  ,('REVISION','R43',430,9,'R43',NULL,NULL,NULL)
  ,('REVISION','R44',440,9,'R44',NULL,NULL,NULL)
  ,('REVISION','R45',450,9,'R45',NULL,NULL,NULL)
  ,('REVISION','R46',460,9,'R46',NULL,NULL,NULL)
  ,('REVISION','R47',470,9,'R47',NULL,NULL,NULL)
  ,('REVISION','R48',480,9,'R48',NULL,NULL,NULL)
  ,('REVISION','R49',490,9,'R49',NULL,NULL,NULL)
  ,('REVISION','R50',500,9,'R50',NULL,NULL,NULL)
  ,('REVISION','R51',510,9,'R51',NULL,NULL,NULL)
  ,('REVISION','R52',520,9,'R52',NULL,NULL,NULL)
  ,('REVISION','R53',530,9,'R53',NULL,NULL,NULL)
  ,('REVISION','R54',540,9,'R54',NULL,NULL,NULL)
  ,('REVISION','R55',550,9,'R55',NULL,NULL,NULL)
  ,('REVISION','R56',560,9,'R56',NULL,NULL,NULL)
  ,('REVISION','R57',570,9,'R57',NULL,NULL,NULL)
  ,('REVISION','R58',580,9,'R58',NULL,NULL,NULL)
  ,('REVISION','R59',590,9,'R59',NULL,NULL,NULL)
  ,('REVISION','R60',600,9,'R60',NULL,NULL,NULL)
  ,('REVISION','R61',610,9,'R61',NULL,NULL,NULL)
  ,('REVISION','R62',620,9,'R62',NULL,NULL,NULL)
  ,('ENVTYPE','STD',1,10,'Regression and evolution Standard Testing.',NULL,NULL,NULL)
  ,('ENVTYPE','COMPARISON',2,10,'Comparison Testing. No GUI Tests are allowed.',NULL,NULL,NULL)
  ,('ENVACTIVE','Y',1,11,'Active',NULL,NULL,NULL)
  ,('ENVACTIVE','N',2,11,'Disable',NULL,NULL,NULL)
  ,('ACTION','addSelection',10,12,'addSelection',NULL,NULL,NULL)
  ,('ACTION','calculateProperty',20,12,'calculateProperty',NULL,NULL,NULL)
  ,('ACTION','click',30,12,'click',NULL,NULL,NULL)
  ,('ACTION','clickAndWait',40,12,'clickAndWait',NULL,NULL,NULL)
  ,('ACTION','doubleClick',45,12,'doubleClick',NULL,NULL,NULL)
  ,('ACTION','enter',50,12,'enter',NULL,NULL,NULL)
  ,('ACTION','keypress',55,12,'keypress',NULL,NULL,NULL)
  ,('ACTION','openUrlWithBase',60,12,'openUrlWithBase',NULL,NULL,NULL)
  ,('ACTION','removeSelection',70,12,'removeSelection',NULL,NULL,NULL)
  ,('ACTION','select',80,12,'select',NULL,NULL,NULL)
  ,('ACTION','selectAndWait',90,12,'selectAndWait',NULL,NULL,NULL)
  ,('ACTION','store',100,12,'store',NULL,NULL,NULL)
  ,('ACTION','type',110,12,'type',NULL,NULL,NULL)
  ,('ACTION','URLLOGIN',120,12,'URLLOGIN',NULL,NULL,NULL)
  ,('ACTION','verifyTextPresent',130,12,'verifyTextPresent',NULL,NULL,NULL)
  ,('ACTION','verifyTitle',140,12,'verifyTitle',NULL,NULL,NULL)
  ,('ACTION','verifyValue',150,12,'verifyValue',NULL,NULL,NULL)
  ,('ACTION','wait',160,12,'wait',NULL,NULL,NULL)
  ,('ACTION','waitForPage',170,12,'waitForPage',NULL,NULL,NULL)
  ,('CONTROL','PropertyIsEqualTo',10,13,'PropertyIsEqualTo',NULL,NULL,NULL)
  ,('CONTROL','PropertyIsGreaterThan',12,13,'PropertyIsGreaterThan',NULL,NULL,NULL)
  ,('CONTROL','PropertyIsMinorThan',14,13,'PropertyIsMinorThan',NULL,NULL,NULL)
  ,('CONTROL','verifyElementPresent',20,13,'verifyElementPresent',NULL,NULL,NULL)
  ,('CONTROL','verifyElementVisible',30,13,'verifyElementVisible',NULL,NULL,NULL)
  ,('CONTROL','verifyText',40,13,'verifyText',NULL,NULL,NULL)
  ,('CONTROL','verifyTextPresent',50,13,'verifyTextPresent',NULL,NULL,NULL)
  ,('CONTROL','verifytitle',60,13,'verifytitle',NULL,NULL,NULL)
  ,('CONTROL','verifyurl',70,13,'verifyurl',NULL,NULL,NULL)
  ,('CONTROL','verifyContainText',80,13,'Verify Contain Text',NULL,NULL,NULL)
  ,('CHAIN','0',1,14,'0',NULL,NULL,NULL)
  ,('CHAIN','1',2,14,'1',NULL,NULL,NULL)
  ,('PRIORITY','1',1,15,'Critical Priority',NULL,NULL,NULL)
  ,('PRIORITY','2',5,15,'High Priority',NULL,NULL,NULL)
  ,('PRIORITY','3',10,15,'Mid Priority',NULL,NULL,NULL)
  ,('PRIORITY','4',15,15,'Low Priority',NULL,NULL,NULL)
  ,('PRIORITY','5',20,15,'Lower Priority or cosmetic',NULL,NULL,NULL)
  ,('PRIORITY','99',25,15,'No Priority defined',NULL,NULL,NULL)
  ,('TCACTIVE','Y',1,16,'Yes',NULL,NULL,NULL)
  ,('TCACTIVE','N',2,16,'No',NULL,NULL,NULL)
  ,('TCREADONLY','N',1,17,'No',NULL,NULL,NULL)
  ,('TCREADONLY','Y',2,17,'Yes',NULL,NULL,NULL)
  ,('CTRLFATAL','Y',1,18,'Yes',NULL,NULL,NULL)
  ,('CTRLFATAL','N',2,18,'No',NULL,NULL,NULL)
  ,('PROPERTYTYPE','SQL',1,19,'SQL Query',NULL,NULL,NULL)
  ,('PROPERTYTYPE','HTML',2,19,'HTML ID Field',NULL,NULL,NULL)
  ,('PROPERTYTYPE','TEXT',3,19,'Fix Text value',NULL,NULL,NULL)
  ,('PROPERTYTYPE','LIB_SQL',4,19,'Using an SQL from the library',NULL,NULL,NULL)
  ,('PROPERTYNATURE','STATIC',1,20,'Static',NULL,NULL,NULL)
  ,('PROPERTYNATURE','RANDOM',2,20,'Random',NULL,NULL,NULL)
  ,('PROPERTYNATURE','RANDOMNEW',3,20,'Random New',NULL,NULL,NULL)
  ,('ORIGIN','AT',1,21,'Austria',NULL,NULL,NULL)
  ,('ORIGIN','BE',2,21,'Belgium',NULL,NULL,NULL)
  ,('ORIGIN','CH',3,21,'Switzerland',NULL,NULL,NULL)
  ,('ORIGIN','ES',4,21,'Spain',NULL,NULL,NULL)
  ,('ORIGIN','GR',5,21,'Greece',NULL,NULL,NULL)
  ,('ORIGIN','IT',6,21,'Italy',NULL,NULL,NULL)
  ,('ORIGIN','PT',7,21,'Portugal',NULL,NULL,NULL)
  ,('ORIGIN','RU',8,21,'Russia',NULL,NULL,NULL)
  ,('ORIGIN','UA',9,21,'Ukrainia',NULL,NULL,NULL)
  ,('ORIGIN','UK',10,21,'Great Britain',NULL,NULL,NULL)
  ,('ORIGIN','DE',15,21,'Germany',NULL,NULL,NULL)
  ,('ORIGIN','FR',16,21,'France',NULL,NULL,NULL)
  ,('PROPERTYDATABASE','EXAMPLE',1,22,'Example Fake Database',NULL,NULL,NULL)
  ,('OUTPUTFORMAT','gui',1,24,'GUI HTLM output','','',NULL)
  ,('OUTPUTFORMAT','compact',2,24,'Compact single line output.',NULL,NULL,NULL)
  ,('OUTPUTFORMAT','verbose-txt',3,24,'Verbose key=value format.',NULL,NULL,NULL)
  ,('VERBOSE','0',1,25,'Minimum log','','',NULL)
  ,('VERBOSE','1',2,25,'Standard log','','',NULL)
  ,('VERBOSE','2',3,25,'Maximum log',NULL,NULL,NULL)
  ,('RUNQA','Y',1,26,'Test can run in QA enviroment',NULL,NULL,NULL)
  ,('RUNQA','N',2,26,'Test cannot run in QA enviroment',NULL,NULL,NULL)
  ,('RUNUAT','Y',1,27,'Test can run in UAT environment',NULL,NULL,NULL)
  ,('RUNUAT','N',2,27,'Test cannot run in UAT environment',NULL,NULL,NULL)
  ,('RUNPROD','N',1,28,'Test cannot run in PROD environment',NULL,NULL,NULL)
  ,('RUNPROD','Y',2,28,'Test can run in PROD environment',NULL,NULL,NULL)
  ,('FILTERNBDAYS','14',1,29,'14 Days (2 weeks)',NULL,NULL,NULL)
  ,('FILTERNBDAYS','30',2,29,'30 Days (1 month)',NULL,NULL,NULL)
  ,('FILTERNBDAYS','182',3,29,'182 Days (6 months)',NULL,NULL,NULL)
  ,('FILTERNBDAYS','365',4,29,'365 Days (1 year)',NULL,NULL,NULL)
  ,('TCESTATUS','OK',1,35,'Test was fully executed and no bug are to be reported.',NULL,NULL,NULL)
  ,('TCESTATUS','KO',2,35,'Test was executed and bug have been detected.',NULL,NULL,NULL)
  ,('TCESTATUS','PE',3,35,'Test execution is still running...',NULL,NULL,NULL)
  ,('TCESTATUS','FA',4,35,'Test could not be executed because there is a bug on the test.',NULL,NULL,NULL)
  ,('TCESTATUS','NA',5,35,'Test could not be executed because some test data are not available.',NULL,NULL,NULL)
  ,('MAXEXEC','50',1,36,'50',NULL,NULL,NULL)
  ,('MAXEXEC','100',2,36,'100',NULL,NULL,NULL)
  ,('MAXEXEC','200',3,36,'200',NULL,NULL,NULL)
  ,('MAXEXEC','500',4,36,'500',NULL,NULL,NULL)
  ,('MAXEXEC','1000',5,36,'1000',NULL,NULL,NULL);

-- 13
CREATE TABLE `tag` (  `id` int(11) NOT NULL AUTO_INCREMENT,  `Tag` varchar(145) NOT NULL,  `TagDateCre` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,  PRIMARY KEY (`id`),  UNIQUE KEY `Tag_UNIQUE` (`Tag`))
  ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- 14
CREATE TABLE `deploytype` (  `deploytype` varchar(50) NOT NULL,  `description` varchar(200) DEFAULT '',  PRIMARY KEY (`deploytype`))
  ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 15
CREATE TABLE `application` (  `Application` varchar(45) NOT NULL,  `description` varchar(200) DEFAULT NULL,  `internal` varchar(1) NOT NULL COMMENT 'Application',  `sort` int(11) NOT NULL,  `type` varchar(10) DEFAULT NULL,  `system` varchar(45) NOT NULL DEFAULT '',  `svnurl` varchar(150) DEFAULT NULL,  `deploytype` varchar(50) DEFAULT NULL,  `mavengroupid` varchar(50) DEFAULT '',  PRIMARY KEY (`Application`),  KEY `FK_application` (`deploytype`),  CONSTRAINT `FK_application` FOREIGN KEY (`deploytype`) REFERENCES `deploytype` (`deploytype`) ON DELETE CASCADE ON UPDATE CASCADE)
  ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 16
CREATE TABLE `project` (  `idproject` varchar(45) NOT NULL,  `VCCode` varchar(20) DEFAULT NULL,  `Description` varchar(45) DEFAULT NULL,  `active` varchar(1) DEFAULT 'Y',  `datecre` timestamp NULL DEFAULT CURRENT_TIMESTAMP,  PRIMARY KEY (`idproject`))
  ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 17
CREATE TABLE `batchinvariant` (  `Batch` varchar(1) NOT NULL DEFAULT '',  `IncIni` varchar(45) DEFAULT NULL,  `Unit` varchar(45) DEFAULT NULL,  `Description` varchar(45) DEFAULT NULL,  PRIMARY KEY (`Batch`) USING BTREE)
  ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 18
CREATE TABLE `test` (  `Test` varchar(45) NOT NULL,  `Description` varchar(300) NOT NULL,  `Active` varchar(1) NOT NULL,  `Automated` varchar(1) NOT NULL,  `TDateCrea` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,  PRIMARY KEY (`Test`),  KEY `ix_Test_Active` (`Test`,`Active`))
  ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 19
INSERT INTO `application`
  VALUES ('Google','Google Website','N',240,'GUI','DEFAULT','',NULL,'');

-- 20
INSERT INTO `test`
  VALUES ('Examples','Example Tests','Y','Y','2012-06-19 09:56:06')
  ,('Performance Monitor','Performance Monitor Tests','Y','Y','2012-06-19 09:56:06')
  ,('Business Activity Monitor','Business Activity Monitor Tests','Y','Y','2012-06-19 09:56:06')
  ,('Pre Testing','Preliminary Tests','Y','Y','1970-01-01 01:01:01');

-- 21
CREATE TABLE `testcase` (  `Test` varchar(45) NOT NULL,  `TestCase` varchar(45) NOT NULL,  `Application` varchar(45) DEFAULT NULL,  `Project` varchar(45) DEFAULT NULL,  `Ticket` varchar(20) DEFAULT '',  `Description` varchar(500) NOT NULL,  `BehaviorOrValueExpected` varchar(2500) NOT NULL,  `ReadOnly` varchar(1) DEFAULT 'N',  `ChainNumberNeeded` int(10) unsigned DEFAULT NULL,  `Priority` int(1) unsigned NOT NULL,  `Status` varchar(25) NOT NULL,  `TcActive` varchar(1) NOT NULL,  `Group` varchar(45) DEFAULT NULL,  `Origine` varchar(45) DEFAULT NULL,  `RefOrigine` varchar(45) DEFAULT NULL,  `HowTo` varchar(2500) DEFAULT NULL,  `Comment` varchar(500) DEFAULT NULL,  `TCDateCrea` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,  `FromBuild` varchar(10) DEFAULT NULL,  `FromRev` varchar(20) DEFAULT NULL,  `ToBuild` varchar(10) DEFAULT NULL,  `ToRev` varchar(20) DEFAULT NULL,  `BugID` varchar(10) DEFAULT NULL,  `TargetBuild` varchar(10) DEFAULT NULL,  `TargetRev` varchar(20) DEFAULT NULL,  `Creator` varchar(45) DEFAULT NULL,  `Implementer` varchar(45) DEFAULT NULL,  `LastModifier` varchar(45) DEFAULT NULL,  `Sla` varchar(45) DEFAULT NULL,  `activeQA` varchar(1) DEFAULT 'Y',  `activeUAT` varchar(1) DEFAULT 'Y',  `activePROD` varchar(1) DEFAULT 'N',  PRIMARY KEY (`Test`,`TestCase`),  KEY `Index_2` (`Group`),  KEY `Index_3` (`Test`,`TestCase`,`Application`,`TcActive`,`Group`),  KEY `FK_testcase_2` (`Application`),  KEY `FK_testcase_3` (`Project`),  CONSTRAINT `FK_testcase_1` FOREIGN KEY (`Test`) REFERENCES `test` (`Test`) ON DELETE CASCADE ON UPDATE CASCADE,  CONSTRAINT `FK_testcase_2` FOREIGN KEY (`Application`) REFERENCES `application` (`Application`) ON DELETE CASCADE ON UPDATE CASCADE,  CONSTRAINT `FK_testcase_3` FOREIGN KEY (`Project`) REFERENCES `project` (`idproject`) ON DELETE CASCADE ON UPDATE CASCADE)
  ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 22
CREATE TABLE `testcasecountry` (  `Test` varchar(45) NOT NULL,  `TestCase` varchar(45) NOT NULL,  `Country` varchar(2) NOT NULL,  PRIMARY KEY (`Test`,`TestCase`,`Country`),  CONSTRAINT `FK_testcasecountry_1` FOREIGN KEY (`Test`, `TestCase`) REFERENCES `testcase` (`Test`, `TestCase`) ON DELETE CASCADE ON UPDATE CASCADE)
  ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 23
CREATE TABLE `testcasestep` (  `Test` varchar(45) NOT NULL,  `TestCase` varchar(45) NOT NULL,  `Step` int(10) unsigned NOT NULL,  `Description` varchar(150) NOT NULL,  PRIMARY KEY (`Test`,`TestCase`,`Step`),  CONSTRAINT `FK_testcasestep_1` FOREIGN KEY (`Test`, `TestCase`) REFERENCES `testcase` (`Test`, `TestCase`) ON DELETE CASCADE ON UPDATE CASCADE)
  ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 24
CREATE TABLE `testcasestepbatch` (  `Test` varchar(45) NOT NULL,  `TestCase` varchar(45) NOT NULL,  `Step` varchar(45) NOT NULL,  `Batch` varchar(1) NOT NULL DEFAULT '',  PRIMARY KEY (`Test`,`TestCase`,`Step`,`Batch`) USING BTREE,  KEY `fk_testcasestepbatch_1` (`Batch`),  CONSTRAINT `FK_testcasestepbatchl_1` FOREIGN KEY (`Test`, `TestCase`) REFERENCES `testcase` (`Test`, `TestCase`) ON DELETE CASCADE ON UPDATE CASCADE,  CONSTRAINT `FK_testcasestepbatch_2` FOREIGN KEY (`Batch`) REFERENCES `batchinvariant` (`Batch`) ON DELETE CASCADE ON UPDATE CASCADE)
  ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 25
CREATE TABLE `testcasecountryproperties` (  `Test` varchar(45) NOT NULL,  `TestCase` varchar(45) NOT NULL,  `Country` varchar(2) NOT NULL,  `Property` varchar(150) NOT NULL,  `Type` varchar(45) NOT NULL,  `Database` varchar(45) DEFAULT NULL,  `Value` varchar(2500) NOT NULL,  `Length` int(10) unsigned NOT NULL,  `RowLimit` int(10) unsigned NOT NULL,  `Nature` varchar(45) NOT NULL,  PRIMARY KEY (`Test`,`TestCase`,`Country`,`Property`) USING BTREE,  CONSTRAINT `FK_testcasecountryproperties_1` FOREIGN KEY (`Test`, `TestCase`, `Country`) REFERENCES `testcasecountry` (`Test`, `TestCase`, `Country`) ON DELETE CASCADE ON UPDATE CASCADE)
  ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 26
CREATE TABLE `testcasestepaction` (  `Test` varchar(45) NOT NULL,  `TestCase` varchar(45) NOT NULL,  `Step` int(10) unsigned NOT NULL,  `Sequence` int(10) unsigned NOT NULL,  `Action` varchar(45) NOT NULL DEFAULT '',  `Object` varchar(200) NOT NULL DEFAULT '',  `Property` varchar(45) DEFAULT NULL,  PRIMARY KEY (`Test`,`TestCase`,`Step`,`Sequence`),  CONSTRAINT `FK_testcasestepaction_1` FOREIGN KEY (`Test`, `TestCase`, `Step`) REFERENCES `testcasestep` (`Test`, `TestCase`, `Step`) ON DELETE CASCADE ON UPDATE CASCADE)
  ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 27
CREATE TABLE `testcasestepactioncontrol` (  `Test` varchar(45) NOT NULL,  `TestCase` varchar(45) NOT NULL,  `Step` int(10) unsigned NOT NULL,  `Sequence` int(10) unsigned NOT NULL,  `Control` int(10) unsigned NOT NULL,  `Type` varchar(200) NOT NULL DEFAULT '',  `ControlValue` varchar(200) NOT NULL DEFAULT '',  `ControlProperty` varchar(200) DEFAULT NULL,  `Fatal` varchar(1) DEFAULT 'Y',  PRIMARY KEY (`Test`,`Sequence`,`Step`,`TestCase`,`Control`) USING BTREE,  KEY `FK_testcasestepcontrol_1` (`Test`,`TestCase`,`Step`,`Sequence`),  CONSTRAINT `FK_testcasestepcontrol_1` FOREIGN KEY (`Test`, `TestCase`, `Step`, `Sequence`) REFERENCES `testcasestepaction` (`Test`, `TestCase`, `Step`, `Sequence`) ON DELETE CASCADE ON UPDATE CASCADE)
  ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 28
CREATE TABLE `sqllibrary` (  `Type` varchar(45) NOT NULL,  `Name` varchar(45) NOT NULL,  `Script` varchar(2500) NOT NULL,  `Description` varchar(1000) DEFAULT NULL,  PRIMARY KEY (`Name`))
  ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 29
CREATE TABLE `countryenvparam` (  `Country` varchar(2) NOT NULL,  `Environment` varchar(45) NOT NULL,  `Build` varchar(10) DEFAULT NULL,  `Revision` varchar(20) DEFAULT NULL,  `Chain` varchar(20) DEFAULT NULL,  `DistribList` text,  `EMailBodyRevision` text,  `Type` varchar(20) DEFAULT NULL,  `EMailBodyChain` text,  `EMailBodyDisableEnvironment` text,  `active` varchar(1) NOT NULL DEFAULT 'N',  `maintenanceact` varchar(1) DEFAULT 'N',  `maintenancestr` time DEFAULT NULL,  `maintenanceend` time DEFAULT NULL,  PRIMARY KEY (`Country`,`Environment`))
  ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 30
CREATE TABLE `countryenvironmentparameters` (  `Country` varchar(2) NOT NULL,  `Environment` varchar(45) NOT NULL,  `Application` varchar(45) NOT NULL,  `IP` varchar(45) NOT NULL,  `URL` varchar(150) NOT NULL,  `URLLOGIN` varchar(150) DEFAULT NULL,  `JdbcUser` varchar(45) DEFAULT NULL,  `JdbcPass` varchar(45) DEFAULT NULL,  `JdbcIP` varchar(45) DEFAULT NULL,  `JdbcPort` int(10) unsigned DEFAULT NULL,  `as400LIB` varchar(10) DEFAULT NULL,  PRIMARY KEY (`Country`,`Environment`,`Application`),  KEY `FK_countryenvironmentparameters_1` (`Country`,`Environment`),  KEY `FK_countryenvironmentparameters_3` (`Application`),  CONSTRAINT `FK_countryenvironmentparameters_1` FOREIGN KEY (`Country`, `Environment`) REFERENCES `countryenvparam` (`Country`, `Environment`) ON DELETE CASCADE ON UPDATE CASCADE,  CONSTRAINT `FK_countryenvironmentparameters_3` FOREIGN KEY (`Application`) REFERENCES `application` (`Application`) ON DELETE CASCADE ON UPDATE CASCADE)
  ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 31
CREATE TABLE `countryenvironmentdatabase` (  `Database` varchar(45) NOT NULL,  `Environment` varchar(45) NOT NULL,  `Country` varchar(2) NOT NULL,  `ConnectionPoolName` varchar(25) NOT NULL,  PRIMARY KEY (`Database`,`Environment`,`Country`),  KEY `FK_countryenvironmentdatabase_1` (`Country`,`Environment`),  CONSTRAINT `FK_countryenvironmentdatabase_1` FOREIGN KEY (`Country`, `Environment`) REFERENCES `countryenvparam` (`Country`, `Environment`) ON DELETE CASCADE ON UPDATE CASCADE)
  ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 32
CREATE TABLE `host` (  `Country` varchar(2) NOT NULL,  `Session` varchar(20) NOT NULL,  `Environment` varchar(45) NOT NULL,  `Server` varchar(20) NOT NULL,  `host` varchar(20) DEFAULT NULL,  `secure` varchar(1) DEFAULT 'N',  `port` varchar(20) DEFAULT NULL,  `active` varchar(1) DEFAULT 'Y',  PRIMARY KEY (`Country`,`Session`,`Environment`,`Server`) USING BTREE,  KEY `FK_host_1` (`Country`,`Environment`),  CONSTRAINT `FK_host_1` FOREIGN KEY (`Country`, `Environment`) REFERENCES `countryenvparam` (`Country`, `Environment`) ON DELETE CASCADE ON UPDATE CASCADE)
  ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 33
CREATE TABLE `countryenvparam_log` (  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,  `Country` varchar(2) NOT NULL,  `Environment` varchar(45) NOT NULL,  `Build` varchar(10) DEFAULT NULL,  `Revision` varchar(20) DEFAULT NULL,  `Chain` int(10) unsigned DEFAULT NULL,  `Description` varchar(150) DEFAULT NULL,  `datecre` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,  PRIMARY KEY (`id`),  KEY `ID1` (`Country`,`Environment`),  KEY `FK_countryenvparam_log_1` (`Country`,`Environment`),  CONSTRAINT `FK_countryenvparam_log_1` FOREIGN KEY (`Country`, `Environment`) REFERENCES `countryenvparam` (`Country`, `Environment`) ON DELETE CASCADE ON UPDATE CASCADE)
  ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- 34
CREATE TABLE `buildrevisionbatch` (  `ID` int(11) NOT NULL AUTO_INCREMENT,  `Batch` varchar(1) NOT NULL,  `Country` varchar(2) DEFAULT NULL,  `Build` varchar(45) DEFAULT NULL,  `Revision` varchar(45) DEFAULT NULL,  `Environment` varchar(45) DEFAULT NULL,  `DateBatch` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,  PRIMARY KEY (`ID`) USING BTREE,  KEY `FK_buildrevisionbatch_1` (`Batch`),  KEY `FK_buildrevisionbatch_2` (`Country`,`Environment`),  CONSTRAINT `FK_buildrevisionbatch_1` FOREIGN KEY (`Batch`) REFERENCES `batchinvariant` (`Batch`) ON DELETE CASCADE ON UPDATE CASCADE,  CONSTRAINT `FK_buildrevisionbatch_2` FOREIGN KEY (`Country`, `Environment`) REFERENCES `countryenvparam` (`Country`, `Environment`) ON DELETE CASCADE ON UPDATE CASCADE)
  ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- 35
CREATE TABLE `buildrevisionparameters` (  `ID` int(11) NOT NULL AUTO_INCREMENT,  `Build` varchar(10) DEFAULT NULL,  `Revision` varchar(20) DEFAULT NULL,  `Release` varchar(40) DEFAULT NULL,  `Application` varchar(45) DEFAULT NULL,  `Project` varchar(45) DEFAULT '',  `TicketIDFixed` varchar(45) DEFAULT '',  `BugIDFixed` varchar(45) DEFAULT '',  `Link` varchar(300) DEFAULT '',  `ReleaseOwner` varchar(100) NOT NULL DEFAULT '',  `Subject` varchar(1000) DEFAULT '',  `datecre` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,  `jenkinsbuildid` varchar(200) DEFAULT '',  `mavengroupid` varchar(200) DEFAULT '',  `mavenartifactid` varchar(200) DEFAULT '',  `mavenversion` varchar(200) DEFAULT '',  PRIMARY KEY (`ID`),  KEY `FK1` (`Application`),  CONSTRAINT `FK1` FOREIGN KEY (`Application`) REFERENCES `application` (`Application`) ON DELETE CASCADE ON UPDATE CASCADE)
  ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- 36
CREATE TABLE `logevent` (  `LogEventID` int(10) unsigned NOT NULL AUTO_INCREMENT,  `UserID` int(10) unsigned NOT NULL,  `Time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,  `Page` varchar(25) DEFAULT NULL,  `Action` varchar(50) DEFAULT NULL,  `Log` varchar(500) DEFAULT NULL,  PRIMARY KEY (`LogEventID`))
  ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- 37
CREATE TABLE `logeventchange` (  `LogEventChangeID` int(10) unsigned NOT NULL AUTO_INCREMENT,  `LogEventID` int(10) unsigned NOT NULL,  `LogTable` varchar(50) DEFAULT NULL,  `LogBefore` varchar(5000) DEFAULT NULL,  `LogAfter` varchar(5000) DEFAULT NULL,  `datecre` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,  PRIMARY KEY (`LogEventChangeID`))
  ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- 38
CREATE TABLE `testcaseexecution` (  `ID` bigint(20) unsigned NOT NULL AUTO_INCREMENT,  `Test` varchar(45) NOT NULL,  `TestCase` varchar(45) NOT NULL,  `Build` varchar(10) DEFAULT NULL,  `Revision` varchar(5) DEFAULT NULL,  `Environment` varchar(45) DEFAULT NULL,  `Country` varchar(2) DEFAULT NULL,  `Browser` varchar(20) DEFAULT NULL,  `Start` timestamp NULL DEFAULT CURRENT_TIMESTAMP,  `End` timestamp NULL DEFAULT '1970-01-01 01:01:01',  `ControlStatus` varchar(2) DEFAULT NULL,  `Application` varchar(45) DEFAULT NULL,  `IP` varchar(45) DEFAULT NULL,  `URL` varchar(150) DEFAULT NULL,  `Port` varchar(45) DEFAULT NULL,  `Tag` varchar(50) DEFAULT NULL,  `Finished` varchar(1) DEFAULT NULL,  `Verbose` varchar(1) DEFAULT NULL,  `Status` varchar(25) DEFAULT NULL,  PRIMARY KEY (`ID`),  KEY `FK_TestCaseExecution_1` (`Test`,`TestCase`),  KEY `fk_testcaseexecution_2` (`Tag`),  KEY `index_1` (`Start`),  KEY `IX_test_testcase_country` (`Test`,`TestCase`,`Country`,`Start`,`ControlStatus`),  KEY `index_buildrev` (`Build`,`Revision`),  KEY `FK_testcaseexecution_3` (`Application`),  KEY `fk_test` (`Test`),  KEY `ix_TestcaseExecution` (`Test`,`TestCase`,`Build`,`Revision`,`Environment`,`Country`,`ID`),  CONSTRAINT `FK_testcaseexecution_1` FOREIGN KEY (`Test`, `TestCase`) REFERENCES `testcase` (`Test`, `TestCase`) ON DELETE CASCADE ON UPDATE CASCADE,  CONSTRAINT `FK_testcaseexecution_3` FOREIGN KEY (`Application`) REFERENCES `application` (`Application`) ON DELETE CASCADE ON UPDATE CASCADE)
  ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8;

-- 39
CREATE TABLE `testcaseexecutiondata` (  `ID` bigint(20) unsigned NOT NULL AUTO_INCREMENT,  `Property` varchar(150) NOT NULL,  `Value` varchar(150) NOT NULL,  `Type` varchar(200) DEFAULT NULL,  `Object` varchar(2500) DEFAULT NULL,  `RC` varchar(10) DEFAULT NULL,  `Start` timestamp NULL DEFAULT NULL,  `End` timestamp NULL DEFAULT NULL,  `StartLong` bigint(20) DEFAULT NULL,  `EndLong` bigint(20) DEFAULT NULL,  PRIMARY KEY (`ID`,`Property`),  KEY `propertystart` (`Property`,`Start`),  KEY `index_1` (`Start`),  CONSTRAINT `FK_TestCaseExecutionData_1` FOREIGN KEY (`ID`) REFERENCES `testcaseexecution` (`ID`) ON DELETE CASCADE)
  ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- 40
CREATE TABLE `testcaseexecutionwwwdet` (  `ID` bigint(20) NOT NULL AUTO_INCREMENT,  `ExecID` bigint(20) unsigned NOT NULL,  `Start` varchar(45) DEFAULT NULL,  `url` varchar(500) DEFAULT NULL,  `End` varchar(45) DEFAULT NULL,  `ext` varchar(10) DEFAULT NULL,  `statusCode` int(11) DEFAULT NULL,  `method` varchar(10) DEFAULT NULL,  `bytes` int(11) DEFAULT NULL,  `timeInMillis` int(11) DEFAULT NULL,  `ReqHeader_Host` varchar(45) DEFAULT NULL,  `ResHeader_ContentType` varchar(45) DEFAULT NULL,  `ReqPage` varchar(500) DEFAULT NULL,  PRIMARY KEY (`ID`),  KEY `FK_testcaseexecutionwwwdet_1` (`ExecID`),  CONSTRAINT `FK_testcaseexecutionwwwdet_1` FOREIGN KEY (`ExecID`) REFERENCES `testcaseexecution` (`ID`) ON DELETE CASCADE ON UPDATE CASCADE)
  ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- 41
CREATE TABLE `testcaseexecutionwwwsum` (  `ID` bigint(20) unsigned NOT NULL,  `tot_nbhits` int(11) DEFAULT NULL,  `tot_tps` int(11) DEFAULT NULL,  `tot_size` int(11) DEFAULT NULL,  `nb_rc2xx` int(11) DEFAULT NULL,  `nb_rc3xx` int(11) DEFAULT NULL,  `nb_rc4xx` int(11) DEFAULT NULL,  `nb_rc5xx` int(11) DEFAULT NULL,  `img_nb` int(11) DEFAULT NULL,  `img_tps` int(11) DEFAULT NULL,  `img_size_tot` int(11) DEFAULT NULL,  `img_size_max` int(11) DEFAULT NULL,  `js_nb` int(11) DEFAULT NULL,  `js_tps` int(11) DEFAULT NULL,  `js_size_tot` int(11) DEFAULT NULL,  `js_size_max` int(11) DEFAULT NULL,  `css_nb` int(11) DEFAULT NULL,  `css_tps` int(11) DEFAULT NULL,  `css_size_tot` int(11) DEFAULT NULL,  `css_size_max` int(11) DEFAULT NULL,  `img_size_max_url` varchar(500) DEFAULT NULL,  `js_size_max_url` varchar(500) DEFAULT NULL,  `css_size_max_url` varchar(500) DEFAULT NULL,  PRIMARY KEY (`ID`),  KEY `FK_testcaseexecutionwwwsum_1` (`ID`),  CONSTRAINT `FK_testcaseexecutionwwwsum_1` FOREIGN KEY (`ID`) REFERENCES `testcaseexecution` (`ID`) ON DELETE CASCADE ON UPDATE CASCADE)
  ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 42
CREATE TABLE `testcasestepactionexecution` (  `ID` bigint(20) NOT NULL,  `Step` int(10) NOT NULL,  `Sequence` int(10) NOT NULL,  `Action` varchar(45) NOT NULL,  `Object` varchar(200) DEFAULT NULL,  `Property` varchar(45) DEFAULT NULL,  `Start` timestamp NULL DEFAULT NULL,  `End` timestamp NULL DEFAULT NULL,  `StartLong` bigint(20) DEFAULT NULL,  `EndLong` bigint(20) DEFAULT NULL,  PRIMARY KEY (`ID`,`Step`,`Sequence`,`Action`))
  ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 43
CREATE TABLE `testcasestepexecution` (  `ID` bigint(20) unsigned NOT NULL,  `Step` int(10) unsigned NOT NULL,  `BatNumExe` varchar(45) DEFAULT NULL,  `Start` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,  `End` timestamp NOT NULL DEFAULT '1970-01-01 01:01:01',  `FullStart` bigint(20) unsigned DEFAULT NULL,  `FullEnd` bigint(20) unsigned DEFAULT NULL,  `TimeElapsed` decimal(10,3) DEFAULT NULL,  `ReturnCode` varchar(2) DEFAULT NULL,  PRIMARY KEY (`ID`,`Step`),  CONSTRAINT `FK_testcasestepexecution_1` FOREIGN KEY (`ID`) REFERENCES `testcaseexecution` (`ID`) ON DELETE CASCADE)
  ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 44
CREATE TABLE `testcasestepactioncontrolexecution` (  `ID` bigint(20) unsigned NOT NULL,  `Step` int(10) unsigned NOT NULL,  `Sequence` int(10) unsigned NOT NULL,  `Control` int(10) unsigned NOT NULL,  `ReturnCode` varchar(2) NOT NULL,  `ControlType` varchar(200) DEFAULT NULL,  `ControlProperty` varchar(2500) DEFAULT NULL,  `ControlValue` varchar(200) DEFAULT NULL,  `Fatal` varchar(1) DEFAULT NULL,  `Start` timestamp NULL DEFAULT NULL,  `End` timestamp NULL DEFAULT NULL,  `StartLong` bigint(20) DEFAULT NULL,  `EndLong` bigint(20) DEFAULT NULL,  PRIMARY KEY (`ID`,`Step`,`Sequence`,`Control`) USING BTREE,  CONSTRAINT `FK_testcasestepcontrolexecution_1` FOREIGN KEY (`ID`, `Step`) REFERENCES `testcasestepexecution` (`ID`, `Step`) ON DELETE CASCADE)
  ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 45
CREATE TABLE `comparisonstatusdata` (  `idcomparisonstatusdata` int(11) NOT NULL AUTO_INCREMENT,  `Execution_ID` bigint(20) unsigned DEFAULT NULL,  `Property` varchar(45) DEFAULT NULL,  `Property_A` varchar(45) DEFAULT NULL,  `Property_B` varchar(45) DEFAULT NULL,  `Property_C` varchar(45) DEFAULT NULL,  `Status` varchar(45) DEFAULT NULL,  `Comments` varchar(1000) DEFAULT NULL,  PRIMARY KEY (`idcomparisonstatusdata`),  KEY `FK_comparisonstatusdata_1` (`Execution_ID`),  CONSTRAINT `FK_comparisonstatusdata_1` FOREIGN KEY (`Execution_ID`) REFERENCES `testcaseexecution` (`ID`) ON DELETE CASCADE ON UPDATE CASCADE)
  ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- 46
CREATE TABLE `comparisonstatus` (  `idcomparisonstatus` int(11) NOT NULL AUTO_INCREMENT,  `Execution_ID` bigint(20) unsigned DEFAULT NULL,  `Country` varchar(2) DEFAULT NULL,  `Environment` varchar(45) DEFAULT NULL,  `InvoicingDate` varchar(45) DEFAULT NULL,  `TestedChain` varchar(45) DEFAULT NULL,  `Start` varchar(45) DEFAULT NULL,  `End` varchar(45) DEFAULT NULL,  PRIMARY KEY (`idcomparisonstatus`),  KEY `FK_comparisonstatus_1` (`Execution_ID`),  CONSTRAINT `FK_comparisonstatus_1` FOREIGN KEY (`Execution_ID`) REFERENCES `testcaseexecution` (`ID`) ON DELETE CASCADE ON UPDATE CASCADE)
  ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- 47
INSERT INTO `project` (`idproject`, `VCCode`, `Description`, `active`)
  VALUES (' ', ' ', 'None', 'N');

-- 48
INSERT INTO `testcase`
  VALUES ('Examples','0001A','Google',' ','','Search for Cerberus Website','','Y',NULL,1,'WORKING','Y','INTERACTIVE','FR','','','','2012-06-19 09:56:40','','','','','','','','cerberus','cerberus','cerberus',NULL,'Y','Y','Y');

-- 49
INSERT INTO `testcasecountry`
  VALUES ('Examples','0001A','FR');

-- 50
INSERT INTO `testcasestep`
  VALUES ('Examples','0001A',1,'Search');

-- 51
INSERT INTO `testcasecountryproperties`
  VALUES ('Examples','0001A','FR','MYTEXT','text','','cerberus automated testing',0,0,'STATIC'), ('Examples','0001A','FR','WAIT','text','','5000',0,0,'STATIC');

-- 52
INSERT INTO `testcasestepaction`
  VALUES ('Examples','0001A',1,10,'openUrlLogin','','')
  ,('Examples','0001A',1,20,'type','lst-ib','MYTEXT')
  ,('Examples','0001A',1,30,'click','name=btnK','');

-- 53
INSERT INTO `testcasestepactioncontrol`
  VALUES ('Examples','0001A',1,30,1,'verifyTextInPage','','Welcome to Cerberus Website','Y');

-- 54
SELECT 1 FROM dual;

-- 55
CREATE TABLE `abonnement` (  `idabonnement` int(11) NOT NULL AUTO_INCREMENT,  `email` varchar(45) DEFAULT NULL,  `notification` varchar(1000) DEFAULT NULL,  `frequency` varchar(45) DEFAULT NULL,  `LastNotification` varchar(45) DEFAULT NULL,  PRIMARY KEY (`idabonnement`))
  ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- 56
CREATE TABLE `countryenvdeploytype` (  `Country` varchar(2) NOT NULL,  `Environment` varchar(45) NOT NULL,  `deploytype` varchar(50) NOT NULL,  `JenkinsAgent` varchar(50) NOT NULL DEFAULT '',  PRIMARY KEY (`Country`,`Environment`,`deploytype`,`JenkinsAgent`),  KEY `FK_countryenvdeploytype_1` (`Country`,`Environment`),  KEY `FK_countryenvdeploytype_2` (`deploytype`),  CONSTRAINT `FK_countryenvdeploytype_1` FOREIGN KEY (`deploytype`) REFERENCES `deploytype` (`deploytype`) ON DELETE CASCADE ON UPDATE CASCADE,  CONSTRAINT `FK_countryenvdeploytype_2` FOREIGN KEY (`Country`, `Environment`) REFERENCES `countryenvparam` (`Country`, `Environment`) ON DELETE CASCADE ON UPDATE CASCADE)
  ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 57
CREATE TABLE `logglassfish` (  `idlogglassfish` int(11) NOT NULL AUTO_INCREMENT,  `TIMESTAMP` varchar(45) DEFAULT 'CURRENT_TIMESTAMP',  `PARAMETER` varchar(2000) DEFAULT NULL,  `VALUE` varchar(2000) DEFAULT NULL,  PRIMARY KEY (`idlogglassfish`))
  ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 58
CREATE TABLE `qualitynonconformities` (  `idqualitynonconformities` int(11) NOT NULL AUTO_INCREMENT,  `Country` varchar(45) DEFAULT NULL,  `Application` varchar(45) DEFAULT NULL,  `ProblemCategory` varchar(100) DEFAULT NULL, `ProblemDescription` varchar(2500) DEFAULT NULL, `StartDate` varchar(45) DEFAULT NULL,  `StartTime` varchar(45) DEFAULT NULL,  `EndDate` varchar(45) DEFAULT NULL,  `EndTime` varchar(45) DEFAULT NULL, `TeamContacted` varchar(250) DEFAULT NULL,  `Actions` varchar(2500) DEFAULT NULL,  `RootCauseCategory` varchar(100) DEFAULT NULL,  `RootCauseDescription` varchar(2500) DEFAULT NULL,  `ImpactOrCost` varchar(45) DEFAULT NULL,  `Responsabilities` varchar(250) DEFAULT NULL,  `Status` varchar(45) DEFAULT NULL,  `Comments` varchar(1000) DEFAULT NULL,  `Severity` varchar(45) DEFAULT NULL,  PRIMARY KEY (`idqualitynonconformities`))
  ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- 59
CREATE TABLE `qualitynonconformitiesimpact` (  `idqualitynonconformitiesimpact` bigint(20) NOT NULL AUTO_INCREMENT,  `idqualitynonconformities` int(11) DEFAULT NULL,  `Country` varchar(45) DEFAULT NULL,  `Application` varchar(45) DEFAULT NULL,  `StartDate` varchar(45) DEFAULT NULL,  `StartTime` varchar(45) DEFAULT NULL,  `EndDate` varchar(45) DEFAULT NULL,  `EndTime` varchar(45) DEFAULT NULL,  `ImpactOrCost` varchar(250) DEFAULT NULL,  PRIMARY KEY (`idqualitynonconformitiesimpact`))
  ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- 60
ALTER TABLE `application` CHANGE COLUMN `System` `System` VARCHAR(45) NOT NULL DEFAULT 'DEFAULT'  ;

-- 61
ALTER TABLE `application` ADD COLUMN `SubSystem` VARCHAR(45) NOT NULL DEFAULT ''  AFTER `System` ;

-- 62
UPDATE application SET subsystem='system';

-- 63
UPDATE application SET `system`='DEFAULT';

-- 64
SELECT 1 FROM dual;

-- 65
DROP TABLE `tag`;

-- 66
ALTER TABLE `testcaseexecution` ADD COLUMN `CrbVersion` VARCHAR(45) NULL DEFAULT NULL  AFTER `Status` ;

-- 67
SELECT 1 FROM dual;

-- 68
ALTER TABLE `testcasestepactioncontrolexecution` ADD COLUMN `ScreenshotFilename` VARCHAR(45) NULL DEFAULT NULL  AFTER `EndLong` ;

-- 69
ALTER TABLE `testcasestepactionexecution` ADD COLUMN `ScreenshotFilename` VARCHAR(45) NULL DEFAULT NULL  AFTER `EndLong` ;

-- 70
SELECT 1 FROM dual;

-- 71
SELECT 1 FROM dual;

-- 72
ALTER TABLE `testcasestepexecution` ADD COLUMN `Test` VARCHAR(45) NULL DEFAULT NULL  AFTER `Step` , ADD COLUMN `TestCase` VARCHAR(45) NULL DEFAULT NULL  AFTER `Test` ;

-- 73
ALTER TABLE `testcasestepexecution` CHANGE COLUMN `Step` `Step` INT(10) UNSIGNED NOT NULL  AFTER `TestCase` ;

-- 74
ALTER TABLE `testcasestepactionexecution` ADD COLUMN `Test` VARCHAR(45) NULL DEFAULT NULL  AFTER `ID` , ADD COLUMN `TestCase` VARCHAR(45) NULL DEFAULT NULL  AFTER `Test` ;

-- 75
ALTER TABLE `testcasestepactioncontrolexecution` ADD COLUMN `Test` VARCHAR(45) NULL DEFAULT NULL  AFTER `ID` , ADD COLUMN `TestCase` VARCHAR(45) NULL DEFAULT NULL  AFTER `Test` ;

-- 76
ALTER TABLE `application` DROP INDEX `FK_application` , ADD INDEX `FK_application_01` (`deploytype` ASC) ;

-- 77
ALTER TABLE `application` DROP FOREIGN KEY `FK_application` ;

-- 78
ALTER TABLE `application` ADD CONSTRAINT `FK_application_01` FOREIGN KEY (`deploytype` ) REFERENCES `deploytype` (`deploytype` ) ON DELETE CASCADE ON UPDATE CASCADE;

-- 79
ALTER TABLE `buildrevisionbatch` DROP INDEX `FK_buildrevisionbatch_1` , ADD INDEX `FK_buildrevisionbatch_01` (`Batch` ASC) , DROP INDEX `FK_buildrevisionbatch_2` , ADD INDEX `FK_buildrevisionbatch_02` (`Country` ASC, `Environment` ASC) ;

-- 80
ALTER TABLE `buildrevisionbatch` DROP FOREIGN KEY `FK_buildrevisionbatch_1` , DROP FOREIGN KEY `FK_buildrevisionbatch_2` ;

-- 81
ALTER TABLE `buildrevisionbatch`   ADD CONSTRAINT `FK_buildrevisionbatch_01`  FOREIGN KEY (`Batch` )  REFERENCES `batchinvariant` (`Batch` )  ON DELETE CASCADE  ON UPDATE CASCADE,   ADD CONSTRAINT `FK_buildrevisionbatch_02`  FOREIGN KEY (`Country` , `Environment` )  REFERENCES `countryenvparam` (`Country` , `Environment` )  ON DELETE CASCADE  ON UPDATE CASCADE;

-- 82
ALTER TABLE `buildrevisionparameters` DROP INDEX `FK1` , ADD INDEX `FK_buildrevisionparameters_01` (`Application` ASC) ;

-- 83
ALTER TABLE `buildrevisionparameters` DROP FOREIGN KEY `FK1` ;

-- 84
ALTER TABLE `buildrevisionparameters`   ADD CONSTRAINT `FK_buildrevisionparameters_01`  FOREIGN KEY (`Application` )  REFERENCES `application` (`Application` )  ON DELETE CASCADE  ON UPDATE CASCADE;

-- 85
ALTER TABLE `comparisonstatus` DROP FOREIGN KEY `FK_comparisonstatus_1` ;

-- 86
ALTER TABLE `comparisonstatus`   ADD CONSTRAINT `FK_comparisonstatus_01`  FOREIGN KEY (`Execution_ID` )  REFERENCES `testcaseexecution` (`ID` )  ON DELETE CASCADE  ON UPDATE CASCADE, DROP INDEX `FK_comparisonstatus_1` , ADD INDEX `FK_comparisonstatus_01` (`Execution_ID` ASC) ;

-- 87
ALTER TABLE `comparisonstatusdata` DROP FOREIGN KEY `FK_comparisonstatusdata_1` ;

-- 88
ALTER TABLE `comparisonstatusdata`   ADD CONSTRAINT `FK_comparisonstatusdata_01`  FOREIGN KEY (`Execution_ID` )  REFERENCES `testcaseexecution` (`ID` )  ON DELETE CASCADE  ON UPDATE CASCADE, DROP INDEX `FK_comparisonstatusdata_1` , ADD INDEX `FK_comparisonstatusdata_01` (`Execution_ID` ASC) ;

-- 89
ALTER TABLE `countryenvdeploytype` DROP FOREIGN KEY `FK_countryenvdeploytype_1` , DROP FOREIGN KEY `FK_countryenvdeploytype_2` ;

-- 90
ALTER TABLE `countryenvdeploytype`   ADD CONSTRAINT `FK_countryenvdeploytype_01`  FOREIGN KEY (`deploytype` )  REFERENCES `deploytype` (`deploytype` )  ON DELETE CASCADE  ON UPDATE CASCADE,   ADD CONSTRAINT `FK_countryenvdeploytype_02`  FOREIGN KEY (`Country` , `Environment` )  REFERENCES `countryenvparam` (`Country` , `Environment` )  ON DELETE CASCADE  ON UPDATE CASCADE, DROP INDEX `FK_countryenvdeploytype_1` , ADD INDEX `FK_countryenvdeploytype_01` (`Country` ASC, `Environment` ASC) , DROP INDEX `FK_countryenvdeploytype_2` , ADD INDEX `FK_countryenvdeploytype_02` (`deploytype` ASC) ;

-- 91
ALTER TABLE `countryenvironmentdatabase` DROP FOREIGN KEY `FK_countryenvironmentdatabase_1` ;

-- 92
ALTER TABLE `countryenvironmentdatabase`   ADD CONSTRAINT `FK_countryenvironmentdatabase_01`  FOREIGN KEY (`Country` , `Environment` )  REFERENCES `countryenvparam` (`Country` , `Environment` )  ON DELETE CASCADE  ON UPDATE CASCADE, DROP INDEX `FK_countryenvironmentdatabase_1` , ADD INDEX `FK_countryenvironmentdatabase_01` (`Country` ASC, `Environment` ASC) ;

-- 93
ALTER TABLE `countryenvironmentparameters` DROP FOREIGN KEY `FK_countryenvironmentparameters_1` , DROP FOREIGN KEY `FK_countryenvironmentparameters_3` ;

-- 94
ALTER TABLE `countryenvironmentparameters`   ADD CONSTRAINT `FK_countryenvironmentparameters_01`  FOREIGN KEY (`Country` , `Environment` )  REFERENCES `countryenvparam` (`Country` , `Environment` )  ON DELETE CASCADE  ON UPDATE CASCADE,   ADD CONSTRAINT `FK_countryenvironmentparameters_02`  FOREIGN KEY (`Application` )  REFERENCES `application` (`Application` )  ON DELETE CASCADE  ON UPDATE CASCADE, DROP INDEX `FK_countryenvironmentparameters_1` , ADD INDEX `FK_countryenvironmentparameters_01` (`Country` ASC, `Environment` ASC) , DROP INDEX `FK_countryenvironmentparameters_3` , ADD INDEX `FK_countryenvironmentparameters_02` (`Application` ASC) ;

-- 95
ALTER TABLE `countryenvparam_log` DROP FOREIGN KEY `FK_countryenvparam_log_1` ;

-- 96
ALTER TABLE `countryenvparam_log`   ADD CONSTRAINT `FK_countryenvparam_log_01`  FOREIGN KEY (`Country` , `Environment` )  REFERENCES `countryenvparam` (`Country` , `Environment` )  ON DELETE CASCADE  ON UPDATE CASCADE, DROP INDEX `FK_countryenvparam_log_1` , ADD INDEX `FK_countryenvparam_log_01` (`Country` ASC, `Environment` ASC) , DROP INDEX `ID1` ;

-- 97
ALTER TABLE `host` DROP FOREIGN KEY `FK_host_1` ;

-- 98
ALTER TABLE `host`   ADD CONSTRAINT `FK_host_01`  FOREIGN KEY (`Country` , `Environment` )  REFERENCES `countryenvparam` (`Country` , `Environment` )  ON DELETE CASCADE  ON UPDATE CASCADE, DROP INDEX `FK_host_1` , ADD INDEX `FK_host_01` (`Country` ASC, `Environment` ASC) ;

-- 99
ALTER TABLE `log` DROP INDEX `datecre` , ADD INDEX `IX_log_01` (`datecre` ASC) ;

-- 100
ALTER TABLE `test` DROP INDEX `ix_Test_Active` , ADD INDEX `IX_test_01` (`Test` ASC, `Active` ASC) ;

-- 101
ALTER TABLE `testcase` DROP INDEX `Index_2` , ADD INDEX `IX_testcase_01` (`Group` ASC) ;

-- 102
ALTER TABLE `testcase` DROP INDEX `Index_3` , ADD INDEX `IX_testcase_02` (`Test` ASC, `TestCase` ASC, `Application` ASC, `TcActive` ASC, `Group` ASC) ;

-- 103
ALTER TABLE `testcase` DROP INDEX `FK_testcase_2` , ADD INDEX `IX_testcase_03` (`Application` ASC) ;

-- 104
ALTER TABLE `testcase` DROP INDEX `FK_testcase_3` , ADD INDEX `IX_testcase_04` (`Project` ASC) ;

-- 105
ALTER TABLE `testcase` DROP FOREIGN KEY `FK_testcase_1` ;

-- 106
ALTER TABLE `testcase`   ADD CONSTRAINT `FK_testcase_01`  FOREIGN KEY (`Test` )  REFERENCES `test` (`Test` )  ON DELETE CASCADE  ON UPDATE CASCADE;

-- 107
UPDATE testcase SET Application=null where Application='';

-- 108
ALTER TABLE `testcase` DROP FOREIGN KEY `FK_testcase_2` ;

-- 109
ALTER TABLE `testcase`   ADD CONSTRAINT `FK_testcase_02`  FOREIGN KEY (`Application` )  REFERENCES `application` (`Application` )  ON DELETE CASCADE  ON UPDATE CASCADE;

-- 110
ALTER TABLE `testcase` DROP FOREIGN KEY `FK_testcase_3` ;

-- 111
ALTER TABLE `testcase`   ADD CONSTRAINT `FK_testcase_03`  FOREIGN KEY (`Project` )  REFERENCES `project` (`idproject` )  ON DELETE CASCADE  ON UPDATE CASCADE;

-- 112
DELETE FROM testcase USING testcase left outer join test ON testcase.test = test.test where test.test is null;

-- 113
ALTER TABLE `testcasecountry` DROP FOREIGN KEY `FK_testcasecountry_1` ;

-- 114
ALTER TABLE `testcasecountry`   ADD CONSTRAINT `FK_testcasecountry_01`  FOREIGN KEY (`Test` , `TestCase` )  REFERENCES `testcase` (`Test` , `TestCase` )  ON DELETE CASCADE  ON UPDATE CASCADE;

-- 115
ALTER TABLE `testcasecountryproperties` DROP FOREIGN KEY `FK_testcasecountryproperties_1` ;

-- 116
ALTER TABLE `testcasecountryproperties`   ADD CONSTRAINT `FK_testcasecountryproperties_01`  FOREIGN KEY (`Test` , `TestCase` , `Country` )  REFERENCES `testcasecountry` (`Test` , `TestCase` , `Country` )  ON DELETE CASCADE  ON UPDATE CASCADE;

-- 117
ALTER TABLE `testcaseexecution` DROP FOREIGN KEY `FK_testcaseexecution_1` ;

-- 118
ALTER TABLE `testcaseexecution`   ADD CONSTRAINT `FK_testcaseexecution_01`  FOREIGN KEY (`Test` , `TestCase` )  REFERENCES `testcase` (`Test` , `TestCase` )  ON DELETE CASCADE  ON UPDATE CASCADE;

-- 119
ALTER TABLE `testcaseexecution` DROP FOREIGN KEY `FK_testcaseexecution_3` ;

-- 120
ALTER TABLE `testcaseexecution`   ADD CONSTRAINT `FK_testcaseexecution_02`  FOREIGN KEY (`application`)  REFERENCES `application` (`application`)  ON DELETE CASCADE  ON UPDATE CASCADE;

-- 121
ALTER TABLE `testcaseexecution` DROP INDEX `FK_TestCaseExecution_1` , ADD INDEX `IX_testcaseexecution_01` (`Test` ASC, `TestCase` ASC) ;

-- 122
ALTER TABLE `testcaseexecution` DROP INDEX `fk_testcaseexecution_2` , ADD INDEX `IX_testcaseexecution_02` (`Tag` ASC) , DROP INDEX `index_1` , ADD INDEX `IX_testcaseexecution_03` (`Start` ASC) , DROP INDEX `IX_test_testcase_country` , ADD INDEX `IX_testcaseexecution_04` (`Test` ASC, `TestCase` ASC, `Country` ASC, `Start` ASC, `ControlStatus` ASC) , DROP INDEX `index_buildrev` , ADD INDEX `IX_testcaseexecution_05` (`Build` ASC, `Revision` ASC) , DROP INDEX `fk_test` , ADD INDEX `IX_testcaseexecution_06` (`Test` ASC) , DROP INDEX `ix_TestcaseExecution` , ADD INDEX `IX_testcaseexecution_07` (`Test` ASC, `TestCase` ASC, `Build` ASC, `Revision` ASC, `Environment` ASC, `Country` ASC, `ID` ASC) , DROP INDEX `FK_testcaseexecution_3` , ADD INDEX `IX_testcaseexecution_08` (`Application` ASC) ;

-- 123
ALTER TABLE `testcaseexecutiondata` DROP INDEX `propertystart` , ADD INDEX `IX_testcaseexecutiondata_01` (`Property` ASC, `Start` ASC) , DROP INDEX `index_1` , ADD INDEX `IX_testcaseexecutiondata_02` (`Start` ASC) ;

-- 124
ALTER TABLE `testcaseexecutiondata` DROP FOREIGN KEY `FK_TestCaseExecutionData_1` ;

-- 125
ALTER TABLE `testcaseexecutiondata`   ADD CONSTRAINT `FK_testcaseexecutiondata_01`  FOREIGN KEY (`ID` )  REFERENCES `testcaseexecution` (`ID` )  ON DELETE CASCADE  ON UPDATE CASCADE;

-- 126
ALTER TABLE `testcaseexecutionwwwdet` DROP FOREIGN KEY `FK_testcaseexecutionwwwdet_1` ;

-- 127
ALTER TABLE `testcaseexecutionwwwdet`   ADD CONSTRAINT `FK_testcaseexecutionwwwdet_01`  FOREIGN KEY (`ExecID` )  REFERENCES `testcaseexecution` (`ID` )  ON DELETE CASCADE  ON UPDATE CASCADE;

-- 128
ALTER TABLE `testcaseexecutionwwwdet` DROP INDEX `FK_testcaseexecutionwwwdet_1` , ADD INDEX `FK_testcaseexecutionwwwdet_01` (`ExecID` ASC) ;

-- 129
ALTER TABLE `testcaseexecutionwwwsum` DROP FOREIGN KEY `FK_testcaseexecutionwwwsum_1` ;

-- 130
ALTER TABLE `testcaseexecutionwwwsum`   ADD CONSTRAINT `FK_testcaseexecutionwwwsum_01`  FOREIGN KEY (`ID` )  REFERENCES `testcaseexecution` (`ID` )  ON DELETE CASCADE ON UPDATE CASCADE, DROP INDEX `FK_testcaseexecutionwwwsum_1` ;

-- 131
ALTER TABLE `testcasestep` DROP FOREIGN KEY `FK_testcasestep_1` ;

-- 132
ALTER TABLE `testcasestep`   ADD CONSTRAINT `FK_testcasestep_01`  FOREIGN KEY (`Test` , `TestCase` )  REFERENCES `testcase` (`Test` , `TestCase` )  ON DELETE CASCADE  ON UPDATE CASCADE;

-- 133
ALTER TABLE `testcasestepaction` DROP FOREIGN KEY `FK_testcasestepaction_1` ;

-- 134
ALTER TABLE `testcasestepaction`   ADD CONSTRAINT `FK_testcasestepaction_01`  FOREIGN KEY (`Test` , `TestCase` , `Step` )  REFERENCES `testcasestep` (`Test` , `TestCase` , `Step` )  ON DELETE CASCADE  ON UPDATE CASCADE;

-- 135
ALTER TABLE `testcasestepactioncontrol` DROP FOREIGN KEY `FK_testcasestepcontrol_1` ;

-- 136
ALTER TABLE `testcasestepactioncontrol`   ADD CONSTRAINT `FK_testcasestepactioncontrol_01`  FOREIGN KEY (`Test` , `TestCase` , `Step` , `Sequence` )  REFERENCES `testcasestepaction` (`Test` , `TestCase` , `Step` , `Sequence` )  ON DELETE CASCADE  ON UPDATE CASCADE, DROP INDEX `FK_testcasestepcontrol_1` ;

-- 137
ALTER TABLE `testcasestepactioncontrolexecution` DROP FOREIGN KEY `FK_testcasestepcontrolexecution_1` ;

-- 138
ALTER TABLE `testcasestepactioncontrolexecution`   ADD CONSTRAINT `FK_testcasestepactioncontrolexecution_01`  FOREIGN KEY (`ID` , `Step` )  REFERENCES `testcasestepexecution` (`ID` , `Step` )  ON DELETE CASCADE  ON UPDATE CASCADE;

-- 139
ALTER TABLE `testcasestepbatch` DROP FOREIGN KEY `FK_testcasestepbatchl_1` ;

-- 140
ALTER TABLE `testcasestepbatch`   ADD CONSTRAINT `FK_testcasestepbatch_01`  FOREIGN KEY (`Test` , `TestCase` )  REFERENCES `testcase` (`Test` , `TestCase` )  ON DELETE CASCADE  ON UPDATE CASCADE;

-- 141
ALTER TABLE `testcasestepbatch` DROP FOREIGN KEY `FK_testcasestepbatch_2` ;

-- 142
ALTER TABLE `testcasestepbatch`   ADD CONSTRAINT `FK_testcasestepbatch_02`  FOREIGN KEY (`Batch` )  REFERENCES `batchinvariant` (`Batch` )  ON DELETE CASCADE  ON UPDATE CASCADE;

-- 143
ALTER TABLE `testcasestepbatch` DROP INDEX `fk_testcasestepbatch_1` , ADD INDEX `FK_testcasestepbatch_02` (`Batch` ASC) ;

-- 144
ALTER TABLE `testcasestepbatch` DROP INDEX `FK_testcasestepbatch_02` , ADD INDEX `IX_testcasestepbatch_01` (`Batch` ASC) ;

-- 145
DELETE FROM testcasestepexecution, testcaseexecution USING testcasestepexecution left outer join testcaseexecution  ON testcasestepexecution.ID = testcaseexecution.ID where testcaseexecution.ID is null;

-- 146
ALTER TABLE `testcasestepexecution` DROP FOREIGN KEY `FK_testcasestepexecution_1` ;

-- 147
ALTER TABLE `testcasestepexecution`   ADD CONSTRAINT `FK_testcasestepexecution_01`  FOREIGN KEY (`ID` )  REFERENCES `testcaseexecution` (`ID` )  ON DELETE CASCADE  ON UPDATE CASCADE;

-- 148
ALTER TABLE `user` DROP INDEX `ID1` , ADD UNIQUE INDEX `IX_user_01` (`Login` ASC) ;

-- 149
INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`)
  VALUES ('TCESTATUS', 'CA', 6, 35, 'Test could not be done because of technical issues.');

-- 150
SELECT 1 FROM dual;

-- 151
ALTER TABLE `testcaseexecution` ADD COLUMN `ControlMessage` VARCHAR(500) NULL DEFAULT NULL  AFTER `ControlStatus` ;

-- 152
ALTER TABLE `testcasestepactioncontrolexecution` ADD COLUMN `ReturnMessage` VARCHAR(500) NULL DEFAULT NULL  AFTER `ReturnCode` ;

-- 153
ALTER TABLE `testcasestepactionexecution` ADD COLUMN `ReturnCode` VARCHAR(2) NULL DEFAULT NULL  AFTER `Sequence` , ADD COLUMN `ReturnMessage` VARCHAR(500) NULL DEFAULT NULL  AFTER `ReturnCode` ;

-- 154
ALTER TABLE `usergroup` ADD CONSTRAINT `FK_usergroup_01`  FOREIGN KEY (`Login` )  REFERENCES `user` (`Login` )  ON DELETE CASCADE ON UPDATE CASCADE;

-- 155
INSERT INTO `parameter` (`param`, `value`, `description`)
  VALUES ('cerberus_performancemonitor_nbminutes', '5', 'Integer that correspond to the number of minutes where the number of executions are collected on the servlet that manage the monitoring of the executions.');

-- 156
INSERT INTO `parameter` (`param`, `value`, `description`)
  VALUES ('cerberus_selenium_firefoxextension_firebug', 'D:\\CerberusDocuments\\firebug-fx.xpi', 'Link to the firefox extension FIREBUG file needed to track network traffic');

-- 157
INSERT INTO `parameter` (`param`, `value`, `description`)
  VALUES ('cerberus_selenium_firefoxextension_netexport', 'D:\\CerberusDocuments\\netExport.xpi', 'Link to the firefox extension NETEXPORT file needed to export network traffic');

-- 158
INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`)
  VALUES ('BROWSER', 'FIREFOX', 1, 37, 'Firefox Browser');

-- 159
DELETE FROM `parameter` WHERE `param`='cerberus_performancemonitor_nbminutes';

-- 160
UPDATE `invariant` SET `idname`='TCSTATUS' WHERE `id`='1' and`sort`='1';

-- 161
UPDATE `invariant` SET `idname`='TCSTATUS' WHERE `id`='1' and`sort`='2';

-- 162
UPDATE `invariant` SET `idname`='TCSTATUS' WHERE `id`='1' and`sort`='3';

-- 163
UPDATE `invariant` SET `idname`='TCSTATUS' WHERE `id`='1' and`sort`='4';

-- 164
UPDATE `invariant` SET `idname`='TCSTATUS' WHERE `id`='1' and`sort`='5';

-- 165
UPDATE `invariant` SET `idname`='TCSTATUS' WHERE `id`='1' and`sort`='6';

-- 166
SELECT 1 FROM dual;

-- 167
SELECT 1 FROM dual;

-- 168
INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`)
  VALUES ('EXECNBMIN', '5', 10, 38, '5 Minutes');

-- 169
INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`)
  VALUES ('EXECNBMIN', '10', 20, 38, '10 Minutes');

-- 170
INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`)
  VALUES ('EXECNBMIN', '15', 30, 38, '15 Minutes');

-- 171
INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`)
  VALUES ('EXECNBMIN', '20', 40, 38, '20 Minutes');

-- 172
INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`)
  VALUES ('EXECNBMIN', '30', 50, 38, '30 Minutes');

-- 173
INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`)
  VALUES ('EXECNBMIN', '45', 60, 38, '45 Minutes');

-- 174
INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`)
  VALUES ('EXECNBMIN', '60', 70, 38, '1 Hour');

-- 175
INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`)
  VALUES ('EXECNBMIN', '90', 80, 38, '1 Hour 30 Minutes');

-- 176
INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`)
  VALUES ('EXECNBMIN', '120', 90, 38, '2 Hours');

-- 177
INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`)
  VALUES ('EXECNBMIN', '180', 100, 38, '3 Hours');

-- 178
INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`)
  VALUES ('EXECNBMIN', '0', 1, 38, 'No Limit');

-- 179
SELECT 1 FROM dual;

-- 180
DELETE FROM `invariant` WHERE `id`='12' and`sort`='120';

-- 181
DELETE FROM `invariant` WHERE `id`='12' and`sort`='130';

-- 182
DELETE FROM `invariant` WHERE `id`='12' and`sort`='140';

-- 183
DELETE FROM `invariant` WHERE `id`='12' and`sort`='150';

-- 184
INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`)
  VALUES ('ACTION', 'mouseOver', 57, 12, 'mouseOver');

-- 185
INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`)
  VALUES ('ACTION', 'mouseOverAndWait', 58, 12, 'mouseOverAndWait');

-- 186
select 1 from DUAL;

-- 187
select 1 from DUAL;

-- 188
ALTER TABLE `user` ADD COLUMN `Team` VARCHAR(45) NULL  AFTER `Name` , ADD COLUMN `DefaultSystem` VARCHAR(45) NULL  AFTER `DefaultIP` , CHANGE COLUMN `Request` `Request` VARCHAR(5) NULL DEFAULT NULL  AFTER `Password` ;

-- 189
SELECT 1 FROM dual;

-- 190
select 1 from DUAL;

-- 191
select 1 from DUAL;

-- 192
INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`)
  VALUES ('SCREENSHOT', '0', 10, 39, 'No Screenshot')
  ,('SCREENSHOT', '1', 20, 39, 'Screenshot on error')
  ,('SCREENSHOT', '2', 30, 39, 'Screenshot on every action');

-- 193
ALTER TABLE `testcaseexecutiondata` ADD COLUMN `RMessage` VARCHAR(500) NULL DEFAULT ''  AFTER `RC` ;

-- 194
ALTER TABLE `testcasestepactioncontrolexecution` DROP FOREIGN KEY `FK_testcasestepactioncontrolexecution_01`;

-- 195
ALTER TABLE `testcasestepexecution` CHANGE COLUMN `Test` `Test` VARCHAR(45) NOT NULL DEFAULT '' , CHANGE COLUMN `TestCase` `TestCase` VARCHAR(45) NOT NULL DEFAULT '' ;

-- 196
ALTER TABLE `testcasestepexecution` CHANGE COLUMN `ID` `ID` BIGINT(20) UNSIGNED NOT NULL  , CHANGE COLUMN `Step` `Step` INT(10) UNSIGNED NOT NULL  ;

-- 197
ALTER TABLE `testcasestepexecution` DROP FOREIGN KEY `FK_testcasestepexecution_01`;

-- 198
ALTER TABLE `testcasestepexecution` ADD CONSTRAINT `FK_testcasestepexecution_01`   FOREIGN KEY (`ID` ) REFERENCES `testcaseexecution` (`ID` ) ON DELETE CASCADE ON UPDATE CASCADE ;

-- 199
ALTER TABLE `testcasestepexecution` DROP PRIMARY KEY , ADD PRIMARY KEY (`ID`, `Test`, `TestCase`, `Step`) ;

-- 200
ALTER TABLE `testcasestepactioncontrolexecution` CHANGE COLUMN `Test` `Test` VARCHAR(45) NOT NULL DEFAULT '' , CHANGE COLUMN `TestCase` `TestCase` VARCHAR(45) NOT NULL DEFAULT '' ;

-- 201
ALTER TABLE `testcasestepactioncontrolexecution`  DROP PRIMARY KEY , ADD PRIMARY KEY (`ID`, `Test`, `TestCase`, `Step`, `Sequence`, `Control`) ;

-- 202
ALTER TABLE `testcasestepactioncontrolexecution` ADD CONSTRAINT `FK_testcasestepactioncontrolexecution_01` FOREIGN KEY (`ID` , `Test` , `TestCase` , `Step` ) REFERENCES `testcasestepexecution` (`ID` , `Test` , `TestCase` , `Step` ) ON DELETE CASCADE ON UPDATE CASCADE ;

-- 203
ALTER TABLE `testcasestepactionexecution` CHANGE COLUMN `ID` `ID` BIGINT(20) UNSIGNED NOT NULL  , CHANGE COLUMN `Test` `Test` VARCHAR(45) NOT NULL DEFAULT '' , CHANGE COLUMN `TestCase` `TestCase` VARCHAR(45) NOT NULL DEFAULT '' , CHANGE COLUMN `Step` `Step` INT(10) UNSIGNED NOT NULL ;

-- 204
UPDATE `testcasestepactionexecution` SET Sequence=51 WHERE Step=0 and Sequence=50 and Action='Wait';

-- 205
ALTER TABLE `testcasestepactionexecution`  DROP PRIMARY KEY , ADD PRIMARY KEY (`ID`, `Test`, `TestCase`, `Step`, `Sequence`) ;

-- 206
DELETE FROM testcasestepactionexecution WHERE ID in ( SELECT ID FROM ( SELECT a.ID FROM testcasestepactionexecution a LEFT OUTER JOIN testcasestepexecution b ON a.ID=b.ID and a.Test=b.Test and a.TestCase=b.TestCase and a.Step=b.Step WHERE b.ID is null) as toto);

-- 207
ALTER TABLE `testcasestepactionexecution` ADD CONSTRAINT `FK_testcasestepactionexecution_01` FOREIGN KEY (`ID` , `Test` , `TestCase` , `Step` ) REFERENCES `testcasestepexecution` (`ID` , `Test` , `TestCase` , `Step` ) ON DELETE CASCADE ON UPDATE CASCADE;

-- 208
ALTER TABLE `testcasestepactioncontrolexecution` CHANGE COLUMN `ScreenshotFilename` `ScreenshotFilename` VARCHAR(150) NULL DEFAULT NULL  ;

-- 209
ALTER TABLE `testcasestepactionexecution` CHANGE COLUMN `ScreenshotFilename` `ScreenshotFilename` VARCHAR(150) NULL DEFAULT NULL  ;

-- 210
UPDATE testcasestepactioncontrol SET type='verifyUrl' where type='verifyurl';

-- 211
UPDATE testcasestepactioncontrol SET type='verifyTitle' where type='verifytitle';

-- 212
UPDATE invariant SET value='verifyUrl', description ='verifyUrl' where value='verifyurl' and idname='CONTROL';

-- 213
UPDATE invariant SET value='verifyTitle', description ='verifyTitle' where value='verifytitle' and idname='CONTROL';

-- 214
UPDATE invariant SET value = 'verifyPropertyEqual', description = 'verifyPropertyEqual' where value='PropertyIsEqualTo' and idname='CONTROL';

-- 215
UPDATE testcasestepactioncontrol SET type='verifyPropertyEqual' where type='PropertyIsEqualTo';

-- 216
UPDATE invariant SET value = 'verifyPropertyGreater', description = 'verifyPropertyGreater' where value='PropertyIsGreaterThan' and idname='CONTROL';

-- 217
UPDATE testcasestepactioncontrol SET type='verifyPropertyGreater' where type='PropertyIsGreaterThan';

-- 218
UPDATE invariant SET value = 'verifyPropertyMinor', description = 'verifyPropertyMinor' where value='PropertyIsMinorThan' and idname='CONTROL';

-- 219
UPDATE testcasestepactioncontrol SET type='verifyPropertyMinor' where type='PropertyIsMinorThan';

-- 220
INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`)
  VALUES ('CONTROL', 'verifyPropertyDifferent', 11, 13, 'verifyPropertyDifferent');

-- 221
INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`)
  VALUES ('CONTROL', 'verifyElementNotPresent', 21, 13, 'verifyElementNotPresent');

-- 222
INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`)
  VALUES ('ACTION', 'openUrlLogin', 61, 12, 'openUrlLogin');

-- 223
UPDATE testcasestepaction SET action='openUrlLogin' where action='URLLOGIN';

-- 224
UPDATE `invariant` SET `value`='firefox' WHERE `id`='37' and`sort`='1';

-- 225
INSERT INTO `parameter` (`param`, `value`, `description`)
  VALUES ('cerberus_url', 'http://localhost:8080/', 'URL to Cerberus used in order to call back cerberus from NetExport plugin. This parameter is mandatory for saving the firebug detail information back to cerberus. ex : http://host:port/contextroot');

-- 226
UPDATE invariant SET value = 'verifyStringEqual', description = 'verifyStringEqual' where value='verifyPropertyEqual' and idname='CONTROL';

-- 227
UPDATE testcasestepactioncontrol SET type='verifyStringEqual' where type='verifyPropertyEqual';

-- 228
UPDATE invariant SET value = 'verifyStringDifferent', description = 'verifyStringDifferent' where value='verifyPropertyDifferent' and idname='CONTROL';

-- 229
UPDATE testcasestepactioncontrol SET type='verifyStringDifferent' where type='verifyPropertyDifferent';

-- 230
UPDATE invariant SET value = 'verifyIntegerGreater', description = 'verifyIntegerGreater' where value='verifyPropertyGreater' and idname='CONTROL';

-- 231
UPDATE testcasestepactioncontrol SET type='verifyIntegerGreater' where type='verifyPropertyGreater';

-- 232
UPDATE invariant SET value = 'verifyIntegerMinor', description = 'verifyIntegerMinor' where value='verifyPropertyMinor' and idname='CONTROL';

-- 233
UPDATE testcasestepactioncontrol SET type='verifyIntegerMinor' where type='verifyPropertyMinor';

-- 234
UPDATE invariant SET value = 'executeSql', sort=20 where value='SQL' and idname='PROPERTYTYPE';

-- 235
UPDATE invariant SET value = 'executeSqlFromLib', sort=25 where value='LIB_SQL' and idname='PROPERTYTYPE';

-- 236
UPDATE invariant SET value = 'getFromHtmlVisible', sort=35, description='Getting from an HTML visible field in the current page.' where value='HTML' and idname='PROPERTYTYPE';

-- 237
UPDATE invariant SET value = 'text', sort=40 where value='TEXT' and idname='PROPERTYTYPE';

-- 238
INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`)
  VALUES ('PROPERTYTYPE', 'getFromHtml', 30, 19, 'Getting from an html field in the current page.');

-- 239
UPDATE testcasecountryproperties SET type='text' where type='TEXT';

-- 240
UPDATE testcasecountryproperties SET type='executeSqlFromLib' where type='LIB_SQL';

-- 241
UPDATE testcasecountryproperties SET type='executeSql' where type='SQL';

-- 242
UPDATE testcasecountryproperties SET type='getFromHtmlVisible' where type='HTML';

-- 243
INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`)
  VALUES ('PROPERTYNATURE', 'NOTINUSE', 4, 20, 'Not In Use');

-- 244
INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`)
  VALUES ('CONTROL', 'verifyTextNotPresent', 51, 13, 'verifyTextNotPresent');

-- 245
INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`) 
  VALUES ('TEAM', 'France', 10, 40, 'France Team'),  ('TEAM', 'Portugal', 20, 40, 'Portugal Team'),  ('SYSTEM', 'DEFAULT', 10, 41, 'System1 System'),  ('SYSTEM', 'SYS2', 20, 41, 'System2 System');

-- 246
UPDATE `user` SET Request='Y' where Request='true';

-- 247
UPDATE `user` SET Request='N' where Request='false';

-- 248
DROP TABLE `comparisonstatusdata`;

-- 249
DROP TABLE `comparisonstatus`;

-- 250
select 1 from DUAL;

-- 251
DROP TABLE `logeventchange`;

-- 252
ALTER TABLE `logevent` ADD COLUMN `Login` VARCHAR(30) NOT NULL DEFAULT '' AFTER `UserID`, CHANGE COLUMN `Time` `Time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, ADD COLUMN `remoteIP` VARCHAR(20) NULL DEFAULT NULL  AFTER `Log` , ADD COLUMN `localIP` VARCHAR(20) NULL DEFAULT NULL  AFTER `remoteIP`;

-- 253
INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`, `gp1`, `gp2`, `gp3`)
  VALUES ('USERGROUP', 'Visitor', 5, 42, 'Visitor', null, null, null)
  ,('USERGROUP', 'Integrator', 10, 42, 'Integrator', null, null, null)
  ,('USERGROUP', 'User', 15, 42, 'User', null, null, null)
  ,('USERGROUP', 'Admin', 20, 42, 'Admin', null, null, null);

-- 254
ALTER TABLE `application` ADD COLUMN `BugTrackerUrl` VARCHAR(300) NULL DEFAULT ''  AFTER `svnurl` , ADD COLUMN `BugTrackerNewUrl` VARCHAR(300) NULL DEFAULT ''  AFTER `BugTrackerUrl` ;

-- 255
SELECT 1 FROM dual;

-- 256
INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`, `gp1`, `gp2`, `gp3`)
  VALUES ('APPLITYPE', 'GUI', 5, 43, 'GUI application', null, null, null)
  ,('APPLITYPE', 'BAT', 10, 43, 'Batch Application', null, null, null)
  ,('APPLITYPE', 'SRV', 15, 43, 'Service Application', null, null, null)
  ,('APPLITYPE', 'NONE', 20, 43, 'Any Other Type of application', null, null, null);

-- 257
UPDATE application SET deploytype=null where deploytype is null;

-- 258
DELETE FROM `parameter` WHERE `param`='sitdmoss_bugtracking_url';

-- 259
DELETE FROM `parameter` WHERE `param`='sitdmoss_newbugtracking_url';

-- 260
DELETE FROM `parameter` WHERE `param`='cerberus_selenium_plugins_path';

-- 261
DELETE FROM `parameter` WHERE `param`='svn_application_url';

-- 262
UPDATE `invariant` SET `sort`=16 WHERE `id`='13' and`sort`='12';

-- 263
UPDATE `invariant` SET `sort`=17 WHERE `id`='13' and`sort`='14';

-- 264
INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`)
  VALUES   ('CONTROL', 'verifyStringGreater', 12, 13, 'verifyStringGreater') 
  ,('CONTROL', 'verifyStringMinor', 13, 13, 'verifyStringMinor');

-- 265
UPDATE `invariant` SET `value`='verifyTextInPage', `description`='verifyTextInPage' WHERE `id`='13' and`sort`='50';

-- 266
UPDATE `testcasestepactioncontrol` SET `type`='verifyTextInPage' WHERE `type`='verifyTextPresent';

-- 267
UPDATE `invariant` SET `value`='verifyTextNotInPage', `description`='verifyTextNotInPage' WHERE `id`='13' and`sort`='51';

-- 268
UPDATE `testcasestepactioncontrol` SET `type`='verifyTextNotInPage' WHERE `type`='verifyTextNotPresent';

-- 269
UPDATE `invariant` SET `value`='verifyTextInElement', `description`='verifyTextInElement' WHERE `id`='13' and`sort`='40';

-- 270
UPDATE `testcasestepactioncontrol` SET `type`='verifyTextInElement' WHERE `type`='VerifyText';

-- 271
UPDATE `invariant` SET `value`='verifyRegexInElement', `description`='verifyRegexInElement', sort='43' WHERE `id`='13' and`sort`='80';

-- 272
UPDATE `testcasestepactioncontrol` SET `type`='verifyRegexInElement' WHERE `type`='verifyContainText';

-- 273
ALTER TABLE `testcase` CHANGE COLUMN `BehaviorOrValueExpected` `BehaviorOrValueExpected` TEXT NULL , CHANGE COLUMN `HowTo` `HowTo` TEXT NULL ;

-- 274
ALTER TABLE testcasestepactionexecution CHANGE Property Property varchar(200);

-- 275
INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`, `gp1`)
  VALUES   ('LANGUAGE', '', 1, 44, 'Default language', 'en') 
  ,('LANGUAGE', 'BE', 5, 44, 'Belgium language', 'fr-be') 
  ,('LANGUAGE', 'CH', 10, 44, 'Switzerland language', 'fr-ch') 
  ,('LANGUAGE', 'ES', 15, 44, 'Spain language', 'es') 
  ,('LANGUAGE', 'FR', 20, 44, 'France language', 'fr') 
  ,('LANGUAGE', 'IT', 25, 44, 'Italy language', 'it') 
  ,('LANGUAGE', 'PT', 30, 44, 'Portugal language', 'pt') 
  ,('LANGUAGE', 'RU', 35, 44, 'Russia language', 'ru') 
  ,('LANGUAGE', 'UK', 40, 44, 'Great Britain language', 'gb') 
  ,('LANGUAGE', 'VI', 45, 44, 'Generic language', 'en');

-- 276
INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`)
  VALUES ('ACTION','focusToIframe',52,12,'focusToIframe')
  ,('ACTION','focusDefaultIframe',53,12,'focusDefaultIframe');

-- 277
select 1 from DUAL;

-- 278
ALTER TABLE `countryenvironmentdatabase`  CHANGE COLUMN `Country` `Country` VARCHAR(2) NOT NULL  FIRST ,  CHANGE COLUMN `Environment` `Environment` VARCHAR(45) NOT NULL  AFTER `Country` ,  DROP PRIMARY KEY , ADD PRIMARY KEY (`Country`, `Environment`, `Database`) ;

-- 279
ALTER TABLE `host`  CHANGE COLUMN `Environment` `Environment` VARCHAR(45) NOT NULL  AFTER `Country` ,  DROP PRIMARY KEY , ADD PRIMARY KEY USING BTREE (`Country`, `Environment`, `Session`, `Server`) ;

-- 280
ALTER TABLE `buildrevisionbatch`  CHANGE COLUMN `Environment` `Environment` VARCHAR(45) NULL DEFAULT NULL  AFTER `Country` ;

-- 281
UPDATE `invariant` SET gp2 = 'fr-be' WHERE idname = 'COUNTRY' and value = 'BE';

-- 282
UPDATE `invariant` SET gp2 = 'fr-ch' WHERE idname = 'COUNTRY' and value = 'CH';

-- 283
UPDATE `invariant` SET gp2 = 'es' WHERE idname = 'COUNTRY' and value = 'ES';

-- 284
UPDATE `invariant` SET gp2 = 'it' WHERE idname = 'COUNTRY' and value = 'IT';

-- 285
UPDATE `invariant` SET gp2 = 'pt-pt' WHERE idname = 'COUNTRY' and value = 'PT';

-- 286
UPDATE `invariant` SET gp2 = 'ru' WHERE idname = 'COUNTRY' and value = 'RU';

-- 287
UPDATE `invariant` SET gp2 = 'en-gb' WHERE idname = 'COUNTRY' and value = 'UK';

-- 288
UPDATE `invariant` SET gp2 = 'en-gb' WHERE idname = 'COUNTRY' and value = 'VI';

-- 289
UPDATE `invariant` SET gp2 = 'ru' WHERE idname = 'COUNTRY' and value = 'RU';

-- 290
UPDATE `invariant` SET gp2 = 'fr' WHERE idname = 'COUNTRY' and value = 'FR';

-- 291
UPDATE `invariant` SET gp2 = 'en-gb' WHERE idname = 'COUNTRY' and value = 'RX';

-- 292
DELETE FROM `invariant` WHERE idname = 'LANGUAGE';

-- 293
ALTER TABLE `countryenvironmentparameters` DROP COLUMN `as400LIB` , DROP COLUMN `JdbcPort` , DROP COLUMN `JdbcIP` , DROP COLUMN `JdbcPass` , DROP COLUMN `JdbcUser` ;

-- 294
ALTER TABLE `countryenvparam` ADD COLUMN `system` VARCHAR(45) NOT NULL DEFAULT ''  FIRST ;

-- 295
ALTER TABLE `countryenvdeploytype` ADD COLUMN `system` VARCHAR(45) NOT NULL DEFAULT '' FIRST ;

-- 296
ALTER TABLE `countryenvdeploytype` DROP FOREIGN KEY `FK_countryenvdeploytype_02` ;

-- 297
ALTER TABLE `countryenvironmentparameters` ADD COLUMN `system` VARCHAR(45) NOT NULL DEFAULT '' FIRST ;

-- 298
ALTER TABLE `countryenvironmentparameters` DROP FOREIGN KEY `FK_countryenvironmentparameters_01` ;

-- 299
ALTER TABLE `countryenvironmentdatabase` ADD COLUMN `system` VARCHAR(45) NOT NULL DEFAULT '' FIRST ;

-- 300
ALTER TABLE `countryenvironmentdatabase` DROP FOREIGN KEY `FK_countryenvironmentdatabase_01` ;

-- 301
ALTER TABLE `host` ADD COLUMN `system` VARCHAR(45) NOT NULL DEFAULT '' FIRST ;

-- 302
ALTER TABLE `host` DROP FOREIGN KEY `FK_host_01` ;

-- 303
ALTER TABLE `countryenvparam_log` ADD COLUMN `system` VARCHAR(45) NOT NULL DEFAULT ''  AFTER `id` ;

-- 304
ALTER TABLE `countryenvparam_log` DROP FOREIGN KEY `FK_countryenvparam_log_01` ;

-- 305
ALTER TABLE `countryenvparam_log` DROP INDEX `FK_countryenvparam_log_01` ;

-- 306
ALTER TABLE `buildrevisionbatch` ADD COLUMN `system` VARCHAR(45) NOT NULL DEFAULT ''  AFTER `Batch` ;

-- 307
ALTER TABLE `buildrevisionbatch` DROP FOREIGN KEY `FK_buildrevisionbatch_02` ;

-- 308
ALTER TABLE `buildrevisionbatch` DROP INDEX `FK_buildrevisionbatch_02` ;

-- 309
ALTER TABLE `countryenvparam`  DROP PRIMARY KEY , ADD PRIMARY KEY (`system`, `Country`, `Environment`) ;

-- 310
ALTER TABLE `countryenvdeploytype` DROP PRIMARY KEY , ADD PRIMARY KEY (`system`, `Country`, `Environment`, `deploytype`, `JenkinsAgent`) ;

-- 311
ALTER TABLE `countryenvdeploytype`  ADD CONSTRAINT `FK_countryenvdeploytype_1` FOREIGN KEY (`system` , `Country` , `Environment` )  REFERENCES `countryenvparam` (`system` , `Country` , `Environment` ) ON DELETE CASCADE ON UPDATE CASCADE;

-- 312
ALTER TABLE `countryenvdeploytype`  DROP INDEX `FK_countryenvdeploytype_01` , ADD INDEX `FK_countryenvdeploytype_01` (`system` ASC, `Country` ASC, `Environment` ASC) ;

-- 313
ALTER TABLE `countryenvdeploytype` DROP FOREIGN KEY `FK_countryenvdeploytype_01` ;

-- 314
ALTER TABLE `countryenvdeploytype`   ADD CONSTRAINT `FK_countryenvdeploytype_02`  FOREIGN KEY (`deploytype` )  REFERENCES `deploytype` (`deploytype` )  ON DELETE CASCADE  ON UPDATE CASCADE;

-- 315
ALTER TABLE `countryenvdeploytype` DROP FOREIGN KEY `FK_countryenvdeploytype_1` ;

-- 316
ALTER TABLE `countryenvdeploytype`   ADD CONSTRAINT `FK_countryenvdeploytype_01`  FOREIGN KEY (`system` , `Country` , `Environment` )  REFERENCES `countryenvparam` (`system` , `Country` , `Environment` )  ON DELETE CASCADE  ON UPDATE CASCADE;

-- 317
ALTER TABLE `countryenvironmentparameters` DROP PRIMARY KEY , ADD PRIMARY KEY (`system`, `Country`, `Environment`, `Application`) ;

-- 318
ALTER TABLE `countryenvironmentparameters`   ADD CONSTRAINT `FK_countryenvironmentparameters_01` FOREIGN KEY (`system` , `Country` , `Environment` )  REFERENCES `countryenvparam` (`system` , `Country` , `Environment` ) ON DELETE CASCADE ON UPDATE CASCADE;

-- 319
ALTER TABLE `countryenvironmentparameters` DROP INDEX `FK_countryenvironmentparameters_01` , ADD INDEX `FK_countryenvironmentparameters_01` (`system` ASC, `Country` ASC, `Environment` ASC) ;

-- 320
ALTER TABLE `buildrevisionbatch`   ADD CONSTRAINT `FK_buildrevisionbatch_02`  FOREIGN KEY (`system` , `Country` , `Environment` )  REFERENCES `countryenvparam` (`system` , `Country` , `Environment` )  ON DELETE CASCADE   ON UPDATE CASCADE, ADD INDEX `FK_buildrevisionbatch_02` (`system` ASC, `Country` ASC, `Environment` ASC) ;

-- 321
ALTER TABLE `countryenvironmentdatabase` DROP PRIMARY KEY , ADD PRIMARY KEY (`system`, `Country`, `Environment`, `Database`) ;

-- 322
ALTER TABLE `countryenvironmentdatabase`   ADD CONSTRAINT `FK_countryenvironmentdatabase_01`  FOREIGN KEY (`system` , `Country` , `Environment` )  REFERENCES `countryenvparam` (`system` , `Country` , `Environment` )  ON DELETE CASCADE ON UPDATE CASCADE, DROP INDEX  `FK_countryenvironmentdatabase_01` , ADD INDEX `FK_countryenvironmentdatabase_01` (`system` ASC, `Country` ASC, `Environment` ASC) ;

-- 323
ALTER TABLE `host` DROP PRIMARY KEY , ADD PRIMARY KEY (`system`, `Country`, `Environment`, `Session`, `Server`) ;

-- 324
ALTER TABLE `host` DROP INDEX `FK_host_01` , ADD INDEX `FK_host_01` (`system` ASC, `Country` ASC, `Environment` ASC) ;

-- 325
ALTER TABLE `host`   ADD CONSTRAINT `FK_host_01`  FOREIGN KEY (`system` , `Country` , `Environment` )  REFERENCES `countryenvparam` (`system` , `Country` , `Environment` )  ON DELETE CASCADE ON UPDATE CASCADE ;

-- 326
ALTER TABLE `countryenvparam_log`   ADD CONSTRAINT `FK_countryenvparam_log_01`  FOREIGN KEY (`system` , `Country` , `Environment` )  REFERENCES `countryenvparam` (`system` , `Country` , `Environment` )  ON DELETE CASCADE ON UPDATE CASCADE, ADD INDEX `FK_countryenvparam_log_01` (`system` ASC, `Country` ASC, `Environment` ASC) ;

-- 327
ALTER TABLE `testcaseexecutiondata` CHANGE COLUMN `Value` `Value` VARCHAR(3000) NOT NULL  , CHANGE COLUMN `RMessage` `RMessage` VARCHAR(3000) NULL DEFAULT ''  ;

-- 328
INSERT INTO `countryenvparam` (`system`, `Country`, `Environment`, `Build`, `Revision`, `Chain`, `DistribList`, `EMailBodyRevision`, `Type`, `EMailBodyChain`, `EMailBodyDisableEnvironment`, `active`, `maintenanceact`)
  VALUES ('DEFAULT', 'FR', 'PROD', '', '', '', '', '', 'STD', '', '', 'Y', 'N');

-- 329
INSERT INTO `countryenvironmentparameters` (`system`, `Country`, `Environment`, `Application`, `IP`, `URL`, `URLLOGIN`)
  VALUES ('DEFAULT', 'FR', 'PROD', 'Google', 'www.google.fr', '/', '');

-- 330
UPDATE `user` SET DefaultSystem='DEFAULT' where DefaultSystem is null;

-- 331
CREATE  TABLE `testcaseexecutionsysver` (  `ID` BIGINT UNSIGNED NOT NULL ,  `system` VARCHAR(45) NOT NULL ,  `Build` VARCHAR(10) NULL ,  `Revision` VARCHAR(20) NULL ,  PRIMARY KEY (`ID`, `system`) ,  INDEX `FK_testcaseexecutionsysver_01` (`ID` ASC) ,  CONSTRAINT `FK_testcaseexecutionsysver_01`    FOREIGN KEY (`ID` )    REFERENCES `testcaseexecution` (`ID` )    ON DELETE CASCADE ON UPDATE CASCADE);

-- 332
CREATE  TABLE `countryenvlink` (  `system` VARCHAR(45) NOT NULL DEFAULT '' ,  `Country` VARCHAR(2) NOT NULL ,  `Environment` VARCHAR(45) NOT NULL ,  `systemLink` VARCHAR(45) NOT NULL DEFAULT '' ,  `CountryLink` VARCHAR(2) NOT NULL ,  `EnvironmentLink` VARCHAR(45) NOT NULL ,  PRIMARY KEY (`system`, `Country`, `Environment`,`systemLink`, `CountryLink`, `EnvironmentLink`) ,  INDEX `FK_countryenvlink_01` (`system` ASC, `Country` ASC, `Environment` ASC) ,  INDEX `FK_countryenvlink_02` (`systemLink` ASC, `CountryLink` ASC, `EnvironmentLink` ASC) ,  CONSTRAINT `FK_countryenvlink_01`    FOREIGN KEY (`system` , `Country` , `Environment` )    REFERENCES `countryenvparam` (`system` , `Country` , `Environment` )    ON DELETE CASCADE ON UPDATE CASCADE,  CONSTRAINT `FK_countryenvlink_02`    FOREIGN KEY (`systemLink` , `CountryLink` , `EnvironmentLink` )    REFERENCES `countryenvparam` (`system` , `Country` , `Environment` )    ON DELETE CASCADE ON UPDATE CASCADE) 
  ENGINE=InnoDB DEFAULT CHARSET=utf8 ;

-- 333
ALTER TABLE `countryenvlink` DROP PRIMARY KEY , ADD PRIMARY KEY (`system`, `Country`, `Environment`, `systemLink`) ;

-- 334
DELETE FROM `invariant` WHERE `id`='23' and`sort`='6';

-- 335
SELECT 1 from DUAL;

-- 336
UPDATE testcasestepactioncontrol SET ControlValue=REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(ControlValue,'%ENV%','%SYS_ENV%'),'%ENVGP%','%SYS_ENVGP%'),'%COUNTRY%','%SYS_COUNTRY%'),'%COUNTRYGP1%','%SYS_COUNTRYGP1%'),'%SSIP%','%SYS_SSIP%'),'%SSPORT%','%SYS_SSPORT%'),'%TAG%','%SYS_TAG%'),'%TODAY-yyyy%','%SYS_TODAY-yyyy%'),'%TODAY-MM%','%SYS_TODAY-MM%'),'%TODAY-dd%','%SYS_TODAY-dd%'),'%TODAY-HH%','%SYS_TODAY-HH%'),'%TODAY-mm%','%SYS_TODAY-mm%'),'%TODAY-ss%','%SYS_TODAY-ss%'),'%YESTERDAY-yyyy%','%SYS_YESTERDAY-yyyy%'),'%YESTERDAY-MM%','%SYS_YESTERDAY-MM%'),'%YESTERDAY-dd%','%SYS_YESTERDAY-dd%'),'%YESTERDAY-HH%','%SYS_YESTERDAY-HH%'),'%YESTERDAY-mm%','%SYS_YESTERDAY-mm%'),'%YESTERDAY-ss%','%SYS_YESTERDAY-ss%'), ControlProperty=REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(ControlProperty,'%ENV%','%SYS_ENV%'),'%ENVGP%','%SYS_ENVGP%'),'%COUNTRY%','%SYS_COUNTRY%'),'%COUNTRYGP1%','%SYS_COUNTRYGP1%'),'%SSIP%','%SYS_SSIP%'),'%SSPORT%','%SYS_SSPORT%'),'%TAG%','%SYS_TAG%'),'%TODAY-yyyy%','%SYS_TODAY-yyyy%'),'%TODAY-MM%','%SYS_TODAY-MM%'),'%TODAY-dd%','%SYS_TODAY-dd%'),'%TODAY-HH%','%SYS_TODAY-HH%'),'%TODAY-mm%','%SYS_TODAY-mm%'),'%TODAY-ss%','%SYS_TODAY-ss%'),'%YESTERDAY-yyyy%','%SYS_YESTERDAY-yyyy%'),'%YESTERDAY-MM%','%SYS_YESTERDAY-MM%'),'%YESTERDAY-dd%','%SYS_YESTERDAY-dd%'),'%YESTERDAY-HH%','%SYS_YESTERDAY-HH%'),'%YESTERDAY-mm%','%SYS_YESTERDAY-mm%'),'%YESTERDAY-ss%','%SYS_YESTERDAY-ss%');

-- 337
UPDATE testcasestepaction SET Object=REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(Object,'%ENV%','%SYS_ENV%'),'%ENVGP%','%SYS_ENVGP%'),'%COUNTRY%','%SYS_COUNTRY%'),'%COUNTRYGP1%','%SYS_COUNTRYGP1%'),'%SSIP%','%SYS_SSIP%'),'%SSPORT%','%SYS_SSPORT%'),'%TAG%','%SYS_TAG%'),'%TODAY-yyyy%','%SYS_TODAY-yyyy%'),'%TODAY-MM%','%SYS_TODAY-MM%'),'%TODAY-dd%','%SYS_TODAY-dd%'),'%TODAY-HH%','%SYS_TODAY-HH%'),'%TODAY-mm%','%SYS_TODAY-mm%'),'%TODAY-ss%','%SYS_TODAY-ss%'),'%YESTERDAY-yyyy%','%SYS_YESTERDAY-yyyy%'),'%YESTERDAY-MM%','%SYS_YESTERDAY-MM%'),'%YESTERDAY-dd%','%SYS_YESTERDAY-dd%'),'%YESTERDAY-HH%','%SYS_YESTERDAY-HH%'),'%YESTERDAY-mm%','%SYS_YESTERDAY-mm%'),'%YESTERDAY-ss%','%SYS_YESTERDAY-ss%'), property=REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(property,'%ENV%','%SYS_ENV%'),'%ENVGP%','%SYS_ENVGP%'),'%COUNTRY%','%SYS_COUNTRY%'),'%COUNTRYGP1%','%SYS_COUNTRYGP1%'),'%SSIP%','%SYS_SSIP%'),'%SSPORT%','%SYS_SSPORT%'),'%TAG%','%SYS_TAG%'),'%TODAY-yyyy%','%SYS_TODAY-yyyy%'),'%TODAY-MM%','%SYS_TODAY-MM%'),'%TODAY-dd%','%SYS_TODAY-dd%'),'%TODAY-HH%','%SYS_TODAY-HH%'),'%TODAY-mm%','%SYS_TODAY-mm%'),'%TODAY-ss%','%SYS_TODAY-ss%'),'%YESTERDAY-yyyy%','%SYS_YESTERDAY-yyyy%'),'%YESTERDAY-MM%','%SYS_YESTERDAY-MM%'),'%YESTERDAY-dd%','%SYS_YESTERDAY-dd%'),'%YESTERDAY-HH%','%SYS_YESTERDAY-HH%'),'%YESTERDAY-mm%','%SYS_YESTERDAY-mm%'),'%YESTERDAY-ss%','%SYS_YESTERDAY-ss%');

-- 338
UPDATE testcasecountryproperties SET Value=REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(Value,'%ENV%','%SYS_ENV%'),'%ENVGP%','%SYS_ENVGP%'),'%COUNTRY%','%SYS_COUNTRY%'),'%COUNTRYGP1%','%SYS_COUNTRYGP1%'),'%SSIP%','%SYS_SSIP%'),'%SSPORT%','%SYS_SSPORT%'),'%TAG%','%SYS_TAG%'),'%TODAY-yyyy%','%SYS_TODAY-yyyy%'),'%TODAY-MM%','%SYS_TODAY-MM%'),'%TODAY-dd%','%SYS_TODAY-dd%'),'%TODAY-HH%','%SYS_TODAY-HH%'),'%TODAY-mm%','%SYS_TODAY-mm%'),'%TODAY-ss%','%SYS_TODAY-ss%'),'%YESTERDAY-yyyy%','%SYS_YESTERDAY-yyyy%'),'%YESTERDAY-MM%','%SYS_YESTERDAY-MM%'),'%YESTERDAY-dd%','%SYS_YESTERDAY-dd%'),'%YESTERDAY-HH%','%SYS_YESTERDAY-HH%'),'%YESTERDAY-mm%','%SYS_YESTERDAY-mm%'),'%YESTERDAY-ss%','%SYS_YESTERDAY-ss%');

-- 339
INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`)
  VALUES ('ACTION', 'takeScreenshot', 105, 12, 'takeScreenshot');

-- 340
INSERT INTO `parameter` (`param`, `value`, `description`)
  VALUES ('selenium_download_url', 'http://selenium.googlecode.com/files/selenium-server-standalone-2.35.0.jar', 'URL to download the selenium package from the web.');

-- 341
SELECT 1 FROM dual;

-- 342
SELECT 1 FROM dual;

-- 343
ALTER TABLE `testcaseexecution` CHANGE COLUMN `Revision` `Revision` VARCHAR(20) NULL DEFAULT NULL ;

-- 344
UPDATE testcase SET HowTo=REPLACE(HowTo, '\n', '<br/>');

-- 345
CREATE  TABLE `buildrevisioninvariant` (  `system` VARCHAR(45) NOT NULL DEFAULT '' ,  `level` INT NOT NULL ,  `seq` INT NOT NULL ,  `versionname` VARCHAR(20) NULL ,  PRIMARY KEY (`system`, `level`, `seq`),  UNIQUE INDEX `IX_buildrevisioninvariant_01` (`system`,`level`,`versionname`) );

-- 346
DELETE FROM `invariant` where id in (8,9);

-- 347
INSERT INTO `parameter` (`param`, `value`, `description`)
  VALUES ('selenium_defaultWait', '90', 'Integer that correspond to the number of seconds that selenium will wait before give timeout, when searching for a element.');

-- 348
SELECT 1 FROM dual;

-- 349
SELECT 1 FROM dual;

-- 350
UPDATE `invariant` SET `value`='AUTOMATED', `sort`='20' WHERE `id`='2' and`sort`='2';

-- 351
UPDATE `testcase` SET `Group`='AUTOMATED' WHERE `Group`='INTERACTIVE';

-- 352
UPDATE `invariant` SET `sort`='10' WHERE `id`='2' and`sort`='6';

-- 353
UPDATE `invariant` SET `sort`='30' WHERE `id`='2' and`sort`='5';

-- 354
UPDATE `invariant` SET `sort`='40' WHERE `id`='2' and`sort`='3';

-- 355
UPDATE `invariant` SET `sort`='50' WHERE `id`='2' and`sort`='4';

-- 356
UPDATE `invariant` SET `sort`='70' WHERE `id`='2' and`sort`='1';

-- 357
SELECT 1 FROM dual;

-- 358
ALTER TABLE `parameter` ADD COLUMN `system` VARCHAR(45) NOT NULL  FIRST , DROP PRIMARY KEY , ADD PRIMARY KEY (`system`, `param`) ;

-- 359
ALTER TABLE `buildrevisionparameters`  ADD INDEX `IX_buildrevisionparameters_02` (`Build` ASC, `Revision` ASC, `Application` ASC)  ,DROP INDEX `FK_buildrevisionparameters_01`, ADD INDEX `FK_buildrevisionparameters_01_IX` (`Application` ASC) ;

-- 360
ALTER TABLE `testcaseexecutionsysver`  DROP INDEX `FK_testcaseexecutionsysver_01` , ADD INDEX `FK_testcaseexecutionsysver_01_IX` (`ID` ASC)  , ADD INDEX `IX_testcaseexecutionsysver_02` (`system` ASC, `Build` ASC, `Revision` ASC) ;

-- 361
ALTER TABLE `application`  DROP INDEX `FK_application_01` , ADD INDEX `FK_application_01_IX` (`deploytype` ASC) ;

-- 362
ALTER TABLE `buildrevisionbatch`  DROP INDEX `FK_buildrevisionbatch_01` , ADD INDEX `FK_buildrevisionbatch_01_IX` (`Batch` ASC)  , DROP INDEX `FK_buildrevisionbatch_02` , ADD INDEX `FK_buildrevisionbatch_02_IX` (`system` ASC, `Country` ASC, `Environment` ASC) ;

-- 363
ALTER TABLE `countryenvdeploytype`  DROP INDEX `FK_countryenvdeploytype_02` , ADD INDEX `FK_countryenvdeploytype_02_IX` (`deploytype` ASC)  , DROP INDEX `FK_countryenvdeploytype_01` , ADD INDEX `FK_countryenvdeploytype_01_IX` (`system` ASC, `Country` ASC, `Environment` ASC) ;

-- 364
ALTER TABLE `countryenvironmentdatabase`  DROP INDEX `FK_countryenvironmentdatabase_01` , ADD INDEX `FK_countryenvironmentdatabase_01_IX` (`system` ASC, `Country` ASC, `Environment` ASC) ;

-- 365
ALTER TABLE `countryenvironmentparameters`  DROP INDEX `FK_countryenvironmentparameters_02` , ADD INDEX `FK_countryenvironmentparameters_02_IX` (`Application` ASC)  , DROP INDEX `FK_countryenvironmentparameters_01` , ADD INDEX `FK_countryenvironmentparameters_01_IX` (`system` ASC, `Country` ASC, `Environment` ASC) ;

-- 366
ALTER TABLE `countryenvparam_log`  DROP INDEX `FK_countryenvparam_log_01` , ADD INDEX `FK_countryenvparam_log_01_IX` (`system` ASC, `Country` ASC, `Environment` ASC) ;

-- 367
ALTER TABLE `host`  DROP INDEX `FK_host_01` , ADD INDEX `FK_host_01_IX` (`system` ASC, `Country` ASC, `Environment` ASC) ;

-- 368
ALTER TABLE `testcaseexecutionwwwdet`  DROP INDEX `FK_testcaseexecutionwwwdet_01` , ADD INDEX `FK_testcaseexecutionwwwdet_01_IX` (`ExecID` ASC) ;

-- 369
ALTER TABLE `testcasestepactioncontrol`  DROP INDEX `FK_testcasestepactioncontrol_01` , ADD INDEX `FK_testcasestepactioncontrol_01_IX` (`Test` ASC, `TestCase` ASC, `Step` ASC, `Sequence` ASC) ;

-- 370
ALTER TABLE `countryenvlink`  DROP INDEX `FK_countryenvlink_01` , ADD INDEX `FK_countryenvlink_01_IX` (`system` ASC, `Country` ASC, `Environment` ASC)  , DROP INDEX `FK_countryenvlink_02` , ADD INDEX `FK_countryenvlink_02_IX` (`systemLink` ASC, `CountryLink` ASC, `EnvironmentLink` ASC) ;

-- 371
INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`)
  VALUES ('BROWSER', 'iexplorer', 2, 37, 'Internet Explorer Browser');

-- 372
INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`)
  VALUES ('BROWSER', 'chrome', 3, 37, 'Chrome Browser');

-- 373
UPDATE `invariant` SET `sort`='54' WHERE `id`='12' and`sort`='55';

-- 374
INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`)
  VALUES ('ACTION', 'mouseDown', 55, 12, 'Selenium Action mouseDown');

-- 375
INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`)
  VALUES ('ACTION', 'mouseUp', 56, 12, 'Selenium Action mouseDown');

-- 376
ALTER TABLE `invariant` CHANGE COLUMN `description` `description` VARCHAR(250) NOT NULL ;

-- 377
INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`)
  VALUES  ('USERGROUP', 'TestRO', '100', '42', 'Has read only access to the information related to test cases and also has access to execution reporting options.') 
  ,('USERGROUP', 'Test', '110', '42', 'Can modify non WORKING test cases but cannot delete test cases.') 
  ,('USERGROUP', 'TestAdmin', '120', '42', 'Can modify or delete any test case (including Pre Testing test cases). Can also create or delete a test.') 
  ,('USERGROUP', 'RunTest', '200', '42', 'Can run both Manual and Automated test cases from GUI.') 
  ,('USERGROUP', 'IntegratorRO', '300', '42', 'Has access to the integration status.') 
  ,('USERGROUP', 'IntegratorNewChain', '350', '42', 'Can register the end of the chain execution. Has read only access to the other informations on the same page.') 
  ,('USERGROUP', 'IntegratorDeploy', '360', '42', 'Can disable or enable environments and register new build / revision.') 
  ,('USERGROUP', 'Integrator', '310', '42', 'Can add an application. Can change parameters of the environments.') 
  ,('USERGROUP', 'Administrator', '400', '42', 'Can create, modify or delete users. Has access to log Event and Database Maintenance. Can change Parameter values.');

-- 378
ALTER TABLE `usergroup` CHANGE COLUMN `GroupName` `GroupName` VARCHAR(45) NOT NULL  ;

-- 379
INSERT INTO usergroup SELECT distinct Login, 'TestRO' FROM usergroup where GroupName in ('User','Visitor','Admin');

-- 380
INSERT INTO usergroup SELECT distinct Login, 'Test' FROM usergroup where GroupName in ('User','Admin');

-- 381
INSERT INTO usergroup SELECT distinct Login, 'TestAdmin' FROM usergroup where GroupName in ('Admin');

-- 382
INSERT INTO usergroup SELECT distinct Login, 'RunTest' FROM usergroup where GroupName in ('User','Admin');

-- 383
INSERT INTO usergroup SELECT distinct Login, 'IntegratorRO' FROM usergroup where GroupName in ('Visitor','Integrator');

-- 384
INSERT INTO usergroup SELECT distinct Login, 'IntegratorNewChain' FROM usergroup where GroupName in ('Integrator');

-- 385
INSERT INTO usergroup SELECT distinct Login, 'IntegratorDeploy' FROM usergroup where GroupName in ('Integrator');

-- 386
INSERT INTO usergroup SELECT distinct Login, 'Administrator' FROM usergroup where GroupName in ('Admin');

-- 387
DELETE FROM `invariant` WHERE `id`='42' and `sort` in ('5','10','15','20');

-- 388
DELETE FROM `usergroup` where GroupName in ('Admin','User','Visitor');

-- 389
SELECT 1 FROM dual;

-- 390
SELECT 1 FROM dual;

-- 391
SELECT 1 FROM dual;

-- 392
ALTER TABLE `testcasestepactioncontrol` ADD COLUMN `ControlDescription` VARCHAR(255) NOT NULL DEFAULT ''  AFTER `ControlProperty` ;

-- 393
ALTER TABLE `testcasestepaction` ADD COLUMN `Description` VARCHAR(255) NOT NULL DEFAULT ''  AFTER `Property` ;

-- 394
CREATE TABLE `testdata` (  `key` varchar(200) NOT NULL ,  `value` varchar(5000) NOT NULL DEFAULT '',  PRIMARY KEY (`key`))
  ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 395
INSERT INTO `parameter` (`system`, `param`, `value`, `description`)
  VALUES ('', 'cerberus_accountcreation_defaultpassword', 'Cerberus2014', 'Default Password when creating an account.')
  ,('', 'cerberus_notification_accountcreation_cc', 'Cerberus <no.reply@cerberus-testing.org>', 'Copy List used for Cerberus account creation notification email.')
  ,('', 'cerberus_notification_accountcreation_subject', '[Cerberus] Welcome, your account has been created', 'Subject of Cerberus account creation notification email.')
  ,('', 'cerberus_notification_accountcreation_body', 'Hello %NAME%<br><br>Your Cerberus account has been created<br><br>To connect Cerberus, please click <a href="http://cerberus_server/Cerberus">here</a> and use this credential : <br><br>login : %LOGIN%<br>password : %DEFAULT_PASSWORD%<br><br>At your first connection, you will be invited to modify your password<br><br>Enjoy the tool<br><br>','Cerberus account creation notification email body. %LOGIN%, %NAME% and %DEFAULT_PASSWORD% can be used as variables.')
  ,('', 'cerberus_notification_accountcreation_from','Cerberus <no.reply@cerberus-testing.org>', 'From field of Cerberus account creation notification email.')
  ,('', 'cerberus_notification_accountcreation_activatenotification','N', 'Activation boolean for sending automatic email on account creation. Y value will activate the notifications. Any other value will not.');

-- 396
ALTER TABLE `user` ADD COLUMN `Email` VARCHAR(100) NULL AFTER `DefaultSystem`;

-- 397
ALTER TABLE `application` DROP COLUMN `internal` ;

-- 398
UPDATE `invariant` SET `idname`='ACTION' WHERE `id`='12' and`sort`='45';

-- 399
UPDATE `invariant` set  `value`='Unknown', `description`='Unknown' where `idname`='ACTION' and `value`='addSelection';

-- 400
INSERT INTO `invariant`
  VALUES ('ACTION','switchToWindow',180,12,'switchToWindow',NULL,NULL,NULL);

-- 401
SELECT 1 FROM dual;

-- 402
SELECT 1 FROM dual;

-- 403
INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`)
  VALUES ('PROPERTYTYPE', 'getFromTestData', '10', '19', 'Getting from the test Data library using the Key');

-- 404
UPDATE `invariant` SET `sort`='60' WHERE `id`='1' and`sort`='6';

-- 405
UPDATE `invariant` SET `sort`='50' WHERE `id`='1' and`sort`='5';

-- 406
UPDATE `invariant` SET `sort`='40' WHERE `id`='1' and`sort`='4';

-- 407
UPDATE `invariant` SET `sort`='20' WHERE `id`='1' and`sort`='3';

-- 408
UPDATE `invariant` SET `sort`='30' WHERE `id`='1' and`sort`='2';

-- 409
UPDATE `invariant` SET `sort`='10' WHERE `id`='1' and`sort`='1';

-- 410
SELECT 1 FROM dual;

-- 411
SELECT 1 FROM dual;

-- 412
ALTER TABLE `testcaseexecution` ADD COLUMN `BrowserFullVersion` VARCHAR(100) NULL DEFAULT ''  AFTER `Browser` ;

-- 413
SELECT 1 FROM dual;

-- 414
SELECT 1 FROM dual;

-- 415
INSERT INTO `parameter` (`system`, `param`, `value`, `description`)
  VALUES ('', 'selenium_chromedriver_download_url', 'http://chromedriver.storage.googleapis.com/index.html', 'Download URL for Selenium Chrome webdrivers.') 
  ,('', 'selenium_iedriver_download_url', 'http://code.google.com/p/selenium/downloads/list','Download URL for Internet Explorer webdrivers.');

-- 416
INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`, `gp1`, `gp2`, `gp3`)
  VALUES ('CONTROL', 'verifyElementNotVisible', 31, 13, 'verifyElementNotVisible', NULL, NULL, NULL);

-- 417
ALTER TABLE `documentation` CHANGE COLUMN `DocLabel` `DocLabel` VARCHAR(100) NULL DEFAULT NULL  , CHANGE COLUMN `DocDesc` `DocDesc` TEXT NULL DEFAULT NULL  ;

-- 418
SELECT 1 FROM dual;

-- 419
UPDATE `invariant` SET `sort`='31' WHERE `id`='1' and`sort`='20';

-- 420
UPDATE `invariant` SET `sort`='21' WHERE `id`='1' and`sort`='30';

-- 421
SELECT 1 FROM dual;

-- 422
ALTER TABLE `application` CHANGE COLUMN `BugTrackerUrl` `BugTrackerUrl` VARCHAR(5000) NULL DEFAULT ''  , CHANGE COLUMN `BugTrackerNewUrl` `BugTrackerNewUrl` VARCHAR(5000) NULL DEFAULT '' ;

-- 423
SELECT 1 FROM dual;

-- 424
ALTER TABLE `invariant` ADD COLUMN `VeryShortDesc` VARCHAR(45) NULL DEFAULT '' AFTER `description`;

-- 425
UPDATE `invariant` SET `VeryShortDesc`=description WHERE `id`='1' ;

-- 426
UPDATE `invariant` SET `gp1`='Y' WHERE `id`='1' ;

-- 427
INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`, `gp1`, `gp2`, `gp3`)
  VALUES ('ACTION','manageDialog',200,12,'manageDialog',NULL,NULL,NULL),  ('CONTROL', 'verifyTextInDialog', 80, 13, 'verifyTextInDialog', NULL, NULL, NULL),  ('CONTROL', 'verifyStringContains', 14, 13, 'verifyStringContains', NULL, NULL, NULL);

-- 428
ALTER TABLE `testcasecountryproperties` CHANGE COLUMN `Value` `Value1` VARCHAR(2500) NULL DEFAULT '' ,ADD COLUMN `Value2` VARCHAR(2500) NULL DEFAULT '' AFTER `Value1`;

-- 429
ALTER TABLE `testcaseexecutiondata` CHANGE COLUMN `Object` `Value1` VARCHAR(3000) NULL DEFAULT NULL ,ADD COLUMN `Value2` VARCHAR(3000) NULL DEFAULT NULL AFTER `Value1`;

-- 430
UPDATE `invariant` SET `sort`='10' WHERE `id`='37' and`sort`='1';

-- 431
UPDATE `invariant` SET `sort`='20' WHERE `id`='37' and`sort`='3';

-- 432
INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`, `VeryShortDesc`) 
  VALUES ('BROWSER', 'IE9', '30', '37', 'Internet Explorer 9 Browser', ''),        ('BROWSER', 'IE10', '40', '37', 'Internet Explorer 10 Browser', ''),        ('BROWSER', 'IE11', '50', '37', 'Internet Explorer 11 Browser', '');

-- 433
DELETE FROM `invariant` WHERE `id`='37' and`sort`='2';

-- 434
INSERT INTO `invariant` (`idname`, `value`, `sort`, `id`, `description`, `VeryShortDesc`) 
  VALUES ('INVARIANTPRIVATE', 'ACTION', '10', '44', '', ''),        ('INVARIANTPRIVATE', 'APPLITYPE', '20', '44', '', ''),        ('INVARIANTPRIVATE', 'BROWSER', '30', '44', '', ''),        ('INVARIANTPRIVATE', 'CHAIN', '40', '44', '', ''),        ('INVARIANTPRIVATE', 'CONTROL', '50', '44', '', ''),        ('INVARIANTPRIVATE', 'CTRLFATAL', '70', '44', '', ''),        ('INVARIANTPRIVATE', 'ENVACTIVE', '80', '44', '', ''),        ('INVARIANTPRIVATE', 'ENVTYPE', '100', '44', '', ''),        ('INVARIANTPRIVATE', 'GROUP', '130', '44', '', ''),        ('INVARIANTPRIVATE', 'OUTPUTFORMAT', '170', '44', '', ''),        ('INVARIANTPRIVATE', 'PROPERTYNATURE', '220', '44', '', ''),        ('INVARIANTPRIVATE', 'PROPERTYTYPE', '230', '44', '', ''),        ('INVARIANTPRIVATE', 'RUNPROD', '260', '44', '', ''),        ('INVARIANTPRIVATE', 'RUNQA', '270', '44', '', ''),        ('INVARIANTPRIVATE', 'RUNUAT', '280', '44', '', ''),        ('INVARIANTPRIVATE', 'SCREENSHOT', '290', '44', '', ''),        ('INVARIANTPRIVATE', 'SERVER', '300', '44', '', ''),        ('INVARIANTPRIVATE', 'SESSION', '310', '44', '', ''),        ('INVARIANTPRIVATE', 'TCACTIVE', '340', '44', '', ''),        ('INVARIANTPRIVATE', 'TCESTATUS', '350', '44', '', ''),        ('INVARIANTPRIVATE', 'TCREADONLY', '360', '44', '', ''),        ('INVARIANTPRIVATE', 'USERGROUP', '390', '44', '', ''),        ('INVARIANTPRIVATE', 'VERBOSE', '400', '44', '', ''),        ('INVARIANTPRIVATE', 'INVARIANTPRIVATE', '410', '44', '', ''),        ('INVARIANTPRIVATE', 'INVARIANTPUBLIC', '420', '44', '', ''),        ('INVARIANTPUBLIC', 'COUNTRY', '60', '45', '', ''),        ('INVARIANTPUBLIC', 'ENVIRONMENT', '90', '45', '', ''),        ('INVARIANTPUBLIC', 'EXECNBMIN', '110', '45', '', ''),        ('INVARIANTPUBLIC', 'FILTERNBDAYS', '120', '45', '', ''),        ('INVARIANTPUBLIC', 'MAXEXEC', '140', '45', '', ''),        ('INVARIANTPUBLIC', 'ORIGIN', '160', '45', '', ''),        ('INVARIANTPUBLIC', 'PRIORITY', '180', '45', '', ''),        ('INVARIANTPUBLIC', 'PROPERTYDATABASE', '210', '45', '', ''),        ('INVARIANTPUBLIC', 'SYSTEM', '330', '45', '', ''),        ('INVARIANTPUBLIC', 'TCSTATUS', '370', '45', '', ''),        ('INVARIANTPUBLIC', 'TEAM', '380', '45', '', '');

-- 435
ALTER TABLE `invariant` DROP PRIMARY KEY , DROP COLUMN `id` , CHANGE COLUMN `sort` `sort` INT(10) NOT NULL DEFAULT 0  , ADD PRIMARY KEY (`idname`, `value`) , ADD INDEX `IX_invariant_01` (`idname` ASC, `sort` ASC) ;

-- 436
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`) 
  VALUES ('PROPERTYTYPE', 'executeSoapFromLib', '27', 'Getting from the SOAP request using the query');

-- 437
CREATE TABLE `soaplibrary` (  `Name` VARCHAR(45) ,  `Type` VARCHAR(45) ,  `ServicePath` VARCHAR(250) ,  `Method` VARCHAR(45) ,  `Envelope` TEXT ,  `ParsingAnswer` TEXT ,  `Description` VARCHAR(1000) ,  PRIMARY KEY (`Name`))
  ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 438
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`) 
  VALUES ('INVARIANTPRIVATE', 'PROJECTACTIVE', '21', ''),        ('PROJECTACTIVE', 'Y', '10', 'Active'),        ('PROJECTACTIVE', 'N', '20', 'Disable');

-- 439
SELECT 1 FROM dual;

-- 440
SELECT 1 FROM dual;

-- 441
ALTER TABLE `testcase` DROP FOREIGN KEY `FK_testcase_03` ;

-- 442
ALTER TABLE `testcase` ADD CONSTRAINT `FK_testcase_03` FOREIGN KEY (`Project`) REFERENCES `project` (`idproject`) ON DELETE SET NULL ON UPDATE CASCADE;

-- 443
ALTER TABLE `testdata` ADD COLUMN `Description` VARCHAR(1000) NULL DEFAULT ''  AFTER `value` ;

-- 444
INSERT INTO `parameter` (`system`, `param`, `value`, `description`)
  VALUES ('', 'cerberus_log_publiccalls', 'N', 'Enable [Y] or Disable [N] the loging of all the calls done to Cerberus public servlets.');

-- 445
DROP TABLE `logglassfish`;

-- 446
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`)
  VALUES ('PROPERTYTYPE', 'getAttributeFromHtml', '38', 'Getting Attribute value from an HTML field in the current page.', '');

-- 447
SELECT 1 FROM dual;

-- 448
ALTER TABLE `testcaseexecution` DROP INDEX `IX_testcaseexecution_04` ,ADD INDEX `IX_testcaseexecution_04` (`Test` ASC, `TestCase` ASC, `Country` ASC, `Browser` ASC, `Start` ASC, `ControlStatus` ASC);

-- 449
CREATE TABLE `testbattery` (  `testbatteryID` int(10) unsigned NOT NULL AUTO_INCREMENT,  `testbattery` varchar(45) NOT NULL,  `Description` varchar(300) NOT NULL DEFAULT '',  PRIMARY KEY (`testbatteryID`),  UNIQUE KEY `IX_testbattery_01` (`testbattery`))
  ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- 450
CREATE TABLE `testbatterycontent` (  `testbatterycontentID` int(10) unsigned NOT NULL AUTO_INCREMENT,  `testbattery` varchar(45) NOT NULL,  `Test` varchar(45) NOT NULL,  `TestCase` varchar(45) NOT NULL,  PRIMARY KEY (`testbatterycontentID`),  UNIQUE KEY `IX_testbatterycontent_01` (`testbattery`, `Test`, `TestCase`),  KEY `IX_testbatterycontent_02` (`testbattery`),  KEY `IX_testbatterycontent_03` (`Test`, `TestCase`),  CONSTRAINT `FK_testbatterycontent_01` FOREIGN KEY (`testbattery`) REFERENCES `testbattery` (`testbattery`) ON DELETE CASCADE ON UPDATE CASCADE,  CONSTRAINT `FK_testbatterycontent_02` FOREIGN KEY (`Test`,`TestCase`) REFERENCES `testcase` (`Test`,`TestCase`) ON DELETE CASCADE ON UPDATE CASCADE)
  ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- 451
CREATE TABLE `campaign` (  `campaignID` int(10) unsigned NOT NULL AUTO_INCREMENT,  `campaign` varchar(45) NOT NULL,  `Description` varchar(300) NOT NULL DEFAULT '',  PRIMARY KEY (`campaignID`),  UNIQUE KEY `IX_campaign_01` (`campaign`))
  ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- 452
CREATE TABLE `campaignparameter` (  `campaignparameterID` int(10) unsigned NOT NULL AUTO_INCREMENT,  `campaign` varchar(45) NOT NULL,  `Parameter` varchar(100) NOT NULL,  `Value` varchar(100) NOT NULL,  PRIMARY KEY (`campaignparameterID`),  UNIQUE KEY `IX_campaignparameter_01` (`campaign`, `Parameter`),  KEY `IX_campaignparameter_02` (`campaign`),  CONSTRAINT `FK_campaignparameter_01` FOREIGN KEY (`campaign`) REFERENCES `campaign` (`campaign`) ON DELETE CASCADE ON UPDATE CASCADE)
  ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- 453
CREATE TABLE `campaigncontent` (  `campaigncontentID` int(10) unsigned NOT NULL AUTO_INCREMENT,  `campaign` varchar(45) NOT NULL,  `testbattery` varchar(45) NOT NULL,  PRIMARY KEY (`campaigncontentID`),  UNIQUE KEY `IX_campaigncontent_01` (`campaign`, `testbattery`),  KEY `IX_campaigncontent_02` (`campaign`),  CONSTRAINT `FK_campaigncontent_01` FOREIGN KEY (`campaign`) REFERENCES `campaign` (`campaign`) ON DELETE CASCADE ON UPDATE CASCADE,  CONSTRAINT `FK_campaigncontent_02` FOREIGN KEY (`testbattery`) REFERENCES `testbattery` (`testbattery`) ON DELETE CASCADE ON UPDATE CASCADE)
  ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- 454
ALTER TABLE `sqllibrary` CHANGE COLUMN `Name` `Name` VARCHAR(45) NOT NULL FIRST,  ADD COLUMN `Database` VARCHAR(45) NULL DEFAULT '' AFTER `Type` ;

-- 455
CREATE TABLE `robot` (`robotID` int(10) NOT NULL AUTO_INCREMENT,`robot` varchar(100) NOT NULL,`host` varchar(150) NOT NULL DEFAULT '',`port` varchar(20) NOT NULL DEFAULT '',`platform` varchar(45) NOT NULL DEFAULT '',`browser` varchar(45) NOT NULL DEFAULT '',`version` varchar(45) NOT NULL DEFAULT '',`active` varchar(1) NOT NULL DEFAULT 'Y',`description` varchar(250) NOT NULL DEFAULT '', PRIMARY KEY (`robotID`), UNIQUE KEY IX_robot_01 (`robot`))
  ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- 456
UPDATE `user` SET `DefaultIP`='' where `DefaultIP` IS NULL;

-- 457
ALTER TABLE `user` CHANGE COLUMN `DefaultIP` `robotHost` VARCHAR(150) NOT NULL DEFAULT '',ADD COLUMN `robotPort` VARCHAR(20) NOT NULL DEFAULT '' AFTER `robotHost`,ADD COLUMN `robotPlatform` VARCHAR(45) NOT NULL DEFAULT '' AFTER `robotPort`,ADD COLUMN `robotBrowser` VARCHAR(45) NOT NULL DEFAULT '' AFTER `robotPlatform`,ADD COLUMN `robotVersion` VARCHAR(45) NOT NULL DEFAULT '' AFTER `robotBrowser`, ADD COLUMN `robot` VARCHAR(100) NOT NULL DEFAULT '' AFTER `robotVersion`;

-- 458
INSERT INTO `invariant` (`idname`,`value`,`sort`,`description`,`VeryShortDesc`,`gp1`,`gp2`,`gp3`)
  VALUES  ('PLATFORM','LINUX',30,'Linux Platform','',NULL,NULL,NULL)
  ,('PLATFORM','MAC',40,'Mac Platform','',NULL,NULL,NULL)
  ,('PLATFORM','WINDOWS',70,'Windows Platform','',NULL,NULL,NULL)
  ,('PLATFORM','ANDROID',10,'Android Platform','',NULL,NULL,NULL)
  ,('PLATFORM','UNIX',50,'Unix Platform','',NULL,NULL,NULL)
  ,('PLATFORM','VISTA',60,'Windows Vista Platform','',NULL,NULL,NULL)
  ,('PLATFORM','WIN8',80,'Windows 8 Platform','',NULL,NULL,NULL)
  ,('PLATFORM','XP',90,'Windows XP Platform','',NULL,NULL,NULL)
  ,('BROWSER','IE',20,'Internet Explorer Browser','',NULL,NULL,NULL)
  ,('BROWSER','android',70,'Android browser','',NULL,NULL,NULL)
  ,('BROWSER','ipad',80,'ipad browser','',NULL,NULL,NULL)
  ,('BROWSER','iphone',90,'iphone browser','',NULL,NULL,NULL)
  ,('BROWSER','opera',60,'Opera browser','',NULL,NULL,NULL)
  ,('BROWSER','safari',60,'Safari browser','',NULL,NULL,NULL)
  ,('ROBOTACTIVE','N',2,'Disable','',NULL,NULL,NULL)
  ,('ROBOTACTIVE','Y',1,'Active','',NULL,NULL,NULL)
  ,('INVARIANTPRIVATE','ROBOTACTIVE',430,'','',NULL,NULL,NULL)
  ,('INVARIANTPRIVATE','PLATFORM','35','','',NULL,NULL,NULL);

-- 459
DELETE FROM `invariant` where `idname`='BROWSER' and `value` in ('IE9','IE10','IE11');

-- 460
ALTER TABLE `testcaseexecution` ADD COLUMN `Version` VARCHAR(20) NOT NULL DEFAULT '' AFTER `Browser`,ADD COLUMN `Platform` VARCHAR(20) NOT NULL DEFAULT '' AFTER `Version`,CHANGE COLUMN `IP` `IP` VARCHAR(150) NULL DEFAULT NULL ;

-- 461
INSERT INTO `robot` (`robot` ,`host` ,`port` ,`platform` ,`browser` ,`version` , `active` ,`description`)VALUES ('MyRobot', '127.0.0.1', '4444', 'LINUX', 'firefox', '28', 'Y', 'My Robot');

-- 462
INSERT INTO `parameter` (`system`, `param`, `value`, `description`)
  VALUES ('', 'cerberus_picture_testcase_path', '', 'Path to store the Cerberus Value and HowTo pictures of TestCase page');

-- 463
ALTER TABLE `countryenvironmentparameters` CHANGE COLUMN `IP` `IP` VARCHAR(150) NOT NULL DEFAULT '';

-- 464
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`, `gp1`)
  VALUES ('CAMPAIGN_PARAMETER', 'BROWSER', '10', 'Browser use to execute campaign', 'Browser', 'INVARIANTPRIVATE')
  ,('CAMPAIGN_PARAMETER', 'COUNTRY', '20', 'Country selected for campaign', 'Country', 'INVARIANTPUBLIC')
  ,('CAMPAIGN_PARAMETER', 'ENVIRONMENT', '30', 'Which environment used to execute campaign', 'Environment', 'INVARIANTPUBLIC')
  ,('INVARIANTPRIVATE','CAMPAIGN_PARAMETER','440','','',NULL);

-- 465
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`, `gp1`, `gp2`, `gp3`)
  VALUES ('CONTROL', 'verifyElementInElement', 32, 'verifyElementInElement', '', NULL, NULL, NULL);

-- 466
SELECT 1 FROM dual;

-- 467
update `countryenvparam` set  `Build` = '' where `Build` is null;

-- 468
update `countryenvparam` set  `Revision` = '' where `Revision` is null;

-- 469
update `countryenvparam` set  `Chain` = '' where `Chain` is null;

-- 470
update `countryenvparam` set  `DistribList` = '' where `DistribList` is null;

-- 471
update `countryenvparam` set  `EMailBodyRevision` = '' where `EMailBodyRevision` is null;

-- 472
update `countryenvparam` set  `Type` = '' where `Type` is null;

-- 473
update `countryenvparam` set  `EMailBodyChain` = '' where `EMailBodyChain` is null;

-- 474
update `countryenvparam` set  `EMailBodyDisableEnvironment` = '' where `EMailBodyDisableEnvironment` is null;

-- 475
update `countryenvparam` set  `maintenanceact` = '' where `maintenanceact` is null;

-- 476
update `countryenvparam` set  `maintenanceend` = '0' where `maintenanceend` is null;

-- 477
update `countryenvparam` set `maintenancestr` = '0' where `maintenancestr` is null;

-- 478
ALTER TABLE `countryenvparam`CHANGE COLUMN `Build` `Build` VARCHAR(10) NOT NULL DEFAULT '' ,CHANGE COLUMN `Revision` `Revision` VARCHAR(20) NOT NULL DEFAULT '' ,CHANGE COLUMN `Chain` `Chain` VARCHAR(20) NOT NULL DEFAULT '' ,CHANGE COLUMN `DistribList` `DistribList` TEXT NOT NULL ,CHANGE COLUMN `EMailBodyRevision` `EMailBodyRevision` TEXT NOT NULL  ,CHANGE COLUMN `Type` `Type` VARCHAR(20) NOT NULL DEFAULT '' ,CHANGE COLUMN `EMailBodyChain` `EMailBodyChain` TEXT NOT NULL ,CHANGE COLUMN `EMailBodyDisableEnvironment` `EMailBodyDisableEnvironment` TEXT NOT NULL ,CHANGE COLUMN `maintenanceact` `maintenanceact` VARCHAR(1) NOT NULL DEFAULT 'N' ,CHANGE COLUMN `maintenancestr` `maintenancestr` TIME NOT NULL DEFAULT 0 ,CHANGE COLUMN `maintenanceend` `maintenanceend` TIME NOT NULL DEFAULT 0 ;

-- 479
ALTER TABLE `campaignparameter` DROP INDEX `IX_campaignparameter_01` , ADD UNIQUE INDEX `IX_campaignparameter_01` (`campaign` ASC, `Parameter` ASC, `Value` ASC);

-- 480
INSERT INTO `invariant` 
  VALUES ('ACTION', 'openUrl', '65', 'openUrl', '', NULL,NULL,NULL);

-- 481
SELECT 1 FROM dual;

-- 482
ALTER TABLE `testcase` ADD COLUMN `function` VARCHAR(500) NULL DEFAULT '' AFTER `activePROD`;

-- 483
SELECT 1 FROM dual;

-- 484
INSERT INTO `parameter` (`system`, `param`, `value`, `description`)
  VALUES ('', 'cerberus_testcase_function_urlForListOfFunction', '/URL/TO/FUNCTION/SERVICE', 'URL to feed the function field with proposal for autocompletion. URL should respond JSON format');

-- 485
INSERT INTO `parameter` (`system`, `param`, `value`, `description`)
  VALUES ('', 'cerberus_testcase_function_booleanListOfFunction', 'N', 'boolean to activate autocompletion on function fields.');

-- 486
SELECT 1 FROM dual;

-- 487
INSERT INTO `invariant`
  VALUES  ('SYNCHRONEOUS', 'N', '2', 'Redirect to the execution before the end of the execution', '', NULL, NULL, NULL), ('SYNCHRONEOUS', 'Y', '1', 'Redirect to the execution after the end of the execution', '', NULL, NULL, NULL);

-- 488
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`)
  VALUES ('INVARIANTPRIVATE', 'SYNCHRONEOUS', '430', '', '');

-- 489
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`)
  VALUES ('ACTION', 'callSoapWithBase', '190', 'callSoapWithBase', '');

-- 490
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`)
  VALUES ('CONTROL', 'verifyXmlTreeStructure', '90', 'verifyXmlTreeStructure', '');

-- 491
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`)
  VALUES ('ACTION', 'mouseDownMouseUp', '200', 'mouseDownMouseUp', '');

-- 492
SELECT 1 FROM dual;

-- 493
ALTER TABLE `testcasestep` ADD COLUMN `useStep` VARCHAR(1) NULL DEFAULT 'N' AFTER `Description`, ADD COLUMN `useStepTest` VARCHAR(45) NOT NULL DEFAULT '' AFTER `useStep`, ADD COLUMN `useStepTestCase` VARCHAR(45) NOT NULL DEFAULT '' AFTER `useStepTest`, ADD COLUMN `useStepStep` INT(10) NOT NULL  AFTER `useStepTestCase`;

-- 494
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`)
  VALUES  ('CONTROL','verifyElementClickable',35,'isElementClickable',''), ('CONTROL','verifyElementNotClickable',36,'isElementNotClickable','');

-- 495
SELECT 1 FROM dual;

-- 496
DELETE FROM `invariant` WHERE `idname`='ACTION' and `value`='mouseDownMouseUp';

-- 497
SELECT 1 FROM dual;

-- 498
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`)
  VALUES ('ACTION', 'getPageSource', '210', 'getPageSource', '');

-- 499
SELECT 1 FROM dual;

-- 500
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`)
  VALUES  ('ACTION', 'callSoap', '189', 'callSoap', ''), ('PROPERTYTYPE', 'getFromXml', '50', 'getFromXml', '');

-- 501
SELECT 1 FROM dual;

-- 502
SELECT 1 FROM dual;

-- 503
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`)
  VALUES  ('PROPERTYTYPE', 'getFromCookie', '60', 'getFromCookie', '');

-- 504
SELECT 1 FROM dual;

-- 505
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`)
  VALUES  ('INVARIANTPRIVATE', 'PAGESOURCE', '440', '', ''), ('INVARIANTPRIVATE', 'SELENIUMLOG', '450', '', ''), ('PAGESOURCE', '0', '10', 'Never get Page Source', ''), ('PAGESOURCE', '1', '20', 'Get Page Source on error only', ''), ('PAGESOURCE', '2', '30', 'Get Page Source after each action', ''), ('SELENIUMLOG', '0', '10', 'Never record Selenium Log', ''), ('SELENIUMLOG', '1', '20', 'Record Selenium Log on error only', ''), ('SELENIUMLOG', '2', '30', 'Record Selenium Log on testcase', '');

-- 506
ALTER TABLE `testcasestepactionexecution` ADD COLUMN `PageSourceFileName` VARCHAR(150) NULL DEFAULT NULL AFTER `ScreenshotFilename`;

-- 507
ALTER TABLE `testcasestepactioncontrolexecution` ADD COLUMN `PageSourceFilename` VARCHAR(150) NULL DEFAULT NULL AFTER `ScreenshotFilename`;

-- 508
SELECT 1 FROM dual;

-- 509
ALTER TABLE `testcasestep` CHANGE COLUMN `useStepStep` `useStepStep` INT(10) NOT NULL DEFAULT '0' ;

-- 510
CREATE TABLE `usersystem` (`Login` VARCHAR(10) NOT NULL,`System` VARCHAR(45) NOT NULL,PRIMARY KEY (`Login`, `System`)) 
  ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 511
insert into usersystem select u.login, i.value from user u, invariant i where i.idname='SYSTEM';

-- 512
ALTER TABLE `application` CHANGE COLUMN `sort` `sort` INT(11) NOT NULL DEFAULT 10 ;

-- 513
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`)
  VALUES ('APPLITYPE', 'WS', '30', 'Web Service Application');

-- 514
ALTER TABLE testcaseexecution ADD COLUMN `Executor` VARCHAR(10) NULL;

-- 515
SELECT 1 FROM dual;

-- 516
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`)
  VALUES ('PROPERTYTYPE', 'getDifferencesFromXml', '51', 'Get differences from XML files');

-- 517
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`)
  VALUES ('ACTION', 'removeDifference', '220', 'Remove differences from the given pattern');

-- 518
ALTER TABLE `testdata` ADD COLUMN `Application` VARCHAR(45) NOT NULL DEFAULT '' AFTER `Description`,ADD COLUMN `Country` VARCHAR(2) NOT NULL DEFAULT '' AFTER `Application`,ADD COLUMN `Environment` VARCHAR(45) NOT NULL DEFAULT '' AFTER `Country`;

-- 519
ALTER TABLE `testdata` DROP PRIMARY KEY, ADD PRIMARY KEY (`key`, `Environment`, `Country`, `Application`);

-- 520
ALTER TABLE `soaplibrary` CHANGE COLUMN `Name` `Name` VARCHAR(255) NOT NULL DEFAULT '' ;

-- 521
ALTER TABLE `soaplibrary` CHANGE COLUMN `Envelope` `Envelope` MEDIUMTEXT NULL DEFAULT NULL ;

-- 522
DROP TABLE `usersystem` ;

-- 523
CREATE TABLE `usersystem` (  `Login` VARCHAR(10) NOT NULL,  `System` VARCHAR(45) NOT NULL, PRIMARY KEY (`Login`, `System`),  CONSTRAINT `FK_usersystem_01` FOREIGN KEY (`Login` ) REFERENCES `user` (`Login`) ON DELETE CASCADE ON UPDATE CASCADE )
  ENGINE=InnoDB DEFAULT CHARSET=utf8 ;

-- 524
INSERT INTO usersystem  SELECT u.login, i.value FROM user u, invariant i WHERE i.idname='SYSTEM';

-- 525
CREATE TABLE `testdatalib` (  `Name` varchar(200) NOT NULL,  `system` varchar(45) NOT NULL DEFAULT '',  `Environment` varchar(45) NOT NULL DEFAULT '',  `Country` varchar(2) NOT NULL DEFAULT '',  `Group` varchar(200) NOT NULL DEFAULT '',  `Type` varchar(45) NOT NULL DEFAULT '',  `Database` varchar(45) NOT NULL DEFAULT '',  `Script` varchar(2500) NOT NULL DEFAULT '',  `ServicePath` varchar(250) NOT NULL DEFAULT '',  `Method` varchar(45) NOT NULL DEFAULT '',  `Envelope` text,  `Description` varchar(1000) NOT NULL DEFAULT '',  PRIMARY KEY (`Name`,`system`,`Environment`,`Country`))
  ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 526
CREATE TABLE `testdatalibdata` (  `Name` varchar(200) NOT NULL,  `system` varchar(45) NOT NULL DEFAULT '',  `Environment` varchar(45) NOT NULL DEFAULT '',  `Country` varchar(2) NOT NULL DEFAULT '',  `SubData` varchar(200) NOT NULL DEFAULT '',  `Value` text,  `Column` varchar(255) NOT NULL DEFAULT '',  `ParsingAnswer` text ,  `Description` varchar(1000)  NOT NULL DEFAULT '',  PRIMARY KEY (`Name`,`system`,`Environment`,`Country`,`SubData`),  CONSTRAINT `FK_testdatalibdata_01` FOREIGN KEY (`Name`,`system`,`Environment`,`Country`) REFERENCES `testdatalib` (`Name`,`system`,`Environment`,`Country`) ON DELETE CASCADE ON UPDATE CASCADE)
  ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 527
INSERT INTO testdatalib (`system`,`Country`,`Environment`,`Name`, `Type`, `Description`, `Envelope`)  SELECT '', `Country`, `Environment`, `key`, 'STATIC', IFNULL(td.`Description`,''), '' from testdata td ON DUPLICATE KEY UPDATE Description = IFNULL(td.`Description`,'');

-- 528
INSERT INTO testdatalibdata (`system`,`Country`,`Environment`,`Name`, `SubData`, `Value`, `Description`, `ParsingAnswer`)  SELECT '', `Country`, `Environment`, `key`, '', IFNULL(td.`value`,''), IFNULL(td.`Description`,''), '' from testdata td ON DUPLICATE KEY UPDATE `Value` = IFNULL(td.`value`,''), Description = IFNULL(td.`Description`,'');

-- 529
INSERT INTO testdatalib (`system`,`Country`,`Environment`,`Name`, `Group`, `Type`, `Database`, `Script`, `Description`, `Envelope`)  SELECT '', '', '', `Name`, IFNULL(`Type`,''), 'SQL', IFNULL(`Database`,''), IFNULL(`Script`,''), IFNULL(description,'') , '' from sqllibrary sl ON DUPLICATE KEY UPDATE `Group`=IFNULL(sl.Type,''), `Database`=IFNULL(sl.`Database`,''), `Script`=IFNULL(sl.`Script`,''), Description=IFNULL(sl.Description,'');

-- 530
INSERT INTO testdatalibdata (`system`,`Country`,`Environment`,`Name`, `SubData`, `Column`, `Description`, `ParsingAnswer`, `Value`)  SELECT '', '', '', `Name`, '', '', IFNULL(description,''), '', '' from sqllibrary sl ON DUPLICATE KEY UPDATE Description=IFNULL(sl.Description,'');

-- 531
INSERT INTO testdatalib (`system`,`Country`,`Environment`,`Name`, `Group`, `Type`, `ServicePath`, `Method`, `Envelope`, `Description`)  SELECT '', '', '', `Name`, IFNULL(`Type`,''), 'SOAP', IFNULL(`ServicePath`,''), IFNULL(`Method`,''), IFNULL(Envelope,''), IFNULL(description,'') from soaplibrary sl ON DUPLICATE KEY UPDATE `Group`=IFNULL(sl.Type,''), `ServicePath`=IFNULL(sl.`ServicePath`,''), `Method`=IFNULL(sl.`Method`,''), `Envelope`=IFNULL(sl.`Envelope`,''), Description=IFNULL(sl.Description,'');

-- 532
INSERT INTO testdatalibdata (`system`,`Country`,`Environment`,`Name`, `SubData`, `ParsingAnswer`, `Description`, `Value`)  SELECT '', '', '', `Name`, '', IFNULL(ParsingAnswer,''), IFNULL(description, ''), '' from soaplibrary sl ON DUPLICATE KEY UPDATE `ParsingAnswer`=IFNULL(sl.ParsingAnswer,''), Description=IFNULL(sl.Description,'');

-- 533
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`)
  VALUES  ('INVARIANTPRIVATE', 'TESTDATATYPE', '460', '', ''), ('TESTDATATYPE', 'STATIC', '10', 'Static test data.', ''), ('TESTDATATYPE', 'SQL', '20', 'Dynamic test data from SQL execution.', ''), ('TESTDATATYPE', 'SOAP', '30', 'Dynamic test data from SOAP Webservice call.', '');

-- 534
ALTER TABLE `testdatalib` ADD COLUMN `TestDataLibID` INT UNSIGNED NOT NULL AUTO_INCREMENT FIRST,DROP PRIMARY KEY,ADD PRIMARY KEY (`TestDataLibID`),ADD UNIQUE INDEX `IX_testdatalib_01` (`Name` ASC, `system` ASC, `Environment` ASC, `Country` ASC);

-- 535
ALTER TABLE `testdatalibdata` ADD COLUMN `TestDataLibID` INT UNSIGNED NOT NULL DEFAULT 0 FIRST;

-- 536
UPDATE `testdatalibdata` ld, `testdatalib` l SET ld.TestDataLibID=l.TestDataLibID WHERE ld.`Name`=l.`Name` and ld.`system`=l.`system` and ld.`Environment`=l.`Environment` and ld.`Country`=l.`Country`;

-- 537
ALTER TABLE `testdatalibdata` DROP FOREIGN KEY `FK_testdatalibdata_01`;

-- 538
ALTER TABLE `testdatalibdata` DROP COLUMN `Country`, DROP COLUMN `Environment`, DROP COLUMN `system`, DROP COLUMN `Name`, DROP PRIMARY KEY, ADD PRIMARY KEY (`TestDataLibID`, `SubData`);

-- 539
ALTER TABLE `testdatalibdata` ADD CONSTRAINT `FK_testdatalibdata_01`  FOREIGN KEY (`TestDataLibID`)  REFERENCES `testdatalib` (`TestDataLibID`) ON DELETE CASCADE ON UPDATE CASCADE;

-- 540
DELETE FROM invariant where idname='EXECNBMIN' or (idname='INVARIANTPUBLIC' and value='EXECNBMIN');

-- 541
SELECT 1 FROM dual;

-- 542
ALTER TABLE `testcase` DROP COLUMN `Sla`;

-- 543
ALTER TABLE `invariant` CHANGE COLUMN `value` `value` VARCHAR(255) NOT NULL , CHANGE COLUMN `description` `description` VARCHAR(255) NOT NULL , CHANGE COLUMN `gp1` `gp1` VARCHAR(255) NULL DEFAULT NULL , CHANGE COLUMN `gp2` `gp2` VARCHAR(255) NULL DEFAULT NULL , CHANGE COLUMN `gp3` `gp3` VARCHAR(255) NULL DEFAULT NULL;

-- 544
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`)
  VALUES ('APPLITYPE', 'APK', '40', 'Android Application', '');

-- 545
ALTER TABLE `testcasestep` ADD COLUMN `inlibrary` VARCHAR(1) NULL DEFAULT 'N' AFTER `useStepStep`;

-- 546
CREATE TABLE `testcaseexecutionqueue` (  `ID` bigint(20) unsigned NOT NULL AUTO_INCREMENT,  `Test` varchar(45) NOT NULL,  `TestCase` varchar(45) NOT NULL,  `Country` varchar(2) NOT NULL,  `Environment` varchar(45) NOT NULL,  `Robot` varchar(45) DEFAULT NULL,  `RobotIP` varchar(150) DEFAULT NULL,  `RobotPort` varchar(20) DEFAULT NULL,  `Browser` varchar(45) NOT NULL,  `BrowserVersion` varchar(20) DEFAULT NULL,  `Platform` varchar(45) DEFAULT NULL,  `ManualURL` tinyint(1) NOT NULL DEFAULT '0',  `ManualHost` varchar(255) DEFAULT NULL,  `ManualContextRoot` varchar(255) DEFAULT NULL,  `ManualLoginRelativeURL` varchar(255) DEFAULT NULL,  `ManualEnvData` varchar(255) DEFAULT NULL,  `Tag` varchar(255) NOT NULL,  `OutputFormat` varchar(20) NOT NULL DEFAULT 'gui',  `Screenshot` int(11) NOT NULL DEFAULT '0',  `Verbose` int(11) NOT NULL DEFAULT '0',  `Timeout` mediumtext,  `Synchroneous` tinyint(1) NOT NULL DEFAULT '0',  `PageSource` int(11) NOT NULL DEFAULT '1',  `SeleniumLog` int(11) NOT NULL DEFAULT '1',  `RequestDate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,  `Proceeded` tinyint(1) NOT NULL DEFAULT '0',  PRIMARY KEY (`ID`),  KEY `IX_testcaseexecution_01` (`Test`,`TestCase`,`Country`),  KEY `IX_testcaseexecution_02` (`Tag`),  CONSTRAINT `FK_testcaseexecutionqueue_01` FOREIGN KEY (`Test`, `TestCase`, `Country`) REFERENCES `testcasecountry` (`Test`, `TestCase`, `Country`) ON DELETE CASCADE ON UPDATE CASCADE)
  ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 547
SELECT 1 FROM dual;

-- 548
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`)
  VALUES ('INVARIANTPRIVATE', 'MANUALEXECUTION', '470', '', '')
  ,('MANUALEXECUTION', 'Y', '2', 'Manual Execution', '')
  ,('MANUALEXECUTION', 'N', '1', 'Automatic Execution', '');

-- 549
ALTER TABLE `testcasestepactioncontrolexecution` ADD INDEX `IX_testcasestepactioncontrolexecution_01` (`Start` ASC);

-- 550
ALTER TABLE `testcasestepactionexecution` ADD INDEX `IX_testcasestepactionexecution_01` (`Start` ASC);

-- 551
ALTER TABLE `testcasestepexecution` ADD INDEX `IX_testcasestepexecution_01` (`Start` ASC);

-- 552
ALTER TABLE `testcaseexecutionwwwdet` ADD INDEX `IX_testcaseexecutionwwwdet_01` (`Start` ASC);

-- 553
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`)
  VALUES ('CONTROL', 'verifyElementEquals', 44, 'verifyElementEquals');

-- 554
UPDATE `invariant` SET `sort`='32' WHERE `idname`='CONTROL' and`value`='verifyElementEquals';

-- 555
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`)
  VALUES ('CONTROL', 'verifyElementDifferent', 33, 'verifyElementDifferent')
  ,('CONTROL', 'verifyIntegerEquals', 18, 'verifyIntegerEquals')
  ,('CONTROL', 'verifyIntegerDifferent', 19, 'verifyIntegerDifferent');

-- 556
SELECT 1 FROM dual;

-- 557
ALTER TABLE `soaplibrary` CHANGE COLUMN `Method` `Method` VARCHAR(255) NULL DEFAULT NULL ;

-- 558
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`)
  VALUES ('CONTROL', 'verifyTextNotInElement', 41, 'verifyTextNotInElement');

-- 559
SELECT 1 FROM dual;

-- 560
ALTER TABLE `testcasestepexecution`  ADD COLUMN `ReturnMessage` VARCHAR(500) NULL DEFAULT NULL AFTER `ReturnCode`;

-- 561
ALTER TABLE `test` ADD COLUMN `last_modified` TIMESTAMP NULL DEFAULT '1970-01-01 01:01:01';

-- 562
ALTER TABLE `testcase` ADD COLUMN `last_modified` TIMESTAMP NULL DEFAULT '1970-01-01 01:01:01';

-- 563
ALTER TABLE `testcasecountry` ADD COLUMN `last_modified` TIMESTAMP NULL DEFAULT '1970-01-01 01:01:01';

-- 564
ALTER TABLE `testcasecountryproperties` ADD COLUMN `last_modified` TIMESTAMP NULL DEFAULT '1970-01-01 01:01:01';

-- 565
ALTER TABLE `testcasestep` ADD COLUMN `last_modified` TIMESTAMP NULL DEFAULT '1970-01-01 01:01:01';

-- 566
ALTER TABLE `testcasestepaction` ADD COLUMN `last_modified` TIMESTAMP NULL DEFAULT '1970-01-01 01:01:01';

-- 567
ALTER TABLE `testcasestepactioncontrol` ADD COLUMN `last_modified` TIMESTAMP NULL DEFAULT '1970-01-01 01:01:01';

-- 568
ALTER TABLE `testcasestepaction` ADD COLUMN `ScreenshotFileName` VARCHAR(150) NULL DEFAULT NULL AFTER `Description`;

-- 569
ALTER TABLE `testcasestepactioncontrol` ADD COLUMN `ScreenshotFileName` VARCHAR(150) NULL DEFAULT NULL AFTER `Fatal`;

-- 570
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) 
  VALUES ('PROPERTYTYPE', 'getFromJson', '70', 'Getting value from a Json file', '');

-- 571
SELECT 1 FROM dual;

-- 572
INSERT INTO `parameter` (`system`, `param`,`value`, `description`)
  VALUES ('', 'solr_url','', 'URL of Solr search Engine used on Search Testcase Page. Value is empty if no Solr implementation is available');

-- 573
INSERT INTO `parameter` (`system`, `param`, `value`, `description`)
  VALUES ('', 'cerberus_execution_threadpool_size', '10', 'Number of Simultaneous execution handled by Cerberus');

-- 574
ALTER TABLE `testcaseexecutionqueue` ADD COLUMN `comment` VARCHAR(250) NULL DEFAULT NULL AFTER `proceeded`, ADD COLUMN `retries` TINYINT(1) NOT NULL DEFAULT '0' AFTER `comment`,ADD COLUMN `manualexecution` VARCHAR(1) NOT NULL DEFAULT 'N' AFTER `retries`;

-- 575
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`)
  VALUES ('OUTPUTFORMAT', 'redirectToReport', '4', 'Go to ReportByTag page', '');

-- 576
SELECT 1 FROM dual;

-- 577
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`)
  VALUES ('INVARIANTPRIVATE', 'RETRIES', '470', '', ''), ('RETRIES', '0', '10', 'Do not retry in case of Not OK', ''), ('RETRIES', '1', '20', 'Retry 1 time in case of Not OK', ''), ('RETRIES', '2', '30', 'Retry 2 times in case of Not OK', ''), ('RETRIES', '3', '40', 'Retry 3 times in case of Not OK', '');

-- 578
ALTER TABLE `robot` ADD COLUMN `useragent` VARCHAR(250) NOT NULL DEFAULT '' AFTER `active`;

-- 579
ALTER TABLE `countryenvironmentparameters` ADD COLUMN `domain` VARCHAR(150) NOT NULL DEFAULT '' AFTER `IP`;

-- 580
SELECT 1 FROM dual;

-- 581
SELECT 1 FROM dual;

-- 582
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) 
  VALUES ('PROPERTYTYPE', 'getFromDataLib', '75', 'Determines the data value associated with a library entry', 'Data value');

-- 583
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`) 
  VALUES ('USERGROUP', 'TestDataManager', '130', 'User that can manage the testdatalibrary');

-- 584
ALTER TABLE `testcasestepaction`  CHANGE COLUMN `Property` `Property` VARCHAR(150) NULL DEFAULT NULL;

-- 585
INSERT INTO usergroup SELECT distinct Login, 'TestDataManager' FROM usergroup where GroupName in ('Test');

-- 586
ALTER TABLE `documentation` ADD COLUMN `Lang` VARCHAR(45) NOT NULL DEFAULT 'en' AFTER `DocValue`, DROP PRIMARY KEY, ADD PRIMARY KEY (`DocTable`, `DocField`, `DocValue`, `Lang`);

-- 587
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`) 
  VALUES ('INVARIANTPUBLIC', 'FUNCTION', '400', '');

-- 588
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) 
  VALUES ('INVARIANTPRIVATE', 'LANGUAGE', '500', '', ''),        ('LANGUAGE', 'en', '100', 'English', 'English');

-- 589
ALTER TABLE `user` ADD COLUMN `Language` VARCHAR(45) NULL DEFAULT 'en' AFTER `Team`;

-- 590
SELECT 1 FROM dual;

-- 591
SELECT 1 FROM dual;

-- 592
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) 
  VALUES ('PROPERTYTYPE', 'getFromJS', '37', 'Getting data from javascript variable', '');

-- 593
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) 
  VALUES ('INVARIANTPUBLIC', 'SCREENSIZE', '400', '', '')
  ,('SCREENSIZE', '320*480', '10', '320 px /  480 px', '')
  ,('SCREENSIZE', '360*640', '20', '360 px /  640 px', '')
  ,('SCREENSIZE', '1024*768', '30', '1024 px /  768 px', '')
  ,('SCREENSIZE', '1280*600', '40', '1280 px /  600 px', '')
  ,('SCREENSIZE', '1280*800', '50', '1280 px /  800 px', '')
  ,('SCREENSIZE', '1280*980', '60', '1280 px /  980 px', '')
  ,('SCREENSIZE', '1920*900', '70', '1920 px /  900 px', '');

-- 594
SELECT 1 FROM dual;

-- 595
ALTER TABLE `testcaseexecution`  ADD COLUMN `screensize` VARCHAR(45) NULL DEFAULT NULL AFTER `Executor`;

-- 596
SELECT 1 FROM dual;

-- 597
SELECT 1 FROM dual;

-- 598
SELECT 1 FROM dual;

-- 599
SELECT 1 FROM dual;

-- 600
SELECT 1 FROM dual;

-- 601
SELECT 1 FROM dual;

-- 602
SELECT 1 FROM dual;

-- 603
SELECT 1 FROM dual;

-- 604
SELECT 1 FROM dual;

-- 605
SELECT 1 FROM dual;

-- 606
SELECT 1 FROM dual;

-- 607
SELECT 1 FROM dual;

-- 608
SELECT 1 FROM dual;

-- 609
SELECT 1 FROM dual;

-- 610
SELECT 1 FROM dual;

-- 611
SELECT 1 FROM dual;

-- 612
SELECT 1 FROM dual;

-- 613
ALTER TABLE `logevent` CHANGE COLUMN `LogEventID` `LogEventID` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT ;

-- 614
ALTER TABLE `testcaseexecutionqueue` CHANGE COLUMN `Robot` `Robot` VARCHAR(100) NULL DEFAULT NULL ,CHANGE COLUMN `BrowserVersion` `BrowserVersion` VARCHAR(45) NULL DEFAULT NULL ;

-- 615
ALTER TABLE `testcaseexecution` CHANGE COLUMN `Browser` `Browser` VARCHAR(45) NULL DEFAULT NULL ,CHANGE COLUMN `Version` `Version` VARCHAR(45) NOT NULL DEFAULT '' ,CHANGE COLUMN `Platform` `Platform` VARCHAR(45) NOT NULL DEFAULT '' ,CHANGE COLUMN `BrowserFullVersion` `BrowserFullVersion` VARCHAR(200) NULL DEFAULT '' ;

-- 616
ALTER TABLE `application` DROP FOREIGN KEY `FK_application_01`;

-- 617
ALTER TABLE `application` ADD CONSTRAINT `FK_application_01`  FOREIGN KEY (`deploytype`) REFERENCES `deploytype` (`deploytype`) ON DELETE SET NULL ON UPDATE CASCADE;

-- 618
SELECT 1 FROM dual;

-- 619
SELECT 1 FROM dual;

-- 620
SELECT 1 FROM dual;

-- 621
SELECT 1 FROM dual;

-- 622
SELECT 1 FROM dual;

-- 623
SELECT 1 FROM dual;

-- 624
SELECT 1 FROM dual;

-- 625
SELECT 1 FROM dual;

-- 626
SELECT 1 FROM dual;

-- 627
SELECT 1 FROM dual;

-- 628
SELECT 1 FROM dual;

-- 629
SELECT 1 FROM dual;

-- 630
INSERT INTO `invariant` (idname, value, sort, description, VeryShortDesc)
  VALUES ('INVARIANTPRIVATE', 'TESTACTIVE', '510', '', '')
  ,('INVARIANTPRIVATE', 'TESTAUTOMATED', '520', '', '')
  ,('TESTACTIVE', 'Y', '10', 'Active', '')
  ,('TESTACTIVE', 'N', '20', 'Disable', '')
  ,('TESTAUTOMATED', 'Y', '10', 'Automated', '')
  ,('TESTAUTOMATED', 'N', '20', 'Not automated', '');

-- 631
SELECT 1 FROM dual;

-- 632
SELECT 1 FROM dual;

-- 633
SELECT 1 FROM dual;

-- 634
SELECT 1 FROM dual;

-- 635
SELECT 1 FROM dual;

-- 636
ALTER TABLE `logevent` CHANGE COLUMN `Page` `Page` VARCHAR(200) NULL DEFAULT NULL ;

-- 637
SELECT 1 FROM dual;

-- 638
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) 
  VALUES ('ACTION', 'executeSqlUpdate', '230', 'Execute SQL Script (Update, Delete, Insert)', ''), ('ACTION', 'executeSqlStoredProcedure', '240', 'Execute Stored Procedure', ''), ('ACTION', 'skipAction', '250', 'Skip Action', '');

-- 639
SELECT 1 FROM dual;

-- 640
SELECT 1 FROM dual;

-- 641
SELECT 1 FROM dual;

-- 642
ALTER TABLE `testdatalib` CHANGE COLUMN `Method` `Method` VARCHAR(255) NOT NULL DEFAULT '' ;

-- 643
ALTER TABLE `documentation` CHANGE COLUMN `DocLabel` `DocLabel` VARCHAR(300) NULL DEFAULT NULL ;

-- 644
SELECT 1 FROM dual;

-- 645
SELECT 1 FROM dual;

-- 646
INSERT INTO `parameter` (`system`, `param`, `value`, `description`)
  VALUES ('', 'cerberus_screenshot_max_size', '1048576', 'Max size in bytes for a screenshot take while test case execution');

-- 647
SELECT 1 FROM dual;

-- 648
SELECT 1 FROM dual;

-- 649
SELECT 1 FROM dual;

-- 650
SELECT 1 FROM dual;

-- 651
DELETE FROM `invariant` WHERE `idname`='GROUP' and`value`='';

-- 652
UPDATE testcase SET `group`='MANUAL' WHERE `group` = '' or `group` is null;

-- 653
SELECT 1 FROM dual;

-- 654
SELECT 1 FROM dual;

-- 655
SELECT 1 FROM dual;

-- 656
ALTER TABLE `countryenvparam_log` ADD COLUMN `Creator` VARCHAR(10) NULL DEFAULT NULL AFTER `datecre`;

-- 657
ALTER TABLE `countryenvparam_log` ADD INDEX `FK_countryenvparam_log_02_IX` (`system` ASC, `Build` ASC, `Revision` ASC );

-- 658
ALTER TABLE `countryenvparam` ADD COLUMN `Description` VARCHAR(200) NOT NULL DEFAULT '' AFTER `Environment`;

-- 659
SELECT 1 FROM dual;

-- 660
SELECT 1 FROM dual;

-- 661
SELECT 1 FROM dual;

-- 662
SELECT 1 FROM dual;

-- 663
ALTER TABLE `testdatalibdata`  ADD COLUMN `TestDataLibDataID` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT FIRST,  DROP PRIMARY KEY,  ADD PRIMARY KEY (`TestDataLibDataID`),  ADD UNIQUE INDEX `IX_testdatalibdata_01` (`TestDataLibID` ASC, `SubData` ASC);

-- 664
SELECT 1 FROM dual;

-- 665
SELECT 1 FROM dual;

-- 666
SELECT 1 FROM dual;

-- 667
SELECT 1 FROM dual;

-- 668
ALTER TABLE `testdatalib` ADD COLUMN `Creator` VARCHAR(45) NOT NULL DEFAULT '' AFTER `Description`,ADD COLUMN `Created` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER `Creator`,ADD COLUMN `LastModifier` VARCHAR(45) NOT NULL DEFAULT '' AFTER `Created`,ADD COLUMN `LastModified` TIMESTAMP NOT NULL DEFAULT '2000-01-01 00:00:00' AFTER `LastModifier`;

-- 669
SELECT 1 FROM dual;

-- 670
SELECT 1 FROM dual;

-- 671
UPDATE `invariant` SET `value`='getFromDataLib_BETA', `description`='[Beta] Determines the data value associated with a library entry'  WHERE `idname`='PROPERTYTYPE' and`value`='getFromDataLib';

-- 672
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`)
  VALUES ('ACTION', 'callSoap_BETA', '900', '[BETA] callSoap', ''),    ('ACTION', 'callSoapWithBase_BETA', '910', '[BETA] callSoapWithBase', '');

-- 673
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`)
  VALUES ('CONTROL', 'takeScreenshot', '100', 'Take a screenshot.', '');

-- 674
UPDATE `invariant` SET `description`='[DEPRECATED] takeScreenshot' WHERE `idname`='ACTION' and`value`='takeScreenshot';

-- 675
UPDATE `invariant` SET `description`='[DEPRECATED] clickAndWait' WHERE `idname`='ACTION' and`value`='clickAndWait';

-- 676
UPDATE `invariant` SET `description`='[DEPRECATED] enter' WHERE `idname`='ACTION' and`value`='enter';

-- 677
UPDATE `invariant` SET `description`='[DEPRECATED] selectAndWait' WHERE `idname`='ACTION' and`value`='selectAndWait';

-- 678
SELECT 1 FROM dual;

-- 679
SELECT 1 FROM dual;

-- 680
ALTER TABLE `testdata`  CHANGE COLUMN `Application` `Application` VARCHAR(200) NOT NULL DEFAULT '' , CHANGE COLUMN `Country` `Country` VARCHAR(45) NOT NULL DEFAULT '' ;

-- 681
ALTER TABLE `testcase`  DROP FOREIGN KEY `FK_testcase_02`;

-- 682
ALTER TABLE `testcase`  CHANGE COLUMN `Application` `Application` VARCHAR(200) NULL DEFAULT NULL ;

-- 683
ALTER TABLE `testcaseexecution`  DROP FOREIGN KEY `FK_testcaseexecution_02`;

-- 684
ALTER TABLE `testcaseexecution`  CHANGE COLUMN `Country` `Country` VARCHAR(45) NULL DEFAULT NULL , CHANGE COLUMN `Application` `Application` VARCHAR(200) NULL DEFAULT NULL ;

-- 685
ALTER TABLE `countryenvironmentparameters`  DROP FOREIGN KEY `FK_countryenvironmentparameters_01`, DROP FOREIGN KEY `FK_countryenvironmentparameters_02`;

-- 686
ALTER TABLE `countryenvironmentparameters`  CHANGE COLUMN `Country` `Country` VARCHAR(45) NOT NULL , CHANGE COLUMN `Application` `Application` VARCHAR(200) NOT NULL ;

-- 687
ALTER TABLE `buildrevisionparameters`  DROP FOREIGN KEY `FK_buildrevisionparameters_01`;

-- 688
ALTER TABLE `buildrevisionparameters`  CHANGE COLUMN `Application` `Application` VARCHAR(200) NULL DEFAULT NULL ;

-- 689
ALTER TABLE `application`  CHANGE COLUMN `Application` `Application` VARCHAR(200) NOT NULL ;

-- 690
ALTER TABLE `testcaseexecutionqueue`  DROP FOREIGN KEY `FK_testcaseexecutionqueue_01`;

-- 691
ALTER TABLE `testcaseexecutionqueue`  CHANGE COLUMN `Country` `Country` VARCHAR(45) NOT NULL ;

-- 692
ALTER TABLE `testcasecountryproperties`  DROP FOREIGN KEY `FK_testcasecountryproperties_01`;

-- 693
ALTER TABLE `testcasecountryproperties` CHANGE COLUMN `Country` `Country` VARCHAR(45) NOT NULL ;

-- 694
ALTER TABLE `testcasecountry` CHANGE COLUMN `Country` `Country` VARCHAR(45) NOT NULL ;

-- 695
ALTER TABLE `testdatalib` CHANGE COLUMN `Country` `Country` VARCHAR(45) NOT NULL DEFAULT '' ;

-- 696
ALTER TABLE `buildrevisionbatch` DROP FOREIGN KEY `FK_buildrevisionbatch_02`;

-- 697
ALTER TABLE `buildrevisionbatch` CHANGE COLUMN `Country` `Country` VARCHAR(45) NULL DEFAULT NULL ;

-- 698
ALTER TABLE `countryenvdeploytype` DROP FOREIGN KEY `FK_countryenvdeploytype_01`;

-- 699
ALTER TABLE `countryenvdeploytype` CHANGE COLUMN `Country` `Country` VARCHAR(45) NOT NULL ;

-- 700
ALTER TABLE `countryenvironmentdatabase` DROP FOREIGN KEY `FK_countryenvironmentdatabase_01`;

-- 701
ALTER TABLE `countryenvironmentdatabase` CHANGE COLUMN `Country` `Country` VARCHAR(45) NOT NULL ;

-- 702
ALTER TABLE `countryenvparam` CHANGE COLUMN `Country` `Country` VARCHAR(45) NOT NULL ;

-- 703
ALTER TABLE `host` DROP FOREIGN KEY `FK_host_01`;

-- 704
ALTER TABLE `host` CHANGE COLUMN `Country` `Country` VARCHAR(45) NOT NULL ;

-- 705
ALTER TABLE `countryenvparam_log` DROP FOREIGN KEY `FK_countryenvparam_log_01`;

-- 706
ALTER TABLE `countryenvparam_log` CHANGE COLUMN `Country` `Country` VARCHAR(45) NOT NULL ;

-- 707
ALTER TABLE `countryenvlink` DROP FOREIGN KEY `FK_countryenvlink_01`;

-- 708
ALTER TABLE `countryenvlink` CHANGE COLUMN `Country` `Country` VARCHAR(45) NOT NULL ;

-- 709
ALTER TABLE `countryenvlink` ADD CONSTRAINT `FK_countryenvlink_01` FOREIGN KEY (`system` , `Country` , `Environment`) REFERENCES `countryenvparam` (`system` , `Country` , `Environment`) ON DELETE CASCADE ON UPDATE CASCADE;

-- 710
ALTER TABLE `testcase`  ADD CONSTRAINT `FK_testcase_02` FOREIGN KEY (`Application`) REFERENCES `application` (`Application`) ON DELETE CASCADE ON UPDATE CASCADE;

-- 711
ALTER TABLE `testcaseexecution`  ADD CONSTRAINT `FK_testcaseexecution_02` FOREIGN KEY (`Application`) REFERENCES `application` (`Application`) ON DELETE CASCADE ON UPDATE CASCADE;

-- 712
ALTER TABLE `countryenvironmentparameters`  ADD CONSTRAINT `FK_countryenvironmentparameters_01` FOREIGN KEY (`system` , `Country` , `Environment`) REFERENCES `countryenvparam` (`system` , `Country` , `Environment`) ON DELETE CASCADE ON UPDATE CASCADE, ADD CONSTRAINT `FK_countryenvironmentparameters_02` FOREIGN KEY (`Application`) REFERENCES `application` (`Application`) ON DELETE CASCADE ON UPDATE CASCADE;

-- 713
ALTER TABLE `buildrevisionparameters`  ADD CONSTRAINT `FK_buildrevisionparameters_01` FOREIGN KEY (`Application`) REFERENCES `application` (`Application`) ON DELETE CASCADE ON UPDATE CASCADE;

-- 714
ALTER TABLE `testcaseexecutionqueue`  ADD CONSTRAINT `FK_testcaseexecutionqueue_01` FOREIGN KEY (`Test` , `TestCase` , `Country`) REFERENCES `testcasecountry` (`Test` , `TestCase` , `Country`) ON DELETE CASCADE ON UPDATE CASCADE;

-- 715
ALTER TABLE `testcasecountryproperties` ADD CONSTRAINT `FK_testcasecountryproperties_01` FOREIGN KEY (`Test` , `TestCase` , `Country`) REFERENCES `testcasecountry` (`Test` , `TestCase` , `Country`) ON DELETE CASCADE ON UPDATE CASCADE;

-- 716
ALTER TABLE `buildrevisionbatch` ADD CONSTRAINT `FK_buildrevisionbatch_02` FOREIGN KEY (`system` , `Country` , `Environment`) REFERENCES `countryenvparam` (`system` , `Country` , `Environment`) ON DELETE CASCADE ON UPDATE CASCADE;

-- 717
ALTER TABLE `countryenvdeploytype` ADD CONSTRAINT `FK_countryenvdeploytype_01` FOREIGN KEY (`system` , `Country` , `Environment`) REFERENCES `countryenvparam` (`system` , `Country` , `Environment`) ON DELETE CASCADE ON UPDATE CASCADE;

-- 718
ALTER TABLE `countryenvironmentdatabase` ADD CONSTRAINT `FK_countryenvironmentdatabase_01` FOREIGN KEY (`system` , `Country` , `Environment`) REFERENCES `countryenvparam` (`system` , `Country` , `Environment`) ON DELETE CASCADE ON UPDATE CASCADE;

-- 719
ALTER TABLE `host` ADD CONSTRAINT `FK_host_01` FOREIGN KEY (`system` , `Country` , `Environment`) REFERENCES `countryenvparam` (`system` , `Country` , `Environment`) ON DELETE CASCADE ON UPDATE CASCADE;

-- 720
ALTER TABLE `countryenvparam_log` ADD CONSTRAINT `FK_countryenvparam_log_01` FOREIGN KEY (`system` , `Country` , `Environment`) REFERENCES `countryenvparam` (`system` , `Country` , `Environment`) ON DELETE CASCADE ON UPDATE CASCADE;

-- 721
ALTER TABLE `logevent`  ADD INDEX `IX_logevent_01` (`Time` ASC);

-- 722
UPDATE `testcasecountryproperties`  SET type='getFromDataLib_BETA' where type='getFromDataLib';

-- 723
UPDATE `testdatalib`  SET Created = '2000-01-01 00:00:00' WHERE Created = '1970-01-01 01:01:01';

-- 724
SELECT 1 FROM dual;

-- 725
SELECT 1 FROM dual;

-- 726
SELECT 1 FROM dual;

-- 727
SELECT 1 FROM dual;

-- 728
ALTER TABLE `buildrevisionparameters` CHANGE COLUMN `Release` `Release` VARCHAR(200) NULL DEFAULT NULL ;

-- 729
ALTER TABLE `buildrevisionparameters` ADD COLUMN `repositoryurl` VARCHAR(1000) NULL DEFAULT '' AFTER `mavenversion`;

-- 730
INSERT INTO `documentation` (`DocTable`, `DocField`, `DocValue`, `Lang`, `DocLabel`, `DocDesc`) 
  VALUES ('buildrevisionparameters', 'repositoryUrl', '', 'en', 'Repository URL', 'This information corresponds to the URL where the current build of the <code class=\'doc-crbvvoca\'>application</code> can be downloaded.<br>It allow to retrieve it in a repository such as Nexus.')
  ,('buildrevisionparameters', 'repositoryUrl', '', 'fr', 'URL du Dépot', 'Cette information correspond à l\'URL d\'où le build de l\'<code class=\'doc-crbvvoca\'>application</code> peut-être téléchargé.<br>Cela permet de retrouver un build spécifique dans un dépot de livrable de type Nexus.');

-- 731
ALTER TABLE `buildrevisionbatch` DROP FOREIGN KEY `FK_buildrevisionbatch_01`;

-- 732
ALTER TABLE `testcasestepbatch` DROP FOREIGN KEY `FK_testcasestepbatch_02`;

-- 733
ALTER TABLE `batchinvariant` ADD COLUMN `system` VARCHAR(45) NOT NULL FIRST, DROP COLUMN `Unit`, DROP COLUMN `IncIni`, CHANGE COLUMN `Batch` `Batch` VARCHAR(100) NOT NULL DEFAULT '', CHANGE COLUMN `Description` `Description` VARCHAR(200) NULL DEFAULT NULL ;

-- 734
ALTER TABLE `buildrevisionbatch` CHANGE COLUMN `Batch` `Batch` VARCHAR(100) NOT NULL ;

-- 735
ALTER TABLE `buildrevisionbatch` ADD CONSTRAINT `FK_buildrevisionbatch_01` FOREIGN KEY (`Batch`) REFERENCES `batchinvariant` (`Batch`) ON DELETE CASCADE ON UPDATE CASCADE;

-- 736
ALTER TABLE `testcasestepbatch` CHANGE COLUMN `Batch` `Batch` VARCHAR(100) NOT NULL DEFAULT '' ;

-- 737
ALTER TABLE `testcasestepbatch` ADD CONSTRAINT `FK_testcasestepbatch_02` FOREIGN KEY (`Batch`) REFERENCES `batchinvariant` (`Batch`) ON DELETE CASCADE ON UPDATE CASCADE;

-- 738
insert into batchinvariant select value, concat(`value`,b.batch), b.description from batchinvariant b join invariant where idname='SYSTEM';

-- 739
SELECT 1 FROM dual;

-- 740
SELECT 1 FROM dual;

-- 741
SELECT 1 FROM dual;

-- 742
SELECT 1 FROM dual;

-- 743
SELECT 1 FROM dual;

-- 744
SELECT 1 FROM dual;

-- 745
DELETE FROM `invariant` WHERE `idname`='ACTION' and`value` in ('store','removeSelection','waitForPage');

-- 746
UPDATE testcasestepaction set Action='Unknown' where Action in ('store','removeSelection','waitForPage');

-- 747
SELECT 1 FROM dual;

-- 748
SELECT 1 FROM dual;

-- 749
INSERT INTO `parameter` (`system`, `param`, `value`, `description`)
  VALUES ('', 'cerberus_testdatalib_fetchmax', '100', 'Maximum number of fetched records that Cerberus will perform when retrieving a data from SQL Data Library.');

-- 750
SELECT 1 FROM dual;

-- 751
SELECT 1 FROM dual;

-- 752
DELETE FROM `invariant` WHERE `idname`='ACTION' and `value`='takeScreenshot';

-- 753
UPDATE testcasestepaction SET Action='skipAction' WHERE Action='takeScreenshot';

-- 754
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`)
  VALUES     ('ENVGP', 'DEV', '100', 'Development Environments', 'DEV'),    ('ENVGP', 'QA', '200', 'Quality Assurance Environments', 'QA'),    ('ENVGP', 'UAT', '300', 'User Acceptance Test Environments', 'UAT'),    ('ENVGP', 'PROD', '400', 'Production Environments', 'PROD'),    ('INVARIANTPRIVATE', 'ENVGP', '530', '', '');

-- 755
UPDATE testcasestepaction SET Action='doNothing' WHERE Action='skipAction';

-- 756
UPDATE `invariant` SET `value`='doNothing', `description`='doNothing' WHERE `idname`='ACTION' and`value`='skipAction';

-- 757
UPDATE `invariant` SET `value`='mouseLeftButtonPress', `sort`='37' WHERE `idname`='ACTION' and`value`='mouseDown';

-- 758
UPDATE `invariant` SET `value`='mouseLeftButtonRelease', `sort`='38', `description`='Selenium Action mouseUp' WHERE `idname`='ACTION' and`value`='mouseUp';

-- 759
UPDATE `invariant` SET `sort`='49' WHERE `idname`='ACTION' and`value`='keypress';

-- 760
UPDATE `invariant` SET `sort`='31' WHERE `idname`='ACTION' and`value`='clickAndWait';

-- 761
UPDATE `invariant` SET `sort`='35' WHERE `idname`='ACTION' and`value`='doubleClick';

-- 762
UPDATE `invariant` SET `sort`='55' WHERE `idname`='ACTION' and`value`='switchToWindow';

-- 763
UPDATE `invariant` SET `sort`='59' WHERE `idname`='ACTION' and`value`='manageDialog';

-- 764
UPDATE `invariant` SET sort=sort*10 where `idname` in ('ACTION', 'CONTROL');

-- 765
SELECT 1 FROM dual;

-- 766
SELECT 1 FROM dual;

-- 767
UPDATE `invariant` SET `value`='Y', `description`='Yes' WHERE `idname`='CHAIN' and`value`='0';

-- 768
UPDATE `invariant` SET `value`='N', `description`='No' WHERE `idname`='CHAIN' and`value`='1';

-- 769
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`)
  VALUES ('ACTION', 'hideKeyboard', '1200', 'hideKeyboard', '');

-- 770
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`)
  VALUES ('CONTROL', 'Unknown', '10', 'Unknown', '');

-- 771
SELECT 1 FROM dual;

-- 772
SELECT 1 FROM dual;

-- 773
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`)
  VALUES ('ACTION', 'swipe', '1300', 'Swipe mobile screen', '');

-- 774
SELECT 1 FROM dual;

-- 775
INSERT INTO `parameter` (`system`, `param`, `value`, `description`)
  VALUES ('', 'appium_swipeDuration', '2000', 'The duration for the Appium swipe action');

-- 776
INSERT INTO `parameter` (`system`, `param`, `value`, `description`)
  VALUES ('', 'cerberus_notinuse_timeout', '600', 'Integer that correspond to the number of seconds after which, any pending execution (status=PE) will not be considered as pending.');

-- 777
ALTER TABLE `testdatalib` DROP INDEX `IX_testdatalib_01` ,ADD INDEX `IX_testdatalib_01` (`Name` ASC, `system` ASC, `Environment` ASC, `Country` ASC) ;

-- 778
CREATE TABLE `robotcapability` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `robot` varchar(100) NOT NULL,
  `capability` varchar(45) NOT NULL,
  `value` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_capability_value_idx` (`capability`,`value`,`robot`),
  KEY `fk_robot_idx` (`robot`),
  CONSTRAINT `fk_robot` FOREIGN KEY (`robot`) REFERENCES `robot` (`robot`) ON DELETE CASCADE ON UPDATE CASCADE
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 779
INSERT INTO `robotcapability` (`robot`, `value`, `capability`)  
  SELECT `robot`, `platform`, 'platform' AS `capability` FROM `robot`
  UNION
  SELECT `robot`, `browser`, 'browser' AS `capability` FROM `robot`
  UNION
  SELECT `robot`, `version`, 'version' AS `capability` FROM `robot`;

-- 780
ALTER TABLE `robotcapability` 
  DROP FOREIGN KEY `fk_robot`;

-- 781
ALTER TABLE `robotcapability` 
  DROP INDEX `uq_capability_value_idx` ,
  ADD UNIQUE INDEX `IX_robotcapability_01` (`capability` ASC, `value` ASC, `robot` ASC),
  DROP INDEX `fk_robot_idx` ,
  ADD INDEX `IX_robotcapability_02` (`robot` ASC);

-- 782
ALTER TABLE `robotcapability` 
  ADD CONSTRAINT `FK_robotcapability_01`
  FOREIGN KEY (`robot`)
  REFERENCES `robot` (`robot`)
  ON DELETE CASCADE
  ON UPDATE CASCADE;

-- 783
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`)
  VALUES ('APPLITYPE', 'IPA', '50', 'IOS Application');

-- 784
DELETE FROM `robotcapability`;

-- 785
ALTER TABLE `testcaseexecution` CHANGE COLUMN `End` `End` TIMESTAMP NOT NULL DEFAULT '1970-01-01 01:01:01',CHANGE COLUMN `Start` `Start` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- 786
ALTER TABLE `testcasestepexecution` CHANGE COLUMN `End` `End` TIMESTAMP NOT NULL DEFAULT '1970-01-01 01:01:01',CHANGE COLUMN `Start` `Start` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- 787
ALTER TABLE `testdatalib` CHANGE COLUMN `LastModified` `LastModified` TIMESTAMP NOT NULL DEFAULT '1970-01-01 01:01:01';

-- 788
ALTER TABLE `test` CHANGE COLUMN `last_modified` `last_modified` TIMESTAMP NOT NULL DEFAULT '1970-01-01 01:01:01';

-- 789
ALTER TABLE `testcase` CHANGE COLUMN `last_modified` `last_modified` TIMESTAMP NOT NULL DEFAULT '1970-01-01 01:01:01';

-- 790
ALTER TABLE `testcasecountry` CHANGE COLUMN `last_modified` `last_modified` TIMESTAMP NOT NULL DEFAULT '1970-01-01 01:01:01';

-- 791
ALTER TABLE `testcasecountryproperties` CHANGE COLUMN `last_modified` `last_modified` TIMESTAMP NOT NULL DEFAULT '1970-01-01 01:01:01';

-- 792
ALTER TABLE `testcasestep` CHANGE COLUMN `last_modified` `last_modified` TIMESTAMP NOT NULL DEFAULT '1970-01-01 01:01:01';

-- 793
ALTER TABLE `testcasestepaction` CHANGE COLUMN `last_modified` `last_modified` TIMESTAMP NOT NULL DEFAULT '1970-01-01 01:01:01';

-- 794
ALTER TABLE `testcasestepactioncontrol` CHANGE COLUMN `last_modified` `last_modified` TIMESTAMP NOT NULL DEFAULT '1970-01-01 01:01:01';

-- 795
ALTER TABLE `testcasestepexecution` ADD COLUMN `Description` VARCHAR(150) NOT NULL DEFAULT '' AFTER `ReturnMessage`;

-- 796
ALTER TABLE `testcasestepactionexecution` ADD COLUMN `Description` VARCHAR(255) NOT NULL DEFAULT '' AFTER `PageSourceFileName`;

-- 797
ALTER TABLE `testcasestepactioncontrolexecution`  ADD COLUMN `Description` VARCHAR(255) NOT NULL DEFAULT '' AFTER `PageSourceFilename`;

-- 798
UPDATE `testdatalib` SET `LastModified` =  '1970-01-01 01:01:01' WHERE `LastModified` = '1970-01-01 01:01:01';

-- 799
UPDATE `test` SET `last_modified` =  '1970-01-01 01:01:01' WHERE `last_modified` = '1970-01-01 01:01:01';

-- 800
UPDATE `testcase` SET `last_modified` =  '1970-01-01 01:01:01' WHERE `last_modified` = '1970-01-01 01:01:01';

-- 801
UPDATE `testcasecountry` SET `last_modified` =  '1970-01-01 01:01:01' WHERE `last_modified` = '1970-01-01 01:01:01';

-- 802
UPDATE `testcasecountryproperties` SET `last_modified` =  '1970-01-01 01:01:01' WHERE `last_modified` = '1970-01-01 01:01:01';

-- 803
UPDATE `testcasestep` SET `last_modified` =  '1970-01-01 01:01:01' WHERE `last_modified` = '1970-01-01 01:01:01';

-- 804
UPDATE `testcasestepaction` SET `last_modified` =  '1970-01-01 01:01:01' WHERE `last_modified` = '1970-01-01 01:01:01';

-- 805
UPDATE `testcasestepactioncontrol` SET `last_modified` =  '1970-01-01 01:01:01' WHERE `last_modified` = '1970-01-01 01:01:01';

-- 806
UPDATE `testcase` SET `TCDateCrea` =  '1970-01-01 01:01:01' WHERE `TCDateCrea` = '1970-01-01 01:01:01';

-- 807
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`)
  VALUES ('INVARIANTPUBLIC', 'CAPABILITY', '500', 'Robot capabilities', ''), ('CAPABILITY', 'automationName', '1', 'Automation name, e.g.: Appium)', ''), ('CAPABILITY', 'deviceName', '2', 'Device name (useful for Appium)', ''), ('CAPABILITY', 'app', '3', 'Application name (useful for Appium)', ''), ('CAPABILITY', 'platformName', '4', 'Platform name (useful for Appium)', ''), ('CAPABILITY', 'platformVersion', '5', 'Platform version (useful for Appium)', ''), ('CAPABILITY', 'browserName', '6', 'Browser name (useful for Appium)', ''), ('CAPABILITY', 'autoWebview', '7', 'If auto web view has to be enabled (useful for Appium, e.g.: true) ', '');

-- 808
SELECT 1 FROM dual;

-- 809
UPDATE testcasecountryproperties SET value2 = concat(value2, '/text()') WHERE `type` = 'getFromXML' and value2 not like '%ext()';

-- 810
ALTER TABLE `testcaseexecution`  ADD INDEX `IX_testcaseexecution_09` (`Country` ASC, `Environment` ASC, `ControlStatus` ASC),  ADD INDEX `IX_testcaseexecution_10` (`Test` ASC, `TestCase` ASC, `Environment` ASC, `Country` ASC, `Build` ASC) ;

-- 811
ALTER TABLE `countryenvironmentdatabase` ADD COLUMN `SoapUrl` VARCHAR(200) NOT NULL DEFAULT ''  AFTER `ConnectionPoolName`;

-- 812
ALTER TABLE `testdatalib` ADD COLUMN `DatabaseUrl` VARCHAR(45) NOT NULL DEFAULT '' AFTER `Script`;

-- 813
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`)
  VALUES ('ACTION', 'skipAction', '2600', 'skipAction');

-- 814
INSERT INTO `parameter` (`system`, `param`, `value`, `description`)
  VALUES ('', 'cerberus_notification_forgotpassword_subject', '[Cerberus] Reset your password', 'Subject of Cerberus forgot password notification email.'), ('', 'cerberus_notification_forgotpassword_body', 'Hello %NAME%<br><br>We\'ve received a request to reset your Cerberus password.<br><br>%LINK%<br><br>If you didn\'t request a password reset, not to worry, just ignore this email and your current password will continue to work.<br><br>Cheers,<br>The Cerberus Team', 'Cerberus forgot password notification email body. %LOGIN%, %NAME% and %LINK% can be used as variables.');

-- 815
ALTER TABLE `user` ADD COLUMN `ResetPasswordToken` CHAR(40) NOT NULL DEFAULT '' AFTER `Password`;

-- 816
ALTER TABLE `testcasestep` ADD COLUMN `Sort` INT(10) UNSIGNED AFTER `Step`;

-- 817
UPDATE `testcasestep` SET `Sort` = `Step`;

-- 818
ALTER TABLE `testcasestepaction` ADD COLUMN `Sort` INT(10) UNSIGNED AFTER `Sequence`;

-- 819
UPDATE `testcasestepaction` SET `Sort` = `Sequence`;

-- 820
ALTER TABLE `testcasestepactioncontrol` ADD COLUMN `Sort` INT(10) UNSIGNED AFTER `Control`;

-- 821
UPDATE `testcasestepactioncontrol` SET `Sort` = `Control`;

-- 822
ALTER TABLE `testcasestepactioncontrolexecution` ADD COLUMN `Sort` INT(10) UNSIGNED AFTER `Control`;

-- 823
UPDATE `testcasestepactioncontrolexecution` SET `Sort` = `Control`;

-- 824
ALTER TABLE `testcasestepactionexecution` ADD COLUMN `Sort` INT(10) UNSIGNED AFTER `Sequence`;

-- 825
UPDATE `testcasestepactionexecution` SET `Sort` = `Sequence`;

-- 826
ALTER TABLE `testcasestepexecution` ADD COLUMN `Sort` INT(10) UNSIGNED AFTER `Step`;

-- 827
UPDATE `testcasestepexecution` SET `Sort` = `Step`;

-- 828
DELETE from invariant where idname='ACTION' and value in ('callSoapWithBase_BETA','callSoap_BETA');

-- 829
ALTER TABLE `testcasestepaction` ADD COLUMN `ForceExeStatus` VARCHAR(45) NOT NULL DEFAULT '' AFTER `Property`;

-- 830
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`)
  VALUES  ('ACTIONFORCEEXESTATUS', '', '10', 'Standard behaviour.', 'Std Behaviour'), ('ACTIONFORCEEXESTATUS', 'PE', '20', 'Force the Execution to continue running not impacting the final status whatever the result of the action is.', 'Continue'), ('INVARIANTPRIVATE', 'ACTIONFORCEEXESTATUS', '540', '', '');

-- 831
ALTER TABLE `testcasestepactionexecution` ADD COLUMN `ForceExeStatus` VARCHAR(45) NOT NULL DEFAULT '' AFTER `Property`;

-- 832
SELECT 1 FROM dual;

-- 833
SELECT 1 FROM dual;

-- 834
INSERT INTO `parameter` (`system`, `param`, `value`, `description`)
  VALUES ('', 'cerberus_automaticexecution_enable', 'Y', 'Activation boolean in order to activate the automatic executions.Y value will allow execution. Any other value will stop the execution returning an error message..');

-- 835
UPDATE `parameter` SET `description`='URL to Cerberus reporting screen. the following variables can be used : %COUNTRY%, %ENV%,  %APPLI%, %BUILD% and %REV%.' WHERE `system`='' and`param`='cerberus_reporting_url';

-- 836
ALTER TABLE `testcasestepactioncontrol` CHANGE COLUMN `ControlValue` `ControlValue` VARCHAR(2500) NOT NULL DEFAULT ''  ,CHANGE COLUMN `ControlProperty` `ControlProperty` VARCHAR(2500) NULL DEFAULT NULL ;

-- 837
ALTER TABLE `testcasestepactioncontrolexecution` CHANGE COLUMN `ControlValue` `ControlValue` VARCHAR(2500) NULL DEFAULT NULL ;

-- 838
ALTER TABLE `testcasestepaction` CHANGE COLUMN `Object` `Object` VARCHAR(2500) NOT NULL DEFAULT ''  ,CHANGE COLUMN `Property` `Property` VARCHAR(2500) NULL DEFAULT NULL ;

-- 839
ALTER TABLE `testcasestepactionexecution` CHANGE COLUMN `Object` `Object` VARCHAR(2500) NULL DEFAULT NULL  ,CHANGE COLUMN `Property` `Property` VARCHAR(2500) NULL DEFAULT NULL ;

-- 840
ALTER TABLE `user` ADD COLUMN `UserPreferences` TEXT NOT NULL AFTER `Email`;

-- 841
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`)
  VALUES ('PROPERTYTYPE', 'getFromGroovy', '80', 'Getting value from a Groovy script', '');

-- 842
SELECT 1 FROM dual;

-- 843
ALTER TABLE `testcasecountryproperties` ADD COLUMN `Description` VARCHAR(255) NULL DEFAULT '' AFTER `Property`;

-- 844
SELECT 1 FROM dual;

-- 845
ALTER TABLE `testcaseexecutiondata` ADD COLUMN `Description` VARCHAR(255) NULL DEFAULT '' AFTER `Property`;

-- 846
SELECT 1 FROM dual;

-- 847
UPDATE countryenvironmentparameters SET URLLOGIN = '' WHERE URLLOGIN is null;

-- 848
ALTER TABLE `countryenvironmentparameters` CHANGE COLUMN `URLLOGIN` `URLLOGIN` VARCHAR(150) NOT NULL DEFAULT '' ;

-- 849
ALTER TABLE `testdatalib` ADD COLUMN `CsvUrl` VARCHAR(250) NOT NULL DEFAULT '' AFTER `Envelope`;

-- 850
ALTER TABLE `testdatalibdata` ADD COLUMN `ColumnPosition` VARCHAR(45) NOT NULL DEFAULT '' AFTER `ParsingAnswer`;

-- 851
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`)
  VALUES ('TESTDATATYPE', 'CSV', '40', 'Dynamic test data from CSV file');

-- 852
ALTER TABLE `testdatalib` ADD COLUMN `Separator` VARCHAR(45) NOT NULL DEFAULT '' AFTER `CsvUrl`;

-- 853
SELECT 1 FROM dual;

-- 854
CREATE TABLE `label` (`Id` INT NOT NULL AUTO_INCREMENT,`System` VARCHAR(45) NOT NULL DEFAULT '',`Label` VARCHAR(100) NOT NULL DEFAULT '',`Color` VARCHAR(45) NOT NULL DEFAULT '',`ParentLabel` VARCHAR(100) NOT NULL DEFAULT '',`Description` VARCHAR(250) NOT NULL DEFAULT '',`UsrCreated` VARCHAR(45) NOT NULL DEFAULT '',`DateCreated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,`UsrModif` VARCHAR(45) NOT NULL DEFAULT '',`DateModif` TIMESTAMP NOT NULL DEFAULT '1970-01-01 01:01:01',  PRIMARY KEY (`id`),  UNIQUE INDEX `IX_label_01` (`system` ASC, `label` ASC))
  ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 855
CREATE TABLE `testcaselabel` (`Id` INT NOT NULL AUTO_INCREMENT,`Test` varchar(45) NOT NULL,`TestCase` varchar(45) NOT NULL,`LabelId` INT NOT NULL,`UsrCreated` VARCHAR(45) NOT NULL DEFAULT '',`DateCreated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,`UsrModif` VARCHAR(45) NOT NULL DEFAULT '',`DateModif` TIMESTAMP NOT NULL DEFAULT '1970-01-01 01:01:01',  PRIMARY KEY (`Id`), UNIQUE KEY `IX_testcaselabel_03` (`Test`,`TestCase`,`LabelId`), KEY `IX_testcaselabel_01` (`Test`,`TestCase`), KEY `IX_testcaselabel_02` (`LabelId`), CONSTRAINT `FK_testcaselabel_01` FOREIGN KEY (`Test`, `TestCase`) REFERENCES `testcase` (`Test`, `TestCase`) ON DELETE CASCADE ON UPDATE CASCADE, CONSTRAINT `FK_testcaselabel_02` FOREIGN KEY (`LabelId`) REFERENCES `label` (`Id`) ON DELETE CASCADE ON UPDATE CASCADE) 
  ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 856
SELECT 1 FROM dual;

-- 857
INSERT INTO label (`system`,`label`, `color`,`UsrCreated`, `UsrModif`) SELECT `value` , 'MyFirstLabel', '#000000' , 'admin' , 'admin' from invariant where idname = 'SYSTEM';

-- 858
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`)
  VALUES ('ACTION', 'rightClick', '310', 'Right click on an element', 'Right click');

-- 859
SELECT 1 FROM dual;

-- 860
SELECT 1 FROM dual;

-- 861
INSERT INTO `parameter` (`system`, `param`, `value`, `description`)
  VALUES  ('', 'cerberus_propertyexternalsql_timeout', '60', 'Integer that correspond to the number of seconds after which, any SQL executed from a property calculation will fail.')
  ,('', 'cerberus_actionexecutesqlupdate_timeout', '60', 'Integer that correspond to the number of seconds after which, any SQL triggered from action executeSqlUpdate will fail.');

-- 862
ALTER TABLE `testcaseexecutiondata` ADD COLUMN `Index` INT NOT NULL DEFAULT 1 AFTER `Property`,DROP PRIMARY KEY, ADD PRIMARY KEY (`ID`, `Property`, `Index`) ;

-- 863
ALTER TABLE `countryenvironmentparameters` ADD COLUMN `Var1` VARCHAR(200) NOT NULL DEFAULT '' AFTER `URLLOGIN`,ADD COLUMN `Var2` VARCHAR(200) NOT NULL DEFAULT '' AFTER `Var1`,ADD COLUMN `Var3` VARCHAR(200) NOT NULL DEFAULT '' AFTER `Var2`,ADD COLUMN `Var4` VARCHAR(200) NOT NULL DEFAULT '' AFTER `Var3`;

-- 864
UPDATE testcasecountryproperties SET `Type` = 'getFromDataLib' where `Type` = 'getFromDataLib_BETA';

-- 865
SELECT 1 FROM dual;

-- 866
SELECT 1 FROM dual;

-- 867
UPDATE `invariant` SET `value`='getFromDataLib' WHERE `idname`='PROPERTYTYPE' and `value`='getFromDataLib_BETA';

-- 868
ALTER TABLE `countryenvironmentdatabase` ADD COLUMN `CsvUrl` VARCHAR(200) NOT NULL DEFAULT '' AFTER `SoapUrl`;

-- 869
ALTER TABLE `testdatalib` ADD COLUMN `DatabaseCsv` VARCHAR(45) NOT NULL DEFAULT '' AFTER `Envelope`;

-- 870
UPDATE `testdatalib` SET Type='INTERNAL' WHERE `Type`='STATIC';

-- 871
UPDATE `invariant` SET `value`='INTERNAL', `description`='Internal Cerberus test data.' WHERE `idname`='TESTDATATYPE' and`value`='STATIC';

-- 872
CREATE TABLE `testcaseexecutionfile` ( `ID` BIGINT(20) NOT NULL AUTO_INCREMENT , `ExeID` BIGINT(20) unsigned NOT NULL , `Level` VARCHAR(150) NOT NULL DEFAULT '' , `FileDesc` VARCHAR(100) NOT NULL DEFAULT '' , `Filename` VARCHAR(150) NOT NULL DEFAULT '' , `FileType` VARCHAR(45) NOT NULL DEFAULT '' , `UsrCreated` VARCHAR(45) NOT NULL DEFAULT '', `DateCreated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, `UsrModif` VARCHAR(45) NOT NULL DEFAULT '', `DateModif` TIMESTAMP NOT NULL DEFAULT '1970-01-01 01:01:01',  PRIMARY KEY (`ID`) , UNIQUE INDEX `IX_testcaseexecutionfile_01` (`ExeID` ASC, `Level` ASC, `FileDesc` ASC) , CONSTRAINT `FK_testcaseexecutionfile_01` FOREIGN KEY (`ExeID`) REFERENCES `testcaseexecution` (`ID`) ON DELETE CASCADE ON UPDATE CASCADE)
  ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 873
UPDATE `parameter` SET `param`='cerberus_mediastorage_path', `description`='Path to store the Cerberus Media files (like Selenium Screenshot or SOAP requests and responses).' WHERE `param`='cerberus_picture_path';

-- 874
UPDATE `parameter` SET `param`='cerberus_mediastorage_url', `description`='Link (URL) to the Cerberus Media Files. That link should point to cerberus_mediastorage_path location.' WHERE `system`='' and`param`='cerberus_picture_url';

-- 875
INSERT into testcaseexecutionfile (`exeid`, `level`, `FileDesc`, `Filename`, `FileType`, `UsrCreated`)select ID ExeID, concat(test,"-", testcase,"-", Step,"-", Sequence) level, 'Screenshot' FileDesc, replace(ScreenshotFilename, '\\', '/') Filename ,ucase(right(ScreenshotFilename, 3)) FileType, 'RecoverSQL' UsrCreated from testcasestepactionexecution where ScreenshotFilename is not null and TO_DAYS(NOW()) - TO_DAYS(Start) <= 10;

-- 876
INSERT into testcaseexecutionfile (`exeid`, `level`, `FileDesc`, `Filename`, `FileType`, `UsrCreated`)select ID ExeID, concat(test,"-", testcase,"-", Step,"-", Sequence) level, 'PageSource' FileDesc, replace(PageSourceFileName, '\\', '/') Filename ,ucase(right(PageSourceFileName, 3)) FileType, 'RecoverSQL' UsrCreated from testcasestepactionexecution where PageSourceFileName is not null and TO_DAYS(NOW()) - TO_DAYS(Start) <= 10;

-- 877
INSERT into testcaseexecutionfile (`exeid`, `level`, `FileDesc`, `Filename`, `FileType`, `UsrCreated`)select ID ExeID, concat(test,"-", testcase,"-", Step,"-", Sequence,"-", Control) level, 'Screenshot' FileDesc, replace(ScreenshotFilename, '\\', '/') Filename ,ucase(right(ScreenshotFilename, 3)) FileType, 'RecoverSQL' UsrCreated from testcasestepactioncontrolexecution where ScreenshotFilename is not null and TO_DAYS(NOW()) - TO_DAYS(Start) <= 10;

-- 878
INSERT into testcaseexecutionfile (`exeid`, `level`, `FileDesc`, `Filename`, `FileType`, `UsrCreated`)select ID ExeID, concat(test,"-", testcase,"-", Step,"-", Sequence,"-", Control) level, 'PageSource' FileDesc, replace(PageSourceFileName, '\\', '/') Filename ,ucase(right(PageSourceFileName, 3)) FileType, 'RecoverSQL' UsrCreated from testcasestepactioncontrolexecution where PageSourceFileName is not null and TO_DAYS(NOW()) - TO_DAYS(Start) <= 10;

-- 879
INSERT INTO `parameter` (`system`, `param`, `value`, `description`)
  VALUES  ('', 'cerberus_actionexecutesqlstoredprocedure_timeout', '60', 'Integer that correspond to the number of seconds after which, any SQL triggered from action executeSqlStoredProcedure will fail.');

-- 880
ALTER TABLE `testcasestepactioncontrolexecution` DROP COLUMN `PageSourceFilename`, DROP COLUMN `ScreenshotFilename`;

-- 881
ALTER TABLE `testcasestepactionexecution` DROP COLUMN `PageSourceFileName`, DROP COLUMN `ScreenshotFilename`;

-- 882
SELECT 1 FROM dual;

-- 883
UPDATE `invariant` SET `sort`='10' WHERE `idname`='ACTION' and`value`='Unknown';

-- 884
UPDATE `invariant` SET `sort`='25010' WHERE `idname`='ACTION' and`value`='skipAction';

-- 885
UPDATE `invariant` SET `sort`='24900' WHERE `idname`='ACTION' and`value`='calculateProperty';

-- 886
UPDATE `invariant` SET `sort`='99999', `description`='[DEPRECATED] getPageSource' WHERE `idname`='ACTION' and`value`='getPageSource';

-- 887
UPDATE `invariant` SET `sort`='3900' WHERE `idname`='ACTION' and`value`='rightClick';

-- 888
UPDATE `invariant` SET `sort`='3850' WHERE `idname`='ACTION' and`value`='doubleClick';

-- 889
UPDATE `invariant` SET `sort`='1000' WHERE `idname`='ACTION' and`value`='keypress';

-- 890
UPDATE `invariant` SET `sort`='5400' WHERE `idname`='ACTION' and`value`='switchToWindow';

-- 891
UPDATE `invariant` SET `sort`='5500' WHERE `idname`='ACTION' and`value`='manageDialog';

-- 892
DELETE FROM `invariant` WHERE `idname`='ACTION' and `value` in ('clickAndWait','enter','selectAndWait');

-- 893
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`)
  VALUES ('CONTROL', 'getPageSource', '10100', 'Save the Page Source.', '');

-- 894
UPDATE `invariant` SET `sort`='1500' WHERE `idname`='CONTROL' and`value`='verifyIntegerEquals';

-- 895
UPDATE `invariant` SET `sort`='1550' WHERE `idname`='CONTROL' and`value`='verifyIntegerDifferent';

-- 896
UPDATE `invariant` SET `sort`='3250' WHERE `idname`='CONTROL' and`value`='verifyElementDifferent';

-- 897
UPDATE `invariant` SET `sort`='3350' WHERE `idname`='CONTROL' and`value`='verifyElementInElement';

-- 898
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`)
  VALUES ('CONTROL', 'skipControl', '15000', 'Skip the control.', '');

-- 899
UPDATE `invariant` SET `sort`='5' WHERE `idname`='PROPERTYTYPE' and`value`='text';

-- 900
UPDATE `invariant` SET `description`='Determines the data value associated with a library entry' WHERE `idname`='PROPERTYTYPE' and`value`='getFromDataLib';

-- 901
UPDATE `invariant` SET `description`='[DEPRECATED] Getting from the SOAP request using the query' WHERE `idname`='PROPERTYTYPE' and`value`='executeSoapFromLib';

-- 902
UPDATE `invariant` SET `description`='[DEPRECATED] Using an SQL from the library' WHERE `idname`='PROPERTYTYPE' and`value`='executeSqlFromLib';

-- 903
UPDATE `invariant` SET `description`='[DEPRECATED] Getting from the test Data library using the Key' WHERE `idname`='PROPERTYTYPE' and`value`='getFromTestData';

-- 904
UPDATE `invariant` SET `sort`='9999' WHERE `idname`='PROPERTYTYPE' and`value`='getFromTestData';

-- 905
UPDATE `invariant` SET `sort`='9999' WHERE `idname`='PROPERTYTYPE' and`value`='executeSqlFromLib';

-- 906
UPDATE `invariant` SET `sort`='9999' WHERE `idname`='PROPERTYTYPE' and`value`='executeSoapFromLib';

-- 907
UPDATE `invariant` SET `sort`='10' WHERE `idname`='PROPERTYTYPE' and`value`='getFromDataLib';

-- 908
UPDATE `invariant` SET `sort`='40' WHERE `idname`='PROPERTYTYPE' and`value`='getFromCookie';

-- 909
SELECT 1 FROM dual;

-- 910
SELECT 1 FROM dual;

-- 911
SELECT 1 FROM dual;

-- 912
SELECT 1 FROM dual;

-- 913
ALTER TABLE `testcasestepaction` ADD COLUMN `ConditionOper` VARCHAR(45) NOT NULL DEFAULT '' AFTER `Sort`,CHANGE COLUMN `Property` `ConditionVal1` VARCHAR(2500) NULL DEFAULT '' AFTER `ConditionOper`,CHANGE COLUMN `Object` `Value1` VARCHAR(2500) NOT NULL DEFAULT '' ,ADD COLUMN `Value2` VARCHAR(2500) NOT NULL DEFAULT '' AFTER `Value1`;

-- 914
ALTER TABLE `testcasestepactionexecution` CHANGE COLUMN `Description` `Description` VARCHAR(255) NOT NULL DEFAULT '' AFTER `ForceExeStatus`,CHANGE COLUMN `ReturnCode` `ReturnCode` VARCHAR(2) NULL DEFAULT NULL AFTER `Description`,CHANGE COLUMN `ReturnMessage` `ReturnMessage` VARCHAR(500) NULL DEFAULT NULL AFTER `ReturnCode`,CHANGE COLUMN `Object` `Value1` VARCHAR(2500) NULL DEFAULT '' ,CHANGE COLUMN `Property` `Value2` VARCHAR(2500) NULL DEFAULT '' ,ADD COLUMN `ConditionOper` VARCHAR(45) AFTER `Sort`,ADD COLUMN `ConditionVal1` VARCHAR(2500) AFTER `ConditionOper`,ADD COLUMN `Value1Init` VARCHAR(2500) NULL DEFAULT '' AFTER `Action`,ADD COLUMN `Value2Init` VARCHAR(2500) NULL DEFAULT '' AFTER `Value1Init`;

-- 915
UPDATE testcasestepaction SET ConditionOper = 'ifPropertyExist' where ConditionVal1<>'';

-- 916
UPDATE testcasestepaction SET ConditionOper = 'always' where ConditionOper='';

-- 917
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`)
  VALUES  ('ACTIONCONDITIONOPER', 'always', '100', 'Always.', ''), ('ACTIONCONDITIONOPER', 'ifPropertyExist', '200', 'Only execute if property exist for the execution.', ''), ('ACTIONCONDITIONOPER', 'never', '9999', 'Never execute the action.', ''), ('INVARIANTPRIVATE', 'ACTIONCONDITIONOPER', '550', '', '');

-- 918
UPDATE testcasestepaction SET Value2 = concat('%', ConditionVal1, '%') where ConditionVal1<>'' and action not in ('calculateProperty');

-- 919
UPDATE testcasestepaction SET Value1 = ConditionVal1, Value2='' where action in ('calculateProperty');

-- 920
select 1 from DUAL;

-- 921
select 1 from DUAL;

-- 922
select 1 from DUAL;

-- 923
UPDATE testcasestepaction SET ConditionVal1 = left(ConditionVal1,locate('(',ConditionVal1)-1) WHERE conditionval1 like '%(%';

-- 924
select 1 from DUAL;

-- 925
select 1 from DUAL;

-- 926
select 1 from DUAL;

-- 927
ALTER TABLE `testcase` DROP COLUMN `ChainNumberNeeded`,DROP COLUMN `ReadOnly`,ADD COLUMN `useragent` VARCHAR(250) NULL DEFAULT '' AFTER `function`,CHANGE COLUMN `Creator` `UsrCreated` VARCHAR(45) NOT NULL DEFAULT '' AFTER `useragent`,CHANGE COLUMN `TCDateCrea` `DateCreated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER `UsrCreated`,CHANGE COLUMN `LastModifier` `UsrModif` VARCHAR(45) NULL DEFAULT '' AFTER `DateCreated`,CHANGE COLUMN `last_modified` `DateModif` TIMESTAMP NOT NULL DEFAULT '1970-01-01 01:01:01' ;

-- 928
select 1 from DUAL;

-- 929
select 1 from DUAL;

-- 930
DELETE FROM `invariant` WHERE `idname`='GROUP' and`value` in ('COMPARATIVE','PROCESS');

-- 931
UPDATE testcase SET `group`='MANUAL' WHERE `group` in ('COMPARATIVE', 'PROCESS');

-- 932
select 1 from DUAL;

-- 933
select 1 from DUAL;

-- 934
select 1 from DUAL;

-- 935
UPDATE testcase set `BehaviorOrValueExpected` = coalesce(`BehaviorOrValueExpected`, ''), `howto` = coalesce(`howto`, ''), `Group` = coalesce(`Group`,''),`Origine` = coalesce(`Origine`,''),`RefOrigine` = coalesce(`RefOrigine`,''),`Comment` = coalesce(`Comment`,''),`FromBuild` = coalesce(`FromBuild`,''),`FromRev` = coalesce(`FromRev`,''),`ToBuild` = coalesce(`ToBuild`,''),`ToRev` = coalesce(`ToRev`,''),`BugID` = coalesce(`BugID`,''),`TargetBuild` = coalesce(`TargetBuild`,''),`TargetRev` = coalesce(`TargetRev`,''),`Implementer` = coalesce(`Implementer`,'')where `BehaviorOrValueExpected` is null or `howto` is null or `Group` is null or `Origine` is null or `RefOrigine` is null or `Comment` is null or `FromBuild` is null or `FromRev` is null or `ToBuild` is null or `ToRev` is null or `BugID` is null or `TargetBuild` is null or `TargetRev` is null or `Implementer` is null;

-- 936
ALTER TABLE `testcase` CHANGE COLUMN `BehaviorOrValueExpected` `BehaviorOrValueExpected` TEXT NOT NULL ,CHANGE COLUMN `Group` `Group` VARCHAR(45) NOT NULL DEFAULT ''  ,CHANGE COLUMN `Origine` `Origine` VARCHAR(45) NOT NULL DEFAULT ''  ,CHANGE COLUMN `RefOrigine` `RefOrigine` VARCHAR(45) NOT NULL DEFAULT ''  ,CHANGE COLUMN `HowTo` `HowTo` TEXT NOT NULL  ,CHANGE COLUMN `Comment` `Comment` VARCHAR(500) NOT NULL DEFAULT ''  ,CHANGE COLUMN `FromBuild` `FromBuild` VARCHAR(10) NOT NULL DEFAULT ''  ,CHANGE COLUMN `FromRev` `FromRev` VARCHAR(20) NOT NULL DEFAULT ''  ,CHANGE COLUMN `ToBuild` `ToBuild` VARCHAR(10) NOT NULL DEFAULT ''  ,CHANGE COLUMN `ToRev` `ToRev` VARCHAR(20) NOT NULL DEFAULT ''  ,CHANGE COLUMN `BugID` `BugID` VARCHAR(10) NOT NULL DEFAULT ''  ,CHANGE COLUMN `TargetBuild` `TargetBuild` VARCHAR(10) NOT NULL DEFAULT ''  ,CHANGE COLUMN `TargetRev` `TargetRev` VARCHAR(20) NOT NULL DEFAULT ''  ,CHANGE COLUMN `Implementer` `Implementer` VARCHAR(45) NOT NULL DEFAULT ''  ;

-- 937
INSERT INTO `parameter` (`system`, `param`, `value`, `description`)
  VALUES ('', 'cerberus_property_maxretry', '50', 'Integer that correspond to the maximum number of retry allowed when calculating a property.');

-- 938
INSERT INTO `parameter` (`system`, `param`, `value`, `description`)
  VALUES ('', 'cerberus_property_maxretrytotalduration', '1800000', 'Integer (in ms) that correspond to the maximum duration of the property calculation. In case the period is greated than this parameter, the period value will be replaced by this parameter with 1 single retry. If number of retries x period is greated than this parameter, the number of retry will be reduced to fit the constrain.');

-- 939
select 1 from DUAL;

-- 940
ALTER TABLE `testcasecountryproperties` ADD COLUMN `RetryNb` INT(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '' AFTER `Nature`,ADD COLUMN `RetryPeriod` INT(10) UNSIGNED NOT NULL DEFAULT 10000 COMMENT '' AFTER `RetryNb`;

-- 941
select 1 from DUAL;

-- 942
select 1 from DUAL;

-- 943
ALTER TABLE `testdatalib` CHANGE COLUMN `Script` `Script` TEXT NOT NULL ;

-- 944
select 1 from DUAL;

-- 945
INSERT INTO `parameter` (`system`, `param`, `value`, `description`)
  VALUES ('', 'cerberus_selenium_pageLoadTimeout', '45000', 'Integer that correspond to the number of milliseconds that selenium will wait before give timeout, when loading a page.')
  ,('', 'cerberus_selenium_implicitlyWait', '0', 'Integer that correspond to the number of milliseconds that selenium will implicitely wait when searching an element.')
  ,('', 'cerberus_selenium_setScriptTimeout', '45000', 'Integer that correspond to the number of milliseconds that selenium will wait before give timeout, when executing a Javascript Script.')
  ,('', 'cerberus_action_wait_default', '45000', 'Integer that correspond to the number of milliseconds that cerberus will wait by default using the wait action.')
  ,('', 'cerberus_selenium_wait_element', '45000', 'Integer that correspond to the number of milliseconds that selenium will wait before give timeout, when searching an element.')
  ,('', 'cerberus_appium_wait_element', '45000', 'Integer that correspond to the number of milliseconds that appium will wait before give timeout, when searching an element.');

-- 946
UPDATE parameter p2 set `value` = (select * from (select `value` * 1000 from parameter p1 where p1.`param` = 'selenium_defaultWait' and p1.`system` = '') p3 ) where p2.`param` in ('cerberus_selenium_wait_element', 'cerberus_selenium_setScriptTimeout', 'cerberus_selenium_pageLoadTimeout','cerberus_appium_wait_element' , 'cerberus_action_wait_default');

-- 947
DELETE FROM parameter where `param` = 'selenium_defaultWait';

-- 948
ALTER TABLE `testcaseexecutiondata` CHANGE COLUMN `Type` `Type` VARCHAR(45) NULL DEFAULT NULL AFTER `Index`,CHANGE COLUMN `RC` `RC` VARCHAR(10) NULL DEFAULT NULL AFTER `EndLong`,CHANGE COLUMN `RMessage` `RMessage` TEXT NULL AFTER `RC`,CHANGE COLUMN `Description` `Description` VARCHAR(255) NULL DEFAULT '' AFTER `RMessage`,CHANGE COLUMN `Value` `Value` TEXT NOT NULL ,CHANGE COLUMN `Value1` `Value1` TEXT NULL ,CHANGE COLUMN `Value2` `Value2` TEXT NULL ,ADD COLUMN `Database` VARCHAR(45) NULL AFTER `Value`,ADD COLUMN `Value1Init` TEXT NULL AFTER `Database`,ADD COLUMN `Value2Init` TEXT NULL AFTER `Value1Init`,ADD COLUMN `Length` INT(10) NULL AFTER `Value2`,ADD COLUMN `RowLimit` INT(10) NULL AFTER `Length`,ADD COLUMN `Nature` VARCHAR(45) NULL AFTER `RowLimit`,ADD COLUMN `RetryNb` INT(10) NULL AFTER `Nature`,ADD COLUMN `RetryPeriod` INT(10) NULL AFTER `RetryNb`;

-- 949
ALTER TABLE `testcasestepactioncontrol` CHANGE COLUMN `Control` `ControlSequence` INT(10) UNSIGNED NOT NULL ,CHANGE COLUMN `Type` `Control` VARCHAR(200) NOT NULL DEFAULT '' ,CHANGE COLUMN `ControlProperty` `Value1` TEXT NULL AFTER `Control`,CHANGE COLUMN `ControlValue` `Value2` TEXT NULL  AFTER `Value1`,CHANGE COLUMN `ControlDescription` `Description` VARCHAR(255) NOT NULL DEFAULT '' ,CHANGE COLUMN `Fatal` `Fatal` VARCHAR(1) NULL DEFAULT 'Y' AFTER `Value2`,DROP PRIMARY KEY, ADD PRIMARY KEY USING BTREE (`Test`, `TestCase`, `Step`, `Sequence`, `ControlSequence`) ;

-- 950
ALTER TABLE `testcasestepactioncontrolexecution` CHANGE COLUMN `Control` `ControlSequence` INT(10) UNSIGNED NOT NULL ,CHANGE COLUMN `ControlType` `Control` VARCHAR(200) NULL DEFAULT NULL ,ADD COLUMN `Value1Init` TEXT NULL AFTER `Control`,ADD COLUMN `Value2Init` TEXT NULL AFTER `Value1Init`,CHANGE COLUMN `ControlProperty` `Value1` TEXT NULL AFTER `Value2Init`,CHANGE COLUMN `ControlValue` `Value2` TEXT NULL ,CHANGE COLUMN `Description` `Description` VARCHAR(255) NOT NULL DEFAULT '' AFTER `Fatal`,CHANGE COLUMN `ReturnCode` `ReturnCode` VARCHAR(2) NOT NULL AFTER `Description`,CHANGE COLUMN `ReturnMessage` `ReturnMessage` TEXT NULL AFTER `ReturnCode`;

-- 951
ALTER TABLE `testcasecountryproperties` CHANGE COLUMN `Description` `Description` VARCHAR(255) NULL AFTER `RetryPeriod`,CHANGE COLUMN `Value1` `Value1` TEXT NULL  ,CHANGE COLUMN `Value2` `Value2` TEXT NULL  ;

-- 952
ALTER TABLE `testcasestepaction` CHANGE COLUMN `ConditionVal1` `ConditionVal1` TEXT NULL  ,CHANGE COLUMN `Value1` `Value1` TEXT NOT NULL  ,CHANGE COLUMN `Value2` `Value2` TEXT NOT NULL  ;

-- 953
ALTER TABLE `testcasestepactionexecution` CHANGE COLUMN `ConditionVal1` `ConditionVal1` TEXT NULL  ,CHANGE COLUMN `Value1Init` `Value1Init` TEXT NULL  ,CHANGE COLUMN `Value2Init` `Value2Init` TEXT NULL  ,CHANGE COLUMN `Value1` `Value1` TEXT NULL  ,CHANGE COLUMN `Value2` `Value2` TEXT NULL  ,CHANGE COLUMN `ReturnMessage` `ReturnMessage` TEXT NULL ;

-- 954
select 1 from DUAL;

-- 955
Update soaplibrary set `envelope` = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(`envelope`, '&amp;', '&'),'&lt;','<'),'&gt;','>'),'&apos;','\''),'&quot;','\"');

-- 956
select 1 from DUAL;

-- 957
select 1 from DUAL;

-- 958
select 1 from DUAL;

-- 959
INSERT INTO `parameter`
  VALUES ('','cerberus_applicationobject_path','/opt/CerberusMedias/objects/','Path whare you will store all the files you upload in application object');

-- 960
CREATE TABLE `applicationobject` (  `ID` int(11) NOT NULL AUTO_INCREMENT,  `Application` varchar(200) NOT NULL,  `Object` varchar(150) NOT NULL,  `Value` text,  `ScreenshotFileName` varchar(250) DEFAULT NULL,  `UsrCreated` varchar(45) DEFAULT NULL,  `DateCreated` timestamp NULL DEFAULT NULL,  `UsrModif` varchar(45) DEFAULT NULL,  `DateModif` timestamp NULL DEFAULT NULL,  PRIMARY KEY (`Application`,`Object`),  UNIQUE KEY `ID_UNIQUE` (`ID`),  CONSTRAINT `fk_applicationobject_1` FOREIGN KEY (`Application`) REFERENCES `application` (`Application`) ON DELETE CASCADE ON UPDATE CASCADE)
  ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8;

-- 961
select 1 from DUAL;

-- 962
select 1 from DUAL;

-- 963
INSERT INTO `parameter`
  VALUES ('','cerberus_executiondetail_use','Y','Do you want to use the new Execution Detail Page (Y or N)');

-- 964
ALTER TABLE `testcasestep` DROP COLUMN `last_modified`,ADD COLUMN `UsrCreated` VARCHAR(45) NOT NULL DEFAULT '' AFTER `inlibrary`,ADD COLUMN `DateCreated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER `UsrCreated`,ADD COLUMN `UsrModif` VARCHAR(45) NULL DEFAULT '' AFTER `DateCreated`,ADD COLUMN `DateModif` TIMESTAMP NOT NULL DEFAULT '1970-01-01 01:01:01' ;

-- 965
UPDATE testcasestep tcs SET DateModif=now(), UsrModif = 'DatabaseMaintenanceV964', useStep='N', UseStepTest='', UseStepTestCase='', UseStepStep=-1 where concat(tcs.test, '||', tcs.testcase, '||', tcs.step, '||') in (select concat(toto.test, '||', toto.testcase, '||', toto.step, '||') from (select tcsa.* from testcasestep tcsa join testcasestep tcs1 on tcs1.test=tcsa.useSteptest and tcs1.testcase=tcsa.useSteptestcase and tcs1.step=tcsa.useStepstep where tcsa.useStep = 'Y' and tcs1.useStep='Y' order by tcs1.test, tcs1.testcase, tcs1.step) as toto );

-- 966
UPDATE testcasestep tcs SET DateModif=now(), UsrModif = 'DatabaseMaintenanceV965', inLibrary='Y' where concat(tcs.test, '||', tcs.testcase, '||', tcs.step, '||') in (select concat(toto.test, '||', toto.testcase, '||', toto.step, '||') from (select tcs1.* from testcasestep tcsa join testcasestep tcs1 on tcs1.test=tcsa.useSteptest and tcs1.testcase=tcsa.useSteptestcase and tcs1.step=tcsa.useStepstep where tcsa.useStep = 'Y' and tcs1.inLibrary!='Y' order by tcs1.test, tcs1.testcase, tcs1.step) as toto );

-- 967
select 1 from DUAL;

-- 968
ALTER TABLE `testcasestepactioncontrol` ADD COLUMN `ConditionOper` VARCHAR(45) NOT NULL DEFAULT '' AFTER `Sort`,ADD COLUMN `ConditionVal1` TEXT AFTER `ConditionOper`;

-- 969
ALTER TABLE `testcasestepactioncontrolexecution` ADD COLUMN `ConditionOper` VARCHAR(45) AFTER `Sort`,ADD COLUMN `ConditionVal1` TEXT AFTER `ConditionOper`;

-- 970
UPDATE testcasestepactioncontrol SET ConditionOper = 'always' where ConditionOper='';

-- 971
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`)
  VALUES  ('CONTROLCONDITIONOPER', 'always', '100', 'Always.', ''), ('CONTROLCONDITIONOPER', 'ifPropertyExist', '200', 'Only execute if property exist for the execution.', ''), ('CONTROLCONDITIONOPER', 'never', '9999', 'Never execute the control.', ''), ('INVARIANTPRIVATE', 'CONTROLCONDITIONOPER', '560', '', '');

-- 972
ALTER TABLE `usersystem` DROP FOREIGN KEY `FK_usersystem_01`;

-- 973
ALTER TABLE `usersystem` CHANGE COLUMN `Login` `Login` VARCHAR(255) NOT NULL ;

-- 974
ALTER TABLE `usergroup` DROP FOREIGN KEY `FK_usergroup_01`;

-- 975
ALTER TABLE `usergroup` CHANGE COLUMN `Login` `Login` VARCHAR(255) NOT NULL ;

-- 976
ALTER TABLE `user` CHANGE COLUMN `Login` `Login` VARCHAR(255) NOT NULL ;

-- 977
ALTER TABLE `usersystem` ADD CONSTRAINT `FK_usersystem_01` FOREIGN KEY (`Login`) REFERENCES `user` (`Login`) ON DELETE CASCADE ON UPDATE CASCADE;

-- 978
ALTER TABLE `usergroup` ADD CONSTRAINT `FK_usergroup_01` FOREIGN KEY (`Login`) REFERENCES `user` (`Login`) ON DELETE CASCADE ON UPDATE CASCADE;

-- 979
INSERT INTO `parameter`
  VALUES ('','cerberus_featureflipping_activatewebsocketpush','Y','Boolean that enable/disable the websocket push.');

-- 980
ALTER TABLE `testcasestep` ADD COLUMN `ConditionOper` VARCHAR(45) NOT NULL DEFAULT '' AFTER `Sort`,ADD COLUMN `ConditionVal1` TEXT AFTER `ConditionOper`;

-- 981
ALTER TABLE `testcasestepexecution` ADD COLUMN `ConditionOper` VARCHAR(45) AFTER `Sort`,ADD COLUMN `ConditionVal1` TEXT AFTER `ConditionOper`;

-- 982
UPDATE testcasestep SET ConditionOper = 'always' where ConditionOper='';

-- 983
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`)
  VALUES  ('STEPCONDITIONOPER', 'always', '100', 'Always.', ''), ('STEPCONDITIONOPER', 'ifPropertyExist', '200', 'Only execute if property exist for the execution.', ''), ('STEPCONDITIONOPER', 'never', '9999', 'Never execute the control.', ''), ('INVARIANTPRIVATE', 'STEPCONDITIONOPER', '570', '', '');

-- 984
ALTER TABLE `testcase` ADD COLUMN `ConditionOper` VARCHAR(45) NOT NULL DEFAULT '' AFTER `TcActive`,ADD COLUMN `ConditionVal1` TEXT AFTER `ConditionOper`;

-- 985
UPDATE testcase SET ConditionOper = 'always' where ConditionOper='';

-- 986
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`)
  VALUES  ('TESTCASECONDITIONOPER', 'always', '100', 'Always.', ''), ('TESTCASECONDITIONOPER', 'ifPropertyExist', '200', 'Only execute if property exist for the execution.', ''), ('TESTCASECONDITIONOPER', 'never', '9999', 'Never execute the control.', ''), ('INVARIANTPRIVATE', 'TESTCASECONDITIONOPER', '580', '', '');

-- 987
UPDATE testcase SET ConditionVal1 = '' where ConditionVal1 is null;

-- 988
UPDATE testcasestep SET ConditionVal1 = '' where ConditionVal1 is null;

-- 989
UPDATE testcasestepexecution SET ConditionVal1 = '' where ConditionVal1 is null;

-- 990
UPDATE testcasestepactioncontrol SET ConditionVal1 = '' where ConditionVal1 is null;

-- 991
DELETE from invariant where idname = 'ACTION' and value = 'skipAction';

-- 992
DELETE from invariant where idname = 'CONTROL' and value = 'skipControl';

-- 993
UPDATE testcasestepaction Set ConditionOper = 'never', Action = 'Unknown' where Action = 'skipAction';

-- 994
UPDATE testcasestepactioncontrol Set ConditionOper = 'never', Control = 'Unknown' where Control = 'skipControl';

-- 995
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) SELECT * from (SELECT 'CAPABILITY', 'udid', '8', 'Unique Device IDentifier (useful for IOS testing)', '') AS tmp WHERE NOT EXISTS ( SELECT `value` FROM `invariant` WHERE idname='CAPABILITY' and `value`='udid') LIMIT 1;

-- 996
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) SELECT * from (SELECT 'CAPABILITY', 'xcodeConfigFile', '9', 'Path to the Xcode Configuration File containing information about application sign (useful for IOS testing)', '') AS tmp WHERE NOT EXISTS ( SELECT `value` FROM `invariant` WHERE idname='CAPABILITY' and `value`='xcodeConfigFile') LIMIT 1;

-- 997
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) SELECT * from (SELECT 'CAPABILITY', 'realDeviceLogger', '10', 'Path to the logger for real IOS devices (useful for IOS testing)', '') AS tmp WHERE NOT EXISTS ( SELECT `value` FROM `invariant` WHERE idname='CAPABILITY' and  `value`='realDeviceLogger') LIMIT 1;

-- 998
select 1 from DUAL;

-- 999
ALTER TABLE `testcase` ADD COLUMN `ConditionVal2` TEXT NULL AFTER `ConditionVal1`;

-- 1000
ALTER TABLE `testcasestep` ADD COLUMN `ConditionVal2` TEXT NULL AFTER `ConditionVal1`;

-- 1001
ALTER TABLE `testcasestepaction` ADD COLUMN `ConditionVal2` TEXT NULL AFTER `ConditionVal1`;

-- 1002
ALTER TABLE `testcasestepactioncontrol` ADD COLUMN `ConditionVal2` TEXT NULL AFTER `ConditionVal1`;

-- 1003
ALTER TABLE `testcasestepexecution` ADD COLUMN `ConditionVal2` TEXT NULL AFTER `ConditionVal1`;

-- 1004
ALTER TABLE `testcasestepactionexecution` ADD COLUMN `ConditionVal2` TEXT NULL AFTER `ConditionVal1`;

-- 1005
ALTER TABLE `testcasestepactioncontrolexecution` ADD COLUMN `ConditionVal2` TEXT NULL AFTER `ConditionVal1`;

-- 1006
UPDATE `testcase` SET `ConditionVal2` = '';

-- 1007
UPDATE `testcasestep` SET `ConditionVal2` = '';

-- 1008
UPDATE `testcasestepaction` SET `ConditionVal2` = '';

-- 1009
UPDATE `testcasestepactioncontrol` SET `ConditionVal2` = '';

-- 1010
select 1 from DUAL;

-- 1011
select 1 from DUAL;

-- 1012
INSERT INTO `invariant`
  VALUES ('ACTIONCONDITIONOPER', 'ifNumericEqual', 300, 'Only execute if value1 equals value2.', '', '', '', '')
  ,('ACTIONCONDITIONOPER', 'ifNumericDifferent', 310, 'Only execute if value1 is different from value2.', '', '', '', '')
  ,('ACTIONCONDITIONOPER', 'ifNumericGreater', 320, 'Only execute if value1 greater than value2.', '', '', '', '')
  ,('ACTIONCONDITIONOPER', 'ifNumericGreaterOrEqual', 330, 'Only execute if value1 greater or equal than value2.', '', '', '', '')
  ,('ACTIONCONDITIONOPER', 'ifNumericMinor', 340, 'Only execute if value1 lower than value2.', '', '', '', '')
  ,('ACTIONCONDITIONOPER', 'ifNumericMinorOrEqual', 350, 'Only execute if value1 lower or equal than value2.', '', '', '', '')
  ,('ACTIONCONDITIONOPER', 'ifStringEqual', 400, 'Only execute if value1 equals value2.', '', '', '', '')
  ,('ACTIONCONDITIONOPER', 'ifStringDifferent', 410, 'Only execute if value1 different from value2.', '', '', '', '')
  ,('ACTIONCONDITIONOPER', 'ifStringGreater', 420, 'Only execute if value1 greater than value2.', '', '', '', '')
  ,('ACTIONCONDITIONOPER', 'ifStringMinor', 430, 'Only execute if value1 lower than value2.', '', '', '', '')
  ,('ACTIONCONDITIONOPER', 'ifStringContains', 440, 'Only execute if value1 contains value2.', '', '', '', '');

-- 1013
INSERT INTO `invariant`
  VALUES ('STEPCONDITIONOPER', 'ifNumericEqual', 300, 'Only execute if value1 equals value2.', '', '', '', '')
  ,('STEPCONDITIONOPER', 'ifNumericDifferent', 310, 'Only execute if value1 is different from value2.', '', '', '', '')
  ,('STEPCONDITIONOPER', 'ifNumericGreater', 320, 'Only execute if value1 greater than value2.', '', '', '', '')
  ,('STEPCONDITIONOPER', 'ifNumericGreaterOrEqual', 330, 'Only execute if value1 greater or equal than value2.', '', '', '', '')
  ,('STEPCONDITIONOPER', 'ifNumericMinor', 340, 'Only execute if value1 lower than value2.', '', '', '', '')
  ,('STEPCONDITIONOPER', 'ifNumericMinorOrEqual', 350, 'Only execute if value1 lower or equal than value2.', '', '', '', '')
  ,('STEPCONDITIONOPER', 'ifStringEqual', 400, 'Only execute if value1 equals value2.', '', '', '', '')
  ,('STEPCONDITIONOPER', 'ifStringDifferent', 410, 'Only execute if value1 different from value2.', '', '', '', '')
  ,('STEPCONDITIONOPER', 'ifStringGreater', 420, 'Only execute if value1 greater than value2.', '', '', '', '')
  ,('STEPCONDITIONOPER', 'ifStringMinor', 430, 'Only execute if value1 lower than value2.', '', '', '', '')
  ,('STEPCONDITIONOPER', 'ifStringContains', 440, 'Only execute if value1 contains value2.', '', '', '', '');

-- 1014
INSERT INTO `invariant`
  VALUES ('CONTROLCONDITIONOPER', 'ifNumericEqual', 300, 'Only execute if value1 equals value2.', '', '', '', '')
  ,('CONTROLCONDITIONOPER', 'ifNumericDifferent', 310, 'Only execute if value1 is different from value2.', '', '', '', '')
  ,('CONTROLCONDITIONOPER', 'ifNumericGreater', 320, 'Only execute if value1 greater than value2.', '', '', '', '')
  ,('CONTROLCONDITIONOPER', 'ifNumericGreaterOrEqual', 330, 'Only execute if value1 greater or equal than value2.', '', '', '', '')
  ,('CONTROLCONDITIONOPER', 'ifNumericMinor', 340, 'Only execute if value1 lower than value2.', '', '', '', '')
  ,('CONTROLCONDITIONOPER', 'ifNumericMinorOrEqual', 350, 'Only execute if value1 lower or equal than value2.', '', '', '', '')
  ,('CONTROLCONDITIONOPER', 'ifStringEqual', 400, 'Only execute if value1 equals value2.', '', '', '', '')
  ,('CONTROLCONDITIONOPER', 'ifStringDifferent', 410, 'Only execute if value1 different from value2.', '', '', '', '')
  ,('CONTROLCONDITIONOPER', 'ifStringGreater', 420, 'Only execute if value1 greater than value2.', '', '', '', '')
  ,('CONTROLCONDITIONOPER', 'ifStringMinor', 430, 'Only execute if value1 lower than value2.', '', '', '', '')
  ,('CONTROLCONDITIONOPER', 'ifStringContains', 440, 'Only execute if value1 contains value2.', '', '', '', '');

-- 1015
INSERT INTO `invariant`
  VALUES ('TESTCASECONDITIONOPER', 'ifNumericEqual', 300, 'Only execute if value1 equals value2.', '', '', '', '')
  ,('TESTCASECONDITIONOPER', 'ifNumericDifferent', 310, 'Only execute if value1 is different from value2.', '', '', '', '')
  ,('TESTCASECONDITIONOPER', 'ifNumericGreater', 320, 'Only execute if value1 greater than value2.', '', '', '', '')
  ,('TESTCASECONDITIONOPER', 'ifNumericGreaterOrEqual', 330, 'Only execute if value1 greater or equal than value2.', '', '', '', '')
  ,('TESTCASECONDITIONOPER', 'ifNumericMinor', 340, 'Only execute if value1 lower than value2.', '', '', '', '')
  ,('TESTCASECONDITIONOPER', 'ifNumericMinorOrEqual', 350, 'Only execute if value1 lower or equal than value2.', '', '', '', '')
  ,('TESTCASECONDITIONOPER', 'ifStringEqual', 400, 'Only execute if value1 equals value2.', '', '', '', '')
  ,('TESTCASECONDITIONOPER', 'ifStringDifferent', 410, 'Only execute if value1 different from value2.', '', '', '', '')
  ,('TESTCASECONDITIONOPER', 'ifStringGreater', 420, 'Only execute if value1 greater than value2.', '', '', '', '')
  ,('TESTCASECONDITIONOPER', 'ifStringMinor', 430, 'Only execute if value1 lower than value2.', '', '', '', '')
  ,('TESTCASECONDITIONOPER', 'ifStringContains', 440, 'Only execute if value1 contains value2.', '', '', '', '');

-- 1016
INSERT INTO `invariant`
  VALUES ('APPLITYPE', 'FAT', '60', 'FAT client application', '', '', '', '');

-- 1017
ALTER TABLE `testcasestepactioncontrolexecution` DROP FOREIGN KEY `FK_testcasestepactioncontrolexecution_01`;

-- 1018
ALTER TABLE `testcasestepactionexecution` DROP FOREIGN KEY `FK_testcasestepactionexecution_01`;

-- 1019
ALTER TABLE `testcasestepexecution`ADD COLUMN `index` INT(11) NOT NULL DEFAULT '1' AFTER `Step`,DROP PRIMARY KEY,ADD PRIMARY KEY (`ID`, `Test`, `TestCase`, `Step`, `index`) ;

-- 1020
ALTER TABLE `testcasestepactioncontrolexecution` ADD COLUMN `index` INT(11) NOT NULL DEFAULT '1' AFTER `Step`;

-- 1021
ALTER TABLE `testcasestepactioncontrolexecution` ADD CONSTRAINT `FK_testcasestepactioncontrolexecution_01` FOREIGN KEY (`ID` , `Test` , `TestCase` , `Step`, `index` ) REFERENCES `testcasestepexecution` (`ID` , `Test` , `TestCase` , `Step` , `index` ) ON DELETE CASCADE ON UPDATE CASCADE ;

-- 1022
ALTER TABLE `testcasestepactionexecution` ADD COLUMN `index` INT(11) NOT NULL DEFAULT '1' AFTER `Step`;

-- 1023
ALTER TABLE `testcasestepactionexecution` ADD CONSTRAINT `FK_testcasestepactionexecution_01` FOREIGN KEY (`ID` , `Test` , `TestCase` , `Step` , `index`) REFERENCES `testcasestepexecution` (`ID` , `Test` , `TestCase` , `Step` , `index`) ON DELETE CASCADE ON UPDATE CASCADE;

-- 1024
ALTER TABLE `testcasestep` ADD COLUMN `Loop` VARCHAR(45) NOT NULL DEFAULT '' AFTER `Sort`;

-- 1025
ALTER TABLE `testcasestepactionexecution` DROP PRIMARY KEY,ADD PRIMARY KEY (`ID`, `Test`, `TestCase`, `Step`, `index`, `Sequence`)  ;

-- 1026
ALTER TABLE `testcasestepactioncontrolexecution` DROP PRIMARY KEY,ADD PRIMARY KEY (`ID`, `Test`, `TestCase`, `Step`, `index`, `Sequence`, `ControlSequence`) ;

-- 1027
INSERT INTO `invariant`
  VALUES ('STEPLOOP', 'onceIfConditionTrue', 100, 'We execute the step once only if the condiion is true.', '', '', '', '')
  ,('STEPLOOP', 'onceIfConditionFalse', 200, 'We execute the step once only if the condiion is false.', '', '', '', '')
  ,('STEPLOOP', 'doWhileConditionTrue', 300, 'We execute the step and then execute it again and again as long as condition is true.', '', '', '', '')
  ,('STEPLOOP', 'doWhileConditionFalse', 400, 'We execute the step and then execute it again and again as long as condition is false.', '', '', '', '')
  ,('STEPLOOP', 'whileConditionTrueDo', 500, 'We execute the step as long the condition is true.', '', '', '', '')
  ,('STEPLOOP', 'whileConditionFalseDo', 600, 'We execute the step as long the condition is false.', '', '', '', '')
  ,('INVARIANTPRIVATE', 'STEPLOOP', '590', '', '', '', '', '');

-- 1028
UPDATE `testcasestep` SET `Loop` = 'onceIfConditionTrue' WHERE `Loop` = '';

-- 1029
ALTER TABLE `robot` ADD COLUMN `screensize` VARCHAR(250) NOT NULL DEFAULT '' AFTER `useragent`;

-- 1030
select 1 from DUAL;

-- 1031
INSERT INTO `parameter`
  VALUES ('','cerberus_loopstep_max','20','Integer value that correspond to the max number of step loop authorised.<br>This parameter can be configured at the system level.');

-- 1032
select 1 from DUAL;

-- 1033
select 1 from DUAL;

-- 1034
ALTER TABLE `countryenvironmentparameters` ADD COLUMN `poolSize` INT NULL AFTER `Var4`;

-- 1035
INSERT INTO `parameter`
  VALUES ('','cerberus_featureflipping_websocketpushperiod','5000','Integer value that correspond to the nb of ms between every websocket push.');

-- 1036
ALTER TABLE `testcaseexecutionqueue` ADD COLUMN `State` VARCHAR(9) NOT NULL DEFAULT 'WAITING' AFTER `manualexecution`;

-- 1037
UPDATE `testcaseexecutionqueue` SET `State` = 'WAITING' WHERE `Proceeded` = 0;

-- 1038
UPDATE `testcaseexecutionqueue` SET `State` = 'EXECUTING' WHERE `Proceeded` = 1;

-- 1039
ALTER TABLE `testcaseexecution` ADD COLUMN `EnvironmentData` VARCHAR(45) NULL DEFAULT ''  AFTER `Environment`,ADD COLUMN `ConditionOper` VARCHAR(45) NULL DEFAULT ''  AFTER `screensize`,ADD COLUMN `ConditionVal1Init` TEXT NULL AFTER `ConditionOper`,ADD COLUMN `ConditionVal2Init` TEXT NULL AFTER `ConditionVal1Init`,ADD COLUMN `ConditionVal1` TEXT NULL AFTER `ConditionVal2Init`,ADD COLUMN `ConditionVal2` TEXT NULL AFTER `ConditionVal1`;

-- 1040
ALTER TABLE `testcasestepexecution` ADD COLUMN `ConditionVal1Init` TEXT NULL AFTER `ConditionOper`,ADD COLUMN `ConditionVal2Init` TEXT NULL AFTER `ConditionVal1Init`;

-- 1041
ALTER TABLE `testcasestepactionexecution` ADD COLUMN `ConditionVal1Init` TEXT NULL AFTER `ConditionOper`,ADD COLUMN `ConditionVal2Init` TEXT NULL AFTER `ConditionVal1Init`;

-- 1042
ALTER TABLE `testcasestepactioncontrolexecution` ADD COLUMN `ConditionVal1Init` TEXT NULL AFTER `ConditionOper`,ADD COLUMN `ConditionVal2Init` TEXT NULL AFTER `ConditionVal1Init`;

-- 1043
select 1 from DUAL;

-- 1044
ALTER TABLE `testcaseexecution`ADD COLUMN `ManualExecution` VARCHAR(1) NULL DEFAULT '' AFTER `ConditionVal2`;

-- 1045
select 1 from DUAL;

-- 1046
INSERT INTO `parameter`
  VALUES ('','cerberus_property_countrylevelheritage','N','Boolean that activate the heritage of the property calculation at the country level. if N, a property will be considered as not available on country XXX when it does not exist for XXX and exist for any other country but XXX at testcase level (even if it has been defined at usestep or pretest level for that country XXX). If Y, it will be considered as defined for country XXX as long as it has been defined for that country at testcase, usestep or pretest level.');

-- 1047
select 1 from DUAL;

-- 1048
select 1 from DUAL;

-- 1049
ALTER TABLE `testcaseexecutionqueue` CHANGE COLUMN `comment` `comment` TEXT NULL DEFAULT NULL ;

-- 1050
select 1 from DUAL;

-- 1051
ALTER TABLE `testcasestepexecution`  ADD COLUMN `loop` VARCHAR(45) NULL DEFAULT '' AFTER `Sort`;

-- 1052
select 1 from DUAL;

-- 1053
select 1 from DUAL;

-- 1054
ALTER TABLE `soaplibrary` CHANGE COLUMN `Name` `Service` VARCHAR(255) NOT NULL DEFAULT ''  ,CHANGE COLUMN `Type` `Group` VARCHAR(45) NULL DEFAULT ''  ,CHANGE COLUMN `Method` `Operation` VARCHAR(255) NULL DEFAULT ''  ,CHANGE COLUMN `Envelope` `ServiceRequest` MEDIUMTEXT NULL DEFAULT NULL  , RENAME TO  `appservice` ;

-- 1055
ALTER TABLE `appservice` CHANGE COLUMN `Group` `Group` VARCHAR(45) NULL DEFAULT '' AFTER `ParsingAnswer`,ADD COLUMN `Application` VARCHAR(200) NULL DEFAULT '' AFTER `Service`,ADD COLUMN `Type` VARCHAR(45) NOT NULL DEFAULT ''  AFTER `Application`,ADD COLUMN `Method` VARCHAR(45) NOT NULL DEFAULT '' AFTER `Type`;

-- 1056
INSERT INTO `invariant`
  VALUES ('SRVTYPE', 'SOAP', 100, 'SOAP Service.', '', '', '', '')
  ,('SRVTYPE', 'REST', 200, 'REST Service.', '', '', '', '')
  ,('SRVMETHOD', 'GET', 100, 'GET http method.', '', '', '', '')
  ,('SRVMETHOD', 'POST', 200, 'POST http method.', '', '', '', '')
  ,('INVARIANTPRIVATE', 'SRVTYPE', '600', '', '', '', '', '')
  ,('INVARIANTPRIVATE', 'SRVMETHOD', '610', '', '', '', '', '');

-- 1057
UPDATE `appservice` SET `Type`='SOAP', `application` = null;

-- 1058
ALTER TABLE `appservice` ADD INDEX `FK_appservice_01` (`Application` ASC) ;

-- 1059
ALTER TABLE `appservice` ADD CONSTRAINT `FK_appservice_01` FOREIGN KEY (`Application`) REFERENCES `application` (`Application`) ON DELETE CASCADE ON UPDATE CASCADE;

-- 1060
ALTER TABLE `appservice` ADD COLUMN `UsrCreated` VARCHAR(45) NOT NULL DEFAULT '' AFTER `Description`,ADD COLUMN `DateCreated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER `UsrCreated`,ADD COLUMN `UsrModif` VARCHAR(45) NULL DEFAULT '' AFTER `DateCreated`,ADD COLUMN `DateModif` TIMESTAMP NOT NULL DEFAULT '1970-01-01 01:01:01' ;

-- 1061
update testcasestepaction SET `action` = 'callService' where `action` in ('callSoap','callSoapWithBase');

-- 1062
INSERT INTO `invariant`
  VALUES ('ACTION', 'callService', 17000, 'Call Service.', '', '', '', '');

-- 1063
DELETE FROM `invariant` WHERE `idname` = 'ACTION' and `value` in ('callSoap','callSoapWithBase');

-- 1064
select 1 from DUAL;

-- 1065
select 1 from DUAL;

-- 1066
UPDATE appservice SET DateCreated = '1970-01-01 01:01:01' WHERE DateCreated = '1970-01-01 01:01:01';

-- 1067
CREATE TABLE `appservicecontent` (  `Service` VARCHAR(255) NOT NULL ,  `Key` VARCHAR(255) NOT NULL ,  `Value` TEXT NULL ,  `Sort` INT NULL DEFAULT 0 ,  `Active` VARCHAR(45) NULL DEFAULT 'Y' ,  `UsrCreated` VARCHAR(45) NOT NULL DEFAULT '' ,  `DateCreated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ,  `UsrModif` VARCHAR(45) NULL DEFAULT '' ,  `DateModif` TIMESTAMP NOT NULL DEFAULT '1970-01-01 01:01:01' ,  PRIMARY KEY (`Service`, `Key`),    CONSTRAINT `FK_appservicecontent_01` FOREIGN KEY (`Service`) REFERENCES `appservice` (`Service`) ON DELETE CASCADE ON UPDATE CASCADE) 
  ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 1068
CREATE TABLE `appserviceheader` (  `Service` VARCHAR(255) NOT NULL ,  `Key` VARCHAR(255) NOT NULL ,  `Value` TEXT NULL ,  `Sort` INT NULL DEFAULT 0 ,  `Active` VARCHAR(45) NULL DEFAULT 'Y' ,  `UsrCreated` VARCHAR(45) NOT NULL DEFAULT '' ,  `DateCreated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ,  `UsrModif` VARCHAR(45) NULL DEFAULT '' ,  `DateModif` TIMESTAMP NOT NULL DEFAULT '1970-01-01 01:01:01' ,  PRIMARY KEY (`Service`, `Key`),   CONSTRAINT `FK_appserviceheader_01` FOREIGN KEY (`Service`) REFERENCES `appservice` (`Service`) ON DELETE CASCADE ON UPDATE CASCADE) 
  ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 1069
ALTER TABLE `appserviceheader`   ADD COLUMN `Description` VARCHAR(255) NOT NULL DEFAULT '' AFTER `Active`;

-- 1070
ALTER TABLE `appservicecontent`   ADD COLUMN `Description` VARCHAR(255) NOT NULL DEFAULT '' AFTER `Active`;

-- 1071
ALTER TABLE `application` ADD COLUMN `UsrCreated` VARCHAR(45) NOT NULL DEFAULT '' AFTER `mavengroupid`,ADD COLUMN `DateCreated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER `UsrCreated`,ADD COLUMN `UsrModif` VARCHAR(45) NULL DEFAULT '' AFTER `DateCreated`,ADD COLUMN `DateModif` TIMESTAMP NOT NULL DEFAULT '1970-01-01 01:01:01' ;

-- 1072
INSERT INTO `parameter`
  VALUES  ('','cerberus_callservice_enablehttpheadertoken','Y','Boolean that activate the addition of a header entry cerberus_token with execution id value on every serice call.'),  ('','cerberus_callservice_timeoutms','60000','timeout in ms second used for any service call.');

-- 1073
UPDATE application SET `Type` = 'SRV' WHERE `Type` = 'WS';

-- 1074
DELETE FROM `invariant` WHERE `idname`='APPLITYPE' and`value`='WS';

-- 1075
UPDATE `invariant` SET `sort`='900' WHERE `idname`='APPLITYPE' and`value`='NONE';

-- 1076
UPDATE `invariant` SET `sort`='800' WHERE `idname`='APPLITYPE' and`value`='BAT';

-- 1077
UPDATE `invariant` SET `description`='Web GUI application' WHERE `idname`='APPLITYPE' and`value`='GUI';

-- 1078
UPDATE `invariant` SET `description`='Service Application (REST or SOAP)' WHERE `idname`='APPLITYPE' and`value`='SRV';

-- 1079
select 1 from DUAL;

-- 1080
select 1 from DUAL;

-- 1081
select 1 from DUAL;

-- 1082
select 1 from DUAL;

-- 1083
select 1 from DUAL;

-- 1084
select 1 from DUAL;

-- 1085
select 1 from DUAL;

-- 1086
ALTER TABLE `testcaseexecutionqueue` DROP COLUMN `Proceeded`;

-- 1087
ALTER TABLE `testcasecountryproperties` ADD COLUMN `valueTemp` TEXT NULL AFTER `last_modified`;

-- 1088
UPDATE testcasecountryproperties  SET valueTemp=Value2 where `Type` in ('getFromJSON', 'getFromXML');

-- 1089
UPDATE testcasecountryproperties  SET Value2=Value1 where `Type` in ('getFromJSON', 'getFromXML');

-- 1090
UPDATE testcasecountryproperties  SET Value1=valueTemp where `Type` in ('getFromJSON', 'getFromXML');

-- 1091
ALTER TABLE `testcasecountryproperties` DROP COLUMN `valueTemp` ;

-- 1092
INSERT INTO `invariant`
  VALUES ('APPSERVICECONTENTACT', 'Y', 100, 'Yes', '', '', '', '')
  ,('APPSERVICECONTENTACT', 'N', 200, 'No', '', '', '', '')
  ,('APPSERVICEHEADERACT', 'Y', 100, 'Yes', '', '', '', '')
  ,('APPSERVICEHEADERACT', 'N', 200, 'No', '', '', '', '')
  ,('INVARIANTPRIVATE', 'APPSERVICECONTENTACT', '620', '', '', '', '', '')
  ,('INVARIANTPRIVATE', 'APPSERVICEHEADERACT', '630', '', '', '', '', '');

-- 1093
UPDATE testcasestepactioncontrol SET Control = 'verifyNumericEquals' where Control in ('verifyIntegerEquals');

-- 1094
UPDATE testcasestepactioncontrol SET Control = 'verifyNumericDifferent' where Control in ('verifyIntegerDifferent');

-- 1095
UPDATE testcasestepactioncontrol SET Control = 'verifyNumericGreater' where Control in ('verifyIntegerGreater');

-- 1096
UPDATE testcasestepactioncontrol SET Control = 'verifyNumericMinor' where Control in ('verifyIntegerMinor');

-- 1097
UPDATE `invariant` SET `value`='verifyNumericEquals', `description`='verifyNumericEquals' WHERE `idname`='CONTROL' and`value`='verifyIntegerEquals';

-- 1098
UPDATE `invariant` SET `value`='verifyNumericDifferent', `description`='verifyNumericDifferent' WHERE `idname`='CONTROL' and`value`='verifyIntegerDifferent';

-- 1099
UPDATE `invariant` SET `value`='verifyNumericGreater', `description`='verifyNumericGreater' WHERE `idname`='CONTROL' and`value`='verifyIntegerGreater';

-- 1100
UPDATE `invariant` SET `value`='verifyNumericMinor', `description`='verifyNumericMinor' WHERE `idname`='CONTROL' and`value`='verifyIntegerMinor';

-- 1101
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`)
  VALUES ('CONTROL', 'verifyNumericGreaterOrEqual', '1610', 'verifyNumericGreaterOrEqual');

-- 1102
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`)
  VALUES ('CONTROL', 'verifyNumericMinorOrEqual', '1710', 'verifyNumericMinorOrEqual');

-- 1103
ALTER TABLE `testdatalib` ADD COLUMN `Service` VARCHAR(255) NULL DEFAULT null AFTER `DatabaseUrl`;

-- 1104
ALTER TABLE `testdatalib` ADD INDEX `IX_testdatalib_02` (`Service` ASC);

-- 1105
ALTER TABLE `testdatalib` ADD CONSTRAINT `FK_testdatalib_01` FOREIGN KEY (`Service`) REFERENCES `appservice` (`Service`) ON DELETE CASCADE ON UPDATE CASCADE;

-- 1106
INSERT INTO `invariant`
  VALUES ('ACTIONCONDITIONOPER', 'ifElementPresent', 250, 'Only execute if Element is present.', '', '', '', '')
  ,('STEPCONDITIONOPER', 'ifElementPresent', 250, 'Only execute if Element is present.', '', '', '', '')
  ,('CONTROLCONDITIONOPER', 'ifElementPresent', 250, 'Only execute if Element is present.', '', '', '', '');

-- 1107
INSERT INTO `parameter`
  VALUES ('','cerberus_selenium_action_click_timeout','90000','timeout in ms second used during selenium click action.');

-- 1108
INSERT INTO `parameter`
  VALUES ('','cerberus_homepage_nbdisplayedtag','5','Number of tag summary displayed inside homepage.');

-- 1109
ALTER TABLE `testcaseexecution` CHANGE COLUMN `Port` `Port` VARCHAR(150) NULL DEFAULT NULL ;

-- 1110
ALTER TABLE `robot` CHANGE COLUMN `Port` `Port` VARCHAR(150) NULL DEFAULT NULL ;

-- 1111
ALTER TABLE `testcaseexecutionqueue` CHANGE COLUMN `RobotPort` `RobotPort` VARCHAR(150) NULL DEFAULT NULL ;

-- 1112
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`) SELECT 'CAPABILITY', 'acceptInsecureCerts', 10, 'Whether the session should accept insecure SSL certs by default.' FROM DUAL WHERE NOT EXISTS (SELECT * FROM `invariant` where `idname`='CAPABILITY' AND `value` = 'acceptInsecureCerts');

-- 1113
INSERT INTO `invariant` (`idname`, `value`, `sort`,`description`) SELECT 'CAPABILITY', 'acceptSslCerts', 10,'Whether the session should accept all SSL certs by default.' FROM DUAL WHERE NOT EXISTS (SELECT * FROM `invariant` where `idname`='CAPABILITY' AND `value` = 'acceptSslCerts');

-- 1114
INSERT INTO `invariant` (`idname`, `value`, `sort`,`description`) SELECT 'CAPABILITY', 'appActivity', 10,'Activity name for the Android activity you want to launch from your package. This often needs to be preceded by a . (e.g., .MainActivity instead of MainActivity).' FROM DUAL WHERE NOT EXISTS (SELECT * FROM `invariant` where `idname`='CAPABILITY' AND `value` = 'appActivity');

-- 1115
INSERT INTO `invariant` (`idname`, `value`, `sort`,`description`) SELECT 'CAPABILITY', 'appWaitActivity', 10,'Activity name for the Android activity you want to wait for.' FROM DUAL WHERE NOT EXISTS (SELECT * FROM `invariant` where `idname`='CAPABILITY' AND `value` = 'appWaitActivity');

-- 1116
ALTER TABLE `testcaseexecution` ADD COLUMN `UserAgent` VARCHAR(250) NULL DEFAULT NULL AFTER `screensize`;

-- 1117
select 1 from DUAL;

-- 1118
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`)
  VALUES ('OUTPUTFORMAT', 'verbose-json', '5', 'Verbose json format', '');

-- 1119
ALTER TABLE `testcaseexecutionqueue` CHANGE COLUMN `Browser` `Browser` VARCHAR(45) NULL DEFAULT NULL ;

-- 1120
INSERT INTO `parameter`
  VALUES ('','integration_smtp_username','','Username to be used in case of SMTP with Authentication. Empty if no authentication required.')
  ,('','integration_smtp_password','','Password to be used in case of SMTP with Authentication. Empty if no authentication required.');

-- 1121
ALTER TABLE `invariant`  ADD COLUMN `gp4` VARCHAR(45) NULL DEFAULT NULL AFTER `gp3`,  ADD COLUMN `gp5` VARCHAR(45) NULL DEFAULT NULL AFTER `gp4`,  ADD COLUMN `gp6` VARCHAR(45) NULL DEFAULT NULL AFTER `gp5`,  ADD COLUMN `gp7` VARCHAR(45) NULL DEFAULT NULL AFTER `gp6`,  ADD COLUMN `gp8` VARCHAR(45) NULL DEFAULT NULL AFTER `gp7`,  ADD COLUMN `gp9` VARCHAR(45) NULL DEFAULT NULL AFTER `gp8` ;

-- 1122
INSERT INTO `parameter`
  VALUES ('','cerberus_callservicerest_proxyactive','N','Y if you want to activate proxy for REST CallService.')
  ,('','cerberus_callservicerest_proxyhost','proxy','Hostname of the proxy that will be used for REST CallService.')
  ,('','cerberus_callservicerest_proxyport','80','Port Number of the proxy that will be used for REST CallService.')
  ,('','cerberus_callservicerest_proxyauthentificationactive','N','Y if you want to activate proxy authentification for REST CallService.')
  ,('','cerberus_callservicerest_proxyuser','user','Username to be used in case of REST Call Service with Authentication.')
  ,('','cerberus_callservicerest_proxypassword','password','Password to be used in case of REST Call Service with Authentication.');

-- 1123
UPDATE `invariant` SET `value`='SERVICE', `description`='Dynamic test data from SERVICE Webservice call.' WHERE `idname`='TESTDATATYPE' and`value`='SOAP';

-- 1124
UPDATE `testdatalib` SET `Type`='SERVICE' WHERE `Type`='SOAP';

-- 1125
select 1 from DUAL;

-- 1126
select 1 from DUAL;

-- 1127
select 1 from DUAL;

-- 1128
select 1 from DUAL;

-- 1129
ALTER TABLE `testcaseexecution`  ADD COLUMN `Description` VARCHAR(500) NULL DEFAULT NULL AFTER `testcase`;

-- 1130
CREATE TABLE `campaignlabel` (  `campaignlabelID` int(10) unsigned NOT NULL AUTO_INCREMENT,  `campaign` varchar(45) NOT NULL,  `labelId` INT(11) ,  PRIMARY KEY (`campaignlabelID`),  UNIQUE KEY `IX_campaignlabel_01` (`campaign`, `labelId`),  KEY `IX_campaignlabel_02` (`campaign`),  CONSTRAINT `FK_campaignlabel_01` FOREIGN KEY (`campaign`) REFERENCES `campaign` (`campaign`) ON DELETE CASCADE ON UPDATE CASCADE,  CONSTRAINT `FK_campaignlabel_02` FOREIGN KEY (`labelId`) REFERENCES `label` (`Id`) ON DELETE CASCADE ON UPDATE CASCADE)
  ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- 1131
select 1 from DUAL;

-- 1132
select 1 from DUAL;

-- 1133
UPDATE `parameter` SET `description`='Y if you want to activate proxy.' WHERE `system`='' and`param`='cerberus_callservicerest_proxyactive';

-- 1134
UPDATE `parameter` SET `description`='Y if you want to activate proxy authentification.' WHERE `system`='' and`param`='cerberus_callservicerest_proxyauthentificationactive';

-- 1135
UPDATE `parameter` SET `description`='Hostname of the proxy.' WHERE `system`='' and`param`='cerberus_callservicerest_proxyhost';

-- 1136
UPDATE `parameter` SET `description`='Password to be used in case of proxy with Authentication.' WHERE `system`='' and`param`='cerberus_callservicerest_proxypassword';

-- 1137
UPDATE `parameter` SET `description`='Port Number of the proxy.' WHERE `system`='' and`param`='cerberus_callservicerest_proxyport';

-- 1138
UPDATE `parameter` SET `description`='Username to be used in case of proxy with Authentication.' WHERE `system`='' and`param`='cerberus_callservicerest_proxyuser';

-- 1139
UPDATE `parameter` SET `param`='cerberus_proxy_active' WHERE `param`='cerberus_callservicerest_proxyactive';

-- 1140
UPDATE `parameter` SET `param`='cerberus_proxyauthentification_active' WHERE `param`='cerberus_callservicerest_proxyauthentificationactive';

-- 1141
UPDATE `parameter` SET `param`='cerberus_proxy_host' WHERE `param`='cerberus_callservicerest_proxyhost';

-- 1142
UPDATE `parameter` SET `param`='cerberus_proxyauthentification_password' WHERE `param`='cerberus_callservicerest_proxypassword';

-- 1143
UPDATE `parameter` SET `param`='cerberus_proxy_port' WHERE `param`='cerberus_callservicerest_proxyport';

-- 1144
UPDATE `parameter` SET `param`='cerberus_proxyauthentification_user' WHERE `param`='cerberus_callservicerest_proxyuser';

-- 1145
ALTER TABLE `campaignlabel` ADD COLUMN `UsrCreated` VARCHAR(45) NOT NULL DEFAULT '' AFTER `LabelId`,ADD COLUMN `DateCreated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER `UsrCreated`,ADD COLUMN `UsrModif` VARCHAR(45) NULL DEFAULT '' AFTER `DateCreated`,ADD COLUMN `DateModif` TIMESTAMP NOT NULL DEFAULT '1970-01-01 01:01:01' ;

-- 1146
DROP TABLE `log`;

-- 1147
INSERT INTO `parameter`
  VALUES ('','cerberus_proxy_nonproxyhosts','localhost,127.0.0.1,192.168.1.*','The list of hosts that should be reached directly, bypassing the proxy. This is a list of patterns separated by \',\'. The patterns may start or end with a \'*\' for wildcards. Any host matching one of these patterns will be reached through a direct connection instead of through a proxy.');

-- 1148
DROP TABLE `testdata`;

-- 1149
UPDATE `testcasecountryproperties` SET `Type`='Unknown' where `Type` = 'getFromTestData';

-- 1150
DELETE FROM `invariant` WHERE `idname`='PROPERTYTYPE' and`value`='getFromTestData';

-- 1151
ALTER TABLE `appservice` DROP COLUMN `ParsingAnswer`, ADD COLUMN `AttachementURL` VARCHAR(255) NULL DEFAULT '' AFTER `Operation`;

-- 1152
ALTER TABLE `testcase` ADD COLUMN `screensize` VARCHAR(250) NOT NULL DEFAULT '' AFTER `useragent`;

-- 1153
UPDATE testcasecountryproperties SET `type` = 'text' where type='Unknown';

-- 1154
INSERT INTO `invariant`
  VALUES ('USERAGENT', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36', 100, 'Chrome Generic Win10', '', '', '', '', '', '', '', '', '', '')
  ,('USERAGENT', 'Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36', 100, 'Chrome Generic Win7', '', '', '', '', '', '', '', '', '', '')
  ,('USERAGENT', 'Mozilla/5.0 (Linux; Android 6.0.1; SM-G920V Build/MMB29K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.98 Mobile Safari/537.36', 100, 'Samsung Galaxy S6', '', '', '', '', '', '', '', '', '', '')
  ,('USERAGENT', 'Mozilla/5.0 (Linux; Android 6.0.1; Nexus 6P Build/MMB29P) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.83 Mobile Safari/537.36', 100, 'Nexus 6P', '', '', '', '', '', '', '', '', '', '')
  ,('USERAGENT', 'Mozilla/5.0 (Linux; Android 5.0.2; SAMSUNG SM-T550 Build/LRX22G) AppleWebKit/537.36 (KHTML, like Gecko) SamsungBrowser/3.3 Chrome/38.0.2125.102 Safari/537.36', 100, 'Samsung Galaxy Tab A', '', '', '', '', '', '', '', '', '', '')
  ,('USERAGENT', 'Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)', 100, 'Google bot', '', '', '', '', '', '', '', '', '', '')
  ,('USERAGENT', 'Mozilla/5.0 (compatible; bingbot/2.0; +http://www.bing.com/bingbot.htm)', 100, 'Bing bot', '', '', '', '', '', '', '', '', '', '')
  ,('USERAGENT', 'Mozilla/5.0 (iPhone; CPU iPhone OS 5_0 like Mac OS X) AppleWebKit/534.46 (KHTML, like Gecko) Version/5.1 Mobile/9A334 Safari/7534.48.3', 100, 'iPhone', '', '', '', '', '', '', '', '', '', '')
  ,('USERAGENT', 'Mozilla/5.0 (iPad; CPU OS 5_0 like Mac OS X) AppleWebKit/534.46 (KHTML, like Gecko) Version/5.1 Mobile/9A334 Safari/7534.48.3', 100, 'iPad', '', '', '', '', '', '', '', '', '', '')
  ,('INVARIANTPUBLIC', 'USERAGENT', '600', '', '', '', '', '', '', '', '', '', '', '');

-- 1155
DELETE from parameter where param='cerberus_homepage_nbbuildhistorydetail';

-- 1156
INSERT INTO usergroup Select Login, 'Label' from usergroup where GroupName = 'Test';

-- 1157
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`)
  VALUES ('USERGROUP', 'Label', '160', 'Can Create, update and delete Labels.', '');

-- 1158
INSERT INTO usergroup Select Login, 'TestStepLibrary' from usergroup where GroupName = 'Test';

-- 1159
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`)
  VALUES ('USERGROUP', 'TestStepLibrary', '115', 'Can modify Step Library and flag Step as Library.', '');

-- 1160
UPDATE `parameter` SET value=replace(value,'style="color: yellow"','') where param='cerberus_support_email';

-- 1161
ALTER TABLE `testcaseexecutionqueue` ADD COLUMN `ScreenSize` VARCHAR(45) NULL DEFAULT NULL AFTER `Platform`;

-- 1162
DELETE from parameter where param='cerberus_executiondetail_use';

-- 1163
UPDATE test SET TDateCrea = '1970-01-01 01:01:01' where TDateCrea = '1970-01-01 01:01:01';

-- 1164
UPDATE `invariant` SET sort = sort*10 where idname='CONTROL' and sort < 1500 and sort > 10;

-- 1165
UPDATE invariant SET sort = sort*10 where idname='ACTION' and sort <= 2500 and sort > 10;

-- 1166
UPDATE `invariant` SET `sort`='4000' WHERE `idname`='ACTION' and`value`='mouseOver';

-- 1167
UPDATE `invariant` SET `sort`='4100' WHERE `idname`='ACTION' and`value`='mouseOverAndWait';

-- 1168
INSERT INTO `parameter`
  VALUES ('','cerberus_testcase_defaultselectedcountry','ALL','Parameter that define the default list of countries selected when creating a new testcase. \'ALL\' select all countries. Leave the parameter empty to select none. You can also specify a list of countries separated by \',\' in order to select some.');

-- 1169
ALTER TABLE `testcaseexecution` DROP FOREIGN KEY `FK_testcaseexecution_02`, DROP FOREIGN KEY `FK_testcaseexecution_01`;

-- 1170
ALTER TABLE `testcaseexecution` ADD COLUMN `QueueID` BIGINT(20) NULL DEFAULT NULL AFTER `ManualExecution`;

-- 1171
ALTER TABLE `testcaseexecutionqueue` ADD COLUMN `ExeID` BIGINT(20) NULL DEFAULT NULL AFTER `State`;

-- 1172
ALTER TABLE `testcaseexecution` DROP COLUMN `Verbose`, DROP COLUMN `Finished`,  ADD COLUMN `UsrCreated` VARCHAR(45) NOT NULL DEFAULT '' AFTER `QueueID`, ADD COLUMN `DateCreated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER `UsrCreated`, ADD COLUMN `UsrModif` VARCHAR(45) NULL DEFAULT '' AFTER `DateCreated`, ADD COLUMN `DateModif` TIMESTAMP NOT NULL DEFAULT '1970-01-01 01:01:01' AFTER `UsrModif` ;

-- 1173
ALTER TABLE `testcaseexecutionqueue` DROP COLUMN `OutputFormat`, DROP COLUMN `Synchroneous`,  CHANGE COLUMN `manualexecution` `manualexecution` VARCHAR(1) NOT NULL DEFAULT 'N' AFTER `SeleniumLog`,  CHANGE COLUMN `retries` `retries` TINYINT(1) NOT NULL DEFAULT '0' AFTER `manualexecution`,  CHANGE COLUMN `State` `State` VARCHAR(9) NOT NULL DEFAULT 'WAITING' AFTER `RequestDate`,  ADD COLUMN `UsrCreated` VARCHAR(45) NOT NULL DEFAULT '' AFTER `ExeID`, ADD COLUMN `DateCreated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER `UsrCreated`, ADD COLUMN `UsrModif` VARCHAR(45) NULL DEFAULT '' AFTER `DateCreated`, ADD COLUMN `DateModif` TIMESTAMP NOT NULL DEFAULT '1970-01-01 01:01:01'  AFTER `UsrModif` ;

-- 1174
select 1 from DUAL;

-- 1175
select 1 from DUAL;

-- 1176
ALTER TABLE `testcaseexecutionqueue` DROP FOREIGN KEY `FK_testcaseexecutionqueue_01`;

-- 1177
ALTER TABLE `testcaseexecutionqueue` ADD INDEX `IX_testcaseexecution_03` (`DateCreated` ASC);

-- 1178
select 1 from DUAL;

-- 1179
select 1 from DUAL;

-- 1180
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`)
  VALUES ('ACTION', 'openApp', '6600', 'Open Application', '')
  ,('ACTION', 'closeApp', '6700', 'Close Application', '')
  ,('ACTION', 'waitVanish', '16500', 'Wait for an element that disapear', '');

-- 1181
INSERT INTO `invariant`
  VALUES ('MANUALURL', '0', 100, 'Do not activate Application URL Manual definition', '', '', '', '', '', '', '', '', '', '')
  ,('MANUALURL', '1', 200, 'Activate Application URL Manual definition', '', '', '', '', '', '', '', '', '', '')
  ,('INVARIANTPUBLIC', 'MANUALURL', '650', '', '', '', '', '', '', '', '', '', '', '');

-- 1182
INSERT INTO `parameter`
  VALUES ('','cerberus_queueexecution_timeout','600000','Parameter that define the time cerberus will wait in ms when triggering an execution from the queue.');

-- 1183
ALTER TABLE `testcaseexecutionqueue` ADD INDEX `IX_testcaseexecution_04` (`State` ASC);

-- 1184
ALTER TABLE `robot` ADD COLUMN `poolsize` INT(11) NOT NULL DEFAULT 0 AFTER `description`;

-- 1185
ALTER TABLE `appservice` CHANGE COLUMN `Method` `Method` VARCHAR(255) NOT NULL DEFAULT '';

-- 1186
ALTER TABLE `myversion` ADD COLUMN `ValueString` VARCHAR(200) NULL DEFAULT NULL AFTER `Value`;

-- 1187
INSERT INTO `myversion` (`Key`, `ValueString`)
  VALUES ('queueprocessingjobrunning', 'N');

-- 1188
INSERT INTO `myversion` (`Key`)
  VALUES ('queueprocessingjobstart');

-- 1189
UPDATE `invariant` SET `idname`='INVARIANTPRIVATE' WHERE `idname`='INVARIANTPUBLIC' and`value`='MANUALURL';

-- 1190
INSERT INTO `invariant`
  VALUES ('ROBOTHOST', 'localhost', 100, 'Localhost Robot', '', '10', '', '', '', '', '', '', '', '')
  ,('INVARIANTPUBLIC', 'ROBOTHOST', '650', '', '', '', '', '', '', '', '', '', '', '');

-- 1191
INSERT INTO `parameter`
  VALUES ('','cerberus_queueexecution_defaultrobothost_threadpoolsize','10','Default number of simultaneous execution allowed for Robot host constrain (only used when host entry does not exist in invariant table).');

-- 1192
UPDATE `parameter` SET `param`='cerberus_queueexecution_global_threadpoolsize' WHERE `param`='cerberus_execution_threadpool_size';

-- 1193
select 1 from DUAL;

-- 1194
select 1 from DUAL;

-- 1195
ALTER TABLE `testcaseexecutionqueue`  CHANGE COLUMN `State` `State` VARCHAR(9) NOT NULL DEFAULT 'QUEUED' ,ADD COLUMN `Priority` INT DEFAULT 1000 AFTER `State`, ADD COLUMN `DebugFlag` VARCHAR(1) NULL DEFAULT 'N' AFTER `comment`;

-- 1196
ALTER TABLE `robot` DROP COLUMN `poolsize`;

-- 1197
select 1 from DUAL;

-- 1198
select 1 from DUAL;

-- 1199
INSERT INTO `invariant`
  VALUES ('QUEUEDEBUGFLAG', 'N', 100, 'No debug message.', '', '', '', '', '', '', '', '', '', '')
  ,('QUEUEDEBUGFLAG', 'Y', 200, 'Activate debug message.', '', '', '', '', '', '', '', '', '', '')
  ,('INVARIANTPRIVATE', 'QUEUEDEBUGFLAG', '650', '', '', '', '', '', '', '', '', '', '', '');

-- 1200
INSERT INTO `parameter`
  VALUES ('','cerberus_queueexecution_enable','Y','Activation boolean in order to activate the job that process the execution queue.Y value will activate the job. N will stop it, leaving all the executions in QUEUED State.');

-- 1201
select 1 from DUAL;

-- 1202
select 1 from DUAL;

-- 1203
ALTER TABLE `user` CHANGE COLUMN `UserPreferences` `UserPreferences` LONGTEXT NOT NULL ;

-- 1204
CREATE TABLE `tag` (  `id` INT NOT NULL,  `Tag` VARCHAR(50) NOT NULL DEFAULT '',  `Description` VARCHAR(300) NOT NULL DEFAULT '',  `Campaign` VARCHAR(45) NULL DEFAULT NULL,  `UsrCreated` VARCHAR(45) NOT NULL DEFAULT '',  `DateCreated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,  `UsrModif` VARCHAR(45) NULL DEFAULT '',  `DateModif` TIMESTAMP NOT NULL DEFAULT '1970-01-01 01:01:01',  PRIMARY KEY (`id`),  INDEX `IX_tag_01` (`Tag` ASC),  INDEX `IX_tag_02` (`Campaign` ASC));

-- 1205
ALTER TABLE `invariant`  ADD COLUMN `UsrCreated` VARCHAR(45) NOT NULL DEFAULT '' AFTER `gp9`, ADD COLUMN `DateCreated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER `UsrCreated`, ADD COLUMN `UsrModif` VARCHAR(45) NULL DEFAULT '' AFTER `DateCreated`, ADD COLUMN `DateModif` TIMESTAMP NOT NULL DEFAULT '1970-01-01 01:01:01' AFTER `UsrModif` ;

-- 1206
select 1 from DUAL;

-- 1207
select 1 from DUAL;

-- 1208
ALTER TABLE `documentation` ADD COLUMN `DocAnchor` VARCHAR(300) NULL DEFAULT NULL AFTER `DocDesc`;

-- 1209
select 1 from DUAL;

-- 1210
select 1 from DUAL;

-- 1211
ALTER TABLE `tag` CHANGE COLUMN `id` `id` INT(11) NOT NULL AUTO_INCREMENT ;

-- 1212
ALTER TABLE `tag` DROP INDEX `IX_tag_01` , ADD UNIQUE INDEX `IX_tag_01` (`Tag` ASC);

-- 1213
INSERT INTO `tag` (`Tag`) (select distinct tag from testcaseexecution a where tag != '') ON DUPLICATE KEY UPDATE Tag=a.tag;

-- 1214
INSERT INTO `tag` (`Tag`) (select distinct tag from testcaseexecutionqueue a where tag != '') ON DUPLICATE KEY UPDATE Tag=a.tag;

-- 1215
DROP TABLE `tag`;

-- 1216
CREATE TABLE `tag` (  `id` INT(11) NOT NULL AUTO_INCREMENT,  `Tag` VARCHAR(50) NOT NULL DEFAULT '',  `Description` VARCHAR(300) NOT NULL DEFAULT '',  `Campaign` VARCHAR(45) NULL DEFAULT NULL,  `UsrCreated` VARCHAR(45) NOT NULL DEFAULT '',  `DateCreated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,  `UsrModif` VARCHAR(45) NULL DEFAULT '',  `DateModif` TIMESTAMP NOT NULL DEFAULT '1970-01-01 01:01:01',  PRIMARY KEY (`id`),  UNIQUE INDEX `IX_tag_01` (`Tag` ASC),  INDEX `IX_tag_02` (`Campaign` ASC))
  ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- 1217
ALTER TABLE `tag` ADD CONSTRAINT `FK_tag_1` FOREIGN KEY (`Campaign`) REFERENCES `campaign` (`campaign`)  ON DELETE SET NULL  ON UPDATE CASCADE;

-- 1218
INSERT INTO `tag` (`Tag`) (select distinct tag from testcaseexecutionqueue a where tag != '')  ON DUPLICATE KEY UPDATE Tag=a.tag;

-- 1219
INSERT INTO `tag` (`Tag`) (select distinct tag from testcaseexecution a where tag != '')  ON DUPLICATE KEY UPDATE Tag=a.tag;

-- 1220
INSERT INTO `tag` (`Tag`) (select distinct tag from testcaseexecutionqueue a where tag != '')  ON DUPLICATE KEY UPDATE Tag=a.tag;

-- 1221
INSERT INTO `tag` (`Tag`) (select distinct tag from testcaseexecution a where tag != '')  ON DUPLICATE KEY UPDATE Tag=a.tag;

-- 1222
select 1 from DUAL;

-- 1223
select 1 from DUAL;

-- 1224
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `veryshortdesc`)
  VALUES ('LANGUAGE', 'fr', 200, 'Francais', 'Francais');

-- 1225
ALTER TABLE `tag` CHANGE COLUMN `Tag` `Tag` VARCHAR(255) NOT NULL DEFAULT '' ;

-- 1226
ALTER TABLE `testcaseexecution` CHANGE COLUMN `Tag` `Tag` VARCHAR(255) NULL DEFAULT NULL ;

-- 1227
ALTER TABLE `tag` ADD COLUMN `DateEndQueue` TIMESTAMP NOT NULL DEFAULT '1970-01-01 01:01:01' AFTER `Campaign`;

-- 1228
ALTER TABLE `campaign` ADD COLUMN `DistribList` TEXT NOT NULL AFTER `campaign`, ADD COLUMN `NotifyEndTagExecution` VARCHAR(5) NOT NULL DEFAULT 'N' AFTER `DistribList`;

-- 1229
ALTER TABLE `campaign` ADD COLUMN `NotifyStartTagExecution` VARCHAR(5) NOT NULL DEFAULT 'N' AFTER `DistribList` ;

-- 1230
INSERT INTO `parameter` (`system`, `param`, `value`, `description`)
  VALUES ('', 'cerberus_notification_tagexecutionstart_subject', '[Cerberus] Tag Execution %TAG% [%CAMPAIGN%] started.', 'Subject of Cerberus start of tag execution notification email. %TAG% and %CAMPAIGN% can be used as variables.')
  ,('', 'cerberus_notification_tagexecutionstart_body', 'Hello,<br><br>The Cerberus Tag Execution %TAG% from campaign %CAMPAIGN% has just started.<br><br>You can follow its execution <a href="%URLTAGREPORT%">here</a>.','Cerberus start of tag execution notification email body. %TAG%, %URLTAGREPORT% and %CAMPAIGN% can be used as variables.')
  ,('', 'cerberus_notification_tagexecutionstart_from','Cerberus <no.reply@cerberus-testing.org>', 'From field of Cerberus start of tag execution notification email.')
  ,('', 'cerberus_notification_tagexecutionend_subject', '[Cerberus] Tag Execution %TAG% [%CAMPAIGN%] finished.', 'Subject of Cerberus end of tag execution notification email. %TAG% and %CAMPAIGN% can be used as variables.')
  ,('', 'cerberus_notification_tagexecutionend_body', 'Hello,<br><br>The Cerberus Tag Execution %TAG% from campaign %CAMPAIGN% has just finished.<br><br>You can analyse the result <a href="%URLTAGREPORT%">here</a>.','Cerberus End of tag execution notification email body. %TAG%, %URLTAGREPORT% and %CAMPAIGN% can be used as variables.')
  ,('', 'cerberus_notification_tagexecutionend_from','Cerberus <no.reply@cerberus-testing.org>', 'From field of Cerberus end of tag execution notification email.');

-- 1231
select 1 from DUAL;

-- 1232
select 1 from DUAL;

-- 1233
INSERT INTO `testdatalibdata` (`TestDataLibID`, `SubData`, `Value`, `Column`, `ParsingAnswer`, `ColumnPosition`, `Description`)  SELECT a1.testdatalibid, '', '', '', '', '', '' FROM testdatalib a1  LEFT OUTER JOIN (   SELECT a.testdatalibid FROM testdatalib a JOIN testdatalibdata b ON a.testdatalibid = b.testdatalibid and b.subdata=''  ) as toto ON toto.testdatalibid = a1.testdatalibID WHERE toto.testdatalibid is null;

-- 1234
select 1 from DUAL;

-- 1235
select 1 from DUAL;

-- 1236
select 1 from DUAL;

-- 1237
select 1 from DUAL;

-- 1238
INSERT INTO `parameter` (`system`, `param`, `value`, `description`)  SELECT '', 'cerberus_loginpage_welcomemessagehtml', concat('If you don\'t have login, please contact ' , p.`value`) , 'Message that will appear in login page. %SUPPORTEMAIL% will be replaced by parameter cerberus_support_email.' FROM parameter p WHERE param = 'cerberus_support_email' and `system`='';

-- 1239
UPDATE `parameter` SET `value`='', `description`='Support Email for Cerberus.' WHERE `system`='' and`param`='cerberus_support_email';

-- 1240
DELETE FROM parameter where param in ('cerberus_mediastorage_url','cerberus_picture_testcase_path','cerberus_reporting_url','cerberus_selenium_firefoxextension_firebug','cerberus_selenium_firefoxextension_netexport','cerberus_testcase_function_booleanListOfFunction','cerberus_testcase_function_urlForListOfFunction','cerberus_testexecutiondetailpage_nbmaxexe','cerberus_testexecutiondetailpage_nbmaxexe_max','index_alert_subject','index_alert_from','index_alert_body','index_alert_to','index_notification_body_between','index_notification_body_end','index_notification_body_top','index_notification_subject','index_smtp_from','index_smtp_host','index_smtp_port','jenkins_application_pipeline_url','jenkins_deploy_pipeline_url','selenium_chromedriver_download_url','selenium_download_url','selenium_iedriver_download_url','solr_url','sonar_application_dashboard_url','ticketing tool_bugtracking_url','ticketing tool_newbugtracking_url','ticketing tool_ticketservice_url');

-- 1241
ALTER TABLE `testcasestep` CHANGE COLUMN `useStepStep` `useStepStep` INT(10) NULL DEFAULT NULL ;

-- 1242
UPDATE testcasestep set usestepstep = null where usestepstep < 0;

-- 1243
ALTER TABLE `testcasestep` CHANGE COLUMN `useStepTest` `useStepTest` VARCHAR(45) NULL DEFAULT NULL ,CHANGE COLUMN `useStepTestCase` `useStepTestCase` VARCHAR(45) NULL DEFAULT NULL ,CHANGE COLUMN `useStepStep` `useStepStep` INT(10) UNSIGNED NULL DEFAULT NULL ,ADD INDEX `IX_testcasestep_01` (`useStepTest` ASC, `useStepTestCase` ASC, `useStepStep` ASC);

-- 1244
UPDATE testcasestep set usesteptest = null where usesteptest='';

-- 1245
UPDATE testcasestep set usesteptestcase = null where usesteptestcase='';

-- 1246
UPDATE testcasestep set usestepstep = null where usesteptest is null;

-- 1247
UPDATE testcasestep c SET UsrModif='DatabaseVersioningService', useStep='N', useStepTest=null, useStepTestCase=null, useStepStep=null, DateModif = now()WHERE EXISTS ( Select 1 from (select a.test, a.testcase, a.step from testcasestep a left outer join testcasestep b on a.usesteptest=b.test and a.usesteptestcase=b.testcase and a.usestepstep=b.step where b.test is null and a.usesteptest is not null and a.usesteptest != '') as t where t.test=c.test and t.testcase=c.testcase and t.step=c.step and c.usesteptest is not null and c.usesteptest != '');

-- 1248
ALTER TABLE `testcasestep` ADD CONSTRAINT `FK_testcasestep_02`  FOREIGN KEY (`useStepTest` , `useStepTestCase`)  REFERENCES `testcase` (`Test` , `TestCase` )  ON DELETE SET NULL  ON UPDATE CASCADE;

-- 1249
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`)
  VALUES   ('SRVMETHOD', 'DELETE', 300 , 'DELETE http method') 
  ,('SRVMETHOD', 'PUT', 400, 'PUT http method');

-- 1250
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`)
  VALUES   ('SRVMETHOD', 'PATCH', 500 , 'PATCH http method');

-- 1251
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`)
  VALUES   ('INVARIANTPRIVATE', 'CAMPAIGN_TCCRITERIA', 450 , '');

-- 1252
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`)
  VALUES   ('CAMPAIGN_TCCRITERIA', 'PRIORITY', 10 , '')  
  ,('CAMPAIGN_TCCRITERIA', 'STATUS', 20 , '')  
  ,('CAMPAIGN_TCCRITERIA', 'SYSTEM', 30 , '')  
  ,('CAMPAIGN_TCCRITERIA', 'APPLICATION', 40 , '');

-- 1253
INSERT INTO `parameter` (`system`,`param`, `value`, `description`)
  VALUES   ('','cerberus_testcase_maxreturn', '1000', 'Integer that correspond to the maximum of testcase that cerberus can return');

-- 1254
ALTER TABLE `robot` add column host_user varchar(255);

-- 1255
ALTER TABLE `robot` add column host_password varchar(255);

-- 1256
UPDATE `parameter` SET `param`='cerberus_campaign_maxtestcase', `description`='Integer that correspond to the maximum number of testcase that a Cerberus campaign can contain.' WHERE `system`='' and `param`='cerberus_testcase_maxreturn';

-- 1257
UPDATE parameter SET description='Cerberus End of tag execution notification email body. %TAG%, %URLTAGREPORT%, %CAMPAIGN%, %TAGDURATION%, %TAGSTART%, %TAGEND%, %TAGGLOBALSTATUS% and %TAGTCDETAIL% can be used as variables.', value=REPLACE(value, 'You can analyse the result', '<table><thead><tr style="background-color:#cad3f1; font-style:bold"><td>Start</td><td>End</td><td>Duration</td></tr></thead><tbody><tr><td>%TAGSTART%</td><td>%TAGEND%</td><td>%TAGDURATION% min</td></tr></tbody></table><br><br>Global Status : <br>%TAGGLOBALSTATUS%<br><br>Non OK TestCases : <br>%TAGTCDETAIL%<br><br>You can analyse the result') WHERE param='cerberus_notification_tagexecutionend_body';

-- 1258
select 1 from DUAL;

-- 1259
select 1 from DUAL;

-- 1260
update parameter set param = replace(param, 'integration_', 'cerberus_') where param like 'integration_%';

-- 1261
update parameter set param = replace(param, 'jenkins_', 'cerberus_jenkins') where param like 'jenkins_%';

-- 1262
update parameter set param = 'cerberus_appium_swipe_duration' where param = 'appium_swipeDuration';

-- 1263
update parameter set param = replace(param, 'CI_OK_', 'cerberus_ci_okcoef') where param like 'CI_OK%';

-- 1264
ALTER TABLE `label` ADD COLUMN `Type` VARCHAR(45) NOT NULL DEFAULT 'STICKER' AFTER `Label`;

-- 1265
INSERT INTO label (`system`,`Label`,`Type`,`Color`,`ParentLabel`, `Description`, `UsrCreated`) SELECT '', testbattery, 'BATTERY', '#CCCCCC', '', Description, 'DatabaseVersioningV1264' from testbattery ON DUPLICATE KEY UPDATE `UsrModif` = 'DatabaseVersioningV1264', DateModif = now();

-- 1266
INSERT INTO testcaselabel (`Test`,`TestCase`,`LabelId`, `UsrCreated`) SELECT Test, TestCase, l.id, 'DatabaseVersioningV1264' from testbatterycontent b  join label l where b.testbattery = l.Label  ON DUPLICATE KEY UPDATE `UsrModif` = 'DatabaseVersioningV1264', DateModif = now();

-- 1267
INSERT INTO campaignlabel (`campaign`,`LabelId`, `UsrCreated`) SELECT campaign, l.id, 'DatabaseVersioningV1264' from campaigncontent b  join label l where b.testbattery = l.Label  ON DUPLICATE KEY UPDATE `UsrModif` = 'DatabaseVersioningV1264', DateModif = now();

-- 1268
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`)
  VALUES ('LABELTYPE', 'STICKER', 100, 'Sticker.')
  ,('LABELTYPE', 'BATTERY', 200, 'Battery.')
  ,('LABELTYPE', 'REQUIREMENT', 300, 'Requirement.')
  ,('INVARIANTPRIVATE', 'LABELTYPE', 700, '');

-- 1269
INSERT INTO `parameter` (`system`,`param`, `value`, `description`)
  VALUES   ('','cerberus_testdatalibcsv_path', '/opt/CerberusMedias/csvdata/', 'Default path for the csv file location');

-- 1270
ALTER TABLE `label` ADD COLUMN `ReqType` VARCHAR(100) NOT NULL DEFAULT '' AFTER `ParentLabel`,ADD COLUMN `ReqStatus` VARCHAR(100) NOT NULL DEFAULT '' AFTER `ReqType`,ADD COLUMN `ReqCriticity` VARCHAR(100) NOT NULL DEFAULT '' AFTER `ReqStatus`,ADD COLUMN `LongDesc` TEXT NOT NULL AFTER `Description`;

-- 1271
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`)
  VALUES   ('REQUIREMENTTYPE', 'Unknown', 100, '')
  ,('REQUIREMENTTYPE', 'Ergonomy', 110, '')
  ,('REQUIREMENTTYPE', 'Evolutivity', 120, '')
  ,('REQUIREMENTTYPE', 'Functional', 130, '')
  ,('REQUIREMENTTYPE', 'Internationalization', 140, '')
  ,('REQUIREMENTTYPE', 'Legal', 150, '')
  ,('REQUIREMENTTYPE', 'Maintenance', 160, '')
  ,('REQUIREMENTTYPE', 'Operation', 170, '')
  ,('REQUIREMENTTYPE', 'Portability', 180, '')
  ,('REQUIREMENTTYPE', 'Performance', 190, '')
  ,('REQUIREMENTTYPE', 'Scalability', 200, '')
  ,('REQUIREMENTTYPE', 'Security', 210, '')
  ,('REQUIREMENTSTATUS', 'Unknown', 100, '')
  ,('REQUIREMENTSTATUS', 'Approved', 200, '')
  ,('REQUIREMENTSTATUS', 'In Progress', 300, '')
  ,('REQUIREMENTSTATUS', 'Verified', 400, '')
  ,('REQUIREMENTSTATUS', 'Obsolete', 500, '')
  ,('REQUIREMENTCRITICITY', 'Unknown', 100, '')
  ,('REQUIREMENTCRITICITY', 'Low', 200, '')
  ,('REQUIREMENTCRITICITY', 'Medium', 300, '')
  ,('REQUIREMENTCRITICITY', 'High', 400, '')
  ,('INVARIANTPUBLIC', 'REQUIREMENTTYPE', '700', '')
  ,('INVARIANTPUBLIC', 'REQUIREMENTSTATUS', '750', '')
  ,('INVARIANTPUBLIC', 'REQUIREMENTCRITICITY', '800', '');

-- 1272
select 1 from DUAL;

-- 1273
select 1 from DUAL;

-- 1274
select 1 from DUAL;

-- 1275
select 1 from DUAL;

-- 1276
INSERT INTO `parameter` (`system`,`param`, `value`, `description`)
  VALUES   ('','cerberus_exemanualmedia_path', '/opt/CerberusMedias/executions-manual/', 'Path to store the Cerberus Media files for Manual executions.');

-- 1277
UPDATE `parameter` SET `param`='cerberus_exeautomedia_path', `description`='Path to store the Cerberus Media files for Automatic executions (like Selenium Screenshot or SOAP requests and responses).' WHERE `param`='cerberus_mediastorage_path';

-- 1278
select 1 from DUAL;

-- 1279
select 1 from DUAL;

-- 1280
select 1 from DUAL;

-- 1281
select 1 from DUAL;

-- 1282
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`)
  VALUES   ('CAMPAIGNSTARTNOTIF', 'Y', 100, 'Yes')
  ,('CAMPAIGNSTARTNOTIF', 'N', 200, 'No')
  ,('CAMPAIGNENDNOTIF', 'Y', 100, 'Yes')
  ,('CAMPAIGNENDNOTIF', 'N', 200, 'No')
  ,('CAMPAIGNENDNOTIF', 'CIKO', 300, 'Only when Continuous Integration result is KO.')
  ,('INVARIANTPRIVATE', 'CAMPAIGNSTARTNOTIF', '750', '')
  ,('INVARIANTPRIVATE', 'CAMPAIGNENDNOTIF', '800', '');

-- 1283
UPDATE `parameter` SET  description='Cerberus End of tag execution notification email body. %TAG%, %URLTAGREPORT%, %CAMPAIGN%, %TAGDURATION%, %TAGSTART%, %TAGEND%, %CIRESULT%, %CISCORE%, %TAGGLOBALSTATUS% and %TAGTCDETAIL% can be used as variables.' , value=replace(value,'%TAGDURATION% min</td></tr></tbody></table>','%TAGDURATION% min</td></tr></tbody></table><table><thead><tr style="background-color:#cad3f1; font-style:bold"><td>CI Result</td><td>CI Score</td></tr></thead><tbody><tr><td>%CIRESULT%</td><td>%CISCORE%</td></tr></tbody></table>')  where param='cerberus_notification_tagexecutionend_body';

-- 1284
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) 
  VALUES ('INVARIANTPRIVATE','FILETYPE', '710','All type of file', 'file type');

-- 1285
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`)
  VALUES ('FILETYPE', 'PNG', '6600', '', '')
  ,('FILETYPE', 'JPG', '6700', '', '')
  ,('FILETYPE', 'XML', '16500', '', '')
  ,('FILETYPE', 'JSON', '18500', '', '')
  ,('FILETYPE', 'TXT', '22500', '', '');

-- 1286
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`)
  VALUES ('FILETYPE', 'PDF', '23500', '', '')
  ,('FILETYPE', 'BIN', '24500', '', '');

-- 1287
ALTER TABLE testcasecountryproperties MODIFY COLUMN Length text;

-- 1288
UPDATE testcasestepaction a1 SET value2=value1, value1='', last_modified = now() WHERE EXISTS ( select 1 from (select a.test, a.testcase, a.step, a.sequence, a.value1, a.value2, a.last_modified from testcasestepaction a join testcase t on t.test=a.test and t.testcase=a.testcase join application ap on ap.application=t.application where ap.type in ('APK', 'IPA') and Action = 'keyPress') as t where t.test=a1.test and t.testcase=a1.testcase and t.step=a1.step and t.sequence=a1.sequence);

-- 1289
ALTER TABLE testcaseexecutiondata ADD COLUMN `System` varchar(45) NOT NULL DEFAULT ' ' AFTER `index`, ADD COLUMN `Environment` varchar(45) NOT NULL DEFAULT ' ' AFTER `System`, ADD COLUMN `Country` varchar(45) NOT NULL DEFAULT ' ' AFTER `Environment`, ADD COLUMN `LengthInit` text AFTER `Value2`, ADD COLUMN `JsonResult` text AFTER `value`, ADD COLUMN `DataLib` varchar(45) NOT NULL DEFAULT ' ' AFTER `JsonResult`, MODIFY Length TEXT;

-- 1290
ALTER TABLE `testcasestepexecution`  CHANGE COLUMN `ReturnMessage` `ReturnMessage` TEXT ;

-- 1291
ALTER TABLE `testcaseexecutiondata` ADD COLUMN `FromCache` VARCHAR(45) NULL DEFAULT 'N' AFTER `JsonResult`, ADD INDEX `IX_testcaseexecutiondata_03` (`System` ASC, `Environment` ASC, `Country` ASC, `FromCache` ASC, `Property` ASC, `Index` ASC, `Start` ASC);

-- 1292
ALTER TABLE `testcasecountryproperties` ADD COLUMN `CacheExpire` INT NULL DEFAULT 0 AFTER `Nature`;

-- 1293
DROP TABLE `abonnement`, `qualitynonconformities`, `qualitynonconformitiesimpact`;

-- 1294
DROP TABLE `testbatterycontent`, `campaigncontent`, `testbattery`;

-- 1295
ALTER TABLE `testcaseexecution` CHANGE COLUMN `Executor` `Executor` VARCHAR(255) NULL DEFAULT NULL ,CHANGE COLUMN `UsrCreated` `UsrCreated` VARCHAR(255) NOT NULL DEFAULT '' ,CHANGE COLUMN `UsrModif` `UsrModif` VARCHAR(255) NULL DEFAULT '' ;

-- 1296
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`)
  VALUES ('ACTION', 'executeJS', '6550', 'Execute Javascript', 'Execute JS');

-- 1297
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`)
  VALUES   ('CAMPAIGN_TCCRITERIA', 'GROUP', 100 , '');

-- 1298
ALTER TABLE testcaseexecutiondata MODIFY COLUMN datalib VARCHAR(200);

-- 1299
ALTER TABLE testcase ADD COLUMN TestCaseVersion int(10) DEFAULT 0 AFTER `screensize`;

-- 1300
ALTER TABLE testcaseexecution ADD COLUMN TestCaseVersion int(10) DEFAULT 0 AFTER `QueueID`;

-- 1301
ALTER TABLE `robot` CHANGE COLUMN `host_user` `host_user` VARCHAR(255) NULL DEFAULT NULL AFTER `Port`,CHANGE COLUMN `host_password` `host_password` VARCHAR(255) NULL DEFAULT NULL AFTER `host_user`,ADD COLUMN `robotdecli` VARCHAR(100) NOT NULL DEFAULT '' AFTER `screensize`;

-- 1302
ALTER TABLE `testcaseexecution` ADD COLUMN `System` VARCHAR(45) NOT NULL DEFAULT '' AFTER `ID`,ADD COLUMN `RobotDecli` VARCHAR(100) NOT NULL DEFAULT '' AFTER `Country`;

-- 1303
UPDATE testcaseexecution SET robotdecli=browser;

-- 1304
ALTER TABLE `testcaseexecution` DROP INDEX `IX_testcaseexecution_04` ,ADD INDEX `IX_testcaseexecution_04` (`Test` ASC, `TestCase` ASC, `Country` ASC, `RobotDecli` ASC, `Start` ASC, `ControlStatus` ASC),DROP INDEX `IX_testcaseexecution_05` ,ADD INDEX `IX_testcaseexecution_05` (`System` ASC),DROP INDEX `IX_testcaseexecution_06` ;

-- 1305
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`)
  VALUES ('CAMPAIGN_PARAMETER', 'ROBOT', '40', 'Robot used for execution.', 'Robot');

-- 1306
UPDATE invariant set gp1=null, gp2=null, gp3=null WHERE idname in ('CAMPAIGN_PARAMETER', 'ACTIONCONDITIONOPER', 'ACTION', 'APPLITYPE', 'APPSERVICECONTENTACT', 'APPSERVICEHEADERACT', 'CONTROLCONDITIONOPER', 'INVARIANTPRIVATE', 'OUTPUTFORMAT', 'STEPLOOP', 'STEPCONDITIONOPER', 'SRVTYPE', 'SRVMETHOD', 'TESTCASECONDITIONOPER', 'VERBOSE');

-- 1307
DELETE FROM invariant WHERE idname in ('MNTACTIVE','NCONFSTATUS','PROBLEMCATEGORY','PROPERTYBAM','RESPONSABILITY','ROOTCAUSECATEGORY','SEVERITY');

-- 1308
select 1 from DUAL;

-- 1309
select 1 from DUAL;

-- 1310
DELETE FROM invariant where idname='ACTION' and value in ('getPageSource');

-- 1311
UPDATE `invariant` SET `sort`='99999', `description`='[DEPRECATED] Remove differences from the given pattern' WHERE `idname`='ACTION' and`value`='removeDifference';

-- 1312
UPDATE `invariant` SET `sort`='99999', `description`='[DEPRECATED] mouseOverAndWait' WHERE `idname`='ACTION' and`value`='mouseOverAndWait';

-- 1313
UPDATE `invariant` SET `value`='getFromSql' WHERE `idname`='PROPERTYTYPE' and`value`='executeSql';

-- 1314
UPDATE testcasecountryproperties set type = 'getFromSql' where type = 'executeSql';

-- 1315
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`)
  VALUES ('ACTIONCONDITIONOPER', 'ifElementNotPresent', 260, 'Only execute if Element is not present.')
  ,('STEPCONDITIONOPER', 'ifElementNotPresent', 260, 'Only execute if Element is not present.')
  ,('CONTROLCONDITIONOPER', 'ifElementNotPresent', 260, 'Only execute if Element is not present.');

-- 1316
ALTER TABLE `appservice` DROP FOREIGN KEY `FK_appservice_01`;

-- 1317
ALTER TABLE `appservice` ADD CONSTRAINT `FK_appservice_01` FOREIGN KEY (`Application`) REFERENCES `application` (`Application`) ON DELETE SET NULL ON UPDATE CASCADE;

-- 1318
ALTER TABLE `testdatalib` DROP FOREIGN KEY `FK_testdatalib_01`;

-- 1319
ALTER TABLE `testdatalib` ADD CONSTRAINT `FK_testdatalib_01` FOREIGN KEY (`Service`) REFERENCES `appservice` (`Service`) ON DELETE SET NULL ON UPDATE CASCADE;

-- 1320
CREATE TABLE `interactive_tuto` ( id int primary key,titleTranslationLabel varchar(61) not null,translationLabel varchar(61) not null,role varchar(31) not null,ord int not null,level int not null);

-- 1321
CREATE TABLE `interactive_tuto_step` ( id int AUTO_INCREMENT,id_interactive_tuto int not null,selector varchar(255),step_order int,type varchar(31) not null,attr1 varchar(125),FOREIGN KEY (id_interactive_tuto) REFERENCES interactive_tuto(id),primary key (id));

-- 1322
select 1 from DUAL;

-- 1323
ALTER TABLE `documentation` MODIFY COLUMN DocLabel varchar(800);

-- 1324
select 1 from DUAL;

-- 1325
select 1 from DUAL;

-- 1326
select 1 from DUAL;

-- 1327
select 1 from DUAL;

-- 1328
INSERT INTO `invariant` (idname, value, sort, description)
  VALUES ('SRVTYPE', 'FTP', 300, 'FTP Service.');

-- 1329
ALTER TABLE `testcaseexecutionqueue` ADD COLUMN `RobotDecli` VARCHAR(100) NOT NULL DEFAULT '' AFTER `Robot`;

-- 1330
ALTER TABLE `testcaseexecutionqueue` ADD COLUMN `System` VARCHAR(45) NOT NULL DEFAULT '' AFTER `ID`;

-- 1331
UPDATE `parameter` SET `description`='URL to Cerberus used in order to trigger executions from the queue. This parameter is mandatory. ex : http://localhost:8080/Cerberus' WHERE `system`='' and`param`='cerberus_url';

-- 1332
INSERT INTO `parameter` (`system`, `param`, `value`, `description`)
  VALUES ('', 'cerberus_gui_url', '', 'URL to Cerberus used inside all GUI links and mail sent by Cerberus. This parameter is not mandatory and takes the value of cerberus_url in case empty. ex : http://localhost:8080/Cerberus');

-- 1333
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`)
  VALUES ('ACTION', 'executeCommand', '6551', 'Execute 1 command shell. Value1 is the command (ex : "grep"), Value2 is the arguments (ex : "-name toto")', 'Execute 1 command shell');

-- 1334
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`)
  VALUES ('ACTION', 'scrollTo', '13003', 'Scroll to element or text', 'Scroll to element or text');

-- 1335
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`)
  VALUES ('MANUALEXECUTION', 'A', '10', 'Determined from Test Case Group value.', '');

-- 1336
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`)
  VALUES  ('MNTACTIVE', 'N', '10', '', ''), ('MNTACTIVE', 'Y', '20', '', ''), ('INVARIANTPRIVATE','MNTACTIVE', '810','Maintenance Activation flag', '');

-- 1337
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`)
  VALUES ('PROPERTYTYPE', 'getFromCommand', '80', 'Getting value from a Shell command', '');

-- 1338
ALTER TABLE `testcaseexecution` CHANGE COLUMN `ControlMessage` `ControlMessage` TEXT NULL DEFAULT NULL ;

-- 1339
INSERT INTO test (test, description, active, automated)
  VALUES ('Post Testing', 'Post Tests', 'Y', 'Y') ON DUPLICATE KEY UPDATE test = 'Post Testing';

-- 1340
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`)
  VALUES ('ACTION', 'installApp', '13005', 'Install mobile application', '')
  ,('ACTION', 'removeApp', '13006', 'Remove mobile application', '');

-- 1341
ALTER TABLE `countryenvironmentparameters` ADD column mobileActivity varchar(255);

-- 1342
ALTER TABLE `countryenvironmentparameters`  ADD column mobilePackage varchar(255);

-- 1343
ALTER TABLE `test` DROP COLUMN `Automated`,  CHANGE COLUMN `TDateCrea` `DateCreated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP , CHANGE COLUMN `last_modified` `DateModif` TIMESTAMP NOT NULL DEFAULT '1970-01-01 01:01:01' , ADD COLUMN `UsrCreated` VARCHAR(45) NOT NULL DEFAULT '' AFTER `Active`, ADD COLUMN `UsrModif` VARCHAR(45) NOT NULL DEFAULT '' AFTER `DateCreated`;

-- 1344
ALTER TABLE `appservice` ADD COLUMN FileName VARCHAR(100) DEFAULT NULL AFTER `ServicePath`;

-- 1345
INSERT INTO `parameter` (`system`, `param`, `value`, `description`)
  VALUES ('', 'cerberus_ftpfile_path', '/opt/CerberusMedias/ftpfiles/', 'Path to store local files which will be stored into ftpServer');

-- 1346
ALTER TABLE `testcasestep` ADD COLUMN `ForceExe` VARCHAR(1) NOT NULL DEFAULT 'N' AFTER `inlibrary`;

-- 1347
UPDATE testcasestep SET ForceExe='Y' where Description like '%FORCEDSTEP%';

-- 1348
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`)
  VALUES  ('STEPFORCEEXE', 'N', '10', '', ''), ('STEPFORCEEXE', 'Y', '20', '', ''), ('INVARIANTPRIVATE','STEPFORCEEXE', '820','Step Force Exe Flag.', '');

-- 1349
ALTER TABLE `testcaselabel` DROP FOREIGN KEY `FK_testcaselabel_02`;

-- 1350
ALTER TABLE `testcaselabel` ADD CONSTRAINT `FK_testcaselabel_02` FOREIGN KEY (`LabelId`)   REFERENCES `label` (`Id`) ON DELETE RESTRICT ON UPDATE CASCADE;

-- 1351
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`)
  VALUES ('ACTION', 'dragAndDrop', '7000', 'Drag an element and drop it to a specific target', '');

-- 1352
ALTER TABLE `tag` ADD COLUMN `nbExe` INT NULL DEFAULT 0 AFTER `DateEndQueue`, ADD COLUMN `nbExeUsefull` INT NULL DEFAULT 0 AFTER `nbExe`,ADD COLUMN `nbOK` INT NULL DEFAULT 0 AFTER `nbExeUsefull`, ADD COLUMN `nbKO` INT NULL DEFAULT 0 AFTER `nbOK`, ADD COLUMN `nbFA` INT NULL DEFAULT 0 AFTER `nbKO`, ADD COLUMN `nbNA` INT NULL DEFAULT 0 AFTER `nbFA`,ADD COLUMN `nbNE` INT NULL DEFAULT 0 AFTER `nbNA`, ADD COLUMN `nbWE` INT NULL DEFAULT 0 AFTER `nbNE`, ADD COLUMN `nbPE` INT NULL DEFAULT 0 AFTER `nbWE`, ADD COLUMN `nbQU` INT NULL DEFAULT 0 AFTER `nbPE`,ADD COLUMN `nbQE` INT NULL DEFAULT 0 AFTER `nbQU`, ADD COLUMN `nbCA` INT NULL DEFAULT 0 AFTER `nbQE`,ADD COLUMN `CIScore` INT NULL DEFAULT 0 AFTER `nbCA`, ADD COLUMN `CIScoreThreshold` INT NULL DEFAULT 0 AFTER `CIScore`, ADD COLUMN `CIResult` VARCHAR(45) NULL DEFAULT '' AFTER `CIScoreThreshold`;

-- 1353
UPDATE parameter SET value=CAST(value*100 AS SIGNED INTEGER), description = concat(description, " (integer)") where param like 'cerberus_ci%';

-- 1354
INSERT INTO `parameter` (`system`, `param`, `value`, `description`)
  VALUES ('', 'cerberus_ci_threshold', '100', 'Target integer value above which the result CI is KO.');

-- 1355
UPDATE parameter SET value=REPLACE(REPLACE(value, '<td>%CISCORE%</td>', '<td>%CISCORE%</td><td>%CISCORETHRESHOLD%</td>'), '<td>CI Score</td>', '<td>CI Score</td><td>CI Score Threshold</td>'), description = 'Cerberus End of tag execution notification email body. %TAG%, %URLTAGREPORT%, %CAMPAIGN%, %TAGDURATION%, %TAGSTART%, %TAGEND%, %CIRESULT%, %CISCORE%, %CISCORETHRESHOLD%, %TAGGLOBALSTATUS% and %TAGTCDETAIL% can be used as variables.' where param like 'cerberus_notification_tagexecutionend_body%';

-- 1356
CREATE TABLE `tagsystem` (  `Tag` VARCHAR(255) NOT NULL,  `System` VARCHAR(45) NOT NULL DEFAULT '',  `UsrCreated` VARCHAR(45) NOT NULL DEFAULT '',  `DateCreated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,  `UsrModif` VARCHAR(45) NULL DEFAULT '',  `DateModif` TIMESTAMP NOT NULL DEFAULT '1970-01-01 01:01:01',  PRIMARY KEY (`Tag`,`System`),  INDEX `IX_tagsystem_01` (`System` ASC))
  ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 1357
INSERT INTO `parameter` (`system`, `param`, `value`, `description`)
  VALUES ('', 'cerberus_robot_timeout', '60000', 'Timeout (in ms) for the robot to answer Cerberus requests.');

-- 1358
DELETE FROM `invariant` WHERE `idname`='CAMPAIGN_PARAMETER' and`value`='BROWSER';

-- 1359
INSERT INTO `parameter` (`system`, `param`, `value`, `description`)
  VALUES ('', 'cerberus_accept_unsigned_ssl_certificate', 'Y', 'Allow to use unsigned ssl protocol on REST service');

-- 1360
UPDATE label SET parentlabel=0 WHERE parentlabel='';

-- 1361
ALTER TABLE `label` CHANGE COLUMN `ParentLabel` `ParentLabelID` INT(11) NULL ;

-- 1362
SELECT 1 FROM dual;

-- 1363
UPDATE `invariant` set `description` = 'No Screenshots/Video' where `idname`='SCREENSHOT' and `value` = '0';

-- 1364
UPDATE `invariant` set `description` = 'Automatic Screenshots on error' where `idname`='SCREENSHOT' and `value` = '1';

-- 1365
UPDATE `invariant` set `description` = 'Systematic Screenshots' where `idname`='SCREENSHOT' and `value` = '2';

-- 1366
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`)  values ('SCREENSHOT', '3', 40, 'Automatic Screenshots/Video on error ', '')
  ,('SCREENSHOT', '4', 50, 'Systematic Screenshots/Video', '');

-- 1367
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) 
  VALUES ('INVARIANTPRIVATE', 'SELECTOR', '430', '', '')
  ,('SELECTOR', 'xpath=', '6550', '', '')
  ,('SELECTOR', 'id=', '6550', '', '')
  ,('SELECTOR', 'picture=', '6550', '', '')
  ,('SELECTOR', 'data-cerberus=', '6550', '', '');

-- 1368
CREATE TABLE `robotexecutor` (  `id` int(11) NOT NULL AUTO_INCREMENT,  `robot` varchar(100) NOT NULL,  `executor` varchar(100) NOT NULL,  `active` varchar(1) NOT NULL DEFAULT 'Y',  `rank` int(11) NOT NULL DEFAULT '10',  `host`	varchar(150),  `Port`	varchar(150),  `host_user`	varchar(255),  `host_password`	varchar(255),  `deviceUuid` varchar(255) NOT NULL,  `deviceName` varchar(255) NOT NULL,  `description` varchar(255) NOT NULL,  `DateLastExeSubmitted` BIGINT(20) NOT NULL DEFAULT 0,  `UsrCreated` varchar(45) NOT NULL DEFAULT '',  `DateCreated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,  `UsrModif` varchar(45) DEFAULT '',  `DateModif` timestamp NOT NULL DEFAULT '1970-01-01 01:01:01',  PRIMARY KEY (`id`),  UNIQUE KEY `IX_robotexecutor_01` (`robot`, `executor`),  CONSTRAINT `FK_robotexecutor_01` FOREIGN KEY (`robot`) REFERENCES `robot` (`robot`) ON DELETE CASCADE ON UPDATE CASCADE)
  ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- 1369
INSERT INTO `robotexecutor` (`robot`, `executor`, `active`, `rank`, `host`, `port`, `host_user`, `host_password`, `deviceuuid`, `devicename`, `description`)select robot, 'MAIN', 'Y', 1, host, port, host_user, host_password, '', '', '' from robot ON DUPLICATE KEY UPDATE description='';

-- 1370
ALTER TABLE `testcaseexecution` ADD COLUMN `robot` VARCHAR(100) NULL DEFAULT NULL AFTER `Country`, ADD COLUMN `robotexecutor` VARCHAR(100) NULL DEFAULT NULL AFTER `robot`, CHANGE COLUMN `IP` `RobotHost` VARCHAR(150) NULL DEFAULT NULL AFTER `robotexecutor`,CHANGE COLUMN `Port` `RobotPort` VARCHAR(150) NULL DEFAULT NULL AFTER `RobotHost`;

-- 1371
ALTER TABLE `testcaseexecutionqueue` ADD COLUMN `SelectedRobotHost` VARCHAR(150) NULL DEFAULT '' AFTER `DebugFlag`;

-- 1372
ALTER TABLE `robot` ADD COLUMN `lbexemethod` VARCHAR(45) NOT NULL DEFAULT '' AFTER `robotdecli`;

-- 1373
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`)
  VALUES  ('ROBOTLBMETHOD', 'BYRANKING', '20', '', ''), ('ROBOTLBMETHOD', 'ROUNDROBIN', '10', '', ''), ('INVARIANTPRIVATE','ROBOTLBMETHOD', '830','Load Balancing Rule.', ''), ('ROBOTEXECUTORACTIVE', 'N', '20', '', ''), ('ROBOTEXECUTORACTIVE', 'Y', '10', '', ''), ('INVARIANTPRIVATE','ROBOTEXECUTORACTIVE', '840','Activation flag for Robot Executor.', '');

-- 1374
ALTER TABLE `robotexecutor` ADD COLUMN `devicePort` int(8);

-- 1375
ALTER TABLE `robotexecutor` CHANGE COLUMN `deviceUuid` `deviceUdid` varchar(255) NOT NULL;

-- 1376
UPDATE `robotexecutor` r1 SET  `deviceUdid`= (SELECT value FROM robotcapability r2 where r1.robot = r2.robot and r2.capability = 'udid' union all select '' as value  limit 1) where `deviceUdid` is null or `deviceUdid` = '';

-- 1377
UPDATE `robotexecutor` r1 SET  `devicename`= (SELECT value FROM robotcapability r2 where r1.robot = r2.robot and r2.capability = 'deviceName' union all select '' as value  limit 1)  where `deviceName` is null or `deviceName` = '';

-- 1378
UPDATE `robotexecutor` r1 SET  `devicePort`= (SELECT value FROM robotcapability r2 where r1.robot = r2.robot and r2.capability = 'systemPort')  where `devicePort` is  null;

-- 1379
DELETE FROM `robotcapability` WHERE capability = 'udid' and value in (select  re.deviceUdid from `robotexecutor` re);

-- 1380
DELETE FROM `robotcapability` WHERE capability = 'deviceName' and value in (select  re.deviceName from `robotexecutor` re);

-- 1381
DELETE FROM `robotcapability` WHERE capability = 'systemPort' and value in (select  re.devicePort from `robotexecutor` re);

-- 1382
ALTER TABLE `testcasecountryproperties` ADD COLUMN `Rank` int(2) not null default 1;

-- 1383
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`)
  VALUES ('ACTIONCONDITIONOPER', 'ifTextInElement', 270, 'Only execute if text is present in element.')
  ,('ACTIONCONDITIONOPER', 'ifTextNotInElement', 280, 'Only execute if text is not present in element.')
  ,('STEPCONDITIONOPER', 'ifTextInElement', 270, 'Only execute if text is present in element.')
  ,('STEPCONDITIONOPER', 'ifTextNotInElement', 280, 'Only execute if text is not present in element.');

-- 1384
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`)
  VALUES ('MANUALURL', '2', 300, 'Activate Application URL Manual definition only on defined parameters.');

-- 1385
ALTER TABLE `testcaseexecutiondata` ADD COLUMN `Rank` int(2) not null default 1 AFTER `Type`;

-- 1386
INSERT INTO `parameter` (`system`, `param`, `value`, `description`)
  VALUES ('', 'cerberus_notification_tagexecutionend_tclistmax', '100', 'Limit the number of rows produced on testcase list detail table.');

-- 1387
UPDATE `parameter` SET value=replace(value,'<td>%CIRESULT%</td>','<td style="background-color:%CIRESULTCOLOR%; font-style:bold">%CIRESULT%</td>'), description = 'Cerberus End of tag execution notification email body. %TAG%, %URLTAGREPORT%, %CAMPAIGN%, %TAGDURATION%, %TAGSTART%, %TAGEND%, %CIRESULT%, %CIRESULTCOLOR%, %CISCORE%, %CISCORETHRESHOLD%, %TAGGLOBALSTATUS% and %TAGTCDETAIL% can be used as variables.' where param='cerberus_notification_tagexecutionend_body';

-- 1388
ALTER TABLE `campaign` ADD COLUMN `SlackNotifyStartTagExecution` VARCHAR(5) NULL DEFAULT 'N' AFTER `NotifyEndTagExecution`,ADD COLUMN `SlackNotifyEndTagExecution` VARCHAR(5) NULL DEFAULT 'N' AFTER `SlackNotifyStartTagExecution`,ADD COLUMN `SlackWebhook` VARCHAR(200) NOT NULL DEFAULT '' AFTER `SlackNotifyEndTagExecution`,ADD COLUMN `SlackChannel` VARCHAR(100) NOT NULL DEFAULT '' AFTER `SlackWebhook`,ADD COLUMN CIScoreThreshold VARCHAR(45) NULL DEFAULT NULL AFTER `SlackChannel`,ADD COLUMN Tag VARCHAR(255) NULL DEFAULT NULL AFTER `CIScoreThreshold`,ADD COLUMN Verbose VARCHAR(5) NULL DEFAULT NULL AFTER `Tag`,ADD COLUMN Screenshot VARCHAR(5) NULL DEFAULT NULL AFTER `Verbose`,ADD COLUMN PageSource VARCHAR(5) NULL DEFAULT NULL AFTER `Screenshot`,ADD COLUMN RobotLog VARCHAR(5) NULL DEFAULT NULL AFTER `PageSource`,ADD COLUMN Timeout VARCHAR(10) NULL DEFAULT NULL AFTER `RobotLog`,ADD COLUMN Retries VARCHAR(5) NULL DEFAULT NULL AFTER `Timeout`,ADD COLUMN Priority VARCHAR(45) NULL DEFAULT NULL  AFTER `Retries`,ADD COLUMN ManualExecution VARCHAR(1) NULL DEFAULT NULL AFTER `Priority`,ADD COLUMN LongDescription TEXT AFTER `Description`,ADD COLUMN `UsrCreated` VARCHAR(45) NOT NULL DEFAULT '' AFTER `LongDescription`,ADD COLUMN `DateCreated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER `UsrCreated`,ADD COLUMN `UsrModif` VARCHAR(45) DEFAULT '' AFTER `DateCreated`,ADD COLUMN `DateModif` timestamp NOT NULL DEFAULT '1970-01-01 01:01:01' AFTER `UsrModif`;

-- 1389
ALTER TABLE `robot` ADD COLUMN `type` VARCHAR(10) NOT NULL DEFAULT '' AFTER `robot`, DROP COLUMN `host`,DROP COLUMN `Port`,DROP COLUMN `host_user`,DROP COLUMN `host_password`,ADD COLUMN `UsrCreated` VARCHAR(45) NOT NULL DEFAULT '' AFTER `description`,ADD COLUMN `DateCreated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER `UsrCreated`,ADD COLUMN `UsrModif` VARCHAR(45) DEFAULT '' AFTER `DateCreated`,ADD COLUMN `DateModif` timestamp NOT NULL DEFAULT '1970-01-01 01:01:01' AFTER `UsrModif`;

-- 1390
INSERT INTO `parameter` (`system`, `param`, `value`, `description`)
  VALUES ('', 'cerberus_automaticqueuecancellationjob_period', '60', 'Period of time in minutes between each job that will cancel old queue entries that are still running.')
  ,('', 'cerberus_automaticqueuecancellationjob_active', 'Y', 'Y in order to activate the job that will cancel old queue entries that are still running.')
  ,('', 'cerberus_automaticqueuecancellationjob_timeout', '3600', 'Nb of Second after which a queue entry will be moved to CANCELLED state automaticly (3600 default).');

-- 1391
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`)
  VALUES ('CONTROLCONDITIONOPER', 'ifTextInElement', 270, 'Only execute if text is present in element.')
  ,('CONTROLCONDITIONOPER', 'ifTextNotInElement', 280, 'Only execute if text is not present in element.');

-- 1392
CREATE TABLE `testcaseexecutionqueuedep` (  `ID` bigint(20) NOT NULL AUTO_INCREMENT,  `ExeQueueID` bigint(20) unsigned NOT NULL,  `Environment` varchar(45) DEFAULT NULL,  `Country` varchar(45) DEFAULT NULL,  `Tag` varchar(255) DEFAULT NULL,  `Type` varchar(15) NOT NULL DEFAULT '',  `DepTest` varchar(45) NULL,  `DepTestCase` varchar(45) NULL,  `DepEvent` varchar(100) NULL,  `Status` varchar(15) NOT NULL DEFAULT 'WAITING',  `ReleaseDate` timestamp NOT NULL DEFAULT '1970-01-01 01:01:01',  `Comment` varchar(255) NOT NULL DEFAULT '',  `ExeID` bigint(20) unsigned NULL,  `UsrCreated` varchar(45) NOT NULL DEFAULT '',  `DateCreated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,  `UsrModif` varchar(45) NOT NULL DEFAULT '',  `DateModif` timestamp NOT NULL DEFAULT '1970-01-01 01:01:01',  PRIMARY KEY (`ID`),  UNIQUE KEY `IX_testcaseexecutiondep_01` (`ExeQueueID`,`Type`,`DepTest`,`DepTestCase`,`DepEvent`),  KEY `IX_testcaseexecutiondep_02` (`Status`,`Type`,`DepTest`,`DepTestCase`,`Tag`),  KEY `IX_testcaseexecutiondep_03` (`Status`,`Type`,`DepEvent`,`Tag`),  CONSTRAINT `FK_testcaseexecutiondep_01` FOREIGN KEY (`ExeQueueID`) REFERENCES `testcaseexecutionqueue` (`ID`) ON DELETE CASCADE ON UPDATE CASCADE)
  ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- 1393
CREATE TABLE `testcasedep` (  `ID` bigint(20) NOT NULL AUTO_INCREMENT,  `Test` varchar(45) NULL,  `TestCase` varchar(45) NULL,  `Type` varchar(15) NOT NULL DEFAULT '',  `DepTest` varchar(45) NULL,  `DepTestCase` varchar(45) NULL,  `DepEvent` varchar(100) NULL,  `Active` varchar(1) NOT NULL DEFAULT 'Y',  `Description` varchar(255) NOT NULL DEFAULT '',  `UsrCreated` varchar(45) NOT NULL DEFAULT '',  `DateCreated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,  `UsrModif` varchar(45) NOT NULL DEFAULT '',  `DateModif` timestamp NOT NULL DEFAULT '1970-01-01 01:01:01',  PRIMARY KEY (`ID`),  UNIQUE KEY `IX_testcasedep_01` (`Test`,`TestCase`,`Type`,`DepTest`,`DepTestCase`,`DepEvent`),  CONSTRAINT `FK_testcasedep_01` FOREIGN KEY (`Test`,`TestCase`) REFERENCES `testcase` (`Test`,`TestCase`) ON DELETE CASCADE ON UPDATE CASCADE,  CONSTRAINT `FK_testcasedep_02` FOREIGN KEY (`DepTest`,`DepTestCase`) REFERENCES `testcase` (`Test`,`TestCase`) ON DELETE CASCADE ON UPDATE CASCADE)
  ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- 1394
ALTER TABLE `testcaseexecutionqueuedep` DROP INDEX `IX_testcaseexecutiondep_02` , ADD INDEX `IX_testcaseexecutiondep_02` (`Status` ASC, `Type` ASC, `DepTest` ASC, `DepTestCase` ASC, `Tag` ASC, `Environment` ASC, `Country` ASC),DROP INDEX `IX_testcaseexecutiondep_03` , ADD INDEX `IX_testcaseexecutiondep_03` (`Status` ASC, `Type` ASC, `DepEvent` ASC, `Tag` ASC, `Environment` ASC, `Country` ASC),ADD INDEX `IX_testcaseexecutiondep_04` (`ExeID` ASC);

-- 1395
INSERT INTO `parameter` (`system`, `param`, `value`, `description`)
  VALUES ('', 'cerberus_accountcreation_systemlist', 'ALL', 'Either ALL, NONE or a coma separated list of system to create by default when a new user is created.')
  ,('', 'cerberus_accountcreation_ownsystemcreation', 'Y', 'Y in order to automatically create a personal system for the user. That system will be named US-login')
  ,('', 'cerberus_automaticqueueprocessingjob_period', '30', 'Period of time in minutes between each job that will process queue entries that are still in the queue (that job should automatically be submitted after the end of each execution).')
  ,('', 'cerberus_automaticqueueprocessingjob_active', 'Y', 'Y in order to activate the job that will process the queue entries that are still in the sueue.');

-- 1396
ALTER TABLE `robotexecutor`  MODIFY COLUMN `devicePort` int(8) AFTER `deviceName`,  ADD COLUMN `deviceLockUnlock` VARCHAR(1) NOT NULL DEFAULT 'N' AFTER `devicePort` ;

-- 1397
INSERT INTO `parameter` (`system`,`param`, `value`, `description`)
  VALUES ('','cerberus_selenium_autoscroll', 'N', 'Boolean (Y/N) that define if Cerberus automatically scroll the current page on any element it interact with.');

-- 1398
ALTER TABLE `logevent` CHANGE COLUMN `Login` `Login` VARCHAR(255) NOT NULL DEFAULT '' ;

-- 1399
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`)
  VALUES ('PROPERTYTYPE', 'getElementPosition', '36', 'Get an element position (format posx;posy)', '');

-- 1400
ALTER TABLE `testcaseexecution` ADD COLUMN `TestCasePriority` INT(1) UNSIGNED NOT NULL DEFAULT '0' AFTER `TestCaseVersion`;

-- 1401
ALTER TABLE `tag`  ADD COLUMN `CountryList` TEXT NULL AFTER `CIResult`, ADD COLUMN `EnvironmentList` TEXT NULL AFTER `CountryList`, ADD COLUMN `RobotDecliList` TEXT NULL AFTER `EnvironmentList`, ADD COLUMN `SystemList` TEXT NULL AFTER `RobotDecliList`, ADD COLUMN `ApplicationList` TEXT NULL AFTER `SystemList`, ADD COLUMN `ReqCountryList` TEXT NULL AFTER `ApplicationList`, ADD COLUMN `ReqEnvironmentList` TEXT NULL AFTER `ReqCountryList`;

-- 1402
UPDATE `parameter` SET description='Cerberus End of tag execution notification email body. %TAG%, %ENVIRONMENTLIST%, %COUNTRYLIST%, %APPLICATIONLIST%, %SYSTEMLIST%, %ROBOTDECLILIST%, %URLTAGREPORT%, %CAMPAIGN%, %TAGDURATION%, %TAGSTART%, %TAGEND%, %CIRESULT%, %CISCORE%, %CISCORETHRESHOLD%, %TAGGLOBALSTATUS% and %TAGTCDETAIL% can be used as variables.' , value=replace(replace(value,'%CISCORETHRESHOLD%</td>','%CISCORETHRESHOLD%</td><td>%ENVIRONMENTLIST%</td><td>%COUNTRYLIST%</td>'),'Threshold</td>','Threshold</td><td>Environments</td><td>Countries</td>') where param='cerberus_notification_tagexecutionend_body';

-- 1403
UPDATE `parameter` SET description='Cerberus start of tag execution notification email body. %TAG%, %REQENVIRONMENTLIST%, %REQCOUNTRYLIST%, %URLTAGREPORT% and %CAMPAIGN% can be used as variables.' , value=replace(value,'The Cerberus Tag Execution %TAG% from campaign %CAMPAIGN% has just started.','Tag <b>%TAG%</b> from campaign <b>%CAMPAIGN%</b> has just started for %REQENVIRONMENTLIST% on %REQCOUNTRYLIST%.') where param='cerberus_notification_tagexecutionstart_body';

-- 1404
INSERT INTO `parameter` (`system`, `param`, `value`, `description`)
  VALUES ('', 'cerberus_tagvariable_separator', '-', 'Text or character that will be used as separator on the list of environment and countries on variable %REQENVIRONMENTLIST% and %REQCOUNTRYLIST% at tag level.');

-- 1405
CREATE TABLE `scheduleentry` (  `ID` int(10) NOT NULL AUTO_INCREMENT,  `type` varchar(15) NOT NULL DEFAULT 'CAMPAIGN',  `name` varchar(100) NOT NULL,  `cronDefinition` varchar(45) NOT NULL,  `lastExecution` timestamp NOT NULL DEFAULT '1970-01-01 01:01:01',  `active` varchar(1) NOT NULL DEFAULT 'Y',  `UsrCreated` varchar(45) NOT NULL DEFAULT '',  `DateCreated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,  `UsrModif` varchar(45) NOT NULL DEFAULT '',  `DateModif` timestamp NOT NULL DEFAULT '1970-01-01 01:01:01',  PRIMARY KEY (`ID`))
  ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- 1406
CREATE TABLE `scheduledexecution` (  `ID` bigint(20) NOT NULL AUTO_INCREMENT,  `schedulerID` int(10) NOT NULL,  `scheduleName` varchar(100) DEFAULT NULL,  `scheduledDate` timestamp NOT NULL,  `scheduleFireTime` timestamp NOT NULL DEFAULT '1970-01-01 01:01:01',  `status` varchar(15) DEFAULT NULL,  `comment` varchar(250) DEFAULT NULL,  `UsrCreated` varchar(45) NOT NULL DEFAULT '',  `DateCreated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,  `UsrModif` varchar(45) NOT NULL DEFAULT '',  `DateModif` timestamp NOT NULL DEFAULT '1970-01-01 01:01:01',  PRIMARY KEY (`ID`),  UNIQUE KEY `IX_scheduledexecution_01` (`scheduledDate`,`schedulerId`),  CONSTRAINT `FK_scheduledexecution_01` FOREIGN KEY (`schedulerID`) REFERENCES `scheduleentry` (`ID`))
  ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- 1407
INSERT into myversion values('scheduler_version',0,'INIT');

-- 1408
ALTER TABLE `application` ADD COLUMN `poolSize` INT NULL AFTER `BugTrackerNewUrl`;

-- 1409
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`)
  VALUES ('ACTION', 'manageDialogKeypress', '5600', 'Keypress on a popup dialog.', 'Popup Keypress');

-- 1410
ALTER TABLE `robotexecutor` ADD COLUMN `executorExtensionPort` INT(8) NULL DEFAULT NULL AFTER `deviceLockUnlock`,ADD COLUMN `executorProxyHost` VARCHAR(255) NULL DEFAULT NULL AFTER `executorExtensionPort`,ADD COLUMN `executorProxyPort` INT(8) NULL DEFAULT NULL AFTER `executorProxyHost`,ADD COLUMN `executorProxyActive` VARCHAR(1) NOT NULL DEFAULT 'N' AFTER `executorProxyPort`;

-- 1411
ALTER TABLE `scheduledexecution` DROP FOREIGN KEY FK_scheduledexecution_01;

-- 1412
ALTER TABLE `scheduledexecution` ADD CONSTRAINT `FK_scheduledexecution_01` FOREIGN KEY (`schedulerID`) REFERENCES `scheduleentry` (`ID`) ON UPDATE CASCADE ON DELETE CASCADE;

-- 1413
ALTER TABLE `testcaseexecutionqueuedep` ADD COLUMN `QueueID` BIGINT(20) UNSIGNED NULL DEFAULT NULL AFTER `ExeID`, ADD INDEX `IX_testcaseexecutiondep_05` (`QueueID` ASC);

-- 1414
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`)
  VALUES ('ACTION', 'longPress', '3100', 'Long tap on element', 'longPress')
  ,('ACTION', 'clearField', '11500', 'Clear a Field', 'clearField');

-- 1415
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`)
  VALUES ('CONTROL', 'verifyStringNotContains', 1420, 'verifyStringNotContains.')
  ,('ACTIONCONDITIONOPER', 'ifStringNotContains', 445, 'Only execute if value1 does not contains value2.')
  ,('CONTROLCONDITIONOPER', 'ifStringNotContains', 445, 'Only execute if value1 does not contains value2.')
  ,('STEPCONDITIONOPER', 'ifStringNotContains', 445, 'Only execute if value1 does not contains value2.')
  ,('TESTCASECONDITIONOPER', 'ifStringNotContains', 445, 'Only execute if value1 does not contains value2.')
  ,('ACTIONCONDITIONOPER', 'ifPropertyNotExist', 210, 'Only execute if property does not exist for the execution.')
  ,('CONTROLCONDITIONOPER', 'ifPropertyNotExist', 210, 'Only execute if property does not exist for the execution.')
  ,('STEPCONDITIONOPER', 'ifPropertyNotExist', 210, 'Only execute if property does not exist for the execution.')
  ,('TESTCASECONDITIONOPER', 'ifPropertyNotExist', 210, 'Only execute if property does not exist for the execution.');

-- 1416
ALTER TABLE `user` CHANGE COLUMN `DefaultSystem` `DefaultSystem` MEDIUMTEXT NULL DEFAULT NULL ;

-- 1417
INSERT INTO `parameter` (`system`, `param`, `value`, `description`)
  VALUES ('', 'cerberus_smtp_isSetTls', 'true', 'Boolean defining if the TLS is set or not for the email!<br>true / false');

-- 1418
ALTER TABLE `testdatalib` ADD COLUMN `PrivateData` VARCHAR(1) NOT NULL DEFAULT 'N' AFTER `Country`;

-- 1419
ALTER TABLE `testdatalibdata` ADD COLUMN `Encrypt` VARCHAR(1) NOT NULL DEFAULT 'N' AFTER `SubData`;

-- 1420
INSERT INTO `parameter` (`system`, `param`, `value`, `description`)
  VALUES ('', 'cerberus_appium_action_longpress_wait', '8000', 'Integer value that correspond to the nb of ms of the longpress Appium action.');

-- 1421
ALTER TABLE `scheduledexecution` DROP FOREIGN KEY `FK_scheduledexecution_01`;

-- 1422
ALTER TABLE `testcaseexecution` DROP COLUMN `BrowserFullVersion`, ADD COLUMN `RobotProvider` VARCHAR(20) NOT NULL DEFAULT '' AFTER `RobotDecli`, ADD COLUMN `RobotSessionId` VARCHAR(100) NOT NULL DEFAULT '' AFTER `RobotProvider`;

-- 1423
INSERT INTO `parameter` (`system`, `param`, `value`, `description`)
  VALUES ('', 'cerberus_browserstack_defaultexename', 'Exe : %EXEID%', 'Define the default value for the name of the execution to be sent to Browserstack when a test is executed. Variable %EXEID% can be used.')
  ,('', 'cerberus_kobiton_defaultsessionname', '%EXEID% : %TEST% - %TESTCASE%', 'Define the default value for the SessionName to be sent to Kobiton when a test is executed. Variables %EXEID%, %APPLI%, %TAG%, %TEST%, %TESTCASE%, %TESTCASEDESC% can be used.')
  ,('', 'cerberus_kobiton_defaultsessiondescription', '%TESTCASEDESC%', 'Define the default value for the SessionDescription to be sent to Kobiton when a test is executed. Variables %EXEID%, %APPLI%, %TAG%, %TEST%, %TESTCASE%, %TESTCASEDESC% can be used.');

-- 1424
ALTER TABLE `tag` ADD COLUMN `browserstackBuildHash` VARCHAR(100) NOT NULL DEFAULT '' AFTER `ReqEnvironmentList`;

-- 1425
ALTER TABLE `scheduleentry` ADD COLUMN `description` VARCHAR(200) NOT NULL DEFAULT '' AFTER `active`;

-- 1426
TRUNCATE `scheduledexecution`;

-- 1427
ALTER TABLE `scheduledexecution` ADD UNIQUE INDEX `IX_scheduledexecution_02` (`scheduleName` ASC, `scheduleFireTime` ASC);

-- 1428
INSERT INTO invariant(idname, value, sort, description, VeryShortDesc)
  VALUES('ACTION', 'refreshCurrentPage', 6520, 'refresh current page', 'refresh current page');

-- 1429
ALTER TABLE testcasestepactioncontrol ADD Value3 TEXT after Value2;

-- 1430
ALTER TABLE testcasestepactioncontrolexecution ADD Value3 TEXT AFTER Value2, ADD Value3Init TEXT AFTER Value2Init;

-- 1431
ALTER TABLE testcase ADD ConditionVal3 TEXT AFTER ConditionVal2;

-- 1432
ALTER TABLE testcasestep ADD ConditionVal3 TEXT AFTER ConditionVal2;

-- 1433
ALTER TABLE testcasestepaction ADD Value3 TEXT AFTER Value2, ADD ConditionVal3 TEXT AFTER ConditionVal2;

-- 1434
ALTER TABLE testcasestepactioncontrol ADD ConditionVal3 TEXT AFTER ConditionVal2;

-- 1435
ALTER TABLE testcaseexecution ADD ConditionVal3Init TEXT AFTER ConditionVal2Init, ADD ConditionVal3 TEXT AFTER ConditionVal2;

-- 1436
ALTER TABLE testcasestepexecution ADD ConditionVal3Init TEXT AFTER ConditionVal2Init, ADD ConditionVal3 TEXT AFTER ConditionVal2;

-- 1437
ALTER TABLE testcasestepactionexecution ADD Value3 TEXT AFTER Value2, ADD Value3Init TEXT AFTER Value2Init, ADD ConditionVal3 TEXT AFTER ConditionVal2, ADD ConditionVal3Init TEXT AFTER ConditionVal2Init;

-- 1438
ALTER TABLE testcasestepactioncontrolexecution ADD ConditionVal3 TEXT AFTER ConditionVal2, ADD ConditionVal3Init TEXT AFTER ConditionVal2Init;

-- 1439
ALTER TABLE campaign ADD Group1 VARCHAR(100) NOT NULL DEFAULT '' AFTER LongDescription, ADD Group2 VARCHAR(100) NOT NULL DEFAULT '' AFTER Group1, ADD Group3 VARCHAR(100) NOT NULL DEFAULT '' AFTER Group2;

-- 1440
INSERT INTO invariant(idname, value, sort, description, VeryShortDesc)
  VALUES('ACTION', 'executeCerberusCommand', 6552, 'execute Cerberus command', 'execute Cerberus command');

-- 1441
INSERT INTO `parameter` (`system`, `param`, `value`, `description`)
  VALUES ('', 'cerberus_stopinstance_timeout', '300', 'Integer value that correspond to the nb of s until the stopinstance servlet will stop waiting.');

-- 1442
INSERT INTO `parameter` (`system`, `param`, `value`, `description`)
  VALUES ('', 'cerberus_manage_token', LEFT(MD5(RAND()), 32), 'Token in order to secure public access to manage api.');

-- 1443
UPDATE `parameter` SET `param` = 'cerberus_manage_timeout', `description` = 'Integer value that correspond to the nb of s until the manage servlet will stop waiting a clean stop of a global or instance stop.' WHERE (`param` = 'cerberus_stopinstance_timeout');

-- 1444
ALTER TABLE `appservice` ADD COLUMN `KafkaTopic` VARCHAR(1000) NULL DEFAULT '' AFTER `ServiceRequest`,ADD COLUMN `KafkaKey` VARCHAR(1000) NULL DEFAULT '' AFTER `KafkaTopic`,ADD COLUMN `KafkaFilterPath` VARCHAR(1000) NULL DEFAULT '' AFTER `KafkaKey`,ADD COLUMN `KafkaFilterValue` VARCHAR(1000) NULL DEFAULT '' AFTER `KafkaFilterPath`;

-- 1445
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`)
  VALUES   ('SRVMETHOD', 'PRODUCE', 600 , 'Produce a Kafka event'),  ('SRVMETHOD', 'SEARCH', 700 , 'Search in a Kafka stream'),  ('SRVTYPE', 'KAFKA', 400 , 'KAFKA Service');

-- 1446
INSERT INTO `parameter` (`system`, param, value, description)
  VALUES  ('', 'cerberus_executeCerberusCommand_path', '/opt/CerberusMedias/scripts/', 'Path to the Cerberus script folder'), ('', 'cerberus_executeCerberusCommand_user', 'cerberus', 'User used to execute a script with Cerberus'), ('', 'cerberus_executeCerberusCommand_password', LEFT(MD5(RAND()), 32), 'Password used to execute a script with Cerberus');

-- 1447
CREATE TABLE `dashboardTypeReportItem` (`idTypeRepItem` int(11) NOT NULL AUTO_INCREMENT,`codeTypeRepItem` varchar(50) NOT NULL ,`descTypeRepItem` varchar(255) NOT NULL,PRIMARY KEY (`idTypeRepItem`))ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- 1448
INSERT INTO `dashboardTypeReportItem` (`codeTypeRepItem`,`descTypeRepItem`)
  VALUES ('CAMPAIGN','Report-Item associés aux campagnes')
  ,('CAMPAIGN_GROUP','Report-Item associés aux groupes de campagnes'), ('APPLICATION','Report-Item associés aux applications')
  ,('GENERIC','Report-Item génériques sur l\'instance de Cerberus')
  ,('ENVIRONMENT','Report-Item associés aux environnements');

-- 1449
CREATE TABLE `dashboardReportItem` (`reportItemCode` varchar(50) NOT NULL,`reportItemTitre` varchar(50) NOT NULL,`isConfigurable` BOOL,`reportItemType` int(11) NOT NULL,PRIMARY KEY (`reportItemCode`),CONSTRAINT `FK_typeReportItem_01` FOREIGN KEY (`reportItemType`) REFERENCES `dashboardTypeReportItem` (`idTypeRepItem`))ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- 1450
INSERT INTO `dashboardReportItem` (`reportItemCode`,`reportItemTitre`,`isConfigurable`,`reportItemType`)
  VALUES('CAMPAIGN_EVOLUTION','Campaign evolution',true,'1');

-- 1451
CREATE TABLE `dashboardGroupEntries` (`idGroupEntries` int(11) NOT NULL AUTO_INCREMENT,`codeGroupeEntries` varchar(50) NOT NULL,`sort` int(11) DEFAULT 10,`dashboardUserId` int(10) UNSIGNED NOT NULL,`reportItemType` int(11) NOT NULL,PRIMARY KEY (`idGroupEntries`),CONSTRAINT `FK_dashboardGroup_01` FOREIGN KEY (`dashboardUserId`) REFERENCES `user` (`UserID`) ON DELETE CASCADE ON UPDATE CASCADE,CONSTRAINT `FK_dashboardGroup_02` FOREIGN KEY (`reportItemType`) REFERENCES `dashboardTypeReportItem` (`idTypeRepItem`))ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- 1452
CREATE TABLE `dashboardGroupEntriesCampaign` (`idGroupEntries` int(11) NOT NULL,`idCampaign` int(10) UNSIGNED NOT NULL,PRIMARY KEY (`idGroupEntries`,`idCampaign`),CONSTRAINT `FK_dashboardGroupCampaign_01` FOREIGN KEY (`idGroupEntries`) REFERENCES `dashboardGroupEntries` (`idGroupEntries`) ON DELETE CASCADE ON UPDATE CASCADE,CONSTRAINT `FK_dashboardGroupCampaign_02` FOREIGN KEY (`idCampaign`) REFERENCES `campaign` (`campaignId`) ON DELETE CASCADE ON UPDATE CASCADE)ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- 1453
CREATE TABLE `dashboardGroupEntriesApplication` (`idGroupEntries` int(11) NOT NULL,`application` varchar(200) NOT NULL,PRIMARY KEY (`idGroupEntries`,`application`),CONSTRAINT `FK_dashboardGroupApplication_01` FOREIGN KEY (`idGroupEntries`) REFERENCES `dashboardGroupEntries` (`idGroupEntries`) ON DELETE CASCADE ON UPDATE CASCADE,CONSTRAINT `FK_dashboardGroupApplication_02` FOREIGN KEY (`application`) REFERENCES `application` (`Application`) ON DELETE CASCADE ON UPDATE CASCADE)ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- 1454
CREATE TABLE `dashboardEntry` (`idGroupEntries` int(11) NOT NULL,`reportItemCode` varchar(50) NOT NULL,`paramId1` varchar(255) DEFAULT NULL,`paramId2` varchar(255) DEFAULT NULL,`UsrCreated` varchar(45) DEFAULT NULL,`DateCreated` timestamp NULL DEFAULT NULL,`UsrModif` varchar(45) DEFAULT NULL,`DateModif` timestamp NULL DEFAULT NULL,PRIMARY KEY (`idGroupEntries`),CONSTRAINT `FK_dashboardEntry_01` FOREIGN KEY (`idGroupEntries`) REFERENCES `dashboardGroupEntries` (`idGroupEntries`) ON DELETE CASCADE ON UPDATE CASCADE,CONSTRAINT `FK_dashboardEntry_02` FOREIGN KEY (`reportItemCode`) REFERENCES `dashboardReportItem` (`reportItemCode`))ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- 1455
ALTER TABLE `dashboardReportItem` MODIFY `isConfigurable` varchar(1);

-- 1456
UPDATE dashboardReportItem SET `isConfigurable`='Y' WHERE 1=1;

-- 1457
UPDATE testcase SET ConditionVal3 = '' WHERE ConditionVal3 IS NULL;

-- 1458
UPDATE testcasestep SET ConditionVal3 = '' WHERE ConditionVal3 IS NULL;

-- 1459
UPDATE testcasestepaction SET ConditionVal3 = '' WHERE ConditionVal3 IS NULL;

-- 1460
UPDATE testcasestepaction SET Value3 = '' WHERE Value3 IS NULL;

-- 1461
UPDATE testcasestepactioncontrol SET ConditionVal3 = '' WHERE ConditionVal3 IS NULL;

-- 1462
UPDATE testcasestepactioncontrol SET Value3 = '' WHERE Value3 IS NULL;

-- 1463
ALTER TABLE `testcase` DROP FOREIGN KEY `FK_testcase_03`, DROP COLUMN `Ticket`, DROP COLUMN `Project`, ADD COLUMN `Executor` VARCHAR(45) NOT NULL DEFAULT '' AFTER `Implementer`, CHANGE COLUMN `BugID` `BugID` TEXT NOT NULL, DROP INDEX `IX_testcase_04`  ;

-- 1464
UPDATE testcase SET bugID = CASE WHEN bugID = '' or bugID is null THEN "[]" ELSE concat('[{"id":"',bugID,'","desc":""}]') END;

-- 1465
ALTER TABLE `test` ADD COLUMN `ParentTest` VARCHAR(45) NULL DEFAULT NULL AFTER `Active`;

-- 1466
INSERT INTO `parameter` (`system`, param, value, description)
  VALUES  ('', 'cerberus_testcasepage_controlemptybugdescription', 'N', 'Boolean that activate a blocking control when saving a testcase that have at least one empty bugid description.');

-- 1467
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`)
  VALUES  ('PLATFORM', 'IOS', '20', 'iOS Platform', '');

-- 1468
INSERT INTO `parameter` (`system`, `param`, `value`, `description`)
  VALUES  ('', 'cerberus_selenium_autoscroll_vertical_offset', '0', 'Integer that correspond to the hertical offset applied after autoscrolling to element. '),  ('', 'cerberus_selenium_autoscroll_horizontal_offset', '0', 'Integer that correspond to the horizontal offset applied after autoscrolling to element');

-- 1469
INSERT INTO invariant (idname, value, sort, description, VeryShortDesc)
  VALUES('PROPERTYTYPE', 'getRawFromXml', 55, 'Get XML code', 'Get XML code');

-- 1470
ALTER TABLE `robotexecutor` ADD COLUMN `executorExtensionHost` VARCHAR(255) NULL DEFAULT NULL AFTER `deviceLockUnlock`;

-- 1471
INSERT INTO invariant (idname, value, sort, description, VeryShortDesc)
  VALUES('ACTION', 'setNetworkTrafficContent', 24900, 'Set Network Traffic to current content', 'Set HAR content');

-- 1472
INSERT INTO `parameter` (`system`, param, value, description)
  VALUES  ('', 'cerberus_webperf_thirdpartyfilepath', '', 'Full location of the file descriptor for thirdparty definition. ex : /opt/data/entities.json');

-- 1473
UPDATE invariant set value = 'verifyElementTextMatchRegex', description = 'verifyElementTextMatchRegex' where value = 'verifyRegexInElement' and idname='CONTROL';

-- 1474
UPDATE invariant set value = 'verifyElementTextDifferent', description = 'verifyElementTextDifferent' where value = 'verifyTextNotInElement' and idname='CONTROL';

-- 1475
UPDATE invariant set value = 'verifyElementTextEqual', description = 'verifyElementTextEqual' where value = 'verifyTextInElement' and idname='CONTROL';

-- 1476
INSERT INTO invariant (idname, value, sort, description, VeryShortDesc)
  VALUES ('CONTROL', 'verifyElementNumericEqual', 4400, 'verifyElementNumericEqual', '')
  ,('CONTROL', 'verifyElementNumericDifferent', 4500, 'verifyElementNumericDifferent', '')
  ,('CONTROL', 'verifyElementNumericGreater', 4600, 'verifyElementNumericGreater', '')
  ,('CONTROL', 'verifyElementNumericGreaterOrEqual', 4700, 'verifyElementNumericGreaterOrEqual', '')
  ,('CONTROL', 'verifyElementNumericMinor', 4800, 'verifyElementNumericMinor', '')
  ,('CONTROL', 'verifyElementNumericMinorOrEqual', 4900, 'verifyElementNumericMinorOrEqual', '');

-- 1477
UPDATE testcasestepactioncontrol set control = 'verifyElementTextMatchRegex' where control = 'verifyRegexInElement' ;

-- 1478
UPDATE testcasestepactioncontrol set control = 'verifyElementTextDifferent' where control = 'verifyTextNotInElement' ;

-- 1479
UPDATE testcasestepactioncontrol set control = 'verifyElementTextEqual' where control = 'verifyTextInElement' ;

-- 1480
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`)
  VALUES ('ACTIONCONDITIONOPER', 'ifElementVisible', 264, 'Only execute if Element is visible.', '')
  ,('STEPCONDITIONOPER', 'ifElementVisible', 264, 'Only execute if Element is visible.', '')
  ,('CONTROLCONDITIONOPER', 'ifElementVisible', 264, 'Only execute if Element is visible.', '')
  ,('ACTIONCONDITIONOPER', 'ifElementNotVisible', 265, 'Only execute if Element is not visible.', '')
  ,('STEPCONDITIONOPER', 'ifElementNotVisible', 265, 'Only execute if Element is not visible.', '')
  ,('CONTROLCONDITIONOPER', 'ifElementNotVisible', 265, 'Only execute if Element is not visible.', '');

-- 1481
INSERT INTO `parameter` (`system`, param, value, description)
  VALUES  ('', 'cerberus_webperf_ignoredomainlist', '', 'coma separated domain that should be ignored when building webperf stats from Network Traffic. ex : gvt1.com,domain.fr,toto.com');

-- 1482
INSERT INTO `invariant` (`idname`, `value`, `gp1`, `sort`, `description`, `VeryShortDesc`)
  VALUES  ('WEBPERFTHIRDPARTY', 'TrustCommander', 'trustcommander.net,privacy.trustcommander.net', 100, 'Trustcommander third party.', ''), ('INVARIANTPUBLIC','WEBPERFTHIRDPARTY', '', '850','Webperf ThirdParty.', '');

-- 1483
INSERT INTO `parameter` (`system`, param, value, description)
  VALUES  ('', 'cerberus_networkstatsave_active', 'N', 'Boolean in order to activate the saving of the file at execution level.');

-- 1484
DROP TABLE `testcaseexecutionwwwdet`, `testcaseexecutionwwwsum`;

-- 1485
DROP TABLE `project`;

-- 1486
CREATE TABLE `testcaseexecutionhttpstat` (`ID` bigint(20) unsigned,`Start` TIMESTAMP NOT NULL DEFAULT '1970-01-01 01:01:01',`ControlStatus` varchar(2) NOT NULL,`System` varchar(45) NOT NULL,`Application` varchar(200) DEFAULT NULL,`Test` varchar(45) NULL,`TestCase` varchar(45) NULL,`Country` varchar(45) NOT NULL,`Environment` varchar(45) NOT NULL,`RobotDecli` varchar(100) NOT NULL,`total_hits` int(10) DEFAULT 0,`total_size` int(10) DEFAULT 0,`total_time` int(10) DEFAULT 0,`internal_hits` int(10) DEFAULT 0,`internal_size` int(10) DEFAULT 0,`internal_time` int(10) DEFAULT 0,`img_size` int(10) DEFAULT 0,`img_size_max` int(10) DEFAULT 0,`img_hits` int(10) DEFAULT 0,`js_size` int(10) DEFAULT 0,`js_size_max` int(10) DEFAULT 0,`js_hits` int(10) DEFAULT 0,`css_size` int(10) DEFAULT 0,`css_size_max` int(10) DEFAULT 0,`css_hits` int(10) DEFAULT 0,`html_size` int(10) DEFAULT 0,`html_size_max` int(10) DEFAULT 0,`html_hits` int(10) DEFAULT 0,`media_size` int(10) DEFAULT 0,`media_size_max` int(10) DEFAULT 0,`media_hits` int(10) DEFAULT 0,`nb_thirdparty` int(10) DEFAULT 0,`CrbVersion` varchar(45) ,`statdetail` MEDIUMTEXT ,`UsrCreated` VARCHAR(45) NOT NULL DEFAULT '',`DateCreated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,`UsrModif` VARCHAR(45) NOT NULL DEFAULT '',`DateModif` TIMESTAMP NOT NULL DEFAULT '1970-01-01 01:01:01',  PRIMARY KEY (`id`), KEY `IX_testcaseexecutionhttpstat_01` (`ControlStatus`, `Test`,`TestCase`,`Environment`,`Country`,`RobotDecli` ), KEY `IX_testcaseexecutionhttpstat_02` (`Start`), INDEX `IX_testcaseexecutionhttpstat_03` (`Application`), INDEX `IX_testcaseexecutionhttpstat_04` (`Test`,`TestCase`), CONSTRAINT `FK_testcaseexecutionhttpstat_03` FOREIGN KEY (`Application`) REFERENCES `application` (`Application`) ON DELETE SET NULL ON UPDATE CASCADE, CONSTRAINT `FK_testcaseexecutionhttpstat_04` FOREIGN KEY (`Test` , `TestCase`) REFERENCES `testcase` (`Test` , `TestCase`) ON DELETE SET NULL ON UPDATE CASCADE)ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- 1487
INSERT INTO `parameter` (`system`, param, value, description)
  VALUES  ('', 'cerberus_executorproxy_timeoutms', '3600000', 'Timeout in ms second used for Cerberus Executor proxy session.');

-- 1488
INSERT INTO `parameter` (`system`, param, value, description)
  VALUES  ('', 'cerberus_networkstatsave_idleperiod_ms', '5000', 'Period between every checks in ms (default to 5000). No network requests withing that period means that Network is idle and stats can be saved.'), ('', 'cerberus_networkstatsave_idlemaxloop_nb', '20', 'Maximum nb of loop before idle checks stops. After that max amount of checks, stats will be saved even if hits are still detected.');

-- 1489
ALTER TABLE `tag` ADD INDEX `IX_tag_03` (`DateCreated` ASC) ;

-- 1490
INSERT INTO invariant (idname, value, sort, description, VeryShortDesc)
  VALUES('ACTION', 'waitNetworkTrafficIdle', 16700, 'Wait until there are no more Network Traffic.', 'Wait Network Idle');

-- 1491
INSERT INTO invariant (idname, value, sort, description, VeryShortDesc)
  VALUES('PROPERTYTYPE', 'getFromNetworkTraffic', 45, 'Get stats from Network Trafic JSON data structure.', 'Get Network Stats');

-- 1492
UPDATE testcasestepaction set Value2=concat("{'command': '", Value1, "', 'args': ['", Value2, "']}"), Value1='mobile: shell' where Action='executeCommand';

-- 1493
UPDATE testcasecountryproperties set Value2=concat("{'command': '", Value1, "', 'args': ['']}"), Value1='mobile: shell' where Type='getFromCommand';

-- 1494
ALTER TABLE testcase CHANGE `Group` `Type` VARCHAR(45);

-- 1495
UPDATE invariant SET idname='TESTCASE_TYPE', description='Type of interactive tests' WHERE idname='GROUP' AND value='AUTOMATED';

-- 1496
UPDATE invariant SET idname='TESTCASE_TYPE', description='Type of test which cannot be automatized' WHERE idname='GROUP' AND value='MANUAL';

-- 1497
UPDATE invariant SET idname='TESTCASE_TYPE', description='Type of tests which not appear in Cerberus' WHERE idname='GROUP' AND value='PRIVATE';

-- 1498
ALTER TABLE testcase CHANGE BehaviorOrValueExpected DetailedDescription TEXT;

-- 1499
ALTER TABLE testcase CHANGE TestCaseVersion Version INT(10),CHANGE ConditionOper ConditionOperator VARCHAR(45),CHANGE FromBuild FromMajor VARCHAR(10),CHANGE ToBuild ToMajor VARCHAR(10),CHANGE TargetBuild TargetMajor VARCHAR(10),CHANGE FromRev FromMinor VARCHAR(20),CHANGE ToRev ToMinor VARCHAR(20),CHANGE TargetRev TargetMinor VARCHAR(20);

-- 1500
ALTER TABLE testcaseexecution CHANGE ConditionOper ConditionOperator VARCHAR(45);

-- 1501
ALTER TABLE testcasestep CHANGE ConditionOper ConditionOperator VARCHAR(45);

-- 1502
ALTER TABLE testcasestepexecution CHANGE ConditionOper ConditionOperator VARCHAR(45);

-- 1503
ALTER TABLE testcasestepaction CHANGE ConditionOper ConditionOperator VARCHAR(45);

-- 1504
ALTER TABLE testcasestepactionexecution CHANGE ConditionOper ConditionOperator VARCHAR(45);

-- 1505
ALTER TABLE testcasestepactioncontrol CHANGE ConditionOper ConditionOperator VARCHAR(45);

-- 1506
ALTER TABLE testcasestepactioncontrolexecution CHANGE ConditionOper ConditionOperator VARCHAR(45);

-- 1507
UPDATE invariant set idname = 'TESTCASECONDITIONOPERATOR' WHERE idname = 'TESTCASECONDITIONOPER';

-- 1508
UPDATE invariant set idname = 'STEPCONDITIONOPERATOR' WHERE idname = 'STEPCONDITIONOPER';

-- 1509
UPDATE invariant set idname = 'ACTIONCONDITIONOPERATOR' WHERE idname = 'ACTIONCONDITIONOPER';

-- 1510
UPDATE invariant set idname = 'CONTROLCONDITIONOPERATOR' WHERE idname = 'CONTROLCONDITIONOPER';

-- 1511
ALTER TABLE testcase DROP `function`;

-- 1512
ALTER TABLE testcase CHANGE TcActive isActive VARCHAR(1),CHANGE activeQA isActiveQA VARCHAR(1),CHANGE activeUAT isActiveUAT VARCHAR(1),CHANGE activePROD isActivePROD VARCHAR(1);

-- 1513
UPDATE testcase SET isActive = 1 WHERE isActive = 'Y';

-- 1514
UPDATE testcase SET isActiveQA = 1 WHERE isActiveQA = 'Y';

-- 1515
UPDATE testcase SET isActiveUAT = 1 WHERE isActiveUAT = 'Y';

-- 1516
UPDATE testcase SET isActivePROD = 1 WHERE isActivePROD = 'Y';

-- 1517
UPDATE testcase SET isActive = 0 WHERE isActive != '1';

-- 1518
UPDATE testcase SET isActiveQA = 0 WHERE isActiveQA != '1';

-- 1519
UPDATE testcase SET isActiveUAT = 0 WHERE isActiveUAT != '1';

-- 1520
UPDATE testcase SET isActivePROD = 0 WHERE isActivePROD != '1';

-- 1521
ALTER TABLE testcase MODIFY isActive BOOLEAN;

-- 1522
ALTER TABLE testcase MODIFY isActiveQA BOOLEAN;

-- 1523
ALTER TABLE testcase MODIFY isActiveUAT BOOLEAN;

-- 1524
ALTER TABLE testcase MODIFY isActivePROD BOOLEAN;

-- 1525
DELETE FROM invariant WHERE idname = 'TCACTIVE';

-- 1526
ALTER TABLE testcase DROP HowTo;

-- 1527
ALTER TABLE testcase CHANGE BugID Bugs TEXT;

-- 1528
ALTER TABLE `testcaseexecutionqueue` ADD COLUMN `SelectedExtensionHost` VARCHAR(150) NULL DEFAULT '' AFTER `SelectedRobotHost`;

-- 1529
INSERT INTO `parameter` (`system`, param, value, description)
  VALUES  ('', 'cerberus_queueexecution_defaultexecutorexthost_threadpoolsize', '2', 'Default number of simultaneous execution allowed for Robot execution extension host constrain (only used when host entry does not exist in EXECUTOREXTENSIONHOST invariant table).');

-- 1530
INSERT INTO invariant (idname, value, sort, description, VeryShortDesc, gp1)
  VALUES('EXECUTOREXTENSIONHOST', 'localhost', 100, 'Localhost Extension', '', '2')
  ,('INVARIANTPUBLIC', 'EXECUTOREXTENSIONHOST', 900, '', '', '');

-- 1531
INSERT INTO invariant (idname, value, sort, description, VeryShortDesc)
  VALUES('BROWSER', 'edge', 100, 'Edge Browser', '');

-- 1532
INSERT INTO appservicecontent select asr.* from appserviceheader asr join appservice app ON app.Service=asr.Service where `type`='KAFKA';

-- 1533
DELETE FROM appserviceheader USING appserviceheader join appservice ON appservice.Service = appserviceheader.Service where appservice.`type` = 'KAFKA';

-- 1534
CREATE TABLE `queuestat` (  `ID` int(11) NOT NULL AUTO_INCREMENT,  `GlobalConstrain` int(10) DEFAULT 0,  `CurrentlyRunning` int(10) DEFAULT 0,  `QueueSize` int(10) DEFAULT 0,`UsrCreated` VARCHAR(45) NOT NULL DEFAULT '',`DateCreated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,`UsrModif` VARCHAR(45) NOT NULL DEFAULT '',`DateModif` TIMESTAMP NOT NULL DEFAULT '1970-01-01 01:01:01',  PRIMARY KEY (`ID`),  KEY `IX_queuestat_01` (`DateCreated`))
  ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- 1535
INSERT INTO invariant (idname, value, sort, description, VeryShortDesc)
  VALUES('ACTION', 'setServiceCallContent', 24910, 'Set JSON Service Call to current content', 'Set Call content');

-- 1536
ALTER TABLE `appservice` ADD COLUMN `isFollowRedir` BOOLEAN DEFAULT 1 AFTER `ServicePath`;

-- 1537
ALTER TABLE `tag` ADD COLUMN `Comment` VARCHAR(1000) DEFAULT '' AFTER `Description`;

-- 1538
INSERT INTO `parameter` (`system`, param, value, description)
  VALUES  ('', 'cerberus_queueshistorystatgraph_maxnbpoints', '1000', 'Maximum number of points on the queue history graph.');

-- 1539
INSERT INTO `parameter`
  VALUES('', 'cerberus_splashpage_enable', 'false', 'Boolean to display for non admin users a splashpage is case of maintenance');

-- 1540
INSERT INTO `parameter`
  VALUES('', 'cerberus_messageinfo_text', 'your text here', 'text that will be displayed in case paramater "cerberus_messageinfo_enable" is set on true');

-- 1541
INSERT INTO `parameter`
  VALUES('', 'cerberus_messageinfo_enable', 'false', 'Boolean to display a message info to all Cerberus users');

-- 1542
ALTER TABLE test CHANGE Active isActive VARCHAR(1);

-- 1543
UPDATE test SET isActive = 1 WHERE isActive = 'Y';

-- 1544
UPDATE test SET isActive = 0 WHERE isActive != '1';

-- 1545
ALTER TABLE test MODIFY isActive BOOLEAN;

-- 1546
UPDATE invariant SET value = 'true' WHERE idname = 'TESTACTIVE' AND value ='Y';

-- 1547
UPDATE invariant SET value = 'false' WHERE idname = 'TESTACTIVE' AND value ='N';

-- 1548
ALTER TABLE `testcaseexecutionqueue` ADD COLUMN `Video` INT(11) NOT NULL DEFAULT '0' AFTER `Screenshot`, CHANGE COLUMN `SeleniumLog` `RobotLog` INT(11) NOT NULL DEFAULT '1', ADD COLUMN `ConsoleLog` INT(11) NOT NULL DEFAULT '0' AFTER `RobotLog`, CHANGE COLUMN `Verbose` `Verbose` INT(11) NOT NULL DEFAULT '0' AFTER `Tag`, CHANGE COLUMN `Timeout` `Timeout` MEDIUMTEXT NULL DEFAULT NULL AFTER `ConsoleLog` ;

-- 1549
ALTER TABLE `campaign` ADD COLUMN `Video` VARCHAR(5) NULL DEFAULT NULL AFTER `Screenshot`,ADD COLUMN `ConsoleLog` VARCHAR(5) NULL DEFAULT NULL AFTER `RobotLog`;

-- 1550
UPDATE campaign SET Video = '1', Screenshot = '1' WHERE Screenshot = '3';

-- 1551
UPDATE campaign SET Video = '2', Screenshot = '2' WHERE Screenshot = '4';

-- 1552
DELETE FROM invariant where idname in ('SELENIUMLOG','SCREENSHOT');

-- 1553
DELETE FROM invariant where idname in ('PRIVATEINVARIANT') and value in ('SELENIUMLOG');

-- 1554
INSERT INTO `invariant` (`idname`, `value`, `gp1`, `sort`, `description`, `VeryShortDesc`)
  VALUES  ('ROBOTLOG', '0', NULL, 10, 'Never record Robot Log (Selenium, Appium, ...)', ''), ('ROBOTLOG', '1', NULL, 20, 'Record Robot Log on error only (Selenium, Appium, ...)', ''), ('ROBOTLOG', '2', NULL, 30, 'Always record Robot Log (Selenium, Appium, ...)', ''), ('CONSOLELOG', '0', NULL, 10, 'Never record Console Log', ''), ('CONSOLELOG', '1', NULL, 20, 'Record Console Log on error only', ''), ('CONSOLELOG', '2', NULL, 30, 'Always record Console Log', ''), ('VIDEO', '0', NULL, 10, 'Never record Video', ''), ('VIDEO', '1', NULL, 20, 'Record Video on error only', ''), ('VIDEO', '2', NULL, 30, 'Always record Video', ''), ('SCREENSHOT', '0', NULL, 10, 'No Screenshots', ''), ('SCREENSHOT', '1', NULL, 20, 'Automatic Screenshots on error', ''), ('SCREENSHOT', '2', NULL, 30, 'Systematic Screenshots', ''), ('INVARIANTPRIVATE','ROBOTLOG', '', '850','.', ''), ('INVARIANTPRIVATE','CONSOLELOG', '', '850','.', ''), ('INVARIANTPRIVATE','VIDEO', '', '850','.', '');

-- 1555
ALTER TABLE `testcaseexecution`  CHANGE COLUMN `URL` `URL` VARCHAR(350) NULL DEFAULT NULL ;

-- 1556
INSERT INTO invariant (idname, value, sort, description, VeryShortDesc)
  VALUES('ACTION', 'setConsoleContent', 24950, 'Set JSON Console Logs to current content', 'Set Console content');

-- 1557
ALTER TABLE `documentation` CHANGE COLUMN `DocLabel` `DocLabel` TEXT NULL DEFAULT NULL ;

-- 1558
ALTER TABLE `scheduleentry`  CHANGE COLUMN `cronDefinition` `cronDefinition` VARCHAR(200) NOT NULL ;

-- 1559
INSERT INTO invariant (idname, value, sort, description, VeryShortDesc)
  VALUES('ACTION', 'setContent', 24960, 'Set parameter1 to current content', 'Set content');

-- 1560
INSERT INTO invariant (idname, value, sort, description, VeryShortDesc)
  VALUES('ACTION', 'indexNetworkTraffic', 24905, 'Index Network Traffic requests', 'Index Network Traffic');

-- 1561
INSERT INTO `parameter`
  VALUES('', 'cerberus_creditlimit_nbexeperday', '0', 'Maximum number of execution per day.'), ('', 'cerberus_creditlimit_secondexeperday', '0', 'Maximum duration of all execution in minutes.');

-- 1562
UPDATE invariant set value = 'TYPE' where idname='CAMPAIGN_TCCRITERIA' and value='GROUP';

-- 1563
ALTER TABLE testcasestepaction DROP FOREIGN KEY FK_testcasestepaction_01;

-- 1564
ALTER TABLE testcasestep CHANGE COLUMN Step StepId INT(10) UNSIGNED NOT NULL, CHANGE COLUMN TestCase Testcase VARCHAR(45) NOT NULL, CHANGE COLUMN ConditionVal1 ConditionValue1 TEXT, CHANGE COLUMN ConditionVal2 ConditionValue2 TEXT, CHANGE COLUMN ConditionVal3 ConditionValue3 TEXT, CHANGE COLUMN useStep IsUsingLibraryStep VARCHAR(1), CHANGE COLUMN useStepTest LibraryStepTest VARCHAR(45) DEFAULT NULL, CHANGE COLUMN useStepTestCase LibraryStepTestcase VARCHAR(45) DEFAULT NULL, CHANGE COLUMN useStepStep LibraryStepStepId int(10) UNSIGNED DEFAULT NULL, CHANGE COLUMN inLibrary IsLibraryStep VARCHAR(1), CHANGE COLUMN forceExe IsExecutionForced VARCHAR(1) NOT NULL;

-- 1565
UPDATE testcasestep SET IsUsingLibraryStep = 1 WHERE IsUsingLibraryStep = 'Y';

-- 1566
UPDATE testcasestep SET IsExecutionForced = 1 WHERE IsExecutionForced = 'Y';

-- 1567
UPDATE testcasestep SET IsLibraryStep = 1 WHERE IsLibraryStep = 'Y';

-- 1568
UPDATE testcasestep SET IsUsingLibraryStep = 0 WHERE IsUsingLibraryStep != '1';

-- 1569
UPDATE testcasestep SET IsExecutionForced = 0 WHERE IsExecutionForced != '1';

-- 1570
UPDATE testcasestep SET IsLibraryStep = 0 WHERE IsLibraryStep != '1';

-- 1571
ALTER TABLE testcasestep MODIFY IsUsingLibraryStep BOOLEAN DEFAULT 0, MODIFY IsExecutionForced BOOLEAN DEFAULT 0, MODIFY IsLibraryStep BOOLEAN DEFAULT 0;

-- 1572
ALTER TABLE testcasestepaction ADD CONSTRAINT FK_testcasestepaction_01 FOREIGN KEY (Test , Testcase , Step) REFERENCES testcasestep (Test , TestCase , StepId) ON DELETE CASCADE ON UPDATE CASCADE;

-- 1573
ALTER TABLE testcasecountryproperties ADD COLUMN UsrCreated VARCHAR(45) NOT NULL DEFAULT '', ADD COLUMN DateCreated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, ADD COLUMN UsrModif VARCHAR(45) NOT NULL DEFAULT '', CHANGE COLUMN TestCase Testcase VARCHAR(45) NOT NULL, CHANGE COLUMN last_modified DateModif TIMESTAMP NOT NULL DEFAULT '1970-01-01 01:01:01' AFTER UsrModif;

-- 1574
INSERT INTO `parameter` (`system`, `param`, `value`, `description`)
  VALUES  ('', 'cerberus_lambdatest_defaultexename', 'Exe : %EXEID% - %TESTDESCRIPTION%', 'Define the default value for the name of the execution to be sent to Lambdatest when a test is executed. Variable %EXEID%, %TESTFOLDER%, %TESTID% and %TESTDESCRIPTION% can be used.')
  ,('', 'cerberus_apikey_enable', 'false', 'Boolean in order to activate the API Key control for all public API calls.')
  ,('', 'cerberus_apikey_value2', '', 'API key value. API Keys are used to secure public access to all public API.')
  ,('', 'cerberus_apikey_value3', '', 'API key value. API Keys are used to secure public access to all public API.')
  ,('', 'cerberus_apikey_value4', '', 'API key value. API Keys are used to secure public access to all public API.')
  ,('', 'cerberus_apikey_value5', '', 'API key value. API Keys are used to secure public access to all public API.');

-- 1575
ALTER TABLE `tag` ADD COLUMN `LambdatestBuild` VARCHAR(100) NOT NULL DEFAULT '' AFTER `browserstackBuildHash`, CHANGE COLUMN `browserstackBuildHash` `BrowserstackBuildHash` VARCHAR(100) NOT NULL DEFAULT '';

-- 1576
INSERT IGNORE INTO invariant (idname, value, sort, description, VeryShortDesc)
  VALUES('ROBOTHOST', 'hub-cloud.browserstack.com', 10, 'BrowserStack cloud service', 'BrowserStack')
  ,('ROBOTHOST', 'hub.lambdatest.com', 10, 'LambdaTest cloud service', 'LambdaTest')
  ,('ROBOTHOST', 'api.kobiton.com', 10, 'Kobiton cloud service', 'Kobiton');

-- 1577
ALTER TABLE testcaseexecution ADD COLUMN `RobotProviderSessionId` VARCHAR(100) NOT NULL DEFAULT '' AFTER `RobotProvider`;

-- 1578
UPDATE `parameter` SET `param` = 'cerberus_apikey_value1', `description` = 'API key value. API Keys are used to secure public access to all public API.' WHERE (`param` = 'cerberus_manage_token');

-- 1579
UPDATE invariant SET value='TESTCASE_TYPE' WHERE idname='INVARIANTPRIVATE' AND value='GROUP';

-- 1580
INSERT INTO `parameter` (`system`, `param`, `value`, `description`)
  VALUES  ('', 'cerberus_sikuli_minSimilarity', '', 'Define similarity value used by Sikuli. Value can take from 0 (minumum similarity) to 1 (maximum similarity). Sikuli default to 0.7.')
  ,('', 'cerberus_sikuli_highlightElement', '', 'Number of second Sikuli will highlight the element when interacting with it. Default to 2 seconds.');

-- 1581
ALTER TABLE robot ADD COLUMN `ProfileFolder` VARCHAR(400) NOT NULL DEFAULT '' AFTER `screensize`;

-- 1582
INSERT INTO `parameter` (`system`, `param`, `value`, `description`)
  VALUES ('', 'cerberus_sikuli_wait_element', '30000', 'Integer that correspond to the number of milliseconds that sikuli will wait before give timeout, when searching an element.');

-- 1583
INSERT INTO invariant (idname, value, sort, description, VeryShortDesc)
  VALUES('ACTION', 'mouseMove', 4500, 'Move the mouse', 'Move the mouse');

-- 1584
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`)
  VALUES  ('PROPERTYTYPE', 'getOTP', '90', 'get OTP Code', '');

-- 1585
DROP TABLE testcasestepbatch;

-- 1586
ALTER TABLE label
  CHANGE COLUMN ReqType RequirementType VARCHAR(100) NOT NULL DEFAULT '',
  CHANGE COLUMN ReqStatus RequirementStatus VARCHAR(100) NOT NULL DEFAULT '',
  CHANGE COLUMN ReqCriticity RequirementCriticity VARCHAR(100) NOT NULL DEFAULT '',
  CHANGE COLUMN LongDesc LongDescription TEXT NOT NULL;

-- 1587
ALTER TABLE testcaselabel
  CHANGE COLUMN TestCase Testcase VARCHAR(45) NOT NULL;

-- 1588
ALTER TABLE testcaseexecutionhttpstat
  CHANGE COLUMN TestCase Testcase VARCHAR(45);

-- 1589
ALTER TABLE testcasecountry
  ADD COLUMN UsrCreated VARCHAR(45) NOT NULL DEFAULT '',
  ADD COLUMN DateCreated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  ADD COLUMN UsrModif VARCHAR(45) NOT NULL DEFAULT '',
  CHANGE COLUMN TestCase Testcase VARCHAR(45) NOT NULL,
  CHANGE COLUMN last_modified DateModif TIMESTAMP NOT NULL DEFAULT '1970-01-01 01:01:01' AFTER UsrModif;

-- 1590
ALTER TABLE testcasedep
  CHANGE COLUMN TestCase Testcase VARCHAR(45) DEFAULT NULL,
  CHANGE COLUMN DepTest DependencyTest VARCHAR(45) DEFAULT NULL,
  CHANGE COLUMN DepTestCase DependencyTestcase VARCHAR(45) DEFAULT NULL,
  CHANGE COLUMN DepEvent DependencyEvent VARCHAR(100) DEFAULT NULL,
  CHANGE COLUMN Active IsActive VARCHAR(1) NOT NULL;

-- 1591
UPDATE testcasedep SET IsActive = 1 WHERE IsActive = 'Y';

-- 1592
UPDATE testcasedep SET IsActive = 0 WHERE IsActive != '1';

-- 1593
ALTER TABLE testcasedep MODIFY IsActive BOOLEAN DEFAULT 1;

-- 1594
ALTER TABLE testcasestepactioncontrol DROP FOREIGN KEY FK_testcasestepactioncontrol_01;

-- 1595
ALTER TABLE testcasestepaction DROP FOREIGN KEY FK_testcasestepaction_01;

-- 1596
ALTER TABLE testcasestepactioncontrol
  ADD COLUMN UsrCreated VARCHAR(45) NOT NULL DEFAULT '',
  ADD COLUMN DateCreated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  ADD COLUMN UsrModif VARCHAR(45) NOT NULL DEFAULT '',
  CHANGE COLUMN TestCase Testcase VARCHAR(45) NOT NULL,
  CHANGE COLUMN Step StepId INT(10) UNSIGNED NOT NULL,
  CHANGE COLUMN Sequence ActionId INT(10) UNSIGNED NOT NULL,
  CHANGE COLUMN ControlSequence ControlId INT(10) UNSIGNED NOT NULL,
  CHANGE COLUMN ConditionVal1 ConditionValue1 TEXT,
  CHANGE COLUMN ConditionVal2 ConditionValue2 TEXT,
  CHANGE COLUMN ConditionVal3 ConditionValue3 TEXT,
  CHANGE COLUMN Fatal IsFatal VARCHAR(1),
  CHANGE COLUMN last_modified DateModif TIMESTAMP NOT NULL DEFAULT '1970-01-01 01:01:01' AFTER UsrModif;

-- 1597
UPDATE testcasestepactioncontrol SET IsFatal = 1 WHERE IsFatal = 'Y';

-- 1598
UPDATE testcasestepactioncontrol SET IsFatal = 0 WHERE IsFatal != '1';

-- 1599
ALTER TABLE testcasestepactioncontrol MODIFY IsFatal BOOLEAN DEFAULT 0;

-- 1600
ALTER TABLE testcasestepaction
  ADD COLUMN UsrCreated VARCHAR(45) NOT NULL DEFAULT '',
  ADD COLUMN DateCreated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  ADD COLUMN UsrModif VARCHAR(45) NOT NULL DEFAULT '',
  CHANGE COLUMN TestCase Testcase VARCHAR(45) NOT NULL,
  CHANGE COLUMN Step StepId INT(10) UNSIGNED NOT NULL,
  CHANGE COLUMN Sequence ActionId INT(10) UNSIGNED NOT NULL,
  CHANGE COLUMN ConditionVal1 ConditionValue1 TEXT,
  CHANGE COLUMN ConditionVal2 ConditionValue2 TEXT,
  CHANGE COLUMN ConditionVal3 ConditionValue3 TEXT,
  CHANGE COLUMN ForceExeStatus IsFatal VARCHAR(45) NOT NULL,
  CHANGE COLUMN last_modified DateModif TIMESTAMP NOT NULL DEFAULT '1970-01-01 01:01:01' AFTER UsrModif;

-- 1601
UPDATE testcasestepaction SET IsFatal = 0 WHERE IsFatal = 'PE';

-- 1602
UPDATE testcasestepaction SET IsFatal = 1 WHERE IsFatal != '0';

-- 1603
ALTER TABLE testcasestepaction MODIFY IsFatal BOOLEAN DEFAULT 0;

-- 1604
ALTER TABLE testcasestepaction
  ADD CONSTRAINT FK_testcasestepaction_01
  FOREIGN KEY (Test , Testcase , StepId)
  REFERENCES testcasestep (Test , Testcase , StepId)
  ON DELETE CASCADE ON UPDATE CASCADE;

-- 1605
ALTER TABLE testcasestepactioncontrol
  ADD CONSTRAINT FK_testcasestepactioncontrol_01
  FOREIGN KEY (Test , Testcase , StepId , ActionId)
  REFERENCES testcasestepaction (Test , Testcase , StepId , ActionId)
  ON DELETE CASCADE ON UPDATE CASCADE;

-- 1606
ALTER TABLE testcase
    CHANGE TestCase Testcase VARCHAR(45) NOT NULL,
    CHANGE COLUMN ConditionVal1 ConditionValue1 TEXT,
    CHANGE COLUMN ConditionVal2 ConditionValue2 TEXT,
    CHANGE COLUMN ConditionVal3 ConditionValue3 TEXT;

-- 1607
UPDATE invariant SET value = 'false' where idname = 'STEPFORCEEXE' AND value = 'N';

-- 1608
UPDATE invariant SET value = 'true' where idname = 'STEPFORCEEXE' AND value = 'Y';

-- 1609
UPDATE invariant SET idname = 'ACTIONFATAL' WHERE idname = 'ACTIONFORCEEXESTATUS';

-- 1610
UPDATE invariant SET value = 'true' WHERE idname = 'ACTIONFATAL' AND value = 'PE';

-- 1611
UPDATE invariant SET value = 'false' WHERE idname = 'ACTIONFATAL' AND value != 'true';

-- 1612
UPDATE invariant SET value = 'true' WHERE idname = 'CTRLFATAL' AND value = 'Y';

-- 1613
UPDATE invariant SET value = 'false' WHERE idname = 'CTRLFATAL' AND value != 'true';

-- 1614
ALTER TABLE `testcase` 
    CHANGE COLUMN `ConditionValue1` `ConditionValue1` LONGTEXT NULL DEFAULT NULL ,
    CHANGE COLUMN `ConditionValue2` `ConditionValue2` LONGTEXT NULL DEFAULT NULL ,
    CHANGE COLUMN `ConditionValue3` `ConditionValue3` LONGTEXT NULL DEFAULT NULL ;

-- 1615
ALTER TABLE `testcasestep` 
    CHANGE COLUMN `ConditionValue1` `ConditionValue1` LONGTEXT NULL DEFAULT NULL ,
    CHANGE COLUMN `ConditionValue2` `ConditionValue2` LONGTEXT NULL DEFAULT NULL ,
    CHANGE COLUMN `ConditionValue3` `ConditionValue3` LONGTEXT NULL DEFAULT NULL ;

-- 1616
ALTER TABLE `testcasestepaction` 
    CHANGE COLUMN `ConditionValue1` `ConditionValue1` LONGTEXT NULL DEFAULT NULL ,
    CHANGE COLUMN `ConditionValue2` `ConditionValue2` LONGTEXT NULL DEFAULT NULL ,
    CHANGE COLUMN `ConditionValue3` `ConditionValue3` LONGTEXT NULL DEFAULT NULL ,
    CHANGE COLUMN `Value1` `Value1` LONGTEXT NULL DEFAULT NULL ,
    CHANGE COLUMN `Value2` `Value2` LONGTEXT NULL DEFAULT NULL ,
    CHANGE COLUMN `Value3` `Value3` LONGTEXT NULL DEFAULT NULL ;

-- 1617
ALTER TABLE `testcasestepactioncontrol` 
    CHANGE COLUMN `ConditionValue1` `ConditionValue1` LONGTEXT NULL DEFAULT NULL ,
    CHANGE COLUMN `ConditionValue2` `ConditionValue2` LONGTEXT NULL DEFAULT NULL ,
    CHANGE COLUMN `ConditionValue3` `ConditionValue3` LONGTEXT NULL DEFAULT NULL ,
    CHANGE COLUMN `Value1` `Value1` LONGTEXT NULL DEFAULT NULL ,
    CHANGE COLUMN `Value2` `Value2` LONGTEXT NULL DEFAULT NULL ,
    CHANGE COLUMN `Value3` `Value3` LONGTEXT NULL DEFAULT NULL ;

-- 1618
ALTER TABLE `testcaseexecution` 
    CHANGE COLUMN `ConditionVal1` `ConditionVal1` LONGTEXT NULL DEFAULT NULL ,
    CHANGE COLUMN `ConditionVal2` `ConditionVal2` LONGTEXT NULL DEFAULT NULL ,
    CHANGE COLUMN `ConditionVal3` `ConditionVal3` LONGTEXT NULL DEFAULT NULL ,
    CHANGE COLUMN `ConditionVal1Init` `ConditionVal1Init` LONGTEXT NULL DEFAULT NULL ,
    CHANGE COLUMN `ConditionVal2Init` `ConditionVal2Init` LONGTEXT NULL DEFAULT NULL ,
    CHANGE COLUMN `ConditionVal3Init` `ConditionVal3Init` LONGTEXT NULL DEFAULT NULL ;

-- 1619
ALTER TABLE `testcasestepexecution` 
    CHANGE COLUMN `ConditionVal1` `ConditionVal1` LONGTEXT NULL DEFAULT NULL ,
    CHANGE COLUMN `ConditionVal2` `ConditionVal2` LONGTEXT NULL DEFAULT NULL ,
    CHANGE COLUMN `ConditionVal3` `ConditionVal3` LONGTEXT NULL DEFAULT NULL ,
    CHANGE COLUMN `ConditionVal1Init` `ConditionVal1Init` LONGTEXT NULL DEFAULT NULL ,
    CHANGE COLUMN `ConditionVal2Init` `ConditionVal2Init` LONGTEXT NULL DEFAULT NULL ,
    CHANGE COLUMN `ConditionVal3Init` `ConditionVal3Init` LONGTEXT NULL DEFAULT NULL ;

-- 1620
ALTER TABLE `testcasestepactionexecution` 
    CHANGE COLUMN `ConditionVal1` `ConditionVal1` LONGTEXT NULL DEFAULT NULL ,
    CHANGE COLUMN `ConditionVal2` `ConditionVal2` LONGTEXT NULL DEFAULT NULL ,
    CHANGE COLUMN `ConditionVal3` `ConditionVal3` LONGTEXT NULL DEFAULT NULL ,
    CHANGE COLUMN `ConditionVal1Init` `ConditionVal1Init` LONGTEXT NULL DEFAULT NULL ,
    CHANGE COLUMN `ConditionVal2Init` `ConditionVal2Init` LONGTEXT NULL DEFAULT NULL ,
    CHANGE COLUMN `ConditionVal3Init` `ConditionVal3Init` LONGTEXT NULL DEFAULT NULL ,
    CHANGE COLUMN `Value1` `Value1` LONGTEXT NULL DEFAULT NULL ,
    CHANGE COLUMN `Value2` `Value2` LONGTEXT NULL DEFAULT NULL ,
    CHANGE COLUMN `Value3` `Value3` LONGTEXT NULL DEFAULT NULL ,
    CHANGE COLUMN `Value1Init` `Value1Init` LONGTEXT NULL DEFAULT NULL ,
    CHANGE COLUMN `Value2Init` `Value2Init` LONGTEXT NULL DEFAULT NULL ,
    CHANGE COLUMN `Value3Init` `Value3Init` LONGTEXT NULL DEFAULT NULL ;

-- 1621
ALTER TABLE `testcasestepactioncontrolexecution` 
    CHANGE COLUMN `ConditionVal1` `ConditionVal1` LONGTEXT NULL DEFAULT NULL ,
    CHANGE COLUMN `ConditionVal2` `ConditionVal2` LONGTEXT NULL DEFAULT NULL ,
    CHANGE COLUMN `ConditionVal3` `ConditionVal3` LONGTEXT NULL DEFAULT NULL ,
    CHANGE COLUMN `ConditionVal1Init` `ConditionVal1Init` LONGTEXT NULL DEFAULT NULL ,
    CHANGE COLUMN `ConditionVal2Init` `ConditionVal2Init` LONGTEXT NULL DEFAULT NULL ,
    CHANGE COLUMN `ConditionVal3Init` `ConditionVal3Init` LONGTEXT NULL DEFAULT NULL ,
    CHANGE COLUMN `Value1` `Value1` LONGTEXT NULL DEFAULT NULL ,
    CHANGE COLUMN `Value2` `Value2` LONGTEXT NULL DEFAULT NULL ,
    CHANGE COLUMN `Value3` `Value3` LONGTEXT NULL DEFAULT NULL ,
    CHANGE COLUMN `Value1Init` `Value1Init` LONGTEXT NULL DEFAULT NULL ,
    CHANGE COLUMN `Value2Init` `Value2Init` LONGTEXT NULL DEFAULT NULL ,
    CHANGE COLUMN `Value3Init` `Value3Init` LONGTEXT NULL DEFAULT NULL ;

-- 1622
INSERT INTO `parameter` (`system`, `param`, `value`, `description`)
  VALUES  ('', 'cerberus_selenium_highlightElement', '', 'Number of second Selenium will highlight the element when interacting with it. Default to 0 second.');

-- 1623
ALTER TABLE `testcase` 
    ADD COLUMN `ConditionOptions` TEXT NULL DEFAULT NULL AFTER `ConditionValue3`;

-- 1624
ALTER TABLE `testcasestep` 
    ADD COLUMN `ConditionOptions` TEXT NULL DEFAULT NULL AFTER `ConditionValue3`;

-- 1625
ALTER TABLE `testcasestepaction` 
    ADD COLUMN `ConditionOptions` TEXT NULL DEFAULT NULL AFTER `ConditionValue3`,
    ADD COLUMN `Options` TEXT NULL DEFAULT NULL AFTER `Value3`;

-- 1626
ALTER TABLE `testcasestepactioncontrol` 
    ADD COLUMN `ConditionOptions` TEXT NULL DEFAULT NULL AFTER `ConditionValue3`,
    ADD COLUMN `Options` TEXT NULL DEFAULT NULL AFTER `Value3`;

-- 1627
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `veryshortdesc`)
  VALUES ('LANGUAGE', 'ru', 300, 'русский', 'русский');

-- 1628
ALTER TABLE `robotexecutor` ADD COLUMN `NodeProxyPort` INT NULL DEFAULT 0 AFTER `host_password`;

-- 1629
CREATE TABLE `eventhook` ( `ID` int(11) NOT NULL AUTO_INCREMENT,  `EventReference` VARCHAR(45) NOT NULL DEFAULT '',  `ObjectKey1` VARCHAR(150) NOT NULL DEFAULT '',  `ObjectKey2` VARCHAR(150) NOT NULL DEFAULT '',  
    `IsActive` BOOLEAN DEFAULT 1, 
    `HookConnector` VARCHAR(150) NOT NULL DEFAULT '', `HookRecipient` VARCHAR(500)  NOT NULL DEFAULT '', `HookChannel` VARCHAR(150) NOT NULL DEFAULT '', 
    `Description` VARCHAR(500) DEFAULT '', 
    `UsrCreated` VARCHAR(45) NOT NULL DEFAULT '',`DateCreated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,`UsrModif` VARCHAR(45) NOT NULL DEFAULT '',`DateModif` TIMESTAMP NOT NULL DEFAULT '1970-01-01 01:01:01',  
    PRIMARY KEY (`ID`),  KEY `IX_eventhook_01` (`EventReference`))
  ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- 1630
INSERT INTO `parameter` (`system`, `param`, `value`, `description`)
  VALUES ('', 'cerberus_notification_executionstart_subject', '[Cerberus] Execution %EXECUTIONID% started.', 'Subject of Cerberus start of execution notification email. %TAG%, %URLEXECUTION%, %EXECUTIONID%, %COUNTRY%, %ENVIRONMENT%, %ROBOT%, %ROBOTDECLINATION%, %TESTFOLDER% and %TESTCASE% can be used as variables.')
  ,('', 'cerberus_notification_executionstart_body', 'Hello,<br><br>The Cerberus Execution %EXECUTIONID% of testcase \'%TESTFOLDER% - %TESTCASE%\' just started on %ENVIRONMENT% and %COUNTRY% and on %ROBOT%.<br><br>You can follow its execution <a href="%URLEXECUTION%">here</a>.','Cerberus start of execution notification email body. %TAG%, %URLEXECUTION%, %EXECUTIONID%, %COUNTRY%, %ENVIRONMENT%, %ROBOT%, %ROBOTDECLINATION%, %TESTFOLDER% and %TESTCASE% can be used as variables.')
  ,('', 'cerberus_notification_from','Cerberus <no.reply@cerberus-testing.com>', 'From field of Cerberus notification email.')
  ,('', 'cerberus_notification_executionend_subject', '[Cerberus] Execution %EXECUTIONID% finished with status %STATUS%.', 'Subject of Cerberus end of execution notification email. %TAG%, %URLEXECUTION%, %EXECUTIONID%, %COUNTRY%, %ENVIRONMENT%, %ROBOT%, %ROBOTDECLINATION%, %TESTFOLDER%, %TESTCASE% and %STATUS% can be used as variables.')
  ,('', 'cerberus_notification_executionend_body', 'Hello,<br><br>The Cerberus Execution %EXECUTIONID% of testcase \'%TESTFOLDER% - %TESTCASE%\' has just finished on %ENVIRONMENT% and %COUNTRY% and on %ROBOT%.<br><br>You can analyse the result <a href="%URLEXECUTION%">here</a>.','Cerberus End of execution notification email body. %TAG%, %URLEXECUTION%, %EXECUTIONID%, %COUNTRY%, %ENVIRONMENT%, %ROBOT%, %ROBOTDECLINATION%, %TESTFOLDER%, %TESTCASE% and %STATUS% can be used as variables.');

-- 1631
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`)
  VALUES   ('EVENTHOOK', 'CAMPAIGN_START', 100, 'When a campaign starts.')
  ,('EVENTHOOK', 'CAMPAIGN_END', 200, 'When a campaign ends.')
  ,('EVENTHOOK', 'CAMPAIGN_END_CIKO', 300, 'When a campaign ends with a CIScore KO.')
  ,('EVENTHOOK', 'EXECUTION_START', 400, 'When a testcase execution starts.')
  ,('EVENTHOOK', 'EXECUTION_END', 500, 'When a testcase execution ends.')
  ,('EVENTHOOK', 'EXECUTION_END_LASTRETRY', 550, 'When a testcase execution ends (only on the last retry of the testcase).')
  ,('EVENTHOOK', 'TESTCASE_CREATE', 700, 'When a testcase is created.')
  ,('EVENTHOOK', 'TESTCASE_UPDATE', 720, 'When a testcase is updated.')
  ,('EVENTHOOK', 'TESTCASE_DELETE', 740, 'When a testcase is deleted.')
  ,('EVENTCONNECTOR', 'SLACK', 200, 'Slack connector.')
  ,('EVENTCONNECTOR', 'EMAIL', 100, 'EMail connector.')
  ,('EVENTCONNECTOR', 'TEAMS', 220, 'Microsoft Teams connector.')
  ,('EVENTCONNECTOR', 'GENERIC', 900, 'Native Cerberus connector.')
  ,('EVENTCONNECTOR', 'GOOGLE-CHAT', 240, 'Google Chat connector.')
  ,('INVARIANTPRIVATE', 'EVENTHOOK', '850', '')
  ,('INVARIANTPRIVATE', 'EVENTCONNECTOR', '900', '');

-- 1632
INSERT INTO eventhook (Eventreference, ObjectKey1, IsActive, HookConnector, HookRecipient, HookChannel, UsrCreated)
    select 'CAMPAIGN_START', campaign, 1, 'EMAIL', DistribList, '', 'importSQL' from campaign where NotifyStartTagExecution = 'Y';

-- 1633
INSERT INTO eventhook (Eventreference, ObjectKey1, IsActive, HookConnector, HookRecipient, HookChannel, UsrCreated)
    select 'CAMPAIGN_END', campaign, 1, 'EMAIL', DistribList, '', 'importSQL' from campaign where NotifyEndTagExecution = 'Y';

-- 1634
INSERT INTO eventhook (Eventreference, ObjectKey1, IsActive, HookConnector, HookRecipient, HookChannel, UsrCreated)
    select 'CAMPAIGN_END_CIKO', campaign, 1, 'EMAIL', DistribList, '', 'importSQL' from campaign where NotifyEndTagExecution = 'CIKO';

-- 1635
INSERT INTO eventhook (Eventreference, ObjectKey1, IsActive, HookConnector, HookRecipient, HookChannel, UsrCreated)
    select 'CAMPAIGN_START', campaign, 1, 'SLACK', SlackWebhook, SlackChannel, 'importSQL' from campaign where SlackNotifyStartTagExecution = 'Y';

-- 1636
INSERT INTO eventhook (Eventreference, ObjectKey1, IsActive, HookConnector, HookRecipient, HookChannel, UsrCreated)
  select 'CAMPAIGN_END', campaign, 1, 'SLACK', SlackWebhook, SlackChannel, 'importSQL' from campaign where SlackNotifyEndTagExecution = 'Y';

-- 1637
INSERT INTO eventhook (Eventreference, ObjectKey1, IsActive, HookConnector, HookRecipient, HookChannel, UsrCreated)
  select 'CAMPAIGN_END_CIKO', campaign, 1, 'SLACK', SlackWebhook, SlackChannel, 'importSQL' from campaign where SlackNotifyEndTagExecution = 'CIKO';

-- 1638
ALTER TABLE `campaign` 
    DROP COLUMN `SlackChannel`,DROP COLUMN `SlackWebhook`,DROP COLUMN `SlackNotifyEndTagExecution`,DROP COLUMN `SlackNotifyStartTagExecution`,DROP COLUMN `NotifyEndTagExecution`,DROP COLUMN `NotifyStartTagExecution`,DROP COLUMN `DistribList`;

-- 1639
UPDATE `parameter` set `value`='0' WHERE `system`='' and `param` in ('cerberus_selenium_highlightElement','cerberus_sikuli_highlightElement') and `value` = '';

-- 1640
ALTER TABLE `testcasestepexecution` 
    MODIFY COLUMN `Start` TIMESTAMP(3) NOT NULL DEFAULT NOW(3), 
    MODIFY COLUMN `End` TIMESTAMP(3) NOT NULL DEFAULT NOW(3);

-- 1641 to 1688
UPDATE `invariant` SET `gp1` = 'Property Name', `gp2` = '[opt] Name of an other property', `gp3` = '' WHERE idname = 'ACTION' AND value = 'calculateProperty'; 
UPDATE `invariant` SET `gp1` = 'Service Name', `gp2` = 'Nb Evt (Kafka)', `gp3` = 'Evt Wait sec (Kafka)' WHERE idname = 'ACTION' AND value = 'callService'; 
UPDATE `invariant` SET `gp1` = 'Element path to Clear', `gp2` = '', `gp3` = '' WHERE idname = 'ACTION' AND value = 'clearField'; 
UPDATE `invariant` SET `gp1` = 'Element path', `gp2` = '', `gp3` = '' WHERE idname = 'ACTION' AND value = 'click'; 
UPDATE `invariant` SET `gp1` = 'Application name or path', `gp2` = '', `gp3` = '' WHERE idname = 'ACTION' AND value = 'closeApp'; 
UPDATE `invariant` SET `gp1` = '', `gp2` = '', `gp3` = '' WHERE idname = 'ACTION' AND value = 'doNothing'; 
UPDATE `invariant` SET `gp1` = 'Element path', `gp2` = '', `gp3` = '' WHERE idname = 'ACTION' AND value = 'doubleClick'; 
UPDATE `invariant` SET `gp1` = 'Element path', `gp2` = 'Destination Element Path', `gp3` = '' WHERE idname = 'ACTION' AND value = 'dragAndDrop'; 
UPDATE `invariant` SET `gp1` = 'Command (ex : "grep")', `gp2` = '', `gp3` = '' WHERE idname = 'ACTION' AND value = 'executeCerberusCommand'; 
UPDATE `invariant` SET `gp1` = 'Appium Command (ex : "mobile:deepLink")', `gp2` = 'Arguments (ex : {url: "www.site.com", package: "com.Package"})', `gp3` = '' WHERE idname = 'ACTION' AND value = 'executeCommand'; 
UPDATE `invariant` SET `gp1` = 'JavaScript to execute', `gp2` = '', `gp3` = '' WHERE idname = 'ACTION' AND value = 'executeJS'; 
UPDATE `invariant` SET `gp1` = 'Database Name', `gp2` = 'Stored Procedure', `gp3` = '' WHERE idname = 'ACTION' AND value = 'executeSqlStoredProcedure'; 
UPDATE `invariant` SET `gp1` = 'Database Name', `gp2` = 'Script', `gp3` = '' WHERE idname = 'ACTION' AND value = 'executeSqlUpdate'; 
UPDATE `invariant` SET `gp1` = '', `gp2` = '', `gp3` = '' WHERE idname = 'ACTION' AND value = 'focusDefaultIframe'; 
UPDATE `invariant` SET `gp1` = 'Element path of the target iFrame', `gp2` = '', `gp3` = '' WHERE idname = 'ACTION' AND value = 'focusToIframe'; 
UPDATE `invariant` SET `gp1` = '', `gp2` = '', `gp3` = '' WHERE idname = 'ACTION' AND value = 'hideKeyboard'; 
UPDATE `invariant` SET `gp1` = '[opt] Index name', `gp2` = '', `gp3` = '' WHERE idname = 'ACTION' AND value = 'indexNetworkTraffic'; 
UPDATE `invariant` SET `gp1` = 'Application path (ex : /root/toto.apk)', `gp2` = '', `gp3` = '' WHERE idname = 'ACTION' AND value = 'installApp'; 
UPDATE `invariant` SET `gp1` = '[opt] Target element path', `gp2` = 'Key to press', `gp3` = '' WHERE idname = 'ACTION' AND value = 'keypress'; 
UPDATE `invariant` SET `gp1` = 'Element path', `gp2` = '[opt] Duration (ms) : 8000 by default', `gp3` = '' WHERE idname = 'ACTION' AND value = 'longPress'; 
UPDATE `invariant` SET `gp1` = 'ok or cancel', `gp2` = '', `gp3` = '' WHERE idname = 'ACTION' AND value = 'manageDialog'; 
UPDATE `invariant` SET `gp1` = 'keys to press.', `gp2` = '', `gp3` = '' WHERE idname = 'ACTION' AND value = 'manageDialogKeypress'; 
UPDATE `invariant` SET `gp1` = 'Element path', `gp2` = '', `gp3` = '' WHERE idname = 'ACTION' AND value = 'mouseLeftButtonPress'; 
UPDATE `invariant` SET `gp1` = 'Element path', `gp2` = '', `gp3` = '' WHERE idname = 'ACTION' AND value = 'mouseLeftButtonRelease'; 
UPDATE `invariant` SET `gp1` = 'Relative coord. (ex : 50,100 ; 200,50)', `gp2` = '', `gp3` = '' WHERE idname = 'ACTION' AND value = 'mouseMove'; 
UPDATE `invariant` SET `gp1` = 'Element path', `gp2` = '', `gp3` = '' WHERE idname = 'ACTION' AND value = 'mouseOver'; 
UPDATE `invariant` SET `gp1` = '[Deprecated]', `gp2` = '[Deprecated]', `gp3` = '' WHERE idname = 'ACTION' AND value = 'mouseOverAndWait'; 
UPDATE `invariant` SET `gp1` = 'Application name or path or package for Android', `gp2` = '[Optional, required for Android] Activity', `gp3` = '' WHERE idname = 'ACTION' AND value = 'openApp'; 
UPDATE `invariant` SET `gp1` = 'URL to call (ex : http://www.domain.com)', `gp2` = '', `gp3` = '' WHERE idname = 'ACTION' AND value = 'openUrl'; 
UPDATE `invariant` SET `gp1` = '', `gp2` = '', `gp3` = '' WHERE idname = 'ACTION' AND value = 'openUrlLogin'; 
UPDATE `invariant` SET `gp1` = 'URI to call  (ex : /index.html)', `gp2` = '', `gp3` = '' WHERE idname = 'ACTION' AND value = 'openUrlWithBase'; 
UPDATE `invariant` SET `gp1` = '', `gp2` = '', `gp3` = '' WHERE idname = 'ACTION' AND value = 'refreshCurrentPage'; 
UPDATE `invariant` SET `gp1` = 'Application package (ex : com.appmobile)', `gp2` = '', `gp3` = '' WHERE idname = 'ACTION' AND value = 'removeApp'; 
UPDATE `invariant` SET `gp1` = '[Deprecated]', `gp2` = '[Deprecated]', `gp3` = '' WHERE idname = 'ACTION' AND value = 'removeDifference'; 
UPDATE `invariant` SET `gp1` = 'Element path', `gp2` = '', `gp3` = '' WHERE idname = 'ACTION' AND value = 'rightClick'; 
UPDATE `invariant` SET `gp1` = 'element (''id=ressource-id''. Empty if you want use text)', `gp2` = 'text (empty if you want use element)', `gp3` = '' WHERE idname = 'ACTION' AND value = 'scrollTo'; 
UPDATE `invariant` SET `gp1` = '', `gp2` = '', `gp3` = '' WHERE idname = 'ACTION' AND value = 'select'; 
UPDATE `invariant` SET `gp1` = '', `gp2` = '', `gp3` = '' WHERE idname = 'ACTION' AND value = 'setConsoleContent'; 
UPDATE `invariant` SET `gp1` = 'Value to Set', `gp2` = '', `gp3` = '' WHERE idname = 'ACTION' AND value = 'setContent'; 
UPDATE `invariant` SET `gp1` = 'url to filter', `gp2` = 'Activate http response content (Y/N)', `gp3` = '' WHERE idname = 'ACTION' AND value = 'setNetworkTrafficContent'; 
UPDATE `invariant` SET `gp1` = '', `gp2` = '', `gp3` = '' WHERE idname = 'ACTION' AND value = 'setServiceCallContent'; 
UPDATE `invariant` SET `gp1` = 'Action (UP DOWN LEFT RIGHT CUSTOM...)', `gp2` = 'Direction x;y;z;y', `gp3` = '' WHERE idname = 'ACTION' AND value = 'swipe'; 
UPDATE `invariant` SET `gp1` = 'Window title or url', `gp2` = '', `gp3` = '' WHERE idname = 'ACTION' AND value = 'switchToWindow'; 
UPDATE `invariant` SET `gp1` = 'Element path', `gp2` = 'Text to type', `gp3` = '' WHERE idname = 'ACTION' AND value = 'type'; 
UPDATE `invariant` SET `gp1` = '', `gp2` = '', `gp3` = '' WHERE idname = 'ACTION' AND value = 'Unknown'; 
UPDATE `invariant` SET `gp1` = 'Duration(ms) or Element', `gp2` = '', `gp3` = '' WHERE idname = 'ACTION' AND value = 'wait'; 
UPDATE `invariant` SET `gp1` = '', `gp2` = '', `gp3` = '' WHERE idname = 'ACTION' AND value = 'waitNetworkTrafficIdle'; 
UPDATE `invariant` SET `gp1` = 'Element', `gp2` = '', `gp3` = '' WHERE idname = 'ACTION' AND value = 'waitVanish';

-- 1689
INSERT INTO `parameter` (`system`, `param`, `value`, `description`)
  VALUES ('', 'cerberus_executionlog_enable', 'true','Enable (true) or disable (false) the execution json log messages inside application server logs. If false, no json log messages will never be logged. If true, json message will be logged only if verbose level is at the correct level.');

--1690
INSERT INTO `invariant` (idname, value, sort, description)
  VALUES ('CONTROL', 'verifyElementTextContains', 4200, 'verifyElementTextContains');

-- 1691
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`)
  VALUES   ('EXTERNALPROVIDER', 'SID', 100, 'Selenium IDE.')
  ,('EXTERNALPROVIDER', 'XRAY', 200, 'JIRA XRay.')
  ,('INVARIANTPUBLIC', 'EXTERNALPROVIDER', '950', '');

-- 1692
ALTER TABLE `usergroup` 
    CHANGE COLUMN `GroupName` `Role` VARCHAR(45) NOT NULL , RENAME TO  `userrole` ;

-- 1693
ALTER TABLE `user` 
    ADD COLUMN `Attribute01` VARCHAR(255) NOT NULL DEFAULT '' AFTER `Login`,
    ADD COLUMN `Attribute02` VARCHAR(255) NOT NULL DEFAULT '' AFTER `Attribute01`,
    ADD COLUMN `Attribute03` VARCHAR(255) NOT NULL DEFAULT ''  AFTER `Attribute02`,
    ADD COLUMN `Attribute04` VARCHAR(255) NOT NULL DEFAULT ''  AFTER `Attribute03`,
    ADD COLUMN `Attribute05` VARCHAR(255) NOT NULL DEFAULT ''  AFTER `Attribute04`,
    ADD COLUMN `APIKey` VARCHAR(200) NULL AFTER `Attribute05`,
    ADD COLUMN `Comment` VARCHAR(255) NOT NULL DEFAULT ''   AFTER `APIKey`,
    CHANGE COLUMN `Name` `Name` VARCHAR(25) NOT NULL AFTER `Login`,
    CHANGE COLUMN `Email` `Email` VARCHAR(100) NULL DEFAULT NULL AFTER `Name`,
    CHANGE COLUMN `Team` `Team` VARCHAR(45) NULL DEFAULT NULL AFTER `Email`,
    CHANGE COLUMN `Language` `Language` VARCHAR(45) NULL DEFAULT 'en' AFTER `Team`;

-- 1694
ALTER TABLE `user` 
    ADD UNIQUE INDEX `IX_user_02` (`APIKey`);

-- 1695
ALTER TABLE `user` 
    ADD COLUMN `UsrCreated` varchar(45) NOT NULL DEFAULT '',
    ADD COLUMN `DateCreated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN `UsrModif` varchar(45) NOT NULL DEFAULT '',
    ADD COLUMN `DateModif` TIMESTAMP NOT NULL DEFAULT '1970-01-01 01:01:01';

-- 1696
INSERT INTO `user` (`login`, `name`, `email`, `team`, `language`, `attribute01`, `attribute02`, `attribute03`, `attribute04`, `attribute05`, `apikey`, `password`, `userpreferences`, `usrcreated`)
    SELECT concat('srvaccount', right(param, 1)), concat('Service Account ', right(param, 1)), '', '', 'en', '', '', '', '', '', value, '', '' , 'DatabaseVersioningService'
    FROM parameter where param like 'cerberus_apikey_value%' and `system` = '' and value is not null and value !='';

-- 1697
INSERT INTO `user` (`login`, `name`, `email`, `team`, `language`, `attribute01`, `attribute02`, `attribute03`, `attribute04`, `attribute05`, `apikey`, `password`, `userpreferences`, `usrcreated`, `comment`)
    SELECT 'srvaccount', 'Cerberus Service Account', '', '', 'en', '', '', '', '', '', LEFT(MD5(RAND()), 90), '', '', 'DatabaseVersioningService', 'DO NOT REMOVE !! Service Account for internal use by Cerberus.'
    FROM dual;

-- 1698
ALTER TABLE `applicationobject` CHANGE COLUMN `Value` `Value` LONGTEXT NULL DEFAULT NULL ;

-- 1699
ALTER TABLE `robot` 
    ADD COLUMN `ExtraParam` VARCHAR(1000) NOT NULL DEFAULT '' AFTER `ProfileFolder`,
    ADD COLUMN `IsAcceptInsecureCerts` BOOLEAN DEFAULT 1 AFTER `ExtraParam`;

-- 1700
INSERT IGNORE INTO `invariant` (`idname`, `value`, `sort`, `description`)
  VALUES   ('CAPABILITY', 'browserstack.local', 10, '')
  ,('CAPABILITY', 'browserstack.localIdentifier', 20, '');

-- 1701
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) VALUES('PROPERTYTYPE', 'getRawFromJson', 72, 'Get element in standard JSON format', 'Get raw JSON element');

-- 1702
ALTER TABLE appserviceheader CHANGE COLUMN `Active` `IsActive` VARCHAR(45);

-- 1703
ALTER TABLE appservicecontent CHANGE COLUMN `Active` `IsActive` VARCHAR(45);

-- 1704 - 1706
UPDATE appserviceheader SET `IsActive` = 1 WHERE `IsActive` = 'Y';
UPDATE appserviceheader SET `IsActive` = 0 WHERE `IsActive` != '1';
ALTER TABLE appserviceheader MODIFY `IsActive` BOOLEAN DEFAULT 1;

-- 1707 - 1709
UPDATE appservicecontent SET `IsActive` = 1 WHERE `IsActive` = 'Y';
UPDATE appservicecontent SET `IsActive` = 0 WHERE `IsActive` != '1';
ALTER TABLE appservicecontent MODIFY `IsActive` BOOLEAN DEFAULT 1;

-- 1710 - 1713
UPDATE invariant SET `value` = 'true' WHERE `idname` = 'APPSERVICECONTENTACT' AND `value` = 'Y';
UPDATE invariant SET `value` = 'false' WHERE `idname` = 'APPSERVICECONTENTACT' AND `value` = 'N';
UPDATE invariant SET `value` = 'true' WHERE `idname` = 'APPSERVICEHEADERACT' AND `value` = 'Y';
UPDATE invariant SET `value` = 'false' WHERE `idname` = 'APPSERVICEHEADERACT' AND `value` = 'N';

-- 1714 - 1723
ALTER TABLE `tag` DROP FOREIGN KEY FK_tag_1;
ALTER TABLE `campaignparameter` DROP FOREIGN KEY FK_campaignparameter_01;
ALTER TABLE `campaignlabel` DROP FOREIGN KEY FK_campaignlabel_01;
ALTER TABLE `campaign` MODIFY COLUMN `campaign` VARCHAR(200) NOT NULL;
ALTER TABLE `campaignlabel` MODIFY COLUMN `campaign` VARCHAR(200) NOT NULL;
ALTER TABLE `campaignparameter` MODIFY COLUMN `campaign` VARCHAR(200) NOT NULL;
ALTER TABLE `tag` MODIFY COLUMN `Campaign` VARCHAR(200) NULL DEFAULT NULL;
ALTER TABLE `tag` ADD CONSTRAINT `FK_tag_1` FOREIGN KEY (`Campaign`) REFERENCES `campaign` (`campaign`)  ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE `campaignparameter` ADD CONSTRAINT `FK_campaignparameter_01` FOREIGN KEY (`campaign`) REFERENCES `campaign` (`campaign`) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE `campaignlabel` ADD CONSTRAINT `FK_campaignlabel_01` FOREIGN KEY (`campaign`) REFERENCES `campaign` (`campaign`) ON DELETE CASCADE ON UPDATE CASCADE;

-- 1724
ALTER TABLE `appservice` 
    ADD COLUMN `KafkaFilterHeaderPath` VARCHAR(1000) NULL DEFAULT '' AFTER `KafkaFilterValue`,
    ADD COLUMN `KafkaFilterHeaderValue` VARCHAR(1000) NULL DEFAULT '' AFTER `KafkaFilterHeaderPath`,
    ADD COLUMN `IsAvroEnable` BOOLEAN DEFAULT 0 AFTER `KafkaFilterHeaderValue`,
    ADD COLUMN `SchemaRegistryUrl` VARCHAR(250) NULL DEFAULT '' AFTER `IsAvroEnable`;

-- 1725-1726
ALTER TABLE `appservice` 
    ADD COLUMN `ParentContentService` VARCHAR(255) NULL DEFAULT NULL AFTER `SchemaRegistryUrl`;
ALTER TABLE `appservice` 
    ADD CONSTRAINT `FK_appservice_02` FOREIGN KEY (`ParentContentService`) REFERENCES `appservice` (`Service`) ON DELETE SET NULL ON UPDATE CASCADE;

-- 1727
ALTER TABLE `tag` 
    ADD COLUMN `XRayTestExecution` VARCHAR(45) NULL DEFAULT '' AFTER `LambdatestBuild`,
    ADD COLUMN `XRayURL` VARCHAR(100) NULL DEFAULT '' AFTER `XRayTestExecution`;

-- 1728
INSERT INTO `parameter` (`system`, `param`, `value`, `description`)
  VALUES ('', 'cerberus_xraycloud_clientsecret', '','JIRA XRay Cloud Client Secret.'),
        ('', 'cerberus_xraycloud_clientid', '','JIRA XRay Cloud Client ID.');

-- 1729-1731
UPDATE `invariant` SET `value` = 'SeleniumIDE', `description` = 'Selenium IDE' WHERE (`idname` = 'EXTERNALPROVIDER') and (`value` = 'SID');
UPDATE `invariant` SET `value` = 'JiraXray-Cloud', `description` = 'JIRA Xray Cloud' WHERE (`idname` = 'EXTERNALPROVIDER') and (`value` = 'XRAY');
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) VALUES ('EXTERNALPROVIDER', 'JiraXray-DC', '210', 'JIRA Xray DC', '');

-- 1732
INSERT INTO `parameter` (`system`, `param`, `value`, `description`)
  VALUES ('', 'cerberus_xraydc_url', '','JIRA XRay DC Site URL. Ex : http://yourserver/rest/raven/2.0/api'),
        ('', 'cerberus_xraydc_token', '','JIRA XRay DC Token value.');

-- 1733
UPDATE `parameter` SET `description` = 'JIRA XRay DC Site URL. Ex : http://yourserver In order to access the API, /rest/raven/2.0/api will be added in order to have the format http://yourserver/rest/raven/2.0/api'
    WHERE (`system` = '') and (`param` = 'cerberus_xraydc_url');

-- 1734
INSERT INTO `parameter` (`system`, `param`, `value`, `description`)
  VALUES ('', 'cerberus_sikuli_typeDelay', '0.1', 'Insert a delay between every keystroke. Ex : 0.5 correspond to 500 msec delay between every keystroke.');

-- 1735
ALTER TABLE `applicationobject`
    ADD COLUMN `XOffset` VARCHAR(45) NULL AFTER `ScreenshotFileName`,
    ADD COLUMN `YOffset` VARCHAR(45) NULL AFTER `XOffset`;

-- 1736
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`)
    VALUES ('CONTROL', 'verifyElementTextArrayContains', 4910, 'Verify if a specific string is in the array retrieved using JSONPath or Xpath','verifyElementTextArrayContains'),
    ('CONTROL', 'verifyElementNumericArrayContains', 4920, 'Verify if a specific numeric is in the array retrieved using JSONPath or Xpath','verifyElementNumericArrayContains'),
    ('CONTROL', 'verifyStringArrayContains', 1450, 'Verify if a specific string is in the array','verifyStringArrayContains'),
    ('CONTROL', 'verifyNumericArrayContains', 1800, 'Verify if a specific numeric is in the array','verifyNumericArrayContains');

-- 1737
UPDATE `invariant` SET `sort` = '4310' WHERE (`idname` = 'CONTROL') and (`value` = 'verifyElementTextArrayContains');

-- 1738
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`, `gp1`, `gp2`, `gp3`) VALUES 
    ('ACTION', 'cleanRobotFile', '24300', 'Clean a folder or file pattern', 'Remove robot files', 'Path/Pattern to clean', '', ''),
    ('ACTION', 'uploadRobotFile', '24320', 'Upload a file to the robot', 'Upload a robot file', 'Filename', 'Content (base64 format)', 'Option'),
    ('ACTION', 'getRobotFile', '24340', 'Get a list of files from the robot', 'Get robot files', 'Path/Pattern to retrieved', 'Nb of files', 'Option');

-- 1739-1740
UPDATE invariant set idname = 'ROBOTPROXYHOST' where idname = 'EXECUTOREXTENSIONHOST';
UPDATE invariant set value = 'ROBOTPROXYHOST' where idname = 'INVARIANTPUBLIC' and value = 'EXECUTOREXTENSIONHOST' ;

-- 1741
INSERT INTO `parameter` (`system`, `param`, `value`, `description`)
  VALUES ('', 'cerberus_testcaseautofeed_enable', 'true', 'Enable the creation of standard actions and controls on first creation of a testcase.');

-- 1742
ALTER TABLE `appservice` 
    ADD COLUMN `AvroSchema` MEDIUMTEXT NULL DEFAULT NULL AFTER `SchemaRegistryUrl`;

-- 1743-1745
ALTER TABLE `appservice` 
    ADD COLUMN `AvroSchemaKey` MEDIUMTEXT NULL DEFAULT NULL AFTER `SchemaRegistryUrl`;
ALTER TABLE `appservice` 
    CHANGE COLUMN `AvroSchema` `AvroSchemaValue` MEDIUMTEXT NULL DEFAULT NULL ;
ALTER TABLE `appservice` 
    ADD COLUMN `IsAvroEnableKey` BOOLEAN DEFAULT 0 AFTER `SchemaRegistryUrl`,
    ADD COLUMN `IsAvroEnableValue` BOOLEAN DEFAULT 0 AFTER `AvroSchemaKey`;

-- 1746
INSERT INTO `parameter` (`system`, `param`, `value`, `description`)
  VALUES ('', 'cerberus_xray_tokencache_duration', '300', 'Cache duration in second of JIRA XRay token (default to 300 seconds / 5 minutes).'),
    ('', 'cerberus_xray_sendenvironments_enable', 'false', 'boolean in order to activate or not the sending of environments to XRay.');

-- 1747
ALTER TABLE `tag` 
    ADD COLUMN `XRayMessage` TEXT NULL AFTER `XRayURL`;

-- 1748
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`, `gp1`, `gp2`, `gp3`) VALUES 
    ('SRVTYPE', 'MONGODB', '100', 'MongoDB Service', '', '', '', '');

-- 1749
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`)
  VALUES ('SRVMETHOD', 'FIND', 800 , 'Find a MongoDB Record');

-- 1750
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`)
    VALUES ('CONTROL', 'verifyElementChecked', 4930, 'Verify the element (checkbox) is checked','verifyElementChecked'),
           ('CONTROL', 'verifyElementNotChecked', 4940, 'Verify the element (checkbox) is not checked','verifyElementNotChecked');

-- 1751
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `veryshortdesc`)
  VALUES ('LANGUAGE', 'fa', 400, 'فارسی', 'فارسی');

-- 1752
INSERT INTO `parameter` (`system`, `param`, `value`, `description`)
  VALUES ('', 'cerberus_instancelogo_url', 'https://vm.cerberus-testing.org/img/logo.png', 'URl that point to the instance logo. Use that parameter in order to personalize some screens and pdf report.'),
    ('', 'cerberus_pdfcampaignreportdisplaycountry_boolean', 'true', 'Boolean in order to show or hide the country column on pdf campaign execution pdf report.');

-- 1753
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `veryshortdesc`)
 VALUES ('ACTION', 'switchToContext', 5450, 'Switch to another application context', 'switchToContext');

-- 1754
ALTER TABLE tag MODIFY COLUMN Description TEXT NULL;

-- 1755
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`)
  VALUES   ('CAMPAIGN_TCCRITERIA', 'TESTFOLDER', 50 , '');

-- 1756
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`)
    VALUES   ('CONTROL','verifyElementTextNotContains',4210, 'verifyElementTextNotContains');

-- 1757
INSERT INTO `parameter` (`system`, `param`, `value`, `description`)
  VALUES ('', 'cerberus_testdatalib_subdataDefaultValue', '', 'Default value when a subdata does not match with the datalib resultset (when the "cerberus_testdatalib_ignoreNonMatchedSubdata property" is set to true).'),
         ('', 'cerberus_testdatalib_ignoreNonMatchedSubdata', 'false', 'If set to true, then allow subdata expression not to match the datalib resultset. Any non-matched subdata will be set by the "cerberus_testdatalib_subdataDefaultValue" property value.');

-- 1758
ALTER TABLE `countryenvironmentparameters` 
    ADD COLUMN `UsrCreated` varchar(45) NOT NULL DEFAULT '',
    ADD COLUMN `DateCreated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN `UsrModif` varchar(45) NOT NULL DEFAULT '',
    ADD COLUMN `DateModif` TIMESTAMP NOT NULL DEFAULT '1970-01-01 01:01:01';

-- 1759
DROP TABLE `dashboardEntry`, `dashboardGroupEntries`, `dashboardGroupEntriesApplication`, `dashboardGroupEntriesCampaign`, `dashboardReportItem`, `dashboardTypeReportItem` ;

-- 1760
CREATE TABLE `datafile` ( `ID` int(11) NOT NULL AUTO_INCREMENT,  `Fileid` varchar(150) NOT NULL,  `FileName` varchar(250) DEFAULT NULL,  `UsrCreated` varchar(45) DEFAULT NULL,  `DateCreated` timestamp NULL DEFAULT NULL,  `UsrModif` varchar(45) DEFAULT NULL,  `DateModif` timestamp NULL DEFAULT NULL,  PRIMARY KEY (`FileId`),  UNIQUE KEY `ID_UNIQUE` (`ID`) )
  ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8;

-- 1761
ALTER TABLE logevent 
    MODIFY COLUMN remoteIP varchar(200) NULL,
    MODIFY COLUMN localIP varchar(200) NULL,
    ADD Status varchar(50) NULL AFTER `Action`;

-- 1762
ALTER TABLE testcasestepaction 
    ADD doScreenshotBefore TINYINT DEFAULT 0 NULL AFTER ScreenshotFileName,
    ADD doScreenshotAfter TINYINT DEFAULT 0 NULL AFTER doScreenshotBefore,
    ADD waitBefore INT DEFAULT 0 NULL AFTER doScreenshotAfter,
    ADD waitAfter INT DEFAULT 0 NULL AFTER waitBefore;

-- 1763
ALTER TABLE testcasestepactioncontrol 
    ADD doScreenshotBefore TINYINT DEFAULT 0 NULL AFTER ScreenshotFileName,
    ADD doScreenshotAfter TINYINT DEFAULT 0 NULL AFTER doScreenshotBefore,
    ADD waitBefore INT DEFAULT 0 NULL AFTER doScreenshotAfter,
    ADD waitAfter INT DEFAULT 0 NULL AFTER waitBefore;

-- 1764-1766
ALTER TABLE robotexecutor CHANGE executorProxyActive executorProxyType varchar(50) DEFAULT 'NONE' NOT NULL AFTER deviceLockUnlock;
UPDATE robotexecutor set executorProxyType='NETWORKTRAFFIC' where executorProxyType='Y';
UPDATE robotexecutor set executorProxyType='NONE' where executorProxyType='N';

-- 1767
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`)
  VALUES   ('PROXYTYPE', 'NONE', 100, 'No Proxy..')
  ,('PROXYTYPE', 'MANUAL', 200, 'Manual Proxy.')
  ,('PROXYTYPE', 'NETWORKTRAFFIC', 300, 'Proxy with Network Traffic analysis and control.')
  ,('INVARIANTPUBLIC', 'PROXYTYPE', '950', 'Robot Executor Proxy type.');

-- 1768
INSERT INTO `parameter` (`system`, `param`, `value`, `description`)
  VALUES ('', 'cerberus_pdfcampaignreportdisplayciresult_boolean', 'true', 'Boolean in order to show or hide the cicd campaign result on pdf campaign execution pdf report.');

-- 1769
ALTER TABLE countryenvparam_log MODIFY Creator VARCHAR(45);

-- 1770
INSERT INTO `parameter` (`system`, `param`, `value`, `description`)
  VALUES ('', 'cerberus_appium_scroll_endTopScreenPercentageScreenHeight', '0.125', 'Float value between 0 and 1 that represents the percentage of the screen height where the scroll ends. 0 for the top of the screen, 0.5 for the middle. (default to : 0.125)')
  ,('', 'cerberus_appium_scroll_startBottomPercentageScreenHeight', '0.8', 'Float value between 0 and 1 that represents the percentage of the screen height where the scroll starts. 0.5 for the middle of the screen, 1 for the bottom. (default to : 0.8)');

-- 1771
ALTER TABLE countryenvironmentparameters MODIFY COLUMN URLLOGIN varchar(300) DEFAULT '' NOT NULL;

-- 1772
ALTER TABLE application MODIFY COLUMN SubSystem varchar(200) DEFAULT '' NOT NULL;

-- 1773
ALTER TABLE `tag` ADD COLUMN `BrowserstackAppBuildHash` VARCHAR(100) NOT NULL DEFAULT '' AFTER `BrowserstackBuildHash`;

-- 1774
UPDATE testcasecountryproperties SET `Length` = 1 where `Length` = 0;

-- 1775
UPDATE testcasestepaction SET Value1 = Value2, Value2 = '' where action = 'scrollTo' and Value1 = '' and Value2 != '';

-- 1776
ALTER TABLE `tag` ADD COLUMN `DateStartExe` TIMESTAMP NOT NULL DEFAULT '1970-01-01 01:01:01' AFTER `Campaign`;

-- 1777
UPDATE `tag` SET `DateStartExe` = `DateCreated`;

-- 1778
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`)
  VALUES   ('SRVBODYTYPE', 'none', 200, '')
  ,('SRVBODYTYPE', 'raw', 300, '')
  ,('SRVBODYTYPE', 'form-data', 400, '')
  ,('SRVBODYTYPE', 'form-urlencoded', 500, '')
  ,('INVARIANTPRIVATE', 'SRVBODYTYPE', '950', 'Service Body type.');

-- 1779
UPDATE invariant SET idname='INVARIANTPRIVATE' WHERE idname='INVARIANTPUBLIC' AND value='PROXYTYPE';

-- 1780-1785
ALTER TABLE `appservice` ADD COLUMN `BodyType` VARCHAR(60) NOT NULL DEFAULT '' AFTER `AttachementURL`;
UPDATE appservice a  SET a.bodytype = 'raw' where `Type` ='REST' and ServiceRequest != '';
UPDATE appservice a  SET a.bodytype = 'form-data' where `Type` ='REST' and Description like '%[form-data]%';
UPDATE appservice a  SET a.bodytype = 'form-urlencoded' where `Type` ='REST' and ServiceRequest = '' and a.bodytype = '';
ALTER TABLE appservice CHANGE `Group` Collection varchar(100) DEFAULT '' NULL;
ALTER TABLE `appservice` ADD COLUMN `SimulationParameters` TEXT AFTER `Collection`;

-- 1786-1789
UPDATE invariant SET sort=15 WHERE idname='TESTDATATYPE' AND value='CSV';
UPDATE invariant SET value='FILE' WHERE idname='TESTDATATYPE' AND value='CSV';
UPDATE testdatalib  SET `Type` = 'FILE' where `Type` ='CSV';
UPDATE parameter  SET `param` = 'cerberus_testdatalibfile_path' where `param` ='cerberus_testdatalibcsv_path';

-- 1790
INSERT INTO `parameter` (`system`, `param`, `value`, `description`)
  VALUES ('', 'cerberus_reportbytag_nblinestotriggerautohide_int', '50', 'If Report by Tag has more that this value of test case lines, it will automaticaly hide the full OK execution without any pending bug defined.');

-- 1791-1796
ALTER TABLE testcaseexecution MODIFY COLUMN `Start` timestamp(3)  NULL;
ALTER TABLE testcaseexecution MODIFY COLUMN `End` timestamp(3) NULL;
ALTER TABLE testcasestepactionexecution MODIFY COLUMN `Start` timestamp(3) NULL;
ALTER TABLE testcasestepactionexecution MODIFY COLUMN `End` timestamp(3) NULL;
ALTER TABLE testcasestepactioncontrolexecution MODIFY COLUMN `Start` timestamp(3) NULL;
ALTER TABLE testcasestepactioncontrolexecution MODIFY COLUMN `End` timestamp(3) NULL;

-- 1797
INSERT INTO `parameter` (`system`, `param`, `value`, `description`)
  VALUES ('', 'cerberus_featureflipping_tagstatistics_enable', 'false', 'Temporary parameter used during development of new statistics dashboard.');

-- 1798
ALTER TABLE testdatalib ADD IgnoreFirstLine BOOLEAN NOT NULL AFTER `Separator`;

-- 1799
INSERT INTO `parameter` (`system`, `param`, `value`, `description`)
  VALUES ('', 'cerberus_homepage_nbdisplayedscheduledtag', '3', 'Number of scheduled not yet executed tag displayed inside homepage.');

-- 1800
CREATE TABLE `tagstatistic` (
    `Id` INT NOT NULL AUTO_INCREMENT,
    `Tag` VARCHAR(255) NOT NULL,
    `Country` VARCHAR(45) NOT NULL,
    `Environment` VARCHAR(45) NOT NULL,
    `Campaign` VARCHAR(200) NOT NULL,
    `CampaignGroup1` VARCHAR(45),
    `SystemList` text NOT NULL,
    `ApplicationList` text NOT NULL,
    `DateStartExe` TIMESTAMP NOT NULL,
    `DateEndExe` TIMESTAMP NOT NULL,
    `NbExe` INT DEFAULT 0,
    `NbExeUsefull` INT DEFAULT 0,
    `NbOK` int DEFAULT 0,
    `NbKO` int DEFAULT 0,
    `NbFA` int DEFAULT 0,
    `NbNA` int DEFAULT 0,
    `NbNE` int DEFAULT 0,
    `NbWE` int DEFAULT 0,
    `NbPE` int DEFAULT 0,
    `NbQU` int DEFAULT 0,
    `NbQE` int DEFAULT 0,
    `NbCA` int DEFAULT 0,
    `UsrCreated` VARCHAR(45) NOT NULL DEFAULT '',
    `DateCreated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `UsrModif` VARCHAR(45) NOT NULL DEFAULT '',
    `DateModif` timestamp NOT NULL DEFAULT '1970-01-01 01:01:01',
    PRIMARY KEY (`Id`),
    UNIQUE KEY `tag_stat_unique` (`Tag`, `Country`, `Environment`))
    ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- 1801
ALTER TABLE application 
    CHANGE svnurl RepoUrl varchar(150) NULL,
    ADD BugTrackerConnector varchar(100) NULL AFTER RepoUrl,
    ADD BugTrackerParam1 varchar(100) NULL AFTER BugTrackerConnector,
    ADD BugTrackerParam2 varchar(100) NULL AFTER BugTrackerParam1,
    ADD BugTrackerParam3 varchar(100) NULL AFTER BugTrackerParam2;

-- 1802
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`)
  VALUES   ('BUGTRACKERCONNECTOR', 'REDIRECT', 100, 'Redirection to Bug Tracker..')
  ,('INVARIANTPRIVATE', 'BUGTRACKERCONNECTOR', '910', 'Type of the Bug tracker.');

-- 1803
UPDATE application SET BugTrackerConnector = 'REDIRECT';

-- 1804-1805
ALTER TABLE testcaseexecutionqueuedep 
    ADD DepDate TIMESTAMP NULL AFTER DepEvent,
    ADD DepTCDelay INT DEFAULT 0 NULL AFTER DepTestCase;
ALTER TABLE testcasedep 
    ADD DependencyTCDelay INTEGER DEFAULT 0 NULL AFTER DependencyTestcase;

-- 1806
INSERT INTO `parameter` (`system`, `param`, `value`, `description`)
  VALUES ('', 'cerberus_notification_tagexecutionend_googlechat_maxexelines', '20', 'Maximum number of lines of execution displayed inside the end of campaign execution googlechat notification (default to 20).');

-- 1807
ALTER TABLE `tagstatistic` MODIFY `Campaign` VARCHAR(200);

-- 1808-1811
ALTER TABLE `tagstatistic` DROP INDEX `tag_stat_unique`;
ALTER TABLE `tagstatistic` ADD CONSTRAINT `IX_tagstatistic_01` UNIQUE (`Tag`, `Country`, `Environment`);
ALTER TABLE `tagstatistic` ADD CONSTRAINT `FK_tagstatistic_01` FOREIGN KEY (`Campaign`) REFERENCES `campaign` (`Campaign`) ON UPDATE CASCADE ON DELETE SET NULL;
ALTER TABLE `tagstatistic` ADD CONSTRAINT `FK_tagstatistic_02` FOREIGN KEY (`Tag`) REFERENCES `tag` (`Tag`) ON UPDATE CASCADE ON DELETE CASCADE;

-- 1812-1813
ALTER TABLE tag ADD FalseNegative BOOLEAN DEFAULT false NULL AFTER CIResult;
ALTER TABLE testcaseexecution ADD FalseNegative BOOLEAN DEFAULT false NULL AFTER ControlStatus;

-- 1814-1817
ALTER TABLE robot CHANGE COLUMN `active` `IsActive` VARCHAR(45);
UPDATE robot SET `IsActive` = 1 WHERE `IsActive` = 'Y';
UPDATE robot SET `IsActive` = 0 WHERE `IsActive` != '1';
ALTER TABLE robot MODIFY `IsActive` BOOLEAN DEFAULT 1;

-- 1818-1821
ALTER TABLE robotexecutor CHANGE COLUMN `active` `IsActive` VARCHAR(45);
UPDATE robotexecutor SET `IsActive` = 1 WHERE `IsActive` = 'Y';
UPDATE robotexecutor SET `IsActive` = 0 WHERE `IsActive` != '1';
ALTER TABLE robotexecutor MODIFY `IsActive` BOOLEAN DEFAULT 1;

-- 1822-1825
ALTER TABLE robotexecutor CHANGE COLUMN `deviceLockUnlock` `IsDeviceLockUnlock` VARCHAR(45);
UPDATE robotexecutor SET `IsDeviceLockUnlock` = 1 WHERE `IsDeviceLockUnlock` = 'Y';
UPDATE robotexecutor SET `IsDeviceLockUnlock` = 0 WHERE `IsDeviceLockUnlock` != '1';
ALTER TABLE robotexecutor MODIFY `IsDeviceLockUnlock` BOOLEAN DEFAULT 0;

-- 1826
ALTER TABLE countryenvironmentparameters 
    ADD Secret1 varchar(200) DEFAULT '' NOT NULL AFTER Var4,
    ADD Secret2 varchar(200) DEFAULT '' NOT NULL AFTER Secret1,
    ADD `IsActive` BOOLEAN DEFAULT 1 AFTER Application;

-- 1827
INSERT INTO `parameter` (`system`, `param`, `value`, `description`)
  VALUES ('', 'cerberus_jiracloud_url', '', 'JIRA Cloud Site URL. Ex : http://yourcompany.atlassian.net/'),
    ('', 'cerberus_jiradc_url', '', 'JIRA DC Site URL. Ex : http://yourcompany.atlassian.net/');

-- 1828
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) VALUES 
    ('EXTERNALPROVIDER', 'Jira-Cloud', '150', 'JIRA Cloud', ''),
    ('EXTERNALPROVIDER', 'Jira-DC', '160', 'JIRA DC', '');

-- 1829
ALTER TABLE appservice 
    ADD AuthType varchar(200) DEFAULT 'none' NOT NULL AFTER SimulationParameters,
    ADD AuthUser varchar(500) DEFAULT '' NOT NULL AFTER AuthType,
    ADD AuthPassword varchar(500) DEFAULT '' NOT NULL AFTER AuthUser,
    ADD `AuthAddTo` varchar(500) DEFAULT '' NOT NULL AFTER AuthPassword;

-- 1830
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`)
  VALUES   
   ('AUTHTYPE', 'none', 100, 'No Authorization')
  ,('AUTHTYPE', 'API Key', 150, 'API Key Authorization')
  ,('AUTHTYPE', 'Bearer Token', 200, 'Bearer Token Authorization')
  ,('AUTHTYPE', 'Basic Auth', 250, 'Basic Authorization')
  ,('INVARIANTPRIVATE', 'AUTHTYPE', '1000', 'Authorization Type')
  ,('AUTHADDTO', 'Query String', 100, 'Authorization Parameters in Query String')
  ,('AUTHADDTO', 'Header', 150, 'Authorization Parameters in http headers')
  ,('INVARIANTPRIVATE', 'AUTHADDTO', '1050', 'Authorization API Key Parameter Method');

-- 1831
INSERT INTO `parameter` (`system`, `param`, `value`, `description`)
  VALUES ('', 'cerberus_jiracloud_apiuser', '', 'JIRA Cloud User that will be used to create JIRA issues. Ex : myuser@gmail.com'),
    ('', 'cerberus_jiracloud_apiuser_apitoken', '', 'JIRA Cloud User API Token that will be used to create JIRA issues.');

-- 1832
INSERT INTO `parameter` (`system`, `param`, `value`, `description`)
  VALUES ('', 'cerberus_autobugcreation_enable', 'false', 'Activate the automatic creation of a bug following an execution.');

-- 1833
INSERT IGNORE INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) VALUES 
    ('PRIORITY', '0', 0, 'No Priority defined', '');

-- 1834
UPDATE application set BugTrackerConnector = '' WHERE BugTrackerConnector = 'REDIRECT';

-- 1835
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`)
  VALUES   ('BUGTRACKERCONNECTOR', 'GITHUB', 200, 'Github Bug Tracker connector')
        ,('BUGTRACKERCONNECTOR', 'NONE', 50, 'No Server to Server connector')
        ,('BUGTRACKERCONNECTOR', 'JIRA', 100, 'JIRA Bug Tracker connector') ;

-- 1836
DELETE FROM invariant WHERE idname ='BUGTRACKERCONNECTOR' and value='REDIRECT';

-- 1837
INSERT INTO `parameter` (`system`, `param`, `value`, `description`)
  VALUES ('', 'cerberus_github_apitoken', '', 'Github Personal Access Token that will be used to create issues from API.');

-- 1838 - 1839 ADD VALUE3
ALTER TABLE `testcasecountryproperties`
  ADD COLUMN `Value3` TEXT NULL DEFAULT NULL AFTER `Value2`;
ALTER TABLE `testcaseexecutiondata`
  ADD COLUMN `Value3Init` TEXT NULL DEFAULT NULL AFTER `Value2Init`,
  ADD COLUMN `Value3` TEXT NULL DEFAULT NULL AFTER `Value2`;

-- 1840 - 1846 UPDATE PROPERTIES ACCORDINGLY WITH NEW PROPERTY FEATURE
UPDATE `testcasecountryproperties` SET `Value3` = 'value' , `nature` = 'STATIC'  WHERE `type` = 'getFromHTML';
UPDATE `testcasecountryproperties` SET  `Type` = 'getFromHTML' , `Value3` = 'value' , `nature` = 'STATIC'  WHERE `type` = 'getFromHTMLVisible';
UPDATE `testcasecountryproperties` SET  `Type` = 'getFromHTML' , `Value3` = 'coordinate' , `nature` = 'STATIC'  WHERE `type` = 'getElementPosition';
UPDATE `testcasecountryproperties` SET  `Type` = 'getFromHTML' , `Value3` = 'attribute' , `nature` = 'STATIC'  WHERE `type` = 'getAttributeFromHTML';
UPDATE `testcasecountryproperties` SET  `Type` = 'getFromXml' , `Value3` = 'raw' , `nature` = 'STATIC'  WHERE `type` = 'getRawFromXml';
UPDATE `testcasecountryproperties` SET  `Value3` = 'valueList' , `nature` = 'STATIC'  WHERE `type` = 'getFromJson';
UPDATE `testcasecountryproperties` SET  `Type` = 'getFromJson' , `Value3` = 'rawList' , `nature` = 'STATIC'  WHERE `type` = 'getRawFromJson';

-- 1847 - 1848 RANK VALUE DEFAULT TO 0 INSTEAD OF 1
ALTER TABLE `testcasecountryproperties` CHANGE COLUMN `Rank` `Rank` INT NOT NULL DEFAULT '0' ;
UPDATE `testcasecountryproperties` SET  `Rank` = 0  WHERE `Rank` = 1;

-- 1849 1856 REMOVE  DEPRECATED PROPERTIES
DELETE FROM `invariant` WHERE (`idname` = 'PROPERTYTYPE') and (`value` = 'executeSoapFromLib');
DELETE FROM `invariant` WHERE (`idname` = 'PROPERTYTYPE') and (`value` = 'executeSqlFromLib');
DELETE FROM `invariant` WHERE (`idname` = 'PROPERTYTYPE') and (`value` = 'getDifferencesFromXml');
DELETE FROM `invariant` WHERE (`idname` = 'PROPERTYTYPE') and (`value` = 'getAttributeFromHtml');
DELETE FROM `invariant` WHERE (`idname` = 'PROPERTYTYPE') and (`value` = 'getElementPosition');
DELETE FROM `invariant` WHERE (`idname` = 'PROPERTYTYPE') and (`value` = 'getFromHtmlVisible');
DELETE FROM `invariant` WHERE (`idname` = 'PROPERTYTYPE') and (`value` = 'getRawFromJson');
DELETE FROM `invariant` WHERE (`idname` = 'PROPERTYTYPE') and (`value` = 'getRawFromXml');

-- 1857 ADD COLUMN ACCEPTNOTIFICATIONS ON ROBOT
ALTER TABLE `robot` ADD COLUMN `AcceptNotifications` TINYINT(1) NULL DEFAULT 0 AFTER `ProfileFolder`;

-- 1858
INSERT INTO `parameter` (`system`, `param`, `value`, `description`)
  VALUES ('', 'cerberus_executionloghar_enable', 'false', 'Enable saving of har file at the end of non OK executions.');

-- 1859
UPDATE parameter SET description='Enable saving of har file at the end of executions (robotlog=2 for all executions or robotlog=1 for all non OK executions).' WHERE param='cerberus_executionloghar_enable';

-- 1860-1868
ALTER TABLE testcaseexecution MODIFY COLUMN `Start` timestamp(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3);
UPDATE testcaseexecution SET `End`='1970-01-01 01:01:01.000' WHERE `End`=null;
ALTER TABLE testcaseexecution MODIFY COLUMN `End` timestamp(3) NOT NULL DEFAULT '1970-01-01 01:01:01.000';
ALTER TABLE testcasestepactionexecution MODIFY COLUMN `Start` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3);
UPDATE testcasestepactionexecution SET `End`='1970-01-01 01:01:01.000' WHERE `End`=null;
ALTER TABLE testcasestepactionexecution MODIFY COLUMN `End` timestamp(3) NOT NULL DEFAULT '1970-01-01 01:01:01.000';
ALTER TABLE testcasestepactioncontrolexecution MODIFY COLUMN `Start` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3);
UPDATE testcasestepactioncontrolexecution SET `End`='1970-01-01 01:01:01.000' WHERE `End`=null;
ALTER TABLE testcasestepactioncontrolexecution MODIFY COLUMN `End` timestamp(3) NOT NULL DEFAULT '1970-01-01 01:01:01.000';

-- 1869
DELETE FROM `parameter` WHERE `param` in ('cerberus_apikey_value1','cerberus_apikey_value2','cerberus_apikey_value3','cerberus_apikey_value4','cerberus_apikey_value5') ;

-- 1870
UPDATE application SET BugTrackerConnector='NONE' where BugTrackerConnector ='';

-- 1871-1872
UPDATE application SET `System`=REPLACE(`System`, '%40', '@');
UPDATE testcase SET `UsrModif`=REPLACE(REPLACE(`UsrModif`, '&#64;', '@'), '%40', '@'), `UsrCreated`=REPLACE(REPLACE(`UsrCreated`, '&#64;', '@'), '%40', '@');

-- 1873
UPDATE testcasecountryproperties SET `Type` = 'getFromHtml' WHERE `Type` = 'getFromHTML';

-- 1874
UPDATE robot SET lbexemethod='BYRANKING' WHERE lbexemethod='';

-- 1875-1877
ALTER TABLE myversion MODIFY COLUMN Value BIGINT NULL;
INSERT INTO myversion (`Key`,Value) VALUES ('scheduler_active_instance_version',0);
INSERT INTO myversion (`Key`,Value) VALUES ('documentation_database_last_refresh',0);

-- 1878
INSERT IGNORE INTO `invariant` (`idname`, `value`, `sort`, `description`)
  VALUES   ('CAPABILITY', 'unhandledPromptBehavior', 10, 'Defines how the driver should respond when a non-alert action is taken while an alert is present. Possible values : dismiss, accept, ignore, dismiss and notify, accept and notify');

-- 1879
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`, `VeryShortDesc`) VALUES ('EXTERNALPROVIDER', 'TestLink', '120', 'Test Link', '');

-- 1880-1881
UPDATE testcasedep SET `Type`='TCEXEENDOK' where `Type`='TCEXEEND';
UPDATE testcaseexecutionqueuedep SET `Type`='TCEXEENDOK' where `Type`='TCEXEEND';

-- 1882
ALTER TABLE testcaseexecutionqueue MODIFY COLUMN `State` VARCHAR(20) NOT NULL DEFAULT 'QUEUED';

-- 1883
ALTER TABLE `tag` ADD COLUMN `CIScoreMax` INT DEFAULT 0 AFTER `CIScoreThreshold`;

-- 1884-1885
UPDATE testcasestepactioncontrol SET Control='verifyUrlEqual' WHERE Control='verifyUrl';
UPDATE testcasestepactioncontrol SET Control='verifyTitleEqual' WHERE Control='verifyTitle';

-- 1886
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`)
  VALUES   ('BUGTRACKERCONNECTOR', 'AZUREDEVOPS', 250, 'Azure Devops Bug Tracker connector') ;

-- 1887
INSERT INTO `parameter` (`system`, `param`, `value`, `description`)
  VALUES ('', 'cerberus_azuredevops_accesstoken', '', 'Azure Devops Personal Access Token that will be used to create issues from API.');

-- 1888
INSERT INTO `invariant` (`idname`, `value`, `sort`, `description`)
  VALUES   ('BUGTRACKERCONNECTOR', 'GITLAB', 220, 'Gitlab Bug Tracker connector') ;

-- 1889
INSERT INTO `parameter` (`system`, `param`, `value`, `description`)
  VALUES ('', 'cerberus_gitlab_apitoken', '', 'Gitlab Personal Access Token that will be used to create issues from API.');

-- 1890
ALTER TABLE testcase ADD DateLastExecuted timestamp NOT NULL DEFAULT '1970-01-01 01:01:01' AFTER Version;

-- 1891
UPDATE testcase a
    INNER JOIN (select test, testcase, max(DateCreated) maxexe from testcaseexecution t group by test, testcase) b
    ON a.test = b.test and a.testcase = b.testcase
    SET DateLastExecuted = maxexe ;

-- 1892
ALTER TABLE `tag`
    ADD `nbFlaky` int DEFAULT 0 AFTER `FalseNegative`,
    ADD `nbMuted` int DEFAULT 0 AFTER `NbFlaky`,
    ADD `FalseNegativeRootCause` varchar(200) DEFAULT '' AFTER `FalseNegative`;

-- 1893-1896
ALTER TABLE `testcase` ADD `isMuted` BOOLEAN DEFAULT 0 AFTER `Priority`;
UPDATE testcase SET `isMuted` = 1 WHERE `Priority`=0;
ALTER TABLE `testcaseexecution` ADD `TestCaseIsMuted` BOOLEAN DEFAULT 0 AFTER `TestCasePriority`;
UPDATE testcaseexecution SET `TestCaseisMuted` = 1 WHERE `TestCasePriority`=0;

-- 1897
DELETE FROM parameter WHERE `param` = 'cerberus_featureflipping_tagstatistics_enable';

-- 1898
INSERT INTO `parameter` (`system`, `param`, `value`, `description`)
  VALUES ('', 'cerberus_tagcombofilterpersystem_boolean', 'true', 'Define if tag (campaign execution) combo boxes are filtered by system or not. Filtering by system could have some performance issue that can be improved using that parameter.');

-- 1899-1905
ALTER TABLE robotexecutor RENAME COLUMN executorExtensionHost TO ExecutorProxyServiceHost;
ALTER TABLE robotexecutor RENAME COLUMN executorExtensionPort TO ExecutorProxyServicePort;
ALTER TABLE robotexecutor RENAME COLUMN executorProxyHost TO ExecutorBrowserProxyHost;
ALTER TABLE robotexecutor RENAME COLUMN executorProxyPort TO ExecutorBrowserProxyPort;
ALTER TABLE robotexecutor RENAME COLUMN NodeProxyPort TO ExecutorExtensionProxyPort;
ALTER TABLE robotexecutor RENAME COLUMN host_user TO HostUser;
ALTER TABLE robotexecutor RENAME COLUMN host_password TO HostPassword;

-- 1906
ALTER TABLE robotexecutor ADD COLUMN ExecutorExtensionPort int DEFAULT NULL AFTER ExecutorBrowserProxyPort;

-- 1907
ALTER TABLE robotexecutor MODIFY COLUMN ExecutorExtensionPort int DEFAULT NULL AFTER HostPassword;

-- 1908
DELETE FROM `invariant` WHERE `idname` = 'BROWSER' AND `value` = 'opera';

-- 1909
INSERT INTO `parameter` (`system`, `param`, `value`, `description`)
    VALUES ('', 'cerberus_anthropic_apikey', 'my_apikey', 'Your Anthropic API Key : https://console.anthropic.com/settings/keys');

-- 1910
CREATE TABLE `userprompt` (`Id` INT NOT NULL AUTO_INCREMENT,`login` varchar(255) NOT NULL,`sessionID` varchar(255) NOT NULL,`iaModel` varchar(255) NOT NULL, `iaMaxTokens` INT, `title` VARCHAR(255),`UsrCreated` VARCHAR(45) NOT NULL DEFAULT '',`DateCreated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,`UsrModif` VARCHAR(45) NOT NULL DEFAULT '',`DateModif` TIMESTAMP NOT NULL DEFAULT '1970-01-01 01:01:01', PRIMARY KEY (`Id`), UNIQUE KEY `ID1` (`sessionID`), CONSTRAINT `FK_user_login_01` FOREIGN KEY (`login`) REFERENCES `user` (`login`) ON DELETE CASCADE ON UPDATE CASCADE)
    ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 1911
CREATE TABLE `userpromptmessage` (`Id` INT AUTO_INCREMENT, `sessionID` VARCHAR(255) NOT NULL, `role` VARCHAR(255), `message` TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,`UsrCreated` VARCHAR(45) NOT NULL DEFAULT '',`DateCreated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,`UsrModif` VARCHAR(45) NOT NULL DEFAULT '',`DateModif` TIMESTAMP NOT NULL DEFAULT '1970-01-01 01:01:01', PRIMARY KEY (`Id`), CONSTRAINT `FK_userprompt_01` FOREIGN KEY (`sessionID`) REFERENCES `userprompt` (`sessionID`) ON DELETE CASCADE ON UPDATE CASCADE)
    ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 1912
INSERT INTO `parameter` (`system`, `param`, `value`, `description`)
    VALUES ('', 'cerberus_anthropic_maxtoken', '1024', 'Max token to use'),
           ('', 'cerberus_anthropic_defaultmodel', 'claude-3-5-sonnet-latest', 'Which model to use by default');

-- 1913
ALTER TABLE `testcaseexecution`  
    ADD COLUMN `IsLast` BOOLEAN DEFAULT false AFTER `FalseNegative`,
    ADD COLUMN `IsFlacky` BOOLEAN DEFAULT false AFTER `IsLast`,
    ADD COLUMN `DurationMs` BIGINT DEFAULT 0 AFTER `End` ;

-- 1914
ALTER TABLE `tag`  
    ADD COLUMN `DurationMs` BIGINT DEFAULT 0 AFTER `DateEndQueue` ;

-- 1915
UPDATE tag SET DurationMs = TIMESTAMPDIFF(MICROSECOND, DateStartExe , DateEndQueue) / 1000  WHERE DateEndQueue > DateStartExe;

-- 1916
ALTER TABLE `testcaseexecutionqueue`  
    ADD COLUMN `AlreadyExecuted` INT DEFAULT 0 AFTER `Retries`;

-- 1917
UPDATE testcaseexecution SET DurationMs = TIMESTAMPDIFF(MICROSECOND, `Start`  , `End` ) /1000  where `End`  > `Start` ;

-- 1918
ALTER TABLE `testcaseexecution`  
    CHANGE COLUMN `IsLast` `IsUseful` BOOLEAN DEFAULT false;

-- 1919
INSERT INTO `parameter` (`system`, `param`, `value`, `description`)
  VALUES ('', 'cerberus_use_w3c_capabilities', 'false', 'This parameter allows to be compliant with the W3C protocol used by Selenium 4 and Appium 2. If you are still on Selenium 3 or Appium 1, you can let this parameter to false.');

-- 1920
ALTER TABLE `testcaseexecution`  
    CHANGE COLUMN `IsFlacky` `IsFlaky` BOOLEAN DEFAULT false;

-- 1921
CREATE TABLE `testcasehisto` (
    `Id` INT NOT NULL AUTO_INCREMENT,
    `Test` varchar(45) NULL,
    `TestCase` varchar(45) NULL,
    `Version` INT(10) NOT NULL ,
    `DateVersion` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `Description` varchar(500) NOT NULL DEFAULT '',
    `TestCaseContent` MEDIUMTEXT ,
    `UsrCreated` varchar(45) NOT NULL DEFAULT '',
    `DateCreated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `UsrModif` varchar(45) NOT NULL DEFAULT '',
    `DateModif` timestamp NOT NULL DEFAULT '1970-01-01 01:01:01',
        PRIMARY KEY (`ID`),
        UNIQUE KEY `IX_testcasehisto_01` (`Test`,`TestCase`,`Version`),
        CONSTRAINT `FK_testcasehisto_01` FOREIGN KEY (`Test`,`TestCase`) REFERENCES `testcase` (`Test`,`TestCase`) ON DELETE CASCADE ON UPDATE CASCADE)
    ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- 1922
INSERT INTO `parameter` (`system`, `param`, `value`, `description`)
  VALUES ('', 'cerberus_automatescore_changehorizon', '300000', 'This duration in ms will be added before and after any testcase save action when calculating the time spent on maintaining the test cases. Default to 300000 (5 min).');

-- 1923
INSERT INTO `parameter` (`system`, `param`, `value`, `description`)
  VALUES ('', 'cerberus_homepage_nbdisplayedcampaign', '5', 'Maximum number of campaign group displayed inside homepage (that each of them will contain the latest executed tags).'),
    ('', 'cerberus_homepage_nbdisplayedtagpercampaign', '3', 'Maximum number of tags displayed in every campaign group inside homepage.');

-- 1924
UPDATE robot set `ExtraParam` = REPLACE(`ExtraParam`, ' ', ' | ');