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
package org.cerberus.core.service.robotproviders;

import java.util.List;
import org.cerberus.core.engine.entity.Session;
import org.json.JSONObject;

/**
 *
 * @author vertigo17
 */
public interface ILambdaTestService {

    /**
     *
     * @param session
     * @param status TestCase Execution control status.
     */
    public void setSessionStatus(Session session, String status);

    /**
     *
     * @param sessionId
     * @param user
     * @param pass
     * @return
     */
    public List<String> getSeleniumLogs(String sessionId, String user, String pass);

    /**
     *
     * @param sessionId
     * @param user
     * @param pass
     * @return
     */
    public JSONObject getHarLogs(String sessionId, String user, String pass);

    /**
     *
     * @param tag
     * @param user
     * @param pass
     * @param system
     * @return
     */
    public String getBuildValue(String tag, String user, String pass, String system);
    
    /**
     *
     * @param build
     * @param seleniumsession
     * @param user
     * @param pass
     * @param system
     * @return
     */
    String getTestID(String build, String seleniumsession, String user, String pass, String system);
}
