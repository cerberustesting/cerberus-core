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

import org.openqa.selenium.WebDriver;


/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 18/Dez/2012
 * @since 2.0.0
 */
public class Selenium {

    private String host;
    private String port;
    private String browser;
    private String version;
    private String platform;
    private String login;
    private String ip;
    private WebDriver driver;
    private long defaultWait;
    private boolean started;

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
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

    public String getHost() {
        return this.host;
    }

    public void setHost(String tempHost) {
        this.host = tempHost;
    }

    public String getPort() {
        return this.port;
    }

    public void setPort(String tempPort) {
        this.port = tempPort;
    }

    public String getBrowser() {
        return this.browser;
    }

    public void setBrowser(String tempBrowser) {
        this.browser = tempBrowser;
    }

    public String getLogin() {
        return this.login;
    }

    public void setLogin(String tempLogin) {
        this.login = tempLogin;
    }

    public String getIp() {
        return this.ip;
    }

    public void setIp(String tempIp) {
        this.ip = tempIp;
    }

    public WebDriver getDriver() {
        return this.driver;
    }

    public void setDriver(WebDriver webDriver) {
        this.driver = webDriver;
    }

    public long getDefaultWait() {
        return defaultWait;
    }

    public void setDefaultWait(long defaultWait) {
        this.defaultWait = defaultWait;
    }
}
