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
package org.cerberus.engine.threadpool.entity;

public class TestCaseExecutionQueueToTreat {

    private long id;
    private String manualExecution;
    private String system;
    private String environment;
    private String country;
    private String application;
    private int poolSizeApplication;
    private String robotHost;

    /**
     * Invariant Constrains.
     */
    public static final String CONSTRAIN1_GLOBAL = "constrain1_global";
    public static final String CONSTRAIN2_APPLICATION = "constrain2_application";
    public static final String CONSTRAIN3_ROBOT = "constrain3_robot";

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getManualExecution() {
        return manualExecution;
    }

    public void setManualExecution(String manualExecution) {
        this.manualExecution = manualExecution;
    }

    public String getSystem() {
        return system;
    }

    public void setSystem(String system) {
        this.system = system;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getRobotHost() {
        return robotHost;
    }

    public void setRobotHost(String robot) {
        this.robotHost = robot;
    }

    public int getPoolSizeApplication() {
        return poolSizeApplication;
    }

    public void setPoolSizeApplication(int poolSizeApplication) {
        this.poolSizeApplication = poolSizeApplication;
    }

}
