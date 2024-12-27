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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.entity.ScheduleEntry;
import org.cerberus.core.crud.factory.IFactoryScheduleEntry;
import org.springframework.stereotype.Service;

/**
 *
 * @author cdelage
 */

@Service
public class FactoryScheduleEntry implements IFactoryScheduleEntry {
    
    private static final Logger LOG = LogManager.getLogger(FactoryScheduleEntry.class);

    @Override
    public ScheduleEntry create(long ID, String type, String name, String cronDefinition, Timestamp lastExecution, String active, String description, String UsrCreated, Timestamp DateCreated, String UsrModif, Timestamp DateModif) {
        ScheduleEntry scheduler = new ScheduleEntry();
        scheduler.setID(ID);
        scheduler.setType(type);
        scheduler.setName(name);
        scheduler.setCronDefinition(cronDefinition);
        scheduler.setLastExecution(lastExecution);
        scheduler.setActive(active);
        scheduler.setDescription(description);
        scheduler.setUsrCreated(UsrCreated);
        scheduler.setDateCreated(DateCreated);
        scheduler.setUsrModif(UsrModif);
        scheduler.setDateModif(DateModif);
   
        return scheduler;
    }

}
