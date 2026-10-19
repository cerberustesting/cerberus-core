/**
 * Cerberus Copyright (C) 2013 - 2026 cerberustesting
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
package org.cerberus.core.service.notifications.email.impl;

import java.util.List;
import java.util.Map;
import static java.util.Map.entry;

/**
 * One color per execution status, taken from the Tailwind palette, used by every email.
 */
public final class EmailStatusPalette {

    /**
     * Statuses in the order displayed (donut, legend).
     */
    public static final List<String> ORDER = List.of("OK", "KO", "FA", "NA", "NE", "WE", "PE", "QU", "QE", "PA", "CA");

    private static final Map<String, String> COLORS = Map.ofEntries(
            entry("OK", "#22c55e"), // green-500
            entry("KO", "#ef4444"), // red-500
            entry("FA", "#f97316"), // orange-500
            entry("NA", "#eab308"), // yellow-500
            entry("NE", "#cbd5e1"), // slate-300
            entry("WE", "#8b5cf6"), // violet-500
            entry("PE", "#3b82f6"), // blue-500
            entry("QU", "#38bdf8"), // sky-400
            entry("QE", "#be123c"), // rose-700
            entry("PA", "#f59e0b"), // amber-500
            entry("CA", "#64748b")); // slate-500

    private static final Map<String, String> LABELS = Map.ofEntries(
            entry("OK", "OK"), entry("KO", "KO"), entry("FA", "Failed"), entry("NA", "Not Applicable"),
            entry("NE", "Not Executed"), entry("WE", "Waiting Exe"), entry("PE", "Executing"), entry("QU", "Queued"),
            entry("QE", "Queue Error"), entry("PA", "Paused"), entry("CA", "Cancelled"));

    // Text is white on the dark colors, dark on the light ones.
    private static final List<String> LIGHT_BACKGROUNDS = List.of("NA", "NE", "QU", "PA");

    private static final String DEFAULT_COLOR = "#cbd5e1";

    private EmailStatusPalette() {
    }

    public static String color(String status) {
        return status == null ? DEFAULT_COLOR : COLORS.getOrDefault(status, DEFAULT_COLOR);
    }

    public static String textColor(String status) {
        return status == null || LIGHT_BACKGROUNDS.contains(status) || !COLORS.containsKey(status) ? "#1e2124" : "#ffffff";
    }

    public static String label(String status) {
        return status == null ? "" : LABELS.getOrDefault(status, status);
    }
}
