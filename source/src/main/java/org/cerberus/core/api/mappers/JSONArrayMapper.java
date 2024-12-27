/*
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
package org.cerberus.core.api.mappers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.cerberus.core.util.json.ObjectMapperUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.mapstruct.Mapper;

/**
 * @author mlombard
 */

@Mapper(componentModel = "spring")
public interface JSONArrayMapper {

    public default JsonNode toJsonNode(JSONArray jsonArray) throws JsonProcessingException {
        return jsonArray == null ? null : ObjectMapperUtil.newDefaultInstance().readTree(jsonArray.toString());
    }

    public default JSONArray toJSONArray(JsonNode jsonNode) throws JSONException {
        return jsonNode == null ? null : new JSONArray(jsonNode.toString());
    }
}
