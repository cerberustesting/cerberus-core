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
package org.cerberus.service.engine.impl;

import io.appium.java_client.IOSKeyCode;
import org.apache.log4j.Logger;
import org.cerberus.crud.entity.MessageEvent;
import org.cerberus.crud.entity.Session;
import org.cerberus.enums.MessageEventEnum;
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

    @Override
    public MessageEvent keyPress(Session session, String keyName) {
        try {
            // Check if the key name is well known
            KeyCode.valueOf(keyName);

            // Do the keyPress for the ENTER or SEARCH key
            session.getAppiumDriver().getKeyboard().sendKeys("\n");

            // Finally return success
            return new MessageEvent(MessageEventEnum.ACTION_SUCCESS_KEYPRESS_NO_ELEMENT).resolveDescription("KEY", keyName);
        } catch (Exception e) {
            LOGGER.warn("Unable to key press due to " + e.getMessage());
            return new MessageEvent(MessageEventEnum.ACTION_FAILED_KEYPRESS_OTHER)
                    .resolveDescription("KEY", keyName)
                    .resolveDescription("REASON", e.getMessage());
        }
    }

    @Override
    public MessageEvent hideKeyboard(Session session) {
        MessageEvent keyPressResult = keyPress(session, KeyCode.ENTER.name());
        return new MessageEvent(keyPressResult.getCode() == MessageEventEnum.ACTION_SUCCESS_KEYPRESS_NO_ELEMENT.getCode() ?
                MessageEventEnum.ACTION_SUCCESS_HIDEKEYBOARD :
                MessageEventEnum.ACTION_FAILED_HIDEKEYBOARD);
    }

    /**
     * Translator between Cerberus and IOS key codes
     */
    private enum KeyCode {

        ENTER(IOSKeyCode.ENTER), SEARCH(IOSKeyCode.ENTER);

        private int code;

        KeyCode(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

    }

}
