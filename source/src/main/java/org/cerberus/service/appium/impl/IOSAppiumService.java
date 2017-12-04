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

import io.appium.java_client.TouchAction;
import io.appium.java_client.ios.IOSTouchAction;
import java.time.Duration;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.cerberus.crud.entity.Parameter;
import org.cerberus.crud.service.impl.ParameterService;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.engine.entity.Session;
import org.cerberus.engine.entity.SwipeAction;
import org.cerberus.engine.entity.SwipeAction.Direction;
import org.cerberus.enums.MessageEventEnum;
import org.openqa.selenium.Keys;
import org.springframework.beans.factory.annotation.Autowired;
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
    private static final Logger LOG = LogManager.getLogger(IOSAppiumService.class);
    
    @Autowired
    private ParameterService parameters;
    
    
    /**
     * The Appium swipe duration parameter which is got thanks to the
     * {@link ParameterService}
     */
    private static final String CERBERUS_APPIUM_SWIPE_DURATION_PARAMETER = "cerberus_appium_swipe_duration";
    
    /**
     * The default Appium swipe duration if no
     * {@link AppiumService#APPIUM_SWIPE_DURATION_PARAMETER} has been defined
     */
    private static final int DEFAULT_CERBERUS_APPIUM_SWIPE_DURATION = 2000;

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
            LOG.warn("Unable to key press due to " + e.getMessage());
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
        return new MessageEvent(MessageEventEnum.ACTION_SUCCESS_KEYPRESS_NO_ELEMENT.equals(keyPressResult.getSource()) ?
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

    @Override
    public MessageEvent swipe(Session session, SwipeAction action) {
        try {
            Direction direction = this.getDirectionForSwipe(session, action);
            
            // Get the parametrized swipe duration
            Parameter duration = parameters.findParameterByKey(CERBERUS_APPIUM_SWIPE_DURATION_PARAMETER, "");

            // Do the swipe thanks to the Appium driver
            TouchAction dragNDrop
                    = new IOSTouchAction(session.getAppiumDriver()).longPress(direction.getX1(), direction.getY1(), Duration.ofMillis(duration == null ? DEFAULT_CERBERUS_APPIUM_SWIPE_DURATION : Integer.parseInt(duration.getValue())))
                            .moveTo(direction.getX2(), direction.getY2()).release();
            dragNDrop.perform();
                       
            return new MessageEvent(MessageEventEnum.ACTION_SUCCESS_SWIPE).resolveDescription("DIRECTION", action.getActionType().name());
        } catch (IllegalArgumentException e) {
            return new MessageEvent(MessageEventEnum.ACTION_FAILED_SWIPE)
                            .resolveDescription("DIRECTION", action.getActionType().name())
                            .resolveDescription("REASON", "Unknown direction");
        } catch (Exception e) {
            LOG.warn("Unable to swipe screen due to " + e.getMessage(), e);
            return new MessageEvent(MessageEventEnum.ACTION_FAILED_SWIPE)
                    .resolveDescription("DIRECTION", action.getActionType().name())
                    .resolveDescription("REASON", e.getMessage());
        }
    }
        
    }
    

