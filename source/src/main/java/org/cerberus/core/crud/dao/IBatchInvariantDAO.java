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

import java.util.List;
import java.util.Map;
import org.cerberus.core.crud.entity.BatchInvariant;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;

/**
 * @author bdumont
 */
public interface IBatchInvariantDAO {

    /**
     *
     * @param batch
     * @return
     */
    public AnswerItem<BatchInvariant> readByKey(String batch);

    /**
     *
     * @param system
     * @param start
     * @param amount
     * @param column
     * @param dir
     * @param searchTerm
     * @param individualSearch
     * @return
     */
    public AnswerList<BatchInvariant> readBySystemByCriteria(List<String> system, int start, int amount, String column, String dir, String searchTerm, Map<String, List<String>> individualSearch);

    /**
     *
     * @param object
     * @return
     */
    public Answer create(BatchInvariant object);

    /**
     *
     * @param object
     * @return
     */
    public Answer delete(BatchInvariant object);

    /**
     *
     * @param batch
     * @param object
     * @return
     */
    public Answer update(String batch, BatchInvariant object);

    /**
     * 
     * @param system
     * @param searchParameter
     * @param individualSearch
     * @param columnName 
     * @return  
     */
    public AnswerList<String> readDistinctValuesByCriteria(List<String> system, String searchParameter, Map<String, List<String>> individualSearch, String columnName);
}
