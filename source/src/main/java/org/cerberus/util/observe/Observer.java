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
package org.cerberus.util.observe;

/**
 * An observable-pattern's observer
 *
 * @param <TOPIC> the associated topic of received item from {@link Observable} triggers
 * @param <ITEM>  the type of item that will be received from {@link Observable} triggers
 * @author Aurelien Bourdon
 */
public interface Observer<TOPIC, ITEM> {

    /**
     * Callback method when a create action from an {@link Observable} occurs
     *
     * @param topic the associated topic
     * @param item  the created item
     */
    void observeCreate(TOPIC topic, ITEM item);

    /**
     * Callback method when an update action from an {@link Observable} occurs
     *
     * @param topic the associated topic
     * @param item  the updated item
     */
    void observeUpdate(TOPIC topic, ITEM item);

    /**
     * Callback method when a delete action from an {@link Observable} occurs
     *
     * @param topic the associated topic
     * @param item  the deleted item
     */
    void observeDelete(TOPIC topic, ITEM item);

}
