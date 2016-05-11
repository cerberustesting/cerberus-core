/*
 * Cerberus  Copyright (C) 2016  vertigo17
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

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Aurelien Bourdon.
 */
public class RobotCapabilities {

    public interface Capability {
        String PLATFORM = "platform";

        String BROWSER = "browser";

        String VERSION = "version";
    }

    @JsonIgnore
    private Robot robot;

    private Map<String, String> capabilities;

    @SuppressWarnings("unused")
    public RobotCapabilities() {
        this(null);
    }

    public RobotCapabilities(Robot robot) {
        this.robot = robot;
        capabilities = new HashMap<>();
    }

    public Robot getRobot() {
        return robot;
    }

    public RobotCapabilities setRobot(Robot robot) {
        this.robot = robot;
        return this;
    }

    public Map<String, String> getCapabilities() {
        return Collections.unmodifiableMap(capabilities);
    }

    public RobotCapabilities setCapabilities(Map<String, String> capabilities) {
        this.capabilities = capabilities;
        return this;
    }

    public void putCapability(String key, String value) {
        capabilities.put(key, value);
    }

    public String getCapability(String key) {
        return capabilities.get(key);
    }

    public boolean hasCapability(String key) {
        return capabilities.containsKey(key);
    }

}
