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
package org.cerberus.core.service.robotextension;

import java.io.File;

import org.cerberus.core.engine.entity.Identifier;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.engine.entity.Session;
import org.cerberus.core.util.answer.AnswerItem;
import org.json.JSONObject;

/**
 *
 * @author bcivel
 */
public interface ISikuliService {

    /**
     *
     * @param session
     * @return
     */
    public boolean isSikuliServerReachableOnRobot(Session session);

    /**
     *
     * @param session
     * @return
     */
    public boolean isSikuliServerReachableOnNode(Session session);

    /**
     *
     * @param session
     * @param action
     * @param locator
     * @param text
     * @return
     */
    public AnswerItem<JSONObject> doSikuliAction(Session session, String action, String locator, String locator2, String text, String text2);

    /**
     *
     * @param session
     * @param locator
     * @param text
     * @return
     */
    public MessageEvent doSikuliActionClick(Session session, String locator, String text);

    /**
     *
     * @param session
     * @param locatorDrag
     * @param locatorDrop
     * @return
     */
    public MessageEvent doSikuliActionDragAndDrop(Session session, Identifier locatorDrag, Identifier locatorDrop);

    /**
     *
     * @param session
     * @param locator
     * @return
     */
    public MessageEvent doSikuliActionOpenApp(Session session, String locator);

    /**
     *
     * @param session
     * @param locator
     * @return
     */
    public MessageEvent doSikuliActionCloseApp(Session session, String locator);

    /**
     *
     * @param session
     * @return
     */
    public File takeScreenShotFile(Session session);

    /**
     *
     * @param session
     * @param locator
     * @param text
     * @return
     */
    public MessageEvent doSikuliActionRightClick(Session session, String locator, String text);

    /**
     *
     * @param session
     * @param locator
     * @return
     */
    public MessageEvent doSikuliActionSwitchApp(Session session, String locator);

    /**
     *
     * @param session
     * @return
     */
    public MessageEvent doSikuliActionLeftButtonPress(Session session);

    /**
     *
     * @param session
     * @return
     */
    public MessageEvent doSikuliActionLeftButtonRelease(Session session);

    /**
     *
     * @param session
     * @param xyoffset x and y offset to move. Ex : 100,200
     * @return
     */
    public MessageEvent doSikuliActionMouseMove(Session session, String xyoffset);

    /**
     *
     * @param session
     * @param locator
     * @param text
     * @return
     */
    public MessageEvent doSikuliActionDoubleClick(Session session, String locator, String text);

    /**
     *
     * @param session
     * @param locator
     * @param property
     * @return
     */
    public MessageEvent doSikuliActionType(Session session, String locator, String property);

    /**
     *
     * @param session
     * @param locator
     * @param text
     * @return
     */
    public MessageEvent doSikuliActionMouseOver(Session session, String locator, String text, String offset);

    /**
     *
     * @param session
     * @param locator
     * @param text
     * @return
     */
    public MessageEvent doSikuliActionWait(Session session, String locator, String text);

    /**
     *
     * @param session
     * @param locator
     * @param text
     * @return
     */
    public MessageEvent doSikuliActionWaitVanish(Session session, String locator, String text);

    /**
     *
     * @param session
     * @param locator
     * @param textToKey
     * @param modifier
     * @return
     */
    public MessageEvent doSikuliActionKeyPress(Session session, String locator, String textToKey, String modifier);

    /**
     *
     * @param session
     * @param locator
     * @param text
     * @return
     */
    public MessageEvent doSikuliVerifyElementPresent(Session session, String locator, String text);

    /**
     *
     * @param session
     * @param locator
     * @param text
     * @return
     */
    public MessageEvent doSikuliVerifyElementNotPresent(Session session, String locator, String text);

    /**
     *
     * @param session
     * @param locator
     * @return
     */
    public MessageEvent doSikuliVerifyTextInPage(Session session, String locator);

    /**
     *
     * @param session
     * @return
     */
    public MessageEvent doSikuliEndExecution(Session session);

}
