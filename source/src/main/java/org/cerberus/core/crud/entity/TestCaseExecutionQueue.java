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
package org.cerberus.core.crud.entity;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

public class TestCaseExecutionQueue {

    private long id;
    private String system;
    private String test;
    private String testCase;
    private String country;
    private String environment;
    private String robot;
    private String robotDecli;
    private String robotIP;
    private String robotPort;
    private String browser;
    private String browserVersion;
    private String platform;
    private String screenSize;
    private int manualURL;
    private String manualHost;
    private String manualContextRoot;
    private String manualLoginRelativeURL;
    private String manualEnvData;
    private String tag;
    private int screenshot;
    private int video;
    private int verbose;
    private String timeout;
    private int pageSource;
    private int robotLog;
    private int consoleLog;
    private String manualExecution;
    private int retries;
    private int alreadyExecuted;
    private Date requestDate;
    private State state;
    private int priority;
    private String comment;
    private String debugFlag;
    private long exeId;
    private String UsrCreated;
    private Timestamp DateCreated;
    private String UsrModif;
    private Timestamp DateModif;

    /**
     * From here are data outside database model.
     */
    private Application applicationObj;
    private TestCase testCaseObj;
    private List<TestCaseExecutionQueueDep> testcaseExecutionQueueDepList;

    public static final int PRIORITY_DEFAULT = 1000;
    public static final int PRIORITY_WHENDEPENDENCY = 100;

    public enum State {
        QUTEMP,
        QUWITHDEP,
        QUWITHDEP_PAUSED,
        QUEUED,
        QUEUED_PAUSED,
        WAITING,
        STARTING,
        EXECUTING,
        CANCELLED,
        ERROR,
        DONE
    }

    public List<TestCaseExecutionQueueDep> getTestcaseExecutionQueueDepList() {
        return testcaseExecutionQueueDepList;
    }

    public void setTestcaseExecutionQueueDepList(List<TestCaseExecutionQueueDep> testcaseExecutionQueueDepList) {
        this.testcaseExecutionQueueDepList = testcaseExecutionQueueDepList;
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

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getDebugFlag() {
        return debugFlag;
    }

    public void setDebugFlag(String debugFlag) {
        this.debugFlag = debugFlag;
    }

    public long getExeId() {
        return exeId;
    }

    public void setExeId(long exeId) {
        this.exeId = exeId;
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

    public String getScreenSize() {
        return screenSize;
    }

    public void setScreenSize(String screenSize) {
        this.screenSize = screenSize;
    }

    public TestCase getTestCaseObj() {
        return testCaseObj;
    }

    public void setTestCaseObj(TestCase testCaseObj) {
        this.testCaseObj = testCaseObj;
    }

    public Application getApplicationObj() {
        return applicationObj;
    }

    public void setApplicationObj(Application applicationObj) {
        this.applicationObj = applicationObj;
    }

    public String getManualExecution() {
        return manualExecution;
    }

    public void setManualExecution(String manualExecution) {
        this.manualExecution = manualExecution;
    }

    public int getRetries() {
        return retries;
    }

    public void setRetries(int retries) {
        this.retries = retries;
    }

    public int getAlreadyExecuted() {
        return alreadyExecuted;
    }

    public void setAlreadyExecuted(int alreadyExecuted) {
        this.alreadyExecuted = alreadyExecuted;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTest() {
        if (test == null) {
            throw new IllegalStateException("test must not be null");
        }
        return test;
    }

    public void setTest(String test) {
        if (test == null) {
            throw new IllegalArgumentException("test must not be null");
        }
        this.test = test;
    }

    public String getTestCase() {
        if (testCase == null) {
            throw new IllegalStateException("testCase must not be null");
        }
        return testCase;
    }

    public void setTestCase(String testCase) {
        if (testCase == null) {
            throw new IllegalArgumentException("testCase must not be null");
        }
        this.testCase = testCase;
    }

    public String getCountry() {
        if (country == null) {
            throw new IllegalStateException("country must not be null");
        }
        return country;
    }

    public void setCountry(String country) {
        if (country == null) {
            throw new IllegalArgumentException("country must not be null");
        }
        this.country = country;
    }

    public String getEnvironment() {
        if (environment == null) {
            throw new IllegalStateException("environment must not be null");
        }
        return environment;
    }

    public void setEnvironment(String environment) {
        if (environment == null) {
            throw new IllegalArgumentException("environment must not be null");
        }
        this.environment = environment;
    }

    public String getRobot() {
        return robot;
    }

    public void setRobot(String robot) {
        this.robot = robot;
    }

    public String getRobotIP() {
        return robotIP;
    }

    public void setRobotIP(String robotIP) {
        this.robotIP = robotIP;
    }

    public String getRobotPort() {
        return robotPort;
    }

    public void setRobotPort(String robotPort) {
        this.robotPort = robotPort;
    }

    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    public String getBrowserVersion() {
        return browserVersion;
    }

    public void setBrowserVersion(String browserVersion) {
        this.browserVersion = browserVersion;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public int getManualURL() {
        return manualURL;
    }

    public void setManualURL(int manualURL) {
        this.manualURL = manualURL;
    }

    public String getManualHost() {
        return manualHost;
    }

    public void setManualHost(String manualHost) {
        this.manualHost = manualHost;
    }

    public String getManualContextRoot() {
        return manualContextRoot;
    }

    public void setManualContextRoot(String manualContextRoot) {
        this.manualContextRoot = manualContextRoot;
    }

    public String getManualLoginRelativeURL() {
        return manualLoginRelativeURL;
    }

    public void setManualLoginRelativeURL(String manualLoginRelativeURL) {
        this.manualLoginRelativeURL = manualLoginRelativeURL;
    }

    public String getManualEnvData() {
        return manualEnvData;
    }

    public void setManualEnvData(String manualEnvData) {
        this.manualEnvData = manualEnvData;
    }

    public String getTag() {
        if (tag == null) {
            throw new IllegalStateException("tag must not be null");
        }
        return tag;
    }

    public void setTag(String tag) {
        if (tag == null) {
            throw new IllegalArgumentException("tag must not be null");
        }
        this.tag = tag;
    }

    public int getScreenshot() {
        return screenshot;
    }

    public void setScreenshot(int screenshot) {
        this.screenshot = screenshot;
    }

    public int getVideo() {
        return video;
    }

    public void setVideo(int video) {
        this.video = video;
    }

    public int getVerbose() {
        return verbose;
    }

    public void setVerbose(int verbose) {
        this.verbose = verbose;
    }

    public String getTimeout() {
        return timeout;
    }

    public void setTimeout(String timeout) {
        this.timeout = timeout;
    }

    public int getPageSource() {
        return pageSource;
    }

    public void setPageSource(int pageSource) {
        this.pageSource = pageSource;
    }

    public int getRobotLog() {
        return robotLog;
    }

    public void setRobotLog(int robotLog) {
        this.robotLog = robotLog;
    }

    public int getConsoleLog() {
        return consoleLog;
    }

    public void setConsoleLog(int consoleLog) {
        this.consoleLog = consoleLog;
    }

    public Date getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(Date requestDate) {
        this.requestDate = requestDate;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "TestCaseExecutionInQueue [id=" + id + ", test=" + test + ", testCase=" + testCase + ", country=" + country + ", environment=" + environment + ", robot=" + robot
                + ", robotIP=" + robotIP + ", robotPort=" + robotPort + ", browser=" + browser + ", browserVersion=" + browserVersion + ", platform=" + platform + ", manualURL="
                + manualURL + ", manualHost=" + manualHost + ", manualContextRoot=" + manualContextRoot + ", manualLoginRelativeURL=" + manualLoginRelativeURL + ", manualEnvData="
                + manualEnvData + ", tag=" + tag + ", screenshot=" + screenshot + ", verbose=" + verbose + ", timeout=" + timeout
                + ", pageSource=" + pageSource + ", seleniumLog=" + robotLog + ", requestDate=" + requestDate + "]";
    }

}
