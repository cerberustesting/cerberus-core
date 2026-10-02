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
package org.cerberus.core.mcpdelta.run;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cerberus.core.mcpdelta.read.Outline;
import org.cerberus.core.mcpdelta.read.PageHints;
import org.cerberus.core.mcpdelta.tools.Tool;
import org.cerberus.core.mcpdelta.util.Text;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The screen as the robot actually renders it. On the web the outline is computed inside the browser
 * (resources/live/outline.js): what is visible, in which region, what a layer covers, the selector that
 * reaches each element — whatever the framework. Frames of another origin are entered from outside. A
 * native mobile session has no DOM: its XML source is outlined instead. Used by "read live:<url>" (a
 * throw-away browser) and by debug mode (the execution's own session, on the execution's own robot).
 */
public final class LiveOutline {

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final String SCRIPT = load("mcpdelta/live/outline.js");
    private static final String ELEMENT = "element-6066-11e4-a52e-4f735466cecf";

    /** Resolves when the DOM has stopped changing for a moment (single-page apps render after "load"). */
    private static final String SETTLE = """
            var done = arguments[arguments.length - 1], quiet = arguments[0], max = arguments[1];
            var last = Date.now(), start = Date.now();
            var mo = new MutationObserver(function () { last = Date.now(); });
            mo.observe(document, {subtree: true, childList: true, characterData: true});
            (function tick() {
              var now = Date.now();
              if ((document.readyState !== 'loading' && now - last >= quiet) || now - start >= max) { mo.disconnect(); done(true); }
              else setTimeout(tick, 100);
            })();
            """;

    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    private final String grid;
    private final String auth;

    public LiveOutline(String grid) {
        this(grid, null);
    }

    /** @param auth "user:password" for a robot that asks for it, or null */
    public LiveOutline(String grid, String auth) {
        this.grid = grid.endsWith("/") ? grid.substring(0, grid.length() - 1) : grid;
        this.auth = Text.isBlank(auth) ? null : "Basic " + java.util.Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
    }

    private static String load(String resource) {
        try (InputStream in = LiveOutline.class.getClassLoader().getResourceAsStream(resource)) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("missing " + resource, e);
        }
    }

    /** A grid that gave no browser session: the next one may. */
    private static final class Unreachable extends RuntimeException {
        Unreachable(String message) {
            super(message);
        }
    }

    /** When each grid last failed to give a session: a dead grid costs its timeout once in a while, not on every read. */
    private static final Map<String, Long> DOWN = new java.util.concurrent.ConcurrentHashMap<>();
    private static final long DOWN_FOR_MS = 10 * 60_000L;
    private static final int MAX_TRIES = 4;

    /** Outlines the URLs on the first grid that gives a browser session, grids that failed lately tried last. */
    public static String probeFirst(List<String> grids, List<String> urls, int room) {
        long now = System.currentTimeMillis();
        List<String> order = new ArrayList<>(grids);
        order.sort(java.util.Comparator.comparing(g -> now - DOWN.getOrDefault(g, 0L) < DOWN_FOR_MS));
        List<String> failed = new ArrayList<>();
        for (String grid : order.subList(0, Math.min(MAX_TRIES, order.size()))) {
            try {
                String outline = new LiveOutline(grid).probe(urls, room);
                DOWN.remove(grid);
                return outline;
            } catch (Unreachable e) {
                DOWN.put(grid, System.currentTimeMillis());
                failed.add(grid + " (" + e.getMessage() + ")");
            }
        }
        throw new Tool.ToolError("cannot drive a browser: no grid gave a session. Tried " + String.join(", ", failed)
                + (order.size() > MAX_TRIES ? " (" + (order.size() - MAX_TRIES) + " more not tried)" : "")
                + ". Configure the grid to use: DELTA_GRID_URL, or org.cerberus.core.mcpdelta.grid in Cerberus.");
    }

    /** Opens each URL in one throw-away browser session and outlines it; what several pages share is shown once. */
    public String probe(List<String> urls, int room) {
        String session = null;
        try {
            // "eager": the DOM is what is outlined; waiting for every ad and tracker to load can take half a minute.
            JsonNode created;
            try {
                created = post("/session", "{\"capabilities\":{\"alwaysMatch\":{\"browserName\":\"chrome\",\"pageLoadStrategy\":\"eager\"}}}");
            } catch (Exception e) {
                throw new Unreachable(e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage());
            }
            session = created.path("value").path("sessionId").asText();
            post("/session/" + session + "/window/rect", "{\"width\":1366,\"height\":768}");
            post("/session/" + session + "/timeouts", "{\"script\":20000}");
            StringBuilder sb = new StringBuilder();
            Map<String, String[]> shared = urls.size() > 1 ? new LinkedHashMap<>() : null;
            for (String url : urls) {
                post("/session/" + session + "/url", JSON.writeValueAsString(Map.of("url", url)));
                settle(session, 700, 8000);
                sb.append(outline(session, Math.max(1500, room / urls.size()), true, shared, path(url))).append('\n');
            }
            return sb.toString().stripTrailing();
        } catch (Tool.ToolError | Unreachable e) {
            throw e;
        } catch (Exception e) {
            throw new Tool.ToolError("cannot drive a browser on " + grid + ": " + e.getMessage());
        } finally {
            if (session != null) {
                try {
                    http.send(request("/session/" + session).DELETE().timeout(Duration.ofSeconds(20)).build(), HttpResponse.BodyHandlers.discarding());
                } catch (Exception ignored) {
                    // the grid reclaims idle sessions anyway
                }
            }
        }
    }

    /**
     * Outlines what an existing session shows now (a debug execution's browser or device), without moving
     * it: no frame is entered, nothing is clicked. Null when the robot cannot be reached.
     */
    public String current(String session, int room) {
        try {
            settle(session, 400, 3000);
            return outline(session, room, false, null, null);
        } catch (Exception e) {
            try {
                return nativeScreen(session, room);
            } catch (Exception ignored) {
                return null;
            }
        }
    }

    /** A native app (Appium): no DOM, but the XML source of the screen. */
    private String nativeScreen(String session, int room) throws Exception {
        JsonNode src = get("/session/" + session + "/source");
        String xml = src.path("value").asText("");
        if (xml.isBlank()) {
            return null;
        }
        return "live " + Text.truncate(PageHints.outline(xml, 150), room);
    }

    private void settle(String session, int quietMs, int maxMs) {
        try {
            post("/session/" + session + "/execute/async", JSON.writeValueAsString(Map.of("script", SETTLE, "args", List.of(quietMs, maxMs))));
        } catch (Exception ignored) {
            // a page that cannot run scripts is outlined as it is
        }
    }

    private String outline(String session, int room, boolean enterFrames, Map<String, String[]> shared, String page) throws Exception {
        JsonNode v = run(session);
        List<Outline.Item> items = items(v.path("items"), null);
        List<Outline.Frame> frames = new ArrayList<>();
        int entered = 0;
        for (JsonNode f : v.path("frames")) {
            String sel = f.path("s").asText("");
            // Frames of another origin: entered from outside, outlined, left. Only in a throw-away session.
            if (enterFrames && entered < 3 && f.path("big").asBoolean() && f.path("el").has(ELEMENT)) {
                try {
                    post("/session/" + session + "/frame", JSON.writeValueAsString(Map.of("id", Map.of(ELEMENT, f.path("el").path(ELEMENT).asText()))));
                    settle(session, 500, 4000);
                    JsonNode inner = run(session);
                    String region = f.path("r").asText("");
                    boolean layer = region.startsWith("cookie/consent") || region.startsWith("dialog") || region.startsWith("overlay");
                    List<Outline.Item> in = items(inner.path("items"), sel.isEmpty() ? "iframe" : sel);
                    for (Outline.Item it : in) {
                        // What a frame shows inside a layer belongs to that layer, and is listed with it.
                        items.add(layer ? new Outline.Item(region, it.tag(), it.text(), it.attrs(), it.selector(), it.visible(), it.frame(), it.shadow()) : it);
                    }
                    entered++;
                    continue;
                } catch (Exception e) {
                    // listed below as not entered
                } finally {
                    post("/session/" + session + "/frame", "{\"id\":null}");
                }
            }
            frames.add(new Outline.Frame(sel, f.path("title").asText(""), f.path("src").asText(""), f.path("r").asText("")));
        }
        List<Outline.Cover> covers = new ArrayList<>();
        for (JsonNode c : v.path("covered")) {
            covers.add(new Outline.Cover(c.path("layer").asText(), c.path("n").asInt()));
        }
        String head = "live " + v.path("url").asText() + " · title " + Text.quote(v.path("title").asText());
        return Outline.format(head, items, covers, frames, v.path("more").asBoolean(), room, shared, page);
    }

    private JsonNode run(String session) throws Exception {
        JsonNode r = post("/session/" + session + "/execute/sync", JSON.writeValueAsString(Map.of("script", SCRIPT, "args", List.of(400))));
        JsonNode v = r.path("value");
        if (v.isMissingNode() || !v.has("items")) {
            throw new Tool.ToolError("the page could not be outlined: " + Text.truncate(r.toString(), 200));
        }
        return v;
    }

    private static List<Outline.Item> items(JsonNode list, String frame) {
        List<Outline.Item> out = new ArrayList<>();
        for (JsonNode it : list) {
            Map<String, String> attrs = new LinkedHashMap<>();
            it.path("a").fields().forEachRemaining(f -> attrs.put(f.getKey(), f.getValue().asText()));
            String f = frame != null ? frame : it.hasNonNull("f") ? it.path("f").asText() : null;
            out.add(new Outline.Item(it.path("r").asText("page"), it.path("t").asText(), it.path("x").asText(""), attrs,
                    it.path("s").asText(""), it.path("v").asBoolean(), f, it.path("sh").asInt(0) == 1));
        }
        return out;
    }

    private static String path(String url) {
        try {
            String p = URI.create(url).getPath();
            return p == null || p.isEmpty() ? "/" : p;
        } catch (Exception e) {
            return url;
        }
    }

    private HttpRequest.Builder request(String path) {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(grid + path)).timeout(Duration.ofSeconds(40));
        if (auth != null) {
            b.header("Authorization", auth);
        }
        return b;
    }

    private JsonNode get(String path) throws Exception {
        return answer(http.send(request(path).GET().build(), HttpResponse.BodyHandlers.ofString()));
    }

    private JsonNode post(String path, String body) throws Exception {
        return answer(http.send(request(path).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(body)).build(),
                HttpResponse.BodyHandlers.ofString()));
    }

    private static JsonNode answer(HttpResponse<String> r) throws Exception {
        JsonNode n = JSON.readTree(r.body());
        if (r.statusCode() >= 300) {
            throw new Tool.ToolError("browser grid answered " + r.statusCode() + ": " + Text.truncate(n.path("value").path("message").asText(r.body()), 200));
        }
        return n;
    }
}
