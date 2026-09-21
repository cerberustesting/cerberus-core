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
import org.cerberus.core.crud.service.ILabelService;
import org.cerberus.core.mcp.MCPTool;
import org.cerberus.core.mcp.util.MCPLabelResolver;
import org.cerberus.core.mcp.util.MCPLogUtils;
import org.cerberus.core.mcp.util.MCPPagination;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.cerberus.core.util.answer.AnswerList;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP tool that lists the labels a system defines, under the tool name
 * {@code cerberus_system_label_list}.
 *
 * <p>Three different things are called "label" in Cerberus, and only this one is the label itself:
 * the definition that lives in a system. The other two are attachments to it —
 * {@code cerberus_testcase_label_*} for the testcases wearing a label, {@code cerberus_label_*} for
 * the campaigns selecting on one. Without this tool neither of them could be used by name, because
 * nothing could say which names exist.</p>
 */
@Component
public class ListSystemLabelsTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_system_label_list";

    /** How many labels a listing returns when the caller does not say. */
    private static final int DEFAULT_LIMIT = 100;

    private final ILabelService labelService;
    private final MCPLogUtils mcpLogUtils;

    public ListSystemLabelsTool(ILabelService labelService, MCPLogUtils mcpLogUtils) {
        this.labelService = labelService;
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
                "description", "System whose labels to list. Labels with no system are global and always "
                        + "included."
        ));
        properties.put("type", Map.of(
                "type", "string",
                "description", "Narrow to one kind of label.",
                "enum", List.of(Label.TYPE_STICKER, Label.TYPE_BATTERY, Label.TYPE_REQUIREMENT)
        ));
        properties.put("search", Map.of(
                "type", "string",
                "description", "Free-text filter on the label name and its description."
        ));

        // Copied because several of these tools build their properties with Map.of, which is
        // immutable; the copy keeps one insertion point for every listing.
        properties = new LinkedHashMap<>(properties);
        MCPPagination.declare(properties, DEFAULT_LIMIT, "labels");

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Lists the labels defined in a system, with their ids.

                Call this before attaching a label to a testcase or a campaign: both take a name, and this is
                the only place that says which names exist and what each one means. It also gives the id,
                which is what you need when the same name is defined more than once.

                This lists label definitions. To see which labels a testcase wears, use
                cerberus_testcase_label_list; for the ones a campaign selects on, cerberus_label_list.
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        List.of(),
                        null,
                        null,
                        null
                ),
                null,
                MCPToolUtils.readOnlyAnnotations("List system labels", false),
                null
        );
    }

    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String system = MCPToolUtils.getString(args, "system", "").trim();
        String type = MCPToolUtils.getString(args, "type", "").trim();
        String search = MCPToolUtils.getString(args, "search", "").trim();

        mcpLogUtils.call(TOOL_NAME, "system_label_list",
                String.format("MCP tool %s called with system=%s type=%s search=%s",
                        TOOL_NAME, system, type, search));

        // readBySystemByCriteria appends the empty system to the list it is given, to fold in the
        // global labels — so the list has to be mutable, and a List.of() here fails at runtime.
        List<String> systems = new ArrayList<>();
        if (!system.isBlank()) {
            systems.add(system);
        }
        List<String> types = new ArrayList<>();
        if (!type.isBlank()) {
            types.add(type);
        }

        AnswerList<Label> answer = labelService.readByVarious(systems, types);
        List<Label> found = answer.getDataList() == null ? List.of() : answer.getDataList();

        List<Map<String, Object>> labels = new ArrayList<>();
        for (Label label : found) {
            if (!search.isBlank()
                    && !MCPToolUtils.containsIgnoreCase(label.getLabel(), search)
                    && !MCPToolUtils.containsIgnoreCase(label.getDescription(), search)) {
                continue;
            }
            Map<String, Object> described = MCPLabelResolver.describe(label);
            described.put("system", MCPToolUtils.nullSafe(label.getSystem()));
            if (label.getParentLabelID() != null && label.getParentLabelID() > 0) {
                described.put("parentLabelId", label.getParentLabelID());
            }
            labels.add(described);
        }

        MCPPagination.Window window = MCPPagination.of(args, DEFAULT_LIMIT);
        List<Map<String, Object>> page = MCPPagination.slice(labels, window);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("system", system);
        response.put("count", page.size());
        MCPPagination.describe(response, window, labels.size(), page.size(), "labels");
        response.put("labels", page);
        if (labels.isEmpty()) {
            response.put("message", "No label matches. Create one with cerberus_system_label_create before "
                    + "attaching it to a testcase or a campaign.");
        }
        return MCPToolUtils.successJson(response);
    }
}
