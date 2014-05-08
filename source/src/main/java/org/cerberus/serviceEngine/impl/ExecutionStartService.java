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
package org.cerberus.serviceEngine.impl;

import java.util.Date;
import java.util.logging.Logger;
import org.apache.log4j.Level;
import org.cerberus.entity.CountryEnvParam;
import org.cerberus.entity.CountryEnvironmentApplication;
import org.cerberus.entity.Invariant;
import org.cerberus.entity.MessageGeneral;
import org.cerberus.entity.MessageGeneralEnum;
import org.cerberus.entity.TCase;
import org.cerberus.entity.TestCaseExecution;
import org.cerberus.exception.CerberusException;
import org.cerberus.factory.IFactoryCountryEnvironmentApplication;
import org.cerberus.factory.IFactoryTestCaseExecutionData;
import org.cerberus.factory.IFactoryTestCaseExecutionSysVer;
import org.cerberus.factory.IFactoryTestCaseStepActionControlExecution;
import org.cerberus.factory.IFactoryTestCaseStepActionExecution;
import org.cerberus.factory.IFactoryTestCaseStepExecution;
import org.cerberus.log.MyLogger;
import org.cerberus.service.IApplicationService;
import org.cerberus.service.ICountryEnvLinkService;
import org.cerberus.service.ICountryEnvParamService;
import org.cerberus.service.ICountryEnvironmentApplicationService;
import org.cerberus.service.IInvariantService;
import org.cerberus.service.ILoadTestCaseService;
import org.cerberus.service.ITestCaseCountryPropertiesService;
import org.cerberus.service.ITestCaseExecutionDataService;
import org.cerberus.service.ITestCaseExecutionService;
import org.cerberus.service.ITestCaseExecutionSysVerService;
import org.cerberus.service.ITestCaseExecutionWWWService;
import org.cerberus.service.ITestCaseService;
import org.cerberus.service.ITestCaseStepActionControlExecutionService;
import org.cerberus.service.ITestCaseStepActionExecutionService;
import org.cerberus.service.ITestCaseStepExecutionService;
import org.cerberus.serviceEngine.IActionService;
import org.cerberus.serviceEngine.IControlService;
import org.cerberus.serviceEngine.IExecutionCheckService;
import org.cerberus.serviceEngine.IExecutionStartService;
import org.cerberus.serviceEngine.IPropertyService;
import org.cerberus.serviceEngine.ISeleniumService;
import org.cerberus.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 */
public class ExecutionStartService implements IExecutionStartService{

    @Autowired
    private IExecutionCheckService executionCheckService;
    @Autowired
    private ISeleniumService seleniumService;
    @Autowired
    private IActionService actionService;
    @Autowired
    private IPropertyService propertyService;
    @Autowired
    private IControlService controlService;
    @Autowired
    private ITestCaseService testCaseService;
    @Autowired
    private ITestCaseStepExecutionService testCaseStepExecutionService;
    @Autowired
    private ITestCaseStepActionExecutionService testCaseStepActionExecutionService;
    @Autowired
    private ITestCaseStepActionControlExecutionService testCaseStepActionControlExecutionService;
    @Autowired
    private ITestCaseExecutionService testCaseExecutionService;
    @Autowired
    private ITestCaseExecutionSysVerService testCaseExecutionSysVerService;
    @Autowired
    private ICountryEnvLinkService countryEnvLinkService;
    @Autowired
    private ITestCaseExecutionWWWService testCaseExecutionWWWService;
    @Autowired
    private ITestCaseCountryPropertiesService testCaseCountryPropertiesService;
    @Autowired
    private ITestCaseExecutionDataService testCaseExecutionDataService;
    @Autowired
    private ICountryEnvParamService countryEnvParamService;
    @Autowired
    private ICountryEnvironmentApplicationService countryEnvironmentApplicationService;
    @Autowired
    private IApplicationService applicationService;
    @Autowired
    private ILoadTestCaseService loadTestCaseService;
    @Autowired
    private IFactoryTestCaseStepExecution factoryTestCaseStepExecution;
    @Autowired
    private IFactoryTestCaseStepActionExecution factoryTestCaseStepActionExecution;
    @Autowired
    private IFactoryTestCaseStepActionControlExecution factoryTestCaseStepActionControlExecution;
    @Autowired
    private IFactoryTestCaseExecutionData factoryTestCaseExecutionData;
    @Autowired
    private IFactoryTestCaseExecutionSysVer factoryTestCaseExecutionSysVer;
    @Autowired
    private IFactoryCountryEnvironmentApplication factorycountryEnvironmentApplication;
    @Autowired
    private IInvariantService invariantService;
    
    
    @Override
    public TestCaseExecution startExecution(TestCaseExecution tCExecution) {
        /**
         * Start timestamp.
         */
        long executionStart = new Date().getTime();
        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Initializing Start Timestamp : " + executionStart);
        tCExecution.setStart(executionStart);

        /**
         * Checking the parameters.
         */
        tCExecution.setResultMessage(new MessageGeneral(MessageGeneralEnum.EXECUTION_PE_CHECKINGPARAMETERS));
        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Checking the parameters.");
        Invariant myInvariant;
        try {
            myInvariant = this.invariantService.findInvariantByIdValue("OUTPUTFORMAT", tCExecution.getOutputFormat());
        } catch (CerberusException ex) {
            MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_OUTPUTFORMAT_INVALID);
            mes.setDescription(mes.getDescription().replaceAll("%PARAM%", tCExecution.getOutputFormat()));
            tCExecution.setResultMessage(mes);
            Logger.getLogger(RunTestCaseService.class.getName()).log(java.util.logging.Level.WARNING, mes.getDescription());
            return tCExecution;
        }
        try {
            myInvariant = this.invariantService.findInvariantByIdValue("VERBOSE", String.valueOf(tCExecution.getVerbose()));
        } catch (CerberusException ex) {
            MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_VERBOSE_INVALID);
            mes.setDescription(mes.getDescription().replaceAll("%PARAM%", String.valueOf(tCExecution.getVerbose())));
            tCExecution.setResultMessage(mes);
            Logger.getLogger(RunTestCaseService.class.getName()).log(java.util.logging.Level.WARNING, mes.getDescription());
            return tCExecution;
        }
        try {
            myInvariant = this.invariantService.findInvariantByIdValue("SCREENSHOT", String.valueOf(tCExecution.getScreenshot()));
        } catch (CerberusException ex) {
            MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_SCREENSHOT_INVALID);
            mes.setDescription(mes.getDescription().replaceAll("%PARAM%", String.valueOf(tCExecution.getScreenshot())));
            tCExecution.setResultMessage(mes);
            Logger.getLogger(RunTestCaseService.class.getName()).log(java.util.logging.Level.WARNING, mes.getDescription());
            return tCExecution;
        }

        /**
         * Load TestCase information and set TCase to the TestCaseExecution object.
         */
        tCExecution.setResultMessage(new MessageGeneral(MessageGeneralEnum.EXECUTION_PE_LOADINGDATA));
        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Loading Test Case Information. " + tCExecution.getTest() + "-" + tCExecution.getTestCase());
        // Integrate this.loadTestCaseService.loadTestCase(tCExecution); inside with Dependency.
        try {
            TCase tCase = testCaseService.findTestCaseByKey(tCExecution.getTest(), tCExecution.getTestCase());
            tCExecution.settCase(tCase);
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
            tCExecution.setResultMessage(mes);
            Logger.getLogger(RunTestCaseService.class.getName()).log(java.util.logging.Level.WARNING, mes.getDescription());
            return tCExecution;
        }
        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Test Case Information Loaded - " + tCExecution.getTest() + "-" + tCExecution.getTestCase());


        /**
         * Load Application information and Set Application to the TestCaseExecution
         * object.
         */
        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Loading Application Information");
        try {
            tCExecution.setApplication(this.applicationService.findApplicationByKey(tCExecution.gettCase().getApplication()));
        } catch (CerberusException ex) {
            MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_APPLICATION_NOT_FOUND);
            mes.setDescription(mes.getDescription().replaceAll("%APPLI%", tCExecution.gettCase().getApplication()));
            tCExecution.setResultMessage(mes);
            Logger.getLogger(RunTestCaseService.class.getName()).log(java.util.logging.Level.WARNING, mes.getDescription());
            return tCExecution;
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
            tCExecution.setResultMessage(mes);
            Logger.getLogger(RunTestCaseService.class.getName()).log(java.util.logging.Level.WARNING, mes.getDescription());
            return tCExecution;
        }
        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Country Information Loaded - " + tCExecution.getCountryObj().getDescription());


        /**
         * Checking if execution is manual or automaticaly configured. If
         * Manual, CountryEnvironmentApplication object is manually created with
         * the servlet parameters. If automatic, parameters are build from the
         * CountryEnvironmentApplication table in the database. Environmentdata
         * will always be filled with the environment. Environment will be empty
         * if execution is manual.
         *
         */
        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Checking if connectivity parameters are manual or automatic from the database. '" + tCExecution.isManualURL() + "'");
        if (tCExecution.isManualURL()) {
            MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Execution will be done with manual application connectivity setting.");
            if (StringUtil.isNullOrEmpty(tCExecution.getMyHost())) {
                MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_MANUALURL_INVALID);
                tCExecution.setResultMessage(mes);
                Logger.getLogger(RunTestCaseService.class.getName()).log(java.util.logging.Level.WARNING, mes.getDescription());
                return tCExecution;
            } else {
                CountryEnvironmentApplication cea;
                cea = this.factorycountryEnvironmentApplication.create(tCExecution.getApplication().getSystem(), tCExecution.getCountry(), tCExecution.getEnvironment(), tCExecution.getApplication().getApplication(), tCExecution.getMyHost(), tCExecution.getMyContextRoot(), tCExecution.getMyLoginRelativeURL());
                cea.setIp(tCExecution.getMyHost());
                cea.setUrl(tCExecution.getMyContextRoot());
                tCExecution.setUrl(cea.getIp() + cea.getUrl());
                cea.setUrlLogin(tCExecution.getMyLoginRelativeURL());
                tCExecution.setCountryEnvironmentApplication(cea);
                MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, " -> Execution will be done with manual application connectivity setting. IP/URL/LOGIN : " + cea.getIp() + "-" + cea.getUrl() + "-" + cea.getUrlLogin());
            }
            /**
             * If execution is manual, we force the env at empty string.
             */
            tCExecution.setEnvironment("");
        } else {
            /**
             * Automatic application configuration execution.
             */
            MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Execution will be done with automatic application connectivity setting.");
            /**
             * Load Country/Environment/Application information and set them to
             * the TestCaseExecution object
             */
            MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Loading Country/Environment/Application Information. " + tCExecution.getCountry() + "-" + tCExecution.getEnvironment() + "-" + tCExecution.getApplication().getApplication());
            CountryEnvironmentApplication cea;
            try {
                cea = this.countryEnvironmentApplicationService.findCountryEnvironmentParameterByKey(
                        tCExecution.getApplication().getSystem(), tCExecution.getCountry(), tCExecution.getEnvironment(), tCExecution.getApplication().getApplication());
                tCExecution.setCountryEnvironmentApplication(cea);
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
                tCExecution.setResultMessage(mes);
                Logger.getLogger(RunTestCaseService.class.getName()).log(java.util.logging.Level.WARNING, mes.getDescription());
                return tCExecution;
            }
            MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "-> Execution will be done with automatic application connectivity setting. IP/URL/LOGIN : " + cea.getIp() + "-" + cea.getUrl() + "-" + cea.getUrlLogin());
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
                tCExecution.setResultMessage(mes);
                Logger.getLogger(RunTestCaseService.class.getName()).log(java.util.logging.Level.WARNING, mes.getDescription());
                return tCExecution;
            } else {
                MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_ENVIRONMENT_DOESNOTEXIST);
                mes.setDescription(mes.getDescription().replaceAll("%ENV%", tCExecution.getEnvironmentData()));
                tCExecution.setResultMessage(mes);
                Logger.getLogger(RunTestCaseService.class.getName()).log(java.util.logging.Level.WARNING, mes.getDescription());
                return tCExecution;
            }
        }


        /**
         * Load Country/Environment information and set them to the TestCaseExecution
         * object. Environment considered here is the data environment.
         */
        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Loading Country/Environment Information. " + tCExecution.getCountry() + "-" + tCExecution.getEnvironmentData());
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
            tCExecution.setResultMessage(mes);
            Logger.getLogger(RunTestCaseService.class.getName()).log(java.util.logging.Level.WARNING, mes.getDescription());
            return tCExecution;
        }


        /**
         * Check if test can be executed TODO : Replace Message with try/catch
         * cerberus exception
         */
        tCExecution.setResultMessage(new MessageGeneral(MessageGeneralEnum.EXECUTION_PE_VALIDATIONSTARTING));
        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Performing the Checks before starting the execution");
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
        if (tCExecution.getApplication().getType().equalsIgnoreCase("GUI")) {

            try {
                myInvariant = this.invariantService.findInvariantByIdValue("BROWSER", tCExecution.getBrowser());
            } catch (CerberusException ex) {
                MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_BROWSER_NOT_SUPPORTED);
                mes.setDescription(mes.getDescription().replaceAll("%BROWSER%", tCExecution.getBrowser()));
                tCExecution.setResultMessage(mes);
                Logger.getLogger(RunTestCaseService.class.getName()).log(java.util.logging.Level.WARNING, mes.getDescription());
                return tCExecution;
            }

            if (tCExecution.getIp().equalsIgnoreCase("")) {
                tCExecution.setResultMessage(new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_SELENIUM_EMPTYORBADIP));
                tCExecution.getResultMessage().setDescription(tCExecution.getResultMessage().getDescription().replaceAll("%IP%", tCExecution.getIp()));
                return tCExecution;
            }
            if (tCExecution.getPort().equalsIgnoreCase("")) {
                tCExecution.setResultMessage(new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_SELENIUM_EMPTYORBADPORT));
                tCExecution.getResultMessage().setDescription(tCExecution.getResultMessage().getDescription().replaceAll("%PORT%", tCExecution.getPort()));
                return tCExecution;
            }

            MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Application is GUI. Trying to reach selenium server.");
            if (!this.seleniumService.isSeleniumServerReachable(tCExecution.getIp(), tCExecution.getPort())) {
                tCExecution.setResultMessage(new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_SELENIUM_COULDNOTCONNECT));
                tCExecution.getResultMessage().setDescription(tCExecution.getResultMessage().getDescription().replaceAll("%SSIP%", tCExecution.getIp()));
                tCExecution.getResultMessage().setDescription(tCExecution.getResultMessage().getDescription().replaceAll("%SSPORT%", tCExecution.getPort()));
                return tCExecution;
            }
        }

        /**
         * Register RunID inside database.
         */
        tCExecution.setResultMessage(new MessageGeneral(MessageGeneralEnum.EXECUTION_PE_CREATINGRUNID));
        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Registering Execution ID on database");
        long runID = this.testCaseExecutionService.registerRunID(tCExecution);
        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, tCExecution.getId() + " - RunID Registered on database.");
        if (runID <= 0) {
            tCExecution.setResultMessage(new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_COULDNOTCREATE_RUNID));
            return tCExecution;
        }
    
        return tCExecution;
    }
    
}
