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

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 10/01/2013
 * @since 2.0.0
 */
public class Environment {

    private String env;
    private String ip;
    private String url;
    private String urlLogin;
    private String build;
    private String revision;
    private boolean active;
    private String typeApplication;
    private String seleniumIp;
    private String seleniumPort;
    private String seleniumBrowser;
    private String path;
    private boolean maintenance;
    private String maintenanceStr;
    private String maintenanceEnd;

    public String getIp() {
        return this.ip;
    }

    public void setIp(String tempIp) {
        this.ip = tempIp;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String tempUrl) {
        this.url = tempUrl;
    }

    public String getUrlLogin() {
        return this.urlLogin;
    }

    public void setUrlLogin(String tempUrlLogin) {
        this.urlLogin = tempUrlLogin;
    }

    public String getBuild() {
        return this.build;
    }

    public void setBuild(String tempBuild) {
        this.build = tempBuild;
    }

    public String getRevision() {
        return this.revision;
    }

    public void setRevision(String tempRevision) {
        this.revision = tempRevision;
    }

    public boolean isActive() {
        return this.active;
    }

    public void setActive(boolean tempActive) {
        this.active = tempActive;
    }

    public String getTypeApplication() {
        return this.typeApplication;
    }

    public void setTypeApplication(String tempTypeApplication) {
        this.typeApplication = tempTypeApplication;
    }

    public String getSeleniumIp() {
        return this.seleniumIp;
    }

    public void setSeleniumIp(String seleniumIp) {
        this.seleniumIp = seleniumIp;
    }

    public String getSeleniumPort() {
        return this.seleniumPort;
    }

    public void setSeleniumPort(String seleniumPort) {
        this.seleniumPort = seleniumPort;
    }

    public String getSeleniumBrowser() {
        return this.seleniumBrowser;
    }

    public void setSeleniumBrowser(String seleniumBrowser) {
        this.seleniumBrowser = seleniumBrowser;
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(String tempPath) {
        this.path = tempPath;
    }

    public boolean isMaintenance() {
        return maintenance;
    }

    public void setMaintenance(boolean maintenance) {
        this.maintenance = maintenance;
    }

    public String getMaintenanceStr() {
        return maintenanceStr;
    }

    public void setMaintenanceStr(String maintenanceStr) {
        this.maintenanceStr = maintenanceStr;
    }

    public String getMaintenanceEnd() {
        return maintenanceEnd;
    }

    public void setMaintenanceEnd(String maintenanceEnd) {
        this.maintenanceEnd = maintenanceEnd;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }
    
}
