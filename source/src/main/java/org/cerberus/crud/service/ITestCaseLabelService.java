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
package org.cerberus.crud.service;

import java.util.List;
import java.util.Map;

import org.cerberus.crud.entity.TestCaseLabel;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;

/**
 *
 * @author bcivel
 */
public interface ITestCaseLabelService {

    /**
     *
     * @param id
     * @return
     */
    AnswerItem readByKey(Integer id);

    /**
     *
     * @return
     */
    AnswerList readAll();

    /**
     *
     * @param startPosition
     * @param length
     * @param columnName
     * @param sort
     * @param searchParameter
     * @param individualSearch
     * @return
     */
    AnswerList readByCriteria(int startPosition, int length, String columnName, String sort, String searchParameter, Map<String, List<String>> individualSearch);

    /**
     *
     * @param test
     * @param testCase
     * @return
     */
    AnswerList readByTestTestCase(String test, String testCase);

    /**
     *
     * @param id
     * @return true is label exist or false is label does not exist in database.
     */
    boolean exist(Integer id);

    /**
     *
     * @param object
     * @return
     */
    Answer create(TestCaseLabel object);

    /**
     *
     * @param objectList
     * @return
     */
    Answer createList(List<TestCaseLabel> objectList);

    /**
     *
     * @param object
     * @return
     */
    Answer delete(TestCaseLabel object);

    /**
     *
     * @param objectList
     * @return
     */
    Answer deleteList(List<TestCaseLabel> objectList);

    /**
     *
     * @param object
     * @return
     */
    Answer update(TestCaseLabel object);

    /**
     *
     * @param answerItem
     * @return
     * @throws CerberusException
     */
    TestCaseLabel convert(AnswerItem answerItem) throws CerberusException;

    /**
     *
     * @param answerList
     * @return
     * @throws CerberusException
     */
    List<TestCaseLabel> convert(AnswerList answerList) throws CerberusException;

    /**
     *
     * @param answer
     * @throws CerberusException
     */
    void convert(Answer answer) throws CerberusException;

    /**
     *
     * @param test
     * @param testCase
     * @param newList
     * @return 
     */
    Answer compareListAndUpdateInsertDeleteElements(String test, String testCase, List<TestCaseLabel> newList);

    Answer duplicateList(List<TestCaseLabel> dataList, String test, String testCase);
}
