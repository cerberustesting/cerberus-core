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

import org.cerberus.crud.entity.Application;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;

/**
 * Interface that defines the public methods to manage Application data on table
 * Insert, Delete, Update, Find
 *
 * @author tbernardes
 * @version 1.0, 15/10/13
 * @since 0.9.0
 */
public interface IApplicationDAO {

    /**
     *
     * @param application
     * @return
     */
    AnswerItem readByKey(String application);

    /**
     *
     * @param system
     * @param startPosition
     * @param length
     * @param columnName
     * @param sort
     * @param searchParameter
     * @param string
     * @return
     */
    AnswerList readBySystemByCriteria(String system, int startPosition, int length, String columnName, String sort, String searchParameter, String string);

    /**
     *
     * @param system
     * @return
     */
    AnswerItem readTestCaseCountersBySystemByStatus(String system);

    /**
     *
     * @param application
     * @return
     */
    Answer create(Application application);

    /**
     *
     * @param application
     * @return
     */
    Answer delete(Application application);

    /**
     *
     * @param application
     * @return
     */
    Answer update(Application application);

    /**
     *
     * @return 
     */
    AnswerList readDistinctSystem();

    /**
     * Uses data of ResultSet to create object {@link Application}
     *
     * @param rs ResultSet relative to select from table Application
     * @return object {@link Application}
     * @throws SQLException when trying to get value from
     * {@link java.sql.ResultSet#getString(String)}
     * @see FactoryApplication
     */
    Application loadFromResultSet(ResultSet rs) throws SQLException;

}
