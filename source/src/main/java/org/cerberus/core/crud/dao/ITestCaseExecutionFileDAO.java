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

import org.cerberus.core.crud.entity.TestCaseExecutionFile;
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
public interface ITestCaseExecutionFileDAO {

    /**
     *
     * @param id
     * @return
     */
    AnswerItem<TestCaseExecutionFile> readByKey(long id);

    /**
     *
     * @param exeId
     * @param fileDesc
     * @param level
     * @return
     */
    AnswerItem<TestCaseExecutionFile> readByKey(long exeId, String level, String fileDesc);

    /**
     *
     * @param ExeId
     * @param level
     * @param start
     * @param amount
     * @param column
     * @param dir
     * @param searchTerm
     * @param individualSearch
     * @return
     */
    AnswerList<TestCaseExecutionFile> readByVariousByCriteria(long ExeId, String level, int start, int amount, String column, String dir, String searchTerm, Map<String, List<String>> individualSearch);

    /**
     * Return all File correponding to fileDesc
     * @param exeId
     * @param fileDesc
     * @return
     */
    List<TestCaseExecutionFile> getListByFileDesc(long exeId, String fileDesc) throws CerberusException;


    /**
     *
     * @param object
     * @return
     */
    Answer create(TestCaseExecutionFile object);

    /**
     *
     * @param object
     * @return
     */
    Answer delete(TestCaseExecutionFile object);

    /**
     *
     * @param object
     * @return
     */
    Answer update(TestCaseExecutionFile object);

    /**
     * Uses data of ResultSet to create object {@link Application}
     *
     * @param rs ResultSet relative to select from table Application
     * @return object {@link Application}
     * @throws SQLException when trying to get value from
     * {@link java.sql.ResultSet#getString(String)}
     * @see FactoryApplication
     */
    TestCaseExecutionFile loadFromResultSet(ResultSet rs) throws SQLException;

}
