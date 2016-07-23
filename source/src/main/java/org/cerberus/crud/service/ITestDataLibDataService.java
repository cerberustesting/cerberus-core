/* DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
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

import java.util.ArrayList;
import java.util.List;
import org.cerberus.crud.entity.TestDataLib;
import org.cerberus.crud.entity.TestDataLibData;
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
     * @param columnEmpty Y will filter with empty string. N will filter not empty, null disable the filter.
     * @param parsingAnswerEmpty Y will filter with empty string. N will filter not empty, null disable the filter.
     * @return
     */
    AnswerList readByVarious(Integer testDataLibID, String columnEmpty, String parsingAnswerEmpty);

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
     * Creates several TestDataLibData entries
     *
     * @param completeSubDataList - entries to insert
     * @return
     */
    Answer create(List<TestDataLibData> completeSubDataList);

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
     * Deletes all sub-data entries that belong to a testdatalibrary entry
     *
     * @param testDataLib
     * @return
     */
    Answer delete(TestDataLib testDataLib);

    /**
     * Method that performs a CUD of operations in one set of testdatalibrary
     * entries
     *
     * @param entriesToInsert
     * @param entriesToUpdate
     * @param entriesToRemove
     * @return
     */
    Answer createUpdateDelete(ArrayList<TestDataLibData> entriesToInsert, ArrayList<TestDataLibData> entriesToUpdate, ArrayList<TestDataLibData> entriesToRemove);

    /**
     *
     * @param subDataList
     * @return
     */
    Answer validate(List<TestDataLibData> subDataList);

}
