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

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.api.services.PublicApiAuthenticationService;
import org.cerberus.core.crud.entity.Parameter;
import org.cerberus.core.crud.service.IParameterService;
import org.cerberus.core.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Security filter protecting the MCP endpoint (/mcp).
 *
 * The MCP endpoint is served by a raw servlet (see WebAppInitializer), so it is
 * not routed through the DispatcherServlet : a Spring MVC HandlerInterceptor
 * would never fire. The Spring Security filter chain however does wrap it
 * (filter mapped on "/*"), hence the authentication is enforced here.
 *
 * Behaviour driven by the {@code cerberus_mcp_enable} parameter :
 * <ul>
 *   <li>parameter false / absent (default) : MCP is disabled, every call is rejected (403).</li>
 *   <li>parameter true : the caller must be authenticated (401 otherwise).</li>
 * </ul>
 *
 * Authentication resolution order (first match wins) :
 * <ol>
 *   <li>An {@link Authentication} already set in the {@link SecurityContextHolder}
 *       by an upstream filter : HTTP Basic ({@code local} profile) or Bearer JWT
 *       ({@code keycloak} profile). The resolved Cerberus / Keycloak authorities
 *       are preserved and augmented with {@code ROLE_MCP}.</li>
 *   <li>Fallback : a valid {@code X-API-KEY} header, mapped to its Cerberus user.</li>
 * </ol>
 *
 * @author bcivel
 */
@Component
public class McpApiKeyAuthFilter extends OncePerRequestFilter {

    private static final Logger LOG = LogManager.getLogger(McpApiKeyAuthFilter.class);

    private static final String API_KEY_HEADER = "X-API-KEY";

    @Autowired
    private IParameterService parameterService;

    @Autowired
    private PublicApiAuthenticationService authenticationService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // MCP feature toggle : disabled by default until the parameter is created.
        if (!parameterService.getParameterBooleanByKey(Parameter.VALUE_cerberus_mcp_enable, "", false)) {
            LOG.warn("MCP access refused (cerberus_mcp_enable is disabled) from {}", request.getRemoteAddr());
            writeJsonRpcError(response, HttpServletResponse.SC_FORBIDDEN, -32002, "MCP is disabled");
            return;
        }

        // 1. Already authenticated upstream (HTTP Basic / Bearer JWT) ?
        Authentication existing = SecurityContextHolder.getContext().getAuthentication();
        if (existing != null && existing.isAuthenticated()
                && !(existing instanceof AnonymousAuthenticationToken)
                && StringUtil.isNotEmptyOrNull(existing.getName())) {

            String login = existing.getName();
            // Preserve the real Cerberus / Keycloak authorities and add ROLE_MCP.
            List<GrantedAuthority> authorities = new ArrayList<>(existing.getAuthorities());
            authorities.add(new SimpleGrantedAuthority("ROLE_MCP"));

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(login, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            request.setAttribute("authenticatedLogin", login);

            filterChain.doFilter(request, response);
            return;
        }

        // 2. Fallback : X-API-KEY header.
        String apiKey = request.getHeader(API_KEY_HEADER);

        try {
            String login = authenticationService.authenticateLogin(null, apiKey);

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    login, null, List.of(new SimpleGrantedAuthority("ROLE_MCP")));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            request.setAttribute("authenticatedLogin", login);

            filterChain.doFilter(request, response);

        } catch (BadCredentialsException ex) {
            LOG.warn("Unauthorized MCP access from {}", request.getRemoteAddr());
            response.setHeader("WWW-Authenticate", buildAuthenticateChallenge(request));
            writeJsonRpcError(response, HttpServletResponse.SC_UNAUTHORIZED, -32001, "Unauthorized");
        }
    }

    /**
     * Builds the {@code WWW-Authenticate} challenge so MCP clients can discover
     * how to authenticate :
     * <ul>
     *   <li>keycloak configured : {@code Bearer resource_metadata="…"} pointing
     *       to the RFC 9728 metadata document, enabling the OAuth flow.</li>
     *   <li>otherwise (local profile) : {@code Basic realm="Cerberus MCP"}.</li>
     * </ul>
     */
    private String buildAuthenticateChallenge(HttpServletRequest request) {
        if (System.getProperty("org.cerberus.keycloak.url") != null) {
            String metadataUrl = OAuthProtectedResourceMetadataServlet.baseUrl(request)
                    + "/.well-known/oauth-protected-resource";
            return "Bearer resource_metadata=\"" + metadataUrl + "\"";
        }
        return "Basic realm=\"Cerberus MCP\"";
    }

    private void writeJsonRpcError(HttpServletResponse response, int httpStatus, int code, String message) throws IOException {
        response.setStatus(httpStatus);
        response.setContentType("application/json");
        response.getWriter().write(
                "{\"jsonrpc\":\"2.0\",\"error\":{\"code\":" + code + ",\"message\":\"" + message + "\"}}"
        );
    }
}