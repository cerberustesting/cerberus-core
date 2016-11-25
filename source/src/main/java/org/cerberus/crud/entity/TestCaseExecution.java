/* DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
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

import org.cerberus.engine.entity.MessageGeneral;
import org.cerberus.engine.entity.Session;
import org.cerberus.engine.entity.Selenium;
import java.util.List;
import org.apache.log4j.Logger;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author bcivel
 */
public class TestCaseExecution {

    private long id;
    private String test;
    private String testCase;
    private String build;
    private String revision;
    private String environment;
    private String country;
    private String browser;
    private String version;
    private String platform;
    private String browserFullVersion;
    private long start;
    private long end;
    private String controlStatus;
    private String controlMessage;
    private String application;
    private String ip; // Host the Selenium IP
    private String url;
    private String port; // host the Selenium Port
    private String tag;
    private String finished;
    private int verbose;
    private String status;
    private String crbVersion;
    private String executor;
    private String screenSize;

    /**
     * From here are data outside database model.
     */
    private Application applicationObj;
    private String environmentData;
    private Invariant environmentDataObj;
    private Invariant CountryObj;
    private int screenshot;
    private String outputFormat;
    private Test testObj;
    private TestCase testCaseObj;
    private List<TestCase> PreTestCaseList;
    private CountryEnvParam countryEnvParam;
    private CountryEnvironmentParameters countryEnvironmentParameters;
    private boolean manualURL;
    private String myHost;
    private String myContextRoot;
    private String myLoginRelativeURL;
    private String seleniumIP;
    private String seleniumPort;
    private List<TestCaseStepExecution> testCaseStepExecutionList; // Host the list of Steps that will be executed (both pre tests and main test)
    private List<TestCaseExecutionData> testCaseExecutionDataList; // Host the full list of data calculated during the execution.
    private MessageGeneral resultMessage;
    private Selenium selenium;
    private String executionUUID;
    private Integer pageSource;
    private Integer seleniumLog;
    private Session session;
    private String manualExecution;
    private List<TestCaseCountryProperties> testCaseCountryPropertyList;
    private long idFromQueue;
    private Integer numberOfRetries;
    private String userAgent;
    private boolean synchroneous;
    private String timeout;
    private AnswerList testCaseStepExecutionAnswerList;
    private AnswerItem lastSOAPCalled;
    private List<RobotCapability> capabilities;
    private Integer cerberus_action_wait_default;
    
    /**
     * Temp attribute.
     */
    private boolean featureFlippingActivateWebsocketPush;

    public boolean isFeatureFlippingActivateWebsocketPush() {
        return featureFlippingActivateWebsocketPush;
    }

    public void setFeatureFlippingActivateWebsocketPush(boolean featureFlippingActivateWebsocketPush) {
        this.featureFlippingActivateWebsocketPush = featureFlippingActivateWebsocketPush;
    }
    
    private static final Logger LOG = Logger.getLogger(TestCaseExecution.class);

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

    public long getIdFromQueue() {
        return idFromQueue;
    }

    public void setIdFromQueue(long idFromQueue) {
        this.idFromQueue = idFromQueue;
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

    public List<TestCaseStepExecution> getTestCaseStepExecutionList() {
        return testCaseStepExecutionList;
    }

    public void setTestCaseStepExecutionList(List<TestCaseStepExecution> testCaseStepExecutionList) {
        this.testCaseStepExecutionList = testCaseStepExecutionList;
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

    public String getBrowserFullVersion() {
        return browserFullVersion;
    }

    public void setBrowserFullVersion(String browserFullVersion) {
        this.browserFullVersion = browserFullVersion;
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

    public String getFinished() {
        return finished;
    }

    public void setFinished(String finished) {
        this.finished = finished;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
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

    public List<TestCase> getPreTestCaseList() {
        return PreTestCaseList;
    }

    public void setPreTestCaseList(List<TestCase> PreTCase) {
        this.PreTestCaseList = PreTCase;
    }

    public List<TestCaseExecutionData> getTestCaseExecutionDataList() {
        return testCaseExecutionDataList;
    }

    public void setTestCaseExecutionDataList(List<TestCaseExecutionData> testCaseExecutionDataList) {
        this.testCaseExecutionDataList = testCaseExecutionDataList;
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

    public AnswerList getTestCaseStepExecutionAnswerList() {
        return testCaseStepExecutionAnswerList;
    }

    public void setTestCaseStepExecutionAnswerList(AnswerList testCaseStepExecutionAnswerList) {
        this.testCaseStepExecutionAnswerList = testCaseStepExecutionAnswerList;
    }

    public AnswerItem getLastSOAPCalled() {
        return lastSOAPCalled;
    }

    public void setLastSOAPCalled(AnswerItem lastSOAPCalled) {
        this.lastSOAPCalled = lastSOAPCalled;
    }

    public List<RobotCapability> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(List<RobotCapability> capabilities) {
        this.capabilities = capabilities;
    }

    public JSONObject toJson() {
        JSONObject result = new JSONObject();
        try {
            result.put("id", this.getId());
            result.put("test", this.getTest());
            result.put("testcase", this.getTestCase());
            result.put("build", this.getBuild());
            result.put("revision", this.getRevision());
            result.put("environment", this.getEnvironment());
            result.put("country", this.getCountry());
            result.put("browser", this.getBrowser());
            result.put("version", this.getVersion());
            result.put("platform", this.getPlatform());
            result.put("browserFullVersion", this.getBrowserFullVersion());
            result.put("capabilities", this.getCapabilities());
            result.put("start", this.getStart());
            result.put("end", this.getEnd());
            result.put("controlStatus", this.getControlStatus());
            result.put("controlMessage", this.getControlMessage());
            result.put("application", this.getApplication());
            result.put("ip", this.getIp());
            result.put("url", this.getUrl());
            result.put("port", this.getPort());
            result.put("tag", this.getTag());
            result.put("finished", this.getFinished());
            result.put("verbose", this.getVerbose());
            result.put("status", this.getStatus());
            result.put("crbVersion", this.getCrbVersion());
            result.put("executor", this.getExecutor());
            result.put("screenSize", this.getScreenSize());
            JSONArray array = new JSONArray();
            if(this.getTestCaseStepExecutionAnswerList() != null && this.getTestCaseStepExecutionAnswerList().getDataList() != null) {
                for (Object testCaseStepExecution : this.getTestCaseStepExecutionAnswerList().getDataList()) {
                    array.put(((TestCaseStepExecution) testCaseStepExecution).toJson());
                }
            }
            result.put("testCaseStepExecutionList", array);
        } catch (JSONException ex) {
            LOG.error(ex.toString());
        }
        return result;
    }

}
