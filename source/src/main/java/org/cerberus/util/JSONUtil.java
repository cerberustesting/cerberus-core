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
package org.cerberus.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
     *
     * @param param
     * @return
     * @throws org.json.JSONException
     */
    public static Map<String, Object> convertFromJSONObject(JSONObject param) throws JSONException {
        Map<String, Object> params = new HashMap<>();
        Iterator keys = param.keys();
        while (keys.hasNext()) {
            String key = (String) keys.next();
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
     *
     * @param param
     * @return
     * @throws org.json.JSONException
     */
    public static Map<String, Object> convertFromJSONObjectString(String param) throws JSONException {
        JSONObject jsonParam = new JSONObject(param);
        return convertFromJSONObject(jsonParam);
    }

}
