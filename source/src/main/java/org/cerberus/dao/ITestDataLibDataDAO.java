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
package org.cerberus.dao;

import java.util.ArrayList;
import java.util.List;
import org.cerberus.entity.TestDataLibData;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerList;

/**
 *
 * @author vertigo17
 */
public interface ITestDataLibDataDAO {

    /**
     *
     * @param testDataLibData
     * @throws CerberusException
     */
    void createTestDataLibData(TestDataLibData testDataLibData) throws CerberusException;

    /**
     *
     * @param testDataLibData
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
     * @return All TestData
     */
    List<TestDataLibData> findAllTestDataLibData();

    /**
     *
     * @param testDataLibID resultSet
     * @return
     */
    AnswerList findTestDataLibDataListByTestDataLib(Integer testDataLibID);

    List<TestDataLibData> findTestDataLibDataByCriteria(Integer testDataLibID, String subData, String value, String column, String parsingAnswer, String description) throws CerberusException;

    
    /**
     * Deletes all testdatalibdata records that belong to a testdatalib.
     * @param testDataLibID - testdatalibid used to filter the rows that will be removed.
     * @return Answer indicating the status of the operation
     * @throws CerberusException 
     */
    public Answer deleteByTestDataLibID(int testDataLibID) throws CerberusException;
 
    /**
     * Batch that inserts several records in the table TestDataLibData
     * @param subdataSet - entries to insert
     * @return Answer indicating the status of the operation
     * @throws CerberusException 
     */
    public Answer createTestDataLibDataBatch(List<TestDataLibData> subdataSet) throws CerberusException; 
    /**
     * Batch that updates several records in the table TestDataLibData.
     * @param entriesToUpdate - entries to update
     * @return  Answer indicating the status of the operation
     */
    public Answer updateTestDataLibDataBatch(ArrayList<TestDataLibData> entriesToUpdate);
    /**
     * Batch that deletes several records in the table TestDataLibData.
     * @param testDataLibIdForData - testdatalibID associated with the entries that will be removed. This is part of the PK.
     * @param entriesToRemove - subdata names for the records that should be removed
     * @return  Answer indicating the status of the operation
     */
    public Answer deleteTestDataLibDataBatch(int testDataLibIdForData, ArrayList<String> entriesToRemove);
}
