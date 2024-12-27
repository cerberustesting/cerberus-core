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
package org.cerberus.core.service.robotproxy;

import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.exception.CerberusEventException;
import org.cerberus.core.exception.CerberusException;
import org.json.JSONObject;

/**
 *
 * @author vertigo17
 */
public interface IRobotProxyService {

    /**
     *
     * @param tce
     */
    void startRemoteProxy(TestCaseExecution tce);

    /**
     *
     * @param tce
     */
    void stopRemoteProxy(TestCaseExecution tce);

    /**
     *
     * @param exHost
     * @param exPort
     * @param exUuid
     * @param system
     * @return
     * @throws org.cerberus.core.exception.CerberusEventException
     */
    public MessageEvent waitForIdleNetwork(String exHost, Integer exPort, String exUuid, String system) throws CerberusEventException;

    /**
     *
     * @param urlFilter
     * @param withContent
     * @param exHost
     * @param exPort
     * @param exUuid
     * @param system
     * @param indexFrom
     * @return
     * @throws CerberusException
     */
    public JSONObject getHar(String urlFilter, boolean withContent, String exHost, Integer exPort, String exUuid, String system, Integer indexFrom) throws CerberusException;

    /**
     *
     * @param urlFilter
     * @param withContent
     * @param exHost
     * @param exPort
     * @param exUuid
     * @return
     */
    public String getExecutorURL(String urlFilter, boolean withContent, String exHost, Integer exPort, String exUuid);

    /**
     *
     * @param exHost
     * @param exPort
     * @param exUuid
     * @return
     * @throws CerberusEventException
     */
    public Integer getHitsNb(String exHost, Integer exPort, String exUuid) throws CerberusEventException;
}
