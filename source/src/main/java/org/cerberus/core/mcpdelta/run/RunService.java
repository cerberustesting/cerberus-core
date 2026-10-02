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
import org.cerberus.core.mcpdelta.read.ExecutionReader;
import org.cerberus.core.mcpdelta.store.Index;
import org.cerberus.core.mcpdelta.tools.Tool;
import org.cerberus.core.mcpdelta.util.Text;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Runs testcases on a robot through Cerberus' own public queue API, waits for them, and answers with the
 * verdicts. Country, environment and robot are checked first: an invalid combination is refused with the
 * valid ones, instead of queueing nothing and returning a tag that looks like a run.
 */
public final class RunService {

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final DateTimeFormatter TAG_TIME = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
    private static final int MAX_TESTCASES = 200;

    private final Context ctx;
    private final ExecutionReader reader;
    private DebugService debug;
    private final RunLedger ledger = new RunLedger();

    public RunService(Context ctx, ExecutionReader reader) {
        this.ctx = ctx;
        this.reader = reader;
        this.debug = new DebugService(ctx, reader);
    }

    /** Starts Cerberus' debug mode on one testcase, after the same checks as a run. */
    public String startDebug(Request r) {
        if (r.refs().size() != 1) {
            throw new Tool.ToolError("debug runs one testcase: refs [\"Folder/Testcase\"]");
        }
        String[] parts = Index.splitRef(r.refs().get(0));
        if (parts == null) {
            throw new Tool.ToolError("debug needs a testcase reference Folder/Testcase");
        }
        Index.Tc tc = ctx.db.read(c -> {
            List<Index.Tc> found = Index.resolve(c, r.refs().get(0));
            return found.isEmpty() ? null : found.get(0);
        });
        if (tc == null) {
            throw new Tool.ToolError(r.refs().get(0) + " does not exist");
        }
        String[] resolved = ctx.db.read(c -> validate(c, List.of(tc), r.countries().isEmpty() ? null : r.countries().get(0),
                r.envs().isEmpty() ? null : r.envs().get(0), r.robots().isEmpty() ? null : r.robots().get(0)));
        return debug.start(tc, resolved[0], resolved[1], resolved[2], r.tag());
    }

    public String stepDebug(String session, String action, int count) {
        return debug.step(session, action, count);
    }

    /** Everything a run call can carry. Lists may hold several countries, environments and robots. */
    public record Request(List<String> refs, List<String> countries, List<String> envs, List<String> robots, int waitSeconds, String tag,
                          Map<String, String> options, String action, boolean force) {
    }

    /** Execution options as the run tool names them, and the queue API parameter each one becomes. */
    private static final Map<String, String> OPTIONS = org.cerberus.core.mcpdelta.cerberus.CerberusGateway.OPTION_PARAMS;

    public static java.util.Set<String> optionNames() {
        return OPTIONS.keySet();
    }

    public String run(Request r) {
        if (!Text.isBlank(r.action())) {
            return tagControl(r.tag(), r.action().trim().toLowerCase());
        }
        List<String> campaigns = r.refs().stream().filter(x -> x.startsWith("campaign:")).toList();
        if (!campaigns.isEmpty()) {
            if (campaigns.size() > 1 || r.refs().size() > 1) {
                throw new Tool.ToolError("run one campaign at a time, without other refs");
            }
            return runCampaign(campaigns.get(0).substring(9).trim(), r);
        }
        return run(r.refs(), r.countries(), r.envs(), r.robots(), r.waitSeconds(), r.tag(), r.options(), r.force());
    }

    public String run(List<String> refs, String country, String env, String robot, int waitSeconds, String tag, boolean force) {
        return run(refs, single(country), single(env), single(robot), waitSeconds, tag, Map.of(), force);
    }

    private static List<String> single(String v) {
        return Text.isBlank(v) ? List.of() : List.of(v);
    }

    private String run(List<String> refs, List<String> countries, List<String> envs, List<String> robots, int waitSeconds, String tag,
                       Map<String, String> options, boolean force) {
        if (refs.isEmpty() && !Text.isBlank(tag)) {
            // No refs and a tag: keep waiting for a run already queued.
            return waitFor(tag.trim(), waitSeconds, "");
        }
        if (refs.isEmpty()) {
            throw new Tool.ToolError("run needs refs: testcases (Folder/Testcase) or scopes (Folder, app:X, label:X); "
                    + "or only tag to keep waiting for a run already queued");
        }
        List<Index.Tc> tcs = ctx.db.read(c -> {
            Map<String, Index.Tc> out = new LinkedHashMap<>();
            for (String r : refs) {
                List<Index.Tc> found = Index.resolve(c, r);
                if (found.isEmpty()) {
                    throw new Tool.ToolError("nothing to run in " + r);
                }
                for (Index.Tc t : found) {
                    out.putIfAbsent(t.ref(), t);
                }
            }
            return new ArrayList<>(out.values());
        });
        if (tcs.size() > MAX_TESTCASES) {
            throw new Tool.ToolError(tcs.size() + " testcases selected; run at most " + MAX_TESTCASES + " at once (narrow the scope)");
        }
        // Every combination asked for is checked; a dimension left out is chosen only when one value is valid.
        java.util.Set<String> cs = new java.util.LinkedHashSet<>();
        java.util.Set<String> es = new java.util.LinkedHashSet<>();
        java.util.Set<String> rs = new java.util.LinkedHashSet<>();
        for (String country : countries.isEmpty() ? java.util.Collections.<String>singletonList(null) : countries) {
            for (String env : envs.isEmpty() ? java.util.Collections.<String>singletonList(null) : envs) {
                for (String robot : robots.isEmpty() ? java.util.Collections.<String>singletonList(null) : robots) {
                    String[] resolved = ctx.db.read(c -> validate(c, tcs, country, env, robot));
                    cs.add(resolved[0]);
                    es.add(resolved[1]);
                    rs.add(resolved[2]);
                }
            }
        }
        checkOptions(options);
        String runTag = Text.isBlank(tag) ? "delta-" + LocalDateTime.now().format(TAG_TIME) : tag.trim();
        String session = org.cerberus.core.mcpdelta.util.CallContext.session();
        boolean tracked = session != null && cs.size() == 1 && es.size() == 1 && rs.size() == 1 && options.isEmpty();
        StringBuilder header = new StringBuilder();
        List<Index.Tc> toRun = tcs;
        Map<String, String> fps = Map.of();
        if (tracked) {
            String country = cs.iterator().next();
            String env = es.iterator().next();
            String robot = rs.iterator().next();
            fps = ctx.db.read(c -> fingerprints(c, tcs));
            if (!force) {
                Map<String, String> fp = fps;
                List<String> passed = new ArrayList<>();
                toRun = new ArrayList<>();
                for (Index.Tc t : tcs) {
                    String exe = ctx.db.read(c -> passedUnchanged(c, t, country, env, robot, fp.get(t.ref()), session));
                    if (exe == null) {
                        toRun.add(t);
                    } else {
                        passed.add(t.testcase() + " OK " + exe);
                    }
                }
                if (!passed.isEmpty()) {
                    header.append("not run again (passed in this session, unchanged since): ").append(String.join(", ", passed))
                            .append(" — force:true runs them anyway\n");
                }
                if (toRun.isEmpty()) {
                    return header + "nothing else to run: every testcase asked for already passed with its current content on "
                            + env + "/" + country + " · robot " + robot + ".";
                }
            }
        }
        String queued = enqueue(toRun, new ArrayList<>(cs), new ArrayList<>(es), new ArrayList<>(rs), runTag, options, null);
        if (tracked) {
            for (Index.Tc t : toRun) {
                ledger.record(RunLedger.key(runTag, t.test(), t.testcase(), cs.iterator().next(), es.iterator().next(), rs.iterator().next()),
                        session, fps.get(t.ref()));
            }
        }
        return waitFor(runTag, waitSeconds, header + queued + "\n");
    }

    /**
     * "#id" of the last execution of a testcase on a target when it passed, was queued by this session with
     * this exact content, recently, and nothing newer is queued for it; null otherwise (it must run).
     */
    private String passedUnchanged(Connection c, Index.Tc t, String country, String env, String robot, String fp, String session)
            throws SQLException {
        Row q = Db.one(c, "SELECT q.Tag, q.State, q.ExeID, e.ControlStatus FROM testcaseexecutionqueue q"
                + " LEFT JOIN testcaseexecution e ON e.ID=q.ExeID WHERE q.Test=? AND q.TestCase=? AND q.Country=? AND q.Environment=?"
                + " AND q.Robot=? ORDER BY q.ID DESC LIMIT 1", t.test(), t.testcase(), country, env, robot);
        if (q == null || !"DONE".equals(q.s("State")) || !"OK".equals(q.s("ControlStatus")) || fp == null) {
            return null;
        }
        return ledger.vouches(RunLedger.key(q.s("Tag"), t.test(), t.testcase(), country, env, robot), session, fp) ? "#" + q.s("ExeID") : null;
    }

    /** Content fingerprint of each testcase, the library steps it uses included: what a run actually executes. */
    private static Map<String, String> fingerprints(Connection c, List<Index.Tc> tcs) throws SQLException {
        Map<String, String> out = new LinkedHashMap<>();
        Map<String, String> libs = new LinkedHashMap<>();
        for (Index.Tc t : tcs) {
            org.cerberus.core.mcpdelta.store.Aggregate a = org.cerberus.core.mcpdelta.store.AggregateStore.load(c, t.test(), t.testcase(), false);
            if (a == null) {
                continue;
            }
            StringBuilder sb = new StringBuilder(a.fingerprint());
            Set<String> used = new java.util.TreeSet<>();
            for (Row s : a.get(org.cerberus.core.mcpdelta.store.Table.STEP)) {
                if ("1".equals(s.s("IsUsingLibraryStep")) && !Text.isBlank(s.s("LibraryStepTest"))) {
                    used.add(s.s("LibraryStepTest") + "\u0001" + s.s("LibraryStepTestcase"));
                }
            }
            for (String lib : used) {
                String[] p = lib.split("\u0001", 2);
                if (!libs.containsKey(lib)) {
                    org.cerberus.core.mcpdelta.store.Aggregate l = org.cerberus.core.mcpdelta.store.AggregateStore.load(c, p[0], p[1], false);
                    libs.put(lib, l == null ? "-" : l.fingerprint());
                }
                sb.append('|').append(libs.get(lib));
            }
            out.put(t.ref(), sb.toString());
        }
        return out;
    }

    private static void checkOptions(Map<String, String> options) {
        for (String k : options.keySet()) {
            if (!OPTIONS.containsKey(k)) {
                throw new Tool.ToolError("unknown run option '" + k + "'; known: " + String.join(", ", new java.util.TreeSet<>(OPTIONS.keySet())));
            }
        }
    }

    /** Runs a campaign as Cerberus defines it (its labels and criteria), optionally on other countries/environments/robots. */
    private String runCampaign(String campaign, Request r) {
        Row exists = ctx.db.read(c -> Db.one(c, "SELECT campaign FROM campaign WHERE campaign=?", campaign));
        if (exists == null) {
            throw new Tool.ToolError("campaign " + campaign + " does not exist (read campaigns)");
        }
        checkOptions(r.options());
        String runTag = Text.isBlank(r.tag()) ? "" : r.tag().trim();
        String queued = enqueue(List.of(), r.countries(), r.envs(), r.robots(), runTag, r.options(), campaign);
        String tag = queued.substring(queued.lastIndexOf("tag ") + 4).trim();
        return waitFor(tag, r.waitSeconds(), queued + "\n");
    }

    /**
     * Cancel, pause or resume what is still waiting in the queue for a tag — the same state changes Cerberus
     * makes, and for resume the same wake-up of its queue job.
     */
    private String tagControl(String tag, String action) {
        if (Text.isBlank(tag)) {
            throw new Tool.ToolError(action + " needs the tag");
        }
        String comment = switch (action) {
            case "cancel" -> "Cancelled by user " + ctx.user();
            case "pause" -> "Paused by user " + ctx.user();
            case "resume" -> "Resumed by user " + ctx.user();
            default -> throw new Tool.ToolError("action is cancel, pause or resume");
        };
        String sql = switch (action) {
            case "cancel" -> "UPDATE testcaseexecutionqueue SET State='CANCELLED', RequestDate=now(), DateModif=now(), comment=?, UsrModif=?"
                    + " WHERE Tag=? AND State IN ('QUEUED','QUWITHDEP','QUEUED_PAUSED','QUWITHDEP_PAUSED')";
            case "pause" -> "UPDATE testcaseexecutionqueue SET State=concat(State,'_PAUSED'), RequestDate=now(), DateModif=now(), comment=?, UsrModif=?"
                    + " WHERE Tag=? AND State IN ('QUEUED','QUWITHDEP')";
            default -> "UPDATE testcaseexecutionqueue SET State=REPLACE(State,'_PAUSED',''), RequestDate=now(), DateModif=now(), comment=?, UsrModif=?"
                    + " WHERE Tag=? AND State LIKE '%_PAUSED'";
        };
        int n = ctx.db.tx(true, c -> {
            int changed = Db.update(c, sql, comment, ctx.user(), tag);
            if (changed > 0) {
                Db.update(c, "UPDATE tag SET Comment=CONCAT(IFNULL(Comment,''), ?), UsrModif=?, DateModif=now() WHERE Tag=?",
                        " " + comment + ": " + changed + " queue entry(ies).", ctx.user(), tag);
            }
            return changed;
        });
        String wake = "resume".equals(action) && n > 0 ? " · " + ctx.gateway.runQueueJob() : "";
        return action + " " + tag + ": " + n + " queue entr" + (n == 1 ? "y" : "ies") + wake + "\n" + reader.tag(tag, false).stripTrailing();
    }

    /**
     * Waits for a tag, never longer than one call may last: MCP clients cut a call after about a minute, and
     * a cut call loses its answer. What is finished is reported; the rest is one more call away.
     */
    private String waitFor(String runTag, int waitSeconds, String header) {
        int wait = Math.max(0, Math.min(ctx.config.maxWaitSeconds, waitSeconds));
        long deadline = System.currentTimeMillis() + wait * 1000L;
        boolean finished;
        StringBuilder notes = new StringBuilder();
        while (true) {
            requeueEngineErrors(runTag, notes);
            finished = ctx.db.read(c -> done(c, runTag));
            if (finished || System.currentTimeMillis() >= deadline) {
                break;
            }
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        String summary = notes + reader.tag(runTag, false) + flaky(runTag);
        if (finished) {
            return header + summary.stripTrailing();
        }
        int[] progress = ctx.db.read(c -> {
            Row q = Db.one(c, "SELECT COUNT(*) total, SUM(State IN ('DONE','ERROR','CANCELLED')) finished FROM testcaseexecutionqueue q WHERE Tag=?"
                    + " AND q.ID=(SELECT MAX(q2.ID) FROM testcaseexecutionqueue q2 WHERE q2.Tag=q.Tag AND q2.Test=q.Test AND q2.TestCase=q.TestCase"
                    + " AND q2.Country=q.Country AND q2.Environment=q.Environment)", runTag);
            return new int[]{q == null ? 0 : q.i("finished"), q == null ? 0 : q.i("total")};
        });
        return header + summary.stripTrailing() + "\nstill running (" + progress[0] + "/" + progress[1]
                + " done) — call run {tag:\"" + runTag + "\"} to wait for the rest";
    }

    /**
     * Same content, different verdict: a testcase that fails now but passed with exactly this content earlier
     * in the session (or the reverse) is intermittent — timing, animation, hover, data — not a wrong
     * selector. Said explicitly, so the next move is to make it robust rather than to run everything again.
     */
    private String flaky(String tag) {
        String session = org.cerberus.core.mcpdelta.util.CallContext.session();
        if (session == null) {
            return "";
        }
        List<Row> exes = ctx.db.read(c -> Db.query(c, "SELECT ID, Test, TestCase, Country, Environment, robot, ControlStatus FROM testcaseexecution"
                + " WHERE Tag=? AND ControlStatus IN ('OK','KO','FA') ORDER BY ID", tag));
        StringBuilder sb = new StringBuilder();
        for (Row e : exes) {
            String key = RunLedger.key(tag, e.s("Test"), e.s("TestCase"), e.s("Country"), e.s("Environment"), e.s("robot"));
            RunLedger.Entry mine = ledger.get(key);
            if (mine == null || !mine.session().equals(session)) {
                continue;
            }
            boolean ok = "OK".equals(e.s("ControlStatus"));
            for (String other : ledger.sameContent(tag, e.s("Test"), e.s("TestCase"), e.s("Country"), e.s("Environment"), e.s("robot"),
                    session, mine.fingerprint())) {
                Row o = ctx.db.read(c -> Db.one(c, "SELECT ID, ControlStatus FROM testcaseexecution WHERE Tag=? AND Test=? AND TestCase=?"
                        + " AND ControlStatus IN ('OK','KO','FA') ORDER BY ID DESC LIMIT 1", other, e.s("Test"), e.s("TestCase")));
                if (o != null && "OK".equals(o.s("ControlStatus")) != ok) {
                    sb.append("intermittent: ").append(e.s("TestCase")).append(ok ? " passed now but failed" : " failed now but passed")
                            .append(" with exactly this content in #").append(o.s("ID"))
                            .append(" — timing, animation, hover or data, not a wrong selector: make that step robust (wait for the element,"
                                    + " waitAfter), and run only this testcase to check\n");
                    break;
                }
            }
        }
        return sb.toString();
    }

    /** Returns {country, environment, robot}, each checked or chosen when only one is valid. */
    private String[] validate(Connection c, List<Index.Tc> tcs, String country, String env, String robot) throws SQLException {
        // Cerberus refuses inactive folders and testcases in the queue, after the fact: say it before queueing.
        List<String> inactive = new ArrayList<>();
        for (Index.Tc t : tcs) {
            Row f = Db.one(c, "SELECT isActive FROM test WHERE Test=?", t.test());
            if (f != null && "0".equals(f.s("isActive"))) {
                inactive.add("folder " + t.test());
            } else if (!t.active()) {
                inactive.add(t.ref());
            }
        }
        if (!inactive.isEmpty()) {
            throw new Tool.ToolError("not active, Cerberus would not run them: " + String.join(", ", new java.util.LinkedHashSet<>(inactive))
                    + " (set active=yes on the folder or the testcase first). Nothing was queued.");
        }
        Set<String> apps = new LinkedHashSet<>();
        for (Index.Tc t : tcs) {
            apps.add(t.application());
        }
        // Countries every selected testcase is defined for.
        Set<String> countries = null;
        for (Index.Tc t : tcs) {
            Set<String> cs = new LinkedHashSet<>();
            for (Row r : Db.query(c, "SELECT Country FROM testcasecountry WHERE Test=? AND Testcase=?", t.test(), t.testcase())) {
                cs.add(r.s("Country"));
            }
            if (countries == null) {
                countries = cs;
            } else {
                countries.retainAll(cs);
            }
        }
        countries = countries == null ? new LinkedHashSet<>() : countries;
        // Only countries where some environment is active for every application can run at all (and, when an
        // environment is given, that one): a single remaining country needs no question.
        Set<String> runnable = new LinkedHashSet<>();
        for (String ct : countries) {
            boolean ok = true;
            for (String app : apps) {
                Row r = Db.one(c, "SELECT COUNT(*) n FROM countryenvironmentparameters cep JOIN countryenvparam ce"
                        + " ON ce.`system`=cep.`system` AND ce.Country=cep.Country AND ce.Environment=cep.Environment"
                        + " WHERE cep.Application=? AND cep.Country=? AND cep.IsActive=1 AND ce.active='Y'"
                        + (Text.isBlank(env) ? "" : " AND cep.Environment=?"),
                        Text.isBlank(env) ? new Object[]{app, ct} : new Object[]{app, ct, env.trim()});
                ok &= r != null && r.i("n") > 0;
            }
            if (ok) {
                runnable.add(ct);
            }
        }
        String ctry = Text.isBlank(country) && runnable.size() == 1 ? runnable.iterator().next()
                : pick("country", country, new ArrayList<>(Text.isBlank(country) ? runnable : countries),
                Text.isBlank(country) ? "countries with an active environment" + (Text.isBlank(env) ? "" : " " + env) + " for these testcases"
                        : "countries common to the selected testcases");
        // Environments active for that country and every application involved.
        Set<String> envs = null;
        for (String app : apps) {
            Set<String> es = new LinkedHashSet<>();
            for (Row r : Db.query(c, "SELECT cep.Environment FROM countryenvironmentparameters cep JOIN countryenvparam ce"
                    + " ON ce.`system`=cep.`system` AND ce.Country=cep.Country AND ce.Environment=cep.Environment"
                    + " WHERE cep.Application=? AND cep.Country=? AND cep.IsActive=1 AND ce.active='Y'", app, ctry)) {
                es.add(r.s("Environment"));
            }
            if (envs == null) {
                envs = es;
            } else {
                envs.retainAll(es);
            }
        }
        envs = envs == null ? Set.of() : envs;
        String environment = pick("env", env, new ArrayList<>(envs), "environments active for " + String.join(", ", apps) + " in " + ctry);
        List<String> robots = new ArrayList<>();
        for (Row r : Db.query(c, "SELECT DISTINCT r.robot FROM robot r JOIN robotexecutor x ON x.robot=r.robot AND x.IsActive=1"
                + " WHERE r.IsActive=1 ORDER BY r.robot")) {
            robots.add(r.s("robot"));
        }
        // A browser, a device or a desktop session needs a robot; an API, a batch or a database test does not.
        boolean needsRobot = false;
        for (String app : apps) {
            Row a = Db.one(c, "SELECT type FROM application WHERE Application=?", app);
            needsRobot |= a == null || a.s("type").matches("(?i)GUI|APK|IPA|FAT");
        }
        if (!needsRobot && Text.isBlank(robot)) {
            return new String[]{ctry, environment, ""};
        }
        String rob = pick("robot", robot, robots, "active robots with an executor");
        return new String[]{ctry, environment, rob};
    }

    private static String pick(String what, String given, List<String> valid, String meaning) {
        if (!Text.isBlank(given)) {
            if (valid.contains(given.trim())) {
                return given.trim();
            }
            throw new Tool.ToolError(what + " '" + given + "' is not valid here; " + meaning + ": "
                    + (valid.isEmpty() ? "none" : String.join(", ", valid)) + ". Nothing was queued.");
        }
        if (valid.size() == 1) {
            return valid.get(0);
        }
        throw new Tool.ToolError(what + " is required; " + meaning + ": " + (valid.isEmpty() ? "none" : String.join(", ", valid))
                + ". Nothing was queued.");
    }

    private String enqueue(List<Index.Tc> tcs, String country, String env, String robot, String tag) {
        return enqueue(tcs, List.of(country), List.of(env), List.of(robot), tag, Map.of(), null);
    }

    private String enqueue(List<Index.Tc> tcs, List<String> countries, List<String> envs, List<String> robots, String tag,
                           Map<String, String> options, String campaign) {
        // Screenshot and page source on error by default: they are what failure analysis reads.
        Map<String, String> opts = new LinkedHashMap<>();
        opts.put("screenshot", "1");
        opts.put("pageSource", "1");
        opts.putAll(options);
        List<String[]> testcases = new ArrayList<>();
        for (Index.Tc t : tcs) {
            testcases.add(new String[]{t.test(), t.testcase()});
        }
        org.cerberus.core.mcpdelta.cerberus.CerberusGateway.Queued q = ctx.gateway.enqueue(
                new org.cerberus.core.mcpdelta.cerberus.CerberusGateway.Order(testcases, countries, envs, robots, tag, opts, campaign));
        String country = String.join(",", countries);
        String env = String.join(",", envs);
        String robot = String.join(",", robots.stream().filter(r -> !Text.isBlank(r)).toList());
        return "queued " + q.count() + " execution(s)" + (campaign == null ? "" : " of campaign " + campaign)
                + (env.isEmpty() ? "" : " · " + env + "/" + country) + (robot.isEmpty() ? "" : " · robot " + robot) + " · tag " + q.tag();
    }

    /**
     * An execution the queue failed to start (engine error before any step ran) has had no effect: it is put
     * back in the queue once, under the same tag, instead of handing a spurious failure to the model.
     */
    private void requeueEngineErrors(String tag, StringBuilder notes) {
        // Two ways an execution dies before the test itself: the queue cannot call the engine, or the robot
        // (browser, device) cannot be started — a saturated grid, a device not ready. Neither says anything about the test.
        List<Row> errors = ctx.db.read(c -> Db.query(c, "SELECT q.ID, q.Test, q.TestCase, q.Country, q.Environment, q.Robot, q.Comment"
                + " FROM testcaseexecutionqueue q LEFT JOIN testcaseexecution e ON e.ID=q.ExeID WHERE q.Tag=?"
                + " AND (q.State='ERROR' AND q.Comment LIKE 'Error occured when calling the service%'"
                + " OR q.State='DONE' AND e.ControlStatus='FA' AND NOT EXISTS (SELECT 1 FROM testcasestepactionexecution a WHERE a.ID=e.ID)"
                + " AND (e.ControlMessage LIKE '%Could not start Robot%' OR e.ControlMessage LIKE '%session not created%'"
                + " OR e.ControlMessage LIKE '%SessionNotCreated%'))", tag));
        for (Row q : errors) {
            String key = tag + "/" + q.s("Test") + "/" + q.s("TestCase");
            // Up to twice: under load the engine can fail to start the same execution two times in a row.
            if (retried.merge(key, 1, Integer::sum) > 2) {
                continue;
            }
            try {
                enqueue(List.of(new Index.Tc(q.s("Test"), q.s("TestCase"), "", "", "", true)), q.s("Country"), q.s("Environment"),
                        q.s("Robot"), tag);
                notes.append("note: ").append(q.s("Test")).append('/').append(q.s("TestCase"))
                        .append(" could not start (engine or robot unavailable, before any step); queued again\n");
            } catch (RuntimeException e) {
                notes.append("note: could not re-queue ").append(q.s("TestCase")).append(": ").append(e.getMessage()).append('\n');
            }
        }
    }

    private final Map<String, Integer> retried = new java.util.concurrent.ConcurrentHashMap<>();

    private static boolean done(Connection c, String tag) throws SQLException {
        // Only the latest queue entry of each testcase counts: an entry retried after an engine error is superseded.
        Row q = Db.one(c, "SELECT COUNT(*) total, SUM(State IN ('DONE','ERROR','CANCELLED')) finished FROM testcaseexecutionqueue q WHERE Tag=?"
                + " AND q.ID=(SELECT MAX(q2.ID) FROM testcaseexecutionqueue q2 WHERE q2.Tag=q.Tag AND q2.Test=q.Test AND q2.TestCase=q.TestCase"
                + " AND q2.Country=q.Country AND q2.Environment=q.Environment)", tag);
        if (q == null || q.i("total") == 0) {
            return false;
        }
        // The queue is the source of truth: once every entry is DONE, ERROR or CANCELLED nothing else will
        // happen, even if Cerberus left an execution row behind in PE after an engine error.
        return q.i("finished") >= q.i("total");
    }
}
