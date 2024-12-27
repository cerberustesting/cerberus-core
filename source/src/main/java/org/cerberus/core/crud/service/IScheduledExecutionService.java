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

import java.util.List;
import org.cerberus.core.crud.entity.ScheduledExecution;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;

/**
 *
 * @author cdelage
 */
public interface IScheduledExecutionService {

    /**
     *
     * @param scheduledExecution
     * @return
     * @throws org.cerberus.core.exception.CerberusException
     */
    public long create(ScheduledExecution scheduledExecution) throws CerberusException;

    /**
     *
     * @param scheduledExecution
     * @return
     */
    public Answer update(ScheduledExecution scheduledExecution);

    /**
     *
     * @param answerItem
     * @return
     * @throws CerberusException
     */
    ScheduledExecution convert(AnswerItem<ScheduledExecution> answerItem) throws CerberusException;

    /**
     *
     * @param answerList
     * @return
     * @throws CerberusException
     */
    List<ScheduledExecution> convert(AnswerList<ScheduledExecution> answerList) throws CerberusException;

    /**
     *
     * @param answer
     * @throws CerberusException
     */
    void convert(Answer answer) throws CerberusException;

}
