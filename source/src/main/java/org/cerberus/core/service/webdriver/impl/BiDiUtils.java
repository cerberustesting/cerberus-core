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
package org.cerberus.core.service.webdriver.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.bidi.BiDi;
import org.openqa.selenium.bidi.Command;
import org.openqa.selenium.bidi.HasBiDi;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BiDiUtils {

    private static final Logger LOG = LogManager.getLogger("BiDiUtils");

    public static BiDi enableBiDi(WebDriver driver){

        WebDriver augmentedDriver = new Augmenter().augment(driver);

        if (!(augmentedDriver instanceof HasBiDi)) {
            LOG.debug("Driver does not support BiDi");
            return null;
        }

        BiDi bidiSession = ((HasBiDi) augmentedDriver).getBiDi();

        LOG.debug("BiDi OK: " + (bidiSession != null));

        Object wsUrl = ((RemoteWebDriver) driver).getCapabilities().getCapability("webSocketUrl");
        LOG.debug("webSocketUrl = " + wsUrl);

        return bidiSession;
    }

    public static String addPreloadScript(BiDi biDiSession, String jsCode) {
        return addPreloadScript(biDiSession, jsCode, null, null);
    }

    public static String addPreloadScript(BiDi biDiSession, String jsCode, List<Object> args, String sandbox) {

        Map<String, Object> params = new HashMap<>();
        params.put("functionDeclaration", jsCode);
        if (args != null) params.put("arguments", args);
        if (sandbox != null) params.put("sandbox", sandbox);

        Map<String, Object> result = biDiSession.send(new Command<>("script.addPreloadScript", params));

        return (String) result.get("scriptId");
    }



    public static void removePreloadScript(WebDriver driver, String scriptId) {
        BiDi bidi = ((HasBiDi) driver).getBiDi();
        Map<String, Object> params = Map.of("scriptId", scriptId);
        bidi.send(new Command<>("script.removePreloadScript", params));
    }

}
