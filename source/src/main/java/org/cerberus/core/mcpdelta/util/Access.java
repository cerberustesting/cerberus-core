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

import org.cerberus.core.mcpdelta.tools.Tool;

import java.util.Set;

/**
 * The Cerberus roles an operation needs, the same as the Cerberus web application asks for the equivalent
 * screen or servlet (WebSecurityRules). Enforced only when the transport provides the caller's roles.
 */
public final class Access {

    public static final String READ = "TestRO";
    public static final String TESTCASE = "Test";
    public static final String TESTCASE_DELETE = "TestAdmin";
    public static final String FOLDER = "TestAdmin";
    public static final String LABEL = "Label";
    public static final String DATALIB = "TestDataManager";
    public static final String INTEGRATION = "Integrator";
    public static final String RUN = "RunTest";
    public static final String ADMIN = "Administrator";

    private Access() {
    }

    /** Refuses the call when the caller's roles are known and do not include the role. */
    public static void require(String role, String what) {
        Set<String> roles = CallContext.roles();
        if (roles == null || roles.contains(role)) {
            return;
        }
        String who = CallContext.user() == null ? "this account" : "the Cerberus account " + CallContext.user();
        throw new Tool.ToolError(who + " lacks the role " + role + ", needed to " + what
                + " (an administrator grants it in User Manager). Nothing was done.");
    }

    /** The role writing (or deleting) an object of this kind needs. */
    public static String forKind(String kind, boolean delete) {
        return switch (kind) {
            case "testcase" -> delete ? TESTCASE_DELETE : TESTCASE;
            case "folder" -> FOLDER;
            case "labels", "label" -> LABEL;
            case "datalib" -> DATALIB;
            case "application", "service", "robot", "environment" -> INTEGRATION;
            case "campaign" -> RUN;
            case "invariant" -> ADMIN;
            default -> READ; // context: one's own systems, checked by the context rules themselves
        };
    }
}
