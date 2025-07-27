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

import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.entity.*;
import org.cerberus.core.crud.factory.IFactoryRobotCapability;
import org.cerberus.core.crud.factory.IFactoryTestCaseExecutionSysVer;
import org.cerberus.core.crud.factory.IFactoryTestCaseStepActionControlExecution;
import org.cerberus.core.crud.factory.IFactoryTestCaseStepActionExecution;
import org.cerberus.core.crud.factory.IFactoryTestCaseStepExecution;
import org.cerberus.core.crud.service.ICountryEnvLinkService;
import org.cerberus.core.crud.service.ICountryEnvParamService;
import org.cerberus.core.crud.service.ILoadTestCaseService;
import org.cerberus.core.crud.service.IParameterService;
import org.cerberus.core.crud.service.ITagService;
import org.cerberus.core.crud.service.ITestCaseCountryPropertiesService;
import org.cerberus.core.crud.service.ITestCaseExecutionDataService;
import org.cerberus.core.crud.service.ITestCaseExecutionQueueDepService;
import org.cerberus.core.crud.service.ITestCaseExecutionQueueService;
import org.cerberus.core.crud.service.ITestCaseExecutionService;
import org.cerberus.core.crud.service.ITestCaseExecutionSysVerService;
import org.cerberus.core.crud.service.ITestCaseService;
import org.cerberus.core.crud.service.ITestCaseStepActionControlExecutionService;
import org.cerberus.core.crud.service.ITestCaseStepActionExecutionService;
import org.cerberus.core.crud.service.ITestCaseStepExecutionService;
import org.cerberus.core.engine.entity.ExecutionUUID;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.engine.entity.MessageGeneral;
import org.cerberus.core.engine.execution.IConditionService;
import org.cerberus.core.engine.execution.IExecutionRunService;
import org.cerberus.core.engine.execution.IRecorderService;
import org.cerberus.core.engine.execution.IRetriesService;
import org.cerberus.core.engine.execution.IRobotServerService;
import org.cerberus.core.engine.execution.enums.ConditionOperatorEnum;
import org.cerberus.core.engine.execution.video.VideoRecorder;
import org.cerberus.core.engine.gwt.IActionService;
import org.cerberus.core.engine.gwt.IControlService;
import org.cerberus.core.engine.gwt.IVariableService;
import org.cerberus.core.engine.queuemanagement.IExecutionThreadPoolService;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.enums.MessageGeneralEnum;
import org.cerberus.core.enums.Video;
import org.cerberus.core.event.IEventService;
import org.cerberus.core.exception.CerberusEventException;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.service.kafka.IKafkaService;
import org.cerberus.core.service.robotproviders.IBrowserstackService;
import org.cerberus.core.service.robotproviders.IKobitonService;
import org.cerberus.core.service.robotproviders.ILambdaTestService;
import org.cerberus.core.service.robotextension.ISikuliService;
import org.cerberus.core.session.SessionCounter;
import org.cerberus.core.util.DateUtil;
import org.cerberus.core.util.StringUtil;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.websocket.TestCaseExecutionWebSocket;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriverException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.cerberus.core.engine.entity.ExecutionLog;
import org.cerberus.core.engine.entity.Identifier;
import org.cerberus.core.engine.execution.IIdentifierService;
import org.cerberus.core.service.bug.IBugService;
import org.cerberus.core.service.robotextension.impl.SikuliService;
import org.cerberus.core.service.xray.IXRayService;
import org.cerberus.core.service.robotproxy.IRobotProxyService;
import org.cerberus.core.websocket.ExecutionMonitor;
import org.cerberus.core.websocket.ExecutionMonitorWebSocket;

/**
 * @author bcivel
 */
@AllArgsConstructor
@EnableAsync
@Service
public class ExecutionRunService implements IExecutionRunService {

    private static final Logger LOG = LogManager.getLogger(ExecutionRunService.class);

    @Autowired
    TestCaseExecutionWebSocket testCaseExecutionWebSocket;

    @Autowired
    ExecutionMonitor executionMonitor;

    @Autowired
    ExecutionMonitorWebSocket executionMonitorWebSocket;

    private ISikuliService sikuliService;
    private IRobotServerService robotServerService;
    private IActionService actionService;
    private IControlService controlService;
    private IConditionService conditionService;
    private ITestCaseService testCaseService;
    private ITestCaseStepExecutionService testCaseStepExecutionService;
    private ITestCaseStepActionExecutionService testCaseStepActionExecutionService;
    private ITestCaseStepActionControlExecutionService testCaseStepActionControlExecutionService;
    private ITestCaseExecutionService testCaseExecutionService;
    private ITestCaseExecutionSysVerService testCaseExecutionSysVerService;
    private ICountryEnvLinkService countryEnvLinkService;
    private ITestCaseExecutionQueueService executionQueueService;
    private IExecutionThreadPoolService executionThreadPoolService;
    private ITestCaseCountryPropertiesService testCaseCountryPropertiesService;
    private ICountryEnvParamService countryEnvParamService;
    private ILoadTestCaseService loadTestCaseService;
    private IFactoryTestCaseStepExecution factoryTestCaseStepExecution;
    private IFactoryTestCaseStepActionExecution factoryTestCaseStepActionExecution;
    private IFactoryTestCaseStepActionControlExecution factoryTestCaseStepActionControlExecution;
    private IFactoryTestCaseExecutionSysVer factoryTestCaseExecutionSysVer;
    private ExecutionUUID executionUUID;
    private SessionCounter sessionCounter;
    private IRecorderService recorderService;
    private IVariableService variableService;
    private IParameterService parameterService;
    private ITagService tagService;
    private IRetriesService retriesService;
    private IFactoryRobotCapability robotCapabilityFactory;
    private ITestCaseExecutionQueueDepService testCaseExecutionQueueDepService;
    private ITestCaseExecutionDataService testCaseExecutionDataService;
    private IBrowserstackService browserstackService;
    private IKobitonService kobitonService;
    private ILambdaTestService lambdaTestService;
    private IKafkaService kafkaService;
    private IRobotProxyService executorService;
    private IEventService eventService;
    private IXRayService xRayService;
    private IBugService bugService;
    private IIdentifierService identifierService;

    @Override
    public TestCaseExecution executeTestCase(TestCaseExecution execution) throws CerberusException {
        long runID = execution.getId();
        String logPrefix = runID + " - ";

        VideoRecorder videoRecorder = null;
        // Feeding Build Rev of main Application system to testcaseexecutionsysver table. Only if execution is not manual.
        try {

            AnswerItem<String> answerDecode = new AnswerItem<>();

            if (execution.getManualURL() < 1) {
                // Insert SystemVersion in Database
                TestCaseExecutionSysVer myExeSysVer;
                try {
                    LOG.debug("{}Registering Main System Version.", logPrefix);
                    myExeSysVer = factoryTestCaseExecutionSysVer.create(runID, execution.getApplicationObj().getSystem(), execution.getBuild(), execution.getRevision());
                    testCaseExecutionSysVerService.insertTestCaseExecutionSysVer(myExeSysVer);
                } catch (CerberusException ex) {
                    LOG.error("{}{}", logPrefix, ex.getMessage(), ex);
                }
                LOG.debug("{}Main System Version Registered.", logPrefix);

                /*
                 * For all Linked environment, we also keep track on the
                 * build/rev information inside testcaseexecutionsysver table.
                 */
                LOG.debug("{}Registering Linked System Version.", logPrefix);
                try {
                    List<CountryEnvLink> ceLink;
                    ceLink = countryEnvLinkService.convert(countryEnvLinkService.readByVarious(execution.getApplicationObj().getSystem(), execution.getCountry(), execution.getEnvironment()));
                    for (CountryEnvLink myCeLink : ceLink) {
                        LOG.debug("{}Linked environment found : {} {} {}", logPrefix, myCeLink.getSystemLink(), myCeLink.getCountryLink(), myCeLink.getEnvironmentLink());

                        CountryEnvParam myCountryEnvParam;
                        try {
                            myCountryEnvParam = this.countryEnvParamService.convert(this.countryEnvParamService.readByKey(myCeLink.getSystemLink(), myCeLink.getCountryLink(), myCeLink.getEnvironmentLink()));
                            myExeSysVer = factoryTestCaseExecutionSysVer.create(runID, myCeLink.getSystemLink(), myCountryEnvParam.getBuild(), myCountryEnvParam.getRevision());
                            testCaseExecutionSysVerService.insertTestCaseExecutionSysVer(myExeSysVer);
                        } catch (CerberusException ex) {
                            // Referencial Integrity link between countryEnvLink and CountryEnvParam table should secure that exception to never happen.
                            LOG.error("{}{}", logPrefix, ex.getMessage(), ex);
                            throw new CerberusException(ex.getMessageError());
                        }
                    }
                } catch (CerberusException ex) {
                    LOG.debug("{}No Linked environment found.", logPrefix);
                }
                LOG.debug("{}Linked System Version Registered.", logPrefix);
            }

            // Start robot server if execution is not manual
            if (!execution.getManualExecution().equals("Y")
                    && (execution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_GUI)
                    || execution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_APK)
                    || execution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_IPA)
                    || execution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_FAT))) {

                MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.EXECUTION_PE_PREPARINGROBOTSERVER);
                mes.setDescription(mes.getDescription().replace("%IP%", execution.getRobotHost()));
                execution.setResultMessage(mes);
                updateExecutionWebSocketOnly(execution, true);

                // Decoding Robot capabilities.
                if (execution.getRobotObj() != null) {
                    List<RobotCapability> caps = execution.getRobotObj().getCapabilities();
                    List<RobotCapability> capsDecoded = new ArrayList<>();

                    for (RobotCapability cap : caps) {

                        String capDecoded = "";
                        try {
                            answerDecode = variableService.decodeStringCompletly(cap.getCapability(), execution, null, false);
                            capDecoded = answerDecode.getItem();

                            if (!(answerDecode.isCodeStringEquals("OK"))) {
                                // If anything wrong with the decode --> we stop here with decode message in the action result.
                                LOG.debug("{}TestCase interrupted due to decode 'Robot Capability key' Error.", logPrefix);
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
                                LOG.debug("{}TestCase interrupted due to decode 'Robot Capability value' Error.", logPrefix);
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
                    LOG.debug("{}{}", logPrefix, mes.getDescription());
                    throw new CerberusException(mes);

                } else {
                    //Start Robot server (Selenium/Appium/Sikuli)
                    LOG.debug("{}Starting Robot Server.", logPrefix);
                    try {
                        //Ensure retrocompability with selenium 3
                        if (parameterService.getParameterBooleanByKey(Parameter.VALUE_cerberus_use_w3c_capabilities, "", false) || execution.getRobot().contains("capabilityV2")) {
                            this.robotServerService.startServerV2(execution);
                        } else {
                            this.robotServerService.startServer(execution);
                        }
                        LOG.debug("{}Robot Server Started.", logPrefix);
                    } catch (CerberusException ex) {
                        // No need to report exception message as it will be catched and reported later
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

            // For BrowserStack and LambdaTest, we try to enrich the Tag with build hash.
            switch (execution.getRobotProvider()) {
                case TestCaseExecution.ROBOTPROVIDER_BROWSERSTACK:
                case TestCaseExecution.ROBOTPROVIDER_LAMBDATEST:
                    //TODO Why this variable has been declared ?
                    String newBuildHash = tagService.enrichTagWithCloudProviderBuild(execution.getRobotProvider(), execution.getSystem(), execution.getTag(), execution.getRobotExecutorObj().getHostUser(), execution.getRobotExecutorObj().getHostPassword());
                    execution.addExecutionLog(ExecutionLog.STATUS_INFO, "Retrieved new cloud provider hash '" + newBuildHash + "'");
                    Tag newTag = tagService.convert(tagService.readByKey(execution.getTag()));
                    execution.setTagObj(newTag);
                    break;
            }

            // Get used SeleniumCapabilities (empty if application is not GUI)
            LOG.debug("{}Getting Selenium capabitities for GUI applications.", logPrefix);
            if (execution.getApplicationObj().getType().equalsIgnoreCase(Application.TYPE_GUI) && !execution.getManualExecution().equals("Y")) {
                try {
                    Capabilities caps = this.robotServerService.getUsedCapabilities(execution.getSession());
                    execution.setVersion(caps.getBrowserVersion());
                    execution.setPlatform(caps.getPlatformName().toString());
                } catch (Exception ex) {
                    LOG.error("{}Exception on Selenium getting Used Capabilities.", logPrefix, ex);
                }
                LOG.debug("{}Selenium capabitities loaded.", logPrefix);
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
                LOG.debug("{}No Selenium capabitities loaded because application not (GUI,IPA,APK) : {}", logPrefix, execution.getApplicationObj().getType());
            }
            execution.setRobotDecli(execution.getRobotDecli().replace("%BROWSER%", execution.getBrowser()));
            execution.setRobotDecli(execution.getRobotDecli().replace("%BROWSERVERSION%", execution.getVersion()));
            execution.setRobotDecli(execution.getRobotDecli().replace("%PLATFORM%", execution.getPlatform()));

            //Load Pre TestCase information
            execution.setResultMessage(new MessageGeneral(MessageGeneralEnum.EXECUTION_PE_LOADINGDETAILEDDATA));
            updateExecutionWebSocketOnly(execution, true);

            LOG.debug("{}Loading Pre-testcases.", logPrefix);
            execution.addExecutionLog(ExecutionLog.STATUS_INFO, "Loading pre-testcases");

            List<TestCase> preTests = testCaseService.getTestCaseForPrePostTesting(Test.TEST_PRETESTING, execution.getTestCaseObj().getApplication(), execution.getCountry(),
                    execution.getSystem(), execution.getCountryEnvParam().getBuild(), execution.getCountryEnvParam().getRevision());
            List<TestCaseStep> preTestCaseStepList = new ArrayList<>();
            //Load Pre TestCase with Step dependencies (Actions/Control)
            if (preTests != null && !preTests.isEmpty()) {
                LOG.debug("{}Loaded PreTest List. {} found", logPrefix, preTests.size());
                LOG.debug("{}Pre-testcases Loaded.", logPrefix);
                LOG.debug("{}Loading all Steps information (Actions & Controls) of all Pre-testcase.", logPrefix);
                for (TestCase myTCase : preTests) {
                    preTestCaseStepList.addAll(this.loadTestCaseService.loadTestCaseStep(myTCase));
                    LOG.debug("{}Pre testcase : {} - {} Loaded With all Step(s) found.", logPrefix, myTCase.getTest(), myTCase.getTestcase());
                }
                LOG.debug("{}All Steps information (Actions & Controls) of all Pre-testcase Loaded.", logPrefix);
            } else {
                LOG.debug("{}No Pre-testcases found.", logPrefix);
            }

            //Load Post TestCase information
            execution.addExecutionLog(ExecutionLog.STATUS_INFO, "Loading post-testcases");
            LOG.debug("{}Loading Post-testcases.", logPrefix);
            List<TestCase> postTests = testCaseService.getTestCaseForPrePostTesting(Test.TEST_POSTTESTING, execution.getTestCaseObj().getApplication(), execution.getCountry(),
                    execution.getSystem(), execution.getCountryEnvParam().getBuild(), execution.getCountryEnvParam().getRevision());
            List<TestCaseStep> postTestCaseStepList = new ArrayList<>();
            // Load Post TestCase with Step dependencies (Actions/Control)
            if (postTests != null && !postTests.isEmpty()) {
                LOG.debug("{}Loaded PostTest List. {} found.", logPrefix, postTests.size());
                LOG.debug("{}Post-testcases Loaded.", logPrefix);
                LOG.debug("{}Loading all Steps information (Actions & Controls) of all Post-testcase.", logPrefix);
                for (TestCase myTCase : postTests) {
                    postTestCaseStepList.addAll(this.loadTestCaseService.loadTestCaseStep(myTCase));
                    LOG.debug("{}Post testcase : {}-{} Loaded With all Step(s) found.", logPrefix, myTCase.getTest(), myTCase.getTestcase());
                }
                LOG.debug("{}All Steps information (Actions & Controls) of all Post-testcase Loaded.", logPrefix);
            } else {
                LOG.debug("{}No Post-testcases found.", logPrefix);
            }

            // Load Main TestCase with Step dependencies (Actions/Control)
            execution.addExecutionLog(ExecutionLog.STATUS_INFO, "Loading steps information of main testcase");
            LOG.debug("{}Loading all Steps information of Main testcase.", logPrefix);
            List<TestCaseStep> testCaseStepList;
            testCaseStepList = this.loadTestCaseService.loadTestCaseStep(execution.getTestCaseObj());
            execution.getTestCaseObj().setSteps(testCaseStepList);
            LOG.debug("{}Steps information of Main testcase Loaded : {} Step(s) found.", logPrefix, execution.getTestCaseObj().getSteps().size());

            // Load All properties of the testcase
            LOG.debug("{}Loading all Properties.", logPrefix);
            List<TestCaseCountryProperties> tcProperties = new ArrayList<>();
            try {
                tcProperties = testCaseCountryPropertiesService.findAllWithDependencies(execution.getTest(), execution.getTestCase(), execution.getCountry(),
                        execution.getSystem(), execution.getCountryEnvParam().getBuild(), execution.getCountryEnvParam().getRevision());
                execution.setTestCaseCountryPropertyList(tcProperties);
            } catch (CerberusException ex) {
                LOG.warn("Exception getting all the properties : ", ex);
            }
            LOG.debug("{}All Properties Loaded. {} property(ies) found : {}", logPrefix, tcProperties.size(), tcProperties);

            // Load All Execution Data of testcases that this execution depends
            LOG.debug("{}Loading all Execution Data of the execution from queue dependencies.", logPrefix);
            this.testCaseExecutionDataService.loadTestCaseExecutionDataFromDependencies(execution);

            /*
             * Start Execution of the steps/Actions/controls Iterate Steps.
             * mainExecutionTestCaseStepList will contain the list of steps to
             * execute for both pretest and test. This is where we schedule the
             * execution of the steps using mainExecutionTestCaseStepList
             * object.
             */
            LOG.debug("{}Starting the execution with step iteration.", logPrefix);
            List<TestCaseStep> mainExecutionTestCaseStepList;
            mainExecutionTestCaseStepList = new ArrayList<>();
            mainExecutionTestCaseStepList.addAll(preTestCaseStepList);
            mainExecutionTestCaseStepList.addAll(testCaseStepList);
            mainExecutionTestCaseStepList.addAll(postTestCaseStepList);

            // Open Kafka Consumer
            execution.setResultMessage(new MessageGeneral(MessageGeneralEnum.EXECUTION_PE_LOADINGKAFKACONSUMERS));
            updateExecution(execution, true);
            execution.setKafkaLatestOffset(kafkaService.getAllConsumers(mainExecutionTestCaseStepList, execution));

            // Initialize the global TestCaseExecution Data List
            execution.setResultMessage(new MessageGeneral(MessageGeneralEnum.EXECUTION_PE_TESTEXECUTING));
            updateExecution(execution, true);

            // Evaluate the condition at the step level.
            AnswerItem<Boolean> conditionAnswerTc;
            boolean conditionDecodeError = false;

            // If execution is not manual, evaluate the condition at the top level
            if (!execution.getManualExecution().equals("Y")) {
                execution.addExecutionLog(ExecutionLog.STATUS_INFO, "Decode execution eondition variables");
                try {
                    answerDecode = variableService.decodeStringCompletly(execution.getConditionVal1(), execution, null, false);
                    execution.setConditionVal1(answerDecode.getItem());

                    if (!(answerDecode.isCodeStringEquals("OK"))) {
                        // If anything wrong with the decode --> we stop here with decode message in the action result.
                        execution.setResultMessage(new MessageGeneral(MessageGeneralEnum.EXECUTION_FA_DECODE)
                                .resolveDescription("MES", answerDecode.getMessageDescription())
                                .resolveDescription("AREA", "TestCase Condition Value1"));
                        execution.setEnd(new Date().getTime());
                        LOG.debug("{}TestCase interrupted due to decode 'TestCase Condition Value1' Error.", logPrefix);
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
                        LOG.debug("{}TestCase interrupted due to decode 'TestCase Condition Value2' Error.", logPrefix);
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
                        LOG.debug("{}TestCase interrupted due to decode 'TestCase Condition Value3Error.", logPrefix);
                        conditionDecodeError = true;
                    }
                } catch (CerberusEventException cex) {
                    LOG.warn(cex);
                }
            }

            if (!conditionDecodeError) {

                execution.addExecutionLog(ExecutionLog.STATUS_INFO, "Evaluate testcase condition");

                conditionAnswerTc = this.conditionService.evaluateCondition(execution.getConditionOperator(),
                        execution.getConditionVal1(), execution.getConditionVal2(), execution.getConditionVal3(),
                        execution, execution.getConditionOptions());

                boolean doExecuteTestCase = conditionAnswerTc.getItem();

                if (doExecuteTestCase || execution.getManualExecution().equals("Y")) {

                    boolean doStepStopExecution = false;
                    for (TestCaseStep step : mainExecutionTestCaseStepList) {

                        ConditionOperatorEnum testcaseStepConditionEnum = ConditionOperatorEnum.getConditionOperatorEnumFromString(step.getConditionOperator());

                        // exeMod management : We trigger Forced Step no matter if previous step execution asked to stop.
                        if (!doStepStopExecution || step.isExecutionForced()) {

                            // init the index of the step in case we loop.
                            int stepIndex = 1;
                            boolean doExecuteNextStep = false;
                            TestCaseStepExecution stepExecution;
                            int maxloop = parameterService.getParameterIntegerByKey("cerberus_loopstep_max", execution.getApplicationObj().getSystem(), 20);

                            // Step Loop management.
                            do {

                                // Start Execution of TestCaseStep
                                LOG.debug("{}Start execution of testcasestep", logPrefix);
                                long startStep = new Date().getTime();

                                // Clean condition depending on the operatot.
                                String condval1 = conditionService.cleanValue1(step.getConditionOperator(), step.getConditionValue1());
                                String condval2 = conditionService.cleanValue2(step.getConditionOperator(), step.getConditionValue2());
                                String condval3 = conditionService.cleanValue3(step.getConditionOperator(), step.getConditionValue3());

                                //Create and Register TestCaseStepExecution
                                MessageEvent stepMess = new MessageEvent(MessageEventEnum.STEP_PENDING)
                                        .resolveDescription("STEP", String.valueOf(step.getSort()))
                                        .resolveDescription("STEPINDEX", String.valueOf(stepIndex));
                                stepExecution = factoryTestCaseStepExecution.create(
                                        runID, step.getTest(), step.getTestcase(),
                                        step.getStepId(), stepIndex, step.getSort(), step.getLoop(), step.getConditionOperator(), condval1, condval2, condval3, condval1, condval2, condval3, null,
                                        startStep, startStep, startStep, startStep, new BigDecimal("0"), null, stepMess, step, execution,
                                        step.isUsingLibraryStep(), step.getLibraryStepTest(), step.getLibraryStepTestcase(), step.getLibraryStepStepId(), step.getDescription());
                                stepExecution.setLoop(step.getLoop());
                                stepExecution.setConditionOptions(step.getConditionOptionsActive());

                                testCaseStepExecutionService.insertTestCaseStepExecution(stepExecution, execution.getSecrets());
                                stepExecution.setExecutionResultMessage(new MessageGeneral(MessageGeneralEnum.EXECUTION_PE_TESTSTARTED));

                                // We populate the TestCaseStep inside the execution List
                                execution.addStepExecutionList(stepExecution);

                                // determine if step is executed (doExecuteStep) and if we trigger a new step execution after (doExecuteNextStep)
                                boolean doExecuteStep = true;
                                boolean descriptionOrConditionStepDecodeError = false;
                                boolean conditionStepError = false;
                                AnswerItem<Boolean> conditionAnswer = new AnswerItem<>(new MessageEvent(MessageEventEnum.CONDITIONEVAL_FAILED_UNKNOWNCONDITION));
                                if (!((stepExecution.getLoop().equals(TestCaseStep.LOOP_DOWHILECONDITIONTRUE)
                                        || stepExecution.getLoop().equals(TestCaseStep.LOOP_DOWHILECONDITIONFALSE))
                                        && stepIndex == 1)) {
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
                                                LOG.debug("{}Step interrupted due to decode 'Step Condition Value1' Error.", logPrefix);
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
                                                LOG.debug("{}Step interrupted due to decode 'Step Condition Value2' Error.", logPrefix);
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
                                                LOG.debug("{}Step interrupted due to decode 'Step Condition Value3' Error.", logPrefix);
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
                                            LOG.debug("{}Step interrupted due to decode 'Step Description' Error.", logPrefix);
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
                                        || stepIndex > 1) {
                                    if (!(descriptionOrConditionStepDecodeError)) {

                                        conditionAnswer = this.conditionService.evaluateCondition(stepExecution.getConditionOperator(),
                                                stepExecution.getConditionValue1(), stepExecution.getConditionValue2(), stepExecution.getConditionValue3(),
                                                execution, stepExecution.getConditionOptions());

                                        doExecuteStep = conditionAnswer.getItem();
                                        if (conditionAnswer.getResultMessage().getMessage().getCodeString().equals("PE")) {
                                            // There were no error when performing the condition evaluation.
                                            switch (stepExecution.getLoop()) {
                                                case TestCaseStep.LOOP_ONCEIFCONDITIONFALSE:
                                                    doExecuteStep = !doExecuteStep;
                                                    doExecuteNextStep = false;
                                                    break;
                                                case TestCaseStep.LOOP_ONCEIFCONDITIONTRUE:
                                                case "":
                                                    doExecuteNextStep = false;
                                                    break;
                                                case TestCaseStep.LOOP_WHILECONDITIONFALSEDO:
                                                case TestCaseStep.LOOP_DOWHILECONDITIONFALSE:
                                                    doExecuteStep = !doExecuteStep;
                                                    doExecuteNextStep = doExecuteStep;
                                                    break;
                                                case TestCaseStep.LOOP_WHILECONDITIONTRUEDO:
                                                case TestCaseStep.LOOP_DOWHILECONDITIONTRUE:
                                                    doExecuteNextStep = doExecuteStep;
                                                    break;
                                                default:
                                                    doExecuteNextStep = false;
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
                                            LOG.debug("{}Step interrupted due to condition error.", logPrefix);
                                            conditionStepError = true;
                                            doExecuteNextStep = false;
                                            doExecuteStep = false;
                                        }
                                    } else {

                                        // If anything wrong with the decode --> we stop here with decode message in the action result.
                                        execution.setResultMessage(new MessageGeneral(MessageGeneralEnum.EXECUTION_FA_DECODE)
                                                .resolveDescription("AREA", "Step")
                                                .resolveDescription("MES", answerDecode.getMessageDescription()));
                                        execution.setEnd(new Date().getTime());
                                        LOG.debug("{}TestCase interrupted due to decode Error.", logPrefix);

                                        // There was an error on decode so we stop everything.
                                        if (execution.getManualExecution().equals("Y")) {
                                            doExecuteNextStep = true;
                                            doExecuteStep = true;
                                        } else {
                                            doExecuteNextStep = false;
                                            doExecuteStep = false;
                                        }
                                    }
                                } else if (stepExecution.getLoop().equals(TestCaseStep.LOOP_DOWHILECONDITIONFALSE)
                                        || stepExecution.getLoop().equals(TestCaseStep.LOOP_DOWHILECONDITIONTRUE)) {
                                    // First Step execution for LOOP_DOWHILECONDITIONTRUE and LOOP_DOWHILECONDITIONFALSE --> We force the step execution and activate the next step execution.
                                    // We also force the condition message to always true with success.
                                    doExecuteStep = true;
                                    doExecuteNextStep = true;
                                    conditionAnswer.setResultMessage(new MessageEvent(MessageEventEnum.CONDITIONEVAL_TRUE_ALWAYS));
                                } else {
                                    // First Step execution for Unknown Loop --> We force the step execution only once (default behaviour).
                                    doExecuteStep = true;
                                    doExecuteNextStep = false;
                                    conditionAnswer.setResultMessage(new MessageEvent(MessageEventEnum.CONDITIONEVAL_FAILED_UNKNOWNLOOP).resolveDescription("LOOP", stepExecution.getLoop()));
                                }

                                //  Execute Step
                                LOG.debug("{}Executing step : {} - {} - Step {} - Index {}",
                                        logPrefix, stepExecution.getTest(), stepExecution.getTestCase(), stepExecution.getStepId(), stepExecution.getStepId());
                                execution.addExecutionLog(ExecutionLog.STATUS_INFO, "Executing step : " + stepExecution.getStepId() + " - " + stepExecution.getDescription());

                                if (doExecuteStep) {
                                    // We execute the step
                                    stepExecution = this.executeStep(stepExecution, execution);

                                    /*
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

                                    /*
                                     * We test here is execution is manual and
                                     * operator needs to evaluate the condition
                                     * manually. If this is the case, we add the
                                     * comment inside the description.
                                     */
                                    if (execution.getManualExecution().equals("Y") && testcaseStepConditionEnum.isOperatorEvaluationRequired()) {
                                        stepExecution.setDescription(stepExecution.getDescription() + " - " + conditionAnswer.getMessageDescription());
                                    }

                                    testCaseStepExecutionService.updateTestCaseStepExecution(stepExecution, execution.getSecrets());

                                    if (stepExecution.isStopExecution()) {
                                        break;
                                    }

                                } else if ((!descriptionOrConditionStepDecodeError) && (!conditionStepError)) { // We don't execute the step and record a generic execution.

                                    // Register Step in database
                                    LOG.debug("{}Registering Step : {}", logPrefix, stepExecution.getStepId());

                                    // We change the Step message only if the Step is not executed due to condition.
                                    MessageEvent stepMes = new MessageEvent(MessageEventEnum.CONDITION_TESTCASESTEP_NOTEXECUTED);
                                    stepExecution.setStepResultMessage(stepMes);
                                    stepExecution.setReturnMessage(stepExecution.getReturnMessage()
                                            .replace("%COND%", stepExecution.getConditionOperator())
                                            .replace("%LOOP%", stepExecution.getLoop())
                                            .replace("%MESSAGE%", conditionAnswer.getResultMessage().getDescription())
                                    );

                                    stepExecution.setEnd(new Date().getTime());
                                    this.testCaseStepExecutionService.updateTestCaseStepExecution(stepExecution, execution.getSecrets());
                                    LOG.debug("{}Registered Step", logPrefix);

                                } else {
                                    // Not executed because decode error or failed condition.
                                    stepExecution.setEnd(new Date().getTime());
                                    stepExecution.setStopExecution(true);
                                    this.testCaseStepExecutionService.updateTestCaseStepExecution(stepExecution, execution.getSecrets());
                                    LOG.debug("{}Registered Step", logPrefix);
                                }

                                // Log TestCaseStepExecution
                                if ((execution.getVerbose() > 0) && parameterService.getParameterBooleanByKey("cerberus_executionlog_enable", execution.getSystem(), false)) {
                                    LOG.info(stepExecution.toJson(false, true, execution.getSecrets()));
                                }

                                // Websocket --> we refresh the corresponding Detail Execution pages attached to this execution.
                                updateExecutionWebSocketOnly(execution, false);

                                stepIndex++;
                            } while (doExecuteNextStep && stepIndex <= maxloop);

                            // Step execution boolean is considered for next step execution only if current step was not forced or forced and failed.
                            if (!step.isExecutionForced() || stepExecution.isStopExecution()) {
                                doStepStopExecution = stepExecution.isStopExecution();
                            }
                        }
                    }

                    /*
                     * If at that time the execution is still PE, we move it to
                     * OK. It means that no issue were met.
                     */
                    if ((execution.getResultMessage() == null) || (execution.getResultMessage().equals(new MessageGeneral(MessageGeneralEnum.EXECUTION_PE_TESTSTARTED)))) {
                        execution.setResultMessage(new MessageGeneral(MessageGeneralEnum.EXECUTION_OK));
                    }

                    // We notify external robot provider of end of execution status.
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
                    // Update Execution status from condition
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
            // If an exception is found, set the execution to FA and print the exception (only in debug mode)
            MessageGeneral messageFin = new MessageGeneral(MessageGeneralEnum.EXECUTION_FA);
            messageFin.setDescription(messageFin.getDescription() + " " + ex.getMessageError().getDescription());
            execution.setResultMessage(messageFin);
            LOG.debug("{}Exception found Executing Test {} : {}", logPrefix, execution.getId(), ex.getMessageError().getDescription());
        } catch (Exception ex) {
            // If an exception is found, set the execution to FA and print the exception
            MessageGeneral messageFin = new MessageGeneral(MessageGeneralEnum.EXECUTION_FA);
            messageFin.setDescription(messageFin.getDescription() + " " + ex.getMessage());
            execution.setResultMessage(messageFin);
            execution.setControlMessage(execution.getControlMessage() + " Exception: " + ex);
            LOG.error("{}Exception found Executing Test {}", logPrefix, execution.getId(), ex);
        } finally {

            //  We stop the server session here (selenium for ex.).
            try {
                if (videoRecorder != null) {
                    videoRecorder.endRecordVideo();
                }
                execution = this.stopTestCase(execution);
            } catch (Exception ex) {
                LOG.error("{}Exception Stopping Test {} Exception: {}", logPrefix, execution.getId(), ex.toString(), ex);
            }

            // Log Execution
            if ((execution.getVerbose() > 0) && parameterService.getParameterBooleanByKey("cerberus_executionlog_enable", execution.getSystem(), false)) {
                LOG.info(execution.toJson(false));
            }

            // Clean memory
            try {
                executionUUID.removeExecutionUUID(execution.getExecutionUUID());
                LOG.debug("{}Clean ExecutionUUID", logPrefix);
            } catch (Exception ex) {
                LOG.error("{}Exception cleaning Memory: {}", logPrefix, ex.toString(), ex);
            }

            // Credit Limit increase
            sessionCounter.incrementCreditLimitNbExe();
            long durationinSecond = (execution.getEnd() - execution.getStart()) / 1000;
            if ((durationinSecond > 0) && (durationinSecond <= 1000000)) {
                sessionCounter.incrementCreditLimitSecondExe((int) durationinSecond);
            }

            // Log execution is finished
            LOG.info("Execution Finished : UUID={} ID={} RC={} TestName={}.{}.{}.{}.{}_{}_{}",
                    execution.getExecutionUUID(),
                    execution.getId(),
                    execution.getControlStatus(),
                    execution.getEnvironment(),
                    execution.getCountry(),
                    execution.getBuild(),
                    execution.getRevision(),
                    execution.getTest(),
                    execution.getTestCase(),
                    execution.getTestCaseObj().getDescription().replace(".", ""));

            // Retry management, in case the result is not (OK or NE), we execute the job again reducing the retry to 1.
            Map<String, Integer> willBeRetriedMap = retriesService.manageRetries(execution);
            boolean willBeRetried = false;
            if (willBeRetriedMap.get("Retry") == 1) {
                willBeRetried = true;
            }

            execution.setUseful(!willBeRetried);

            execution.setFlaky(!willBeRetried && (TestCaseExecution.CONTROLSTATUS_OK.equals(execution.getControlStatus()) && willBeRetriedMap.get("AlreadyExecuted") > 0));

            testCaseExecutionService.updateLastAndFlaky(runID,
                    !willBeRetried,
                    !willBeRetried && (TestCaseExecution.CONTROLSTATUS_OK.equals(execution.getControlStatus()) && willBeRetriedMap.get("AlreadyExecuted") > 0),
                    execution.getUsrCreated());

            // Updating queue to done status only for execution from queue
            if (execution.getQueueID() != 0) {
                executionQueueService.updateToDone(execution.getQueueID(), "", runID);
            }

            // Trigger the necessary Event for WebHook and notification management.
            eventService.triggerEvent(EventHook.EVENTREFERENCE_EXECUTION_END, execution, null, null, null);
            if (!willBeRetried) {
                eventService.triggerEvent(EventHook.EVENTREFERENCE_EXECUTION_END_LASTRETRY, execution, null, null, null);
            }

            // JIRA XRay Connector is triggered at the end of every execution..
            if (!willBeRetried) {
                xRayService.createXRayTestExecution(execution);
            }

            // Bug creation Connector is triggered at the end of every execution.
            if (!willBeRetried) {
                bugService.createBugAsync(execution, false);
            }

            /*
             * After every execution finished, <br>
             * if the execution has a tag that has a campaign associated  <br>
             * and no more executions are in the queue, <br>
             * we trigger : <br>
             * 1/ The update of the EndExeQueue of the tag <br>
             * 2/ We notify the Distribution List with execution report status
             */
            tagService.manageCampaignEndOfExecution(execution.getTag());

            /*
             * Dependency management, At the end of the execution, we RELEASE
             * the corresponding dependencies and put corresponding Queue
             * entries to QUEUED status.
             */
            if (!willBeRetried) {
                testCaseExecutionQueueDepService.manageDependenciesEndOfExecution(execution);
            }

            // Write Execution log to file and make it available as a file on execution.
            execution.addFileList(recorderService.recordExeLog(execution));

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
            testCaseExecutionWebSocket.send(execution, forcePush);
        }

    }

    @Override
    public TestCaseExecution stopTestCase(TestCaseExecution execution) {

        // Stop Execution
        LOG.debug("{} - Stop the execution {} UUID: {}", execution.getId(), execution.getId(), execution.getExecutionUUID());
        try {
            this.stopExecutionRobotAndProxy(execution);
        } catch (Exception ex) {
            LOG.warn("Exception Stopping Execution {} Exception : {}", execution.getId(), ex.toString(), ex);
        }

        // Saving TestCaseExecution object.
        execution.setEnd(new Date().getTime());
        execution.setDurationMs(new Date().getTime() - execution.getStart());

        try {
            testCaseExecutionService.updateTCExecution(execution);
        } catch (CerberusException ex) {
            LOG.warn("Exception updating Execution : {} Exception: {}", execution.getId(), ex.toString());
        }

        // Websocket --> we refresh the corresponding Detail Execution pages attached to this execution.
        if (execution.isCerberus_featureflipping_activatewebsocketpush()) {
            testCaseExecutionWebSocket.send(execution, true);
            testCaseExecutionWebSocket.end(execution);
        }

        if (execution.isCerberus_featureflipping_activatewebsocketpush()) {
            executionMonitor.addNewExecutionToMonitor(execution.toLight());
            executionMonitorWebSocket.send(true);
        }

        return execution;
    }

    private TestCaseStepExecution executeStep(TestCaseStepExecution stepExecution, TestCaseExecution execution) {

        long runID = stepExecution.getId();
        String logPrefix = runID + " - ";
        AnswerItem<String> answerDecode;

        // Initialise the Step Data List.
        List<TestCaseExecutionData> myStepDataList = new ArrayList<>();
        stepExecution.setTestCaseExecutionDataList(myStepDataList);
        // Initialise the Data List used to enter the action.
        // Iterate Actions
        List<TestCaseStepAction> testCaseStepActionList = stepExecution.getTestCaseStep().getActions();
        LOG.debug("{}Getting list of actions of the step. {} action(s) to perform.", logPrefix, testCaseStepActionList.size());

        execution.setTestCaseStepInExecution(stepExecution);
        for (TestCaseStepAction tcAction : testCaseStepActionList) {

            // Start Execution of TestCaseStepAction
            long startAction = new Date().getTime();
            DateFormat df = new SimpleDateFormat(DateUtil.DATE_FORMAT_TIMESTAMP);
            long startLongAction = Long.parseLong(df.format(startAction));

            // Clean condition depending on the operatot.
            String condval1 = conditionService.cleanValue1(tcAction.getConditionOperator(), tcAction.getConditionValue1());
            String condval2 = conditionService.cleanValue2(tcAction.getConditionOperator(), tcAction.getConditionValue2());
            String condval3 = conditionService.cleanValue3(tcAction.getConditionOperator(), tcAction.getConditionValue3());

            // Create and Register TestCaseStepActionExecution.
            TestCaseStepActionExecution actionExecution = factoryTestCaseStepActionExecution.create(
                    stepExecution.getId(), tcAction.getTest(), tcAction.getTestcase(),
                    tcAction.getStepId(), stepExecution.getIndex(), tcAction.getActionId(), tcAction.getSort(), null, null,
                    tcAction.getConditionOperator(), condval1, condval2, condval3, condval1, condval2, condval3,
                    tcAction.getAction(), tcAction.getValue1(), tcAction.getValue2(), tcAction.getValue3(), tcAction.getValue1(),
                    tcAction.getValue2(), tcAction.getValue3(),
                    (tcAction.isFatal() ? "Y" : "N"), startAction, startAction, startLongAction, startLongAction, new MessageEvent(MessageEventEnum.ACTION_PENDING),
                    tcAction.getDescription(), tcAction, stepExecution);
            actionExecution.setOptions(tcAction.getOptionsActive());
            actionExecution.setConditionOptions(tcAction.getConditionOptionsActive());
            actionExecution.setWaitBefore(tcAction.getWaitBefore());
            actionExecution.setWaitAfter(tcAction.getWaitAfter());
            actionExecution.setDoScreenshotBefore(tcAction.isDoScreenshotBefore());
            actionExecution.setDoScreenshotAfter(tcAction.isDoScreenshotAfter());

            this.testCaseStepActionExecutionService.insertTestCaseStepActionExecution(actionExecution, execution.getSecrets());

            // We populate the TestCase Action List
            stepExecution.addActionExecutionList(actionExecution);

            // If execution is not manual, evaluate the condition at the action level
            AnswerItem<Boolean> conditionAnswer;
            boolean conditionDecodeError = false;
            if (!execution.getManualExecution().equals("Y")) {

                try {
                    answerDecode = variableService.decodeStringCompletly(actionExecution.getConditionVal1(), execution, null, false);
                    actionExecution.setConditionVal1(answerDecode.getItem());

                    if (!(answerDecode.isCodeStringEquals("OK"))) {
                        // If anything wrong with the decode --> we stop here with decode message in the action result.
                        actionExecution.setActionResultMessage(answerDecode.getResultMessage().resolveDescription("FIELD", "Action Condition Value1"));
                        actionExecution.setExecutionResultMessage(new MessageGeneral(answerDecode.getResultMessage().getMessage()));
                        actionExecution.setStopExecution(answerDecode.getResultMessage().isStopTest());
                        actionExecution.setEnd(new Date().getTime());
                        LOG.debug("{}Action interrupted due to decode 'Action Condition Value1' Error.", logPrefix);
                        conditionDecodeError = true;
                    }
                } catch (CerberusEventException cex) {
                    LOG.warn(cex);
                }

                try {
                    answerDecode = variableService.decodeStringCompletly(actionExecution.getConditionVal2(), execution, null, false);
                    actionExecution.setConditionVal2(answerDecode.getItem());

                    if (!(answerDecode.isCodeStringEquals("OK"))) {
                        // If anything wrong with the decode --> we stop here with decode message in the action result.
                        actionExecution.setActionResultMessage(answerDecode.getResultMessage().resolveDescription("FIELD", "Action Condition Value2"));
                        actionExecution.setExecutionResultMessage(new MessageGeneral(answerDecode.getResultMessage().getMessage()));
                        actionExecution.setStopExecution(answerDecode.getResultMessage().isStopTest());
                        actionExecution.setEnd(new Date().getTime());
                        LOG.debug("{}Action interrupted due to decode 'Action Condition Value2' Error.", logPrefix);
                        conditionDecodeError = true;
                    }
                } catch (CerberusEventException cex) {
                    LOG.warn(cex);
                }
                try {
                    answerDecode = variableService.decodeStringCompletly(actionExecution.getConditionVal3(), execution, null, false);
                    actionExecution.setConditionVal3(answerDecode.getItem());

                    if (!(answerDecode.isCodeStringEquals("OK"))) {
                        // If anything wrong with the decode --> we stop here with decode message in the action result.
                        actionExecution.setActionResultMessage(answerDecode.getResultMessage().resolveDescription("FIELD", "Action Condition Value3"));
                        actionExecution.setExecutionResultMessage(new MessageGeneral(answerDecode.getResultMessage().getMessage()));
                        actionExecution.setStopExecution(answerDecode.getResultMessage().isStopTest());
                        actionExecution.setEnd(new Date().getTime());
                        LOG.debug("{}Action interrupted due to decode 'Action Condition Value3' Error.", logPrefix);
                        conditionDecodeError = true;
                    }
                } catch (CerberusEventException cex) {
                    LOG.warn(cex);
                }
            }

            if (!(conditionDecodeError)) {

                // Record picture= files at Condition action level.
                Identifier identifier = identifierService.convertStringToIdentifier(actionExecution.getConditionVal1());
                if (identifier.getIdentifier().equals(SikuliService.SIKULI_IDENTIFIER_PICTURE) && !StringUtil.isEmptyOrNull(identifier.getLocator())) {
                    LOG.debug("Saving Image 2 on Action : " + identifier.getLocator());
                    actionExecution.addFileList(recorderService.recordPicture(actionExecution, -1, identifier.getLocator(), "Condition1"));
                }
                identifier = identifierService.convertStringToIdentifier(actionExecution.getConditionVal2());
                if (identifier.getIdentifier().equals(SikuliService.SIKULI_IDENTIFIER_PICTURE) && !StringUtil.isEmptyOrNull(identifier.getLocator())) {
                    LOG.debug("Saving Image 2 on Action : " + identifier.getLocator());
                    actionExecution.addFileList(recorderService.recordPicture(actionExecution, -1, identifier.getLocator(), "Condition2"));
                }

                ConditionOperatorEnum actionConditionOperatorEnum = ConditionOperatorEnum.getConditionOperatorEnumFromString(actionExecution.getConditionOperator());

                conditionAnswer = this.conditionService.evaluateCondition(actionExecution.getConditionOperator(),
                        actionExecution.getConditionVal1(), actionExecution.getConditionVal2(), actionExecution.getConditionVal3(),
                        execution, actionExecution.getConditionOptions());
                boolean doExecuteAction = conditionAnswer.getItem();

                if (execution.getManualExecution().equals("Y") && actionConditionOperatorEnum.isOperatorEvaluationRequired()) {
                    actionExecution.setDescription(actionExecution.getDescription() + " - " + conditionAnswer.getMessageDescription());
                }

                // If condition OK or if manual execution, then execute the action
                if (conditionAnswer.getResultMessage().getMessage().getCodeString().equals("PE")
                        || execution.getManualExecution().equals("Y")) {

                    // Execute or not the action here.
                    if (doExecuteAction || execution.getManualExecution().equals("Y")) {
                        LOG.debug("Executing action : {} with val1 : {} and val2 : {} and val3 : {}",
                                actionExecution.getAction(),
                                actionExecution.getValue1(),
                                actionExecution.getValue2(),
                                actionExecution.getValue3());

                        execution.addExecutionLog(ExecutionLog.STATUS_INFO, "Executing action : " + actionExecution.getSequence() + " - " + actionExecution.getDescription() + " Action '" + actionExecution.getAction() + "' with '" + actionExecution.getValue1() + "' | '" + actionExecution.getValue2() + "' | '" + actionExecution.getValue3() + "'");

                        // We execute the Action
                        actionExecution = this.executeAction(actionExecution, execution);
                        // If Action or property reported to stop the testcase, we stop it and update the step with the message.
                        stepExecution.setStopExecution(actionExecution.isStopExecution());
                        if ((!(actionExecution.getExecutionResultMessage().equals(new MessageGeneral(MessageGeneralEnum.EXECUTION_OK))))
                                && (!(actionExecution.getExecutionResultMessage().equals(new MessageGeneral(MessageGeneralEnum.EXECUTION_PE_TESTEXECUTING))))) {
                            stepExecution.setExecutionResultMessage(actionExecution.getExecutionResultMessage());
                            stepExecution.setStepResultMessage(actionExecution.getActionResultMessage());
                        }

                        if (actionExecution.isStopExecution()) {
                            break;
                        }
                    } else {
                        // We don't execute the action and record a generic execution.
                        // Record Screenshot, PageSource
                        actionExecution.addFileList(recorderService.recordExecutionInformationAfterStepActionAndControl(actionExecution, null));

                        LOG.debug("Registering Action : {}", actionExecution.getAction());

                        // We change the Action message only if the action is not executed due to condition.
                        MessageEvent actionMes = new MessageEvent(MessageEventEnum.CONDITION_TESTCASEACTION_NOTEXECUTED);
                        actionExecution.setActionResultMessage(actionMes);
                        actionExecution.setReturnMessage(actionExecution.getReturnMessage()
                                .replace("%COND%", actionExecution.getConditionOperator())
                                .replace("%MESSAGE%", conditionAnswer.getResultMessage().getDescription())
                        );

                        actionExecution.setEnd(new Date().getTime());
                        this.testCaseStepActionExecutionService.updateTestCaseStepActionExecution(actionExecution, execution.getSecrets());
                        LOG.debug("{}Registered Action", logPrefix);

                    }
                } else {
                    // Error when performing the condition evaluation. We force no execution (false)
                    MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.EXECUTION_FA_CONDITION);
                    mes.setDescription(mes.getDescription()
                            .replace("%COND%", actionExecution.getConditionOperator())
                            .replace("%AREA%", "action ")
                            .replace("%MES%", conditionAnswer.getResultMessage().getDescription()));
                    actionExecution.setExecutionResultMessage(mes);
                    stepExecution.setExecutionResultMessage(actionExecution.getExecutionResultMessage());

                    actionExecution.setActionResultMessage(new MessageEvent(MessageEventEnum.CONDITION_TESTCASEACTION_FAILED)
                            .resolveDescription("AREA", "")
                            .resolveDescription("COND", actionExecution.getConditionOperator())
                            .resolveDescription("MESSAGE", conditionAnswer.getResultMessage().getDescription()));

                    stepExecution.setStepResultMessage(new MessageEvent(MessageEventEnum.CONDITION_TESTCASESTEP_FAILED)
                            .resolveDescription("AREA", "action ")
                            .resolveDescription("COND", actionExecution.getConditionOperator())
                            .resolveDescription("MESSAGE", conditionAnswer.getResultMessage().getDescription()));
                    if (actionExecution.isFatal().equals("N")) {
                        actionExecution.setStopExecution(false);
                        MessageEvent actionMes = actionExecution.getActionResultMessage();
                        actionMes.setDescription(actionExecution.getActionResultMessage().getDescription() + " -- Execution forced to continue.");
                        actionExecution.setActionResultMessage(actionMes);
                    } else {
                        actionExecution.setStopExecution(true);
                    }

                    stepExecution.setStopExecution(actionExecution.isStopExecution());

                    actionExecution.setEnd(new Date().getTime());

                    this.testCaseStepActionExecutionService.updateTestCaseStepActionExecution(actionExecution, execution.getSecrets());
                    LOG.debug("{}Action interrupted due to condition error.", logPrefix);
                    // We stop any further Action execution.
                    if (actionExecution.isStopExecution()) {
                        break;
                    }
                }
            } else {

                actionExecution.setEnd(new Date().getTime());
                stepExecution.setExecutionResultMessage(actionExecution.getExecutionResultMessage());
                stepExecution.setStepResultMessage(actionExecution.getActionResultMessage());
                stepExecution.setStopExecution(actionExecution.isStopExecution());
                this.testCaseStepActionExecutionService.updateTestCaseStepActionExecution(actionExecution, execution.getSecrets());
                LOG.debug("{}Registered Action", logPrefix);
                if (actionExecution.isStopExecution()) {
                    break;
                }
            }

            // Log TestCaseStepActionExecution
            if ((execution.getVerbose() > 0) && parameterService.getParameterBooleanByKey("cerberus_executionlog_enable", execution.getSystem(), false)) {
                LOG.info(actionExecution.toJson(false, true, execution.getSecrets()));
            }

        }
        stepExecution.setEnd(new Date().getTime());

        this.testCaseStepExecutionService.updateTestCaseStepExecution(stepExecution, execution.getSecrets());

        updateExecutionWebSocketOnly(execution, false);

        return stepExecution;
    }

    private TestCaseStepActionExecution executeAction(TestCaseStepActionExecution actionExecution, TestCaseExecution execution) {

        LOG.debug("Starting execute Action : {}", actionExecution.getAction());
        AnswerItem<String> answerDecode;

        // If execution is not manual, do action and record files
        if (!execution.getManualExecution().equals("Y")) {

            // Record Screenshot
            try {
                actionExecution.addFileList(recorderService.recordExecutionInformationBeforeStepActionAndControl(actionExecution, null));
            } catch (Exception ex) {
                LOG.warn("Unable to record Screenshot Before Action : {}", ex.toString(), ex);
            }

            actionExecution = this.actionService.doAction(actionExecution);

            // Record Screenshot, PageSource
            try {
                actionExecution.addFileList(recorderService.recordExecutionInformationAfterStepActionAndControl(actionExecution, null));
            } catch (Exception ex) {
                LOG.warn("Unable to record Screenshot/PageSource After Action : {}", ex.toString(), ex);
            }

        } else {
            // If execution manual, set Action result message as notExecuted
            actionExecution.setActionResultMessage(new MessageEvent(MessageEventEnum.ACTION_WAITINGFORMANUALEXECUTION));
            actionExecution.setExecutionResultMessage(new MessageGeneral(MessageGeneralEnum.EXECUTION_WE));
            actionExecution.setEnd(new Date().getTime());
        }

        // Register Action in database
        LOG.debug("Registering Action : {}", actionExecution.getAction());

        this.testCaseStepActionExecutionService.updateTestCaseStepActionExecution(actionExecution, execution.getSecrets());
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
        MessageGeneral executionResultMessage = actionExecution.getExecutionResultMessage();
        // Iterate Control
        List<TestCaseStepActionControl> tcsacList = actionExecution.getTestCaseStepAction().getControls();
        for (TestCaseStepActionControl control : tcsacList) {

            // Start Execution of TestCAseStepActionControl
            long startControl = new Date().getTime();
            DateFormat df = new SimpleDateFormat(DateUtil.DATE_FORMAT_TIMESTAMP);
            long startLongControl = Long.parseLong(df.format(startControl));

            // Clean condition depending on the operatot.
            String condval1 = conditionService.cleanValue1(control.getConditionOperator(), control.getConditionValue1());
            String condval2 = conditionService.cleanValue2(control.getConditionOperator(), control.getConditionValue2());
            String condval3 = conditionService.cleanValue3(control.getConditionOperator(), control.getConditionValue3());

            // Create and Register TestCaseStepActionControlExecution
            LOG.debug("Creating TestCaseStepActionControlExecution");
            TestCaseStepActionControlExecution controlExecution
                    = factoryTestCaseStepActionControlExecution.create(actionExecution.getId(), control.getTest(), control.getTestcase(),
                            control.getStepId(), actionExecution.getIndex(), control.getActionId(), control.getControlId(), control.getSort(),
                            null, null, control.getConditionOperator(), condval1, condval2, condval3, condval1, condval2, condval3,
                            control.getControl(), control.getValue1(), control.getValue2(), control.getValue3(), control.getValue1(), control.getValue2(),
                            control.getValue3(), (control.isFatal() ? "Y" : "N"), startControl, startControl, startLongControl, startLongControl,
                            control.getDescription(), actionExecution, new MessageEvent(MessageEventEnum.CONTROL_PENDING));
            controlExecution.setConditionOptions(control.getConditionOptionsActive());
            controlExecution.setOptions(control.getOptionsActive());
            controlExecution.setDoScreenshotBefore(control.isDoScreenshotBefore());
            controlExecution.setDoScreenshotAfter(control.isDoScreenshotAfter());
            controlExecution.setWaitBefore(control.getWaitBefore());
            controlExecution.setWaitAfter(control.getWaitAfter());
            controlExecution.setTestCaseStepActionControl(control);

            this.testCaseStepActionControlExecutionService.insertTestCaseStepActionControlExecution(controlExecution, execution.getSecrets());

            LOG.debug("Executing control : {} type : {}", controlExecution.getControlId(), controlExecution.getControl());
            execution.addExecutionLog(ExecutionLog.STATUS_INFO, "Executing control : " + controlExecution.getControlId() + " - " + controlExecution.getDescription() + " Control '" + controlExecution.getControl() + "' with '" + controlExecution.getValue1() + "' | '" + controlExecution.getValue2() + "' | '" + controlExecution.getValue3() + "'");

            // We populate the TestCase Control List
            actionExecution.addTestCaseStepActionExecutionList(controlExecution);

            // Evaluate the condition at the control level.
            AnswerItem<Boolean> conditionAnswer;
            boolean conditionDecodeError = false;
            if (!execution.getManualExecution().equals("Y")) {
                try {
                    answerDecode = variableService.decodeStringCompletly(controlExecution.getConditionVal1(), execution, null, false);
                    controlExecution.setConditionVal1(answerDecode.getItem());

                    if (!(answerDecode.isCodeStringEquals("OK"))) {
                        // If anything wrong with the decode --> we stop here with decode message in the action result.
                        controlExecution.setControlResultMessage(answerDecode.getResultMessage().resolveDescription("FIELD", "Control Condition Value1"));
                        controlExecution.setExecutionResultMessage(new MessageGeneral(answerDecode.getResultMessage().getMessage()));
                        controlExecution.setStopExecution(answerDecode.getResultMessage().isStopTest());
                        controlExecution.setEnd(new Date().getTime());
                        LOG.debug("Control interrupted due to decode 'Control Condition Value1' Error.");
                        conditionDecodeError = true;
                    }

                } catch (CerberusEventException cex) {
                    LOG.warn(cex);
                }
                try {
                    answerDecode = variableService.decodeStringCompletly(controlExecution.getConditionVal2(), execution, null, false);
                    controlExecution.setConditionVal2(answerDecode.getItem());

                    if (!(answerDecode.isCodeStringEquals("OK"))) {
                        // If anything wrong with the decode --> we stop here with decode message in the action result.
                        controlExecution.setControlResultMessage(answerDecode.getResultMessage().resolveDescription("FIELD", "Control Condition Value2"));
                        controlExecution.setExecutionResultMessage(new MessageGeneral(answerDecode.getResultMessage().getMessage()));
                        controlExecution.setStopExecution(answerDecode.getResultMessage().isStopTest());
                        controlExecution.setEnd(new Date().getTime());
                        LOG.debug("Control interrupted due to decode 'Control Condition Value2' Error.");
                        conditionDecodeError = true;
                    }
                } catch (CerberusEventException cex) {
                    LOG.warn(cex);
                }
                try {
                    answerDecode = variableService.decodeStringCompletly(controlExecution.getConditionVal3(), execution, null, false);
                    controlExecution.setConditionVal3(answerDecode.getItem());

                    if (!(answerDecode.isCodeStringEquals("OK"))) {
                        // If anything wrong with the decode --> we stop here with decode message in the action result.
                        controlExecution.setControlResultMessage(answerDecode.getResultMessage().resolveDescription("FIELD", "Control Condition Value3"));
                        controlExecution.setExecutionResultMessage(new MessageGeneral(answerDecode.getResultMessage().getMessage()));
                        controlExecution.setStopExecution(answerDecode.getResultMessage().isStopTest());
                        controlExecution.setEnd(new Date().getTime());
                        LOG.debug("Control interrupted due to decode 'Control Condition Value3' Error.");
                        conditionDecodeError = true;
                    }
                } catch (CerberusEventException cex) {
                    LOG.warn(cex);
                }
            }

            if (!(conditionDecodeError)) {

                // Record picture= files at Condition control level.
                Identifier identifier = identifierService.convertStringToIdentifier(controlExecution.getConditionVal1());
                if (identifier.getIdentifier().equals(SikuliService.SIKULI_IDENTIFIER_PICTURE) && !StringUtil.isEmptyOrNull(identifier.getLocator())) {
                    LOG.debug("Saving Image 2 on Action : " + identifier.getLocator());
                    controlExecution.addFileList(recorderService.recordPicture(actionExecution, controlExecution.getControlId(), identifier.getLocator(), "Condition1"));
                }
                identifier = identifierService.convertStringToIdentifier(controlExecution.getConditionVal2());
                if (identifier.getIdentifier().equals(SikuliService.SIKULI_IDENTIFIER_PICTURE) && !StringUtil.isEmptyOrNull(identifier.getLocator())) {
                    LOG.debug("Saving Image 2 on Action : " + identifier.getLocator());
                    controlExecution.addFileList(recorderService.recordPicture(actionExecution, controlExecution.getControlId(), identifier.getLocator(), "Condition2"));
                }

                ConditionOperatorEnum controlConditionOperatorEnum = ConditionOperatorEnum.getConditionOperatorEnumFromString(controlExecution.getConditionOperator());

                conditionAnswer = this.conditionService.evaluateCondition(controlExecution.getConditionOperator(),
                        controlExecution.getConditionVal1(), controlExecution.getConditionVal2(), controlExecution.getConditionVal3(),
                        execution, controlExecution.getConditionOptions());

                boolean doExecuteControl = conditionAnswer.getItem();

                if (execution.getManualExecution().equals("Y") && controlConditionOperatorEnum.isOperatorEvaluationRequired()) {
                    controlExecution.setDescription(controlExecution.getDescription() + " - " + conditionAnswer.getMessageDescription());
                }

                // If condition OK or if manual execution, then execute the control
                if (conditionAnswer.getResultMessage().getMessage().getCodeString().equals("PE")
                        || execution.getManualExecution().equals("Y")) {

                    if (doExecuteControl || execution.getManualExecution().equals("Y")) {

                        // We execute the control
                        controlExecution = executeControl(controlExecution, execution);

                        /*
                         * We update the Action with the execution message and
                         * stop flag from the control. We update the status only
                         * if the control is not OK. This is to prevent moving
                         * the status to OK when it should stay KO when a
                         * control failed previously.
                         */
                        actionExecution.setStopExecution(controlExecution.isStopExecution());
                        if (!(controlExecution.getControlResultMessage().equals(new MessageEvent(MessageEventEnum.CONTROL_SUCCESS)))) {
                            //NA is a special case of not having success while calculating the property; the action shouldn't be stopped
                            if (controlExecution.getControlResultMessage().equals(new MessageEvent(MessageEventEnum.PROPERTY_FAILED_NO_PROPERTY_DEFINITION))) {
                                // restores the messages' information if the property is not defined for the country
                                actionExecution.setActionResultMessage(actionMessage);
                                actionExecution.setExecutionResultMessage(executionResultMessage);
                            } else {
                                actionExecution.setExecutionResultMessage(controlExecution.getExecutionResultMessage());
                                actionExecution.setActionResultMessage(controlExecution.getControlResultMessage());
                            }
                        }
                        //If Control report stopping the testcase, we stop it.
                        if (controlExecution.isStopExecution()) {
                            break;
                        }

                    } else { // We don't execute the control and record a generic execution.

                        //Record Screenshot, PageSource
                        controlExecution.addFileList(recorderService.recordExecutionInformationAfterStepActionAndControl(controlExecution.getTestCaseStepActionExecution(), controlExecution));

                        // Register Control in database
                        LOG.debug("Registering Control : {}", controlExecution.getControlId());

                        // We change the Action message only if the action is not executed due to condition.
                        MessageEvent controlMes = new MessageEvent(MessageEventEnum.CONDITION_TESTCASECONTROL_NOTEXECUTED);
                        controlExecution.setControlResultMessage(controlMes);
                        controlExecution.setReturnMessage(controlExecution.getReturnMessage()
                                .replace("%COND%", controlExecution.getConditionOperator())
                                .replace("%MESSAGE%", conditionAnswer.getResultMessage().getDescription())
                        );

                        controlExecution.setEnd(new Date().getTime());
                        this.testCaseStepActionControlExecutionService.updateTestCaseStepActionControlExecution(controlExecution, execution.getSecrets());
                        LOG.debug("Registered Control");

                        // Websocket --> we refresh the corresponding Detail Execution pages attached to this execution.
                        updateExecutionWebSocketOnly(execution, false);

                    }
                } else {
                    // Error when performing the condition evaluation. We force no execution (false)
                    MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.EXECUTION_FA_CONDITION);
                    mes.setDescription(mes.getDescription()
                            .replace("%COND%", controlExecution.getConditionOperator())
                            .replace("%AREA%", "control ")
                            .replace("%MES%", conditionAnswer.getResultMessage().getDescription()));
                    controlExecution.setExecutionResultMessage(mes);
                    actionExecution.setExecutionResultMessage(mes);

                    controlExecution.setControlResultMessage(new MessageEvent(MessageEventEnum.CONDITION_TESTCASECONTROL_FAILED)
                            .resolveDescription("AREA", "")
                            .resolveDescription("COND", controlExecution.getConditionOperator())
                            .resolveDescription("MESSAGE", conditionAnswer.getResultMessage().getDescription()));

                    actionExecution.setActionResultMessage(new MessageEvent(MessageEventEnum.CONDITION_TESTCASEACTION_FAILED)
                            .resolveDescription("AREA", "control ")
                            .resolveDescription("COND", controlExecution.getConditionOperator())
                            .resolveDescription("MESSAGE", conditionAnswer.getResultMessage().getDescription()));

                    controlExecution.setEnd(new Date().getTime());

                    this.testCaseStepActionControlExecutionService.updateTestCaseStepActionControlExecution(controlExecution, execution.getSecrets());
                    LOG.debug("Control interrupted due to condition error.");
                    // We stop any further Control execution.
                    break;
                }
            } else {

                controlExecution.setEnd(new Date().getTime());
                actionExecution.setExecutionResultMessage(controlExecution.getExecutionResultMessage());
                actionExecution.setActionResultMessage(controlExecution.getControlResultMessage());
                this.testCaseStepActionControlExecutionService.updateTestCaseStepActionControlExecution(controlExecution, execution.getSecrets());
                LOG.debug("Registered Control");

                // Websocket --> we refresh the corresponding Detail Execution pages attached to this execution.
                updateExecutionWebSocketOnly(execution, false);
            }

            // log TestCaseStepActionControlExecution
            if ((execution.getVerbose() > 0) && parameterService.getParameterBooleanByKey("cerberus_executionlog_enable", execution.getSystem(), false)) {
                LOG.info(controlExecution.toJson(false, true, execution.getSecrets()));
            }

        }

        /*
         * All controls of the actions are done. We now put back the
         * AppTypeEngine value to the one from the application. and also put
         * back the last service called content and format.
         */
        execution.setAppTypeEngine(execution.getApplicationObj().getType());
        if (execution.getLastServiceCalled() != null) {
            execution.getLastServiceCalled().setResponseHTTPBody(execution.getOriginalLastServiceCalled());
            execution.getLastServiceCalled().setResponseHTTPBodyContentType(execution.getOriginalLastServiceCalledContent());
            execution.getLastServiceCalled().setRecordTraceFile(true);
        }

        /*
         * Reset the current application back to null so that the main application will be used.
         */
        execution.setCurrentApplication(null);

        // Websocket --> we refresh the corresponding Detail Execution pages attached to this execution.
        updateExecutionWebSocketOnly(execution, false);

        LOG.debug("Finished execute Action : {}", actionExecution.getAction());
        return actionExecution;

    }

    private TestCaseStepActionControlExecution executeControl(TestCaseStepActionControlExecution controlExecution, TestCaseExecution execution) {

        // If execution is not manual, do control and record files
        if (!execution.getManualExecution().equals("Y")) {

            // Record Screenshot
            try {
                controlExecution.addFileList(recorderService.recordExecutionInformationBeforeStepActionAndControl(controlExecution.getTestCaseStepActionExecution(), controlExecution));
            } catch (Exception ex) {
                LOG.warn("Unable to record Screenshot Before Control : {}", ex.toString(), ex);
            }

            controlExecution = this.controlService.doControl(controlExecution);

            // Record Screenshot, PageSource
            controlExecution.addFileList(recorderService.recordExecutionInformationAfterStepActionAndControl(controlExecution.getTestCaseStepActionExecution(), controlExecution));

        } else {
            // If execution manual, set Control result message as notExecuted
            controlExecution.setControlResultMessage(new MessageEvent(MessageEventEnum.CONTROL_WAITINGEXECUTION));
            controlExecution.setExecutionResultMessage(new MessageGeneral(MessageGeneralEnum.EXECUTION_WE));
            controlExecution.setEnd(new Date().getTime());
        }

        // Register Control in database
        LOG.debug("Registering Control : {}", controlExecution.getControlId());
        this.testCaseStepActionControlExecutionService.updateTestCaseStepActionControlExecution(controlExecution, execution.getSecrets());
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
                LOG.debug("Stop server for execution {}", execution.getId());
            } catch (WebDriverException exception) {
                LOG.warn("Selenium/Appium didn't manage to close connection for execution {}", execution.getId(), exception);
            }
            break;
            case Application.TYPE_FAT:
                LOG.debug("Stop Sikuli server for execution {} closing application {}", execution.getId(), execution.getCountryEnvApplicationParam().getIp());
                if (!StringUtil.isEmptyOrNull(execution.getCountryEnvApplicationParam().getIp())) {
                    this.sikuliService.doSikuliActionCloseApp(execution.getSession(), execution.getCountryEnvApplicationParam().getIp());
                }
                LOG.debug("Ask Sikuli to clean execution {}", execution.getId());
                this.sikuliService.doSikuliEndExecution(execution.getSession());
                break;
            default:
        }

        // Stopping remote proxy.
        try {
            this.executorService.stopRemoteProxy(execution);
            LOG.debug("Stop Cerberus Robot Proxy for execution {}", execution.getId());
        } catch (Exception exception) {
            LOG.warn("Exception on Cerberus Robot Proxy stop for execution {}", execution.getId(), exception);
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
