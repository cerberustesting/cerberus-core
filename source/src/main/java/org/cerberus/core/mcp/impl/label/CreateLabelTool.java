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
 * MCP tool that attaches an existing label to a campaign, under the tool name
 * {@code cerberus_label_create}.
 *
 * <p>Attaching a label is not cosmetic: it is how a campaign picks up testcases to run — see
 * {@code TestCaseService.findTestCaseByCampaignNameAndCountries}, which selects every testcase
 * carrying one of the campaign's attached labels (or a child of one). {@link ILabelService} has no
 * lookup by name, only by numeric id, so this tool accepts either {@code labelId} directly or a
 * {@code label} name that it resolves itself by scanning {@link ILabelService#readAll()}. The
 * label itself must already exist — this tool does not create new labels, only the (campaign,
 * label) attachment.</p>
 *
 * <p>Validates the label id before calling {@link ICampaignLabelService#create(CampaignLabel)}:
 * the DAO reports both a duplicate (campaign, labelId) pair and a foreign-key violation on an
 * unknown labelId as the same MySQL SQLState {@code 23000}, so without this pre-check a typo'd
 * labelId would surface as a confusing "already exists" error instead of "label not found".</p>
 */
@Component
public class CreateLabelTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_label_create";

    private final ICampaignLabelService campaignLabelService;
    private final ILabelService labelService;
    private final MCPLogUtils mcpLogUtils;

    public CreateLabelTool(ICampaignLabelService campaignLabelService, ILabelService labelService, MCPLogUtils mcpLogUtils) {
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
        properties.put("campaign", Map.of("type", "string", "description", "Name of the campaign to label."));
        properties.put("label", Map.of("type", "string", "description", "Name of an existing label (e.g. 'Regression'). Provide this or labelId."));
        properties.put("labelId", Map.of("type", "integer", "description", "Numeric id of an existing label. Provide this or label."));

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Attaches an existing label to a campaign. This is how a campaign selects testcases to
                run: every testcase carrying this label (or a child of it) becomes part of the
                campaign's next execution, alongside the countries/environments/robots and other
                filters set with cerberus_campaign_parameter_create.

                Call this tool whenever the user asks to tag, label, or classify a campaign, or to add
                a set of testcases to a campaign by their label. Identify the label by name (label) or
                numeric id (labelId) — the label must already exist in Cerberus; this tool does not
                create new labels.

                Use cerberus_label_list first to see what is already attached and avoid a duplicate.
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
                MCPToolUtils.createAnnotations("Label campaign", false),
                null
        );
    }

    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String campaignName = MCPToolUtils.getString(args, "campaign", "");
        String labelName = MCPToolUtils.getString(args, "label", "");
        int labelIdArg = MCPToolUtils.getInteger(args, "labelId", 0);
        String user = MCPToolUtils.getString(args, "user", "MCP");

        mcpLogUtils.call(TOOL_NAME, "label_create",
                String.format("MCP tool %s called with campaign=%s label=%s labelId=%d", TOOL_NAME, campaignName, labelName, labelIdArg));

        if (campaignName.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: campaign");
        }
        if (labelName.isBlank() && labelIdArg <= 0) {
            return MCPToolUtils.errorText("Provide either label (name) or labelId to identify the label.");
        }

        Integer labelId;
        Label label;
        if (labelIdArg > 0) {
            AnswerItem<Label> labelAnswer = labelService.readByKey(labelIdArg);
            if (!labelAnswer.isCodeStringEquals("OK") || labelAnswer.getItem() == null) {
                return MCPToolUtils.errorText("Label does not exist: " + labelIdArg);
            }
            label = labelAnswer.getItem();
            labelId = labelIdArg;
        } else {
            label = labelService.readAll().getDataList().stream()
                    .filter(l -> labelName.equalsIgnoreCase(l.getLabel()))
                    .findFirst()
                    .orElse(null);
            if (label == null) {
                return MCPToolUtils.errorText("No label named '" + labelName + "' exists.");
            }
            labelId = label.getId();
        }

        if (campaignLabelService.exist(campaignName, labelId)) {
            return MCPToolUtils.errorText("Campaign '" + campaignName + "' is already labelled '" + label.getLabel() + "'.");
        }

        CampaignLabel campaignLabel = new CampaignLabel();
        campaignLabel.setCampaign(campaignName);
        campaignLabel.setLabelId(labelId);
        campaignLabel.setUsrCreated(user);

        Answer answer = campaignLabelService.create(campaignLabel);

        if (!answer.isCodeStringEquals("OK")) {
            return MCPToolUtils.errorText("Unable to label campaign " + campaignName + ": " + answer.getMessageDescription());
        }

        return MCPToolUtils.successJson(Map.of(
                "status", "created",
                "campaign", campaignName,
                "labelId", labelId,
                "label", MCPToolUtils.nullSafe(label.getLabel())
        ));
    }
}