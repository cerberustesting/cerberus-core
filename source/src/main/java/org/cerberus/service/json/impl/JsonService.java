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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.InvalidPathException;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONStyle;
import org.cerberus.service.json.IJsonService;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
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
    public String callUrlAndGetJsonResponse(String url) throws MalformedURLException {
        StringBuilder sb = new StringBuilder();
        URL urlToCall = new URL(url);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(urlToCall.openStream()))) {
            String str;
            while (null != (str = br.readLine())) {
                sb.append(str);
            }
        } catch (IOException ex) {
            LOG.warn(String.format("Error Getting Json File %s", ex));
        }
        return sb.toString();
    }

    /**
     * Get element (from attributeToFind) from Json file either from the url
     * called or jsonmessage.
     *
     * @param jsonMessage
     * @param url             URL of the Json file to parse
     * @param attributeToFind
     * @return Value of the element from the Json File or null if the element is
     * not found.
     */
    @Override
    public String getFromJson(String jsonMessage, String url, String attributeToFind) throws InvalidPathException {
        if (attributeToFind == null) {
            LOG.warn("Null argument");
            return DEFAULT_GET_FROM_JSON_VALUE;
        }

        //Get the Json File in string format
        String json = "";
        if (url == null) {
            json = jsonMessage;
        } else {
            try {
                json = this.callUrlAndGetJsonResponse(url);
            } catch (MalformedURLException e) {
                LOG.warn("Malformed URL");
            }
        }

        //Get the value
        Object document = Configuration.defaultConfiguration().jsonProvider().parse(json);
        String jsonPath = checkJsonPathFormat(attributeToFind);
        //LOG.debug("JSON PATH : " + jsonPath);
        //List<Object> objects = new ArrayList<Object>();
        //LOG.debug("ttttttttttt objects " + objects.getClass().getSimpleName());
        //List<String> strings = new ArrayList<String>();
        //strings.add("test");
        //LOG.debug("ttttttttttt strings " + strings.get(0).getClass().getSimpleName());
        //LOG.debug("ttttttttttt strings " + strings.getClass().getCanonicalName());
        //LOG.debug("ttttttttttt strings " + strings.getClass().getTypeName());
        /*List<Object> test = new ArrayList<>();
        switchCorrectObjectList(test);*/

        return switchCorrectObject(JsonPath.read(document, jsonPath));
        /*try {
            //Maybe it is a string.
            LOG.debug("JSON PATH trying String : " + jsonPath);
            Object test = JsonPath.read(json, jsonPath);
            LOG.debug("ttttttttt " + test.getClass().getSimpleName());
            String tryString = JsonPath.read(json, jsonPath);
            LOG.debug("CLASS : " + JsonPath.read(document, jsonPath).getClass().getSimpleName());
            LOG.debug("CLASS with getName: " + tryString.getClass().getSimpleName());
            return String.valueOf(tryString);

        } catch (Exception exString) {
            try {
                //Maybe it is an Integer.
                LOG.debug("JSON PATH trying Integer : " + jsonPath);
                LOG.debug("CLASS : " + JsonPath.read(document, jsonPath).getClass().getSimpleName());
                int tryInt = JsonPath.read(document, jsonPath);
                return String.valueOf(tryInt);

            } catch (Exception exInt) {
                try {
                    //Maybe it is a Boolean.
                    LOG.debug("JSON PATH trying Boolean : " + jsonPath);
                    LOG.debug("CLASS : " + JsonPath.read(document, jsonPath).getClass().getSimpleName());
                    Boolean tryBoolean = JsonPath.read(document, jsonPath);
                    return tryBoolean.toString();

                } catch (Exception exBool) {
                    try {
                        //Maybe it is a JSONArray.
                        LOG.debug("JSON PATH trying JSONArray : " + jsonPath);
                        LOG.debug("CLASS : " + JsonPath.read(document, jsonPath).getClass().getSimpleName());
                        JSONArray tryJSONArray = JsonPath.read(document, jsonPath);
                        return tryJSONArray.toString(JSONStyle.LT_COMPRESS);

                    } catch (Exception exArray) {
                        try {
                            LOG.debug("JSON PATH trying float : " + jsonPath);
                            LOG.debug("CLASS : " + JsonPath.read(document, jsonPath).getClass().getSimpleName());
                            double tryFloat = JsonPath.read(document, jsonPath);
                            return String.valueOf(tryFloat);
                        } catch (Exception exFloat) {
                            try {
                                LOG.debug("JSON PATH trying Object : " + jsonPath);
                                LOG.debug("CLASS : " + JsonPath.read(document, jsonPath).getClass().getSimpleName());
                                Object tryObject = JsonPath.read(document, jsonPath);
                                return tryObject.toString();
                            } catch (Exception exObject) {
                                return DEFAULT_GET_FROM_JSON_VALUE;
                            }
                        }
                    }
                }
            }
        }*/

    }

    @Override
    public String getRawFromJson(String jsonMessage, String attributeToFind) throws JsonProcessingException {
        String jsonPath = checkJsonPathFormat(attributeToFind);
        ObjectMapper objectMapper = new ObjectMapper();

        //Exception PathNotFound throwed by read method when not elements found
        JsonNode jsonElementsSearched = JsonPath.using(
                        Configuration
                                .defaultConfiguration()
                                .jsonProvider(new JacksonJsonNodeJsonProvider()))
                .parse(jsonMessage)
                .read(jsonPath);
        return objectMapper.writeValueAsString(jsonElementsSearched);
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
        //Get the Json File in string format
        String json = jsonMessage;

        //Get the value
        Object document = Configuration.defaultConfiguration().jsonProvider().parse(json);
        String jsonPath = checkJsonPathFormat(attributeToFind);

        LOG.debug("JSON PATH : " + jsonPath);
        try {
            //Maybe it is a List of string.
            LOG.debug("JSON PATH trying ListOfObject : " + jsonPath);
            List<Object> toto = JsonPath.read(document, jsonPath);
            List<String> result = new ArrayList<>();
            for (Object obj : toto) {
                result.add(String.valueOf(obj));
            }
            return result;

        } catch (Exception exString) {
            LOG.debug("JSON PATH ListOfObject exception.");
            try {
                //Maybe it is an Integer.
                LOG.debug("JSON PATH trying ListOfInteger : " + jsonPath);
                List<Integer> toto = JsonPath.read(document, jsonPath);
                List<String> result = new ArrayList<>();
                for (Integer inte : toto) {
                    result.add(String.valueOf(inte));
                }
                return result;

            } catch (Exception exIntList) {
                LOG.debug("JSON PATH ListOfInteger Exception.");
                try {
                    //Maybe it is an Integer.
                    LOG.debug("JSON PATH trying String : " + jsonPath);
                    String toto = JsonPath.read(document, jsonPath);
                    List<String> result = new ArrayList<>();
                    result.add(toto);
                    return result;

                } catch (Exception exInt) {
                    LOG.debug("JSON PATH String Exception.");
                    try {
                        //Maybe it is an JSONArray.
                        LOG.debug("JSON PATH trying Integer : " + jsonPath);
                        int toto = JsonPath.read(document, jsonPath);
                        List<String> result = new ArrayList<>();
                        result.add(String.valueOf(toto));
                        return result;

                    } catch (Exception exBool) {
                        LOG.debug("JSON PATH Integer Exception.");
                        try {
                            //Maybe it is an JSONArray.
                            LOG.debug("JSON PATH trying Boolean : " + jsonPath);
                            Boolean toto = JsonPath.read(document, jsonPath);
                            List<String> result = new ArrayList<>();
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

    //Private methods

    private String checkJsonPathFormat(String path) {
        return (!path.startsWith("$.") && !path.startsWith("$[")) ? String.format("$.%s", path) : path;
    }

    private String switchCorrectObject(Object value) {
        switch (value.getClass().getSimpleName()) {
            case "String":
                LOG.debug("object string");
                return String.valueOf(value);
            case "Integer":
                LOG.debug("object integer");
                return String.valueOf(((int) value));
            case "Boolean":
                LOG.debug("object boolean");
                return ((Boolean) value).toString();
            case "JSONArray":
                LOG.debug("object jsonarray");
                return ((JSONArray) value).toString(JSONStyle.LT_COMPRESS);
            case "Double":
                LOG.debug("object double");
                return String.valueOf((double) value);
            default:
                LOG.debug("object other type");
                try {
                    return value.toString();
                } catch (Exception e) {
                    return DEFAULT_GET_FROM_JSON_VALUE;
                }

        }
    }

    private List<String> switchCorrectObjectList(Object value) {
        List<String> result = new ArrayList<>();
        LOG.debug("VALUE : " + value.getClass().getSimpleName());
        if (value.getClass().getSimpleName().equals("ArrayList")) {
            try {
                switch (((ArrayList) value).get(0).getClass().getSimpleName()) {
                    case "Integer": //List of integer
                        List<Integer> integer = (ArrayList) value;
                        for (Integer inte : integer) {
                            result.add(String.valueOf(inte));
                        }
                        return result;
                    default: //List of object
                        List<Object> objects = (ArrayList) value;
                        for (Object obj : objects) {
                            result.add(String.valueOf(obj));
                        }
                        return result;
                }
            } catch (IndexOutOfBoundsException exception) {
                return new ArrayList<>();
            }
        } else {
            //List<String> result = new ArrayList<>();
            switch (value.getClass().getSimpleName()) {
                case "String":
                    result.add(value.toString());
                    return result;
                case "Integer":
                    result.add(String.valueOf(((int) value)));
                    return result;
                case "Boolean":
                    result.add(((Boolean) value).toString());
                    return result;
            }
        }
        return result;
        //if value = list
        //if value != list


        //return null;
    }
}
