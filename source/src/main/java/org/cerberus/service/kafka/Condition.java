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
package org.cerberus.service.kafka;

import com.jayway.jsonpath.JsonPath;

/**
 * Using Jayway JsonPath to search json objects for matches See
 * https://github.com/json-path/JsonPath for examples
 *
 * @author Pete
 */
public class Condition {

    private String path;
    private String value;

    public boolean test(String json) {
        try {
            String extractedField = JsonPath.read(json, path);
            if (extractedField.contentEquals(value)) {
                return true;
            }
        } catch (Exception ex) {
            //catch any errors, not json etc
            return false;
        }
        return false;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
