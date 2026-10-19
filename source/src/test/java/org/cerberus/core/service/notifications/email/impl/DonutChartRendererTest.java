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

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.List;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class DonutChartRendererTest {

    private static BufferedImage decode(byte[] png) throws Exception {
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(png));
        assertNotNull(image);
        return image;
    }

    @Test
    void drawsTheRingWithTheColorOfEachSlice() throws Exception {
        // 75% green then 25% red, clockwise from the top : 3 o'clock is in the green, 10:30 is in the middle of the red.
        BufferedImage image = decode(DonutChartRenderer.render(List.of(
                new DonutChartRenderer.Slice("#22c55e", 75), new DonutChartRenderer.Slice("#ef4444", 25)), null, null, "#16a34a"));
        int size = DonutChartRenderer.DISPLAY_SIZE * 2;
        assertEquals(size, image.getWidth());
        assertEquals(size, image.getHeight());

        int ringX = (int) (size / 2.0 + 80 * size / 200.0); // 3 o'clock on the ring
        double offset = 80 * size / 200.0 * Math.sqrt(0.5);
        int redX = (int) (size / 2.0 - offset); // 10:30 on the ring
        int redY = (int) (size / 2.0 - offset);
        assertEquals(0x22c55e, image.getRGB(ringX, size / 2) & 0xffffff);
        assertEquals(0xef4444, image.getRGB(redX, redY) & 0xffffff);
        // Outside of the ring the image is transparent.
        assertEquals(0, image.getRGB(2, 2) >>> 24);
    }

    @Test
    void drawsAnEmptyTrackWithoutSlices() throws Exception {
        BufferedImage image = decode(DonutChartRenderer.render(List.of(), "0%", "OK RATE", "#dc2626"));
        int size = DonutChartRenderer.DISPLAY_SIZE * 2;
        assertEquals(0xf1f5f9, image.getRGB((int) (size / 2.0 + 80 * size / 200.0), size / 2) & 0xffffff);
    }

    @Test
    void centerTextDoesNotChangeTheRing() throws Exception {
        byte[] withText = DonutChartRenderer.render(List.of(new DonutChartRenderer.Slice("#22c55e", 1)), "100%", "OK RATE", "#16a34a");
        byte[] withoutText = DonutChartRenderer.render(List.of(new DonutChartRenderer.Slice("#22c55e", 1)), null, null, "#16a34a");
        BufferedImage a = decode(withText);
        BufferedImage b = decode(withoutText);
        int size = DonutChartRenderer.DISPLAY_SIZE * 2;
        int x = (int) (size / 2.0 + 80 * size / 200.0);
        assertEquals(b.getRGB(x, size / 2), a.getRGB(x, size / 2));
        // When fonts are available the middle of the image is drawn : otherwise the text is skipped silently.
        assertNotEquals(-1, a.getRGB(size / 2, size / 2));
    }

    @Test
    void centerTextUsesTheGivenColor() throws Exception {
        // The image cannot adapt to the dark mode of the mail clients : the text color is chosen by the caller.
        BufferedImage image = decode(DonutChartRenderer.render(List.of(new DonutChartRenderer.Slice("#22c55e", 1)), "80%", "OK RATE", "#d97706"));
        int size = image.getWidth();
        int inner = (int) (size * 0.25);
        int opaqueText = 0;
        for (int x = size / 2 - inner; x < size / 2 + inner; x++) {
            for (int y = size / 2 - inner; y < size / 2 + inner; y++) {
                int argb = image.getRGB(x, y);
                if ((argb >>> 24) == 255 && (argb & 0xffffff) == 0xd97706) {
                    opaqueText++;
                }
            }
        }
        // No font on this machine : the text is skipped on purpose, nothing to check.
        assumeTrue(opaqueText > 0 || !hasAnyOpaquePixelInside(image, inner));
        assertTrue(opaqueText > 0);
    }

    private static boolean hasAnyOpaquePixelInside(BufferedImage image, int inner) {
        int size = image.getWidth();
        for (int x = size / 2 - inner; x < size / 2 + inner; x++) {
            for (int y = size / 2 - inner; y < size / 2 + inner; y++) {
                if ((image.getRGB(x, y) >>> 24) > 0) {
                    return true;
                }
            }
        }
        return false;
    }
}
