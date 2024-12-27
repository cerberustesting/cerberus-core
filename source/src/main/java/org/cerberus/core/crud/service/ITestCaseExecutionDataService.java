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
package org.cerberus.core.crud.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.crud.entity.TestCaseExecutionData;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;

/**
 *
 * @author bcivel
 */
public interface ITestCaseExecutionDataService {

    /**
     *
     * @param id
     * @param property
     * @param index
     * @return
     * @throws org.cerberus.core.exception.CerberusException
     */
    TestCaseExecutionData readByKey(long id, String property, int index) throws CerberusException;

    /**
     *
     * @param id
     * @param start
     * @param amount
     * @param column
     * @param dir
     * @param searchTerm
     * @param individualSearch
     * @return
     * @throws org.cerberus.core.exception.CerberusException
     */
    List<TestCaseExecutionData> readByIdByCriteria(long id, int start, int amount, String column, String dir, String searchTerm, Map<String, List<String>> individualSearch) throws CerberusException;

    /**
     *
     * @param system
     * @param environment
     * @param country
     * @param property
     * @param cacheExpire
     * @return
     * @throws org.cerberus.core.exception.CerberusException
     */
    TestCaseExecutionData readLastCacheEntry(String system, String environment, String country, String property, int cacheExpire) throws CerberusException;

    /**
     *
     * @param id
     * @return
     * @throws org.cerberus.core.exception.CerberusException
     */
    List<TestCaseExecutionData> readById(long id) throws CerberusException;

    /**
     *
     * @param id
     * @return
     * @throws org.cerberus.core.exception.CerberusException
     */
    List<TestCaseExecutionData> readByIdWithDependency(long id) throws CerberusException;

    /**
     *
     * @param id
     * @param property
     * @param index
     * @return
     * @throws org.cerberus.core.exception.CerberusException
     */
    boolean exist(long id, String property, int index) throws CerberusException;

    /**
     *
     * @param id
     * @param propName
     * @param test
     * @param testCase
     * @param build
     * @param environment
     * @param country
     * @return
     * @throws org.cerberus.core.exception.CerberusException
     */
    List<String> getPastValuesOfProperty(long id, String propName, String test, String testCase, String build, String environment, String country) throws CerberusException;

    /**
     *
     * @param id
     * @param propName
     * @param environment
     * @param country
     * @param timeoutInSecond
     * @return
     * @throws org.cerberus.core.exception.CerberusException
     */
    List<String> getInUseValuesOfProperty(long id, String propName, String environment, String country, Integer timeoutInSecond) throws CerberusException;

    /**
     *
     * @param object
     * @param secrets
     * @throws org.cerberus.core.exception.CerberusException
     */
    void create(TestCaseExecutionData object, HashMap<String, String> secrets) throws CerberusException;

    /**
     *
     * @param object
     * @throws org.cerberus.core.exception.CerberusException
     */
    void delete(TestCaseExecutionData object) throws CerberusException;

    /**
     *
     * @param object
     * @param secrets
     * @throws org.cerberus.core.exception.CerberusException
     */
    public void update(TestCaseExecutionData object, HashMap<String, String> secrets) throws CerberusException;

    /**
     *
     * @param answerItem
     * @return
     * @throws CerberusException
     */
    TestCaseExecutionData convert(AnswerItem answerItem) throws CerberusException;

    /**
     *
     * @param answerList
     * @return
     * @throws CerberusException
     */
    List<TestCaseExecutionData> convert(AnswerList<TestCaseExecutionData> answerList) throws CerberusException;

    /**
     *
     * @param answer
     * @throws CerberusException
     */
    void convert(Answer answer) throws CerberusException;

    /**
     *
     * @param object
     * @param secrets
     * @throws org.cerberus.core.exception.CerberusException
     */
    void save(TestCaseExecutionData object, HashMap<String, String> secrets) throws CerberusException;

    /**
     * Load All ExecutionData of testcases that this execution depends
     *
     * @param testCaseExecution
     * @throws org.cerberus.core.exception.CerberusException
     */
    void loadTestCaseExecutionDataFromDependencies(TestCaseExecution testCaseExecution) throws CerberusException;
}
