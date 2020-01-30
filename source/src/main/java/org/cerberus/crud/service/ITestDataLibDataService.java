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
import org.cerberus.crud.entity.TestDataLibData;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;

/**
 *
 * @author bcivel
 * @author FNogueira
 */
public interface ITestDataLibDataService {

    /**
     *
     * @param testDataLibID
     * @param subData
     * @return
     */
    AnswerItem readByKey(Integer testDataLibID, String subData);

    /**
     * Returns the subdata entry that matches a specific technical key.
     *
     * @param testDataLibDataID
     * @return
     */
    AnswerItem readByKeyTech(Integer testDataLibDataID);

    /**
     * Reads a list with basis on the test data library id
     *
     * @param testDataLibID
     * @param columnEmpty Y will filter with empty string. N will filter not
     * empty, null disable the filter.
     * @param parsingAnswerEmpty Y will filter with empty string. N will filter
     * not empty, null disable the filter.
     * @param columnPositionEmpty Y will filter with empty string. N will filter
     * not empty, null disable the filter.
     * @return
     */
    AnswerList<TestDataLibData> readByVarious(Integer testDataLibID, String columnEmpty, String parsingAnswerEmpty, String columnPositionEmpty);

    /**
     *
     * @return All TestDataLibData
     */
    AnswerList readAll();

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
    AnswerList readByCriteria(int start, int amount, String colName, String dir, String searchTerm, String individualSearch);

    /**
     * Reads all test data library entries that match a specific name.
     *
     * @param testDataLibName
     * @return
     */
    AnswerList readByName(String testDataLibName);

    /**
     *
     * @param testDataLibData TestDataLib to insert
     * @return
     */
    Answer create(TestDataLibData testDataLibData);

    /**
     *
     * @param testDataLibData TestData to update using the key
     * @return
     */
    Answer update(TestDataLibData testDataLibData);

    /**
     *
     * @param testDataLibData
     * @return
     */
    Answer delete(TestDataLibData testDataLibData);

    /**
     *
     * @param objectList
     * @return
     */
    public Answer createList(List<TestDataLibData> objectList);

    /**
     *
     * @param objectList
     * @return
     */
    public Answer deleteList(List<TestDataLibData> objectList);

    /**
     * Update all TestDataLibData from the sourceList to the perimeter of the
     * testDataLibId list. All existing TestDataLibData from newList will be
     * updated, the new ones added and missing ones deleted.
     *
     * @param testDataLibId
     * @param newList
     * @return
     */
    Answer compareListAndUpdateInsertDeleteElements(Integer testDataLibId, List<TestDataLibData> newList);

    /**
     *
     * @param answerItem
     * @return
     * @throws CerberusException
     */
    TestDataLibData convert(AnswerItem<TestDataLibData> answerItem) throws CerberusException;

    /**
     *
     * @param answerList
     * @return
     * @throws CerberusException
     */
    List<TestDataLibData> convert(AnswerList<TestDataLibData> answerList) throws CerberusException;

    /**
     *
     * @param answer
     * @throws CerberusException
     */
    void convert(Answer answer) throws CerberusException;

}
