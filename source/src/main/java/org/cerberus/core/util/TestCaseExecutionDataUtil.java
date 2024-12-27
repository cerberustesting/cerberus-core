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
package org.cerberus.core.util;

import org.cerberus.core.crud.entity.TestCaseExecutionData;

/**
 * Set of utility methods related to the {@link TestCaseExecutionData} class.
 * 
 * @author abourdon
 */
public final class TestCaseExecutionDataUtil {

    /**
     * Resets timers from the given {@link TestCaseExecutionData}
     * 
     * @param data
     *            the {@link TestCaseExecutionData} to reset timers
     * @param time
     *            the new time the set to the given {@link TestCaseExecutionData}
     */
    public static void resetTimers(TestCaseExecutionData data, long time) {
        data.setStart(time);
        data.setStartLong(time);
        data.setEnd(time);
        data.setEndLong(time);
    }

    /**
     * Utility class then private constructor
     */
    private TestCaseExecutionDataUtil() {
    }

}
