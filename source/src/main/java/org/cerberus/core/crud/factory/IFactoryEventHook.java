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
package org.cerberus.core.crud.factory;

import java.sql.Timestamp;
import org.cerberus.core.crud.entity.EventHook;

/**
 * @author vertigo17
 */
public interface IFactoryEventHook {

    /**
     *
     * @param id
     * @param eventReference
     * @param objectKey1
     * @param objectKey2
     * @param isActive
     * @param hookConnector
     * @param hookRecipient
     * @param hookChannel
     * @param description
     * @param usrCreated
     * @param dateCreated
     * @param usrModif
     * @param dateModif
     * @return
     */
    EventHook create(Integer id, String eventReference, String objectKey1, String objectKey2, boolean isActive, String hookConnector, String hookRecipient, String hookChannel, String description
            , String usrCreated, Timestamp dateCreated, String usrModif, Timestamp dateModif);


}
