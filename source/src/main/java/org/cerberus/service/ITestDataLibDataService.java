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
package org.cerberus.service;

import java.util.ArrayList;
import java.util.List; 
import org.cerberus.entity.TestDataLibData;
import org.cerberus.entity.TestDataLibResult;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;

/**
 *
 * @author bcivel
 */
public interface ITestDataLibDataService {

    /**
     *
     * @param testDataLibData TestDataLib to insert
     * @throws CerberusException
     */
    void createTestDataLibData(TestDataLibData testDataLibData) throws CerberusException;

    /**
     *
     * @param testDataLibData TestData to update using the key
     * @throws CerberusException
     */
    void updateTestDataLibData(TestDataLibData testDataLibData) throws CerberusException;

    /**
     *
     * @param testDataLibData
     * @throws CerberusException
     */
    void deleteTestDataLibData(TestDataLibData testDataLibData) throws CerberusException;

    /**
     *
     * @param testDataLibID
     * @param subData
     * @return
     * @throws org.cerberus.exception.CerberusException
     */
    TestDataLibData findTestDataLibDataByKey(Integer testDataLibID, String subData) throws CerberusException;

    /**
     *
     * @return All TestDataLibData
     */
    List<TestDataLibData> findAllTestDataLibData();

    /**
     *
     * @param testDataLibID
     * @return
     */
    AnswerList findTestDataLibDataListByTestDataLib(Integer testDataLibID);

    /**
     *
     * @param testDataLibID
     * @return
     */
    List<TestDataLibData> findTestDataLibDataByCriteria(Integer testDataLibID, String subData, String value, String column, String parsingAnswer, String description) throws CerberusException;
    /**
     * Creates several TestDataLibData entries
     * @param subdataSet - entries to insert
     * @throws CerberusException 
     */
    public void createTestDataLibDataBatch(List<TestDataLibData> subdataSet) throws CerberusException;

    public AnswerItem<String> fetchSubData(TestDataLibResult result, TestDataLibData subDataEntry);

    public void deleteByTestDataLibID(int testDataLibID) throws CerberusException;


    /**
     *
     * @param testDataLibID
     * @param entriesToInsert
     * @param entriesToUpdate
     * @param entriesToRemove
     * @return
     */
    public Answer cudTestDataLibData(int testDataLibID, ArrayList<TestDataLibData> entriesToInsert, ArrayList<TestDataLibData> entriesToUpdate, ArrayList<String> entriesToRemove);

    /**
     *
     * @param testDataLibName
     * @return
     */
    public AnswerList findTestDataLibDataByName(String testDataLibName); 

    /**
     *
     * @param testDataLib
     * @param nameToSearch
     * @param limit
     * @return
     */
    public AnswerList findTestDataLibSubData(String testDataLib, String nameToSearch, int limit);
    
}
