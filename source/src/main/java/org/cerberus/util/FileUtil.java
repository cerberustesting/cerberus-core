/*
 * Cerberus  Copyright (C) 2013  vertigo17
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
package org.cerberus.util;

/**
 *
 * @author FNogueira
 */
public class FileUtil {
    private FileUtil() {
    }

    /**
     * Generate ScreenshotFileName using 2 method : If pictureName is not null,
     * use it directly. If picture name is null, generate name using test,
     * testcase, step sequence and control.
     *
     * @param test
     * @param testCase
     * @param step
     * @param sequence
     * @param control
     * @param pictureName
     * @param extension
     * @return
     */
    public static String generateScreenshotFilename(String test, String testCase, String step, String sequence, String control, String pictureName, String extension) {

        StringBuilder sbScreenshotFilename = new StringBuilder();
        if (pictureName == null) {
            sbScreenshotFilename.append(test);
            sbScreenshotFilename.append("-");
            sbScreenshotFilename.append(testCase);
            sbScreenshotFilename.append("-St");
            sbScreenshotFilename.append(step);
            sbScreenshotFilename.append("Sq");
            sbScreenshotFilename.append(sequence);
            if (control != null) {
                sbScreenshotFilename.append("Ct");
                sbScreenshotFilename.append(control);
            }
        } else {
            sbScreenshotFilename.append(pictureName);
        }
        sbScreenshotFilename.append(".");
        sbScreenshotFilename.append(extension);

        return sbScreenshotFilename.toString().replaceAll(" ", "");
    }
}
