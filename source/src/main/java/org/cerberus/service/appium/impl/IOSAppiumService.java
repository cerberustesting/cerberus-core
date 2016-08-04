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

import org.apache.log4j.Logger;
import org.cerberus.crud.entity.MessageEvent;
import org.cerberus.crud.entity.Session;
import org.cerberus.enums.MessageEventEnum;
import org.openqa.selenium.Keys;
import org.springframework.stereotype.Service;

/**
 * Specific IOS implementation of the {@link AppiumService}
 *
 * @author Aurelien Bourdon.
 */
@Service("IOSAppiumService")
public class IOSAppiumService extends AppiumService {

    /**
     * Associated {@link Logger} to this class
     */
    private static final Logger LOGGER = Logger.getLogger(IOSAppiumService.class);

    /**
     * Because of https://github.com/appium/java-client/issues/402
     * we are unfortunately unable to press to whatever key on the IOS keyboard.
     * This is due to an IOS limitation
     * <p>
     * Then this method only press on recognized keys by IOS, which are enumerated from {@link KeyCode}
     *
     * @param session the associated {@link Session}
     * @param keyName the key name to be pressed
     * @return a {@link MessageEvent} containing result value
     */
    @Override
    public MessageEvent keyPress(Session session, String keyName) {
        // First, check if the key name is correct, due to the IOS limitation
        KeyCode keyToPress;
        try {
            keyToPress = KeyCode.valueOf(keyName);
        } catch (IllegalArgumentException e) {
            return new MessageEvent(MessageEventEnum.ACTION_FAILED_KEYPRESS_NOT_AVAILABLE).resolveDescription("KEY", keyName);
        }

        // Then do the key press
        try {
            session.getAppiumDriver().getKeyboard().pressKey(keyToPress.getCode());
            return new MessageEvent(MessageEventEnum.ACTION_SUCCESS_KEYPRESS_NO_ELEMENT).resolveDescription("KEY", keyName);
        } catch (Exception e) {
            LOGGER.warn("Unable to key press due to " + e.getMessage());
            return new MessageEvent(MessageEventEnum.ACTION_FAILED_KEYPRESS_OTHER)
                    .resolveDescription("KEY", keyName)
                    .resolveDescription("REASON", e.getMessage());
        }
    }

    /**
     * Due to https://discuss.appium.io/t/appium-ios-guide-hiding-the-keyboard-on-real-devices/8221,
     * IOS keyboard can be only hidden by taping on a keyboard key.
     * As same as the tutorial, the {@link Keys#RETURN} (so the {@link KeyCode#RETURN} in Cerberus language) is used to hide keyboard.
     *
     * @param session
     * @return
     */
    @Override
    public MessageEvent hideKeyboard(Session session) {
        MessageEvent keyPressResult = keyPress(session, KeyCode.RETURN.name());
        return new MessageEvent(keyPressResult.getCode() == MessageEventEnum.ACTION_SUCCESS_KEYPRESS_NO_ELEMENT.getCode() ?
                MessageEventEnum.ACTION_SUCCESS_HIDEKEYBOARD :
                MessageEventEnum.ACTION_FAILED_HIDEKEYBOARD);
    }

    /**
     * The only valid IOS key codes to be able to be pressed
     * <p>
     * See https://github.com/appium/java-client/issues/402 for more information
     */
    private enum KeyCode {

        RETURN(Keys.RETURN.toString()),
        ENTER(Keys.ENTER.toString()),
        SEARCH(Keys.ENTER.toString()),
        BACKSPACE(Keys.BACK_SPACE.toString());

        private String code;

        KeyCode(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }

    }

}
