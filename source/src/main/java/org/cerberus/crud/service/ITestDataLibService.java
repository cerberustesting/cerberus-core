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

import java.util.HashMap;
import java.util.List;
import org.cerberus.crud.entity.TestDataLib;
import org.cerberus.crud.entity.TestDataLibData; 
import org.cerberus.exception.CerberusException;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;

/**
 *
 * @author bcivel
 */
public interface ITestDataLibService {

    /**
     *
     * @param testDataLib TestDataLib to insert
     * @throws CerberusException
     */
    void createTestDataLib(TestDataLib testDataLib) throws CerberusException;

    /**
     *
     * @param testDataLib TestData to update using the key
     * @return
     */
    Answer updateTestDataLib(TestDataLib testDataLib);

    /**
     *
     * @param testDataLib
     * @throws CerberusException
     */
    void deleteTestDataLib(TestDataLib testDataLib) throws CerberusException;

    /**
     *
     * @return All TestDataLib
     */
    List<TestDataLib> findAllTestDataLib();

    /**
     *
     * @param start first row of the resultSet
     * @param amount number of row of the resultSet
     * @param column order the resultSet by this column
     * @param dir Asc or desc, information for the order by command
     * @param searchTerm search term on all the column of the resultSet
     * @param individualSearch search term on a dedicated column of the
     * resultSet
     * @return
     */
    AnswerList findTestDataLibListByCriteria(int start, int amount, String column, String dir, String searchTerm, String individualSearch);
    /**
     * 
     * @param name
     * @param system
     * @param environment
     * @param country
     * @return 
     * @throws org.cerberus.exception.CerberusException 
     */
    AnswerItem findTestDataLibByKey(String name, String system, String environment, String country) throws CerberusException;
    
    AnswerItem findTestDataLibByKey(int testDatalib);
    /**
     * 
     * @param searchTerm words to be searched in every column (Exemple : article)
     * @param inds part of the script to add to where clause (Exemple : `type` = 'Article')
     * @return The number of records for these criterias
     */
    Integer getNumberOfTestDataLibPerCriteria(String searchTerm, String inds);
    /**
     * Auxiliary method that retrieves all the group names that were already defined for a type.
     * @param type STATIC or SQL or SOAP
     * @return list of group values for the type
     */
    AnswerList<String> getListOfGroupsPerType(String type);
    /**
     * Gets the rawData associated to the library; for SQL and SOAP, the corresponding instructions will be executed in order to retrieve the data 
     * from the database and webservice.
     * @param lib testdatalib entry
     * @param rowLimit
     * @param propertyName
     * @return the result data for the library entry
     */
    AnswerItem fetchData(TestDataLib lib, int rowLimit, String propertyName);

    /**
     * Deletes a testdatalib with basis on the id.
     * @param testDataLibID - id of the entry that we want to remove
     * @return an answer indicating the status of the operation
     */
    Answer deleteTestDataLib(int testDataLibID);

    /**
     * Creates the test data lib entries for the com
     * @param testDataLibList combinations of entries that 
     * @param subDataList subdata entries that were defined in the add window, which are associated with each entry in the list testDataLibList
     * @return an answer indicating the status of the operation
     */
    Answer createTestDataLibBatch(List<TestDataLib> testDataLibList, List<TestDataLibData> subDataList);
    Answer createTestDataLib(TestDataLib testDataLib, List<TestDataLibData> subDataList);
    Answer createTestDataLibBatch(List<TestDataLib> entries)throws CerberusException;
    Answer createTestDataLibBatch(HashMap<TestDataLib, List<TestDataLibData>> entries);
    
    AnswerList findTestDataLibNameList(String testDataLibName, int limit);
}
