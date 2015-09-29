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
package org.cerberus.crud.entity;

import io.appium.java_client.AppiumDriver;
import java.util.List;
import org.openqa.selenium.WebDriver;

/**
 *
 * @author bcivel
 */
public class Session {
    
    private String host;
    private String port;
    private WebDriver driver;
    private AppiumDriver appiumDriver;
    private long defaultWait;
    private boolean started;
    List<SessionCapabilities> capabilities;

    public AppiumDriver getAppiumDriver() {
        return appiumDriver;
    }

    public void setAppiumDriver(AppiumDriver appiumDriver) {
        this.appiumDriver = appiumDriver;
    }

    public List<SessionCapabilities> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(List<SessionCapabilities> capabilities) {
        this.capabilities = capabilities;
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

    public long getDefaultWait() {
        return defaultWait;
    }

    public void setDefaultWait(long defaultWait) {
        this.defaultWait = defaultWait;
    }

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }
    
    
    
}
