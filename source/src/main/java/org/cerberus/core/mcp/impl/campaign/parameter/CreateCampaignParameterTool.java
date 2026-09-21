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
package org.cerberus.core.mcp.impl.campaign.parameter;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import org.cerberus.core.crud.entity.CampaignParameter;
import org.cerberus.core.crud.service.ICampaignParameterService;
import org.cerberus.core.mcp.MCPTool;
import org.cerberus.core.mcp.util.MCPLogUtils;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerList;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP tool that adds a single selection criterion to a campaign, under the tool name
 * {@code cerberus_campaign_parameter_create}.
 *
 * <p>Each call inserts one {@link CampaignParameter} row (a campaign can have several rows with
 * the same {@code parameter} name, e.g. multiple COUNTRY values). Delegates to
 * {@link ICampaignParameterService#create(CampaignParameter)} after checking the exact
 * (campaign, parameter, value) tuple does not already exist, since the table itself does not
 * enforce that uniqueness.</p>
 */
@Component
public class CreateCampaignParameterTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_campaign_parameter_create";

    private static final List<String> KNOWN_PARAMETERS = List.of(
            CampaignParameter.COUNTRY_PARAMETER,
            CampaignParameter.ENVIRONMENT_PARAMETER,
            CampaignParameter.ROBOT_PARAMETER,
            CampaignParameter.PRIORITY_PARAMETER,
            CampaignParameter.STATUS_PARAMETER,
            CampaignParameter.SYSTEM_PARAMETER,
            CampaignParameter.APPLICATION_PARAMETER,
            CampaignParameter.TYPE_PARAMETER,
            CampaignParameter.TYPE_TESTFOLDER,
            CampaignParameter.BROWSER_PARAMETER
    );

    private final ICampaignParameterService campaignParameterService;
    private final MCPLogUtils mcpLogUtils;

    public CreateCampaignParameterTool(ICampaignParameterService campaignParameterService, MCPLogUtils mcpLogUtils) {
        this.campaignParameterService = campaignParameterService;
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
        properties.put("campaign", Map.of("type", "string", "description", "Name of the campaign to add the criterion to."));
        properties.put("parameter", Map.of(
                "type", "string",
                "description", "Type of criterion. COUNTRY, ENVIRONMENT and ROBOT are execution-time "
                        + "defaults, overridable per run via cerberus_campaign_execution_create. "
                        + "STATUS, SYSTEM, APPLICATION, PRIORITY, TYPE and TESTFOLDER are permanent "
                        + "filters, always applied (together with the campaign's labels) to decide "
                        + "which testcases it selects — they cannot be overridden at run time.",
                "enum", KNOWN_PARAMETERS
        ));
        properties.put("value", Map.of("type", "string", "description", "Value for this criterion (e.g. 'FR' for a COUNTRY parameter)."));

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Adds one selection criterion (country, environment, robot, or a filter narrowing
                which labelled testcases run) to a campaign.

                Call this tool whenever the user asks to add a country, environment or robot to a
                campaign, or to add another one alongside existing values — a campaign can have
                several values for the same parameter type. To change which testcases are included
                in the first place, attach a label instead with cerberus_label_create.

                Use cerberus_campaign_parameter_list first to see what is already configured, so you
                do not create a duplicate.
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        List.of("campaign", "parameter", "value"),
                        null,
                        null,
                        null
                ),
                null,
                MCPToolUtils.createAnnotations("Add campaign parameter", false),
                null
        );
    }

    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String campaignName = MCPToolUtils.getString(args, "campaign", "");
        String parameterName = MCPToolUtils.getString(args, "parameter", "");
        String value = MCPToolUtils.getString(args, "value", "");

        mcpLogUtils.call(TOOL_NAME, "campaign_parameter_create",
                String.format("MCP tool %s called with campaign=%s parameter=%s value=%s", TOOL_NAME, campaignName, parameterName, value));

        if (campaignName.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: campaign");
        }
        if (parameterName.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: parameter");
        }
        if (value.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: value");
        }

        AnswerList<CampaignParameter> existingAnswer = campaignParameterService.readByCampaign(campaignName);
        boolean alreadyExists = existingAnswer.getDataList().stream()
                .anyMatch(p -> parameterName.equalsIgnoreCase(p.getParameter()) && value.equalsIgnoreCase(p.getValue()));

        if (alreadyExists) {
            return MCPToolUtils.errorText("Campaign parameter already exists: campaign='" + campaignName
                    + "' parameter='" + parameterName + "' value='" + value + "'.");
        }

        CampaignParameter parameter = new CampaignParameter();
        parameter.setCampaign(campaignName);
        parameter.setParameter(parameterName);
        parameter.setValue(value);

        Answer answer = campaignParameterService.create(parameter);

        if (!answer.isCodeStringEquals("OK")) {
            return MCPToolUtils.errorText("Unable to create campaign parameter: " + answer.getMessageDescription());
        }

        return MCPToolUtils.successJson(Map.of(
                "status", "created",
                "campaign", campaignName,
                "parameter", parameterName,
                "value", value
        ));
    }
}