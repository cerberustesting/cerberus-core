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
import org.cerberus.core.mcp.MCPTool;
import org.cerberus.core.mcp.util.MCPLogUtils;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.cerberus.core.crud.entity.Test;
import org.cerberus.core.crud.service.ITestService;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class UpdateTestFolderTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_test_folder_update";

    private final ITestService testService;
    private final MCPLogUtils mcpLogUtils;

    public UpdateTestFolderTool(ITestService testService, MCPLogUtils mcpLogUtils) {
        this.testService = testService;
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

    private McpSchema.Tool createTool() {
        Map<String, Object> updateProperties = new LinkedHashMap<>();

        updateProperties.put("description", Map.of(
                "type", "string",
                "description", "New description of the test folder."
        ));

        updateProperties.put("active", Map.of(
                "type", "boolean",
                "description", "Whether the test folder is active."
        ));

        Map<String, Object> updatesSchema = new LinkedHashMap<>();
        updatesSchema.put("type", "object");
        updatesSchema.put("description", "Fields to update on the test folder. Only supported fields are allowed.");
        updatesSchema.put("properties", updateProperties);
        updatesSchema.put("additionalProperties", false);

        Map<String, Object> properties = new LinkedHashMap<>();

        properties.put("testFolder", Map.of(
                "type", "string",
                "description", "Name of the test folder to update."
        ));

        properties.put("updates", updatesSchema);

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Updates an existing test folder in Cerberus.

                Call this tool whenever the user asks to modify, update, activate, deactivate,
                or change properties of an existing test folder.

                The test folder name is required.

                Only explicitly supported fields can be updated:
                - description
                - active

                Do not call this tool when the user only asks to display, list, read, or search a test folder.
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        List.of("testFolder", "updates"),
                        null,
                        null,
                        null
                ),
                null,
                MCPToolUtils.createAnnotations("Update test folder", false),
                null
        );
    }

    @SuppressWarnings("unchecked")
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String testFolder = MCPToolUtils.getString(args, "testFolder", "");

        mcpLogUtils.call(
                TOOL_NAME,
                "test_folder_update",
                String.format("MCP tool %s called with testFolder=%s", TOOL_NAME, testFolder)
        );

        if (testFolder.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: testFolder");
        }

        Object updatesObject = args.get("updates");

        if (!(updatesObject instanceof Map)) {
            return MCPToolUtils.errorText("Missing or invalid required parameter: updates");
        }

        Map<String, Object> updates = (Map<String, Object>) updatesObject;

        if (updates.isEmpty()) {
            return MCPToolUtils.errorText("No field provided to update.");
        }

        AnswerItem<Test> readAnswer = testService.readByKey(testFolder);

        if (!readAnswer.isCodeStringEquals("OK") || readAnswer.getItem() == null) {
            return MCPToolUtils.errorText("Test Folder does not exist: " + testFolder);
        }

        Test test = readAnswer.getItem();

        Map<String, Object> modifiedFields = new LinkedHashMap<>();

        try {
            for (Map.Entry<String, Object> entry : updates.entrySet()) {
                String field = entry.getKey();
                Object value = entry.getValue();

                switch (field) {
                    case "description":
                        String description = asString(value, field);
                        test.setDescription(description);
                        modifiedFields.put(field, description);
                        break;

                    case "active":
                        Boolean active = asBoolean(value, field);
                        test.setActive(active);
                        modifiedFields.put(field, active);
                        break;

                    default:
                        return MCPToolUtils.errorText("Unsupported field for test folder update: " + field);
                }
            }
        } catch (IllegalArgumentException e) {
            return MCPToolUtils.errorText(e.getMessage());
        }

        if (modifiedFields.isEmpty()) {
            return MCPToolUtils.errorText("No valid field provided to update.");
        }

        test.setUsrModif("MCP");

        Answer updateAnswer = testService.update(test.getTest(), test);

        if (!updateAnswer.isCodeStringEquals("OK")) {
            return MCPToolUtils.errorText(
                    "Unable to update Test Folder " + testFolder + ": "
                            + updateAnswer.getMessageDescription()
            );
        }

        return MCPToolUtils.successJson(Map.of(
                "status", "updated",
                "testFolder", testFolder,
                "updatedFields", modifiedFields
        ));
    }

    private String asString(Object value, String field) {
        if (value == null) {
            return "";
        }

        if (!(value instanceof String)) {
            throw new IllegalArgumentException("Invalid value for field '" + field + "'. Expected string.");
        }

        return ((String) value).trim();
    }

    private Boolean asBoolean(Object value, String field) {
        if (!(value instanceof Boolean)) {
            throw new IllegalArgumentException("Invalid value for field '" + field + "'. Expected boolean.");
        }

        return (Boolean) value;
    }
}