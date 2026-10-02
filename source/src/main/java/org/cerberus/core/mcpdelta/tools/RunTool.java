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
package org.cerberus.core.mcpdelta.tools;

import com.fasterxml.jackson.databind.JsonNode;
import org.cerberus.core.mcpdelta.run.RunService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class RunTool implements Tool {

    private final RunService service;

    public RunTool(RunService service) {
        this.service = service;
    }

    @Override
    public String name() {
        return "run";
    }

    @Override
    public String description() {
        return "Run testcases on a robot and wait for the verdicts. refs = testcases, scopes, or one campaign (\"campaign:NAME\"). "
                + "country/env/robot may be lists; they are checked (see read targets), and one left out is chosen only when a single value "
                + "is valid. Failures come grouped by cause, with the failing line, the real values, the message and the closest elements "
                + "found in the page the robot saved. A call waits at most 45 s; run {tag} waits for the rest; action cancel|pause|resume "
                + "acts on a tag. A testcase that already passed in this session and has not changed since is not run again (force:true runs it). debug:true on one testcase starts Cerberus' debug mode (paused before each action): then "
                + "run {debug:<session>, do:next|retry|stop|status, count} advances and shows what ran and the page now displayed.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        Map<String, Object> props = new LinkedHashMap<>();
        Map<String, Object> stringOrList = Map.of("anyOf", List.of(Map.of("type", "string"), Map.of("type", "array", "items", Map.of("type", "string"))));
        props.put("refs", Map.of("type", "array", "items", Map.of("type", "string")));
        props.put("country", stringOrList);
        props.put("env", stringOrList);
        props.put("robot", stringOrList);
        props.put("wait", Map.of("type", "integer", "description", "Seconds to wait, at most 45 per call (default 45, 0 = do not wait)."));
        props.put("tag", Map.of("type", "string", "description", "Without refs: keep waiting for this run, or the tag to cancel/pause/resume."));
        props.put("action", Map.of("type", "string", "enum", List.of("cancel", "pause", "resume")));
        props.put("options", Map.of("type", "object", "description", "screenshot, video, verbose, pageSource, robotLog, consoleLog (0/1/2), "
                + "timeout, retries, priority, manualExecution, manualUrl, manualHost, manualContextRoot, manualLoginRelativeUrl, manualEnvData, executor."));
        props.put("debug", Map.of("anyOf", List.of(Map.of("type", "boolean"), Map.of("type", "string")),
                "description", "true with one testcase in refs: start debug mode. A session id: act on that session."));
        props.put("do", Map.of("type", "string", "enum", List.of("next", "retry", "stop", "status")));
        props.put("count", Map.of("type", "integer", "description", "Actions to advance with do:next (stops at the first failure)."));
        props.put("force", Map.of("type", "boolean", "description", "Rarely needed: a new or changed testcase always runs. force only re-runs "
                + "a testcase that already passed in this session and has not changed since (to check flakiness)."));
        return Map.of("type", "object", "properties", props);
    }

    @Override
    public String call(JsonNode args) {
        // Waiting on a tag only reads; queueing, debugging and acting on a tag run tests.
        boolean onlyWait = !args.hasNonNull("refs") && !args.hasNonNull("ref") && !args.hasNonNull("debug") && !args.hasNonNull("action");
        org.cerberus.core.mcpdelta.util.Access.require(onlyWait ? org.cerberus.core.mcpdelta.util.Access.READ : org.cerberus.core.mcpdelta.util.Access.RUN,
                onlyWait ? "read executions" : "run tests");
        JsonNode dbg = args.get("debug");
        if (dbg != null && dbg.isTextual() && !dbg.asText().isBlank() && !"true".equalsIgnoreCase(dbg.asText())) {
            return service.stepDebug(dbg.asText(), Args.str(args, "do", "next"), Args.integer(args, "count", 1));
        }
        List<String> refs = Args.list(args, "refs");
        refs.addAll(Args.list(args, "ref"));
        Map<String, String> options = new LinkedHashMap<>();
        JsonNode o = Args.object(args, "options");
        if (o != null) {
            o.fields().forEachRemaining(e -> options.put(e.getKey(), e.getValue().isTextual() ? e.getValue().asText() : e.getValue().toString()));
        }
        RunService.Request r = new RunService.Request(refs, listOf(args, "country"), listOf(args, "env", "environment"),
                listOf(args, "robot"), Args.integer(args, "wait", 45), Args.str(args, "tag", null), options, Args.str(args, "action", null),
                Args.bool(args, "force", false));
        if (dbg != null && Args.bool(args, "debug", false)) {
            return service.startDebug(r);
        }
        return service.run(r);
    }

    private static List<String> listOf(JsonNode args, String... keys) {
        List<String> out = new java.util.ArrayList<>();
        for (String k : keys) {
            for (String v : Args.list(args, k)) {
                for (String part : v.split(",")) {
                    if (!part.isBlank()) {
                        out.add(part.trim());
                    }
                }
            }
        }
        return out;
    }
}
