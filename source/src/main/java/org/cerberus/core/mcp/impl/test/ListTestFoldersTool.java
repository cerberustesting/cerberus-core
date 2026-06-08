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
package org.cerberus.core.mcp.impl.test;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import org.cerberus.core.api.dto.test.TestMapperV001;
import org.cerberus.core.mcp.MCPTool;
import org.cerberus.core.mcp.util.MCPLogUtils;
import org.cerberus.core.mcp.util.MCPProjectionUtils;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.cerberus.core.crud.entity.Test;
import org.cerberus.core.crud.service.ITestService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * MCP tool that lists Cerberus test folders (the {@link Test} entity).
 *
 * <p>Exposes the MCP tool {@code cerberus_test_folders_list} to AI clients.
 * Delegates to {@link ITestService#readAll()} to retrieve all test folders,
 * then applies an optional in-memory search filter and field projection before
 * returning the result.</p>
 *
 * <p>The tool supports an {@code intent} parameter that selects a sensible
 * default field subset so callers do not need to enumerate fields explicitly
 * for common use-cases (e.g. picking a folder before creating a test case).</p>
 */
@Component
public class ListTestFoldersTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_test_folders_list";

    /** Exhaustive list of fields the tool can return; used to populate the JSON schema enum. */
    private static final List<String> ALL_FIELDS = List.of("test", "description", "isActive", "parentTest",
            "usrCreated", "dateCreated", "usrModif", "dateModif");

    private final ITestService testService;
    private final TestMapperV001 testMapper;
    private final MCPLogUtils mcpLogUtils;

    public ListTestFoldersTool(ITestService testService, TestMapperV001 testMapper, MCPLogUtils mcpLogUtils) {
        this.testService = testService;
        this.testMapper = testMapper;
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
     * Builds the MCP {@link McpSchema.Tool} descriptor for this tool.
     *
     * <p>Defines the JSON schema accepted by the tool, including the optional
     * {@code intent}, {@code search}, and {@code fields} parameters.
     * The {@code fields} enum is derived from {@link #ALL_FIELDS} so the schema
     * stays in sync with the projection logic automatically.</p>
     *
     * @return a fully configured {@link McpSchema.Tool} ready for MCP registration
     */
    private McpSchema.Tool createTool() {
        Map<String, Object> properties = Map.of(
                "intent", Map.of(
                        "type", "string",
                        "description", """
                                Optional usage context used to optimize the returned fields when fields is not provided.

                                Use:
                                - select_test_folder when the user needs to choose a test folder.
                                  Default fields: test, description.

                                - create_testcase before creating a testcase when the test folder is unknown.
                                  Default fields: test.

                                - inspect_test_folder when detailed test folder metadata is needed.
                                  Default fields: ALL_FIELDS.

                                If fields is provided, fields takes precedence over intent defaults.
                                Default: select_test_folder.
                                """,
                        "enum", List.of(
                                "select_test_folder",
                                "create_testcase",
                                "inspect_test_folder"
                        )
                ),
                "search", Map.of(
                        "type", "string",
                        "description", "Optional text filter on test folder name or description."
                ),
                "fields", Map.of(
                        "type", "array",
                        "description", "Optional list of fields to return.",
                        "items", Map.of(
                                "type", "string",
                                "enum", ALL_FIELDS
                        )
                )
        );

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Returns the list of Cerberus test folders available to the user.

                Use this tool when the target test folder is unknown or when the user needs to select a test folder.
                This tool can be used before creating a testcase, or retrieving test folder metadata.

                Use intent to describe the current usage context.
                Use fields to reduce the returned data to what is useful for the current task.
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
                MCPToolUtils.readOnlyAnnotations("List test folder", false),
                null
        );
    }

    /**
     * Executes the list operation: loads all test folders, applies the optional
     * search filter, maps each entity to a DTO, projects the requested fields,
     * and wraps the result in a successful MCP response.
     *
     * @param args the raw MCP tool arguments supplied by the client
     * @return a {@link McpSchema.CallToolResult} containing the serialised folder list
     */
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String intent = MCPToolUtils.getString(args, "intent", "select_test_folder");
        String search = MCPToolUtils.getString(args, "search", "");

        mcpLogUtils.call(TOOL_NAME, intent, String.format("MCP tool %s called with intent=%s", TOOL_NAME, intent));

        // fields override intent defaults when explicitly provided by the caller
        List<String> fields = MCPToolUtils.getStringList(
                args,
                "fields",
                defaultFieldsForIntent(intent)
        );

        List<Map<String, Object>> folders = testService.readAll()
                .getDataList()
                .stream()
                .filter(test -> matchesSearch(test, search))
                .map(testMapper::toDTO)
                .map(dto -> MCPProjectionUtils.project(dto, fields))
                .toList();

        return MCPToolUtils.successJson(Map.of(
                "intent", intent,
                "count", folders.size(),
                "folders", folders
        ));
    }

    /**
     * Returns the default set of fields to project for a given intent value.
     *
     * <p>Keeping defaults intent-driven avoids over-fetching data when the
     * caller does not know exactly which fields it needs (e.g. an AI agent
     * picking a folder before creating a test case only needs the folder name).</p>
     *
     * @param intent the intent string supplied by the caller
     * @return the list of field names to include in the projection
     */
    private List<String> defaultFieldsForIntent(String intent) {
        return switch (intent) {
            // only the folder key is needed when the AI is resolving a folder name
            case "create_testcase" -> List.of("test");
            // expose all metadata for detailed inspection
            case "inspect_test_folder" -> ALL_FIELDS;
            case "select_test_folder" -> List.of("test", "description");
            // safe fallback: return only the key to minimise noise
            default -> List.of("test");
        };
    }

    /**
     * Returns {@code true} when the given {@link Test} matches the search string.
     *
     * <p>Matches against both the folder name and its description so that
     * callers can locate a folder by either attribute without knowing in advance
     * which field holds the relevant value.</p>
     *
     * @param test   the entity to check
     * @param search the search term; blank or {@code null} matches everything
     * @return {@code true} if the entity matches, {@code false} otherwise
     */
    private boolean matchesSearch(Test test, String search) {
        if (search == null || search.isBlank()) {
            return true;
        }

        return MCPToolUtils.containsIgnoreCase(test.getTest(), search)
                || MCPToolUtils.containsIgnoreCase(test.getDescription(), search);
    }

}