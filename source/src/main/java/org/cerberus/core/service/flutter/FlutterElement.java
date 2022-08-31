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
package org.cerberus.core.service.flutter;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import io.appium.java_client.MobileElement;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * To have a look : <br>
 * https://github.com/renjujv/appium-flutterfinder-java
 * https://github.com/appium-userland/appium-flutter-driver
 *
 * @author vertigo
 */
public class FlutterElement extends MobileElement {

    private final Map<String, Object> rawMap;
    private final Gson gson = new Gson();

    public FlutterElement(ImmutableMap<String, Object> rawMap) {
        this.rawMap = rawMap;
        id = serialize(rawMap);
    }

    public Map<String, Object> getRawMap() {
        return rawMap;
    }

    //TODO Optimize usage of maps
    public String serialize(Map<String, Object> rawMap) {
        final JsonPrimitive INSTANCE = new JsonPrimitive(false);
        Map<String, Object> tempMap = new HashMap<String, Object>();
        rawMap.forEach(
                (key, value) -> {
                    if (value instanceof String || value instanceof Integer || value instanceof Boolean) {
                        tempMap.put(key, new JsonPrimitive((String) value));
                    } else if (value instanceof JsonElement) {
                        tempMap.put(key, value);
                    } else {
                        tempMap.put(key, INSTANCE);
                    }
                });
        Map<String, Object> iMap = ImmutableMap.copyOf(tempMap);
        String mapJsonStringified = gson.toJson(tempMap);
        return Base64.getEncoder().encodeToString(mapJsonStringified.getBytes());
    }
}
