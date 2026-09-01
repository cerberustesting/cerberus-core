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
package org.cerberus.core.mcp.impl.execution;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import org.cerberus.core.api.entity.ManualUrlParameters;
import org.cerberus.core.api.entity.QueuedExecution;
import org.cerberus.core.api.entity.QueuedExecutionResult;
import org.cerberus.core.api.entity.QueuedExecutionTestcase;
import org.cerberus.core.api.services.QueuedExecutionService;
import org.cerberus.core.mcp.MCPTool;
import org.cerberus.core.mcp.util.MCPLogUtils;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP tool that queues (or re-queues) the execution of a whole campaign, under the tool name
 * {@code cerberus_campaign_execution_create}.
 *
 * <p>Delegates to {@link QueuedExecutionService#addCampaignToExecutionQueue(String, QueuedExecution, Principal)}
 * — the same service backing the {@code POST /public/queuedexecutions/{campaignId}} REST endpoint.
 * There is no "campaign execution" object: a campaign is only a saved selection — labels
 * (which testcases match, see {@code cerberus_label_list}) plus {@link org.cerberus.core.crud.entity.CampaignParameter}
 * rows (which countries/environments/robots to use, and further status/system/application/priority/
 * type/testFolder filters on top of the label match, see {@code cerberus_campaign_parameter_list}).
 * Running it resolves whatever the caller does not explicitly override here (countries,
 * environments, robots, or the campaign's default screenshot/video/verbose/timeout/... settings),
 * re-resolves the matching testcases via {@code TestCaseService.findTestCaseByCampaignNameAndCountries},
 * and queues one execution per valid combination under a freshly generated tag — that tag is the
 * only thing tying the resulting executions back together.</p>
 *
 * <p>Unlike {@code cerberus_testcase_execution_create}, optional numeric and text fields here are
 * left unset (not defaulted by this tool) when the caller omits them, so the campaign's own
 * configured defaults apply. Only pass a field to override what the campaign is configured with.</p>
 *
 * <p>A campaign execution is just a tag applied to many testcase executions: read the overall
 * result with {@code cerberus_tag_get} on the returned tag (aggregated OK/KO counters and CI
 * score, de-duplicated across retries), then {@code cerberus_testcase_execution_get} on a specific
 * execution id to see why one testcase failed — there is no separate campaign-level read tool.</p>
 */
@Component
public class CreateCampaignExecutionTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_campaign_execution_create";

    private static final List<String> MANUAL_EXECUTION_VALUES = List.of("N", "Y", "A");

    private final QueuedExecutionService queuedExecutionService;
    private final MCPLogUtils mcpLogUtils;

    public CreateCampaignExecutionTool(QueuedExecutionService queuedExecutionService, MCPLogUtils mcpLogUtils) {
        this.queuedExecutionService = queuedExecutionService;
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
        properties.put("campaign", Map.of("type", "string", "description", "Exact name of the campaign to run."));
        properties.put("countries", Map.of(
                "type", "array",
                "items", Map.of("type", "string"),
                "description", "Countries to run against. Omit to use the campaign's configured COUNTRY parameters."
        ));
        properties.put("environments", Map.of(
                "type", "array",
                "items", Map.of("type", "string"),
                "description", "Environments to run against. Omit to use the campaign's configured ENVIRONMENT parameters."
        ));
        properties.put("robots", Map.of(
                "type", "array",
                "items", Map.of("type", "string"),
                "description", "Robots to run on. Omit to use the campaign's configured ROBOT parameters, if any."
        ));
        properties.put("testcases", Map.of(
                "type", "array",
                "description", "Optional explicit list of testcases to run instead of the campaign's own selection. "
                        + "Each entry needs testFolder and testcase.",
                "items", Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "testFolder", Map.of("type", "string"),
                                "testcase", Map.of("type", "string")
                        ),
                        "required", List.of("testFolder", "testcase")
                )
        ));
        properties.put("tag", Map.of(
                "type", "string",
                "description", "Optional execution tag. Omit to use the campaign's default tag (or an auto-generated one)."
        ));
        properties.put("manualExecution", Map.of(
                "type", "string",
                "description", "Whether to run in manual mode. Omit to use the campaign's configured value.",
                "enum", MANUAL_EXECUTION_VALUES
        ));
        properties.put("screenshot", Map.of("type", "integer", "description", "Screenshot capture level override (0=off, 1=on error, 2=always)."));
        properties.put("video", Map.of("type", "integer", "description", "Video capture level override (0=off, 1=on error, 2=always)."));
        properties.put("verbose", Map.of("type", "integer", "description", "Log verbosity level override."));
        properties.put("pageSource", Map.of("type", "integer", "description", "Page source capture override (0/1)."));
        properties.put("robotLog", Map.of("type", "integer", "description", "Robot log capture override (0/1)."));
        properties.put("consoleLog", Map.of("type", "integer", "description", "Browser console log capture override (0/1)."));
        properties.put("timeout", Map.of("type", "string", "description", "Per-action timeout override in milliseconds."));
        properties.put("retries", Map.of("type", "integer", "description", "Number of retries override if the result is not OK."));
        properties.put("priority", Map.of("type", "integer", "description", "Queue priority override (lower runs first)."));
        properties.put("manualUrl", Map.of(
                "type", "integer",
                "description", "0 = use Cerberus-configured URL (default), 1 = force manual URL (requires manualHost and manualEnvData), 2 = override only the provided manual* fields.",
                "enum", List.of(0, 1, 2)
        ));
        properties.put("manualHost", Map.of("type", "string", "description", "Manual host override, used when manualUrl is 1 or 2."));
        properties.put("manualContextRoot", Map.of("type", "string", "description", "Manual context root override, used when manualUrl is 1 or 2."));
        properties.put("manualLoginRelativeUrl", Map.of("type", "string", "description", "Manual relative login URL override, used when manualUrl is 1 or 2."));
        properties.put("manualEnvData", Map.of("type", "string", "description", "Environment to source test data from when manualUrl is 1 or 2."));
        properties.put("user", Map.of("type", "string", "description", "Optional user name recorded as the execution trigger. Defaults to 'MCP'."));

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Queues the execution of every testcase in a campaign against its configured (or
                overridden) countries/environments/robots, and immediately triggers it — equivalent
                to clicking "Run" on a campaign in the Cerberus UI.

                Call this tool whenever the user asks to run, execute, launch, or relaunch a campaign.
                Relaunching is simply calling this tool again — there is no separate rerun action.

                Unlike running a single testcase, most fields here are optional: the campaign already
                knows which testcases it targets — every testcase carrying one of its labels
                (cerberus_label_list), narrowed by its other parameters (cerberus_campaign_parameter_list) —
                and which countries/environments/robots to use (cerberus_campaign_get shows the
                whole picture). Only pass a field to override what the campaign is configured with
                for this one run.

                This tool only starts the run. Read the overall outcome with cerberus_tag_get on the
                tag it returns (aggregated pass/fail counters and CI score), then
                cerberus_testcase_execution_get on a specific execution id to see why one testcase
                failed. Always check the result after running a campaign — a queued run is not a
                passing run.

                A response with nbExecutions = 0 means nothing was queued: check that the campaign has
                at least one label attached (cerberus_label_list), plus a COUNTRY and an ENVIRONMENT
                parameter configured, or pass countries/environments explicitly.
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
                MCPToolUtils.createAnnotations("Run campaign", false),
                null
        );
    }

    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String campaignName = MCPToolUtils.getString(args, "campaign", "");
        List<String> countries = MCPToolUtils.getStringList(args, "countries", List.of());
        List<String> environments = MCPToolUtils.getStringList(args, "environments", List.of());
        List<String> robots = MCPToolUtils.getStringList(args, "robots", List.of());
        String user = MCPToolUtils.getString(args, "user", "MCP");

        mcpLogUtils.call(TOOL_NAME, "campaign_execution_create",
                String.format("MCP tool %s called with campaign=%s countries=%s environments=%s robots=%s",
                        TOOL_NAME, campaignName, countries, environments, robots));

        if (campaignName.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: campaign");
        }

        List<QueuedExecutionTestcase> testcases = parseTestcases(args.get("testcases"));

        ManualUrlParameters manualUrlParameters = ManualUrlParameters.builder()
                .host(MCPToolUtils.getString(args, "manualHost", ""))
                .contextRoot(MCPToolUtils.getString(args, "manualContextRoot", ""))
                .loginRelativeUrl(MCPToolUtils.getString(args, "manualLoginRelativeUrl", ""))
                .envData(MCPToolUtils.getString(args, "manualEnvData", ""))
                .build();

        // Numeric/text overrides are left null (not defaulted here) when the caller omits them, so
        // QueuedExecutionService.addCampaignToExecutionQueue falls back to the campaign's own
        // configured defaults instead of a generic one.
        QueuedExecution queuedExecution = QueuedExecution.builder()
                .testcases(testcases)
                .countries(countries)
                .environments(environments)
                .robots(robots)
                .tag(optionalString(args, "tag"))
                .manualExecution(optionalString(args, "manualExecution"))
                .screenshot(optionalInteger(args, "screenshot"))
                .video(optionalInteger(args, "video"))
                .verbose(optionalInteger(args, "verbose"))
                .pageSource(optionalInteger(args, "pageSource"))
                .robotLog(optionalInteger(args, "robotLog"))
                .consoleLog(optionalInteger(args, "consoleLog"))
                .timeout(optionalString(args, "timeout"))
                .retries(optionalInteger(args, "retries"))
                .priority(optionalInteger(args, "priority"))
                .manualUrl(MCPToolUtils.getInteger(args, "manualUrl", 0))
                .manualUrlParameters(manualUrlParameters)
                .build();

        Principal principal = () -> user;

        QueuedExecutionResult result;
        try {
            result = queuedExecutionService.addCampaignToExecutionQueue(campaignName, queuedExecution, principal);
        } catch (RuntimeException e) {
            return MCPToolUtils.errorText("Unable to queue campaign execution: " + e.getMessage());
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("campaign", campaignName);
        response.put("tag", result.getTag());
        response.put("nbExecutions", result.getNbExecutions());
        response.put("queuedEntries", result.getQueuedEntries());
        if (result.getNbExecutions() == 0) {
            response.put("testcasesNotExist", result.getTestcasesNotExist());
            response.put("testcasesNotActive", result.getTestcasesNotActive());
            response.put("testcasesNotAllowedOnEnvironment", result.getTestcasesNotAllowedOnEnvironment());
            response.put("environmentsNotExistOrNotActive", result.getEnvironmentsNotExistOrNotActive());
            response.put("robotsMissing", result.getRobotsMissing());
        }
        response.put("messages", result.getMessages());

        return MCPToolUtils.successJson(response);
    }

    @SuppressWarnings("unchecked")
    private List<QueuedExecutionTestcase> parseTestcases(Object rawTestcases) {
        List<QueuedExecutionTestcase> testcases = new ArrayList<>();
        if (!(rawTestcases instanceof List<?> rawList)) {
            return testcases;
        }
        for (Object entry : rawList) {
            if (!(entry instanceof Map<?, ?> map)) {
                continue;
            }
            Object testFolder = map.get("testFolder");
            Object testcase = map.get("testcase");
            if (testFolder instanceof String testFolderId && testcase instanceof String testcaseId
                    && !testFolderId.isBlank() && !testcaseId.isBlank()) {
                testcases.add(QueuedExecutionTestcase.builder()
                        .testFolderId(testFolderId)
                        .testcaseId(testcaseId)
                        .build());
            }
        }
        return testcases;
    }

    private String optionalString(Map<String, Object> args, String key) {
        Object value = args.get(key);
        return (value instanceof String stringValue && !stringValue.isBlank()) ? stringValue : null;
    }

    private Integer optionalInteger(Map<String, Object> args, String key) {
        Object value = args.get(key);
        return (value instanceof Number number) ? number.intValue() : null;
    }

}