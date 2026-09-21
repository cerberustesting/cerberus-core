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
import org.cerberus.core.util.answer.AnswerItem;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP tool that retrieves the details of a single campaign/label attachment, under the tool name
 * {@code cerberus_label_get}.
 *
 * <p>Delegates to {@link ICampaignLabelService#readByKey(String, Integer)}, which joins the
 * {@code label} table, so the response carries the label's name/color/type alongside the
 * attachment's own id and audit fields — the detail {@code cerberus_label_list} omits.</p>
 */
@Component
public class GetLabelTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_label_get";

    private final ICampaignLabelService campaignLabelService;
    private final ILabelService labelService;
    private final MCPLogUtils mcpLogUtils;

    public GetLabelTool(ICampaignLabelService campaignLabelService, ILabelService labelService, MCPLogUtils mcpLogUtils) {
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
        properties.put("campaign", Map.of("type", "string", "description", "Name of the campaign."));
        properties.put("label", Map.of("type", "string", "description", "Name of the label to look up. Provide this or labelId."));
        properties.put("labelId", Map.of("type", "integer", "description", "Numeric id of the label to look up. Provide this or label."));

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Retrieves the details of one label attached to a campaign (its attachment id, the
                label's name/color/type, and who attached it). A campaign runs every testcase
                carrying one of its attached labels, so this confirms one specific selection criterion.

                Call this tool when the user asks about a specific label on a campaign, or to get the
                attachment id (campaignLabelId) needed by cerberus_label_update.

                Use cerberus_label_list instead to browse every label attached to a campaign.
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
                MCPToolUtils.readOnlyAnnotations("Get campaign label", true),
                null
        );
    }

    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String campaignName = MCPToolUtils.getString(args, "campaign", "");
        String labelName = MCPToolUtils.getString(args, "label", "");
        int labelIdArg = MCPToolUtils.getInteger(args, "labelId", 0);

        mcpLogUtils.call(TOOL_NAME, "label_get",
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

        AnswerItem<CampaignLabel> answer = campaignLabelService.readByKey(campaignName, labelId);
        if (!answer.isCodeStringEquals("OK") || answer.getItem() == null) {
            return MCPToolUtils.errorText("Campaign '" + campaignName + "' is not labelled with labelId " + labelId + ".");
        }

        return MCPToolUtils.successJson(toDetail(answer.getItem()));
    }

    private Map<String, Object> toDetail(CampaignLabel campaignLabel) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("campaignLabelId", campaignLabel.getCampaignLabelID());
        map.put("campaign", MCPToolUtils.nullSafe(campaignLabel.getCampaign()));
        map.put("labelId", campaignLabel.getLabelId());
        Label label = campaignLabel.getLabel();
        map.put("label", label != null ? MCPToolUtils.nullSafe(label.getLabel()) : "");
        map.put("color", label != null ? MCPToolUtils.nullSafe(label.getColor()) : "");
        map.put("type", label != null ? MCPToolUtils.nullSafe(label.getType()) : "");
        map.put("usrCreated", MCPToolUtils.nullSafe(campaignLabel.getUsrCreated()));
        map.put("dateCreated", campaignLabel.getDateCreated());
        map.put("usrModif", MCPToolUtils.nullSafe(campaignLabel.getUsrModif()));
        map.put("dateModif", campaignLabel.getDateModif());
        return map;
    }
}