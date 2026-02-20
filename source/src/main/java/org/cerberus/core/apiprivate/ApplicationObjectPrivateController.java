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
package org.cerberus.core.apiprivate;

import io.swagger.v3.oas.annotations.Operation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.entity.Parameter;
import org.cerberus.core.crud.service.IParameterService;
import org.cerberus.core.service.ai.impl.AISessionManager;
import org.cerberus.core.util.servlet.ServletUtil;
import org.json.JSONObject;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.servlet.http.HttpServletRequest;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author bcivel
 */
@RestController
@RequestMapping("/applicationobjects")
public class ApplicationObjectPrivateController {

    private static final Logger LOG = LogManager.getLogger(ApplicationObjectPrivateController.class);
    private final PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);

    @Autowired
    IParameterService parameterService;

    @PostMapping(value = "/uploadFiles", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadFiles(
            @RequestParam("screenshot") MultipartFile screenshot,
            @RequestParam(value = "htmlFile", required = false) MultipartFile htmlFile) {

        try {
            // Read Picture from MultipartFile
            BufferedImage rawImage = ImageIO.read(screenshot.getInputStream());
            if (rawImage == null) {
                LOG.error("Impossible de lire l'image uploadée.");
                return ResponseEntity.badRequest().build();
            }

            // Convert in TYPE_INT_RGB for JPEG
            BufferedImage converted = new BufferedImage(
                    rawImage.getWidth(),
                    rawImage.getHeight(),
                    BufferedImage.TYPE_INT_RGB
            );
            Graphics2D g2d = converted.createGraphics();
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, converted.getWidth(), converted.getHeight());
            g2d.drawImage(rawImage, 0, 0, null);
            g2d.dispose();

            // Resize if too large
            int maxWidth = 1280;
            if (converted.getWidth() > maxWidth) {
                double scale = (double) maxWidth / converted.getWidth();
                int newWidth = maxWidth;
                int newHeight = (int) (converted.getHeight() * scale);

                BufferedImage resized = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
                Graphics2D g = resized.createGraphics();
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g.drawImage(converted, 0, 0, newWidth, newHeight, null);
                g.dispose();

                converted = resized;
                LOG.info("Image resized to {}x{}", newWidth, newHeight);
            }

            // Save in JPEG
            String screenshotName = UUID.randomUUID() + ".jpg";
            String tempFolderPath = parameterService.getParameterStringByKey(Parameter.VALUE_cerberus_applicationobject_path, "", "")+"/temp";
            File screenshotFile = new File(tempFolderPath, screenshotName);
            screenshotFile.getParentFile().mkdirs();

            ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(0.85f);

            try (ImageOutputStream ios = ImageIO.createImageOutputStream(screenshotFile)) {
                writer.setOutput(ios);
                writer.write(null, new IIOImage(converted, null, null), param);
                writer.dispose();
            }

            LOG.info("Screenshot saved: {} ({} KB)", screenshotName, screenshotFile.length() / 1024);

            // HTML
            String htmlName = null;
            String htmlPath = null;
            if (htmlFile != null && !htmlFile.isEmpty()) {
                htmlName = UUID.randomUUID() + ".html";
                File html = new File(tempFolderPath, htmlName);
                htmlFile.transferTo(html);
                htmlPath = html.getAbsolutePath();
            }

            // Response in JSON
            Map<String, String> response = new HashMap<>();
            response.put("screenshotName", screenshotName);
            response.put("screenshotPath", screenshotFile.getAbsolutePath());
            response.put("screenshotStoredWidth", String.valueOf(converted.getWidth()));
            response.put("screenshotStoredHeight", String.valueOf(converted.getHeight()));
            response.put("htmlName", htmlName);
            response.put("htmlPath", htmlPath);

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            LOG.error("Upload failed", e);
            return ResponseEntity.internalServerError().build();
        }
    }


    @GetMapping("/screenshot/temp/{filename:.+}")
    public ResponseEntity<Resource> getTempScreenshot(@PathVariable String filename) {
        try {
            if (filename.contains("..") || filename.contains("/")) {
                return ResponseEntity.badRequest().build();
            }

            String tempFolderPath = parameterService.getParameterStringByKey(Parameter.VALUE_cerberus_applicationobject_path, "", "")+"/temp";
            File file = new File(tempFolderPath, filename);
            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }

            //MEDIATYPE depends on extension
            String ext = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
            MediaType mediaType;
            if ("png".equals(ext)) {
                mediaType = MediaType.IMAGE_PNG;
            } else if ("jpg".equals(ext) || "jpeg".equals(ext)) {
                mediaType = MediaType.IMAGE_JPEG;
            } else {
                mediaType = MediaType.APPLICATION_OCTET_STREAM;
            }

            Resource resource = new FileSystemResource(file);
            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

}
