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
import org.cerberus.core.crud.entity.ScheduledExecution;
import org.cerberus.core.crud.factory.IFactoryScheduledExecution;
import org.springframework.stereotype.Service;

/**
 *
 * @author cdelage
 */
@Service
public class FactoryScheduledExecution implements IFactoryScheduledExecution {

    @Override
    public ScheduledExecution create(long ID, long schedulerId, String scheduleName, String status, String comment, String usrCreated, String usrModif, Timestamp scheduledDate, Timestamp scheduleFireTime, Timestamp dateCreated, Timestamp dateModif) {
        ScheduledExecution scheduledExecution = new ScheduledExecution();
        scheduledExecution.setID(ID);
        scheduledExecution.setSchedulerId(schedulerId);
        scheduledExecution.setScheduleName(scheduleName);
        scheduledExecution.setStatus(status);
        scheduledExecution.setComment(comment);
        scheduledExecution.setUsrCreated(usrCreated);
        scheduledExecution.setUsrModif(usrModif);
        scheduledExecution.setScheduledDate(scheduledDate);
        scheduledExecution.setScheduleFireTime(scheduleFireTime);
        scheduledExecution.setDateCreated(dateCreated);
        scheduledExecution.setDateModif(dateModif);

        return scheduledExecution;
    }
}
