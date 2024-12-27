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

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author memiks
 */
public class GraphicHelper {
    private static final Logger LOGGER = LogManager.getLogger(GraphicHelper.class);

    private GraphicHelper() {
    }

    /**
     *
     * @param label axis label
     * @param value axis value
     * @param color axis color
     * @param highlight axis highlight
     * @return the JSON Object represent the graphic axis
     */
    public static final JSONObject generateAxisForPieBarOrBarColor(String label, int value, String color, String highlight) {
        try {
            JSONObject jSONObject = new JSONObject();
            jSONObject.put("label", label);
            jSONObject.put("value", value);
            jSONObject.put("color", color);
            jSONObject.put("highlight", highlight);
            return jSONObject;
        } catch (JSONException ex) {
            LOGGER.error("Unable to generate Graphic line", ex);
        }
        return null;
    };

    /**
     *
     * @param label axis label
     * @param data array represent the data
     * @param fillColor color fill of the point
     * @param pointColor color of the point
     * @param pointHighlight point highlight color of the axis
     * @return the JSON Object represent the graphic axis
     */
    public static final JSONObject generateAxisForMultiBar(String label, String[] data, String fillColor,
            String pointColor, String pointHighlight) {
        try {
            JSONObject jSONObject = new JSONObject();
            jSONObject.put("label", label);
            jSONObject.put("fillColor", fillColor);
            jSONObject.put("pointColor", pointColor);
            jSONObject.put("pointHighlight", pointHighlight);

            
            JSONArray datas = new JSONArray();
            for (String dataStr : data) {
                if(dataStr != null) {
                    datas.put(Float.parseFloat(dataStr));
                } else {
                    datas.put(Float.parseFloat("0"));
                }
            }
            jSONObject.put("data", datas);

            return jSONObject;
        } catch (JSONException ex) {
            LOGGER.error("Unable to generate Graphic line", ex);
        }
        return null;
    };

    /**
     *
     * @param chartType the type of the chart
     * @param axis the array of data generate by GraphicHelper.generateAxisForMultiBar 
     *              or GraphicHelper.generateAxisForPieBarOrBarColor
     * @return the JSON Object represent the graphic chart
     */
    public static final JSONObject generateChart(ChartType chartType, JSONObject[] axis, String[] labels) {
        try {
            JSONObject jSONObject = new JSONObject();
            jSONObject.put("type", chartType);
            jSONObject.put("axis", new JSONArray(axis));
            if(labels != null && labels.length > 0) {
                jSONObject.put("labels", new JSONArray(labels));
            }

            return jSONObject;
        } catch (JSONException ex) {
            LOGGER.error("Unable to generate Graphic line", ex);
        }
        return null;
    };

    /**
     * this enum is used to specify which Chart use.
     */
    public enum ChartType {
        Pie,
        Donut,
        Bar,
        MultiBar,
        Radar,
        BarColor
    }
}
