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
package org.cerberus.core.config.security;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Serves the OAuth 2.0 Protected Resource Metadata (RFC 9728) for the MCP
 * endpoint, allowing standards-compliant MCP clients to discover the
 * authorization server (Keycloak) without manual token handling.
 *
 * Mapped on {@code /.well-known/oauth-protected-resource}. Only meaningful when
 * the {@code keycloak} profile is configured : when the Keycloak system
 * properties are absent (e.g. {@code local} profile) it returns 404, since
 * there is no OAuth authorization server to advertise.
 *
 * @author bcivel
 */
public class OAuthProtectedResourceMetadataServlet extends HttpServlet {

    private static final Logger LOG = LogManager.getLogger(OAuthProtectedResourceMetadataServlet.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String keycloakUrl = System.getProperty("org.cerberus.keycloak.url");
        String realm = System.getProperty("org.cerberus.keycloak.realm");

        if (keycloakUrl == null || realm == null) {
            // No OAuth authorization server configured (e.g. local profile).
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String issuer = keycloakUrl + "/realms/" + realm;
        String resource = baseUrl(request) + "/mcp";

        JSONObject metadata = new JSONObject();
        metadata.put("resource", resource);
        metadata.put("authorization_servers", new JSONArray().put(issuer));
        metadata.put("scopes_supported", new JSONArray().put("openid").put("profile").put("email"));
        metadata.put("bearer_methods_supported", new JSONArray().put("header"));

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.setHeader("Cache-Control", "public, max-age=3600");
        response.getWriter().write(metadata.toString());

        LOG.debug("Served OAuth protected resource metadata for {}", resource);
    }

    /**
     * Builds {@code scheme://host[:port]/context} from the incoming request,
     * omitting the port when it is the default for the scheme.
     */
    static String baseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        int port = request.getServerPort();
        StringBuilder base = new StringBuilder(scheme).append("://").append(request.getServerName());
        boolean defaultPort = ("http".equals(scheme) && port == 80) || ("https".equals(scheme) && port == 443);
        if (!defaultPort && port > 0) {
            base.append(':').append(port);
        }
        base.append(request.getContextPath());
        return base.toString();
    }
}