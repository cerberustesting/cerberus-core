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
package org.cerberus.core.mcpdelta.doc;

import org.cerberus.core.mcpdelta.util.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Renders a {@link TcDoc} as text. The output is canonical: parsing it and rendering again gives the same
 * text, which is what makes "edit the text, the server computes the delta" safe.
 */
public final class DocRenderer {

    private static final int HEADER_WIDTH = 110;

    private DocRenderer() {
    }

    public static String render(TcDoc doc) {
        StringBuilder sb = new StringBuilder();
        sb.append("testcase ").append(refToken(doc.test, doc.testcase)).append(": ").append(Text.oneLine(doc.title)).append('\n');
        List<String> attrTokens = new ArrayList<>();
        for (Map.Entry<String, String> e : doc.attrs.entrySet()) {
            attrTokens.add(e.getKey() + "=" + Text.quoteIfNeeded(e.getValue()));
        }
        if (doc.condition != null && !doc.condition.isAlways()) {
            attrTokens.add(condition(doc.condition).trim());
        }
        StringBuilder line = new StringBuilder("  ");
        for (String token : attrTokens) {
            if (line.length() > 2 && line.length() + 1 + token.length() > HEADER_WIDTH) {
                sb.append(line).append('\n');
                line = new StringBuilder("  ");
            }
            if (line.length() > 2) {
                line.append(' ');
            }
            line.append(token);
        }
        if (line.length() > 2) {
            sb.append(line).append('\n');
        }
        for (TcDoc.Prop p : doc.props) {
            sb.append(prop(p)).append('\n');
        }
        int n = 1;
        for (TcDoc.Step s : doc.steps) {
            sb.append(stepLine(s, n++)).append('\n');
            for (TcDoc.Item a : s.actions) {
                sb.append("  ").append(item(a)).append('\n');
                for (TcDoc.Item c : a.controls) {
                    sb.append("    ").append(item(c)).append('\n');
                }
            }
        }
        return sb.toString();
    }

    public static String refToken(String test, String testcase) {
        String ref = test + "/" + testcase;
        return (ref.contains(" ") || ref.contains(":") || ref.contains("\"")) ? Text.quote(ref) : ref;
    }

    public static String stepLine(TcDoc.Step s, int position) {
        StringBuilder sb = new StringBuilder("step ").append(position);
        for (Map.Entry<String, String> e : s.flags.entrySet()) {
            sb.append(' ').append(flag(e.getKey(), e.getValue()));
        }
        if (s.condition != null && !s.condition.isAlways()) {
            sb.append(condition(s.condition));
        }
        return sb.append(": ").append(Text.oneLine(s.title)).toString();
    }

    public static String prop(TcDoc.Prop p) {
        StringBuilder sb = new StringBuilder("prop ").append(p.name).append(" = ").append(p.type);
        values(sb, p.values);
        for (Map.Entry<String, String> e : p.flags.entrySet()) {
            sb.append(' ').append(flag(e.getKey(), e.getValue()));
        }
        if (p.countries != null) {
            sb.append(" countries=").append(String.join(",", p.countries));
        }
        description(sb, p.description);
        return sb.toString();
    }

    public static String item(TcDoc.Item it) {
        StringBuilder sb = new StringBuilder(it.name);
        values(sb, it.values);
        for (Map.Entry<String, String> e : it.flags.entrySet()) {
            sb.append(' ').append(flag(e.getKey(), e.getValue()));
        }
        if (it.condition != null && !it.condition.isAlways()) {
            sb.append(condition(it.condition));
        }
        description(sb, it.description);
        return sb.toString();
    }

    /** The item with its controls, as one block: the signature used to recognise unchanged items. */
    public static String itemBlock(TcDoc.Item it) {
        StringBuilder sb = new StringBuilder(item(it));
        for (TcDoc.Item c : it.controls) {
            sb.append('\n').append("  ").append(item(c));
        }
        return sb.toString();
    }

    public static String stepBlock(TcDoc.Step s) {
        StringBuilder sb = new StringBuilder(stepLine(s, 0));
        for (TcDoc.Item a : s.actions) {
            sb.append('\n').append(itemBlock(a));
        }
        return sb.toString();
    }

    private static void values(StringBuilder sb, List<String> values) {
        int last = values.size() - 1;
        while (last >= 0 && Text.nz(values.get(last)).isEmpty()) {
            last--;
        }
        for (int i = 0; i <= last; i++) {
            sb.append(' ').append(Text.quote(values.get(i)));
        }
    }

    private static String flag(String key, String value) {
        if ("true".equals(value)) {
            return key;
        }
        return key + "=" + Text.quoteIfNeeded(value);
    }

    private static String condition(TcDoc.Cond c) {
        StringBuilder sb = new StringBuilder(" if ").append(c.operator);
        values(sb, c.values);
        for (Map.Entry<String, String> e : c.options.entrySet()) {
            sb.append(" if.").append(e.getKey()).append('=').append(Text.quoteIfNeeded(e.getValue()));
        }
        return sb.toString();
    }

    private static void description(StringBuilder sb, String description) {
        String d = Text.oneLine(description);
        if (!d.isEmpty()) {
            sb.append(" // ").append(d);
        }
    }
}
