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
package org.cerberus.core.mcpdelta.util;

import java.util.Set;

/**
 * Who a tool call is made for, for the call's thread: the MCP session (the Mcp-Session-Id the client sends back),
 * the authenticated Cerberus login and its roles. Roles are null when the transport does not enforce them (the
 * standalone local server); inside the Cerberus webapp they are the caller's Cerberus roles.
 */
public final class CallContext {

    private static final ThreadLocal<String> SESSION = new ThreadLocal<>();
    private static final ThreadLocal<String> USER = new ThreadLocal<>();
    private static final ThreadLocal<Set<String>> ROLES = new ThreadLocal<>();

    private CallContext() {
    }

    public static void set(String session) {
        SESSION.set(session == null || session.isBlank() ? null : session.trim());
    }

    /** The authenticated caller: its login, and its roles (null = not enforced). */
    public static void caller(String login, Set<String> roles) {
        USER.set(login == null || login.isBlank() ? null : login.trim());
        ROLES.set(roles);
    }

    public static String session() {
        return SESSION.get();
    }

    public static String user() {
        return USER.get();
    }

    public static Set<String> roles() {
        return ROLES.get();
    }

    public static void clear() {
        SESSION.remove();
        USER.remove();
        ROLES.remove();
    }
}
