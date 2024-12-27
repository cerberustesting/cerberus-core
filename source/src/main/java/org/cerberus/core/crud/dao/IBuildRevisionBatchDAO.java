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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import org.cerberus.core.crud.entity.BuildRevisionBatch;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;

/**
 *
 * @author bcivel
 */
public interface IBuildRevisionBatchDAO {

    /**
     *
     * @param id
     * @return
     */
    AnswerItem readByKey(long id);

    /**
     *
     * @param system
     * @param country
     * @param environment
     * @param startPosition
     * @param length
     * @param columnName
     * @param sort
     * @param searchParameter
     * @param string
     * @return
     */
    AnswerList<BuildRevisionBatch> readByVariousByCriteria(String system, String country, String environment, int startPosition, int length, String columnName, String sort, String searchParameter, String string);

    /**
     *
     * @param buildRevisionBatch
     * @return
     */
    Answer create(BuildRevisionBatch buildRevisionBatch);

    /**
     *
     * @param buildRevisionBatch
     * @return
     */
    Answer delete(BuildRevisionBatch buildRevisionBatch);

    /**
     *
     * @param buildRevisionBatch
     * @return
     */
    Answer update(BuildRevisionBatch buildRevisionBatch);

    /**
     *
     * @param resultSet
     * @return
     * @throws SQLException
     */
    BuildRevisionBatch loadFromResultSet(ResultSet resultSet) throws SQLException;

    /**
     * 
     * @param system
     * @param searchParameter
     * @param individualSearch
     * @param columnName
     * @return 
     */
    public AnswerList<String> readDistinctValuesByCriteria(String system, String searchParameter, Map<String, List<String>> individualSearch, String columnName);
}
