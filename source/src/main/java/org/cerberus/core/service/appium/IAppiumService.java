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
package org.cerberus.core.service.appium;

import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.engine.entity.Identifier;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.engine.entity.Session;
import org.cerberus.core.service.appium.SwipeAction.Direction;

/**
 * @author bcivel
 */
public interface IAppiumService {

    /**
     * @param session
     * @param identifier
     * @return
     */
    MessageEvent switchToContext(Session session, Identifier identifier);

    /**
     * @param session Appium session
     * @param context Name of the context to switch to
     * @return Message to inform of the action result
     */
    MessageEvent switchToContext(Session session, String context);

    /**
     * @param session
     * @param identifier
     * @return
     */
    MessageEvent wait(Session session, Identifier identifier);

    /**
     * @param session
     * @param identifier
     * @param valueToType
     * @param propertyName
     * @return
     */
    MessageEvent type(Session session, Identifier identifier, String valueToType, String propertyName);

    /**
     * @param session
     * @param identifier
     * @return
     */
    MessageEvent click(Session session, Identifier identifier, Integer hOffset, Integer vOffset);

    /**
     * @param session
     * @param keyName
     * @return
     */
    MessageEvent keyPress(Session session, String keyName);

    /**
     * @param session
     * @return
     */
    MessageEvent hideKeyboard(Session session);

    /**
     * @param session
     * @param swipeAction
     * @return
     */
    MessageEvent swipe(Session session, SwipeAction swipeAction);

    /**
     * @param session
     * @param action
     * @return
     * @throws IllegalArgumentException
     */
    Direction getDirectionForSwipe(Session session, SwipeAction action) throws IllegalArgumentException;

    /**
     * @param session
     * @param cmd
     * @param args
     * @return
     * @throws IllegalArgumentException
     */
    MessageEvent executeCommand(Session session, String cmd, String args) throws IllegalArgumentException;

    /**
     * Scroll to an element or a text
     *
     * @param session
     * @param element if not null or not empty, switch to this element
     * @param numberScrollDownMax    if not null or not empty, switch to this text
     * @return
     * @throws IllegalArgumentException
     */
    MessageEvent scrollTo(TestCaseExecution testCaseExecution, Identifier element, String numberScrollDownMax, Integer hOffset, Integer vOffset) throws IllegalArgumentException;

    /**
     * install an application on mobile devices
     *
     * @param session
     * @param appPath
     * @return
     * @throws IllegalArgumentException
     */
    MessageEvent installApp(Session session, String appPath) throws IllegalArgumentException;

    /**
     * uninstall an application on mobile devices
     *
     * @param session
     * @param appPackage
     * @return
     * @throws IllegalArgumentException
     */
    MessageEvent removeApp(Session session, String appPackage) throws IllegalArgumentException;

    /**
     * Open application
     *
     * @param session
     * @param appPackage
     * @param appActivity
     * @return
     */
    MessageEvent openApp(Session session, String appPackage, String appActivity);

    /**
     * Open application
     *
     * @param session
     * @return
     */
    MessageEvent closeApp(Session session);

    /**
     * @param session
     * @param identifier
     * @param pressDuration
     * @return
     */
    MessageEvent longPress(Session session, Identifier identifier, Integer pressDuration);

    /**
     * @param session
     * @param identifier
     * @return
     */
    MessageEvent clearField(Session session, Identifier identifier);

    /**
     * @param session
     * @return
     */
    MessageEvent lockDevice(Session session);

    /**
     * @param session
     * @return
     */
    MessageEvent unlockDevice(Session session);

    /**
     * @param session
     * @return
     */
    MessageEvent rotateDevice(Session session);
}
