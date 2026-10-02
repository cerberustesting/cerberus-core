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
import org.cerberus.core.mcpdelta.write.WriteAnswer;
import org.cerberus.core.mcpdelta.write.WriteService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class WriteTool implements Tool {

    private final WriteService service;
    private final RunService runs;

    public WriteTool(WriteService service, RunService runs) {
        this.service = service;
        this.runs = runs;
    }

    @Override
    public String name() {
        return "write";
    }

    @Override
    public String description() {
        return "Change testcases and any Cerberus object (application, service, campaign, robot, environment, folder, invariant, labels, "
                + "datalib, context) in one atomic, undoable transaction. docs: whole documents (create or replace). "
                + "edits: [{ref, old, new}] text edits on the document as read; old must be unique (all:true for every occurrence). "
                + "replace: [{find, with, in: scope}] rules over many testcases. delete: refs (Folder/Testcase or kind:key). "
                + "Answer: the resulting diff and a delta id. Any error is listed with its fix and nothing is written. "
                + "dryRun:true simulates; plan:\"pN\" applies a simulated plan. "
                + "run:{env, country?, robot?, refs?} then runs (by default the testcases just written) and adds the verdicts.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("docs", Map.of("type", "array", "items", Map.of("type", "string"), "description", "Full testcase documents."));
        props.put("edits", Map.of("type", "array", "items", Map.of("type", "object", "properties", Map.of(
                "ref", Map.of("type", "string"),
                "old", Map.of("type", "string"),
                "new", Map.of("type", "string"),
                "all", Map.of("type", "boolean")), "required", List.of("ref", "old", "new"))));
        props.put("replace", Map.of("type", "array", "description", "Find/replace rules on values, applied in order.", "items", Map.of("type", "object", "properties", Map.of(
                "find", Map.of("type", "string"),
                "with", Map.of("type", "string"),
                "in", Map.of("type", "string", "description", "Folder | Folder/Testcase | Folder/PREFIX-* | app:X | label:X | *"),
                "regex", Map.of("type", "boolean"),
                "ignoreCase", Map.of("type", "boolean"),
                "fields", Map.of("type", "array", "items", Map.of("type", "string", "enum", List.of("values", "properties", "descriptions", "all")))),
                "required", List.of("find", "with", "in"))));
        props.put("delete", Map.of("type", "array", "items", Map.of("type", "string")));
        props.put("dryRun", Map.of("type", "boolean"));
        props.put("plan", Map.of("type", "string"));
        props.put("intent", Map.of("type", "string", "description", "Why, in a few words (kept in the journal)."));
        props.put("run", Map.of("type", "object", "description", "After applying, run on a robot (same fields as the run tool; refs default to the testcases written).",
                "properties", Map.of("refs", Map.of("type", "array", "items", Map.of("type", "string")), "country", Map.of("type", "string"),
                        "env", Map.of("type", "string"), "robot", Map.of("type", "string"))));
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("properties", props);
        return schema;
    }

    @Override
    public String call(JsonNode args) {
        WriteService.Request req = new WriteService.Request();
        req.docs = Args.list(args, "docs");
        String single = Args.str(args, "doc", null);
        if (single != null) {
            req.docs.add(single);
        }
        for (JsonNode e : Args.objects(args, "edits")) {
            WriteService.Edit edit = new WriteService.Edit();
            edit.ref = Args.str(e, "ref", "");
            edit.oldText = Args.str(e, "old", Args.str(e, "old_string", ""));
            edit.newText = Args.str(e, "new", Args.str(e, "new_string", ""));
            edit.all = Args.bool(e, "all", false);
            req.edits.add(edit);
        }
        for (JsonNode r : Args.objects(args, "replace")) {
            WriteService.Replace rep = new WriteService.Replace();
            rep.find = Args.str(r, "find", "");
            rep.with = Args.str(r, "with", "");
            rep.in = Args.str(r, "in", "");
            rep.regex = Args.bool(r, "regex", false);
            rep.ignoreCase = Args.bool(r, "ignoreCase", false);
            rep.fields = Args.list(r, "fields");
            req.replace.add(rep);
        }
        req.delete = Args.list(args, "delete");
        req.dryRun = Args.bool(args, "dryRun", false);
        req.plan = Args.str(args, "plan", null);
        req.intent = Args.str(args, "intent", null);
        WriteService.Outcome outcome = service.write(req);
        String answer = WriteAnswer.format(outcome);
        JsonNode run = Args.object(args, "run");
        if (run == null || run.isBoolean() && !run.asBoolean()) {
            return answer;
        }
        if (!outcome.committed && outcome.changes.stream().anyMatch(c -> c.stats != null && !c.stats.isEmpty())) {
            return answer + "\n\nrun skipped: nothing was applied yet";
        }
        List<String> refs = run.isObject() ? Args.list(run, "refs") : new java.util.ArrayList<>();
        if (refs.isEmpty()) {
            // The testcases written; an object written alongside (folder, application...) is not something to run.
            for (WriteService.Change c : outcome.changes) {
                if (c.entity == null && !"deleted".equals(c.kind)) {
                    refs.add(c.ref);
                }
            }
        }
        if (refs.isEmpty()) {
            return answer + "\n\nrun skipped: no testcase to run";
        }
        JsonNode r = run.isObject() ? run : com.fasterxml.jackson.databind.node.JsonNodeFactory.instance.objectNode();
        try {
            return answer + "\n\n" + runs.run(refs, Args.str(r, "country", null), Args.str(r, "env", Args.str(r, "environment", null)),
                    Args.str(r, "robot", null), Args.integer(r, "wait", 45), null, Args.bool(r, "force", false));
        } catch (ToolError e) {
            return answer + "\n\nrun refused (the write is applied): " + e.getMessage();
        }
    }
}
