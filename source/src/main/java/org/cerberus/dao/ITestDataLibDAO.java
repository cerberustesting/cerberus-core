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

import java.util.List;
import org.cerberus.entity.TestDataLib;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;

/**
 *
 * @author bcivel
 */
public interface ITestDataLibDAO {

    /**
     *
     * @param testDataLib
     * @return 
     */
    Answer createTestDataLib(TestDataLib testDataLib);

    /**
     *
     * @param testDataLib
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
     * Deletes a testdatalib with basis on the unique identifier
     * @param testDataLibID - test data lib entry
     * @return 
     */
     Answer deleteUnusedTestDataLib(int testDataLibID);
    /**
     *
     * @return All TestData
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

    AnswerItem findTestDataLibByKey(int testDataLibID);
    /**
     *
     * @param searchTerm words to be searched in every column (Exemple :
     * article)
     * @param inds part of the script to add to where clause (Exemple : `type` =
     * 'Article')
     * @return The number of records for these criterias
     */
    Integer getNumberOfTestDataLibPerCriteria(String searchTerm, String inds);
    
    AnswerList<String> getListOfGroupsPerType(String type); 

    Answer createTestDataLibBatch(List<TestDataLib> testDataLibEntries);

    AnswerList findTestDataLibNameList(String testDataLibName, int limit);
}
