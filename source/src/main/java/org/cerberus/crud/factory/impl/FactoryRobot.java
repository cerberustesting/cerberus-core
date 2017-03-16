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
package org.cerberus.crud.factory.impl;

import java.util.ArrayList;
import java.util.List;
import org.cerberus.crud.entity.Robot;
import org.cerberus.crud.entity.RobotCapability;
import org.cerberus.crud.factory.IFactoryRobot;
import org.springframework.stereotype.Service;

/**
 * @author bcivel
 */
@Service
public class FactoryRobot implements IFactoryRobot {

    public static final int NO_ROBOTID_DEFINED = -1;

    @Override
    public Robot create(Integer robotID, String robot, String host, String port, String platform ,
    String browser, String version, String active, String description, String userAgent, String screenSize) {
        return create(robotID, robot, host, port, platform, browser, version, active, description, userAgent, screenSize, new ArrayList<RobotCapability>());
    }

    @Override
    public Robot create(Integer robotID, String robot, String host, String port, String platform, String browser, String version, String active, String description, String userAgent, String screenSize, List<RobotCapability> capabilities) {
        Robot newRobot = new Robot();
        if (robotID != NO_ROBOTID_DEFINED) {
            newRobot.setRobotID(robotID);
        }
        newRobot.setRobot(robot);
        newRobot.setHost(host);
        newRobot.setPort(port);
        newRobot.setPlatform(platform);
        newRobot.setBrowser(browser);
        newRobot.setVersion(version);
        newRobot.setActive(active);
        newRobot.setDescription(description);
        newRobot.setUserAgent(userAgent);
        newRobot.setCapabilities(capabilities);
        newRobot.setScreenSize(screenSize);
        return newRobot;
    }

    @Override
    public Robot create(final String robot, final String host, final String port, final String platform, final String browser, final String version, final String active, final String description, final String userAgent, final String screenSize) {
        return create(NO_ROBOTID_DEFINED, robot, host, port, platform, browser, version, active, description, userAgent, screenSize);
    }


}
