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
package org.cerberus.core.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.util.json.ObjectMapperUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Utility class centralizing string utility methods
 *
 * @author Tiago Bernardes
 * @version 1.0, 10/01/2013
 * @since 2.0.0
 */
public final class JSONUtil {

    private static final Logger LOG = LogManager.getLogger(JSONUtil.class);

    /**
     * To avoid instantiation of utility class
     */
    private JSONUtil() {
    }

    /**
     * @param param
     * @return
     * @throws org.json.JSONException
     */
    public static Map<String, Object> convertFromJSONObject(JSONObject param) throws JSONException {
        Map<String, Object> params = new HashMap<>();
        @SuppressWarnings("unchecked")
        Iterator<String> keys = param.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            if (param.get(key) instanceof JSONObject) {
                LOG.debug("Still an Object.");
                // do something with jsonObject here      
            } else if (param.get(key) instanceof JSONArray) {
                ArrayList<String> newtoto = new ArrayList<>();
                JSONArray newJsonArray = (JSONArray) param.get(key);
                for (int i = 0; i < newJsonArray.length(); i++) {
                    newtoto.add(newJsonArray.getString(i));
                }
                params.put(key, newtoto);
            } else {
                params.put(key, param.get(key));
            }
        }
        return params;
    }

    /**
     * @param param
     * @return
     * @throws org.json.JSONException
     */
    public static Map<String, Object> convertFromJSONObjectString(String param) throws JSONException {
        JSONObject jsonParam = new JSONObject(param);
        return convertFromJSONObject(jsonParam);
    }

    public static boolean isJSONValid(String jsonString) {
        final ObjectMapper mapper = ObjectMapperUtil.newDefaultInstance();
        try {
            mapper.readTree(jsonString);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Verify if a value is present in a JSONArray
     * @param array array to search
     * @param value value to search
     * @return false is value is not present, true if value is present
     * @throws JSONException
     */
    public static boolean jsonArrayContains(JSONArray array, String value) throws JSONException {
        for (int i = 0; i < array.length(); i++) {
            if (array.getString(i).equals(value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Create a JSONArray with distinct values from another JSONArray
     * @param source source array with duplicated values
     * @param target target array with distinct values
     * @return JSONArray with distinct values
     * @throws JSONException
     */
    public static JSONArray jsonArrayAddUniqueElement(JSONArray source, JSONArray target) throws JSONException {
        for (int i = 0; i < source.length(); i++) {
            String element = source.getString(i);
            if (!jsonArrayContains(target, element)) {
                target.put(element);
            }
        }
        return target;
    }
}
