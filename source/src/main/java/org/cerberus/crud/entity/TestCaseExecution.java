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
package org.cerberus.crud.entity;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.Map;
import org.apache.kafka.common.TopicPartition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.engine.entity.MessageGeneral;
import org.cerberus.engine.entity.Selenium;
import org.cerberus.engine.entity.Session;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author bcivel
 */
public class TestCaseExecution {

    private static final Logger LOG = LogManager.getLogger(TestCaseExecution.class);

    private long id;
    private String system;
    private String test;
    private String testCase;
    private String description;
    private String build;
    private String revision;
    private String environment;
    private String environmentData;
    private String country;
    private String robot;
    private String robotExecutor;
    private String robotHost; // Host the Selenium IP
    private String robotPort; // host the Selenium Port
    private String robotDecli;
    private String robotProvider;
    private String robotSessionID;
    private String browser;
    private String version;
    private String platform;
    private long start;
    private long end;
    private String controlStatus;
    private String controlMessage;
    private String application;
    private String url;
    private String tag;
    private String status;
    private String crbVersion;
    private String executor;
    private String screenSize;
    private String conditionOperator;
    private String conditionVal1Init;
    private String conditionVal2Init;
    private String conditionVal3Init;
    private String conditionVal1;
    private String conditionVal2;
    private String conditionVal3;
    private String manualExecution;
    private String userAgent;
    private long queueID;
    private String UsrCreated;
    private Timestamp DateCreated;
    private String UsrModif;
    private Timestamp DateModif;
    private int testCaseVersion;
    private int testCasePriority;

    /**
     * From here are data outside database model.
     */
    // Execution Parameters
    private String queueState;
    private int verbose;
    private int screenshot;
    private String outputFormat;
    private boolean manualURL;
    private String myHost;
    private String myContextRoot;
    private String myLoginRelativeURL;
    private String seleniumIP;
    private String seleniumIPUser;
    private String seleniumIPPassword;
    private String seleniumPort;
    private Integer pageSource;
    private Integer seleniumLog;
    private Integer numberOfRetries;
    private boolean synchroneous;
    private String timeout;
    // Objects.
    private TestCaseExecutionQueue testCaseExecutionQueue;
    private Application applicationObj;
    // App Type that is used by the engine to interpret the context. By defaut it is linked to the Type of the application but it can be temporary switch to a different type.
    private String appTypeEngine;
    private Invariant CountryObj;
    private Test testObj;
    private TestCase testCaseObj;
    private Tag tagObj;
    private CountryEnvParam countryEnvParam;
    private CountryEnvironmentParameters countryEnvironmentParameters;
    private Invariant environmentDataObj;
    // Host the list of the files stored at execution level
    private List<TestCaseExecutionFile> fileList;
    // Host the list of Steps that will be executed (both pre tests and main test)
    private List<TestCaseStepExecution> testCaseStepExecutionList;
    // Host the full list of data calculated during the execution.
    private TreeMap<String, TestCaseExecutionData> testCaseExecutionDataMap;
    // This is used to keep track of all property calculated within a step/action/control. It is reset each time we enter a step/action/control and the property name is added to the list each time it gets calculated. In case it was already asked for calculation, we stop the execution with FA message.
    private List<String> recursiveAlreadyCalculatedPropertiesList;
    private List<TestCaseCountryProperties> testCaseCountryPropertyList;

    private List<TestCaseExecutionQueueDep> testCaseExecutionQueueDepList;

    private List<String> videos;

    // Used in reporting page to report the previous executions from the same tag.
    private long previousExeId;
    private String previousExeStatus;

    // Others
    private MessageGeneral resultMessage;
    private String executionUUID;
    private Selenium selenium;
    private Session session;
    private Robot robotObj;
    private RobotExecutor robotExecutorObj;
    private AppService lastServiceCalled;
    private String originalLastServiceCalled; // Used in order to save the last call when using the action setServiceCallContent.
    private String originalLastServiceCalledContent; // Used in order to save the last call when using the action setServiceCallContent.
    private Integer nbExecutions; // Has the nb of execution that was necessary to execute the testcase.
    // Global parameters.
    private Integer cerberus_action_wait_default;
    private boolean cerberus_featureflipping_activatewebsocketpush;
    private long cerberus_featureflipping_websocketpushperiod;
    private long lastWebsocketPush;
    // Remote Proxy data.
    private boolean remoteProxyStarted;
    private Integer remoteProxyPort;
    private String remoteProxyUUID;
    private String remoteProxyLastHarMD5;
    // Kafka Consumers
    private HashMap<String, Map<TopicPartition, Long>> kafkaLatestOffset;
    // Http Stats
    private TestCaseExecutionHttpStat httpStat;

    /**
     * Invariant PROPERTY TYPE String.
     */
    public static final String CONTROLSTATUS_OK = "OK"; // Test executed and everything was fine.
    public static final String CONTROLSTATUS_OK_COL = "#5CB85C"; // Test executed and everything was fine.
    public static final String CONTROLSTATUS_KO = "KO"; // Test executed and 1 control has reported a bug. --> Ticket to be open for dev team.
    public static final String CONTROLSTATUS_KO_COL = "#D9534F"; // Test executed and 1 control has reported a bug. --> Ticket to be open for dev team.
    public static final String CONTROLSTATUS_FA = "FA"; // Test failed to be executed. --> Problem is in the test itself.
    public static final String CONTROLSTATUS_FA_COL = "#F0AD4E"; // Test failed to be executed. --> Problem is in the test itself.
    public static final String CONTROLSTATUS_NA = "NA"; // Test could not be executed because no data could be retreive for testing.
    public static final String CONTROLSTATUS_NA_COL = "#F1C40F"; // Test could not be executed because no data could be retreive for testing.
    public static final String CONTROLSTATUS_NE = "NE"; // Test was not executed.
    public static final String CONTROLSTATUS_NE_COL = "#aaa"; // Test was not executed.
    public static final String CONTROLSTATUS_WE = "WE"; // Test is waiting for a manual testing.
    public static final String CONTROLSTATUS_WE_COL = "#34495E"; // Test is waiting for a manual testing.
    public static final String CONTROLSTATUS_PE = "PE"; // Test is currently beeing executed.
    public static final String CONTROLSTATUS_PE_COL = "#3498DB"; // Test is currently beeing executed.
    public static final String CONTROLSTATUS_CA = "CA"; // Test has been cancelled by user.
    public static final String CONTROLSTATUS_CA_COL = "#F0AD4E"; // Test has been cancelled by user.
    public static final String CONTROLSTATUS_QU = "QU"; // Test is still waiting in queue.
    public static final String CONTROLSTATUS_QU_COL = "#BF00BF"; // Test is still waiting in queue.
    public static final String CONTROLSTATUS_QE = "QE"; // Test is stuck in Queue.
    public static final String CONTROLSTATUS_QE_COL = "#5C025C"; // Test is stuck in Queue.

    public enum ControlStatus {
        OK, KO, FA, NA, NE, WE, PE, CA, QU, QE
    };

    public static final String MANUAL_Y = "Y";
    public static final String MANUAL_N = "N";
    public static final String MANUAL_A = "A";

    public static final String ROBOTPROVIDER_BROWSERSTACK = "BROWSERSTACK";
    public static final String ROBOTPROVIDER_KOBITON = "KOBITON";
    public static final String ROBOTPROVIDER_NONE = "NONE";

    public TestCaseExecutionHttpStat getHttpStat() {
        return httpStat;
    }

    public void setHttpStat(TestCaseExecutionHttpStat httpStat) {
        this.httpStat = httpStat;
    }

    public String getAppTypeEngine() {
        return appTypeEngine;
    }

    public void setAppTypeEngine(String appTypeEngine) {
        this.appTypeEngine = appTypeEngine;
    }

    public HashMap<String, Map<TopicPartition, Long>> getKafkaLatestOffset() {
        return kafkaLatestOffset;
    }

    public void setKafkaLatestOffset(HashMap<String, Map<TopicPartition, Long>> kafkaLatestOffset) {
        this.kafkaLatestOffset = kafkaLatestOffset;
    }

    public boolean isRemoteProxyStarted() {
        return remoteProxyStarted;
    }

    public void setRemoteProxyStarted(boolean remoteProxyStarted) {
        this.remoteProxyStarted = remoteProxyStarted;
    }

    public String getRobotSessionID() {
        return robotSessionID;
    }

    public void setRobotSessionID(String robotSessionID) {
        this.robotSessionID = robotSessionID;
    }

    public String getRobotProvider() {
        return robotProvider;
    }

    public void setRobotProvider(String robotProvider) {
        this.robotProvider = robotProvider;
    }

    public long getPreviousExeId() {
        return previousExeId;
    }

    public void setPreviousExeId(long previousExeId) {
        this.previousExeId = previousExeId;
    }

    public String getPreviousExeStatus() {
        return previousExeStatus;
    }

    public void setPreviousExeStatus(String previousExeStatus) {
        this.previousExeStatus = previousExeStatus;
    }

    public RobotExecutor getRobotExecutorObj() {
        return robotExecutorObj;
    }

    public void setRobotExecutorObj(RobotExecutor robotExecutorObj) {
        this.robotExecutorObj = robotExecutorObj;
    }

    public Robot getRobotObj() {
        return robotObj;
    }

    public void setRobotObj(Robot robotObj) {
        this.robotObj = robotObj;
    }

    public String getSystem() {
        return system;
    }

    public void setSystem(String system) {
        this.system = system;
    }

    public String getRobotDecli() {
        return robotDecli;
    }

    public void setRobotDecli(String robotDecli) {
        this.robotDecli = robotDecli;
    }

    public Integer getNbExecutions() {
        return nbExecutions;
    }

    public void setNbExecutions(Integer nbExecutions) {
        this.nbExecutions = nbExecutions;
    }

    public String getQueueState() {
        return queueState;
    }

    public void setQueueState(String queueState) {
        this.queueState = queueState;
    }

    public TestCaseExecutionQueue getTestCaseExecutionQueue() {
        return testCaseExecutionQueue;
    }

    public void setTestCaseExecutionQueue(TestCaseExecutionQueue testCaseExecutionQueue) {
        this.testCaseExecutionQueue = testCaseExecutionQueue;
    }

    public String getUsrCreated() {
        return UsrCreated;
    }

    public void setUsrCreated(String UsrCreated) {
        this.UsrCreated = UsrCreated;
    }

    public Timestamp getDateCreated() {
        return DateCreated;
    }

    public void setDateCreated(Timestamp DateCreated) {
        this.DateCreated = DateCreated;
    }

    public String getUsrModif() {
        return UsrModif;
    }

    public void setUsrModif(String UsrModif) {
        this.UsrModif = UsrModif;
    }

    public Timestamp getDateModif() {
        return DateModif;
    }

    public void setDateModif(Timestamp DateModif) {
        this.DateModif = DateModif;
    }

    public long getQueueID() {
        return queueID;
    }

    public void setQueueID(long queueID) {
        this.queueID = queueID;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getRecursiveAlreadyCalculatedPropertiesList() {
        return recursiveAlreadyCalculatedPropertiesList;
    }

    public void setRecursiveAlreadyCalculatedPropertiesList(List<String> recursiveAlreadyCalculatedPropertiesList) {
        this.recursiveAlreadyCalculatedPropertiesList = recursiveAlreadyCalculatedPropertiesList;
    }

    public TreeMap<String, TestCaseExecutionData> getTestCaseExecutionDataMap() {
        return testCaseExecutionDataMap;
    }

    public void setTestCaseExecutionDataMap(TreeMap<String, TestCaseExecutionData> testCaseExecutionDataMap) {
        this.testCaseExecutionDataMap = testCaseExecutionDataMap;
    }

    public AppService getLastServiceCalled() {
        return lastServiceCalled;
    }

    public void setLastServiceCalled(AppService lastServiceCalled) {
        this.lastServiceCalled = lastServiceCalled;
    }

    public String getOriginalLastServiceCalled() {
        return originalLastServiceCalled;
    }

    public void setOriginalLastServiceCalled(String originalLastServiceCalled) {
        LOG.debug("TOTO set.");
        this.originalLastServiceCalled = originalLastServiceCalled;
    }

    public String getOriginalLastServiceCalledContent() {
        return originalLastServiceCalledContent;
    }

    public void setOriginalLastServiceCalledContent(String originalLastServiceCalledContent) {
        this.originalLastServiceCalledContent = originalLastServiceCalledContent;
    }

    public long getLastWebsocketPush() {
        return lastWebsocketPush;
    }

    public void setLastWebsocketPush(long lastWebsocketPush) {
        this.lastWebsocketPush = lastWebsocketPush;
    }

    public String getConditionOperator() {
        return conditionOperator;
    }

    public void setConditionOperator(String conditionOperator) {
        this.conditionOperator = conditionOperator;
    }

    public String getConditionVal1Init() {
        return conditionVal1Init;
    }

    public void setConditionVal1Init(String conditionVal1Init) {
        this.conditionVal1Init = conditionVal1Init;
    }

    public String getConditionVal2Init() {
        return conditionVal2Init;
    }

    public void setConditionVal2Init(String conditionVal2Init) {
        this.conditionVal2Init = conditionVal2Init;
    }

    public String getConditionVal3Init() {
        return conditionVal3Init;
    }

    public void setConditionVal3Init(String conditionVal3Init) {
        this.conditionVal3Init = conditionVal3Init;
    }

    public String getConditionVal1() {
        return conditionVal1;
    }

    public void setConditionVal1(String conditionVal1) {
        this.conditionVal1 = conditionVal1;
    }

    public String getConditionVal2() {
        return conditionVal2;
    }

    public void setConditionVal2(String conditionVal2) {
        this.conditionVal2 = conditionVal2;
    }

    public String getConditionVal3() {
        return conditionVal3;
    }

    public void setConditionVal3(String conditionVal3) {
        this.conditionVal3 = conditionVal3;
    }

    public long getCerberus_featureflipping_websocketpushperiod() {
        return cerberus_featureflipping_websocketpushperiod;
    }

    public void setCerberus_featureflipping_websocketpushperiod(long cerberus_featureflipping_websocketpushperiod) {
        this.cerberus_featureflipping_websocketpushperiod = cerberus_featureflipping_websocketpushperiod;
    }

    public boolean isCerberus_featureflipping_activatewebsocketpush() {
        return cerberus_featureflipping_activatewebsocketpush;
    }

    public void setCerberus_featureflipping_activatewebsocketpush(boolean cerberus_featureflipping_activatewebsocketpush) {
        this.cerberus_featureflipping_activatewebsocketpush = cerberus_featureflipping_activatewebsocketpush;
    }

    public Integer getCerberus_action_wait_default() {
        return cerberus_action_wait_default;
    }

    public void setCerberus_action_wait_default(Integer cerberus_action_wait_default) {
        this.cerberus_action_wait_default = cerberus_action_wait_default;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public Integer getNumberOfRetries() {
        return numberOfRetries;
    }

    public void setNumberOfRetries(Integer numberOfRetries) {
        this.numberOfRetries = numberOfRetries;
    }

    public void decreaseNumberOfRetries() {
        this.numberOfRetries--;
    }

    public List<TestCaseCountryProperties> getTestCaseCountryPropertyList() {
        return testCaseCountryPropertyList;
    }

    public void setTestCaseCountryPropertyList(List<TestCaseCountryProperties> testCaseCountryPropertyList) {
        this.testCaseCountryPropertyList = testCaseCountryPropertyList;
    }

    public String getManualExecution() {
        return manualExecution;
    }

    public void setManualExecution(String manualExecution) {
        this.manualExecution = manualExecution;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public Integer getPageSource() {
        return pageSource;
    }

    public void setPageSource(Integer pageSource) {
        this.pageSource = pageSource;
    }

    public Integer getSeleniumLog() {
        return seleniumLog;
    }

    public void setSeleniumLog(Integer seleniumLog) {
        this.seleniumLog = seleniumLog;
    }

    public boolean isSynchroneous() {
        return synchroneous;
    }

    public void setSynchroneous(boolean synchroneous) {
        this.synchroneous = synchroneous;
    }

    public String getTimeout() {
        return timeout;
    }

    public void setTimeout(String timeout) {
        this.timeout = timeout;
    }

    public String getExecutionUUID() {
        return executionUUID;
    }

    public void setExecutionUUID(String executionUUID) {
        this.executionUUID = executionUUID;
    }

    public Selenium getSelenium() {
        return selenium;
    }

    public void setSelenium(Selenium selenium) {
        this.selenium = selenium;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public Invariant getCountryObj() {
        return CountryObj;
    }

    public void setCountryObj(Invariant CountryObj) {
        this.CountryObj = CountryObj;
    }

    public Invariant getEnvironmentDataObj() {
        return environmentDataObj;
    }

    public void setEnvironmentDataObj(Invariant environmentDataObj) {
        this.environmentDataObj = environmentDataObj;
    }

    public String getEnvironmentData() {
        return environmentData;
    }

    public void setEnvironmentData(String environmentData) {
        this.environmentData = environmentData;
    }

    public boolean isManualURL() {
        return manualURL;
    }

    public void setManualURL(boolean manualURL) {
        this.manualURL = manualURL;
    }

    public String getMyHost() {
        return myHost;
    }

    public void setMyHost(String myHost) {
        this.myHost = myHost;
    }

    public String getMyContextRoot() {
        return myContextRoot;
    }

    public void setMyContextRoot(String myContextRoot) {
        this.myContextRoot = myContextRoot;
    }

    public String getMyLoginRelativeURL() {
        return myLoginRelativeURL;
    }

    public void setMyLoginRelativeURL(String myLoginRelativeURL) {
        this.myLoginRelativeURL = myLoginRelativeURL;
    }

    public String getOutputFormat() {
        return outputFormat;
    }

    public void setOutputFormat(String outputFormat) {
        this.outputFormat = outputFormat;
    }

    public int getScreenshot() {
        return screenshot;
    }

    public void setScreenshot(int screenshot) {
        this.screenshot = screenshot;
    }

    public MessageGeneral getResultMessage() {
        return resultMessage;
    }

    public void setResultMessage(MessageGeneral resultMessage) {
        this.resultMessage = resultMessage;
        if (resultMessage != null) {
            this.setControlMessage(resultMessage.getDescription());
            this.setControlStatus(resultMessage.getCodeString());
        }
    }

    public List<TestCaseExecutionFile> getFileList() {
        return fileList;
    }

    public void setFileList(List<TestCaseExecutionFile> fileList) {
        this.fileList = fileList;
    }

    public void addFileList(TestCaseExecutionFile file) {
        if (file != null) {
            this.fileList.add(file);
        }
    }

    public void addFileList(List<TestCaseExecutionFile> fileList) {
        if (fileList != null) {
            for (TestCaseExecutionFile testCaseExecutionFile : fileList) {
                this.fileList.add(testCaseExecutionFile);
            }
        }
    }

    public List<TestCaseStepExecution> getTestCaseStepExecutionList() {
        return testCaseStepExecutionList;
    }

    public void setTestCaseStepExecutionList(List<TestCaseStepExecution> testCaseStepExecutionList) {
        this.testCaseStepExecutionList = testCaseStepExecutionList;
    }

    public void addTestCaseStepExecutionList(TestCaseStepExecution testCaseStepExecution) {
        if (testCaseStepExecution != null) {
            this.testCaseStepExecutionList.add(testCaseStepExecution);
        }
    }

    public void addTestCaseStepExecutionList(List<TestCaseStepExecution> testCaseStepExecutionList) {
        if (testCaseStepExecutionList != null) {
            for (TestCaseStepExecution testCaseStepExecution : testCaseStepExecutionList) {
                this.testCaseStepExecutionList.add(testCaseStepExecution);
            }
        }
    }

    public String getSeleniumIPUser() {
        return seleniumIPUser;
    }

    public void setSeleniumIPUser(String seleniumIPUser) {
        this.seleniumIPUser = seleniumIPUser;
    }

    public String getSeleniumIPPassword() {
        return seleniumIPPassword;
    }

    public void setSeleniumIPPassword(String seleniumIPPassword) {
        this.seleniumIPPassword = seleniumIPPassword;
    }

    public String getSeleniumIP() {
        return seleniumIP;
    }

    public void setSeleniumIP(String seleniumIP) {
        this.seleniumIP = seleniumIP;
    }

    public String getSeleniumPort() {
        return seleniumPort;
    }

    public void setSeleniumPort(String seleniumPort) {
        this.seleniumPort = seleniumPort;
    }

    public CountryEnvParam getCountryEnvParam() {
        return countryEnvParam;
    }

    public void setCountryEnvParam(CountryEnvParam countryEnvParam) {
        this.countryEnvParam = countryEnvParam;
    }

    public CountryEnvironmentParameters getCountryEnvironmentParameters() {
        return countryEnvironmentParameters;
    }

    public void setCountryEnvironmentParameters(CountryEnvironmentParameters countryEnvironmentParameters) {
        this.countryEnvironmentParameters = countryEnvironmentParameters;
    }

    public Test getTestObj() {
        return testObj;
    }

    public void setTestObj(Test testObj) {
        this.testObj = testObj;
    }

    public TestCase getTestCaseObj() {
        return testCaseObj;
    }

    public void setTestCaseObj(TestCase testCase) {
        this.testCaseObj = testCase;
    }

    public Application getApplicationObj() {
        return applicationObj;
    }

    public void setApplicationObj(Application applicationObj) {
        this.applicationObj = applicationObj;
    }

    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    public String getBuild() {
        return build;
    }

    public void setBuild(String build) {
        this.build = build;
    }

    public String getControlMessage() {
        return controlMessage;
    }

    public void setControlMessage(String controlMessage) {
        this.controlMessage = controlMessage;
    }

    public String getControlStatus() {
        return controlStatus;
    }

    public void setControlStatus(String controlStatus) {
        this.controlStatus = controlStatus;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCrbVersion() {
        return crbVersion;
    }

    public void setCrbVersion(String crbVersion) {
        this.crbVersion = crbVersion;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getRobot() {
        return robot;
    }

    public void setRobot(String robot) {
        this.robot = robot;
    }

    public String getRobotExecutor() {
        return robotExecutor;
    }

    public void setRobotExecutor(String robotExecutor) {
        this.robotExecutor = robotExecutor;
    }

    public String getRobotHost() {
        return robotHost;
    }

    public void setRobotHost(String robotHost) {
        this.robotHost = robotHost;
    }

    public String getRobotPort() {
        return robotPort;
    }

    public void setRobotPort(String robotPort) {
        this.robotPort = robotPort;
    }

    public String getRevision() {
        return revision;
    }

    public void setRevision(String revision) {
        this.revision = revision;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public Tag getTagObj() {
        return tagObj;
    }

    public void setTagObj(Tag tagObj) {
        this.tagObj = tagObj;
    }

    public String getTest() {
        return test;
    }

    public void setTest(String test) {
        this.test = test;
    }

    public String getTestCase() {
        return testCase;
    }

    public void setTestCase(String testCase) {
        this.testCase = testCase;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getVerbose() {
        return verbose;
    }

    public void setVerbose(int verbose) {
        this.verbose = verbose;
    }

    public String getExecutor() {
        return executor;
    }

    public void setExecutor(String executor) {
        this.executor = executor;
    }

    public String getScreenSize() {
        return screenSize;
    }

    public void setScreenSize(String screenSize) {
        this.screenSize = screenSize;
    }

    public int getTestCaseVersion() {
        return this.testCaseVersion;
    }

    public void setTestCaseVersion(int testCaseVersion) {
        this.testCaseVersion = testCaseVersion;
    }

    public int getTestCasePriority() {
        return testCasePriority;
    }

    public void setTestCasePriority(int testCasePriority) {
        this.testCasePriority = testCasePriority;
    }

    public List<String> getVideos() {
        return videos;
    }

    public void setVideos(List<String> videos) {
        this.videos = videos;
    }

    public List<TestCaseExecutionQueueDep> getTestCaseExecutionQueueDepList() {
        return testCaseExecutionQueueDepList;
    }

    public void setTestCaseExecutionQueueDep(List<TestCaseExecutionQueueDep> testCaseExecutionQueueDep) {
        this.testCaseExecutionQueueDepList = testCaseExecutionQueueDep;
    }

    public Integer getRemoteProxyPort() {
        return remoteProxyPort;
    }

    public void setRemoteProxyPort(Integer remoteProxyPort) {
        this.remoteProxyPort = remoteProxyPort;
    }

    public String getRemoteProxyUUID() {
        return remoteProxyUUID;
    }

    public void setRemoteProxyUUID(String remoteProxyUUID) {
        this.remoteProxyUUID = remoteProxyUUID;
    }

    public String getRemoteProxyLastHarMD5() {
        return remoteProxyLastHarMD5;
    }

    public void setRemoteProxyLastHarMD5(String remoteProxyLastHarMD5) {
        this.remoteProxyLastHarMD5 = remoteProxyLastHarMD5;
    }

    /**
     * Convert the current TestCaseExecution into JSON format
     *
     * @param withChilds boolean that define if childs should be included
     * @return TestCaseExecution in JSONObject format
     */
    public JSONObject toJson(boolean withChilds) {
        JSONObject result = new JSONObject();
        try {
            result.put("type", "testCaseExecution");
            result.put("id", this.getId());
            result.put("test", this.getTest());
            result.put("testcase", this.getTestCase());
            result.put("description", this.getDescription());
            result.put("build", this.getBuild());
            result.put("revision", this.getRevision());
            result.put("environment", this.getEnvironment());
            result.put("environmentData", this.getEnvironmentData());
            result.put("country", this.getCountry());
            result.put("browser", this.getBrowser());
            result.put("version", this.getVersion());
            result.put("platform", this.getPlatform());
            result.put("start", this.getStart());
            result.put("end", this.getEnd());
            result.put("controlStatus", this.getControlStatus());
            result.put("controlMessage", this.getControlMessage());
            result.put("application", this.getApplication());
            result.put("robot", this.getRobot());
            result.put("robotExecutor", this.getRobotExecutor());
            result.put("robotHost", this.getRobotHost());
            result.put("robotPort", this.getRobotPort());
            result.put("url", this.getUrl());
            result.put("tag", this.getTag());
            result.put("verbose", this.getVerbose());
            result.put("status", this.getStatus());
            result.put("crbVersion", this.getCrbVersion());
            result.put("executor", this.getExecutor());
            result.put("screenSize", this.getScreenSize());
            result.put("conditionOperator", this.getConditionOperator());
            result.put("conditionVal1Init", this.getConditionVal1Init());
            result.put("conditionVal2Init", this.getConditionVal2Init());
            result.put("conditionVal3Init", this.getConditionVal3Init());
            result.put("conditionVal1", this.getConditionVal1());
            result.put("conditionVal2", this.getConditionVal2());
            result.put("conditionVal3", this.getConditionVal3());
            result.put("userAgent", this.getUserAgent());
            result.put("queueId", this.getQueueID());
            result.put("manualExecution", this.getManualExecution());
            result.put("testCaseVersion", this.getTestCaseVersion());
            result.put("system", this.getSystem());
            result.put("robotDecli", this.getRobotDecli());
            result.put("robotProvider", this.getRobotProvider());
            result.put("robotSessionId", this.getRobotSessionID());
            result.put("videos", this.getVideos());
            result.put("previousExeId", this.getPreviousExeId());
            result.put("previousExeStatus", this.getPreviousExeStatus());

            result.put("usrCreated", this.getUsrCreated());
            result.put("dateCreated", this.getDateCreated());
            result.put("usrModif", this.getUsrModif());
            result.put("dateModif", this.getDateModif());

            if (withChilds) {
                // Looping on ** Step **
                JSONArray array = new JSONArray();
                if (this.getTestCaseStepExecutionList() != null) {
                    for (Object testCaseStepExecution : this.getTestCaseStepExecutionList()) {
                        array.put(((TestCaseStepExecution) testCaseStepExecution).toJson(true, false));
                    }
                }
                result.put("testCaseStepExecutionList", array);

                array = new JSONArray();
                if (this.getTestCaseExecutionQueueDepList() != null) {
                    for (Object tceQDep : this.getTestCaseExecutionQueueDepList()) {
                        array.put(((TestCaseExecutionQueueDep) tceQDep).toJson());
                    }
                }
                result.put("testCaseExecutionQueueDepList", array);

                // ** TestCase **
                if (this.getTestCaseObj() != null) {
                    TestCase tc = this.getTestCaseObj();
                    result.put("testCaseObj", tc.toJson());
                }

                // ** Tag **
                if (this.getTagObj() != null) {
                    Tag tagO = this.getTagObj();
                    result.put("tagObj", tagO.toJsonLight());
                }

                // Looping on ** Execution Data **
                array = new JSONArray();
                for (String key1 : this.getTestCaseExecutionDataMap().keySet()) {
                    TestCaseExecutionData tced = (TestCaseExecutionData) this.getTestCaseExecutionDataMap().get(key1);
                    array.put((tced).toJson(true, false));
                }
                result.put("testCaseExecutionDataList", array);

                // Looping on ** Media File Execution **
                array = new JSONArray();
                if (this.getFileList() != null) {
                    for (Object testCaseFileExecution : this.getFileList()) {
                        array.put(((TestCaseExecutionFile) testCaseFileExecution).toJson());
                    }
                }
                result.put("fileList", array);

                if (this.getHttpStat() != null) {
                    result.put("httpStat", this.getHttpStat().toJson());
                }

            }

        } catch (JSONException ex) {
            LOG.error(ex.toString(), ex);
        } catch (Exception ex) {
            LOG.error(ex.toString(), ex);
        }
        return result;
    }

}
