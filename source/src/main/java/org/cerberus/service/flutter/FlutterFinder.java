/**
 * Cerberus Copyright (C) 2013 - 2017 cerberustesting
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
package org.cerberus.service.flutter;

import com.google.common.collect.ImmutableMap;
import org.openqa.selenium.remote.FileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;

/**
 *
 * @author vertigo
 */
public class FlutterFinder {

    RemoteWebDriver driver;
    FileDetector fileDetector;

    public FlutterFinder(RemoteWebDriver driver) {
        this.driver = driver;
        this.fileDetector = null;
    }

    public FlutterElement byValueKey(String key) {
        FlutterElement flutterElement = new FlutterElement(ImmutableMap
                .of("finderType", "ByValueKey",
                        "keyValueType", "String",
                        "keyValueString", key));
        flutterElement.setParent(driver);
        flutterElement.setFileDetector(fileDetector);
        return flutterElement;
    }
}
