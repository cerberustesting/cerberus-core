/*
 * Cerberus  Copyright (C) 2013  vertigo17
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
package org.cerberus.service.engine;

import org.cerberus.crud.entity.Identifier;
import org.cerberus.crud.entity.MessageEvent;
import org.cerberus.crud.entity.Session;
import org.cerberus.crud.entity.SwipeAction;

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

}
