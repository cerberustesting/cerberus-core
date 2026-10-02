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
import org.cerberus.core.mcpdelta.read.ReadService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ReadTool implements Tool {

    private final ReadService service;
    private final int defaultBudget;

    public ReadTool(ReadService service, int defaultBudget) {
        this.service = service;
        this.defaultBudget = defaultBudget;
    }

    @Override
    public String name() {
        return "read";
    }

    @Override
    public String description() {
        return "Read Cerberus as compact text, several refs per call. \"live:<url>\" = a page as the robot's browser shows it (visible elements, "
                + "regions, closed menus, selectors), no testcase needed. \"Folder/Testcase\" = the testcase document (what write accepts back); "
                + "\"Folder\", \"Folder/PREFIX-*\", \"app:X\", \"label:X\", \"*\" = listings with last run; \"run:<id>\" = an execution analysed; "
                + "\"tag:<tag>\" = all executions of a tag; \"runs:Folder/Testcase\" = history; \"page:<id>\" = outline of the page the robot saved "
                + "(its elements, ids, texts); \"targets\" = valid country/env/robot; "
                + "\"catalog\", \"catalog:<action|control>\", \"catalog:variables\" = what exists; \"executions status=FA env=QA\"; \"file:<id>\"; "
                + "\"library\" = library steps and duplicates; objects as documents: \"application:X\", \"service:X\", \"campaign:X\", \"robot:X\", "
                + "\"environment:SYS/FR/QA\", \"folder:X\", \"invariant:ID\", \"labels:SYS\", \"datalib:X\", \"context:login\", and their plurals "
                + "(\"robots\", \"applications:DEFAULT\"); \"journal\"; \"version\"; \"guide\" = full syntax.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("refs", Map.of("type", "array", "items", Map.of("type", "string")));
        props.put("full", Map.of("type", "boolean", "description", "Every detail (all execution lines, hidden attributes)."));
        props.put("budget", Map.of("type", "integer", "description", "Max tokens to return (default " + defaultBudget + ")."));
        return Map.of("type", "object", "properties", props, "required", List.of("refs"));
    }

    @Override
    public boolean readOnly() {
        return true;
    }

    @Override
    public String call(JsonNode args) {
        org.cerberus.core.mcpdelta.util.Access.require(org.cerberus.core.mcpdelta.util.Access.READ, "read Cerberus data");
        List<String> refs = Args.list(args, "refs");
        refs.addAll(Args.list(args, "ref"));
        if (refs.isEmpty()) {
            throw new ToolError("read needs refs, e.g. [\"DemoShop/DEMO-001\"] or [\"DemoShop\"]");
        }
        return service.read(refs, Args.bool(args, "full", false), Args.integer(args, "budget", defaultBudget));
    }
}
