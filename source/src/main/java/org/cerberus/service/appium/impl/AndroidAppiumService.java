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
package org.cerberus.service.appium.impl;

import io.appium.java_client.android.AndroidKeyCode;
import io.appium.java_client.android.AndroidDriver;
import org.apache.log4j.Logger;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.engine.entity.Session;
import org.cerberus.enums.MessageEventEnum;
import org.openqa.selenium.WebDriverException;
import org.springframework.stereotype.Service;

import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Specific Android implementation of the {@link AppiumService}
 * <p>
 * e@author Aurelien Bourdon.
 */
@Service("AndroidAppiumService")
public class AndroidAppiumService extends AppiumService {

    /**
     * The associated {@link Logger} to this class
     */
    private static final Logger LOGGER = Logger.getLogger(AndroidAppiumService.class);

    /**
     * The {@link Pattern} related error when keyboard is absent
     */
    private static final Pattern IS_KEYBOARD_ABSENT_ERROR_PATTERN = Pattern.compile("Original error: Soft keyboard not present");

    @Override
    public MessageEvent keyPress(Session session, String keyName) {
        // First, check if key name exists
        KeyCode keyToPress;
        try {
            keyToPress = KeyCode.valueOf(keyName);
        } catch (IllegalArgumentException e) {
            return new MessageEvent(MessageEventEnum.ACTION_FAILED_KEYPRESS_NOT_AVAILABLE).resolveDescription("KEY", keyName);
        }

        // Then press the key
        try {
            ((AndroidDriver) session.getAppiumDriver()).pressKeyCode(keyToPress.getCode());
            return new MessageEvent(MessageEventEnum.ACTION_SUCCESS_KEYPRESS_NO_ELEMENT).resolveDescription("KEY", keyName);
        } catch (Exception e) {
            LOGGER.warn("Unable to key press due to " + e.getMessage(), e);
            return new MessageEvent(MessageEventEnum.ACTION_FAILED_KEYPRESS_OTHER)
                    .resolveDescription("KEY", keyName)
                    .resolveDescription("REASON", e.getMessage());
        }
    }

    @Override
    public MessageEvent hideKeyboard(Session session) {
        try {
            session.getAppiumDriver().hideKeyboard();
            return new MessageEvent(MessageEventEnum.ACTION_SUCCESS_HIDEKEYBOARD);
        } catch (Exception e) {
            // Instead of http://stackoverflow.com/questions/35030794/soft-keyboard-not-present-cannot-hide-keyboard-appium-android?answertab=votes#tab-top
            // and testing if keyboard is already hidden by executing an ADB command,
            // we prefer to parse error message to know if it's just due to keyboard which is already hidden.
            // This way, we are more portable because it is not necessary to connect to the Appium server and send the ADB command.
            if (IS_KEYBOARD_ABSENT_ERROR_PATTERN.matcher(e.getMessage()).find()) {
                return new MessageEvent(MessageEventEnum.ACTION_SUCCESS_HIDEKEYBOARD_ALREADYHIDDEN);
            }
            LOGGER.warn("Unable to hide keyboard due to " + e.getMessage(), e);
            return new MessageEvent(MessageEventEnum.ACTION_FAILED_HIDEKEYBOARD);
        }
    }

    /**
     * Translator between Cerberus key names and Android key codes
     */
    private enum KeyCode {

        RETURN(AndroidKeyCode.ENTER),
        ENTER(AndroidKeyCode.ENTER),
        SEARCH(AndroidKeyCode.KEYCODE_SEARCH),
        BACKSPACE(AndroidKeyCode.BACKSPACE),
        BACK(AndroidKeyCode.BACK); 

        private int code;

        KeyCode(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

    }

}
