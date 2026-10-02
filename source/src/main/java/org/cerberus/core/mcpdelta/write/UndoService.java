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
import org.cerberus.core.mcpdelta.doc.DocRenderer;
import org.cerberus.core.mcpdelta.doc.RowMapper;
import org.cerberus.core.mcpdelta.entity.ObjCodec;
import org.cerberus.core.mcpdelta.entity.ObjService;
import org.cerberus.core.mcpdelta.entity.ObjStore;
import org.cerberus.core.mcpdelta.journal.Journal;
import org.cerberus.core.mcpdelta.store.Aggregate;
import org.cerberus.core.mcpdelta.store.AggregateStore;
import org.cerberus.core.mcpdelta.store.DbResolver;
import org.cerberus.core.mcpdelta.store.Index;
import org.cerberus.core.mcpdelta.tools.Tool;
import org.cerberus.core.mcpdelta.util.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Reverts a delta by putting back the exact rows each testcase had before it. Refused when a testcase changed
 * since — by a person or a later delta — so an undo never erases work it did not do. The undo is itself a
 * delta, so it can be undone.
 */
public final class UndoService {

    private final Context ctx;

    public UndoService(Context ctx) {
        this.ctx = ctx;
    }

    public String undo(String id) {
        String deltaId = Text.nz(id).trim();
        Journal.Delta d = ctx.journal.load(deltaId);
        if (d == null) {
            throw new Tool.ToolError("unknown delta '" + deltaId + "'; read journal to list them");
        }
        if (d.undoneBy != null) {
            throw new Tool.ToolError(deltaId + " was already undone by " + d.undoneBy);
        }
        return ctx.db.tx(true, c -> {
            DbResolver resolver = new DbResolver(c, ctx.catalog);
            List<String> conflicts = new ArrayList<>();
            List<Aggregate> currents = new ArrayList<>();
            List<ObjStore.Agg> objCurrents = new ArrayList<>();
            // Undoing restores what was written: it needs the roles the write needed.
            for (Journal.Entry e : d.entries) {
                ObjService.Ref r = ObjService.parseRef(e.ref);
                String kind = r == null ? "testcase" : r.spec().kind;
                org.cerberus.core.mcpdelta.util.Access.require(org.cerberus.core.mcpdelta.util.Access.forKind(kind, true), "undo changes to " + kind + "s");
            }
            for (Journal.Entry e : d.entries) {
                String fp;
                if (e.entity != null) {
                    ObjService.Ref ref = ObjService.parseRef(e.ref);
                    ObjStore.Agg cur = ObjStore.load(c, ref.spec(), ref.key(), true);
                    objCurrents.add(cur);
                    currents.add(null);
                    fp = ObjStore.fingerprint(cur);
                } else {
                    String[] p = Index.splitRef(e.ref);
                    Aggregate cur = AggregateStore.load(c, p[0], p[1], true);
                    currents.add(cur);
                    objCurrents.add(null);
                    fp = cur == null ? null : cur.fingerprint();
                }
                if (!Objects.equals(fp, e.afterFingerprint)) {
                    conflicts.add(e.ref);
                }
            }
            if (!conflicts.isEmpty()) {
                List<String> later = new ArrayList<>();
                for (Journal.Delta x : ctx.journal.recent(50)) {
                    if (x.id.equals(deltaId)) {
                        break;
                    }
                    if (x.undoneBy == null && x.entries.stream().anyMatch(en -> conflicts.contains(en.ref))) {
                        later.add(x.id);
                    }
                }
                throw new Tool.ToolError("cannot undo " + deltaId + ": " + String.join(", ", conflicts) + " changed since"
                        + (later.isEmpty() ? " (outside MCP Delta)" : "; undo " + String.join(", ", later) + " first")
                        + ". Nothing was changed.");
            }
            Journal.Delta u = new Journal.Delta();
            u.id = ctx.journal.nextId();
            u.user = ctx.user();
            u.undoes = deltaId;
            u.intent = "undo " + deltaId + (d.intent == null ? "" : " (" + d.intent + ")");
            StringBuilder diff = new StringBuilder();
            ObjStore.Labels labels = ObjService.labels(c);
            for (int i = d.entries.size() - 1; i >= 0; i--) {
                Journal.Entry e = d.entries.get(i);
                if (e.entity != null) {
                    ObjService.Ref ref = ObjService.parseRef(e.ref);
                    ObjStore.Agg cur = objCurrents.get(i);
                    String before = cur == null ? "" : ObjCodec.render(ObjStore.toDoc(cur, labels));
                    ObjStore.restore(c, ref.spec(), ref.key(), e.before);
                    ObjStore.Agg now = ObjStore.load(c, ref.spec(), ref.key(), false);
                    String after = now == null ? "" : ObjCodec.render(ObjStore.toDoc(now, labels));
                    Journal.Entry ue = new Journal.Entry();
                    ue.ref = e.ref;
                    ue.entity = e.entity;
                    ue.before = ObjStore.snapshot(cur);
                    ue.afterFingerprint = ObjStore.fingerprint(now);
                    ue.summary = now == null ? "deleted" : cur == null ? "restored" : "reverted";
                    u.entries.add(ue);
                    diff.append("── ").append(e.ref).append(' ').append(ue.summary).append('\n');
                    if (cur != null && now != null) {
                        for (String l : Text.docDiff(before, after)) {
                            diff.append(l).append('\n');
                        }
                    }
                    continue;
                }
                String[] p = Index.splitRef(e.ref);
                Aggregate cur = currents.get(i);
                String before = cur == null ? "" : DocRenderer.render(RowMapper.toDoc(cur, resolver, false));
                AggregateStore.restore(c, p[0], p[1], Journal.restoreAggregate(p[0], p[1], e.before));
                resolver.forget(p[0], p[1]);
                Aggregate now = AggregateStore.load(c, p[0], p[1], false);
                String after = now == null ? "" : DocRenderer.render(RowMapper.toDoc(now, resolver, false));
                Journal.Entry ue = new Journal.Entry();
                ue.ref = e.ref;
                ue.before = Journal.snapshot(cur);
                ue.afterFingerprint = now == null ? null : now.fingerprint();
                ue.summary = now == null ? "deleted" : cur == null ? "restored" : "reverted";
                u.entries.add(ue);
                diff.append("── ").append(e.ref).append(' ').append(ue.summary).append('\n');
                if (cur != null && now != null) {
                    for (String l : Text.docDiff(before, after)) {
                        diff.append(l).append('\n');
                    }
                }
            }
            d.undoneBy = u.id;
            ctx.journal.save(u);
            ctx.journal.save(d);
            return "undone " + deltaId + " as " + u.id + " (undo " + u.id + " to redo)\n" + diff.toString().stripTrailing();
        });
    }
}
