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
package org.cerberus.core.service.ai.impl.parsing;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.entity.ApplicationObject;
import org.cerberus.core.crud.entity.Parameter;
import org.cerberus.core.crud.service.IParameterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ApplicationObjectFromAI {

    @Autowired
    IParameterService parameterService;

    private static final Logger LOG = LogManager.getLogger(ApplicationObjectFromAI.class);
    private static final Pattern AO_BLOCK = Pattern.compile("<AO>(.*?)</AO>", Pattern.DOTALL);


    // DTO to save coord before crop
    private static class AORaw {
        String name;
        String xpath;
        String screenshotName;
        double xPct;
        double yPct;
        double wPct;
        double hPct;
    }

    /**
     * Get all <AO></AO>, generate object, and crop Picture
     * @param text
     * @param applicationId
     * @param user
     * @param screenshotPath
     * @return
     */
    public List<ApplicationObject> parseAndCropAOBlocks(
            String text,
            String applicationId,
            String user,
            String screenshotPath) {

        List<ApplicationObject> result = new ArrayList<>();

        BufferedImage fullImage = loadAndConvertImage(screenshotPath);
        if (fullImage == null) {
            LOG.error("Cannot load screenshot, aborting AO generation: " + screenshotPath);
            return result;
        }

        Matcher matcher = AO_BLOCK.matcher(text);
        while (matcher.find()) {
            String block = matcher.group(1).trim();
            AORaw raw = parseRawAO(block);

            if (raw == null || raw.name == null || raw.name.isEmpty()) {
                LOG.warn("Skipping AO block — missing name field");
                continue;
            }

            String tempFilename = cropAndSaveTempScreenshot(raw, fullImage);

            ApplicationObject ao = new ApplicationObject();
            ao.setApplication(applicationId);
            ao.setUsrCreated(user);
            ao.setObject(raw.name);
            ao.setValue(raw.xpath);
            ao.setScreenshotFilename(tempFilename != null ? tempFilename : "");

            result.add(ao);
        }

        return result;
    }

    private BufferedImage loadAndConvertImage(String screenshotPath) {
        try {
            BufferedImage image = ImageIO.read(new File(screenshotPath));
            if (image == null) {
                LOG.warn("Could not read screenshot: " + screenshotPath);
                return null;
            }
            LOG.info("Image loaded: {}x{} type={}", image.getWidth(), image.getHeight(), image.getType());
            return image;

        } catch (IOException e) {
            LOG.error("Failed to read screenshot: " + screenshotPath, e);
            return null;
        }
    }

    private AORaw parseRawAO(String block) {
        AORaw raw = new AORaw();

        String[] lines = block.split("\\r?\\n");
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty() || !line.contains("=")) continue;

            String[] parts = line.split("=", 2);
            if (parts.length < 2) continue;

            String key   = parts[0].trim();
            String value = parts[1].trim();

            switch (key) {
                case "name": raw.name = value; break;
                case "xpath": raw.xpath = value; break;
                case "screenshotName": raw.screenshotName = value; break;
                case "xPct": raw.xPct = parseDoubleSafe(value); break;
                case "yPct": raw.yPct = parseDoubleSafe(value); break;
                case "wPct": raw.wPct = parseDoubleSafe(value); break;
                case "hPct": raw.hPct = parseDoubleSafe(value); break;
            }
        }

        LOG.info("Parsed AO — name:{} xPct:{} yPct:{} wPct:{} hPct:{}", raw.name, raw.xPct, raw.yPct, raw.wPct, raw.hPct);
        return raw;
    }

    private String cropAndSaveTempScreenshot(AORaw raw, BufferedImage fullImage) {
        try {
            File tempDir = new File(parameterService.getParameterStringByKey(Parameter.VALUE_cerberus_applicationobject_path, "", "")+"/temp");
            if (!tempDir.exists()) {
                if (!tempDir.mkdirs()) {
                    LOG.error("Failed to create temp directory: " + parameterService.getParameterStringByKey(Parameter.VALUE_cerberus_applicationobject_path, "", "")+"/temp");
                    return null;
                }
            }



            int x = (int) Math.round(raw.xPct * fullImage.getWidth()) - 150 ;
            int y = (int) Math.round(raw.yPct * fullImage.getHeight()) - 50;
            int width  = (int) Math.round(raw.wPct * fullImage.getWidth()) + 100;
            int height = (int) Math.round(raw.hPct * fullImage.getHeight()) + 100;
            LOG.info("Crop coords (stored space) — x:{} y:{} w:{} h:{}", x, y, width, height);

            // clamp pour éviter les exceptions
            x = Math.max(0, x);
            y = Math.max(0, y);
            width  = Math.min(width,  fullImage.getWidth()  - x);
            height = Math.min(height, fullImage.getHeight() - y);

            if (width <= 0 || height <= 0) {
                LOG.warn("Invalid crop dimensions for AO: {}", raw.name);
                return null;
            }

            // === DEBUG: Image complète avec rectangle rouge ===
            BufferedImage debug = new BufferedImage(fullImage.getWidth(), fullImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = debug.createGraphics();
            g.drawImage(fullImage, 0, 0, null);
            g.setColor(Color.RED);
            g.setStroke(new BasicStroke(3));
            g.drawRect(x, y, width, height);
            g.dispose();

            String tempFolderPath = parameterService.getParameterStringByKey(Parameter.VALUE_cerberus_applicationobject_path, "", "")+"/temp";
            File debugFile = new File(tempFolderPath, "debug_" + raw.name + "_" + UUID.randomUUID() + ".png");
            ImageIO.write(debug, "PNG", debugFile);
            LOG.info("Debug image saved: {}", debugFile.getAbsolutePath());

            // === Crop normal ===
            BufferedImage cropped = fullImage.getSubimage(x, y, width, height);
            BufferedImage copy = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = copy.createGraphics();
            g2d.drawImage(cropped, 0, 0, null);
            g2d.dispose();

            File output = new File(tempFolderPath, "temp_" + raw.name + "_" + UUID.randomUUID() + ".png");
            boolean written = ImageIO.write(copy, "PNG", output);


            if (!written) {
                LOG.error("ImageIO.write returned false for: " + output.getName());
                return null;
            }

            LOG.info("Temp screenshot saved: {}", output.getAbsolutePath());
            return output.getName();

        } catch (IOException e) {
            LOG.error("Failed to crop screenshot for AO: " + raw.name, e);
            return null;
        }
    }

    private double parseDoubleSafe(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            LOG.warn("Could not parse double value: {}", value);
            return 0.0;
        }
    }

}
