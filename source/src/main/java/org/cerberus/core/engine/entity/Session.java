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
package org.cerberus.core.engine.entity;

import io.appium.java_client.AppiumDriver;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;

/**
 *
 * @author bcivel
 */
public class Session {

    private String host;
    private String hostUser;
    private String hostPassword;
    private String port;
    private String nodeHost;
    private String nodePort;

    private Integer executorExtensionPort; //Port where the cerberus robot extension is available on the node host
    private Integer executorExtensionProxyPort; // if >0, we use the proxy on host:proxyPort in order to connect to sikuli API (host is the host of the robot - ie the proxy should run on the same server as the robot server).
    
    private boolean started;
    private boolean sikuliAvailable;
    
    
    private WebDriver driver;
    private AppiumDriver appiumDriver;

    private Integer cerberus_selenium_implicitlyWait;
    
    private Integer cerberus_selenium_pageLoadTimeout;
    private Integer cerberus_selenium_pageLoadTimeout_default; // Default value
    private Integer cerberus_selenium_setScriptTimeout;
    private Integer cerberus_selenium_setScriptTimeout_default; // Default value

    private Integer cerberus_selenium_wait_element; // Current Value. This one is the one used by the engine.
    private Integer cerberus_selenium_wait_element_default; // Default value
    private Integer cerberus_sikuli_wait_element; // Current Value. This one is the one used by the engine.
    private Integer cerberus_sikuli_wait_element_default; // Default value
    private Integer cerberus_appium_wait_element; // Current Value. This one is the one used by the engine.
    private Integer cerberus_appium_wait_element_default; // Default value

    private Integer cerberus_selenium_action_click_timeout;
    private Integer cerberus_appium_action_longpress_wait;

    private boolean cerberus_selenium_autoscroll;
    private Integer cerberus_selenium_autoscroll_vertical_offset;
    private Integer cerberus_selenium_autoscroll_horizontal_offset;

    private Integer cerberus_selenium_highlightElement;
    private Integer cerberus_selenium_highlightElement_default;
    private Integer cerberus_sikuli_highlightElement;
    private Integer cerberus_sikuli_highlightElement_default;

    private String cerberus_sikuli_minSimilarity;
    private String cerberus_sikuli_minSimilarity_default;
    
    private String cerberus_sikuli_typeDelay;
    private String cerberus_sikuli_typeDelay_default;

    private MutableCapabilities desiredCapabilities;

    private JSONArray consoleLogs;

    public boolean isSikuliAvailable() {
        return sikuliAvailable;
    }

    public void setSikuliAvailable(boolean sikuliAvailable) {
        this.sikuliAvailable = sikuliAvailable;
    }

    public Integer getExecutorExtensionProxyPort() {
        return executorExtensionProxyPort;
    }

    public void setExecutorExtensionProxyPort(Integer executorExtensionProxyPort) {
        this.executorExtensionProxyPort = executorExtensionProxyPort;
    }

    public Integer getExecutorExtensionPort() {
        return executorExtensionPort;
    }

    public void setExecutorExtensionPort(Integer executorExtensionPort) {
        this.executorExtensionPort = executorExtensionPort;
    }

    public Integer getCerberus_selenium_highlightElement_default() {
        return cerberus_selenium_highlightElement_default;
    }

    public void setCerberus_selenium_highlightElement_default(Integer cerberus_selenium_highlightElement_default) {
        this.cerberus_selenium_highlightElement_default = cerberus_selenium_highlightElement_default;
    }

    public String getCerberus_sikuli_minSimilarity_default() {
        return cerberus_sikuli_minSimilarity_default;
    }

    public void setCerberus_sikuli_minSimilarity_default(String cerberus_sikuli_minSimilarity_default) {
        this.cerberus_sikuli_minSimilarity_default = cerberus_sikuli_minSimilarity_default;
    }

    public Integer getCerberus_selenium_highlightElement() {
        return cerberus_selenium_highlightElement;
    }

    public void setCerberus_selenium_highlightElement(Integer cerberus_selenium_highlightElement) {
        this.cerberus_selenium_highlightElement = cerberus_selenium_highlightElement;
    }

    public Integer getCerberus_sikuli_highlightElement() {
        return cerberus_sikuli_highlightElement;
    }

    public void setCerberus_sikuli_highlightElement(Integer cerberus_sikuli_highlightElement) {
        this.cerberus_sikuli_highlightElement = cerberus_sikuli_highlightElement;
    }

    public Integer getCerberus_sikuli_highlightElement_default() {
        return cerberus_sikuli_highlightElement_default;
    }

    public void setCerberus_sikuli_highlightElement_default(Integer cerberus_sikuli_highlightElement_default) {
        this.cerberus_sikuli_highlightElement_default = cerberus_sikuli_highlightElement_default;
    }

    public String getCerberus_sikuli_minSimilarity() {
        return cerberus_sikuli_minSimilarity;
    }

    public void setCerberus_sikuli_minSimilarity(String cerberus_sikuli_minSimilarity) {
        this.cerberus_sikuli_minSimilarity = cerberus_sikuli_minSimilarity;
    }

    public String getCerberus_sikuli_typeDelay() {
        return cerberus_sikuli_typeDelay;
    }

    public void setCerberus_sikuli_typeDelay(String cerberus_sikuli_typeDelay) {
        this.cerberus_sikuli_typeDelay = cerberus_sikuli_typeDelay;
    }

    public String getCerberus_sikuli_typeDelay_default() {
        return cerberus_sikuli_typeDelay_default;
    }

    public void setCerberus_sikuli_typeDelay_default(String cerberus_sikuli_typeDelay_default) {
        this.cerberus_sikuli_typeDelay_default = cerberus_sikuli_typeDelay_default;
    }

    public String getNodeHost() {
        return nodeHost;
    }

    public void setNodeHost(String nodeHost) {
        this.nodeHost = nodeHost;
    }

    public String getNodePort() {
        return nodePort;
    }

    public void setNodePort(String nodePort) {
        this.nodePort = nodePort;
    }

    public JSONArray getConsoleLogs() {
        return consoleLogs;
    }

    public void setConsoleLogs(JSONArray consoleLogs) {
        this.consoleLogs = consoleLogs;
    }

    public void appendConsoleLogs(JSONObject consoleLogsEntry) {
        if (this.consoleLogs == null) {
            this.consoleLogs = new JSONArray();
        }
        this.consoleLogs.put(consoleLogsEntry);
    }

    public Integer getCerberus_selenium_pageLoadTimeout() {
        return cerberus_selenium_pageLoadTimeout;
    }

    public void setCerberus_selenium_pageLoadTimeout(Integer cerberus_selenium_pageLoadTimeout) {
        this.cerberus_selenium_pageLoadTimeout = cerberus_selenium_pageLoadTimeout;
    }

    public Integer getCerberus_selenium_implicitlyWait() {
        return cerberus_selenium_implicitlyWait;
    }

    public void setCerberus_selenium_implicitlyWait(Integer cerberus_selenium_implicitlyWait) {
        this.cerberus_selenium_implicitlyWait = cerberus_selenium_implicitlyWait;
    }

    public Integer getCerberus_selenium_setScriptTimeout() {
        return cerberus_selenium_setScriptTimeout;
    }

    public void setCerberus_selenium_setScriptTimeout(Integer cerberus_selenium_setScriptTimeout) {
        this.cerberus_selenium_setScriptTimeout = cerberus_selenium_setScriptTimeout;
    }

    public Integer getCerberus_sikuli_wait_element_default() {
        return cerberus_sikuli_wait_element_default;
    }

    public void setCerberus_sikuli_wait_element_default(Integer cerberus_sikuli_wait_element_default) {
        this.cerberus_sikuli_wait_element_default = cerberus_sikuli_wait_element_default;
    }

    public Integer getCerberus_sikuli_wait_element() {
        return cerberus_sikuli_wait_element;
    }

    public void setCerberus_sikuli_wait_element(Integer cerberus_sikuli_wait_element) {
        this.cerberus_sikuli_wait_element = cerberus_sikuli_wait_element;
    }

    public Integer getCerberus_selenium_pageLoadTimeout_default() {
        return cerberus_selenium_pageLoadTimeout_default;
    }

    public void setCerberus_selenium_pageLoadTimeout_default(Integer cerberus_selenium_pageLoadTimeout_default) {
        this.cerberus_selenium_pageLoadTimeout_default = cerberus_selenium_pageLoadTimeout_default;
    }

    public Integer getCerberus_selenium_setScriptTimeout_default() {
        return cerberus_selenium_setScriptTimeout_default;
    }

    public void setCerberus_selenium_setScriptTimeout_default(Integer cerberus_selenium_setScriptTimeout_default) {
        this.cerberus_selenium_setScriptTimeout_default = cerberus_selenium_setScriptTimeout_default;
    }

    public Integer getCerberus_selenium_wait_element_default() {
        return cerberus_selenium_wait_element_default;
    }

    public void setCerberus_selenium_wait_element_default(Integer cerberus_selenium_wait_element_default) {
        this.cerberus_selenium_wait_element_default = cerberus_selenium_wait_element_default;
    }

    public Integer getCerberus_selenium_wait_element() {
        return cerberus_selenium_wait_element;
    }

    public void setCerberus_selenium_wait_element(Integer cerberus_selenium_wait_element) {
        this.cerberus_selenium_wait_element = cerberus_selenium_wait_element;
    }

    public Integer getCerberus_appium_wait_element_default() {
        return cerberus_appium_wait_element_default;
    }

    public void setCerberus_appium_wait_element_default(Integer cerberus_appium_wait_element_default) {
        this.cerberus_appium_wait_element_default = cerberus_appium_wait_element_default;
    }

    public Integer getCerberus_appium_wait_element() {
        return cerberus_appium_wait_element;
    }

    public void setCerberus_appium_wait_element(Integer cerberus_appium_wait_element) {
        this.cerberus_appium_wait_element = cerberus_appium_wait_element;
    }

    public AppiumDriver getAppiumDriver() {
        return appiumDriver;
    }

    public void setAppiumDriver(AppiumDriver appiumDriver) {
        this.appiumDriver = appiumDriver;
    }

    public MutableCapabilities getDesiredCapabilities() {
        return desiredCapabilities;
    }

    public void setDesiredCapabilities(MutableCapabilities capabilities) {
        this.desiredCapabilities = capabilities;
    }

    public String getHostUser() {
        return hostUser;
    }

    public void setHostUser(String hostUser) {
        this.hostUser = hostUser;
    }

    public String getHostPassword() {
        return hostPassword;
    }

    public void setHostPassword(String hostPassword) {
        this.hostPassword = hostPassword;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public WebDriver getDriver() {
        return driver;
    }

    public void setDriver(WebDriver driver) {
        this.driver = driver;
    }

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    public boolean isCerberus_selenium_autoscroll() {
        return cerberus_selenium_autoscroll;
    }

    public void setCerberus_selenium_autoscroll(boolean cerberus_selenium_autoscroll) {
        this.cerberus_selenium_autoscroll = cerberus_selenium_autoscroll;
    }

    public void quit() {
        if (driver != null) {
            driver.quit();
        }
        if (appiumDriver != null) {
            appiumDriver.close();
        }
    }

    public Integer getCerberus_selenium_action_click_timeout() {
        return cerberus_selenium_action_click_timeout;
    }

    public void setCerberus_selenium_action_click_timeout(Integer cerberus_selenium_action_click_timeout) {
        this.cerberus_selenium_action_click_timeout = cerberus_selenium_action_click_timeout;
    }

    public Integer getCerberus_appium_action_longpress_wait() {
        return cerberus_selenium_action_click_timeout;
    }

    public void setCerberus_appium_action_longpress_wait(Integer cerberus_appium_action_longpress_wait) {
        this.cerberus_appium_action_longpress_wait = cerberus_appium_action_longpress_wait;
    }

    public Integer getCerberus_selenium_autoscroll_vertical_offset() {
        return cerberus_selenium_autoscroll_vertical_offset;
    }

    public void setCerberus_selenium_autoscroll_vertical_offset(Integer cerberus_selenium_autoscroll_vertical_offset) {
        this.cerberus_selenium_autoscroll_vertical_offset = cerberus_selenium_autoscroll_vertical_offset;
    }

    public Integer getCerberus_selenium_autoscroll_horizontal_offset() {
        return cerberus_selenium_autoscroll_horizontal_offset;
    }

    public void setCerberus_selenium_autoscroll_horizontal_offset(Integer cerberus_selenium_autoscroll_horizontal_offset) {
        this.cerberus_selenium_autoscroll_horizontal_offset = cerberus_selenium_autoscroll_horizontal_offset;
    }

}
