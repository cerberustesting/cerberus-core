/**
 * Cerberus Copyright (C) 2013 - 2026 cerberustesting
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
package org.cerberus.core.service.robotproxy;

import org.cerberus.core.crud.entity.RobotExecutor;
import org.cerberus.core.service.robotproxy.entity.RelayException;
import org.cerberus.core.service.robotproxy.entity.RelayRequest;
import org.cerberus.core.service.robotproxy.entity.RelayResponse;

/**
 * Client of the HTTP relay exposed by a remote runner. When the relay
 * of a Robot Executor is active, the service calls of an execution are sent to
 * the relay of the runner that performs them from its own network.
 *
 * @author bcivel
 */
public interface IRelayService {

    /**
     * @param executor the Robot Executor of the execution (can be null)
     * @return true if the calls must go through the relay of the runner.
     */
    boolean isRelayActive(RobotExecutor executor);

    /**
     * Base url of the runner ("http://host:port", or "https://host" when the
     * port is 443).
     *
     * @param executor
     * @return the base url, never ending with a slash.
     * @throws RelayException with code {@link RelayException#CODE_NOT_CONFIGURED}
     * when the Proxy service host is missing.
     */
    String getBaseUrl(RobotExecutor executor) throws RelayException;

    /**
     * Checks the relay is reachable, the token accepted and the relay not
     * paused.
     *
     * @param executor
     * @throws RelayException when the relay cannot be used.
     */
    void check(RobotExecutor executor) throws RelayException;

    /**
     * Performs an HTTP call through the relay. A response of the target (even
     * 4xx or 5xx) is returned normally.
     *
     * @param executor
     * @param request
     * @return the response of the target.
     * @throws RelayException when the relay itself failed.
     */
    RelayResponse call(RobotExecutor executor, RelayRequest request) throws RelayException;

}
