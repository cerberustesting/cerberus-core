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
package org.cerberus.core.service.robotproxy.impl;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Property;
import org.cerberus.core.crud.entity.RobotExecutor;
import org.cerberus.core.service.robotproxy.entity.RelayException;
import org.cerberus.core.service.robotproxy.entity.RelayRequest;
import org.cerberus.core.service.robotproxy.entity.RelayResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests of the relay client against a fake runner (JDK http server).
 */
public class RelayServiceTest {

    private static final String TOKEN = "s3cr3t-t0k3n-XYZ";

    private final RelayService service = newService();

    private static RelayService newService() {
        RelayService s = new RelayService();
        try {
            java.lang.reflect.Field f = RelayService.class.getDeclaredField("proxyAuthService");
            f.setAccessible(true);
            f.set(s, new ProxyAuthService());
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException(ex);
        }
        return s;
    }
    private HttpServer server;
    private final List<String> receivedPaths = new ArrayList<>();
    private final List<String> receivedAuth = new ArrayList<>();
    private final List<String> receivedMethods = new ArrayList<>();
    private final List<String> receivedBodies = new ArrayList<>();
    private ListAppender appender;

    /** Collects every log message to check the token is never logged. */
    private static class ListAppender extends AbstractAppender {

        final List<String> messages = Collections.synchronizedList(new ArrayList<>());

        ListAppender() {
            super("relayTestAppender", null, null, true, Property.EMPTY_ARRAY);
        }

        @Override
        public void append(LogEvent event) {
            messages.add(event.getMessage().getFormattedMessage() + (event.getThrown() != null ? " " + event.getThrown() : ""));
        }
    }

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.start();

        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration config = ctx.getConfiguration();
        appender = new ListAppender();
        appender.start();
        config.getRootLogger().addAppender(appender, Level.ALL, null);
        config.getRootLogger().setLevel(Level.ALL);
        ctx.updateLoggers();
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        ctx.getConfiguration().getRootLogger().removeAppender("relayTestAppender");
        appender.stop();
    }

    private RobotExecutor executor() {
        RobotExecutor e = new RobotExecutor();
        e.setRobot("rob");
        e.setExecutor("exe");
        e.setRelayActive(true);
        e.setExecutorProxyType(RobotExecutor.PROXY_TYPE_MITMPROXY);
        e.setExecutorProxyServiceHost("127.0.0.1");
        e.setExecutorProxyServicePort(server.getAddress().getPort());
        e.setExecutorProxyAuthMode(RobotExecutor.PROXY_AUTH_TOKEN);
        e.setExecutorProxyAuthToken(TOKEN);
        return e;
    }

    private void handle(String path, HttpHandler handler) {
        server.createContext(path, exchange -> {
            receivedPaths.add(exchange.getRequestURI().getPath());
            receivedAuth.add(exchange.getRequestHeaders().getFirst("Authorization"));
            receivedMethods.add(exchange.getRequestMethod());
            receivedBodies.add(readAll(exchange.getRequestBody()));
            handler.handle(exchange);
        });
    }

    private static String readAll(InputStream is) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int n;
        while ((n = is.read(buf)) > 0) {
            out.write(buf, 0, n);
        }
        return new String(out.toByteArray(), StandardCharsets.UTF_8);
    }

    private static void reply(HttpExchange ex, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().add("Content-Type", "application/json");
        ex.sendResponseHeaders(status, bytes.length == 0 ? -1 : bytes.length);
        if (bytes.length > 0) {
            ex.getResponseBody().write(bytes);
        }
        ex.close();
    }

    private RelayRequest sampleRequest() {
        return new RelayRequest().setMethod("POST").setUrl("https://target.example.com/api?x=1")
                .addHeader("Content-Type", "application/json").addHeader("X-Multi", "a").addHeader("X-Multi", "b")
                .setBody("{\"hello\":\"wörld\"}".getBytes(StandardCharsets.UTF_8))
                .setFollowRedirects(false).setTimeoutMs(5000).setAcceptUnsignedSsl(true);
    }

    @Test
    void callSerializesRequestAndMapsResponse() throws Exception {
        handle("/relay", ex -> {
            JSONObject resp = new JSONObject()
                    .put("status", 404).put("statusText", "Not Found")
                    .put("headers", new JSONArray().put(new JSONArray().put("Content-Type").put("text/plain; charset=UTF-8")).put(new JSONArray().put("Set-Cookie").put("a=1")).put(new JSONArray().put("Set-Cookie").put("b=2")))
                    .put("bodyBase64", Base64.getEncoder().encodeToString("not là".getBytes(StandardCharsets.UTF_8)))
                    .put("truncated", true).put("durationMs", 42).put("finalUrl", "https://target.example.com/final");
            reply(ex, 200, resp.toString());
        });

        RelayResponse response = service.call(executor(), sampleRequest());

        assertEquals("/relay", receivedPaths.get(0));
        assertEquals("POST", receivedMethods.get(0));
        assertEquals("Bearer " + TOKEN, receivedAuth.get(0));
        JSONObject sent = new JSONObject(receivedBodies.get(0));
        assertEquals("POST", sent.getString("method"));
        assertEquals("https://target.example.com/api?x=1", sent.getString("url"));
        assertEquals("application/json", sent.getJSONObject("headers").getString("Content-Type"));
        assertEquals(2, sent.getJSONObject("headers").getJSONArray("X-Multi").length());
        assertEquals("{\"hello\":\"wörld\"}", new String(Base64.getDecoder().decode(sent.getString("bodyBase64")), StandardCharsets.UTF_8));
        assertFalse(sent.getBoolean("followRedirects"));
        assertEquals(5000, sent.getInt("timeoutMs"));
        assertTrue(sent.getBoolean("acceptUnsignedSsl"));

        // The 200 of the relay is returned whatever the target answered.
        assertEquals(404, response.getStatus());
        assertEquals("Not Found", response.getStatusText());
        assertEquals(3, response.getHeaders().size());
        assertEquals("a=1", response.getHeaders().get(1)[1]);
        assertEquals("b=2", response.getHeaders().get(2)[1]);
        assertEquals("not là", response.getBodyAsString());
        assertTrue(response.isTruncated());
        assertEquals(42, response.getDurationMs());
        assertEquals("https://target.example.com/final", response.getFinalUrl());
    }

    @Test
    void callWithoutBodyOmitsBodyBase64() throws Exception {
        handle("/relay", ex -> reply(ex, 200, "{\"status\":204,\"statusText\":\"No Content\",\"headers\":[],\"bodyBase64\":\"\"}"));
        RelayResponse response = service.call(executor(), new RelayRequest().setMethod("GET").setUrl("http://t/"));
        assertFalse(new JSONObject(receivedBodies.get(0)).has("bodyBase64"));
        assertEquals(204, response.getStatus());
        assertEquals(0, response.getBody().length);
    }

    private void assertRelayError(int httpStatus, String body, String expectedCode) {
        handle("/relay", ex -> reply(ex, httpStatus, body));
        RelayException ex = assertThrows(RelayException.class, () -> service.call(executor(), sampleRequest()));
        assertEquals(expectedCode, ex.getCode());
        assertEquals(httpStatus, ex.getHttpStatus());
        assertFalse(ex.getMessage().contains(TOKEN));
    }

    @Test
    void errors401() {
        assertRelayError(401, "{\"error\":\"bad token\",\"code\":\"unauthorized\"}", RelayException.CODE_UNAUTHORIZED);
    }

    @Test
    void errors403() {
        assertRelayError(403, "{\"error\":\"private address\",\"code\":\"target_blocked\"}", RelayException.CODE_TARGET_BLOCKED);
    }

    @Test
    void errors502() {
        assertRelayError(502, "{\"error\":\"ECONNREFUSED\",\"code\":\"connect_failed\"}", RelayException.CODE_CONNECT_FAILED);
    }

    @Test
    void errors503() {
        assertRelayError(503, "{\"code\":\"relay_stopped\"}", RelayException.CODE_RELAY_STOPPED);
    }

    @Test
    void errors504IsTimeout() {
        handle("/relay", ex -> reply(ex, 504, "{\"error\":\"timeout\",\"code\":\"timeout\"}"));
        RelayException ex = assertThrows(RelayException.class, () -> service.call(executor(), sampleRequest()));
        assertTrue(ex.isTimeout());
    }

    @Test
    void errors429And413And400() {
        assertRelayError(429, "{\"error\":\"x\",\"code\":\"too_many_requests\"}", RelayException.CODE_TOO_MANY_REQUESTS);
    }

    @Test
    void unexpectedStatusWithoutJsonIsInvalidResponse() {
        assertRelayError(500, "<html>boom</html>", RelayException.CODE_INVALID_RESPONSE);
    }

    @Test
    void redirectIsFollowedOnceKeepingMethodAndBody() throws Exception {
        handle("/relay", ex -> {
            ex.getResponseHeaders().add("Location", "/relay-https");
            reply(ex, 307, "");
        });
        handle("/relay-https", ex -> reply(ex, 200, "{\"status\":200,\"statusText\":\"OK\",\"headers\":[],\"bodyBase64\":\"\"}"));

        RelayResponse response = service.call(executor(), sampleRequest());

        assertEquals(200, response.getStatus());
        assertEquals(List.of("/relay", "/relay-https"), receivedPaths);
        assertEquals(List.of("POST", "POST"), receivedMethods);
        assertEquals(receivedBodies.get(0), receivedBodies.get(1));
        assertEquals("Bearer " + TOKEN, receivedAuth.get(1));
    }

    @Test
    void redirectIsFollowedOnlyOnce() {
        handle("/relay", ex -> {
            ex.getResponseHeaders().add("Location", "/relay2");
            reply(ex, 302, "");
        });
        handle("/relay2", ex -> {
            ex.getResponseHeaders().add("Location", "/relay3");
            reply(ex, 302, "");
        });
        RelayException ex = assertThrows(RelayException.class, () -> service.call(executor(), sampleRequest()));
        assertEquals(RelayException.CODE_INVALID_RESPONSE, ex.getCode());
        assertEquals(2, receivedPaths.size());
    }

    @Test
    void redirectToAnotherHostIsRefusedAndTokenNotSent() {
        handle("/relay", ex -> {
            ex.getResponseHeaders().add("Location", "http://localhost:" + server.getAddress().getPort() + "/relay");
            reply(ex, 301, "");
        });
        RelayException ex = assertThrows(RelayException.class, () -> service.call(executor(), sampleRequest()));
        assertEquals(RelayException.CODE_INVALID_RESPONSE, ex.getCode());
        assertEquals(1, receivedPaths.size());
    }

    @Test
    void checkOk() throws Exception {
        handle("/relay/check", ex -> reply(ex, 200, "{\"ok\":true,\"version\":1,\"runnerId\":\"r1\"}"));
        service.check(executor());
        assertEquals("GET", receivedMethods.get(0));
        assertEquals("Bearer " + TOKEN, receivedAuth.get(0));
    }

    private RelayException checkFailure(int status, String body) {
        handle("/relay/check", ex -> reply(ex, status, body));
        return assertThrows(RelayException.class, () -> service.check(executor()));
    }

    @Test
    void checkFailures() {
        assertEquals(RelayException.CODE_UNAUTHORIZED, checkFailure(401, "{\"error\":\"no\",\"code\":\"unauthorized\"}").getCode());
    }

    @Test
    void checkRelayStopped() {
        assertEquals(RelayException.CODE_RELAY_STOPPED, checkFailure(503, "{\"code\":\"relay_stopped\"}").getCode());
    }

    @Test
    void checkOldRunnerWithoutRelay() {
        assertEquals(RelayException.CODE_UNSUPPORTED, checkFailure(404, "Not Found").getCode());
    }

    @Test
    void checkOkFalse() {
        assertEquals(RelayException.CODE_INVALID_RESPONSE, checkFailure(200, "{\"ok\":false}").getCode());
    }

    @Test
    void checkUnreachableDoesNotLeakToken() {
        RobotExecutor e = executor();
        server.stop(0);
        RelayException ex = assertThrows(RelayException.class, () -> service.check(e));
        assertEquals(RelayException.CODE_UNREACHABLE, ex.getCode());
        assertFalse(ex.getMessage().contains(TOKEN));
    }

    @Test
    void missingHostOrTokenIsNotConfigured() {
        RobotExecutor noHost = executor();
        noHost.setExecutorProxyServiceHost("");
        assertEquals(RelayException.CODE_NOT_CONFIGURED, assertThrows(RelayException.class, () -> service.check(noHost)).getCode());
        RobotExecutor noToken = executor();
        noToken.setExecutorProxyAuthToken(null);
        assertEquals(RelayException.CODE_NOT_CONFIGURED, assertThrows(RelayException.class, () -> service.call(noToken, sampleRequest())).getCode());
    }

    @Test
    void baseUrlConvention() throws Exception {
        RobotExecutor e = executor();
        e.setExecutorProxyServiceHost("runner.local");
        e.setExecutorProxyServicePort(8091);
        assertEquals("http://runner.local:8091", service.getBaseUrl(e));
        e.setExecutorProxyServicePort(443);
        assertEquals("https://runner.local", service.getBaseUrl(e));
    }

    @Test
    void noAuthenticationSendsNoAuthorizationHeader() throws Exception {
        handle("/relay/check", ex -> reply(ex, 200, "{\"ok\":true}"));
        RobotExecutor none = executor();
        none.setExecutorProxyAuthMode(RobotExecutor.PROXY_AUTH_NONE);
        service.check(none);
        assertEquals(1, receivedAuth.size());
        assertEquals(null, receivedAuth.get(0));
    }

    @Test
    void oauthGetsAndCachesTheAccessToken() throws Exception {
        final int[] tokenCalls = {0};
        server.createContext("/token", ex -> {
            tokenCalls[0]++;
            String form = readAll(ex.getRequestBody());
            assertTrue(form.contains("grant_type=client_credentials"));
            assertTrue(form.contains("client_id=cerberus-backend"));
            reply(ex, 200, "{\"access_token\":\"jwt-abc\",\"expires_in\":300}");
        });
        handle("/relay/check", ex -> reply(ex, 200, "{\"ok\":true}"));
        RobotExecutor oauth = executor();
        oauth.setExecutorProxyAuthMode(RobotExecutor.PROXY_AUTH_OAUTH);
        oauth.setExecutorProxyOauthTokenUrl("http://127.0.0.1:" + server.getAddress().getPort() + "/token");
        oauth.setExecutorProxyOauthClientId("cerberus-backend");
        oauth.setExecutorProxyOauthClientSecret("client-secret-123");
        service.check(oauth);
        service.check(oauth);
        assertEquals("Bearer jwt-abc", receivedAuth.get(0));
        assertEquals("Bearer jwt-abc", receivedAuth.get(1));
        assertEquals(1, tokenCalls[0]);
        for (String m : appender.messages) {
            assertFalse(m.contains("jwt-abc") || m.contains("client-secret-123"), "secret found in log : " + m);
        }
    }

    @Test
    void oauthWithoutParametersIsNotConfigured() {
        RobotExecutor oauth = executor();
        oauth.setExecutorProxyAuthMode(RobotExecutor.PROXY_AUTH_OAUTH);
        assertEquals(RelayException.CODE_NOT_CONFIGURED, assertThrows(RelayException.class, () -> service.check(oauth)).getCode());
    }

    @Test
    void relayActiveOnlyWithExecutorAndFlag() {
        assertFalse(service.isRelayActive(null));
        RobotExecutor e = executor();
        assertTrue(service.isRelayActive(e));
        e.setExecutorProxyType(RobotExecutor.PROXY_TYPE_MANUAL);
        assertFalse(service.isRelayActive(e));
        e.setExecutorProxyType(RobotExecutor.PROXY_TYPE_MITMPROXY);
        e.setExecutorProxyAuthMode(RobotExecutor.PROXY_AUTH_NONE);
        assertFalse(service.isRelayActive(e));
        e.setExecutorProxyAuthMode(RobotExecutor.PROXY_AUTH_OAUTH);
        assertTrue(service.isRelayActive(e));
        e.setExecutorProxyType(RobotExecutor.PROXY_TYPE_MITMPROXY);
        e.setRelayActive(false);
        assertFalse(service.isRelayActive(e));
    }

    @Test
    void tokenIsNeverLogged() throws Exception {
        handle("/relay", ex -> reply(ex, 200, "{\"status\":200,\"statusText\":\"OK\",\"headers\":[],\"bodyBase64\":\"\"}"));
        handle("/relay/check", ex -> reply(ex, 401, "{\"code\":\"unauthorized\"}"));
        service.call(executor(), sampleRequest());
        assertThrows(RelayException.class, () -> service.check(executor()));
        for (String m : appender.messages) {
            assertFalse(m.contains(TOKEN), "token found in log : " + m);
        }
    }

}
