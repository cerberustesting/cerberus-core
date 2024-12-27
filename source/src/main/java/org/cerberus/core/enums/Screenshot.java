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
package org.cerberus.core.enums;

public enum Screenshot {

    NO_SCREENSHOTS(0),
    AUTOMATIC_SCREENSHOTS_ON_ERROR(1),
    SYSTEMATIC_SCREENSHOTS(2);

    private int value;

    Screenshot(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static boolean printScreenOnError(int value) {
        return value == AUTOMATIC_SCREENSHOTS_ON_ERROR.getValue();
    }

    public static boolean printScreenSystematicaly(int value) {
        return value == SYSTEMATIC_SCREENSHOTS.getValue();
    }
}
