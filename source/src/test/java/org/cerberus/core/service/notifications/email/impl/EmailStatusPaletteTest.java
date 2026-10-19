/*
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
package org.cerberus.core.service.notifications.email.impl;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class EmailStatusPaletteTest {

    @Test
    void everyStatusHasItsOwnColor() {
        Set<String> colors = new HashSet<>();
        for (String status : EmailStatusPalette.ORDER) {
            colors.add(EmailStatusPalette.color(status));
        }
        assertEquals(EmailStatusPalette.ORDER.size(), colors.size());
    }

    @Test
    void textIsReadableOnTheBackground() {
        assertEquals("#ffffff", EmailStatusPalette.textColor("KO"));
        assertEquals("#1e2124", EmailStatusPalette.textColor("NE"));
        assertEquals("#1e2124", EmailStatusPalette.textColor("unknown"));
    }

    @Test
    void unknownOrNullStatusFallsBack() {
        assertEquals(EmailStatusPalette.color("NE"), EmailStatusPalette.color(null));
        assertEquals("", EmailStatusPalette.label(null));
        assertEquals("Failed", EmailStatusPalette.label("FA"));
    }
}
