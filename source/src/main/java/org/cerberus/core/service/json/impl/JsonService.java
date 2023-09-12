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
package org.cerberus.core.service.json.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.InvalidPathException;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONStyle;
import org.cerberus.core.service.json.IJsonService;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
     * @param jsonMessage     JSON Content
     * @param url             URL of the Json file to parse
     * @param attributeToFind The path of the searched element
     * @return Value of the element from the Json File or PathNotFoundException if the element is
     * not found.
     */
    @Override
    public String getFromJson(String jsonMessage, String url, String attributeToFind) throws InvalidPathException {
        if (attributeToFind == null) {
            LOG.warn("Null argument");
            throw new InvalidPathException();
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
        Object value = JsonPath.read(document, jsonPath);
        //Value "null" in the json.
        if (value == null) value = "null";
        //When using $.. syntax, it will return an empty array [] in case of no element found
        if (value.toString().equals("[]")) {
            throw new PathNotFoundException();
        }
        return castObjectAccordingToJson(value);
    }

    /**
     * Get element from a JSON content
     *
     * @param jsonMessage     JSON Content
     * @param attributeToFind The path of the searched element
     * @return A string according to the standard JSON Format of the searched element (i.e '{ key:"value", key2:"value2" }')
     * @throws JsonProcessingException Error with Jackson when he tries to write the value
     */
    @Override
    public String getRawFromJson(String jsonMessage, String attributeToFind) throws JsonProcessingException {
        String jsonPath = checkJsonPathFormat(attributeToFind);
        ObjectMapper objectMapper = new ObjectMapper();

        //Exception PathNotFoundException throwed by read method when not elements found
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
     * @param jsonMessage     JSON Content
     * @param attributeToFind The path of the searched element
     * @return Value of the element from the Json File or null if the element is
     * not found.
     */
    @Override
    public List<String> getFromJson(String jsonMessage, String attributeToFind) throws Exception {
        if (attributeToFind == null) {
            LOG.warn("Null argument");
            return null;
        }

        //Get the value
        Object document = Configuration.defaultConfiguration().jsonProvider().parse(jsonMessage);
        String jsonPath = checkJsonPathFormat(attributeToFind);

        //When JsonPath returns a list
        if (JsonPath.read(document, jsonPath) instanceof List) {
            List<Object> jsonSearchedElements = JsonPath.read(document, jsonPath);
            return jsonSearchedElements
                    .stream()
                    .map(this::castObjectAccordingToJson)
                    .collect(Collectors.toList());
        } else {
            List<String> jsonSearchedElements = new ArrayList<>();
            jsonSearchedElements.add(this.castObjectAccordingToJson(JsonPath.read(document, jsonPath)));
            return jsonSearchedElements;
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

    /**
     * Add required elements for the json path if necessary
     *
     * @param path The JSON Path entered by the user
     * @return Correct path
     */
    private String checkJsonPathFormat(String path) {
        return (!path.startsWith("$.") && !path.startsWith("$[")) ? String.format("$.%s", path) : path;
    }

    /**
     * Cast and return a string according to the object in the JSON
     *
     * @param value The object which is returned by JsonPath.read() method
     * @return String which represent the value of the object
     */
    private String castObjectAccordingToJson(Object value) {
        if (value instanceof String) {
            return value.toString();
        } else if (value instanceof Integer) {
            return ((Integer) value).toString();
        } else if (value instanceof Boolean) {
            return ((Boolean) value).toString();
        } else if (value instanceof JSONArray) {
            return ((JSONArray) value).toString(JSONStyle.LT_COMPRESS);
        } else if (value instanceof Double) {
            return ((Double) value).toString();
        } else {
            try {
                return value.toString();
            } catch (Exception e) {
                return DEFAULT_GET_FROM_JSON_VALUE;
            }
        }
    }
}
