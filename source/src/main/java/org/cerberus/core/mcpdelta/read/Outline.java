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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * One format for every screen the model has to understand: a live page, a page the robot saved, a mobile
 * screen. Layers that sit above the page (a consent banner, an open dialog) first, with what they cover;
 * then what is visible, by region, each element with the selector that reaches it; then, briefly, what
 * exists but is hidden now (closed menus, other tabs), so the model knows something must open first.
 */
public final class Outline {

    public record Item(String region, String tag, String text, Map<String, String> attrs, String selector, boolean visible,
                       String frame, boolean shadow) {
    }

    /** A layer above the page and how many visible elements of the page it covers. */
    public record Cover(String layer, int count) {
    }

    /** A frame whose content is not in this outline (another origin): reached with focusToIframe. */
    public record Frame(String selector, String title, String src, String region) {
    }

    private Outline() {
    }

    private static final String CONSENT = "cookie/consent banner";

    /**
     * @param shared region blocks already shown by an earlier page of the same answer (region → {page, block});
     *               a block shown again identically is replaced by one line. Null for a single page.
     * @param page   how this page is named in such a line
     */
    public static String format(String head, List<Item> items, List<Cover> covers, List<Frame> frames, boolean more, int room,
                                Map<String, String[]> shared, String page) {
        StringBuilder sb = new StringBuilder(head).append('\n');
        Map<String, List<Item>> visible = new LinkedHashMap<>();
        Map<String, List<Item>> hidden = new LinkedHashMap<>();
        for (Item it : items) {
            boolean layer = it.region().startsWith(CONSENT) || it.region().startsWith("dialog") || it.region().startsWith("overlay");
            String region = it.frame() == null || layer ? it.region() : "in iframe " + it.frame();
            (it.visible() ? visible : hidden).computeIfAbsent(region, k -> new ArrayList<>()).add(it);
        }
        Map<String, Integer> covered = new LinkedHashMap<>();
        for (Cover c : covers) {
            covered.merge(c.layer(), c.count(), Integer::sum);
        }
        // Layers first: they decide whether anything else can be clicked.
        List<String> layers = new ArrayList<>();
        for (String region : visible.keySet()) {
            if (region.equals(CONSENT) || region.startsWith("dialog") || region.startsWith("overlay")) {
                layers.add(region);
            }
        }
        layers.sort((a, b) -> Boolean.compare(!a.equals(CONSENT), !b.equals(CONSENT)));
        for (String layer : layers) {
            List<Item> its = visible.remove(layer);
            Integer n = covered.remove(layer);
            String what = layer.equals(CONSENT) ? "a cookie/consent banner is shown" : layer + " is shown";
            String block = what + (n == null ? "" : " — it covers " + n + " visible element(s) of the page: deal with it first") + "\n"
                    + lines(its, layer.equals(CONSENT), Integer.MAX_VALUE);
            sb.append(reuse(layer, block, shared, page));
        }
        for (Map.Entry<String, Integer> c : covered.entrySet()) {
            sb.append(c.getKey()).append(" covers ").append(c.getValue()).append(" visible element(s) of the page\n");
        }
        for (Map.Entry<String, List<Item>> e : visible.entrySet()) {
            if (sb.length() > room) {
                sb.append("… (budget reached)\n");
                break;
            }
            String block = e.getKey() + (e.getKey().startsWith("in iframe ") ? " (focusToIframe it first, focusDefaultIframe after)" : "") + ":\n"
                    + lines(e.getValue(), false, room - sb.length());
            sb.append(reuse(e.getKey(), block, shared, page));
        }
        for (Frame f : frames) {
            sb.append("iframe ").append(f.selector().isEmpty() ? "" : f.selector() + " ")
                    .append(f.title().isEmpty() ? "" : Text.quote(f.title()) + " ")
                    .append(f.src().isEmpty() ? "" : "src=" + Text.quote(Text.truncate(f.src(), 80)) + " ")
                    .append("— another origin: its content is listed only when read live; focusToIframe to act in it\n");
        }
        int nHidden = hidden.values().stream().mapToInt(List::size).sum();
        if (nHidden > 0 && sb.length() < room) {
            sb.append("hidden now (").append(nHidden).append(", e.g. closed menus): ");
            List<String> names = new ArrayList<>();
            for (List<Item> its : hidden.values()) {
                for (Item it : its) {
                    String href = it.attrs().getOrDefault("href", "");
                    if (!it.text().isEmpty() && names.size() < 40) {
                        names.add(Text.quote(it.text()) + (href.isEmpty() ? "" : "→" + href));
                    }
                }
            }
            sb.append(String.join(", ", names)).append('\n');
        }
        if (more) {
            sb.append("(long page: the first elements only)\n");
        }
        return sb.toString();
    }

    /** The same block as an earlier page of this answer costs one line. */
    private static String reuse(String region, String block, Map<String, String[]> shared, String page) {
        if (shared == null) {
            return block;
        }
        String[] before = shared.get(region);
        if (before != null && before[1].equals(block)) {
            return region + ": same as on " + before[0] + "\n";
        }
        if (before == null) {
            shared.put(region, new String[]{page, block});
        }
        return block;
    }

    private static String lines(List<Item> items, boolean compact, int room) {
        StringBuilder sb = new StringBuilder();
        String frame = null;
        int left = 0;
        for (Item it : items) {
            if (compact && !isAction(it)) {
                continue;
            }
            if (sb.length() > room) {
                left++;
                continue;
            }
            if (it.frame() != null && !it.frame().equals(frame)) {
                frame = it.frame();
                sb.append("  (inside iframe ").append(frame).append(": focusToIframe it first, focusDefaultIframe after)\n");
            }
            sb.append("  ").append(line(it)).append('\n');
        }
        if (left > 0) {
            sb.append("  … ").append(left).append(" more here (budget reached)\n");
        }
        return sb.toString();
    }

    private static boolean isAction(Item it) {
        String t = it.tag();
        String role = it.attrs().getOrDefault("role", "");
        return t.equals("button") || t.equals("a") || t.equals("input") || t.equals("label") || t.equals("select") || role.equals("button");
    }

    public static String line(Item it) {
        StringBuilder sb = new StringBuilder(it.tag());
        if (!it.text().isEmpty()) {
            sb.append(' ').append(Text.quote(it.text()));
        }
        for (Map.Entry<String, String> f : it.attrs().entrySet()) {
            String k = f.getKey();
            // What the selector already says, or what a button always is, costs tokens and tells nothing.
            if (k.equals("type") && (it.tag().equals("button") || it.tag().equals("a")) || k.equals("href") && it.selector().contains(f.getValue())) {
                continue;
            }
            sb.append(' ').append(k).append('=').append(Text.quote(Text.truncate(f.getValue(), 60)));
        }
        if (!it.selector().isEmpty()) {
            sb.append("  → ").append(it.selector());
        } else if (it.shadow()) {
            sb.append("  (in a shadow root)");
        }
        return sb.toString();
    }
}
