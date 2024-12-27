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
package org.cerberus.core.crud.dao;

import java.sql.Timestamp;
import org.cerberus.core.crud.entity.ScheduleEntry;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;

/**
 *
 * @author cdelage
 */
public interface IScheduleEntryDAO {

    /**
     *
     * @param id
     * @return
     */
    public AnswerItem<ScheduleEntry> readByKey(long id);

    /**
     *
     * @param scheduler
     * @return
     */
    public AnswerItem<Integer> create(ScheduleEntry scheduler);
    
    /**
     *
     * @return
     */
    public AnswerList<ScheduleEntry> readAllActive();
    
    /**
     *
     * @param scheduleEntryObject
     * @return
     */
    public Answer update(ScheduleEntry scheduleEntryObject);
    
    /**
     *
     * @param object
     * @return
     */
    public Answer delete(ScheduleEntry object);
    
    /**
     *
     * @param name
     * @return
     */
    public AnswerList<ScheduleEntry> readByName(String name);
    
    /**
     *
     * @param schedulerId
     * @param lastExecution
     * @return
     */
    public Answer updateLastExecution(long schedulerId, Timestamp lastExecution);
}
