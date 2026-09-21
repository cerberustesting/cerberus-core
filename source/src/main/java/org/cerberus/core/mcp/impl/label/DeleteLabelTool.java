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
package org.cerberus.core.mcp.impl.label;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import org.cerberus.core.crud.entity.CampaignLabel;
import org.cerberus.core.crud.entity.Label;
import org.cerberus.core.crud.service.ICampaignLabelService;
import org.cerberus.core.crud.service.ILabelService;
import org.cerberus.core.mcp.MCPTool;
import org.cerberus.core.mcp.util.MCPLogUtils;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP tool that removes a label from a campaign, under the tool name {@code cerberus_label_delete}.
 *
 * <p>Deletion is keyed by {@code campaignLabelID}, not by (campaign, labelId), so this tool reads
 * the row first via {@link ICampaignLabelService#readByKey(String, Integer)} before deleting it —
 * this also naturally reports "not labelled with that" instead of a silent no-op.</p>
 */
@Component
public class DeleteLabelTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_label_delete";

    private final ICampaignLabelService campaignLabelService;
    private final ILabelService labelService;
    private final MCPLogUtils mcpLogUtils;

    public DeleteLabelTool(ICampaignLabelService campaignLabelService, ILabelService labelService, MCPLogUtils mcpLogUtils) {
        this.campaignLabelService = campaignLabelService;
        this.labelService = labelService;
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
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("campaign", Map.of("type", "string", "description", "Name of the campaign to unlabel."));
        properties.put("label", Map.of("type", "string", "description", "Name of the label to remove. Provide this or labelId."));
        properties.put("labelId", Map.of("type", "integer", "description", "Numeric id of the label to remove. Provide this or label."));

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Removes a label from a campaign. Since a campaign runs every testcase carrying one
                of its attached labels, this excludes the testcases that only matched through this
                label from the campaign's next execution.

                Call this tool whenever the user asks to untag, unlabel, or remove a label from a
                campaign. Identify the label by name (label) or numeric id (labelId).

                Use cerberus_label_list first to see the labels currently attached.
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        List.of("campaign"),
                        null,
                        null,
                        null
                ),
                null,
                MCPToolUtils.deleteAnnotations("Remove campaign label", false),
                null
        );
    }

    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String campaignName = MCPToolUtils.getString(args, "campaign", "");
        String labelName = MCPToolUtils.getString(args, "label", "");
        int labelIdArg = MCPToolUtils.getInteger(args, "labelId", 0);

        mcpLogUtils.call(TOOL_NAME, "label_delete",
                String.format("MCP tool %s called with campaign=%s label=%s labelId=%d", TOOL_NAME, campaignName, labelName, labelIdArg));

        if (campaignName.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: campaign");
        }
        if (labelName.isBlank() && labelIdArg <= 0) {
            return MCPToolUtils.errorText("Provide either label (name) or labelId to identify the label.");
        }

        Integer labelId = labelIdArg;
        if (labelIdArg <= 0) {
            Label label = labelService.readAll().getDataList().stream()
                    .filter(l -> labelName.equalsIgnoreCase(l.getLabel()))
                    .findFirst()
                    .orElse(null);
            if (label == null) {
                return MCPToolUtils.errorText("No label named '" + labelName + "' exists.");
            }
            labelId = label.getId();
        }

        AnswerItem<CampaignLabel> readAnswer = campaignLabelService.readByKey(campaignName, labelId);
        if (!readAnswer.isCodeStringEquals("OK") || readAnswer.getItem() == null) {
            return MCPToolUtils.errorText("Campaign '" + campaignName + "' is not labelled with labelId " + labelId + ".");
        }

        Answer deleteAnswer = campaignLabelService.delete(readAnswer.getItem());
        if (!deleteAnswer.isCodeStringEquals("OK")) {
            return MCPToolUtils.errorText("Unable to remove label from campaign " + campaignName + ": " + deleteAnswer.getMessageDescription());
        }

        return MCPToolUtils.successJson(Map.of(
                "status", "deleted",
                "campaign", campaignName,
                "labelId", labelId
        ));
    }
}