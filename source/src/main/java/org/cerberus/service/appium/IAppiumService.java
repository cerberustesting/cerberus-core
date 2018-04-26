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
package org.cerberus.service.appium;

import org.cerberus.engine.entity.Identifier;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.engine.entity.Session;
import org.cerberus.engine.entity.SwipeAction;
import org.cerberus.engine.entity.SwipeAction.Direction;

/**
 *
 * @author bcivel
 */
public interface IAppiumService {
    
    MessageEvent switchToContext(Session session, Identifier identifier);
    
    MessageEvent type(Session session, Identifier identifier, String property, String propertyName);

    MessageEvent click(Session session, Identifier identifier);

    MessageEvent keyPress(Session session, String keyName);

    MessageEvent hideKeyboard(Session session);

    MessageEvent swipe(Session session, SwipeAction swipeAction);
    
    Direction getDirectionForSwipe(Session session, SwipeAction action) throws IllegalArgumentException;

    MessageEvent executeCommand(Session session, String cmd, String args) throws IllegalArgumentException;

    /**
     * Scroll to an element or a text
     * @param session
     * @param element if not null or not empty, switch to this element
     * @param text if not null or not empty, switch to this text
     * @return
     * @throws IllegalArgumentException
     */
    MessageEvent scrollTo(Session session, Identifier element, String text) throws IllegalArgumentException;

}
