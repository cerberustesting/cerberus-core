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
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.entity.RobotExecutor;
import org.cerberus.core.service.robotproxy.IProxyAuthService;
import org.cerberus.core.service.robotproxy.entity.RelayException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

/**
 * See {@link IProxyAuthService}. In mode OAUTH, the access token is obtained
 * with the client credentials grant (service account) and kept in memory until it
 * expires. Secrets are never logged.
 *
 * @author bcivel
 */
@Service
public class ProxyAuthService implements IProxyAuthService {

    private static final Logger LOG = LogManager.getLogger(ProxyAuthService.class);

    private static final int OAUTH_TIMEOUT_MS = 10000;
    private static final long EXPIRY_MARGIN_MS = 30000;

    private static class CachedToken {

        final String accessToken;
        final long expiryMs;

        CachedToken(String accessToken, long expiryMs) {
            this.accessToken = accessToken;
            this.expiryMs = expiryMs;
        }
    }

    /**
     * Access tokens by identity provider + client.
     */
    private final Map<String, CachedToken> cache = new HashMap<>();

    private static String cacheKey(RobotExecutor e) {
        return e.getExecutorProxyOauthTokenUrl() + "|" + e.getExecutorProxyOauthClientId() + "|" + e.getExecutorProxyOauthClientSecret();
    }

    @Override
    public String getAuthorizationHeader(RobotExecutor executor) throws RelayException {
        String mode = executor == null || isEmpty(executor.getExecutorProxyAuthMode()) ? RobotExecutor.PROXY_AUTH_NONE : executor.getExecutorProxyAuthMode().toUpperCase();
        switch (mode) {
            case RobotExecutor.PROXY_AUTH_NONE:
                return null;
            case RobotExecutor.PROXY_AUTH_TOKEN:
                if (isEmpty(executor.getExecutorProxyAuthToken())) {
                    throw new RelayException(RelayException.CODE_NOT_CONFIGURED, 0,
                            "The proxy authentication is TOKEN but no token is defined (robot : " + executor.getRobot() + ", executor : " + executor.getExecutor() + ").");
                }
                return "Bearer " + executor.getExecutorProxyAuthToken().trim();
            case RobotExecutor.PROXY_AUTH_OAUTH:
                return "Bearer " + getOauthAccessToken(executor);
            default:
                throw new RelayException(RelayException.CODE_NOT_CONFIGURED, 0,
                        "Unknown proxy authentication '" + mode + "' (NONE, TOKEN or OAUTH).");
        }
    }

    @Override
    public List<String> getSecrets(RobotExecutor executor) {
        List<String> secrets = new ArrayList<>();
        if (executor == null) {
            return secrets;
        }
        for (String v : new String[]{executor.getExecutorProxyAuthToken(), executor.getExecutorProxyOauthClientSecret()}) {
            if (!isEmpty(v)) {
                secrets.add(v);
            }
        }
        synchronized (cache) {
            CachedToken cached = cache.get(cacheKey(executor));
            if (cached != null) {
                secrets.add(cached.accessToken);
            }
        }
        return secrets;
    }

    private String getOauthAccessToken(RobotExecutor executor) throws RelayException {
        String tokenUrl = executor.getExecutorProxyOauthTokenUrl();
        String clientId = executor.getExecutorProxyOauthClientId();
        String clientSecret = executor.getExecutorProxyOauthClientSecret();
        if (isEmpty(tokenUrl) || isEmpty(clientId) || isEmpty(clientSecret)) {
            throw new RelayException(RelayException.CODE_NOT_CONFIGURED, 0,
                    "The proxy authentication is OAUTH but the token URL, client id or client secret is missing (robot : "
                    + executor.getRobot() + ", executor : " + executor.getExecutor() + ").");
        }
        String key = cacheKey(executor);
        long now = System.currentTimeMillis();
        synchronized (cache) {
            CachedToken cached = cache.get(key);
            if (cached != null && now < cached.expiryMs - EXPIRY_MARGIN_MS) {
                return cached.accessToken;
            }
        }
        String form = "grant_type=client_credentials"
                + "&client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8)
                + "&client_secret=" + URLEncoder.encode(clientSecret, StandardCharsets.UTF_8);
        try {
            HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofMillis(OAUTH_TIMEOUT_MS)).build();
            HttpRequest request = HttpRequest.newBuilder(URI.create(tokenUrl.trim()))
                    .timeout(Duration.ofMillis(OAUTH_TIMEOUT_MS))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(form))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new RelayException(RelayException.CODE_UNAUTHORIZED, response.statusCode(),
                        "The identity provider refused the client credentials of '" + clientId + "' (http " + response.statusCode() + ").");
            }
            JSONObject json = new JSONObject(response.body());
            String accessToken = json.getString("access_token");
            long expiresInMs = json.optLong("expires_in", 60) * 1000L;
            synchronized (cache) {
                cache.put(key, new CachedToken(accessToken, now + expiresInMs));
            }
            LOG.debug("Access token for the Cerberus Proxy obtained (expires in {} s)", expiresInMs / 1000);
            return accessToken;
        } catch (RelayException ex) {
            throw ex;
        } catch (IOException | RuntimeException ex) {
            throw new RelayException(RelayException.CODE_UNREACHABLE, 0,
                    "Could not get the access token for the proxy from '" + tokenUrl + "' : " + ex.getClass().getSimpleName(), ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new RelayException(RelayException.CODE_UNREACHABLE, 0, "Interrupted while getting the access token for the proxy.", ex);
        }
    }

    private static boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

}
