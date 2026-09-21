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
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP tool that lists the labels attached to a testcase, under the tool name
 * {@code cerberus_testcase_label_list}.
 *
 * <p>Labels are how a campaign decides which testcases it runs, so a testcase carrying the wrong
 * ones — or none — is silently left out of, or dragged into, a run. Until now nothing in the tools
 * could see them: {@code cerberus_label_*} manages what a campaign selects on, not which testcase
 * wears which.</p>
 */
@Component
public class ListTestCaseLabelsTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_testcase_label_list";

    private final ITestCaseLabelService testCaseLabelService;
    private final MCPLabelResolver labelResolver;
    private final MCPLogUtils mcpLogUtils;

    public ListTestCaseLabelsTool(ITestCaseLabelService testCaseLabelService,
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
                "description", "Testcase whose labels to list."
        ));
        properties.put("includeAvailable", Map.of(
                "type", "boolean",
                "description", "Also return every label defined in this testcase's system, so you can see what "
                        + "could be attached without a second call. Defaults to false."
        ));

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Lists the labels attached to a testcase.

                Labels are what campaigns select on, so this is the first thing to check when a testcase does
                not run in a campaign it should, or runs in one it should not.

                Three things are called label here. cerberus_system_label_list lists the definitions a system
                holds, cerberus_label_list lists the ones a campaign selects on, and this one says which of
                them a given testcase wears.

                Use cerberus_testcase_label_add and cerberus_testcase_label_remove to change them.
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
                MCPToolUtils.readOnlyAnnotations("List testcase labels", false),
                null
        );
    }

    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String testFolder = MCPToolUtils.getString(args, "testFolder", "").trim();
        String testcaseId = MCPToolUtils.getString(args, "testcase", "").trim();
        boolean includeAvailable = MCPToolUtils.getBoolean(args, "includeAvailable", false);

        mcpLogUtils.call(TOOL_NAME, "testcase_label_list",
                String.format("MCP tool %s called with testFolder=%s testcase=%s", TOOL_NAME, testFolder, testcaseId));

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

        AnswerList<TestCaseLabel> answer = testCaseLabelService.readByTestTestCase(testFolder, testcaseId, null);
        List<TestCaseLabel> attached = answer.getDataList() == null ? List.of() : answer.getDataList();

        List<Map<String, Object>> labels = new ArrayList<>();
        for (TestCaseLabel testCaseLabel : attached) {
            Map<String, Object> described = new LinkedHashMap<>();
            if (testCaseLabel.getLabel() != null) {
                described.putAll(MCPLabelResolver.describe(testCaseLabel.getLabel()));
            } else {
                // The join is a left outer one: a row whose label was deleted survives here and would
                // otherwise vanish from the answer, leaving a testcase carrying something invisible.
                described.put("labelId", testCaseLabel.getLabelId());
                described.put("label", "(label no longer exists)");
            }
            described.put("linkId", testCaseLabel.getId());
            labels.add(described);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("testFolder", testFolder);
        response.put("testcase", testcaseId);
        response.put("count", labels.size());
        response.put("labels", labels);

        if (includeAvailable) {
            String system = labelResolver.systemOf(testCaseAnswer.getItem());
            response.put("system", system == null ? "" : system);
            response.put("availableInSystem", labelResolver.labelsOf(system).stream()
                    .map(MCPLabelResolver::describe)
                    .toList());
        }

        return MCPToolUtils.successJson(response);
    }
}
