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
package org.cerberus.core.crud.entity;

import org.json.JSONException;
import org.json.JSONObject;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * @author bcivel
 */
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
    public static final String IDNAME_TCSTATUS = "TCSTATUS";

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVeryShortDesc() {
        return veryShortDesc;
    }

    public void setVeryShortDesc(String veryShortDesc) {
        this.veryShortDesc = veryShortDesc;
    }

    public String getGp1() {
        return gp1;
    }

    public void setGp1(String gp1) {
        this.gp1 = gp1;
    }

    public String getGp2() {
        return gp2;
    }

    public void setGp2(String gp2) {
        this.gp2 = gp2;
    }

    public String getGp3() {
        return gp3;
    }

    public void setGp3(String gp3) {
        this.gp3 = gp3;
    }

    public String getGp4() {
        return gp4;
    }

    public void setGp4(String gp4) {
        this.gp4 = gp4;
    }

    public String getGp5() {
        return gp5;
    }

    public void setGp5(String gp5) {
        this.gp5 = gp5;
    }

    public String getGp6() {
        return gp6;
    }

    public void setGp6(String gp6) {
        this.gp6 = gp6;
    }

    public String getGp7() {
        return gp7;
    }

    public void setGp7(String gp7) {
        this.gp7 = gp7;
    }

    public String getGp8() {
        return gp8;
    }

    public void setGp8(String gp8) {
        this.gp8 = gp8;
    }

    public String getGp9() {
        return gp9;
    }

    public void setGp9(String gp9) {
        this.gp9 = gp9;
    }

    public String getIdName() {
        return idName;
    }

    public void setIdName(String idName) {
        this.idName = idName;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Invariant{" + "idName=" + idName + ", value=" + value + ", sort=" + sort + ", description=" + description + ", veryShortDesc=" + veryShortDesc + ", gp1=" + gp1 + ", gp2=" + gp2 + ", gp3=" + gp3 + ", gp4=" + gp4 + ", gp5=" + gp5 + ", gp6=" + gp6 + ", gp7=" + gp7 + ", gp8=" + gp8 + ", gp9=" + gp9 + '}';
    }

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
