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
import java.util.Date;
import java.util.List;

import org.cerberus.core.crud.entity.Application;
import org.cerberus.core.crud.entity.TestCase;
import org.cerberus.core.crud.entity.TestCaseExecutionHttpStat;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;
import org.json.JSONObject;

/**
 * Interface that defines the public methods to manage Application data on table
 * Insert, Delete, Update, Find
 *
 * @author tbernardes
 * @version 1.0, 15/10/13
 * @since 0.9.0
 */
public interface ITestCaseExecutionHttpStatDAO {

    /**
     *
     * @param object
     * @return
     */
    Answer create(TestCaseExecutionHttpStat object);

    /**
     *
     * @param exeId
     * @return
     */
    AnswerItem<TestCaseExecutionHttpStat> readByKey(long exeId);

    /**
     *
     * @param controlStatus
     * @param testcases
     * @param from
     * @param to
     * @param system
     * @param countries
     * @param environments
     * @param robotDecli
     * @return
     */
    AnswerList<TestCaseExecutionHttpStat> readByCriteria(String controlStatus, List<TestCase> testcases, Date from, Date to, List<String> system, List<String> countries, List<String> environments, List<String> robotDecli);

    /**
     *
     * @param controlStatus
     * @param testcases
     * @param from
     * @param to
     * @param system
     * @param countries
     * @param environments
     * @param robotDecli
     * @param parties
     * @param types
     * @param units
     * @return
     */
    AnswerItem<JSONObject> readByCriteria(String controlStatus, List<TestCase> testcases, Date from, Date to, List<String> system, List<String> countries, List<String> environments, List<String> robotDecli,
             List<String> parties, List<String> types, List<String> units);

    /**
     * Uses data of ResultSet to create object {@link Application}
     *
     * @param rs ResultSet relative to select from table Application
     * @return object {@link Application}
     * @throws SQLException when trying to get value from
     * {@link java.sql.ResultSet#getString(String)}
     * @see FactoryApplication
     */
    TestCaseExecutionHttpStat loadFromResultSet(ResultSet rs) throws SQLException;

}
