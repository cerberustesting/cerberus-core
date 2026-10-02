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
package org.cerberus.core.mcpdelta.entity;

import org.cerberus.core.mcpdelta.Context;
import org.cerberus.core.mcpdelta.db.Db;
import org.cerberus.core.mcpdelta.db.Db.Row;
import org.cerberus.core.mcpdelta.entity.Spec.Entity;
import org.cerberus.core.mcpdelta.entity.Spec.Field;
import org.cerberus.core.mcpdelta.util.Text;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Reading objects: references, documents and listings. */
public final class ObjService {

    /** A parsed "kind:key" reference. */
    public record Ref(Entity spec, List<String> key) {
        public String text() {
            return spec.kind + ":" + String.join("/", ObjStore.trimKey(key));
        }
    }

    private final Context ctx;

    public ObjService(Context ctx) {
        this.ctx = ctx;
    }

    /** "robot:LocalChromium", "environment:DEFAULT/FR/QA", "datalib:ACCOUNTS"; null when it is not an object reference. */
    public static Ref parseRef(String raw) {
        String r = Text.nz(raw).trim();
        int colon = r.indexOf(':');
        if (colon <= 0) {
            return null;
        }
        Entity e = Specs.byKind(r.substring(0, colon));
        if (e == null) {
            return null;
        }
        String keyPart = r.substring(colon + 1).trim();
        boolean quoted = keyPart.startsWith("\"") && keyPart.endsWith("\"") && keyPart.length() > 1;
        if (quoted) {
            keyPart = keyPart.substring(1, keyPart.length() - 1);
        }
        String[] parts = keyPart.split("/", -1);
        List<String> key = new ArrayList<>();
        for (int i = 0; i < e.keys.size(); i++) {
            key.add(i < parts.length ? (quoted ? parts[i] : parts[i].trim()) : "");
        }
        return new Ref(e, key);
    }

    /** True for "robots", "applications:DEFAULT"... */
    public static boolean isListing(String raw) {
        String r = Text.nz(raw).trim();
        int colon = r.indexOf(':');
        String head = colon < 0 ? r : r.substring(0, colon);
        return Specs.byPlural(head) != null && (colon < 0 || Specs.byKind(head) == null);
    }

    public String read(Connection c, String raw) throws SQLException {
        if (isListing(raw)) {
            String r = raw.trim();
            int colon = r.indexOf(':');
            Entity e = Specs.byPlural(colon < 0 ? r : r.substring(0, colon));
            return listing(c, e, colon < 0 ? "" : r.substring(colon + 1).trim());
        }
        Ref ref = parseRef(raw);
        if (ref == null) {
            return null;
        }
        ObjStore.Agg a = ObjStore.load(c, ref.spec(), ref.key(), false);
        if (a == null) {
            List<Row> near = ObjStore.list(c, ref.spec(), ref.key().get(0), 8);
            return "!! " + ref.text() + " does not exist" + (near.isEmpty() ? "" : " — close: " + String.join(", ", keys(ref.spec(), near)))
                    + " (read " + ref.spec().plural + " to list them; write a " + ref.spec().kind + " document to create it)";
        }
        String doc = ObjCodec.render(ObjStore.toDoc(a, labels(c)));
        int lines = a.children.values().stream().mapToInt(List::size).sum();
        return "# v" + ObjStore.fingerprint(a) + (lines > 0 ? " · " + lines + " line(s)" : "") + "\n" + doc;
    }

    private String listing(Connection c, Entity e, String filter) throws SQLException {
        List<Row> rows = ObjStore.list(c, e, filter, 500);
        StringBuilder sb = new StringBuilder(e.plural).append(Text.isBlank(filter) ? "" : " matching " + filter).append(" — ")
                .append(rows.size() == 500 ? "500+" : rows.size()).append(" · ").append(e.summary == null ? "" : e.summary).append('\n');
        if (e.isCollection()) {
            for (Row r : rows) {
                sb.append("  ").append(e.kind).append(':').append(r.s("k0")).append("  (").append(r.s("n")).append(")\n");
            }
            return sb.toString();
        }
        for (Row r : rows) {
            sb.append("  ").append(String.join("/", ObjStore.trimKey(keyOf(e, r))));
            for (String attr : e.listAttrs) {
                Field f = e.field(attr);
                if (f != null && !r.s(f.column).isEmpty()) {
                    sb.append("  ").append(attr).append('=').append(ObjStore.display(f, r.get(f.column), id -> id, null));
                }
            }
            if (e.title != null && !r.s(e.title).isEmpty()) {
                sb.append("  · ").append(Text.truncate(Text.oneLine(r.s(e.title)), 70));
            }
            sb.append('\n');
        }
        return sb.append("read ").append(e.kind).append(":<key> for one, as a document you can edit").toString();
    }

    private static List<String> keyOf(Entity e, Row r) {
        List<String> k = new ArrayList<>();
        for (Field f : e.keys) {
            k.add(r.s(f.column));
        }
        return k;
    }

    private static List<String> keys(Entity e, List<Row> rows) {
        List<String> out = new ArrayList<>();
        for (Row r : rows) {
            out.add(e.kind + ":" + String.join("/", ObjStore.trimKey(keyOf(e, r))));
        }
        return out;
    }

    /** Label names and ids, read on the caller's connection so labels created in the same write resolve. */
    public static ObjStore.Labels labels(Connection c) {
        Map<String, String> names = new HashMap<>();
        return new ObjStore.Labels() {
            @Override
            public String name(String id) {
                if (id == null || id.isEmpty() || "0".equals(id)) {
                    return null;
                }
                return names.computeIfAbsent(id, k -> {
                    try {
                        Row r = Db.one(c, "SELECT Label FROM label WHERE Id=?", k);
                        return r == null ? null : r.s("Label");
                    } catch (SQLException e) {
                        throw new Db.DbException(e);
                    }
                });
            }

            @Override
            public String id(String name, String system) {
                try {
                    List<Row> rows = Db.query(c, "SELECT Id, `System` FROM label WHERE Label=? ORDER BY (`System`=?) DESC, Id", name,
                            system == null ? "" : system);
                    return rows.isEmpty() ? null : rows.get(0).s("Id");
                } catch (SQLException e) {
                    throw new Db.DbException(e);
                }
            }
        };
    }
}
