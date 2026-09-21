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
import org.cerberus.core.crud.entity.TestCase;
import org.cerberus.core.mcp.MCPTool;
import org.cerberus.core.mcp.util.MCPExecutionTargets;
import org.cerberus.core.mcp.util.MCPLogUtils;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP tool that reports where a testcase can actually be executed, under the tool name
 * {@code cerberus_testcase_execution_targets}.
 *
 * <p>{@code cerberus_testcase_execution_create} requires a country, an environment and a robot, and
 * silently queues nothing when the combination does not exist. The three values are not free
 * choices: a country must be declared on the testcase, an environment only exists for a given
 * (system, country) pair <em>and</em> must be configured for the application, and a robot must
 * match the application type. Nothing in the create tool exposes those constraints, so an agent
 * has to guess — and the intuitive guess is wrong whenever a team hosts its environments under a
 * country code that is not the obvious one for the market being tested.</p>
 *
 * <p>This tool resolves the whole chain server-side and returns the combinations that will work,
 * so the caller picks from a list instead of guessing. When a testcase declares a country that has
 * no configured environment, that country is reported with an empty environment list rather than
 * omitted: knowing that a country is declared but unusable is what explains a run that queued
 * nothing.</p>
 *
 * <p>The resolution itself lives in {@link MCPExecutionTargets}, shared with
 * {@code cerberus_testcase_execution_create} so a run refused for an unusable country or
 * environment names the valid ones from the same source this tool reports.</p>
 */
@Component
public class ListTestCaseExecutionTargetsTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_testcase_execution_targets";

    private final MCPExecutionTargets executionTargets;
    private final MCPLogUtils mcpLogUtils;

    public ListTestCaseExecutionTargetsTool(MCPExecutionTargets executionTargets, MCPLogUtils mcpLogUtils) {
        this.executionTargets = executionTargets;
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

    /**
     * Builds the MCP tool descriptor.
     *
     * @return the fully-described {@link McpSchema.Tool} for registration with the MCP server.
     */
    private McpSchema.Tool createTool() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("testFolder", Map.of(
                "type", "string",
                "description", "Test folder the testcase belongs to."
        ));
        properties.put("testcase", Map.of(
                "type", "string",
                "description", "Testcase identifier to resolve execution targets for."
        ));

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Returns the country / environment / robot combinations a testcase can actually be executed on.

                Call this before cerberus_testcase_execution_create, every time you do not already know the exact
                country, environment and robot to use. Do not guess them: a country code that looks right for the
                market under test is often not the one the environments are declared under, and queuing a run with
                an unconfigured combination silently creates nothing.

                The response gives, per country declared on the testcase, the environments configured for its
                application, plus the robots whose type matches that application. "runnable" lists the pairs that
                are ready to execute; a country with an empty environment list is declared on the testcase but has
                no environment configured, so it cannot be used.

                If several combinations are valid and the user has not said which one they want, ask them rather
                than picking one.
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
                MCPToolUtils.readOnlyAnnotations("List testcase execution targets", false),
                null
        );
    }

    /**
     * Resolves the testcase, its application, its declared countries and the matching robots.
     *
     * @param args tool arguments extracted from the MCP request (may be empty but never null).
     * @return a {@link McpSchema.CallToolResult} carrying the executable targets, or an error.
     */
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String testFolder = MCPToolUtils.getString(args, "testFolder", "").trim();
        String testcaseId = MCPToolUtils.getString(args, "testcase", "").trim();

        mcpLogUtils.call(TOOL_NAME, "testcase_execution_targets",
                String.format("MCP tool %s called with testFolder=%s testcase=%s", TOOL_NAME, testFolder, testcaseId));

        if (testFolder.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: testFolder");
        }
        if (testcaseId.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: testcase");
        }

        MCPExecutionTargets.Targets targets = executionTargets.resolve(testFolder, testcaseId);
        if (targets.failed()) {
            return MCPToolUtils.errorText(targets.error());
        }

        TestCase testCase = targets.testCase();
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("testFolder", testFolder);
        response.put("testcase", testcaseId);
        response.put("application", targets.application());
        response.put("testcaseStatus", MCPToolUtils.nullSafe(testCase.getStatus()));
        response.put("testcaseIsActive", testCase.isActive());

        if (targets.application().isBlank()) {
            response.put("countries", List.of());
            response.put("robots", List.of());
            response.put("runnable", List.of());
            response.put("message", "This testcase has no application attached, so no environment can be resolved. "
                    + "Attach an application to it before trying to execute it.");
            return MCPToolUtils.successJson(response);
        }

        response.put("system", targets.system());
        response.put("applicationType", targets.applicationType());
        response.put("countries", targets.countries());
        response.put("robotRequired", targets.robotRequired());
        response.put("robots", targets.robots());
        response.put("runnable", targets.runnable());

        if (targets.runnable().isEmpty()) {
            response.put("message", targets.declaredCountries().isEmpty()
                    ? "No country is declared on this testcase. Add one with cerberus_testcase_country_create before executing it."
                    : "The countries declared on this testcase (" + targets.declaredCountries() + ") have no active environment "
                            + "configured for application '" + targets.application() + "' in system '" + targets.system() + "'. "
                            + "Check which country the environments are actually declared under with "
                            + "cerberus_country_environment_parameters_list.");
        } else if (!targets.robotRequired()) {
            response.put("message", "Application type '" + targets.applicationType() + "' is not driven by a robot, so the "
                    + "execution engine ignores it. cerberus_testcase_execution_create fills the robot in by itself "
                    + "for this application type — leave the robots parameter out.");
        } else if (targets.robots().isEmpty()) {
            response.put("message", "No robot matches application type '" + targets.applicationType() + "', so this testcase "
                    + "cannot be executed automatically. A robot matches when its type equals the application type "
                    + "exactly (case-sensitive) or is left empty. Create one with cerberus_robot_create, or run with "
                    + "manualExecution set to 'Y'.");
        }

        return MCPToolUtils.successJson(response);
    }
}
