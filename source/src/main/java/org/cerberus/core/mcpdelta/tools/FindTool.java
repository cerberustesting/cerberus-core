/**
 * Cerberus Copyright (C) 2013 - 2026 cerberustesting
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
package org.cerberus.core.mcpdelta.tools;

import com.fasterxml.jackson.databind.JsonNode;
import org.cerberus.core.mcpdelta.read.FindService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class FindTool implements Tool {

    private final FindService service;

    public FindTool(FindService service) {
        this.service = service;
    }

    @Override
    public String name() {
        return "find";
    }

    @Override
    public String description() {
        return "Find where a value is used: matching document lines across a scope, grouped by testcase and step. "
                + "in = Folder | Folder/PREFIX-* | app:X | label:X | * (default). Case-insensitive unless caseSensitive.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("text", Map.of("type", "string"));
        props.put("in", Map.of("type", "string"));
        props.put("regex", Map.of("type", "boolean"));
        props.put("caseSensitive", Map.of("type", "boolean"));
        props.put("limit", Map.of("type", "integer", "description", "Max lines (default 100)."));
        return Map.of("type", "object", "properties", props, "required", List.of("text"));
    }

    @Override
    public boolean readOnly() {
        return true;
    }

    @Override
    public String call(JsonNode args) {
        org.cerberus.core.mcpdelta.util.Access.require(org.cerberus.core.mcpdelta.util.Access.READ, "search Cerberus data");
        return service.find(Args.str(args, "text", Args.str(args, "query", "")), Args.str(args, "in", Args.str(args, "scope", "*")),
                Args.bool(args, "regex", false), Args.bool(args, "caseSensitive", false), Args.integer(args, "limit", 100));
    }
}
