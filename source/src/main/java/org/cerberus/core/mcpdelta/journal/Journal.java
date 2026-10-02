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
package org.cerberus.core.mcpdelta.journal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.cerberus.core.mcpdelta.db.Db.Row;
import org.cerberus.core.mcpdelta.store.Aggregate;
import org.cerberus.core.mcpdelta.store.Table;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * The git of MCP Delta: one file per applied delta, with the exact rows each touched testcase had before and
 * the fingerprint it had after. Plain JSON, readable and exportable at any time.
 */
public final class Journal {

    /** One testcase inside a delta. {@code before} null = the delta created it; {@code after} null = deleted it. */
    public static final class Entry {
        public String ref;
        /** Object kind (application, robot...) for an object; null for a testcase. */
        public String entity;
        public Map<String, List<Map<String, String>>> before;
        public String afterFingerprint;
        public String summary;
    }

    public static final class Delta {
        public String id;
        public String time;
        public String user;
        public String intent;
        public String undoes;
        public String undoneBy;
        public List<Entry> entries = new ArrayList<>();
    }

    private final Path dir;
    private final ObjectMapper json = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    private int last;

    public Journal(Path dir) {
        this.dir = dir;
        try {
            Files.createDirectories(dir);
            try (Stream<Path> files = Files.list(dir)) {
                last = files.map(p -> p.getFileName().toString())
                        .filter(n -> n.matches("d\\d+\\.json"))
                        .mapToInt(n -> Integer.parseInt(n.substring(1, n.length() - 5)))
                        .max().orElse(0);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Cannot use journal directory " + dir, e);
        }
    }

    public synchronized String nextId() {
        return "d" + (++last);
    }

    public synchronized void save(Delta d) {
        if (d.time == null) {
            d.time = Instant.now().toString();
        }
        try {
            json.writeValue(dir.resolve(d.id + ".json").toFile(), d);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot write journal entry " + d.id, e);
        }
    }

    public Delta load(String id) {
        Path p = dir.resolve(id + ".json");
        if (!Files.exists(p)) {
            return null;
        }
        try {
            return json.readValue(p.toFile(), Delta.class);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read journal entry " + id, e);
        }
    }

    /** Most recent first. */
    public List<Delta> recent(int max) {
        List<Delta> out = new ArrayList<>();
        for (int i = last; i > 0 && out.size() < max; i--) {
            Delta d = load("d" + i);
            if (d != null) {
                out.add(d);
            }
        }
        return out;
    }

    public static Map<String, List<Map<String, String>>> snapshot(Aggregate a) {
        if (a == null) {
            return null;
        }
        Map<String, List<Map<String, String>>> out = new LinkedHashMap<>();
        for (Table t : Table.values()) {
            List<Map<String, String>> rows = new ArrayList<>();
            for (Row r : a.get(t)) {
                rows.add(new LinkedHashMap<>(r));
            }
            out.put(t.name(), rows);
        }
        return out;
    }

    public static Aggregate restoreAggregate(String test, String testcase, Map<String, List<Map<String, String>>> snap) {
        if (snap == null) {
            return null;
        }
        Aggregate a = new Aggregate(test, testcase);
        for (Map.Entry<String, List<Map<String, String>>> e : snap.entrySet()) {
            Table t = Table.valueOf(e.getKey());
            for (Map<String, String> m : e.getValue()) {
                a.get(t).add(new Row(m));
            }
        }
        return a;
    }
}
