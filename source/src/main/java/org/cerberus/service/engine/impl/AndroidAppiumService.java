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

import io.appium.java_client.AndroidKeyCode;
import org.cerberus.crud.entity.MessageEvent;
import org.cerberus.crud.entity.Session;
import org.cerberus.enums.MessageEventEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

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
    private static final Logger LOGGER = LoggerFactory.getLogger(AndroidAppiumService.class);

    @Override
    public MessageEvent keyPress(Session session, String keyName) {
        try {
            session.getAppiumDriver().sendKeyEvent(KeyCode.valueOf(keyName).getCode());
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
