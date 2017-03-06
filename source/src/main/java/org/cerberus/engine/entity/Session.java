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
package org.cerberus.engine.entity;

import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

/**
 *
 * @author bcivel
 */
public class Session {
    
    private String host;
    private String port;
    private WebDriver driver;
    private AppiumDriver appiumDriver;
    private Integer cerberus_selenium_pageLoadTimeout;
    private Integer cerberus_selenium_implicitlyWait;
    private Integer cerberus_selenium_setScriptTimeout;
    private Integer cerberus_selenium_wait_element;
    private Integer cerberus_appium_wait_element;
    private Integer cerberus_selenium_action_click_timeout;
    private boolean started;
    private DesiredCapabilities desiredCapabilities;

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

    public Integer getCerberus_selenium_wait_element() {
        return cerberus_selenium_wait_element;
    }

    public void setCerberus_selenium_wait_element(Integer cerberus_selenium_wait_element) {
        this.cerberus_selenium_wait_element = cerberus_selenium_wait_element;
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

    public DesiredCapabilities getDesiredCapabilities() {
        return desiredCapabilities;
    }

    public void setDesiredCapabilities(DesiredCapabilities capabilities) {
        this.desiredCapabilities = capabilities;
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

    public void quit() {
        if (driver != null) {
            driver.quit();
        }
        if (appiumDriver != null) {
            appiumDriver.closeApp();
        }
    }

    public Integer getCerberus_selenium_action_click_timeout() {
        return cerberus_selenium_action_click_timeout;
    }

    public void setCerberus_selenium_action_click_timeout(Integer cerberus_selenium_action_click_timeout) {
        this.cerberus_selenium_action_click_timeout = cerberus_selenium_action_click_timeout;
    }
    
}
