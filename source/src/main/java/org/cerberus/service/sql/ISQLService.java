/*
 * Cerberus  Copyright (C) 2013  vertigo17
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
package org.cerberus.service.sql;

import java.util.HashMap;
import java.util.List;
import org.cerberus.crud.entity.MessageEvent;
import org.cerberus.crud.entity.TestCaseCountryProperties;
import org.cerberus.crud.entity.TestCaseExecution;
import org.cerberus.crud.entity.TestCaseExecutionData;
import org.cerberus.exception.CerberusEventException;
import org.cerberus.util.answer.AnswerItem;

/**
 *
 * @author bcivel
 */
public interface ISQLService {

    /**
     *
     * @param testCaseExecutionData
     * @param testCaseProperties
     * @param tCExecution
     * @return
     */
    TestCaseExecutionData calculateOnDatabase(TestCaseExecutionData testCaseExecutionData, TestCaseCountryProperties testCaseProperties, TestCaseExecution tCExecution);

    /**
     * Performs an SQL query that retrieves information from several columns.
     *
     * @param sql - SQL query to be executed
     * @param db - database
     * @param system - system where the query should be performed
     * @param country - country where the query should be performed
     * @param environment - environment where the query should be performed
     * @param testCaseCountryProperty
     * @param keyColumn - string with the column that contain the key (used for
     * some propertyNature).
     * @param tCExecution - Execution context is required for RANDOMNEW and
     * NOTINUSE Natures (that requires test, testcase, build, environment,
     * etc,...)
     * @param dataLibID ID of the dataLib beeing calculated. This is used to get
     * the detail list of columns to retreive.
     * @return a row with several columns
     */
    AnswerItem<HashMap<String, String>> calculateOnDatabaseNColumns(String sql, String db, String system, String country, String environment, TestCaseCountryProperties testCaseCountryProperty, String keyColumn, TestCaseExecution tCExecution, Integer dataLibID);

    /**
     * Performs a query in the database
     *
     * @param connectionName
     * @param sql
     * @param limit
     * @param defaultTimeOut
     * @return
     * @throws CerberusEventException
     */
    List<String> queryDatabase(String connectionName, String sql, int limit, int defaultTimeOut) throws CerberusEventException;

    /**
     * @param list List of String in which it will take a value randomly
     * @return A Random String from a List of String
     */
    String getRandomStringFromList(List<String> list);

    /**
     *
     * @param resultset
     * @param pastValues
     * @return
     */
    String getRandomNewStringFromList(List<String> resultset, List<String> pastValues);

    /**
     *
     * @param resultSet
     * @param valuesInUse
     * @return
     */
    String getRandomStringNotInUse(List<String> resultSet, List<String> valuesInUse);

    /**
     *
     * @param system
     * @param country
     * @param environment
     * @param db
     * @param sql
     * @param defaultTimeOut
     * @return
     */
    MessageEvent executeUpdate(String system, String country, String environment, String db, String sql, int defaultTimeOut);

    /**
     *
     * @param system
     * @param country
     * @param environment
     * @param db
     * @param sql
     * @return
     */
    MessageEvent executeCallableStatement(String system, String country, String environment, String db, String sql);
}
