/* DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.cerberus.crud.entity.Parameter;
import org.cerberus.crud.entity.ParameterSystem;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 22/03/2013
 * @since 2.0.0
 */
public interface IParameterDAO {

    Parameter findParameterByKey(String system, String key) throws CerberusException;

    List<Parameter> findAllParameter() throws CerberusException;

    void updateParameter(Parameter parameter) throws CerberusException;

    void insertParameter(Parameter parameter) throws CerberusException;

    List<Parameter> findAllParameterWithSystem1(String system, String system1) throws CerberusException;

    AnswerList readWithSystem1BySystemByCriteria(String system, String system1, int startPosition, int length, String columnName, String sort, String searchParameter, Map<String, List<String>> individualSearch);

    AnswerItem readWithSystem1ByKey(String system, String key, String system1);

    Parameter loadFromResultSetWithSystem1(ResultSet rs) throws SQLException;

    Parameter loadFromResultSet(ResultSet rs) throws SQLException;

    AnswerList<String> readDistinctValuesWithSystem1ByCriteria(String system, String system1, String searchParameter, Map<String, List<String>> individualSearch, String columnName);

    /**
     * Get the {@link Parameter} of the given key
     *
     * @param system the system of the {@link Parameter} to get
     * @param param  the param of the {@link Parameter} to get
     */
    AnswerItem readByKey(String system, String param);

    /**
     * @param object the {@link Parameter} to Create
     * @return {@link AnswerItem}
     */
    Answer create(Parameter object);

    /**
     * @param object the {@link Parameter} to Update
     * @return {@link AnswerItem}
     */
    Answer update(Parameter object);

    /**
     * @param object the {@link Parameter} to Delete
     * @return {@link AnswerItem}
     */
    Answer delete(Parameter object);
}
