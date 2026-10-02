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
package org.cerberus.core.mcpdelta.run;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cerberus.core.mcpdelta.Context;
import org.cerberus.core.mcpdelta.db.Db;
import org.cerberus.core.mcpdelta.db.Db.Row;
import org.cerberus.core.mcpdelta.doc.DocRenderer;
import org.cerberus.core.mcpdelta.doc.RowMapper;
import org.cerberus.core.mcpdelta.doc.TcDoc;
import org.cerberus.core.mcpdelta.read.ExecutionReader;
import org.cerberus.core.mcpdelta.store.Aggregate;
import org.cerberus.core.mcpdelta.store.AggregateStore;
import org.cerberus.core.mcpdelta.store.DbResolver;
import org.cerberus.core.mcpdelta.store.Index;
import org.cerberus.core.mcpdelta.store.Table;
import org.cerberus.core.mcpdelta.tools.Tool;
import org.cerberus.core.mcpdelta.util.Text;

import java.net.CookieManager;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cerberus' debug mode, driven from Delta: an execution that pauses before each action, advanced one or
 * several actions at a time. After each move the answer gives what ran, what comes next and an outline of
 * the page the browser now shows — what a test author needs to find selectors on a site never seen before.
 */
public final class DebugService {

    private record Session(String uuid, long exeId, String test, String testcase, int reported) {
    }

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final int MAX_STEPS = 30;

    private final Context ctx;
    private final ExecutionReader reader;
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

    public DebugService(Context ctx, ExecutionReader reader) {
        this.ctx = ctx;
        this.reader = reader;
    }

    /** Starts a debug execution of one testcase and waits until it pauses before its first action. */
    public String start(Index.Tc tc, String country, String env, String robot, String tag) {
        String body;
        try {
            body = JSON.writeValueAsString(Map.of("test", tc.test(), "testCase", tc.testcase(), "country", country,
                    "environment", env, "robot", robot, "tag", Text.isBlank(tag) ? "delta-debug" : tag));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        JsonNode data = call("POST", "", body);
        String uuid = data.path("executionUUID").asText();
        long exeId = data.path("executionId").asLong();
        sessions.put(uuid, new Session(uuid, exeId, tc.test(), tc.testcase(), 0));
        JsonNode status = waitPaused(uuid, 30_000);
        // The short execution number is the handle: a model copies "#703" reliably, a 36-character UUID not always.
        return "debug session #" + exeId + " · " + tc.ref() + " · " + env + "/" + country + " · " + robot + "\n"
                + describe(sessions.get(uuid), status, false)
                + "\nadvance with run {debug:\"#" + exeId + "\", do:\"next\", count:n}; stop with do:\"stop\"";
    }

    /** next (count actions, stopping at the first failure), retry (the failed action, re-read from the database), stop, status. */
    public String step(String id, String action, int count) {
        Session s = find(id);
        String act = Text.isBlank(action) ? "next" : action.trim().toLowerCase();
        JsonNode status;
        try {
            status = statusOf(s.uuid);
        } catch (Tool.ToolError e) {
            // Cerberus forgets a debug execution once it has ended: the session is over, its result stays readable.
            sessions.remove(s.uuid);
            return "debug session #" + s.exeId + " has ended\n" + reader.execution(String.valueOf(s.exeId), false).stripTrailing();
        }
        if (act.equals("stop") && finished(status)) {
            sessions.remove(s.uuid);
            return "debug session #" + s.exeId + " had already ended\n" + reader.execution(String.valueOf(s.exeId), false).stripTrailing();
        }
        switch (act) {
            case "status" -> {
                lastPage.remove(s.uuid);
                return describe(s, status, true);
            }
            case "stop" -> {
                call("POST", s.uuid + "/stop", null);
                sessions.remove(s.uuid);
                return "debug session #" + s.exeId + " stopped\n" + reader.execution(String.valueOf(s.exeId), false).stripTrailing();
            }
            case "next", "retry" -> {
                int n = Math.max(1, Math.min(MAX_STEPS, count));
                // The whole answer must fit in one MCP call: no new action after 25 s, the last one waited for
                // at most until 37 s, the page read after. What did not run is one more call away.
                long t0 = System.currentTimeMillis();
                long launchUntil = t0 + Math.max(5, ctx.config.maxWaitSeconds - 20) * 1000L;
                long waitUntil = t0 + Math.max(10, ctx.config.maxWaitSeconds - 8) * 1000L;
                int done = 0;
                for (int i = 0; i < n && System.currentTimeMillis() < launchUntil; i++) {
                    if (finished(status)) {
                        break;
                    }
                    call("POST", s.uuid + "/" + (i == 0 ? act : "next"), null);
                    status = waitPaused(s.uuid, Math.max(500, waitUntil - System.currentTimeMillis()));
                    done++;
                    if (status.path("pendingFailed").asBoolean(false) || !status.path("state").asText("").startsWith("WAITING") && !finished(status)) {
                        break;
                    }
                }
                String out = describe(s, status, true);
                if (done < n && !finished(status) && !status.path("pendingFailed").asBoolean(false)) {
                    out += "\n(" + done + " of " + n + " moves made within this call" + (status.path("state").asText("").startsWith("WAITING")
                            ? "" : "; the last action is still running") + " — call do:\"next\" again to go on)";
                }
                return out;
            }
            default -> throw new Tool.ToolError("do is next, retry, stop or status");
        }
    }

    /** A session by its execution number ("#703"), its UUID, or its testcase ("Folder/TC", the latest one). */
    private Session find(String id) {
        String key = Text.nz(id).trim().replace("#", "");
        Session s = sessions.get(key);
        if (s == null) {
            for (Session x : sessions.values()) {
                if (String.valueOf(x.exeId).equals(key) || (x.test + "/" + x.testcase).equals(key) && (s == null || x.exeId > s.exeId)) {
                    s = x;
                }
            }
        }
        if (s == null) {
            String open = sessions.values().stream().map(x -> "#" + x.exeId + " " + x.test + "/" + x.testcase)
                    .reduce((a, b) -> a + ", " + b).orElse("none");
            throw new Tool.ToolError("no debug session " + id + " (open: " + open + "; start one with run {refs:[\"Folder/Testcase\"], debug:true})");
        }
        return s;
    }

    private static boolean finished(JsonNode status) {
        String state = status.path("state").asText("");
        return !state.isEmpty() && !state.startsWith("WAITING") && !state.equals("RUNNING") && !state.equals("STARTING");
    }

    private JsonNode waitPaused(String uuid, long maxMs) {
        long end = System.currentTimeMillis() + maxMs;
        JsonNode st = statusOf(uuid);
        while (System.currentTimeMillis() < end && !st.path("state").asText("").startsWith("WAITING") && !finished(st)) {
            try {
                Thread.sleep(400);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
            st = statusOf(uuid);
        }
        return st;
    }

    private JsonNode statusOf(String uuid) {
        return call("GET", uuid + "/status", null);
    }

    /** What ran since the last answer, what comes next, and the page as it is now. */
    private String describe(Session s, JsonNode status, boolean withPage) {
        StringBuilder sb = new StringBuilder();
        List<Row> ran = ctx.db.read(c -> Db.query(c, "SELECT 'A' k, Step, Sequence, 0 ControlSequence, Action name, Value1, Value2, ReturnCode, ReturnMessage, Start"
                + " FROM testcasestepactionexecution WHERE ID=? UNION ALL SELECT 'C', Step, Sequence, ControlSequence, Control, Value1, Value2,"
                + " ReturnCode, ReturnMessage, Start FROM testcasestepactioncontrolexecution WHERE ID=? ORDER BY Start, Step, Sequence, ControlSequence",
                s.exeId, s.exeId));
        if (ran.size() > s.reported) {
            sb.append("ran:\n");
            for (Row r : ran.subList(s.reported, ran.size())) {
                sb.append("  ").append(String.format("%-3s", r.s("ReturnCode"))).append(" step ").append(r.s("Step")).append(" · ")
                        .append(r.s("k").equals("C") ? "  " : "").append(r.s("name"));
                if (!r.s("Value1").isEmpty()) {
                    sb.append(' ').append(Text.quote(Text.truncate(r.s("Value1"), 80)));
                }
                if (!r.s("Value2").isEmpty()) {
                    sb.append(' ').append(Text.quote(Text.truncate(r.s("Value2"), 60)));
                }
                if (!"OK".equals(r.s("ReturnCode"))) {
                    sb.append(" — ").append(Text.truncate(Text.oneLine(r.s("ReturnMessage")), 200));
                }
                sb.append('\n');
            }
            sessions.put(s.uuid, new Session(s.uuid, s.exeId, s.test, s.testcase, ran.size()));
        }
        if (finished(status)) {
            Row exe = ctx.db.read(c -> Db.one(c, "SELECT ControlStatus, ControlMessage FROM testcaseexecution WHERE ID=?", s.exeId));
            sb.append("finished: ").append(exe == null ? status.path("controlStatus").asText("?") : exe.s("ControlStatus") + " — "
                    + Text.truncate(Text.oneLine(exe.s("ControlMessage")), 160)).append('\n');
        } else {
            sb.append(status.path("pendingFailed").asBoolean(false) ? "failed, paused on: " : "paused before: ")
                    .append(pending(s, status)).append('\n');
            if (status.path("pendingFailed").asBoolean(false)) {
                sb.append("fix the testcase with write, then run {debug:\"#").append(s.exeId)
                        .append("\", do:\"retry\"} re-runs it as now stored; or do:\"next\" to go on\n");
            }
        }
        if (withPage && !finished(status)) {
            String live = livePage(s.exeId);
            if (live != null) {
                return (sb + pageChange(s.uuid, live.substring(5))).stripTrailing();
            }
        }
        if (withPage) {
            String page = reader.page(String.valueOf(s.exeId));
            if (!page.startsWith("!!")) {
                sb.append(page.substring(page.indexOf('\n') + 1).isBlank() ? "" : "page now:\n" + page.substring(page.indexOf('\n') + 1));
            }
        }
        return sb.toString().stripTrailing();
    }

    /**
     * The screen the execution shows now, read on the robot that runs it (its host and credentials as
     * Cerberus recorded them), else on the configured grid, the host-side address of a local robot.
     */
    private String livePage(long exeId) {
        Row exe = ctx.db.read(c -> Db.one(c, "SELECT e.RobotSessionId, e.RobotHost, e.RobotPort, x.HostUser, x.HostPassword"
                + " FROM testcaseexecution e LEFT JOIN robotexecutor x ON x.robot=e.robot AND x.executor=e.robotexecutor WHERE e.ID=?", exeId));
        if (exe == null || exe.s("RobotSessionId").isEmpty()) {
            return null;
        }
        String session = exe.s("RobotSessionId");
        if (!exe.s("RobotHost").isEmpty()) {
            String port = exe.s("RobotPort");
            String host = exe.s("RobotHost").contains("://") ? exe.s("RobotHost") : ("443".equals(port) ? "https://" : "http://") + exe.s("RobotHost");
            String url = host + (port.isEmpty() || host.matches(".*:\\d+/?$") ? "" : ":" + port);
            String auth = exe.s("HostUser").isEmpty() ? null : exe.s("HostUser") + ":" + exe.s("HostPassword");
            String page = new LiveOutline(url, auth).current(session, 9000);
            if (page != null) {
                return page;
            }
        }
        return ctx.config.gridUrl.isBlank() ? null : new LiveOutline(ctx.config.gridUrl).current(session, 9000);
    }

    /** Last page shown per session: a move that leaves the page as it was costs one line, not the whole outline. */
    private final Map<String, String> lastPage = new ConcurrentHashMap<>();

    private String pageChange(String uuid, String outline) {
        String before = lastPage.put(uuid, outline);
        String url = outline.substring(0, outline.indexOf('\n') < 0 ? outline.length() : outline.indexOf('\n'));
        if (before == null || !before.startsWith(url.substring(0, Math.max(0, url.indexOf(" · ") < 0 ? url.length() : url.indexOf(" · "))))) {
            return "page now — " + outline;
        }
        if (before.equals(outline)) {
            return "page unchanged — " + url + "\n";
        }
        StringBuilder sb = new StringBuilder("page changed — " + url + " (only what differs; read live or do:status for all)\n");
        for (Text.DiffLine d : Text.diffLines(before, outline)) {
            if (d.kind() != ' ' && !d.text().isBlank()) {
                sb.append(d.kind()).append(' ').append(d.text().strip()).append('\n');
            }
        }
        return sb.toString();
    }

    /** The pending action as its document line, so the model sees exactly what will run. */
    private String pending(Session s, JsonNode status) {
        int stepId = status.path("pendingStepId").asInt(-1);
        int actionId = status.path("pendingActionId").asInt(-1);
        int controlId = status.path("pendingControlId").asInt(-1);
        String found = ctx.db.read(c -> {
            Aggregate a = AggregateStore.load(c, s.test, s.testcase, false);
            if (a == null) {
                return null;
            }
            DbResolver resolver = new DbResolver(c, ctx.catalog);
            TcDoc doc = RowMapper.toDoc(a, resolver, false);
            int stepPos = 0;
            for (Row st : a.get(Table.STEP)) {
                stepPos++;
                if (st.i("StepId") != stepId) {
                    continue;
                }
                TcDoc.Step step = doc.steps.get(stepPos - 1);
                int actPos = 0;
                for (Row ar : a.get(Table.ACTION)) {
                    if (ar.i("StepId") != stepId) {
                        continue;
                    }
                    actPos++;
                    if (ar.i("ActionId") != actionId) {
                        continue;
                    }
                    TcDoc.Item it = step.actions.get(actPos - 1);
                    if (controlId > 0) {
                        int cPos = 0;
                        for (Row cr : a.get(Table.CONTROL)) {
                            if (cr.i("StepId") == stepId && cr.i("ActionId") == actionId) {
                                cPos++;
                                if (cr.i("ControlId") == controlId) {
                                    return "step " + stepPos + " · " + DocRenderer.item(it) + " → control " + DocRenderer.item(it.controls.get(cPos - 1));
                                }
                            }
                        }
                    }
                    return "step " + stepPos + " · " + DocRenderer.item(it);
                }
                return "step " + stepPos + " (library step " + Text.nz(step.flags.get("use")) + ")";
            }
            return null;
        });
        if (found != null) {
            return found;
        }
        String d = status.path("pendingActionDescription").asText("");
        return d.isEmpty() ? "step " + stepId + " action " + actionId : d;
    }

    // ---------------------------------------------------------------- HTTP

    private JsonNode call(String method, String path, String body) {
        return ctx.gateway.debug(method, path, body);
    }

}
