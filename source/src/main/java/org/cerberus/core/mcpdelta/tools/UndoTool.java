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
package org.cerberus.core.mcpdelta.tools;

import com.fasterxml.jackson.databind.JsonNode;
import org.cerberus.core.mcpdelta.write.UndoService;

import java.util.List;
import java.util.Map;

public final class UndoTool implements Tool {

    private final UndoService service;

    public UndoTool(UndoService service) {
        this.service = service;
    }

    @Override
    public String name() {
        return "undo";
    }

    @Override
    public String description() {
        return "Revert an applied delta (\"d12\"), restoring the testcases exactly as they were. Refused if they changed since.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return Map.of("type", "object", "properties", Map.of("delta", Map.of("type", "string")), "required", List.of("delta"));
    }

    @Override
    public String call(JsonNode args) {
        return service.undo(Args.str(args, "delta", Args.str(args, "id", "")));
    }
}
