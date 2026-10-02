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
package org.cerberus.core.mcpdelta.cerberus;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cerberus.core.mcpdelta.Context;
import org.cerberus.core.mcpdelta.tools.Tool;
import org.cerberus.core.mcpdelta.util.Text;

import java.net.CookieManager;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

/**
 * Cerberus over its public HTTP API, for the standalone server: AddToExecutionQueueV003 for runs, the debug
 * execution API (an API key, or a session opened with the configured account), ManageV001 for caches and the
 * queue job.
 */
public final class HttpGateway implements CerberusGateway {

    private static final ObjectMapper JSON = new ObjectMapper();

    private final Context.Config config;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private final HttpClient session = HttpClient.newBuilder().cookieHandler(new CookieManager()).connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NEVER).build();
    private volatile boolean loggedIn;

    public HttpGateway(Context.Config config) {
        this.config = config;
    }

    @Override
    public Queued enqueue(Order order) {
        StringBuilder form = new StringBuilder();
        if (order.campaign() != null) {
            param(form, "campaign", order.campaign());
        }
        for (String[] t : order.testcases()) {
            param(form, "test", t[0]);
            param(form, "testcase", t[1]);
        }
        order.countries().forEach(v -> param(form, "country", v));
        order.environments().forEach(v -> param(form, "environment", v));
        order.robots().stream().filter(v -> !Text.isBlank(v)).forEach(v -> param(form, "robot", v));
        if (!Text.isBlank(order.tag())) {
            param(form, "tag", order.tag());
        }
        for (Map.Entry<String, String> o : order.options().entrySet()) {
            param(form, OPTION_PARAMS.get(o.getKey()), o.getValue());
        }
        param(form, "outputformat", "json");
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(config.cerberusUrl + "/AddToExecutionQueueV003"))
                .timeout(Duration.ofSeconds(60))
                .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(form.toString()));
        if (!config.cerberusApiKey.isBlank()) {
            b.header("apikey", config.cerberusApiKey);
        }
        try {
            HttpResponse<String> resp = http.send(b.build(), HttpResponse.BodyHandlers.ofString());
            JsonNode n = JSON.readTree(resp.body());
            int nb = n.path("nbExe").asInt(0);
            if (!"OK".equals(n.path("messageType").asText("")) || nb == 0) {
                throw new Tool.ToolError("Cerberus refused the run: " + Text.truncate(Text.oneLine(n.path("message").asText(resp.body())), 400));
            }
            return new Queued(nb, n.path("tag").asText(order.tag()));
        } catch (Tool.ToolError e) {
            throw e;
        } catch (Exception e) {
            throw new Tool.ToolError("cannot reach Cerberus at " + config.cerberusUrl + ": " + e.getMessage());
        }
    }

    @Override
    public JsonNode debug(String method, String path, String body) {
        for (int attempt = 0; attempt < 2; attempt++) {
            if (!loggedIn && config.cerberusApiKey.isBlank()) {
                login();
            }
            try {
                HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(config.cerberusUrl + "/api/public/debugexecutions/" + path))
                        .timeout(Duration.ofSeconds(60)).header("X-API-VERSION", "1");
                if (!config.cerberusApiKey.isBlank()) {
                    b.header("X-API-KEY", config.cerberusApiKey);
                }
                if ("GET".equals(method)) {
                    b.GET();
                } else {
                    b.header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(body == null ? "" : body));
                }
                HttpResponse<String> r = session.send(b.build(), HttpResponse.BodyHandlers.ofString());
                if (r.statusCode() == 401 && attempt == 0) {
                    loggedIn = false;
                    continue;
                }
                JsonNode n = JSON.readTree(r.body());
                if (r.statusCode() >= 300) {
                    throw new Tool.ToolError("Cerberus debug API " + r.statusCode() + ": " + n.path("message").asText(r.body()));
                }
                return n.path("data");
            } catch (Tool.ToolError e) {
                throw e;
            } catch (Exception e) {
                throw new Tool.ToolError("cannot reach the Cerberus debug API: " + e.getMessage());
            }
        }
        throw new Tool.ToolError("the Cerberus debug API refused the session (check DELTA_CERBERUS_LOGIN / DELTA_CERBERUS_PASSWORD)");
    }

    /** A Cerberus session, as the debug page itself uses: the public API takes a logged-in user or an API key. */
    private synchronized void login() {
        try {
            session.send(HttpRequest.newBuilder(URI.create(config.cerberusUrl + "/Login.jsp")).GET().build(), HttpResponse.BodyHandlers.discarding());
            String form = "j_username=" + URLEncoder.encode(config.cerberusLogin, StandardCharsets.UTF_8)
                    + "&j_password=" + URLEncoder.encode(config.cerberusPassword, StandardCharsets.UTF_8);
            session.send(HttpRequest.newBuilder(URI.create(config.cerberusUrl + "/j_security_check"))
                    .header("Content-Type", "application/x-www-form-urlencoded").POST(HttpRequest.BodyPublishers.ofString(form)).build(),
                    HttpResponse.BodyHandlers.discarding());
            loggedIn = true;
        } catch (Exception e) {
            throw new Tool.ToolError("cannot open a Cerberus session for the debug API: " + e.getMessage());
        }
    }

    @Override
    public String purgeCache() {
        return manage("purgeCache", "Cerberus caches purged");
    }

    @Override
    public String runQueueJob() {
        return manage("runQueueJob", "Cerberus queue job triggered");
    }

    private String manage(String action, String ok) {
        try {
            HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(config.cerberusUrl + "/ManageV001?action=" + action))
                    .timeout(Duration.ofSeconds(20)).GET();
            if (!config.cerberusApiKey.isBlank()) {
                b.header("apikey", config.cerberusApiKey);
            }
            HttpResponse<String> r = http.send(b.build(), HttpResponse.BodyHandlers.ofString());
            return r.statusCode() < 300 ? ok : "Cerberus answered " + r.statusCode() + " to " + action;
        } catch (Exception e) {
            return "could not reach Cerberus for " + action + ": " + e.getMessage();
        }
    }

    private static void param(StringBuilder form, String key, String value) {
        if (form.length() > 0) {
            form.append('&');
        }
        form.append(URLEncoder.encode(key, StandardCharsets.UTF_8)).append('=').append(URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8));
    }
}
