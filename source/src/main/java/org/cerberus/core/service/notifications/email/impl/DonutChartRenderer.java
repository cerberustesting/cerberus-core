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
package org.cerberus.core.service.notifications.email.impl;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.List;
import javax.imageio.ImageIO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Draws the donut of the "Report by status" as a PNG, for the emails (SVG is not supported by the mail clients).
 * Same look as the donut of the campaign report page : a ring split by status, with the OK rate in the middle.
 */
public final class DonutChartRenderer {

    private static final Logger LOG = LogManager.getLogger(DonutChartRenderer.class);

    /**
     * Displayed size in pixels. The image is drawn at twice this size for the high density screens.
     */
    public static final int DISPLAY_SIZE = 190;
    private static final int SCALE = 2;

    /**
     * A portion of the ring.
     *
     * @param color hex color (#rrggbb)
     * @param count number of executions
     */
    public record Slice(String color, int count) {
    }

    private DonutChartRenderer() {
    }

    /**
     * @param slices the portions of the ring, in the order to draw (clockwise from the top)
     * @param centerText big text in the middle (ex : "80%"), null for none
     * @param centerSubText small text under it (ex : "OK RATE"), null for none
     * @param textColor hex color of both texts. The image is transparent and cannot adapt to the dark mode of the
     * mail clients : use a medium tone, readable on a white and on a dark background (not black, not light grey)
     * @return the PNG bytes
     * @throws Exception when the image cannot be produced
     */
    public static byte[] render(List<Slice> slices, String centerText, String centerSubText, String textColor) throws Exception {
        System.setProperty("java.awt.headless", "true");

        int size = DISPLAY_SIZE * SCALE;
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

            // Same geometry as the svg of the page : viewBox 200, radius 80, stroke 26.
            double unit = size / 200.0;
            double radius = 80 * unit;
            double center = size / 2.0;
            g.setStroke(new BasicStroke((float) (26 * unit), BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));

            // Track.
            g.setColor(Color.decode("#f1f5f9"));
            g.draw(new Arc2D.Double(center - radius, center - radius, 2 * radius, 2 * radius, 0, 360, Arc2D.OPEN));

            // Segments, clockwise from the top (Java2D angles are counter clockwise from 3 o'clock).
            int total = slices.stream().mapToInt(Slice::count).sum();
            if (total > 0) {
                double start = 90;
                for (Slice slice : slices) {
                    if (slice.count() <= 0) {
                        continue;
                    }
                    double extent = -360.0 * slice.count() / total;
                    g.setColor(Color.decode(slice.color()));
                    g.draw(new Arc2D.Double(center - radius, center - radius, 2 * radius, 2 * radius, start, extent, Arc2D.OPEN));
                    start += extent;
                }
            }

            drawCenterText(g, center, unit, centerText, centerSubText, textColor);
        } finally {
            g.dispose();
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        if (!ImageIO.write(image, "png", out)) {
            throw new IllegalStateException("No PNG writer available.");
        }
        return out.toByteArray();
    }

    /**
     * The text needs a font : on a minimal server (no fontconfig) this can fail. The ring is then kept
     * without text, the OK rate is also displayed by the tiles of the mail.
     */
    private static void drawCenterText(Graphics2D g, double center, double unit, String text, String subText, String textColor) {
        if (text == null) {
            return;
        }
        try {
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setColor(Color.decode(textColor));
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, (int) (36 * unit)));
            FontMetrics fm = g.getFontMetrics();
            double textY = center + fm.getAscent() / 2.0 - fm.getDescent() / 2.0 - (subText == null ? 0 : 6 * unit);
            g.drawString(text, (float) (center - fm.stringWidth(text) / 2.0), (float) textY);

            if (subText != null) {
                g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, (int) (10 * unit)));
                FontMetrics sfm = g.getFontMetrics();
                g.drawString(subText, (float) (center - sfm.stringWidth(subText) / 2.0), (float) (textY + 20 * unit));
            }
        } catch (Exception | LinkageError e) {
            LOG.warn("Donut center text skipped (font not available) : " + e);
        }
    }
}
