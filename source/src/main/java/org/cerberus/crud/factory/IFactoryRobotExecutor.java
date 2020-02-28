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
package org.cerberus.crud.factory;

import java.sql.Timestamp;
import org.cerberus.crud.entity.RobotExecutor;

/**
 * @author bcivel
 */
public interface IFactoryRobotExecutor {

    /**
     *
     * @param ID
     * @param robot Name of the Robot
     * @param executor
     * @param host Host of the Robot
     * @param port port of the Robot
     * @param rank
     * @param active Robot active
     * @param description description of the Robot
     * @param hostUser
     * @param hostPassword
     * @param deviceName
     * @param devicePort
     * @param deviceLockUnlock
     * @param executorExtensionPort
     * @param executorProxyHost
     * @param executorProxyPort
     * @param deviceUdid
     * @param UsrCreated
     * @param executorProxyActive
     * @param DateCreated
     * @param UsrModif
     * @param DateModif
     * @return
     */
    RobotExecutor create(Integer ID, String robot, String executor, String active, 
            Integer rank, String host, String port, String hostUser, String hostPassword, String deviceUdid,
            String deviceName, Integer devicePort, String deviceLockUnlock, String executorExtensionHost, Integer executorExtensionPort, String executorProxyHost, Integer executorProxyPort, String executorProxyActive, 
            String description, String UsrCreated, Timestamp DateCreated, String UsrModif, Timestamp DateModif);
}
