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
package org.cerberus.crud.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author bcivel
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Getter
@Setter
public class Invariant {

    private String idName;
    private String value;
    private Integer sort;
    private String description;
    private String veryShortDesc;
    private String gp1;
    private String gp2;
    private String gp3;
    private String gp4;
    private String gp5;
    private String gp6;
    private String gp7;
    private String gp8;
    private String gp9;

    /**
     * Not included in table.
     */
    private static final Logger LOG = LogManager.getLogger(Invariant.class);

    public static final String IDNAME_COUNTRY = "COUNTRY";
    public static final String IDNAME_PRIORITY = "PRIORITY";
    public static final String IDNAME_ENVIRONMENT = "ENVIRONMENT";
    public static final String IDNAME_SYSTEM = "SYSTEM";


    public JSONObject toJson(boolean fatVersion) {
        JSONObject invariantJson = new JSONObject();
        try {
            if (fatVersion) {
                invariantJson.put("idName", this.getIdName());
                invariantJson.put("sort", this.getSort());
                invariantJson.put("veryShortDesc", this.getVeryShortDesc());
                invariantJson.put("gp4", this.getGp4());
                invariantJson.put("gp5", this.getGp5());
                invariantJson.put("gp6", this.getGp6());
                invariantJson.put("gp7", this.getGp7());
                invariantJson.put("gp8", this.getGp8());
                invariantJson.put("gp9", this.getGp9());
            }
            invariantJson.put("value", this.getValue());
            invariantJson.put("description", this.getDescription());
            invariantJson.put("gp1", this.getGp1());
            invariantJson.put("gp2", this.getGp2());
            invariantJson.put("gp3", this.getGp3());
        } catch (JSONException ex) {
            LOG.error(ex.toString(), ex);
        }
        return invariantJson;
    }

    public JSONObject toJsonV001() {
        JSONObject result = new JSONObject();
        try {
            result.put("JSONVersion", "001");
            result.put("idName", this.getIdName());
            result.put("value", this.getValue());
            result.put("description", this.getDescription());
            result.put("shortDescription", this.getVeryShortDesc());
            result.put("attribute1", this.getGp1());
            result.put("attribute2", this.getGp2());
            result.put("attribute3", this.getGp3());
            result.put("attribute4", this.getGp4());
            result.put("attribute5", this.getGp5());
            result.put("attribute6", this.getGp6());
            result.put("attribute7", this.getGp7());
            result.put("attribute8", this.getGp8());
            result.put("attribute9", this.getGp9());
        } catch (JSONException ex) {
            LOG.error(ex.toString(), ex);
        }
        return result;
    }
}
