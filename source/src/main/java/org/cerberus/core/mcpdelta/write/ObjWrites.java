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
import org.cerberus.core.mcpdelta.doc.Diagnostic;
import org.cerberus.core.mcpdelta.entity.CurlCommandParser;
import org.cerberus.core.mcpdelta.entity.ObjCodec;
import org.cerberus.core.mcpdelta.entity.ObjDoc;
import org.cerberus.core.mcpdelta.entity.ObjService;
import org.cerberus.core.mcpdelta.entity.ObjStore;
import org.cerberus.core.mcpdelta.entity.Spec;
import org.cerberus.core.mcpdelta.entity.Specs;
import org.cerberus.core.mcpdelta.util.Text;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * The object half of a write: applications, services, campaigns, robots, environments, folders, invariants,
 * labels and data libraries, written as documents, edited, deleted — in the same transaction as the testcases.
 */
final class ObjWrites {

    record Target(ObjService.Ref ref, ObjDoc doc, String source, ObjStore.Agg base) {
    }

    final Map<String, Target> targets = new LinkedHashMap<>();
    private final Context ctx;
    /** Why a write is held as a plan instead of applied (deletions, rows removed by a full document...). */
    final List<String> holdReasons = new ArrayList<>();
    boolean invariantsChanged;

    ObjWrites(Context ctx) {
        this.ctx = ctx;
    }

    /** Splits a text into its testcase part and its object part, by the first word of each top-level line. */
    static String[] split(String text) {
        StringBuilder tc = new StringBuilder();
        StringBuilder obj = new StringBuilder();
        boolean inObject = false;
        for (String line : Text.nz(text).replace("\r\n", "\n").split("\n", -1)) {
            if (!line.isEmpty() && !Character.isWhitespace(line.charAt(0)) && !line.startsWith("#")) {
                String first = line.split("\\s+", 2)[0];
                if (first.equals("testcase")) {
                    inObject = false;
                } else if (Specs.byKind(first) != null) {
                    inObject = true;
                }
            }
            (inObject ? obj : tc).append(line).append('\n');
        }
        return new String[]{tc.toString(), obj.toString()};
    }

    void addDocs(String objectText, String where, List<Diagnostic> diags) {
        if (objectText.isBlank()) {
            return;
        }
        for (ObjDoc d : ObjCodec.parse(objectText, where, diags)) {
            ObjService.Ref ref = new ObjService.Ref(Specs.byKind(d.kind), d.key);
            if (targets.containsKey(ref.text())) {
                diags.add(Diagnostic.error(ref.text(), d.line, "this object appears twice in the same write", "send it once"));
                continue;
            }
            targets.put(ref.text(), new Target(ref, d, "doc", null));
        }
    }

    void addEdits(Connection c, ObjService.Ref ref, List<WriteService.Edit> edits, List<Diagnostic> diags, List<String> notes)
            throws SQLException {
        if (targets.containsKey(ref.text())) {
            diags.add(Diagnostic.error(ref.text(), 0, "both a full document and edits were sent for this object", "send one or the other"));
            return;
        }
        ObjStore.Agg base = ObjStore.load(c, ref.spec(), ref.key(), true);
        if (base == null) {
            diags.add(Diagnostic.error(ref.text(), 0, "does not exist", "to create it, send its full document in docs"));
            return;
        }
        String baseText = ObjCodec.render(ObjStore.toDoc(base, ObjService.labels(c)));
        String text = WriteService.applyEdits(ref.text(), baseText, edits, diags, notes);
        if (text == null) {
            return;
        }
        List<ObjDoc> parsed = ObjCodec.parse(text, ref.text(), diags);
        if (parsed.size() != 1 || !new ObjService.Ref(Specs.byKind(parsed.get(0).kind), parsed.get(0).key).text().equals(ref.text())) {
            diags.add(Diagnostic.error(ref.text(), 0, "an edit must keep the document's first line (kind and key)", null));
            return;
        }
        targets.put(ref.text(), new Target(ref, parsed.get(0), "edit", base));
    }

    void addDelete(ObjService.Ref ref, List<Diagnostic> diags) {
        if (targets.containsKey(ref.text())) {
            diags.add(Diagnostic.error(ref.text(), 0, "an object cannot be changed and deleted in the same write", null));
            return;
        }
        targets.put(ref.text(), new Target(ref, null, "delete", null));
    }

    /** Keys of the objects of a kind this write creates or keeps (documents, not deletions). */
    java.util.Set<String> created(String kind) {
        java.util.Set<String> out = new java.util.HashSet<>();
        for (Target t : targets.values()) {
            if (t.doc != null && t.ref.spec().kind.equals(kind)) {
                out.add(t.ref.key().get(0));
            }
        }
        return out;
    }

    long deletions() {
        return targets.values().stream().filter(t -> t.doc == null).count();
    }

    /** Builds and applies every object target; records a change for each. */
    private Connection conn;

    void apply(Connection c, Map<String, String> expected, List<Diagnostic> diags, WriteService.Outcome out) throws SQLException {
        apply(c, expected, diags, out, false);
    }

    /**
     * @param deletions false: creations and updates (before the testcases, so a folder created here exists for
     *                  them); true: deletions (after the testcases, so a folder emptied in this same write can go)
     */
    void apply(Connection c, Map<String, String> expected, List<Diagnostic> diags, WriteService.Outcome out, boolean deletions) throws SQLException {
        this.conn = c;
        ObjStore.Labels labels = ObjService.labels(c);
        for (Target t : targets.values()) {
            if ((t.doc == null) != deletions) {
                continue;
            }
            Spec.Entity spec = t.ref.spec();
            String ref = t.ref.text();
            ObjStore.Agg base = t.base != null ? t.base : ObjStore.load(c, spec, t.ref.key(), true);
            if (base != null && base.sharing > 1) {
                diags.add(Diagnostic.error(ref, 0, ObjStore.sharedKey(base) + ": which one to " + (t.doc == null ? "delete" : "change")
                        + " would be a guess", "give each its own key in Cerberus first"));
                continue;
            }
            String baseFp = base == null ? WriteService.ABSENT : ObjStore.fingerprint(base);
            if (expected != null && !Objects.equals(expected.get(ref), baseFp)) {
                diags.add(Diagnostic.error(ref, 0, "changed since the plan was made", "plan the change again"));
                continue;
            }
            WriteService.Change ch = new WriteService.Change();
            ch.ref = ref;
            ch.entity = spec.kind;
            ch.baseFingerprint = baseFp;
            ch.snapshot = ObjStore.snapshot(base);
            ch.before = base == null ? "" : ObjCodec.render(ObjStore.toDoc(base, labels));
            ObjStore.Agg target = null;
            if (t.doc == null) {
                if (base == null) {
                    diags.add(Diagnostic.error(ref, 0, "does not exist, nothing to delete", null));
                    continue;
                }
                if (spec.noDelete) {
                    diags.add(Diagnostic.error(ref, 0, spec.plural + " cannot be deleted here", null));
                    continue;
                }
                List<String> blockers = ObjStore.guards(c, spec, t.ref.key());
                if (!blockers.isEmpty()) {
                    diags.add(Diagnostic.error(ref, 0, "cannot be deleted: " + String.join("; ", blockers),
                            "remove or move what depends on it first"));
                    continue;
                }
                ch.kind = "deleted";
            } else {
                int before = diags.size();
                if ("service".equals(spec.kind) && t.doc.attrs.containsKey("curl")) {
                    fromCurl(t.doc, diags, ref);
                }
                target = ObjStore.build(spec, t.doc, base, labels, ctx.catalog, diags, ref);
                check(spec, t, base, target, diags, ref);
                if (diags.subList(before, diags.size()).stream().anyMatch(Diagnostic::error)) {
                    continue;
                }
                ch.kind = base == null ? "created" : "updated";
                if (base != null && "doc".equals(t.source)) {
                    int removed = removedLines(base, target);
                    if (removed > 0) {
                        holdReasons.add(ref + ": the document leaves out " + removed + " existing line(s), which would be removed");
                    }
                }
            }
            if ("invariant".equals(spec.kind)) {
                invariantsChanged = true;
                if (ctx.catalog.invariant("INVARIANTPRIVATE").contains(t.ref.key().get(0))) {
                    holdReasons.add(ref + " is a private invariant: the engine relies on these values");
                }
            }
            ch.stats = ObjStore.apply(c, base, target, ctx.user());
            out.changes.add(ch);
        }
    }

    /**
     * A service written with curl="curl ..." takes its method, URL, body, credentials and headers from the
     * command, as the classic MCP does; anything written explicitly in the document wins.
     */
    private static void fromCurl(ObjDoc d, List<Diagnostic> diags, String ref) {
        String curl = d.attrs.remove("curl");
        CurlCommandParser.ParsedCurl p = CurlCommandParser.parse(curl);
        if (p == null) {
            diags.add(Diagnostic.error(ref, d.line, "could not parse the curl command",
                    "it must start with curl and carry a URL, a header or a body; or write type/method/path instead"));
            return;
        }
        d.attrs.putIfAbsent("type", "REST");
        d.attrs.putIfAbsent("method", p.method());
        if (!p.url().isEmpty()) {
            d.attrs.putIfAbsent("path", p.url());
        }
        if (!p.body().isEmpty()) {
            d.attrs.putIfAbsent("request", p.body());
            d.attrs.putIfAbsent("bodyType", "raw");
        }
        String authType = "";
        String user = "";
        String password = "";
        List<CurlCommandParser.CurlHeader> remaining = new ArrayList<>();
        for (CurlCommandParser.CurlHeader h : p.headers()) {
            String v = h.value();
            if ("authorization".equalsIgnoreCase(h.key()) && authType.isEmpty() && v.regionMatches(true, 0, "Bearer ", 0, 7)) {
                authType = "Bearer Token";
                password = v.substring(7).trim();
            } else if ("authorization".equalsIgnoreCase(h.key()) && authType.isEmpty() && v.regionMatches(true, 0, "Basic ", 0, 6)) {
                try {
                    String decoded = new String(java.util.Base64.getDecoder().decode(v.substring(6).trim()), java.nio.charset.StandardCharsets.UTF_8);
                    int sep = decoded.indexOf(':');
                    if (sep < 0) {
                        remaining.add(h);
                        continue;
                    }
                    authType = "Basic Auth";
                    user = decoded.substring(0, sep);
                    password = decoded.substring(sep + 1);
                } catch (IllegalArgumentException e) {
                    remaining.add(h);
                }
            } else {
                remaining.add(h);
            }
        }
        if (!p.user().isEmpty()) {
            int sep = p.user().indexOf(':');
            authType = "Basic Auth";
            user = sep >= 0 ? p.user().substring(0, sep) : p.user();
            password = sep >= 0 ? p.user().substring(sep + 1) : "";
        }
        if (!authType.isEmpty() && !d.attrs.containsKey("authType")) {
            d.attrs.put("authType", authType);
            d.attrs.putIfAbsent("authUser", user);
            d.attrs.putIfAbsent("authPassword", password);
        }
        int sort = 10;
        for (CurlCommandParser.CurlHeader h : remaining) {
            String key = h.key();
            if (d.lines.stream().noneMatch(l -> "header".equals(l.kind) && !l.keys.isEmpty() && l.keys.get(0).equalsIgnoreCase(key))) {
                ObjDoc.Line l = new ObjDoc.Line();
                l.kind = "header";
                l.keys.add(key);
                l.attrs.put("value", h.value());
                l.attrs.put("sort", String.valueOf(sort));
                d.lines.add(l);
            }
            sort += 10;
        }
    }

    /** Rules a document must follow beyond its fields. */
    private void check(Spec.Entity spec, Target t, ObjStore.Agg base, ObjStore.Agg target, List<Diagnostic> diags, String ref) {
        if (spec.isCollection() && base == null && t.doc.lines.isEmpty()) {
            diags.add(Diagnostic.error(ref, t.doc.line, "a new " + spec.kind + " needs at least one line", null));
        }
        if ("datalib".equals(spec.kind) && "INTERNAL".equals(target.root.get("Type"))
                && target.children.get("sub").stream().noneMatch(r -> Text.nz(r.get("SubData")).isEmpty())) {
            diags.add(Diagnostic.error(ref, t.doc.line, "a datalib needs its key entry: a sub-data with an empty name",
                    "sub \"\" value=\"...\""));
        }
        if (base == null && spec.noCreate) {
            diags.add(Diagnostic.error(ref, t.doc.line, spec.kind + " documents change existing " + spec.plural + " only", null));
        }
        if ("context".equals(spec.kind) && target.root != null) {
            try {
                List<String> allowed = new ArrayList<>();
                for (org.cerberus.core.mcpdelta.db.Db.Row r : org.cerberus.core.mcpdelta.db.Db.query(conn, "SELECT `System` FROM usersystem WHERE Login=?",
                        t.ref.key().get(0))) {
                    allowed.add(r.s("System"));
                }
                String shown = ObjStore.toDoc(target, ObjService.labels(conn)).attrs.getOrDefault("systems", "");
                for (String sys : shown.split(",")) {
                    if (!sys.isBlank() && !allowed.contains(sys.trim())) {
                        diags.add(Diagnostic.error(ref, t.doc.line, "system " + sys.trim() + " is not allowed for " + t.ref.key().get(0),
                                "allowed: " + String.join(", ", allowed) + " (granted by an administrator)"));
                    }
                }
            } catch (SQLException e) {
                throw new org.cerberus.core.mcpdelta.db.Db.DbException(e);
            }
        }
        if ("application".equals(spec.kind) && base == null && Text.isBlank(target.root.get("type"))) {
            diags.add(Diagnostic.error(ref, t.doc.line, "a new application needs its type", "type=GUI"));
        }
    }

    private static int removedLines(ObjStore.Agg base, ObjStore.Agg target) {
        int n = 0;
        for (Map.Entry<String, List<org.cerberus.core.mcpdelta.db.Db.Row>> e : base.children.entrySet()) {
            Spec.Child ch = base.spec.child(e.getKey());
            java.util.Set<String> kept = new java.util.HashSet<>();
            for (org.cerberus.core.mcpdelta.db.Db.Row r : target.children.get(e.getKey())) {
                kept.add(childKey(ch, r));
            }
            for (org.cerberus.core.mcpdelta.db.Db.Row r : e.getValue()) {
                if (!kept.contains(childKey(ch, r))) {
                    n++;
                }
            }
        }
        return n;
    }

    private static String childKey(Spec.Child ch, org.cerberus.core.mcpdelta.db.Db.Row r) {
        StringBuilder sb = new StringBuilder();
        for (Spec.Field k : ch.keys) {
            sb.append(Text.nz(r.get(k.column))).append('\u0001');
        }
        return sb.toString();
    }

    /** Reads back each changed object as stored now. */
    void readBack(Connection c, WriteService.Outcome out) throws SQLException {
        ObjStore.Labels labels = ObjService.labels(c);
        for (WriteService.Change ch : out.changes) {
            if (ch.entity == null) {
                continue;
            }
            ObjService.Ref ref = ObjService.parseRef(ch.ref);
            ObjStore.Agg now = ObjStore.load(c, ref.spec(), ref.key(), false);
            ch.after = now == null ? "" : ObjCodec.render(ObjStore.toDoc(now, labels));
            ch.afterFingerprint = ObjStore.fingerprint(now);
            if (ch.stats.isEmpty()) {
                ch.kind = "unchanged";
            }
        }
    }
}
