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
package org.cerberus.core.crud.service;

import org.cerberus.core.crud.entity.MyVersion;

/**
 *
 * @author bdumont
 */
public interface IMyVersionService {

    /**
     *
     * @param key
     * @return MyVersion that correspond to the key.
     */
    MyVersion findMyVersionByKey(String key);

    /**
     * This method can be used in order to retrieve a parameter directly in
     * String format.
     *
     * @param key
     * @param defaultValue
     * @return
     */
    String getMyVersionStringByKey(String key, String defaultValue);

    /**
     *
     * @param key
     * @param value
     * @return true if the update was done. False in case there were an issue.
     */
    boolean updateMyVersionString(String key, String value);

    /**
     *
     * @param version epoch timing in ms
     * @param value value of the MyVersion entry table
     * @param lockDurationMs duration when the entry will not be refreshed.
     * After that duration a new value will be authorized for update.
     * @return true if the update was done. False in case there were an issue.
     */
    boolean updateAndLockVersionEntryDuringMs(String version, long value, long lockDurationMs);

    /**
     * Flag the key. Means that the method will return true if the previous
     * value was N and update manage to move it to Y. It returns false if the
     * previous value was already Y.
     *
     * @param key
     * @return true if the update was done. False in case there were an issue.
     */
    boolean flagMyVersionString(String key);

    /**
     *
     * @param myversion
     * @return true if the update was done. False in case there were an issue.
     */
    boolean update(MyVersion myversion);

}
