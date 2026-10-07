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

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.entity.RobotExecutor;
import org.cerberus.core.service.robotproxy.IProxyAuthService;
import org.cerberus.core.service.robotproxy.IRelayService;
import org.cerberus.core.service.robotproxy.entity.RelayException;
import org.cerberus.core.service.robotproxy.entity.RelayRequest;
import org.cerberus.core.service.robotproxy.entity.RelayResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Client of the relay service of the Cerberus Proxy (GET /relay/check and POST
 * /relay). Authentication of the executor comes from {@link IProxyAuthService} (NONE, TOKEN or
 * OAUTH). Secrets are never logged nor put in an exception message.
 *
 * @author bcivel
 */
@Service
public class RelayService implements IRelayService {

    private static final Logger LOG = LogManager.getLogger(RelayService.class);

    /**
     * Margin added to the timeout of the call to let the relay answer with its
     * own timeout error.
     */
    private static final int TIMEOUT_MARGIN_MS = 10000;
    private static final int MAX_RELAY_TIMEOUT_MS = 600000;
    private static final int CHECK_TIMEOUT_MS = 15000;

    @Autowired
    private IProxyAuthService proxyAuthService;

    @Override
    public boolean isRelayActive(RobotExecutor executor) {
        // The relay is a service of the Cerberus Proxy : it only applies when the executor uses the Cerberus Proxy.
        // It also requires an authentication on the proxy (TOKEN or OAUTH).
        return executor != null && executor.isRelayActive()
                && (RobotExecutor.PROXY_TYPE_MITMPROXY.equals(executor.getExecutorProxyType())
                || RobotExecutor.PROXY_TYPE_NETWORKTRAFFIC.equals(executor.getExecutorProxyType()))
                && executor.getExecutorProxyAuthMode() != null
                && !RobotExecutor.PROXY_AUTH_NONE.equalsIgnoreCase(executor.getExecutorProxyAuthMode());
    }

    @Override
    public String getBaseUrl(RobotExecutor executor) throws RelayException {
        if (executor == null || executor.getExecutorProxyServiceHost() == null || executor.getExecutorProxyServiceHost().trim().isEmpty()) {
            throw new RelayException(RelayException.CODE_NOT_CONFIGURED, 0,
                    "The relay is active but no Proxy service host is defined (robot : "
                    + (executor == null ? "" : executor.getRobot()) + ", executor : " + (executor == null ? "" : executor.getExecutor()) + ").");
        }
        String host = executor.getExecutorProxyServiceHost().trim();
        Integer port = executor.getExecutorProxyServicePort();
        if (port != null && port == 443) {
            return "https://" + host;
        }
        if (port == null || port <= 0) {
            return "http://" + host;
        }
        return "http://" + host + ":" + port;
    }

    private String getAddress(RobotExecutor executor) {
        return executor.getExecutorProxyServiceHost() + (executor.getExecutorProxyServicePort() == null ? "" : ":" + executor.getExecutorProxyServicePort());
    }

    /**
     * The JDK http client is used on purpose : it does not log the headers (the
     * Authorization header would be exposed by the debug logs of Apache
     * HttpClient wire logging).
     */
    private HttpClient newClient(int timeoutMs) {
        return HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.NEVER)
                .connectTimeout(Duration.ofMillis(timeoutMs))
                .build();
    }

    @Override
    public void check(RobotExecutor executor) throws RelayException {
        String base = getBaseUrl(executor);
        String authorization = proxyAuthService.getAuthorizationHeader(executor);
        String address = getAddress(executor);
        LOG.debug("Checking relay : {}/relay/check", base);

        try {
            RawResponse response = execute(newClient(CHECK_TIMEOUT_MS), URI.create(base + "/relay/check"), authorization, null, CHECK_TIMEOUT_MS);
            if (response.status == 200) {
                try {
                    if (new JSONObject(response.body).optBoolean("ok", false)) {
                        return;
                    }
                } catch (Exception ex) {
                    // Handled below as invalid response.
                }
                throw new RelayException(RelayException.CODE_INVALID_RESPONSE, response.status,
                        "The relay of the runner '" + address + "' answered an unexpected check response.");
            }
            throw toRelayException(response.status, response.body, address);
        } catch (RelayException ex) {
            throw ex;
        } catch (Exception ex) {
            throw ioFailure(ex, executor, address, CHECK_TIMEOUT_MS, false);
        }
    }

    @Override
    public RelayResponse call(RobotExecutor executor, RelayRequest request) throws RelayException {
        String base = getBaseUrl(executor);
        String authorization = proxyAuthService.getAuthorizationHeader(executor);
        String address = getAddress(executor);

        int callTimeout = request.getTimeoutMs() > 0 ? Math.min(request.getTimeoutMs(), MAX_RELAY_TIMEOUT_MS) : 60000;
        byte[] payload = toJson(request, callTimeout).toString().getBytes(StandardCharsets.UTF_8);
        LOG.debug("Calling relay {}/relay for {} {}", base, request.getMethod(), request.getUrl());

        try {
            RawResponse response = execute(newClient(callTimeout + TIMEOUT_MARGIN_MS), URI.create(base + "/relay"), authorization, payload, callTimeout + TIMEOUT_MARGIN_MS);
            if (response.status != 200) {
                throw toRelayException(response.status, response.body, address);
            }
            return parseResponse(response.body, address);
        } catch (RelayException ex) {
            throw ex;
        } catch (Exception ex) {
            throw ioFailure(ex, executor, address, callTimeout + TIMEOUT_MARGIN_MS, true);
        }
    }

    private RelayException ioFailure(Exception ex, RobotExecutor executor, String address, int timeoutMs, boolean isCall) {
        if (ex instanceof InterruptedException) {
            Thread.currentThread().interrupt();
        }
        if (ex instanceof HttpTimeoutException && !(ex instanceof HttpConnectTimeoutException)) {
            return new RelayException(isCall ? RelayException.CODE_TIMEOUT : RelayException.CODE_UNREACHABLE, 0,
                    "No answer from the relay of the runner '" + address + "' after " + timeoutMs + " ms.", ex);
        }
        return new RelayException(RelayException.CODE_UNREACHABLE, 0,
                "Could not reach the relay of the runner '" + address + "' : " + ex.getClass().getSimpleName() + " " + safe(ex.getMessage(), executor), ex);
    }

    private static class RawResponse {

        final int status;
        final String body;

        RawResponse(int status, String body) {
            this.status = status;
            this.body = body;
        }
    }

    /**
     * Executes the request (GET, or POST when payload is defined), following
     * once a 301/302/307/308 while keeping the method and the body. The token
     * is only sent again if the redirection stays on the same host.
     */
    private RawResponse execute(HttpClient client, URI uri, String authorization, byte[] payload, int timeoutMs) throws IOException, InterruptedException, RelayException {
        URI current = uri;
        for (int attempt = 0; attempt < 2; attempt++) {
            HttpRequest.Builder builder = HttpRequest.newBuilder(current)
                    .timeout(Duration.ofMillis(timeoutMs))
                    .header("Accept", "application/json");
            if (authorization != null) {
                builder.header("Authorization", authorization);
            }
            if (payload != null) {
                builder.header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofByteArray(payload));
            } else {
                builder.GET();
            }
            HttpResponse<byte[]> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofByteArray());
            int status = response.statusCode();
            if (attempt == 0 && (status == 301 || status == 302 || status == 307 || status == 308)) {
                String location = response.headers().firstValue("Location").orElse(null);
                if (location == null) {
                    throw new RelayException(RelayException.CODE_INVALID_RESPONSE, status, "The runner answered a redirection without location.");
                }
                URI target = current.resolve(location);
                if (!("http".equalsIgnoreCase(target.getScheme()) || "https".equalsIgnoreCase(target.getScheme()))
                        || target.getHost() == null || !target.getHost().equalsIgnoreCase(current.getHost())) {
                    throw new RelayException(RelayException.CODE_INVALID_RESPONSE, status, "The runner redirected to another host, redirection refused.");
                }
                LOG.debug("Relay redirected to {}", target);
                current = target;
                continue;
            }
            return new RawResponse(status, new String(response.body(), StandardCharsets.UTF_8));
        }
        throw new RelayException(RelayException.CODE_INVALID_RESPONSE, 0, "Too many redirections from the runner.");
    }

    private JSONObject toJson(RelayRequest request, int timeoutMs) {
        JSONObject json = new JSONObject();
        json.put("method", request.getMethod());
        json.put("url", request.getUrl());
        JSONObject headers = new JSONObject();
        for (Map.Entry<String, List<String>> entry : request.getHeaders().entrySet()) {
            List<String> values = entry.getValue();
            if (values.size() == 1) {
                headers.put(entry.getKey(), values.get(0));
            } else {
                headers.put(entry.getKey(), new JSONArray(values));
            }
        }
        json.put("headers", headers);
        if (request.getBody() != null && request.getBody().length > 0) {
            json.put("bodyBase64", Base64.getEncoder().encodeToString(request.getBody()));
        }
        json.put("followRedirects", request.isFollowRedirects());
        json.put("timeoutMs", timeoutMs);
        json.put("acceptUnsignedSsl", request.isAcceptUnsignedSsl());
        return json;
    }

    private RelayResponse parseResponse(String body, String address) throws RelayException {
        try {
            JSONObject json = new JSONObject(body);
            RelayResponse result = new RelayResponse();
            result.setStatus(json.getInt("status"));
            result.setStatusText(json.optString("statusText", ""));
            JSONArray headers = json.optJSONArray("headers");
            if (headers != null) {
                for (int i = 0; i < headers.length(); i++) {
                    JSONArray pair = headers.getJSONArray(i);
                    result.getHeaders().add(new String[]{pair.getString(0), pair.optString(1, "")});
                }
            }
            String b64 = json.optString("bodyBase64", "");
            result.setBody(b64.isEmpty() ? new byte[0] : Base64.getDecoder().decode(b64));
            result.setTruncated(json.optBoolean("truncated", false));
            result.setDurationMs(json.optLong("durationMs", 0));
            result.setFinalUrl(json.optString("finalUrl", null));
            return result;
        } catch (Exception ex) {
            throw new RelayException(RelayException.CODE_INVALID_RESPONSE, 200, "The relay of the runner '" + address + "' returned an unreadable response : " + ex.getClass().getSimpleName(), ex);
        }
    }

    /**
     * Translates an error of the relay (non 200) into an exception.
     */
    private RelayException toRelayException(int status, String body, String address) {
        String code = null;
        String error = null;
        try {
            JSONObject json = new JSONObject(body);
            code = json.optString("code", null);
            error = json.optString("error", null);
        } catch (Exception ex) {
            // Body is not JSON.
        }
        String detail = (error == null || error.isEmpty()) ? "" : " (" + error + ")";
        String prefix = "Relay of the runner '" + address + "' ";
        if (code == null) {
            // Check endpoint answers or old runner.
            if (status == 401) {
                code = RelayException.CODE_UNAUTHORIZED;
            } else if (status == 404) {
                code = RelayException.CODE_UNSUPPORTED;
            } else if (status == 503) {
                code = RelayException.CODE_RELAY_STOPPED;
            }
        }
        if (code == null) {
            return new RelayException(RelayException.CODE_INVALID_RESPONSE, status, prefix + "answered an unexpected http status " + status + "." + detail);
        }
        switch (code) {
            case RelayException.CODE_UNAUTHORIZED:
                return new RelayException(code, status, prefix + "refused the authentication (http 401). Check the proxy authentication of the robot executor." + detail);
            case RelayException.CODE_RELAY_STOPPED:
                return new RelayException(code, status, prefix + "is stopped (http 503). Start the relay on the runner." + detail);
            case RelayException.CODE_UNSUPPORTED:
                return new RelayException(code, status, prefix + "does not exist (http 404). The runner is probably too old, please upgrade it.");
            case RelayException.CODE_TARGET_BLOCKED:
                return new RelayException(code, status, prefix + "blocked the target (http 403)." + detail);
            case RelayException.CODE_CONNECT_FAILED:
                return new RelayException(code, status, prefix + "could not connect to the target (http 502)." + detail);
            case RelayException.CODE_TIMEOUT:
                return new RelayException(code, status, prefix + "reached a timeout when calling the target (http 504)." + detail);
            case RelayException.CODE_REQUEST_TOO_LARGE:
                return new RelayException(code, status, prefix + "refused the request because it is too large (http 413)." + detail);
            case RelayException.CODE_TOO_MANY_REQUESTS:
                return new RelayException(code, status, prefix + "refused the request because of too many requests (http 429)." + detail);
            case RelayException.CODE_INVALID_REQUEST:
                return new RelayException(code, status, prefix + "refused the request as invalid (http 400)." + detail);
            default:
                return new RelayException(code, status, prefix + "failed with code '" + code + "' (http " + status + ")." + detail);
        }
    }

    private String safe(String message, RobotExecutor executor) {
        if (message == null) {
            return "";
        }
        String result = message;
        for (String secret : proxyAuthService.getSecrets(executor)) {
            result = result.replace(secret, "***");
        }
        return result;
    }

}
