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
import org.cerberus.entity.TestData;
import org.cerberus.exception.CerberusException;

/**
 *
 * @author bcivel
 */
public interface ITestDataDAO {

    /**
     *
     * @param testData TestData to insert
     * @throws CerberusException
     */
    void createTestData(TestData testData) throws CerberusException;

    /**
     *
     * @param testData TestData to update using the key
     * @throws CerberusException
     */
    void updateTestData(TestData testData) throws CerberusException;

    /**
     *
     * @param testData TestData to delete
     * @throws CerberusException
     */
    void deleteTestData(TestData testData) throws CerberusException;

    /**
     *
     * @return All TestData
     */
    List<TestData> findAllTestData();

    /**
     *
     * @param start first row of the resultSet
     * @param amount number of row of the resultSet
     * @param column order the resultSet by this column
     * @param dir Asc or desc, information for the order by command
     * @param searchTerm search term on all the column of the resultSet
     * @param individualSearch search term on a dedicated column of the resultSet
     * @return
     */
    List<TestData> findTestDataListByCriteria(int start, int amount, String column, String dir, String searchTerm, String individualSearch);
    
    /**
     * 
     * @param key Key (String) of testData
     * @return 
     */
    TestData findTestDataByKey(String key) throws CerberusException;
}
