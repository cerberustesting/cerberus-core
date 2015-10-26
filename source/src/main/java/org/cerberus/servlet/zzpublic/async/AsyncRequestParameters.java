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
package org.cerberus.servlet.zzpublic.async;


/**
 * {Insert class description here}
 *
 * @author FNogueira
 * @version 1.0, 24/10/2015
 * @since 1.1.3
 */
public class AsyncRequestParameters {

    private String test;
    private String testCase;
    private String country;
    private String environment;
    private String seleniumIp;
    private String seleniumPort;
    private String browser;
    private String version;
    private String platform;
    private String robot;
    private long timeout;
    private String userAgent;
    private String screenSize;
    
    private String manualExecution;
    private String tag;
    private String outputFormat;
    private int verbose ;
    private int screenshot;
    private int seleniumLog;
    private int pageSource;
    private int nrRetries;
    private int idFromQueue;

   
    public AsyncRequestParameters(String test, String testcase, String country, String environment){
        this.test = test;
        this.testCase = testcase;
        this.country = country;
        this.environment = environment;
        this.seleniumIp = "";
        this.seleniumPort= "";
        this.browser= "";
        this.version= "";
        this.platform= "";
        this.robot= "";
        this.timeout = 0;
        this.userAgent = "";
        this.screenSize = "";
        this.pageSource = 0;
        this.manualExecution = "";
        this.outputFormat = "compact";
        this.verbose = 0;
        this.screenshot = 1;
        this.seleniumLog = 1;
        this.nrRetries = 1;
        this.idFromQueue = 0;
    }
    
    
    public String getTest() {
        return test;
    }

    public String getTestCase() {
        return testCase;
    }

    public String getCountry() {
        return country;
    }

    public String getEnvironment() {
        return environment;
    }

    public String getSeleniumIp() {
        return seleniumIp;
    }

    public String getSeleniumPort() {
        return seleniumPort;
    }

    public String getBrowser() {
        return browser;
    }

    public String getVersion() {
        return version;
    }

    public String getPlatform() {
        return platform;
    }

    public String getRobot() {
        return robot;
    }

    public long getTimeout() {
        return timeout;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public String getScreenSize() {
        return screenSize;
    }

    

    public String getManualExecution() {
        return manualExecution;
    }    
    public void setTest(String test) {
        this.test = test;
    }

    public void setTestCase(String testCase) {
        this.testCase = testCase;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public void setSeleniumIp(String seleniumIp) {
        this.seleniumIp = seleniumIp;
    }

    public void setSeleniumPort(String seleniumPort) {
        this.seleniumPort = seleniumPort;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public void setRobot(String robot) {
        this.robot = robot;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public void setScreenSize(String screenSize) {
        this.screenSize = screenSize;
    }


    
    public void setManualExecution(String manualExecution) {
        this.manualExecution = manualExecution;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }   
    public int getNrRetries() {
        return nrRetries;
    }

    public void setNrRetries(int nrRetries) {
        this.nrRetries = nrRetries;
    }
    
    public String getOutputFormat() {
        return outputFormat;
    }

    public void setOutputFormat(String outputFormat) {
        this.outputFormat = outputFormat;
    }

    public int getVerbose() {
        return verbose;
    }

    public void setVerbose(int verbose) {
        this.verbose = verbose;
    }

    public int getScreenshot() {
        return screenshot;
    }

    public void setScreenshot(int screenshot) {
        this.screenshot = screenshot;
    }

    public int getSeleniumLog() {
        return seleniumLog;
    }

    public void setSeleniumLog(int seleniumLog) {
        this.seleniumLog = seleniumLog;
    }

    public int getPageSource() {
        return pageSource;
    }

    public void setPageSource(int pageSource) {
        this.pageSource = pageSource;
    }
    public int getIdFromQueue() {
        return idFromQueue;
    }

    public void setIdFromQueue(int idFromQueue) {
        this.idFromQueue = idFromQueue;
    }
    
}
