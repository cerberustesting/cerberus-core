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
package org.cerberus.core.crud.factory;

import java.util.List;
import org.cerberus.core.crud.entity.Robot;
import org.cerberus.core.crud.entity.RobotCapability;
import org.cerberus.core.crud.entity.RobotExecutor;

/**
 * @author bcivel
 */
public interface IFactoryRobot {

    /**
     *
     * @param robotID
     * @param robot
     * @param platform
     * @param browser
     * @param version
     * @param isActive
     * @param lbexemethod
     * @param description
     * @param userAgent
     * @param type
     * @param profileFolder
     * @param acceptNotifications
     * @param extraParam
     * @param screenSize
     * @param isAcceptInsecureCerts
     * @param robotDecli
     * @return
     */
    Robot create(Integer robotID, String robot, String platform,
            String browser, String version, boolean isActive, String lbexemethod, String description, String userAgent, String screenSize, String profileFolder, Integer acceptNotifications, String extraParam, boolean isAcceptInsecureCerts, String robotDecli, String type);

    /**
     *
     * @param robotID Autoincrement Id
     * @param robot Name of the Robot
     * @param platform platform of the Robot
     * @param browser browser of the Robot
     * @param version version of the Robot
     * @param isActive Robot active
     * @param lbexemethod
     * @param description description of the Robot
     * @param userAgent userAgent to Use
     * @param screenSize
     * @param profileFolder
     * @param acceptNotifications
     * @param extraParam
     * @param isAcceptInsecureCerts
     * @param capabilities
     * @param executors
     * @param robotDecli
     * @param type
     * @return
     */
    Robot create(Integer robotID, String robot, String platform,
            String browser, String version, boolean isActive, String lbexemethod, String description, String userAgent, String screenSize, String profileFolder, Integer acceptNotifications, String extraParam, boolean isAcceptInsecureCerts,
            List<RobotCapability> capabilities, List<RobotExecutor> executors, String robotDecli, String type);

}
