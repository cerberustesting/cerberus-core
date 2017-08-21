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
package org.cerberus.service.sikuli;

import java.io.File;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.engine.entity.Session;
import org.cerberus.util.answer.AnswerItem;
import org.json.JSONObject;

/**
 *
 * @author bcivel
 */
public interface ISikuliService {
    
    public boolean isSikuliServerReachable(Session session);
    
    /**
     *
     * @param session
     * @param action
     * @param locator
     * @param text
     * @return
     */
    public AnswerItem<JSONObject> doSikuliAction(Session session, String action, String locator, String text);
    
    public MessageEvent doSikuliActionClick(Session session, String locator, String text);
    
    public MessageEvent doSikuliActionOpenApp(Session session, String locator);
    
    public MessageEvent doSikuliActionCloseApp(Session session, String locator);
    
    public File takeScreenShotFile(Session session);

    public MessageEvent doSikuliActionRightClick(Session session, String locator, String text);

    public MessageEvent doSikuliActionSwitchApp(Session session, String locator);

    public MessageEvent doSikuliActionDoubleClick(Session session, String locator, String text);

    public MessageEvent doSikuliActionType(Session session, String locator, String property);

    public MessageEvent doSikuliActionMouseOver(Session session, String locator, String text);

    public MessageEvent doSikuliActionWait(Session session, String locator, String text);
    
    public MessageEvent doSikuliActionWaitVanish(Session session, String locator, String text);

    public MessageEvent doSikuliActionKeyPress(Session session, String locator, String property);

    public MessageEvent doSikuliVerifyElementPresent(Session session, String locator);
    
    public MessageEvent doSikuliVerifyElementNotPresent(Session session, String locator);
    
    public MessageEvent doSikuliVerifyTextInPage(Session session, String locator);
}
