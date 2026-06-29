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
package org.cerberus.core.engine.queuemanagement.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor
public class TestCaseExecutionQueueToTreat {

    /**
     * Invariant Constraints.
     */
    public static final String CONSTRAIN1_GLOBAL = "constrain1_global";
    public static final String CONSTRAIN2_APPLIENV = "constrain2_applienvironment";
    public static final String CONSTRAIN3_APPLICATION = "constrain3_application";
    public static final String CONSTRAIN4_ROBOT = "constrain4_robot";
    public static final String CONSTRAIN5_EXECUTOREXTENSION = "constrain5_proxyservice";

    private long id;
    private String tag;
    private String test;
    private String testCase;
    private String debugFlag;
    private String manualExecution;
    private String system;
    private String environment;
    private String country;
    private String application;
    private int poolSizeAppEnvironment;
    private int poolSizeApplication;
    private String queueRobot;
    private String queueRobotHost;
    private String queueRobotPort;
    private String appType;
    private String selectedRobotHost;
    private String selectedRobotExtensionHost;
    private String usrCreated;
    private Timestamp dateCreated;
    private int queuePosition;
    private int nbEntryBefore;

}