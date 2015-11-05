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
import org.cerberus.crud.entity.TestDataLibData;
import org.cerberus.dto.TestDataLibDataUpdateDTO;
import org.cerberus.service.engine.testdata.TestDataLibResult;
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
     * @param testDataLibData TestDataLib to insert
     */
    Answer create(TestDataLibData testDataLibData);

    /**
     *
     * @param testDataLibData TestData to update using the key
     */
    Answer update(TestDataLibData testDataLibData);

    /**
     *
     * @param testDataLibData
     */
    Answer delete(TestDataLibData testDataLibData);

    /**
     *
     * @param testDataLibID
     * @param subData
     * @return
     */
    AnswerItem<TestDataLibData> readByKey(Integer testDataLibID, String subData);

    /**
     *
     * @return All TestDataLibData
     */
    AnswerList<TestDataLibData> readAll();

    /**
     *
     * @param testDataLibID
     * @return
     */
    AnswerList<TestDataLibData> readById(Integer testDataLibID);

    /**
     *
     * @param testDataLibID
     * @param subData
     * @param value
     * @param column
     * @param parsingAnswer
     * @param description
     * @return
     */
    AnswerList<TestDataLibData> readByCriteria(Integer testDataLibID, String subData, String value, String column, String parsingAnswer, String description);
    /**
     * Creates several TestDataLibData entries
     * @param subdataSet - entries to insert
     * @throws CerberusException 
     */
    Answer createBatch(List<TestDataLibData> subdataSet) throws CerberusException;

    AnswerItem<String> fetchSubData(TestDataLibResult result, TestDataLibData subDataEntry);

    Answer delete(int testDataLibID);


    /**
     *
     * @param testDataLibID
     * @param entriesToInsert
     * @param entriesToUpdate
     * @param entriesToRemove
     * @return
     */
    Answer createUpdateDelete(int testDataLibID, ArrayList<TestDataLibData> entriesToInsert, ArrayList<TestDataLibDataUpdateDTO> entriesToUpdate, ArrayList<String> entriesToRemove);

    /**
     *
     * @param testDataLibName
     * @return
     */
    AnswerList readByName(String testDataLibName); 

    /**
     *
     * @param testDataLib
     * @param nameToSearch
     * @param limit
     * @return
     */
    AnswerList readByIdByName(String testDataLib, String nameToSearch, int limit);
    
}
