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
package org.cerberus.service.json;

import java.util.List;
import org.json.JSONObject;

/**
 *
 * @author bcivel
 */
public interface IJsonService {

    /**
     *
     * @param url
     * @return
     */
    String callUrlAndGetJsonResponse(String url);

    /**
     *
     * @param jsonMessage
     * @param url
     * @param attribute
     * @return
     * @throws java.lang.Exception
     */
    String getFromJson(String jsonMessage, String url, String attribute) throws Exception;

    /**
     *
     * @param jsonMessage
     * @param attribute
     * @return
     * @throws java.lang.Exception
     */
    List<String> getFromJson(String jsonMessage, String attribute) throws Exception;

    /**
     *
     * @param jsonMessage
     * @param filterPath
     * @return
     * @throws Exception
     */
    String getStringFromJson(String jsonMessage, String filterPath) throws Exception;
}
