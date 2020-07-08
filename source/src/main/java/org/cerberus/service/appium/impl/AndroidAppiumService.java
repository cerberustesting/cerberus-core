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
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;
import io.appium.java_client.touch.WaitOptions;
import io.appium.java_client.touch.offset.PointOption;
import java.time.Duration;
import java.util.HashMap;
import java.util.regex.Pattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.crud.entity.Parameter;
import org.cerberus.crud.service.impl.ParameterService;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.engine.entity.Session;
import org.cerberus.engine.entity.SwipeAction;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.util.JSONUtil;
import org.cerberus.util.StringUtil;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Specific Android implementation of the {@link AppiumService}
 * <p>
 * e@author Aurelien Bourdon.
 */
@Service("AndroidAppiumService")
public class AndroidAppiumService extends AppiumService {

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
     * {@link AppiumService#CERBERUS_APPIUM_SWIPE_DURATION_PARAMETER} has been
     * defined
     */
    private static final int DEFAULT_CERBERUS_APPIUM_SWIPE_DURATION = 2000;

    /**
     * The {@link Pattern} related error when keyboard is absent
     */
    private static final Pattern IS_KEYBOARD_ABSENT_ERROR_PATTERN = Pattern.compile("Original error: Soft keyboard not present");

    @Override
    public MessageEvent keyPress(Session session, String keyName) {

        // Then press the key
        try {
            ((AndroidDriver) session.getAppiumDriver()).pressKey(new KeyEvent(AndroidKey.valueOf(keyName)));
            return new MessageEvent(MessageEventEnum.ACTION_SUCCESS_KEYPRESS_NO_ELEMENT).resolveDescription("KEY", keyName);

        } catch (IllegalArgumentException e) {
            return new MessageEvent(MessageEventEnum.ACTION_FAILED_KEYPRESS_NOT_AVAILABLE).resolveDescription("KEY", keyName);

        } catch (Exception e) {
            LOG.warn("Unable to key press due to " + e.getMessage(), e);
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
            LOG.warn("Unable to hide keyboard due to " + e.getMessage(), e);
            return new MessageEvent(MessageEventEnum.ACTION_FAILED_HIDEKEYBOARD);
        }
    }

    @Override
    public MessageEvent swipe(Session session, SwipeAction action) {
        try {
            SwipeAction.Direction direction = this.getDirectionForSwipe(session, action);

            // Get the parametrized swipe duration
            Parameter duration = parameters.findParameterByKey(CERBERUS_APPIUM_SWIPE_DURATION_PARAMETER, "");

            // Do the swipe thanks to the Appium driver
            TouchAction dragNDrop
                    = new TouchAction(session.getAppiumDriver()).press(PointOption.point(direction.getX1(), direction.getY1())).waitAction(WaitOptions.waitOptions(Duration.ofMillis(duration == null ? DEFAULT_CERBERUS_APPIUM_SWIPE_DURATION : Integer.parseInt(duration.getValue()))))
                            .moveTo(PointOption.point(direction.getX2(), direction.getY2())).release();
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
        if (StringUtil.isNullOrEmpty(args)) {
            value = session.getAppiumDriver().executeScript(cmd, new HashMap<>());
        } else {
            value = session.getAppiumDriver().executeScript(cmd, JSONUtil.convertFromJSONObjectString(args));
        }

        if (value != null) {
            valueString = value.toString();
        }
        // execute Script return an \n or \r\n sometimes, so we delete the last occurence of it
        if (!StringUtil.isNullOrEmpty(valueString)) {
            if (valueString.endsWith("\r\n")) {
                valueString = valueString.substring(0, valueString.lastIndexOf("\r\n"));
            }
            if (valueString.endsWith("\n")) {
                valueString = valueString.substring(0, valueString.lastIndexOf("\n"));
            }
        }

        return valueString;
    }

    @Override
    public MessageEvent installApp(Session session, String appPath) throws IllegalArgumentException {
        try {
            AndroidDriver driver = ((AndroidDriver) session.getAppiumDriver());

            driver.installApp(appPath);

            return new MessageEvent(MessageEventEnum.ACTION_SUCCESS_INSTALLAPP);
        } catch (Exception e) {
            LOG.warn("Unable to install app " + e.getMessage(), e);
            return new MessageEvent(MessageEventEnum.ACTION_FAILED_GENERIC)
                    .resolveDescription("DETAIL", "Unable to install app " + e.getMessage());
        }
    }

    @Override
    public MessageEvent removeApp(Session session, String appPackage) throws IllegalArgumentException {
        try {
            AndroidDriver driver = ((AndroidDriver) session.getAppiumDriver());

            if (driver.isAppInstalled(appPackage)) {
                driver.removeApp(appPackage);
            }

            return new MessageEvent(MessageEventEnum.ACTION_SUCCESS_REMOVEAPP);
        } catch (Exception e) {
            LOG.warn("Unable to remove app " + e.getMessage(), e);
            return new MessageEvent(MessageEventEnum.ACTION_FAILED_GENERIC)
                    .resolveDescription("DETAIL", "Unable to remove app " + e.getMessage());
        }
    }

    @Override
    public MessageEvent openApp(Session session, String appPackage, String appActivity) {
        return executeCommand(session, "am start", "-n " + appPackage + "/" + appActivity + "\n");
    }

    @Override
    public MessageEvent closeApp(Session session) {
        try {

            session.getAppiumDriver().closeApp();

            return new MessageEvent(MessageEventEnum.ACTION_SUCCESS_CLOSEAPP_GENERIC);

        } catch (Exception e) {
            LOG.warn("Unable to close app " + e.getMessage(), e);
            return new MessageEvent(MessageEventEnum.ACTION_FAILED_GENERIC)
                    .resolveDescription("DETAIL", "Unable to close app : " + e.getMessage());
        }
    }

}
