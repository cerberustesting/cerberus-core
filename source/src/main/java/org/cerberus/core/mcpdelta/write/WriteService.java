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
package org.cerberus.core.mcpdelta.write;

import org.cerberus.core.mcpdelta.Context;
import org.cerberus.core.mcpdelta.db.Db;
import org.cerberus.core.mcpdelta.doc.Diagnostic;
import org.cerberus.core.mcpdelta.doc.DocException;
import org.cerberus.core.mcpdelta.doc.DocParser;
import org.cerberus.core.mcpdelta.doc.DocRenderer;
import org.cerberus.core.mcpdelta.doc.RowMapper;
import org.cerberus.core.mcpdelta.doc.TcDoc;
import org.cerberus.core.mcpdelta.entity.ObjService;
import org.cerberus.core.mcpdelta.journal.Journal;
import org.cerberus.core.mcpdelta.store.Aggregate;
import org.cerberus.core.mcpdelta.store.AggregateStore;
import org.cerberus.core.mcpdelta.store.DbResolver;
import org.cerberus.core.mcpdelta.store.Index;
import org.cerberus.core.mcpdelta.util.Text;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

/**
 * One write = one intention = one transaction. Whole documents, text edits and scoped replacements are
 * compiled together, checked, turned into the minimal set of row changes, and either applied atomically
 * and journaled, or simulated (the real statements, rolled back) and sealed as a plan.
 */
public final class WriteService {

    public static final class Request {
        public List<String> docs = new ArrayList<>();
        public List<Edit> edits = new ArrayList<>();
        public List<Replace> replace = new ArrayList<>();
        public List<String> delete = new ArrayList<>();
        public boolean dryRun;
        public String intent;
        public String plan;
    }

    public static final class Edit {
        public String ref;
        public String oldText;
        public String newText;
        public boolean all;
    }

    public static final class Replace {
        public String find;
        public String with;
        public boolean regex;
        public boolean ignoreCase;
        public String in;
        public List<String> fields = new ArrayList<>();
    }

    /** What happened to one testcase. */
    public static final class Change {
        public String ref;
        public String kind;
        /** The object kind for objects (application, robot...); null for a testcase. */
        public String entity;
        public String before = "";
        public String after = "";
        public AggregateStore.Stats stats;
        Map<String, List<Map<String, String>>> snapshot;
        String baseFingerprint;
        String afterFingerprint;
        int replacements;
    }

    public static final class Outcome {
        public final List<Change> changes = new ArrayList<>();
        public final List<Diagnostic> warnings = new ArrayList<>();
        public final List<String> notes = new ArrayList<>();
        public boolean committed;
        public boolean guarded;
        public String deltaId;
        public String planId;
    }

    private record Target(String ref, String test, String testcase, TcDoc doc, String source, Aggregate base, int replacements) {
    }

    private record Plan(Request request, Map<String, String> fingerprints, long createdAt) {
    }

    private static final long PLAN_TTL_MS = 60 * 60 * 1000L;
    static final String ABSENT = "absent";

    private final Context ctx;
    private final Map<String, Plan> plans = new ConcurrentHashMap<>();
    private final AtomicInteger planSeq = new AtomicInteger();

    public WriteService(Context ctx) {
        this.ctx = ctx;
    }

    public Outcome write(Request req) {
        if (!Text.isBlank(req.plan)) {
            Plan p = plans.get(req.plan.trim());
            if (p == null || System.currentTimeMillis() - p.createdAt > PLAN_TTL_MS) {
                throw new DocException(Diagnostic.error("plan", 0, "unknown or expired plan '" + req.plan + "'",
                        "send the change again; plans are kept one hour"));
            }
            Outcome o = execute(p.request, true, true, p.fingerprints);
            plans.remove(req.plan.trim());
            return o;
        }
        return execute(req, !req.dryRun, false, null);
    }

    /**
     * The Cerberus roles this write needs, checked before anything is read or written — for a plan too, when it
     * is applied: the same roles the web application asks for the equivalent screens.
     */
    private static void checkAccess(Request req) {
        java.util.regex.Pattern header = java.util.regex.Pattern.compile(
                "^(testcase|application|service|campaign|robot|environment|folder|invariant|labels|datalib|context)\\s", java.util.regex.Pattern.MULTILINE);
        java.util.regex.Pattern hook = java.util.regex.Pattern.compile("^\\s*hook\\s", java.util.regex.Pattern.MULTILINE);
        for (String doc : req.docs) {
            java.util.regex.Matcher m = header.matcher(Text.nz(doc));
            while (m.find()) {
                org.cerberus.core.mcpdelta.util.Access.require(org.cerberus.core.mcpdelta.util.Access.forKind(m.group(1), false), "write " + m.group(1) + " documents");
            }
            if (hook.matcher(Text.nz(doc)).find()) {
                org.cerberus.core.mcpdelta.util.Access.require(org.cerberus.core.mcpdelta.util.Access.ADMIN, "write campaign hooks");
            }
        }
        for (Edit e : req.edits) {
            ObjService.Ref ref = ObjService.parseRef(Text.nz(e.ref).trim());
            String kind = ref == null ? "testcase" : ref.spec().kind;
            org.cerberus.core.mcpdelta.util.Access.require(org.cerberus.core.mcpdelta.util.Access.forKind(kind, false), "edit " + kind + " documents");
            if (hook.matcher(Text.nz(e.newText)).find()) {
                org.cerberus.core.mcpdelta.util.Access.require(org.cerberus.core.mcpdelta.util.Access.ADMIN, "write campaign hooks");
            }
        }
        if (!req.replace.isEmpty()) {
            org.cerberus.core.mcpdelta.util.Access.require(org.cerberus.core.mcpdelta.util.Access.TESTCASE, "change testcases");
        }
        // One's own context (the systems one works in) is one's own; another user's needs an administrator.
        String me = org.cerberus.core.mcpdelta.util.CallContext.user();
        if (me != null) {
            java.util.regex.Matcher ctxDoc = java.util.regex.Pattern.compile("^context\\s+\"?([^\":\\s]+)", java.util.regex.Pattern.MULTILINE)
                    .matcher(String.join("\n", req.docs));
            while (ctxDoc.find()) {
                if (!ctxDoc.group(1).equals(me)) {
                    org.cerberus.core.mcpdelta.util.Access.require(org.cerberus.core.mcpdelta.util.Access.ADMIN, "change another user's context");
                }
            }
            for (Edit e : req.edits) {
                String ref = Text.nz(e.ref).trim();
                if (ref.startsWith("context:") && !ref.substring(8).trim().equals(me)) {
                    org.cerberus.core.mcpdelta.util.Access.require(org.cerberus.core.mcpdelta.util.Access.ADMIN, "change another user's context");
                }
            }
        }
        for (String d : req.delete) {
            ObjService.Ref ref = ObjService.parseRef(Text.nz(d).trim());
            String kind = ref == null ? "testcase" : ref.spec().kind;
            org.cerberus.core.mcpdelta.util.Access.require(org.cerberus.core.mcpdelta.util.Access.forKind(kind, true), "delete " + kind + "s");
        }
    }

    private Outcome execute(Request req, boolean commit, boolean forced, Map<String, String> expected) {
        checkAccess(req);
        Outcome out = new Outcome();
        try (Connection c = ctx.db.open()) {
            c.setAutoCommit(false);
            try {
                run(c, req, commit, forced, expected, out);
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new Db.DbException(e);
        }
        return out;
    }

    private void run(Connection c, Request req, boolean commit, boolean forced, Map<String, String> expected, Outcome out)
            throws SQLException {
        DbResolver resolver = new DbResolver(c, ctx.catalog);
        List<Diagnostic> diags = new ArrayList<>();
        Map<String, Target> targets = new LinkedHashMap<>();
        ObjWrites objects = new ObjWrites(ctx);

        // 1. Whole documents: create, or replace what a testcase (or an object) is.
        for (int i = 0; i < req.docs.size(); i++) {
            String where = req.docs.size() == 1 ? "doc" : "docs[" + i + "]";
            String[] parts = ObjWrites.split(req.docs.get(i));
            objects.addDocs(parts[1], where, diags);
            if (parts[0].isBlank()) {
                continue;
            }
            List<TcDoc> parsed = DocParser.parseLenient(parts[0], where, diags);
            for (TcDoc d : parsed) {
                if (d.testcase == null || d.testcase.isEmpty()) {
                    continue;
                }
                if (targets.containsKey(d.ref())) {
                    diags.add(Diagnostic.error(d.ref(), d.line, "this testcase appears twice in the same write", "send it once"));
                    continue;
                }
                targets.put(d.ref(), new Target(d.ref(), d.test, d.testcase, d, "doc", null, 0));
            }
        }

        // 2. Text edits: the document as it is now, edited like a file.
        Map<String, List<Edit>> editsByRef = new LinkedHashMap<>();
        for (Edit e : req.edits) {
            editsByRef.computeIfAbsent(Text.nz(e.ref).trim(), k -> new ArrayList<>()).add(e);
        }
        for (Map.Entry<String, List<Edit>> e : editsByRef.entrySet()) {
            String ref = e.getKey();
            ObjService.Ref objRef = ObjService.parseRef(ref);
            if (objRef != null) {
                objects.addEdits(c, objRef, e.getValue(), diags, out.notes);
                continue;
            }
            String[] parts = Index.splitRef(ref);
            if (parts == null) {
                diags.add(Diagnostic.error("edits", 0, "edit ref must be <Folder>/<Testcase>, got '" + ref + "'", null));
                continue;
            }
            if (targets.containsKey(parts[0] + "/" + parts[1])) {
                diags.add(Diagnostic.error(ref, 0, "both a full document and edits were sent for this testcase", "send one or the other"));
                continue;
            }
            Aggregate base = AggregateStore.load(c, parts[0], parts[1], true);
            if (base == null) {
                diags.add(Diagnostic.error(ref, 0, "testcase does not exist", "to create it, send its full document in docs"));
                continue;
            }
            String baseText = DocRenderer.render(RowMapper.toDoc(base, resolver, false));
            String text = applyEdits(ref, baseText, e.getValue(), diags, out.notes);
            if (text == null) {
                continue;
            }
            List<Diagnostic> syntax = new ArrayList<>();
            List<TcDoc> parsed = DocParser.parseLenient(text, ref, syntax);
            diags.addAll(withLineText(syntax, text));
            if (parsed.size() != 1) {
                diags.add(Diagnostic.error(ref, 0, "after the edits the document holds " + parsed.size() + " testcases", "keep exactly one testcase line"));
                continue;
            }
            TcDoc doc = parsed.get(0);
            if (!doc.ref().equals(base.ref())) {
                diags.add(Diagnostic.error(ref, doc.line, "an edit cannot rename the testcase (" + doc.ref() + ")",
                        "keep the testcase line as it is"));
                continue;
            }
            targets.put(base.ref(), new Target(base.ref(), base.test, base.testcase, doc, "edit", base, 0));
        }

        // 3. Scoped replacement: one rule, every matching value in the scope.
        for (Replace rule : req.replace) {
            replace(c, rule, resolver, targets, diags, out.notes);
        }

        // 4. Deletions.
        for (String ref : req.delete) {
            ObjService.Ref objRef = ObjService.parseRef(ref);
            if (objRef != null) {
                objects.addDelete(objRef, diags);
                continue;
            }
            String[] parts = Index.splitRef(ref);
            if (parts == null) {
                diags.add(Diagnostic.error("delete", 0, "delete ref must be <Folder>/<Testcase>, got '" + ref + "'", null));
                continue;
            }
            String key = parts[0] + "/" + parts[1];
            if (targets.containsKey(key)) {
                diags.add(Diagnostic.error(key, 0, "a testcase cannot be changed and deleted in the same write", null));
                continue;
            }
            targets.put(key, new Target(key, parts[0], parts[1], null, "delete", null, 0));
        }

        if (targets.isEmpty() && objects.targets.isEmpty() && diags.stream().noneMatch(Diagnostic::error)) {
            if (out.notes.isEmpty()) {
                diags.add(Diagnostic.error("write", 0, "nothing to write", "give docs, edits, replace or delete"));
            }
        }
        if (diags.stream().anyMatch(Diagnostic::error)) {
            // Syntax errors stop the write, but the meaning is still checked: every problem comes back at once.
            for (Target t : targets.values()) {
                if (t.doc != null) {
                    boolean exists = t.base != null || AggregateStore.load(c, t.test, t.testcase, false) != null;
                    new Validator(ctx.catalog, diags, t.ref).known(objects.created("folder"), objects.created("application")).check(t.doc, !exists);
                }
            }
            throw new DocException(diags);
        }

        long deletions = targets.values().stream().filter(t -> t.doc == null).count() + objects.deletions();
        boolean guarded = commit && !forced && (deletions > 0 || targets.size() > ctx.config.maxAutoTestcases);

        // 5a. Objects first: a folder, an application or a label created in this write exists for its testcases.
        objects.apply(c, expected, diags, out);
        if (!forced && !objects.holdReasons.isEmpty()) {
            guarded = commit;
            out.notes.addAll(objects.holdReasons);
        }

        // 5b. Testcases, library testcases first so the steps that use them resolve.
        List<Target> ordered = new ArrayList<>(targets.values());
        ordered.sort((a, b) -> Boolean.compare(!definesLibrary(a.doc), !definesLibrary(b.doc)));
        for (Target t : ordered) {
            Aggregate base = t.base != null ? t.base : AggregateStore.load(c, t.test, t.testcase, true);
            String baseFp = base == null ? ABSENT : base.fingerprint();
            if (expected != null && !Objects.equals(expected.get(t.ref), baseFp)) {
                diags.add(Diagnostic.error(t.ref, 0, "changed since the plan was made", "plan the change again"));
                continue;
            }
            Change ch = new Change();
            ch.ref = t.ref;
            ch.baseFingerprint = baseFp;
            ch.snapshot = Journal.snapshot(base);
            ch.replacements = t.replacements;
            ch.before = base == null ? "" : DocRenderer.render(RowMapper.toDoc(base, resolver, false));
            Aggregate target = null;
            if (t.doc == null) {
                if (base == null) {
                    diags.add(Diagnostic.error(t.ref, 0, "testcase does not exist, nothing to delete", null));
                    continue;
                }
                ch.kind = "deleted";
            } else {
                List<Diagnostic> found = new ArrayList<>();
                new Validator(ctx.catalog, found, t.ref).known(objects.created("folder"), objects.created("application")).check(t.doc, base == null);
                target = new RowBuilder(resolver, found, t.ref).build(t.doc, base);
                diags.addAll(tolerateExisting(found, base, resolver, t.ref));
                if (diags.stream().anyMatch(d -> d.error() && t.ref.equals(d.where()))) {
                    continue;
                }
                ch.kind = base == null ? "created" : "updated";
            }
            ch.stats = AggregateStore.apply(c, base, target, ctx.user());
            resolver.forget(t.test, t.testcase);
            out.changes.add(ch);
        }
        // 5c. Object deletions last: a folder whose testcases this same write deletes is empty by now.
        objects.apply(c, expected, diags, out, true);
        if (diags.stream().anyMatch(Diagnostic::error)) {
            c.rollback();
            throw new DocException(diags);
        }
        out.warnings.addAll(diags);

        // 6. Read back what the database now holds: the answer shows the real result, not the request.
        objects.readBack(c, out);
        for (Change ch : out.changes) {
            if (ch.entity != null) {
                continue;
            }
            String[] parts = Index.splitRef(ch.ref);
            Aggregate now = AggregateStore.load(c, parts[0], parts[1], false);
            ch.after = now == null ? "" : DocRenderer.render(RowMapper.toDoc(now, resolver, false));
            ch.afterFingerprint = now == null ? null : now.fingerprint();
            if (ch.stats.isEmpty()) {
                ch.kind = "unchanged";
            }
        }

        boolean anything = out.changes.stream().anyMatch(ch -> !ch.stats.isEmpty());
        if (commit && !guarded && anything) {
            Journal.Delta d = new Journal.Delta();
            d.id = ctx.journal.nextId();
            d.user = ctx.user();
            d.intent = Text.isBlank(req.intent) ? summary(out) : req.intent.trim();
            for (Change ch : out.changes) {
                if (ch.stats.isEmpty()) {
                    continue;
                }
                Journal.Entry e = new Journal.Entry();
                e.ref = ch.ref;
                e.entity = ch.entity;
                e.before = ch.snapshot;
                e.afterFingerprint = ch.afterFingerprint;
                e.summary = ch.kind;
                d.entries.add(e);
            }
            ctx.journal.save(d);
            c.commit();
            out.committed = true;
            out.deltaId = d.id;
            if (!objects.targets.isEmpty()) {
                ctx.catalog.invalidate();
            }
            if (objects.invariantsChanged) {
                // Cerberus keeps invariants in memory: ask it to reload them.
                out.notes.add(org.cerberus.core.mcpdelta.run.CerberusHttp.purgeCache(ctx));
            }
        } else {
            c.rollback();
            if (anything) {
                out.guarded = guarded;
                Map<String, String> fps = new LinkedHashMap<>();
                for (Change ch : out.changes) {
                    fps.put(ch.ref, ch.baseFingerprint);
                }
                Request sealed = copyWithoutPlan(req);
                String id = "p" + planSeq.incrementAndGet();
                plans.put(id, new Plan(sealed, fps, System.currentTimeMillis()));
                out.planId = id;
            }
        }
    }

    /**
     * A problem the testcase already had before this write (an empty step title, a deprecated action) is
     * reported, not blocking: the model is not made to fix what it did not touch.
     */
    private List<Diagnostic> tolerateExisting(List<Diagnostic> found, Aggregate base, DbResolver resolver, String ref) {
        if (base == null || found.stream().noneMatch(Diagnostic::error)) {
            return found;
        }
        List<Diagnostic> baseDiags = new ArrayList<>();
        TcDoc baseDoc = RowMapper.toDoc(base, resolver, false);
        new Validator(ctx.catalog, baseDiags, ref).check(baseDoc, false);
        new RowBuilder(resolver, baseDiags, ref).build(baseDoc, base);
        java.util.Set<String> existing = new java.util.HashSet<>();
        for (Diagnostic d : baseDiags) {
            if (d.error()) {
                existing.add(d.message());
            }
        }
        List<Diagnostic> out = new ArrayList<>();
        for (Diagnostic d : found) {
            if (d.error() && existing.contains(d.message())) {
                out.add(new Diagnostic(d.where(), d.line(), false, d.message() + " (already the case before this change)", d.fix()));
            } else {
                out.add(d);
            }
        }
        return out;
    }

    private static boolean definesLibrary(TcDoc doc) {
        return doc != null && doc.steps.stream().anyMatch(s -> s.flags.containsKey("library"));
    }

    private static Request copyWithoutPlan(Request r) {
        Request c = new Request();
        c.docs = new ArrayList<>(r.docs);
        c.edits = new ArrayList<>(r.edits);
        c.replace = new ArrayList<>(r.replace);
        c.delete = new ArrayList<>(r.delete);
        c.intent = r.intent;
        return c;
    }

    // ---------------------------------------------------------------- text edits

    /**
     * Applies old→new edits to a document, like a file editor: each old text must be found exactly once
     * (unless all is set). Indentation slips are forgiven: if the exact text is not found, lines are
     * compared without their leading spaces.
     */
    static String applyEdits(String ref, String text, List<Edit> edits, List<Diagnostic> diags, List<String> notes) {
        String cur = text;
        int n = 0;
        for (Edit e : edits) {
            n++;
            String where = ref + " edit " + n;
            String oldT = Text.nz(e.oldText).replace("\r\n", "\n");
            String newT = Text.nz(e.newText).replace("\r\n", "\n");
            if (oldT.isEmpty()) {
                diags.add(Diagnostic.error(where, 0, "old text is empty", "quote the lines to change, as read; to add a line, include the line before it in old and new"));
                return null;
            }
            int count = Text.count(cur, oldT);
            if (count == 1 || (count > 1 && e.all)) {
                cur = e.all ? replaceAllAligned(cur, oldT, newT) : replaceAligned(cur, oldT, newT, cur.indexOf(oldT));
                continue;
            }
            if (count > 1) {
                diags.add(Diagnostic.error(where, 0, "old text appears " + count + " times", "include the surrounding lines to make it unique, or set all=true"));
                return null;
            }
            String loose = looseReplace(cur, oldT, newT, e.all);
            if (loose != null) {
                cur = loose;
                continue;
            }
            if (!newT.isEmpty() && cur.contains(newT.strip())) {
                notes.add(where + ": already applied, skipped");
                continue;
            }
            diags.add(Diagnostic.error(where, 0, "old text not found: " + Text.truncate(Text.oneLine(oldT), 80),
                    closestLine(cur, oldT)));
            return null;
        }
        return cur;
    }

    private static String replaceAllAligned(String text, String oldT, String newT) {
        List<Integer> hits = new ArrayList<>();
        for (int i = text.indexOf(oldT); i >= 0; i = text.indexOf(oldT, i + oldT.length())) {
            hits.add(i);
        }
        String out = text;
        // From the last occurrence back, so the earlier positions stay valid.
        for (int k = hits.size() - 1; k >= 0; k--) {
            out = replaceAligned(out, oldT, newT, hits.get(k));
        }
        return out;
    }

    /**
     * Replaces at {@code at}. When the old text starts inside an indented line and the new text adds lines with
     * less indentation than that line, those lines are written as if relative to it — the usual slip of a
     * replacement that quotes "openUrlWithBase ..." without its two leading spaces.
     */
    private static String replaceAligned(String text, String oldT, String newT, int at) {
        int lineStart = text.lastIndexOf('\n', at - 1) + 1;
        String before = text.substring(lineStart, at);
        String fixed = newT;
        if (!before.isEmpty() && before.isBlank() && newT.contains("\n")) {
            String[] lines = newT.split("\n", -1);
            int minRest = Integer.MAX_VALUE;
            for (int k = 1; k < lines.length; k++) {
                if (!lines[k].isBlank()) {
                    minRest = Math.min(minRest, lines[k].length() - lines[k].stripLeading().length());
                }
            }
            if (minRest != Integer.MAX_VALUE && minRest < before.length()) {
                StringBuilder sb = new StringBuilder(lines[0]);
                for (int k = 1; k < lines.length; k++) {
                    sb.append('\n').append(lines[k].isBlank() ? lines[k] : before + lines[k]);
                }
                fixed = sb.toString();
            }
        }
        return text.substring(0, at) + fixed + text.substring(at + oldT.length());
    }

    private static String looseReplace(String text, String oldT, String newT, boolean all) {
        String[] lines = text.split("\n", -1);
        String[] want = oldT.strip().split("\n");
        List<Integer> hits = new ArrayList<>();
        for (int i = 0; i + want.length <= lines.length; i++) {
            boolean ok = true;
            for (int k = 0; k < want.length && ok; k++) {
                ok = lines[i + k].strip().equals(want[k].strip());
            }
            if (ok) {
                hits.add(i);
            }
        }
        if (hits.isEmpty() || (hits.size() > 1 && !all)) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        int i = 0;
        int h = 0;
        while (i < lines.length) {
            if (h < hits.size() && i == hits.get(h)) {
                String indent = lines[i].substring(0, lines[i].length() - lines[i].stripLeading().length());
                String[] repl = newT.strip().isEmpty() ? new String[0] : newT.stripTrailing().split("\n");
                // Re-indent the replacement as the matched block was indented, keeping its inner structure.
                int baseIndent = repl.length == 0 ? 0 : repl[0].length() - repl[0].stripLeading().length();
                for (String r : repl) {
                    int own = r.length() - r.stripLeading().length();
                    sb.append(indent).append(" ".repeat(Math.max(0, own - baseIndent))).append(r.stripLeading()).append('\n');
                }
                i += want.length;
                h++;
                continue;
            }
            sb.append(lines[i]);
            if (i < lines.length - 1) {
                sb.append('\n');
            }
            i++;
        }
        return sb.toString();
    }

    private static String closestLine(String text, String oldT) {
        String first = oldT.strip().split("\n")[0].strip();
        String best = null;
        int bestD = Integer.MAX_VALUE;
        for (String l : text.split("\n")) {
            int d = Text.levenshtein(l.strip(), first);
            if (d < bestD) {
                bestD = d;
                best = l.strip();
            }
        }
        return best == null ? "read the testcase again" : "closest line is: " + best;
    }

    private static List<Diagnostic> withLineText(List<Diagnostic> diags, String text) {
        String[] lines = text.split("\n", -1);
        List<Diagnostic> out = new ArrayList<>();
        for (Diagnostic d : diags) {
            if (d.line() > 0 && d.line() <= lines.length) {
                out.add(new Diagnostic(d.where(), 0, d.error(), d.message() + " in: " + Text.truncate(lines[d.line() - 1].strip(), 90), d.fix()));
            } else {
                out.add(d);
            }
        }
        return out;
    }

    // ---------------------------------------------------------------- scoped replacement

    private void replace(Connection c, Replace r, DbResolver resolver, Map<String, Target> targets, List<Diagnostic> diags,
                         List<String> notes) throws SQLException {
        if (Text.isBlank(r.find)) {
            diags.add(Diagnostic.error("replace", 0, "replace needs find", null));
            return;
        }
        Pattern p;
        try {
            int flags = r.ignoreCase ? Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE : 0;
            p = r.regex ? Pattern.compile(r.find, flags) : Pattern.compile(Pattern.quote(r.find), flags);
        } catch (Exception e) {
            diags.add(Diagnostic.error("replace", 0, "invalid regex: " + e.getMessage(), null));
            return;
        }
        String with = r.with == null ? "" : r.with;
        String replacement = r.regex ? with : java.util.regex.Matcher.quoteReplacement(with);
        List<String> fields = r.fields == null || r.fields.isEmpty() ? List.of("values", "properties") : r.fields;
        for (String f : fields) {
            if (!List.of("values", "properties", "descriptions", "all").contains(f)) {
                diags.add(Diagnostic.error("replace", 0, "unknown field group '" + f + "'", "values, properties, descriptions or all"));
                return;
            }
        }
        List<Index.Tc> scope;
        try {
            scope = Index.resolve(c, r.in);
        } catch (DocException e) {
            diags.addAll(e.diagnostics());
            return;
        }
        int total = 0;
        int touched = 0;
        for (Index.Tc tc : scope) {
            Target existing = targets.get(tc.ref());
            TcDoc doc;
            Aggregate base = null;
            if (existing != null) {
                if (existing.doc == null) {
                    continue;
                }
                doc = existing.doc;
            } else {
                base = AggregateStore.load(c, tc.test(), tc.testcase(), true);
                if (base == null) {
                    continue;
                }
                doc = RowMapper.toDoc(base, resolver, false);
            }
            int n = Replacer.apply(doc, p, replacement, fields);
            if (n == 0) {
                continue;
            }
            total += n;
            touched++;
            if (existing != null) {
                targets.put(tc.ref(), new Target(existing.ref, existing.test, existing.testcase, doc, existing.source, existing.base, n));
            } else {
                targets.put(tc.ref(), new Target(tc.ref(), tc.test(), tc.testcase(), doc, "replace", base, n));
            }
        }
        if (total == 0) {
            notes.add("replace: no match for " + Text.quote(r.find) + " in " + r.in + " (" + scope.size() + " testcases searched, fields "
                    + String.join("+", fields) + ")");
        } else {
            notes.add("replace: " + total + " value(s) in " + touched + " testcase(s) of " + scope.size() + " searched");
        }
    }

    // ---------------------------------------------------------------- answer

    public static String summary(Outcome o) {
        long created = o.changes.stream().filter(c -> "created".equals(c.kind)).count();
        long updated = o.changes.stream().filter(c -> "updated".equals(c.kind)).count();
        long deleted = o.changes.stream().filter(c -> "deleted".equals(c.kind)).count();
        List<String> parts = new ArrayList<>();
        if (created > 0) {
            parts.add(created + " created");
        }
        if (updated > 0) {
            parts.add(updated + " updated");
        }
        if (deleted > 0) {
            parts.add(deleted + " deleted");
        }
        return parts.isEmpty() ? "no change" : String.join(", ", parts);
    }
}
