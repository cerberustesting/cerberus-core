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
import org.cerberus.core.crud.factory.IFactoryTestCaseLabel;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * MCP tool that attaches labels to a testcase, under the tool name
 * {@code cerberus_testcase_label_add}.
 *
 * <p>A campaign picks its testcases by label, so attaching one is what puts a testcase into a run —
 * and the absence of this operation is why a testcase that should have been selected was not.</p>
 */
@Component
public class AddTestCaseLabelTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_testcase_label_add";

    private final ITestCaseLabelService testCaseLabelService;
    private final IFactoryTestCaseLabel factoryTestCaseLabel;
    private final MCPLabelResolver labelResolver;
    private final MCPLogUtils mcpLogUtils;

    public AddTestCaseLabelTool(ITestCaseLabelService testCaseLabelService,
                                IFactoryTestCaseLabel factoryTestCaseLabel,
                                MCPLabelResolver labelResolver,
                                MCPLogUtils mcpLogUtils) {
        this.testCaseLabelService = testCaseLabelService;
        this.factoryTestCaseLabel = factoryTestCaseLabel;
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
                "description", "Testcase to label."
        ));
        properties.put("labels", Map.of(
                "type", "array",
                "items", Map.of("type", "string"),
                "description", "Label names to attach, as they read in the system this testcase belongs to. "
                        + "Call cerberus_system_label_list to see what exists, or cerberus_system_label_create "
                        + "to define one first — this tool never creates a label it does not find."
        ));
        properties.put("labelIds", Map.of(
                "type", "array",
                "items", Map.of("type", "integer"),
                "description", "Label ids to attach. Use this when a name exists more than once in the system."
        ));

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Attaches one or more existing labels to a testcase.

                This is what puts a testcase into a campaign: a campaign selects the testcases carrying a
                label, so a missing label is a testcase that quietly never runs.

                Name the labels in 'labels', or in 'labelIds' when a name is used twice in the system. A label
                already attached is left alone and reported, so calling this twice changes nothing.

                This tool does not create labels. Use cerberus_system_label_create for that, then attach it here.
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
                MCPToolUtils.createAnnotations("Add labels to a testcase", false),
                null
        );
    }

    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String testFolder = MCPToolUtils.getString(args, "testFolder", "").trim();
        String testcaseId = MCPToolUtils.getString(args, "testcase", "").trim();
        List<String> names = MCPToolUtils.getStringList(args, "labels", List.of());
        List<Integer> ids = MCPToolUtils.getIntegerList(args, "labelIds");

        mcpLogUtils.call(TOOL_NAME, "testcase_label_add",
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
        Set<Integer> alreadyAttached = new LinkedHashSet<>();
        if (existingAnswer.getDataList() != null) {
            existingAnswer.getDataList().forEach(link -> alreadyAttached.add(link.getLabelId()));
        }

        List<TestCaseLabel> toCreate = new ArrayList<>();
        List<Map<String, Object>> added = new ArrayList<>();
        List<Map<String, Object>> alreadyThere = new ArrayList<>();

        for (Label label : resolution.labels()) {
            if (alreadyAttached.contains(label.getId())) {
                alreadyThere.add(MCPLabelResolver.describe(label));
                continue;
            }
            // Guards a caller naming the same label twice in one call, which would otherwise insert
            // the link twice and leave a duplicate the UI shows as two identical chips.
            alreadyAttached.add(label.getId());
            toCreate.add(factoryTestCaseLabel.create(null, testFolder, testcaseId, label.getId(),
                    "MCP", null, null, null, label));
            added.add(MCPLabelResolver.describe(label));
        }

        if (!toCreate.isEmpty()) {
            Answer answer = testCaseLabelService.createList(toCreate);
            if (!answer.isCodeStringEquals("OK")) {
                return MCPToolUtils.errorText("Unable to attach the labels: " + answer.getMessageDescription());
            }
        }

        mcpLogUtils.success(TOOL_NAME, "testcase_label_add",
                String.format("Labels %s attached to %s/%s", added, testFolder, testcaseId));

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", added.isEmpty() ? "unchanged" : "updated");
        response.put("testFolder", testFolder);
        response.put("testcase", testcaseId);
        response.put("added", added);
        if (!alreadyThere.isEmpty()) {
            response.put("alreadyAttached", alreadyThere);
        }
        return MCPToolUtils.successJson(response);
    }
}
