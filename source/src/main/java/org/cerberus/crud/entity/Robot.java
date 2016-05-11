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

import com.google.gson.annotations.Expose;

/**
 *
 * @author bcivel
 */
public class Robot {

    Integer robotID;
    String robot;
    String host;
    String port;
    String active;
    String description;
    // FIXME Do not use user agent as a direct Robot attribute
    String userAgent;

    private RobotCapabilities capabilities;

    public Robot() {
        initCapabilities();
    }

    private void initCapabilities() {
        setCapabilities(new RobotCapabilities(this));
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public Integer getRobotID() {
        return robotID;
    }

    public void setRobotID(Integer robotID) {
        this.robotID = robotID;
    }

    public String getRobot() {
        return robot;
    }

    public void setRobot(String robot) {
        this.robot = robot;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getActive() {
        return active;
    }

    public void setActive(String active) {
        this.active = active;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPlatform() {
        return capabilities.getCapability(RobotCapabilities.Capability.PLATFORM);
    }

    public void setPlatform(String platform) {
        capabilities.putCapability(RobotCapabilities.Capability.PLATFORM, platform);
    }

    public String getBrowser() {
        return capabilities.getCapability(RobotCapabilities.Capability.BROWSER);
    }

    public void setBrowser(String browser) {
        capabilities.putCapability(RobotCapabilities.Capability.BROWSER, browser);
    }

    public String getVersion() {
        return capabilities.getCapability(RobotCapabilities.Capability.VERSION);
    }

    public void setVersion(String version) {
        capabilities.putCapability(RobotCapabilities.Capability.VERSION, version);
    }

    public RobotCapabilities getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(RobotCapabilities capabilities) {
        if (capabilities == null) {
            throw new IllegalArgumentException("Unable to set null capabilities for robot " + getRobotID());
        }
        this.capabilities = capabilities;
        this.capabilities.setRobot(this);
    }

}
