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
 * MCP tool that corrects a data library and its sub-data mapping, under the tool name
 * {@code cerberus_datalib_update}.
 *
 * <p>The counterpart of {@code cerberus_datalib_get}: that tool shows why a
 * {@code getFromDataLib} property fails to resolve, this one repairs it.</p>
 *
 * <p>Sub-data entries are <strong>merged</strong> by default rather than replaced. The underlying
 * {@link ITestDataLibDataService#compareListAndUpdateInsertDeleteElements} deletes every entry
 * absent from the list it is given, so handing it the one entry a caller wants to fix would wipe
 * all the others. The tool therefore reads the current entries, applies the supplied ones by
 * {@code subData} key and passes the merged list on, which makes "fix one mapping" the safe
 * default and full replacement an explicit choice.</p>
 */
@Component
public class UpdateTestDataLibTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_datalib_update";

    private final ITestDataLibService testDataLibService;
    private final ITestDataLibDataService testDataLibDataService;
    private final MCPLogUtils mcpLogUtils;

    public UpdateTestDataLibTool(ITestDataLibService testDataLibService,
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
     * <p>The library is addressed by its numeric id rather than its name, because the same name
     * covers several variants and correcting the wrong one would leave the failure untouched while
     * appearing to fix it. The id comes from {@code cerberus_datalib_get}, which states which
     * variant a given context resolves to.</p>
     *
     * @return the fully-described {@link McpSchema.Tool} for registration with the MCP server.
     */
    private McpSchema.Tool createTool() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("testDataLibId", Map.of(
                "type", "integer",
                "description", "Numeric id of the library variant to correct, from cerberus_datalib_get. "
                        + "Not the name: several variants share a name and only one of them applies to a "
                        + "given system / environment / country."
        ));
        properties.put("updates", Map.of(
                "type", "object",
                "description", "Optional library fields to change. Supported keys: description, group, "
                        + "database, script, service, servicePath, method, csvUrl, separator. Fields not "
                        + "named keep their current value."
        ));
        properties.put("subData", Map.of(
                "type", "array",
                "description", """
                        Sub-data entries to set, merged into the existing ones by subData name. An entry whose
                        subData already exists is updated, a new one is added, and entries you do not mention are
                        left untouched.

                        Use the mapping field that matches the library type — column for SQL, columnPosition for
                        FILE, parsingAnswer for a SERVICE answering XML or JSON. cerberus_datalib_get names the
                        one that applies and flags entries whose field is empty.

                        The entry whose subData is an empty string is the key entry, which the engine requires.
                        """,
                "items", Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "subData", Map.of("type", "string",
                                        "description", "Entry name, as used in %property.LIBRARY.ENTRY%. "
                                                + "An empty string designates the required key entry."),
                                "column", Map.of("type", "string",
                                        "description", "Column name returned by the query, for an SQL library."),
                                "parsingAnswer", Map.of("type", "string",
                                        "description", "Parsing expression, for a SERVICE library answering XML or JSON."),
                                "columnPosition", Map.of("type", "string",
                                        "description", "Column position, for a FILE library read as CSV."),
                                "value", Map.of("type", "string",
                                        "description", "Literal value, for an INTERNAL library."),
                                "description", Map.of("type", "string",
                                        "description", "Optional description of the entry.")
                        ),
                        "required", List.of("subData")
                )
        ));
        properties.put("replaceAllSubData", Map.of(
                "type", "boolean",
                "description", "Set to true to make subData the complete list, deleting every entry not named "
                        + "in it. Defaults to false, which merges. Only use it when the user asked to rebuild "
                        + "the mapping from scratch — it removes entries other testcases may rely on."
        ));

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Corrects a Cerberus data library: its definition, its sub-data mapping, or both.

                Call this after cerberus_datalib_get has shown why a getFromDataLib property does not resolve —
                typically an entry whose mapping field is empty, or a missing key entry.

                Sub-data entries are merged by name, so sending just the one you are fixing is safe and leaves
                the others alone.
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        List.of("testDataLibId"),
                        null,
                        null,
                        null
                ),
                null,
                MCPToolUtils.updateAnnotations("Update data library", false),
                null
        );
    }

    /**
     * Applies the requested library and sub-data changes.
     *
     * @param args tool arguments extracted from the MCP request (may be empty but never null).
     * @return a {@link McpSchema.CallToolResult} summarising what changed, or an error.
     */
    @SuppressWarnings("unchecked")
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        int testDataLibId = MCPToolUtils.getInteger(args, "testDataLibId", 0);
        boolean replaceAll = MCPToolUtils.getBoolean(args, "replaceAllSubData", false);

        mcpLogUtils.call(TOOL_NAME, "datalib_update",
                String.format("MCP tool %s called with testDataLibId=%s replaceAllSubData=%s",
                        TOOL_NAME, testDataLibId, replaceAll));

        if (testDataLibId <= 0) {
            return MCPToolUtils.errorText("Missing or invalid required parameter: testDataLibId");
        }

        AnswerItem<TestDataLib> readAnswer = testDataLibService.readByKey(testDataLibId);
        if (!readAnswer.isCodeStringEquals("OK") || readAnswer.getItem() == null) {
            return MCPToolUtils.errorText("Data library does not exist: " + testDataLibId);
        }

        TestDataLib lib = readAnswer.getItem();
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("testDataLibId", testDataLibId);
        response.put("name", MCPToolUtils.nullSafe(lib.getName()));
        response.put("type", MCPToolUtils.nullSafe(lib.getType()));

        Object updatesObject = args.get("updates");
        List<String> changedFields = new ArrayList<>();
        if (updatesObject instanceof Map) {
            try {
                applyUpdates(lib, (Map<String, Object>) updatesObject, changedFields);
            } catch (IllegalArgumentException e) {
                return MCPToolUtils.errorText(e.getMessage());
            }
            if (!changedFields.isEmpty()) {
                Answer updateAnswer = testDataLibService.update(lib);
                if (!updateAnswer.isCodeStringEquals("OK")) {
                    return MCPToolUtils.errorText("Unable to update data library " + testDataLibId + ": "
                            + updateAnswer.getMessageDescription());
                }
            }
        }
        response.put("updatedFields", changedFields);

        Object subDataObject = args.get("subData");
        if (subDataObject instanceof List) {
            List<Object> requested = (List<Object>) subDataObject;
            if (!requested.isEmpty()) {
                McpSchema.CallToolResult failure = applySubData(lib, requested, replaceAll, response);
                if (failure != null) {
                    return failure;
                }
            }
        }

        String mappingField = TestDataLibMappingUtils.mappingFieldFor(lib);
        response.put("mappingField", mappingField);
        response.put("status", "updated");
        response.put("nextStep", "Call cerberus_datalib_get on this library to confirm the mapping is now "
                + "complete, then re-run the testcase.");

        return MCPToolUtils.successJson(response);
    }

    /**
     * Applies the caller-supplied library field changes, rejecting unknown keys rather than
     * silently ignoring them.
     */
    private void applyUpdates(TestDataLib lib, Map<String, Object> updates, List<String> changedFields) {
        for (Map.Entry<String, Object> entry : updates.entrySet()) {
            String field = entry.getKey();
            String value = asString(entry.getValue(), field);

            switch (field) {
                case "description" -> lib.setDescription(value);
                case "group" -> lib.setGroup(value);
                case "database" -> lib.setDatabase(value);
                case "script" -> lib.setScript(value);
                case "service" -> lib.setService(value);
                case "servicePath" -> lib.setServicePath(value);
                case "method" -> lib.setMethod(value);
                case "csvUrl" -> lib.setCsvUrl(value);
                case "separator" -> lib.setSeparator(value);
                default -> throw new IllegalArgumentException(
                        "Unsupported field for data library update: " + field);
            }
            changedFields.add(field);
        }
    }

    /**
     * Merges (or replaces) the sub-data entries and persists them.
     *
     * @return {@code null} on success, or the error result to return to the caller.
     */
    @SuppressWarnings("unchecked")
    private McpSchema.CallToolResult applySubData(TestDataLib lib, List<Object> requested,
                                                  boolean replaceAll, Map<String, Object> response) {
        Integer libId = lib.getTestDataLibID();

        List<TestDataLibData> current = new ArrayList<>();
        AnswerList<TestDataLibData> currentAnswer = testDataLibDataService.readByVarious(libId, null, null, null);
        if (currentAnswer.getDataList() != null) {
            current.addAll(currentAnswer.getDataList());
        }

        // Start from the existing entries unless the caller explicitly asked for a clean slate,
        // because the persistence call deletes whatever is missing from the list it receives.
        List<TestDataLibData> merged = replaceAll ? new ArrayList<>() : new ArrayList<>(current);
        List<String> touched = new ArrayList<>();

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
            TestDataLibData target = merged.stream()
                    .filter(existing -> subDataName.equals(MCPToolUtils.nullSafe(existing.getSubData())))
                    .findFirst()
                    .orElse(null);

            if (target == null) {
                target = new TestDataLibData();
                target.setTestDataLibID(libId);
                target.setSubData(subDataName);
                // The persistence layer writes these columns unconditionally, so an entry created
                // here starts from empty strings rather than nulls.
                target.setColumn("");
                target.setParsingAnswer("");
                target.setColumnPosition("");
                target.setValue("");
                target.setDescription("");
                target.setEncrypt("N");
                merged.add(target);
            }

            if (entry.containsKey("column")) target.setColumn(MCPToolUtils.getString(entry, "column", ""));
            if (entry.containsKey("parsingAnswer")) target.setParsingAnswer(MCPToolUtils.getString(entry, "parsingAnswer", ""));
            if (entry.containsKey("columnPosition")) target.setColumnPosition(MCPToolUtils.getString(entry, "columnPosition", ""));
            if (entry.containsKey("value")) target.setValue(MCPToolUtils.getString(entry, "value", ""));
            if (entry.containsKey("description")) target.setDescription(MCPToolUtils.getString(entry, "description", ""));

            touched.add(subDataName.isEmpty() ? "(key entry)" : subDataName);
        }

        Answer answer = testDataLibDataService.compareListAndUpdateInsertDeleteElements(libId, merged);
        if (!answer.isCodeStringEquals("OK")) {
            return MCPToolUtils.errorText("Unable to update the sub-data of library " + libId + ": "
                    + answer.getMessageDescription());
        }

        response.put("subDataTouched", touched);
        response.put("subDataTotalAfter", merged.size());
        if (replaceAll) {
            List<String> removed = current.stream()
                    .map(existing -> MCPToolUtils.nullSafe(existing.getSubData()))
                    .filter(existing -> merged.stream()
                            .noneMatch(kept -> existing.equals(MCPToolUtils.nullSafe(kept.getSubData()))))
                    .map(existing -> existing.isEmpty() ? "(key entry)" : existing)
                    .toList();
            if (!removed.isEmpty()) {
                response.put("subDataRemoved", removed);
            }
        }

        return null;
    }

    /**
     * Coerces a raw MCP argument to a string, refusing anything that is not one so a mistyped
     * value cannot be written as its {@code toString()}.
     */
    private String asString(Object value, String field) {
        if (value == null) {
            return "";
        }
        if (value instanceof String stringValue) {
            return stringValue.trim();
        }
        throw new IllegalArgumentException("Field '" + field + "' must be a string.");
    }

}
