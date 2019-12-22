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
package org.cerberus.service.json.impl;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import net.minidev.json.JSONArray;
import org.cerberus.service.json.IJsonService;
import org.cerberus.util.StringUtil;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 */
@Service
public class JsonService implements IJsonService {

    public static final String DEFAULT_GET_FROM_JSON_VALUE = null;

    private static final org.apache.logging.log4j.Logger LOG = org.apache.logging.log4j.LogManager.getLogger(JsonService.class);

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
        BufferedReader br = null;
        try {
            URL urlToCall = new URL(url);
            br = new BufferedReader(new InputStreamReader(urlToCall.openStream()));
            while (null != (str = br.readLine())) {
                sb.append(str);
            }
        } catch (IOException ex) {
            LOG.warn("Error Getting Json File " + ex);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    LOG.warn(e.toString());
                }
            }
        }
        return sb.toString();
    }

    /**
     * Get element (from attributeToFind) from Json file either from the url
     * called or jsonmessage.
     *
     * @param jsonMessage
     * @param url URL of the Json file to parse
     * @param attributeToFind
     * @return Value of the element from the Json File or null if the element is
     * not found.
     */
    @Override
    public String getFromJson(String jsonMessage, String url, String attributeToFind) throws Exception {
        if (attributeToFind == null) {
            LOG.warn("Null argument");
            return DEFAULT_GET_FROM_JSON_VALUE;
        }

        String result = null;
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
        String jsonPath = attributeToFind;
        if (!attributeToFind.startsWith("$.") && !attributeToFind.startsWith("$[")) {
            jsonPath = "$." + attributeToFind;
        }
        LOG.debug("JSON PATH : " + jsonPath);
        try {
            /**
             * Maybe it is a string.
             */
            LOG.debug("JSON PATH trying Object : " + jsonPath);
            Object obj = JsonPath.read(document, jsonPath);
            return String.valueOf(obj);

        } catch (Exception exString) {
            try {
                /**
                 * Maybe it is an Integer.
                 */
                LOG.debug("JSON PATH trying Integer : " + jsonPath);
                int toto = JsonPath.read(document, jsonPath);
                return String.valueOf(toto);

            } catch (Exception exInt) {
                try {
                    /**
                     * Maybe it is a Boolean.
                     */
                    LOG.debug("JSON PATH trying Boolean : " + jsonPath);
                    Boolean toto = JsonPath.read(document, jsonPath);
                    return toto.toString();

                } catch (Exception exBool) {
                    try {
                        /**
                         * Maybe it is an JSONArray.
                         */
                        LOG.debug("JSON PATH trying JSONArray : " + jsonPath);
                        JSONArray toto = JsonPath.read(document, jsonPath);
                        return toto.toJSONString();

                    } catch (Exception exArray) {
                        return DEFAULT_GET_FROM_JSON_VALUE;
                    }
                }
            }
        }

    }

    /**
     * Get element (from attributeToFind) from jsonMessage
     *
     * @param jsonMessage
     * @param attributeToFind
     * @return Value of the element from the Json File or null if the element is
     * not found.
     */
    @Override
    public List<String> getFromJson(String jsonMessage, String attributeToFind) throws Exception {
        if (attributeToFind == null) {
            LOG.warn("Null argument");
            return null;
        }

//        int resultInt = 0;
        /**
         * Get the Json File in string format
         */
        String json = jsonMessage;

        /**
         * Get the value
         */
        Object document = Configuration.defaultConfiguration().jsonProvider().parse(json);
        String jsonPath = attributeToFind;
        if (!attributeToFind.startsWith("$.") && !attributeToFind.startsWith("$[")) {
            jsonPath = "$." + attributeToFind;
        }
        LOG.debug("JSON PATH : " + jsonPath);
        try {
            /**
             * Maybe it is a List of string.
             */
            LOG.debug("JSON PATH trying ListOfObject : " + jsonPath);
            List<Object> toto = JsonPath.read(document, jsonPath);
            List<String> result = new ArrayList<String>();
            for (Object obj : toto) {
                result.add(String.valueOf(obj));
            }
            return result;

        } catch (Exception exString) {
            LOG.debug("JSON PATH ListOfObject exception.");
            try {
                /**
                 * Maybe it is an Integer.
                 */
                LOG.debug("JSON PATH trying ListOfInteger : " + jsonPath);
                List<Integer> toto = JsonPath.read(document, jsonPath);
                List<String> result = new ArrayList<String>();
                for (Integer inte : toto) {
                    result.add(String.valueOf(inte));
                }
                return result;

            } catch (Exception exIntList) {
                LOG.debug("JSON PATH ListOfInteger Exception.");
                try {
                    /**
                     * Maybe it is an Integer.
                     */
                    LOG.debug("JSON PATH trying String : " + jsonPath);
                    String toto = JsonPath.read(document, jsonPath);
                    List<String> result = new ArrayList<String>();
                    result.add(toto);
                    return result;

                } catch (Exception exInt) {
                    LOG.debug("JSON PATH String Exception.");
                    try {
                        /**
                         * Maybe it is an JSONArray.
                         */
                        LOG.debug("JSON PATH trying Integer : " + jsonPath);
                        int toto = JsonPath.read(document, jsonPath);
                        List<String> result = new ArrayList<String>();
                        result.add(String.valueOf(toto));
                        return result;

                    } catch (Exception exBool) {
                        LOG.debug("JSON PATH Integer Exception.");
                        try {
                            /**
                             * Maybe it is an JSONArray.
                             */
                            LOG.debug("JSON PATH trying Boolean : " + jsonPath);
                            Boolean toto = JsonPath.read(document, jsonPath);
                            List<String> result = new ArrayList<String>();
                            result.add(toto.toString());
                            return result;

                        } catch (Exception exArray) {
                            throw exArray;
                        }
                    }
                }
            }
        }

    }

    @Override
    public String getStringFromJson(String jsonMessage, String filterPath) throws Exception {
        List<String> resultList = getFromJson(jsonMessage, filterPath);
        StringBuilder result = new StringBuilder();
        for (String string : resultList) {
            result.append(string).append(" ");
        }
        return result.toString().trim();
    }

}
