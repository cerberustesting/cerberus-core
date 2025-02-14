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

import java.sql.Timestamp;
import org.cerberus.core.crud.entity.RobotExecutor;
import org.cerberus.core.crud.factory.IFactoryRobotExecutor;
import org.springframework.stereotype.Service;

/**
 * @author bcivel
 */
@Service
public class FactoryRobotExecutor implements IFactoryRobotExecutor {

    @Override
    public RobotExecutor create(Integer ID, String robot, String executor, boolean isActive, Integer rank, String host, String port, String hostUser, String hostPassword,
                                Integer executorExtensionProxyPort, String deviceUdid, String deviceName, Integer devicePort, boolean isDeviceLockUnlock, String executorProxyServiceHost, Integer executorProxyServicePort, String executorBrowserProxyHost, Integer executorBrowserProxyPort, Integer executorExtensionPort, String executorProxyType, String description,
                                String UsrCreated, Timestamp DateCreated, String UsrModif, Timestamp DateModif) {
        RobotExecutor newRobot = new RobotExecutor();
        newRobot.setID(ID);
        newRobot.setRobot(robot);
        newRobot.setExecutor(executor);
        newRobot.setIsActive(isActive);
        newRobot.setRank(rank);
        newRobot.setHost(host);
        newRobot.setPort(port);
        newRobot.setHostUser(hostUser);
        newRobot.setHostPassword(hostPassword);
        newRobot.setExecutorExtensionProxyPort(executorExtensionProxyPort);
        newRobot.setDeviceUuid(deviceUdid);
        newRobot.setDeviceName(deviceName);
        newRobot.setIsDeviceLockUnlock(isDeviceLockUnlock);
        newRobot.setExecutorProxyServiceHost(executorProxyServiceHost);
        newRobot.setExecutorProxyServicePort(executorProxyServicePort);
        newRobot.setExecutorBrowserProxyHost(executorBrowserProxyHost);
        newRobot.setExecutorProxyType(executorProxyType);
        newRobot.setExecutorBrowserProxyPort(executorBrowserProxyPort);
        newRobot.setExecutorExtensionPort(executorExtensionPort);
        newRobot.setDescription(description);
        newRobot.setUsrCreated(UsrCreated);
        newRobot.setDateCreated(DateCreated);
        newRobot.setUsrModif(UsrModif);
        newRobot.setDateModif(DateModif);
        newRobot.setDevicePort(devicePort);
        return newRobot;
    }

}
