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
package org.cerberus.core.engine.factory.impl;

import org.cerberus.core.engine.entity.Selenium;
import org.cerberus.core.engine.factory.IFactorySelenium;
import org.openqa.selenium.WebDriver;
import org.springframework.stereotype.Service;

/**
 * @author bcivel
 */
@Service
public class FactorySelenium implements IFactorySelenium {

    @Override
    public Selenium create(String host, String port, String browser, String version, String platform, String login, String ip, WebDriver driver, long wait) {
        Selenium newSelenium = new Selenium();
        newSelenium.setHost(host == null ? "localhost" : host);
        newSelenium.setPort(port);
        newSelenium.setBrowser(browser);
        newSelenium.setVersion(version);
        newSelenium.setPlatform(platform);
        newSelenium.setLogin(login.startsWith("/") ? login.substring(1) : login);
        newSelenium.setIp(ip);
        newSelenium.setDefaultWait(wait);

        return newSelenium;
    }
}
