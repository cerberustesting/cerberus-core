/**
 * Cerberus Copyright (C) 2013 - 2026 cerberustesting
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
import org.cerberus.core.crud.entity.ScheduledExecution;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerList;

/**
 *
 * @author cdelage
 */
public interface IScheduledExecutionDAO {

    /**
     *
     * @param object
     * @return
     * @throws org.cerberus.core.exception.CerberusException
     */
    public long create(ScheduledExecution object) throws CerberusException;
    /**
     *
     * @param object
     * @return
     * @throws org.cerberus.core.exception.CerberusException
     */
    public long createWhenNotExist(ScheduledExecution object) throws CerberusException;

    /**
     *
     * @param scheduledExecutionObject
     * @return
     */
    public Answer update(ScheduledExecution scheduledExecutionObject);

    /**
     * @param since only the executions scheduled at or after this date
     * @param maxRows maximum number of rows returned (most recent first)
     * @return the scheduled executions, most recent first
     */
    public AnswerList<ScheduledExecution> readSince(Timestamp since, int maxRows);
}
