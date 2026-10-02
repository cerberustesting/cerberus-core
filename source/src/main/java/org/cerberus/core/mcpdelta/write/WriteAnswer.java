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

import org.cerberus.core.mcpdelta.doc.Diagnostic;
import org.cerberus.core.mcpdelta.util.Text;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The answer to a write: what the database now holds, as a diff of documents. Identical changes across
 * testcases are shown once — a selector fixed in 30 testcases is one hunk and a list, not 30 hunks.
 */
public final class WriteAnswer {

    private static final int MAX_LINES = 120;

    private WriteAnswer() {
    }

    public static String format(WriteService.Outcome o) {
        StringBuilder sb = new StringBuilder();
        List<WriteService.Change> real = o.changes.stream().filter(c -> c.stats != null && !c.stats.isEmpty()).toList();
        int rows = real.stream().mapToInt(c -> c.stats.inserted + c.stats.updated + c.stats.deleted).sum();
        if (o.committed) {
            sb.append("applied ").append(o.deltaId).append(" · ").append(WriteService.summary(o)).append(" · ")
                    .append(rows).append(" rows · undo: undo ").append(o.deltaId).append('\n');
        } else if (o.planId != null && o.guarded) {
            sb.append("held as plan ").append(o.planId).append(" (nothing written): ").append(WriteService.summary(o))
                    .append(o.changes.stream().anyMatch(c -> "deleted".equals(c.kind)) ? " — deletions" : " — large change")
                    .append(" wait for a confirmation · apply: write {plan:\"").append(o.planId).append("\"}\n");
        } else if (o.planId != null) {
            sb.append("plan ").append(o.planId).append(" (simulated, nothing written): ").append(WriteService.summary(o))
                    .append(" · ").append(rows).append(" rows · apply: write {plan:\"").append(o.planId).append("\"}\n");
        } else {
            sb.append("no change: the testcases already read this way\n");
        }
        for (String note : o.notes) {
            sb.append("note: ").append(note).append('\n');
        }

        Map<String, String> firstWithDiff = new LinkedHashMap<>();
        List<String> lines = new ArrayList<>();
        List<String> same = new ArrayList<>();
        int shown = 0;
        for (WriteService.Change c : real) {
            List<String> diff = "deleted".equals(c.kind) ? List.of() : Text.docDiff(c.before, c.after);
            String key = String.join("\n", diff.stream().filter(l -> !l.startsWith("  ")).toList());
            if ("updated".equals(c.kind) && !key.isEmpty() && firstWithDiff.containsKey(key)) {
                same.add(c.ref + " (same change as " + firstWithDiff.get(key) + ")");
                continue;
            }
            if ("updated".equals(c.kind) && !key.isEmpty()) {
                firstWithDiff.put(key, c.ref);
            }
            if (shown >= MAX_LINES) {
                same.add(c.ref + " " + c.kind);
                continue;
            }
            lines.add("── " + c.ref + " " + c.kind + (c.replacements > 0 ? " (" + c.replacements + " values replaced)" : ""));
            if ("created".equals(c.kind)) {
                String[] stored = c.after.split("\n");
                if (stored.length <= 40) {
                    // The stored form, as Cerberus now holds it: no need to read it back.
                    lines.add("  stored as:");
                    for (String l : stored) {
                        lines.add("  | " + l);
                    }
                    shown += stored.length;
                } else {
                    lines.add("  " + stored.length + " lines, " + c.stats.inserted + " rows (read it to see the stored form)");
                }
            } else {
                for (String l : diff) {
                    if (shown++ >= MAX_LINES) {
                        lines.add("  … diff cut, read " + c.ref + " for the whole testcase");
                        break;
                    }
                    lines.add(l);
                }
            }
        }
        for (String l : lines) {
            sb.append(l).append('\n');
        }
        if (!same.isEmpty()) {
            sb.append("also: ").append(String.join("; ", same)).append('\n');
        }
        List<Diagnostic> warnings = o.warnings.stream().filter(d -> !d.error()).toList();
        if (!warnings.isEmpty()) {
            Map<String, Integer> dedup = new LinkedHashMap<>();
            for (Diagnostic d : warnings) {
                dedup.merge(d.format(), 1, Integer::sum);
            }
            sb.append("warnings:\n");
            int k = 0;
            for (Map.Entry<String, Integer> e : dedup.entrySet()) {
                if (k++ >= 15) {
                    sb.append("  … ").append(dedup.size() - 15).append(" more\n");
                    break;
                }
                sb.append("  ").append(e.getKey()).append(e.getValue() > 1 ? " (×" + e.getValue() + ")" : "").append('\n');
            }
        }
        return sb.toString().stripTrailing();
    }
}
