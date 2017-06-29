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

import java.util.Date;

public class TestCaseExecutionInQueue {

    public enum State {
        WAITING,
        QUEUED,
        EXECUTING,
        CANCELLED,
        ERROR
    }

    private long id;
    private String test;
    private String testCase;
    private String country;
    private String environment;
    private String robot;
    private String robotIP;
    private String robotPort;
    private String browser;
    private String browserVersion;
    private String platform;
    private String screenSize;
    private boolean manualURL;
    private String manualHost;
    private String manualContextRoot;
    private String manualLoginRelativeURL;
    private String manualEnvData;
    private String tag;
    private String outputFormat;
    private int screenshot;
    private int verbose;
    private String timeout;
    private boolean synchroneous;
    private int pageSource;
    private int seleniumLog;
    private Date requestDate;
    private String comment;
    private int retries;
    private boolean manualExecution;
    private State state;

    /**
     * From here are data outside database model.
     */
    private Application applicationObj;
    private TestCase testCaseObj;

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

    public boolean isManualExecution() {
        return manualExecution;
    }

    public void setManualExecution(boolean manualExecution) {
        this.manualExecution = manualExecution;
    }

    public int getRetries() {
        return retries;
    }

    public void setRetries(int retries) {
        this.retries = retries;
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

    public boolean isManualURL() {
        return manualURL;
    }

    public void setManualURL(boolean manualURL) {
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

    public boolean isSynchroneous() {
        return synchroneous;
    }

    public void setSynchroneous(boolean synchroneous) {
        this.synchroneous = synchroneous;
    }

    public int getPageSource() {
        return pageSource;
    }

    public void setPageSource(int pageSource) {
        this.pageSource = pageSource;
    }

    public int getSeleniumLog() {
        return seleniumLog;
    }

    public void setSeleniumLog(int seleniumLog) {
        this.seleniumLog = seleniumLog;
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
                + manualEnvData + ", tag=" + tag + ", outputFormat=" + outputFormat + ", screenshot=" + screenshot + ", verbose=" + verbose + ", timeout=" + timeout
                + ", synchroneous=" + synchroneous + ", pageSource=" + pageSource + ", seleniumLog=" + seleniumLog + ", requestDate=" + requestDate + "]";
    }

}
