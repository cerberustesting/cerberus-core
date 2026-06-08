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
package org.cerberus.core.mcp.impl.test.testcase;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import org.cerberus.core.crud.entity.TestCase;
import org.cerberus.core.crud.service.ITestCaseService;
import org.cerberus.core.mcp.MCPTool;
import org.cerberus.core.mcp.util.MCPLogUtils;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * MCP tool that deletes an existing {@link TestCase} entity from Cerberus.
 *
 * <p>Exposed MCP tool name: {@value #TOOL_NAME}</p>
 *
 * <p>Delegates to {@link ITestCaseService#readByKey(String, String)} to verify the
 * testcase exists before attempting deletion, then to
 * {@link ITestCaseService#delete(TestCase)} to perform the actual removal.</p>
 */
@Component
public class DeleteTestCaseTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_testcase_delete";

    private final ITestCaseService testCaseService;
    private final MCPLogUtils mcpLogUtils;

    public DeleteTestCaseTool(ITestCaseService testCaseService, MCPLogUtils mcpLogUtils) {
        this.testCaseService = testCaseService;
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
     * Builds the MCP {@link McpSchema.Tool} descriptor for the delete operation,
     * declaring the two required string parameters ({@code testFolder} and {@code testcase})
     * and attaching destructive-action annotations so the MCP client can surface an
     * appropriate confirmation prompt.
     *
     * @return the fully configured tool descriptor
     */
    private McpSchema.Tool createTool() {
        Map<String, Object> properties = Map.of(
                "testFolder", Map.of(
                        "type", "string",
                        "description", "Name of the test folder containing the testcase."
                ),
                "testcase", Map.of(
                        "type", "string",
                        "description", "Identifier of the testcase to delete."
                )
        );

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Deletes an existing testcase from Cerberus.

                Call this tool whenever the user asks to delete or remove a testcase.
                Before calling this tool, confirm the testcase to delete with the user.

                Do not call this tool when the user only asks to list, read, create, or update a testcase.
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        List.of("testFolder", "testcase"),
                        null,
                        null,
                        null
                ),
                null,
                MCPToolUtils.deleteAnnotations("Delete testcase", false),
                null
        );
    }

    /**
     * Validates the supplied arguments, verifies that the target testcase exists,
     * and delegates the deletion to {@link ITestCaseService}.
     *
     * <p>The existence check ({@code readByKey}) is performed first so that a
     * clear "does not exist" error is returned rather than a generic service
     * failure when the caller passes an unknown key. The resolved {@link TestCase}
     * entity is passed directly to {@link ITestCaseService#delete(TestCase)} —
     * snapshotting the DTO before deletion — because the entity is gone after
     * the service call and any further reference would yield stale or missing data.</p>
     *
     * @param args map of MCP tool arguments provided by the caller
     * @return a success JSON result containing the deleted identifiers, or an
     *         error result describing the failure reason
     */
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String testFolder = MCPToolUtils.getString(args, "testFolder", "");
        String testcaseId = MCPToolUtils.getString(args, "testcase", "");

        mcpLogUtils.call(TOOL_NAME, "testcase_delete",
                String.format("MCP tool %s called with testFolder=%s testcase=%s", TOOL_NAME, testFolder, testcaseId));

        if (testFolder.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: testFolder");
        }

        if (testcaseId.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: testcase");
        }

        // Load the entity first to confirm it exists and obtain the managed object required by the delete API.
        AnswerItem<TestCase> existing = testCaseService.readByKey(testFolder, testcaseId);
        if (!existing.isCodeStringEquals("OK") || existing.getItem() == null) {
            return MCPToolUtils.errorText("Testcase does not exist: testFolder=" + testFolder + " testcase=" + testcaseId);
        }

        // Snapshot the DTO before deletion — the entity is gone after the service call.
        Answer answer = testCaseService.delete(existing.getItem());

        if (!answer.isCodeStringEquals("OK")) {
            return MCPToolUtils.errorText("Unable to delete testcase: " + answer.getMessageDescription());
        }

        return MCPToolUtils.successJson(Map.of(
                "status", "deleted",
                "testFolder", testFolder,
                "testcase", testcaseId
        ));
    }

}