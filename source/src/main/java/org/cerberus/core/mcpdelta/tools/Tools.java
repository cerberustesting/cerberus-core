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

import org.cerberus.core.mcpdelta.Context;
import org.cerberus.core.mcpdelta.read.ExecutionReader;
import org.cerberus.core.mcpdelta.read.FindService;
import org.cerberus.core.mcpdelta.read.ReadService;
import org.cerberus.core.mcpdelta.run.RunService;
import org.cerberus.core.mcpdelta.write.UndoService;
import org.cerberus.core.mcpdelta.write.WriteService;

import java.util.List;

/** The whole surface: five tools. */
public final class Tools {

    private Tools() {
    }

    public static List<Tool> all(Context ctx) {
        ExecutionReader executions = new ExecutionReader(ctx);
        RunService runs = new RunService(ctx, executions);
        return List.of(
                new ReadTool(new ReadService(ctx, executions), ctx.config.readBudget),
                new FindTool(new FindService(ctx)),
                new WriteTool(new WriteService(ctx), runs),
                new RunTool(runs),
                new UndoTool(new UndoService(ctx)));
    }
}
