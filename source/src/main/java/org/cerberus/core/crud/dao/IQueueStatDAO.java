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

import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerList;

import java.util.Date;
import org.cerberus.core.crud.entity.QueueStat;
import org.cerberus.core.util.answer.AnswerItem;

/**
 * Interface that defines the public methods to manage Application data on table
 * Insert, Delete, Update, Find
 *
 * @author tbernardes
 * @version 1.0, 15/10/13
 * @since 0.9.0
 */
public interface IQueueStatDAO {

    /**
     *
     * @param from
     * @param to
     * @param modulo allow to sample data retreived taking 1 record out of
     * modulo.
     * @return
     */
    AnswerList<QueueStat> readByCriteria(Date from, Date to, int modulo);

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

}
