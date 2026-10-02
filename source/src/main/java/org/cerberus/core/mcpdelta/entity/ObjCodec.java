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

import org.cerberus.core.mcpdelta.doc.Diagnostic;
import org.cerberus.core.mcpdelta.doc.Lexer;
import org.cerberus.core.mcpdelta.doc.Lexer.Kind;
import org.cerberus.core.mcpdelta.doc.Lexer.Token;
import org.cerberus.core.mcpdelta.util.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Renders and parses object documents. Same lexical rules as testcase documents. */
public final class ObjCodec {

    private static final int WIDTH = 110;

    private ObjCodec() {
    }

    public static String render(ObjDoc d) {
        StringBuilder sb = new StringBuilder(d.kind).append(' ').append(keyToken(d.key));
        if (d.title != null) {
            sb.append(": ").append(Text.oneLine(d.title));
        }
        sb.append('\n');
        StringBuilder line = new StringBuilder("  ");
        for (Map.Entry<String, String> e : d.attrs.entrySet()) {
            String token = e.getKey() + "=" + value(e.getValue());
            if (line.length() > 2 && line.length() + 1 + token.length() > WIDTH) {
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
        for (ObjDoc.Line l : d.lines) {
            sb.append("  ").append(line(l)).append('\n');
        }
        return sb.toString();
    }

    public static String line(ObjDoc.Line l) {
        StringBuilder sb = new StringBuilder(l.kind);
        for (String k : l.keys) {
            sb.append(' ').append(keyValue(k));
        }
        for (Map.Entry<String, String> e : l.attrs.entrySet()) {
            sb.append(' ').append(e.getKey()).append('=').append(value(e.getValue()));
        }
        String d = Text.nz(l.description);
        if (!d.isEmpty()) {
            // A description "// text" is trimmed and on one line; any other one is written exactly, quoted.
            if (d.equals(d.strip()) && !d.contains("\n") && !d.contains("\r")) {
                sb.append(" // ").append(d);
            } else {
                sb.append(" desc=").append(Text.quote(d));
            }
        }
        return sb.toString();
    }

    /** Simple words stay bare (type=GUI, active=yes, sort=10); anything else is quoted, so a value always reads one way. */
    static String value(String v) {
        String s = Text.nz(v);
        return !s.isEmpty() && s.matches("[A-Za-z0-9_.,*+-]+") ? s : Text.quote(s);
    }

    public static String keyToken(List<String> key) {
        String joined = String.join("/", key);
        while (joined.endsWith("/")) {
            joined = joined.substring(0, joined.length() - 1);
        }
        return joined.isEmpty() || joined.contains(" ") || joined.contains(":") || joined.contains("\"") || joined.contains("=")
                ? Text.quote(joined) : joined;
    }

    private static String keyValue(String k) {
        String v = Text.nz(k);
        if (v.isEmpty() || v.contains("=") || v.startsWith("//") || v.contains("\"") || v.contains(" ") || v.contains("#")) {
            return Text.quote(v);
        }
        return v;
    }

    /** Parses every object document in a text. {@code kinds} tells which first words start a document. */
    public static List<ObjDoc> parse(String text, String where, List<Diagnostic> diags) {
        Lexer lexer = new Lexer(where, diags);
        List<ObjDoc> docs = new ArrayList<>();
        ObjDoc doc = null;
        Spec.Entity spec = null;
        for (Lexer.Line ln : lexer.logicalLines(text == null ? "" : text)) {
            String first = ln.text().split("\\s+", 2)[0];
            if (ln.indent() == 0) {
                spec = Specs.byKind(first);
                if (spec == null) {
                    diags.add(Diagnostic.error(where, ln.number(), "unknown kind of document '" + first + "'",
                            "one of: testcase, " + String.join(", ", Specs.all().keySet())));
                    doc = null;
                    continue;
                }
                doc = header(lexer, ln, spec, where, diags);
                docs.add(doc);
                continue;
            }
            if (doc == null) {
                continue;
            }
            String[] desc = new String[1];
            List<Token> toks = lexer.tokens(ln.text(), ln.number(), desc);
            if (toks.isEmpty()) {
                continue;
            }
            if (toks.get(0).kind() == Kind.KV) {
                for (Token t : toks) {
                    if (t.kind() != Kind.KV) {
                        diags.add(Diagnostic.error(where, ln.number(), "attribute lines hold key=value pairs, got '" + t.text() + "'", null));
                        continue;
                    }
                    doc.attrs.put(t.key(), t.text());
                }
                continue;
            }
            ObjDoc.Line l = new ObjDoc.Line();
            l.kind = toks.get(0).text();
            l.line = ln.number();
            l.description = desc[0] == null ? "" : desc[0];
            boolean attrs = false;
            for (int i = 1; i < toks.size(); i++) {
                Token t = toks.get(i);
                if (t.kind() == Kind.KV) {
                    attrs = true;
                    l.attrs.put(t.key(), t.text());
                } else if (attrs) {
                    diags.add(Diagnostic.error(where, ln.number(), "'" + t.text() + "' comes after attributes; key values come right after '"
                            + l.kind + "'", null));
                } else {
                    l.keys.add(t.text());
                }
            }
            doc.lines.add(l);
        }
        return docs;
    }

    private static ObjDoc header(Lexer lexer, Lexer.Line ln, Spec.Entity spec, String where, List<Diagnostic> diags) {
        ObjDoc d = new ObjDoc();
        d.kind = spec.kind;
        d.line = ln.number();
        String rest = ln.text().substring(spec.kind.length()).trim();
        int colon = Lexer.titleColon(rest);
        String keyPart = colon < 0 ? rest : rest.substring(0, colon).trim();
        if (colon >= 0) {
            d.title = rest.substring(colon + 1).trim();
        }
        boolean quoted = keyPart.startsWith("\"");
        if (quoted) {
            int[] end = new int[1];
            keyPart = lexer.readString(keyPart, 0, end, ln.number());
        }
        if (keyPart.isEmpty()) {
            diags.add(Diagnostic.error(where, ln.number(), spec.kind + " needs its key", spec.kind + " " + example(spec)));
        }
        String[] parts = keyPart.split("/", -1);
        for (int i = 0; i < spec.keys.size(); i++) {
            d.key.add(i < parts.length ? (quoted ? parts[i] : parts[i].trim()) : "");
        }
        if (parts.length > spec.keys.size()) {
            diags.add(Diagnostic.error(where, ln.number(), spec.kind + " key has " + spec.keys.size() + " part(s): " + example(spec), null));
        }
        return d;
    }

    static String example(Spec.Entity spec) {
        List<String> names = new ArrayList<>();
        for (Spec.Field f : spec.keys) {
            names.add("<" + f.attr + ">");
        }
        return String.join("/", names);
    }
}
