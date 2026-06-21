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
package org.cerberus.core.service.accessibilitylog.impl;

import java.util.HashMap;
import java.util.Map;
import org.cerberus.core.crud.service.IInvariantService;
import org.cerberus.core.crud.service.IParameterService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.cerberus.core.service.accessibilitylog.IAccessibilitylogService;

/**
 *
 * @author bcivel
 */
@Service
public class AccessibilitylogService implements IAccessibilitylogService {

    @Autowired
    private IParameterService parameterService;
    @Autowired
    private IInvariantService invariantService;

    private static final org.apache.logging.log4j.Logger LOG = org.apache.logging.log4j.LogManager.getLogger(AccessibilitylogService.class);

    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.S'Z'";

    @Override
    public JSONObject enrichWithStats(JSONObject logs) {
        LOG.debug("Build Stats from Accessibility Logs.");
        JSONObject result = new JSONObject();
        try {
            HashMap<String, Integer> newEntry;
            HashMap<String, HashMap<String, Integer>> perLevel = new HashMap<>();

            for (String key : logs.keySet()) {
                JSONArray values = logs.getJSONArray(key);

                LOG.debug(key + " " + values);

                HashMap<String, Integer> newEntryImpact = new HashMap<>();
                for (Object value : values) {
                    JSONObject valueJSON = (JSONObject) value;

                    if (newEntryImpact.containsKey("total")) {
                        newEntryImpact.put("total", newEntryImpact.get("total") + 1);
                    } else {
                        newEntryImpact.put("total", 1);
                    }

                    if (newEntryImpact.containsKey("total-nodes")) {
                        newEntryImpact.put("total-nodes", newEntryImpact.get("total-nodes") + valueJSON.getJSONArray("nodes").length());
                    } else {
                        newEntryImpact.put("total-nodes", valueJSON.getJSONArray("nodes").length());
                    }
                    
                    if (valueJSON.has("impact")) {
                        String impact = valueJSON.getString("impact");
                        if (impact.isEmpty()) {
                            impact = "undefined";
                        }
                        if (newEntryImpact.containsKey(impact + "-total")) {
                            newEntryImpact.put(impact + "-total", newEntryImpact.get(impact + "-total") + 1);
                            newEntryImpact.put(impact + "-total-nodes", newEntryImpact.get(impact + "-total-nodes") + valueJSON.getJSONArray("nodes").length());
                        } else {
                            newEntryImpact.put(impact + "-total", 1);
                            newEntryImpact.put(impact + "-total-nodes", valueJSON.getJSONArray("nodes").length());
                        }
                    }

                }

                if (perLevel.containsKey(key)) {
                    newEntry = perLevel.get(key);
                    newEntry.putAll(newEntryImpact);
                    perLevel.put(key, newEntry);
                } else {
                    newEntry = new HashMap<>();
                    newEntry.putAll(newEntryImpact);
                    perLevel.put(key, newEntry);
                }

            }

            for (Map.Entry<String, HashMap<String, Integer>> entry : perLevel.entrySet()) {
                String key = entry.getKey();
                HashMap<String, Integer> val = entry.getValue();
                JSONObject resultEntry = new JSONObject();

                for (Map.Entry<String, Integer> entry2 : val.entrySet()) {
                    String key2 = entry2.getKey();
                    Integer val2 = entry2.getValue();
                    resultEntry.put(key2, val2);
                }
                result.put(key, resultEntry);
            }

        } catch (JSONException ex) {
            LOG.error("Exception when trying to enrich accessibility logs : " + ex.toString());
        } catch (Exception ex) {
            LOG.error("Exception when trying to enrich accessibility logs.", ex);
        }
        return result;
    }

}
