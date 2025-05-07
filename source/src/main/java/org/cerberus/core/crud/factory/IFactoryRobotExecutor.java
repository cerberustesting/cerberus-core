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

import java.sql.Timestamp;
import org.cerberus.core.crud.entity.RobotExecutor;

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
     * @param isActive Robot active
     * @param description description of the Robot
     * @param hostUser
     * @param executorExtensionProxyPort
     * @param hostPassword
     * @param deviceName
     * @param devicePort
     * @param isDeviceLockUnlock
     * @param executorProxyServiceHost
     * @param executorProxyServicePort
     * @param executorBrowserProxyHost
     * @param executorBrowserProxyPort
     * @param executorExtensionPort
     * @param deviceUdid
     * @param UsrCreated
     * @param executorProxyType
     * @param DateCreated
     * @param UsrModif
     * @param DateModif
     * @return
     */
    RobotExecutor create(Integer ID, String robot, String executor, boolean isActive, 
            Integer rank, String host, String port, String hostUser, String hostPassword, Integer executorExtensionProxyPort, String deviceUdid,
            String deviceName, Integer devicePort, boolean isDeviceLockUnlock, String executorProxyServiceHost, Integer executorProxyServicePort, String executorBrowserProxyHost, Integer executorBrowserProxyPort, Integer executorExtensionPort, String executorProxyType,
            String description, String UsrCreated, Timestamp DateCreated, String UsrModif, Timestamp DateModif);
}
