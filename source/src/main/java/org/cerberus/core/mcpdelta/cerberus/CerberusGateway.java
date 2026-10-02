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
package org.cerberus.core.mcpdelta.cerberus;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;

/**
 * Everything MCP Delta asks Cerberus to do besides reading and writing its database: queue executions, drive
 * the debug mode, purge caches, wake the queue job. Two implementations: over Cerberus' public HTTP API (the
 * standalone server) and in-process through Cerberus' own services (inside the Cerberus webapp, on behalf of
 * the authenticated user).
 */
public interface CerberusGateway {

    /** Execution options as the run tool names them, and the queue API parameter each one becomes. */
    Map<String, String> OPTION_PARAMS = Map.ofEntries(
            Map.entry("screenshot", "screenshot"), Map.entry("video", "video"), Map.entry("verbose", "verbose"),
            Map.entry("pageSource", "pagesource"), Map.entry("robotLog", "seleniumlog"), Map.entry("consoleLog", "consolelog"),
            Map.entry("timeout", "timeout"), Map.entry("retries", "retries"), Map.entry("priority", "priority"),
            Map.entry("manualExecution", "manualexecution"), Map.entry("manualUrl", "manualurl"), Map.entry("manualHost", "myhost"),
            Map.entry("manualContextRoot", "mycontextroot"), Map.entry("manualLoginRelativeUrl", "myloginrelativeurl"),
            Map.entry("manualEnvData", "myenvdata"), Map.entry("executor", "executor"));

    /** What to queue: testcases ({test, testcase}) or a campaign, on every country × environment × robot given. */
    record Order(List<String[]> testcases, List<String> countries, List<String> environments, List<String> robots, String tag,
                 Map<String, String> options, String campaign) {
    }

    /** What was queued. */
    record Queued(int count, String tag) {
    }

    /** Queues the executions; throws Tool.ToolError with Cerberus' own message when nothing could be queued. */
    Queued enqueue(Order order);

    /** The debug execution API: a path under debugexecutions/ ("" to start, "<uuid>/next"...), answering its "data". */
    JsonNode debug(String method, String path, String body);

    /** Drops Cerberus' caches (parameters, invariants...) so a change is seen at once; returns a short status. */
    String purgeCache();

    /** Wakes the queue job up, as Cerberus does after resuming a paused campaign; returns a short status. */
    String runQueueJob();
}
