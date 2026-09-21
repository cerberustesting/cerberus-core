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
package org.cerberus.core.mcp.impl.test.testcase;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import org.cerberus.core.crud.entity.TestCaseContentMatch;
import org.cerberus.core.crud.service.ITestCaseContentSearchService;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.mcp.MCPTool;
import org.cerberus.core.mcp.util.MCPLogUtils;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.cerberus.core.mcp.util.MCPUserContextService;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * MCP tool that finds every place a string appears inside testcases, under the tool name
 * {@code cerberus_testcase_search_content}.
 *
 * <p>The step library can be searched by description and by testcase name, which answers "what is
 * this step called" and nothing else. The question that actually comes up while repairing a suite
 * is the other one: where else does this appear. A selector that turns out to be wrong was almost
 * certainly copied; a URL that changed is in more than one place; a data library about to be
 * renamed is read from testcases nobody remembers.</p>
 *
 * <p>Without it, a copy of a defect is found by accident — while looking at something else — which
 * means the copies nobody happened to look at stay broken, and the next run blames them on
 * flakiness.</p>
 */
@Component
public class SearchTestCaseContentTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_testcase_search_content";

    /** Enough to see a pattern across a suite; beyond it the search itself needs narrowing. */
    private static final int DEFAULT_MAX_RESULTS = 100;
    private static final int HARD_MAX_RESULTS = 500;

    /** Refuses a search so short it would match most of the suite and say nothing. */
    private static final int MIN_SEARCH_LENGTH = 2;

    private final ITestCaseContentSearchService searchService;
    private final MCPUserContextService userContext;
    private final MCPLogUtils mcpLogUtils;

    public SearchTestCaseContentTool(ITestCaseContentSearchService searchService,
                                     MCPUserContextService userContext,
                                     MCPLogUtils mcpLogUtils) {
        this.searchService = searchService;
        this.userContext = userContext;
        this.mcpLogUtils = mcpLogUtils;
    }

    @Override
    public McpServerFeatures.SyncToolSpecification toToolSpecification() {
        return new McpServerFeatures.SyncToolSpecification(
                createTool(),
                (exchange, request) -> execute(MCPToolUtils.argumentsOrEmpty(request.arguments()), exchange)
        );
    }

    private McpSchema.Tool createTool() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("search", Map.of(
                "type", "string",
                "description", "The literal text to find. Matched case-insensitively, anywhere in the field. "
                        + "Not a pattern: % and _ are matched as themselves, so searching "
                        + "\"%property.LOGIN%\" finds that property reference and nothing else."
        ));
        properties.put("testFolder", Map.of(
                "type", "string",
                "description", "Restrict to one test folder. Omit to search the whole active system context."
        ));
        properties.put("testcase", Map.of(
                "type", "string",
                "description", "Restrict to one testcase. Use it to answer \"where in this testcase\" rather "
                        + "than \"where in the suite\"."
        ));
        properties.put("scopes", Map.of(
                "type", "array",
                "items", Map.of("type", "string", "enum",
                        List.of("ACTION", "CONTROL", "STEP", "PROPERTY")),
                "description", "What to search. ACTION and CONTROL cover operands, conditions and "
                        + "descriptions; STEP covers step descriptions and conditions; PROPERTY covers "
                        + "property values, which is where a data library or a SQL query is named. Defaults "
                        + "to all four."
        ));
        properties.put("maxResults", Map.of(
                "type", "integer",
                "description", "Cap on matches returned. Defaults to " + DEFAULT_MAX_RESULTS
                        + ", maximum " + HARD_MAX_RESULTS + "."
        ));

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Finds every place a string appears inside the testcases of your active systems: action and
                control operands, conditions, step descriptions and property values.

                This is how you find the other copies of something. Call it before repairing a selector, a
                URL or a hardcoded value: whatever you are about to fix in one testcase was very likely
                copied into others, and fixing only the one you were shown leaves the rest to fail later and
                look like flakiness.

                Also call it before renaming or deleting anything shared — a data library, a property, an
                application object — to see what reads it.

                The search is literal, not a pattern, so you can search for "%property.NAME%", "//div[@id=",
                or a full URL as they are written.

                Results carry the exact coordinates of each match, so the fix goes straight to the right row
                with cerberus_testcase_step_action_update or its neighbours.
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        List.of("search"),
                        null,
                        null,
                        null
                ),
                null,
                MCPToolUtils.readOnlyAnnotations("Search testcase content", false),
                null
        );
    }

    private McpSchema.CallToolResult execute(Map<String, Object> args, McpSyncServerExchange exchange) {
        String search = MCPToolUtils.getString(args, "search", "");
        String testFolder = MCPToolUtils.getString(args, "testFolder", "").trim();
        String testcase = MCPToolUtils.getString(args, "testcase", "").trim();
        List<String> requestedScopes = MCPToolUtils.getStringList(args, "scopes", List.of());
        int maxResults = Math.min(MCPToolUtils.getInteger(args, "maxResults", DEFAULT_MAX_RESULTS), HARD_MAX_RESULTS);

        String login = userContext.getLogin(exchange);
        mcpLogUtils.call(TOOL_NAME, "testcase_search_content",
                String.format("MCP tool %s called with search=%s testFolder=%s testcase=%s",
                        TOOL_NAME, search, testFolder, testcase), login);

        if (search.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: search");
        }
        if (search.trim().length() < MIN_SEARCH_LENGTH) {
            return MCPToolUtils.errorText("Search '" + search + "' is too short to be useful — it would match "
                    + "most of the suite. Give at least " + MIN_SEARCH_LENGTH + " characters.");
        }
        if (maxResults <= 0) {
            return MCPToolUtils.errorText("maxResults must be positive.");
        }

        List<TestCaseContentMatch.Scope> scopes = new ArrayList<>();
        for (String requested : requestedScopes) {
            try {
                scopes.add(TestCaseContentMatch.Scope.valueOf(requested.trim().toUpperCase()));
            } catch (IllegalArgumentException e) {
                return MCPToolUtils.errorText("Unknown scope '" + requested + "'. Supported scopes: ACTION, "
                        + "CONTROL, STEP, PROPERTY.");
            }
        }
        if (scopes.isEmpty()) {
            scopes = List.of(TestCaseContentMatch.Scope.ACTION, TestCaseContentMatch.Scope.CONTROL,
                    TestCaseContentMatch.Scope.STEP, TestCaseContentMatch.Scope.PROPERTY);
        }

        // The search reads across every testcase of a system, so it stays inside the caller's active
        // context like the other read tools do.
        List<String> systems;
        if (login == null) {
            return MCPToolUtils.errorText("Unable to resolve the authenticated MCP user for this call.");
        }
        try {
            systems = userContext.getContextSystems(userContext.getUser(login));
        } catch (CerberusException e) {
            return MCPToolUtils.errorText("Unable to read system context for '" + login + "': "
                    + e.getMessageError().getDescription());
        }
        if (systems.isEmpty()) {
            return MCPToolUtils.errorText("No active system in your MCP context, so there is nothing to search. "
                    + "Call cerberus_context_system_list, then cerberus_context_system_update (action=add).");
        }

        List<TestCaseContentMatch> matches =
                searchService.search(search, systems, testFolder, testcase, scopes, maxResults);

        List<Map<String, Object>> rendered = new ArrayList<>();
        Set<String> testcasesTouched = new LinkedHashSet<>();
        for (TestCaseContentMatch match : matches) {
            testcasesTouched.add(match.test() + "/" + match.testcase());
            rendered.add(describe(match));
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("search", search);
        response.put("systems", systems);
        response.put("scopes", scopes.stream().map(Enum::name).toList());
        response.put("matchCount", rendered.size());
        response.put("testcaseCount", testcasesTouched.size());
        response.put("testcases", testcasesTouched);
        response.put("matches", rendered);

        if (rendered.isEmpty()) {
            response.put("message", "Nothing matches. The search is literal and case-insensitive — check the "
                    + "exact spelling, and remember that a value may be written through a property or an "
                    + "application object rather than inline.");
        } else if (rendered.size() >= maxResults) {
            response.put("message", "The result was cut at " + maxResults + " matches, so there are probably "
                    + "more. Narrow with testFolder, or raise maxResults.");
        }

        return MCPToolUtils.successJson(response);
    }

    /**
     * Renders one match with the coordinates needed to change it.
     */
    private Map<String, Object> describe(TestCaseContentMatch match) {
        Map<String, Object> described = new LinkedHashMap<>();
        described.put("scope", match.scope().name());
        described.put("system", match.system());
        described.put("testFolder", match.test());
        described.put("testcase", match.testcase());

        switch (match.scope()) {
            case ACTION -> {
                described.put("stepId", match.stepId());
                described.put("actionId", match.actionId());
                described.put("action", match.name());
            }
            case CONTROL -> {
                described.put("stepId", match.stepId());
                described.put("actionId", match.actionId());
                described.put("controlId", match.controlId());
                described.put("control", match.name());
            }
            case STEP -> described.put("stepId", match.stepId());
            case PROPERTY -> {
                described.put("country", match.country());
                described.put("property", match.name());
            }
        }

        described.put("field", match.field());
        described.put("value", match.value());
        return described;
    }
}
