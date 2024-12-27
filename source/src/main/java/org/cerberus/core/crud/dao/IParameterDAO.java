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

import org.cerberus.core.crud.entity.Parameter;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 22/03/2013
 * @since 2.0.0
 */
public interface IParameterDAO {

    /**
     *
     * @param system
     * @param key
     * @return
     * @throws CerberusException
     */
    Parameter findParameterByKey(String system, String key) throws CerberusException;

    /**
     *
     * @return @throws CerberusException
     */
    List<Parameter> findAllParameter() throws CerberusException;

    /**
     *
     * @param system
     * @param system1
     * @return
     * @throws CerberusException
     */
    List<Parameter> findAllParameterWithSystem1(String system, String system1) throws CerberusException;

    /**
     *
     * @param system
     * @param system1
     * @param startPosition
     * @param length
     * @param columnName
     * @param sort
     * @param searchParameter
     * @param individualSearch
     * @return
     */
    AnswerList<Parameter> readWithSystem1BySystemByCriteria(String system, String system1, int startPosition, int length, String columnName, String sort, String searchParameter, Map<String, List<String>> individualSearch);

    /**
     *
     * @param system
     * @param key
     * @param system1
     * @return
     */
    AnswerItem<Parameter> readWithSystem1ByKey(String system, String key, String system1);

    /**
     *
     * @param rs
     * @return
     * @throws SQLException
     */
    Parameter loadFromResultSetWithSystem1(ResultSet rs) throws SQLException;

    /**
     *
     * @param rs
     * @return
     * @throws SQLException
     */
    Parameter loadFromResultSet(ResultSet rs) throws SQLException;

    /**
     *
     * @param system
     * @param system1
     * @param searchParameter
     * @param individualSearch
     * @param columnName
     * @return
     */
    AnswerList<String> readDistinctValuesWithSystem1ByCriteria(String system, String system1, String searchParameter, Map<String, List<String>> individualSearch, String columnName);

    /**
     * Get the {@link Parameter} of the given key
     *
     * @param system the system of the {@link Parameter} to get
     * @param param the param of the {@link Parameter} to get
     * @return
     */
    AnswerItem<Parameter> readByKey(String system, String param);

    /**
     * @param object the {@link Parameter} to Update
     * @return {@link AnswerItem}
     */
    Answer update(Parameter object);

    /**
     *
     * @param parameterKey
     * @param system
     * @param value
     * @return
     */
    Answer setParameter(String parameterKey, String system, String value);

    /**
     *
     * @param object
     * @return
     */
    Answer create(Parameter object);
}
