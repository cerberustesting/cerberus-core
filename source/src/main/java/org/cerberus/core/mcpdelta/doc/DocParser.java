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

import org.cerberus.core.mcpdelta.doc.Lexer.Kind;
import org.cerberus.core.mcpdelta.doc.Lexer.Line;
import org.cerberus.core.mcpdelta.doc.Lexer.Token;
import org.cerberus.core.mcpdelta.util.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Parses Delta documents. One text may hold several testcases. Every problem found is reported, with its
 * line and a suggested fix, instead of stopping at the first one: a model fixes all of them in one retry.
 */
public final class DocParser {

    /** Header attribute names, canonical first; the map gives the accepted aliases. */
    public static final List<String> HEADER_KEYS = List.of("application", "status", "priority", "countries", "labels",
            "type", "active", "muted", "activeQA", "activeUAT", "activePROD", "details", "comment", "bugs", "origin",
            "refOrigin", "implementer", "executor", "fromMajor", "fromMinor", "toMajor", "toMinor", "targetMajor",
            "targetMinor", "useragent", "screensize");
    private static final Map<String, String> HEADER_ALIASES = Map.of("app", "application", "prio", "priority",
            "country", "countries", "label", "labels", "description", "details");

    public static final List<String> STEP_KEYS = List.of("library", "forced", "loop", "use");

    public static final List<String> ITEM_KEYS = List.of("fatal", "timeout", "highlight", "minSimilarity", "typeDelay",
            "waitBefore", "waitAfter", "shot", "shotName");
    private static final Map<String, String> ITEM_ALIASES = Map.of("highlightElement", "highlight", "isFatal", "fatal",
            "screenshot", "shot");

    public static final List<String> PROP_KEYS = List.of("db", "length", "rows", "nature", "cache", "retry",
            "retryPeriod", "rank");
    private static final Map<String, String> PROP_ALIASES = Map.of("database", "db", "rowLimit", "rows",
            "cacheExpire", "cache", "retryNb", "retry");

    /** Element path prefixes: a key=value with one of these keys is a value written without quotes. */
    private static final Set<String> LOCATORS = Set.of("id", "xpath", "css", "name", "link", "class", "data-cerberus",
            "picture", "querySelector".toLowerCase(Locale.ROOT), "text", "coord", "erratum");

    private static final Set<String> CONDITION_OPTION_KEYS = Set.of("timeout", "highlight", "minSimilarity", "typeDelay");

    private final String where;
    private final List<Diagnostic> diagnostics = new ArrayList<>();
    private final Lexer lexer;

    private DocParser(String where) {
        this.where = where;
        this.lexer = new Lexer(where, diagnostics);
    }

    /** Parses every testcase in the text, or throws a {@link DocException} listing all errors. */
    public static List<TcDoc> parse(String text, String where) {
        DocParser parser = new DocParser(where);
        List<TcDoc> docs = parser.run(text == null ? "" : text);
        if (parser.diagnostics.stream().anyMatch(Diagnostic::error)) {
            throw new DocException(parser.diagnostics);
        }
        return docs;
    }

    /** Parses without throwing: syntax problems go to {@code out}, and whatever could be read is returned. */
    public static List<TcDoc> parseLenient(String text, String where, List<Diagnostic> out) {
        DocParser parser = new DocParser(where);
        List<TcDoc> docs = parser.run(text == null ? "" : text);
        out.addAll(parser.diagnostics);
        return docs;
    }

    public static TcDoc parseOne(String text, String where) {
        List<TcDoc> docs = parse(text, where);
        if (docs.size() != 1) {
            throw new DocException(Diagnostic.error(where, 0, "expected exactly one testcase, found " + docs.size(), null));
        }
        return docs.get(0);
    }

    // ---------------------------------------------------------------- logical lines and tokens

    private List<Line> logicalLines(String text) {
        return lexer.logicalLines(text);
    }

    private List<Token> tokens(String s, int lineNo, String[] description) {
        return lexer.tokens(s, lineNo, description);
    }

    private String readString(String s, int start, int[] end, int lineNo) {
        return lexer.readString(s, start, end, lineNo);
    }

    private static int titleColon(String s) {
        return Lexer.titleColon(s);
    }

    // ---------------------------------------------------------------- structure

    private List<TcDoc> run(String text) {
        List<TcDoc> docs = new ArrayList<>();
        TcDoc doc = null;
        TcDoc.Step step = null;
        TcDoc.Item action = null;
        boolean inHeader = false;
        for (Line line : logicalLines(text)) {
            String first = line.text().split("\\s+", 2)[0];
            if (line.indent() == 0 && first.equals("testcase")) {
                doc = header(line);
                docs.add(doc);
                step = null;
                action = null;
                inHeader = true;
                continue;
            }
            if (doc == null) {
                diagnostics.add(Diagnostic.error(where, line.number(), "the document must start with a testcase line",
                        "testcase <Folder>/<Testcase>: <title>"));
                return docs;
            }
            if (first.equals("prop")) {
                doc.props.add(prop(line));
                inHeader = false;
                continue;
            }
            if (first.equals("step") || first.startsWith("step:")) {
                step = step(line);
                doc.steps.add(step);
                action = null;
                inHeader = false;
                continue;
            }
            if (line.indent() == 0) {
                diagnostics.add(Diagnostic.error(where, line.number(), "unexpected line at the left margin: "
                        + Text.truncate(line.text(), 50), "top-level lines are testcase, prop or step; actions are indented by 2, controls by 4"));
                continue;
            }
            if (inHeader) {
                headerAttributes(doc, line);
                continue;
            }
            if (step == null) {
                diagnostics.add(Diagnostic.error(where, line.number(), "an action must sit under a step", "add a line: step: <title>"));
                continue;
            }
            if (line.indent() >= 4) {
                if (action == null) {
                    diagnostics.add(Diagnostic.error(where, line.number(), "a control (indent 4) must follow an action (indent 2)",
                            "indent actions by 2 spaces and their controls by 4"));
                    continue;
                }
                action.controls.add(item(line));
            } else {
                action = item(line);
                step.actions.add(action);
            }
        }
        if (docs.isEmpty() && diagnostics.isEmpty()) {
            diagnostics.add(Diagnostic.error(where, 0, "empty document", "testcase <Folder>/<Testcase>: <title>"));
        }
        return docs;
    }

    private TcDoc header(Line line) {
        TcDoc doc = new TcDoc();
        doc.line = line.number();
        String rest = line.text().substring("testcase".length()).trim();
        int colon = titleColon(rest);
        String refPart = colon < 0 ? rest : rest.substring(0, colon).trim();
        doc.title = colon < 0 ? "" : rest.substring(colon + 1).trim();
        if (refPart.startsWith("\"")) {
            int[] end = new int[1];
            refPart = readString(refPart, 0, end, line.number());
        }
        int slash = refPart.lastIndexOf('/');
        if (slash <= 0 || slash == refPart.length() - 1) {
            diagnostics.add(Diagnostic.error(where, line.number(), "testcase reference must be <Folder>/<Testcase>, got '" + refPart + "'",
                    "testcase DemoShop/DEMO-010: My title"));
            doc.test = refPart;
            doc.testcase = "";
        } else {
            doc.test = refPart.substring(0, slash).trim();
            doc.testcase = refPart.substring(slash + 1).trim();
        }
        if (colon < 0) {
            diagnostics.add(Diagnostic.error(where, line.number(), "missing ': <title>' after the testcase reference",
                    "testcase " + refPart + ": <title>"));
        }
        return doc;
    }

    private void headerAttributes(TcDoc doc, Line line) {
        List<Token> toks = tokens(line.text(), line.number(), null);
        for (int i = 0; i < toks.size(); i++) {
            Token t = toks.get(i);
            if (t.isWord("if")) {
                TcDoc.Cond cond = new TcDoc.Cond();
                i = condition(toks, i, cond, line);
                doc.condition = cond;
                continue;
            }
            if (t.kind() == Kind.KV && t.key().startsWith("if.") && doc.condition != null) {
                conditionOption(doc.condition, t, line);
                continue;
            }
            if (t.kind() != Kind.KV) {
                diagnostics.add(Diagnostic.error(where, line.number(), "header lines hold key=value attributes, got '" + t.text() + "'",
                        "application=DemoShop status=WORKING priority=1 countries=FR,BE"));
                continue;
            }
            String key = HEADER_ALIASES.getOrDefault(t.key(), t.key());
            if (!HEADER_KEYS.contains(key)) {
                diagnostics.add(Diagnostic.error(where, line.number(), "unknown testcase attribute '" + t.key() + "'",
                        "known: " + String.join(", ", Text.closest(t.key(), HEADER_KEYS, 3))));
                continue;
            }
            doc.attrs.put(key, t.text());
        }
    }

    private TcDoc.Prop prop(Line line) {
        TcDoc.Prop p = new TcDoc.Prop();
        p.line = line.number();
        String[] desc = new String[1];
        List<Token> toks = tokens(line.text(), line.number(), desc);
        p.description = desc[0] == null ? "" : desc[0];
        int i = 1;
        if (toks.size() > 1 && toks.get(1).kind() == Kind.KV) {
            p.name = toks.get(1).key();
            p.type = toks.get(1).text();
            i = 2;
        } else if (toks.size() > 3 && toks.get(1).kind() == Kind.WORD && toks.get(2).isWord("=") && toks.get(3).kind() == Kind.WORD) {
            p.name = toks.get(1).text();
            p.type = toks.get(3).text();
            i = 4;
        } else {
            diagnostics.add(Diagnostic.error(where, line.number(), "a property reads: prop <name> = <type> \"value\"",
                    "prop email = text \"alice@demo.test\""));
            p.name = toks.size() > 1 ? toks.get(1).text() : "";
            p.type = "";
            return p;
        }
        boolean flagsStarted = false;
        for (; i < toks.size(); i++) {
            Token t = toks.get(i);
            if (t.kind() == Kind.STR) {
                if (flagsStarted) {
                    diagnostics.add(Diagnostic.error(where, line.number(), "property values must come before its options", null));
                } else if (p.values.size() == 3) {
                    diagnostics.add(Diagnostic.error(where, line.number(), "a property takes at most 3 values", null));
                } else {
                    p.values.add(t.text());
                }
                continue;
            }
            flagsStarted = true;
            if (t.kind() == Kind.KV && t.key().equals("countries")) {
                p.countries = splitList(t.text());
                continue;
            }
            if (t.kind() != Kind.KV) {
                diagnostics.add(Diagnostic.error(where, line.number(), "unexpected '" + t.text() + "' in property " + p.name,
                        "values are quoted: \"" + t.text() + "\"; options are key=value (" + String.join(", ", PROP_KEYS) + ")"));
                continue;
            }
            String key = PROP_ALIASES.getOrDefault(t.key(), t.key());
            if (!PROP_KEYS.contains(key)) {
                diagnostics.add(Diagnostic.error(where, line.number(), "unknown property option '" + t.key() + "'",
                        "known: " + String.join(", ", PROP_KEYS)));
                continue;
            }
            p.flags.put(key, t.text());
        }
        return p;
    }

    private TcDoc.Step step(Line line) {
        TcDoc.Step s = new TcDoc.Step();
        s.line = line.number();
        String rest = line.text().substring(4);
        int colon = titleColon(rest);
        String left = colon < 0 ? rest : rest.substring(0, colon);
        s.title = colon < 0 ? "" : rest.substring(colon + 1).trim();
        if (colon < 0) {
            diagnostics.add(Diagnostic.error(where, line.number(), "a step reads: step <n>: <title>", "step 2: Sign in"));
        }
        List<Token> toks = tokens(left, line.number(), null);
        for (int i = 0; i < toks.size(); i++) {
            Token t = toks.get(i);
            if (i == 0 && t.kind() == Kind.WORD && t.text().matches("\\d+")) {
                continue; // the position is informational: order in the document decides
            }
            if (t.isWord("if")) {
                TcDoc.Cond cond = new TcDoc.Cond();
                i = condition(toks, i, cond, line);
                s.condition = cond;
                continue;
            }
            if (t.kind() == Kind.KV && t.key().startsWith("if.") && s.condition != null) {
                conditionOption(s.condition, t, line);
                continue;
            }
            String key = t.kind() == Kind.KV ? t.key() : t.text();
            if (!STEP_KEYS.contains(key)) {
                diagnostics.add(Diagnostic.error(where, line.number(), "unknown step option '" + key + "'",
                        "known: library, forced, loop=<mode>, use=\"Folder/Testcase#n\", if <condition>"));
                continue;
            }
            s.flags.put(key, t.kind() == Kind.KV ? t.text() : "true");
        }
        return s;
    }

    private TcDoc.Item item(Line line) {
        TcDoc.Item it = new TcDoc.Item();
        it.line = line.number();
        String[] desc = new String[1];
        List<Token> toks = tokens(line.text(), line.number(), desc);
        it.description = desc[0] == null ? "" : desc[0];
        if (toks.isEmpty() || toks.get(0).kind() != Kind.WORD) {
            diagnostics.add(Diagnostic.error(where, line.number(), "an action or control line starts with its name",
                    "click \"id=submit\""));
            it.name = "";
            return it;
        }
        it.name = toks.get(0).text();
        boolean flagsStarted = false;
        for (int i = 1; i < toks.size(); i++) {
            Token t = toks.get(i);
            if (t.kind() == Kind.STR) {
                if (flagsStarted) {
                    diagnostics.add(Diagnostic.error(where, line.number(), "values of " + it.name + " must come right after its name, before options",
                            null));
                } else if (it.values.size() == 3) {
                    diagnostics.add(Diagnostic.error(where, line.number(), it.name + " takes at most 3 values", null));
                } else {
                    it.values.add(t.text());
                }
                continue;
            }
            if (t.kind() == Kind.KV && !flagsStarted && LOCATORS.contains(t.key().toLowerCase(Locale.ROOT))) {
                // An unquoted element path is not ambiguous: take it, and say how it should be written.
                if (it.values.size() < 3) {
                    it.values.add(t.key() + "=" + t.text());
                }
                diagnostics.add(Diagnostic.warning(where, line.number(), "unquoted value " + t.key() + "=" + t.text() + " read as \"" + t.key() + "=" + t.text() + "\"",
                        "quote values"));
                continue;
            }
            flagsStarted = true;
            if (t.isWord("if")) {
                TcDoc.Cond cond = new TcDoc.Cond();
                i = condition(toks, i, cond, line);
                it.condition = cond;
                continue;
            }
            if (t.kind() == Kind.KV && t.key().startsWith("if.") && it.condition != null) {
                conditionOption(it.condition, t, line);
                continue;
            }
            if (t.kind() == Kind.KV && t.key().startsWith("opt.")) {
                it.flags.put(t.key(), t.text());
                continue;
            }
            String key = t.kind() == Kind.KV ? t.key() : t.text();
            key = ITEM_ALIASES.getOrDefault(key, key);
            if (!ITEM_KEYS.contains(key)) {
                String hint;
                if (t.kind() == Kind.KV && Arrays.asList("id", "xpath", "css", "name", "link", "class", "data-cerberus", "picture", "text", "querySelector")
                        .contains(t.key().toLowerCase(Locale.ROOT))) {
                    hint = "values are quoted: \"" + t.key() + "=" + t.text() + "\"";
                } else if (t.kind() == Kind.WORD) {
                    hint = "values are quoted: \"" + t.text() + "\"; options are " + String.join(", ", ITEM_KEYS);
                } else {
                    hint = "known options: " + String.join(", ", Text.closest(key, ITEM_KEYS, 4));
                }
                diagnostics.add(Diagnostic.error(where, line.number(), "unexpected '" + (t.kind() == Kind.KV ? t.key() + "=" + t.text() : t.text())
                        + "' in " + it.name, hint));
                continue;
            }
            it.flags.put(key, t.kind() == Kind.KV ? t.text() : "true");
        }
        return it;
    }

    private int condition(List<Token> toks, int ifIndex, TcDoc.Cond cond, Line line) {
        int i = ifIndex + 1;
        if (i >= toks.size() || toks.get(i).kind() != Kind.WORD) {
            diagnostics.add(Diagnostic.error(where, line.number(), "'if' must be followed by a condition operator",
                    "if ifElementPresent \"id=cookie-banner\""));
            return ifIndex;
        }
        cond.operator = toks.get(i).text();
        while (i + 1 < toks.size() && toks.get(i + 1).kind() == Kind.STR) {
            i++;
            if (cond.values.size() == 3) {
                diagnostics.add(Diagnostic.error(where, line.number(), "a condition takes at most 3 values", null));
            } else {
                cond.values.add(toks.get(i).text());
            }
        }
        return i;
    }

    private void conditionOption(TcDoc.Cond cond, Token t, Line line) {
        String key = t.key().substring(3);
        key = ITEM_ALIASES.getOrDefault(key, key);
        if (!CONDITION_OPTION_KEYS.contains(key)) {
            diagnostics.add(Diagnostic.error(where, line.number(), "unknown condition option '" + t.key() + "'",
                    "known: if.timeout, if.highlight, if.minSimilarity, if.typeDelay"));
            return;
        }
        cond.options.put(key, t.text());
    }

    public static List<String> splitList(String value) {
        List<String> out = new ArrayList<>();
        for (String part : Text.nz(value).split(",")) {
            String p = part.trim();
            if (!p.isEmpty()) {
                out.add(p);
            }
        }
        return out;
    }

    /** For tests and tools: the canonical text of a parsed document. */
    public static Map<String, String> normalize(String text) {
        Map<String, String> out = new LinkedHashMap<>();
        for (TcDoc d : parse(text, "doc")) {
            out.put(d.ref(), DocRenderer.render(d));
        }
        return out;
    }
}
