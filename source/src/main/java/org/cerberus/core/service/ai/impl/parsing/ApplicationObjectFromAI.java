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


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
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
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.UUID;

@Service
public class ApplicationObjectFromAI {

    @Autowired
    IParameterService parameterService;

    private static final Logger LOG = LogManager.getLogger(ApplicationObjectFromAI.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();


    // DTO to save coord before crop
    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class AORaw {
        String name;
        String xpath;
        String screenshotName;

        @JsonProperty("xPct")
        double xPct;

        @JsonProperty("yPct")
        double yPct;

        @JsonProperty("wPct")
        double wPct;

        @JsonProperty("hPct")
        double hPct;

        @JsonProperty("confidence")
        double confidence;

        String notes;
        String type;
        String page;
    }


    public ApplicationObject parseAndCropAOJson(String applicationId,String fullImageBase64,String jsonObject,String user) {

        //Parse returned JSON and convert into AORaw
        AORaw raw = parseRawAOJson(jsonObject);
        if (raw == null || raw.name == null || raw.name.isEmpty()) {
            LOG.warn("Skipping AO — missing name field");
            return null;
        }

        String tempFilename = cropAndSaveTempScreenshot(raw, fullImageBase64);

        ApplicationObject ao = new ApplicationObject();
        ao.setApplication(applicationId);
        ao.setUsrCreated(user);
        ao.setObject(raw.name);
        ao.setValue(raw.xpath);
        ao.setScreenshotFilename(tempFilename != null ? tempFilename : "");

        return ao;
    }

    //TODO : Remove if no more necessary
    public BufferedImage loadAndConvertImage(String screenshotPath) {
        try {
            BufferedImage image = ImageIO.read(new File(screenshotPath));
            if (image == null) {
                LOG.warn("Could not read screenshot: " + screenshotPath);
                return null;
            }
            LOG.debug("Image loaded: {}x{} type={}", image.getWidth(), image.getHeight(), image.getType());
            return image;

        } catch (IOException e) {
            LOG.error("Failed to read screenshot: " + screenshotPath, e);
            return null;
        }
    }


    private AORaw parseRawAOJson(String json) {
        try {
            return MAPPER.readValue(json, AORaw.class);
        } catch (Exception e) {
            LOG.error("Failed to parse AO JSON", e);
            return null;
        }
    }

    /*
    CROP Image, save sample and return path on disk
     */
    private String cropAndSaveTempScreenshot(AORaw raw, String base64Image) {
        try {
            byte[] imageBytes = Base64.getDecoder().decode(base64Image);
            BufferedImage fullImage = ImageIO.read(new ByteArrayInputStream(imageBytes));

            if (fullImage == null) {
                LOG.error("Failed to decode base64 image for AO: {}", raw.name);
                return null;
            }

            File tempDir = new File(parameterService.getParameterStringByKey(Parameter.VALUE_cerberus_applicationobject_path, "", "")+"/temp");
            if (!tempDir.exists()) {
                if (!tempDir.mkdirs()) {
                    LOG.error("Failed to create temp directory: " + parameterService.getParameterStringByKey(Parameter.VALUE_cerberus_applicationobject_path, "", "")+"/temp");
                    return null;
                }
            }



            LOG.debug("Full Picture — x:{} y:{} w:{} h:{}", fullImage.getWidth(), fullImage.getHeight(), fullImage.getWidth(), fullImage.getHeight());
            LOG.debug("Received coords — x:{} y:{} w:{} h:{}", raw.xPct, raw.yPct, raw.wPct, raw.hPct);
            LOG.debug("Received coords — x:{} y:{} w:{} h:{}", raw.getXPct(), raw.getYPct(), raw.getWPct(), raw.getHPct());
            int x = (int) Math.round(raw.xPct * fullImage.getWidth());
            int y = (int) Math.round(raw.yPct * fullImage.getHeight());
            int width  = (int) Math.round(raw.wPct * fullImage.getWidth());
            int height = (int) Math.round(raw.hPct * fullImage.getHeight());
            LOG.debug("Crop coords (stored space) — x:{} y:{} w:{} h:{}", x, y, width, height);

            // clamp pour éviter les exceptions
            int paddingX = 20;
            int paddingTop = 50;
            int paddingBottom = 50;

            x = Math.max(0, x - paddingX);
            y = Math.max(0, y - paddingTop);
            width  = Math.min(width  + paddingX * 2,     fullImage.getWidth()  - x);
            height = Math.min(height + paddingTop + paddingBottom, fullImage.getHeight() - y);

            if (width <= 0 || height <= 0) {
                LOG.warn("Invalid crop dimensions for AO: {}", raw.name);
                return null;
            }

            String tempFolderPath = parameterService.getParameterStringByKey(Parameter.VALUE_cerberus_applicationobject_path, "", "") + "/temp";
            // === DEBUG: Image complète avec rectangle rouge ===
            if (LOG.isDebugEnabled()) {
                BufferedImage debug = new BufferedImage(fullImage.getWidth(), fullImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = debug.createGraphics();
                g.drawImage(fullImage, 0, 0, null);
                g.setColor(Color.RED);
                g.setStroke(new BasicStroke(3));
                g.drawRect(x, y, width, height);
                g.dispose();

                File debugFile = new File(tempFolderPath, "debug_" + raw.name + "_" + UUID.randomUUID() + ".png");
                ImageIO.write(debug, "PNG", debugFile);
                LOG.info("Debug image saved: {}", debugFile.getAbsolutePath());
            }

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

}
