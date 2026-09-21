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
package org.cerberus.core.mcp.impl;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import org.cerberus.core.mcp.MCPTool;
import org.cerberus.core.mcp.util.CerberusVariableCatalog;
import org.cerberus.core.mcp.util.MCPLogUtils;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP tool that answers "what can I write in this value field", under the tool name
 * {@code cerberus_variable_list}.
 *
 * <p>Substitution is the heart of a Cerberus testcase: almost every action and control operand is a
 * string the engine rewrites before use. An agent that does not know the vocabulary either hard
 * codes a value that then breaks on the next country, or invents a variable that silently stays in
 * the string and reaches the action as literal text.</p>
 *
 * <p>The system variables in particular are recorded nowhere an agent could reach: they are literal
 * replacements in the engine, absent from every table, and mentioned in the documentation only in
 * the changelog entry of the release that added them. This tool is the first place they are
 * enumerated.</p>
 *
 * <p>It also states the three other families and where to enumerate each, because they are what an
 * agent reaches for next and choosing the wrong one is a common detour — most notably that a data
 * library can be read directly with {@code %datalib.…%}, without declaring a property for it.</p>
 */
@Component
public class ListCerberusVariablesTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_variable_list";

    private final MCPLogUtils mcpLogUtils;

    public ListCerberusVariablesTool(MCPLogUtils mcpLogUtils) {
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
        properties.put("family", Map.of(
                "type", "string",
                "description", "Narrow the list to one family of system variables.",
                "enum", CerberusVariableCatalog.FAMILIES
        ));
        properties.put("search", Map.of(
                "type", "string",
                "description", "Free-text filter on the variable name and its description. Use it when you know "
                        + "what you want but not how it is spelled — \"url\", \"country\", \"response\"."
        ));

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Lists the variables Cerberus substitutes inside a testcase value, with what each one holds and
                when it resolves.

                Call this before writing any action or control operand that should depend on the run — a URL, a
                country, an environment host, the response of the last service call. Never invent a variable: an
                unknown one raises no error, it stays in the string and the action receives the literal text.

                The response also names the three other substitution families and the tool that enumerates each,
                so you can tell a system variable from a property, an application object or a data library.

                Use cerberus_testcase_country_property_list with includeInherited to see the properties a given
                testcase can use, which is the part that depends on the testcase rather than on Cerberus itself.
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        null,
                        null,
                        null,
                        null
                ),
                null,
                MCPToolUtils.readOnlyAnnotations("List Cerberus variables", false),
                null
        );
    }

    /**
     * Filters the catalogue and returns it together with the other substitution families.
     *
     * @param args tool arguments extracted from the MCP request (may be empty but never null).
     * @return a {@link McpSchema.CallToolResult} carrying the variables, or an error.
     */
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String family = MCPToolUtils.getString(args, "family", "").trim();
        String search = MCPToolUtils.getString(args, "search", "").trim();

        mcpLogUtils.call(TOOL_NAME, "variable_list",
                String.format("MCP tool %s called with family=%s search=%s", TOOL_NAME, family, search));

        if (!family.isBlank() && !CerberusVariableCatalog.FAMILIES.contains(family)) {
            return MCPToolUtils.errorText("Unknown family: " + family
                    + ". Supported families: " + CerberusVariableCatalog.FAMILIES);
        }

        List<Map<String, Object>> variables = new ArrayList<>();
        for (CerberusVariableCatalog.Variable variable : CerberusVariableCatalog.all()) {
            if (!family.isBlank() && !family.equals(variable.family())) {
                continue;
            }
            if (!search.isBlank()
                    && !MCPToolUtils.containsIgnoreCase(variable.name(), search)
                    && !MCPToolUtils.containsIgnoreCase(variable.description(), search)) {
                continue;
            }

            Map<String, Object> map = new LinkedHashMap<>();
            map.put("variable", variable.name());
            map.put("family", variable.family());
            map.put("description", variable.description());
            // The single most useful field: a variable used before it can resolve fails silently.
            map.put("availability", variable.availability());
            String legacy = CerberusVariableCatalog.legacyForm(variable.name());
            if (!legacy.isEmpty()) {
                map.put("legacyForm", legacy);
            }
            variables.add(map);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("count", variables.size());
        response.put("systemVariables", variables);

        if (variables.isEmpty()) {
            response.put("message", "No system variable matches. Widen the search, or check the other "
                    + "substitution families below — what you are after may be a property, an application "
                    + "object or a data library rather than a system variable.");
        }

        response.put("otherSubstitutionFamilies", otherFamilies());
        response.put("howSubstitutionWorks", List.of(
                "The engine rewrites a value in this order: system variables, application objects, data "
                        + "libraries, then properties.",
                "It runs that whole sequence twice, so a property may itself contain another variable.",
                "An unknown variable is not an error: it is left as-is and the action receives the literal "
                        + "text. A run failing on \"unknown variable\" during property decoding means the "
                        + "property does not exist for that testcase and country.",
                "A variable that exists but has nothing to resolve to yields "
                        + CerberusVariableCatalog.NULL_MARKER + " or an empty string, never an error."));

        return MCPToolUtils.successJson(response);
    }

    /**
     * The three families this tool does not enumerate, each with the tool that does.
     *
     * <p>Included on every call, short: knowing that {@code %datalib.…%} exists is what stops an
     * agent from declaring a property whose only job is to read a data library.</p>
     */
    private List<Map<String, Object>> otherFamilies() {
        List<Map<String, Object>> families = new ArrayList<>();

        families.add(family("property", "%property.NAME%",
                "A value the testcase declares and the engine resolves at run time, per country — from a "
                + "database, a data library, the page, a service response.",
                "cerberus_testcase_country_property_list with includeInherited=true, which also reports the "
                + "properties inherited from library steps."));

        families.add(family("application object", "%object.NAME.value%",
                "A value shared across the testcases of one application, such as an element locator or a "
                + "reference picture. Also addressable as .picture and .picturepath.",
                "cerberus_application_object_list"));

        families.add(family("data library", "%datalib.NAME.SUBDATA%",
                "A data library read directly inside a value, with no property to declare. Prefer it when you "
                + "only need one entry once — declaring a property for that is a detour.",
                "cerberus_datalib_list, then cerberus_datalib_get to see the sub-data entries"));

        return families;
    }

    private Map<String, Object> family(String name, String syntax, String description, String listedBy) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("family", name);
        map.put("syntax", syntax);
        map.put("description", description);
        map.put("listedBy", listedBy);
        return map;
    }

}
