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

import java.util.Date;
import java.util.List;

import org.cerberus.core.crud.entity.QueueStat;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;

/**
 *
 * @author vertigo
 */
public interface IQueueStatService {

    /**
     *
     * @param from
     * @param to
     * @return
     */
    AnswerList<QueueStat> readByCriteria(Date from, Date to);

    /**
     *
     * @param from
     * @param to
     * @return
     */
    AnswerItem<Integer> readNbRowsByCriteria(Date from, Date to);

    /**
     *
     * @param object
     * @return
     */
    Answer create(QueueStat object);

    /**
     *
     * @param answerItem
     * @return
     * @throws CerberusException
     */
    QueueStat convert(AnswerItem<QueueStat> answerItem) throws CerberusException;

    /**
     *
     * @param answerList
     * @return
     * @throws CerberusException
     */
    List<QueueStat> convert(AnswerList<QueueStat> answerList) throws CerberusException;

    /**
     *
     * @param answer
     * @throws CerberusException
     */
    void convert(Answer answer) throws CerberusException;

}
