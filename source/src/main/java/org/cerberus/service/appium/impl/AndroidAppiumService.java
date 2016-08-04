/*
 * Cerberus  Copyright (C) 2016  vertigo17
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
import org.cerberus.crud.entity.MessageEvent;
import org.cerberus.crud.entity.Session;
import org.cerberus.enums.MessageEventEnum;
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
     * Specific ADB command to know if keyboard is currently opened from the connected device
     */
    private static final String IS_KEYBOAD_OPEN_COMMAND = "adb shell dumpsys input_method | grep mInputShown";

    /**
     * {@link #IS_KEYBOARD_OPEN_PATTERN} {@link Pattern} related to retrieve keyboard open state
     */
    private static final Pattern IS_KEYBOARD_OPEN_PATTERN = Pattern.compile(".*mInputShown=([^\\s]+).*");

    /**
     * The value from #IS_KEYBOAD_OPEN_COMMAND if keyboard is currently opened from the connected device
     */
    private static final String IS_KEYBOARD_OPEN_VALUE = "true";

    @Override
    public MessageEvent keyPress(Session session, String keyName) {
        try {
            ((AndroidDriver) session.getAppiumDriver()).pressKeyCode(KeyCode.valueOf(keyName).getCode());
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
            // First, check if keyboard is currently opened
            boolean isKeyboardOpened = false;
            try (Scanner scanner = new Scanner(Runtime.getRuntime().exec(IS_KEYBOAD_OPEN_COMMAND).getInputStream())) {
                while (scanner.hasNext()) {
                    Matcher candidateLine = IS_KEYBOARD_OPEN_PATTERN.matcher(scanner.nextLine());
                    if (candidateLine.matches() && IS_KEYBOARD_OPEN_VALUE.equals(candidateLine.group(1))) {
                        isKeyboardOpened = true;
                        break;
                    }
                }
            }

            // Then hide keyboard if necessary
            if (isKeyboardOpened) {
                session.getAppiumDriver().hideKeyboard();
                return new MessageEvent(MessageEventEnum.ACTION_SUCCESS_HIDEKEYBOARD);
            }
            return new MessageEvent(MessageEventEnum.ACTION_SUCCESS_HIDEKEYBOARD_ALREADYHIDDEN);
        } catch (Exception e) {
            LOGGER.warn("Unable to hide keyboard due to " + e.getMessage(), e);
            return new MessageEvent(MessageEventEnum.ACTION_FAILED_HIDEKEYBOARD);
        }
    }

    /**
     * Translator between Cerberus and Android key codes
     */
    private enum KeyCode {

        ENTER(AndroidKeyCode.ENTER), SEARCH(AndroidKeyCode.ENTER);

        private int code;

        KeyCode(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

    }

}
