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
package org.cerberus.core.mcp.impl.testdatalib;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import org.cerberus.core.crud.entity.TestDataLib;
import org.cerberus.core.crud.service.ITestDataLibService;
import org.cerberus.core.mcp.MCPTool;
import org.cerberus.core.mcp.util.MCPLogUtils;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.cerberus.core.util.answer.AnswerList;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP tool that lists Cerberus data libraries, under the tool name {@code cerberus_datalib_list}.
 *
 * <p>Answers "which libraries exist, and under what name" so an agent can find the one a property
 * refers to before inspecting it with {@code cerberus_datalib_get}. Kept deliberately shallow: the
 * sub-data mapping is what matters for diagnosis and it belongs to the get tool, where it can be
 * reported for one resolved variant rather than guessed at across many.</p>
 *
 * <p>Delegation: {@link ITestDataLibService#readByVariousByCriteria}.</p>
 */
@Component
public class ListTestDataLibsTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_datalib_list";

    /** Hard ceiling on returned libraries, so a broad search cannot flood the agent's context. */
    private static final int MAX_RESULTS = 200;

    private static final int DEFAULT_LIMIT = 50;

    private final ITestDataLibService testDataLibService;
    private final MCPLogUtils mcpLogUtils;

    public ListTestDataLibsTool(ITestDataLibService testDataLibService, MCPLogUtils mcpLogUtils) {
        this.testDataLibService = testDataLibService;
        this.mcpLogUtils = mcpLogUtils;
    }

    @Override
    public McpServerFeatures.SyncToolSpecification toToolSpecification() {
        return new McpServerFeatures.SyncToolSpecification(
                createTool(),
                (exchange, request) -> {
                    Map<String, Object> args = MCPToolUtils.argumentsOrEmpty(request.arguments());
                    return execute(args);
                }
        );
    }

    /**
     * Builds the MCP tool descriptor.
     *
     * @return the fully-described {@link McpSchema.Tool} for registration with the MCP server.
     */
    private McpSchema.Tool createTool() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("name", Map.of(
                "type", "string",
                "description", "Optional partial name. Matching is a contains on the name only, so DATA_VP "
                        + "finds DATA_VP_SOLD. Leave it out to list everything in a system or of a type."
        ));
        properties.put("system", Map.of(
                "type", "string",
                "description", "Optional system filter."
        ));
        properties.put("type", Map.of(
                "type", "string",
                "description", "Optional type filter.",
                "enum", List.of(
                        TestDataLib.TYPE_INTERNAL,
                        TestDataLib.TYPE_SQL,
                        TestDataLib.TYPE_SERVICE,
                        TestDataLib.TYPE_FILE
                )
        ));
        properties.put("limit", Map.of(
                "type", "integer",
                "description", "Maximum number of libraries to return. Defaults to " + DEFAULT_LIMIT
                        + ", capped at " + MAX_RESULTS + "."
        ));

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Lists the Cerberus data libraries a property of type getFromDataLib can draw from.

                Call this to find the library a property refers to — %property.DATA_VP_SOLD.ENSEIGNE% uses the
                library DATA_VP_SOLD — or to check whether a library exists at all before creating one.

                The same name can appear several times, once per system / environment / country variant. To see
                which variant applies and to inspect the sub-data mapping, call cerberus_datalib_get.
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        null,
                        null,
                        null,
                        null
                ),
                null,
                MCPToolUtils.readOnlyAnnotations("List data libraries", false),
                null
        );
    }

    /**
     * Applies the filters and returns the matching libraries.
     *
     * @param args tool arguments extracted from the MCP request (may be empty but never null).
     * @return a {@link McpSchema.CallToolResult} carrying the libraries, or an error.
     */
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String name = MCPToolUtils.getString(args, "name", "").trim();
        String system = MCPToolUtils.getString(args, "system", "").trim();
        String type = MCPToolUtils.getString(args, "type", "").trim();
        int limit = Math.min(Math.max(MCPToolUtils.getInteger(args, "limit", DEFAULT_LIMIT), 1), MAX_RESULTS);

        mcpLogUtils.call(TOOL_NAME, "datalib_list",
                String.format("MCP tool %s called with name=%s system=%s type=%s", TOOL_NAME, name, system, type));

        // readByVariousByCriteria compares the name with plain equality, so a partial name has to
        // go through readNameListByName, which is the only call that does a contains. Filtering on
        // system and type is then applied here rather than losing the partial match.
        AnswerList<TestDataLib> answer = name.isBlank()
                ? testDataLibService.readByVariousByCriteria(
                        null,
                        system.isBlank() ? null : List.of(system),
                        null, null,
                        type.isBlank() ? null : type,
                        0, 0, "Name", "asc", null, null)
                : testDataLibService.readNameListByName(name, MAX_RESULTS, true);

        List<TestDataLib> found = answer.getDataList() == null ? new ArrayList<>() : new ArrayList<>(answer.getDataList());
        if (!name.isBlank()) {
            found.removeIf(lib ->
                    (!system.isBlank() && !system.equalsIgnoreCase(MCPToolUtils.nullSafe(lib.getSystem()))
                            && !MCPToolUtils.nullSafe(lib.getSystem()).isEmpty())
                    || (!type.isBlank() && !type.equalsIgnoreCase(MCPToolUtils.nullSafe(lib.getType()))));
        }

        List<Map<String, Object>> libraries = new ArrayList<>();

        boolean truncated = found.size() > limit;
        for (TestDataLib lib : found.subList(0, Math.min(found.size(), limit))) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("testDataLibId", lib.getTestDataLibID());
            map.put("name", MCPToolUtils.nullSafe(lib.getName()));
            map.put("system", MCPToolUtils.nullSafe(lib.getSystem()));
            map.put("environment", MCPToolUtils.nullSafe(lib.getEnvironment()));
            map.put("country", MCPToolUtils.nullSafe(lib.getCountry()));
            map.put("type", MCPToolUtils.nullSafe(lib.getType()));
            map.put("group", MCPToolUtils.nullSafe(lib.getGroup()));
            map.put("description", MCPToolUtils.nullSafe(lib.getDescription()));
            libraries.add(map);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("count", libraries.size());
        if (truncated) {
            response.put("truncated", true);
            response.put("message", "More libraries matched than the requested limit of " + limit
                    + ". Narrow the search with name, system or type, or raise limit.");
        }
        response.put("libraries", libraries);

        return MCPToolUtils.successJson(response);
    }

}
