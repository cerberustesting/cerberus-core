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

public enum Video {

    NO_VIDEO (0),
    AUTOMATIC_VIDEO_ON_ERROR (1),
    SYSTEMATIC_VIDEO (2);

    private int value;
    Video(int value) {
        this.value=value;
    }

    public int getValue() {
        return value;
    }


    public static boolean recordVideo(int value) {
        return value == AUTOMATIC_VIDEO_ON_ERROR.getValue() || value == SYSTEMATIC_VIDEO.getValue();
    }
}
