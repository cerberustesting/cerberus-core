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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import org.apache.log4j.Level;
import org.cerberus.entity.CountryEnvLink;
import org.cerberus.entity.CountryEnvParam;
import org.cerberus.entity.ExecutionSOAPResponse;
import org.cerberus.entity.ExecutionUUID;
import org.cerberus.entity.MessageEvent;
import org.cerberus.entity.MessageEventEnum;
import org.cerberus.entity.MessageGeneral;
import org.cerberus.entity.MessageGeneralEnum;
import org.cerberus.entity.TCase;
import org.cerberus.entity.TestCaseCountryProperties;
import org.cerberus.entity.TestCaseExecution;
import org.cerberus.entity.TestCaseExecutionData;
import org.cerberus.entity.TestCaseExecutionSysVer;
import org.cerberus.entity.TestCaseStep;
import org.cerberus.entity.TestCaseStepAction;
import org.cerberus.entity.TestCaseStepActionControl;
import org.cerberus.entity.TestCaseStepActionControlExecution;
import org.cerberus.entity.TestCaseStepActionExecution;
import org.cerberus.entity.TestCaseStepExecution;
import org.cerberus.exception.CerberusException;
import org.cerberus.factory.IFactoryTestCaseExecutionData;
import org.cerberus.factory.IFactoryTestCaseExecutionSysVer;
import org.cerberus.factory.IFactoryTestCaseStepActionControlExecution;
import org.cerberus.factory.IFactoryTestCaseStepActionExecution;
import org.cerberus.factory.IFactoryTestCaseStepExecution;
import org.cerberus.log.MyLogger;
import org.cerberus.service.ICountryEnvLinkService;
import org.cerberus.service.ICountryEnvParamService;
import org.cerberus.service.ILoadTestCaseService;
import org.cerberus.service.IParameterService;
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
import org.cerberus.serviceEngine.IExecutionRunService;
import org.cerberus.serviceEngine.IPropertyService;
import org.cerberus.serviceEngine.IRecorderService;
import org.cerberus.serviceEngine.ISeleniumService;
import org.cerberus.util.StringUtil;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.UnreachableBrowserException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 */
@Service
public class ExecutionRunService implements IExecutionRunService {

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
    private ExecutionSOAPResponse eSResponse;
    @Autowired
    private ExecutionUUID executionUUID;
    @Autowired
    private IRecorderService recorderService;

    @Override
    public TestCaseExecution executeTestCase(TestCaseExecution tCExecution) throws CerberusException {
        long runID = tCExecution.getId();
        /**
         * Feeding Build Rev of main Application system to
         * testcaseexecutionsysver table. Only if execution is not manual.
         */
        if (!(tCExecution.isManualURL())) {
            /**
             * Insert SystemVersion in Database
             */
            TestCaseExecutionSysVer myExeSysVer = null;
            try {
                myExeSysVer = factoryTestCaseExecutionSysVer.create(runID, tCExecution.getApplication().getSystem(), tCExecution.getBuild(), tCExecution.getRevision());
                testCaseExecutionSysVerService.insertTestCaseExecutionSysVer(myExeSysVer);
            } catch (CerberusException ex) {
                MyLogger.log(RunTestCaseService.class.getName(), Level.INFO, ex.getMessage());
            }

            /**
             * For all Linked environment, we also keep track on the build/rev
             * information inside testcaseexecutionsysver table.
             */
            try {
                List<CountryEnvLink> ceLink = null;
                ceLink = countryEnvLinkService.findCountryEnvLinkByCriteria(tCExecution.getApplication().getSystem(), tCExecution.getCountry(), tCExecution.getEnvironment());
                MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, tCExecution.getId() + " - Linked environment found.");
                for (CountryEnvLink myCeLink : ceLink) {
                    MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, tCExecution.getId() + " - Linked environment found : " + myCeLink.getSystemLink() + myCeLink.getCountryLink() + myCeLink.getEnvironmentLink());

                    CountryEnvParam mycountEnvParam;
                    try {
                        mycountEnvParam = this.countryEnvParamService.findCountryEnvParamByKey(myCeLink.getSystemLink(), myCeLink.getCountryLink(), myCeLink.getEnvironmentLink());
                        myExeSysVer = factoryTestCaseExecutionSysVer.create(runID, myCeLink.getSystemLink(), mycountEnvParam.getBuild(), mycountEnvParam.getRevision());
                        testCaseExecutionSysVerService.insertTestCaseExecutionSysVer(myExeSysVer);
                    } catch (CerberusException ex) {
                        // Referencial Integrity link between countryEnvLink and CountryEnvParam table should secure that exception to never happen.
                        MyLogger.log(RunTestCaseService.class.getName(), Level.FATAL, ex.getMessage());
                        throw new CerberusException(ex.getMessageError());
                    }
                }
            } catch (CerberusException ex) {
                MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, tCExecution.getId() + " - No Linked environment found.");
            }
        }

        /**
         * Get used SeleniumCapabilities (empty if application is not GUI)
         */
        if (tCExecution.getApplication().getType().equalsIgnoreCase("GUI")) {
            try {
                Capabilities caps = this.seleniumService.getUsedCapabilities(tCExecution.getSelenium());
                tCExecution.setBrowserFullVersion(caps.getBrowserName() + " " + caps.getVersion() + " " + caps.getPlatform().toString());
                tCExecution.setVersion(caps.getVersion());
                tCExecution.setPlatform(caps.getPlatform().toString());
            } catch (Exception ex) {
                MyLogger.log(RunTestCaseService.class.getName(), Level.ERROR, "exception on selenium getting Used Capabilities :" + ex.toString());
            }
        } else {
            // If Selenium is not needed, the selenium and browser info is set to empty.
            tCExecution.setSeleniumIP("");
            tCExecution.setSeleniumPort("");
            tCExecution.setBrowser("");
            tCExecution.setVersion("");
            tCExecution.setPlatform("");
        }

        /**
         * Load PreTestCase information and set PreTCase to the
         * TestCaseExecution object
         */
        tCExecution.setResultMessage(new MessageGeneral(MessageGeneralEnum.EXECUTION_PE_LOADINGDETAILEDDATA));
        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, runID + " - Loading Pre testcases.");
        List<TCase> preTests = testCaseService.findTestCaseActiveByCriteria("Pre Testing", tCExecution.gettCase().getApplication(), tCExecution.getCountry());
        tCExecution.setPreTCase(preTests);
        if (!(preTests == null)) {
            MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, runID + " - Loaded PreTest List. " + tCExecution.getPreTCase().size() + " found.");
        }

        /**
         * Load Main TestCase with Step dependencies (Actions/Control)
         */
        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, runID + " - Loading all Steps information of main testcase.");
        List<TestCaseStep> testCaseStepList;
        testCaseStepList = this.loadTestCaseService.loadTestCaseStep(tCExecution.gettCase());
        tCExecution.gettCase().setTestCaseStep(testCaseStepList);
        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, runID + " - Loaded all Steps information of main testcase. " + tCExecution.gettCase().getTestCaseStep().size() + " Step(s) found.");

        /**
         * Load Pre TestCase with Step dependencies (Actions/Control)
         */
        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, runID + " - Loading all Steps information of all pre testcase.");
        //TODO Pretest this.loadTestCaseService.loadTestCaseStep(tCExecution.getPreTCase());
        List<TestCaseStep> preTestCaseStepList = new ArrayList<TestCaseStep>();
        List<TCase> preTestCase = new ArrayList<TCase>();
        for (TCase myTCase : tCExecution.getPreTCase()) {
            myTCase.setTestCaseStep(this.loadTestCaseService.loadTestCaseStep(myTCase));
            preTestCaseStepList.addAll(myTCase.getTestCaseStep());
            preTestCase.add(myTCase);
            MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, runID + " - Pre testcase : " + myTCase.getTest() + "-" + myTCase.getTestCase() + " With " + myTCase.getTestCaseStep().size() + " Step(s) found.");
        }
        tCExecution.setPreTCase(preTestCase);

        /**
         * Start Execution of the steps/Actions/controls Iterate Steps.
         * mainExecutionTestCaseStepList will contain the list of steps to
         * execute for both pretest and test. This is where we schedule the
         * execution of the steps using mainExecutionTestCaseStepList object.
         */
        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, runID + " - Starting the execution with step iteration.");
        List<TestCaseStep> mainExecutionTestCaseStepList;
        mainExecutionTestCaseStepList = new ArrayList<TestCaseStep>();
        mainExecutionTestCaseStepList.addAll(preTestCaseStepList);
        mainExecutionTestCaseStepList.addAll(testCaseStepList);

        /**
         * Initialise the global TestCaseExecution Data List.
         */
        // 
        tCExecution.setResultMessage(new MessageGeneral(MessageGeneralEnum.EXECUTION_PE_TESTEXECUTING));
        try {
            testCaseExecutionService.updateTCExecution(tCExecution);
        } catch (CerberusException ex) {
            Logger.getLogger(RunTestCaseService.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        List<TestCaseExecutionData> myExecutionDataList = new ArrayList<TestCaseExecutionData>();
        tCExecution.setTestCaseExecutionDataList(myExecutionDataList);

        for (TestCaseStep testCaseStep : mainExecutionTestCaseStepList) {

            /**
             * Start Execution of TestCaseStep
             */
            long startStep = new Date().getTime();

            /**
             * Create and Register TestCaseStepExecution
             */
            TestCaseStepExecution testCaseStepExecution = factoryTestCaseStepExecution.create(
                    runID, testCaseStep.getTest(), testCaseStep.getTestCase(),
                    testCaseStep.getStep(), null,
                    startStep, 0, startStep, 0, 0, null, new MessageEvent(MessageEventEnum.STEP_PENDING), testCaseStep, tCExecution);
            testCaseStepExecutionService.insertTestCaseStepExecution(testCaseStepExecution);
            testCaseStepExecution.setExecutionResultMessage(new MessageGeneral(MessageGeneralEnum.EXECUTION_PE_TESTSTARTED));

            /**
             * Execute Step
             */
            MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, runID + " - Executing step : " + testCaseStepExecution.getTest() + "-" + testCaseStepExecution.getTestCase() + "-" + testCaseStepExecution.getStep());
            testCaseStepExecution = this.executeStep(testCaseStepExecution);

            /**
             * Adding the data to the execution datalist.
             */
            myExecutionDataList.addAll(testCaseStepExecution.getTestCaseExecutionDataList());
            tCExecution.setTestCaseExecutionDataList(myExecutionDataList);

            /**
             * Updating Execution Result Message only if execution result
             * message of the step is not PE or OK.
             */
            if ((!(testCaseStepExecution.getExecutionResultMessage().equals(new MessageGeneral(MessageGeneralEnum.EXECUTION_PE_TESTSTARTED))))
                    && (!(testCaseStepExecution.getExecutionResultMessage().equals(new MessageGeneral(MessageGeneralEnum.EXECUTION_OK))))) {
                tCExecution.setResultMessage(testCaseStepExecution.getExecutionResultMessage());
            }
            if (testCaseStepExecution.getStepResultMessage().equals(new MessageEvent(MessageEventEnum.STEP_PENDING))) {
                testCaseStepExecution.setStepResultMessage(new MessageEvent(MessageEventEnum.STEP_SUCCESS));
            }

            testCaseStepExecutionService.updateTestCaseStepExecution(testCaseStepExecution);

            if (testCaseStepExecution.isStopExecution()) {
                break;
            }

        }

        try {
            tCExecution = this.stopTestCase(tCExecution);
        } catch (Exception ex) {
            MyLogger.log(RunTestCaseService.class.getName(), Level.FATAL, "Exception Stopping Test: " + ex.toString());
        }

        try {
            if (!tCExecution.isSynchroneous()) {
                if (executionUUID.getExecutionID(tCExecution.getExecutionUUID()) != 0) {
                    executionUUID.removeExecutionUUID(tCExecution.getExecutionUUID());
                    MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Clean ExecutionUUID");
                }
                if (eSResponse.getExecutionSOAPResponse(tCExecution.getExecutionUUID()) != null) {
                    eSResponse.removeExecutionSOAPResponse(tCExecution.getExecutionUUID());
                    MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Clean ExecutionSOAPResponse");
                }
            }
        } catch (Exception ex) {
            MyLogger.log(RunTestCaseService.class.getName(), Level.FATAL, "Exception cleaning Memory: " + ex.toString());
        }

        MyLogger.log(RunTestCaseService.class.getName(), Level.INFO, "Execution Finished : UUID=" + tCExecution.getExecutionUUID() + "__ID=" + tCExecution.getId() + "__RC=" + tCExecution.getControlStatus());

        return tCExecution;

    }

    @Override
    public TestCaseExecution stopTestCase(TestCaseExecution tCExecution) {

        /**
         * Stop Execution
         */
        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, tCExecution.getId() + " - Stop the execution.");
        this.stopRunTestCase(tCExecution);

        /**
         * Collecting and calculating Statistics.
         */
        this.collectExecutionStats(tCExecution);

        /**
         * Saving TestCaseExecution object.
         */
        tCExecution.setEnd(new Date().getTime());
        /**
         * If at that time the execution is still PE, we move it to OK. It means
         * that no issue were met.
         */
        if ((tCExecution.getResultMessage() == null) || (tCExecution.getResultMessage().equals(new MessageGeneral(MessageGeneralEnum.EXECUTION_PE_TESTSTARTED)))) {
            tCExecution.setResultMessage(new MessageGeneral(MessageGeneralEnum.EXECUTION_OK));
        }
        try {
            testCaseExecutionService.updateTCExecution(tCExecution);
        } catch (CerberusException ex) {
            Logger.getLogger(RunTestCaseService.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        return tCExecution;
    }

    private TestCaseStepExecution executeStep(TestCaseStepExecution testCaseStepExecution) {

        // Initialise the Step Data List.
        List<TestCaseExecutionData> myStepDataList = new ArrayList<TestCaseExecutionData>();
        testCaseStepExecution.setTestCaseExecutionDataList(myStepDataList);
        // Initialise the Data List used to enter the action.
        List<TestCaseExecutionData> myActionDataList = new ArrayList<TestCaseExecutionData>();
        /**
         * Iterate Actions
         */
        List<TestCaseStepAction> testCaseStepActionList = testCaseStepExecution.getTestCaseStep().getTestCaseStepAction();
        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Getting list of actions of the step. " + testCaseStepActionList.size() + " action(s) to perform.");

        for (TestCaseStepAction testCaseStepAction : testCaseStepActionList) {

            /**
             * Start Execution of TestCaseStepAction
             */
            long startAction = new Date().getTime();

            /**
             * Create and Register TestCaseStepActionExecution.
             */
            TestCaseStepActionExecution testCaseStepActionExecution = factoryTestCaseStepActionExecution.create(
                    testCaseStepExecution.getId(), testCaseStepAction.getTest(), testCaseStepAction.getTestCase(),
                    testCaseStepAction.getStep(), testCaseStepAction.getSequence(),
                    null, null, testCaseStepAction.getAction(), testCaseStepAction.getObject(), testCaseStepAction.getProperty(),
                    startAction, 0, startAction, 0, null,null, new MessageEvent(MessageEventEnum.ACTION_PENDING), testCaseStepAction, testCaseStepExecution);
            this.testCaseStepActionExecutionService.insertTestCaseStepActionExecution(testCaseStepActionExecution);

            /**
             * Preparing the previously calculated data coming from 1/ the other
             * steps 2/ the one current step. Attaching them to the current
             * action execution.
             */
            myActionDataList.clear();
            myActionDataList.addAll(testCaseStepExecution.gettCExecution().getTestCaseExecutionDataList());
            myActionDataList.addAll(testCaseStepExecution.getTestCaseExecutionDataList());
            testCaseStepActionExecution.setTestCaseExecutionDataList(myActionDataList);

            /**
             * We calculate the property.
             */
            String propertyToCalculate = testCaseStepActionExecution.getProperty();
            if ((!propertyToCalculate.equals("null")) && !StringUtil.isNullOrEmpty(propertyToCalculate)) {
                /**
                 * Only calculate property if it is feed.
                 */
                MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Calculating property : " + propertyToCalculate);
                /**
                 * Calculating the data (Property).
                 */
                TestCaseExecutionData testCaseExecutionData = calculateProperty(testCaseStepActionExecution);
                /**
                 * Adding the calculated data to the current step Execution and
                 * ActionExecution.
                 */
                myStepDataList.add(testCaseExecutionData);
                testCaseStepExecution.setTestCaseExecutionDataList(myStepDataList);
                myActionDataList.add(testCaseExecutionData);
                testCaseStepActionExecution.setTestCaseExecutionDataList(myActionDataList);

                if (testCaseExecutionData.getPropertyResultMessage().equals(new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS))) {
                    /**
                     * If property could be calculated, we execute the action.
                     */
                    testCaseStepActionExecution.setProperty(testCaseExecutionData.getValue());
                    testCaseStepActionExecution.setPropertyName(testCaseExecutionData.getProperty());
                    MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Executing action : " + testCaseStepActionExecution.getAction() + " with property : " + testCaseStepActionExecution.getProperty());
                    testCaseStepActionExecution = this.executeAction(testCaseStepActionExecution);
                } else {
                    /**
                     * Any other cases (Property does not exist or failed to be
                     * calculated), we just don't execute the action and move
                     * Property Execution message to the action.
                     */
                    testCaseStepActionExecution.setStopExecution(testCaseExecutionData.isStopExecution());
                    testCaseStepActionExecution.setExecutionResultMessage(testCaseExecutionData.getExecutionResultMessage());

                    /**
                     * Screenshot only done when : screenshot parameter is eq to
                     * 2 or screenshot parameter is eq to 1 with the correct
                     * doScreenshot flag on the last action MessageEvent.
                     */
                    if (((testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution().getScreenshot() == 2)
                            || (testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution().getScreenshot() == 1))
                            && (testCaseExecutionData.getPropertyResultMessage().isDoScreenshot())) {

                        if (testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution().getApplication().getType().equals("GUI")) {
                            MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Doing screenshot.");
                            File myFile = null;
                            String screenshotFilename = testCaseStepActionExecution.getTest() + "-" + testCaseStepActionExecution.getTestCase()
                                    + "-St" + testCaseStepActionExecution.getStep()
                                    + "Sq" + testCaseStepActionExecution.getSequence() + ".jpg";
                            screenshotFilename = screenshotFilename.replaceAll(" ", "");
                            this.seleniumService.doScreenShot(testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution().getSelenium(), Long.toString(testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution().getId()), screenshotFilename);
                            testCaseStepActionExecution.setScreenshotFilename(Long.toString(testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution().getId()) + File.separator + screenshotFilename);
                            MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Screenshot done in : " + testCaseStepActionExecution.getScreenshotFilename());
                        }

                    }

                    /**
                     * Register the empty Action in database.
                     */
                    if (testCaseExecutionData.getPropertyResultMessage().equals(new MessageEvent(MessageEventEnum.PROPERTY_FAILED_NO_PROPERTY_DEFINITION))) {
                        MessageEvent mes = new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NO_PROPERTY_DEFINITION);
                        mes.setDescription(mes.getDescription().replaceAll("%PROP%", testCaseStepActionExecution.getProperty()));
                        mes.setDescription(mes.getDescription().replaceAll("%COUNTRY%", testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution().getCountry()));
                        testCaseStepActionExecution.setActionResultMessage(mes);
                    } else {
                        testCaseStepActionExecution.setActionResultMessage(new MessageEvent(MessageEventEnum.ACTION_FAILED_PROPERTYFAILED));
                    }
                    MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Registering Action : " + testCaseStepActionExecution.getAction());
                    this.testCaseStepActionExecutionService.updateTestCaseStepActionExecution(testCaseStepActionExecution);
                    MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Registered Action");

                }
            } else {
                /**
                 * If no property defined, we just execute the action.
                 */
                MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Executing action : " + testCaseStepActionExecution.getAction() + " without property.");
                testCaseStepActionExecution = this.executeAction(testCaseStepActionExecution);
            }

            /**
             * If Action or property reported to stop the testcase, we stop it
             * and update the step with the message.
             */
            testCaseStepExecution.setStopExecution(testCaseStepActionExecution.isStopExecution());
            if ((!(testCaseStepActionExecution.getExecutionResultMessage().equals(new MessageGeneral(MessageGeneralEnum.EXECUTION_OK))))
                    && (!(testCaseStepActionExecution.getExecutionResultMessage().equals(new MessageGeneral(MessageGeneralEnum.EXECUTION_PE_TESTEXECUTING))))) {
                testCaseStepExecution.setExecutionResultMessage(testCaseStepActionExecution.getExecutionResultMessage());
                testCaseStepExecution.setStepResultMessage(testCaseStepActionExecution.getActionResultMessage());
            }
            if (testCaseStepActionExecution.isStopExecution()) {
                break;
            }

        }
        testCaseStepExecution.setEnd(new Date().getTime());
        this.testCaseStepExecutionService.updateTestCaseStepExecution(testCaseStepExecution);

        return testCaseStepExecution;
    }

    private TestCaseStepActionExecution executeAction(TestCaseStepActionExecution testCaseStepActionExecution) {

        testCaseStepActionExecution = this.actionService.doAction(testCaseStepActionExecution);

        /**
         * Screenshot only done when : screenshot parameter is eq to 2 or
         * screenshot parameter is eq to 1 with the correct doScreenshot flag on
         * the last action MessageEvent.
         */
        if ((testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution().getScreenshot() == 2)
                || ((testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution().getScreenshot() == 1)
                && (testCaseStepActionExecution.getActionResultMessage().isDoScreenshot()))) {

            if (testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution().getApplication().getType().equals("GUI")) {
                /**
                 * Only if the return code is not equal to Cancel, meaning lost
                 * connectivity with selenium.
                 */
                if (!testCaseStepActionExecution.getReturnCode().equals("CA")) {
                    String screenshotPath = recorderService.recordScreenshotAndGetName(testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution(), testCaseStepActionExecution, 0);
                    testCaseStepActionExecution.setScreenshotFilename(screenshotPath);
                } else {
                    MyLogger.log(RunTestCaseService.class.getName(), Level.INFO, "Not Doing screenshot because connectivity with selenium server lost.");
                }

            } else if (testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution().getApplication().getType().equals("WS")) {
                String screenshotPath = recorderService.recordXMLAndGetName(testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution(), testCaseStepActionExecution, 0);
                testCaseStepActionExecution.setScreenshotFilename(screenshotPath);
            }
        } else {
            MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Not Doing screenshot after action because of the screenshot parameter or flag on the last Action result.");
        }
        
        /**
         * Get PageSource if requested by the last Action MessageEvent.
         * TODO: implement execution parameter getPageSource with 3 possibilities (Never, on KO or always).
         */
        if (testCaseStepActionExecution.getActionResultMessage().isGetPageSource()){
            if (testCaseStepActionExecution.getAction().contains("callSoap")){
            String screenshotPath = recorderService.recordXMLAndGetName(testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution(), testCaseStepActionExecution, 0);
            testCaseStepActionExecution.setScreenshotFilename(screenshotPath);
            }
        }

        /**
         * Register Action in database
         */
        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Registering Action : " + testCaseStepActionExecution.getAction());
        this.testCaseStepActionExecutionService.updateTestCaseStepActionExecution(testCaseStepActionExecution);
        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Registered Action");

        if (testCaseStepActionExecution.isStopExecution()) {
            return testCaseStepActionExecution;
        }

        /**
         * Iterate Control
         */
        List<TestCaseStepActionControl> tcsacList = testCaseStepActionExecution.getTestCaseStepAction().getTestCaseStepActionControl();
        for (TestCaseStepActionControl testCaseStepActionControl : tcsacList) {

            /**
             * Start Execution of TestCAseStepActionControl
             */
            long startControl = new Date().getTime();

            /**
             * Create and Register TestCaseStepActionControlExecution
             */
            MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Creating TestCaseStepActionControlExecution");
            TestCaseStepActionControlExecution testCaseStepActionControlExecution
                    = factoryTestCaseStepActionControlExecution.create(testCaseStepActionExecution.getId(), testCaseStepActionControl.getTest(),
                            testCaseStepActionControl.getTestCase(), testCaseStepActionControl.getStep(), testCaseStepActionControl.getSequence(), testCaseStepActionControl.getControl(),
                            null, null, testCaseStepActionControl.getType(), testCaseStepActionControl.getControlProperty(), testCaseStepActionControl.getControlValue(),
                            testCaseStepActionControl.getFatal(), startControl, 0, 0, 0, null,null, testCaseStepActionExecution, new MessageEvent(MessageEventEnum.CONTROL_PENDING));
            this.testCaseStepActionControlExecutionService.insertTestCaseStepActionControlExecution(testCaseStepActionControlExecution);

            MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Executing control : " + testCaseStepActionControlExecution.getControl() + " type : " + testCaseStepActionControlExecution.getControlType());
            testCaseStepActionControlExecution = executeControl(testCaseStepActionControlExecution);

            /**
             * We update the Action with the execution message and stop flag
             * from the control. We update the status only if the control is not
             * OK. This is to prevent moving the status to OK when it should
             * stay KO when a control failed previously.
             */
            testCaseStepActionExecution.setStopExecution(testCaseStepActionControlExecution.isStopExecution());
            if (!(testCaseStepActionControlExecution.getControlResultMessage().equals(new MessageEvent(MessageEventEnum.CONTROL_SUCCESS)))) {
                testCaseStepActionExecution.setExecutionResultMessage(testCaseStepActionControlExecution.getExecutionResultMessage());
                testCaseStepActionExecution.setActionResultMessage(testCaseStepActionControlExecution.getControlResultMessage());
            }
            /**
             * If Control reported to stop the testcase, we stop it.
             */
            if (testCaseStepActionControlExecution.isStopExecution()) {
                break;
            }

        }

        return testCaseStepActionExecution;

    }

    private TestCaseStepActionControlExecution executeControl(TestCaseStepActionControlExecution testCaseStepActionControlExecution) {

        testCaseStepActionControlExecution = this.controlService.doControl(testCaseStepActionControlExecution);

        TestCaseExecution myExecution = testCaseStepActionControlExecution.getTestCaseStepActionExecution().getTestCaseStepExecution().gettCExecution();

        /**
         * Screenshot only done when : screenshot parameter is eq to 2 or
         * screenshot parameter is eq to 1 with the correct doScreenshot flag on
         * the last control MessageEvent.
         */
        if ((myExecution.getScreenshot() == 2)
                || ((myExecution.getScreenshot() == 1) && (testCaseStepActionControlExecution.getControlResultMessage().isDoScreenshot()))) {

            if (testCaseStepActionControlExecution.getTestCaseStepActionExecution().getTestCaseStepExecution().gettCExecution().getApplication().getType().equals("GUI")) {
                /**
                 * Only if the return code is not equal to Cancel, meaning lost
                 * connectivity with selenium.
                 */
                if (!testCaseStepActionControlExecution.getReturnCode().equals("CA")) {
                    String screenshotPath = recorderService.recordScreenshotAndGetName(testCaseStepActionControlExecution.getTestCaseStepActionExecution().getTestCaseStepExecution().gettCExecution(),
                            testCaseStepActionControlExecution.getTestCaseStepActionExecution(), testCaseStepActionControlExecution.getControl());
                    testCaseStepActionControlExecution.setScreenshotFilename(screenshotPath);
                } else {
                    MyLogger.log(RunTestCaseService.class.getName(), Level.INFO, "Not Doing screenshot because connectivity with selenium server lost.");
                }
            }
            } else {
                MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Not Doing screenshot after control because of parameter of result of last control execution.");
            }
            /**
             * Register Control in database
             */
            MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Registering Control : " + testCaseStepActionControlExecution.getControl());
            this.testCaseStepActionControlExecutionService.updateTestCaseStepActionControlExecution(testCaseStepActionControlExecution);
            MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Registered Control");

            return testCaseStepActionControlExecution;
        }

    

    private TestCaseExecution stopRunTestCase(TestCaseExecution tCExecution) {
        if (tCExecution.getApplication().getType().equalsIgnoreCase("GUI")) {
            try {
                this.seleniumService.stopSeleniumServer(tCExecution.getSelenium());
            } catch (UnreachableBrowserException exception) {
                MyLogger.log(RunTestCaseService.class.getName(), Level.FATAL, "Selenium didn't manage to close browser - " + exception.toString());
            }
        }
        return tCExecution;
    }

    private TestCaseExecution collectExecutionStats(TestCaseExecution tCExecution) {
        if (tCExecution.getVerbose() > 0) {
            this.testCaseExecutionWWWService.registerSummary(tCExecution.getId());
        }
        return tCExecution;
    }

    private TestCaseExecutionData calculateProperty(TestCaseStepActionExecution testCaseStepActionExecution) {
        String propertyName = testCaseStepActionExecution.getProperty();
        String test = testCaseStepActionExecution.getTest();
        String testCase = testCaseStepActionExecution.getTestCase();
        String country = testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution().getCountry();

        /**
         * Search for property already calculated
         */
        for (TestCaseExecutionData tced : testCaseStepActionExecution.getTestCaseExecutionDataList()) {
            if (tced.getProperty().equalsIgnoreCase(propertyName)) {
                return tced;
            }
        }

        /**
         * Create and initialize the TestCaseExecutionData object
         */
        MessageEvent msg = new MessageEvent(MessageEventEnum.PROPERTY_PENDING);
        long now = new Date().getTime();
        TestCaseExecutionData testCaseExecutionData = factoryTestCaseExecutionData.create(testCaseStepActionExecution.getTestCaseStepExecution().gettCExecution().getId(), propertyName, null, null, null, null, null, null, now, now, now, now, msg);

        /**
         * Find TestCaseCountryProperty from database.
         */
        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Getting TestCaseCountryProperty definition from database : " + test + "-" + testCase + "-" + country + "-" + propertyName);
        TestCaseCountryProperties testCaseCountryProperty = null;
        try {
            testCaseCountryProperty = testCaseCountryPropertiesService.findTestCaseCountryPropertiesByKey(test, testCase, country, propertyName);
            /**
             * Feed TestCaseExecutionData object
             */
            testCaseExecutionData.setType(testCaseCountryProperty.getType());
            testCaseExecutionData.setValue1(testCaseCountryProperty.getValue1());
            testCaseExecutionData.setValue2(testCaseCountryProperty.getValue2());
            testCaseExecutionData.setTestCaseCountryProperties(testCaseCountryProperty);
            MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Calculating property : " + testCaseCountryProperty.getProperty());
            testCaseExecutionData = this.propertyService.calculateProperty(testCaseExecutionData, testCaseStepActionExecution, testCaseCountryProperty);
            try {
                /**
                 * Inserting testCaseExecutionData into the database.
                 */
                testCaseExecutionDataService.insertTestCaseExecutionData(testCaseExecutionData);
            } catch (CerberusException ex1) {
                Logger.getLogger(RunTestCaseService.class.getName()).log(java.util.logging.Level.SEVERE, null, ex1);
            }

        } catch (CerberusException ex) {
            MessageEvent msg1 = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_NO_PROPERTY_DEFINITION);
            msg1.setDescription(msg1.getDescription().replaceAll("%COUNTRY%", country));
            msg1.setDescription(msg1.getDescription().replaceAll("%PROP%", propertyName));
            testCaseExecutionData.setPropertyResultMessage(msg1);

            try {
                testCaseExecutionDataService.insertTestCaseExecutionData(testCaseExecutionData);
            } catch (CerberusException ex1) {
                Logger.getLogger(RunTestCaseService.class.getName()).log(java.util.logging.Level.SEVERE, null, ex1);
            }
        }

        return testCaseExecutionData;
    }

    @Override
    @Async
    public TestCaseExecution executeAsynchroneouslyTestCase(TestCaseExecution tCExecution) throws CerberusException {
        try {
            return executeTestCase(tCExecution);
        } catch (CerberusException ex) {
            throw new CerberusException(ex.getMessageError());
        }
    }

}
