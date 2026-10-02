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
package org.cerberus.core.mcpdelta.store;

import org.cerberus.core.mcpdelta.db.Db;
import org.cerberus.core.mcpdelta.db.Db.Row;
import org.cerberus.core.mcpdelta.doc.Diagnostic;
import org.cerberus.core.mcpdelta.doc.DocException;
import org.cerberus.core.mcpdelta.util.Text;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Resolves scopes — "DemoShop", "app:DemoShop", "label:smoke", "DemoShop/DEMO-00*", "*" — into testcases.
 * A scope is how one intention reaches many testcases without the model listing them one by one.
 */
public final class Index {

    public record Tc(String test, String testcase, String application, String status, String title, boolean active) {
        public String ref() {
            return test + "/" + testcase;
        }
    }

    private Index() {
    }

    public static List<Tc> resolve(Connection c, String scope) throws SQLException {
        if (Text.isBlank(scope)) {
            throw new DocException(Diagnostic.error("scope", 0, "a scope is required",
                    "a folder (DemoShop), a testcase (DemoShop/DEMO-001), app:<application>, label:<label>, status:<s>, type:<t>, system:<s>, q:<text in id or title>, or * for everything"));
        }
        Map<String, Tc> out = new LinkedHashMap<>();
        for (String raw : splitScopes(scope)) {
            String s = raw.trim();
            if (s.isEmpty()) {
                continue;
            }
            List<Row> rows;
            if (s.equals("*")) {
                rows = Db.query(c, BASE + " ORDER BY tc.Test, tc.Testcase");
            } else if (s.startsWith("app:")) {
                rows = Db.query(c, BASE + " WHERE tc.Application=? ORDER BY tc.Test, tc.Testcase", s.substring(4).trim());
            } else if (s.startsWith("status:")) {
                rows = Db.query(c, BASE + " WHERE tc.Status=? ORDER BY tc.Test, tc.Testcase", s.substring(7).trim());
            } else if (s.startsWith("system:")) {
                rows = Db.query(c, BASE + " JOIN application a ON a.Application=tc.Application WHERE a.`System`=? ORDER BY tc.Test, tc.Testcase",
                        s.substring(7).trim());
            } else if (s.startsWith("type:")) {
                rows = Db.query(c, BASE + " WHERE tc.Type=? ORDER BY tc.Test, tc.Testcase", s.substring(5).trim());
            } else if (s.startsWith("q:")) {
                String q = "%" + s.substring(2).trim().replace("%", "\\%").replace("_", "\\_") + "%";
                rows = Db.query(c, BASE + " WHERE tc.Testcase LIKE ? OR tc.Description LIKE ? ORDER BY tc.Test, tc.Testcase", q, q);
            } else if (s.startsWith("label:")) {
                rows = Db.query(c, BASE + " JOIN testcaselabel tl ON tl.Test=tc.Test AND tl.Testcase=tc.Testcase"
                        + " JOIN label l ON l.Id=tl.LabelId WHERE l.Label=? ORDER BY tc.Test, tc.Testcase", s.substring(6).trim());
            } else if (s.contains("/")) {
                int slash = s.lastIndexOf('/');
                String test = s.substring(0, slash);
                String tc = s.substring(slash + 1);
                if (tc.isEmpty() || tc.equals("*")) {
                    rows = Db.query(c, BASE + " WHERE tc.Test=? ORDER BY tc.Testcase", test);
                } else if (tc.contains("*")) {
                    rows = Db.query(c, BASE + " WHERE tc.Test=? AND tc.Testcase LIKE ? ORDER BY tc.Testcase", test,
                            tc.replace("%", "\\%").replace("_", "\\_").replace('*', '%'));
                } else {
                    rows = Db.query(c, BASE + " WHERE tc.Test=? AND tc.Testcase=?", test, tc);
                }
            } else {
                rows = Db.query(c, BASE + " WHERE tc.Test=? ORDER BY tc.Testcase", s);
            }
            for (Row r : rows) {
                Tc t = new Tc(r.s("Test"), r.s("Testcase"), r.s("Application"), r.s("Status"), r.s("Description"),
                        !"0".equals(r.s("isActive")));
                out.putIfAbsent(t.ref(), t);
            }
        }
        return new ArrayList<>(out.values());
    }

    private static final String BASE = "SELECT tc.Test, tc.Testcase, tc.Application, tc.Status, tc.Description, tc.isActive FROM testcase tc";

    /** Splits on commas outside quotes, so a folder name with a comma can still be quoted. */
    static List<String> splitScopes(String scope) {
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean q = false;
        for (char ch : scope.toCharArray()) {
            if (ch == '"') {
                q = !q;
            } else if (ch == ',' && !q) {
                out.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(ch);
            }
        }
        out.add(cur.toString());
        return out;
    }

    /** Splits "Folder/Testcase" at its last slash; null when it is not a testcase reference. */
    public static String[] splitRef(String ref) {
        String r = Text.nz(ref).trim();
        if (r.startsWith("\"") && r.endsWith("\"") && r.length() > 1) {
            r = r.substring(1, r.length() - 1);
        }
        int slash = r.lastIndexOf('/');
        if (slash <= 0 || slash == r.length() - 1) {
            return null;
        }
        return new String[]{r.substring(0, slash), r.substring(slash + 1)};
    }

    public static Pattern glob(String g) {
        return Pattern.compile(Pattern.quote(g).replace("*", "\\E.*\\Q"));
    }
}
