/**
 * Cerberus Copyright (C) 2013 - 2025 cerberustesting
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
package org.cerberus.core.service.jsoup;

import org.cerberus.core.api.dto.debugexecution.DebugExecutionElementDTOV001;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Deterministic (no AI call) extraction of the interactive/notable elements of an HTML page
 * source, each with a short description and a best-effort unique XPath locator. Parses with
 * Jsoup only — synchronous and free, so it's cheap enough to run on every page source capture
 * (i.e. potentially after every action/control in a debug session), unlike the AI-based
 * alternative it replaces.
 */
@Service
public class PageElementExtractionService {

    // Interactive/notable elements a tester would locate/assert on. data-testid/data-test/data-cy
    // are common test-automation hooks worth surfacing even on an otherwise generic element.
    private static final String CANDIDATE_SELECTOR = String.join(", ",
            "a[href]", "button", "input", "select", "textarea",
            "[role=button]", "[role=link]", "[role=checkbox]", "[role=radio]",
            "[role=tab]", "[role=menuitem]", "[role=textbox]", "[role=combobox]", "[role=switch]",
            "[onclick]", "[data-testid]", "[data-cerberus]", "[data-test]", "[data-cy]", "[data-qa]");

    private static final int MAX_ELEMENTS = 300;
    private static final int MAX_DESCRIPTION_LENGTH = 80;

    public List<DebugExecutionElementDTOV001> extractPageElements(String pageSource) {
        Document doc = Jsoup.parse(pageSource == null ? "" : pageSource);

        List<DebugExecutionElementDTOV001> result = new ArrayList<>();
        Set<String> seenXpaths = new LinkedHashSet<>();

        for (Element el : doc.select(CANDIDATE_SELECTOR)) {
            if (isHidden(el)) {
                continue;
            }

            String xpath = buildXPath(el, doc);
            // Same element matched by more than one selector clause (e.g. <button onclick=...>) :
            // keep only the first, richest match.
            if (!seenXpaths.add(xpath)) {
                continue;
            }

            result.add(DebugExecutionElementDTOV001.builder()
                    .description(buildDescription(el))
                    .xpath(xpath)
                    .build());

            if (result.size() >= MAX_ELEMENTS) {
                break;
            }
        }

        return result;
    }

    private boolean isHidden(Element el) {
        if ("hidden".equalsIgnoreCase(el.attr("type"))) {
            return true;
        }
        String style = el.attr("style").toLowerCase();
        return style.contains("display:none") || style.contains("display: none")
                || style.contains("visibility:hidden") || style.contains("visibility: hidden");
    }

    // ═══ DESCRIPTION ═══

    private String buildDescription(Element el) {
        String label = firstNonBlank(
                el.attr("aria-label"),
                el.attr("placeholder"),
                el.attr("title"),
                ownText(el),
                el.attr("value"),
                el.attr("name"));

        if (!label.isBlank()) {
            return truncate(capitalize(label));
        }

        String tag = el.tagName().toLowerCase();
        switch (tag) {
            case "input":
                String type = el.attr("type");
                return capitalize((type.isBlank() ? "text" : type) + " input");
            case "a":
                return "Link";
            case "button":
                return "Button";
            case "select":
                return "Dropdown";
            case "textarea":
                return "Text area";
            default:
                return capitalize(tag);
        }
    }

    // Own visible text, short-circuited to a sane length : a large text block matched only
    // because it carries onclick/role wouldn't make for a useful "short description" anyway.
    private String ownText(Element el) {
        String text = el.text();
        return (text != null && text.length() <= MAX_DESCRIPTION_LENGTH) ? text : "";
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return "";
    }

    private String truncate(String value) {
        return value.length() <= MAX_DESCRIPTION_LENGTH ? value : value.substring(0, MAX_DESCRIPTION_LENGTH - 1).trim() + "…";
    }

    private String capitalize(String value) {
        if (value.isBlank()) {
            return value;
        }
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }

    // ═══ XPATH ═══
    // Tries progressively less specific, but still element-scoped, locators before falling back
    // to a fully structural (always-unique, "Chrome-copy-XPath"-style) absolute path.

    private String buildXPath(Element el, Document doc) {
        String tag = el.tagName();

        String id = el.attr("id");
        if (!id.isBlank() && isUniqueByCss(doc, "#" + cssEscape(id))) {
            return "//*[@id=" + xpathLiteral(id) + "]";
        }

        String testHook = firstNonBlank(el.attr("data-testid"), el.attr("data-test"), el.attr("data-cy"), el.attr("data-qa"));
        if (!testHook.isBlank()) {
            String attrName = !el.attr("data-testid").isBlank() ? "data-testid"
                    : !el.attr("data-test").isBlank() ? "data-test"
                    : !el.attr("data-cy").isBlank() ? "data-cy" : "data-qa";
            String xpath = "//" + tag + "[@" + attrName + "=" + xpathLiteral(testHook) + "]";
            if (isUniqueByCss(doc, tag + "[" + attrName + "=" + cssAttrLiteral(testHook) + "]")) {
                return xpath;
            }
        }

        String name = el.attr("name");
        if (!name.isBlank() && isUniqueByCss(doc, tag + "[name=" + cssAttrLiteral(name) + "]")) {
            return "//" + tag + "[@name=" + xpathLiteral(name) + "]";
        }

        String ariaLabel = el.attr("aria-label");
        if (!ariaLabel.isBlank() && isUniqueByCss(doc, tag + "[aria-label=" + cssAttrLiteral(ariaLabel) + "]")) {
            return "//" + tag + "[@aria-label=" + xpathLiteral(ariaLabel) + "]";
        }

        String placeholder = el.attr("placeholder");
        if (!placeholder.isBlank() && isUniqueByCss(doc, tag + "[placeholder=" + cssAttrLiteral(placeholder) + "]")) {
            return "//" + tag + "[@placeholder=" + xpathLiteral(placeholder) + "]";
        }

        String text = ownText(el);
        if (!text.isBlank()) {
            Elements sameTextSameTag = doc.select(tag);
            int matches = 0;
            for (Element candidate : sameTextSameTag) {
                if (text.equals(candidate.ownText())) {
                    matches++;
                }
            }
            if (matches == 1) {
                return "//" + tag + "[normalize-space(text())=" + xpathLiteral(text) + "]";
            }
        }

        return buildStructuralXPath(el);
    }

    private boolean isUniqueByCss(Document doc, String cssSelector) {
        try {
            return doc.select(cssSelector).size() == 1;
        } catch (Exception ex) {
            // Malformed selector (e.g. an attribute value containing characters Jsoup's selector
            // parser chokes on) : treat as "not safely verifiable", fall through to the next
            // candidate rather than risk emitting a locator that isn't actually unique.
            return false;
        }
    }

    // Absolute, always-valid, always-unique fallback : one [tag][positionAmongSameTagSiblings]
    // step per ancestor, same strategy browser devtools use for "Copy > Copy XPath".
    private String buildStructuralXPath(Element el) {
        StringBuilder path = new StringBuilder();
        Element current = el;

        while (current != null && current.parent() != null) {
            Element parent = current.parent();
            String tag = current.tagName();

            int index = 1;
            for (Element sibling : parent.children()) {
                if (sibling == current) {
                    break;
                }
                if (sibling.tagName().equals(tag)) {
                    index++;
                }
            }

            path.insert(0, "/" + tag + "[" + index + "]");
            current = parent;
        }

        return path.length() > 0 ? path.toString() : "/" + el.tagName();
    }

    private String cssEscape(String value) {
        return value.replaceAll("([^a-zA-Z0-9_-])", "\\\\$1");
    }

    // Jsoup attribute-selector values don't need CSS identifier escaping (they're quoted), just
    // their own quote char escaped.
    private String cssAttrLiteral(String value) {
        return "\"" + value.replace("\"", "\\\"") + "\"";
    }

    private String xpathLiteral(String value) {
        if (!value.contains("'")) {
            return "'" + value + "'";
        }
        if (!value.contains("\"")) {
            return "\"" + value + "\"";
        }
        StringBuilder sb = new StringBuilder("concat(");
        String[] parts = value.split("'");
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) {
                sb.append(", \"'\", ");
            }
            sb.append('\'').append(parts[i]).append('\'');
        }
        sb.append(')');
        return sb.toString();
    }
}