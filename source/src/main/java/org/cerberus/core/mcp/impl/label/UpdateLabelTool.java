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
 * MCP tool that repoints an existing campaign/label attachment to a different label, under the
 * tool name {@code cerberus_label_update}.
 *
 * <p>A {@link CampaignLabel} row only ever carries {@code campaign} and {@code labelId} — there is
 * nothing else on it to mutate — so "update" here means swapping which {@link Label} the attachment
 * points to, keyed by {@code campaignLabelID} via {@link ICampaignLabelService#update(CampaignLabel)}.
 * The campaign itself cannot be changed through this tool; use {@code cerberus_label_delete} +
 * {@code cerberus_label_create} to move a label attachment to a different campaign.</p>
 */
@Component
public class UpdateLabelTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_label_update";

    private final ICampaignLabelService campaignLabelService;
    private final ILabelService labelService;
    private final MCPLogUtils mcpLogUtils;

    public UpdateLabelTool(ICampaignLabelService campaignLabelService, ILabelService labelService, MCPLogUtils mcpLogUtils) {
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
        Map<String, Object> updateProperties = new LinkedHashMap<>();
        updateProperties.put("label", Map.of("type", "string", "description", "New label name to attach. Provide this or labelId."));
        updateProperties.put("labelId", Map.of("type", "integer", "description", "New numeric label id to attach. Provide this or label."));

        Map<String, Object> updatesSchema = new LinkedHashMap<>();
        updatesSchema.put("type", "object");
        updatesSchema.put("description", "The new label to attach in place of the current one.");
        updatesSchema.put("properties", updateProperties);
        updatesSchema.put("additionalProperties", false);

        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("campaign", Map.of("type", "string", "description", "Name of the campaign the attachment belongs to."));
        properties.put("id", Map.of("type", "integer", "description", "Attachment id (campaignLabelId), as returned by cerberus_label_list or cerberus_label_get."));
        properties.put("updates", updatesSchema);

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Changes which label an existing campaign label attachment points to. Since a
                campaign runs every testcase carrying one of its attached labels, this effectively
                swaps one set of selected testcases for another.

                Call this tool whenever the user asks to replace or change one of a campaign's
                labels for another one (identify the attachment with its id, and the new label with
                label or labelId). Use cerberus_label_list first to get the id.

                To simply add or remove a label without replacing another, use
                cerberus_label_create / cerberus_label_delete instead.
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        List.of("campaign", "id", "updates"),
                        null,
                        null,
                        null
                ),
                null,
                MCPToolUtils.updateAnnotations("Update campaign label", false),
                null
        );
    }

    @SuppressWarnings("unchecked")
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String campaignName = MCPToolUtils.getString(args, "campaign", "");
        int id = MCPToolUtils.getInteger(args, "id", 0);

        mcpLogUtils.call(TOOL_NAME, "label_update",
                String.format("MCP tool %s called with campaign=%s id=%d", TOOL_NAME, campaignName, id));

        if (campaignName.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: campaign");
        }
        if (id <= 0) {
            return MCPToolUtils.errorText("Missing or invalid required parameter: id");
        }

        Object updatesObject = args.get("updates");
        if (!(updatesObject instanceof Map)) {
            return MCPToolUtils.errorText("Missing or invalid required parameter: updates");
        }
        Map<String, Object> updates = (Map<String, Object>) updatesObject;

        String newLabelName = MCPToolUtils.getString(updates, "label", "");
        int newLabelIdArg = MCPToolUtils.getInteger(updates, "labelId", 0);
        if (newLabelName.isBlank() && newLabelIdArg <= 0) {
            return MCPToolUtils.errorText("Provide either updates.label (name) or updates.labelId to identify the new label.");
        }

        AnswerItem<CampaignLabel> readAnswer = campaignLabelService.readByKeyTech(id);
        if (!readAnswer.isCodeStringEquals("OK") || readAnswer.getItem() == null) {
            return MCPToolUtils.errorText("Campaign label attachment does not exist: " + id);
        }

        CampaignLabel campaignLabel = readAnswer.getItem();
        if (!campaignName.equalsIgnoreCase(campaignLabel.getCampaign())) {
            return MCPToolUtils.errorText("Campaign label attachment " + id + " does not belong to campaign " + campaignName);
        }

        Integer newLabelId;
        Label newLabel;
        if (newLabelIdArg > 0) {
            AnswerItem<Label> labelAnswer = labelService.readByKey(newLabelIdArg);
            if (!labelAnswer.isCodeStringEquals("OK") || labelAnswer.getItem() == null) {
                return MCPToolUtils.errorText("Label does not exist: " + newLabelIdArg);
            }
            newLabel = labelAnswer.getItem();
            newLabelId = newLabelIdArg;
        } else {
            newLabel = labelService.readAll().getDataList().stream()
                    .filter(l -> newLabelName.equalsIgnoreCase(l.getLabel()))
                    .findFirst()
                    .orElse(null);
            if (newLabel == null) {
                return MCPToolUtils.errorText("No label named '" + newLabelName + "' exists.");
            }
            newLabelId = newLabel.getId();
        }

        if (!newLabelId.equals(campaignLabel.getLabelId()) && campaignLabelService.exist(campaignName, newLabelId)) {
            return MCPToolUtils.errorText("Campaign '" + campaignName + "' is already labelled '" + newLabel.getLabel() + "'.");
        }

        campaignLabel.setLabelId(newLabelId);
        campaignLabel.setUsrModif(MCPToolUtils.getString(args, "user", "MCP"));

        Answer updateAnswer = campaignLabelService.update(campaignLabel);
        if (!updateAnswer.isCodeStringEquals("OK")) {
            return MCPToolUtils.errorText("Unable to update campaign label attachment " + id + ": " + updateAnswer.getMessageDescription());
        }

        return MCPToolUtils.successJson(Map.of(
                "status", "updated",
                "id", id,
                "campaign", campaignName,
                "labelId", newLabelId,
                "label", MCPToolUtils.nullSafe(newLabel.getLabel())
        ));
    }
}