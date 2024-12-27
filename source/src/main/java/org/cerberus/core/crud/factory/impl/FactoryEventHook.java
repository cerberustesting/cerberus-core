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
package org.cerberus.core.crud.factory.impl;

import java.sql.Timestamp;
import org.cerberus.core.crud.entity.EventHook;
import org.cerberus.core.crud.factory.IFactoryEventHook;
import org.springframework.stereotype.Service;

/**
 * @author vertigo17
 */
@Service
public class FactoryEventHook implements IFactoryEventHook {

    @Override
    public EventHook create(Integer id, String eventReference, String objectKey1, String objectKey2, boolean isActive, String hookConnector, String hookRecipient, String hookChannel, String description,
            String usrCreated, Timestamp dateCreated, String usrModif, Timestamp dateModif) {

        EventHook eventHookObject = new EventHook();
        eventHookObject.setId(id);
        eventHookObject.setEventReference(eventReference);
        eventHookObject.setObjectKey1(objectKey1);
        eventHookObject.setObjectKey2(objectKey2);
        eventHookObject.setActive(isActive);
        eventHookObject.setHookConnector(hookConnector);
        eventHookObject.setHookRecipient(hookRecipient);
        eventHookObject.setHookChannel(hookChannel);
        eventHookObject.setDescription(description);
        eventHookObject.setDateCreated(dateCreated);
        eventHookObject.setDateModif(dateModif);
        eventHookObject.setUsrCreated(usrCreated);
        eventHookObject.setUsrModif(usrModif);

        return eventHookObject;
    }

}
