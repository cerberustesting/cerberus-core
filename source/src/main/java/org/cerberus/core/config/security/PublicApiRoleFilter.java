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
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Security filter protecting the public REST API ({@code /api/public/**}) once a Bearer JWT
 * has been resolved by the resource-server filter on {@code publicApiSecurityFilterChain}.
 *
 * That chain itself is {@code permitAll()} so that requests with no {@code Authorization}
 * header can still fall through to the legacy {@code X-API-KEY} check performed inside each
 * public controller ({@link org.cerberus.core.api.services.PublicApiAuthenticationService}).
 * This filter only steps in when a JWT authentication was actually resolved :
 * <ul>
 *   <li>no {@link Authentication} in the {@link SecurityContextHolder} (no Bearer token
 *       supplied) : let the request through, the controller's {@code X-API-KEY} fallback
 *       applies exactly as before.</li>
 *   <li>an authenticated JWT carrying none of the standard Cerberus roles (see
 *       {@link #STANDARD_ROLES}) : reject with 403 immediately. {@code KeycloakRoleMapper}
 *       maps every realm role on the token to a {@code ROLE_*} authority, including Keycloak's
 *       own default roles (e.g. {@code offline_access}) that carry no Cerberus meaning, so this
 *       filter only accepts one of the roles Cerberus itself already recognises (the same ones
 *       used in {@link WebSecurityRules} for the legacy JSP/servlet pages) - no dedicated
 *       "public API" role is introduced.</li>
 *   <li>an authenticated JWT carrying at least one standard role : let the request through.</li>
 * </ul>
 *
 * @author bcivel
 */
@Component
public class PublicApiRoleFilter extends OncePerRequestFilter {

    private static final Logger LOG = LogManager.getLogger(PublicApiRoleFilter.class);

    /** Mirrors the role names used in {@link WebSecurityRules#applyRules} - keep both in sync. */
    private static final Set<String> STANDARD_ROLES = Set.of(
            "ROLE_TestRO", "ROLE_Test", "ROLE_TestStepLibrary", "ROLE_TestAdmin",
            "ROLE_Label", "ROLE_AS", "ROLE_RunTest", "ROLE_TestDataManager",
            "ROLE_IntegratorRO", "ROLE_Integrator", "ROLE_IntegratorNewChain", "ROLE_IntegratorDeploy",
            "ROLE_Administrator");

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {

        Authentication existing = SecurityContextHolder.getContext().getAuthentication();

        if (existing != null && existing.isAuthenticated() && !(existing instanceof AnonymousAuthenticationToken)) {
            boolean hasStandardRole = existing.getAuthorities().stream()
                    .anyMatch(a -> STANDARD_ROLES.contains(a.getAuthority()));

            if (!hasStandardRole) {
                LOG.warn("Public API access refused for '{}' (no standard Cerberus role) from {}",
                        existing.getName(), request.getRemoteAddr());
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "No standard Cerberus role on this token");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}