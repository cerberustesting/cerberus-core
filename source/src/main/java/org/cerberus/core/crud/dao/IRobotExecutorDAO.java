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
import org.cerberus.core.crud.entity.AppServiceContent;
import org.cerberus.core.crud.entity.RobotExecutor;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;

/**
 * Interface that defines the public methods to manage Application data on table
 * Insert, Delete, Update, Find
 *
 * @author tbernardes
 * @version 1.0, 15/10/13
 * @since 0.9.0
 */
public interface IRobotExecutorDAO {

    /**
     *
     * @param robot
     * @param executor
     * @return
     */
    AnswerItem<RobotExecutor> readByKey(String robot, String executor);

    /**
     *
     * @param robot
     * @return
     * @throws org.cerberus.core.exception.CerberusException
     */
    List<RobotExecutor> readBestByKey(String robot) throws CerberusException;

    /**
     *
     * @param robot
     * @param active
     * @param startPosition
     * @param length
     * @param columnName
     * @param sort
     * @param searchParameter
     * @param individualSearch
     * @return
     */
    AnswerList<RobotExecutor> readByVariousByCriteria(List<String> robot, String active, int startPosition, int length, String columnName, String sort, String searchParameter, Map<String, List<String>> individualSearch);

    /**
     *
     * @param object
     * @return
     */
    Answer create(RobotExecutor object);

    /**
     *
     * @param object
     * @return
     */
    Answer delete(RobotExecutor object);

    /**
     *
     * @param robot
     * @param executor
     * @param object
     * @return
     */
    Answer update(String robot, String executor, RobotExecutor object);

    /**
     *
     * @param robot
     * @param executor
     * @return
     */
    Answer updateLastExe(String robot, String executor);

    /**
     * Uses data of ResultSet to create object {@link AppServiceContent}
     *
     * @param rs ResultSet relative to select from table Application
     * @return object {@link AppServiceContent}
     * @throws SQLException when trying to get value from
     * {@link java.sql.ResultSet#getString(String)}
     * @see FactoryApplication
     */
    RobotExecutor loadFromResultSet(ResultSet rs) throws SQLException;

    /**
     *
     * @param robot
     * @param searchParameter
     * @param individualSearch
     * @param columnName
     * @return
     */
    public AnswerList<String> readDistinctValuesByCriteria(String robot, String searchParameter, Map<String, List<String>> individualSearch, String columnName);

}
