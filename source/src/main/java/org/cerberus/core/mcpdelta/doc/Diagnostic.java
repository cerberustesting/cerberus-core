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
package org.cerberus.core.mcpdelta.doc;

/**
 * A compiler-style message: where, what, and how to fix it. Errors stop the write before anything
 * reaches the database; warnings are reported with a successful write.
 */
public record Diagnostic(String where, int line, boolean error, String message, String fix) {

    public static Diagnostic error(String where, int line, String message, String fix) {
        return new Diagnostic(where, line, true, message, fix);
    }

    public static Diagnostic warning(String where, int line, String message, String fix) {
        return new Diagnostic(where, line, false, message, fix);
    }

    public String format() {
        StringBuilder sb = new StringBuilder(error ? "error " : "warning ");
        if (where != null && !where.isEmpty()) {
            sb.append(where);
            if (line > 0) {
                sb.append(':').append(line);
            }
            sb.append(' ');
        } else if (line > 0) {
            sb.append("line ").append(line).append(' ');
        }
        sb.append("— ").append(message);
        if (fix != null && !fix.isEmpty()) {
            sb.append(" → ").append(fix);
        }
        return sb.toString();
    }
}
