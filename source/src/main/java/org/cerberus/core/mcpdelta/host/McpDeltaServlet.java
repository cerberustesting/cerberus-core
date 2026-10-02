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
package org.cerberus.core.mcpdelta.host;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.config.security.McpApiKeyAuthFilter;
import org.cerberus.core.mcpdelta.McpEndpoint;
import org.cerberus.core.mcpdelta.util.CallContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.support.WebApplicationContextUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * The MCP Delta endpoint, /mcpdelta/mcp: authenticated upstream by the same filter chain as /mcp (HTTP Basic or
 * a Keycloak Bearer token, then an X-API-KEY; gated by the cerberus_mcpdelta_enable parameter), then handed to
 * {@link McpEndpoint} as the authenticated user, with that user's Cerberus roles.
 */
public class McpDeltaServlet extends HttpServlet {

    private static final Logger LOG = LogManager.getLogger(McpDeltaServlet.class);
    private static final int MAX_BODY = 10 * 1024 * 1024;

    private transient McpDeltaHost host;

    @Override
    public void init() {
        host = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext()).getBean(McpDeltaHost.class);
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!originAllowed(request)) {
            LOG.warn("MCP Delta call refused: origin {} not allowed", request.getHeader("Origin"));
            write(response, new McpEndpoint.Response(403, "application/json",
                    "{\"jsonrpc\":\"2.0\",\"error\":{\"code\":-32003,\"message\":\"Origin not allowed\"}}", java.util.Map.of()));
            return;
        }
        Object login = request.getAttribute(McpApiKeyAuthFilter.AUTHENTICATED_LOGIN_ATTR);
        if (login == null) {
            // The security filter answers 401 before this point; never serve an unauthenticated call.
            write(response, new McpEndpoint.Response(401, "application/json",
                    "{\"jsonrpc\":\"2.0\",\"error\":{\"code\":-32001,\"message\":\"Unauthorized\"}}", java.util.Map.of()));
            return;
        }
        String body = "";
        if ("POST".equals(request.getMethod())) {
            try (InputStream in = request.getInputStream()) {
                byte[] bytes = in.readNBytes(MAX_BODY + 1);
                if (bytes.length > MAX_BODY) {
                    write(response, new McpEndpoint.Response(413, "text/plain", "request too large", java.util.Map.of()));
                    return;
                }
                body = new String(bytes, StandardCharsets.UTF_8);
            }
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CallContext.caller(login.toString(), host.rolesOf(login.toString(), auth == null ? List.of() : auth.getAuthorities()));
        try {
            write(response, host.endpoint().handle(request.getMethod(), body, request::getHeader));
        } finally {
            CallContext.clear();
        }
    }

    /**
     * DNS-rebinding protection, as the MCP specification asks of HTTP servers: a browser request (it carries an
     * Origin) is served only from the instance's own origin or an origin listed in org.cerberus.mcpdelta.allowedOrigins.
     * Clients that are not browsers (Claude Code, server-side connectors) send no Origin.
     */
    private static boolean originAllowed(HttpServletRequest request) {
        String origin = request.getHeader("Origin");
        if (origin == null || origin.isBlank() || "null".equals(origin)) {
            return origin == null || origin.isBlank();
        }
        try {
            URI o = URI.create(origin.trim());
            int port = o.getPort() > 0 ? o.getPort() : ("https".equals(o.getScheme()) ? 443 : 80);
            if (request.getServerName().equalsIgnoreCase(o.getHost()) && request.getServerPort() == port) {
                return true;
            }
        } catch (IllegalArgumentException e) {
            return false;
        }
        String allowed = System.getProperty("org.cerberus.mcpdelta.allowedOrigins", "");
        return Arrays.stream(allowed.split(",")).map(String::trim).anyMatch(a -> !a.isEmpty() && a.equalsIgnoreCase(origin.trim()));
    }

    private static void write(HttpServletResponse response, McpEndpoint.Response r) throws IOException {
        response.setStatus(r.status());
        r.headers().forEach(response::setHeader);
        if (r.status() == 202 || r.status() == 204) {
            return;
        }
        response.setContentType(r.contentType());
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(r.body());
    }
}
