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
package org.cerberus.core.crud.factory.impl;

import java.util.ArrayList;
import java.util.List;
import org.cerberus.core.crud.entity.Robot;
import org.cerberus.core.crud.entity.RobotCapability;
import org.cerberus.core.crud.entity.RobotExecutor;
import org.cerberus.core.crud.factory.IFactoryRobot;
import org.springframework.stereotype.Service;

/**
 * @author bcivel
 */
@Service
public class FactoryRobot implements IFactoryRobot {

    @Override
    public Robot create(Integer robotID, String robot, String platform,
            String browser, String version, boolean isActive, String lbexemethod, String description, String userAgent, String screenSize, String profileFolder, Integer acceptNotifications, String extraParam, boolean isAcceptInsecureCerts, String robotDecli, String type) {
        Robot r = create(robotID, robot, platform, browser, version, isActive, lbexemethod, description, userAgent, screenSize, profileFolder,acceptNotifications, extraParam, isAcceptInsecureCerts, new ArrayList<>(), new ArrayList<>(), robotDecli, type);
        return r;
    }

    @Override
    public Robot create(Integer robotID, String robot, String platform, String browser, String version, boolean isActive, String lbexemethod, String description, String userAgent,
            String screenSize, String profileFolder, Integer acceptNotifications, String extraParam, boolean isAcceptInsecureCerts, List<RobotCapability> capabilities, List<RobotExecutor> executors, String robotDecli, String type) {
        Robot newRobot = new Robot();
        newRobot.setRobotID(robotID);
        newRobot.setRobot(robot);
        newRobot.setPlatform(platform);
        newRobot.setBrowser(browser);
        newRobot.setVersion(version);
        newRobot.setIsActive(isActive);
        newRobot.setLbexemethod(lbexemethod);
        newRobot.setDescription(description);
        newRobot.setUserAgent(userAgent);
        newRobot.setCapabilities(capabilities);
        newRobot.setExecutors(executors);
        newRobot.setScreenSize(screenSize);
        newRobot.setProfileFolder(profileFolder);
        newRobot.setRobotDecli(robotDecli);
        newRobot.setType(type);
        newRobot.setIsAcceptInsecureCerts(isAcceptInsecureCerts);
        newRobot.setAcceptNotifications(acceptNotifications);
        newRobot.setExtraParam(extraParam);
        return newRobot;
    }

}
