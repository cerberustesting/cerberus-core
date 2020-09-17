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
package org.cerberus.engine.execution.impl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.logging.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.crud.entity.Application;
import org.cerberus.crud.entity.CountryEnvParam;
import org.cerberus.crud.entity.CountryEnvironmentParameters;
import org.cerberus.engine.entity.ExecutionUUID;
import org.cerberus.crud.entity.Invariant;
import org.cerberus.crud.entity.Robot;
import org.cerberus.crud.entity.RobotExecutor;
import org.cerberus.engine.entity.MessageGeneral;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.crud.entity.TestCase;
import org.cerberus.crud.entity.TestCaseExecution;
import org.cerberus.exception.CerberusException;
import org.cerberus.crud.service.IApplicationService;
import org.cerberus.crud.service.ICountryEnvParamService;
import org.cerberus.crud.service.IInvariantService;
import org.cerberus.crud.service.ITestCaseExecutionService;
import org.cerberus.crud.service.ITestCaseService;
import org.cerberus.crud.service.ITestService;
import org.cerberus.engine.execution.IExecutionCheckService;
import org.cerberus.engine.execution.IExecutionStartService;
import org.cerberus.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.cerberus.crud.service.ICountryEnvironmentParametersService;
import org.cerberus.crud.factory.IFactoryCountryEnvironmentParameters;
import org.cerberus.crud.service.IParameterService;
import org.cerberus.crud.service.IRobotExecutorService;
import org.cerberus.crud.service.IRobotService;
import org.cerberus.crud.service.ITestCaseExecutionQueueService;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.engine.execution.IRobotServerService;
import org.cerberus.engine.queuemanagement.IExecutionThreadPoolService;

/**
 *
 * @author bcivel
 */
@Service
public class ExecutionStartService implements IExecutionStartService {

    @Autowired
    private IExecutionCheckService executionCheckService;
    @Autowired
    private ITestCaseService testCaseService;
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
    ExecutionUUID executionUUIDObject;
    @Autowired
    private IRobotServerService serverService;
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

    private static final Logger LOG = LogManager.getLogger(ExecutionStartService.class);

    @Override
    public TestCaseExecution startExecution(TestCaseExecution tCExecution) throws CerberusException {
        /**
         * Start timestamp.
         */
        long executionStart = new Date().getTime();
        LOG.debug("Initializing Start Timestamp : " + executionStart);
        tCExecution.setStart(executionStart);

        // Checking is the instance allow to open a new execution. It may be in the process to restart.
        if (!executionThreadPoolService.isInstanceActive()) {
            MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_INSTANCE_INACTIVE);
            LOG.debug(mes.getDescription());
            throw new CerberusException(mes);
        }

        /**
         * Checking the parameters.
         */
        LOG.debug("Checking the parameters.");
        Invariant myInvariant;
        try {
            myInvariant = invariantService.convert(invariantService.readByKey("OUTPUTFORMAT", tCExecution.getOutputFormat()));
        } catch (CerberusException ex) {
            MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_OUTPUTFORMAT_INVALID);
            mes.setDescription(mes.getDescription().replace("%PARAM%", tCExecution.getOutputFormat()));
            LOG.debug(mes.getDescription());
            throw new CerberusException(mes);
        }
        try {
            myInvariant = invariantService.convert(invariantService.readByKey("VERBOSE", String.valueOf(tCExecution.getVerbose())));
        } catch (CerberusException ex) {
            MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_VERBOSE_INVALID);
            mes.setDescription(mes.getDescription().replace("%PARAM%", String.valueOf(tCExecution.getVerbose())));
            LOG.debug(mes.getDescription());
            throw new CerberusException(mes);
        }
        try {
            myInvariant = invariantService.convert(invariantService.readByKey("SCREENSHOT", String.valueOf(tCExecution.getScreenshot())));
        } catch (CerberusException ex) {
            MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_SCREENSHOT_INVALID);
            mes.setDescription(mes.getDescription().replace("%PARAM%", String.valueOf(tCExecution.getScreenshot())));
            LOG.debug(mes.getDescription());
            throw new CerberusException(mes);
        }
        LOG.debug("Parameters checked.");

        /**
         * Load TestCase information and set TCase to the TestCaseExecution
         * object.
         */
        tCExecution.setResultMessage(new MessageGeneral(MessageGeneralEnum.EXECUTION_PE_LOADINGDATA));
        LOG.debug("Loading Test Case Information. " + tCExecution.getTest() + "-" + tCExecution.getTestCase());
        // Integrate this.loadTestCaseService.loadTestCase(tCExecution); inside with Dependency.
        try {
//            TestCase tCase = testCaseService.findTestCaseByKey(tCExecution.getTest(), tCExecution.getTestCase());
            TestCase tCase = testCaseService.convert(testCaseService.readByKey(tCExecution.getTest(), tCExecution.getTestCase()));
            if (tCase != null) {
                tCExecution.setTestCaseObj(tCase);
                tCExecution.setDescription(tCase.getDescription());
                tCExecution.setConditionOperator(tCase.getConditionOperator());
                tCExecution.setConditionVal1(tCase.getConditionVal1());
                tCExecution.setConditionVal1Init(tCase.getConditionVal1());
                tCExecution.setConditionVal2(tCase.getConditionVal2());
                tCExecution.setConditionVal2Init(tCase.getConditionVal2());
                tCExecution.setConditionVal3(tCase.getConditionVal3());
                tCExecution.setConditionVal3Init(tCase.getConditionVal3());
                tCExecution.setTestCaseVersion(tCase.getVersion());
                tCExecution.setTestCasePriority(tCase.getPriority());
            } else {
                throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
            }
            /**
             * Copy the status of the testcase to the status column of the
             * Execution. This is done to know how stable was the testcase at
             * the time of the execution.
             */
            tCExecution.setStatus(tCase.getStatus());
        } catch (CerberusException ex) {
            MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_TESTCASE_NOT_FOUND);
            mes.setDescription(mes.getDescription().replace("%TEST%", tCExecution.getTest()));
            mes.setDescription(mes.getDescription().replace("%TESTCASE%", tCExecution.getTestCase()));
            LOG.debug(mes.getDescription());
            throw new CerberusException(mes);
        }
        LOG.debug("Test Case Information Loaded - " + tCExecution.getTest() + "-" + tCExecution.getTestCase());

        /**
         * Load Test information and Set TestObject to the TestCaseExecution
         * object.
         */
        LOG.debug("Loading Test Information");
        try {
            tCExecution.setTestObj(this.testService.convert(this.testService.readByKey(tCExecution.getTest())));
        } catch (CerberusException ex) {
            MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_TEST_NOT_FOUND);
            mes.setDescription(mes.getDescription().replace("%TEST%", tCExecution.getTest()));
            LOG.debug(mes.getDescription());
            throw new CerberusException(mes);
        }
        LOG.debug("Test Information Loaded - " + tCExecution.getTest());

        /**
         * Load Application information and Set Application to the
         * TestCaseExecution object.
         */
        LOG.debug("Loading Application Information");
        try {
            tCExecution.setApplication(tCExecution.getTestCaseObj().getApplication());
            tCExecution.setApplicationObj(applicationService.convert(this.applicationService.readByKey(tCExecution.getTestCaseObj().getApplication())));
            // Setting Application Type to value coming from Application.
            tCExecution.setAppTypeEngine(tCExecution.getApplicationObj().getType());
            // Setting System from queue.
            tCExecution.getTestCaseExecutionQueue().setSystem(tCExecution.getApplicationObj().getSystem());
            LOG.debug("Application Information Loaded - " + tCExecution.getApplicationObj().getApplication() + " - " + tCExecution.getApplicationObj().getDescription());
        } catch (CerberusException ex) {
            MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_APPLICATION_NOT_FOUND);
            mes.setDescription(mes.getDescription().replace("%APPLI%", tCExecution.getTestCaseObj().getApplication()));
            LOG.debug(mes.getDescription());
            throw new CerberusException(mes);
        }

        /**
         * Init System from Application.
         */
        tCExecution.setSystem(tCExecution.getApplicationObj().getSystem());

        /**
         * Load Country information and Set it to the TestCaseExecution object.
         */
        LOG.debug("Loading Country Information");
        try {
            tCExecution.setCountryObj(invariantService.convert(invariantService.readByKey("COUNTRY", tCExecution.getCountry())));
            if (tCExecution.getCountryObj() != null) {
                LOG.debug("Country Information Loaded - " + tCExecution.getCountryObj().getValue() + " - " + tCExecution.getCountryObj().getDescription());
            } else {
                MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_COUNTRY_NOT_FOUND);
                mes.setDescription(mes.getDescription().replace("%COUNTRY%", tCExecution.getCountry()));
                LOG.debug(mes.getDescription());
                throw new CerberusException(mes);
            }
        } catch (CerberusException ex) {
            MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_COUNTRY_NOT_FOUND);
            mes.setDescription(mes.getDescription().replace("%COUNTRY%", tCExecution.getCountry()));
            LOG.debug(mes.getDescription());
            throw new CerberusException(mes);
        }

        /**
         * Checking if execution is manual or automaticaly configured. If
         * Manual, CountryEnvironmentParameters object is manually created with
         * the servlet parameters. If automatic, parameters are build from the
         * CountryEnvironmentParameters. table in the database. Environmentdata
         * will always be filled with the environment. Environment will be
         * forced to MANUAL if execution is manual.
         *
         */
        LOG.debug("Checking if connectivity parameters are manual or automatic from the database. '" + tCExecution.isManualURL() + "'");
        if (tCExecution.isManualURL()) {
            LOG.debug("Execution will be done with manual application connectivity setting.");
            if (StringUtil.isNullOrEmpty(tCExecution.getMyHost())) {
                MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_MANUALURL_INVALID);
                LOG.debug(mes.getDescription());
                throw new CerberusException(mes);
            } else {
                CountryEnvironmentParameters cea;
                cea = this.factorycountryEnvironmentParameters.create(tCExecution.getApplicationObj().getSystem(), tCExecution.getCountry(), tCExecution.getEnvironment(), tCExecution.getApplicationObj().getApplication(), tCExecution.getMyHost(), "", tCExecution.getMyContextRoot(), tCExecution.getMyLoginRelativeURL(), "", "", "", "", CountryEnvironmentParameters.DEFAULT_POOLSIZE, "", "");
                cea.setIp(tCExecution.getMyHost());
                cea.setUrl(tCExecution.getMyContextRoot());
                String appURL = StringUtil.getURLFromString(cea.getIp(), cea.getUrl(), "", "");
                tCExecution.setUrl(appURL);
                // If domain is empty we guess it from URL.
                if (StringUtil.isNullOrEmpty(cea.getDomain())) {
                    cea.setDomain(StringUtil.getDomainFromUrl(appURL));
                }
                cea.setUrlLogin(tCExecution.getMyLoginRelativeURL());
                tCExecution.setCountryEnvironmentParameters(cea);
                LOG.debug(" -> Execution will be done with manual application connectivity setting. IP/URL/LOGIN : " + cea.getIp() + "-" + cea.getUrl() + "-" + cea.getUrlLogin());
                LOG.debug(" Domain : " + cea.getDomain());
            }
            /**
             * If execution is manual, we force the env at 'MANUAL-ENVDATA'
             * string. We keep envData information in order to trace the env
             * data that has been used.
             */
            tCExecution.setEnvironment("MANUAL-" + tCExecution.getEnvironmentData());
        } else {
            /**
             * Automatic application configuration execution.
             */
            LOG.debug("Execution will be done with automatic application connectivity setting.");
            /**
             * Load Country/Environment/Application information and set them to
             * the TestCaseExecution object
             */
            LOG.debug("Loading Country/Environment/Application Information. " + tCExecution.getCountry() + "-" + tCExecution.getEnvironment() + "-" + tCExecution.getApplicationObj().getApplication());
            CountryEnvironmentParameters cea;
            try {
                cea = this.countryEnvironmentParametersService.convert(this.countryEnvironmentParametersService.readByKey(
                        tCExecution.getApplicationObj().getSystem(), tCExecution.getCountry(), tCExecution.getEnvironment(), tCExecution.getApplicationObj().getApplication()));
                if (cea != null) {
                    tCExecution.setUrl(StringUtil.getURLFromString(cea.getIp(), cea.getUrl(), "", ""));
                    // add possibility to override URL with MyHost if MyHost is available
                    if (!StringUtil.isNullOrEmpty(tCExecution.getMyHost())) {
                        String contextRoot = !StringUtil.isNullOrEmpty(tCExecution.getMyContextRoot()) ? tCExecution.getMyContextRoot() : "";
                        tCExecution.setUrl(StringUtil.getURLFromString(tCExecution.getMyHost(), contextRoot, "", ""));
                    }
                    if (!StringUtil.isNullOrEmpty(tCExecution.getMyLoginRelativeURL())) {
                        cea.setUrlLogin(tCExecution.getMyLoginRelativeURL());
                    }
                    // If domain is empty we guess it from URL.
                    if (StringUtil.isNullOrEmpty(cea.getDomain())) {
                        cea.setDomain(StringUtil.getDomainFromUrl(tCExecution.getUrl()));
                    }
                    tCExecution.setCountryEnvironmentParameters(cea);
                } else {
                    MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_COUNTRYENVAPP_NOT_FOUND);
                    mes.setDescription(mes.getDescription().replace("%COUNTRY%", tCExecution.getCountry()));
                    mes.setDescription(mes.getDescription().replace("%ENV%", tCExecution.getEnvironment()));
                    mes.setDescription(mes.getDescription().replace("%APPLI%", tCExecution.getTestCaseObj().getApplication()));
                    LOG.error(mes.getDescription());
                    throw new CerberusException(mes);
                }
                /**
                 * Forcing the IP URL and Login config from DevIP, DevURL and
                 * DevLogin parameter only if DevURL is defined.
                 */
            } catch (CerberusException ex) {
                MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_COUNTRYENVAPP_NOT_FOUND);
                mes.setDescription(mes.getDescription().replace("%COUNTRY%", tCExecution.getCountry()));
                mes.setDescription(mes.getDescription().replace("%ENV%", tCExecution.getEnvironment()));
                mes.setDescription(mes.getDescription().replace("%APPLI%", tCExecution.getTestCaseObj().getApplication()));
                LOG.error(mes.getDescription(), ex);
                throw new CerberusException(mes);
            }
            LOG.debug("Country/Environment/Application Information Loaded. " + tCExecution.getCountry() + " - " + tCExecution.getEnvironment() + " - " + tCExecution.getApplicationObj().getApplication());
            LOG.debug(" -> Execution will be done with automatic application connectivity setting. IP/URL/LOGIN : " + cea.getIp() + "-" + cea.getUrl() + "-" + cea.getUrlLogin());
            LOG.debug(" Domain : " + cea.getDomain());
            tCExecution.setEnvironmentData(tCExecution.getEnvironment());
        }

        /**
         * Load Environment object from invariant table.
         */
        LOG.debug("Loading Environment Information. " + tCExecution.getEnvironmentData());
        try {
            tCExecution.setEnvironmentDataObj(invariantService.convert(invariantService.readByKey("ENVIRONMENT", tCExecution.getEnvironmentData())));
        } catch (CerberusException ex) {
            if (tCExecution.isManualURL()) {
                MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_ENVIRONMENT_DOESNOTEXIST_MAN);
                mes.setDescription(mes.getDescription().replace("%ENV%", tCExecution.getEnvironmentData()));
                LOG.debug(mes.getDescription());
                throw new CerberusException(mes);
            } else {
                MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_ENVIRONMENT_DOESNOTEXIST);
                mes.setDescription(mes.getDescription().replace("%ENV%", tCExecution.getEnvironmentData()));
                LOG.debug(mes.getDescription());
                throw new CerberusException(mes);
            }
        }
        LOG.debug("Environment Information Loaded");

        /**
         * Load Country/Environment information and set them to the
         * TestCaseExecution object. Environment considered here is the data
         * environment.
         */
        LOG.debug("Loading Country/Environment Information. " + tCExecution.getCountry() + " - " + tCExecution.getEnvironmentData());
        CountryEnvParam countEnvParam;
        try {
            countEnvParam = this.countryEnvParamService.convert(this.countryEnvParamService.readByKey(tCExecution.getApplicationObj().getSystem(), tCExecution.getCountry(), tCExecution.getEnvironmentData()));
            tCExecution.setCountryEnvParam(countEnvParam);
            /**
             * Copy the Build/Revision of the environment to the Execution. This
             * is done to keep track of all execution done on a specific version
             * of system
             */
            tCExecution.setBuild(countEnvParam.getBuild());
            tCExecution.setRevision(countEnvParam.getRevision());
        } catch (CerberusException ex) {
            MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_COUNTRYENV_NOT_FOUND);
            mes.setDescription(mes.getDescription().replace("%SYSTEM%", tCExecution.getApplicationObj().getSystem()));
            mes.setDescription(mes.getDescription().replace("%COUNTRY%", tCExecution.getCountry()));
            mes.setDescription(mes.getDescription().replace("%ENV%", tCExecution.getEnvironmentData()));
            LOG.debug(mes.getDescription());
            throw new CerberusException(mes);
        }
        LOG.debug("Country/Environment Information Loaded. " + tCExecution.getCountry() + " - " + tCExecution.getEnvironmentData());

        // If Robot is feeded, we check it exist. If it exist, we overwrite the associated parameters.
        Robot robObj = null;
        RobotExecutor robExeObj = null;
        String robotHost = "";
        String robotPort = "";
        String browser = tCExecution.getBrowser();
        String robotDecli = "";
        String version = "";
        String platform = "";
        if (!StringUtil.isNullOrEmpty(tCExecution.getRobot())) {
            robObj = robotService.readByKey(tCExecution.getRobot());

            if (robObj == null) {
                throw new CerberusException(new MessageGeneral(MessageGeneralEnum.EXECUTION_FA_ROBOTNOTEXIST)
                        .resolveDescription("ROBOT", tCExecution.getRobot()));
            }

            // If Robot parameter is defined and we can find the robot, we overwrite the corresponding parameters.
            browser = ParameterParserUtil.parseStringParam(robObj.getBrowser(), browser);
            robotDecli = ParameterParserUtil.parseStringParam(robObj.getRobotDecli(), "");
            if (StringUtil.isNullOrEmpty(robotDecli)) {
                robotDecli = robObj.getRobot();
            }
            version = ParameterParserUtil.parseStringParam(robObj.getVersion(), version);
            platform = ParameterParserUtil.parseStringParam(robObj.getPlatform(), platform);
            tCExecution.setUserAgent(robObj.getUserAgent());
            tCExecution.setScreenSize(robObj.getScreenSize());
            tCExecution.setBrowser(browser);
            tCExecution.setRobotDecli(robotDecli);
            tCExecution.setVersion(version);
            tCExecution.setPlatform(platform);
            tCExecution.setRobotObj(robObj);

            // We cannot execute a testcase on a desactivated Robot.
            if ("N".equalsIgnoreCase(robObj.getActive())) {
                LOG.debug("Robot " + tCExecution.getRobot() + " is not active.");
                throw new CerberusException(new MessageGeneral(MessageGeneralEnum.EXECUTION_FA_ROBOTNOTACTIVE)
                        .resolveDescription("ROBOT", tCExecution.getRobot()));
            }

            // If executor is not set, we get the best one from the list.
            if (StringUtil.isNullOrEmpty(tCExecution.getRobotExecutor())) {
                LOG.debug("Getting the best Executor on Robot : " + tCExecution.getRobot());
                robExeObj = robotExecutorService.readBestByKey(tCExecution.getRobot());
                if (robExeObj != null) {
                    tCExecution.setRobotExecutor(robExeObj.getExecutor());
                    tCExecution.setRobotExecutorObj(robExeObj);
                    robotExecutorService.updateLastExe(robExeObj.getRobot(), robExeObj.getExecutor());
                } else {
                    throw new CerberusException(new MessageGeneral(MessageGeneralEnum.EXECUTION_FA_ROBOTBESTEXECUTORNOTEXIST)
                            .resolveDescription("ROBOT", tCExecution.getRobot())
                            .resolveDescription("EXECUTOR", tCExecution.getRobotExecutor()));
                }
                LOG.debug(" Executor retreived : " + robExeObj.getExecutor());
            } else {
                LOG.debug(" Getting Requested Robot / Executor : " + tCExecution.getRobot() + " / " + tCExecution.getRobotExecutor());
                robExeObj = robotExecutorService.convert(robotExecutorService.readByKey(tCExecution.getRobot(), tCExecution.getRobotExecutor()));
                tCExecution.setRobotExecutorObj(robExeObj);
                if (robExeObj == null) {
                    throw new CerberusException(new MessageGeneral(MessageGeneralEnum.EXECUTION_FA_ROBOTEXECUTORNOTEXIST)
                            .resolveDescription("ROBOT", tCExecution.getRobot())
                            .resolveDescription("EXECUTOR", tCExecution.getRobotExecutor()));
                } else {
                    // We cannot execute a testcase on a desactivated Robot.
                    if ("N".equalsIgnoreCase(robExeObj.getActive())) {
                        LOG.debug("Robot Executor " + tCExecution.getRobot() + " / " + tCExecution.getRobotExecutor() + " is not active.");
                        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.EXECUTION_FA_ROBOTEXECUTORNOTACTIVE)
                                .resolveDescription("ROBOT", tCExecution.getRobot())
                                .resolveDescription("EXECUTOR", tCExecution.getRobotExecutor()));
                    }

                }
            }

            robotHost = ParameterParserUtil.parseStringParam(robExeObj.getHost(), tCExecution.getRobotHost());
            robotPort = ParameterParserUtil.parseStringParam(String.valueOf(robExeObj.getPort()), tCExecution.getRobotPort());
            tCExecution.setRobotHost(robotHost);
            tCExecution.setRobotPort(robotPort);
            tCExecution.setSeleniumIP(robotHost);
            tCExecution.setSeleniumPort(robotPort);
            tCExecution.setSeleniumIPUser(robExeObj.getHostUser());
            tCExecution.setSeleniumIPPassword(robExeObj.getHostPassword());

        } else {
            tCExecution.setRobotDecli(browser);
        }

        /**
         * If Timeout is defined at the execution level, set action wait default
         * to this value, else Get the cerberus_action_wait_default parameter.
         * This parameter will be used by tha wait action if no timeout/event is
         * defined.
         */
        try {
            if (!tCExecution.getTimeout().isEmpty()) {
                tCExecution.setCerberus_action_wait_default(Integer.valueOf(tCExecution.getTimeout()));
            } else {
                tCExecution.setCerberus_action_wait_default(parameterService.getParameterIntegerByKey("cerberus_action_wait_default", tCExecution.getApplicationObj().getSystem(), 90000));
            }
        } catch (NumberFormatException ex) {
            LOG.warn("Parameter cerberus_action_wait_default must be an integer, default value set to 90000 milliseconds. " + ex.toString());
            tCExecution.setCerberus_action_wait_default(90000);
        }

        /**
         * Check if test can be executed TODO : Replace Message with try/catch
         * cerberus exception
         */
        tCExecution.setResultMessage(new MessageGeneral(MessageGeneralEnum.EXECUTION_PE_VALIDATIONSTARTING));
        LOG.debug("Performing the Checks before starting the execution");
        MessageGeneral canExecuteTestCase = this.executionCheckService.checkTestCaseExecution(tCExecution);
        tCExecution.setResultMessage(canExecuteTestCase);
        /**
         * We stop if the result is not OK
         */
        if (!(tCExecution.getResultMessage().equals(new MessageGeneral(MessageGeneralEnum.EXECUTION_PE_CHECKINGPARAMETERS)))) {
            return tCExecution;
        }
        LOG.debug("Checks performed -- > OK to continue.");

        /**
         * Changing Automatic execution flag depending on test case information.
         */
        if (tCExecution.getManualExecution().equals(TestCaseExecution.MANUAL_A)) {
            if (tCExecution.getTestCaseObj().getType().equals(TestCase.TESTCASE_TYPE_AUTOMATED)
                    || tCExecution.getTestCaseObj().getType().equals(TestCase.TESTCASE_TYPE_PRIVATE)) {
                tCExecution.setManualExecution(TestCaseExecution.MANUAL_N);

            } else {
                tCExecution.setManualExecution(TestCaseExecution.MANUAL_Y);
            }
        }

        /**
         * For GUI application, check if Browser is supported.
         */
        if (!tCExecution.getManualExecution().equals("Y") && tCExecution.getAppTypeEngine().equalsIgnoreCase(Application.TYPE_GUI)) {
            try {
                myInvariant = invariantService.convert(invariantService.readByKey("BROWSER", tCExecution.getBrowser()));
            } catch (CerberusException ex) {
                MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_BROWSER_NOT_SUPPORTED);
                mes.setDescription(mes.getDescription().replace("%BROWSER%", tCExecution.getBrowser()));
                LOG.debug(mes.getDescription());
                throw new CerberusException(mes);
            }
        }

        /**
         * Register RunID inside database.
         */
        tCExecution.setResultMessage(new MessageGeneral(MessageGeneralEnum.EXECUTION_PE_CREATINGRUNID));
        LOG.debug("Registering Execution ID on database");
        long runID = 0;
        try {
            runID = this.testCaseExecutionService.registerRunID(tCExecution);

            if (runID != 0) {
                tCExecution.setId(runID);
                executionUUIDObject.setExecutionUUID(tCExecution.getExecutionUUID(), tCExecution);
                // Update Queue Execution here if QueueID =! 0.
                if (tCExecution.getQueueID() != 0) {
                    inQueueService.updateToExecuting(tCExecution.getQueueID(), "", runID);
                }

            } else {
                MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_COULDNOTCREATE_RUNID);
                tCExecution.setResultMessage(mes);
                LOG.fatal("Could not create RunID, or cannot retreive the generated Key");
                throw new CerberusException(mes);
            }

        } catch (CerberusException ex) {
            tCExecution.setResultMessage(new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_COULDNOTCREATE_RUNID));
            LOG.warn(ex.getMessageError().getDescription(), ex);
            throw new CerberusException(ex.getMessageError(), ex);
        }

        LOG.debug("Execution ID registered on database : " + tCExecution.getId());

        /**
         * Stop the browser if executionID is equal to zero (to prevent database
         * instabilities)
         */
        if (tCExecution.getManualExecution().equals(TestCaseExecution.MANUAL_Y)) {
            // Set execution executor from testcase executor (only for manual execution).
            tCExecution.setExecutor(tCExecution.getTestCaseObj().getExecutor());
//            try {
//                if (tCExecution.getId() == 0) {
//                    LOG.debug("Starting to Stop the Selenium Server.");
//                    this.serverService.stopServer(tCExecution);
//                    LOG.debug("Selenium Server stopped.");
//                    this.serverService.stopRemoteProxy(tCExecution);
//
//                }
//            } catch (Exception ex) {
//                LOG.warn(ex.toString(), ex);
//            }
        }

        /**
         * Stop the Cerberus Executor Proxy
         */
//        this.serverService.stopRemoteProxy(tCExecution);
        /**
         * Feature Flipping. Should be removed when websocket push is fully
         * working
         */
        tCExecution.setCerberus_featureflipping_activatewebsocketpush(parameterService.getParameterBooleanByKey("cerberus_featureflipping_activatewebsocketpush", "", false));
        tCExecution.setCerberus_featureflipping_websocketpushperiod(parameterService.getParameterLongByKey("cerberus_featureflipping_websocketpushperiod", "", 5000));

        return tCExecution;
    }

}
