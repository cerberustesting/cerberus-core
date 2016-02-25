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
package org.cerberus.service.engine.impl;

import java.util.Date;
import java.util.logging.Logger;
import org.apache.log4j.Level;
import org.cerberus.crud.entity.CountryEnvParam;
import org.cerberus.crud.entity.CountryEnvironmentParameters;
import org.cerberus.crud.entity.ExecutionUUID;
import org.cerberus.crud.entity.Invariant;
import org.cerberus.crud.entity.MessageGeneral;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.crud.entity.TCase;
import org.cerberus.crud.entity.TestCaseExecution;
import org.cerberus.exception.CerberusException;
import org.cerberus.log.MyLogger;
import org.cerberus.crud.service.IApplicationService;
import org.cerberus.crud.service.ICountryEnvParamService;
import org.cerberus.crud.service.IInvariantService;
import org.cerberus.crud.service.ITestCaseExecutionService;
import org.cerberus.crud.service.ITestCaseService;
import org.cerberus.crud.service.ITestService;
import org.cerberus.service.engine.IExecutionCheckService;
import org.cerberus.service.engine.IExecutionStartService;
import org.cerberus.service.engine.ISeleniumServerService;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.cerberus.crud.service.ICountryEnvironmentParametersService;
import org.cerberus.crud.factory.IFactoryCountryEnvironmentParameters;

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

    @Override
    public TestCaseExecution startExecution(TestCaseExecution tCExecution) throws CerberusException {
        /**
         * Start timestamp.
         */
        long executionStart = new Date().getTime();
        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Initializing Start Timestamp : " + executionStart);
        tCExecution.setStart(executionStart);

        /**
         * Checking the parameters.
         */
        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Checking the parameters.");
        Invariant myInvariant;
        try {
            myInvariant = this.invariantService.findInvariantByIdValue("OUTPUTFORMAT", tCExecution.getOutputFormat());
        } catch (CerberusException ex) {
            MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_OUTPUTFORMAT_INVALID);
            mes.setDescription(mes.getDescription().replaceAll("%PARAM%", tCExecution.getOutputFormat()));
            MyLogger.log(ExecutionStartService.class.getName(), Level.DEBUG, mes.getDescription());
            throw new CerberusException(mes);
        }
        try {
            myInvariant = this.invariantService.findInvariantByIdValue("VERBOSE", String.valueOf(tCExecution.getVerbose()));
        } catch (CerberusException ex) {
            MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_VERBOSE_INVALID);
            mes.setDescription(mes.getDescription().replaceAll("%PARAM%", String.valueOf(tCExecution.getVerbose())));
            MyLogger.log(ExecutionStartService.class.getName(), Level.DEBUG, mes.getDescription());
            throw new CerberusException(mes);
        }
        try {
            myInvariant = this.invariantService.findInvariantByIdValue("SCREENSHOT", String.valueOf(tCExecution.getScreenshot()));
        } catch (CerberusException ex) {
            MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_SCREENSHOT_INVALID);
            mes.setDescription(mes.getDescription().replaceAll("%PARAM%", String.valueOf(tCExecution.getScreenshot())));
            MyLogger.log(ExecutionStartService.class.getName(), Level.DEBUG, mes.getDescription());
            throw new CerberusException(mes);
        }

        /**
         * Load TestCase information and set TCase to the TestCaseExecution
         * object.
         */
        tCExecution.setResultMessage(new MessageGeneral(MessageGeneralEnum.EXECUTION_PE_LOADINGDATA));
        MyLogger.log(ExecutionStartService.class.getName(), Level.DEBUG, "Loading Test Case Information. " + tCExecution.getTest() + "-" + tCExecution.getTestCase());
        // Integrate this.loadTestCaseService.loadTestCase(tCExecution); inside with Dependency.
        try {
            TCase tCase = testCaseService.findTestCaseByKey(tCExecution.getTest(), tCExecution.getTestCase());
            if (tCase != null) {
                tCExecution.settCase(tCase);
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
            mes.setDescription(mes.getDescription().replaceAll("%TEST%", tCExecution.getTest()));
            mes.setDescription(mes.getDescription().replaceAll("%TESTCASE%", tCExecution.getTestCase()));
            MyLogger.log(ExecutionStartService.class.getName(), Level.DEBUG, mes.getDescription());
            throw new CerberusException(mes);
        }
        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Test Case Information Loaded - " + tCExecution.getTest() + "-" + tCExecution.getTestCase());

        /**
         * Load Test information and Set TestObject to the
         * TestCaseExecution object.
         */
        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Loading Test Information");
        try {
            tCExecution.setTestObj(this.testService.convert(this.testService.readByKey(tCExecution.getTest())));
        } catch (CerberusException ex) {
            MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_TEST_NOT_FOUND);
            mes.setDescription(mes.getDescription().replaceAll("%TEST%", tCExecution.getTest()));
            MyLogger.log(ExecutionStartService.class.getName(), Level.DEBUG, mes.getDescription());
            throw new CerberusException(mes);
        }
        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Test Information Loaded - " + tCExecution.getTest());

        /**
         * Load Application information and Set Application to the
         * TestCaseExecution object.
         */
        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Loading Application Information");
        try {
            tCExecution.setApplication(this.applicationService.convert(this.applicationService.readByKey(tCExecution.gettCase().getApplication())));
        } catch (CerberusException ex) {
            MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_APPLICATION_NOT_FOUND);
            mes.setDescription(mes.getDescription().replaceAll("%APPLI%", tCExecution.gettCase().getApplication()));
            MyLogger.log(ExecutionStartService.class.getName(), Level.DEBUG, mes.getDescription());
            throw new CerberusException(mes);
        }
        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Application Information Loaded - " + tCExecution.getApplication().getDescription());

        /**
         * Load Country information and Set it to the TestCaseExecution object.
         */
        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Loading Country Information");
        try {
            tCExecution.setCountryObj(this.invariantService.findInvariantByIdValue("COUNTRY", tCExecution.getCountry()));
        } catch (CerberusException ex) {
            MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_COUNTRY_NOT_FOUND);
            mes.setDescription(mes.getDescription().replaceAll("%COUNTRY%", tCExecution.getCountry()));
            MyLogger.log(ExecutionStartService.class.getName(), Level.DEBUG, mes.getDescription());
            throw new CerberusException(mes);
        }
        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Country Information Loaded - " + tCExecution.getCountryObj().getDescription());

        /**
         * Checking if execution is manual or automaticaly configured. If
         * Manual, CountryEnvironmentParameters object is manually created with
         * the servlet parameters. If automatic, parameters are build from the
         * CountryEnvironmentParameters. table in the database. Environmentdata
         * will always be filled with the environment. Environment will be forced to MANUAL
         * if execution is manual.
         *
         */
        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Checking if connectivity parameters are manual or automatic from the database. '" + tCExecution.isManualURL() + "'");
        if (tCExecution.isManualURL()) {
            MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Execution will be done with manual application connectivity setting.");
            if (StringUtil.isNullOrEmpty(tCExecution.getMyHost())) {
                MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_MANUALURL_INVALID);
                MyLogger.log(ExecutionStartService.class.getName(), Level.DEBUG, mes.getDescription());
                throw new CerberusException(mes);
            } else {
                CountryEnvironmentParameters cea;
                cea = this.factorycountryEnvironmentParameters.create(tCExecution.getApplication().getSystem(), tCExecution.getCountry(), tCExecution.getEnvironment(), tCExecution.getApplication().getApplication(), tCExecution.getMyHost(), "", tCExecution.getMyContextRoot(), tCExecution.getMyLoginRelativeURL());
                cea.setIp(tCExecution.getMyHost());
                cea.setUrl(tCExecution.getMyContextRoot());
                tCExecution.setUrl(cea.getIp() + cea.getUrl());
                cea.setUrlLogin(tCExecution.getMyLoginRelativeURL());
                tCExecution.setCountryEnvironmentParameters(cea);
                MyLogger.log(ExecutionStartService.class.getName(), Level.DEBUG, " -> Execution will be done with manual application connectivity setting. IP/URL/LOGIN : " + cea.getIp() + "-" + cea.getUrl() + "-" + cea.getUrlLogin());
            }
            /**
             * If execution is manual, we force the env at 'MANUAL' string.
             */
            tCExecution.setEnvironment("MANUAL");
        } else {
            /**
             * Automatic application configuration execution.
             */
            MyLogger.log(ExecutionStartService.class.getName(), Level.DEBUG, "Execution will be done with automatic application connectivity setting.");
            /**
             * Load Country/Environment/Application information and set them to
             * the TestCaseExecution object
             */
            MyLogger.log(ExecutionStartService.class.getName(), Level.DEBUG, "Loading Country/Environment/Application Information. " + tCExecution.getCountry() + "-" + tCExecution.getEnvironment() + "-" + tCExecution.getApplication().getApplication());
            CountryEnvironmentParameters cea;
            try {
                cea = this.countryEnvironmentParametersService.findCountryEnvironmentParameterByKey(
                        tCExecution.getApplication().getSystem(), tCExecution.getCountry(), tCExecution.getEnvironment(), tCExecution.getApplication().getApplication());
                tCExecution.setCountryEnvironmentParameters(cea);
                tCExecution.setUrl(cea.getIp() + cea.getUrl());
                /**
                 * Forcing the IP URL and Login config from DevIP, DevURL and
                 * DevLogin parameter only if DevURL is defined.
                 */
            } catch (CerberusException ex) {
                MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_COUNTRYENVAPP_NOT_FOUND);
                mes.setDescription(mes.getDescription().replaceAll("%COUNTRY%", tCExecution.getCountry()));
                mes.setDescription(mes.getDescription().replaceAll("%ENV%", tCExecution.getEnvironment()));
                mes.setDescription(mes.getDescription().replaceAll("%APPLI%", tCExecution.gettCase().getApplication()));
                MyLogger.log(ExecutionStartService.class.getName(), Level.DEBUG, mes.getDescription());
                throw new CerberusException(mes);
            }
            MyLogger.log(ExecutionStartService.class.getName(), Level.DEBUG, "-> Execution will be done with automatic application connectivity setting. IP/URL/LOGIN : " + cea.getIp() + "-" + cea.getUrl() + "-" + cea.getUrlLogin());
            tCExecution.setEnvironmentData(tCExecution.getEnvironment());
        }

        /**
         * Load Environment object from invariant table.
         */
        try {
            tCExecution.setEnvironmentDataObj(this.invariantService.findInvariantByIdValue("ENVIRONMENT", tCExecution.getEnvironmentData()));
        } catch (CerberusException ex) {
            if (tCExecution.isManualURL()) {
                MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_ENVIRONMENT_DOESNOTEXIST_MAN);
                mes.setDescription(mes.getDescription().replaceAll("%ENV%", tCExecution.getEnvironmentData()));
                MyLogger.log(ExecutionStartService.class.getName(), Level.DEBUG, mes.getDescription());
                throw new CerberusException(mes);
            } else {
                MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_ENVIRONMENT_DOESNOTEXIST);
                mes.setDescription(mes.getDescription().replaceAll("%ENV%", tCExecution.getEnvironmentData()));
                MyLogger.log(ExecutionStartService.class.getName(), Level.DEBUG, mes.getDescription());
                throw new CerberusException(mes);
            }
        }

        /**
         * Load Country/Environment information and set them to the
         * TestCaseExecution object. Environment considered here is the data
         * environment.
         */
        MyLogger.log(ExecutionStartService.class.getName(), Level.DEBUG, "Loading Country/Environment Information. " + tCExecution.getCountry() + "-" + tCExecution.getEnvironmentData());
        CountryEnvParam countEnvParam;
        try {
            countEnvParam = this.countryEnvParamService.findCountryEnvParamByKey(tCExecution.getApplication().getSystem(), tCExecution.getCountry(), tCExecution.getEnvironmentData());
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
            mes.setDescription(mes.getDescription().replaceAll("%SYSTEM%", tCExecution.getApplication().getSystem()));
            mes.setDescription(mes.getDescription().replaceAll("%COUNTRY%", tCExecution.getCountry()));
            mes.setDescription(mes.getDescription().replaceAll("%ENV%", tCExecution.getEnvironmentData()));
            MyLogger.log(ExecutionStartService.class.getName(), Level.DEBUG, mes.getDescription());
            throw new CerberusException(mes);
        }

        tCExecution.setManualExecution("N");
        /**
         * Check if test can be executed TODO : Replace Message with try/catch
         * cerberus exception
         */
        tCExecution.setResultMessage(new MessageGeneral(MessageGeneralEnum.EXECUTION_PE_VALIDATIONSTARTING));
        MyLogger.log(ExecutionStartService.class.getName(), Level.DEBUG, "Performing the Checks before starting the execution");
        MessageGeneral canExecuteTestCase = this.executionCheckService.checkTestCaseExecution(tCExecution);
        tCExecution.setResultMessage(canExecuteTestCase);
        /**
         * We stop if the result is not OK
         */
        if (!(tCExecution.getResultMessage().equals(new MessageGeneral(MessageGeneralEnum.EXECUTION_PE_CHECKINGPARAMETERS)))) {
            return tCExecution;
        }

        /**
         * Check if Browser is supported and if selenium server is reachable.
         */
        if (tCExecution.getApplication().getType().equalsIgnoreCase("GUI")
                || tCExecution.getApplication().getType().equalsIgnoreCase("APK")
        || tCExecution.getApplication().getType().equalsIgnoreCase("IPA")){

            try {
                myInvariant = this.invariantService.findInvariantByIdValue("BROWSER", tCExecution.getBrowser());
            } catch (CerberusException ex) {
                MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_BROWSER_NOT_SUPPORTED);
                mes.setDescription(mes.getDescription().replaceAll("%BROWSER%", tCExecution.getBrowser()));
                MyLogger.log(ExecutionStartService.class.getName(), Level.DEBUG, mes.getDescription());
                throw new CerberusException(mes);
            }

            if (tCExecution.getIp().equalsIgnoreCase("")) {
                MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_SELENIUM_EMPTYORBADIP);
                mes.setDescription(mes.getDescription().replaceAll("%IP%", tCExecution.getIp()));
                MyLogger.log(ExecutionStartService.class.getName(), Level.DEBUG, mes.getDescription());
                throw new CerberusException(mes);
            }
            if (tCExecution.getPort().equalsIgnoreCase("")) {
                MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_SELENIUM_EMPTYORBADPORT);
                mes.setDescription(mes.getDescription().replaceAll("%IP%", tCExecution.getIp()));
                MyLogger.log(ExecutionStartService.class.getName(), Level.DEBUG, mes.getDescription());
                throw new CerberusException(mes);
            }

//            if (!tCExecution.getManualExecution().equals("Y")){
            MyLogger.log(ExecutionStartService.class.getName(), Level.DEBUG, "Application is GUI. Trying to reach selenium server.");

            /**
             * Start Selenium server
             */
            String url = ParameterParserUtil.parseStringParam(tCExecution.getCountryEnvironmentParameters().getIp() + tCExecution.getCountryEnvironmentParameters().getUrl(), "");
            String login = ParameterParserUtil.parseStringParam(tCExecution.getCountryEnvironmentParameters().getUrlLogin(), "");
            MyLogger.log(ExecutionStartService.class.getName(), Level.DEBUG, "Starting Selenium Server.");

            try {
                this.serverService.startServer(tCExecution);
            } catch (CerberusException ex) {
                MyLogger.log(ExecutionStartService.class.getName(), Level.DEBUG, ex.getMessageError().getDescription());
                throw new CerberusException(ex.getMessageError());
            }
        }

//        }
        /**
         * Register RunID inside database.
         */
        tCExecution.setResultMessage(new MessageGeneral(MessageGeneralEnum.EXECUTION_PE_CREATINGRUNID));
        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Registering Execution ID on database");
        long runID = 0;
        try {
            runID = this.testCaseExecutionService.registerRunID(tCExecution);

            if (runID != 0) {
                tCExecution.setId(runID);
                executionUUIDObject.setExecutionUUID(tCExecution.getExecutionUUID(), tCExecution);
            } else {
                MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_COULDNOTCREATE_RUNID);
                tCExecution.setResultMessage(mes);
                MyLogger.log(ExecutionStartService.class.getName(), Level.FATAL, "Could not create RunID, or cannot retreive the generated Key");
                throw new CerberusException(mes);
            }

        } catch (CerberusException ex) {
            tCExecution.setResultMessage(new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_COULDNOTCREATE_RUNID));
            Logger.getLogger(ExecutionStartService.class.getName()).log(java.util.logging.Level.WARNING, ex.getMessageError().getDescription());
            throw new CerberusException(ex.getMessageError());
        }

        MyLogger.log(ExecutionStartService.class.getName(), Level.DEBUG, tCExecution.getId() + " - RunID Registered on database.");

        /**
         * Stop the browser if executionID is equal to zero (to prevent database
         * instabilities)
         */
        try {
            if (tCExecution.getId() == 0) {
                this.serverService.stopServer(tCExecution.getSession());
            }
        } catch (Exception ex) {
            MyLogger.log(ExecutionStartService.class.getName(), Level.WARN, ex.toString());
        }

        return tCExecution;
    }

}
