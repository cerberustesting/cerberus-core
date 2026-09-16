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

import java.util.List;
import java.util.Map;

/**
 * Bounds what a listing tool returns, and says what was left out.
 *
 * <p>An unbounded listing is not merely verbose. A tool result larger than the calling agent's
 * limit — 25 000 tokens in Claude Code — is not truncated: it is written to a file and replaced by
 * an error, so the call the agent made returns nothing usable at all. A listing that behaves
 * perfectly on a demo instance therefore fails outright on a real one, which is the worst place to
 * discover it.</p>
 *
 * <p>The answer is a window, never a silent cut. Every paged response carries the total, the window
 * it returned, and whether more exists — so a caller can tell "these are all of them" from "these
 * are the first fifty", which is the distinction a silent truncation destroys.</p>
 */
public final class MCPPagination {

    /** Ceiling applied to every listing, whatever it asks for. */
    public static final int HARD_MAX = 500;

    private MCPPagination() {
    }

    /**
     * The window a caller asked for.
     *
     * @param offset how many matches to skip, from the start of the ordered result.
     * @param limit  how many to return.
     */
    public record Window(int offset, int limit) {
    }

    /**
     * Declares {@code limit} and {@code offset} on a tool's input schema.
     *
     * @param properties   the tool's property map, added to in place.
     * @param defaultLimit what the tool returns when the caller says nothing.
     * @param what         plural noun for the listed thing, used in the descriptions.
     */
    public static void declare(Map<String, Object> properties, int defaultLimit, String what) {
        properties.put("limit", Map.of(
                "type", "integer",
                "description", "How many " + what + " to return. Defaults to " + defaultLimit
                        + ", maximum " + HARD_MAX + ". The answer always reports the total and whether "
                        + "more remain."
        ));
        properties.put("offset", Map.of(
                "type", "integer",
                "description", "How many " + what + " to skip before returning any, for paging through a "
                        + "long list. Defaults to 0. Prefer narrowing the search to paging blindly."
        ));
    }

    /**
     * Reads the window from the tool arguments, clamped to something a caller can actually receive.
     */
    public static Window of(Map<String, Object> args, int defaultLimit) {
        int limit = Math.min(Math.max(MCPToolUtils.getInteger(args, "limit", defaultLimit), 1), HARD_MAX);
        int offset = Math.max(MCPToolUtils.getInteger(args, "offset", 0), 0);
        return new Window(offset, limit);
    }

    /**
     * Cuts one window out of the full ordered result.
     *
     * @return the elements inside the window, empty when the offset is past the end.
     */
    public static <T> List<T> slice(List<T> all, Window window) {
        if (all == null || all.isEmpty() || window.offset() >= all.size()) {
            return List.of();
        }
        int to = Math.min(window.offset() + window.limit(), all.size());
        return all.subList(window.offset(), to);
    }

    /**
     * Writes the window's own description into the response.
     *
     * <p>Always present, whether or not anything was cut: "50 of 50" is the answer that lets a
     * caller stop looking, and it can only be trusted if the same fields appear when there is more.
     * The message is only added when something was actually left out, and it says how to get it.</p>
     *
     * @param response the response map, added to in place.
     * @param window   the window that was applied.
     * @param total    how many matched in all.
     * @param returned how many are in this response.
     * @param what     plural noun for the listed thing.
     */
    public static void describe(Map<String, Object> response, Window window, int total, int returned, String what) {
        response.put("total", total);
        response.put("offset", window.offset());
        boolean hasMore = window.offset() + returned < total;
        response.put("hasMore", hasMore);
        if (hasMore) {
            response.put("message", "Showing " + what + " " + (window.offset() + 1) + " to "
                    + (window.offset() + returned) + " of " + total + ". Narrow the search, or pass offset="
                    + (window.offset() + returned) + " for the next page. Asking for everything at once can "
                    + "exceed what a tool result is allowed to carry, which fails the call outright.");
        }
    }
}
