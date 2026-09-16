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
package org.cerberus.core.mcp.impl.test.testcase.label;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import org.cerberus.core.crud.entity.Label;
import org.cerberus.core.crud.entity.TestCase;
import org.cerberus.core.crud.entity.TestCaseLabel;
import org.cerberus.core.crud.service.ITestCaseLabelService;
import org.cerberus.core.mcp.MCPTool;
import org.cerberus.core.mcp.util.MCPLabelResolver;
import org.cerberus.core.mcp.util.MCPLogUtils;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP tool that detaches labels from a testcase, under the tool name
 * {@code cerberus_testcase_label_remove}.
 *
 * <p>Removes the link between the testcase and the label. The label itself keeps existing for every
 * other testcase; nothing here touches the definition the system holds.</p>
 */
@Component
public class RemoveTestCaseLabelTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_testcase_label_remove";

    private final ITestCaseLabelService testCaseLabelService;
    private final MCPLabelResolver labelResolver;
    private final MCPLogUtils mcpLogUtils;

    public RemoveTestCaseLabelTool(ITestCaseLabelService testCaseLabelService,
                                   MCPLabelResolver labelResolver,
                                   MCPLogUtils mcpLogUtils) {
        this.testCaseLabelService = testCaseLabelService;
        this.labelResolver = labelResolver;
        this.mcpLogUtils = mcpLogUtils;
    }

    @Override
    public McpServerFeatures.SyncToolSpecification toToolSpecification() {
        return new McpServerFeatures.SyncToolSpecification(
                createTool(),
                (exchange, request) -> execute(MCPToolUtils.argumentsOrEmpty(request.arguments()))
        );
    }

    private McpSchema.Tool createTool() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("testFolder", Map.of(
                "type", "string",
                "description", "Test folder containing the testcase."
        ));
        properties.put("testcase", Map.of(
                "type", "string",
                "description", "Testcase to unlabel."
        ));
        properties.put("labels", Map.of(
                "type", "array",
                "items", Map.of("type", "string"),
                "description", "Label names to detach from this testcase."
        ));
        properties.put("labelIds", Map.of(
                "type", "array",
                "items", Map.of("type", "integer"),
                "description", "Label ids to detach. Use this when a name is used more than once in the system."
        ));

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Detaches labels from a testcase.

                Only the link is removed: the label keeps existing and every other testcase keeps it. Removing
                the label definition from the system is a different, far wider operation, done from the
                interface.

                Removing a label can take the testcase out of a campaign that selects on it. Confirm with the
                user before removing a label you did not just add.

                A label that is not attached is reported rather than treated as an error.
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
                MCPToolUtils.deleteAnnotations("Remove labels from a testcase", false),
                null
        );
    }

    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String testFolder = MCPToolUtils.getString(args, "testFolder", "").trim();
        String testcaseId = MCPToolUtils.getString(args, "testcase", "").trim();
        List<String> names = MCPToolUtils.getStringList(args, "labels", List.of());
        List<Integer> ids = MCPToolUtils.getIntegerList(args, "labelIds");

        mcpLogUtils.call(TOOL_NAME, "testcase_label_remove",
                String.format("MCP tool %s called with testFolder=%s testcase=%s labels=%s labelIds=%s",
                        TOOL_NAME, testFolder, testcaseId, names, ids));

        if (testFolder.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: testFolder");
        }
        if (testcaseId.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: testcase");
        }

        AnswerItem<TestCase> testCaseAnswer = labelResolver.readTestCase(testFolder, testcaseId);
        if (!testCaseAnswer.isCodeStringEquals("OK") || testCaseAnswer.getItem() == null) {
            return MCPToolUtils.errorText("Testcase does not exist: testFolder=" + testFolder
                    + " testcase=" + testcaseId);
        }

        String system = labelResolver.systemOf(testCaseAnswer.getItem());
        MCPLabelResolver.Resolution resolution = labelResolver.resolve(system, names, ids);
        if (resolution.failed()) {
            return MCPToolUtils.errorText(resolution.error());
        }

        AnswerList<TestCaseLabel> existingAnswer = testCaseLabelService.readByTestTestCase(testFolder, testcaseId, null);
        List<TestCaseLabel> attached = existingAnswer.getDataList() == null ? List.of() : existingAnswer.getDataList();

        List<TestCaseLabel> toDelete = new ArrayList<>();
        List<Map<String, Object>> removed = new ArrayList<>();
        List<Map<String, Object>> notAttached = new ArrayList<>();

        for (Label label : resolution.labels()) {
            List<TestCaseLabel> links = attached.stream()
                    .filter(link -> label.getId().equals(link.getLabelId()))
                    .toList();
            if (links.isEmpty()) {
                notAttached.add(MCPLabelResolver.describe(label));
                continue;
            }
            // More than one link for the same label should not exist, but a testcase that collected a
            // duplicate before this tool existed must still come out clean rather than half-cleaned.
            toDelete.addAll(links);
            removed.add(MCPLabelResolver.describe(label));
        }

        if (!toDelete.isEmpty()) {
            Answer answer = testCaseLabelService.deleteList(toDelete);
            if (!answer.isCodeStringEquals("OK")) {
                return MCPToolUtils.errorText("Unable to detach the labels: " + answer.getMessageDescription());
            }
        }

        mcpLogUtils.success(TOOL_NAME, "testcase_label_remove",
                String.format("Labels %s removed from %s/%s", removed, testFolder, testcaseId));

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", removed.isEmpty() ? "unchanged" : "updated");
        response.put("testFolder", testFolder);
        response.put("testcase", testcaseId);
        response.put("removed", removed);
        if (!notAttached.isEmpty()) {
            response.put("wasNotAttached", notAttached);
        }
        return MCPToolUtils.successJson(response);
    }
}
