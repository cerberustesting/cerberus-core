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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * Extracts Cerberus authorities ({@code ROLE_*}) from a set of Keycloak claims.
 *
 * The claim structure is identical between the OIDC login flow
 * ({@code OidcUserAuthority.getIdToken().getClaims()}) and the OAuth2
 * resource-server flow ({@code Jwt.getClaims()}), so both reuse this mapper :
 * <ul>
 *   <li>realm roles : {@code realm_access.roles}</li>
 *   <li>client roles : {@code resource_access.<clientId>.roles}</li>
 * </ul>
 *
 * @author bcivel
 */
public final class KeycloakRoleMapper {

    private KeycloakRoleMapper() {
        // utility class
    }

    public static Collection<SimpleGrantedAuthority> extractRoles(Map<String, Object> claims, String clientId) {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        if (claims == null) {
            return authorities;
        }

        // --- Realm roles ---
        if (claims.get("realm_access") instanceof Map<?, ?> realmAccess
                && realmAccess.get("roles") instanceof Collection<?> roles) {
            addRoles(authorities, roles);
        }

        // --- Client roles ---
        if (clientId != null
                && claims.get("resource_access") instanceof Map<?, ?> resourceAccess
                && resourceAccess.get(clientId) instanceof Map<?, ?> client
                && client.get("roles") instanceof Collection<?> roles) {
            addRoles(authorities, roles);
        }

        return authorities;
    }

    private static void addRoles(List<SimpleGrantedAuthority> authorities, Collection<?> roles) {
        for (Object role : roles) {
            if (role instanceof String name) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + name));
            }
        }
    }
}