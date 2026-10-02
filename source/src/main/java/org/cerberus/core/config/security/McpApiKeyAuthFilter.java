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
import org.cerberus.core.crud.entity.Parameter;
import org.cerberus.core.crud.service.IParameterService;
import org.cerberus.core.crud.service.IUserService;
import org.cerberus.core.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
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
 *   <li>Fallback : a valid {@code X-API-KEY} header, mapped to its Cerberus user.
 *       Checked directly against {@link IUserService#verifyAPIKey(String)} rather
 *       than through the legacy public-API auth path, since that path is also
 *       gated on the unrelated {@code cerberus_apikey_enable} parameter — {@code
 *       cerberus_mcp_enable} is already the sufficient gate for this endpoint.</li>
 * </ol>
 *
 * @author bcivel
 */
@Component
public class McpApiKeyAuthFilter extends OncePerRequestFilter {

    private static final Logger LOG = LogManager.getLogger(McpApiKeyAuthFilter.class);

    private static final String API_KEY_HEADER = "X-API-KEY";

    /**
     * Request attribute carrying the resolved Cerberus login, set here and read back by
     * {@link org.cerberus.core.config.cerberus.WebAppInitializer}'s MCP transport context
     * extractor so tool handlers can identify the caller via {@code exchange.transportContext()}.
     */
    public static final String AUTHENTICATED_LOGIN_ATTR = "authenticatedLogin";

    @Autowired
    private IParameterService parameterService;

    @Autowired
    private IUserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // MCP feature toggle : disabled by default until the parameter is created.
        // /mcpdelta/mcp (MCP Delta) has its own switch, cerberus_mcpdelta_enable.
        boolean delta = isMcpDelta(request);
        String toggle = delta ? Parameter.VALUE_cerberus_mcpdelta_enable : Parameter.VALUE_cerberus_mcp_enable;
        if (!parameterService.getParameterBooleanByKey(toggle, "", false)) {
            LOG.warn("MCP access refused ({} is disabled) from {}", toggle, request.getRemoteAddr());
            writeJsonRpcError(response, HttpServletResponse.SC_FORBIDDEN, -32002, delta ? "MCP Delta is disabled" : "MCP is disabled");
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
            request.setAttribute(AUTHENTICATED_LOGIN_ATTR, login);

            filterChain.doFilter(request, response);
            return;
        }

        // 2. Fallback : X-API-KEY header.
        String apiKey = request.getHeader(API_KEY_HEADER);
        String login = StringUtil.isEmptyOrNull(apiKey) ? null : userService.verifyAPIKey(apiKey);

        if (login == null) {
            LOG.warn("Unauthorized MCP access from {}", request.getRemoteAddr());
            response.setHeader("WWW-Authenticate", buildAuthenticateChallenge(request));
            writeJsonRpcError(response, HttpServletResponse.SC_UNAUTHORIZED, -32001, "Unauthorized");
            return;
        }

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                login, null, List.of(new SimpleGrantedAuthority("ROLE_MCP")));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        request.setAttribute(AUTHENTICATED_LOGIN_ATTR, login);

        filterChain.doFilter(request, response);
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
                    + (isMcpDelta(request) ? "/.well-known/oauth-protected-resource/mcpdelta/mcp" : "/.well-known/oauth-protected-resource");
            return "Bearer resource_metadata=\"" + metadataUrl + "\"";
        }
        return isMcpDelta(request) ? "Basic realm=\"Cerberus MCP Delta\"" : "Basic realm=\"Cerberus MCP\"";
    }

    /** The MCP Delta endpoint (/mcpdelta/mcp), served by McpDeltaServlet behind this same filter. */
    static boolean isMcpDelta(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri != null && uri.endsWith("/mcpdelta/mcp");
    }

    private void writeJsonRpcError(HttpServletResponse response, int httpStatus, int code, String message) throws IOException {
        response.setStatus(httpStatus);
        response.setContentType("application/json");
        response.getWriter().write(
                "{\"jsonrpc\":\"2.0\",\"error\":{\"code\":" + code + ",\"message\":\"" + message + "\"}}"
        );
    }
}