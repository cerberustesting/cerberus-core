/**
 * Cerberus Copyright (C) 2013 - 2017 cerberustesting
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
package org.cerberus.crud.dao;

import java.util.List;
import java.util.Map;
import org.cerberus.crud.entity.BatchInvariant;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;

/**
 * @author bdumont
 */
public interface IBatchInvariantDAO {

    public AnswerItem readByKey(String batch);

    public AnswerList readBySystemByCriteria(String system, int start, int amount, String column, String dir, String searchTerm, Map<String, List<String>> individualSearch);

    public Answer create(BatchInvariant object);

    public Answer delete(BatchInvariant object);

    public Answer update(BatchInvariant object);

    /**
     * 
     * @param system
     * @param searchParameter
     * @param individualSearch
     * @param columnName 
     */
    public AnswerList<List<String>> readDistinctValuesByCriteria(String system, String searchParameter, Map<String, List<String>> individualSearch, String columnName);
}
