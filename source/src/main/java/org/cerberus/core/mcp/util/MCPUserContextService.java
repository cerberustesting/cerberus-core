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
package org.cerberus.core.mcp.util;

import io.modelcontextprotocol.server.McpSyncServerExchange;
import org.cerberus.core.config.security.McpApiKeyAuthFilter;
import org.cerberus.core.crud.entity.User;
import org.cerberus.core.crud.entity.UserSystem;
import org.cerberus.core.crud.service.IUserService;
import org.cerberus.core.crud.service.IUserSystemService;
import org.cerberus.core.exception.CerberusException;
import org.json.JSONArray;
import org.json.JSONException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Resolves the MCP caller's identity and system context for use by MCP tool handlers.
 *
 * <p>{@code User.defaultSystem} is the same column the Cerberus web UI uses to store the
 * user's current "system context" ({@link org.cerberus.core.servlet.crud.usermanagement.UpdateMyUserSystem}):
 * despite its singular name, it holds a JSON array of system names, e.g.
 * {@code ["SYS2","US-bcivel","DEFAULT"]}. This service reuses that same column so the
 * context an operator sets from the UI or from an MCP tool is the same context, and adds
 * the read side needed by MCP handlers, which have no {@code HttpSession} to read it from
 * (see {@link org.cerberus.core.util.security.UserSecurity}, which depends on one).</p>
 *
 * <p>The full set of systems a user is allowed to touch at all is the separate,
 * admin-managed {@link UserSystem} list ({@link IUserSystemService#findUserSystemByUser(String)}).
 * The "context" here is always a subset of that allow-list.</p>
 */
@Component
public class MCPUserContextService {

    private final IUserService userService;
    private final IUserSystemService userSystemService;

    public MCPUserContextService(IUserService userService, IUserSystemService userSystemService) {
        this.userService = userService;
        this.userSystemService = userSystemService;
    }

    /**
     * Resolves the login of the MCP caller from the transport context populated in
     * {@link org.cerberus.core.config.cerberus.WebAppInitializer}.
     *
     * @return the login, or {@code null} when the transport did not carry one (e.g. no
     * authenticated request attribute was set — should not happen once {@code /mcp} is behind
     * {@code McpApiKeyAuthFilter}, but tool handlers must not assume it).
     */
    public String getLogin(McpSyncServerExchange exchange) {
        Object login = exchange.transportContext().get(McpApiKeyAuthFilter.AUTHENTICATED_LOGIN_ATTR);
        return (login instanceof String loginString && !loginString.isBlank()) ? loginString : null;
    }

    public User getUser(String login) throws CerberusException {
        return userService.findUserByKey(login);
    }

    /**
     * Parses the user's current system context out of {@link User#getDefaultSystem()}.
     *
     * @return the active systems, in the order stored; empty when unset or unparsable.
     */
    public List<String> getContextSystems(User user) {
        List<String> systems = new ArrayList<>();
        String raw = user.getDefaultSystem();
        if (raw == null || raw.isBlank()) {
            return systems;
        }
        try {
            JSONArray array = new JSONArray(raw);
            for (int i = 0; i < array.length(); i++) {
                systems.add(array.getString(i));
            }
        } catch (JSONException e) {
            // Legacy / manually-edited value that isn't a JSON array: treat the raw string
            // itself as a single active system rather than silently losing it.
            systems.add(raw);
        }
        return systems;
    }

    /**
     * Returns the systems the user is allowed to use at all, per the {@link UserSystem} table
     * (managed by an administrator) — the ceiling that {@link #getContextSystems(User)} must
     * stay within.
     */
    public List<String> getAllowedSystems(String login) throws CerberusException {
        return userSystemService.findUserSystemByUser(login).stream()
                .map(UserSystem::getSystem)
                .toList();
    }

    /**
     * Persists the given systems as the user's new context, in the same JSON-array-in-a-string
     * format {@link org.cerberus.core.servlet.crud.usermanagement.UpdateMyUserSystem} writes.
     */
    public void saveContextSystems(User user, List<String> systems) throws CerberusException {
        user.setDefaultSystem(new JSONArray(systems).toString());
        userService.updateUser(user);
    }
}