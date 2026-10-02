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

import org.cerberus.core.mcpdelta.doc.TcDoc;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Replacement on values, not on text: a replacement that contains quotes or backslashes can never break the
 * document syntax, and a field group decides what may be touched (values by default, never titles unless
 * asked).
 */
final class Replacer {

    private Replacer() {
    }

    /** Applies the pattern in place and returns how many values changed. */
    static int apply(TcDoc doc, Pattern p, String replacement, List<String> fields) {
        boolean all = fields.contains("all");
        boolean values = all || fields.contains("values");
        boolean props = all || fields.contains("properties");
        boolean descriptions = all || fields.contains("descriptions");
        int n = 0;
        if (descriptions) {
            String t = sub(doc.title, p, replacement);
            if (!t.equals(doc.title)) {
                doc.title = t;
                n++;
            }
        }
        if (values && doc.condition != null) {
            n += list(doc.condition.values, p, replacement);
        }
        if (props) {
            for (TcDoc.Prop pr : doc.props) {
                n += list(pr.values, p, replacement);
            }
        }
        for (TcDoc.Step s : doc.steps) {
            if (descriptions) {
                String t = sub(s.title, p, replacement);
                if (!t.equals(s.title)) {
                    s.title = t;
                    n++;
                }
            }
            if (values && s.condition != null) {
                n += list(s.condition.values, p, replacement);
            }
            for (TcDoc.Item a : s.actions) {
                n += item(a, p, replacement, values, descriptions);
                for (TcDoc.Item c : a.controls) {
                    n += item(c, p, replacement, values, descriptions);
                }
            }
        }
        return n;
    }

    private static int item(TcDoc.Item it, Pattern p, String replacement, boolean values, boolean descriptions) {
        int n = 0;
        if (values) {
            n += list(it.values, p, replacement);
            if (it.condition != null) {
                n += list(it.condition.values, p, replacement);
            }
        }
        if (descriptions) {
            String d = sub(it.description, p, replacement);
            if (!d.equals(it.description)) {
                it.description = d;
                n++;
            }
        }
        return n;
    }

    private static int list(List<String> values, Pattern p, String replacement) {
        int n = 0;
        for (int i = 0; i < values.size(); i++) {
            String v = values.get(i);
            String r = sub(v, p, replacement);
            if (!r.equals(v)) {
                values.set(i, r);
                n++;
            }
        }
        return n;
    }

    private static String sub(String value, Pattern p, String replacement) {
        if (value == null || value.isEmpty()) {
            return value == null ? "" : value;
        }
        Matcher m = p.matcher(value);
        return m.find() ? m.replaceAll(replacement) : value;
    }
}
