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
package org.cerberus.crud.service;

import java.util.List;
import java.util.Map;

import org.cerberus.crud.entity.TestCaseExecutionData;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;

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
     */
    public AnswerItem readByKey(long id, String property, int index);

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
     */
    public AnswerList<TestCaseExecutionData> readByIdByCriteria(long id, int start, int amount, String column, String dir, String searchTerm, Map<String, List<String>> individualSearch);

    /**
     *
     * @param id
     * @return
     */
    public AnswerList<TestCaseExecutionData> readById(long id);

    /**
     *
     * @param id
     * @return
     */
    public AnswerList<TestCaseExecutionData> readByIdWithDependency(long id);

    /**
     *
     * @param id
     * @param property
     * @param index
     * @return
     */
    public boolean exist(long id, String property, int index);

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
     */
    public List<String> getPastValuesOfProperty(long id, String propName, String test, String testCase, String build, String environment, String country);

    /**
     *
     * @param id
     * @param propName
     * @param environment
     * @param country
     * @param timeoutInSecond
     * @return
     */
    public List<String> getInUseValuesOfProperty(long id, String propName, String environment, String country, Integer timeoutInSecond);

    /**
     *
     * @param object
     * @return
     */
    public Answer create(TestCaseExecutionData object);

    /**
     *
     * @param object
     * @return
     */
    public Answer delete(TestCaseExecutionData object);

    /**
     *
     * @param object
     * @return
     */
    public Answer update(TestCaseExecutionData object);

    /**
     *
     * @param answerItem
     * @return
     * @throws CerberusException
     */
    public TestCaseExecutionData convert(AnswerItem answerItem) throws CerberusException;

    /**
     *
     * @param answerList
     * @return
     * @throws CerberusException
     */
    public List<TestCaseExecutionData> convert(AnswerList answerList) throws CerberusException;

    /**
     *
     * @param answer
     * @throws CerberusException
     */
    public void convert(Answer answer) throws CerberusException;

    /**
     *
     * @param object
     * @return
     */
    public Answer save(TestCaseExecutionData object);

}
