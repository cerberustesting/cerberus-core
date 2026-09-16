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

import org.cerberus.core.engine.execution.impl.RobotServerService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads and writes the per-action robot overrides Cerberus stores in {@code options} and
 * {@code conditionOptions}.
 *
 * <p>These four settings decide how long the robot waits before declaring an element absent, and
 * they are the only way to stop a condition that never matches from costing its full timeout on
 * every single run — the default {@code cerberus_selenium_wait_element} is 45 seconds, so a
 * {@code ifElementVisible} guarding a popup that rarely appears silently spends that on every
 * execution.</p>
 *
 * <p>Both columns hold the same shape: a JSON array of {@code {"option": …, "value": …,
 * "act": …}}, always the same four entries in the same order, with {@code act} deciding whether
 * the override applies. That shape is an artefact of the editor's four checkbox-and-field rows,
 * and asking a tool caller to reproduce it exactly — including the entries it does not care
 * about — would be a trap. Callers name the settings they want instead, and this class folds
 * them into the stored form the engine and the editor both expect.</p>
 */
public final class MCPActionOptions {

    /** The four settings, in the order the editor writes them. */
    private static final List<String> OPTION_NAMES = List.of(
            RobotServerService.OPTIONS_TIMEOUT_SYNTAX,
            RobotServerService.OPTIONS_MINSIMILARITY_SYNTAX,
            RobotServerService.OPTIONS_HIGHLIGHTELEMENT_SYNTAX,
            RobotServerService.OPTIONS_TYPEDELAY_SYNTAX
    );

    /**
     * The settings the engine reads back with {@code Integer.valueOf}, unguarded — a non-numeric
     * value stored here does not fail at save time, it fails mid-execution.
     */
    private static final List<String> INTEGER_OPTIONS = List.of(
            RobotServerService.OPTIONS_TIMEOUT_SYNTAX,
            RobotServerService.OPTIONS_HIGHLIGHTELEMENT_SYNTAX
    );

    private MCPActionOptions() {
    }

    /** Raised when a caller-supplied override cannot be stored as it stands. */
    public static class InvalidOptionException extends RuntimeException {
        public InvalidOptionException(String message) {
            super(message);
        }
    }

    /**
     * Describes the four settings as a tool input schema.
     *
     * @param what a phrase naming what the settings apply to, used in the description.
     */
    public static Map<String, Object> schema(String what) {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put(RobotServerService.OPTIONS_TIMEOUT_SYNTAX, Map.of(
                "type", "string",
                "description", "Milliseconds the robot waits for an element before giving up, for " + what
                        + " only. This is the setting that removes dead waiting time from a check that "
                        + "rarely matches: without it the run spends the system default "
                        + "(cerberus_selenium_wait_element, 45000 ms out of the box) every single time. "
                        + "Empty string removes the override and restores the default."
        ));
        properties.put(RobotServerService.OPTIONS_MINSIMILARITY_SYNTAX, Map.of(
                "type", "string",
                "description", "Sikuli image-matching threshold for " + what
                        + " (image-based robots only). Empty string removes the override."
        ));
        properties.put(RobotServerService.OPTIONS_HIGHLIGHTELEMENT_SYNTAX, Map.of(
                "type", "string",
                "description", "Milliseconds the matched element stays highlighted for " + what
                        + ", for debugging. Empty string removes the override."
        ));
        properties.put(RobotServerService.OPTIONS_TYPEDELAY_SYNTAX, Map.of(
                "type", "string",
                "description", "Delay between simulated keystrokes for " + what
                        + " (image-based robots only). Empty string removes the override."
        ));

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("description", "Robot overrides applied to " + what
                + ". Name only the settings you want to change; the others keep their current value.");
        schema.put("properties", properties);
        schema.put("additionalProperties", false);
        return schema;
    }

    /**
     * Folds caller-supplied settings into the stored array.
     *
     * <p>A setting given a value is turned on, a setting given an empty string is turned off, and a
     * setting not mentioned keeps whatever it had. The result always carries all four entries in
     * the canonical order, so the editor reads back what the tool wrote.</p>
     *
     * @param existing  the array currently stored, possibly {@code null} or incomplete.
     * @param requested the settings the caller named.
     * @return the array to store.
     * @throws InvalidOptionException when a setting is not one of the four, or when a numeric
     *                                setting is given something the engine could not parse.
     */
    public static JSONArray merge(JSONArray existing, Map<String, Object> requested) {
        Map<String, JSONObject> current = index(existing);

        for (Map.Entry<String, Object> entry : requested.entrySet()) {
            String name = entry.getKey();
            if (!OPTION_NAMES.contains(name)) {
                throw new InvalidOptionException("Unknown option '" + name + "'. Supported options: "
                        + String.join(", ", OPTION_NAMES) + ".");
            }

            String value = entry.getValue() == null ? "" : String.valueOf(entry.getValue()).trim();
            if (!value.isEmpty() && INTEGER_OPTIONS.contains(name)) {
                try {
                    Integer.valueOf(value);
                } catch (NumberFormatException e) {
                    throw new InvalidOptionException("Option '" + name + "' must be a whole number of "
                            + "milliseconds, got '" + value + "'. Cerberus reads this value back at "
                            + "execution time, so a non-numeric one fails the run, not this call.");
                }
            }

            JSONObject option = current.get(name);
            try {
                option.put("value", value);
                // An empty value and an active override would mean "wait zero" rather than "use the
                // default", so emptying a setting is what turns it off.
                option.put("act", !value.isEmpty());
            } catch (JSONException e) {
                throw new InvalidOptionException("Unable to store option '" + name + "': " + e.getMessage());
            }
        }

        JSONArray result = new JSONArray();
        for (String name : OPTION_NAMES) {
            result.put(current.get(name));
        }
        // Anything stored under a name this class does not know is carried through untouched. Only
        // four settings exist today, but rebuilding the array from that list alone would mean a
        // fifth one — added to the engine later, or written by a newer Cerberus — is deleted by the
        // first update made through these tools, silently and with no way to notice.
        if (existing != null) {
            for (int i = 0; i < existing.length(); i++) {
                JSONObject option = existing.optJSONObject(i);
                if (option != null && !OPTION_NAMES.contains(option.optString("option", ""))) {
                    result.put(option);
                }
            }
        }
        return result;
    }

    /**
     * Reports the settings that are actually in force, for a tool response.
     *
     * @return the active settings by name, empty when none override the defaults.
     */
    public static Map<String, String> active(JSONArray options) {
        Map<String, String> result = new LinkedHashMap<>();
        if (options == null) {
            return result;
        }
        for (int i = 0; i < options.length(); i++) {
            JSONObject option = options.optJSONObject(i);
            if (option != null && option.optBoolean("act", false)) {
                result.put(option.optString("option", ""), option.optString("value", ""));
            }
        }
        return result;
    }

    /**
     * Reads the stored array into one entry per known setting, filling in anything missing so a
     * row written before a setting existed — or a row never configured at all — still merges.
     */
    private static Map<String, JSONObject> index(JSONArray existing) {
        Map<String, JSONObject> current = new LinkedHashMap<>();
        for (String name : OPTION_NAMES) {
            JSONObject blank = new JSONObject();
            try {
                blank.put("option", name);
                blank.put("value", "");
                blank.put("act", false);
            } catch (JSONException e) {
                throw new InvalidOptionException("Unable to build option '" + name + "': " + e.getMessage());
            }
            current.put(name, blank);
        }

        if (existing == null) {
            return current;
        }
        for (int i = 0; i < existing.length(); i++) {
            JSONObject option = existing.optJSONObject(i);
            if (option == null) {
                continue;
            }
            String name = option.optString("option", "");
            if (current.containsKey(name)) {
                JSONObject kept = current.get(name);
                try {
                    kept.put("value", option.optString("value", ""));
                    kept.put("act", option.optBoolean("act", false));
                } catch (JSONException e) {
                    throw new InvalidOptionException("Unable to read option '" + name + "': " + e.getMessage());
                }
            }
        }
        return current;
    }
}
