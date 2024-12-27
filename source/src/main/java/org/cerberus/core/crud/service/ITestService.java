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

import java.util.List;
import java.util.Map;
import org.cerberus.core.crud.entity.Test;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 07/01/2013
 * @since 2.0.0
 */
public interface ITestService {

    /**
     *
     * @param test
     * @return
     */
    public AnswerItem<Test> readByKey(String test);

    /**
     *
     * @param system
     * @return
     */
    public AnswerList<Test> readDistinctBySystem(String system);

    /**
     *
     * @param start
     * @param amount
     * @param colName
     * @param dir
     * @param searchTerm
     * @param individualSearch
     * @return
     */
    public AnswerList<Test> readByCriteria(int start, int amount, String colName, String dir, String searchTerm, Map<String, List<String>> individualSearch);

    /**
     *
     * @param Object
     * @return
     */
    public boolean exist(String Object);
    
    /**
     *
     * @param test
     * @return
     */
    public Answer create(Test test);

    /**
     *
     * @param keyTest
     * @param test
     * @return
     */
    public Answer update(String keyTest, Test test);

    /**
     *
     * @param test
     * @return
     */
    public Answer delete(Test test);

    /**
     *
     * @param answerItem
     * @return
     * @throws CerberusException
     */
    Test convert(AnswerItem<Test> answerItem) throws CerberusException;

    /**
     *
     * @param answerList
     * @return
     * @throws CerberusException
     */
    List<Test> convert(AnswerList<Test> answerList) throws CerberusException;

    /**
     *
     * @param answer
     * @throws CerberusException
     */
    void convert(Answer answer) throws CerberusException;

    /**
     * Read distinct Value of specified column
     *
     * @param searchTerm
     * @param individualSearch
     * @param columnName
     * @return
     */
    AnswerList<String> readDistinctValuesByCriteria(String searchTerm, Map<String, List<String>> individualSearch, String columnName);
    
    /**
     * Delete test if no testCase attached to the test
     * @param test
     * @return Answer
     */
    public Answer deleteIfNotUsed(String test);
    
    /**
     * Upfdate test if exists
     * @param originalTest : The test name of the object to update
     * @param test : The testObject to use for the update
     * @return Answer
     */
    public Answer updateIfExists(String originalTest, Test test);
}
