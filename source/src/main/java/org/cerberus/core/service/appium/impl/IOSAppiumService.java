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
package org.cerberus.core.service.appium.impl;

import com.google.common.collect.ImmutableMap;
import io.appium.java_client.InteractsWithApps;
import io.appium.java_client.ios.IOSDriver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.entity.Parameter;
import org.cerberus.core.crud.service.impl.ParameterService;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.engine.entity.Session;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.service.appium.SwipeAction;
import org.cerberus.core.service.appium.SwipeAction.Direction;
import org.cerberus.core.util.JSONUtil;
import org.cerberus.core.util.StringUtil;
import org.json.JSONException;
import org.openqa.selenium.Keys;
import org.openqa.selenium.ScreenOrientation;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
     * AppiumService.CERBERUS_APPIUM_SWIPE_DURATION_PARAMETER has been
     * defined
     */
    private static final int DEFAULT_CERBERUS_APPIUM_SWIPE_DURATION = 2000;

    /**
     * Because of https://github.com/appium/java-client/issues/402 we are
     * unfortunately unable to press to whatever key on the IOS keyboard. This
     * is due to an IOS limitation
     * <p>
     * Then this method only press on recognized keys by IOS, which are
     * enumerated from {@link KeyCode}
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
            Actions actions = new Actions(session.getAppiumDriver());
            actions.sendKeys(keyToPress.getCode()).perform();

            return new MessageEvent(MessageEventEnum.ACTION_SUCCESS_KEYPRESS_NO_ELEMENT_NO_MODIFIER).resolveDescription("KEY", keyName);
        } catch (Exception e) {
            LOG.warn("Unable to key press due to " + e.getMessage());
            return new MessageEvent(MessageEventEnum.ACTION_FAILED_KEYPRESS_OTHER_NOELEMENT_NOMODIFIER)
                    .resolveDescription("KEY", keyName)
                    .resolveDescription("REASON", e.getMessage());
        }
    }

    /**
     * Due to
     * https://discuss.appium.io/t/appium-ios-guide-hiding-the-keyboard-on-real-devices/8221,
     * IOS keyboard can be only hidden by taping on a keyboard key. As same as
     * the tutorial, the {@link Keys#RETURN} (so the {@link KeyCode#RETURN} in
     * Cerberus language) is used to hide keyboard.
     *
     * @param session
     * @return
     */
    @Override
    public MessageEvent hideKeyboard(Session session) {
        MessageEvent keyPressResult = keyPress(session, KeyCode.RETURN.name());
        return new MessageEvent(MessageEventEnum.ACTION_SUCCESS_KEYPRESS_NO_ELEMENT_NO_MODIFIER.equals(keyPressResult.getSource())
                ? MessageEventEnum.ACTION_SUCCESS_HIDEKEYBOARD
                : MessageEventEnum.ACTION_FAILED_HIDEKEYBOARD);
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
            int swipeDuration = duration == null
                    ? DEFAULT_CERBERUS_APPIUM_SWIPE_DURATION
                    : Integer.parseInt(duration.getValue());

            // Initialize PointerInput for touch gestures
            PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
            Sequence swipe = new Sequence(finger, 0);

            // Start point of the swipe
            swipe.addAction(finger.createPointerMove(Duration.ofMillis(0),
                    PointerInput.Origin.viewport(), direction.getX1(), direction.getY1()));
            swipe.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));

            // Move to the end point of the swipe
            swipe.addAction(finger.createPointerMove(Duration.ofMillis(swipeDuration),
                    PointerInput.Origin.viewport(), direction.getX2(), direction.getY2()));

            // Release touch
            swipe.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

            // Perform the swipe
            session.getAppiumDriver().perform(Collections.singletonList(swipe));

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

    @Override
    public MessageEvent executeCommand(Session session, String cmd, String args) throws IllegalArgumentException {
        try {
            String message = executeCommandString(session, cmd, args);

            return new MessageEvent(MessageEventEnum.ACTION_SUCCESS_EXECUTECOMMAND).resolveDescription("LOG", message);
        } catch (Exception e) {
            LOG.warn("Unable to execute command screen due to " + e.getMessage(), e);
            return new MessageEvent(MessageEventEnum.ACTION_FAILED_EXECUTECOMMAND)
                    .resolveDescription("EXCEPTION", e.getMessage());
        }
    }

    @Override
    public String executeCommandString(Session session, String cmd, String args) throws IllegalArgumentException, JSONException {

        Object value;
        String valueString = "";
        if (StringUtil.isEmptyOrNull(args)) {
            value = session.getAppiumDriver().executeScript(cmd, new HashMap<>());
        } else {
            value = session.getAppiumDriver().executeScript(cmd, JSONUtil.convertFromJSONObjectString(args));
        }

        if (value != null) {
            valueString = value.toString();
        }
        // execute Script return an \n or \r\n sometimes, so we delete the last occurence of it
        if (!StringUtil.isEmptyOrNull(valueString)) {
            if (valueString.endsWith("\r\n")) {
                valueString = valueString.substring(0, valueString.lastIndexOf("\r\n"));
            }
            if (valueString.endsWith("\n")) {
                valueString = valueString.substring(0, valueString.lastIndexOf('\n'));
            }
        }

        return valueString;
    }

    @Override
    public MessageEvent installApp(Session session, String appPath) throws IllegalArgumentException {
        return new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
    }

    @Override
    public MessageEvent removeApp(Session session, String appPackage) throws IllegalArgumentException {
        return new MessageEvent(MessageEventEnum.ACTION_NOTEXECUTED_NOTSUPPORTED_FOR_APPLICATION);
    }

    @Override
    public MessageEvent openApp(Session session, String appPackage, String appActivity) {
        try {
            ((IOSDriver) session.getAppiumDriver()).activateApp(appPackage);

            return new MessageEvent(MessageEventEnum.ACTION_SUCCESS_OPENAPP).resolveDescription("APP", appPackage);

        } catch (Exception e) {
            LOG.warn("Unable to open app. " + e.getMessage(), e);
            return new MessageEvent(MessageEventEnum.ACTION_FAILED_GENERIC)
                    .resolveDescription("DETAIL", "Unable to open app " + e.getMessage());
        }
    }

    @Override
    public MessageEvent closeApp(Session session) {
        try {
            IOSDriver appiumDriver = (IOSDriver) session.getAppiumDriver();

            appiumDriver.terminateApp(appiumDriver.getCapabilities().getCapability("appium:bundleId").toString());

            return new MessageEvent(MessageEventEnum.ACTION_SUCCESS_CLOSEAPP_GENERIC);

        } catch (Exception e) {
            LOG.warn("Unable to close app " + e.getMessage(), e);
            return new MessageEvent(MessageEventEnum.ACTION_FAILED_GENERIC)
                    .resolveDescription("DETAIL", "Unable to close app : " + e.getMessage());
        }
    }

    @Override
    public MessageEvent lockDevice(Session session) {
        try {
            IOSDriver driver = ((IOSDriver) session.getAppiumDriver());
            driver.lockDevice();

            return new MessageEvent(MessageEventEnum.ACTION_SUCCESS_LOCKDEVICE_GENERIC);

        } catch (Exception e) {
            LOG.warn("Unable to close app " + e.getMessage(), e);
            return new MessageEvent(MessageEventEnum.ACTION_FAILED_GENERIC)
                    .resolveDescription("DETAIL", "Unable to close app : " + e.getMessage());
        }
    }

    @Override
    public MessageEvent unlockDevice(Session session) {
        try {
            IOSDriver driver = ((IOSDriver) session.getAppiumDriver());
            driver.unlockDevice();

            return new MessageEvent(MessageEventEnum.ACTION_SUCCESS_UNLOCKDEVICE_GENERIC);

        } catch (Exception e) {
            LOG.warn("Unable to close app " + e.getMessage(), e);
            return new MessageEvent(MessageEventEnum.ACTION_FAILED_GENERIC)
                    .resolveDescription("DETAIL", "Unable to close app : " + e.getMessage());
        }
    }

    @Override
    public MessageEvent rotateDevice(Session session) {
        try {
            IOSDriver driver = ((IOSDriver) session.getAppiumDriver());
            LOG.warn("orientation : " + driver.getOrientation().value());
            driver.rotate(ScreenOrientation.LANDSCAPE == driver.getOrientation() ? ScreenOrientation.PORTRAIT : ScreenOrientation.LANDSCAPE);

            return new MessageEvent(MessageEventEnum.ACTION_SUCCESS_UNLOCKDEVICE_GENERIC);

        } catch (Exception e) {
            LOG.warn("Unable to close app " + e.getMessage(), e);
            return new MessageEvent(MessageEventEnum.ACTION_FAILED_GENERIC)
                    .resolveDescription("DETAIL", "Unable to close app : " + e.getMessage());
        }
    }

}
