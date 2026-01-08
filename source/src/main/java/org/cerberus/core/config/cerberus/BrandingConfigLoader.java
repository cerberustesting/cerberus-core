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
package org.cerberus.core.config.cerberus;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@WebListener
public class BrandingConfigLoader implements ServletContextListener {

    private static final String CONTEXT_ATTR = "brandingConfig";
    private static final String CONTEXT_FAVICON = "brandingFavicon";
    private static final String DEFAULT_FAVICON_TYPE = "brandingFaviconType";
    private static final String DEFAULT_FAVICON = "/images/favicon.ico.png";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext ctx = sce.getServletContext();

        try {
            String brandingJson = loadBranding(ctx);
            ctx.setAttribute(CONTEXT_ATTR, brandingJson);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(brandingJson);

            String favicon = root.path("favicon").asText(DEFAULT_FAVICON);
            String faviconType = "";

            if (favicon != null) {
                if (favicon.endsWith(".svg")) {
                    faviconType = "image/svg+xml";
                } else if (favicon.endsWith(".png")) {
                    faviconType = "image/png";
                } else if (favicon.endsWith(".ico")) {
                    faviconType = "image/x-icon";
                }
            }

            ctx.setAttribute(CONTEXT_FAVICON, favicon);
            ctx.setAttribute(DEFAULT_FAVICON_TYPE, faviconType);
            ctx.log("[Cerberus] Branding loaded successfully");

        } catch (Exception e) {
            ctx.log("[Cerberus] FATAL: cannot load branding", e);
            throw new RuntimeException(e); // stop startup
        }
    }

    private String loadBranding(ServletContext ctx) throws IOException {

        // 1️⃣ External override
        Path external = Paths.get(
                System.getProperty("catalina.base"),
                "conf", "cerberus", "branding.json"
        );

        if (Files.exists(external)) {
            ctx.log("[Cerberus] Using external branding.json");
            return Files.readString(external, StandardCharsets.UTF_8);
        }

        // 2️⃣ Fallback in WAR
        ctx.log("[Cerberus] Using embedded branding.json");

        try (InputStream is = ctx.getResourceAsStream("/WEB-INF/branding.json")) {
            if (is == null) {
                throw new FileNotFoundException("/WEB-INF/branding.json not found");
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}