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
package org.cerberus.core.mcp.util;

import org.cerberus.core.crud.entity.TestDataLib;
import org.cerberus.core.crud.entity.TestDataLibData;

import java.util.Comparator;
import java.util.List;

/**
 * Reproduces the two data-library rules the execution engine applies, so MCP tools describe what
 * Cerberus will really do rather than a plausible approximation of it.
 *
 * <p>Both rules live in code an MCP tool cannot call directly — one in
 * {@code TestDataLibDAO.readByNameBySystemByEnvironmentByCountry}, the other in
 * {@code DataLibService.getSubDataFromType} — and both are invisible in the stored data, so
 * reporting a library without them would be actively misleading.</p>
 */
public final class TestDataLibMappingUtils {

    private TestDataLibMappingUtils() {
    }

    /**
     * Picks the variant the engine would use for a given context.
     *
     * <p>Mirrors the DAO query: a variant is eligible when its system, environment and country
     * each either equal the requested value or are empty, an empty field acting as a wildcard.
     * Among the eligible ones the DAO orders by system, environment then country descending and
     * takes the first, which selects the most specific variant, ties broken by the lowest id.</p>
     *
     * <p>A blank requested value matches only wildcard variants, exactly as the query does when
     * the caller passes an empty string.</p>
     *
     * @param variants    every variant sharing the library name.
     * @param system      the system to resolve for, possibly blank.
     * @param environment the environment to resolve for, possibly blank.
     * @param country     the country to resolve for, possibly blank.
     * @return the variant the engine would select, or {@code null} when none is eligible.
     */
    public static TestDataLib resolveVariant(List<TestDataLib> variants,
                                             String system, String environment, String country) {
        if (variants == null) {
            return null;
        }

        return variants.stream()
                .filter(lib -> matches(lib.getSystem(), system)
                        && matches(lib.getEnvironment(), environment)
                        && matches(lib.getCountry(), country))
                // Descending on the three qualifiers puts a concrete value ahead of the empty
                // wildcard, so the most specific variant wins; the id ascending breaks ties.
                .sorted(Comparator
                        .comparing((TestDataLib lib) -> MCPToolUtils.nullSafe(lib.getSystem())).reversed()
                        .thenComparing(Comparator.comparing((TestDataLib lib) -> MCPToolUtils.nullSafe(lib.getEnvironment())).reversed())
                        .thenComparing(Comparator.comparing((TestDataLib lib) -> MCPToolUtils.nullSafe(lib.getCountry())).reversed())
                        .thenComparing(lib -> lib.getTestDataLibID() == null ? Integer.MAX_VALUE : lib.getTestDataLibID()))
                .findFirst()
                .orElse(null);
    }

    /**
     * A qualifier matches when it equals the requested value or is empty, the empty value being
     * the wildcard the SQL {@code (field = ? or field = '')} clause allows.
     */
    private static boolean matches(String libValue, String requested) {
        String actual = MCPToolUtils.nullSafe(libValue);
        return actual.isEmpty() || actual.equalsIgnoreCase(MCPToolUtils.nullSafe(requested));
    }

    /**
     * Names the sub-data column the engine reads for this library's type.
     *
     * <p>Mirrors {@code DataLibService}: a FILE library is parsed as CSV and keyed by column
     * position, an SQL library by column name, and a SERVICE library by a parsing expression when
     * the response is XML or JSON. INTERNAL libraries carry their data in the entries themselves
     * and use no mapping at all.</p>
     *
     * @param lib the library.
     * @return the entity field name the engine will read.
     */
    public static String mappingFieldFor(TestDataLib lib) {
        String type = MCPToolUtils.nullSafe(lib == null ? null : lib.getType());

        return switch (type) {
            case TestDataLib.TYPE_SQL -> "column";
            case TestDataLib.TYPE_FILE -> "columnPosition";
            // A SERVICE library is read as XML/JSON when the service answers with either content
            // type, and as CSV otherwise. The content type is only known at execution time, so
            // parsingAnswer is named as the usual case rather than guessed at.
            case TestDataLib.TYPE_SERVICE -> "parsingAnswer";
            // INTERNAL entries hold their value directly.
            default -> "value";
        };
    }

    /**
     * A sentence an agent can act on, explaining why that field and not another.
     */
    public static String mappingExplanationFor(TestDataLib lib) {
        String type = MCPToolUtils.nullSafe(lib == null ? null : lib.getType());

        return switch (type) {
            case TestDataLib.TYPE_SQL ->
                    "This is an SQL library, so each entry maps to a column name returned by the query.";
            case TestDataLib.TYPE_FILE ->
                    "This is a FILE library, parsed as CSV, so each entry maps to a column position.";
            case TestDataLib.TYPE_SERVICE ->
                    "This is a SERVICE library. When the service answers in XML or JSON each entry maps to a "
                    + "parsing expression (parsingAnswer); when it answers with anything else the response is "
                    + "read as CSV and columnPosition is used instead. The content type is only known at "
                    + "execution time, so check both if the entry looks correct.";
            case TestDataLib.TYPE_INTERNAL ->
                    "This is an INTERNAL library: entries carry their value directly, with no column mapping.";
            default ->
                    "Unknown library type '" + type + "'; entries are reported with all their mapping fields.";
        };
    }

    /**
     * Returns the mapping value of one entry for the given field, never {@code null}.
     *
     * @param entry        the sub-data entry.
     * @param mappingField the field named by {@link #mappingFieldFor(TestDataLib)}.
     * @return the value the engine would read, or an empty string when the entry carries none.
     */
    public static String mappingValue(TestDataLibData entry, String mappingField) {
        if (entry == null) {
            return "";
        }

        return switch (MCPToolUtils.nullSafe(mappingField)) {
            case "column" -> MCPToolUtils.nullSafe(entry.getColumn());
            case "columnPosition" -> MCPToolUtils.nullSafe(entry.getColumnPosition());
            case "parsingAnswer" -> MCPToolUtils.nullSafe(entry.getParsingAnswer());
            default -> MCPToolUtils.nullSafe(entry.getValue());
        };
    }
}
