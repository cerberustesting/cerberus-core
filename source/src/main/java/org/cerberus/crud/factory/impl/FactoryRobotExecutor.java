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
package org.cerberus.crud.factory.impl;

import java.sql.Timestamp;
import org.cerberus.crud.entity.RobotExecutor;
import org.cerberus.crud.factory.IFactoryRobotExecutor;
import org.springframework.stereotype.Service;

/**
 * @author bcivel
 */
@Service
public class FactoryRobotExecutor implements IFactoryRobotExecutor {

    @Override
    public RobotExecutor create(Integer ID, String robot, String executor, String active, Integer rank, String host, String port, String hostUser, String hostPassword, String deviceUdid, String deviceName, Integer devicePort, String deviceLockUnlock,String executorExtensionHost, Integer executorExtensionPort, String executorProxyHost, Integer executorProxyPort, String executorProxyActive, String description,
            String UsrCreated, Timestamp DateCreated, String UsrModif, Timestamp DateModif) {
        RobotExecutor newRobot = new RobotExecutor();
        newRobot.setID(ID);
        newRobot.setRobot(robot);
        newRobot.setExecutor(executor);
        newRobot.setActive(active);
        newRobot.setRank(rank);
        newRobot.setHost(host);
        newRobot.setPort(port);
        newRobot.setHostUser(hostUser);
        newRobot.setHostPassword(hostPassword);
        newRobot.setDeviceUuid(deviceUdid);
        newRobot.setDeviceName(deviceName);
        newRobot.setDeviceLockUnlock(deviceLockUnlock);
        newRobot.setExecutorExtensionHost(executorExtensionHost);
        newRobot.setExecutorExtensionPort(executorExtensionPort);
        newRobot.setExecutorProxyHost(executorProxyHost);
        newRobot.setExecutorProxyActive(executorProxyActive);
        newRobot.setExecutorProxyPort(executorProxyPort);
        newRobot.setDescription(description);
        newRobot.setUsrCreated(UsrCreated);
        newRobot.setDateCreated(DateCreated);
        newRobot.setUsrModif(UsrModif);
        newRobot.setDateModif(DateModif);
        newRobot.setDevicePort(devicePort);
        return newRobot;
    }

}
