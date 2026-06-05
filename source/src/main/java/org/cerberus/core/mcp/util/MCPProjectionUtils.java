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
package org.cerberus.core.mcp.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class MCPProjectionUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private MCPProjectionUtils() {
    }

    public static Map<String, Object> project(Object dto, List<String> fields) {
        Map<String, Object> fullDtoAsMap = OBJECT_MAPPER.convertValue(
                dto,
                new TypeReference<Map<String, Object>>() {
                }
        );

        Map<String, Object> projectedDto = new LinkedHashMap<>();

        for (String field : fields) {
            if (fullDtoAsMap.containsKey(field)) {
                projectedDto.put(field, fullDtoAsMap.get(field));
            }
        }

        return projectedDto;
    }
}