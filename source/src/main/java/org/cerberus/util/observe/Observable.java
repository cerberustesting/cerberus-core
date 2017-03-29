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
 * An observable-pattern's observable.
 * <p>
 * Change event registration can be filtered by topics.
 *
 * @param <TOPIC> the type of topic that can be use to filter observeUpdate events
 * @param <ITEM>  type type of items that will be observe and fired by this observable
 * @author Aurelien Bourdon
 */
public interface Observable<TOPIC, ITEM> {

    /**
     * Register to any changes from this observable
     *
     * @param observer the {@link Observer} to register
     * @return <code>true</code> if registration success, <code>false</code> otherwise
     */
    boolean register(Observer<TOPIC, ITEM> observer);

    /**
     * Register to changes according to the given topic
     *
     * @param topic    the related topic from which register the given observer
     * @param observer the {@link Observer} to register
     * @return <code>true</code> if registration success, <code>false</code> otherwise
     */
    boolean register(TOPIC topic, Observer<TOPIC, ITEM> observer);

    /**
     * Unregister to changes according to the given topic from this observable.
     * <p>
     * Warning: if observer is already registered via {@link #register(Observer)}, then observer will still be triggered by
     * this topic relative changes, because registered to be triggered by all changes.
     *
     * @param topic the related topic from which unregister the given observer
     * @return <code>true</code> if unregistration success, <code>false</code> otherwise
     */
    boolean unregister(TOPIC topic, Observer<TOPIC, ITEM> observer);

    /**
     * Unregister to changes from this observable.
     * <p>
     * Observer will be also unregistered from previous specific topic registrations.
     *
     * @param observer the {@link Observer} to register
     * @return <code>true</code> if unregistration success, <code>false</code> otherwise
     */
    boolean unregister(Observer<TOPIC, ITEM> observer);

    /**
     * Fire created item to registered observers
     *
     * @param topic the related topic to the item
     * @param item  the created item to fire to registered observers
     */
    void fireCreate(TOPIC topic, ITEM item);

    /**
     * Fire updated item to registered observers
     *
     * @param topic the related topic to the item
     * @param item  the updated item to fire to registered observers
     */
    void fireUpdate(TOPIC topic, ITEM item);

    /**
     * Fire deleted item to the registerd observers
     *
     * @param topic the related topic the item
     * @param item  the deleted item to fire to registered observers
     */
    void fireDelete(TOPIC topic, ITEM item);

}
