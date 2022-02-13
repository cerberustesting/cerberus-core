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

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.crud.entity.Application;
import org.cerberus.crud.entity.CountryEnvLink;
import org.cerberus.crud.entity.CountryEnvParam;
import org.cerberus.crud.entity.EventHook;
import org.cerberus.crud.entity.RobotCapability;
import org.cerberus.crud.entity.Tag;
import org.cerberus.crud.entity.Test;
import org.cerberus.crud.entity.TestCase;
import org.cerberus.crud.entity.TestCaseCountryProperties;
import org.cerberus.crud.entity.TestCaseExecution;
import org.cerberus.crud.entity.TestCaseExecutionData;
import org.cerberus.crud.entity.TestCaseExecutionSysVer;
import org.cerberus.crud.entity.TestCaseStep;
import org.cerberus.crud.entity.TestCaseStepAction;
import org.cerberus.crud.entity.TestCaseStepActionControl;
import org.cerberus.crud.entity.TestCaseStepActionControlExecution;
import org.cerberus.crud.entity.TestCaseStepActionExecution;
import org.cerberus.crud.entity.TestCaseStepExecution;
import org.cerberus.crud.factory.IFactoryRobotCapability;
import org.cerberus.crud.factory.IFactoryTestCaseExecutionSysVer;
import org.cerberus.crud.factory.IFactoryTestCaseStepActionControlExecution;
import org.cerberus.crud.factory.IFactoryTestCaseStepActionExecution;
import org.cerberus.crud.factory.IFactoryTestCaseStepExecution;
import org.cerberus.crud.service.IAppServiceService;
import org.cerberus.crud.service.ICountryEnvLinkService;
import org.cerberus.crud.service.ICountryEnvParamService;
import org.cerberus.crud.service.ILoadTestCaseService;
import org.cerberus.crud.service.IParameterService;
import org.cerberus.crud.service.ITagService;
import org.cerberus.crud.service.ITestCaseCountryPropertiesService;
import org.cerberus.crud.service.ITestCaseExecutionDataService;
import org.cerberus.crud.service.ITestCaseExecutionQueueDepService;
import org.cerberus.crud.service.ITestCaseExecutionQueueService;
import org.cerberus.crud.service.ITestCaseExecutionService;
import org.cerberus.crud.service.ITestCaseExecutionSysVerService;
import org.cerberus.crud.service.ITestCaseService;
import org.cerberus.crud.service.ITestCaseStepActionControlExecutionService;
import org.cerberus.crud.service.ITestCaseStepActionExecutionService;
import org.cerberus.crud.service.ITestCaseStepExecutionService;
import org.cerberus.engine.entity.ExecutionUUID;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.engine.entity.MessageGeneral;
import org.cerberus.engine.execution.IConditionService;
import org.cerberus.engine.execution.IExecutionRunService;
import org.cerberus.engine.execution.IRecorderService;
import org.cerberus.engine.execution.IRetriesService;
import org.cerberus.engine.execution.IRobotServerService;
import org.cerberus.engine.execution.enums.ConditionOperatorEnum;
import org.cerberus.engine.execution.video.VideoRecorder;
import org.cerberus.engine.gwt.IActionService;
import org.cerberus.engine.gwt.IControlService;
import org.cerberus.engine.gwt.IVariableService;
import org.cerberus.engine.queuemanagement.IExecutionThreadPoolService;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.enums.Video;
import org.cerberus.event.IEventService;
import org.cerberus.exception.CerberusEventException;
import org.cerberus.exception.CerberusException;
import org.cerberus.service.executor.IExecutorService;
import org.cerberus.service.kafka.IKafkaService;
import org.cerberus.service.robotproviders.IBrowserstackService;
import org.cerberus.service.robotproviders.IKobitonService;
import org.cerberus.service.robotproviders.ILambdaTestService;
import org.cerberus.service.sikuli.ISikuliService;
import org.cerberus.session.SessionCounter;
import org.cerberus.util.DateUtil;
import org.cerberus.util.StringUtil;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.websocket.TestCaseExecutionEndPoint;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriverException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 */
@EnableAsync
@Service
public class ExecutionRunService implements IExecutionRunService {

    private static final Logger LOG = LogManager.getLogger(ExecutionRunService.class);

    @Autowired
    private ISikuliService sikuliService;
    @Autowired
    private IRobotServerService robotServerService;
    @Autowired
    private IActionService actionService;
    @Autowired
    private IControlService controlService;
    @Autowired
    private IConditionService conditionService;
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
    private ITestCaseExecutionQueueService executionQueueService;
    @Autowired
    private IExecutionThreadPoolService executionThreadPoolService;
    @Autowired
    private ITestCaseCountryPropertiesService testCaseCountryPropertiesService;
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
    private IFactoryTestCaseExecutionSysVer factoryTestCaseExecutionSysVer;
    @Autowired
    private ExecutionUUID executionUUID;
    @Autowired
    private SessionCounter sessionCounter;
    @Autowired
    private IRecorderService recorderService;
    @Autowired
    private IVariableService variableService;
    @Autowired
    private IParameterService parameterService;
    @Autowired
    private ITagService tagService;
    @Autowired
    private IRetriesService retriesService;
    @Autowired
    private IFactoryRobotCapability robotCapabilityFactory;
    @Autowired
    private ITestCaseExecutionQueueDepService testCaseExecutionQueueDepService;
    @Autowired
    private ITestCaseExecutionDataService testCaseExecutionDataService;
    @Autowired
    private IBrowserstackService browserstackService;
    @Autowired
    private IKobitonService kobitonService;
    @Autowired
    private ILambdaTestService lambdaTestService;
    @Autowired
    private IKafkaService kafkaService;
    @Autowired
    private IAppServiceService appServiceService;
    @Autowired
    private IExecutorService executorService;
    @Autowired
    private IEventService eventService;

    @Override
    public TestCaseExecution executeTestCase(TestCaseExecution execution) throws CerberusException {
        long runID = execution.getId();
        String logPrefix = runID + " - ";

        VideoRecorder videoRecorder = null;
        /**
         * Feeding Build Rev of main Application system to
         * testcaseexecutionsysver table. Only if execution is not manual.
         */
        try {

            AnswerItem<String> answerDecode = new AnswerItem<>();

            if (!(execution.getManualURL() >= 1)) {
                /**
                 * Insert SystemVersion in Database
                 */
                TestCaseExecutionSysVer myExeSysVer = null;
                try {
                    LOG.debug(logPrefix + "Registering Main System Version.");
                    myExeSysVer = factoryTestCaseExecutionSysVer.create(runID, execution.getApplicationObj().getSystem(), execution.getBuild(), execution.getRevision());
                    testCaseExecutionSysVerService.insertTestCaseExecutionSysVer(myExeSysVer);
                } catch (CerberusException ex) {
                    LOG.error(logPrefix + ex.getMessage(), ex);
                }
                LOG.debug(logPrefix + "Main System Version Registered.");

                /**
                 * For all Linked environment, we also keep track on the
                 * build/rev information inside testcaseexecutionsysver table.
                 */
                LOG.debug(logPrefix + "Registering Linked System Version.");
                try {
                    List<CountryEnvLink> ceLink = null;
                    ceLink = countryEnvLinkService.convert(countryEnvLinkService.readByVarious(execution.getApplicationObj().getSystem(), execution.getCountry(), execution.getEnvironment()));
                    for (CountryEnvLink myCeLink : ceLink) {
                        LOG.debug(logPrefix + "Linked environment found : " + myCeLink.getSystemLink() + myCeLink.getCountryLink() + myCeLink.getEnvironmentLink());

                        CountryEnvParam mycountEnvParam;
                        try {
                            mycountEnvParam = this.countryEnvParamService.convert(this.countryEnvParamService.readByKey(myCeLink.getSystemLink(), myCeLink.getCountryLink(), myCeLink.getEnvironmentLink()));
                            myExeSysVer = factoryTestCaseExecutionSysVer.create(runID, myCeLink.getSystemLink(), mycountEnvParam.getBuild(), mycountEnvParam.getRevision());
                            testCaseExecutionSysVerService.insertTestCaseExecutionSysVer(myExeSysVer);
                        } catch (CerberusException ex) {
                            // Referencial Integrity link between countryEnvLink and CountryEnvParam table should secure that exception to never happen.
                            LOG.error(logPrefix + ex.getMessage(), ex);
                            throw new CerberusException(ex.getMessageError());
                        }
                    }
                } catch (CerberusException ex) {
                    LOG.debug(logPrefix + "No Linked environment found.");
                }
                LOG.debug(logPrefix + "Linked System Version Registered.");
            }

            /**
             * Start robot server if execution is not manual
             */
            if (!execution.getManualExecution().equals("Y")) {
                if (execution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_GUI)
                        || execution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_APK)
                        || execution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_IPA)
                        || execution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_FAT)) {

                    MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.EXECUTION_PE_PREPARINGROBOTSERVER);
                    mes.setDescription(mes.getDescription().replace("%IP%", execution.getRobotHost()));
                    execution.setResultMessage(mes);
                    updateExecutionWebSocketOnly(execution, true);

                    // Decoding Robot capabilities.
                    if (execution.getRobotObj() != null) {
                        List<RobotCapability> caps = execution.getRobotObj().getCapabilities();
                        List<RobotCapability> capsDecoded = new ArrayList<>();

                        // TODO ce n'est pas ça encore, faut faire ça au moment ou il recupère l'ip / port
                        if (execution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_APK)) {
                            int portNumber = 8200;
                            portNumber++;
                        }

                        for (RobotCapability cap : caps) {

                            String capDecoded = "";
                            try {
                                answerDecode = variableService.decodeStringCompletly(cap.getCapability(), execution, null, false);
                                capDecoded = answerDecode.getItem();

                                if (!(answerDecode.isCodeStringEquals("OK"))) {
                                    // If anything wrong with the decode --> we stop here with decode message in the action result.
                                    LOG.debug(logPrefix + "TestCase interupted due to decode 'Robot Capability key' Error.");
                                    throw new CerberusException(new MessageGeneral(MessageGeneralEnum.EXECUTION_FA_CAPABILITYDECODE)
                                            .resolveDescription("MES", answerDecode.getMessageDescription())
                                            .resolveDescription("FIELD", "")
                                            .resolveDescription("AREA", "Robot Capability key : " + cap.getCapability()));
                                }
                            } catch (CerberusEventException cex) {
                                LOG.warn(cex);
                            }

                            String valDecoded = "";
                            try {
                                answerDecode = variableService.decodeStringCompletly(cap.getValue(), execution, null, false);
                                valDecoded = answerDecode.getItem();

                                if (!(answerDecode.isCodeStringEquals("OK"))) {
                                    // If anything wrong with the decode --> we stop here with decode message in the action result.
                                    LOG.debug(logPrefix + "TestCase interupted due to decode 'Robot Capability value' Error.");
                                    throw new CerberusException(new MessageGeneral(MessageGeneralEnum.EXECUTION_FA_CAPABILITYDECODE)
                                            .resolveDescription("MES", answerDecode.getMessageDescription())
                                            .resolveDescription("FIELD", "")
                                            .resolveDescription("AREA", "Robot Capability value : " + cap.getValue()));
                                }
                            } catch (CerberusEventException cex) {
                                LOG.warn(cex);
                            }

                            capsDecoded.add(robotCapabilityFactory.create(cap.getId(), cap.getRobot(), capDecoded, valDecoded));
                        }
                        execution.getRobotObj().setCapabilitiesDecoded(capsDecoded);
                    }

                    mes = new MessageGeneral(MessageGeneralEnum.EXECUTION_PE_STARTINGROBOTSERVER);
                    mes.setDescription(mes.getDescription().replace("%IP%", execution.getRobotHost()));
                    execution.setResultMessage(mes);
                    updateExecution(execution, true);

                    if (execution.getRobotHost().isEmpty()) {
                        mes = new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_SELENIUM_EMPTYORBADIP);
                        mes.setDescription(mes.getDescription().replace("%IP%", execution.getRobotHost()));
                        LOG.debug(logPrefix + mes.getDescription());
                        throw new CerberusException(mes);

                    } else {
                        /**
                         * Start Robot server (Selenium/Appium/Sikuli)
                         */
                        LOG.debug(logPrefix + "Starting Robot Server.");
                        try {
                            this.robotServerService.startServer(execution);
                            LOG.debug(logPrefix + "Robot Server Started.");
                        } catch (CerberusException ex) {
                            // No need to report exception message as it will be catched and reported later
                            // LOG.debug(logPrefix + ex.getMessageError().getDescription());
                            throw new CerberusException(ex.getMessageError(), ex);
                        }

                        // Start video
                        try {
                            if (Video.recordVideo(execution.getVideo())) {
                                videoRecorder = VideoRecorder.getInstance(execution, recorderService);
                                videoRecorder.beginRecordVideo();
                            }
                        } catch (UnsupportedOperationException ex) {
                            LOG.info(ex.getMessage()); // log only message that application type is not supported
                        }
                    }

                }
            }

            /**
             * For BrowserStack and LambdaTest, we try to enrich the Tag with
             * build hash.
             */
            switch (execution.getRobotProvider()) {
                case TestCaseExecution.ROBOTPROVIDER_BROWSERSTACK:
                case TestCaseExecution.ROBOTPROVIDER_LAMBDATEST:
                    String newBuildHash = tagService.enrichTagWithCloudProviderBuild(execution.getRobotProvider(), execution.getSystem(), execution.getTag(), execution.getRobotExecutorObj().getHostUser(), execution.getRobotExecutorObj().getHostPassword());
                    Tag newTag = tagService.convert(tagService.readByKey(execution.getTag()));
                    execution.setTagObj(newTag);
                    break;
            }

            /**
             * Get used SeleniumCapabilities (empty if application is not GUI)
             */
            LOG.debug(logPrefix + "Getting Selenium capabitities for GUI applications.");
            if (execution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_GUI) && !execution.getManualExecution().equals("Y")) {
                try {
                    Capabilities caps = this.robotServerService.getUsedCapabilities(execution.getSession());
                    execution.setVersion(caps.getVersion());
                    execution.setPlatform(caps.getPlatform().toString());
                } catch (Exception ex) {
                    LOG.error(logPrefix + "Exception on Selenium getting Used Capabilities.", ex);
                }
                LOG.debug(logPrefix + "Selenium capabitities loaded.");
            } else if ((execution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_APK) || execution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_IPA)) && !execution.getManualExecution().equals("Y")) {
                //do nothing, and keep the robot name
            } else if (!execution.getManualExecution().equals("Y")) {
                // If Selenium is not needed, the selenium and browser info is set to empty.
                execution.setSeleniumIP("");
                execution.setSeleniumPort("");
                execution.setBrowser("");
                execution.setVersion("");
                execution.setPlatform("");
                execution.setRobotDecli("");
                LOG.debug(logPrefix + "No Selenium capabitities loaded because application not (GUI,IPA,APK) : " + execution.getApplicationObj().getType());
            }
            execution.setRobotDecli(execution.getRobotDecli().replace("%BROWSER%", execution.getBrowser()));
            execution.setRobotDecli(execution.getRobotDecli().replace("%BROWSERVERSION%", execution.getVersion()));
            execution.setRobotDecli(execution.getRobotDecli().replace("%PLATFORM%", execution.getPlatform()));

            /**
             * Load Pre TestCase information
             */
            execution.setResultMessage(new MessageGeneral(MessageGeneralEnum.EXECUTION_PE_LOADINGDETAILEDDATA));
            updateExecutionWebSocketOnly(execution, true);

            LOG.debug(logPrefix + "Loading Pre-testcases.");
            List<TestCase> preTests = testCaseService.getTestCaseForPrePostTesting(Test.TEST_PRETESTING, execution.getTestCaseObj().getApplication(), execution.getCountry(),
                    execution.getSystem(), execution.getCountryEnvParam().getBuild(), execution.getCountryEnvParam().getRevision());
            if (!(preTests == null)) {
                LOG.debug(logPrefix + "Loaded PreTest List. " + preTests.size() + " found.");
            }
            LOG.debug(logPrefix + "Pre-testcases Loaded.");

            /**
             * Load Pre TestCase with Step dependencies (Actions/Control)
             */
            LOG.debug(logPrefix + "Loading all Steps information (Actions & Controls) of all Pre-testcase.");
            List<TestCaseStep> preTestCaseStepList = new ArrayList<>();
            for (TestCase myTCase : preTests) {
                preTestCaseStepList.addAll(this.loadTestCaseService.loadTestCaseStep(myTCase));
                LOG.debug(logPrefix + "Pre testcase : " + myTCase.getTest() + "-" + myTCase.getTestcase() + " Loaded With all Step(s) found.");
            }
            LOG.debug(logPrefix + "All Steps information (Actions & Controls) of all Pre-testcase Loaded.");

            /**
             * Load Post TestCase information
             */
            LOG.debug(logPrefix + "Loading Post-testcases.");
            List<TestCase> postTests = testCaseService.getTestCaseForPrePostTesting(Test.TEST_POSTTESTING, execution.getTestCaseObj().getApplication(), execution.getCountry(),
                    execution.getSystem(), execution.getCountryEnvParam().getBuild(), execution.getCountryEnvParam().getRevision());
            if (!(postTests == null)) {
                LOG.debug(logPrefix + "Loaded PostTest List. " + postTests.size() + " found.");
            }
            LOG.debug(logPrefix + "Post-testcases Loaded.");

            /**
             * Load Post TestCase with Step dependencies (Actions/Control)
             */
            LOG.debug(logPrefix + "Loading all Steps information (Actions & Controls) of all Post-testcase.");
            List<TestCaseStep> postTestCaseStepList = new ArrayList<>();
            for (TestCase myTCase : postTests) {
                postTestCaseStepList.addAll(this.loadTestCaseService.loadTestCaseStep(myTCase));
                LOG.debug(logPrefix + "Post testcase : " + myTCase.getTest() + "-" + myTCase.getTestcase() + " Loaded With all Step(s) found.");
            }
            LOG.debug(logPrefix + "All Steps information (Actions & Controls) of all Post-testcase Loaded.");

            /**
             * Load Main TestCase with Step dependencies (Actions/Control)
             */
            LOG.debug(logPrefix + "Loading all Steps information of Main testcase.");
            List<TestCaseStep> testCaseStepList;
            testCaseStepList = this.loadTestCaseService.loadTestCaseStep(execution.getTestCaseObj());
            execution.getTestCaseObj().setSteps(testCaseStepList);
            LOG.debug(logPrefix + "Steps information of Main testcase Loaded : " + execution.getTestCaseObj().getSteps().size() + " Step(s) found.");

            /**
             * Load All properties of the testcase
             */
            LOG.debug(logPrefix + "Loading all Properties.");
            List<TestCaseCountryProperties> tcProperties = new ArrayList<>();
            try {
                tcProperties = testCaseCountryPropertiesService.findAllWithDependencies(execution.getTest(), execution.getTestCase(), execution.getCountry(),
                        execution.getSystem(), execution.getCountryEnvParam().getBuild(), execution.getCountryEnvParam().getRevision());
                execution.setTestCaseCountryPropertyList(tcProperties);
            } catch (CerberusException ex) {
                LOG.warn("Exception getting all the properties : ", ex);
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug(logPrefix + "All Properties Loaded. " + tcProperties.size() + " property(ies) found : " + tcProperties);
            }

            /**
             * Load All Execution Data of testcases that this execution depends
             */
            LOG.debug(logPrefix + "Loading all Execution Data of the execution from queue dependencies.");
            this.testCaseExecutionDataService.loadTestCaseExecutionDataFromDependencies(execution);

            /**
             * Start Execution of the steps/Actions/controls Iterate Steps.
             * mainExecutionTestCaseStepList will contain the list of steps to
             * execute for both pretest and test. This is where we schedule the
             * execution of the steps using mainExecutionTestCaseStepList
             * object.
             */
            LOG.debug(logPrefix + "Starting the execution with step iteration.");
            List<TestCaseStep> mainExecutionTestCaseStepList;
            mainExecutionTestCaseStepList = new ArrayList<>();
            mainExecutionTestCaseStepList.addAll(preTestCaseStepList);
            mainExecutionTestCaseStepList.addAll(testCaseStepList);
            mainExecutionTestCaseStepList.addAll(postTestCaseStepList);

            /**
             * Open Kafka Consumers
             */
            execution.setResultMessage(new MessageGeneral(MessageGeneralEnum.EXECUTION_PE_LOADINGKAFKACONSUMERS));
            updateExecutionWebSocketOnly(execution, true);
            execution.setKafkaLatestOffset(kafkaService.getAllConsumers(mainExecutionTestCaseStepList, execution));

            /**
             * Initialize the global TestCaseExecution Data List.
             */
            //
            execution.setResultMessage(new MessageGeneral(MessageGeneralEnum.EXECUTION_PE_TESTEXECUTING));
            updateExecution(execution, true);

            // Evaluate the condition at the step level.
            AnswerItem<Boolean> conditionAnswerTc;
            boolean conditionDecodeError = false;
            /**
             * If execution is not manual, evaluate the condition at the step
             * level
             */
            if (!execution.getManualExecution().equals("Y")) {
                try {
                    answerDecode = variableService.decodeStringCompletly(execution.getConditionVal1(), execution, null, false);
                    execution.setConditionVal1(answerDecode.getItem());

                    if (!(answerDecode.isCodeStringEquals("OK"))) {
                        // If anything wrong with the decode --> we stop here with decode message in the action result.
                        execution.setResultMessage(new MessageGeneral(MessageGeneralEnum.EXECUTION_FA_DECODE)
                                .resolveDescription("MES", answerDecode.getMessageDescription())
                                .resolveDescription("AREA", "TestCase Condition Value1"));
                        execution.setEnd(new Date().getTime());
                        LOG.debug(logPrefix + "TestCase interupted due to decode 'TestCase Condition Value1' Error.");
                        conditionDecodeError = true;
                    }
                } catch (CerberusEventException cex) {
                    LOG.warn(cex);
                }
                try {
                    answerDecode = variableService.decodeStringCompletly(execution.getConditionVal2(), execution, null, false);
                    execution.setConditionVal2(answerDecode.getItem());

                    if (!(answerDecode.isCodeStringEquals("OK"))) {
                        // If anything wrong with the decode --> we stop here with decode message in the action result.
                        execution.setResultMessage(new MessageGeneral(MessageGeneralEnum.EXECUTION_FA_DECODE)
                                .resolveDescription("MES", answerDecode.getMessageDescription())
                                .resolveDescription("AREA", "TestCase Condition Value2"));
                        execution.setEnd(new Date().getTime());
                        LOG.debug(logPrefix + "TestCase interupted due to decode 'TestCase Condition Value2' Error.");
                        conditionDecodeError = true;
                    }
                } catch (CerberusEventException cex) {
                    LOG.warn(cex);
                }
                try {
                    answerDecode = variableService.decodeStringCompletly(execution.getConditionVal3(), execution, null, false);
                    execution.setConditionVal3(answerDecode.getItem());

                    if (!(answerDecode.isCodeStringEquals("OK"))) {
                        // If anything wrong with the decode --> we stop here with decode message in the action result.
                        execution.setResultMessage(new MessageGeneral(MessageGeneralEnum.EXECUTION_FA_DECODE)
                                .resolveDescription("MES", answerDecode.getMessageDescription())
                                .resolveDescription("AREA", "TestCase Condition Value3"));
                        execution.setEnd(new Date().getTime());
                        LOG.debug(logPrefix + "TestCase interupted due to decode 'TestCase Condition Value3Error.");
                        conditionDecodeError = true;
                    }
                } catch (CerberusEventException cex) {
                    LOG.warn(cex);
                }
            }

            if (!conditionDecodeError) {

                conditionAnswerTc = this.conditionService.evaluateCondition(execution.getConditionOperator(),
                        execution.getConditionVal1(), execution.getConditionVal2(), execution.getConditionVal3(),
                        execution, execution.getConditionOptions());

                boolean execute_TestCase = conditionAnswerTc.getItem();

                if (execute_TestCase || execution.getManualExecution().equals("Y")) {

                    boolean doStepStopExecution = false;
                    for (TestCaseStep testCaseStep : mainExecutionTestCaseStepList) {

                        ConditionOperatorEnum testcaseStepConditionEnum = ConditionOperatorEnum.getConditionOperatorEnumFromString(testCaseStep.getConditionOperator());

                        // exeMod management : We trigger Forced Step no matter if previous step execution asked to stop.
                        if (!doStepStopExecution || testCaseStep.isExecutionForced()) {

                            // init the index of the step in case we loop.
                            int step_index = 1;
                            boolean execute_Next_Step = false;
                            TestCaseStepExecution stepExecution;
                            int maxloop = parameterService.getParameterIntegerByKey("cerberus_loopstep_max", execution.getApplicationObj().getSystem(), 20);

                            // Step Loop management.
                            do {

                                /**
                                 * Start Execution of TestCaseStep
                                 */
                                LOG.debug(logPrefix + "Start execution of testcasestep");
                                long startStep = new Date().getTime();

                                /**
                                 * Create and Register TestCaseStepExecution
                                 */
                                MessageEvent stepMess = new MessageEvent(MessageEventEnum.STEP_PENDING)
                                        .resolveDescription("STEP", String.valueOf(testCaseStep.getSort()))
                                        .resolveDescription("STEPINDEX", String.valueOf(step_index));
                                stepExecution = factoryTestCaseStepExecution.create(
                                        runID, testCaseStep.getTest(), testCaseStep.getTestcase(),
                                        testCaseStep.getStepId(), step_index, testCaseStep.getSort(), testCaseStep.getLoop(), testCaseStep.getConditionOperator(), testCaseStep.getConditionValue1(), testCaseStep.getConditionValue2(), testCaseStep.getConditionValue3(), testCaseStep.getConditionValue1(), testCaseStep.getConditionValue2(), testCaseStep.getConditionValue3(), null,
                                        startStep, startStep, startStep, startStep, new BigDecimal("0"), null, stepMess, testCaseStep, execution,
                                        testCaseStep.isUsingLibraryStep(), testCaseStep.getLibraryStepTest(), testCaseStep.getLibraryStepTestcase(), testCaseStep.getLibraryStepStepId(), testCaseStep.getDescription());
                                stepExecution.setLoop(testCaseStep.getLoop());
                                stepExecution.setConditionOptions(testCaseStep.getConditionOptionsActive());

                                testCaseStepExecutionService.insertTestCaseStepExecution(stepExecution);
                                stepExecution.setExecutionResultMessage(new MessageGeneral(MessageGeneralEnum.EXECUTION_PE_TESTSTARTED));

                                /**
                                 * We populate the TestCaseStep inside the
                                 * execution List
                                 */
                                execution.addTestCaseStepExecutionList(stepExecution);

                                // determine if step is executed (execute_Step) and if we trigger a new step execution after (execute_Next_Step)
                                boolean execute_Step = true;
                                boolean descriptionOrConditionStepDecodeError = false;
                                boolean conditionStepError = false;
                                AnswerItem<Boolean> conditionAnswer = new AnswerItem<>(new MessageEvent(MessageEventEnum.CONDITIONEVAL_FAILED_UNKNOWNCONDITION));
                                if (!((stepExecution.getLoop().equals(TestCaseStep.LOOP_DOWHILECONDITIONTRUE)
                                        || stepExecution.getLoop().equals(TestCaseStep.LOOP_DOWHILECONDITIONFALSE))
                                        && step_index == 1)) {
                                    // We don't decode value1, value2 and value3 if loop condition is a doWhile in order to prevent error message on condition that will not be executed.
                                    if (!descriptionOrConditionStepDecodeError) {
                                        try {
                                            answerDecode = variableService.decodeStringCompletly(stepExecution.getConditionValue1(), execution, null, false);
                                            stepExecution.setConditionValue1(answerDecode.getItem());
                                            if (!(answerDecode.isCodeStringEquals("OK"))) {
                                                stepExecution.setExecutionResultMessage(new MessageGeneral(answerDecode.getResultMessage().getMessage()));
                                                stepExecution.setStepResultMessage(answerDecode.getResultMessage().resolveDescription("FIELD", "Step Condition Value1"));
                                                stepExecution.setReturnMessage(answerDecode.getResultMessage().resolveDescription("FIELD", "Step Condition Value1").getDescription());
                                                stepExecution.setReturnCode(answerDecode.getResultMessage().getCodeString());
                                                stepExecution.setStopExecution(answerDecode.getResultMessage().isStopTest());
                                                stepExecution.setEnd(new Date().getTime());
                                                LOG.debug(logPrefix + "Step interupted due to decode 'Step Condition Value1' Error.");
                                                descriptionOrConditionStepDecodeError = true;
                                            }
                                        } catch (CerberusEventException cex) {
                                            LOG.warn(cex);
                                        }
                                    }
                                    if (!descriptionOrConditionStepDecodeError) {
                                        try {
                                            answerDecode = variableService.decodeStringCompletly(stepExecution.getConditionValue2(), execution, null, false);
                                            stepExecution.setConditionValue2(answerDecode.getItem());
                                            if (!(answerDecode.isCodeStringEquals("OK"))) {
                                                stepExecution.setExecutionResultMessage(new MessageGeneral(answerDecode.getResultMessage().getMessage()));
                                                stepExecution.setStepResultMessage(answerDecode.getResultMessage().resolveDescription("FIELD", "Step Condition Value2"));
                                                stepExecution.setReturnMessage(answerDecode.getResultMessage().resolveDescription("FIELD", "Step Condition Value2").getDescription());
                                                stepExecution.setReturnCode(answerDecode.getResultMessage().getCodeString());
                                                stepExecution.setStopExecution(answerDecode.getResultMessage().isStopTest());
                                                stepExecution.setEnd(new Date().getTime());
                                                LOG.debug(logPrefix + "Step interupted due to decode 'Step Condition Value2' Error.");
                                                descriptionOrConditionStepDecodeError = true;
                                            }
                                        } catch (CerberusEventException cex) {
                                            LOG.warn(cex);
                                        }
                                    }
                                    if (!descriptionOrConditionStepDecodeError) {
                                        try {
                                            answerDecode = variableService.decodeStringCompletly(stepExecution.getConditionValue3(), execution, null, false);
                                            stepExecution.setConditionValue3(answerDecode.getItem());
                                            if (!(answerDecode.isCodeStringEquals("OK"))) {
                                                stepExecution.setExecutionResultMessage(new MessageGeneral(answerDecode.getResultMessage().getMessage()));
                                                stepExecution.setStepResultMessage(answerDecode.getResultMessage().resolveDescription("FIELD", "Step Condition Value3"));
                                                stepExecution.setReturnMessage(answerDecode.getResultMessage().resolveDescription("FIELD", "Step Condition Value3").getDescription());
                                                stepExecution.setReturnCode(answerDecode.getResultMessage().getCodeString());
                                                stepExecution.setStopExecution(answerDecode.getResultMessage().isStopTest());
                                                stepExecution.setEnd(new Date().getTime());
                                                LOG.debug(logPrefix + "Step interupted due to decode 'Step Condition Value3' Error.");
                                                descriptionOrConditionStepDecodeError = true;
                                            }
                                        } catch (CerberusEventException cex) {
                                            LOG.warn(cex);
                                        }
                                    }
                                }

                                if (!descriptionOrConditionStepDecodeError) {
                                    try {
                                        answerDecode = variableService.decodeStringCompletly(stepExecution.getDescription(), execution, null, false);
                                        stepExecution.setDescription(answerDecode.getItem());
                                        if (!(answerDecode.isCodeStringEquals("OK"))) {
                                            stepExecution.setExecutionResultMessage(new MessageGeneral(answerDecode.getResultMessage().getMessage()));
                                            stepExecution.setStepResultMessage(answerDecode.getResultMessage().resolveDescription("FIELD", "Step Description"));
                                            stepExecution.setReturnMessage(answerDecode.getResultMessage().resolveDescription("FIELD", "Step Description").getDescription());
                                            stepExecution.setReturnCode(answerDecode.getResultMessage().getCodeString());
                                            stepExecution.setStopExecution(answerDecode.getResultMessage().isStopTest());
                                            stepExecution.setEnd(new Date().getTime());
                                            LOG.debug(logPrefix + "Step interupted due to decode 'Step Description' Error.");
                                            descriptionOrConditionStepDecodeError = true;
                                        }
                                    } catch (CerberusEventException cex) {
                                        LOG.warn(cex);
                                    }
                                }

                                if (stepExecution.getLoop().equals(TestCaseStep.LOOP_ONCEIFCONDITIONFALSE)
                                        || stepExecution.getLoop().equals(TestCaseStep.LOOP_ONCEIFCONDITIONTRUE)
                                        || stepExecution.getLoop().equals(TestCaseStep.LOOP_WHILECONDITIONFALSEDO)
                                        || stepExecution.getLoop().equals(TestCaseStep.LOOP_WHILECONDITIONTRUEDO)
                                        || stepExecution.getLoop().isEmpty()
                                        || step_index > 1) {
                                    if (!(descriptionOrConditionStepDecodeError)) {

                                        conditionAnswer = this.conditionService.evaluateCondition(
                                                stepExecution.getConditionOperator(),
                                                stepExecution.getConditionValue1(), stepExecution.getConditionValue2(), stepExecution.getConditionValue3(),
                                                execution, stepExecution.getConditionOptions());

                                        execute_Step = conditionAnswer.getItem();
                                        if (conditionAnswer.getResultMessage().getMessage().getCodeString().equals("PE")) {
                                            // There were no error when performing the condition evaluation.
                                            switch (stepExecution.getLoop()) {
                                                case TestCaseStep.LOOP_ONCEIFCONDITIONFALSE:
                                                    execute_Step = !execute_Step;
                                                    execute_Next_Step = false;
                                                    break;
                                                case TestCaseStep.LOOP_ONCEIFCONDITIONTRUE:
                                                case "":
                                                    execute_Next_Step = false;
                                                    break;
                                                case TestCaseStep.LOOP_WHILECONDITIONFALSEDO:
                                                case TestCaseStep.LOOP_DOWHILECONDITIONFALSE:
                                                    execute_Step = !execute_Step;
                                                    execute_Next_Step = execute_Step;
                                                    break;
                                                case TestCaseStep.LOOP_WHILECONDITIONTRUEDO:
                                                case TestCaseStep.LOOP_DOWHILECONDITIONTRUE:
                                                    execute_Next_Step = execute_Step;
                                                    break;
                                                default:
                                                    execute_Next_Step = false;
                                            }
                                        } else {
                                            // Error when performing the condition evaluation. We force no execution (false)
                                            MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.EXECUTION_FA_CONDITION);
                                            mes.setDescription(mes.getDescription()
                                                    .replace("%AREA%", "step ")
                                                    .replace("%COND%", stepExecution.getConditionOperator())
                                                    .replace("%MES%", conditionAnswer.getResultMessage().getDescription()));
                                            execution.setResultMessage(mes);
                                            stepExecution.setExecutionResultMessage(mes);

                                            stepExecution.setStepResultMessage(new MessageEvent(MessageEventEnum.CONDITION_TESTCASESTEP_FAILED)
                                                    .resolveDescription("AREA", "")
                                                    .resolveDescription("COND", stepExecution.getConditionOperator())
                                                    .resolveDescription("MESSAGE", conditionAnswer.getResultMessage().getDescription())
                                            );

                                            stepExecution.setEnd(new Date().getTime());
                                            LOG.debug(logPrefix + "Step interupted due to condition error.");
                                            conditionStepError = true;
                                            execute_Next_Step = false;
                                            execute_Step = false;
                                        }
                                    } else {

                                        // If anything wrong with the decode --> we stop here with decode message in the action result.
                                        execution.setResultMessage(new MessageGeneral(MessageGeneralEnum.EXECUTION_FA_DECODE)
                                                .resolveDescription("AREA", "Step")
                                                .resolveDescription("MES", answerDecode.getMessageDescription()));
                                        execution.setEnd(new Date().getTime());
                                        LOG.debug(logPrefix + "TestCase interupted due to decode Error.");

                                        // There was an error on decode so we stop everything.
                                        if (execution.getManualExecution().equals("Y")) {
                                            execute_Next_Step = true;
                                            execute_Step = true;
                                        } else {
                                            execute_Next_Step = false;
                                            execute_Step = false;
                                        }
                                    }
                                } else if (stepExecution.getLoop().equals(TestCaseStep.LOOP_DOWHILECONDITIONFALSE)
                                        || stepExecution.getLoop().equals(TestCaseStep.LOOP_DOWHILECONDITIONTRUE)) {
                                    // First Step execution for LOOP_DOWHILECONDITIONTRUE and LOOP_DOWHILECONDITIONFALSE --> We force the step execution and activate the next step execution.
                                    // We also force the condition message to always true with success.
                                    execute_Step = true;
                                    execute_Next_Step = true;
                                    conditionAnswer.setResultMessage(new MessageEvent(MessageEventEnum.CONDITIONEVAL_TRUE_ALWAYS));
                                } else {
                                    // First Step execution for Unknown Loop --> We force the step execution only once (default behaviour).
                                    execute_Step = true;
                                    execute_Next_Step = false;
                                    conditionAnswer.setResultMessage(new MessageEvent(MessageEventEnum.CONDITIONEVAL_FAILED_UNKNOWNLOOP).resolveDescription("LOOP", stepExecution.getLoop()));
                                }

                                /**
                                 * Execute Step
                                 */
                                LOG.debug(logPrefix + "Executing step : " + stepExecution.getTest() + " - " + stepExecution.getTestCase() + " - Step " + stepExecution.getStepId() + " - Index " + stepExecution.getStepId());

                                if (execute_Step) {

                                    /**
                                     * We execute the step
                                     */
                                    stepExecution = this.executeStep(stepExecution, execution);

                                    /**
                                     * Updating Execution Result Message only if
                                     * execution result message of the step is
                                     * not PE or OK.
                                     */
                                    if ((!(stepExecution.getExecutionResultMessage().equals(new MessageGeneral(MessageGeneralEnum.EXECUTION_PE_TESTSTARTED))))
                                            && (!(stepExecution.getExecutionResultMessage().equals(new MessageGeneral(MessageGeneralEnum.EXECUTION_OK))))) {
                                        execution.setResultMessage(stepExecution.getExecutionResultMessage());
                                    }
                                    if (stepExecution.getStepResultMessage().equals(new MessageEvent(MessageEventEnum.STEP_PENDING))) {
                                        stepExecution.setStepResultMessage(new MessageEvent(MessageEventEnum.STEP_SUCCESS));
                                    }

                                    /**
                                     * We test here is execution is manual and
                                     * operator needs to evaluate the condition
                                     * manually. If this is the case, we add the
                                     * comment inside the description.
                                     */
                                    if (execution.getManualExecution().equals("Y") && testcaseStepConditionEnum.isOperatorEvaluationRequired()) {
                                        stepExecution.setDescription(stepExecution.getDescription() + " - " + conditionAnswer.getMessageDescription());
                                    }

                                    testCaseStepExecutionService.updateTestCaseStepExecution(stepExecution);

                                    if (stepExecution.isStopExecution()) {
                                        break;
                                    }

                                } else if ((!descriptionOrConditionStepDecodeError) && (!conditionStepError)) { // We don't execute the step and record a generic execution.

                                    /**
                                     * Register Step in database
                                     */
                                    LOG.debug(logPrefix + "Registering Step : " + stepExecution.getStepId());

                                    // We change the Step message only if the Step is not executed due to condition.
                                    MessageEvent stepMes = new MessageEvent(MessageEventEnum.CONDITION_TESTCASESTEP_NOTEXECUTED);
                                    stepExecution.setStepResultMessage(stepMes);
                                    stepExecution.setReturnMessage(stepExecution.getReturnMessage()
                                            .replace("%COND%", stepExecution.getConditionOperator())
                                            .replace("%LOOP%", stepExecution.getLoop())
                                            .replace("%MESSAGE%", conditionAnswer.getResultMessage().getDescription())
                                    );

                                    stepExecution.setEnd(new Date().getTime());
                                    this.testCaseStepExecutionService.updateTestCaseStepExecution(stepExecution);
                                    LOG.debug(logPrefix + "Registered Step");

                                } else {
                                    // Not executed because decode error or failed condition.
                                    stepExecution.setEnd(new Date().getTime());
                                    stepExecution.setStopExecution(true);
                                    this.testCaseStepExecutionService.updateTestCaseStepExecution(stepExecution);
                                    LOG.debug(logPrefix + "Registered Step");
                                }

                                /**
                                 * Log TestCaseStepExecution
                                 */
                                if ((execution.getVerbose() > 0) && parameterService.getParameterBooleanByKey("cerberus_executionlog_enable", execution.getSystem(), false)) {
                                    LOG.info(stepExecution.toJson(false, true));
                                }

                                // Websocket --> we refresh the corresponding Detail Execution pages attached to this execution.
                                updateExecutionWebSocketOnly(execution, false);

                                step_index++;
                            } while (execute_Next_Step && step_index <= maxloop);

                            // Step execution boolean is considered for next step execution only if current step was not forced or forced and failed.
                            if (!testCaseStep.isExecutionForced() || stepExecution.isStopExecution()) {
                                doStepStopExecution = stepExecution.isStopExecution();
                            }
                        }
                    }

                    /**
                     * If at that time the execution is still PE, we move it to
                     * OK. It means that no issue were met.
                     */
                    if ((execution.getResultMessage() == null) || (execution.getResultMessage().equals(new MessageGeneral(MessageGeneralEnum.EXECUTION_PE_TESTSTARTED)))) {
                        execution.setResultMessage(new MessageGeneral(MessageGeneralEnum.EXECUTION_OK));
                    }

                    /**
                     * We notify external robot provider of end of execution
                     * status.
                     */
                    switch (execution.getRobotProvider()) {
                        case TestCaseExecution.ROBOTPROVIDER_BROWSERSTACK:
                            browserstackService.setSessionStatus(execution.getSystem(), execution.getRobotSessionID(), execution.getControlStatus(), execution.getControlMessage(), execution.getRobotExecutorObj().getHostUser(), execution.getRobotExecutorObj().getHostPassword());
                            break;
                        case TestCaseExecution.ROBOTPROVIDER_KOBITON:
                            kobitonService.setSessionStatus(execution.getSystem(), execution.getRobotSessionID(), execution.getControlStatus(), execution.getControlMessage(), execution.getRobotExecutorObj().getHostUser(), execution.getRobotExecutorObj().getHostPassword());
                            break;
                        case TestCaseExecution.ROBOTPROVIDER_LAMBDATEST:
                            lambdaTestService.setSessionStatus(execution.getSession(), execution.getControlStatus());
                            // We also set the exeid at that stage.
                            String session1 = lambdaTestService.getTestID(execution.getTagObj().getLambdaTestBuild(), execution.getRobotSessionID(), execution.getRobotExecutorObj().getHostUser(), execution.getRobotExecutorObj().getHostPassword(), execution.getSystem());
                            execution.setRobotProviderSessionID(session1);
                            break;
                    }

                } else { // We don't execute the testcase linked with condition.
                    MessageGeneral mes;
                    /**
                     * Update Execution status from condition
                     */
                    if (conditionAnswerTc.getResultMessage().getMessage().getCodeString().equals("PE")) {
                        mes = new MessageGeneral(MessageGeneralEnum.EXECUTION_NE_CONDITION);
                    } else {
                        mes = new MessageGeneral(MessageGeneralEnum.EXECUTION_FA_CONDITION);
                    }
                    mes.setDescription(mes.getDescription().replace("%COND%", execution.getConditionOperator())
                            .replace("%MES%", conditionAnswerTc.getResultMessage().getDescription()));
                    execution.setResultMessage(mes);
                }
            }
        } catch (CerberusException ex) {
            /**
             * If an exception is found, set the execution to FA and print the
             * exception (only in debug mode)
             */
            MessageGeneral messageFin = new MessageGeneral(MessageGeneralEnum.EXECUTION_FA);
            messageFin.setDescription(messageFin.getDescription() + " " + ex.getMessageError().getDescription());
            execution.setResultMessage(messageFin);
            LOG.debug(logPrefix + "Exception found Executing Test " + execution.getId() + " : " + ex.getMessageError().getDescription());
        } catch (Exception ex) {
            /**
             * If an exception is found, set the execution to FA and print the
             * exception
             */
            MessageGeneral messageFin = new MessageGeneral(MessageGeneralEnum.EXECUTION_FA);
            messageFin.setDescription(messageFin.getDescription() + " " + ex.getMessage());
            execution.setResultMessage(messageFin);
            execution.setControlMessage(execution.getControlMessage() + " Exception: " + ex);
            LOG.error(logPrefix + "Exception found Executing Test " + execution.getId(), ex);
        } finally {

            /**
             * We stop the server session here (selenium for ex.).
             */
            try {
                if (videoRecorder != null) {
                    videoRecorder.endRecordVideo();
                }
                execution = this.stopTestCase(execution);
            } catch (Exception ex) {
                LOG.error(logPrefix + "Exception Stopping Test " + execution.getId() + " Exception : " + ex.toString(), ex);
            }

            /**
             * Log Execution
             */
            if ((execution.getVerbose() > 0) && parameterService.getParameterBooleanByKey("cerberus_executionlog_enable", execution.getSystem(), false)) {
                LOG.info(execution.toJson(false));
            }

            /**
             * Clean memory
             */
            try {
                executionUUID.removeExecutionUUID(execution.getExecutionUUID());
                LOG.debug(logPrefix + "Clean ExecutionUUID");
            } catch (Exception ex) {
                LOG.error(logPrefix + "Exception cleaning Memory: " + ex.toString(), ex);
            }

            /**
             * Credit Limit increase
             */
            sessionCounter.incrementCreditLimitNbExe();
            Long durationinSecond = (execution.getEnd() - execution.getStart()) / 1000;
            if ((durationinSecond > 0) && (durationinSecond <= 1000000)) {
                sessionCounter.incrementCreditLimitSecondExe(durationinSecond.intValue());
            }

            /**
             * Log execution is finished
             */
            LOG.info("Execution Finished : UUID=" + execution.getExecutionUUID()
                    + " ID=" + execution.getId() + " RC=" + execution.getControlStatus() + " "
                    + "TestName=" + execution.getEnvironment() + "." + execution.getCountry() + "."
                    + execution.getBuild() + "." + execution.getRevision() + "." + execution.getTest() + "_"
                    + execution.getTestCase() + "_" + execution.getTestCaseObj().getDescription().replace(".", ""));

            /**
             * Retry management, in case the result is not (OK or NE), we
             * execute the job again reducing the retry to 1.
             */
            boolean willBeRetried = retriesService.manageRetries(execution);

            /**
             * Updating queue to done status only for execution from queue
             */
            if (execution.getQueueID() != 0) {
                executionQueueService.updateToDone(execution.getQueueID(), "", runID);
            }

            /**
             * Trigger the necessary Event for WebHook and notification
             * management.
             */
            eventService.triggerEvent(EventHook.EVENTREFERENCE_EXECUTION_END, execution, null, null, null);
            if (!willBeRetried) {
                eventService.triggerEvent(EventHook.EVENTREFERENCE_EXECUTION_END_LASTRETRY, execution, null, null, null);
            }

            /**
             * After every execution finished, <br>
             * if the execution has a tag that has a campaign associated  <br>
             * and no more executions are in the queue, <br>
             * we trigger : <br>
             * 1/ The update of the EndExeQueue of the tag <br>
             * 2/ We notify the Distribution List with execution report status
             */
            tagService.manageCampaignEndOfExecution(execution.getTag());

            /**
             * Dependency management, At the end of the execution, we RELEASE
             * the corresponding dependencies and put corresponding Queue
             * entries to QUEUED status.
             */
            if (!willBeRetried) {
                testCaseExecutionQueueDepService.manageDependenciesEndOfExecution(execution);
            }

            // After every execution finished we try to trigger more from the queue;-).
            executionThreadPoolService.executeNextInQueueAsynchroneously(false);

        }

        return execution;

    }

    // Update Execution status and eventually push the new value to websocket.
    private void updateExecution(TestCaseExecution execution, boolean forcePush) {
        try {
            testCaseExecutionService.updateTCExecution(execution);
        } catch (CerberusException ex) {
            LOG.warn(ex);
        }

        updateExecutionWebSocketOnly(execution, forcePush);

    }

    private void updateExecutionWebSocketOnly(TestCaseExecution execution, boolean forcePush) {
        // Websocket --> we refresh the corresponding Detail Execution pages attached to this execution.
        if (execution.isCerberus_featureflipping_activatewebsocketpush()) {
            TestCaseExecutionEndPoint.getInstance().send(execution, forcePush);
        }

    }

    @Override
    public TestCaseExecution stopTestCase(TestCaseExecution execution) {

        /**
         * Stop Execution
         */
        LOG.debug(execution.getId() + " - Stop the execution " + execution.getId() + " UUID:" + execution.getExecutionUUID());
        try {
            //TODO:FN debug messages to be removed
            LOG.debug("[DEBUG] STOP " + " ID=" + execution.getId());
            this.stopExecutionRobotAndProxy(execution);
        } catch (Exception ex) {
            LOG.warn("Exception Stopping Execution " + execution.getId() + " Exception :" + ex.toString(), ex);
        }

        /**
         * Saving TestCaseExecution object.
         */
        execution.setEnd(new Date().getTime());

        try {
            testCaseExecutionService.updateTCExecution(execution);
        } catch (CerberusException ex) {
            LOG.warn("Exception updating Execution :" + execution.getId() + " Exception:" + ex.toString());
        }

        // Websocket --> we refresh the corresponding Detail Execution pages attached to this execution.
        if (execution.isCerberus_featureflipping_activatewebsocketpush()) {
            TestCaseExecutionEndPoint.getInstance().send(execution, true);
            TestCaseExecutionEndPoint.getInstance().end(execution);
        }

        return execution;
    }

    private TestCaseStepExecution executeStep(TestCaseStepExecution stepExecution, TestCaseExecution execution) {

        long runID = stepExecution.getId();
        String logPrefix = runID + " - ";
        AnswerItem<String> answerDecode = new AnswerItem<>();

        // Initialise the Step Data List.
        List<TestCaseExecutionData> myStepDataList = new ArrayList<>();
        stepExecution.setTestCaseExecutionDataList(myStepDataList);
        // Initialise the Data List used to enter the action.
        /**
         * Iterate Actions
         */
        List<TestCaseStepAction> testCaseStepActionList = stepExecution.getTestCaseStep().getActions();
        LOG.debug("Getting list of actions of the step. " + testCaseStepActionList.size() + " action(s) to perform.");

        for (TestCaseStepAction testCaseStepAction : testCaseStepActionList) {

            /**
             * Start Execution of TestCaseStepAction
             */
            long startAction = new Date().getTime();
            DateFormat df = new SimpleDateFormat(DateUtil.DATE_FORMAT_TIMESTAMP);
            long startLongAction = Long.valueOf(df.format(startAction));

            /**
             * Create and Register TestCaseStepActionExecution.
             */
            TestCaseStepActionExecution testCaseStepActionExecution = factoryTestCaseStepActionExecution.create(
                    stepExecution.getId(), testCaseStepAction.getTest(), testCaseStepAction.getTestcase(),
                    testCaseStepAction.getStepId(), stepExecution.getIndex(), testCaseStepAction.getActionId(), testCaseStepAction.getSort(), null, null,
                    testCaseStepAction.getConditionOperator(), testCaseStepAction.getConditionValue1(), testCaseStepAction.getConditionValue2(), testCaseStepAction.getConditionValue3(),
                    testCaseStepAction.getConditionValue1(), testCaseStepAction.getConditionValue2(), testCaseStepAction.getConditionValue3(),
                    testCaseStepAction.getAction(), testCaseStepAction.getValue1(), testCaseStepAction.getValue2(), testCaseStepAction.getValue3(), testCaseStepAction.getValue1(),
                    testCaseStepAction.getValue2(), testCaseStepAction.getValue3(),
                    (testCaseStepAction.isFatal() ? "Y" : "N"), startAction, startAction, startLongAction, startLongAction, new MessageEvent(MessageEventEnum.ACTION_PENDING),
                    testCaseStepAction.getDescription(), testCaseStepAction, stepExecution);
            testCaseStepActionExecution.setOptions(testCaseStepAction.getOptionsActive());
            testCaseStepActionExecution.setConditionOptions(testCaseStepAction.getConditionOptionsActive());

            this.testCaseStepActionExecutionService.insertTestCaseStepActionExecution(testCaseStepActionExecution);

            /**
             * We populate the TestCase Action List
             */
            stepExecution.addTestCaseStepActionExecutionList(testCaseStepActionExecution);

            /**
             * If execution is not manual, evaluate the condition at the action
             * level
             */
            AnswerItem<Boolean> conditionAnswer;
            boolean conditionDecodeError = false;
            if (!execution.getManualExecution().equals("Y")) {

                try {
                    answerDecode = variableService.decodeStringCompletly(testCaseStepActionExecution.getConditionVal1(), execution, null, false);
                    testCaseStepActionExecution.setConditionVal1(answerDecode.getItem());

                    if (!(answerDecode.isCodeStringEquals("OK"))) {
                        // If anything wrong with the decode --> we stop here with decode message in the action result.
                        testCaseStepActionExecution.setActionResultMessage(answerDecode.getResultMessage().resolveDescription("FIELD", "Action Condition Value1"));
                        testCaseStepActionExecution.setExecutionResultMessage(new MessageGeneral(answerDecode.getResultMessage().getMessage()));
                        testCaseStepActionExecution.setStopExecution(answerDecode.getResultMessage().isStopTest());
                        testCaseStepActionExecution.setEnd(new Date().getTime());
                        LOG.debug("Action interupted due to decode 'Action Condition Value1' Error.");
                        conditionDecodeError = true;
                    }
                } catch (CerberusEventException cex) {
                    LOG.warn(cex);
                }

                try {
                    answerDecode = variableService.decodeStringCompletly(testCaseStepActionExecution.getConditionVal2(), execution, null, false);
                    testCaseStepActionExecution.setConditionVal2(answerDecode.getItem());

                    if (!(answerDecode.isCodeStringEquals("OK"))) {
                        // If anything wrong with the decode --> we stop here with decode message in the action result.
                        testCaseStepActionExecution.setActionResultMessage(answerDecode.getResultMessage().resolveDescription("FIELD", "Action Condition Value2"));
                        testCaseStepActionExecution.setExecutionResultMessage(new MessageGeneral(answerDecode.getResultMessage().getMessage()));
                        testCaseStepActionExecution.setStopExecution(answerDecode.getResultMessage().isStopTest());
                        testCaseStepActionExecution.setEnd(new Date().getTime());
                        LOG.debug("Action interupted due to decode 'Action Condition Value2' Error.");
                        conditionDecodeError = true;
                    }
                } catch (CerberusEventException cex) {
                    LOG.warn(cex);
                }
                try {
                    answerDecode = variableService.decodeStringCompletly(testCaseStepActionExecution.getConditionVal3(), execution, null, false);
                    testCaseStepActionExecution.setConditionVal3(answerDecode.getItem());

                    if (!(answerDecode.isCodeStringEquals("OK"))) {
                        // If anything wrong with the decode --> we stop here with decode message in the action result.
                        testCaseStepActionExecution.setActionResultMessage(answerDecode.getResultMessage().resolveDescription("FIELD", "Action Condition Value3"));
                        testCaseStepActionExecution.setExecutionResultMessage(new MessageGeneral(answerDecode.getResultMessage().getMessage()));
                        testCaseStepActionExecution.setStopExecution(answerDecode.getResultMessage().isStopTest());
                        testCaseStepActionExecution.setEnd(new Date().getTime());
                        LOG.debug("Action interupted due to decode 'Action Condition Value3' Error.");
                        conditionDecodeError = true;
                    }
                } catch (CerberusEventException cex) {
                    LOG.warn(cex);
                }
            }

            if (!(conditionDecodeError)) {

                ConditionOperatorEnum actionConditionOperatorEnum = ConditionOperatorEnum.getConditionOperatorEnumFromString(testCaseStepActionExecution.getConditionOperator());

                conditionAnswer = this.conditionService.evaluateCondition(testCaseStepActionExecution.getConditionOperator(),
                        testCaseStepActionExecution.getConditionVal1(), testCaseStepActionExecution.getConditionVal2(), testCaseStepActionExecution.getConditionVal3(),
                        execution, testCaseStepActionExecution.getConditionOptions());
                boolean execute_Action = conditionAnswer.getItem();

                if (execution.getManualExecution().equals("Y") && actionConditionOperatorEnum.isOperatorEvaluationRequired()) {
                    testCaseStepActionExecution.setDescription(testCaseStepActionExecution.getDescription() + " - " + conditionAnswer.getMessageDescription());
                }

                /**
                 * If condition OK or if manual execution, then execute the
                 * action
                 */
                if (conditionAnswer.getResultMessage().getMessage().getCodeString().equals("PE")
                        || execution.getManualExecution().equals("Y")) {

                    // Execute or not the action here.
                    if (execute_Action || execution.getManualExecution().equals("Y")) {
                        LOG.debug("Executing action : " + testCaseStepActionExecution.getAction() + " with val1 : " + testCaseStepActionExecution.getValue1()
                                + " and val2 : " + testCaseStepActionExecution.getValue2()
                                + " and val3 : " + testCaseStepActionExecution.getValue3());

                        /**
                         * We execute the Action
                         */
                        testCaseStepActionExecution = this.executeAction(testCaseStepActionExecution, execution);
                        /**
                         * If Action or property reported to stop the testcase,
                         * we stop it and update the step with the message.
                         */
                        stepExecution.setStopExecution(testCaseStepActionExecution.isStopExecution());
                        if ((!(testCaseStepActionExecution.getExecutionResultMessage().equals(new MessageGeneral(MessageGeneralEnum.EXECUTION_OK))))
                                && (!(testCaseStepActionExecution.getExecutionResultMessage().equals(new MessageGeneral(MessageGeneralEnum.EXECUTION_PE_TESTEXECUTING))))) {
                            stepExecution.setExecutionResultMessage(testCaseStepActionExecution.getExecutionResultMessage());
                            stepExecution.setStepResultMessage(testCaseStepActionExecution.getActionResultMessage());
                        }

                        if (testCaseStepActionExecution.isStopExecution()) {
                            break;
                        }

                    } else { // We don't execute the action and record a generic execution.

                        /**
                         * Record Screenshot, PageSource
                         */
                        testCaseStepActionExecution.addFileList(recorderService.recordExecutionInformationAfterStepActionandControl(testCaseStepActionExecution, null));

                        LOG.debug("Registering Action : " + testCaseStepActionExecution.getAction());

                        // We change the Action message only if the action is not executed due to condition.
                        MessageEvent actionMes = new MessageEvent(MessageEventEnum.CONDITION_TESTCASEACTION_NOTEXECUTED);
                        testCaseStepActionExecution.setActionResultMessage(actionMes);
                        testCaseStepActionExecution.setReturnMessage(testCaseStepActionExecution.getReturnMessage()
                                .replace("%COND%", testCaseStepActionExecution.getConditionOperator())
                                .replace("%MESSAGE%", conditionAnswer.getResultMessage().getDescription())
                        );

                        testCaseStepActionExecution.setEnd(new Date().getTime());
                        this.testCaseStepActionExecutionService.updateTestCaseStepActionExecution(testCaseStepActionExecution);
                        LOG.debug("Registered Action");

                    }
                } else {
                    // Error when performing the condition evaluation. We force no execution (false)
                    MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.EXECUTION_FA_CONDITION);
                    mes.setDescription(mes.getDescription()
                            .replace("%COND%", testCaseStepActionExecution.getConditionOperator())
                            .replace("%AREA%", "action ")
                            .replace("%MES%", conditionAnswer.getResultMessage().getDescription()));
                    testCaseStepActionExecution.setExecutionResultMessage(mes);
                    stepExecution.setExecutionResultMessage(testCaseStepActionExecution.getExecutionResultMessage());

                    testCaseStepActionExecution.setActionResultMessage(new MessageEvent(MessageEventEnum.CONDITION_TESTCASEACTION_FAILED)
                            .resolveDescription("AREA", "")
                            .resolveDescription("COND", testCaseStepActionExecution.getConditionOperator())
                            .resolveDescription("MESSAGE", conditionAnswer.getResultMessage().getDescription()));

                    stepExecution.setStepResultMessage(new MessageEvent(MessageEventEnum.CONDITION_TESTCASESTEP_FAILED)
                            .resolveDescription("AREA", "action ")
                            .resolveDescription("COND", testCaseStepActionExecution.getConditionOperator())
                            .resolveDescription("MESSAGE", conditionAnswer.getResultMessage().getDescription()));
                    if (testCaseStepActionExecution.isFatal().equals("N")) {
                        testCaseStepActionExecution.setStopExecution(false);
                        MessageEvent actionMes = testCaseStepActionExecution.getActionResultMessage();
                        actionMes.setDescription(testCaseStepActionExecution.getActionResultMessage().getDescription() + " -- Execution forced to continue.");
                        testCaseStepActionExecution.setActionResultMessage(actionMes);
                    } else {
                        testCaseStepActionExecution.setStopExecution(true);
                    }

                    stepExecution.setStopExecution(testCaseStepActionExecution.isStopExecution());

                    testCaseStepActionExecution.setEnd(new Date().getTime());

                    this.testCaseStepActionExecutionService.updateTestCaseStepActionExecution(testCaseStepActionExecution);
                    LOG.debug("Action interupted due to condition error.");
                    // We stop any further Action execution.
                    if (testCaseStepActionExecution.isStopExecution()) {
                        break;
                    }
                }
            } else {

                testCaseStepActionExecution.setEnd(new Date().getTime());
                stepExecution.setExecutionResultMessage(testCaseStepActionExecution.getExecutionResultMessage());
                stepExecution.setStepResultMessage(testCaseStepActionExecution.getActionResultMessage());
                stepExecution.setStopExecution(testCaseStepActionExecution.isStopExecution());
                this.testCaseStepActionExecutionService.updateTestCaseStepActionExecution(testCaseStepActionExecution);
                LOG.debug("Registered Action");
                if (testCaseStepActionExecution.isStopExecution()) {
                    break;
                }

            }

            /**
             * Log TestCaseStepActionExecution
             */
            if ((execution.getVerbose() > 0) && parameterService.getParameterBooleanByKey("cerberus_executionlog_enable", execution.getSystem(), false)) {
                LOG.info(testCaseStepActionExecution.toJson(false, true));
            }

        }
        stepExecution.setEnd(new Date().getTime());

        this.testCaseStepExecutionService.updateTestCaseStepExecution(stepExecution);

        updateExecutionWebSocketOnly(execution, false);

        return stepExecution;
    }

    private TestCaseStepActionExecution executeAction(TestCaseStepActionExecution actionExecution, TestCaseExecution execution) {

        LOG.debug("Starting execute Action : " + actionExecution.getAction());
        AnswerItem<String> answerDecode = new AnswerItem<>();

        /**
         * If execution is not manual, do action and record files
         */
        if (!execution.getManualExecution().equals("Y")) {
            actionExecution = this.actionService.doAction(actionExecution);

            /**
             * Record Screenshot, PageSource
             */
            try {
                actionExecution.addFileList(recorderService.recordExecutionInformationAfterStepActionandControl(actionExecution, null));
            } catch (Exception ex) {
                LOG.warn("Unable to record Screenshot/PageSource : " + ex.toString(), ex);
            }

        } else {
            /**
             * If execution manual, set Action result message as notExecuted
             */
            actionExecution.setActionResultMessage(new MessageEvent(MessageEventEnum.ACTION_WAITINGFORMANUALEXECUTION));
            actionExecution.setExecutionResultMessage(new MessageGeneral(MessageGeneralEnum.EXECUTION_WE));
            actionExecution.setEnd(new Date().getTime());
        }

        /**
         * Register Action in database
         */
        LOG.debug("Registering Action : " + actionExecution.getAction());

        this.testCaseStepActionExecutionService.updateTestCaseStepActionExecution(actionExecution);
        LOG.debug("Registered Action");

        if (actionExecution.isStopExecution()) {
            return actionExecution;
        }

        // If Action setXXContent is not executed, we don't execute the corresponding controls.
        if (actionExecution.getActionResultMessage().getCodeString().equals("NE")
                && (actionExecution.getAction().equals(TestCaseStepAction.ACTION_SETNETWORKTRAFFICCONTENT)
                || actionExecution.getAction().equals(TestCaseStepAction.ACTION_SETSERVICECALLCONTENT)
                || actionExecution.getAction().equals(TestCaseStepAction.ACTION_SETCONSOLECONTENT)
                || actionExecution.getAction().equals(TestCaseStepAction.ACTION_SETCONTENT))) {
            return actionExecution;
        }
        //As controls are associated with an action, the current state for the action is stored in order to restore it
        //if some property is not defined for the country
        MessageEvent actionMessage = actionExecution.getActionResultMessage();
        MessageGeneral excutionResultMessage = actionExecution.getExecutionResultMessage();
        /**
         * Iterate Control
         */
        List<TestCaseStepActionControl> tcsacList = actionExecution.getTestCaseStepAction().getControls();
        for (TestCaseStepActionControl control : tcsacList) {

            /**
             * Start Execution of TestCAseStepActionControl
             */
            long startControl = new Date().getTime();
            DateFormat df = new SimpleDateFormat(DateUtil.DATE_FORMAT_TIMESTAMP);
            long startLongControl = Long.valueOf(df.format(startControl));

            /**
             * Create and Register TestCaseStepActionControlExecution
             */
            LOG.debug("Creating TestCaseStepActionControlExecution");
            TestCaseStepActionControlExecution controlExe
                    = factoryTestCaseStepActionControlExecution.create(actionExecution.getId(), control.getTest(), control.getTestcase(),
                            control.getStepId(), actionExecution.getIndex(), control.getActionId(), control.getControlId(), control.getSort(),
                            null, null,
                            control.getConditionOperator(), control.getConditionValue1(), control.getConditionValue2(), control.getConditionValue3(), control.getConditionValue1(), control.getConditionValue2(), control.getConditionValue3(),
                            control.getControl(), control.getValue1(), control.getValue2(), control.getValue3(), control.getValue1(), control.getValue2(),
                            control.getValue3(), (control.isFatal() ? "Y" : "N"), startControl, startControl, startLongControl, startLongControl,
                            control.getDescription(), actionExecution, new MessageEvent(MessageEventEnum.CONTROL_PENDING));
            controlExe.setConditionOptions(control.getConditionOptionsActive());
            controlExe.setOptions(control.getOptionsActive());

            this.testCaseStepActionControlExecutionService.insertTestCaseStepActionControlExecution(controlExe);

            LOG.debug("Executing control : " + controlExe.getControlId() + " type : " + controlExe.getControl());

            /**
             * We populate the TestCase Control List
             */
            actionExecution.addTestCaseStepActionExecutionList(controlExe);

            // Evaluate the condition at the control level.
            AnswerItem<Boolean> conditionAnswer;
            boolean conditionDecodeError = false;
            if (!execution.getManualExecution().equals("Y")) {
                try {
                    answerDecode = variableService.decodeStringCompletly(controlExe.getConditionVal1(), execution, null, false);
                    controlExe.setConditionVal1(answerDecode.getItem());

                    if (!(answerDecode.isCodeStringEquals("OK"))) {
                        // If anything wrong with the decode --> we stop here with decode message in the action result.
                        controlExe.setControlResultMessage(answerDecode.getResultMessage().resolveDescription("FIELD", "Control Condition Value1"));
                        controlExe.setExecutionResultMessage(new MessageGeneral(answerDecode.getResultMessage().getMessage()));
                        controlExe.setStopExecution(answerDecode.getResultMessage().isStopTest());
                        controlExe.setEnd(new Date().getTime());
                        LOG.debug("Control interupted due to decode 'Control Condition Value1' Error.");
                        conditionDecodeError = true;
                    }

                } catch (CerberusEventException cex) {
                    LOG.warn(cex);
                }
                try {
                    answerDecode = variableService.decodeStringCompletly(controlExe.getConditionVal2(), execution, null, false);
                    controlExe.setConditionVal2(answerDecode.getItem());

                    if (!(answerDecode.isCodeStringEquals("OK"))) {
                        // If anything wrong with the decode --> we stop here with decode message in the action result.
                        controlExe.setControlResultMessage(answerDecode.getResultMessage().resolveDescription("FIELD", "Control Condition Value2"));
                        controlExe.setExecutionResultMessage(new MessageGeneral(answerDecode.getResultMessage().getMessage()));
                        controlExe.setStopExecution(answerDecode.getResultMessage().isStopTest());
                        controlExe.setEnd(new Date().getTime());
                        LOG.debug("Control interupted due to decode 'Control Condition Value2' Error.");
                        conditionDecodeError = true;
                    }
                } catch (CerberusEventException cex) {
                    LOG.warn(cex);
                }
                try {
                    answerDecode = variableService.decodeStringCompletly(controlExe.getConditionVal3(), execution, null, false);
                    controlExe.setConditionVal3(answerDecode.getItem());

                    if (!(answerDecode.isCodeStringEquals("OK"))) {
                        // If anything wrong with the decode --> we stop here with decode message in the action result.
                        controlExe.setControlResultMessage(answerDecode.getResultMessage().resolveDescription("FIELD", "Control Condition Value3"));
                        controlExe.setExecutionResultMessage(new MessageGeneral(answerDecode.getResultMessage().getMessage()));
                        controlExe.setStopExecution(answerDecode.getResultMessage().isStopTest());
                        controlExe.setEnd(new Date().getTime());
                        LOG.debug("Control interupted due to decode 'Control Condition Value3' Error.");
                        conditionDecodeError = true;
                    }
                } catch (CerberusEventException cex) {
                    LOG.warn(cex);
                }
            }

            if (!(conditionDecodeError)) {

                ConditionOperatorEnum controlConditionOperatorEnum = ConditionOperatorEnum.getConditionOperatorEnumFromString(controlExe.getConditionOperator());

                conditionAnswer = this.conditionService.evaluateCondition(controlExe.getConditionOperator(),
                        controlExe.getConditionVal1(), controlExe.getConditionVal2(), controlExe.getConditionVal3(),
                        execution, controlExe.getConditionOptions());

                boolean execute_Control = conditionAnswer.getItem();

                if (execution.getManualExecution().equals("Y") && controlConditionOperatorEnum.isOperatorEvaluationRequired()) {
                    controlExe.setDescription(controlExe.getDescription() + " - " + conditionAnswer.getMessageDescription());
                }
                /**
                 * If condition OK or if manual execution, then execute the
                 * control
                 */
                if (conditionAnswer.getResultMessage().getMessage().getCodeString().equals("PE")
                        || execution.getManualExecution().equals("Y")) {

                    if (execute_Control || execution.getManualExecution().equals("Y")) {

                        /**
                         * We execute the control
                         */
                        controlExe = executeControl(controlExe, execution);

                        /**
                         * We update the Action with the execution message and
                         * stop flag from the control. We update the status only
                         * if the control is not OK. This is to prevent moving
                         * the status to OK when it should stay KO when a
                         * control failed previously.
                         */
                        actionExecution.setStopExecution(controlExe.isStopExecution());
                        if (!(controlExe.getControlResultMessage().equals(new MessageEvent(MessageEventEnum.CONTROL_SUCCESS)))) {
                            //NA is a special case of not having success while calculating the property; the action shouldn't be stopped
                            if (controlExe.getControlResultMessage().equals(new MessageEvent(MessageEventEnum.PROPERTY_FAILED_NO_PROPERTY_DEFINITION))) {
                                //restores the messages information if the property is not defined for the country
                                actionExecution.setActionResultMessage(actionMessage);
                                actionExecution.setExecutionResultMessage(excutionResultMessage);
                            } else {
                                actionExecution.setExecutionResultMessage(controlExe.getExecutionResultMessage());
                                actionExecution.setActionResultMessage(controlExe.getControlResultMessage());
                            }
                        }
                        /**
                         * If Control reported to stop the testcase, we stop it.
                         */
                        if (controlExe.isStopExecution()) {
                            break;
                        }

                    } else { // We don't execute the control and record a generic execution.

                        /**
                         * Record Screenshot, PageSource
                         */
                        controlExe.addFileList(recorderService.recordExecutionInformationAfterStepActionandControl(controlExe.getTestCaseStepActionExecution(), controlExe));

                        /**
                         * Register Control in database
                         */
                        LOG.debug("Registering Control : " + controlExe.getControlId());

                        // We change the Action message only if the action is not executed due to condition.
                        MessageEvent controlMes = new MessageEvent(MessageEventEnum.CONDITION_TESTCASECONTROL_NOTEXECUTED);
                        controlExe.setControlResultMessage(controlMes);
                        controlExe.setReturnMessage(controlExe.getReturnMessage()
                                .replace("%COND%", controlExe.getConditionOperator())
                                .replace("%MESSAGE%", conditionAnswer.getResultMessage().getDescription())
                        );

                        controlExe.setEnd(new Date().getTime());
                        this.testCaseStepActionControlExecutionService.updateTestCaseStepActionControlExecution(controlExe);
                        LOG.debug("Registered Control");

                        // Websocket --> we refresh the corresponding Detail Execution pages attached to this execution.
                        updateExecutionWebSocketOnly(execution, false);

                    }
                } else {
                    // Error when performing the condition evaluation. We force no execution (false)
                    MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.EXECUTION_FA_CONDITION);
                    mes.setDescription(mes.getDescription()
                            .replace("%COND%", controlExe.getConditionOperator())
                            .replace("%AREA%", "control ")
                            .replace("%MES%", conditionAnswer.getResultMessage().getDescription()));
                    controlExe.setExecutionResultMessage(mes);
                    actionExecution.setExecutionResultMessage(mes);

                    controlExe.setControlResultMessage(new MessageEvent(MessageEventEnum.CONDITION_TESTCASECONTROL_FAILED)
                            .resolveDescription("AREA", "")
                            .resolveDescription("COND", controlExe.getConditionOperator())
                            .resolveDescription("MESSAGE", conditionAnswer.getResultMessage().getDescription()));

                    actionExecution.setActionResultMessage(new MessageEvent(MessageEventEnum.CONDITION_TESTCASEACTION_FAILED)
                            .resolveDescription("AREA", "control ")
                            .resolveDescription("COND", controlExe.getConditionOperator())
                            .resolveDescription("MESSAGE", conditionAnswer.getResultMessage().getDescription()));

                    controlExe.setEnd(new Date().getTime());

                    this.testCaseStepActionControlExecutionService.updateTestCaseStepActionControlExecution(controlExe);
                    LOG.debug("Control interupted due to condition error.");
                    // We stop any further Control execution.
                    break;
                }
            } else {

                controlExe.setEnd(new Date().getTime());
                actionExecution.setExecutionResultMessage(controlExe.getExecutionResultMessage());
                actionExecution.setActionResultMessage(controlExe.getControlResultMessage());
                this.testCaseStepActionControlExecutionService.updateTestCaseStepActionControlExecution(controlExe);
                LOG.debug("Registered Control");

                // Websocket --> we refresh the corresponding Detail Execution pages attached to this execution.
                updateExecutionWebSocketOnly(execution, false);
            }

            /**
             * Log TestCaseStepActionControlExecution
             */
            if ((execution.getVerbose() > 0) && parameterService.getParameterBooleanByKey("cerberus_executionlog_enable", execution.getSystem(), false)) {
                LOG.info(controlExe.toJson(false, true));
            }

        }

        /**
         *
         * All controls of the actions are done. We now put back the
         * AppTypeEngine value to the one from the application. and also put
         * back the last service called content and format.
         *
         */
        execution.setAppTypeEngine(execution.getApplicationObj().getType());
        if (execution.getLastServiceCalled() != null) {
            execution.getLastServiceCalled().setResponseHTTPBody(execution.getOriginalLastServiceCalled());
            execution.getLastServiceCalled().setResponseHTTPBodyContentType(execution.getOriginalLastServiceCalledContent());
            execution.getLastServiceCalled().setRecordTraceFile(true);
        }

        // Websocket --> we refresh the corresponding Detail Execution pages attached to this execution.
        updateExecutionWebSocketOnly(execution, false);

        LOG.debug("Finished execute Action : " + actionExecution.getAction());
        return actionExecution;

    }

    private TestCaseStepActionControlExecution executeControl(TestCaseStepActionControlExecution controlExecution, TestCaseExecution execution) {

        /**
         * If execution is not manual, do control and record files
         */
        if (!execution.getManualExecution().equals("Y")) {
            controlExecution = this.controlService.doControl(controlExecution);

            /**
             * Record Screenshot, PageSource
             */
            controlExecution.addFileList(recorderService.recordExecutionInformationAfterStepActionandControl(controlExecution.getTestCaseStepActionExecution(), controlExecution));
        } else {
            /**
             * If execution manual, set Control result message as notExecuted
             */
            controlExecution.setControlResultMessage(new MessageEvent(MessageEventEnum.CONTROL_WAITINGEXECUTION));
            controlExecution.setExecutionResultMessage(new MessageGeneral(MessageGeneralEnum.EXECUTION_WE));
            controlExecution.setEnd(new Date().getTime());
        }

        /**
         * Register Control in database
         */
        LOG.debug("Registering Control : " + controlExecution.getControlId());
        this.testCaseStepActionControlExecutionService.updateTestCaseStepActionControlExecution(controlExecution);
        LOG.debug("Registered Control");

        // Websocket --> we refresh the corresponding Detail Execution pages attached to this execution.
        updateExecutionWebSocketOnly(execution, false);

        return controlExecution;
    }

    private TestCaseExecution stopExecutionRobotAndProxy(TestCaseExecution execution) {

        switch (execution.getApplicationObj().getType()) {
            case Application.TYPE_GUI:
            case Application.TYPE_APK:
            case Application.TYPE_IPA:
                try {
                this.robotServerService.stopServer(execution);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Stop server for execution " + execution.getId());
                }
            } catch (WebDriverException exception) {
                LOG.warn("Selenium/Appium didn't manage to close connection for execution " + execution.getId(), exception);
            }
            break;
            case Application.TYPE_FAT:
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Stop Sikuli server for execution " + execution.getId() + " closing application " + execution.getCountryEnvironmentParameters().getIp());
                }
                if (!StringUtil.isNullOrEmpty(execution.getCountryEnvironmentParameters().getIp())) {
                    this.sikuliService.doSikuliActionCloseApp(execution.getSession(), execution.getCountryEnvironmentParameters().getIp());
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Ask Sikuli to clean execution " + execution.getId());
                }
                this.sikuliService.doSikuliEndExecution(execution.getSession());
                break;
            default:
        }

        /**
         * Stopping remote proxy.
         */
        try {
            this.executorService.stopRemoteProxy(execution);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Stop Cerberus Executor Proxy for execution " + execution.getId());
            }
        } catch (Exception exception) {
            LOG.warn("Exception on Cerberus Executor Proxy stop for execution " + execution.getId(), exception);
        }

        // Websocket --> we refresh the corresponding Detail Execution pages attached to this execution.
        updateExecutionWebSocketOnly(execution, false);

        return execution;
    }

    @Override
    @Async
    public TestCaseExecution executeTestCaseAsynchronously(TestCaseExecution execution) throws CerberusException {
        try {
            return executeTestCase(execution);
        } catch (CerberusException ex) {
            throw new CerberusException(ex.getMessageError());
        }
    }

}
