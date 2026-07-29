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
package org.cerberus.core.engine.execution.debug;

/**
 * A command sent to wake up a worker thread blocked in {@link DebugSession#awaitCommand()}.
 *
 * @author bcivel
 */
public enum DebugCommand {
    /** Proceed : run the pending action/control, or (if it already failed once) accept that
     *  failure and move on to the next one. */
    NEXT,
    /** Only meaningful when the pending action/control already failed once : re-run it (the
     *  engine reloads its definition fresh from DB first, so an edit made elsewhere is picked
     *  up) instead of moving on. */
    RETRY
}