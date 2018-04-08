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
import org.cerberus.crud.entity.Test;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 07/01/2013
 * @since 2.0.0
 */
public interface ITestService {

    /**
     * @return List of Tests name
     */
    List<String> getListOfTests();

    /**
     * @return List of Test
     */
    List<Test> getListOfTest();



    /**
     *
     * @param test Key of the table Test
     * @return Test object
     */
    Test findTestByKey(String test);

    List<Test> findTestBySystems(List<String> systems);

    public AnswerItem readByKey(String test);

    public AnswerList readDistinctBySystem(String system);
    
    public AnswerList readByCriteria(int start, int amount, String colName, String dir, String searchTerm, Map<String, List<String>> individualSearch);

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
     * @param searchTerm
     * @param individualSearch
     * @param columnName
     * @return 
     */
    AnswerList<List<String>> readDistinctValuesByCriteria(String searchTerm, Map<String, List<String>> individualSearch, String columnName);
}
