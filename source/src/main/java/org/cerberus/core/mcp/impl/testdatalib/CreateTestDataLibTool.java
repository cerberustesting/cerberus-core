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
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP tool that creates a data library together with its sub-data mapping, under the tool name
 * {@code cerberus_datalib_create}.
 *
 * <p>The library and its entries are created in one call rather than separately, because a library
 * without entries is inert: a property referencing it resolves to nothing, and the engine rejects
 * it outright when the key entry is missing. Creating both together is what makes the result
 * immediately usable.</p>
 *
 * <p>The tool checks the two things that otherwise turn into a silent failure hours later — that a
 * key entry exists, and that each entry fills the mapping field this library's type actually reads.
 * Neither is enforced by the database, and both produce a property that stays unresolved with no
 * error of its own. They are reported as warnings rather than refusals: creating the shell first
 * and filling it afterwards is legitimate, so the tool says plainly what is still missing instead
 * of blocking.</p>
 */
@Component
public class CreateTestDataLibTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_datalib_create";

    private final ITestDataLibService testDataLibService;
    private final ITestDataLibDataService testDataLibDataService;
    private final MCPLogUtils mcpLogUtils;

    public CreateTestDataLibTool(ITestDataLibService testDataLibService,
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
                "description", "Name of the library, used in properties as %property.NAME.ENTRY% and directly "
                        + "as %datalib.NAME.ENTRY%."
        ));
        properties.put("type", Map.of(
                "type", "string",
                "description", """
                        Where the data comes from, which also decides the mapping field each entry must fill:
                        - SQL: a query against a Cerberus-declared database. Entries map to a column name.
                        - SERVICE: a call to an app service. Entries map to a parsing expression when the
                          service answers XML or JSON, to a column position otherwise.
                        - FILE: a CSV file. Entries map to a column position.
                        - INTERNAL: values written in the entries themselves, no external source.
                        """,
                "enum", List.of(
                        TestDataLib.TYPE_INTERNAL,
                        TestDataLib.TYPE_SQL,
                        TestDataLib.TYPE_SERVICE,
                        TestDataLib.TYPE_FILE
                )
        ));
        properties.put("system", Map.of(
                "type", "string",
                "description", "System this variant applies to. Leave empty to make it apply to every system — "
                        + "an empty qualifier is a wildcard, not a missing value."
        ));
        properties.put("environment", Map.of(
                "type", "string",
                "description", "Environment this variant applies to. Empty means every environment."
        ));
        properties.put("country", Map.of(
                "type", "string",
                "description", "Country this variant applies to. Empty means every country."
        ));
        properties.put("description", Map.of(
                "type", "string",
                "description", "What this library provides. Worth filling: it is what tells one variant of a "
                        + "name from another."
        ));
        properties.put("group", Map.of(
                "type", "string",
                "description", "Optional group used to organise libraries."
        ));
        properties.put("database", Map.of(
                "type", "string",
                "description", "For an SQL library: the Cerberus database name to query."
        ));
        properties.put("script", Map.of(
                "type", "string",
                "description", "For an SQL library: the query. It may itself contain %system.…% variables, "
                        + "for example to filter on %system.COUNTRY%."
        ));
        properties.put("service", Map.of(
                "type", "string",
                "description", "For a SERVICE library: the app service to call, from cerberus_appservice_list."
        ));
        properties.put("servicePath", Map.of(
                "type", "string",
                "description", "For a SERVICE library: the path appended to the service."
        ));
        properties.put("method", Map.of(
                "type", "string",
                "description", "For a SERVICE library: the method to call it with."
        ));
        properties.put("csvUrl", Map.of(
                "type", "string",
                "description", "For a FILE library: the URL of the CSV file."
        ));
        properties.put("separator", Map.of(
                "type", "string",
                "description", "For a FILE library: the column separator. Defaults to a comma."
        ));
        properties.put("ignoreFirstLine", Map.of(
                "type", "boolean",
                "description", "For a FILE library: skip the header row. Defaults to false."
        ));
        properties.put("subData", Map.of(
                "type", "array",
                "description", """
                        The entries this library exposes. Create them here rather than afterwards: a library
                        with no entry resolves to nothing.

                        Include the key entry — the one whose subData is an empty string. The engine refuses a
                        library without it, whatever the other entries contain, and its mapping designates the
                        column that identifies a row.

                        Fill the mapping field matching the type: column for SQL, columnPosition for FILE,
                        parsingAnswer for a SERVICE answering XML or JSON, value for INTERNAL. An entry whose
                        field is empty is skipped by the engine without a word.
                        """,
                "items", Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "subData", Map.of("type", "string",
                                        "description", "Entry name, used as %property.LIBRARY.ENTRY%. "
                                                + "An empty string designates the required key entry."),
                                "column", Map.of("type", "string",
                                        "description", "Column name returned by the query, for SQL."),
                                "parsingAnswer", Map.of("type", "string",
                                        "description", "Parsing expression, for SERVICE with an XML or JSON answer."),
                                "columnPosition", Map.of("type", "string",
                                        "description", "Column position, for FILE."),
                                "value", Map.of("type", "string",
                                        "description", "Literal value, for INTERNAL."),
                                "description", Map.of("type", "string",
                                        "description", "Optional description of the entry.")
                        ),
                        "required", List.of("subData")
                )
        ));

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Creates a Cerberus data library and the entries it exposes, in one call.

                Call this when a testcase needs data that does not belong in the testcase itself — rows from a
                database, an answer from a service, a CSV file — and cerberus_datalib_list shows no library
                already provides it.

                Give the entries in subData, including the key entry whose subData is an empty string: a library
                without it is rejected by the engine, and one without entries resolves to nothing.

                The same name may exist several times, once per system / environment / country variant, an empty
                qualifier meaning "any". Creating a second variant of a name is deliberate; check with
                cerberus_datalib_list first so you do not shadow an existing one by accident.

                Once created, read it back with cerberus_datalib_get to confirm the engine will accept it.
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        List.of("name", "type"),
                        null,
                        null,
                        null
                ),
                null,
                MCPToolUtils.createAnnotations("Create data library", false),
                null
        );
    }

    /**
     * Validates the request, creates the library, then its entries.
     *
     * @param args tool arguments extracted from the MCP request (may be empty but never null).
     * @return a {@link McpSchema.CallToolResult} describing what was created, or an error.
     */
    @SuppressWarnings("unchecked")
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String name = MCPToolUtils.getString(args, "name", "").trim();
        String type = MCPToolUtils.getString(args, "type", "").trim();
        String system = MCPToolUtils.getString(args, "system", "").trim();
        String environment = MCPToolUtils.getString(args, "environment", "").trim();
        String country = MCPToolUtils.getString(args, "country", "").trim();

        mcpLogUtils.call(TOOL_NAME, "datalib_create",
                String.format("MCP tool %s called with name=%s type=%s system=%s environment=%s country=%s",
                        TOOL_NAME, name, type, system, environment, country));

        if (name.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: name");
        }
        if (type.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: type");
        }

        List<String> supportedTypes = List.of(TestDataLib.TYPE_INTERNAL, TestDataLib.TYPE_SQL,
                TestDataLib.TYPE_SERVICE, TestDataLib.TYPE_FILE);
        if (!supportedTypes.contains(type)) {
            return MCPToolUtils.errorText("Unsupported type: " + type + ". Supported types: " + supportedTypes);
        }

        // Refuse an exact duplicate of the (name, system, environment, country) key. The lookup the
        // engine uses matches wildcards too, so it cannot tell an existing variant from the one
        // being created; the comparison is done here on the four qualifiers exactly.
        AnswerList<TestDataLib> existing = testDataLibService.readByVariousByCriteria(
                name, null, null, null, null, 0, 0, "Name", "asc", null, null);
        if (existing.getDataList() != null) {
            for (TestDataLib lib : existing.getDataList()) {
                if (name.equalsIgnoreCase(MCPToolUtils.nullSafe(lib.getName()))
                        && system.equalsIgnoreCase(MCPToolUtils.nullSafe(lib.getSystem()))
                        && environment.equalsIgnoreCase(MCPToolUtils.nullSafe(lib.getEnvironment()))
                        && country.equalsIgnoreCase(MCPToolUtils.nullSafe(lib.getCountry()))) {
                    return MCPToolUtils.errorText("A data library named '" + name + "' already exists for "
                            + "system='" + system + "' environment='" + environment + "' country='" + country
                            + "' (id " + lib.getTestDataLibID() + "). Update it with cerberus_datalib_update, "
                            + "or create a variant with different qualifiers.");
                }
            }
        }

        TestDataLib lib = new TestDataLib();
        lib.setName(name);
        lib.setType(type);
        lib.setSystem(system);
        lib.setEnvironment(environment);
        lib.setCountry(country);
        lib.setGroup(MCPToolUtils.getString(args, "group", ""));
        lib.setDescription(MCPToolUtils.getString(args, "description", ""));
        lib.setDatabase(MCPToolUtils.getString(args, "database", ""));
        lib.setScript(MCPToolUtils.getString(args, "script", ""));
        lib.setService(MCPToolUtils.getString(args, "service", ""));
        lib.setServicePath(MCPToolUtils.getString(args, "servicePath", ""));
        lib.setMethod(MCPToolUtils.getString(args, "method", ""));
        lib.setCsvUrl(MCPToolUtils.getString(args, "csvUrl", ""));
        lib.setSeparator(MCPToolUtils.getString(args, "separator", ","));
        lib.setIgnoreFirstLine(MCPToolUtils.getBoolean(args, "ignoreFirstLine", false));
        lib.setPrivateData("N");
        lib.setCreator("MCP");
        lib.setLastModifier("MCP");

        AnswerItem<TestDataLib> created = testDataLibService.create(lib);
        if (!created.isCodeStringEquals("OK")) {
            return MCPToolUtils.errorText("Unable to create data library '" + name + "': "
                    + created.getMessageDescription());
        }

        TestDataLib createdLib = created.getItem() != null ? created.getItem() : lib;
        Integer libId = createdLib.getTestDataLibID();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "created");
        response.put("testDataLibId", libId);
        response.put("name", name);
        response.put("type", type);
        response.put("system", system);
        response.put("environment", environment);
        response.put("country", country);

        String mappingField = TestDataLibMappingUtils.mappingFieldFor(createdLib);
        response.put("mappingField", mappingField);
        response.put("mappingFieldExplanation", TestDataLibMappingUtils.mappingExplanationFor(createdLib));

        Object subDataObject = args.get("subData");
        List<Object> requested = subDataObject instanceof List ? (List<Object>) subDataObject : List.of();

        if (libId == null) {
            response.put("warning", "The library was created but its id was not returned, so the entries could "
                    + "not be added. Read it back with cerberus_datalib_get and add them with "
                    + "cerberus_datalib_update.");
            return MCPToolUtils.successJson(response);
        }

        McpSchema.CallToolResult failure = createSubData(libId, requested, mappingField, response);
        if (failure != null) {
            return failure;
        }

        addUsabilityWarnings(name, requested, mappingField, response);

        return MCPToolUtils.successJson(response);
    }

    /**
     * Persists the entries of the freshly created library.
     *
     * <p>Each is created individually rather than through the list-synchronising call used on
     * update: there is nothing to reconcile here, and a per-entry answer says exactly which one
     * failed if one does.</p>
     *
     * @return {@code null} on success, or the error result to return to the caller.
     */
    @SuppressWarnings("unchecked")
    private McpSchema.CallToolResult createSubData(Integer libId, List<Object> requested,
                                                   String mappingField, Map<String, Object> response) {
        List<String> created = new ArrayList<>();
        List<String> failed = new ArrayList<>();

        for (Object item : requested) {
            if (!(item instanceof Map)) {
                return MCPToolUtils.errorText("Each subData item must be an object.");
            }
            Map<String, Object> entry = (Map<String, Object>) item;
            if (!entry.containsKey("subData")) {
                return MCPToolUtils.errorText("Each subData item must carry a 'subData' key, "
                        + "possibly an empty string for the key entry.");
            }

            String subDataName = MCPToolUtils.getString(entry, "subData", "");

            TestDataLibData data = new TestDataLibData();
            data.setTestDataLibID(libId);
            data.setSubData(subDataName);
            // The persistence layer writes every column, so an unset one starts empty rather than null.
            data.setColumn(MCPToolUtils.getString(entry, "column", ""));
            data.setParsingAnswer(MCPToolUtils.getString(entry, "parsingAnswer", ""));
            data.setColumnPosition(MCPToolUtils.getString(entry, "columnPosition", ""));
            data.setValue(MCPToolUtils.getString(entry, "value", ""));
            data.setDescription(MCPToolUtils.getString(entry, "description", ""));
            data.setEncrypt("N");

            Answer answer = testDataLibDataService.create(data);
            if (answer.isCodeStringEquals("OK")) {
                created.add(subDataName.isEmpty() ? "(key entry)" : subDataName);
            } else {
                failed.add((subDataName.isEmpty() ? "(key entry)" : subDataName)
                        + " (" + answer.getMessageDescription() + ")");
            }
        }

        response.put("subDataCreated", created);
        if (!failed.isEmpty()) {
            response.put("subDataFailed", failed);
        }

        return null;
    }

    /**
     * Reports what would stop the new library from resolving.
     *
     * <p>Both checks mirror what the engine does silently: it rejects a library with no key entry,
     * and it skips an entry whose mapping field for this type is empty. Neither raises an error of
     * its own, so a property referencing such an entry simply comes back unresolved — which is the
     * failure this tool exists to prevent rather than reproduce.</p>
     */
    @SuppressWarnings("unchecked")
    private void addUsabilityWarnings(String name, List<Object> requested,
                                      String mappingField, Map<String, Object> response) {
        List<String> warnings = new ArrayList<>();

        if (requested.isEmpty()) {
            warnings.add("This library has no entry yet, so nothing can be read from it. Add them with "
                    + "cerberus_datalib_update, including the key entry whose subData is an empty string.");
            response.put("warnings", warnings);
            response.put("usable", false);
            return;
        }

        boolean hasKeyEntry = false;
        List<String> unmapped = new ArrayList<>();

        for (Object item : requested) {
            Map<String, Object> entry = (Map<String, Object>) item;
            String subDataName = MCPToolUtils.getString(entry, "subData", "");
            if (subDataName.isEmpty()) {
                hasKeyEntry = true;
            }
            if (MCPToolUtils.getString(entry, mappingField, "").isBlank()) {
                unmapped.add(subDataName.isEmpty() ? "(key entry)" : subDataName);
            }
        }

        if (!hasKeyEntry) {
            warnings.add("No key entry was created (a sub-data whose subData is an empty string). The engine "
                    + "rejects the library in that state, whatever the other entries contain. Add it with "
                    + "cerberus_datalib_update.");
        }
        if (!unmapped.isEmpty()) {
            warnings.add("These entries have an empty '" + mappingField + "', the field this library's type "
                    + "reads, so the engine skips them and any property referencing them stays unresolved: "
                    + unmapped + ".");
        }

        response.put("usable", warnings.isEmpty());
        if (!warnings.isEmpty()) {
            warnings.add("A property written %property." + name + ".ENTRY% only resolves when ENTRY exists "
                    + "with a non-empty '" + mappingField + "'.");
            response.put("warnings", warnings);
        }
    }

}
