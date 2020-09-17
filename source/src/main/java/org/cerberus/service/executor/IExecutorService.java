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
package org.cerberus.service.executor;

import org.cerberus.crud.entity.TestCaseExecution;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.exception.CerberusEventException;
import org.cerberus.exception.CerberusException;
import org.json.JSONObject;

/**
 *
 * @author vertigo17
 */
public interface IExecutorService {

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
     * @throws org.cerberus.exception.CerberusEventException
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
     * @return
     * @throws CerberusException
     */
    public JSONObject getHar(String urlFilter, boolean withContent, String exHost, Integer exPort, String exUuid, String system) throws CerberusException;

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

}
