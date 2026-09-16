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
package org.cerberus.core.mcp.impl.systemlabel;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import org.cerberus.core.crud.entity.Label;
import org.cerberus.core.crud.factory.IFactoryLabel;
import org.cerberus.core.crud.service.ILabelService;
import org.cerberus.core.mcp.MCPTool;
import org.cerberus.core.mcp.util.MCPLabelResolver;
import org.cerberus.core.mcp.util.MCPLogUtils;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerList;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP tool that defines a new label in a system, under the tool name
 * {@code cerberus_system_label_create}.
 *
 * <p>Creating the label is the step before attaching it to anything. Without it, "tag these
 * testcases as smoke" stops at the first call whenever the tag does not exist yet.</p>
 */
@Component
public class CreateSystemLabelTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_system_label_create";

    /** Fallback colour, so a label created without one is still legible in the interface. */
    private static final String DEFAULT_COLOR = "#6c757d";

    private final ILabelService labelService;
    private final IFactoryLabel factoryLabel;
    private final MCPLogUtils mcpLogUtils;

    public CreateSystemLabelTool(ILabelService labelService, IFactoryLabel factoryLabel, MCPLogUtils mcpLogUtils) {
        this.labelService = labelService;
        this.factoryLabel = factoryLabel;
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
        properties.put("system", Map.of(
                "type", "string",
                "description", "System the label belongs to."
        ));
        properties.put("label", Map.of(
                "type", "string",
                "description", "The label itself, as it will read on a testcase — SMOKE, REGRESSION, CHECKOUT."
        ));
        properties.put("type", Map.of(
                "type", "string",
                "description", "STICKER is the ordinary tag used to group testcases and to drive campaign "
                        + "selection. BATTERY and REQUIREMENT exist for requirement coverage. Defaults to "
                        + "STICKER.",
                "enum", List.of(Label.TYPE_STICKER, Label.TYPE_BATTERY, Label.TYPE_REQUIREMENT)
        ));
        properties.put("color", Map.of(
                "type", "string",
                "description", "Hex colour of the chip, for example #3b82f6. Defaults to a neutral grey."
        ));
        properties.put("description", Map.of(
                "type", "string",
                "description", "Short description of what the label means. Worth filling in: it is what stops "
                        + "a second label with the same intent being created next to this one."
        ));
        properties.put("parentLabelId", Map.of(
                "type", "integer",
                "description", "Parent label id, to nest this one under another. A campaign selecting the "
                        + "parent also picks up the testcases carrying this one."
        ));

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Defines a new label in a system.

                Call this when a label you want to attach does not exist yet — check with
                cerberus_system_label_list first, since a label whose name differs only in case or spelling
                from an existing one splits a campaign's selection in two without any error.

                Creating a label attaches it to nothing. Use cerberus_testcase_label_add to put it on a
                testcase, and cerberus_label_create to make a campaign select on it.
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        List.of("system", "label"),
                        null,
                        null,
                        null
                ),
                null,
                MCPToolUtils.createAnnotations("Create a system label", false),
                null
        );
    }

    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String system = MCPToolUtils.getString(args, "system", "").trim();
        String label = MCPToolUtils.getString(args, "label", "").trim();
        String type = MCPToolUtils.getString(args, "type", Label.TYPE_STICKER).trim();
        String color = MCPToolUtils.getString(args, "color", DEFAULT_COLOR).trim();
        String description = MCPToolUtils.getString(args, "description", "");
        int parentLabelId = MCPToolUtils.getInteger(args, "parentLabelId", 0);

        mcpLogUtils.call(TOOL_NAME, "system_label_create",
                String.format("MCP tool %s called with system=%s label=%s type=%s", TOOL_NAME, system, label, type));

        if (system.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: system");
        }
        if (label.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: label");
        }
        if (!List.of(Label.TYPE_STICKER, Label.TYPE_BATTERY, Label.TYPE_REQUIREMENT).contains(type)) {
            return MCPToolUtils.errorText("Unknown label type '" + type + "'. Use STICKER, BATTERY or REQUIREMENT.");
        }

        // readBySystemByCriteria appends the empty system to the list it is given, to fold in the
        // global labels — so the list has to be mutable.
        List<String> systems = new ArrayList<>();
        systems.add(system);
        AnswerList<Label> existingAnswer = labelService.readBySystem(systems);
        List<Label> existing = existingAnswer.getDataList() == null ? List.of() : existingAnswer.getDataList();
        for (Label candidate : existing) {
            // Case-insensitive on purpose: two labels differing only in case look identical on a
            // testcase and select differently in a campaign, which is the worst of both.
            if (label.equalsIgnoreCase(MCPToolUtils.nullSafe(candidate.getLabel()))
                    && system.equalsIgnoreCase(MCPToolUtils.nullSafe(candidate.getSystem()))) {
                return MCPToolUtils.errorText("A label named '" + candidate.getLabel() + "' already exists in "
                        + "system '" + system + "' (id " + candidate.getId() + "). Attach that one with "
                        + "cerberus_testcase_label_add instead of creating a second one.");
            }
        }

        // Every column of the label table is named in the INSERT, so none of the schema defaults
        // applies and a null reaches a NOT NULL column as an error. The requirement fields and the
        // long description are empty strings rather than nulls for that reason, and 0 — not null —
        // is what the application means by "no parent" (LabelDAO.updateParentToRoot writes 0).
        Timestamp now = new Timestamp(System.currentTimeMillis());
        Label created = factoryLabel.create(null, system, label, type, color,
                Math.max(parentLabelId, 0), "", "", "", description, "",
                "MCP", now, "MCP", now);

        Answer answer = labelService.create(created);
        if (!answer.isCodeStringEquals("OK")) {
            return MCPToolUtils.errorText("Unable to create the label: " + answer.getMessageDescription());
        }

        mcpLogUtils.success(TOOL_NAME, "system_label_create",
                String.format("Label %s created in system %s", label, system));

        // The insert does not hand the generated id back on the entity, and the id is what every
        // other label call needs when a name turns out to be ambiguous — so it is read back rather
        // than reported as null.
        Label stored = created;
        AnswerList<Label> reread = labelService.readBySystem(new ArrayList<>(List.of(system)));
        if (reread.getDataList() != null) {
            for (Label candidate : reread.getDataList()) {
                if (label.equals(MCPToolUtils.nullSafe(candidate.getLabel()))
                        && system.equals(MCPToolUtils.nullSafe(candidate.getSystem()))) {
                    stored = candidate;
                    break;
                }
            }
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "created");
        response.put("system", system);
        response.putAll(MCPLabelResolver.describe(stored));
        response.put("nextStep", "Attach it with cerberus_testcase_label_add, or make a campaign select on it "
                + "with cerberus_label_create.");
        return MCPToolUtils.successJson(response);
    }
}
