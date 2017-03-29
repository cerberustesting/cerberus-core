/**
 * Cerberus Copyright (C) 2013 - 2017 cerberustesting
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
package org.cerberus.log;

import org.apache.log4j.Level;

/**
 * Class used to automatically generate log4j of a log message
 *
 * @author Tiago Bernardes
 * @version 1.0, 22/02/2013
 * @since 2.0.0
 */
public final class MyLogger {

    /**
     *
     * @param className of the class generate log
     * @param level of the log
     * @param message need to be logged
     */
    public static final void log(String className, Level level, String message) {
        org.apache.log4j.Logger.getLogger(className).log(level, message);
    }
}
