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
package org.cerberus.core.mcpdelta.read;

import org.cerberus.core.mcpdelta.util.Text;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads a screen the robot saved — the DOM of a web page, or the XML source of an Android or iOS screen —
 * the way a tester would: the outline (what can be targeted, where, with which selector), the elements
 * closest to a selector that matched nothing, and what a selector that did match actually points to.
 * Nothing here knows a framework, a vendor or a site: identity, visibility and layers are inferred from
 * the document itself, with the same rules as the in-browser outline.
 */
public final class PageHints {

    public record Hint(String element, String selector, double score) {
    }

    static final Pattern CONSENT = Pattern.compile("cookie|consent|gdpr|rgpd|dsgvo|lgpd|privacy|privacidad|privacit|confidentialit|datenschutz|traceur|tracker",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern NAMED_CONSENT = Pattern.compile("cookie|consent|gdpr|rgpd", Pattern.CASE_INSENSITIVE);
    static final Pattern TEST_WORDS = Pattern.compile("(^|[-_:])(test|testid|test-id|qa|cy|e2e|automation|auto-id|cerberus|selenium|hook|tid)([-_:]|id|$)",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern STANDARD = Pattern.compile("^(id|class|style|href|src|srcset|sizes|alt|title|type|name|value|placeholder|role|tabindex|"
            + "target|rel|for|action|method|width|height|loading|decoding|fetchpriority|lang|dir|hidden|disabled|checked|selected|readonly|required|"
            + "autocomplete|autofocus|autocapitalize|inputmode|enterkeyhint|maxlength|minlength|pattern|min|max|step|form|accept|multiple|download|"
            + "draggable|spellcheck|contenteditable|translate|nonce|integrity|crossorigin|referrerpolicy|xmlns.*|viewbox|fill|stroke|d|aria-.*|on.*|"
            + "data-src|data-srcset|data-sizes|data-lazy.*|data-bg.*|data-href|data-url|open|inert|content|charset|http-equiv|scrolling|frameborder|"
            + "allow|allowfullscreen|allowtransparency|sandbox|srcdoc|marginwidth|marginheight|align|valign|border|cellpadding|cellspacing|bgcolor|"
            + "color|face|size|cols|rows|wrap|colspan|rowspan|headers|scope|span|start|reversed|label|datetime|cite|coords|shape|usemap|ismap|poster|"
            + "preload|autoplay|controls|loop|muted|playsinline|kind|srclang|default|media|async|defer|ping|hreflang|enctype|novalidate|formaction|"
            + "formmethod|formtarget|list|dirname|accesskey|slot|part|is|itemprop|itemscope|itemtype|itemid|itemref|property|typeof|vocab|prefix|"
            + "about|resource|datatype|rev|xml:.*|xlink:.*|focusable|clip-rule|fill-rule|stroke-.*|transform|x|y|cx|cy|r|rx|ry|points|version|"
            + "data-.*-(dir|direction|state|status|index|position|size|count|length|width|height|theme|variant|mode))$", Pattern.CASE_INSENSITIVE);
    private static final Pattern MIXED = Pattern.compile("[a-z](?=\\d)|\\d(?=[a-z])", Pattern.CASE_INSENSITIVE);
    private static final Set<String> SKIP = Set.of("script", "style", "template", "noscript", "meta", "link", "head", "title", "base");
    private static final Pattern INTERACTIVE = Pattern.compile("^(a|button|input|select|textarea|summary|label|option|details)$");
    private static final Pattern ROLES = Pattern.compile("^(button|link|tab|menuitem|menuitemcheckbox|menuitemradio|checkbox|radio|switch|option|"
            + "combobox|textbox|searchbox|slider|spinbutton|treeitem)$");
    private static final Pattern QUOTED = Pattern.compile("\"([^\"]+)\"|'([^']+)'");

    private PageHints() {
    }

    // ------------------------------------------------------------------ shared rules

    /** The first part of an address or a selector that is a generated id (/product/01M3XK5ZKF…), or null. */
    public static String generatedPart(String value) {
        for (String p : Text.nz(value).split("[/?#=&'\"\\s\\[\\]()@,]+")) {
            if (p.length() >= 8 && hashy(p)) {
                return p;
            }
        }
        return null;
    }

    /** A value no one wrote by hand: hashes, UUIDs, long counters, generated ids (":r1:"). */
    public static boolean hashy(String v) {
        if (v == null || v.isEmpty()) {
            return true;
        }
        if (v.matches("(?is).*[0-9a-f]{8}-[0-9a-f]{4}-.*") || v.matches("(?s).*\\d{5,}.*") || v.startsWith(":") || v.endsWith(":")) {
            return true;
        }
        for (String k : v.split("[^A-Za-z0-9]+")) {
            if (k.length() >= 5 && k.matches(".*\\d.*") && k.matches("(?i).*[a-z].*")) {
                Matcher m = MIXED.matcher(k);
                int n = 0;
                while (m.find()) {
                    n++;
                }
                if (n >= 3) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean codey(String v) {
        return v.matches("(?s).*([{}();<>]|=>|\\bfunction\\b).*");
    }

    /** XPath normalize-space(): only ASCII whitespace collapses, a no-break space stays what it is. */
    static String ws(String s) {
        return s == null ? "" : s.replaceAll("[ \\t\\r\\n\\f]+", " ").trim();
    }

    static String lit(String s) {
        if (!s.contains("'")) {
            return "'" + s + "'";
        }
        if (!s.contains("\"")) {
            return "\"" + s + "\"";
        }
        return "concat('" + String.join("',\"'\",'", s.split("'", -1)) + "')";
    }

    private static boolean isXml(String src) {
        String head = src.stripLeading();
        head = head.substring(0, Math.min(head.length(), 400));
        return head.startsWith("<?xml") && !head.toLowerCase(Locale.ROOT).contains("<html") || head.startsWith("<hierarchy")
                || head.contains("<AppiumAUT") || head.contains("<XCUIElementType");
    }

    private static Document parse(String src) {
        return isXml(src) ? Jsoup.parse(src, "", Parser.xmlParser()) : Jsoup.parse(src);
    }

    // ------------------------------------------------------------------ a parsed page

    /** A web page with its identity statistics, computed once. */
    private static final class Web {
        final Document doc;
        final List<Element> all = new ArrayList<>();
        final List<String> identityAttrs;
        final Map<Element, String> texts = new IdentityHashMap<>();
        final Map<String, Integer> byText = new HashMap<>();

        Web(Document doc) {
            this.doc = doc;
            Element root = doc.body() == null ? doc : doc.body();
            for (Element e : root.getAllElements()) {
                String t = e.normalName();
                if (SKIP.contains(t) || e.closest("svg") != null && !t.equals("svg")) {
                    continue;
                }
                all.add(e);
            }
            Map<String, int[]> stats = new LinkedHashMap<>();
            Map<String, Set<String>> values = new HashMap<>();
            for (Element e : all) {
                for (org.jsoup.nodes.Attribute a : e.attributes()) {
                    String k = a.getKey().toLowerCase(Locale.ROOT);
                    String v = a.getValue();
                    if (STANDARD.matcher(k).matches() || hashy(k)) {
                        continue;
                    }
                    int[] s = stats.computeIfAbsent(k, x -> new int[3]); // n, bad, distinct
                    s[0]++;
                    if (v.isEmpty() || hashy(v) || codey(v) || v.length() > 60 || v.matches("(?i)\\d+|true|false|null|undefined")) {
                        s[1]++;
                        continue;
                    }
                    if (values.computeIfAbsent(k, x -> new LinkedHashSet<>()).add(v)) {
                        s[2]++;
                    }
                }
            }
            List<String> ids = new ArrayList<>();
            for (Map.Entry<String, int[]> e : stats.entrySet()) {
                int[] s = e.getValue();
                if (s[1] > s[0] / 2) {
                    continue;
                }
                // A test hook, or a data- attribute used as a naming scheme: on several elements, mostly distinct values.
                if (TEST_WORDS.matcher(e.getKey()).find() || e.getKey().startsWith("data-") && s[0] - s[1] >= 2
                        && s[2] >= 0.6 * (s[0] - s[1]) && s[0] <= Math.max(20, all.size() / 4)) {
                    ids.add(e.getKey());
                }
            }
            ids.sort(Comparator.comparing(a -> TEST_WORDS.matcher(a).find() ? 0 : 1));
            identityAttrs = ids;
            for (Element e : all) {
                byText.merge(e.normalName() + "\u0001" + text(e), 1, Integer::sum);
            }
        }

        /** The XPath string-value, normalized: what normalize-space() compares. */
        String text(Element e) {
            return texts.computeIfAbsent(e, x -> ws(x.wholeText()));
        }

        List<String[]> identity(Element e) {
            List<String[]> out = new ArrayList<>();
            for (String a : identityAttrs) {
                String v = e.attr(a);
                if (!v.isEmpty() && !hashy(v) && !codey(v) && v.length() <= 60) {
                    out.add(new String[]{a, v});
                }
            }
            return out;
        }

        int countAttr(String attr, String value) {
            int n = 0;
            for (Element e : all) {
                if (e.hasAttr(attr) && e.attr(attr).equals(value)) {
                    n++;
                }
            }
            return n;
        }

        List<Element> withAttr(String tag, String attr, String value) {
            List<Element> out = new ArrayList<>();
            for (Element e : all) {
                if ((tag == null || e.normalName().equals(tag)) && e.hasAttr(attr) && e.attr(attr).equals(value)) {
                    out.add(e);
                }
            }
            return out;
        }

        List<Element> withText(String tag, String text) {
            List<Element> out = new ArrayList<>();
            if (byText.getOrDefault(tag + "\u0001" + text, 0) == 0) {
                return out;
            }
            for (Element e : all) {
                if (e.normalName().equals(tag) && text(e).equals(text)) {
                    out.add(e);
                }
            }
            return out;
        }
    }

    // ------------------------------------------------------------------ web: visibility, region, selector

    private static boolean hidden(Element e) {
        if (e.normalName().equals("input") && "hidden".equalsIgnoreCase(e.attr("type"))) {
            return true;
        }
        for (Element p = e; p != null; p = p.parent()) {
            if (p.hasAttr("hidden") || "true".equalsIgnoreCase(p.attr("aria-hidden")) || p.hasAttr("inert")) {
                return true;
            }
            String st = p.attr("style").replace(" ", "").toLowerCase(Locale.ROOT);
            if (st.contains("display:none") || st.contains("visibility:hidden") || st.matches(".*(^|;)opacity:0(;|$).*")) {
                return true;
            }
            if (p != e && p.normalName().equals("details") && !p.hasAttr("open")) {
                Element summary = e.closest("summary");
                if (summary == null || summary.parent() != p) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean modal(Element p) {
        String rl = p.attr("role");
        return p.normalName().equals("dialog") && p.hasAttr("open") || rl.equals("dialog") || rl.equals("alertdialog")
                || "true".equals(p.attr("aria-modal"));
    }

    private static boolean pinned(Element p) {
        String st = p.attr("style").replace(" ", "").toLowerCase(Locale.ROOT);
        return st.contains("position:fixed") || st.contains("position:sticky");
    }

    private static String label(Element p) {
        String al = p.attr("aria-label");
        return !p.id().isEmpty() && !hashy(p.id()) ? " #" + p.id() : al.isEmpty() ? "" : " \"" + Text.truncate(ws(al), 30) + "\"";
    }

    /** Same regions as the live outline; layers are known only from the markup (no computed style here). */
    private static String region(Web w, Element e) {
        Element layer = null;
        for (Element p = e; p != null && p != w.doc.body(); p = p.parent()) {
            if (modal(p) || pinned(p) || NAMED_CONSENT.matcher(p.id() + " " + p.className() + " " + p.attr("aria-label")).find()) {
                layer = p;
            }
        }
        if (layer != null && layer.selectFirst("main,[role=main]") == null) {
            String text = w.text(layer);
            boolean named = NAMED_CONSENT.matcher(layer.id() + " " + layer.className() + " " + layer.attr("aria-label")).find();
            if (text.length() < 1500 && (named || CONSENT.matcher(text).find())) {
                return "cookie/consent banner";
            }
            if (modal(layer)) {
                return "dialog" + label(layer);
            }
        }
        for (Element p = e; p != null; p = p.parent()) {
            String t = p.normalName();
            String role = p.attr("role");
            if (t.equals("dialog") || role.equals("dialog") || role.equals("alertdialog")) {
                return "dialog" + label(p);
            }
            Element par = p.parent();
            boolean top = par == null || par.closest("main,article,section,aside,[role=main]") == null;
            if (t.equals("header") && top || role.equals("banner")) {
                return "header";
            }
            if (t.equals("nav") || role.equals("navigation")) {
                return "navigation" + label(p);
            }
            if (t.equals("footer") && top || role.equals("contentinfo")) {
                return "footer";
            }
            if (t.equals("main") || role.equals("main")) {
                return "main";
            }
            if (t.equals("aside") && top || role.equals("complementary")) {
                return "aside";
            }
            if (role.equals("search")) {
                return "search";
            }
        }
        return "page";
    }

    private static final String LANDMARK = "header,nav,main,footer,aside,form,dialog,[role=dialog],[role=navigation],[role=banner],"
            + "[role=contentinfo],[role=main],[role=search],[role=menu],[role=tabpanel]";

    /** Scopes that can make a selector unique, nearest first: the layer the element sits in, then its landmarks. */
    private static List<Element> scopes(Element e) {
        List<Element> out = new ArrayList<>();
        Element layer = null;
        for (Element p = e.parent(); p != null; p = p.parent()) {
            if (modal(p) || pinned(p) || NAMED_CONSENT.matcher(p.id() + " " + p.className() + " " + p.attr("aria-label")).find()) {
                layer = p;
            }
        }
        if (layer != null && layer.selectFirst("main,[role=main]") == null) {
            out.add(layer);
        }
        for (Element p = e.parent(); p != null && out.size() < 6; p = p.parent()) {
            if (p.is(LANDMARK) || !p.id().isEmpty() && !hashy(p.id())
                    && p.id().matches("(?i).*(menu|nav|panel|modal|drawer|dropdown|popover|list|grid|card|form|section).*")) {
                if (!out.contains(p)) {
                    out.add(p);
                }
            }
        }
        return out;
    }

    /** XPath prefixes for a scope, shortest first, each with the elements it designates. */
    private static Map<String, List<Element>> scopePrefixes(Web w, Element l) {
        Map<String, List<Element>> out = new LinkedHashMap<>();
        String t = l.normalName();
        out.put("//" + t, new ArrayList<>(w.doc.getElementsByTag(t)));
        if (!l.attr("aria-label").isEmpty()) {
            out.put("//" + t + "[@aria-label=" + lit(l.attr("aria-label")) + "]", w.withAttr(t, "aria-label", l.attr("aria-label")));
        }
        if (!l.id().isEmpty() && !hashy(l.id())) {
            out.put("//*[@id=" + lit(l.id()) + "]", w.withAttr(null, "id", l.id()));
        }
        for (String[] id : w.identity(l)) {
            out.put("//" + t + "[@" + id[0] + "=" + lit(id[1]) + "]", w.withAttr(t, id[0], id[1]));
        }
        if (!l.attr("role").isEmpty()) {
            out.put("//*[@role=" + lit(l.attr("role")) + "]", w.withAttr(null, "role", l.attr("role")));
        }
        return out;
    }

    private static List<org.jsoup.nodes.TextNode> descendantsText(Element e) {
        List<org.jsoup.nodes.TextNode> out = new ArrayList<>();
        e.traverse((node, depth) -> {
            if (node instanceof org.jsoup.nodes.TextNode tn) {
                out.add(tn);
            }
        });
        return out;
    }

    /** The first text node not hidden: the label a reader sees when an element also holds hidden or glued text. */
    private static String firstText(Element e) {
        for (org.jsoup.nodes.TextNode n : descendantsText(e)) {
            String t = ws(n.getWholeText());
            Element p = n.parent();
            if (t.length() >= 2 && p != null && !hidden(p)) {
                return t;
            }
        }
        return "";
    }

    /** Elements of a tag that have a descendant text node reading exactly this. */
    private static List<Element> withTextNode(Web w, String tag, String text) {
        List<Element> out = new ArrayList<>();
        for (Element e : w.all) {
            if (e.normalName().equals(tag) && w.text(e).contains(text)
                    && descendantsText(e).stream().anyMatch(n -> ws(n.getWholeText()).equals(text))) {
                out.add(e);
            }
        }
        return out;
    }

    /** The selector Cerberus will use for this element, unique in the page wherever possible. */
    private static String selector(Web w, Element e) {
        String t = e.normalName();
        for (String[] id : w.identity(e)) {
            if (w.countAttr(id[0], id[1]) == 1) {
                return id[0].equals("data-cerberus") ? "data-cerberus=" + id[1] : "css=[" + id[0] + "=\"" + id[1].replace("\"", "\\\"") + "\"]";
            }
        }
        if (!e.id().isEmpty() && !hashy(e.id()) && w.countAttr("id", e.id()) == 1) {
            return "id=" + e.id();
        }
        String nm = e.attr("name");
        if (!nm.isEmpty() && !hashy(nm) && w.countAttr("name", nm) == 1) {
            return "name=" + nm;
        }
        String full = w.text(e);
        String own = firstText(e);
        // "Clean" text: one visible piece of text, so normalize-space() reads what a person reads.
        boolean clean = !full.isEmpty() && full.length() <= 60
                && descendantsText(e).stream().filter(n -> !ws(n.getWholeText()).isEmpty()).count() <= 1;
        if (t.equals("a") && clean && !full.contains("\u00a0") && w.withText("a", full).size() == 1) {
            return "link=" + full;
        }
        List<String[]> preds = new ArrayList<>(); // {predicate, kind, value}
        if (clean) {
            preds.add(new String[]{"normalize-space()=" + lit(full), "text", full});
        }
        String href = e.attr("href");
        if (t.equals("a") && !href.isEmpty() && href.length() <= 100 && !href.toLowerCase(Locale.ROOT).startsWith("javascript:")
                && generatedPart(href) == null) {
            preds.add(new String[]{"@href=" + lit(href), "href", href});
        }
        for (String a : new String[]{"aria-label", "placeholder"}) {
            if (!e.attr(a).isEmpty()) {
                preds.add(new String[]{"@" + a + "=" + lit(e.attr(a)), a, e.attr(a)});
            }
        }
        if (full.isEmpty() && !e.attr("title").isEmpty()) {
            preds.add(new String[]{"@title=" + lit(e.attr("title")), "title", e.attr("title")});
        }
        if (!clean && !own.isEmpty() && own.length() <= 60) {
            preds.add(new String[]{".//text()[normalize-space()=" + lit(own) + "]", "textnode", own});
        }
        // A field inside its label: the label's words reach it ("//label[normalize-space()='Pliers']//input").
        Element label = e.parent() == null ? null : e.parent().closest("label");
        if (label != null) {
            String lt = w.text(label);
            if (!lt.isEmpty() && lt.length() <= 60 && w.withText("label", lt).size() == 1 && label.getElementsByTag(t).size() == 1) {
                return "xpath=//label[normalize-space()=" + lit(lt) + "]//" + t;
            }
        }
        for (String a : new String[]{"aria-controls", "value", "data-value", "title", "alt", "for", "type"}) {
            String v = e.attr(a);
            if (!v.isEmpty() && !hashy(v) && v.length() <= 60 && preds.stream().noneMatch(p -> p[1].equals(a))) {
                preds.add(new String[]{"@" + a + "=" + lit(v), a, v});
            }
        }
        List<Element> scopes = null;
        String first = null;
        List<Element> firstMatches = null;
        for (String[] p : preds) {
            List<Element> found = p[1].equals("text") ? w.withText(t, p[2]) : p[1].equals("textnode") ? withTextNode(w, t, p[2])
                    : w.withAttr(t, p[1], p[2]);
            String xp = "//" + t + "[" + p[0] + "]";
            if (found.size() == 1) {
                return "xpath=" + xp;
            }
            if (found.isEmpty()) {
                continue;
            }
            if (scopes == null) {
                scopes = scopes(e);
            }
            for (Element l : scopes) {
                for (Map.Entry<String, List<Element>> pre : scopePrefixes(w, l).entrySet()) {
                    if (pre.getValue().stream().noneMatch(anc -> e.parents().contains(anc))) {
                        continue;
                    }
                    int n = 0;
                    for (Element f : found) {
                        Set<Element> parents = java.util.Collections.newSetFromMap(new IdentityHashMap<>());
                        parents.addAll(f.parents());
                        if (pre.getValue().stream().anyMatch(parents::contains)) {
                            n++;
                        }
                    }
                    if (n == 1) {
                        return "xpath=" + pre.getKey() + xp;
                    }
                }
            }
            if (first == null) {
                first = xp;
                firstMatches = found;
            }
        }
        if (first != null) {
            int k = firstMatches.indexOf(e);
            if (k >= 0) {
                return "xpath=(" + first + ")[" + (k + 1) + "]";
            }
        }
        return "";
    }

    private static boolean interesting(Web w, Element e) {
        String t = e.normalName();
        if (t.equals("input") && "hidden".equalsIgnoreCase(e.attr("type"))) {
            return false;
        }
        if (t.equals("a") && !e.hasAttr("href") && e.attr("role").isEmpty()) {
            return false;
        }
        if (INTERACTIVE.matcher(t).matches() || ROLES.matcher(e.attr("role")).matches() || e.hasAttr("onclick") || e.hasAttr("aria-expanded")
                || e.hasAttr("aria-haspopup") || "true".equals(e.attr("contenteditable"))) {
            return true;
        }
        if (t.matches("h[1-3]|iframe|frame")) {
            return true;
        }
        if (t.equals("html") || t.equals("body")) {
            return false;
        }
        boolean named = !w.identity(e).isEmpty() || !e.id().isEmpty() && !hashy(e.id());
        String tx = w.text(e);
        return named && e.children().size() <= 3 && !tx.isEmpty() && tx.length() <= 60;
    }

    private static String shownText(Web w, Element e) {
        String t = e.normalName();
        String x;
        if (t.equals("input") || t.equals("textarea") || t.equals("select")) {
            String lab = "";
            if (!e.id().isEmpty()) {
                Element l = w.doc.selectFirst("label[for=" + cssValue(e.id()) + "]");
                lab = l == null ? "" : w.text(l);
            }
            x = firstNonEmpty(e.attr("aria-label"), lab, e.attr("placeholder"),
                    e.attr("type").matches("(?i)submit|button|reset") ? e.attr("value") : "", e.attr("title"));
        } else if (t.equals("iframe") || t.equals("frame")) {
            x = firstNonEmpty(e.attr("title"), e.attr("name"));
        } else {
            x = firstNonEmpty(w.text(e), e.attr("aria-label"), e.attr("alt"), e.attr("title"));
            if (x.isEmpty()) {
                Element im = e.selectFirst("img[alt],[aria-label]");
                x = im == null ? "" : firstNonEmpty(im.attr("alt"), im.attr("aria-label"));
            }
        }
        x = ws(x);
        return x.length() > 70 ? x.substring(0, 69) + "…" : x;
    }

    private static String cssValue(String v) {
        return "\"" + v.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    private static String firstNonEmpty(String... vs) {
        for (String v : vs) {
            if (v != null && !ws(v).isEmpty()) {
                return v;
            }
        }
        return "";
    }

    private static Outline.Item webItem(Web w, Element e) {
        String t = e.normalName();
        Map<String, String> a = new LinkedHashMap<>();
        for (String k : new String[]{"name", "type", "href", "aria-expanded", "aria-controls", "placeholder", "src"}) {
            String v = e.attr(k);
            if (!v.isEmpty() && !(k.equals("src") && !t.matches("i?frame"))) {
                a.put(k, v);
            }
        }
        if (t.equals("input") && e.attr("type").matches("(?i)checkbox|radio")) {
            a.put("checked", String.valueOf(e.hasAttr("checked")));
        }
        if (e.hasAttr("disabled")) {
            a.put("disabled", "true");
        }
        for (String[] id : w.identity(e)) {
            a.put(id[0], id[1]);
        }
        return new Outline.Item(region(w, e), t, shownText(w, e), a, selector(w, e), !hidden(e), null, false);
    }

    // ------------------------------------------------------------------ outline

    /**
     * The screen as a tester sees it, in the same format as the live outline: title, layers, visible
     * elements by region with their selector, hidden ones in brief. Web pages and mobile screens alike.
     */
    public static String outline(String source, int max) {
        if (source == null || source.isBlank()) {
            return "";
        }
        Document d = parse(source);
        if (isXml(source)) {
            return Mobile.outline(d, max);
        }
        Web w = new Web(d);
        List<Outline.Item> visible = new ArrayList<>();
        List<Outline.Item> hiddenOnes = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();
        int total = 0;
        for (Element e : w.all) {
            if (!interesting(w, e)) {
                continue;
            }
            total++;
            Outline.Item it = webItem(w, e);
            if (!seen.add(it.tag() + "|" + it.text() + "|" + it.attrs().getOrDefault("href", "") + "|" + e.id())) {
                continue;
            }
            (it.visible() ? visible : hiddenOnes).add(it);
        }
        List<Outline.Item> items = new ArrayList<>(visible.subList(0, Math.min(max, visible.size())));
        items.addAll(hiddenOnes.subList(0, Math.min(hiddenOnes.size(), Math.max(0, max - items.size()))));
        String title = d.title().isBlank() ? "untitled page" : "title " + Text.quote(ws(d.title()));
        return Outline.format(title, items, List.of(), List.of(), total > max, Integer.MAX_VALUE, null, null);
    }

    // ------------------------------------------------------------------ selector checks on a saved screen

    private record Wanted(String attr, List<String> tokens, String raw, String kind, String body) {
    }

    private static Wanted wanted(String selector) {
        String s = Text.nz(selector).trim();
        List<String> tokens = new ArrayList<>();
        String attr = null;
        int eq = s.indexOf('=');
        String prefix = eq > 0 ? s.substring(0, eq).toLowerCase(Locale.ROOT) : "";
        String body = eq > 0 ? s.substring(eq + 1) : s;
        switch (prefix) {
            case "id", "name", "data-cerberus", "class" -> {
                attr = prefix;
                tokens.add(body);
            }
            case "link", "text" -> tokens.add(body);
            case "xpath" -> {
                Matcher q = QUOTED.matcher(body);
                while (q.find()) {
                    tokens.add(q.group(1) != null ? q.group(1) : q.group(2));
                }
                Matcher at = Pattern.compile("@([\\w:-]+)\\s*=\\s*['\"]([^'\"]+)").matcher(body);
                if (at.find()) {
                    attr = at.group(1);
                }
            }
            case "css", "queryselector" -> {
                Matcher id = Pattern.compile("#([\\w-]+)").matcher(body);
                while (id.find()) {
                    tokens.add(id.group(1));
                    attr = "id";
                }
                Matcher cls = Pattern.compile("\\.([\\w-]+)").matcher(body);
                while (cls.find()) {
                    tokens.add(cls.group(1));
                }
                Matcher av = Pattern.compile("\\[([\\w:-]+)[~|^$*]?=['\"]?([^'\"\\]]+)").matcher(body);
                while (av.find()) {
                    tokens.add(av.group(2));
                    attr = attr == null ? av.group(1) : attr;
                }
            }
            default -> {
                prefix = "id";
                tokens.add(s);
            }
        }
        tokens.removeIf(String::isBlank);
        return new Wanted(attr, tokens, s, prefix, body);
    }

    /** What the selector matches in the saved screen, evaluated the way the robot would; null when it cannot be evaluated here. */
    private static List<Element> matches(Document d, Wanted w) {
        try {
            return switch (w.kind()) {
                case "id" -> new ArrayList<>(d.select("[id=" + cssValue(w.body()) + "],[resource-id=" + cssValue(w.body()) + "],[name="
                        + cssValue(w.body()) + "]").stream().filter(e -> e.id().equals(w.body()) || e.attr("resource-id").equals(w.body())
                        || e.attr("name").equals(w.body()) && e.tagName().startsWith("XCUIElementType")).toList());
                case "name" -> new ArrayList<>(d.select("[name=" + cssValue(w.body()) + "]"));
                case "data-cerberus" -> new ArrayList<>(d.select("[data-cerberus=" + cssValue(w.body()) + "]"));
                case "class" -> new ArrayList<>(d.getElementsByClass(w.body()));
                case "css" -> new ArrayList<>(d.select(w.body()));
                case "xpath" -> new ArrayList<>(d.selectXpath(w.body()));
                case "link" -> new ArrayList<>(d.select("a").stream().filter(a -> ws(a.text()).equals(ws(w.body()))).toList());
                default -> null;
            };
        } catch (RuntimeException e) {
            return null;
        }
    }

    /** Best candidates for a selector that matched nothing, best first. */
    public static List<Hint> forSelector(String selector, String source, int max) {
        Wanted w = wanted(selector);
        if (w.tokens().isEmpty() || source == null || source.isEmpty()) {
            return List.of();
        }
        Document d = parse(source);
        boolean xml = isXml(source);
        Web web = xml ? null : new Web(d);
        List<Element> pool = xml ? d.getAllElements() : web.all;
        List<Hint> hints = new ArrayList<>();
        for (Element e : pool) {
            String tag = e.tagName();
            String text = e.children().size() <= 3 ? ws(e.wholeText()) : ws(e.ownText());
            if (text.length() > 80) {
                text = text.substring(0, 80);
            }
            double score = score(w, e, text);
            if (score >= 0.55) {
                String sel = xml ? Mobile.selector(d, e) : selector(web, e);
                hints.add(new Hint(render(tag, e, text, xml), sel.isEmpty() ? null : sel, score));
            }
        }
        hints.sort(Comparator.comparingDouble(Hint::score).reversed());
        List<Hint> out = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();
        for (Hint h : hints) {
            if (seen.add(h.element()) && out.size() < max) {
                out.add(h);
            }
        }
        return out;
    }

    /** The text an element shows, for a selector that matched but whose text was not the expected one. */
    public static String textOf(String selector, String source) {
        Wanted w = wanted(selector);
        if (source == null || source.isEmpty()) {
            return null;
        }
        Document d = parse(source);
        List<Element> found = matches(d, w);
        if (found == null || found.isEmpty()) {
            return null;
        }
        Element e = found.get(0);
        String t = ws(e.hasAttr("text") ? e.attr("text") : e.hasAttr("label") ? e.attr("label") : e.wholeText());
        return Text.truncate(t, 80);
    }

    /**
     * The failing selector evaluated on the saved screen, the way the robot evaluates it. Null when it
     * cannot be evaluated here, or when it points to exactly one visible element and the robot found it.
     * The robot acts on the first match: a hidden duplicate first (a closed menu, a mobile copy of the
     * same link, search suggestions) explains "not visible" or "could not find", and so does an element
     * that is there in the end but was not yet there when the robot looked.
     */
    public static String diagnose(String selector, String source, boolean robotFoundNothing) {
        if (source == null || source.isEmpty()) {
            return null;
        }
        Wanted w = wanted(selector);
        Document d = parse(source);
        List<Element> found = matches(d, w);
        if (found == null) {
            return null;
        }
        if (found.isEmpty()) {
            return "";
        }
        boolean xml = isXml(source);
        Web web = xml ? null : new Web(d);
        java.util.function.Predicate<Element> shown = e -> xml ? Mobile.visible(e) : !hidden(e);
        List<Element> visible = found.stream().filter(shown).toList();
        boolean firstHidden = !shown.test(found.get(0));
        if (found.size() == 1 && !firstHidden) {
            if (!robotFoundNothing) {
                return null;
            }
            return selector + " is in the saved page, visible" + (xml ? "" : " in " + region(web, found.get(0)))
                    + ": it was not there yet when the robot looked (wait for it first), or it sits in an iframe or a shadow root";
        }
        StringBuilder sb = new StringBuilder(selector).append(" matches ").append(found.size()).append(" element(s) in the saved page");
        if (firstHidden) {
            sb.append("; the first one, the one the robot uses, is hidden").append(xml ? "" : " (in " + region(web, found.get(0)) + ")");
        }
        if (!visible.isEmpty()) {
            Element v = visible.get(0);
            String sel = xml ? Mobile.selector(d, v) : selector(web, v);
            sb.append("; visible: ").append(Text.quote(Text.truncate(xml ? ws(firstNonEmpty(v.attr("text"), v.attr("label"), v.attr("name")))
                    : shownText(web, v), 50))).append(xml ? "" : " in " + region(web, v)).append(sel.isEmpty() ? "" : " → " + sel);
        } else {
            sb.append("; none is visible in the saved page (something must open or scroll first)");
        }
        return sb.toString();
    }

    /** Characters that look like another one: typography, not content. */
    private static final Map<Character, String[]> LOOKALIKE = Map.ofEntries(
            Map.entry('\u00a0', new String[]{" ", "a no-break space (\\u00a0)"}),
            Map.entry('\u202f', new String[]{" ", "a narrow no-break space (\\u202f)"}),
            Map.entry('\u2007', new String[]{" ", "a figure space (\\u2007)"}),
            Map.entry('\u2009', new String[]{" ", "a thin space (\\u2009)"}),
            Map.entry('\u200b', new String[]{"", "a zero-width space (\\u200b)"}),
            Map.entry('\u2019', new String[]{"'", "a typographic apostrophe ’"}),
            Map.entry('\u2018', new String[]{"'", "a typographic quote ‘"}),
            Map.entry('\u02bc', new String[]{"'", "a modifier apostrophe ʼ"}),
            Map.entry('\u201c', new String[]{"\"", "a typographic quote “"}),
            Map.entry('\u201d', new String[]{"\"", "a typographic quote ”"}),
            Map.entry('\u2013', new String[]{"-", "an en dash –"}),
            Map.entry('\u2014', new String[]{"-", "an em dash —"}),
            Map.entry('\u2011', new String[]{"-", "a non-breaking hyphen ‑"}),
            Map.entry('\u2026', new String[]{"...", "an ellipsis …"}));

    private static String canon(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        for (char c : s.toCharArray()) {
            String[] l = LOOKALIKE.get(c);
            sb.append(l == null ? String.valueOf(c) : l[0].length() == 1 ? l[0] : "\u0001");
        }
        return sb.toString();
    }

    /**
     * When a text in the selector is on the screen but written with other characters that look the same
     * (a no-break space before "?" or ":", a typographic apostrophe, a dash), says which: the most common
     * reason a text selector "cannot be found" on a page that visibly shows it, in any language.
     */
    public static String typography(String selector, String source) {
        Wanted w = wanted(selector);
        if (w.tokens().isEmpty() || source == null || source.isEmpty()) {
            return null;
        }
        Document d = parse(source);
        List<String> texts = new ArrayList<>();
        for (Element e : d.getAllElements()) {
            if (SKIP.contains(e.normalName())) {
                continue;
            }
            // Raw text nodes: jsoup's own text() turns a no-break space into a space, the very difference looked for.
            StringBuilder own = new StringBuilder();
            for (org.jsoup.nodes.TextNode n : e.textNodes()) {
                own.append(n.getWholeText());
            }
            if (!ws(own.toString()).isEmpty()) {
                texts.add(ws(own.toString()));
            }
            for (org.jsoup.nodes.Attribute a : e.attributes()) {
                if (!a.getValue().isEmpty() && a.getValue().length() <= 200) {
                    texts.add(a.getValue());
                }
            }
        }
        for (String token : w.tokens()) {
            String ct = canon(token);
            if (ct.contains("\u0001") || token.length() < 2) {
                continue;
            }
            for (String raw : texts) {
                if (raw.contains(token) || raw.length() != canon(raw).length()) {
                    continue;
                }
                int i = canon(raw).indexOf(ct);
                if (i < 0) {
                    continue;
                }
                String found = raw.substring(i, i + ct.length());
                Set<String> kinds = new LinkedHashSet<>();
                for (int k = 0; k < found.length(); k++) {
                    if (found.charAt(k) != token.charAt(k)) {
                        String[] l = LOOKALIKE.get(found.charAt(k));
                        kinds.add(l == null ? "another character" : l[1]);
                    }
                }
                return "the screen writes " + Text.quote(found) + " where the selector has " + Text.quote(token) + ": " + String.join(", ", kinds)
                        + " — write it that way (\\u00a0 and the like are accepted inside quotes)";
            }
        }
        return null;
    }

    private static double score(Wanted w, Element e, String text) {
        double best = 0;
        for (String token : w.tokens()) {
            String tk = token.toLowerCase(Locale.ROOT);
            for (org.jsoup.nodes.Attribute a : e.attributes()) {
                String k = a.getKey();
                String v = a.getValue();
                if (v.isEmpty() || k.equals("style") || k.startsWith("on") || k.equals("bounds")) {
                    continue;
                }
                List<String> values = k.equals("class") ? List.of(v.split("\\s+")) : List.of(v);
                for (String val : values) {
                    double s = similarity(tk, val.toLowerCase(Locale.ROOT));
                    if (w.attr() != null && w.attr().equals(k)) {
                        s = Math.min(1.0, s + 0.05);
                    }
                    best = Math.max(best, s);
                }
            }
            if (!text.isEmpty()) {
                best = Math.max(best, similarity(tk, text.toLowerCase(Locale.ROOT)) - 0.05);
            }
        }
        return best;
    }

    /**
     * 1 for equal. Otherwise the edit-distance ratio, lifted when the two share words: "cart-count" is far
     * closer to "basket-count" (same shape, one word in common) than to "nav-cart" (one word, nothing else).
     */
    static double similarity(String a, String b) {
        if (a.isEmpty() || b.isEmpty()) {
            return 0;
        }
        if (a.equals(b)) {
            return 1.0;
        }
        double lev = 1.0 - (double) Text.levenshtein(a, b) / Math.max(a.length(), b.length());
        Set<String> wa = new LinkedHashSet<>(List.of(a.split("[^a-z0-9]+")));
        Set<String> wb = new LinkedHashSet<>(List.of(b.split("[^a-z0-9]+")));
        wa.removeIf(x -> x.length() < 2);
        wb.removeIf(x -> x.length() < 2);
        Set<String> common = new LinkedHashSet<>(wa);
        common.retainAll(wb);
        Set<String> union = new LinkedHashSet<>(wa);
        union.addAll(wb);
        double jaccard = union.isEmpty() ? 0 : (double) common.size() / union.size();
        double score = lev;
        if (!common.isEmpty()) {
            score = Math.max(score, 0.5 * jaccard + 0.5 * lev + 0.2);
        }
        if (b.contains(a) || a.contains(b)) {
            score = Math.max(score, 0.6 + 0.3 * Math.min(a.length(), b.length()) / Math.max(a.length(), b.length()));
        }
        return Math.min(score, 0.99);
    }

    private static final Set<String> SHOWN_ATTRS = Set.of("id", "name", "type", "value", "placeholder", "aria-label", "data-cerberus", "href",
            "title", "role", "for", "resource-id", "content-desc", "text", "label");

    private static String render(String tag, Element e, String text, boolean xml) {
        StringBuilder sb = new StringBuilder("<").append(xml ? Mobile.shortType(e) : tag);
        for (org.jsoup.nodes.Attribute a : e.attributes()) {
            String k = a.getKey().toLowerCase(Locale.ROOT);
            if ((SHOWN_ATTRS.contains(k) || TEST_WORDS.matcher(k).find()) && !(xml && k.equals("text"))) {
                sb.append(' ').append(k).append("=\"").append(Text.truncate(a.getValue(), 40)).append('"');
            }
        }
        sb.append('>');
        if (!text.isEmpty()) {
            sb.append(Text.truncate(text, 50)).append("</").append(xml ? Mobile.shortType(e) : tag).append('>');
        }
        return sb.toString();
    }

    // ------------------------------------------------------------------ mobile screens (Appium sources)

    /**
     * Android (UiAutomator2) and iOS (XCUITest) screens: elements by what the platform says about them
     * (clickable, text, content description, resource id; type, name, label), grouped by the platform's own
     * structural widgets, with the selectors Cerberus' Appium robot accepts (id=, xpath=).
     */
    static final class Mobile {

        private Mobile() {
        }

        static boolean ios(Document d) {
            return !d.select("AppiumAUT").isEmpty() || d.getAllElements().stream().anyMatch(e -> e.tagName().startsWith("XCUIElementType"));
        }

        static String shortType(Element e) {
            String t = e.hasAttr("type") ? e.attr("type") : e.hasAttr("class") && e.attr("class").contains(".") ? e.attr("class") : e.tagName();
            t = t.replace("XCUIElementType", "");
            return t.contains(".") ? t.substring(t.lastIndexOf('.') + 1) : t;
        }

        static boolean visible(Element e) {
            if ("false".equals(e.attr("displayed")) || "false".equals(e.attr("visible"))) {
                return false;
            }
            String b = e.attr("bounds");
            return !b.equals("[0,0][0,0]") && !("0".equals(e.attr("width")) || "0".equals(e.attr("height")));
        }

        private static String region(Element e, boolean ios) {
            for (Element p = e.parent(); p != null; p = p.parent()) {
                String t = shortType(p);
                String rid = p.attr("resource-id");
                if (ios) {
                    if (t.equals("Alert") || t.equals("Sheet")) {
                        return "alert";
                    }
                    if (t.equals("NavigationBar")) {
                        return "navigation bar";
                    }
                    if (t.equals("TabBar")) {
                        return "tab bar";
                    }
                    if (t.equals("Keyboard")) {
                        return "keyboard";
                    }
                    if (t.equals("Table") || t.equals("CollectionView")) {
                        return "list";
                    }
                } else {
                    if (rid.startsWith("android:id/parentPanel") || rid.startsWith("android:id/alertTitle") || rid.equals("android:id/content") && p.parent() != null
                            && p.parent().attr("class").contains("Dialog")) {
                        return "dialog";
                    }
                    if (t.contains("Toolbar") || t.contains("ActionBar")) {
                        return "toolbar";
                    }
                    if (t.contains("TabLayout") || t.contains("TabWidget") || t.contains("BottomNavigation") || t.contains("NavigationBarView")) {
                        return "tabs";
                    }
                    if (t.contains("RecyclerView") || t.contains("ListView") || t.contains("GridView")) {
                        return "list";
                    }
                }
            }
            return "screen";
        }

        private static long count(Document d, String attr, String value) {
            return d.getAllElements().stream().filter(x -> x.hasAttr(attr) && x.attr(attr).equals(value)).count();
        }

        static String selector(Document d, Element e) {
            boolean ios = e.tagName().startsWith("XCUIElementType") || e.hasAttr("type") && e.attr("type").startsWith("XCUIElementType");
            if (ios) {
                String name = e.attr("name");
                if (!name.isEmpty() && !hashy(name) && count(d, "name", name) == 1) {
                    return "id=" + name;
                }
                for (String a : new String[]{"label", "value"}) {
                    String v = e.attr(a);
                    if (!v.isEmpty() && count(d, a, v) == 1) {
                        return "xpath=//*[@" + a + "=" + lit(v) + "]";
                    }
                }
                String base = "//" + e.tagName() + (name.isEmpty() ? "" : "[@name=" + lit(name) + "]");
                return indexed(d, base, e);
            }
            String rid = e.attr("resource-id");
            if (!rid.isEmpty() && !hashy(rid) && count(d, "resource-id", rid) == 1) {
                return "id=" + rid;
            }
            for (String a : new String[]{"content-desc", "text"}) {
                String v = e.attr(a);
                if (!v.isEmpty() && count(d, a, v) == 1) {
                    return "xpath=//*[@" + a + "=" + lit(v) + "]";
                }
            }
            String cls = e.attr("class");
            String base = "//" + (cls.isEmpty() ? "*" : cls) + (rid.isEmpty() ? "" : "[@resource-id=" + lit(rid) + "]");
            return indexed(d, base, e);
        }

        private static String indexed(Document d, String xp, Element e) {
            try {
                Elements found = d.selectXpath(xp);
                int k = found.indexOf(e);
                return k < 0 ? "" : found.size() == 1 ? "xpath=" + xp : "xpath=(" + xp + ")[" + (k + 1) + "]";
            } catch (RuntimeException x) {
                return "";
            }
        }

        static String outline(Document d, int max) {
            boolean ios = ios(d);
            List<Outline.Item> visible = new ArrayList<>();
            List<Outline.Item> hiddenOnes = new ArrayList<>();
            String app = "";
            int total = 0;
            for (Element e : d.getAllElements()) {
                String tag = e.tagName();
                if (tag.equals("#root") || tag.equals("hierarchy") || tag.equals("AppiumAUT")) {
                    continue;
                }
                if (app.isEmpty()) {
                    app = ios ? e.attr("name") : e.attr("package");
                }
                String type = shortType(e);
                String text;
                boolean actionable;
                if (ios) {
                    if (region(e, true).equals("keyboard")) {
                        continue;
                    }
                    text = firstNonEmpty(e.attr("label"), e.attr("value"), e.attr("name"));
                    actionable = type.matches("Button|Link|Cell|Switch|TextField|SecureTextField|SearchField|TextView|Tab|MenuItem|Slider|Picker|"
                            + "PickerWheel|SegmentedControl|Stepper|Toggle|CheckBox|RadioButton");
                } else {
                    text = firstNonEmpty(e.attr("text"), e.attr("content-desc"));
                    actionable = "true".equals(e.attr("clickable")) || "true".equals(e.attr("long-clickable")) || "true".equals(e.attr("checkable"))
                            || type.contains("EditText");
                }
                text = ws(text);
                if (!actionable && text.isEmpty()) {
                    continue;
                }
                if (!actionable && e.children().stream().anyMatch(c -> ws(firstNonEmpty(c.attr("text"), c.attr("label"))).equals(ws(e.attr(ios ? "label" : "text"))))) {
                    continue; // a container repeating its child's text
                }
                total++;
                Map<String, String> a = new LinkedHashMap<>();
                if (!ios && !e.attr("resource-id").isEmpty()) {
                    a.put("id", e.attr("resource-id"));
                }
                if (!ios && !e.attr("content-desc").isEmpty() && !e.attr("content-desc").equals(e.attr("text"))) {
                    a.put("desc", e.attr("content-desc"));
                }
                if (ios && !e.attr("name").isEmpty() && !e.attr("name").equals(text)) {
                    a.put("name", e.attr("name"));
                }
                for (String k : new String[]{"checked", "selected", "enabled"}) {
                    String v = e.attr(k);
                    if (k.equals("enabled") ? "false".equals(v) : "true".equals(v)) {
                        a.put(k, v);
                    }
                }
                if ("true".equals(e.attr("password")) || type.equals("SecureTextField")) {
                    a.put("password", "true");
                }
                Outline.Item it = new Outline.Item(region(e, ios), type, text.length() > 70 ? text.substring(0, 69) + "…" : text, a,
                        selector(d, e), visible(e), null, false);
                (it.visible() ? visible : hiddenOnes).add(it);
            }
            List<Outline.Item> items = new ArrayList<>(visible.subList(0, Math.min(max, visible.size())));
            items.addAll(hiddenOnes.subList(0, Math.min(hiddenOnes.size(), Math.max(0, max - items.size()))));
            String head = (ios ? "iOS" : "Android") + " screen" + (app.isEmpty() ? "" : " · " + app);
            return Outline.format(head, items, List.of(), List.of(), total > max, Integer.MAX_VALUE, null, null);
        }
    }
}
