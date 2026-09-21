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
package org.cerberus.core.mcp.impl.testdatalib;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import org.cerberus.core.crud.entity.TestDataLib;
import org.cerberus.core.crud.entity.TestDataLibData;
import org.cerberus.core.crud.service.ITestDataLibDataService;
import org.cerberus.core.crud.service.ITestDataLibService;
import org.cerberus.core.mcp.MCPTool;
import org.cerberus.core.mcp.util.MCPLogUtils;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.cerberus.core.mcp.util.TestDataLibMappingUtils;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP tool that returns a data library together with its sub-data mapping, under the tool name
 * {@code cerberus_datalib_get}.
 *
 * <p>A property of type {@code getFromDataLib} resolves in two hops: {@code %property.NAME.ENTRY%}
 * finds the {@link TestDataLib} called NAME, then the {@link TestDataLibData} row whose
 * {@code subData} is ENTRY, and it is that row which says where the value comes from. When the
 * mapping is wrong the property silently stays unresolved and the step fails with
 * "unknown variable" — pointing at the testcase, which is not where the fault is. Nothing else in
 * the MCP surface reaches these two tables, so that failure was undiagnosable from an agent.</p>
 *
 * <p>Two properties of the model make this more than a plain read, and both are reproduced here:</p>
 * <ul>
 *   <li>A library is keyed by (name, system, environment, country), and several variants of the
 *       same name may coexist. The engine picks one with the resolution rule in
 *       {@code TestDataLibDAO.readByNameBySystemByEnvironmentByCountry}; this tool applies the
 *       same rule and says which variant would be used, so an agent cannot read one variant and
 *       draw conclusions about another.</li>
 *   <li>Which column of a sub-data row carries the mapping depends on the library's format, and a
 *       row whose relevant column is empty is skipped by the engine without a word. The tool
 *       therefore names the field that counts and flags the rows that would be ignored.</li>
 * </ul>
 *
 * <p>Delegation: {@link ITestDataLibService} and {@link ITestDataLibDataService}.</p>
 */
@Component
public class GetTestDataLibTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_datalib_get";

    private final ITestDataLibService testDataLibService;
    private final ITestDataLibDataService testDataLibDataService;
    private final MCPLogUtils mcpLogUtils;

    public GetTestDataLibTool(ITestDataLibService testDataLibService,
                              ITestDataLibDataService testDataLibDataService,
                              MCPLogUtils mcpLogUtils) {
        this.testDataLibService = testDataLibService;
        this.testDataLibDataService = testDataLibDataService;
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
        properties.put("name", Map.of(
                "type", "string",
                "description", "Name of the data library, as it appears in a property: for "
                        + "%property.DATA_VP_SOLD.ENSEIGNE% the name is DATA_VP_SOLD."
        ));
        properties.put("system", Map.of(
                "type", "string",
                "description", "System the property is resolved for. Supply it together with country and "
                        + "environment to learn which variant of the library the engine would actually use."
        ));
        properties.put("environment", Map.of(
                "type", "string",
                "description", "Environment the property is resolved for (e.g. QA, INT, PROD)."
        ));
        properties.put("country", Map.of(
                "type", "string",
                "description", "Country the property is resolved for (e.g. FR, NET)."
        ));

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Returns a Cerberus data library with its sub-data entries and the column mapping each one uses.

                Call this whenever a property of type getFromDataLib does not resolve — when a step fails with
                "Error on decoding Property" or an unknown variable, and the property is written
                %property.LIBRARY.ENTRY%. The fault is usually in the library's mapping, not in the testcase, and
                this is the only way to see it.

                The response lists every variant of the library, says which one the engine would pick for the
                system / country / environment you supply, names the mapping field that matters for that
                library's type, and flags the two misconfigurations that break resolution silently: a missing key
                entry, and entries whose mapping field is empty.

                Use cerberus_datalib_update to correct a mapping once you have found the problem.
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        List.of("name"),
                        null,
                        null,
                        null
                ),
                null,
                MCPToolUtils.readOnlyAnnotations("Get data library", false),
                null
        );
    }

    /**
     * Resolves the library, its variants and its sub-data mapping.
     *
     * @param args tool arguments extracted from the MCP request (may be empty but never null).
     * @return a {@link McpSchema.CallToolResult} describing the library, or an error.
     */
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String name = MCPToolUtils.getString(args, "name", "").trim();
        String system = MCPToolUtils.getString(args, "system", "").trim();
        String environment = MCPToolUtils.getString(args, "environment", "").trim();
        String country = MCPToolUtils.getString(args, "country", "").trim();

        mcpLogUtils.call(TOOL_NAME, "datalib_get",
                String.format("MCP tool %s called with name=%s system=%s environment=%s country=%s",
                        TOOL_NAME, name, system, environment, country));

        if (name.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: name");
        }

        List<TestDataLib> variants = readVariants(name);
        if (variants.isEmpty()) {
            return MCPToolUtils.errorText("No data library is named '" + name + "'. "
                    + "Call cerberus_datalib_list to see the libraries that exist.");
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("name", name);
        response.put("variantCount", variants.size());
        response.put("variants", variants.stream().map(this::toVariantSummary).toList());

        TestDataLib selected = TestDataLibMappingUtils.resolveVariant(variants, system, environment, country);
        if (selected == null) {
            response.put("resolved", null);
            response.put("message", "None of the " + variants.size() + " variant(s) of '" + name
                    + "' matches system='" + system + "' environment='" + environment + "' country='" + country
                    + "'. A variant matches when each of those three fields either equals the requested value "
                    + "or is left empty, which acts as a wildcard. This alone prevents the property from "
                    + "resolving in that context.");
            return MCPToolUtils.successJson(response);
        }

        response.put("resolved", toVariantSummary(selected));
        response.put("resolvedDetail", toDetail(selected));

        String mappingField = TestDataLibMappingUtils.mappingFieldFor(selected);
        response.put("mappingField", mappingField);
        response.put("mappingFieldExplanation", TestDataLibMappingUtils.mappingExplanationFor(selected));

        List<TestDataLibData> subData = readSubData(selected.getTestDataLibID());
        response.put("subDataCount", subData.size());
        response.put("subData", subData.stream().map(entry -> toSubDataMap(entry, mappingField)).toList());

        addDiagnostics(response, name, subData, mappingField);

        return MCPToolUtils.successJson(response);
    }

    /**
     * Loads every library sharing the requested name, across systems, environments and countries.
     */
    private List<TestDataLib> readVariants(String name) {
        List<TestDataLib> variants = new ArrayList<>();

        AnswerList<TestDataLib> answer = testDataLibService.readByVariousByCriteria(
                name, null, null, null, null, 0, 0, "Name", "asc", null, null);

        if (answer.getDataList() != null) {
            for (TestDataLib lib : answer.getDataList()) {
                // readByVariousByCriteria matches on a LIKE, so a library whose name merely contains
                // the requested one would come back too and would be a different library entirely.
                if (name.equalsIgnoreCase(MCPToolUtils.nullSafe(lib.getName()))) {
                    variants.add(lib);
                }
            }
        }

        return variants;
    }

    /**
     * Loads the sub-data entries of one library.
     */
    private List<TestDataLibData> readSubData(Integer testDataLibId) {
        List<TestDataLibData> entries = new ArrayList<>();

        AnswerList<TestDataLibData> answer =
                testDataLibDataService.readByVarious(testDataLibId, null, null, null);

        if (answer.getDataList() != null) {
            entries.addAll(answer.getDataList());
        }

        return entries;
    }

    /**
     * The identity of a variant: the four fields that decide which one the engine picks.
     */
    private Map<String, Object> toVariantSummary(TestDataLib lib) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("testDataLibId", lib.getTestDataLibID());
        map.put("name", MCPToolUtils.nullSafe(lib.getName()));
        // An empty system, environment or country is a wildcard, not a missing value.
        map.put("system", MCPToolUtils.nullSafe(lib.getSystem()));
        map.put("environment", MCPToolUtils.nullSafe(lib.getEnvironment()));
        map.put("country", MCPToolUtils.nullSafe(lib.getCountry()));
        map.put("type", MCPToolUtils.nullSafe(lib.getType()));
        map.put("description", MCPToolUtils.nullSafe(lib.getDescription()));
        return map;
    }

    /**
     * The definition of the selected variant — where it actually fetches its data from.
     */
    private Map<String, Object> toDetail(TestDataLib lib) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("group", MCPToolUtils.nullSafe(lib.getGroup()));
        map.put("database", MCPToolUtils.nullSafe(lib.getDatabase()));
        map.put("script", MCPToolUtils.nullSafe(lib.getScript()));
        map.put("service", MCPToolUtils.nullSafe(lib.getService()));
        map.put("servicePath", MCPToolUtils.nullSafe(lib.getServicePath()));
        map.put("method", MCPToolUtils.nullSafe(lib.getMethod()));
        map.put("csvUrl", MCPToolUtils.nullSafe(lib.getCsvUrl()));
        map.put("separator", MCPToolUtils.nullSafe(lib.getSeparator()));
        map.put("ignoreFirstLine", lib.isIgnoreFirstLine());
        return map;
    }

    /**
     * One sub-data entry, with the mapping value the engine will actually read for this library.
     *
     * <p>All three mapping columns are returned, not just the relevant one: seeing that a value was
     * filled into the wrong column — a column name entered where a JSON path was expected — is
     * usually the whole answer.</p>
     */
    private Map<String, Object> toSubDataMap(TestDataLibData entry, String mappingField) {
        Map<String, Object> map = new LinkedHashMap<>();
        String subData = MCPToolUtils.nullSafe(entry.getSubData());
        map.put("subData", subData);
        if (subData.isEmpty()) {
            map.put("role", "key entry — required by the engine; its mapping designates the column that "
                    + "identifies a row");
        }
        map.put("column", MCPToolUtils.nullSafe(entry.getColumn()));
        map.put("parsingAnswer", MCPToolUtils.nullSafe(entry.getParsingAnswer()));
        map.put("columnPosition", MCPToolUtils.nullSafe(entry.getColumnPosition()));
        map.put("effectiveMapping", TestDataLibMappingUtils.mappingValue(entry, mappingField));
        map.put("usedByEngine", !TestDataLibMappingUtils.mappingValue(entry, mappingField).isEmpty());
        map.put("value", MCPToolUtils.nullSafe(entry.getValue()));
        map.put("encrypt", MCPToolUtils.nullSafe(entry.getEncrypt()));
        map.put("description", MCPToolUtils.nullSafe(entry.getDescription()));
        return map;
    }

    /**
     * Flags the two misconfigurations that stop a property resolving without any error of their own.
     *
     * <p>Both are checks the engine performs silently: it refuses a library with no key entry, and
     * {@code TestDataLibDataService.readByVarious} filters out entries whose mapping column is
     * empty, so such an entry is simply absent from the resolved row and the property referencing
     * it comes back unknown.</p>
     */
    private void addDiagnostics(Map<String, Object> response, String name,
                                List<TestDataLibData> subData, String mappingField) {
        List<String> problems = new ArrayList<>();

        boolean hasKeyEntry = subData.stream()
                .anyMatch(entry -> MCPToolUtils.nullSafe(entry.getSubData()).isEmpty());
        if (!hasKeyEntry) {
            problems.add("This library has no key entry (a sub-data row whose subData is empty). The engine "
                    + "rejects the library outright in that state, whatever the other entries contain.");
        }

        List<String> ignored = subData.stream()
                .filter(entry -> TestDataLibMappingUtils.mappingValue(entry, mappingField).isEmpty())
                .map(entry -> {
                    String subDataName = MCPToolUtils.nullSafe(entry.getSubData());
                    return subDataName.isEmpty() ? "(key entry)" : subDataName;
                })
                .toList();
        if (!ignored.isEmpty()) {
            problems.add("These entries have an empty '" + mappingField + "', which is the field this library's "
                    + "type uses, so the engine skips them and any property referencing them stays unresolved: "
                    + ignored + ".");
        }

        if (problems.isEmpty()) {
            response.put("configurationOk", true);
        } else {
            response.put("configurationOk", false);
            response.put("problems", problems);
            response.put("hint", "A property written %property." + name + ".ENTRY% can only resolve when ENTRY "
                    + "appears above with a non-empty '" + mappingField + "'. Fix it with cerberus_datalib_update.");
        }
    }

}
