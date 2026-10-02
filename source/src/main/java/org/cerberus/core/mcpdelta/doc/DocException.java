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

import java.util.List;
import java.util.stream.Collectors;

/** Raised when a document or a change cannot be applied; carries every diagnostic found, not just the first. */
public final class DocException extends RuntimeException {

    private final transient List<Diagnostic> diagnostics;

    public DocException(List<Diagnostic> diagnostics) {
        super(diagnostics.stream().map(Diagnostic::format).collect(Collectors.joining("\n")));
        this.diagnostics = List.copyOf(diagnostics);
    }

    public DocException(Diagnostic diagnostic) {
        this(List.of(diagnostic));
    }

    public List<Diagnostic> diagnostics() {
        return diagnostics;
    }
}
