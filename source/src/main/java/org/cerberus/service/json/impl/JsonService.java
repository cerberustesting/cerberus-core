/*
 * Cerberus  Copyright (C) 2013  vertigo17
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
package org.cerberus.service.json.impl;

import org.cerberus.engine.execution.impl.ExecutionRunService;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import org.cerberus.service.json.IJsonService;
import org.cerberus.util.StringUtil;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 */
@Service
public class JsonService implements IJsonService {

    public static final String DEFAULT_GET_FROM_JSON_VALUE = null;

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ExecutionRunService.class);

    /**
     * Get Json from URL and convert it into JSONObject format
     *
     * @param url Url location of the Json file to download.
     * @return JsonObject downloaded.
     */
    @Override
    public String callUrlAndGetJsonResponse(String url) {
        String str = "";
        StringBuilder sb = new StringBuilder();
        try {
            URL urlToCall = new URL(url);
            BufferedReader br = new BufferedReader(new InputStreamReader(urlToCall.openStream()));
            while (null != (str = br.readLine())) {
                sb.append(str);
            }
        } catch (IOException ex) {
            LOG.warn("Error Getting Json File " + ex);
        }
        return sb.toString();
    }

    /**
     * Get element value from Json file
     *
     * @param jsonMessage
     * @param url URL of the Json file to parse
     * @param attributeToFind
     * @return Value of the element from the Json File
     */
    @Override
    public String getFromJson(String jsonMessage, String url, String attributeToFind) {
        if (attributeToFind == null) {
            LOG.warn("Null argument");
            return DEFAULT_GET_FROM_JSON_VALUE;
        }

        String result;
        /**
         * Get the Json File in string format
         */
        String json;
        if (url == null) {
            json = jsonMessage;
        } else {
            json = this.callUrlAndGetJsonResponse(url);
        }
        /**
         * Get the value
         */
        Object document = Configuration.defaultConfiguration().jsonProvider().parse(json);
        String jsonPath = StringUtil.addPrefixIfNotAlready(attributeToFind, "$.");
        LOG.debug("JSON PATH : " + jsonPath);
        result = JsonPath.read(document, jsonPath);

        return result;
    }
}
