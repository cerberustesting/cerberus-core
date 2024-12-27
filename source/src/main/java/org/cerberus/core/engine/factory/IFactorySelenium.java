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
package org.cerberus.core.engine.factory;

import org.cerberus.core.engine.entity.Selenium;
import org.openqa.selenium.WebDriver;

/**
 *
 * @author bcivel
 */
public interface IFactorySelenium {

    /**
     * 
     * @param host : IP of the Test Machine
     * @param port : Port ued for connection to the test Machine
     * @param browser : Browser Name used for the test
     * @param version : Version of the browser
     * @param platform : Platform of the robot (MAC/LINUX/WINDOWS...)
     * @param login
     * @param ip
     * @param driver
     * @param wait
     * @return 
     */
    Selenium create(String host, String port, String browser, String version, String platform, String login, String ip, WebDriver driver, long wait);
}
