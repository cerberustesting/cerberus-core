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
package org.cerberus.core.engine.execution.impl;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.entity.Application;
import org.cerberus.core.crud.entity.CountryEnvParam;
import org.cerberus.core.crud.entity.CountryEnvironmentParameters;
import org.cerberus.core.crud.entity.EventHook;
import org.cerberus.core.crud.entity.Invariant;
import org.cerberus.core.crud.entity.Robot;
import org.cerberus.core.crud.entity.RobotExecutor;
import org.cerberus.core.crud.entity.TestCase;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.crud.factory.IFactoryCountryEnvironmentParameters;
import org.cerberus.core.crud.service.IApplicationService;
import org.cerberus.core.crud.service.ICountryEnvParamService;
import org.cerberus.core.crud.service.ICountryEnvironmentParametersService;
import org.cerberus.core.crud.service.IInvariantService;
import org.cerberus.core.crud.service.IParameterService;
import org.cerberus.core.crud.service.IRobotExecutorService;
import org.cerberus.core.crud.service.IRobotService;
import org.cerberus.core.crud.service.ITagService;
import org.cerberus.core.crud.service.ITestCaseExecutionQueueService;
import org.cerberus.core.crud.service.ITestCaseExecutionService;
import org.cerberus.core.crud.service.ITestCaseService;
import org.cerberus.core.crud.service.ITestService;
import org.cerberus.core.engine.entity.ExecutionUUID;
import org.cerberus.core.engine.entity.MessageGeneral;
import org.cerberus.core.engine.execution.IConditionService;
import org.cerberus.core.engine.execution.IExecutionCheckService;
import org.cerberus.core.engine.execution.IExecutionStartService;
import org.cerberus.core.engine.queuemanagement.IExecutionThreadPoolService;
import org.cerberus.core.enums.MessageGeneralEnum;
import org.cerberus.core.event.IEventService;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.ParameterParserUtil;
import org.cerberus.core.util.StringUtil;
import org.cerberus.core.websocket.QueueStatus;
import org.cerberus.core.websocket.QueueStatusWebSocket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author bcivel
 */
@Service
public class ExecutionStartService implements IExecutionStartService {

    @Autowired
    private IExecutionCheckService executionCheckService;
    @Autowired
    private ITestCaseService testCaseService;
    @Autowired
    private IConditionService conditionService;
    @Autowired
    private ITestService testService;
    @Autowired
    private ITestCaseExecutionService testCaseExecutionService;
    @Autowired
    private ICountryEnvParamService countryEnvParamService;
    @Autowired
    private ICountryEnvironmentParametersService countryEnvironmentParametersService;
    @Autowired
    private IApplicationService applicationService;
    @Autowired
    private IFactoryCountryEnvironmentParameters factorycountryEnvironmentParameters;
    @Autowired
    private IInvariantService invariantService;
    @Autowired
    private ExecutionUUID executionUUIDObject;
    @Autowired
    private ITagService tagService;
    @Autowired
    private IParameterService parameterService;
    @Autowired
    private ITestCaseExecutionQueueService inQueueService;
    @Autowired
    private IRobotService robotService;
    @Autowired
    private IRobotExecutorService robotExecutorService;
    @Autowired
    private IExecutionThreadPoolService executionThreadPoolService;
    @Autowired
    private IEventService eventService;
    @Autowired
    private QueueStatusWebSocket queueStatusWebSocket;

    private static final Logger LOG = LogManager.getLogger(ExecutionStartService.class);

    @Override
    public TestCaseExecution startExecution(TestCaseExecution execution) throws CerberusException {
        // Start timestamp.
        long executionStart = new Date().getTime();
        LOG.debug("Initializing Start Timestamp : {}", executionStart);
        execution.setStart(executionStart);

        // Checking is the instance allow to open a new execution. It may be in the process to restart.
        if (!executionThreadPoolService.isInstanceActive()) {
            MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_INSTANCE_INACTIVE);
            LOG.debug(mes.getDescription());
            throw new CerberusException(mes);
        }

        // Checking the parameters.
        LOG.debug("Checking the parameters.");
        Invariant myInvariant;
        try {
            myInvariant = invariantService.convert(invariantService.readByKey("OUTPUTFORMAT", execution.getOutputFormat()));
        } catch (CerberusException ex) {
            MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_OUTPUTFORMAT_INVALID);
            mes.setDescription(mes.getDescription().replace("%PARAM%", execution.getOutputFormat()));
            LOG.debug(mes.getDescription());
            throw new CerberusException(mes);
        }
        try {
            myInvariant = invariantService.convert(invariantService.readByKey("VERBOSE", String.valueOf(execution.getVerbose())));
        } catch (CerberusException ex) {
            MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_VERBOSE_INVALID);
            mes.setDescription(mes.getDescription().replace("%PARAM%", String.valueOf(execution.getVerbose())));
            LOG.debug(mes.getDescription());
            throw new CerberusException(mes);
        }
        try {
            myInvariant = invariantService.convert(invariantService.readByKey("SCREENSHOT", String.valueOf(execution.getScreenshot())));
        } catch (CerberusException ex) {
            MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_SCREENSHOT_INVALID);
            mes.setDescription(mes.getDescription().replace("%PARAM%", String.valueOf(execution.getScreenshot())));
            LOG.debug(mes.getDescription());
            throw new CerberusException(mes);
        }
        LOG.debug("Parameters checked.");

        // Load TestCase information and set TCase to the TestCaseExecution object.
        execution.setResultMessage(new MessageGeneral(MessageGeneralEnum.EXECUTION_PE_LOADINGDATA));
        LOG.debug("Loading Test Case Information. {}-{}", execution.getTest(), execution.getTestCase());
        // Integrate this.loadTestCaseService.loadTestCase(tCExecution); inside with Dependency.
        try {
            // TestCase tCase = testCaseService.findTestCaseByKey(tCExecution.getTest(), tCExecution.getTestCase());
            TestCase tCase = testCaseService.convert(testCaseService.readByKey(execution.getTest(), execution.getTestCase()));
            if (tCase != null) {
                execution.setTestCaseObj(tCase);
                execution.setDescription(tCase.getDescription());
                execution.setConditionOperator(tCase.getConditionOperator());

                // Clean condition depending on the operatot.
                String condval1 = conditionService.cleanValue1(tCase.getConditionOperator(), tCase.getConditionValue1());
                String condval2 = conditionService.cleanValue2(tCase.getConditionOperator(), tCase.getConditionValue2());
                String condval3 = conditionService.cleanValue3(tCase.getConditionOperator(), tCase.getConditionValue3());

                execution.setConditionVal1(condval1);
                execution.setConditionVal1Init(condval1);
                execution.setConditionVal2(condval2);
                execution.setConditionVal2Init(condval2);
                execution.setConditionVal3(condval3);
                execution.setConditionVal3Init(condval3);

                execution.setTestCaseVersion(tCase.getVersion());
                execution.setTestCasePriority(tCase.getPriority());
                execution.setTestCaseIsMuted(tCase.isMuted());
                execution.setConditionOptions(tCase.getConditionOptionsActive());
            } else {
                throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
            }
            /*
             * Copy the status of the testcase to the status column of the
             * Execution. This is done to know how stable was the testcase at
             * the time of the execution.
             */
            execution.setStatus(tCase.getStatus());
        } catch (CerberusException ex) {
            MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_TESTCASE_NOT_FOUND);
            mes.setDescription(mes.getDescription().replace("%TEST%", execution.getTest()));
            mes.setDescription(mes.getDescription().replace("%TESTCASE%", execution.getTestCase()));
            LOG.debug(mes.getDescription());
            throw new CerberusException(mes);
        }
        LOG.debug("Test Case Information Loaded - {}-{}", execution.getTest(), execution.getTestCase());

        // Load Test information and Set TestObject to the TestCaseExecution object.
        LOG.debug("Loading Test Information");
        try {
            execution.setTestObj(this.testService.convert(this.testService.readByKey(execution.getTest())));
        } catch (CerberusException ex) {
            MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_TEST_NOT_FOUND);
            mes.setDescription(mes.getDescription().replace("%TEST%", execution.getTest()));
            LOG.debug(mes.getDescription());
            throw new CerberusException(mes);
        }
        LOG.debug("Test Information Loaded - {}", execution.getTest());

        // Load Application information and Set Application to the TestCaseExecution object.
        LOG.debug("Loading Application Information");
        try {
            execution.setApplication(execution.getTestCaseObj().getApplication());
            execution.setApplicationObj(applicationService.convert(this.applicationService.readByKey(execution.getTestCaseObj().getApplication())));
            // Setting Application Type to value coming from Application.
            execution.setAppTypeEngine(execution.getApplicationObj().getType());
            // Setting System from queue.
            execution.getTestCaseExecutionQueue().setSystem(execution.getApplicationObj().getSystem());
            LOG.debug("Application Information Loaded - {}-{}", execution.getApplicationObj().getApplication(), execution.getApplicationObj().getDescription());
        } catch (CerberusException ex) {
            MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_APPLICATION_NOT_FOUND);
            mes.setDescription(mes.getDescription().replace("%APPLI%", execution.getTestCaseObj().getApplication()));
            LOG.debug(mes.getDescription());
            throw new CerberusException(mes);
        }

        // Init System from Application.
        execution.setSystem(execution.getApplicationObj().getSystem());

        // Load Country information and Set it to the TestCaseExecution object.
        LOG.debug("Loading Country Information");
        try {
            execution.setCountryObj(invariantService.convert(invariantService.readByKey("COUNTRY", execution.getCountry())));
            if (execution.getCountryObj() != null) {
                LOG.debug("Country Information Loaded - {} - {}", execution.getCountryObj().getValue(), execution.getCountryObj().getDescription());
            } else {
                MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_COUNTRY_NOT_FOUND);
                mes.setDescription(mes.getDescription().replace("%COUNTRY%", execution.getCountry()));
                LOG.debug(mes.getDescription());
                throw new CerberusException(mes);
            }
        } catch (CerberusException ex) {
            MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_COUNTRY_NOT_FOUND);
            mes.setDescription(mes.getDescription().replace("%COUNTRY%", execution.getCountry()));
            LOG.debug(mes.getDescription());
            throw new CerberusException(mes);
        }

        /*
         * Checking if execution is manual or automaticaly configured. If
         * Manual, CountryEnvironmentParameters object is manually created with
         * the servlet parameters. If automatic, parameters are build from the
         * CountryEnvironmentParameters. table in the database. Environmentdata
         * will always be filled with the environment. Environment will be
         * forced to MANUAL if execution is manual.
         *
         */
        LOG.debug("Checking if connectivity parameters are manual or automatic from the database. '{}'", execution.getManualURL());
        String appURL = "";
        if (execution.getManualURL() == 1) {
            LOG.debug("Execution will be done with manual application connectivity setting.");
            if (StringUtil.isEmptyOrNull(execution.getMyHost())) {
                MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_MANUALURL_INVALID);
                LOG.debug(mes.getDescription());
                throw new CerberusException(mes);
            } else {
                CountryEnvironmentParameters cea;
                cea = this.factorycountryEnvironmentParameters.create(execution.getApplicationObj().getSystem(), execution.getCountry(), execution.getEnvironment(), execution.getApplicationObj().getApplication(),
                        true, execution.getMyHost(), "", execution.getMyContextRoot(), execution.getMyLoginRelativeURL(), "", "", "", "", "", "", CountryEnvironmentParameters.DEFAULT_POOLSIZE, "", "", null, null, null, null);
                cea.setIp(execution.getMyHost());
                cea.setUrl(execution.getMyContextRoot());
                appURL = StringUtil.getURLFromString(cea.getIp(), cea.getUrl(), "", "");
                execution.addSecret(StringUtil.getPasswordFromUrl(appURL));
                execution.setUrl(appURL);
                // If domain is empty we guess it from URL.
                if (StringUtil.isEmptyOrNull(cea.getDomain())) {
                    cea.setDomain(StringUtil.getDomainFromUrl(appURL));
                }
                cea.setUrlLogin(execution.getMyLoginRelativeURL());
                execution.setCountryEnvApplicationParam(cea);
                LOG.debug(" -> Execution will be done with manual application connectivity setting. IP/URL/LOGIN : {}-{}-{}", cea.getIp(), cea.getUrl(), cea.getUrlLogin());
                LOG.debug(" Domain : {}", cea.getDomain());
            }
            /*
             * If execution is manual, we force the env at 'MANUAL-ENVDATA'
             * string. We keep envData information in order to trace the env
             * data that has been used.
             */
            execution.setEnvironment("MANUAL-" + execution.getEnvironmentData());
        } else {
            // Automatic application configuration execution.
            LOG.debug("Execution will be done with automatic application connectivity setting.");
            // Load Country/Environment/Application information and set them to the TestCaseExecution object
            LOG.debug("Loading Country/Environment/Application Information. {}-{}-{}", execution.getCountry(), execution.getEnvironment(), execution.getApplicationObj().getApplication());
            CountryEnvironmentParameters cea;
            try {
                cea = this.countryEnvironmentParametersService.convert(this.countryEnvironmentParametersService.readByKey(
                        execution.getApplicationObj().getSystem(), execution.getCountry(), execution.getEnvironment(), execution.getApplicationObj().getApplication()));
                if (cea != null && (cea.isActive())) {
                    if (execution.getManualURL() == 2) {
                        // add possibility to override URL with MyHost if MyHost is available
                        if (!StringUtil.isEmptyOrNull(execution.getMyHost())) {
                            cea.setIp(execution.getMyHost());
                        }
                        if (!StringUtil.isEmptyOrNull(execution.getMyContextRoot())) {
                            cea.setUrl(execution.getMyContextRoot());
                        }
                        if (!StringUtil.isEmptyOrNull(execution.getMyLoginRelativeURL())) {
                            cea.setUrlLogin(execution.getMyLoginRelativeURL());
                        }
                    }
                    appURL = StringUtil.getURLFromString(cea.getIp(), cea.getUrl(), "", "");
                    execution.addSecret(StringUtil.getPasswordFromUrl(appURL));
                    execution.setUrl(appURL);
                    if ("GUI".equals(execution.getApplicationObj().getType()) && StringUtil.isEmptyOrNull(cea.getDomain())) {
                        // Domain calculation only make sense for Web applications.
                        // If domain is empty we guess it from URL.
                        cea.setDomain(StringUtil.getDomainFromUrl(execution.getUrl()));
                    }
                    // Protect Secret data coming from application-environment.
                    execution.addSecret(cea.getSecret1());
                    execution.addSecret(cea.getSecret2());
                    execution.setCountryEnvApplicationParam(cea);

                    // Load all associated application informations (same system, country and env as the execution).
                    HashMap<String, CountryEnvironmentParameters> countryEnvApplicationParamHash;
                    List<CountryEnvironmentParameters> ceaList;
                    ceaList = this.countryEnvironmentParametersService.convert(this.countryEnvironmentParametersService.readByVarious(
                            execution.getApplicationObj().getSystem(), execution.getCountry(), execution.getEnvironment(), null));
                    execution.addcountryEnvApplicationParams(ceaList);
                    // Load all linked application informations of the application testcase.
                    ceaList = this.countryEnvironmentParametersService.convert(this.countryEnvironmentParametersService.readDependenciesByVarious(
                            execution.getApplicationObj().getSystem(), execution.getCountry(), execution.getEnvironment()));
                    execution.addcountryEnvApplicationParams(ceaList);

                } else {
                    MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_COUNTRYENVAPP_NOT_FOUND);
                    mes.setDescription(mes.getDescription().replace("%COUNTRY%", execution.getCountry()));
                    mes.setDescription(mes.getDescription().replace("%ENV%", execution.getEnvironment()));
                    mes.setDescription(mes.getDescription().replace("%APPLI%", execution.getTestCaseObj().getApplication()));
                    LOG.error(mes.getDescription());
                    throw new CerberusException(mes);
                }
                // Forcing the IP URL and Login config from DevIP, DevURL and DevLogin parameter only if DevURL is defined.
            } catch (CerberusException ex) {
                MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_COUNTRYENVAPP_NOT_FOUND);
                mes.setDescription(mes.getDescription().replace("%COUNTRY%", execution.getCountry()));
                mes.setDescription(mes.getDescription().replace("%ENV%", execution.getEnvironment()));
                mes.setDescription(mes.getDescription().replace("%APPLI%", execution.getTestCaseObj().getApplication()));
                LOG.error(mes.getDescription(), ex);
                throw new CerberusException(mes);
            }
            LOG.debug("Country/Environment/Application Information Loaded. {} - {} - {}", execution.getCountry(), execution.getEnvironment(), execution.getApplicationObj().getApplication());
            LOG.debug(" -> Execution will be done with automatic application connectivity setting. IP/URL/LOGIN : {}-{}-{}", cea.getIp(), cea.getUrl(), cea.getUrlLogin());
            LOG.debug(" Domain : {}", cea.getDomain());
            execution.setEnvironmentData(execution.getEnvironment());
        }

        // Load Environment object from invariant table.
        LOG.debug("Loading Environment Information. {}", execution.getEnvironmentData());
        try {
            execution.setEnvironmentDataObj(invariantService.convert(invariantService.readByKey("ENVIRONMENT", execution.getEnvironmentData())));
        } catch (CerberusException ex) {
            if (execution.getManualURL() >= 1) {
                MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_ENVIRONMENT_DOESNOTEXIST_MAN);
                mes.setDescription(mes.getDescription().replace("%ENV%", execution.getEnvironmentData()));
                LOG.debug(mes.getDescription());
                throw new CerberusException(mes);
            } else {
                MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_ENVIRONMENT_DOESNOTEXIST);
                mes.setDescription(mes.getDescription().replace("%ENV%", execution.getEnvironmentData()));
                LOG.debug(mes.getDescription());
                throw new CerberusException(mes);
            }
        }
        LOG.debug("Environment Information Loaded");

        // Load Environment object from invariant table.
        LOG.debug("Loading Environment Information. {}", execution.getEnvironment());
        try {
            execution.setEnvironmentObj(invariantService.convert(invariantService.readByKey("ENVIRONMENT", execution.getEnvironment())));
        } catch (CerberusException ex) {
            if (execution.getManualURL() >= 1) {
                MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_ENVIRONMENT_DOESNOTEXIST_MAN);
                mes.setDescription(mes.getDescription().replace("%ENV%", execution.getEnvironmentData()));
                LOG.debug(mes.getDescription());
                throw new CerberusException(mes);
            } else {
                MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_ENVIRONMENT_DOESNOTEXIST);
                mes.setDescription(mes.getDescription().replace("%ENV%", execution.getEnvironmentData()));
                LOG.debug(mes.getDescription());
                throw new CerberusException(mes);
            }
        }
        LOG.debug("Environment Information Loaded");

        // Load Priority object from invariant table.
        LOG.debug("Loading Priority Information. {}", execution.getTestCaseObj().getPriority());
        try {
            execution.setPriorityObj(invariantService.convert(invariantService.readByKey("PRIORITY", String.valueOf(execution.getTestCaseObj().getPriority()))));
        } catch (CerberusException ex) {
            MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_PRIORITY_DOESNOTEXIST);
            mes.setDescription(mes.getDescription().replace("%PRIO%", String.valueOf(execution.getTestCaseObj().getPriority())));
            LOG.debug(mes.getDescription());
            throw new CerberusException(mes);
        }
        LOG.debug("Priority Information Loaded");

        /*
         * Load Country/Environment information and set them to the
         * TestCaseExecution object. Environment considered here is the data
         * environment.
         */
        LOG.debug("Loading Country/Environment Information. {} - {}", execution.getCountry(), execution.getEnvironmentData());
        CountryEnvParam countEnvParam;
        try {
            countEnvParam = this.countryEnvParamService.convert(this.countryEnvParamService.readByKey(execution.getApplicationObj().getSystem(), execution.getCountry(), execution.getEnvironmentData()));
            execution.setCountryEnvParam(countEnvParam);
            /*
             * Copy the Build/Revision of the environment to the Execution. This
             * is done to keep track of all execution done on a specific version
             * of system
             */
            execution.setBuild(countEnvParam.getBuild());
            execution.setRevision(countEnvParam.getRevision());
        } catch (CerberusException ex) {
            MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_COUNTRYENV_NOT_FOUND);
            mes.setDescription(mes.getDescription().replace("%SYSTEM%", execution.getApplicationObj().getSystem()));
            mes.setDescription(mes.getDescription().replace("%COUNTRY%", execution.getCountry()));
            mes.setDescription(mes.getDescription().replace("%ENV%", execution.getEnvironmentData()));
            LOG.debug(mes.getDescription());
            throw new CerberusException(mes);
        }
        LOG.debug("Country/Environment Information Loaded. {} - {}", execution.getCountry(), execution.getEnvironmentData());

        // If Robot is feeded, we check it exist. If it exist, we overwrite the associated parameters.
        Robot robObj = null;
        RobotExecutor robExeObj = null;
        String robotHost = "";
        String robotPort = "";
        String browser = execution.getBrowser();
        String robotDecli = "";
        String version = "";
        String platform = "";
        if (!StringUtil.isEmptyOrNull(execution.getRobot())) {
            robObj = robotService.readByKey(execution.getRobot());

            if (robObj == null) {
                throw new CerberusException(new MessageGeneral(MessageGeneralEnum.EXECUTION_FA_ROBOTNOTEXIST)
                        .resolveDescription("ROBOT", execution.getRobot()));
            }

            // If Robot parameter is defined and we can find the robot, we overwrite the corresponding parameters.
            browser = ParameterParserUtil.parseStringParam(robObj.getBrowser(), browser);
            robotDecli = ParameterParserUtil.parseStringParam(robObj.getRobotDecli(), "");
            if (StringUtil.isEmptyOrNull(robotDecli)) {
                robotDecli = robObj.getRobot();
            }
            version = ParameterParserUtil.parseStringParam(robObj.getVersion(), version);
            platform = ParameterParserUtil.parseStringParam(robObj.getPlatform(), platform);
            execution.setUserAgent(robObj.getUserAgent());
            execution.setScreenSize(robObj.getScreenSize());
            execution.setBrowser(browser);
            execution.setRobotDecli(robotDecli);
            execution.setVersion(version);
            execution.setPlatform(platform);
            execution.setRobotObj(robObj);

            // We cannot execute a testcase on a desactivated Robot.
            if (!robObj.isActive()) {
                LOG.debug("Robot " + execution.getRobot() + " is not active.");
                throw new CerberusException(new MessageGeneral(MessageGeneralEnum.EXECUTION_FA_ROBOTNOTACTIVE)
                        .resolveDescription("ROBOT", execution.getRobot()));
            }

            // If executor is not set, we get the best one from the list.
            if (StringUtil.isEmptyOrNull(execution.getRobotExecutor())) {
                LOG.debug("Getting the best Executor on Robot : {}", execution.getRobot());
                robExeObj = robotExecutorService.readBestByKey(execution.getRobot());
                if (robExeObj != null) {
                    execution.setRobotExecutor(robExeObj.getExecutor());
                    execution.setRobotExecutorObj(robExeObj);
                    robotExecutorService.updateLastExe(robExeObj.getRobot(), robExeObj.getExecutor());
                } else {
                    throw new CerberusException(new MessageGeneral(MessageGeneralEnum.EXECUTION_FA_ROBOTBESTEXECUTORNOTEXIST)
                            .resolveDescription("ROBOT", execution.getRobot())
                            .resolveDescription("EXECUTOR", execution.getRobotExecutor()));
                }
                LOG.debug(" Executor retreived : {}", robExeObj.getExecutor());
            } else {
                LOG.debug(" Getting Requested Robot / Executor : {} / {}", execution.getRobot(), execution.getRobotExecutor());
                robExeObj = robotExecutorService.convert(robotExecutorService.readByKey(execution.getRobot(), execution.getRobotExecutor()));
                execution.setRobotExecutorObj(robExeObj);
                if (robExeObj == null) {
                    throw new CerberusException(new MessageGeneral(MessageGeneralEnum.EXECUTION_FA_ROBOTEXECUTORNOTEXIST)
                            .resolveDescription("ROBOT", execution.getRobot())
                            .resolveDescription("EXECUTOR", execution.getRobotExecutor()));
                } else {
                    // We cannot execute a testcase on a desactivated Robot.
                    if (!robExeObj.isActive()) {
                        LOG.debug("Robot Executor {} / {} is not active", execution.getRobot(), execution.getRobotExecutor());
                        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.EXECUTION_FA_ROBOTEXECUTORNOTACTIVE)
                                .resolveDescription("ROBOT", execution.getRobot())
                                .resolveDescription("EXECUTOR", execution.getRobotExecutor()));
                    }

                }
            }

            robotHost = ParameterParserUtil.parseStringParam(robExeObj.getHost(), execution.getRobotHost());
            robotPort = ParameterParserUtil.parseStringParam(String.valueOf(robExeObj.getPort()), execution.getRobotPort());
            execution.setRobotHost(robotHost);
            execution.setRobotPort(robotPort);
            execution.setSeleniumIP(robotHost);
            execution.setSeleniumPort(robotPort);
            execution.setSeleniumIPUser(robExeObj.getHostUser());
            execution.setSeleniumIPPassword(robExeObj.getHostPassword());

        } else {
            execution.setRobotDecli(browser);
        }

        /*
         * If Timeout is defined at the execution level, set action wait default
         * to this value, else Get the cerberus_action_wait_default parameter.
         * This parameter will be used by tha wait action if no timeout/event is
         * defined.
         */
        try {
            if (!execution.getTimeout().isEmpty()) {
                execution.setCerberus_action_wait_default(Integer.valueOf(execution.getTimeout()));
            } else {
                execution.setCerberus_action_wait_default(parameterService.getParameterIntegerByKey("cerberus_action_wait_default", execution.getApplicationObj().getSystem(), 90000));
            }
        } catch (NumberFormatException ex) {
            LOG.warn("Parameter cerberus_action_wait_default must be an integer, default value set to 90000 milliseconds. {}", ex.toString());
            execution.setCerberus_action_wait_default(90000);
        }

        // Check if test can be executed TODO : Replace Message with try/catch cerberus exception
        execution.setResultMessage(new MessageGeneral(MessageGeneralEnum.EXECUTION_PE_VALIDATIONSTARTING));
        LOG.debug("Performing the Checks before starting the execution");
        MessageGeneral canExecuteTestCase = this.executionCheckService.checkTestCaseExecution(execution);
        execution.setResultMessage(canExecuteTestCase);
        // We stop if the result is not OK
        if (!(execution.getResultMessage().equals(new MessageGeneral(MessageGeneralEnum.EXECUTION_PE_CHECKINGPARAMETERS)))) {
            return execution;
        }
        LOG.debug("Checks performed -- > OK to continue.");

        // Changing Automatic execution flag depending on test case information.
        if (execution.getManualExecution().equals(TestCaseExecution.MANUAL_A)) {
            if (execution.getTestCaseObj().getType().equals(TestCase.TESTCASE_TYPE_AUTOMATED)
                    || execution.getTestCaseObj().getType().equals(TestCase.TESTCASE_TYPE_PRIVATE)) {
                execution.setManualExecution(TestCaseExecution.MANUAL_N);

            } else {
                execution.setManualExecution(TestCaseExecution.MANUAL_Y);
            }
        }

        // For GUI application, check if Browser is supported.
        if (!execution.getManualExecution().equals("Y") && execution.getAppTypeEngine().equalsIgnoreCase(Application.TYPE_GUI)) {
            try {
                myInvariant = invariantService.convert(invariantService.readByKey("BROWSER", execution.getBrowser()));
            } catch (CerberusException ex) {
                MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_BROWSER_NOT_SUPPORTED);
                mes.setDescription(mes.getDescription().replace("%BROWSER%", execution.getBrowser()));
                LOG.debug(mes.getDescription());
                throw new CerberusException(mes);
            }
        }

        // Register RunID inside database.
        execution.setResultMessage(new MessageGeneral(MessageGeneralEnum.EXECUTION_PE_CREATINGRUNID));
        LOG.debug("Registering Execution ID on database");
        long runID = 0;
        try {
            runID = this.testCaseExecutionService.registerRunID(execution);

            if (runID != 0) {
                execution.setId(runID);
                executionUUIDObject.setExecutionUUID(execution.getExecutionUUID(), execution);
                QueueStatus queueS = QueueStatus.builder()
                        .executionHashMap(executionUUIDObject.getExecutionUUIDList())
                        .globalLimit(executionUUIDObject.getGlobalLimit())
                        .running(executionUUIDObject.getRunning())
                        .queueSize(executionUUIDObject.getQueueSize()).build();
                queueStatusWebSocket.send(queueS, true);
                // Update Queue Execution here if QueueID =! 0.
                if (execution.getQueueID() != 0) {
                    inQueueService.updateToExecuting(execution.getQueueID(), "", runID);
                }

                tagService.manageCampaignStartOfExecution(execution.getTag(), new Timestamp(executionStart));
                
                // Update the testcase with timestamp of last execution.
                testCaseService.updateLastExecuted(execution.getTest(), execution.getTestCase(), new Timestamp(executionStart));

                eventService.triggerEvent(EventHook.EVENTREFERENCE_EXECUTION_START, execution, null, null, null);

            } else {
                MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_COULDNOTCREATE_RUNID);
                execution.setResultMessage(mes);
                LOG.fatal("Could not create RunID, or cannot retrieve the generated Key");
                throw new CerberusException(mes);
            }

        } catch (CerberusException ex) {
            execution.setResultMessage(new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_COULDNOTCREATE_RUNID));
            LOG.warn(ex.getMessageError().getDescription(), ex);
            throw new CerberusException(ex.getMessageError(), ex);
        }

        LOG.debug("Execution ID registered on database : {}", execution.getId());

        if (execution.getManualExecution().equals(TestCaseExecution.MANUAL_Y)) {
            // Set execution executor from testcase executor (only for manual execution).
            execution.setExecutor(execution.getTestCaseObj().getExecutor());
        }

        //Define websocket parameter
        execution.setCerberus_featureflipping_activatewebsocketpush(parameterService.getParameterBooleanByKey("cerberus_featureflipping_activatewebsocketpush", "", false));
        execution.setCerberus_featureflipping_websocketpushperiod(parameterService.getParameterLongByKey("cerberus_featureflipping_websocketpushperiod", "", 5000));

        return execution;
    }

}
