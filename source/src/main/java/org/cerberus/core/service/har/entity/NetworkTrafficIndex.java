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
package org.cerberus.core.service.har.entity;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author vertigo17
 */
public class NetworkTrafficIndex {

    private static final org.apache.logging.log4j.Logger LOG = org.apache.logging.log4j.LogManager.getLogger(NetworkTrafficIndex.class);

    private Integer index;
    private Integer indexRequestNb;
    private String name;

    public Integer getIndexRequestNb() {
        return indexRequestNb;
    }

    public void setIndexRequestNb(Integer index) {
        this.indexRequestNb = index;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public JSONObject toJson() {
        JSONObject result = new JSONObject();
        try {
            result.put("name", this.name);
            result.put("indexRequestNb", this.indexRequestNb);
            result.put("index", this.index);
        } catch (JSONException ex) {
            LOG.error(ex.toString(), ex);
        }
        return result;
    }

}
