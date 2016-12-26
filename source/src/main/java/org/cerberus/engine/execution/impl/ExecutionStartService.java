/*
 * Cerberus  Copyright (C) 2013  vertigo17
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

import java.util.Date;
import java.util.logging.Logger;
import org.cerberus.crud.entity.CountryEnvParam;
import org.cerberus.crud.entity.CountryEnvironmentParameters;
import org.cerberus.engine.entity.ExecutionUUID;
import org.cerberus.crud.entity.Invariant;
import org.cerberus.engine.entity.MessageGeneral;
import org.cerberus.crud.entity.Parameter;
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
import org.cerberus.engine.execution.ISeleniumServerService;
import org.cerberus.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.cerberus.crud.service.ICountryEnvironmentParametersService;
import org.cerberus.crud.factory.IFactoryCountryEnvironmentParameters;
import org.cerberus.crud.service.IParameterService;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.util.answer.AnswerItem;

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
    private ISeleniumServerService serverService;
    @Autowired
    private IParameterService parameterService;

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ExecutionStartService.class);

    @Override
    public TestCaseExecution startExecution(TestCaseExecution tCExecution) throws CerberusException {
        /**
         * Start timestamp.
         */
        long executionStart = new Date().getTime();
        LOG.debug("Initializing Start Timestamp : " + executionStart);
        tCExecution.setStart(executionStart);

        /**
         * Checking the parameters.
         */
        LOG.debug("Checking the parameters.");
        Invariant myInvariant;
        try {
            myInvariant = this.invariantService.findInvariantByIdValue("OUTPUTFORMAT", tCExecution.getOutputFormat());
        } catch (CerberusException ex) {
            MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_OUTPUTFORMAT_INVALID);
            mes.setDescription(mes.getDescription().replace("%PARAM%", tCExecution.getOutputFormat()));
            LOG.debug(mes.getDescription());
            throw new CerberusException(mes);
        }
        try {
            myInvariant = this.invariantService.findInvariantByIdValue("VERBOSE", String.valueOf(tCExecution.getVerbose()));
        } catch (CerberusException ex) {
            MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_VERBOSE_INVALID);
            mes.setDescription(mes.getDescription().replace("%PARAM%", String.valueOf(tCExecution.getVerbose())));
            LOG.debug(mes.getDescription());
            throw new CerberusException(mes);
        }
        try {
            myInvariant = this.invariantService.findInvariantByIdValue("SCREENSHOT", String.valueOf(tCExecution.getScreenshot()));
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
            TestCase tCase = testCaseService.findTestCaseByKey(tCExecution.getTest(), tCExecution.getTestCase());
            if (tCase != null) {
                tCExecution.setTestCaseObj(tCase);
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
            tCExecution.setApplicationObj(this.applicationService.convert(this.applicationService.readByKey(tCExecution.getTestCaseObj().getApplication())));
        } catch (CerberusException ex) {
            MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_APPLICATION_NOT_FOUND);
            mes.setDescription(mes.getDescription().replace("%APPLI%", tCExecution.getTestCaseObj().getApplication()));
            LOG.debug(mes.getDescription());
            throw new CerberusException(mes);
        }
        LOG.debug("Application Information Loaded - " + tCExecution.getApplicationObj().getApplication() + " - " + tCExecution.getApplicationObj().getDescription());

        /**
         * Load Country information and Set it to the TestCaseExecution object.
         */
        LOG.debug("Loading Country Information");
        try {
            tCExecution.setCountryObj(this.invariantService.findInvariantByIdValue("COUNTRY", tCExecution.getCountry()));
        } catch (CerberusException ex) {
            MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_COUNTRY_NOT_FOUND);
            mes.setDescription(mes.getDescription().replace("%COUNTRY%", tCExecution.getCountry()));
            LOG.debug(mes.getDescription());
            throw new CerberusException(mes);
        }
        LOG.debug("Country Information Loaded - " + tCExecution.getCountryObj().getValue() + " - " + tCExecution.getCountryObj().getDescription());

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
                cea = this.factorycountryEnvironmentParameters.create(tCExecution.getApplicationObj().getSystem(), tCExecution.getCountry(), tCExecution.getEnvironment(), tCExecution.getApplicationObj().getApplication(), tCExecution.getMyHost(), "", tCExecution.getMyContextRoot(), tCExecution.getMyLoginRelativeURL(), "", "", "", "");
                cea.setIp(tCExecution.getMyHost());
                cea.setUrl(tCExecution.getMyContextRoot());
                tCExecution.setUrl(cea.getIp() + cea.getUrl());
                cea.setUrlLogin(tCExecution.getMyLoginRelativeURL());
                tCExecution.setCountryEnvironmentParameters(cea);
                LOG.debug(" -> Execution will be done with manual application connectivity setting. IP/URL/LOGIN : " + cea.getIp() + "-" + cea.getUrl() + "-" + cea.getUrlLogin());
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
                    tCExecution.setCountryEnvironmentParameters(cea);
                    tCExecution.setUrl(cea.getIp() + cea.getUrl());
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
                LOG.error(mes.getDescription());
                throw new CerberusException(mes);
            }
            LOG.debug("Country/Environment/Application Information Loaded. " + tCExecution.getCountry() + " - " + tCExecution.getEnvironment() + " - " + tCExecution.getApplicationObj().getApplication());
            LOG.debug(" -> Execution will be done with automatic application connectivity setting. IP/URL/LOGIN : " + cea.getIp() + "-" + cea.getUrl() + "-" + cea.getUrlLogin());
            tCExecution.setEnvironmentData(tCExecution.getEnvironment());
        }

        /**
         * Load Environment object from invariant table.
         */
        LOG.debug("Loading Environment Information. " + tCExecution.getEnvironmentData());
        try {
            tCExecution.setEnvironmentDataObj(this.invariantService.findInvariantByIdValue("ENVIRONMENT", tCExecution.getEnvironmentData()));
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

        /**
         * If Timeout is defined at the execution level, set action wait default to this value, else
         * Get the cerberus_action_wait_default parameter. This parameter will
         * be used by tha wait action if no timeout/event is defined.
         */
        try {
            if (!tCExecution.getTimeout().isEmpty()){
                tCExecution.setCerberus_action_wait_default(Integer.valueOf(tCExecution.getTimeout()));
            } else {
            AnswerItem timeoutParameter = parameterService.readWithSystem1ByKey("", "cerberus_action_wait_default", tCExecution.getApplicationObj().getSystem());
            if (timeoutParameter != null && timeoutParameter.isCodeStringEquals(MessageEventEnum.DATA_OPERATION_OK.getCodeString())) {
                if (((Parameter) timeoutParameter.getItem()).getSystem1value().isEmpty()) {
                    tCExecution.setCerberus_action_wait_default(Integer.valueOf(((Parameter) timeoutParameter.getItem()).getValue()));
                } else {
                    tCExecution.setCerberus_action_wait_default(Integer.valueOf(((Parameter) timeoutParameter.getItem()).getSystem1value()));
                }
            } else {
                LOG.warn("Parameter cerberus_action_wait_default not set in Parameter table, default value set to 90000 milliseconds. ");
                tCExecution.setCerberus_action_wait_default(90000);
            }
            }
        } catch (NumberFormatException ex) {
            LOG.warn("Parameter cerberus_action_wait_default must be an integer, default value set to 90000 milliseconds. " + ex.toString());
            tCExecution.setCerberus_action_wait_default(90000);
        }

        /**
         * What is that for ???
         */
        tCExecution.setManualExecution("N");

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
         * For GUI application, check if Browser is supported.
         */
        if (tCExecution.getApplicationObj().getType().equalsIgnoreCase("GUI")) {
            try {
                myInvariant = this.invariantService.findInvariantByIdValue("BROWSER", tCExecution.getBrowser());
            } catch (CerberusException ex) {
                MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_BROWSER_NOT_SUPPORTED);
                mes.setDescription(mes.getDescription().replace("%BROWSER%", tCExecution.getBrowser()));
                LOG.debug(mes.getDescription());
                throw new CerberusException(mes);
            }
        }

        /**
         * Start server
         */
        if (tCExecution.getApplicationObj().getType().equalsIgnoreCase("GUI")
                || tCExecution.getApplicationObj().getType().equalsIgnoreCase("APK")
                || tCExecution.getApplicationObj().getType().equalsIgnoreCase("IPA")
                || tCExecution.getApplicationObj().getType().equalsIgnoreCase("FAT")) {

            if (tCExecution.getIp().equalsIgnoreCase("")) {
                MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_SELENIUM_EMPTYORBADIP);
                mes.setDescription(mes.getDescription().replace("%IP%", tCExecution.getIp()));
                LOG.debug(mes.getDescription());
                throw new CerberusException(mes);
            }
            if (tCExecution.getPort().equalsIgnoreCase("")) {
                MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_SELENIUM_EMPTYORBADPORT);
                mes.setDescription(mes.getDescription().replace("%PORT%", tCExecution.getPort()));
                LOG.debug(mes.getDescription());
                throw new CerberusException(mes);
            }

            /**
             * Start Selenium server
             */
            LOG.debug("Starting Server.");
            try {
                this.serverService.startServer(tCExecution);
            } catch (CerberusException ex) {
                LOG.debug(ex.getMessageError().getDescription());
                throw new CerberusException(ex.getMessageError());
            }
            LOG.debug("Server Started.");

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
            } else {
                MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_COULDNOTCREATE_RUNID);
                tCExecution.setResultMessage(mes);
                LOG.fatal("Could not create RunID, or cannot retreive the generated Key");
                throw new CerberusException(mes);
            }

        } catch (CerberusException ex) {
            tCExecution.setResultMessage(new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_COULDNOTCREATE_RUNID));
            Logger.getLogger(ExecutionStartService.class.getName()).log(java.util.logging.Level.WARNING, ex.getMessageError().getDescription());
            throw new CerberusException(ex.getMessageError());
        }

        LOG.debug("Execution ID registered on database : " + tCExecution.getId());

        /**
         * Stop the browser if executionID is equal to zero (to prevent database
         * instabilities)
         */
        try {
            if (tCExecution.getId() == 0) {
                LOG.debug("Starting to Stop the Selenium Server.");
                this.serverService.stopServer(tCExecution.getSession());
                LOG.debug("Selenium Server stopped.");
            }
        } catch (Exception ex) {
            LOG.warn(ex.toString());
        }

        /**
         * Feature Flipping. Should be removed when websocket push is fully
         * working
         */
        boolean websocketPush = true;
        try {
            AnswerItem<Parameter> featureFlippingActivateWebsocketPush = parameterService.readWithSystem1ByKey("", "cerberus_featureflipping_activatewebsocketpush", tCExecution.getApplicationObj().getSystem());
            websocketPush = StringUtil.parseBoolean(((Parameter) featureFlippingActivateWebsocketPush.getItem()).getValue());
        } catch (Exception ex) {
            LOG.warn(ex.toString());
        }
        tCExecution.setFeatureFlippingActivateWebsocketPush(websocketPush);
        /**
         * End of Feature Flipping. Should be removed when websocket push is fully
         * working
         */

        return tCExecution;
    }

}
