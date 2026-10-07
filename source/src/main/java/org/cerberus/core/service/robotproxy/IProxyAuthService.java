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

import java.util.List;
import org.cerberus.core.crud.entity.RobotExecutor;
import org.cerberus.core.service.robotproxy.entity.RelayException;

/**
 * Authentication of Cerberus towards the Cerberus Robot Proxy of a Robot
 * Executor. It is defined on each executor : NONE, TOKEN (a Bearer token) or
 * OAUTH (client credentials of a service account, eg Keycloak).
 *
 * @author bcivel
 */
public interface IProxyAuthService {

    /**
     * @param executor the Robot Executor owning the proxy
     * @return the value of the Authorization header to send to the proxy, or
     * null when the authentication is NONE.
     * @throws RelayException not_configured when the parameters are missing or
     * unauthorized/unreachable when the OAuth token cannot be obtained.
     */
    String getAuthorizationHeader(RobotExecutor executor) throws RelayException;

    /**
     * @param executor
     * @return the secrets of the executor (and the current access token) to be
     * masked in any message, never null.
     */
    List<String> getSecrets(RobotExecutor executor);

}
