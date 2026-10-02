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
package org.cerberus.core.mcpdelta.run;

import org.cerberus.core.mcpdelta.Context;

/** Cache purge and queue job trigger, through whichever gateway reaches Cerberus. */
public final class CerberusHttp {

    private CerberusHttp() {
    }

    /** Asks Cerberus to drop its caches (invariants, parameters), so a change is seen at once. */
    public static String purgeCache(Context ctx) {
        return ctx.gateway.purgeCache();
    }

    /** Wakes the queue job up, as Cerberus does after resuming a paused campaign. */
    public static String runQueueJob(Context ctx) {
        return ctx.gateway.runQueueJob();
    }
}
